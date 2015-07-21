package com.fans.im.logic.svc.api.v1.convert;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.fans.data.common.util.JsonUtil;
import com.fans.data.svc.common.service.UserInfoService;
import com.fans.im.logic.common.domain.ChatMessage;
import com.fans.im.logic.common.domain.ChatType;
import com.fans.im.logic.common.domain.DialogMessageIndex;
import com.fans.im.logic.common.domain.UserDialogIndex;
import com.fans.im.logic.common.service.DialogMessageIndexService;
import com.fans.im.logic.common.util.DialogIdDecomposeUtil;
import com.fans.im.logic.svc.api.v1.bean.MsgBean;
import com.fans.im.logic.svc.api.v1.bean.MsgDialogBean;

@Component
public class UserDialogConverter {
	@Resource
	private DialogMessageIndexService dialogMessageIndexService;
	
	@Resource
	private UserInfoService userInfoService;
	
	public List<MsgDialogBean> convert(List<UserDialogIndex> indexes, String userId){
		List<MsgDialogBean> beans = new ArrayList<>(indexes.size());
		for (UserDialogIndex index : indexes) {
			List<Object> results = DialogIdDecomposeUtil.decompostDialogId(index.getDialogId());
			ChatType chatType = (ChatType)results.get(0);
			
			MsgDialogBean bean = new MsgDialogBean();
			bean.setDid(index.getDialogId());
			
			
			bean.setChat_type(chatType.getValue());
			switch (chatType) {
			case single:
				if(userId.equals((String)results.get(1))){
					bean.setTo((String)results.get(2));
				} else {
					bean.setTo((String)results.get(1));
				}
				
				bean.setTo_user(userInfoService.findById(bean.getTo()));
				
				break;
			case group:
				bean.setTo((String)results.get(1));
				break;
			default:
				break;
			}
			
			DialogMessageIndex condition = new DialogMessageIndex();
	    	condition.setDialogId(index.getDialogId());
	    	List<DialogMessageIndex> msgIndexes = dialogMessageIndexService.findByToken(condition, null, null, 2, 0);
	    	if(!msgIndexes.isEmpty()){
	    		
	    		
	    		ChatMessage msgBean = JsonUtil.getObjectFromJson(msgIndexes.get(0).getMsg(), ChatMessage.class);
	    		bean.setMsg_id(msgBean.getMsgId());
	    		//bean.setChat_type(bean.getChat_type());
	    		bean.setC_type(msgBean.getcType());
	    		bean.setFrom(msgBean.getFrom());
	    		bean.setMsg(msgBean.getMsg());
	    		bean.setImg(msgBean.getImg());
	    		bean.setAddr(msgBean.getAddr());
	    		bean.setLat(msgBean.getLat());
	    		bean.setLng(msgBean.getLng());
	    		bean.setAudio(msgBean.getAudio());
	    		bean.setLen(msgBean.getLen());
	    		bean.setC_time(msgBean.getcTime().getTime());
	    		
	    		if(msgIndexes.size() == 2){
	    			MsgBean msgBean2 = JsonUtil.getObjectFromJson(msgIndexes.get(0).getMsg(), MsgBean.class);
	    			bean.setLast_id(msgBean2.getMsg_id());
	    		}
			}
	    	
			beans.add(bean);
		}
		
		return beans;
	}
}
