package io.github.pleuvoir.service;

import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserAccServiceImpl implements UserAccService {

	private final static Logger LOGGER = LoggerFactory.getLogger(UserAccServiceImpl.class);

	@Override
	public void update(String userId) throws BussinessException {

		if (ThreadLocalRandom.current().nextBoolean()) {
			LOGGER.info("update p_mer_pay .. {}", userId);
			LOGGER.info("update user_acc .. {}", userId);
			LOGGER.info("update user_acc_detail .. {}", userId);
		} else {
			throw new BussinessException("出现异常");
		}
	}

}
