
### RabbitMQ 总结

在使用原生的 Java 客户端测试时发现，当 `channel.queueBind(queueName, Const.EXCHANGE_NAME, routekey);` 队列和交换机通过路由键进行绑定时，如果之前已经绑定过了，此次又换了路由键，那么这两个绑定都是存在的，可以通过管理控制台进行查看。收发消息时会发现之前绑定的依然生效，这点需要注意。

队列一般是伴随消费者创建的，生产者只关心交换机和消费者没有任何关系。

交换机的差异：

direct 是发送方投递消息到交换机， RabbitMQ 根据路由键完全匹配到后会路由到不同的队列，从而消费者就接收到了消息
fanout 是发送方投递消息到交换机， RabbitMQ 直接忽略路由键发送消息到交换机绑定的队列，从而消费者就接收到了消息
topic 可以实现占位符替换的功能， 按照约定的路由键动态配置，具体可参考示例 [topic 交换机](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-native/src/main/java/io/github/pleuvoir/exchange/topic)

#### 可用性配置

生产者设置 `mandatory=true` 后如果投递失败（如未找到绑定的路由键）则会回调，注意：只有失败时有回调，成功时则没有。但此方式只是稍微保证了消息的可靠，因为在回调的过程中如果网络出现异常，则不会收到失败的通知，所以以此来判断成功与否不太可靠。

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

发送消息时开启事务，性能下降严重，官方提出发送方确认模式。