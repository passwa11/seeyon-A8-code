package com.seeyon.apps.govdoc.manager.impl;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.govdoc.bo.DateSharedWithWorkflowEngineThreadLocal;
import com.seeyon.apps.govdoc.constant.GovdocEnum.GovdocFromTypeEnum;
import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.helper.GovdocMessageHelper;
import com.seeyon.apps.govdoc.listener.GovdocWorkflowEventListener;
import com.seeyon.apps.govdoc.manager.GovdocMessageManager;
import com.seeyon.apps.govdoc.manager.GovdocOCIPExchangeManager;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.ocip.OCIPConstants;
import com.seeyon.apps.ocip.util.CommonUtil;
import com.seeyon.apps.ocip.util.OrgUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.BeanUtils;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UniqueList;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ocip.common.IConstant;
import com.seeyon.ocip.common.org.OcipOrgMember;
import com.seeyon.ocip.common.org.OrgMember;
import com.seeyon.ocip.common.organization.IOrganizationManager;
import com.seeyon.ocip.common.utils.Global;
import com.seeyon.ocip.configuration.OcipConfiguration;
import com.seeyon.ocip.exchange.api.IBussinessService;
import com.seeyon.ocip.exchange.exceptions.ExchangeException;
import com.seeyon.ocip.exchange.model.BIZContentType;
import com.seeyon.ocip.exchange.model.col.Affair;
import com.seeyon.ocip.exchange.model.col.ColOperation;
import com.seeyon.ocip.exchange.model.edoc.EdocOCIPSummary;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.workflow.event.EdocWorkflowEventListener;

import net.joinwork.bpm.engine.wapi.WorkItem;

public class GovdocOCIPExchangeManagerImpl implements GovdocOCIPExchangeManager{
	private static final Log      log = LogFactory.getLog(GovdocOCIPExchangeManagerImpl.class);
	public static final String MemberId_Of_OCIP_Client = "memberId_of_ocip_client";
	private boolean hasMutiPlugin = Boolean.valueOf(AppContext.getSystemProperty(OCIPConstants.HAS_MUTI_PLUGIN));//是否开启多G6功能
	
	
	private AffairManager affairManager;
	private GovdocSummaryManager govdocSummaryManager;
	private UserMessageManager userMessageManager;
	private IOrganizationManager organizationManager;
	private GovdocMessageManager govdocMessageManager;
	private OrgManager orgManager;
	
	public OrgManager getOrgManager() {
		return orgManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public UserMessageManager getUserMessageManager() {
		return userMessageManager;
	}

	public void setUserMessageManager(UserMessageManager userMessageManager) {
		this.userMessageManager = userMessageManager;
	}

	public IOrganizationManager getOrganizationManager() {
		return organizationManager;
	}

	public void setOrganizationManager(IOrganizationManager organizationManager) {
		this.organizationManager = organizationManager;
	}

	public AffairManager getAffairManager() {
		return affairManager;
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	public GovdocSummaryManager getGovdocSummaryManager() {
		return govdocSummaryManager;
	}

	public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}

	private IBussinessService getBussinessService() {
		return OcipConfiguration.getInstance().getExchangeSpi().getBussinessService();
	}

	@Override
	public void handCollaborationAffair(AffairData data, ColOperation operation, EdocSummary edocSummary) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("ColOperation", operation);
		param.put("affairData", data);
		param.put("edocSummary", edocSummary);
		try {
			getBussinessService().fireExchange(BIZContentType.EDOC, param);
		} catch (ExchangeException e) {
			log.error("",e);
		}
	}

	@Override
	public void handCollaborationAffair(AffairData data, ColOperation operation, EdocSummary edocSummary,
			String opinionContent) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("ColOperation", operation);
		param.put("affairData", data);
		param.put("edocSummary", edocSummary);
		param.put("opinionContent", opinionContent);
		try {
			getBussinessService().fireExchange(BIZContentType.EDOC, param);
		} catch (ExchangeException e) {
			log.error("",e);
		}
	}

	@Override
	public void sendEdocAndMeg(Affair affair, EdocOCIPSummary edocSummary, MessageReceiver receiver, String sendName,
			int opinionType, String opinionContent, String sendSysCode) {
		try{
			//保存待办和colSummary
			EdocSummary v5Summary = new EdocSummary();
			BeanUtils.convert(v5Summary, edocSummary);
			CtpAffair v5Affair = new CtpAffair();
	    	BeanUtils.convert(v5Affair, affair);
	    	if(affair.getFromId() == null){
		    	v5Affair.setPreApprover(affair.getSenderId());
	    	}
	    	//v5Affair.setAddition(sendSysCode);
	    	Map<String, Object> paraMap = AffairUtil.getExtProperty(v5Affair);
			paraMap.put("ocipSystemCode", sendSysCode);
			AffairUtil.setExtProperty(v5Affair, paraMap);
			if(v5Summary.getId()==null){
				v5Summary.setId(edocSummary.getId());
			}
			v5Summary.setSubject(edocSummary.getSubject());
			v5Summary.setCanTrack(edocSummary.getCanTrack());
			v5Summary.setCreateTime(new Timestamp(System.currentTimeMillis()));
			v5Summary.setGovdocType(edocSummary.getGovdocType());
			v5Summary.setEdocType(edocSummary.getEdocType());
			v5Summary.setFromType(GovdocFromTypeEnum.m3sso.ordinal());//外部M3SSO
			govdocSummaryManager.saveOrUpdateEdocSummary(v5Summary);
	    	this.dealUpdateAffair(v5Affair);
	        //affairManager.updateAffair(v5Affair);
	        //发送消息
	    	AffairData affairData = new AffairData();
	    	affairData.setSender(v5Affair.getSenderId());
	    	affairData.setSubject(v5Affair.getSubject());
	    	affairData.setForwardMember(v5Affair.getForwardMember());
	    	affairData.setBodyType(v5Affair.getBodyType());
	    	List<CtpAffair> affList = new ArrayList<CtpAffair>();
	    	affList.add(v5Affair);
	    	affairData.setAffairList(affList);
	    	List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
	    	receivers.add(receiver);
	    	govdocMessageManager.sendMessage(affairData, receivers, null, affair.getCreateDate());
/*	        String froward = v5Affair.getForwardMember();
            int isFroward = Strings.isBlank(froward) ? 0 : 1;
			String forwardName="";
            if(isFroward==1){
            	OcipOrgMember forwardMember = organizationManager.getMember(froward);
            	if(null!=forwardMember){
            		forwardName=forwardMember.getName();
            	}
            }
	        Long msgSenderId = v5Affair.getSenderId();
			
			Object[] subjects = new Object[] { affair.getSubject(), sendName, isFroward, forwardName,0,"",0,v5Affair.getSubApp()};
	        MessageContent mContent = MessageContent.get("govdoc.send", subjects);
	    	userMessageManager.sendSystemMessage(
					mContent.setImportantLevel(v5Affair.getImportantLevel()),
					ApplicationCategoryEnum.edoc,msgSenderId, receiver, v5Affair.getImportantLevel());*/
			//MessageContent mc = new MessageContent("collaboration.opinion.deal", sendName, affair.getSubject(), 0, null, opinionType, opinionContent, -1, -1, 0);
	        //userMessageManager.sendSystemMessage(mc, ApplicationCategoryEnum.collaboration,msgSenderId, receiver);
		}catch(Exception e){
			 log.error(e.getMessage(), e);
		}
	}
	/**
	 * 已办
	 * @param affair
	 * @param colSummary
	 * @param msgContent
	 */
	@Override
	public void sendDone(Affair affair, EdocOCIPSummary edocSummary, String sendSysCode) {
		try{
			//保存待办和colSummary
			EdocSummary v5Summary = new EdocSummary();
			BeanUtils.convert(v5Summary, edocSummary);
			v5Summary.setId(edocSummary.getId());
			v5Summary.setSubject(edocSummary.getSubject());
			v5Summary.setCanTrack(edocSummary.getCanTrack());
			//v5Summary.setCreateTime(new Timestamp(System.currentTimeMillis()));
			v5Summary.setGovdocType(edocSummary.getGovdocType());
			v5Summary.setEdocType(edocSummary.getEdocType());
			v5Summary.setFromType(GovdocFromTypeEnum.m3sso.ordinal());
			CtpAffair v5Affair = new CtpAffair();
	    	BeanUtils.convert(v5Affair, affair);
	    	//v5Affair.setAddition(sendSysCode);
	    	Map<String, Object> paraMap = AffairUtil.getExtProperty(v5Affair);
			paraMap.put("ocipSystemCode", sendSysCode);
			AffairUtil.setExtProperty(v5Affair, paraMap);
	    	EdocSummary oldSummary = govdocSummaryManager.getSummaryById(v5Summary.getId());
	    	if(oldSummary.getCompleteTime()!=null){
	    		v5Summary.setState(3);//已完成
	    		v5Summary.setCurrentNodesInfo("");
	    		v5Summary.setCompleteTime(oldSummary.getCompleteTime());;
	    	}
	    	//对于没有传输完成时间的补起
	    	if(v5Summary.getCompleteTime() !=null && v5Affair.getCompleteTime() == null){
	    		v5Summary.setState(3);//已完成
	    		v5Summary.setCurrentNodesInfo("");
	    		v5Affair.setCompleteTime(new Date());
	    	}
    		//v5Affair.setUpdateDate(new Date());//以一个发起公文的服务器的时间为准，本机不再重新设置更新时间 chenyq
    		govdocSummaryManager.updateEdocSummary(v5Summary);
	    	this.dealUpdateAffair(v5Affair);
	        //affairManager.updateAffair(v5Affair);
	        
		}catch(Exception e){
			 log.error(e.getMessage(), e);
		}
	}
	/**
	 * 统一处理事务的更新
	 * @param affair
	 */
	private void dealUpdateAffair(CtpAffair v5Affair){
		//检查是更新还是新插入
		long newAffairId = v5Affair.getId();
		Date newUpdateDate = v5Affair.getUpdateDate();
		boolean isUpdate = true;//默认要更新数据
		try {
			CtpAffair oldAffair = affairManager.get(newAffairId);
			if(oldAffair != null){//存在即更新
				Date oldUpdateDate = oldAffair.getUpdateDate();
				//如果 新数据的更新时间小于原有数据的时间，则不更新、
				if(newUpdateDate!=null&&oldUpdateDate !=null&&(newUpdateDate.before(oldUpdateDate))){
					isUpdate = false;
				}
			}
	
		} catch (BusinessException e) {
			log.error("",e);
		}
		//处理更新
		try{
			if(isUpdate){
				affairManager.updateAffair(v5Affair);	
			}
		}catch(Exception e){
			log.error("",e);
		}
	}

	@Override
	public void sendHasten(Affair affair, EdocOCIPSummary edocSummary, String msgContent, boolean sendMsg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendStepBack(Affair affair, EdocOCIPSummary edocSummary, String sendName, ColOperation operation,
			String opinionContent, boolean isSelf, String systemCode) {
		try{
			//保存待办和colSummary
			CtpAffair v5Affair = new CtpAffair();
	    	BeanUtils.convert(v5Affair, affair);
			EdocSummary v5Summary = null;
			v5Summary = govdocSummaryManager.getSummaryById(v5Affair.getObjectId());
			v5Summary.setId(edocSummary.getId());
			v5Summary.setSubject(edocSummary.getSubject());
			v5Summary.setCanTrack(edocSummary.getCanTrack());
			//v5Summary.setCreateTime(new Timestamp(System.currentTimeMillis()));
			v5Summary.setGovdocType(edocSummary.getGovdocType());
			v5Summary.setEdocType(edocSummary.getEdocType());
			v5Summary.setFromType(GovdocFromTypeEnum.m3sso.ordinal());
			//发送消息	    
	        Long msgSenderId = v5Affair.getSenderId();
	        String froward = v5Affair.getForwardMember();
            int isFroward = Strings.isBlank(froward) ? 0 : 1;
			 MessageReceiver receiver =new MessageReceiver(affair.getId(), affair
						.getMemberId().longValue(), "message.link.govdoc.pending",
						new Object[]{affair.getId().toString()});
			v5Affair.setSubState(SubStateEnum.col_pending_read.key());
			Map<String, Object> paraMap = AffairUtil.getExtProperty(v5Affair);
			paraMap.put("ocipSystemCode", systemCode);
			AffairUtil.setExtProperty(v5Affair, paraMap);
			 //v5Affair.setAddition(systemCode);
			String messageLink="edoc.msg.takeback";
			//特殊判断-对于回退回来的消息接收人包含自己的要过滤掉
            try{
				String senderIdStr = organizationManager.getLocalObjectId(IConstant.AddressType.member.name(), String.valueOf(affair.getSenderId()));	
				if(Long.parseLong(senderIdStr)==affair.getMemberId()){
					isSelf = true;
				}
            }catch(Exception e){
   			 log.error(e.getMessage(), e);
            }
        	switch (operation){
			case STEPBACK:
				v5Affair.setState(StateEnum.col_stepBack.getKey());
				if(!isSelf){//控制是否发送消息
					//govdocMessageManager.sendStepBackMessage(allTrackAffairLists, affair, summaryId, signOpinion, traceFlag, msg2Sender)
		            userMessageManager.sendSystemMessage(new MessageContent("edoc.stepBack", affair.getSubject(), sendName, isFroward, 
		            		froward,opinionContent,-1).setImportantLevel(affair.getImportantLevel()), 
	            			ApplicationCategoryEnum.edoc, msgSenderId, receiver, v5Affair.getImportantLevel());
				}
	            break;
			case TAKEBACK:
				//取回的时候回发送两条TAKEBACK包，一条已办数据，一条下一步人的待办数据，这里暂时以状态区分:待办数据更新为取回，已办数据更新为待办
				CtpAffair localAffair = affairManager.get(v5Affair.getId());
				if(localAffair.getState()==StateEnum.col_pending.key()){
					v5Affair.setState(StateEnum.col_takeBack.key());
				}else{
					v5Affair.setState(StateEnum.col_pending.key());
					v5Affair.setSubState(SubStateEnum.col_pending_unRead.key());
					v5Affair.setCompleteTime(null);
					v5Affair.setOverTime(null);
					v5Affair.setOverWorktime(null);
					v5Affair.setRunTime(null);
					v5Affair.setRunWorktime(null);
					//v5Affair.setUpdateDate(new Timestamp(System.currentTimeMillis()));
					//取回后 将代理人清空
					v5Affair.setTransactorId(null);
				}
				if(!isSelf){//控制是否发送消息
					messageLink="godvoc.msg.takeback";
					MessageContent content1 = MessageContent.get(messageLink, affair.getSubject(),sendName, isFroward, froward);          
		            userMessageManager.sendSystemMessage(content1, ApplicationCategoryEnum.edoc, msgSenderId, receiver);
				}
				break;
			case REVOKED:
				v5Affair.setState(StateEnum.col_cancel.getKey());
				if(!isSelf){//控制是否发送消息
					messageLink="edoc.summary.cancel";
					userMessageManager.sendSystemMessage(new MessageContent(messageLink, v5Affair.getSubject(), sendName, opinionContent, isFroward, froward,1)
		            		   .setImportantLevel(v5Affair.getImportantLevel()),ApplicationCategoryEnum.edoc, v5Affair.getSenderId(), receiver, v5Affair);
				}
				break;
					
            } 
			
//				v5Affair.setState(StateEnum.col_cancel.key());
        	/*if(v5Summary.getGovdocType() == null){
            		v5Affair.setDelete(false);
            	}*/
            this.dealUpdateAffair(v5Affair);
        	//affairManager.updateAffair(v5Affair);

            if(v5Affair.getDeadlineDate() != null && v5Affair.getDeadlineDate() != 0){
                WFComponentUtil.deleteQuartzJobForNode(v5Affair);
            }
            //affairManager.updateAffairsState2Cancel(v5Summary.getId());  
            v5Summary.setCaseId(null);
            v5Summary.setState(EdocConstant.flowState.cancel.ordinal());
            v5Summary.setCoverTime(Boolean.FALSE);
            //更新当前处理人信息
            GovdocHelper.updateCurrentNodesInfo(v5Summary, false);
            //edocStatManager.deleteEdocStat(v5Summary.getId());
            govdocSummaryManager.updateEdocSummary(v5Summary);
		}catch(Exception e){
			 log.error(e.getMessage(), e);
		}
		
	}

	@Override
	public void competitionDone(Affair affair, EdocOCIPSummary edocSummary) {
		EdocSummary v5Summary = new EdocSummary();
		BeanUtils.convert(v5Summary, edocSummary);
		CtpAffair v5Affair = new CtpAffair();
		BeanUtils.convert(v5Affair, affair);
		User user = AppContext.getCurrentUser();
		if (user == null) {
			user = new User();
			//发送端将senderId替换为处理者
			Long userId = affair.getSenderId();
			user.setId(userId);
			try {
				V3xOrgMember member = orgManager.getMemberById(userId);
				if (null != member) {
					user.setName(member.getName());
				}
				CommonUtil.setCurrentUser(user);
			} catch (BusinessException e) {
				// TODO Auto-generated catch block
				log.error(e);
				//e.printStackTrace();
			}
		}

		ApplicationCategoryEnum applicationCategoryEnum = null;
		if (affair.getApp() == ApplicationCategoryEnum.edoc.getKey()) {
			applicationCategoryEnum = ApplicationCategoryEnum.edoc;
		} else {
			applicationCategoryEnum = ApplicationCategoryEnum.collaboration;
		}
		List<CtpAffair> affairs = null;
		try {
			affairs = affairManager.getAffairsByObjectIdAndNodeId( affair.getObjectId(),
					affair.getActivityId());
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			log.error(e);
			//e.printStackTrace();
		}
		if (null != affairs && !affairs.isEmpty()) {
			StringBuilder hql = new StringBuilder();
			hql.append(
					"update CtpAffair set state=:state,subState=:subState,updateDate=:updateDate where id=:id");
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("state", StateEnum.col_competeOver.key());
			params.put("subState", SubStateEnum.col_normal.key());
			params.put("updateDate", new Date());
			params.put("id", affair.getId());
			try {
				affairManager.update(hql.toString(), params);
			} catch (BusinessException e) {
				// TODO Auto-generated catch block
				log.error(e);
				//e.printStackTrace();
			}

			// 给在竞争执行中被取消的affair发送消息提醒
			GovdocMessageHelper.competitionCancel(affairManager, orgManager, userMessageManager, new WorkItem(), affairs,v5Affair);
		}
		// 更新当前处理人信息
		EdocSummary summary = (EdocSummary) DateSharedWithWorkflowEngineThreadLocal
				.getColSummary();
		if (summary == null) {
			try {
				summary =govdocSummaryManager.getSummaryById(affair.getObjectId());
			} catch (BusinessException e) {
				// TODO Auto-generated catch block
				log.error(e);
				//e.printStackTrace();
			}
		}
		if (summary == null) {
			return;
		}
		if (affair.getState().equals(StateEnum.col_pending.getKey())) {// 当前节点为待办的时候增加到当前处待办人信息
			summary.setCurrentNodesInfo(affair.getMemberId() + "");
		} else {
			summary.setCurrentNodesInfo("");
		}
		DateSharedWithWorkflowEngineThreadLocal.setColSummary(summary);
	}

	@Override
	public void sendStop(Affair affair, EdocOCIPSummary edocSummary, String sendSysCode, String sendName,
			String opinionContent, boolean isSelf) {
		try{
			//保存待办和colSummary
			EdocSummary v5Summary = new EdocSummary();
			BeanUtils.convert(v5Summary, edocSummary);
			CtpAffair v5Affair = new CtpAffair();
	    	BeanUtils.convert(v5Affair, affair);
	    	//v5Affair.setAddition(sendSysCode);
	    	Map<String, Object> paraMap = AffairUtil.getExtProperty(v5Affair);
			paraMap.put("ocipSystemCode", sendSysCode);
			AffairUtil.setExtProperty(v5Affair, paraMap);
			v5Summary.setId(edocSummary.getId());
			v5Summary.setSubject(edocSummary.getSubject());
			v5Summary.setCanTrack(edocSummary.getCanTrack());
			//v5Summary.setCreateTime(new Timestamp(System.currentTimeMillis()));
			v5Summary.setGovdocType(edocSummary.getGovdocType());
			v5Summary.setEdocType(edocSummary.getEdocType());
			v5Summary.setFromType(GovdocFromTypeEnum.m3sso.ordinal());
	    	govdocSummaryManager.updateEdocSummary(v5Summary);
	    	this.dealUpdateAffair(v5Affair);
	        //affairManager.updateAffair(v5Affair);
	        //发送提醒信息
	    	String froward = v5Affair.getForwardMember();
	        int isFroward = Strings.isBlank(froward) ? 0 : 1;
	        Long msgSenderId = v5Affair.getSenderId();
	    	Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.govdoc.done.detail", affair.getId()));
            if(!isSelf){//控制是否发送消息
            	userMessageManager.sendSystemMessage(new MessageContent("edoc.terminate", v5Affair.getSubject(), 
	    			sendName, isFroward, froward, -1, opinionContent).setImportantLevel(v5Affair.getImportantLevel()), 
                    ApplicationCategoryEnum.edoc, msgSenderId, receivers, v5Affair.getImportantLevel()); 
            }
		}catch(Exception e){
			 log.error(e.getMessage(), e);
		}
		
	}

	@Override
	public void sendComments(Affair affair, EdocOCIPSummary edocSummary, String sendName) {
		
	}

	@Override
	public void sendOvertime(Affair affair, EdocOCIPSummary edocSummary, String sendName) {
		
	}

	@Override
	public void sendModifyBody(Affair affair, EdocOCIPSummary edocSummary, String sendName) {
		
	}

	@Override
	public void sendCommentsReply(Affair affair, EdocOCIPSummary edocSummary, String sendName) {
		
	}

	@Override
	public void sendFavorite(Affair affair, EdocOCIPSummary edocSummary, String sendName) {
		
	}

	@Override
	public void forward(Affair affair, EdocOCIPSummary edocSummary, String sendName, String sendSysCode) {
		
	}

	@Override
	public void sendZcdb(Affair affair, EdocOCIPSummary edocSummary) {
		
	}

	public GovdocMessageManager getGovdocMessageManager() {
		return govdocMessageManager;
	}

	public void setGovdocMessageManager(GovdocMessageManager govdocMessageManager) {
		this.govdocMessageManager = govdocMessageManager;
	}

	@Override
	public void onWorkflowAssigned(AffairData affairData,EdocSummary summary) {
		if (affairData!=null&&summary!=null) {
			try {
				List<CtpAffair> affairList = affairData.getAffairList();
				AffairData ocipAffairData = (AffairData)org.apache.commons.beanutils.BeanUtils.cloneBean(affairData);
					List<CtpAffair> list = new UniqueList<CtpAffair>();
					// 存在事务缓存,本来不是pengding的他的status也=3,这样就可以过滤了
					List<CtpAffair> pendingList = affairManager.getAffairs(summary.getId(), StateEnum.col_pending);
					Set<Long> pendingIds = new HashSet<Long>();
					for (CtpAffair pAff : pendingList) {
						try {
							if (pAff.getId().equals((Long) affairData.getBusinessData().get("currentAffairId")))
								continue;
						} catch (Exception e) {
							log.error("", e);
						}
						pendingIds.add(pAff.getId());
					}
					String resource = Global.getConfig("sysCode");
					List<String> objectList = new ArrayList<String>();
					Map<String, List<CtpAffair>> localAffair = new HashMap<String, List<CtpAffair>>();
					for (CtpAffair aff : affairList) {
						if (pendingIds.contains(aff.getId()) && aff.getApp() == ApplicationCategoryEnum.edoc.getKey()) {
							//aff.setFromId(ColUtil.isNotBlank(contextList.get(0).getAddedFromId()) ? Long.valueOf(contextList.get(0).getAddedFromId()) : null);
							V3xOrgMember dMember = orgManager.getMemberById(aff.getMemberId());
							if (OrgUtil.isPlatformEntity(dMember)) {
								CtpAffair cloneBean = (CtpAffair) org.apache.commons.beanutils.BeanUtils.cloneBean(aff);
								list.add(cloneBean);
							}else {// 本系统
								if(localAffair.containsKey(dMember.getId().toString())){
									localAffair.get(dMember.getId().toString()).add(aff);
								}else{
									List<CtpAffair> affList = new ArrayList<CtpAffair>();
									affList.add(aff);
									localAffair.put(dMember.getId().toString(), affList);
								}
								objectList.add(dMember.getId().toString());
							}
						}
					}
					if (hasMutiPlugin&&!objectList.isEmpty()&&organizationManager!=null) {
						Map<String, List<OrgMember>> mappingMember = organizationManager.getMemberByObjectIds(objectList, resource);
						for (String objectId : objectList) {
							List<OrgMember> members = mappingMember.get(objectId);
							if (members != null && members.size() > 1) {
								List<CtpAffair> affList = localAffair.get(objectId);
								for (CtpAffair aff : affList) {
									CtpAffair cloneBean = (CtpAffair) org.apache.commons.beanutils.BeanUtils.cloneBean(aff);
									Map<String, Object> affairExtMap = AffairUtil.getExtProperty(cloneBean);
									affairExtMap.put(MemberId_Of_OCIP_Client, aff.getMemberId());
									cloneBean.setMemberId(Long.valueOf(members.get(0).getOrgPlatformUserId()));
									if(cloneBean.getFromId()!=null){
										OcipOrgMember m = organizationManager.getMember(cloneBean.getFromId().toString(), resource);
										if(m!=null){
											cloneBean.setFromId(Long.valueOf(m.getId()));
										}
									}
									AffairUtil.setExtProperty(cloneBean, affairExtMap);
									list.add(cloneBean);
								}
							}
						}
					}
					ocipAffairData.setAffairList(list);
				
				if(ocipAffairData.getAffairList() !=null && ocipAffairData.getAffairList().size()>0 &&ocipAffairData.getAffairList().get(0).getApp()==ApplicationCategoryEnum.edoc.getKey()){
					this.handCollaborationAffair(ocipAffairData, ColOperation.TODO,summary);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

		

	@Override
	public void onWorkitemFinished(EventDataContext context) {
		try {
			AffairData affairData = (AffairData) context.getBusinessData(EventDataContext.CTP_AFFAIR_DATA);
			CtpAffair affair = affairManager.getAffairBySubObjectId(context.getWorkItem().getId());		
			affair.setState(StateEnum.col_done.key());
			List<CtpAffair> list = affairData.getAffairList();
			AffairData	cloneAData = null;
			try {
				cloneAData = (AffairData) org.apache.commons.beanutils.BeanUtils.cloneBean(affairData);
			}catch(Exception e){
				log.error("AffairData拷贝失败",e);
			}
			if (Strings.isEmpty(list)) {
				list = new UniqueList<CtpAffair>();
			}
			CtpAffair cloneBean=null;
			try {
				cloneBean = (CtpAffair) org.apache.commons.beanutils.BeanUtils.cloneBean(affair);
			}  catch (Exception e) {
				log.error("CtpAffair拷贝失败",e);
			}
			list.add(cloneBean);
			EdocSummary summary = null;
			EdocSummary cloneSummary = null;
			Object summaryObj = context.getAppObject();
			if (summaryObj != null) {
				summary = (EdocSummary) summaryObj;
			} else {
				try {
					summary = govdocSummaryManager.getSummaryById(affairData.getModuleId());
				} catch (Exception e) {
					log.error("",e);
				}
			}
			cloneAData.setAffairList(list);
			if(affair.getApp() == ApplicationCategoryEnum.edoc.getKey()){
				V3xOrgMember dMember = orgManager.getMemberById(affair.getMemberId());
				if (OrgUtil.isPlatformEntity(dMember)) {
				   try{
					   cloneSummary =  (EdocSummary) org.apache.commons.beanutils.BeanUtils.cloneBean(summary);
				   }  catch (Exception e) {
						log.error("EdocSummary拷贝失败",e);
				   }
				   this.handCollaborationAffair(cloneAData, ColOperation.DONE,cloneSummary);
				} else {
					try{
						cloneSummary =  (EdocSummary) org.apache.commons.beanutils.BeanUtils.cloneBean(summary);
					}catch (Exception e) {
						log.error("ColSummary拷贝失败",e);
					}
					String resource = Global.getConfig("sysCode");
					List<String> objectList = new ArrayList<String>();
					objectList.add(dMember.getId().toString());
					if (organizationManager != null&&hasMutiPlugin) {
						Map<String, List<OrgMember>> mappingMember = organizationManager
								.getMemberByObjectIds(objectList, resource);
						for (String objectId : objectList) {
							List<OrgMember> members = mappingMember.get(objectId);
							if (members != null && members.size() > 1) {
								Map<String, Object> affairExtMap = AffairUtil.getExtProperty(cloneBean);
								affairExtMap.put(MemberId_Of_OCIP_Client, cloneBean.getMemberId());
								cloneBean.setMemberId(Long.valueOf(members.get(0).getOrgPlatformUserId()));
								AffairUtil.setExtProperty(cloneBean, affairExtMap);
								this.handCollaborationAffair(cloneAData, ColOperation.DONE,
										cloneSummary);
							}
						}
					}
				}
			}
		} catch (BusinessException e) {
			log.error(e.getMessage(), e);
		}
		
	}

	@Override
	public void onProcessFinished(EventDataContext context) {
		// 本想事务完成后再通知一次，但是发送前获取的ColSummary对象还是更新前的，没找到原因，暂时注释掉
		try {
			CtpAffair currentAffair = null;
			Object currentAffairObj = context.getBusinessData(GovdocWorkflowEventListener.CTPAFFAIR_CONSTANT);
			if (currentAffairObj != null) {
				currentAffair = (CtpAffair) currentAffairObj;
			}
			EdocSummary cloneSummary = null;
			EdocSummary summary = (EdocSummary) context.getAppObject();
			if (summary == null) {
				summary = govdocSummaryManager.getSummaryByProcessId(context.getProcessId());
			}
			if (summary == null) {
				return;
			}
			AffairData affairData = (AffairData) context.getBusinessData(EventDataContext.CTP_AFFAIR_DATA);
			if (affairData == null) {
				affairData = new AffairData();
			}
			AffairData cloneAData = null;
			try {
				cloneAData = (AffairData) org.apache.commons.beanutils.BeanUtils.cloneBean(affairData);
			} catch (Exception e) {
				log.error("AffairData拷贝失败", e);
			}
			if (null != currentAffair) {
				try {
					final CtpAffair cloneBean = (CtpAffair) org.apache.commons.beanutils.BeanUtils.cloneBean(currentAffair);
					cloneBean.setState(StateEnum.col_done.key());
					cloneSummary = (EdocSummary) org.apache.commons.beanutils.BeanUtils.cloneBean(summary);
					if (summary.getCompleteTime() != null) {
						cloneSummary.setState(3);
						cloneSummary.setCurrentNodesInfo("");
					}
					cloneAData.setAffairList(new ArrayList<CtpAffair>() {
						{
							add(cloneBean);
						}
					});
				} catch (Exception e) {
					log.error("", e);
				}
			}
			if (null != currentAffair && currentAffair.getApp() == ApplicationCategoryEnum.edoc.getKey()) {
				V3xOrgMember dMember = orgManager.getMemberById(currentAffair.getMemberId());
				if (OrgUtil.isPlatformEntity(dMember)) {

					this.handCollaborationAffair(cloneAData, ColOperation.DONE, cloneSummary);
				} else if (organizationManager != null && hasMutiPlugin) {
					
					String resource = Global.getConfig("sysCode");
					List<String> objectList = new ArrayList<String>();
					objectList.add(dMember.getId().toString());
					Map<String, List<OrgMember>> mappingMember = organizationManager.getMemberByObjectIds(objectList,
							resource);
					for (String objectId : objectList) {
						List<OrgMember> members = mappingMember.get(objectId);
						if (members != null && members.size() > 1) {
							cloneAData.setState(500);
							cloneAData.setCreateDate(summary.getCompleteTime());
							CtpAffair cloneBean = cloneAData.getAffairList().get(0);
							Map<String, Object> affairExtMap = AffairUtil.getExtProperty(cloneBean);
							affairExtMap.put(MemberId_Of_OCIP_Client, cloneBean.getMemberId());
							cloneBean.setMemberId(Long.valueOf(members.get(0).getOrgPlatformUserId()));
							AffairUtil.setExtProperty(cloneBean, affairExtMap);
							this.handCollaborationAffair(cloneAData, ColOperation.DONE,
									cloneSummary);
						}
					}
				}
			}

		} catch (BusinessException e) {
			log.error(e.getMessage(), e);
		}
		
	}

	@Override
	public void onProcessCanceled(EventDataContext context) {
		try {
			Integer _operationType = (Integer) context.getBusinessData().get("operationType");
			Long affairId = null;
            if(context.getBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_AFFAIR_ID) != null) {
                affairId = (Long)context.getBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_AFFAIR_ID);
            }
            String repealComment= "";
            if(context.getBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_COMMENT_CONTENT) != null) {
                repealComment = (String)context.getBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_COMMENT_CONTENT);
            }
            Long summaryId = null;
            if(context.getBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_SUMMARY_ID) != null) {
                summaryId = (Long)context.getBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_SUMMARY_ID);
            }
			Long _summaryId = null;
			Long currentWorkitemId = context.getCurrentWorkitemId();
			CtpAffair currentAffair = null;
			if (affairId==null && currentWorkitemId != null) {
				currentAffair = affairManager.getAffairBySubObjectId(currentWorkitemId);

			} else {
				currentAffair = affairManager.get(affairId);
			}
			if (summaryId==null) {
				_summaryId = currentAffair.getObjectId();
			} else {
				_summaryId = summaryId;
			}
			if (Strings.isNotBlank(repealComment)) {
				repealComment = repealComment.replaceAll(new String(new char[] { (char) 160 }), " ");
			}
			List<Integer> states = new ArrayList<Integer>();
			List<Integer> _states = new ArrayList<Integer>();
			states.add(StateEnum.col_pending.key());
			states.add(StateEnum.col_done.key());
			states.add(StateEnum.col_cancel.key());
			_states.addAll(states);
			FlipInfo flipInfo = new FlipInfo();
			flipInfo.setSize(99999);
			List<CtpAffair> affairs = affairManager.getAffairsByObjectIdAndStates(flipInfo,_summaryId, states);
			//对于指定回退由onWorkitemCanceled方法内执行，这里过滤掉
			if (Strings.isNotEmpty(affairs)) {
				List<CtpAffair> _affairs = new ArrayList<CtpAffair>();
				for (CtpAffair affair : affairs) {
					if (_states.contains(affair.getState())) {
						CtpAffair cloneBean = (CtpAffair) org.apache.commons.beanutils.BeanUtils.cloneBean(affair);
						cloneBean.setObjectId(_summaryId);
						_affairs.add(cloneBean);
					}
				}
				AffairData affairData = new AffairData();
				affairData.setAffairList(_affairs);
				affairData.setIsSendMessage(true);
				EdocSummary summary = null;		
				try {
					summary = govdocSummaryManager.getSummaryById(_summaryId);
				} catch (Exception e) {
					log.error("",e);
				}
				EdocSummary cloneSummary = (EdocSummary) org.apache.commons.beanutils.BeanUtils.cloneBean(summary);
				ColOperation operation=ColOperation.STEPBACK;
				if(_operationType==EdocWorkflowEventListener.CANCEL){
					operation=ColOperation.REVOKED;
				}
				if(currentAffair.getApp()==ApplicationCategoryEnum.edoc.getKey()){
					this.handCollaborationAffair(affairData, operation,cloneSummary,repealComment);
				}
				
			}
		} catch (Exception e) {
			log.error("",e);
		}
	}

	@Override
	public void onWorkitemTakeBack(EventDataContext context) {
		try {
			CtpAffair affair = null;
			String resource = Global.getConfig("sysCode");
			if (null != context.getBusinessData(GovdocWorkflowEventListener.CTPAFFAIR_CONSTANT)) {
				affair = (CtpAffair) context.getBusinessData(GovdocWorkflowEventListener.CTPAFFAIR_CONSTANT);
			} else {
				WorkItem workitem = context.getWorkItem();
				affair = affairManager.getAffairBySubObjectId(workitem.getId());
			}
			if (affair != null) {
				AffairData affairData = (AffairData) context.getBusinessData(EventDataContext.CTP_AFFAIR_DATA);
				if (affairData == null) {
					affairData = new AffairData();
				}
				AffairData cloneAffairData = (AffairData) org.apache.commons.beanutils.BeanUtils.cloneBean(affairData);
				
				List<CtpAffair> list = affairData.getAffairList();
				if (Strings.isEmpty(list)) {
					list = new UniqueList<CtpAffair>();
				}
				V3xOrgMember dMember = orgManager.getMemberById(affair.getMemberId());
				if (OrgUtil.isPlatformEntity(dMember)) {
					CtpAffair cloneBean = (CtpAffair) org.apache.commons.beanutils.BeanUtils.cloneBean(affair);
					list.add(cloneBean);
				}else if(organizationManager!=null&&hasMutiPlugin){
					List<String> objectList = new ArrayList<String>();
					objectList.add(dMember.getId().toString());
					Map<String, List<OrgMember>> mappingMember = organizationManager.getMemberByObjectIds(objectList,resource);
					List<OrgMember> members = mappingMember.get(dMember.getId().toString());
					if(members != null && members.size() > 1){
						CtpAffair cloneBean = (CtpAffair) org.apache.commons.beanutils.BeanUtils.cloneBean(affair);
						Map<String, Object> affairExtMap = AffairUtil.getExtProperty(cloneBean);
						affairExtMap.put(MemberId_Of_OCIP_Client, cloneBean.getMemberId());
						cloneBean.setMemberId(Long.valueOf(members.get(0).getOrgPlatformUserId()));
						AffairUtil.setExtProperty(cloneBean, affairExtMap);
						list.add(cloneBean);
					}
				}
				if(list.isEmpty()){
					return;
				}
				EdocSummary summary = null;
				EdocSummary cloneSummary = null;
				Object summaryObj = context.getAppObject();
				if (summaryObj != null) {
					summary = (EdocSummary) summaryObj;
				} else {
					try {
						summary = govdocSummaryManager.getSummaryById(affair.getObjectId());
					} catch (Exception e) {
						log.error("",e);
					}
				}
	            if(summary == null && context.getProcessId()!=null){
	            	summary = govdocSummaryManager.getSummaryByProcessId(context.getProcessId());
	            }
				cloneAffairData.setAffairList(list);
			    cloneSummary = (EdocSummary) org.apache.commons.beanutils.BeanUtils.cloneBean(summary);
				if (affair.getApp()==ApplicationCategoryEnum.edoc.getKey()){
					this.handCollaborationAffair(cloneAffairData, ColOperation.TAKEBACK,cloneSummary);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void onWorkitemCanceled(EventDataContext context) {
		try {
			User user = AppContext.getCurrentUser();
			Object _operationType =  (Object)context.getBusinessData().get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE) ;
            Integer operationType = null;
            if(_operationType != null){
            	operationType = (Integer)_operationType;
            }
			V3xOrgMember cMember = orgManager.getMemberById(user.getId());
			List<String> objectList = new ArrayList<String>();
			objectList.add(user.getId().toString());
			String resource = Global.getConfig("sysCode");
			Map<String, List<OrgMember>> mappingMember = new HashMap<String, List<OrgMember>>();
			if (organizationManager != null && hasMutiPlugin) {
				mappingMember = organizationManager.getMemberByObjectIds(objectList, resource);
			}
			if (operationType != GovdocWorkflowEventListener.TAKE_BACK && !OrgUtil.isPlatformEntity(cMember)
					&& mappingMember.get(user.getId().toString()).isEmpty())
				return;
			// 是否为竞争执行
			this.delTakeBack(operationType, context);

		} catch (BusinessException e) {
			log.error(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			log.error(e.getMessage(), e);
		} catch (InstantiationException e) {
			log.error(e.getMessage(), e);
		} catch (InvocationTargetException e) {
			log.error(e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return;
		
	}
	private boolean delTakeBack(int operationType, EventDataContext context) throws Exception {
		boolean isCompetition = (operationType == EdocWorkflowEventListener.COMMONDISPOSAL
				|| operationType == EdocWorkflowEventListener.ZCDB
				|| operationType == EdocWorkflowEventListener.AUTOSKIP
				|| operationType == EdocWorkflowEventListener.SPECIAL_BACK_SUBMITTO)
				&& "competition".equals(context.getProcessMode());
		// 退回||取回||指定回退：流程重走
		boolean isBack = operationType == EdocWorkflowEventListener.WITHDRAW
				|| operationType == EdocWorkflowEventListener.TAKE_BACK
				|| operationType == EdocWorkflowEventListener.SPECIAL_BACK_RERUN;
		if (isCompetition || isBack) {

			List<WorkItem> workitemLists = context.getWorkitemLists();
			List<CtpAffair> affairs = new UniqueList<CtpAffair>();
			List<String> objectList = new ArrayList<String>();
			Map<String, List<CtpAffair>> localAffair = new HashMap<String, List<CtpAffair>>();
			for (WorkItem item : workitemLists) {
				CtpAffair affair = affairManager.getAffairBySubObjectId(item.getId());
				if (affair != null) {
					V3xOrgMember dMember = orgManager.getMemberById(affair.getMemberId());
					if (OrgUtil.isPlatformEntity(dMember)) {
						CtpAffair cloneBean = (CtpAffair) org.apache.commons.beanutils.BeanUtils.cloneBean(affair);
						cloneBean.setSubObjectId(item.getId());
						affairs.add(cloneBean);
						// 竞争执行需要把执行人传过去，暂时用senderId传
						if (isCompetition) {
							cloneBean.setSenderId(Long.valueOf(context.getCurrentUserId()));
						}
					} else {

						CtpAffair cloneBean = (CtpAffair) org.apache.commons.beanutils.BeanUtils.cloneBean(affair);
						cloneBean.setSubObjectId(item.getId());
						if (isCompetition) {
							cloneBean.setSenderId(Long.valueOf(context.getCurrentUserId()));
						}
						if (localAffair.containsKey(dMember.getId().toString())) {
							localAffair.get(dMember.getId().toString()).add(cloneBean);
						} else {
							List<CtpAffair> list = new ArrayList<CtpAffair>();
							list.add(cloneBean);
							localAffair.put(dMember.getId().toString(), list);
						}
						objectList.add(dMember.getId().toString());
					}
				}
			}
			if (!objectList.isEmpty() && organizationManager != null && hasMutiPlugin) {
				String resource = Global.getConfig("sysCode");
				Map<String, List<OrgMember>> mappingMember = organizationManager.getMemberByObjectIds(objectList,
						resource);
				for (String objectId : objectList) {
					List<OrgMember> members = mappingMember.get(objectId);
					if (members != null && members.size() > 1) {
						for (CtpAffair cloneBean : localAffair.get(objectId)) {
							Map<String, Object> affairExtMap = AffairUtil.getExtProperty(cloneBean);
							affairExtMap.put(MemberId_Of_OCIP_Client, cloneBean.getMemberId());
							AffairUtil.setExtProperty(cloneBean, affairExtMap);
							cloneBean.setMemberId(Long.valueOf(members.get(0).getOrgPlatformUserId()));
							affairs.add(cloneBean);
						}
					}
				}
			}
			// 没有跨系统数据，直接返回
			if (affairs.isEmpty()) {
				return true;
			}
			AffairData affairData = (AffairData) context.getBusinessData(EventDataContext.CTP_AFFAIR_DATA);
			if (affairData == null) {
				affairData = new AffairData();
				affairData.setIsSendMessage(true);
			}
			AffairData cloneAffairData = (AffairData) org.apache.commons.beanutils.BeanUtils.cloneBean(affairData);

			cloneAffairData.setAffairList(affairs);
			EdocSummary summary = null;
			Object summaryObj = context.getAppObject();
			if (summaryObj != null) {
				summary = (EdocSummary) summaryObj;
			} else {
				try {
					summary = govdocSummaryManager.getSummaryById(affairs.get(0).getObjectId());
				} catch (Exception e) {
					log.error("",e);
				}
			}
			// 复制colSummary对象
			EdocSummary cloneSummary = (EdocSummary) org.apache.commons.beanutils.BeanUtils.cloneBean(summary);
			ColOperation colExchangeOperation = null;
			if (isBack) {
				switch (operationType) {
				case 1:// 退回
				case 100:// 指定回退，流程重走
					Map<String, Object> businessData = affairData.getBusinessData();
					Comment comment = new Comment();
					ParamUtil.getJsonDomainToBean("comment_deal", comment);
					Map parm = ParamUtil.getJsonDomain("comment_deal");
					if(parm.get("content_coll")!= null){
						String content_coll=(String)parm.get("content_coll");
						comment.setContent(content_coll);
			        }
					colExchangeOperation = ColOperation.STEPBACK;
					break;
				case 2:// 取回
					colExchangeOperation = ColOperation.TAKEBACK;
					break;
				}
			} else if (isCompetition) {
				cloneAffairData.setIsSendMessage(true);
				colExchangeOperation = ColOperation.COMPETE;
			}
			if (colExchangeOperation != null && summary != null) {
				this.handCollaborationAffair(cloneAffairData, colExchangeOperation, cloneSummary);

			}
		}
		return true;
	}

}
