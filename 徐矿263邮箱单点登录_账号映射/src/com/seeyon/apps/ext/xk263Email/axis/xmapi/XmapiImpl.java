/**
 * XmapiImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.seeyon.apps.ext.xk263Email.axis.xmapi;

public interface XmapiImpl extends java.rmi.Remote {
    public void main(String[] args) throws java.rmi.RemoteException;
    public String getDomainDisGroupUser(String domain, String account, String sign) throws java.rmi.RemoteException;
    public void deleteXmailuserExpired(String[] userid, int status, String operator) throws java.rmi.RemoteException;
    public int updateDepartment(String userid, String domain, int departmentid, String departmentName, int deptId, String description, String listName, String listSync, String mobile, String account, String sign) throws java.rmi.RemoteException;
    public int createDepartment(String userid, String domain, String departmentName, int deptId, String description, String listName, String listSync, String mobile, String account, String sign) throws java.rmi.RemoteException;
    public int createDisGroupUser(String userid, String domain, int departmentId, String office, String mobile, String phone, String fax, String account, String sign) throws java.rmi.RemoteException;
    public String getDepartment(String userid, String domain, String account, String sign) throws java.rmi.RemoteException;
    public int regUser(String userid, String domain, String passwd, int crypttype, int gid, int departmentid, String username, String offic, String mobile, String phone, String fax) throws java.rmi.RemoteException;
    public int regUser_New(String userid, String domain, String passwd, int crypttype, int gid, int departmentid, String username, String offic, String mobile, String phone, String fax, String alias, String alias2, int roleId, int changepwd, String account, String sign) throws java.rmi.RemoteException;
    public int regUser_Multi(String userid, String domain, String passwd, int crypttype, int gid, String departmentid, String username, String offic, String mobile, String phone, String fax, String alias, String alias2, int roleId, int changepwd, String account, String sign) throws java.rmi.RemoteException;
    public int delUser(String userid, String domain) throws java.rmi.RemoteException;
    public int delUser_New(String userid, String domain, String account, String sign) throws java.rmi.RemoteException;
    public int modPasswd(String userid, String domain, String passwd, int crypttype) throws java.rmi.RemoteException;
    public int modPasswd_New(String userid, String domain, String passwd, int crypttype, String account, String sign) throws java.rmi.RemoteException;
    public String getDirInfo(String userid, String domain, String passwd, int crypttype) throws java.rmi.RemoteException;
    public String getDirInfo_New(String userid, String domain, String passwd, int crypttype, String account, String sign) throws java.rmi.RemoteException;
    public String aspGetDomainUserlist(String domain) throws java.rmi.RemoteException;
    public int modGid(String userid, String domain, int gid) throws java.rmi.RemoteException;
    public int modGid_New(String userid, String domain, int gid, String account, String sign) throws java.rmi.RemoteException;
    public int authentication(String userid, String domain, String passwd) throws java.rmi.RemoteException;
    public int authentication_New(String userid, String domain, String passwd, int crypttype, String account, String sign) throws java.rmi.RemoteException;
    public int authenticationManager_New(String userid, String domain, String passwd, int crypttype, String account, String sign) throws java.rmi.RemoteException;
    public int modStatus(String userid, String domain, int status) throws java.rmi.RemoteException;
    public int modStatus_New(String userid, String domain, int status, String account, String sign) throws java.rmi.RemoteException;
    public int modUserInfo(String userid, String domain, int departmentid, String username, String office, String mobile, String phone, String fax) throws java.rmi.RemoteException;
    public int modUserInfo_New(String userid, String domain, int departmentid, String username, String office, String mobile, String phone, String fax, String alias, String alias2, String account, String sign) throws java.rmi.RemoteException;
    public int modUserInfo_Multi(String userid, String domain, String departmentid, String username, String office, String mobile, String phone, String fax, String alias, String alias2, String account, String sign) throws java.rmi.RemoteException;
    public int deleteDepartment(String domain, String departmentId, String account, String sign) throws java.rmi.RemoteException;
    public int createDisGroup(String userid, String domain, String disGroupName, String description, int parent, String account, String sign) throws java.rmi.RemoteException;
    public int deleteDisGroupUser(int deptId, String userid, String domain, String account, String sign) throws java.rmi.RemoteException;
    public int createDisGroupUser_New(String userid, String domain, int departmentId, String username, String office, String mobile, String phone, String fax, String account, String sign) throws java.rmi.RemoteException;
    public int updateDisGroup(String userid, String domain, int disGroupId, String disGroupName, String description, int parent, String account, String sign) throws java.rmi.RemoteException;
    public int deleteDisGroup(String userid, String domain, int departmentId, String account, String sign) throws java.rmi.RemoteException;
    public String getDisGroup(String userid, String domain, String account, String sign) throws java.rmi.RemoteException;
    public String getDomainUserlist(String domain) throws java.rmi.RemoteException;
    public String getDomainUserlist_New(String domain, String account, String sign) throws java.rmi.RemoteException;
    public String getDomainUserlistAndAlias_New(String domain, String account, String sign) throws java.rmi.RemoteException;
    public String getDomainUserlistByStatus(String domain, int email_status, int em_status, String account, String sign) throws java.rmi.RemoteException;
    public byte[] getDomainUserlistGsoap(String domain, String account, String sign) throws java.rmi.RemoteException;
    public int getDomainUserlistLengthGsoap(String domain, String account, String sign) throws java.rmi.RemoteException;
    public String getSaasDomainUserlist(String domain, String userid, String passwd) throws java.rmi.RemoteException;
    public String getUserInfo(String userid, String domain) throws java.rmi.RemoteException;
    public String getUserInfo_New(String userid, String domain, String account, String sign) throws java.rmi.RemoteException;
    public String getDisGroupUser(int deptId, String domain, String account, String sign) throws java.rmi.RemoteException;
    public String getAllXmaillist(String domain, String account, String sign) throws java.rmi.RemoteException;
    public String getXmaillistByName(String name, String domain, String account, String sign) throws java.rmi.RemoteException;
    public String regXmailList(String name, String description, String members, String domain, String account, String sign) throws java.rmi.RemoteException;
    public String deleteXmailListByName(String name, String domain, String account, String sign) throws java.rmi.RemoteException;
    public String updateXmailListByName(String oldname, String newname, String description, String members, String domain, String account, String sign) throws java.rmi.RemoteException;
    public int modUserStatus_New(String userid, String domain, int status, String account, String sign) throws java.rmi.RemoteException;
    public int modDomainCos_New(String domain, int cos, String account, String sign) throws java.rmi.RemoteException;
    public String getDomainInfo_new(String domainname, String account, String sign) throws java.rmi.RemoteException;
}
