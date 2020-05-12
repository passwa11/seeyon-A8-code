package com.seeyon.cap4.form.modules.engin.businessRelation;

import com.seeyon.cap4.form.bean.*;
import com.seeyon.cap4.form.bean.FormTriggerBean.TriggerBusinessType;
import com.seeyon.cap4.form.modules.engin.trigger.CAP4FormTriggerManager;
import com.seeyon.cap4.form.modules.engin.trigger.CAP4FormTriggerRecordDAO;
import com.seeyon.cap4.form.modules.engin.trigger.FormTriggerUtil;
import com.seeyon.cap4.form.po.CAPFormBusinessRelation;
import com.seeyon.cap4.form.service.CAP4FormCacheManager;
import com.seeyon.cap4.form.util.FormConstant;
import com.seeyon.cap4.magic.constants.MagicPrivateConstants;
import com.seeyon.cap4.magic.dao.IMagicInterfaceDefineDao;
import com.seeyon.cap4.magic.dao.IMagicReflectionRelationDao;
import com.seeyon.cap4.magic.manager.ICAP4MagicManager;
import com.seeyon.cap4.magic.po.MagicInterfaceDefineEntity;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.form.modules.engin.formula.FormulaDAO;
import com.seeyon.ctp.form.po.CtpFormula;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.logging.Log;

import java.util.*;

/**
 * 表单业务关系--触发设置manager实现 Created by chenxb on 2017-10-13.
 */
public class CAP4FormTriggerDesignManagerImpl implements CAP4FormTriggerDesignManager {

    private static final Log LOGGER = CtpLogFactory.getLog(CAP4FormTriggerDesignManagerImpl.class);

    private CAP4FormCacheManager cap4FormCacheManager;
    private CAP4FormBusinessRelationDAO cap4FormBusinessRelationDAO;
    private FormulaDAO formulaDAO;
    private CAP4FormTriggerManager cap4FormTriggerManager;
    private CAP4FormTriggerRecordDAO cap4FormTriggerRecordDAO;
    private IMagicInterfaceDefineDao magicInterfaceDefineDao;
    private ICAP4MagicManager cap4MagicManager;
    private IMagicReflectionRelationDao magicReflectionRelationDao;

    @AjaxAccess
    @Override
    public Map<String, Object> editTrigger(String triggerId) throws BusinessException {
        Map<String, Object> map = new HashMap<String, Object>();
        FormTriggerBean triggerBean = cap4FormCacheManager.getFormTriggerBean(Long.valueOf(triggerId));
        if (triggerBean != null) {
            map.put("triggerId", triggerBean.getId());
            map.put("triggerName", triggerBean.getName());
            map.put("state", triggerBean.getState());
            map.put("triggerType", triggerBean.getType());
            map.put("triggerPoint", triggerBean.getTriggerPoint());
            if (FormTriggerBean.TriggerPoint.Condition_Operation.getKey().equals(triggerBean.getTriggerPoint())) {
                map.put("pcRight", triggerBean.getPcRight());
                map.put("phoneRight", triggerBean.getPhoneRight());
            }

            FormTriggerConditionBean filterCondition = triggerBean.getFilterCondition();
            if (filterCondition == null) {
                map.put("filterConditionId", "");
                map.put("filterConditionFormulaId", "");
                map.put("filterConditionValue", "");
            } else {
                map.put("filterConditionId", filterCondition.getId());
                map.put("filterConditionFormulaId", filterCondition.getConditionFormulaId());
                map.put("filterConditionValue", filterCondition.getConditionFormula() == null ? "" : filterCondition.getConditionFormula().getFormulaForDisplay());
            }

            FormTriggerConditionBean dateCondition = triggerBean.getDateCondition();
            if (dateCondition == null) {
                map.put("timeConditionId", "");
                map.put("timeFormulaId", "");
                map.put("timeQuartz", "");
            } else {
                map.put("timeConditionId", dateCondition.getId());
                map.put("timeFormulaId", dateCondition.getConditionFormulaId());
                map.put("timeQuartz", dateCondition.getTimeFrequency() + "|" + dateCondition.getConditionFormula().getFormulaForDisplay() + "|" + dateCondition.getTriggerTime());
            }

            // 表间触发和触发消息才直接构造动作列表，其余的单独请求调用下面的getActionList方法
            if (FormTriggerBean.TriggerBusinessType.FormOuter.getKey().equals(triggerBean.getType())
                    || FormTriggerBean.TriggerBusinessType.Message.getKey().equals(triggerBean.getType())
                    || FormTriggerBean.TriggerBusinessType.OuterForm.getKey().equals(triggerBean.getType())
                    || FormTriggerBean.TriggerBusinessType.DataInterface.getKey().equals(triggerBean.getType())) {
                List<Map<String, Object>> actionMapList = new ArrayList<Map<String, Object>>();
                map.put("actionList", actionMapList);
                for (FormTriggerActionBean action : triggerBean.getActionList()) {
                    Map<String, Object> paramMap = FormTriggerUtil.getDesignManagerByKey(action.getType()).getParamFromAction(triggerBean, action);
                    actionMapList.add(paramMap);
                }
            }

            //执行死循环校验
            map.put("deadCycle", validateTrigger4Save(triggerBean.getSourceFormId(), triggerBean.getTargetFormId()));
        }
        return map;
    }

    @AjaxAccess
    @Override
    public Map<String, Object> getActionList(String triggerId) throws BusinessException {
        Map<String, Object> map = new HashMap<String, Object>();
        FormTriggerBean triggerBean = cap4FormCacheManager.getFormTriggerBean(Long.valueOf(triggerId));
        if (triggerBean != null) {
            List<Map<String, Object>> actionMapList = new ArrayList<Map<String, Object>>();
            map.put("actionList", actionMapList);
            for (FormTriggerActionBean action : triggerBean.getActionList()) {
                Map<String, Object> paramMap = FormTriggerUtil.getDesignManagerByKey(action.getType()).getParamFromAction(triggerBean, action);
                actionMapList.add(paramMap);
            }
            map.put("type", triggerBean.getType());
        }
        return map;
    }

    @AjaxAccess
    @Override
    public boolean validateTriggerName(String formId, String triggerId, String triggerName) {
        if (Strings.isBlank(triggerName)) {
            return true;
        }
        FormBean formBean = cap4FormCacheManager.getForm(Long.valueOf(formId));
        List<Long> triggerIdList = formBean.getFormTriggerIdList();
        if (triggerIdList.size() > 0) {
            for (Long id : triggerIdList) {
                if (!id.toString().equals(triggerId)) {
                    FormTriggerBean triggerBean = cap4FormCacheManager.getFormTriggerBean(id);
                    if (triggerBean != null && triggerBean.getName().equals(triggerName)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @AjaxAccess
    @Override
    public String validateTrigger4Save(Long sourceFormId, Long targetFormId) {
        String result = "";
        if (targetFormId == null || 0 == targetFormId) {
            return result;
        }
        try {
            // 校验死循环 如果根据目标表找到的表单ID集合中包含源表单，则表示可能存在死循环
            Set<Long> formIdSet = getTargetFormIdSet(targetFormId, 5, 0, null);
            if (formIdSet.contains(sourceFormId)) {
                StringBuilder sb = new StringBuilder();
                for (Long formId : formIdSet) {
                    if (formId.equals(sourceFormId)) {
                        continue;
                    }
                    FormBean formBean = cap4FormCacheManager.getForm(formId);
                    sb.append(formBean.getFormName()).append("、");
                }
                String formNames = sb.toString();
                formNames = formNames.substring(0, formNames.length() - 1);
                return "deadCycle" + formNames;
            }
            /*// 校验数量限制
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("type", "trigger");
            // 先查顺向
            param.put("sourceFormId", sourceFormId);
            param.put("targetFormId", targetFormId);
            int source2Target = cap4FormBusinessRelationDAO.getRelationCount(param);
            // 再查反向
            param.put("sourceFormId", targetFormId);
            param.put("targetFormId", sourceFormId);
            int target2Source = cap4FormBusinessRelationDAO.getRelationCount(param);
            if ((source2Target + target2Source) > 50) {
                return "countLimit";
            }*/
        } catch (Exception e) {
            LOGGER.error("校验异常 " + e.getMessage(), e);
        }
        return result;
    }

    /**
     * 递归遍历指定表单的关系中的目标表单链
     *
     * @param formId    指定表单ID
     * @param maxIndex  最大遍历层级
     * @param nowIndex  当前遍历的层级
     * @param formIdSet 目标表单ID集合
     */
    private Set<Long> getTargetFormIdSet(Long formId, int maxIndex, int nowIndex, Set<Long> formIdSet) throws BusinessException {
        Set<Long> resultList = formIdSet;
        if (resultList == null) {
            resultList = new LinkedHashSet<Long>();
        }
        resultList.add(formId);
        if (nowIndex > maxIndex) {
            return resultList;
        }
        nowIndex++;
        Set<Long> tempList;
        List<Long> triggerIdList = Collections.emptyList();

        FormBean fb = cap4FormCacheManager.getForm(formId);
        if (fb != null) {
            triggerIdList = fb.getFormTriggerIdList();
        }
        for (Long triggerId : triggerIdList) {
            FormTriggerBean triggerBean = cap4FormCacheManager.getFormTriggerBean(triggerId);
            // 空防护，一般情况下不会出现空的
            if (triggerBean == null) {
                continue;
            }
            if (FormTriggerBean.TriggerBusinessType.OuterForm.getKey().equals(triggerBean.getType())
                    || FormTriggerBean.TriggerBusinessType.DataInterface.getKey().equals(triggerBean.getType())) {
                // 数据魔方：外部表单、数据接口
                List<Long> targetIds = cap4MagicManager.getServiceProviderSource(triggerBean);
                if (!targetIds.isEmpty()) {
                    for (Long t : targetIds) {
                        if (resultList.add(t)) {
                            tempList = getTargetFormIdSet(t, maxIndex, nowIndex, resultList);
                            resultList.addAll(tempList);
                        }
                    }
                }
            } else if (FormTriggerBean.TriggerBusinessType.FormOuter.getKey().equals(triggerBean.getType())) {
                // 表间关系
                Long targetFormId = triggerBean.getTargetFormId();
                if (targetFormId != 0L) {
                    if (resultList.add(targetFormId)) {
                        tempList = getTargetFormIdSet(targetFormId, maxIndex, nowIndex, resultList);
                        resultList.addAll(tempList);
                    }
                }
            }

        }
        return resultList;
    }

    @AjaxAccess
    @Override
    public String saveTrigger2DB(Map<String, String> baseInfo, List<Map<String, String>> actionList) throws BusinessException {
        String triggerId = baseInfo.get("triggerId");
        boolean isNew = true;
        FormTriggerBean triggerBean;
        if ("".equals(triggerId) || "-1".equals(triggerId)) {
            triggerBean = new FormTriggerBean();
            triggerBean.setId(UUIDLong.longUUID());
            triggerBean.setCreateId(AppContext.currentUserId());
            triggerBean.setCreateTime(DateUtil.currentDate());
        } else {
            isNew = false;
            triggerBean = cap4FormCacheManager.getFormTriggerBean(Long.parseLong(triggerId));
            //删除值拷贝计算公式和行条件公式
            deleteFormulaInTrigger(triggerBean.getNeedDelFormulaList());
        }
        triggerBean.setModifyTime(DateUtil.currentDate());
        triggerBean.clearFormulaList();

        triggerBean.setName(baseInfo.get("triggerName").trim());
        triggerBean.setState(Integer.valueOf(baseInfo.get("state")));
        triggerBean.setSourceFormId(Long.valueOf(baseInfo.get("sourceFormId")));
        FormBean sourceForm = cap4FormCacheManager.getForm(triggerBean.getSourceFormId());
        if (Strings.isNotBlank(baseInfo.get("targetFormId"))) {
            triggerBean.setTargetFormId(Long.valueOf(baseInfo.get("targetFormId")));
        } else {
            triggerBean.setTargetFormId(0L);
        }
        triggerBean.setType(baseInfo.get("triggerType"));

        triggerBean.setTriggerPoint(baseInfo.get("triggerPoint"));
        if (FormTriggerBean.TriggerPoint.Condition_Operation.getKey().equals(triggerBean.getTriggerPoint())) {
            triggerBean.setPcRight(baseInfo.get("pcRight"));
            triggerBean.setPhoneRight(baseInfo.get("phoneRight"));
        } else {
            triggerBean.setPcRight("");
            triggerBean.setPhoneRight("");
        }

        // 数据域过滤条件
        String filterConditionId = baseInfo.get("filterConditionId");
        String filterConditionValue = baseInfo.get("filterConditionValue");
        if (Strings.isNotBlank(filterConditionValue)) {
            FormTriggerConditionBean filterCondition = new FormTriggerConditionBean(triggerBean);
            filterCondition.setId("".equals(filterConditionId) ? UUIDLong.longUUID() : Long.valueOf(filterConditionId));
            filterCondition.setType(FormTriggerBean.ConditionType.form.getKey());

            FormFormulaBean filterFormula = new FormFormulaBean(sourceForm);
            filterFormula.loadFromFormula(filterConditionValue);
            if (Strings.isNotBlank(baseInfo.get("filterConditionFormulaId"))) {
                filterFormula.setFormulaId(Long.valueOf(baseInfo.get("filterConditionFormulaId")));
            }
            filterCondition.setConditionFormulaId(filterFormula.getFormulaId() == null ? "" : "" + filterFormula.getFormulaId());
            filterCondition.setConditionFormula(filterFormula);
            triggerBean.setFilterCondition(filterCondition);
            triggerBean.addFormula(filterFormula);
        } else {
            triggerBean.setFilterCondition(null);
        }

        // 时间调度
        String oldTimeQuartz = "";
        FormTriggerConditionBean oldDateCondition = triggerBean.getDateCondition();
        if (oldDateCondition != null) {
            oldTimeQuartz = oldDateCondition.getTimeFrequency() + "|" + oldDateCondition.getConditionFormula().getFormulaForDisplay() + "|" + oldDateCondition.getTriggerTime();
        }
        String timeConditionId = baseInfo.get("timeConditionId");
        String timeQuartz = baseInfo.get("timeQuartz");
        if (Strings.isNotBlank(timeQuartz)) {
            FormTriggerConditionBean dateCondition = new FormTriggerConditionBean(triggerBean);
            dateCondition.setType(FormTriggerBean.ConditionType.date.getKey());
            dateCondition.setId(Strings.isBlank(timeConditionId) ? UUIDLong.longUUID() : Long.parseLong(timeConditionId));

            String[] timeQuarts = timeQuartz.split("\\|");
            dateCondition.setTimeFrequency(timeQuarts[0]);
            dateCondition.setTriggerTime(timeQuarts[2]);

            FormFormulaBean timeFormula = new FormFormulaBean(sourceForm);
            timeFormula.loadFromFormula(timeQuarts[1]);
            if (Strings.isNotBlank(baseInfo.get("timeFormulaId"))) {
                timeFormula.setFormulaId(Long.valueOf(baseInfo.get("timeFormulaId")));
            }
            dateCondition.setConditionFormulaId(timeFormula.getFormulaId() == null ? "" : "" + timeFormula.getFormulaId());
            dateCondition.setConditionFormula(timeFormula);
            triggerBean.setDateCondition(dateCondition);
            triggerBean.addFormula(timeFormula);
            //重置下，用于后面更新调度判断
            timeQuartz = dateCondition.getTimeFrequency() + "|" + dateCondition.getConditionFormula().getFormulaForDisplay() + "|" + dateCondition.getTriggerTime();
        } else {
            triggerBean.setDateCondition(null);
            timeQuartz = "";
        }

        // 如果是数据接口需要先删除，再保存，防止接口被无用的触发绑定导致接口绑定数统计不准确
        if (TriggerBusinessType.OuterForm.getKey().equals(triggerBean.getType())
                || TriggerBusinessType.DataInterface.getKey().equals(triggerBean.getType())) {
            magicReflectionRelationDao.deleteByActionOrShipId(String.valueOf(triggerBean.getId()));
        }
        // 动作列表
        List<FormTriggerActionBean> actionBeanList = new ArrayList<FormTriggerActionBean>();
        List<Map<String, Object>> entitySummary = new ArrayList<Map<String, Object>>();
        for (Map<String, String> actionMap : actionList) {
            String actionType = actionMap.get("actionType");
            FormTriggerActionBean actionBean = FormTriggerUtil.getDesignManagerByKey(actionType).getActionFromMap(triggerBean, actionMap);
            actionBeanList.add(actionBean);

            // 如果是外部表单 需要设置改名功能
            if (FormTriggerBean.TriggerType.DataMagicOuterForm.getKey().equals(actionType)) {
                Object needRename = actionBean.getParam(MagicPrivateConstants.DATAMAGIC_NEEDRENAME);

                Object entityId = actionBean.getParam(MagicPrivateConstants.DATAMAGIC_ID);
                if (entityId != null) {
                    try {
                        Long id = Long.parseLong(entityId.toString());
                        MagicInterfaceDefineEntity entity = magicInterfaceDefineDao.loadDefineById(id);
                        if (entity != null) {
                            Map<String, Object> summary = new HashMap<String, Object>();
                            summary.put("id", String.valueOf(entity.getId()));
                            summary.put("name", entity.getName(AppContext.getLocale()));
                            summary.put("type", entity.getCategory());
                            if (needRename != null && "1".equals(needRename.toString())) {
                                summary.put("exists", "0");
                            } else {
                                summary.put("exists", "1");
                            }
                            entitySummary.add(summary);
                        }
                    } catch (NumberFormatException e) {
                        LOGGER.warn(e);
                    }
                }
            }
        }
        triggerBean.setActionList(actionBeanList);

        // 保存计算公式
        formulaDAO.insertList(triggerBean.getCtpFormulaList());
        if (!isNew) {
            cap4FormBusinessRelationDAO.deleteById(triggerBean.getId());
        }
        CAPFormBusinessRelation businessRelation = triggerBean.toCAPFormBusinessRelation();
        cap4FormBusinessRelationDAO.save(businessRelation);
        // 创建此触发对应的触发记录表
        if (isNew) {
            cap4FormTriggerRecordDAO.createTriggerRecordTable(FormTriggerUtil.getTriggerRecordName(sourceForm));
        }
        // 保存完成后初始化一次表单
        cap4FormCacheManager.removeFormBean(triggerBean.getSourceFormId());
        cap4FormCacheManager.initForm(triggerBean.getSourceFormId());
        //todo zhou
        // 更新时间调度 更新调度采用异步线程方式执行，因此需要在初始化表单之后
        cap4FormTriggerManager.updateTriggerQuartzJob(triggerBean, isNew, oldTimeQuartz, timeQuartz);
        LOGGER.info("保存业务关系：" + triggerBean.getId() + triggerBean.getName());

        Map<String, Object> dc = new HashMap<String, Object>();
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("success", true);
        result.put("id", String.valueOf(businessRelation.getId()));
        result.put("name", businessRelation.getName());
        dc.put("result", result);

        dc.put(FormConstant.SUCCESS, true);
        dc.put("needRename", "1");
        dc.put("entitySummarys", entitySummary);
        return JSONUtil.toJSONString(dc);
    }

    @AjaxAccess
    @Override
    public void deleteTrigger(List<String> ids) throws BusinessException {
        //删除关系需要判断是否有cap_base 或者cap_advance插件
        if (!AppContext.hasPlugin(FormConstant.CAP_ADVANCE) && !AppContext.hasPlugin(FormConstant.CAP_BASE)) {
            throw new BusinessException(ResourceUtil.getString("form.over.power.tips"));
        }
        Set<Long> formIds = new HashSet<Long>();
        for (String id : ids) {
            // 先移除缓存
            FormTriggerBean triggerBean = cap4FormCacheManager.getFormTriggerBean(Long.valueOf(id));
            if (triggerBean != null) {
                formIds.add(triggerBean.getSourceFormId());
                // 删除定时任务
                if (triggerBean.hasTimeCondition()) {
                    QuartzHolder.deleteQuartzJobByGroup(id);
                }
                // 删除计算公式
                deleteFormulaInTrigger(triggerBean.getFormulaList());
                this.cap4MagicManager.deleteReflectionsByTrigger(triggerBean);
            }
            // 再提交数据库
            cap4FormBusinessRelationDAO.deleteById(Long.valueOf(id));
        }
        // 重新加载表单缓存
        for (Long formId : formIds) {
            cap4FormCacheManager.removeFormBean(formId);
            cap4FormCacheManager.initForm(formId);
        }
        LOGGER.info("删除业务关系成功：" + ids);
    }

    /**
     * 将触发中涉及到的计算公式从缓存中取出，删除时使用
     */
    private void deleteFormulaInTrigger(List<FormFormulaBean> formulaList) throws BusinessException {
        if (Strings.isNotEmpty(formulaList)) {
            List<CtpFormula> ctpFormulas = new ArrayList<CtpFormula>();
            for (FormFormulaBean formula : formulaList) {
                if (!(formula == null || formula.getFormulaId() == null)) {
                    CtpFormula ctpFormula = cap4FormCacheManager.getFormula(formula.getFormulaId());
                    if (ctpFormula != null) {
                        ctpFormulas.add(ctpFormula);
                    }
                }
            }
            formulaDAO.deleteList(ctpFormulas);
            cap4FormCacheManager.removeFormula(ctpFormulas);
        }
    }

    @AjaxAccess
    @Override
    public String getSubTableNameInCondition(long formId, String conditionStr, boolean needDefault)
            throws BusinessException {
        FormBean formBean = cap4FormCacheManager.getForm(formId);
        if (Strings.isNotEmpty(conditionStr)) {
            Set<FormTableBean> subTableBeans = getSubTableBeanInCondition(formBean, conditionStr);
            for (FormTableBean table : subTableBeans) {
                if (!table.isMainTable()) {
                    return table.getTableName();
                }
            }
        }
        if (needDefault) {
            List<FormTableBean> beans = formBean.getSubTableBean();
            if (Strings.isNotEmpty(beans) && beans.size() == 1) {
                return beans.get(0).getTableName();
            }
        }
        return "";
    }

    @AjaxAccess
    @Override
    public boolean senderIsSubField(long formId, String conditionStr, String senderStr) throws BusinessException {
        FormBean formBean = cap4FormCacheManager.getForm(formId);
        // 没有重复表；或者重复表只有一个时，不限制
        if (Strings.isEmpty(formBean.getSubTableBean()) || formBean.getSubTableBean().size() == 1) {
            return false;
        }
        // 创建人是主表字段，不限制
        String[] senderArr = FormTriggerUtil.getFieldName(senderStr);
        FormTableBean senderTableBean = formBean.getFormTableBeanByFieldName(senderArr[1]);
        return !senderTableBean.isMainTable() && senderTableBean.getTableName().equalsIgnoreCase(getSubTableNameInCondition(formId, conditionStr, false));
    }

    @AjaxAccess
    @Override
    public boolean senderIsSubField(long formId, String conditionStr, String senderStr, String msContent)
            throws BusinessException {
        FormBean formBean = cap4FormCacheManager.getForm(formId);
        // 没有重复表；或者重复表只有一个时，不限制
        if (Strings.isEmpty(formBean.getSubTableBean()) || formBean.getSubTableBean().size() == 1) {
            return false;
        }
        // 创建人是主表字段，不限制
        String[] senderArr = FormTriggerUtil.getFieldName(senderStr);
        FormTableBean senderTableBean = formBean.getFormTableBeanByFieldName(senderArr[1]);
        if (senderTableBean.isMainTable()) {
            return false;
        }

        Set<FormTableBean> conditionSubTableBeans = getSubTableBeanInCondition(formBean, conditionStr);
        Set<FormTableBean> msgSubTableBeans = FormTriggerUtil.getSubTableBeanInMsgContent(formBean, msContent);
        return !conditionSubTableBeans.contains(senderTableBean) && !msgSubTableBeans.contains(senderTableBean);
    }

    /**
     * 私有方法，获取数据域条件中的重复表列表
     */
    private Set<FormTableBean> getSubTableBeanInCondition(FormBean formBean, String conditionStr)
            throws BusinessException {
        Set<FormTableBean> subTableBeans = new HashSet<FormTableBean>();
        FormFormulaBean formulaBean = new FormFormulaBean(formBean);
        formulaBean.loadFromFormula(conditionStr);
        Set<FormFieldBean> fieldBeans = formulaBean.getInFormulaFormFieldBean();
        for (FormFieldBean fieldBean : fieldBeans) {
            subTableBeans.add(formBean.getTableByTableName(fieldBean.getOwnerTableName()));
        }
        return subTableBeans;
    }

    @Override
    @AjaxAccess
    public String getSubTableNameInMsgContent(long formId, String msg) throws BusinessException {
        String subTableName = "";
        FormBean formBean = cap4FormCacheManager.getForm(formId);
        if (Strings.isNotEmpty(msg)) {
            Set<FormTableBean> subTableList = FormTriggerUtil.getSubTableBeanInMsgContent(formBean, msg);
            if (Strings.isNotEmpty(subTableList) && subTableList.size() == 1) {
                Iterator<FormTableBean> iterator = subTableList.iterator();
                subTableName = iterator.next().getTableName();
            }
        }
        if ("".equals(subTableName)) {
            List<FormTableBean> beans = formBean.getSubTableBean();
            if (Strings.isNotEmpty(beans) && beans.size() == 1) {
                subTableName = beans.get(0).getTableName();
            }
        }
        return subTableName;
    }

    public void setCap4FormCacheManager(CAP4FormCacheManager cap4FormCacheManager) {
        this.cap4FormCacheManager = cap4FormCacheManager;
    }

    public void setCap4FormBusinessRelationDAO(CAP4FormBusinessRelationDAO cap4FormBusinessRelationDAO) {
        this.cap4FormBusinessRelationDAO = cap4FormBusinessRelationDAO;
    }

    public void setFormulaDAO(FormulaDAO formulaDAO) {
        this.formulaDAO = formulaDAO;
    }

    public void setCap4FormTriggerManager(CAP4FormTriggerManager cap4FormTriggerManager) {
        this.cap4FormTriggerManager = cap4FormTriggerManager;
    }

    public void setCap4FormTriggerRecordDAO(CAP4FormTriggerRecordDAO cap4FormTriggerRecordDAO) {
        this.cap4FormTriggerRecordDAO = cap4FormTriggerRecordDAO;
    }

    public void setMagicInterfaceDefineDao(IMagicInterfaceDefineDao magicInterfaceDefineDao) {
        this.magicInterfaceDefineDao = magicInterfaceDefineDao;
    }

    public void setCap4MagicManager(ICAP4MagicManager cap4MagicManager) {
        this.cap4MagicManager = cap4MagicManager;
    }

    public void setMagicReflectionRelationDao(IMagicReflectionRelationDao magicReflectionRelationDao) {
        this.magicReflectionRelationDao = magicReflectionRelationDao;
    }

}
