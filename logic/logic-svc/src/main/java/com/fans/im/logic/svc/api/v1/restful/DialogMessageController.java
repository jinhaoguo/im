package com.fans.im.logic.svc.api.v1.restful;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fans.data.common.bean.result.FallListRespData;
import com.fans.data.common.bean.result.PagedListRespData;
import com.fans.data.common.domain.Appplt;
import com.fans.data.common.xinterface.ExceptionHandler;
import com.fans.data.common.xinterface.ParameterTool;
import com.fans.im.logic.common.domain.DialogMessageIndex;
import com.fans.im.logic.common.service.DialogMessageIndexService;
import com.fans.im.logic.svc.api.common.LogicRequestExtract;
import com.fans.im.logic.svc.api.v1.bean.MsgBean;
import com.fans.im.logic.svc.api.v1.convert.DialogMessageConverter;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @author tianhui
 *
 */
@Controller
@RequestMapping("/v1/dialog/{dialogId}/message")
public class DialogMessageController {
	private static final Logger logger = LoggerFactory.getLogger(DialogMessageController.class);
	
	@Resource
	private DialogMessageIndexService dialogMessageIndexService;
	
	@Resource
	private DialogMessageConverter dialogMessageConverter;
	
	@RequestMapping(value="/list", method = RequestMethod.GET)
	public Object getMessageList(@PathVariable String dialogId, HttpServletRequest request,
			HttpServletResponse response, Model model) {
		String appver = null;
		String ip = null;
		Appplt appplt = null;
		
		Monitor monitor = MonitorFactory.start("v1.dialog.message.list.get");
		try {
			appver = LogicRequestExtract.getAppver(request);
			ip = ParameterTool.getIpAddr(request);
			appplt = LogicRequestExtract.getAppplt(request);
			
			String userId = LogicRequestExtract.verifyAuthUserName(request);
			boolean pageAction = LogicRequestExtract.getPageAction(request);
			int ps = LogicRequestExtract.getPageSize(request);
			int asc = LogicRequestExtract.getAsc(request);
			
			logger.info("v1 get dialog message list, userid={}, appplt={}, appver={}, ip={}, action={}, ps={}, asc={}",
					new Object[]{userId, appplt, appver, ip, pageAction, ps, asc});
			
			DialogMessageIndex condition = new DialogMessageIndex();
			condition.setDialogId(dialogId);
			
			long count = dialogMessageIndexService.count(condition);
			
			if(pageAction){//跳页
				int pn = LogicRequestExtract.getPageNum(request);
				List<DialogMessageIndex> indexes = dialogMessageIndexService.findByPage(condition, pn, ps, asc);
				if(indexes == null || indexes.isEmpty()){
					return new PagedListRespData<MsgBean>(new ArrayList<MsgBean>(0), pn, ps, (int)count);
				}
				
				return new PagedListRespData<MsgBean>(dialogMessageConverter.convert(indexes), pn, ps, (int) count);
			} else {//瀑布
				Long nt = LogicRequestExtract.getNextToken(request);
				Long pt = LogicRequestExtract.getPreviousToken(request);
				
				List<DialogMessageIndex> indexes = dialogMessageIndexService.findByToken(condition, nt, pt, ps, asc);
				if(indexes == null || indexes.isEmpty()){
					return new FallListRespData<MsgBean>(new ArrayList<MsgBean>(0), nt!=null?nt.toString():null, pt!=null?pt.toString():null, (int)count);
				}
				
				return new FallListRespData<MsgBean>(dialogMessageConverter.convert(indexes),
						String.valueOf(indexes.get(indexes.size()-1).getScore()), 
						String.valueOf(indexes.get(0).getScore()), (int)count);
			}
		} catch (Exception e) {
			ExceptionHandler.handleExcepiton(model, response, e, appplt, appver, ip);
		} finally {
			monitor.stop();
		}
		
		return "";
	}
	
//	@RequestMapping(value="/ack", method = RequestMethod.POST)
//	public Object ackMessage(@PathVariable String dialogId, HttpServletRequest request,
//			HttpServletResponse response, Model model) {
//		String appver = null;
//		String ip = null;
//		Appplt appplt = null;
//		
//		Monitor monitor = MonitorFactory.start("v1.msg.ack");
//		try {
//			appver = LogicRequestExtract.getAppver(request);
//			ip = ParameterTool.getIpAddr(request);
//			appplt = LogicRequestExtract.getAppplt(request);
//			
//			String userId = LogicRequestExtract.verifyAuthUserName(request);
//			
//			MsgAckBean ackBean = (MsgAckBean) JsonUtil.jsonConvert(request, MsgAckBean.class);
//			logger.info("v1 ack msg, userid={}, appplt={}, appver={}, ip={}, body={}",
//					new Object[]{userId, appplt, appver, ip, JsonUtil.getJsonFromObject(ackBean)});
//
//			userLastPositionService.set(userId, dialogId, ackBean.getPos());
//		         
//			return ResultBean.SUCCESS_OK;
//		} catch (Exception e) {
//			ExceptionHandler.handleExcepiton(model, response, e, appplt, appver, ip);
//		} finally {
//			monitor.stop();
//		}
//		
//		return "";
//	}
}
