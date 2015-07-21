package com.fans.im.logic.common.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.fans.data.common.rao.GenericIndexRao;
import com.fans.data.common.service.impl.AbstractGenericIndexServiceOnlyRedisImpl;
import com.fans.im.logic.common.domain.UserDialogIndex;
import com.fans.im.logic.common.rao.UserDialogIndexRao;
import com.fans.im.logic.common.service.UserDialogIndexService;

/**
 * @author tianhui
 *
 */
@Service
public class UserDialogIndexServiceImpl extends
		AbstractGenericIndexServiceOnlyRedisImpl<UserDialogIndex> implements UserDialogIndexService {

	@Resource
	private UserDialogIndexRao userDialogIndexRao;
	
	@Override
	protected GenericIndexRao<UserDialogIndex> getRao() {

		return userDialogIndexRao;
	}

	@Override
	protected void fillScore(UserDialogIndex t) {
		//do nothing
	}

	
}
