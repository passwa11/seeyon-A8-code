package com.seeyon.apps.meeting.manager;

import com.google.common.collect.Lists;
import com.seeyon.apps.ext.welinkMenu.manager.welinkMenuManagerImpl;
import com.seeyon.apps.ext.welinkMenu.po.WeLinkOaMapper;
import com.seeyon.apps.ext.welinkMenu.po.WeLinkUsers;
import com.seeyon.apps.ext.welinkMenu.util.WelinkUtil;
import com.seeyon.apps.meeting.constants.MeetingConstant;
import com.seeyon.apps.meeting.constants.MeetingListConstant;
import com.seeyon.apps.meeting.util.MeetingUtil;
import com.seeyon.apps.meeting.vo.MeetingPanelConfigVO;
import com.seeyon.apps.meetingroom.manager.MeetingRoomManager;
import com.seeyon.apps.meetingroom.po.MeetingRoom;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.v3x.common.web.login.CurrentUser;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会议ajax接口
 * @author gyp
 */
public class MeetingAjaxManagerImpl implements MeetingAjaxManager{

    private static final Log LOGGER = CtpLogFactory.getLog(MeetingAjaxManagerImpl.class);

    private MeetingSettingManager meetingSettingManager;
    private MeetingBarCodeManager meetingBarCodeManager;
    private MeetingManager meetingManager;
    private MeetingRoomManager meetingRoomManager;

    private com.seeyon.apps.ext.welinkMenu.manager.welinkMenuManager welinkMenuManager = new welinkMenuManagerImpl();


    public void setMeetingSettingManager(MeetingSettingManager meetingSettingManager) {
        this.meetingSettingManager = meetingSettingManager;
    }

    public void setMeetingBarCodeManager(MeetingBarCodeManager meetingBarCodeManager) {
        this.meetingBarCodeManager = meetingBarCodeManager;
    }

    public void setMeetingManager(MeetingManager meetingManager) {
        this.meetingManager = meetingManager;
    }

    public void setMeetingRoomManager(MeetingRoomManager meetingRoomManager) {
        this.meetingRoomManager = meetingRoomManager;
    }

    @Override
    public Map<String, Object> isMeetingPlaceInputAble() throws BusinessException {
        Map<String, Object> res = new HashMap<String, Object>(16);
        res.put(MeetingConstant.MEETING_PLACE_INPUT,meetingSettingManager.isMeetingPlaceInputAble());
        return res;
    }

    @Override
    public boolean saveMeetingPlaceSetting(Map<String, Object> param) throws BusinessException {
        return meetingSettingManager.saveMeetingPlaceSetting(param);
    }

    @Override
    public Map<String, Object> meetingPanelData(Map<String, Object> param) throws BusinessException {
        return meetingBarCodeManager.meetingPanelData(param);
    }
    
    @SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getMeetingScreenSet() throws BusinessException {
		Map res = new HashMap<String, Object>(16);
		try {
			res = BeanUtils.describe(meetingSettingManager.getMeetingScreenSet(AppContext.currentAccountId()));
		} catch (Exception e) {
			LOGGER.error("读取会议指示屏配置异常", e);
		}
		return res;
	}

	@Override
	public void saveMeetingScreenSet(Map<String, Object> params) throws BusinessException {
		meetingSettingManager.saveOrUpdateMeetingScreenSet(params);
	}

    /**
     * 撤销会议
     * @param param
     * @return
     * @throws BusinessException
     */
    @Override
    public Map<String, Object> cancelMeeting(Map<String, Object> param) throws BusinessException {
        String listType = ParamUtil.getString(param,"listType", MeetingListConstant.ListTypeEnum.listSendMeeting.name());
        Long meetingId = ParamUtil.getLong(param,"id", Constants.GLOBAL_NULL_ID);
        String content = ParamUtil.getString(param,"cancelComment","");
        boolean isBatch = Boolean.parseBoolean(ParamUtil.getString(param,"isBatch","0"));
        int type = MeetingListConstant.MeetingListTypeEnum.getTypeName(listType);

        Map<String, Object> parameterMap = new HashMap<String, Object>(16);
        parameterMap.put("meetingId", meetingId);
        parameterMap.put("type", type);
        parameterMap.put("isBatch", isBatch);
        parameterMap.put("currentUser", AppContext.getCurrentUser());
        parameterMap.put("content", content);

        Map<String, Object> result = new HashMap<String, Object>(16);
        try {
            boolean flag=meetingManager.transCancelMeeting(parameterMap);
            if(flag){
                Long currentUserID = CurrentUser.get().getId();
                WeLinkUsers weLinkUsers = welinkMenuManager.selectByCurrentUserId(currentUserID + "");
                String token = WelinkUtil.getInstance().getToken(weLinkUsers.getWeLinkLoginname(), weLinkUsers.getWeLinkPwd());
                WeLinkOaMapper oaMapper = welinkMenuManager.selectByOaMeetingId(meetingId + "");
                if (null != oaMapper) {
                    String delete = WelinkUtil.getInstance().welinkDeleteMeeting(token, oaMapper.getWelinkMeetingId());
                }
            }
        }catch (Exception e){
            LOGGER.error("会议撤销出错",e);
            if(MeetingConstant.VIDEO_MEEING_START_CODE.equals(e.getMessage())){
                result.put(MeetingConstant.AJAX_STATE,false);
                result.put(MeetingConstant.AJAX_MESSAGE,ResourceUtil.getString("meeting.page.msg.videoMeetingStart"));
                return result;
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> finishMeeting(Map<String, Object> param) throws BusinessException {
        String listType = ParamUtil.getString(param,"listType", MeetingListConstant.ListTypeEnum.listSendMeeting.name());
        int type = MeetingListConstant.MeetingListTypeEnum.getTypeName(listType);
        Long meetingId = ParamUtil.getLong(param,"id", Constants.GLOBAL_NULL_ID);
        Map<String, Object> result = new HashMap<String, Object>(16);
        if(!MeetingUtil.isIdNull(meetingId)) {
            try {
                Map<String, Object> parameterMap = new HashMap<String, Object>(16);
                parameterMap.put("meetingId", meetingId);
                parameterMap.put("type", type);
                parameterMap.put("currentUser", AppContext.getCurrentUser());
                parameterMap.put("action", MeetingConstant.MeetingActionEnum.finishMeeting.name());
                parameterMap.put("endDatetime", DateUtil.currentDate());
                meetingManager.transFinishAdvanceMeeting(parameterMap);
            } catch(Exception e) {
                LOGGER.error("会议提前结束出错", e);
                if(MeetingConstant.VIDEO_MEEING_START_CODE.equals(e.getMessage())){
                    result.put(MeetingConstant.AJAX_STATE,false);
                    result.put(MeetingConstant.AJAX_MESSAGE,ResourceUtil.getString("meeting.page.msg.videoMeetingStart"));
                    return result;
                }
            }
        }

        return null;
    }

    @Override
    public Map<String, Object> finishMeetingRoom(Map<String, Object> param) throws BusinessException {
        Long id = ParamUtil.getLong(param,"id", Constants.GLOBAL_NULL_ID);
        boolean isContainMeeting = MeetingUtil.getBoolean(param, "isContainMeeting", Boolean.FALSE);

        Map<String, Object> parameterMap = new HashMap<String, Object>(16);
        parameterMap.put("roomAppId", id);
        parameterMap.put("currentUser", AppContext.getCurrentUser());
        parameterMap.put("endDatetime", DateUtil.currentDate());
        parameterMap.put("action", MeetingConstant.MeetingActionEnum.finishRoomApp.name());
        parameterMap.put("isContainMeeting", isContainMeeting);

        Map<String, Object> result = new HashMap<String, Object>(16);
        try {
            meetingRoomManager.transFinishAdvanceRoomApp(parameterMap);
        }catch (Exception e){
            LOGGER.error("会议室提前结束出错", e);
            if(MeetingConstant.VIDEO_MEEING_START_CODE.equals(e.getMessage())){
                result.put(MeetingConstant.AJAX_STATE,false);
                result.put(MeetingConstant.AJAX_MESSAGE,ResourceUtil.getString("meeting.page.msg.videoMeetingStart"));
                return result;
            }
        }
        return null;
    }

	@Override
	public void saveOrUpdateMeetingPanel(Map<String, Object> param) throws BusinessException {
		meetingSettingManager.saveOrUpdateMeetingPanel(param);
	}

	@Override
	public FlipInfo findMeetingPanel(FlipInfo fi, Map<String, Object> params) throws BusinessException {
        params.put("accountId", AppContext.currentAccountId());
		return meetingSettingManager.findMeetingPanel(params, fi);
	}

	@Override
	public void deleteMeetingPanel(List<Long> panelIds) throws BusinessException {
		if(CollectionUtils.isNotEmpty(panelIds)){
			List<Long> pDeleteIds = Lists.newArrayList();
			for(Object pId : panelIds){
				pDeleteIds.add(Long.parseLong(pId.toString()));
			}
			meetingSettingManager.deleteMeetingPanel(pDeleteIds);
		}
	}

	@Override
	public List<MeetingRoom> findMeetingRoomsByName(Map<String, Object> params) throws BusinessException {
		params.put("accountId", AppContext.currentAccountId());
		return meetingRoomManager.findMeetingRoomsByName(params);
	}
	@Override
	public void saveOrUpdateMeetingPanelConfig(Map<String, Object> param) throws BusinessException {
		meetingSettingManager.saveOrUpdateMeetingPanelConfig(param);
	}

	@Override
	public MeetingPanelConfigVO getMeetingPanelConfig(Map<String, Object> param) throws BusinessException {
		param.put("accountId", AppContext.getCurrentUser().getLoginAccount());
		return meetingSettingManager.getMeetingPanelConfig(param);
	}

	@Override
	public Map<String, Object> meetingPanelDisplay(Map<String, Object> param) throws BusinessException {
		return  meetingBarCodeManager.meetingPanelDisplay(param);
	}
	
}
