package com.fans.im.logic.common.rao.impl;

import com.fans.data.common.proxy.rao.HashCacheRao;
import com.fans.im.logic.common.rao.UserLastPositionRao;

/**
 * @author tianhui
 *
 */
public class UserLastPositionRaoImpl implements UserLastPositionRao {
	private HashCacheRao hashCacheRao;
	private Integer seconds;
	private String keyPrefix;
	
	public void set(String userId, String dialogId, long position) {
		hashCacheRao.hset(userId, keyPrefix, dialogId, String.valueOf(position), seconds);
	}

	public Long get(String userId, String dialogId) {
		String result = hashCacheRao.hget(userId, keyPrefix, dialogId);
		if(result == null){
			return null;
		} else {
			return Long.valueOf(result);
		}
	}

	public void setHashCacheRao(HashCacheRao hashCacheRao) {
		this.hashCacheRao = hashCacheRao;
	}

	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

	public void setSeconds(Integer seconds) {
		this.seconds = seconds;
	}
}
