package com.fans.im.logic.common.rao;

/**
 * @author tianhui
 *
 */
public interface UserLastPositionRao {
	public void set(String userId, String dialogId, long position);
	
	public Long get(String userId, String dialogId);
}
