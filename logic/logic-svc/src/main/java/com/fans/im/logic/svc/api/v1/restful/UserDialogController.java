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
import com.fans.data.common.xinterface.ResultBean;
import com.fans.im.logic.common.domain.UserDialogIndex;
import com.fans.im.logic.common.service.UserDialogIndexService;
import com.fans.im.logic.svc.api.common.LogicRequestExtract;
import com.fans.im.logic.svc.api.v1.bean.MsgDialogBean;
import com.fans.im.logic.svc.api.v1.convert.UserDialogConverter;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @author tianhui
 *
 */
@Controller
@RequestMapping("/v1/user/{userId}/dialog")
public class UserDialogController {
	private static final Logger logger = LoggerFactory.getLogger(UserDialogController.class);
	
	@Resource
	private UserDialogIndexService userDialogIndexService;
	
	@Resource
	private UserDialogConverter userDialogConverter;
	
	@RequestMapping(value="/list", method = RequestMethod.GET)
	public Object getDialogList(@PathVariable String userId, HttpServletRequest request,
			HttpServletResponse response, Model model) {
		String appver = null;
		String ip = null;
		Appplt appplt = null;
		
		Monitor monitor = MonitorFactory.start("v1.user.dialog.list.get");
		try {
			appver = LogicRequestExtract.getAppver(request);
			ip = ParameterTool.getIpAddr(request);
			appplt = LogicRequestExtract.getAppplt(request);
			
			LogicRequestExtract.verifyUserName(request, userId);
			boolean pageAction = LogicRequestExtract.getPageAction(request);
			int ps = LogicRequestExtract.getPageSize(request);
			int asc = LogicRequestExtract.getAsc(request);
			
			logger.info("v1 get user dialog list, userid={}, appplt={}, appver={}, ip={}, action={}, ps={}, asc={}",
					new Object[]{userId, appplt, appver, ip, pageAction, ps, asc});
			
			UserDialogIndex condition = new UserDialogIndex();
			condition.setUserId(userId);
			
			long count = userDialogIndexService.count(condition);
			
			if(pageAction){//跳页
				int pn = LogicRequestExtract.getPageNum(request);
				List<UserDialogIndex> indexes = userDialogIndexService.findByPage(condition, pn, ps, asc);
				if(indexes==null || indexes.isEmpty()){
					return new PagedListRespData<MsgDialogBean>(new ArrayList<MsgDialogBean>(0), pn, ps, (int)count);
				}
				
				return new PagedListRespData<MsgDialogBean>(userDialogConverter.convert(indexes, userId), pn, ps, (int) count);
			} else {//瀑布
				Long nt = LogicRequestExtract.getNextToken(request);
				Long pt = LogicRequestExtract.getPreviousToken(request);
				
				List<UserDialogIndex> indexes = userDialogIndexService.findByToken(condition, nt, pt, ps, asc);
				if(indexes==null || indexes.isEmpty()){
					return new FallListRespData<MsgDialogBean>(new ArrayList<MsgDialogBean>(0), nt!=null?nt.toString():null, pt!=null?pt.toString():null, (int)count);
				}
				
				return new FallListRespData<MsgDialogBean>(userDialogConverter.convert(indexes, userId), 
						String.valueOf(indexes.get(indexes.size()-1).getScore()),
						String.valueOf(indexes.get(0).getScore()),  (int)count);
			}
		} catch (Exception e) {
			ExceptionHandler.handleExcepiton(model, response, e, appplt, appver, ip);
		} finally {
			monitor.stop();
		}
		
		return "";
	}
	
	@RequestMapping(value="/{dialogId}", method = RequestMethod.DELETE)
	public Object delDialog(@PathVariable String userId, @PathVariable String dialogId, HttpServletRequest request,
			HttpServletResponse response, Model model) {
		String appver = null;
		String ip = null;
		Appplt appplt = null;
		
		Monitor monitor = MonitorFactory.start("v1.user.dialog.del");
		try {
			appver = LogicRequestExtract.getAppver(request);
			ip = ParameterTool.getIpAddr(request);
			appplt = LogicRequestExtract.getAppplt(request);
			
			UserDialogIndex condition = new UserDialogIndex();
			condition.setUserId(userId);;
			condition.setDialogId(dialogId);
			userDialogIndexService.del(condition);
		
			return ResultBean.SUCCESS_OK;
		} catch (Exception e) {
			ExceptionHandler.handleExcepiton(model, response, e, appplt, appver, ip);
		} finally {
			monitor.stop();
		}
		
		return "";
	}
}
