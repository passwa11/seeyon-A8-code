package com.seeyon.apps.meetingSeat.dao;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.meetingSeat.po.Formson0256;

public interface MeetingSeatDao {
	
	
	public List<Map> selectIdByhybh(String hybh);
	
	public int updateMeetingSeatPerson(String meetingId,String name,String dep,String col,String row);
	
	public int updateMeetingSeatDepStatus(String meetingId, String name);

	public List<Map> selectDepNoList(String meetingId);

	public void insertMeetingSeatDep(List<Formson0256> formson0256);

	

}
