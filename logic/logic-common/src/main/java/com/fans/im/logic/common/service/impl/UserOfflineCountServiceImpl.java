package com.fans.im.logic.common.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.fans.im.logic.common.rao.UserOfflineCountRao;
import com.fans.im.logic.common.service.UserOfflineCountService;

/**
 * @author tianhui
 *
 */
@Service
public class UserOfflineCountServiceImpl implements UserOfflineCountService {
	@Resource
	private UserOfflineCountRao userOfflineCountRao;
	
	public void incre(String uid, String dialogId) {
		userOfflineCountRao.incre(uid, dialogId);
	}

	public int get(String uid, String dialogId) {
		return userOfflineCountRao.get(uid, dialogId);
	}

	public void del(String uid, String dialogId) {
		userOfflineCountRao.del(uid, dialogId);
	}

	public void delAll(String uid) {
		userOfflineCountRao.delAll(uid);
	}

}
