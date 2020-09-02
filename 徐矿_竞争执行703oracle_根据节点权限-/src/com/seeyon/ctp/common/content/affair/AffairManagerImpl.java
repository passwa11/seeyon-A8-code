/**
 * $Author: muj $
 * $Rev: 16092 $
 * $Date:: 2015-02-06 17:49:48#$:
 * <p>
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 * <p>
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.common.content.affair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.utils.AgentUtil;
import com.seeyon.apps.collaboration.bo.WorkflowAnalysisParam;
import com.seeyon.apps.meeting.api.MeetingApi;
import com.seeyon.apps.xkjt.dao.XkjtDao;
import com.seeyon.apps.xkjt.po.XkjtOpenMode;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.AffairCondition.SearchModel;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.organization.bo.MemberRole;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.manager.RoleManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UniqueList;
import com.seeyon.ctp.workflow.engine.enums.ChangeType;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;


/**
 * <p>
 * Title: T1开发框架
 * </p>
 * <p>
 * Description: 内容组件封装Affair事项处理接口实现
 * </p>
 * <p>
 * Copyright: Copyright (c) 2012
 * </p>
 * <p>
 * Company: seeyon.com
 * </p>
 *
 * @since CTP2.0
 */
public class AffairManagerImpl implements AffairManager {
    private static final Log LOGGER = LogFactory.getLog(AffairManagerImpl.class);
    private AffairDao affairDao;
    private OrgManager orgManager;
    private WorkTimeManager workTimeManager;
    private MeetingApi meetingApi;
    private RoleManager roleManager;
    private XkjtDao xkjtDao;

    public XkjtDao getXkjtDao() {
        return xkjtDao;
    }

    public void setXkjtDao(XkjtDao xkjtDao) {
        this.xkjtDao = xkjtDao;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public void setRoleManager(RoleManager roleManager) {
        this.roleManager = roleManager;
    }

    public void setMeetingApi(MeetingApi meetingApi) {
        this.meetingApi = meetingApi;
    }

    public void setWorkTimeManager(WorkTimeManager workTimeManager) {
        this.workTimeManager = workTimeManager;
    }

    public AffairDao getAffairDao() {
        return affairDao;
    }

    public void setAffairDao(AffairDao affairDao) {
        this.affairDao = affairDao;
    }

    public OrgManager getOrgManager() {
        return orgManager;
    }


    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    @Override
    public List<CtpAffair> findState6(Map<String, Object> map) {
        return affairDao.findState6(map);
    }

    @Override
    public List<CtpAffair> findBycondition(Map<String, Object> map) {
        return affairDao.findByCondition(map);
    }

    @Override
    public void save(CtpAffair affair) throws BusinessException {
        affairDao.save(affair);
    }

    @Override
    public void saveAffairs(List<CtpAffair> affairs) throws BusinessException {
        affairDao.saveAffairs(affairs);
    }

    public CtpAffair get(Long id) throws BusinessException {
        if (id == null || Strings.equals(id, -1L)) {
            return null;
        }
        CtpAffair affair = affairDao.get(id);

        return affair;
    }

    public CtpAffair getByHis(Long id) throws BusinessException {
        CtpAffair affair = null;
        AffairDao affairDaoFK = (AffairDao) AppContext.getBean("affairDaoFK");
        if (affairDaoFK != null) {
            affair = affairDaoFK.getByHis(id);
        } else {
            LOGGER.error("______________________fkdao is null!");
        }

        return affair;
    }

    public CtpAffair getSenderAffair(Long summaryId) throws BusinessException {
        return affairDao.getSenderAffair(summaryId);
    }

    public CtpAffair getSenderAffairByHis(Long summaryId) throws BusinessException {
        CtpAffair affair = null;
        AffairDao affairDaoFK = (AffairDao) AppContext.getBean("affairDaoFK");
        if (affairDaoFK != null) {
            affair = affairDaoFK.getSenderAffairHis(summaryId);
        } else {
            LOGGER.error("______________________fkdao is null!");
        }
        return affair;
    }

    @Override
    public void deleteByObjectId(ApplicationCategoryEnum appEnum, Long objectId) throws BusinessException {
        affairDao.deleteByAppAndObjectId(appEnum, objectId);
    }

    @Override
    public void updateAffair(CtpAffair affair) throws BusinessException {
        affairDao.update(affair);
    }

    @Override
    public void updateAffairs(List<CtpAffair> affairs) throws BusinessException {
        DBAgent.updateAll(affairs);
    }

    public void update(String hql, Map<String, Object> params) throws BusinessException {
        affairDao.update(hql, params);
    }

    @Override
    public List<CtpAffair> getByConditions(FlipInfo flipInfo, Map conditions) throws BusinessException {
        return affairDao.getByConditions(flipInfo, conditions);
    }

    @Override
    public int getCountByConditions(Map conditions) throws BusinessException {
        return affairDao.getCountByConditions(conditions);
    }

    @Override
    public int getCountByConditionsHis(Map conditions) throws BusinessException {
        AffairDao affairDaoFK = (AffairDao) AppContext.getBean("affairDaoFK");
        if (affairDaoFK == null) {
            return 0;
        }
        return affairDaoFK.getCountByConditions(conditions);
    }

    @Override
    public List<CtpAffair> getAffairs(ApplicationCategoryEnum collaboration, Long summaryId) throws BusinessException {

        return affairDao.getAffairsByAppAndObjectId(collaboration, summaryId);
    }

    public List<CtpAffair> getAffairsHis(ApplicationCategoryEnum collaboration, Long summaryId)
            throws BusinessException {
        AffairDao affairDaoFK = (AffairDao) AppContext.getBean("affairDaoFK");
        if (null == affairDaoFK) {
            return new ArrayList();
        }
        return affairDaoFK.getAffairsByAppAndObjectIdHis(collaboration, summaryId);
    }

    @Override
    public List<CtpAffair> getAffairsHis(ApplicationCategoryEnum appEnum, Long objectId, Long membeId)
            throws BusinessException {
        List<CtpAffair> affairs = null;
        if (AppContext.hasPlugin("fk")) {

            AffairDao affairDaoFK = (AffairDao) AppContext.getBean("affairDaoFK");
            if (null == affairDaoFK) {
                affairs = Collections.emptyList();
            } else {
                affairs = affairDaoFK.getAffairsByObjectIdAndUserIdHis(appEnum, objectId, membeId);
            }
        } else {
            affairs = Collections.emptyList();
        }
        return affairs;
    }

    @Override
    public List<CtpAffair> getAffairs(ApplicationCategoryEnum appEnum, Long objectId, Long userId)
            throws BusinessException {
        return affairDao.getAffairsByObjectIdAndUserId(appEnum, objectId, userId);
    }

    @Override
    public void deleteAffair(Long id) throws BusinessException {
        affairDao.delete(id);
    }

    public void deletePhysical(Long id) throws BusinessException {
        affairDao.deletePhysicalById(id);
    }

    public void deletePhysicalByObjectId(Long objectId) throws BusinessException {
        affairDao.deletePhysicalByObjectId(objectId);
    }

    public void deletePhysical(Long objectId, Long memberId) throws BusinessException {
        affairDao.deletePhysicalByObjectIdAndMemberId(objectId, memberId);
    }

    public void deletePhysical(ApplicationCategoryEnum app, Long objectId, Long memberId) throws BusinessException {
        affairDao.deletePhysicalByAppAndObjectIdAndMemberId(app, objectId, memberId);
    }


    @Override
    public void deleteAffair(Long objectId, Long memberId)
            throws BusinessException {
        affairDao.deleteByObjectIdAndMemberId(objectId, memberId);
    }

    @Override
    public CtpAffair getAffairBySubObjectId(Long subObjectId) throws BusinessException {
        CtpAffair affair = affairDao.getAffairBySubObjectId(subObjectId);
        if (affair == null) {
            AffairDao affairDaoFK = (AffairDao) AppContext.getBean("affairDaoFK");
            if (affairDaoFK != null) {
                affair = affairDaoFK.getAffairBySubObjectId(subObjectId);
            }
        }
        return affair;
    }

    @Override
    public List<CtpAffair> getValidAffairs(
            ApplicationCategoryEnum appEnum, Long objectId)
            throws BusinessException {
        return affairDao.getAvailabilityAffairsByAppAndObjectId(appEnum, objectId);
    }

    @Override
    public List<CtpAffair> getValidAffairsHis(ApplicationCategoryEnum appEnum, Long objectId) throws BusinessException {

        List<CtpAffair> list = new ArrayList<CtpAffair>();
        if (AppContext.hasPlugin("fk")) {
            AffairDao affairDaoFK = (AffairDao) AppContext.getBean("affairDaoFK");
            if (affairDaoFK != null) {
                list = affairDaoFK.getAvailabilityAffairsByAppAndObjectIdHis(appEnum, objectId);
            }
        }
        return list;
    }


    @Override
    public List<CtpAffair> getValidTrackAffairs(Long objectId) throws BusinessException {
        return affairDao.getAvailabilityTrackingAffairsBySummaryId(objectId);
    }


    @Override
    public void updateAffairsState2Cancel(Long summaryId) throws BusinessException {
        affairDao.updateAffairsStateAndUpdateDate(summaryId);
    }

    public Date getMinStartTimePending(Long memberId) throws BusinessException {
        return this.affairDao.getMinStartTimePending(memberId);
    }

    @Override
    public List<CtpAffair> getAffairsByObjectIdAndNodeId(Long objectId, Long activityId) throws BusinessException {
        List<CtpAffair> affairs = affairDao.getAffairsByObjectIdAndActivityId(objectId, activityId);
        if (AppContext.hasPlugin("fk") && Strings.isEmpty(affairs)) {
            AffairDao affairDaoFK = (AffairDao) AppContext.getBean("affairDaoFK");
            affairs = affairDaoFK.getAffairsByObjectIdAndActivityId(objectId, activityId);
        }
        return affairs;
    }

    @Override
    public List<CtpAffair> getAffairs(Long objectId, StateEnum state) throws BusinessException {
        return affairDao.getAffairsByObjectIdAndState(objectId, state);
    }

    public List<CtpAffair> getAffairs(Long objectId, StateEnum state, SubStateEnum subState) throws BusinessException {
        return affairDao.getAffairs(objectId, state, subState);
    }

    @Override
    public void deleteBySubObject(ApplicationCategoryEnum appEnum,
                                  Long subObjectId) throws BusinessException {
        this.affairDao.deleteByAppAndSubObjectId(appEnum, subObjectId);
    }

    @Override
    public void update(Long affairId, Map<String, Object> columnValue)
            throws BusinessException {
        this.update(columnValue, new Object[][]{{"id", affairId}});
    }

    @Override
    public void update(Map<String, Object> columns, Object[][] wheres)
            throws BusinessException {
        affairDao.update(columns, wheres);
    }

    @Override
    public List<CtpAffair> getTrackAndPendingAffairs(
            Long objectId, Integer app) throws BusinessException {
        return affairDao.getTrackingAndPendingAffairBySummaryId(objectId, app);
    }

    @Override
    public void updateByObjectIdAndSubObjIds(StateEnum stateEnum, SubStateEnum subStateEnum, Long objectId,
                                             List<Long> subObjectIds) throws BusinessException {
        affairDao.updatePendingAndDoneAffairsByObjectIdAndSubObjectIds(stateEnum, subStateEnum, objectId, subObjectIds);
    }

    @Override
    public int getTrackCount4BizConfig(Long memberId, List<Long> tempIds) {
        return affairDao.getTrackCount4BizConfig(memberId, tempIds);
    }

    @Override
    public void updateFinishFlag(Long objectId) {
        affairDao.updateFinishFlag(objectId);
    }

    @Override
    public List<CtpAffair> getAffairsByNodeId(Long activityId) {
        return affairDao.getAffairsByActivityId(activityId);
    }

    @Override
    public List<CtpAffair> getValidAffairs(FlipInfo flipInfo, Map params) throws BusinessException {

        return this.affairDao.getALLAvailabilityAffairList(flipInfo, params);

    }

    @Override
    public List<CtpAffair> getValidAffairsHis(FlipInfo flipInfo, Map<String, Object> params) throws BusinessException {
        AffairDao affairDaoFK = (AffairDao) AppContext.getBean("affairDaoFK");
        if (affairDaoFK != null) {
            return affairDaoFK.getALLAvailabilityAffairList(flipInfo, params);
        } else {
            return new ArrayList<CtpAffair>();
        }
    }

    /**
     * yangwulin 提供F111接口
     * @param flipInfo 分页对象
     * @param params  需要设置的参数 memberId、senderId
     * @return
     */
    public List<CtpAffair> getWorkflowRelatedAffairs(FlipInfo flipInfo, Map params) throws BusinessException {
        List<CtpAffair> listCtpAffair = new ArrayList<CtpAffair>();
        Object isReceive = params.get("isReceive");
        if (null != isReceive && !(Boolean) isReceive) {
            listCtpAffair = this.affairDao.getSenderColAndEdocList(flipInfo, params);
        } else {
            listCtpAffair = this.affairDao.getSenderOrMemberColAndEdocList(flipInfo, params);
        }
        return listCtpAffair;
    }


    /**
     * yangwulin  提供给F111接口
     * @param flipInfo 分页对象
     * @param params 需要设置的参数 memberId、senderId
     * @return List<CtpAffair>
     * @throws BusinessException
     */
    public List<CtpAffair> getSenderOrMemberMtList(FlipInfo flipInfo, Map params) throws BusinessException {
        List<CtpAffair> listCtpAffair = this.affairDao.getSenderOrMemberMtList(flipInfo, params);
        return listCtpAffair;
    }

    public Map<Long, Integer> getOverNodeCount(WorkflowAnalysisParam param) {

        Long templateId = param.getTempleteId();
        Long accountId = param.getOrgAccountId();
        boolean isCol = param.isCol();
        List<Integer> states = param.getWorkFlowStates();
        Date startDate = param.getStartDate();
        Date endDate = param.getEndDate();

        return this.affairDao.getOverNodeCount(templateId, accountId, isCol, states, startDate, endDate);
    }

    public Map<Long, String> getNodeCountAndSumRunTime(WorkflowAnalysisParam param) {

        Long templateId = param.getTempleteId();
        Long accountId = param.getOrgAccountId();
        boolean isCol = param.isCol();
        List<Integer> states = param.getWorkFlowStates();
        Date startDate = param.getStartDate();
        Date endDate = param.getEndDate();

        return this.affairDao.getNodeCountAndSumRunTime(templateId, accountId, isCol, states, startDate, endDate);
    }

    public List<CtpAffair> getAffairByActivityId(WorkflowAnalysisParam param) {

        Long templateId = param.getTempleteId();
        Long accountId = param.getOrgAccountId();
        boolean isCol = param.isCol();
        List<Integer> states = param.getWorkFlowStates();
        Date startDate = param.getStartDate();
        Date endDate = param.getEndDate();
        Long activityId = param.getActivityId();

        return this.affairDao.getAffairByActivityId(templateId, accountId, isCol, states, activityId, startDate, endDate);
    }

    public Map<Long, String> getStaticsByActivityId(WorkflowAnalysisParam param) {

        Long templateId = param.getTempleteId();
        Long accountId = param.getOrgAccountId();
        boolean isCol = param.isCol();
        List<Integer> states = param.getWorkFlowStates();
        Date startDate = param.getStartDate();
        Date endDate = param.getEndDate();
        Long activityId = param.getActivityId();

        return this.affairDao.getStaticsByActivityId(templateId, accountId, isCol, states, activityId, startDate, endDate);
    }

    public Map<Long, Integer> getOverCountByMember(WorkflowAnalysisParam param) {

        Long templateId = param.getTempleteId();
        Long accountId = param.getOrgAccountId();
        boolean isCol = param.isCol();
        List<Integer> states = param.getWorkFlowStates();
        Date startDate = param.getStartDate();
        Date endDate = param.getEndDate();
        Long activityId = param.getActivityId();

        return this.affairDao.getOverCountByMember(templateId, accountId, isCol, states, activityId, startDate, endDate);
    }


    /**
     * IDX_REF_A_O(ObjectID)
     * IDX_AFFAIR_STATE(State)
     */

    public void updateSentPigeonholeInfo(Long summaryId, Long archiveId) {
        String hql = "update " + CtpAffair.class.getName() + " a set a.archiveId=? where a.objectId=? and a.state=?";
        Object[] values = {archiveId, summaryId, StateEnum.col_sent.key()};//协同待办
        DBAgent.bulkUpdate(hql, values);
    }

    /**
     * IDX_REF_A_O(ObjectID)
     */
    public void updateAllPigeonholeInfo(Long summaryId, Long archiveId) {
        String hql = "update " + CtpAffair.class.getName() + " a set a.archiveId=? where a.archiveId is null and a.objectId=? ";
        Object[] values = {archiveId, summaryId};//协同待办
        DBAgent.bulkUpdate(hql, values);
    }

    public List<Long> getAffairMemberIds(ApplicationCategoryEnum app, Long id) {
        List<Long> memberIds = affairDao.getMemberIdListByAppAndObjectId(app, id);
        if (memberIds == null || memberIds.size() == 0) {
            AffairDao affairDaoFK = (AffairDao) AppContext.getBean("affairDaoFK");
            if (affairDaoFK != null) {
                memberIds = affairDaoFK.getMemberIdListByAppAndObjectIdHis(app, id);
            }
        }
        return memberIds;
    }

    @Override
    public List<Long> findMembers(ApplicationCategoryEnum category, Long objectId,
                                  List<StateEnum> states, FlipInfo flp) throws BusinessException {
        return affairDao.findMembers(category, objectId, states, flp);
    }

    public List<CtpAffair> getPendingAffairs(Long summaryId, List<Long> nodeIds) throws BusinessException {
        return affairDao.getPendingAffairListByNodes(summaryId, nodeIds);
    }

    public boolean isAffairInProcess(ApplicationCategoryEnum app,
                                     Long objectId, List<Long> memberIds) {
        return affairDao.checkPermission4TheObject(app, objectId, memberIds);
    }

    @Override
    public void updateAffairs(ApplicationCategoryEnum appEnum,
                              Long objectId, Map<String, Object> parameter) {
        affairDao.updateAllAvailabilityAffair(appEnum, objectId, parameter);
    }


    public List<CtpAffair> getAffairsHis(Long objectId, Long activityId) throws BusinessException {
        AffairDao affairDaoFK = (AffairDao) AppContext.getBean("affairDaoFK");
        if (affairDaoFK != null) {
            return affairDaoFK.getAffairsByActivityId(objectId, activityId);
        } else {
            LOGGER.error("______________________fkdao is null!");
            return null;
        }

    }

    public CtpAffair getEdocSenderAffair(Long objectId) throws BusinessException {
        CtpAffair affair = affairDao.getEdocSenderAffair(objectId);

        return affair;
    }

    @Override
    public List<CtpAffair> getAffairs(ApplicationCategoryEnum appEnum, Long objectId, Long subObjectId, Long memberId) throws BusinessException {
        return affairDao.getAffairsByObjectIdAndSubObjectIdAndUserId(appEnum, objectId, subObjectId, memberId);
    }

    public int getCountAffairsByAppsAndStatesAndMemberId(List<ApplicationCategoryEnum> appEnums, List<StateEnum> statesEnums, Long memberId) {
        return affairDao.getCountAffairsByAppsAndStatesAndMemberId(appEnums, statesEnums, memberId);
    }

    public List<CtpAffair> getAffairsByAppsAndStatesAndMemberId(FlipInfo flipInfo, List<ApplicationCategoryEnum> appEnums, List<StateEnum> statesEnums, Long memberId) {
        return affairDao.getAffairsByAppsAndStatesAndMemberId(flipInfo, appEnums, statesEnums, memberId);
    }

    @Override
    public List<CtpAffair> getAffairs(Long objectId, List<StateEnum> states) throws BusinessException {
        List<Integer> _states = new ArrayList<Integer>();
        if (Strings.isNotEmpty(states)) {
            for (StateEnum state : states) {
                _states.add(state.getKey());
            }
        }
        List<CtpAffair> list = affairDao.getAffairsByObjectIdAndStates(objectId, _states);
        if (AppContext.hasPlugin("fk") && Strings.isEmpty(list)) {
            AffairDao affairDaoFK = (AffairDao) AppContext.getBean("affairDaoFK");
            list = affairDaoFK.getAffairsByObjectIdAndStates(objectId, _states);
        }

        return list;
    }

    @Override
    public String getErrorMsgByAffair(CtpAffair affair) throws BusinessException {


        String state = "";
        String msg = "";
        if (affair != null) {
            String forwardMemberId = affair.getForwardMember();
            int forwardMemberFlag = 0;
            String forwardMember = null;
            if (Strings.isNotBlank(forwardMemberId)) {
                try {
                    forwardMember = getOrgManager().getMemberById(Long.parseLong(forwardMemberId)).getName();
                    forwardMemberFlag = 1;
                } catch (Exception e) {
                    LOGGER.info(e.getLocalizedMessage());
                }
            }

            if (affair.isDelete()) {
                state = ResourceUtil.getString("collaboration.state.9.delete");
            } else {
                switch (StateEnum.valueOf(affair.getState())) {
                    case col_done:
                        state = ResourceUtil.getString("collaboration.state.4.done");
                        break;
                    case col_cancel:
                        state = ResourceUtil.getString("collaboration.state.5.cancel");
                        break;
                    case col_stepBack:
                        state = ResourceUtil.getString("collaboration.state.6.stepback");
                        break;
                    case col_takeBack:
                        state = ResourceUtil.getString("collaboration.state.7.takeback");
                        break;
                    case col_competeOver:
                        state = ResourceUtil.getString("collaboration.state.8.strife");
                        break;
                    case col_stepStop:
                        state = ResourceUtil.getString("collaboration.state.10.stepstop");
                        break;
                    case col_waitSend:
                        switch (SubStateEnum.valueOf(affair.getSubState())) {
                            case col_waitSend_stepBack:
                                state = ResourceUtil.getString("collaboration.state.6.stepback");
                                break;
                            case col_waitSend_cancel:
                                state = ResourceUtil.getString("collaboration.state.5.cancel");
                                break;
                            case col_pending_specialBackToSenderCancel:
                                state = ResourceUtil.getString("collaboration.state.6.stepback");
                                break;
                        }
                        break;
                }
            }
            String appName = ResourceUtil.getString("application." + affair.getApp() + ".label");
            msg = ResourceUtil.getString("collaboration.state.invalidation.alert", affair.getSubject(), state, appName, forwardMemberFlag, forwardMember);
        } else {
            state = ResourceUtil.getString("collaboration.state.9.delete");
            msg = ResourceUtil.getString("collaboration.state.inexistence.alert", state);
        }
        return msg;

    }

    public int getAgentPendingCount(Long memberId) throws BusinessException {
        Object[] agentObj = AgentUtil.getUserAgentToMap(memberId);
        boolean agentToFlag = (Boolean) agentObj[0];
        Map<Integer, List<AgentModel>> ma = (Map<Integer, List<AgentModel>>) agentObj[1];

        AffairCondition condition = new AffairCondition(memberId, StateEnum.col_pending);
        condition.setAgent(agentToFlag, ma);
        return condition.getAgentPendingCount();
    }

    @Override
    public void updateAffairReaded(CtpAffair affair) throws BusinessException {

        Integer sub_state = affair.getSubState();
        if (sub_state == null || sub_state.intValue() == SubStateEnum.col_pending_unRead.key()) {

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("subState", SubStateEnum.col_pending_read.key());
            update(affair.getId(), map);

            //要把已读状态写写进流程
            if (affair.getSubObjectId() != null) {
                try {
                    WorkflowApiManager wapi = (WorkflowApiManager) AppContext.getBean("wapi");
                    wapi.readWorkItem(affair.getSubObjectId());
                } catch (BPMException e) {
                    LOGGER.error("", e);
                    throw new BusinessException(e);
                }
            }
        }
    }


    @Override
    public boolean isAffairValid(CtpAffair affair, Boolean isDeleteValid) {
        if (affair == null) {
            return false;
        }
        StateEnum state = StateEnum.valueOf(affair.getState());

        boolean isSpecail = false;
        boolean isUserSelf = false;
        if (null != AppContext.getCurrentUser()) {
            isUserSelf = AppContext.getCurrentUser().getId().equals(affair.getSenderId());
        }
        if (affair.getSubState() != null) {
            SubStateEnum subState = SubStateEnum.valueOf(affair.getSubState());
            isSpecail = SubStateEnum.col_pending_specialBacked.equals(subState) || SubStateEnum.col_pending_specialBackToSenderCancel.equals(subState);
        }

        if (affair == null
                || StateEnum.col_cancel.equals(state)
                || StateEnum.col_stepBack.equals(state)
                || StateEnum.col_takeBack.equals(state)
                || StateEnum.col_stepStop.equals(state)
                || StateEnum.col_competeOver.equals(state)
                || StateEnum.col_competeOver.equals(state)
                || (StateEnum.col_waitSend.equals(state) && !isSpecail && !isUserSelf)
                || (!isDeleteValid && affair.isDelete())) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public List<CtpAffair> getAffairs(long objectId) throws BusinessException {
        return affairDao.getAffairsByObjectId(objectId);
    }

    @Override
    public Object getAffairListBySender(Long memberId, String orgStr,
                                        AffairCondition condition, boolean onlyCount, FlipInfo fi, List<Integer> appEnum, String... groupByPropertyName) {
        return getAffairListBySender(memberId, orgStr, condition, onlyCount, fi, appEnum, false, groupByPropertyName);
    }

    @Override
    public Object getAffairListBySender(Long memberId, String orgStr,
                                        AffairCondition condition, boolean onlyCount, FlipInfo fi, List<Integer> appEnum, boolean isGroupBy, String... groupByPropertyName) {
        //if(orgStr == null||"".equals(orgStr)) return new ArrayList<CtpAffair>();
        String[] types = (orgStr == null ? "" : orgStr).split("[,]");
        boolean isAccount = false;
        boolean isDepartment = false;
        boolean isMember = false;
        boolean isTeam = false;
        boolean isPost = false;
        boolean isPeopleRelate = false;
        List<Long> accountIds = new ArrayList<Long>();
        List<Long> departmentIds = new ArrayList<Long>();
        List<Long> postIds = new ArrayList<Long>();
        List<Long> peopleRelateTypes = new ArrayList<Long>();
        List<Long> memberIds = new ArrayList<Long>();
        List<Long> teamIds = new ArrayList<Long>();
        for (int i = 0; i < types.length; i++) {
            String singleType[] = types[i].split("[|]");
            if ("Account".equals(singleType[0])) {
                try {
                    if (!orgManager.getAccountById(Long.valueOf(singleType[1])).isGroup()) {//待办更多组织分类查询集团待办
                        isAccount = true;
                        accountIds.add(Long.valueOf(singleType[1]));
                        List<V3xOrgAccount> accounts = orgManager.getChildAccount(Long.valueOf(singleType[1]), false);
                        for (V3xOrgAccount account : accounts) {
                            accountIds.add(account.getId());
                        }
                    }
                } catch (NumberFormatException e) {
                    LOGGER.error("", e);
                } catch (BusinessException e) {
                    LOGGER.error("", e);
                }
            } else if ("Department".equals(singleType[0])) {
                isDepartment = true;
                departmentIds.add(Long.valueOf(singleType[1]));
                try {
                    //包含子部门
                    List<V3xOrgDepartment> depts = orgManager.getChildDepartments(Long.valueOf(singleType[1]), false);
                    for (V3xOrgDepartment dept : depts) {
                        departmentIds.add(dept.getId());
                    }
                    //外单位||编外单位
                    List<V3xOrgDepartment> otherdepts = orgManager.getChildDepartments(Long.valueOf(singleType[1]), false, false);
                    for (V3xOrgDepartment dept : otherdepts) {
                        departmentIds.add(dept.getId());
                    }
                } catch (NumberFormatException e) {
                    LOGGER.error("", e);
                } catch (BusinessException e) {
                    LOGGER.error("", e);
                }
            } else if ("Member".equals(singleType[0])) {
                isMember = true;
                memberIds.add(Long.valueOf(singleType[1]));
            } else if ("Post".equals(singleType[0])) {
                isPost = true;
                postIds.add(Long.valueOf(singleType[1]));
            } else if ("RelatePeople".equals(singleType[0])) {
                isPeopleRelate = true;
                peopleRelateTypes.add(Long.valueOf(singleType[1]));
            } else if ("Team".equals(singleType[0])) {
                isTeam = true;
                teamIds.add(Long.valueOf(singleType[1]));
            }
        }
        final StringBuilder hql = new StringBuilder();
        final Map<String, Object> parameter = new HashMap<String, Object>();


        StateEnum stateEnum = null;
        if (condition.getState() == null) {
            stateEnum = StateEnum.col_pending;
        } else {
            stateEnum = condition.getState();
        }

        if (!isGroupBy) {
            hql.append("from ctp_affair affair ");
        }
        if (isMember || isPost || isDepartment || isAccount) {
            hql.append(" inner join org_member m on affair.sender_id= m.id ");
        }
        if (isPeopleRelate) {
            hql.append(" left join relate_member pr on affair.member_id = pr.related_id  ");
        }
        if (isTeam) {
            hql.append(" left join org_relationship rship on affair.sender_id = rship.objective0_id  ");
            hql.append(" left join org_team rteam on rship.source_id = rteam.id  ");
        }
        hql.append(" where ");
        if (appEnum.size() > 0 && !condition.getVjoin()) {
            hql.append(" affair.app in (:appEnum) and ");
            parameter.put("appEnum", appEnum);
        } else if (condition.getVjoin()) {//移动端只获取协同类
            if (stateEnum != null && StateEnum.col_pending.equals(stateEnum)) {//待办查询协同和公文(移动端只能查看发文\收文\签报)
                List<Integer> appList = new ArrayList<Integer>();
                appList.add(ApplicationCategoryEnum.edoc.key());
                appList.add(ApplicationCategoryEnum.collaboration.key());
                appList.add(ApplicationCategoryEnum.edocSend.key());
                appList.add(ApplicationCategoryEnum.edocRec.key());
                appList.add(ApplicationCategoryEnum.edocSign.key());
                appList.add(ApplicationCategoryEnum.edocRegister.key());
                appList.add(ApplicationCategoryEnum.edocRecDistribute.key());
                appList.add(ApplicationCategoryEnum.exchange.key());
                appList.add(ApplicationCategoryEnum.exSend.key());
                appList.add(ApplicationCategoryEnum.exSign.key());

                hql.append(" affair.app in (:appEnum) and ");
                parameter.put("appEnum", appList);
            } else {
                hql.append(" affair.app = :appEnum and ");
                parameter.put("appEnum", ApplicationCategoryEnum.collaboration.key());
            }
        }

        hql.append(" affair.member_id = :memberId ");

        hql.append(" and affair.sub_state != :substate  ");
        parameter.put("substate", SubStateEnum.meeting_pending_periodicity.getKey());

        //当查已办affair时，还需要加上complete_time不为空的条件
        if (stateEnum.key() == StateEnum.col_done.key()) {
            hql.append(" and affair.complete_time is not null ");
        }
        //跟踪栏目
        if (condition.getIsTrack() != null && condition.getIsTrack()) {
            hql.append(" and affair.track !=:track ");
            parameter.put("track", 0);
            hql.append(" and affair.state in (:states) ");
            List<Integer> stateList = new ArrayList<Integer>();
            stateList.add(StateEnum.col_sent.key());
            stateList.add(StateEnum.col_done.key());
            stateList.add(StateEnum.col_pending.key());
            parameter.put("states", stateList);
        } else if (StateEnum.col_done.getKey() == stateEnum.key()) {
            hql.append(" and affair.state = :state  and  affair.app not in(:notInApp) ");
            //当查待办和已办affair时先直接设置外面传来的stateEnum
            parameter.put("state", stateEnum.key());
            List<Integer> notInApp = new ArrayList<Integer>();
            notInApp.add(ApplicationCategoryEnum.edocRegister.key());
            notInApp.add(ApplicationCategoryEnum.edocRecDistribute.getKey());
            parameter.put("notInApp", notInApp);
        } else if (StateEnum.col_sent.getKey() == stateEnum.key()) {
            hql.append(" and affair.state = :state  and  affair.app not in(:notInApp) ");
            //当查待办和已办affair时先直接设置外面传来的stateEnum
            parameter.put("state", stateEnum.key());
            List<Integer> notInApp = new ArrayList<Integer>();
            notInApp.add(ApplicationCategoryEnum.meeting.key());
            parameter.put("notInApp", notInApp);
        } else {
            hql.append(" and affair.state = :state ");
            //当查待办和已办affair时先直接设置外面传来的stateEnum
            parameter.put("state", stateEnum.key());
            if (stateEnum.key() == StateEnum.col_pending.key()) {
                hql.append(" and affair.sub_State not in (:subState)");
                List sub_State = new ArrayList();
                sub_State.add(SubStateEnum.col_pending_specialBack.getKey());
                sub_State.add(SubStateEnum.col_pending_specialBackCenter.getKey());
                parameter.put("subState", sub_State);
            }
        }


        hql.append(" and affair.is_delete = :isDelete ");
        parameter.put("memberId", memberId);
        parameter.put("isDelete", false);
        hql.append(" and ");
        //代理信息
        hql.append(getCondition4Agent(condition, parameter, false));
        if (isMember || isAccount || isDepartment || isPost || isPeopleRelate || isTeam) {
            hql.append(" and ");
            hql.append(" ( ");
        }
        boolean hasCondition = false;
        if (isPeopleRelate) {
            if (hasCondition) {
                hql.append(" or ");
            }
            hql.append(" (pr.relate_member_id = affair.sender_id and pr.relate_type in (:relateType))");
            parameter.put("relateType", peopleRelateTypes);
            hasCondition = true;
        }
        if (isMember) {
            if (hasCondition) {
                hql.append(" or ");
            }
            if (memberIds.size() > 1) {
                hql.append(" m.id in (:senderId)");
                parameter.put("senderId", memberIds);
            } else {
                hql.append(" m.id  = :senderId");
                parameter.put("senderId", memberIds.get(0));
            }
            hasCondition = true;
        }
        if (isAccount) {
            if (hasCondition) {
                hql.append(" or ");
            }
            if (accountIds.size() > 1) {
                hql.append(" m.org_account_id in (:accountId) ");
                parameter.put("accountId", accountIds);
            } else {
                hql.append(" m.org_account_id = :accountId ");
                parameter.put("accountId", accountIds.get(0));
            }
            hasCondition = true;
        }
        if (isDepartment) {
            if (hasCondition) {
                hql.append(" or ");
            }
            if (departmentIds.size() > 1) {
                hql.append(" m.org_department_id in (:orgDepartmentId)");
                parameter.put("orgDepartmentId", departmentIds);
            } else {
                hql.append(" m.org_department_id = :orgDepartmentId");
                parameter.put("orgDepartmentId", departmentIds.get(0));
            }
            hasCondition = true;
        }
        if (isPost) {
            if (hasCondition) {
                hql.append(" or ");
            }
            if (postIds.size() > 1) {
                hql.append(" m.org_post_id in (:orgPostId) ");
                parameter.put("orgPostId", postIds);
            } else {
                hql.append(" m.org_post_id = :orgPostId ");
                parameter.put("orgPostId", postIds.get(0));
            }

            hasCondition = true;
        }

        if (isTeam) {
            if (hasCondition) {
                hql.append(" or ");
            }
            hql.append(" rteam.id in (:orgTeamIds)");
            parameter.put("orgTeamIds", teamIds);
            hasCondition = true;
        }
        if (isMember || isAccount || isDepartment || isPost || isPeopleRelate || isTeam) {
            hql.append(" ) ");
        }
        String searchHql = condition.getSearchHql(parameter);
        if (Strings.isNotBlank(searchHql)) {
            hql.append(searchHql);
        }
        StringBuilder sbHql = new StringBuilder();
        if (isGroupBy) {
            sbHql.append(" from ctp_affair affair ");
            sbHql.append(hql);
            sbHql.append(" and affair.complete_time in (select max(complete_time) from ctp_affair affair ");
            sbHql.append(hql);
            sbHql.append(" group by affair.object_id ) ");

        } else {
            sbHql.append(hql);
        }
        return affairDao.getAffairListBySender(sbHql.toString(), parameter, onlyCount, fi, groupByPropertyName);
    }

    @Override
    public CtpAffair getSimpleAffair(Long id) throws BusinessException {
        CtpAffair affair = affairDao.getSimpleAffair(id);
        if (AppContext.hasPlugin("fk") && affair == null) {
            AffairDao affairDaoFK = (AffairDao) AppContext.getBean("affairDaoFK");
            affair = affairDaoFK.getSimpleAffair(Long.valueOf(id));
        }
        return affair;
    }

    public void updateSignleViewTimes(List<Map<String, String>> viewRecordMaps) throws BusinessException {
        List<List> affairDatas = new ArrayList<List>(viewRecordMaps.size());

        for (Map<String, String> viewRecordMap : viewRecordMaps) {
            if (viewRecordMap != null && viewRecordMap.size() > 0) {
                try {
                    String viewPeriod = viewRecordMap.get("signleViewPeriod");
                    String _firstViewTime = viewRecordMap.get("firstViewTime");

                    if (Strings.isBlank(viewPeriod) && Strings.isNotBlank(_firstViewTime)) {

                        String _affairId = viewRecordMap.get("affairId");
                        String _accountId = viewRecordMap.get("accountId");

                        Long affairId = Long.valueOf(_affairId);
                        Long accountId = Long.valueOf(_accountId);
                        Long firstViewTime = Long.valueOf(_firstViewTime);

                        Date viewTime = new Date(firstViewTime);
                        Date nowTime = new Date();
                        long t = workTimeManager.getDealWithTimeValue(viewTime, nowTime, accountId);

                        affairDatas.add(Strings.newArrayList(affairId, t));
                    }
                } catch (Exception e) {
                    LOGGER.error("记录第一次关闭窗口时间异常", e);
                }
            }
        }

        JDBCAgent jdbcAgent = new JDBCAgent(true);
        try {
            jdbcAgent.batch1Prepare("update ctp_affair set signleview_period=? where id=?");
            for (List<Object> objects : affairDatas) {
                jdbcAgent.batch2Add(objects);
            }
            jdbcAgent.batch3Execute();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            jdbcAgent.close();
        }
    }

    public void updateSignleViewTime(Map<String, String> viewRecordMap) throws BusinessException {

        if (viewRecordMap != null && viewRecordMap.size() > 0) {
            try {

                String viewPeriod = viewRecordMap.get("signleViewPeriod");
                String _firstViewTime = viewRecordMap.get("firstViewTime");

                if (Strings.isBlank(viewPeriod) && Strings.isNotBlank(_firstViewTime)) {

                    //怕事务出事，还是更新单个字段
                    Map<String, Object> m = new HashMap<String, Object>();

                    String _affairId = viewRecordMap.get("affairId");
                    String _accountId = viewRecordMap.get("accountId");

                    Long affairId = Long.valueOf(_affairId);
                    Long accountId = Long.valueOf(_accountId);
                    Long firstViewTime = Long.valueOf(_firstViewTime);

                    Date viewTime = new Date(firstViewTime);
                    Date nowTime = new Date();
                    long t = workTimeManager.getDealWithTimeValue(viewTime, nowTime, accountId);

                    m.put("signleViewPeriod", t);
                    this.update(affairId, m);
                }
            } catch (Exception e) {
                LOGGER.error("记录第一次关闭窗口时间异常", e);
            }
        }
    }

    @Override
    public void updateSignleViewTime(CtpAffair affair) throws BusinessException {

        if (affair != null) {

            Map<String, String> viewRecordMap = new HashMap<String, String>();
            viewRecordMap.put("affairId", affair.getId().toString());
            viewRecordMap.put("accountId", affair.getOrgAccountId().toString());

            if (affair.getFirstViewDate() != null) {
                viewRecordMap.put("firstViewTime", String.valueOf(affair.getFirstViewDate().getTime()));
            }

            if (affair.getSignleViewPeriod() != null) {
                viewRecordMap.put("signleViewPeriod", affair.getSignleViewPeriod().toString());
            }

            updateSignleViewTime(viewRecordMap);
        }
    }

    @Override
    public void updateAffairAnalyzeData(CtpAffair affair) throws BusinessException {
        Map<String, Object> updateMap = new HashMap<String, Object>();
        Date nowTime = new Date();
        //记录第一次处理时间
        if (affair.getSignleViewPeriod() == null && affair.getFirstViewDate() != null) {
            long viewTime = workTimeManager.getDealWithTimeValue(affair.getFirstViewDate(), nowTime, affair.getOrgAccountId());
            updateMap.put("signleViewPeriod", viewTime);
        }

        //回退，记录第一次操作时间
        if (affair.getFirstResponsePeriod() == null) {
            long responseTime = workTimeManager.getDealWithTimeValue(affair.getReceiveTime(), nowTime, affair.getOrgAccountId());

            //前面的事物没有提交，只能通过修改单独的字段进行更新
            updateMap.put("firstResponsePeriod", responseTime);
        }
        if (!updateMap.isEmpty()) {
            this.update(affair.getId(), updateMap);
        }
    }

    @Override
    public List<CtpAffair> getAffairsByObjectIdAndStates(FlipInfo flipInfo, Long objectId, List<Integer> states) throws BusinessException {
        return affairDao.getAffairsByObjectIdAndStates(flipInfo, objectId, states);
    }


    @Override
    public Object getDeduplicationAffairs(Long memberId, AffairCondition condition, boolean onlyCount, FlipInfo fi) throws BusinessException {

        final StringBuilder hql = new StringBuilder();
        final Map<String, Object> parameter = new HashMap<String, Object>();


        StateEnum stateEnum = null;
        if (condition.getState() == null) {
            stateEnum = StateEnum.col_pending;
        } else {
            stateEnum = condition.getState();
        }

        hql.append(" from ctp_affair affair,");
        hql.append(" (select max(affair2.id) id from ctp_affair affair2 where affair2.member_id = :memberId and affair2.state = :state");
        hql.append(" and affair2.is_delete = :isDelete ");
        hql.append(" and ((affair2.app = :collaborationApp and affair2.archive_id is null)  or ( affair2.app not in(:notInApp) ))");
        parameter.put("memberId", memberId);
        parameter.put("state", StateEnum.col_done.getKey());
        parameter.put("isDelete", false);
        parameter.put("collaborationApp", ApplicationCategoryEnum.collaboration.getKey());

        List<Integer> notInApp = new ArrayList<Integer>();
        notInApp.add(Integer.valueOf(ApplicationCategoryEnum.collaboration.getKey()));
        notInApp.add(Integer.valueOf(ApplicationCategoryEnum.edocRegister.key()));
        notInApp.add(Integer.valueOf(ApplicationCategoryEnum.edocRecDistribute.getKey()));
        parameter.put("notInApp", notInApp);
        Boolean isSourcesRelationOr = condition.getIsSourcesRelationOr();

        if (condition.getVjoin()) {
            hql.append(" and affair.app = :vjoinApp");
            parameter.put("vjoinApp", ApplicationCategoryEnum.collaboration.key());
        }

        Set<SearchModel> searchList = condition.getSearchList();
        StringBuilder searchHql = new StringBuilder();
        StringBuilder portalSearchHql = new StringBuilder();
        if (searchList != null && !searchList.isEmpty()) {
            for (SearchModel model : searchList) {
                boolean isPortalMore = model.isMorePageSearch();
                switch (model.getSearchCondition()) {
                    case moduleId:
                        if (Strings.isNotBlank(model.getSearchValue1())) {
                            StringBuilder moduleIdHql = new StringBuilder(" affair2.objectId = :muduleIdObjectId");
                            parameter.put("muduleIdObjectId", model.getSearchValue1());
                            if (isPortalMore) {
                                appendHql(portalSearchHql, false, moduleIdHql);
                            } else {
                                appendHql(searchHql, isSourcesRelationOr, moduleIdHql);
                            }
                        }
                        break;
                    case subject:
                        if (Strings.isNotBlank(model.getSearchValue1())) {
                            StringBuilder subjectHql = new StringBuilder(" affair2.subject like :subject");
                            parameter.put("subject", "%" + SQLWildcardUtil.escape(model.getSearchValue1()) + "%");
                            if (isPortalMore) {
                                appendHql(portalSearchHql, false, subjectHql);
                            } else {
                                appendHql(searchHql, isSourcesRelationOr, subjectHql);
                            }
                        }
                        break;
                    case importLevel:
                        if (Strings.isNotBlank(model.getSearchValue1())) {
                            StringBuilder importLevelHql = new StringBuilder();
                            String[] imps = model.getSearchValue1().split("[,]");
                            List<Integer> importantList = new ArrayList<Integer>();
                            List<Integer> newList = new ArrayList<Integer>();
                            for (int i = 0; i < imps.length; i++) {
                                if (NumberUtils.isNumber(imps[i])) {
                                    importantList.add(Integer.valueOf(imps[i]));
                                }
                            }
                            if (importantList.contains(Integer.valueOf(6))) {//包含其他
                                if (importantList.size() == 1) {//只勾选了'其他'就查询出重要程度为空的 OA-21615
                                    newList.add(Integer.valueOf(1));
                                    newList.add(Integer.valueOf(2));
                                    newList.add(Integer.valueOf(3));
                                    newList.add(Integer.valueOf(4));
                                    newList.add(Integer.valueOf(5));
                                    importLevelHql.append(" (affair2.important_level not in(:notImportantLevel) or affair2.important_level is null)");
                                    parameter.put("notImportantLevel", newList);
                                } else if (importantList.size() != 6) {//等于4的时候就是所有的重要程度都包括，就不用加条件了。
                                    newList.add(Integer.valueOf(1));
                                    newList.add(Integer.valueOf(2));
                                    newList.add(Integer.valueOf(3));
                                    newList.add(Integer.valueOf(4));
                                    newList.add(Integer.valueOf(5));
                                    importantList.remove(Integer.valueOf(6));
                                    newList.removeAll(importantList);
                                    importLevelHql.append(" (affair2.important_level not in(:notImportantLevel) or affair2.important_level is null)");
                                    parameter.put("notImportantLevel", newList);
                                }
                            } else { //不包含其他
                                if (importantList.size() > 1) {
                                    importLevelHql.append(" (affair2.important_level in(:importantLevel) )");
                                    parameter.put("importantLevel", importantList);
                                } else {
                                    importLevelHql.append(" (affair2.important_level = :importantLevel1)");
                                    parameter.put("importantLevel1", importantList.get(0));
                                }
                            }
                            //重要紧急程度需屏蔽信息报送
                            if (Strings.isNotBlank(importLevelHql.toString())) {
                                importLevelHql.append(" and ");
                            }
                            importLevelHql.append("  (affair2.app != :notInInfo)");
                            parameter.put("notInInfo", ApplicationCategoryEnum.info.key());
                            if (isPortalMore) {
                                appendHql(portalSearchHql, false, importLevelHql);
                            } else {
                                appendHql(searchHql, isSourcesRelationOr, importLevelHql);
                            }
                        }
                        break;
                    case applicationEnum:
                        if (Strings.isNotBlank(model.getSearchValue1())) {
                            String[] apps = model.getSearchValue1().split(",");
                            List<Integer> appList = new ArrayList<Integer>();
                            Map<Integer, List<Integer>> app2SubApp = new HashMap<Integer, List<Integer>>();
                            for (String app : apps) {
                                if (ApplicationCategoryEnum.exchange.key() == Integer.parseInt(app)) {
                                    appList.add(ApplicationCategoryEnum.exSend.key());
                                    appList.add(ApplicationCategoryEnum.exSign.key());
                                    appList.add(ApplicationCategoryEnum.edocRegister.key());
                                } else if (ApplicationCategoryEnum.inquiry.key() == Integer.parseInt(app)) {
                                    int subState = ApplicationSubCategoryEnum.inquiry_audit.key();
                                    //当按应用类型查询调查的时候只查询带填写的调查
                                    if (apps.length == 1) {
                                        subState = ApplicationSubCategoryEnum.inquiry_write.key();
                                    }
                                    Strings.addToMap(app2SubApp, ApplicationCategoryEnum.inquiry.key(), subState);

                                } else if (ApplicationCategoryEnum.news.key() == Integer.parseInt(app)) {
                                    //为新闻的时候为综合信息审批，包含待审批的调查，不包括待填写的。
                                    appList.add(ApplicationCategoryEnum.bulletin.key());
                                    appList.add(ApplicationCategoryEnum.news.key());

                                    Strings.addToMap(app2SubApp, ApplicationCategoryEnum.inquiry.key(), ApplicationSubCategoryEnum.inquiry_audit.key());

                                } else {
                                    appList.add(Integer.parseInt(app));
                                }
                            }
                            StringBuilder applicationEnumHql = new StringBuilder();
                            if (appList.size() > 1) {
                                applicationEnumHql.append(" affair2.app in(:applicationEnum) ");
                                parameter.put("applicationEnum", appList);
                            } else if (appList.size() == 1) {
                                applicationEnumHql.append(" affair2.app = :applicationEnum ");
                                parameter.put("applicationEnum", appList.get(0));
                            }
                            //更具app2SubApp来判断。
                            if (!app2SubApp.isEmpty()) {
                                int index = 0;
                                for (Iterator<Map.Entry<Integer, List<Integer>>> iterator = app2SubApp.entrySet().iterator(); iterator.hasNext(); ) {
                                    Map.Entry<Integer, List<Integer>> entry = iterator.next();
                                    if (!entry.getValue().isEmpty()) {

                                        applicationEnumHql.append(" and ( affair2.app = :").append("app").append(index).append(" and affair2.subApp in(:").append("subApp").append(index).append("))");
                                        parameter.put("app" + index, entry.getKey());
                                        parameter.put("subApp" + index, entry.getValue());
                                        index++;
                                    }
                                }
                            }
                            if (isPortalMore) {
                                appendHql(portalSearchHql, false, applicationEnumHql);
                            } else {
                                appendHql(searchHql, isSourcesRelationOr, applicationEnumHql);
                            }
                        }
                        break;
                    case nodePerm:
                        if (Strings.isNotBlank(model.getSearchValue1())) {
                            if ("audit".equals(model.getSearchValue1())) {
                                StringBuilder nodePermAuditHql = new StringBuilder();
                                nodePermAuditHql.append(" affair2.node_policy in (:auditPopedom) or affair2.app in(:auditPopedomApp)");
                                List<Integer> listApps = new ArrayList<Integer>();
                                listApps.add(ApplicationCategoryEnum.bulletin.key());
                                listApps.add(ApplicationCategoryEnum.news.key());
                                listApps.add(ApplicationCategoryEnum.inquiry.key());
                                parameter.put("auditPopedom", condition.getAuditPopedom());
                                parameter.put("auditPopedomApp", listApps);
                                if (isPortalMore) {
                                    portalSearchHql.append(" ").append(false).append(" (").append(nodePermAuditHql).append(")");
                                } else {
                                    searchHql.append(" ").append(isSourcesRelationOr.toString()).append(" (").append(nodePermAuditHql).append(")");
                                }
                            } else if ("read".equals(model.getSearchValue1())) {
                                StringBuilder nodePermReadHql = new StringBuilder();
                                nodePermReadHql.append(" affair2.node_policy in (:auditPopedom)");
                                parameter.put("auditPopedom", condition.getAuditPopedom());
                                if (isPortalMore) {
                                    appendHql(portalSearchHql, false, nodePermReadHql);
                                } else {
                                    appendHql(searchHql, isSourcesRelationOr, nodePermReadHql);
                                }
                            }
                        }
                        break;
                    case sender:
                        if (Strings.isNotBlank(model.getSearchValue1())) {

                            StringBuilder senderHql = new StringBuilder(" exists (select id from org_member where id=affair2.sender_id and name like :senderName) ");
                            parameter.put("senderName", "%" + SQLWildcardUtil.escape(model.getSearchValue1()) + "%");
                            if (isPortalMore) {
                                appendHql(portalSearchHql, false, senderHql);
                            } else {
                                appendHql(searchHql, isSourcesRelationOr, senderHql);
                            }
                        }
                        break;
                    case createDate:
                        StringBuilder createDateHql = new StringBuilder();
                        if (Strings.isNotBlank(model.getSearchValue1())) {
                            Date startDate = Datetimes.parseDatetime(model.getSearchValue1());
                            createDateHql.append(" affair2.create_date>:createDate1");
                            parameter.put("createDate1", startDate);
                        }
                        if (Strings.isNotBlank(model.getSearchValue2())) {
                            Date startDate = Datetimes.parseDatetime(model.getSearchValue2());
                            StringBuilder date2Hql = new StringBuilder(" affair2.create_date<:createDate2");
                            appendHql(createDateHql, false, date2Hql);
                            parameter.put("createDate2", startDate);
                        }
                        if (isPortalMore) {
                            appendHql(portalSearchHql, false, createDateHql);
                        } else {
                            appendHql(searchHql, isSourcesRelationOr, createDateHql);
                        }
                        break;
                    case receiveDate:
                        StringBuilder receiveDateHql = new StringBuilder();
                        if (Strings.isNotBlank(model.getSearchValue1())) {
                            Date startDate = Datetimes.parseDatetime(model.getSearchValue1());
                            receiveDateHql.append(" affair2.receive_time>:receiveTime1");
                            parameter.put("receiveTime1", startDate);
                        }
                        if (Strings.isNotBlank(model.getSearchValue2())) {
                            Date endDate = Datetimes.parseDatetime(model.getSearchValue2());
                            StringBuilder date2Hql = new StringBuilder(" affair2.receive_time<:receiveTime2");
                            appendHql(receiveDateHql, false, date2Hql);
                            parameter.put("createDate1", endDate);
                        }
                        if (isPortalMore) {
                            appendHql(portalSearchHql, false, receiveDateHql);
                        } else {
                            appendHql(searchHql, isSourcesRelationOr, receiveDateHql);
                        }
                        break;
                    case dealDate:
                        StringBuilder dealDateHql = new StringBuilder();
                        if (Strings.isNotBlank(model.getSearchValue1())) {
                            Date startDate = Datetimes.parseDatetime(model.getSearchValue1());
                            dealDateHql.append(" affair2.update_date>:updateDate1");
                            parameter.put("updateDate1", startDate);
                        }
                        if (Strings.isNotBlank(model.getSearchValue2())) {
                            Date endDate = Datetimes.parseDatetime(model.getSearchValue2());
                            StringBuilder date2Hql = new StringBuilder(" affair2.update_date<:updateDate2");
                            appendHql(dealDateHql, false, date2Hql);
                            parameter.put("updateDate2", endDate);
                        }
                        if (isPortalMore) {
                            appendHql(portalSearchHql, false, dealDateHql);
                        } else {
                            appendHql(searchHql, isSourcesRelationOr, dealDateHql);
                        }
                        break;
                    case subState:
                        String subState = model.getSearchValue1();
                        if (Strings.isNotBlank(subState)) {
                            if (subState.length() == 1 && NumberUtils.isNumber(subState)) {

                                if (String.valueOf(SubStateEnum.col_waitSend_stepBack.getKey()).equals(subState)
                                        || String.valueOf(SubStateEnum.col_waitSend_sendBack.getKey()).equals(subState)) {
                                    StringBuilder subStateWaitSendHql = new StringBuilder();
                                    subStateWaitSendHql.append("affair2.sub_state = :subState or affair2.back_from_id is not null");
                                    //去掉暂存待办的数据
                                    StringBuilder removeZCDBHql = new StringBuilder();
                                    removeZCDBHql.append("affair2.sub_state != :zcdbSubState or affair2.back_from_id is not null");
                                    parameter.put("subState", subState);
                                    parameter.put("zcdbSubState", SubStateEnum.col_pending_ZCDB.getKey());
                                    if (isPortalMore) {
                                        appendHql(portalSearchHql, false, subStateWaitSendHql);
                                    } else {
                                        appendHql(searchHql, isSourcesRelationOr, subStateWaitSendHql);
                                    }
                                } else {
                                    StringBuilder subStateOtherHql = new StringBuilder();
                                    subStateOtherHql.append("affair2.sub_state = :subState ");
                                    parameter.put("subState", subState);
                                    if (isPortalMore) {
                                        appendHql(portalSearchHql, false, subStateOtherHql);
                                    } else {
                                        appendHql(searchHql, isSourcesRelationOr, subStateOtherHql);
                                    }
                                }
                                //指定回退
                            } else if (String.valueOf(SubStateEnum.col_pending_specialBack.getKey()).equals(subState)) {
                                StringBuilder subStateStepBackHql = new StringBuilder();
                                subStateStepBackHql.append("affair2.sub_state in (:subState) ");
                                List<Integer> subStateList = new ArrayList<Integer>();
                                subStateList.add(SubStateEnum.col_pending_specialBack.getKey());
                                subStateList.add(SubStateEnum.col_pending_specialBackCenter.getKey());
                                subStateList.add(SubStateEnum.col_pending_specialBackToSenderCancel.getKey());
                                subStateList.add(SubStateEnum.col_pending_specialBackToSenderReGo.getKey());
                                parameter.put("subState", subStateList);
                                if (isPortalMore) {
                                    appendHql(portalSearchHql, false, subStateStepBackHql);
                                }
                                //已读
                            } else if (String.valueOf(SubStateEnum.col_pending_read.getKey()).equals(subState)) {
                                List<Integer> subStateList = new ArrayList<Integer>();
                                subStateList.add(SubStateEnum.col_pending_read.getKey());
                                subStateList.add(SubStateEnum.col_pending_specialBack.getKey());
                                subStateList.add(SubStateEnum.col_pending_specialBackToSenderReGo.getKey());
                                StringBuilder subStateReadHql = new StringBuilder();
                                subStateReadHql.append("affair2.sub_state in (:subState) ");
                                parameter.put("subState", subStateList);
                                if (isPortalMore) {
                                    appendHql(portalSearchHql, false, subStateReadHql);
                                }
                            } else {
                                String[] subStates = subState.split("[,]");
                                List<Integer> subStateList = new ArrayList<Integer>();
                                for (String sState : subStates) {
                                    subStateList.add(Integer.parseInt(sState));
                                }
                                if (subStateList.contains(SubStateEnum.col_waitSend_sendBack.getKey())
                                        || subStateList.contains(SubStateEnum.col_waitSend_stepBack.getKey())) {
                                    StringBuilder subStateReadHql = new StringBuilder();
                                    subStateReadHql.append("affair2.sub_state in (:subState) or affair2.back_from_id is not null");
                                    parameter.put("subState", subStateList);
                                    if (isPortalMore) {
                                        appendHql(portalSearchHql, false, subStateReadHql);
                                    } else {
                                        appendHql(searchHql, isSourcesRelationOr, subStateReadHql);
                                    }
                                } else {
                                    StringBuilder subStateReadHql = new StringBuilder();
                                    subStateReadHql.append("affair2.sub_state in (:subState) ");
                                    parameter.put("subState", subStateList);
                                    if (isPortalMore) {
                                        appendHql(portalSearchHql, false, subStateReadHql);
                                    } else {
                                        appendHql(searchHql, isSourcesRelationOr, subStateReadHql);
                                    }
                                }

                            }
                        }
                        break;
                    case templete:
                        if (Strings.isNotBlank(model.getSearchValue1())) {
                            String searchValue1 = model.getSearchValue1();
                            String[] colAndEdocTIds = searchValue1.split(",");
                            List<Long> tempIdList = new ArrayList<Long>();
                            List<Long> tempCategoryIdList = new ArrayList<Long>();
                            for (String tempId : colAndEdocTIds) {
                                if (Strings.isNotBlank(tempId)) {
                                    if (tempId.contains("C_")) {
                                        if (tempCategoryIdList.size() < 1000) {
                                            String categoryIdStr = tempId.substring(2, tempId.length());
                                            Long categoryId = Long.valueOf(categoryIdStr);
                                            tempCategoryIdList.add(categoryId);
                                        }
                                    } else {
                                        if (tempIdList.size() < 1000) {
                                            Long id = Long.valueOf(tempId);
                                            tempIdList.add(id);
                                        }

                                    }
                                }
                            }
                            StringBuilder templateHql = new StringBuilder();
                            templateHql.append(" affair2.templete_id in( select id from ctp_template t where 1=1 ");
                            if (Strings.isNotEmpty(tempIdList)) {
                                if (Strings.isNotEmpty(tempCategoryIdList)) {
                                    templateHql.append(" and t.category_id in (:categoryId) or t.id in(:templateId)");
                                    parameter.put("categoryId", tempCategoryIdList);
                                    parameter.put("templateId", tempIdList);
                                } else {
                                    templateHql.append(" and  t.id in(:templateId)");
                                    parameter.put("templateId", tempIdList);
                                }
                            } else {
                                if (Strings.isNotEmpty(tempCategoryIdList)) {
                                    templateHql.append(" and t.category_id in (:categoryId)");
                                    parameter.put("categoryId", tempCategoryIdList);
                                }
                            }
                            templateHql.append(")");
                            if (isPortalMore) {
                                appendHql(portalSearchHql, false, templateHql);
                            } else {
                                appendHql(searchHql, isSourcesRelationOr, templateHql);
                            }

                        }
                        break;
                    case overTime:
                        if (isPortalMore) {
                            portalSearchHql.append(" ").append(false).append(" (").append("affair2.cover_time = 0").append(")");
                        } else {
                            searchHql.append(" ").append(isSourcesRelationOr.toString()).append(" (").append("affair2.cover_time = 0").append(")");
                        }
                        break;
                    case catagory:
                        if (Strings.isNotBlank(model.getSearchValue1())) {
                            String value = model.getSearchValue1();
                            String[] sv = value.split(",");
//						Junction junction = Restrictions.disjunction();
                            StringBuilder catagoryHql = new StringBuilder();
                            for (String s : sv) {
                                if (Strings.isNotBlank(catagoryHql.toString())) {
                                    catagoryHql.append(" or ");
                                }
                                if ("done_catagory_all".equals(s)) {//已办栏目全部，需要排除会议相关
                                    catagoryHql.append("(");
                                    catagoryHql.append(" affair2.app !=").append(ApplicationCategoryEnum.info.key());
                                    catagoryHql.append(" and affair2.app != ").append(ApplicationCategoryEnum.office.key());
                                    catagoryHql.append(" and affair2.app != ").append(ApplicationCategoryEnum.inquiry.key());
                                    catagoryHql.append(")");
                                } else if ("sent_catagory_all".equals(s) || "waitsend_catagory_all".equals(s)) {//已办栏目全部，需要排除会议相关
                                    catagoryHql.append("(");
                                    catagoryHql.append(" affair2.app !=").append(ApplicationCategoryEnum.info.key());
                                    catagoryHql.append(" and affair2.app != ").append(ApplicationCategoryEnum.office.key());
                                    catagoryHql.append(")");
                                } else if ("catagory_collOrFormTemplete".equals(s)) {
                                    //模板协同
                                    catagoryHql.append("(");
                                    catagoryHql.append(" affair2.templete_id is not null ");
                                    catagoryHql.append(" and affair2.app = ").append(ApplicationCategoryEnum.collaboration.key());
                                    catagoryHql.append(")");
                                } else if ("catagory_coll".equals(s)) {
                                    //自由协同
                                    catagoryHql.append("(");
                                    catagoryHql.append(" affair2.templete_id is null ");
                                    catagoryHql.append(" and affair2.app = ").append(ApplicationCategoryEnum.collaboration.key());
                                    catagoryHql.append(")");
                                } else if ("catagory_edoc".equals(s)) {
                                    //公文
                                    List<Integer> keys = new ArrayList<Integer>();
                                    keys.add(ApplicationCategoryEnum.edoc.key());
                                    keys.add(ApplicationCategoryEnum.edocRec.key());
                                    keys.add(ApplicationCategoryEnum.edocRegister.key());
                                    keys.add(ApplicationCategoryEnum.edocSend.key());
                                    keys.add(ApplicationCategoryEnum.edocSign.key());
                                    keys.add(ApplicationCategoryEnum.exSend.key());
                                    keys.add(ApplicationCategoryEnum.exSign.key());
                                    keys.add(ApplicationCategoryEnum.exchange.key());
                                    keys.add(ApplicationCategoryEnum.edocRecDistribute.key());
                                    catagoryHql.append(" affair2.app in(:catagoryEdocApp)");
                                    parameter.put("catagoryEdocApp", keys);
                                } else if ("catagory_meet".equals(s)) {
                                    //已召开会议
                                    //TODO
                                    List<Integer> keys = new ArrayList<Integer>();
                                    keys.add(ApplicationCategoryEnum.meeting.key());
                                    keys.add(ApplicationCategoryEnum.meetingroom.key());
                                    catagoryHql.append(" affair2.app in(:catagoryMeetApp)");
                                    parameter.put("catagoryMeetApp", keys);
                                } else if ("catagory_meetRoom".equals(s)) {
                                    catagoryHql.append(" affair2.app =").append(ApplicationCategoryEnum.meetingroom.key());
                                } else if ("catagory_inquiry".equals(s)) {//调查
                                    catagoryHql.append(" affair2.app =").append(ApplicationCategoryEnum.inquiry.key());
                                } else if ("catagory_publicInfo".equals(s)) {//公共信息
                                    List<Integer> publicInformatEnums = new ArrayList<Integer>();
                                    publicInformatEnums.add(ApplicationCategoryEnum.bulletin.getKey());// 公告
                                    publicInformatEnums.add(ApplicationCategoryEnum.news.getKey());// 新闻
                                    publicInformatEnums.add(ApplicationCategoryEnum.bbs.getKey());// 讨论
                                    publicInformatEnums.add(ApplicationCategoryEnum.inquiry.getKey());// 调查
                                    catagoryHql.append(" affair2.app in(:catagoryPublicInfoApp)");
                                    parameter.put("catagoryPublicInfoApp", publicInformatEnums);
                                } else if ("catagory_comprehensiveOffice".equals(s)) {//综合办公
                                    catagoryHql.append(" affair2.app =").append(ApplicationCategoryEnum.office.key());
                                }
                            }
                            if (isPortalMore) {
                                appendHql(portalSearchHql, false, catagoryHql);
                            } else {
                                appendHql(searchHql, isSourcesRelationOr, catagoryHql);
                            }
                        }
                        break;
                    case policy4Portal:
                        if (Strings.isNotBlank(model.getSearchValue1())) {
                            //wangjingjing 子应用subapp begin
                            //key: subapp, Value:节点权限
                            Map<Integer, List<String>> subAppPolicy = new HashMap<Integer, List<String>>();
                            //wangjingjing end
                            List<Integer> category = new ArrayList<Integer>();
                            Map<Integer, List<Integer>> app2SubApp = new HashMap<Integer, List<Integer>>();
                            Map<Integer, List<String>> policy = new HashMap<Integer, List<String>>();
                            StringBuilder policy4PortalHql = new StringBuilder();
                            String[] all = model.getSearchValue1().split(",");
                            for (String str : all) {
                                if (Strings.isBlank(str)) {
                                    continue;
                                }

                                String[] poli = str.split("___");
                                if (poli[0] != null) {
                                    if (poli.length == 2) {
                                        if (poli[0].startsWith("P")) {
                                            //3.5格式的数据：P1___shenpi,P19___shenpi,P20___shenpi ,P21___shenpi
                                            //3.5之前的数据：P_shenpi   (包括协同，公文)
                                            String apps = poli[0].substring(1, poli[0].length());
                                            if (Strings.isNotBlank(apps)) {
                                                int app = Integer.valueOf(apps);
                                                List<String> l = null;
                                                if (policy.get(app) == null) {
                                                    l = new ArrayList<String>();
                                                } else {
                                                    l = policy.get(app);
                                                }
                                                l.add(poli[1]);
                                                policy.put(app, l);
                                            } else {
                                                int type[] = {ApplicationCategoryEnum.collaboration.key(), ApplicationCategoryEnum.edocSend.key(),
                                                        ApplicationCategoryEnum.edocRec.key(), ApplicationCategoryEnum.edocSign.key()};
                                                List<String> l = null;
                                                for (int i = 0; i < type.length; i++) {
                                                    if (policy.get(type[i]) == null) {
                                                        l = new UniqueList<String>();
                                                    } else {
                                                        l = policy.get(type[i]);
                                                    }
                                                    l.add(poli[1]);
                                                    policy.put(type[i], l);
                                                }
                                            }
                                        } else if (poli[0].startsWith("A")) {
                                            //公文发文
                                            if (Integer.parseInt(poli[1]) == 19) {
                                                category.add(ApplicationCategoryEnum.edocSend.getKey());
                                            } else if (Integer.parseInt(poli[1]) == 20) { //公文收文
                                                category.add(ApplicationCategoryEnum.edocRec.getKey());
                                                if (AffairCondition.isG6Version()) {//G6版本包含登记、分发
                                                    if (AffairCondition.isOpenRegister()) {
                                                        //G6版本,在开启了收文登记的情况下，查询收文待登记的数据
                                                        category.add(ApplicationCategoryEnum.edocRegister.getKey());
                                                    }

                                                    //是V5-G6版本,则登记查询待分发的数据
                                                    category.add(ApplicationCategoryEnum.edocRecDistribute.getKey());

                                                } else {
                                                    category.add(ApplicationCategoryEnum.edocRegister.getKey());
                                                }
                                            } else if (Integer.parseInt(poli[1]) == 21) { //公文签报
                                                category.add(ApplicationCategoryEnum.edocSign.getKey());
                                            } else if (Integer.parseInt(poli[1]) == 16) { //公文交换
                                                category.add(ApplicationCategoryEnum.exSend.getKey());
                                                category.add(ApplicationCategoryEnum.exSign.getKey());
                                            }
                                            //公共信息发布待审
                                            else if (Integer.parseInt(poli[1]) == 7 || Integer.parseInt(poli[1]) == 8 || Integer.parseInt(poli[1]) == 10) {
                                                category.add(7);
                                                category.add(8);

                                                Strings.addToMap(app2SubApp, ApplicationCategoryEnum.inquiry.key(), ApplicationSubCategoryEnum.inquiry_audit.key());
//											}
                                            }
                                            //公文
                                            else if (Integer.parseInt(poli[1]) == 4) {
                                                category.add(ApplicationCategoryEnum.edoc.key());
                                                category.add(ApplicationCategoryEnum.edocRec.key());
                                                category.add(ApplicationCategoryEnum.edocRegister.key());
                                                category.add(ApplicationCategoryEnum.edocSend.key());
                                                category.add(ApplicationCategoryEnum.edocSign.key());
                                                category.add(ApplicationCategoryEnum.exSend.key());
                                                category.add(ApplicationCategoryEnum.exSign.key());
                                                category.add(ApplicationCategoryEnum.exchange.key());
                                                category.add(ApplicationCategoryEnum.edocRecDistribute.key());
                                            }
                                            // 会议
                                            else if (Integer.parseInt(poli[1]) == 6) {
                                                category.add(ApplicationCategoryEnum.meeting.key());
                                            } else if (Integer.parseInt(poli[1]) == 30) {
                                                category.add(ApplicationCategoryEnum.meeting.key());
                                                category.add(ApplicationCategoryEnum.meetingroom.key());
                                            } else if (Integer.parseInt(poli[1]) == 29) {
                                                category.add(ApplicationCategoryEnum.meetingroom.key());
                                            } else {
                                                category.add(Integer.parseInt(poli[1]));
                                            }
                                        }
                                        //wangjingjing 子应用subapp begin
                                        else if (poli[0].startsWith("S")) { //S6___shenpi, S6___all
                                            String apps = poli[0].substring(1, poli[0].length());
                                            if (Strings.isNotBlank(apps)) {
                                                int app = Integer.valueOf(apps);
                                                List<String> l = subAppPolicy.get(app);
                                                if (null == l) {
                                                    l = new UniqueList<String>();
                                                    subAppPolicy.put(app, l);
                                                }
                                                l.add(poli[1]);
                                            }
                                        }
                                        //wangjingjing end
                                    } else if (poli.length == 3) {
                                        if (poli[0].startsWith("A")) {
                                            if (Integer.parseInt(poli[1]) == 10) {
                                                //调查的格式为A___10___0 (待审核)  A___10___1(待填写)
                                                Strings.addToMap(app2SubApp, Integer.parseInt(poli[1]), Integer.parseInt(poli[2]));
                                            } else if (Integer.parseInt(poli[1]) == 32) {
                                                //信息报送
                                                if (Integer.parseInt(poli[1]) == 32) {
                                                    int subApp = Integer.parseInt(poli[2]);
                                                    if (subApp == 0) {
                                                        Strings.addToMap(app2SubApp, ApplicationCategoryEnum.info.key(), ApplicationSubCategoryEnum.info_self.key());
                                                        Strings.addToMap(app2SubApp, ApplicationCategoryEnum.info.key(), ApplicationSubCategoryEnum.info_tempate.key());
                                                    } else {
                                                        Strings.addToMap(app2SubApp, ApplicationCategoryEnum.info.key(), subApp);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            StringBuilder poliHql = new StringBuilder();
                            if (!category.isEmpty()) {
                                poliHql.append(" affair2.app in(:policy4PortalApp)");
                                parameter.put("policy4PortalApp", category);
                            }

                            if (!policy.isEmpty()) {

                                /**
                                 * 1.Restrictions.and(Restrictions.eq("app", key)
                                 * 2.nodepolicy :
                                 * 	 a.阅文，办文替换前缀
                                 *   b.只会的时候需要兼容inform,zhihui
                                 *   c.原生的，直接取传递过来的
                                 *   d.登记的时候，节点权限为空，IsNull
                                 * 3.subApp: 如果开启了越文和办文，区分越文和办文
                                 */
                                int parmIdex = 0;
                                for (Iterator<Integer> iterator = policy.keySet().iterator(); iterator.hasNext(); ) {
                                    Integer key = iterator.next();
                                    if (!policy.get(key).isEmpty()) {
                                        List<String> policys = policy.get(key);
                                        for (String pol : policys) {

                                            StringBuilder policyHql = new StringBuilder();
                                            //登记节点需要单独处理
                                            if ("regist".equals(pol) || "dengji".equals(pol)) {
                                                if (AffairCondition.isG6Version()) {//G6版本包含登记、分发
                                                    if ("regist".equals(pol) && AffairCondition.isOpenRegister()) {
                                                        //G6版本,在开启了收文登记的情况下，查询收文待登记的数据
                                                        String paramName = "app_policy4Portal_" + parmIdex++;
                                                        policyHql.append(" affair2.app = :").append(paramName);
                                                        parameter.put(paramName, ApplicationCategoryEnum.edocRegister.getKey());
                                                    } else {
                                                        //是V5-G6版本,则登记查询待分发的数据
                                                        String paramName = "app_policy4Portal_" + parmIdex++;
                                                        policyHql.append(" affair2.app = :").append(paramName);
                                                        parameter.put(paramName, ApplicationCategoryEnum.edocRecDistribute.getKey());
                                                    }
                                                } else {
                                                    if (("dengji").equals(pol)) {//A8只有登记
                                                        String paramName = "app_policy4Portal_" + parmIdex++;
                                                        policyHql.append(" affair2.app = :").append(paramName);
                                                        parameter.put(paramName, ApplicationCategoryEnum.edocRegister.getKey());
                                                    }
                                                }
                                            } else {
                                                policyHql.append(" affair2.app=").append(key);
                                                if (pol.endsWith("_ban") || pol.endsWith("_yue")) {
                                                    pol = pol.replace("_ban", "");
                                                    int subapp = ApplicationSubCategoryEnum.edocRecHandle.getKey();
                                                    if (pol.endsWith("_yue")) {
                                                        pol = pol.replace("_yue", "");
                                                        subapp = ApplicationSubCategoryEnum.edocRecRead.getKey();
                                                    }
                                                    String paramName = "app_policy4Portal_" + parmIdex++;
                                                    policyHql.append(" and  affair2.app = :").append(paramName);
                                                    parameter.put(paramName, subapp);
                                                }

                                                if ("zhihui".equals(pol)) {
                                                    String paramName = "nodepolicy_policy4Portal_" + parmIdex++;
                                                    String paramName1 = "nodepolicy_policy4Portal_" + parmIdex++;
                                                    policyHql.append(" and (affair2.node_policy = :").append(paramName).append(" or affair2.node_policy = :").append(paramName1).append(")");
                                                    parameter.put(paramName, "zhihui");
                                                    parameter.put(paramName1, "inform");
                                                } else {

                                                    String paramName = "nodepolicy_policy4Portal_" + parmIdex++;
                                                    policyHql.append(" and affair2.node_policy = :").append(paramName);
                                                    parameter.put(paramName, pol);
                                                }
                                            }
                                            if (Strings.isNotBlank(poliHql.toString())) {
                                                appendHql(poliHql, true, policyHql);
                                            } else {
                                                poliHql.append(policyHql);
                                            }
                                        }
                                    }
                                }
                            }
                            //wangjingjing 子应用subapp begin
                            List<String> list;
                            Integer key;
                            int index = 0;
                            if (!subAppPolicy.isEmpty()) {
                                for (Iterator<Integer> iterator = subAppPolicy.keySet().iterator(); iterator.hasNext(); ) {
                                    key = iterator.next();

                                    list = subAppPolicy.get(key);
                                    if (!list.isEmpty()) {
                                        if (list.contains("all")) {
                                            StringBuilder subAppPolicyHql = new StringBuilder("affair2.sub_app=").append(key);
                                            appendHql(poliHql, true, subAppPolicyHql);
                                        } else {
                                            StringBuilder subAppPolicyHql = new StringBuilder();
                                            subAppPolicyHql.append(" affair2.sub_app=").append(key);
                                            subAppPolicyHql.append(" and affair2.node_policy in (:").append("nodePolicy").append(index).append(")");
                                            parameter.put("nodePolicy" + index, list);
                                            appendHql(poliHql, true, subAppPolicyHql);
                                        }
                                    }
                                }
                            }
                            //wangjingjing end

                            if (!app2SubApp.isEmpty()) {
                                for (Iterator<Map.Entry<Integer, List<Integer>>> iterator = app2SubApp.entrySet().iterator(); iterator.hasNext(); ) {
                                    Map.Entry<Integer, List<Integer>> entry = iterator.next();
                                    if (!entry.getValue().isEmpty()) {
                                        StringBuilder app2SubAppHql = new StringBuilder();
                                        app2SubAppHql.append(" affair2.sub_app=").append(entry.getKey());
                                        app2SubAppHql.append(" and affair2.node_policy in (:").append("nodePolicy").append(index).append(")");
                                        parameter.put("nodePolicy" + index, entry.getValue());
                                        appendHql(poliHql, true, app2SubAppHql);
                                    }
                                }
                            }
                            if (isPortalMore) {
                                appendHql(portalSearchHql, false, poliHql);
                            } else {
                                appendHql(searchHql, isSourcesRelationOr, poliHql);
                            }
                        }
                        break;
                    case expectedProcessTime:
                        StringBuilder expectedProcessTimeHql = new StringBuilder();
                        if (Strings.isNotBlank(model.getSearchValue1())) {
                            Date startDate = Datetimes.parseDatetime(model.getSearchValue1());
                            expectedProcessTimeHql.append(" affair2.expected_process_time>:expectedProcessTime1");
                            parameter.put("expectedProcessTime1", startDate);
                        }
                        if (Strings.isNotBlank(model.getSearchValue2())) {
                            Date startDate = Datetimes.parseDatetime(model.getSearchValue2());
                            StringBuilder date2Hql = new StringBuilder(" affair2.expected_process_time<:expectedProcessTime2");
                            appendHql(expectedProcessTimeHql, false, date2Hql);
                            parameter.put("expectedProcessTime2", startDate);
                        }
                        if (isPortalMore) {
                            appendHql(portalSearchHql, false, expectedProcessTimeHql);
                        } else {
                            appendHql(searchHql, isSourcesRelationOr, expectedProcessTimeHql);
                        }
                        break;
                    //case 自由协同:
                    //	break;
                    //case 其他:
                    //	break;
                }
            }
        }
        appendHql(hql, false, searchHql);
        appendHql(hql, false, portalSearchHql);
        hql.append("  GROUP BY affair2.object_id ) taffair where taffair.id = affair.id");
        /**徐矿集团【已办公文首页栏目只显示已处理但流程未结束的公文】 wxt.dulong 2019-5-28 start*/
        hql.append("  and summary_state = 0");
        hql.append("  and node_policy <> 'fengfa'");
        /**徐矿集团【已办公文首页栏目只显示已处理但流程未结束的公文】 wxt.dulong 2019-5-28 end*/
        String orderBySql = "  order by affair.complete_time desc";

        return affairDao.getDeduplicationAffairList(hql.toString(), parameter, onlyCount, fi, orderBySql);

    }

    private void appendHql(StringBuilder hql, Boolean isSourcesRelationOr, StringBuilder addHql) {
        if (Strings.isNotBlank(hql.toString()) && Strings.isNotBlank(addHql.toString())) {
            hql.append(" ").append(isSourcesRelationOr ? "or" : "and ").append(" (").append(addHql).append(")");
        } else {
            if (Strings.isNotBlank(addHql.toString())) {
                hql.append(" ").append(" (").append(addHql).append(")");
            }
        }

    }

    @Override
    public void updateFormCollSubject(Long summaryId, String newSubject) throws BusinessException {
        affairDao.updateFormCollSubject(summaryId, newSubject);
    }

    @Override
    public List<Long> getAllAffairIdByAppAndObjectId(ApplicationCategoryEnum appEnum, Long objectId)
            throws BusinessException {
        return affairDao.getAllAffairIdByAppAndObjectId(appEnum, objectId);
    }

    @Override
    public List getAffairDetailsBygorup(Map<String, Object> params)
            throws BusinessException {
        if (null != params.get("isHistoryFlag") && "true".equals((String) params.get("isHistoryFlag"))) {
            AffairDao affairDaoFK = (AffairDao) AppContext.getBean("affairDaoFK");
            if (affairDaoFK != null) {
                return affairDaoFK.getAffairDetailsBygorup(params);
            } else {
                return new ArrayList<CtpAffair>();
            }
        } else {
            return affairDao.getAffairDetailsBygorup(params);
        }
    }

    public Integer getStartAffairStateByObjectId(Long objectId) throws BusinessException {
        return affairDao.getStartAffairStateByObjectId(objectId);
    }

    public void updateAffairSummaryState(Long objectId, Integer summaryState) throws BusinessException {
        affairDao.updateAffairSummaryState(objectId, summaryState);
    }

    @Override
    public List<CtpAffair> getAffairsForCurrentUsers(FlipInfo flipInfo,
                                                     Map<String, Object> map) throws BusinessException {

        return affairDao.getAffairsForCurrentUsers(flipInfo, map);
    }

    @Override
    public Map<String, Integer> countPendingAffairs(long memberId, String[] appKeys) throws BusinessException {
        Map<String, Integer> map = new HashMap<String, Integer>();

        for (int i = 0; i < appKeys.length; i++) {
            if ("999".equals(appKeys[i])) {
                // best 如果是需要显示所有则需要显示公文和协同 start
                Integer count = 0;
                Map<String, Object> param = new HashMap<String, Object>();
                // 先查询公文的
                param.put("app", "4");
                param.put("memberId", memberId);
                param.put("flag", true); // 用作过滤签报
                count = affairDao.countPendingAffairs(param);
                // 再查询协同的
                param.clear();
                param.put("app", "1");
                param.put("memberId", memberId);
                count += affairDao.countPendingAffairs(param);
                map.put("3003", count);
                // best 如果是需要显示所有则需要显示公文和协同 end

            } else {
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("app", appKeys[i]);
                param.put("memberId", memberId);

                Integer count = 0;
                if (!Integer.valueOf(appKeys[i]).equals(ApplicationCategoryEnum.meeting.getKey())) {
                    count = affairDao.countPendingAffairs(param);
                } else {//会议待开列表数据查询比较复杂,直接调用会议模块查询
                    if (meetingApi != null) {
                        count = meetingApi.getPortleMeetingDataCount();
                    }
                }
                map.put(appKeys[i], count);
            }
        }

        return map;
    }

    @Override
    public StringBuilder getCondition4Agent(AffairCondition condition, Map<String, Object> parameter, boolean isHQL) {
        StringBuilder hql = new StringBuilder();

        int len = condition.getApps().size();
        if (len > 0) {
            hql.append("(");
        }

        if (!condition.getShowAgentAffair()) {
            int count = 0;
            for (ApplicationCategoryEnum app : condition.getApps()) {
                switch (app) {
                    case collaboration:
                        count++;
                        if (count > 1)
                            hql.append(" or ");
                        hql.append(condition.getSql4ColAgent("affair", parameter, isHQL));
                        break;
                    case info:
                        count++;
                        if (count > 1)
                            hql.append(" or ");
                        hql.append("(");
                        hql.append("affair.app = :getHql32ColAgentapp ");
                        parameter.put("getHql32ColAgentapp", ApplicationCategoryEnum.info.key());
                        hql.append(")");
                        break;
                    case edoc:
                    case meeting:
                    case meetingroom:
                    case bulletin:
                    case news:
                    case inquiry:
                    case office:
                        count++;
                        if (count > 1)
                            hql.append(" or ");
                        hql.append(condition.getSql4AppAgent(app, "affair", parameter, isHQL));
                        break;
                }
            }
        } else {
            hql.append(" 1=1 ");
        }
        if (len > 0) {
            hql.append(")");
        }

        return hql;
    }


    @Override
    public List getAIProcessingCountByMemberId(Date beginTime, Date endTime) {
        return affairDao.getAIProcessingCountByMemberId(beginTime, endTime);
    }

    @Override
    public List<CtpAffair> getAffairListByMemberIdBodyTypeAndState(Long memberId, List<String> bodyTypeList, StateEnum state)
            throws BusinessException {
        return affairDao.getAffairListByMemberIdBodyTypeAndState(memberId, bodyTypeList, state);
    }

    @Override
    public List<CtpAffair> getProcessOverdueAffairs(Date beginTime, Date endTime) throws BusinessException {
        return affairDao.getProcessOverdueAffairs(beginTime, endTime);
    }

    @Override
    public List<CtpAffair> getNodeOverdueAffairs(Date beginTime, Date endTime) throws BusinessException {
        return affairDao.getNodeOverdueAffairs(beginTime, endTime);
    }

    @Override
    public List<CtpAffair> getAffairsByAppAndReceivetimeAndState(ApplicationCategoryEnum appEnum, Date beginTime,
                                                                 Date endTime, StateEnum stateEnum) throws BusinessException {
        return affairDao.getAffairsByAppAndReceivetimeAndState(appEnum, beginTime, endTime, stateEnum);
    }

    @Override
    public void updateSortWeight(int sortWeight, List<Long> affairIdList) throws BusinessException {
        affairDao.updateSortWeight(sortWeight, affairIdList);
    }

    public boolean isAddNodeAffair(CtpAffair affair) {

        boolean add = false;
        if (affair.getFromType() != null) {
            boolean addNode = Integer.valueOf(ChangeType.AddNode.getKey()).equals(affair.getFromType());

            boolean addInform = Integer.valueOf(ChangeType.AddInform.getKey()).equals(affair.getFromType());

            boolean assign = Integer.valueOf(ChangeType.Assign.getKey()).equals(affair.getFromType());

            boolean multistageAsign = Integer.valueOf(ChangeType.MultistageAsign.getKey()).equals(affair.getFromType());

            boolean passRead = Integer.valueOf(ChangeType.PassRead.getKey()).equals(affair.getFromType());

            if (addNode || addInform || assign || multistageAsign || passRead) {
                add = true;
            }
        }
        return add;
    }

    @Override
    public boolean isOpen(Long nodeId, Long affairId) throws BusinessException {

        User user = AppContext.getCurrentUser();
        List<MemberRole> roles = orgManager.getMemberRoles(user.getId(), user.getAccountId());
        CtpAffair affair = this.get(affairId);
        if (affair.isFinish() || affair.getState() == 3) {
            return true;
        }
        List<XkjtOpenMode> openModes = xkjtDao.findOpenModeByNodeId(nodeId);
        if (openModes.size() == 0) {
            return true;
        } else {
            XkjtOpenMode openMode = openModes.get(0);
            for (MemberRole memberRole : roles) {
                V3xOrgRole role = memberRole.getRole();
                if (openMode.getRoleIds().indexOf(String.valueOf(role.getId())) != -1) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 根据节点权限获取affairs
     * add by shenwei
     * 20200724
     * @param a
     * @return
     */
    @Override
    public List<CtpAffair> getAffairsByNodePolicy(String pquanxian, Long objectId) throws BusinessException {
        return affairDao.getAffairsByNodePolicy(pquanxian, objectId);
    }

    @Override
    public List<CtpAffair> getAffairsByNodePolicyAndState(String hql, Map<String, Object> map) throws BusinessException {
        return affairDao.getAffairsByNodePolicyAndState(hql, map);
    }
}
