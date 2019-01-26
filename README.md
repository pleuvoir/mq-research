
:rocket: 常用消息中间件的总结研究<br>

| <img src="docs/01 rabbitmq/RabbitMQ-logo.svg" href="http://www.rabbitmq.com/" width="200" height="100"> |<img src="docs/02 activemq/activemq-logo.png"  href="http://activemq.apache.org/" width="200" height="100"> | <img src="docs/03 rocketmq/rmq-logo.png" href="http://rocketmq.apache.org/" width="200" height="100"> | <img src="docs/04 kafka/logo.png" href="http://kafka.apache.org/" width="200" height="100">|
| :--------: | :---------: | :---------: | :---------: |
| [RabbitMQ](#one-RabbitMQ) | [ActiveMQ](#two-ActiveMQ)|[RocketMQ](#three-RocketMQ) | [Kafka](#four-Kafka) |

<br>

### :one: RabbitMQ

#### 总结

:memo: [RabbitMQ 总结篇](https://github.com/pleuvoir/mq-research/blob/master/docs/01%20rabbitmq/README.md)

#### 环境准备

* :memo: [在 Linux 中安装 RabbitMQ](https://pleuvoir.github.io/2017/09/28/rabbitmq-an-zhuang/)
* :memo: [在 Windows 中安装 RabbitMQ](https://github.com/pleuvoir/reference-samples/tree/master/spring-amqp-example)

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

* [Spring 中使用 RabbitMQ 示例](https://github.com/pleuvoir/mq-research/tree/master/source/rabbitmq/rabbitmq-springmvc)
* :memo: [Spring 中使用 RabbitMQ 总结](todo)

#### 集成 springboot 

### :two: ActiveMQ

### :three: RocketMQ

### :four: Kafka
