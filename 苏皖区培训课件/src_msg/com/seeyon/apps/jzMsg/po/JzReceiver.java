package com.seeyon.apps.jzMsg.po;

/**
 * @author Fangaowei
 * 
 *         <pre>
 *         </pre>
 * 
 * @date 2018年10月22日 上午11:13:03 @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class JzReceiver {

    private String userId;

    private String mobile;

    private String email;

    private int flag = 0;
    
    public JzReceiver() {
        
    }

    public JzReceiver(String userId) {
        this.userId = userId;
    }
    
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
