package com.seeyon.v3x.edoc.webmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.util.EdocUtil;

/**
 * H5 公文详细页面数据对象
 */
public class EdocSummaryBO {

    private String opins;
    private Map<String, Object> opinions;
    private String edocFormContent;
    private String memberPictureSrc;
    private String memberdName;
    private EdocSummary edocSummary;
    private List<Attachment> attachmentList;
    private int attachmentSize;
    private int relatedDocSize;
    private String opinionPolicy;//意见必填
    private String cancelOpinionPolicy;//撤销/回退/终止时必须填写意见
    private String disAgreeOpinionPolicy;//不同意时必须填写意见
    private CtpAffair ctpAffair;
    private Integer attribute;
    private String secretLevel;
    private String createTime;
    private String actions;
    private EdocOpinion nowNodeOpinion;
    private Long templateProcessId;
    private String disPosition;
    private String filedValue;
    private Boolean specialStepBack = true;
    private String optionType = "";
    private Long flowPermAccout;
    private boolean isProcessTemplate;

    private Integer submitStyleCfg;

    private String nodePolicy;

    /**
     * 项目：徐矿集团 【在移动端返回公文详情时增加是否屏蔽按钮】 作者：jiangchenxi 时间：2019年3月13日  start
     */
    private boolean shield;

    public boolean isShield() {
        return shield;
    }

    public void setShield(boolean shield) {
        this.shield = shield;
    }

    /**
     * 项目：徐矿集团 【在移动端返回公文详情时增加是否屏蔽按钮】 作者：jiangchenxi 时间：2019年3月13日  end
     */

    public Long getFlowPermAccout() {
        return flowPermAccout;
    }

    public void setFlowPermAccout(Long flowPermAccout) {
        this.flowPermAccout = flowPermAccout;
    }

    //处理流程需要的参数
    private Map<String, String> defaultNode = null;
    private Map<String, String> errorRet = new HashMap<String, String>();

    private User currentUser = null;

    private String listType;
    private Boolean isCanComment = Boolean.FALSE;
    private String bodyLastModify = null;//正文最后更新时间，用Office正文M3端缓存

    public String getOpins() {
        return opins;
    }

    public void setOpins(String opins) {
        this.opins = opins;
    }

    public Map<String, Object> getOpinions() {
        return opinions;
    }

    public void setOpinions(Map<String, Object> opinions) {
        this.opinions = opinions;
    }

    public String getEdocFormContent() {
        return edocFormContent;
    }

    public void setEdocFormContent(String edocFormContent) {
        this.edocFormContent = edocFormContent;
    }

    public String getMemberPictureSrc() {
        return memberPictureSrc;
    }

    public void setMemberPictureSrc(String memberPictureSrc) {
        this.memberPictureSrc = memberPictureSrc;
    }

    public String getMemberdName() {
        return memberdName;
    }

    public void setMemberdName(String memberdName) {
        this.memberdName = memberdName;
    }

    public Map<String, Object> getEdocSummary() {

        Map<String, Object> summary = new HashMap<String, Object>();

        if (edocSummary != null) {
            summary.put("id", edocSummary.getId());
            summary.put("processId", edocSummary.getProcessId());
            summary.put("caseId", edocSummary.getCaseId());
            summary.put("isQuickSend", edocSummary.getIsQuickSend());
            summary.put("urgentLevel", edocSummary.getUrgentLevel());
            summary.put("subject", edocSummary.getSubject());
            summary.put("orgAccountId", edocSummary.getOrgAccountId());
            summary.put("docMark", edocSummary.getDocMark());
            summary.put("docMark2", edocSummary.getDocMark2());
            summary.put("serialNo", edocSummary.getSerialNo());
            summary.put("finished", edocSummary.getFinished());
            summary.put("state", edocSummary.getState());

            //发起人
            Map<String, Object> startMenber = new HashMap<String, Object>();
            startMenber.put("id", edocSummary.getStartMember().getId());
            startMenber.put("name", edocSummary.getStartMember().getName());
            summary.put("startMember", startMenber);


            //正文
            List<Map<String, Object>> edocBodys = new ArrayList<Map<String, Object>>();
            int bodyType = 10;
            for (EdocBody body : edocSummary.getEdocBodies()) {
                Map<String, Object> bMap = new HashMap<String, Object>();
                bMap.put("contentType", body.getContentType());
                bMap.put("content", body.getContent());
                bMap.put("lastModified", bodyLastModify);
                bodyType = EdocHelper.getBodyTypeKey(body.getContentType());
                edocBodys.add(bMap);
            }
            summary.put("edocBodies", edocBodys);
            summary.put("createDate", Datetimes.format(edocSummary.getCreateTime(), "yyyy-MM-dd"));
            summary.put("bodyType", bodyType);
            summary.put("edocType", edocSummary.getEdocType());
            summary.put("edocTypeName", EdocUtil.convertEdocType2Name(edocSummary.getEdocType()));
        }

        return summary;
    }

    @JsonIgnore
    public CtpAffair getAffairObj() {
        return this.ctpAffair;
    }

    @JsonIgnore
    public EdocSummary getSummaryObj() {
        return this.edocSummary;
    }

    public void setEdocSummary(EdocSummary edocSummary) {
        this.edocSummary = edocSummary;
    }

    public List<Attachment> getAttachmentList() {
        return attachmentList;
    }

    public void setAttachmentList(List<Attachment> attachmentList) {
        this.attachmentList = attachmentList;
    }

    public int getAttachmentSize() {
        return attachmentSize;
    }

    public void setAttachmentSize(int attachmentSize) {
        this.attachmentSize = attachmentSize;
    }

    public int getRelatedDocSize() {
        return relatedDocSize;
    }

    public void setRelatedDocSize(int relatedDocSize) {
        this.relatedDocSize = relatedDocSize;
    }

    public String getOpinionPolicy() {
        return opinionPolicy;
    }

    public void setOpinionPolicy(String opinionPolicy) {
        this.opinionPolicy = opinionPolicy;
    }

    public String getCancelOpinionPolicy() {
        return cancelOpinionPolicy;
    }

    public void setCancelOpinionPolicy(String cancelOpinionPolicy) {
        this.cancelOpinionPolicy = cancelOpinionPolicy;
    }

    public String getDisAgreeOpinionPolicy() {
        return disAgreeOpinionPolicy;
    }

    public void setDisAgreeOpinionPolicy(String disAgreeOpinionPolicy) {
        this.disAgreeOpinionPolicy = disAgreeOpinionPolicy;
    }

    public Map<String, Object> getCtpAffair() {
        Map<String, Object> affairMap = new HashMap<String, Object>();

        if (ctpAffair != null) {
            affairMap.put("finish", ctpAffair.isFinish());
            affairMap.put("state", ctpAffair.getState());
            affairMap.put("backFromId", ctpAffair.getBackFromId());
            affairMap.put("track", ctpAffair.getTrack());
            affairMap.put("activityId", ctpAffair.getActivityId());
            affairMap.put("subObjectId", ctpAffair.getSubObjectId());
            affairMap.put("id", ctpAffair.getId());
            affairMap.put("subState", ctpAffair.getSubState());
            affairMap.put("isDelete", ctpAffair.isDelete());
            affairMap.put("memberId", ctpAffair.getMemberId());
        }

        return affairMap;
    }

    public void setCtpAffair(CtpAffair ctpAffair) {
        this.ctpAffair = ctpAffair;
    }

    public Integer getAttribute() {
        return attribute;
    }

    public void setAttribute(Integer attribute) {
        this.attribute = attribute;
    }

    public String getSecretLevel() {
        return secretLevel;
    }

    public void setSecretLevel(String secretLevel) {
        this.secretLevel = secretLevel;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public Map<String, Object> getNowNodeOpinion() {
        Map<String, Object> op = new HashMap<String, Object>();
        if (nowNodeOpinion != null) {
            op.put("id", nowNodeOpinion.getId());
            op.put("content", nowNodeOpinion.getContent());
            op.put("attribute", nowNodeOpinion.getAttribute());
            //意见附件
            op.put("opinionAttachments", nowNodeOpinion.getOpinionAttachments());
        }
        return op;
    }

    public void setNowNodeOpinion(EdocOpinion nowNodeOpinion) {
        this.nowNodeOpinion = nowNodeOpinion;
    }

    public Long getTemplateProcessId() {
        return templateProcessId;
    }

    public void setTemplateProcessId(Long templateProcessId) {
        this.templateProcessId = templateProcessId;
    }

    /**
     * 公文类型
     *
     * @return
     */
    public int getModuleType() {

        if (ctpAffair != null) {
            return ctpAffair.getApp();
        }
        return 0;
    }

    public Map<String, String> getDefaultNode() {
        return defaultNode;
    }

    public void setDefaultNode(Map<String, String> defaultNode) {
        this.defaultNode = defaultNode;
    }

    /**
     * 获取当前登录人员信息
     *
     * @return
     * @Author : xuqw
     * @Date : 2016年6月30日下午7:56:50
     */
    public Map<String, String> getCurrentUser() {

        Map<String, String> cUser = new HashMap<String, String>();

        if (currentUser != null) {
            cUser.put("currentUserId", String.valueOf(currentUser.getId()));
            cUser.put("currentUserName", currentUser.getName());
            cUser.put("currentAccountId", String.valueOf(currentUser.getLoginAccount()));
            cUser.put("currentAccountName", currentUser.getLoginAccountName());
        }
        return cUser;
    }


    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public Map<String, String> getErrorRet() {
        return errorRet;
    }

    public void setErrorRet(Map<String, String> errorRet) {
        this.errorRet = errorRet;
    }

    public String getListType() {
        return listType;
    }

    public void setListType(String listType) {
        this.listType = listType;
    }

    public String getDisPosition() {
        return disPosition;
    }

    public void setDisPosition(String disPosition) {
        this.disPosition = disPosition;
    }

    public String getFiledValue() {
        return filedValue;
    }

    public void setFiledValue(String filedValue) {
        this.filedValue = filedValue;
    }

    public Boolean getSpecialStepBack() {
        return specialStepBack;
    }

    public void setSpecialStepBack(Boolean specialStepBack) {
        this.specialStepBack = specialStepBack;
    }

    public Boolean getIsCanComment() {
        return isCanComment;
    }

    public void setIsCanComment(Boolean isCanComment) {
        this.isCanComment = isCanComment;
    }

    public void setBodyLastModify(String bodyLastModify) {
        this.bodyLastModify = bodyLastModify;
    }

    public String getOptionType() {
        return optionType;
    }

    public void setOptionType(String optionType) {
        this.optionType = optionType;
    }

    public boolean isProcessTemplate() {
        return isProcessTemplate;
    }

    public void setProcessTemplate(boolean isProcessTemplate) {
        this.isProcessTemplate = isProcessTemplate;
    }

    public Integer getSubmitStyleCfg() {
        return submitStyleCfg;
    }

    public void setSubmitStyleCfg(Integer submitStyleCfg) {
        this.submitStyleCfg = submitStyleCfg;
    }

    public String getNodePolicy() {
        return nodePolicy;
    }

    public void setNodePolicy(String nodePolicy) {
        this.nodePolicy = nodePolicy;
    }


}
