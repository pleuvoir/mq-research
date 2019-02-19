package io.github.pleuvoir.service;

public interface LiveBeginService {

	void update(String liveId) throws LiveBeginException, LiveNotBeginException;
}
