package com.fans.im.rpc.service.impl;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.data.common.auth.util.TokenUtil_RE;
import com.fans.data.common.util.StringUtil;
import com.fans.data.common.xinterface.ParameterTool;
import com.fans.im.logic.common.domain.UserOnline;
import com.fans.im.logic.common.service.UserOnlineService;
import com.fans.im.rpc.service.ControllerRpcService;

/**
 * Created by patronfeng on 15/6/29.
 */
@Service
public class ControllerPrcServiceImpl implements ControllerRpcService.Iface {
	private static final Logger logger = LoggerFactory.getLogger(ControllerPrcServiceImpl.class);
	@Resource
	private UserOnlineService userOnlineService;
	
	private ExecutorService excutor = null;
	
    @Override
    public void notifyConnectionStatus(String nodeName, String userId, int status) throws TException {
    	logger.debug("receive a connect status, userid={}, status={}", userId, status);
    	if(StringUtil.isEmpty(userId)){
    		return;
    	}
    	UserOnline userOnline = new UserOnline();
    	userOnline.setAppplt("unknown");
    	userOnline.setLastOnlineTime(System.currentTimeMillis());
    	userOnline.setStatus(status);
    	userOnlineService.set(userId, userOnline);
    }
    
    private static final int USER_NAME_INDX = 0;
	private static final int EXPIRE_TIME_INDX = 1;
	@Override
	public int auth(String userId, String userToken) throws TException {
		try {
			logger.debug("auth, userid={}, tk={}", userId, userToken);
			String origData = TokenUtil_RE.decryptToken(userToken);
			 String[] fields = origData.split("&");
		     long time = Long.parseLong(fields[EXPIRE_TIME_INDX]);
		     if (time < System.currentTimeMillis()) {
					logger.warn("expired token, userid={}, tk={}", 
							new Object[]{fields[USER_NAME_INDX], userToken});
					return 4013;
			 }
		     
		     if(!userId.equalsIgnoreCase(fields[USER_NAME_INDX])){
		    	 logger.warn("user not consistent, userid={}, tk={}", 
							new Object[]{fields[USER_NAME_INDX], userToken});
					return 4014;
		     }
		} catch (Exception e) {
			logger.warn("token error, userid={}, tk={}", new Object[]{userId, userToken});
			
			return 4012;
		}
		
		return 0;
	}
	
    @PostConstruct
    public void init(){
    	excutor = Executors.newSingleThreadExecutor();
    	excutor.execute(new ThriftRunnable(this));
    }
    
    public class ThriftRunnable implements Runnable{
    	ControllerPrcServiceImpl controllerPrcService;
    	
    	
		public ThriftRunnable(ControllerPrcServiceImpl controllerPrcService) {
			super();
			this.controllerPrcService = controllerPrcService;
		}


		@Override
		public void run() {
			TNonblockingServerTransport serverTransport = null;
	        try {
	            serverTransport = new TNonblockingServerSocket(19090);
	        } catch (TTransportException e) {
	            e.printStackTrace();
	        }

	        ControllerRpcService.Processor<ControllerRpcService.Iface> processor = new ControllerRpcService.Processor<ControllerRpcService.Iface>(controllerPrcService);

	        Factory protFactory = new TBinaryProtocol.Factory(true, true);
	        //TCompactProtocol.Factory protFactory = new TCompactProtocol.Factory();

	        TNonblockingServer.Args args = new TNonblockingServer.Args(
	                serverTransport);
	        args.processor(processor);
	        args.protocolFactory(protFactory);
	        
	        args.transportFactory(new TFramedTransport.Factory());
	      	
	        TServer server = new TNonblockingServer(args);
	        server.serve();
		}
    	
    }
    
    @PreDestroy
    public void detory(){
    	logger.info("shutdown thrift thread");
    	if(excutor != null){
    		excutor.shutdownNow();
    	}
    }
}
