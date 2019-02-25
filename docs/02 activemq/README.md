
## ActiveMQ安装、部署和运行 

下载 Windows版 ActiveMQ，解压，运行bin目录下的activemq.bat即可。Linux下操作类似（进入bin目录运行./activemq start启动，./activemq stop关闭）。

下载地址：http://activemq.apache.org/activemq-580-release.html

运行后在浏览器中访问http://127.0.0.1:8161/admin，即可看到ActiveMQ的管理控制台，用户名和密码 admin/admin。ActiveMQ中，61616为服务端口，8161为管理控制台端口。

## TOPIC 和 QUEUE

TOPIC 模式是广播模式，所有在线的消费者都会收到，不在线的不会收到，即使上线后也不会收到。

QUEUE 模式是点对点（P2P）模式，同一时刻一个消息只会发给一个消费者，哪怕没有消费者在线也会保存起来，等到消费者上线后会投递。
