package com.fans.im.logic.common.rao.impl;

import java.util.Date;

import com.fans.data.common.domain.redis.TupleStringDouble;
import com.fans.data.common.rao.impl.AbstractGenericIndexRaoImpl;
import com.fans.im.logic.common.domain.DialogMessageIndex;
import com.fans.im.logic.common.rao.DialogMessageIndexRao;

/**
 * @author tianhui
 *
 */
public class DialogMessageIndexRaoImpl extends
		AbstractGenericIndexRaoImpl<DialogMessageIndex, TupleStringDouble> implements DialogMessageIndexRao {

	@Override
	protected Object getValue(DialogMessageIndex t) {
		return t.getMsg();
	}

	@Override
	protected DialogMessageIndex convert(Object hashKey, Object keyType,
			TupleStringDouble tuple) {
		DialogMessageIndex index = new DialogMessageIndex();
		index.setDialogId(hashKey.toString());
		index.setScore((long)tuple.getScore());
		index.setMsg(tuple.getElementId());
		
		index.setCreateTime(new Date((long)tuple.getScore()/1000));//TODO:不能写死，等消息ID生成规则出来后替换
		return index;
	}

	

}
