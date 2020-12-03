package com.seeyon.ctp.common.barCode.vo;

import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;

/**
 * 二维码生成对象返回结果
 * Created by daiy on 2016-1-12.
 */
public class ResultVO {

    private boolean success;

    private String msg;

    private V3XFile file;

    private Attachment attachment;

    public ResultVO() {
    }

    public ResultVO(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public ResultVO(boolean success, V3XFile file) {
        this.success = success;
        this.file = file;
    }

    public ResultVO(boolean success, Attachment attachment) {
        this.success = success;
        this.attachment = attachment;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public V3XFile getFile() {
        return file;
    }

    public void setFile(V3XFile file) {
        this.file = file;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }
}
