package com.fans.im.logic.common.domain;

import java.util.Date;

import com.fans.data.common.domain.GenericIndex;

/**
 * @author tianhui
 *
 */
public class DialogMessageIndex implements GenericIndex {

	private static final long serialVersionUID = 1L;
	
	private static final String[] CONDITIONSOFQUERYALL = new String[]{"dialogId"};
	
	private String dialogId;
	
	private String msg;
	
	private Date time;
	
	private Long score;

	public String getDialogId() {
		return dialogId;
	}

	public void setDialogId(String dialogId) {
		this.dialogId = dialogId;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Long getScore() {
		return score;
	}

	public void setScore(Long score) {
		this.score = score;
	}

	public Date getCreateTime() {
		return time;
	}

	public void setCreateTime(Date createTime) {
		this.time = createTime;
	}

	public Object getHashKey() {
		return dialogId;
	}

	public Object getKeyType() {
		return null;
	}

	public boolean equalIndex(GenericIndex index) {
		DialogMessageIndex dIndex = (DialogMessageIndex)index;
		if(dIndex.getMsg().equalsIgnoreCase(msg) && dIndex.getDialogId().equalsIgnoreCase(dialogId)){
			return true;
		} else {
			return false;
		}
	}

	public String[] getConditionsOfQueryAll() {
		return CONDITIONSOFQUERYALL;
	}

	public String[] getConditionsOfQueryUniqueRecord() {
		return null;
	}

}
