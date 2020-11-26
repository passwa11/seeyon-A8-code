package com.seeyon.ctp.rest.resources;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.collaboration.util.CollaborationUtils;
import com.seeyon.apps.collaboration.vo.ColListSimpleVO;
import com.seeyon.apps.collaboration.vo.NodePolicyVO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.comment.CtpCommentAll;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Strings;

public class SummaryListVO {

	private final static Log LOGGER = LogFactory.getLog(SummaryListVO.class);
	
	private CommentManager commentManager = (CommentManager) AppContext.getBean("ctpCommentManager");
	
	private PermissionManager permissionManager = (PermissionManager) AppContext.getBean("permissionManager");
	
	private String affairId;
	
	private String summaryId;
	//发起人
	private String startMemberId;
	//重要程度
    private Integer importantLevel;
    //标题
	private String  subject;
	//发起人
    private String  startMemberName;
    //发起时间
    private String    startDate;
    //发起时间-标准格式
    private String    startDateStandard;
    //是否有附件
    private Boolean hasAttsFlag;
    //是否节点超期
    private Boolean isCoverTime;
    //剩余超期时间
    private String  surplusTime;   
    //节点权限名称
    private String nodeName; 
    //当前节点操作状态名称
    private String subStateName;
    //当前流程状态
    private Integer state;
    //当前事项
    private Integer affairState;
    
    private Integer subState;
    
    //冗余summary表中的流程状态，用于首页栏目图标判断
	private Integer summaryState;

    //加签
    private String fromName = "";
    private String fullfromName ="";
    //模板ID
    private String templateId;
    
    /**
     * 不同意时必须填写意见
     */
    private Integer disAgreeOpinionPolicy;
    
    public String getFullfromName() {
		return fullfromName;
	}

	public void setFullfromName(String fullfromName) {
		this.fullfromName = fullfromName;
	}

	//回退
    private String backFromName = "";
    private String fullBFName ="";
    
    public String getFullBFName() {
		return fullBFName;
	}

	public void setFullBFName(String fullBFName) {
		this.fullBFName = fullBFName;
	}

	//意见态度
    private String commentReply="";
    
    private String    workitemId;
    private String  processId;
    private String    caseId;
    private String    activityId;
    private String  bodyType;
    private Boolean hasFavorite;
    private String replyCounts;
    private int replyCountsNum;
    
   

	//是否有转发权限
    private Boolean canForward;

    /** 当前登录单位ID **/
    private String currrentLoginAccountId = null;
    
	
	private Boolean canDeleteORarchive;
	
	public Boolean getCanDeleteORarchive() {
        return canDeleteORarchive;
    }

    public void setCanDeleteORarchive(Boolean canDeleteORarchive) {
        this.canDeleteORarchive = canDeleteORarchive;
    }

    /**
     * 是否允许删除
     */
    private Boolean canReMove = true;
    
    
    public String getReplyCounts() {
		return replyCounts;
	}
    
    public int getReplyCountsNum() {
        return replyCountsNum;
    }

	public void setReplyCounts(String replyCounts) {
		this.replyCounts = replyCounts;
	}
   
    public Boolean getcanReMove() {
		return canReMove;
	}
    
	public void setcanReMove(Boolean canReMove) {
		this.canReMove = canReMove;
	}
	
	public SummaryListVO() {
		super();
	}
	
	
    public SummaryListVO(ColListSimpleVO col){
		this.summaryId = String.valueOf(col.getSummaryId());
		this.affairId = String.valueOf(col.getAffairId());
		this.importantLevel = col.getImportantLevel();
		this.subject = col.getSubject();
		this.startMemberName = col.getStartMemberName();
		
		this.hasAttsFlag = col.getHasAttsFlag();
		this.isCoverTime = col.getIsCoverTime();
		
		this.state = col.getState();
		this.subStateName = col.getSubStateName();
		this.startMemberId = String.valueOf(col.getStartMemberId());

		this.workitemId = String.valueOf(col.getWorkitemId());
		this.processId = col.getProcessId();
		this.caseId = String.valueOf(col.getCaseId());
		this.bodyType = col.getBodyType();
		this.hasFavorite = col.getHasFavorite();
        this.canForward = col.getCanForward();
        this.templateId = String.valueOf(col.getTempleteId());
        if(null != col.getReplyCounts()){
            replyCountsNum = col.getReplyCounts();
        }
        this.replyCounts =ResourceUtil.getString("collaboration.pending.replyCounts.label", replyCountsNum);
        this.canDeleteORarchive = col.getCanDeleteORarchive();
        this.disAgreeOpinionPolicy = col.getDisAgreeOpinionPolicy();
        this.summaryState = col.getSummaryState();
        this.canReMove = col.getCanReMove();
        
		try {
			if (col.getAffairId() != null) {
				this.activityId = String.valueOf(col.getActivityId());
				Date sDate = col.getCreateDate();
				if (col.getAffairState() != 1) {
					sDate = col.getStartDate();
				}
				this.startDate = CollaborationUtils.showDate(sDate);
				this.startDateStandard = DateUtil.format(sDate, "yyyy-MM-dd HH:mm");
				
				this.nodeName = col.getNodeName();
				this.affairState = col.getAffairState();
				this.subState = col.getSubState();
				//剩余时间
				this.surplusTime =  this.calculateSurplusTime(col.getReceiveTime(), col.getExpectedProcessTime());
				
				//加签、知会、当前会签  ps:回退优先，如果这条记录也被回退过，则优先显示回退图标，加签图标不显示
	            if(col.getFromId()!=null && col.getBackFromId()==null){
	            	String _fmName = Functions.showMemberName(col.getFromId());
	            	this.fullfromName = ResourceUtil.getString("collaboration.pending.addOrJointly.label", _fmName);//可以做为title显示。
	            	if(_fmName.length() > 4){
	            		_fmName = _fmName.substring(0,4) + "...";
	            	}
	            	this.fromName = ResourceUtil.getString("collaboration.pending.addOrJointly.label", _fmName);
	            }
	            //回退、指定回退
	            if(col.getBackFromId()!=null 
	            		&& !Integer.valueOf(SubStateEnum.col_pending_specialBackCenter.getKey()).equals(col.getSubState())){
	            	
	            	String _bfName = Functions.showMemberName(col.getBackFromId());
	            	this.fullBFName = ResourceUtil.getString("collaboration.pending.stepBack.label", _bfName);//可以做为title显示。
	            	if(_bfName.length() > 4){
	            		_bfName =  _bfName.substring(0, 4) +"...";
	            	}
	            	this.backFromName = ResourceUtil.getString("collaboration.pending.stepBack.label", _bfName);
	            	
	            }
				/*CtpCommentAll comment = commentManager.getLastDealComment(col.getAffairId());
				if (comment != null && comment.getExtAtt1() != null) {
					this.commentReply = ResourceUtil.getString(comment.getExtAtt1());
				}*/
			}
		} catch (Exception e) {
			LOGGER.error("对象转换出问题了！", e);
		}
	}
	/**
	 * 计算节点处理剩余时间(不计算超过24小时的)
	 * 
	 * @param date 流程到达时间
	 * @param expecetProcessTime 节点处理期限（具体时间）
	 * @return 剩余时间（返回不足一天的时间：小时 分）
	 */
	private String calculateSurplusTime(Date date, Date expecetProcessTime) {
		String surplusTime = "";
		try {
			if (expecetProcessTime != null) {
				// 获取系统当前时间
				Date nowTime = new Date();
				// 未超期
				if (nowTime.before(expecetProcessTime)) {
					// 得到剩余处理时间（分钟）
					long surplusMinu = (expecetProcessTime.getTime() - nowTime.getTime()) / (1000*60);
					//剩余小时
					long surplusHours = surplusMinu / 60;
					//剩余分钟
					long surplusMinute = surplusMinu % 60;
					if (surplusHours < 24) {
						if (surplusHours > 0) {
							surplusTime = surplusHours +ResourceUtil.getString("collaboration.summaryList.hours");
						}
						if (surplusMinute > 0) {
							surplusTime += surplusMinute + ResourceUtil.getString("collaboration.summaryList.minute");
						}
					}
				}
			}
			if (Strings.isNotBlank(surplusTime)) {
				surplusTime = ResourceUtil.getString("collaboration.summaryList.also")
						+surplusTime+ ResourceUtil.getString("collaboration.summaryList.timeOut");
			}
		} catch (Exception e) {
			LOGGER.error("计算节点处理剩余时间抛出异常", e);
		}
		return surplusTime;
	}
	
	public String getAffairId() {
		return affairId;
	}
	public void setAffairId(String affairId) {
		this.affairId = affairId;
	}

	public String getSummaryId() {
		return summaryId;
	}

	public void setSummaryId(String summaryId) {
		this.summaryId = summaryId;
	}
	
	public String getStartMemberId() {
		return startMemberId;
	}

	public void setStartMemberId(String startMemberId) {
		this.startMemberId = startMemberId;
	}

	public Integer getImportantLevel() {
		return importantLevel;
	}
	public void setImportantLevel(Integer importantLevel) {
		this.importantLevel = importantLevel;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getStartMemberName() {
		return startMemberName;
	}
	public void setStartMemberName(String startMemberName) {
		this.startMemberName = startMemberName;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public Boolean getHasAttsFlag() {
		return hasAttsFlag;
	}
	public void setHasAttsFlag(Boolean hasAttsFlag) {
		this.hasAttsFlag = hasAttsFlag;
	}
	public Boolean getIsCoverTime() {
		return isCoverTime;
	}
	public void setIsCoverTime(Boolean isCoverTime) {
		this.isCoverTime = isCoverTime;
	}
	public String getSurplusTime() {
		return surplusTime;
	}
	public void setSurplusTime(String surplusTime) {
		this.surplusTime = surplusTime;
	}
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getSubStateName() {
		return subStateName;
	}

	public void setSubStateName(String subStateName) {
		this.subStateName = subStateName;
	}

	public Integer getAffairState() {
		return affairState;
	}

	public void setAffairState(Integer affairState) {
		this.affairState = affairState;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getBackFromName() {
		return backFromName;
	}

	public void setBackFromName(String backFromName) {
		this.backFromName = backFromName;
	}

	public String getCommentReply() {
		return commentReply;
	}

	public void setCommentReply(String commentReply) {
		this.commentReply = commentReply;
	}

	public Integer getSubState() {
		return subState;
	}

	public void setSubState(Integer subState) {
		this.subState = subState;
	}

	public String getWorkitemId() {
		return workitemId;
	}

	public void setWorkitemId(String workitemId) {
		this.workitemId = workitemId;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public String getBodyType() {
		return bodyType;
	}

	public void setBodyType(String bodyType) {
		this.bodyType = bodyType;
	}

    public Boolean getHasFavorite() {
        return hasFavorite;
    }

    public void setHasFavorite(Boolean hasFavorite) {
        this.hasFavorite = hasFavorite;
    }

	public Boolean getAffairSentCanForward() {
		boolean isNewColNode = (this.affairState != null && StateEnum.col_sent.getKey() == this.affairState.intValue());
	    if (isNewColNode) {
	    	NodePolicyVO nodePolicy = null;
	        try {
	            Permission permission = permissionManager.getPermission(EnumNameEnum.col_flow_perm_policy.name(), "newCol", Long.valueOf(currrentLoginAccountId));
	            nodePolicy = new NodePolicyVO(permission);
	            if (!nodePolicy.isForward()) {
		        	return Boolean.FALSE;
		        }
	        } catch (BusinessException e) {
	            LOGGER.error("", e);
	        }
	    }
	    return Boolean.TRUE;
	}
	
	public Boolean getCanForward() {
		return canForward;
	}

	public void setCanForward(Boolean canForward) {
		this.canForward = canForward;
	}
	
	public void setCurrrentLoginAccountId(String currrentLoginAccountId) {
		this.currrentLoginAccountId = currrentLoginAccountId;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public Integer getDisAgreeOpinionPolicy() {
		return disAgreeOpinionPolicy;
	}

	public void setDisAgreeOpinionPolicy(Integer disAgreeOpinionPolicy) {
		this.disAgreeOpinionPolicy = disAgreeOpinionPolicy;
	}

	public String getStartDateStandard() {
		return startDateStandard;
	}

	public void setStartDateStandard(String startDateStandard) {
		this.startDateStandard = startDateStandard;
	}
	
	public Integer getSummaryState() {
		return summaryState;
	}

	public void setSummaryState(Integer summaryState) {
		this.summaryState = summaryState;
	}
}
