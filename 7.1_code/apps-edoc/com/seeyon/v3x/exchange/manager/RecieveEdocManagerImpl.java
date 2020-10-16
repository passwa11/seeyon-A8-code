package com.seeyon.v3x.exchange.manager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.govdoc.constant.GovdocEnum.OldExchangeNodePolicyEnum;
import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.helper.GovdocRoleHelper;
import com.seeyon.apps.govdoc.manager.GovdocPishiManager;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.web.login.CurrentUser;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.enums.EdocMessageFilterParamEnum;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocListManagerImpl;
import com.seeyon.v3x.edoc.manager.EdocRoleHelper;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.exchange.dao.EdocRecieveRecordDao;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.domain.EdocSendRecord;
import com.seeyon.v3x.exchange.util.Constants;

public class RecieveEdocManagerImpl implements RecieveEdocManager {
	
	private final static Log LOGGER = LogFactory.getLog(EdocListManagerImpl.class);
	private EdocRecieveRecordDao edocRecieveRecordDao;	
	private UserMessageManager userMessageManager = null;
	private AffairManager affairManager;
	private OrgManager orgManager;
	private AppLogManager appLogManager;
	private GovdocPishiManager govdocPishiManager;
	
	public OrgManager getOrgManager() {
		return orgManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public void setUserMessageManager(UserMessageManager userMessageManager) {
        this.userMessageManager = userMessageManager;
    }
	
	public UserMessageManager getUserMessageManager()
	{
		return this.userMessageManager;
	}
	
	public EdocRecieveRecordDao getEdocRecievedRecordDao() {
		return edocRecieveRecordDao;
	}
	
	public void setEdocRecieveRecordDao(EdocRecieveRecordDao edocRecieveRecordDao) {
		this.edocRecieveRecordDao = edocRecieveRecordDao;
	}	

	public void setGovdocPishiManager(GovdocPishiManager govdocPishiManager) {
		this.govdocPishiManager = govdocPishiManager;
	}

	public void setAppLogManager(AppLogManager appLogManager) {
		this.appLogManager = appLogManager;
	}

	public void create(EdocSendRecord edocSendRecord,
			long exchangeOrgId,
			int exchangeType,
			Object replyId,
			String[] aRecUnit,EdocSummary edocSummary) throws Exception {
		
		User user = AppContext.getCurrentUser();
		String userName = "";
		if(null!=user){
			userName = user.getName();
		}
		String key = "exchange.sign";
		EdocRecieveRecord edocRecieveRecord = new EdocRecieveRecord();
		/*****************************************************************/
        //2部门公文收发员发送ExchangeOrgId表示部门id |1单位公文收发员发送ExchangeOrgId表示单位id
        //默认为发送者机构
        long orgAccountId = edocSendRecord.getExchangeOrgId();
        V3xOrgDepartment dept = null;
        if(edocSendRecord.getExchangeType() == 0) {
            dept = orgManager.getDepartmentById(edocSendRecord.getExchangeOrgId());
            if(dept!=null) {
                orgAccountId = dept.getOrgAccountId();
            }
        }
        Integer sendUnitType = getSendUnitInfo(orgAccountId, exchangeType, Long.valueOf(exchangeOrgId));
        edocRecieveRecord.setSendUnitType(sendUnitType);
        /*****************************************************************/
		edocRecieveRecord.setIdIfNew();
		edocRecieveRecord.setSubject(edocSendRecord.getSubject());
		edocRecieveRecord.setDocType(edocSendRecord.getDocType());
		edocRecieveRecord.setDocMark(edocSendRecord.getDocMark());
		edocRecieveRecord.setSecretLevel(edocSendRecord.getSecretLevel());
		edocRecieveRecord.setUrgentLevel(edocSendRecord.getUrgentLevel());
		edocRecieveRecord.setSendUnit(edocSendRecord.getSendUnit());
		edocRecieveRecord.setIssuer(edocSendRecord.getIssuer());
		edocRecieveRecord.setIssueDate(edocSendRecord.getIssueDate());
		edocRecieveRecord.setSendTo(aRecUnit[0]);
		edocRecieveRecord.setCopyTo(aRecUnit[1]);
		edocRecieveRecord.setReportTo(aRecUnit[2]);
		edocRecieveRecord.setReplyId(String.valueOf(replyId));
		edocRecieveRecord.setEdocId(edocSendRecord.getEdocId());
		edocRecieveRecord.setExchangeOrgId(exchangeOrgId);
		edocRecieveRecord.setExchangeType(exchangeType);		
		edocRecieveRecord.setFromInternal(true);
		if(replyId instanceof String)
		{
			edocRecieveRecord.setFromInternal(false);
		}
		long l = System.currentTimeMillis();
		edocRecieveRecord.setCreateTime(new Timestamp(l));
		edocRecieveRecord.setStatus(EdocRecieveRecord.Exchange_iStatus_Torecieve);
		edocRecieveRecord.setContentNo(edocSendRecord.getContentNo());
		edocRecieveRecordDao.save(edocRecieveRecord);
		
		
		List<V3xOrgMember> member = GovdocRoleHelper.getDepartMentExchangeUsers(user != null ? user.getLoginAccount() : null, Long.valueOf(exchangeOrgId));
		CtpAffair affair = null;
		for(V3xOrgMember m:member){
			affair = new CtpAffair();
			affair.setIdIfNew();
			affair.setApp(ApplicationCategoryEnum.edoc.getKey());
			affair.setSubApp(ApplicationSubCategoryEnum.old_exSign.getKey());
			affair.setNodePolicy(OldExchangeNodePolicyEnum.oldqianshou.name());
			affair.setBodyType(String.valueOf(GovdocUtil.getContentType(edocSummary.getBodyType())));
			affair.setCreateDate(new Timestamp(System.currentTimeMillis()));
			affair.setReceiveTime(new Timestamp(System.currentTimeMillis()));
			affair.setSubject(edocSummary.getSubject());
			affair.setMemberId(m.getId());
			affair.setFinish(false);
			affair.setObjectId(edocSummary.getId());
			affair.setSubObjectId(edocRecieveRecord.getId());
			if(user != null){				
				affair.setSenderId(user.getId());
			}
			affair.setState(StateEnum.edoc_exchange_receive.key());
			affair.setSubState(SubStateEnum.col_pending_unRead.key());//OA-39223待办栏目统计图，办理状态图，待登记的被统计在待办中，但是通过点击图，却筛选不出来
			//首页栏目的扩展字段设置--公文文号、发文单位等--start
			Map<String, Object> extParam=EdocUtil.createExtParam(edocSendRecord.getDocMark(),edocSendRecord.getSendUnit(),edocSummary.getSendUnitId());
			if(null != extParam)  AffairUtil.setExtProperty(affair, extParam);
			//首页栏目的扩展字段设置--公文文号、发文单位等--end
			
			affairManager.save(affair);
			MessageReceiver receiver_a = new MessageReceiver(affair.getId(), affair.getMemberId(),"message.link.exchange.receive", affair.getSubObjectId().toString());
	        userMessageManager.sendSystemMessage(new MessageContent(key,affair.getSubject(),userName), ApplicationCategoryEnum.exSign, user.getId(), receiver_a,EdocMessageFilterParamEnum.exchange.key);	
			}
	}

	public void update(EdocRecieveRecord edocRecieveRecord) throws BusinessException {
		edocRecieveRecordDao.update(edocRecieveRecord);
	}
	
	public EdocRecieveRecord getEdocRecieveRecord(long id) {
		return edocRecieveRecordDao.get(id);
	}
	
	public EdocRecieveRecord getEdocRecieveRecordByEdocId(long edocId){
		return edocRecieveRecordDao.getByEdocId(edocId);
	}

	public List<EdocRecieveRecord> getEdocRecieveRecords(int status) {
		return edocRecieveRecordDao.getEdocRecieveRecords(status);
	}
	
	/**
	 * 查询签收列表 (唐桂林-20110924)
	 * @param accountId 签收单位
	 * @param departIds 所有部门id
	 * @param statusSet 
	 * @param condition 查询条件
	 * @param value 查询值
	 * @param listType 查询列表类型
	 * @return 签收列表
	 */
	public List<EdocRecieveRecord> findEdocRecieveRecords(String accountIds, String departIds, Set<Integer> statusSet, String condition, String[] value, int listType, int dateType, Map<String, Object> conditionParams) {
		List<EdocRecieveRecord> records = new ArrayList<EdocRecieveRecord>();
		getEdocAgentInfo(conditionParams);
		List<EdocRecieveRecord> list = edocRecieveRecordDao.findEdocRecieveRecords(accountIds, departIds, statusSet, condition, value, listType, dateType, conditionParams);
		for(EdocRecieveRecord record:list){
			List<AgentModel> edocAgent = new ArrayList<AgentModel>();
    		if(conditionParams.get("edocAgent") != null) {
    			edocAgent = (List<AgentModel>)conditionParams.get("edocAgent");
    		}
    		boolean agentToFlag = false;
    		boolean agentFlag = false;
			java.util.Date early = null;
			if(conditionParams.get("agentFlag") != null) {
    			agentFlag = (Boolean)conditionParams.get("agentFlag");
    		}
			if(conditionParams.get("agentToFlag") != null) {
    			agentToFlag = (Boolean)conditionParams.get("agentToFlag");
    		}
			if(edocAgent != null && !edocAgent.isEmpty()) {
    			early = edocAgent.get(0).getStartDate();
    		}
			Long registerUserId = record.getRegisterUserId();
			if(agentFlag && registerUserId!=null && registerUserId!=0 
					&& (registerUserId!=((Long)conditionParams.get("userId")).longValue())) {
				Long proxyMemberId = registerUserId;
				try {
					V3xOrgMember member = orgManager.getMemberById(proxyMemberId);
					if(member != null) {
						record.setProxyName(member.getName());
						record.setProxy(true);
					}
				} catch (BusinessException e) {
					LOGGER.error("", e);
				}
			} else if(agentToFlag && early != null && record.getRecTime()!=null && early.before(record.getRecTime())) {
				record.setProxy(true);
			}
			records.add(record);
		}
		return records;
	}
	public List<EdocRecieveRecord> findEdocRecieveRecords(int status, Map<String, Object> condition) {
		return edocRecieveRecordDao.findWaitEdocRegisterList(status, condition);
	}
	
	public List<EdocRecieveRecord> getWaitRegisterEdocRecieveRecords(Long userId) {
		return edocRecieveRecordDao.getToRegisterEdocs(userId);		
	}
		
	public void delete(long id) throws Exception {
		EdocRecieveRecord edocRecieveRecord =  edocRecieveRecordDao.get(id);
		edocRecieveRecord.setStatus(EdocRecieveRecord.Exchange_iStatus_Receive_Delete);    
		edocRecieveRecordDao.update(edocRecieveRecord);
		//edocRecieveRecordDao.delete(id);
	}
	public void create(EdocSendRecord edocSendRecord,
			long exchangeOrgId,
			int exchangeType,
			Object replyId,
			String[] aRecUnit,String sender,EdocSummary edocSummary) throws Exception {
		
			create(edocSendRecord, exchangeOrgId, exchangeType, replyId, aRecUnit,sender,null, edocSummary);
	
	}
	public String create(EdocSendRecord edocSendRecord,
			long exchangeOrgId,
			int exchangeType,
			Object replyId,
			String[] aRecUnit,String sender,Long agentToId,EdocSummary edocSummary) throws Exception {
		
		String agentToName = "";//被代理人姓名
		if(agentToId != null){
			V3xOrgMember member = orgManager.getMemberById(agentToId);
			agentToName = member.getName();
		}
		User user = AppContext.getCurrentUser();
		String userName = "";
		if(null!=user){
			userName = user.getName();
		}
		String key = "exchange.sign";
		String pishiFlag = govdocPishiManager.checkLeaderPishi(user.getId(), agentToId);
		
		EdocRecieveRecord edocRecieveRecord = new EdocRecieveRecord();
		/*****************************************************************/
        //2部门公文收发员发送ExchangeOrgId表示部门id |1单位公文收发员发送ExchangeOrgId表示单位id
        //默认为发送者机构
        long orgAccountId = edocSendRecord.getExchangeOrgId();
        V3xOrgDepartment dept = null;
        if(edocSendRecord.getExchangeType() == 0) {
            dept = orgManager.getDepartmentById(edocSendRecord.getExchangeOrgId());
            if(dept!=null) {
                orgAccountId = dept.getOrgAccountId();
            }
        }
        Integer sendUnitType = getSendUnitInfo(orgAccountId, exchangeType, Long.valueOf(exchangeOrgId));
        edocRecieveRecord.setSendUnitType(sendUnitType);
        /*****************************************************************/
		edocRecieveRecord.setIdIfNew();
		edocRecieveRecord.setSender(Strings.isBlank(agentToName)?sender:agentToName);
		edocRecieveRecord.setSubject(edocSummary.getSubject());
		edocRecieveRecord.setDocType(edocSendRecord.getDocType());
		edocRecieveRecord.setDocMark(edocSendRecord.getDocMark());
		edocRecieveRecord.setSecretLevel(edocSendRecord.getSecretLevel());
		edocRecieveRecord.setUrgentLevel(edocSendRecord.getUrgentLevel());
		edocRecieveRecord.setSendUnit(edocSendRecord.getSendUnit());
		edocRecieveRecord.setIssuer(edocSendRecord.getIssuer());
		edocRecieveRecord.setIssueDate(edocSendRecord.getIssueDate());
		edocRecieveRecord.setSendTo(aRecUnit[0]);
		edocRecieveRecord.setCopyTo(aRecUnit[1]);
		edocRecieveRecord.setReportTo(aRecUnit[2]);
		edocRecieveRecord.setReplyId(String.valueOf(replyId));
		edocRecieveRecord.setEdocId(edocSendRecord.getEdocId());
		edocRecieveRecord.setExchangeOrgId(exchangeOrgId);
		edocRecieveRecord.setExchangeType(exchangeType);		
		edocRecieveRecord.setFromInternal(true);
		//是否为转收文类型
		edocRecieveRecord.setIsTurnRec(edocSendRecord.getIsTurnRec());
		if(replyId instanceof String)
		{
			edocRecieveRecord.setFromInternal(false);
		}
		edocRecieveRecord.setContentNo(edocSendRecord.getContentNo());
		long l = System.currentTimeMillis();
		edocRecieveRecord.setCreateTime(new Timestamp(l));
		edocRecieveRecord.setStatus(EdocRecieveRecord.Exchange_iStatus_Torecieve);
		edocRecieveRecordDao.save(edocRecieveRecord);
		
		V3xOrgEntity entity = null;
		List<V3xOrgMember> member = null;
		if(exchangeType == EdocRecieveRecord.Exchange_Receive_iAccountType_Dept) {
			entity=orgManager.getGlobalEntity(V3xOrgEntity.ORGENT_TYPE_DEPARTMENT, exchangeOrgId);
			member = GovdocRoleHelper.getDepartMentExchangeUsers(entity.getOrgAccountId(), Long.valueOf(exchangeOrgId));			
		}else if(exchangeType == EdocRecieveRecord.Exchange_Receive_iAccountType_Org){
			//entity.getOrgAccountId() 和 exchangeOrgId不相等。导致查找单位角色的时候报错。直接取单位ID 29673
			member = GovdocRoleHelper.getAccountExchangeUsers(exchangeOrgId);
		}
		List<CtpAffair> affairList = new ArrayList<CtpAffair>();
		CtpAffair affair = null;
		for(V3xOrgMember m:member){
			affair = new CtpAffair();
			affair.setIdIfNew();
			affair.setApp(ApplicationCategoryEnum.edoc.getKey());
			affair.setSubApp(ApplicationSubCategoryEnum.old_exSign.getKey());
			affair.setNodePolicy(OldExchangeNodePolicyEnum.oldqianshou.name());
			affair.setBodyType(String.valueOf(GovdocUtil.getContentType(edocSummary.getBodyType())));
			affair.setCreateDate(new Timestamp(System.currentTimeMillis()));
			affair.setReceiveTime(new Timestamp(System.currentTimeMillis()));
			affair.setSubject(edocSummary.getSubject());
			affair.setMemberId(m.getId());
			affair.setFinish(false);
			affair.setObjectId(edocSummary.getId());
			affair.setSubObjectId(edocRecieveRecord.getId());
			if("pishi".equals(pishiFlag)){
				affair.setSenderId(agentToId);
			}else{
				affair.setSenderId(user.getId());
			}
			affair.setState(StateEnum.edoc_exchange_receive.key());
			affair.setSubState(SubStateEnum.col_pending_unRead.key());//OA-39223待办栏目统计图，办理状态图，待登记的被统计在待办中，但是通过点击图，却筛选不出来
			if(edocSummary.getUrgentLevel()!=null && !"".equals(edocSummary.getUrgentLevel()))
				affair.setImportantLevel(Integer.parseInt(edocSummary.getUrgentLevel()));
			if(user.getId()==-1)
			{
				affair.setExtProps(sender);
			}
			
			//首页栏目的扩展字段设置--公文文号、发文单位等--start
			Map<String, Object> extParam=EdocUtil.createExtParam(edocSendRecord.getDocMark(),edocSendRecord.getSendUnit(),edocSummary.getSendUnitId());
			if(null != extParam)  AffairUtil.setExtProperty(affair, extParam);
			//首页栏目的扩展字段设置--公文文号、发文单位等--end
			
			affairList.add(affair);
			
			MessageReceiver receiver_a = new MessageReceiver(affair.getId(), affair.getMemberId(),"message.link.exchange.receive", affair.getSubObjectId().toString());
			Long agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(),m.getId());
			MessageReceiver agentReceiver  = null;
			if(agentMemberId!=null){
				agentReceiver = new MessageReceiver(affair.getId(), agentMemberId,"message.link.exchange.receive", affair.getSubObjectId().toString());
			}
			if(agentToId !=null){//当前处理人是代理人
				if(agentMemberId!=null && !"pishi".equals(pishiFlag)){//当前查找出来的人还设置了代理人
					userMessageManager.sendSystemMessage(new MessageContent(key,affair.getSubject(),agentToName).add("edoc.agent.deal", user.getName()).add("col.agent"), ApplicationCategoryEnum.exSign, agentToId, agentReceiver,EdocMessageFilterParamEnum.exchange.key);
				}else{
					userMessageManager.sendSystemMessage(new MessageContent(key,affair.getSubject(),agentToName).add("edoc.pishi.deal", user.getName()).add("col.pishi"), ApplicationCategoryEnum.exSign, agentToId, agentReceiver,EdocMessageFilterParamEnum.exchange.key);
				}
				if("pishi".equals(pishiFlag)){
		        	userMessageManager.sendSystemMessage(new MessageContent(key,affair.getSubject(),agentToName).add("edoc.pishi.deal", user.getName()), ApplicationCategoryEnum.exSign, agentToId, receiver_a,EdocMessageFilterParamEnum.exchange.key);

				}else{
		        	userMessageManager.sendSystemMessage(new MessageContent(key,affair.getSubject(),agentToName).add("edoc.agent.deal", user.getName()), ApplicationCategoryEnum.exSign, agentToId, receiver_a,EdocMessageFilterParamEnum.exchange.key);

				}
	        }else{
	        	if(agentMemberId!=null && !"pishi".equals(pishiFlag)){//当前查找出来的人还设置了代理人
	        		userMessageManager.sendSystemMessage(new MessageContent(key,affair.getSubject(),userName).add("col.agent"), ApplicationCategoryEnum.exSign, user.getId(), agentReceiver,EdocMessageFilterParamEnum.exchange.key);
	        	}else{
	        		userMessageManager.sendSystemMessage(new MessageContent(key,affair.getSubject(),userName).add("col.pishi"), ApplicationCategoryEnum.exSign, user.getId(), agentReceiver,EdocMessageFilterParamEnum.exchange.key);
	        	}
	        	userMessageManager.sendSystemMessage(new MessageContent(key,affair.getSubject(),userName), ApplicationCategoryEnum.exSign, user.getId(), receiver_a,EdocMessageFilterParamEnum.exchange.key);
			}
		}
		if(Strings.isNotEmpty(affairList)) {
			affairManager.saveAffairs(affairList);
		}
		return String.valueOf(edocRecieveRecord.getId());
	}
	
	public Boolean registerRecieveEdoc(Long id,int state) throws Exception 
	{
		EdocRecieveRecord rec=getEdocRecieveRecord(id);
		rec.setRegisterTime(new Timestamp(System.currentTimeMillis()));
		rec.setStatus(EdocRecieveRecord.Exchange_iStatus_Registered);
		edocRecieveRecordDao.update(rec);
		
		Map<String, Object> columns=new Hashtable<String, Object>();
		if(state == 1){//登记保存待发，不修改待办事项状态，还是在待办里
			/*columns.put("state",Integer.valueOf(StateEnum.col_waitSend.getKey()));
			columns.put("subState",Integer.valueOf(SubStateEnum.col_waitSend_draft.getKey()));*/
		}else{
			columns.put("state",Integer.valueOf(StateEnum.col_done.getKey()));
			affairManager.update(columns, new Object[][]{{"objectId",Long.valueOf(rec.getEdocId())}, {"subObjectId",rec.getId()}});	
		}
		
		return true; 
	}
	
	
	
	public Boolean registerRecieveEdoc(Long id) throws Exception 
	{
		return registerRecieveEdoc(id,4);
	}
	
	public void delete(EdocRecieveRecord o)throws Exception{
		edocRecieveRecordDao.deleteObject(o);
	}

	public void deleteRecRecordByReplayId(long replayId)throws Exception{
		edocRecieveRecordDao.deleteReceiveRecordByReplayId(replayId);
	}

	public EdocRecieveRecord getReceiveRecordByReplyId(long replyId)throws Exception{
		return edocRecieveRecordDao.getRecRecordByReplayId(replyId);
	}	
	
	public AffairManager getAffairManager() {
		return affairManager;
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	
	/**
	 * 唐桂林添加发送公文类型
	 * @param orgAccountId 机关id
	 * @param recOrgType 机类类型
	 * @param recOrgId 本单位id
	 * @return
	 */
	public int getSendUnitInfo(long orgAccountId, int recOrgType, long recOrgId) {
	    int sendUnitType = -1;
        try {
            if(recOrgType == 1) {//发送机关为单位
                if(recOrgId == orgAccountId) {//本单位
                	sendUnitType = Constants.EDOC_EXCHANGE_UNION_TYPE_A8_INNER; 
                } else { //同套A8多组织其它单位
                	sendUnitType = Constants.EDOC_EXCHANGE_UNION_TYPE_A8_OUTER;
                }
            } else {
                V3xOrgDepartment dept2 = orgManager.getDepartmentById(recOrgId);
                if(dept2!=null) {
                    if(dept2.getOrgAccountId() == orgAccountId) {
                    	sendUnitType = Constants.EDOC_EXCHANGE_UNION_TYPE_A8_INNER; 
                    } else {
                    	sendUnitType = Constants.EDOC_EXCHANGE_UNION_TYPE_A8_OUTER;                     
                    }
                } else {
                	sendUnitType = Constants.EDOC_EXCHANGE_UNION_TYPE_UKNOW;
                }
            }
        } catch(Exception e) {
            LOGGER.error("", e);
        }
        return sendUnitType;
    }
	
	public List<EdocRecieveRecord> findWaitRegisterEdocRecieveRecords(String registerIds, int state, int registerType, String condition, String[] value){
		return edocRecieveRecordDao.getWaitRegiterEdoc(registerIds, state, registerType, condition, value);
	}
	
	public List<EdocRecieveRecord> findWaitEdocRegisterList(int state, Map<String, Object> condition) {
		Long userId = (Long)condition.get("userId");
		condition.put("memberId", userId);
		List<EdocRecieveRecord> self_edocs = edocRecieveRecordDao.findWaitEdocRegisterList(state, condition);
		List<AgentModel> agentModels = EdocHelper.getEdocAgents();
        if(CollectionUtils.isNotEmpty(agentModels)) {
        	for(AgentModel agent : agentModels) {
        		if(agent != null) {
        			if(agent.getAgentId().equals(userId)) {//我给别人做事，标题显示蓝色，标题后添加(代理某某)
	        			condition.put("agentModel", agent);
	        			condition.put("memberId", agent.getAgentToId());
	        			List<EdocRecieveRecord> agent_edocs = edocRecieveRecordDao.findWaitEdocRegisterList(state, condition);
	        			agent_edocs = this.convertEdocRegistersAgent(agent_edocs, userId, agent);
	        			Strings.addAllIgnoreEmpty(self_edocs, agent_edocs);
        			}else if(agent.getAgentToId().equals(userId)) {//别人给我做事，标题显示蓝色
        				self_edocs = this.convertEdocRegistersAgent(self_edocs, userId, agent);
        			}
        		} else {
        			//log.warn("用户[id=" + userId + "]代理对象集合中存在无效元素！");
        		}
        	}
        	if(self_edocs != null) {
        		//Collections.sort(self_edocs);
        	}
        }
		return self_edocs;
	}

	/**
	 * 为代理人所能查看的会议加上代理标识
	 */
	private List<EdocRecieveRecord> convertEdocRegistersAgent(List<EdocRecieveRecord> registers,Long currentUserId, AgentModel agent) {
		List<EdocRecieveRecord> result = null;		
		if(CollectionUtils.isNotEmpty(registers)) {
			result = new ArrayList<EdocRecieveRecord>();
			//判断当前用户是不是让别人干活的人
			boolean isAgentTo = agent.getAgentToId().equals(currentUserId);
			for(EdocRecieveRecord er : registers) {
				try {
					EdocRecieveRecord register = (EdocRecieveRecord) er.clone();
					register.setId(er.getId());
					//设置代理时列表信息全部显示为蓝色的错误
					if(register!=null && null != register.getRecTime()) {
						if(register.getRegisterUserId() !=  AppContext.getCurrentUser().getId()){
							if(agent.getStartDate().compareTo(register.getRecTime())<=0 && agent.getEndDate().compareTo(register.getRecTime())>=0) {
								er.setProxy(true);
								er.setProxyUserId(isAgentTo?null:agent.getAgentToId());
								result.add(er);
							}
						} else {
							//er.setProxy(true);
							//er.setProxyUserId(isAgentTo?null:agent.getAgentToId());
							result.add(er);
						}
					}
				} catch (CloneNotSupportedException e) {
					//log.error("克隆会议出现异常：", e);
				}
			}
		}
		return result;
	}
	
	public EdocRecieveRecord getEdocRecieveRecordByReciveEdocId(long id) {
		return edocRecieveRecordDao.getEdocRecieveRecordByReciveEdocId(id);
	}
	
	//changyi moveTo5.0 TODO A8 APP加的方法
	public Boolean registerRecieveEdoc(Long id,Long reciveEdocId) throws Exception 
    {
        EdocRecieveRecord rec=getEdocRecieveRecord(id);
        rec.setRegisterTime(new Timestamp(System.currentTimeMillis()));
        rec.setStatus(EdocRecieveRecord.Exchange_iStatus_Registered);
        rec.setReciveEdocId(reciveEdocId);
        edocRecieveRecordDao.update(rec);
        
        Map<String, Object> columns=new Hashtable<String, Object>();
        columns.put("state",Integer.valueOf(StateEnum.col_done.getKey()));
    
        affairManager.update(columns, new Object[][]{{"objectId",Long.valueOf(rec.getEdocId())}, {"subObjectId",rec.getId()}});     
        User user = AppContext.getCurrentUser();
        Long regUserId = rec.getRegisterUserId();
        Long agentToId = null; //被代理人ID
        String agentToName= "";
        if(!Long.valueOf(user.getId()).equals(regUserId)){
            agentToId = regUserId;
            try{
                agentToName = orgManager.getMemberById(agentToId).getName();
            }catch(Exception e){
                LOGGER.error("", e);
            }
        }
        if(agentToId != null){
            MessageContent msgContent=new MessageContent("exchange.edoc.register",agentToName,rec.getSubject()).add("edoc.agent.deal", user.getName());
            msgContent.setResource("com.seeyon.v3x.exchange.resources.i18n.ExchangeResource");
            MessageReceiver receiver=new MessageReceiver(rec.getId(),rec.getRecUserId(),"message.link.exchange.register.receive",rec.getId().toString());
            userMessageManager.sendSystemMessage(msgContent,ApplicationCategoryEnum.edoc,agentToId,receiver,EdocMessageFilterParamEnum.exchange.key);
        }else{
            MessageContent msgContent=new MessageContent("exchange.edoc.register",user.getName(),rec.getSubject());
            msgContent.setResource("com.seeyon.v3x.exchange.resources.i18n.ExchangeResource");
            MessageReceiver receiver=new MessageReceiver(rec.getId(),rec.getRecUserId(),"message.link.exchange.register.receive",rec.getId().toString());
            userMessageManager.sendSystemMessage(msgContent,ApplicationCategoryEnum.edoc,user.getId(),receiver,EdocMessageFilterParamEnum.exchange.key);    
        }
        return true;
    }

	@Override
	public void update(Map<String, Object> columns, Object[][] where)
			throws BusinessException {
		edocRecieveRecordDao.update(EdocRecieveRecord.class, columns, where);
		
	}
	/**
	 * 代理人处理 TODO--SP1重构
	 * @param condition
	 * @param userId
	 * @return
	 */
	@SuppressWarnings("unused")
	public Map<String, Object> getEdocAgentInfo(Map<String, Object> condition) {
		long userId = (Long)condition.get("userId");
		//获取代理相关信息
  		List<AgentModel> _agentModelList = MemberAgentBean.getInstance().getAgentModelList(userId);
      	List<AgentModel> _agentModelToList = MemberAgentBean.getInstance().getAgentModelToList(userId);
  		List<AgentModel> agentModelList = null;
  		boolean agentToFlag = false;
  		boolean agentFlag = false;
  		if(_agentModelList != null && !_agentModelList.isEmpty()){
  			agentModelList = _agentModelList;
  			agentFlag = true;
  		}else if(_agentModelToList != null && !_agentModelToList.isEmpty()){
  			agentModelList = _agentModelToList;
  			agentToFlag = true;
  		}
      		
  		List<AgentModel> edocAgent = new ArrayList<AgentModel>();
  		if(agentModelList != null && !agentModelList.isEmpty()){
  			java.util.Date now = new java.util.Date();
  	    	for(AgentModel agentModel : agentModelList){
      			if(agentModel.isHasEdoc() && agentModel.getStartDate().before(now) && agentModel.getEndDate().after(now)){
      				edocAgent.add(agentModel);
      			}
  	    	}
  		}
      	boolean isProxy = false;
  		if(edocAgent != null && !edocAgent.isEmpty()){
  			isProxy = true;
  		}else{
  			agentFlag = false;
  			agentToFlag = false;
  		}
  		condition.put("edocAgent", edocAgent);
  		condition.put("agentToFlag", agentToFlag);
  		condition.put("agentFlag", agentFlag);
  		return condition;
	}
	
	/********************************************公文签收列表 start*************************************************************/
	/**
	 * 
	 * @param type 3：待签收列表 4已签收列表
	 * @param condition
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<EdocRecieveRecord> findEdocRecieveRecordList(int type, Map<String, Object> condition) throws BusinessException {
		String listType = (String)condition.get("listType");
		int state = Integer.parseInt(EdocNavigationEnum.EdocV5ListTypeEnum.getStateName(listType));
		List<Integer> statusList = (List<Integer>)condition.get("statusList");
		condition.put("statusList", getListStatusByListType(state, statusList));
		return edocRecieveRecordDao.findEdocRecieveRecordList(type, condition);
	}
	
	private List<Integer> getListStatusByListType(int state, List<Integer> statusList) {
		if(state == 3) {
			String isG6 = SystemProperties.getInstance().getProperty("edoc.toReceiveSearch");
			//目前G6-V5待登记中回退，就直接在交换--待签收中显示了(原收文管理下签收页签取消掉了，因此没有了签收退箱了)
			//将下面的判断注释掉
//	      	if("false".equals(isG6)) {//G6
//	      		statusList.add(EdocRecieveRecord.Exchange_iStatus_Receive_Retreat);
//	      	}
			statusList.add(EdocRecieveRecord.Exchange_iStatus_Receive_Retreat);
		}
		return statusList;
	}
	@SuppressWarnings("unchecked")
	public int findEdocRecieveRecordCount(int type, Map<String, Object> condition) throws BusinessException {
		String listType = (String)condition.get("listType");
		int state = Integer.parseInt(EdocNavigationEnum.EdocV5ListTypeEnum.getStateName(listType));
		List<Integer> statusList = (List<Integer>)condition.get("statusList");
		condition.put("statusList", getListStatusByListType(state, statusList));
		return edocRecieveRecordDao.findEdocRecieveRecordCount(type, condition);
	}
	/**
	 * 公文交换列表逻辑-签收-批量删除
	 * @param ids
	 * @throws BusinessException
	 */
	public void deleteEdocRecieveRecordByLogic(String listType, Map<Long, String> map) throws BusinessException {
		User user = CurrentUser.get();
		//1 逻辑删除交换数据
		int state = Integer.parseInt(EdocNavigationEnum.EdocV5ListTypeEnum.getStateName(listType));
		int status = (state==3) ? EdocRecieveRecord.Exchange_iStatus_ToReceive_Delete : EdocRecieveRecord.Exchange_iStatus_Receive_Delete;
		edocRecieveRecordDao.deleteEdocRecieveRecordByLogic(map.keySet().toArray(), status);
		EdocRecieveRecord edocRecieveRecord = null;
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("delete", Boolean.TRUE);
		String userName = AppContext.getCurrentUser().getName();
		Iterator<Entry<Long, String>> it = map.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Long, String> obj = (Entry<Long, String>)it.next();
			edocRecieveRecord = edocRecieveRecordDao.get(obj.getKey());
			if(edocRecieveRecord == null) continue;
			AppLogAction action = AppLogAction.Edoc_PreSign_Record_Del;
			if(edocRecieveRecord.getStatus() == EdocRecieveRecord.Exchange_iStatus_Receive_Delete) {//已签收中被删除
				action = AppLogAction.Edoc_Sign_Record_Del;
			}
			//2 写交换删除日志
			appLogManager.insertLog(user, action, userName, edocRecieveRecord.getSubject());
			//3 同步首页栏目
			Object[][] wheres = new Object[4][2];
			wheres[0][0] = "app";
			wheres[0][1] = ApplicationCategoryEnum.edoc.key();
			wheres[1][0] = "subApp";
			wheres[1][1] = ApplicationSubCategoryEnum.old_exSend.key();
			wheres[2][0] = "objectId";
			wheres[2][1] = edocRecieveRecord.getEdocId();
			wheres[3][0] = "subObjectId";
			wheres[3][1] = edocRecieveRecord.getId();
			affairManager.update(paramMap, wheres);
			//4 发删除消息
			List<V3xOrgMember> memberList = null;
			if(edocRecieveRecord.getExchangeType()==EdocRecieveRecord.Exchange_Receive_iAccountType_Dept) {//部门
				V3xOrgDepartment dept = orgManager.getDepartmentById(edocRecieveRecord.getExchangeOrgId());
				if(dept != null) {
					memberList = GovdocRoleHelper.getDepartMentExchangeUsers(dept.getOrgAccountId(), dept.getId());
				}
			} else if(edocRecieveRecord.getExchangeType()==EdocRecieveRecord.Exchange_Receive_iAccountType_Org) {//单位
				memberList = GovdocRoleHelper.getAccountExchangeUsers(edocRecieveRecord.getExchangeOrgId());
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
				MessageContent content = new MessageContent("edoc.exchange.recieve.list.delete."+status, user.getName(), edocRecieveRecord.getSubject());
				userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.edoc, user.getId(), receiverList,EdocMessageFilterParamEnum.exchange.key);
			}
		}
		
	}
	/********************************************公文签收列表       end*************************************************************/

	@Override
	public List<EdocRecieveRecord> getEdocRecieveRecordByEdocIdAndOrgIdAndStatus(
			long edocId, long exchangeOrgId, int status) {
		return edocRecieveRecordDao.getEdocRecieveRecordByEdocIdAndOrgIdAndStatus(edocId, exchangeOrgId, status);
	}
	
	@Override
	public List<EdocRecieveRecord> getEdocRecieveRecordByOrgIdAndStatus(long exchangeOrgId, int status) {
		List<EdocRecieveRecord> list = edocRecieveRecordDao.getEdocRecieveRecordByOrgIdAndStatus(exchangeOrgId, status);
		return list;
	}
	
	/**
	 * 获得某单位下某个状态的签收数据
	 * 用于 登记开关切换时，需要判断该单位中是否还有待登记的数据
	 * @param status
	 * @param accountId
	 * @return
	 */
	public List<EdocRecieveRecord> findEdocRecieveRecordByStatusAndAccountId(int status,long accountId){
		List<Long> idList = getAllDepIdByAccountId(accountId);
		
		return edocRecieveRecordDao.findEdocRecieveRecordByStatusAndAccountId(status,idList);
	}
	
	public List<Long> getAllDepIdByAccountId(long accountId){
		List<Long> idList = new ArrayList<Long>();
		idList.add(accountId);
		//获得单位下的所有部门
		try {
			List<V3xOrgDepartment> departments = orgManager.getAllDepartments(accountId);
			for(V3xOrgDepartment de: departments){
				idList.add(de.getId());
			}
		} catch (BusinessException e) {
			LOGGER.error("获得单位下所有部门报错", e);
		}
		return idList;
	}
	
	public int findEdocRecieveRecordCountByStatusAndAccountId(int status,long accountId){
		List<Long> idList = getAllDepIdByAccountId(accountId);
		return edocRecieveRecordDao.findEdocRecieveRecordCountByStatusAndAccountId(status,idList);
	}
	/**
	 * 老公文交换待签收的数据的在待办列表显示正文类型图标 
	 * @return
	 * @throws BusinessException
	 */
	public String getEdocExchangeContentType(Long recieveId){
		EdocRecieveRecord record = edocRecieveRecordDao.get(recieveId);
		String contentType = null;
		if(record != null){
			List<EdocBody> bodys = GovdocHelper.getEdocBodys(record.getEdocId());
			if(bodys != null && !bodys.isEmpty()){
				for (EdocBody body : bodys) {
					if(body.getContentNo().intValue() == record.getContentNo().intValue()){
						contentType = body.getContentType();
						break;
					}
				}
			}
		}
		return contentType;
	}
}
