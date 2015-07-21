package com.fans.im.logic.common.domain;

import java.io.Serializable;
import java.util.Date;

import com.fans.data.common.dao.annotation.Column;
import com.fans.data.common.dao.annotation.Id;
import com.fans.data.common.dao.annotation.Table;
import com.fans.data.common.rao.annotation.RedisField;
import com.fans.data.common.rao.annotation.RedisId;

/**
 * 用户在IM中的config
 * @author tianhui
 *
 */
@Table(name="user_im_config")
public class UserImConfig implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Id
	@RedisId
	private String userId;
	
	@Column
	@RedisField
	private Integer allowStrangerIm;// 0  可以发  1 不容许陌生人发  2 都不容许发
	
	@Column
	@RedisField
	private Date createTime;
	
	@Column
	@RedisField
	private Date updateTime;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Integer getAllowStrangerIm() {
		return allowStrangerIm;
	}

	public void setAllowStrangerIm(Integer allowStrangerIm) {
		this.allowStrangerIm = allowStrangerIm;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	

	
}
