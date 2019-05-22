package com.seeyon.ctp.rest.resources;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.seeyon.apps.customFieldCtrl.constants.SelectPeople;
import com.seeyon.apps.customFieldCtrl.kit.CAP4FormKit;
import com.seeyon.apps.customFieldCtrl.kit.StrKit;
import com.seeyon.apps.customFieldCtrl.vo.ZJsonObject;
import com.seeyon.cap4.form.bean.*;
import com.seeyon.cap4.form.modules.engin.base.formData.CAP4FormDataManager;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.cap4.template.util.CAPFormUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 周刘成   2019/5/16
 */
@Path("cap4/selectPeople")
@Consumes({"application/json"})
@Produces({"application/json"})
public class SelectPeopleResources extends BaseResource {

    private static CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");

    private static CAP4FormDataManager cap4FormDataManager = (CAP4FormDataManager) AppContext.getBean("cap4FormDataManager");

    private static Random rand = new Random();

    @GET
    @Produces({"application/json"})
    @Path("backfillpeopleInfo")
    public Response backfillpeopleInfo(@QueryParam("masterId") String masterId,
                                       @QueryParam("dataInfo") String dataInfo) throws BusinessException {

        //fastjson 解析json字符串
        JSONObject jsonObject = JSON.parseObject(dataInfo);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        List<ZJsonObject> list = JSON.parseObject(jsonArray.toJSONString(), new TypeReference<List<ZJsonObject>>() {
        });

        //formBean是主表的信息
        FormDataMasterBean cacheFormData = cap4FormManager.getSessioMasterDataBean(Long.valueOf(Long.parseLong(masterId)));
        Map<String, Object> result = new HashMap<String, Object>();
        if (null == cacheFormData) {
            return fail("表单数据在session中找不到（masterId:" + masterId + "），请尝试重新打开。");
        }
        try {

            //明细行信息，使用cap4提供
            List<FormDataSubBean> subBeans = CAP4FormKit.getSubBeans(cacheFormData);
            // 增加一个逻辑：先判断是否含有这个数据
            List<FormDataSubBean> filterList = null;

            // 赋值bug修改   新增到空白的一行上面
            filterList = subBeans.stream().filter(item ->
                    StrKit.isNull(CAP4FormKit.getFieldValue(item, SelectPeople.field0001.getText())))
                    .collect(Collectors.toList());


            String tableName = subBeans.get(0).getFormTable().getTableName();
            result.put("tableName", tableName);
            List<FormDataSubBean> subs = cacheFormData.getSubData(tableName);


            if (StrKit.isNull(filterList) || filterList.size()==0) {
                result.put("add", true);
                return success(result);
            } else {
                result.put("add", false);
            }

            Map<String, Object> masterMap = filterList.get(0).getRowData();


            Map<String, Object> filldatas = new HashMap<>();



            int count = 1;
            for (String key : masterMap.keySet()) {

                if (key.startsWith("field")) {
                    Object fieldVal = masterMap.get(key);
                    if (null == fieldVal) {
                        Map<String, Object> subTemp1 = new HashMap<>();
                        subTemp1.put("showValue", list.get(0).getField0001());
                        subTemp1.put("showValue2", list.get(0).getField0001());
                        subTemp1.put("value", list.get(0).getField0001());

                        Map<String, Object> subTemp2 = new HashMap<>();
                        subTemp2.put("showValue", list.get(0).getField0004());
                        subTemp2.put("showValue2", list.get(0).getField0004());
                        subTemp2.put("value", list.get(0).getField0004());

                        Map<String, Object> subTemp3 = new HashMap<>();
                        subTemp3.put("showValue", list.get(0).getField0003());
                        subTemp3.put("showValue2", list.get(0).getField0003());
                        subTemp3.put("value", list.get(0).getField0003());
                        if (count == 1) {
                            filldatas.put(key, subTemp1);
                        }
                        if (count == 2) {
                            filldatas.put(key, subTemp2);
                        }
                        if (count == 3) {
                            filldatas.put(key, subTemp3);
                        }
                        count++;
                    }
                }
            }

            result.put("recordId", filterList.get(0).getId() + "");
            result.put("data", filldatas);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return success(result);
    }

//    @GET
//    @Produces({"application/json"})
//    @Path("backfillpeopleInfo")
//    public Response backfillpeopleInfo(@QueryParam("masterId") String masterId,
//                                       @QueryParam("value") String value,
//                                       @QueryParam("subId") String recordId,
//                                       @QueryParam("dataInfo") String dataInfo) throws BusinessException {
//        //formBean是主表的信息
//        FormBean formBean = cap4FormManager.getForm(Long.valueOf(formId), false);
//        FormDataMasterBean cacheFormData = cap4FormManager.getSessioMasterDataBean(Long.valueOf(Long.parseLong(masterId)));
//        String checkMaster=cap4FormManager.checkSessioMasterDataExists(masterId);
//
//        if (null == cacheFormData) {
//            return fail("表单数据在session中找不到（masterId:" + masterId + "），请尝试重新打开。");
//        }
//        //subForms是明细表信息
//        List<FormTableBean> subForms = formBean.getSubTableBean();
//        FormTableBean subForm = subForms.get(0);
//        //zlc ->这是获取明细表字段
//        Map<String, FormFieldBean> fieldMap = subForm.getFieldMap4Name();
//
//        //fastjson 解析json字符串
//        JSONObject jsonObject = JSON.parseObject(dataInfo);
//        JSONArray jsonArray = jsonObject.getJSONArray("data");
//        List<ZJsonObject> list = JSON.parseObject(jsonArray.toJSONString(), new TypeReference<List<ZJsonObject>>() {
//        });
//
//        //我的想法：先将字段放进一个map集合中，值都为空
//        List<String> fieldListKey = new ArrayList<>();
//        for (Map.Entry entry : fieldMap.entrySet()) {
//            fieldListKey.add((String) entry.getKey());
//        }
//
//        List parentList = new ArrayList();
//
//        for (ZJsonObject zj : list) {
//            Map showValue1 = new HashMap();
//            showValue1.put("showValue", zj.getField0001());
//            showValue1.put("showValue2", zj.getField0001());
//            showValue1.put("value", zj.getField0001());
//
//            Map showValue2 = new HashMap();
//            showValue2.put("showValue", zj.getField0003());
//            showValue2.put("showValue2", zj.getField0003());
//            showValue2.put("value", zj.getField0003());
//
//            Map showValue3 = new HashMap();
//            showValue3.put("showValue", zj.getField0004());
//            showValue3.put("showValue2", zj.getField0004());
//            showValue3.put("value", zj.getField0004());
//
//            Map<String, Object> m = new HashMap<>();
//            for (int i = 0; i < fieldListKey.size(); i++) {
//                if (i == 0) {
//                    m.put(fieldListKey.get(i), showValue1);
//                }
//                if (i == 1) {
//                    m.put(fieldListKey.get(i), showValue2);
//                }
//                if (i == 2) {
//                    m.put(fieldListKey.get(i), showValue3);
//                }
//
//            }
//            parentList.add(m);
//        }
//
//
//        List<FormDataSubBean> subs = cacheFormData.getSubData(subForm.getTableName());
//        Map<String, Object> result = new HashMap<String, Object>();
//
//        result.put("subTbName", subForm.getTableName());
//
//        FormDataSubBean subdata = null;
//////        // 赋值bug修改   新增到空白的一行上面
////        List<FormDataSubBean> filterList = subs.stream().filter(item ->
////                StrKit.isNull(CAP4FormKit.getFieldValue(item, SelectPeople.field0001.getText())))
////                .collect(Collectors.toList());
////
////        if (StrKit.isNull(filterList)) {
////            result.put("add", true);
////            return success(result);
////        }
//
//        Map<String, Object> masterMap = cacheFormData.getAllDataMap();
//        subdata = subs.get(0);
//        result.put("recordId", Long.toString(subdata.getId()));
//        result.put("add", false);
//
//        return success(result);
//    }
}
