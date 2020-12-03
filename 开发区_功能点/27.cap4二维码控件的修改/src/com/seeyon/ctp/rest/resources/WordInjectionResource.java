package com.seeyon.ctp.rest.resources;

import com.seeyon.cap4.form.bean.FormAuthViewBean;
import com.seeyon.cap4.form.bean.FormAuthViewFieldBean;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.fieldCtrl.FormFieldCtrl;
import com.seeyon.cap4.form.modules.engin.base.formData.CAP4FormDataManager;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.form.util.FormUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.logging.Log;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by weijh on 2018-6-7.
 * 自定义控件--表单转文档
 */
@Path("cap4/wordInjectionCtrl")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WordInjectionResource extends BaseResource {
    private static final Log LOG = CtpLogFactory.getLog(WordInjectionResource.class);

    /**
     * 获取当前表单能支持套红的主表字段列表
     *
     * @param formId           表单id
     * @param currentFieldName 当前字段名称
     * @return
     */
    @GET
    @Path("injectionFields")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.0")
    public Response getInjectionFields(@QueryParam("formId") String formId, @QueryParam("currentFieldName") String currentFieldName) {
        CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
        FormBean formBean = cap4FormManager.getEditingForm(Long.valueOf(formId));
        //只支持主表字段参与套红
        List<FormFieldBean> masterFields = formBean.getMasterTableBean().getFields();

        List<Map<String, Object>> resList = new ArrayList<Map<String, Object>>();
        for (FormFieldBean field : masterFields) {
            //排出当前字段名称
            if (field.getName().equalsIgnoreCase(currentFieldName)) {
                continue;
            }
            FormFieldCtrl ctrl = field.getFieldCtrl();
            //判断控件是否支持套红
            if (null != ctrl && ctrl.canInjectionWord()) {
                resList.add(field.getJsonObj4Design(false));
            }
        }
        return success(resList);
    }

    /**
     * 通过word文件相关id获取word文件中的标签
     *
     * @param fileUrl word模板物理文件ID
     * @return
     */
    @GET
    @Path("wordMarks")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.0")
    public Response getWordMarks(@QueryParam("fileUrl") String fileUrl) throws BusinessException {
        Long fileUrlLong = Long.parseLong(fileUrl);
        FileManager filemanager = (FileManager) AppContext.getBean("fileManager");
        V3XFile v3XFile = filemanager.getV3XFile(fileUrlLong);
        File wordFile = filemanager.getFile(fileUrlLong);
        String fName = v3XFile.getFilename();
        String fileType = fName.substring(fName.lastIndexOf(".") + 1);
        List<String> marks = null;
        try {
            marks = FormUtil.readBookmarks(wordFile, fileType);
        } catch (BusinessException e) {
            LOG.error(e);
            throw e;
        }
        return success(marks);
    }

    /**
     * 通过模板文件fileUrl值拷贝word文件，并返回拷贝之后的Attachment的json值
     *
     * @param templateId   模板文件fileUrl值
     * @param masterDataId 主数据id
     * @return 拷贝过后的当前数据的套红附件json
     * @throws BusinessException
     */
    @GET
    @Path("copyWord")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.0")
    public Response copyWord(@QueryParam("templateId") String templateId, @QueryParam("masterDataId") String masterDataId) throws BusinessException {
        Long fileUrlLong = Long.parseLong(templateId);
        Long masterDataIdLong = Long.parseLong(masterDataId);
        AttachmentManager attachmentManager = (AttachmentManager) AppContext.getBean("attachmentManager");
        Attachment templateAtt = attachmentManager.getAttachmentByFileURL(fileUrlLong);
        if (null == templateAtt) {
            throw new BusinessException("找不到Word模板，请检查表单设置是否上传Word模板");
        }
        Long newSubReferenceId = UUIDLong.longUUID();
        attachmentManager.copy(templateAtt.getReference(), templateAtt.getSubReference(), masterDataIdLong, newSubReferenceId, ApplicationCategoryEnum.cap4Form.getKey(), AppContext.currentUserId(), AppContext.currentAccountId(), templateAtt.getFilename());
        List<Attachment> newAtts = attachmentManager.getByReference(masterDataIdLong, newSubReferenceId);
        if (null == newAtts || newAtts.size() <= 0) {
            throw new BusinessException("拷贝Word模板文件失败，拷贝之后查询不到新文件信息。");
        }
        Attachment newAtt = newAtts.get(0);
        Map<String, Object> retObj = new HashMap<String, Object>();
        retObj.put("attJson", getAttJson(newAtt));
        return success(retObj);
    }

    /**
     * word模板上传之后的回调函数，需要更新附件对象的reference和sub_reference
     *
     * @param fileUrl word模板物理文件ID
     * @param formId  当前表单ID
     * @return work模板附件json
     * @throws BusinessException
     */
    @GET
    @Path("uploadAtt")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.0")
    public Response uploadAtt(@QueryParam("fileUrl") String fileUrl, @QueryParam("formId") String formId) throws BusinessException {
        Long fileUrlLong = Long.parseLong(fileUrl);
        Long formIdLong = Long.parseLong(formId);
        AttachmentManager attachmentManager = (AttachmentManager) AppContext.getBean("attachmentManager");
        Attachment templateAtt = attachmentManager.getAttachmentByFileURL(fileUrlLong);
        Long newSubReferenceId = UUIDLong.longUUID();
        templateAtt.setSubReference(newSubReferenceId);
        templateAtt.setReference(formIdLong);
        attachmentManager.update(templateAtt);
        Map<String, Object> retObj = new HashMap<String, Object>();
        retObj.put("attJson", getAttJson(templateAtt));
        return success(retObj);
    }

    /**
     * 获取套红数据
     * @return
     */
    @GET
    @Path("getInjectionData")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.1")
    public Response getInjectionData(@QueryParam("formId") String formId,@QueryParam("dataId") String dataId,@QueryParam("rightId") String rightId,@QueryParam("fieldName") String fieldName) throws BusinessException {

        CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
        CAP4FormDataManager cap4FormDataManager = (CAP4FormDataManager) AppContext.getBean("cap4FormDataManager");
        Long formIdLong = Long.parseLong(formId);
        FormBean formBean = cap4FormManager.getForm(formIdLong,false);

        FormDataMasterBean masterBean = cap4FormManager.getSessioMasterDataBean(Long.parseLong(dataId));
        FormAuthViewBean authViewBean = formBean.getAuthViewBeanById(Long.parseLong(rightId));

        Map<String, Object> stringObjectMap = cap4FormDataManager.dealFormRightChangeResult(formBean, authViewBean, masterBean, true);
        Map<String, FormAuthViewFieldBean> apartAuthMap = (Map<String, FormAuthViewFieldBean>) stringObjectMap.get("apartAuthMap");
        //可转换的控件
        List<String> showFields = new ArrayList<String>();
        if (authViewBean == null) {
            LOG.error("表单转文档，获取到的权限为null，对应operationId = " + rightId);
        } else {
            List<FormAuthViewFieldBean> viewFieldBeans = authViewBean.getFormAuthorizationFieldList();
            for (FormAuthViewFieldBean avfb : viewFieldBeans) {
                FormFieldBean fieldBean = avfb.getFormFieldBean();
                String name = fieldBean.getName();
                FormAuthViewFieldBean auth = apartAuthMap.get(name) == null ? authViewBean.getFormAuthorizationField(name) : apartAuthMap.get(name);
                if (auth.isEditAuth() || Enums.FieldAccessType.browse.getKey().equals(auth.getAccess())){
                    showFields.add(fieldBean.getName());
                }
            }
        }

        FormFieldBean form2Word = formBean.getFieldBeanByName(fieldName);
        Map<String,Object> customParam = (Map<String,Object>) JSONUtil.parseJSONString(form2Word.getCustomParam());
        List<Map> mapping = (List<Map>)customParam.get("mapping");
        List<Map> list = new ArrayList<Map>();
        for (Map map:mapping) {

            Map<String,Object> returnValue = new HashMap<String,Object>();
            String source = (String)map.get("source");
            FormFieldBean sourceBean = formBean.getFieldBeanByName(source);
            if(null == sourceBean){
                continue;
            }
            returnValue.put("inputType",sourceBean.getInputType());
            returnValue.put("source",source);
            returnValue.put("target",map.get("target"));

            FormFieldCtrl fieldCtrl = sourceBean.getFieldCtrl();
            if (null != fieldCtrl){
                String value = fieldCtrl.getTaoHongValue(masterBean,sourceBean,returnValue);
                if (!showFields.contains(source)){
                    //隐藏字段，赋值***
                    value="***";
                }
                returnValue.put("value",value);
            }else{
                LOG.error("表单转文档： "+sourceBean.getName()+" 不存在对应的控件。");
            }

            list.add(returnValue);
        }

        return success(list);
    }

    private Map<String, Object> getAttJson(Attachment att) {
        Map<String, Object> retVal = new HashMap<String, Object>();
        retVal.put("category", String.valueOf(att.getCategory()));
        retVal.put("createdate", String.valueOf(Datetimes.format(att.getCreatedate(), Datetimes.datetimeWithoutSecondStyle)));
        retVal.put("description", att.getDescription());
        retVal.put("fileUrl", String.valueOf(att.getFileUrl()));
        retVal.put("filename", att.getFilename());
        retVal.put("genesisId", String.valueOf(att.getGenesisId()));
        retVal.put("icon", att.getIcon());
        retVal.put("id", String.valueOf(att.getId()));
        retVal.put("mimeType", att.getMimeType());
        retVal.put("new", att.isNew());
        retVal.put("officeTransformEnable", att.getOfficeTransformEnable());
        retVal.put("reference", String.valueOf(att.getReference()));
        retVal.put("subReference", String.valueOf(att.getSubReference()));
        retVal.put("type", String.valueOf(att.getType()));
        retVal.put("size", String.valueOf(att.getSize()));
        retVal.put("v", String.valueOf(att.getV()));
        return retVal;
    }
}
