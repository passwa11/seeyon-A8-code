/**
 * $Author: 翟锋$
 * $Rev: $
 * $Date:: $
 * <p>
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 * <p>
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.common.detaillog.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.seeyon.apps.ext.KfqInform.manager.KfqInformManager;
import com.seeyon.apps.ext.KfqInform.manager.KfqInformManagerImpl;
import com.seeyon.apps.ext.KfqInform.po.KfqInform;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.util.JDBCAgent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.edoc.api.EdocApi;
import com.seeyon.apps.edoc.bo.EdocSummaryBO;
import com.seeyon.apps.info.api.InfoApi;
import com.seeyon.apps.info.po.InfoSummaryPO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.manager.HisAffairManager;
import com.seeyon.ctp.common.affair.po.CtpAffairHis;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.detaillog.vo.FlowNodeDetailAffairVO;
import com.seeyon.ctp.common.detaillog.vo.ProcessLogVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.processlog.his.manager.HisProcessLogManager;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.processlog.po.ProcessLog;
import com.seeyon.ctp.common.supervise.bo.SuperviseLogWebModel;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.dubbo.RefreshInterfacesAfterUpdate;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.common.taglibs.functions.Functions;
import com.seeyon.v3x.worktimeset.exception.WorkTimeSetExecption;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

/**
 * @author zhaifeng
 * update 20140222 增加信息报送 李兵
 */
public class DetaillogManagerImpl implements DetaillogManager {

    private static final Log log = LogFactory.getLog(DetaillogManagerImpl.class);

    private SuperviseManager superviseManager;
    private ProcessLogManager processLogManager;
    private HisProcessLogManager hisProcessLogManager;
    private WorkTimeManager workTimeManager;
    private AffairManager affairManager;
    private OrgManager orgManager;
    private WorkflowApiManager wapi;
    private PermissionManager permissionManager;
    private HisAffairManager hisAffairManager;
    private TemplateManager templateManager;
    private CollaborationApi collaborationApi = null;

    private Map<ModuleType, DetaillogHandler> detaillogHandlerMap = new ConcurrentHashMap<ModuleType, DetaillogHandler>();


    private EdocApi edocApi;
    private InfoApi infoApi;

    public void setInfoApi(InfoApi infoApi) {
        this.infoApi = infoApi;
    }

    public void setEdocApi(EdocApi edocApi) {
        this.edocApi = edocApi;
    }

    public void setCollaborationApi(CollaborationApi collaborationApi) {
        this.collaborationApi = collaborationApi;
    }

    public static void setWorkTime(Integer workTime) {
        DetaillogManagerImpl.workTime = workTime;
    }

    public static void setYear(int year) {
        DetaillogManagerImpl.year = year;
    }

    public SuperviseManager getSuperviseManager() {
        return superviseManager;
    }

    public void setSuperviseManager(SuperviseManager superviseManager) {
        this.superviseManager = superviseManager;
    }

    public ProcessLogManager getProcessLogManager() {
        return processLogManager;
    }

    public void setProcessLogManager(ProcessLogManager processLogManager) {
        this.processLogManager = processLogManager;
    }

    public HisProcessLogManager getHisProcessLogManager() {
        return hisProcessLogManager;
    }

    public void setHisProcessLogManager(HisProcessLogManager hisProcessLogManager) {
        this.hisProcessLogManager = hisProcessLogManager;
    }

    public WorkTimeManager getWorkTimeManager() {
        return workTimeManager;
    }

    public void setWorkTimeManager(WorkTimeManager workTimeManager) {
        this.workTimeManager = workTimeManager;
    }

    public AffairManager getAffairManager() {
        return affairManager;
    }

    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

    public OrgManager getOrgManager() {
        return orgManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public WorkflowApiManager getWapi() {
        return wapi;
    }

    public void setWapi(WorkflowApiManager wapi) {
        this.wapi = wapi;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public HisAffairManager getHisAffairManager() {
        return hisAffairManager;
    }

    public void setHisAffairManager(HisAffairManager hisAffairManager) {
        this.hisAffairManager = hisAffairManager;
    }

    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    /**
     * 内容管理接口初始化，内容处理器接口加载
     */
    @RefreshInterfacesAfterUpdate(inface = DetaillogHandler.class)
    public void init() {
        Map<String, DetaillogHandler> detaillogHandlers = AppContext.getBeansOfType(DetaillogHandler.class);
        for (String key : detaillogHandlers.keySet()) {
            DetaillogHandler handler = detaillogHandlers.get(key);
            detaillogHandlerMap.put(handler.getModuleType(), handler);
        }
    }

    @SuppressWarnings("unused")
    private DetaillogHandler getDetaillogHandler(ModuleType moduleType) throws BusinessException {
        DetaillogHandler handler = null;
        try {
            handler = detaillogHandlerMap.get(moduleType);
            if (handler == null) {
                throw new BusinessException("getDetaillogHandler没有找到数据处理器：" + moduleType);
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return handler;
    }

    @Override
    @AjaxAccess
    public FlipInfo getProcessLog(FlipInfo flipInfo, Map query) throws BusinessException {
        if (query.get("processId") == null || "".equals(query.get("processId"))) {
            return flipInfo;
        }
        //分页时，携带的参数会变成String类型，存在 类型转换异常，因此 进行转换
        Long processId = Long.parseLong(query.get("processId").toString());
        query.put("processId", processId);
        List<ProcessLog> processLogList = processLogManager.getLogsByProcessIdAndActionId(query);
        if (null != query.get("editContent") && processLogList != null) {
            for (int i = processLogList.size() - 1; i >= 0; i--) {
                if (processLogList.get(i).getActionId() != null && processLogList.get(i).getActionId().equals(ProcessLogAction.processEdoc.getKey()) &&
                        !processLogList.get(i).getParam0().equals(Integer.toString(ProcessLogAction.ProcessEdocAction.modifyBody.getKey())) && !processLogList.get(i).getParam0().equals(Integer.toString(ProcessLogAction.ProcessEdocAction.Body.getKey())) && !processLogList.get(i).getParam0().equals(Integer.toString(ProcessLogAction.ProcessEdocAction.bodyFromRed.getKey())) && !processLogList.get(i).getParam0().equals(Integer.toString(ProcessLogAction.ProcessEdocAction.signed.getKey()))) {
                    processLogList.remove(i);
                }
            }
        }
        List<ProcessLogVO> list = commonProcessLog(processLogList);

        flipInfo.setParams(query);
        DBAgent.memoryPaging(list, flipInfo);

        return flipInfo;
    }

    @Override
    public List<ProcessLogVO> getProcessLog(Long processId) throws BusinessException {
        if (processId == null) {
            return null;
        }
        List<ProcessLog> processLogList = processLogManager.getLogsByProcessId(processId, false);
        //没有数据时 查询分表
        if (processLogList == null || processLogList.isEmpty()) {
            processLogList = hisProcessLogManager.getLogsByProcessId(processId, false);
        }
        return commonProcessLog(processLogList);
    }

    private KfqInformManager informManager = new KfqInformManagerImpl();

    public KfqInformManager getInformManager() {
        return informManager;
    }

    @AjaxAccess
    public FlipInfo getFlowNodeDetail(FlipInfo flipInfo, Map query) throws BusinessException {
        //分页时，携带的参数会变成String类型，存在 类型转换异常，因此 进行转换
        Long summaryId = Long.parseLong(query.get("objectId").toString());
        query.put("objectId", summaryId);
        if (null == query.get("app")) {
            CtpAffair senderAffair = affairManager.getSenderAffair(summaryId);
            if (senderAffair != null) {
                int app = senderAffair.getApp().intValue();
                if (app == 19 || app == 20 || app == 21) {
                    query.put("app", app);
                }
            }
        }
        List<CtpAffair> affairList = affairManager.getValidAffairs(flipInfo, query);
        if (Strings.isEmpty(affairList)) {
            affairList = affairManager.getValidAffairsHis(flipInfo, query);
        }

        List<FlowNodeDetailAffairVO> affairVOList = new ArrayList<FlowNodeDetailAffairVO>();
        affairVOList = ctpAffair2ctpAffairVO(affairList, affairVOList);
        List<FlowNodeDetailAffairVO> list = commonCtpAffairVO(affairVOList);
//        zhou
        List<Map<String, Object>> mapList = getUnitData();
        Map params = new HashMap();
        params.put("summaryid", query.get("objectId").toString());
        List<KfqInform> informList = informManager.findInformbySummaryid(params);
        Connection connection = null;
        List<FlowNodeDetailAffairVO> newList = new ArrayList<>();
        if (informList.size() > 0) {
            try {
                connection = JDBCAgent.getRawConnection();
                for (int i = 0; i < informList.size(); i++) {
                    String userId = informList.get(i).getMemberid();
                    for (FlowNodeDetailAffairVO vo : list) {
                        if (Long.toString(vo.getMemberId()).equals(userId)) {
                            String deptname = getParentDept(userId, connection, mapList);
                            vo.setDeptName(deptname);
                            newList.add(vo);
                        }
                    }
                }
            } catch (Exception e) {
            }
        } else {
            try {
                connection = JDBCAgent.getRawConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            for (FlowNodeDetailAffairVO vo : list) {
                String deptname = getParentDept(Long.toString(vo.getMemberId()), connection, mapList);
                vo.setDeptName(deptname);
                newList.add(vo);
            }
        }
        if (null != connection) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
//        zhou

        flipInfo.setNeedTotal(false);
        flipInfo.setData(newList);

//        if(list != null && list.size() > 0){
//        	DBAgent.memoryPaging(list, flipInfo);
//        }
        return flipInfo;
    }

    //zhou Start
    public String getParentDept(String memberId, Connection connection, List<Map<String, Object>> mapList) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuffer result = new StringBuffer();
        String sql = "select ORG_DEPARTMENT_ID from ORG_MEMBER where id ='" + memberId + "'";
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            String departmentId = "";
            while (rs.next()) {
                departmentId = rs.getString(1);
            }

            for (int i = 0; i < mapList.size(); i++) {
                if ((mapList.get(i).get("id").toString()).equals(departmentId)) {
                    String path = (mapList.get(i).get("path")).toString();
                    String p12 = path.substring(0, 12);
                    result.append("/");
                    result.append(getUnitName(p12, mapList));
                    if (path.length() >= 16) {
                        String p16 = path.substring(0, 16);
                        result.append("/");
                        result.append(getUnitName(p16, mapList));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
                if (null != ps) {
                    ps.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result.toString();
    }

    public String getUnitName(String path, List<Map<String, Object>> mapList) {
        String name = "";
        for (int j = 0; j < mapList.size(); j++) {
            String p = mapList.get(j).get("path").toString();
            if (p.equals(path)) {
                name = mapList.get(j).get("name").toString();
            }
        }
        return name;
    }


    public List<Map<String, Object>> getUnitData() {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> mapList = new ArrayList<>();
        String sql = "select id,name,path from ORG_UNIT";
        try {
            connection = JDBCAgent.getRawConnection();
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            Map<String, Object> map = null;
            while (rs.next()) {
                map = new HashMap<>();
                map.put("id", Long.toString(rs.getLong("id")));
                map.put("name", rs.getString("name"));
                map.put("path", rs.getString("path"));
                mapList.add(map);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
                if (null != ps) {
                    ps.close();
                }
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return mapList;
    }
//zhou end


    @Override
    @Deprecated
    public List<FlowNodeDetailAffairVO> getFlowNodeDetail(Long summaryId) throws BusinessException {
        if (summaryId == null) {
            return null;
        }
        Map<String, Long> query = new HashMap<String, Long>();
        query.put("objectId", summaryId);
        List<CtpAffair> affairList = new ArrayList<CtpAffair>();
        affairList = affairManager.getValidAffairs(null, query);
        List<FlowNodeDetailAffairVO> affairVOList = new ArrayList<FlowNodeDetailAffairVO>();
        affairVOList = ctpAffair2ctpAffairVO(affairList, affairVOList);
        return commonCtpAffairVO(affairVOList);
    }

    @Override
    public List<SuperviseLogWebModel> getRemindersLog(Long summaryId) throws BusinessException {
        if (summaryId == null) {
            return null;
        }
        return superviseManager.getLogBySummaryId(summaryId);
    }

    @Override
    public FlipInfo getRemindersLog(FlipInfo flipInfo, Map query) throws BusinessException {
        if (query == null || query.get("summaryId") == null) {
            return null;
        }
        flipInfo = superviseManager.getLogBySummaryId(flipInfo, query);
        return flipInfo;
    }

    /**
     * 对流程日志 查询集合进行特殊处理
     *
     * @param logList
     * @return
     * @throws BusinessException
     */
    private List<ProcessLogVO> commonProcessLog(List<ProcessLog> logList) throws BusinessException {
        if (logList == null) {
            return null;
        }
        List<ProcessLogVO> processLog = new ArrayList<ProcessLogVO>();
        Map<Long, String> idToNameMap = new HashMap<Long, String>();
        Map<String, String> nodeIdToNameMap = new HashMap<String, String>();
        if (Strings.isNotEmpty(logList)) {
            boolean isNeedParseWf = false;
            for (ProcessLog log : logList) {
                String memname = Functions.showProcessActionName(log.getActionUserId());
                if (Strings.isNotBlank(memname)) {
                    idToNameMap.put(log.getId(), memname);
                } else {
                    isNeedParseWf = true;
                }
            }
            if (isNeedParseWf) {
                ProcessLog pl = logList.get(0);
                if (pl != null && pl.getProcessId() != null) {
                    nodeIdToNameMap = wapi.getRunningProcessNodeIdAndName(pl.getProcessId());
                }
            }

        }
        int i = 1;
        for (ProcessLog log : logList) {
            ProcessLogVO vo = new ProcessLogVO();
            // 序号
            vo.setNumber((i++) + "");
            // 操作人
            String memname = idToNameMap.get(log.getId());
            if (Strings.isBlank(memname)) {
                memname = nodeIdToNameMap.get(String.valueOf(log.getActivityId()));
            }
            vo.setHandler(memname);
            // 操作时间
            vo.setFinishDate(log.getActionTime());
            // 指定回退的日志
            if (log.getActionId() == ProcessLogAction.colStepBackToSender.getKey() || log.getActionId() == ProcessLogAction.colStepBackToPoint.getKey()
                    || log.getActionId() == ProcessLogAction.colStepBackToResend.getKey()) {
                String actionName = ResourceUtil.getString("processLog.action.name." + log.getActionId());
                // String actionDesc =
                // ResourceUtil.getString("processLog.action." +
                // log.getActionId(), log.getParam0());
                vo.setContent(actionName);
                if ("true".equals(log.getParam4())) {
                    if (Strings.isNotBlank(log.getParam5())) {
                        memname = log.getParam5();
                    }
                    vo.setDesc(ResourceUtil.getString(log.getParam1(), memname, log.getParam2(), log.getParam3()));
                } else {
                    String actionDesc = ResourceUtil.getString("processLog.action." + log.getActionId(), log.getParam0());
                    vo.setDesc(actionDesc);
                }
            } else {
                // 操作内容
                vo.setContent(log.getActionName());
                // 操作描述
                if (log.getActionId() == ProcessLogAction.insertPeople.getKey()
                        && Strings.isNotBlank(log.getParam1()) && "1".equals(log.getParam1())) {
                    String immediatereceiptMsg = ResourceUtil.getString("processLog.action.8.immediatereceipt");
                    vo.setDesc(log.getActionDesc() + immediatereceiptMsg);
                } else {
                    vo.setDesc(log.getActionDesc());
                }
            }
            // 意见
            vo.setOpinion(log.getParam5());
            vo.setDetailStatus(log.getDetailStatus());
            vo.setId(log.getId().toString());
            vo.setActionId(log.getActionId());
            processLog.add(vo);
        }
        return processLog;
    }

    /**
     * 对List<CtpAffairVO> affairVOList 进行特殊处理
     *
     * @param affairVOList
     * @return
     * @throws BusinessException
     */
    private List<FlowNodeDetailAffairVO> commonCtpAffairVO(List<FlowNodeDetailAffairVO> affairVOList) throws BusinessException {

        if (affairVOList != null && affairVOList.size() > 0) {
            Long _accountId = AppContext.getCurrentUser().getLoginAccount();
            Long orgAccountId = 0l;
            Map<String, String> nodeIdToNameMap = null;
            for (FlowNodeDetailAffairVO affairVO : affairVOList) {
                String handler = "";
                String policyName = "";
                Date createDate = null;
                Date finishDate = null;
                String dealTime = null;
                String stateLabel = "";
                String deadline = null;
                //如果affair为公文收发员处理的已封发的公文事项（即待发送公文和已发送公文），不显示在处理明细节点中。
                //因为此时的affair不在流程中，获取节点权限异常
                if (affairVO.getApp() == ApplicationCategoryEnum.exSend.getKey())
                    continue; //待发送22
                if (affairVO.getApp() == ApplicationCategoryEnum.exSign.getKey())
                    continue; //待签收23
                if (affairVO.getApp() == ApplicationCategoryEnum.edocRegister.getKey())
                    continue; //待登记24
                String appName = "";
                String processId = "";
                Date _finishDate = null;
                //已发列表
                if (Integer.valueOf(StateEnum.col_sent.key()).equals(affairVO.getState())
                        || Integer.valueOf(StateEnum.col_waitSend.key()).equals(affairVO.getState())) {
                    Long senderId = affairVO.getSenderId();
                    V3xOrgMember member = orgManager.getMemberById(senderId);
                    if (member != null) {
                        handler = member.getName();
                        if (!_accountId.equals(member.getOrgAccountId())) { //同一个单位的
                            String accountShortname = orgManager.getAccountById(member.getOrgAccountId())
                                    .getShortName();
                            handler += "(" + accountShortname + ")";
                        }
                    }
                    createDate = affairVO.getCreateDate();
                    //客开 项目名称： [修改功能：] 作者：fzc 修改日期：2018-4-28 start
                    boolean isNewSend = affairVO.getApp() == ApplicationCategoryEnum.edoc.getKey() && (affairVO.getSubApp().intValue() == ApplicationSubCategoryEnum.edoc_fawen.getKey() || affairVO.getSubApp().intValue() == ApplicationSubCategoryEnum.edoc_qianbao.getKey());
                    if (affairVO.getApp().intValue() == ApplicationCategoryEnum.edocSend.getKey()
                            || affairVO.getApp() == ApplicationCategoryEnum.edocSign.getKey() || isNewSend) {
                        //发文,签报
                        policyName = ResourceUtil.getString("node.policy.niwen");
                    }
                    boolean isNewRec = affairVO.getApp() == ApplicationCategoryEnum.edoc.getKey() && affairVO.getSubApp().intValue() == ApplicationSubCategoryEnum.edoc_shouwen.getKey();
                    if (affairVO.getApp().intValue() == ApplicationCategoryEnum.edocRec.getKey() || isNewRec) {
                        //客开 项目名称： [修改功能：] 作者：fzc 修改日期：2018-4-28 end
                        //收文
                        policyName = ResourceUtil.getString("node.policy.dengji" + Functions.suffix());
                    }
                    boolean isExchange = affairVO.getApp() == ApplicationCategoryEnum.edoc.getKey() && affairVO.getSubApp().intValue() == ApplicationSubCategoryEnum.edoc_jiaohuan.getKey();
                    if (affairVO.getApp().intValue() == ApplicationCategoryEnum.edocRec.getKey() || isExchange) {
                        policyName = ResourceUtil.getString("node.policy.niwen");
                    }
                    if (affairVO.getApp().intValue() == ApplicationCategoryEnum.collaboration.getKey()) {
                        if ("newCol".equals(affairVO.getNodePolicy())) {
                            policyName = ResourceUtil.getString("node.policy.newCol");
                        } else {
                            policyName = ResourceUtil.getString("node.policy.collaboration");
                        }
                    }

                    if (affairVO.getApp().intValue() == ApplicationCategoryEnum.info.getKey()) {
                        policyName = ResourceUtil.getString("node.policy.shangbao");
                    }

                } else {
                    Long memberId = affairVO.getMemberId();
                    V3xOrgMember member = orgManager.getMemberById(memberId);
                    if (member != null) {
                        handler = member.getName();
                        if (!_accountId.equals(member.getOrgAccountId())) { //同一个单位的
                            String accountShortname = orgManager.getAccountById(member.getOrgAccountId())
                                    .getShortName();
                            handler += "(" + accountShortname + ")";
                        }
                    } else {
                        if (nodeIdToNameMap == null) {
                            nodeIdToNameMap = wapi.getRunningProcessNodeIdAndName(affairVO.getProcessId());
                        }
                        handler = nodeIdToNameMap.get(String.valueOf(affairVO.getActivityId()));
                    }
                    Long summaryId = affairVO.getObjectId();
                    ColSummary summary = null;
                    EdocSummaryBO eSummary = null;
                    //TODO DEV 2015-08-19 InfoSummary 还未迁移
                    //InfoSummary infoSummary = null;
                    if (affairVO.getApp().intValue() == ApplicationCategoryEnum.edocSend.key()) {
                        //发文
                        eSummary = edocApi.getEdocSummary(summaryId);
                        DetaillogHandler handler1 = getDetaillogHandler(ModuleType.edoc);
                        orgAccountId = handler1.getFlowPermAccountId(summaryId);
                        appName = ApplicationCategoryEnum.edoc.name();
                        policyName = permissionManager.getPermissionName(EnumNameEnum.edoc_send_permission_policy.name(), affairVO.getNodePolicy(), orgAccountId);
                        if (Strings.isBlank(policyName)) {
                            //拟文
                            policyName = ResourceUtil.getString("edoc.new.type.send");
                        }
                        processId = eSummary.getProcessId().toString();
                    } else if (affairVO.getApp().intValue() == ApplicationCategoryEnum.edocRec.key()) {
                        //收文
                        DetaillogHandler handler1 = getDetaillogHandler(ModuleType.edoc);
                        eSummary = edocApi.getEdocSummary(summaryId);
                        orgAccountId = handler1.getFlowPermAccountId(summaryId);
                        appName = ApplicationCategoryEnum.edoc.name();
                        policyName = permissionManager.getPermissionName(EnumNameEnum.edoc_rec_permission_policy.name(), affairVO.getNodePolicy(), orgAccountId);
                        processId = eSummary.getProcessId().toString();
                    } else if (affairVO.getApp().intValue() == ApplicationCategoryEnum.edocSign.key()) {
                        //签报
                        eSummary = edocApi.getEdocSummary(summaryId);
                        DetaillogHandler handler1 = getDetaillogHandler(ModuleType.edoc);
                        orgAccountId = handler1.getFlowPermAccountId(summaryId);
                        appName = ApplicationCategoryEnum.edoc.name();
                        policyName = permissionManager.getPermissionName(EnumNameEnum.edoc_qianbao_permission_policy.name(), affairVO.getNodePolicy(), orgAccountId);
                        processId = eSummary.getProcessId().toString();
                    } else if (affairVO.getApp().intValue() == ApplicationCategoryEnum.edoc.key()) {
                        //发文
                        eSummary = edocApi.getEdocSummary(summaryId);
                        DetaillogHandler handler1 = getDetaillogHandler(ModuleType.edoc);
                        orgAccountId = handler1.getFlowPermAccountId(summaryId);
                        appName = ApplicationCategoryEnum.edoc.name();
                        String subAppName = EnumNameEnum.edoc_new_send_permission_policy.name();
                        if (affairVO.getSubApp() != null) {
                            if (affairVO.getSubApp().intValue() == ApplicationSubCategoryEnum.edoc_shouwen.key()) {
                                subAppName = EnumNameEnum.edoc_new_rec_permission_policy.name();
                            } else if (affairVO.getSubApp().intValue() == ApplicationSubCategoryEnum.edoc_qianbao.key()) {
                                subAppName = EnumNameEnum.edoc_new_qianbao_permission_policy.name();
                            } else if (affairVO.getSubApp().intValue() == ApplicationSubCategoryEnum.edoc_jiaohuan.key()) {
                                subAppName = EnumNameEnum.edoc_new_change_permission_policy.name();
                            } else if (affairVO.getSubApp().intValue() == ApplicationSubCategoryEnum.old_edocSign.key()) {
                                subAppName = EnumNameEnum.edoc_qianbao_permission_policy.name();
                            } else if (affairVO.getSubApp().intValue() == ApplicationSubCategoryEnum.old_edocSend.key()) {
                                subAppName = EnumNameEnum.edoc_send_permission_policy.name();
                            } else if (affairVO.getSubApp().intValue() == ApplicationSubCategoryEnum.old_edocRec.key()) {
                                subAppName = EnumNameEnum.edoc_rec_permission_policy.name();
                            } else if (affairVO.getSubApp().intValue() == ApplicationSubCategoryEnum.old_exSign.key()) {
                                subAppName = EnumNameEnum.edoc_new_change_permission_policy.name();
                            }
                        }
                        if (affairVO.getNodePolicy().startsWith("old")) {
                            affairVO.setNodePolicy(affairVO.getNodePolicy().replaceAll("old", ""));
                        }
                        policyName = permissionManager.getPermissionName(subAppName, affairVO.getNodePolicy(), orgAccountId);
                        if (Strings.isBlank(policyName)) {
                            //拟文
                            policyName = ResourceUtil.getString("edoc.new.type.send");
                        }
                        processId = eSummary.getProcessId().toString();
                    } else if (affairVO.getApp().intValue() == ApplicationCategoryEnum.info.key()) {
                        //TODO DEV 2015-08-19 InfoSummary 还未迁移
                        InfoSummaryPO infoSummary = infoApi.getInfoSummary(summaryId);
                        DetaillogHandler handler1 = getDetaillogHandler(ModuleType.info);
                        orgAccountId = handler1.getFlowPermAccountId(summaryId);
                        appName = ApplicationCategoryEnum.info.name();
                        policyName = permissionManager.getPermissionName(EnumNameEnum.info_send_permission_policy.name(), affairVO.getNodePolicy(), orgAccountId);
                        processId = infoSummary.getProcessId().toString();
                    } else {
                        //协同
                        summary = collaborationApi.getColSummary(summaryId);
                        orgAccountId = collaborationApi.getFlowPermAccountId(summary.getOrgAccountId(), summary);
                        appName = ApplicationCategoryEnum.collaboration.name();
                        policyName = permissionManager.getPermissionName(EnumNameEnum.col_flow_perm_policy.name(), affairVO.getNodePolicy(), orgAccountId);
                        if (summary.getProcessId() != null) {
                            processId = summary.getProcessId().toString();
                        }
                    }

                    if (Strings.isBlank(policyName)) {
                        String[] policy = wapi.getNodePolicyIdAndName(appName, processId, affairVO.getActivityId() == null ? "start" : affairVO
                                .getActivityId().toString());
                        if (policy != null && policy.length > 1) {
                            policyName = policy[1];
                        }
                    }
                    createDate = affairVO.getReceiveTime();
                    finishDate = affairVO.getCompleteTime();
//                    Date updateDate = affairvo.getUpdateDate();
                    Date _createDate = null;
                    if (createDate != null) {
                        _createDate = new Date(createDate.getTime());
                    }

                    //如果没有完成(处理)时间,取更新时间
                    if (finishDate != null) {
                        _finishDate = new Date(finishDate.getTime());
                    } else if (affairVO.getState() == StateEnum.col_pending.key() && affairVO.getSubState() == SubStateEnum.col_pending_ZCDB.key()) {
                        _finishDate = affairVO.getUpdateDate();
                    }
                    //计算处理时长
                    if (_createDate != null && _finishDate != null) {
                        Long intervalTime = Functions.getMinutesBetweenDatesByWorkTime(_createDate, _finishDate, orgAccountId);
                        if (intervalTime != null) {
                            dealTime = Functions.showDateByWork(Integer.parseInt(intervalTime.toString()));
                        }
                    }
                    //处理期限(节点期限)
//                    if (affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0) {
//                        long[] deadlineArr = Datetimes.formatLongToTimeStr(affair.getDeadlineDate() * 60000);
//                        long date1 = deadlineArr[0];
//                        long hour1 = deadlineArr[1];
//                        long minut1 = deadlineArr[2];
//                        deadline = ColUtil.timePatchwork(date1, hour1, minut1, 0L, true);
//                    }
                    deadline = affairVO.getDeadline();
                }
                int state = affairVO.getState();
                Integer subState = affairVO.getSubState();
                if (subState == null) {
                    subState = SubStateEnum.col_normal.key();
                }
                if (state == StateEnum.col_pending.key() && subState == SubStateEnum.col_pending_ZCDB.key()) {
                    stateLabel = ResourceUtil.getString("collaboration.substate.13.label");
                    //暂存待办 时 不计算处理时长,将其值为null
                    dealTime = null;
                } else if (state == StateEnum.col_sent.key()) {
                    stateLabel = ResourceUtil.getString("collaboration.state.12.col_sent");
                } else if ((state == StateEnum.col_pending.key() && subState != SubStateEnum.col_pending_ZCDB.key())
                        || state == StateEnum.col_pending_repeat_auto_deal.key()) {
                    stateLabel = ResourceUtil.getString("collaboration.state.13.col_pending");
                } else if (state == StateEnum.col_done.key()) {
                    stateLabel = ResourceUtil.getString("collaboration.state.14.done");
                    if (subState == SubStateEnum.col_done_stepStop.key()) {
                        stateLabel += "(" + ResourceUtil.getString("collaboration.state.10.stepstop") + ")";
                    }
                } else if (state == StateEnum.col_waitSend.key()) {
                    stateLabel = ResourceUtil.getString("collaboration.state.11.waitSend");
                }
                //发起、收到时间（如果是已发、待发 则是 创建时间、其它的为收到时间）
                affairVO.setCreateDate(createDate);
                //处理时长
                affairVO.setDealTime(dealTime);
                //处理期限
                affairVO.setDeadline(deadline);
                //处理时间
                affairVO.setFinishDate(_finishDate);
                //处理人
                affairVO.setHandler(handler);
                //节点权限
                affairVO.setPolicyName(policyName);
                //处理状态
                affairVO.setStateLabel(stateLabel);
                //没有超期时长动态运行.
                affairVO.setDeadlineTime(affairVO.getOverWorktime() == null ? null : showDate(Integer.parseInt(affairVO.getOverWorktime().toString()), true));
                boolean isCompute = false;
                Date computeEndDate = new Date();
                //设置了节点期限的才需要计算.  计算超长时长
//                if (affairvo.getDeadlineDate() != null && affairvo.getDeadlineDate() != 0) {
                if (affairVO.getDeadline() != null) {    // 根据传入的节点期限时间来判断
                    if (affairVO.getState().intValue() == StateEnum.col_done.key() && affairVO.getOverWorktime() == null) {
                        isCompute = true;
                        computeEndDate = affairVO.getCompleteTime();
                    } else if (affairVO.getState().intValue() == StateEnum.col_pending.key()) {
                        isCompute = true;
                    }
                }
                if (isCompute) {
                    try {
                        long time = workTimeManager.getDealWithTimeValue(affairVO.getReceiveTime(), computeEndDate, orgAccountId);
                        time = time / (60 * 1000); //毫秒转化为分钟
                        long workDeadLine = 0l;
                        long over = 0l;
                        if (affairVO.getDeadlineDate() != null && affairVO.getDeadlineDate() != 0) {
                            workDeadLine = workTimeManager.convert2WorkTime(Long.parseLong(affairVO.getDeadlineDate().toString()), orgAccountId);
                            over = time - workDeadLine;
                            affairVO.setOverWorktime(over > 0 ? over : 0);
                            if (over > 0) {
                                affairVO.setCoverTime(true);
                            }
                        }

                        affairVO.setDeadlineTime(showDate(Integer.parseInt(over + ""), true));


                    } catch (Exception e) {
                        log.error("", e);
                    }
                }

                if ((affairVO.getDeadline() == null || "".equals(affairVO.getDeadline()))) {
                    affairVO.setOverWorktime(0l);
                    affairVO.setDeadlineTime(null);
                }
            }
        }
        return affairVOList;
    }

    private List<FlowNodeDetailAffairVO> ctpAffair2ctpAffairVO(List<CtpAffair> affairList, List<FlowNodeDetailAffairVO> affairVOList) {

        if (affairList == null || affairList.size() == 0) {
            return null;
        }
        for (CtpAffair affair : affairList) {
            FlowNodeDetailAffairVO vo = new FlowNodeDetailAffairVO();
            vo.setActivityId(affair.getActivityId());
            vo.setAddition(affair.getAddition());
            vo.setApp(affair.getApp());
            vo.setSubApp(affair.getSubApp());
            vo.setArchiveId(affair.getArchiveId());
            vo.setBodyType(affair.getBodyType());
            vo.setCompleteTime(affair.getCompleteTime());
            vo.setCreateDate(affair.getCreateDate());
            vo.setFirstViewDate(affair.getFirstViewDate());
            vo.setDeadlineDate(affair.getDeadlineDate());
            //处理期限
            if (affair.getExpectedProcessTime() != null) {
                vo.setDeadline(WFComponentUtil.getDeadLineName(affair.getExpectedProcessTime()));
            } else {
                vo.setDeadline(WFComponentUtil.getDeadLineName(affair.getDeadlineDate()));
            }
            vo.setDealTermType(affair.getDealTermType());
            vo.setDealTermUserid(affair.getDealTermUserid());
            vo.setDelete(affair.isDelete());
            vo.setDueRemind(affair.isDueRemind());
            vo.setExtProps(affair.getExtProps());
            vo.setFinish(affair.isFinish());
            vo.setFormAppId(affair.getFormAppId());
//            vo.setFormId(affair.getFormId());
//            vo.setFormOperationId(affair.getFormOperationId());
//            vo.setFormViewOperation(affair.getMultiViewStr());
            vo.setForwardMember(affair.getForwardMember());
            vo.setFromId(affair.getFromId());
            vo.setHastenTimes(affair.getHastenTimes());
            vo.setIdentifier(affair.getIdentifier());
            vo.setImportantLevel(affair.getImportantLevel());
            vo.setMemberId(affair.getMemberId());
            vo.setNodePolicy(affair.getNodePolicy());
            vo.setObjectId(affair.getObjectId());
            vo.setOverTime(affair.getOverTime());
            vo.setOverWorktime(affair.getOverWorktime());
            vo.setReceiveTime(affair.getReceiveTime());
            vo.setRemindDate(affair.getRemindDate());
            vo.setRemindInterval(affair.getRemindInterval());
            vo.setResentTime(affair.getResentTime());
            vo.setRunWorktime(affair.getRunWorktime());
            vo.setSenderId(affair.getSenderId());
            vo.setState(affair.getState());
            vo.setSubApp(affair.getSubApp());
            vo.setSubject(WFComponentUtil.mergeSubjectWithForwardMembers(affair.getSubject(), -1, affair.getForwardMember(), affair.getResentTime(), null));
            vo.setSubObjectId(affair.getSubObjectId());
            vo.setSubState(affair.getSubState());
            String subStateName = ResourceUtil.getString("collaboration.substate." + affair.getSubState() + ".label");
            vo.setSubStateName(subStateName);
            vo.setTempleteId(affair.getTempleteId());
            vo.setTrack(affair.getTrack());
            vo.setTransactorId(affair.getTransactorId());
            //UpdateDate将作为置顶辅助参数使用,所以置顶之后这个参数值不准,尽量不要使用
            //vo.setUpdateDate(affair.getUpdateDate());
            vo.setCoverTime(affair.isCoverTime());
            vo.setEntityId(affair.getId());

            vo.setProcessId(Strings.isBlank(affair.getProcessId()) ? 0l : Long.valueOf(affair.getProcessId()));
            //发起人(处理人)
            vo.setHandler(Functions.showMemberName(affair.getSenderId()));
            //类型
            vo.setAppName(ResourceUtil.getString("application." + affair.getApp() + ".label"));
            vo.setHasAttsFlag(AffairUtil.isHasAttachments(affair));
            affairVOList.add(vo);
        }
        return affairVOList;
    }

    private List<FlowNodeDetailAffairVO> ctpAffairHis2ctpAffairVO(List<CtpAffairHis> affairList, List<FlowNodeDetailAffairVO> affairVOList) {
        if (affairList == null || affairList.size() == 0) {
            return null;
        }
        for (CtpAffairHis affair : affairList) {
            FlowNodeDetailAffairVO vo = new FlowNodeDetailAffairVO();
            vo.setActivityId(affair.getActivityId());
            vo.setAddition(affair.getAddition());
            vo.setApp(affair.getApp());
            vo.setSubApp(affair.getSubApp());
            vo.setArchiveId(affair.getArchiveId());
            vo.setBodyType(affair.getBodyType());
            vo.setCompleteTime(affair.getCompleteTime());
            vo.setCreateDate(affair.getCreateDate());
            vo.setDeadlineDate(affair.getDeadlineDate());
            vo.setDealTermType(affair.getDealTermType());
            vo.setDealTermUserid(affair.getDealTermUserid());
            vo.setDelete(affair.isDelete());
            vo.setDueRemind(affair.isDueRemind());
            vo.setExtProps(affair.getExtProps());
            vo.setFinish(affair.isFinish());
            vo.setFormAppId(affair.getFormAppId());
//			vo.setFormId(affair.getFormId());
//            vo.setFormOperationId(affair.getFormOperationId());
//            vo.setFormViewOperation(affair.getMultiViewStr());
            vo.setForwardMember(affair.getForwardMember());
            vo.setFromId(affair.getFromId());
            vo.setHastenTimes(affair.getHastenTimes());
            vo.setIdentifier(affair.getIdentifier());
            vo.setImportantLevel(affair.getImportantLevel());
            vo.setMemberId(affair.getMemberId());
            vo.setNodePolicy(affair.getNodePolicy());
            vo.setObjectId(affair.getObjectId());
            vo.setOverTime(affair.getOverTime());
            vo.setOverWorktime(affair.getOverWorktime());
            vo.setReceiveTime(affair.getReceiveTime());
            vo.setRemindDate(affair.getRemindDate());
            vo.setRemindInterval(affair.getRemindInterval());
            vo.setResentTime(affair.getResentTime());
            vo.setRunWorktime(affair.getRunWorktime());
            vo.setSenderId(affair.getSenderId());
            vo.setState(affair.getState());
            vo.setSubApp(affair.getSubApp());
            vo.setSubject(affair.getSubject());
            vo.setSubObjectId(affair.getSubObjectId());
            vo.setSubState(affair.getSubState());
            vo.setTempleteId(affair.getTempleteId());
            vo.setTrack(affair.getTrack());
            vo.setTransactorId(affair.getTransactorId());
            vo.setUpdateDate(affair.getUpdateDate());
            vo.setCoverTime(affair.isCoverTime());
            vo.setEntityId(affair.getId());
            vo.setProcessId(affair.getProcessId() == null ? 0l : Long.valueOf(affair.getProcessId()));
            affairVOList.add(vo);
        }
        return affairVOList;
    }

    private static Integer workTime = 0;
    private static int year;

    /**
     * 将分钟数按当前工作时间转化为按天表示的时间。
     * 例如 1天7小时2分。
     */
    private String showDate(Integer minutes, boolean isWork) {
        if (minutes == null || minutes == 0)
            return "－";
        int dayH = 24 * 60;
        if (isWork) {
            Calendar cal = Calendar.getInstance();
            int y = cal.get(Calendar.YEAR);
            if (year != y || workTime.intValue() == 0) { //需要取工作时间
                workTime = getCurrentYearWorkTime();
                year = y;
            }
            if (workTime == null || workTime.intValue() == 0) {
                return "－";
            }
            dayH = workTime;
        }

        long m = minutes.longValue();
        long day = m / dayH;
        long d1 = m % dayH;
        long hour = d1 / 60;
        long minute = d1 % 60;
        String display
                = ResourceUtil.getStringByParams("collaboration.date.display",
                day > 0 ? day : "",
                day > 0 ? 1 : 0,
                hour > 0 ? hour : "",
                hour > 0 ? 1 : 0,
                minute > 0 ? minute : "",
                minute > 0 ? 1 : 0);
        //{0}{1,choice,0#|1#\u5929}{2}{3,choice,0#|1#\u5C0F\u65F6}{4}{5,choice,0#|1#\u5206}
        return display;
    }

    private int getCurrentYearWorkTime() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int t = 0;
        try {
            t = workTimeManager.getEachDayWorkTime(year, AppContext.getCurrentUser().getLoginAccount());
        } catch (WorkTimeSetExecption e) {
            log.error("", e);
        }
        return t;
    }

    /**
     * 流程日志页面数据统计
     *
     * @param processId 流程ID
     * @return
     */
    public List getProcessLogCount(Long processId) {
        return processLogManager.getProcessLogCount(processId);
    }
}
