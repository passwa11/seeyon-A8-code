package com.seeyon.apps.ext.zs.listener;

import com.seeyon.apps.collaboration.event.CollaborationAffairsAssignedEvent;
import com.seeyon.apps.collaboration.event.CollaborationFinishEvent;
import com.seeyon.apps.collaboration.event.CollaborationStopEvent;
import com.seeyon.apps.ext.zs.manager.ZsTempFormCorrelationManager;
import com.seeyon.apps.ext.zs.manager.ZsTempFormCorrelationManagerImpl;
import com.seeyon.apps.ext.zs.po.ZsTempFormCorrelation;
import com.seeyon.apps.ext.zs.util.JDBCUtil;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.ctp.util.annotation.ListenEvent;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollaborationZsListener {

    @ListenEvent(event = CollaborationFinishEvent.class, async = true)
    public void finish(CollaborationFinishEvent event) {
        CtpAffair affair = event.getAffair();
        String sql = "select a.COMPLETE_TIME,(select p.login_name from ORG_MEMBER m,ORG_PRINCIPAL p where m.id=p.member_id and m.id= a.MEMBER_ID) loginName,(select name from ORG_MEMBER where id= a.MEMBER_ID) memberName,(select OU.name from ORG_UNIT ou,ORG_MEMBER om where OM.id=a.MEMBER_ID and OM.ORG_DEPARTMENT_ID=OU.id) deptName,c.content,case c.ext_att4 when 'disagree' then '不同意' when 'agree' then '同意' when 'haveRead' then '已阅' else '' end result  " +
                " from CTP_COMMENT_ALL c,CTP_AFFAIR a where  c.AFFAIR_ID=a.id and c.AFFAIR_ID=" + affair.getId() + " order by a.COMPLETE_TIME asc";
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = JDBCAgent.getRawConnection();
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            toPushInfo(rs, affair.getObjectId().longValue());
        } catch (Exception e) {

        }finally {
            try {
                if (null != rs) {
                    rs.close();
                }
                if (null != ps) {
                    ps.close();
                }
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 下一节点处理信息
     *
     * @param event
     * @throws BusinessException
     */
    @ListenEvent(event = CollaborationAffairsAssignedEvent.class, async = true)
    public void assigned(CollaborationAffairsAssignedEvent event) throws BusinessException {
        CtpAffair currentAffair = event.getCurrentAffair();
        String sql = "select a.COMPLETE_TIME,(select name from ORG_MEMBER where id= a.MEMBER_ID) memberName,(select OU.name from ORG_UNIT ou,ORG_MEMBER om where OM.id=a.MEMBER_ID and OM.ORG_DEPARTMENT_ID=OU.id) deptName,c.content,case c.ext_att4 when 'disagree' then '不同意' when 'agree' then '同意' when 'haveRead' then '已阅' else '' end result  " +
                " from CTP_COMMENT_ALL c,CTP_AFFAIR a where  c.AFFAIR_ID=a.id and c.AFFAIR_ID=" + currentAffair.getId() + " order by a.COMPLETE_TIME asc";

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = JDBCAgent.getRawConnection();
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            boolean flag = false;
            while (rs.next()) {
                String result = rs.getString("result");
                if ("不同意".equals(result)) {
                    flag = true;
                }
            }

            if (flag) {
                toPushInfo(rs, currentAffair.getObjectId().longValue());
            }
        } catch (Exception e) {
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
                if (null != ps) {
                    ps.close();
                }
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void toPushInfo(ResultSet rs, long summaryId) throws SQLException {
        String url = "http://192.168.1.88:8088/SrmCommSrv.asmx/upVender";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        List<NameValuePair> pairs = new ArrayList<>();

        ZsTempFormCorrelationManager zsManager = new ZsTempFormCorrelationManagerImpl();
        Map<String, Object> map = new HashMap<>();
        map.put("oaSummaryId", summaryId + "");
        List<ZsTempFormCorrelation> list = zsManager.getFormInfoBySummaryId(map);

        while (rs.next()) {
            pairs.add(new BasicNameValuePair("FormID", summaryId + ""));
            pairs.add(new BasicNameValuePair("NodeID", "1"));
            pairs.add(new BasicNameValuePair("Statue", rs.getString("result")));
            pairs.add(new BasicNameValuePair("User", rs.getString("loginName")));
            pairs.add(new BasicNameValuePair("UserName", rs.getString("memberName")));
            pairs.add(new BasicNameValuePair("Approval", rs.getString("content")));
            pairs.add(new BasicNameValuePair("SRMID", list.get(0).getThirdId()));
        }

        UrlEncodedFormEntity encodedFormEntity = null;
        try {
            encodedFormEntity = new UrlEncodedFormEntity(pairs, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        post.setEntity(encodedFormEntity);
        HttpResponse response = null;
        try {
            response = client.execute(post);
            response.setHeader("Cache-Control", "no-cache");
            int code = response.getStatusLine().getStatusCode();
            System.out.println(code);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(response.getEntity(), "utf-8");
                System.out.println(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @ListenEvent(event = CollaborationStopEvent.class, async = true)
    public void stop(CollaborationStopEvent event) {
        Long summaryId = event.getSummaryId();
        CtpAffair currentAffair = event.getAffair();
        String sql = "select a.COMPLETE_TIME,(select name from ORG_MEMBER where id= a.MEMBER_ID) memberName,(select OU.name from ORG_UNIT ou,ORG_MEMBER om where OM.id=a.MEMBER_ID and OM.ORG_DEPARTMENT_ID=OU.id) deptName,c.content,case c.ext_att4 when 'disagree' then '不同意' when 'agree' then '同意' when 'haveRead' then '已阅' else '' end result  " +
                " from CTP_COMMENT_ALL c,CTP_AFFAIR a where  c.AFFAIR_ID=a.id and c.AFFAIR_ID=" + currentAffair.getId() + " order by a.COMPLETE_TIME asc";

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = JDBCAgent.getRawConnection();
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            boolean flag = false;
            while (rs.next()) {
                String result = rs.getString("result");
                if ("不同意".equals(result)) {
                    flag = true;
                }
            }

            if (flag) {
                toPushInfo(rs, currentAffair.getObjectId().longValue());
            }
        } catch (Exception e) {
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
                if (null != ps) {
                    ps.close();
                }
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }


}
