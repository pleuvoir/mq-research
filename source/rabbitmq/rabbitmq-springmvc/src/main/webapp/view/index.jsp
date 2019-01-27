<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<base href="<%=basePath%>">

<title>RabbitMQ 程序</title>

<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<script src="//cdn.bootcss.com/jquery/2.2.4/jquery.min.js"></script>
<style type="text/css">
.h1 {
	margin: 0 auto;
}

#producer{
	width: 48%;
 	border: 1px solid blue; 
	height: 80%;
	align:center;
	margin:0 auto;
}

body{
	text-align :center;
} 
div {
	text-align :center;
}
textarea{
	width:80%;
	height:100px;
	border:1px solid gray;
}
button{
	background-color: rgb(62, 156, 66);
	border: none;
	font-weight: bold;
	color: white;
	height:30px;
}
</style>
<script type="text/javascript">
	
	function send(controller){
		if($("#message").val()==""){
			$("#message").css("border","1px solid red");
			return;
		}else{
			$("#message").css("border","1px solid gray");
		}
		$.ajax({
			type: 'post',
			url:'<%=basePath%>'+controller,
			dataType:'text', 
			data:{"message":$("#message").val()},
			success:function(data){
				if(data=="suc"){
					$("#status").html("<font color=green>发送成功</font>");
					setTimeout(clear,1000);
				}else{
					$("#status").html("<font color=red>"+data+"</font>");
					setTimeout(clear,5000);
				}
			},
			error:function(data){
				$("#status").html("<font color=red>ERROR:"+data["status"]+","+data["statusText"]+"</font>");
				setTimeout(clear,5000);
			}
			
		});
	}
	
	function clear(){
		$("#status").html("");
	}

</script>
</head>

<body>
	<h1>Hello RabbitMQ</h1>
	<div id="producer">
		<h2>Producer</h2>
		<textarea id="message">我是消息</textarea>
		<br>
		<button onclick="send('normalMessageProducer')">发送 Direct 消息（同时测试消费者自动应答和手动应答）</button> <br/><br/>
		<button onclick="send('noExchangeProducer')">交换机、路由键都不存在（结果：NACKED，不会触发 mandatory）</button> <br/><br/>
		<button onclick="send('producerWithConfirmAndReturnCallback')">生产者发送确认和故障检测（发布消息到不存在的路由键）</button> <br/><br/>
		<button onclick="send('delayMessageProducer')">延迟消息（5 秒后被消费者收到，区别在 FIFO）</button> <br/><br/>
		<button onclick="send('fixedTimeMessageProducer')">定时消息（5 秒后被消费者收到，依靠临时队列实现）</button> <br/><br/>
		<br>
		<span id="status"></span>
	</div>
</body>
</html>
