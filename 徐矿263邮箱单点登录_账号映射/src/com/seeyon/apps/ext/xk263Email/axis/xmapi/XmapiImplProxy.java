package com.seeyon.apps.ext.xk263Email.axis.xmapi;

public class XmapiImplProxy implements XmapiImpl {
  private String _endpoint = null;
  private XmapiImpl xmapiImpl = null;
  
  public XmapiImplProxy() {
    _initXmapiImplProxy();
  }
  
  public XmapiImplProxy(String endpoint) {
    _endpoint = endpoint;
    _initXmapiImplProxy();
  }
  
  private void _initXmapiImplProxy() {
    try {
      xmapiImpl = (new XmapiImplServiceLocator()).getxmapi();
      if (xmapiImpl != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)xmapiImpl)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)xmapiImpl)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (xmapiImpl != null)
      ((javax.xml.rpc.Stub)xmapiImpl)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public XmapiImpl getXmapiImpl() {
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl;
  }
  
  public void main(String[] args) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    xmapiImpl.main(args);
  }

  public String getDomainDisGroupUser(String domain, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.getDomainDisGroupUser(domain, account, sign);
  }

  public void deleteXmailuserExpired(String[] userid, int status, String operator) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    xmapiImpl.deleteXmailuserExpired(userid, status, operator);
  }

  public int updateDepartment(String userid, String domain, int departmentid, String departmentName, int deptId, String description, String listName, String listSync, String mobile, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.updateDepartment(userid, domain, departmentid, departmentName, deptId, description, listName, listSync, mobile, account, sign);
  }

  public int createDepartment(String userid, String domain, String departmentName, int deptId, String description, String listName, String listSync, String mobile, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.createDepartment(userid, domain, departmentName, deptId, description, listName, listSync, mobile, account, sign);
  }

  public int createDisGroupUser(String userid, String domain, int departmentId, String office, String mobile, String phone, String fax, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.createDisGroupUser(userid, domain, departmentId, office, mobile, phone, fax, account, sign);
  }

  public String getDepartment(String userid, String domain, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.getDepartment(userid, domain, account, sign);
  }

  public int regUser(String userid, String domain, String passwd, int crypttype, int gid, int departmentid, String username, String offic, String mobile, String phone, String fax) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.regUser(userid, domain, passwd, crypttype, gid, departmentid, username, offic, mobile, phone, fax);
  }

  public int regUser_New(String userid, String domain, String passwd, int crypttype, int gid, int departmentid, String username, String offic, String mobile, String phone, String fax, String alias, String alias2, int roleId, int changepwd, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.regUser_New(userid, domain, passwd, crypttype, gid, departmentid, username, offic, mobile, phone, fax, alias, alias2, roleId, changepwd, account, sign);
  }

  public int regUser_Multi(String userid, String domain, String passwd, int crypttype, int gid, String departmentid, String username, String offic, String mobile, String phone, String fax, String alias, String alias2, int roleId, int changepwd, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.regUser_Multi(userid, domain, passwd, crypttype, gid, departmentid, username, offic, mobile, phone, fax, alias, alias2, roleId, changepwd, account, sign);
  }

  public int delUser(String userid, String domain) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.delUser(userid, domain);
  }

  public int delUser_New(String userid, String domain, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.delUser_New(userid, domain, account, sign);
  }

  public int modPasswd(String userid, String domain, String passwd, int crypttype) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.modPasswd(userid, domain, passwd, crypttype);
  }

  public int modPasswd_New(String userid, String domain, String passwd, int crypttype, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.modPasswd_New(userid, domain, passwd, crypttype, account, sign);
  }

  public String getDirInfo(String userid, String domain, String passwd, int crypttype) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.getDirInfo(userid, domain, passwd, crypttype);
  }

  public String getDirInfo_New(String userid, String domain, String passwd, int crypttype, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.getDirInfo_New(userid, domain, passwd, crypttype, account, sign);
  }

  public String aspGetDomainUserlist(String domain) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.aspGetDomainUserlist(domain);
  }

  public int modGid(String userid, String domain, int gid) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.modGid(userid, domain, gid);
  }

  public int modGid_New(String userid, String domain, int gid, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.modGid_New(userid, domain, gid, account, sign);
  }

  public int authentication(String userid, String domain, String passwd) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.authentication(userid, domain, passwd);
  }

  public int authentication_New(String userid, String domain, String passwd, int crypttype, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.authentication_New(userid, domain, passwd, crypttype, account, sign);
  }

  public int authenticationManager_New(String userid, String domain, String passwd, int crypttype, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.authenticationManager_New(userid, domain, passwd, crypttype, account, sign);
  }

  public int modStatus(String userid, String domain, int status) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.modStatus(userid, domain, status);
  }

  public int modStatus_New(String userid, String domain, int status, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.modStatus_New(userid, domain, status, account, sign);
  }

  public int modUserInfo(String userid, String domain, int departmentid, String username, String office, String mobile, String phone, String fax) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.modUserInfo(userid, domain, departmentid, username, office, mobile, phone, fax);
  }

  public int modUserInfo_New(String userid, String domain, int departmentid, String username, String office, String mobile, String phone, String fax, String alias, String alias2, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.modUserInfo_New(userid, domain, departmentid, username, office, mobile, phone, fax, alias, alias2, account, sign);
  }

  public int modUserInfo_Multi(String userid, String domain, String departmentid, String username, String office, String mobile, String phone, String fax, String alias, String alias2, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.modUserInfo_Multi(userid, domain, departmentid, username, office, mobile, phone, fax, alias, alias2, account, sign);
  }

  public int deleteDepartment(String domain, String departmentId, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.deleteDepartment(domain, departmentId, account, sign);
  }

  public int createDisGroup(String userid, String domain, String disGroupName, String description, int parent, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.createDisGroup(userid, domain, disGroupName, description, parent, account, sign);
  }

  public int deleteDisGroupUser(int deptId, String userid, String domain, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.deleteDisGroupUser(deptId, userid, domain, account, sign);
  }

  public int createDisGroupUser_New(String userid, String domain, int departmentId, String username, String office, String mobile, String phone, String fax, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.createDisGroupUser_New(userid, domain, departmentId, username, office, mobile, phone, fax, account, sign);
  }

  public int updateDisGroup(String userid, String domain, int disGroupId, String disGroupName, String description, int parent, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.updateDisGroup(userid, domain, disGroupId, disGroupName, description, parent, account, sign);
  }

  public int deleteDisGroup(String userid, String domain, int departmentId, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.deleteDisGroup(userid, domain, departmentId, account, sign);
  }

  public String getDisGroup(String userid, String domain, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.getDisGroup(userid, domain, account, sign);
  }

  public String getDomainUserlist(String domain) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.getDomainUserlist(domain);
  }

  public String getDomainUserlist_New(String domain, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.getDomainUserlist_New(domain, account, sign);
  }

  public String getDomainUserlistAndAlias_New(String domain, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.getDomainUserlistAndAlias_New(domain, account, sign);
  }

  public String getDomainUserlistByStatus(String domain, int email_status, int em_status, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.getDomainUserlistByStatus(domain, email_status, em_status, account, sign);
  }

  public byte[] getDomainUserlistGsoap(String domain, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.getDomainUserlistGsoap(domain, account, sign);
  }

  public int getDomainUserlistLengthGsoap(String domain, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.getDomainUserlistLengthGsoap(domain, account, sign);
  }

  public String getSaasDomainUserlist(String domain, String userid, String passwd) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.getSaasDomainUserlist(domain, userid, passwd);
  }

  public String getUserInfo(String userid, String domain) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.getUserInfo(userid, domain);
  }

  public String getUserInfo_New(String userid, String domain, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.getUserInfo_New(userid, domain, account, sign);
  }

  public String getDisGroupUser(int deptId, String domain, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.getDisGroupUser(deptId, domain, account, sign);
  }

  public String getAllXmaillist(String domain, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.getAllXmaillist(domain, account, sign);
  }

  public String getXmaillistByName(String name, String domain, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.getXmaillistByName(name, domain, account, sign);
  }

  public String regXmailList(String name, String description, String members, String domain, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.regXmailList(name, description, members, domain, account, sign);
  }

  public String deleteXmailListByName(String name, String domain, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.deleteXmailListByName(name, domain, account, sign);
  }

  public String updateXmailListByName(String oldname, String newname, String description, String members, String domain, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.updateXmailListByName(oldname, newname, description, members, domain, account, sign);
  }

  public int modUserStatus_New(String userid, String domain, int status, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.modUserStatus_New(userid, domain, status, account, sign);
  }

  public int modDomainCos_New(String domain, int cos, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.modDomainCos_New(domain, cos, account, sign);
  }

  public String getDomainInfo_new(String domainname, String account, String sign) throws java.rmi.RemoteException{
    if (xmapiImpl == null)
      _initXmapiImplProxy();
    return xmapiImpl.getDomainInfo_new(domainname, account, sign);
  }
  
  
}