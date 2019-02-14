
:rocket: 常用消息中间件的总结研究<br>

| <img src="docs/01 rabbitmq/RabbitMQ-logo.svg" href="http://www.rabbitmq.com/" width="200" height="100"> |<img src="docs/02 activemq/activemq-logo.png"  href="http://activemq.apache.org/" width="200" height="100"> | <img src="docs/03 rocketmq/rmq-logo.png" href="http://rocketmq.apache.org/" width="200" height="100"> | <img src="docs/04 kafka/logo.png" href="http://kafka.apache.org/" width="200" height="100">|
| :--------: | :---------: | :---------: | :---------: |
| [RabbitMQ](#one-RabbitMQ) | [ActiveMQ](#two-ActiveMQ)|[RocketMQ](#three-RocketMQ) | [Kafka](#four-Kafka) |

<br>

### :one: RabbitMQ

#### 总结

:memo: [RabbitMQ 总结](https://github.com/pleuvoir/mq-research/blob/master/docs/01%20rabbitmq/RabbitMQ%20Summary.md)

#### 参考资料

[朱小厮的博客](https://blog.csdn.net/u013256816)

#### 环境准备

* :memo: [在 Linux 中安装 RabbitMQ](https://pleuvoir.github.io/2017/09/28/rabbitmq-an-zhuang/)
* :memo: [在 Windows 中安装 RabbitMQ](https://github.com/pleuvoir/reference-samples/tree/master/spring-amqp-example)
* :memo: [在 Windows 中安装 RabbitMQ 教程英文版](https://codenotfound.com/rabbitmq-download-install-windows.html)
* :memo: [RabbitMQ 集群及高可用](https://github.com/pleuvoir/mq-research/blob/master/docs/01%20rabbitmq/RabbitMQ%20HA.md)

#### Java 客户端原生用法

* [发送普通消息](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-native/src/main/java/io/github/pleuvoir/exchange/direct)
* [队列和交换器的多重绑定](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-native/src/main/java/io/github/pleuvoir/exchange/direct)
* [一次连接多个信道，每个信道多个消费者消费同一队列](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-native/src/main/java/io/github/pleuvoir/exchange/direct)
* [fanout 交换机](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-native/src/main/java/io/github/pleuvoir/exchange/fanout)
* [topic 交换机](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-native/src/main/java/io/github/pleuvoir/exchange/topic)
* [mandatory 投递失败通知](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-native/src/main/java/io/github/pleuvoir/mandatory)
* [发送方开启事务](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-native/src/main/java/io/github/pleuvoir/transaction)
* [发送方确认模式（确认、批量确认、异步确认）](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-native/src/main/java/io/github/pleuvoir/producerconfirm)
* [拉取消息](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-native/src/main/java/io/github/pleuvoir/pullmessage)
* [消费者手动 ACK（单条确认）](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-native/src/main/java/io/github/pleuvoir/consumerack)
* [消费者手动 ACK（单条确认并开启事务）](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-native/src/main/java/io/github/pleuvoir/consumerack)
* [消费者单次 Reject 或批量 Nack](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-native/src/main/java/io/github/pleuvoir/consumerack)
* [消费者 QOS（批量确认）](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-native/src/main/java/io/github/pleuvoir/qos)
* [死信队列的使用（消息过期、队列过期、被拒绝）](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-native/src/main/java/io/github/pleuvoir/deadletter)
* [队列控制（持久化、消费者独占、队列过期删除、自动删除）](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-native/src/main/java/io/github/pleuvoir/queue)
* [request-response 模式](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-native/src/main/java/io/github/pleuvoir/requestresponse)
* [amq.rabbitmq.log 日志监控](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-native/src/main/java/io/github/pleuvoir/system)

#### 集成 spring 

* [发送 Direct 消息（同时测试消费者自动应答和手动应答）](https://github.com/pleuvoir/mq-research/blob/master/source/rabbitmq/rabbitmq-springmvc/src/main/java/io/github/pleuvoir/rabbit/producer/NormalMessageProducer.java)
* [交换机、路由键都不存在（结果：NACKED，不会触发 mandatory）](https://github.com/pleuvoir/mq-research/blob/master/source/rabbitmq/rabbitmq-springmvc/src/main/java/io/github/pleuvoir/rabbit/producer/NoExchangeProducer.java)
* [生产者发送确认和故障检测（发布消息到不存在的路由键）](https://github.com/pleuvoir/mq-research/blob/master/source/rabbitmq/rabbitmq-springmvc/src/main/java/io/github/pleuvoir/rabbit/producer/ProducerWithConfirmAndReturnCallback.java)
* [延迟消息（5 秒后被消费者收到，区别在 FIFO）](https://github.com/pleuvoir/mq-research/blob/master/source/rabbitmq/rabbitmq-springmvc/src/main/java/io/github/pleuvoir/rabbit/producer/DelayMessageProducer.java)
* [定时消息（5 秒后被消费者收到，依靠临时队列实现）](https://github.com/pleuvoir/mq-research/blob/master/source/rabbitmq/rabbitmq-springmvc/src/main/java/io/github/pleuvoir/rabbit/producer/FixedTimeMessageProducer.java)
* [模拟支付成功异步阶梯通知](https://github.com/pleuvoir/mq-research/blob/master/source/rabbitmq/rabbitmq-springmvc/src/main/java/io/github/pleuvoir/rabbit/consumer/NotifyMessageConsumer.java)

#### 集成 springboot 

* [发送 Direct 消息（同时测试消费者自动应答和手动应答）](https://github.com/pleuvoir/mq-research/blob/master/source/rabbitmq/rabbitmq-springboot/src/test/java/io/github/pleuvoir/consumerack/NormalMessageExampleTests.java)
* [交换机、路由键都不存在（结果：NACKED，不会触发 mandatory）](https://github.com/pleuvoir/mq-research/blob/master/source/rabbitmq/rabbitmq-springboot/src/test/java/io/github/pleuvoir/producerconfirm/NoExchangeProducerExampleTests.java)
* [生产者发送确认和故障检测（发布消息到不存在的路由键）](https://github.com/pleuvoir/mq-research/blob/master/source/rabbitmq/rabbitmq-springboot/src/test/java/io/github/pleuvoir/producerconfirm/ProducerWithConfirmAndReturnCallbackTest.java)
* [延迟消息（5 秒后被消费者收到，区别在 FIFO）](https://github.com/pleuvoir/mq-research/blob/master/source/rabbitmq/rabbitmq-springboot/src/test/java/io/github/pleuvoir/delay/DelayMessageExampleTests.java)
* [定时消息（5 秒后被消费者收到，依靠临时队列实现）](https://github.com/pleuvoir/mq-research/blob/master/source/rabbitmq/rabbitmq-springboot/src/test/java/io/github/pleuvoir/fixedtime/FixTimeMessageExampleTests.java)
* [Topic 交换机](https://github.com/pleuvoir/mq-research/blob/master/source/rabbitmq/rabbitmq-springboot/src/test/java/io/github/pleuvoir/topic/TopicExampleTests.java)

### :two: ActiveMQ

### :three: RocketMQ

### :four: Kafka
