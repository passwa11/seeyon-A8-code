/**
 * 
 */
package com.seeyon.apps.collaboration.quartz;

import com.seeyon.apps.collaboration.bo.BackgroundDealParamBO;
import com.seeyon.apps.collaboration.bo.BackgroundDealResult;
import com.seeyon.apps.collaboration.enums.BackgroundDealType;
import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum;
import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.dee.api.CollaborationFormBindEventListener;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.ContentUtil;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.permission.bo.CustomAction;
import com.seeyon.ctp.common.permission.bo.DetailAttitude;
import com.seeyon.ctp.common.permission.bo.NodePolicy;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.AttitudeManager;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.lock.Lock;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.quartz.QuartzJob;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.util.WorkflowMatchLogMessageConstants;
import com.seeyon.ctp.workflow.wapi.PopResult;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMCircleTransition;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;
import org.apache.commons.logging.Log;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;



/**
 * @Description: 服务器线程处理协同的接口，例如 定时任务、重复跳过、智能处理等
 * @author muj
 * @date 2018年3月7日 上午11:20:50
 * 
 */
public class CollProcessBackgroundManagerImpl implements CollProcessBackgroundManager ,QuartzJob{

    private static final Log LOG = CtpLogFactory.getLog(CollProcessBackgroundManagerImpl.class);
    private WorkflowApiManager wapi;
    private CAPFormManager capFormManager;
    private PermissionManager permissionManager;
    private CollaborationFormBindEventListener collaborationFormBindEventListener;
    private ColManager colManager;
    private AffairManager affairManager;
    private OrgManager orgManager;
    private MainbodyManager              ctpMainbodyManager;
    private AttitudeManager attitudeManager;
    
    public MainbodyManager getCtpMainbodyManager() {
		return ctpMainbodyManager;
	}

	public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
		this.ctpMainbodyManager = ctpMainbodyManager;
	}

    public ColManager getColManager() {
        return colManager;
    }

    public void setColManager(ColManager colManager) {
        this.colManager = colManager;
    }

    public AffairManager getAffairManager() {
        return affairManager;
    }

    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

    public OrgManager getOrgManager() {
        return orgManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public WorkflowApiManager getWapi() {
        return wapi;
    }

    public void setWapi(WorkflowApiManager wapi) {
        this.wapi = wapi;
    }

    public CAPFormManager getCapFormManager() {
        return capFormManager;
    }

    public void setCapFormManager(CAPFormManager capFormManager) {
        this.capFormManager = capFormManager;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public CollaborationFormBindEventListener getCollaborationFormBindEventListener() {
        return collaborationFormBindEventListener;
    }

    public void setCollaborationFormBindEventListener(CollaborationFormBindEventListener collaborationFormBindEventListener) {
        this.collaborationFormBindEventListener = collaborationFormBindEventListener;
    }

    public void setAttitudeManager(AttitudeManager attitudeManager) {
		this.attitudeManager = attitudeManager;
	}

	@Override
    public void execute(Map<String, String> parameters) {
        String affairId = parameters.get("affairId");
        String dealType = parameters.get("dealType");
        
        
        if(Strings.isBlank(affairId) || Strings.isBlank(dealType)){
            LOG.error("获取到的affairId为空！！affairId:"+affairId+",dealType:"+dealType);
            return ;
        }
        LOG.info("CollProcessBackgroundManagerImpl-execute-开始后台显示线程处理，affairId:"+affairId+",dealType:"+dealType);
        CtpAffair affair= null;
        ColSummary summary = null;
        try {
            affair = affairManager.get(Long.valueOf(affairId));
            summary = colManager.getColSummaryById(affair.getObjectId());
        } catch (Exception e1) {
            LOG.error("", e1);
        } 
        
        if(affair == null || summary == null ){
            LOG.error("数据为空！！affair:"+(affair==null)+",summary:"+(summary==null));
            return;
        }
        
        BackgroundDealParamBO bo = new BackgroundDealParamBO();
        bo.setAffair(affair);
        bo.setSummary(summary);
        bo.setDealType(BackgroundDealType.valueOf(dealType));
        
        try {
            transFinishWorkItem(bo);
        } catch (Exception e) {
           LOG.error("", e);
        }
    }
    
    private void initUser(long memberId){
        
        User user = AppContext.getCurrentUser();
        if(user == null){
            
            V3xOrgMember member = null;
            try {
                member = orgManager.getMemberById(memberId);
            } catch (BusinessException e) {
                LOG.error("", e);
            }
            if(member != null){
                
                user= new User();
                
                user.setId(member.getId());
                user.setDepartmentId(member.getOrgDepartmentId());
                user.setLoginAccount(member.getOrgAccountId());
                user.setLoginName(member.getLoginName());
                user.setName(member.getName());
                
                AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY, user);
            }
        }
    }
    @Override
    public void transFinishWorkItem(Long affairId, BackgroundDealType dealType) throws Exception {
        
        CtpAffair affair = affairManager.get(affairId);
      
        
        if(affair != null){
            
            LOG.info("CollProcessBackgroundManagerImpl-transFinishWorkItem-开始后台显示线程处理，affairId:"+affairId+",dealType:"+dealType.name());

            ColSummary s = colManager.getSummaryById(affair.getObjectId());

            BackgroundDealParamBO bo = new BackgroundDealParamBO();
            bo.setAffair(affair);
            bo.setSummary(s);
            bo.setDealType(dealType);
            
            transFinishWorkItem(bo);
            
        }
    }

    @Override
    public BackgroundDealResult  transFinishWorkItem(BackgroundDealParamBO threadDealParamBO) throws Exception {
        
        BackgroundDealResult  dealResult =  new BackgroundDealResult();
        dealResult.setCan(true);
        dealResult.setMsg(null);
        
        boolean isLock = false; // 是否加锁了

        // 1、参数处理,必填参数：affair/dealType,其他参数可以不录入
        
        CtpAffair affair = threadDealParamBO.getAffair();
        BackgroundDealType dealType = threadDealParamBO.getDealType();
        int count = threadDealParamBO.getCount();
        ColSummary summary = threadDealParamBO.getSummary();
        

        boolean affairIsNull = (affair == null);

        
        boolean affairIsPending = Integer.valueOf(StateEnum.col_pending.key()).equals(affair.getState());
        if(affairIsNull || !affairIsPending){
            LOG.info("--------------affair is null :"+affairIsNull+",affairIsPending:"+affairIsPending);
            dealResult.setCan(false);
            dealResult.setMsg(ResourceUtil.getString("coll.summary.validate.lable24"));
            return dealResult;
        }

        LOG.info("CollProcessBackgroundManagerImpl-开始后台显示线程处理，affairId:"+affair.getId()+",COUNT:"+count+",dealType:"+dealType.name());
        
        if(summary == null){
            summary = colManager.getColSummaryById(affair.getObjectId());
        }
        
        String processId = affair.getProcessId();
        String userId = String.valueOf(affair.getMemberId());
   
        //初始化User
        initUser(affair.getMemberId());
        
        try {
            
            // 2、锁校验
        	Boolean checkLock = (Boolean) threadDealParamBO.getExtParam(BackgroundDealParamBO.EXTPARAM_IS_NEED_CHECK_LOCK);
            if(!Boolean.FALSE.equals(checkLock)) {
            	
            	String[] result = wapi.lockWorkflowProcess(affair.getProcessId(), userId, 14, Lock.FROM_SYSTEM);
            	if ("false".equals(result[0])) {
            		
            		LOG.info("获取不到锁：affair.id:" + affair.getId());
            		
            		if(threadDealParamBO.isRetry()){
            			
            			if (count > 60) {
            				LOG.info("一直获取不到锁，放弃了！affair.id:" + affair.getId());
            			}
            			else{
            				Map<String,String> extParams = new HashMap<String,String>();
            				createQuartz(String.valueOf(affair.getId()),count,dealType,extParams);
            			}
            		}
            		
            		dealResult.setCan(false);
            		dealResult.setMsg(result[1]);
            		
            		return dealResult;
            	}
            	else {
            		LOG.info("sucess获取到锁：affair.id:" + affair.getId());
            		isLock = true;
            	}
            }

            
            Map<String, Object> extParam = threadDealParamBO.getExtParam();
            String attitudeKey = (String)extParam.get("attitudeKey");
            
            // 3、校验分支、表单必填等必须用户手动处理的情况
            String matchRequestToken = String.valueOf(UUIDLong.longUUID());
            CheckResult  checkResult = canSystemThreadDeal(dealType,matchRequestToken, summary, affair, attitudeKey);
            if (!checkResult.isCan()) {
                LOG.info("不能处理：affairId:" + affair.getId() + "," + dealResult.getMsg());
                dealResult.setCan(false);
                dealResult.setMsg(checkResult.getMsg());
                return dealResult;
            }

            // 组装意见
            Comment comment = (Comment) extParam.get(BackgroundDealParamBO.EXTPARAM_COMMENT);
           
            if(comment == null) {
            	comment = createComment(affair, summary, extParam);
            }
            
            // 4、正式处理。
            if (BackgroundDealType.AI.equals(dealType)) {
                affair.setAiProcessing(true);
            }

            Map<String,Object> params = new HashMap<String,Object>();
            params.put("conditionsOfNodes", checkResult.getBranchArgs());
            params.put("pr", checkResult.getPopResult());
            params.put(BackgroundDealParamBO.EXTPARAM_IS_MODIFYWORKFLOW_MODEL,threadDealParamBO.getExtParam(BackgroundDealParamBO.EXTPARAM_IS_MODIFYWORKFLOW_MODEL));
            params.putAll(threadDealParamBO.getExtParam());

            dealResult = this.transDeal(summary, affair, comment,params);
            
        } catch (Exception e) {
            LOG.error("", e);
        } finally {
            if (isLock) {
                wapi.releaseWorkFlowProcessLock(processId,userId, 14);
            }
        }
        return dealResult;
    }

    private Comment createComment(CtpAffair affair, ColSummary summary,Map<String,Object> extParam) {
        Comment comment = new Comment();
        comment.setHidden(false);
        comment.setId(UUIDLong.longUUID());
        if(null != extParam && null != extParam.get("attitude")){
        	String attitudeString = (String)extParam.get("attitude");
        	String attitudeKeyString = (String)extParam.get("attitudeKey");
        	
        	comment.setExtAtt1(attitudeString);
        	
        	if (CommentExtAtt1Enum.haveRead.i18nLabel().equals(attitudeKeyString)) {
        		comment.setExtAtt4(CommentExtAtt1Enum.haveRead.name());
        	} else if (CommentExtAtt1Enum.agree.i18nLabel().equals(attitudeKeyString)) {
        		comment.setExtAtt4(CommentExtAtt1Enum.agree.name());
        	} else if (CommentExtAtt1Enum.disagree.i18nLabel().equals(attitudeKeyString)) {
        		comment.setExtAtt4(CommentExtAtt1Enum.disagree.name());
        	} 
        }else{
        	// 意见设置为自动跳过
        	Permission permission = getNodePermission(summary, affair);
        	if (permission != null) {
        		String attitude = getDisplayAttitude(permission);
        		comment.setExtAtt1(attitude);
        		try {
					comment.setExtAtt4(attitudeManager.getAttitudeCodeByPermission(attitude, permission));
				} catch (BusinessException e) {
					LOG.error("通过节点权限获取态度code出错",e);
				}
        	}
        }

        comment.setCtype(Comment.CommentType.comment.getKey());
        comment.setPid(0L);
        comment.setPraiseNumber(0);
        comment.setPraiseToComment(false);
        comment.setPraiseToComment(false);
        comment.setModuleType(ModuleType.collaboration.getKey());
        comment.setModuleId(summary.getId());
        comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
        comment.setAffairId(affair.getId());
        comment.setCreateId(affair.getMemberId());
        comment.setForwardCount(0);
        comment.setPath(AppContext.getCurrentUser().getUserAgentFrom()==null?"pc":AppContext.getCurrentUser().getUserAgentFrom());
        
        comment.setDepartmentId(affair.getMatchDepartmentId());
        comment.setPostId(affair.getMatchPostId());
        comment.setAccountId(affair.getMatchAccountId());
        
        return comment;
    }

    private String getDisplayAttitude(Permission permission ) {
    	//取默认态度
    	String defaultAttitude = permission.getNodePolicy().getDefaultAttitude();
    	if(Strings.isBlank(defaultAttitude)){
    		//默认态度不存在时,取第一个态度
    		String attitude =  permission.getNodePolicy().getAttitude();
    		if (Strings.isNotBlank(attitude)) {
    			defaultAttitude = attitude.split(",")[0];
    		}
    	}
    	
    	String showAttitude = "";
    	DetailAttitude detailAttitude = permission.getNodePolicy().getDatailAttitude();
    	if (null != detailAttitude) {
    		if ("haveRead".equals(defaultAttitude)) {
    			showAttitude = detailAttitude.getHaveRead();
    		} else if ("agree".equals(defaultAttitude)) {
    			showAttitude = detailAttitude.getAgree();
    		} else if ("disagree".equals(defaultAttitude)) {
    			showAttitude = detailAttitude.getDisagree();
    		}
    	}
        return showAttitude;
    }

    private BackgroundDealResult excuteDee(ColSummary colSummary, CtpAffair affair, Comment comment,PopResult pr) throws BusinessException {
        BackgroundDealResult dealResult = new BackgroundDealResult();
        dealResult.setCan(true);
        dealResult.setMsg(null);
        
        if (!String.valueOf(MainbodyType.FORM.getKey()).equals(affair.getBodyType())) {
            return dealResult;
        }
        
        if (collaborationFormBindEventListener != null) {

            String attitude = "";
            String content = "";
            if (comment != null) {
                attitude = comment.getExtAtt1();
                content = comment.getContent();
            }
            
            String isNodeLastMember = "false";
            if(pr != null){
                isNodeLastMember = pr.getCurrentNodeLast();
            }
            
            Map<String, String> ret = collaborationFormBindEventListener.checkBindEventBatch(affair.getId(), affair.getFormAppId(), affair.getMultiViewStr(), colSummary.getId(), ColHandleType.finish,
                    attitude, content,isNodeLastMember);

            if (!"true".equals(ret.get("success"))) {
                String msg = ret.get("msg");
                LOG.info("DEE执行失败 - affair.id:" + affair.getId() + ",msg:" + msg);
                dealResult.setCan(false);
                dealResult.setMsg(msg);
            }
        }
        return dealResult;
    }

    private BackgroundDealResult transDeal(ColSummary colSummary, CtpAffair affair, Comment comment,Map<String,Object> params) throws Exception {

        
        BackgroundDealResult dealResult = new BackgroundDealResult();
        dealResult.setCan(true);
        dealResult.setMsg(null);
        
        String currentNodeLast = "";
        PopResult pr = (PopResult)params.get("pr");
        
        if (pr != null) {
            currentNodeLast = pr.getCurrentNodeLast();
        }
        
        // 处理前事件
        String msg = ColUtil.executeWorkflowBeforeEvent(affair, "BeforeFinishWorkitem", currentNodeLast,comment);
        if (Strings.isNotBlank(msg)) {
            LOG.error("BeforeFinishWorkitem - affair.id:" + affair.getId() + ",currentNodeLast：" + currentNodeLast + ",msg:" + msg);
            dealResult.setCan(false);
            dealResult.setMsg(msg);
            return dealResult;
        }
        
        //流程结束前事件
        if(null != pr && "true".equals(pr.getLast())){
            String eventMsg = ColUtil.executeWorkflowBeforeEvent(affair, "BeforeProcessFinished", currentNodeLast,comment);
            if (Strings.isNotBlank(eventMsg)) {
                LOG.error("BeforeProcessFinished - affair.id:"+affair.getId()+msg);
                dealResult.setCan(false);
                dealResult.setMsg(eventMsg);
                return dealResult;
            }
        }
        
        // 执行DEE
        dealResult = excuteDee(colSummary, affair, comment, pr);
        if (!dealResult.isCan()) {
            return dealResult;
        }
        
        // 更新表单初始值,只会节点权限 & 加签只读不赋值表单初始值
        if (ColUtil.isForm(affair.getBodyType()) && !AffairUtil.isFormReadonly(affair) && !"inform".equals(affair.getNodePolicy())) {
         
            try {
                // 更新表单初始值
                String rightId = ContentUtil.findRightIdbyAffairIdOrTemplateId(affair, affair.getTempleteId());
                capFormManager.procDefaultValue(affair.getFormAppId(), affair.getFormRecordid(),rightId, affair.getObjectId(),null);
                LOG.info("表单初始值赋值：formAppId:"+colSummary.getFormAppid()+"getFormRecordid:"+colSummary.getFormRecordid()+",rightId:"+rightId
                        +",summary.getId():"+colSummary.getId()+",affairid:"+affair.getId());
            } catch (Exception e) {
                throw new BusinessException("设置表单初始值异常", e);
            }
        }
        
     
        params.put("currentNodeLast", currentNodeLast);
        
        
        colManager.transFinishWorkItemPublic(affair, colSummary, comment, ColHandleType.finish, params);
        
        return dealResult;

    }

    class CheckResult {
        // 是否能处理
        private boolean can;
        // 不能处理情况下的提示语
        private String msg;
        // 分支数据
        private String branchArgs;
        
        private PopResult popResult;
        
        
        

        public PopResult getPopResult() {
            return popResult;
        }

        public void setPopResult(PopResult popResult) {
            this.popResult = popResult;
        }

        public boolean isCan() {
            return can;
        }

        public void setCan(boolean can) {
            this.can = can;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getBranchArgs() {
            return branchArgs;
        }

        public void setBranchArgs(String branchArgs) {
            this.branchArgs = branchArgs;
        }
    }

    /**
     * B节点是核定、新闻审批、公告审批、封发（带交换类型的节点）时，不能跳过。<br>
     * B节点有必填项时不能自动跳过。<br>
     * B节点后有手动分支和非强制分支和选人不能跳过。强制分支不需要手动选择的可以跳过。<br>
     * B节点有子流程不能跳过。<br>
     * 对于被加签人员与加签人员同一个人时，和下一节点需要选人但又选择了自己的情况，不支持跳过<br>
     * B节点处理意见必填时不能跳过<br>
     * 
     * @throws BusinessException
     */
    @SuppressWarnings("deprecation")
    private CheckResult canSystemThreadDeal(BackgroundDealType dealType,String matchRequestToken, ColSummary summary, CtpAffair affair, String attitude) throws BusinessException {

        CheckResult cr = new CheckResult();

        String msg = "";
        boolean can = true;
        String branchArgs = "";
        Object formdata =null;
        String processId = affair.getProcessId();
        
        boolean isSpecialBacked = Integer.valueOf(SubStateEnum.col_pending_specialBacked.getKey()).equals(affair.getSubState());
        boolean isSepcial = Integer.valueOf(SubStateEnum.col_pending_specialBackCenter.getKey()).equals(affair.getSubState())
                || Integer.valueOf(SubStateEnum.col_pending_specialBack.getKey()).equals(affair.getSubState())
                || isSpecialBacked;
        if (isSepcial) {
            
            // 允许指定回退给我，移动端快速处理
            if(!BackgroundDealType.QUICK.equals(dealType) || !isSpecialBacked){

                LOG.info("指定回退不能重复跳过:affairId:" + affair.getId());
                msg = ResourceUtil.getString(WorkflowMatchLogMessageConstants.PROCESS_AFFAIR_IN_SPECIAL_STEPBACK);
                can = false;
            }
        }
        
        Permission permission = getNodePermission(summary, affair);
        
        if (can) {
        	        	
            if (permission!=null && !permission.getCanBackgroundDeal()) {// B节点是核定、新闻审批、公告审批、系统自定节点时，不能跳过。
                LOG.info("节点权限不能重复跳过:affairId:" + affair.getId() + ",nodepolicy:" + affair.getNodePolicy());
                //节点权限为{0}
                msg = ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY, permission.getLabel());
                
                can = false;
            }
        }
    
        if (can) {
            // B节点有必填项时不能自动跳过。
            if (ColUtil.isForm(affair.getBodyType())) {// 表单类型的数据
                boolean isAffairReadOnly = AffairUtil.isFormReadonly(affair);
                if (!"inform".equals(affair.getNodePolicy()) && !isAffairReadOnly) {
                    Set<String> fields = capFormManager.getNotNullableFields(affair.getFormAppId(), affair.getMultiViewStr());
                    LOG.info("[getNotNullableFields],入参：FormAppid():" + affair.getFormAppId() + ",MultiViewStr:" + affair.getMultiViewStr());
                    if (Strings.isNotEmpty(fields)) {
                        LOG.info("表单必须填写，不能重复跳过:affairId:" + affair.getId());
                        msg = ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_FORM_FIELD_MUSTWRITE) + Strings.join(fields, ",");
                        can = false;
                    }
                }
              //校验表单校验规则
                Map<String, Object> ret = capFormManager.checkRule(summary.getId(), summary.getFormAppid(), summary.getFormRecordid(), affair.getMultiViewStr());
                formdata = ret.get("formData");
                String isPass = (String)ret.get("isPass");
                if ("false".equals(isPass)) {
                    LOG.info("表单校验规则不对" + affair.getId());
                    msg = ResourceUtil.getString("collaboration.batch.alert.notdeal.31");
                    can = false;
                }
            }
        }

        if(can){
        	boolean isForm = ColUtil.isForm(affair.getBodyType());
            if (isForm) {
                List<CtpContentAll> content = ctpMainbodyManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, affair.getObjectId());
                if (Strings.isNotEmpty(content)) {
                    String s = content.get(0).getContent();
                    if (Strings.isNotBlank(s) && Strings.isDigits(s)) {
                    	msg = ResourceUtil.getString("coll.summary.validate.lable25");
                        can = false;
                    }
                }
            }
        }
        if (can) {
            // 前面节点触发的子流程是否已经结束
            if (ColUtil.isForm(affair.getBodyType())) {
                boolean isPreNewFlowFinish = !wapi.hasUnFinishedNewflow(affair.getProcessId(), String.valueOf(affair.getActivityId()));
                if (!isPreNewFlowFinish) {
                    LOG.info("表单触发的子流程没有结束，不能重复跳过:affairId:" + affair.getId());
                    msg = ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_FLOWSUB_UNFINISHED);
                    can = false;
                }
            }
        }

        if (can) {
            // 当前跳过节点是否有子流程
            boolean isFromTemplate = false;
            if (affair != null) {
                isFromTemplate = affair.getTempleteId() != null && affair.getTempleteId().longValue() != -1;
            }
            BPMProcess bpmProcess = wapi.getBPMProcess(processId);
            if (bpmProcess != null) {
                BPMActivity bpmActivity = bpmProcess.getActivityById(affair.getActivityId().toString());
                if (bpmActivity != null) {
                    BPMSeeyonPolicy bpmSeeyonPolicy = bpmActivity.getSeeyonPolicy();
                    if (bpmSeeyonPolicy != null) {
                        boolean hasNewflow = isFromTemplate && bpmSeeyonPolicy != null && "1".equals(bpmSeeyonPolicy.getNF());
                        // 当前跳过的节点后面是否有分支或者需要选人或者人员不可用
                        if (hasNewflow) {
                            msg = ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_FLOWSUB);
                            LOG.info("设置了触发子流程");
                            can = false;
                        }
                    }
                }
            }

        }
        PopResult pr =  null;
        if (can) {
            // 判断当前处理人员是否为当前流程节点的最后一个处理人,是决定流程走向的人
            boolean isExecuteFinished = wapi.isExecuteFinished(affair.getProcessId(), affair.getSubObjectId());
            if (isExecuteFinished) {// 是决定流程走向的人

                // 知会节点超期-自动跳过，有流程分支或选人选项，忽略并自动跳转
                if (!"inform".equals(affair.getNodePolicy()) && !"zhihui".equals(affair.getNodePolicy())) {
                    String formAppId = affair.getFormAppId() == null ? null : affair.getFormAppId().toString();
                    String masterId = affair.getFormRecordid() == null ? null : affair.getFormRecordid().toString();
                    // 当前跳过的节点后面是否有分支或者需要选人或者人员不可用
                    
                    pr = isPop(matchRequestToken, ResourceUtil.getString(WorkflowMatchLogMessageConstants.step5), affair, affair.getProcessId(), affair.getCaseId(),
                            affair.getSenderId(), formAppId, masterId, "collaboration");

                    cr.setPopResult(pr);
                    
                    if ("true".equals(pr.getPopResult())) {
                        LOG.warn("该协同待办需要触发新流程或者后面节点需要进行分支匹配、选择执行人或人员不可用，不允许执行自动跳过操作。colSummaryId:=" + affair.getObjectId() + ";  affairId:=" + affair.getId());
                        msg = pr.getMsg();
                        can = false;
                    }else{
                        branchArgs = pr.getConditionsOfNodes();
                    }

                }
            }
        }

        // 意见必填
        if (can) {
            // 不同意校验
            if (permission != null) {
                
                NodePolicy nodePolicy = permission.getNodePolicy();
                boolean isDisagree = CommentExtAtt1Enum.disagree.getI18nLabel().equals(attitude);
                
                // 快速处理， 不同意需要选择操作，不允许处理
                if(BackgroundDealType.QUICK.equals(dealType) && isDisagree){
                    
                    CustomAction customAction = nodePolicy.getCustomAction();
                    if(customAction != null 
                            && "1".equals(customAction.getIsOptional())
                            && !"Continue".equals(nodePolicy.getCustomAction().getOptionalAction()) ){
                        can = false;
                        // 很抱歉，由于管理员将"不同意"配置了其他操作，需要进入详情处理
                        msg = ResourceUtil.getString("collaboration.deal.disagreeCannotQuickDeal");
                    }
                }
                
                if(can){
                    Integer opinionPolicy = nodePolicy.getOpinionPolicy();
                    Integer disAgredOpinionPlicy = nodePolicy.getDisAgreeOpinionPolicy();
                    
                    // 不同意意见必填
                    boolean _disAgredOpinionPlicy = Integer.valueOf(1).equals(disAgredOpinionPlicy) && isDisagree;
                    
                    if (Integer.valueOf(1).equals(opinionPolicy) || _disAgredOpinionPlicy) {
                        LOG.info("意见必须填写,不能重复跳过，  affairId:=" + affair.getId());
                        msg = ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_OPINIONMUSTWRITE);
                        can = false;
                    }
                }
            }
        }

        // 环形分支
        if (can) {
            boolean isCircle = this.isCircleNode(processId, affair.getActivityId().toString());
            if (isCircle) {
                LOG.info("该协同待办存在环形分支，不能重复跳过，  affairId:=" + affair.getId());
                msg = ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_LINK_CIRCLE);
                can = false;
            }
        }

        if(can){
     	   
     	   if (ColUtil.isForm(affair.getBodyType()) && !AffairUtil.isFormReadonly(affair) && !"inform".equals(affair.getNodePolicy())) {
 		          
 		        String rightId = ContentUtil.findRightIdbyAffairIdOrTemplateId(affair, affair.getTempleteId());
 		        try {
 					capFormManager.procDefaultValue(summary.getFormAppid(), summary.getFormRecordid(), rightId, affair.getObjectId(),formdata);
 				} catch (SQLException e) {
 					 LOG.error("表单初始值赋值异常",e);
 				}
 		        LOG.info("表单初始值赋值：formAppId:"+summary.getFormAppid()+"getFormRecordid:"+summary.getFormRecordid()+",rightId:"+rightId
                        +",summary.getId():"+summary.getId()+",affairid:"+affair.getId());
 		    }
              
         }
        
        cr.setCan(can);
        cr.setMsg(msg);
        cr.setBranchArgs(branchArgs);

        //各种处理单独的校验规则
        if(BackgroundDealType.AI.equals(dealType)){
            canDealAI(summary, affair, cr);
            
        }
        return cr;
    }
    
    private CheckResult canDealAI(ColSummary summary, CtpAffair affair,CheckResult cr) throws BusinessException {
        
        
        // 校验表单校验规则
        Map<String, Object> ret = capFormManager.checkRule(summary.getId(), summary.getFormAppid(), summary.getFormRecordid(), affair.getMultiViewStr());
        
        String isPass = (String)ret.get("isPass");
        
        LOG.info("[checkRule]节点超期自动跳过,传入参数：formMasterDataId：" + summary.getFormRecordid() + ", formAppID:" + summary.getFormAppid() + ",summaryId:" + summary.getId()
        +",isPass:"+isPass);
        
        if ("false".equals(isPass)) {// 强制校验
            String msg = ResourceUtil.getString(ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_CAN_AUTOSKIP_ISSTRONGVALIDATE));
            cr.setCan(false);
            cr.setMsg(msg);
            return cr;
        }
        
        if(Integer.valueOf(SubStateEnum.col_pending_takeBack.key()).equals(affair.getSubState())){
            String msg = "取回的事项不能被智能处理,"+affair.getId();
            LOG.info(msg);
            cr.setCan(false);
            cr.setMsg(msg);
            return cr;
        }
        
        if(Integer.valueOf(SubStateEnum.col_pending_Back.key()).equals(affair.getSubState())){
            String msg = "回退的事项不能被智能处理,"+affair.getId();
            LOG.info(msg);
            cr.setCan(false);
            cr.setMsg(msg);
            return cr;
        }
        return cr;
    }

    private Permission getNodePermission(ColSummary summary, CtpAffair affair) {
        Permission permission = null;
        try {
            String configItem = ColUtil.getPolicyByAffair(affair).getId();

            String category = EnumNameEnum.col_flow_perm_policy.name();
            Long accountId = ColUtil.getFlowPermAccountId(AppContext.currentAccountId(), summary);
            permission = permissionManager.getPermission(category, configItem, accountId);
            return permission;
        } catch (BusinessException e) {
            LOG.error("", e);
        }
        return permission;
    }

    private PopResult isPop(String matchRequestToken, String validateStep, CtpAffair affair, String processId, Long caseId, Long startUserId, String formAppId, String materId, String appName)
            throws BPMException {
        WorkflowBpmContext wfContext = new WorkflowBpmContext();
        wfContext.setProcessId(processId);
        wfContext.setCaseId(caseId);
        wfContext.setCurrentActivityId(String.valueOf(affair.getActivityId()));
        wfContext.setCurrentWorkitemId(affair.getSubObjectId());
        wfContext.setFormData(formAppId);
        wfContext.setMastrid(materId);
        wfContext.setAppName(appName);
        wfContext.setStartUserId(String.valueOf(startUserId));
        wfContext.setCurrentUserId(String.valueOf(AppContext.getCurrentUser().getId()));
        wfContext.setBussinessId(String.valueOf(affair.getObjectId()));
        wfContext.setSysAutoFinishFlag(true);
        wfContext.setIsValidate(true);
        wfContext.setMatchRequestToken(matchRequestToken);
        wfContext.setAutoSkipNodeId(affair.getActivityId().toString());
        wfContext.setValidateStep(validateStep);
        OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
        try {
            wfContext.setCurrentAccountId(orgManager.getMemberById(affair.getMemberId()).getOrgAccountId().toString());
        } catch (BusinessException e) {
            LOG.error("", e);
        }
        wapi = (WorkflowApiManager) AppContext.getBean("wapi");
        PopResult pr = wapi.isPop(wfContext);
       // LOG.info("r0:=" + result[0] + ";r1:=" + result[1] + ";r2:=" + result[2]);
        return pr;
    }

    private boolean isCircleNode(String processId, String activityId) throws BPMException {

        boolean ret = false;

        BPMProcess process = wapi.getBPMProcess(processId);
        BPMActivity myNode = process.getActivityById(activityId);
        List<BPMCircleTransition> circleList = myNode.getDownCirlcleTransitions();
        if (Strings.isNotEmpty(circleList) && circleList.size() > 0) {
            ret = true;
        }
        return ret;
    }
    
    private static void createQuartz(String affairId,int count,BackgroundDealType dealType,Map<String,String> extParams){
        if(Strings.isEmpty(affairId)){
            return ;
        }
        String name = "CollThreadDeal" + affairId +"_"+System.currentTimeMillis()+"_"+Math.random();
        Map<String, String> datamap = new HashMap<String, String>(2);
        datamap.put("affairId", affairId);
        datamap.put("count",String.valueOf(count+1));
        datamap.put("dealType",dealType.name());
        datamap.putAll(extParams);
        //当前时间之后的30秒
        int randomInOneMinte = (int)(Math.random()*20+1)*1000;
        Date excuteTime =  new java.util.Date(System.currentTimeMillis()+randomInOneMinte);
        
        try {
            if(QuartzHolder.newQuartzJob(name, excuteTime , "collProcessBackgroundManager", datamap)){
                LOG.info("__CollProcessBackgroundManagerImpl____增加定时任务__ids:"+affairId+"dealType:"+dealType.name()+",定时任务name:"+name+",预计执行时间:"+excuteTime);
            }else{
                LOG.info("__CollProcessBackgroundManagerImpl___增加定时任务__ids:"+affairId+",定时任务name:"+name+",失败了、失败了、失败了!");
            }
            
        } catch (Throwable e) {
            LOG.error("",e);
        }
    
    }

   
}
