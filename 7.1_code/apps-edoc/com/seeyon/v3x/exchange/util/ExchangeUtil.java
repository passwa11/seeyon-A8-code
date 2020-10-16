package com.seeyon.v3x.exchange.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.govdoc.constant.GovdocEnum.OldExchangeNodePolicyEnum;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.enums.AffairExtPropEnums;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.domain.RegisterBody;
import com.seeyon.v3x.edoc.manager.EdocRegisterManager;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.domain.EdocSendDetail;
import com.seeyon.v3x.exchange.domain.EdocSendRecord;
import com.seeyon.v3x.exchange.manager.SendEdocManager;
import com.seeyon.ctp.util.Strings;

public class ExchangeUtil {
	private static final Log LOGGER = LogFactory.getLog(ExchangeUtil.class);
	/**
	 * 判断待交换下发文状态是否为“待发送”
	 * @param status
	 * @return
	 */
	public static boolean isEdocExchangeToSendRecord(int status) {
		if(status==EdocSendRecord.Exchange_iStatus_Tosend 
				|| status==EdocSendRecord.Exchange_iStatus_Send_New_StepBacked
				|| status==EdocSendRecord.Exchange_iStatus_Send_New_Cancel) {
			return true;
		}
		return false;
	}
	
	/**
	 * 判断待交换下发文状态是否为“已回退”
	 * @param status
	 * @return
	 */
	public static boolean isEdocExchangeSentStepBacked(int status) {
		if(status==EdocSendRecord.Exchange_iStatus_Send_StepBacked) {
			return true;
		}
		return false;
	}
	
	public static void createRegisterAffair(CtpAffair reAffair,User user,EdocRecieveRecord record,EdocSummary summary,int state){
		reAffair.setIdIfNew();
		reAffair.setApp(ApplicationCategoryEnum.edoc.getKey());
		reAffair.setSubApp(ApplicationSubCategoryEnum.old_edocRegister.getKey());
		reAffair.setNodePolicy(OldExchangeNodePolicyEnum.olddengji.name());
		reAffair.setBodyType(String.valueOf(GovdocUtil.getContentType(summary.getBodyType())));
		reAffair.setSubject(record.getSubject());
		reAffair.setCreateDate(new Timestamp(System.currentTimeMillis()));
		reAffair.setReceiveTime(new Timestamp(System.currentTimeMillis()));
		reAffair.setMemberId(record.getRegisterUserId());
		reAffair.setSenderId(user.getId());
		reAffair.setObjectId(record.getEdocId());
		reAffair.setSubObjectId(record.getId());
		reAffair.setSenderId(user.getId());
		reAffair.setState(state);
		reAffair.setSubState(SubStateEnum.col_pending_unRead.getKey());  //要给待登记的设置子状态为正常，不然统计图统计不到 OA-39223	待办栏目统计图，办理状态图，待登记的被统计在待办中，但是通过点击图，却筛选不出来。。
		reAffair.setTrack(null);	
		//wangjinging 待办事项 待登记 匹配条件 begin
		reAffair.setAddition("sendUnitId="+record.getExchangeOrgId());
		//wangjinging 待办事项 待登记 匹配条件 end
		if(record.getUrgentLevel()!=null && !"".equals(record.getUrgentLevel()))
			reAffair.setImportantLevel(Integer.parseInt(record.getUrgentLevel()));
		
		//首页栏目的扩展字段设置--公文文号、发文单位等--start
		AffairUtil.addExtProperty(reAffair,AffairExtPropEnums.edoc_edocMark ,record.getDocMark());
		AffairUtil.addExtProperty(reAffair,AffairExtPropEnums.edoc_sendUnit ,record.getSendUnit());
		AffairUtil.addExtProperty(reAffair,AffairExtPropEnums.edoc_sendAccountId ,summary.getSendUnitId());
		//首页栏目的扩展字段设置--公文文号、发文单位等--end
    }
	
	private static void setSendToIdAndNameToRegister(EdocRecieveRecord record,EdocRegister register){
		String sendToId = null;
        String sendToNames = null;
        LOGGER.info("监听方法setSendToIdAndNameToRegister*******");
        //获取送文单上的主送单位
        if(record != null) {
        	if(Strings.isNotBlank(record.getReplyId())) {
        		long sendDetailId = Long.parseLong(record.getReplyId());
        		SendEdocManager sendEdocManager = (SendEdocManager)AppContext.getBean("sendEdocManager");
        		EdocSendDetail sendDetail;
				try {
					sendDetail = sendEdocManager.getSendRecordDetail(sendDetailId);
	        		if(sendDetail !=null){
	        			Long sendId = sendDetail.getSendRecordId();
	        			if(sendId != null) {
		        			EdocSendRecord sendRecord = sendEdocManager.getEdocSendRecord(sendId);
		        			if(sendRecord != null) {
			        			sendToId = sendRecord.getSendedTypeIds();
			        			sendToNames = sendRecord.getSendEntityNames();
		        			} else {
		        				LOGGER.info("sendRecord为空: sendId="+sendId);
		        			}
	        			} else {
	        				LOGGER.info("sendId为空: sendDetailId="+sendDetailId);
	        			}
	        		} else {
	        			LOGGER.info("sendDetail为空：sendDetailId="+sendDetailId);
	        		}
				}catch (BusinessException e) {
					LOGGER.error("",e);
				}
        	}
        } else {
        	LOGGER.info("replyId为空：recordId="+record.getId());
        }
         
        register.setSendTo(sendToNames);
        register.setSendToId(sendToId);
	}
	
	
	public static EdocRegister createAutoRegisterData(
		EdocRecieveRecord record,
		EdocSummary summary,
		long distributerId,
		OrgManager orgManager)throws BusinessException{
		
		//签收完了，就登记
        EdocRegister register = new EdocRegister();
        register.setNewId();
        register.setAutoRegister(EdocRegister.AUTO_REGISTER);
        register.setRecTime(record.getRecTime());
        register.setRecieveId(record.getId());
        register.setSubject(record.getSubject());
        register.setRegisterType(EdocNavigationEnum.RegisterType.ByAutomatic.ordinal());//电子登记
        register.setRegisterUserId(record.getRegisterUserId());
        register.setCreateUserId(record.getRegisterUserId());
        V3xOrgMember member = orgManager.getMemberById(record.getRegisterUserId());
        register.setCreateUserName(member.getName());
        
        register.setRegisterUserName(member.getName());
        register.setDistributeEdocId(-1l);
        
        //单位id就是登记人所在单位id
//        long accountId = member.getOrgAccountId();
        
        long accountId = 0L;
        
        if(summary != null){
            accountId = summary.getOrgAccountId();
        }else{
            
            //获得交换到的单位id
            if(record.getExchangeType() == 1){
                accountId = record.getExchangeOrgId();
            }else{
                accountId = orgManager.getDepartmentById(record.getExchangeOrgId()).getOrgAccountId();
            }
        }
        
        
        register.setUrgentLevel(record.getUrgentLevel());
        register.setState(2);//已登记
        register.setEdocType(1);//收文
        register.setEdocId(record.getEdocId());
        register.setDocType(record.getDocType());
        register.setDocMark(record.getDocMark());
        register.setCreateTime(new java.sql.Timestamp(new Date().getTime()));
        register.setUpdateTime(new java.sql.Timestamp(new Date().getTime()));
        register.setRegisterDate(new java.sql.Date(new Date().getTime()));
        register.setIsRetreat(EdocNavigationEnum.RegisterRetreatState.NotRetreat.ordinal());//非退回
        register.setDistributeState(EdocNavigationEnum.EdocDistributeState.WaitDistribute.ordinal());//待分发状态
        
        
        V3xOrgAccount account = orgManager.getAccountById(accountId);
        
        register.setSendUnit(account==null?"":account.getName());
        register.setSendUnitId(account==null?null:account.getId());
        
        register.setSendUnitType(record==null? 1 : record.getSendUnitType());
        register.setSecretLevel(record==null? "1" : record.getSecretLevel());
        register.setUrgentLevel(record==null? "1" : record.getUrgentLevel());
        
        /* xiangfan G6 V1.0 SP1后续功能_签收时自动登记功能  修复文单没有保密期限时 存入的值为‘null’字符串 导致 分发时报错 Start*/
        String summarykeepPeriod = null;
        if(summary != null && null != summary.getKeepPeriod()){
        	summarykeepPeriod = String.valueOf(summary.getKeepPeriod());
        }
        /* xiangfan G6 V1.0 SP1后续功能_签收时自动登记功能  修复文单没有保密期限时 存入的值为‘null’字符串 导致 分发时报错 End*/
        register.setKeepPeriod(summary==null? record==null?"":record.getKeepPeriod() : summarykeepPeriod);
        register.setSendType(summary==null? "1" : summary.getSendType());
        register.setKeywords(summary==null? "" : summary.getKeywords());
        register.setIssuerId(-1l);
        register.setIssuer(summary==null? "" : summary.getIssuer());
        register.setEdocDate(summary==null? null : summary.getSigningDate());
        /*if(register.getEdocDate()==null) {//如果没有签发时间，则显示为封发时间
        	register.setEdocDate(summary==null ? null : new java.sql.Date(summary.getPackTime().getTime()));
        }
        */
        
        String _summarySendTo = summary==null? "" : summary.getSendTo();
        String _summarySendToId = summary==null? "" : summary.getSendToId();

        setSendToIdAndNameToRegister(record,register);
       
        if(Strings.isBlank(register.getSendTo())){
        	register.setSendTo(_summarySendTo);
        	register.setSendToId(_summarySendToId);
        }
        register.setCopyTo(null);
        register.setCopyToId(null);
        
        List<Attachment> attachmentList = new ArrayList<Attachment>();
        //附件信息
        if(summary != null) {
        	AttachmentManager attachmentManager = (AttachmentManager)AppContext.getBean("attachmentManager");
        	attachmentList = attachmentManager.getByReference(summary.getId(), summary.getId());
        }
        register.setIdentifier("00000000000000000000");
        register.setAttachmentList(attachmentList);
        register.setHasAttachments(attachmentList.size()>0);
        register.setEdocUnit(record.getSendTo());
        register.setEdocUnitId("Account|" + accountId);
        
        register.setDistributerId(distributerId);
        //分发人名称
        register.setDistributer(orgManager.getMemberById(distributerId).getName());
        
        Long registerOrgAccount = ExchangeUtil.getAccountIdOfRegisterByOrgIdAndOrgType(record.getExchangeOrgId(),record.getExchangeType());
        
        register.setOrgAccountId(registerOrgAccount);
        register.setIdentifier("00000000000000000000");
        
        RegisterBody registerBody = null;
        //装载公文正文
        if(summary !=null && record!=null) {
        	EdocBody edocBody = summary.getBody(record.getContentNo().intValue());
        	registerBody = new RegisterBody();
        	edocBody = edocBody==null?summary.getFirstBody() : edocBody;
			if(null != edocBody){
				registerBody.setIdIfNew();
				registerBody.setContent(edocBody.getContent());
				registerBody.setContentNo(edocBody.getContentNo());
				registerBody.setContentType(edocBody.getContentType());
				registerBody.setCreateTime(edocBody.getLastUpdate());
				register.setRegisterBody(registerBody);	
			}
        } else {
        	registerBody = new RegisterBody();
        	String bodyContentType=com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_OFFICE_WORD;
            if(com.seeyon.ctp.common.SystemEnvironment.hasPlugin("officeOcx")==false) {
            	bodyContentType = com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_HTML;
            }
            registerBody.setIdIfNew();
        	registerBody.setContentType(bodyContentType);
        	registerBody.setCreateTime(new java.sql.Timestamp(new java.util.Date().getTime()));
        	register.setRegisterBody(registerBody);
        }
        if(summary!=null && !Strings.isBlank(summary.getSendUnit()) && !Strings.isBlank(summary.getSendUnitId())) {
        	register.setEdocUnit(summary.getSendUnit());
        	register.setEdocUnitId(summary.getSendUnitId());
        }else{//lijl添加else，如果来文单位为空,则取record里的来文单位
        	register.setEdocUnit(record==null?"":record.getSendUnit());
        }
        if(registerBody!=null){
        	registerBody.setEdocRegister(register);
        }
        
        register.setSerialNo("");
        register.setRecieveUserId(record.getRecUserId());
        String recUserName = "";
		V3xOrgEntity recMember = orgManager.getEntity("Member", record.getRecUserId());
		if (recMember != null) {
			recUserName = recMember.getName();
		}
    	register.setRecieveUserName(recUserName);
    	register.setExchangeSendTime(record.getCreateTime());
    	
        return register;
    }
	
	public static void createRegisterDataByPaperEdoc(EdocSummary summary,EdocRegisterManager edocRegisterManager){
		EdocRegister register = edocRegisterManager.findRegisterByDistributeEdocId(summary.getId());
		if(register == null){
			register = new EdocRegister();
    		register.setNewId();
    		register.setRegisterType(EdocRegister.REGISTER_TYPE_BY_PAPER_REC_EDOC);
    		register.setSubject(summary.getSubject());
    		register.setDistributeEdocId(summary.getId());
    		register.setCreateTime(new Timestamp(System.currentTimeMillis()));
    		register.setUpdateTime(new Timestamp(System.currentTimeMillis()));
    		register.setDistributeDate(new java.sql.Date(new Date().getTime()));
    		
    		//当公文单上设置了登记日期时，登记表中才插入登记日期，在分发待发列表中会显示出登记日期
    		if(summary.getRegistrationDate() != null){
    			register.setRegisterDate(summary.getRegistrationDate());
    		}
    		//G6纸质收文，在收文登记簿中分发日期查询时其实是对register的登记日期字段查的
    		else{
    			register.setRegisterDate(new java.sql.Date(new Date().getTime()));
    		}
    		
    		register.setEdocType(1);
    		register.setState(EdocRegister.REGISTER_TYPE_BY_PAPER_REC_EDOC);
    		register.setIsRetreat(0);
    		register.setIdentifier("00000000000000000000");
    		register.setDistributerId(summary.getStartUserId());
    		try {
    		    OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
                register.setDistributer(orgManager.getMemberById(summary.getStartUserId()).getName());
            } catch (BusinessException e) {
                LOGGER.error("", e);
            }
    		register.setOrgAccountId(summary.getOrgAccountId());
    		register.setDocMark(summary.getDocMark());
    		register.setSerialNo(summary.getSerialNo());
            if(summary!=null &&!Strings.isBlank(summary.getSendUnitId())) {
            	String[] suSt=summary.getSendUnitId().split("[|]");
            	if(suSt.length==2 && Strings.isNotBlank(suSt[1])){
            		register.setSendUnitId(Long.parseLong(suSt[1]));
            	}
            	
            	register.setEdocUnitId(summary.getSendUnitId());
            }
        	register.setSendUnit(summary.getSendUnit());
        	register.setEdocUnit(summary.getSendUnit());
            
            
            RegisterBody registerBody = new RegisterBody();
          	String bodyContentType=com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_OFFICE_WORD;
            if(com.seeyon.ctp.common.SystemEnvironment.hasPlugin("officeOcx")==false) {
              	bodyContentType = com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_HTML;
            }
            registerBody.setIdIfNew();
          	registerBody.setContentType(bodyContentType);
          	registerBody.setCreateTime(new java.sql.Timestamp(new java.util.Date().getTime()));
          	registerBody.setEdocRegister(register);
          	register.setRegisterBody(registerBody);
          	register.setAutoRegister(EdocRegister.AUTO_REGISTER);
          	register.setEdocId(summary.getId());
          	edocRegisterManager.createEdocRegister(register);
		}
	}
	
    /**
     * 查找签收部门所属单位或者签收单位
     * 
     * @param exchangeOrgId
     *            签收ID（单位ID|部门ID）
     * @param exchangeOrgType
     *            签收类型（部门|单位）
     * @return
     */
    public static Long getAccountIdOfRegisterByOrgIdAndOrgType(Long exchangeOrgId, int exchangeOrgType) {
        if (EdocRecieveRecord.Exchange_Receive_iAccountType_Dept == exchangeOrgType) {
            V3xOrgDepartment dept;
            try {
                OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
                dept = orgManager.getDepartmentById(exchangeOrgId);
                return dept.getOrgAccountId();
            } catch (BusinessException e) {
                LOGGER.error("查找部门异常:", e);
            }
        } else {
            return exchangeOrgId;
        }
        return 0L;
    }
	
}
