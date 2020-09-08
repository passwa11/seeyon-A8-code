package com.seeyon.v3x.edoc.controller.newedoc;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.common.isignaturehtml.manager.ISignatureHtmlManager;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.AffairManager;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.office.HtmlHandWriteManager;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.web.util.WebUtil;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.MemberPost;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.edoc.constants.EdocConstant;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocCategory;
import com.seeyon.v3x.edoc.domain.EdocDocTemplate;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.domain.RegisterBody;
import com.seeyon.v3x.edoc.enums.EdocOpenFrom;
import com.seeyon.v3x.edoc.manager.EdocCategoryManager;
import com.seeyon.v3x.edoc.manager.EdocFormManager;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.manager.EdocMarkDefinitionManager;
import com.seeyon.v3x.edoc.manager.EdocRegisterManager;
import com.seeyon.v3x.edoc.manager.EdocRoleHelper;
import com.seeyon.v3x.edoc.manager.EdocSummaryQuickManager;
import com.seeyon.v3x.edoc.manager.EdocSwitchHelper;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.edoc.util.NewEdocHelper;
import com.seeyon.v3x.edoc.webmodel.EdocFormModel;
import com.seeyon.v3x.exchange.domain.EdocExchangeTurnRec;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.manager.RecieveEdocManager;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class NewEdocHandle{

    protected String register;
    protected String meetingSummaryId;
    protected String comm;
    protected String from;
    protected String s_summaryId;
    protected String templeteId;
    protected String oldtempleteId;
    protected String edocType;
    protected long subType;
    protected long registerId;
    protected int iEdocType;
    protected String checkOption;
    private String  openFrom;
    
    
   

	protected EdocCategory edocCategory = null;
    protected EdocSummary summary = null;
    protected EdocBody body = null;
    protected EdocForm defaultEdocForm = null;
    protected EdocOpinion senderOpinion = null;
    protected List<Attachment> atts = null;       
    protected long distributeEdocId = -1;
    protected EdocRecieveRecord record = null;
    protected EdocRegister edocRegister = null;
    protected RegisterBody registerBody = null;
    protected long templeteProcessId = 0L;
    protected CtpTemplate templete = null;
    
    protected com.seeyon.ctp.common.authenticate.domain.User user;
    protected String content = "";
    protected boolean cloneOriginalAtts = false;//是否允许附件复制
    protected boolean isFromWaitSend = false;//来自待发
    protected boolean isSystem = true;//是否是系统模板
    protected boolean canUpdateContent=true;//是否允许修改正文
    protected boolean canDeleteOriginalAtts = true;//是否允许删作附件
    protected Long edocFormId = 0L;
    protected String bodyContentType = null;
    protected Integer standarduration = 0;//基准时长，在调用模板的时候对其赋值（直接调用模板|来自待发的模板）
    protected String templeteType = ""; //模板类型(templete,text,workflow)
    protected boolean openTempleteOfExchangeRegist;
    
    protected EdocManager edocManager;
    protected EdocRegisterManager edocRegisterManager; 
    protected EdocFormManager edocFormManager;
    protected FileManager fileManager;
    protected EdocMarkDefinitionManager edocMarkDefinitionManager;
    protected AttachmentManager attachmentManager;
    protected AffairManager affairManager;
    protected TemplateManager templeteManager;
    protected SuperviseManager superviseManager;
    protected RecieveEdocManager recieveEdocManager;
    protected OrgManager orgManager;
    private EdocCategoryManager edocCategoryManager;
    protected EdocSummaryQuickManager edocSummaryQuickManager;
    protected DocApi docApi;
    private ISignatureHtmlManager        iSignatureHtmlManager;
    
    
    private WorkflowApiManager wapi  = (WorkflowApiManager) AppContext.getBean("wapi");
    
    protected HtmlHandWriteManager htmlHandWriteManager;
	
    public void setDocApi(DocApi docApi) {
        this.docApi = docApi;
    }

	public void setEdocCategoryManager(EdocCategoryManager edocCategoryManager) {
		this.edocCategoryManager = edocCategoryManager;
	}

    public NewEdocHandle(){}
    
    public void setParams(String register,
		String meetingSummaryId,
		String comm,
		String from,
        String s_summaryId,
        String templeteId,
        String oldtempleteId,
        String edocType,
        String subTypeStr,
        String registerIdStr,
        String checkOption,
        String _openForm){
    	
        this.user = AppContext.getCurrentUser();
        this.register = register;
        this.meetingSummaryId = meetingSummaryId;
        this.comm = comm;
        this.from = from;
        this.s_summaryId = s_summaryId;
        this.templeteId = templeteId;
        this.oldtempleteId = oldtempleteId;
        this.edocType = edocType;
        
        this.subType = Strings.isBlank(subTypeStr)?-1:Long.parseLong(subTypeStr);//发文单类型
        this.iEdocType = Strings.isBlank(edocType)?-1:Integer.parseInt(edocType);//公文类型
        this.registerId = Strings.isBlank(registerIdStr)?-1:Long.parseLong(registerIdStr);//登记id
        this.checkOption=Strings.isBlank(checkOption) ? "" : checkOption;
        this.openFrom = _openForm;
    }
    
    /**
     * 创建文单需要的EdocSummary对象，在子类中实现
     * @param request
     * @param modelAndView
     * @throws Exception
     */
    public abstract void createEdocSummary(HttpServletRequest request, ModelAndView modelAndView)throws Exception;
    
    public ModelAndView execute(HttpServletRequest request,HttpServletResponse response,ModelAndView modelAndView) throws Exception{

        /**
         * 收文分发相关
         */
        //公文交换调用模板分发
        openTempleteOfExchangeRegist = "distribute".equals(register);
         
        this.edocManager = (EdocManager)AppContext.getBean("edocManager");
        this.edocRegisterManager = (EdocRegisterManager)AppContext.getBean("edocRegisterManager");
        this.edocFormManager = (EdocFormManager)AppContext.getBean("edocFormManager");
        this.fileManager = (FileManager)AppContext.getBean("fileManager");
        this.edocMarkDefinitionManager = (EdocMarkDefinitionManager)AppContext.getBean("edocMarkDefinitionManager");
        this.attachmentManager = (AttachmentManager)AppContext.getBean("attachmentManager");
        this.affairManager = (AffairManager)AppContext.getBean("affairManager");
        this.recieveEdocManager = (RecieveEdocManager)AppContext.getBean("recieveEdocManager");
        this.superviseManager = (SuperviseManager)AppContext.getBean("superviseManager");
        this.orgManager = (OrgManager)AppContext.getBean("orgManager");
        this.templeteManager = (TemplateManager)AppContext.getBean("templateManager");
        this.edocSummaryQuickManager = (EdocSummaryQuickManager)AppContext.getBean("edocSummaryQuickManager");
        this.docApi = (DocApi)AppContext.getBean("docApi");
        this.htmlHandWriteManager=(HtmlHandWriteManager) AppContext.getBean("htmlHandWriteManager");
        this.iSignatureHtmlManager=(ISignatureHtmlManager) AppContext.getBean("iSignatureHtmlManager");
        
        EnumManager enumManager = (EnumManager)AppContext.getBean("enumManagerNew");

        PermissionManager permissionManager = (PermissionManager)AppContext.getBean("permissionManager");
        
        /********************************调用子类构造EdocSummary对象*************************************/
        String barCode=request.getParameter("barCode");
        try{
            createEdocSummary(request, modelAndView);
        }catch(NewEdocHandleException e){
            if(e.getCode() == NewEdocHandleException.PRINT_CODE){
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
                out.print(e.getMsg());
                return null;
            }
            else if(e.getCode() == NewEdocHandleException.REDIRECT_CODE){
                return new ModelAndView("common/redirect", "redirectURL", BaseController.REDIRECT_BACK);
            }
        }
        /********************************调用子类构造EdocSummary对象*************************************/
        long affairId = Strings.isBlank(request.getParameter("affairId"))?-1:Long.parseLong(request.getParameter("affairId"));
        CtpAffair affair = null;
        if(affairId!=-1){
            affair= affairManager.get(affairId);
        }
        /**
         * 包装公文数据
         */
        if(summary.getStartTime()==null){//自动带出拟文时间。
            summary.setStartTime(new Timestamp(System.currentTimeMillis()));
        }
        //BUG_普通_V5_V5.1sp1_青田县农村信用合作联社_收文单中登记日期不能自动带出系统日期_20150317007671
        if(summary.getRegistrationDate() == null) {
        	summary.setRegistrationDate(new Date(System.currentTimeMillis()));
        }
        String papery = request.getParameter("papery");//1:纸质公文；2：非纸质公文
        boolean isPapery = Strings.isNotBlank(papery) && "1".equals(papery);
        if(Strings.isBlank(summary.getSendUnit()) && Strings.isBlank(summary.getSendUnitId()) && !isPapery){
            summary.setSendUnit(EdocRoleHelper.getAccountById(user.getLoginAccount()).getName());
            summary.setSendUnitId("Account|"+Long.toString(user.getLoginAccount()));
        }
     
        if(Strings.isBlank(summary.getSendDepartment()) && Strings.isBlank(summary.getSendDepartmentId())){
            Long deptId = 0L;
            String deptName = "";
            if(!user.getAccountId().equals(user.getLoginAccount())) {
                Long accountId = user.getLoginAccount();
                Map<Long, List<MemberPost>> map=orgManager.getConcurentPostsByMemberId(accountId, user.getId());
                Set<Long> set = map.keySet();
                if(Strings.isNotEmpty(set)){
                    deptId = set.iterator().next();
                    deptName=orgManager.getDepartmentById(deptId).getName();
                }
            } else {
                deptId = user.getDepartmentId();
                deptName = EdocRoleHelper.getDepartmentById(user.getDepartmentId()).getName();
            }
            /**
             * 默认获取一级部门作为发文部门
             */
            V3xOrgDepartment department = orgManager.getDepartmentById(deptId);
            int i = 0;
            while(department.getSuperior() != null && !"".equals(department.getSuperior()) && i < 5) {
            	V3xOrgDepartment department_temp = orgManager.getDepartmentById(department.getSuperior());
            	if(department_temp != null && department_temp.getType().equals(OrgConstants.UnitType.Department)) {
            		department = department_temp;
            	}else {
            		deptId = department.getId();
            		deptName = department.getName();
            		break;
            	}
            }
            /**
             * 默认获取一级部门作为发文部门
             */
            summary.setSendDepartment(deptName);
            summary.setSendDepartmentId("Department|"+Long.toString(deptId));
        }
        
        //预归档
        Long archiveId = null;
        String archiveName = "";
        String fullArchiveName = "";
        if(summary.getArchiveId() != null){
            archiveId = summary.getArchiveId(); 
            if(AppContext.hasPlugin("doc")){
                archiveName =  edocManager.getShowArchiveNameByArchiveId(archiveId);
                fullArchiveName = edocManager.getFullArchiveNameByArchiveId(archiveId);
            }
        }
        
        modelAndView.addObject("archiveName", archiveName);
        modelAndView.addObject("fullArchiveName", fullArchiveName);
        //代理人可以登记
        long checkSendAclUserId = user.getId();
        if("distribute".equals(comm) || "register".equals(comm) ||  openTempleteOfExchangeRegist) {
            if(edocRegister != null) {
                checkSendAclUserId=edocRegister.getDistributerId();
            }
        }
        if(summary.getOrgAccountId() == null || summary.getOrgAccountId().intValue() == 0){
	        //判断是否是代理人处理
	        if(user.getId().longValue() != checkSendAclUserId){//是代理
	        	V3xOrgMember agentToMem=orgManager.getMemberById(checkSendAclUserId);
	        	summary.setOrgAccountId(agentToMem.getOrgAccountId());
	        }else{
	        	summary.setOrgAccountId(user.getLoginAccount());
	        }
        }
        
        if(defaultEdocForm == null){
            ///////////////////////////////默认公文单
            defaultEdocForm = edocFormManager.getDefaultEdocForm(summary.getOrgAccountId(),iEdocType);
        }
        
        //检查是否有公文发起权
        int roleEdocType = iEdocType;
        if(roleEdocType == EdocEnum.edocType.recEdoc.ordinal() && EdocHelper.isG6Version()){
        	roleEdocType = EdocEnum.edocType.distributeEdoc.ordinal();
        }
        boolean isEdocCreateRole=EdocRoleHelper.isEdocCreateRole(summary.getOrgAccountId(),checkSendAclUserId,roleEdocType);
        // 当人员调出到另外的单位时，可以继续处理在之前单位的待登记文
        if(!isEdocCreateRole && edocRegister != null && edocRegister.getRecieveUserId() != null && edocRegister.getRecieveUserId().equals(checkSendAclUserId)) {
            isEdocCreateRole = EdocRoleHelper.isEdocCreateRole(user.getLoginAccount(), checkSendAclUserId, roleEdocType);
        }
        //检测是否代理登记
        if(!isEdocCreateRole){
        	List<Long> agentIds = MemberAgentBean.getInstance().getAgentToMemberId(ApplicationCategoryEnum.edoc.key(),checkSendAclUserId );
        	if(Strings.isNotEmpty(agentIds)){
        		for(Long agentId : agentIds){
        			isEdocCreateRole=EdocRoleHelper.isEdocCreateRole(summary.getOrgAccountId(),agentId,roleEdocType);
        			if(isEdocCreateRole)break;
        		}
        	}
        }
        
        //如果是阅转办，则允许直接转，不用判断是否有公文发起权--start--这种修改是临时的，发版后继续修改，阅转办存在数据结构问题
        if("3".equals(checkOption)) {
            isEdocCreateRole=true;
            summary.setProcessType(Long.valueOf(1)); //阅转办后，处理类型应该是办文。杨帆直接修改的，有问题找我
        }
        //如果是阅转办，则允许直接转，不用判断是否有公文发起权--end--这种修改是临时的，发版后继续修改，阅转办存在数据结构问题
       
        //检查是否有归档修改权
        int modifyType=EdocEnum.ArchiveModifyType.archiveModifySendEdoc.ordinal();
        switch(summary.getEdocType()){
        case 1:
           modifyType=EdocEnum.ArchiveModifyType.archiveModifyRecEdoc.ordinal();
           break;
        case 2:
           modifyType=EdocEnum.ArchiveModifyType.archiveModifySignEdoc.ordinal();
           break;
        default:
           modifyType=EdocEnum.ArchiveModifyType.archiveModifySendEdoc.ordinal();
           break;
        }
        boolean edocCreateRole=EdocRoleHelper.isEdocArchivedModifyRole(summary.getOrgAccountId(),checkSendAclUserId,modifyType);
        if("archived".equals(from) ){//如果是从公文归档修改按钮进来的
            if(!edocCreateRole){//但是没有公文归档修改权的提醒
                response.setContentType("text/html;charset=UTF-8");
                String szJs = "alert(parent.edocLang.alert_not_edocUPPower);";
                szJs += "if(window.dialogArguments) {"; // 弹出
                szJs += "   window.returnValue = \"true\";";
                szJs += "   window.close();";
                szJs += "} else {";
                szJs += "   parent.parent.location.href='edocController.do?method=listIndex&listType=listDoneAll&from=listDone&edocType="+edocType+"';";
                szJs += "}";

                szJs += "";
                rendJavaScript(response,szJs);
                return null;
            }
        }else{
            if(isEdocCreateRole==false && !"agent".equals(request.getParameter("app"))) {//没有公文发起权不能发送
                response.setContentType("text/html;charset=UTF-8");
                String szJs = "";
                if(roleEdocType==1){
                	szJs = "alert(parent.edocLang.alert_not_edoc_rec_create);";
                }else if(roleEdocType==3){
                	szJs = "alert(parent.edocLang.alert_not_edoc_fenfa_create);";
                }else{
                	szJs = "alert(parent.edocLang.alert_not_edoccreate);";
                }
                szJs += "if(window.dialogArguments) {"; // 弹出
                szJs += "   window.returnValue = \"true\";";
                szJs += "   window.close();";
                szJs += "} else {";
                szJs += "   parent.parent.location.href='edocController.do?method=listIndex&from=listPending&listType=listPending&edocType="+edocType+"';";
                szJs += "}";
                rendJavaScript(response,szJs);
                return null;
            }
        }
        //页面显示的公文单列表。如果是模板，只取当前模板的。
        String domainIds = orgManager.getUserIDDomain(user.getId(), V3xOrgEntity.VIRTUAL_ACCOUNT_ID, V3xOrgEntity.ORGENT_TYPE_ACCOUNT);
        int _iEdocType = iEdocType;
        String readToDeal = request.getParameter("pageview");
        if("listReading".equals(readToDeal) || "listReaded".equals(readToDeal)){
        	_iEdocType = 1;
        }
        List<EdocForm> edocForms = NewEdocHelper.getLoginAccountOrCurrentTempleteEdocForms(summary.getOrgAccountId(),domainIds,_iEdocType,templeteId,edocFormId, templeteType, subType);
       
        if(defaultEdocForm == null && Strings.isNotEmpty(edocForms)){
        	defaultEdocForm = edocForms.get(0);
        }
        if(!edocForms.contains(defaultEdocForm) && defaultEdocForm != null){
        	edocForms.add(defaultEdocForm);
        }
        ///////////////////////////////没有找到公文单，请到管理界面建立公文单
        if(Strings.isEmpty(edocForms)) {
        	response.setContentType("text/html;charset=UTF-8");
            String szJs = "alert(\""+ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource","alert_nofind_edocForm")+"\");self.history.back();";
            rendJavaScript(response,szJs);
            return null;
        }
        
        if(Strings.isNotBlank(s_summaryId)&&"archived".equals(from)){//从已发中打开的时候，使用原来的文单
        	EdocSummary oldSummary=edocManager.getEdocSummaryById(Long.valueOf(s_summaryId),false);
        	edocFormId=oldSummary.getFormId();
        }else{
        	edocFormId=defaultEdocForm.getId();
        }
        
        modelAndView.addObject("registerId", registerId);
        modelAndView.addObject("edocForms",edocForms);
        modelAndView.addObject("attachments", atts);
        modelAndView.addObject("canDeleteOriginalAtts", canDeleteOriginalAtts);
        modelAndView.addObject("cloneOriginalAtts", cloneOriginalAtts);
        
        CtpEnumBean remindMetadata = enumManager.getEnum(EnumNameEnum.common_remind_time.name());
        CtpEnumBean  deadlineMetadata= enumManager.getEnum(EnumNameEnum.collaboration_deadline.name());
        
        modelAndView.addObject("remindMetadata", remindMetadata);
        modelAndView.addObject("deadlineMetadata", deadlineMetadata); 
        modelAndView.addObject("controller", "edocController.do");
        modelAndView.addObject("appName",EdocEnum.getEdocAppName(iEdocType));
        int templeteCategrory = 0;
        if(iEdocType==0){
            templeteCategrory = ModuleType.edocSend.ordinal();
        }else if(iEdocType == 1){
            templeteCategrory = ModuleType.edocRec.ordinal();
            //判断是否有收文待发页签
            //boolean enableRecWaitSend = AppContext.hasResourceCode(EdocConstant.F07_RECWAITSEND);
            //不再对资源进行判断，直接返回true
            boolean enableRecWaitSend = true;
            modelAndView.addObject("enableRecWaitSend", String.valueOf(enableRecWaitSend));     
        }else if(iEdocType == 2){
            templeteCategrory = ModuleType.edocSign.ordinal();
        }
        modelAndView.addObject("templeteCategrory",templeteCategrory);
        
        if(iEdocType == 1){
        	//--将record去掉-- A8的登记人也存在登记表的distributerId中
        	if(edocRegister != null && Strings.isBlank(summary.getCreatePerson())){
        		long registerUserId = edocRegister.getDistributerId();
        		V3xOrgMember mb = orgManager.getMemberById(registerUserId);
        		summary.setCreatePerson(mb.getName());
        		summary.setStartUserId(registerUserId);
        	}
        }
        
        /**
         * 默认节点权限
         */
        CtpEnumBean flowPermPolicyMetadata = null; 
        String defaultPerm = "shenpi";
        if(EdocEnum.edocType.recEdoc.ordinal()==iEdocType) {
            modelAndView.addObject("policy", "dengji");
            modelAndView.addObject("newEdoclabel", "edoc.new.type.rec"+Functions.suffix());
            flowPermPolicyMetadata =  enumManager.getEnum(EnumNameEnum.edoc_rec_permission_policy.name());
        } 
        else if(EdocEnum.edocType.sendEdoc.ordinal()==iEdocType) {
            modelAndView.addObject("policy", "niwen");
            modelAndView.addObject("newEdoclabel", "edoc.new.type.send");
          flowPermPolicyMetadata = enumManager.getEnum(EnumNameEnum.edoc_send_permission_policy.name());
        }  
        else {
            modelAndView.addObject("policy", "niwen");
            modelAndView.addObject("newEdoclabel", "edoc.new.type.send");
          flowPermPolicyMetadata= enumManager.getEnum(EnumNameEnum.edoc_qianbao_permission_policy.name());
        }
        modelAndView.addObject("defaultPermLabel", "node.policy."+defaultPerm);
        modelAndView.addObject("flowPermPolicyMetadata",flowPermPolicyMetadata);
        
        //从草稿箱编辑，进入新建页面时回显流程
        String pageview = request.getParameter("pageview");
       
        String processId = summary.getProcessId();
        
        //设置工作流相关信息到页面中，展现工作流
        boolean isSpecialSteped= affair!=null && affair.getSubState() != null && affair.getSubState()==SubStateEnum.col_pending_specialBacked.key();
        processId = getWorkFlowInfo4New(modelAndView, flowPermPolicyMetadata,processId,isSpecialSteped);
        
        
        modelAndView.addObject("caseId",summary.getCaseId()==null?-1l:summary.getCaseId());
        boolean isFormatOrTextTemplate= false;//是否为格式模板
        if(null!=templete && null!=templete.getType() && "text".equals(templete.getType())){
            isFormatOrTextTemplate= true;
        }
        if(Strings.isNotBlank(processId) && null!=summary.getTempleteId() 
                && summary.getTempleteId()!=-1L && summary.getTempleteId()!=0L && !isFormatOrTextTemplate){//模板流程被撤销
            modelAndView.addObject("isRepealTemplate",true);
            modelAndView.addObject("isSystemTemplate",isSystem);
        }else if(Strings.isNotBlank(processId) ){//自由公文被撤销
            modelAndView.addObject("isRepealFree",true);
        }else{
            modelAndView.addObject("isRepealTemplate",false);
            modelAndView.addObject("isSystemTemplate",isSystem);
            modelAndView.addObject("isRepealFree",false);
        }
        long actorId = -1;

    	//OA-33699 客户bug：授权给外单位来用的公文模板，外单位拟文人，拟文节点权限跟外单位的一致
        Long orgAccountId = null;
        if (templete != null){
        	orgAccountId = templete.getOrgAccountId();
        	//普通A8-V5BUG_V5.0sp2_工银安盛人寿保险有限公司_有一个公文的模板是word正文  但是发起之后出现了标准正文的格式_20140322023846,如果是office正文的模板，本地没有安装office的话就给个提示
        	modelAndView.addObject("templeteBodyTyep", templete.getBodyType());
        	modelAndView.addObject("isSystemTemplate", templete.isSystem());
        	}
        else if("listReading".equals(pageview)|| "listReaded".equals(pageview)){
        	orgAccountId=user.getLoginAccount();
        }else{
          orgAccountId = summary.getOrgAccountId();
          if(summary.getEdocBodies() != null && summary.getEdocBodies().size()>0){
        	  modelAndView.addObject("A8registerBodyType", summary.getFirstBody().getContentType()); //BUG_OA-66448_紧急_V5_V5.0sp2_安徽山鹰纸业股份有限公司_公文进行收文后，有些收文流程的正文丢失_20140729001855_140801
          }
        }
        if(!"archived".equals(from)){//归档可修改时，所有的公文元素都可以编辑，而不是根据节点权限来判断。actorId为-1时，所有元素都可编辑了。否则会以拟文节点权限来编辑
            actorId=permissionManager.getPermission(EdocUtil.getEdocMetadataNameEnum(iEdocType).name()
                    , EdocUtil.getSendFlowpermNameByEdocType(iEdocType), orgAccountId.longValue()).getFlowPermId();
        }

        if("distribute".equals(comm) || openTempleteOfExchangeRegist) {
            int contentNo = 0;
        	if(summary.getFirstBody() != null && summary.getFirstBody().getContentNo() != null){
        		contentNo = summary.getFirstBody().getContentNo();
        	}
            if(registerBody != null) {
                contentNo = registerBody.getContentNo().intValue();
            }
            if(contentNo == EdocBody.EDOC_BODY_SECOND) {
                summary.setSendUnit(summary.getSendUnit2());
                summary.setSendUnit2("");
                summary.setSendUnitId(summary.getSendUnitId2());
                summary.setSendUnitId2("");
                summary.setSendDepartment(summary.getSendDepartment2());
                summary.setSendDepartment2("");
                summary.setSendDepartmentId(summary.getSendDepartmentId2());
                summary.setSendDepartmentId2("");
                summary.setDocMark(summary.getDocMark2());
                summary.setDocMark2("");
                summary.setCopies(summary.getCopies2());
                summary.setSendTo(summary.getSendTo2());
                summary.setSendTo2("");
                summary.setSendToId(summary.getSendToId2());
                summary.setSendToId2("");
                summary.setCopyTo(summary.getCopyTo2());
                summary.setCopyTo2("");
                summary.setCopyToId(summary.getCopyToId2());
                summary.setCopyToId2("");
                summary.setReportTo(summary.getReportTo2());
                summary.setReportTo2("");
                summary.setReportToId2(summary.getReportToId2());
                summary.setReportToId2("");
            }   
        }
        
        //校验sendUnitId数据是否正确
        if(!Strings.isBlank(summary.getSendUnitId())) {
            if(summary.getSendUnitId().indexOf("|")<0){summary.setSendUnitId("Account|"+summary.getSendUnitId());}
        }
        if(!Strings.isBlank(summary.getSendUnitId2())) {
            if(summary.getSendUnitId2().indexOf("|")<0){summary.setSendUnitId2("Account|"+summary.getSendUnitId2());}
        }
        //校验sendDepartmentId数据是否正确
        if(!Strings.isBlank(summary.getSendDepartmentId())) {
            if(summary.getSendDepartmentId().indexOf("|")<0){summary.setSendDepartmentId("Department|"+summary.getSendDepartmentId());}
        }
        if(!Strings.isBlank(summary.getSendDepartmentId2())) {
            if(summary.getSendDepartmentId2().indexOf("|")<0){summary.setSendDepartmentId2("Department|"+summary.getSendDepartmentId2());}
        }
        
        /**
         * 所有数据装载在EdocFormModel对象中
         */
        WebUtil.setRequest(request);
        EdocFormModel fm = null;
        if(StringUtils.isNotEmpty(templeteId)) {
            fm = edocFormManager.getEdocFormModel(edocFormId,summary,actorId,false,true);
            modelAndView.addObject("isTempleteHasDeadline", summary.getDeadline()!=null && summary.getDeadline() != 0);
            modelAndView.addObject("isTempleteHasRemind", summary.getAdvanceRemind()!=null && summary.getAdvanceRemind() != -1);
            modelAndView.addObject("isTempleteHasArchiveId", summary.getArchiveId()!=null);
        } else {
        	//外单位签收并分发的时候送文单位应该是登记单中的成文单位,而不是当前单位
        	if(edocRegister!=null){
        		//jl修改,普通G6BUG_G6_v1.0sp1_成都市教育局_收文流程撤销后，草稿中，来文单位不显示了_20130624018033
        		String unit = edocRegister.getEdocUnit();
        		if((Strings.isBlank(summary.getSendUnit())) && unit != null){
        		    summary.setSendUnit(unit);
                    summary.setSendUnitId(edocRegister.getEdocUnitId());
        		}
        	}
        	if(Strings.isBlank(summary.getDocMark()) && summary.getTempleteId() != null){
        		CtpTemplate templete = templeteManager.getCtpTemplate(summary.getTempleteId());
            	EdocSummary templateSummary = (EdocSummary) XMLCoder.decoder(templete.getSummary());
            	summary.setDocMark(templateSummary.getDocMark());
        		fm = edocFormManager.getEdocFormModel(edocFormId,summary,actorId,false,true);
        	}else{
        		fm = edocFormManager.getEdocFormModel(edocFormId,summary,actorId);
        	}
        }
        summary.setIdIfNew();
        fm.setEdocBody(summary.getFirstBody());
        
        if("0".equals(checkOption) || "3".equals(checkOption)){
            if(content!=null && !"".equals(content)){
                summary.getFirstBody().setContent(content);
                fm.setEdocBody(summary.getFirstBody());
            }
        }else if("1".equals(checkOption)){//转发文，如果是正文作为附件转发过来，正文要清空，不作任何事
            if(StringUtils.isNotEmpty(templeteId)){ 
                fm.setEdocBody(body);//但这时调用模板的话，要取模板的正文//GOV-4935
            }else{
                fm.setEdocBody(null);//转发文，如果是正文作为附件转发过来，正文要清空，不作任何事
            }
        }else{
            fm.setEdocBody(summary.getFirstBody());
        }
        
      //判断条件和下面一致，无法合并 ...
        if((("register".equals(comm) 
            || "distribute".equals(comm) 
            || openTempleteOfExchangeRegist)
            ))    {

            if(edocRegister!=null) {
                Long accountId = edocRegister.getOrgAccountId();
                accountId = accountId == null  ? 0L : accountId;
                
                Integer registerType = edocRegister.getRegisterType();
                
                canUpdateContent = (registerType == EdocRegister.REGISTER_TYPE_BY_PAPER_REC_EDOC 
                                            || registerType == 2
                                            || registerType == 3) ? true : 
                                //A8签收时插入的登记数据registerType
                                EdocSwitchHelper.canUpdateAtOutRegist(accountId);
            } 
        }
        
        if((("register".equals(comm) 
    		|| "distribute".equals(comm) 
    		|| openTempleteOfExchangeRegist)
    		) && !EdocOpenFrom.listWaitSend.name().equals(openFrom))	{

            if(edocRegister!=null) {
            	//复制之前先删除ISIgnatureHTML专业签章
            	iSignatureHtmlManager.deleteAllByDocumentId(summary.getId());
            	//复制签章
            	iSignatureHtmlManager.save(edocRegister.getEdocId(),summary.getId());
                //登记单正文
                //组装分发正文和附件 wangjingjing begin
                if(null!=registerBody && null!=registerBody.getContentNo()){
                    EdocBody edocBody = summary.getBody(registerBody.getContentNo().intValue());
                    if(null!=edocBody){
                        fm.setEdocBody(edocBody);
                    }else{
                        fm.setEdocBody(summary.getFirstBody());
                    }
                }
            } 
            summary.setEdocOpinion(null);
            summary.setEdocOpinions(null);
            senderOpinion=null;
        }else if("transmitSend".equals(comm)){
            summary.setNewId(); 
            fm.getEdocSummary().setNewId();         
        }
        
        fm.setEdocSummaryId(summary.getId());
        fm.setSenderOpinion(senderOpinion);
        fm.setDeadline(summary.getDeadline());
        if(summary.getDeadlineDatetime()!=null){
        	fm.setDeadline4temp(Datetimes.formatDatetimeWithoutSecond(summary.getDeadlineDatetime()));
        }
        
        //特殊字符替换 OA-83571
        String xslt = fm.getXslt();
        xslt = xslt.replaceAll("&", "&amp;");
        fm.setXslt(xslt);
        
        modelAndView.addObject("formModel", fm);
        modelAndView.addObject("edocFormId", edocFormId);
        
        modelAndView.addObject("contentRecordId", summary.getEdocBodiesJs());
        /*if(isFromWaitSend) {
            comm = "distribute";
        }*/
        modelAndView.addObject("comm",comm);
        //hasBody1,hasBody2:主要用途：联合发文有多套正文，判断是否存在套红后的多套正文
        
        if(summary != null) {
            modelAndView.addObject("hasBody1", summary.getBody(1)!=null);
            modelAndView.addObject("hasBody2", summary.getBody(2)!=null);
        }

        //OA-22553 兼职人员test01在兼职单位的公文自建流程开关未开，流程框也提示不可自建流程，但编辑流程可以新建
        //当后台设置开关时，设置不能自建流程时，就不能编辑流程了，只能查看流程 (最开始 按钮置灰)
        //而且要在调用模板之后，当templeteProcessId有值时，才能点查看流程
        boolean selfCreateFlow = EdocSwitchHelper.canSelfCreateFlow();
        //从待发中编辑就 不设查看流程为置灰了
        //当自建流程开关关闭后，这里是直接拟文后，不调用模板
        if(!selfCreateFlow ){
            //如果调用的是格式模板，编辑流程就不置灰了
            Long templeteId = summary.getTempleteId();
            //当待发编辑时，如果来自模板时，重新获得模板类型(这种情况需要通过CtpTemplate中得到)
            if(templeteId != null && "".equals(templeteType)){
            	CtpTemplate tem = templeteManager.getCtpTemplate(templeteId);
            	if (tem != null) {
            		templeteType = tem.getType();
				}
            }
        	if((templeteId == null && Strings.isBlank(s_summaryId)) || "text".equals(templeteType)) {
        		//只读流程按钮 置灰标志
                modelAndView.addObject("readWorkflow","disabled");
        	}
        }
        
        //二维码扫描不受后台这个开关控制
        if(selfCreateFlow==false&&"true".equals(barCode)){
        	selfCreateFlow=true;
        }
        
        modelAndView.addObject("selfCreateFlow",selfCreateFlow);
        modelAndView.addObject("canUpdateContent",canUpdateContent);
        modelAndView.addObject("actorId",actorId);
        modelAndView.addObject("appType",EdocUtil.getAppCategoryByEdocType(iEdocType).getKey());
        
        long edocAccountId = 0L;
        //模板的公文开关权限应该取模板制作单位的权限控制
        if(summary.getTempleteId()!=null){
            CtpTemplate t = templeteManager.getCtpTemplate(summary.getTempleteId());
            if(t!=null){
            	edocAccountId = t.getOrgAccountId();
            }
        }
        //不是模板时，取公文所属单位权限控制
        else{
            edocAccountId = summary.getOrgAccountId();
        }
        modelAndView.addObject("personInput", EdocSwitchHelper.canInputEdocWordNum(edocAccountId));

//        String logoURL = EdocHelper.getLogoURL(summary.getOrgAccountId());
//        modelAndView.addObject("logoURL",logoURL);
        //String logoURL = com.seeyon.ctp.system.Constants.DEFAULT_LOGO_NAME;
        //modelAndView.addObject("logoURL", "<img src='/seeyon"+logoURL+"' />");
        modelAndView.addObject("logoURL", EdocHelper.getLogoURL());
        modelAndView.addObject("standardDuration", standarduration == null?0:standarduration);
        
        
        long distributerId = user.getId();
        if(edocRegister != null) {
            
            if("agent".equals(request.getParameter("app"))) {
                if(affairId != -1) {
                    if(affair != null && !affair.getMemberId().equals(AppContext.getCurrentUser().getId())) {
                        if(affair.getApp() == 24) {//收文登记
                            boolean hasRegistButton = EdocRoleHelper.isEdocCreateRole(user.getLoginAccount(), user.getId(), EdocEnum.edocType.recEdoc.ordinal());
                            if(!hasRegistButton) {  
                                modelAndView.addObject("isAgent", true);
                            }
                        } else if(affair.getApp() == 34) {//收文分发
                            if(!isEdocCreateRole) {
                                modelAndView.addObject("isAgent", true);
                            }
                        }                   
                    }
                } else {
                    modelAndView.addObject("isAgent", true);
                }
            }
            distributerId = edocRegister.getDistributerId();
        }
        
        if(distributeEdocId == -1) {
            distributeEdocId = summary.getId();
        }
        
        modelAndView.addObject("distributerId", distributerId);
        modelAndView.addObject("distributeEdocId", distributeEdocId);
        
        
        //保存调用工作流相关接口所需要的参数
        if(iEdocType == 1 && edocRegister != null){
        	//有课V5登记的时候 是代理人登记的，发起的收文流程的发起人需要设置为 被代理人
    		long registerUserId = edocRegister.getDistributerId();
    		V3xOrgMember mb = orgManager.getMemberById(registerUserId);
    		modelAndView.addObject("currentUserId", registerUserId);
        	modelAndView.addObject("currentUserName", mb.getName());
        	modelAndView.addObject("currentUserAccountName", user.getLoginAccountName());
            modelAndView.addObject("currentUserAccountId", user.getLoginAccount());
        }else{
        	modelAndView.addObject("currentUserId", user.getId());
        	modelAndView.addObject("currentUserName", user.getName());
        	modelAndView.addObject("currentUserAccountName", user.getLoginAccountName());
            modelAndView.addObject("currentUserAccountId", user.getLoginAccount());
        }
        
        
        
        //判断是否有发文及签报待发页签
        boolean enableWaitSend = false;
        if ((iEdocType==0 && AppContext.hasResourceCode(EdocConstant.F07_SENDWAITSEND))||
                (iEdocType==2 && AppContext.hasResourceCode(EdocConstant.F07_SIGNWAITSEND))
            ){
            enableWaitSend=true;
        }
                
        modelAndView.addObject("enableWaitSend", String.valueOf(enableWaitSend));   
        modelAndView.addObject("recieveId",request.getParameter("recieveId"));

        String waitRegister_recieveId = request.getParameter("waitRegister_recieveId");
        String newSummaryId = request.getParameter("newSummaryId");
        if(Strings.isNotBlank(newSummaryId)){ 
            EdocRecieveRecord edRe = recieveEdocManager.getEdocRecieveRecordByReciveEdocId(Long.parseLong(newSummaryId));
            if(edRe!=null){
            	waitRegister_recieveId = String.valueOf(edRe.getId());
            }
        } 
        if(Strings.isNotBlank(templeteId)&&Strings.isNotBlank(waitRegister_recieveId)){
        	CtpTemplate ctpTemplate= templeteManager.getCtpTemplate(Long.parseLong(templeteId));
        	if(null!=ctpTemplate && Integer.valueOf(20).equals(ctpTemplate.getModuleType())){
        		modelAndView.addObject("canUpdateContent",EdocSwitchHelper.canUpdateAtOutRegist());
        	}
        }     
        //发文种类相关
        if(EdocHelper.isG6Version()&& Integer.valueOf(EdocEnum.edocType.sendEdoc.ordinal()).equals(summary.getEdocType())){
        	getEdocCategory(modelAndView, edocForms,user);
        }
        	
        
        //快速发文相关--start
        //***交换类型 start***
		List<V3xOrgMember> memberList = new ArrayList<V3xOrgMember>();
		Set<Long> deptSenderList=new HashSet<Long>();
		if (iEdocType==0) {
    		int exchangeTypeSwitchValue=EdocSwitchHelper.getDefaultExchangeType(); //交换类型读取单位的开关。
    		modelAndView.addObject("exchangeTypeSwitchValue",exchangeTypeSwitchValue);
    		
    		String exchangeDeptTypeSwitchValue = EdocSwitchHelper.getDefaultExchangeDeptType(user.getLoginAccount());
    		modelAndView.addObject("exchangeDeptTypeSwitchValue", exchangeDeptTypeSwitchValue);
    		
    		//分发人部门取所有单位的部门
			memberList = EdocRoleHelper.getAccountExchangeUsers(user.getLoginAccount());
			deptSenderList=EdocHelper.getDeptSenderList(user.getId());
			Iterator<Long> iterator = deptSenderList.iterator();
			StringBuilder deptList= new StringBuilder();
			while(iterator.hasNext()){
			    Long deptId = iterator.next();
				V3xOrgDepartment dept=orgManager.getDepartmentById(deptId);
				if(dept!=null){
					if(deptList.length() > 0){
					    deptList.append("|");
					}
					deptList.append(dept.getId())
					        .append(",")
					        .append(dept.getName());
				}
			}
			
			//拟文人部门取当前登录单位的
			StringBuilder createrExchangeDepts = new StringBuilder();
			Set<Long>  createSenderDeptList = EdocHelper.getDeptSenderList(user.getId(), user.getLoginAccount());
			for(Long deptId : createSenderDeptList){
			    V3xOrgDepartment dept=orgManager.getDepartmentById(deptId);
                if(dept != null){
                    if(createrExchangeDepts.length() == 0){
                        createrExchangeDepts.append(dept.getId()+","+dept.getName());
                    }else{
                        createrExchangeDepts.append("|"+dept.getId()+","+dept.getName());
                    }
                }
			}
			
			modelAndView.addObject("deptSenderList", deptList);
			modelAndView.addObject("createrExchangeDepts", createrExchangeDepts);
			modelAndView.addObject("memberList", memberList);
		}
        //***交换类型 end ***
		
		//***正文套红start***
		List<EdocDocTemplate> taohongList = EdocHelper.getEdocDocTemplate(user.getLoginAccount(),user,"edoc",null);
		modelAndView.addObject("taohongList", taohongList);
		//***正文套红 end ***
		
        //快速发文相关--end
        	
		modelAndView.addObject("showBanwenYuewen", EdocSwitchHelper.showBanwenYuewen(edocAccountId));
		
		
		/**
		 * 如果是上级单位转的收文，要在收文单的右上方显示：转收文信息
		 */
		if(summary.getEdocType() == 1){
			if(edocRegister != null){
				record = recieveEdocManager.getEdocRecieveRecord(edocRegister.getRecieveId());
			}
			if(record != null){
				if(Integer.valueOf(EdocExchangeTurnRec.TURN_REC_TYPE).equals(record.getIsTurnRec())){
					modelAndView.addObject("showTurnRecInfo", "true");
					modelAndView.addObject("supEdocId", record.getEdocId());
				}
                V3xOrgAccount unit = null;
                if(EdocRecieveRecord.Exchange_Receive_iAccountType_Org == record.getExchangeType()){
                    unit = orgManager.getAccountById(record.getExchangeOrgId());
                }else if(EdocRecieveRecord.Exchange_Receive_iAccountType_Dept == record.getExchangeType()){
				    V3xOrgDepartment department = orgManager.getDepartmentById(record.getExchangeOrgId());
				    if(department != null) {
                        unit = orgManager.getAccountById(department.getOrgAccountId());
                    }
                }
                if(unit != null) {
                    // 签收后收文登记时，如果当前人是替兼职单位处理文，则需要使用兼职单位的单位id，打开流程图
                    modelAndView.addObject("recPostAccountId", unit.getId());
                    modelAndView.addObject("recPostAccountName", unit.getName());
                }
				orgManager.getAccountById(record.getExchangeOrgId());
			}
		}
		
		
        return modelAndView;
    }

	private String getWorkFlowInfo4New(ModelAndView modelAndView,CtpEnumBean flowPermPolicyMetadata, String processId,boolean isSpecialSteped) throws BusinessException, BPMException {
		if( Strings.isNotBlank(processId) && ( "0".equals(processId) || "-1".equals(processId))){
            processId= null;
        }
        String workflowNodesInfo= "";
        boolean isTemplateSystem = templete != null && (templete.isSystem() || templete.getFormParentid() != null);
        if(!isSpecialSteped && isTemplateSystem && !TemplateEnum.Type.text.name().equals(templeteType)){
        	if(templete.getFormParentid() != null){
        		 CtpTemplate pTemplate= templeteManager.getCtpTemplate(templete.getFormParentid());
                 if(pTemplate.getWorkflowId()!=null){
                     templeteProcessId= pTemplate.getWorkflowId();
                 }
        	}else if(templete.getWorkflowId() != null){
                templeteProcessId= templete.getWorkflowId();
            }
            workflowNodesInfo = wapi.getWorkflowNodesInfo(String.valueOf(templeteProcessId), "edoc", flowPermPolicyMetadata);
            String processXML = wapi.selectWrokFlowTemplateXml(String.valueOf(templeteProcessId));
            if(templeteProcessId !=0L &&  templeteProcessId != -1L){
            	modelAndView.addObject("templeteProcessId",templeteProcessId );
            }
            modelAndView.addObject("processXML",processXML);
        }else if(Strings.isNotBlank(processId)){
        	workflowNodesInfo = wapi.getWorkflowNodesInfo(processId, "edoc", flowPermPolicyMetadata);
            modelAndView.addObject("processId",processId);
        }else if(Strings.isBlank(s_summaryId) && templete != null){//个人模版情况
            
            if(templete.getWorkflowId() != null){
                templeteProcessId= templete.getWorkflowId();
            }
            if("0".equals(String.valueOf(templeteProcessId))) {
            	templeteProcessId = -1L;
            }
            workflowNodesInfo = wapi.getWorkflowNodesInfo(String.valueOf(templeteProcessId), "edoc", flowPermPolicyMetadata);
        }
        modelAndView.addObject("workflowNodesInfo",workflowNodesInfo);
		return processId;
	}

	private void getEdocCategory(ModelAndView modelAndView, List<EdocForm> edocForms,User user) {
		edocCategoryManager = (EdocCategoryManager)AppContext.getBean("edocCategoryManager");
        
        Long categoryId = defaultEdocForm.getSubType();
        List<Map<String,String>> categroy2Form = new ArrayList<Map<String,String>>();
        List<Long> validCategoryIds = new ArrayList<Long>();
        
        Long loginAccountId = 0l;
        if(user!=null){
        	loginAccountId = user.getLoginAccount();
        }
        for(Iterator<EdocForm> ef = edocForms.iterator();ef.hasNext();){
        	EdocForm edocForm = ef.next();
        	
       
        	String eid  = String.valueOf(edocForm.getId());
        	String ename = edocForm.getName();
        	String isdefault = String.valueOf(edocForm.getIsDefault()== null ? false:edocForm.getIsDefault().booleanValue());
        	
        	Map<String, String> m = new HashMap<String,String>();
        	m.put("eid", eid);
        	m.put("ename", Strings.escapeJavascript(Strings.toHTML(ename)));
        	m.put("isdefault", isdefault);
        	if(loginAccountId.equals(edocForm.getDomainId())){
        		
        		m.put("subtype",String.valueOf(edocForm.getSubType()));
        	}else{
        		m.put("subtype","");
        	}
        	categroy2Form.add(m);
        	
        	if(edocForm.getSubType() != null && !validCategoryIds.contains(edocForm.getSubType())){
        		validCategoryIds.add(edocForm.getSubType());
        	}
        	
        	if(categoryId!=null && categoryId != 0 && !categoryId.equals(edocForm.getSubType())){
        		ef.remove();
        	}
        }
        
        List<EdocCategory> categorys = edocCategoryManager.findCategory(validCategoryIds);
        
        if(Strings.isNotEmpty(categorys)){//加空防护
            for(Iterator<EdocCategory> it = categorys.iterator();it.hasNext();){
                EdocCategory  ec  = it.next();
                if(!loginAccountId.equals(ec.getAccountId())){
                    it.remove();
                }
            }
        }
        
        modelAndView.addObject("categorys",categorys);
        modelAndView.addObject("categroy2Forms",JSONUtil.toJSONString(categroy2Form));
        modelAndView.addObject("categoryId",categoryId);
	}
	protected void rendJavaScript(HttpServletResponse response, String jsContent)
			throws IOException {
		    response.setContentType("text/html;charset=UTF-8");
			PrintWriter out = response.getWriter();
			out.println("<script type=\"text/javascript\">");
			out.println(jsContent);
			out.println("</script>");
			out.close();
		}
}
