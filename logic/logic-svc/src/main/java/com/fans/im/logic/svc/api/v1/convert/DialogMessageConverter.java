package com.fans.im.logic.svc.api.v1.convert;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fans.im.logic.common.domain.ChatMessage;
import com.fans.im.logic.common.domain.DialogMessageIndex;
import com.fans.im.logic.svc.api.v1.bean.MsgBean;

/**
 * @author tianhui
 *
 */
@Component
public class DialogMessageConverter {

	public List<MsgBean> convert(List<DialogMessageIndex> indexes){
		List<MsgBean> beans = new ArrayList<>(indexes.size());
		for (DialogMessageIndex index : indexes) {
			ChatMessage chatMsg = ChatMessage.fromString(index.getMsg());
			
			MsgBean msgBean = new MsgBean();
			msgBean.setAddr(chatMsg.getAddr());
			msgBean.setAudio(chatMsg.getAudio());
			msgBean.setC_time(index.getTime().getTime());
			msgBean.setC_type(chatMsg.getcType());
			msgBean.setFrom(chatMsg.getFrom());
			msgBean.setImg(chatMsg.getImg());
			msgBean.setLat(chatMsg.getLat());
			msgBean.setLen(chatMsg.getLen());
			msgBean.setLng(chatMsg.getLng());
			msgBean.setMsg(chatMsg.getMsg());
			msgBean.setMsg_id(chatMsg.getMsgId());
			
			beans.add(msgBean);
		}
		
		return beans;
	}
}
