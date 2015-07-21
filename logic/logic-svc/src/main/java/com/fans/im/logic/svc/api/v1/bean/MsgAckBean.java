package com.fans.im.logic.svc.api.v1.bean;

import java.io.IOException;
import java.util.Properties;

/**
 * @author tianhui
 *
 */
public class MsgAckBean {
	Long pos;

	public Long getPos() {
		return pos;
	}

	public void setPos(Long pos) {
		this.pos = pos;
	}
	
	public static void main(String[] argc) throws IOException{
		Properties properties = new Properties();
        properties.load(ClassLoader.getSystemResourceAsStream("msg.properties"));
        
        System.out.println(properties);
	}
	
}
