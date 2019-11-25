package com.seeyon.apps.ext.fileUploadConfig.po;


/**
 * 周刘成   2019-11-21
 */
public class ZorgMember {

    private String id;
    private String name;
    private String orgLevelId;
    private String levelName;
    private String loginName;

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrgLevelId() {
        return orgLevelId;
    }

    public void setOrgLevelId(String orgLevelId) {
        this.orgLevelId = orgLevelId;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }
}
