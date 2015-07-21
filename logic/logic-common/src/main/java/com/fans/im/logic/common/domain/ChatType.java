package com.fans.im.logic.common.domain;

import java.util.HashMap;
import java.util.Map;

import com.fans.data.common.exception.CloudPlatformRuntimeException;

/**
 * @author tianhui
 *
 */
public enum ChatType {
	single(0), group(1);
	
	private int value;
	
	private static Map<Integer, ChatType> map = new HashMap<Integer, ChatType>();
	static{
		for(ChatType chatType : ChatType.values()){
			map.put(chatType.getValue(), chatType);
		}
	}
	
	private ChatType(int value){
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public static ChatType convert(int value){
		ChatType res = map.get(value);
		if(res == null) throw new CloudPlatformRuntimeException("unsupport chat type " + value);
		return res;
	}
	
}
