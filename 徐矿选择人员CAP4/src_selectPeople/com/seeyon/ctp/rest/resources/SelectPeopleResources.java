package com.seeyon.ctp.rest.resources;

import com.seeyon.apps.customFieldCtrl.constants.SelectPeople;
import com.seeyon.apps.customFieldCtrl.kit.CAP4FormKit;
import com.seeyon.apps.customFieldCtrl.kit.StrKit;
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
    public Response backfillpeopleInfo(@QueryParam("formId") String formId, @QueryParam("masterId") String masterId, @QueryParam("value") String value) throws BusinessException {
        FormBean formBean = cap4FormManager.getForm(Long.valueOf(formId), false);
        FormDataMasterBean cacheFormData = cap4FormManager.getSessioMasterDataBean(Long.valueOf(Long.parseLong(masterId)));

        if (null == cacheFormData) {
            return fail("表单数据在session中找不到（masterId:" + masterId + "），请尝试重新打开。");
        }

        List<FormTableBean> subForms = formBean.getSubTableBean();
        FormTableBean subForm = subForms.get(0);
        List<FormDataSubBean> subs = cacheFormData.getSubData(subForm.getTableName());
        Map<String, Object> result = new HashMap<String, Object>();

        result.put("subTbName", subForm.getTableName());

        FormDataSubBean subdata=null;
//        // 赋值bug修改   新增到空白的一行上面
//        List<FormDataSubBean> filterList = subs.stream().filter(item->
//                StrKit.isNull(CAP4FormKit.getFieldValue(item, SelectPeople.field0004.getText())))
//                .collect(Collectors.toList());
//
//        if(StrKit.isNull(filterList)) {
//            result.put("add", true);
//            return success(result);
//        }

        subdata = subs.get(0);

//        Map<String, Object> filldatas = new HashMap<String, Object>();
//        // 处理回填数据的问题 只有第一次需要进行此处理
//        Set<String> fillBackFields = new HashSet<String>();
//
//        Map<String, Object> masterMap = cacheFormData.getAllDataMap();
        result.put("recordId",Long.toString(subdata.getId()));
        result.put("add",false);

        return success(result);
    }
}
