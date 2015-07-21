package com.fans.im.msg.processor.impl;

import com.fans.data.common.msg.kafka.bean.Message;
import com.fans.data.common.msg.kafka.service.MessageProcessor;
import com.fans.data.common.util.JsonUtil;
import com.fans.im.logic.common.domain.ChatMessage;
import com.fans.im.logic.svc.manager.MessageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by TangTao on 15/6/17.
 */
@Component("msgDispatchProcessor")
public class MsgDispatchProcessor implements MessageProcessor {
	private static final Logger logger = LoggerFactory.getLogger(MsgDispatchProcessor.class);
			
    @Resource(name="messageManager")
    MessageManager messageManager;

    @Override
    public void process(Message message) {
    	logger.debug("receive msg, msg={}", JsonUtil.getJsonFromObject(message));
        if (message.getCname().equals(ChatMessage.class.getName())){
            ChatMessage chatMessage = (ChatMessage)message.getData();
        	messageManager.asyncAddMsg(chatMessage);
        }

    }
}
