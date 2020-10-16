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

import com.seeyon.apps.leaderwindow.po.LeaderWindowPost;
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
public interface LeaderWindowPostDao {

	void savePost(LeaderWindowPost leaderWindowPost) throws BusinessException;

	void updatePost(LeaderWindowPost leaderWindowPost) throws BusinessException;

	List<LeaderWindowPost> findAllPostByPage(FlipInfo fi) throws BusinessException;
	
	List<LeaderWindowPost> findAll() throws BusinessException;
	
	LeaderWindowPost findPostById(Long postId) throws BusinessException;
	
	void deletePost(List<LeaderWindowPost> postList) throws BusinessException;
	
	LeaderWindowPost findPostByName(String postName,String postId) throws BusinessException;
	
	List<LeaderWindowPost> findPostByAccount(Long accountId) throws BusinessException;
}
