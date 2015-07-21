package com.fans.im.logic.common.service;

import com.fans.im.logic.common.domain.UserImConfig;


/**
 * @author tianhui
 *
 */
public interface UserImConfigService {
	
	/**
	 * 设置所有配置。如果字段为null，不做改变
	 * @param userImConfig
	 */
	public void set(UserImConfig userImConfig);
	
	/**
	 * 设置所有配置。如果字段为null，不做改变
	 * @param userImConfig
	 */
	public UserImConfig get(String userId);
	
	/**
	 * @param userId
	 * @return
	 */
	public Integer getAllStrangerIm(String userId);

}
