package com.seeyon.apps.ext.edocDetail.controller;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.apps.ext.edocDetail.po.JdbcEntity;
import com.seeyon.apps.ext.edocDetail.po.Opinion;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import com.seeyon.ctp.util.JDBCAgent;
import org.opensaml.xml.signature.J;
import org.springframework.web.servlet.ModelAndView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class edocDetailController extends BaseController {

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("apps/ext/edocDetail/index");
        String summaryId = request.getParameter("id");
        String sql = "select s.id,s.send_unit,s.subject,s.doc_mark,e.avarchar1,s.create_time from EDOC_SUMMARY s,EDOC_SUMMARY_EXTEND e where s.id=e.SUMMARY_ID and s.id ='" + summaryId + "'";
        Connection connection = JDBCAgent.getRawConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            JdbcEntity entity = null;
            while (rs.next()) {
                entity = new JdbcEntity();
                entity.setCode(rs.getString("avarchar1"));
                entity.setSubject(rs.getString("subject"));
                entity.setEdocMark(rs.getString("doc_mark"));
                entity.setCreateUnit(rs.getString("send_unit"));
                entity.setTime(rs.getString("create_time").substring(0, 10));

            }
            modelAndView.addObject("entity", entity);

            //拟办、批示、办理意见
            String ideaSql = " select id,content,create_time,(select name from ORG_MEMBER where id=CREATE_USER_ID) username,policy , " +
                    " ( select wmsys.wm_concat(FILENAME) from CTP_ATTACHMENT where sub_REFERENCE=o.id) filename from  EDOC_OPINION o where policy <>'转送' and policy <> '来文编号'  and  edoc_id ='" + summaryId + "'  order by create_time asc";
            ps = connection.prepareStatement(ideaSql);
            rs = ps.executeQuery();
            List<Opinion> nibanList = new ArrayList<>();
            List<Opinion> pishiList = new ArrayList<>();
            List<Opinion> banliList = new ArrayList<>();
            while (rs.next()) {
                Opinion opinion = new Opinion();
                opinion.setId(rs.getString("id"));
                opinion.setContent(rs.getString("content"));
                opinion.setCreateTime(rs.getString("create_time"));
                opinion.setUsername(rs.getString("username"));
                opinion.setPolicy(rs.getString("policy"));
                opinion.setFilename(rs.getString("filename"));
                String policy = rs.getString("policy");
                if ("拟办".equals(policy)) {
                    nibanList.add(opinion);
                }
                if ("批示".equals(policy)) {
                    pishiList.add(opinion);
                }
                if ("办理".equals(policy)) {
                    banliList.add(opinion);
                }
            }

            modelAndView.addObject("niban", nibanList);
            modelAndView.addObject("pishi", pishiList);
            modelAndView.addObject("banli", banliList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != rs) {
                rs.close();
            }
            if (null != ps) {
                ps.close();
            }
            if (null != connection) {
                connection.close();
            }
        }
        return modelAndView;
    }
}
