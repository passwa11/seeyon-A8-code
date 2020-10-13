package com.seeyon.ctp.rest.resources;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.util.Datetimes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class MeetingRoomRestVO {
	private static final Log LOG = LogFactory.getLog(MeetingRoomRestVO.class);
	
	/**
	 * 会议室id
	 */
	private Long roomId;
	
	/**
	 * 会议id
	 */
	private Long meetingId;
	
	/**
	 * 会议室申请表Id
	 */
	private Long roomAppId;
	
	/**
	 * 会议室名称
	 */
	private String roomName;
	
	/**
	 * 可容纳
	 */
	private Integer roomSeatCount;
	
	/**
	 * 会议室申请状态
	 */
	private Integer appStatus;
	
	/**
	 * 开始时间
	 */
	private String startDatetime;
	
	/**
	 * 结束时间
	 */
	private String endDatetime;
	
	/** 图片 */
    private String image;
    
    /**
     * 申请人
     */
    private String appPerName;
    
    /**
     * 申请人
     */
    private Long appPerId;
    
    /**
     * 时间展示
     */
    private String showTime;
    
    /**
     * 是否能撤销申请的会议室
     */
    private boolean isCancelMRApp;
    
    /**
     * 用途
     */
    private String description;
    
    /**
     * 审核意见
     */
    private String permDescription;
    
    /**
     * 是否能提前结束的会议室
     */
    private boolean isFinishMRApp;
    
    /**
     * 会议名称
     */
    private String meetingName;
    
    /**
     * 会议室审核人
     */
    private Map<Long,String> adminLab;
    
    /**
     * 会议用品
     */
    private String meetingResources;

	private String sqrdh;
	private String hcyq;
	private String sqrdept;
	private String ldname;

	public String getLdname() {
		return ldname;
	}

	public void setLdname(String ldname) {
		this.ldname = ldname;
	}

	public String getSqrdh() {
		return sqrdh;
	}

	public void setSqrdh(String sqrdh) {
		this.sqrdh = sqrdh;
	}

	public String getHcyq() {
		return hcyq;
	}

	public void setHcyq(String hcyq) {
		this.hcyq = hcyq;
	}

	public String getSqrdept() {
		return sqrdept;
	}

	public void setSqrdept(String sqrdept) {
		this.sqrdept = sqrdept;
	}

	public String getMeetingName() {
        return meetingName;
    }
	
    public void setMeetingName(String meetingName) {
        this.meetingName = meetingName;
    }
    
    public boolean getIsFinishMRApp() {
        return isFinishMRApp;
    }
    
    public void setFinishMRApp(boolean isFinishMRApp) {
        this.isFinishMRApp = isFinishMRApp;
    }
    
    public Long getRoomId() {
		return roomId;
	}

	public Long getMeetingId() {
		return meetingId;
	}
	
	public Long getRoomAppId() {
		return roomAppId;
	}

	public void setRoomAppId(Long roomAppId) {
		this.roomAppId = roomAppId;
	}

	public String getRoomName() {
		return roomName;
	}

	public Integer getRoomSeatCount() {
		return roomSeatCount;
	}

	public Integer getAppStatus() {
		return appStatus;
	}

	public String getStartDatetime() {
		return startDatetime;
	}

	public String getEndDatetime() {
		return endDatetime;
	}

	public String getImage() {
		return image;
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public void setMeetingId(Long meetingId) {
		this.meetingId = meetingId;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public void setRoomSeatCount(Integer roomSeatCount) {
		this.roomSeatCount = roomSeatCount;
	}

	public void setAppStatus(Integer appStatus) {
		this.appStatus = appStatus;
	}

	public void setStartDatetime(String startDatetime) {
		this.startDatetime = startDatetime;
	}

	public void setEndDatetime(String endDatetime) {
		this.endDatetime = endDatetime;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getAppPerName() {
		return appPerName;
	}

	public void setAppPerName(String appPerName) {
		this.appPerName = appPerName;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getShowTime() {
		try {
			String startDateYearMD = this.startDatetime.substring(0, 10);
			String endDateYearMD = this.endDatetime.substring(0, 10);

			/**
			 * 判断今天，明天，今年
			 */
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(new Date());
			String year = String.valueOf(calendar.get(Calendar.YEAR));
			String today = Datetimes.format(calendar.getTime(),Datetimes.dateStyle);
			calendar.add(Calendar.DATE,1);
			String tomorrow = Datetimes.format(calendar.getTime(),Datetimes.dateStyle);
			String beginDate = this.startDatetime.substring(5,10),endDate = this.endDatetime.substring(5, 10);
			String beginYear = this.startDatetime.substring(0,4),endYear = this.endDatetime.substring(0,4);

			if(startDateYearMD.equals(today)){
				beginDate = ResourceUtil.getString("meeting.list.date.today");
			}else if(startDateYearMD.equals(tomorrow)){
				beginDate = ResourceUtil.getString("meeting.list.date.tomorrow");
			}else if(!beginYear.equals(year) || !beginYear.equals(endYear)){
				beginDate = this.startDatetime.substring(0,10);
			}

			if(endDateYearMD.equals(today)){
				endDate = ResourceUtil.getString("meeting.list.date.today");
			}else if(endDateYearMD.equals(tomorrow)){
				endDate = ResourceUtil.getString("meeting.list.date.tomorrow");
			}else if(!endYear.equals(year) || !endYear.equals(beginYear)){
				endDate = this.endDatetime.substring(0, 10);
			}

			if (startDateYearMD.equals(endDateYearMD)){
				showTime = "<span class='date' >" + beginDate +"</span>"
						+ "<span class='time'>" + this.startDatetime.substring(11, 16) +"</span><span class='join'>-</span>"
						+ "<span class='time'>" + this.endDatetime.substring(11, 16) +"</span>";
			} else {
				showTime = "<span class='date' >" + beginDate +"</span>"
						+"<span class='time' >" + this.startDatetime.substring(11, 16) +"</span><span class='join'>-</span>"
						+"<span class='date' >" + endDate +"</span>"
						+"<span class='time' >" + this.endDatetime.substring(11, 16) +"</span>";
			}
		} catch (Exception e) {
			LOG.error("",e);
		}
		return showTime;
	}

	public boolean isCancelMRApp() {
		return isCancelMRApp;
	}

	public void setCancelMRApp(boolean isCancelMRApp) {
		this.isCancelMRApp = isCancelMRApp;
	}

	public String getPermDescription() {
		return permDescription;
	}

	public void setPermDescription(String permDescription) {
		this.permDescription = permDescription;
	}

	public Long getAppPerId() {
		return appPerId;
	}

	public void setAppPerId(Long appPerId) {
		this.appPerId = appPerId;
	}

	public Map<Long, String> getAdminLab() {
		return adminLab;
	}

	public void setAdminLab(Map<Long, String> adminLab) {
		this.adminLab = adminLab;
	}
	
	public String getMeetingResources() {
		return meetingResources;
	}

	public void setMeetingResources(String meetingResources) {
		this.meetingResources = meetingResources;
	}
}
