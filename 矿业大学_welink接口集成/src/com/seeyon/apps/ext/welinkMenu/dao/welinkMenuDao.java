package com.seeyon.apps.ext.welinkMenu.dao;


import com.seeyon.apps.ext.welinkMenu.po.WeLinkOaMapper;
import com.seeyon.apps.ext.welinkMenu.po.WeLinkUsers;

import java.util.List;

public interface welinkMenuDao {
    int insertWebLinkUsers(WeLinkUsers users);

    WeLinkUsers selectByCurrentUserId(String userId);

    int updateWebLinkUsers(WeLinkUsers users);

    List<WeLinkUsers> selectListByOauserIdOfUnion(List<String> list);

    /*************************oa welink 会议id关联**********************************/
    int insertWlAndOaMapper(WeLinkOaMapper weLinkOaMapper);

    WeLinkOaMapper selectByOaMeetingId(String oaMeetingId);

    /***************************************************************************/

    List<String> selectUserIDByOrgId(String orgId);

}