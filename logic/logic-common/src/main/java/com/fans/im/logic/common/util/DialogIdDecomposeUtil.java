package com.fans.im.logic.common.util;

import java.util.ArrayList;
import java.util.List;

import com.fans.data.common.exception.CloudPlatformRuntimeException;
import com.fans.im.logic.common.domain.ChatType;

public class DialogIdDecomposeUtil {
	/**
	 * 分解失败返回null
	 * @param dialogId
	 * @return
	 */
	public static List<Object> decompostDialogId(String dialogId){
		String[] strs = dialogId.split("_");		
		ChatType chatType = ChatType.convert(Integer.valueOf(strs[0]));
		List<Object> result = new ArrayList<Object>(3);
		result.add(chatType);
		switch (chatType) {
		case single:
			if(strs.length != 3){
				return null;
			}
			result.add(strs[1]);
			result.add(strs[2]);
			return result;
		case group:
			if(strs.length != 2){
				return null;
			}
			//TODO:
			
			break;
		default:
			break;
		}
		
		return null;
	}
	
	public static ChatType parseChatType(String dialogId){
		String[] strs = dialogId.split("_");		
		return ChatType.convert(Integer.valueOf(strs[0]));
	}
	
	public static String generateDialogId(ChatType chatType, String from, String to){
		switch (chatType) {
		case single:
			
			if(from.compareToIgnoreCase(to)<0){
				return chatType.getValue() + "_" + from + "_" + to;
			} else {
				return chatType.getValue() + "_" + to + "_" + from;
			}
			
		case group:
			return chatType.getValue() + "_" + to; 
		default:
			throw new CloudPlatformRuntimeException("generate dialogid error, type="
					+ chatType +",from=" + from + ", to="+to);
		}
	}
}
