package com.fans.im.logic.common.domain;

import java.util.Date;

import com.fans.data.common.domain.GenericIndex;

/**
 * @author tianhui
 *
 */
public class UserDialogIndex implements GenericIndex{
	private static final long serialVersionUID = 1L;
	private static final String[] CONDITIONSOFQUERYALL = new String[]{"userId"};
	
	private String userId;
	
	private String dialogId;
	
	private Date time;
	
	private Long score;//最新一个消息ID
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getDialogId() {
		return dialogId;
	}

	public void setDialogId(String dialogId) {
		this.dialogId = dialogId;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public boolean equalIndex(GenericIndex index) {
		UserDialogIndex dIndex = (UserDialogIndex)index;
		if(dIndex.getUserId().equalsIgnoreCase(userId) && dIndex.getDialogId().equalsIgnoreCase(dialogId)){
			return true;
		} else {
			return false;
		}
	}
		

	public String[] getConditionsOfQueryAll() {
		return CONDITIONSOFQUERYALL;
	}

	public Date getCreateTime() {
		return time;
	}

	public Object getHashKey() {
		return userId;
	}

	public Object getKeyType() {
		return null;
	}

	public Long getScore() {
		return score;
	}

	public void setCreateTime(Date time) {
		this.time = time;
	}

	public void setScore(Long score) {
		this.score = score;
	}

	public String[] getConditionsOfQueryUniqueRecord() {
		return null;
	}
}
