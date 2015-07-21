package com.fans.im.logic.common.rao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fans.data.common.proxy.rao.HashCacheRao;
import com.fans.im.logic.common.domain.UserOnline;
import com.fans.im.logic.common.rao.UserOnlineRao;

/**
 * @author tianhui
 *
 */
public class UserOnlineRaoImpl implements UserOnlineRao {
	private HashCacheRao hashCacheRao;
	private Integer seconds;
	private String keyPrefix;
	public void set(String userId, UserOnline userOnline) {
		hashCacheRao.hsetnx(userId, keyPrefix, userOnline.getAppplt().toLowerCase(), 
				convert2Str(userOnline), seconds);
	}
	public List<UserOnline> get(String userId) {
		Map<String, String> map = hashCacheRao.hgetall(userId, keyPrefix);
		if(map == null || map.isEmpty()){
			return null;
		}
		
		List<UserOnline> onlines = new ArrayList<UserOnline>(map.size());
		for (Entry<String, String> entry : map.entrySet()) {
			onlines.add(convert2Object(entry.getKey(), entry.getValue()));
		}
		return onlines;
	}
	
	private String convert2Str(UserOnline userOnline){
		return String.valueOf(userOnline.getLastOnlineTime()*10+userOnline.getStatus());
	}
	
	public boolean exist(String userId) {
		return hashCacheRao.existKey(userId, keyPrefix);
	}
	
	public void delKey(String userId) {
		hashCacheRao.del(userId, keyPrefix);;
	}
	public Boolean delField(String userId, String appplt) {
		return hashCacheRao.hdel(userId, keyPrefix, appplt);
	}

	private UserOnline convert2Object(String appplt, String value){
		UserOnline online = new UserOnline();
		Long v = Long.valueOf(value);
		online.setAppplt(appplt);
		online.setLastOnlineTime(v/10);
		online.setStatus((int)(v%10));
		
		return online;
	}
	
	
	public void setHashCacheRao(HashCacheRao hashCacheRao) {
		this.hashCacheRao = hashCacheRao;
	}
	public void setSeconds(Integer seconds) {
		this.seconds = seconds;
	}
	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

}
