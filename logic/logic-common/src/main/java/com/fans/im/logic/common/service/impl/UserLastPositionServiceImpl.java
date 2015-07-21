package com.fans.im.logic.common.service.impl;

import javax.annotation.Resource;

import com.fans.im.logic.common.rao.UserLastPositionRao;
import com.fans.im.logic.common.service.UserLastPositionService;

/**
 * @author tianhui
 *
 */
@Deprecated
public class UserLastPositionServiceImpl implements UserLastPositionService {

	@Resource
	private UserLastPositionRao userLastPositionRao;
	
	public void set(String userId, String dialogId, long position) {
		userLastPositionRao.set(userId, dialogId, position);
	}

	public Long get(String userId, String dialogId) {
		return userLastPositionRao.get(userId, dialogId);
	}

	public void zeroCount(String userId, String dialogId) {
		// TODO Auto-generated method stub
		
	}

	public void zeroAllCount(String userId) {
		// TODO Auto-generated method stub
		
	}
}
