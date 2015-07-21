package com.fans.im.logic.common.domain;

public class UserOnline {
	private String appplt;//登录点
	
	private long lastOnlineTime;//最近登录时间
	
	private Integer status;//0 离线  1 在线

	public String getAppplt() {
		return appplt;
	}

	public void setAppplt(String appplt) {
		this.appplt = appplt;
	}

	public long getLastOnlineTime() {
		return lastOnlineTime;
	}

	public void setLastOnlineTime(long lastOnlineTime) {
		this.lastOnlineTime = lastOnlineTime;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

}
