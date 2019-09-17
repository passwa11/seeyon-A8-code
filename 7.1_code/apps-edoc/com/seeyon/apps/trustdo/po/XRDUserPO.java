package com.seeyon.apps.trustdo.po;

import java.io.Serializable;

import com.seeyon.ctp.common.po.BasePO;

public class XRDUserPO extends BasePO implements Serializable {
    private Long id;

    /**
     * 协同用户名
     */
    private String seeyonUserName;

    /**
     * 协同用户部门
     */
    private String seeyonUserDept;

    /**
     * 协同用户登录名
     */
    private String seeyonLoginName;

    /**
     * 手机盾账户
     */
    private String trustdoAccount;

    /**
     * 绑定时间
     */
    private String bindTime;

    /**
     * 手机盾key标识
     */
    private String keyId;

    /**
     * 员工单位名，同步修改授权列表页
     */
    private String extend2;

    private String extend3;

    private String extend4;

    private String extend5;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSeeyonUserName() {
        return seeyonUserName;
    }

    public void setSeeyonUserName(String seeyonUserName) {
        this.seeyonUserName = seeyonUserName == null ? null : seeyonUserName.trim();
    }

    public String getSeeyonUserDept() {
        return seeyonUserDept;
    }

    public void setSeeyonUserDept(String seeyonUserDept) {
        this.seeyonUserDept = seeyonUserDept == null ? null : seeyonUserDept.trim();
    }

    public String getSeeyonLoginName() {
        return seeyonLoginName;
    }

    public void setSeeyonLoginName(String seeyonLoginName) {
        this.seeyonLoginName = seeyonLoginName == null ? null : seeyonLoginName.trim();
    }

    public String getTrustdoAccount() {
        return trustdoAccount;
    }

    public void setTrustdoAccount(String trustdoAccount) {
        this.trustdoAccount = trustdoAccount == null ? null : trustdoAccount.trim();
    }

    public String getBindTime() {
        return bindTime;
    }

    public void setBindTime(String bindTime) {
        this.bindTime = bindTime == null ? null : bindTime.trim();
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId == null ? null : keyId.trim();
    }

    public String getExtend2() {
        return extend2;
    }

    public void setExtend2(String extend2) {
        this.extend2 = extend2 == null ? null : extend2.trim();
    }

    public String getExtend3() {
        return extend3;
    }

    public void setExtend3(String extend3) {
        this.extend3 = extend3 == null ? null : extend3.trim();
    }

    public String getExtend4() {
        return extend4;
    }

    public void setExtend4(String extend4) {
        this.extend4 = extend4 == null ? null : extend4.trim();
    }

    public String getExtend5() {
        return extend5;
    }

    public void setExtend5(String extend5) {
        this.extend5 = extend5 == null ? null : extend5.trim();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", seeyonUserName=").append(seeyonUserName);
        sb.append(", seeyonUserDept=").append(seeyonUserDept);
        sb.append(", seeyonLoginName=").append(seeyonLoginName);
        sb.append(", trustdoAccount=").append(trustdoAccount);
        sb.append(", bindTime=").append(bindTime);
        sb.append(", keyId=").append(keyId);
        sb.append(", extend2=").append(extend2);
        sb.append(", extend3=").append(extend3);
        sb.append(", extend4=").append(extend4);
        sb.append(", extend5=").append(extend5);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}