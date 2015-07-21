package com.fans.im.logic.common.service.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fans.im.logic.common.dao.UserImConfigDao;
import com.fans.im.logic.common.domain.UserImConfig;
import com.fans.im.logic.common.rao.UserImConfigRao;
import com.fans.im.logic.common.service.UserImConfigService;

/**
 * @author tianhui
 *
 */
@Service
public class UserImConfigServiceImpl implements UserImConfigService {
	private static final Logger logger = LoggerFactory.getLogger(UserImConfigServiceImpl.class);
	
	@Resource
	private UserImConfigRao userImConfigRao;
	
	@Resource
	private UserImConfigDao userImConfigDao;

	public Integer getAllStrangerIm(String userId) {
		String field = "allowstrangerim";
		try {
			String value = userImConfigRao.get(userId, field);
			if(null != value){
				return Integer.valueOf(value);
			}
		} catch (Exception e) {
			logger.warn("config cache error", e);
		}
		
		UserImConfig t = userImConfigDao.query(userId);
		if(t == null){
			t = new UserImConfig();
			t.setAllowStrangerIm(0);
		}
		
		try {
			userImConfigRao.add(t);
		} catch (Exception e) {
			logger.warn("config cache error", e);
		}
		
		return t.getAllowStrangerIm();
	}

	public void set(UserImConfig userImConfig) {
		// 记住有可能数据库中没有，redis为了缓存不存在的情况，存在这个key
		try {
			userImConfigRao.update(userImConfig);
		} catch (Exception e) {
			logger.warn("config cache error", e);
		}
		
		userImConfigDao.save(userImConfig, true);
	}

	public UserImConfig get(String userId) {
		UserImConfig config;
		try {
			config = userImConfigRao.get(userId);
			if(null != config){
				return config;
			}
		} catch (Exception e) {
			logger.warn("config cache error", e);
		}
		
		UserImConfig t = userImConfigDao.query(userId);
		if(t == null){
			t = new UserImConfig();
			t.setAllowStrangerIm(0);
		}
		
		try {
			userImConfigRao.add(t);
		} catch (Exception e) {
			logger.warn("config cache error", e);
		}
		
		return t;
	}

}
