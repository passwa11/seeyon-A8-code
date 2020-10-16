package com.seeyon.apps.leaderwindow.po;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.seeyon.ctp.common.po.BasePO;

/**
 * 
* @ClassName: LeaderWindowPost
* @Description: 领导之窗-岗位po类
* @Company seeyon
* @author gsl
* @date 2018年3月7日 上午10:18:01
 */
public class LeaderWindowPost extends BasePO{

	private static final long serialVersionUID = 1L;
	
	 /**
     * 主键ID.
     */
    private Long   id;
    
    private String name;
    
    private java.util.Date createTime;
    
    private Set<LeaderWindowUser> postUsers=new HashSet<LeaderWindowUser>();
    
    private String postUserNames;
    
    private Long accountId;
    
    //用于存放有序的岗位人员
    private List<LeaderWindowUser> postUserList=new ArrayList<LeaderWindowUser>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public java.util.Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(java.util.Date createTime) {
		this.createTime = createTime;
	}

	public Set<LeaderWindowUser> getPostUsers() {
		return postUsers;
	}

	public void setPostUsers(Set<LeaderWindowUser> postUsers) {
		this.postUsers = postUsers;
	}

	public String getPostUserNames() {
        return postUserNames;
	}

	public void setPostUserNames(String postUserNames) {
		this.postUserNames = postUserNames;
	}
	
	public void changePostUserNames(){
		StringBuilder _selObjStr = new StringBuilder();
		getPostUsers();//lazy
        for(LeaderWindowUser etm:postUsers){
            if(_selObjStr.length() > 0){
                _selObjStr.append("、");
             }
            _selObjStr.append(etm.getUserName());
        }
        postUserNames=_selObjStr.toString();
	}

	public List<LeaderWindowUser> getPostUserList() {
		return postUserList;
	}

	public void setPostUserList(List<LeaderWindowUser> postUserList) {
		this.postUserList = postUserList;
	}

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}
	
}
