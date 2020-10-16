package com.seeyon.apps.leaderwindow.controller;

import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.leaderwindow.manager.LeaderWindowManager;
import com.seeyon.apps.leaderwindow.po.LeaderWindowPost;
import com.seeyon.apps.leaderwindow.po.LeaderWindowUser;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.portal.api.PortalApi;
import com.seeyon.ctp.util.CommonTools;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;

/**
 * <p>
 * Title: 领导之窗业务controller类
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
public class LeaderWindowController extends BaseController{
	
	private PortalApi portalApi;
	
	//领导之窗业务类接口
	private LeaderWindowManager leaderWindowManager;

	
	public void setPortalApi(PortalApi portalApi) {
		this.portalApi = portalApi;
	}

	public void setLeaderWindowManager(LeaderWindowManager leaderWindowManager) {
		this.leaderWindowManager = leaderWindowManager;
	}

	@CheckRoleAccess(roleTypes = {Role_NAME.GroupAdmin,Role_NAME.AccountAdministrator})
	public ModelAndView listPost(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        ModelAndView result = new ModelAndView("plugin/leaderWindow/listPost");
        //List<LeaderWindowPost> postList=leaderWindowManager.findAll(true);
        Long accountId=AppContext.currentAccountId();
        List<LeaderWindowPost> postList=leaderWindowManager.findPostByAccount(true, accountId);
        result.addObject("list", postList);
        return result;
    }
	
	@CheckRoleAccess(roleTypes = {Role_NAME.GroupAdmin,Role_NAME.AccountAdministrator})
	public ModelAndView listPostUser(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        ModelAndView result = new ModelAndView("plugin/leaderWindow/listPostUser");
        result.addObject("postId",request.getParameter("postId"));
        //List<LeaderWindowUser> list=leaderWindowManager.findPostUsersByPostId(request.getParameter("postId"));
        //result.addObject("list", list);
        return result;
    }

	@CheckRoleAccess(roleTypes = {Role_NAME.GroupAdmin,Role_NAME.AccountAdministrator})
	public ModelAndView listMain(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("plugin/leaderWindow/list_main");
    }
	
	@CheckRoleAccess(roleTypes = {Role_NAME.GroupAdmin,Role_NAME.AccountAdministrator})
	public ModelAndView listUserMain(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView result=new ModelAndView("plugin/leaderWindow/user_list_main");
		result.addObject("postId", request.getParameter("postId"));
		return result;
    }
	
	@CheckRoleAccess(roleTypes = {Role_NAME.GroupAdmin,Role_NAME.AccountAdministrator})
	public ModelAndView postEdit(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView result=new ModelAndView("plugin/leaderWindow/postform");
		String postIdStr=request.getParameter("postId");
		if(StringUtils.isBlank(postIdStr)){
			postIdStr="-1";
		}
		Long postId=Long.valueOf(postIdStr);
		LeaderWindowPost post=leaderWindowManager.findPostById(postId);
		if(post!=null){
			post.changePostUserNames();
		}
		result.addObject("post", post);
		result.addObject("operType", request.getParameter("operType"));
        return result;
    }
	
	@CheckRoleAccess(roleTypes = {Role_NAME.GroupAdmin,Role_NAME.AccountAdministrator})
	public ModelAndView savePost(HttpServletRequest request,HttpServletResponse response)throws Exception{
		String postIdStr=request.getParameter("id");
		LeaderWindowPost post=new LeaderWindowPost();
		if(StringUtils.isNotBlank(postIdStr)){
			post=leaderWindowManager.findPostById(Long.valueOf(postIdStr));
		}
		bind(request, post);
		String operType=String.valueOf(request.getParameter("operType"));
		if(StringUtils.isNotBlank(operType) && "add".equals(operType)){
			post.setCreateTime(DateUtil.currentDate());
			post.setIdIfNew();
			post.setAccountId(AppContext.currentAccountId());
			leaderWindowManager.savePost(post);
		}else{
			leaderWindowManager.updatePost(post);
		}
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");
		return this.redirectModelAndView("/portal/leaderWindowController.do?method=listMain");
	}
	
	@CheckRoleAccess(roleTypes = {Role_NAME.GroupAdmin,Role_NAME.AccountAdministrator})
	public ModelAndView delete(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<Long> postIds = CommonTools.parseStr2Ids(request.getParameter("id"));
        leaderWindowManager.deletePost(postIds);
        return this.redirectModelAndView("/portal/leaderWindowController.do?method=listMain");
    }
	
	public ModelAndView postUserEdit(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView result=new ModelAndView("plugin/leaderWindow/postUserForm");
		String userIds=request.getParameter("userId");
		if(StringUtils.isBlank(userIds)){
			userIds="-1";
		}
		Long userId=Long.valueOf(userIds);
		LeaderWindowUser postUser=leaderWindowManager.findUserById(userId);
		result.addObject("postUser", postUser);
		result.addObject("postId", request.getParameter("postId"));
		result.addObject("operType", request.getParameter("operType"));
        return result;
    }
	
	public ModelAndView isSameName(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	String postName = request.getParameter("postName");
    	String postId = request.getParameter("id");
    	if(Strings.isNotBlank(postName)){
    		postName = URLDecoder.decode(postName,"UTF-8");
    	}
    	LeaderWindowPost post=leaderWindowManager.findPostByName(postName,postId);
    	boolean isSame=false;
    	if(post!=null){
    		isSame=true;
    	}
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.print(isSame);
    	return null;
    }
	
	public ModelAndView orderPostUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String postId = request.getParameter("postId");
        FlipInfo fi=leaderWindowManager.findPostUsersByPostId(null,postId);
        List<LeaderWindowUser> userlist=null;
        if(fi!=null){
        	userlist =fi.getData();
        }
        return new ModelAndView("plugin/leaderWindow/orderPostUser", "userlist", userlist);
    }
	
    public ModelAndView getLeaderWindow(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
    	response.setContentType("text/html;charset=UTF-8");
    	ModelAndView mView = new ModelAndView("plugin/leaderWindow/view_leader_window");
    	Long accountId=AppContext.currentAccountId();
    	String accountIdStr=request.getParameter("accountId");
    	if(StringUtils.isNotBlank(accountIdStr)){
    		accountId=Long.valueOf(accountIdStr);
    	}
    	List<LeaderWindowPost> allPost=leaderWindowManager.findPostByAccount(false, accountId);
    	if(allPost.size()>0){
    		//1.获取创建时间最早的数据
    		LeaderWindowPost earlyPost=allPost.get(0);
    		mView.addObject("earlyPost", earlyPost);//最早岗位
    		earlyPost.changePostUserNames();
    		Set<LeaderWindowUser> userSet=earlyPost.getPostUsers();
    		List<LeaderWindowUser> userList=new ArrayList<LeaderWindowUser>(userSet);
    		Collections.sort(userList, new Comparator<LeaderWindowUser>() {
				@Override
				public int compare(LeaderWindowUser arg0, LeaderWindowUser arg1) {
					// TODO Auto-generated method stub
					if(arg0.getSortId()==null && arg1.getSortId()==null){
						return 1;
					}else if(arg0.getSortId()==null){
						return 1;
					}
					return arg0.getSortId()>arg1.getSortId()?1:-1;
				}
			});
    		if(userList.size()>0){
    			mView.addObject("viewPostUser", userList.get(0));//最早岗位的最小序号的岗位人员
    		}
    		//其他岗位
			for(LeaderWindowPost post:allPost){
				FlipInfo fi=leaderWindowManager.findPostUsersByPostId(null, post.getId().toString());
				List<LeaderWindowUser> otherPostUserList=fi.getData();
				post.setPostUserList(otherPostUserList);
			}
			if(allPost.size()>1){
				List<LeaderWindowPost> otherPost=allPost.subList(1, allPost.size());
				mView.addObject("otherPost", otherPost);
			}
    	}
    	mView.addObject("allPost", allPost);
    	mView.addObject("accountId", accountId);
        return mView;
    }
    
    
    public ModelAndView moreLeaderWindow(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
    	response.setContentType("text/html;charset=UTF-8");
    	ModelAndView mView = new ModelAndView("plugin/leaderWindow/more_view");
    	
    	Long accountId=AppContext.currentAccountId();
    	String accountIdStr=request.getParameter("accountId");
    	if(StringUtils.isNotBlank(accountIdStr)){
    		accountId=Long.valueOf(accountIdStr);
    	}
    	String portalIdStr=request.getParameter("portalId");
    	Long portalId = 0L;
    	if(StringUtils.isNotBlank(portalIdStr)){
    		portalId=Long.valueOf(portalIdStr);
    		accountId = portalApi.getPortalSetFromCache(portalId).getAccountId();
    	}
    	List<LeaderWindowPost> allPost=leaderWindowManager.findPostByAccount(false, accountId);
    	
    	String userId=request.getParameter("userId");
    	LeaderWindowUser viewPostUser=null;
    	if(allPost.size()>0){
    		//循环所有岗位，加载出所有人员
    		for(LeaderWindowPost post:allPost){
				FlipInfo fi=leaderWindowManager.findPostUsersByPostId(null, post.getId().toString());
				List<LeaderWindowUser> otherPostUserList=fi.getData();
				post.setPostUserList(otherPostUserList);
			}
    		//2.判断是否具体人员进入更多页面
    		if(StringUtils.isNotBlank(userId)){
        		//当是点击用户名进入更多页面时，则显示当前用户信息
        		viewPostUser=leaderWindowManager.findUserById(Long.valueOf(userId));
        		mView.addObject("viewPostUser", viewPostUser);
        		//3.查询当前人的所属岗位
        		LeaderWindowPost viewPost=leaderWindowManager.findPostById(viewPostUser.getPostId());
        		if(viewPost!=null){
        			mView.addObject("viewPostName",viewPost.getName());
        		}
        	}else{
        		if(allPost.get(0).getPostUserList()!=null && allPost.get(0).getPostUserList().size()>0){
        			viewPostUser=allPost.get(0).getPostUserList().get(0);
        			mView.addObject("viewPostUser", viewPostUser);//最早岗位的最小序号的岗位人员
        			mView.addObject("viewPostName", allPost.get(0).getName());
        		}
        	}
    	}
    	mView.addObject("allPost", allPost);
        return mView;
    }
    

//	private LeaderWindow initLeaderWindow(HttpServletRequest request)
//			throws BusinessException {
//		LeaderWindow leaderWindow = new LeaderWindow(); // 领导之窗对象
//    	String spaceType = request.getParameter("spaceType");
//    	leaderWindow.setSpaceType(spaceType);
//    	leaderWindow = this.leaderWindowManager.getLeaderWindow(leaderWindow);
//		return leaderWindow;
//	}
}
