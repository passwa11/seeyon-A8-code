package com.seeyon.v3x.inquiry.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.inquiry.constants.InquiryConstants;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.excel.DataRecord;
import com.seeyon.ctp.common.excel.FileToExcelManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.manager.RoleManager;
import com.seeyon.ctp.portal.api.PortalApi;
import com.seeyon.ctp.portal.po.PortalSpaceFix;
import com.seeyon.ctp.portal.util.Constants.SpaceType;
import com.seeyon.ctp.util.CommonTools;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.inquiry.domain.InquirySurveybasic;
import com.seeyon.v3x.inquiry.domain.InquirySurveytype;
import com.seeyon.v3x.inquiry.domain.InquirySurveytypeextend;
import com.seeyon.v3x.inquiry.manager.InquiryManager;
import com.seeyon.v3x.inquiry.vo.SurveyTypeCompose;

@CheckRoleAccess(roleTypes = { Role_NAME.GroupAdmin, Role_NAME.AccountAdministrator }, extendRoles = { "SpaceManager" })
public class InquiryController extends BaseController {

    private InquiryManager     inquiryManager;
    private OrgManager         orgManager;
    private AppLogManager      appLogManager;
    private PortalApi          portalApi;
    private RoleManager        roleManager;
    private FileToExcelManager fileToExcelManager;

    public void setInquiryManager(InquiryManager inquiryManager) {
        this.inquiryManager = inquiryManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setAppLogManager(AppLogManager appLogManager) {
        this.appLogManager = appLogManager;
    }

    public void setPortalApi(PortalApi portalApi) {
        this.portalApi = portalApi;
    }

    public void setRoleManager(RoleManager roleManager) {
        this.roleManager = roleManager;
    }

    public void setFileToExcelManager(FileToExcelManager fileToExcelManager) {
        this.fileToExcelManager = fileToExcelManager;
    }

    public ModelAndView inquiryFrame(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        return new ModelAndView("inquiry/categoryFrame");
    }

    public ModelAndView categoryList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        String spaceId = request.getParameter("spaceId");
        Integer types = null;
        if (StringUtils.isNotBlank(spaceId)) {
            PortalSpaceFix spaceFix = portalApi.getSpaceFix(Long.valueOf(spaceId));
            types = spaceFix.getType();
        }
        List<SurveyTypeCompose> typelist = new ArrayList<SurveyTypeCompose>();
        if (Strings.isNotBlank(spaceId)) {
            // 获取自定义单位或集团的调查类型列表
            typelist = inquiryManager.getCustomAccInquiryList(Long.parseLong(spaceId), types.toString());
        } else {
            typelist = inquiryManager.getInquiryList(user);// 获取调查类型列表
        }
        typelist = CommonTools.pagenate(typelist);
        if(Strings.isNotEmpty(typelist)){
            for(SurveyTypeCompose inquiryType:typelist){
                if(Strings.isNotEmpty(inquiryType.getManagers())){
                    String memberIds = "";
                    for(Long id : inquiryType.getManagers()){
                        memberIds += id + ",";
                    }
                    memberIds = memberIds.substring(0,memberIds.length()-1);
                    inquiryType.setManagerIds(memberIds);
                }
            }
        }
        ModelAndView mav = new ModelAndView("inquiry/categoryList");
        mav.addObject("group", user.isGroupAdmin());
        mav.addObject("typelist", typelist);
        return mav;
    }

    public ModelAndView categoryDetail(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        String param = httpServletRequest.getParameter("id");
        ModelAndView mav = new ModelAndView("inquiry/categoryDetail");
        mav.addObject("param", param);
        return mav;
    }

    public ModelAndView categoryAdd(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("inquiry/categoryAdd");
        User user = AppContext.getCurrentUser();
        boolean isGroup = user.isGroupAdmin();
        mav.addObject("isGroup", isGroup);
        String spaceId = request.getParameter("spaceId");
        Integer types = null;
        if (StringUtils.isNotBlank(spaceId)) {
            PortalSpaceFix spaceFix = portalApi.getSpaceFix(Long.valueOf(spaceId));
            types = spaceFix.getType();
        }
        if (Strings.isNotBlank(spaceId)) {
            List<Object[]> entityObj = portalApi.getSecuityOfSpace(Long.parseLong(spaceId));
            String entity = "";
            for (Object[] obj : entityObj) {
                entity += obj[0] + "|" + obj[1] + ",";
            }
            if (!"".equals(entity)) {
                entity = entity.substring(0, entity.length() - 1);
            }
            mav.addObject("spaceType", types);
            mav.addObject("entity", entity);
            mav.addObject("typeNameList", inquiryManager.getTypeNameList(Long.parseLong(spaceId), types));//已创建调查类型列表
        } else {
            mav.addObject("typeNameList", inquiryManager.getTypeNameList(isGroup, user.getLoginAccount()));//已创建调查类型列表
        }
        return mav;
    }

    public ModelAndView create_Type(HttpServletRequest request, HttpServletResponse response) throws Exception {
        InquirySurveytype surveytype = new InquirySurveytype();
        User user = AppContext.getCurrentUser();
        // 设置单位
        Long accountId = user.getLoginAccount();
        V3xOrgAccount account = orgManager.getAccountById(accountId);
        surveytype.setIdIfNew();// 设置ID号
        String type_name = request.getParameter("typename");// 调查类型名称
        surveytype.setTypeName(type_name);
        String survey_desc = request.getParameter("surveydesc");// 调查类型描述
        //		String space_type = request.getParameter("spaceType");//所属空间
        String spaceId = request.getParameter("spaceId");
        String spaceType = request.getParameter("spaceType");
        if (StringUtils.isNotBlank(spaceId)) {
            PortalSpaceFix spaceFix = portalApi.getSpaceFix(Long.valueOf(spaceId));
            spaceType = String.valueOf(spaceFix.getType());
        }
        if (account.isGroup()) {
            surveytype.setSpaceType(SpaceType.group.ordinal());
            surveytype.setAccountId(-1730833917365171641L);
        } else if (Strings.isNotBlank(spaceId)) {
            surveytype.setSpaceType(Integer.parseInt(spaceType));
            surveytype.setAccountId(Long.parseLong(spaceId));
        } else {
            surveytype.setSpaceType(SpaceType.corporation.ordinal());
            surveytype.setAccountId(accountId);
        }
        surveytype.setSurveyDesc(survey_desc);
        // 评审标记 1:需要评审 0：不需要评审
        String censor_desc = request.getParameter("censordesc");
        surveytype.setCensorDesc(Integer.parseInt(censor_desc));
        surveytype.setFlag(InquiryConstants.FLAG_NORMAL); // 设置为正常状态 1为删除状态
        // 是否允许匿名投票 0:允许 1：不允许
        String anonymousFlag = request.getParameter("anonymousFlag");
        surveytype.setAnonymousFlag(Integer.parseInt(anonymousFlag));

        String roleName4Admin = "";
        String roleName4Auditor = "";
        if (user.isGroupAdmin()) {
            roleName4Admin = OrgConstants.Role_NAME.GroupSurveyAdmin.name();
            roleName4Auditor = OrgConstants.Role_NAME.GroupSurveyAuditor.name();
        } else {
            roleName4Admin = OrgConstants.Role_NAME.UnitSurveyAdmin.name();
            roleName4Auditor = OrgConstants.Role_NAME.UnitSurveyAuditor.name();
        }

        String manager = request.getParameter("peopleId");// 管理员ID列表
        Set<InquirySurveytypeextend> managerSet = new HashSet<InquirySurveytypeextend>();
        if (Strings.isNotBlank(manager)) {
            String[] managerID = manager.split(",");
            for (int j = 0; j < managerID.length; j++) {
                InquirySurveytypeextend isextendmanager = new InquirySurveytypeextend();
                isextendmanager.setIdIfNew();
                isextendmanager.setManagerId(Long.parseLong(managerID[j]));
                isextendmanager.setSort(j);
                isextendmanager.setManagerDesc(InquirySurveytypeextend.MANAGER_SYSTEM);// 设置为管理员
                isextendmanager.setSurveytypeId(surveytype.getId());
                managerSet.add(isextendmanager);// 级联加入管理员子对象
            }
            //管理员角色处理
            roleManager.batchRole2Member(roleName4Admin, AppContext.currentAccountId(), manager);
        }

        String checker = request.getParameter("peopleIdSecond");
        if (!"".equals(checker) && checker != null) {
            InquirySurveytypeextend isextendchecker = new InquirySurveytypeextend();
            isextendchecker.setIdIfNew();
            isextendchecker.setSurveytypeId(surveytype.getId());
            isextendchecker.setManagerId(Long.parseLong(checker));
            isextendchecker.setManagerDesc(InquirySurveytypeextend.MANAGER_CHECK);// 设置为审核员
            managerSet.add(isextendchecker);
            roleManager.batchRole2Member(roleName4Auditor, AppContext.currentAccountId(), checker);
        }
        surveytype.setInquirySurveytypeextends(managerSet);
        surveytype.setAuthType(InquiryConstants.AUTHTYPE_ALL);
        inquiryManager.saveInquiryType(surveytype);// 保存调查类型

        //对管理员、审核员设定记录应用日志
        this.saveManagersChangeLog(surveytype, true);
        super.rendJavaScript(response, "parent.parent.location.href=parent.parent.location;");
        return null;
    }

    public ModelAndView categoryMDetail(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        ModelAndView mav = new ModelAndView("inquiry/categoryMDetail");
        mav.addObject("isDetail", httpServletRequest.getParameter("isDetail"));
        return mav;
    }

    public ModelAndView categoryModify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String type_id = request.getParameter("id");
        ModelAndView mav = new ModelAndView("inquiry/categoryModify");
        SurveyTypeCompose surveycompose = Strings.isNotBlank(type_id) ? inquiryManager.getSurveyTypeComposeBYID(Long.parseLong(type_id)) : null;
        if(Strings.isNotEmpty(surveycompose.getManagers())){
            String memberIds = "";
            for(Long id : surveycompose.getManagers()){
                memberIds += id + ",";
            }
            memberIds = memberIds.substring(0,memberIds.length()-1);
            surveycompose.setManagerIds(memberIds);
        }
        mav.addObject("surveytype", surveycompose);
        String update = request.getParameter("update");

        if (update != null && !"".equals(update)) {
            mav.addObject("update", "update");
        }

        User user = AppContext.getCurrentUser();
        boolean isGroup = user.isGroupAdmin();
        mav.addObject("isGroup", isGroup);
        //取得是否是详细页面标志
        mav.addObject("readOnly", "readOnly".equals(update));

        //判断是否可以修改审核员
        boolean hasNoCheck = false;
        boolean isAlert = false;
        InquirySurveytype type = surveycompose == null ? null : surveycompose.getInquirySurveytype();
        if (type != null && InquiryConstants.CENSOR_NO_PASS.equals(type.getCensorDesc())) {
            Long auditorId = -1L;
            Set<InquirySurveytypeextend> surveytypeextends = type.getInquirySurveytypeextends();
            if (CollectionUtils.isNotEmpty(surveytypeextends)) {
                for (InquirySurveytypeextend surveytypeextend : surveytypeextends) {
                    if (surveytypeextend.getManagerDesc().intValue() == InquirySurveytypeextend.MANAGER_CHECK.intValue()) {// 审核人员
                        auditorId = surveytypeextend.getManagerId();
                    }
                }
            }
            String spaceId = request.getParameter("spaceId");
            if (auditorId != -1 && Strings.isNotBlank(spaceId)) {
                isAlert = check(Long.valueOf(spaceId), auditorId);
            }
            if ("readOnly".equals(update)) {
                isAlert = false;
            }
            mav.addObject("isAlert", isAlert);
            V3xOrgMember checker = surveycompose.getChecker();
            if (checker != null && checker.isValid()) {
                hasNoCheck = inquiryManager.hasInquiryNoCheckByType(Long.parseLong(type_id));
            } else {
                mav.addObject("needTransfer2NewChecker", true).addObject("oldCheckerId", checker != null ? checker.getId() : -1l);
            }
        }
        mav.addObject("hasNoCheck", isAlert ? false : hasNoCheck);
        String spaceId = request.getParameter("spaceId");
        Integer types = null;
        if (StringUtils.isNotBlank(spaceId)) {
            PortalSpaceFix spaceFix = portalApi.getSpaceFix(Long.valueOf(spaceId));
            types = spaceFix.getType();
        }
        if (Strings.isNotBlank(spaceId)) {
            List<Object[]> entityObj = portalApi.getSecuityOfSpace(Long.parseLong(spaceId));
            String entity = "";
            for (Object[] obj : entityObj) {
                entity += obj[0] + "|" + obj[1] + ",";
            }
            if (!"".equals(entity)) {
                entity = entity.substring(0, entity.length() - 1);
            }
            mav.addObject("spaceType", types);
            mav.addObject("entity", entity);
            mav.addObject("typeNameList", inquiryManager.getTypeNameList(Long.parseLong(spaceId), types));//已创建调查类型列表
        } else {
            mav.addObject("typeNameList", inquiryManager.getTypeNameList(isGroup, user.getLoginAccount()));//已创建调查类型列表
        }
        return mav;
    }

    public ModelAndView update_Type(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String type_id = request.getParameter("id");
        InquirySurveytype surveytype = inquiryManager.getSurveyTypeById(Long.parseLong(type_id));
        String type_name = request.getParameter("typename");// 调查类型名称       
        String survey_desc = request.getParameter("surveydesc");// 调查类型描述       
        // 评审标记 1:需要评审 0：不需要评审
        String censor_desc = request.getParameter("censordesc");
        // 是否允许匿名投票 0:允许 1：不允许
        String anonymousFlag = request.getParameter("anonymousFlag");
        if (surveytype != null) {
            surveytype.setTypeName(type_name);
            surveytype.setSurveyDesc(survey_desc);
            surveytype.setCensorDesc(Integer.parseInt(censor_desc));
            surveytype.setFlag(0);// 设置为正常状态 1为删除状态
            surveytype.setAnonymousFlag(Integer.parseInt(anonymousFlag));
        }

        this.delRoleInfo(surveytype);

        User user = AppContext.getCurrentUser();
        String roleName4Admin = "";
        String roleName4Auditor = "";
        if (user.isGroupAdmin()) {
            roleName4Admin = OrgConstants.Role_NAME.GroupSurveyAdmin.name();
            roleName4Auditor = OrgConstants.Role_NAME.GroupSurveyAuditor.name();
        } else {
            roleName4Admin = OrgConstants.Role_NAME.UnitSurveyAdmin.name();
            roleName4Auditor = OrgConstants.Role_NAME.UnitSurveyAuditor.name();
        }
        // manager其格式为"99,100,101,..."
        String manager = request.getParameter("peopleId");// 管理员ID列表
        Set<InquirySurveytypeextend> managerSet = new HashSet<InquirySurveytypeextend>();
        if (manager != null) {
            String[] managerID = manager.split(",");
            for (int j = 0; j < managerID.length; j++) {
                InquirySurveytypeextend isextendmanager = new InquirySurveytypeextend();
                isextendmanager.setIdIfNew();
                isextendmanager.setManagerId(Long.parseLong(managerID[j]));
                isextendmanager.setManagerDesc(InquirySurveytypeextend.MANAGER_SYSTEM);// 设置为管理员
                isextendmanager.setSurveytypeId(surveytype.getId());
                isextendmanager.setSort(Integer.valueOf(j));
                managerSet.add(isextendmanager);// 级联加入管理员子对象
            }
            //管理员角色处理
            roleManager.batchRole2Member(roleName4Admin, AppContext.currentAccountId(), manager);
        }
        String checker = request.getParameter("peopleIdSecond");
        if (Strings.isNotBlank(censor_desc) && "0".equals(censor_desc) && Strings.isNotBlank(checker)) {
            InquirySurveytypeextend isextendchecker = new InquirySurveytypeextend();
            isextendchecker.setIdIfNew();
            isextendchecker.setSurveytypeId(surveytype.getId());
            isextendchecker.setManagerId(Long.parseLong(checker));
            isextendchecker.setManagerDesc(InquirySurveytypeextend.MANAGER_CHECK);// 设置为审核员
            managerSet.add(isextendchecker);
            if ("true".equals(request.getParameter("needTransfer2NewChecker"))) {
                this.inquiryManager.transfer2NewChecker(Long.parseLong(type_id), Long.parseLong(request.getParameter("oldCheckerId")), Long.parseLong(checker));
            }
            roleManager.batchRole2Member(roleName4Auditor, AppContext.currentAccountId(), checker);
        }
        if (Strings.isNotBlank(checker)) {
        }
        inquiryManager.updateInquiryType(surveytype, managerSet);// 保存调查类型

        //对管理员、审核员设定记录应用日志
        this.saveManagersChangeLog(surveytype, false);
        super.rendJavaScript(response, "parent.parent.location.href=parent.parent.location;");
        return null;
    }

    public ModelAndView removetype(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<Long> typeIds = CommonTools.parseStr2Ids(request.getParameter("id"));
        List<InquirySurveybasic> inquiryList = new ArrayList<InquirySurveybasic>();
        for (Long typeId : typeIds) {
            InquirySurveytype surveytype = inquiryManager.getSurveyTypeById(typeId);
            if (surveytype != null) {
                this.delRoleInfo(surveytype);

                surveytype.setFlag(1);// 设置调查类型为删除状态
                String name = AppContext.currentUserName();
                appLogManager.insertLog(AppContext.getCurrentUser(), AppLogAction.Inquiry_Type_Delete, name, surveytype.getTypeName());
                inquiryManager.updateInquiryType(surveytype);
            }
            inquiryList = inquiryManager.getInquirySurveyByTypeId(typeId);
            for (InquirySurveybasic inquiry : inquiryList) {
                inquiryManager.deleteInquiryBasic(inquiry.getId());
            }
        }
        super.rendJavaScript(response, "parent.location.href=parent.location;");
        return null;
    }

    private boolean check(Long spaceId, Long auditId) throws BusinessException {
        boolean flag = true;
        Set<Long> entityIds = new HashSet<Long>(); //取当前空间使用范围内的所有人员
        List<Object[]> entityObj = portalApi.getSecuityOfSpace(spaceId);
        if (CollectionUtils.isEmpty(entityObj)) {
            List<V3xOrgMember> members = orgManager.getAllMembers(spaceId);
            for (V3xOrgMember member : members) {
                entityIds.add(member.getId());
            }
        } else {
            String scopeStr = "";
            for (Object[] objects : entityObj) {
                scopeStr += objects[0] + "|" + objects[1] + ",";
            }
            if (Strings.isNotBlank(scopeStr)) {
                scopeStr = scopeStr.substring(0, scopeStr.length() - 1);
            }
            Set<V3xOrgMember> members = orgManager.getMembersByTypeAndIds(scopeStr);
            for (V3xOrgMember org : members) {
                entityIds.add(org.getId());
            }
        }
        if (entityIds.contains(auditId)) {
            flag = false;
        }
        return flag;
    }

    /**
     * 单位、集团讨论版块管理员和审核员设置与变更时保存日志
     */
    private void saveManagersChangeLog(InquirySurveytype type, boolean isNew) {
        User user = AppContext.getCurrentUser();
        String actionText = ResourceUtil.getString("bul.manageraction." + isNew);
        if (user.isGroupAdmin()) {
            this.appLogManager.insertLog(user, AppLogAction.Group_InquManagers_Update, user.getName(), type.getTypeName(), actionText);
        } else {
            String accountName = null;
            try {
                accountName = this.orgManager.getAccountById(user.getLoginAccount()).getName();
            } catch (Exception e) {

            }
            this.appLogManager.insertLog(user, AppLogAction.Account_InqManagers_Update, user.getName(), accountName, type.getTypeName(), actionText);
        }
    }

    /**
     * 取消管理员/审核员角色
     * 其它同类型（单位、集团）板块所有管理员中不包含此板块管理员的先删除增加
     */
    private void delRoleInfo(InquirySurveytype type) throws Exception {
        if (type == null) {
            return;
        }

        User user = AppContext.getCurrentUser();
        String roleName4Admin = "";
        String roleName4Auditor = "";
        List<InquirySurveytype> types = null;
        if (user.isGroupAdmin()) {
            roleName4Admin = OrgConstants.Role_NAME.GroupSurveyAdmin.name();
            roleName4Auditor = OrgConstants.Role_NAME.GroupSurveyAuditor.name();
            types = inquiryManager.getGroupSurveyTypeList();
        } else {
            roleName4Admin = OrgConstants.Role_NAME.UnitSurveyAdmin.name();
            roleName4Auditor = OrgConstants.Role_NAME.UnitSurveyAuditor.name();
            types = inquiryManager.getAccountSurveyTypeList(user.getLoginAccount());
        }

        Set<Long> otherAdmins = new HashSet<Long>();
        Set<Long> otherAuditors = new HashSet<Long>();
        if (Strings.isNotEmpty(types)) {
            for (InquirySurveytype inquirySurveytype : types) {
                if (!inquirySurveytype.equals(type)) {
                    Set<InquirySurveytypeextend> inquiryExtends = inquirySurveytype.getInquirySurveytypeextends();
                    if (Strings.isNotEmpty(inquiryExtends)) {
                        for (InquirySurveytypeextend inquiryExtend : inquiryExtends) {
                            if (InquirySurveytypeextend.MANAGER_SYSTEM.equals(inquiryExtend.getManagerDesc())) {
                                otherAdmins.add(inquiryExtend.getManagerId());
                            } else if (InquirySurveytypeextend.MANAGER_CHECK.equals(inquiryExtend.getManagerDesc())) {
                                otherAuditors.add(inquiryExtend.getManagerId());
                            }
                        }
                    }
                }
            }
        }

        String adminEntityIds = "";
        String auditorEntityIds = "";
        Set<InquirySurveytypeextend> oldInquiryExtends = type.getInquirySurveytypeextends();
        if (Strings.isNotEmpty(oldInquiryExtends)) {
            for (InquirySurveytypeextend inquiryExtend : oldInquiryExtends) {
                if (InquirySurveytypeextend.MANAGER_SYSTEM.equals(inquiryExtend.getManagerDesc())) {
                    if (!otherAdmins.contains(inquiryExtend.getManagerId())) {
                        adminEntityIds += OrgConstants.ORGENT_TYPE.Member.name() + "|" + inquiryExtend.getManagerId() + ",";
                    }
                } else if (InquirySurveytypeextend.MANAGER_CHECK.equals(inquiryExtend.getManagerDesc())) {
                    if (!otherAuditors.contains(inquiryExtend.getManagerId())) {
                        auditorEntityIds += OrgConstants.ORGENT_TYPE.Member.name() + "|" + inquiryExtend.getManagerId() + ",";
                    }
                }
            }
        }

        if (Strings.isNotBlank(adminEntityIds)) {
            roleManager.delRole2Entity(roleName4Admin, AppContext.currentAccountId(), adminEntityIds);
        }
        if (Strings.isNotBlank(auditorEntityIds)) {
            roleManager.delRole2Entity(roleName4Auditor, AppContext.currentAccountId(), auditorEntityIds);
        }
    }

    public ModelAndView orderSurveyType(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        V3xOrgAccount account = orgManager.getAccountById(user.getLoginAccount());
        ModelAndView mav = new ModelAndView("inquiry/orderSurveyType");
        List<InquirySurveytype> typelist = new ArrayList<InquirySurveytype>();
        String spaceId = request.getParameter("spaceId");
        String spaceType = request.getParameter("spaceType");
        if (SpaceType.public_custom.name().equals(spaceType)) {
            spaceType = String.valueOf(SpaceType.public_custom.ordinal());
        } else if (SpaceType.public_custom_group.name().equals(spaceType)) {
            spaceType = String.valueOf(SpaceType.public_custom_group.ordinal());
        }

        if (account != null && account.isGroup()) {
            typelist = inquiryManager.getGroupSurveyTypeList();
        } else if (Strings.isNotBlank(spaceId)) {
            typelist = inquiryManager.getCustomAccInquiryTypeList(Long.parseLong(spaceId), NumberUtils.toInt(spaceType, SpaceType.public_custom.ordinal()));
        } else {
            typelist = inquiryManager.getAccountSurveyTypeList(user.getLoginAccount());
        }
        mav.addObject("typelist", typelist);

        return mav;
    }

    public ModelAndView saveOrder(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String[] surveyTypeIds = request.getParameterValues("projects");
        inquiryManager.updateSurveyTypeOrder(surveyTypeIds);
        super.rendJavaScript(response, "parent.location.href=parent.location;");
        return null;
    }

    @CheckRoleAccess(roleTypes = { Role_NAME.NULL })
    public ModelAndView showDesignated(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("news/user/showDesignated");
        User user = AppContext.getCurrentUser();
        List<InquirySurveytype> typeList = null;
        String group = request.getParameter("group");
        String textfield = request.getParameter("textfield");

        if (Strings.isNotBlank(group)) {
            typeList = inquiryManager.getGroupSurveyTypeList();
        } else {
            typeList = inquiryManager.getAccountSurveyTypeList(user.getLoginAccount());
        }

        List<InquirySurveytype> resultList = new ArrayList<InquirySurveytype>();
        if (Strings.isNotBlank(textfield)) {
            for (InquirySurveytype type : typeList) {
                if (type.getTypeName().contains(textfield)) {
                    resultList.add(type);
                }
            }
        } else {
            resultList = typeList;
        }

        mav.addObject("typeList", resultList);
        return mav;
    }

    /**
    * 文化建设统计入口
    */
    @CheckRoleAccess(roleTypes = { Role_NAME.NULL })
    public ModelAndView publishInfoStc(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("stc/publishInfoStc");
        int year = DateUtil.getYear();
        Map<String, Object> jval = new HashMap<String, Object>();
        jval.put("year", year);
        jval.put("publishDateStart", year + "-01-01");
        jval.put("publishDateEnd", DateUtil.format(new Date()));
        //那个模块的统计
        String mode = request.getParameter("mode");
        String spceTypeId = request.getParameter("typeId");
        Map<String, Object> modeType = inquiryManager.getTypeByMode(mode, spceTypeId);
        jval.put("isGroupStc", !(Boolean) modeType.get("hideAcc"));
        jval.put("isStcDeptHide", modeType.get("hideDept"));
        jval.put("isStcAccHide", modeType.get("hideAcc"));

        jval.put("mode", mode);
        //参数spaceType,spaceId,typeId
        jval.put("spaceType", request.getParameter("spaceType"));
        jval.put("spaceId", spceTypeId);
        jval.put("typeId", request.getParameter("typeId"));
        mav.addObject("today", DateUtil.format(new Date()));
        mav.addObject("jval", Strings.escapeJson(JSONUtil.toJSONString(jval)));
        return mav;
    }

    /**
    * 统计导出
    */
    @SuppressWarnings("unchecked")
    @CheckRoleAccess(roleTypes = { Role_NAME.NULL })
    public ModelAndView stcExpToXls(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> param = ParamUtil.getJsonParams();
        DataRecord record = inquiryManager.expStcToXls(param);
        fileToExcelManager.save(response, record.getTitle(), record);
        return null;
    }

}