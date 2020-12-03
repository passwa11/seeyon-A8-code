package com.seeyon.cap4.form.bean.fieldCtrl;

import com.seeyon.cap4.form.bean.FormAuthViewFieldBean;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.FormSaveAsBean;
import com.seeyon.cap4.form.bean.ParamDefinition;
import com.seeyon.cap4.form.modules.importandexport.BusinessDataBean;
import com.seeyon.cap4.form.modules.importandexport.BusinessExportConstant;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.logging.Log;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.seeyon.ctp.util.json.JSONUtil.parseJSONString;

/**
 * Created by weijh on 2018-6-6.
 * 正文套红控件实现类
 */
public class WordInjectionCtrl extends FormFieldCustomCtrl {
    private static final Log LOGGER = CtpLogFactory.getLog(FormQueryBtnCtrl.class);

    @Override
    public String getKey() {
        return "1872888230778916558";
    }

    @Override
    public String getText() {
        return ResourceUtil.getString("com.cap.ctrl.wordinjectionctrl.text");
    }

    @Override
    public boolean canBathUpdate() {
        return false;
    }

    @Override
    public boolean isAttachment() {
        return true;
    }

    /**
     * 是否支持套红，自定义控件默认false，如果支持需要重新接口返回true
     *
     * @return
     */
    @Override
    public boolean canInjectionWord() {
        return false;
    }

    /**
     * 定义控件是否能参与明细表，默认支持，如果不支持重写此方法返回false
     *
     * @return
     */
    @Override
    public boolean canInSubTable() {
        return false;
    }

    /**
     * 表单转文档控件另存为实现接口
     *
     * @param fieldBean
     * @param formBean
     * @param mapping
     */
    @Override
    public void otherSave(FormFieldBean fieldBean, FormBean formBean, FormSaveAsBean mapping) {
        String customParam = fieldBean.getCustomParam();
        if (Strings.isNotBlank(customParam)) {
            Map<String, Object> objectMap = (Map<String, Object>) parseJSONString(customParam);
            if (objectMap.containsKey("templateId")) {
                Map<String, String> templateSet = (Map<String, String>) objectMap.get("templateId");
                if (templateSet.containsKey("fileUrl")) {
                    String fileUrl = templateSet.get("fileUrl");
                    if (Strings.isNotBlank(fileUrl)) {
                        AttachmentManager attachmentManager = (AttachmentManager) AppContext.getBean("attachmentManager");
                        Attachment attachment = attachmentManager.getAttachmentByFileURL(Long.parseLong(fileUrl));
                        if (attachment != null) {
                            Long newRef = UUIDLong.longUUID();
                            Long newSubref = UUIDLong.longUUID();
                            attachmentManager.copy(attachment.getReference(), attachment.getSubReference(), newRef, newSubref, attachment.getCategory());
                            List<Attachment> newAtts = attachmentManager.getByReference(newRef, newSubref);
                            if (newAtts.size() == 1) {
                                Long newFileUrl = newAtts.get(0).getFileUrl();
                                templateSet.put("fileUrl", String.valueOf(newFileUrl));
                                fieldBean.setCustomParam(JSONUtil.toJSONString(objectMap));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean authNotNullAndValIsNull(FormDataMasterBean formDataMasterBean, FormFieldBean field, FormAuthViewFieldBean authViewFieldBean, Object val) {
        String strVal = String.valueOf(val);
        boolean result = false;
        if(StringUtil.checkNull(strVal)&&authViewFieldBean.isNotNullable()){
            result = true;
        }else{
            AttachmentManager attachmentManager = (AttachmentManager)AppContext.getBean("attachmentManager");
            result = authViewFieldBean.isNotNullable()&&attachmentManager.getBySubReference(Long.parseLong(strVal)).size()<=0;
        }
        return result;
    }

    @Override
    public List<String[]> getListShowDefaultVal(Integer externalType) {
        return null;
    }

    @Override
    public String[] getDefaultVal(String defaultValue) {
        return new String[0];
    }

    @Override
    public void init() {
        //设置控件所属插件的id
        this.setPluginId("formwordinjectionctrl");
        this.setIcon("cap-icon-form-to-document");
        LOGGER.info("自定义控件" + this.getText() + "init执行开始");
        ParamDefinition templateIdParam = new ParamDefinition();
        templateIdParam.setDialogUrl("apps_res/cap/customCtrlResources/wordInjectionResources/html/templatesetting.html");
        //templateIdParam.setDisplay(ResourceUtil.getString("com.cap.ctrl.formquery.paramtext"));
        templateIdParam.setDisplay("com.cap.ctrl.wordinjectionctrl.template");
        templateIdParam.setDialogTitle("com.cap.ctrl.wordinjectionctrl.uploadtitle");
        templateIdParam.setName("templateId");
        templateIdParam.setParamType(Enums.ParamType.button);
        templateIdParam.setDialogWidth("400");
        templateIdParam.setDialogHeight("200");
        ParamDefinition mappingDefinition = new ParamDefinition();
        mappingDefinition.setDialogUrl("apps_res/cap/customCtrlResources/wordInjectionResources/html/setmapping.html");
        mappingDefinition.setDialogTitle("com.cap.ctrl.wordinjectionctrl.mapingsetting");
        mappingDefinition.setDisplay("com.cap.ctrl.wordinjectionctrl.mapingsetting");
        mappingDefinition.setName("mapping");
        mappingDefinition.setParamType(Enums.ParamType.button);
        addDefinition(templateIdParam);
        addDefinition(mappingDefinition);
        LOGGER.info("自定义控件" + this.getText() + "init执行结束，params.size:" + super.params.size());
    }

    @Override
    public String getPCInjectionInfo() {
        return "{path:'apps_res/cap/customCtrlResources/wordInjectionResources/',jsUri:'js/wordInjectionPcRunning.js',initMethod:'init',nameSpace:'field_" + this.getKey() + "'}";
    }

    @Override
    public String getMBInjectionInfo() {
        return "{path:'http://formwordinjectionctrl.v5.cmp/v1.0.0/',weixinpath:'formwordinjectionctrl',jsUri:'js/wordInjectionMbRunning.js',initMethod:'init',nameSpace:'feild_" + this.getKey() + "'}";
    }

    @Override
    public String getFieldLength() {
        return "20";
    }

    /**
     * 导出的扩展接口，自定义控件用，有需要的重写该方法
     *
     * @param formBean         当前表单
     * @param formFieldBean    当前字段
     * @param businessDataBean 导出中间对象，如果有附件，可以放到对象中的unifiedExportAttachment中
     * @param resultMap        字段json
     */
    @SuppressWarnings("unchecked")
    @Override
    public void getJson4Export(FormBean formBean, FormFieldBean formFieldBean, BusinessDataBean businessDataBean, Map<String, Object> resultMap) {
        String customParam = formFieldBean.getCustomParam();
        if (Strings.isNotBlank(customParam)) {
            Map<String, Object> customMap = (Map<String, Object>) parseJSONString(customParam);
            Map<String, Object> templateMap = (Map<String, Object>) customMap.get("templateId");
            if (templateMap != null) {
                String fileUrl = templateMap.get("fileUrl") == null ? "" : templateMap.get("fileUrl").toString();
                if (Strings.isNotBlank(fileUrl)) {
                    businessDataBean.addUnifiedExportAttachment(this.getKey(), Long.parseLong(fileUrl));
                    businessDataBean.putAttNameMapping(fileUrl, templateMap.get("fileName").toString());
                }
            }
            resultMap.put(BusinessExportConstant.CUSTOMPARAM, JSONUtil.toJSONString(customMap));
        } else {
            resultMap.put(BusinessExportConstant.CUSTOMPARAM, "");
        }
    }

    /**
     * 导入的时候处理自定义控件的扩展信息，自行重写接口来实现逻辑
     *
     * @param formBean         当前表单
     * @param formFieldBean    当前字段
     * @param businessDataBean 导入中间对象
     * @param fieldInfo        字段信息
     */
    @Override
    public void importExtInfo(FormBean formBean, FormFieldBean formFieldBean, BusinessDataBean businessDataBean, Map<String, Object> fieldInfo) {
        File attachmentFolder = businessDataBean.getAttachmentFolder();
        String customParam = formFieldBean.getCustomParam();
        if (Strings.isNotBlank(customParam) && attachmentFolder != null && attachmentFolder.isDirectory() && attachmentFolder.listFiles().length > 0) {
            File fileFolder = new File(attachmentFolder.getAbsolutePath() + File.separator + this.getKey());
            if (fileFolder.isDirectory() && fileFolder.listFiles().length > 0) {
                FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
                AttachmentManager attachmentManager = (AttachmentManager) AppContext.getBean("attachmentManager");
                File[] files = fileFolder.listFiles();
                Map<String, String> attNameMapping = businessDataBean.getAttNameMapping();
                for (File tempFile : files) {
                    String oldId = tempFile.getName();
                    String newName = attNameMapping.get(oldId);
                    if (Strings.isBlank(newName)) {
                        newName = oldId;
                    }
                    try {
                        V3XFile v3xfile = fileManager.save(tempFile, ApplicationCategoryEnum.cap4Form, newName, DateUtil.currentDate(), true);
                        if (v3xfile != null) {
                            //生成附件表记录
                            attachmentManager.create(new Long[]{v3xfile.getId()}, ApplicationCategoryEnum.cap4Form, formBean.getId(), UUIDLong.longUUID());
                            customParam = customParam.replace(oldId, String.valueOf(v3xfile.getId()));
                            formFieldBean.setCustomParam(customParam);
                        }
                    } catch (BusinessException e) {
                        LOGGER.error("导入应用-附件保存异常：" + e.getMessage(), e);
                    }
                }
            }
        }
    }
}
