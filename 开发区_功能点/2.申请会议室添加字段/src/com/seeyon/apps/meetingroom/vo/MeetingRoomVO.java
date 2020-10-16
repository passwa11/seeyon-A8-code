package com.seeyon.apps.meetingroom.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.seeyon.apps.meeting.util.MeetingUtil;
import com.seeyon.apps.meetingroom.po.MeetingRoom;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;

/**
 * 
 * @author 唐桂林
 *
 */
public class MeetingRoomVO extends MeetingRoom {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2990778547836934192L;
	
	public static final String MEETINGROOM_REGISTER = "register";
	
	public static final String MEETINGROOM_EDIT = "edit";
	
	private String errorMsg;
	
	private String mngdepName;
	
	public String adminMembers;
	public String adminIds;
	public String adminNames;

	private Date systemNowDatetime;
	
	private User currentUser;
	
	private Attachment attObj;
	private List<Attachment> attatchments;

	/**
	 * 原会议室图片单文件
	  */
	private Attachment imageObj;

	private String imageIds;
	private List<Attachment> attatchImage;
	private String image;

	private MeetingRoom meetingRoom;

	private boolean hasMeetingRoomApp;

	private Long roomId;

	private MeetingRoom oldRoom;

	/**
	 * 二维码附件关联
	 */
	private Long qrCodeApply;

	private List<V3xOrgMember> roomAdminList = new ArrayList<V3xOrgMember>();

	//zhou
	private List<MeetingRoom> roomList;

	public List<MeetingRoom> getRoomList() {
		return roomList;
	}

	public void setRoomList(List<MeetingRoom> roomList) {
		this.roomList = roomList;
	}

	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	public String getMngdepName() {
		return mngdepName;
	}
	public void setMngdepName(String mngdepName) {
		this.mngdepName = mngdepName;
	}
	public String getAdminIds() {
		return adminIds;
	}
	public void setAdminIds(String adminIds) {
		this.adminIds = adminIds;
	}
	public String getAdminNames() {
		return adminNames;
	}
	public void setAdminNames(String adminNames) {
		this.adminNames = adminNames;
	}
	public Date getSystemNowDatetime() {
		return systemNowDatetime;
	}
	public void setSystemNowDatetime(Date systemNowDatetime) {
		this.systemNowDatetime = systemNowDatetime;
	}
	public User getCurrentUser() {
		return currentUser;
	}
	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}
	public Attachment getAttObj() {
		return attObj;
	}
	public void setAttObj(Attachment attObj) {
		this.attObj = attObj;
	}
	public List<Attachment> getAttatchments() {
		return attatchments;
	}
	public void setAttatchments(List<Attachment> attatchments) {
		this.attatchments = attatchments;
	}
	public List<Attachment> getAttatchImage() {
		return attatchImage;
	}
	public void setAttatchImage(List<Attachment> attatchImage) {
		this.attatchImage = attatchImage;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getImageIds() {
		return imageIds;
	}
	public void setImageIds(String imageIds) {
		this.imageIds = imageIds;
	}
	public Attachment getImageObj() {
		return imageObj;
	}
	public void setImageObj(Attachment imageObj) {
		this.imageObj = imageObj;
	}
	public boolean isHasMeetingRoomApp() {
		return hasMeetingRoomApp;
	}
	public void setHasMeetingRoomApp(boolean hasMeetingRoomApp) {
		this.hasMeetingRoomApp = hasMeetingRoomApp;
	}
	public MeetingRoom getMeetingRoom() {
		return meetingRoom;
	}
	public void setMeetingRoom(MeetingRoom meetingRoom) {
		this.meetingRoom = meetingRoom;
	}
	public Long getRoomId() {
		return roomId;
	}
	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}
	public List<V3xOrgMember> getRoomAdminList() {
		return roomAdminList;
	}
	public void setRoomAdminList(List<V3xOrgMember> roomAdminList) {
		this.roomAdminList = roomAdminList;
	}
	public String getAdminMembers() {
		return adminMembers;
	}
	public void setAdminMembers(String adminMembers) {
		this.adminMembers = adminMembers;
	}
	public MeetingRoom getOldRoom() {
		return oldRoom;
	}
	public void setOldRoom(MeetingRoom oldRoom) {
		this.oldRoom = oldRoom;
	}
	public boolean isNew() {
		return MeetingUtil.isIdNull(roomId);
    }

	@Override
	public Long getQrCodeApply() {
		return qrCodeApply;
	}

	@Override
	public void setQrCodeApply(Long qrCodeApply) {
		this.qrCodeApply = qrCodeApply;
	}
}
