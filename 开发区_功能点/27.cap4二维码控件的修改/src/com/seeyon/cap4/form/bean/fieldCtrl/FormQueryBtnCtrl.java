package com.seeyon.cap4.form.bean.fieldCtrl;

import com.seeyon.cap4.form.bean.FormAuthViewFieldBean;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.FormSaveAsBean;
import com.seeyon.cap4.form.bean.ParamDefinition;
import com.seeyon.cap4.form.modules.business.BusinessManager;
import com.seeyon.cap4.form.modules.importandexport.BusinessDataBean;
import com.seeyon.cap4.form.modules.importandexport.BusinessEnums;
import com.seeyon.cap4.form.modules.importandexport.BusinessExportConstant;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.logging.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.seeyon.ctp.util.json.JSONUtil.parseJSONString;

/**
 * Created by weijh on 2018-1-3.
 * 查询统计
 */
public class FormQueryBtnCtrl extends FormFieldCustomCtrl {
    private static final Log LOGGER = CtpLogFactory.getLog(FormQueryBtnCtrl.class);

    /**
     * 定义控件名称，控件名称定好之后不允许变化，最好是一个uuid string全球唯一
     * 获取一个uuid string可以使用工具类com.seeyon.ctp.util.UUIDLong();
     * 举例：比如通过UUIDLong()获取到一个id：8710168352240535179，可以在此接口直接返回这个id值return "8710168352240535179"
     *
     * @return
     */
    @Override
    public String getKey() {
        return "5902128098173592526";
    }

    @Override
    public String getFieldLength() {
        return "20";
    }


    @Override
    public void init() {
        //设置控件所属插件的id
        this.setPluginId("formQueryBtn");
        this.setIcon("cap-icon-querystatistics");
        LOGGER.info("自定义控件" + this.getText() + "init执行开始");
        ParamDefinition templateIdParam = new ParamDefinition();
        templateIdParam.setDialogUrl("apps_res/cap/customCtrlResources/formQueryBtnCtrlResources/html/setTemplate.html");
        templateIdParam.setDisplay("com.cap.ctrl.formquery.paramtext");
        templateIdParam.setName("templateId");
        templateIdParam.setParamType(Enums.ParamType.button);
        addDefinition(templateIdParam);

        ParamDefinition conditionParam = new ParamDefinition();
        conditionParam.setDialogUrl("apps_res/cap/customCtrlResources/formQueryBtnCtrlResources/html/setCondition.html");
        conditionParam.setDisplay("com.cap.ctrl.formquery.set.condition");
        conditionParam.setName("mapping");
        conditionParam.setParamType(Enums.ParamType.button);
        addDefinition(conditionParam);
        LOGGER.info("自定义控件" + this.getText() + "init执行结束，params.size:" + super.params.size());
    }

    /**
     * 获取PC端自定义控件运行态资源注入信息
     * jsUri:定义PC端表单运行态加载第三方JavaScript的路径
     * initMethod:定义PC端表单运行态第三方js入口方法名称
     * nameSpace:定义PC端表单运行态命名空间
     *
     * @return
     */
    @Override
    public String getPCInjectionInfo() {
        return "{path:'apps_res/cap/customCtrlResources/formQueryBtnCtrlResources/',jsUri:'js/formQueryBtn.js',initMethod:'init',nameSpace:'field_" + this.getKey() + "'}";
    }

    /**
     * 获取移动端自定义控件运行态资源注入信息
     * path：'http://'+m3应用包mainifest.json中的urlSchemes的值+'v'+m3应用包mainifest.json中的version的值
     * weixinpath: 微信端打开的时候使用的m3/apps/v5/自定义控件移动端资源目录名称/,weixinpath配置的就是此自定义控件移动端资源目录名称
     * jsUri:移动端表单运行态加载第三方JavaScript的路径
     * initMethod:定义M3端表单运行态第三方js入口方法名称
     * * nameSpace:定义M3端表单运行态命名空间
     *
     * @return
     */
    @Override
    public String getMBInjectionInfo() {
        return "{path:'http://querybtn.v5.cmp/v1.0.0/',weixinpath:'querybtn/',jsUri:'js/formQueryBtn.js',initMethod:'init',nameSpace:'field_" + this.getKey() + "'}";
    }

    /**
     * 举例：比如是一个身份证识别控件，可以return '身份证识别控件';
     *
     * @return
     */
    @Override
    public String getText() {
        return ResourceUtil.getString("com.cap.ctrl.formquery.text");
    }

    @Override
    public boolean canBathUpdate() {
        return false;
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
    public boolean canInjectionWord() {
        return false;
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
                String categoryId = (String) templateMap.get("categoryId");
                templateMap.put("categoryId", String.valueOf(businessDataBean.getRealId4Export(Long.valueOf(categoryId))));
            }
            resultMap.put(BusinessExportConstant.CUSTOMPARAM, JSONUtil.toJSONString(customMap));
        } else {
            resultMap.put(BusinessExportConstant.CUSTOMPARAM, "");
        }
    }

    /**
     * 在业务导入完之后自定义控件的处理接口
     *
     * @param formBean         当前导入表单
     * @param formFieldBean    当前控件所在字段
     * @param businessDataBean 导入全局对象
     */
    @SuppressWarnings("unchecked")
    public void importInfoAfterBizImport(FormBean formBean, FormFieldBean formFieldBean, BusinessDataBean businessDataBean) {
        String customParam = formFieldBean.getCustomParam();
        if (!StringUtil.checkNull(customParam)) {
            Map<String, Object> customParamMap = (Map<String, Object>) JSONUtil.parseJSONString(customParam);
            if (customParamMap.containsKey("templateId")) {
                Map<String, Object> templateMap = (Map<String, Object>) customParamMap.get("templateId");
                //设置了
                if (null != templateMap && businessDataBean.getExportType().getKey().equals(BusinessEnums.ExportType.APPLICATION.getKey())) {
                    Map<String, Object> newSet = new HashMap<String, Object>();
                    String categoryId = (String) templateMap.get("categoryId");
                    Map<String, Object> bizConfigMap = (Map<String, Object>) businessDataBean.getRootMap().get("bizconfig");
                    String bizId = (String) bizConfigMap.get("id");
                    String oldDesignId = (String) templateMap.get("designId");
                    //应用内的
                    if (categoryId.equals(bizId)) {
                        if (businessDataBean.isResetId()) {
                            templateMap.put("categoryId", String.valueOf(businessDataBean.genNewIdByOldId(Long.parseLong(bizId))));
                        } else {
                            // 业务包ID，导入时进行ID冲突预防
                            boolean conflict = false;
                            try {
                                BusinessManager businessManager4 = (BusinessManager) AppContext.getBean("businessManager4");
                                conflict = businessManager4.findBizConfigById(Long.valueOf(bizId)) != null;
                            } catch (Exception e) {

                            }
                            templateMap.put("categoryId", String.valueOf(businessDataBean.genNewIdByOldId(Long.parseLong(bizId), conflict, false)));
                        }
                        templateMap.put("designId", String.valueOf(businessDataBean.genNewIdByOldId(Long.parseLong(oldDesignId))));
                        newSet.put("templateId", templateMap);

                        List<Map<String, Object>> mappings = (List<Map<String, Object>>) customParamMap.get("mapping");
                        if (Strings.isNotEmpty(mappings)) {
                            for (Map<String, Object> mapping : mappings) {
                                Map<String, Object> source = (Map<String, Object>) mapping.get("source");
                                String oldTableName = (String) source.get("tableName");
                                String newTableName = businessDataBean.getOldAndNewStringMap().get(oldTableName);
                                if(null != newTableName){
                                    source.put("tableName", newTableName);
                                    String oldAliasTableName = (String) source.get("aliasTableName");
                                    String newAliasTableName = oldAliasTableName.replace(oldTableName, newTableName);
                                    source.put("aliasTableName", newAliasTableName);
                                }
                                String oldEnumId = source.get("enumId") == null ? null : String.valueOf(source.get("enumId"));
                                Long newEnumId = 0L;
                                if (oldEnumId != null && !"0".equals(oldEnumId)) {
                                    newEnumId = Long.parseLong(businessDataBean.getEnumMapOld2New().get(oldEnumId));
                                }
                                source.put("enumId", newEnumId);
                                mapping.put("source", source);
                                Map<String, Object> oldTarget = (Map<String, Object>) mapping.get("target");
                                String fieldName = (String) oldTarget.get("name");
                                FormFieldBean targetField = formBean.getFieldBeanByName(fieldName);
                                if(targetField == null){
                                    mapping.put("target",new HashMap<String,Object>());
                                }else{
                                    mapping.put("target", getFieldJson(targetField));
                                }
                            }
                            newSet.put("mapping", mappings);
                        }
                        formFieldBean.setCustomParam(JSONUtil.toJSONString(newSet));
                    } else {//应用外的
                        formFieldBean.setCustomParam("");
                    }
                } else {//没有设置
                    formFieldBean.setCustomParam("");
                }
            } else {
                formFieldBean.setCustomParam("");
            }
        }
    }


    private Map<String, Object> getFieldJson(FormFieldBean field) {
        Map<String, Object> result = new HashMap<String, Object>(16);
        result.put("id", String.valueOf(field.getId()));
        result.put("name", field.getName());
        String tempDisplayStr = (field.getDisplay() == null) ? "" : field.getDisplay();
        result.put("display", tempDisplayStr);
        result.put("fieldType", field.getFieldType());
        result.put("type", field.getInputType());
        result.put("formatType", field.getFormatType());
        String tempFieldLength = "";
        if (Strings.isNotBlank(field.getFieldLength())) {
            tempFieldLength = field.getFieldLength();
            //数字类型需要补充小数位
            if (Strings.isNotBlank(field.getDigitNum()) && Enums.FieldType.DECIMAL.getKey().equals(field.getFieldType()) && !tempFieldLength.contains(",")) {
                tempFieldLength += "," + field.getDigitNum();
            }
        }
        result.put("fieldLength", tempFieldLength);
        return result;
    }


    /**
     * 查询统计控件另存为实现接口
     *
     * @param fieldBean
     * @param formBean
     * @param mapping
     */
    @SuppressWarnings("unchecked")
    @Override
    public void otherSave(FormFieldBean fieldBean, FormBean formBean, FormSaveAsBean mapping) {
        String customParam = fieldBean.getCustomParam();
        if (Strings.isNotBlank(customParam)) {
            Map<String, Object> objectMap = (Map<String, Object>) parseJSONString(customParam);
            if (objectMap.containsKey("mapping")) {
                List<Map<String, Object>> jsonObjectList = (List<Map<String, Object>>) objectMap.get("mapping");
                if(null!=jsonObjectList) {
                    for (Map<String, Object> jsonObject : jsonObjectList) {
                        Map<String, Object> target = (Map<String, Object>) jsonObject.get("target");
                        if (target != null) {
                            String oldId = target.get("id").toString();
                            Long newId = mapping.getNewIdByOldId(Long.parseLong(oldId));
                            if (newId != null) {
                                customParam = customParam.replace(oldId, newId.toString());
                            }
                        }
                        fieldBean.setCustomParam(customParam);
                    }
                }
            }
        }
    }

    @Override
    public boolean authNotNullAndValIsNull(FormDataMasterBean formDataMasterBean, FormFieldBean field, FormAuthViewFieldBean authViewFieldBean, Object val) {
        return false;
    }
}