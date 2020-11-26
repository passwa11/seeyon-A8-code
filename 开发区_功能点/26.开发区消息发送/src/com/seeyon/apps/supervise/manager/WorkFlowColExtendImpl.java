package com.seeyon.apps.supervise.manager;

import com.seeyon.apps.agent.AgentConstants;
import com.seeyon.apps.agent.bo.AgentDetailModel;
import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.bo.BackgroundDealParamBO;
import com.seeyon.apps.collaboration.bo.BackgroundDealResult;
import com.seeyon.apps.collaboration.enums.BackgroundDealType;
import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.listener.WorkFlowEventListener;
import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.manager.ColMessageManager;
import com.seeyon.apps.collaboration.manager.ColReceiverManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColSelfUtil;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.collaboration.vo.ColReceiverVO;
import com.seeyon.apps.edoc.api.EdocApi;
import com.seeyon.apps.edoc.bo.EdocSummaryBO;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.Comment.CommentType;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.lock.manager.LockManager;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.lock.Lock;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.supervise.bo.SuperviseHastenParam;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.bo.WorkflowFormFieldBO;
import com.seeyon.ctp.workflow.dynamicform.manager.WFDynamicFormManager;
import com.seeyon.ctp.workflow.util.WorkflowMatchLogMessageConstants;
import com.seeyon.ctp.workflow.vo.User;
import com.seeyon.ctp.workflow.wapi.WorkFlowAppExtendAbstractManager;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.ctp.workflow.wapi.WorkflowFormDataMapManager;
import com.seeyon.v3x.mobile.message.manager.MobileMessageManager;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;
import org.apache.commons.logging.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WorkFlowColExtendImpl extends WorkFlowAppExtendAbstractManager {
	
	private static Log LOG = CtpLogFactory.getLog(WorkFlowColExtendImpl.class);
	private SuperviseManager superviseManager;
	private ColManager colManager;
	private OrgManager orgManager;
	private AffairManager affairManager;
    private ColMessageManager colMessageManager;
    private MobileMessageManager mobileMessageManager;
    private WorkTimeManager workTimeManager;
    private EdocApi edocApi;
    private WFDynamicFormManager wFDynamicFormManager; 
    private CAPFormManager capFormManager;
    private PermissionManager	permissionManager;
    private ColReceiverManager colReceiverManager;
    private CollaborationApi collaborationApi;
    private LockManager lockManager;
    private FormApi4Cap3 formApi4Cap3;
    private ProcessLogManager processLogManager;
    private AppLogManager                appLogManager;
    
    
	public AppLogManager getAppLogManager() {
		return appLogManager;
	}

	public void setAppLogManager(AppLogManager appLogManager) {
		this.appLogManager = appLogManager;
	}

	public ProcessLogManager getProcessLogManager() {
		return processLogManager;
	}

	public void setProcessLogManager(ProcessLogManager processLogManager) {
		this.processLogManager = processLogManager;
	}

	public FormApi4Cap3 getFormApi4Cap3() {
		return formApi4Cap3;
	}

	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
		this.formApi4Cap3 = formApi4Cap3;
	}

	public LockManager getLockManager() {
		return lockManager;
	}

	public CollaborationApi getCollaborationApi() {
		return collaborationApi;
	}

	public void setCollaborationApi(CollaborationApi collaborationApi) {
		this.collaborationApi = collaborationApi;
	}

	public void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}

	public void setCapFormManager(CAPFormManager capFormManager) {
        this.capFormManager = capFormManager;
    }
    
	public void setwFDynamicFormManager(WFDynamicFormManager wFDynamicFormManager) {
		this.wFDynamicFormManager = wFDynamicFormManager;
	}
	public void setEdocApi(EdocApi edocApi) {
		this.edocApi = edocApi;
	}
	public void setWorkTimeManager(WorkTimeManager workTimeManager) {
		this.workTimeManager = workTimeManager;
	}
	public void setColMessageManager(ColMessageManager colMessageManager) {
        this.colMessageManager = colMessageManager;
    }
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	
	public void setColManager(ColManager colManager) {
		this.colManager = colManager;
	}
	
	public void setSuperviseManager(SuperviseManager superviseManager) {
		this.superviseManager = superviseManager;
	}
	
	public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

	public void setMobileMessageManager(MobileMessageManager mobileMessageManager) {
		this.mobileMessageManager = mobileMessageManager;
	}
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}
	
    public void setColReceiverManager(ColReceiverManager colReceiverManager) {
        this.colReceiverManager = colReceiverManager;
    }

    @Override
	public ApplicationCategoryEnum getAppName() {
	    return ApplicationCategoryEnum.collaboration;
	}

	@Override
	public int getSummaryVouch(String arg0) {
		int returnValue = 0;
	    if(Strings.isBlank(arg0)){
	        return returnValue;
	    }
	    Long processId = Long.parseLong(arg0);
	    ColSummary summary = null;
	    try {
            summary = colManager.getColSummaryByProcessId(processId);
            if(summary==null){
                summary = colManager.getColSummaryByProcessIdHistory(processId);
            }
            
            if(colManager.getSummaryHasVouch(summary)){
            	returnValue = 1;
            }
        } catch (BusinessException e) {
        	LOG.error("", e);
        }
		return returnValue;
	}
	
	@Override
	public boolean getSummaryHasAudit(String arg0) {
		boolean flag = false;
	    if(Strings.isBlank(arg0)){
	        return flag;
	    }
	    Long processId = Long.parseLong(arg0);
	    ColSummary summary = null;
	    try {
            summary = colManager.getColSummaryByProcessId(processId);
            if(summary==null){
                summary = colManager.getColSummaryByProcessIdHistory(processId);
            }
            
            if(colManager.getSummaryHasAudit(summary)){
            	flag = true;
            }
        } catch (BusinessException e) {
        	LOG.error("", e);
        }
		return flag;
	}
	
	//发送催办信息的同时，发送短信

	public String hasten(String processId, String activityId,
			List<Long> personIds, String superviseId, String content,
			boolean sendPhoneMessage) {
		boolean result = this.hasten(processId, activityId, personIds, superviseId, content);
		
		String ret = ResourceUtil.getString("supervise.hasten.success.label"); //催办成功
		
		if(sendPhoneMessage && result){
			 try {
				  content = fillContent(processId, activityId, content);
				} catch (NumberFormatException e) {
					LOG.error("", e);
				} catch (BusinessException e) {
					LOG.error("", e);
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
		        LOG.error("", e);
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
			ColSummary summary = colManager.getColSummaryByProcessId(Long.valueOf(processId));
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
			//Object[] params = { name, subject, forwardMemberFlag, forwardMember, additional_remarkFlag, content };
			// 按本地国际化将发送的内容处理
			String newContent = ResourceUtil.getStringByParams("col.hasten", name, subject, forwardMemberFlag, forwardMember, additional_remarkFlag, content);
			return newContent;
		}
	//是否允许具备发送短信的权限
	
	public boolean isCanSendPhoneMessage(long userId, long accountId) {
		boolean result = false;
		if(SystemEnvironment.hasPlugin("sms") && mobileMessageManager!=null){
			//只需要判断是否可以发送短信即可！
			if(mobileMessageManager.isCanUseSMS()){
				result = true;
			}
		}

		return result;
	}
	//流程id, 节点id, 接受催办消息的人员, 督办记录id, 催办正文
	@Override
	public boolean hasten(String processId, String activityId, List<Long> personIds, String superviseId,String content) {
		boolean retValue = false;
	    try {
	    	Long summaryId=0L;
	    	Long startMemeberId=0L;
	    	String subject="";
		    ColSummary summary=colManager.getColSummaryByProcessId(Long.valueOf(processId));
		    if(summary!=null){
		    	summaryId=summary.getId();
		    	startMemeberId=summary.getStartMemberId();
		    	subject=summary.getSubject();
		    }else{
		    	EdocSummaryBO edocSummary=edocApi.getEdocSummaryByProcessId(Long.valueOf(processId));
		    	summaryId=edocSummary.getId();
		    	startMemeberId=edocSummary.getStartUserId();
		    	subject=edocSummary.getSubject();
		    }
		    
		    if(Strings.isBlank(superviseId)) {
			    CtpSuperviseDetail detail =superviseManager.getSupervise(summaryId); 
			    //即使superviseId为空，也要从数据库中查询一次，当CtpSuperviseDetail有值时赋值，没值则不是督办人，传入空值
			    if (detail!=null){
			        superviseId = detail.getId().toString();
			    }
		    }
		    SuperviseHastenParam param = new SuperviseHastenParam();
		    param.setMemberId(startMemeberId);
		    param.setActivityId(activityId);
		    param.setContent(content);
		    param.setSummaryId(summaryId);
		    param.setPersonIds(personIds);
		    param.setSuperviseId(superviseId);
		    param.setTitle(subject);
		    retValue= superviseManager.transHasten(param);
	   } catch (Exception e) {
	     LOG.info(e.getLocalizedMessage());
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
            ColSummary summary = colManager.getColSummaryByProcessId(processId);
            if(summary==null){
                summary = colManager.getColSummaryByIdHistory(processId);
            }
            if(summary == null ) return false;
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("objectId", summary.getId());
            map.put("activityId", activityId);
            List<Integer> state = new ArrayList<Integer>();
            state.add(StateEnum.col_sent.getKey());
            state.add(StateEnum.col_pending.getKey());
            state.add(StateEnum.col_done.getKey());
            map.put("state", state);
            List<CtpAffair> list = affairManager.getByConditions(null,map);
            boolean isOver = false;
          
            if(Strings.isNotEmpty(list)){
                for(CtpAffair affair :list){
                	
                    Date _expectedProcessTime = affair.getExpectedProcessTime();
                    if((affair.isCoverTime() != null && affair.isCoverTime())){
                        isOver = true;
                        
                        if(_expectedProcessTime == null){
                            isOver = false;
                        }
                        if(affair.getCompleteTime()!= null && affair.getCompleteTime().before(_expectedProcessTime)){
                            isOver = false;
                        }
                        if(isOver){
                            break;
                        }
                    }

                	if(!isOver){
                		
                        if(_expectedProcessTime == null && affair.getDeadlineDate() != null && affair.getDeadlineDate().intValue() != 0){
                        	_expectedProcessTime = workTimeManager.getCompleteDate4Nature(affair.getReceiveTime(), affair.getDeadlineDate(),AppContext.currentAccountId());
                        }
                       //节点是否超期
                        boolean isExpectedOvertime = ColUtil.checkAffairIsOverTime(affair, summary);
                        if(isExpectedOvertime){
                        	 isOver = true;
                             break;
                        }
                	}
                }
                return isOver;
            }else{
                return false;
            }
        } catch (BusinessException e) {
          LOG.info("调用计算节点超期出错！",e);
        }
		return false;
	}
    /* (non-Javadoc)
     * @see com.seeyon.ctp.workflow.wapi.WorkFlowAppExtendManager#checkUserSupervisorRight(java.lang.String, java.lang.Long)
     */
    @Override
    public boolean checkUserSupervisorRight(String caseId, Long userId) {
		try {
		    
			
		    V3xOrgMember m = orgManager.getMemberById(userId);
            boolean isAdmin= orgManager.isAdministratorById(userId, m.getOrgAccountId());//单位管理员判断
            boolean isGroupAdmin=orgManager.isGroupAdminById(userId);//集团管理员判断
            boolean isFormAdmin = orgManager.isRole(userId, m.getOrgAccountId(), 
                     OrgConstants.Role_NAME.FormAdmin.name());
		    
            boolean businessDesigner = orgManager.isRole(userId, m.getOrgAccountId(), 
                    OrgConstants.Role_NAME.BusinessDesigner.name());
            
            // 管理员都有权限
            if(isAdmin || isGroupAdmin || isFormAdmin || businessDesigner){
                return true;
            }
            
			ColSummary summary=colManager.getSummaryByCaseId(Long.parseLong(caseId));
			CtpAffair affair=affairManager.getSenderAffair(summary.getId());
			boolean isSupervisor = superviseManager.isSupervisor(userId, summary.getId());
			if(!isSupervisor){			    
				if(affair.getSenderId().equals(userId) && null==summary.getTempleteId()){//发起人可以修改
					return true;
				}else {
				    return false;
				}
			}
			else {
				return true;
			}
		}catch (Exception e) {
		  LOG.error("检查督办权限错误：",e);
		}
		
		return false;
    }
    /* (non-Javadoc)
     * @see com.seeyon.ctp.workflow.wapi.WorkFlowAppExtendManager#getSummarySubject(java.lang.String)
     */
    @Override
    public String getSummarySubject(String processId) {
    	String subject="";
    	try {
			ColSummary summary=colManager.getColSummaryByProcessId(Long.parseLong(processId));
			if(summary==null){
			  summary = colManager.getColSummaryByProcessIdHistory(Long.parseLong(processId));
			}
			if(summary != null){
				 subject = ColUtil.showSubjectOfSummary(summary, false, -1, null).replaceAll("\r\n", "").replaceAll("\n", "");
			}
			
		} catch (NumberFormatException e) {
			LOG.info(e.getLocalizedMessage());
		} catch (BusinessException e) {
		  LOG.info(e.getLocalizedMessage());
		}
        return subject;
    }
    /* (non-Javadoc)
     * @see com.seeyon.ctp.workflow.wapi.WorkFlowAppExtendManager#sendSupervisorMsgAndRecordAppLog(java.lang.String)
     */
    @Override
    public void sendSupervisorMsgAndRecordAppLog(String caseId) {
    	this.colMessageManager.sendSupervisorMsgAndRecordAppLogCol(caseId);
    }
	@Override
	public AffairData getAffairData(String processId) {
		AffairData affairData=null;
		try {
			ColSummary summary=colManager.getColSummaryByProcessId(Long.valueOf(processId));
			if(summary==null){
			    summary=colManager.getColSummaryByProcessIdHistory(Long.valueOf(processId));
			}
			affairData=ColUtil.getAffairData(summary);
		} catch (NumberFormatException e) {
			LOG.error("", e);
		} catch (BusinessException e) {
			LOG.error("", e);
		}
		return affairData;
	}
	@Override
  public String[] getNodeReceiveAndDealedTime(String processId, String nodeId, boolean isHistoryFlag) {

	String[] str = new String[]{"","","false",""};
    try {
      ColSummary summary = null;
      if(isHistoryFlag){
        summary = colManager.getColSummaryByProcessIdHistory(Long.parseLong(processId));
      }else{
        summary = colManager.getColSummaryByProcessId(Long.parseLong(processId));;
      }
      
      if(summary == null){ 
          return str;
      }
      List<CtpAffair> list = null;
      if(isHistoryFlag){
    	  list = affairManager.getAffairsHis(summary.getId(), Long.parseLong(nodeId));
      }else{
    	  list = affairManager.getAffairsByObjectIdAndNodeId(summary.getId(), Long.parseLong(nodeId));
      }
      
            if(Strings.isEmpty(list)){
              return str;
            }
            CtpAffair affair = null;
            for (CtpAffair ctpAffair : list) {
            	if(Integer.valueOf(StateEnum.col_sent.getKey()).equals(ctpAffair.getState())
                        || Integer.valueOf(StateEnum.col_waitSend.getKey()).equals(ctpAffair.getState())
            			|| Integer.valueOf(StateEnum.col_pending.getKey()).equals(ctpAffair.getState())
            			|| Integer.valueOf(StateEnum.col_done.getKey()).equals(ctpAffair.getState())
                        ){
                   affair = ctpAffair;
                   break;
                }
            }
            //A->B，B多次回退到A后，A查看流程时，B节点属性中接收时间一直显示的是第一次接收时间。修改未激活的节点不显示时间
            if(affair == null) {
            	return str;
            }
            Date receiveTime = affair.getReceiveTime();
            if(receiveTime != null){
              str[0] = Datetimes.format(receiveTime, "yyyy-MM-dd HH:mm:ss");
            }
            Date completeTime = affair.getCompleteTime();
            //UpdateDate将作为置顶辅助参数使用;暂存待办没处理,因此不显示完成时间
            /*if(Integer.valueOf(SubStateEnum.col_pending_ZCDB.getKey()).equals(affair.getSubState())){
                completeTime = affair.getUpdateDate();
            }*/
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
    } catch (NumberFormatException e) {
      LOG.error("", e);
    } catch (BusinessException e) {
      LOG.error("", e);
    }
    return str;
  
  }
  @Override
	public String[] getNodeReceiveAndDealedTime(String processId, String nodeId) {
	  String[] str = new String[]{"","","false",""};
		try {
			ColSummary summary=colManager.getColSummaryByProcessId(Long.parseLong(processId));
			if(summary == null){ 
			    summary = colManager.getColSummaryByProcessIdHistory(Long.parseLong(processId));
            }
			if(summary == null){ 
			    return str;
			}
            List<CtpAffair> list = affairManager.getAffairsByObjectIdAndNodeId(summary.getId(), Long.parseLong(nodeId));
            if(Strings.isEmpty(list)){
                list = affairManager.getAffairsHis(summary.getId(), Long.parseLong(nodeId));
            }
            if(Strings.isEmpty(list)){
            	return str;
            }
            CtpAffair affair = null;
            for(CtpAffair c : list){
                Integer state = c.getState();
                if(state != null
                        && state != StateEnum.col_cancel.getKey()
                        && state != StateEnum.col_stepBack.getKey()
                        && state != StateEnum.col_takeBack.getKey()
                        && state != StateEnum.col_competeOver.getKey()){
                       
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
            //UpdateDate将作为置顶辅助参数使用;暂存待办没处理,因此不显示完成时间
            /*if(Integer.valueOf(SubStateEnum.col_pending_ZCDB.getKey()).equals(affair.getSubState())){
                completeTime = affair.getUpdateDate();
            }*/
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
		} catch (NumberFormatException e) {
			LOG.error("", e);
		} catch (BusinessException e) {
			LOG.error("", e);
		}
		return str;
	}
  
   /**
    *
    * @param list
    */
   @Override
   public void processNodesChanged(String processId,Object appObject,List<Map<String,String>>  list) {

	   if (Strings.isNotEmpty(list) && Strings.isNotBlank(processId)) {


		   int count = 0;

		   if (list.size() > 30) {

			   list = list.subList(0, 30);

			   ColReceiverVO receiveVO = new ColReceiverVO();

			   Map<String, String> map = new HashMap<String, String>();
			   map.put(ColReceiverVO.NODE_ID, "");
			   map.put(ColReceiverVO.NODE_NAME, "");
			   map.put(ColReceiverVO.ACTOR_TYPE_ID, "more");
			   map.put(ColReceiverVO.ACTOR_PARTY_ID, "");

			   list.add(map);

		   }


		   String nodesInfo = JSONUtil.toJSONString(list);

		   if (appObject != null && appObject instanceof ColSummary) {
			   ((ColSummary) appObject).setProcessNodesInfo(nodesInfo.toString());
		   } else {
			   colManager.updateColSummaryProcessNodeInfos(processId, nodesInfo.toString());
		   }
	   }
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
  
	public void transReMeToReGo(WorkflowBpmContext context) throws BusinessException{

		colManager.transReMeToReGo(context);
    
	}
	
	@Override
	public boolean hasWFDynamicForm(String currentFormAppId,String baseFormAppId ) {
		return wFDynamicFormManager.hasWFDynamicForm(currentFormAppId,baseFormAppId);
	}
	
	
	@Override
	public User getUser(String memberId, String processId,String appName) {
		User user = null;
        boolean isEnabled = false;//人员是否启用状态
        boolean hasAgent = false;//是否设置了代理
        try {
            V3xOrgMember orgMember = orgManager.getMemberById(Long.valueOf(memberId));
            if(orgMember != null){
            	isEnabled = orgMember.getEnabled();
            	user = new User(orgMember.getId() + "", orgMember.getName());
            }
            if(user != null){
            	//查询代理情况
            	List<AgentModel> agentModelList = MemberAgentBean.getInstance().getAgentModelToList(Long.valueOf(memberId));
            	for(AgentModel agentModel : agentModelList){
            		Long agentId = 0L;
            		ColSummary summary = colManager.getColSummaryByProcessId(Long.valueOf(processId));
            		if(summary.getCreateDate().compareTo(agentModel.getStartDate()) >= 0 && summary.getCreateDate().before(agentModel.getEndDate())){
            			if(summary.getTempleteId() != null){//模板协同或者表单协同
            				List<AgentDetailModel> agentDetails = agentModel.getAgentDetail(ApplicationCategoryEnum.collaboration.key());
            				if(agentDetails != null){
            					for(AgentDetailModel model : agentDetails){
            						if(model.getEntityId().equals(summary.getTempleteId())){
            							agentId = agentModel.getAgentId();
            							hasAgent = true;
            							break;
            						}
            					}
            				}else{
            					if(agentModel.isHasTemplate()){
            						agentId = agentModel.getAgentId();
            						hasAgent = true;
            					}
            				}
            			}else{
            				if(agentModel.isHasCol()){//自由协同
            					agentId = agentModel.getAgentId();
            					hasAgent = true;
            				}
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
            LOG.error("出现异常",e);
        }
        if(isEnabled || hasAgent){//人员可用状态都要显示
        	return user;
        }else{
        	return null;
        }
	}
	
	@Override
    public void onSuperNodeHumanHandled(Long workitemId) {
        CtpAffair affair = null;
        try {
            affair = affairManager.getAffairBySubObjectId(workitemId);
            if (affair != null) {
                affair.setState(StateEnum.col_done.key());
                affairManager.updateAffair(affair);
                ColSummary s = colManager.getColSummaryById(affair.getObjectId());
                if (s != null) {
                    String info = s.getCurrentNodesInfo();
                    info = info.replaceFirst(String.valueOf(affair.getMemberId()), "");
                    s.setCurrentNodesInfo(info);
                    colManager.updateColSummary(s);
                }
            }
        } catch (BusinessException e) {
            LOG.error("出现异常", e);
        }
    }
	
	@Override
    public void onSuperNodeWaitHumanHandle(Long workitemId, Long memberId, String msg) {
        CtpAffair affair = null;
        try {
            affair = affairManager.getAffairBySubObjectId(workitemId);
            if (affair != null) {
                affair.setMemberId(memberId);
                AffairUtil.setIsSuperNode(affair,true); 
                
                ColSummary s = colManager.getColSummaryById(affair.getObjectId());

                Long proxyMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.getKey(), memberId, s.getTempleteId(), affair.getSenderId());
                affair.setProxyMemberId(proxyMemberId);
                
                affairManager.updateAffair(affair);
                
                String info = s.getCurrentNodesInfo();
                if (info.endsWith(";")) {
                    info += memberId;
                }
                else {
                    info += ";" + memberId;
                }
                
                // 增加意见数量
                ColUtil.addOneReplyCounts(s);
                s.setCurrentNodesInfo(info);
                colManager.updateColSummary(s);
                
                String newMsg = null;
                String memberName = "";
                
                V3xOrgMember member = orgManager.getMemberById(memberId);
                if(member != null) {
                    memberName = member.getName();
                }
                
                if(Strings.isNotBlank(msg)) {
                    newMsg = ResourceUtil.getString("coll.summary.validate.lable27",msg,memberName);
                }else {
                    newMsg = ResourceUtil.getString("coll.summary.validate.lable28",memberName);
                }
                
               // 插入意见
                Comment comment = createSuperNodeComment(newMsg, affair);
                
                colManager.insertComment(comment, "");
                // 更新summary的意见数量
            }
            
        } catch (BusinessException e) {
            LOG.error("出现异常", e);
        }
    }
	
	@Override
    public void onSuperNodeBackHandle(Long workitemId, Long memberId, String msg) {
	    
	    CtpAffair affair = null;
        try {
            affair = affairManager.getAffairBySubObjectId(workitemId);
            if (affair != null) {
                
               // 插入意见
                Comment comment = createSuperNodeComment(msg,  affair);
                
                comment.setExtAtt3("collaboration.dealAttitude.rollback");
                
                colManager.insertComment(comment, "");
            }
            
        } catch (BusinessException e) {
            LOG.error("出现异常", e);
        }
    } 
	
	
	/**
	 * 超级节点控制流程时， 生成意见到意见列表
	 * 
	 * @param msg
	 * @param title
	 * @param moduledId
	 * @param affairId
	 * @param createrId
	 * @return
	 *
	 * @Since A8-V5 7.0SP1
	 * @Author      : xuqw
	 * @Date        : 2018年8月16日下午6:18:15
	 *
	 */
	private Comment createSuperNodeComment(String msg, CtpAffair affair){
	    
	    Comment comment = new Comment();
        
        comment.setContent(msg);
        comment.setTitle(affair.getSubject());
        comment.setHidden(false);
        comment.setClevel(1);
        comment.setModuleType(ApplicationCategoryEnum.collaboration.getKey());
        comment.setModuleId(affair.getObjectId());
        comment.setCtype(CommentType.comment.getKey());
        comment.setAffairId(affair.getId());
        comment.setCreateDate(new Date());
        comment.setCreateId(null);
        comment.setCreateName(affair.getNodeName());
        comment.setPid(0L);
        comment.setPath("pc");
        comment.setSubType(Comment.SubType.SUPERNODE.getKey());
        return comment;
	}
	
	
	public CtpAffair getCtpAffairBySubObjectId(Long workitemId){
        try {
            return affairManager.getAffairBySubObjectId(workitemId);
        } catch (BusinessException e) {
            LOG.error("出现异常", e);
        }
        return null;
    }
    @Override
    public void transDoSomethingAffterProcessFinish(long workitemId, WorkflowBpmContext context) throws BusinessException {
        
        CtpAffair affair = getCtpAffairBySubObjectId(workitemId);
        ColSummary summary = null;
        
        Object businessObject = context.getBusinessData(WorkFlowEventListener.COLSUMMARY_CONSTANT);
        if(businessObject != null){
            summary = (ColSummary) businessObject;
        }else{
            summary = colManager.getColSummaryById(affair.getObjectId());
        }
        
        List<Comment> list = Collections.emptyList();
        
        try {
            capFormManager.updateDataState(summary, affair, ColHandleType.finish, list);
        } catch (SQLException e) {
            LOG.error("动态更新表单数据失败", e);
            throw new BusinessException("动态更新表单数据失败", e);
        }
    }
    
    @Override
    public void policyNodesChanged(String processId, Object appObject, Map<String, String> map) {
		if(Strings.isBlank(processId) || map==null || map.isEmpty()){
			return;
		}
		/*	
			ColSummary summary  = (ColSummary) appObject;
			
			Map<String,Object> conditions = new HashMap<String,Object>();
			
			conditions.put("processId", processId);
			
			
			List<Long> activityIds = new ArrayList<Long>();
			for(String activityId : map.keySet()) {
				activityIds.add(Long.valueOf(activityId));
			}
			conditions.put("activityId", activityIds);*/
		
		/*List<Integer> states = new ArrayList<Integer>();
		states.add(StateEnum.col_pending.getKey());
		states.add(StateEnum.col_pending_repeat_auto_deal.getKey());
		conditions.put("state", states);*/
		
		try {
			/*List<CtpAffair> updateAffair = new ArrayList<CtpAffair>();
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
			*/
			
			for(String activityId : map.keySet()) {
				
				String updatePolicy = map.get(activityId);
				
				Map<String,Object> params=new HashMap<String, Object>();
				params.put("nodePolicy", updatePolicy);
				
				Object[][] wheres = new Object[][] {
					{"processId",processId},
					{"activityId",Long.valueOf(activityId)}};
				
				affairManager.update(params, wheres);
			}
			
			
			
			
		} catch (BusinessException e) {
			LOG.error("更新affair节点权限异常:", e);
		}
	}
    
    @Override
    public void onChangeProcessState(String processId) throws BusinessException {
        
        ColSummary summary= colManager.getColSummaryByProcessId(Long.valueOf(processId));
        
        ColUtil.updateCurrentNodesInfo(summary);
        
        colManager.updateColSummary(summary);
    }
    
    @Override
    public String getDefaultPolicyId4InsertNode(Long accountId,String processId) {
    	Map<String, String> defaultNode = new HashMap<String, String>();
		try {
			defaultNode = colManager.getColDefaultNode(accountId);
		} catch (BusinessException e) {
			LOG.error("加签获取协同默认节点权限错误", e);
		}
    	if(defaultNode==null || defaultNode.isEmpty()){
    		return ApplicationCategoryEnum.collaboration.name();
    	}
    	return defaultNode.get("defaultNodeName");
    }
    
    @Override
	public Map<String, Object> getFormValueMap(WorkflowBpmContext context, String rightId) {
    	 if(context==null){
             return null;
         }
         String masterId = context.getMastrid();
         String formAppId = context.getFormData();
         if(masterId==null || formAppId==null){
             return null;
         }
         masterId = masterId.trim();
         formAppId = formAppId.trim();
         if(Strings.isDigits(formAppId) && Strings.isDigits(masterId)){
             
        	 WorkflowApiManager wapi=(WorkflowApiManager)AppContext.getBean("wapi");
     	     WorkflowFormDataMapManager formDataManager = wapi.getWorkflowFormDataMapManager("form");
             Map<String,Object> rowDataDisplayName = formDataManager.getFormValueMap(Long.parseLong(formAppId), Long.parseLong(masterId), rightId);
             return rowDataDisplayName;
         }
         
         return null;
	}
    
    @Override
    public Map<String, WorkflowFormFieldBO> getFormDefinitionMap(String formApp) {
    	if(!Strings.isDigits(formApp)){
    		return null;
    	}
    	WorkflowApiManager wapi=(WorkflowApiManager)AppContext.getBean("wapi");
	    WorkflowFormDataMapManager formDataManager = wapi.getWorkflowFormDataMapManager("form");
        
        Map<String, WorkflowFormFieldBO> rowDataDisplayName = formDataManager.getFormFieldMap(formApp);
        return rowDataDisplayName;
    }
    
    @Override
    public boolean canCopyHistoryTemplate(Long formAppId) {
    	if(null == formAppId){
    		return false;
    	}
    	return capFormManager.isCAP4Form(formAppId);
    }
    
    @Override
    public String getAddedFirstNodePolicyId(Long accountId, String processId) {
    	return "collaboration";
    }
    public String getFormFieldDisplay(Long formAppId ,String fieldName){
    	return colManager.getFormFieldDisplay(formAppId, fieldName);
    }
    
    @Override
    public String getNodePolicyName(String id, Long accountId) {
    	String nodePolicyName = "";
    	if(accountId==null){
    		return nodePolicyName;
    	}
    	try {

    		nodePolicyName  = permissionManager.getPermissionName(EnumNameEnum.col_flow_perm_policy.name(), id, accountId);
			if(Strings.isBlank(nodePolicyName) && !accountId.equals(AppContext.currentAccountId())){
				nodePolicyName  = permissionManager.getPermissionName(EnumNameEnum.col_flow_perm_policy.name(), id, AppContext.currentAccountId());
			}
			if(Strings.isBlank(nodePolicyName)){
				nodePolicyName = "";
			}
		} catch (BusinessException e) {
			LOG.error("获取节点权限名称错误", e);
		}
    	return nodePolicyName;
    }

	@Override
	public Date getSummaryStartDate(String processId) {
		try {
			ColSummary summary= colManager.getColSummaryByProcessId(Long.parseLong(processId));
			return summary.getStartDate();
		} catch (Throwable e) {
			LOG.error("获取节点权限名称错误", e);
		}
		return null;
	}
    @Override
    public String getFormMasterId(Long processId) {
    	try {
			ColSummary summary = colManager.getColSummaryByProcessId(processId);
			if(summary != null){
				return summary.getFormRecordid() == null ? "" : String.valueOf(summary.getFormRecordid());
			}
		} catch (BusinessException e) {
			LOG.error("获取getFormMasterId错误", e);
		}
    	return "";
    }
	@Override
	public void sendMsgForWorkflowBugReport(Map<String, Object> messageMap) {
		colMessageManager.sendMsgForWorkflowBugReport(messageMap);
	}
	
	public void unLockBugReport(Long affairId) throws BusinessException {
		LOG.info("管理员unLockBugReport:affairId:" + affairId);
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
			if (LOG.isDebugEnabled()) {
				LOG.info("管理员unLockBugReport，解锁表单锁：summaryid:" + affair.getObjectId() + ",fromRecordId:"
						+ affair.getFormRecordid());
			}
			capFormManager.removeSessionMasterData(affair.getFormAppId(), affair.getFormRecordid());
			lockManager.unlock(affair.getFormRecordid());
		}
	}
	
	@Override
	public String transSkipNode(String nodeId,String nodeName,Set<Long> workItemIds,String skipWholeNode) throws BusinessException{
		
		
		if(Strings.isEmpty(workItemIds)) {
			LOG.info(AppContext.currentUserName()+"---nodeId:"+nodeId);
			return "";
		}
		
		if(workItemIds == null) {
			LOG.error(AppContext.currentUserName()+"skipWorkItem param workitemId is NULL");
			return "skipWorkItem param workitemId is NULL";
		}
		
		
		ColSummary summary = null;
		
		
		StringBuilder memberNames = new StringBuilder();
		
		List<CtpAffair> affairs = new ArrayList<CtpAffair>();
		
		for(Long workItemId : workItemIds) {
			
			CtpAffair affair = affairManager.getAffairBySubObjectId(workItemId);
			if(summary == null) {
                summary  = colManager.getColSummaryById(affair.getObjectId());
            }
			
			// 核定 等节点不能自动跳过
			String category = EnumNameEnum.col_flow_perm_policy.name();
			String configItem = ColUtil.getPolicyByAffair(affair).getId();
            Long accountId = ColUtil.getFlowPermAccountId(AppContext.currentAccountId(), summary);
            
            Permission permission = permissionManager.getPermission(category, configItem, accountId);   
            if (permission != null && !permission.getCanBackgroundDeal()) {// B节点是核定、新闻审批、公告审批、系统自定节点时，不能跳过。
                
                LOG.info("节点权限不能重复跳转:affairId:" + affair.getId() + ",nodepolicy:" + affair.getNodePolicy());
                
             // 节点权限为{0}
                return ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY, permission.getLabel())
                        + ResourceUtil.getString("collaboration.supervise.cannot.skipnode.js");
            }
			
			String memberName = Functions.showMemberNameOnly(affair.getMemberId());
			
			if(memberNames.length() != 0 ) {
				memberNames.append(",");
			}
			memberNames.append(memberName);
			
			BackgroundDealParamBO bo = new BackgroundDealParamBO();
			bo.setAffair(affair);
			bo.setDealType(BackgroundDealType.SKIPNODE);
			bo.setSummary(summary);
			bo.addExtParam(BackgroundDealParamBO.EXTPARAM_IS_NEED_CHECK_LOCK,Boolean.FALSE);
			bo.addExtParam(BackgroundDealParamBO.EXTPARAM_IS_MODIFYWORKFLOW_MODEL,true);
			bo.addExtParam(BackgroundDealParamBO.EXTPARAM_IS_NEED_PROCESSLOG,false);

			Comment c = ColSelfUtil.createNullCommentWithoutAttitude(affair, summary);
			bo.addExtParam(BackgroundDealParamBO.EXTPARAM_COMMENT,c);
			
			
			BackgroundDealResult result = collaborationApi.transfinishWorkItemInBackground(bo);
			
			if(!result.isCan()) {
				/**
				 * collaboration.supervise.cannot.skipnode.js 暂不支持跳转；
				 */
				return result.getMsg()+ResourceUtil.getString("collaboration.supervise.cannot.skipnode.js");
			}
			
			 //强制提交事务；
	        DBAgent.commit();
	        
	        affairs.add(affair);
		}
		
		colMessageManager.sendMessageForSkipNode(affairs);
		
		String logMsg = nodeName;
		if("false".equals(skipWholeNode)) {
			logMsg += "(" + memberNames.toString()+")";
		}
		
		/*跳过节点{0}*/
		processLogManager.insertLog(AppContext.getCurrentUser(), Long.parseLong(summary.getProcessId()),Long.valueOf(nodeId), 
				ProcessLogAction.skipNode,logMsg);
		appLogManager.insertLog(AppContext.getCurrentUser(),177, summary.getSubject());
		return "";
	}
	
	public Long getModuleIdByProcessId(String processId) {

		if (Strings.isDigits(processId)) {
			try {
				ColSummary s = colManager.getColSummaryByProcessId(Long.valueOf(processId));
				return s.getId();
			} catch (BusinessException e) {
				LOG.error("", e);
			}
		}
		return null;
	}
	
}
