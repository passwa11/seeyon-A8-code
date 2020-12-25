package com.seeyon.common;

import java.io.*;
import java.util.Properties;

public class ProptiesUtil {

    private Properties properties;

    private String appId;
    private String serverUrl;
    private String docreate;
    private String oaUrl;
    private String oaPendingMemberId;
    private String oaTeamUnitId;
    private String workflowId;
    private String sendUserId;
    private String restUsername;
    private String restPassword;
    private String sendLoginName;

    public ProptiesUtil() {
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        File file = new File(path, "config/appid.properties");
        InputStream is = null;
        try {
            properties = new Properties();
            is = new FileInputStream(file);
            properties.load(new InputStreamReader(is, "UTF-8"));
            appId = properties.getProperty("fw.appid");
            serverUrl = properties.getProperty("fw.server.url");
            docreate = properties.getProperty("fw.api.doCreateRequest");
            oaUrl = properties.getProperty("oa.server.url");
            oaPendingMemberId = properties.getProperty("oa.pending.memberid");
            oaTeamUnitId = properties.getProperty("oa.team.unit.id");
            workflowId=properties.getProperty("fw.workflowId");
            sendUserId=properties.getProperty("fw.sendUserId");
            restUsername=properties.getProperty("oa.rest.username");
            restPassword = properties.getProperty("oa.rest.password");
            sendLoginName= properties.getProperty("oa.sendEdoc.loginName");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getRestUsername() {
        return restUsername;
    }

    public void setRestUsername(String restUsername) {
        this.restUsername = restUsername;
    }

    public String getRestPassword() {
        return restPassword;
    }

    public void setRestPassword(String restPassword) {
        this.restPassword = restPassword;
    }

    public String getSendLoginName() {
        return sendLoginName;
    }

    public void setSendLoginName(String sendLoginName) {
        this.sendLoginName = sendLoginName;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getSendUserId() {
        return sendUserId;
    }

    public void setSendUserId(String sendUserId) {
        this.sendUserId = sendUserId;
    }

    public String getOaTeamUnitId() {
        return oaTeamUnitId;
    }

    public void setOaTeamUnitId(String oaTeamUnitId) {
        this.oaTeamUnitId = oaTeamUnitId;
    }

    public String getOaPendingMemberId() {
        return oaPendingMemberId;
    }

    public void setOaPendingMemberId(String oaPendingMemberId) {
        this.oaPendingMemberId = oaPendingMemberId;
    }

    public String getOaUrl() {
        return oaUrl;
    }

    public void setOaUrl(String oaUrl) {
        this.oaUrl = oaUrl;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getDocreate() {
        return docreate;
    }

    public void setDocreate(String docreate) {
        this.docreate = docreate;
    }
}
