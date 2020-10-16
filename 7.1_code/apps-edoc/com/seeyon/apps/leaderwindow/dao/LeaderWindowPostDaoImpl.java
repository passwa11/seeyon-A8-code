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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.seeyon.apps.leaderwindow.po.LeaderWindowPost;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
/**
 * <p>
 * Title: 领导之窗业务dao实现类
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
public class LeaderWindowPostDaoImpl extends BaseHibernateDao<LeaderWindowPost> implements LeaderWindowPostDao {
	
	@Override
    public void savePost(LeaderWindowPost leaderWindowPost) throws BusinessException {
        DBAgent.saveOrUpdate(leaderWindowPost);

    }

    @Override
    public void updatePost(LeaderWindowPost leaderWindowPost) throws BusinessException {
        DBAgent.update(leaderWindowPost);

    }

	public List<LeaderWindowPost> findAllPostByPage(FlipInfo fi) throws BusinessException{
		List<LeaderWindowPost> postList=DBAgent.find("from LeaderWindowPost", null, fi);
		return postList;
	}
	
	public List<LeaderWindowPost> findAll() throws BusinessException{
		StringBuilder sql = new StringBuilder("from ");
		sql.append(LeaderWindowPost.class.getName());
		sql.append(" order by createTime ");
		List<LeaderWindowPost> postList=DBAgent.find(sql.toString());
		return postList;
	}
	
	public LeaderWindowPost findPostById(Long postId) throws BusinessException{
		return super.get(postId);
	}

	@Override
	public void deletePost(List<LeaderWindowPost> postList) throws BusinessException {
		DBAgent.deleteAll(postList);
	}
	
	public LeaderWindowPost findPostByName(String postName,String postId) throws BusinessException{
		Map<String, Object> params=new HashMap<String, Object>();
		params.put("name", postName);
		StringBuilder sql=new StringBuilder();
		sql.append("from LeaderWindowPost where name=:name and accountId=:accountId");
		if(StringUtils.isNotBlank(postId)){
			sql.append(" and id <> :postId");
			params.put("postId", Long.valueOf(postId));
		}
		params.put("accountId", AppContext.currentAccountId());
		List<LeaderWindowPost> postList=DBAgent.find(sql.toString(),params);
		if(postList.size()>0){
			return postList.get(0);
		}
		return null;
	}
	
	public List<LeaderWindowPost> findPostByAccount(Long accountId) throws BusinessException{
		StringBuilder sql = new StringBuilder("from ");
		sql.append(LeaderWindowPost.class.getName());
		sql.append(" where accountId=:accountId order by createTime ");
		Map<String, Object> params=new HashMap<String, Object>();
		if(accountId!=null){
			params.put("accountId", accountId);
		}
		List<LeaderWindowPost> postList=DBAgent.find(sql.toString(),params);
		return postList;
	}
	
}
