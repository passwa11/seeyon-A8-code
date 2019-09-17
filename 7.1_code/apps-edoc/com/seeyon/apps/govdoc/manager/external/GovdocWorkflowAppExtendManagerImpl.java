package com.seeyon.apps.govdoc.manager.external;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.agent.AgentConstants;
import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.LocaleContext;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.lock.manager.LockManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.lock.Lock;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.supervise.bo.SuperviseHastenParam;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.usermessage.Constants;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
//import com.seeyon.ctp.workflow.vo.WorkflowFormFieldVO;
import com.seeyon.ctp.workflow.bo.WorkflowFormFieldBO;
import com.seeyon.ctp.workflow.wapi.WorkFlowAppExtendAbstractManager;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.ctp.workflow.wapi.WorkflowFormDataMapManager;
import com.seeyon.v3x.edoc.domain.EdocFormElement;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.enums.EdocMessageFilterParamEnum;
import com.seeyon.v3x.edoc.manager.EdocFormManager;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.manager.EdocSummaryManager;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.mobile.message.manager.MobileMessageManager;

import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

public class GovdocWorkflowAppExtendManagerImpl extends WorkFlowAppExtendAbstractManager {
	
	private final static Log LOGGER = LogFactory.getLog(GovdocWorkflowAppExtendManagerImpl.class);
	
	public static final int SEND_CONDITION = 3; //发文
    public static final int REC_CONDITION = 4;  //收文
    public static final int SIGN_CONDITION = 5; //签报

    private EdocManager edocManager;
    private SuperviseManager superviseManager;
    private AffairManager affairManager;
    private OrgManager orgManager;
    private AppLogManager appLogManager;
    private UserMessageManager userMessageManager;
    private MobileMessageManager mobileMessageManager;
    private EdocFormManager edocFormManager;
    private EdocSummaryManager edocSummaryManager;
    private WorkflowApiManager wapi; 
    private TemplateManager templateManager;
    private LockManager lockManager;
    private FormApi4Cap3 formApi4Cap3;

	public FormApi4Cap3 getFormApi4Cap3() {
		return formApi4Cap3;
	}

	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
		this.formApi4Cap3 = formApi4Cap3;
	}

	public LockManager getLockManager() {
		return lockManager;
	}

	public void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}
	public WorkflowApiManager getWapi() {
		return wapi;
	}

	public void setWapi(WorkflowApiManager wapi) {
		this.wapi = wapi;
	}

	public void setMobileMessageManager(MobileMessageManager mobileMessageManager) {
		this.mobileMessageManager = mobileMessageManager;
	}

	public void setUserMessageManager(UserMessageManager userMessageManager) {
		this.userMessageManager = userMessageManager;
	}

	public void setAppLogManager(AppLogManager appLogManager) {
		this.appLogManager = appLogManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	public void setSuperviseManager(SuperviseManager superviseManager) {
		this.superviseManager = superviseManager;
	}

	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}
	public void setEdocSummaryManager(EdocSummaryManager edocSummaryManager) {
		this.edocSummaryManager = edocSummaryManager;
	}

	@Override
	//判断当前人员是否有督办权限
	public boolean checkUserSupervisorRight(String caseId, Long userId) {
		EdocSummary summary;
		try {
			summary = edocManager.getSummaryByCaseId(Long.parseLong(caseId));
			CtpAffair affair = affairManager.getSenderAffair(summary.getId());
			boolean isSupervisor = superviseManager.isSupervisor(userId, summary.getId());
			if(!isSupervisor){
				if(affair.getSenderId().equals(userId) && null==summary.getTempleteId()){//发起人可以修改
					return true;
				}else {
					V3xOrgMember m = orgManager.getMemberById(userId);
                    boolean isAdmin= orgManager.isAdministratorById(userId, m.getOrgAccountId());
                    boolean isGroupAdmin=orgManager.isGroupAdminById(userId);//集团管理员判断
                    if(isAdmin||isGroupAdmin){
                        return true;
                    }
                    return false;
                }
			}
		}catch (Exception e) {
			LOGGER.error("检查督办权限错误：",e);
		}
		return true;
	}

	@Override
	public AffairData getAffairData(String processId) {
		AffairData affairData=null;
		User user = AppContext.getCurrentUser();
		try {
			EdocSummary summary=edocManager.getEdocSummaryByProcessId(Long.valueOf(processId));
			affairData=EdocHelper.getAffairData(summary,user);
		} catch (NumberFormatException e) {
			LOGGER.error("", e);
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return affairData;
	}

	@Override
	public ApplicationCategoryEnum getAppName() {
		 return ApplicationCategoryEnum.edoc;
	}

	@Override
	public String getSummarySubject(String processId) {
		String subject="";
    	try {
			EdocSummary summary=edocManager.getSummaryByProcessId(processId);
			subject=summary.getSubject();
			
		} catch (NumberFormatException e) {
			LOGGER.error("", e);
		}
        return subject;
	}

	@Override
	public int getSummaryVouch(String arg0) {
		return 0;
	}
	
	//发送催办信息的同时，发送短信
	@Override
	public String hasten(String processId, String activityId,
			List<Long> personIds, String superviseId, String content,
			boolean sendPhoneMessage) {
		boolean result = this.hasten(processId, activityId, personIds, superviseId, content);
		
		String ret = ResourceUtil.getString("supervise.hasten.success.label"); //催办成功
		
		if(sendPhoneMessage && result){
		    
		    try {
                content = fillContent(processId, activityId, content);
              } catch (NumberFormatException e) {
              } catch (BusinessException e) {
              }
		    
			int app = ApplicationCategoryEnum.collaboration.getKey();//应用类型
			Long senderId = AppContext.currentUserId();//发送者id
			Date time = new Date();//发送时间
			if(personIds!=null && personIds.size()>0 && mobileMessageManager!=null){
				//这句代码发送短信
				mobileMessageManager.sendMobileMessage(app, content, senderId, time, personIds);
			}
			
            //检查人员电话号码
            StringBuilder msg = new StringBuilder();
            try {
                for(Long memberId : personIds){
                    V3xOrgMember member = orgManager.getMemberById(memberId);
                    String phone = member.getTelNumber();
                    if(Strings.isBlank(phone)){
                        if(msg.length() > 0){
                            msg.append("、");
                        }
                        msg.append(member.getName());
                    }
                }
                String tempMsg = msg.toString();
                if(Strings.isNotBlank(tempMsg)){
                    ret = Strings.toHTML(ResourceUtil.getString("calcel.meeting.send.sms.alert.info2") + "\n" + tempMsg);
                }
            } catch (BusinessException e) {
                LOGGER.error("", e);
            }
		}
		
		if(!result){
		    ret = ResourceUtil.getString("supervise.hasten.fail.label"); //催办失败
		}
		
		return ret;
	}
	
	
	//内容处理，将催办的内容加上催办人和协同标题
    private String fillContent(String processId, String activityId, String content) throws BusinessException {
        // 当前登录用户姓名
        String name = AppContext.currentUserName();
        String subject = "";
        // 附言标记，有内容为1，无内容为0
        int additional_remarkFlag = Strings.isBlank(content) ? 0 : 1;
        int forwardMemberFlag = 0;
        String forwardMember = null;
        Long summaryId = null;
        String forwardMemberId = "";
        // 获取催办的协同
        EdocSummary summary = edocManager.getSummaryByProcessId(processId);
        if (summary != null) {
            subject = summary.getSubject();
            summaryId = summary.getId();
        }
        List<CtpAffair> affairs = affairManager.getAffairsByObjectIdAndNodeId(summaryId, Long.parseLong(activityId));
        if (Strings.isNotEmpty(affairs)) {
            // 转发人id
            forwardMemberId = affairs.get(0).getForwardMember();
            if (Strings.isNotBlank(forwardMemberId)) {
                forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                forwardMemberFlag = 1;
            }
        }
        String resouce = Constants.DEFAULT_MESSAGE_RESOURCE;
        Locale locale = LocaleContext.parseLocale("zh_CN");
        ResourceBundle rb = ResourceBundleUtil.getResourceBundle(resouce, locale);
        Object[] params = { name, subject, forwardMemberFlag, forwardMember, additional_remarkFlag, content };
        // 按本地国际化将发送的内容处理
        String newContent = ResourceBundleUtil.getString(rb, "col.hasten", params);
        return newContent;
    }
	
	
	//是否允许具备发送短信的权限
	@Override
	public boolean isCanSendPhoneMessage(long userId, long accountId) {
		boolean result = false;
		if(SystemEnvironment.hasPlugin("sms") && mobileMessageManager!=null){
			/*
			 * 需求变更-接口变更  
			 * 任会阳 2014-06-16 20:52  
			 * 不用判断是否有短信插件，短信插件系统必有
			 */
//				 if(SystemEnvironment.hasPlugin("sms") && mobileMessageManager!=null){
//				 if(mobileMessageManager.isCanSend(userId, accountId)){
//				 result = true;
//				 }
//				 }
			//只需要判断是否可以发送短信即可！
			if(mobileMessageManager.isCanUseSMS()){
			result = true;
			}
		}
		return result;
	}

	@Override
    public boolean hasten(String processId, String activityId, List<Long> personIds, String superviseId,
            String content) {

        String _superviseId = superviseId;
        boolean retValue = false;
        try {
            EdocSummary summary = edocManager.getSummaryByProcessId(processId);
            if (Strings.isBlank(_superviseId)) {
                CtpSuperviseDetail detail = superviseManager.getSupervise(summary.getId());
                if (detail != null)// 即使superviseId为空，也要从数据库中查询一次，当CtpSuperviseDetail有值时赋值，没值则不是督办人，传入空值
                {
                    _superviseId = detail.getId().toString();
                }
            }
            SuperviseHastenParam sParam = new SuperviseHastenParam();
            sParam.setActivityId(activityId);
            sParam.setContent(content);
            sParam.setMemberId(summary.getStartUserId());
            sParam.setPersonIds(personIds);
            sParam.setSummaryId(summary.getId());
            sParam.setSuperviseId(_superviseId);
            sParam.setTitle(summary.getSubject());
            retValue = superviseManager.transHasten(sParam);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return retValue;
    }

	@Override
	public boolean isOverTime(String arg0, String arg1) {
		 if(Strings.isBlank(arg0) || Strings.isBlank(arg1)){
		        return false;
		    }
		    Long processId = Long.parseLong(arg0);
		    Long activityId = Long.parseLong(arg1);
		    try {
	            EdocSummary summary = edocManager.getSummaryByProcessId(processId.toString());
	            if(summary!=null){
	                Map<String,Long> map = new HashMap<String,Long>();
	                map.put("objectId", summary.getId());
	                map.put("activityId", activityId);
	                List<CtpAffair> list = affairManager.getByConditions(null, map);
	                if(Strings.isNotEmpty(list)){
	                    CtpAffair affair = list.get(0);
	                    if(!affair.isCoverTime()){
	                    	//定时器任务不准确，重新计算
	                    	Date edate = affair.getExpectedProcessTime();
	                    	if(edate == null) {
	                    		return false;
	                    	}
	                    	boolean isOverTime = false;
	                    	Date finishDate = affair.getCompleteTime();
	                    	if(finishDate == null){
	                    		if(edate.after(new Date())){
	                    			isOverTime = false;
	                    		}else{
	                    			isOverTime = true;
	                    		}
	                    	}else{
	                    		if(edate.after(finishDate)){
	                    			isOverTime = false;
	                    		}else{
	                    			isOverTime = true;
	                    		}
	                    	}
	                    	return isOverTime;
	                    }
	                    return affair.isCoverTime();
	                }else{
	                    return false;
	                }
	            }
	        } catch (BusinessException e) {
	        	LOGGER.error("", e);
	        }
			return false;
	}

	@Override
	public void sendSupervisorMsgAndRecordAppLog(String caseId) {
    	//给相关人员发送消息
		V3xOrgMember member = null;
    	EdocSummary edocSummary;
    	User user=AppContext.getCurrentUser();
    	String subject= "";
		try {
			member = orgManager.getMemberById(user.getId());
			if(member == null){
				return ;
			}
			
			String memname = OrgHelper.showMemberNameOnly(user.getId());
			
			edocSummary = edocManager.getSummaryByCaseId(Long.parseLong(caseId));
			subject = edocSummary.getSubject();
			if(!edocSummary.getStartUserId().equals(user.getId())){
            	ApplicationCategoryEnum app = EdocUtil.getAppCategoryByEdocType(edocSummary.getEdocType());
            	Integer importantLevel = edocSummary.getImportantLevel();
            	MessageReceiver receiver = new MessageReceiver(edocSummary.getId(), Long.valueOf(edocSummary.getStartUserId()));
            	userMessageManager.sendSystemMessage(new MessageContent("edoc.supervise.workflow.update", edocSummary.getSubject(), memname,app.ordinal()).setImportantLevel(importantLevel),
            			app, 
            			member.getId(), 
            			receiver,
            			EdocMessageFilterParamEnum.supervise.key);
			}
			appLogManager.insertLog(user, AppLogAction.Edoc_FlowPermModify, user.getName(), subject);
		} catch (Throwable e1) {
			LOGGER.warn(e1);
		}
    }
	
	@Override
    public String[] getNodeReceiveAndDealedTime(String processId, String nodeId) {
		String[] str = new String[]{"","","false",""};
        try {
            EdocSummary summary = edocManager.getSummaryByProcessId(processId.toString());
            if(summary!=null){
                List<CtpAffair> list = affairManager.getAffairsByObjectIdAndNodeId(summary.getId(), Long.parseLong(nodeId));
                if(null==list || list.isEmpty()){
                    return str;
                }
                CtpAffair affair = null;
                for(CtpAffair c : list){
                    Integer state = c.getState();
                    if(state != null
                            && state != StateEnum.col_cancel.getKey()
                            && state != StateEnum.col_stepBack.getKey()
                            && state != StateEnum.col_takeBack.getKey()
                            && state != StateEnum.col_competeOver.getKey()
                            && state != StateEnum.edoc_exchange_withdraw.getKey()){
                           
                        affair = c;
                        break;
                    }
                }
                
                //不是有效的Affair
                if(affair == null){
                    return str;
                }
                
                Date receiveTime = affair.getReceiveTime();
                if(receiveTime != null){
                    str[0] = Datetimes.format(receiveTime, "yyyy-MM-dd HH:mm:ss");
                }
                Date completeTime = affair.getCompleteTime();
                if(Integer.valueOf(SubStateEnum.col_pending_ZCDB.getKey()).equals(affair.getSubState())){
                    completeTime = affair.getUpdateDate();
                }
                if(completeTime != null){
                    str[1] = Datetimes.format(completeTime, "yyyy-MM-dd HH:mm:ss");
                }
                Boolean isOverTime = affair.isCoverTime();
                if(isOverTime != null){
                    str[2] = isOverTime.toString();
                }
                Date expectedProcessTime= affair.getExpectedProcessTime();
                if(null!=expectedProcessTime){
                	str[3] = Datetimes.format(expectedProcessTime, "yyyy-MM-dd HH:mm:ss");
                }
            }
        } catch (NumberFormatException e) {
        	LOGGER.error("", e);
        } catch (BusinessException e) {
        	LOGGER.error("", e);
        }
        return str;
    }

    /* (non-Javadoc)
     * @see com.seeyon.ctp.workflow.wapi.WorkFlowAppExtendAbstractManager#getFormData(java.lang.String)
     */
    @Override
    public Map<String, Object> getFormValueMap(WorkflowBpmContext context, String rightId) {
    	 
    	Map<String, Object> edocFormData= new HashMap<String, Object>();
    	 if(context==null){
    		return edocFormData;
    	 }
    	 
    	
       try{
           //先以新公文的方式获取参数获取不到再获取页面的上的参数,再获取数据库中的参数
    	   //获取新公文的参数
		   edocFormData = getGovFormData(context);
		   if(edocFormData==null || edocFormData.isEmpty()){
			  
		    	String formData= context.getFormData();
		    	
		    	//获取页面上的参数
		    	if(Strings.isNotBlank(formData) && !Strings.equals(formData, "-1")){
		    		edocFormData = getPageFormData(context);
		    	}
		    	
		    	//获取数据库中的参数
		    	if(edocFormData==null){
		    		 String summaryIdStr = context.getBussinessId();
		      	   if(Strings.isBlank(summaryIdStr)){
		      		   return edocFormData;
		      	   }
		      	   Long summaryId= Long.parseLong(summaryIdStr);
		      	   EdocSummary edocSummary= edocManager.getEdocSummaryById(summaryId, false);
		    	   edocFormData = getDbFormData(edocSummary);
		    	}
		    }
    	
     }catch(Throwable e){
    	 LOGGER.warn("", e);
     }
     
    return edocFormData;
  }
 private Map<String, Object> getGovFormData(WorkflowBpmContext context){
	 Map<String, Object> edocFormData= new HashMap<String, Object>();
	 String masterId = context.getMastrid();
 	String formAppId = context.getFormData();
 	if(masterId==null || formAppId==null){
 		return edocFormData;
 	}
 	masterId = masterId.trim();
 	formAppId = formAppId.trim();
 	if(Strings.isDigits(formAppId) && Strings.isDigits(masterId)){
 		
 	    WorkflowFormDataMapManager formDataManager = wapi.getWorkflowFormDataMapManager("form");
 		edocFormData = formDataManager.getFormValueMap(Long.parseLong(formAppId), Long.parseLong(masterId), null);
 	}
 	
 	return edocFormData;
 }
 
 private Map<String,Object> getPageFormData(WorkflowBpmContext context){
	 String formData= context.getFormData();
     LOGGER.info("当前公文单数据formData:="+formData);
     Map<String,Object> edocFormDataPage = null;
     if(Strings.isNotBlank(formData) && !Strings.equals(formData, "-1")){
         
         edocFormDataPage = new HashMap<String, Object>();
         
     	Map<String,Map<String,String>> edocFormData1 = (Map<String,Map<String,String>>)JSONUtil.parseJSONString(formData);
         if(null!=edocFormData1){
             Set<String> fieldSet= edocFormData1.keySet();
             for (String fieldName : fieldSet) {
                 Map<String,String> aFiledValueAndTypeMap= edocFormData1.get(fieldName);
                 String value= aFiledValueAndTypeMap.get("value");
                 String type= aFiledValueAndTypeMap.get("type");
                 
                 if("varchar".equals(type)){
                 	edocFormDataPage.put(fieldName, value);
                 }else if("int".equals(type)){
                     value = value.trim();
                     if(null!=value && !"".equals(value) && !"null".equals(value) && !"undefined".equals(value)){
                         try{
                         	edocFormDataPage.put(fieldName, Integer.parseInt(value));
                         }catch(Throwable e){
                        	 LOGGER.warn("edoc filed int type is error:"+value);
                             edocFormDataPage.put(fieldName, value);
                         }
                     }else{
                     	edocFormDataPage.put(fieldName, null);
                     }
                 }else if("decimal".equals(type)){
                     value = value.trim();
                     if(null!=value && !"".equals(value) && !"null".equals(value) && !"undefined".equals(value)){
                         try{
                         	edocFormDataPage.put(fieldName, Double.parseDouble(value));
                         }catch(Throwable e){
                        	 LOGGER.warn("edoc filed decimal type  is error:"+value);
                         	edocFormDataPage.put(fieldName, value);
                         }
                     }else{
                     	edocFormDataPage.put(fieldName, null);
                     }
                 }else if("date".equals(type)){
                 	value = value.trim();
                     if(null!=value && !"".equals(value) && !"null".equals(value) && !"undefined".equals(value)){
                         try{
                         	edocFormDataPage.put(fieldName, Datetimes.parse(value));
                         }catch(Throwable e){
                        	 LOGGER.warn("edoc filed date type is error:"+value);
                         	edocFormDataPage.put(fieldName, null);
                         }
                     }else{
                     	edocFormDataPage.put(fieldName, null);
                     }
                 }else{
                 	edocFormDataPage.put(fieldName, value);
                 }
             }
         }
     }
     
     LOGGER.info("当前公文单数据edocFormDataPage:="+edocFormDataPage);
     return edocFormDataPage;
 }
 
 private Map<String,Object> getDbFormData(EdocSummary edocSummary){
	 Map<String, Object> edocFormData= new HashMap<String, Object>();
     List<EdocFormElement> elements= edocFormManager.getEdocFormElementByFormId(edocSummary.getFormId());
     EdocFormElement formElement = null;
     String fieldName= "";
     Object fieldValue= null;
     String branchFieldDbType="varchar";
     for(int i=0;i<elements.size();i++){
         fieldName="";
         fieldValue= null;
         branchFieldDbType="";
         formElement = elements.get(i);
         DecimalFormat df =  new DecimalFormat("###########0.####");
         if(formElement.getElementId()==1){
             fieldName = "subject";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getSubject()){
                 fieldValue = edocSummary.getSubject();
             }
         }
         else if(formElement.getElementId()==2){
             fieldName = "doc_type";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getDocType())){
                 fieldValue = edocSummary.getDocType();
             }
         }
         else if(formElement.getElementId()==3){
             fieldName = "send_type";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getSendType())){
                 fieldValue = edocSummary.getSendType();
             }
         }
         else if(formElement.getElementId()==4){
             fieldName = "doc_mark"; 
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getDocMark()){
                 String docMark = edocSummary.getDocMark();
                 fieldValue = docMark;
             } 
         }
         else if(formElement.getElementId()==21){
             fieldName = "doc_mark2";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getDocMark2()){
                 fieldValue = edocSummary.getDocMark2();
             }
         }
         else if(formElement.getElementId()==5){
             fieldName = "serial_no";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if( null!=edocSummary && null!=edocSummary.getSerialNo()){
                 fieldValue = edocSummary.getSerialNo();
             }
         }
         else if(formElement.getElementId()==6){
             fieldName = "secret_level";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getSecretLevel())){
                 fieldValue = edocSummary.getSecretLevel();
             }
         }
         else if(formElement.getElementId()==7){
             fieldName = "urgent_level";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getUrgentLevel())){
                 fieldValue = edocSummary.getUrgentLevel();
             } 
         }
         else if(formElement.getElementId()==350){//公文级别
             fieldName = "unit_level";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getUnitLevel())){
                 fieldValue = edocSummary.getUnitLevel();
             } 
         }
         else if(formElement.getElementId()==8){
             fieldName = "keep_period";
             branchFieldDbType ="int";
             if(null!=edocSummary && edocSummary.getKeepPeriod()!=null){
                 fieldValue = String.valueOf(edocSummary.getKeepPeriod());
             }
         }
         else if(formElement.getElementId()==9){
             fieldName = "create_person";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getCreatePerson()){
                 fieldValue = edocSummary.getCreatePerson();
             }
         }
         else if(formElement.getElementId()==10){
             fieldName = "send_unit";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getSendUnit()){
                 fieldValue = edocSummary.getSendUnit();
             }
         }
         else if(formElement.getElementId()==26){
             fieldName = "send_unit2";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getSendUnit2()){
                 fieldValue = edocSummary.getSendUnit2();
             }
         }
         else if(formElement.getElementId()==311) {
             fieldName = "attachments";
             fieldValue = "";
             if(null!=edocSummary && null!=edocSummary.getAttachments()){
                 fieldValue = edocSummary.getAttachments();
             }
         }
         else if(formElement.getElementId()==312){
             branchFieldDbType ="varchar";
             fieldName = "send_department";
             fieldValue = "";
             String department= (edocSummary==null?null:edocSummary.getSendDepartment());
             if(Strings.isNotBlank(department)){
                 fieldValue =department; 
             }
         } 
         else if(formElement.getElementId()==313){
             branchFieldDbType ="varchar";
             fieldName = "send_department2";
             fieldValue = "";
             String department= (edocSummary==null?null:edocSummary.getSendDepartment2());
             if(Strings.isNotBlank(department)){
                 fieldValue =department; 
             }
         }
         else if(formElement.getElementId()==11){
             fieldName = "issuer";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getIssuer()){
                 fieldValue = edocSummary.getIssuer();
             }
         }
         else if(formElement.getElementId()==12){
             fieldName = "signing_date";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getSigningDate()){
                 fieldValue = edocSummary.getSigningDate();                    
             }
         }
         else if(formElement.getElementId()==13){
             fieldName = "send_to";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getSendTo()){
                 fieldValue = edocSummary.getSendTo();
             }
         }
         else if(formElement.getElementId()==23){
             fieldName = "send_to2";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getSendTo2()){
                 fieldValue = edocSummary.getSendTo2();
             }
         }
         else if(formElement.getElementId()==14){
             fieldName = "copy_to";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getCopyTo()){
                 fieldValue = edocSummary.getCopyTo();
             }
         }
         else if(formElement.getElementId()==24){
             fieldName = "copy_to2";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getCopyTo2()){
                 fieldValue = edocSummary.getCopyTo2();
             }
         }
         else if(formElement.getElementId()==15){
             fieldName = "report_to";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getReportTo()){
                 fieldValue = edocSummary.getReportTo();
             }
         }
         else if(formElement.getElementId()==25){
             fieldName = "report_to2";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getReportTo2()){
                 fieldValue = edocSummary.getReportTo2();
             }
         }
         else if(formElement.getElementId()==16){
             fieldName = "keyword";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getKeywords()){
                 fieldValue = edocSummary.getKeywords();
             }
         }
         else if(formElement.getElementId()==17){
             fieldName = "print_unit";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getPrintUnit()){
                 fieldValue = edocSummary.getPrintUnit();
             }
         }
         else if(formElement.getElementId()==18){
             fieldName = "copies";
             fieldValue = "";
             branchFieldDbType ="int";
             if(null!=edocSummary && null!=edocSummary.getCopies()){
                 fieldValue = String.valueOf(edocSummary.getCopies());
             }
         }
         else if(formElement.getElementId()==22){
             fieldName = "copies2";
             fieldValue = "";
             branchFieldDbType ="int";
             if(null != edocSummary && null != edocSummary.getCopies2()){
                 fieldValue = String.valueOf(edocSummary.getCopies2());
             }
         }
         else if(formElement.getElementId()==19){
             fieldName = "printer";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getPrinter()){
                 fieldValue = edocSummary.getPrinter();
             }
         }
         else if(formElement.getElementId()==201){
             fieldName = "createdate";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getStartTime()){
                 fieldValue = edocSummary.getStartTime();
             }
         }
         else if(formElement.getElementId()==202){
             fieldName = "packdate";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getPackTime()){
                 fieldValue = edocSummary.getPackTime();                   
             }
         }
         else if(formElement.getElementId()==320){
             fieldName = "filesm";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getFilesm()){
                 fieldValue = edocSummary.getFilesm();
             }
         }
         else if(formElement.getElementId()==321){
             fieldName = "filefz";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getFilefz()){
                 fieldValue = edocSummary.getFilefz();
             }
         }
         else if(formElement.getElementId()==322){
             fieldName = "phone";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getPhone()){
                 fieldValue = edocSummary.getPhone();
             }
         }
         else if(formElement.getElementId()==325){
             fieldName = "party";  
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getParty())){
                 fieldValue = edocSummary.getParty();  
             }  
         }
         else if(formElement.getElementId()==326){
             fieldName = "administrative";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getAdministrative())){
                 fieldValue = edocSummary.getAdministrative();
             }
         }
         //签收日期
         else if(formElement.getElementId()==329){
             fieldName = "receipt_date";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getReceiptDate()){
                 fieldValue = edocSummary.getReceiptDate();                    
             }
         }
         //登记日期
         else if(formElement.getElementId()==330){
             fieldName = "registration_date";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getRegistrationDate()){
                 fieldValue = edocSummary.getRegistrationDate();                    
             }
         }
         //审核人
         else if(formElement.getElementId()==331){
             fieldName = "auditor";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getAuditor()){
                 fieldValue = edocSummary.getAuditor();
             }
         }
         //复核人
         else if(formElement.getElementId()==332){
             fieldName = "review";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getReview()){
                 fieldValue = edocSummary.getReview();
             }
         }
         //承办人
         else if(formElement.getElementId()==333){
             fieldName = "undertaker";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getUndertaker()){
                 fieldValue = edocSummary.getUndertaker();
             }
         }
       //承办机构
         else if(formElement.getElementId()==349){
             fieldName = "undertakenoffice";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(edocSummary != null && edocSummary.getUndertakenoffice() != null){
                 fieldValue = edocSummary.getUndertakenoffice();
             }
         }
         else if(formElement.getElementId()==51){
             fieldName = "string1";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getVarchar1()){
                 fieldValue = edocSummary.getVarchar1();
             }
         }
         else if(formElement.getElementId()==52){
             fieldName = "string2";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getVarchar2()){
                 fieldValue = edocSummary.getVarchar2();
             }
         }
         else if(formElement.getElementId()==53){
             fieldName = "string3";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getVarchar3()){
                 fieldValue = edocSummary.getVarchar3();
             }
         }
         else if(formElement.getElementId()==54){
             fieldName = "string4";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getVarchar4()){
                 fieldValue = edocSummary.getVarchar4();
             }
         }
         else if(formElement.getElementId()==55){
             fieldName = "string5";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getVarchar5()){
                 fieldValue = edocSummary.getVarchar5();
             }
         }
         else if(formElement.getElementId()==56){
             fieldName = "string6";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getVarchar6()){
                 fieldValue = edocSummary.getVarchar6();
             }
         }
         else if(formElement.getElementId()==57){
             fieldName = "string7";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getVarchar7()){
                 fieldValue = edocSummary.getVarchar7();
             }
         }
         else if(formElement.getElementId()==58){
             fieldName = "string8";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getVarchar8()){
                 fieldValue = edocSummary.getVarchar8();
             }
         }
         else if(formElement.getElementId()==59){
             fieldName = "string9";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getVarchar9()){
                 fieldValue = edocSummary.getVarchar9();
             }
         }
         else if(formElement.getElementId()==60){
             fieldName = "string10";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if( null!=edocSummary && null!=edocSummary.getVarchar10()){
                 fieldValue = edocSummary.getVarchar10();
             }
         }
         else if(formElement.getElementId()==241){
             fieldName = "string11";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if( null!=edocSummary && null!=edocSummary.getVarchar11()){
                 fieldValue = edocSummary.getVarchar11();
             }
         }
         else if(formElement.getElementId()==242){
             fieldName = "string12";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if( null!=edocSummary && null!=edocSummary.getVarchar12()){
                 fieldValue = edocSummary.getVarchar12();
             }
         }
         else if(formElement.getElementId()==243){
             fieldName = "string13";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if( null!=edocSummary && null!=edocSummary.getVarchar13()){
                 fieldValue = edocSummary.getVarchar13();
             }
         }
         else if(formElement.getElementId()==244){
             fieldName = "string14";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if( null!=edocSummary && null!=edocSummary.getVarchar14()){
                 fieldValue = edocSummary.getVarchar14();
             }
         }
         else if(formElement.getElementId()==245){
             fieldName = "string15";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if( null!=edocSummary && null!=edocSummary.getVarchar15()){
                 fieldValue = edocSummary.getVarchar15();
             }
         }
         else if(formElement.getElementId()==246){
             fieldName = "string16";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if( null!=edocSummary && null!=edocSummary.getVarchar16()){
                 fieldValue = edocSummary.getVarchar16();
             }
         }
         else if(formElement.getElementId()==247){
             fieldName = "string17";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if( null!=edocSummary && null!=edocSummary.getVarchar17()){
                 fieldValue = edocSummary.getVarchar17();
             }
         }
         else if(formElement.getElementId()==248){
             fieldName = "string18";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if( null!=edocSummary && null!=edocSummary.getVarchar18()){
                 fieldValue = edocSummary.getVarchar18();
             }
         }
         else if(formElement.getElementId()==249){
             fieldName = "string19";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if( null!=edocSummary && null!=edocSummary.getVarchar19()){
                 fieldValue = edocSummary.getVarchar19();
             }
         }
         else if(formElement.getElementId()==250){
             fieldName = "string20";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if( null!=edocSummary && null!=edocSummary.getVarchar20()){
                 fieldValue = edocSummary.getVarchar20();
             }
         }
         else if(formElement.getElementId()==61){
             fieldName = "text1";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getText1()){
                 fieldValue = edocSummary.getText1();
             }
         }
         else if(formElement.getElementId()==62){
             fieldName = "text2";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getText2()){
                 fieldValue = edocSummary.getText2();
             }
         }
         else if(formElement.getElementId()==63){
             fieldName = "text3";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getText3()){
                 fieldValue = edocSummary.getText3();
             }
         }
         else if(formElement.getElementId()==64){
             fieldName = "text4";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getText4()){
                 fieldValue = edocSummary.getText4();
             }
         }
         else if(formElement.getElementId()==65){
             fieldName = "text5";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getText5()){
                 fieldValue = edocSummary.getText5();
             }
         }
         else if(formElement.getElementId()==66){
             fieldName = "text6";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getText6()){
                 fieldValue = edocSummary.getText6();
             }
         }
         else if(formElement.getElementId()==67){
             fieldName = "text7";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getText7()){
                 fieldValue = edocSummary.getText7();
             }
         }
         else if(formElement.getElementId()==68){
             fieldName = "text8";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getText8()){
                 fieldValue = edocSummary.getText8();
             }
         }
         else if(formElement.getElementId()==69){
             fieldName = "text9";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getText9()){
                 fieldValue = edocSummary.getText9();
             }
         }
         else if(formElement.getElementId()==70){
             fieldName = "text10";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getText10()){
                 fieldValue = edocSummary.getText10();
             }
         }
         else if(formElement.getElementId()==71){
             fieldName = "integer1";
             fieldValue = "";
             branchFieldDbType ="int";
             if(null!=edocSummary && null!=edocSummary.getInteger1()){
                 fieldValue = String.valueOf(edocSummary.getInteger1());
             }
         }
         else if(formElement.getElementId()==72){
             fieldName = "integer2";
             fieldValue = "";
             branchFieldDbType ="int";
             if(null!=edocSummary && null!=edocSummary.getInteger2()){
                 fieldValue = String.valueOf(edocSummary.getInteger2());
             }
         }
         else if(formElement.getElementId()==73){
             fieldName = "integer3";
             fieldValue = "";
             branchFieldDbType ="int";
             if(null!=edocSummary && null!=edocSummary.getInteger3()){
                 fieldValue = String.valueOf(edocSummary.getInteger3());
             }
         }
         else if(formElement.getElementId()==74){
             fieldName = "integer4";
             fieldValue = "";
             branchFieldDbType ="int";
             if(null!=edocSummary && null!=edocSummary.getInteger4()){
                 fieldValue = String.valueOf(edocSummary.getInteger4());
             }
         }
         else if(formElement.getElementId()==75){
             fieldName = "integer5";
             fieldValue = "";
             branchFieldDbType ="int";
             if(null!=edocSummary && null!=edocSummary.getInteger5()){
                 fieldValue = String.valueOf(edocSummary.getInteger5());
             }
         }
         else if(formElement.getElementId()==76){
             fieldName = "integer6";
             branchFieldDbType ="int";
             if(null!=edocSummary && null!=edocSummary.getInteger6()){
                 fieldValue = String.valueOf(edocSummary.getInteger6());
             }
         }
         else if(formElement.getElementId()==77){
             fieldName = "integer7";
             fieldValue = "";
             branchFieldDbType ="int";
             if(null!=edocSummary && null!=edocSummary.getInteger7()){
                 fieldValue = String.valueOf(edocSummary.getInteger7());
             }
         }
         else if(formElement.getElementId()==78){
             fieldName = "integer8";
             fieldValue = "";
             branchFieldDbType ="int";
             if(null!=edocSummary && null!=edocSummary.getInteger8()){
                 fieldValue = String.valueOf(edocSummary.getInteger8());
             }
         }
         else if(formElement.getElementId()==79){
             fieldName = "integer9";
             fieldValue = "";
             branchFieldDbType ="int";
             if(null!=edocSummary && null!=edocSummary.getInteger9()){
                 fieldValue = String.valueOf(edocSummary.getInteger9());
             }
         }
         else if(formElement.getElementId()==80){
             fieldName = "integer10";
             fieldValue = "";
             branchFieldDbType ="int";
             if(null!=edocSummary && null!=edocSummary.getInteger10()){
                 fieldValue = String.valueOf(edocSummary.getInteger10());
             }
         }
         else if(formElement.getElementId()==231){
             fieldName = "integer11";
             fieldValue = "";
             branchFieldDbType ="int";
             if(null!=edocSummary && null!=edocSummary.getInteger11()){
                 fieldValue = String.valueOf(edocSummary.getInteger11());
             }
         }
         else if(formElement.getElementId()==232){
             fieldName = "integer12";
             fieldValue = "";
             branchFieldDbType ="int";
             if(null!=edocSummary && null!=edocSummary.getInteger12()){
                 fieldValue = String.valueOf(edocSummary.getInteger12());
             }
         }
         else if(formElement.getElementId()==233){
             fieldName = "integer13";
             fieldValue = "";
             branchFieldDbType ="int";
             if(null!=edocSummary && null!=edocSummary.getInteger13()){
                 fieldValue = String.valueOf(edocSummary.getInteger13());
             }
         }
         else if(formElement.getElementId()==234){
             fieldName = "integer14";
             fieldValue = "";
             branchFieldDbType ="int";
             if(null!=edocSummary && null!=edocSummary.getInteger14()){
                 fieldValue = String.valueOf(edocSummary.getInteger14());
             }
         }
         else if(formElement.getElementId()==235){
             fieldName = "integer15";
             fieldValue = "";
             branchFieldDbType ="int";
             if(null!=edocSummary && null!=edocSummary.getInteger15()){
                 fieldValue = String.valueOf(edocSummary.getInteger15());
             }
         }
         else if(formElement.getElementId()==236){
             fieldName = "integer16";
             fieldValue = "";
             branchFieldDbType ="int";
             if(null!=edocSummary && null!=edocSummary.getInteger16()){
                 fieldValue = String.valueOf(edocSummary.getInteger16());
             }
         }
         else if(formElement.getElementId()==237){
             fieldName = "integer17";
             fieldValue = "";
             branchFieldDbType ="int";
             if(null!=edocSummary && null!=edocSummary.getInteger17()){
                 fieldValue = String.valueOf(edocSummary.getInteger17());
             }
         }
         else if(formElement.getElementId()==238){
             fieldName = "integer18";
             fieldValue = "";
             branchFieldDbType ="int";
             if(null!=edocSummary && null!=edocSummary.getInteger18()){
                 fieldValue = String.valueOf(edocSummary.getInteger18());
             }
         }
         else if(formElement.getElementId()==239){
             fieldName = "integer19";
             fieldValue = "";
             branchFieldDbType ="int";
             if(null!=edocSummary && null!=edocSummary.getInteger19()){
                 fieldValue = String.valueOf(edocSummary.getInteger19());
             }
         }
         else if(formElement.getElementId()==240){
             fieldName = "integer20";
             fieldValue = "";
             branchFieldDbType ="int";
             if(null!=edocSummary && null!=edocSummary.getInteger20()){
                 fieldValue = String.valueOf(edocSummary.getInteger20());
             }
         }
         
         else if(formElement.getElementId()==81){
             fieldName = "decimal1"; 
             fieldValue = "";
             branchFieldDbType ="decimal";
             if(null!=edocSummary && null!=edocSummary.getDecimal1()){
                 fieldValue = df.format(edocSummary.getDecimal1());
             }
         }
         else if(formElement.getElementId()==82){
             fieldName = "decimal2";
             fieldValue = "";
             branchFieldDbType ="decimal";
             if(null!=edocSummary && null!=edocSummary.getDecimal2()){
                 fieldValue = df.format(edocSummary.getDecimal2());
             }
         }
         else if(formElement.getElementId()==83){
             fieldName = "decimal3";
             fieldValue = "";
             branchFieldDbType ="decimal";
             if(null!=edocSummary && null!=edocSummary.getDecimal3()){
                 fieldValue = df.format(edocSummary.getDecimal3());
             }
         }
         else if(formElement.getElementId()==84){
             fieldName = "decimal4";
             fieldValue = "";
             branchFieldDbType ="decimal";
             if(null!=edocSummary && null!=edocSummary.getDecimal4()){
                 fieldValue = df.format(edocSummary.getDecimal4());
             }
         }
         else if(formElement.getElementId()==85){
             fieldName = "decimal5";
             fieldValue = "";
             branchFieldDbType ="decimal";
             if(null!=edocSummary && null!=edocSummary.getDecimal5()){
                 fieldValue = df.format(edocSummary.getDecimal5());
             }
         }
         else if(formElement.getElementId()==86){
             fieldName = "decimal6";
             fieldValue = "";
             branchFieldDbType ="decimal";
             if(null!=edocSummary && null!=edocSummary.getDecimal6()){
                 fieldValue = df.format(edocSummary.getDecimal6());
             }
         }
         else if(formElement.getElementId()==87){
             fieldName = "decimal7";
             fieldValue = "";
             branchFieldDbType ="decimal";
             if(null!=edocSummary && null!=edocSummary.getDecimal7()){
                 fieldValue = df.format(edocSummary.getDecimal7());
             }
         }
         else if(formElement.getElementId()==88){
             fieldName = "decimal8";
             fieldValue = "";
             branchFieldDbType ="decimal";
             if(null!=edocSummary && null!=edocSummary.getDecimal8()){
                 fieldValue = df.format(edocSummary.getDecimal8());
             }
         }
         else if(formElement.getElementId()==89){
             fieldName = "decimal9";
             fieldValue = "";
             branchFieldDbType ="decimal";
             if(null!=edocSummary && null!=edocSummary.getDecimal9()){
                 fieldValue = df.format(edocSummary.getDecimal9());
             }
         }
         else if(formElement.getElementId()==90){
             fieldName = "decimal10";
             fieldValue = "";
             branchFieldDbType ="decimal";
             if(null!=edocSummary && null!=edocSummary.getDecimal10()){
                 fieldValue = df.format(edocSummary.getDecimal10());
             }
         }
         else if(formElement.getElementId()==251){
             fieldName = "decimal11";
             fieldValue = "";
             branchFieldDbType ="decimal";
             if(null!=edocSummary && null!=edocSummary.getDecimal11()){
                 fieldValue = df.format(edocSummary.getDecimal11());
             }
         }
         else if(formElement.getElementId()==252){
             fieldName = "decimal12";
             fieldValue = "";
             branchFieldDbType ="decimal";
             if(null!=edocSummary && null!=edocSummary.getDecimal12()){
                 fieldValue = df.format(edocSummary.getDecimal12());
             }
         }
         else if(formElement.getElementId()==253){
             fieldName = "decimal13";
             fieldValue = "";
             branchFieldDbType ="decimal";
             if(null!=edocSummary && null!=edocSummary.getDecimal13()){
                 fieldValue = df.format(edocSummary.getDecimal13());
             }
         }
         else if(formElement.getElementId()==254){
             fieldName = "decimal14";
             fieldValue = "";
             branchFieldDbType ="decimal";
             if(null!=edocSummary && null!=edocSummary.getDecimal14()){
                 fieldValue = df.format(edocSummary.getDecimal14());
             }
         }
         else if(formElement.getElementId()==255){
             fieldName = "decimal15";
             fieldValue = "";
             branchFieldDbType ="decimal";
             if(null!=edocSummary && null!=edocSummary.getDecimal15()){
                 fieldValue = df.format(edocSummary.getDecimal15());
             }
         }
         else if(formElement.getElementId()==256){
             fieldName = "decimal16";
             fieldValue = "";
             branchFieldDbType ="decimal";
             if(null!=edocSummary && null!=edocSummary.getDecimal16()){
                 fieldValue = df.format(edocSummary.getDecimal16());
             }
         }
         else if(formElement.getElementId()==257){
             fieldName = "decimal17";
             fieldValue = "";
             branchFieldDbType ="decimal";
             if(null!=edocSummary && null!=edocSummary.getDecimal17()){
                 fieldValue = df.format(edocSummary.getDecimal17());
             }
         }
         else if(formElement.getElementId()==258){
             fieldName = "decimal18";
             fieldValue = "";
             branchFieldDbType ="decimal";
             if(null!=edocSummary && null!=edocSummary.getDecimal18()){
                 fieldValue = df.format(edocSummary.getDecimal18());
             }
         }
         else if(formElement.getElementId()==259){
             fieldName = "decimal19";
             fieldValue = "";
             branchFieldDbType ="decimal";
             if(null!=edocSummary && null!=edocSummary.getDecimal19()){
                 fieldValue = df.format(edocSummary.getDecimal19());
             }
         }
         else if(formElement.getElementId()==260){
             fieldName = "decimal20";
             fieldValue = "";
             branchFieldDbType ="decimal";
             if(null!=edocSummary && null!=edocSummary.getDecimal20()){
                 fieldValue = df.format(edocSummary.getDecimal20());
             }
         }
         else if(formElement.getElementId()==91){
             fieldName = "date1";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getDate1()){
                 fieldValue = edocSummary.getDate1();
             }
         }
         else if(formElement.getElementId()==92){
             fieldName = "date2";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getDate2()){
                 fieldValue = edocSummary.getDate2();
             }
         }
         else if(formElement.getElementId()==93){
             fieldName = "date3";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getDate3()){
                 fieldValue = edocSummary.getDate3();                  
             }
         }
         else if(formElement.getElementId()==94){
             fieldName = "date4";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getDate4()){
                 fieldValue = edocSummary.getDate4();
             }
         }
         else if(formElement.getElementId()==95){
             fieldName = "date5";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getDate5()){
                 fieldValue = edocSummary.getDate5();
             }
         }
         else if(formElement.getElementId()==96){
             fieldName = "date6";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getDate6()){
                 fieldValue = edocSummary.getDate6();
             }
         }
         else if(formElement.getElementId()==97){
             fieldName = "date7";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getDate7()){
                 fieldValue = edocSummary.getDate7();
             }
         }
         else if(formElement.getElementId()==98){
             fieldName = "date8";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getDate8()){
                 fieldValue = edocSummary.getDate8();
             }
         }
         else if(formElement.getElementId()==99){
             fieldName = "date9";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getDate9()){
                 fieldValue = edocSummary.getDate9();
             }
         }
         else if(formElement.getElementId()==100){
             fieldName = "date10";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getDate10()){
                 fieldValue = edocSummary.getDate10();
             }
         }
         else if(formElement.getElementId()==271){
             fieldName = "date11";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getDate11()){
                 fieldValue = edocSummary.getDate11();
             }
         }
         else if(formElement.getElementId()==272){
             fieldName = "date12";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getDate12()){
                 fieldValue = edocSummary.getDate12();
             }
         }
         else if(formElement.getElementId()==273){
             fieldName = "date13";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getDate13()){
                 fieldValue = edocSummary.getDate13();
             }
         }
         else if(formElement.getElementId()==274){
             fieldName = "date14";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getDate14()){
                 fieldValue = edocSummary.getDate14();
             }
         }
         else if(formElement.getElementId()==275){
             fieldName = "date15";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getDate15()){
                 fieldValue = edocSummary.getDate15();
             }
         }
         else if(formElement.getElementId()==276){
             fieldName = "date16";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getDate16()){
                 fieldValue = edocSummary.getDate16();
             }
         }
         else if(formElement.getElementId()==277){
             fieldName = "date17";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getDate17()){
                 fieldValue = edocSummary.getDate17();
             }
         }
         else if(formElement.getElementId()==278){
             fieldName = "date18";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getDate18()){
                 fieldValue = edocSummary.getDate18();
             }
         }
         else if(formElement.getElementId()==279){
             fieldName = "date19";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getDate19()){
                 fieldValue = edocSummary.getDate19();
             }
         }
         else if(formElement.getElementId()==280){
             fieldName = "date20";
             branchFieldDbType ="date";
             if(null!=edocSummary && null!=edocSummary.getDate20()){
                 fieldValue = edocSummary.getDate20();
             }
         }
         else if(formElement.getElementId()==101){               
             fieldName = "list1";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getList1())){
                 fieldValue = edocSummary.getList1();
             }
         }
         else if(formElement.getElementId()==102){               
             fieldName = "list2";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getList2())){
                 fieldValue = edocSummary.getList2();
             }
         }
         else if(formElement.getElementId()==103){               
             fieldName = "list3";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getList3())){
                 fieldValue = edocSummary.getList3();
             }
         }
         else if(formElement.getElementId()==104){               
             fieldName = "list4";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getList4())){
                 fieldValue = edocSummary.getList4();
             }
         }
         else if(formElement.getElementId()==105){               
             fieldName = "list5";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getList5())){
                 fieldValue = edocSummary.getList5();
             }
         }
         else if(formElement.getElementId()==106){               
             fieldName = "list6";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getList6())){
                 fieldValue = edocSummary.getList6();
             }
         }
         else if(formElement.getElementId()==107){               
             fieldName = "list7";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getList7())){
                 fieldValue = edocSummary.getList7();
             }
         }
         else if(formElement.getElementId()==108){               
             fieldName = "list8";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getList8())){
                 fieldValue = edocSummary.getList8();
             }
         }
         else if(formElement.getElementId()==109){               
             fieldName = "list9";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getList9())){
                 fieldValue = edocSummary.getList9();
             }
         }
         else if(formElement.getElementId()==110){               
             fieldName = "list10";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getList10())){
                 fieldValue = edocSummary.getList10();
             }
         }
         else if(formElement.getElementId()==261){               
             fieldName = "list11";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getList11())){
                 fieldValue = edocSummary.getList11();
             }
         }
         else if(formElement.getElementId()==262){               
             fieldName = "list12";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getList12())){
                 fieldValue = edocSummary.getList12();
             }
         }
         else if(formElement.getElementId()==263){               
             fieldName = "list13";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getList13())){
                 fieldValue = edocSummary.getList13();
             }
         }
         else if(formElement.getElementId()==264){               
             fieldName = "list14";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getList14())){
                 fieldValue = edocSummary.getList14();
             }
         }
         else if(formElement.getElementId()==265){               
             fieldName = "list15";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getList15())){
                 fieldValue = edocSummary.getList15();
             }
         }
         else if(formElement.getElementId()==266){               
             fieldName = "list16";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getList16())){
                 fieldValue = edocSummary.getList16();
             }
         }
         else if(formElement.getElementId()==267){               
             fieldName = "list17";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getList17())){
                 fieldValue = edocSummary.getList17();
             }
         }
         else if(formElement.getElementId()==268){               
             fieldName = "list18";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getList18())){
                 fieldValue = edocSummary.getList18();
             }
         }
         else if(formElement.getElementId()==269){               
             fieldName = "list19";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getList19())){
                 fieldValue = edocSummary.getList19();
             }
         }
         else if(formElement.getElementId()==270){               
             fieldName = "list20";
             branchFieldDbType ="int";
             if(null!=edocSummary && !"".equals(edocSummary.getList20())){
                 fieldValue = edocSummary.getList20();
             }
         }else if(formElement.getElementId()==291){              
             fieldName = "string21";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getVarchar21()){
                 fieldValue = edocSummary.getVarchar21();
             }
         }else if(formElement.getElementId()==292){              
             fieldName = "string22";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getVarchar22()){
                 fieldValue = edocSummary.getVarchar22();
             }
         }else if(formElement.getElementId()==293){              
             fieldName = "string23";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getVarchar23()){
                 fieldValue = edocSummary.getVarchar23();
             }
         }else if(formElement.getElementId()==294){              
             fieldName = "string24";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getVarchar24()){
                 fieldValue = edocSummary.getVarchar24();
             }
         }else if(formElement.getElementId()==295){              
             fieldName = "string25";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getVarchar25()){
                 fieldValue = edocSummary.getVarchar25();
             }
         }else if(formElement.getElementId()==296){              
             fieldName = "string26";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getVarchar26()){
                 fieldValue = edocSummary.getVarchar26();
             }
         }else if(formElement.getElementId()==297){              
             fieldName = "string27";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getVarchar27()){
                 fieldValue = edocSummary.getVarchar27();
             }
         }else if(formElement.getElementId()==298){              
             fieldName = "string28";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getVarchar28()){
                 fieldValue = edocSummary.getVarchar28();
             }
         }else if(formElement.getElementId()==299){              
             fieldName = "string29";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getVarchar29()){
                 fieldValue = edocSummary.getVarchar29();
             }
         }else if(formElement.getElementId()==300){              
             fieldName = "string30";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getVarchar30()){
                 fieldValue = edocSummary.getVarchar30();
             }
         }else if(formElement.getElementId()==301){
             fieldName = "text11";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getText11()){
                 fieldValue = edocSummary.getText11();
             }
         }else if(formElement.getElementId()==302){
             fieldName = "text12";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getText12()){
                 fieldValue = edocSummary.getText12();
             }
         }else if(formElement.getElementId()==303){
             fieldName = "text13";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getText13()){
                 fieldValue = edocSummary.getText13();
             }
         }else if(formElement.getElementId()==304){
             fieldName = "text14";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getText14()){
                 fieldValue = edocSummary.getText14();
             }
         }else if(formElement.getElementId()==305){
             fieldName = "text15";
             fieldValue = "";
             branchFieldDbType ="varchar";
             if(null!=edocSummary && null!=edocSummary.getText15()){
                 fieldValue = edocSummary.getText15();
             }
         }
         if(!StringUtils.isBlank(fieldName)){
             if(fieldValue==null){
                 fieldValue="";
             }                
             if("varchar".equals(branchFieldDbType)){
                 edocFormData.put(fieldName, fieldValue);
             }else if("int".equals(branchFieldDbType)){
                 if(null!=fieldValue && !"".equals(fieldValue.toString().trim())){
                     try{
                         fieldValue= fieldValue.toString().trim();
                         edocFormData.put(fieldName, Integer.parseInt(fieldValue.toString()));
                     }catch(Throwable e){
                         LOGGER.warn("edoc filed type is error,", e);
                         edocFormData.put(fieldName, fieldValue);
                     }
                 }else{
                     edocFormData.put(fieldName, null);
                 }
             }else if("decimal".equals(branchFieldDbType)){
                 if(null!=fieldValue && !"".equals(fieldValue.toString().trim())){
                     try{
                         fieldValue= fieldValue.toString().trim();
                         edocFormData.put(fieldName, Double.parseDouble(fieldValue.toString()));
                     }catch(Throwable e){
                         LOGGER.warn("edoc filed type is error,"+e);
                         edocFormData.put(fieldName, fieldValue);
                     }
                 }else{
                     edocFormData.put(fieldName, null);
                 }
             }else{
                 edocFormData.put(fieldName, fieldValue);
             }
         }
     }
     LOGGER.info("当前公文单数据edocFormDataDb:="+edocFormData);
     return edocFormData;
 }
 
	
//	@Override
//	public List<WorkflowFormFieldVO> getWorkflowBranchFormFieldVOListById(Long id) {
//		return edocFormManager.getElementByEdocForm(id);
//	}
	/**
	 * @param:
	 */
	@SuppressWarnings("unchecked")
	public String getFormIdByWorkflowId(String workflowId) {
		String result = "-1";
		try{
			String json = edocFormManager.getFormIdByWorkflowId(workflowId, true);
			Map<String, Object> jsObj = JSONUtil.parseJSONString(json.replaceAll("formId", "\"formId\"").replaceAll("'", "\""), Map.class);
			if(jsObj!=null){
				Object o = jsObj.get("formId");
				if(o!=null){
					if(o.equals(Integer.valueOf(0))){
						result = "-1";
					}else{
						result = o.toString();
					}
				}else{
					result = "-1";
				}
			}
		}catch(NullPointerException e){
			result = "-1";
		}
		return result;
	}

	/**
     * @return the edocFormManager
     */
    public EdocFormManager getEdocFormManager() {
        return edocFormManager;
    }

    /**
     * @param edocFormManager the edocFormManager to set
     */
    public void setEdocFormManager(EdocFormManager edocFormManager) {
        this.edocFormManager = edocFormManager;
    }

//	@Override
//	public Map<String, WorkflowFormFieldVO> getFormFieldsDefinition(String appName, String formAppId,
//			List<String> fieldTypes, List<String> tableTypes) {
//		 List<WorkflowFormFieldVO> allEdocFields= edocFormManager.getAllElementsByEdocForm(Long.parseLong(formAppId));
//		 Map<String, WorkflowFormFieldVO> fieldMap = new LinkedHashMap<String, WorkflowFormFieldVO>();
//		 if(null!=allEdocFields && !allEdocFields.isEmpty()){
//			 for (WorkflowFormFieldVO field : allEdocFields) {
//				 String tableType= "main";
//				 if(null!=tableTypes && !tableTypes.isEmpty()){
//             		if(!tableTypes.contains(tableType)){
//             			continue;
//             		}
//             	 }
//				 boolean isFieldTypeMatched= getFieldTypeMatchResult(field,fieldTypes);
//             	 if(isFieldTypeMatched){
//	                    WorkflowFormFieldVO vo= new WorkflowFormFieldVO();
//	                    vo.setDisplay(field.getDisplay());
//	                    vo.setEnumId(field.getEnumId());
//	                    vo.setFieldName(field.getFieldName());
//	                    vo.setFieldType(field.getFieldType());
//	                    vo.setInputType(field.getInputType());
//	                    vo.setMasterField(field.isMasterField()); 
//	                    vo.setName(field.getName());
//	                    vo.setOwnerTableName("edoc_summary");
//	                    vo.setFieldDisplayLabel(field.getName());
//	                    vo.setRealType(field.getInputType());
//	                    fieldMap.put(field.getFieldName(), vo);
//	                    fieldMap.put(field.getDisplay(), vo);
//             	}
//			}
//		 }
//		 return fieldMap;
//	}
	
	/**
	 * 字段类型匹配
	 * @param field
	 * @param fieldTypes
	 * @return
	 */
//	private boolean getFieldTypeMatchResult(WorkflowFormFieldVO field, List<String> fieldTypes) {
//		if(null==fieldTypes || fieldTypes.isEmpty()){
//			return true;
//		}
//		if(field!=null && field.getInputType()!=null){
//			if(fieldTypes.contains(field.getInputType()) || fieldTypes.contains(field.getInputType().toLowerCase())){
//				return true;
//			}else{
//				String fieldDbType= field.getFieldType().toLowerCase();
//				if( fieldTypes.contains(fieldDbType) ){
//					return true;
//				}
//			}
//        }
//		return false;
//	}

	@Override
	public com.seeyon.ctp.workflow.vo.User getUser(String memberId, String processId, String appName) {
		com.seeyon.ctp.workflow.vo.User user = null;
        boolean isEnabled = false;//人员是否启用状态
        boolean hasAgent = false;//是否设置了代理
        try {
            V3xOrgMember orgMember = orgManager.getMemberById(Long.valueOf(memberId));
            if(orgMember != null){
            	isEnabled = orgMember.getEnabled();
            	user = new com.seeyon.ctp.workflow.vo.User(orgMember.getId() + "", orgMember.getName());
            }
            if(user != null){
            	//查询代理情况
            	List<AgentModel> agentModelList = MemberAgentBean.getInstance().getAgentModelToList(Long.valueOf(memberId));
            	for(AgentModel agentModel : agentModelList){
            		Long agentId = 0L;
            		EdocSummary summary = edocManager.getSummaryByProcessId(processId);
            		if(summary.getCreateTime().compareTo(agentModel.getStartDate()) >= 0 && summary.getCreateTime().before(agentModel.getEndDate())){
            			if(agentModel.isHasEdoc()){//公文
            				agentId = agentModel.getAgentId();
            				hasAgent = true;
            			}
            		}
            		//有代理人
            		V3xOrgMember member = orgManager.getMemberById(agentId);
            		if(member != null){
            			if(agentModel.getAgentType() == AgentConstants.AGENT_SET){//代理设置
            				user.setName(user.getName() + ResourceUtil.getString("supervise.hasten.agent.set",member.getName()));//"（由XX代理）"
            			}else{//离职交接
            				user.setName(user.getName() + ResourceUtil.getString("supervise.hasten.agent.leave",member.getName()));// "（已交接给XX）"
            			}
            		}
            	}
            }
        } catch (Exception e) {
        	LOGGER.error("出现异常",e);
        }
        
        if(isEnabled || hasAgent){//人员可用状态都要显示
			return user;
		}else{
			return null;
		}
	}
	
	@Override
	public String getDefaultPolicyId4InsertNode(Long accountId,String processId) {
		try {
			EdocSummary summary = edocManager.getSummaryByProcessId(processId);
			Map<String,String> defaultPolicyMap = new HashMap<String,String>();
			
			if(AppContext.hasPlugin("govdoc") && summary.getGovdocType().intValue() !=0 ){
				defaultPolicyMap = edocSummaryManager.getGovdocDefaultNode(summary.getGovdocTypeEnum(), accountId);
			}else{
				defaultPolicyMap = edocSummaryManager.getEdocDefaultNode(summary.getEdocTypeEnum(), accountId);
			}
			
			if(defaultPolicyMap!=null && !defaultPolicyMap.isEmpty()){
				return defaultPolicyMap.get("defaultNodeName");
			}
			
		} catch (BusinessException e) {
			LOGGER.error("获取默认节点权限出现异常",e);
		}
		return null;
	}
	
	

	@Override
    public Map<String, WorkflowFormFieldBO> getFormDefinitionMap(String formApp) {
    	if(Strings.isBlank(formApp) || !Strings.isDigits(formApp)){
    		return Collections.emptyMap();
    	}
    	
    	/*WorkflowFormDataMapManager formDataMapManager = WorkflowFormDataMapInvokeManager.getAppManager("form");*/
        WorkflowFormDataMapManager formDataManager = wapi.getWorkflowFormDataMapManager("form");
        Map<String, WorkflowFormFieldBO> rowDataDisplayName = formDataManager.getFormFieldMap(formApp);
        return rowDataDisplayName;
    }
	
	@Override
	public Map<String,WorkflowFormFieldBO> getFormFieldsDefinition(String appName,String formAppId, List<String> fieldTypes,String externalType,List<String> tableTypes) {
		if(Strings.isNotBlank(appName) && Strings.isNotBlank(formAppId) /*&& "form".equals(appName)*/ ){//表单字段
			WorkflowApiManager wapi=(WorkflowApiManager)AppContext.getBean("wapi");
    	    WorkflowFormDataMapManager formDataManager = wapi.getWorkflowFormDataMapManager("form");
			Map<String, WorkflowFormFieldBO> fieldMap= formDataManager.getFormFieldMap(formAppId,fieldTypes,externalType,tableTypes,true);
			return fieldMap;
		}
		return Collections.emptyMap();
	}
	
  @Override
	public void policyNodesChanged(String processId, Object appObject, Map<String, String> map) {
		  if(Strings.isBlank(processId) || map==null || map.isEmpty()){
				return;
			}
			Map<String,Object> conditions = new HashMap<String,Object>();
			
			conditions.put("processId", processId);
			
			
			List<Long> activityIds = new ArrayList<Long>();
			for(String activityId : map.keySet()) {
				activityIds.add(Long.valueOf(activityId));
			}
			conditions.put("activityId", activityIds);
			
			List<Integer> states = new ArrayList<Integer>();
			states.add(StateEnum.col_pending.getKey());
			states.add(StateEnum.col_pending_repeat_auto_deal.getKey());
			conditions.put("state", states);
			
			try {
				List<CtpAffair> updateAffair = new ArrayList<CtpAffair>();
				List<CtpAffair> affairs =	affairManager.getByConditions(null, conditions);
				for(CtpAffair ctpAffair : affairs) {
					Long activityId = ctpAffair.getActivityId();
					String affairPolicy = ctpAffair.getNodePolicy();
					String updatePolicy = map.get(activityId+"");
					if(!affairPolicy.equals(updatePolicy)){
						ctpAffair.setNodePolicy(updatePolicy);
						updateAffair.add(ctpAffair);
					}
				}
				if(Strings.isNotEmpty(updateAffair)){
					affairManager.updateAffairs(updateAffair);
				}
			} catch (BusinessException e) {
				LOGGER.error("更新affair节点权限异常:", e);
			}
	}
  
  	@Override
	public String getAddedFirstNodePolicyId(Long accountId, String templateProcessId) {
  		String policyId = "chengban";//为了兼容A8 OA-174540
  		try {
  			CtpTemplate template = templateManager.getCtpTemplateByWorkFlowId(Long.valueOf(templateProcessId));
  			ModuleType mt = ModuleType.getEnumByKey(template.getModuleType());
			if (mt == ModuleType.govdocRec) {
				policyId = "chengban";
			} else if (mt == ModuleType.govdocExchange) {
				policyId = "qianshou";
			}			
		} catch (NumberFormatException e) {
			LOGGER.error(e);
		} catch (BusinessException e) {
			LOGGER.error(e);
		}
		return policyId;
	}

	public TemplateManager getTemplateManager() {
		return templateManager;
	}

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}
    @Override
    public String getFormMasterId(Long processId) {
    	try {
    		EdocSummary summary = edocManager.getEdocSummaryByProcessId(processId);
			if(summary != null){
				return summary.getFormRecordid() == null ? "" : String.valueOf(summary.getFormRecordid());
			}
		} catch (Exception e) {
			LOGGER.error("获取getFormMasterId错误", e);
		}
    	return "";
    }
    
    public void unLockBugReport(Long affairId) throws BusinessException {
    	LOGGER.info("管理员unLockBugReport:affairId:" + affairId);
		if (affairId == null) {
			return;
		}
		CtpAffair affair = affairManager.get(affairId);

		List<Lock> wflocks = lockManager.getLocks(Long.valueOf(affair.getProcessId()));
		if (wflocks != null && !wflocks.isEmpty()) {
			for (Lock lk : wflocks) {
				lockManager.unlock(lk.getOwner(), lk.getResourceId(), lk.getAction());
			}
		}
		List<Lock> summarylocks = lockManager.getLocks(Long.valueOf(affair.getObjectId()));
		if (summarylocks != null && !summarylocks.isEmpty()) {
			for (Lock lk : summarylocks) {
				lockManager.unlock(lk.getOwner(), lk.getResourceId(), lk.getAction());
			}
		}

		// 解除表单锁
		if (affair.getFormAppId() != null && String.valueOf(MainbodyType.FORM.getKey()).equals(affair.getBodyType())) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.info("管理员unLockBugReport，解锁表单锁：summaryid:" + affair.getObjectId() + ",fromRecordId:"
						+ affair.getFormRecordid());
			}
			formApi4Cap3.removeSessionMasterData(affair.getFormRecordid());
			lockManager.unlock(affair.getFormRecordid());
		}
	}
    
    public CtpAffair getCtpAffairBySubObjectId(Long workitemId){
        try {
            return affairManager.getAffairBySubObjectId(workitemId);
        } catch (BusinessException e) {
            LOGGER.error("出现异常", e);
        }
        return null;
    }
}
