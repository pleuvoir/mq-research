package io.github.pleuvoir.delay;

import java.util.Date;

public class OrderExp {

	private Long id;

	private String orderNo;

	private String orderNote;

	private Date insertTime;

	private Long expireDuration;

	private Date expireTime;

	/* 0：未支付；1：已支付；-1：已过期，关闭 */
	private String orderStatus;

	public OrderExp(Long id, String orderNo, String orderNote, Date insertTime, Long expireDuration, Date expireTime,
			String orderStatus) {
		this.id = id;
		this.orderNo = orderNo;
		this.orderNote = orderNote;
		this.insertTime = insertTime;
		this.expireDuration = expireDuration;
		this.expireTime = expireTime;
		this.orderStatus = orderStatus;
	}

	public OrderExp() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo == null ? null : orderNo.trim();
	}

	public String getOrderNote() {
		return orderNote;
	}

	public void setOrderNote(String orderNote) {
		this.orderNote = orderNote == null ? null : orderNote.trim();
	}

	public Date getInsertTime() {
		return insertTime;
	}

	public void setInsertTime(Date insertTime) {
		this.insertTime = insertTime;
	}

	public Long getExpireDuration() {
		return expireDuration;
	}

	public void setExpireDuration(Long expireDuration) {
		this.expireDuration = expireDuration;
	}

	public Date getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}

	public String getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	@Override
	public String toString() {
		return "OrderExp [id=" + id + ", orderNo=" + orderNo + ", orderNote=" + orderNote + ", orderStatus="
				+ orderStatus + "]";
	}

}