package com.fans.im.logic.svc.api.v1.bean;

/**
 * @author tianhui
 *
 */
public class UserConfigBean {
	private Integer allow_stranger_im;//0 容许 默认  1 容许关注人  2 所有人都不容许

	public Integer getAllow_stranger_im() {
		return allow_stranger_im;
	}

	public void setAllow_stranger_im(Integer allow_stranger_im) {
		this.allow_stranger_im = allow_stranger_im;
	}


}
