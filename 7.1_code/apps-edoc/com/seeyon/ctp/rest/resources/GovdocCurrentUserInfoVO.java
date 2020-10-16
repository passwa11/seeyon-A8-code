/**
 * Author : xuqw
 *   Date : 2015年12月20日 下午8:00:35
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.rest.resources;

import com.seeyon.ctp.common.authenticate.domain.User;

/**
 * <p>Title       : 应用模块名称</p>
 * <p>Description : 代码描述</p>
 * <p>Copyright   : Copyright (c) 2012</p>
 * <p>Company     : seeyon.com</p>
 */
public class GovdocCurrentUserInfoVO {

    private Long id = null;
    private String name = null;
    private Long accountId = null;
    private Long loginAccount = null;
    private String loginAccountName = null;
    private Long departmentId = null;
    
    
    public static GovdocCurrentUserInfoVO valueOf(User user){
        
        GovdocCurrentUserInfoVO v = new GovdocCurrentUserInfoVO();
        
        v.setId(user.getId());
        v.setName(user.getName());
        v.setAccountId(user.getAccountId());
        v.setDepartmentId(user.getDepartmentId());
        v.setLoginAccount(user.getLoginAccount());
        v.setLoginAccountName(user.getLoginAccountName());
        
        return v;
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public Long getAccountId() {
        return accountId;
    }


    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }


    public Long getLoginAccount() {
        return loginAccount;
    }


    public void setLoginAccount(Long loginAccount) {
        this.loginAccount = loginAccount;
    }

    public Long getDepartmentId() {
        return departmentId;
    }


    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }


    public String getLoginAccountName() {
        return loginAccountName;
    }


    public void setLoginAccountName(String loginAccountName) {
        this.loginAccountName = loginAccountName;
    }
    
    
}
