
## ActiveMQ安装、部署和运行 

下载 Windows版 ActiveMQ，解压，运行bin目录下的activemq.bat即可。Linux下操作类似（进入bin目录运行./activemq start启动，./activemq stop关闭）。

下载地址：http://activemq.apache.org/activemq-580-release.html

运行后在浏览器中访问http://127.0.0.1:8161/admin，即可看到ActiveMQ的管理控制台，用户名和密码 admin/admin。ActiveMQ中，61616为服务端口，8161为管理控制台端口。

其中ActiveMQ在没有部署的情况下，提供了内建的阉割版消息队列，可以用来测试以及把玩（适合应急和演示、装逼）。

## TOPIC和QUEUE

TOPIC 模式是广播模式，所有在线的消费者都会收到，不在线的不会收到，即使上线后也不会收到。

QUEUE 模式是点对点（P2P）模式，同一时刻一个消息只会发给一个消费者，哪怕没有消费者在线也会保存起来，等到消费者上线后会投递。

## 持久化机制

ActiveMQ 默认使用 kahadb 关系型数据中保存消息的相关信息，在使用 jdbc 时会生成三张表。可以通过研究其生产表的机制，学习中间件创建表的设计。



## 死信

用来保存处理失败或者过期的消息。
 
出现下面情况时，消息会被重发： 

i.	事务会话被回滚。

ii.	事务会话在提交之前关闭。

iii.会话使用CLIENT_ACKNOWLEDGE模式，并且Session.recover()被调用。 

iv.	自动应答失败

当一个消息被重发超过最大重发次数（缺省为6次，消费者端可以修改）时，会给broker发送一个"有毒标记“，这个消息被认为是有问题，这时broker将这个消息发送到死信队列，以便后续处理。 

