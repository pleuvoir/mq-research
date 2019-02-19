package io.github.pleuvoir.service;

import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LiveBeginServiceImpl implements LiveBeginService {

	private final static Logger LOGGER = LoggerFactory.getLogger(LiveBeginServiceImpl.class);

	@Override
	public void update(String liveId) throws LiveBeginException, LiveNotBeginException  {

		if (ThreadLocalRandom.current().nextLong(9) % 3 == 0) {
			LOGGER.warn("live not begin ..");
			throw new LiveNotBeginException("专场未开始");
		}
		if (ThreadLocalRandom.current().nextBoolean()) {
			LOGGER.info("live begin", liveId);
		} else {
			LOGGER.warn("live check fail ..");
			throw new LiveBeginException("专场开始了，但有异常");
		}
	}

}
