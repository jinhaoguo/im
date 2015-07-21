package com.fans.im.logic.svc.api.v1.bean;

public class MsgAddResBean {
	String did;
	Long msg_id;
	Long c_time;
	public String getDid() {
		return did;
	}
	public void setDid(String did) {
		this.did = did;
	}
	public Long getMsg_id() {
		return msg_id;
	}
	public void setMsg_id(Long msg_id) {
		this.msg_id = msg_id;
	}
	public Long getC_time() {
		return c_time;
	}
	public void setC_time(Long c_time) {
		this.c_time = c_time;
	}

}
