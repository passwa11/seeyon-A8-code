package com.seeyon.ctp.rest.resources;

import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormBindAuthBean;
import com.seeyon.cap4.form.bean.FormBindBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormDataSubBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.SimpleObjectBean;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.cap4.form.util.BarcodeConstant;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.cap4.form.util.FormConstant;
import com.seeyon.cap4.form.util.FormUtil;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.barCode.manager.BarCodeManager;
import com.seeyon.ctp.common.barCode.vo.ResultVO;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.publicqrcode.manager.PublicQrCodeManager;
import com.seeyon.ctp.common.publicqrcode.po.PublicQrCodePO;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.logging.Log;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 二维码rest接口
 *
 * @author wangh
 * @create 2018-02-06 11:06
 **/
@Path("capBarcode")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces(MediaType.APPLICATION_JSON)
public class CAPFormBarcodeResource extends BaseResource{

    private static final Log LOGGER = CtpLogFactory.getLog(CAPFormBarcodeResource.class);
    /**
     * 无流程表单，通过表单id查询有权限的应用绑定列表
     * @param formId 表单id
     * <pre>
     *     类型                       名称               必填          备注
     *     String                     formId             Y          表单id
     * </pre>
     * @return
     * @throws BusinessException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("showTemplatesByFormId/{formId}")
    public Response showTemplatesByFormId(@PathParam("formId") Long formId) throws BusinessException{
        CAP4FormManager cap4FormManager = (CAP4FormManager)AppContext.getBean("cap4FormManager");
        FormBean formBean = cap4FormManager.getForm(formId,false);
        Map<String,Object> result = new HashMap<String, Object>();
        List<Map<String,Object>> templates = new ArrayList<Map<String, Object>>();
        result.put("result",templates);
        if(null != formBean && Enums.FormType.unFlowForm.getKey() == formBean.getFormType()){
            FormBindBean formBindBean = formBean.getBind();
            if(null != formBindBean){
                List<FormBindAuthBean> unflowBinds = formBindBean.getUnflowFormBindAuthByUserId(AppContext.currentUserId());
                if(unflowBinds.size() > 0){
                    for(FormBindAuthBean formBindAuthBean:unflowBinds){
                        Map<String,Object> templateInfoMap = new HashMap<String, Object>();
                        templateInfoMap.put("templateName",formBindAuthBean.getName());
                        templateInfoMap.put("templateId",String.valueOf(formBindAuthBean.getId()));
                        List<Map<String,Object>> auths = new ArrayList<Map<String,Object>>();
                        //新建和浏览都只有一个，修改权限可能有多个
                        SimpleObjectBean browseAuthObj = formBindAuthBean.getAuthObjByName(FormBindAuthBean.AuthName.BROWSE.getKey());
                        List<SimpleObjectBean> updateAuths = formBindAuthBean.getUpdateAuthList();
                        if(null != updateAuths && updateAuths.size()>0){
                            for(SimpleObjectBean sb:updateAuths) {
                                if(!StringUtil.checkNull(sb.getPhoneValue())||!StringUtil.checkNull(sb.getValue())) {
                                    auths.add(convertAuthObjToMap(formBean,sb));
                                }
                            }
                        }
                        if(null != browseAuthObj){
                            auths.add(convertAuthObjToMap(formBean,browseAuthObj));
                        }
                        templateInfoMap.put("authList",auths);
                        templates.add(templateInfoMap);
                    }
                }
            }
        }
        return success(result);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("barcodeJsonInfo/{id}")
    public Response templateRightInfo(@PathParam("id") Long id) throws BusinessException{
        PublicQrCodeManager publicQrCodeManager = (PublicQrCodeManager)AppContext.getBean("publicQrCodeManager");
        PublicQrCodePO qrCodePO = publicQrCodeManager.getPublicQrCode(id);
        Map<String,Object> result = new HashMap<String, Object>();
        result.put("content",qrCodePO.getLinkParams());
        return success(result);
    }

    /**
     * 通过无流程表单的模板，获取模板所配置的所有修改、显示权限
     * @param formId    表单id
     * @param templateId    无流程应用绑定id
     * @return
     * @throws BusinessException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("templateRightInfo/{formId}/{templateId}")
    public Response templateRightInfo(@PathParam("formId") Long formId,@PathParam("templateId") Long templateId) throws BusinessException{
        CAP4FormManager cap4FormManager = (CAP4FormManager)AppContext.getBean("cap4FormManager");
        FormBean formBean = cap4FormManager.getForm(formId,false);
        if(null == formBean){
            throw new BusinessException("通过表单id：" + formId + " 找不到表单定义");
        }
        if(formBean.isFlowForm()){
            throw new BusinessException("表单" + formId + " 不是无流程表单");
        }
        FormBindBean formBindBean = formBean.getBind();
        FormBindAuthBean formBindAuthBean = formBindBean.getUnFlowTemplateById(templateId);
        if(!formBindAuthBean.checkRight(AppContext.currentUserId())){
            throw new BusinessException("当前登录人员" + AppContext.currentUserId() + " 没有模板的使用权限，表单id:"+formId + ",模板id：" + templateId);
        }
        List<Map<String,Object>> auths = new ArrayList<Map<String,Object>>();
        //新建和浏览都只有一个，修改权限可能有多个
        SimpleObjectBean browseAuthObj = formBindAuthBean.getAuthObjByName(FormBindAuthBean.AuthName.BROWSE.getKey());
        List<SimpleObjectBean> updateAuths = formBindAuthBean.getUpdateAuthList();
        if(null != updateAuths && updateAuths.size()>0){
            for(SimpleObjectBean sb:updateAuths) {
                if(!StringUtil.checkNull(sb.getPhoneValue())||!StringUtil.checkNull(sb.getValue())) {
                    auths.add(convertAuthObjToMap(formBean,sb));
                }
            }
        }
        if(null != browseAuthObj){
            auths.add(convertAuthObjToMap(formBean,browseAuthObj));
        }
        Map<String,Object> templateInfoMap = new HashMap<String, Object>();
        templateInfoMap.put("authList",auths);
        return success(templateInfoMap);
    }

    /**
     * 将SimpleObjectBean转换为需要输出到前端的Map
     * @param auth
     * @return
     */
    private Map<String,Object> convertAuthObjToMap(FormBean formBean,SimpleObjectBean auth){
        Map<String,Object> result = new HashMap<String, Object>();
        result.put("type",auth.getName());
        String pcRightId = auth.getValue();
        if(StringUtil.checkNull(pcRightId)){
            pcRightId = "";
        }
        String mbRightId = auth.getPhoneValue();
        if(StringUtil.checkNull(mbRightId)){
            mbRightId = "";
        }
        String display = auth.getDisplay();
        if(FormBindAuthBean.AuthName.BROWSE.getKey().equalsIgnoreCase(auth.getName())){
            display = ResourceUtil.getString("DataDefine.Show");
        }
        String pcRight = Strings.join(Arrays.asList(pcRightId.split("[|]")),"_");
        String mbRight = Strings.join(Arrays.asList(mbRightId.split("[|]")),"_");
        CAPFormManager capFormManager = (CAPFormManager)AppContext.getBean("capFormManager");
        String[] pcRights = FormUtil.paraseOperationIds(pcRight);
        String[] mbRights = FormUtil.paraseOperationIds(mbRight);
        if(null!=pcRights) {
            for (String r : pcRights) {
                capFormManager.addRightId(formBean.getId(), Long.parseLong(r));
            }
        }
        if(null!=mbRights) {
            for (String r : mbRights) {
                capFormManager.addRightId(formBean.getId(), Long.parseLong(r));
            }
        }
        result.put("pcRightId",pcRight);
        result.put("mbRightId",mbRight);
        result.put("display",display);
        return result;
    }

    /**
     * 生成二维码接口
     * @param params
     * <pre>
     *     类型       名称      备注
     *     Long     formId      表单id
     *     Long     dataId      数据id
     *     Long     recordId      明细表字段行id
     *     String   fieldName        字段名称
     *     Long     moduleId    正文moduleId
     * </pre>
     * @return
     * @throws BusinessException
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("generateBarCode")
    public Response generateBarCode(Map<String, Object> params) throws BusinessException{
        Long formId = ParamUtil.getLong(params,"formId");//表单id
        Long dataId = ParamUtil.getLong(params,"dataId");//数据id
        Long recordId = ParamUtil.getLong(params,"recordId");//如果是明细表字段，需要传递当前行的id
        String fieldName = ParamUtil.getString(params,"fieldName");//字段名称
        Long moduleId = ParamUtil.getLong(params,"moduleId");//正文moduleId

        CAP4FormManager cap4FormManager = (CAP4FormManager)AppContext.getBean("cap4FormManager");
        FormBean formBean = cap4FormManager.getForm(formId,false);
        if(null == formBean){
            throw new BusinessException("通过表单id：" + formId + " 找不到表单定义");
        }

        FormFieldBean fieldBean = formBean.getFieldBeanByName(fieldName);
        if(null == fieldBean){
            throw new BusinessException("通过字段名称：" + fieldName + "找不到字段定义");
        }
        String customParams = fieldBean.getCustomParam();
        Map<String, Object> definition = null;
        if (Strings.isNotBlank(customParams)) {
            definition = (Map<String, Object>) JSONUtil.parseJSONString(customParams);
        }
        if(null == definition || definition.size()<=0){
            throw new BusinessException(ResourceUtil.getString("cap.ctrl.barcode.running.notsetmapping"));
        }
        Map<String,Object> barCodeInfo = (Map<String,Object>)definition.get("barcodeInfo");
        String type = (String)barCodeInfo.get("type");
        Map<String,Object> barcodeContentInfo = (Map<String,Object>)barCodeInfo.get("content");
        String codeType = "";
        String sizeOption = "4";//url类型的二维码长度都是200(4 * 50)
        Map<String,Object> barcodeParam = new HashMap<String, Object>();
        //判断二维码是url类型还是文本类型
        if("url".equalsIgnoreCase(type)){
            codeType = "cap4url";
        }else if("text".equalsIgnoreCase(type)){
            codeType = "cap4form";
            sizeOption = String.valueOf(barcodeContentInfo.get("sizeOption"));//文本类型的二维码大小由用户设置
            barcodeParam.put("errorLevel","L");//错误级别
        }else{
            throw new BusinessException("未知的类型：" + type);
        }
        int size = Integer.parseInt(sizeOption) * 50;
        String encrypt = "";
        if(null != barcodeContentInfo) {
            encrypt = String.valueOf(barcodeContentInfo.get("encrypt"));
        }
        //加密级别
        String encryptLevel = Strings.isBlank(encrypt)?"0":encrypt;
        String encodeLevel = "0".equals(encryptLevel)?"no":"normal";
        //每次生成使用不同的uuid，这样避免待办打开的情况下重新生成了二维码，但是没有提交这种情况无法还原之前的二维码
        Long subreference = UUIDLong.longUUID();
        FormDataMasterBean cacheData = cap4FormManager.getSessioMasterDataBean(dataId);
        if(null == cacheData){
            throw new BusinessException("缓存数据不存在，无法生成二维码");
        }
        String oldValue = null;
        if(fieldBean.isMasterField()){
            oldValue = String.valueOf(cacheData.getFieldValue(fieldName));
            cacheData.addFieldValue(fieldName,subreference);
        }else{
            FormDataSubBean subBean = cacheData.getFormDataSubBeanById(fieldBean.getOwnerTableName(),recordId);
            if(null == subBean){
                throw new BusinessException("在" + fieldBean.getOwnerTableName() + "中找不到明细表数据行："+ recordId);
            }
            oldValue = String.valueOf(subBean.getFieldValue(fieldName));
            subBean.addFieldValue(fieldName,subreference);
        }
        barcodeParam.put("width",size);//宽度
        barcodeParam.put("height",size);//高度
        barcodeParam.put("codeType", codeType);//通过当前二维码类型来传递，如果是文本类传递cap4form，如果是二维码，传递cap4url
        barcodeParam.put("subReference",subreference);//控件值，前端传递过来
        barcodeParam.put("encodeLevel",encodeLevel);//加密级别，从定义中获取
        barcodeParam.put("reference",dataId);
        barcodeParam.put("maxLength",500);
        barcodeParam.put("category", ApplicationCategoryEnum.cap4Form.getKey());
        BarCodeManager barCodeManager = (BarCodeManager)AppContext.getBean("barCodeManager");

        Map<String,Object> customMap = new HashMap<String, Object>();
        customMap.put(BarcodeConstant.BARCODE_PARAM_FORM_ID,String.valueOf(formId));
        customMap.put(BarcodeConstant.BARCODE_PARAM_SUB_DATA_ID,String.valueOf(recordId));
        customMap.put(BarcodeConstant.BARCODE_PARAM_FIELD_NAME,fieldName);
        customMap.put(BarcodeConstant.BARCODE_PARAM_DATA_ID,dataId);
        customMap.put(BarcodeConstant.BARCODE_PARAM_MODULE_ID,moduleId);
        customMap.put(BarcodeConstant.BARCODE_PARAM_FORM_TYPE,formBean.getFormType());
        ResultVO resultVO = barCodeManager.getBarCodeAttachment(barcodeParam,customMap);
        Map<String,Object> result = new HashMap<String, Object>();
        result.put("success",resultVO.isSuccess());
        Attachment attachment = resultVO.getAttachment();
        if(resultVO.isSuccess() && null != attachment){
            Map<String,Object> attMap = new HashMap<String, Object>();
            attMap.put("id",String.valueOf(attachment.getId()));
            attMap.put("reference",String.valueOf(attachment.getReference()));
            attMap.put("subReference",String.valueOf(attachment.getSubReference()));
            attMap.put("category",attachment.getCategory());
            attMap.put("type",attachment.getType());
            attMap.put("filename",attachment.getFilename());
            attMap.put("mimeType",attachment.getMimeType());
            attMap.put("createdate", Datetimes.formatNoTimeZone(attachment.getCreatedate(),Datetimes.dateStyle));
            attMap.put("size",attachment.getSize());
            attMap.put("description",attachment.getDescription());
            attMap.put("fileUrl",String.valueOf(attachment.getFileUrl()));
            attMap.put("extension",attachment.getExtension());
            attMap.put("icon",attachment.getIcon());
            attMap.put("sort",attachment.getSort());
            attMap.put("officeTransformEnable",attachment.getOfficeTransformEnable());
            attMap.put("v",attachment.getV());
            result.put("attachment",attMap);
            // 新建状态下生成的二维码直接入库，未放入缓存，此时打印获取的附件有问题。
            // BUG_普通_V5_V7.0SP3_新客户质保服务_天地科技股份有限公司_cap4表单中二维码打印预览不显示（更换电脑以及使用谷歌浏览器均不显示）_20190109075162_2019-01-09
            List<Attachment> attachments = new ArrayList<Attachment>();
            attachments.add(attachment);
            cacheData.putSessionAttachments(String.valueOf(attachment.getSubReference()), attachments);
            if (Strings.isNotBlank(oldValue) && !"null".equals(oldValue)) {
                cacheData.putSessionAttachments(oldValue, null);
            }
        }else{//生成失败
            result.put("msg",Strings.isBlank(resultVO.getMsg()) ? ResourceUtil.getString("cap.ctrl.barcode.running.fail") : resultVO.getMsg());
        }
        return success(result);
    }

    /**
     * 解析读取到的二维码内容
     * @param params 参数
     * <pre>
     *  类型          名称                必填          备注
     *  Long          formId              Y            表单ID
     *  Long          dataId              Y            当前数据Id
     *  Long          moduleId            Y            业务Id
     *  Long          rightId             Y            权限id
     *  Long          recordId            Y            重复行id
     *  String        tableName           Y            重复表表名
     *  String        content             Y            扫描到的内容
     * </pre>
     * @return json string
     * <pre>
     *       - {String} success 是否成功 true-成功 false-失败
     *       - {String} msg 失败时的异常信息
     *       - {String} subReference 二维码生成后的id
     * </pre>
     * @exception BusinessException
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("decodeBarCode")
    public Response decodeBarCode(Map<String,Object> params) throws BusinessException{
        Long formId = ParamUtil.getLong(params,"formId");
        CAP4FormManager cap4FormManager = (CAP4FormManager)AppContext.getBean("cap4FormManager");
        FormBean formBean = cap4FormManager.getForm(formId,false);
        String content = ParamUtil.getString(params,"content","");
        boolean success = false;
        Map<String,Object> resultMap = new HashMap<String, Object>();
        String msg = "";
        Object result = null;
        if(Strings.isNotBlank(content)){
            try {
                BarCodeManager barCodeManager = (BarCodeManager)AppContext.getBean("barCodeManager");
                result = barCodeManager.decodeBarCode("cap4form",content,params);
                if(result instanceof ResultVO){
                    ResultVO vo = (ResultVO)result;
                    success = vo.isSuccess();
                    if(!success) {
                        msg = vo.getMsg();
                    }
                }else {
                    success = true;
                }
            }catch (Exception e) {
                LOGGER.error("解析二维码内容异常。");
                LOGGER.error(e.getMessage(),e);
            }
        }else{
            msg = ResourceUtil.getString("cap.ctrl.barcode.running.nodata");
        }
        resultMap.put(FormConstant.SUCCESS, success);
        resultMap.put("msg",msg);
        Map<String,Object> tableDataMap = new HashMap<String, Object>();
        tableDataMap.put("tableData",result);
        resultMap.put("data", tableDataMap);
        return success(resultMap);
    }
}
