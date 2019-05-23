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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private Logger logger = LoggerFactory.getLogger(SelectPeopleResources.class);

    private static CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");

    private static CAP4FormDataManager cap4FormDataManager = (CAP4FormDataManager) AppContext.getBean("cap4FormDataManager");

    private static Random rand = new Random();

    @GET
    @Produces({"application/json"})
    @Path("backfillpeopleInfo")
    public Response backfillpeopleInfo(@QueryParam("masterId") String masterId,
                                       @QueryParam("flag") int isNext,
                                       @QueryParam("dataInfo") String dataInfo) throws BusinessException {

        //fastjson 解析json字符串
        JSONObject jsonObject = JSON.parseObject(dataInfo);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        List<ZJsonObject> listJson = JSON.parseObject(jsonArray.toJSONString(), new TypeReference<List<ZJsonObject>>() {
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

            //过滤明细行，排除有数据的明细行
            List<FormDataSubBean> excludeExist = new ArrayList<>();

//            List<ZJsonObject> zJsonObjectList = new ArrayList<>();
            for (int i = 0; i < subBeans.size(); i++) {
                Map<String, Object> map = subBeans.get(i).getRowData();
                for (String key : map.keySet()) {
                    if (key.startsWith("field")) {
                        Object fieldVal = map.get(key);
                        if (null == fieldVal) {
                            excludeExist.add(subBeans.get(i));
                            break;
                        } else {
                            String val = fieldVal + "";
                            for (int j = 0; j < listJson.size(); j++) {
                                ZJsonObject zj = listJson.get(j);
                                if (val.equals(zj.getField0001())) {
                                    listJson.remove(j);
                                }
                            }
                            break;
                        }
                    }
                }
            }

            String tableName = subBeans.get(0).getFormTable().getTableName();
            result.put("tableName", tableName);
            int newNext = isNext + 1;
            if (newNext <= 0) {
                result.put("add", false);
            } else {
                result.put("add", true);
            }
            Map<String, Object> filldatas = null;

            Map<String, Object> dataMap = null;
            List<Object> listMap = new ArrayList<>();
            for (int i = 0; i < excludeExist.size(); i++) {
                Map<String, Object> masterMap = excludeExist.get(i).getRowData();
                filldatas = new HashMap<>();
                dataMap = new HashMap<>();
                ZJsonObject zJsonObject = listJson.get(i);
                Map<String, Object> subTemp1 = new HashMap<>();

                subTemp1.put("showValue", zJsonObject.getField0001());
                subTemp1.put("showValue2", zJsonObject.getField0001());
                subTemp1.put("value", zJsonObject.getField0001());

                Map<String, Object> subTemp2 = new HashMap<>();
                subTemp2.put("showValue", zJsonObject.getField0004());
                subTemp2.put("showValue2", zJsonObject.getField0004());
                subTemp2.put("value", zJsonObject.getField0004());

                Map<String, Object> subTemp3 = new HashMap<>();
                subTemp3.put("showValue", zJsonObject.getField0003());
                subTemp3.put("showValue2", zJsonObject.getField0003());
                subTemp3.put("value", zJsonObject.getField0003());

                int count = 1;

                for (String key : masterMap.keySet()) {
                    if (key.startsWith("field")) {
                        Object fieldVal = masterMap.get(key);
                        if (null == fieldVal) {
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
                            dataMap.put(masterMap.get("id") + "", filldatas);
                        }
                        dataMap.put("recordId", masterMap.get("id") + "");
                    }
                }
                listMap.add(dataMap);
            }
            result.put("data", listMap);
        } catch (Exception e) {
            System.out.println("选择人员回填数据报错了：" + e.getMessage());
            logger.error("选择人员回填数据报错了：" + e.getMessage());
        }

        return success(result);
    }


}
