package com.seeyon.apps.ext.pulldata.listener;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.seeyon.apps.collaboration.event.CollaborationAffairsAssignedEvent;
import com.seeyon.apps.collaboration.event.CollaborationStartEvent;
import com.seeyon.apps.ext.pulldata.event.EdocCancelEvent;
import com.seeyon.apps.ext.pulldata.event.EdocExchangeSendEvent;
import com.seeyon.common.GetFwTokenUtil;
import com.seeyon.common.HttpClient;
import com.seeyon.common.JDBCUtil;
import com.seeyon.common.ProptiesUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.filemanager.manager.FileManagerImpl;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.ctp.util.annotation.ListenEvent;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.exception.EdocException;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.manager.EdocManagerImpl;
import net.sf.json.JSONArray;

import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EdocExchangeSendListener {

    private FileManager fileManager = new FileManagerImpl();
    private EdocManager edocManager = (EdocManager) AppContext.getBean("edocManager");

    /**
     * 流程发起监听事件
     */
    @ListenEvent(event = CollaborationStartEvent.class, async = true)
    public void start(CollaborationStartEvent event) {
    }

    /**
     * 下一节点处理信息
     *
     * @param event
     */
    @ListenEvent(event = CollaborationAffairsAssignedEvent.class, async = true)
    public void assigned(CollaborationAffairsAssignedEvent event) throws UnsupportedEncodingException, EdocException {
        CtpAffair currentAffair = event.getCurrentAffair();
        Long summaryId = event.getSummaryId();
        Long affairId = currentAffair.getId();
        ProptiesUtil prop = new ProptiesUtil();
        Map<String, String> map = new HashMap<>();
        map.put("summaryid", summaryId + "");
        map.put("affairid", affairId + "");
        String data = JSON.toJSONString(map, SerializerFeature.WriteMapNullValue);
        String param = URLEncoder.encode(data, "UTF-8");
        String url = prop.getServerUrl() + "/api/integration/wstest?param=" + param;

        Map<String, Object> objectMap = GetFwTokenUtil.testRegist(prop.getServerUrl());
        String token = GetFwTokenUtil.testGetoken(objectMap);
        String appId = prop.getAppId();
        String spk = StrUtil.nullToEmpty((String) objectMap.get("spk"));
        RSA rsa = new RSA(null, spk);
        String userId = rsa.encryptBase64(prop.getSendUserId(), CharsetUtil.CHARSET_UTF_8, KeyType.PublicKey);
        Map<String, String> headers = new HashMap<>();
        headers.put("appid", appId);
        headers.put("token", token);
        headers.put("userid", userId);
        headers.put("Content-Type", "application/json; charset=utf-8");
        String back1 = HttpClient.httpGet(url, headers, "utf-8");
        System.out.println(back1);
    }

    /**
     * 快速发文撤销，调用泛微撤销接口
     *
     * @param event
     */
    @ListenEvent(event = EdocCancelEvent.class, async = true)
    public void cancel(EdocCancelEvent event) {
        ProptiesUtil prop = new ProptiesUtil();

        List<CtpAffair> affairs = event.getList();
        for (int i = 0; i < affairs.size(); i++) {
            CtpAffair ctpAffair = affairs.get(i);
            String memberId = ctpAffair.getMemberId() + "";
            if (memberId.equals(prop.getOaPendingMemberId())) {
                Long summaryId = event.getSummaryId();
                String sql = "select summary_id,fw_id from temp_fw_requrid_id where summary_id=" + summaryId + "";
                List<Map<String, Object>> list = JDBCUtil.doQuery(sql);
                String delUrl = prop.getServerUrl() + "/api/workflow/paService/deleteRequest";
                Map<String, String> map = new HashMap<>();
                map.put("requestId", (String) list.get(0).get("fw_id"));
                map.put("otherParams", "{\"ismonitor\":\"1\"}");

                Map<String, Object> objectMap = GetFwTokenUtil.testRegist(prop.getServerUrl());
                String token = GetFwTokenUtil.testGetoken(objectMap);
                String appId = prop.getAppId();
                String spk = StrUtil.nullToEmpty((String) objectMap.get("spk"));
                RSA rsa = new RSA(null, spk);
                String userId = rsa.encryptBase64(prop.getSendUserId(), CharsetUtil.CHARSET_UTF_8, KeyType.PublicKey);
                Map<String, String> headers = new HashMap<>();
                headers.put("appid", appId);
                headers.put("token", token);
                headers.put("userid", userId);
                headers.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                String back1 = HttpClient.httpPostForm(delUrl, map, headers, "utf-8");
                System.out.println(back1);
            }
        }
    }

    @ListenEvent(event = EdocExchangeSendEvent.class, async = true)
    public void send(EdocExchangeSendEvent event) throws BusinessException, FileNotFoundException, UnsupportedEncodingException {
        ProptiesUtil pUtil = new ProptiesUtil();
        Long summaryId = event.getSummaryId();
        EdocSummary edocSummary = event.getEdocSummary();
        String sendToId = edocSummary.getSendToId();
        String[] ids = pUtil.getOaTeamUnitId().split(",");
        if (sendToId.contains(ids[0]) || sendToId.contains(ids[1]) || sendToId.contains(ids[2])) {
            String sql = "select id,reference,filename,file_url,mime_type,attachment_size,createdate from CTP_ATTACHMENT where reference =" + summaryId;
            List<Map<String, Object>> list = JDBCUtil.doQuery(sql);
            List<String> pathList = new ArrayList<>();

            List<Map<String, Object>> fjList = new ArrayList<>();
            if (list.size() > 0) {
//                String spath = fileManager.getFolder(new Date(), false);
//                System.out.println(spath);
//                String p = spath.substring(0, spath.indexOf("upload") + 6);
//                Date createdate = (Date) list.get(0).get("createdate");
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                String date = sdf.format(createdate);
//                String rq = date.split(" ")[0];
//                String[] _arr = rq.split("-");
//                p += File.separator + _arr[0] + File.separator + _arr[1] + File.separator + _arr[2];
//                String path = "";
                Map<String, Object> fjMap = null;
                String fileDownpath = "/seeyon/rest/attachment/file/";
                ProptiesUtil prop = new ProptiesUtil();
                String token = GetFwTokenUtil.getOaToken();
                for (int i = 0; i < list.size(); i++) {
                    BigDecimal bigDecimal = (BigDecimal) list.get(i).get("file_url");
                    fjMap = new HashMap<>();
                    String h = (String) list.get(i).get("filename");
                    String fileName = bigDecimal.longValue() + "" + h.substring(h.lastIndexOf("."));
                    String downloadUrl = prop.getOaUrl() + fileDownpath + bigDecimal.longValue() + "?fileName=" + fileName + "&token=" + token;
//                fjMap.put("filePath", "base64:" + Base64.getEncoder().encodeToString(downloadUrl.getBytes("utf-8")));
                    fjMap.put("filePath", downloadUrl);
                    fjMap.put("fileName", list.get(i).get("filename"));
                    fjList.add(fjMap);
                }
            }

            List<Map<String, Object>> mapList = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            map.put("fieldName", "wjbt");
            map.put("fieldValue", edocSummary.getSubject());
            mapList.add(map);
            map = new HashMap<>();
            map.put("fieldName", "rq");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String startDate = sdf.format(edocSummary.getStartTime());
            map.put("fieldValue", startDate);
            mapList.add(map);
            map = new HashMap<>();
            map.put("fieldName", "lwdw1");
            map.put("fieldValue", edocSummary.getSendDepartment());
            mapList.add(map);
            map = new HashMap<>();
            map.put("fieldName", "lwh");
            map.put("fieldValue", !"".equals(edocSummary.getDocMark()) && null != edocSummary.getDocMark() ? edocSummary.getDocMark() : "");
            mapList.add(map);
            //附件问题
            Map<String, Object> fj = new HashMap<>();
            fj.put("fieldName", "wjzw");
            fj.put("fieldValue", fjList);
            mapList.add(fj);

            Map<String, String> param = new HashMap<>();
            param.put("requestName", "集团发文");
            param.put("workflowId", pUtil.getWorkflowId());
            param.put("mainData", JSONArray.fromObject(mapList).toString());

//        Map<String, Object> otherParams = new HashMap<>();
//        otherParams.put("isnextflow ", "1");
//        otherParams.put("delReqFlowFaild ", "1");
//
//        param.put("otherParams", otherParams.toString());


            String address = pUtil.getServerUrl();
            Map<String, Object> objectMap = GetFwTokenUtil.testRegist(address);
            String token = GetFwTokenUtil.testGetoken(objectMap);
            String appId = pUtil.getAppId();
            String spk = StrUtil.nullToEmpty((String) objectMap.get("spk"));
            RSA rsa = new RSA(null, spk);
            String userId = rsa.encryptBase64(pUtil.getSendUserId(), CharsetUtil.CHARSET_UTF_8, KeyType.PublicKey);
            Map<String, String> headers = new HashMap<>();
            headers.put("appid", appId);
            headers.put("token", token);
            headers.put("userid", userId);
            headers.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            String url = address + pUtil.getDocreate();
            String back1 = HttpClient.httpPostForm(url, param, headers, "utf-8");
            System.out.println("返回结果：" + back1);
            JSONObject object = JSONObject.parseObject(back1);
            Map data = (Map) object.get("data");
            String requestid = data.get("requestid").toString();
            System.out.println(requestid);
            Connection connection = null;
            PreparedStatement ps = null;
            String insertsql = "insert into temp_fw_requrid_id(summary_id,fw_id) values(?,?)";
            try {
                connection = JDBCAgent.getRawConnection();
                ps = connection.prepareStatement(insertsql);
                ps.setString(1, summaryId + "");
                ps.setString(2, requestid);
                ps.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closeUtil(connection, ps, null);
            }
        }
    }

    public void closeUtil(Connection connection, PreparedStatement ps, ResultSet rs) {
        try {
            if (null != ps) {
                ps.close();
            }
            if (null != rs) {
                rs.close();
            }
            if (null != connection) {
                connection.close();
            }
        } catch (SQLException s) {
            s.printStackTrace();
        }
    }


    public FileManager getFileManager() {
        return fileManager;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public EdocManager getEdocManager() {
        return edocManager;
    }
}
