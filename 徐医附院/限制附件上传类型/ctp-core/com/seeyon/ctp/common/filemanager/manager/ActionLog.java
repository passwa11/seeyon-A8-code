package com.seeyon.ctp.common.filemanager.manager;

import java.util.Date;

public class ActionLog {
    private ActionEnum action;
    private Date       createDate;
    private String     desc;

    public ActionEnum getAction() {
        return action;
    }

    public void setAction(ActionEnum action) {
        this.action = action;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public enum ActionEnum {
        add, //增加
        update, //修改
        delete, //删除
    }
}