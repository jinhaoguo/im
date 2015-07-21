package com.fans.im.logic.svc.api.v1.bean;

/**
 * 用户在线状态
 * @author tianhui
 *
 */
public class UserOnlineStatusBean {
	public String userId;
	public Integer status;// 0离线  1 在线
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	
}
