package com.seeyon.v3x.edoc.util;

import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public class EdocInfo {
	  private EdocSummary summary;
	    /**
	     * 当前Comment
	     */
	    private Comment comment;
	   
	    private User currentUser ;
	    private String  processXml;
	    
	    private int trackType;
	    //指定跟踪人员的ID连接字符串
	    private String trackMemberId; 
	    //是否是删除个人事项操作标记
	    private boolean isDelAffair;
	    
	    public boolean getIsDelAffair() {
			return isDelAffair;
		}
		public void setIsDelAffair(Boolean isDelAffair) {
			this.isDelAffair = isDelAffair;
		}
		public String getTrackMemberId() {
			return trackMemberId;
		}
		public void setTrackMemberId(String trackMemberId) {
			this.trackMemberId = trackMemberId;
		}
		public int getTrackType() {
			return trackType;
		}
		public void setTrackType(int trackType) {
			this.trackType = trackType;
		}
		/**
	     * @return the processXml
	     */
	    public String getProcessXml() {
	        return processXml;
	    }
	    /**
	     * @param processXml the processXml to set
	     */
	    public void setProcessXml(String processXml) {
	        this.processXml = processXml;
	    }
	    /**
	     * @return the cuurentUser
	     */
	    public User getCurrentUser() {
	        return currentUser;
	    }
	    /**
	     * @param cuurentUser the cuurentUser to set
	     */
	    public void setCurrentUser(User cuurentUser) {
	        this.currentUser = cuurentUser;
	    }
	  
	    public EdocSummary getSummary() {
			return summary;
		}
		public void setSummary(EdocSummary summary) {
			this.summary = summary;
		}
		public void setDelAffair(boolean isDelAffair) {
			this.isDelAffair = isDelAffair;
		}
		/**
	     * @return the comment
	     */
	    public Comment getComment() {
	        return comment;
	    }
	    /**
	     * @param comment the comment to set
	     */
	    public void setComment(Comment comment) {
	        this.comment = comment;
	    }
}
