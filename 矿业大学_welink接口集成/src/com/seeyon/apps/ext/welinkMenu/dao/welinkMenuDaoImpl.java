package com.seeyon.apps.ext.welinkMenu.dao;


import com.seeyon.apps.ext.welinkMenu.po.WeLinkOaMapper;
import com.seeyon.apps.ext.welinkMenu.po.WeLinkUsers;
import com.seeyon.ctp.util.JDBCAgent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class welinkMenuDaoImpl implements welinkMenuDao {

    @Override
    public List<String> selectUserIDByOrgId(String orgId) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet set = null;
        String sql = "select id from org_member where ORG_DEPARTMENT_ID =?  and IS_ENABLE=1 and IS_DELETED=0 ";
        List<String> list = new ArrayList<>();
        try {
            connection = JDBCAgent.getRawConnection();
            ps = connection.prepareStatement(sql);
            ps.setString(1, orgId);
            set = ps.executeQuery();
            while (set.next()) {
                list.add(set.getString("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                set.close();
                ps.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public List<WeLinkUsers> selectListByOauserIdOfUnion(List<String> list) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        StringBuffer sql = new StringBuffer();
        List<WeLinkUsers> weLinkUsersList = new ArrayList<>();
        try {
            connection = JDBCAgent.getRawConnection();
            for (int i = 0; i < list.size(); i++) {
                sql.append("select oa_user_id,welink_loginname,welink_pwd,oa_user_name,oa_phone from welink_users where oa_user_id=" + list.get(i) + " and rownum=1 ");
                if (i != (list.size() - 1)) {
                    sql.append(" UNION ");
                }
            }
            ps = connection.prepareStatement(sql.toString());
            resultSet = ps.executeQuery();
            WeLinkUsers weLinkUsers = null;
            while (resultSet.next()) {
                weLinkUsers = new WeLinkUsers();
                weLinkUsers.setOaUserId(resultSet.getString("oa_user_id"));
                weLinkUsers.setWeLinkLoginname(resultSet.getString("welink_loginname"));
                weLinkUsers.setWeLinkPwd(resultSet.getString("welink_pwd"));
                weLinkUsers.setOaUserName(resultSet.getString("oa_user_name"));
                weLinkUsers.setOaPhone(resultSet.getString("oa_phone"));
                weLinkUsersList.add(weLinkUsers);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
                ps.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return weLinkUsersList;
    }

    @Override
    public int insertWebLinkUsers(WeLinkUsers users) {
        Connection connection = null;
        PreparedStatement pst = null;
        int flag = -1;
        try {
            if (null != users) {
                String insertSql = "insert into welink_users(oa_user_id,welink_loginname,welink_pwd,oa_user_name,oa_phone) values(?,?,?,?,?) ";
                connection = JDBCAgent.getRawConnection();
                pst = connection.prepareStatement(insertSql);
                pst.setString(1, users.getOaUserId());
                pst.setString(2, users.getWeLinkLoginname());
                pst.setString(3, users.getWeLinkPwd());
                pst.setString(4, users.getOaUserName());
                pst.setString(5, users.getOaPhone());
                flag = pst.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pst.close();
                connection.close();
            } catch (SQLException e) {
                System.out.println("关闭jdbc链接出错了！");
            }
        }
        return flag;
    }

    @Override
    public WeLinkUsers selectByCurrentUserId(String userId) {
        Connection connection = null;
        PreparedStatement pst = null;
        ResultSet resultSet = null;
        WeLinkUsers linkUsers = null;
        try {
            String selectSql = "select oa_user_id,welink_loginname,welink_pwd,oa_user_name,oa_phone from welink_users where oa_user_id=? and rownum=1";
            connection = JDBCAgent.getRawConnection();
            pst = connection.prepareStatement(selectSql);
            pst.setString(1, userId);
            resultSet = pst.executeQuery();
            while (resultSet.next()) {
                linkUsers = new WeLinkUsers();
                linkUsers.setOaUserId(resultSet.getString(1));
                linkUsers.setWeLinkLoginname(resultSet.getString(2));
                linkUsers.setWeLinkPwd(resultSet.getString(3));
                linkUsers.setOaUserName(resultSet.getString(4));
                linkUsers.setOaPhone(resultSet.getString(5));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
                pst.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return linkUsers;
    }

    @Override
    public int updateWebLinkUsers(WeLinkUsers users) {
        int flag = -1;
        Connection connection = null;
        PreparedStatement pst = null;
        String updateSql = "update welink_users set welink_loginname=?,welink_pwd=?,oa_phone=?,oa_user_name=? where  oa_user_id=?";
        try {
            connection = JDBCAgent.getRawConnection();
            pst = connection.prepareStatement(updateSql);
            pst.setString(1, users.getWeLinkLoginname());
            pst.setString(2, users.getWeLinkPwd());
            pst.setString(3, users.getOaPhone());
            pst.setString(4, users.getOaUserName());
            pst.setString(5, users.getOaUserId());
            flag = pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pst.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    @Override
    public int insertWlAndOaMapper(WeLinkOaMapper weLinkOaMapper) {
        Connection connection = null;
        PreparedStatement pst = null;
        int flag = -1;
        try {
            if (null != weLinkOaMapper) {
                String insertSql = "insert into welink_oa_mapper(oa_meeting_id,welink_meeting_id) values(?,?)";
                connection = JDBCAgent.getRawConnection();
                pst = connection.prepareStatement(insertSql);
                pst.setString(1, weLinkOaMapper.getOaMeetingId());
                pst.setString(2, weLinkOaMapper.getWelinkMeetingId());
                flag = pst.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pst.close();
                connection.close();
            } catch (SQLException e) {
                System.out.println("关闭jdbc链接出错了！");
            }
        }
        return flag;
    }

    @Override
    public WeLinkOaMapper selectByOaMeetingId(String oaMeetingId) {
        Connection connection = null;
        PreparedStatement pst = null;
        ResultSet resultSet = null;
        WeLinkOaMapper linkOaMapper = null;
        String selectSql = "select oa_meeting_id,welink_meeting_id from welink_oa_mapper where oa_meeting_id=? and rownum=1";
        try {
            connection = JDBCAgent.getRawConnection();
            pst = connection.prepareStatement(selectSql);
            pst.setString(1, oaMeetingId);
            resultSet = pst.executeQuery();
            while (resultSet.next()) {
                linkOaMapper=new WeLinkOaMapper();
                linkOaMapper.setOaMeetingId(resultSet.getString(1));
                linkOaMapper.setWelinkMeetingId(resultSet.getString(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
                pst.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return linkOaMapper;
    }
}