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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.seeyon.apps.leaderwindow.dao.LeaderWindowPostDao;
import com.seeyon.apps.leaderwindow.dao.LeaderWindowUserDao;
import com.seeyon.apps.leaderwindow.po.LeaderWindowPost;
import com.seeyon.apps.leaderwindow.po.LeaderWindowUser;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.annotation.AjaxAccess;

/**
 * <p>
 * Title: 领导之窗业务Manager实现类
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
public class LeaderWindowManagerImpl implements LeaderWindowManager{
	
	private LeaderWindowPostDao leaderWindowPostDao;   //领导之窗dao接口
	
	private LeaderWindowUserDao leaderWindowUserDao;
	
	public void setLeaderWindowPostDao(LeaderWindowPostDao leaderWindowPostDao) {
		this.leaderWindowPostDao = leaderWindowPostDao;
	}

	public void setLeaderWindowUserDao(LeaderWindowUserDao leaderWindowUserDao) {
		this.leaderWindowUserDao = leaderWindowUserDao;
	}

	@AjaxAccess
	public FlipInfo findAllPostByPage(FlipInfo fi,Map<String,Object> params) throws BusinessException{
		if(fi==null){
			fi=new FlipInfo();
		}
		fi.setSortField("createTime");
		List<LeaderWindowPost> postList=leaderWindowPostDao.findAllPostByPage(fi);
		for(LeaderWindowPost post:postList){
			post.changePostUserNames();
		}
		fi.setData(postList);
		return fi;
	}
	
	@AjaxAccess
	public FlipInfo findPostUsersByPostId(FlipInfo fi,String postIds) throws BusinessException{
		Map<String,Object> params=new HashMap<String, Object>();
		if(fi==null){
			fi=new FlipInfo();
		}
		LeaderWindowPost post=null;
		if(StringUtils.isBlank(postIds)){
			params.put("postId", -1L);
		}else{
			params.put("postId", Long.valueOf(postIds));
			post=leaderWindowPostDao.findPostById(Long.valueOf(postIds));
		}
		List<LeaderWindowUser> postUserList=leaderWindowUserDao.findPostUserByPage(null, params);
		String postName="";
		if(post!=null){
			postName=post.getName();
		}
		for(LeaderWindowUser user:postUserList){
			user.setUserPostName(postName);
		}
		fi.setData(postUserList);
		return fi;
	}

	@AjaxAccess
	public Long createPostUser(String postIds, Map<String,Object> postUserMap) throws BusinessException {
        Long postId = Long.valueOf(postIds);
        LeaderWindowUser postUser=new LeaderWindowUser();
        ParamUtil.mapToBean(postUserMap, postUser, false);
        postUser.setPostId(postId);
        
        //排序号的重复处理 1.判断序号是否存在 2.存在则比此大的排序号统一+1
        boolean isDuplicated=leaderWindowUserDao.isSortIdDuplicated(postUser.getSortId(), postId,postUser.getId());
        if(isDuplicated){
        	leaderWindowUserDao.insertRepeatSortNum(postUser.getSortId(), postId);
        }
        if (postUser.getId() == null) {
           
        	postUser.setIdIfNew();
        	leaderWindowUserDao.savePostUser(postUser);
            //记录日志
            //appLogManager.insertLog4Account(user, accId, AppLogAction.Organization_NewPost, user.getName(), newpost.getName());
        } else {
        	leaderWindowUserDao.updatePostUser(postUser);
            //记录日志
            //appLogManager.insertLog4Account(user, accId, AppLogAction.Organization_UpdatePost, user.getName(), newpost.getName());
        }
        return postUser.getId();
    }
	
	@AjaxAccess
	public Long createPost(Map<String,Object> postMap) throws BusinessException{
		LeaderWindowPost post=new LeaderWindowPost();
		ParamUtil.mapToBean(postMap, post, false);
		String operType=String.valueOf(postMap.get("operType"));
		if(StringUtils.isNotBlank(operType) && "add".equals(operType)){
			post.setCreateTime(DateUtil.currentDate());
			leaderWindowPostDao.savePost(post);
		}else{
			leaderWindowPostDao.updatePost(post);
		}
		return post.getId();
	}
	
	@Override
	@AjaxAccess
    public HashMap viewPostUser(Long userId) throws BusinessException {
        HashMap<String,Object> map = new HashMap<String,Object>();
        LeaderWindowUser postUser=leaderWindowUserDao.findUserById(userId);
        if(postUser!=null){
        	Long postId=postUser.getPostId();
        	LeaderWindowPost post=this.findPostById(postId);
        	if(post!=null){
        		postUser.setUserPostName(post.getName());
        	}
        }
        ParamUtil.beanToMap(postUser, map, false);
        return map;
    }
	
	public void savePost(LeaderWindowPost post) throws BusinessException{
		leaderWindowPostDao.savePost(post);
	}
	
	public void updatePost(LeaderWindowPost post) throws BusinessException{
		leaderWindowPostDao.updatePost(post);
	}
	
	@AjaxAccess
	public Long createNewId(){
		return UUIDLong.longUUID();
	}
	
	@AjaxAccess
	public List<LeaderWindowPost> findAll(boolean isCascade) throws BusinessException{
		List<LeaderWindowPost> postList=leaderWindowPostDao.findAll();
		if(isCascade){
			for(LeaderWindowPost post:postList){
				post.changePostUserNames();
			}
		}
		return postList;
	}
	
	public LeaderWindowPost findPostById(Long postId) throws BusinessException{
		return leaderWindowPostDao.findPostById(postId);
	}
	
	public void deletePost(List<Long> postIds) throws BusinessException{
		List<LeaderWindowUser> deleteUserList=new ArrayList<LeaderWindowUser>();
		List<LeaderWindowPost> deletePostList=new ArrayList<LeaderWindowPost>();
		for(Long postId:postIds){
			Map<String, Object> params=new HashMap<String, Object>();
			params.put("postId", postId);
			List<LeaderWindowUser> userList=leaderWindowUserDao.findPostUserByPage(null, params);
			deleteUserList.addAll(userList);
			
			LeaderWindowPost post=leaderWindowPostDao.findPostById(postId);
			deletePostList.add(post);
		}
		
		leaderWindowPostDao.deletePost(deletePostList);
		leaderWindowUserDao.deleteUser(deleteUserList);
	}

	@Override
	public LeaderWindowUser findUserById(Long userId) throws BusinessException {
		return leaderWindowUserDao.findUserById(userId);
	}

	@Override
	public void deleteUser(List<Long> userIds) throws BusinessException {
		leaderWindowUserDao.deleteUserById(userIds);
	}

	@Override
	public void savePostUser(LeaderWindowUser postUser) throws BusinessException {
		leaderWindowUserDao.savePostUser(postUser);
	}

	@Override
	public void updatePostUser(LeaderWindowUser postUser) throws BusinessException {
		leaderWindowUserDao.updatePostUser(postUser);
	}
	
	public LeaderWindowPost findPostByName(String postName,String postId) throws BusinessException{
		return leaderWindowPostDao.findPostByName(postName,postId);
	}

	@AjaxAccess
	public Integer getMaxSortId(String postIds) throws BusinessException {
		if(StringUtils.isNotBlank(postIds)){
			Long postId=Long.valueOf(postIds);
			int maxSortId=leaderWindowUserDao.getMaxSortId(postId);
			return maxSortId+1;
		}
		return 1;
	}
	
	@AjaxAccess
	public void deleteUserByEntity(List<Map<String,Object>> postUser) throws BusinessException {
		List<LeaderWindowUser> postUserlist = new ArrayList<LeaderWindowUser>();
		postUserlist = ParamUtil.mapsToBeans(postUser, LeaderWindowUser.class, false);
		leaderWindowUserDao.deleteUser(postUserlist);
		
        //日志信息
//        User user = AppContext.getCurrentUser();
//        List<String[]> appLogs = new ArrayList<String[]>();
//        for (V3xOrgPost p : postlist) {
//            String[] appLog = new String[2];
//            appLog[0] = user.getName();
//            appLog[1] = p.getName();
//            appLogs.add(appLog);
//        }
//        //记录日志
//        appLogManager.insertLogs4Account(user, postlist.get(0).getOrgAccountId(), AppLogAction.Organization_DeletePost, appLogs);
	}
	
	@AjaxAccess
	public void updateUserOrder(String userOrderIds) throws NumberFormatException, BusinessException {
        if (userOrderIds == null) {
            return;
        }
        List<LeaderWindowUser> userList=new ArrayList<LeaderWindowUser>();
        int i = 0;
        String[] userIds=userOrderIds.split(",");
        for (String userId : userIds) {
            i++;
            LeaderWindowUser user = this.findUserById(Long.valueOf(userId));
            user.setSortId(i);
            userList.add(user);
        }
        leaderWindowUserDao.batchUpdateUser(userList);
    }
	
	public List<LeaderWindowPost> findPostByAccount(boolean isCascade,Long accountId) throws BusinessException{
		List<LeaderWindowPost> postList=leaderWindowPostDao.findPostByAccount(accountId);
		if(isCascade){
			for(LeaderWindowPost post:postList){
				post.changePostUserNames();
			}
		}
		return postList;
	}
	
}
