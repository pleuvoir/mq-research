package io.github.pleuvoir.redis;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.TimeToLive;


public class RabbitMessageLogCache implements Serializable {

	private static final long serialVersionUID = 1333938552046986878L;

	@Id
	private String messageId; // 消息编号

	private LocalDateTime createTime;

	private RabbitMessageStatusEnum messageStatus;

	@TimeToLive(unit = TimeUnit.HOURS)
	private Long ttl = 24L * 1; // 缓存存在时间（1天）

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}

	public RabbitMessageStatusEnum getMessageStatus() {
		return messageStatus;
	}

	public void setMessageStatus(RabbitMessageStatusEnum messageStatus) {
		this.messageStatus = messageStatus;
	}

	public Long getTtl() {
		return ttl;
	}

	public void setTtl(Long ttl) {
		this.ttl = ttl;
	}

}
