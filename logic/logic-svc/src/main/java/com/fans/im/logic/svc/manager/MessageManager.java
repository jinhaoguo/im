package com.fans.im.logic.svc.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fans.data.common.msg.kafka.service.MessageSender;
import com.fans.data.common.msg.push.bean.PushBean;
import com.fans.im.logic.common.domain.ChatMessage;
import com.fans.im.logic.common.domain.DialogMessageIndex;
import com.fans.im.logic.common.domain.UserDialogIndex;
import com.fans.im.logic.common.domain.UserOnline;
import com.fans.im.logic.common.service.DialogMessageIndexService;
import com.fans.im.logic.common.service.UserDialogIndexService;
import com.fans.im.logic.common.service.UserImConfigService;
import com.fans.im.logic.common.service.UserOfflineCountService;
import com.fans.im.logic.common.service.UserOnlineService;
import com.fans.im.logic.common.util.DialogIdDecomposeUtil;
import com.fans.im.logic.svc.api.v1.bean.ChatNotifyBean;

/**
 * @author tianhui
 *
 */
@Service
public class MessageManager {
	private static final Logger logger = LoggerFactory.getLogger(MessageManager.class);
	
	@Resource
	private DialogMessageIndexService dialogMessageIndexService;
	
	@Resource
	private UserDialogIndexService userDialogIndexService;
	
	@Resource(name="imDispathMessageSender")
	private MessageSender imDispathMessageSender;
	
	@Resource(name="imNotifyMessageSender")
	private MessageSender imNotifyMessageSender;
	
	@Resource
	private UserOnlineService userOnlineService;
	
	@Resource
	private UserOfflineCountService userOfflineCountService;
	
	@Resource
	private UserImConfigService userImConfigService;
	
//	@Resource
//	private PushMsgSender pushMsgSender;
//	
//	@Resource
//	private UserInfoService userInfoService;
	
	public void syncAddMsg(ChatMessage msg){
		//消息体和自己的对话框同步，对方的对话框类别更新异步
		DialogMessageIndex dialogMessageIndex = new DialogMessageIndex();
		dialogMessageIndex.setCreateTime(msg.getcTime());
		dialogMessageIndex.setDialogId(msg.getDialogId());
		dialogMessageIndex.setScore(msg.getMsgId());
		dialogMessageIndex.setTime(msg.getcTime());
		dialogMessageIndex.setMsg(ChatMessage.toString(msg));
		dialogMessageIndexService.add(dialogMessageIndex);
		
		UserDialogIndex userDialogIndexFrom = new UserDialogIndex();
		userDialogIndexFrom.setCreateTime(msg.getcTime());
		userDialogIndexFrom.setDialogId(msg.getDialogId());
		userDialogIndexFrom.setScore(msg.getcTime().getTime());
		userDialogIndexFrom.setTime(msg.getcTime());
		userDialogIndexFrom.setUserId(msg.getFrom());
		userDialogIndexService.add(userDialogIndexFrom);
		
//		List<String> senders = calcSendUsers(dialogId, msg.getFrom());
//		for (String sender : senders) {
//			UserDialogIndex userDialogIndexTo = new UserDialogIndex();
//			userDialogIndexTo.setCreateTime(msg.getcTime());
//			userDialogIndexTo.setDialogId(dialogId);
//			userDialogIndexTo.setScore(msg.getcTime().getTime());
//			userDialogIndexTo.setTime(msg.getcTime());
//			userDialogIndexTo.setUserId(sender);
//			
//			userDialogIndexService.add(userDialogIndexTo);
//		}
	

		try {
			// send msg
			imDispathMessageSender.send(msg);
		} catch (Exception e) {
			logger.warn("im notify send kafka error", e);
		}
	}
	
	public void asyncAddMsg(ChatMessage msg){
		List<String> senders = calcSendUsers(msg);
		for (String sender : senders) {
			UserDialogIndex userDialogIndexTo = new UserDialogIndex();
			userDialogIndexTo.setCreateTime(msg.getcTime());
			userDialogIndexTo.setDialogId(msg.getDialogId());
			userDialogIndexTo.setScore(msg.getcTime().getTime());
			userDialogIndexTo.setTime(msg.getcTime());
			userDialogIndexTo.setUserId(sender);
			
			userDialogIndexService.add(userDialogIndexTo);
		}
		
		Iterator<String> iter = senders.iterator();
//		UserBean userBean = null;
		while(iter.hasNext()){
			String send = iter.next();
			List<UserOnline> onlines = userOnlineService.get(send);
			if(onlines == null || onlines.isEmpty()){//用户不在线
				userOfflineCountService.incre(send, msg.getDialogId());
				iter.remove();
				
//				if(userBean == null){{
//					userBean = userInfoService.findById(msg.getFrom());
//				}
//				
//				String msgstr = null;
//				switch (ChatMessageType.convert(msg.getcType())) {
//				case txt:
//					msgstr = userBean.getNickname() + ":" + msg.getMsg().substring(12) + "..";;
//					break;
//				case audio:
//					msgstr = userBean.getNickname() + ": 发来一条语音";
//					break;
//				case img:
//					msgstr = userBean.getNickname() + ": 发来一张图片";
//					break;
//				case location:
//					msgstr = userBean.getNickname() + ": 发来位置信息";
//					break;
//				default:
//					break;
//				}
//				
//				//离线push
//				pushMsgSender.send(new ImChatMessage(msgstr, send, msg.getDialogId()));
			}
		}
		
		if(senders.isEmpty()){//不用发了。
			return ;
		}
		
    	ChatNotifyBean notifyBean = new ChatNotifyBean();
    	notifyBean.setAddr(msg.getAddr());
    	notifyBean.setAudio(msg.getAudio());
    	notifyBean.setChat_type(msg.getChatType());
    	notifyBean.setC_time(msg.getcTime().getTime());
    	notifyBean.setC_type(msg.getcType());
    	notifyBean.setDid(msg.getDialogId());
    	notifyBean.setFrom(msg.getFrom());
    	notifyBean.setImg(msg.getImg());
    	notifyBean.setLat(msg.getLat());
    	notifyBean.setLen(msg.getLen());
    	notifyBean.setLng(msg.getLng());
    	notifyBean.setMsg(msg.getMsg());
    	notifyBean.setMsg_id(msg.getMsgId());
    	DialogMessageIndex condition = new DialogMessageIndex();
    	condition.setDialogId(msg.getDialogId());
    	
    	List<DialogMessageIndex> indexes = dialogMessageIndexService.findByToken(condition, msg.getMsgId(), null, 1, 0);
		if(!indexes.isEmpty()){
			notifyBean.setLast_id(indexes.get(0).getScore());
		}
		
		//用户不在线，不用给用户发消息。
		try {
			imNotifyMessageSender.send(new PushBean(senders, notifyBean));
		} catch (Exception e) {
			logger.warn("im notify send kafka error", e);
		}
	}
	
	
	private List<String> calcSendUsers(ChatMessage msg){
		//只算单聊
		List<Object> result = DialogIdDecomposeUtil.decompostDialogId(msg.getDialogId());
		
		List<String> senders = new ArrayList<>();
		//senders.add(msg.getTo());
		if(result.get(1).toString().equals(msg.getFrom())){
			senders.add(result.get(2).toString());
		} else {
			senders.add(result.get(1).toString());
		}
		
		return senders;
	}
}
