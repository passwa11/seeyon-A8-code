package com.seeyon.apps.govdoc.bo;

import java.util.Date;

import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.util.Strings;

public class GovdocLockObject {

    /** 锁拥有者ID */
    private long   owner;

    /** 加锁时间 */
    private Date   lockTime;

    /** 登陆名称 */
    private String loginName;

    /** 登陆时间 */
    private Long   loginTimestamp;
    
    private String canSubmit;
    
    private String from;//来自那个端
    
    private String isReadOnly ="affairReadOnly";
    
    //是否真正的锁住了表单。
    private String realLockForm = "false";
    
   

    public String getRealLockForm() {
        return realLockForm;
    }

    public void setRealLockForm(String realLockForm) {
        this.realLockForm = realLockForm;
    }

    public void setFrom(String from) {
        this.from = from;
    }
    
    public String getFrom() {

        String ret = "";
        if(Strings.isNotBlank(from)){
            if(Constants.login_sign.pc.toString().equals(from)//电脑端
                    || Constants.login_sign.phone.toString().equals(from)){ //移动端
                ret = ResourceUtil.getString("collaboration.lock.from." + from);
            }
        }
        return ret;
    }

    public String getCanSubmit() {
		return canSubmit;
	}

	public void setCanSubmit(String canSubmit) {
		this.canSubmit = canSubmit;
	}

	public Long getLoginTimestamp() {
        return loginTimestamp;
    }

    public void setLoginTimestamp(Long loginTimestamp) {
        this.loginTimestamp = loginTimestamp;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public long getOwner() {
        return this.owner;
    }

    public void setOwner(long owner) {
        this.owner = owner;
    }

    public Date getLockTime() {
        return this.lockTime;
    }

    public void setLockTime(Date lockTime) {
        this.lockTime = lockTime;
    }

	public String getIsReadOnly() {
		return isReadOnly;
	}

	public void setIsReadOnly(String isReadOnly) {
		this.isReadOnly = isReadOnly;
	}
}
