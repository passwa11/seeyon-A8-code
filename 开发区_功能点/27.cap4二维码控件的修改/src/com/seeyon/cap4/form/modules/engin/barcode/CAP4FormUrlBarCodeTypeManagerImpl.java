package com.seeyon.cap4.form.modules.engin.barcode;

import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormBindAuthBean;
import com.seeyon.cap4.form.bean.FormBindBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormFormulaBean;
import com.seeyon.cap4.form.bean.SimpleObjectBean;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.cap4.form.util.BarcodeConstant;
import com.seeyon.cap4.form.util.FormConstant;
import com.seeyon.cap4.form.util.FormUtil;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.barCode.manager.BarCodeTypeManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.publicqrcode.dao.PublicQrCodeDao;
import com.seeyon.ctp.common.publicqrcode.po.PublicQrCodePO;
import com.seeyon.ctp.form.modules.engin.formula.FormulaEnums;
import com.seeyon.ctp.form.modules.engin.formula.FormulaUtil;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.logging.Log;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表单无流程列表解析二维码实现
 * Created by wangh on 2018-1-30
 */
public class CAP4FormUrlBarCodeTypeManagerImpl implements BarCodeTypeManager {
    private static Log log = CtpLogFactory.getLog(CAP4FormUrlBarCodeTypeManagerImpl.class);
    private CAP4FormManager cap4FormManager;
    private PublicQrCodeDao publicQrCodeDao;

    public PublicQrCodeDao getPublicQrCodeDao() {
        return publicQrCodeDao;
    }

    public void setPublicQrCodeDao(PublicQrCodeDao publicQrCodeDao) {
        this.publicQrCodeDao = publicQrCodeDao;
    }

    @Override
    public String getType() {
        return "cap4url";
    }

    @Override
    public String getContentStr(Map<String, Object> param) {
        Map<String,Object> contentJson = new HashMap<String, Object>(16);
        Long formId = ParamUtil.getLong(param, BarcodeConstant.BARCODE_PARAM_FORM_ID, 0L);
        Long dataId = ParamUtil.getLong(param, BarcodeConstant.BARCODE_PARAM_DATA_ID, 0L);
        Long moduleId = ParamUtil.getLong(param, BarcodeConstant.BARCODE_PARAM_MODULE_ID, 0L);
        int formType = ParamUtil.getInt(param, BarcodeConstant.BARCODE_PARAM_FORM_TYPE, 1);
        contentJson.put(BarcodeConstant.BARCODE_PARAM_FORM_ID, formId+"");
        contentJson.put(BarcodeConstant.BARCODE_PARAM_DATA_ID, dataId+"");
        contentJson.put(BarcodeConstant.BARCODE_PARAM_MODULE_ID, moduleId+"");
        contentJson.put(BarcodeConstant.BARCODE_PARAM_FORM_TYPE, formType);
        contentJson.put(BarcodeConstant.BARCODE_PARAM_VERSION,FormConstant.cap4version);
        Date date = new Date();
        PublicQrCodePO publicQrCodePO = new PublicQrCodePO();
        Long id = UUIDLong.longUUID();
        publicQrCodePO.setId(id);
        publicQrCodePO.setCategory(ApplicationCategoryEnum.cap4Form.name());
        publicQrCodePO.setObjectId(dataId);
        publicQrCodePO.setLinkParams(JSONUtil.toJSONString(contentJson));
        publicQrCodePO.setState(0);
        publicQrCodePO.setCreateDate(date);
        publicQrCodePO.setUpdateDate(date);
        publicQrCodePO.setAccountId(AppContext.currentAccountId());
        try {
            publicQrCodeDao.createPublicQrCode(publicQrCodePO);
        } catch (BusinessException e) {
            log.error(e.getMessage(),e);
        }
        return String.valueOf(id);
    }

    /**
     * 当生成的二维码内容超过长度之后，生成自己想要的特殊二维码
     *
     * @param param
     * @return
     */
    @Override
    public String getContent4OutOfLength(Map<String, Object> param) {
        return "";
    }

    @Override
    public Object decode(String decodeStr, Map<String, Object> param) throws SQLException, BusinessException {
        Map<String,Object> result = new HashMap<String, Object>(16);
        Object json = null;
        if(decodeStr.contains("{")&&decodeStr.contains("}")) {//兼容老的二维码
            try {
                json = JSONUtil.parseJSONString(decodeStr);
            } catch (Exception e) {
                log.error("无流程解析url二维码数据异常：" + decodeStr, e);
            }
        }else{
            PublicQrCodePO qrCodePO = publicQrCodeDao.getPublicQrCode(Long.parseLong(decodeStr));
            try {
                json = JSONUtil.parseJSONString(qrCodePO.getLinkParams());
            } catch (Exception e) {
                log.error("无流程解析url二维码数据异常：" + decodeStr, e);
            }
        }
        if (json == null) {
            result.put(FormConstant.SUCCESS,false);
            result.put(FormConstant.MSG,ResourceUtil.getString("collaboration.erweima.nodata.js"));
            return result;
        }
        if (json instanceof Map) {
            Map<String, String> map = (Map<String, String>) json;

            Long formId = ParamUtil.getLong(map, BarcodeConstant.BARCODE_PARAM_FORM_ID);
            Long currentFormId = ParamUtil.getLong(param, BarcodeConstant.BARCODE_PARAM_FORM_ID);
            //校验表单id是否与当前表单相等
            if (!currentFormId.equals(formId)) {
                result.put(FormConstant.SUCCESS,false);
                result.put(FormConstant.MSG,ResourceUtil.getString("cap.ctrl.barcode.running.noform"));
                return result;
            }
            result.put(BarcodeConstant.BARCODE_PARAM_FORM_ID,formId+"");
            FormBean formBean = cap4FormManager.getForm(formId,false);
            //校验表单是否可用
            if (!cap4FormManager.isEnabled(formBean)) {
                result.put(FormConstant.SUCCESS,false);
                result.put(FormConstant.MSG,ResourceUtil.getString("cap.ctrl.barcode.running.connect"));
                return result;
            }
            //校验数据是否存在
            Long moduleId = ParamUtil.getLong(map, BarcodeConstant.BARCODE_PARAM_MODULE_ID);
            Long dataId = ParamUtil.getLong(map, BarcodeConstant.BARCODE_PARAM_DATA_ID);
            FormDataMasterBean masterBean = cap4FormManager.getDataMasterBeanById(dataId, formBean, null);
            if(dataId == null || moduleId == null || masterBean == null){
                result.put(FormConstant.SUCCESS,false);
                result.put(FormConstant.MSG,ResourceUtil.getString("collaboration.erweima.nodata.js"));
                return result;
            }

            //判断操作范围
            Long templateId = ParamUtil.getLong(param, BarcodeConstant.BARCODE_PARAM_TEMPLATE_ID);
            if(templateId != null && templateId != 0L && templateId != -1L){
                FormBindBean bindBean = formBean.getBind();
                FormBindAuthBean formBindAuthBean = bindBean.getFormBindAuthBean(String.valueOf(templateId));
                if(formBindAuthBean != null){
                    FormFormulaBean formulaBean = formBindAuthBean.getFormFormulaBean();
                    if(formulaBean != null){
                        Map<String,Object> formDataMap = masterBean.getFormulaMap(FormulaEnums.componentType_condition);
                        String formulaVar =formulaBean.getExecuteFormulaForGroove();
                        boolean flag = FormulaUtil.isMatch(formulaVar,formDataMap);
                        if(!flag){
                            result.put(FormConstant.SUCCESS,false);
                            result.put(FormConstant.MSG,ResourceUtil.getString("form.unFlow.data.view.forbidden.tips.label"));
                            return result;
                        }
                    }
                }
            }

            //有修改权限时，要校验数据是否锁定
            FormBindAuthBean bindAuthBean = formBean.getBind().getFormBindAuthBean(String.valueOf(templateId));
            List<SimpleObjectBean> editAuth = bindAuthBean.getUpdateAuthList();
            if (editAuth != null && editAuth.size() > 0){
                CAPFormManager capFormManager = (CAPFormManager) AppContext.getBean("capFormManager");
                for(SimpleObjectBean auth:editAuth) {
                    String pcRightId = auth.getValue();
                    if (StringUtil.checkNull(pcRightId)) {
                        pcRightId = "";
                    }
                    String mbRightId = auth.getPhoneValue();
                    if (StringUtil.checkNull(mbRightId)) {
                        mbRightId = "";
                    }
                    String[] pcRights = FormUtil.paraseOperationIds(pcRightId);
                    String[] mbRights = FormUtil.paraseOperationIds(mbRightId);
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
                }
                if(cap4FormManager.checkLock(formId,dataId+"")){
                    result.put(FormConstant.SUCCESS,false);
                    result.put(FormConstant.MSG,ResourceUtil.getString("cap.ctrl.barcode.running.lock"));
                    return result;
                }
                String checkResult = cap4FormManager.checkDataLockForEdit(dataId + "");
                if (!checkResult.isEmpty()){
                    result.put(FormConstant.SUCCESS,false);
                    result.put(FormConstant.MSG,checkResult);
                    return result;
                }
            }

            result.put(BarcodeConstant.BARCODE_PARAM_DATA_ID,dataId+"");
            result.put(BarcodeConstant.BARCODE_PARAM_MODULE_ID,moduleId+"");

        }
        return result;
    }

    public void setCap4FormManager(CAP4FormManager cap4FormManager) {
        this.cap4FormManager = cap4FormManager;
    }
}
