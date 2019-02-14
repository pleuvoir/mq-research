package io.github.pleuvoir.kit;

public class RabbitConst {

	/** 开始 */
	public static class Begin {

		public static final String EXCHANGE 	= "x.begin";

		public static final String QUEUE 		= "q.begin";

		public static final String ROUTING_KEY 	= "r.begin";
	}

	/** 到达开始时间 （死信队列）*/
	public static class BeginArrival {

		public static final String EXCHANGE 	= "x.beginArrival";

		public static final String QUEUE 		= "q.beginArrival";

		public static final String ROUTING_KEY 	= "r.beginArrival";
	}

	
	public static class Normal {

		public static final String EXCHANGE 	= "x.normal";

		public static final String QUEUE 		= "q.normal";

		public static final String ROUTING_KEY 	= "r.normal";
	}
	
	public static class RateLimit {

		public static final String EXCHANGE 	= "x.rateLimit";

		public static final String QUEUE 		= "q.rateLimit";

		public static final String ROUTING_KEY 	= "r.rateLimit";
	}
	
	// 通知队列
	public static class Notify {

		public static final String EXCHANGE 	= "x.notify";

		public static final String QUEUE 		= "q.notify";

		public static final String ROUTING_KEY 	= "r.notify";
	}
	
	/**
	 * 定时队列 （死信队列）
	 */
	public static class FixedTime {

		public static final String EXCHANGE 	= "x.fixedTime";

		public static final String QUEUE 		= "q.fixedTime";

		public static final String ROUTING_KEY 	= "r.fixedTime";
	}
	
}
