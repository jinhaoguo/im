package com.fans.im.logic.svc.api.v1.bean;

import com.fans.data.common.msg.push.bean.NotifyBean;
import com.fans.data.common.msg.push.bean.NotifyType;

/**
 * @author tianhui
 *
 */
public class ChatNotifyBean implements NotifyBean{
	Long msg_id; //消息ID
	Long last_id;//用于判断消息是否丢失或断层，为null表示第一条消息
	Integer chat_type;//聊天类型，0 单聊
	Integer c_type; //消息内容类型：0 文本消息 1 图片消息 2 位置 3 语音
	String from; //消息产生者
	String did;//对话框ID
	String msg; //消息内容
	String img; // 图片地址
	String addr; //用户位置
	Float lat;// 纬度
	Float lng;// 经度
	String audio; // 语音地址
	Integer len;// 语音长度，单位s
	Long c_time;// 创建时间，单位s
	
	public Long getMsg_id() {
		return msg_id;
	}
	public void setMsg_id(Long msg_id) {
		this.msg_id = msg_id;
	}
	public Integer getC_type() {
		return c_type;
	}
	public void setC_type(Integer c_type) {
		this.c_type = c_type;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
	}
	public Float getLat() {
		return lat;
	}
	public void setLat(Float lat) {
		this.lat = lat;
	}
	public Float getLng() {
		return lng;
	}
	public void setLng(Float lng) {
		this.lng = lng;
	}
	public String getAudio() {
		return audio;
	}
	public void setAudio(String audio) {
		this.audio = audio;
	}
	public Integer getLen() {
		return len;
	}
	public void setLen(Integer len) {
		this.len = len;
	}
	public Long getC_time() {
		return c_time;
	}
	public void setC_time(Long c_time) {
		this.c_time = c_time;
	}
	public void setLast_id(Long last_id) {
		this.last_id = last_id;
	}
	public void setChat_type(Integer chat_type) {
		this.chat_type = chat_type;
	}
	public void setDid(String did) {
		this.did = did;
	}
	public Long getLast_id() {
		return last_id;
	}
	public Integer getChat_type() {
		return chat_type;
	}
	public String getDid() {
		return did;
	}
	@Override
	public Integer getNotify_type() {
		return NotifyType.chat.getValue();
	}
	
}
