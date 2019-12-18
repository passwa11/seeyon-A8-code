package com.seeyon.apps.ext.modulePortlet.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.ai.api.AIApi;
import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.bo.DocResourceBO;
import com.seeyon.apps.edoc.api.EdocApi;
import com.seeyon.apps.edoc.bo.GovdocTemplateBO;
import com.seeyon.apps.edoc.enums.EdocEnum.TempleteType;
import com.seeyon.apps.info.api.InfoApi;
import com.seeyon.apps.info.po.GovFormBo;
import com.seeyon.apps.project.api.ProjectApi;
import com.seeyon.apps.project.bo.ProjectBO;
import com.seeyon.cap4.form.api.FormApi4Cap4;
import com.seeyon.cap4.form.bean.FormViewBean;
import com.seeyon.cdp.CDPAgent;
import com.seeyon.ctp.cap.api.bean.CAPFormBean;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.customize.manager.CustomizeManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.permission.manager.PermissionLayoutManager;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseTemplateRole;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.po.template.CtpTemplateAuth;
import com.seeyon.ctp.common.po.template.CtpTemplateCategory;
import com.seeyon.ctp.common.po.template.CtpTemplateHistory;
import com.seeyon.ctp.common.shareMap.V3xShareMap;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.template.enums.TemplateCategoryConstant;
import com.seeyon.ctp.common.template.enums.TemplateChooseScope;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.common.template.enums.TemplateTypeEnums;
import com.seeyon.ctp.common.template.manager.ProcessInsHandler;
import com.seeyon.ctp.common.template.manager.TemplateApproveManager;
import com.seeyon.ctp.common.template.manager.TemplateCategoryManager;
import com.seeyon.ctp.common.template.manager.TemplateInsManager;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.template.util.CtpTemplateUtil;
import com.seeyon.ctp.common.template.vo.TemplateBO;
import com.seeyon.ctp.common.template.vo.TemplateCategoryComparator;
import com.seeyon.ctp.common.template.vo.TemplateTreeVo;
import com.seeyon.ctp.common.template.vo.TemplateVO;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormAuthViewBean;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.po.CtpTemplateRelationAuth;
import com.seeyon.ctp.form.util.Enums.FormType;
import com.seeyon.ctp.form.util.FormConstant;
import com.seeyon.ctp.form.util.FormUtil;
import com.seeyon.ctp.form.util.SelectPersonOperation;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.portal.api.PortalApi;
import com.seeyon.ctp.util.BeanUtils;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.ReqUtil;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.bo.NodeSimpleBO;
import com.seeyon.ctp.workflow.engine.enums.BPMSeeyonPolicySetting;
import com.seeyon.ctp.workflow.messageRule.bo.MessageRuleVO;
import com.seeyon.ctp.workflow.messageRule.manager.MessageRuleManager;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.ctp.workflow.wapi.WorkflowFormDataMapManager;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

public class modulePortletController extends BaseController {


    public MessageRuleManager getMessageRuleManager() {
        return messageRuleManager;
    }

    public void setMessageRuleManager(MessageRuleManager messageRuleManager) {
        this.messageRuleManager = messageRuleManager;
    }

    private static Log LOG = CtpLogFactory.getLog(modulePortletController.class);

    private AffairManager affairManager;
    private WorkTimeManager workTimeManager;
    private TemplateInsManager templateInsManager;
    private static EnumManager enumManagerNew;
    private CollaborationApi collaborationApi;
    private EdocApi edocApi;
    private AIApi aiApi;
    private CustomizeManager customizeManager;
    private TemplateApproveManager templateApproveManager;
    private PermissionLayoutManager permissionLayoutManager;
    private FormApi4Cap4 formApi4Cap4;
    private FormApi4Cap3 formApi4Cap3;
    private MessageRuleManager messageRuleManager;

    private TemplateCategoryManager templateCategoryManager;
    private TemplateManager templateManager;

    private OrgManager orgManager;

    private PortalApi portalApi;

    private CAPFormManager capFormManager = null;

    private ProjectApi projectApi;

    private PermissionManager permissionManager;

    private AttachmentManager attachmentManager;

    private SuperviseManager superviseManager;

    private InfoApi infoApi;


    private Map<ApplicationCategoryEnum, ProcessInsHandler> processInsHandlerMap = new HashMap<ApplicationCategoryEnum, ProcessInsHandler>();


    public FormApi4Cap3 getFormApi4Cap3() {
        return formApi4Cap3;
    }

    public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }

    public void init() {
        Map<String, ProcessInsHandler> handlers = AppContext.getBeansOfType(ProcessInsHandler.class);
        for (String key : handlers.keySet()) {
            ProcessInsHandler handler = handlers.get(key);
            try {
                processInsHandlerMap.put(handler.getAppEnum(), handler);
            } catch (BusinessException e) {
                LOG.error("", e);
            }
        }
    }


    private Map<ApplicationCategoryEnum, ProcessInsHandler> getProcessInsHandlerMap() {
        if (processInsHandlerMap.isEmpty()) {
            init();
        }
        return processInsHandlerMap;
    }

    public void setTemplateApproveManager(TemplateApproveManager templateApproveManager) {
        this.templateApproveManager = templateApproveManager;
    }

    public void setCollaborationApi(CollaborationApi collaborationApi) {
        this.collaborationApi = collaborationApi;
    }

    public EdocApi getEdocApi() {
        return edocApi;
    }

    public void setEdocApi(EdocApi edocApi) {
        this.edocApi = edocApi;
    }

    private static EnumManager getEnumManager() {
        if (enumManagerNew == null) {
            enumManagerNew = (EnumManager) AppContext.getBean("enumManagerNew");
        }
        return enumManagerNew;
    }

    public static EnumManager getEnumManagerNew() {
        return enumManagerNew;
    }


    public static void setEnumManagerNew(EnumManager enumManagerNew) {
        modulePortletController.enumManagerNew = enumManagerNew;
    }


    public TemplateInsManager getTemplateInsManager() {
        return templateInsManager;
    }


    public void setTemplateInsManager(TemplateInsManager templateInsManager) {
        this.templateInsManager = templateInsManager;
    }


    public WorkTimeManager getWorkTimeManager() {
        return workTimeManager;
    }


    public void setWorkTimeManager(WorkTimeManager workTimeManager) {
        this.workTimeManager = workTimeManager;
    }


    public AffairManager getAffairManager() {
        return affairManager;
    }


    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }


    public void setWapi(WorkflowApiManager wapi) {
        this.wapi = wapi;
    }


    public SuperviseManager getSuperviseManager() {
        return superviseManager;
    }


    public void setSuperviseManager(SuperviseManager superviseManager) {
        this.superviseManager = superviseManager;
    }

    private DocApi docApi;

    public DocApi getDocApi() {
        return docApi;
    }


    public void setDocApi(DocApi docApi) {
        this.docApi = docApi;
    }

    public AttachmentManager getAttachmentManager() {
        return attachmentManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public ProjectApi getProjectApi() {
        return projectApi;
    }

    public void setProjectApi(ProjectApi projectApi) {
        this.projectApi = projectApi;
    }

    public void setCapFormManager(CAPFormManager capFormManager) {
        this.capFormManager = capFormManager;
    }


    public void setPortalApi(PortalApi portalApi) {
        this.portalApi = portalApi;
    }

    public OrgManager getOrgManager() {
        return orgManager;
    }

    public TemplateCategoryManager getTemplateCategoryManager() {
        return templateCategoryManager;
    }

    public void setTemplateCategoryManager(TemplateCategoryManager templateCategoryManager) {
        this.templateCategoryManager = templateCategoryManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public TemplateManager getTemplateManager() {
        return templateManager;
    }

    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    private WorkflowApiManager wapi;

    private boolean isEdoc(String categoryKey) {
        if (Strings.isNotBlank(categoryKey)) {
            //协同表单传入‘0,4’，转换出问题 直接判断。
            String[] len = categoryKey.split(",");
            if (len.length > 1) {
                return false;
            }
            Long ordinal = Long.valueOf(categoryKey);

            if (Long.valueOf(ModuleType.edoc.ordinal()).equals(ordinal)
                    || Long.valueOf(ModuleType.edocRec.ordinal()).equals(ordinal)
                    || Long.valueOf(ModuleType.edocSend.ordinal()).equals(ordinal)
                    || Long.valueOf(ModuleType.edocSign.ordinal()).equals(ordinal)
                    || Long.valueOf(TemplateCategoryConstant.edocRoot.key()).equals(ordinal)
                    || Long.valueOf(TemplateCategoryConstant.govdocRoot.key()).equals(ordinal)
                    || Long.valueOf(ModuleType.govdoc.getKey()).equals(ordinal)
                    || Long.valueOf(ModuleType.govdocExchange.getKey()).equals(ordinal)
                    || Long.valueOf(ModuleType.govdocRec.getKey()).equals(ordinal)
                    || Long.valueOf(ModuleType.govdocSend.getKey()).equals(ordinal)
                    || Long.valueOf(ModuleType.govdocSign.getKey()).equals(ordinal)) {
                return true;
            }
        }
        return false;
    }
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response)
            throws BusinessException {
        try {
            String viewType = customizeManager.getCustomizeValue(AppContext.currentUserId(), "template_view_type");
            if ("1".equals(viewType)) {
                return this.listRACITemplate(request, response);
            }
        } catch (Exception e) {
        }
        ModelAndView modelAndView = new ModelAndView("apps/ext/modulePortlet/index");
        User user = AppContext.getCurrentUser();
        Long orgAccountId = user.getLoginAccount();
        //保存我的模板个性化查看方式-树状结构
        customizeManager.saveOrUpdateCustomize(AppContext.currentUserId(), "template_view_type", "0");

        //***********************更多页面传递过来的参数处理 begin****************************************
        // 全部种类
        String category = request.getParameter("category");
        String fragmentId = ReqUtil.getString(request, "fragmentId");
        String ordinal = ReqUtil.getString(request, "ordinal");
        //如果有则不查询
        if (Strings.isBlank(category)) {
            category = String.valueOf(TemplateCategoryConstant.personRoot.key());
            if (AppContext.hasPlugin("collaboration")) {
                category += ",1,2,66";//CAP4表单
            }
            //G6 6.1sp1新公文不现实老公文模板
            if (!AppContext.hasPlugin("govdoc") && AppContext.hasPlugin("edoc")) {
                category += ",4,19,20,21";
            }
            if (AppContext.hasPlugin("govdoc")) {
                category += ",401,402,404";
            }
            if (AppContext.hasPlugin("infosend")) {
                category += ",32";
            }
            category = this.getMoreTemplateCategorys(category, fragmentId, ordinal);
        }
        //显示的最近使用模版数量
        Integer recent = ReqUtil.getInt(request, "recent", 10);
        //**********************更多页面传递过来的参数处理 end*********************************************


        //**************************我的模版更多页面传递过来的参数 begin*************************************
        //获取显示授权模版的单位
        String selectAccountId = request.getParameter("selectAccountId");
        //是否所有模版。
        String isShowTemplates = "true";
        //传递的单位selectAccountId=1为全部，则查询外单位授权过来的
        if (Strings.isNotBlank(selectAccountId) && "1".equals(selectAccountId)) {
            isShowTemplates = "true";
        } else if (Strings.isNotBlank(selectAccountId) && !"1".equals(selectAccountId)) {
            isShowTemplates = "false";
            orgAccountId = Long.parseLong(selectAccountId);
        }
        String searchValue = ReqUtil.getString(request, "searchValue");
        //**************************我的模版更多页面传递过来的参数 end******************************************
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("categoryIds", category);
        params.put("subject", searchValue);
        params.put("isToMoreTree", true);
        //合并权限
        templateManager.transMergeCtpTemplateConfig(user.getId());

        //根据首页模板栏目编辑页面条件，查询所有配置模板的集合
        List<CtpTemplate> allTempletes = templateManager.getMyConfigCollTemplate(null, params);
        List<TemplateVO> showTemplates = new ArrayList<TemplateVO>();

        Map<String, CtpTemplateCategory> nameCategory = new HashMap<String, CtpTemplateCategory>();
        //Map<Long, CtpTemplateCategory> idCategory =  this.templateManager.getAllShowCategorys(orgAccountId, category,nameCategory);
        Map<Long, CtpTemplateCategory> idCategory = new HashMap<Long, CtpTemplateCategory>();
        //获取所有子分类的模板分类,getTemplateTree中组装树的时候，需要用到
        List<CtpTemplateCategory> alls = getAllCategorys(category);
        for (CtpTemplateCategory c : alls) {
            if (orgAccountId.equals(c.getOrgAccountId())) {//登录单位的分类名称集合，用于外单位模板合并
                nameCategory.put(c.getName(), c);
            }
            if (c.getId() == ModuleType.govdocSend.getKey() || c.getId() == ModuleType.govdocRec.getKey() ||
                    c.getId() == ModuleType.govdocSign.getKey()) {
                c.setParentId(4L);
            }
            idCategory.put(c.getId(), c);
        }
        //模版所在的单位集合
        Map<Long, String> accounts = new HashMap<Long, String>();
        V3xOrgAccount logonOrgAccount = orgManager.getAccountById(orgAccountId);
        if (!accounts.containsKey(logonOrgAccount.getId())) {
            accounts.put(logonOrgAccount.getId(), logonOrgAccount.getName());
        }

        for (CtpTemplate template : allTempletes) {
            if (null != template.getFormParentid()) {
                CtpTemplate pTemplate = templateManager.getCtpTemplate(template.getFormParentid());
                if (null == pTemplate) {
                    continue;
                } else if (null != pTemplate) {
                    boolean templateEnabled = templateManager.isTemplateEnabled(pTemplate, AppContext.getCurrentUser().getId());
                    if (!templateEnabled || pTemplate.isDelete() || template.getState().equals(TemplateEnum.State.invalidation.ordinal())) {
                        continue;
                    }
                }
            }
            TemplateVO templateVO = new TemplateVO();
            templateVO.setSubject(template.getSubject());
            //外单位的模板才进行分类合并
            if (!template.getOrgAccountId().equals(orgAccountId)) {
                V3xOrgAccount outOrgAccount = orgManager.getAccountById(template.getOrgAccountId());
                if (!accounts.containsKey(outOrgAccount.getId())) {
                    accounts.put(outOrgAccount.getId(), outOrgAccount.getName());
                }
                if (!"true".equals(isShowTemplates)) {
                    continue;
                } else if (template.isSystem() && template.getCategoryId() != 0) {// 等于0是顶层的模板
                    CtpTemplateCategory tc = this.templateManager.getCtpTemplateCategory(template.getCategoryId());
                    if (tc != null) {
                        CtpTemplateCategory n = nameCategory.get(tc.getName());
                        if (n != null) {
                            template.setCategoryId(n.getId());//外单位的设置成自己单位的模板分类Id
                        } else {
                            //如果需要按照原有单位的单位层级显示的话，后面再考虑，先保持跟3.5 一致
                            if (tc.getParentId() == null) {
                                if (!isGovdoc(tc.getType().toString())) {//新公文分类类型,数据库中存在,因此不需要创建虚拟分类
                                    tc.setParentId(1l);
                                }
                            } else {
                                tc.setParentId(tc.getParentId());
                            }
                            if (!isGovdoc(tc.getType().toString())) {
                                idCategory.put(tc.getId(), tc);
                            }
                            nameCategory.put(tc.getName(), tc);
                        }
                    }
                }
            }
            if (!template.getOrgAccountId().equals(user.getLoginAccount())) {
                V3xOrgAccount outOrgAccount = orgManager.getAccountById(template.getOrgAccountId());
                templateVO.setSubject(template.getSubject() + "(" + outOrgAccount.getShortName() + ")");
            }

            templateVO.setId(template.getId());
            templateVO.setCategoryId(template.getCategoryId());
            templateVO.setMemberId(template.getMemberId());
            templateVO.setModuleType(template.getModuleType());
            templateVO.setSystem(template.isSystem());
            templateVO.setType(template.getType());
            templateVO.setBodyType(template.getBodyType());
            templateVO.setOrgAccountId(template.getOrgAccountId());
            templateVO.setFormAppId(template.getFormAppId());
            showTemplates.add(templateVO);
        }


        List<TemplateTreeVo> listTreeVo = new ArrayList<TemplateTreeVo>();
        //一、添加最近使用模版的分类，受栏目设置控制
        boolean showRecentTemplate = ReqUtil.getBoolean(request, "showRecentTemplate", true);
        if (!Strings.isBlank(fragmentId) && !Strings.isBlank(ordinal)) {
            Map<String, String> preference = portalApi.getPropertys(Long.parseLong(fragmentId),
                    ordinal);
            showRecentTemplate = preference.get("showRecentTemplate") != null ? Boolean.valueOf(preference.get("showRecentTemplate")) : true;
        }

        if (showRecentTemplate) {
            TemplateTreeVo recentTemplate = new TemplateTreeVo();
            recentTemplate.setId(-1l);
            recentTemplate.setName(ResourceUtil.getString("template.choose.category.recent.label"));//最近使用根目录
            recentTemplate.setpId(null);
            recentTemplate.setType("category");
            listTreeVo.add(recentTemplate);
        }

        List<CtpTemplate> templetes = new ArrayList<CtpTemplate>();
        //idCategory通过分类Id组装模板树，templetes通过模板组装模板树
        listTreeVo = getTemplateTree(listTreeVo, category, idCategory, templetes, null, null, null, null, true);
        //新建一个最终显示的分类树
        List<TemplateTreeVo> newTreeVoList = new ArrayList<TemplateTreeVo>();
        List<Long> newTreeVOId = new ArrayList<Long>();
        //分类id及对应的map
        Map<Long, TemplateTreeVo> categoryMap = new HashMap<Long, TemplateTreeVo>();
        //分类的树
        for (TemplateTreeVo vo : listTreeVo) {
            if ("category".equals(vo.getType()) || "personal".equals(vo.getType()) || "template_coll".equals(vo.getType())
                    || "text_coll".equals(vo.getType()) || "workflow_coll".equals(vo.getType()) || "edoc_coll".equals(vo.getType())) {
                categoryMap.put(vo.getId(), vo);
            }
            // 协同三个根节点："最近使用模板"、"公共模板"、"个人模板"无论下面是否有具体模板，都在前端展示。公文显示对应的公文
            if (vo.getId() == 0 || vo.getId() == 100 || vo.getId() == -1 || Long.valueOf(ModuleType.info.getKey()).equals(vo.getId()) || (Long.valueOf(ModuleType.edoc.getKey()).equals(vo.getId())
                    || Long.valueOf(ModuleType.edocSend.getKey()).equals(vo.getId()) || Long.valueOf(ModuleType.edocRec.getKey()).equals(vo.getId())
                    || Long.valueOf(ModuleType.edocSign.getKey()).equals(vo.getId()) || Long.valueOf(ModuleType.govdocSend.getKey()).equals(vo.getId())
                    || Long.valueOf(ModuleType.govdocRec.getKey()).equals(vo.getId()) || Long.valueOf(ModuleType.govdocSign.getKey()).equals(vo.getId())) && user.getExternalType() == OrgConstants.ExternalType.Inner.ordinal()
                    && !newTreeVOId.contains(vo.getId())) {
                newTreeVoList.add(vo);
                newTreeVOId.add(vo.getId());
            }
        }
        for (TemplateVO template : showTemplates) {
            // 如果包含此模板的分类
            if (template.getSystem() && categoryMap.containsKey(template.getCategoryId())) {
                TemplateTreeVo theCategory = categoryMap.get(template.getCategoryId());
                getParentCategory(theCategory, categoryMap, newTreeVoList);
            } else if (!template.getSystem()) {//个人模板
                TemplateTreeVo vv = null;
                if ("template".equals(template.getType()) && !isGovdoc(template.getModuleType().toString())) {//协同模板
                    vv = categoryMap.get(101L);
                } else if ("text".equals(template.getType())) {//格式模板
                    vv = categoryMap.get(102L);
                } else if ("workflow".equals(template.getType())) {//流程模板
                    vv = categoryMap.get(103L);
                } else if ("template".equals(template.getType())
                        && user.getExternalType() == OrgConstants.ExternalType.Inner.ordinal()
                        && isGovdoc(template.getModuleType().toString())) {//公文个人模板
                    vv = categoryMap.get(104L);
                } else if (null != template.getModuleType() && Integer.valueOf(32).equals(template.getModuleType())) {//信息报送
                    vv = categoryMap.get(105L);
                }
                if (null != vv && !newTreeVoList.contains(vv))
                    newTreeVoList.add(vv);
            }
        }
        String categoryName = null;
        //通过category的sort排序后的tree,listTreeVo是有顺序的
        List treeid = new ArrayList();
        for (Iterator<TemplateTreeVo> it = listTreeVo.iterator(); it.hasNext(); ) {
            TemplateTreeVo t = it.next();

            if (!newTreeVoList.contains(t) ||
                    treeid.contains(t.getId())) {
                it.remove();
            } else {
                // 判断是否是国际化名称
                treeid.add(t.getId());
                categoryName = ResourceUtil.getString(t.getName());
                if (Strings.isNotBlank(categoryName)) {
                    t.setName(categoryName);
                }
            }

            t.setCAP4(false);
        }
        request.setAttribute("fftree", listTreeVo);


        Map<Long, String> templeteIcon = new HashMap<Long, String>();
        Map<Long, String> templeteCreatorAlt = new HashMap<Long, String>();
        //设置图标,设置浮动显示的模版来源
        this.templateManager.floatDisplayTemplateSource(showTemplates, templeteIcon, templeteCreatorAlt);

        modelAndView.addObject("showCategorys", listTreeVo);
        modelAndView.addObject("showTemplates", JSONUtil.toJSONString(showTemplates));
        modelAndView.addObject("templeteIcon", templeteIcon);
        modelAndView.addObject("templeteCreatorAlt", templeteCreatorAlt);

        modelAndView.addObject("category", category);
        modelAndView.addObject("orgAccountId", orgAccountId);
        modelAndView.addObject("recent", recent);
        modelAndView.addObject("searchValue", Strings.toHTML(CtpTemplateUtil.unescape(searchValue), false));
        modelAndView.addObject("accounts", accounts);
        modelAndView.addObject("isShowTemplates", isShowTemplates);
        modelAndView.addObject("showRecentTemplate", showRecentTemplate);

        return modelAndView;

    }


    /**
     * 调用模板选择
     * @param request
     * @param response
     * @return
     * @throws BusinessException
     */
    public ModelAndView templateChoose(HttpServletRequest request, HttpServletResponse response)
            throws BusinessException {
        ModelAndView modelAndView = new ModelAndView("common/template/templateChoose");
        //如果调用了模板就不可以点击编辑-disCoverFlag 参数只有cap3模板才有效
        modelAndView.addObject("disCoverFlag", true);
        // 模板分类
        String category = request.getParameter("category");
        if (category == null) {
            return modelAndView;
        }
        User user = AppContext.getCurrentUser();
        Long orgAccountId = user.getLoginAccount();
        //模板选择单位，不传值默认为当前用户的登录单位
        String accountId = request.getParameter("accountId");
        if (Strings.isNotBlank(accountId)) {
            orgAccountId = Long.parseLong(accountId);
        }

        if (!user.isV5Member()) {
            orgAccountId = OrgHelper.getVJoinAllowAccount();
        }

        String condition = request.getParameter("condition");
        String sName = request.getParameter("sName");
        String smtype = request.getParameter("smtype");
        String[] typestrs = StringUtils.split(category, ",");
        List<ModuleType> moduleTypes = new ArrayList<ModuleType>();
        //判断是否有信息报送插件
        Boolean hasInfoPlugin = AppContext.hasPlugin("infosend");
        Boolean hasEdoc = AppContext.hasPlugin("edoc");
        String real_category = "";
        for (int i = 0; i < typestrs.length; i++) {
            String cType = typestrs[i];
            if ("-1".equals(cType)) {
                continue;
            }
            if (String.valueOf(ApplicationCategoryEnum.info.key()).equals(cType) && !hasInfoPlugin) {
                continue;
            }
            if (!hasEdoc && isEdoc(cType)) {
                continue;
            }
            real_category += cType + ",";
            try {
                ModuleType m = ModuleType.getEnumByKey(Integer.valueOf(cType));
                if (m != null) {
                    moduleTypes.add(m);
                }
            } catch (IllegalArgumentException e) {
                logger.error("获取枚举报错了", e);
            }
        }
        if (Strings.isNotBlank(real_category)) {
            real_category = real_category.substring(0, real_category.length() - 1);
            category = real_category;
        }
        // 取对应单位某模块的模板分类
        List<CtpTemplateCategory> templeteCategory = new ArrayList<CtpTemplateCategory>();
        List<CtpTemplateCategory> templeteCategoryTemp = templateManager.getCategorys(orgAccountId, moduleTypes);
        // 移出已经删除的模板分类
        if (!CollectionUtils.isEmpty(templeteCategoryTemp)) {
            for (CtpTemplateCategory ctpTemplateCategory : templeteCategoryTemp) {
                if (ctpTemplateCategory.isDelete() == null || !ctpTemplateCategory.isDelete()) {
                    templeteCategory.add(ctpTemplateCategory);
                }
            }
        }
        Map<Long, CtpTemplateCategory> idCategory = new HashMap<Long, CtpTemplateCategory>();
        Map<String, CtpTemplateCategory> nameCategory = new HashMap<String, CtpTemplateCategory>();
        for (CtpTemplateCategory c : templeteCategory) {
            nameCategory.put(c.getName(), c);
            idCategory.put(c.getId(), c);
        }
        //取个人模板
        List<CtpTemplate> personalTempletes = null;
        /**
         * StringUtil.checkNull(request.getParameter("templateChoose")) 新建页面走上面的判断
         * "true".equals(request.getParameter("templateChoose") 我的模板更多 走下面查询不出来数据 也改成了 走上面
         */
        if ((moduleTypes.contains(ModuleType.collaboration) || moduleTypes.contains(ModuleType.form)) &&
                "true".equals(request.getParameter("templateChoose"))) {

            List<ModuleType> m = new ArrayList<ModuleType>();
            m.add(ModuleType.collaboration);
            m.add(ModuleType.form);
            m.add(ModuleType.edoc);
//        	m.add(ModuleType.edocSend);
//        	m.add(ModuleType.edocRec);
//        	m.add(ModuleType.edocSign);
            m.add(ModuleType.govdocSend);
            m.add(ModuleType.govdocRec);
            m.add(ModuleType.govdocExchange);
            m.add(ModuleType.govdocSign);
            m.add(ModuleType.info);

            personalTempletes = templateManager.getPersonalTemplates(user.getId(), m);
        } else {
            personalTempletes = templateManager.getPersonalTemplates(user.getId(), moduleTypes);
        }
        // 所有模板，包括协同和表单
        List<CtpTemplate> systemTempletes = templateManager.getSystemTemplatesByAcl(user.getId(), moduleTypes);

        List<CtpTemplate> showTempletes = new ArrayList<CtpTemplate>();
        // 是否显示外单位模板
        boolean isShowOuter = Boolean.valueOf(Functions.getSysFlag("col_showOtherAccountTemplate").toString());
        List<CtpTemplateCategory> outerCategory = new ArrayList<CtpTemplateCategory>();
        for (CtpTemplate template : systemTempletes) {
            //外单位的模板才进行分类合并
            if (!template.getOrgAccountId().equals(orgAccountId)) {
                if (!isShowOuter) {
                    continue;
                }
                if (template.isSystem() && template.getCategoryId() != 0) {// 等于0是顶层的模板
                    CtpTemplateCategory tc = templateManager.getCtpTemplateCategory(template.getCategoryId());
                    if (tc != null) {
                        CtpTemplateCategory n = nameCategory.get(tc.getName());
                        if (n != null) {
                            template.setCategoryId(n.getId());
                        } else {
                            //TODO 先这么处理，外单位授权过来的直接显示在根目录下面，
                            //如果需要按照原有单位的单位层级显示的话，后面再考虑，先保持跟3.5 一致
                            //tc.setParentId(1l);
                            if (tc.getParentId() == null) {
                                tc.setParentId(1l);
                            } else {
                                tc.setParentId(tc.getParentId());
                            }

                            outerCategory.add(tc);
                            idCategory.put(tc.getId(), tc);
                            nameCategory.put(tc.getName(), tc);
                        }
                    }
                }
            }
            showTempletes.add(template);
        }
        List<TemplateTreeVo> listTreeVo = new ArrayList<TemplateTreeVo>();
        listTreeVo = getTemplateTree(listTreeVo, category, idCategory, showTempletes, personalTempletes, condition, sName, smtype, false);
        if (null == request.getParameter("templateChoose")) {//如果不是从我的模板更多页面配置的
            List<TemplateTreeVo> newListTreeVo = new ArrayList<TemplateTreeVo>();
            Map<Long, TemplateTreeVo> realCategory = new HashMap<Long, TemplateTreeVo>();
            List<TemplateTreeVo> realTemplates = new ArrayList<TemplateTreeVo>();
            List<TemplateTreeVo> categorys = new ArrayList<TemplateTreeVo>();
            for (TemplateTreeVo vo : listTreeVo) {
                // 分类
                if ("category".equals(vo.getType()) || "personal".equals(vo.getType()) || "template_coll".equals(vo.getType())
                        || "text_coll".equals(vo.getType()) || "workflow_coll".equals(vo.getType()) || "edoc_coll".equals(vo.getType())) {
                    realCategory.put(vo.getId(), vo);
                    // 协同三个根节点："最近使用模板"、"公共模板"、"个人模板"无论下面是否有具体模板，都在前端展示。公文显示对应的公文
                    List<Long> cateGoryList = new ArrayList<Long>();
                    cateGoryList.add(0L);
                    cateGoryList.add(100L);
                    cateGoryList.add(110L);

                    cateGoryList.add(Long.valueOf(ModuleType.edoc.getKey()));
                    cateGoryList.add(Long.valueOf(ModuleType.edocSend.getKey()));
                    cateGoryList.add(Long.valueOf(ModuleType.edocRec.getKey()));
                    cateGoryList.add(Long.valueOf(ModuleType.edocSign.getKey()));

                    if (cateGoryList.contains(vo.getId())) {
                        categorys.add(vo);
                    }
                } else {// 具体模板
                    realTemplates.add(vo);
                }
            }
            for (TemplateTreeVo template : realTemplates) {
                // 如果包含此模板的分类
                if (realCategory.containsKey(template.getpId())) {
                    TemplateTreeVo theCategory = realCategory.get(template.getpId());
                    getParentCategory(theCategory, realCategory, categorys);
                }
            }

            newListTreeVo.addAll(categorys);
            newListTreeVo.addAll(realTemplates);
            //通过category的sort排序后的tree
            for (Iterator<TemplateTreeVo> it = listTreeVo.iterator(); it.hasNext(); ) {
                TemplateTreeVo t = it.next();
                if (!newListTreeVo.contains(t)) {
                    it.remove();
                }
            }
        }

        request.setAttribute("fftree", listTreeVo);
        // 判断是否是来自我的模板配置
        // 如果来自我得模板配置则需要得到发布到首页的模板ID列表
        if (Strings.isNotBlank(request.getParameter("templateChoose"))) {
            // 已选的模板
            List<CtpTemplate> templeteList = templateManager.getPersonalTemplete(category, -1, false);
            Long[] templeteIds = null;
            if (templeteList != null) {
                templeteIds = new Long[templeteList.size()];
                for (int i = 0; i < templeteList.size(); i++) {
                    templeteIds[i] = templeteList.get(i).getId();
                }
            }
            request.setAttribute("templateChoose", true);
            request.setAttribute("fftempleteList", templeteIds);
        }
        String optionCategory = transCategoryHtml(moduleTypes, idCategory);
        modelAndView.addObject("optionCategory", optionCategory);
        modelAndView.addObject("templeteCategory", templeteCategory);
        modelAndView.addObject("outerCategory", outerCategory);
        modelAndView.addObject("sysTempletes", showTempletes);
        modelAndView.addObject("isEdoc", isEdoc(category));
        // 传到前台 查询的时候传入到后台
        modelAndView.addObject("category", category);
        // 传到页面上查询的时候传入到后台
        modelAndView.addObject("accountId", accountId);

        modelAndView.addObject("condition", condition);
        modelAndView.addObject("sName", sName);
        modelAndView.addObject("smtype", smtype);
        return modelAndView;
    }

    private List<TemplateTreeVo> addRecentlyUsedTemplate(List<TemplateTreeVo> listTreeVo, List<CtpTemplate> personalRencentTemplete, String category) throws BusinessException {

        TemplateTreeVo rut = new TemplateTreeVo();
        rut.setId(110L);
        rut.setName(ResourceUtil.getString("template.choose.category.recent.label"));//最近使用根目录
        rut.setpId(null);
        rut.setType("category");
        listTreeVo.add(rut);
        CtpTemplate ctpTemplate = null;
        Long la = AppContext.getCurrentUser().getLoginAccount();
        boolean colFlag = true;
        if ("19".equals(category) || "20".equals(category) || "21".equals(category)) {
            colFlag = false;
        }
        for (int a = 0; a < personalRencentTemplete.size(); a++) {
            ctpTemplate = personalRencentTemplete.get(a);
            if (!ctpTemplate.isSystem() && null != ctpTemplate.getFormParentid()) {
                CtpTemplate parent_t = templateManager.getCtpTemplate(ctpTemplate.getFormParentid());
                if (null != parent_t && (parent_t.isDelete() || Integer.valueOf(TemplateEnum.State.invalidation.ordinal()).equals(parent_t.getState()))) {
                    continue;
                }
            }

            if (null != ctpTemplate.getModuleType()) {
                if (!(ctpTemplate.getModuleType().toString().equals(category)) &&
                        !("1".equals(ctpTemplate.getModuleType().toString()) && !("2".equals(ctpTemplate.getModuleType().toString())
                        ))) {
                    continue;
                }
            }
            rut = new TemplateTreeVo();
            rut.setId(ctpTemplate.getId());
            String shortName = "";
            if (!ctpTemplate.getOrgAccountId().equals(-1l) && null != ctpTemplate.getOrgAccountId() && !la.equals(ctpTemplate.getOrgAccountId()) && ctpTemplate.isSystem()) {
                shortName = orgManager.getAccountById(ctpTemplate.getOrgAccountId()).getShortName();
                rut.setName(ctpTemplate.getSubject() + "(" + shortName + ")");
            } else {
                rut.setName(ctpTemplate.getSubject());
            }
            if (!colFlag) {
                rut.setIsEdoc(true);
            }
            rut = setWendanId(rut, ctpTemplate);
            rut.setWorkflowId(ctpTemplate.getWorkflowId());
            rut.setBodyType(ctpTemplate.getBodyType());
            rut.setSystem(ctpTemplate.isSystem());
            if (null != ctpTemplate.getFormParentid()) {
                CtpTemplate ctpP = templateManager.getCtpTemplate(ctpTemplate.getFormParentid());
                if (null != ctpP && !"text".equals(ctpP.getType())) {
                    rut.setWorkflowId(ctpP.getWorkflowId());
                    rut.setSystem(true);
                }
            }
            rut.setType(ctpTemplate.getType());
            try {
                if (null != ctpTemplate.getModuleType()) {
                    rut.setCategoryType(ctpTemplate.getModuleType());
                }
            } catch (Exception e) {
                LOG.error("", e);
            }

            //设置浮动显示的title
            V3xOrgMember createrMember = orgManager.getMemberById(ctpTemplate.getMemberId());
            StringBuilder showTitle = new StringBuilder();
            if (null != createrMember) {
                if (Strings.isNotBlank(createrMember.getName())) {
                    //创建人
                    String creater = ResourceUtil.getString("collaboration.summary.createdBy") + ":";
                    if (null != ctpTemplate.getOrgAccountId() && !ctpTemplate.getOrgAccountId().equals(la)) {

                        showTitle.append(creater + createrMember.getName()//显示单位管理员加简称
                                + "(" + shortName + ")" + "\r");
                    } else {
                        showTitle.append(creater + createrMember.getName() + "\r");
                    }
                }
                try {
                    if (!createrMember.getIsAdmin()) {
                        //部门
                        String departMent = ResourceUtil.getString("org.department.label") + ":";
                        showTitle.append(departMent + Functions.showDepartmentFullPath(createrMember.getOrgDepartmentId()) + "\r");
                    }
                } catch (Exception e) {
                    //showTitle.append("部门:"+" ");
                }
                try {
                    //岗位
                    String post = ResourceUtil.getString("org.post.label") + ":";
                    showTitle.append(post + orgManager.getPostById(createrMember.getOrgPostId()).getName());
                } catch (Exception e) {
                    //showTitle.append("岗位:"+" ");
                }
                rut.setFullName(showTitle.toString());
            }

            String icon = getTemplateIcon(ctpTemplate);
            rut.setIconSkin(icon);
            rut.setpId(110L);
            rut.setFormAppId(ctpTemplate.getFormAppId());

            if (WFComponentUtil.isForm(rut.getBodyType()) && null != rut.getFormAppId()) {
                rut.setCAP4(capFormManager.isCAP4Form(rut.getFormAppId()));
            }
            rut.setSystem(ctpTemplate.isSystem());
            listTreeVo.add(rut);
        }
        return listTreeVo;
    }

    private TemplateTreeVo setWendanId(TemplateTreeVo vo, CtpTemplate t) throws BusinessException {

        if (vo.getIsEdoc() != null && vo.getIsEdoc()) {
            CtpTemplate ct = templateManager.getCtpTemplate(t.getId());
            if (null != ct && Strings.isNotBlank(ct.getSummary())) {

                //TODO
                /**     EdocSummary es = (EdocSummary)XMLCoder.decoder(ct.getSummary());
                 vo.setWendanId(es.getFormId());
                 */
            }
        }
        return vo;
    }

    /**
     * 查询条件中的模板类型条件的值
     * @param moduleTypes
     * @param nameCategory
     * @return
     */
    private String transCategoryHtml(List<ModuleType> moduleTypes, Map<Long, CtpTemplateCategory> nameCategory) {
        List<Map<String, String>> jsonList = new ArrayList<Map<String, String>>();
        Map<String, String> jsonMap;
        // 公文的
        boolean pdocFlag = false;

        if (moduleTypes.contains(ModuleType.edoc) || moduleTypes.contains(ModuleType.edocSend)
                || moduleTypes.contains(ModuleType.edocRec) || moduleTypes.contains(ModuleType.edocSign) || moduleTypes.contains(ModuleType.govdoc) || moduleTypes.contains(ModuleType.govdocSend)
                || moduleTypes.contains(ModuleType.govdocRec) || moduleTypes.contains(ModuleType.govdocSign)) {
            Set<Long> keySet = nameCategory.keySet();
            jsonMap = new HashMap<String, String>();
            if (moduleTypes.contains(ModuleType.govdocSend)) {
                jsonMap.put("id", "401");
                jsonMap.put("name", ResourceUtil.getString("template.edocsend.label"));
                jsonList.add(jsonMap);
            } else if (moduleTypes.contains(ModuleType.govdocRec)) {
                jsonMap.put("id", "402");
                jsonMap.put("name", ResourceUtil.getString("template.edocrec.label"));
                jsonList.add(jsonMap);
            } else if (moduleTypes.contains(ModuleType.govdocSign)) {
                jsonMap.put("id", "404L");
                jsonMap.put("name", ResourceUtil.getString("template.edocsign.label"));
                jsonList.add(jsonMap);
            }
            for (Long id : keySet) {
                CtpTemplateCategory category = nameCategory.get(id);
                if (nameCategory.get(category.getParentId()) != null) {
                    continue;
                }
                jsonMap = new HashMap<String, String>();
                jsonMap.put("id", String.valueOf(category.getId()));
                jsonMap.put("name", category.getName());
                jsonList.add(jsonMap);
                this.addChildCategory(nameCategory, category.getId(), jsonList);
            }
        }
        // 表单，协同的
        if (moduleTypes.contains(ModuleType.collaboration) || moduleTypes.contains(ModuleType.form)) {
            // 公共部分
            Set<Long> keySet = nameCategory.keySet();
            for (Long id : keySet) {
                CtpTemplateCategory category = nameCategory.get(id);
                if (nameCategory.get(category.getParentId()) != null) {
                    continue;
                }
                jsonMap = new HashMap<String, String>();
                jsonMap.put("id", String.valueOf(category.getId()));
                jsonMap.put("name", category.getName());
                jsonList.add(jsonMap);
                this.addChildCategory(nameCategory, category.getId(), jsonList);
            }

            jsonMap = new HashMap<String, String>();
            jsonMap.put("id", "101");
            jsonMap.put("name", ResourceUtil.getString("collaboration.template.category.type.0")); //协同模板
            jsonList.add(jsonMap);

            jsonMap = new HashMap<String, String>();
            jsonMap.put("id", "102");
            jsonMap.put("name", ResourceUtil.getString("collaboration.saveAsTemplate.formatTemplate")); //格式模板
            jsonList.add(jsonMap);

            jsonMap = new HashMap<String, String>();
            jsonMap.put("id", "103");
            jsonMap.put("name", ResourceUtil.getString("collaboration.saveAsTemplate.flowTemplate")); //流程模板
            jsonList.add(jsonMap);
        }
        return JSONUtil.toJSONString(jsonList);
    }

    private void addChildCategory(Map<Long, CtpTemplateCategory> nameCategory, Long categoryId, List<Map<String, String>> jsonList) {
        Map<String, String> jsonMap;
        Set<Long> keySet = nameCategory.keySet();
        for (Long str : keySet) {
            CtpTemplateCategory category = nameCategory.get(str);
            if (category.getParentId().equals(categoryId)) {
                jsonMap = new HashMap<String, String>();
                jsonMap.put("id", String.valueOf(category.getId()));
                jsonMap.put("name", this.getNbspLength(nameCategory, category.getParentId()) + category.getName());
                jsonList.add(jsonMap);
                this.addChildCategory(nameCategory, category.getId(), jsonList);
            }
        }
    }

    private String getNbspLength(Map<Long, CtpTemplateCategory> nameCategory, Long categoryId) {
        CtpTemplateCategory category = nameCategory.get(categoryId);
        if (category == null) {
            return "";
        } else {
            return "&nbsp;&nbsp;&nbsp;&nbsp;" + this.getNbspLength(nameCategory, category.getParentId());
        }
    }

    private List<TemplateTreeVo> getTemplateTree(List<TemplateTreeVo> listTreeVo, String category, Map<Long, CtpTemplateCategory> idCategory, List<CtpTemplate> showTempletes,
                                                 List<CtpTemplate> personalTempletes, String condition, String sName, String smtype, boolean isTemplateMore) throws BusinessException {
        List<CtpTemplate> personalRencentTemplete = new ArrayList<CtpTemplate>();
        List<Long> listTreeVoIds = new ArrayList<Long>();
        //如果是我的模版更多页面则不需要查询个人的
        if (!isTemplateMore) {
            String categoryStr = category + "," + String.valueOf(TemplateCategoryConstant.personRoot.key());
            personalRencentTemplete = templateManager.getRecentTemplates(categoryStr, 10);
        }
        // 查询进入的
        if (Strings.isNotBlank(condition)) {
            //按名字查询
            if ("templateName".equals(condition)) {
                String searchTN = sName;
                for (int a = showTempletes.size() - 1; a > -1; a--) {
                    String tempName = showTempletes.get(a).getSubject();
                    if (Strings.isNotBlank(tempName) && !(tempName.indexOf(searchTN) > -1)) {
                        showTempletes.remove(showTempletes.get(a));
                    }
                }
                //最近使用
                for (int a = personalRencentTemplete.size() - 1; a > -1; a--) {
                    if (!(personalRencentTemplete.get(a).getSubject().indexOf(searchTN) > -1)) {
                        personalRencentTemplete.remove(a);
                    }
                }
                for (int a = personalTempletes.size() - 1; a > -1; a--) {
                    CtpTemplate pt = personalTempletes.get(a);
                    if (pt != null) {
                        if (Strings.isNotBlank(pt.getSubject())) {
                            if (!(pt.getSubject().indexOf(searchTN) > -1)) {
                                personalTempletes.remove(personalTempletes.get(a));
                            }
                        }
                    }
                }
            }
            //按分类查询
            if ("applied".equals(condition)) {
                for (int a = showTempletes.size() - 1; a > -1; a--) {
                    if (null == showTempletes.get(a).getCategoryId()) {
                        showTempletes.remove(showTempletes.get(a));
                        continue;
                    }
                    if (!(showTempletes.get(a).getCategoryId() == Long.valueOf(smtype).longValue())) {
                        showTempletes.remove(showTempletes.get(a));
                    }

                }
                if ("101".equals(smtype) || "102".equals(smtype) || "103".equals(smtype)) {
                    //个人模板下的 协同 流程 格式模板
                    for (int a = personalTempletes.size() - 1; a > -1; a--) {
                        if (null != personalTempletes.get(a).getCategoryId()) {
                            personalTempletes.remove(personalTempletes.get(a));
                            continue;
                        }
                        if ("101".equals(smtype) && !"template".equals(personalTempletes.get(a).getType())) {
                            personalTempletes.remove(personalTempletes.get(a));
                        }
                        if ("102".equals(smtype) && !"text".equals(personalTempletes.get(a).getType())) {
                            personalTempletes.remove(personalTempletes.get(a));
                        }
                        if ("103".equals(smtype) && !"workflow".equals(personalTempletes.get(a).getType())) {
                            personalTempletes.remove(personalTempletes.get(a));
                        }
                    }
                } else if ("104".equals(smtype)) {
                    for (int a = personalTempletes.size() - 1; a > -1; a--) {
                        if (!"templete".equals(personalTempletes.get(a).getType())) {
                            personalTempletes.remove(personalTempletes.get(a));
                        }
                    }
                } else {
                    for (int a = personalTempletes.size() - 1; a > -1; a--) {
                        if (null == personalTempletes.get(a).getCategoryId()) {
                            personalTempletes.remove(personalTempletes.get(a));
                            continue;
                        }
                        if (!(personalTempletes.get(a).getCategoryId() == Long.valueOf(smtype).longValue())) {
                            personalTempletes.remove(personalTempletes.get(a));
                        }
                    }
                }
            }
        }


        //sp1需求 树上增加最近使用模板
        if (!isTemplateMore && ("1,2,66".equals(category) || "401".equals(category) || "402".equals(category) || "404".equals(category)) && !"applied".equals(condition)) {
            listTreeVo = addRecentlyUsedTemplate(listTreeVo, personalRencentTemplete, category);
            listTreeVoIds.add(110L);
        }

        String[] tv = category.replaceAll("C_", "").split(",");
        List<Long> typesL = new ArrayList<Long>();
        for (int i = 0; i < tv.length; i++) {
            if (Strings.isNotBlank(tv[i])) {
                typesL.add(new Long(tv[i]));
            }
        }
        boolean pdocflag = false;
        boolean infoflag = false;
        if (typesL.contains(Long.valueOf(ModuleType.info.getKey())) && AppContext.hasPlugin("infosend")) {
            infoflag = true;
        }
        if (typesL.contains(Long.valueOf(ModuleType.govdocRec.getKey())) || typesL.contains(Long.valueOf(ModuleType.govdocSend.getKey()))
                || typesL.contains(Long.valueOf(ModuleType.govdocSign.getKey()))
                || (typesL.contains(Long.valueOf(TemplateCategoryConstant.edocRoot.key())) || typesL.contains(Long.valueOf(ModuleType.edoc.getValue())))) {

            /*
             * G6 6.1 新表单公文时不现实老公文模板*/
            if (typesL.contains(Long.valueOf(ModuleType.edoc.getKey()))) {
                listTreeVo.add(setCategory(ModuleType.edoc.getValue()));
                pdocflag = true;
            }
            if (typesL.contains(Long.valueOf(ModuleType.govdocSend.getKey()))) {
                listTreeVo.add(setCategory(ModuleType.govdocSend.getValue()));
                listTreeVoIds.add(Long.valueOf(ModuleType.govdocSend.getValue()));
                pdocflag = true;
            }
            if (typesL.contains(Long.valueOf(ModuleType.govdocRec.getKey()))) {
                listTreeVo.add(setCategory(ModuleType.govdocRec.getValue()));
                listTreeVoIds.add(Long.valueOf(ModuleType.govdocRec.getValue()));
                pdocflag = true;
            }
            if (typesL.contains(Long.valueOf(ModuleType.govdocSign.getKey()))) {
                listTreeVo.add(setCategory(ModuleType.govdocSign.getValue()));
                listTreeVoIds.add(Long.valueOf(ModuleType.govdocSign.getValue()));
                pdocflag = true;
            }
            if ((typesL.contains(Long.valueOf(TemplateCategoryConstant.edocRoot.key())) || typesL.contains(Long.valueOf(ModuleType.edoc.getValue()))) && !pdocflag) {
                listTreeVo.add(setCategory(ModuleType.govdocSend.getValue()));
                listTreeVoIds.add(Long.valueOf(ModuleType.govdocSend.getValue()));
                listTreeVo.add(setCategory(ModuleType.govdocRec.getValue()));
                listTreeVoIds.add(Long.valueOf(ModuleType.govdocRec.getValue()));
                listTreeVo.add(setCategory(ModuleType.govdocSign.getValue()));
                listTreeVoIds.add(Long.valueOf(ModuleType.govdocSign.getValue()));
            }
            if (isTemplateMore || typesL.contains(Long.valueOf(ModuleType.edoc.getKey()))) {
                listTreeVo.add(setCategory(ModuleType.edoc.getValue()));
                listTreeVoIds.add(Long.valueOf(ModuleType.edoc.getValue()));
                pdocflag = true;
            }
            if (typesL.contains(Long.parseLong(String.valueOf(ModuleType.info.getKey())))) {
                infoflag = true;
            }
        }
        for (String s : tv) {//这段逻辑只组装所选分类的父模板树。子模板树通过传入的参数idCategory组装
            if (Strings.isBlank(s)) {
                continue;
            }
            // 构造几个个人分类目录
            TemplateTreeVo ttPersonlVO = new TemplateTreeVo();
            if ("1".equals(s) || "2".equals(s)
                    || String.valueOf(TemplateCategoryConstant.personRoot.key()).equals(s)
                    || String.valueOf(TemplateCategoryConstant.publicRoot.key()).equals(s)) {
                // 表单和协同的构建根节点(pid为空的，则为顶层)
                TemplateTreeVo templateTreeVO = new TemplateTreeVo(0L, ResourceUtil.getString("template.public.label"), "category", null, "");
                TemplateTreeVo ttPersonlVO1 = new TemplateTreeVo(101L, ResourceUtil.getString("collaboration.template.category.type.0"), "template_coll", 100L, "");//"协同模板"
                TemplateTreeVo ttPersonlVO2 = new TemplateTreeVo(102L, ResourceUtil.getString("collaboration.saveAsTemplate.formatTemplate"), "text_coll", 100L, "");//"格式模板"
                TemplateTreeVo ttPersonlVO3 = new TemplateTreeVo(103L, ResourceUtil.getString("collaboration.saveAsTemplate.flowTemplate"), "workflow_coll", 100L, "");//"流程模板"
                TemplateTreeVo ttPersonlVO4 = null;
                TemplateTreeVo ttPersonlVO5 = null;
                TemplateTreeVo ttPersonlVO6 = null;
                if (pdocflag || (isTemplateMore && typesL.contains(Long.valueOf(TemplateCategoryConstant.personRoot.key())))) {
                    ttPersonlVO4 = new TemplateTreeVo(104L, ResourceUtil.getString("collaboration.saveAsTemplate.edocPtem"), "category", 100L, "");
                    ttPersonlVO4.setIsEdoc(true);
                }
                if (infoflag) {
                    ttPersonlVO5 = new TemplateTreeVo(105L, ResourceUtil.getString("template.info.personal.label"), "category", 100L, "");
                    ttPersonlVO5.setIsInfo(true);
                    ttPersonlVO6 = new TemplateTreeVo(32L, ResourceUtil.getString("template.info.label"), "category", -32L, "");
                    ttPersonlVO6.setIsInfo(true);
                }
                boolean isCol = typesL.contains(Long.valueOf(TemplateCategoryConstant.publicRoot.key())) || typesL.contains(1L) || typesL.contains(2L);
                if (!listTreeVoIds.contains(0L) && (!isTemplateMore || (isTemplateMore && isCol))) {
                    listTreeVo.add(templateTreeVO);
                    listTreeVoIds.add(0L);
                }
                if (!listTreeVoIds.contains(101L)) {
                    listTreeVo.add(ttPersonlVO1);
                    listTreeVoIds.add(101L);
                }
                if (!listTreeVoIds.contains(102L)) {
                    listTreeVo.add(ttPersonlVO2);
                    listTreeVoIds.add(102L);
                }
                if (!listTreeVoIds.contains(103L)) {
                    listTreeVo.add(ttPersonlVO3);
                    listTreeVoIds.add(103L);
                }
                if (isTemplateMore && typesL.contains(Long.valueOf(TemplateCategoryConstant.personRoot.key()))) {
                    if (!listTreeVoIds.contains(104L)) {
                        listTreeVo.add(ttPersonlVO4);
                        listTreeVoIds.add(104L);
                    }
                }
                if (infoflag) {
                    if (!listTreeVoIds.contains(105L)) {
                        listTreeVo.add(ttPersonlVO5);
                        listTreeVoIds.add(105L);
                        listTreeVo.add(ttPersonlVO6);
                        listTreeVoIds.add(106L);
                    }
                }
                // 插入个人模板到相应分类下面
                if (personalTempletes != null) {
                    for (CtpTemplate t : personalTempletes) {
                        Boolean isGovDocTempalte = Integer.valueOf(32).equals(t.getModuleType()) || Integer.valueOf(401).equals(t.getModuleType()) || Integer.valueOf(402).equals(t.getModuleType())
                                || Integer.valueOf(403).equals(t.getModuleType()) || Integer.valueOf(404).equals(t.getModuleType());
                        Boolean isEdocTempalte = Integer.valueOf(19).equals(t.getModuleType()) || Integer.valueOf(20).equals(t.getModuleType())
                                || Integer.valueOf(21).equals(t.getModuleType());

                        Long pId = null;
                        if (null == t.getType() && null == t.getSubject()) {
                            continue;
                        }
                        if ("template".equals(t.getType())) {
                            pId = ttPersonlVO1.getId();
                        } else if ("text".equals(t.getType())) {
                            pId = ttPersonlVO2.getId();
                        } else if ("workflow".equals(t.getType())) {
                            pId = ttPersonlVO3.getId();
                        } else if ("templete".equals(t.getType())) {//公文
                            if (!pdocflag) {
                                continue;
                            }
                            if (null != ttPersonlVO4) {
                                pId = ttPersonlVO4.getId();
                            }
                        } else if (Integer.valueOf(32).equals(t.getModuleType())) {
                            pId = ttPersonlVO5.getId();
                        }
                        ttPersonlVO = new TemplateTreeVo(t.getId(), t.getSubject(), t.getType(), pId, t.getBodyType());
                        if ("templete".equals(t.getType()) && isEdocTempalte) {
                            ttPersonlVO.setIsEdoc(true);
                        } else if (null != t.getModuleType() && Integer.valueOf(32).equals(t.getModuleType())) {
                            ttPersonlVO.setIsInfo(true);
                        }
                        ttPersonlVO.setWorkflowId(t.getWorkflowId());
                        ttPersonlVO.setSystem(t.isSystem());
                        ttPersonlVO.setBodyType(t.getBodyType());
                        ttPersonlVO.setFormParentid(t.getFormParentid());

                        //个人协同和流程模板取父模板的wfId
                        if (!t.isSystem() && null != t.getFormParentid()) {
                            CtpTemplate xyz = templateManager.getCtpTemplateFromCache(t.getFormParentid());
                            if (null == xyz) {
                                continue;
                            }
                            if (xyz.isSystem() && null != xyz.getWorkflowId()) {
                                ttPersonlVO.setWorkflowId(xyz.getWorkflowId());
                                ttPersonlVO.setFormAppId(xyz.getFormAppId());
                                ttPersonlVO.setSystem(true);
                                if (WFComponentUtil.isForm(ttPersonlVO.getBodyType())) {
                                    ttPersonlVO.setCAP4(capFormManager.isCAP4Form(ttPersonlVO.getFormAppId()));
                                }
                            }
                        }
                        String icon = getTemplateIcon(t);
                        ttPersonlVO.setIconSkin(icon);
                        ttPersonlVO.setCategoryType(t.getModuleType());
                        if (!listTreeVoIds.contains(t.getId())) {
                            listTreeVo.add(ttPersonlVO);
                        }
                    }
                }
                break;
            } else if (isGovdoc(s)) {
                if (personalTempletes != null) {
                    TemplateTreeVo vp = null;
                    for (CtpTemplate t : personalTempletes) {
                        try {//数据防护
                            if (!(t.getCategoryId().toString().equals(s))) {//个人模板过滤出各种类型
                                continue;
                            }
                            vp = new TemplateTreeVo();
                            vp.setId(t.getId());
                            vp.setName(t.getSubject());
                            vp.setType(t.getType());
                            vp.setWorkflowId(t.getWorkflowId());
                            vp.setSystem(t.isSystem());
                            vp.setFormParentid(t.getFormParentid());
                            //个人协同和流程模板取父模板的wfId
                            if (!t.isSystem() && null != t.getFormParentid()) {
                                CtpTemplate pt = templateManager.getCtpTemplate(t.getFormParentid());
                                if (null == pt) {
                                    continue;
                                }
                                if (pt.isSystem() && null != pt.getWorkflowId()) {
                                    vp.setWorkflowId(pt.getWorkflowId());
                                }
                            }
                            vp.setIsEdoc(true);
                            vp.setBodyType(t.getBodyType());
                            try {
                                vp.setCategoryType(t.getModuleType());
                            } catch (Exception e) {
                            }
                            if ("templete".equals(t.getType()) || "template".equals(t.getType()))
                                vp.setpId(100L);

                            String icon = getTemplateIcon(t);
                            vp.setIconSkin(icon);

                            listTreeVo.add(vp);
                        } catch (Exception e) {
                            LOG.error("ID为***********" + t.getId() + "*************的模板存在数据问题，不允许掉用。");
                        }
                    }
                }
            } else if (isEdoc(s)) {
                if (personalTempletes != null) {
                    TemplateTreeVo vp = null;
                    for (CtpTemplate t : personalTempletes) {
                        try {//数据防护
                            if (!(t.getCategoryId().toString().equals(category))) {//个人模板过滤出各种类型
                                continue;
                            }
                            vp = new TemplateTreeVo();
                            vp.setId(t.getId());
                            vp.setName(t.getSubject());
                            vp.setType(t.getType());
                            vp.setWorkflowId(t.getWorkflowId());
                            vp.setSystem(t.isSystem());
                            //个人协同和流程模板取父模板的wfId
                            if (!t.isSystem() && null != t.getFormParentid()) {
                                CtpTemplate pt = templateManager.getCtpTemplate(t.getFormParentid());
                                if (null == pt) {
                                    continue;
                                }
                                if (pt.isSystem() && null != pt.getWorkflowId()) {
                                    vp.setWorkflowId(pt.getWorkflowId());
                                    vp.setSystem(true);
                                }
                            }
                            vp.setIsEdoc(true);
                            vp.setBodyType(t.getBodyType());
                            try {
                                vp.setCategoryType(t.getModuleType());
                            } catch (Exception e) {
                                LOG.error("", e);
                            }
                            if ("templete".equals(t.getType()))
                                vp.setpId(100L);

                            String icon = getTemplateIcon(t);
                            vp.setIconSkin(icon);
                            if (!listTreeVoIds.contains(t.getId())) {
                                listTreeVo.add(vp);
                            }
                        } catch (Exception e) {
                            LOG.error("ID为***********" + t.getId() + "*************的模板存在数据问题，不允许掉用。", e);
                        }
                    }
                }
            } else if (infoflag) {
                TemplateTreeVo templateTreeVOInfo = new TemplateTreeVo(300L, ResourceUtil.getString("template.information.lable"), "category", null, "");
                templateTreeVOInfo.setIsInfo(true);
                if (!listTreeVoIds.contains(300L)) {
                    listTreeVo.add(templateTreeVOInfo);
                }
            } else { //选择协同具体的模板分类
                listTreeVo.addAll(createCategoryParentTreeVO(listTreeVo, s, listTreeVoIds));
            }
        }
        // 个人模板
        if (!isTemplateMore) {
            TemplateTreeVo tvo = new TemplateTreeVo(100L, ResourceUtil.getString("template.templatePub.personalTemplates"), "personal", null, "");//"个人模板"
            listTreeVo.add(tvo);
        } else if (typesL.contains(Long.valueOf(TemplateCategoryConstant.personRoot.key()))) {//模板更多个人模板显示受控制
            TemplateTreeVo tvo = new TemplateTreeVo(100L, ResourceUtil.getString("template.templatePub.personalTemplates"), "personal", null, "");//"个人模板"
            listTreeVo.add(tvo);
        }

        transCategory2TreeVo(idCategory, listTreeVo);
        transTemplate2TreeVo(showTempletes, listTreeVo);
        return listTreeVo;
    }

    // 获取父节点分类，如果新的分类列表中没有添加，则将其添加进去
    private void getParentCategory(TemplateTreeVo category, Map<Long, TemplateTreeVo> realCategory, List<TemplateTreeVo> categorys) {
        if (category != null && category.getpId() != null && realCategory.containsKey(category.getpId())) {
            TemplateTreeVo pCategory = realCategory.get(category.getpId());
            if (pCategory != null && !categorys.contains(pCategory)) {
                categorys.add(pCategory);
            }
            getParentCategory(pCategory, realCategory, categorys);
        }
        if (category != null && realCategory.containsKey(category.getId())) {
            if (!categorys.contains(category)) {
                categorys.add(category);
            }
        }
    }



    private List<CtpTemplateCategory> getAllCategorys(String s) {
        if (Strings.isBlank(s)) {
            return new ArrayList<CtpTemplateCategory>();
        }
        String[] arr = s.split(",");
        List<CtpTemplateCategory> resultList = new ArrayList<CtpTemplateCategory>();
        List<CtpTemplateCategory> list = new ArrayList<CtpTemplateCategory>();
        List<Long> cats = new ArrayList<Long>();
        CtpTemplateCategory category = null;
        String categoryName = null;
        for (String id : arr) {
            Long categoryId = Long.valueOf(id);
            if (Long.valueOf(TemplateCategoryConstant.publicRoot.key()).equals(categoryId)) {
                cats.add(Long.valueOf(ModuleType.collaboration.getKey()));
                cats.add(Long.valueOf(ModuleType.form.getKey()));
            } else if (Long.valueOf(TemplateCategoryConstant.edocRoot.key()).equals(categoryId)) {
                cats.add(Long.valueOf(TemplateCategoryConstant.govdocSendRoot.key()));
                cats.add(Long.valueOf(TemplateCategoryConstant.govdocRecRoot.key()));
                cats.add(Long.valueOf(TemplateCategoryConstant.govdocSignRoot.key()));
            } else {
                cats.add(categoryId);
            }
        }

        for (Long cateId : cats) {
            try {
                category = templateManager.getCategoryIncludeAllChildren(cateId);
            } catch (BusinessException e) {
                LOG.error("获取模板分类异常", e);
            }
            if (category != null) {
                // 判断是否是国际化名称,
                categoryName = ResourceUtil.getString(category.getName());
                if (Strings.isNotBlank(categoryName)) {
                    category.setName(categoryName);
                }
                list.add(category);
            }
        }

        getAllCategorysCascade(list, resultList);

        return resultList;
    }

    private void getAllCategorysCascade(List<CtpTemplateCategory> currentCategorys, List<CtpTemplateCategory> allResultList) {
        if (Strings.isEmpty(currentCategorys)) {
            return;
        }
        allResultList.addAll(currentCategorys);

        for (CtpTemplateCategory category : currentCategorys) {
            try {
                List<CtpTemplateCategory> childrens = category.getAllCascadeChildrens();
                if (Strings.isNotEmpty(childrens)) {
                    allResultList.addAll(childrens);
                }
            } catch (Exception e) {
                LOG.error("", e);
            }
        }
    }

    /**
     * 将CtpTemplateCategory模板类型对象转换为树节点对象
     * @param idCategory
     * @param listTreeVo
     */
    private void transCategory2TreeVo(Map<Long, CtpTemplateCategory> idCategory, List<TemplateTreeVo> listTreeVo) {
        //分类
        TemplateTreeVo templateTreeVO = null;
        List<CtpTemplateCategory> categoryList = new ArrayList<CtpTemplateCategory>();
        for (CtpTemplateCategory ctpTemplateCategory : idCategory.values()) {
            categoryList.add(ctpTemplateCategory);
        }
        Collections.sort(categoryList, new TemplateCategoryComparator());
        for (CtpTemplateCategory ctpTemplateCategory : categoryList) {
            //做个防护 去除表单模板这个分类。
            if (ctpTemplateCategory.getId() == 2L || ctpTemplateCategory.getId() == 19L
                    || ctpTemplateCategory.getId() == 20L || ctpTemplateCategory.getId() == 21L) {
                continue;
            }
            templateTreeVO = new TemplateTreeVo();
            templateTreeVO.setId(ctpTemplateCategory.getId());
            templateTreeVO.setName(ctpTemplateCategory.getName());
            templateTreeVO.setType("category");
            if (null == ctpTemplateCategory.getParentId()) {
                templateTreeVO.setpId(null);
            } else {
                //表单的插入到公共模板下面
                if (ctpTemplateCategory.getParentId() == 2L || 1L == ctpTemplateCategory.getParentId()) {
                    templateTreeVO.setpId(0L);
                } else {
                    templateTreeVO.setpId(ctpTemplateCategory.getParentId());
                }

            }
            listTreeVo.add(templateTreeVO);
        }
    }

    /**
     * 将CtpTemplate模板对象转换为树节点对象
     * @param showTempletes
     * @param listTreeVo
     * @throws BusinessException
     */
    private void transTemplate2TreeVo(List<CtpTemplate> showTempletes, List<TemplateTreeVo> listTreeVo)
            throws BusinessException {
        TemplateTreeVo templateTreeVO;
        Long caccountId = AppContext.getCurrentUser().getLoginAccount();
        for (CtpTemplate ctpTemplate : showTempletes) {
            templateTreeVO = new TemplateTreeVo();
            templateTreeVO.setId(ctpTemplate.getId());
            String shortName = "";
            if (null != ctpTemplate.getOrgAccountId() && !ctpTemplate.getOrgAccountId().equals(caccountId)) {
                V3xOrgAccount orgAccount = orgManager.getAccountById(ctpTemplate.getOrgAccountId());
                if (null != orgAccount) {
                    shortName = orgAccount.getShortName();
                }
                if (Strings.isNotBlank(shortName)) {
                    templateTreeVO.setName(ctpTemplate.getSubject() + "(" + shortName + ")");
                } else {
                    templateTreeVO.setName(ctpTemplate.getSubject());
                }
            } else {
                templateTreeVO.setName(ctpTemplate.getSubject());
            }
            templateTreeVO.setType(ctpTemplate.getType());
            // 表单显示在公共模板分类下
            if (Integer.valueOf(32).equals(ctpTemplate.getModuleType())) {
                templateTreeVO.setpId(300L);
                templateTreeVO.setIsInfo(true);
            } else {
                templateTreeVO.setpId(ctpTemplate.getCategoryId() == 2 ? 0l : ctpTemplate.getCategoryId());
                templateTreeVO.setIsEdoc(isEdoc(String.valueOf(ctpTemplate.getModuleType())));
            }
            templateTreeVO.setCategoryType(ctpTemplate.getModuleType());
            templateTreeVO.setWorkflowId(ctpTemplate.getWorkflowId());
            templateTreeVO.setSystem(ctpTemplate.isSystem());
            templateTreeVO.setBodyType(ctpTemplate.getBodyType());
            //设置浮动显示的title
            V3xOrgMember createrMember = orgManager.getMemberById(ctpTemplate.getMemberId());
            StringBuilder showTitle = new StringBuilder();
            if (null != createrMember) {
                if (Strings.isNotBlank(createrMember.getName())) {
                    //创建人
                    String creater = ResourceUtil.getString("collaboration.summary.createdBy") + ":";
                    if (null != ctpTemplate.getOrgAccountId() && !ctpTemplate.getOrgAccountId().equals(caccountId)) {

                        showTitle.append(creater + createrMember.getName()//显示单位管理员加简称
                                + "(" + shortName + ")" + "\r");
                    } else {
                        showTitle.append(creater + createrMember.getName() + "\r");
                    }
                }
                try {
                    if (!createrMember.getIsAdmin()) {
                        //部门
                        String departMent = ResourceUtil.getString("org.department.label") + ":";
                        showTitle.append(departMent + Functions.showDepartmentFullPath(createrMember.getOrgDepartmentId()) + "\r");
                    }
                } catch (Exception e) {
                    //showTitle.append("部门:"+" ");
                }
                try {
                    //岗位
                    String post = ResourceUtil.getString("org.post.label") + ":";
                    showTitle.append(post + orgManager.getPostById(createrMember.getOrgPostId()).getName());
                } catch (Exception e) {
                    //showTitle.append("岗位:"+" ");
                }
                templateTreeVO.setFullName(showTitle.toString());
            }
            templateTreeVO = setWendanId(templateTreeVO, ctpTemplate);
            String icon = getTemplateIcon(ctpTemplate);
            templateTreeVO.setIconSkin(icon);
            templateTreeVO.setFormAppId(ctpTemplate.getFormAppId());
            if (WFComponentUtil.isForm(templateTreeVO.getBodyType())) {
                templateTreeVO.setCAP4(capFormManager.isCAP4Form(templateTreeVO.getFormAppId()));
            }

            listTreeVo.add(templateTreeVO);
        }
    }

    private String getTemplateIcon(CtpTemplate ctpTemplate) {
        String icon = "";
        if (null == ctpTemplate.getType()) {
            return "";
        }
        switch (TemplateTypeEnums.getEnumByKey(ctpTemplate.getType())) {
            case workflow:
                icon = "flow";
                break;
            case text:
                icon = "format";
                break;
            case templete:
                icon = "edoc";
                break;
            case template:
                if (isGovdoc(ctpTemplate.getModuleType().toString())) {
                    icon = "edoc";
                } else if (String.valueOf(MainbodyType.FORM.getKey()).equals(ctpTemplate.getBodyType())) {
                    icon = "form_temp";
                } else {
                    icon = "freeCollaboration";
                }
                break;
        }

        //如果不是系统模板的表单模板那么
        if (ctpTemplate.isSystem() == null || ctpTemplate.isSystem() == false) {
            icon = "person_template_16";
        }
        return icon;
    }

    private TemplateTreeVo setCategory(String s) {
        TemplateTreeVo ttpersonlVO = null;
        ttpersonlVO = new TemplateTreeVo();
        // 表单和协同的构建根节点(pid为空的，则为顶层)
        ttpersonlVO.setId(Long.valueOf(s));
        if (s.equals(ModuleType.edoc.getValue()))
            ttpersonlVO.setName(ResourceUtil.getString("template.edoc.label"));
        if (s.equals(ModuleType.govdocSend.getValue()))
            ttpersonlVO.setName(ResourceUtil.getString("template.edocsend.label"));
        if (s.equals(ModuleType.govdocRec.getValue()))
            ttpersonlVO.setName(ResourceUtil.getString("template.edocrec.label"));
        if (s.equals(ModuleType.govdocSign.getValue()))
            ttpersonlVO.setName(ResourceUtil.getString("template.edocsign.label"));
        ttpersonlVO.setType("category");
        if (s.equals(ModuleType.edoc.getValue()))
            ttpersonlVO.setpId(null);
        else
            ttpersonlVO.setpId(Long.parseLong(ModuleType.edoc.getValue()));
        return ttpersonlVO;
    }

    private List<TemplateTreeVo> createCategoryParentTreeVO(List<TemplateTreeVo> treeVOs, String s, List<Long> treeIds) {
        List<TemplateTreeVo> returnTreeVos = new ArrayList<TemplateTreeVo>();
        Long categoryId = Long.valueOf(s);

        while (true) {
            CtpTemplateCategory category = templateCategoryManager.get(categoryId);
            if (category != null) {
                if (Long.valueOf(ModuleType.collaboration.getKey()).equals(category.getParentId())
                        || Long.valueOf(ModuleType.form.getKey()).equals(category.getParentId())) {
                    TemplateTreeVo treeVO = new TemplateTreeVo(0L, ResourceUtil.getString("template.public.label"), "category", null, "");
                    if (!treeIds.contains(treeVO.getId())) {
                        treeIds.add(treeVO.getId());
                        returnTreeVos.add(treeVO);
                    }
                    category.setParentId(0L);
                    TemplateTreeVo treeVOchrld = new TemplateTreeVo(category.getId(), category.getName(), "category", category.getParentId(), "");
                    if (!treeIds.contains(treeVOchrld.getId())) {
                        returnTreeVos.add(treeVOchrld);
                    }
                    break;
                }
                TemplateTreeVo treeVO = new TemplateTreeVo(category.getId(), category.getName(), "category", category.getParentId(), "");
                if (!treeIds.contains(treeVO.getId())) {
                    returnTreeVos.add(treeVO);
                }
                categoryId = category.getParentId();
            } else {
                break;
            }
        }

        return returnTreeVos;
    }

    //@CheckRoleAccess(roleTypes = { Role_NAME.PerformanceAdmin})  525权限问题
    public ModelAndView templateChooseMul(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        ModelAndView modelAndView = new ModelAndView("common/template/templateChooseM");
        String moduleType = request.getParameter("moduleType");

        String isMul = request.getParameter("isMul");//是否多选
        String accountId = request.getParameter("accountId");
        String scope = request.getParameter("scope");
        String reportId = request.getParameter("reportId");
        String searchType = request.getParameter("searchType");
        String excludeTemplateIds = request.getParameter("excludeTemplateIds");
        String templateTypes = request.getParameter("templateTypes");
        String isCanSelectCategory = request.getParameter("isCanSelectCategory");
        String canNotSelectRoot = request.getParameter("canNotSelectRoot");
        String showChildrenCategory = request.getParameter("showChildrenCategory");
        String showNoChildrenCategory = request.getParameter("showNoChildrenCategory");
        String showOldEdocTemplate = request.getParameter("showOldEdocTemplate");
        String memberId = request.getParameter("memberId");
        boolean isShowTemplate = (null == request.getParameter("isShowTemplate")) ? true : Boolean.valueOf(request.getParameter("isShowTemplate"));//是否显示具体的模板
        boolean isShowMyTemplate = Boolean.valueOf(request.getParameter("isShowMyTemplate"));
        boolean isShowTemplateRecentDeal = (null == request.getParameter("isShowTemplateRecentDeal")) ? false : Boolean.valueOf(request.getParameter("isShowTemplateRecentDeal"));
        boolean isAlwaysShowTemplateCommon = (null == request.getParameter("isAlwaysShowTemplateCommon")) ? false : Boolean.valueOf(request.getParameter("isAlwaysShowTemplateCommon"));
        boolean opeEdocRoot = (null == request.getParameter("opeEdocRoot")) ? true : Boolean.valueOf(request.getParameter("opeEdocRoot"));//是否默认展开公文根目录

        /*String spaceType  = request.getParameter("spaceType");
        String openFrom = request.getParameter("openFrom");
        if (("sentSection".equals(openFrom) || "doneSection".equals(openFrom)) && "m3mobile".equals(spaceType )) {
        	//移动端已发、已办栏目设置屏蔽公文有关 by wxj,并能取到最大范围的数据
        	moduleType = ModuleType.collaboration.getKey()+ "," + ModuleType.form.getKey();
        	if(!"sentSection".equals(openFrom)) {//已发只能查询到自己有权限的
        		scope = TemplateChooseScope.MaxScope.name();
        	}
        }*/

        Map<String, String> m = new HashMap<String, String>();
        m.put("moduleType", moduleType);
        m.put("scope", scope);
        m.put("reportId", reportId);
        m.put("searchType", searchType);
        m.put("excludeTemplateIds", excludeTemplateIds);
        m.put("templateTypes", templateTypes);
        m.put("memberId", memberId);
        m.put("showNoChildrenCategory", showNoChildrenCategory);
        m.put("isShowTemplateRecentDeal", String.valueOf(isShowTemplateRecentDeal));
        m.put("isAlwaysShowTemplateCommon", String.valueOf(isAlwaysShowTemplateCommon));
        m.put("showOldEdocTemplate", showOldEdocTemplate);

        List<TemplateTreeVo> vos = templateManager.getTemplateChooseTreeData(m);
        List<TemplateTreeVo> returnVos = new ArrayList<TemplateTreeVo>();

        if (!isShowTemplate) {
            //排除具体的模板信息，根据：type值：template、category
            for (TemplateTreeVo templateTree : vos) {
                if ("category".equals(templateTree.getType())) {
                    returnVos.add(templateTree);
                }
            }
        } else {
            returnVos = vos;
        }
        if (isShowMyTemplate) {//添加分类（个人模板）
            //一、添加个人模版的分类
            TemplateTreeVo myTemplate = new TemplateTreeVo();
            myTemplate.setId(TemplateCategoryConstant.personRoot.key());
            myTemplate.setName(ResourceUtil.getString("template.templatePub.personalTemplates"));//个人模板
            myTemplate.setpId(null);
            myTemplate.setType("category");
            myTemplate.setCombinId(String.valueOf(TemplateCategoryConstant.personRoot.key()));
            returnVos.add(myTemplate);
        }
        request.setAttribute("fftemplateTree", returnVos);
        Long accountIdLong = Strings.isBlank(accountId) ? AppContext.currentAccountId() : Long.valueOf(accountId);

        boolean isV5Member = AppContext.getCurrentUser().getExternalType() == OrgConstants.ExternalType.Inner.ordinal();
        if (!isV5Member) {
            accountIdLong = OrgHelper.getVJoinAllowAccount();
        }

        List<Long> categoryIds = new ArrayList<Long>();
        for (TemplateTreeVo templateTree : vos) {
            if ("category".equals(templateTree.getType())) {
                categoryIds.add(templateTree.getId());
            }
        }
        // 需要根据指定的类型查找分类
        if (Strings.isBlank(moduleType)) {
            modelAndView.addObject("categoryHTML", templateManager.categoryHTML(accountIdLong, true, "", categoryIds).toString());
        } else {
            List<String> types = Strings.newArrayList(moduleType.split(","));
            if (types.size() == 1 && (String.valueOf(ModuleType.edocSend.getKey()).equals(types.get(0))
                    || String.valueOf(ModuleType.edocRec.getKey()).equals(types.get(0))
                    || String.valueOf(ModuleType.edocSign.getKey()).equals(types.get(0)))) {

                modelAndView.addObject("categoryHTML", templateManager.categoryHTMLEdoc(types.get(0)).toString());

            } else {
                if (types.contains(String.valueOf(ModuleType.edoc.getKey()))
                        || types.contains(String.valueOf(ModuleType.govdoc.getKey()))
                        || types.contains(String.valueOf(ModuleType.govdocSend.getKey()))
                        || types.contains(String.valueOf(ModuleType.govdocRec.getKey()))
                        || types.contains(String.valueOf(ModuleType.govdocSign.getKey()))) {
                    //暂时屏蔽  categoryHTMLGovdoc这个方法合并代码没有合并上 需要大研发的同事修改
                    modelAndView.addObject("categoryHTML", templateManager.categoryHTML(accountIdLong, true, moduleType, categoryIds).toString());
                } else {
                    modelAndView.addObject("categoryHTML", templateManager.categoryHTML(accountIdLong, false, "", categoryIds).toString());
                }
            }
        }


        modelAndView.addObject("moduleType", moduleType);
        modelAndView.addObject("isMul", isMul);
        modelAndView.addObject("accountId", accountId);
        modelAndView.addObject("scope", scope);
        modelAndView.addObject("reportId", reportId);
        modelAndView.addObject("isCanSelectCategory", isCanSelectCategory);
        modelAndView.addObject("canNotSelectRoot", canNotSelectRoot);
        modelAndView.addObject("showChildrenCategory", showChildrenCategory);
        modelAndView.addObject("showNoChildrenCategory", showNoChildrenCategory);
        modelAndView.addObject("isShowTemplate", isShowTemplate);//是否能选择具体的模板
        modelAndView.addObject("isShowTemplateRecentDeal", isShowTemplateRecentDeal);//最近处理模板
        modelAndView.addObject("isAlwaysShowTemplateCommon", isAlwaysShowTemplateCommon);//默认显示公共
        modelAndView.addObject("showOldEdocTemplate", showOldEdocTemplate);//是否显示老公文
        modelAndView.addObject("opeEdocRoot", opeEdocRoot);//是否显示老公文

        return modelAndView;
    }

    public ModelAndView showDatetimeFormFields(HttpServletRequest request,
                                               HttpServletResponse response) throws Exception {

        ModelAndView modelAndView = new ModelAndView("common/template/dateFormField");
        String formAppId = request.getParameter("formAppId");

        /*	CAPFormBean fb = capFormManager.getForm(Long.valueOf(formAppId));  */

        Long formAppIdLong = Long.valueOf(formAppId);

        boolean isCap4 = capFormManager.isCAP4Form(formAppIdLong);

        List<String[]> datetimes = new ArrayList<String[]>();
        if (isCap4) {
            com.seeyon.cap4.form.bean.FormBean fb = formApi4Cap4.getEditingForm(formAppIdLong);
            List<com.seeyon.cap4.form.bean.FormFieldBean> fieldBeans = fb.getAllFieldBeans();
            if (Strings.isNotEmpty(fieldBeans)) {
                for (com.seeyon.cap4.form.bean.FormFieldBean field : fieldBeans) {
                    if (field.isMasterField() && (com.seeyon.cap4.form.bean.FormFieldComEnum.EXTEND_DATETIME.equals(field.getInputTypeEnum())
                            || com.seeyon.cap4.form.bean.FormFieldComEnum.EXTEND_DATE.equals(field.getInputTypeEnum()))) {
                        String[] date = new String[2];
                        date[0] = field.getDisplay();
                        date[1] = field.getName();
                        datetimes.add(date);
                    }
                }
            }
        } else {
            FormBean fb = formApi4Cap3.getEditingForm();
            List<FormFieldBean> fieldBeans = fb.getAllFieldBeans();
            if (Strings.isNotEmpty(fieldBeans)) {
                for (FormFieldBean field : fieldBeans) {
                    if (field.isMasterField() && (com.seeyon.ctp.form.bean.FormFieldComBean.FormFieldComEnum.EXTEND_DATETIME.equals(field.getInputTypeEnum())
                            || com.seeyon.ctp.form.bean.FormFieldComBean.FormFieldComEnum.EXTEND_DATE.equals(field.getInputTypeEnum()))) {
                        String[] date = new String[2];
                        date[0] = field.getDisplay();
                        date[1] = field.getName();
                        datetimes.add(date);
                    }
                }
            }
        }

        modelAndView.addObject("datetimes", datetimes);

        return modelAndView;
    }

    public ModelAndView templateProcessInstruction(HttpServletRequest request,
                                                   HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("apps/collaboration/detailIframe");
        String tId = request.getParameter("templateId");
        String objectId = request.getParameter("objectId");
        String moduleType = request.getParameter("moduleType");
        String needRead = request.getParameter("needRead");
        boolean templateDataShow = Strings.isBlank(objectId);
        if (Strings.isNotBlank(tId) && !"null".equals(tId) && !"undefined".equals(tId)) {
            CtpTemplate ctpTemplate = templateManager.getCtpTemplate(Long.valueOf(tId));
            if (ctpTemplate == null) {
                CtpTemplateHistory ctpTemplateHistory = templateManager.getCtpTemplateHistory(Long.valueOf(tId));
                if (ctpTemplateHistory != null) {
                    ctpTemplate = new CtpTemplate();
                    BeanUtils.convert(ctpTemplate, ctpTemplateHistory);
                    ctpTemplate.setSystem(ctpTemplateHistory.isSystem());
                }
            }
            if (null != ctpTemplate) {
                if (!ctpTemplate.isSystem() && null != ctpTemplate.getFormParentid()) {
                    CtpTemplate orgTem = templateManager.getCtpTemplate(ctpTemplate.getFormParentid());
                    if (null != orgTem) {
                        ctpTemplate = orgTem;
                    }
                }
                if (Strings.isBlank(moduleType)) {
                    Integer tmt = ctpTemplate.getModuleType();
                    if (tmt.equals(Integer.valueOf(1))) {
                        moduleType = "1";
                        if ("20".equals(ctpTemplate.getBodyType())) {
                            moduleType = "2";
                        }
                    } else if (tmt.equals(ApplicationCategoryEnum.info.getKey())) {
                        moduleType = "32";
                    } else {
                        moduleType = "4";
                    }
                }
                boolean ifFormFlag = String.valueOf(ModuleType.form.getKey()).equals(moduleType)
                        || isGovdoc(moduleType);
                if (Strings.isNotBlank(needRead)) {
                    if (Strings.isNotBlank(tId)) {
                        if (null != ctpTemplate) {
                            mav.addObject("template", ctpTemplate);
                            //基础信息
                            if (null != ctpTemplate.getCategoryId()) {
                                CtpTemplateCategory templateCategory = templateCategoryManager.get(ctpTemplate.getCategoryId());
                                if (templateCategory != null) {
                                    mav.addObject("templateCategoryName", templateCategory.getName());
                                }
                            }
                            Long formAppId = ctpTemplate.getFormAppId();
                            if (formAppId != null && ifFormFlag) {
                                if (ctpTemplate.getModuleType() == ApplicationCategoryEnum.info.getKey()) {
                                    GovFormBo bo = infoApi.getGovForm(formAppId);
                                    mav.addObject("formTemplateName", bo.getName());
                                } else {
                                    CAPFormBean form = capFormManager.getForm(formAppId);
                                    mav.addObject("formTemplateName", form.getFormName());
                                }
                            }
                            V3xOrgMember bm = orgManager.getMemberById(ctpTemplate.getMemberId());
                            String bmName = "";
                            if (bm != null) {
                                bmName = bm.getName();
                                if (bm.getIsAdmin()) {
                                    bmName = orgManager.getAccountById(bm.getOrgAccountId()).getName() + ResourceUtil.getString("template.unit.manager");
                                }
                            }
                            mav.addObject("belongPerson", bmName);
                            Map<String, Object> map = null;
                            String superviseStr = (String) ctpTemplate.getExtraAttr("superviseStr");
                            if (superviseStr == null) {
                                map = new HashMap<String, Object>();
                                CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(ctpTemplate.getId());
                                if (null != superviseDetail) {
                                    List<CtpSupervisor> supervisors = superviseManager.getSupervisors(superviseDetail.getId());
                                    String sids = "";
                                    map.put("supervisorNames", superviseDetail.getSupervisors());
                                    //督办角色
                                    List<CtpSuperviseTemplateRole> roleList = superviseManager.findRoleByTemplateId(ctpTemplate.getId());
                                    StringBuilder roles = new StringBuilder("");
                                    for (CtpSuperviseTemplateRole srole : roleList) {
                                        roles.append(srole.getRole()).append(",");
                                    }
                                    if (roles.length() > 0) {
                                        roles = new StringBuilder(roles.substring(0, roles.toString().length() - 1));
                                        mav.addObject("role", roles.toString());
                                        map.put("role", roles.toString());
                                    }
                                }
                            } else {
                                map = (Map<String, Object>) XMLCoder.decoder(superviseStr);
                            }
                            String showSupers = "";
                            if (map.get("role") != null && Strings.isNotBlank(map.get("role").toString())) {
                                String role = map.get("role").toString();
                                String[] roles = role.split(",");
                                for (String string : roles) {
                                    showSupers += (getRoleName(string) + "、");
                                }
                                showSupers = showSupers.substring(0, showSupers.length() - 1);
                            }
                            if (map.get("supervisorNames") != null && Strings.isNotBlank(map.get("supervisorNames").toString())) {
                                showSupers = ("".equals(showSupers) ? "" : (showSupers + "、")) + map.get("supervisorNames").toString();
                            }
                            mav.addObject("showSupervisors", showSupers);
                            EnumManager em = (EnumManager) AppContext.getBean("enumManagerNew");
                            if (moduleType.equals(String.valueOf(ModuleType.collaboration.getKey()))
                                    || moduleType.equals(String.valueOf(ModuleType.form.getKey()))) {
                                ColSummary summary = XMLCoder.decoder(ctpTemplate.getSummary(), ColSummary.class);
                                CtpEnumItem cei = em.getEnumItem(EnumNameEnum.collaboration_deadline, String.valueOf(summary.getDeadline()));
                                if (null != cei) {
                                    String enumLabel = ResourceUtil.getString(cei.getLabel());
                                    mav.addObject("deadlineLabel", enumLabel);
                                }
                            } else if (moduleType.equals(String.valueOf(ModuleType.info.getKey()))) {
                                Long deadline = infoApi.getTemplateDeadline(ctpTemplate);
                                CtpEnumItem cei = em.getEnumItem(EnumNameEnum.collaboration_deadline, String.valueOf(deadline));
                                if (null != cei) {
                                    String enumLabel = ResourceUtil.getString(cei.getLabel());
                                    mav.addObject("deadlineLabel", enumLabel);
                                }
                            } else {
                                ProcessInsHandler processInsHandler = getProcessInsHandlerMap().get(ApplicationCategoryEnum.edoc);
                                Long deadlineNumber = processInsHandler.getDeadlineNumber(ctpTemplate.getSummary());
                                CtpEnumItem cei = em.getEnumItem(EnumNameEnum.collaboration_deadline, String.valueOf(deadlineNumber));
                                if (null != cei) {
                                    String enumLabel = ResourceUtil.getString(cei.getLabel());
                                    mav.addObject("deadlineLabel", enumLabel);
                                }
                            }

                            if (null != ctpTemplate.getProcessLevel()) {
                                String enumShowName = templateManager.getEnumShowName("cap_process_leavel", ctpTemplate.getProcessLevel().toString());
                                mav.addObject("processLL", enumShowName);
                            }

                            Object auth = ctpTemplate.getExtraAttr("authList");
                            if (auth == null) {
                                //修改时读取授权信息
                                Integer mt = ctpTemplate.getModuleType();
                                if (null != mt && (mt.intValue() == 19 || mt.intValue() == 20 || mt.intValue() == 21)) {
                                    mt = 4;
                                }
                                ctpTemplate.putExtraAttr("authList", templateManager.getCtpTemplateAuths(ctpTemplate.getId(), mt));
                                auth = ctpTemplate.getExtraAttr("authList");
                            }
                            List<CtpTemplateAuth> authList = (List<CtpTemplateAuth>) auth;
                            StringBuilder ids = new StringBuilder("");
                            String names = "";
                            if (authList.size() > 0) {
                                for (CtpTemplateAuth moduleAuth : authList) {
                                    ids.append(moduleAuth.getAuthType()).append("|").append(moduleAuth.getAuthId()).append(",");
                                }
                                ids = new StringBuilder(ids.substring(0, ids.toString().length() - 1));
                                PageContext p = null;
                                names = Functions.showOrgEntities(ids.toString(), p);
                            }
                            mav.addObject("auth_txt", names);

                            if (null != ctpTemplate.getBelongOrg()) {
                                V3xOrgAccount accountById = orgManager.getAccountById(ctpTemplate.getBelongOrg());
                                if (null != accountById) {
                                    mav.addObject("belongOrgShow", accountById.getName());
                                } else {
                                    V3xOrgDepartment departmentById = orgManager.getDepartmentById(ctpTemplate.getBelongOrg());
                                    if (null != departmentById) {
                                        mav.addObject("belongOrgShow", departmentById.getName());
                                    }
                                }
                            }
                            ctpTemplate = templateManager.addOrgIntoTempalte(ctpTemplate);
                            if (Strings.isNotBlank(ctpTemplate.getCoreUseOrg())) {
                                String[] raciFillbackMessage = getRACIFillbackMessage(ctpTemplate.getCoreUseOrg());
                                mav.addObject("coreUseOrgShow", raciFillbackMessage[0]);
                            }
                            if (null != ctpTemplate.getPublishTime()) {
                                String publishTime = Datetimes.formatDatetimeWithoutSecond(ctpTemplate.getPublishTime());
                                mav.addObject("publishTime", publishTime);
                            }
                            if (null != ctpTemplate.getWorkflowId()) {
                                mav.addObject("siwfRule", wapi.getWorkflowRuleInfo("", ctpTemplate.getWorkflowId().toString()));
                            }
                            //RACI
                            if (Strings.isNotBlank(ctpTemplate.getResponsible())) {
                                if (!templateDataShow) {
                                    String[] raciFillbackMessage = getRACIFillbackMessage(ctpTemplate.getResponsible(),
                                            Long.valueOf(objectId), moduleType);
                                    mav.addObject("r4show", raciFillbackMessage[0]);
                                } else {
                                    String[] raciFillbackMessage = getRACIFillbackMessage(ctpTemplate.getResponsible());
                                    mav.addObject("r4show", raciFillbackMessage[0]);
                                }
                            }
                            if (Strings.isNotBlank(ctpTemplate.getAuditor())) {
                                if (!templateDataShow) {
                                    String[] raciFillbackMessage = getRACIFillbackMessage(ctpTemplate.getAuditor(),
                                            Long.valueOf(objectId), moduleType);
                                    mav.addObject("a4show", raciFillbackMessage[0]);
                                } else {
                                    String[] raciFillbackMessage = getRACIFillbackMessage(ctpTemplate.getAuditor());
                                    mav.addObject("a4show", raciFillbackMessage[0]);
                                }
                            }
                            if (Strings.isNotBlank(ctpTemplate.getConsultant())) {
                                if (!templateDataShow) {
                                    String[] raciFillbackMessage = getRACIFillbackMessage(ctpTemplate.getConsultant(),
                                            Long.valueOf(objectId), moduleType);
                                    mav.addObject("c4show", raciFillbackMessage[0]);
                                } else {
                                    String[] raciFillbackMessage = getRACIFillbackMessage(ctpTemplate.getConsultant());
                                    mav.addObject("c4show", raciFillbackMessage[0]);
                                }
                            }
                            if (Strings.isNotBlank(ctpTemplate.getInform())) {
                                if (!templateDataShow) {
                                    String[] raciFillbackMessage = getRACIFillbackMessage(ctpTemplate.getInform(),
                                            Long.valueOf(objectId), moduleType);
                                    mav.addObject("i4show", raciFillbackMessage[0]);
                                } else {
                                    String[] raciFillbackMessage = getRACIFillbackMessage(ctpTemplate.getInform());
                                    mav.addObject("i4show", raciFillbackMessage[0]);
                                }
                            }

                        }
                    }
                    //相关流程
                    if (ifFormFlag) {
                        List<Long> mainTemplateProcessIds = wapi.getMainTemplateProcessIdBySubTempalteProcessId(ctpTemplate.getWorkflowId());
                        if (Strings.isNotEmpty(mainTemplateProcessIds)) {
                            List<CtpTemplate> mainTemplates = templateManager.getCtpTemplateByWorkFlowIds(mainTemplateProcessIds);
                            mav.addObject("mainProcessList", mainTemplates);
                        }
                        List<Long> subTemplateProcessIds = wapi.getSubTemplateProcessIdByMainTempalteProcessId(ctpTemplate.getWorkflowId());
                        if (Strings.isNotEmpty(subTemplateProcessIds)) {
                            List<CtpTemplate> subTemplates = templateManager.getCtpTemplateByWorkFlowIds(subTemplateProcessIds);
                            mav.addObject("subProcessList", subTemplates);
                        }
                    }
                }
                List<NodeSimpleBO> nodeSimpleBOList = wapi.getProcessNodeSimpleBOsByProcessId(ctpTemplate.getWorkflowId());

                for (int a = 0; a < nodeSimpleBOList.size(); a++) {
                    NodeSimpleBO bo = nodeSimpleBOList.get(a);
                    if (Strings.isBlank(bo.getNodeDeadLine())) {
                        bo.setNodeDeadLine(ResourceUtil.getString("collaboration.project.nothing.label"));
                    } else {
                        EnumManager enumManager = getEnumManager();
                        if ("0".equals(bo.getNodeDeadLine())) {
                            bo.setNodeDeadLine(ResourceUtil.getString("collaboration.project.nothing.label"));
                        } else {
                            CtpEnumItem cei = enumManager.getEnumItem(EnumNameEnum.collaboration_deadline, bo.getNodeDeadLine().toString());
                            if (cei == null) {//无
                                bo.setNodeDeadLine(ResourceUtil.getString("collaboration.project.nothing.label"));
                            } else {
                                bo.setNodeDeadLine(ResourceUtil.getString(cei.getLabel()));
                            }

                        }
                    }
                    /** UE检查去掉是否超期
                     if(templateDataShow || bo.getId().equals("start")){//模板
                     bo.setIsOvertopTime("否");
                     }else{//实际流程
                     Map<String,Object> map = new HashMap<String,Object>();
                     map.put("objectId", Long.valueOf(objectId));
                     map.put("activityId", Long.valueOf(bo.getId()));
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
                     boolean isExpectedOvertime = ColUtil.checkAffairIsOverTime(affair, null);
                     if(isExpectedOvertime){
                     isOver = true;
                     break;
                     }
                     }
                     }
                     }
                     bo.setIsOvertopTime(isOver?"是":"否");

                     }
                     **/
                    if (Strings.isNotBlank(ctpTemplate.getAuditor())) {
                        if (ctpTemplate.getAuditor().indexOf("nodevouch") > -1 && "vouch".equals(bo.getPermissionId())
                                || ctpTemplate.getAuditor().indexOf("nodeqianfa") > -1 && "qianfa".equals(bo.getPermissionId())) {
                            bo.setExt(ResourceUtil.getString("template.list.operation.raci.a.js"));
                        }
                    }
                    if (Strings.isNotBlank(ctpTemplate.getInform())) {
                        if (ctpTemplate.getInform().indexOf("nodeinfo") > -1 &&
                                ("inform".equals(bo.getPermissionId()) || "zhihui".equals(bo.getPermissionId()))) {
                            bo.setExt(ResourceUtil.getString("template.list.operation.raci.i.js"));
                        }
                    }
                }
                mav.addObject("nodeSimpleBO", nodeSimpleBOList);
            }
        }
        mav.addObject("moduleType", moduleType);
        return mav;
    }

    //应用绑定Frame
    public ModelAndView templateBeanFormBindFrame(HttpServletRequest request,
                                                  HttpServletResponse response) throws BusinessException {
        ModelAndView mav = new ModelAndView("apps/collaboration/formBindFrame");
        boolean cap4Flag = Strings.isNotBlank(request.getParameter("cap4Flag")) && "1".equals(request.getParameter("cap4Flag"));
        if (!cap4Flag) {
            FormBean fb = capFormManager.getEditingForm();
            if (fb != null) {
                mav.addObject("formId", fb.getId());
            }
        } else {
            mav.addObject("formId", request.getParameter("formId"));
        }
        mav.addObject("hasWorkflowAdvanced", AppContext.hasPlugin("workflowAdvanced"));
        return mav;
    }

    //应用绑定的查询
    public ModelAndView templateBeanList(HttpServletRequest request,
                                         HttpServletResponse response) throws BusinessException {
        ModelAndView mav = new ModelAndView("apps/collaboration/formBindList");
        boolean cap4Flag = Strings.isNotBlank(request.getParameter("cap4Flag")) && "1".equals(request.getParameter("cap4Flag"));
        mav.addObject("cap4Flag", request.getParameter("cap4Flag"));

        FlipInfo ff = new FlipInfo();
        Long formAppId = null;
        if (cap4Flag) {

            Long formId = ReqUtil.getLong(request, "formId");
            formAppId = formId;
            com.seeyon.cap4.form.bean.FormBean cap4fb = formApi4Cap4.checkAndLoadForm2Session(formId, "");

            if (cap4fb.getBind().getExtraAttr("flowFormTemplatePGIdentify") == null) {
                cap4fb.getBind().putExtraAttr("flowFormTemplatePGIdentify", true);
            }


            if (cap4fb.getBind().getExtraAttr("batchId") == null) {
                cap4fb.getBind().putExtraAttr("batchId", UUIDLong.longUUID());
                Map query = new HashMap();
                query.put("contentTemplateId", String.valueOf(cap4fb.getId()));
                query.put("delete", false);
                ff = templateManager.getformBindList(null, query);
                mav.addObject("fbatchId", cap4fb.getBind().getExtraAttr("batchId"));
                cap4fb.getBind().setFlowTemplateList(ff.getData());

                //第一次打开应用绑定列表的时候才清理工作流草稿数据
                wapi.deleteJunkDataOfWorkflowTemplates(formId);

            } else {
                ff = new FlipInfo();
                sortWithModifyData(cap4fb.getBind().getFlowTemplateList());
                ff.setData(cap4fb.getBind().getFlowTemplateList());
                mav.addObject("fbatchId", cap4fb.getBind().getExtraAttr("batchId"));
            }
            List<TemplateBO> tVO = templateManager.covertTemplatePO2VO(ff.getData());
            ff.setData(tVO);
            request.setAttribute("ffformBindList", ff);
            com.seeyon.cap4.form.bean.FormAuthViewBean startAuth = cap4fb.getNewFormAuthViewBean();
            com.seeyon.cap4.form.bean.FormAuthViewBean nomorlAuth = cap4fb.getUpdateAndShowFormAuthViewBeans().get(0);
            mav.addObject("startOperation", startAuth.getId());
            mav.addObject("nomorlOperation", nomorlAuth.getId());
            mav.addObject("formBean", cap4fb);
            mav.addObject("redTemplete", cap4fb.hasRedTemplete());
        } else {


            FormBean fb = capFormManager.getEditingForm();

            if (fb.getBind().getExtraAttr("flowFormTemplatePGIdentify") == null) {
                fb.getBind().putExtraAttr("flowFormTemplatePGIdentify", true);
            }

            if (fb.getBind().getExtraAttr("batchId") == null) {
                fb.getBind().putExtraAttr("batchId", UUIDLong.longUUID());
                Map query = new HashMap();
                query.put("contentTemplateId", String.valueOf(fb.getId()));
                query.put("delete", false);
                ff = templateManager.getformBindList(null, query);
                mav.addObject("fbatchId", fb.getBind().getExtraAttr("batchId"));
                fb.getBind().setFlowTemplateList(ff.getData());

                //第一次打开应用绑定列表的时候才清理工作流草稿数据
                wapi.deleteJunkDataOfWorkflowTemplates(fb.getId());

            } else {
                ff = new FlipInfo();
                sortWithModifyData(fb.getBind().getFlowTemplateList());
                ff.setData(fb.getBind().getFlowTemplateList());
                mav.addObject("fbatchId", fb.getBind().getExtraAttr("batchId"));
            }
            List<TemplateBO> tVO = templateManager.covertTemplatePO2VO(ff.getData());
            ff.setData(tVO);
            request.setAttribute("ffformBindList", ff);
            FormAuthViewBean startAuth = fb.getNewFormAuthViewBean();
            FormAuthViewBean nomorlAuth = fb.getUpdateAndShowFormAuthViewBeans().get(0);
            mav.addObject("startOperation", startAuth.getId());
            mav.addObject("nomorlOperation", nomorlAuth.getId());
            mav.addObject("formBean", fb);
            mav.addObject("redTemplete", fb.hasRedTemplete());
            formAppId = fb.getId();

            //微服务问题：更新缓存
            try {
                capFormManager.addEditForm(fb);
            } catch (CloneNotSupportedException e) {
                LOG.error("", e);
            }
        }

        /**
         * 加一段兼容逻辑，开发过程中数据库中太多错误数据了。正式发版以后，可以删除这段逻辑。
         * 1、删除wf_process_template中既不在Ctp_template又不在CTP_TEMPLATE_HISTORY中的数据，这部分数据是开发过程中错误的，正式环境不应该有这种数据
         * 2、只设置逻辑删除，不做物理删除、防止误删。
         */
		/*if(ff!=null && Strings.isNotEmpty(ff.getData())) {
			StringBuilder sb  = new StringBuilder();
			sb.append(" update wf_process_templete  set state = 3 ");
			sb.append(" where ");
			sb.append(" appId = ").append(formAppId);
			sb.append(" AND ID  not IN (select workflow_id from ctp_template  where workflow_id is not null ) ");
			sb.append(" AND ID not in (select workflow_id from ctp_template_history  where workflow_id is not null ) ");

			JDBCAgent jdbc = new JDBCAgent();
			try {
				jdbc.execute(sb.toString());
			} catch (SQLException e) {
		         LOG.error("", e);
			}finally {
				jdbc.close();
			}
		}*/


        PermissionVO vo = permissionManager.getDefaultPermissionByConfigCategory("col_flow_perm_policy", AppContext.currentAccountId());
        mav.addObject("defaultPolicyId", vo.getName());
        mav.addObject("defaultPolicyName", vo.getLabel());
        List<ProjectBO> projectSummaries = new ArrayList<ProjectBO>();
        if (AppContext.hasPlugin("project")) {
            projectSummaries = projectApi.findProjectsByAccountId(AppContext.currentAccountId());
        }
        mav.addObject("project", projectSummaries);
        mav.addObject("productId", SystemProperties.getInstance().getProperty("system.ProductId"));
        return mav;
    }

    private void sortWithModifyData(List<CtpTemplate> t) {
        Collections.sort(t, new Comparator<CtpTemplate>() {
            @Override
            public int compare(CtpTemplate t1, CtpTemplate t2) {
                //附件列表需倒序
                Date d1 = t1.getModifyDate();
                Date d2 = t2.getModifyDate();
                int res = 0;
                if (d1 != null && d2 != null) {
                    res = d1.compareTo(d2);
                } else if (d1 == null && d2 != null) {
                    res = -1;
                } else if (d1 != null) {
                    res = 1;
                }
                return res == 0 ? 0 : res > 0 ? -1 : 1;
            }

        });
    }

    public ModelAndView editTemplatePage(HttpServletRequest request, HttpServletResponse response) throws BusinessException, JSONException {

        //原来是查询的ctpTemplate 现在查询最新的历史记录
        ModelAndView mav = new ModelAndView("apps/collaboration/processIns");
        String defId = request.getParameter("defId");

        boolean workflowAdvancedFlag = false;
        workflowAdvancedFlag = AppContext.hasPlugin("workflowAdvanced");//是否存在流程高级插件
        String fbatchId = request.getParameter("fbatchId");
        String templateId = request.getParameter("templateId");
        String moduleType = request.getParameter("moduleType");
        String govdocModuleType = request.getParameter("govdocModuleType");
        mav.addObject("moduleType", moduleType);
        mav.addObject("hasAIPlugin", AppContext.hasPlugin("ai"));
        mav.addObject("hasCDPPlugin", CDPAgent.isEnabled());
        mav.addObject("workflowAdvancedFlag", workflowAdvancedFlag);
        String isNew = request.getParameter("isNew");
        if ("false".equals(isNew)) {
            CtpTemplate ctpTemplate = templateManager.getCtpTemplate(Long.valueOf(templateId));
            if (ctpTemplate == null) {
                isNew = "true";
            }
        }
        if ("true".equals(isNew)) {
            mav.addObject("editFlag", true);
        }

        mav.addObject("isNew", Strings.escapeJavascript(isNew));//是否是新建
        String templateNameShow = ResourceUtil.getString("template.page.processIns.tn.js");
        if (Strings.isNotBlank(moduleType) && "1".equals(moduleType)) {//协同模板

            String _subject = request.getParameter("defaultSubject");
            mav.addObject("defaultSubject", _subject);
            if (Strings.isNotBlank(request.getParameter(_subject))) {
                templateNameShow = _subject;
            }
            if (Strings.isNotBlank(request.getParameter("categoryName"))) {
                //空格的Ascii 值为160  trim不掉。
                mav.addObject("categoryName", request.getParameter("categoryName").replaceAll("\\u00A0", ""));
            }
            mav.addObject("deadlineLabel", request.getParameter("deadlineLabel"));
            mav.addObject("auth_txt", request.getParameter("auth_txt"));
            mav.addObject("showSupervisors", request.getParameter("showSupervisors"));
            mav.addObject("templateId", templateId);
            if (null != V3xShareMap.getReserved("templateData" + AppContext.currentUserId() + templateId)) {
                CtpTemplate c = (CtpTemplate) V3xShareMap.getReserved("templateData" + AppContext.currentUserId() + templateId);
                mav = bulidPageData(mav, c);
                templateNameShow = c.getSubject();
            } else {//修改模板第一次的时候进来 走该分支
                CtpTemplate ctpTemplate = templateManager.getCtpTemplate(Long.valueOf(templateId));
                if (null != ctpTemplate) {
                    ctpTemplate = templateManager.addOrgIntoTempalte(ctpTemplate);
                    mav = bulidPageData(mav, ctpTemplate);
                    templateNameShow = ctpTemplate.getSubject();
                }
            }
            mav.addObject("appName", ApplicationCategoryEnum.collaboration.name());
            mav = commonPermissAttribute(mav, moduleType);
            mav.addObject("templateNameShow", templateNameShow);
        } else if (Strings.isNotBlank(moduleType) &&
                ("19".equals(moduleType) || "20".equals(moduleType) || "21".equals(moduleType))) {
            moduleType = govdocModuleType;
            String openWinId = request.getParameter("openWinId");

            String _subject = request.getParameter("defaultSubject");
            mav.addObject("defaultSubject", _subject);
            if (Strings.isNotBlank(request.getParameter(_subject))) {
                templateNameShow = _subject;
            }
            mav.addObject("deadlineLabel", request.getParameter("deadlineLabel"));
            mav.addObject("categoryName", ResourceUtil.getString("templete.category.type." + moduleType));
            mav.addObject("auth_txt", request.getParameter("auth_txt"));
            mav.addObject("showSupervisors", request.getParameter("showSupervisors"));
            mav.addObject("templateId", templateId);
            mav.addObject("wendanId", request.getParameter("wendanId"));
            mav.addObject("openWinId", openWinId);
            if (null != V3xShareMap.getReserved("templateData" + AppContext.currentUserId() + openWinId)) {
                CtpTemplate c = (CtpTemplate) V3xShareMap.getReserved("templateData" + AppContext.currentUserId() + openWinId);
                mav = bulidPageData(mav, c);
                templateNameShow = c.getSubject();
            } else {//修改模板第一次的时候进来 走该分支
                if (Strings.isNotBlank(templateId)) {
                    CtpTemplate ctpTemplate = templateManager.getCtpTemplate(Long.valueOf(templateId));
                    if (null != ctpTemplate) {
                        ctpTemplate = templateManager.addOrgIntoTempalte(ctpTemplate);
                        mav = bulidPageData(mav, ctpTemplate);
                        templateNameShow = ctpTemplate.getSubject();
                    }
                }
            }
            mav.addObject("appName", ApplicationCategoryEnum.valueOf(Integer.valueOf(moduleType)).name());
            mav = commonPermissAttribute(mav, moduleType);
            mav.addObject("templateNameShow", templateNameShow);
        } else if (Strings.isNotBlank(moduleType) && "32".equals(moduleType)) {
            String openWinId = request.getParameter("openWinId");

            String _subject = request.getParameter("defaultSubject");
            mav.addObject("defaultSubject", _subject);
            if (Strings.isNotBlank(request.getParameter(_subject))) {
                templateNameShow = _subject;
            }
            mav.addObject("deadlineLabel", request.getParameter("deadlineLabel"));
            mav.addObject("categoryName", ResourceUtil.getString("templete.category.type." + moduleType));
            mav.addObject("auth_txt", request.getParameter("auth_txt"));
            mav.addObject("showSupervisors", request.getParameter("showSupervisors"));
            mav.addObject("templateId", templateId);
            mav.addObject("wendanId", request.getParameter("wendanId"));
            mav.addObject("openWinId", openWinId);
            if (null != V3xShareMap.getReserved("templateData" + AppContext.currentUserId() + templateId)) {
                CtpTemplate c = (CtpTemplate) V3xShareMap.getReserved("templateData" + AppContext.currentUserId() + templateId);
                mav = bulidPageData(mav, c);
                templateNameShow = c.getSubject();
            } else {//修改模板第一次的时候进来 走该分支
                if (Strings.isNotBlank(templateId)) {
                    CtpTemplate ctpTemplate = templateManager.getCtpTemplate(Long.valueOf(templateId));
                    if (null != ctpTemplate) {
                        ctpTemplate = templateManager.addOrgIntoTempalte(ctpTemplate);
                        mav = bulidPageData(mav, ctpTemplate);
                        templateNameShow = ctpTemplate.getSubject();
                    }
                }
            }
            mav.addObject("appName", ApplicationCategoryEnum.valueOf(Integer.valueOf(moduleType)).name());
            mav = commonPermissAttribute(mav, moduleType);
            mav.addObject("templateNameShow", templateNameShow);
        } else {
            //表单模板
            mav.addObject("moduleType", "2");
            boolean cap4Flag = Strings.isNotBlank(request.getParameter("cap4Flag")) && "1".equals(request.getParameter("cap4Flag"));
            mav.addObject("templateNameShow", templateNameShow);
            if (cap4Flag) {
                mav.addObject("cap4Flag", "1");
                mav = templatePageCAP4(mav, defId, fbatchId, templateId);
            } else {
                mav = templatePageCAP3(mav, defId, fbatchId, templateId);
            }
            //新公文以表单的方式展现
            if (!Strings.isBlank(govdocModuleType) && govdocModuleType.length() < 4) {
                mav.addObject("appName", ApplicationCategoryEnum.valueOf(Integer.valueOf(govdocModuleType)).name());
            } else {
                mav.addObject("appName", ApplicationCategoryEnum.collaboration.name());
            }
        }

        List<String[]> deadlines = new ArrayList<String[]>();
        List<CtpEnumItem> collaborationDeadlines = enumManagerNew.getEnumItems(EnumNameEnum.collaboration_deadline, true);
        if (Strings.isNotEmpty(collaborationDeadlines)) {
            for (CtpEnumItem item : collaborationDeadlines) {
                String[] deadline = new String[2];
                deadline[0] = item.getValue();
                deadline[1] = ResourceUtil.getString(item.getLabel());
                deadlines.add(deadline);
            }
        }
        mav.addObject("deadlines", deadlines);

        //是否存在关联项目 relationProject
        boolean relationProjectFlag = false;
        if (AppContext.hasPlugin("project")) {
            relationProjectFlag = true;
        }
        mav.addObject("relationProjectFlag", relationProjectFlag);
        boolean templateCanUse = false;
        if (Strings.isNotBlank(templateId)) {
            templateCanUse = templateManager.templateCanUse(Long.valueOf(templateId));
        }
        mav.addObject("templateCanUse", templateCanUse);

        if (edocApi != null) {
            //获取新公文的正文类型
            mav.addObject("contentTypeList", edocApi.getGovdocContentType(ModuleType.govdoc));
            //取出所有正文套红模版
            List<GovdocTemplateBO> docTemplates = edocApi.findTemplateByType(TempleteType.content.getKey());
            mav.addObject("docTemplates", docTemplates);
        }
        mav = setBelongOrgDefaultValue(mav);
        return mav;
    }

    private ModelAndView setBelongOrgDefaultValue(ModelAndView mav) throws BusinessException {
        User cUser = AppContext.getCurrentUser();
        if (null == mav.getModelMap().get("belongOrgFB")) {
            if (cUser.isAdmin()) {
                Long accountId = cUser.getAccountId();
                mav.addObject("belongOrgFB", "Account|" + accountId);
                V3xOrgAccount account = orgManager.getAccountById(accountId);
                if (null != account) {
                    mav.addObject("belongOrgShow", account.getName());
                }
            } else {
                Long departmentId = 0l;
                if (AppContext.currentAccountId() == cUser.getAccountId().longValue()) {
                    departmentId = cUser.getDepartmentId();
                } else {
                    List<V3xOrgDepartment> departments = orgManager.getDepartmentsByUser(cUser.getId());
                    for (V3xOrgDepartment department : departments) {
                        if (department.getOrgAccountId().equals(AppContext.currentAccountId())) {
                            departmentId = department.getId();
                            break;
                        }
                    }
                }
                mav.addObject("belongOrgFB", "Department|" + departmentId);
                V3xOrgDepartment departmentById = orgManager.getDepartmentById(departmentId);
                if (null != departmentById) {
                    mav.addObject("belongOrgShow", departmentById.getName());
                }
            }
        }
        mav.addObject("defaultUser", cUser.getName());
        return mav;
    }

    private void processMonitorInfo(ModelAndView mav, String templateId) {
        try {
            if (CDPAgent.isEnabled() && AppContext.hasPlugin("ai")) {
                List processMonitorList = aiApi.getByTemplateId(Long.valueOf(templateId));
                if (processMonitorList != null && processMonitorList.size() > 0) {
                    String jsonStr = JSONUtil.toJSONString(processMonitorList.get(0));
                    Map<String, String> boMap = JSONUtil.parseJSONString(jsonStr, Map.class);
                    mav.addObject("isSendMsg", boMap.get("isSendMsg"));
                    mav.addObject("monitorArray", JSONUtil.toJSONString(processMonitorList));
                }
            }
        } catch (BusinessException e) {
            LOG.error("查询流程监控条件异常：", e);
        }
    }

    public ModelAndView bulidPageData(ModelAndView mav, CtpTemplate c) throws BusinessException {
        //高级信息
        mav.addObject("template", c);
        mav = CommonOperation(mav, c);
        if (null != c.getBelongOrg()) {
            V3xOrgAccount accountById = orgManager.getAccountById(c.getBelongOrg());
            if (null != accountById) {
                mav.addObject("belongOrgFB", "Account|" + c.getBelongOrg());
                mav.addObject("belongOrgShow", accountById.getName());
            } else {
                mav.addObject("belongOrgFB", "Department|" + c.getBelongOrg());
                V3xOrgDepartment departmentById = orgManager.getDepartmentById(c.getBelongOrg());
                if (null != departmentById) {
                    mav.addObject("belongOrgShow", departmentById.getName());
                }
            }
        }
        //RACI数据回填
        if (Strings.isNotBlank(c.getResponsible())) {
            String[] raciFillbackMessage = getRACIFillbackMessage(c.getResponsible());
            mav.addObject("r4show", raciFillbackMessage[0]);
            mav.addObject("r4db", raciFillbackMessage[1]);
            mav.addObject("r4fb", raciFillbackMessage[2]);
        }
        if (Strings.isNotBlank(c.getAuditor())) {
            String[] raciFillbackMessage = getRACIFillbackMessage(c.getAuditor());
            mav.addObject("a4show", raciFillbackMessage[0]);
            mav.addObject("a4db", raciFillbackMessage[1]);
            mav.addObject("a4fb", raciFillbackMessage[2]);
        }
        if (Strings.isNotBlank(c.getConsultant())) {
            String[] raciFillbackMessage = getRACIFillbackMessage(c.getConsultant());
            mav.addObject("c4show", raciFillbackMessage[0]);
            mav.addObject("c4db", raciFillbackMessage[1]);
            mav.addObject("c4fb", raciFillbackMessage[2]);
        }
        if (Strings.isNotBlank(c.getInform())) {
            String[] raciFillbackMessage = getRACIFillbackMessage(c.getInform());
            mav.addObject("i4show", raciFillbackMessage[0]);
            mav.addObject("i4db", raciFillbackMessage[1]);
            mav.addObject("i4fb", raciFillbackMessage[2]);
        }
        if (Strings.isNotBlank(c.getCoreUseOrg())) {
            String[] raciFillbackMessage = getRACIFillbackMessage(c.getCoreUseOrg());
            mav.addObject("coreUseOrg4show", raciFillbackMessage[0]);
            mav.addObject("coreUseOrg4db", raciFillbackMessage[1]);
            mav.addObject("coreUseOrg4fb", raciFillbackMessage[2]);
        }
        //流程信息
        if (c.getId() == null) {
            mav.addObject("siwfRule", c.getExtraAttr("wfRule"));
            //流程信息
            mav.addObject("processXml", c.getExtraAttr("processXml"));
            mav.addObject("process_id", c.getExtraAttr("process_id"));
            mav.addObject("process_event", c.getExtraAttr("process_event"));
        } else {//修改模板的时候第一次进来
            if (null != c.getWorkflowId()) {//排除格式模板
                mav.addObject("siwfRule", wapi.getWorkflowRuleInfo("", c.getWorkflowId().toString()));
            }
        }
        if (null != c.getMemberId()) {
            V3xOrgMember bm = orgManager.getMemberById(c.getMemberId());
            String bmName = bm.getName();
            if (bm.getIsAdmin()) {
                bmName = orgManager.getAccountById(bm.getOrgAccountId()).getName() + "单位管理员";
            }

            mav.addObject("createMemberName", bmName);
        }
        return mav;
    }

    private ModelAndView templatePageCAP3(ModelAndView mav, String defId, String fbatchId, String templateId) throws BusinessException {
        FormBean fb = capFormManager.getEditingForm();
        boolean hasAIPlugin = AppContext.hasPlugin("ai");
        boolean hasCDPPlugin = CDPAgent.isEnabled();
        if (hasAIPlugin && hasCDPPlugin) {
            List<Map<String, String>> beanList = new ArrayList<Map<String, String>>();
            List<FormFieldBean> fieldBeanList = fb.getMasterTableBean().getFields();
            for (FormFieldBean bean : fieldBeanList) {
                if ("DECIMAL".equals(bean.getFieldType())) {
                    Map<String, String> beanJSON = new HashMap<String, String>();
                    beanJSON.put("tableName", bean.getOwnerTableName());
                    beanJSON.put("formName", fb.getFormName());
                    beanJSON.put("fieldName", bean.getName());
                    beanJSON.put("fieldNameDisplay", "（主表）" + bean.getDisplay());
                    beanList.add(beanJSON);
                }
            }
            mav.addObject("decimalFieldList", JSONUtil.toJSONString(beanList));
        }
        mav.addObject("defaultSubject", fb.getFormName());

        Boolean isNewTemplate = true;//告诉界面是否是新建的模板
        CtpTemplate template = null;
        if (Strings.isNotBlank(templateId)) {
            template = fb.getBind().getFlowTemplate(Long.valueOf(templateId));
        }

        if (Strings.isNotBlank(templateId) && template != null) {//修改模板

            // 恶心的把数据结构当缓存用
            Long orginalTemplateWorkFlowId = (Long) template.getExtraAttr("orginalTemplateWorkFlowId");
            if (orginalTemplateWorkFlowId == null || Long.valueOf(-1).equals(orginalTemplateWorkFlowId)) { //为空再添加，确保一次完成的流程编辑保存操作，只添加一次，否则重复添加多次还是有问题。
                template.putExtraAttr("orginalTemplateWorkFlowId", template.getWorkflowId() == null ? -1 : template.getWorkflowId());
            }

            isNewTemplate = false;
            String templateHistoryId = "";

            CtpTemplateHistory history = (CtpTemplateHistory) template.getExtraAttr("templateHistory");
            if (history == null) {
                List<CtpTemplateHistory> historys = templateApproveManager.getCtpTemplateHistoryByTemplateId(templateId);
                history = Strings.isEmpty(historys) ? null : historys.get(0);
            }

            //如果都没有那么就是历史数据,那么立即保存一套历史记录
            if (history == null) {
                history = templateManager.cloneAndSaveTemplateToHistory(template);
            }

            //直接去获取持久化数据
            if (history != null) {

                collaborationApi.deepClone(template, history);

                templateHistoryId = String.valueOf(history.getId());

            }

            mav.addObject("templateHistoryId", templateHistoryId);

            //拷贝还是走原来的流程-只是现在添加一个templateHistoryId 为历史版本的id
            mav.addObject("templateId", Long.valueOf(templateId));

            //组装页面信息用于修改回填到页面
            mav.addObject("defaultSubject", template.getSubject());
            if (hasAIPlugin) {
                if (hasCDPPlugin) {
                    String processMonitorJSON = (String) template.getExtraAttr(FormConstant.FLOW_TEMPLATE_EXT_PROCESS_MONITOR);
                    String processMonitorInput = (String) template.getExtraAttr(FormConstant.FLOW_TEMPLATE_EXT_MONITOR_CAN_SEND_MSG);
                    //加载流程监控配置信息
                    if (Strings.isNotBlank(processMonitorJSON)) {
                        mav.addObject("isSendMsg", "1".equals(processMonitorInput) ? true : false);
                        mav.addObject("monitorArray", processMonitorJSON);
                    } else {
                        processMonitorInfo(mav, templateId);
                    }
                }
                setAIDealCondition(mav, template);
            }
            //表单授权
            Object auth = template.getExtraAttr("authList");
            if (auth == null) {
                //修改时读取授权信息
                template.putExtraAttr("authList", templateManager.getCtpTemplateAuths(template.getId(), template.getModuleType()));
                auth = template.getExtraAttr("authList");
            }
            List<CtpTemplateAuth> authList = (List<CtpTemplateAuth>) auth;
            StringBuilder ids = new StringBuilder("");
            String names = "";
            if (authList.size() > 0) {
                for (CtpTemplateAuth moduleAuth : authList) {
                    ids.append(moduleAuth.getAuthType()).append("|").append(moduleAuth.getAuthId()).append(",");
                }
                ids = new StringBuilder(ids.substring(0, ids.toString().length() - 1));
                PageContext p = null;
                names = Functions.showOrgEntities(ids.toString(), p);
            }
            mav.addObject("auth", ids.toString());
            mav.addObject("auth_txt", names);
            //关联表单授权
            Object authRelationObj = template.getExtraAttr("relationAuthList");
            if (authRelationObj == null) {
                template.putExtraAttr("relationAuthList", capFormManager.getCtpTemplateRelationAuths(template.getId()));//修改时读取授权信息
                authRelationObj = template.getExtraAttr("relationAuthList");
            }
            List<CtpTemplateRelationAuth> authRelationList = (List<CtpTemplateRelationAuth>) authRelationObj;
            StringBuilder text = new StringBuilder();
            StringBuilder value = new StringBuilder();
            if (authRelationList.size() > 0) {
                CtpTemplateRelationAuth relationAuth = null;
                for (int i = 0; i < authRelationList.size(); i++) {
                    relationAuth = authRelationList.get(i);
                    String userType = SelectPersonOperation.getTypeByTypeId(relationAuth.getAuthType());
                    String tempName;
                    if (relationAuth.getAuthType() == 7 || relationAuth.getAuthType() == 8) {
                        tempName = FormUtil.getShowMemNameByIds(userType + "|" + relationAuth.getAuthValue(), fb);
                    } else {
                        tempName = SelectPersonOperation.getNameByTypeIdAndUserId(relationAuth.getAuthType().intValue(), Long.parseLong(relationAuth.getAuthValue()));
                    }
                    if (Strings.isNotBlank(tempName)) {
                        text.append(tempName).append("、");
                    }
                    value.append(userType).append("|").append(relationAuth.getAuthValue()).append(",");
                }
            }
            String value4Show = value.toString();
            String text4Show = text.toString();
            if (Strings.isNotBlank(value4Show)) {
                value4Show = value4Show.substring(0, value4Show.length() - 1);
            }
            if (Strings.isNotBlank(text4Show)) {
                text4Show = text4Show.substring(0, text4Show.length() - 1);
            }
            mav.addObject("authRelation", value4Show);
            mav.addObject("authRelation_txt", text4Show);

            if (null != template.getBelongOrg()) {
                V3xOrgAccount accountById = orgManager.getAccountById(template.getBelongOrg());
                if (null != accountById) {
                    mav.addObject("belongOrgFB", "Account|" + template.getBelongOrg());
                    mav.addObject("belongOrgShow", accountById.getName());
                } else {
                    mav.addObject("belongOrgFB", "Department|" + template.getBelongOrg());
                    V3xOrgDepartment departmentById = orgManager.getDepartmentById(template.getBelongOrg());
                    if (null != departmentById) {
                        mav.addObject("belongOrgShow", departmentById.getName());
                    }
                }
            }
            //RACI数据回填
            template = templateManager.addOrgIntoTempalte(template);
            if (Strings.isNotBlank(template.getResponsible())) {
                String[] raciFillbackMessage = getRACIFillbackMessage(template.getResponsible());
                mav.addObject("r4show", raciFillbackMessage[0]);
                mav.addObject("r4db", raciFillbackMessage[1]);
                mav.addObject("r4fb", raciFillbackMessage[2]);
            }
            if (Strings.isNotBlank(template.getAuditor())) {
                String[] raciFillbackMessage = getRACIFillbackMessage(template.getAuditor());
                mav.addObject("a4show", raciFillbackMessage[0]);
                mav.addObject("a4db", raciFillbackMessage[1]);
                mav.addObject("a4fb", raciFillbackMessage[2]);
            }
            if (Strings.isNotBlank(template.getConsultant())) {
                String[] raciFillbackMessage = getRACIFillbackMessage(template.getConsultant());
                mav.addObject("c4show", raciFillbackMessage[0]);
                mav.addObject("c4db", raciFillbackMessage[1]);
                mav.addObject("c4fb", raciFillbackMessage[2]);
            }
            if (Strings.isNotBlank(template.getInform())) {
                String[] raciFillbackMessage = getRACIFillbackMessage(template.getInform());
                mav.addObject("i4show", raciFillbackMessage[0]);
                mav.addObject("i4db", raciFillbackMessage[1]);
                mav.addObject("i4fb", raciFillbackMessage[2]);
            }
            if (Strings.isNotBlank(template.getCoreUseOrg())) {
                String[] raciFillbackMessage = getRACIFillbackMessage(template.getCoreUseOrg());
                mav.addObject("coreUseOrg4show", raciFillbackMessage[0]);
                mav.addObject("coreUseOrg4db", raciFillbackMessage[1]);
                mav.addObject("coreUseOrg4fb", raciFillbackMessage[2]);
            }

            //督办信息
            Map<String, Object> map = null;
            String superviseStr = (String) template.getExtraAttr("superviseStr");
            if (superviseStr == null) {
                map = new HashMap<String, Object>();
                //回填督办信息到页面
                CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(template.getId());
                if (null != superviseDetail) {
                    List<CtpSupervisor> supervisors = superviseManager.getSupervisors(superviseDetail.getId());
                    String supids = "";
                    mav.addObject("detailId", getLongString(superviseDetail.getId()));
                    mav.addObject("awakeDate", getLongString(superviseDetail.getTemplateDateTerminal()));
                    mav.addObject("title", superviseDetail.getTitle());
                    mav.addObject("supervisorNames", superviseDetail.getSupervisors());
                    map.put("supervisorNames", superviseDetail.getSupervisors());
                    mav.addObject("templateDateTerminal", getLongString(superviseDetail.getTemplateDateTerminal()));

                    String snames = "";
                    V3xOrgMember member;
                    for (CtpSupervisor ctps : supervisors) {
                        supids += ctps.getSupervisorId() + ",";
                        member = this.orgManager.getMemberById(ctps.getSupervisorId());
                        if (member != null) {
                            snames += (FormUtil.getOrgEntityName(member, orgManager) + "、");
                        }
                    }
                    if (supids.length() > 0) {
                        supids = supids.substring(0, supids.length() - 1);
                        mav.addObject("supervisorIds", supids);
                    }
                    if (snames.length() > 0) {
                        snames = snames.substring(0, snames.length() - 1);
                        mav.addObject("supervisorNames2", snames);
                    }
                    //督办角色
                    List<CtpSuperviseTemplateRole> roleList = superviseManager.findRoleByTemplateId(template.getId());
                    StringBuilder roles = new StringBuilder("");
                    for (CtpSuperviseTemplateRole srole : roleList) {
                        roles.append(srole.getRole()).append(",");
                    }
                    if (roles.length() > 0) {
                        roles = new StringBuilder(roles.substring(0, roles.toString().length() - 1));
                        mav.addObject("role", roles.toString());
                        map.put("role", roles.toString());
                    }
                }
            } else {
                map = (Map<String, Object>) XMLCoder.decoder(superviseStr);
                mav.addObject("supervisorIds", map.get("supervisorIds"));
                mav.addObject("detailId", map.get("detailId"));
                mav.addObject("supervisorNames", map.get("supervisorNames"));
                mav.addObject("title", map.get("title"));
                mav.addObject("templateDateTerminal", map.get("templateDateTerminal"));
            }
            String showSupers = "";
            if (map.get("role") != null && Strings.isNotBlank(map.get("role").toString())) {
                String role = map.get("role").toString();
                String[] roles = role.split(",");
                for (String string : roles) {
                    showSupers += (getRoleName(string) + "、");
                }
                showSupers = showSupers.substring(0, showSupers.length() - 1);
                mav.addObject("role", role);
            }
            if (map.get("supervisorNames") != null && Strings.isNotBlank(map.get("supervisorNames").toString())) {
                showSupers = ("".equals(showSupers) ? "" : (showSupers + "、")) + map.get("supervisorNames").toString();
            }
            mav.addObject("showSupervisors", showSupers);


            //附件信息

            String attListJSON = "";
            List<Attachment> cacheAttachments = (List<Attachment>) template.getExtraAttr("attachmentList");
            if (cacheAttachments != null) {
                attListJSON = attachmentManager.getAttListJSON(cacheAttachments);
            } else {
                attListJSON = attachmentManager.getAttListJSON(template.getId(), template.getId());
            }
            mav.addObject("attListJSON", attListJSON);


            ColSummary temSummary = new ColSummary();
            if (fb.getGovDocFormType() == FormType.govDocSendForm.getKey() || fb.getGovDocFormType() == FormType.govDocReceiveForm.getKey()
                    || fb.getGovDocFormType() == FormType.govDocSignForm.getKey() || fb.getGovDocFormType() == FormType.govDocExchangeForm.getKey()) {
                temSummary = (ColSummary) edocApi.getColSummaryByEdocSummary(template.getSummary());
            } else {
                temSummary = XMLCoder.decoder(template.getSummary(), ColSummary.class);
            }
            //预归档相关数据回填
            String fullPath = "";
            if (AppContext.hasPlugin("doc") && null != temSummary.getArchiveId()) {
                String archiveName = docApi.getDocResourceName(temSummary.getArchiveId());
                if (Strings.isNotBlank(archiveName)) {
                    mav.addObject("archive_name", archiveName);
                    Object obj = temSummary.getExtraAttr("archiverFormid");
                    if (obj != null) {
                        String archiverFormid = (String) obj;
                        if (Strings.isNotBlank(archiverFormid)) {
                            String[] arc = archiverFormid.split("\\|");
                            for (String a : arc) {
                                if (Strings.isNotBlank(a)) {
                                    String[] viewAuth = a.split("\\.");
                                    mav.addObject("view_" + viewAuth[0], viewAuth[0]);
                                    mav.addObject("auth_" + viewAuth[0], viewAuth[1]);
                                }
                            }
                            mav.addObject("archiverFormid", archiverFormid);
                        }
                    }

                } else {
                    mav.addObject("archive_Id", "");
                }

                DocResourceBO resourceBO = docApi.getDocResource(temSummary.getArchiveId());
                if (null != resourceBO) {
                    fullPath = docApi.getPhysicalPath(resourceBO.getLogicalPath(), "\\", false, 0);
                }

            }
            String archiveField = String.valueOf(temSummary.getExtraAttr("archiveField"));
            String archiveIsCreate = String.valueOf(temSummary.getExtraAttr("archiveIsCreate"));
            String archiveText = fb.hasRedTemplete() ? String.valueOf(temSummary.getExtraAttr("archiveText")) : "";
            String archiveTextName = fb.hasRedTemplete() ? String.valueOf(temSummary.getExtraAttr("archiveTextName")) : "";
            String archiveKeyword = String.valueOf(temSummary.getExtraAttr("archiveKeyword"));
            String archiveAll = String.valueOf(temSummary.getExtraAttr("archiveAll"));
            if (StringUtil.checkNull(archiveKeyword)) {
                archiveKeyword = "";
            }
            if (!StringUtil.checkNull(archiveField)) {
                mav.addObject("archiveFieldName", archiveField);
                FormFieldBean field = fb.getFieldBeanByName(archiveField);
                mav.addObject("archiveFieldDisplay", field.getDisplay());
                fullPath = fullPath + "\\" + "{" + field.getDisplay() + "}";
            }
            mav.addObject("fullPath", fullPath);
            if (!StringUtil.checkNull(archiveIsCreate)) {
                mav.addObject("archiveIsCreate", archiveIsCreate);
            } else {
                mav.addObject("archiveIsCreate", "false");
            }
                /*if (!StringUtil.checkNull(archiveForm)) {
                    map.put("archiveForm", archiveForm);
                }*/
            mav.addObject("archiveText", archiveText);
            mav.addObject("archiveTextName", archiveTextName);
            mav.addObject("archiveKeyword", archiveKeyword);
            mav.addObject("archiveAll", archiveAll);


            if (null != temSummary.getAttachmentArchiveId()) {
                mav = attachmentArchiveInfo(temSummary, mav);
            }

            //--自动发起数据回填开始
            String cycleState = String.valueOf(temSummary.getExtraAttr("cycleState"));
            cycleState = StringUtil.checkNull(cycleState) ? "0" : cycleState;
            mav.addObject("cycleState", cycleState);
            if ("1".equals(cycleState)) {
                String cycleSender = String.valueOf(temSummary.getExtraAttr("cycleSender"));
                if (!StringUtil.checkNull(cycleSender)) {
                    PageContext pageContext = null;
                    String cycleSenderName = Functions.showOrgEntities(cycleSender, pageContext);
                    mav.addObject("cycleSender", cycleSender);
                    mav.addObject("cycleSender_txt", cycleSenderName);

                    mav.addObject("cycleStartDate", String.valueOf(temSummary.getExtraAttr("cycleStartDate")));
                    mav.addObject("cycleEndDate", String.valueOf(temSummary.getExtraAttr("cycleEndDate")));
                    mav.addObject("cycleType", String.valueOf(temSummary.getExtraAttr("cycleType")));
                    mav.addObject("cycleMonth", String.valueOf(temSummary.getExtraAttr("cycleMonth")));
                    mav.addObject("cycleOrder", String.valueOf(temSummary.getExtraAttr("cycleOrder")));
                    mav.addObject("cycleDay", String.valueOf(temSummary.getExtraAttr("cycleDay")));
                    mav.addObject("cycleWeek", String.valueOf(temSummary.getExtraAttr("cycleWeek")));
                    mav.addObject("cycleHour", String.valueOf(temSummary.getExtraAttr("cycleHour")));
                }
            }

            List<MessageRuleVO> searchMessageRuleData = searchMessageRuleData(temSummary.getMessageRuleId());
            mav.addObject("messageRuleList", searchMessageRuleData);
            mav.addObject("messageRuleId", temSummary.getMessageRuleId());

            //--自动发起数据回填结束
            mav.addObject("summary", temSummary);

            //解析流程期限表单字段
            parseDeadlineFormFields4CAP3(mav, fb, temSummary);

            mav.addObject("template", template);
            mav = CommonOperation(mav, template);
            mav.addObject("updateFlag", "1");
            V3xOrgMember bm = orgManager.getMemberById(template.getMemberId());
            String bmName = bm.getName();
            if (bm.getIsAdmin()) {
                bmName = orgManager.getAccountById(bm.getOrgAccountId()).getName() + "单位管理员";
            }
            mav.addObject("createMemberName", bmName);
            EnumManager em = (EnumManager) AppContext.getBean("enumManagerNew");
        	/*CtpEnumItem cei = em.getEnumItem(EnumNameEnum.collaboration_deadline,String.valueOf(temSummary.getDeadline()));
            String enumLabel = ResourceUtil.getString(cei.getLabel());
            mav.addObject("deadlineLabel", enumLabel);*/
            //流程规则说明
            mav.addObject("siwfRule", wapi.getWorkflowRuleInfo("", template.getWorkflowId().toString()));

            //合并处理设置
            boolean canAnyDealMerge = canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE, temSummary);
            boolean canPreDealMerge = canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE, temSummary);
            boolean canStartMerge = canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.START_MERGE, temSummary);
            mav.addObject("canAnyDealMerge", canAnyDealMerge);
            mav.addObject("canPreDealMerge", canPreDealMerge);
            mav.addObject("canStartMerge", canStartMerge);
            mav.addObject("templateNameShow", template.getSubject());
            mav.addObject("orginalTemplateWorkFlowId", template.getExtraAttr("orginalTemplateWorkFlowId"));
        } else {
            mav.addObject("templateNameShow", fb.getFormName());
            mav.addObject("processId", UUIDLong.longUUID());
        }
        mav.addObject("isNewTemplate", isNewTemplate);
        //关联项目
        List<ProjectBO> projectSummaries = new ArrayList<ProjectBO>();
        if (AppContext.hasPlugin("project")) {
            projectSummaries = projectApi.findProjectsByAccountId(AppContext.currentAccountId());
        }
        mav.addObject("project", projectSummaries);

        //视图列表
        mav.addObject("pcViews", fb.getFormViewList());

        //预归档到 显示明细 需要用到 formbean对象
        mav.addObject("formBean", fb);

        String configCategory = "col_flow_perm_policy";
        if (fb.getGovDocFormType() == FormType.govDocSendForm.getKey()) {
            configCategory = EnumNameEnum.edoc_new_send_permission_policy.name();
        } else if (fb.getGovDocFormType() == FormType.govDocReceiveForm.getKey()) {
            configCategory = EnumNameEnum.edoc_new_rec_permission_policy.name();
        } else if (fb.getGovDocFormType() == FormType.govDocSignForm.getKey()) {
            configCategory = EnumNameEnum.edoc_new_qianbao_permission_policy.name();
        } else if (fb.getGovDocFormType() == FormType.govDocExchangeForm.getKey()) {
            configCategory = EnumNameEnum.edoc_new_change_permission_policy.name();
        }
        PermissionVO vo = permissionManager.getDefaultPermissionByConfigCategory(configCategory, AppContext.currentAccountId());
        mav.addObject("defaultPolicyId", vo.getName());
        mav.addObject("defaultPolicyName", vo.getLabel());

        WorkflowFormDataMapManager formDataManager = wapi.getWorkflowFormDataMapManager("form");
        String startDefualtRightId = formDataManager.getStartDefualtRightId(fb.getId());
        String normalDefualtRightId = formDataManager.getNormalDefualtRightId(fb.getId());

        mav.addObject("startDefualtRightId", startDefualtRightId);
        mav.addObject("normalDefualtRightId", normalDefualtRightId);

        mav.addObject("defId", defId);
        mav.addObject("fbatchId", fbatchId);
        mav.addObject("categoryName", tempateCategoryName(fb.getCategoryId()));
        mav.addObject("formTitle", fb.getFormName());
        return mav;
    }

    private void setAIDealCondition(ModelAndView mav, CtpTemplate template) {
        String autoDealConditionVal = (String) template.getExtraAttr(FormConstant.FLOW_TEMPLATE_EXT_AI_PROCESSING_CONDITION);
        if (Strings.isNotBlank(autoDealConditionVal)) {
            mav.addObject("autoDealConditionVal", autoDealConditionVal);
        } else {
            try {
                mav.addObject("autoDealConditionVal", aiApi.getProcessingConditionByTemplateId(template.getId()));
            } catch (BusinessException e) {
                LOG.error("查询流程智能处理设置条件异常", e);
            }
        }
    }

    private ModelAndView attachmentArchiveInfo(ColSummary temSummary, ModelAndView mav) throws BusinessException {
        mav.addObject("attachmentArchiveId", temSummary.getAttachmentArchiveId());
        mav.addObject("archiveAttachment", null == temSummary.getAttachmentArchiveId() ? "false" : "true");
        String attachmentArchiveName = "";
        if (temSummary.getAttachmentArchiveId() != null && docApi != null) {
            DocResourceBO docResourceBO = docApi.getDocResource(temSummary.getAttachmentArchiveId());
            if (docResourceBO != null) {
                attachmentArchiveName = docApi.getPhysicalPath(docResourceBO.getLogicalPath(), "\\", false, 0);
            }
        }
        mav.addObject("attachmentArchiveName", attachmentArchiveName);//附件归档的完整目录
        return mav;
    }

    private String tempateCategoryName(Long categoryId) {
        CtpTemplateCategory categorybyId = templateManager.getCategorybyId(categoryId);
        if (null != categorybyId) {
            return categorybyId.getName();
        }
        return "";
    }

    //表单显示 --old 显示的是ctpTemplate 现在显示历史记录
    private ModelAndView templatePageCAP4(ModelAndView mav, String defId, String fbatchId, String templateId) throws BusinessException {

        com.seeyon.cap4.form.bean.FormBean cap4fb = formApi4Cap4.checkAndLoadForm2Session(Long.valueOf(defId), "");
        boolean hasAIPlugin = AppContext.hasPlugin("ai");
        boolean hasCDPPlugin = CDPAgent.isEnabled();
        if (hasAIPlugin && hasCDPPlugin) {//有AI插件才获取表单数据
            List<com.seeyon.cap4.form.bean.FormFieldBean> fieldBeanList = cap4fb.getAllFieldBeans();
            List<Map<String, String>> beanList = new ArrayList<Map<String, String>>();
            for (com.seeyon.cap4.form.bean.FormFieldBean bean : fieldBeanList) {
                if ("DECIMAL".equals(bean.getFieldType())) {
                    Map<String, String> beanJSON = new HashMap<String, String>();
                    beanJSON.put("tableName", bean.getOwnerTableName());
                    beanJSON.put("formName", cap4fb.getFormName());
                    beanJSON.put("fieldName", bean.getName());
                    beanJSON.put("fieldNameDisplay", "（主表）" + bean.getDisplay());
                    beanList.add(beanJSON);
                }
            }
            mav.addObject("decimalFieldList", JSONUtil.toJSONString(beanList));
        }
        mav.addObject("defaultSubject", cap4fb.getFormName());

        Boolean isNewTemplate = true;//告诉界面是否是新建的模板
        CtpTemplate template = null;
        if (Strings.isNotBlank(templateId)) {
            template = cap4fb.getBind().getFlowTemplate(Long.valueOf(templateId));
            //针对缓存删除后无法找到的问题，需要重新查询数据库
            if (template == null) {
                template = templateManager.getCtpTemplate(Long.valueOf(templateId));

                if (template != null) {

                    // 恶心的把数据结构当缓存用
                    Long orginalTemplateWorkFlowId = (Long) template.getExtraAttr("orginalTemplateWorkFlowId");
                    if (orginalTemplateWorkFlowId == null || Long.valueOf(-1).equals(orginalTemplateWorkFlowId)) { //为空再添加，确保一次完成的流程编辑保存操作，只添加一次，否则重复添加多次还是有问题。
                        template.putExtraAttr("orginalTemplateWorkFlowId", template.getWorkflowId() == null ? -1 : template.getWorkflowId());
                    }
                }
            }
        }

        if (template != null) {//修改模板
            isNewTemplate = false;
            if (templateManager.getCtpTemplate(Long.valueOf(templateId)) == null) {
                isNewTemplate = true;
            }
            String templateHistoryId = "";

            // 好恶心的代码呀， 把结构数据当缓存用
            CtpTemplateHistory history = (CtpTemplateHistory) template.getExtraAttr("templateHistory");


            if (history == null) {
                List<CtpTemplateHistory> historys = templateApproveManager.getCtpTemplateHistoryByTemplateId(templateId);
                history = Strings.isEmpty(historys) ? null : historys.get(0);
            }

            //如果都没有那么就是历史数据,那么立即保存一套历史记录
            if (history == null) {
                history = templateManager.cloneAndSaveTemplateToHistory(template);
            }

            //直接去获取持久化数据
            if (history != null) {

                collaborationApi.deepClone(template, history);

                templateHistoryId = String.valueOf(history.getId());

            }
            //最新的历史版本是审核中 设置为不可编辑界面
            if (isNewTemplate) {
                mav.addObject("editFlag", true);
            } else {
                if ("1".equals(String.valueOf(history.getSubstate()))) {
                    mav.addObject("editFlag", false);
                } else {
                    mav.addObject("editFlag", true);
                }
            }


            mav.addObject("templateHistoryId", templateHistoryId);
            mav.addObject("templateId", Long.valueOf(templateId));
            //拷贝还是走原来的流程-只是现在添加一个templateHistoryId 为历史版本的id
//					cap4fb.getBind().getFlowTemplate(Long.valueOf(templateId));
            //组装页面信息用于修改回填到页面
            mav.addObject("defaultSubject", template.getSubject());
            if (hasAIPlugin) {//有AI插件才获取表单数据
                if (hasCDPPlugin) {
                    String processMonitorJSON = (String) template.getExtraAttr(FormConstant.FLOW_TEMPLATE_EXT_PROCESS_MONITOR);
                    //加载流程监控配置信息
                    if (Strings.isNotBlank(processMonitorJSON)) {
                        mav.addObject("monitorArray", processMonitorJSON);
                    } else {
                        processMonitorInfo(mav, templateId);
                    }
                }
                setAIDealCondition(mav, template);
            }
            //表单授权
            Object auth = template.getExtraAttr("authList");
            if (auth == null) {
                //修改时读取授权信息
                template.putExtraAttr("authList", templateManager.getCtpTemplateAuths(template.getId(), template.getModuleType()));
                auth = template.getExtraAttr("authList");
            }
            List<CtpTemplateAuth> authList = (List<CtpTemplateAuth>) auth;
            StringBuilder ids = new StringBuilder("");
            String names = "";
            if (authList.size() > 0) {
                for (CtpTemplateAuth moduleAuth : authList) {
                    ids.append(moduleAuth.getAuthType()).append("|").append(moduleAuth.getAuthId()).append(",");
                }
                ids = new StringBuilder(ids.substring(0, ids.toString().length() - 1));
                PageContext p = null;
                names = Functions.showOrgEntities(ids.toString(), p);
            }
            mav.addObject("auth", ids.toString());
            mav.addObject("auth_txt", names);

            if (null != template.getBelongOrg()) {
                V3xOrgAccount accountById = orgManager.getAccountById(template.getBelongOrg());
                if (null != accountById) {
                    mav.addObject("belongOrgFB", "Account|" + template.getBelongOrg());
                    mav.addObject("belongOrgShow", accountById.getName());
                } else {
                    mav.addObject("belongOrgFB", "Department|" + template.getBelongOrg());
                    V3xOrgDepartment departmentById = orgManager.getDepartmentById(template.getBelongOrg());
                    if (null != departmentById) {
                        mav.addObject("belongOrgShow", departmentById.getName());
                    }
                }
            }
            //RACI数据回填
            template = templateManager.addOrgIntoTempalte(template);
            if (Strings.isNotBlank(template.getResponsible())) {
                String[] raciFillbackMessage = getRACIFillbackMessage(template.getResponsible());
                mav.addObject("r4show", raciFillbackMessage[0]);
                mav.addObject("r4db", raciFillbackMessage[1]);
                mav.addObject("r4fb", raciFillbackMessage[2]);
            }
            if (Strings.isNotBlank(template.getAuditor())) {
                String[] raciFillbackMessage = getRACIFillbackMessage(template.getAuditor());
                mav.addObject("a4show", raciFillbackMessage[0]);
                mav.addObject("a4db", raciFillbackMessage[1]);
                mav.addObject("a4fb", raciFillbackMessage[2]);
            }
            if (Strings.isNotBlank(template.getConsultant())) {
                String[] raciFillbackMessage = getRACIFillbackMessage(template.getConsultant());
                mav.addObject("c4show", raciFillbackMessage[0]);
                mav.addObject("c4db", raciFillbackMessage[1]);
                mav.addObject("c4fb", raciFillbackMessage[2]);
            }
            if (Strings.isNotBlank(template.getInform())) {
                String[] raciFillbackMessage = getRACIFillbackMessage(template.getInform());
                mav.addObject("i4show", raciFillbackMessage[0]);
                mav.addObject("i4db", raciFillbackMessage[1]);
                mav.addObject("i4fb", raciFillbackMessage[2]);
            }
            if (Strings.isNotBlank(template.getCoreUseOrg())) {
                String[] raciFillbackMessage = getRACIFillbackMessage(template.getCoreUseOrg());
                mav.addObject("coreUseOrg4show", raciFillbackMessage[0]);
                mav.addObject("coreUseOrg4db", raciFillbackMessage[1]);
                mav.addObject("coreUseOrg4fb", raciFillbackMessage[2]);
            }
            //督办信息
            Map<String, Object> map = null;
            String superviseStr = (String) template.getExtraAttr("superviseStr");
            if (superviseStr == null) {
                map = new HashMap<String, Object>();
                //回填督办信息到页面
                CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(template.getId());
                if (null != superviseDetail) {
                    List<CtpSupervisor> supervisors = superviseManager.getSupervisors(superviseDetail.getId());
                    String supids = "";
                    mav.addObject("detailId", getLongString(superviseDetail.getId()));
                    mav.addObject("awakeDate", getLongString(superviseDetail.getTemplateDateTerminal()));
                    mav.addObject("title", superviseDetail.getTitle());
                    mav.addObject("supervisorNames", superviseDetail.getSupervisors());
                    map.put("supervisorNames", superviseDetail.getSupervisors());
                    mav.addObject("templateDateTerminal", getLongString(superviseDetail.getTemplateDateTerminal()));

                    String snames = "";
                    V3xOrgMember member;
                    for (CtpSupervisor ctps : supervisors) {
                        supids += ctps.getSupervisorId() + ",";
                        member = this.orgManager.getMemberById(ctps.getSupervisorId());
                        if (member != null) {
                            snames += (FormUtil.getOrgEntityName(member, orgManager) + "、");
                        }
                    }
                    if (supids.length() > 0) {
                        supids = supids.substring(0, supids.length() - 1);
                        mav.addObject("supervisorIds", supids);
                    }
                    if (snames.length() > 0) {
                        snames = snames.substring(0, snames.length() - 1);
                        mav.addObject("supervisorNames2", snames);
                    }
                    //督办角色
                    List<CtpSuperviseTemplateRole> roleList = superviseManager.findRoleByTemplateId(template.getId());
                    StringBuilder roles = new StringBuilder("");
                    for (CtpSuperviseTemplateRole srole : roleList) {
                        roles.append(srole.getRole()).append(",");
                    }
                    if (roles.length() > 0) {
                        roles = new StringBuilder(roles.substring(0, roles.toString().length() - 1));
                        mav.addObject("role", roles.toString());
                        map.put("role", roles.toString());
                    }
                }
            } else {
                map = (Map<String, Object>) XMLCoder.decoder(superviseStr);
                mav.addObject("supervisorIds", map.get("supervisorIds"));
                mav.addObject("detailId", map.get("detailId"));
                mav.addObject("supervisorNames", map.get("supervisorNames"));
                mav.addObject("title", map.get("title"));
                mav.addObject("templateDateTerminal", map.get("templateDateTerminal"));
            }
            String showSupers = "";
            if (map.get("role") != null && Strings.isNotBlank(map.get("role").toString())) {
                String role = map.get("role").toString();
                String[] roles = role.split(",");
                for (String string : roles) {
                    showSupers += (getRoleName(string) + "、");
                }
                showSupers = showSupers.substring(0, showSupers.length() - 1);
                mav.addObject("role", role);
            }
            if (map.get("supervisorNames") != null && Strings.isNotBlank(map.get("supervisorNames").toString())) {
                showSupers = ("".equals(showSupers) ? "" : (showSupers + "、")) + map.get("supervisorNames").toString();
            }
            mav.addObject("showSupervisors", showSupers);


            //附件信息
            //附件信息

            String attListJSON = "";
            List<Attachment> cacheAttachments = (List<Attachment>) template.getExtraAttr("attachmentList");
            if (cacheAttachments != null) {
                attListJSON = attachmentManager.getAttListJSON(cacheAttachments);
            } else {
                attListJSON = attachmentManager.getAttListJSON(template.getId(), template.getId());
            }
            mav.addObject("attListJSON", attListJSON);

            mav.addObject("attListJSON", attListJSON);
            ColSummary temSummary = XMLCoder.decoder(template.getSummary(), ColSummary.class);
            //预归档相关数据回填
            String fullPath = "";
            if (null != temSummary.getArchiveId() && docApi != null) {

                String archiveName = docApi.getDocResourceName(temSummary.getArchiveId());
                if (Strings.isNotBlank(archiveName)) {
                    mav.addObject("archive_name", archiveName);
                    Object obj = temSummary.getExtraAttr("archiverFormid");
                    if (obj != null) {
                        String archiverFormid = (String) obj;
                        if (Strings.isNotBlank(archiverFormid)) {
                            String[] arc = archiverFormid.split("[|_]");
                            for (String a : arc) {
                                if (Strings.isNotBlank(a)) {
                                    String[] viewAuth = a.split("\\.");
                                    mav.addObject("view_" + viewAuth[0], viewAuth[0]);
                                    mav.addObject("auth_" + viewAuth[0], viewAuth[1]);
                                }
                            }
                            mav.addObject("archiverFormid", archiverFormid);
                        }
                    }

                } else {
                    mav.addObject("archive_Id", "");
                }

                DocResourceBO resourceBO = docApi.getDocResource(temSummary.getArchiveId());
                if (null != resourceBO) {
                    fullPath = docApi.getPhysicalPath(resourceBO.getLogicalPath(), "\\", false, 0);
                }

            }

            String archiveField = String.valueOf(temSummary.getExtraAttr("archiveField"));
            String archiveIsCreate = String.valueOf(temSummary.getExtraAttr("archiveIsCreate"));
            String archiveText = cap4fb.hasRedTemplete() ? String.valueOf(temSummary.getExtraAttr("archiveText")) : "";
            String archiveTextName = cap4fb.hasRedTemplete() ? String.valueOf(temSummary.getExtraAttr("archiveTextName")) : "";
            String multipleArchiveTextName = cap4fb.hasRedTemplete() ? String.valueOf(temSummary.getExtraAttr("multipleArchiveTextName")) : "";
            String archiveAll = String.valueOf(temSummary.getExtraAttr("archiveAll"));
            String archiveKeyword = String.valueOf(temSummary.getExtraAttr("archiveKeyword"));
            if (StringUtil.checkNull(archiveKeyword)) {
                archiveKeyword = "";
            }
            if (!StringUtil.checkNull(archiveField)) {
                mav.addObject("archiveFieldName", archiveField);
                com.seeyon.cap4.form.bean.FormFieldBean field = cap4fb.getFieldBeanByName(archiveField);
                mav.addObject("archiveFieldDisplay", field.getDisplay());
                fullPath = fullPath + "\\" + "{" + field.getDisplay() + "}";
            }
            mav.addObject("fullPath", fullPath);
            if (!StringUtil.checkNull(archiveIsCreate)) {
                mav.addObject("archiveIsCreate", archiveIsCreate);
            } else {
                mav.addObject("archiveIsCreate", "false");
            }
                /*if (!StringUtil.checkNull(archiveForm)) {
                    map.put("archiveForm", archiveForm);
                }*/
            mav.addObject("archiveText", archiveText);
            mav.addObject("archiveTextName", archiveTextName);
            mav.addObject("multipleArchiveTextName", multipleArchiveTextName);
            mav.addObject("archiveAll", archiveAll);
            mav.addObject("archiveKeyword", archiveKeyword);


            if (null != temSummary.getAttachmentArchiveId()) {
                mav = attachmentArchiveInfo(temSummary, mav);
            }
            //--自动发起数据回填开始
            String cycleState = String.valueOf(temSummary.getExtraAttr("cycleState"));
            cycleState = StringUtil.checkNull(cycleState) ? "0" : cycleState;
            mav.addObject("cycleState", cycleState);
            if ("1".equals(cycleState)) {
                String cycleSender = String.valueOf(temSummary.getExtraAttr("cycleSender"));
                if (!StringUtil.checkNull(cycleSender)) {
                    PageContext pageContext = null;
                    String cycleSenderName = Functions.showOrgEntities(cycleSender, pageContext);
                    mav.addObject("cycleSender", cycleSender);
                    mav.addObject("cycleSender_txt", cycleSenderName);

                    mav.addObject("cycleStartDate", String.valueOf(temSummary.getExtraAttr("cycleStartDate")));
                    mav.addObject("cycleEndDate", String.valueOf(temSummary.getExtraAttr("cycleEndDate")));
                    mav.addObject("cycleType", String.valueOf(temSummary.getExtraAttr("cycleType")));
                    mav.addObject("cycleMonth", String.valueOf(temSummary.getExtraAttr("cycleMonth")));
                    mav.addObject("cycleOrder", String.valueOf(temSummary.getExtraAttr("cycleOrder")));
                    mav.addObject("cycleDay", String.valueOf(temSummary.getExtraAttr("cycleDay")));
                    mav.addObject("cycleWeek", String.valueOf(temSummary.getExtraAttr("cycleWeek")));
                    mav.addObject("cycleHour", String.valueOf(temSummary.getExtraAttr("cycleHour")));
                }
            }
            //--自动发起数据回填结束
            List<MessageRuleVO> searchMessageRuleData = searchMessageRuleData(temSummary.getMessageRuleId());
            mav.addObject("messageRuleList", searchMessageRuleData);
            mav.addObject("messageRuleId", temSummary.getMessageRuleId());
            mav.addObject("summary", temSummary);

            //解析流程期限表单字段
            parseDeadlineFormFields4CAP4(mav, cap4fb, temSummary);

            mav.addObject("template", template);
            mav = CommonOperation(mav, template);
            mav.addObject("updateFlag", "1");
            V3xOrgMember bm = orgManager.getMemberById(template.getMemberId());
            String bmName = bm.getName();
            if (bm.getIsAdmin()) {
                bmName = orgManager.getAccountById(bm.getOrgAccountId()).getName() + "单位管理员";
            }
            mav.addObject("createMemberName", bmName);
            //流程规则说明
            EnumManager em = (EnumManager) AppContext.getBean("enumManagerNew");
        	/*CtpEnumItem cei = em.getEnumItem(EnumNameEnum.collaboration_deadline,String.valueOf(temSummary.getDeadline()));
            String enumLabel = ResourceUtil.getString(cei.getLabel());
            mav.addObject("deadlineLabel", enumLabel);*/
            mav.addObject("siwfRule", wapi.getWorkflowRuleInfo("", template.getWorkflowId().toString()));

            //合并处理设置
            boolean canAnyDealMerge = canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE, temSummary);
            boolean canPreDealMerge = canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE, temSummary);
            boolean canStartMerge = canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.START_MERGE, temSummary);
            mav.addObject("canAnyDealMerge", canAnyDealMerge);
            mav.addObject("canPreDealMerge", canPreDealMerge);
            mav.addObject("canStartMerge", canStartMerge);
            mav.addObject("templateNameShow", template.getSubject());
        } else {
            mav.addObject("templateNameShow", cap4fb.getFormName());
        }
        mav.addObject("isNewTemplate", isNewTemplate);
        //关联项目
        List<ProjectBO> projectSummaries = new ArrayList<ProjectBO>();
        if (AppContext.hasPlugin("project")) {
            projectSummaries = projectApi.findProjectsByAccountId(AppContext.currentAccountId());
        }
        mav.addObject("project", projectSummaries);


        List<FormViewBean> views = cap4fb.getFormViewList();
        List<FormViewBean> pcViews = new ArrayList<FormViewBean>(views.size());
        List<FormViewBean> mobileViews = new ArrayList<FormViewBean>(views.size());
        for (FormViewBean view : views) {
            if (view.isPc()) {
                pcViews.add(view);
            } else {
                mobileViews.add(view);
            }
        }
        mav.addObject("pcViews", pcViews);
        mav.addObject("mobileViews", mobileViews);


        //预归档到 显示明细 需要用到 formbean对象
        mav.addObject("formBean", cap4fb);


        PermissionVO vo = permissionManager.getDefaultPermissionByConfigCategory("col_flow_perm_policy", AppContext.currentAccountId());
        mav.addObject("defaultPolicyId", vo.getName());
        mav.addObject("defaultPolicyName", vo.getLabel());

        WorkflowFormDataMapManager formDataManager = wapi.getWorkflowFormDataMapManager("form");
        String startDefualtRightId = formDataManager.getStartDefualtRightId(cap4fb.getId());
        String normalDefualtRightId = formDataManager.getNormalDefualtRightId(cap4fb.getId());

        mav.addObject("startDefualtRightId", startDefualtRightId);
        mav.addObject("normalDefualtRightId", normalDefualtRightId);
        if (template != null) {
            mav.addObject("tempalteVersion", template.getVersion());
            mav.addObject("orginalTemplateWorkFlowId", template.getExtraAttr("orginalTemplateWorkFlowId"));
        } else {
            mav.addObject("tempalteVersion", 1);
            mav.addObject("processId", UUIDLong.longUUID());
        }

        mav.addObject("defId", defId);
        mav.addObject("fbatchId", fbatchId);
        mav.addObject("newFlag", "1");
        mav.addObject("categoryName", tempateCategoryName(cap4fb.getCategoryId()));
        mav.addObject("formTitle", cap4fb.getFormName());


        return mav;
    }


    //找出设置好的消息规则
    private List<MessageRuleVO> searchMessageRuleData(String messageRuleId) {
        List<MessageRuleVO> list = new ArrayList<MessageRuleVO>();
        if (Strings.isNotBlank(messageRuleId)) {
            List<String> idlist = new ArrayList<String>();
            String[] split = messageRuleId.split(",");
            for (int a = 0; a < split.length; a++) {
                if (Strings.isNotBlank(split[a])) {
                    idlist.add(split[a]);
                }
            }
            if (messageRuleManager == null) {
                messageRuleManager = (MessageRuleManager) AppContext.getBean("messageRuleManager");
            }
            list = messageRuleManager.getMessageRuleByIdList(idlist);
        }
        return list;
    }

    private void parseDeadlineFormFields4CAP4(ModelAndView mav, com.seeyon.cap4.form.bean.FormBean cap4fb,
                                              ColSummary temSummary) {
        //解析流程期限表单字段
        String[] deadlineFormField = new String[2];
        String deadlineTemplate = temSummary.getDeadlineTemplate();
        if (Strings.isNotEmpty(deadlineTemplate) && deadlineTemplate.startsWith("field")) {
            com.seeyon.cap4.form.bean.FormFieldBean ffb = cap4fb.getFieldBeanByName(deadlineTemplate);
            if (ffb != null) {
                deadlineFormField[0] = ffb.getName();
                deadlineFormField[1] = ffb.getDisplay();
            }
        }
        mav.addObject("deadlineFormField", deadlineFormField);

        if (Strings.isBlank(deadlineTemplate) && temSummary.getDeadline() != null) {
            deadlineTemplate = String.valueOf(temSummary.getDeadline());
        }
        mav.addObject("deadlineTemplate", deadlineTemplate);

    }

    private void parseDeadlineFormFields4CAP3(ModelAndView mav, FormBean cap4fb,
                                              ColSummary temSummary) {
        //解析流程期限表单字段
        String[] deadlineFormField = new String[2];
        String deadlineTemplate = temSummary.getDeadlineTemplate();
        if (Strings.isNotEmpty(deadlineTemplate) && deadlineTemplate.startsWith("field")) {
            FormFieldBean ffb = cap4fb.getFieldBeanByName(deadlineTemplate);
            if (ffb != null) {
                deadlineFormField[0] = ffb.getName();
                deadlineFormField[1] = ffb.getDisplay();
            }
        }
        mav.addObject("deadlineFormField", deadlineFormField);

        if (Strings.isBlank(deadlineTemplate) && temSummary.getDeadline() != null) {
            deadlineTemplate = String.valueOf(temSummary.getDeadline());
        }
        mav.addObject("deadlineTemplate", deadlineTemplate);
    }

    private ModelAndView CommonOperation(ModelAndView mav, CtpTemplate c) {
        if (null != c.getPublishTime()) {
            String publishTime = Datetimes.format(c.getPublishTime(), "yyyy-MM-dd HH:mm");
            mav.addObject("publishTime", publishTime);
        }
        if (null != c.getProcessLevel()) {
            String enumShowName = templateManager.getEnumShowName("cap_process_leavel", c.getProcessLevel().toString());
            mav.addObject("processLL", enumShowName);
            mav.addObject("processLL", enumShowName);
        }

        return mav;
    }

    private ModelAndView commonPermissAttribute(ModelAndView mav, String moduleType) throws BusinessException {
        String defaultName = "";
        if (moduleType.equals(String.valueOf(ModuleType.edocSend.getKey()))) {
            defaultName = EnumNameEnum.edoc_send_permission_policy.name();
        } else if (moduleType.equals(String.valueOf(ModuleType.edocRec.getKey()))) {
            defaultName = EnumNameEnum.edoc_rec_permission_policy.name();
        } else if (moduleType.equals(String.valueOf(ModuleType.edocSign.getKey()))) {
            defaultName = EnumNameEnum.edoc_qianbao_permission_policy.name();
        } else if (moduleType.equals(String.valueOf(ModuleType.info.getKey()))) {
            defaultName = "info_send_permission_policy";
        } else {
            defaultName = "col_flow_perm_policy";
        }
        PermissionVO vo = permissionManager.getDefaultPermissionByConfigCategory(defaultName, AppContext.currentAccountId());
        mav.addObject("defaultPolicyId", vo.getName());
        mav.addObject("defaultPolicyName", vo.getLabel());
        return mav;
    }

    /**
     * 通过督办角色获取名称
     *
     * @return
     */
    private String getRoleName(String role) {
        if ("sender".equals(role)) {
            return ResourceUtil.getString("collaboration.common.common.supervise.initiator");
        } else {
            return ResourceUtil.getString("collaboration.common.common.supervise.initiatorManager");
        }
    }


    private String getLongString(Long value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    private String[] getRACIFillbackMessage(String str) {
        String[] fb = new String[3];
        String forShow = "";
        String forDB = "";
        String forFb = "";
        if (Strings.isNotBlank(str)) {
            String[] split = str.split("[|]");
            for (int a = 0; a < split.length; a++) {
                if (a == split.length - 1) {
                    forShow += split[a].split("_")[1];
                    forFb += split[a].split("_")[2] + "|" + split[a].split("_")[0];
                } else {
                    forShow += split[a].split("_")[1] + "、";
                    forFb += split[a].split("_")[2] + "|" + split[a].split("_")[0] + ",";
                }
            }
        }
        forDB = str;
        fb[0] = forShow;
        fb[1] = forDB;
        fb[2] = forFb;
        return fb;
    }

    private String[] getRACIFillbackMessage(String str, Long objectId, String moduleType) throws BusinessException {
        String[] fb = new String[3];
        String forShow = "";
        String forDB = "";
        String forFb = "";
        Long senderMember = 0l;
        boolean needSearch = false;
        if (String.valueOf(ModuleType.collaboration.getKey()).equals(moduleType) ||
                String.valueOf(ModuleType.form.getKey()).equals(moduleType)) {
            CtpAffair senderAffair = affairManager.getSenderAffair(objectId);
            senderMember = senderAffair.getMemberId();
            needSearch = true;
        }

        if (Strings.isNotBlank(str)) {
            String[] split = str.split("[|]");
            for (int a = 0; a < split.length; a++) {
                if (a == split.length - 1) {


                    String type = split[a].split("_")[2];
                    String id = split[a].split("_")[0];

                    if ("Node".equals(type)) {
                        List<V3xOrgMember> m = templateInsManager.parseNodeMembers(id, senderMember);
                        if (Strings.isNotEmpty(m)) {
                            forShow += split[a].split("_")[1] + "|";
                            int all = m.size();
                            boolean endwith = false;
                            if (all > 10) {
                                endwith = true;
                                all = 10;
                            }
                            for (int count = 0; count < all; count++) {
                                V3xOrgMember v3xOrgMember = m.get(count);
                                if (count == all - 1) {
                                    forShow += v3xOrgMember.getName();
                                } else {
                                    forShow += v3xOrgMember.getName() + "、";
                                }
                            }
                            if (endwith) {
                                forShow += "...";
                            }
                        } else {
                            forShow += split[a].split("_")[1];
                        }
                    } else if ("Account".equals(type) || "Department".equals(type) || "Member".equals(type) ||
                            "Level".equals(type) || "Post".equals(type)) {
                        forShow += split[a].split("_")[1];
                    } else if ("nodevouch".equals(type) || "nodeinfo".equals(type)) {
                        if (needSearch) {
                            if ("nodevouch".equals(type)) {
                                String strPer = buildNodePeopleStr(objectId, "vouch");
                                if (Strings.isNotBlank(strPer)) {
                                    forShow += split[a].split("_")[1] + "|" + strPer;
                                } else {
                                    forShow += split[a].split("_")[1];
                                }
                            } else {
                                String strPer = buildNodePeopleStr(objectId, "inform");
                                if (Strings.isNotBlank(strPer)) {
                                    forShow += split[a].split("_")[1] + "|" + strPer;
                                } else {
                                    forShow += split[a].split("_")[1];
                                }
                            }
                        } else {
                            forShow += split[a].split("_")[1];
                        }
                    }

                } else {

                    String type = split[a].split("_")[2];
                    String id = split[a].split("_")[0];


                    if ("Node".equals(type)) {

                        List<V3xOrgMember> m = templateInsManager.parseNodeMembers(id, senderMember);
                        if (Strings.isNotEmpty(m)) {
                            forShow += split[a].split("_")[1] + "|";
                            int all = m.size();
                            boolean endwith = false;
                            if (all > 10) {
                                endwith = true;
                                all = 10;
                            }
                            for (int count = 0; count < all; count++) {
                                V3xOrgMember v3xOrgMember = m.get(count);
                                if (count == all - 1) {
                                    forShow += v3xOrgMember.getName();
                                } else {
                                    forShow += v3xOrgMember.getName() + "、";
                                }
                            }
                            if (endwith) {
                                forShow += "...";
                            }
                            forShow += ",";
                        } else {
                            forShow += split[a].split("_")[1] + ",";
                        }

                    } else if ("Account".equals(type) || "Department".equals(type) || "Member".equals(type) ||
                            "Level".equals(type) || "Post".equals(type)) {
                        forShow += split[a].split("_")[1] + ",";
                    } else if ("nodevouch".equals(type) || "nodeinfo".equals(type)) {

                        if (needSearch) {
                            if ("nodevouch".equals(type)) {
                                String strPer = buildNodePeopleStr(objectId, "vouch");
                                if (Strings.isNotBlank(strPer)) {
                                    forShow += split[a].split("_")[1] + "|" + strPer + ",";
                                } else {
                                    forShow += split[a].split("_")[1] + ",";
                                }
                            } else {
                                String strPer = buildNodePeopleStr(objectId, "inform");
                                if (Strings.isNotBlank(strPer)) {
                                    forShow += split[a].split("_")[1] + "|" + strPer + ",";
                                } else {
                                    forShow += split[a].split("_")[1] + ",";
                                }
                            }
                        } else {
                            forShow += split[a].split("_")[1] + ",";
                        }

                    }
                }
            }
        }
        forDB = str;
        fb[0] = forShow;
        //fb[1] = forDB;
        //fb[2] = forFb;
        return fb;
    }

    private String buildNodePeopleStr(Long objectId, String nodeName) throws BusinessException {
        try {

            String peopleStr = "";
            List<StateEnum> states = new ArrayList<StateEnum>();
            states.add(StateEnum.col_pending);
            states.add(StateEnum.col_done);
            List<CtpAffair> affairs = affairManager.getAffairs(objectId, states);
            int count = 0;
            if (Strings.isNotEmpty(affairs)) {
                for (int m = 0; m < affairs.size(); m++) {
                    CtpAffair ctpAffair = affairs.get(m);
                    if (ctpAffair.getNodePolicy().equals(nodeName)) {
                        count++;
                        peopleStr += orgManager.getMemberById(ctpAffair.getMemberId()).getName() + "、";
                        if (count > 9) {
                            peopleStr = peopleStr.substring(0, peopleStr.length() - 1) + "...";
                            break;
                        }
                    }
                }
                if (peopleStr.endsWith("、")) {
                    peopleStr = peopleStr.substring(0, peopleStr.length() - 1);
                }
            }
            return peopleStr;
        } catch (Exception e) {
            LOG.info("解析节点人员出错:" + e.getMessage());
            return "";
        }

    }

    public ModelAndView openRACIDes(HttpServletRequest request, HttpServletResponse response) throws BusinessException {

        ModelAndView mav = new ModelAndView("apps/collaboration/raciDes");

        return mav;
    }


    public ModelAndView saveSimpleTemplate2Cache(HttpServletRequest request,
                                                 HttpServletResponse response) throws BusinessException {

        //流程信息
        Map processCreate = ParamUtil.getJsonDomain("processCreate");
        //高级信息
        Map baseInfo = ParamUtil.getJsonDomain("baseInfo");
        String templateId = request.getParameter("templateId");
        String openWinId = request.getParameter("openWinId");//公文专用

        CtpTemplate c = new CtpTemplate();

        c.setResponsible((String) baseInfo.get("responsible"));
        c.setAuditor((String) baseInfo.get("auditor"));
        c.setConsultant((String) baseInfo.get("consultant"));
        c.setInform((String) baseInfo.get("inform"));
        c.setCoreUseOrg((String) baseInfo.get("coreUseOrg"));

        String s = (String) baseInfo.get("updateProcessDesFlag");
        c.putExtraAttr("updateProcessDesFlag", s);

        String belongOrg = (String) baseInfo.get("belongOrg");
        c.setBelongOrg(Strings.isNotBlank(belongOrg) ? Long.valueOf(belongOrg) : null);

        String publishTime = (String) baseInfo.get("publishTime");
        if (Strings.isNotBlank(publishTime)) {
            c.setPublishTime(Datetimes.parse(publishTime));
        }
        c.setProcessLevel(Integer.valueOf((String) baseInfo.get("processLevel")));

        String processXml = processCreate.get("process_xml") == null || Strings.isBlank((String) processCreate.get("process_xml")) ? "" : (String) processCreate.get("process_xml");//流程定义模版内容 java.lang.String 流程模版xml内容
        c.putExtraAttr("processXml", processXml);
        String subProcessSetting = processCreate.get("process_subsetting") == null || Strings.isBlank((String) processCreate.get("process_subsetting")) ? "" : (String) processCreate.get("process_subsetting");//流程模版绑定的子流程信息 java.lang.String 流程模版绑定的子流程信息
        c.putExtraAttr("process_subsetting", subProcessSetting);
        String workflowRule = (String) baseInfo.get("siwfRule");
        c.putExtraAttr("wfRule", workflowRule);
        String processId = processCreate.get("process_id") == null || Strings.isBlank((String) processCreate.get("process_id")) ? "-1" : (String) processCreate.get("process_id");
        c.putExtraAttr("processId", processId);
        String processEventJson = processCreate.get("process_event") == null || Strings.isBlank((String) processCreate.get("process_event")) ? "" : (String) processCreate.get("process_event");
        c.putExtraAttr("processEventJson", processEventJson);

        if (Strings.isBlank(templateId)) {//公文新建的时候
            templateId = openWinId;
        }
        V3xShareMap.put("templateData" + AppContext.currentUserId() + templateId, c);

        return null;
    }


    public ModelAndView getDes(HttpServletRequest request,
                               HttpServletResponse response) throws BusinessException {
        ModelAndView modelAndView = new ModelAndView("common/template/templateDes");
        String templateId = request.getParameter("templateId");
        String moduleType = request.getParameter("moduleType");
        CtpTemplate ctpTemplate = templateManager.getCtpTemplate(Long.parseLong(templateId));
        if (null == ctpTemplate.getWorkflowId()) {
            return modelAndView;
        }
        String workflowRuleInfo = wapi.getWorkflowRuleInfo(moduleType, ctpTemplate.getWorkflowId().toString());
        modelAndView.addObject("workflowRuleInfo",
                !Strings.isNotBlank(workflowRuleInfo) ? workflowRuleInfo : Strings.toHTML(workflowRuleInfo));
        return modelAndView;
    }

    private String getMoreTemplateCategorys(String category, String fragmentId, String ordinal) {
        if (StringUtils.isNotBlank(fragmentId)) {
            Map<String, String> preference = portalApi.getPropertys(Long.parseLong(fragmentId),
                    ordinal);
            category = templateManager.getPanelCategory4Portal(preference);
        } else {
            List<Long> moduleTypes = new ArrayList<Long>();
            templateManager.getModuleTypes4Portal(moduleTypes);
            StringBuilder categorys = new StringBuilder();
            for (Long moduleType : moduleTypes) {
                if (Strings.isNotBlank(categorys.toString())) {
                    categorys.append(",");
                }
                categorys.append(String.valueOf(moduleType));
            }
            category = categorys.toString();
        }
        return category;
    }

    public ModelAndView listRACITemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            String viewType = customizeManager.getCustomizeValue(AppContext.currentUserId(), "template_view_type");
            if ("0".equals(viewType)) {
                return this.index(request, response);
            }
        } catch (Exception e) {
        }
        ModelAndView modelAndView = new ModelAndView("common/template/RACITemplate");
        User user = AppContext.getCurrentUser();
        Long orgAccountId = user.getLoginAccount();

        String category = ReqUtil.getString(request, "category");
        String fragmentId = ReqUtil.getString(request, "fragmentId");
        String ordinal = ReqUtil.getString(request, "ordinal");
        //如果有则不查询
        if (Strings.isBlank(category)) {
            category = String.valueOf(TemplateCategoryConstant.personRoot.key());
            if (AppContext.hasPlugin("collaboration")) {
                category += ",1,2,66";//CAP4表单
            }
            if (AppContext.hasPlugin("edoc")) {
                category += ",4,19,20,21";
            }
            if (AppContext.hasPlugin("infosend")) {
                category += ",32";
            }
            category = this.getMoreTemplateCategorys(category, fragmentId, ordinal);
        }
        //根据首页模板栏目编辑页面条件，查询所有配置模板的集合
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("categoryIds", category);

        //模版所在的单位集合
        Map<Long, String> accounts = templateManager.getSectionShowCounts(params);

        String recent = ReqUtil.getString(request, "recent");
        modelAndView.addObject("recent", recent);
        modelAndView.addObject("accounts", accounts);
        modelAndView.addObject("category", category);
        modelAndView.addObject("orgAccountId", orgAccountId.toString());

        return modelAndView;
    }

    public void setAiApi(AIApi aiApi) {
        this.aiApi = aiApi;
    }

    public void setCustomizeManager(CustomizeManager customizeManager) {
        this.customizeManager = customizeManager;
    }

    private boolean isGovdoc(String categoryKey) {
        if (Strings.isNotBlank(categoryKey)) {
            //协同表单传入‘0,4’，转换出问题 直接判断。
            String[] len = categoryKey.split(",");
            if (len.length > 1) {
                return false;
            }
            Long ordinal = Long.valueOf(categoryKey);

            if (Long.valueOf(ModuleType.edoc.ordinal()).equals(ordinal)
                    || Long.valueOf(ModuleType.govdocSend.getKey()).equals(ordinal)
                    || Long.valueOf(ModuleType.govdocRec.getKey()).equals(ordinal)
                    || Long.valueOf(ModuleType.govdocSign.getKey()).equals(ordinal)
                    || Long.valueOf(ModuleType.govdocExchange.getKey()).equals(ordinal)) {
                return true;
            }
        }
        return false;
    }

    private boolean canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType dealType, ColSummary summary) {
        String mergeDealType = "";
        if (summary != null) {
            mergeDealType = summary.getMergeDealType();
        }
        if (Strings.isNotBlank(mergeDealType)) {
            String mergeDealTypeValue = dealType.getValue();
            Map<String, String> mergeDealTypeMap = (Map<String, String>) JSONUtil.parseJSONString(mergeDealType);
            if (null != mergeDealTypeMap && !mergeDealTypeMap.isEmpty()) {
                return mergeDealTypeValue.equals(mergeDealTypeMap.get(dealType.name()));
            }
        }
        return false;
    }


    public FormApi4Cap4 getFormApi4Cap4() {
        return formApi4Cap4;
    }

    public void setFormApi4Cap4(FormApi4Cap4 formApi4Cap4) {
        this.formApi4Cap4 = formApi4Cap4;
    }

    public PermissionLayoutManager getPermissionLayoutManager() {
        return permissionLayoutManager;
    }

    public void setPermissionLayoutManager(PermissionLayoutManager permissionLayoutManager) {
        this.permissionLayoutManager = permissionLayoutManager;
    }

    public void setInfoApi(InfoApi infoApi) {
        this.infoApi = infoApi;
    }
}
