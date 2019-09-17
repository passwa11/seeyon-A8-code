/**
 * $Author xiongfeifei$
 * $Rev$
 * $Date:$:2015-9-9
 *
 * Copyright (C) 2015 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.apps.leaderwindow.dao;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.leaderwindow.po.LeaderWindowUser;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;

/**
 * <p>
 * Title: 领导之窗业务dao接口
 * </p>
 * <p>
 * Description: 代码描述
 * </p>
 * <p>
 * Copyright: Copyright (c) 2015
 * </p>
 * <p>
 * Company: seeyon.com
 * </p>
 */
public interface LeaderWindowUserDao {

	void savePostUser(LeaderWindowUser postUser) throws BusinessException;

	void updatePostUser(LeaderWindowUser postUser) throws BusinessException;

	List<LeaderWindowUser> findPostUserByPage(FlipInfo fi,Map<String,Object> params) throws BusinessException;
	
	void deleteUser(List<LeaderWindowUser> postList) throws BusinessException;
	
	void deleteUserById(List<Long> userIds) throws BusinessException;
	
	LeaderWindowUser findUserById(Long userId) throws BusinessException;
	
	int getMaxSortId(Long postId);
	
	void batchUpdateUser(List<LeaderWindowUser> userList) throws BusinessException;
	
	boolean isSortIdDuplicated(Integer sortId,Long postId,Long userId) throws BusinessException;
	
	void insertRepeatSortNum(Integer sortId,Long postId) throws BusinessException;
}
