package com.fans.im.logic.common.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.fans.im.logic.common.domain.UserOnline;
import com.fans.im.logic.common.rao.UserOnlineRao;
import com.fans.im.logic.common.service.UserOnlineService;

@Service("userOnlineService")
public class UserOnlineServiceImpl implements UserOnlineService {

	@Resource
	private UserOnlineRao userOnlineRao;
	
	public void set(String userId, UserOnline userOnline) {
		if(userOnline.getStatus() == 0){//离线删除redis
			userOnlineRao.delField(userId, userOnline.getAppplt());
		} else {//不存在才更新，最近更新时间以第一次回报为准
			userOnlineRao.set(userId, userOnline);
		}
	}

	public List<UserOnline> get(String userId) {
		return userOnlineRao.get(userId);
	}

	public boolean exist(String userId) {
		return userOnlineRao.exist(userId);
	}

}
