package com.fans.im.logic.svc.api.v1.restful;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fans.data.common.domain.Appplt;
import com.fans.data.common.exception.InvalidRequestRuntimeException;
import com.fans.data.common.util.JsonUtil;
import com.fans.data.common.util.StringUtil;
import com.fans.data.common.xinterface.ExceptionHandler;
import com.fans.data.common.xinterface.ParameterTool;
import com.fans.data.common.xinterface.ResultBean;
import com.fans.data.inner.follow.v1.service.InnerFollowService;
import com.fans.data.inner.follow.v1.service.InnerUserBlackService;
import com.fans.im.logic.common.domain.ChatMessage;
import com.fans.im.logic.common.domain.ChatMessageType;
import com.fans.im.logic.common.domain.ChatType;
import com.fans.im.logic.common.service.UserImConfigService;
import com.fans.im.logic.common.service.UserOfflineCountService;
import com.fans.im.logic.common.util.DialogIdDecomposeUtil;
import com.fans.im.logic.svc.api.common.LogicRequestExtract;
import com.fans.im.logic.svc.api.v1.bean.MessageCountPostBean;
import com.fans.im.logic.svc.api.v1.bean.MsgAddBean;
import com.fans.im.logic.svc.api.v1.bean.MsgAddResBean;
import com.fans.im.logic.svc.manager.MessageManager;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @author tianhui
 *
 */
@Controller
@RequestMapping("/v1/message")
public class MessageController {
	private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
	
	private static final int STRANGER_NOT_ALLOW = 4221;//陌生人不容许发送
	private static final int ALL_NOT_ALLOW = 4222;//所哟人不容许发送
	private static final int BLACK_NOT_ALLOW_TO = 4223;//对方设置黑名单
	private static final int BLACK_NOT_ALLOW_FROM = 4224;//发送方设置黑名单
	
	
	@Resource
	private MessageManager messageManager;
	
	@Resource
	private UserOfflineCountService userOfflineCountService;
	
	@Resource
	private UserImConfigService userImConfigService;
	
	@Resource(name="innerUserBlackV1Service")
	private InnerUserBlackService innerUserBlackService;
	
	@Resource(name="innerFollowV1Service")
	private InnerFollowService innerFollowService;
	
	@RequestMapping(method = RequestMethod.POST)
	public Object postMessage(HttpServletRequest request,
			HttpServletResponse response, Model model) {
		String appver = null;
		String ip = null;
		Appplt appplt = null;
		
		Monitor monitor = MonitorFactory.start("v1.msg.post");
		try {
			appver = LogicRequestExtract.getAppver(request);
			ip = ParameterTool.getIpAddr(request);
			appplt = LogicRequestExtract.getAppplt(request);
			List<MsgAddBean> addBeans = JsonUtil.jsonsConvert(request, MsgAddBean.class);
			if(addBeans.isEmpty()){
				throw new InvalidRequestRuntimeException("body empty");
			}
			
			logger.info("v1 post msg, userid={}, appplt={}, appver={}, ip={}, body={}",
					new Object[]{addBeans.get(0).getFrom(), appplt, appver, ip, JsonUtil.getJsonFromObject(addBeans)});
		
			String from = addBeans.get(0).getFrom();
			String to = addBeans.get(0).getTo();
			
			if(StringUtil.isEmpty(from)){
				throw new InvalidRequestRuntimeException("body error, no from");
			}
			
			LogicRequestExtract.verifyUserName(request, from);
			
			if(StringUtil.isEmpty(to)){
				throw new InvalidRequestRuntimeException("body error, no to");
			}
			
			if(to.equals(from)){
				throw new InvalidRequestRuntimeException("不能自己给自己发消息");
			}
			
			ChatType chatType = null;
			try {
				chatType = ChatType.convert(addBeans.get(0).getChat_type());
			} catch (Exception e) {
				throw new InvalidRequestRuntimeException("body error, chat_type error");
			}  
			
			switch (chatType) {
			case single:
				//判断对方是否容许发送私信
				Integer allStranger = userImConfigService.getAllStrangerIm(to);
				if(allStranger != null){
					if(allStranger == 1){//对方设置不容许陌生人发送私信
						if(innerFollowService.findRelation(to, from) == 0){
							throw new InvalidRequestRuntimeException("对方设置不容许陌生人发送私信", STRANGER_NOT_ALLOW);
						}

					} else if(allStranger == 2){
						throw new InvalidRequestRuntimeException("对方设置不容许发送私信", ALL_NOT_ALLOW);
					}
				}

				break;

			default:
				break;
			}
						
			if(innerUserBlackService.exist(from, to)){
				throw new InvalidRequestRuntimeException("你已经把对方拉黑", BLACK_NOT_ALLOW_FROM);
			}
			
			if(innerUserBlackService.exist(to, from)){
				throw new InvalidRequestRuntimeException("对方已经把你拉黑", BLACK_NOT_ALLOW_TO);
			}
			
			List<MsgAddResBean> resbeans = new ArrayList<>();
			
			for (MsgAddBean addBean : addBeans) {
				ChatMessageType chatMessageType = null;
				try {
					chatMessageType = ChatMessageType.convert(addBean.getC_type());
				} catch (Exception e) {
					throw new InvalidRequestRuntimeException("body error, c_type error");
				}
				
				switch (chatMessageType) {
				case txt:
					if(StringUtil.isEmpty(addBean.getMsg())){
						throw new InvalidRequestRuntimeException("body error, no msg");
					}
					break;
				case img:
					if(StringUtil.isEmpty(addBean.getImg())){
						throw new InvalidRequestRuntimeException("body error, no img");
					}
					break;
				case location:
					if(StringUtil.isEmpty(addBean.getAddr()) || addBean.getLat() == null || addBean.getLng() == null){
						throw new InvalidRequestRuntimeException("body error, no loc");
					}
					break;
				case audio:
					if(StringUtil.isEmpty(addBean.getAudio()) || addBean.getLen() == null){
						throw new InvalidRequestRuntimeException("body error, no audio");
					}
					break;
				default:
					break;
				}
				
				ChatMessage msg = new ChatMessage();
				msg.setcTime(new Date());
				msg.setAddr(addBean.getAddr());
				msg.setAudio(addBean.getAudio());
				msg.setcType(chatMessageType.getValue());
				msg.setFrom(addBean.getFrom());
				//msg.setTo(addBean.getTo());
				msg.setImg(addBean.getImg());
				msg.setLat(addBean.getLat());
				msg.setLen(addBean.getLen());
				msg.setLng(addBean.getLng());
				msg.setMsg(addBean.getMsg());
				msg.setMsgId(msg.getcTime().getTime() * 1000 + Math.abs((int)(Math.random()*1000)));
				if(!StringUtil.isEmpty(addBean.getTo())){
					msg.setDialogId(DialogIdDecomposeUtil.generateDialogId(
							chatType, addBean.getFrom(), addBean.getTo()));
				} else if(!StringUtil.isEmpty(addBean.getDid())){
					msg.setDialogId(addBean.getDid());
				} else {
					throw new InvalidRequestRuntimeException("body error, did or to must exist");
				}
				
				msg.setChatType(addBean.getChat_type());
				
				messageManager.syncAddMsg(msg);
				MsgAddResBean res = new MsgAddResBean();
				res.setDid(msg.getDialogId());
				res.setMsg_id(msg.getMsgId());
				res.setC_time(msg.getcTime().getTime());
				resbeans.add(res);
			}
			
			return new ResultBean(resbeans, ResultBean.OK);
		} catch (Exception e) {
			ExceptionHandler.handleExcepiton(model, response, e, appplt, appver, ip);
		} finally {
			monitor.stop();
		}
		
		return "";
	}
	
	/**
	 * 清零对话框消息数
	 * 清零所有对话框消息数
	 */
	@RequestMapping(value="/count", method = RequestMethod.POST)
	public Object postCount(HttpServletRequest request,
			HttpServletResponse response, Model model) {
		String appver = null;
		String ip = null;
		Appplt appplt = null;
		
		Monitor monitor = MonitorFactory.start("v1.msg.post");
		try {
			appver = LogicRequestExtract.getAppver(request);
			ip = ParameterTool.getIpAddr(request);
			appplt = LogicRequestExtract.getAppplt(request);
			
			String userId = LogicRequestExtract.verifyAuthUserName(request);
			
			MessageCountPostBean postBean = (MessageCountPostBean)JsonUtil.jsonConvert(request, MessageCountPostBean.class);
			logger.info("v1 post msg count, userid={}, appplt={}, appver={}, ip={}, body={}",
					new Object[]{userId, appplt, appver, ip, JsonUtil.getJsonFromObject(postBean)});
			
			if(postBean.getType() == 0){
				userOfflineCountService.del(userId, postBean.getDid());
			} else {
				userOfflineCountService.delAll(userId);
			}
			
			return ResultBean.SUCCESS_OK;
		} catch (Exception e) {
			ExceptionHandler.handleExcepiton(model, response, e, appplt, appver, ip);
		} finally {
			monitor.stop();
		}
		
		return "";
	}
}
