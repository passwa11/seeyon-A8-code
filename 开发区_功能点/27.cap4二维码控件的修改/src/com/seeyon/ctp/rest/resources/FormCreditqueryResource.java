package com.seeyon.ctp.rest.resources;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.seeyon.apps.mplus.api.MplusApi;
import com.seeyon.cap4.form.bean.FormAuthViewBean;
import com.seeyon.cap4.form.bean.FormAuthViewFieldBean;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormDataSubBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.FormFieldComEnum;
import com.seeyon.cap4.form.bean.FormTableBean;
import com.seeyon.cap4.form.bean.fieldCtrl.CreditqueryFieldType;
import com.seeyon.cap4.form.bean.fieldCtrl.enums.CreditQueryConstant;
import com.seeyon.cap4.form.modules.engin.base.formData.CAP4FormDataManager;
import com.seeyon.cap4.form.service.CAP4FormCacheManager;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.cap4.template.bean.LockObjectBean;
import com.seeyon.cap4.template.constant.CAPBusinessEnum;
import com.seeyon.cap4.template.manager.CAPRuntimeCalcManager;
import com.seeyon.cap4.template.manager.CAPRuntimeDataLockManager;
import com.seeyon.cap4.template.util.CAPFormUtil;
import com.seeyon.cap4.template.util.HttpClientUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.DataContainer;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.groovy.util.Maps;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.seeyon.cap4.form.bean.fieldCtrl.enums.CreditQueryConstant.Category.BUSINESS_INFO;

/**
 * Created by yangz on 2018/3/20.
 * 自定义控件--企业征信
 */
@Path("cap4/formCreditquery")
@Consumes({MediaType.APPLICATION_JSON})
@Produces(MediaType.APPLICATION_JSON)
public class FormCreditqueryResource extends BaseResource {
    private static final Log LOGGER = CtpLogFactory.getLog(FormCreditqueryResource.class);
    final String serviceCode = "m20000000000003002";

    /**
     * M3根据关键字获取征信查询结果
     *
     * @param keyword 关键字
     * @return 返回一个封装了征信信息的map list
     * @throws BusinessException
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("getCreditqueryFieldInfoM3")
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> creditqueryM3(@QueryParam("keyword") String keyword) throws BusinessException {
        List<Map<String, Object>> map_list = new ArrayList<Map<String, Object>>();
        try {
            Map<String, Object> jsonObject = crditeInfotBykeywordPost(keyword);
            map_list = (List<Map<String, Object>>) jsonObject.get("items");
            if (map_list.size() > 5) {
                map_list = map_list.subList(0, 5);
            }
        } catch (BusinessException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
        return map_list;
    }


    /**
     * pc根据关键字获取征信查询结果 企业名称关键字/注册号/统一社会信用代码 其他：公司全名或企业注册号
     *
     * @param keyword 关键字
     * @return
     * @throws BusinessException
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> crditeInfotBykeywordPost(String keyword) throws BusinessException {
        MplusApi mplusApi = (MplusApi) AppContext.getBean("mplusApi");
        String domain = mplusApi.getDomain();
        String ticket = mplusApi.getTicket(serviceCode);
        String url = domain + "/svr/enterprise/info";
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("ticket", ticket);
        json.put("keyword", keyword);
        json.put("type", "2");
        json.put("pageNum", 1);
        json.put("parameterType", "2");
        Map<String, Object> res = HttpClientUtil.doPost(url, json);
        String state = res.get("code").toString();
        if (!"1000".equals(state)) {
            throw new BusinessException("查询出错，状态为" + state);
        }
        return (Map<String, Object>) res.get("data");
    }


    /**
     * 获取所有企业征信映射属性与所有可以进行映射的表单字段
     *
     * @param formId       表单id
     * @param fieldType    字段类型
     * @param currentField 当前字段
     * @return
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("getCreditqueryFieldInfo")
    public Response getCreditqueryFieldInfo(@QueryParam("formid") String formId, @QueryParam("fieldType") String fieldType, @QueryParam("currentField") String currentField) {
        Map<String, Object> res = new HashMap<String, Object>();
//        Map<String, Object> creditqueryFields = new HashMap<String, Object>();
        ArrayList<Object> list = Lists.newArrayList();
        for (CreditQueryConstant.Category category : CreditQueryConstant.Category.values()) {
            String categoryKey = category.getKey();
            Class subEnum = category.getSubEnum();
            Object[] enumConstants = subEnum.getEnumConstants();
            list.add(Maps.of(categoryKey, enumConstants));
        }
        DataContainer formFieldMap = getFieldsJsonObject(formId, fieldType, currentField);
        res.put("creditqueryMap", list);
        res.put("formFieldMap", formFieldMap);
        return success(res);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("EnterpriseField")
    public Response getEnterpriseField() {


        ArrayList<Object> res = Lists.newArrayList();
        for (CreditQueryConstant.Category category : CreditQueryConstant.Category.values()) {
            String egName = category.getKey();
            String chName = category.getCategoryName();
            boolean onlySuppMain = category.equals(BUSINESS_INFO);
            Class subEnum = category.getSubEnum();
            Object[] enumConstants = subEnum.getEnumConstants();
            ArrayList<Object> enumlist = Lists.newArrayList();
            for (Object enumConstant : enumConstants) {
                enumlist.add(enumConstant.toString());
            }
            res.add(Maps.of("name", egName, "chName", chName, "info", enumlist, "onlySuppMain", onlySuppMain));
        }
        return success(res);
    }

    /**
     * 根据当前字段获取所在表单的所以可以选择的绑定的字段
     *
     * @param formId       表单id
     * @param fieldType    字段类型
     * @param currentField 当前字段
     * @return
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("getFormFields")
    public Response getFormFields(@QueryParam("formid") String formId, @QueryParam("fieldType") String fieldType, @QueryParam("currentField") String currentField) {
        DataContainer result = getFieldsJsonObject(formId, fieldType, currentField);
        return success(result);
    }


    /**
     * 根据当前字段获取所在表单的所以可以选择的绑定的字段
     *
     * @param formId       表单id
     * @param fieldType    字段类型
     * @param currentField 当前字段
     * @return
     */
    private DataContainer getFieldsJsonObject(@QueryParam("formid") String formId, @QueryParam("fieldType") String fieldType, @QueryParam("currentField") String currentField) {
        CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
        FormBean formBean = cap4FormManager.getEditingForm(Long.valueOf(formId));
        FormTableBean currentTable = formBean.getFormTableBeanByFieldName(currentField);
        DataContainer result = new DataContainer();
        //如果fieldType为空，说明是要获取本表所有字段
        List<FormFieldBean> currentTableFields = currentTable.getFields();
        for (FormFieldBean field : currentTableFields) {
            if (!field.isCustomerCtrl() && CreditqueryFieldType.isFieldCanBeChoose(field.getInputType())) {
                result.put(field.getName(), field.getJsonObj4Design(false));
            }
        }
        return result;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("getCreditQueryInfo")
    public Response creditQueryInfo(@QueryParam("name") String name) throws BusinessException {
        Map<String, Object> queryInfo = getCreditQueryInfo(name);
        return success(queryInfo);
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("getCreditQueryInfoAndFillBack")
    public Response getCreditQueryInfoAndFillBack(CreditQueryParamBean paramBean) throws BusinessException {
        Map<String, Object> queryInfo = getCreditQueryInfo(paramBean.getName());
        return success(fillBack(queryInfo, paramBean));
    }

    private Map<String, Object> getCreditQueryInfo(String name) throws BusinessException {
        MplusApi mplusApi = (MplusApi) AppContext.getBean("mplusApi");
        String domain = mplusApi.getDomain();
        String ticket = mplusApi.getTicket(serviceCode);
        String url = domain + "/svr/enterprise/info";
        Map<String, Object> jsonObject = new HashMap<String, Object>();
        jsonObject.put("ticket", ticket);
        jsonObject.put("keyword", name);
        jsonObject.put("type", "1");
        jsonObject.put("parameterType", "2");
        Map<String, Object> res = HttpClientUtil.doPost(url, jsonObject);
        Map<String, Object> data = (Map<String, Object>) res.get("data");
        String state = res.get("code").toString();
        if (!"1000".equals(state)) {
            BusinessException e = new BusinessException(res.get("msg").toString());
            LOGGER.error(res.get("msg").toString(), e);
            throw e;
        }
        Map<String, Object> queryInfo = new HashMap<String, Object>();
        Set<Object> handledNames = Sets.newHashSet();
        for (CreditQueryConstant.Category category : CreditQueryConstant.Category.values()) {
            String egName = category.getKey();
            handledNames.add(egName);
            if (data.containsKey(egName)) {
                queryInfo.put(egName, data.get(egName));
            }
        }
        List municipalityList = Lists.newArrayList("北京", "天津", "上海", "重庆");
        Map<String, Object> busiInfo = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!handledNames.contains(entry.getKey())) {
                if ("city".equals(entry.getKey()) && municipalityList.contains(data.get("province1"))) {
                    busiInfo.put(entry.getKey(), data.get("province1"));
                } else {
                    busiInfo.put(entry.getKey(), entry.getValue());
                }
            }
        }
        queryInfo.put(BUSINESS_INFO.getKey(), busiInfo);
        queryInfo.put("chart_url", data.get("chart_url"));
        return queryInfo;
    }

    private Object fillBack(Map<String, Object> queryInfo, CreditQueryParamBean paramBean) throws BusinessException {
        CAP4FormCacheManager cap4FormCacheManager = (CAP4FormCacheManager) AppContext.getBean("cap4FormCacheManager");
        CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
        CAPRuntimeDataLockManager capRuntimeDataLockManager = (CAPRuntimeDataLockManager) AppContext.getBean("capRuntimeDataLockManager");

        FormBean formBean = cap4FormCacheManager.getForm(paramBean.getFormId());
        FormDataMasterBean formDataMasterBean = cap4FormManager.getSessioMasterDataBean(paramBean.getFormMasterId());
        FormAuthViewBean formAuthViewBean = CAPFormUtil.getFormAuthViewBean(formBean, formDataMasterBean, paramBean.getRightId());
        //获取映射关系
        FormFieldBean fieldBean = formBean.getFieldBeanByName(paramBean.getFieldName());
        String customParam = fieldBean.getCustomParam();

        LockObjectBean lockObjectBean = capRuntimeDataLockManager.get(paramBean.getFormMasterId());

        //获取回填数据
        synchronized (lockObjectBean) {
            return getFillBackData(paramBean.getDetailFormInfos(), queryInfo, customParam, formDataMasterBean, formBean, formAuthViewBean, fieldBean);
        }
    }

    private Object getFillBackData(List<DetailFormInfo> detailFormInfos, Map<String, Object> queryInfo, String customParamJsonStr, FormDataMasterBean formDataMasterBean, FormBean formBean, FormAuthViewBean formAuthViewBean, FormFieldBean fieldBean) throws BusinessException {
        Map<String, Object> customParam = JSONUtil.parseJSONString(customParamJsonStr, Map.class);
        if (MapUtils.isEmpty(customParam)) {
            return Maps.of("tableData", new HashMap<String,Object>(), "chart_url", MapUtils.getString(queryInfo, "chart_url"));
        }
        Object mapping = customParam.get("mapping");
        CAPRuntimeCalcManager capRuntimeCalcManager = (CAPRuntimeCalcManager) AppContext.getBean("capRuntimeCalcManager");
        CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
        Map<String, Map<String, Object>> tableName2DataMaps = new HashMap<String, Map<String, Object>>();
        Map<String, List<Long>> allTableName2AddSubIds = new HashMap<String, List<Long>>();
        Set<String> allFillBackFields = Sets.newHashSet();
        if (mapping instanceof Map) {
            if ("version_2.0".equals(MapUtils.getString((Map<String,Object>)mapping,"version"))) { // version 2.0
                List<Map<String, Object>> mappingRelationList = (List<Map<String, Object>>) ((Map<String,Object>)mapping).get("mapping");
                for (Map<String, Object> mappingRelation : mappingRelationList) {
                    //分析一组映射关系
                    String name = MapUtils.getString(mappingRelation, "name");
                    ArrayListMultimap<String, Map<String, Object>> form2FiledMapping = getForm2FiledMapping((List<Map<String, Object>>) mappingRelation.get("listData"));
                    if (MapUtils.getBoolean(mappingRelation, "onlySuppMain") && form2FiledMapping.containsKey(formBean.getMasterTableBean().getTableName())) { // 控件在主表时 工商信息 只能回填主表
                        //获取主表单的映射关系
                        List<Map<String, Object>> source2TargetList = form2FiledMapping.get(formBean.getMasterTableBean().getTableName());
                        Map<String, Object> rowData = (Map<String, Object>) queryInfo.get(name);
                        for (Map<String, Object> map : source2TargetList) {
                            String source = MapUtils.getString(map, "source");
                            String target = MapUtils.getString(map, "target");
                            formDataMasterBean.addFieldValue(target, rowData.get(source));
                            allFillBackFields.add(target);
                            formDataMasterBean.addFieldChanges4Calc(formDataMasterBean.getFieldBeanByFieldName(target), rowData.get(source), null);
                        }
                    } else if (MapUtils.getBoolean(mappingRelation, "onlySuppMain") && !form2FiledMapping.containsKey(formBean.getMasterTableBean().getTableName())) {
                        //获取明细表的映射关系
                        String formName = null;
                        for (Map.Entry<String, Map<String, Object>> entry : form2FiledMapping.entries()) { //只有一个entry
                            formName = entry.getKey();
                        }
                        //获取被选中行
                        Long preRecordId = null;
                        for (DetailFormInfo detailFormInfo : detailFormInfos) {
                            if (detailFormInfo.getTableName().equals(formName)) {
                                preRecordId = detailFormInfo.getPreRecordId();
                                break;
                            }
                        }
                        FormDataSubBean formDataSubBean= formDataMasterBean.getFormDataSubBeanById(formName, preRecordId);
                        List<Map<String, Object>> source2TargetList = form2FiledMapping.get(formName);
                        Map<String, Object> rowData = (Map<String, Object>) queryInfo.get(name);
                        for (Map<String, Object> map : source2TargetList) {
                            String source = MapUtils.getString(map, "source");
                            String target = MapUtils.getString(map, "target");
                            formDataSubBean.addFieldValue(target, rowData.get(source));
                            allFillBackFields.add(target +"_"+ formDataSubBean.getId());
                            formDataMasterBean.addFieldChanges4Calc(formDataMasterBean.getFieldBeanByFieldName(target), rowData.get(source), formDataSubBean);
                        }

                    } else {  //除了工商信息 只能回填明细表
                        Map<String, List<Long>> tableName2AddSubIds = new HashMap<String, List<Long>>();
                        Map<String, List<FormDataSubBean>> formName2FormList = parseMappingRelation(form2FiledMapping, queryInfo, name, formDataMasterBean, formBean, formAuthViewBean);
                        for (Map.Entry<String, List<FormDataSubBean>> entry : formName2FormList.entrySet()) {
                            String formName = entry.getKey();
                            List<FormDataSubBean> formDataSubBeanList = entry.getValue();
                            if (formDataSubBeanList.isEmpty()) {
                                continue;
                            }
                            //获取选中行
                            Long preRecordId = null, firstRecordId = null;
                            for (DetailFormInfo detailFormInfo : detailFormInfos) {
                                if (detailFormInfo.getTableName().equals(formName)) {
                                    preRecordId = detailFormInfo.getPreRecordId();
                                    firstRecordId = detailFormInfo.getFirstRecordId();
                                }
                            }
                            //插入明细行
                            initFormDataMasterBean(formDataSubBeanList, preRecordId, formDataMasterBean, formName, formAuthViewBean);

                            //记录新增行
                            List<Long> ids = Lists.newArrayList();
                            for (FormDataSubBean formDataSubBean : formDataSubBeanList) {
                                ids.add(formDataSubBean.getId());
                            }
                            tableName2AddSubIds.put(formName, ids);

                            //如果选中行为空 第一行为空时 删除掉
                            if (preRecordId == null) {
                                capRuntimeCalcManager.mergeFirstDelete2Result(formDataMasterBean, tableName2DataMaps, tableName2AddSubIds, null, null);
                            }
                        }
                        allTableName2AddSubIds.putAll(tableName2AddSubIds);
                    }

                }
            }
        } else {  // 兼容老版本 如果控件在主表 只回填主表字段 如果控件在明细表 只回填控件所在当前行
            List<Map<String,Object>> source2TargetList = (List<Map<String, Object>>) mapping;
            FormTableBean formTableBean = formBean.getFormTableBeanByFieldName(fieldBean.getName());
            Map<String, Object> rowData = (Map<String, Object>) queryInfo.get(BUSINESS_INFO.getKey());
            if (formTableBean.isMainTable()) {
                for (Map<String, Object> map : source2TargetList) {
                    String source = MapUtils.getString(map, "source");
                    String target = MapUtils.getString(map, "target");
                    formDataMasterBean.addFieldValue(target, rowData.get(source));
                    allFillBackFields.add(target);
                    formDataMasterBean.addFieldChanges4Calc(formDataMasterBean.getFieldBeanByFieldName(target), rowData.get(source), null);
                }
            }else{
                //获取明细表名
                String formName = formTableBean.getTableName();
                //获取被选中行
                Long preRecordId = null;
                for (DetailFormInfo detailFormInfo : detailFormInfos) {
                    if (detailFormInfo.getTableName().equals(formName)) {
                        preRecordId = detailFormInfo.getPreRecordId();
                        break;
                    }
                }
                FormDataSubBean formDataSubBean= formDataMasterBean.getFormDataSubBeanById(formName, preRecordId);
                for (Map<String, Object> map : source2TargetList) {
                    String source = MapUtils.getString(map, "source");
                    String target = MapUtils.getString(map, "target");
                    formDataSubBean.addFieldValue(target, rowData.get(source));
                    allFillBackFields.add(target +"_"+ formDataSubBean.getId());
                    formDataMasterBean.addFieldChanges4Calc(formDataMasterBean.getFieldBeanByFieldName(target), rowData.get(source), formDataSubBean);
                }
            }
        }
        //执行计算
        Set<String> fillBackFields = capRuntimeCalcManager.execCalcAll(formBean, formDataMasterBean, formAuthViewBean, allTableName2AddSubIds);
        allFillBackFields.addAll(fillBackFields);
        capRuntimeCalcManager.buildCalcAllResult(formBean, formDataMasterBean, formAuthViewBean, allTableName2AddSubIds, allFillBackFields, CAPBusinessEnum.SubBeanNewFrom.FRONT_SUBMIT, tableName2DataMaps);
        cap4FormManager.saveSessioMasterDataBean(formDataMasterBean.getId(), formDataMasterBean);
        return Maps.of("tableData", tableName2DataMaps, "chart_url", MapUtils.getString(queryInfo, "chart_url"));

    }

    private void initFormDataMasterBean(List<FormDataSubBean> formDataSubBeanList, Long preRecordId, FormDataMasterBean formDataMasterBean, String formName, FormAuthViewBean formAuthViewBean) throws BusinessException {
        CAP4FormDataManager cap4FormDataManager = (CAP4FormDataManager) AppContext.getBean("cap4FormDataManager");
        for (FormDataSubBean formDataSubBean : formDataSubBeanList) {
            Map<String, Object> rowData = formDataSubBean.getRowData();
            if (preRecordId != null) {
                // 如果前一行preId不为空，则在此行后面插入数据
                formDataMasterBean.addSubData(formName, formDataSubBean, preRecordId);
            } else {
                formDataMasterBean.addSubData(formName, formDataSubBean);
            }
            preRecordId = formDataSubBean.getId();
            FormTableBean formTableBean = formDataSubBean.getFormTable();
            for (FormFieldBean formFieldBean : formTableBean.getFields()) {
                cap4FormDataManager.procFieldDefaultVal(formDataSubBean, formAuthViewBean, false, formFieldBean);
                // 计算初始值后赋值插入的值
                Object value = rowData.get(formFieldBean.getName());
                if (value != null) {
                    formDataSubBean.addFieldValue(formFieldBean.getName(), value);
                    formDataMasterBean.addFieldChanges4Calc(formFieldBean, value, formDataSubBean);
                }
            }
        }
    }

    private Map<String, List<FormDataSubBean>> parseMappingRelation(ArrayListMultimap<String, Map<String, Object>> form2FiledMapping, Map<String, Object> queryInfo, String name, FormDataMasterBean formDataMasterBean, FormBean formBean, FormAuthViewBean formAuthViewBean) {
        Map<String, List<FormDataSubBean>> formName2FormList = new HashMap<String, List<FormDataSubBean>>();
        List<String> tempList = Lists.newArrayList();
        for (Map.Entry<String, Map<String, Object>> entry : form2FiledMapping.entries()) {
            List<FormDataSubBean> formList = Lists.newArrayList();
            String formName = entry.getKey();
            if (tempList.contains(formName)) {
                continue;
            } else {
                tempList.add(formName);
            }
            List<Map<String, Object>> source2TargetList = form2FiledMapping.get(formName);
            List<Map<String, Object>> dataList = Lists.newArrayList();
            if (queryInfo.get(name) instanceof Map) {
                dataList.add((Map<String, Object>) queryInfo.get(name));
            } else {
                dataList.addAll((List<Map<String, Object>>) queryInfo.get(name));
            }

            for (Map<String, Object> data : dataList) {
                HashMap<String, Object> rowData = new HashMap<String, Object>();
                for (Map<String, Object> map : source2TargetList) {
                    String source = MapUtils.getString(map, "source");
                    String target = MapUtils.getString(map, "target");
                    rowData.put(target, data.get(source));
                }
                FormDataSubBean formDataSubBean = createFormDataSubBean(rowData, formDataMasterBean, formBean, formAuthViewBean, formName);
                if (!formDataSubBean.isEmpty()) {
                    formList.add(formDataSubBean);
                }
            }
            formName2FormList.put(formName, formList);
        }
        return formName2FormList;
    }

    private FormDataSubBean createFormDataSubBean(HashMap<String, Object> rowData, FormDataMasterBean formDataMasterBean, FormBean formBean, FormAuthViewBean formAuthViewBean, String formName) {
        //获取可编辑的字段
        List<String> editFieldNames = Lists.newArrayList();
        for (FormFieldBean formFieldBean : formBean.getAllFieldBeans()) {
            FormAuthViewFieldBean formAuthorizationField = formAuthViewBean.getFormAuthorizationField(formFieldBean.getName());
            if (formAuthorizationField.getAccess().equals(Enums.FieldAccessType.edit.getKey())) {
                editFieldNames.add(formFieldBean.getName());
            }
        }
        //只留下拥有编辑权限的字段
        Iterator<Map.Entry<String, Object>> it = rowData.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            String fieldName = entry.getKey();
            if (!editFieldNames.contains(fieldName)) {
                it.remove();
            }
        }

        FormDataSubBean formDataSubBean = new FormDataSubBean(rowData,
                formBean.getTableByTableName(formName), formDataMasterBean, true);
        formDataSubBean.setId(UUIDLong.longUUID());
        return formDataSubBean;
    }

    private ArrayListMultimap<String, Map<String, Object>> getForm2FiledMapping(List<Map<String, Object>> listData) {
        //先过滤下 source或者taget为空的 映射 直接去掉
        Iterator<Map<String, Object>> iterator = listData.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> next = iterator.next();
            if (StringUtils.isEmpty(MapUtils.getString(next, "source")) || StringUtils.isEmpty(MapUtils.getString(next, "target"))) {
                iterator.remove();
            }
        }
        ArrayListMultimap<String, Map<String, Object>> multimap = ArrayListMultimap.create();

        for (Map<String, Object> map : listData) {
            multimap.put(MapUtils.getString(map, "tableName"), map);
        }
        return multimap;
    }


    /**
     * 接收企业信息后的回调rest
     *
     * @param name      公司名称
     * @param formId    表单id
     * @param fieldName 字段名称
     * @param masterId  主表id
     * @param subId     明细表id
     * @return
     * @throws BusinessException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("parseCreditAndFillBack")
    @SuppressWarnings("unchecked")
    public Response parseCreditqueryFileAndFillBack(@QueryParam("name") String name, @QueryParam("formId") String formId, @QueryParam("fieldName") String fieldName,
                                                    @QueryParam("masterId") String masterId, @QueryParam("subId") String subId) throws BusinessException {
        MplusApi mplusApi = (MplusApi) AppContext.getBean("mplusApi");
        String domain = mplusApi.getDomain();
        String ticket = mplusApi.getTicket(serviceCode);
        String url = domain + "/svr/enterprise/info";
        Map<String, Object> jsonObject = new HashMap<String, Object>();
        jsonObject.put("ticket", ticket);
        jsonObject.put("keyword", name);
        jsonObject.put("type", "1");
        jsonObject.put("parameterType", "2");
        Map<String, Object> res = HttpClientUtil.doPost(url, jsonObject);
        Map<String, Object> data = (Map<String, Object>) res.get("data");
        String state = res.get("code").toString();
        if (!"1000".equals(state)) {
            BusinessException e = new BusinessException(res.get("msg").toString());
            LOGGER.error(res.get("msg").toString(), e);
            throw e;
        }

        CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
        FormBean formBean = cap4FormManager.getForm(Long.valueOf(formId), false);
        FormFieldBean field = formBean.getFieldBeanByName(fieldName);
        //FormAuthViewBean auth = formBean.getAuthViewBeanById(Long.parseLong(rightId));
        //解析配置的映射
        String customParams = field.getCustomParam();

        // 通过主表数据id从session中取到数据对象
        FormDataMasterBean cacheFormData = cap4FormManager.getSessioMasterDataBean(Long.parseLong(masterId));
        if (null == cacheFormData) {
            throw new BusinessException("表单数据在session中找不到（masterId:" + masterId + "），请尝试重新打开。");
        }
        FormDataSubBean formSubData = null;
        if (!field.isMasterField()) {
            //如果是明细表字段，通过明细表数据id取到明细表数据行对象
            formSubData = cacheFormData.getFormDataSubBeanById(field.getOwnerTableName(), Long.parseLong(subId));
            if (null == formSubData) {
                throw new BusinessException("企业征信控件是明细表字段，但是通过明细表行id：" + subId + "找不到明细表数据");
            }
        }

        Map<String, Object> definition = null;
        List<Map<String, String>> array = null;
        if (Strings.isNotBlank(customParams)) {
            definition = (Map<String, Object>) JSONUtil.parseJSONString(customParams);
            array = (List<Map<String, String>>) definition.get("mapping");
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("chart_url", data.get("chart_url"));
        if (array != null) {
            for (Map<String, String> defMap : array) {
                String source = defMap.get("source");
                String target = defMap.get("target");
                FormFieldBean conffield = formBean.getFieldBeanByName(target);
                Object val = data.get(source);
                //主表字段
                if (conffield.isMasterField()) {
                    //合并数据到内存中
                    cacheFormData.addFieldValue(target, val);
                    val = cacheFormData.getFieldValue(target);
                    cacheFormData.addFieldChanges4Calc(conffield, val, null);
                    Map<String, Object> tempRes = CAPFormUtil.getDisplayValueMap(val, conffield, null);
                    result.put(target, tempRes);
                } else {//明细表字段
                    //合并数据到内存中
                    if (null != formSubData) {
                        formSubData.addFieldValue(target, val);
                        val = formSubData.getFieldValue(target);
                        cacheFormData.addFieldChanges4Calc(conffield, val, formSubData);
                        Map<String, Object> tempRes = CAPFormUtil.getDisplayValueMap(val, conffield, null);
                        result.put(target, tempRes);
                    }
                }
            }
        }

        return success(result);
    }


    /**
     * 检查数据绑定配置是否正确
     *
     * @param params <pre>
     *                                                                                                                                                                                                      类型                             名称        必填          备注
     *                                                                                                                                                                                                      Long                            formId      Y            表单ID
     *                                                                                                                                                                                                      ArrayList<Map<String, String>>   datas      Y            数据绑定信息
     *                                                                                                                                                                                                     </pre>
     * @return
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("checkMapping")
    @SuppressWarnings("unchecked")
    public Response checkMapping(Map<String, Object> params) {
        Long formId = Long.parseLong("" + params.get("formId"));
        CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
        FormBean formBean = cap4FormManager.getEditingForm(Long.valueOf(formId));
        Object datas = params.get("datas");
        List<Map<String, String>> mappings = (List<Map<String, String>>) datas;
        boolean checkResult = true;
        String errorMsg = "";
        Map<String, Object> result = new HashMap<String, Object>();
        for (Map<String, String> mapping : mappings) {
            String source = mapping.get("source");
            String target = mapping.get("target");
            FormFieldBean fieldBean = formBean.getFieldBeanByName(target);
            CreditqueryFieldType sourceType = CreditqueryFieldType.getEnumByKey(source);
            FormFieldComEnum[] supportTypes = sourceType.getSupportFieldType();
            boolean hasEquals = false;
            for (FormFieldComEnum supportField : supportTypes) {
                if (supportField.getKey().equals(fieldBean.getInputType())) {
                    //企业征信中无数字类型,如果是数字,则直接返回错误
                    if (fieldBean.getFieldType().equals(Enums.FieldType.DECIMAL.getKey())) {
                        break;
                    } else {
                        hasEquals = true;
                        break;
                    }
                }
            }
            if (!hasEquals) {
                String supportTypeStrs = "";
                FormFieldComEnum[] types = sourceType.getSupportFieldType();
                for (int i = 0; i < types.length; i++) {
                    FormFieldComEnum t = types[i];
                    //最后一个
                    if (i == types.length - 1) {
                        supportTypeStrs = supportTypeStrs + t.getText();
                    } else {
                        supportTypeStrs = supportTypeStrs + t.getText() + "、";
                    }
                }
                String fieldType = "";
                if (fieldBean.isNumberField()) {
                    fieldType = ResourceUtil.getString("cap.formDesign.ctrlArea.number");
                } else {
                    fieldType = fieldBean.getFieldCtrl().getText();
                }
                errorMsg = sourceType.getText() + "[" + supportTypeStrs + "]->" + fieldBean.getDisplay() + "[" + fieldType + "]" + ResourceUtil.getString("form.business.relation.mapping.unique.type.notsame.tips");
                result.put("errorMsg", errorMsg);
                checkResult = false;
                break;
            }
        }
        result.put("result", String.valueOf(checkResult));
        return success(result);
    }


    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("checkServiceUsable")
    public Map<String, Object> checkServiceUsable() throws BusinessException {
        MplusApi mplusApi = (MplusApi) AppContext.getBean("mplusApi");
        String domain = mplusApi.getDomain();
        String ticket = mplusApi.getTicket(serviceCode);
        String url = domain + "/svr/usable";
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("ticket", ticket);
        json.put("parameterType", "2");
        Map<String, Object> res = HttpClientUtil.doPost(url, json);
        String state = res.get("code").toString();
        if (!"1000".equals(state)) {
            throw new BusinessException(res.get("msg").toString());
        }
        List<Map<String, Object>> data = (List<Map<String, Object>>) res.get("data");
        Map<String, Object> map = data.get(1);
        return map;
    }

    public static class CreditQueryParamBean {
        //搜索词
        private String name;
        private String fieldName;
        private Long formId;
        private Long formMasterId;
        private Long rightId;
        private Map<String, Object> mergeData;
        private List<DetailFormInfo> detailFormInfos;

        public CreditQueryParamBean() {
        }

        public CreditQueryParamBean(String name, String fieldName, Long formId, Long formMasterId, Long rightId, Map<String, Object> mergeData, List<DetailFormInfo> detailFormInfos) {
            this.name = name;
            this.fieldName = fieldName;
            this.formId = formId;
            this.formMasterId = formMasterId;
            this.rightId = rightId;
            this.mergeData = mergeData;
            this.detailFormInfos = detailFormInfos;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public Long getFormId() {
            return formId;
        }

        public void setFormId(Long formId) {
            this.formId = formId;
        }

        public Long getFormMasterId() {
            return formMasterId;
        }

        public void setFormMasterId(Long formMasterId) {
            this.formMasterId = formMasterId;
        }

        public Long getRightId() {
            return rightId;
        }

        public void setRightId(Long rightId) {
            this.rightId = rightId;
        }

        public Map<String, Object> getMergeData() {
            return mergeData;
        }

        public void setMergeData(Map<String, Object> mergeData) {
            this.mergeData = mergeData;
        }

        public List<DetailFormInfo> getDetailFormInfos() {
            return detailFormInfos;
        }

        public void setDetailFormInfos(List<DetailFormInfo> detailFormInfos) {
            this.detailFormInfos = detailFormInfos;
        }
    }

    public static class DetailFormInfo {
        private String tableName;
        private Long preRecordId;
        private Long firstRecordId;

        public DetailFormInfo() {
        }

        public DetailFormInfo(String tableName, Long preRecordId, Long firstRecordId) {
            this.tableName = tableName;
            this.preRecordId = preRecordId;
            this.firstRecordId = firstRecordId;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public Long getPreRecordId() {
            return preRecordId;
        }

        public void setPreRecordId(Long preRecordId) {
            this.preRecordId = preRecordId;
        }

        public Long getFirstRecordId() {
            return firstRecordId;
        }

        public void setFirstRecordId(Long firstRecordId) {
            this.firstRecordId = firstRecordId;
        }
    }
}
