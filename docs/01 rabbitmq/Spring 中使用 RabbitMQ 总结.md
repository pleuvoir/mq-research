

spring 消费者抛出异常 消息会进行自动重发，原因 如下

### 消息应答的设置

// TODO

spring 消费消息主要逻辑

```java
private boolean receiveAndExecute(final BlockingQueueConsumer consumer) throws Throwable {
	
	// 如果消费者开启了事务
	if (getTransactionManager() != null) {
		try {
			if (this.transactionTemplate == null) {
				this.transactionTemplate =
						new TransactionTemplate(getTransactionManager(), getTransactionAttribute());
			}
			return this.transactionTemplate
					.execute(status -> {
						RabbitResourceHolder resourceHolder = ConnectionFactoryUtils.bindResourceToTransaction(
								new RabbitResourceHolder(consumer.getChannel(), false),
								getConnectionFactory(), true);
						// unbound in ResourceHolderSynchronization.beforeCompletion()
						try {
							return doReceiveAndExecute(consumer);
						}
						catch (RuntimeException e1) {
							prepareHolderForRollback(resourceHolder, e1);
							throw e1;
						}
						catch (Throwable e2) { //NOSONAR
							// ok to catch Throwable here because we re-throw it below
							throw new WrappedTransactionException(e2);
						}
					});
		}
		catch (WrappedTransactionException e) {
			throw e.getCause();
		}
	}
	
	return doReceiveAndExecute(consumer);
}
```

```java
private boolean doReceiveAndExecute(BlockingQueueConsumer consumer) throws Throwable { //NOSONAR

	// ... 省略一大段开启事务的情况处理

	return consumer.commitIfNecessary(isChannelLocallyTransacted());
}

 // 
public boolean commitIfNecessary(boolean locallyTransacted) throws IOException {

		if (this.deliveryTags.isEmpty()) {
			return false;
		}

		/*
		 * If we have a TX Manager, but no TX, act like we are locally transacted.
		 */
		boolean isLocallyTransacted = locallyTransacted
				|| (this.transactional
				&& TransactionSynchronizationManager.getResource(this.connectionFactory) == null);
		try {

			// 如果不是自动提交或者手动提交，即 acknowledgeMode = AUTO 的情况
			boolean ackRequired = !this.acknowledgeMode.isAutoAck() && !this.acknowledgeMode.isManual();

			if (ackRequired) {  // 进行批量确认
				if (!this.transactional || isLocallyTransacted) {
					long deliveryTag = new ArrayList<Long>(this.deliveryTags).get(this.deliveryTags.size() - 1);
					this.channel.basicAck(deliveryTag, true);
				}
			}

			if (isLocallyTransacted) {
				// For manual acks we still need to commit
				RabbitUtils.commitIfNecessary(this.channel);
			}

		}
		finally {
			this.deliveryTags.clear();
		}

		return true;

	}

```

```java
public static boolean shouldRequeue(boolean defaultRequeueRejected, Throwable throwable, Log logger) {
		boolean shouldRequeue = defaultRequeueRejected ||
				throwable instanceof MessageRejectedWhileStoppingException;
		Throwable t = throwable;
		while (shouldRequeue && t != null) {
			if (t instanceof AmqpRejectAndDontRequeueException) {
				shouldRequeue = false;
			}
			t = t.getCause();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Rejecting messages (requeue=" + shouldRequeue + ")");
		}
		return shouldRequeue;
	}
```


rollbackOnExceptionIfNecessary 