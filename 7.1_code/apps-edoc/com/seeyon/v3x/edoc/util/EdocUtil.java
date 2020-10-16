package com.seeyon.v3x.edoc.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.bo.DocResourceBO;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.enums.AffairExtPropEnums;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.IdentifierUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.engine.enums.BPMSeeyonPolicySetting;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.domain.EdocFormExtendInfo;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.menu.manager.MenuFunction;

public class EdocUtil {
	
	private static final Log LOGGER = LogFactory.getLog(EdocUtil.class);
	
    private static OrgManager orgManager = null;
    private static DocApi             docApi;
    
    private static OrgManager getOrgManager(){
        if(orgManager == null){
            orgManager = (OrgManager) AppContext.getBean("orgManager");
        }
        return orgManager;
    }
    
    private static DocApi getDocApi() {
        if (docApi == null) {
            docApi = (DocApi) AppContext.getBean("docApi");
        }
        return docApi;
    }

	public static ApplicationCategoryEnum getAppCategoryByEdocType(int edocType) {		
		if(edocType==EdocEnum.edocType.sendEdoc.ordinal()) {
			return ApplicationCategoryEnum.edocSend;
		} else if(edocType==EdocEnum.edocType.recEdoc.ordinal()) {
			return ApplicationCategoryEnum.edocRec;
		} else if(edocType==EdocEnum.edocType.signReport.ordinal()) {
			return ApplicationCategoryEnum.edocSign;
		} 
		return ApplicationCategoryEnum.edoc;
	}
	public static ApplicationSubCategoryEnum getSubAppCategoryByEdocType(int edocType) {		
		if(edocType==EdocEnum.edocType.sendEdoc.ordinal()) {
			return ApplicationSubCategoryEnum.old_edocSend;
		} else if(edocType==EdocEnum.edocType.recEdoc.ordinal()) {
			return ApplicationSubCategoryEnum.old_edocRec;
		} else if(edocType==EdocEnum.edocType.signReport.ordinal()) {
			return ApplicationSubCategoryEnum.old_edocSign;
		} 
		return null;
	}
	public static int getApplicationCategoryEnumKeyByEdocType(int edocType)
	{		
		if(edocType==EdocEnum.edocType.sendEdoc.ordinal())
		{
			return ApplicationCategoryEnum.edocSend.getKey();
		}
		else if(edocType==EdocEnum.edocType.recEdoc.ordinal())
		{
			return ApplicationCategoryEnum.edocRec.getKey();
		}
		else if(edocType==EdocEnum.edocType.signReport.ordinal())
		{
			return ApplicationCategoryEnum.edocSign.getKey();
		} 
		return ApplicationCategoryEnum.edoc.getKey();
	}
	public static String getEdocTypeName(int edocType)
	{
		String keys="menu.edoc.sendManager";
		if(edocType==EdocEnum.edocType.recEdoc.ordinal())
		{
			keys="menu.edoc.recManager";
		}
		else if(edocType==EdocEnum.edocType.signReport.ordinal())
		{
			keys="menu.edoc.signManager";
		} 
		return ResourceBundleUtil.getString("com.seeyon.v3x.main.resources.i18n.MainResources",keys);		
	}
	public static String getEdocStateName(int state)
	{
		String keys="edoc.workitem.state.done";
		if(state==StateEnum.col_pending.getKey())
		{
			keys="edoc.workitem.state.pending";
		}
		else if(state==StateEnum.col_sent.getKey())
		{
			keys="edoc.workitem.state.sended";
		} 
		else if(state==StateEnum.col_waitSend.getKey())
		{
			keys="edoc.workitem.state.darft";
		}
		return ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",keys);		
	}
	public static String getEdocLocationName(int edocType,int state)
	{
		return getEdocTypeName(edocType)+" - "+getEdocStateName(state);
	}
	
	public static int getEdocTypeByAppCategory(int appEnum)
	{
		if(appEnum==ApplicationCategoryEnum.edocSend.getKey())
		{
			return EdocEnum.edocType.sendEdoc.ordinal();
		}
		else if(appEnum==ApplicationCategoryEnum.edocRec.getKey())
		{
			return EdocEnum.edocType.recEdoc.ordinal();
		}
		else
		{
			return EdocEnum.edocType.signReport.ordinal();
		}
	}
	
	public static ApplicationCategoryEnum valueOfApplicationCategoryEnum(int affairApp) {
        ApplicationCategoryEnum app = null;
        if (affairApp == ApplicationCategoryEnum.edocSend.ordinal()) {
            app = ApplicationCategoryEnum.edocSend;
        } else if (affairApp == ApplicationCategoryEnum.edocRec.ordinal()) {
            app = ApplicationCategoryEnum.edocRec;
        } else if (affairApp == ApplicationCategoryEnum.edocSign.ordinal()) {
            app = ApplicationCategoryEnum.edocSign;
        } else {
            app = ApplicationCategoryEnum.edoc;
        }
        return app;
    }
	
	public static String getEdocCategroryPendingUrl(int appEnum)
	{
		String url="";
		if(MenuFunction.hasMenu(getMenuIdByApp(appEnum)))
		{
			int edocType=getEdocTypeByAppCategory(appEnum);		
			url="/edocController.do?method=edocFrame&from=listPending&controller=edocController.do&edocType="+edocType;
		}
		else
		{//没有收发文菜单
			url="";
		}
		return url;
	}
	public static String getMenuIdByApp(int appEnum)
	{
		String menuResource = "";
		if(appEnum==ApplicationCategoryEnum.edocSend.getKey())
		{
		    menuResource="F07_sendManager";
		}
		else if(appEnum==ApplicationCategoryEnum.edocRec.getKey() || appEnum==ApplicationCategoryEnum.edocRegister.getKey())
		{
		    menuResource="F07_recManager";
		}
		else if(appEnum==ApplicationCategoryEnum.edocSign.getKey())
		{
		    menuResource="F07_signReport";
		}
		else if(appEnum==ApplicationCategoryEnum.exSend.getKey() || appEnum==ApplicationCategoryEnum.exSign.getKey())
		{//公文交换菜单
		    menuResource="F07_edocExchange";
		}
		return menuResource;
	}
	public static String getEdocCategroryUrl(String from,int appEnum)
	{
		String url="";
		int edocType=getEdocTypeByAppCategory(appEnum);
		url="/edocController.do?method=edocFrame&from="+from+"&controller=edocController.do&edocType="+edocType;
		return url;
	}
	
	/*
	public static String getEdocTypeLocalLanguage(int appType)
	{
		return ResourceBundleUtil.getString("com.seeyon.v3x.common.resources.i18n.SeeyonCommonResources", "application."+appType+".label");	
	}
	
	public static String getEdocTypeLocalLanguageByEdocType(int edocType)	
	{
		int appType=getAppCategoryByEdocType(edocType).getKey();
		return getEdocTypeLocalLanguage(appType);	
	}
	*/
	
	public static EnumNameEnum getEdocMetadataNameEnum(int edocType)
	{
		if(edocType==EdocEnum.edocType.sendEdoc.ordinal())
		{
			return EnumNameEnum.edoc_send_permission_policy;
		}
		else if(edocType==EdocEnum.edocType.recEdoc.ordinal())
		{
			return EnumNameEnum.edoc_rec_permission_policy;
		}
		else
		{
			return EnumNameEnum.edoc_qianbao_permission_policy;
		}
	}
	
	public static EnumNameEnum getEdocMetadataNameEnumByApp(int appType)
	{
		int edocType=getEdocTypeByAppCategory(appType);
		return getEdocMetadataNameEnum(edocType);
	}
	
    public static String getSendFlowpermNameByEdocType(int edocType) {
        
        boolean flag = edocType == EdocEnum.edocType.sendEdoc.ordinal();
        if(!flag){
            flag = edocType != EdocEnum.edocType.recEdoc.ordinal();
        }
        
        if (flag) {
            return "niwen";
        } else{
            return "dengji";
        }
    }
	
	/**
	 * 获得前端传入的公文文号信息
	 * @param s [id|文号|当前号|标志位]
	 * @return
	 */
    public static String[] parseDocMark(String s) {
        try {
            return s.split("\\|");
        } catch (Exception e) {
            LOGGER.error("解析公文文号时出现错误。" + e.toString());
            return null;
        }
    }
	
	public static String getOfficeFileExt(String ftypt)
	{
		String fe=".htm";
		if(com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_OFFICE_EXCEL.equals(ftypt))
		{
			fe=".xls";
		}
		else if(com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_OFFICE_WORD.equals(ftypt))
		{
			fe=".doc";
		}
		else if(com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_WPS_EXCEL.equals(ftypt))
		{
			fe=".et";
		}
		else if(com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_WPS_WORD.equals(ftypt))
		{
			fe=".wps";
		}
		return fe;
	}	
	
	
	public static ModuleType getModuleTypeInSystem(int categoryType){
        ModuleType moduleType = null;
        if(categoryType == ModuleType.edocSend.ordinal()){
            moduleType = ModuleType.edocSend;
        }else if(categoryType == ModuleType.edocRec.ordinal()){
            moduleType = ModuleType.edocRec;
        }else if(categoryType == ModuleType.edocSign.ordinal()){
            moduleType = ModuleType.edocSign;
        }
        return moduleType;
    }
	
	public static int getEdocTypeByTemplateType(int templateType)
	{
		if(templateType==ModuleType.edocRec.ordinal())
		{
			return EdocEnum.edocType.recEdoc.ordinal();
		}
		else if(templateType==ModuleType.edocSend.ordinal())
		{
			return EdocEnum.edocType.sendEdoc.ordinal();
		}
		else if(templateType==ModuleType.edocSign.ordinal())
		{
			return EdocEnum.edocType.signReport.ordinal();
		}		
		return EdocEnum.edocType.recEdoc.ordinal();
	}
	/**
	 * edoc(4), // 公文
	 * edocSend(19), //发文
	 * edocRec(20),	//收文
	 * edocSign(21),	//签报	
	 * exSend(22), //待发送公文
	 * exSign(23), //待签收公文
	 * edocRegister(24), //待登记公文
	 * exchange(16), // 交换
	 * @param key
	 * @return
	 */
	public static boolean isEdocCheckByAppKey(int key){
		if(ApplicationCategoryEnum.edoc.getKey() == key
				||ApplicationCategoryEnum.edocRec.getKey() ==key
				||ApplicationCategoryEnum.edocRegister.getKey() == key
				||ApplicationCategoryEnum.edocSend.getKey() == key
				||ApplicationCategoryEnum.edocSign.getKey()==key
				||ApplicationCategoryEnum.exSend.getKey() == key
				||ApplicationCategoryEnum.exSign.getKey() == key
				||ApplicationCategoryEnum.exchange.getKey() == key
				||ApplicationCategoryEnum.govdocRec.getKey()==key
				||ApplicationCategoryEnum.govdocSend.getKey()==key
				||ApplicationCategoryEnum.govdocSign.getKey()==key){
			return true;
		
		}else{
			return false;
		}
	}
	public static List<ApplicationCategoryEnum>  getAllEdocApplicationCategoryEnum(){
		List<ApplicationCategoryEnum> apps = new ArrayList<ApplicationCategoryEnum>();
		apps.add(ApplicationCategoryEnum.edoc);
		apps.add(ApplicationCategoryEnum.edocRec);
		apps.add(ApplicationCategoryEnum.edocRegister);
		apps.add(ApplicationCategoryEnum.edocSend);
		apps.add(ApplicationCategoryEnum.edocSign);
		apps.add(ApplicationCategoryEnum.exSend);
		apps.add(ApplicationCategoryEnum.exSign);
		apps.add(ApplicationCategoryEnum.exchange);
		return apps;
	}
	public static List<Integer>  getAllEdocApplicationCategoryEnumKey(){
		List<Integer> keys = new ArrayList<Integer>();
		keys.add(ApplicationCategoryEnum.edoc.key());
		keys.add(ApplicationCategoryEnum.edocRec.key());
		keys.add(ApplicationCategoryEnum.edocRegister.key());
		keys.add(ApplicationCategoryEnum.edocSend.key());
		keys.add(ApplicationCategoryEnum.edocSign.key());
		keys.add(ApplicationCategoryEnum.exSend.key());
		keys.add(ApplicationCategoryEnum.exSign.key());
		keys.add(ApplicationCategoryEnum.exchange.key());
		return keys;
	}
	
	/**
	 * 将公文单授权部分的属性设置到公文单中
	 * @param list
	 * @param domainId
	 * @return
	 */
	public static List<EdocForm> convertExtendInfo2EdocForm(List<EdocForm> list,Long domainId){
		if(list == null ) return null;
		if(domainId == null) return list;
		
		for(EdocForm ef : list){
			Set<EdocFormExtendInfo> set = ef.getEdocFormExtendInfo();
			boolean hasInfo = false;
			if(set !=null){
				for(EdocFormExtendInfo info :set){
					if(info.getAccountId().longValue() == domainId){
						ef.setStatus(info.getStatus());
						ef.setIsDefault(info.getIsDefault());
						ef.setStatusId(String.valueOf(info.getId()));
						if(ef.getDomainId().equals(info.getAccountId())){
							ef.setIsOuterAcl(false);
						}else{
							ef.setIsOuterAcl(true);
						}
						hasInfo = true;
						break;
					}
				}
			}
			
			if(!hasInfo){
				ef.setStatus(EdocForm.C_iStatus_Draft); //停用
				ef.setIsDefault(false);	 //非默认公文单 
				ef.setStatusId("");
				ef.setIsOuterAcl(true);
			}
		}
		return list;
	}
	
	/**
	 * 首页栏目配置公文字段的数据保存，存入CTP_AFFAIR的extProperties字段。
	 * 当前保存的是：公文字段、发文单位
	 * @param edocSummary
	 * @return Map<String, Object> 
	 */
	public static Map<String, Object> createExtParam(EdocSummary edocSummary){
		if(edocSummary==null){
			return null;
		}
		
		Map<String, Object> extParam = new HashMap<String, Object>();
		String docMark="";
		String sendUnit="";
		
		if(edocSummary.getDocMark()!=null){
			docMark=edocSummary.getDocMark();
		}
		if(edocSummary.getSendUnit()!=null){
			sendUnit= edocSummary.getSendUnit();
		}
		
        extParam.put(AffairExtPropEnums.edoc_edocMark.name(), docMark); //公文文号
        extParam.put(AffairExtPropEnums.edoc_sendUnit.name(), sendUnit);//发文单位
        //OA-43885 首页待办栏目下，待开会议的主持人名字改变后，仍显示之前的名称
        extParam.put(AffairExtPropEnums.edoc_sendAccountId.name(), edocSummary.getSendUnitId());//发文单位ID 
        if(null != edocSummary.getDeadline() && edocSummary.getDeadline().longValue() > 0){
            extParam.put(AffairExtPropEnums.processPeriod.name(), edocSummary.getDeadline());//流程期限
        }
        
		return extParam;
	}
	

	/**
	 * 首页栏目配置公文字段的数据保存，存入CTP_AFFAIR的extProperties字段。
	 * 当前保存的是：公文字段、发文单位
	 * @param edocSummary
	 * @return Map<String, Object> 
	 */
	public static Map<String, Object> createExtParam(String docMark, String sendUnit,String sendUnitId){
		Map<String, Object> extParam = new HashMap<String, Object>();
		
        if(docMark==null){
        	docMark="";
        }
        if(sendUnit==null){
        	sendUnit="";
        }
		
        extParam.put(AffairExtPropEnums.edoc_edocMark.name(), docMark); //公文文号
        extParam.put(AffairExtPropEnums.edoc_sendUnit.name(), sendUnit);//发文单位
        extParam.put(AffairExtPropEnums.edoc_sendAccountId.name(), sendUnitId);//发文单位ID
        
		return extParam;
	}
	
	
	
	public static String showSubjectOfEdocSummary(EdocSummary summary, Boolean isProxy, int length, String proxyName,Boolean isAgentDeal){
		if(summary == null)
			return null;
		return showSubject(summary.getSubject(),"",0,isProxy,length,proxyName,isAgentDeal);
	}
	
    private static String showSubject(String subject, String forwardMember,Integer resendTiem, Boolean isProxy, int length, String proxyName,Boolean isAgentDeal){
        if(Strings.isEmpty(proxyName))
            return mergeSubjectWithForwardMembers(subject, length, forwardMember, resendTiem, null);
        String colProxyLabel = "";
        if(Boolean.TRUE.equals(isProxy)){
            List<AgentModel> _agentModelList = MemberAgentBean.getInstance().getAgentModelList(AppContext.getCurrentUser().getId());
            List<AgentModel> _agentModelToList = MemberAgentBean.getInstance().getAgentModelToList(AppContext.getCurrentUser().getId());
            boolean agentToFlag = false;
            if(_agentModelList != null && !_agentModelList.isEmpty()){
                agentToFlag = false;
            }else if(_agentModelToList != null && !_agentModelToList.isEmpty()){
                agentToFlag = true;
            }
            if(agentToFlag){
                colProxyLabel = "(" + proxyName + ResourceUtil.getString("collaboration.proxy") + ")";
            }else{
                if(isAgentDeal){      //被代理人自己处理
                    colProxyLabel = "(" + ResourceUtil.getString("col.proxy.deal", proxyName) + ")";
                }else{
                    colProxyLabel = "(" + ResourceUtil.getString("collaboration.proxy") + proxyName + ")";
                }
            }
            length -= colProxyLabel.getBytes().length;
        }       
        return mergeSubjectWithForwardMembers(subject, length, forwardMember, resendTiem, null) + colProxyLabel;
    }
    
    /**
     * 分解转发人、转发次数，用于任务项分配往Affair表写
     *
     * @param subject
     * @param subjectLength
     * @param forwardMember
     * @param resentTime
     * @param orgManager
     * @param locale
     * @return
     */
    public static  String mergeSubjectWithForwardMembers(String subject, int subjectLength, String forwardMember,
            Integer resentTime, Locale locale) {
        if(locale == null){
            User user = AppContext.getCurrentUser();
            if (user != null) {
                locale = user.getLocale();
            }
        }
        StringBuffer sb = new StringBuffer();
        if(resentTime != null && resentTime > 0){
            sb.append(ResourceUtil.getString("collaboration.new.repeat.label", resentTime));
        }
       
		if (subjectLength == -1 || subjectLength == 0) {
			sb.append(subject);
		}
		else if (subject != null) {
			sb.append(Strings.getSafeLimitLengthString(subject, subjectLength, "..."));
		}
        if(StringUtils.isNotBlank(forwardMember)) {
            String[] forwardMembers = forwardMember.split(",");
            for (String m : forwardMembers) {
                long memberId = Long.parseLong(m);
                try {
                    V3xOrgMember member =  getOrgManager().getEntityById(V3xOrgMember.class, memberId);
                    sb.append(ResourceUtil.getString("collaboration.forward.subject.suffix", member.getName()));
                }
                catch (Exception e) {
                    LOGGER.error("", e);
                }
            }
        }
        return sb.toString();
    }
    
    
    /**
     * 用于协同已办的显示
     * @param summary
     * @param isProxy
     * @param length
     * @param proxyName
     * @param isAgentDeal
     * @return
     */
    public static String showSubjectOfSummary4Done(CtpAffair affair, int length){
        if(affair == null)
            return null;
        V3xOrgMember member = null;
        String subject = "";
        String colProxyLabel = "";
        boolean isAgent = true;
        User user = AppContext.getCurrentUser();
        if(user == null)return affair.getSubject();
        long userId = user.getId();
        if(affair.getTransactorId() != null){ //已办事项是代理人处理的
            Long memberId = affair.getTransactorId();
            if(memberId.longValue()==userId){
                memberId = affair.getMemberId();
            }else{
                isAgent = false;
            }
            try {
                member = getOrgManager().getMemberById(memberId);
                if(member != null){
                    if(isAgent) //代理登陆
                        colProxyLabel = "(" + ResourceUtil.getString("collaboration.proxy") + member.getName() + ")";//代理***
                    else  //被代理人登陆
                        colProxyLabel = "(" + member.getName() + ResourceUtil.getString("collaboration.proxy") + ")"; //***处理
                    length -= colProxyLabel.getBytes().length;
                    subject = mergeSubjectWithForwardMembers(affair.getSubject(), length, affair.getForwardMember(), affair.getResentTime(), null) + colProxyLabel;
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }else if(affair.getMemberId().longValue() != userId){ //代理人登陆查看已办事项不是自己的，自己帮忙办的
            try{
                member = getOrgManager().getMemberById(affair.getMemberId());
                //(XX)处理
                colProxyLabel = "(" + ResourceUtil.getString("collaboration.proxy.deal", member.getName())+  ")"; //代理***
                length -= colProxyLabel.getBytes().length;
                subject = mergeSubjectWithForwardMembers(affair.getSubject(), length, affair.getForwardMember(), affair.getResentTime(), null) + colProxyLabel;
            }catch(Exception e){
                LOGGER.error("", e);
            }
        }else{
            subject = mergeSubjectWithForwardMembers(affair.getSubject(), length, affair.getForwardMember(), affair.getResentTime(), null);
        }
        return subject;
    }
    
    /**
     * 标志位, 共100位，采用枚举的自然顺序
     */
    protected static final int INENTIFIER_SIZE = 20;
    protected static enum INENTIFIER_INDEX {
        HAS_ATTACHMENTS, // 是否有附件
        HAS_FORMTRIGGER, // 是否有配置表单触发
    };
    
    public static  boolean isHasAttachments(EdocSummary summary) {
        return IdentifierUtil.lookupInner(summary.getIdentifier(),
                INENTIFIER_INDEX.HAS_ATTACHMENTS.ordinal(), '1');
    }
    
    public static void setHasAttachments(EdocSummary summary,Boolean hasAttachments) {
        if(Strings.isBlank(summary.getIdentifier())){
            summary.setIdentifier(IdentifierUtil.newIdentifier(summary.getIdentifier(), INENTIFIER_SIZE,
                    '0'));
        }
        summary.setIdentifier(IdentifierUtil.update(summary.getIdentifier(),
               INENTIFIER_INDEX.HAS_ATTACHMENTS.ordinal(), hasAttachments ? '1' : '0'));
    }
    
	public static String removeFormContentStyle(String content) {
		int start = content.indexOf("<style");
		int end = content.lastIndexOf("</style>");
		if(start!=-1 && end!=-1) {
			end = end + 8;
			String style = content.substring(start, end);
			content = content.replace(style, "");
		}
		return content;
	}
	
	/**
	 * 分离文单自带样式
	 * @param content
	 * @return String数组   String[0]移除样式后的content，String[1]移除的样式
	 */
	public static String[] divFormContentStyle(String content){
		String[] conAndSty = new String[2];
		int start = content.indexOf("<style");
		int end = content.lastIndexOf("</style>");
		if(start!=-1 && end!=-1) {
			end = end + 8;
			String style = content.substring(start, end);
			conAndSty[1] = style;
			content = content.replace(style, "");
			conAndSty[0] = content;
		}else{
			conAndSty[0] = content;
			conAndSty[1] = "";
		}
		return conAndSty;
	}
	
    /**
     * 将集团管理员账号和单位管理员账号 转换成'集团管理员'或者'单位管理员'
     * @return
     */
    public static String getAccountName(){
        User user = AppContext.getCurrentUser();
        String memname = OrgHelper.showMemberNameOnly(Long.valueOf(user.getId()));
        return memname;
    }
    
    /**
     * 检测是否是空字符串, 不允许空格 <br>
     * EdocUtil.isBlank(null)        = true <br>
     * EdocUtil.isBlank("")          = true <br>
     * EdocUtil.isBlank(" ")         = true <br>
     * EdocUtil.isBlank("null")      = true <br>
     * EdocUtil.isBlank("undefined") = true <br>
     * EdocUtil.isBlank("bob")       = false <br>
     * EdocUtil.isBlank("  bob  ")   = false <br>
     * @param val
     * @return
     */
    public static boolean isBlank(String val){
        if("null".equals(val)){
            return true;
        }
        if("undefined".equals(val)){
            return true;
        }
        return Strings.isBlank(val);
    }
    
    /**
     * 更新节点权限引用状态
     * @param modulType 应用类型
     * @param processXml 工作流流程ID
     * @param processId 工作流流程ID
     * @param processTemplateId 工作流流程模版ID
     * @throws BusinessException
     */
    public static void updatePermissinRef(Integer modulType, String processXml, String processId,String processTemplateId,Long accountId) throws BusinessException {
        //更新节点权限引用状态
        ModuleType type = ModuleType.getEnumByKey(modulType);
        String configCategory = "";
        if (ModuleType.edocSend.name().equals(type.name())) {
            configCategory = EnumNameEnum.edoc_send_permission_policy.name();
        } else if (ModuleType.edocRec.name().equals(type.name())) {
            configCategory = EnumNameEnum.edoc_rec_permission_policy.name();
        } else if (ModuleType.edocSign.name().equals(type.name())) {
            configCategory = EnumNameEnum.edoc_qianbao_permission_policy.name();
        } 
        
        PermissionManager permissionManager = (PermissionManager) AppContext.getBean("permissionManager");
        WorkflowApiManager wapi             = (WorkflowApiManager) AppContext.getBean("wapi");
        
        List<String> list = wapi.getWorkflowUsedPolicyIds(type.name(), processXml, processId, processTemplateId);
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                permissionManager.updatePermissionRef(configCategory, list.get(i), accountId);
            }
        }
    }
    
    /**
     * 将EdocType转换成名称
     * 
     * @param edocType
     * @return
     *
     * @Author      : xuqw
     * @Date        : 2016年7月5日上午12:58:14
     *
     */
    public static String convertEdocType2Name(int edocType){
        String appName = EdocEnum.edocType.sendEdoc.name();//"sendEdoc";
        if(edocType == EdocEnum.edocType.recEdoc.ordinal()){
            appName = EdocEnum.edocType.recEdoc.name();//"recEdoc";
        }else if(edocType==EdocEnum.edocType.signReport.ordinal()){
            appName = EdocEnum.edocType.signReport.name();
        }
        return appName;
    }
	 /**
	  * 返回M3时间显示格式
	  * @param date
	  * @return
	  */
	public static String showDate(Date date){
		Date nDate = DateUtil.currentDate();   //当前时间
		Date dBefore = DateUtil.addDay(new Date(), -1);   //得到前一天的时间
		
		String before = DateUtil.format(dBefore, "yyyy-MM-dd");    //格式化前一天
		String now = DateUtil.format(nDate, "yyyy-MM-dd"); //格式化当前时间
		String cDate = DateUtil.format(date, "yyyy-MM-dd"); //格式化待比较的时间
		
		String returnDate = "";
		int nYear = DateUtil.getYear(); //得到当前年
		int cYear = DateUtil.getYear(date); //获取待比较的年
		if(nYear == cYear){
			if(cDate.equals(before)){
				returnDate = ResourceUtil.getString("edoc.date.yestoday") + DateUtil.format(date, "HH:mm");
			}else if(cDate.equals(now)){
				returnDate = ResourceUtil.getString("edoc.date.today") + DateUtil.format(date, "HH:mm");
			}else{
				returnDate = DateUtil.format(date, "MM-dd HH:mm");
			}
		}else{
			returnDate = DateUtil.format(date, "yyyy-MM-dd HH:mm");
		}
		return returnDate;
	}
	
	public static boolean canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType dealType,ColSummary summary){
        String mergeDealType = "";
        if(summary!=null){
            mergeDealType = summary.getMergeDealType();
        }
        if(Strings.isNotBlank(mergeDealType)){
            String mergeDealTypeValue= dealType.getValue();
            Map<String,String> mergeDealTypeMap  = (Map<String, String>) JSONUtil.parseJSONString(mergeDealType);
            if(null!=mergeDealTypeMap && !mergeDealTypeMap.isEmpty()){
                return mergeDealTypeValue.equals(mergeDealTypeMap.get(dealType.name()));
            }
        }
        return false;
    }
	
	/**
     * 根据文档ID获取归档全路径名称
     * @param archiveId
     * @return
     */
    public static String getArchiveAllNameById(Long archiveId) throws BusinessException {
        String archiveAllName = ResourceUtil.getString("collaboration.project.nothing.label");
        if (AppContext.hasPlugin(ApplicationCategoryEnum.doc.name()) && archiveId != null) {
            DocResourceBO docResourceBO = getDocApi().getDocResource(archiveId);
            if(docResourceBO == null){
                archiveAllName = "";
            }else{
                archiveAllName = getDocApi().getPhysicalPath(docResourceBO.getLogicalPath(), "\\", false, 0);
            }
        }
        
        return archiveAllName;
    }
    
    /**
     * 根据文档Id获取归档简称路径
     * @return
     * @throws BusinessException
     */
    public static String getArchiveNameById(Long archiveId) throws BusinessException{
        String archiveName = ResourceUtil.getString("collaboration.project.nothing.label");
        if (AppContext.hasPlugin(ApplicationCategoryEnum.doc.name()) && archiveId != null) {
            archiveName = getDocApi().getDocResourceName(archiveId);
            String archiveAllName = getArchiveAllNameById(archiveId);
            if (archiveName != null && !archiveName.equals(archiveAllName)) {
                archiveName = "...\\"+archiveName;
            }
        }
        return archiveName;
    }
    
    /**
     * 获取高级归档的归档路径
     */
    public static String getAdvancePigeonholeName(Long archiveId,String str,String flag){
        String dvancePigeonholeName = "";
        try {
        	if(Strings.isBlank(str)){
        		return dvancePigeonholeName;
        	}
            JSONObject jo = new JSONObject(str);
            String archiveFolder = jo.optString("archiveFieldName", "");
            String archiveFieldValue = jo.optString("archiveFieldValue", "");
            //如果这两属性都为空，说明模板没有设置高级归档
            if(Strings.isNotBlank(archiveFolder) || Strings.isNotBlank(archiveFieldValue)){
            	if(Strings.isNotBlank(archiveFolder)){
            		archiveFolder = "\\"+"{"+archiveFolder+"}";
            	}
            	//调用模板的时候显示的名字
            	if("template".equals(flag)){
            		String docPath = getArchiveAllNameById(archiveId);
            		if(Strings.isBlank(docPath)){
            			return "wendangisdeleted";
            		}
            		dvancePigeonholeName = docPath+archiveFolder;
            	}else{
            		String docPath = getDocApi().getDocResourceName(archiveId);
            		if(Strings.isBlank(docPath)){
            			dvancePigeonholeName = ResourceUtil.getString("collaboration.project.nothing.label");
            		}else{
            			dvancePigeonholeName = docPath+"\\"+archiveFieldValue;
            		}
            	}
            }
        } catch (Exception e) {
            LOGGER.error("",e);
        }
        return dvancePigeonholeName;
    }
    
    public static String getAdvancePigeonholeName(Long archiveId,String str){
        String dvancePigeonholeName = "";
        try {
            JSONObject jo = new JSONObject(str);
            String archiveFolder = jo.optString("archiveFieldName", "");
            if(Strings.isNotBlank(archiveFolder)){
                archiveFolder = "\\"+"{"+archiveFolder+"}";
            }
            //调用模板的时候显示的名字
              if(null == getDocApi().getDocResourceName(archiveId)){
                 return "wendangisdeleted";
            }
            dvancePigeonholeName = getArchiveAllNameById(archiveId) + archiveFolder;
        } catch (Exception e) {
            LOGGER.error("",e);
        }
        return dvancePigeonholeName;
    }
    
    /**
     * 获取高级归档的json格式
     */
    public static String getAdvancePigeonhole(String archiveField,String archiveFieldName,String archiveFieldValue,String archiveIsCreate,String archiveKeyword){
        String dvancePigeonholeName = "";
        try {
            JSONObject jo = new JSONObject();
            jo.put("archiveField", archiveField);
            jo.put("archiveFieldName", archiveFieldName);
            jo.put("archiveIsCreate", archiveIsCreate);
            jo.put("archiveFieldValue", archiveFieldValue);
            jo.put("archiveKeyword",archiveKeyword);
            dvancePigeonholeName = jo.toString();
        } catch (JSONException e) {
            LOGGER.error("",e);
        }
        return dvancePigeonholeName;
    }
}
