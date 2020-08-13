package com.seeyon.apps.meetingroom.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.meeting.constants.MeetingConstant.RoomAppStateEnum;
import com.seeyon.apps.meeting.manager.MeetingManager;
import com.seeyon.apps.meeting.manager.MeetingMessageManager;
import com.seeyon.apps.meeting.util.MeetingHelper;
import com.seeyon.apps.meeting.util.MeetingUtil;
import com.seeyon.apps.meetingroom.dao.MeetingRoomPermDao;
import com.seeyon.apps.meetingroom.po.MeetingRoom;
import com.seeyon.apps.meetingroom.po.MeetingRoomApp;
import com.seeyon.apps.meetingroom.po.MeetingRoomPerm;
import com.seeyon.apps.meetingroom.util.MeetingRoomAdminUtil;
import com.seeyon.apps.meetingroom.util.MeetingRoomRoleUtil;
import com.seeyon.apps.meetingroom.vo.MeetingRoomAppVO;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.Strings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author 唐桂林
 *
 */
class MeetingRoomPermManagerImpl implements MeetingRoomPermManager {

    private static final Log LOGGER = LogFactory.getLog(MeetingRoomPermManagerImpl.class);

	private MeetingRoomPermDao meetingRoomPermDao;
	private MeetingRoomAppManager meetingRoomAppManager;
	private MeetingRoomRecordManager meetingRoomRecordManager;
	private MeetingRoomManager meetingRoomManager;
	private MeetingManager meetingManager;
	private MeetingMessageManager meetingMessageManager;
	private AffairManager affairManager;
	private AppLogManager appLogManager;
	
	/**
	 * 审核会议室
	 * @param appVo
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public boolean transPerm(MeetingRoomAppVO appVo) throws BusinessException {	
		
		MeetingRoomApp roomApp = this.meetingRoomAppManager.getRoomAppById(appVo.getRoomAppId());
		MeetingRoom room = this.meetingRoomManager.getRoomById(roomApp.getRoomId());
		
		boolean isAdmin = MeetingRoomRoleUtil.isMeetingRoomAdminRole();
		//若当前登录者不是会议室的有效会议室管理员或代理人，则不能审核
		List<Long> roleAdminList = MeetingRoomAdminUtil.getRoomAdminIdList(room);
		if (Strings.isNotEmpty(roleAdminList) && !roleAdminList.contains(appVo.getCurrentUser().getId())) {
			isAdmin = false;
		}
		//当前用户的被代理人
		if (!isAdmin) {
			List<AgentModel> agentList = MemberAgentBean.getInstance().getAgentModelList(appVo.getCurrentUser().getId());
			if(Strings.isNotEmpty(agentList)){//增加能查看被代理人会议
				for(int i = 0 ; i < agentList.size() ; i++){
					AgentModel model = agentList.get(i);
					if(model.isHasMeeting()){
						if (roleAdminList.contains(model.getAgentToId())) {
							isAdmin = true;
							break;
						}
					}
				}
			}
		}
		
		if (!isAdmin) {
			throw new BusinessException(ResourceUtil.getString("mr.alert.notAdmin"));
		}

		/**
		 * 多个管理员同时审批的情况
		 */
		if(roomApp.getAuditingId() != null){
			throw new BusinessException(ResourceUtil.getString("本条会议室申请已被其他管理员审核过了"));
		}

		roomApp.setStatus(appVo.getStatus());
		roomApp.setAuditingId(appVo.getCurrentUser().getId());
		appVo.setMeetingRoomApp(roomApp);
		//代理逻辑需要查询会议室管理员
		appVo.setMeetingRoom(room);
		
		this.setProxyInfo(appVo);

		MeetingRoomPerm roomPerm = this.meetingRoomPermDao.getRoomPermByAppId(appVo.getRoomAppId());
		roomPerm.setIsAllowed(appVo.getStatus());
		roomPerm.setProDatetime(appVo.getSystemNowDatetime());
		roomPerm.setAuditingId(appVo.getCurrentUser().getId());
		roomPerm.setDescription(appVo.getDescription());
		roomPerm.setProxyId(appVo.getProxyId());
		appVo.setMeetingRoomPerm(roomPerm);
		
		appVo.setMeetingRoom(appVo.getMeetingRoom());

		Set<Long> meetingIds = new HashSet<Long>();
		/** 4、校验会议室审批状态 */
		Long meetingId = appVo.getMeetingRoomApp().getMeetingId();
		if(!MeetingUtil.isIdNull(meetingId)){
			meetingIds.add(meetingId);
		}

		/** 5、单次会议室审核 */
		if(Strings.isEmpty(appVo.getPeriodicityRoomAppIdList())) {
			this.meetingRoomAppManager.saveOrUpdate(appVo.getMeetingRoomApp());
			this.saveOrUpdate(appVo.getMeetingRoomPerm());
			
			this.meetingRoomRecordManager.saveOrUpdate(appVo.getMeetingRoomApp());
		}
		
		/** 6、周期会议室审核 */
		else {
			List<MeetingRoomApp> roomAppList = meetingRoomAppManager.getRoomAppListById(appVo.getPeriodicityRoomAppIdList());

			for(MeetingRoomApp bean : roomAppList) {
				/**
				 * 多个管理员同时审批的情况
				 */
				if(!bean.equals(roomApp) && bean.getAuditingId() != null){
					throw new BusinessException(ResourceUtil.getString("周期会议中部分会议室申请已被其他管理员审批了"));
				}
				bean.setStatus(roomApp.getStatus());
				bean.setAuditingId(appVo.getCurrentUser().getId());

				if(!MeetingUtil.isIdNull(bean.getMeetingId())) {
					meetingIds.add(bean.getMeetingId());
				}
			}
			this.meetingRoomAppManager.updateRoomApp(roomAppList);
			
			this.meetingRoomRecordManager.saveRoomRecord(roomAppList);
			
			List<MeetingRoomPerm> roomPermList = this.getRoomPermListByAppId(appVo.getPeriodicityRoomAppIdList());
			for(MeetingRoomPerm bean : roomPermList) {
				bean.setIsAllowed(roomPerm.getIsAllowed());
				bean.setProDatetime(roomPerm.getProDatetime());
				bean.setAuditingId(appVo.getCurrentUser().getId());
			}
			this.meetingRoomPermDao.updateRoomPerm(roomPermList);
			appVo.setRoomPermList(roomPermList);
		}

		//发起会议通知(调用会议方法)
		if(!meetingIds.isEmpty()) {
			if(MeetingHelper.isRoomPass(roomPerm.getIsAllowed())) {
				for(Long id : meetingIds){
					Map<String, Object> parameterMap = new HashMap<String, Object>();
					parameterMap.put("meetingId", id);
					parameterMap.put("status", roomApp.getStatus());
					parameterMap.put("systemNowDatetime", appVo.getSystemNowDatetime());
					meetingManager.transPublishMeeting(parameterMap);
				}
			}
			if(MeetingHelper.isRoomNotPass(roomPerm.getIsAllowed())){
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				if(!MeetingUtil.isIdNull(roomApp.getPeriodicityId())) {
					parameterMap.put("meetingId", meetingId);
					parameterMap.put("periodicityId", roomApp.getPeriodicityId());
					parameterMap.put("currentUser", appVo.getCurrentUser());
					parameterMap.put("systemNowDatetime", appVo.getSystemNowDatetime());
				}
				this.meetingManager.transRefuseRoom(meetingId, parameterMap);
			}
		}
		//记录下跟踪日志
        LOGGER.info(appVo.getCurrentUser().getName() + "审核了会议室(" + room.getName() + ")申请。 appId:" + roomApp.getId() + " auditId:" + roomApp.getAuditingId());
		//更新待办状态
		this.updatedRoomPermAffair(appVo);
		//给发起人发消息
		meetingMessageManager.sendRoomPermMessage(appVo);
		return true;
	}
	
	/**
	 * 审核会议室-不通过
	 * @param room
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public boolean transRefuseRoomPerm(MeetingRoom room) throws BusinessException {
		Long roomId = room.getId();
		List<Long> appIdList = new ArrayList<Long>();
		List<Long> meetingIdList = new ArrayList<Long>();
		List<Long> perIdList = new ArrayList<Long>();
		
		List<MeetingRoomApp> appWaitList = this.meetingRoomAppManager.getRoomAppWaitList(roomId);
		if(Strings.isNotEmpty(appWaitList)) {
			for(MeetingRoomApp bean : appWaitList) {
				bean.setStatus(RoomAppStateEnum.notpass.key());
				
				appIdList.add(bean.getId());
				if (!perIdList.contains(bean.getPerId())) {
					perIdList.add(bean.getPerId());
				}
				if(bean.getMeetingId() != null) {
					meetingIdList.add(bean.getMeetingId());
				}
			}
		}
		
		List<MeetingRoomPerm> permWaitList = this.getRoomPermWaitList(roomId);
		if(Strings.isNotEmpty(permWaitList)) {
			for(MeetingRoomPerm bean : permWaitList) {
				bean.setIsAllowed(RoomAppStateEnum.notpass.key());
			}
			this.meetingRoomPermDao.updateRoomPerm(permWaitList);
			
			if(Strings.isNotEmpty(meetingIdList)) {
				this.meetingManager.transRefuseRoom(meetingIdList);
			}
		}
		
		if(Strings.isNotEmpty(appIdList)) {
			Map<String,Object> parameterMap = new HashMap<String,Object>();
			parameterMap.put("objectId", appIdList);
			parameterMap.put("state", Integer.valueOf(StateEnum.col_pending.getKey()));
			List<CtpAffair> affairList = affairManager.getByConditions(null, parameterMap);
			if(Strings.isNotEmpty(affairList)){
				for(CtpAffair affair : affairList){
					affair.setDelete(true);
				}
				affairManager.updateAffairs(affairList);
			}
		}
		
		if(Strings.isNotEmpty(perIdList)) {
			meetingMessageManager.sendRoomRefuseMessage(room, perIdList);
		}
		
		return true;
	}
	
	/**
	 * 删除会议室审核记录
	 * @param parameterMap
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean transClearRoomPerm(Map<String, Object> parameterMap) throws BusinessException {
		User currentUser = (User)parameterMap.get("currentUser");
		List<Long> roomAppIdList = parameterMap.get("roomAppIdList")==null ? null : (List<Long>)parameterMap.get("roomAppIdList");
		
		List<MeetingRoomApp> roomAppList = meetingRoomAppManager.getRoomAppListById(roomAppIdList);
		
		List<Long> roomIdList = new ArrayList<Long>();
		for(MeetingRoomApp bean : roomAppList) {
			roomIdList.add(bean.getRoomId());
		}
		
		Map<Long, MeetingRoom> roomMap = meetingRoomManager.getRoomMap(roomIdList);
		
		List<String[]> delLogList = new ArrayList<String[]>(); // 批量写入日志用到的参数
		for(MeetingRoomApp bean : roomAppList) {
			String[] delLogs = new String[2];
			delLogs[0] = currentUser.getName();
			MeetingRoom room = roomMap.get(bean.getRoomId());
			delLogs[1] = room==null ? "" : room.getName();
            delLogList.add(delLogs);
		}
		
		meetingRoomPermDao.deleteRoomPermByAppId(roomAppIdList);
		
		appLogManager.insertLogs(currentUser, AppLogAction.Meeting_app_delete, delLogList);
		
		return true;
	}
	
	/**
	 * 保存会议室审核记录
	 * @param po
	 * @throws BusinessException
	 */
	@Override
	public void saveOrUpdate(MeetingRoomPerm po) throws BusinessException {
		this.meetingRoomPermDao.saveOrUpdate(po);
	}
	
	/**
	 * 批量保存会议室审核记录
	 * @param poList
	 * @throws BusinessException
	 */
	@Override
	public void saveRoomPerm(List<MeetingRoomPerm> poList) throws BusinessException {
		this.meetingRoomPermDao.saveRoomPerm(poList);
	}
	
	/**
	 * 批量删除会议室审核记录
	 * @param appIdList
	 * @param boolean isDeleteAffair
	 * @throws BusinessException
	 */
	@Override
	public void deleteRoomPermByAppId(List<Long> appIdList, boolean isDeleteAffair) throws BusinessException {
		this.meetingRoomPermDao.deleteRoomPermByAppIdPhysical(appIdList);
		if(isDeleteAffair) {
			affairManager.deleteByIds(appIdList);
		}
	}
	
	/**
	 * 获取单个会议室审核
	 * @param roomPermId
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public MeetingRoomPerm getRoomPermById(Long id) throws BusinessException {
		return meetingRoomPermDao.getRoomPermById(id);
	}
	
	/**
	 * 通会议室申请ID获取会议室审核
	 * @param roomAppId
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public MeetingRoomPerm getRoomPermByAppId(Long roomAppId) throws BusinessException {
		return meetingRoomPermDao.getRoomPermByAppId(roomAppId);
	}
	
	/**
	 * 通会议室申请ID获取会议室审核
	 * @param roomAppId
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public List<MeetingRoomPerm> getRoomPermListByAppId(List<Long> roomAppIdList) throws BusinessException {
		return meetingRoomPermDao.getRoomPermListByAppId(roomAppIdList);
	}
	
	/**
	 * 获取某会议室待审核记录
	 * @param roomId
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public List<MeetingRoomPerm> getRoomPermWaitList(Long roomId) throws BusinessException {
		return this.meetingRoomPermDao.getRoomPermWaitList(roomId);
	}
	
	/**
	 * 清理会议室已审核的待办数据
	 * @throws BusinessException
	 */
	@Override
	public void clearMeetingRoomPermedAffairData() throws BusinessException {		
		List<Long> appIdList = new ArrayList<Long>();
		
		Map<String,Object> parameterMap = new HashMap<String,Object>();
		parameterMap.put("app", Integer.valueOf(ApplicationCategoryEnum.meetingroom.key()));
		parameterMap.put("state", Integer.valueOf(StateEnum.col_pending.getKey()));
		parameterMap.put("subState", MeetingHelper.getRoomPermSubStateList());
		parameterMap.put("delete", Boolean.valueOf(false));
		List<CtpAffair> affairList = affairManager.getByConditions(null, parameterMap);
		if(Strings.isNotEmpty(affairList)) {
			
			for(CtpAffair affair : affairList) {
				if(!appIdList.contains(affair.getObjectId())) {
					appIdList.add(affair.getObjectId());
				}
			}
			
			List<Long> doneAppIdList = meetingRoomPermDao.getRoomPermedAppIdList(appIdList);
			if(Strings.isNotEmpty(doneAppIdList)) {
				List<CtpAffair> updateAffairList = new ArrayList<CtpAffair>();
				for(CtpAffair affair : affairList) {
					if(doneAppIdList.contains(affair.getObjectId())) {
						affair.setState(StateEnum.col_done.key());
						updateAffairList.add(affair);
					}
				}
				if(Strings.isNotEmpty(updateAffairList)) {
					affairManager.updateAffairs(updateAffairList);
				}
			}
		}
	}

	@Override
	public void removeByAppId(Long meetingRoomAppId) throws BusinessException {
		List<Long> ids = new ArrayList<Long>();
		ids.add(meetingRoomAppId);
		meetingRoomPermDao.deleteRoomPermByAppIdPhysical(ids);
	}


	private void updatedRoomPermAffair(MeetingRoomAppVO appVo) throws BusinessException {
		List<Long> roomAppIdList = new ArrayList<Long>();
		if(Strings.isEmpty(appVo.getRoomPermList())) {
			roomAppIdList.add(appVo.getMeetingRoomPerm().getRoomAppId());
		} else {
			for(MeetingRoomPerm bean : appVo.getRoomPermList()) {
				roomAppIdList.add(bean.getRoomAppId());
			}
		}
		Map<String,Object> parameters = new HashMap<String, Object>(16);
		parameters.put("objectId",roomAppIdList);
		List<CtpAffair> affairList = affairManager.getByConditions(null,parameters);

		/**
		 * 单条周期会议的审核
		 */
		boolean isPeriodAuditOnce = Strings.isEmpty(appVo.getPeriodicityRoomAppIdList()) && !MeetingUtil.isIdNull(appVo.getMeetingRoomApp().getPeriodicityId());
		if(isPeriodAuditOnce){
			List<MeetingRoomApp> periodicityRoomAppList = meetingRoomAppManager.getPeriodicityExceptApps(appVo.getMeetingRoomApp());
			if(!periodicityRoomAppList.isEmpty()){
				Long nextAppId = periodicityRoomAppList.get(0).getId();
				for(CtpAffair affair : affairList){
					affair.setObjectId(nextAppId);
				}
			}else{
				for(CtpAffair affair : affairList) {
					affair.setState(StateEnum.col_done.key());
					affair.setCompleteTime(appVo.getSystemNowDatetime());
				}
			}
		}else {
			for(CtpAffair affair : affairList) {
				affair.setState(StateEnum.col_done.key());
				affair.setCompleteTime(appVo.getSystemNowDatetime());
			}
		}
		affairManager.updateAffairs(affairList);
	}

	/**
	 * 设置代理相关信息
	 * @param appVo
	 */
	private void setProxyInfo(MeetingRoomAppVO appVo) throws BusinessException{
		Long userId = appVo.getCurrentUser().getId();

		Long affairId = appVo.getAffairId();
		CtpAffair affair = affairManager.get(affairId);
		if(affair != null){
			appVo.setCurrentAffair(affair);
		}else{
			List<CtpAffair> affairList = affairManager.getAffairs(ApplicationCategoryEnum.meetingroom, appVo.getMeetingRoomApp().getId());
			appVo.setCurrentAffair(Strings.isNotEmpty(affairList) ? affairList.get(0) : null);
			for(CtpAffair tempAffair : affairList){
				if(tempAffair.getMemberId().equals(userId)){
					appVo.setCurrentAffair(tempAffair);
				}
			}
		}

		appVo.setProxyId(-1l);
		CtpAffair currentAffair = appVo.getCurrentAffair();
		if (null!=currentAffair) {
			appVo.setAuditingId(currentAffair.getMemberId());
		}
		if(null==currentAffair || (null!=currentAffair && !currentAffair.getMemberId().equals(userId))){
			MeetingRoom meetingroom = appVo.getMeetingRoom();
			if (null != meetingroom) {
				String roomAdmin = meetingroom.getAdmin();
				if (Strings.isNotBlank(roomAdmin)) {
					if (roomAdmin.indexOf(String.valueOf(userId)) == -1 ) {//当前登录人不是管理员,代理处理 
						appVo.setProxyId(userId);
						//设置我为代理人的人员
						List<Long> agentMemberId = MemberAgentBean.getInstance().getAgentToMemberId(ApplicationCategoryEnum.meeting.key(),userId);
						String[] roomAdminList = roomAdmin.split(",");
						for (int i=0; i<agentMemberId.size(); i++) {
							for (int j=0; j<roomAdminList.length; j++) {
								if (roomAdminList[j].equals(String.valueOf(agentMemberId.get(i)))) {
									appVo.setAuditingId(agentMemberId.get(i));
									return;
								}
							}
						}
					}
				}
			}
		}
	}
	
	/****************************** 依赖注入 **********************************/
	public void setMeetingRoomPermDao(MeetingRoomPermDao meetingRoomPermDao) {
		this.meetingRoomPermDao = meetingRoomPermDao;
	}
	public void setMeetingRoomAppManager(MeetingRoomAppManager meetingRoomAppManager) {
		this.meetingRoomAppManager = meetingRoomAppManager;
	}
	public void setMeetingRoomRecordManager(MeetingRoomRecordManager meetingRoomRecordManager) {
		this.meetingRoomRecordManager = meetingRoomRecordManager;
	}
	public void setMeetingManager(MeetingManager meetingManager) {
		this.meetingManager = meetingManager;
	}
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public void setAppLogManager(AppLogManager appLogManager) {
		this.appLogManager = appLogManager;
	}
	public void setMeetingMessageManager(MeetingMessageManager meetingMessageManager) {
		this.meetingMessageManager = meetingMessageManager;
	}
	public void setMeetingRoomManager(MeetingRoomManager meetingRoomManager) {
		this.meetingRoomManager = meetingRoomManager;
	}
	
}
