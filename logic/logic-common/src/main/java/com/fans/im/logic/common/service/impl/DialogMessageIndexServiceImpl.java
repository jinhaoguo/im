package com.fans.im.logic.common.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.fans.data.common.rao.GenericIndexRao;
import com.fans.data.common.service.impl.AbstractGenericIndexServiceOnlyRedisImpl;
import com.fans.im.logic.common.domain.DialogMessageIndex;
import com.fans.im.logic.common.rao.DialogMessageIndexRao;
import com.fans.im.logic.common.service.DialogMessageIndexService;

@Service
public class DialogMessageIndexServiceImpl extends
		AbstractGenericIndexServiceOnlyRedisImpl<DialogMessageIndex> implements
		DialogMessageIndexService {
	
	@Resource
	private DialogMessageIndexRao dialogMessageIndexRao;

	@Override
	protected GenericIndexRao<DialogMessageIndex> getRao() {
		return dialogMessageIndexRao;
	}

	@Override
	protected void fillScore(DialogMessageIndex t) {
		//do nothing
	}
}
