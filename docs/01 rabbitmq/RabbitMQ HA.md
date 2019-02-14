
## RabbitMQ 集群

### RabbitMQ 內建集群

#### 內建集群的设计目标

1. 允许消费者和生产者在节点崩溃的情况下继续运行
2. 通过添加节点线性扩展消息通信的吞吐量
3. 不能保证消息的万无一失，当某一节点挂掉时会丢失队列及其队列上的所有消息

#### 集群中的队列和交换器

队列(默认不会在集群上复制)

集群中队列信息只在队列的所有者节点保存队列的所有信息，其他节点只知道队列的元数据和指向所有者节点的指针，节点崩溃时，该节点的队列和其上的绑定信息都消失了。
为什么集群不复制队列内容和状态到所有节点：1）存储空间；2）性能，如果消息需要复制到集群中每个节点，网络开销不可避免，持久化消息还需要写磁盘。
所以其他节点接收到不属于该节点的队列的消息时会使用元数据将该消息转发给该队列的所有者节点上。

交换器(会在集群上复制)

本质上是个这个交换器的名称和队列的绑定列表，可以看成一个类似于hashmap的映射表，所以交换器会在整个集群上复制。

元数据(会在集群上复制)

队列元数据：队列名称和属性（是否可持久化，是否自动删除）
交换器元数据：交换器名称、类型和属性
绑定元数据：交换器和队列的绑定列表
vhost元数据：vhost内的相关属性，如安全属性等等

集群中的节点

要么是内存节点，要么是磁盘节点。怎么区分？就是节点将队列、交换器、用户等等信息保存在哪里？单节点肯定是磁盘类型。为了性能的考虑，集群中可以有内存节点。当全部是磁盘节点，当声明队列、交换器等等时，rabbitmq必须将数据保存在所有节点后才能表示操作完成。
Rabbitmq强制要求集群中至少有一个磁盘节点，否则无法构建。从高可用的角度讲官方建议每个集群应该至少配备两个磁盘节点。因为只有一个磁盘节点的情况下，当这个磁盘节点崩溃时，集群可以保持运行，但任何修改操作，比如创建队列、交换器、添加和删除集群节点都无法进行，必须等待磁盘节点恢复才可正常使用上述功能。


#### 构建我们自己的集群

集群常用命令

rabbitmqctl join_cluster [rabbit@node1]将节点加入集群
rabbitmqctl cluster_status 查询集群状态
rabbitmqctl reset 严格来说，不属于集群命令，reset的作用是将node节点恢复为空白状态，包括但不限于，比如，用户信息，虚拟主机信息，所有持久化的消息。在集群下，通过这个命令，可以让节点离开集群。

集群下的注意事项

元数据的变更，我们知道，这些消息都要记录在磁盘节点上。当有节点离开集群时，所有的磁盘节点上都要记录这个信息。如果磁盘节点在离开集群时不用reset命令，会导致集群认为该节点发生了故障，并会一直等待该节点恢复才允许新节点加入，所以，当磁盘节点是被暴力从集群中脱离时，有可能导致集群永久性的无法变更。


多机下的集群

Rabbitmq集群对延迟非常敏感，只能在本地局域网内使用。
1、	修改 /etc/hosts
192.168.1.1 node1
192.168.1.2 node2
192.168.1.3 node3

2、Erlang Cookie 文件：/var/lib/rabbitmq/.erlang.cookie。将 node1 的该文件复制到 node2、node3，由于这个文件权限是 400，所以需要先修改 node2、node3 中的该文件权限为 777，然后将 node1 中的该文件拷贝到 node2、node3，最后将权限和所属用户/组修改回来。

3、运行各节点

4、在node2、node3上分别运行

[root@node2 ~]# rabbitmqctl stop_app
[root@node2 ~]./rabbitmqctl reset
[root@node2 ~]# rabbitmqctl join_cluster rabbit@node1
[root@node2 ~]# rabbitmqctl start_app
rabbitmqctl cluster_status
内存节点则是rabbitmqctl join_cluster rabbit@node1 --ram
移除集群中的节点
[root@node2 ~]# rabbitmqctl stop_app
[root@node2 ~]./rabbitmqctl reset
[root@node2 ~]# rabbitmqctl start_app

RabbitMQ集群高可用

镜像队列

什么是镜像队列

如果RabbitMQ集群是由多个broker节点构成的，那么从服务的整体可用性上来讲，该集群对于单点失效是有弹性的，但是同时也需要注意：尽管exchange和binding能够在单点失效问题上幸免于难，但是queue和其上持有的message却不行，这是因为queue及其内容仅仅存储于单个节点之上，所以一个节点的失效表现为其对应的queue不可用。
引入RabbitMQ的镜像队列机制，将queue镜像到cluster中其他的节点之上。在该实现下，如果集群中的一个节点失效了，queue能自动地切换到镜像中的另一个节点以保证服务的可用性。在通常的用法中，针对每一个镜像队列都包含一个master和多个slave，分别对应于不同的节点。slave会准确地按照master执行命令的顺序进行命令执行，故slave与master上维护的状态应该是相同的。除了publish外所有动作都只会向master发送，然后由master将命令执行的结果广播给slave们，故看似从镜像队列中的消费操作实际上是在master上执行的。
RabbitMQ的镜像队列同时支持publisher confirm和事务两种机制。在事务机制中，只有当前事务在全部镜像queue中执行之后，客户端才会收到Tx.CommitOk的消息。同样的，在publisher confirm机制中，向publisher进行当前message确认的前提是该message被全部镜像所接受了。

镜像队列的配置

```java
Map<String, Object> args = new HashMap<String, Object>();
args.put("x-ha-policy", "all");
//在声明队列时传入
channel.queueDeclare(queueName,false,false, false, args);
```

通过控制台添加策略
镜像队列的配置通过添加policy完成，policy添加的命令为：
`rabbitmqctl set_policy [-p Vhost] Name Pattern Definition [Priority]`
-p Vhost： 可选参数，针对指定vhost下的queue进行设置
Name: policy的名称
Pattern: queue的匹配模式(正则表达式)
Definition：镜像定义，包括三个部分ha-mode, ha-params, ha-sync-mode
    ha-mode:指明镜像队列的模式，有效值为 all/exactly/nodes
        all：表示在集群中所有的节点上进行镜像
        exactly：表示在指定个数的节点上进行镜像，节点的个数由ha-params指定
        nodes：表示在指定的节点上进行镜像，节点名称通过ha-params指定
    ha-params：ha-mode模式需要用到的参数
    ha-sync-mode：进行队列中消息的同步方式，有效值为automatic和manual

priority：可选参数，policy的优先级
例如，对队列名称以"queue_"开头的所有队列进行镜像，并在集群的两个节点上完成进行，policy的设置命令为：
`rabbitmqctl set_policy ha-queue-two "^queue_" '{"ha-mode":"exactly","ha-params":2,"ha-sync-mode":"automatic"}'`
windows下将单引号改为双引号(rabbitmqctl set_policy ha-all "^ha." "{""ha-mode"":""all""}")

补充：
可通过如下命令确认哪些salve在同步：
`rabbitmqctl list_queues name slave_pids synchronised_slave_pids`

手动同步queue：
`rabbitmqctl sync_queue name`

取消queue同步：
`rabbitmqctl cancel_sync_queue name`

### 使用HAProxy 

处理节点选择，故障服务器检测和负载分布可以使用 HAProxy，类似于 nginx

```
============下载并解压============
cd /usr/local/src/
wget http://pkgs.fedoraproject.org/repo/pkgs/haproxy/haproxy-1.7.9.tar.gz/sha512/d1ed791bc9607dbeabcfc6a1853cf258e28b3a079923b63d3bf97504dd59e64a5f5f44f9da968c23c12b4279e8d45ff3bd39418942ca6f00d9d548c9a0ccfd73/haproxy-1.7.9.tar.gz
tar zxvf haproxy-1.7.9.tar.gz

============安装============
cd haproxy-1.7.9
uname -r
3.10.0-514.el7.x86_64
make TARGET=linux310 ARCH=x86_64 PREFIX=/usr/local/haproxy
make install PREFIX=/usr/local/haproxy

==参数说明：
==TARGET=linux310，内核版本，使用uname -r查看内核，如：3.10.0-514.el7，此时该参数就为linux310；kernel ==大于2.6.28的可以用：TARGET=linux2628；
==ARCH=x86_64，系统位数；
==PREFIX=/usr/local/haprpxy #/usr/local/haprpxy，为haprpxy安装路径。

============添加配置文件============
cd /usr/local/haproxy
mkdir conf
cd conf/
vim haproxy.cfg
 
global
        log 127.0.0.1   local0
        maxconn 1000
        daemon
 
defaults
        log     global
        mode    http
        option  httplog
        option  dontlognull
        retries 3
        timeout connect 5000
        timeout client  50000
        timeout server 50000
 
listen admin_stats
        bind 0.0.0.0:1080
        mode http
        option httplog
        maxconn 10
        stats refresh 30s
        stats uri /stats
        stats realm XingCloud\ Haproxy
        stats auth admin:admin
        stats auth  Frank:Frank
        stats hide-version
        stats  admin if TRUE
		
listen rabbitmq_cluster
        bind 0.0.0.0:5670
        mode tcp
        balance roundrobin
		server rabbit01 127.0.0.1:5672 check inter 5000 rise 2 fall 3
		server rabbit02 node2:5672 check inter 5000 rise 2 fall 3
        
		
		
============启动haproxy============
/usr/local/haproxy/sbin/haproxy -f /usr/local/haproxy/conf/haproxy.cfg

============验证============
lsof -i :1080

============访问统计页面============
http://127.0.0.1:1080/stats
```