package com.fans.im.logic.svc.api.v1.restful;

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
import com.fans.data.common.xinterface.ExceptionHandler;
import com.fans.data.common.xinterface.ParameterTool;
import com.fans.im.logic.svc.api.common.LogicRequestExtract;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @author tianhui
 *
 */
@Controller
@RequestMapping("/v1/pushserver")
public class ScheduleController {
	private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

	@Resource
	private List<String> pushServers;
	
	@RequestMapping(method = RequestMethod.GET)
	public Object getPushServer(HttpServletRequest request,
			HttpServletResponse response, Model model) {
		String appver = null;
		String ip = null;
		Appplt appplt = null;
		
		Monitor monitor = MonitorFactory.start("v1.push.server.get");
		try {
			appver = LogicRequestExtract.getAppver(request);
			ip = ParameterTool.getIpAddr(request);
			appplt = LogicRequestExtract.getAppplt(request);
			
			String userId = LogicRequestExtract.getAuthUserName(request);
			
			String mac = ParameterTool.getParameterString(request, "mac");
			
			logger.info("v1 get push server, mac={}, userid={}, appplt={}, appver={}, ip={}",
					new Object[]{mac, userId, appplt, appver, ip});
			
			return pushServers;
		} catch (Exception e) {
			ExceptionHandler.handleExcepiton(model, response, e, appplt, appver, ip);
		} finally {
			monitor.stop();
		}
		
		return "";
	}
}
