package com.seeyon.cap4.form.modules.engin.trigger;

import com.seeyon.cap4.form.bean.*;
import com.seeyon.cap4.form.modules.business.BizConfigBean;
import com.seeyon.cap4.form.modules.business.BusinessManager;
import com.seeyon.cap4.form.modules.engin.base.formData.CAP4FormDataDAO;
import com.seeyon.cap4.form.modules.engin.base.formData.CAP4FormDataManager;
import com.seeyon.cap4.form.modules.engin.formula.FormulaEnums;
import com.seeyon.cap4.form.modules.engin.formula.FormulaUtil;
import com.seeyon.cap4.form.po.CAPFormTriggerEvent;
import com.seeyon.cap4.form.service.CAP4FormCacheManager;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.cap4.form.util.FormConstant;
import com.seeyon.cap4.form.vo.FormTriggerQuartzDataVo;
import com.seeyon.cap4.task.TaskExecuteManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.quartz.QuartzListener;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.event.EventTriggerMode;
import com.seeyon.ctp.form.modules.trigger.FormWithHoldingDAO;
import com.seeyon.ctp.form.po.FormWithholding;
import com.seeyon.ctp.thread.ThreadInfoHolder;
import com.seeyon.ctp.util.*;
import com.seeyon.ctp.util.annotation.ListenEvent;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.logging.Log;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表单业务关系--触发类manager实现类
 * Created by chenxb on 2017-9-18.
 */
public class CAP4FormTriggerManagerImpl implements CAP4FormTriggerManager {

    private static final Log LOGGER = CtpLogFactory.getLog(CAP4FormTriggerManagerImpl.class);

    private CAP4FormCacheManager cap4FormCacheManager;
    private CAP4FormTriggerRecordDAO cap4FormTriggerRecordDAO;
    private TaskExecuteManager taskExecuteManager;
    private CAP4FormManager cap4FormManager;
    private FormWithHoldingDAO formWithHoldingDAO;
    private CAP4FormDataDAO cap4FormDataDAO;
    private CAP4FormDataManager cap4FormDataManager;
    private Pattern datePattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");

    /**
     * 执行触发,对外接口，让其他模块调用
     */
    @Override
    public void doTrigger(int moduleType, long moduleId, long formId, String rightStr) throws BusinessException, SQLException {
        doTrigger(moduleType, moduleId, formId, rightStr, null, FormTriggerBean.TriggerEventSourceEnum.outerInterface.getKey());
    }

    /**
     * 自动更新、新建记录、更新记录、数据魔方等执行完成后调用，谨记此方法不对外，调用之后如果后续有变动概不负责
     * <p>
     * isAuto 新建记录、更新记录是为true，为false
     */
    @Override
    public void doTrigger(int moduleType, long moduleId, long formId, String rightStr, List<String> modifiedFields, boolean isAuto) throws BusinessException, SQLException {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put(FormTriggerConstant.TRIGGER_PARAM_FORM_ID, formId);
        param.put(FormTriggerConstant.TRIGGER_PARAM_RIGHTSTR, rightStr);
        param.put(FormTriggerConstant.TRIGGER_PARAM_MODIFY_FIELDS, modifiedFields);
        param.put(FormTriggerConstant.TRIGGER_PARAM_IS_AUTO, isAuto);
        param.put(FormTriggerConstant.TRIGGER_PARAM_SOURCE_TYPE, FormTriggerBean.TriggerEventSourceEnum.triggerInner.getKey());
        //采用异步执行，增加当前用户参数
        param.put("current_user_id", AppContext.currentUserId());
        param.put(FormTriggerConstant.TRIGGER_PARAM_USER_LOCALE, AppContext.getCurrentUser().getLocale());
        param.put(FormTriggerConstant.TRIGGER_PARAM_USER_IPADDR, AppContext.getRemoteAddr());
        //通过事件监听去处理协同触发
        dispatchTriggerFireEvent(moduleType, moduleId, moduleId, param);
    }

    /**
     * 定时调度(自动更新、新建记录、更新记录、数据魔方等)执行完成后调用，谨记此方法不对外，调用之后如果后续有变动概不负责
     */
    @Override
    public void doTrigger(FormTriggerQuartzDataVo dataVo) throws BusinessException, SQLException {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put(FormTriggerConstant.TRIGGER_PARAM_FORM_ID, dataVo.getFormId());
        param.put(FormTriggerConstant.TRIGGER_PARAM_RIGHTSTR, dataVo.getFormId());
        param.put(FormTriggerConstant.TRIGGER_PARAM_MODIFY_FIELDS, dataVo.getModifiedFields());
        param.put(FormTriggerConstant.TRIGGER_PARAM_IS_AUTO, dataVo.isAuto());
        param.put("current_user_id", dataVo.getMemberId());
        param.put(FormTriggerConstant.TRIGGER_PARAM_USER_LOCALE, dataVo.getLocale());
        param.put(FormTriggerConstant.TRIGGER_PARAM_USER_IPADDR, AppContext.getRemoteAddr());
        param.put(FormTriggerConstant.TRIGGER_PARAM_SOURCE_TYPE, FormTriggerBean.TriggerEventSourceEnum.triggerInner.getKey());
        //通过事件监听去处理协同触发
        dispatchTriggerFireEvent(dataVo.getModuleType(), dataVo.getMasterId(), dataVo.getMasterId(), param);
    }

    /**
     * 新建同步刷新、反馈执行时调用，谨记此方法不对外，调用之后如果后续有变动概不负责
     * FormTriggerCreateBaseAction.doTargetMasterBeanTrigger()
     */
    @Override
    public void doTrigger(int moduleType, long moduleId, long formId, long sourceTriggerId, long sourceActionId, List<String> modifiedFields) throws BusinessException {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put(FormTriggerConstant.TRIGGER_PARAM_FORM_ID, formId);
        param.put(FormTriggerConstant.TRIGGER_PARAM_SOURCE_TRIGGER_ID, sourceTriggerId);
        param.put(FormTriggerConstant.TRIGGER_PARAM_SOURCE_ACTION_ID, sourceActionId);
        param.put(FormTriggerConstant.TRIGGER_PARAM_MODIFY_FIELDS, modifiedFields);
        //param.put(FormTriggerConstant.TRIGGER_PARAM_IS_AUTO, true);
        param.put(FormTriggerConstant.TRIGGER_PARAM_SOURCE_TYPE, FormTriggerBean.TriggerEventSourceEnum.triggerInner.getKey());
        //采用异步执行，增加当前用户参数
        param.put("current_user_id", AppContext.currentUserId());
        param.put(FormTriggerConstant.TRIGGER_PARAM_USER_LOCALE, AppContext.getCurrentUser().getLocale());
        param.put(FormTriggerConstant.TRIGGER_PARAM_USER_IPADDR, AppContext.getRemoteAddr());
        //通过事件监听去处理协同触发
        dispatchTriggerFireEvent(moduleType, moduleId, moduleId, param);
    }

    /**
     * 无流程数据保存、批量操作等执行完成后调用，谨记此方法不对外，调用之后如果后续有变动概不负责
     */
    @Override
    public void doTrigger(int moduleType, long moduleId, long formId, String rightStr, List<String> modifiedFields, String sourceFrom) throws BusinessException, SQLException {
        Map<String, Object> param = new HashMap<String, Object>();
        String right = rightStr;
        if (Strings.isNotBlank(rightStr)) {
            FormBean formBean = cap4FormCacheManager.getForm(formId);
            FormAuthViewBean viewBean = formBean.getAuthViewBeanById(Long.valueOf(rightStr));
            if (viewBean != null) {
                viewBean = viewBean.findBaseAuth();
                if (viewBean != null) {
                    right = viewBean.getId().toString();
                }
            }
        }
        param.put(FormTriggerConstant.TRIGGER_PARAM_FORM_ID, formId);
        param.put(FormTriggerConstant.TRIGGER_PARAM_RIGHTSTR, right);
        param.put(FormTriggerConstant.TRIGGER_PARAM_MODIFY_FIELDS, modifiedFields);
        param.put(FormTriggerConstant.TRIGGER_PARAM_IS_AUTO, false);
        param.put(FormTriggerConstant.TRIGGER_PARAM_SOURCE_TYPE, sourceFrom);
        //采用异步执行，增加当前用户参数
        param.put("current_user_id", AppContext.currentUserId());
        param.put(FormTriggerConstant.TRIGGER_PARAM_USER_LOCALE, AppContext.getCurrentUser().getLocale());
        param.put(FormTriggerConstant.TRIGGER_PARAM_USER_IPADDR, AppContext.getRemoteAddr());
        //通过事件监听去处理协同触发
        dispatchTriggerFireEvent(moduleType, moduleId, moduleId, param);
    }

    /**
     * 协同状态处理(数据状态修改)完后执行 FormDataManager.updateDataState() 即有流程的触发执行
     */
    @Override
    public void doTrigger(CtpAffair affair, int moduleType, long moduleId, FormTriggerBean.TriggerPoint triggerPoint, String triggerEventSourceEnum) throws BusinessException, SQLException {
        FormBean formBean = cap4FormCacheManager.getForm(affair == null ? 0L : affair.getFormAppId());
        if (affair == null || formBean == null) {
            LOGGER.info("当前流程对应表单不存在，触发中断…………");
            return;
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(FormTriggerConstant.TRIGGER_PARAM_FORM_ID, formBean.getId());
        map.put(FormTriggerConstant.TRIGGER_PARAM_CONDITION_STATE, triggerPoint);
        map.put("affair_member_id", affair.getMemberId());
        map.put("current_user_id", AppContext.currentUserId());//采用异步执行，增加当前用户参数
        map.put(FormTriggerConstant.TRIGGER_PARAM_USER_LOCALE, AppContext.getCurrentUser().getLocale());
        map.put(FormTriggerConstant.TRIGGER_PARAM_USER_IPADDR, AppContext.getRemoteAddr());
        map.put(FormTriggerConstant.TRIGGER_PARAM_TITLE, affair.getSubject());
        map.put(FormTriggerConstant.TRIGGER_PARAM_SOURCE_TYPE, triggerEventSourceEnum);

        //通过事件监听去处理协同触发
        dispatchTriggerFireEvent(moduleType, moduleId, affair.getFormRecordid(), map);
    }

    /**
     * 所有doTrigger接口，都采用事件监听的方式来处理，避免因为事务问题导致在解析和执行过程中数据不一致
     */
    private void dispatchTriggerFireEvent(int moduleType, long moduleId, long dataId, Map<String, Object> map) {
        //通过事件监听去处理协同触发
        CAP4FormTriggerFireEvent event = new CAP4FormTriggerFireEvent(this);
        event.setModuleType(moduleType);
        event.setModuleId(moduleId);
        event.setMasterId(dataId);
        event.setParam(map);
        EventDispatcher.fireEventAfterCommit(event);
    }

    /**
     * 所有doTrigger接口都通过事件监听的方式去执行
     */
    @Override
    @ListenEvent(event = CAP4FormTriggerFireEvent.class, async = true, mode = EventTriggerMode.afterCommit)
    public void doTriggerAfterCommit(CAP4FormTriggerFireEvent fireEvent) throws BusinessException, SQLException {
        this.doTrigger(fireEvent.getMasterId(), fireEvent.getModuleType(), fireEvent.getModuleId(), fireEvent.getParam());
    }

    /**
     * 执行触发 私有方法
     *
     * @param dataId     主数据记录 id
     * @param moduleType 模块type
     * @param moduleId   模块ID
     * @param map        需要的附加参数 conditionState:TriggerConditionState,触发条件与流程相关：流程结束或者核定节点通过时
     */
    private void doTrigger(Long dataId, int moduleType, long moduleId, Map<String, Object> map) throws BusinessException {
        Long formId = (Long) map.get(FormTriggerConstant.TRIGGER_PARAM_FORM_ID);
        FormBean formBean = cap4FormCacheManager.getForm(formId);
        if (formBean == null) {
            LOGGER.warn("表单缓存不存在，表单正在初始化或者已被删除！" + formId);
            return;
        }
        if (Strings.isEmpty(formBean.getFormTriggerIdList())) {
            LOGGER.warn("表单 ID " + formBean.getId() + " ;表单 名称" + formBean.getFormName() + " 没有触发设置");
            return;
        }

        Map<String, Object> param = new HashMap<String, Object>();
        param.putAll(map);
        // 异步执行时当前线程中用户是空的，需要从参数中取
        Long currentUserId = (Long) map.get("current_user_id");
        if (currentUserId == null) {
            currentUserId = AppContext.currentUserId();
            map.put(FormTriggerConstant.TRIGGER_PARAM_USER_LOCALE, AppContext.getCurrentUser().getLocale());
            map.put(FormTriggerConstant.TRIGGER_PARAM_USER_IPADDR, AppContext.getRemoteAddr());
        }
        param.put(FormTriggerConstant.TRIGGER_PARAM_USER_ID, currentUserId);
        LOGGER.info("表单 ID " + formBean.getId() + " ;表单 名称" + formBean.getFormName() + "数据ID " + dataId + "发起触发执行事件……");
        CAPFormTriggerEvent event = new CAPFormTriggerEvent();
        event.setNewId();
        event.setState(0);
        event.setModuleType(moduleType);
        event.setModuleId(moduleId);
        event.setFormId(formBean.getId());
        event.setFormName(formBean.getFormName());
        event.setMasterId(dataId);
        event.setUserId(currentUserId);
        event.setCreateTime(new Date());
        String title = param.get(FormTriggerConstant.TRIGGER_PARAM_TITLE) == null ? formBean.getFormName() : param.get(FormTriggerConstant.TRIGGER_PARAM_TITLE).toString();
        event.setTitle(title);
        //事件来源
        String sourceFrom = param.get(FormTriggerConstant.TRIGGER_PARAM_SOURCE_TYPE) == null ? FormTriggerBean.TriggerEventSourceEnum.outerInterface.getKey() : param.get(FormTriggerConstant.TRIGGER_PARAM_SOURCE_TYPE).toString();
        event.setSourceType(sourceFrom);
        //当前表单所属应用ID
        Long groupId = 0L;
        BusinessManager businessManager4 = (BusinessManager) AppContext.getBean("businessManager4");
        BizConfigBean bizConfigBean = businessManager4.findBizConfigByFormId(formBean.getId());
        if (bizConfigBean != null) {
            groupId = bizConfigBean.getId();
        }
        event.setGroupId(groupId);

        //去掉param中的对象键值对和枚举键值对，并将其转换为json存储
        Map<String, Object> par = new HashMap<String, Object>();
        par.putAll(param);
        par.remove(FormTriggerConstant.TRIGGER_PARAM_CONTENT);
        par.remove(FormTriggerConstant.TRIGGER_PARAM_MASTER_BEAN);
        par.remove(FormTriggerConstant.TRIGGER_PARAM_FORM_BEAN);
        par.remove(FormTriggerConstant.TRIGGER_PARAM_CONDITION_STATE);
        event.setExtendAttr(JSONUtil.toJSONString(par));
        //枚举存其key
        if (param.get(FormTriggerConstant.TRIGGER_PARAM_CONDITION_STATE) != null) {
            event.setConditionState(((FormTriggerBean.TriggerPoint) param.get(FormTriggerConstant.TRIGGER_PARAM_CONDITION_STATE)).getKey());
        }
        //加入调度
        taskExecuteManager.addTaskEvent(event);
    }

    /**
     * 调度event解析的时候，过滤出数据需要执行的触发设置
     * 无流程用当前表单设置中的所有触发设置；有流程根据执行记录来查询，并合并新增的触发设置
     */
    @Override
    public List<FormTriggerBean> getNeedExecTriggerList(FormBean sourceForm, long masterId) throws BusinessException {
        List<FormTriggerBean> needExecList = new LinkedList<FormTriggerBean>();
        List<Long> formTriggerIdList = new ArrayList<Long>(sourceForm.getFormTriggerIdList());
        List<CAP4FormTriggerRecord> recordList = new ArrayList<CAP4FormTriggerRecord>();
        //无流程的直接取表单的，有流程的要做判断
        if (sourceForm.isFlowForm()) {
            recordList = getRecordList(FormTriggerUtil.getTriggerRecordName(sourceForm), masterId);
        }
        //如果该表单下的触发设置在当前数据记录ID下，触发记录表有相应记录，则用记录表中的，否则直接用表单里的
        if (Strings.isNotEmpty(recordList)) {
            FormTriggerBean temp;
            Map<Long, List<CAP4FormTriggerRecord>> triggerRecordMap = new HashMap<Long, List<CAP4FormTriggerRecord>>();
            for (CAP4FormTriggerRecord record : recordList) {
                long triggerId = record.getTriggerId();
                temp = cap4FormCacheManager.getFormTriggerBean(triggerId);
                if (temp == null || !sourceForm.getFormTriggerIdList().contains(triggerId)) {
                    //源触发已经被删除，不再执行
                    sourceForm.removeFormTriggerSet(triggerId);
                    continue;
                }
                Strings.addToMap(triggerRecordMap, triggerId, record);
            }
            FormTriggerBean tempTriggerBean;
            //循环分类的Map，筛选出需要执行的动作
            for (Map.Entry<Long, List<CAP4FormTriggerRecord>> entry : triggerRecordMap.entrySet()) {
                CAP4FormTriggerRecord tempRecord = null;
                //TODO 这个contentDesc在新的record里面无法按照cap3的逻辑使用，因为加出来的最后一次执行的record有可能不是回写，cap4里面的触发都可以设置多个
                StringBuilder contentDesc = new StringBuilder();
                for (CAP4FormTriggerRecord record : entry.getValue()) {
                    if (Strings.isNotEmpty(record.getTriggerContentDesc())) {
                        String desc = record.getTriggerContentDesc();
                        if (desc.contains("|")) {
                            desc = desc.split("\\|")[0];
                        }
                        contentDesc.append(desc).append("|");
                    }
                    //只取时间最近的那一条执行记录来进行判断
                    if (tempRecord == null || record.getLastTriggerTime().compareTo(tempRecord.getLastTriggerTime()) > 0) {
                        tempRecord = record;
                    }
                }
                if (tempRecord == null) {
                    continue;
                }
                tempTriggerBean = new FormTriggerBean(tempRecord.getTriggerJson());
                temp = cap4FormCacheManager.getFormTriggerBean(tempRecord.getTriggerId());
                tempTriggerBean.putExtraAttr(FormTriggerConstant.TRIGGER_PARAM_TRIGGER_CONTENT_DESC + "_" + masterId, contentDesc.toString());
                if (FormTriggerBean.TriggerPoint.FlowState_PerRatify.getKey().equals(tempTriggerBean.getTriggerPoint())) {
                    //每次核定通过
                    needExecList.add(tempTriggerBean);
                    if (tempTriggerBean.getState() != temp.getState()) {
                        LOGGER.info("触发状态被修改！" + tempTriggerBean.getState() + " -> " + temp.getState());
                    }
                    tempTriggerBean.setState(temp.getState());
                    formTriggerIdList.remove(tempTriggerBean.getId());
                } else {
                    //非每次核定通过的
                    if (tempRecord.getState() == FormTriggerBean.TriggerState.TRIGGERED.getKey()) {
                        //已经执行的，需要过滤掉
                        formTriggerIdList.remove(tempTriggerBean.getId());
                    } else {
                        needExecList.add(tempTriggerBean);
                        if (tempTriggerBean.getState() != temp.getState()) {
                            LOGGER.info("触发状态被修改！" + tempTriggerBean.getState() + " -> " + temp.getState());
                        }
                        tempTriggerBean.setState(temp.getState());
                        formTriggerIdList.remove(tempTriggerBean.getId());
                    }
                }
            }
            for (Long triggerId : formTriggerIdList) {
                FormTriggerBean bean = cap4FormCacheManager.getFormTriggerBean(triggerId);
                needExecList.add(bean);
            }
        } else {
            for (Long triggerId : formTriggerIdList) {
                FormTriggerBean bean = cap4FormCacheManager.getFormTriggerBean(triggerId);
                if (bean == null) {
                    sourceForm.removeFormTriggerSet(triggerId);
                    continue;
                }
                needExecList.add(bean);
            }
        }
        return needExecList;
    }

    /**
     * 根据表名和数据ID，查询此数据的执行记录 私有方法，用于event解析时过滤不需要执行的触发设置
     */
    private List<CAP4FormTriggerRecord> getRecordList(String tableName, long masterId) throws BusinessException {
        return cap4FormTriggerRecordDAO.getRecordListByMasterId(tableName, masterId);
    }

    /**
     * 查询指定数据ID，指定动作ID的执行记录 用于在getContext的时候判断此数据此动作是否需要执行
     */
    @Override
    public List<CAP4FormTriggerRecord> getRecordList(FormBean sourceForm, Long masterId, Long triggerId, Long actionId) throws BusinessException {
        return cap4FormTriggerRecordDAO.getRecordList(FormTriggerUtil.getTriggerRecordName(sourceForm), masterId, triggerId, actionId);
    }

    /**
     * 反馈执行时，查询分发的数据的执行记录
     */
    @Override
    public List<CAP4FormTriggerRecord> getRecordList(FormBean sourceForm, Long triggerId, Long contentId, String actionType) throws BusinessException {
        return cap4FormTriggerRecordDAO.getRecordList(FormTriggerUtil.getTriggerRecordName(sourceForm), triggerId, contentId, actionType);
    }

    @Override
    public List<CAP4FormTriggerRecord> getRecordList(FormBean sourceForm, Long contentId, String actionType) throws BusinessException {
        return cap4FormTriggerRecordDAO.getRecordList(FormTriggerUtil.getTriggerRecordName(sourceForm), contentId, actionType);
    }

    @Override
    public int saveOrUpdate(FormBean sourceForm, CAP4FormTriggerRecord record) throws BusinessException {
        return cap4FormTriggerRecordDAO.saveOrUpdate(FormTriggerUtil.getTriggerRecordName(sourceForm), record);
    }

    @Override
    public int saveOrUpdate(FormBean sourceForm, List<CAP4FormTriggerRecord> recordList) throws BusinessException {
        return cap4FormTriggerRecordDAO.saveOrUpdate(FormTriggerUtil.getTriggerRecordName(sourceForm), recordList);
    }

    @Override
    public void saveAll(FormBean sourceForm, List<CAP4FormTriggerRecord> recordList) throws BusinessException {
        cap4FormTriggerRecordDAO.saveAll(FormTriggerUtil.getTriggerRecordName(sourceForm), recordList);
    }

    /**
     * 将触发加入到时间调度 根据调度时间进行分类
     * 此方法是计算调度时间，调度逻辑FormTriggerExecuteWorker.addQuartzJob 以及修改触发设置FormTriggerManagerImpl.updateQuartzJob 的时候才对调用
     * 调度表中groupName——triggerID  triggerName——formId_date
     * <p>
     * 将触发加入到时间调度 根据调度时间进行分类  此方法是新增调度
     * 根据其传入的调度时间，将当前数据新增一个调度或者加入到已存在的调度中，
     * 同时还需要判断此数据是否已经存在过调度，如果已经存在过，需要将其从已存在的调度中移除
     * 调度表中groupName——triggerID  triggerName——formId_date
     * TRIGGER_PARAM_DATE_DATAIDS    : dataId, dataId, dataId, dataId
     * TRIGGER_PARAM_Date_SubDataIds : dataId:subId,subId,subId @@@ dataId:subId,subId,subId @@@ dataId:subId,subId,subId
     * TRIGGER_PARAM_USER_ID         : dataId_memId,dataId_memId,dataId_memId,dataId_memId
     * <p>
     * 每一个job需要的参数具体有：form_id, trigger_id, dataId, moduleId, moduleType, userId, subDataIds, subTableName
     */
    @Override
    public void addTriggerQuartzJob(Map<String, String> map, FormDataMasterBean masterBean, FormTriggerConditionBean dateCondition) throws Exception {
        FormFormulaBean dateFormula = dateCondition.getConditionFormula();
        if (dateFormula == null) {
            return;
        }
        Map<String, Object> dataMap = masterBean.getFormulaMap(FormulaEnums.formulaType_datetime);

        //calcDate(start_date,1)
        //calcDateByWorkDay( {表单字段} , 0 )
        //month|calcDateByWorkDay([PerMonthEnd],0)|9:00
        //season|calcDateByWorkDay([PerSeasonEnd],-2)|11:00
        //once|calcDate([SpecifiedTime]2013-03-22,0)|11:00   //calcDate无所谓

        String formulaStr = dateFormula.getExecuteFormulaForGroove();
        if (Strings.isBlank(formulaStr)) {
            return;
        }
        LOGGER.info("触发1：時間: " + formulaStr);

        String groupName = map.get(FormTriggerConstant.TRIGGER_PARAM_TRIGGER_ID);
        String dataId = map.get(FormTriggerConstant.TRIGGER_PARAM_MASTER_ID);
        removeDataParamInQuartzJob(groupName, dataId);// 将此数据的相关ID从已存在的调度的参数中移除

        String jobName = map.get(FormTriggerConstant.TRIGGER_PARAM_MASTER_ID);
        //指定日期
        if (formulaStr.contains("SpecifiedTime")) {
            Matcher m = datePattern.matcher(formulaStr);

            m.find();
            String dateStr = m.group();
            Date date = Datetimes.parse(dateStr, Datetimes.dateStyle);
            FormTriggerBean.TimeFrequency timeFrequency = FormTriggerBean.TimeFrequency.getEnumByKey(dateCondition.getTimeFrequency());
            addTriggerQuartzJob(jobName, false, map, date, timeFrequency, dateCondition);
        } else {
            if (formulaStr.contains(FormTriggerBean.TimeFrequency.PerMonthBegin.getKey())) {
                //每月初
                dataMap.put(FormTriggerBean.TimeFrequency.PerMonthBegin.getKey(), Datetimes.getFirstDayInMonth(new Date()));
                Date date = (Date) FormulaUtil.doResult(formulaStr, dataMap);
                addTriggerQuartzJob(jobName, false, map, date, FormTriggerBean.TimeFrequency.PerMonthBegin, dateCondition);

            } else if (formulaStr.contains(FormTriggerBean.TimeFrequency.PerMonthEnd.getKey())) {
                //每月末
                dataMap.put(FormTriggerBean.TimeFrequency.PerMonthEnd.getKey(), Datetimes.getLastDayInMonth(new Date()));
                Date date = (Date) FormulaUtil.doResult(formulaStr, dataMap);
                addTriggerQuartzJob(jobName, false, map, date, FormTriggerBean.TimeFrequency.PerMonthEnd, dateCondition);

            } else if (formulaStr.contains(FormTriggerBean.TimeFrequency.PerSeasonBegin.getKey())) {
                //每季度初
                dataMap.put(FormTriggerBean.TimeFrequency.PerSeasonBegin.getKey(), Datetimes.getFirstDayInSeason(new Date()));
                Date date = (Date) FormulaUtil.doResult(formulaStr, dataMap);
                addTriggerQuartzJob(jobName, false, map, date, FormTriggerBean.TimeFrequency.PerSeasonBegin, dateCondition);

            } else if (formulaStr.contains(FormTriggerBean.TimeFrequency.PerSeasonEnd.getKey())) {
                //每季度末
                dataMap.put(FormTriggerBean.TimeFrequency.PerSeasonEnd.getKey(), Datetimes.getLastDayInSeason(new Date()));
                Date date = (Date) FormulaUtil.doResult(formulaStr, dataMap);
                addTriggerQuartzJob(jobName, false, map, date, FormTriggerBean.TimeFrequency.PerSeasonEnd, dateCondition);

            } else if (formulaStr.contains(FormTriggerBean.TimeFrequency.PerHalfYearBegin.getKey())) {
                //每半年初 01-01/07-01
                Calendar c = Calendar.getInstance();
                int month = c.get(Calendar.MONTH);
                c.set(Calendar.DAY_OF_MONTH, 1);
                Date date;
                if (month <= Calendar.JUNE) {
                    c.set(Calendar.MONTH, Calendar.FEBRUARY);
                    date = c.getTime();
                } else {
                    c.set(Calendar.MONTH, Calendar.AUGUST);
                    date = c.getTime();
                }
                dataMap.put(FormTriggerBean.TimeFrequency.PerHalfYearBegin.getKey(), Datetimes.getFirstDayInSeason(date));
                Date date1 = (Date) FormulaUtil.doResult(formulaStr, dataMap);
                addTriggerQuartzJob(jobName, false, map, date1, FormTriggerBean.TimeFrequency.PerHalfYearBegin, dateCondition);

            } else if (formulaStr.contains(FormTriggerBean.TimeFrequency.PerHalfYearEnd.getKey())) {
                //每半年末 06-30/12-21
                Calendar c = Calendar.getInstance();
                int month = c.get(Calendar.MONTH);
                c.set(Calendar.DAY_OF_MONTH, 1);
                Date date;
                if (month <= Calendar.JUNE) {
                    c.set(Calendar.MONTH, Calendar.MAY);
                    date = c.getTime();
                } else {
                    c.set(Calendar.MONTH, Calendar.NOVEMBER);
                    date = c.getTime();
                }
                dataMap.put(FormTriggerBean.TimeFrequency.PerHalfYearEnd.getKey(), Datetimes.getLastDayInSeason(date));
                Date date1 = (Date) FormulaUtil.doResult(formulaStr, dataMap);
                addTriggerQuartzJob(jobName, false, map, date1, FormTriggerBean.TimeFrequency.PerHalfYearEnd, dateCondition);

            } else if (formulaStr.contains(FormTriggerBean.TimeFrequency.PerYearBegin.getKey())) {
                //每年初 01-01
                dataMap.put(FormTriggerBean.TimeFrequency.PerYearBegin.getKey(), Datetimes.getFirstDayInYear(new Date()));
                Date date = (Date) FormulaUtil.doResult(formulaStr, dataMap);
                addTriggerQuartzJob(jobName, false, map, date, FormTriggerBean.TimeFrequency.year, dateCondition);

            } else if (formulaStr.contains(FormTriggerBean.TimeFrequency.PerYearEnd.getKey())) {
                //每年末 12-31
                dataMap.put(FormTriggerBean.TimeFrequency.PerYearEnd.getKey(), Datetimes.getLastDayInYear(new Date()));
                Date date = (Date) FormulaUtil.doResult(formulaStr, dataMap);
                addTriggerQuartzJob(jobName, false, map, date, FormTriggerBean.TimeFrequency.year, dateCondition);

            } else {
                //表单字段作为发起时间
                FormTriggerBean.TimeFrequency timeFrequency = FormTriggerBean.TimeFrequency.getEnumByKey(dateCondition.getTimeFrequency());
                String fieldName = org.apache.commons.lang.StringUtils.substringBetween(formulaStr, "(", ",");

                FormFieldBean fieldBean = null;
                try {
                    //先根据表单id找到表单然后再找字段，如果表单没找到则根据数据对象找字段
                    String formId = map.get(FormTriggerConstant.TRIGGER_PARAM_FORM_ID);
                    if (StringUtil.checkNull(formId)) {
                        fieldBean = masterBean.getFieldBeanByFieldName(fieldName);
                    } else {
                        FormBean formBean = cap4FormCacheManager.getForm(Long.valueOf(formId));
                        if (formBean == null) {
                            fieldBean = masterBean.getFieldBeanByFieldName(fieldName);
                        } else {
                            fieldBean = formBean.getFieldBeanByName(fieldName);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("获取字段异常，表单 " + map.get(FormTriggerConstant.TRIGGER_PARAM_FORM_ID) + " 数据 " + masterBean.getId() + " 字段 " + fieldName);
                }
                if (fieldBean != null && fieldBean.isSubField()) {
                    //重复表字段
                    FormTriggerBean triggerBean = dateCondition.getFormTriggerBean();
                    String filterSubTableName = triggerBean.getFilterConditionSubTableName();
                    String formId = map.get(FormTriggerConstant.TRIGGER_PARAM_FORM_ID);
                    FormBean formBean = cap4FormCacheManager.getForm(Long.valueOf(formId));
                    List<Long> triggeredSubDataIds = cap4FormTriggerRecordDAO.getTriggeredSubDataIds(FormTriggerUtil.getTriggerRecordName(formBean), masterBean.getId(), triggerBean.getId(), FormTriggerBean.TriggerState.TRIGGERED.getKey());
                    //根据条件过滤满足条件的重复行
                    String ownerTableName = fieldBean.getOwnerTableName();
                    List<FormDataSubBean> subBeanList = masterBean.getSubData(ownerTableName);
                    subBeanList = FormTriggerUtil.filterSubBeansByFilterCondition(triggerBean, masterBean, subBeanList, ownerTableName, filterSubTableName);
                    //key: 触发日期yyyy-MM-dd, value: subDataIds
                    Map<String, List<String>> triggerSubBeanMap = new HashMap<String, List<String>>();
                    for (FormDataSubBean subBean : subBeanList) {
                        //包含，说明已经触发
                        if (triggeredSubDataIds.contains(subBean.getId()) && timeFrequency == FormTriggerBean.TimeFrequency.once && triggerBean.isConditionFirstMet()) {
                            continue;
                        }
                        dataMap.putAll(subBean.getRowData());
                        Date date = (Date) FormulaUtil.doResult(formulaStr, dataMap);
                        if (date == null) {
                            continue;
                        }
                        Strings.addToMap(triggerSubBeanMap, Datetimes.formatDate(date), String.valueOf(subBean.getId()));
                    }
                    map.put(FormTriggerConstant.TRIGGER_PARAM_DATE_SUBTABLENAME, ownerTableName);
                    map.put("formField", "isSubField");
                    for (Map.Entry<String, List<String>> entry : triggerSubBeanMap.entrySet()) {
                        Date date = Datetimes.parseDate(entry.getKey());
                        String subDataStr = Strings.join(entry.getValue(), ",");
                        LOGGER.info("jobName: " + jobName + " subDataIds1---: " + subDataStr);
                        map.put(FormTriggerConstant.TRIGGER_PARAM_DATE_SUBDATAIDS, subDataStr);
                        Map<String, String> paramMap = new HashMap<String, String>(map);
                        addTriggerQuartzJob(jobName, true, paramMap, date, timeFrequency, dateCondition);
                    }
                } else {
                    if (fieldBean != null || fieldName.equals(Enums.MasterTableField.start_date.getKey())) {
                        Date date = (Date) FormulaUtil.doResult(formulaStr, dataMap);
                        addTriggerQuartzJob(jobName, false, map, date, timeFrequency, dateCondition);
                    } else {
                        LOGGER.error(map.get(FormTriggerConstant.TRIGGER_PARAM_FORM_ID) + ",修改表单更新表单数据触发时，触发时间点为重复表字段，耗性能，暂时不更新，表单字段：" + fieldName);
                    }
                }
            }
        }
    }

    /**
     * 将此数据的ID从调度参数中移除
     */
    private void removeDataParamInQuartzJob(String groupName, String dataId) throws SchedulerException {
        //需要判断是否创建过任务了，也就是需要检查所有任务中的dataMap中是否包含当前数据ID 根据groupName即触发ID找此触发的所有的job，看当前数据ID是否在任务的dataMap中
        Scheduler sched = QuartzListener.getScheduler();
        Set<JobKey> jobNameSet = sched.getJobKeys(GroupMatcher.<JobKey>groupStartsWith(groupName));
        if (jobNameSet != null && !jobNameSet.isEmpty()) {
            for (JobKey jobKey : jobNameSet) {
                //兼容历史，将此数据的历史调度删除
                String name = jobKey.getName();
                if (name.startsWith(dataId)) {
                    QuartzHolder.deleteQuartzJobByGroupAndJobName(groupName, dataId);
                    continue;
                }
                JobDetail jd = sched.getJobDetail(JobKey.jobKey(name, groupName));
                if (jd != null) {
                    JobDataMap jobDataMap = jd.getJobDataMap();
                    String dataIds = jobDataMap.getString(FormTriggerConstant.TRIGGER_PARAM_DATE_DATAIDS);
                    if (Strings.isBlank(dataIds)) {
                        continue;
                    }
                    if (dataIds.contains(dataId)) {
                        if (jd.getJobDataMap().getString("formField") != null && "null".equals(jd.getJobDataMap().getString("formField"))) {
                            //触发日期为重复表字段
                            String oldSubDataIds = jobDataMap.getString(FormTriggerConstant.TRIGGER_PARAM_DATE_SUBDATAIDS);
                            if (oldSubDataIds.contains(dataId)) {
                                oldSubDataIds = replaceDataParam(oldSubDataIds, dataId, "@@@");
                                jobDataMap.put(FormTriggerConstant.TRIGGER_PARAM_DATE_SUBDATAIDS, oldSubDataIds);
                                LOGGER.info("jobName: " + name + " subDataIds2---: " + oldSubDataIds);
                            }
                        }
                        // 移除dataId
                        //dataIds = dataIds.replace(dataId, "");//这样替换之后，可能出现id,,id这种情况，因此需要在使用的时候做空判断
                        dataIds = replaceDataParam(dataIds, dataId, ",");
                        jobDataMap.put(FormTriggerConstant.TRIGGER_PARAM_DATE_DATAIDS, dataIds);
                        // 移除userId
                        String memIds = jobDataMap.getString(FormTriggerConstant.TRIGGER_PARAM_USER_ID);
                        memIds = replaceDataParam(memIds, dataId, ",");
                        jobDataMap.put(FormTriggerConstant.TRIGGER_PARAM_USER_ID, memIds);
                        // 移除moduleId
                        String moduleIds = jobDataMap.getString(FormTriggerConstant.TRIGGER_PARAM_DATE_MODULEIDS);
                        moduleIds = replaceDataParam(moduleIds, dataId, ",");
                        jobDataMap.put(FormTriggerConstant.TRIGGER_PARAM_MODULE_ID, moduleIds);

                        // 更新job
                        sched.addJob(jd, true, true);
                    }
                }
            }
        }
    }

    /**
     * 根据分隔符，移除原始参数中的指定字符串
     */
    private String replaceDataParam(String paramString, String dataId, String split) {
        if (paramString.contains(split)) {
            int splitLen = split.length();
            int index = paramString.indexOf(dataId);
            int beginIndex = paramString.indexOf(split, index) == -1 ? index - splitLen : index;
            int endIndex = paramString.indexOf(split, index) == -1 ? paramString.length() : paramString.indexOf(split, index) + splitLen;
            String tempStr = paramString.substring(beginIndex, endIndex);
            return paramString.replace(tempStr, "");
        }
        return "";
    }

    /**
     * 将触发加入到时间调度 根据调度时间进行分类  此方法是新增调度
     * 根据其传入的调度时间，将当前数据新增一个调度或者加入到已存在的调度中，
     * 同时还需要判断此数据是否已经存在过调度，如果已经存在过，需要将其从已存在的调度中移除
     * 调度表中groupName——triggerID  triggerName——formId_date
     * TRIGGER_PARAM_DATE_DATAIDS    : dataId, dataId, dataId, dataId
     * TRIGGER_PARAM_Date_SubDataIds : dataId:subId,subId,subId @@@ dataId:subId,subId,subId @@@ dataId:subId,subId,subId
     * TRIGGER_PARAM_USER_ID         : dataId_memId,dataId_memId,dataId_memId,dataId_memId
     */
    @SuppressWarnings("unchecked")//todo zhou
    private void addTriggerQuartzJob(String jobName, boolean needSubName, Map<String, String> map, Date date0, FormTriggerBean.TimeFrequency timeFrequency, FormTriggerConditionBean dateCondition) throws BusinessException, SchedulerException {
        FormTriggerBean triggerBean = dateCondition.getFormTriggerBean();
        if (date0 == null || timeFrequency == null) {
            LOGGER.warn("触发异常，日期[" + date0 + "]或者周期[" + timeFrequency + "]为null; FormTriggerCondition.id=" + dateCondition.getFormTriggerBean().getId());
            return;
        }

        String groupName = map.get(FormTriggerConstant.TRIGGER_PARAM_TRIGGER_ID);
        String dataId = map.get(FormTriggerConstant.TRIGGER_PARAM_MASTER_ID);
        String subDataId = map.get(FormTriggerConstant.TRIGGER_PARAM_DATE_SUBDATAIDS);
        String moduleId = map.get(FormTriggerConstant.TRIGGER_PARAM_MODULE_ID);
        String userId = map.get(FormTriggerConstant.TRIGGER_PARAM_USER_ID);
        String formId = map.get(FormTriggerConstant.TRIGGER_PARAM_FORM_ID);
        FormBean formBean = cap4FormCacheManager.getForm(Long.valueOf(formId));
        List<CAP4FormTriggerRecord> triggeredList = cap4FormTriggerRecordDAO.getRecordList(FormTriggerUtil.getTriggerRecordName(formBean), Long.valueOf(groupName), Long.valueOf(dataId));
        //首次条件满足，非重复表时间调度,且非周期性的，在此判断是否触发，如果已经触发，则不触发了
        if (triggerBean.isConditionFirstMet() && timeFrequency == FormTriggerBean.TimeFrequency.once && Strings.isNotEmpty(triggeredList)) {
            LOGGER.info("触发：" + groupName + "." + jobName + "已经执行，且非周期性，不加入时间调度！");
            return;
        }
//todo todozhou
//        String result = Datetimes.format(date0, Datetimes.dateStyle) + " " + dateCondition.getTriggerTime();
//        Date date = Datetimes.parse(result, Datetimes.datetimeWithoutSecondStyle);
//        zhou:开始 徐矿——修改定时任务触发的时间，不采用页面配置的时间创建定时任务，使用截止报送时间提前一小时创建定时任务
        String key=timeFrequency.getKey();
        String result="";
        Date date=null;
        if(key.equals("once")){
            long currentTime=System.currentTimeMillis() + 3600*1000;//当前时间增加一小时用于判断截止报送时间能不能提前一小时发送提醒
            long stopSendTime=date0.getTime();

            if(stopSendTime>currentTime){
                long minus1Hour=date0.getTime()-60*60*1000;
                Date _minusHourDate= new Date(minus1Hour);
                result=Datetimes.format(_minusHourDate, Datetimes.datetimeWithoutSecondStyle);
                date= Datetimes.parse(result, Datetimes.datetimeWithoutSecondStyle);
            } else {
                result=Datetimes.format(date0, Datetimes.datetimeWithoutSecondStyle);
                date= Datetimes.parse(result, Datetimes.datetimeWithoutSecondStyle);
            }
        }else {
            result = Datetimes.format(date0, Datetimes.dateStyle) + " " + dateCondition.getTriggerTime();
            date = Datetimes.parse(result, Datetimes.datetimeWithoutSecondStyle);
        }


//        zhou:结束--------------------------------------------------------------------


        LOGGER.info("触发2：" + jobName + " 類型：" + timeFrequency + ", 時間: " + date);

        //jobName = 表单ID_定时任务的日期
        jobName = map.get(FormTriggerConstant.TRIGGER_PARAM_FORM_ID) + "_" + Datetimes.format(date, Datetimes.datetimeStyle);
        map.put(FormTriggerConstant.TRIGGER_PARAM_TRIGGER_JOB_NAME, jobName);

        //存在同一时间任务，则将已存在的任务的数据ID查询出来，加上当前数据的ID，否则直接赋值任务的数据ID为当前数据的ID
        JobDetail jobDetail = QuartzListener.getScheduler().getJobDetail(JobKey.jobKey(jobName, groupName));
        if (null != jobDetail) {
            String dataIds = jobDetail.getJobDataMap().getString(FormTriggerConstant.TRIGGER_PARAM_DATE_DATAIDS);
            dataIds = Strings.isBlank(dataIds) ? dataId : dataIds.contains(dataId) ? dataIds : dataIds + "," + dataId;
            map.put(FormTriggerConstant.TRIGGER_PARAM_DATE_DATAIDS, dataIds);

            if (needSubName) {
                String subDataIds = jobDetail.getJobDataMap().getString(FormTriggerConstant.TRIGGER_PARAM_DATE_SUBDATAIDS);
                String tempSubDataIds = dataId + ":" + subDataId;
                subDataIds = Strings.isBlank(subDataIds) ? tempSubDataIds : subDataIds + "@@@" + tempSubDataIds;
                map.put(FormTriggerConstant.TRIGGER_PARAM_DATE_SUBDATAIDS, subDataIds);
                LOGGER.info("jobName: " + jobName + " subDataIds3---: " + subDataIds);
            }

            String memIds = jobDetail.getJobDataMap().getString(FormTriggerConstant.TRIGGER_PARAM_USER_ID);
            String tempMedIds = dataId + "_" + userId;
            memIds = Strings.isBlank(memIds) ? tempMedIds : memIds + "," + tempMedIds;
            map.put(FormTriggerConstant.TRIGGER_PARAM_USER_ID, memIds);

            String moduleIds = jobDetail.getJobDataMap().getString(FormTriggerConstant.TRIGGER_PARAM_DATE_MODULEIDS);
            String tempModuleIds = dataId + "_" + moduleId;
            moduleIds = Strings.isBlank(moduleIds) ? tempModuleIds : moduleIds + "," + tempModuleIds;
            map.put(FormTriggerConstant.TRIGGER_PARAM_DATE_MODULEIDS, moduleIds);

            QuartzHolder.deleteQuartzJobByGroupAndJobName(groupName, jobName);
        } else {
            map.put(FormTriggerConstant.TRIGGER_PARAM_DATE_DATAIDS, dataId);
            map.put(FormTriggerConstant.TRIGGER_PARAM_DATE_SUBDATAIDS, dataId + ":" + subDataId);
            map.put(FormTriggerConstant.TRIGGER_PARAM_DATE_MODULEIDS, dataId + "_" + moduleId);
            map.put(FormTriggerConstant.TRIGGER_PARAM_USER_ID, dataId + "_" + userId);
        }

        addQuartzJob(groupName, jobName, date, map, timeFrequency);
    }

    /**
     * 修改触发设置后，删除触发或者更新触发的数据时间调度
     * 新建且有时间调度，修改且时间调度从无到有，修改且时间调度发生变化，后两种合并为修改且时间调度发生变化
     * 修改且停用直接进行删除，不管是否存在调度
     *
     * @param triggerBean 当前触发设置
     * @param isNew       是否新建
     * @param oldTimeSet  旧的调度设置字符串
     * @param newTimeSet  新的调度设置字符串
     */
    //todo zhou
    @Override
    public void updateTriggerQuartzJob(FormTriggerBean triggerBean, boolean isNew, String oldTimeSet, String newTimeSet) throws BusinessException {
        //修改且停用直接进行删除，不管是否存在调度
        if (!isNew && !triggerBean.isEnable()) {
            QuartzHolder.deleteQuartzJobByGroup(triggerBean.getId().toString());
            return;
        }

        //新建且有时间调度，修改且时间调度从无到有，修改且时间调度发生变化，后两种合并为修改且时间调度发生变化
        boolean needDoThis = (isNew && Strings.isNotBlank(newTimeSet)) || (!isNew && !oldTimeSet.equals(newTimeSet));
        if (!needDoThis) {
            return;
        }

        //创建并启动重建调度线程
        UpdateQuartzJobThread jobThread = new UpdateQuartzJobThread();
        jobThread.user = AppContext.getCurrentUser();
        jobThread.manager = this;
        jobThread.triggerBean = triggerBean;
        jobThread.sourceForm = cap4FormCacheManager.getForm(triggerBean.getSourceFormId());
        //只有修改且时间调度发生变化时才需要查询历史并删除旧调度
        jobThread.updateQuartz = !isNew && Strings.isNotBlank(oldTimeSet) && !oldTimeSet.equals(newTimeSet);
        jobThread.start();
    }

    /**
     * 更新指定触发时间调度线程
     */
    private class UpdateQuartzJobThread extends Thread {
        User user;
        boolean updateQuartz;
        FormBean sourceForm;
        FormTriggerBean triggerBean;
        CAP4FormTriggerManagerImpl manager;
        CAP4FormDataDAO cap4FormDataDAO = (CAP4FormDataDAO) AppContext.getBean("cap4FormDataDAO");

        @Override
        public void run() {
            ThreadInfoHolder.getInstance().initThread(this);
            AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY, user);

            try {
                Long start = System.currentTimeMillis();
                Map<String, String> dataMemberMap = new HashMap<String, String>();
                if (updateQuartz) {
                    //先将需要处理的调度的人员参数取出来，再将调度删除，人员参数是dataID_memID
                    Scheduler scheduler = QuartzListener.getScheduler();
                    String groupName = triggerBean.getId().toString();
                    Set<JobKey> jobNameSet = scheduler.getJobKeys(GroupMatcher.<JobKey>groupStartsWith(groupName));
                    if (jobNameSet != null && !jobNameSet.isEmpty()) {
                        JobDetail detail;
                        for (JobKey jobKey : jobNameSet) {
                            String name = jobKey.getName();
                            detail = scheduler.getJobDetail(JobKey.jobKey(name, groupName));
                            if (detail != null) {
                                @SuppressWarnings("unchecked")
                                String memIds = detail.getJobDataMap().getString(FormTriggerConstant.TRIGGER_PARAM_USER_ID);
                                if (Strings.isNotBlank(memIds)) {
                                    //需要判断是否包含","，是否包含"_"，都不包含的时候表示是历史调度，历史调度的name就是dataId
                                    if (memIds.contains(",")) {
                                        String[] memIdArr = memIds.split(",");
                                        for (String memId : memIdArr) {
                                            String[] tempArr = memId.split("_");
                                            dataMemberMap.put(groupName + tempArr[0], tempArr[tempArr.length - 1]);
                                        }
                                    } else if (memIds.contains("_")) {
                                        String[] memIdArr = memIds.split("_");
                                        dataMemberMap.put(groupName + memIdArr[0], memIdArr[memIdArr.length - 1]);
                                    } else {
                                        dataMemberMap.put(groupName + name, memIds);
                                    }
                                }
                            }
                        }
                    }
                    //直接将旧的删除掉，因为调度的时间发生变化了
                    QuartzHolder.deleteQuartzJobByGroup(groupName);
                }

                LOGGER.info("业务关系：" + triggerBean.getId() + triggerBean.getName() + " 更新时间调度开始");
                FormTriggerConditionBean dateCondition = triggerBean.getDateCondition();
                if (dateCondition == null || dateCondition.getConditionFormula() == null || Strings.isBlank(dateCondition.getConditionFormula().getExecuteFormulaForGroove())) {
                    LOGGER.info("当前触发不存在调度设置" + triggerBean.getId());
                    return;
                }

                //calcDate(start_date,1)
                //calcDateByWorkDay( {表单字段} , 0 )
                //month|calcDateByWorkDay([PerMonthEnd],0)|9:00
                //season|calcDateByWorkDay([PerSeasonEnd],-2)|11:00
                //once|calcDate([SpecifiedTime]2013-03-22,0)|11:00   //calcDate无所谓
                String formulaStr = dateCondition.getConditionFormula().getExecuteFormulaForGroove();
                LOGGER.info("触发--時間1: " + formulaStr);

                //此处先抓取非表单字段时的触发日期
                Date date = null;//通过下面的if/else获取到的 date 格式为：yyyy-MM-dd
                FormTriggerBean.TimeFrequency timeFrequency = null;
                //指定日期
                if (formulaStr.contains("SpecifiedTime")) {
                    Matcher m = datePattern.matcher(formulaStr);
                    m.find();
                    String dateStr = m.group();
                    date = Datetimes.parse(dateStr, Datetimes.dateStyle);//yyyy-MM-dd
                    timeFrequency = FormTriggerBean.TimeFrequency.getEnumByKey(dateCondition.getTimeFrequency());
                } else {
                    if (formulaStr.contains(FormTriggerBean.TimeFrequency.PerMonthBegin.getKey())) {
                        //每月初
                        date = Datetimes.getFirstDayInMonth(new Date());
                        timeFrequency = FormTriggerBean.TimeFrequency.PerMonthBegin;

                    } else if (formulaStr.contains(FormTriggerBean.TimeFrequency.PerMonthEnd.getKey())) {
                        //每月末
                        date = Datetimes.getLastDayInMonth(new Date());
                        timeFrequency = FormTriggerBean.TimeFrequency.PerMonthEnd;

                    } else if (formulaStr.contains(FormTriggerBean.TimeFrequency.PerSeasonBegin.getKey())) {
                        //每季度初
                        date = Datetimes.getFirstDayInSeason(new Date());
                        timeFrequency = FormTriggerBean.TimeFrequency.PerSeasonBegin;

                    } else if (formulaStr.contains(FormTriggerBean.TimeFrequency.PerSeasonEnd.getKey())) {
                        //每季度末
                        date = Datetimes.getLastDayInSeason(new Date());
                        timeFrequency = FormTriggerBean.TimeFrequency.PerSeasonEnd;

                    } else if (formulaStr.contains(FormTriggerBean.TimeFrequency.PerHalfYearBegin.getKey())) {
                        //每半年初 01-01/07-01
                        Calendar c = Calendar.getInstance();
                        int month = c.get(Calendar.MONTH);
                        c.set(Calendar.DAY_OF_MONTH, 1);
                        Date tempDate;
                        if (month <= Calendar.JUNE) {
                            c.set(Calendar.MONTH, Calendar.FEBRUARY);
                            tempDate = c.getTime();
                        } else {
                            c.set(Calendar.MONTH, Calendar.AUGUST);
                            tempDate = c.getTime();
                        }
                        date = Datetimes.getFirstDayInSeason(tempDate);
                        timeFrequency = FormTriggerBean.TimeFrequency.PerHalfYearBegin;

                    } else if (formulaStr.contains(FormTriggerBean.TimeFrequency.PerHalfYearEnd.getKey())) {
                        //每半年末 06-30/12-21
                        Calendar c = Calendar.getInstance();
                        int month = c.get(Calendar.MONTH);
                        c.set(Calendar.DAY_OF_MONTH, 1);
                        Date tempDate;
                        if (month <= Calendar.JUNE) {
                            c.set(Calendar.MONTH, Calendar.MAY);
                            tempDate = c.getTime();
                        } else {
                            c.set(Calendar.MONTH, Calendar.NOVEMBER);
                            tempDate = c.getTime();
                        }
                        date = Datetimes.getLastDayInSeason(tempDate);
                        timeFrequency = FormTriggerBean.TimeFrequency.PerHalfYearEnd;

                    } else if (formulaStr.contains(FormTriggerBean.TimeFrequency.PerYearBegin.getKey())) {
                        //每年初 01-01
                        date = Datetimes.getFirstDayInYear(new Date());
                        timeFrequency = FormTriggerBean.TimeFrequency.PerYearBegin;

                    } else if (formulaStr.contains(FormTriggerBean.TimeFrequency.PerYearEnd.getKey())) {
                        //每年末 12-31
                        date = Datetimes.getLastDayInYear(new Date());
                        timeFrequency = FormTriggerBean.TimeFrequency.PerYearEnd;
                    }
                }

                Map<String, Map<String, String>> allDataJobParamMap = new HashMap<String, Map<String, String>>();
                FlipInfo fi = new FlipInfo();
                fi.setSize(900);
                //fi.setSize(1);
                int count = cap4FormDataDAO.selectDataCount(sourceForm.getMasterTableBean().getTableName());
                fi.setTotal(count);
                int page = fi.getPages();
////zhou 暂时注释掉
                for (int i = 1; i <= page; i++) {
                    fi.setPage(i);
                    List<FormDataMasterBean> dataList = cap4FormDataDAO.selectMasterDataList(sourceForm, fi);
                    timeFrequency = analysisTimeSet(allDataJobParamMap, sourceForm, dateCondition, formulaStr, timeFrequency, date, dataList, dataMemberMap);
                }

                String moduleType = sourceForm.isFlowForm() ? ModuleType.cap4Form.getKey() + "" : ModuleType.cap4UnflowForm.getKey() + "";
                for (Map.Entry<String, Map<String, String>> entry : allDataJobParamMap.entrySet()) {
                    Map<String, String> jobParam = entry.getValue();
                    jobParam.put(FormTriggerConstant.TRIGGER_PARAM_MODULE_TYPE, moduleType);
                    jobParam.put(FormTriggerConstant.TRIGGER_PARAM_FORM_ID, String.valueOf(sourceForm.getId()));
                    jobParam.put(FormTriggerConstant.TRIGGER_PARAM_TRIGGER_ID, String.valueOf(triggerBean.getId()));

                    addQuartzJob(String.valueOf(triggerBean.getId()), jobParam.get(FormTriggerConstant.TRIGGER_PARAM_TRIGGER_JOB_NAME), Datetimes.parse(entry.getKey(), Datetimes.datetimeStyle), jobParam, timeFrequency);
                }

                Long end = System.currentTimeMillis();
                LOGGER.info("更新时间调度结束 " + triggerBean.getName() + " 耗时:" + (end - start));
            } catch (Exception e) {
                LOGGER.error("更新时间调度发生错误", e);
            }

            ThreadInfoHolder.getInstance().remove(this);
        }
    }

    /**
     * 解析时间调度设置，并构造quartz需要的参数
     */
    private FormTriggerBean.TimeFrequency analysisTimeSet(Map<String, Map<String, String>> allDataJobParamMap, FormBean sourceForm, FormTriggerConditionBean dateCondition, String formulaStr, FormTriggerBean.TimeFrequency timeFrequency, Date date, List<FormDataMasterBean> dataList, Map<String, String> dataMemberMap) throws BusinessException {
        FormTriggerBean triggerBean = dateCondition.getFormTriggerBean();

        Date resultDate;
        String triggerTime = dateCondition.getTriggerTime();//触发时间，调度设置中的具体的 小时:分钟
        if (triggerTime.length() == 4) {
            triggerTime = "0" + triggerTime;
        }
        CAP4FormTriggerRecordDAO triggerRecordDAO = (CAP4FormTriggerRecordDAO) AppContext.getBean("cap4FormTriggerRecordDAO");
        for (FormDataMasterBean dataBean : dataList) {
            if (!triggerBean.isMatchCondition(dataBean, null)) {
                LOGGER.info("更新调度 数据：" + dataBean.getId() + " 不满足条件");
                continue;
            }

            String dataId = String.valueOf(dataBean.getId());
            String memberId = dataMemberMap.get(String.valueOf(triggerBean.getId()) + dataId);
            memberId = Strings.isNotBlank(memberId) ? memberId : dataBean.getStartMemberId() + "";
            memberId = memberId.replace(dataId + "_", "");

            Map<String, Object> dataMap = dataBean.getFormulaMap(FormulaEnums.formulaType_datetime);
            //date非空表示触发日期非表单字段
            if (date != null) {
                //首次条件满足，非重复表时间调度,且非周期性的，在此判断是否触发，如果已经触发，则不触发了  处理为两次判断，首次且一次性的时候再查询执行记录
                if (triggerBean.isConditionFirstMet() && timeFrequency == FormTriggerBean.TimeFrequency.once) {
                    //此判断放到具体的分支中去执行，因为选择表单控件的时候，其timeFrequency在传入的时候是null的
                    List<CAP4FormTriggerRecord> triggeredList = triggerRecordDAO.getRecordList(FormTriggerUtil.getTriggerRecordName(sourceForm), triggerBean.getId(), dataBean.getId());
                    if (Strings.isNotEmpty(triggeredList)) {
                        LOGGER.info("数据：" + dataBean.getId() + " 触发：" + triggerBean.getId() + " 已经执行，且非周期性，不加入时间调度！");
                        continue;
                    }
                }
                if (formulaStr.contains("SpecifiedTime")) {
                    //指定日期
                    resultDate = date;
                } else {
                    //非指定日期
                    dataMap.put(timeFrequency.getKey(), date);
                    resultDate = (Date) FormulaUtil.doResult(formulaStr, dataMap);
                }
                addDataParam2JobMap(allDataJobParamMap, resultDate, sourceForm.getId(), dataId, memberId, null, null, triggerTime);
            } else {
                //表单字段作为触发日期
                timeFrequency = FormTriggerBean.TimeFrequency.getEnumByKey(dateCondition.getTimeFrequency());
                //首次条件满足，非重复表时间调度,且非周期性的，在此判断是否触发，如果已经触发，则不触发了  处理为两次判断，首次且一次性的时候再查询执行记录
                if (triggerBean.isConditionFirstMet() && timeFrequency == FormTriggerBean.TimeFrequency.once) {
                    List<CAP4FormTriggerRecord> triggeredList = triggerRecordDAO.getRecordList(FormTriggerUtil.getTriggerRecordName(sourceForm), triggerBean.getId(), dataBean.getId());
                    if (Strings.isNotEmpty(triggeredList)) {
                        LOGGER.info("数据：" + dataBean.getId() + " 触发：" + triggerBean.getId() + " 已经执行，且非周期性，不加入时间调度！");
                        continue;
                    }
                }
                String fieldName = org.apache.commons.lang.StringUtils.substringBetween(formulaStr, "(", ",");
                FormFieldBean fieldBean = sourceForm.getFieldBeanByName(fieldName);
                if (fieldBean != null && fieldBean.isSubField()) {
                    String filterSubTableName = triggerBean.getFilterConditionSubTableName();
                    List<Long> triggeredSubDataIds = triggerRecordDAO.getTriggeredSubDataIds(FormTriggerUtil.getTriggerRecordName(sourceForm), triggerBean.getId(), dataBean.getId(), FormTriggerBean.TriggerState.TRIGGERED.getKey());
                    //根据条件过滤满足条件的重复行
                    String ownerTableName = fieldBean.getOwnerTableName();
                    List<FormDataSubBean> subBeanList = dataBean.getSubData(ownerTableName);
                    subBeanList = FormTriggerUtil.filterSubBeansByFilterCondition(triggerBean, dataBean, subBeanList, ownerTableName, filterSubTableName);
                    //key: 触发日期yyyy-MM-dd, value: subDataIds
                    Map<String, List<String>> triggerSubBeanMap = new HashMap<String, List<String>>();
                    for (FormDataSubBean subBean : subBeanList) {
                        //包含，说明已经触发
                        if (triggeredSubDataIds.contains(subBean.getId()) && timeFrequency == FormTriggerBean.TimeFrequency.once && triggerBean.isConditionFirstMet()) {
                            continue;
                        }
                        dataMap.putAll(subBean.getRowData());
                        Date date1 = (Date) FormulaUtil.doResult(formulaStr, dataMap);
                        if (date1 == null) {
                            LOGGER.warn("触发异常，日期为null");
                            continue;
                        }
                        Strings.addToMap(triggerSubBeanMap, Datetimes.formatDate(date1), String.valueOf(subBean.getId()));
                    }
                    for (Map.Entry<String, List<String>> entry : triggerSubBeanMap.entrySet()) {
                        resultDate = Datetimes.parseDate(entry.getKey());//yyyy-mm-dd
                        String subDataStr = Strings.join(entry.getValue(), ",");
                        LOGGER.info("jobName: " + entry.getKey() + " subDataIds1: " + subDataStr);
                        addDataParam2JobMap(allDataJobParamMap, resultDate, sourceForm.getId(), dataId, memberId, subDataStr, ownerTableName, triggerTime);
                    }
                } else if (fieldBean != null || fieldName.equals(Enums.MasterTableField.start_date.getKey())) {
                    resultDate = (Date) FormulaUtil.doResult(formulaStr, dataMap);
                    if (resultDate == null) {
                        LOGGER.warn("触发异常，日期为null");
                        continue;
                    }
                    addDataParam2JobMap(allDataJobParamMap, resultDate, sourceForm.getId(), dataId, memberId, null, null, triggerTime);
                } else {
                    LOGGER.error("修改表单更新表单数据触发时，触发时间点为重复表字段，耗性能，暂时不更新，表单字段：" + fieldName);
                }
            }
        }
        return timeFrequency;
    }

    /**
     * 将当前数据的相关信息加到quartz的参数中
     * <p>
     * 将触发加入到时间调度 根据调度时间进行分类  此方法是新增调度
     * 根据其传入的调度时间，将当前数据新增一个调度或者加入到已存在的调度中，
     * 同时还需要判断此数据是否已经存在过调度，如果已经存在过，需要将其从已存在的调度中移除
     * 调度表中groupName——triggerID  triggerName——formId_date
     * TRIGGER_PARAM_DATE_DATAIDS    : dataId, dataId, dataId, dataId
     * TRIGGER_PARAM_Date_SubDataIds : dataId:subId,subId,subId @@@ dataId:subId,subId,subId @@@ dataId:subId,subId,subId
     * TRIGGER_PARAM_USER_ID         : dataId_memId,dataId_memId,dataId_memId,dataId_memId
     *
     * @param allDataJobParamMap 参数map key:yyyy-mm-dd hh:mm value:此job的参数
     * @param resultDate         触发日期
     * @param formId             表单ID
     * @param dataId             数据ID
     * @param memberId           人员ID
     * @param subDataStr         重复行ID
     * @param subTableName       重复表名称
     * @param dateTime           触发时间
     */
    private void addDataParam2JobMap(Map<String, Map<String, String>> allDataJobParamMap, Date resultDate, Long formId, String dataId, String memberId, String subDataStr, String subTableName, String dateTime) {
        String key = Datetimes.format(resultDate, Datetimes.dateStyle) + " " + dateTime + ":00";
        Map<String, String> jobParam = allDataJobParamMap.get(key);
        if (jobParam == null) {
            jobParam = new HashMap<String, String>();
            allDataJobParamMap.put(key, jobParam);

            jobParam.put(FormTriggerConstant.TRIGGER_PARAM_DATE_DATAIDS, dataId);
            jobParam.put(FormTriggerConstant.TRIGGER_PARAM_USER_ID, dataId + "_" + memberId);
            jobParam.put(FormTriggerConstant.TRIGGER_PARAM_DATE_MODULEIDS, dataId + "_" + dataId);

            if (Strings.isNotBlank(subDataStr)) {
                jobParam.put(FormTriggerConstant.TRIGGER_PARAM_DATE_SUBDATAIDS, dataId + ":" + subDataStr);
                //下面这两个参数只需要加一次
                jobParam.put("formField", "isSubField");
                jobParam.put(FormTriggerConstant.TRIGGER_PARAM_DATE_SUBTABLENAME, subTableName);
            }
        } else {
            String dataIds = jobParam.get(FormTriggerConstant.TRIGGER_PARAM_DATE_DATAIDS);
            dataIds = Strings.isBlank(dataIds) ? dataId : dataIds.contains(dataId) ? dataIds : dataIds + "," + dataId;
            jobParam.put(FormTriggerConstant.TRIGGER_PARAM_DATE_DATAIDS, dataIds);

            String memIds = jobParam.get(FormTriggerConstant.TRIGGER_PARAM_USER_ID);
            String tempMedIds = dataId + "_" + memberId;
            memIds = Strings.isBlank(memIds) ? tempMedIds : memIds.contains(tempMedIds) ? memIds : memIds + "," + tempMedIds;
            jobParam.put(FormTriggerConstant.TRIGGER_PARAM_USER_ID, memIds);

            String moduleIds = jobParam.get(FormTriggerConstant.TRIGGER_PARAM_DATE_MODULEIDS);
            String tempModuleIds = dataId + "_" + dataId;
            moduleIds = Strings.isBlank(moduleIds) ? tempModuleIds : moduleIds.contains(tempModuleIds) ? moduleIds : moduleIds + "," + tempModuleIds;
            jobParam.put(FormTriggerConstant.TRIGGER_PARAM_DATE_MODULEIDS, moduleIds);

            if (Strings.isNotBlank(subDataStr)) {
                String subDataIds = jobParam.get(FormTriggerConstant.TRIGGER_PARAM_DATE_SUBDATAIDS);
                String tempSubDataIds = dataId + ":" + subDataStr;
                //为空或者已经包含，则用原来的
                subDataIds = Strings.isBlank(subDataIds) ? tempSubDataIds : subDataIds.contains(tempSubDataIds) ? subDataIds : subDataIds + "@@@" + tempSubDataIds;
                jobParam.put(FormTriggerConstant.TRIGGER_PARAM_DATE_SUBDATAIDS, subDataIds);
            }
        }
        jobParam.put(FormTriggerConstant.TRIGGER_PARAM_TRIGGER_JOB_NAME, formId + "_" + key);
        LOGGER.info("jobName: " + key + " subDataIds2: " + jobParam.get(FormTriggerConstant.TRIGGER_PARAM_DATE_SUBDATAIDS));
    }

    /**
     * 新增调度公共方法 创建具体的quartz
     */
    private void addQuartzJob(String groupName, String jobName, Date quartzDate, Map<String, String> jobParam, FormTriggerBean.TimeFrequency timeFrequency) throws BusinessException {
        LOGGER.info("触发2：" + jobName + " 類型：" + timeFrequency + ", 時間: " + quartzDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(quartzDate);
        //判断给定时间是否在当前时间之后
        boolean isAfter = quartzDate.compareTo(DateUtil.currentDate()) >= 0;
        /*if (isAfter) {
            calendar.setTime(quartzDate);
        } else {
            if(timeFrequency != FormTriggerBean.TimeFrequency.once) {
                //如果date的时间是在当前时间之前，后面会重新计算时间，为了防止大量的递归出现，此处先将用于递归的calendar的时间设置为当前时间，然后把调度设置的小时分钟设置进去
                calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(timeStr.substring(11, 13)));
                calendar.set(Calendar.MINUTE, Integer.valueOf(timeStr.substring(14, 16)));
                quartzDate = calendar.getTime();
                isAfter = quartzDate.compareTo(DateUtil.currentDate()) >= 0;
            }
        }*/
        //LOGGER.info("jobName: " + jobName + " dataIds: " + jobParam.get(FormTriggerConstant.TRIGGER_PARAM_DATE_DATAIDS));
        LOGGER.info("jobName: " + jobName + " subDataIds3: " + jobParam.get(FormTriggerConstant.TRIGGER_PARAM_DATE_SUBDATAIDS));

        final String jobBeanName = "cap4FormTriggerQuartzJob";
        switch (timeFrequency) {
            case once:
                //时间是未来某个时间点，且未触发过
                if (isAfter) {
                    QuartzHolder.newQuartzJob(groupName, jobName, quartzDate, jobBeanName, jobParam);
                } else {
                    LOGGER.info("时间调度触发时间 " + Datetimes.format(quartzDate, Datetimes.datetimeStyle) + " 点在当前时间之前，不加入时间调度");
                }
                break;
            case PerMonthBegin:
                if (!isAfter) {
                    quartzDate = getAfterDate(calendar, Calendar.MONTH, 1).getTime();
                }
                QuartzHolder.newQuartzJobPerMonth(groupName, jobName, quartzDate, null, jobBeanName, jobParam);
                break;
            case PerMonthEnd:
                if (!isAfter) {
                    quartzDate = getAfterDate(calendar, Calendar.MONTH, 1).getTime();
                }
                QuartzHolder.newQuartzJobEndOfMonth(groupName, jobName, quartzDate, null, jobBeanName, jobParam);
                break;
            case PerSeasonBegin:
                if (!isAfter) {
                    quartzDate = getAfterDate(calendar, Calendar.MONTH, 3).getTime();
                }
                QuartzHolder.newQuartzJobPerSeason(groupName, jobName, quartzDate, null, jobBeanName, jobParam);
                break;
            case PerSeasonEnd:
                if (!isAfter) {
                    quartzDate = getAfterDate(calendar, Calendar.MONTH, 3).getTime();
                }
                QuartzHolder.newQuartzJobEndOfSeason(groupName, jobName, quartzDate, null, jobBeanName, jobParam);
                break;
            case PerHalfYearBegin:
                if (!isAfter) {
                    quartzDate = getAfterDate(calendar, Calendar.MONTH, 6).getTime();
                }
                QuartzHolder.newQuartzJobPerHalfyear(groupName, jobName, quartzDate, null, jobBeanName, jobParam);
                break;
            case PerHalfYearEnd:
                if (!isAfter) {
                    quartzDate = getAfterDate(calendar, Calendar.MONTH, 6).getTime();
                }
                QuartzHolder.newQuartzJobEndOfHalfyear(groupName, jobName, quartzDate, null, jobBeanName, jobParam);
                break;
            case day:
                if (!isAfter) {
                    //如果date的时间是在当前时间之前，后面会重新计算时间，为了防止大量的递归出现，此处先将用于递归的calendar的时间设置为当前时间，然后把调度设置的小时分钟设置进去
                    String timeStr = Datetimes.format(quartzDate, Datetimes.datetimeWithoutSecondStyle);
                    calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(timeStr.substring(11, 13)));
                    calendar.set(Calendar.MINUTE, Integer.valueOf(timeStr.substring(14, 16)));
                    quartzDate = calendar.getTime();
                    isAfter = quartzDate.compareTo(DateUtil.currentDate()) >= 0;
                }
                if (!isAfter) {
                    quartzDate = getAfterDate(calendar, Calendar.DAY_OF_YEAR, 1).getTime();
                }
                QuartzHolder.newQuartzJobPerDay(groupName, jobName, quartzDate, jobBeanName, jobParam);
                break;
            case week:
                if (!isAfter) {
                    quartzDate = getAfterDate(calendar, Calendar.WEEK_OF_MONTH, 1).getTime();
                }
                QuartzHolder.newQuartzJobPerWeek(groupName, jobName, quartzDate, jobBeanName, jobParam);
                break;
            case month:
                if (!isAfter) {
                    quartzDate = getAfterDate(calendar, Calendar.MONTH, 1).getTime();
                }
                QuartzHolder.newQuartzJobPerMonth(groupName, jobName, quartzDate, jobBeanName, jobParam);
                break;
            case season:
                if (!isAfter) {
                    quartzDate = getAfterDate(calendar, Calendar.MONTH, 3).getTime();
                }
                QuartzHolder.newQuartzJobPerSeason(groupName, jobName, quartzDate, jobBeanName, jobParam);
                break;
            case halfYear:
                if (!isAfter) {
                    quartzDate = getAfterDate(calendar, Calendar.MONTH, 6).getTime();
                }
                QuartzHolder.newQuartzJobPerHalfyear(groupName, jobName, quartzDate, null, jobBeanName, jobParam);
                break;
            case year:
                if (!isAfter) {
                    quartzDate = getAfterDate(calendar, Calendar.YEAR, 1).getTime();
                }
                QuartzHolder.newQuartzJobPerYear(groupName, jobName, quartzDate, jobBeanName, jobParam);
                break;
            default:
                break;
        }
        LOGGER.info("触发3：" + jobName + " 類型：" + timeFrequency + ", 時間: " + quartzDate);
    }

    /**
     * 获取当前时间之后的时间点，按照给定的日期类型增加
     *
     * @param c             日期
     * @param calendarField 计算的日期类型，如：年、月、日、星期
     */
    private Calendar getAfterDate(Calendar c, int calendarField, int per) {
        c.add(calendarField, per);
        Date date = c.getTime();
        if (!date.after(DateUtil.currentDate())) {
            return getAfterDate(c, calendarField, per);
        }
        return c;
    }

    @Override
    public void doPreWrite(int moduleType, long moduleId, FormBean formBean, long masterId, FormTriggerBean.TriggerPoint conditionState, String title) throws BusinessException, SQLException {
        Long start = System.currentTimeMillis();
        Long currentUserId = AppContext.currentUserId();
        List<FormTriggerBean> triggerList = getNeedExecTriggerList(formBean, masterId);
        if (Strings.isEmpty(triggerList)) {
            LOGGER.error("表单ID " + formBean.getId() + " 名称" + formBean.getFormName() + "数据ID " + masterId + "没有需要执行的触发…………");
            return;
        }
        FormDataMasterBean masterBean = cap4FormManager.getSessioMasterDataBean(masterId);
        if (masterBean == null) {
            masterBean = cap4FormManager.findDataById(masterId, formBean.getId(), null);
        }
        if (masterBean == null) {
            LOGGER.error("实体数据已经被删除，不执行触发预校验…………");
            return;
        }

        List<FormTriggerActionBean> needPreWriteActionList = new ArrayList<FormTriggerActionBean>();
        for (FormTriggerBean triggerBean : triggerList) {
            if (!triggerBean.isEnable()) {
                continue;
            }
            //满足数据域条件，且非回写节点
            if (triggerBean.isMatchCondition(masterBean)) {
                if (!triggerBean.isConditionFirstMet() && !triggerBean.isMatchTriggerPoint(masterBean, conditionState)) {
                    for (FormTriggerActionBean actionBean : triggerBean.getActionList()) {
                        if ((FormTriggerBean.TriggerType.Update.getKey().equals(actionBean.getType())
                                || FormTriggerBean.TriggerType.DataMagicInterface.getKey().equals(actionBean.getType())
                                || FormTriggerBean.TriggerType.DataMagicOuterForm.getKey().equals(actionBean.getType())
                        ) && actionBean.isWithHolding()) {
                            needPreWriteActionList.add(actionBean);
                        }
                    }
                }
            }
        }
        LOGGER.info("进入预提判断，表单：" + formBean.getFormName() + formBean.getId() + " 对应数据id：" + masterId + " 需要校验执行数为：" + needPreWriteActionList.size());
        if (Strings.isNotEmpty(needPreWriteActionList)) {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("affair_member_id", currentUserId);
            param.put(FormTriggerConstant.TRIGGER_PARAM_CONDITION_STATE, conditionState.getKey());
            param.put(FormTriggerConstant.TRIGGER_PARAM_MODULE_TYPE, moduleType);
            param.put(FormTriggerConstant.TRIGGER_PARAM_MODULE_ID, moduleId);
            param.put(FormTriggerConstant.TRIGGER_PARAM_USER_ID, AppContext.currentUserId());
            param.put(FormTriggerConstant.TRIGGER_PARAM_FORM_BEAN, formBean);
            param.put(FormTriggerConstant.TRIGGER_PARAM_MASTER_BEAN, masterBean);
            param.put(FormTriggerConstant.TRIGGER_PARAM_MASTER_ID, masterBean.getId());
            param.put(FormTriggerConstant.TRIGGER_PARAM_TITLE, title);

            //采用并发控制synchronized来处理预提
            doPreCheck(needPreWriteActionList, param);
        }

        Long end = System.currentTimeMillis();
        LOGGER.info("耗时：" + (end - start));
    }

    /**
     * 采用synchronize方法来控制预提并发情况
     * 具体执行时会校验数据锁，而一旦发现数据被锁，则直接return了，
     * 因此这里采用同步的方式，不然其他的流程执行到这里的时候，发现目标数据被锁就会导致预提不执行的情况
     */
    private synchronized void doPreCheck(List<FormTriggerActionBean> needPreWriteActionList, Map<String, Object> param) {
        for (FormTriggerActionBean actionBean : needPreWriteActionList) {
            FormTriggerUtil.getTriggerActionByKey(actionBean.getType()).afterColSummarySubmit(actionBean.getFormTriggerBean(), actionBean, param);
        }
    }

    /**
     * 回滚预提
     * 根据流程表单的动态表的数据ID，查询出此流程表单对应的预提记录
     * 如果存在预提记录，则先删除预提记录，然后循环预提记录，更新底表对应预写字段的预提值
     *
     * @param summaryId 流程表动态表数据ID
     */
    @Override
    public void rollBackWithHolding(long summaryId) throws BusinessException, SQLException {
        rollBackWithHolding(summaryId, null, null, null);
    }

    /**
     * 回滚预提
     *
     * @param summaryId    流程表动态表数据ID
     * @param recordIdList 预提针对的底表数据ID集合，如果是主表的，则是主表数据ID，如果是重复表的，则是重复行数据ID
     * @param toFormBean   目标表单
     * @param beanMap      根据关联条件找到的目标数据
     */
    @Override
    public void rollBackWithHolding(long summaryId, Set<Long> recordIdList, FormBean toFormBean, Map<Long, FormDataMasterBean> beanMap) throws BusinessException, SQLException {
        List<FormWithholding> holdings = formWithHoldingDAO.selectWithHolding(summaryId);
        if (holdings != null && holdings.size() > 0) {
            // 先将此流程的withHolding删除 如果传入了recordId则表示回滚指定数据的预提，否则是回滚所有
            /**
             * recordIdList和targetFormBean要么同时为null,要么同时非null
             * 非null的时候表示是流程处理过程中的正常预提回滚处理
             * 同时为null这标示流程撤销、回退到发起者、指定回退到发起者的时候进行的预提回滚处理
             * */
            List<FormWithholding> needDeleteHoldings = new ArrayList<FormWithholding>();
            if (recordIdList == null) {
                needDeleteHoldings.addAll(holdings);
            } else {
                for (FormWithholding f : holdings) {
                    if (!recordIdList.contains(f.getRecordId())) {
                        continue;
                    }
                    needDeleteHoldings.add(f);
                }
            }
            formWithHoldingDAO.deleteWithHolding(needDeleteHoldings);

            /*********************************************解析预提记录*******************************************************************/
            //转换的预提键值对 Map<formId,fieldName, Map<masterId,subId, value>>
            Map<String, Map<String, Double>> holdMap = new HashMap<String, Map<String, Double>>();
            //再循环进行底表预提数据的处理
            for (FormWithholding f : needDeleteHoldings) {
                FormBean fb = toFormBean == null ? cap4FormCacheManager.getForm(f.getFormId()) : toFormBean;
                if (fb != null) {
                    FormFieldBean ffb = fb.getFieldBeanByName(f.getPreparewriteFieldName());
                    if (ffb != null) {
                        // 计算除开此流程的预提值后的预提字段之和
                        Double sumValue = formWithHoldingDAO.selectSumValue(f.getFormId(), f.getPreparewriteFieldName(), f.getRecordId());

                        String key1 = f.getFormId() + "," + ffb.getName();
                        Map<String, Double> valueMap = holdMap.get(key1);
                        if (valueMap == null) {
                            valueMap = new HashMap<String, Double>();
                            holdMap.put(key1, valueMap);
                        }
                        String key2 = "";
                        if (!ffb.isMasterField()) {
                            FormTableBean ftb = fb.getTableByTableName(ffb.getOwnerTableName());
                            if (ftb != null) {
                                List<Map<String, Object>> dList = cap4FormDataDAO.selectDataById(new Long[]{f.getRecordId()}, ftb, new String[]{Enums.SubTableField.formmain_id.getKey()});
                                if (dList != null && dList.size() == 1) {
                                    Long masterId = Long.parseLong(dList.get(0).get(Enums.SubTableField.formmain_id.getKey()).toString());
                                    key2 = masterId + "," + f.getRecordId();
                                }
                            }
                        } else {
                            key2 = f.getRecordId() + ",0";
                        }
                        if (Strings.isNotBlank(key2)) {
                            valueMap.put(key2, sumValue);
                        }
                    }
                }
            }

            /*********************************************保存数据*******************************************************************/
            if (holdMap.size() > 0) {
                for (Map.Entry<String, Map<String, Double>> entry : holdMap.entrySet()) {
                    Long formId = Long.valueOf(entry.getKey().split(",")[0]);
                    FormBean relFormBean = toFormBean == null ? cap4FormCacheManager.getForm(formId) : toFormBean;
                    String fieldName = entry.getKey().split(",")[1];
                    FormFieldBean field = relFormBean.getFieldBeanByName(fieldName);
                    List<FormDataMasterBean> dataList = new ArrayList<FormDataMasterBean>();
                    if (entry.getValue() != null && entry.getValue().size() > 0) {
                        Map<Long, FormDataMasterBean> tempBean = new HashMap<Long, FormDataMasterBean>();
                        for (Map.Entry<String, Double> ent : entry.getValue().entrySet()) {
                            Long masterId = Long.valueOf(ent.getKey().split(",")[0]);
                            LOGGER.info("开始刷新回滚预提数据：" + relFormBean.getFormName() + " " + relFormBean.getId() + " " + masterId);
                            FormDataMasterBean newDataBean;
                            if (tempBean.containsKey(masterId)) {
                                newDataBean = tempBean.get(masterId);
                            } else {
                                //先从传入的目标数据map中找，没找到再去查
                                newDataBean = beanMap == null ? null : beanMap.get(masterId);
                                newDataBean = newDataBean == null ? cap4FormDataDAO.selectDataByMasterId(masterId, relFormBean, null) : newDataBean;
                                if (newDataBean == null) {
                                    LOGGER.info("流程：" + summaryId + "撤销预提低表数据失败 " + relFormBean.getFormName() + relFormBean.getId() + "，底表数据 " + masterId + " 不存在或者已经被删除！");
                                    continue;
                                } else {
                                    tempBean.put(masterId, newDataBean);
                                }
                            }
                            Map<String, Map<String, Boolean>> changeTag = new HashMap<String, Map<String, Boolean>>();
                            AppContext.putThreadContext(FormConstant.fieldChangeTag, changeTag);//此标记用于计算，在计算之前将其放入线程变量
                            Long subId = Long.valueOf(ent.getKey().split(",")[1]);
                            if (subId != 0L) {//重表字段
                                FormDataSubBean subBean = newDataBean.getFormDataSubBeanById(field.getOwnerTableName(), subId);
                                subBean.addFieldValue(fieldName, ent.getValue());
                            } else {//主表字段
                                newDataBean.addFieldValue(fieldName, ent.getValue());
                            }
                            //修改了预提的值之后，可能预提字段参与了计算，因此需要刷新计算，然后再保存
                            cap4FormDataManager.calcAll(relFormBean, newDataBean, null, false, false, true, false);
                            AppContext.removeThreadContext(FormConstant.fieldChangeTag);//在计算结束之后移除标记
                            LOGGER.info("保存完成刷新回滚预提数据：" + relFormBean.getFormName() + " " + relFormBean.getId() + " " + masterId + " 新的预提值：" + ent.getValue());
                        }
                        dataList.addAll(tempBean.values());
                    }
                   // cap4FormDataDAO.insertOrUpdateMasterData(dataList, true);
                    cap4FormDataManager.insertOrUpdateMasterData(dataList, true);

                }
            }
        }
    }

    public void setCap4FormCacheManager(CAP4FormCacheManager cap4FormCacheManager) {
        this.cap4FormCacheManager = cap4FormCacheManager;
    }

    public void setCap4FormTriggerRecordDAO(CAP4FormTriggerRecordDAO cap4FormTriggerRecordDAO) {
        this.cap4FormTriggerRecordDAO = cap4FormTriggerRecordDAO;
    }

    public void setTaskExecuteManager(TaskExecuteManager taskExecuteManager) {
        this.taskExecuteManager = taskExecuteManager;
    }

    public void setCap4FormManager(CAP4FormManager cap4FormManager) {
        this.cap4FormManager = cap4FormManager;
    }

    public void setFormWithHoldingDAO(FormWithHoldingDAO formWithHoldingDAO) {
        this.formWithHoldingDAO = formWithHoldingDAO;
    }

    public void setCap4FormDataDAO(CAP4FormDataDAO cap4FormDataDAO) {
        this.cap4FormDataDAO = cap4FormDataDAO;
    }

    public void setCap4FormDataManager(CAP4FormDataManager cap4FormDataManager) {
        this.cap4FormDataManager = cap4FormDataManager;
    }
}
