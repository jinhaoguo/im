package com.fans.im.logic.common.domain;

import java.util.Date;

import com.fans.data.common.util.JsonUtil;

/**
 * @author tianhui
 *
 */
public class ChatMessage {
	Long msgId; //消息ID
	Integer chatType;
	Integer cType; //消息内容类型：0 文本消息 1 图片消息 2 位置 3 语音
	String from; // 消息产生者
	//String to;//消息接受者
	String dialogId;
	String msg; //消息内容
	String img; // 图片地址
	String addr; //用户位置
	Float lat;// 纬度
	Float lng;// 经度
	String audio; // 语音地址
	Integer len;// 语音长度，单位s
	Date cTime;// 创建时间，单位s
	
	public Long getMsgId() {
		return msgId;
	}
	public void setMsgId(Long msgId) {
		this.msgId = msgId;
	}
	public Integer getcType() {
		return cType;
	}
	public void setcType(Integer cType) {
		this.cType = cType;
	}
	public String getFrom() {
		return from;
	}
//	public String getTo() {
//		return to;
//	}
//	public void setTo(String to) {
//		this.to = to;
//	}
	
	public String getDialogId() {
		return dialogId;
	}
	public void setDialogId(String dialogId) {
		this.dialogId = dialogId;
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
	public Date getcTime() {
		return cTime;
	}
	public void setcTime(Date cTime) {
		this.cTime = cTime;
	}
	
	
	public Integer getChatType() {
		return chatType;
	}
	public void setChatType(Integer chatType) {
		this.chatType = chatType;
	}
	public static String toString(ChatMessage msg){
		return JsonUtil.getJsonFromObject(msg);
	}

	public static ChatMessage fromString(String str){
		return (ChatMessage)JsonUtil.getObjectFromJson(str, ChatMessage.class);
	}
}
