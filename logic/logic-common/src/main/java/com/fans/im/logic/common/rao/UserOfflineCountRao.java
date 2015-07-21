package com.fans.im.logic.common.rao;

/**
 * @author tianhui
 *
 */
public interface UserOfflineCountRao {
	public void incre(String uid, String dialogId);
	public int get(String uid, String dialogId);
	
	
	/**
	 * 清零一个框的消息
	 */
	public void del(String uid, String dialogId);
	/**
	 * 清零所有
	 * @param uid
	 */
	public void delAll(String uid);
}
