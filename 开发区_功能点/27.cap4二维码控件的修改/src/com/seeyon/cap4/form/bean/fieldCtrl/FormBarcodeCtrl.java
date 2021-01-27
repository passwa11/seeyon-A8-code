package com.seeyon.cap4.form.bean.fieldCtrl;

import com.seeyon.cap4.form.bean.FormAuthViewFieldBean;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormDataSubBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.FormFieldComEnum;
import com.seeyon.cap4.form.bean.ParamDefinition;
import com.seeyon.cap4.form.util.BarcodeConstant;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.cap4.template.util.CAPAttachmentUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.barCode.manager.BarCodeManager;
import com.seeyon.ctp.common.barCode.vo.ResultVO;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.logging.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * cap4二维码自定义控件
 *
 * @author wangh
 * @create 2018-01-29 10:34
 **/
public class FormBarcodeCtrl extends FormFieldCustomCtrl {
    private static final Log LOGGER = CtpLogFactory.getLog(FormBarcodeCtrl.class);

    @Override
    public boolean authNotNullAndValIsNull(FormDataMasterBean formDataMasterBean, FormFieldBean field, FormAuthViewFieldBean authViewFieldBean, Object val) {
        //二维码发起就会自动生成，不可能为空
        return false;
    }

    @Override
    public void init() {
        this.setPluginId("capctrlbarcode");
        this.setIcon("cap-icon-erweima");
        ParamDefinition param1 = new ParamDefinition();
        param1.setDialogUrl("cap4/barcode.do?method=barcodeSet"+ Functions.csrfSuffix());
        param1.setDisplay("cap.ctrl.barcode.set.title");
        param1.setName("barcodeInfo");
        param1.setParamType(Enums.ParamType.button);
        param1.setDialogWidth("400");
        param1.setDialogHeight("250");
        params.add(param1);
    }

    @Override
    public boolean canUse() {
        boolean res = super.canUse();
        return res;
    }

    @Override
    public String getPCInjectionInfo() {
        return "{path:'apps_res/cap/customCtrlResources/capctrlbarcode/',jsUri:'js/barcodeRunning.js',initMethod:'init',nameSpace:'field_" + this.getKey() + "'}";
    }

    @Override
    public String getMBInjectionInfo(){
        return "{path:'http://capqrcode.v5.cmp/v1.0.0/',weixinpath:'capqrcode',jsUri:'js/barcodeRunning.js',initMethod:'init',nameSpace:'field_" + this.getKey() + "'}";
    }

    @Override
    public String getFieldLength() {
        return "20";
    }

    /**
     * 定义控件名称，控件名称定好之后不允许变化，最好是一个uuid string全球唯一
     * 获取一个uuid string可以使用工具类www.seeyon.com.utils.UUIDUtil.getUUIDString();
     * 举例：比如通过UUIDUtil.getUUIDString()获取到一个id：8710168352240535179，可以在此接口直接返回这个id值return "8710168352240535179"
     * @return
     */
    @Override
    public String getKey() {
        return "6678555354073746763";
    }

    @Override
    public String getText() {
        return ResourceUtil.getString("cap.ctrl.barcode.text");
    }

    @Override
    public boolean canBathUpdate() {
        return false;
    }

    @Override
    public boolean isAttachment() {
        return true;
    }

    @Override
    public List<String[]> getListShowDefaultVal(Integer integer) {
        return null;
    }

    @Override
    public String[] getDefaultVal(String s) {
        return new String[0];
    }

    @Override
    public void handleSaving(Map<String, Object> params) throws BusinessException {
        params.put("mergeCache",true);
        rebuildBarcode(params);

    }

    /**
     * 后台刷新二维码控件内容，供批量刷新、批量修改、触发关系使用
     * @param params
     */
    @Override
    public void refresh(Map<String, Object> params) throws BusinessException {
        rebuildBarcode(params);
    }

    /**
     * 是否支持套红，自定义控件默认false，如果支持需要重新接口返回true
     *
     * @return
     */
    public boolean canInjectionWord() {
        return true;
    }

    /**
     * 翻译控件值提供给标签打印使用
     * @param fieldBean
     * @param masterData
     * @return
     * @throws BusinessException
     */
    @Override
    public Object getLabelPrintVal(FormFieldBean fieldBean,FormDataMasterBean masterData) throws BusinessException {
        String printVal = "";
        String subreferenceIdStr = String.valueOf(masterData.getFieldValue(fieldBean.getName()));
        if(!StringUtil.checkNull(subreferenceIdStr)){
            Long masterDataId = masterData.getId();
            MainbodyManager mainbodyManager = (MainbodyManager)AppContext.getBean("ctpMainbodyManager");
            Map<String,Object> param = new HashMap<String, Object>();
            param.put("contentDataId",masterDataId);
            List<CtpContentAll> contentAlls = mainbodyManager.getContentList(param);
            Long moduleId = 0L;
            if(contentAlls.size()<=0){
                return printVal;
            }else if(contentAlls.size()==1){
                moduleId = contentAlls.get(0).getModuleId();
            }else{
                LOGGER.info("标签打印通过数据id"+masterDataId + " 从正文表查询到"+contentAlls.size()+"条数据，使用第一条。");
                moduleId = contentAlls.get(0).getModuleId();
            }
            //根据数据id获取正文，从而获取moduleId
            Long suberenceId = Long.parseLong(subreferenceIdStr);
            AttachmentManager attachmentManager = (AttachmentManager)AppContext.getBean("attachmentManager");
            List<Attachment> attachments = attachmentManager.getByReference(moduleId,suberenceId);
            if(attachments.size()>0) {
                Attachment attachment = attachments.get(0);
                String createDataStr = DateUtil.format(attachment.getCreatedate(),DateUtil.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_PATTERN);
                printVal = "fileUpload.do?method=showRTE&type=image&fileId="+attachment.getFileUrl()+"&createDate="+createDataStr+"&showType=big";
            }
        }
        return printVal;
    }

    /**
     * 套红
     * @param masterBean
     * @param fieldBean
     * @return
     * @throws BusinessException
     */
    public String getTaoHongValue(FormDataMasterBean masterBean, FormFieldBean fieldBean,Map<String,Object> special) throws BusinessException {
        String fieldName = fieldBean.getName();
        Object value = masterBean.getFieldValue(fieldName);
        String result = "";
        if(null != value){
            List<Attachment> sessionAttachments = masterBean.getSessionAttachments(String.valueOf(value));
            if (null != sessionAttachments && sessionAttachments.size() > 0){
                Attachment attachment = sessionAttachments.get(0);
                special.put("name", attachment.getFilename() );
                result = attachment.getFileUrl() + "_" + DateUtil.format(attachment.getCreatedate(), "yyyy-MM-dd");
            }
        }
        //二维码在套红到word中的时候类似于图片，所以借用图片逻辑处理二维码控件的套红
        special.put("inputType", FormFieldComEnum.EXTEND_IMAGE.getKey());
        return result;
    }

    @SuppressWarnings("unchecked")
    private void rebuildBarcode(Map<String, Object> params) throws BusinessException {
        //因为自定义控件保存在表单数据保存之前，所以附件需要放到缓存中，通过mergeCache来控制
        Object mergeCacheObj = params.get("mergeCache");
        boolean mergeCache = false;
        if(null != mergeCacheObj){
            mergeCache = (Boolean)mergeCacheObj;
        }
        FormDataMasterBean formDataMasterBean = (FormDataMasterBean) params.get("formDataMasterBean");
        FormFieldBean formFieldBean = (FormFieldBean) params.get("formFieldBean");
        FormBean formBean = (FormBean) params.get("formBean");
        Long moduleId = ParamUtil.getLong(params, "moduleId");//正文moduleId

        String customParams = formFieldBean.getCustomParam();
        Map<String, Object> definition = null;
        if (Strings.isNotBlank(customParams)) {
            definition = (Map<String, Object>) JSONUtil.parseJSONString(customParams);
        }
        if (null == definition || definition.size()<=0) {
            throw new BusinessException("当前无法生成二维码，请联系管理员设置该二维码的内容组成项！");
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
            throw new BusinessException("UnKnow Barcode Type：" + type);
        }
        int size = Integer.parseInt(sizeOption) * 50;
        String encrypt = "";
        if(null != barcodeContentInfo) {
            encrypt = String.valueOf(barcodeContentInfo.get("encrypt"));
        }
        //加密级别
        String encryptLevel = Strings.isBlank(encrypt)?"0":encrypt;
        String encodeLevel = "0".equals(encryptLevel)?"no":"normal";
        //zhou----------------------------------------------------------
        String tableName=formDataMasterBean.getFormTable().getTableName();
        if("formmain_0540".equals(tableName)){
            barcodeParam.put("bj",true);//宽度
        }
        //zhou----------------------------------------------------------

        //每次生成使用不同的uuid，这样避免待办打开的情况下重新生成了二维码，但是没有提交这种情况无法还原之前的二维码
        Long subreference = UUIDLong.longUUID();

        barcodeParam.put("width",size);//宽度
        barcodeParam.put("height",size);//高度
        barcodeParam.put("codeType", codeType);//通过当前二维码类型来传递，如果是文本类传递cap4form，如果是二维码，传递cap4url
        barcodeParam.put("subReference",subreference);//控件值，前端传递过来
        barcodeParam.put("encodeLevel",encodeLevel);//加密级别，从定义中获取
        barcodeParam.put("reference",moduleId);
        barcodeParam.put("maxLength",500);
        barcodeParam.put("category", ApplicationCategoryEnum.cap4Form.getKey());
        barcodeParam.put("throwException", false);//刷新二维码的时候，如果超过长度了，不抛出异常，重新生成一个带提示信息的二维码，扫描该二维码的时候给出提示

        Map<String,Object> customMap = new HashMap<String, Object>();
        customMap.put(BarcodeConstant.BARCODE_PARAM_FORM_ID,String.valueOf(formBean.getId()));
        customMap.put(BarcodeConstant.BARCODE_PARAM_FIELD_NAME,formFieldBean.getName());
        customMap.put(BarcodeConstant.BARCODE_PARAM_DATA_ID,formDataMasterBean.getId());
        customMap.put(BarcodeConstant.BARCODE_PARAM_MODULE_ID,moduleId);
        customMap.put(BarcodeConstant.BARCODE_PARAM_FORM_TYPE,formBean.getFormType());


        AttachmentManager attachmentManager = (AttachmentManager)AppContext.getBean("attachmentManager");
        BarCodeManager barCodeManager = (BarCodeManager)AppContext.getBean("barCodeManager");

        if(formFieldBean.isMasterField()){
            customMap.put(BarcodeConstant.BARCODE_PARAM_SUB_DATA_ID,"0");
            String oldStr = String.valueOf(formDataMasterBean.getFieldValue(formFieldBean.getName()));
            //后台直接都生成二维码，前台不点生成后台也一并生成了
            if (Strings.isNotBlank(oldStr) && !"null".equals(oldStr)) {//如果之前有值，则先删除已有的附件信息
                Long oldSubReference = Long.valueOf(oldStr);
                attachmentManager.removeByReference(moduleId, oldSubReference);
                formDataMasterBean.putSessionAttachments(oldStr, null);
            }
            ResultVO resultVO = barCodeManager.getBarCodeAttachment(barcodeParam,customMap);
            if (!resultVO.isSuccess()) {
                throw new BusinessException(resultVO.getMsg());
            }
            if(mergeCache && null != resultVO.getAttachment()){
                List<Attachment> attachments = new ArrayList<Attachment>();
                attachments.add(resultVO.getAttachment());
                formDataMasterBean.putSessionAttachments(String.valueOf(subreference), attachments);
            }
            formDataMasterBean.addFieldValue(formFieldBean.getName(),subreference);

        }else{
            List<FormDataSubBean> subDatas = formDataMasterBean.getSubData(formFieldBean.getOwnerTableName());
            for (FormDataSubBean subBean:subDatas) {
                customMap.put(BarcodeConstant.BARCODE_PARAM_SUB_DATA_ID,String.valueOf(subBean.getId()));
                String oldStr = String.valueOf(subBean.getFieldValue(formFieldBean.getName()));
                if (Strings.isNotBlank(oldStr) && !"null".equals(oldStr)) {
                    Long oldSubReference = Long.valueOf(oldStr);
                    attachmentManager.removeByReference(moduleId, oldSubReference);
                    formDataMasterBean.putSessionAttachments(oldStr, null);
                }
                subBean.addFieldValue(formFieldBean.getName(),subreference);
                ResultVO resultVO = barCodeManager.getBarCodeAttachment(barcodeParam,customMap);
                if (!resultVO.isSuccess()) {
                    throw new BusinessException(resultVO.getMsg());
                }
                if(mergeCache && null != resultVO.getAttachment()){
                    List<Attachment> attachments = new ArrayList<Attachment>();
                    attachments.add(resultVO.getAttachment());
                    formDataMasterBean.putSessionAttachments(String.valueOf(subreference), attachments);
                }
                //重复表有多行，重新生成一个subReference Id
                subreference = UUIDLong.longUUID();
                barcodeParam.put("subReference", subreference);//存入附件字段的id
            }


        }
    }
}
