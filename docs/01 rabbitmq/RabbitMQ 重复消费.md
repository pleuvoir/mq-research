
本文记录 RabbitMQ 重复消费的思路以及解决方案。

## 原因

### 生产者重试

RabbitMQ 提供事务机制，但由于极大降低性能，故不在本文讨论范畴之中。常见的为了保证消息的成功到达 broker，可以选择开启发送方异步确认模式。broker 在收到消息后会回调应用程序 ack 或者 nack。什么时候回调是由 broker 自己决定，当负载较高时可能需要更久的时间。 一般而言，当收到 ack 时我们认为消息已成功到达 broker 并且已经处理成功，nack 代表消息已经丢失（注意：丢失的消息也有可能已经投递给消费者，broker 也不清楚是什么情况）。网上很多博客一般解决可靠投递问题，都会选择在此处做文章。譬如：使用消息记录表维护状态，收到 ack 时为已投递，nack 为投递失败；还有种是没收到 ack 或者 nack 时会用分布式定时任务去扫进行重试。

> Acks represent messages handled successfully; Nacks represent messages lost by the broker.  Note, the lost messages 
> could still have been delivered to consumers, but the broker cannot guarantee this.

换言之，采用这种策略是没什么问题。但是因为重试带来了新的问题，消息的重复。

### 消费者确认异常

自动 ack 并不存在这种问题，因为消息离开 broker 就已经被移除了。我们来看看手动 ack：

1. 业务代码报错了，ack 没执行，此种情况会发生在 ack 没放在 finally 的情况下。需不需要放在 finally 代码块中取决于业务。之前还遇到过依赖的 redis 集群网络闪断导致业务代码报错而能确认。

2. ack 时超时了
3. ack broker 收到了，但 broker 出问题了

RabbitMQ 会在消费者连接断开后将 unacked 的消息重新投递给消费者，当然也可能会发送给原来的消费者。

## 解决方案

生产者不重试，消费者做幂等。

消费者做幂等一般有如下几种情况：

1. 业务天然是幂等的

比如 `update user_acc set banlance = 100 where userId = "001"`，这种情况就不用处理了。

2. 乐观锁

使用乐观锁控制，如果抛出乐观锁异常则 ack 即可，当然其他可以判断业务是否执行过的方式也可以。

3. 消息表

通过消息表来确认消息消费状态，当消费成功时不在进行处理即可。切记，消息表状态的更新必须和业务放在同一个事务中，并且是最后一步。

4. redis

和消息表的原理类似，使用 redis 来进行消息状态的控制。尤其是 `spring-data-redis` 提供了面向对象的 `CRUD` 形式，使我们可以更方便的操作内存数据库。

## 思考

`spring-amqp` 提供了更方便的方法去使用 RabbitMQ，可以通过学习源码的形式了解其面对异常时的思路，譬如，自动应答时面对 io 异常是如何处理的。

生产者发送消息时默认提供标识，避免驳杂的标识，如使用 uuid。

```java
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Bean
public RabbitTemplate getRabbitTemplate(ConnectionFactory connectionFactory) {
	RabbitTemplate template = new RabbitTemplate(connectionFactory);
	template.setBeforePublishPostProcessors(new MessagePostProcessor() { // 携带消息 id
		@Override
		public Message postProcessMessage(Message message) throws AmqpException {
			MessageProperties messageProperties = message.getMessageProperties();
			ThreadLocalRandom random = ThreadLocalRandom.current();
			UUID fastUUID = new UUID(random.nextLong(), random.nextLong());
			messageProperties.setMessageId(fastUUID.toString().replaceAll("-", ""));
			return message;
		}
	});
	return template;
}
```

使用模版方法，将业务代码和消费幂等处理的操作分离开。

```java
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
public <T> T excute(RabbitConsumeCallBack<T> callBack, String messageId) {
	checkCallBack(callBack);
	T result = callBack.doInTransaction();
	logger.info("已经更新为消息处理成功，messageId={}", messageId);
	return result;
}
```

