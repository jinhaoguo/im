package com.fans.im.logic.common.rao.impl;

import java.util.Date;

import com.fans.data.common.domain.redis.TupleStringDouble;
import com.fans.data.common.rao.impl.AbstractGenericIndexRaoImpl;
import com.fans.im.logic.common.domain.UserDialogIndex;
import com.fans.im.logic.common.rao.UserDialogIndexRao;

/**
 * @author tianhui
 *
 */
public class UserDialogIndexRaoImpl extends
		AbstractGenericIndexRaoImpl<UserDialogIndex, TupleStringDouble>
		implements UserDialogIndexRao {

	@Override
	protected Object getValue(UserDialogIndex t) {
		return t.getDialogId();
	}

	@Override
	protected UserDialogIndex convert(Object hashKey, Object keyType,
			TupleStringDouble tuple) {
		UserDialogIndex index = new UserDialogIndex();
		index.setUserId(hashKey.toString());
		index.setDialogId(tuple.getElementId());
		index.setCreateTime(new Date((long)tuple.getScore()/1000));//TODO:不能写死，等消息ID生成规则出来后替换
		index.setScore((long)tuple.getScore());
		
		return index;
	}
}
