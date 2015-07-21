package com.fans.im.logic.common.service;

import java.util.List;

import com.fans.im.logic.common.domain.UserOnline;

/**
 * @author tianhui
 *
 */
public interface UserOnlineService {
	public void set(String userId, UserOnline userOnline);
	
	public List<UserOnline> get(String userId);
	
	public boolean exist(String userId);
}
