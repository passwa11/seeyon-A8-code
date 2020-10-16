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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.leaderwindow.po.LeaderWindowUser;
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
public class LeaderWindowUserDaoImpl extends BaseHibernateDao<LeaderWindowUser> implements LeaderWindowUserDao {
	
	@Override
    public void savePostUser(LeaderWindowUser postUser) throws BusinessException {
        DBAgent.saveOrUpdate(postUser);

    }

    @Override
    public void updatePostUser(LeaderWindowUser postUser) throws BusinessException {
        DBAgent.update(postUser);

    }
	
	public List<LeaderWindowUser> findPostUserByPage(FlipInfo fi,Map<String,Object> params) throws BusinessException{
		List<LeaderWindowUser> postUserList=DBAgent.find("from LeaderWindowUser where postId=:postId order by sortId", params);
		return postUserList;
	}

	@Override
	public void deleteUser(List<LeaderWindowUser> userList)	throws BusinessException {
		DBAgent.deleteAll(userList);
	}

	@Override
	public void deleteUserById(List<Long> userIds) throws BusinessException {
		List<LeaderWindowUser> userList=new ArrayList<LeaderWindowUser>();
		for(Long userId:userIds){
			userList.add(this.findUserById(userId));
		}
		this.deleteUser(userList);
	}
	
	public LeaderWindowUser findUserById(Long userId) throws BusinessException{
		LeaderWindowUser user=super.get(userId);
		return user;
	}
	
	public int getMaxSortId(Long postId) {
		Map<String, Object> param = new HashMap<String, Object>();
		String sql = "SELECT max(sortId) from LeaderWindowUser where postId=:postId";
		param.put("postId", postId);
		List lst = DBAgent.find(sql, param);
		if (!lst.isEmpty() && lst.get(0) != null) {
			return Integer.valueOf(lst.get(0).toString());
		} else {
			return 0;
		}
	}
	
	public void batchUpdateUser(List<LeaderWindowUser> userList) throws BusinessException{
		DBAgent.updateAll(userList);
	}
	
	public boolean isSortIdDuplicated(Integer sortId,Long postId,Long userId) throws BusinessException{
		if(sortId==null || postId==null){
			return false;
		}
		//检查是否存在相同的排序号
		String sql="SELECT COUNT(*) FROM LeaderWindowUser WHERE postId=:postId and sortId=:sortId";
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("postId", postId);
		param.put("sortId", sortId);
		if(userId!=null){
			sql+=" and id <> :id";
			param.put("id", userId);
		}
		List lst = DBAgent.find(sql, param);
		if (!lst.isEmpty() && lst.get(0) != null) {
			return Integer.valueOf(String.valueOf(lst.get(0))) > 0;
		} else {
			return false;
		}
	}
	
	public void insertRepeatSortNum(Integer sortId,Long postId) throws BusinessException{
		if(sortId==null || postId==null){
			return;
		}
		String sql="UPDATE LeaderWindowUser SET sortId=sortId + 1 WHERE postId=? AND sortId>=?";
		DBAgent.bulkUpdate(sql,postId,sortId);
	}
}
