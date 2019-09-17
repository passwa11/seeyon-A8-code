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
package com.seeyon.apps.leaderwindow.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.leaderwindow.po.LeaderWindowPost;
import com.seeyon.apps.leaderwindow.po.LeaderWindowUser;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;

/**
 * <p>
 * Title: 领导之窗业务Manager接口
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
public interface LeaderWindowManager {

	
	FlipInfo findAllPostByPage(FlipInfo fi,Map<String,Object> params) throws BusinessException;
	
	FlipInfo findPostUsersByPostId(FlipInfo fi,String postIds) throws BusinessException;

	Long createPostUser(String postIds, Map<String,Object> postUserMap) throws BusinessException;
	
	Long createPost(Map<String,Object> postMap) throws BusinessException;
	
	Long createNewId();
	
	List<LeaderWindowPost> findAll(boolean isCascade) throws BusinessException;
	
	LeaderWindowPost findPostById(Long postId) throws BusinessException;
	
	LeaderWindowUser findUserById(Long userId) throws BusinessException;
	
	void savePost(LeaderWindowPost post) throws BusinessException;
	
	void updatePost(LeaderWindowPost post) throws BusinessException;
	
	void deletePost(List<Long> postIds) throws BusinessException;
	
	void deleteUser(List<Long> userIds) throws BusinessException;
	
	void savePostUser(LeaderWindowUser postUser) throws BusinessException;
	
	void updatePostUser(LeaderWindowUser postUser) throws BusinessException;
	
	LeaderWindowPost findPostByName(String postName,String postId) throws BusinessException;
	
	HashMap viewPostUser(Long userId) throws BusinessException;
	
	Integer getMaxSortId(String postId) throws BusinessException;
	
	void deleteUserByEntity(List<Map<String,Object>> postUser) throws BusinessException;
	
	void updateUserOrder(String userIds) throws NumberFormatException, BusinessException;
	
	List<LeaderWindowPost> findPostByAccount(boolean isCascade,Long accountId) throws BusinessException;
}
