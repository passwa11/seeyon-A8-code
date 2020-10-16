package com.seeyon.v3x.edoc.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;

import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.govdoc.helper.GovdocRoleHelper;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.MemberRole;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.privilege.manager.PrivilegeManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UniqueList;
import com.seeyon.v3x.common.web.login.CurrentUser;

public class EdocRoleHelper {
	
    private static Log LOG = CtpLogFactory.getLog(EdocRoleHelper.class);
    public static String AccountEdocAdminRoleName="AccountEdocAdmin";
/*	
 * public static String acountExchangeRoleName="account_exchange";
	public static String departmentExchangeRoleName="department_exchange";
	public static String accountEdocCreateRoleName="account_edoccreate";*/
	public static String acountExchangeRoleName="Accountexchange";
	public static String departmentExchangeRoleName="Departmentexchange";

	public static String accountGovdocRec = "AccountGovdocRec";
	public static String accountGovdocSend = "AccountGovdocSend";
	public static String departmentGovdocRec="DepartmentGovdocRec";
	public static String departmentGovdocSend="DepartmentGovdocSend";
	//public static String accountEdocCreateRoleName="account_edoccreate";  没有地方用这个变量，暂时屏蔽。公文发起权直接判断拟文资源是否存在即可。
	public static OrgManager orgManager=(OrgManager)AppContext.getBean("orgManager");
	//private static OrgManagerDirect orgManagerDirect=(OrgManagerDirect)AppContext.getBean("OrgManagerDirect");
	
	public static ConfigManager configManager=(ConfigManager)AppContext.getBean("configManager");
	/**
	 * 得到当前登陆人员所在单位的公文收发员
	 * @return
	 * @throws BusinessException
	 */
	public static List<V3xOrgMember> getAccountExchangeUsers() throws BusinessException
	{
		/*V3xOrgRole roleExchange = orgManagerDirect.getRoleByName(acountExchangeRoleName);
		return orgManagerDirect.getMemberByRole(roleExchange.getBond(), AppContext.getCurrentUser().getLoginAccount(), roleExchange.getId());
		*/
		//*
		User user=AppContext.getCurrentUser();
		V3xOrgRole exchangeRole=orgManager.getRoleByName(acountExchangeRoleName,user.getLoginAccount());
		return orgManager.getMembersByRole(user.getLoginAccount(), exchangeRole.getId());
		//*/
	}
	
	/**
	 * 得到某部门的公文收发员
	 * @return
	 * @throws BusinessException
	 */
	public static List<V3xOrgMember> getDepartmentExchangeUsers(long deptId) throws BusinessException
	{
		List<V3xOrgMember> list =  orgManager.getMembersByRole(deptId, OrgConstants.Role_NAME.Departmentexchange.name());
		return list;
	}
	
	/**
	 * 判断当前登陆人员是否为当前登录单位的公文发起的资源
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isEdocCreateRole(int edocType) throws BusinessException
	{
	    PrivilegeManager pcheck = (PrivilegeManager) AppContext.getBean("privilegeManager");
		/*if((edocType==EdocEnum.edocType.sendEdoc.ordinal() && pcheck.checkByReourceCode("F07_sendManager"))||
		   (edocType==EdocEnum.edocType.signReport.ordinal() && pcheck.checkByReourceCode("F07_signReport"))||
		   (edocType==EdocEnum.edocType.recEdoc.ordinal()&& pcheck.checkByReourceCode("F07_recRegister"))||
		   (edocType==EdocEnum.edocType.distributeEdoc.ordinal()&& pcheck.checkByReourceCode("F07_recRegister"))||
		   //G6RoleModify
		   (EdocHelper.isG6Version() && edocType==EdocEnum.edocType.distributeEdoc.ordinal()&& pcheck.checkByReourceCode("F07_recListFenfaing"))
			){
			return true;
		}*/
	    if((edocType==EdocEnum.edocType.sendEdoc.ordinal() && pcheck.checkByReourceCode("F20_newSend"))||
	 		   (edocType==EdocEnum.edocType.signReport.ordinal() && pcheck.checkByReourceCode("F20_newSend"))||
	 		   (edocType==EdocEnum.edocType.recEdoc.ordinal()&& pcheck.checkByReourceCode("F20_newDengji"))||
	 		   (edocType==EdocEnum.edocType.distributeEdoc.ordinal()&& pcheck.checkByReourceCode("F20_newDengji"))||
	 		   //G6RoleModify
	 		   (EdocHelper.isG6Version() && edocType==EdocEnum.edocType.distributeEdoc.ordinal()&& pcheck.checkByReourceCode("F20_newDengji"))
	 			) {
	 			return true;
	 		}	
		return false;
				
	}
	
	/**
	 * 判断指定人员是否有指定单位的公文归档修改资源
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isEdocArchivedModifyRole(Long accountId, Long userId, int modifyType) throws BusinessException {
	    PrivilegeManager pcheck = (PrivilegeManager) AppContext.getBean("privilegeManager");
       //(sprint3)-yangfan 
		if((modifyType==EdocEnum.ArchiveModifyType.archiveModifySendEdoc.ordinal() && pcheck.checkByReourceCode("F07_sendModArch",userId,accountId))||
		   (modifyType==EdocEnum.ArchiveModifyType.archiveModifyRecEdoc.ordinal() && pcheck.checkByReourceCode("F07_recModArch",userId,accountId))||
		   (modifyType==EdocEnum.ArchiveModifyType.archiveModifySignEdoc.ordinal() && pcheck.checkByReourceCode("F07_signModArch",userId,accountId))
     	){
			return true;
		}
		
		return false;
	}

	/**
	 * 判断指定人员是否为指定单位的公文发起的资源
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isEdocCreateRole(Long accountId, Long userId, int edocType) throws BusinessException {
		boolean isCreateRole = false;

		PrivilegeManager pcheck = (PrivilegeManager) AppContext.getBean("privilegeManager");
		// (sprint3)-yangfan
		Long groupDomainId = OrgConstants.GROUPID;

		if (edocType == EdocEnum.edocType.sendEdoc.ordinal()) {//老公文-发文拟文权限
			boolean hasNewRole = (pcheck.checkByReourceCode("F20_newSend", userId, groupDomainId) || pcheck.checkByReourceCode("F20_newSend", userId, accountId));
			isCreateRole = (pcheck.checkByReourceCode("F07_sendManager", userId, accountId) && (pcheck.checkByReourceCode("F07_sendNewEdoc", userId, groupDomainId) || pcheck.checkByReourceCode("F07_sendNewEdoc", userId, accountId))
					|| hasNewRole);
		}
		else if (edocType == EdocEnum.edocType.signReport.ordinal()) {//老公文-签报拟文权限
			boolean hasNewRole = (pcheck.checkByReourceCode("F20_newSend", userId, groupDomainId) || pcheck.checkByReourceCode("F20_newSend", userId, accountId));
			isCreateRole = (pcheck.checkByReourceCode("F07_signReport", userId, accountId) && (pcheck.checkByReourceCode("F07_signNewEdoc", userId, groupDomainId) || pcheck.checkByReourceCode("F07_signNewEdoc", userId, accountId))) 
					|| hasNewRole;
		}
		else if (edocType == EdocEnum.edocType.recEdoc.ordinal()) {//老公文-收文登记权限(收文新建)
			if (EdocHelper.isG6Version()) {
				boolean hasNewRole = (pcheck.checkByReourceCode("F20_newDengji", userId, groupDomainId) || pcheck.checkByReourceCode("F20_newDengji", userId, accountId));
				// 是否有G6登记权
				isCreateRole = (pcheck.checkByReourceCode("F07_recListRegistering", userId, groupDomainId) || pcheck.checkByReourceCode("F07_recListRegistering", userId, accountId))
						|| hasNewRole;
			} else {
				boolean hasNewRole = (pcheck.checkByReourceCode("F20_newDengji", userId, groupDomainId) || pcheck.checkByReourceCode("F20_newDengji", userId, accountId));
				isCreateRole = (pcheck.checkByReourceCode("F07_recRegister", userId, groupDomainId) || pcheck.checkByReourceCode("F07_recRegister", userId, accountId))
						|| hasNewRole;
			}
		}
		else if (edocType == EdocEnum.edocType.edocRegister.ordinal()) {//老公文-收文登记权限
//			boolean hasNewRole = (pcheck.checkByReourceCode("F20_newDengji", userId, groupDomainId) || pcheck.checkByReourceCode("F20_newDengji", userId, accountId));
//			isCreateRole = (pcheck.checkByReourceCode("F07_recRegister", userId, groupDomainId) || pcheck.checkByReourceCode("F07_recRegister", userId, accountId))
//				|| hasNewRole;
			isCreateRole = true; //老公文放开
		}
		else if (edocType == EdocEnum.edocType.distributeEdoc.ordinal()) {//老公文-收文分发权限
			boolean hasNewRole = (pcheck.checkByReourceCode("F20_newDengji", userId, groupDomainId) || pcheck.checkByReourceCode("F20_newDengji", userId, accountId));
			// 是否有G6分发权
//			isCreateRole =  (pcheck.checkByReourceCode("F07_recListFenfaing", userId, groupDomainId) || pcheck.checkByReourceCode("F07_recListFenfaing", userId, accountId))
//					|| hasNewRole;
			isCreateRole = true; //老公文放开
		}

		return isCreateRole;

	}

	/**
	 * 有从集团导入公文单功能,当前产品是集团版，并且当前用户是单位管理员，返回true
	 * @return
	 * @throws BusinessException
	 */
	public static boolean hasInputFunctionFromGroup() throws BusinessException
	{		
		User user=AppContext.getCurrentUser();
		V3xOrgAccount account = orgManager.getAccountById(user.getAccountId());
		boolean isGroupAdmin=orgManager.isGroupAdmin(user.getLoginName(),account);
		if(isGroupAdmin){return false;}
		boolean isAccountAdmin=orgManager.isAdministrator(user.getLoginName(),account);
		boolean hasImportEdocForm=(Boolean)(SysFlag.edoc_showImportEdocForm.getFlag());
		if(isAccountAdmin && hasImportEdocForm){return true;}
		return false;					
	}
	/*
	 * 是否有修改公文元素的权限，集团管理员，企业版、政务版中的单位管理员
	 */
	public static boolean canEditEdocElements() throws BusinessException
	{
		User user=AppContext.getCurrentUser();
		V3xOrgAccount account = orgManager.getAccountById(user.getAccountId());
		boolean isEnterVer=((Boolean)(SysFlag.sys_isEnterpriseVer.getFlag()) || (Boolean)(SysFlag.sys_isGovVer.getFlag()));
		if(isEnterVer==true && orgManager.isAdministrator(user.getLoginName(),account)){return true;}
		boolean isGroupVer=(Boolean)(SysFlag.sys_isGroupVer.getFlag());
		if(isGroupVer && orgManager.isGroupAdmin(user.getLoginName(),account)){return true;}
		return false;
	}
	/**
	 * 判断当前用户是否为集团管理员
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isGroupManager() throws BusinessException
	{
		User user=AppContext.getCurrentUser();
		V3xOrgAccount account = orgManager.getAccountById(user.getAccountId());
		return orgManager.isGroupAdmin(user.getLoginName(),account);	
	}
	/**
	 * 得到指定单位的公文收发员
	 * @param accountId：单位ID
	 * @return
	 * @throws BusinessException
	 */
	public static List<V3xOrgMember> getAccountExchangeUsers(Long accountId) throws BusinessException
	{		
		V3xOrgRole roleExchange = orgManager.getRoleByName(accountGovdocRec,accountId);
		List<V3xOrgMember> list=new ArrayList<V3xOrgMember>();
		if(roleExchange!=null && roleExchange.getEnabled()){
			list= orgManager.getMembersByRole(accountId, roleExchange.getId());	
		}
		
		return list;

			
	}
	
	/**
	 * 得到指定单位的公文收发员
	 * @param accountId：单位ID
	 * @return
	 * @throws BusinessException
	 */
	public static List<V3xOrgMember> getUsersByRoleName(String roleName,Long accountId) throws BusinessException
	{		
		V3xOrgRole roleExchange = orgManager.getRoleByName(roleName,accountId);
		List<V3xOrgMember> list=new ArrayList<V3xOrgMember>();
		if(roleExchange!=null && roleExchange.getEnabled()){
			list= orgManager.getMembersByRole(accountId, roleExchange.getId());	
		}
		
		return list;

			
	}
	
	/**
	 * 得到当前用户的部门收发员
	 * @return
	 * @throws BusinessException
	 */
	public static List<V3xOrgMember> getDepartMentExchangeUsers() throws BusinessException
	{
		/*
		V3xOrgRole roleExchange=null;
		User user=AppContext.getCurrentUser();
		V3xOrgDepartment dep=orgManagerDirect.getDepartmentById(user.getDepartmentId());
		List <Long> roleIds=dep.getRoles();
		for(Long roleId:roleIds)
		{
			roleExchange=orgManagerDirect.getRoleById(roleId);
			if(departmentExchangeRoleName.equals(roleExchange.getName()))
			{			
				break;
			}
		}		
		return orgManagerDirect.getMemberByRole(roleExchange.getBond(), user.getDepartmentId(), roleExchange.getId());
		*/
		//*
		User user=AppContext.getCurrentUser();
		return getDepartMentExchangeUsers(user.getLoginAccount(),user.getDepartmentId());
		//*/
	}
	
	/**
	 * 得到当前用户的部门收发员
	 * @return
	 * @throws BusinessException
	 */
	public static List<V3xOrgMember> getDepartMentExchangeUsers(Long accountId,Long departmentId) throws BusinessException
	{
		/*
		V3xOrgRole roleExchange=null;		
		V3xOrgAccount acc= orgManagerDirect.getAccountById(accountId);
		V3xOrgDepartment dep=acc.getDepartments().get(departmentId);		
		List <Long> roleIds=dep.getRoles();
		for(Long roleId:roleIds)
		{
			roleExchange=orgManagerDirect.getRoleById(roleId);
			if(departmentExchangeRoleName.equals(roleExchange.getName()))
			{			
				break;
			}
		}		
		return orgManagerDirect.getMemberByRole(roleExchange.getBond(), dep.getId(), roleExchange.getId());
		*/
		///*
		//User user=AppContext.getCurrentUser();
		List<V3xOrgMember> list=new ArrayList<V3xOrgMember>();
		V3xOrgRole exchangeRole=orgManager.getRoleByName(departmentGovdocRec,accountId);
		if(exchangeRole!=null && exchangeRole.getEnabled()){
			list=orgManager.getMembersByRole(departmentId, exchangeRole.getId());
		}
		
		return list;
		//*/
	}
	
	/**
	 * 判断当前用户是否为登陆单位的公文收发员
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isAccountExchange() throws BusinessException {
		return isAccountExchange(AppContext.getCurrentUser().getId(), AppContext.getCurrentUser().getLoginAccount());		
	}
	
	/**
	 * branches_a8_v350_r_gov 唐桂林添加判断某用户是否为登陆单位的公文收发员
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isAccountExchange(Long memberId)  throws BusinessException {
		User user=AppContext.getCurrentUser();
		//V3xOrgRole exchangeRole=orgManager.getRoleByName(acountExchangeRoleName, orgManager.getMemberById(memberId).getOrgAccountId());
		//return orgManager.isInDomain(user.getLoginAccount(), exchangeRole.getId(), memberId);		
		return orgManager.isRole(memberId, user.getLoginAccount(), acountExchangeRoleName);
	}
	
	/**
	 * branches_a8_v350_r_gov 某人是否是某单位的单位公文收发员
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isAccountExchange(Long memberId, Long accountId)  throws BusinessException {
		//V3xOrgRole exchangeRole=orgManager.getRoleByName(acountExchangeRoleName, accountId);
		//return orgManager.isInDomain(accountId, exchangeRole.getId(), memberId);		
		//return orgManager.isRole(memberId, accountId, acountExchangeRoleName);
		return true;//老公文角色权限全部打开V57
	}		
	
	/**
	 * branches_a8_v350_r_gov 某人是否是某部门的部门公文收发员
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isDepartmentExchange(Long memberId, Long deptId)  throws BusinessException {
//		return orgManager.isRole(memberId, deptId, departmentExchangeRoleName);
		return true;//老公文角色权限全部打开V57
	}
	
	/**
	 * branches_a8_v350_r_gov 唐桂林添 得到某用户在<指定单位下>承担部门收发员的部门ID
	 * @param accountId	：指定单位ID< 当accountId为VIRTUAL_ACCOUNT_ID时，返回所有的单位下的实体合集--集团化支持>
	 * @return , ','分割的字符串
	 */
	public static String getUserExchangeDepartmentIds(Long memberId, Long accountId)  throws BusinessException {
		return getUserExchangeAccountIdsOrDepartmentIds(departmentExchangeRoleName, memberId, accountId);
	}
	
	/**
	 * branches_a8_v350_r_gov 唐桂林添 得到某用户在<指定单位下>承担部门收发员的部门ID
	 * @param exchangeRoleName
	 * @param memberId
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public static String getUserExchangeAccountIdsOrDepartmentIds(String exchangeRoleName, Long memberId, Long accountId)  throws BusinessException {
		V3xOrgRole exchangeRole=null;
		List <Long> depIds=new ArrayList<Long>();
		Collection<Long> accountIds = new UniqueList<Long>();
		//1.查找单位ID
		if(accountId.equals(V3xOrgEntity.VIRTUAL_ACCOUNT_ID)){
			for(V3xOrgAccount account:orgManager.getAllAccounts()){
				accountIds.add(account.getId());
			}
		}else{
			accountIds.add(accountId);
		}
		//2、查找充当单位收发员的单位Id
		for(Long accId:accountIds){
			 exchangeRole=orgManager.getRoleByName(exchangeRoleName,accId);
			 if(exchangeRole != null)
				 depIds.addAll(orgManager.getDomainByRole(exchangeRole.getId(), memberId));
		}
		StringBuilder str=new StringBuilder();
		for(Long depId:depIds){
			if(str.length() > 0){
				str.append(",");
			}
			str.append(depId);
		}
		return str.toString();
	}
	
	/**
	 * 判断当前用户是否为收发员（包括部门收发员，单位收发员）
	 * @return
	 */
	public static boolean isExchangeRole()  throws BusinessException
	{
		boolean isExchange=false;
		try
		{
			isExchange=(isAccountExchange() || isDepartmentExchangeOfLoginAccout());
		}catch(Exception e)
		{	
		    LOG.error("", e);
		}
		return isExchange;
	}
	
	
	/**
	 * 判断某用户是否为收发员（包括部门收发员，单位收发员）
	 * @return
	 */
	public static boolean isExchangeRole(long memberId)  throws BusinessException {
		boolean isExchange=false;
		try {
			long loginAccount = CurrentUser.get().getLoginAccount();
			isExchange = (isAccountExchange(memberId) || isDepartmentExchangeOfLoginAccout(memberId, loginAccount));
		} catch(Exception e) {
		    LOG.error("", e);
		}
		return isExchange;
	}	
	
	/**
	 * 判断某用户是否为指定单位的收发员（包括部门收发员，单位收发员）
	 * @return
	 */
	public static boolean isExchangeRole(long memberId,long accountId)  throws BusinessException {
		boolean isExchange=false;
		try {
			long loginAccount = accountId;
			isExchange = (isAccountExchange(memberId,accountId) || isDepartmentExchangeOfLoginAccout(memberId, loginAccount));
		} catch(Exception e) {
		    LOG.error("", e);
		}
		return isExchange;
	}	
	
	public static String getDepartmentExchangeMember(Long deptId) throws BusinessException {
		List<V3xOrgMember> list =  orgManager.getMembersByRole(deptId, OrgConstants.Role_NAME.Departmentexchange.name());
		return list.size()>0?"true":"false";
	}
	
	public static String getAccountExchangeMember(Long accountId) throws BusinessException {
		List<V3xOrgMember> list =  orgManager.getMembersByRole(accountId, OrgConstants.Role_NAME.Accountexchange.name());
		return list.size()>0?"true":"false";
	}
	
	/**
	 * 判断当前用户是否为登陆部门的公文收发员
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isDepartmentExchange()  throws BusinessException
	{
		User user=AppContext.getCurrentUser();
		/*
		List <V3xOrgMember> mems=getAccountExchangeUsers();
		for(V3xOrgMember mem:mems)
		{
			if(mem.getId()==user.getId())
			{
				return true;
			}
		}
		*/
		//V3xOrgRole exchangeRole=orgManager.getRoleByName(departmentExchangeRoleName,user.getLoginAccount());
		//orgManager.getUserDomainIDs(userId, types)		
		//return orgManager.isInDomain(user.getDepartmentId(), exchangeRole.getId(),user.getId());		
		Boolean isExchangeRole = orgManager.isRole(user.getId(), user.getDepartmentId(), departmentExchangeRoleName);
		if(!isExchangeRole){
			Boolean isDepartmentGovdocSend = orgManager.isRole(user.getId(), user.getDepartmentId(), departmentGovdocSend);
			Boolean isDepartmentGovdocRec = orgManager.isRole(user.getId(), user.getDepartmentId(),departmentGovdocRec);
			if(isDepartmentGovdocSend && isDepartmentGovdocRec){
				isExchangeRole = true;
			}
		}
		return isExchangeRole;
	}
	/**
	 * 判断某个用户是否是某个单位下面某个部门的公文收发员
	 * @param accountId		：单位ID
	 * @param departmentId	：部门ID
	 * @param userId		：用户ID
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isDepartmentExchangeOld(Long accountId,Long departmentId,Long userId)  throws BusinessException
	{
		//V3xOrgRole exchangeRole=orgManager.getRoleByName(departmentExchangeRoleName,accountId);
		//return orgManager.isInDomain(departmentId, exchangeRole.getId(),userId);	
		Boolean isExchangeRole = orgManager.isRole(userId, departmentId, departmentExchangeRoleName);
		if(!isExchangeRole){
			Boolean isDepartmentGovdocSend = orgManager.isRole(userId, departmentId, departmentGovdocSend);
			Boolean isDepartmentGovdocRec = orgManager.isRole(userId, departmentId, departmentGovdocRec);
			if(isDepartmentGovdocSend && isDepartmentGovdocRec){
				isExchangeRole = true;
			}
		}
		return isExchangeRole;
	}
	/**
	 * 判断当前用户是否为当前登录单位的部门收发员（如果兼职到几个部门，则只要是其中一个部门的部门收发员即可）
	 * @param accountId ：单位ID
	 * @return
	 * @throws Exception
	 */
	public static boolean isDepartmentExchangeOfLoginAccout() throws BusinessException{
		return !"".equals(getUserExchangeDepartmentIds(AppContext.getCurrentUser().getLoginAccount()));
	}

	/**
	 * branches_a8_v350_r_gov 唐桂林添 判断某用户是否为当前登录单位的部门收发员（如果兼职到几个部门，则只要是其中一个部门的部门收发员即可）
	 * @param accountId ：单位ID
	 * @return
	 * @throws Exception
	 */
	public static boolean isDepartmentExchangeOfLoginAccout(long memberId, long accountId) throws BusinessException{
		//Long accountId = orgManager.getMemberById(memberId).getOrgAccountId();
		return !"".equals(getUserExchangeDepartmentIds(memberId, accountId));
	}
	
	/**
	 * branches_a8_v350_r_gov 唐桂林添 判断某用户是否为当前登录单位的部门收发员（如果兼职到几个部门，则只要是其中一个部门的部门收发员即可）
	 * @param accountId ：单位ID
	 * @return
	 * @throws Exception
	 */
	public static boolean isDepartmentExchangeOfLoginAccout(Long memberId, Long accountId) throws BusinessException{
		return !"".equals(getUserExchangeDepartmentIds(memberId, accountId));
	}
	
	/**
	 * 得到当前用户在<指定单位下>承担部门收发员的部门ID
	 * @param accountId	：指定单位ID< 当accountId为VIRTUAL_ACCOUNT_ID时，返回所有的单位下的实体合集--集团化支持>
	 * @return , ','分割的字符串
	 */
	public static String getUserExchangeDepartmentIds(Long accountId)  throws BusinessException
	{
		return getUserExchangeAccountIdsOrDepartmentIds(departmentExchangeRoleName,accountId);
	}
	/**
	 * 得到当前用户在<当前登录单位>承担部门收发员的部门ID
	 * 	 * @return , ','分割的字符串
	 */
	public static String getUserExchangeDepartmentIds()  throws BusinessException
	{
		User user=AppContext.getCurrentUser();
		return getUserExchangeDepartmentIds(user.getLoginAccount());
	}
	
	/**
	 * 获取用户交换机构的名称
	 * @Author      : xuqiangwei
	 * @Date        : 2014年12月21日上午11:59:33
	 * @param user
	 * @return 如果是单位交换员直接返回单位名称，其他返回所有交换的信息
	 * @throws BusinessException 
	 */
	public static String getExchangeOgrNames(User user) throws BusinessException{
	    
	    String defalutPushName = null;//推送首页默认名称
	    boolean isAccountExcRole = GovdocRoleHelper.isAccountExchange(user.getId());//是否为单位收发员
        if(isAccountExcRole){
            defalutPushName = AppContext.currentAccountName();
        }else{
            String departmentIds = GovdocRoleHelper.getUserExchangeDepartmentIds();
            if (Strings.isNotBlank(departmentIds)) {
                String[] depIds = departmentIds.split("[,]");
                if (depIds.length > 0) {
                    StringBuilder sBuilder = new StringBuilder();
                    for (int i = 0; i < depIds.length; i++) {
                        Long deptId = Long.parseLong(depIds[i]);
                        V3xOrgDepartment dept = orgManager.getDepartmentById(deptId);
                        if(dept != null && user.getLoginAccount().equals(dept.getOrgAccountId())){//当前单位限制
                            
                            String deptName = dept.getName();
                            if(sBuilder.toString().length() > 0){
                                sBuilder.append(ResourceUtil.getString("common.separator.label"));
                            }
                            sBuilder.append(deptName);
                        }
                    }
                    defalutPushName = sBuilder.toString();
                }
            }
        }
        return defalutPushName;
	}
	
	/**
	 * 
	 * @return
	 * @throws BusinessException
	 */
	public static List<Long> getUserExchangeDepartmentIdsToList()  throws BusinessException{
		List<Long> list=new ArrayList<Long>();
		String ids=getUserExchangeDepartmentIds();
		if(ids!=null && !"".equals(ids)){
			for(String s:ids.split(",")){
				list.add(Long.parseLong(s));
			}
		}
		return list;
	}
	/**
	 * 得到当前用户在<指定单位下>承担单位收发员的单位ID
	 * 	 * @return , ','分割的字符串
	 */
	public static String getUserExchangeAccountIds(Long accountId)  throws BusinessException
	{
		return getUserExchangeAccountIdsOrDepartmentIds(acountExchangeRoleName,accountId);
	}
	/**
	 * 得到当前用户在<登录单位>承担单位收发员的单位ID
	 * @return , ','分割的字符串
	 */
	public static String getUserExchangeAccountIds()  throws BusinessException
	{
		User user=AppContext.getCurrentUser();
		return getUserExchangeAccountIds(user.getLoginAccount());
	}
	public static String getUserExchangeAccountIdsOrDepartmentIds(String exchangeRoleName,Long accountId)  throws BusinessException
	{
		User user=AppContext.getCurrentUser();
//		V3xOrgRole exchangeRole=null;
		List <Long> depIds=new ArrayList<Long>();
/*		Collection<Long> accountIds = new UniqueList<Long>();
		//1.查找单位ID
		if(accountId.equals(V3xOrgEntity.VIRTUAL_ACCOUNT_ID)){
			for(V3xOrgAccount account:orgManager.getAllAccounts()){
				accountIds.add(account.getId());
			}
		}else{
			accountIds.add(accountId);
		}
		//2、查找充当单位收发员的单位Id
		for(Long accId:accountIds){
			 exchangeRole=orgManager.getRoleByName(exchangeRoleName,accId);
			 if(exchangeRole != null){
				 List<Long> list=orgManager.getDomainByRole(exchangeRole.getId(),user.getId());
				 depIds.addAll(list);
			 }
		}*/
		
		//OA-49053公文交换待发送、待签收数据在首页待办栏目显示，但是不在列表中显示
		//先查出当前用户的所有角色
		List<MemberRole> roleList=orgManager.getMemberRoles( user.getId(), V3xOrgEntity.VIRTUAL_ACCOUNT_ID);
		for(MemberRole m:roleList){
			//角色过滤，只有和exchangeRoleName相等的角色才能留下
			if(m.getRole().getCode().equals(exchangeRoleName)){
				if(acountExchangeRoleName.equals(exchangeRoleName)){//如果是单位收发员，要取角色的单位id
					depIds.add(m.getAccountId());
				}else if(departmentExchangeRoleName.equals(exchangeRoleName)){//如果是部门收发员，要取角色部门的id
					depIds.add(m.getDepartment().getId());
				}
			}
		}
		
		StringBuilder str= new StringBuilder();
		for(Long depId:depIds){
			if(str.length() > 0){
				str.append(",");
			}
			str.append(depId);
		}

		return str.toString();
		
	}
	
	public static V3xOrgAccount getAccountById(Long accountId) throws Exception {
		return orgManager.getAccountById(accountId);
	}
	
	public static V3xOrgDepartment getDepartmentById(Long deptId) throws Exception {
		return orgManager.getDepartmentById(deptId);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	 /**
     * 判断当前用户是否为登陆单位的公文管理员
     * @return
     * @throws BusinessException
     */
    public static boolean isAccountEdocAdmin()  throws BusinessException {
    	User user=AppContext.getCurrentUser();
        V3xOrgRole accountEdocAdminRole=orgManager.getRoleByName(AccountEdocAdminRoleName,user.getLoginAccount());
        if(accountEdocAdminRole==null){
        	return false;
        }else{
        	//return orgManager.isInDomain(user.getLoginAccount(), accountEdocAdminRole.getId(),user.getId());
        	return orgManager.isRole(user.getId(), user.getLoginAccount(), AccountEdocAdminRoleName);
        }      
    }

    
}
