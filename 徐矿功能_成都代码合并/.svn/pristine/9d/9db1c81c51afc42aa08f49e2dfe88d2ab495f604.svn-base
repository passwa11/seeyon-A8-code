package com.seeyon.v3x.exchange.manager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.AffairManager;
import com.seeyon.ctp.common.content.affair.AffairUtil;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.organization.bo.MemberPost;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.v3x.common.web.login.CurrentUser;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocObjTeam;
import com.seeyon.v3x.edoc.domain.EdocObjTeamMember;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.enums.EdocMessageFilterParamEnum;
import com.seeyon.v3x.edoc.manager.EdocObjTeamManager;
import com.seeyon.v3x.edoc.manager.EdocRoleHelper;
import com.seeyon.v3x.edoc.manager.EdocSummaryManager;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.exchange.dao.EdocSendDetailDao;
import com.seeyon.v3x.exchange.dao.EdocSendRecordDao;
import com.seeyon.v3x.exchange.dao.EdocSendRecordReferenceDao;
import com.seeyon.v3x.exchange.domain.EdocExchangeTurnRec;
import com.seeyon.v3x.exchange.domain.EdocSendDetail;
import com.seeyon.v3x.exchange.domain.EdocSendRecord;
import com.seeyon.v3x.exchange.domain.EdocSendRecordReference;
import com.seeyon.v3x.exchange.domain.ExchangeAccount;
import com.seeyon.v3x.exchange.exception.ExchangeException;
import com.seeyon.v3x.exchange.util.Constants;
import com.seeyon.v3x.exchange.util.ExchangeUtil;
import com.seeyon.ctp.util.Strings;


public class SendEdocManagerImpl implements SendEdocManager {
	private final static Log LOGGER = LogFactory.getLog(SendEdocManagerImpl.class);
	private EdocSendRecordDao edocSendRecordDao;
	private EdocSendDetailDao edocSendDetailDao;
	private EdocObjTeamManager edocObjTeamManager;
	private EdocSummaryManager edocSummaryManager;
	private EdocSendRecordReferenceDao edocSendRecordReferenceDao;
	private OrgManager orgManager;
	private AffairManager affairManager;
	private UserMessageManager userMessageManager;
	private AppLogManager appLogManager;
	private ExchangeAccountManager exchangeAccountManager;
	
	public void setExchangeAccountManager(ExchangeAccountManager exchangeAccountManager) {
		this.exchangeAccountManager = exchangeAccountManager;
	}

	public void setEdocSendRecordReferenceDao(EdocSendRecordReferenceDao edocSendRecordReferenceDao) {
		this.edocSendRecordReferenceDao = edocSendRecordReferenceDao;
	}

	public void setAppLogManager(AppLogManager appLogManager) {
		this.appLogManager = appLogManager;
	}

	public void setEdocSummaryManager(EdocSummaryManager edocSummaryManager) {
		this.edocSummaryManager = edocSummaryManager;
	}
	public UserMessageManager getUserMessageManager() {
		return userMessageManager;
	}

	public void setUserMessageManager(UserMessageManager userMessageManager) {
		this.userMessageManager = userMessageManager;
	}

	public AffairManager getAffairManager() {
		return affairManager;
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	public EdocSendRecordDao getEdocSendRecordDao() {
		return edocSendRecordDao;
	}
	
	public void setEdocSendRecordDao(EdocSendRecordDao edocSendRecordDao) {
		this.edocSendRecordDao = edocSendRecordDao;
	}
	
	public EdocSendDetailDao getEdocSendDetailDao() {
		return edocSendDetailDao;
	}
	
	public void setEdocSendDetailDao(EdocSendDetailDao edocSendDetailDao) {
		this.edocSendDetailDao = edocSendDetailDao;
	}
		
	public void add(){
		
	}
	
	/**
	 * 撤销交换时，创建新的发文记录
	 * @param edocSendRecord
	 * @throws BusinessException
	 */
	public void create(EdocSendRecord edocSendRecord) throws BusinessException {
		edocSendDetailDao.save(edocSendRecord);
	}
	
	public void create(EdocSummary edocSummary, long exchangeOrgId,
			int exchangeType, String edocMangerID,CtpAffair _affair,boolean isTurnRec) throws Exception {
		
		User user =AppContext.getCurrentUser();
		String userName = "";
		if(null!=user){
			userName = user.getName();
		}
		String key = "exchange.send"; 
		
		boolean bool_isunion = edocSummary.getIsunit(); //判断是否为联合发文
		
		EdocSendRecord edocSendRecord = new EdocSendRecord();
		if(isTurnRec){
			edocSendRecord.setIsTurnRec(EdocExchangeTurnRec.TURN_REC_TYPE);
		}else{
			edocSendRecord.setIsTurnRec(EdocExchangeTurnRec.NOT_TURN_REC_TYPE);
		}
		EdocSendRecord r_second = null;
		
		if(bool_isunion){
			r_second = new EdocSendRecord();
			r_second.setIdIfNew();
			r_second.setDocMark(edocSummary.getDocMark2());
			r_second.setSubject(edocSummary.getSubjectB());
			r_second.setDocType(edocSummary.getDocType());
			r_second.setSecretLevel(edocSummary.getSecretLevel());
			r_second.setUrgentLevel(edocSummary.getUrgentLevel());
			
			if(isTurnRec){
				r_second.setIsTurnRec(EdocExchangeTurnRec.TURN_REC_TYPE);
			}else{
				r_second.setIsTurnRec(EdocExchangeTurnRec.NOT_TURN_REC_TYPE);
			}
		}
		
		
		
		edocSendRecord.setIdIfNew();
		edocSendRecord.setSubject(bool_isunion?edocSummary.getSubjectA():edocSummary.getSubject());
		edocSendRecord.setDocType(edocSummary.getDocType());
		edocSendRecord.setDocMark(edocSummary.getDocMark());
		edocSendRecord.setSecretLevel(edocSummary.getSecretLevel());
		edocSendRecord.setUrgentLevel(edocSummary.getUrgentLevel());
		
		String sendUnit=edocSummary.getSendUnit();
		String sendUnit_second = null;
		
		if(sendUnit==null)
		{
			V3xOrgAccount curAcc= orgManager.getAccountByLoginName(user.getLoginName());
			sendUnit=curAcc.getName();
		}		
		edocSendRecord.setSendUnit(sendUnit);
		edocSendRecord.setIssuer(edocSummary.getIssuer());
		edocSendRecord.setIssueDate(edocSummary.getSigningDate());
		edocSendRecord.setCopies(edocSummary.getCopies());		
		edocSendRecord.setEdocId(edocSummary.getId());
		
		Long tempExchangeOrgId = exchangeOrgId;
		if(isTurnRec && exchangeType == EdocSendRecord.Exchange_Send_iExchangeType_Org){
			//交换单位一定要是 公文所在单位
		    tempExchangeOrgId = edocSummary.getOrgAccountId();
		}
		edocSendRecord.setExchangeOrgId(tempExchangeOrgId);
		
		if(exchangeType == EdocSendRecord.Exchange_Send_iExchangeType_Org){
            //交换单位一定要是 公文所在单位
            edocSendRecord.setExchangeAccountId(tempExchangeOrgId);
        }else{
            V3xOrgDepartment dept = orgManager.getDepartmentById(tempExchangeOrgId);
            if(dept != null) {
                edocSendRecord.setExchangeAccountId(dept.getOrgAccountId());
            } 
        }
		
		edocSendRecord.setExchangeType(exchangeType);
		long l = System.currentTimeMillis();
		edocSendRecord.setCreateTime(new Timestamp(l));
		edocSendRecord.setStatus(EdocSendRecord.Exchange_iStatus_Tosend);
		
		
		if(bool_isunion){
			sendUnit_second = edocSummary.getSendUnit2();
			if(Strings.isBlank(sendUnit_second)) {
				V3xOrgAccount curAcc= orgManager.getAccountByLoginName(user.getLoginName());
				sendUnit_second=curAcc.getName();				
			}
    		r_second.setSendUnit(sendUnit_second);
    		r_second.setIssuer(edocSummary.getIssuer());
    		r_second.setIssueDate(edocSummary.getSigningDate());
    		r_second.setCopies(edocSummary.getCopies2());		
    		r_second.setEdocId(edocSummary.getId());
    		r_second.setExchangeOrgId(exchangeOrgId);
    		
    		if(exchangeType == EdocSendRecord.Exchange_Send_iExchangeType_Org){
                //交换单位一定要是 公文所在单位
    		    r_second.setExchangeAccountId(exchangeOrgId);
            }else{
                V3xOrgDepartment dept = orgManager.getDepartmentById(exchangeOrgId);
                if(dept != null) {
                    r_second.setExchangeAccountId(dept.getOrgAccountId());
                } 
            }
    		r_second.setExchangeType(exchangeType);
    		long l_s = System.currentTimeMillis();
    		r_second.setCreateTime(new Timestamp(l_s));
    		r_second.setStatus(EdocSendRecord.Exchange_iStatus_Tosend);
		}
		
		
		//todo:产生details记录
		//get a list from edocSummary,it contains rec id,type
		//use list to generate sendDetail records
		//except the rec is not internal org or dept,...
		
		//List<V3xOrgMember> memberList = new ArrayList<V3xOrgMember>();
		Set<V3xOrgMember> memberSet = new HashSet<V3xOrgMember>();
		edocSendRecord.setAssignType(EdocSendRecord.Exchange_Assign_To_All);
		edocSendRecord.setIsBase(EdocSendRecord.Exchange_Base_YES);//封发表示：原发文
		
		if(exchangeType == EdocSendRecord.Exchange_Send_iExchangeType_Org){
			memberSet = getOrgExchangeMembers(edocSummary.getOrgAccountId(),edocMangerID);
				
			if(Strings.isNotBlank(edocMangerID)) {
				edocSendRecord.setSendUserId(Long.parseLong(edocMangerID));
				edocSendRecord.setAssignType(EdocSendRecord.Exchange_Assign_To_Member);
			}
		} else if(exchangeType == EdocSendRecord.Exchange_Send_iExchangeType_Dept){
			if(Strings.isNotBlank(edocMangerID)){//xiangfan 添加条件判断 给所指定的具体的某个部门的收发员 修复GOV-4911
				memberSet = getDeptExchangeMembers(Long.parseLong(edocMangerID));
			}else {
				memberSet = getDeptExchangeMembers(edocSummary);
			}
		}
		if(bool_isunion) {
			r_second.setAssignType(edocSendRecord.getAssignType());
			r_second.setIsBase(edocSendRecord.getIsBase());
			r_second.setSendUserId(edocSendRecord.getSendUserId());
		}
				//--
		for(V3xOrgMember m:memberSet){
			CtpAffair affair = new CtpAffair();
			affair.setIdIfNew();
			affair.setApp(ApplicationCategoryEnum.exSend.getKey());
			affair.setSubject(edocSummary.getSubject());
			
			if(bool_isunion){affair.setSubject(edocSummary.getSubjectA());}
			if(edocSummary.getUrgentLevel() !=null && !"".equals(edocSummary.getUrgentLevel())){
				affair.setImportantLevel(Integer.valueOf(edocSummary.getUrgentLevel())) ;
			}
			affair.setCreateDate(new Timestamp(System.currentTimeMillis()));
			affair.setReceiveTime(new Timestamp(System.currentTimeMillis()));
			affair.setUpdateDate(new Timestamp(System.currentTimeMillis()));
			affair.setMemberId(m.getId());
			affair.setSenderId(_affair.getMemberId());
			affair.setObjectId(edocSummary.getId());
			affair.setSubObjectId(edocSendRecord.getId());
	        affair.setState(StateEnum.edoc_exchange_send.key());
	        affair.setSubState(SubStateEnum.col_pending_unRead.key());//OA-39223待办栏目统计图，办理状态图，待登记的被统计在待办中，但是通过点击图，却筛选不出来。。
	        affair.setTrack(null);	
			//首页栏目的扩展字段设置--公文文号、发文单位等--start
			Map<String, Object> extParam=EdocUtil.createExtParam(edocSummary);
			if(null != extParam)  AffairUtil.setExtProperty(affair, extParam);
			//首页栏目的扩展字段设置--公文文号、发文单位等--end
	        affairManager.save(affair);
	        
	        //消息
	        MessageReceiver receiver = new MessageReceiver(affair.getId(), affair.getMemberId(),"message.link.exchange.send", affair.getSubObjectId().toString(),affair.getId());
	        Long agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(), m.getId());
	        MessageReceiver agentReceiver =  null;
	        if(agentMemberId!=null){
        		agentReceiver = new MessageReceiver(affair.getId(), agentMemberId,"message.link.exchange.send", affair.getSubObjectId().toString(),affair.getId());
	        }
	        if(bool_isunion){
	        	if(agentMemberId!=null){
	        		MessageContent content = new MessageContent(key,affair.getSubject(),userName).add("col.agent");
	        		if (null != affair.getTempleteId()) {
	    				content.setTemplateId(affair.getTempleteId());
	    			}
	        		userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.exSend, user.getId(), agentReceiver,EdocMessageFilterParamEnum.exchange.key);	
	        	}
	        	MessageContent content1 = new MessageContent(key,affair.getSubject(),userName);
	        	if (null != affair.getTempleteId()) {
    				content1.setTemplateId(affair.getTempleteId());
    			}
	        	userMessageManager.sendSystemMessage(content1, ApplicationCategoryEnum.exSend, user.getId(), receiver,EdocMessageFilterParamEnum.exchange.key);
	        }else{
	        	if(_affair.getMemberId().intValue()!=user.getId()){
	        		userName = orgManager.getMemberById(_affair.getMemberId()).getName();
	        	}
	        	if(agentMemberId!=null){
	        		MessageContent content = new MessageContent(key,affair.getSubject(),userName).add("col.agent");
	        		if (null != affair.getTempleteId()) {
	    				content.setTemplateId(affair.getTempleteId());
	    			}
	        		userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.exSend, user.getId(), agentReceiver,EdocMessageFilterParamEnum.exchange.key);
	        	}
	        	MessageContent content1 = new MessageContent(key,affair.getSubject(),userName);
	        	if (null != affair.getTempleteId()) {
    				content1.setTemplateId(affair.getTempleteId());
    			}
	        	userMessageManager.sendSystemMessage(content1, ApplicationCategoryEnum.exSend, user.getId(), receiver,EdocMessageFilterParamEnum.exchange.key);		        	
	        }
	        //================
	        
	        if(bool_isunion){
				CtpAffair affair_second = new CtpAffair();
				affair_second.setIdIfNew();
				affair_second.setApp(ApplicationCategoryEnum.exSend.getKey());
				affair_second.setSubject(edocSummary.getSubjectB());
				affair_second.setCreateDate(new Timestamp(System.currentTimeMillis()));
				affair_second.setReceiveTime(new Timestamp(System.currentTimeMillis()));
				affair_second.setUpdateDate(new Timestamp(System.currentTimeMillis()));
				affair_second.setMemberId(m.getId());
				affair_second.setSenderId(user.getId());
				affair_second.setObjectId(edocSummary.getId());
				affair_second.setSubObjectId(r_second.getId());
		        affair_second.setState(StateEnum.edoc_exchange_send.key());
		        affair_second.setSubState(SubStateEnum.col_normal.key());//OA-39223待办栏目统计图，办理状态图，待登记的被统计在待办中，但是通过点击图，却筛选不出来。。
		        affair_second.setTrack(null);	
				if(edocSummary.getUrgentLevel() !=null && !"".equals(edocSummary.getUrgentLevel())){
					affair.setImportantLevel(Integer.valueOf(edocSummary.getUrgentLevel())) ;
				}
				//首页栏目的扩展字段设置--公文文号、发文单位等--start
				Map<String, Object> extParam_2=EdocUtil.createExtParam(edocSummary.getDocMark2(), edocSummary.getSendUnit2(),edocSummary.getSendUnitId2());
				if(null != extParam_2)  AffairUtil.setExtProperty(affair_second, extParam_2);
				//首页栏目的扩展字段设置--公文文号、发文单位等--end
		        affairManager.save(affair_second);
		        MessageReceiver receiver_second = new MessageReceiver(affair_second.getId(), affair_second.getMemberId(),"message.link.exchange.send", affair_second.getSubObjectId().toString(),affair_second.getId());
		        MessageContent content = new MessageContent(key,affair_second.getSubject(),userName);
		        if (null != affair_second.getTempleteId()) {
    				content.setTemplateId(affair_second.getTempleteId());
    			}
		        userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.exSend, user.getId(), receiver_second,EdocMessageFilterParamEnum.exchange.key);
		        if(agentMemberId!=null){
		        	MessageContent content1 = new MessageContent(key,affair_second.getSubject(),userName).add("col.agent");
		        	if (null != affair_second.getTempleteId()) {
	    				content1.setTemplateId(affair_second.getTempleteId());
	    			}
		        	userMessageManager.sendSystemMessage(content1, ApplicationCategoryEnum.exSend, user.getId(), agentReceiver,EdocMessageFilterParamEnum.exchange.key);
		        }
	        }
	    }
		
		edocSendRecord.setContentNo(Constants.EDOC_EXCHANGE_UNION_NORMAL);
		if(edocSummary.getBody(EdocBody.EDOC_BODY_PDF_ONE)!=null){
			edocSendRecord.setContentNo(Constants.EDOC_EXCHANGE_UNION_PDF_FIRST);
		}
		if(bool_isunion){
			//第一套正文
			if(edocSummary.getBody(Constants.EDOC_EXCHANGE_UNION_PDF_FIRST)!=null){
				edocSendRecord.setContentNo(Constants.EDOC_EXCHANGE_UNION_PDF_FIRST);
			}else{
				edocSendRecord.setContentNo(Constants.EDOC_EXCHANGE_UNION_FIRST);
			}
			//第二套正文
			if(edocSummary.getBody(Constants.EDOC_EXCHANGE_UNION_PDF_SECOND)!=null){
				r_second.setContentNo(Constants.EDOC_EXCHANGE_UNION_PDF_SECOND);
			}else{
				r_second.setContentNo(Constants.EDOC_EXCHANGE_UNION_SECOND);
			}
			edocSendRecordDao.save(r_second);
		}
		edocSendRecordDao.save(edocSendRecord);

	}
	/**
	 * 得到指定单位的部门收发员
	 * @param edocSummary
	 * @return
	 * @throws Exception
	 */
	private Set<V3xOrgMember> getDeptExchangeMembers(EdocSummary edocSummary)
			throws Exception {
		//根据edocSummary中的发起人id,查找组织机构中,发起人所在<部门>的公文管理员
		long creatorId = edocSummary.getStartUserId();
		//使用Set过滤重复的人员,如果一个人既在主部门充当收发员，又在副岗所在部门充当收发员，首页只显示一条待办事项。
		Set<V3xOrgMember> memberSet =new HashSet<V3xOrgMember>();
		try{
			//发起人在主单位和兼职单位获取公文收发员的方式有所不同。
			V3xOrgMember startMember=orgManager.getMemberById(creatorId);
			if(startMember.getOrgAccountId().longValue()!=edocSummary.getOrgAccountId()){
				//1、查找兼职部门的公文收发员
				Map<Long, List<MemberPost>> map=orgManager.getConcurentPostsByMemberId(edocSummary.getOrgAccountId(),creatorId);
				Set<Long> concurentDepartSet=map.keySet();
				for(Long deptId:concurentDepartSet){
					//V3xOrgMember member = orgManager.getMemberById(creatorId);
					memberSet.addAll(EdocRoleHelper.getDepartMentExchangeUsers(edocSummary.getOrgAccountId(), deptId));
				}
			}else{
				//2.查找发起人副岗所在部门的公文收发员
				List<MemberPost> list=startMember.getSecond_post();
				for(MemberPost memberPost:list){
					if(memberPost.getOrgAccountId().equals(edocSummary.getOrgAccountId())){
						memberSet.addAll(EdocRoleHelper.getDepartMentExchangeUsers(edocSummary.getOrgAccountId(), memberPost.getDepId()));
					}
				}
				//3。主部门的公文收发员。
				memberSet.addAll(EdocRoleHelper.getDepartMentExchangeUsers(edocSummary.getOrgAccountId(), startMember.getOrgDepartmentId()));
			}
			
		}catch(Exception e){
			LOGGER.error("公文交换查找部门收发员出错：", e);
			throw e;
		}
		return memberSet;
	}
	
	/**
	 * 得到指定部门的部门收发员 --向凡添加重载上面的方法 修复GOV-4911
	 * @param deptId
	 * @return
	 * @throws Exception
	 */
	private Set<V3xOrgMember> getDeptExchangeMembers(long deptId) throws Exception {
		//使用Set过滤重复的人员,如果一个人既在主部门充当收发员，又在副岗所在部门充当收发员，首页只显示一条待办事项。
		Set<V3xOrgMember> memberSet =new HashSet<V3xOrgMember>();
		memberSet.addAll(EdocRoleHelper.getDepartMentExchangeUsers(orgManager.getDepartmentById(deptId).getOrgAccountId(), deptId));
		return memberSet;
	}
	
	/**
	 * 得到指定单位的公文收发员。
	 * @param orgAccountId ： 单位ID
	 * @param selectMemberId ：选择的特定的公文收发员.
	 * @return
	 */
	private Set<V3xOrgMember> getOrgExchangeMembers(long orgAccountId,String selectMemberId){
			Set<V3xOrgMember> memberList = new HashSet<V3xOrgMember>();
			try{
				if (Strings.isBlank(selectMemberId)) {
					// 没有选中特定的公文收发员，沿用发给全部的公文收发员，竞争执行
					memberList.addAll( EdocRoleHelper.getAccountExchangeUsers(orgAccountId));
				} else {
					// 选中了特定的公文收发员，只发给特定的公文收发员
					V3xOrgMember member = orgManager.getMemberById(Long.parseLong(selectMemberId));
					memberList.add(member);
				}
			}catch(Exception e){
				LOGGER.error("得到指定单位的公文收发员抛出异常",e);
			}
			return memberList;
	}
	/**
	 * 再次发送
	 * @param edocSendRecord
	 * @param edocSummary
	 * @param exchangeOrgId
	 * @param exchangeType
	 * @throws Exception
	 */
	public void reSend(EdocSendRecord edocSendRecord, EdocSummary edocSummary) throws Exception {
		
		String sendUnit=edocSendRecord.getSendUnit();
		User user = AppContext.getCurrentUser();
		if(sendUnit==null)
		{
			V3xOrgAccount curAcc= orgManager.getAccountByLoginName(user.getLoginName());
			sendUnit=curAcc.getName();
		}		
		edocSendRecord.setSendUnit(sendUnit);
		edocSendRecordDao.save(edocSendRecord);

	}
	
	/**
	 * 撤销交换时，创建新的发文记录详细信息
	 * @param detail
	 * @throws BusinessException
	 */
	public void createSendRecordDetail(EdocSendDetail detail) throws BusinessException {
		edocSendDetailDao.save(detail);	
	}
	
	/**
	 * 更新发文记录详细信息
	 * @param detail
	 * @throws BusinessException
	 */
	public void updateSendRecordDetail(EdocSendDetail detail) throws BusinessException {
		edocSendDetailDao.update(detail);
	}
	
	/**
	 * 获取发文记录详细信息
	 * @param detail
	 * @throws BusinessException
	 */
	public EdocSendDetail getSendRecordDetail(long detailId) throws BusinessException {
		return edocSendDetailDao.get(detailId);
	}
	
	/**
	 * 生成待发送公文要发送的详细信息
	 * @param sendRecordId
	 * @param typeAndIds
	 * @param sendToUnitNames 
	 */
	public List<EdocSendDetail> createSendRecord(Long sendRecordId,String typeAndIds,String sendToUnitNames) throws ExchangeException{
		List<EdocSendDetail> sdList=new ArrayList<EdocSendDetail>();
		//只填写了手动输入送往单位，也需要更新数据
		if(typeAndIds==null || "".equals(typeAndIds)){
			if(null != sendRecordId && Strings.isNotBlank(sendToUnitNames)){
				EdocSendRecord esr= edocSendRecordDao.get(sendRecordId);
				if(null != esr){
					esr.setSendedNames(sendToUnitNames);
					edocSendRecordDao.update(esr);
				}
			}
			return sdList;
		}
		try {	
			boolean haveAnotherAccount = false; //为了和选人页面的形式保持一致,定义变量区分发送的部门/单位中是否含有外单位
			String[] items = typeAndIds.split(V3xOrgEntity.ORG_ID_DELIMITER);
			String data[]=null;
			
			String strTemp;
			//对机构组进行处理
			List <String>strLs=new ArrayList<String>();
			for(String temp:items) {
				data = temp.split("[|]");
				if(EdocObjTeam.ENTITY_TYPE_OrgTeam.equals(data[0])) {
					EdocObjTeam et=edocObjTeamManager.getById(Long.parseLong(data[1]));
					Set <EdocObjTeamMember>mems=et.getEdocObjTeamMembers();
					for(EdocObjTeamMember mem:mems) {
						strTemp=mem.getTeamType()+"|"+mem.getMemberId();
						if(strLs.contains(strTemp)==false){strLs.add(strTemp);}
					}
				} else {
					if(strLs.contains(temp)==false){strLs.add(temp);}
				}				
			}
			items=new String[strLs.size()];
			strLs.toArray(items);
			
			haveAnotherAccount = checkHaveAnotherAccount(items); 
			EdocSendRecord es= edocSendRecordDao.get(sendRecordId);
			
			Map<String,Integer> numMap = new HashMap<String,Integer>();
			if(null!=es && es.getIsBase() == EdocSendRecord.Exchange_Base_NO){
			    EdocSendRecordReference ref = edocSendRecordReferenceDao.findReferenceByNewId(es.getId());
			    if(ref != null){
			    	//对应的原来的公文交换 已发送数据
	                EdocSendRecord source = edocSendRecordDao.get(ref.getReferenceSendRecodId());
	                List<EdocSendDetail> sourceDetails = this.getDetailBySendId(ref.getReferenceSendRecodId());
	                
	                //原来的公文交换 已发送明细 催办次数Map映射
	                for(EdocSendDetail detail : sourceDetails){
	                    numMap.put(detail.getRecOrgId(), detail.getCuibanNum());
	                }
	                edocSendRecordReferenceDao.delete(ref.getId());
			    }
			}
			
			for(String item:items) {	
				String shortName = "";
				V3xOrgAccount account = null;
				EdocSendDetail detail = new EdocSendDetail();
				data = item.split("[|]");
				V3xOrgEntity tempEntity=null;
				if(V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equals(data[0])) {
					detail.setRecOrgType(EdocSendDetail.Exchange_SendDetail_iAccountType_Org);	
					tempEntity = orgManager.getEntity(item);
				} else if(V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equals(data[0])) {
					detail.setRecOrgType(EdocSendDetail.Exchange_SendDetail_iAccountType_Dept);
					tempEntity = orgManager.getEntity(item);
				} else {//交换中心外部单位暂时不做处理
					//detail.setRecOrgType(EdocSendDetail.Exchange_SendDetail_iAccountType_Default);
				}
				
				if(tempEntity == null){
				    continue;
				}
				
				account = orgManager.getAccountById(tempEntity.getOrgAccountId());
				
				if(account != null && Strings.isNotBlank(account.getShortName())){
					shortName = "("+account.getShortName()+")"; //将单位的简称拼接成该形式 '(xxx)'
				}
				
				detail.setIdIfNew();
				if(haveAnotherAccount && (!V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equals(data[0]))){//如果有外单位/部门,全部加上单位简称,反之都不加
					detail.setRecOrgName(tempEntity.getName() + shortName);					
				}else{
					detail.setRecOrgName(tempEntity.getName());							
				}
				detail.setRecOrgId(data[1]);
				detail.setSendRecordId(sendRecordId);
				detail.setSendType(Constants.C_iStatus_Send);
				detail.setStatus(EdocSendDetail.Exchange_iStatus_SendDetail_Torecieve);
				if(numMap.get(detail.getRecOrgId()) != null){
				    detail.setCuibanNum(numMap.get(detail.getRecOrgId()));
				}else{
				    detail.setCuibanNum(0);
				}
				
				edocSendDetailDao.save(detail);	
				sdList.add(detail);
			}	
			
			if(null!=es){
				// 如果是回退的公文再次发送，设置此公文的发文记录的状态为未发送
				if (ExchangeUtil.isEdocExchangeSentStepBacked(es.getStatus())) {
					es.setStatus(EdocSendRecord.Exchange_iStatus_Tosend);
					// 删除原来的收文回退时留下的detail
					List<EdocSendDetail> oldEdocSendDetails = edocSendDetailDao
							.findDetailListBySendId(sendRecordId);
					String[] columns = {"id"};
					List<Long> values = new ArrayList<Long>();
					for (EdocSendDetail edocSendDetail : oldEdocSendDetails) {
						values.add(edocSendDetail.getId());
					}
					edocSendDetailDao.delete(columns, values.toArray());
				}
				es.setSendedTypeIds(typeAndIds);
				if (Strings.isNotBlank(sendToUnitNames)) {
					es.setSendedNames(sendToUnitNames);
				}
				edocSendRecordDao.update(es);
			}
		} catch(Exception e) {
			LOGGER.error("修改公文的送往单位报错!",e);
		}
		return sdList;
	}
	
	/**
	 * 生成待发送公文要发送的详细信息
	 * @param sendRecordId
	 * @param typeAndIds
	 */
	public List<EdocSendDetail> createSendRecord(Long sendRecordId,String typeAndIds) throws ExchangeException {
		return  createSendRecord(sendRecordId, typeAndIds,null);
	}
	
	public void update(EdocSendRecord edocSendRecord) throws BusinessException {
		edocSendRecordDao.update(edocSendRecord);
	}
		
	public EdocSendRecord getEdocSendRecord(long id) {
		return edocSendRecordDao.get(id);
	}
	
	public EdocSendRecord getEdocSendRecordByEdocId(long edocId){
		return edocSendRecordDao.getEdocSendRecordByEdocId(edocId);
	}
	
	/**
	 * 是否能够修改已办的公文,ajax调用
	 * @param edocId
	 * @return
	 */
	public String isCanModifyDoneSummay(long edocId){
		String msg = "no";
		List list = edocSendRecordDao.findEdocSendRecordsByEdocId(edocId);
		if(Strings.isEmpty(list)){
			msg = "ok";
		}else{
			EdocSendRecord send =  edocSendRecordDao.getEdocSendRecordByEdocId(edocId,EdocSendRecord.Exchange_iStatus_Tosend);
			if(send != null){
				msg = "ok";
			}
		}
		return msg;
	}
	
	
	public List<EdocSendRecord> getEdocSendRecordOnlyByEdocId(long edocId){
		return edocSendRecordDao.getEdocSendRecordOnlyByEdocId(edocId);
	}
	
	public EdocSendRecord getEdocSendRecordById(long id){
		return edocSendRecordDao.get(id);
	}
	
	/**
	 * ajax判断发文记录中是否包含指定的单位.
	 */
	public String ajaxCheckContainSpecialUnit(Long sendRecordId,String unitIds){
		if(sendRecordId == null || Strings.isBlank(unitIds)) return "N";
		
		StringBuilder info = new StringBuilder("");
		try{
			EdocSendRecord record = getEdocSendRecord(sendRecordId);
			String[] ids = unitIds.split("[,]");
			Map<Long,String> m  = new HashMap<Long,String>();
			for(EdocSendDetail detail : record.getSendDetails()){
				if(detail.getStatus() != EdocSendDetail.Exchange_iStatus_SendDetail_StepBacked
				        && detail.getStatus() != EdocSendDetail.Exchange_iStatus_SendDetail_Cancel)
					m.put(Long.valueOf(detail.getRecOrgId()),detail.getRecOrgName());
			}
			Set<Long>  s = m.keySet();
			for(String id : ids){
				Long i = Long.valueOf(id.split("[|]")[1]);
				if(s.contains(i)){
					if(info.length() > 0){
					    info.append(",");
					} 
					info.append(m.get(i));
				}
			}
		}catch(Exception e){
			LOGGER.error("ajax判断发文记录中是否包含指定的单位抛出异常",e);
		}
		String ret = info.toString();
		return "".equals(ret)?"N":ret;
	}
	public List<EdocSendRecord> getEdocSendRecords(int status) {
		
		List<EdocSendRecord> list = edocSendRecordDao.getEdocSendRecords(status);
		
		 return list;
	}
	
	public List<EdocSendRecord> findEdocSendRecords(String accountIds,String departIds,int status,String condition,String value) {
		return edocSendRecordDao.findEdocSendRecords(accountIds, departIds, status,condition,value);
	}	
	
	/**
	 * 读取当前用户的（待发送或已发送）公文列表。
	 * @param userId 当前用户id
	 * @param orgId 当前用户登录单位id
	 * @param status 状态（待发送或已发送）
	 * @param condition 查询条件
	 * @param value  查询值
	 * @param dateType 时间类型（本年，本月，上个月等）
	 * @return List
	 */
	public List<EdocSendRecord> findEdocSendRecords(String accountIds,String departIds,int status,String condition,String value,int dateType,long userId){
		return edocSendRecordDao.findEdocSendRecords(accountIds, departIds, status,condition,value,dateType,userId);
	}
	
	/**
	 * 删除公文交换数据: 逻辑删除，将删除状态置为5
	 * @param id
	 * @throws Exception
	 */
	public void delete(long id) throws Exception {	
//		edocSendDetailDao.delete("from EdocSendDetail where sendRecordId = " + id);
//		Session s = edocSendDetailDao.getSessionFactory().getCurrentSession();
//		s.flush();
//		Session s2 = edocSendRecordDao.getSessionFactory().getCurrentSession();
		//GOV-4760.【公文管理】-【发文管理】，发文分发人在已分发列表里删除分发的公文，收文签收人签收时无法退回该公文  start
		EdocSendRecord edocSendRecord = edocSendRecordDao.get(id);
		edocSendRecord.setStatus(EdocSendRecord.Exchange_iStatus_Send_Delete);    
		edocSendDetailDao.update(edocSendRecord);
		//edocSendRecordDao.delete(id);
		//GOV-4760.【公文管理】-【发文管理】，发文分发人在已分发列表里删除分发的公文，收文签收人签收时无法退回该公文  start
	}
	
	public void deleteByPhysical(long id) throws Exception {	
		edocSendRecordDao.delete(id);
	}
	/**
	 * 删除公文交换数据: 逻辑删除，将删除状态置为deleteState(deleteState的值域是5,6)
	 * @param id
	 * @param deleteState
	 * @throws Exception
	 */
	public void delete(long id, int deleteState) throws Exception {	
		EdocSendRecord edocSendRecord = edocSendRecordDao.get(id);
		edocSendRecord.setStatus(deleteState);
		edocSendDetailDao.update(edocSendRecord);
	}
	
	public OrgManager getOrgManager() {
		return orgManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}	
	
	private boolean checkHaveAnotherAccount(String[] items)throws Exception{
		User user = AppContext.getCurrentUser();
		boolean haveAnotherAccount = false; //为了和选人页面的形式保持一致,定义变量区分发送的部门/单位中是否含有外单位
		try
		{	
			V3xOrgEntity tempEntity=null;			
			String data[]=null;
			for(String item:items)
			{	
				V3xOrgAccount account = null;
				data = item.split("[|]");
				if(V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equals(data[0])
						|| V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equals(data[0]))
				{
					tempEntity = orgManager.getEntity(item);
				}
				else
				{//交换中心外部单位暂时不做处理
					//detail.setRecOrgType(EdocSendDetail.Exchange_SendDetail_iAccountType_Default);
					tempEntity=null;
				}
				if(tempEntity==null){
				    continue;
				}
				
				account = orgManager.getAccountById(tempEntity.getOrgAccountId());
				if(null!=account){
					if(null!=account.getId() && account.getId().longValue() != user.getAccountId()){
						haveAnotherAccount = true;  //如果实体的单位ID与当前交换人的ID不属于同一个单位,生效
					}
				}
			}		
		}catch(Exception e)
		{
			throw e;
		}		
		return haveAnotherAccount;
	}
	
	public EdocSendRecord getEdocSendRecordByDetailId(long detailId)
	{		
		EdocSendDetail esd=edocSendDetailDao.get(detailId);
		if(esd==null){return null;}
		if(null==esd.getSendRecordId())
		{
			return null;
		}
		EdocSendRecord er=edocSendRecordDao.get(esd.getSendRecordId());		
		return er;
	}
	
	public void deleteRecordDetailById(long id)throws Exception{
		edocSendDetailDao.delete(id);
	}

	public EdocObjTeamManager getEdocObjTeamManager() {
		return edocObjTeamManager;
	}

	public void setEdocObjTeamManager(EdocObjTeamManager edocObjTeamManager) {
		this.edocObjTeamManager = edocObjTeamManager;
	}
	
	public List<EdocSendDetail> getDetailBySendId(long sendId){
		return edocSendDetailDao.findDetailListBySendId(sendId);
	}

	@Override
	public void update(Map<String, Object> columns, Object[][] where)
			throws BusinessException {
		edocSendRecordDao.update(EdocSendRecord.class, columns, where);
	}
	
	/********************************************公文交换列表 start*************************************************************/
	/**
	 * 公文发送列表
	 * @param type 3：待发送列表 4已发送列表
	 * @param condition
	 * @return
	 * @throws BusinessException
	 */
	public List<EdocSendRecord> findEdocSendRecordList(int type, Map<String, Object> condition) throws BusinessException {
		List<EdocSendRecord> list = edocSendRecordDao.findEdocSendRecordList(type, condition);
		if(list != null) {
			for(EdocSendRecord r : list) {
				//存放交接人
				long undertakerId = r.getSendUserId();
				V3xOrgMember member = orgManager.getMemberById(undertakerId);
				if(null!=member){
					r.setSendUserNames(member.getName());
				}else{
					r.setSendUserNames("");
				}
				//送往单位
				r.setSendNames(getSendNames(r));
			}
		}
		return list;
	}
	/**
	 * 公文发送列表
	 * @param type 3：待发送列表 4已发送列表
	 * @param condition
	 * @return
	 * @throws BusinessException
	 */
	public int findEdocSendRecordCount(int type, Map<String, Object> condition) throws BusinessException {
		int listCount = edocSendRecordDao.findEdocSendRecordCount(type, condition);
		return listCount;
	}
	
	/**
	 * 
	 * @param r
	 * @return
	 * @throws BusinessException
	 */
	private String getSendNames(EdocSendRecord r) throws BusinessException {
		StringBuilder sendNames = new StringBuilder("");
		
		if(Strings.isNotBlank(r.getSendedTypeIds())) {
			String[] typeAndIds = r.getSendedTypeIds().split(",");
			for(int i=0; i<typeAndIds.length; i++) {
			    String tempName = null;
				String[] types = typeAndIds[i].split("[|]");
				if("Account".equals(types[0])) {
					long accountId = Long.parseLong(types[1]);
					V3xOrgAccount account = orgManager.getAccountById(accountId);
					if(account != null) {
					    tempName = account.getName();
					} 
				} else if("Department".equals(types[0])) {
					long deptId = Long.parseLong(types[1]);
					V3xOrgDepartment dept = orgManager.getDepartmentById(deptId);
					if(dept != null) {
					    tempName = dept.getName();
					}
				} else if("ExchangeAccount".equals(types[0])) {
					long accountId = Long.parseLong(types[1]);
					ExchangeAccount acc = exchangeAccountManager.getExchangeAccount(accountId);
					if(acc != null){
					    tempName = acc.getName();
					}
				}else if("OrgTeam".equals(types[0])) {
					long uId = Long.parseLong(types[1]);
					EdocObjTeam team = edocObjTeamManager.getById(uId);
					if(team != null){
					    tempName = team.getName();
					}
				}
				//客开：徐矿集团【将人员ID转化为名字】 chenqiang 2019年4月15日  start
				else if("Member".equals(types[0])) {
					long uId = Long.parseLong(types[1]);
					V3xOrgMember v3xOrgMember = orgManager.getMemberById(uId);
					if(v3xOrgMember != null){
					    tempName = v3xOrgMember.getName();
					}
				}
				//客开：徐矿集团【将人员ID转化为名字】 chenqiang 2019年4月15日  end
				else {
				    tempName = types[1];
				}
				
				if(tempName != null){
				    if(sendNames.length() > 0){
				        sendNames.append("、");
				    }
				    sendNames.append(tempName);
				}
			}
			r.setSendNames(sendNames.toString());
		} else {
			EdocSummary summary = edocSummaryManager.findById(r.getEdocId());
			if(summary != null){
				Integer currentNo = r.getContentNo();
				if(null!=currentNo && (currentNo.intValue() == Constants.EDOC_EXCHANGE_UNION_FIRST || currentNo.intValue() == Constants.EDOC_EXCHANGE_UNION_NORMAL
					|| currentNo.intValue() == Constants.EDOC_EXCHANGE_UNION_PDF_FIRST)){
					r.setSendNames(summary.getSendTo());
					sendNames.append(Strings.escapeNULL(summary.getSendTo(), ""));
				}else if(null!=currentNo && currentNo.intValue() == Constants.EDOC_EXCHANGE_UNION_SECOND){
					r.setSendNames(summary.getSendTo2());
					sendNames.append(Strings.escapeNULL(summary.getSendTo2(), ""));
				}
			}
		}
		return sendNames.toString();
	}
	
	/**
	 * 公文交换列表逻辑-发送-批量删除
	 * @param map
	 * @param type
	 * @throws BusinessException
	 */
	public void deleteEdocSendRecordByLogic(String listType, Map<Long, String> map) throws BusinessException {
		User user = CurrentUser.get();
		//1 逻辑删除交换数据
		int state = Integer.parseInt(EdocNavigationEnum.EdocV5ListTypeEnum.getStateName(listType));
		int status = (state==3) ? EdocSendRecord.Exchange_iStatus_ToSend_Delete : EdocSendRecord.Exchange_iStatus_Send_Delete;
		edocSendRecordDao.deleteEdocSendRecordByLogic(map.keySet().toArray(), status);
		EdocSendRecord edocSendRecord = null;
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("delete", Boolean.TRUE);
		String userName = AppContext.getCurrentUser().getName();
		Iterator<Entry<Long, String>> it = map.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Long, String> obj = (Entry<Long, String>)it.next();
			edocSendRecord = edocSendRecordDao.get(obj.getKey());
			if(edocSendRecord == null) continue;
			//2 写交换删除日志
			AppLogAction action = AppLogAction.Edoc_PreSend_Record_Del;
			if(edocSendRecord.getStatus() == EdocSendRecord.Exchange_iStatus_Send_Delete) {//已发送中被删除
				action = AppLogAction.Edoc_Sended_Record_Del;
			}
			appLogManager.insertLog(user, action, userName, edocSendRecord.getSubject());
			//3 同步首页栏目
			Object[][] wheres = new Object[3][2];
			wheres[0][0] = "app";
			wheres[0][1] = ApplicationCategoryEnum.exSend.key();
			wheres[1][0] = "objectId";
			wheres[1][1] = edocSendRecord.getEdocId();
			wheres[2][0] = "subObjectId";
			wheres[2][1] = edocSendRecord.getId();
			affairManager.update(paramMap, wheres);
			//4 发删除消息
			if(edocSendRecord.getStatus() != EdocSendRecord.Exchange_iStatus_Send_New_Cancel) {
				List<V3xOrgMember> memberList = null;
				if(edocSendRecord.getAssignType() == EdocSendRecord.Exchange_Assign_To_All) {
					if(edocSendRecord.getExchangeType()==EdocSendRecord.Exchange_Send_iExchangeType_Dept) {//部门
						V3xOrgDepartment dept = orgManager.getDepartmentById(edocSendRecord.getExchangeOrgId());
						if(dept != null) {
							memberList = EdocRoleHelper.getDepartMentExchangeUsers(dept.getOrgAccountId(), dept.getId());
						}
					} else if(edocSendRecord.getExchangeType()==EdocSendRecord.Exchange_Send_iExchangeType_Org) {//单位
						memberList = EdocRoleHelper.getAccountExchangeUsers(edocSendRecord.getExchangeOrgId());
					}
				}
				if(Strings.isNotEmpty(memberList)) {
					List<MessageReceiver> receiverList = new ArrayList<MessageReceiver>();
					for(int i=0; i<memberList.size(); i++) {
						if(memberList.get(i).getId().longValue() != obj.getKey()) {
							if(memberList.get(i).getId().longValue() != user.getId().longValue()) {//交换删除不给自己发消息
								MessageReceiver receiver = new MessageReceiver(obj.getKey(), memberList.get(i).getId());
								receiverList.add(receiver);
							}
						}
					}
					//根据status来区分待发送和已发送列表
					MessageContent content = new MessageContent("edoc.exchange.send.list.delete."+status, user.getName(), edocSendRecord.getSubject());
					userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.edoc, user.getId(), receiverList,EdocMessageFilterParamEnum.exchange.key);
				}
			}
		}	
	}
	/********************************************公文交换列表       end*************************************************************/

	@Override
	public List<EdocSendRecord> getEdocSendRecordByEdocIdAndOrgIdAndStatus(
			long edocId, long exchangeOrgId, int status) {
		return edocSendRecordDao.getEdocSendRecordByEdocIdAndOrgIdAndStatus(edocId, exchangeOrgId, status);
	}
	
	@Override
	public List<EdocSendRecord> getEdocSendRecordByOrgIdAndStatus(long exchangeOrgId, int status) {
		List<EdocSendRecord> list = edocSendRecordDao.getEdocSendRecordByOrgIdAndStatus(exchangeOrgId, status);
		return list;
	}
}