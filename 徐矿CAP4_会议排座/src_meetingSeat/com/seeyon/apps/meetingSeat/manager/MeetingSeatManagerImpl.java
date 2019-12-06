package com.seeyon.apps.meetingSeat.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.meetingSeat.dao.MeetingSeatDao;
import com.seeyon.apps.meetingSeat.po.Formson0256;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.util.annotation.AjaxAccess;

public class MeetingSeatManagerImpl implements MeetingSeatManager {

    private MeetingSeatDao meetingSeatDao;

    /**
     * 方法不要抛出异常，返回给前台的一定是提示消息，后台记录错误日志
     * 如果是controller层调用，那么manager、dao都不要处理异常，统一抛给controller层进行异常处理
     * rest接口也一样，不要抛出异常，统一返回消息
     */
    @Override
    @AjaxAccess
    public Map<String, Object> hello(int key, Map<String, Object> param) {
        param.put("key", ++key);
        return param;
    }


    public MeetingSeatDao getMeetingSeatDao() {
        if (meetingSeatDao == null) {
            meetingSeatDao = (MeetingSeatDao) AppContext.getBean("meetingSeatDao");
        }
        return meetingSeatDao;
    }

    @Override
    public List<Map> getMeetingSeatPersonList(String hybh) {
        List<Map> list = getMeetingSeatDao().selectIdByhybh(hybh);
        return list;
    }

    @Override
    public List<Map> getMeetingSeatDepNoList(String meetingId) {
        List<Map> list = getMeetingSeatDao().selectDepNoList(meetingId);
        return list;
    }


    @Override
    public int SetMeetingSeatPerson(String meetingId, String name, String dep, String col, String row) {
        int result = getMeetingSeatDao().updateMeetingSeatPerson(meetingId, name, dep, col, row);
        return result;
    }


    @Override
    public void SetMeetingSeatDep(List<Formson0256> formson0256) {
        getMeetingSeatDao().insertMeetingSeatDep(formson0256);

    }

    @Override
    public void modMeetingSeatDepStatus(String meetingId, String name) {
        int result = getMeetingSeatDao().updateMeetingSeatDepStatus(meetingId, name);

    }


    public static void main(String[] args) {
        MeetingSeatManager manager = new MeetingSeatManagerImpl();
        List<Map> list = manager.getMeetingSeatPersonList("HYBH201905004");
        System.out.println(list);
    }


}
