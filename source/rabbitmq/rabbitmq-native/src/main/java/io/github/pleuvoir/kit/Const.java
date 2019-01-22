package io.github.pleuvoir.kit;

public interface Const {

	String DIRECT_EXCHANGE_NAME = "direct_logs";
	
	String FANOUT_EXCHANGE_NAME = "fanout_logs";
	
	String TOPIC_EXCHANGE_NAME = "machine_bussniess_logs";
	
	String MANDATORY_EXCHANGE_NAME = "mandatory_test";
	
	String TRANSACTION_EXCHANGE_NAME = "transaction_test";
	
	String PRODUCER_CONFIRM_EXCHANGE_NAME = "producer_confirm_mode_test";
	
	String PRODUCER_ASYNC_CONFIRM_EXCHANGE_NAME = "producer_async_confirm_mode_exchange";
	
	String PULL_EXCHANGE_NAME = "pull_message_test";
	
	String QOS_EXCHANGE_NAME = "qos_exchange";
	
	
	public static interface DLX {
		
		String BEGIN_ARRIAL_EXCHANGE = "begin_arrial_exchange";
		
		String BEGIN_ARRIAL_QUEUE = "begin_arrial_exchange";
		
		String BEGIN_ARRIAL_ROUTEKEY = "begin_arrial_routekey";
		
		String BEGIN_EXCHANGE  = "begin_exchange";
		
		String BEGIN_QUEUE = "begin_queue";
		
		String BEGIN_ROUTEKEY = "begin_routekey";
	}
}
