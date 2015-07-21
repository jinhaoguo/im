package com.fans.im.logic.common.domain;

import java.util.HashMap;
import java.util.Map;

import com.fans.data.common.exception.CloudPlatformRuntimeException;

/**
 * @author tianhui
 *
 */
public enum ChatMessageType {
	txt(0), img(1), location(2), audio(3);
	
	private int value;
	
	private static Map<Integer, ChatMessageType> map = new HashMap<Integer, ChatMessageType>();
	static{
		for(ChatMessageType chatType : ChatMessageType.values()){
			map.put(chatType.getValue(), chatType);
		}
	}
	
	private ChatMessageType(int value){
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public static ChatMessageType convert(int value){
		ChatMessageType res = map.get(value);
		if(res == null) throw new CloudPlatformRuntimeException("unsupport chat msg type " + value);
		return res;
	}
	
}
