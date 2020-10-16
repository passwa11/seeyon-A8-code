package com.seeyon.apps.leaderwindow.po;

import com.seeyon.ctp.common.po.BasePO;

/**
 * 
* @ClassName: LeaderWindowPost
* @Description: 领导之窗-人员po类
* @Company seeyon
* @author gsl
* @date 2018年3月7日 上午10:18:01
 */
public class LeaderWindowUser extends BasePO{

	private static final long serialVersionUID = 1L;
	
	 /**
     * 主键ID.
     */
    private Long   id;
    
    private String userName;
    
    private Long postId;
    
    private String content;
    
    private Boolean attachmentsFlag  = Boolean.FALSE;
    
    private Long attachmentId;
    
    private String postWork;
    
    private Integer sortId;
    
    private String userPostName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Long getPostId() {
		return postId;
	}

	public void setPostId(Long postId) {
		this.postId = postId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Boolean getAttachmentsFlag() {
		return attachmentsFlag;
	}

	public void setAttachmentsFlag(Boolean attachmentsFlag) {
		this.attachmentsFlag = attachmentsFlag;
	}

	public Long getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(Long attachmentId) {
		this.attachmentId = attachmentId;
	}

	public String getPostWork() {
		return postWork;
	}

	public void setPostWork(String postWork) {
		this.postWork = postWork;
	}

	public Integer getSortId() {
		return sortId;
	}

	public void setSortId(Integer sortId) {
		this.sortId = sortId;
	}

	public String getUserPostName() {
		return userPostName;
	}

	public void setUserPostName(String userPostName) {
		this.userPostName = userPostName;
	}

}
