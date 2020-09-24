package com.seeyon.v3x.edoc.controller;

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

import com.seeyon.v3x.exchange.domain.EdocSendRecord;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.xkjt.manager.XkjtManager;
import com.seeyon.apps.xkjt.po.EdocFormInfo;
import com.seeyon.ctp.cluster.notification.NotificationManager;
import com.seeyon.ctp.cluster.notification.NotificationType;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.form.util.CharReplace;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.ObjectToXMLUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.common.dao.paginate.Pagination;
import com.seeyon.v3x.common.metadata.MetadataNameEnum;
import com.seeyon.v3x.edoc.constants.EdocOpinionDisplayEnum.OpinionInscriberSetEnum;
import com.seeyon.v3x.edoc.domain.EdocCategory;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.domain.EdocFormAcl;
import com.seeyon.v3x.edoc.domain.EdocFormElement;
import com.seeyon.v3x.edoc.domain.EdocFormExtendInfo;
import com.seeyon.v3x.edoc.domain.EdocFormFlowPermBound;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocCategoryManager;
import com.seeyon.v3x.edoc.manager.EdocElementFlowPermAclManager;
import com.seeyon.v3x.edoc.manager.EdocElementManager;
import com.seeyon.v3x.edoc.manager.EdocFormManager;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.manager.EdocRegisterManager;
import com.seeyon.v3x.edoc.manager.EdocRoleHelper;
import com.seeyon.v3x.edoc.manager.EdocSummaryManager;
import com.seeyon.v3x.edoc.util.Constants;
import com.seeyon.v3x.edoc.util.DataUtil;
import com.seeyon.v3x.edoc.util.EdocFormHelper;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.edoc.util.XMLConverter;
import com.seeyon.v3x.edoc.webmodel.EdocFormModel;
import com.seeyon.v3x.edoc.webmodel.FormBoundPerm;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.manager.EdocExchangeManager;
import com.seeyon.v3x.exchange.manager.SendEdocManager;

@CheckRoleAccess(roleTypes = {Role_NAME.AccountAdministrator, Role_NAME.EdocManagement})
public class EdocFormController extends BaseController {

    private static final Log LOGGER = LogFactory.getLog(EdocFormController.class);

    private OrgManager orgManager;
    
    private EdocElementManager edocElementManager;

    private EdocFormManager edocFormManager;

    private EdocSummaryManager edocSummaryManager;

    private EdocManager edocManager;

    private AppLogManager appLogManager;

    private EdocElementFlowPermAclManager edocElementFlowPermAclManager;
    
    private EdocCategoryManager edocCategoryManager;
    
    private EdocRegisterManager edocRegisterManager = null;
    
    private EdocExchangeManager edocExchangeManager = null;
    private SendEdocManager sendEdocManager = null;

    public void setEdocCategoryManager(EdocCategoryManager edocCategoryManager) {
        this.edocCategoryManager = edocCategoryManager;
    }
    
    public void setEdocRegisterManager(EdocRegisterManager edocRegisterManager) {
        this.edocRegisterManager = edocRegisterManager;
    }
    
    public void setEdocExchangeManager(EdocExchangeManager edocExchangeManager) {
        this.edocExchangeManager = edocExchangeManager;
    }
    
    public void setSendEdocManager(SendEdocManager sendEdocManager) {
        this.sendEdocManager = sendEdocManager;
    }

    /**
     * 设置edocElementFlowPermAclManager
     * @param edocElementFlowPermAclManager edocElementFlowPermAclManager
     */
    public void setEdocElementFlowPermAclManager(EdocElementFlowPermAclManager edocElementFlowPermAclManager) {
        this.edocElementFlowPermAclManager = edocElementFlowPermAclManager;
    }

    /**
     * 设置appLogManager
     * @param appLogManager appLogManager
     */
    public void setAppLogManager(AppLogManager appLogManager) {
        this.appLogManager = appLogManager;
    }

    public EdocManager getEdocManager() {
        return edocManager;
    }

    public void setEdocManager(EdocManager edocManager) {
        this.edocManager = edocManager;
    }

    private XMLConverter xmlConverter;

    private AttachmentManager attachmentManager;

    private ConfigManager configManager;

    private FileManager fileManager;

    private PermissionManager permissionManager;

    public FileManager getFileManager() {
        return fileManager;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }


    public AttachmentManager getAttachmentManager() {
        return attachmentManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }


    public EdocSummaryManager getEdocSummaryManager() {
        return edocSummaryManager;
    }

    public void setEdocSummaryManager(EdocSummaryManager edocSummaryManager) {
        this.edocSummaryManager = edocSummaryManager;
    }

    public EdocFormManager getEdocFormManager() {
        return edocFormManager;
    }

    public void setEdocFormManager(EdocFormManager edocFormManager) {
        this.edocFormManager = edocFormManager;
    }

    public EdocElementManager getEdocElementManager() {
        return edocElementManager;
    }

    public void setEdocElementManager(EdocElementManager edocElementManager) {
        this.edocElementManager = edocElementManager;
    }

    public OrgManager getOrgManager() {
        return orgManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setXmlConverter(XMLConverter xmlConverter) {
        this.xmlConverter = xmlConverter;
    }

    @Override
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
    @CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
    public ModelAndView listMain(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("edoc/formManage/form_list_main");
        if(request.getParameter("id") != null)
            mav.addObject("id", request.getParameter("id"));
        return mav;
    }
    @CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
    public ModelAndView list(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 处理查询条件
        List<EdocForm> list = null;
        User user = AppContext.getCurrentUser();
        list = getEdocFormWebList(user,request);
        ModelAndView ret = new ModelAndView("edoc/formManage/form_list_iframe");
        ret.addObject("isAccountAdmin", EdocRoleHelper.hasInputFunctionFromGroup());
        boolean isGroupVer = (Boolean) (SysFlag.sys_isGroupVer.getFlag());// 判断是否为集团版
        ret.addObject("isGroupVer",isGroupVer);
        ret.addObject("list", list);
        return ret;
    }

    private List<EdocForm> getEdocFormWebList(User user,HttpServletRequest request) {
        List<EdocForm> list = new ArrayList<EdocForm>();
        boolean isGroupVer = (Boolean)(SysFlag.sys_isGroupVer.getFlag());
        String condition = request.getParameter("condition");
        String textfield = request.getParameter("textfield");
        
        try {
            list = edocFormManager.getAllEdocFormsForWeb(user, V3xOrgEntity.VIRTUAL_ACCOUNT_ID,condition,textfield);
        } catch(Exception e) {
            LOGGER.error("查找公文单定义列表异常", e);
        }
        // 由于组织模型的数据都是放在内存中的，所以可以像下面这样循环查询
        for(EdocForm ef : list) {
            Set<EdocFormAcl> acls = ef.getEdocFormAcls();
            StringBuilder aclIds = new StringBuilder();
            if(acls != null) {
                for(EdocFormAcl acl : acls) {
                    if(acl.getDomainId() != null) {
                        if(aclIds.length() > 0){
                            aclIds.append(",");
                        }
                        aclIds.append(acl.getDomainId().toString());
                    }
                }
            }
            ef.setAclIds(aclIds.toString());
            // 查询制作单位的名称。
            String createDomainName = "";
            try {
            	V3xOrgAccount account = orgManager.getAccountById(ef.getDomainId());
            	if(account !=null){
            		createDomainName = account.getName();
            	}
            } catch(BusinessException e) {
                LOGGER.error("查询公文单制作单位名称异常"+ef.getDomainId(), e);
            }
            ef.setDomainName(createDomainName);
        }
        return EdocUtil.convertExtendInfo2EdocForm(list, user.getLoginAccount());
    }

    // 显示系统公文单
    public ModelAndView listSystemForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView ret = new ModelAndView("edoc/formManage/dispSystemForm");
        return ret;
    }

    public ModelAndView listSystemFormIframe(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 处理查询条件
        List<EdocForm> list = null;
        User user = AppContext.getCurrentUser();
        list = edocFormManager.getAllEdocForms(orgManager.getRootAccount().getId());
        ModelAndView ret = new ModelAndView("edoc/formManage/dispSystemFormIframe");
        ret.addObject("list", pagenate(list));
        return ret;
    }
    @CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
    public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();  
        ModelAndView mav = new ModelAndView("edoc/formManage/form_modify");
        mav.addObject("method_type", "change");
        EdocSummary edocSummary = null;
        EdocForm bean = null;
        String idStr = request.getParameter("id"); 
        edocSummary = edocSummaryManager.findById(Long.parseLong(idStr));
        bean = edocFormManager.getEdocForm(Long.parseLong(idStr)); 
        
        //OA-36043 q2新建文单授权给本单位和gw单位，2个单位都没有使用该文单的时候，q2将该文单删了，这时gw单位下人员a1仍然能看到文单，单击，报异常 --文单防空值判断
        if(bean == null ){
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            String errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", "alert_edocform_isnotexsit");
            out.println("<script>alert('"+StringEscapeUtils.escapeJavaScript(errMsg)+"');");
            out.println("</script>");
            return null;
        } 
        
        //如果系统文单内容为空，需要初始化一下content
        if(bean.getIsSystem() && Strings.isBlank(bean.getContent())){
            edocFormManager.updateFormContentToDBOnly();
            bean = edocFormManager.getEdocForm(Long.parseLong(idStr)); 
        }
        boolean isOuterAccountAcl = !bean.getDomainId().equals(user.getLoginAccount());
        if(bean.getType()==0||bean.getType()==1||bean.getType()==2){
            //Permission flowPerm = null;
            //if(bean.getType()==1) {            //获取分发权限对象
            ///    flowPerm = permissionManager.getPermission(MetadataNameEnum.edoc_rec_permission_policy.name(), "dengji", user.getLoginAccount());
            //} else {//获取拟文权限对象
            //    flowPerm = permissionManager.getPermission(MetadataNameEnum.edoc_send_permission_policy.name(), "niwen", user.getLoginAccount());
           // }
                // 获取拟文权限可以设置权限的公文元素
                //if(flowPerm != null){
                    //List<EdocElementFlowPermAcl> acl_list = edocElementFlowPermAclManager.getEdocElementFlowPermAcls(flowPerm.getFlowPermId());   
                    List<EdocFormElement> formElements = new ArrayList<EdocFormElement>();
                    for(EdocFormElement formElement : bean.getEdocFormElements()) {
                       //找出在文单中有编辑权限的公文元素
                        String elementId = String.valueOf(formElement.getElementId());
                        if(elementId.length()==1){
                            elementId ="00"+elementId;
                        }
                        if(elementId.length()==2){
                            elementId ="0"+elementId;
                        }
                        EdocElement element = edocElementManager.getEdocElement(elementId);     
                        //for(EdocElementFlowPermAcl fAcl : acl_list) {
                            // 判断是否能编辑
                            
                            if(element !=null && element.getType()!=EdocElement.C_iElementType_Comment && element.getType()!=EdocElement.C_iElementType_LogoImg){
                                //if(fAcl.getEdocElement().getFieldName().equals(element.getFieldName())){
                                formElement.setElementName(element.getName());
                                formElement.setSystemType(element.getIsSystem());
                                formElement.setAccess(true);
                                formElements.add(formElement); 
                                //}
                            }
                        //}
                    }
                    mav.addObject("formElements", formElements);
               // }
                
                
                //发文增加分类、、
                List<EdocCategory> categories = this.edocCategoryManager.getCategoryByRoot(Long.valueOf(ApplicationCategoryEnum.edocSend.getKey()), user.getLoginAccount(),false);
                //外单位授权，需要查询原单位的发文种类，先临时这么处理
                if(isOuterAccountAcl && bean.getSubType()!=null){
                    EdocCategory currentCategory = this.edocCategoryManager.getCategoryById(bean.getSubType());
                    if(currentCategory != null){
                        categories.add(currentCategory);
                    }
                }
                mav.addObject("categories", categories); 
        }
        
        StringBuilder aclIds = new StringBuilder();
        Set<EdocFormAcl> aclList = bean.getEdocFormAcls();
        if(aclList != null) {
            for(EdocFormAcl acl : aclList) {
                if(aclIds.length() > 0){
                    aclIds.append(",");
                }
                aclIds.append(acl.getDomainId());
            }
        }
        Set<EdocFormExtendInfo> infos = bean.getEdocFormExtendInfo();
        if(infos != null) {
            for(EdocFormExtendInfo info : infos) {
                if(info.getAccountId().longValue() == user.getLoginAccount().longValue()) {
                    bean.setStatus(info.getStatus());
                    bean.setIsDefault(info.getIsDefault());
                    //bean.setWebOpinionSet(info.getOptionFormatSet());//公文单配置，JSON字符串
                    //公文配置详细，和新建保持一致
                    String defualtConfig = info.getOptionFormatSet();
                    mav.addObject("formConfigJSON", defualtConfig);
                    
                    mav.addObject("edocFormStatusId", info.getId());
                }
            }
        }
        // 是否是外单位授权给本单位使用的.
        mav.addObject("isOuterAccountAcl", isOuterAccountAcl);
        mav.addObject("bean", bean);
        mav.addObject("aclIds", aclIds);
        mav.addObject("type", bean.getType());
        String str = edocFormManager.getEdocFormXmlData(Long.parseLong(idStr), edocSummary, -1, bean.getType());// 新增类型
        mav.addObject("xml", str);
        int i = str.indexOf("&&&&&&&&  data_start  &&&&&&&&");
        int j = str.indexOf("&&&&&&&&  input_start  &&&&&&&&");
        String str_a = str.substring(i + 30, j);
        str_a = str_a.substring(str_a.indexOf(">") + 1, str_a.length());
        String original_xml = com.seeyon.v3x.edoc.util.StringUtils.xmlElementToString(str_a);
        mav.addObject("original_xml", com.seeyon.ctp.util.Strings.toHTML(original_xml));
        Long fileId = bean.getFileId();
        if(fileId == null) {
            fileId = 0L;
        }
        V3XFile v3xfile = fileManager.getV3XFile(fileId);
        if(v3xfile != null) {
            mav.addObject("fileId", fileId);
            mav.addObject("fileName", v3xfile.getFilename());
            mav.addObject("createDate", new Timestamp(v3xfile.getCreateDate().getTime()).toString().substring(0, 10));
            // 用于初始化该目录下的节点权限集合
            String category = "";
            if(null != bean) {
                if(bean.getType().intValue() == EdocEnum.edocType.sendEdoc.ordinal()) {
                    category = MetadataNameEnum.edoc_send_permission_policy.name();
                } else if(bean.getType().intValue() == EdocEnum.edocType.recEdoc.ordinal()) {
                    category = MetadataNameEnum.edoc_rec_permission_policy.name();
                } else if(bean.getType().intValue() == EdocEnum.edocType.signReport.ordinal()) {
                    category = MetadataNameEnum.edoc_qianbao_permission_policy.name();
                }
            }
            
            List<PermissionVO> flowPermlist = permissionManager.getPermission(category, Permission.Node_isActive, user.getLoginAccount());
            mav.addObject("flowPermList", flowPermlist);
            //
            String[] urls = new String[1];
            urls[0] = v3xfile.getId().toString();
            
            String[] createDates = new String[1];
            createDates[0] = Datetimes.formatDatetime(v3xfile.getCreateDate());
            
            String[] mimeTypes = new String[1];
            mimeTypes[0] = v3xfile.getMimeType().toString();
            
            String[] names = new String[1];
            names[0] = v3xfile.getFilename().toString();
            
            if(Strings.isBlank(urls[0])) {
                return mav;
            }
            
            List<String> list = new ArrayList<String>();
            List<EdocFormElement> elementList2 = edocFormManager.getEdocFormElementByFormId(bean.getId());
            for(EdocFormElement ele : elementList2) {
                EdocElement element = edocElementManager.getEdocElementsById(ele.getElementId());
                list.add(element.getFieldName());
            }
            
            StringBuilder operationStr = new StringBuilder();
            List<EdocFormFlowPermBound> boundPermList = edocFormManager.findBoundByFormIdAndDomainId(bean.getId(), user.getLoginAccount());
            if(null != boundPermList) {
                for(EdocFormFlowPermBound bound : boundPermList) {
                     operationStr.append("(")
                                 .append(bound.getFlowPermName())
                                 .append(")");
                }
                mav.addObject("operation_str", operationStr);
                // 取得公文处理意见的名称列表
                List<FormBoundPerm> processList = EdocHelper.getProcessOpinionByEdocFormId(list, bean.getId(), bean.getType(), user.getLoginAccount(), false);
                mav.addObject("processList", processList);
                // 用于提交选择参数
                StringBuilder listStr = new StringBuilder();
                for(FormBoundPerm perm : processList) {
                    listStr.append(perm.getPermItem())
                           .append(",");
                }
                if(listStr.length() > 1) {
                    listStr.deleteCharAt(listStr.length() - 1);
                }
                mav.addObject("listStr", listStr.toString());
            }
            String logoURL = EdocHelper.getLogoURL();
            mav.addObject("logoURL", logoURL);
            String content = bean.getContent();
                   
            if(Strings.isBlank(content)) {
                
                //进行容错处理
                String xsnFilePath = edocFormManager.getDirectory(urls, createDates, mimeTypes, names);
                String xsl = EdocFormHelper.parseInfoPathXSL(xsnFilePath);
                if(Strings.isNotBlank(xsl)){
                    content = xsl;
                    bean.setContent(content);
                    edocFormManager.updateEdocForm(bean);//更新数据库
                }else {
                    String alertNote = ResourceUtil.getString("edoc.form.content.empty");
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().println("<script>alert('" + alertNote + "');</script>");
                    return mav;
                }
            }
            byte[] tempByte_b = CharReplace.doReplace_Decode(content.getBytes("UTF-8"));
            content = new String(tempByte_b, "UTF-8");
            //&符号转义-BUG_普通_V5_V5.1sp1_致远客服部_公文单有"<"，在导入的时候会提示“公文单数据出现异常！错误原因：解析XML失败”_20150626010068.在seeyonForm3.js有对应转换。搜bug编号
            content = content.replaceAll("&", "&amp;");
            mav.addObject("xsl", content);
        } else {
            LOGGER.error("单位管理员查看公文单出错，EdocFormController.edit查找v3xfile为空.公文单ID：" + bean.getId() + " v3xFile的ID为：" + fileId);
        }
        
        // best 文单中添加需要隐藏的部门  start
        try {
        	if (bean != null) {
	        	XkjtManager xkjtManager = (XkjtManager) AppContext.getBean("xkjtManager");
	        	EdocFormInfo formInfo = xkjtManager.getByFormId(bean.getId());
	        	if (formInfo != null)
	        	mav.addObject("formInfo", formInfo);
        	}
        } catch (Exception e) {
        	logger.error("best文单中添加需要隐藏的部门异常：", e);
        }
        // best 文单中添加需要隐藏的部门  end
        
        return mav;
    }
    @CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
    public ModelAndView create(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("edoc/formManage/new_edoc_form");
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        User user = AppContext.getCurrentUser();
        String name = request.getParameter("name");
        String type = request.getParameter("type");
        String description = request.getParameter("description");
        String status = request.getParameter("status");
        String content = request.getParameter("content");
        String showLog = request.getParameter("showLog");
        String isDefault = request.getParameter("isDefault");
        String grantedDomainId = request.getParameter("grantedDomainId");
        // 判断是否有重名的公文单
        boolean bool = edocFormManager.checkHasName(name, Integer.valueOf(type));
        if(bool) {
            writer.println("<script>");
            writer.println("alert(parent._('edocLang.edoc_form_duplicated'));");
            writer.println("self.history.back();");
            writer.println("</script>");
            mav.addObject("description", description);
            mav.addObject("type", type);
            mav.addObject("status", status);
            mav.addObject("content", content);
            mav.addObject("showLog", showLog);
            mav.addObject("name", name);
            mav.addObject("isDefault", isDefault);
            return mav;
        }
        if(null != content && !"".equals(content)) {
            byte[] tempByte_a = CharReplace.doReplace_Encode(content.getBytes("UTF-8"));
            content = new String(tempByte_a, "UTF-8");
        }

        List<Long> elementIdList = dealEleList(request.getParameter("element_id_list"));
        
        long l = System.currentTimeMillis();
        Set<EdocFormElement> edocFormElements = null;
        EdocForm edocForm = new EdocForm();
        Long uuid = UUIDLong.longUUID();
        edocForm.setId(uuid);
        edocForm.setName(name);
        edocForm.setDescription(description);
        edocForm.setType(Integer.valueOf(type));
        edocForm.setContent(content);
        edocForm.setCreateUserId(user.getId());
        edocForm.setCreateTime(new java.sql.Timestamp(l));
        edocForm.setLastUserId(user.getId());
        edocForm.setLastUpdate(new java.sql.Timestamp(l));
        edocForm.setStatus(EdocForm.C_iStatus_Draft);
        edocForm.setDomainId(user.getLoginAccount());
        edocForm.setEdocFormElements(edocFormElements);
        edocForm.setStatus(Integer.valueOf(status));
        edocForm.setShowLog("1".equals(showLog));
        edocForm.setIsSystem(false);
        
        //文单设置设置保存
        FormOpinionConfig formConfig = parseConfigData(request);
        
        String edocCategory = request.getParameter("edocCategory");
        if(Strings.isNotBlank(edocCategory)) {
            edocForm.setSubType(Long.parseLong(edocCategory));
        }
        
        try {
            if(null != isDefault && "1".equals(isDefault)) {
                // 从数据库中取消默认公文单
                boolean hasSubType=EdocHelper.hasEdocCategory()&&EdocHelper.isG6Version();//G6版本，并且开启了公文种类开关的时候才有hasSubType
                if(Integer.valueOf(type).intValue() == 0) {
                    edocFormManager.updateDefaultEdocForm(user.getLoginAccount(), Integer.valueOf(type), edocForm.getSubType(),hasSubType);
                } else {
                    edocFormManager.updateDefaultEdocForm(user.getLoginAccount(), Integer.valueOf(type), null,hasSubType);
                }
                edocForm.setIsDefault(true);
                // 设置空的默认公文单，下次读取时，重新从数据库中读取
                edocFormManager.removeDefaultEdocForm(edocForm.getDomainId(), edocForm.getType());
            } else if(null != isDefault && "0".equals(isDefault)) {
                // edocFormManager.updateDefaultEdocForm(user.getLoginAccount(),
                // Integer.valueOf(type));
                edocForm.setIsDefault(false);
            }
            String[] att_fileUrl = request.getParameterValues("att_fileUrl");
            edocForm.setFileId(Long.parseLong(att_fileUrl[0]));
            edocForm.setEdocFormAcls(getEdocFormAclSetForCurrentForm(user.getLoginAccount(), grantedDomainId, edocForm));

            String optionFormatSet = JSONUtil.toJSONString(formConfig);//将配置转换成字符串
            
            // 给所有的授权子单位添加状态信息.保存状态信息
            Set<EdocFormExtendInfo> infos = getCreateInfos(user, edocForm, optionFormatSet);
            edocForm.getEdocFormExtendInfo().addAll(infos);
            edocFormManager.createEdocForm(edocForm, elementIdList);
            
            /******************************* 保存 文单必填项设置信息*****************************/
           if(edocForm.getType()==0||edocForm.getType()==1||edocForm.getType()==2){
                //获取拟文权限对象
                //Permission flowPerm = null;
               // if(edocForm.getType()==1) {            //获取分发权限对象
               //     flowPerm = permissionManager.getPermission(MetadataNameEnum.edoc_rec_permission_policy.name(), "dengji", user.getLoginAccount());
               // } else {//获取拟文权限对象
               //     flowPerm = permissionManager.getPermission(MetadataNameEnum.edoc_send_permission_policy.name(), "niwen", user.getLoginAccount());
               // }
                // 获取拟文权限可以设置权限的公文元素
               // List<EdocElementFlowPermAcl> acl_list = edocElementFlowPermAclManager.getEdocElementFlowPermAcls(flowPerm.getFlowPermId());   
                
                List<EdocFormElement> eles = edocFormManager.getEdocFormElementByFormId(edocForm.getId());
                for(EdocFormElement formElement : eles) {
                   //找出在文单中有编辑权限的公文元素
                    EdocElement element = edocElementManager.getEdocElementsById(formElement.getElementId());   
                    //for(EdocElementFlowPermAcl fAcl : acl_list) {
                        // 判断是否能编辑
                        if(element!=null && element.getType()!=EdocElement.C_iElementType_Comment && element.getType()!=EdocElement.C_iElementType_LogoImg){
                            String onCheck =request.getParameter(String.valueOf(element.getId()));
                            if("on".equals(onCheck) || formElement.getElementId()==1){
                                 formElement.setRequired(true);
                            }else{
                                formElement.setRequired(false);
                            }
                        }
                    //}
                }
                //设置了必填项，需要update到数据库  -xiangfan
                edocFormManager.updateEdocFormElement(eles);
            }
            /******************************* 保存 文单必填项设置信息*****************************/
            // 记录应用
            appLogManager.insertLog(user, AppLogAction.Edoc_Form_Crete, user.getName(), edocForm.getName());
            String boundName = request.getParameter("boundName");
            mav.addObject("boundName", boundName);
            String category = "";
            if(null != type && !"".equals(type)) {
                if(Integer.valueOf(type).intValue() == EdocEnum.edocType.sendEdoc.ordinal()) {
                    category = MetadataNameEnum.edoc_send_permission_policy.name();
                } else if(Integer.valueOf(type).intValue() == EdocEnum.edocType.recEdoc.ordinal()) {
                    category = MetadataNameEnum.edoc_rec_permission_policy.name();
                } else if(Integer.valueOf(type).intValue() == EdocEnum.edocType.signReport.ordinal()) {
                    category = MetadataNameEnum.edoc_qianbao_permission_policy.name();
                }
            }
            String tempS = request.getParameter("listStr");
            if(null != tempS && !"".equals(tempS)) {
                String[] tempArray = tempS.split(",");
                List<String> flowperm_nameList = new ArrayList<String>();
                for(String process_name : tempArray) {
                    String flowperm_name = request.getParameter("returnOperation_" + process_name);
                    flowperm_nameList.add(flowperm_name);
                    String flowperm_label = request.getParameter(process_name);
                    String sortType = request.getParameter("sortType_" + process_name);
                    if(Strings.isNotBlank(flowperm_name) && Strings.isNotBlank(flowperm_label)) {
                        boundEdocFormAndFlowPerm(process_name, flowperm_name, flowperm_label, sortType, edocForm);
                    }
                }
                //更新节点权限引用情况
                int appEnum = EdocUtil.getApplicationCategoryEnumKeyByEdocType(edocForm.getType());
                permissionManager.updatePermissionRef(appEnum, Strings.join(flowperm_nameList, ","), user.getAccountId());
            }
            attachmentManager.create(ApplicationCategoryEnum.edoc, edocForm.getId(), edocForm.getId(), request);
        } catch(Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("用户新建公文单异常,EdocFormController.create ");
            if(edocForm != null) {
                sb.append("名字:" + edocForm.getName());
                sb.append("时间:" + edocForm.getCreateTime());
            }
            sb.append("用户：" + user.getName());
            LOGGER.error(sb.toString(), e);
            throw e;
        }
        return new ModelAndView("edoc/refreshWindow").addObject("windowObj","parent");
    }

    private Set<EdocFormExtendInfo> getCreateInfos(User user, EdocForm edocForm, String optionFormatSet) throws BusinessException {

        /**默认配置**/
        String defultConfig = FormOpinionConfig.getDefualtConfig();
        
        Set<EdocFormExtendInfo> infos = new HashSet<EdocFormExtendInfo>();
        for(EdocFormAcl acl : edocForm.getEdocFormAcls()) {
            V3xOrgAccount acc = orgManager.getAccountById(acl.getDomainId());
            if(acc.isGroup()) { // 授权给集团的给集团下面所有的单位都添加info对象
                List<V3xOrgAccount> accounts = orgManager.getAllAccounts();
                for(V3xOrgAccount account : accounts) {
                    if(account.getId().equals(edocForm.getDomainId()))
                        continue;
                    EdocFormExtendInfo info = new EdocFormExtendInfo();
                    info.setIdIfNew();
                    info.setAccountId(account.getId());
                    if(account.getId().equals(user.getLoginAccount())) {
                        info.setIsDefault(edocForm.getIsDefault());
                        info.setStatus(edocForm.getStatus());
                        info.setOptionFormatSet(optionFormatSet);
                    } else {
                        info.setIsDefault(false);
                        info.setStatus(com.seeyon.v3x.edoc.util.Constants.EDOC_USELESS);
                        info.setOptionFormatSet(defultConfig);
                    }
                    info.setEdocForm(edocForm);
                    infos.add(info);
                }
            } else { // 授权给单位的就单独添加
                if(acc.getId().equals(edocForm.getDomainId()))
                    continue;
                EdocFormExtendInfo info = new EdocFormExtendInfo();
                info.setIdIfNew();
                info.setAccountId(acc.getId());
                if(acc.getId().equals(user.getLoginAccount())) {
                    info.setIsDefault(edocForm.getIsDefault());
                    info.setStatus(edocForm.getStatus());
                    info.setOptionFormatSet(optionFormatSet);
                } else {
                    info.setIsDefault(false);
                    info.setStatus(com.seeyon.v3x.edoc.util.Constants.EDOC_USELESS);
                    info.setOptionFormatSet(defultConfig);
                }
                info.setEdocForm(edocForm);
                infos.add(info);
            }
        }
        // 本单位的单独添加。不管有没有授权都添加
        EdocFormExtendInfo info = new EdocFormExtendInfo();
        info.setIdIfNew();
        info.setAccountId(user.getLoginAccount());
        info.setIsDefault(edocForm.getIsDefault());
        info.setStatus(edocForm.getStatus());
        info.setEdocForm(edocForm);
        info.setOptionFormatSet(optionFormatSet);
        infos.add(info);
        return infos;
    }

    /**
     * 新建公文单的时候绑定公文单上的意见元素和节点权限
     * @param process_name ：意见元素名称
     * @param flowperm_name ：节点权限名称(例如:zhihui)
     * @param flowperm_label ：节点权限名称Lable(例如：知会)
     * @param sortType ：排序方式
     * @param edocForm ：公文单
     * @param accountId :单位ID，如果是指定单位的，则增加指定单位下面的绑定，否则给所有授权对象下面增加绑定
     *            1、新建公文单的时候，给所有授权单位添加绑定对象
     *            2、公文单制作单位修改公文单的时候，由于可以修改授权，所以需要给新增加的授权单位增加绑定对象
     *            3、外单位修改授权公文单的时候，由于不能修改公文单授权，所以不需要添加绑定对象。
     *            4、列表中授权的时候需要相应的增加或者删除绑定对象。
     */
    private void boundEdocFormAndFlowPerm(String process_name, String flowperm_name, String flowperm_label, String sortType, EdocForm edocForm) {
        try {
            Set<EdocFormAcl> acls = edocForm.getEdocFormAcls();
            for(EdocFormAcl acl : acls) {
                V3xOrgAccount acc = orgManager.getAccountById(acl.getDomainId());
                if(acc.isGroup()) {
                    List<V3xOrgAccount> accounts = orgManager.getAllAccounts();
                    for(V3xOrgAccount account : accounts) {
                        boundOneSpecialAccount(process_name, flowperm_name, flowperm_label, sortType, edocForm, account.getId());
                    }
                } else {
                    boundOneSpecialAccount(process_name, flowperm_name, flowperm_label, sortType, edocForm, acl.getDomainId());
                }
            }
            // 没有给自己授权，也要添加绑定
            if(!isIncludeCurrentAccount(edocForm.getDomainId(), acls)) {
                boundOneSpecialAccount(process_name, flowperm_name, flowperm_label, sortType, edocForm, edocForm.getDomainId());
            }
        } catch(Exception e) {
            LOGGER.error("绑定公文单意见元素和节点权限异常", e);
        }
    }

    /**
     * @param cAccount
     * @param transAccount
     * @return
     */
    private boolean isIncludeCurrentAccount(Long cAccount, Set<EdocFormAcl> acl) {
        for(EdocFormAcl a : acl) {
            V3xOrgAccount acc;
            try {
                acc = orgManager.getAccountById(a.getDomainId());
                if(acc.isGroup())
                    return true;
            } catch(BusinessException e) {
                LOGGER.error("判断单位是否为集团时抛出异常",e);
            }
            if(a.getDomainId().equals(cAccount))
                return true;
        }
        return false;
    }

    /**
     * 绑定一个指定单位的节点权限
     */
    private void boundOneSpecialAccount(String process_name, String flowperm_name, String flowperm_label, String sortType, EdocForm edocForm, Long accountId) throws Exception {
        if(edocForm.getDomainId().longValue() == accountId.longValue()) {
            edocFormManager.bound(process_name, flowperm_name, flowperm_label, edocForm.getId(), sortType, accountId);
        } else {
            // 1、授权给外单位的时候只绑定系统节点权限
            // 2、被授权单位修改绑定的时候可以绑定任意的节点权限。
            String category = "";
            Integer type=edocForm.getType();
            if(null != type) {
                if(type== EdocEnum.edocType.sendEdoc.ordinal()) {
                    category = MetadataNameEnum.edoc_send_permission_policy.name();
                } else if(type == EdocEnum.edocType.recEdoc.ordinal()) {
                    category = MetadataNameEnum.edoc_rec_permission_policy.name();
                } else if(type == EdocEnum.edocType.signReport.ordinal()) {
                    category = MetadataNameEnum.edoc_qianbao_permission_policy.name();
                }
            }
             boolean isSystemFlowPerm = permissionManager.isSystemPermission(category,flowperm_name, accountId);
             if(isSystemFlowPerm) {
                edocFormManager.bound(process_name, flowperm_name, flowperm_label, edocForm.getId(), sortType, accountId);
             }
        }
    }
    /**
     * 为指定单位添加公文单意见绑定
     * @param edocForm
     * @param accountId
     * @throws Exception
     */
    private void boundOtherSpecialAccount(EdocForm edocForm, Long accountId) throws Exception {
        FormBoundPerm otherOpinionPerm = null;
        List<FormBoundPerm> boundPermList = new ArrayList<FormBoundPerm>();
        String value = "";
        String processName = "";
        String processItemName = "";
        String sortType = "0";
        String category = "";
        Integer type=edocForm.getType();
        if(null != type) {
            if(type== EdocEnum.edocType.sendEdoc.ordinal()) {
                category = MetadataNameEnum.edoc_send_permission_policy.name();
            } else if(type == EdocEnum.edocType.recEdoc.ordinal()) {
                category = MetadataNameEnum.edoc_rec_permission_policy.name();
            } else if(type == EdocEnum.edocType.signReport.ordinal()) {
                category = MetadataNameEnum.edoc_qianbao_permission_policy.name();
            }
        }
        Set<EdocFormElement> fieldSet = edocForm.getEdocFormElements();
        Iterator<EdocFormElement> it = fieldSet.iterator();
        while(it.hasNext()) {
            EdocFormElement formElement = it.next();
            String elementId = String.valueOf(formElement.getElementId());
            if(formElement.getElementId()<10) {
                elementId = "00"+elementId;
            } else if(formElement.getElementId()<100) {
                elementId = "0"+elementId;
            }
            EdocElement ele = edocElementManager.getEdocElement(elementId);
            if(ele == null || ele.getType() != EdocElement.C_iElementType_Comment){
                continue;
            } 
            String fieldName = ele.getFieldName();
            if(ele.getName() != null) {
                if(ele.getIsSystem() == true){
                    value = ResourceUtil.getString(ele.getName());
                    processName = value;
                    processItemName = ele.getFieldName();
                    Permission f=permissionManager.getPermission(category, ele.getFieldName(), accountId);
                    if(f==null){
                        processName = "";
                        processItemName = "";
                    }
                }else{
                    value = ele.getName();
                    processName = "";
                    processItemName = "";
                }
            }
            FormBoundPerm formBoundPerm = new FormBoundPerm();
            formBoundPerm.setPermItem(ele.getFieldName());
            formBoundPerm.setPermName(value);
            if("otherOpinion".equalsIgnoreCase(fieldName)) {
                otherOpinionPerm = formBoundPerm;
                continue;
            }
            formBoundPerm.setPermItemName(processItemName);
            formBoundPerm.setProcessName(processName);
            formBoundPerm.setProcessItemName(processItemName);
            boundPermList.add(formBoundPerm);
        }
        if(null != otherOpinionPerm) {
            boundPermList.add(otherOpinionPerm);//把处理意见加到最后
        }
        for(FormBoundPerm formBoundPerm : boundPermList) {
            boolean isSystemFlowPerm = permissionManager.isSystemPermission(category, formBoundPerm.getPermItem(), accountId);
                if(isSystemFlowPerm) {
                    edocFormManager.bound(formBoundPerm.getPermItem(), formBoundPerm.getProcessItemName(), formBoundPerm.getProcessName(), edocForm.getId(), sortType, accountId);
                }
        }
    }
    /**
     * 将前端传过来的授权信息参数(auth)转化为授权对象集合
     * @param loginAccountId
     * @param grantedDomainId
     * @param edocForm
     * @return
     */
    private Set<EdocFormAcl> getEdocFormAclSetForCurrentForm(long loginAccountId, String grantedDomainId, EdocForm edocForm) {
        // 授权
        Set<EdocFormAcl> edocFormAcls = new HashSet<EdocFormAcl>();
        if(Strings.isNotBlank(grantedDomainId)) {
            String[] domainIds = grantedDomainId.split(",");
            for(String domainId : domainIds) {
                EdocFormAcl acl = new EdocFormAcl();
                acl.setIdIfNew();
                String[] domainArr = domainId.split("\\|");
                acl.setDomainId(Long.parseLong(domainArr[1]));
                acl.setEntityType(domainArr[0]);
                acl.setFormId(edocForm.getId());
                edocFormAcls.add(acl);
            }
        }
        return edocFormAcls;
    }
    @CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
    public ModelAndView newForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("edoc/formManage/new_edoc_form");
        String type = request.getParameter("type");
        mav.addObject("operType", "add");
        // 授权给自己单位，用来回显数据
        List<EdocFormAcl> list = new ArrayList<EdocFormAcl>();
        EdocFormAcl acl = new EdocFormAcl();
        acl.setDomainId(AppContext.getCurrentUser().getLoginAccount());
        acl.setEntityType(V3xOrgEntity.ORGENT_TYPE_ACCOUNT);
        list.add(acl);
        mav.addObject("elements", list);

        //公文单默认配置情况
        String defualtConfig = FormOpinionConfig.getDefualtConfig();
        mav.addObject("formConfigJSON", defualtConfig);
        
        if("0".equals(type)) {
            //发文增加分类、、
            List<EdocCategory> categories = this.edocCategoryManager.getCategoryByRoot(Long.valueOf(ApplicationCategoryEnum.edocSend.getKey()), AppContext.getCurrentUser().getLoginAccount(),false);
            mav.addObject("categories", categories);
        }
        return mav.addObject("type", type);
    }
    @CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
    public ModelAndView uploadForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setCharacterEncoding("UTF-8");
        User user = AppContext.getCurrentUser();
        String name = request.getParameter("name");
        String type = request.getParameter("type"); // 公文的类型
        String status = request.getParameter("status");
        String isDefault = request.getParameter("isDefault");
        String description = request.getParameter("description");
        String showLog = request.getParameter("showLog");
        String edocFormStatusId = request.getParameter("edocFormStatusId");
        String method = request.getParameter("method_type");
        String operationStr = request.getParameter("operationStr");
        ModelAndView mav = null;
        
        if("change".equals(method)) {
            
            //是否显示单位标识 暂缺
            mav = new ModelAndView("edoc/formManage/form_modify");
            String id = request.getParameter("id");
            EdocForm edocForm = edocFormManager.getEdocForm(Long.valueOf(id));
            StringBuilder aclIds = new StringBuilder();
            Set<EdocFormAcl> aclList = edocForm.getEdocFormAcls();
            if(aclList != null) {
                for(EdocFormAcl acl : aclList) {
                    if(aclIds.length() > 0){
                        aclIds.append(",");
                    }
                    aclIds.append(acl.getDomainId());
                }
            }
            Set<EdocFormExtendInfo> info = edocForm.getEdocFormExtendInfo();
            if(info != null && !info.isEmpty()){
                for(EdocFormExtendInfo e:info){
                    if(e.getAccountId().equals(edocForm.getDomainId())){
                        edocForm.setIsDefault(e.getIsDefault());
                        
                        //公文配置详细，和新建保持一致
                        String defualtConfig = e.getOptionFormatSet();
                        mav.addObject("formConfigJSON", defualtConfig);
                    }
                }
            }
            mav.addObject("aclIds", aclIds);
            mav.addObject("formId", id);
            mav.addObject("bean", edocForm);
            mav.addObject("method_type", "change");
        } else {
            // 授权给自己单位，用来回显数据
            List<EdocFormAcl> list = new ArrayList<EdocFormAcl>();
            EdocFormAcl acl = new EdocFormAcl();
            acl.setDomainId(user.getLoginAccount());
            acl.setEntityType(V3xOrgEntity.ORGENT_TYPE_ACCOUNT);
            list.add(acl);
            mav = new ModelAndView("edoc/formManage/new_edoc_form");
            mav.addObject("elements", list);
            
            //公文单默认配置情况
            String defualtConfig = FormOpinionConfig.getDefualtConfig();
            mav.addObject("formConfigJSON", defualtConfig);
        }
        mav.addObject("edocFormStatusId", edocFormStatusId);
        String att_fileUrl = request.getParameter("att_fileUrl");
        String att_createDate = request.getParameter("att_createDate");
        String att_mimeType = request.getParameter("att_mimeType");
        String att_fileName = request.getParameter("att_filename");
        String att_needClone = request.getParameter("att_needClone");
        String att_description = request.getParameter("att_description");
        String att_type = request.getParameter("att_type");
        String att_size = request.getParameter("att_size");
        mav.addObject("att_fileUrl", att_fileUrl);
        mav.addObject("att_createDate", att_createDate);
        mav.addObject("att_mimeType", att_mimeType);
        mav.addObject("att_filename", att_fileName);
        mav.addObject("att_needClone", att_needClone);
        mav.addObject("att_description", att_description);
        mav.addObject("att_type", att_type);
        mav.addObject("att_size", att_size);
        String[] urls = (String[])request.getParameterValues("fileUrl");
        String[] createDates = (String[])request.getParameterValues("fileCreateDate");
        String[] mimeTypes = (String[])request.getParameterValues("fileMimeType");
        String[] names = (String[])request.getParameterValues("filename");
        if(urls == null) {
            return mav;
        }
        // 前台页面展示，下载公文单的时候使用
        if(urls.length > 0 && createDates != null && createDates.length > 0 && names != null && names.length > 0) {
            mav.addObject("fileId", urls[0]);
            mav.addObject("fileName", names[0]);
            mav.addObject("createDate", createDates[0].substring(0, 10));
        }
        
        // 对存储文件处理 生成.xsn文件，并返回存储路径
        String path = edocFormManager.getDirectory(urls, createDates, mimeTypes, names);
        String xml = "";
        String xsl = "";
        
        try {
            String[] xsnXMLAndXSL = EdocFormHelper.parseInfoPathXMLAndXSL(path);
            xml = xsnXMLAndXSL[0];
            xsl = xsnXMLAndXSL[1];
        } catch (Exception e) {
        	LOGGER.error("通过xsn文件的物理路径计息xml和xsl方法报错:", e);
            return new ModelAndView("edoc/refreshWindow").addObject("windowObj","parent");
        }
        
        mav.addObject("original_xml", request.getParameter("original_xml"));
        
        // 取出所有fieldName
        try {
            int a = xml.indexOf(">");
            int c = xml.indexOf("</my:myFields>");
            xml = xml.substring(a + 1, c);
        } catch(Exception e) {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            LOGGER.error("infopath校验错误,请检查是否包含正确的公文元素字段", e);
            out.println("<script>");
            out.println("alert(parent._('edocLang.edoc_form_infopath_error'));");
            // out.println("alert('infopath校验错误,请检查是否包含正确的公文元素字段! ');");
            out.println("setTimeout(function(){parent.location.href = parent.location.href;}, 10)");
            out.println("</script>");
            return null;
            // return super.refreshWindow("parent");
        }
        List<String> list = new ArrayList<String>();
        String[] str = xml.split("/>");
        for(int i = 0; i < str.length - 1; i++) {
            String str_a = str[i];
            if(str_a.contains("<my:")) {// 判断是否事以<my:开始,因为infopath2007与2003在样式上略又区别
                int x = str_a.indexOf(":");
                str_a = str_a.substring(x + 1, str_a.length());
                list.add(str_a);
            }
        }
        
        if(Strings.isEmpty(list)) {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            LOGGER.error("公文单中没有发现正确的域名字段,请重新设计公文单");
            out.println("<script>");
            out.println("alert(parent._('edocLang.edoc_form_no_such_field'));");
            out.println("setTimeout(function(){parent.location.href = parent.location.href;}, 10)");
            // out.println("alert('公文单中没有发现正确的域名字段,请重新设计公文单');");
            out.println("</script>");
            return null;
        } else {// 判断是否有标题
            boolean bool = list.contains("subject");
            if(!bool) {
                response.setContentType("text/html;charset=UTF-8");
                PrintWriter out = response.getWriter();
                LOGGER.error("公文单中必须包含标题字段!请重新设计公文单");
                out.println("<script>");
                out.println("alert(parent._('edocLang.edoc_form_subject_must'));");
                out.println("setTimeout(function(){parent.location.href = parent.location.href;}, 10)");
                // out.println("alert('公文单中没有发现正确的域名字段,请重新设计公文单');");
                out.println("</script>");
                return null;
                // return super.refreshWindow("parent");
            }
        }
        // 取出所有元素的fieldName
        List<EdocElement> elements = edocElementManager.getEdocElementsByStatus(1, 1, 10000);
        // List<EdocElement> elements = edocElementManager.getAllEdocElements(1,
        // 10000);
        List<String> eleList = new ArrayList<String>();
        List<EdocFormElement> eles = new ArrayList<EdocFormElement>();
        //List<String> boundList = new ArrayList<String>();
        for(EdocElement ele : elements) {
            // if(ele.getType()==6)continue;
            eleList.add(ele.getFieldName().toLowerCase());
            String temp_s = "";
            if(ele.getIsSystem()) {// 如果是系统元素，取国际化值
                temp_s = ResourceUtil.getString(ele.getName());
            } else {
                temp_s = ele.getName();
            }
            
            //拼成 元素得域名+国际化名， 例如:(banli|办理)
            //boundList.add(ele.getFieldName().toLowerCase() + "|" + temp_s); 
        }
        // zhanghua:asking otherOption
        eleList.add("otheropinion");
        List<String> configList_first = com.seeyon.v3x.edoc.util.StringUtils.findEdocElementFromConfig(type);
        eleList.addAll(configList_first);
        for(String s : list) {
            if(!"".equals(s) && !eleList.contains(s.toLowerCase())) {
                
                String alertMsg = null;
                
                if("change".equals(method)) {
                    LOGGER.error("公文单只允许对样式进行修改,不能修改公文元素");
                    
                    //公文单只允许对样式进行修改,不能修改公文元素
                    alertMsg = "'edocLang.edoc_form_field_forbiddend'";
                    // return super.refreshWindow("parent");
                } else {
                    LOGGER.error("'输入的字段域名不正确! 没有域名为 : [" + s + "] 的元素  或此元素没有被启用");
                    String  ss= (s != null  && s.indexOf(">") != -1 ? s.substring(0, s.indexOf(">")) : s) ;
                    
                    //字段域名校验错误! 没有域名为 : ["+s+"] 的元素  或此元素没有被启用
                    alertMsg = "'edocLang.edoc_form_field_error','"+ss+"'";
                    // out.println("alert('字段域名校验错误! 没有域名为 : ["+s+"] 的元素  或此元素没有被启用 ');");
                    
                    // return super.refreshWindow("parent");
                }
                
                response.setContentType("text/html;charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.println("<script>");
                out.println("alert(parent._(" + alertMsg + "));");
                out.println("setTimeout(function(){parent.location.href = parent.location.href;}, 10)");
                out.println("</script>");
                return null;
                
            }
//            else{
//                //查看xsl中是否有相关的元素
//                String bindEle = "xd:binding=\"my:"+s+"\"";
//                if(xsl.indexOf(bindEle) == -1){
//                    
//                    response.setContentType("text/html;charset=UTF-8");
//                    PrintWriter out = response.getWriter();
//                    out.println("<script>");
//                    
//                    String  ss = (s != null  && s.indexOf(">") != -1 ? s.substring(0, s.indexOf(">")) : s) ;
//                    
//                    //字段域名校验错误! 域名为 : ['{0}'] 的元素没有相应的输入框，请重新编辑infopath文件。
//                    String alertMsg = "'edocLang.edoc_form_field_error1','"+ss+"'";
//                    
//                    out.println("alert(parent._(" + alertMsg + "));");
//                    out.println("setTimeout(function(){parent.location.href = parent.location.href;}, 10)");
//                    out.println("</script>");
//                    return null;
//                }
//            }
        }
        

        // Permission flowPerm = null;
        //if(Integer.valueOf(type).intValue()==1) {         //获取分发权限对象
       //   flowPerm = permissionManager.getPermission(MetadataNameEnum.edoc_rec_permission_policy.name(), "dengji", user.getLoginAccount());
       // } else {//获取拟文权限对象
       //   flowPerm = permissionManager.getPermission(MetadataNameEnum.edoc_send_permission_policy.name(), "niwen", user.getLoginAccount());
        //}
        // 获取拟文权限可以设置权限的公文元素
       // List<EdocElementFlowPermAcl> acl_list = edocElementFlowPermAclManager.getEdocElementFlowPermAcls(flowPerm.getFlowPermId());   

        List<EdocFormElement> formElements = new ArrayList<EdocFormElement>();
        for(String fieldName : list) {
           //找出在文单中有编辑权限的公文元素
           // for(EdocElementFlowPermAcl fAcl : acl_list) {
                // 判断是否能编辑
                //if(fAcl.getEdocElement().getFieldName().equals(fieldName)){
                    EdocElement ele = edocElementManager.getByFieldName(fieldName);
                    if(ele!=null && ele.getType()!=EdocElement.C_iElementType_Comment && ele.getType()!=EdocElement.C_iElementType_LogoImg){
                         EdocFormElement formElement = new EdocFormElement();
                         formElement.setElementId(Long.parseLong(ele.getElementId()));
                         formElement.setElementName(ele.getName());
                         formElement.setSystemType(ele.getIsSystem());
                         formElement.setAccess(true);
                         
                         //判断前台设置的必填项设置
                         String onCheck = request.getParameter(formElement.getElementId().toString());
                         if("on".equals(onCheck)){
                             formElement.setRequired(true);
                         }else{
                             formElement.setRequired(false);
                         }
                         
                         formElements.add(formElement);
                     }

               // }
           // }
        }
        mav.addObject("formElements", formElements);
        String method_type = request.getParameter("method_type");
        if(null != method_type && "change".equals(method_type)) {
            List<String> configList = com.seeyon.v3x.edoc.util.StringUtils.findEdocElementFromConfig(type);
            String original_xml = request.getParameter("original_xml");
            String mx = request.getParameter("mx");
            List<String> orgList = new ArrayList<String>();
            String[] strOrg = original_xml.split("\\|");
            for(String so : strOrg) {
                orgList.add(so.toLowerCase());
            }
            orgList.add("otheropinion");
            orgList.addAll(configList);
            // BUG23044
            // 修改文单时候允许调整意见元素
            for(EdocElement ele : elements) {
                if(ele.getType() == EdocElement.C_iElementType_Comment) {
                    orgList.add(ele.getFieldName().toLowerCase());
                }
            }
            //List<String> xslList = new ArrayList<String>();
            String[] strt = mx.split("\\|");
            for(int i = 0; i < strt.length; i++) {
                int x = strt[i].indexOf(":");
                //xslList.add(strt[i].substring(x + 1, strt[i].length()));
            }
            for(String st : list) {
                if(!orgList.contains(st.toLowerCase())) {
                    response.setContentType("text/html;charset=UTF-8");
                    PrintWriter out = response.getWriter();
                    // 上传文单时拼成的xml中没有logoimg，在此取消对其判断
                    if("logoimg".equals(st.toLowerCase()))
                        continue;
                    LOGGER.error("字段域名不匹配 [" + st + "]");
                    out.println("<script>");
                    out.println("alert(parent._('edocLang.edoc_form_field_not_match','" + st + "'));");
                    // out.println("alert('字段域名不匹配 ["+st+"]');");
                    out.println("</script>");
                    out.flush();
                    return new ModelAndView("edoc/refreshWindow").addObject("windowObj","parent");
                }
            }
        }
        StringBuilder element_id_list = new StringBuilder();
        for(String s_a : list) {
            EdocFormElement formElement = new EdocFormElement();
            long id = 0;
            try {
                id = edocElementManager.getIdByFieldName(s_a);
            } catch(Exception e) {
                //TODO 没有做异常处理
            }
            formElement.setElementId(id);
            eles.add(formElement);
            element_id_list.append(id)
                           .append(",");
        }
        EdocSummary edocSummary = null;
        long actorId = -1;
        String result = xmlConverter.convert(eles, edocSummary, actorId, Integer.valueOf(type).intValue()).toString();
        // String result = XMLConverter.uploadXMLConvert(list,
        // "<my:myFields xmlns:my=\"www.seeyon.com/form/2007\">"+temp);
        byte[] tempByte_a = CharReplace.doReplace_Encode(xsl.getBytes("UTF-8"));
        xsl = new String(tempByte_a, "UTF-8");
        byte[] tempByte_b = CharReplace.doReplace_Decode(xsl.getBytes("UTF-8"));
        mav.addObject("xml", result);
        xsl = new String(tempByte_b, "UTF-8");
        //&符号转义-BUG_普通_V5_V5.1sp1_致远客服部_公文单有"<"，在导入的时候会提示“公文单数据出现异常！错误原因：解析XML失败”_20150626010068.在seeyonForm3.js有对应转换。搜bug编号
        xsl = xsl.replaceAll("&", "&amp;");
        mav.addObject("xsl", xsl);
        mav.addObject("tempList", list);
        mav.addObject("element_id_list", element_id_list.toString());
        mav.addObject("isDefault", isDefault);
        mav.addObject("type", type);
        mav.addObject("status", status);
        mav.addObject("name", name);
        mav.addObject("description", description);
        mav.addObject("showLog", showLog);
        // 取得公文处理意见的名称列表
        List<FormBoundPerm> processList = null;
        if(!"".equals(method) && "change".equals(method)) {
            String id = request.getParameter("id");
            EdocForm edocForm = edocFormManager.getEdocForm(Long.valueOf(id));
            if(null != edocForm) {
                processList = EdocHelper.getProcessOpinionByEdocFormId(list, edocForm.getId(), edocForm.getType(), user.getLoginAccount(), true);
            }
        } else {
            processList = EdocHelper.getProcessOpinionFromEdocForm(list, Integer.valueOf(type), AppContext.getCurrentUser().getLoginAccount());
        }
        mav.addObject("processList", processList);
        // 用于提交选择参数
        StringBuilder listStr = new StringBuilder();
        for(FormBoundPerm perm : processList) {
            if(listStr.length() > 0){
                listStr.append(",");
            }
            listStr.append(perm.getPermItem());
        }
        mav.addObject("listStr", listStr.toString());
        //
        String category = "";
        if(null != type && !"".equals(type)) {
            if(Integer.valueOf(type).intValue() == EdocEnum.edocType.sendEdoc.ordinal()) {
                category = MetadataNameEnum.edoc_send_permission_policy.name();
            } else if(Integer.valueOf(type).intValue() == EdocEnum.edocType.recEdoc.ordinal()) {
                category = MetadataNameEnum.edoc_rec_permission_policy.name();
            } else if(Integer.valueOf(type).intValue() == EdocEnum.edocType.signReport.ordinal()) {
                category = MetadataNameEnum.edoc_qianbao_permission_policy.name();
            }
        }
        List<PermissionVO> flowPermlist = permissionManager.getPermission(category, Permission.Node_isActive, user.getLoginAccount());
        mav.addObject("flowPermList", flowPermlist);
        String operation_str = "";
        if(Strings.isNotBlank(operationStr)){
        	operation_str = operationStr;
        }else{
        	operation_str = EdocHelper.getProcessOpinionFromEdocFormOperation(list, Integer.valueOf(type));
        }
        mav.addObject("operation_str", operation_str);
        String logoURL = EdocHelper.getLogoURL();
        mav.addObject("logoURL", logoURL);

        if("0".equals(type)) {
            mav.addObject("categories", this.edocCategoryManager.getCategoryByRoot(Long.valueOf(ApplicationCategoryEnum.edocSend.getKey()), AppContext.getCurrentUser().getLoginAccount(),false));
        }
        return mav;
    }
    @CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
    public ModelAndView delete(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter print = response.getWriter();
        String id = request.getParameter("id");
        String[] ids = id.split(",");
        User user = AppContext.getCurrentUser();
        for(int i = 0; i < ids.length; i++) {
            // 记录应用日志
            EdocForm form = edocFormManager.getEdocForm(Long.valueOf(ids[i]));
            appLogManager.insertLog(user, AppLogAction.Edoc_Form_Delete, user.getName(), form.getName());
            String str = edocFormManager.deleteForm(Long.valueOf(ids[i]));
            if(null != str && !"".equals(str)) {
                LOGGER.error(str);
                return super.refreshWindow("parent", str);
            }
        }
        return new ModelAndView("edoc/refreshWindow").addObject("windowObj","parent");
    }
    @CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
    public ModelAndView change(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        // Long accountId = user.getLoginAccount();
        // String configItem_accountId = String.valueOf(accountId);
        // String logoFileName = "logo.gif";
        // String replacement =
        // "<xsl:template match=\"my:myFields\"><div align=\"left\"><img src=\"/seeyon/apps_res/v3xmain/images/"+logoFileName+"\" /></div>";
        String name = request.getParameter("name");
        // String type = request.getParameter("type");
        String id = request.getParameter("id");
        String statusId = request.getParameter("edocFormStatusId");
        String status = request.getParameter("status");
        String description = request.getParameter("description");
        String isDefault = request.getParameter("isDefault");
        String showLog = request.getParameter("showLog");
        String content = request.getParameter("content");
        String grantedDomainId = request.getParameter("grantedDomainId");
        EdocFormExtendInfo info = null;
        long subType = -1;
        if(Strings.isNotBlank(statusId)) {
            info = edocFormManager.getEdocFormExtendInfo(Long.valueOf(statusId));
            if(info!=null) {
                if(info.getEdocForm().getSubType()!=null){
                    subType = info.getEdocForm().getSubType();//旧的分类
                }
            }
        }
        EdocForm eForm = edocFormManager.getEdocForm(Long.valueOf(id));
        Set<EdocFormExtendInfo> oldInfos=eForm.getEdocFormExtendInfo();
        List<Long> oldAccs=new ArrayList<Long>();
        for(EdocFormExtendInfo tempinfo:oldInfos){
            oldAccs.add(tempinfo.getAccountId());
        }
        // if(info == null) {
        // info = new EdocFormExtendInfo();
        // info.setIdIfNew();
        // info.setEdocForm(eForm);
        // info.setAccountId(user.getLoginAccount());
        // }
        eForm.setName(name);
        
        boolean flag = false;
        Long subTypeNew = null;
        if(eForm.getSubType()!=null){
            subTypeNew = eForm.getSubType();//新的分类
        }
        String edocCategory = request.getParameter("edocCategory");
        if(Strings.isNotBlank(edocCategory)) {
            subTypeNew = Long.parseLong(edocCategory);
        }
        if(!Long.valueOf(subType).equals(subTypeNew) && Integer.valueOf(0).equals(eForm.getType())) {
            flag = true;
        } 
        eForm.setStatus(Integer.valueOf(status));
        if(null != content && !"".equals(content)) {
            byte[] tempByte_a = CharReplace.doReplace_Encode(content.getBytes("UTF-8"));
            content = new String(tempByte_a, "UTF-8");
        }
        
        if(Strings.isBlank(content)){//前台传过来的xslt内容为空，进行提示处理
            request.setAttribute("jsScript", "alert('" + ResourceUtil.getString("edoc.alert.unknownError")+ "')");//未知异常，请重试!
        }else{
            eForm.setContent(content);
            eForm.setDescription(description);
            eForm.setShowLog("1".equals(showLog));
            eForm.setIsDefault(false);
            if(info != null) { 
                if(null != isDefault && "1".equals(isDefault)) {
                    // if the original form has been the default form then change
                    // it's isDefault stauts is unnecessary.
                    eForm.setIsDefault(true);
                    boolean hasSubType=EdocHelper.hasEdocCategory()&&EdocHelper.isG6Version();//G6版本，并且开启了公文种类开关的时候才有hasSubType
                    if(!info.getIsDefault() || flag) {
                        edocFormManager.updateDefaultEdocForm(user.getLoginAccount(), eForm.getType(), subTypeNew,hasSubType);
                        edocFormManager.setDefaultEdocForm(user.getLoginAccount(), eForm.getType(), eForm);
                        NotificationManager.getInstance().send(NotificationType.DefaultEdocFormReSet, new Object[]{eForm.getId(), user.getLoginAccount()});
                    }
                } else {
                    // info.setIsDefault(false);
                    eForm.setIsDefault(false);
                }
            }
            // info.setStatus(eForm.getStatus());
            String[] att_fileUrl = request.getParameterValues("att_fileUrl");
            if(null != att_fileUrl && att_fileUrl.length > 0) {
                if(!"".equals(att_fileUrl[0])) {
                    eForm.setFileId(Long.parseLong(att_fileUrl[0]));
                    attachmentManager.update(ApplicationCategoryEnum.edoc, eForm.getId(), eForm.getId(), request);
                }
            }
            boolean isOuterAccountAcl = !eForm.getDomainId().equals(user.getLoginAccount());
            if(Strings.isNotBlank(edocCategory)) {
                eForm.setSubType(Long.parseLong(edocCategory));
            }else if(!isOuterAccountAcl){                 //外单位授权文单不修改发文种类
                eForm.setSubType(null);
            }
            
            //元素更新
            List<Long> newElementIdList = dealEleList(request.getParameter("element_id_list"));
            
            
            
            /******************************* 保存 文单必填项设置信息*****************************/
            if(eForm.getType()==0||eForm.getType()==1||eForm.getType()==2) {
                
                if(Strings.isNotEmpty(newElementIdList)){
                    //OA-109125【客户Buglist验证】把之前公文单的公文元素删除重新上传保存后还能看到删除的公文元素
                    eForm.getEdocFormElements().clear();;
                    //删除原有的元素
                    edocFormManager.deleteFormElementByFormId(eForm.getId());
                    
                    Set<EdocFormElement> eles = edocFormManager.saveEdocXmlData(eForm.getId(), newElementIdList);
                    eForm.setEdocFormElements(eles);
                }
                
                //Permission flowPerm = null;
                //if(eForm.getType()==1) {            //获取分发权限对象
                //    flowPerm = permissionManager.getPermission(MetadataNameEnum.edoc_rec_permission_policy.name(), "dengji", user.getLoginAccount());
                //} else {//获取拟文权限对象
                //    flowPerm = permissionManager.getPermission(MetadataNameEnum.edoc_send_permission_policy.name(), "niwen", user.getLoginAccount());
               // }
                // 获取拟文权限可以设置权限的公文元素
                //if(flowPerm != null){
                //List<EdocElementFlowPermAcl> acl_list = edocElementFlowPermAclManager.getEdocElementFlowPermAcls(flowPerm.getFlowPermId());   
                for(EdocFormElement formElement : eForm.getEdocFormElements()) {
                   //找出在文单中有编辑权限的公文元素
                    EdocElement element = edocElementManager.getEdocElementsById(formElement.getElementId());   
                    //for(EdocElementFlowPermAcl fAcl : acl_list) {
                        // 判断是否能编辑
                        if(element != null&& element.getType()!=EdocElement.C_iElementType_Comment && element.getType()!=EdocElement.C_iElementType_LogoImg){
                           // if(fAcl.getEdocElement().getFieldName().equals(element.getFieldName())&&fAcl.getAccess()==1){
                                String onCheck = request.getParameter(String.valueOf(element.getId()));
                                if("on".equals(onCheck) || formElement.getElementId()==1) {
                                     formElement.setRequired(true);
                                }else{
                                    formElement.setRequired(false);
                                }
                            //}
                        }
                    //}
                }
                //}            
            }
            /******************************* 保存 文单必填项设置信息*****************************/
    
            
            List<EdocFormFlowPermBound> boundPermList = edocFormManager.findBoundByFormId(eForm.getId());
            Set<EdocFormAcl> reqAcl = getEdocFormAclSetForCurrentForm(user.getLoginAccount(), grantedDomainId, eForm);
            if(null != boundPermList) {
            	List<String> flowperm_nameList = new ArrayList<String>();
                // 首先删除该公文单下得绑定对象
                edocFormManager.deleteEdocFormFlowPermBoundByFormIdAndAccountId(eForm.getId(), user.getLoginAccount());
                String tempS = request.getParameter("listStr");
                if(null != tempS && !"".equals(tempS)) {
                    String[] tempArray = tempS.split(",");
                    List<Long> elementIdList=new ArrayList<Long>();
                    for(String process_name : tempArray) {
                        String flowperm_name = request.getParameter("returnOperation_" + process_name);
                        String flowperm_label = request.getParameter(process_name);
                        String sortType = request.getParameter("sortType_" + process_name);
                        long elementId=edocElementManager.getIdByFieldName(process_name);
                        List<EdocFormElement> edocFormElementList = edocElementManager.getEdocFormElementByElementIdAndFormId(elementId, eForm.getId());
                        if(edocFormElementList.size()==0){
                        	elementIdList.add(elementId);
                        }
                        // 修改的时候自己单位肯定是要添加非系统节点权限的。
                        edocFormManager.bound(process_name, flowperm_name, flowperm_label, eForm.getId(), sortType, user.getLoginAccount());
                        flowperm_nameList.add(flowperm_name);
                        // 增加
                        List<Long> addAcl = getCompareEdocFormAclsAdd(eForm.getEdocFormAcls(), reqAcl);
                        if(addAcl != null) {
                        	for(Long accoutId : addAcl){
                        		if(eForm.getDomainId().equals(accoutId) || oldAccs.contains(accoutId))
                        			continue;
                        		boundOneSpecialAccount(process_name, flowperm_name, flowperm_label, sortType, eForm,accoutId);
                        	}
                        }
                    }
                    
                    //BUG_普通_V5_V5.1sp1_云南城投（NC-OA）_公文单增加处理意见后保存，意见丢失_20141016003800_
                    if(elementIdList!=null && elementIdList.size()>0){
                    	this.edocFormManager.saveEdocXmlData(eForm.getId(), elementIdList);
                    }
                }
                //更新节点权限引用情况
                int appEnum = EdocUtil.getApplicationCategoryEnumKeyByEdocType(eForm.getType());
                permissionManager.updatePermissionRef(appEnum,Strings.join(flowperm_nameList, ",") , user.getAccountId());
            }
            
          //文单设置设置保存
            FormOpinionConfig formConfig = parseConfigData(request);
            
            String optionFormatSet = JSONUtil.toJSONString(formConfig);//将配置转换成
            
            Set<EdocFormExtendInfo> infos = getInfos(eForm, reqAcl, user.getLoginAccount(), optionFormatSet, false);
            eForm.getEdocFormExtendInfo().clear();
            eForm.getEdocFormExtendInfo().addAll(infos);
            eForm.getEdocFormAcls().clear();
            eForm.getEdocFormAcls().addAll(reqAcl);
            eForm.setEdocFormFlowPermBound(null);//EdocFormFlowPermBound表前面已经做记录处理了，所以edocform对象里的级联对象设置为空，避免重复插入数据
            eForm.setLastUpdate(new java.sql.Timestamp(System.currentTimeMillis()));
            eForm.setLastUserId(user.getId());
            edocFormManager.updateEdocForm(eForm);
            appLogManager.insertLog(user, AppLogAction.Edoc_Form_Update, user.getName(), eForm.getName());

            // best 文单中添加需要隐藏的部门  start
            try {
            	XkjtManager xkjtManager = (XkjtManager) AppContext.getBean("xkjtManager");
	            String needNotShowDeptId = request.getParameter("needNotShowDeptId");
	            String needNotShowDeptName = request.getParameter("needNotShowDeptName");
				if (xkjtManager != null && eForm != null && eForm.getId() != null
						&& Strings.isNotBlank(needNotShowDeptName) && Strings.isNotBlank(needNotShowDeptId)) {
					EdocFormInfo formInfo = xkjtManager.getByFormId(eForm.getId());
					boolean isNew = false;
					if (formInfo == null) {
						isNew = true;
						formInfo = new EdocFormInfo();
						formInfo.setId(UUIDLong.longUUID());
						formInfo.setFormId(eForm.getId());
					}
					formInfo.setNeedNotShowDeptId(needNotShowDeptId);
					formInfo.setNeedNotShowDeptName(needNotShowDeptName);
					if (isNew) {
						xkjtManager.saveEdocFormInfo(formInfo);
					} else {
						xkjtManager.updateEdocFormInfo(formInfo);
					}
				}
            } catch (Exception e) {
            	logger.error("best新增或修改表单配置不显示部门异常：", e);
            }
            // best 文单中添加需要隐藏的部门  end
            
        }
        return new ModelAndView("edoc/refreshWindow").addObject("windowObj","parent");
    }
    
    
    /**
     * 处理公文单的元素列表
     * 
     * @param eleList
     * @return
     *
     * @Since A8-V5 6.1
     * @Author      : xuqw
     * @Date        : 2016年12月30日下午5:02:49
     *
     */
    private List<Long> dealEleList(String element_id_list){
        List<Long> elementIdList = new ArrayList<Long>();
        if(element_id_list != null){
            element_id_list = element_id_list.substring(0, element_id_list.length());
            String[] eList = element_id_list.split(",");
            for(int i = 0; i < eList.length; i++) {
                if(Strings.isNotBlank(eList[i]) && !"0".equals(eList[i])) {
                    elementIdList.add(Long.valueOf(eList[i]));
                }
            }
        }
        return elementIdList;
    }
    
    
    /**
     * 相比新增加授权的单位id集合
     * @param orginalAcl  原有的文单授权信息
     * @param newAcl  新的文单授权信息
     * @return
     */
    private List<Long> getCompareEdocFormAclsAdd(Set<EdocFormAcl> orginalAcl, Set<EdocFormAcl> newAcl) {
        return getCompareEdocFormAcls(orginalAcl, newAcl).get("add");
    }
    /**
     * 相比取消授权的单位id集合
     * @param orginalAcl :原始的公文单授权
     * @param newAcl ：新的公文单授权
     * @return
     */
    private List<Long> getCompareEdocFormAclsDel(Set<EdocFormAcl> orginalAcl, Set<EdocFormAcl> newAcl) {
        return getCompareEdocFormAcls(orginalAcl, newAcl).get("del");
    }

    /**
     * @param orginalAcl :原始的公文单授权
     * @param newAcl ：新的公文单授权
     * @return
     */
    private Map<String, List<Long>> getCompareEdocFormAcls(Set<EdocFormAcl> orginalAcl, Set<EdocFormAcl> newAcl) {
        if(orginalAcl == null || newAcl == null)
            return new HashMap<String, List<Long>>();
        Map<String, List<Long>> map = new HashMap<String, List<Long>>();
        try {
            Long rootAccountId = orgManager.getRootAccount().getId();
            // 先判断是不是都是集团。避免性能问题
            if(orginalAcl.size() == 1 && newAcl.size() == 1) {
                for(EdocFormAcl acl : orginalAcl) {
                    if(acl.getDomainId().equals(rootAccountId)) {
                        for(EdocFormAcl ac : newAcl) {
                            if(ac.getDomainId().equals(rootAccountId)) {
                                return new HashMap<String, List<Long>>();
                            }
                            break;
                        }
                    }
                    break;
                }
            }
            List<Long> deleteAcl = new ArrayList<Long>();
            List<Long> addAcl = new ArrayList<Long>();
            Set<Long> orginalListL = new HashSet<Long>();
            Set<Long> newListL = new HashSet<Long>();
            for(EdocFormAcl acl : newAcl) {
                if(acl.getDomainId().equals(rootAccountId)) {
                    List<V3xOrgAccount> accounts = orgManager.getAllAccounts();
                    for(V3xOrgAccount account : accounts) {
                        newListL.add(account.getId());
                    }
                } else {
                    newListL.add(acl.getDomainId());
                }
            }
            for(EdocFormAcl acl : orginalAcl) {
                if(acl.getDomainId().equals(rootAccountId)) {
                    List<V3xOrgAccount> accounts = orgManager.getAllAccounts();
                    for(V3xOrgAccount account : accounts) {
                        orginalListL.add(account.getId());
                    }
                } else {
                    orginalListL.add(acl.getDomainId());
                }
            }
            for(Long id : newListL) {
                if(!orginalListL.contains(id))
                    addAcl.add(id);
            }
            for(Long id : orginalListL) {
                if(!newListL.contains(id))
                    deleteAcl.add(id);
            }
            map.put("add", addAcl);
            map.put("del", deleteAcl);
        } catch(Exception e) {
            LOGGER.error("公文单授权抛出异常",e);
        }
        return map;
    }

    /**
     * 取得指定文单的所有EdocFormExtendInfo，包含被取消授权的所有、新增的授权的和已有不变的授权
     * @param ef
     * @param newAcl
     * @param currentAccount
     * @param order
     * @param isOnlyAcl
     * @return
     */
    private Set<EdocFormExtendInfo> getInfos(EdocForm ef, Set<EdocFormAcl> newAcl, Long currentAccount, String order, boolean isOnlyAcl) {
        
        String defualtCOnfig = FormOpinionConfig.getDefualtConfig();//默认配置
        
        Map<Long, EdocFormExtendInfo> map = new HashMap<Long, EdocFormExtendInfo>();
        for(EdocFormExtendInfo info : ef.getEdocFormExtendInfo()) {
            map.put(info.getAccountId(), info);
        } 
        
        boolean isAcl2CAccount = false;//是否授权制作单位，需要保证制作单位始终有一条数据，
        Set<EdocFormExtendInfo> infos = new HashSet<EdocFormExtendInfo>(); 
        try {
            for(EdocFormAcl acl : newAcl) {
                if(acl.getDomainId().equals(ef.getDomainId())) isAcl2CAccount =  true;
                V3xOrgAccount acc = orgManager.getAccountById(acl.getDomainId());
                if(acc.isGroup()) { // 授权给集团
                    List<V3xOrgAccount> accounts = orgManager.getAllAccounts();
                    for(V3xOrgAccount account : accounts) {
                        if(account.getId().equals(currentAccount))
                            continue;
                        if(map.get(account.getId()) != null) { // 有历史info信息
                            infos.add(map.get(account.getId()));
                            map.remove(account.getId());
                        } else {
                            EdocFormExtendInfo info = new EdocFormExtendInfo();
                            info.setIdIfNew();
                            info.setAccountId(account.getId());
                            info.setIsDefault(false);
                            info.setStatus(com.seeyon.v3x.edoc.util.Constants.EDOC_USELESS);
                            info.setEdocForm(ef);
                            if(Strings.isNotBlank(order)) {
                                info.setOptionFormatSet(order);
                            } else {
                                info.setOptionFormatSet(defualtCOnfig);
                            }
                            infos.add(info);
                        }
                    }
                } else {
                    if(acc.getId().equals(currentAccount))
                        continue;
                    if(map.get(acc.getId()) != null) {
                        infos.add(map.get(acc.getId()));
                        map.remove(acc.getId());
                    } else {
                        EdocFormExtendInfo info = new EdocFormExtendInfo();
                        info.setIdIfNew();
                        info.setAccountId(acc.getId());
                        info.setIsDefault(false);
                        info.setStatus(com.seeyon.v3x.edoc.util.Constants.EDOC_USELESS);
                        info.setEdocForm(ef);
                        if(Strings.isNotBlank(order)) {
                            info.setOptionFormatSet(order);
                        } else {
                            info.setOptionFormatSet(defualtCOnfig);
                        }
                        infos.add(info);
                    }
                }
            }
        } catch(BusinessException e) {
            LOGGER.error("改变授权的时候处理info信息抛出异常：",e);
        }
        
        // 当前单位
        EdocFormExtendInfo cinfo = map.get(currentAccount);
        if(!isOnlyAcl) {// 列表中授权的时候没有修改公文单属性.。
            cinfo.setIsDefault(ef.getIsDefault());
            cinfo.setStatus(ef.getStatus());
            if(Strings.isNotBlank(order))
                cinfo.setOptionFormatSet(order);
        }
        if(!currentAccount.equals(ef.getDomainId())
                && !isAcl2CAccount){//制作单位没有被授权，并且不是当前登录单位的时候，手东加一条数据。
            EdocFormExtendInfo ci  = map.get(ef.getDomainId());
            if(ci!=null){
                infos.add(ci);
            }
        }
        infos.add(cinfo);
        map.remove(currentAccount);
        infos.addAll(map.values());
        return infos;
    }

    /**
     * 切换公文单，切换前先保存用户输入的数据，避免输入数据丢失
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @CheckRoleAccess(roleTypes = {Role_NAME.NULL})
    public ModelAndView getEdocFormModel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long formId = Long.parseLong(request.getParameter("edoctable"));
        String strEdocId = request.getParameter("sendEdocId");
        long actorId = -1L;
        EdocSummary edocSummary = new EdocSummary();
        if(Strings.isNotBlank(strEdocId) &&!"-1".equals(strEdocId)) {
            EdocSummary edocSummaryInDataSource = edocManager.getEdocSummaryById(Long.parseLong(strEdocId), false);

            if(edocSummaryInDataSource!=null){
                edocSummary = (EdocSummary)edocSummaryInDataSource.clone();
                edocSummary.setEdocBodies(null);
                edocSummary.setEdocOpinions(null);
                
                //如果是G6登记过来的，先进行登记数据赋值
                String registerId = request.getParameter("registerId");
                if(Strings.isNotBlank(registerId)){
                    EdocRegister edocRegister = edocRegisterManager.findRegisterById(Long.parseLong(registerId));
                    
                    if(edocRegister != null){

                        //从登记中获取登记数据
                        EdocHelper.copyEdocSummaryFromRegister(edocSummary, edocRegister);
                        
                        String unit = edocRegister.getEdocUnit();
                        if(unit != null){//与newEdocHandle.java保持一致
                            edocSummary.setSendUnit(unit);
                            edocSummary.setSendUnitId(edocRegister.getEdocUnitId());
                        }
                         
                        edocSummary.setReportTo(null);
                        edocSummary.setReportToId(null);
                        edocSummary.setSendTo2(null);
                        edocSummary.setSendToId2(null);
                        edocSummary.setCopyTo2(null);
                        edocSummary.setCopyToId2(null);
                        edocSummary.setReportTo2(null);
                        edocSummary.setReportToId2(null);
                        
                        /*
                        //主送和抄送数据按照发文单赋值
                        Long receiveId = edocRegister.getRecieveId();
                        if(receiveId != null && receiveId.longValue() != 0 && receiveId.longValue() != -1){
                            
                            String sendToId = null;
                            String sendToNames = null;
                            
                            EdocRecieveRecord receiveRecord = edocExchangeManager.getReceivedRecord(receiveId);
                            
                            //获取送文单上的主送单位, 书生交换replyId为空
                            if(receiveRecord != null && Strings.isNotBlank(receiveRecord.getReplyId())){
                                long sendDetailId = Long.parseLong(receiveRecord.getReplyId());
                                EdocSendDetail sendDetail = sendEdocManager.getSendRecordDetail(sendDetailId);
                                
                                if(sendDetail == null){ //A8书生收文没有sendDetail数据
                                    sendToNames = receiveRecord.getSendTo();
                                }else{
                                    Long sendId = sendDetail.getSendRecordId();
                                    if(sendId != null){
                                        EdocSendRecord sendRecord = sendEdocManager.getEdocSendRecord(sendId);
                                        if(sendRecord != null){
                                            sendToId = sendRecord.getSendedTypeIds();
                                            sendToNames = sendRecord.getSendEntityNames();
                                        }else {
                                            sendToNames = receiveRecord.getSendTo();
                                        }
                                    }
                                }
                            }
                            
                            edocSummary.setSendTo(sendToNames);
                            edocSummary.setSendToId(sendToId);
                            edocSummary.setCopyTo(null);
                            edocSummary.setCopyToId(null);
                            edocSummary.setReportTo(null);
                            edocSummary.setReportToId(null);
                            edocSummary.setSendTo2(null);
                            edocSummary.setSendToId2(null);
                            edocSummary.setCopyTo2(null);
                            edocSummary.setCopyToId2(null);
                            edocSummary.setReportTo2(null);
                            edocSummary.setReportToId2(null);
                        }*/
                    }
                }
            }
            //在收文待发编辑的时候向新建页面存储了sendEdocId值，那么在切换文单的时候才能获取到被切换文单中没有的元素值（比如说发文部门）
            //然后再调用下面的，使用户手填的值也可以传到被切换的文单中
            if(edocSummary.getEdocType()==1){
                DataUtil.requestToSummary(request, edocSummary, formId);
            }
            
        } else {
            DataUtil.requestToSummary(request, edocSummary, formId);
        }
        actorId = Long.parseLong(request.getParameter("actorId"));

        //OA-43868SP1，公文：公文拟文时默认的公文单上没有'拟稿人'公文元素，切换到有'拟稿人'的公文单后拟稿人不显示
        if(Strings.isBlank(edocSummary.getCreatePerson())){
            edocSummary.setCreatePerson(request.getParameter("my:create_person"));
        }
        // 新建的时候建文人为空，为了切换文单和新建的时候保持一致，这里也取消掉显示建文人
        // if(edocSummary.getCreatePerson()==null ||
        // edocSummary.getCreatePerson().trim().length()==0)
        // {
        // if(user!=null)edocSummary.setCreatePerson(user.getName());
        // }
        // 新建时切换文档显示发文单位
        // if(edocSummary.getSendUnit()==null ||
        // edocSummary.getSendUnit().trim().length()==0)
        // {
        // edocSummary.setSendUnit(EdocRoleHelper.getAccountById(user.getLoginAccount()).getName());
        // edocSummary.setSendUnitId("Account|"+Long.toString(user.getLoginAccount()));
        // }
        if(request.getParameter("edocType") != null) {
            try {
                edocSummary.setEdocType(Integer.parseInt(request.getParameter("edocType")));
            } catch(Exception e) {
                LOGGER.error("切换公文单时抛出异常：",e);
            }
        }
        if(edocSummary.getStartTime() == null) {
            edocSummary.setStartTime(new java.sql.Timestamp(System.currentTimeMillis()));
        }
        //BUG_普通_V5_V5.1sp1_青田县农村信用合作联社_收文单中登记日期不能自动带出系统日期_20150317007671
        if(edocSummary.getRegistrationDate() == null) {
            edocSummary.setRegistrationDate(new Date(System.currentTimeMillis()));
        }
        String edocType=request.getParameter("edocType");
        if(Constants.EDOC_FORM_TYPE_REC == Integer.parseInt(edocType)){
            
            String recieveId = request.getParameter("recieveId");
            if(Strings.isNotBlank(recieveId)){
                EdocRecieveRecord  recieveRecord = edocExchangeManager.getReceivedRecord(Long.parseLong(recieveId));
                edocSummary.setReceiptDate(new Date(recieveRecord.getRecTime().getTime()));
            }
        }
        
        EdocFormModel formModel = null;
        // 判断是模板调用还是后台管理员调用
        String isEdocTemplete = request.getParameter("isEdocTempletePage");
        if(Strings.isNotBlank(isEdocTemplete) && "true".equals(isEdocTemplete)) {
            formModel = edocFormManager.getEdocFormModel(formId, edocSummary, actorId, true, false);
        } else {
            formModel = edocFormManager.getEdocFormModel(formId, edocSummary, actorId, false, false);
        }
        //如果可以的话直接设置公文的EdocSummary为null--因为没有使用
//        formModel.setEdocSummary(null);
        if(formModel != null && formModel.getEdocSummary() != null){
            formModel.getEdocSummary().setSubject(Strings.toHTML(formModel.getEdocSummary().getSubject()));
            if( formModel.getEdocSummary().getEdocSendRecords() != null && formModel.getEdocSummary().getEdocSendRecords().size()>0){
                formModel.getEdocSummary().setEdocSendRecords(new HashSet<EdocSendRecord>());//暂时停用-xml转换存在异常而且前端并没有使用浪费传输
            }
        }
        response.setContentType("text/xml; charset=utf-8");
        request.setCharacterEncoding("UTF-8");
        response.getWriter().print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + ObjectToXMLUtil.objectToXML(formModel));
        // 分支 开始
      //(5.0sprint3)-FIXED(yangfan)--已经不用这个session的方式了，T4调用查询接口
        //request.getSession().setAttribute("SessionObject", edocFormManager.getElementByEdocForm(edocFormManager.getEdocForm(formId)));
        // 分支 结束
        return null;
    }

    
    public ModelAndView importForms(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String formids = request.getParameter("formids");
        edocFormManager.importEdocForm(formids);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().print("<script>parent.importOk();</script>");
        return null;
    }

    private <T>List<T> pagenate(List<T> list) {
        if(null == list || list.size() == 0)
            return new ArrayList<T>();
        Integer first = Pagination.getFirstResult();
        Integer pageSize = Pagination.getMaxResults();
        Pagination.setRowCount(list.size());
        List<T> subList = null;
        if(first + pageSize > list.size()) {
            subList = list.subList(first, list.size());
        } else {
            subList = list.subList(first, first + pageSize);
        }
        return subList;
    }

    private List<EdocForm> convertIMG(List<EdocForm> list) {
        if(null != list && list.size() > 0) {
            for(EdocForm form : list) {
                String content = form.getContent();
                content = convertContent(content, form.getShowLog());
                form.setContent(content);
            }
            return list;
        } else {
            return null;
        }
    }

    public ModelAndView operationChooseEntry(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("edoc/formManage/operation_choose_iframe");
        String type = request.getParameter("type");
        String boundName = request.getParameter("boundName");
        mav.addObject("boundName", boundName);
        mav.addObject("type", type);
        mav.addObject("permItem", request.getParameter("permItem"));
        return mav;
    }

    public ModelAndView operationChoose(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        ModelAndView mav = new ModelAndView("edoc/formManage/operation_choose");
        String type = request.getParameter("type");
        String boundName = request.getParameter("boundName");
        mav.addObject("boundName", boundName);
        mav.addObject("permItem", request.getParameter("permItem"));
        String category = "";
        if(null != type && !"".equals(type)) {
            if(Integer.valueOf(type).intValue() == EdocEnum.edocType.sendEdoc.ordinal()) {
                category = MetadataNameEnum.edoc_send_permission_policy.name();
            } else if(Integer.valueOf(type).intValue() == EdocEnum.edocType.recEdoc.ordinal()) {
                category = MetadataNameEnum.edoc_rec_permission_policy.name();
            } else if(Integer.valueOf(type).intValue() == EdocEnum.edocType.signReport.ordinal()) {
                category = MetadataNameEnum.edoc_qianbao_permission_policy.name();
            }
        }
      List<PermissionVO> flowPermlist = permissionManager.getPermission(category, Permission.Node_isActive, user.getLoginAccount());
      mav.addObject("flowPermList", flowPermlist);
        return mav;
    }

    private String convertContent(String content, boolean showLog) {
        String replacement = "<div align=\"left\"><img src=\"/seeyon/apps_res/v3xmain/images/logo.gif\" /></div>";
        
        String _content = content.replace(replacement, "");
        // 在修改方法中,首先进行一次替换,如果之前设置的为默认logo, 那么将logo置空
        replacement = EdocHelper.getLogoURL();
        _content = _content.replace(replacement, "");// 因为这是恢复操作,所以使用replace方法,而其他用replaceFirst方法只进行第一次的替换
        // ---
        if(showLog) {
            _content = _content.replaceFirst("<xsl:template match=\"my:myFields\">", "<xsl:template match=\"my:myFields\">" + replacement);
        }// 如果显示单位logo,替换代码
        return _content;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }
    @CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
    public ModelAndView setDefaultForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        String statusId = request.getParameter("statusId");
        EdocFormExtendInfo edocFormExtendInfo = edocFormManager.getEdocFormExtendInfo(Long.valueOf(statusId)); // 得到公文单
        if((null != edocFormExtendInfo && (edocFormExtendInfo.getIsDefault()) || edocFormExtendInfo.getEdocForm()==null)) {
            return new ModelAndView("edoc/refreshWindow").addObject("windowObj","parent");
        }
        boolean hasSubType=EdocHelper.hasEdocCategory()&&EdocHelper.isG6Version();//G6版本，并且开启了公文种类开关的时候才有hasSubType
        if(edocFormExtendInfo.getEdocForm().getType().intValue()==0) {
            edocFormManager.updateDefaultEdocForm(edocFormExtendInfo,user.getLoginAccount(), edocFormExtendInfo.getEdocForm().getType(), edocFormExtendInfo.getEdocForm().getSubType(),hasSubType); // 将符合条件的公文单设成非默认
        } else {
            edocFormManager.updateDefaultEdocForm(edocFormExtendInfo,user.getLoginAccount(), edocFormExtendInfo.getEdocForm().getType(), null,hasSubType); // 将符合条件的公文单设成非默认
        }
        edocFormManager.setDefaultEdocForm(user.getLoginAccount(), edocFormExtendInfo.getEdocForm().getType(), edocFormExtendInfo.getEdocForm());// update
                                                                                                                                   // the
                                                                                                                                   // form
                                                                                                                                   // data
        NotificationManager.getInstance().send(NotificationType.DefaultEdocFormReSet, new Object[]{edocFormExtendInfo.getEdocForm().getId(), user.getLoginAccount()});
        // 记录应用日志
        appLogManager.insertLog(user, AppLogAction.Edoc_Form_SetDefault, user.getName(), edocFormExtendInfo.getEdocForm().getName());
        return new ModelAndView("edoc/refreshWindow").addObject("windowObj","parent");
    }
    @CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
    public ModelAndView doAuthEdocForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        String auth = request.getParameter("auth");
        String[] ids = request.getParameterValues("id"); 
        if(ids != null && ids.length != 0) {
            for(String id : ids) {
                EdocForm eForm = edocFormManager.getEdocForm(Long.valueOf(id));
                //oldInfos记录数据库中原来有哪些EdocFormExtendInfo，用此来判断哪些授权是之前没有的
                Set<EdocFormExtendInfo> oldInfos=new HashSet<EdocFormExtendInfo>();
                oldInfos.addAll(eForm.getEdocFormExtendInfo());
                List<Long> oldAccs=new ArrayList<Long>();
                for(EdocFormExtendInfo info:oldInfos){
                    oldAccs.add(info.getAccountId());
                }
                
                Set<EdocFormAcl> reqAcl = getEdocFormAclSetForCurrentForm(user.getLoginAccount(), auth, eForm);
                Set<EdocFormExtendInfo> infos = getInfos(eForm, reqAcl, user.getLoginAccount(), null, true);
                // 绑定对象
                Set<EdocFormFlowPermBound> bounds = eForm.getEdocFormFlowPermBound();
                String defaultSortType = "0";
                // 增加
                List<Long> accountId = getCompareEdocFormAclsAdd(eForm.getEdocFormAcls(), reqAcl);
                // 删除
                List<Long> del = getCompareEdocFormAclsDel(eForm.getEdocFormAcls(), reqAcl);
                eForm.getEdocFormExtendInfo().clear();
                eForm.getEdocFormExtendInfo().addAll(infos);
                eForm.getEdocFormAcls().clear();
                eForm.getEdocFormAcls().addAll(reqAcl);
                edocFormManager.updateEdocForm(eForm);
                /** 因为form与bound表进行了关联，所有上面的操作会把节点权限设置有关的都还原回去，所以有关节点权限设置有关的放在updateEdocForm下面 */
                /*for(EdocFormFlowPermBound bound : bounds) {
                    if(null != accountId){//xiangfan添加 空指针 防护 修复GOV-5152
                        for(Long aid : accountId) {
                            // 如果授权是原单位则不进行添加
                            if (eForm.getDomainId().equals(aid))
                                continue;
                            if(eForm.getDomainId().longValue() != aid.longValue() && bound.getDomainId().longValue() == eForm.getDomainId().longValue()) {
                                edocFormManager.bound(bound.getProcessName(), bound.getFlowPermName(), bound.getFlowPermNameLabel(), eForm.getId(), defaultSortType, aid);
                            }
                            //boundOneSpecialAccount(bound.getProcessName(), bound.getFlowPermName(), bound.getFlowPermNameLabel(), defaultSortType, eForm, aid);
                        }
                    }
                     //取消某个单位授权时不删除意见绑定，因为这样会导致已调用该文单的公文意见绑定错误
                     if(null != del){//xiangfan添加 空指针 防护 修复GOV-5152
                        for(Long aid : del) {
                            // 不删除原单意见元素绑定
                            if (!eForm.getDomainId().equals(aid)){
                                edocFormManager.deleteEdocFormFlowPermBoundByFormIdAndAccountId(eForm.getId(),aid);
                            }
                        }
                    }
                }*/
                if(null != accountId){//xiangfan添加 空指针 防护 修复GOV-5152
                    for(Long aid : accountId) {
                        // 如果授权是原单位则不进行添加
                        if (eForm.getDomainId().equals(aid))
                            continue;
                        if(eForm.getDomainId().longValue() == aid.longValue()) {
                            continue;
                        }
                        if(!oldAccs.contains(aid)){//原来没有授权过该组织才新建绑定关系，否则已经存在
                            boundOtherSpecialAccount(eForm, aid);
                        }
                    }
                }
                
                // 记录应用日志
                appLogManager.insertLog(user, AppLogAction.Edoc_Form_Authorize, user.getName(), eForm.getName());
            }
        }
        return new ModelAndView("edoc/refreshWindow").addObject("windowObj","parent");
    }
    
    //暂时用来新建单位的接口，到时去掉
    public ModelAndView newAccountCreate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String _accountId=request.getParameter("id");
        if(_accountId!=null){
            EdocHelper.generateZipperFleet(Long.parseLong(_accountId));
        }

        return null;
    }
    
    /**
     * 解析前端传入的文单意见配置信息
     * @Author      : xuqiangwei
     * @Date        : 2014年11月17日下午3:49:58
     * @param request
     * @return
     */
    private FormOpinionConfig parseConfigData(HttpServletRequest request){
      //文单设置设置保存
        FormOpinionConfig formConfig = new FormOpinionConfig();
        
        String showLastOptionOnly = request.getParameter("optionType");
        if(Strings.isNotBlank(showLastOptionOnly)){//意见显示设置
            formConfig.setOpinionType(showLastOptionOnly);
        }
        
        //系统落款设置  start
        String[] depts = request.getParameterValues("showOrgnDept");
        formConfig.setInscriberNewLine(false);
        formConfig.setShowDept(false);
        formConfig.setHideInscriber(false);
        formConfig.setShowName(false);
        formConfig.setShowUnit(false);
        if(depts != null && depts.length > 0){
            for(String s : depts){
                
                if(s.equals(OpinionInscriberSetEnum.UNIT.getValue())){
                    formConfig.setShowUnit(true);
                }else if(s.equals(OpinionInscriberSetEnum.DEPART.getValue())){
                    formConfig.setShowDept(true);
                }else if(s.equals(OpinionInscriberSetEnum.NAME.getValue())){
                    formConfig.setShowName(true);
                }else if(s.equals(OpinionInscriberSetEnum.INSCRIBER.getValue())){
                    formConfig.setHideInscriber(true);
                }else if(s.equals(OpinionInscriberSetEnum.INSCRIBER_NEW_LINE.getValue())){
                    formConfig.setInscriberNewLine(true);
                }
            }
        }
        
        //处理时间格式化
        String dealTimeFmt = request.getParameter("dealTimeFormt");
        if(Strings.isNotBlank(dealTimeFmt)){
            formConfig.setShowDateType(dealTimeFmt);
        }else {
        	formConfig.setShowDateType("2");
        }
        
        //签名方式显示方式设置
        String nameShowType = request.getParameter("nameShowTypeItem");
        if(Strings.isNotBlank(nameShowType)){
            formConfig.setShowNameType(nameShowType);
        }
        
        //签名方式显示方式设置
        String nameAndDateNotInline = request.getParameter("nameAndDateNotInline");
        if(Strings.isNotBlank(nameAndDateNotInline)){
            formConfig.setNameAndDateNotInline(true);
        }
        
        return formConfig;
    }
}
