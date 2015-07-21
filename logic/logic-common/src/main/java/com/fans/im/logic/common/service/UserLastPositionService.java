package com.fans.im.logic.common.service;

/**
 * 不需要此类了。由于notifyBean中设计了lastId。改用UserOfflineCountService做离线消息
 * @author tianhui
 *
 */
@Deprecated
public interface UserLastPositionService {
	public void set(String userId, String dialogId, long position);
	
	public Long get(String userId, String dialogId);
	
	public void zeroCount(String userId, String dialogId);
	
	public void zeroAllCount(String userId);
}
