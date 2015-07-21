package com.fans.im.logic.svc.api.v1.restful;

import java.util.Date;

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

import com.fans.data.common.domain.Appplt;
import com.fans.data.common.util.JsonUtil;
import com.fans.data.common.xinterface.ExceptionHandler;
import com.fans.data.common.xinterface.ParameterTool;
import com.fans.data.common.xinterface.ResultBean;
import com.fans.im.logic.common.domain.UserImConfig;
import com.fans.im.logic.common.service.UserImConfigService;
import com.fans.im.logic.svc.api.common.LogicRequestExtract;
import com.fans.im.logic.svc.api.v1.bean.UserConfigBean;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @author tianhui
 *
 */
@Controller
@RequestMapping("/v1/user/{userId}/config")
public class UserImConfigController {
	private static final Logger logger = LoggerFactory.getLogger(UserImConfigController.class);
	
	@Resource
	private UserImConfigService userImConfigService;
	
	@RequestMapping(method = RequestMethod.POST)
	public Object setConfig(@PathVariable String userId, HttpServletRequest request,
			HttpServletResponse response, Model model) {
		String appver = null;
		String ip = null;
		Appplt appplt = null;
		
		Monitor monitor = MonitorFactory.start("v1.user.config.set");
		try {
			appver = LogicRequestExtract.getAppver(request);
			ip = ParameterTool.getIpAddr(request);
			appplt = LogicRequestExtract.getAppplt(request);
			
			LogicRequestExtract.verifyUserName(request, userId);
			
			UserConfigBean configBean = (UserConfigBean) JsonUtil.jsonConvert(request, UserConfigBean.class);
			logger.info("v1 set user config, userid={}, config={}, appplt={}, appver={}, ip={}",
					new Object[]{userId, JsonUtil.getJsonFromObject(configBean), appplt, appver, ip});
			UserImConfig userImConfig = userImConfigService.get(userId);
			if(userImConfig != null){
				userImConfig = new UserImConfig();
				userImConfig.setCreateTime(new Date());
				userImConfig.setUpdateTime(userImConfig.getCreateTime());
			}

			userImConfig.setAllowStrangerIm(configBean.getAllow_stranger_im());
			userImConfig.setUserId(userId);

			userImConfigService.set(userImConfig);
			return ResultBean.SUCCESS_OK;
		} catch (Exception e) {
			ExceptionHandler.handleExcepiton(model, response, e, appplt, appver, ip);
		} finally {
			monitor.stop();
		}
		
		return "";
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public Object getConfig(@PathVariable String userId, HttpServletRequest request,
			HttpServletResponse response, Model model) {
		String appver = null;
		String ip = null;
		Appplt appplt = null;
		
		Monitor monitor = MonitorFactory.start("v1.user.config.get");
		try {
			appver = LogicRequestExtract.getAppver(request);
			ip = ParameterTool.getIpAddr(request);
			appplt = LogicRequestExtract.getAppplt(request);
			
			logger.debug("v1 get user config, userid={}, appplt={}, appver={}, ip={}",
					new Object[]{userId, appplt, appver, ip});
			UserImConfig userImConfig = userImConfigService.get(userId);
			UserConfigBean configBean = new UserConfigBean();
			configBean.setAllow_stranger_im(userImConfig.getAllowStrangerIm());
			
			return configBean;
		} catch (Exception e) {
			ExceptionHandler.handleExcepiton(model, response, e, appplt, appver, ip);
		} finally {
			monitor.stop();
		}
		
		return "";
	}
}
