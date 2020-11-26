package com.seeyon.apps.collaboration.vo;

import java.io.Serializable;

import com.seeyon.ctp.common.permission.bo.Permission;

/**
 * @author muyx
 * 协同节点权限VO
 * 判定流程节点是否设置了各种权限，当前vo只是针对newCol节点处理 ，如果有其它逻辑可以自己修改
 * UploadAttachment UploadRelDoc Print Forward Cancel EditWorkFlow Pigeonhole RepeatSend
 */
public class NodePolicyVO implements Serializable{
    private static final long serialVersionUID = 4366008815053482493L;
    private String baseAction = "";

    public NodePolicyVO(Permission permission) {
        if (permission != null) {
            if (permission.getNodePolicy() != null) {
                String baseAction = permission.getNodePolicy().getBaseAction();
                if (baseAction != null) {
                    this.baseAction = baseAction;
                }
            }
        }
    }

    public boolean isUploadAttachment() {
        return baseAction.indexOf("UploadAttachment") != -1;
    }

    public boolean isUploadRelDoc() {
        return baseAction.indexOf("UploadRelDoc") != -1;
    }

    public boolean isPrint() {
        return baseAction.indexOf("Print") != -1;
    }

    public boolean isForward() {
        return baseAction.indexOf("Forward") != -1;
    }

    public boolean isCancel() {
        return baseAction.indexOf("Cancel") != -1;
    }

    public boolean isEditWorkFlow() {
        return baseAction.indexOf("EditWorkFlow") != -1;
    }

    public boolean isPigeonhole() {
        return baseAction.indexOf("Pigeonhole") != -1;
    }

    /**
     * 是否有重复发起权限
     */
    public boolean isRepeatSend() {
        return baseAction.indexOf("RepeatSend") != -1;
    }
    /**
     * 是否有删除权限
     */
    public boolean isReMove() {
        return baseAction.indexOf("ReMove") != -1;
    }
}
