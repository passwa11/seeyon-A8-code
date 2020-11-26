/**
 * Author : xuqw
 *   Date : 2015年12月12日 下午3:18:50
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.rest.resources;

import com.seeyon.apps.collaboration.util.CollaborationUtils;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Title       : 应用模块名称</p>
 * <p>Description : 代码描述</p>
 * <p>Copyright   : Copyright (c) 2012</p>
 * <p>Company     : seeyon.com</p>
 */
public class ColSummaryCommentVO {

    /* ID */
    private Long id = null;
    
    /* 回复人ID */
    private Long createId = null;
    
    /* 回复人名称 */
    private String createName = null;
    
    /* 评论内容 */
    private String content = null;
    
    /* 评论时间 */
    private String createDate = null;
    
    /* 附件列表 */
    private String attachments = null;

    /* 态度 */
    private String attitude = null;
    
    private String attitudeCode = null;
    
    /* 代理信息 */
    private String agent = null;
    
    /* 操作 : 暂存待办/回退... */
    private String action = null;
    
    /* 是否赞了协同 */
    private Boolean praiseToSummary = null;
    
    /* 是否赞了当前评论 */
    private Boolean praiseToComment = null;
    
    /* 评论点赞数量 */
    private Integer praiseNumber = null;
    
    /* 评论来自那里 */
    private String m1Info = null;
    
    private Integer                   forwardCount;

    private Integer                   ctype;

    private List<ColSummaryCommentVO> subReplys = null;
    private boolean canView;
    
    private static final String blank ="";

    private Integer subType;
    
    /**
     * 是否是只读的流程
     * 
     * @return
     *
     * @Since A8-V5 7.1SP1
     * @Author      : xuqw
     * @Date        : 2019年8月8日下午8:05:26
     *
     */
    public boolean isReadOnly() {
        
        if(this.subType != null && Comment.SubType.SUPERNODE.getKey() == this.getSubType().intValue()) {
            return true;
        }
        
        return false;
    }

    public Integer getSubType() {
        return subType;
    }

    public void setSubType(Integer subType) {
        this.subType = subType;
    }

    /**
     * 转化成H5 VO
     * @param comments
     * @return
     */
    public static ColSummaryCommentVO valueOf(Comment c){
        
        ColSummaryCommentVO vo = new ColSummaryCommentVO();
        
        vo.setId(c.getId());
        vo.setCreateId(c.getCreateId());
        vo.setCreateName(c.getCreateName());
        vo.setContent(c.getEscapedContent());
        vo.setCreateDate(CollaborationUtils.showDate(c.getCreateDate()));
        vo.setAttachments(c.getRelateInfo());
        //ResourceUtil.getString(c.getExtAtt3());
        vo.setAttitudeCode(c.getExtAtt4());
        vo.setAttitude(ResourceUtil.getString(c.getExtAtt1()));
        String agentInfo = null;
        if(Strings.isNotBlank(c.getExtAtt2())){
            agentInfo = ResourceUtil.getString("collaboration.agent.label", c.getExtAtt2());
        }
        vo.setAgent(agentInfo);
        if (!"collaboration.dealAttitude.repeat.auto.skip".equals(c.getExtAtt3())) {
        	vo.setAction(ResourceUtil.getString(c.getExtAtt3()));
        }
        
        if("collaboration.dealAttitude.termination".equals(c.getExtAtt3())){
            vo.setAttitudeCode(c.getExtAtt3());
        }
        
        vo.setPraiseToSummary(c.getPraiseToSummary());
        vo.setPraiseToComment(c.getPraiseToComment());
        vo.setPraiseNumber(c.getPraiseNumber());
        vo.setM1Info(c.getM1Info());
        vo.setForwardCount(c.getForwardCount());
        vo.setCtype(c.getCtype());
        vo.setCanView(c.isCanView());
        vo.setSubType(c.getSubType());
        return vo;
    }
    
    /**
     * 转化成H5 VO
     * @param comments
     * @return
     */
    public static List<ColSummaryCommentVO> valueOf(List<Comment> comments){
        
        List<ColSummaryCommentVO> vos = new ArrayList<ColSummaryCommentVO>();
        
        if(Strings.isNotEmpty(comments)){
            for(Comment comment : comments){
                vos.add(valueOf(comment));   
            }
        }
        
        return vos;
    }
    

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCreateId(Long createId) {
        this.createId = createId;
    }

    public void setCreateName(String createName) {
        this.createName = createName;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public void setAttitude(String attitude) {
        this.attitude = attitude;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setPraiseToSummary(Boolean praiseToSummary) {
        this.praiseToSummary = praiseToSummary;
    }

    public void setPraiseToComment(Boolean praiseToComment) {
        this.praiseToComment = praiseToComment;
    }

    public void setPraiseNumber(Integer praiseNumber) {
        this.praiseNumber = praiseNumber;
    }

    public void setM1Info(String m1Info) {
        this.m1Info = m1Info;
    }



    public Long getId() {
        return id;
    }



    public Long getCreateId() {
        return createId;
    }



    public String getCreateName() {
        return createName;
    }



    public String getContent() {
        return content;
    }



    public String getAttachments() {
        if (Strings.isNotBlank(attachments)) {
            return attachments;
        }
        return "[]";
    }

    public String getAttitude() {
        if (attitude == null) {
            return blank;
        }
        return attitude;
    }



    public String getAgent() {
        return agent;
    }



    public String getAction() {
        if(action==null){
            return blank;
        }
        return action;
    }



    public Boolean getPraiseToSummary() {
        return praiseToSummary;
    }



    public Boolean getPraiseToComment() {
        return praiseToComment;
    }



    public Integer getPraiseNumber() {
        if (praiseNumber == null) {
            return 0;
        }
        return praiseNumber;
    }

    public String getM1Info() {
        return m1Info;
    }

    public List<ColSummaryCommentVO> getSubReplys() {
        if (subReplys == null) {
            return Collections.emptyList();
        }
        return subReplys;
    }

    public void setSubReplys(List<ColSummaryCommentVO> subReplys) {
        this.subReplys = subReplys;
    }

    public Integer getForwardCount() {
        return forwardCount;
    }

    public void setForwardCount(Integer forwardCount) {
        this.forwardCount = forwardCount;
    }

    public Integer getCtype() {
        return ctype;
    }

    public void setCtype(Integer ctype) {
        this.ctype = ctype;
    }

    public boolean isCanView() {
        return canView;
    }

    public void setCanView(boolean canView) {
        this.canView = canView;
    }

    public String getAttitudeCode() {
        return attitudeCode;
    }

    public void setAttitudeCode(String attitudeCode) {
        this.attitudeCode = attitudeCode;
    }
}
