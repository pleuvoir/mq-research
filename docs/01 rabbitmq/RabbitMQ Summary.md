
## RabbitMQ 总结

### 1. 容易理解错的坑

在使用原生的 Java 客户端测试时发现，当队列和交换机通过路由键进行绑定时，如果之前已经绑定过了，此次又换了路由键，那么这两个绑定都是存在的，可以通过管理控制台进行查看。收发消息时会发现之前绑定的依然生效，这点需要注意。

声明队列或者交换机时，如果之前已经有同名的则此次直接返回成功，但如果参数有所变化，那么 RabbitMQ 会抛出异常 `channel error; protocol method: #method<channel.close>(reply-code=406, reply-text=PRECONDITION_FAILED`，此时可以通过管理控制台近一步查看具体哪些属性不同，可以选择删除原配置或者新配置的声明改为和原来一致。

队列一般是伴随消费者创建的，生产者只关心交换机和消费者没有任何关系。

消费者只关心队列。

排他队列会在消费者断开连接后自动删除。

在 springboot 中，默认消费者应答为 auto，当有异常抛向容器，容器会 requeue，如果只有一个消费者那么会无限重复，注意需要为 none 或者 manual.

在 spring，最方便的绑定就是使用 `@RabbitListener` 注解。

一次 TCP 连接可以开启多个信道，每个信道可以同时有多个消费者，这些消费者可以同时消费同一队列。

什么是没使用？

* 一定时间内没有 Get 操作发生
* 没有 Consumer 连接在队列上
* 就算一直有消息进入队列，也不算队列在被使用。

消息变成死信一般是以下几种情况：

* 消息被拒绝，并且设置 requeue 参数为 false
* 消息过期
* 队列达到最大长度

单个队列中给消息设置过期时间，满足 FIFO 原则，即队列消息的过期时间必须是递增的，否则会出现队中的消息已到过期时间，但是队首的时间未到，那么队首的时间到达后会发现队中消息和队首消息一起到达死信队列。

消息的持久化设置：默认情况下，队列和交换器在服务器重启后都会消失，消息当然也是。将队列和交换器的 durable 属性设为 true，缺省为 false，但是消息要持久化还不够，还需要将消息在发布前，将投递模式设置为 2。消息要持久化，必须要有持久化的队列、交换器和投递模式都为 2。

如下这种声明队列的方式，会和 RabbitMQ 默认的交换机进行绑定，而路由键则是此处声明队列的名称。

```java
// =============== 使用了 RabbitMQ 系统缺省的交换器==========
// 路由键即为队列名称
@Bean
public Queue helloQueue() {
    return new Queue("hello-queue");
}
```

### 2. 交换机的差异

direct 是发送方投递消息到交换机， RabbitMQ 根据路由键完全匹配到后会路由到不同的队列，从而消费者就接收到了消息
fanout 是发送方投递消息到交换机， RabbitMQ 直接忽略路由键发送消息到交换机绑定的队列，从而消费者就接收到了消息
topic 可以实现占位符替换的功能， 按照约定的路由键动态配置，具体可参考示例 [topic 交换机](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-native/src/main/java/io/github/pleuvoir/exchange/topic)


### 3. 消息发布时的权衡

#### 失败确认

在发送消息时设置 mandatory 标志，告诉 RabbitMQ，如果消息不可路由，应该将消息返回给发送者，并通知失败。可以这样认为，开启 mandatory 是开启故障检测模式。

注意：

* 它只会让 RabbitMQ 向你通知失败，而不会通知成功。如果消息正确路由到队列，则发布者不会受到任何通知。带来的问题是无法确保发布消息一定是成功的，因为通知失败的消息可能会丢失。
* 当消息投递到不存在的交换机时不会触发，只有不可路由时会触发

<img src="rabbitmq-producer.png">

```java
channel.addReturnListener(new ReturnListener() {
	public void handleReturn(int replyCode, String replyText, String exchange, String routingKey,
			AMQP.BasicProperties properties, byte[] body) throws IOException {
		String message = new String(body);
		System.out.println("返回的replyText ：" + replyText);
		System.out.println("返回的exchange ：" + exchange);
		System.out.println("返回的routingKey ：" + routingKey);
		System.out.println("返回的message ：" + message);
	}
});
```

#### 事务

事务的实现主要是对信道（Channel）的设置，主要的方法有三个：

1. channel.txSelect() 声明启动事务模式
2. channel.txCommit() 提交事务
3. channel.txRollback() 回滚事务

以上就完成了事务的交互流程，如果其中任意一个环节出现问题，就会抛出 IoException，这样用户就可以拦截异常进行事务回滚，或决定要不要重复消息。发送消息时开启事务，性能下降严重，大概会降低 2~10 倍的性能。

#### 发送方确认模式

基于事务的性能问题，RabbitMQ 团队为我们拿出了更好的方案，即采用发送方确认模式，该模式比事务更轻量，性能影响几乎可以忽略不计。原理：生产者将信道设置成 confirm 模式，一旦信道进入 confirm 模式，所有在该信道上面发布的消息都将会被指派一个唯一的 ID (从 1 开始)，由这个 ID 在生产者和 RabbitMQ 之间进行消息的确认。

不可路由的消息，当交换器发现，消息不能路由到任何队列，会进行确认操作，表示收到了消息。如果发送方设置了 mandatory 模式,则会先调用 addReturnListener 监听器。

<img src="rabbitmq-no-route.png">

可路由的消息，要等到消息被投递到所有匹配的队列之后，broker 会发送一个确认给生产者(包含消息的唯一 ID)，这就使得生产者知道消息已经正确到达目的队列了，如果消息和队列是可持久化的，那么确认消息会在将消息写入磁盘之后发出，broker 回传给生产者的确认消息中 delivery-tag 域包含了确认消息的序列号。

<img src="rabbitmq-route.png">

confirm 模式最大的好处在于他可以是异步的，一旦发布一条消息，生产者应用程序就可以在等信道返回确认的同时继续发送下一条消息，当消息最终得到确认之后，生产者应用便可以通过回调方法来处理该确认消息，如果 RabbitMQ 因为自身内部错误导致消息丢失，就会发送一条 nack 消息，生产者应用程序同样可以在回调方法中处理该 nack 消息决定下一步的处理。

Confirm 三种实现方式：
1. channel.waitForConfirms() 普通 发送方确认模式；消息到达交换器，就会返回true。
2. channel.waitForConfirmsOrDie() 批量确认模式；使用同步方式等所有的消息发送之后才会执行后面代码，只要有一个消息未到达交换器就会抛出IOException异常。
3. channel.addConfirmListener() 异步监听发送方确认模式；

注意：

* 发送方确认模式，生产者无需消费者也是可以确认成功的，所以发送方确认模式和消费者是没有关系的。发送方异步确认模式同样也有批量确认，什么时候确认完全由 RabbitMQ 自己内部决定。
* 当发送消息到不存在的交换机时会触发确认失败， mandatory 机制不会触发

#### 备用交换器

在第一次声明交换器时被指定，用来提供一种预先存在的交换器，如果主交换器无法路由消息，那么消息将被路由到这个新的备用交换器。

如果发布消息时同时设置了 mandatory 会如何？ 如果主交换器无法路由消息，RabbitMQ 并不会通知发布者，因为，向备用交换器发送消息，表示消息已经被路由了。注意，新的备用交换器就是普通的交换器，没有任何特殊的地方。

使用备用交换器，向往常一样，声明 Queue 和备用交换器，把 Queue 绑定到备用交换器上。然后在声明主交换器时，通过交换器的参数，alternate-exchange，将备用交换器设置给主交换器。
建议备用交换器设置为 faout 类型，Queue绑定时的路由键设置为"#"，由于平时不会这么用所以也就没有写示例。


### 4. 消息接收时的权衡

#### 手动 ACK

当队列中的消息到达消费者后，如果开启手动 ACK 模式，那么 RabbitMQ 会一直等待收到应答，才会删除队列中的消息。什么时候会重发？当处理这条消息的消费者断开连接，RabbitMQ 会重新发送此消息。

#### 手动 ACK 并开启事务

1. autoAck=false 手动应对的时候是支持事务的，也就是说即使你已经手动确认了消息已经收到了，但 RabbitMQ 对消息的确认会等事务的返回结果，再做最终决定是确认消息还是重新放回队列，如果你手动确认之后，又回滚了事务，那么以事务回滚为准，此条消息会重新放回队列。

2. autoAck=true 如果自动确认为 true 的情况是不支持事务的，也就是说你即使在收到消息之后在回滚事务也是于事无补的，队列已经把消息移除了。

#### 消费者业务出错拒绝

消息确认可以让 RabbitMQ 知道消费者已经接受并处理完消息。但是如果消息本身或者消息的处理过程出现问题怎么办？需要一种机制，通知 RabbitMQ，这个消息，我无法处理，请让别的消费者处理。这里就有两种机制，Reject 和 Nack。
Reject 在拒绝消息时，可以使用 requeue 标识，告诉 RabbitMQ 是否需要重新发送给别的消费者。不重新发送，一般这个消息就会被 RabbitMQ 丢弃，如果重新发送如果只有一个消费者可能会一直收到这条消息。Reject一次只能拒绝一条消息，Nack 可以批量拒绝。具体可查看示例 [消费者单次 Reject 或批量 Nack](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-native/src/main/java/io/github/pleuvoir/consumerack)


#### QOS 消费者批量确认模式

批量确认模式，需要自己实现确认的数量逻辑，当达到多少条时进行确认，具体可参照示例 [消费者 QOS（批量确认）](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-native/src/main/java/io/github/pleuvoir/qos) 当然如果没有确认，消息会发生堆积，Unacked 的消息会增加。未确认的消息，当消费者断开后同样会进行重发。

```java
// 开启 qos， 150 表示一次确认的条数， true 代表整个信道每次 150 ， false 是每个消费者一次 150
channel.basicQos(150, true);
```

注意：消费者要使用 QOS 前提是开启了手动确认。另外，还可以基于 consume 和 channel 的粒度进行设置（global）。

prefetchCount：会告诉 RabbitMQ 不要同时给一个消费者推送多于 N 个消息，即一旦有 N 个消息还没有 ack，则该 consumer 将阻塞，该特性可用来防止高并发数据库连接打满。

global：true/false 是否将上面设置应用于 channel，简单点说，就是上面限制是 channel 级别的还是 consumer 级别。

有关数据表明，2500 左右的 QOS 可靠性和性能较优。