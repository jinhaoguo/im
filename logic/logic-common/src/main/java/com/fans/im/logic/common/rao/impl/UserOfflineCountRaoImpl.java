package com.fans.im.logic.common.rao.impl;

import java.util.HashMap;
import java.util.Map;

import com.fans.data.common.proxy.rao.HashCacheRao;
import com.fans.im.logic.common.rao.UserOfflineCountRao;

/**
 * @author tianhui
 *
 */
public class UserOfflineCountRaoImpl implements UserOfflineCountRao {
	private HashCacheRao hashCacheRao;
	private Integer seconds;
	private String keyPrefix;
	
	public void incre(String uid, String dialogId) {
		Map<String, Long> fieldValues = new HashMap<String, Long>(1);
		fieldValues.put(dialogId, 1L);
		hashCacheRao.hincreby(uid, keyPrefix, fieldValues, seconds);
	}

	public int get(String uid, String dialogId) {
		String result = hashCacheRao.hget(uid, keyPrefix, dialogId);
		if(result == null){
			return 0;
		} else {
			return Integer.valueOf(result);
		}
	}

	public void del(String uid, String dialogId) {
		hashCacheRao.hdel(uid, keyPrefix, dialogId);
	}

	public void delAll(String uid) {
		hashCacheRao.del(uid, keyPrefix);
	}

	public HashCacheRao getHashCacheRao() {
		return hashCacheRao;
	}

	public void setHashCacheRao(HashCacheRao hashCacheRao) {
		this.hashCacheRao = hashCacheRao;
	}

	public Integer getSeconds() {
		return seconds;
	}

	public void setSeconds(Integer seconds) {
		this.seconds = seconds;
	}

	public String getKeyPrefix() {
		return keyPrefix;
	}

	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}
	
	

}
