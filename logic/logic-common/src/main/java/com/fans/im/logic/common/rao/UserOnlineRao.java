package com.fans.im.logic.common.rao;

import java.util.List;

import com.fans.im.logic.common.domain.UserOnline;

/**
 * 用户在线情况。在哪个端登录的最后登录时间，并显示是否在线。
 * @author tianhui
 *
 */
public interface UserOnlineRao {
	/**
	 * 不存在才更新。
	 * @param userId
	 * @param userOnline
	 */
	public void set(String userId, UserOnline userOnline);
	
	public List<UserOnline> get(String userId);
	
	public boolean exist(String userId);
	
	public void delKey(String userId);
	
	public Boolean delField(String userId, String appplt);
}
