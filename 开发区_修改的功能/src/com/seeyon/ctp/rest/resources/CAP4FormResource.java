package com.seeyon.ctp.rest.resources;

import com.seeyon.cap4.form.bean.button.Button;
import com.seeyon.cap4.form.bean.button.CommonBtn;
import com.seeyon.cap4.form.bean.fieldCtrl.FormFieldUtil;
import com.seeyon.cap4.form.modules.engin.design.CAP4FormDesignManager;
import com.seeyon.cap4.form.modules.engin.plugin.FormPluginParamBean;
import com.seeyon.cap4.form.modules.index.UnflowFormIndex;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.cap4.template.constant.CAPBusinessEnum;
import com.seeyon.cap4.template.service.CAPBatchOperationService;
import com.seeyon.cap4.template.service.CAPConditionService;
import com.seeyon.cap4.template.service.CAPFormDataOpenService;
import com.seeyon.cap4.template.service.CAPFormDataService;
import com.seeyon.cap4.template.service.CAPFormDataSignatureService;
import com.seeyon.cap4.template.service.CAPFormDataListService;
import com.seeyon.cap4.template.service.CAPFormPluginService;
import com.seeyon.cap4.template.service.CAPFormRelationService;
import com.seeyon.cap4.template.service.CAPFormToCollService;
import com.seeyon.cap4.template.service.CAPFormToUnflowService;
import com.seeyon.cap4.template.service.CAPImportService;
import com.seeyon.cap4.template.service.CAPScreenCaptureService;
import com.seeyon.cap4.template.service.ServiceProxy;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceLoader;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation;
import org.apache.commons.logging.Log;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaox on 2017/9/25.
 * 表单相关接口
 * 表单新建,保存,查看,关联,计算公式,表单合并,增加删除明细行
 */
@Path("cap4/form")
@Consumes({MediaType.APPLICATION_JSON})
@Produces(MediaType.APPLICATION_JSON)
public class CAP4FormResource extends BaseResource {
    private static final Log LOGGER = CtpLogFactory.getLog(CAP4FormResource.class);
    CAPFormDataService capFormDataService = (CAPFormDataService) AppContext.getBean("capFormDataService");
    CAPFormDataOpenService capFormDataOpenService = (CAPFormDataOpenService) AppContext.getBean("capFormDataOpenService");
    CAPFormDataSignatureService capFormDataSignatureService = (CAPFormDataSignatureService) AppContext.getBean("capFormDataSignatureService");
    CAPBatchOperationService capBatchOperationService = (CAPBatchOperationService) AppContext.getBean("capBatchOperationService");
    CAPScreenCaptureService capScreenCaptureService = (CAPScreenCaptureService) AppContext.getBean("capScreenCaptureService");
    CAPFormToCollService capFormToCollService = (CAPFormToCollService) AppContext.getBean("capFormToCollService");
    CAPFormRelationService capFormRelationService = (CAPFormRelationService) AppContext.getBean("capFormRelationService");
    CAPFormToUnflowService capFormToUnflowService = (CAPFormToUnflowService) AppContext.getBean("capFormToUnflowService");
    CAPConditionService capConditionService = (CAPConditionService) AppContext.getBean("capConditionService");
    CAPFormPluginService capFormPluginService = (CAPFormPluginService) AppContext.getBean("capFormPluginService");
    CAPFormDataListService capFormDataListService = (CAPFormDataListService) AppContext.getBean("capFormDataListService");
    CAP4FormDesignManager cap4FormDesignManager = (CAP4FormDesignManager)AppContext.getBean("cap4FormDesignManager");
    CAPImportService capImportService = (CAPImportService)AppContext.getBean("capImportService");

    @POST
    @Path("getWhitePlugins")
    public Response getWhitePlugins(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capFormPluginService, "getWhitePlugins", params));
    }

    @POST
    @Path("getConfigedPlugins")
    public Response getConfigedPlugins(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capFormPluginService, "getConfigedPlugins", params));
    }

    @POST
    @Path("saveConfigPlugins")
    public Response saveConfigPlugins(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capFormPluginService, "saveConfigPlugins", params));
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("initFormPlugins")
    public Response initFormPlugins() {
        capFormPluginService.initFormPlugins();
        return success("");
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("pluginScripts")
    public Response pluginScripts() {
        FormPluginParamBean paramBean = new FormPluginParamBean(AppContext.getRawRequest().getParameterMap());
        return Response.ok(capFormPluginService.getFormPlugins4Document(paramBean)).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("plugins/{version}")
    public Response plugins(@PathParam("version") String version) {
        FormPluginParamBean paramBean = new FormPluginParamBean();
        paramBean.setVersion(version);
        HttpServletRequest request = AppContext.getRawRequest();
        paramBean.setModuleId(request.getParameter("moduleId"));
        paramBean.setModuleType(request.getParameter("moduleType"));
        paramBean.setClient(request.getParameter("client"));
        return success(capFormPluginService.getFormPlugins(paramBean));
    }


    /**
     * PC端流程表单：一键复制表单数据
     */
    @POST
    @Path("copyFormData")
    public Response copyFormData(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capFormDataService, "copyFormData", params));
    }

    /**
     * 移动端无流程表单：获取表单的视图信息
     */
    @POST
    @Path("getFormDataRightInfo")
    public Response getFormDataRightInfo(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capFormDataService, "getFormDataRightInfo", params));
    }

    /**
     * 自动汇总、自动新建明细行
     */
    @POST
    @Path("refreshSubData")
    public Response refreshSubData(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capFormRelationService, "refreshSubData", params));
    }

    /**
     * 无流程验证数据是否锁定，包括编辑锁定
     * TODO: 20190730版本去掉, 当前移动端无流程表单还在使用 marked by ouyp 2019/04/25
     */
    @Deprecated
    @POST
    @Path("checkLock")
    public Response checkLock(Map<String, Object> params) {
        return success(capFormToUnflowService.checkLock(params));
    }

    /**
     * 无流程校验权限
     * TODO: 20190730版本去掉, 当前移动端无流程表单还在使用 marked by ouyp 2019/04/25
     */
    @POST
    @Path("validUnflowOperation")
    public Response validUnflowOperation(Map<String, Object> params) {
        return success(capFormToUnflowService.validUnflowOperation(params));
    }

    /**
     * 列表数据——校验按钮
     */
    @POST
    @Path("validBindButton")
    public Response validBindButton(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capFormToUnflowService, "checkButton", params));
    }

    /**
     * 获取流程待办数据
     */
    @POST
    @Path("getToDoItems")
    public Response getToDoItems(Map<String, Object> params) {
        params.put("queryType", CAPBusinessEnum.DataListTypeEnum.FLOW_TO_DO.getValue());
        return success(ServiceProxy.invoke(capFormDataListService, "getList", params));
    }

    /**
     * 获取无流程列表数据
     */
    @POST
    @Path("getCAPFormUnFlowList")
    public Response getCAPFormUnFlowList(Map<String, Object> params) {
        params.put("queryType", CAPBusinessEnum.DataListTypeEnum.UN_FLOW.getValue());
        return success(ServiceProxy.invoke(capFormDataListService, "getList", params));
    }

    /**
     * 无流程表单——字段列计算
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("getCAPFormFieldCalcData")
    public Response getCAPFormFieldCalcData(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capFormToUnflowService, "getFieldCalcData" , params));
    }

    /**
     * 无流程表单——数据筛选
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("getCAPFormFieldListData")
    public Response getCAPFormFieldListData(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capFormToUnflowService, "getFieldDataList" , params));
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("getExcelSheets/{fileId}")
    public Response getExcelSheets(@PathParam("fileId") String fileId) {
        return success(capBatchOperationService.getExcelSheets(fileId));
    }

    /**
     * 无流程批量操作
     * 批量导入
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("importFormExcelDatas")
    public Response importFormExcelDatas(Map<String, Object> params) {
        // return success(capBatchOperationService.importFormExcelDatas(params));
        return success(capImportService.readData(params));
    }

    /**
     * 无流程批量操作
     * 批量替换
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("updateFormExcelDatas")
    public Response updateFormExcelDatas(Map<String, Object> params) {
        // return success(capBatchOperationService.updateFormExcelDatas(params));
        return success(ServiceProxy.invoke(capImportService, "updateCacheDBData" , params));
    }

    /**
     * 无流程批量操作
     * 获取数据列表
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("getFormExcelDatas")
    public Response getFormExcelDatas(Map<String, Object> params) {
        // return success(capBatchOperationService.getFormExcelDatas(params));
        return success(ServiceProxy.invoke(capImportService, "readDataFromCacheDB" , params));
    }

    /**
     * 无流程批量操作
     * 批量修改和批量导入数据入库
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("transFormExcelDatas")
    public Response transFormExcelDatas(Map<String, Object> params) {
        if (CAPBusinessEnum.BatchOperationEnum.BATCH_UPDATE.getKey().equals(params.get("type"))) {
            return success(capBatchOperationService.transFormExcelDatas(params));
        } else {
            return success(capImportService.execute(params));
        }
    }

    /**
     * 无流程批量操作
     * 无流程数据导出，模版导出
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("exportUnflowExcel")
    public Response exportUnflowExcel(Map<String, Object> params) {
        return success(capBatchOperationService.exportUnflowExcel(params,null));
    }
    /**
     * 明细表导出前检查明细表字段
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("checkSubTableExportField")
    public Response checkSubTableExportField(Map<String, Object> params) {
        return success(capBatchOperationService.checkSubTableExportField(params));
    }

    /**
     * 批量操作
     * 删除批量操作数据，删除与删除全部
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("deleteBatchOperationData")
    public Response deleteBatchOperationData(Map<String, Object> params) {
        return success(capBatchOperationService.deleteBatchOperationData(params));
    }

    /**
     * 批量操作
     * toolbar批量修改接口
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("updateBatchOperationData")
    public Response updateBatchOperationData(Map<String, Object> params) {
        return success(capBatchOperationService.updateBatchOperationData(params));
    }

    /**
     * 批量操作
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("updateBatchRefreshData")
    public Response updateBatchRefreshData(Map<String, Object> params) {
        return success(capBatchOperationService.updateBatchRefreshData(params));
    }

    /**
     * 批量操作
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("deleteTempData")
    public Response deleteTempData(Map<String, Object> params) {
        return success(capBatchOperationService.deleteTempData(params));
    }

    /**
     * 转发
     * 必填：summaryId、affairId、forwardOriginalNote(0 or 1)、forwardOriginalopinion(0 or 1)、track(0 or 1)
     * 可选：comment
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("transDoForward")
    public Response transDoForward(Map<String, Object> params) {
        return success(capFormToCollService.transDoForward(params));
    }

    /**
     * 删除
     * 必填：affairId、pageType
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("deleteAffair")
    public Response deleteAffair(Map<String, Object> params) {
        return success(capFormToCollService.deleteAffair(params));
    }

    /**
     * 取回
     * 必填：affairId、isSaveOpinion(0 or 1)
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("takeBack")
    public Response takeBack(Map<String, Object> params) {
        return success(capFormToCollService.takeBack(params));
    }

    /**
     * 是否能取回
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("checkTakeBack")
    public Response checkTakeBack(Map<String, Object> params) {
        return success(capFormToCollService.checkTakeBack(params));
    }

    /**
     * 撤销流程
     * 必填：affairId、content
     * 可选：isWFTrace(0 or 1)
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("transRepeal")
    public Response transRepeal(Map<String, Object> params) {
        return success(capFormToCollService.transRepeal(params));
    }

    /**
     * 是否能撤销流程
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("checkTransRepeal")
    public Response checkTransRepeal(Map<String, Object> params) {
        return success(capFormToCollService.checkTransRepeal(params));
    }

    /**
     * 发起新的协同流程
     * 必填：sendType、templateId、formMasterId
     * 可选：parentSummaryId、newSumamryId
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("transSendColl")
    public Response transSendColl(Map<String, Object> params) {
        return success(capFormToCollService.transSendColl(params));
    }

    /**
     * 检查是否有转发权限
     * @param params affairIds : sumaryId_AffairId,sumaryId_AffairId
     * @return
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("checkForwardPermission")
    public Response checkForwardPermission(Map<String, Object> params) {
        return success(capFormToCollService.checkForwardPermission(params));
    }

    /**
     * 待办列表点击发送
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("sendFromWait")
    public Response sendFromWait(Map<String, Object> params) {
        return success(capFormToCollService.sendFromWait(params));
    }

    /**
     * 处理协同
     * 必填：affairId、summaryId、comment
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("finishWorkItem")
    public Response finishWorkItem(Map<String, Object> params) {
        return success(capFormToCollService.finishWorkItem(params));
    }

    /**
     * 回退协同
     * 必填：affairId、comment、isWFTrace
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("transStepBack")
    public Response transStepBack(Map<String, Object> params) {
        return success(capFormToCollService.transStepBack(params));
    }

    /**
     * 手动关联 选择数据后 发起关联的拷贝处理
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("dealSelectedRelationData")
    public Response dealSelectedRelationData(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capFormRelationService, "dealSelectedRelationData", params));
    }

    /**
     * 手动关联 清除选择器数据
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("cleanRelationData")
    public Response cleanRelationData(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capFormRelationService, "cleanRelationData", params));
    }

    /**
     * 手动关联
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("getFormRelationDatas")
    public Response getFormRelationDatas(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capFormRelationService, "getFormRelationDatas", params));
    }

    /**
     * 删除无流程表单数据
     */
    @POST
    @Path("delFormData")
    public Response delFormData(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capFormToUnflowService, "delFormData", params));
    }

    /**
     * 无流程锁定或解锁
     */
    @POST
    @Path("setLockOrUnlock")
    public Response setLockOrUnlock(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capFormToUnflowService, "setLockOrUnlock", params));
    }

    /**
     * 表单新建or打开
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("createOrEdit")
    public Response createOrEdit(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capFormDataOpenService, "createOrEditForm", params));
    }

    /**
     * 表单保存
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("saveOrUpdate")
    public Response save(Map<String, Object> params) {
        return success(capFormDataService.saveOrUpdateForm(params));
    }

    /**
     * 移除表单缓存
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("removeSessionFormCache/{contentDataId}")
    public Response removeSessionFormCache(@PathParam("contentDataId") String contentDataId) {
        return success(capFormDataService.removeSessionFormCache(contentDataId));
    }

    /**
     * 移除表单缓存
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("removeSessionFormCache")
    public Response removeSessionFormCache() {
        HttpServletRequest request = AppContext.getRawRequest();
        String moduleId = request.getParameter("moduleId");
        String moduleType = request.getParameter("moduleType");
        String contentDataId = request.getParameter("contentDataId");
        return success(capFormDataService.removeSessionFormCache(contentDataId, moduleId, moduleType));
    }

    /**
     * 表单计算公式/系统关联/标间关联
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("calculate")
    public Response calculate(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capFormDataService, "calculate", params));
    }

    /**
     * 明细表删除与增加
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("addOrDelDataSubBean")
    public Response addOrDelDataSubBean(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capFormDataService, "addOrDelDataSubBean", params));
    }

    /**
     * 明细表权限校验
     * @param params
     * @return
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("validateSubTableAuth")
    public Response validateSubTableAuth(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capFormDataService, "validateSubTableAuth", params));
    }

    /**
     * 增加或删除附件，操作的是缓存
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("addOrDelAttachment")
    public Response addOrDelAttachment(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capFormDataService, "addOrDelAttachment", params));
    }

    /**
     * 截图服务
     * @param params
     * @return
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("doScreenCapture")
    public Response doScreenCapture(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capScreenCaptureService, "doScreenCapture", params));
    }

    /**
     * 获取base64码
     * @param imgBase64Key
     * @return
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("getScreenCaptureImgBase64/{imgBase64Key}")
    public Response getScreenCaptureImgBase64(@PathParam("imgBase64Key")String imgBase64Key) {
        return success(capScreenCaptureService.getScreenCaptureImgBase64(imgBase64Key));
    }

    /**
     * 表单截图服务
     * @param params
     * @return
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("doFormScreenCapture")
    public Response doFormScreenCapture(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capScreenCaptureService, "doFormDataContentScreenCapture", params));
    }

    /**
     * 表单截图，转为文件，返回附件url信息
     * @param params
     * @return
     * @deprecated 由于需要合并缓存等原因，方法写入capFormDataService，再内部调用capScreenCaptureService.doFormScreenCapture4File
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("doFormScreenCapture4File")
    @Deprecated
    public Response doFormScreenCapture4File(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capScreenCaptureService, "doFormScreenCapture4File", params));
    }

    /**
     * 表单截图，转为文件，返回附件url信息
     * 有合并缓存的逻辑
     * @param params
     * @return
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("screenCapture")
    public Response screenCapture(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capFormDataService, "screenCapture", params));
    }

    /**
     * M3全文检索,穿透cap4无流程表单获取穿透参数接口
     * @param params  参数
     * <pre>
     *  类型          名称                必填          备注
     *  Long          moduleId            Y            moduleId应用id
     * </pre>
     * @return map
     * @exception BusinessException
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("findQueryParam4Index")
    public Response findSourceInfo4Index(Map<String, Object> params) throws BusinessException {
        Long moduleId = ParamUtil.getLong(params,"moduleId",0L);
        Map<String,Object> resultMap = new HashMap<String, Object>();
        UnflowFormIndex unflowFormIndex = (UnflowFormIndex)AppContext.getBean("unflowFormIndex");
        if(moduleId != null && moduleId != 0L){
            Map<String, Object> indexMap = unflowFormIndex.findSourceInfo(moduleId);
            if(indexMap != null && indexMap.size() != 0){
                ModuleType type = (ModuleType)indexMap.get("moduleType");
                resultMap.put("moduleType",""+type.getKey());
                resultMap.put("rightId",String.valueOf(indexMap.get("rightId")));
                resultMap.put("moduleId",String.valueOf(moduleId));
                resultMap.put("operateType","2");
            }
        }
        if(resultMap.size() == 0){
            throw new BusinessException(ResourceUtil.getString("form.exception.datanotexit"));
        }
        return success(resultMap);
    }

    /**
     * 获取签章数据
     * @param params
     * @return
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("getiSignatureProtectedData")
    public Response getiSignatureProtectedData(Map<String, Object> params) {
        return success(ServiceProxy.invoke(capFormDataSignatureService, "getiSignatureProtectedData", params));
    }


    /**
     * 给表单编辑器提供前端日志保存文件的接口
     * @param params {type:info/debug/warn/error,message:"保存信息"}
     * @return
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("saveLog")
    public Response saveLog(Map<String, Object> params) {
        Map<String,Object> result = new HashMap<String, Object>(1);
        String type = String.valueOf(params.get("type"));
        String message = String.valueOf(params.get("message"));
        if(Strings.isBlank(type)){
            type = "info";
        }
        if("error".equals(type)){
            LOGGER.error(message);
        }else if("debug".equals(type)){
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug(message);
            }
        }else if("warn".equals(type)){
            if(LOGGER.isWarnEnabled()) {
                LOGGER.warn(message);
            }
        }else{
            LOGGER.info(message);
        }
        result.put("success",true);
        return success(result);
    }

    /**
     * 根据表单类型，获取可用的应用绑定按钮类型
     * @param formType
     * @return
     */
    @GET
    @Path("canUseBtns")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.1")
    public Response canUseBtns(@QueryParam("formType") int formType){
        Enums.FormType type = Enums.FormType.getEnumByKey(formType);
        List<Map<String,Object>> canUseBtnInfoMaps = FormFieldUtil.getCanUseBindBtns(type);
        Map<String,Object> result = new HashMap<String, Object>();
        result.put("success",true);
        result.put("canUseBtns",canUseBtnInfoMaps);
        return success(result);
    }

    /**
     * 此接口返回当前环境所有的应用绑定自定义按钮
     * @return
     */
    @GET
    @Path("getCustomBtnList")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.1")
    public Response getCustomBtnList(){
        Map<String,Object> result = new HashMap<String, Object>();
        List<Map<String,Object>> resultJsonObjs = new ArrayList<Map<String, Object>>();
        Map<String,Button> currentEnvBtns = FormFieldUtil.getCustomBtnMap();
        for(Button btn : currentEnvBtns.values()){
            CommonBtn commonBtn = (CommonBtn)btn;
            Map<String,Object> btnInfo = new HashMap<String, Object>();
            btnInfo.put("name",commonBtn.getText());
            if(commonBtn.canUse(Enums.FormType.unFlowForm)){
                btnInfo.put("deployed",1);//已部署
            }else{
                btnInfo.put("deployed",2);//没有部署
            }
            resultJsonObjs.add(btnInfo);
        }
        result.put("result",resultJsonObjs);
        return success(result);
    }

    /**
     * 重新加载所有的国际化资源，此接口为方便开发国际化过程中避免反复重启服务所用，不能用于其他用途！
     * 注意！！！此接口随时会被删掉
     * @return
     */
    @GET
    @Path("reloadI18n")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.1")
    public Response reloadI18n(){
        Map<String,Object> result = new HashMap<String, Object>();
        ResourceLoader.initResources();
        result.put("result","true");
        return success(result);
    }

    /**
     * 返回下级枚举列表
     * @param params
     * <pre>
     *     enumIds: 枚举ID数组[7096181649662363749, 7096181649662363749]
     * </pre>
     * @return
     * <pre>
     *     code : 0
     *     data : {
     *         code : 2000 //成功
     *         data : [{
     *              id : "7096181649662363749" // 下级枚举ID
     *              level : "1"                // 枚举层级
     *              showValue : "1.1.1枚举"    // 显示值
     *              enumValue : "0 "          // 枚举值
     *         }]
     *     }
     * </pre>
     */
    @POST
    @Path("enumItems")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.1")
    public Response getConditionEnumItems(Map<String, Object> params) {
        return success(capConditionService.getEnumItemResult((List<String>) params.get("enumIds")));
    }

    /**
     * 获取枚举树形结构
     *
     * @param itemId
     * @return
     */
    @GET
    @Path("enumItem/{itemId}/tree")
    public Response getEnumTreeResult(@PathParam("itemId") Long itemId) {
        return success(capConditionService.getEnumTreeResult(itemId));
    }

    /**
     * 生成导出表单视图的rest接口
     * @param params {"viewContent":"视图内容json字符串","viewName":"视图名称"}
     * @return 下载视图压缩包的url
     */
    @POST
    @Path("exportView")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.1SP1")
    public Response exportView(Map<String, Object> params) {
        Map<String,Object> resultMap = null;
        try {
            resultMap = cap4FormDesignManager.exportView(params);
            resultMap.put("success",true);
        } catch (BusinessException e) {
            resultMap = new HashMap<String, Object>();
            resultMap.put("success",false);
            resultMap.put("errorMsg",e.getMessage());
        }
        return success(resultMap);
    }


    /**
     * 导入视图
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("importView/{fileId}")
    public Response importView(@PathParam("fileId") String fileId) {
        Map<String,Object> resultMap = null;
        try {
            resultMap = cap4FormDesignManager.importView(fileId);
            resultMap.put("success",true);
        } catch (BusinessException e) {
            resultMap = new HashMap<String, Object>(2);
            resultMap.put("success",false);
            resultMap.put("errorMsg",e.getMessage());
        }
        return success(resultMap);
    }

    /**
     * 从商城获取表单模板或者视图模板，导入本地V5环境
     * @param params {fileType:view/form,fileUrl:url,importType:0/1/2(如果是导入表单模板，需要传这个参数，表示导入之后是什么类型，原类型：0、流程表单：1、无流程表单：2),bizConfigId:应用id（如果是从应用内导入需要传递）}
     * @return
     */
    @POST
    @Path("importViewOrForm4Mall")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.1SP1")
    public Response importViewOrForm4Mall(Map<String, Object> params){
        Map<String,Object> resultMap = null;
        try {
            resultMap = cap4FormDesignManager.importViewOrForm4Mall(params);
            resultMap.put("success",true);
        } catch (BusinessException e) {
            resultMap = new HashMap<String, Object>();
            resultMap.put("success",false);
            resultMap.put("errorMsg",e.getMessage());
        }
        return success(resultMap);
    }
}