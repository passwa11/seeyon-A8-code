package com.seeyon.apps.meetingSeat.manager;

import java.util.List;
import java.util.Map;

import org.apache.geode.internal.cache.FilterProfile.interestType;

import com.seeyon.apps.meetingSeat.po.Formson0256;



public interface MeetingSeatManager {
	
	Map<String, Object> hello(int key, Map<String, Object> param);
	
	//根据会议编号获取人员信息
	List<Map> getMeetingSeatPersonList(String hybh);
	
	//会议排座 修改会议排座信息表
	int SetMeetingSeatPerson(String meetingId,String name,String dep,String col,String row);

	//根据会议编号获取未报送单位信息
	List<Map> getMeetingSeatDepNoList(String meetingId);

	//会议排座，修改会议排座单位信息
	void SetMeetingSeatDep(List<Formson0256> formson0256);

	//将单位未报送改为以报送
	void modMeetingSeatDepStatus(String meetingId, String name);

}
