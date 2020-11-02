package com.seeyon.cap4.template.service;

import com.seeyon.cap4.form.api.ExtendManager;
import com.seeyon.cap4.form.bean.FormAuthViewBean;
import com.seeyon.cap4.form.bean.FormAuthViewFieldBean;
import com.seeyon.cap4.form.bean.FormAuthorizationTableBean;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormBindAuthBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormDataSubBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.FormFieldComEnum;
import com.seeyon.cap4.form.bean.FormQueryTypeEnum;
import com.seeyon.cap4.form.bean.FormRelationshipMapBean;
import com.seeyon.cap4.form.bean.FormTableBean;
import com.seeyon.cap4.form.bean.FormViewBean;
import com.seeyon.cap4.form.bean.SimpleObjectBean;
import com.seeyon.cap4.form.modules.business.BizConfigBean;
import com.seeyon.cap4.form.modules.engin.relation.FormRelationEnums;
import com.seeyon.cap4.form.modules.engin.trigger.CAP4FormTriggerSourceRecordDAO;
import com.seeyon.cap4.form.po.CAPFormRelationRecord;
import com.seeyon.cap4.form.po.CAPFormTriggerSourceRecord;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.cap4.form.util.FormConstant;
import com.seeyon.cap4.monitor.utils.CAP4MonitorUtil;
import com.seeyon.cap4.template.bean.FormDataParamBean;
import com.seeyon.cap4.template.bean.FormDataSignatureParamBean;
import com.seeyon.cap4.template.constant.CAPBusinessConstant;
import com.seeyon.cap4.template.constant.CAPBusinessEnum;
import com.seeyon.cap4.template.exception.FormDataBusinessException;
import com.seeyon.cap4.template.manager.CAPTransFormDataBeanManager;
import com.seeyon.cap4.template.util.CAPAttachmentUtil;
import com.seeyon.cap4.template.util.CAPFormDataLogUtil;
import com.seeyon.cap4.template.util.CAPFormUtil;
import com.seeyon.cap4.template.result.FormDataResult;
import com.seeyon.cap4.template.vo.AFormTableDataVO;
import com.seeyon.cap4.template.vo.FormDataVO;
import com.seeyon.cap4.template.vo.FormTableFormmainDataVO;
import com.seeyon.cap4.template.vo.FormTableFormsonDataVO;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.PerformanceConfig;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.form.modules.serialNumber.SerialCalRecordManager;
import com.seeyon.ctp.form.po.FormSerialCalculateRecord;
import com.seeyon.ctp.monitor.perf.jdbcmonitor.proxyobj.JMTrackUtils;
import com.seeyon.ctp.report.engine.api.manager.ReportApi;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.simulation.api.SimulationApi;
import com.seeyon.util.ReadConfigTools;
import com.seeyon.v3x.common.security.AccessControlBean;
import com.seeyon.v3x.common.web.login.CurrentUser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * cap 表单打开相关逻辑
 *
 * @author wanxiang
 * @since V7.1 International
 */
public class CAPFormDataOpenService extends AbstractCAPFormDataService {
    private static final Log LOGGER = CtpLogFactory.getLog(CAPFormDataOpenService.class);

    private AffairManager affairManager;
    private SerialCalRecordManager serialCalRecordManager;
    private ReportApi reportApi;
    private SimulationApi simulationApi;
    private CAP4FormTriggerSourceRecordDAO cap4FormTriggerSourceRecordDAO;
    private CAPCustomService capCustomService;
    private CAPFormManager capFormManager;
    private CAPFormDataSignatureService capFormDataSignatureService;
    private CAPFormDataForwardService capFormDataForwardService;
    private CAPTransFormDataBeanManager capTransFormDataBeanManager;

    private static final String ATTACHMENT_INFO = "attachmentInfo";
    private static final String RELATION_INFO = "relationInfo";
    private static final String RELATION_DATA = "relationData";
    private static final String ENUMS = "enums";
    private static final String TRIGGER_DATA = "triggerData";
    private static final String RECORD_ID = "recordId";
    /**
     * 移动端明细表数据阈值
     */
    private static final int PHONE_FORMSON_NUM_THRESHOLD = 200;
    /**
     * 移动端明细表数据阈值
     */
    private static final int PHONE_FORMSON_TOTAL_THRESHOLD = 2000;

    /**
     * 打开或编辑表单
     * 通过ServiceProxy访问
     */
    public Object createOrEditForm(FormDataParamBean formDataParamBean) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            if (formDataParamBean.getCurrentUser() == null) {
                formDataParamBean.setCurrentUser(AppContext.getCurrentUser());
            }
            if (Strings.isBlank(formDataParamBean.getLang())) {
                formDataParamBean.setLang(AppContext.getLocale().toString());
            }
            if (formDataParamBean.getFormId() != null) {
                return this.doCreatePreForm(formDataParamBean);
            } else if ("simulate".equals(formDataParamBean.getDataFrom())) {
                return this.doCreateSimulationMap(formDataParamBean);
            } else {
                return this.doCreateOrEditForm(formDataParamBean, true);
            }
        } catch (FormDataBusinessException e) {
            result.put(CAPBusinessConstant.KEY_CODE, e.getCode());
            result.put(CAPBusinessConstant.KEY_MESSAGE, e.getMessage());
        } catch (Exception e) {
            LOGGER.error("createOrEditForm Exception ", e);
            if (e instanceof BusinessException && CODE12.equals(((BusinessException) e).getCode())) {
                result.put(CAPBusinessConstant.KEY_CODE, CODE12);
                result.put(CAPBusinessConstant.KEY_MESSAGE, ResourceUtil.getString(MESSAGE12));
                // bug OA-181979
                cap4FormManager.removeSessionMasterDataBean(formDataParamBean.getModuleId());
            } else {
                result.put(CAPBusinessConstant.KEY_CODE, CODE1);
                result.put(CAPBusinessConstant.KEY_MESSAGE, ResourceUtil.getString(MESSAGE1) + " " + e.getMessage());
            }
        }
        return result;
    }

    /**
     * 创建表单信息result
     *
     * @param formDataVO
     * @param formBean
     * @throws BusinessException
     */
    private FormDataVO.FormInfo createResultFormInfo(FormDataVO formDataVO, FormBean formBean) throws BusinessException {
        FormDataVO.FormInfo formInfo = formDataVO.new FormInfo();
        formInfo.setFormType(String.valueOf(formBean.getFormType()));
        formInfo.setCategoryId(String.valueOf(formBean.getCategoryId()));
        formInfo.setFormName(formBean.getFormName());
        formInfo.setDesc(formBean.getDesc());
        formInfo.setIsEg(formBean.isPublished() ? CAPBusinessEnum.TrueOrFalseEnum.FALSE.getKey() : CAPBusinessEnum.TrueOrFalseEnum.TRUE.getKey());
        formInfo.setExtendFilePaths(CAPFormUtil.getExtendFilePath());
        formInfo.setWaterMark(CAPFormUtil.getFormWaterMark(formBean));
        Integer time = PerformanceConfig.getInstance().getIntegerConfig("cap4.sessionFormDataValidTime");
        if (time == null) {
            String configTime = AppContext.getSystemProperty("cap4.sessionFormDataValidTime");
            time = configTime == null ? time : Integer.parseInt(configTime);
        }
        if (time != null) {
            time = time > 120 ? 120 : time;
            formInfo.setDataValidTime(String.valueOf(time));
        }
        formDataVO.setFormInfo(formInfo);
        return formInfo;
    }

    /**
     * 创建视图信息resutl
     *
     * @param formDataVO
     * @param formViewBean
     */
    private void createResultPreViewInfo(FormDataVO formDataVO, FormViewBean formViewBean) {
        FormDataVO.ViewInfo viewInfo = formDataVO.new ViewInfo();
        viewInfo.setViewId(String.valueOf(formViewBean.getId()));
        viewInfo.setName(formViewBean.getFormViewName());
        viewInfo.setViewContent(formViewBean.getFormViewContent());

        formDataVO.setViewInfo(viewInfo);
    }

    /**
     * 构造表单数据
     *
     * @param dataVO
     * @param tableBean
     * @param fieldInfos
     */
    private void createResultTableData(AFormTableDataVO dataVO, FormTableBean tableBean, Map<String, Object> fieldInfos) {
        dataVO.setTableName(tableBean.getTableName());
        dataVO.setFrontTableName(tableBean.getFrontTableName());
        dataVO.setDisplay(tableBean.getDisplay());
        dataVO.setFieldInfo(fieldInfos);
    }

    /**
     * 预览表单
     */
    private FormDataResult doCreatePreForm(FormDataParamBean formDataParamBean) throws BusinessException {
        FormBean formBean = cap4FormCacheManager.getForm(formDataParamBean.getFormId());
        FormViewBean formViewBean;
        if (formDataParamBean.getViewId() != null) {
            formViewBean = formBean.getFormView(formDataParamBean.getViewId(), formDataParamBean.getLang());
        } else if (Strings.isNotBlank(formDataParamBean.getRightId()) && (!"-1".equals(formDataParamBean.getRightId()))) {
            // 适配协同调用模板预览，OA-190396 传递的formId 和 rightId
            formViewBean = formBean.getFormView(Long.parseLong(formDataParamBean.getRightId().split("[.]")[0]), formDataParamBean.getLang());
        } else {
            List<FormViewBean> pcFormViewBeans = formBean.getFormViewList(Enums.ViewType.SeeyonForm, formDataParamBean.getLang());
            formViewBean = CollectionUtils.isNotEmpty(pcFormViewBeans) ? pcFormViewBeans.get(0) : null;
        }
        if (formViewBean == null) {
            throw new FormDataBusinessException(CODE13, ResourceUtil.getString(MESSAGE13));
        }
        FormDataVO formDataVO = new FormDataVO();
        // 构造表单
        FormDataVO.FormInfo formInfo = this.createResultFormInfo(formDataVO, formBean);
        formInfo.setFlowDealOptionCount(String.valueOf(this.getFlowDealOptionCount(formBean, null)));
        // 构造视图
        this.createResultPreViewInfo(formDataVO, formViewBean);
        // 构造表单数据
        FormDataVO.TableInfo tableInfo = formDataVO.new TableInfo();
        formDataVO.setTableInfo(tableInfo);

        // 主表字段信息
        Map<String, Object> masterFieldMap = new HashMap<String, Object>();
        // 做条默认数据,运行态解析依赖数据
        FormDataMasterBean formDataMasterBean = FormDataMasterBean.newInstance(formBean);
        Map<String, List<FormFieldBean>> fieldBeanMap = CAPFormUtil.getTableName2FieldBeans(formBean, formViewBean);
        List<FormFieldBean> masterFields = fieldBeanMap.get(formBean.getMasterTableBean().getTableName());
        Map<String, List<Map<String, String>>> capFormAttachmentsMap = new HashMap<String, List<Map<String, String>>>();
        if (masterFields != null) {
            for (FormFieldBean formFieldBean : masterFields) {
                Map<String, Object> fieldInfo = CAPFormUtil.getFormFieldInfo(formFieldBean, formDataMasterBean);
                CAPFormUtil.resetFormFieldInfo(fieldInfo, CAPBusinessEnum.FormOperateType.BROWSE.getKey(), null);
                // 附件,图片相关信息
                if (formFieldBean.isAttachment()) {
                    fieldInfo.put(ATTACHMENT_INFO, CAPAttachmentUtil.getFieldAttachmentMap(formDataMasterBean.getId(), formFieldBean, null, capFormAttachmentsMap));
                }
                Map<String, Object> dataInfo = CAPFormUtil.getDisplayValueMap(formDataMasterBean.getFieldValue(formFieldBean.getName()), formFieldBean, null);
                fieldInfo.putAll(dataInfo);
                masterFieldMap.put(formFieldBean.getName(), fieldInfo);
            }
        }
        FormTableFormmainDataVO formmain = new FormTableFormmainDataVO();
        this.createResultTableData(formmain, formBean.getMasterTableBean(), masterFieldMap);
        tableInfo.setFormmain(formmain);

        List<FormTableFormsonDataVO> formsons = new ArrayList<FormTableFormsonDataVO>();
        tableInfo.setFormson(formsons);
        for (FormTableBean subTable : formBean.getSubTableBean()) {
            List<FormFieldBean> subFields = fieldBeanMap.get(subTable.getTableName());
            Map<String, Object> subFieldInfos = new HashMap<String, Object>();
            if (subFields != null) {
                for (FormFieldBean formFieldBean : subFields) {
                    Map<String, Object> fieldInfo = CAPFormUtil.getFormFieldInfo(formFieldBean, formDataMasterBean);
                    CAPFormUtil.resetFormFieldInfo(fieldInfo, CAPBusinessEnum.FormOperateType.BROWSE.getKey(), null);
                    // 附件,图片相关信息
                    if (formFieldBean.isAttachment()) {
                        fieldInfo.put(ATTACHMENT_INFO, CAPAttachmentUtil.getFieldAttachmentMap(formDataMasterBean.getId(), formFieldBean, null, capFormAttachmentsMap));
                    }
                    subFieldInfos.put(formFieldBean.getName(), fieldInfo);
                }
            }
            List<Map<String, Object>> dataSubLineInfos = new ArrayList<Map<String, Object>>();
            Map<String, List<FormDataSubBean>> dataSubBeanMap = formDataMasterBean.getSubTables();
            List<FormDataSubBean> dataSubBeans = dataSubBeanMap.get(subTable.getTableName());
            for (FormDataSubBean formDataSubBean : dataSubBeans) {
                Map<String, Object> dataSubInfos = new HashMap<String, Object>();
                dataSubLineInfos.add(dataSubInfos);
                dataSubInfos.put(RECORD_ID, String.valueOf(formDataSubBean.getId()));
                if (subFields != null) {
                    for (FormFieldBean formFieldBean : subFields) {
                        Map<String, Object> dataInfo = CAPFormUtil.getDisplayValueMap(formDataSubBean.getFieldValue(formFieldBean.getName()), formFieldBean, null);
                        // 明细表多级枚举 需要给每个字段都设置enums
                        if (FormFieldComEnum.SELECT.getKey().equalsIgnoreCase(formFieldBean.getInputType())) {
                            dataInfo.put(ENUMS, CAPFormUtil.getFieldEnumJSON(formFieldBean, formDataMasterBean, formDataSubBean, false));
                        }
                        dataSubInfos.put(formFieldBean.getName(), dataInfo);
                    }
                }
            }
            FormTableFormsonDataVO formson = new FormTableFormsonDataVO();
            this.createResultTableData(formson, subTable, subFieldInfos);
            formson.setDataSubLineInfos(dataSubLineInfos);
            formson.setTableButton(capRuntimeCalcManager.getFormSonAuthButtons(formBean, null, subTable, null, true));
            formson.setTableAuth(capRuntimeCalcManager.getFormSonAuth(null, true));
            formsons.add(formson);
        }
        FormDataResult<FormDataVO> formDataResult = new FormDataResult<FormDataVO>();
        formDataResult.success(formDataVO);
        return formDataResult;
    }

    /**
     * 打开表单
     *
     * @param formDataParamBean
     * @param needCheckAuth
     * @return
     * @throws BusinessException
     * @throws SQLException
     */
    private Object doCreateOrEditForm(FormDataParamBean formDataParamBean, boolean needCheckAuth) throws BusinessException, SQLException {
        CtpContentAll contentAll = this.getCtpContentAllByModuleIdAndType(formDataParamBean);
        if (contentAll == null) {
            LOGGER.info("doCreateOrEditForm 找不到正文！" + formDataParamBean.toString());
            throw new FormDataBusinessException(CODE11, ResourceUtil.getString(MESSAGE11));
        }
        if (Strings.isNotBlank(contentAll.getContent()) && contentAll.getContentType().equals(MainbodyType.HTML.getKey())) {
            return capFormDataForwardService.doCreateForwardMap(formDataParamBean, contentAll);
        } else {
            FormDataResult<FormDataVO> result = new FormDataResult<FormDataVO>();
            return this.doCreateOrEditNormalForm(formDataParamBean, needCheckAuth, contentAll, result);
        }
    }

    /**
     * 正常表单打开
     * 转发表单也在调用（需要传递用户）
     *
     * @param formDataParamBean
     * @param needCheckAuth
     * @param contentAll
     * @return
     * @throws BusinessException
     * @throws SQLException
     */
    protected FormDataResult<FormDataVO> doCreateOrEditNormalForm(FormDataParamBean formDataParamBean, boolean needCheckAuth, CtpContentAll contentAll, FormDataResult<FormDataVO> result) throws BusinessException, SQLException {
        this.dealRightIdAndViews(formDataParamBean, contentAll.getContentTemplateId());
        if (Strings.isBlank(formDataParamBean.getRightId()) || Strings.isBlank(formDataParamBean.getOperateType())) {
            result.error(CODE4, String.format(ResourceUtil.getString(MESSAGE4), "rightId or operateType"));
        } else {
            boolean isAccess = !needCheckAuth || this.checkCreateOrEditAuth(contentAll, formDataParamBean);
            if (!isAccess) {
                result.error(CODE6, ResourceUtil.getString(MESSAGE6));
            } else {
                try {
                    FormDataVO formDataVO = this.doCreateOrEditPackDataMap(contentAll, formDataParamBean, null);
//                    zhou
                    ReadConfigTools configTools = new ReadConfigTools();
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String dateTime = LocalDateTime.now(ZoneOffset.of("+8")).format(dtf);

                    FormTableFormmainDataVO f = formDataVO.getTableInfo().getFormmain();
                    String tableName = f.getTableName();
                    String id = formDataParamBean.getModuleId() + "";
                    String fu = configTools.getString("table_formmain_parent").trim();
                    String isRead = configTools.getString("isRead").trim();

                    String fuCol = configTools.getString("table_formmain_readColumn").trim();
                    String blankVal = configTools.getString("table_formmain_readColumn_val").trim();
//                    当点击会务通知列表是修改会务通知主表阅读字段的状态为已读
                    String huiWu = "select " + fuCol + " from " + fu + " where id=" + id;
                    if (fu.equals(tableName)) {
                        Connection connection = JDBCAgent.getRawConnection();
                        PreparedStatement ps = null;
                        ResultSet rs = null;
                        try {
                            ps = connection.prepareStatement(huiWu);
                            rs = ps.executeQuery();
                            while (rs.next()) {
                                String fieldVal = rs.getString(fuCol);
                                String updateSql = "update " + fu + " set " + fuCol + " = " + blankVal + " where id= " + id;
                                if (null == fieldVal) {
                                    ps = connection.prepareStatement(updateSql);
                                    ps.executeUpdate();
                                } else {
                                    if (!fieldVal.equals(isRead)) {
                                        ps = connection.prepareStatement(updateSql);
                                        ps.executeUpdate();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (null != ps) {
                                ps.close();
                            }
                            if (null != rs) {
                                rs.close();
                            }
                            if (null != connection) {
                                connection.close();
                            }
                        }
                    }

                    String tableInfo = configTools.getString("table_info").trim();
                    if (tableInfo.equals(tableName)) {
                        String columnName = configTools.getString("table_info_column").trim();
                        StringBuffer querySql = new StringBuffer();
                        querySql.append(" select ");
                        querySql.append(columnName + " from " + tableInfo + " where id= '" + id + "'");

                        StringBuffer updateSql = new StringBuffer();

                        updateSql.append("update " + tableInfo + " set " + columnName + " = to_date('" + dateTime + "','yyyy-MM-dd HH24:mi:ss')  where id ='" + id + "'");
                        Connection connection = JDBCAgent.getRawConnection();
                        PreparedStatement ps = null;
                        PreparedStatement psInsert = null;
                        ResultSet rs = null;
                        ResultSet sonrs = null;

                        ResultSet rs2 = null;
                        try {
                            ps = connection.prepareStatement(querySql.toString());
                            rs = ps.executeQuery();
                            while (rs.next()) {
                                String val = rs.getString("field0020");
                                if (null == val || "".equals(val) || "null".equals(val)) {
                                    psInsert = connection.prepareStatement(updateSql.toString());
                                    psInsert.executeUpdate();


//
                                    String queryReciverById = "select field0009,field0012 from " + tableInfo + " where id = " + id;
                                    ps = connection.prepareStatement(queryReciverById);
                                    rs2 = ps.executeQuery();
                                    String field0009 = "";
                                    String field0012 = "";
                                    while (rs2.next()) {
                                        field0009 = rs2.getString("field0009");
                                        field0012 = rs2.getString("field0012");
                                    }

                                    String son = configTools.getString("table_formson_son");
                                    String formsonReciverTime = configTools.getString("formson_reciverTime");
                                    String formsonUserId = configTools.getString("formson_userId");
                                    String querySendSonId = "select f24.id from  " + fu + " f23," + son + " f24 where f23.id=f24.formmain_id and f23.field0014='" + field0012 + "' and f24." + formsonUserId + "='" + field0009 + "'";
                                    ps = connection.prepareStatement(querySendSonId);
                                    rs = ps.executeQuery();
                                    String sonIsRead = configTools.getString("formson_isRead");
                                    //zhou:2020-09-27 在这里判断一下接收时间是否为空，如果为空则执行修改操作。
                                    String sqlBySon = "select " + formsonReciverTime + " from " + son + " where id=";
                                    Long son0171=null;
                                    while (rs.next()) {
                                        son0171=rs.getLong("id");
                                        sqlBySon += rs.getLong("id");
                                    }
                                    ps = connection.prepareStatement(sqlBySon);
                                    sonrs = ps.executeQuery();

                                    while (sonrs.next()) {
                                        String time = sonrs.getString(formsonReciverTime);
                                        if (null == time || "".equals(time)) {
                                            String updateSonSql = "update " + son + " set " + formsonReciverTime + " = to_date('" + dateTime + "','yyyy-MM-dd HH24:mi:ss') ," + sonIsRead + "='" + isRead + "' where id=?";
                                            ps = connection.prepareStatement(updateSonSql);
                                            ps.setLong(1, son0171);
                                            ps.executeUpdate();
                                        }
                                    }
                                    rs2.close();
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (null != rs) {
                                rs.close();
                            }
                            if (null != sonrs) {
                                sonrs.close();
                            }
                            if (null != psInsert) {
                                psInsert.close();
                            }
                            if (null != ps) {
                                ps.close();
                            }
                            if (null != connection) {
                                connection.close();
                            }
                        }

                    }
                    result.success(formDataVO);
                } catch (FormDataBusinessException e) {
                    result.error(e.getCode(), e.getMessage());
                }
            }
        }
        return result;
    }

    /**
     * 流程仿真
     */
    private FormDataResult<FormDataVO> doCreateSimulationMap(FormDataParamBean formDataParamBean) throws BusinessException, SQLException {
        Long formMasterId = formDataParamBean.getFormMasterId();
        FormDataMasterBean formDataMasterBean = formMasterId == null ? null : cap4FormManager.getSessioMasterDataBean(formMasterId);
        if (formDataMasterBean == null) {
            // 调用协同的接口获取string 转换为bean
            String dataStr = simulationApi.getFormBeanData(String.valueOf(formDataParamBean.getModuleId()));
            formDataMasterBean = capTransFormDataBeanManager.transFormDataBeanFromJson(dataStr);
        }
        FormDataResult<FormDataVO> result = new FormDataResult<FormDataVO>();
        if (formDataMasterBean == null) {
            // 无法构建bean出来，调用普通的create接口
            formDataParamBean.setDataFrom(null);
            CtpContentAll contentAll = this.getCtpContentAllByModuleIdAndType(formDataParamBean);
            if (contentAll == null) {
                result.error(CODE11, ResourceUtil.getString(MESSAGE11));
                return result;
            }
            this.doCreateOrEditNormalForm(formDataParamBean, true, contentAll, result);
        } else {
            // 伪造一个contentAll
            CtpContentAll contentAll = new CtpContentAll();
            FormBean formBean = cap4FormCacheManager.getForm(formDataMasterBean.getFormTable().getFormId());
            contentAll.setContentTemplateId(formBean.getId());
            contentAll.setContentType(MainbodyType.FORM.getKey());
            contentAll.setModuleType(formBean.getFormType() == Enums.FormType.processesForm.getKey() ? ModuleType.collaboration.getKey() : ModuleType.cap4UnflowForm.getKey());
            contentAll.setModuleTemplateId(-1L);
            contentAll.setModuleId(formDataParamBean.getModuleId());
            contentAll.setContentDataId(formDataMasterBean.getId());
            contentAll.setNewId();
            formDataMasterBean.putExtraAttr("isNew", true);
            this.dealRightIdAndViews(formDataParamBean, formBean.getId());
            FormDataVO formDataVO = this.doCreateOrEditPackDataMap(contentAll, formDataParamBean, formDataMasterBean);
            result.success(formDataVO);
        }
        return result;
    }

    private FormDataVO.Content createResultContent(FormDataVO formDataVO, CtpContentAll contentAll) {
        FormDataVO.Content content = formDataVO.new Content();
        content.setId(String.valueOf(contentAll.getId()));
        content.setModuleTemplateId(String.valueOf(contentAll.getModuleTemplateId()));
        content.setContentTemplateId(String.valueOf(contentAll.getContentTemplateId()));
        content.setContentType(String.valueOf(contentAll.getContentType()));
        content.setModuleId(String.valueOf(contentAll.getModuleId()));
        content.setTitle(contentAll.getTitle());
        formDataVO.setContent(content);
        return content;
    }

    private void createResultViewInfo(FormDataVO formDataVO, FormViewBean formViewBean, FormDataParamBean formDataParamBean) {
        int userAgent = formDataParamBean.getUserAgent();
        FormDataVO.ViewInfo viewInfo = formDataVO.new ViewInfo();
        formDataVO.setViewInfo(viewInfo);
        viewInfo.setPcViewCount(String.valueOf(formDataParamBean.getPcRightIdSet().size()));
        viewInfo.setPhoneViewCount(String.valueOf(formDataParamBean.getPhoneRightIdSet().size()));
        viewInfo.setViewId(String.valueOf(formViewBean.getId()));
        viewInfo.setName(formViewBean.getFormViewName());
        if ((formViewBean.isPhone() && CAPFormUtil.isPhoneLogin(userAgent)) || (formViewBean.isPc() && CAPFormUtil.isPcLogin(userAgent))) {
            viewInfo.setViewContent(formViewBean.getFormViewContent());
        } else {
            viewInfo.setViewContent("");
        }

    }

    private void createExtend(FormBean formBean, FormDataMasterBean formDataMasterBean,
                              FormAuthViewBean formAuthViewBean, CreateOrEditExtendData extendData,
                              FormDataVO formDataVO, FormDataParamBean formDataParamBean) {
        ExtendManager extendManager = CAPFormUtil.getExtendManager();
        if (extendManager != null) {
            FormViewBean formViewBean = formBean.getFormView(formAuthViewBean.getFormViewId(), formDataParamBean.getLang());
            formDataVO.getExtend().put("conditionHidden", extendManager.getFormConditionHidden(formDataMasterBean, String.valueOf(formBean.getId()), String.valueOf(formViewBean.getId()), formDataParamBean.getUserAgent()));
        }
        formDataVO.getExtend().putAll(this.buildExtendResult(formBean, formDataMasterBean, formAuthViewBean, null));
    }

    /**
     * 验证编辑锁
     *
     * @param formId
     * @param contentDataId
     * @throws BusinessException
     * @throws SQLException
     */
    private void checkUpdateLock(Long formId, Long contentDataId) throws BusinessException, SQLException {
        boolean checkLock = cap4FormManager.checkLock(formId, String.valueOf(contentDataId));
        if (checkLock) {
            throw new FormDataBusinessException(CODE8, ResourceUtil.getString(MESSAGE8));
        } else {
            String checkDataLockForEdit = cap4FormManager.checkDataLockForEdit(String.valueOf(contentDataId));
            if (Strings.isNotBlank(checkDataLockForEdit)) {
                throw new FormDataBusinessException(CODE9, checkDataLockForEdit);
            } else {
                cap4FormManager.lockFormData(contentDataId);
            }
        }
    }

    private void initNewUnFlow(FormBindAuthBean formBindAuthBean, FormDataParamBean formDataParamBean) throws FormDataBusinessException {
        int userAgent = formDataParamBean.getUserAgent();
        // 验证新建权限
        SimpleObjectBean simpleObjectBean = formBindAuthBean.getAuthObjByName(FormBindAuthBean.AuthName.ADD.getKey());
        if ((CAPFormUtil.isPhoneLogin(userAgent) && Strings.isBlank(simpleObjectBean.getPhoneValue()))
                || (CAPFormUtil.isPcLogin(userAgent) && Strings.isBlank(simpleObjectBean.getValue()))) {
            throw new FormDataBusinessException(CODE6, ResourceUtil.getString(MESSAGE6));
        }
        if (DEFAULT_RIGHT_ID.equals(formDataParamBean.getRightId())) {
            String valueStr = CAPFormUtil.isPhoneLogin(userAgent) ? simpleObjectBean.getPhoneValue() : simpleObjectBean.getValue();
            if (valueStr.contains(".")) {
                formDataParamBean.setRightId(valueStr.split("\\.")[1]);
            }
            // 新建适配原样表单逻辑
            if (Strings.isNotBlank(simpleObjectBean.getPhoneValue())) {
                formDataParamBean.getPhoneRightIdSet().add(simpleObjectBean.getPhoneValue().split("\\.")[1]);
            }
            if (Strings.isNotBlank(simpleObjectBean.getValue())) {
                formDataParamBean.getPcRightIdSet().add(simpleObjectBean.getValue().split("\\.")[1]);
            }
        }
    }

    /**
     * 无流程浏览
     *
     * @param formBean
     * @param formBindAuthBean
     * @param formDataParamBean
     */
    private void initUnFlowBrowse(FormBean formBean, FormBindAuthBean formBindAuthBean, FormDataParamBean formDataParamBean) {
        int userAgent = formDataParamBean.getUserAgent();
        List<Map<String, Object>> views = formDataParamBean.getViews();
        //浏览
        SimpleObjectBean simpleObjectBean = formBindAuthBean.getAuthObjByName(FormBindAuthBean.AuthName.BROWSE.getKey());
        String valueStr = CAPFormUtil.isPhoneLogin(userAgent) ? simpleObjectBean.getPhoneValue() : simpleObjectBean.getValue();
        if (valueStr.contains("|")) {
            String[] values = valueStr.split("\\|");
            for (int i = 0; i < values.length; i++) {
                if (values[i].contains(".")) {
                    String[] value = values[i].split("\\.");
                    boolean hasView = false;
                    for (Map<String, Object> tempView : views) {
                        if (value[1].equals(String.valueOf(tempView.get(RIGHT_ID)))) {
                            hasView = true;
                            break;
                        }
                    }
                    if (!hasView) {
                        // 多视图切换，不需要去获取多语言的，这里只需要名称，可能会存在当前语言没有视图
                        FormViewBean formViewBean = formBean.getFormView(Long.parseLong(value[0]));
                        Map<String, Object> view = new HashMap<String, Object>();
                        view.put(RIGHT_ID, value[1]);
                        view.put(NAME, formViewBean.getFormViewName());
                        views.add(view);
                    }
                }
            }
        }
    }

    /**
     * 经过处理之后 无视图的情况
     *
     * @param formBean
     * @param formDataParamBean
     * @throws FormDataBusinessException
     */
    private void doWhenViewsEmpty(FormBean formBean, FormDataParamBean formDataParamBean) throws FormDataBusinessException {
        String rightId = formDataParamBean.getRightId();
        int userAgent = formDataParamBean.getUserAgent();
        FormAuthViewBean formAuthViewBean = formBean.getAuthViewBeanById(Long.parseLong(rightId));
        if (formAuthViewBean == null) {
            throw new FormDataBusinessException(CODE6, ResourceUtil.getString(MESSAGE6));
        }
        FormViewBean formViewBean = formBean.getFormView(formAuthViewBean.getFormViewId(), formDataParamBean.getLang());
        if (formViewBean == null) {
            throw new FormDataBusinessException(CODE13, ResourceUtil.getString(MESSAGE13));
        }
        Map<String, Object> view = new HashMap<String, Object>();
        view.put(RIGHT_ID, rightId);
        view.put(NAME, formViewBean.getFormViewName());
        formDataParamBean.getViews().add(view);
        if (formViewBean.isPhone() && CAPFormUtil.isPhoneLogin(userAgent)) {
            formDataParamBean.getPhoneRightIdSet().add(rightId);
        } else if (formViewBean.isPc() && CAPFormUtil.isPcLogin(userAgent)) {
            formDataParamBean.getPcRightIdSet().add(rightId);
        }
    }

    /**
     * 获取流程处理意见控件数量
     *
     * @param formBean
     * @param formAuthViewBean
     * @return
     */
    private int getFlowDealOptionCount(FormBean formBean, FormAuthViewBean formAuthViewBean) {
        List<FormFieldBean> flowDealOptionFields = formBean.getFieldsByType(FormFieldComEnum.FLOWDEALOPITION);
        int flowDealOptionCount = 0;
        if (formAuthViewBean != null) {
            for (FormFieldBean formFieldBean : flowDealOptionFields) {
                String auth = formAuthViewBean.getFormAuthorizationField(formFieldBean.getName()).getAccess();
                if (Enums.FieldAccessType.edit.getKey().equals(auth) || Enums.FieldAccessType.add.getKey().equals(auth)) {
                    flowDealOptionCount++;
                }
            }
        } else {
            // 预览
            flowDealOptionCount = flowDealOptionFields.size();
        }
        return flowDealOptionCount;
    }

    /*
     * 构建打开表单返回的参数map
     * 此方法的参数都是经过处理的，比如rightId，userAgent，views
     * 此方法内部不再判断是否有权限，在调用前已经处理权限
     */
    private FormDataVO doCreateOrEditPackDataMap(CtpContentAll contentAll, FormDataParamBean formDataParamBean, FormDataMasterBean formDataMasterBean) throws BusinessException, SQLException {
        FormDataVO formDataVO = new FormDataVO();
        FormDataVO.Content content = this.createResultContent(formDataVO, contentAll);
        // 此时的rightId是已经处理过的最终值，如果=-1，则还会被处理一次
        if (formDataParamBean.getRightId().contains(".")) {
            formDataParamBean.setRightId(formDataParamBean.getRightId().split("\\.")[1]);
        }
        int userAgent = formDataParamBean.getUserAgent();
        List<Map<String, Object>> views = formDataParamBean.getViews();
        formDataVO.setViews(views);
        String moduleId = formDataParamBean.getModuleId() == null ? null : formDataParamBean.getModuleId().toString();
        String operateType = formDataParamBean.getOperateType();
        Integer moduleType = formDataParamBean.getModuleType();
        String from = formDataParamBean.getFrom();
        ModuleType mType = ModuleType.getEnumByKey(moduleType);
        Long contentDataId = contentAll.getContentDataId();
        FormBean formBean = cap4FormCacheManager.getForm(contentAll.getContentTemplateId());
        BizConfigBean bizConfigBean = businessManager4.findBizConfigByFormId(formBean.getId());
        if (null != bizConfigBean) {
            JMTrackUtils.getAndNew("跟踪SQL", "修改详情", CAP4MonitorUtil.contactTag(bizConfigBean), CAP4MonitorUtil.contactTag(formBean));
        } else {
            JMTrackUtils.getAndNew("跟踪SQL", "修改详情", CAP4MonitorUtil.contactTag(formBean));
        }
        if (formDataParamBean.getPcRightIdSet() == null) {
            formDataParamBean.setPcRightIdSet(new HashSet<String>());
        }
        if (formDataParamBean.getPhoneRightIdSet() == null) {
            formDataParamBean.setPhoneRightIdSet(new HashSet<String>());
        }
        SimpleObjectBean printSimpleObjectBean = null;
        if (mType == ModuleType.cap4UnflowForm) {
            FormBindAuthBean formBindAuthBean = formDataParamBean.getFormBindAuthBean();
            /**
             * 有无二维码插件和是否允许扫码录入参数分开
             * 浏览权限下一定是不允许扫码录入
             * */
            String allowQRScan = CAPBusinessEnum.FormOperateType.BROWSE.getKey().equals(operateType) ? CAPBusinessEnum.TrueOrFalseEnum.FALSE.getKey() : formBindAuthBean.getScanCodeInput();
            content.setAllowQRScan(CAPBusinessEnum.TrueOrFalseEnum.FALSE.getKey().equals(allowQRScan) ? false : true);
            content.setBarCode(AppContext.hasPlugin("barCode"));
            if (CAPBusinessEnum.FormOperateType.UPDATE.getKey().equals(operateType)) {
                this.checkUpdateLock(formBean.getId(), contentDataId);
            }
            if (formDataParamBean.getFormTemplateId() != null) {
                if (contentDataId == null) {
                    this.initNewUnFlow(formBindAuthBean, formDataParamBean);
                }
                if (CAPBusinessEnum.FormOperateType.BROWSE.getKey().equals(operateType)) {
                    //浏览
                    this.initUnFlowBrowse(formBean, formBindAuthBean, formDataParamBean);
                    printSimpleObjectBean = formBindAuthBean.getAuthObjByName(FormBindAuthBean.AuthName.ALLOWPRINT.getKey());
                }
            }
        }
        if (views.size() == 0) {
            this.doWhenViewsEmpty(formBean, formDataParamBean);
        }
        // 构造正文
        content.setRightId(formDataParamBean.getRightId());
        content.setModuleType(moduleType.toString());
        content.setIsMerge(CAPBusinessEnum.TrueOrFalseEnum.FALSE.getKey());
        content.setNeedCheckRule(CAPBusinessEnum.TrueOrFalseEnum.TRUE.getKey());
        content.setNeedDataUnique(CAPBusinessEnum.TrueOrFalseEnum.TRUE.getKey());
        content.setNeedSn(CAPBusinessEnum.TrueOrFalseEnum.TRUE.getKey());

        FormAuthViewBean formAuthViewBean = formBean.getAuthViewBeanById(Long.parseLong(formDataParamBean.getRightId()));
        FormViewBean formViewBean = formBean.getFormView(formAuthViewBean.getFormViewId(), formDataParamBean.getLang());
        if (formViewBean == null) {
            throw new FormDataBusinessException(CODE13, ResourceUtil.getString(MESSAGE13));
        }
        // 构造表单
        FormDataVO.FormInfo formInfo = this.createResultFormInfo(formDataVO, formBean);
        formInfo.setIsPrint(printSimpleObjectBean != null && CAPBusinessEnum.TrueOrFalseEnum.TRUE.getName().equals(printSimpleObjectBean.getValue()) ? CAPBusinessEnum.TrueOrFalseEnum.TRUE.getKey() : CAPBusinessEnum.TrueOrFalseEnum.FALSE.getKey());
        formInfo.setFlowDealOptionCount(String.valueOf(this.getFlowDealOptionCount(formBean, formAuthViewBean)));

        CreateOrEditExtendData extendData = new CreateOrEditExtendData();
        extendData.setOperateType(operateType);
        formDataMasterBean = this.getCreateOrEditFormDataMasterBean(formDataMasterBean, formBean, formAuthViewBean, contentAll, extendData, formDataParamBean);
        // 移除无控件的视图
        this.removeEmptyFieldsRightId(formBean, formDataParamBean.getPcRightIdSet(), formDataParamBean);
        this.removeEmptyFieldsRightId(formBean, formDataParamBean.getPhoneRightIdSet(), formDataParamBean);
        // 构造视图信息
        this.createResultViewInfo(formDataVO, formViewBean, formDataParamBean);
        cap4FormRelationActionManager.removeCacheRelationData(formDataMasterBean.getId());
        boolean isEdit = CAPBusinessEnum.FormOperateType.NEW.getKey().equals(operateType) || CAPBusinessEnum.FormOperateType.UPDATE.getKey().equals(operateType);
        CAPFormDataLogUtil.recordTrace(formDataMasterBean, "打开计算前");
        if (isEdit) {
            // 用户打开表单执行移除 用户数据请求锁
            capRuntimeDataLockManager.remove(formDataMasterBean.getId());
            if (ModuleType.collaboration == mType && formDataMasterBean.getState() == Enums.FormDataStateEnum.FLOW_DRAFT.getKey()) {
                // 流程待发打开，OA-172492 回退待发清空，从待发打开自动刷数据关联.
                // todo 常量WAIT_SEND_BY_BACK_OR_REPEAL命名不合理,430改掉。控制是否刷数据关联的开关
                CtpAffair ctpAffair = affairManager.getSenderAffair(Long.parseLong(moduleId));
                if (ctpAffair != null && ctpAffair.getState() == StateEnum.col_waitSend.getKey()) {
                    int subState = ctpAffair.getSubState() == null ? -1 : ctpAffair.getSubState().intValue();
                    if (subState == SubStateEnum.col_waitSend_stepBack.getKey() || subState == SubStateEnum.col_waitSend_cancel.getKey()) {
                        formDataMasterBean.putExtraAttr(FormConstant.WAIT_SEND_BY_BACK_OR_REPEAL, true);
                    }
                }
            }
            boolean isWaitSent = isWaitSentEdit(mType, operateType, contentDataId);
            // 可编辑态（无流程：新建、修改；有流程：新建、待办打开、待发编辑）需要通过节点权限给表单赋初始值，然后刷新关联和计算
            // 刷新关联前需要先把moduleId放入formDataMasterBean中，后面如果拷贝附件需要(有/无流程新建都是给的formMasterDataId)
            formDataMasterBean.putExtraAttr(FormConstant.moduleId, CAPBusinessEnum.FormOperateType.NEW.getKey().equals(operateType) ? formDataMasterBean.getId().toString() : moduleId);
            cap4FormManager.putSessioMasterDataBean(formBean, formDataMasterBean, true, false);
            AppContext.putThreadContext(FormConstant.WAIT_SENT_OPEN, isWaitSent);
            cap4FormDataManager.calcAll(formBean, formDataMasterBean, formAuthViewBean, false, false, true, true);
            AppContext.removeThreadContext(FormConstant.WAIT_SENT_OPEN);
            /* 处理自动汇总和自动新增明细行
             *  1:自动汇总是保存时刻生成数据
             *  2:自动新增明细行在打开时生成数据（勾选权限范围内）
             *  3:自动新增按钮受权限控制，自动汇总不受
             *  4:非浏览权限处理
             * */
            if (Strings.isNotBlank(formDataParamBean.getMappingDataKey())) {
                LOGGER.info(formDataParamBean.getCurrentUser().getId() + " dataId " + formDataMasterBean.getId() + " fill mapping data " + formDataParamBean.getMappingDataKey());
                // 处理自定义控件映射值
                capCustomService.fillMappingData(formBean, formDataMasterBean, Long.parseLong(String.valueOf(formDataMasterBean.getExtraAttr(FormConstant.moduleId))), formDataParamBean.getMappingDataKey());
                AppContext.putThreadContext(FormConstant.WAIT_SENT_OPEN, isWaitSent);
                cap4FormDataManager.calcAll(formBean, formDataMasterBean, formAuthViewBean, false, false, true, true);
                AppContext.removeThreadContext(FormConstant.WAIT_SENT_OPEN);
                capCustomService.removeMappingDataCache(formDataParamBean.getMappingDataKey());
            }
            List<FormRelationshipMapBean> autoIncreaseRowBeans = cap4FormRelationActionManager.getValidFormRelationshipMapBeansByType(formBean, FormRelationEnums.ViewSelectType.auto_increase_row.getKey());
            this.dealAutoIncreaseRow(autoIncreaseRowBeans, formBean, formDataMasterBean, formAuthViewBean, formDataParamBean.getRightId(), isWaitSent, extendData.getClearSubReferences());
            formDataMasterBean.removeExtraMap(FormConstant.WAIT_SEND_BY_BACK_OR_REPEAL);
            CAPFormDataLogUtil.recordTrace(formDataMasterBean, "打开计算后");
            /**zhou:[合并的代码] 解决发起表单附件出现两次的问题*/
            Set<String> clearSubReferences4Open = (Set) AppContext.getThreadContext("clearSubReferences4Open");
            if (CollectionUtils.isNotEmpty(clearSubReferences4Open)) {
                LOGGER.info(formDataMasterBean.getId() + " data open remove auto relation files " + clearSubReferences4Open);
                extendData.getClearSubReferences().addAll(clearSubReferences4Open);
            }
        }

        //构造签章数据(计算之后)
        if (mType == ModuleType.collaboration && contentDataId != null
                && !CAPBusinessEnum.FormOpenFrom.API_FORWARD.getKey().equals(from) && collaborationApi.hasCtpIsignature(contentAll.getModuleId())) {
            FormDataSignatureParamBean signatureParamBean = new FormDataSignatureParamBean();
            signatureParamBean.setModuleId(Long.valueOf(moduleId));
            signatureParamBean.setFormMasterDataId(contentDataId);
            signatureParamBean.setFormId(formBean.getId());
            signatureParamBean.setUserAgent(userAgent);
            // signatureParamBean.setFrom(from);
            FormDataResult<Map> iSignatureProtectedData = capFormDataSignatureService.getiSignatureProtectedData(signatureParamBean);
            if (iSignatureProtectedData.getData() != null) {
                content.setiSignatureProtectedData(iSignatureProtectedData.getData());
            }
        }
        // 高级权限模型
        Map<String, Object> auth = cap4FormDataManager.dealFormRightChangeResult(formBean, formAuthViewBean, formDataMasterBean, true);
        Map<String, FormAuthorizationTableBean> subTableAuthMap = (Map<String, FormAuthorizationTableBean>) auth.get("subTableAuthMap");
        content.setContentDataId(String.valueOf(formDataMasterBean.getId()));
        if (contentDataId == null) {
            content.setModuleId(String.valueOf(formDataMasterBean.getId()));
            // 新建，需要把当前的masterId放入权限缓存，关联穿透baseObjectId用的当前表单
            CAPFormUtil.addFormAccessControl(formDataMasterBean.getId(), formDataParamBean.getCurrentUser().getId());
        }
        // 准备关联信息,以首选字段作为key,关联对象为value 关联定义信息。转发的表单，不需要关联映射关系
        this.setCreateOrEditRelation(formDataMasterBean, formBean, extendData, formDataVO, contentDataId, from);
        //准备关联信息,以目标表穿透字段作为key,触发记录对象为value
        this.setCreateOrEditTriggerData(formDataMasterBean, contentDataId, extendData);
        // 附件
        this.setCreateOrEditAttachments(formDataMasterBean, extendData, formDataParamBean, contentDataId, mType);

        // 明细表记录是否超过阈值  前端控件都处理为浏览  并且明细表无法增删
        extendData.setThreshold(checkFormsonNum(from, formDataMasterBean));
        if (extendData.isThreshold()) {
            content.setFormsonNumThreshold(String.valueOf(PHONE_FORMSON_NUM_THRESHOLD));
        }
        LOGGER.info("createOrEdit content " + content);
        extendData.setAutoIncConditionFields(cap4FormRelationActionManager.getValidAutoIncreaseConditionFields(formBean));

        FormDataVO.TableInfo tableInfo = formDataVO.new TableInfo();
        formDataVO.setTableInfo(tableInfo);
        // 准备字段所属表映射
        extendData.setFieldBeanMap(CAPFormUtil.getTableName2FieldBeans(formBean, formViewBean));
        // 主表
        Map<String, Object> fieldInfos;
        if (extendData.getFieldBeanMap().get(formBean.getMasterTableBean().getTableName()) == null) {
            LOGGER.info("createOrEditForm, this view not field! formViewBean id = " + formViewBean.getId() + ", formViewBean info = " + formViewBean.getFormViewContent());
            fieldInfos = new HashMap<String, Object>();
        } else {
            fieldInfos = this.buildCreateOrEditMasterFields(formDataMasterBean, formAuthViewBean, extendData);
        }
        FormTableFormmainDataVO formmain = new FormTableFormmainDataVO();
        this.createResultTableData(formmain, formBean.getMasterTableBean(), fieldInfos);
        tableInfo.setFormmain(formmain);

        // 重复表
        List<FormTableFormsonDataVO> formsons = new ArrayList<FormTableFormsonDataVO>();
        tableInfo.setFormson(formsons);
        for (FormTableBean subTable : formBean.getSubTableBean()) {
            Map<String, Object> subFieldInfos = this.buildCreateOrEditFormSonFields(formDataMasterBean, subTable, extendData);
            List<FormDataSubBean> dataSubBeans = formDataMasterBean.getSubData(subTable.getTableName());
            LOGGER.info(formDataMasterBean.getId() + " 打开表单 " + subTable.getTableName() + " " + dataSubBeans.size() + "行");
            if (extendData.isThreshold() && dataSubBeans.size() > PHONE_FORMSON_NUM_THRESHOLD) {
                dataSubBeans = dataSubBeans.subList(0, PHONE_FORMSON_NUM_THRESHOLD);
            }
            FormTableFormsonDataVO formson = new FormTableFormsonDataVO();
            this.createResultTableData(formson, subTable, subFieldInfos);
            List dataSubLineInfos = this.buildCreateOrEditFormSonData(formDataMasterBean, dataSubBeans, subTable, formAuthViewBean, subFieldInfos, extendData);
            formson.setDataSubLineInfos(dataSubLineInfos);
            this.buildCreateOrEditFormSonAuth(formson, subTableAuthMap, formBean, formAuthViewBean, subTable, extendData);
            formsons.add(formson);
        }
        this.createResultUser(formDataVO, formDataParamBean.getCurrentUser());
        this.createExtend(formBean, formDataMasterBean, formAuthViewBean, extendData, formDataVO, formDataParamBean);
        if (isEdit) {
            cap4FormManager.saveSessioMasterDataBean(formDataMasterBean.getId(), formDataMasterBean);
        }
        JMTrackUtils.clearTrack();
        return formDataVO;
    }

    /**
     * 关联信息
     *
     * @param formDataMasterBean
     * @param formBean
     * @param extendData
     * @param formDataVO
     * @param contentDataId
     * @param from
     */
    private void setCreateOrEditRelation(FormDataMasterBean formDataMasterBean, FormBean formBean,
                                         CreateOrEditExtendData extendData, FormDataVO formDataVO, Long contentDataId, String from) throws BusinessException {
        // 准备关联信息,以首选字段作为key,关联对象为value 关联定义信息。转发的表单，不需要关联映射关系
        extendData.setManualFormRelationshipMapBeanMap(CAPBusinessEnum.FormOpenFrom.API_FORWARD.getKey().equalsIgnoreCase(from) ? new HashMap<String, List<FormRelationshipMapBean>>() : cap4FormRelationActionManager.getFormField4ManualRelationMapBeans(formBean));
        if (formDataMasterBean.getRelationRecords() != null) {
            extendData.setCapFormRelationRecords(formDataMasterBean.getRelationRecords());
        } else if (contentDataId != null && extendData.getManualFormRelationshipMapBeanMap().size() > 0) {
            List<CAPFormRelationRecord> relationRecords = cap4FormRelationActionManager.getCAPFormRelationRecordsByMasterDataId(contentDataId);
            cap4FormRelationActionManager.upgradeCAPFormRelationRecords(formBean, relationRecords, true);
            extendData.setCapFormRelationRecords(relationRecords);
            formDataMasterBean.setRelationRecords(relationRecords);
        }
        List<Map<String, String>> relationRecordArray = new ArrayList<Map<String, String>>();
        for (CAPFormRelationRecord capFormRelationRecord : extendData.getCapFormRelationRecords()) {
            relationRecordArray.add(capFormRelationRecord.exportToMap());
        }
        formDataVO.setRelationRecords(relationRecordArray);
        // 将关联放缓存中，方便计算使用
        formDataMasterBean.initRelationRecordMap();
        extendData.setCapFormRelationRecordMap(formDataMasterBean.getRelationRecordMap());
    }

    /**
     * 打开表单附件信息
     *
     * @param formDataMasterBean
     * @param extendData
     * @param formDataParamBean
     * @param contentDataId
     * @param mType
     * @throws BusinessException
     */
    private void setCreateOrEditAttachments(FormDataMasterBean formDataMasterBean, CreateOrEditExtendData extendData,
                                            FormDataParamBean formDataParamBean, Long contentDataId, ModuleType mType) throws BusinessException {
        // 准备附件信息,以subReference作为key,对应字段附件数据
        // 新建无流程/有流程的reference都使用masterId处理
        List<Attachment> attachments = formDataMasterBean.getSessionAttachments(null);
        if (CAPBusinessEnum.FormOpenFrom.API_FORWARD.getKey().equals(formDataParamBean.getFrom()) && formDataParamBean.getRebuildAttachments() != null && formDataParamBean.getNewSummaryId() != null) {
            extendData.setCapFormAttachmentsMap(formDataParamBean.getRebuildAttachments());
        } else {
            if (extendData.getQueryDbAttachments() &&
                    (isWaitSentEdit(mType, extendData.getOperateType(), contentDataId) || !CAPBusinessEnum.FormOperateType.NEW.getKey().equals(extendData.getOperateType()))) {
                Long reference = formDataParamBean.getModuleId();
                Long realReference = this.getAttachmentRealReference(mType, reference);
                List<Attachment> dbAttachments = attachmentManager.getByReference(realReference);
                if (Strings.isNotEmpty(dbAttachments)) {
                    // 自动新增明细行后，之前明细表数据被清空
                    Iterator<Attachment> iterator = dbAttachments.iterator();
                    while (iterator.hasNext()) {
                        Attachment attachment = iterator.next();
                        if (extendData.getClearSubReferences().contains(String.valueOf(attachment.getSubReference()))) {
                            iterator.remove();
                        }
                    }
                }
                if (!CAPBusinessEnum.FormOperateType.BROWSE.getKey().equals(extendData.getOperateType())) {
                    Map<String, List<Attachment>> dbReferenceMap = CAPAttachmentUtil.buildAttachmentSubReferenceToList(dbAttachments);
                    CAPAttachmentUtil.putDbAttachmentsToSession(formDataMasterBean, dbReferenceMap);
                }
                attachments = CAPAttachmentUtil.mergeAttachments(attachments, dbAttachments);
            }
            if (Strings.isNotEmpty(attachments)) {
                Collections.sort(attachments, new Comparator<Attachment>() {
                    @Override
                    public int compare(Attachment o1, Attachment o2) {
                        return o1.getSort() - o2.getSort();
                    }
                });
            }
            extendData.setCapFormAttachmentsMap(CAPAttachmentUtil.buildAttachmentSubReferenceToListMap(attachments, true));
        }
        if (CAPBusinessEnum.FormOpenFrom.API_FORWARD.getKey().equals(formDataParamBean.getFrom()) && formDataParamBean.getRebuildAttachments() == null && formDataParamBean.getNewSummaryId() != null) {
            // 如果是转发表单，并且新的summaryId不为空，复制attachments，变更attachment的reference和subreference
            CAPAttachmentUtil.copyApiForwardFormAttachments(attachments, formDataParamBean.getNewSummaryId(), extendData.getCapFormAttachmentsMap());
            formDataParamBean.setRebuildAttachments(extendData.getCapFormAttachmentsMap());
        }
    }

    /**
     * 打开表单触发信息
     *
     * @param formDataMasterBean
     * @param contentDataId
     * @param extendData
     * @throws BusinessException
     */
    private void setCreateOrEditTriggerData(FormDataMasterBean formDataMasterBean, Long contentDataId, CreateOrEditExtendData extendData) throws BusinessException {
        if (contentDataId != null) {
            List<CAPFormTriggerSourceRecord> capFormTriggerSourceRecords = cap4FormTriggerSourceRecordDAO.getSourceRecordList(contentDataId);
            for (CAPFormTriggerSourceRecord capFormTriggerSourceRecord : capFormTriggerSourceRecords) {
                String key = capFormTriggerSourceRecord.getFieldName();
                if (capFormTriggerSourceRecord.getTargetSubDataId() != 0L) {
                    key += FormConstant.DOWNLINE + capFormTriggerSourceRecord.getTargetSubDataId();
                }
                extendData.getCapFormTriggerSourceRecordMap().put(key, capFormTriggerSourceRecord);
            }
        }
        // 将触发放缓存中，方便计算使用
        formDataMasterBean.getExtraMap().put("capFormTriggerSourceRecordMap", extendData.getCapFormTriggerSourceRecordMap());
    }

    /**
     * 获取表单打开的masterBean
     *
     * @param formDataMasterBean
     * @param formBean
     * @param formAuthViewBean
     * @param contentAll
     * @param extendData
     * @param formDataParamBean
     * @return
     * @throws BusinessException
     */
    private FormDataMasterBean getCreateOrEditFormDataMasterBean(FormDataMasterBean formDataMasterBean, FormBean formBean,
                                                                 FormAuthViewBean formAuthViewBean, CtpContentAll contentAll,
                                                                 CreateOrEditExtendData extendData, FormDataParamBean formDataParamBean) throws BusinessException {
        if (formDataMasterBean == null) {
            Long contentDataId = contentAll.getContentDataId();
            if (contentDataId == null) {
                if (formDataParamBean.getFormMasterId() != null) {
                    // 保存并复制进入
                    formDataMasterBean = cap4FormManager.getSessioMasterDataBean(formDataParamBean.getFormMasterId());
                } else {
                    Map<String, Map<String, Boolean>> changeTag = new HashMap<String, Map<String, Boolean>>();
                    AppContext.putThreadContext(FormConstant.fieldChangeTag, changeTag);
                    formDataMasterBean = FormDataMasterBean.newInstance(formBean);
                    //SP2中修改saveOrUpdateForm的判断逻辑，将以前已ctpContentAll.getModuleTemplateId() == -1L改为此前的标识判断
                    formDataMasterBean.putExtraAttr("isNew", true);
                }
            } else {
                if (Strings.isNotBlank(contentAll.getContent())) {
                    if (formDataParamBean.getFormMasterId() == null) {
                        // 使用个人模板进入,个人模板不会存储附件
                        formDataMasterBean = capTransFormDataBeanManager.transFormDataBeanFromFormBean(formBean, contentAll.getContent(), formAuthViewBean);
                        this.clearSelfTemplateData(formBean, formDataMasterBean, formAuthViewBean.getId());
                        formDataMasterBean.putExtraAttr("isNew", true);
                        formDataMasterBean.putExtraAttr(FormConstant.WAIT_SEND_BY_BACK_OR_REPEAL, true);
                    } else {
                        // 个人模版的轻原表单切换
                        formDataMasterBean = cap4FormManager.getSessioMasterDataBean(formDataParamBean.getFormMasterId());
                    }
                } else {
                    formDataMasterBean = CAPBusinessEnum.FormOperateType.BROWSE.getKey().equals(extendData.getOperateType()) ? null : cap4FormManager.getSessioMasterDataBean(contentDataId);
                    if (formDataParamBean.isPrintFlag() && formDataMasterBean == null) {
                        // 如果是打印，operateType传递的是浏览，formDataMasterBean = null，从session中再取一次
                        formDataMasterBean = cap4FormManager.getSessioMasterDataBean(contentDataId);
                    }
                    if (Boolean.TRUE.equals(formDataParamBean.isForceDb())) {
                        // 360浏览器兼容模式无法移除缓存
                        formDataMasterBean = null;
                    }
                    if (formDataMasterBean == null) {
                        formDataMasterBean = capFormDataCacheManager.get4Data(contentDataId, formBean.getId());
                        extendData.setQueryDbAttachments(true);
                    }
                    if (formDataMasterBean == null) {
                        throw new FormDataBusinessException(CODE11, ResourceUtil.getString(MESSAGE11));
                    }
                }
                List<FormSerialCalculateRecord> serialRecordList = serialCalRecordManager.selectAllByFormData(formBean.getId(), formDataMasterBean.getId());
                formDataMasterBean.putExtraAttr(FormConstant.serialCalRecords, serialRecordList);
            }
        }
        return formDataMasterBean;
    }

    /**
     * 组建明细表数据
     *
     * @param formDataMasterBean
     * @param dataSubBeans
     * @param subTable
     * @param formAuthViewBean
     * @param subFieldInfos
     * @param extendData
     * @return
     * @throws BusinessException
     */
    private List buildCreateOrEditFormSonData(FormDataMasterBean formDataMasterBean, List<FormDataSubBean> dataSubBeans, FormTableBean subTable,
                                              FormAuthViewBean formAuthViewBean, Map<String, Object> subFieldInfos, CreateOrEditExtendData extendData) throws BusinessException {
        List dataSubLineInfos = new ArrayList();
        List<FormFieldBean> subFields = extendData.getFieldBeanMap().get(subTable.getTableName());
        for (FormDataSubBean formDataSubBean : dataSubBeans) {
            Map<String, Object> dataSubInfos = new HashMap<String, Object>();
            dataSubLineInfos.add(dataSubInfos);
            dataSubInfos.put(RECORD_ID, String.valueOf(formDataSubBean.getId()));
            if (subFields != null) {
                for (FormFieldBean formFieldBean : subFields) {
                    String authKey = formFieldBean.getName() + FormConstant.DOWNLINE + String.valueOf(formDataSubBean.getId());
                    FormAuthViewFieldBean formAuthViewFieldBean = CAPFormUtil.getFormAuthViewFieldBean(formAuthViewBean, formFieldBean, formDataMasterBean, String.valueOf(formDataSubBean.getId()));
                    List<Map<String, Object>> enums = null;
                    // 明细表多级枚举 需要给每个字段都设置enums，其它枚举（单选、图片等）都在field里面
                    if (FormFieldComEnum.SELECT.getKey().equalsIgnoreCase(formFieldBean.getInputType())) {
                        enums = CAPFormUtil.getFieldEnumJSON(formFieldBean, formDataMasterBean, formDataSubBean, false);
                    }
                    Object fieldValue = formDataSubBean.getFieldValue(formFieldBean.getName());
                    // BUG OA-156639 回退后父级隐藏权限清空，二级枚举也要清空值
                    if (FormFieldComEnum.SELECT.getKey().equalsIgnoreCase(formFieldBean.getInputType()) && fieldValue != null && Strings.isEmpty(enums)) {
                        fieldValue = null;
                        formDataSubBean.addFieldValue(formFieldBean.getName(), fieldValue);
                    }
                    //数据相关信息
                    Map<String, Object> dataInfo = CAPFormUtil.getDisplayValueMap(fieldValue, formFieldBean, formAuthViewFieldBean);
                    //附件,图片相关信息
                    if (formFieldBean.isAttachment()) {
                        dataInfo.put(ATTACHMENT_INFO, CAPAttachmentUtil.getFieldAttachmentMap(formDataMasterBean.getId(), formFieldBean, fieldValue, extendData.getCapFormAttachmentsMap()));
                    }
                    // 明细表多级枚举 需要给每个字段都设置enums，其它枚举（单选、图片等）都在field里面
                    if (FormFieldComEnum.SELECT.getKey().equalsIgnoreCase(formFieldBean.getInputType())) {
                        dataInfo.put(ENUMS, enums);
                    }
                    //权限相关信息
                    List<FormRelationshipMapBean> formRelationshipMapBeans = extendData.getManualFormRelationshipMapBeanMap().get(formFieldBean.getName());
                    Map<String, String> authMap = CAPFormUtil.getAuthMap(formFieldBean, formAuthViewFieldBean, extendData.getOperateType(), extendData.isThreshold());
                    dataSubInfos.put(formFieldBean.getName(), dataInfo);
                    //关联相关信息
                    if (CollectionUtils.isNotEmpty(formRelationshipMapBeans)) {
                        Map<String, String> relationData = CAPFormUtil.getRelationData(formRelationshipMapBeans, extendData.getCapFormRelationRecordMap(), formDataSubBean, formFieldBean.getName(), authMap);
                        dataInfo.put(RELATION_DATA, relationData);
                    }
                    //触发相关信息
                    if (extendData.getCapFormTriggerSourceRecordMap().containsKey(authKey)) {
                        Map<String, String> triggerData = CAPFormUtil.getTriggerDataJSON(extendData.getCapFormTriggerSourceRecordMap(), formFieldBean, authMap, String.valueOf(formDataSubBean.getId()));
                        dataInfo.put(TRIGGER_DATA, triggerData);
                    }
                    dataInfo.putAll(authMap);
                }
            }
        }
        return dataSubLineInfos;
    }

    /**
     * 构建明细表权限
     *
     * @param formson
     * @param subTableAuthMap
     * @param formBean
     * @param formAuthViewBean
     * @param subTable
     * @param extendData
     */
    private void buildCreateOrEditFormSonAuth(FormTableFormsonDataVO formson, Map<String, FormAuthorizationTableBean> subTableAuthMap,
                                              FormBean formBean, FormAuthViewBean formAuthViewBean,
                                              FormTableBean subTable, CreateOrEditExtendData extendData) {
        FormAuthorizationTableBean authTableBean = subTableAuthMap.get(subTable.getTableName()) == null ? formAuthViewBean.getSubTableAuth(subTable.getDisplay()) : subTableAuthMap.get(subTable.getTableName());
        // 2019330 新的明细表button结构
        List<Map<String, Object>> tableButtons;
        Map<String, String> tableAuth;
        if (CAPBusinessEnum.FormOperateType.BROWSE.getKey().equals(extendData.getOperateType()) || extendData.isThreshold()) {
            // 浏览态 或者 移动端明细行 超过阀值后没有任何按钮
            tableButtons = new ArrayList<Map<String, Object>>();
            tableAuth = new HashMap<String, String>();
        } else {
            tableButtons = capRuntimeCalcManager.getFormSonAuthButtons(formBean, formAuthViewBean, subTable, authTableBean, false);
            tableAuth = capRuntimeCalcManager.getFormSonAuth(authTableBean, false);
        }
        formson.setTableButton(tableButtons);
        formson.setTableAuth(tableAuth);
    }

    /**
     * 构建明细表字段信息
     *
     * @param formDataMasterBean
     * @param subTable
     * @param extendData
     * @return
     * @throws BusinessException
     */
    private Map<String, Object> buildCreateOrEditFormSonFields(FormDataMasterBean formDataMasterBean, FormTableBean subTable, CreateOrEditExtendData extendData) throws BusinessException {
        Map<String, Object> fieldMap = new HashMap<String, Object>();
        List<FormFieldBean> subFields = extendData.getFieldBeanMap().get(subTable.getTableName());
        if (subFields == null) {
            return fieldMap;
        }
        for (FormFieldBean formFieldBean : subFields) {
            //字段相关信息
            Map<String, Object> subFieldInfo = CAPFormUtil.getFormFieldInfo(formFieldBean, formDataMasterBean);
            CAPFormUtil.resetFormFieldInfo(subFieldInfo, extendData.getOperateType(), extendData.getAutoIncConditionFields());
            //若是首选字段关联信息
            List<FormRelationshipMapBean> formRelationshipMapBeans = extendData.getManualFormRelationshipMapBeanMap().get(formFieldBean.getName());
            if (CollectionUtils.isNotEmpty(formRelationshipMapBeans)) {
                subFieldInfo.put(RELATION_INFO, CAPFormUtil.getRelationInfoMaps(formRelationshipMapBeans));
            }
            fieldMap.put(formFieldBean.getName(), subFieldInfo);
        }
        return fieldMap;
    }

    /**
     * 构建主表字段信息
     *
     * @param formDataMasterBean
     * @param formAuthViewBean
     * @param extendData
     * @return
     * @throws BusinessException
     */
    private Map<String, Object> buildCreateOrEditMasterFields(FormDataMasterBean formDataMasterBean, FormAuthViewBean formAuthViewBean, CreateOrEditExtendData extendData) throws BusinessException {
        Map<String, Object> fieldMap = new HashMap<String, Object>();
        List<FormFieldBean> masterFields = extendData.getFieldBeanMap().get(formDataMasterBean.getFormTable().getTableName());
        for (FormFieldBean formFieldBean : masterFields) {
            FormAuthViewFieldBean formAuthViewFieldBean = CAPFormUtil.getFormAuthViewFieldBean(formAuthViewBean, formFieldBean, formDataMasterBean, null);
            //权限相关信息
            Map<String, String> authMap = CAPFormUtil.getAuthMap(formFieldBean, formAuthViewFieldBean, extendData.getOperateType(), extendData.isThreshold());
            //字段相关信息
            Map<String, Object> fieldInfo = CAPFormUtil.getFormFieldInfo(formFieldBean, formDataMasterBean);
            Object fieldValue = formDataMasterBean.getFieldValue(formFieldBean.getName());
            // BUG OA-156639 回退后父级隐藏权限清空，二级枚举也要清空值
            if (FormFieldComEnum.SELECT.getKey().equalsIgnoreCase(formFieldBean.getInputType()) && fieldValue != null && fieldInfo.get("enums") != null && Strings.isEmpty((List) fieldInfo.get("enums"))) {
                fieldValue = null;
                formDataMasterBean.addFieldValue(formFieldBean.getName(), fieldValue);
            }
            CAPFormUtil.resetFormFieldInfo(fieldInfo, extendData.getOperateType(), extendData.getAutoIncConditionFields());
            //数据相关信息
            Map<String, Object> dataInfo = CAPFormUtil.getDisplayValueMap(fieldValue, formFieldBean, formAuthViewFieldBean);
            //关联相关信息
            //若是首选字段关联信息
            List<FormRelationshipMapBean> formRelationshipMapBeans = extendData.getManualFormRelationshipMapBeanMap().get(formFieldBean.getName());
            if (CollectionUtils.isNotEmpty(formRelationshipMapBeans)) {
                //关联数据随数据不同而不同
                Map<String, String> relationData = CAPFormUtil.getRelationData(formRelationshipMapBeans, extendData.getCapFormRelationRecordMap(), formDataMasterBean, formFieldBean.getName(), authMap);
                fieldInfo.put(RELATION_INFO, CAPFormUtil.getRelationInfoMaps(formRelationshipMapBeans));
                dataInfo.put(RELATION_DATA, relationData);
            }
            //触发相关信息
            if (extendData.getCapFormTriggerSourceRecordMap().containsKey(formFieldBean.getName())) {
                Map<String, String> triggerData = CAPFormUtil.getTriggerDataJSON(extendData.getCapFormTriggerSourceRecordMap(), formFieldBean, authMap, null);
                dataInfo.put(TRIGGER_DATA, triggerData);
            }
            //附件,图片相关信息
            if (formFieldBean.isAttachment()) {
                fieldInfo.put(ATTACHMENT_INFO, CAPAttachmentUtil.getFieldAttachmentMap(formDataMasterBean.getId(), formFieldBean, fieldValue, extendData.getCapFormAttachmentsMap()));
            }
            dataInfo.putAll(authMap);
            fieldInfo.putAll(dataInfo);
            fieldMap.put(formFieldBean.getName(), fieldInfo);
        }
        return fieldMap;
    }

    private class CreateOrEditExtendData {
        private Map<String, CAPFormTriggerSourceRecord> capFormTriggerSourceRecordMap = new HashMap<String, CAPFormTriggerSourceRecord>();
        private Map<String, List<FormRelationshipMapBean>> manualFormRelationshipMapBeanMap = new HashMap<String, List<FormRelationshipMapBean>>();
        /**
         * 以首选字段作为key,关联记录为value  当编辑情况下且含有关联定义再执行
         */
        private Map<String, CAPFormRelationRecord> capFormRelationRecordMap = new HashMap<String, CAPFormRelationRecord>();
        private Map<String, List<Map<String, String>>> capFormAttachmentsMap = new HashMap<String, List<Map<String, String>>>();
        private Map<String, List<FormFieldBean>> fieldBeanMap = new HashMap<String, List<FormFieldBean>>();
        private List<CAPFormRelationRecord> capFormRelationRecords = new ArrayList<CAPFormRelationRecord>();
        /**
         * 打开明细表，被新增明细行等操作清除掉的附件信息，需要移除
         */
        private Set<String> clearSubReferences = new HashSet<String>();
        private Set<String> autoIncConditionFields;
        private String operateType;
        private boolean isThreshold;
        private boolean queryDbAttachments = false;

        public Map<String, CAPFormTriggerSourceRecord> getCapFormTriggerSourceRecordMap() {
            return capFormTriggerSourceRecordMap;
        }

        public void setCapFormTriggerSourceRecordMap(Map<String, CAPFormTriggerSourceRecord> capFormTriggerSourceRecordMap) {
            this.capFormTriggerSourceRecordMap = capFormTriggerSourceRecordMap;
        }

        public Map<String, List<FormRelationshipMapBean>> getManualFormRelationshipMapBeanMap() {
            return manualFormRelationshipMapBeanMap;
        }

        public void setManualFormRelationshipMapBeanMap(Map<String, List<FormRelationshipMapBean>> manualFormRelationshipMapBeanMap) {
            this.manualFormRelationshipMapBeanMap = manualFormRelationshipMapBeanMap;
        }

        public Map<String, CAPFormRelationRecord> getCapFormRelationRecordMap() {
            return capFormRelationRecordMap;
        }

        public void setCapFormRelationRecordMap(Map<String, CAPFormRelationRecord> capFormRelationRecordMap) {
            this.capFormRelationRecordMap = capFormRelationRecordMap;
        }

        public Map<String, List<Map<String, String>>> getCapFormAttachmentsMap() {
            return capFormAttachmentsMap;
        }

        public void setCapFormAttachmentsMap(Map<String, List<Map<String, String>>> capFormAttachmentsMap) {
            this.capFormAttachmentsMap = capFormAttachmentsMap;
        }

        public Map<String, List<FormFieldBean>> getFieldBeanMap() {
            return fieldBeanMap;
        }

        public void setFieldBeanMap(Map<String, List<FormFieldBean>> fieldBeanMap) {
            this.fieldBeanMap = fieldBeanMap;
        }

        public List<CAPFormRelationRecord> getCapFormRelationRecords() {
            return capFormRelationRecords;
        }

        public void setCapFormRelationRecords(List<CAPFormRelationRecord> capFormRelationRecords) {
            this.capFormRelationRecords = capFormRelationRecords;
        }

        public Set<String> getAutoIncConditionFields() {
            return autoIncConditionFields;
        }

        public void setAutoIncConditionFields(Set<String> autoIncConditionFields) {
            this.autoIncConditionFields = autoIncConditionFields;
        }

        public String getOperateType() {
            return operateType;
        }

        public void setOperateType(String operateType) {
            this.operateType = operateType;
        }

        public boolean isThreshold() {
            return isThreshold;
        }

        public void setThreshold(boolean threshold) {
            isThreshold = threshold;
        }

        public boolean getQueryDbAttachments() {
            return queryDbAttachments;
        }

        public void setQueryDbAttachments(boolean queryDbAttachments) {
            this.queryDbAttachments = queryDbAttachments;
        }

        public Set<String> getClearSubReferences() {
            return clearSubReferences;
        }

        public void setClearSubReferences(Set<String> clearSubReferences) {
            this.clearSubReferences = clearSubReferences;
        }
    }

    /**
     * 构造返回结构的user
     *
     * @param formDataVO
     */
    private void createResultUser(FormDataVO formDataVO, User currentUser) {
        FormDataVO.User user = formDataVO.new User();
        user.setId(String.valueOf(currentUser.getId()));
        user.setName(currentUser.getName());
        user.setLoginName(currentUser.getLoginName());
        formDataVO.setUser(user);
    }

    private void dealAutoIncreaseRow(List<FormRelationshipMapBean> autoIncreaseRowBeans, FormBean formBean, FormDataMasterBean formDataMasterBean,
                                     FormAuthViewBean formAuthViewBean, String rightId, boolean isWaitSentEdit, Set<String> clearSubReferences) throws BusinessException {
        Map<String, Set<String>> tableName2DeleteIds = new HashMap<String, Set<String>>();
        Iterator<FormRelationshipMapBean> it = autoIncreaseRowBeans.iterator();
        boolean flag = false;
        Map<String, List<FormDataSubBean>> tableName2AddSubBeans = new HashMap<String, List<FormDataSubBean>>();
        while (it.hasNext()) {
            FormRelationshipMapBean autoIncreaseRowBean = it.next();
            if (autoIncreaseRowBean.getRefreshOperationUpdate() != null && autoIncreaseRowBean.getRefreshOperationUpdate().contains(rightId)) {
                flag = true;
                tableName2AddSubBeans.putAll(cap4FormRelationActionManager.getRelationAutoIncreaseRow(formBean, formDataMasterBean, formAuthViewBean, autoIncreaseRowBean, tableName2DeleteIds, clearSubReferences));
            } else if (autoIncreaseRowBean.getRefreshOperationAdd() != null && autoIncreaseRowBean.getRefreshOperationAdd().contains(rightId)) {
                if (isWaitSentEdit) {
                    if (autoIncreaseRowBean.getWaitSentRefresh() == 1) {
                        flag = true;
                        tableName2AddSubBeans.putAll(cap4FormRelationActionManager.getRelationAutoIncreaseRow(formBean, formDataMasterBean, formAuthViewBean, autoIncreaseRowBean, tableName2DeleteIds, clearSubReferences));
                    }
                } else {
                    flag = true;
                    tableName2AddSubBeans.putAll(cap4FormRelationActionManager.getRelationAutoIncreaseRow(formBean, formDataMasterBean, formAuthViewBean, autoIncreaseRowBean, tableName2DeleteIds, clearSubReferences));
                }
            } else if (autoIncreaseRowBean.getWaitSentRefresh() == 1 && isWaitSentEdit) {
                flag = true;
                tableName2AddSubBeans.putAll(cap4FormRelationActionManager.getRelationAutoIncreaseRow(formBean, formDataMasterBean, formAuthViewBean, autoIncreaseRowBean, tableName2DeleteIds, clearSubReferences));
            } else {
                it.remove();
            }
        }
        if (flag) {
            try {
                AppContext.putThreadContext(FormConstant.WAIT_SENT_OPEN, isWaitSentEdit);
                AppContext.putThreadContext(FormConstant.fieldChangeTag, this.getCalcAllFieldChanges(formBean, null, CAPFormUtil.getTableName2FormSubDataBeanIds(tableName2AddSubBeans)));
                cap4FormDataManager.calcAll(formBean, formDataMasterBean, formAuthViewBean, false, false, true, false);
            } finally {
                AppContext.removeThreadContext(FormConstant.fieldChangeTag);
                AppContext.removeThreadContext(FormConstant.WAIT_SENT_OPEN);
            }
        }
    }

    public Map<String, Map<String, Boolean>> getCalcAllFieldChanges(FormBean formBean, Set<String> changeFields, Map<String, List<Long>> tableName2AddSubIds) {
        // 主表结构
        FormTableBean masterTableBean = formBean.getMasterTableBean();
        Map<String, Map<String, Boolean>> fieldChangeTags = new HashMap<String, Map<String, Boolean>>();
        Map<String, Boolean> masterChangeMap = new HashMap<String, Boolean>();
        if (changeFields != null) {
            for (String str : changeFields) {
                if (str.indexOf(FormConstant.DOWNLINE) > 0) {
                    String fieldName = str.split(FormConstant.DOWNLINE)[0];
                    FormFieldBean formFieldBean = formBean.getFieldBeanByName(fieldName);
                    Map<String, Boolean> tableChangeMap = fieldChangeTags.get(formFieldBean.getOwnerTableName());
                    if (tableChangeMap == null) {
                        tableChangeMap = new HashMap<String, Boolean>();
                        fieldChangeTags.put(formFieldBean.getOwnerTableName(), tableChangeMap);
                    }
                    tableChangeMap.put(str, true);
                } else {
                    masterChangeMap.put(str, true);
                }
            }
        }
        if (!masterChangeMap.isEmpty()) {
            fieldChangeTags.put(masterTableBean.getTableName(), masterChangeMap);
        }
        if (tableName2AddSubIds != null && !tableName2AddSubIds.isEmpty()) {
            Iterator<String> iterator = tableName2AddSubIds.keySet().iterator();
            while (iterator.hasNext()) {
                String tableName = iterator.next();
                Map<String, Boolean> tableChangeMap = fieldChangeTags.get(tableName);
                if (tableChangeMap == null) {
                    tableChangeMap = new HashMap<String, Boolean>();
                }
                FormTableBean subTableBean = formBean.getTableByTableName(tableName);
                List<Long> addIds = tableName2AddSubIds.get(tableName);
                for (FormFieldBean subField : subTableBean.getFields()) {
                    /*if (CAPFormUtil.isCalculateField(subField)) {
                        for (Long addId : addIds) {
                            tableChangeMap.put(subField.getName() + FormConstant.DOWNLINE + addId, true);
                        }
                    }*/
                }
                if (!tableChangeMap.isEmpty()) {
                    fieldChangeTags.put(tableName, tableChangeMap);
                }
            }
        }
        return fieldChangeTags;
    }

    private boolean checkFormsonNum(String from, FormDataMasterBean formDataMasterBean) {
        if (CAPBusinessEnum.FormOpenFrom.ORIGINAL_FORM.getKey().equals(from) || CAPBusinessEnum.FormOpenFrom.LIGHT_FORM.getKey().equals(from)) {
            Map<String, List<FormDataSubBean>> dataSubBeanMap = formDataMasterBean.getSubTables();
            Iterator<String> it = dataSubBeanMap.keySet().iterator();
            int formsonTotal = 0;
            while (it.hasNext()) {
                List<FormDataSubBean> formDataSubBeans = dataSubBeanMap.get(it.next());
                formsonTotal += formDataSubBeans.size();
            }
            if (formsonTotal > PHONE_FORMSON_TOTAL_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    /**
     * 处理打开表单的权限以及根据rightId得到的视图信息
     * 返回信息：params中增加：views、userAgent、pcViewCount、phoneViewCount
     */
    private void dealRightIdAndViews(FormDataParamBean formDataParamBean, Long formId) {
        int userAgent = this.getUserAgent(formDataParamBean);
        String rightId = formDataParamBean.getRightId();
        String currentRightId = formDataParamBean.getCurrentRightId();
        if (CAPBusinessEnum.FormOpenFrom.TRIGGER_MESSAGE.getKey().equals(formDataParamBean.getFrom())) {
            if (CAPFormUtil.isPcLogin(userAgent)) {
                rightId = rightId.split(",")[0];
            } else {
                rightId = rightId.split(",")[1];
            }
        }
        Set<String> pcRightIdSet = new HashSet<String>();
        Set<String> phoneRightIdSet = new HashSet<String>();

        List<Map<String, Object>> views = new ArrayList<Map<String, Object>>();
        if (Strings.isNotBlank(rightId)) {
            FormBean formBean = cap4FormCacheManager.getForm(formId);
            // 适配这种格式(视图id.权限id_视图id.权限id_视图id.权限id)
            if (rightId.indexOf("_") > -1 || rightId.indexOf(".") > -1) {
                String[] viewStrs = rightId.split("_");
                for (int i = 0; i < viewStrs.length; i++) {
                    String rightIdStr = viewStrs[i].indexOf(".") > -1 ? viewStrs[i].split("\\.")[1].split("[|]")[0] : viewStrs[i].split("[|]")[0];
                    FormAuthViewBean formAuthViewBean = formBean.getAuthViewBeanById(Long.parseLong(rightIdStr));
                    if (formAuthViewBean == null) {
                        continue;
                    }
                    // 此处不需要多语言视图，只需要获取名称
                    FormViewBean formViewBean = formBean.getFormView(formAuthViewBean.getFormViewId());
                    if ((CAPFormUtil.isPhoneLogin(userAgent) && Enums.ViewType.Phone == formViewBean.getFormViewTypeEnum())
                            || (CAPFormUtil.isPcLogin(userAgent) && Enums.ViewType.SeeyonForm == formViewBean.getFormViewTypeEnum())) {
                        Map<String, Object> view = new HashMap<String, Object>();
                        view.put(RIGHT_ID, rightIdStr);
                        view.put(NAME, formViewBean.getFormViewName());
                        views.add(view);
                        // 循环rightId时不论pc还是phone，currentRightId取到第一组权限赋值
                        if (Strings.isBlank(currentRightId)) {
                            currentRightId = rightIdStr;
                        }
                    }
                    if (formViewBean.isPc()) {
                        pcRightIdSet.add(rightIdStr);
                    }
                    if (formViewBean.isPhone()) {
                        phoneRightIdSet.add(rightIdStr);
                    }
                }
            }
            if (Strings.isNotBlank(currentRightId)) {
                rightId = currentRightId;
            } else {
                // 如果权限被删除，那么会走到这个逻辑。
                rightId = rightId.contains("_") ? rightId.split("_")[0] : rightId;
                rightId = rightId.contains(".") ? rightId.split("[.]")[1] : rightId;
                rightId = rightId.contains("|") ? rightId.split("[|]")[0] : rightId;
            }
        }
        formDataParamBean.setCurrentRightId(currentRightId);
        formDataParamBean.setRightId(rightId);
        formDataParamBean.setViews(views);
        formDataParamBean.setUserAgent(userAgent);
        formDataParamBean.setPcRightIdSet(pcRightIdSet);
        formDataParamBean.setPhoneRightIdSet(phoneRightIdSet);
    }

    /**
     * 验证打开表单的权限
     */
    private boolean checkCreateOrEditAuth(CtpContentAll contentAll, FormDataParamBean formDataParamBean) throws BusinessException {
        User currentUser = formDataParamBean.getCurrentUser();
        boolean isAccess = false;
        Long contentDataId = contentAll.getContentDataId();
        // designId 为报表的数据
        String designId = formDataParamBean.getDesignId();
        FormBean formBean = cap4FormCacheManager.getForm(contentAll.getContentTemplateId());
        String rightId = formDataParamBean.getRightId();
        /**
         * 表单安全验证
         * 1、如果是新建，比如自定义控件/按钮发起的，此时只传递了formTemplateId，没有权限ID，直接校验bindAuthBean的权限即可
         * 2、其他情况需要校验capFormManager.checkRightId
         */
        if (contentDataId == null) {
            // 新建
            if (contentAll.getModuleType() == ModuleType.cap4UnflowForm.getKey()) {
                FormBindAuthBean formBindAuthBean = null;
                if (formDataParamBean.getFormTemplateId() != null) {
                    formBindAuthBean = formBean.getBind().getFormBindAuthBean(formDataParamBean.getFormTemplateId().toString());
                }
                formDataParamBean.setFormBindAuthBean(formBindAuthBean);
                isAccess = formBindAuthBean != null && formBindAuthBean.checkRight(currentUser.getId());
            } else if (contentAll.getModuleType() == ModuleType.collaboration.getKey()) {
                isAccess = capFormManager.checkRightId(Long.valueOf(rightId));
            }
        } else {
            Long moduleId = contentAll.getModuleId();
            if (contentAll.getModuleType() == ModuleType.cap4UnflowForm.getKey()) {
                FormBindAuthBean formBindAuthBean = null;
                if (formDataParamBean.getFormTemplateId() != null) {
                    formBindAuthBean = formBean.getBind().getFormBindAuthBean(formDataParamBean.getFormTemplateId().toString());
                }
                isAccess = AccessControlBean.getInstance().isAccess(ApplicationCategoryEnum.form, moduleId.toString(), currentUser.getId());
                if (!isAccess && Strings.isNotBlank(designId)) {
                    isAccess = reportApi.checkAuth(Long.valueOf(designId), currentUser.getId());
                }
                if (!isAccess && formBindAuthBean != null) {
                    isAccess = formBindAuthBean.checkRight(currentUser.getId());
                }
                if (!isAccess) {
                    List<FormBindAuthBean> allBindAuths = formBean.getBind().getUnflowFormBindAuthByUserId(currentUser.getId());
                    for (FormBindAuthBean bindAuthBean : allBindAuths) {
                        List<SimpleObjectBean> list = bindAuthBean.getAuthList();
                        for (SimpleObjectBean sob : list) {
                            if ((Strings.isNotBlank(sob.getValue()) && sob.getValue().indexOf(rightId) > -1)
                                    || (Strings.isNotBlank(sob.getPhoneValue()) && sob.getPhoneValue().indexOf(rightId) > -1)) {
                                formBindAuthBean = bindAuthBean;
                                isAccess = true;
                                break;
                            }
                        }
                    }
                }
                if (formBindAuthBean != null && formBindAuthBean.getFormFormulaBean() != null) {
                    //验证此条数据是否在无流程的操作范围中
                    FlipInfo flipInfo = new FlipInfo();
                    AppContext.putThreadContext(FormConstant.CHECK_RIGHT_DATA_ID, contentDataId);
                    // (自动发起)BUG_普通_V5_V7.1_新客户质保服务_广西正明人力资源有限责任公司_某个应用下的所有的无流程表单数据加载很慢，点击数据一直显示数据加载中，大概需要15秒以上才能打开_20191128099647_2019-11-28
                    // 需要传递应用绑定id，否则会拼装多个应用绑定的sql
                    cap4FormDataManager.getFormQueryResult(currentUser.getId(), flipInfo, false, formBean, formBindAuthBean.getId(),
                            FormQueryTypeEnum.unFlowCheckRight, null, null, null, null, null, false);
                    AppContext.removeThreadContext(FormConstant.CHECK_RIGHT_DATA_ID);
                    isAccess = Strings.isEmpty(flipInfo.getData()) ? false : true;
                }
                formDataParamBean.setFormBindAuthBean(formBindAuthBean);
                if (isAccess) {
                    CAPFormUtil.addFormAccessControl(moduleId, currentUser.getId());
                }
            } else if (contentAll.getModuleType() == ModuleType.collaboration.getKey()) {
                isAccess = capFormManager.checkRightId(Long.valueOf(rightId));
                if (isAccess) {
                    isAccess = AccessControlBean.getInstance().isAccess(ApplicationCategoryEnum.form, moduleId.toString(), currentUser.getId());
                }
            }
        }
        return isAccess;
    }

    /**
     * 是否是待发编辑打开
     */
    private boolean isWaitSentEdit(ModuleType mType, String operateType, Long contentDataId) {
        return mType == ModuleType.collaboration && CAPBusinessEnum.FormOperateType.NEW.getKey().equals(operateType) && contentDataId != null;
    }

    private void removeEmptyFieldsRightId(FormBean formBean, Set<String> rightIds, FormDataParamBean formDataParamBean) {
        if (rightIds != null) {
            Iterator<String> iterator = rightIds.iterator();
            while (iterator.hasNext()) {
                String rightId = iterator.next();
                FormAuthViewBean formAuthViewBean = formBean.getAuthViewBeanById(Long.valueOf(rightId));
                if (formAuthViewBean == null) {
                    iterator.remove();
                    continue;
                }
                FormViewBean formViewBean = formBean.getFormView(formAuthViewBean.getFormViewId(), formDataParamBean.getLang());
                if (formViewBean == null || !formViewBean.hasFields()) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 清空个人模板数据
     **/
    private void clearSelfTemplateData(FormBean formBean, FormDataMasterBean formDataMasterBean, Long rightId) throws BusinessException {
        Map<String, Map<String, Object>> fields = cap4FormDataManager.getClearFields(formBean, String.valueOf(rightId));
        if (fields != null) {
            Map<String, CAPFormRelationRecord> capFormRelationRecordMap = formDataMasterBean.getRelationRecordMap();
            List<CAPFormRelationRecord> capFormRelationRecords = formDataMasterBean.getRelationRecords();
            //清空主表
            String masterTableName = formBean.getMasterTableBean().getTableName();
            Map<String, Object> clearMasterFields = fields.get(masterTableName);
            if (clearMasterFields != null) {
                Iterator<String> it = clearMasterFields.keySet().iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    formDataMasterBean.addFieldValue(key, clearMasterFields.get(key));
                    if (capFormRelationRecordMap.get(key) != null) {
                        capFormRelationRecords.remove(capFormRelationRecordMap.get(key));
                    }
                }
            }
            //清空明细表
            List<FormTableBean> subTableBeans = formBean.getSubTableBean();
            for (FormTableBean formTableBean : subTableBeans) {
                String subTableName = formTableBean.getTableName();
                Map<String, Object> clearSubFields = fields.get(subTableName);
                if (clearSubFields != null) {
                    Iterator<String> it = clearSubFields.keySet().iterator();
                    List<FormDataSubBean> formDataSubBeans = formDataMasterBean.getSubData(subTableName);
                    while (it.hasNext()) {
                        String key = it.next();
                        for (FormDataSubBean formDataSubBean : formDataSubBeans) {
                            formDataSubBean.addFieldValue(key, clearSubFields.get(key));
                            String key1 = key + FormConstant.DOWNLINE + formDataSubBean.getId();
                            if (capFormRelationRecordMap.get(key1) != null) {
                                capFormRelationRecords.remove(capFormRelationRecordMap.get(key1));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取userAgent
     */
    protected int getUserAgent(FormDataParamBean formDataParamBean) {
        int userAgent = formDataParamBean.getCurrentUser().getLoginSign();
        String from = formDataParamBean.getFrom();
        if (CAPBusinessEnum.FormOpenFrom.ORIGINAL_FORM.getKey().equals(from)) {
            userAgent = com.seeyon.ctp.common.constants.Constants.login_sign.pc.value();
        } else if (CAPBusinessEnum.FormOpenFrom.LIGHT_FORM.getKey().equals(from)) {
            userAgent = com.seeyon.ctp.common.constants.Constants.login_sign.phone.value();
        } else if (CAPBusinessEnum.FormOpenFrom.API_FORWARD.getKey().equals(from)) {
            // 转发需要set userAgent
            userAgent = formDataParamBean.getUserAgent();
        }
        // 致信打开为pc
        if (com.seeyon.ctp.common.constants.Constants.login_sign.ucpc.value() == userAgent) {
            userAgent = com.seeyon.ctp.common.constants.Constants.login_sign.pc.value();
        }
        // 微协同打开agent为phone
        if (com.seeyon.ctp.common.constants.Constants.login_sign.wechat.value() == userAgent) {
            userAgent = com.seeyon.ctp.common.constants.Constants.login_sign.phone.value();
        }
        return userAgent;
    }

    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

    public void setSerialCalRecordManager(SerialCalRecordManager serialCalRecordManager) {
        this.serialCalRecordManager = serialCalRecordManager;
    }

    public void setReportApi(ReportApi reportApi) {
        this.reportApi = reportApi;
    }

    public void setSimulationApi(SimulationApi simulationApi) {
        this.simulationApi = simulationApi;
    }

    public void setCap4FormTriggerSourceRecordDAO(CAP4FormTriggerSourceRecordDAO cap4FormTriggerSourceRecordDAO) {
        this.cap4FormTriggerSourceRecordDAO = cap4FormTriggerSourceRecordDAO;
    }

    public void setCapCustomService(CAPCustomService capCustomService) {
        this.capCustomService = capCustomService;
    }

    public void setCapFormManager(CAPFormManager capFormManager) {
        this.capFormManager = capFormManager;
    }

    public void setCapFormDataSignatureService(CAPFormDataSignatureService capFormDataSignatureService) {
        this.capFormDataSignatureService = capFormDataSignatureService;
    }

    public void setCapFormDataForwardService(CAPFormDataForwardService capFormDataForwardService) {
        this.capFormDataForwardService = capFormDataForwardService;
    }

    public void setCapTransFormDataBeanManager(CAPTransFormDataBeanManager capTransFormDataBeanManager) {
        this.capTransFormDataBeanManager = capTransFormDataBeanManager;
    }
}
