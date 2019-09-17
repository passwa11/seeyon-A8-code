package com.seeyon.apps.govdoc.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.govdoc.manager.GovdocPishiManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.MemberRole;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.manager.EdocRoleHelper;

public class GovdocRoleHelper extends EdocRoleHelper {
	
	private static final Log LOGGER = LogFactory.getLog(GovdocRoleHelper.class);
	
	public static String ACCOUNT_GOVDOC_SEND = "AccountGovdocSend";//单位公文送文员
	public static String ACCOUNT_GOVDOC_REC = "AccountGovdocRec";//单位公文收文员
	
	public static String DEPARTMENT_GOVDOC_SEND = "DepartmentGovdocSend";//部门公文送文员
	public static String DEPARTMENT_GOVDOC_REC = "DepartmentGovdocRec";//部门公文收文员
	
	private static GovdocPishiManager govdocPishiManager = (GovdocPishiManager)AppContext.getBean("govdocPishiManager");
	
	/**
	 * 是否公文收发员
	 * @param memberId
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isExchangeRole()  throws BusinessException {
		User user = AppContext.getCurrentUser();
		return isAccountExchange(user.getId(), user.getLoginAccount()) || isDepartmentExchange(user.getId(), user.getLoginAccount());
	}
	
	/**
	 * 是否公文收发员
	 * @param memberId
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isExchangeRole(Long memberId)  throws BusinessException {
		User user = AppContext.getCurrentUser();
		return isAccountExchange(memberId, user.getLoginAccount()) || isDepartmentExchange(memberId, user.getLoginAccount());
	}
	
	/**
	 * 是否公文收发员
	 * @param memberId
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isExchangeRole(Long memberId, Long accountId)  throws BusinessException {
		return isAccountExchange(memberId, accountId) || isDepartmentExchange(memberId, accountId);
	}

	/**
	 * 是否单位公文收发员
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isAccountExchange()  throws BusinessException {
		User user = AppContext.getCurrentUser();
		return isAccountExchange(user.getId(), user.getLoginAccount());
	}
	/**
	 * 是否单位公文收发员
	 * @param memberId
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isAccountExchange(Long memberId)  throws BusinessException {
		User user = AppContext.getCurrentUser();
		return isAccountExchange(memberId, user.getLoginAccount());
	}
	/**
	 * 是否单位公文收发员
	 * @param memberId
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isAccountExchange(Long memberId, Long accountId)  throws BusinessException {
		return isAccountSendExchange(memberId, accountId) || isAccountRecExchange(memberId, accountId);
	}
	
	/**
	 * 是否单位公文送文员
	 * @param memberId
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isAccountSendExchange(Long memberId, Long accountId)  throws BusinessException {
		return orgManager.isRole(memberId, accountId, ACCOUNT_GOVDOC_SEND);
	}
	
	/**
	 * 是否单位公文收文员
	 * @param memberId
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isAccountRecExchange(Long memberId, Long accountId)  throws BusinessException {
		return orgManager.isRole(memberId, accountId, ACCOUNT_GOVDOC_REC);
	}

	/**
	 * 是否部门收发员
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isDepartmentExchange()  throws BusinessException {
		User user = AppContext.getCurrentUser();
		return isDepartmentExchange(user.getId(), user.getLoginAccount());
	}

	/**
	 * 是否部门收发员
	 * @param memberId
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isDepartmentExchange(Long memberId, Long accountId)  throws BusinessException {
		V3xOrgMember member = orgManager.getMemberById(memberId);
		if(member != null) {
			return isDepartmentExchange(memberId, member.getOrgDepartmentId(), accountId);
		}
		return false;
	}
	
	/**
	 * 是否部门收发员
	 * @param memberId
	 * @param deptId
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isDepartmentExchange(Long memberId, Long deptId, Long accountId)  throws BusinessException {
		return isDepartmentSendExchange(memberId, deptId, accountId) || isDepartmentRecExchange(memberId, deptId, accountId);
	}
	
	/**
	 * 是否部门公文送文员
	 * @param memberId
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isDepartmentSendExchange(Long memberId, Long departmentId, Long accountId)  throws BusinessException {
		return orgManager.isRole(memberId, departmentId, DEPARTMENT_GOVDOC_SEND);
	}
	
	/**
	 * 是否部门公文收文员
	 * @param memberId
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isDepartmentRecExchange(Long memberId, Long departmentId, Long accountId)  throws BusinessException {
		return orgManager.isRole(memberId, departmentId, DEPARTMENT_GOVDOC_REC);
	}
	
	/**
	 * 获取单位公文收发员人员集合
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public static List<V3xOrgMember> getAccountExchangeUsers() throws BusinessException {
		User user = AppContext.getCurrentUser();
		Map<Long, Long> map = new HashMap<Long, Long>();
		List<V3xOrgMember> memberList = getAccountSendExchangeUsers(user.getLoginAccount());
		if(memberList == null) {
			memberList = new ArrayList<V3xOrgMember>();
		}
		for(V3xOrgMember member : memberList) {
			map.put(member.getId(), member.getId());
		}
		List<V3xOrgMember> memberRecList = getAccountRecExchangeUsers(user.getLoginAccount());
		if(Strings.isNotEmpty(memberRecList)) {
			for(V3xOrgMember member : memberRecList) {
				if(!map.containsKey(member.getId())) {
					map.put(member.getId(), member.getId());
					memberList.add(member);
				}
			}	
		}
		return memberList;
	}
	
	/**
	 * 获取单位公文送文员
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public static List<V3xOrgMember> getAccountSendExchangeUsers(Long accountId) throws BusinessException {		
		return orgManager.getMembersByRole(accountId, ACCOUNT_GOVDOC_SEND);
	}
	/**
	 * 获取单位公文收文员
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public static List<V3xOrgMember> getAccountRecExchangeUsers(Long accountId) throws BusinessException {		
		return orgManager.getMembersByRole(accountId, ACCOUNT_GOVDOC_REC);
	}
	/**
	 * 得到某部门的公文收发员
	 * @return
	 * @throws BusinessException
	 */
	public static List<V3xOrgMember> getDepartmentExchangeUsers(Long deptId) throws BusinessException {
		Map<Long, Long> map = new HashMap<Long, Long>();
		List<V3xOrgMember> memberList = getDepartmentSendExchangeUsers(deptId);
		if(memberList == null) {
			memberList = new ArrayList<V3xOrgMember>();
		}
		for(V3xOrgMember member : memberList) {
			map.put(member.getId(), member.getId());
		}
		List<V3xOrgMember> memberRecList = getDepartmentRecExchangeUsers(deptId);
		if(Strings.isNotEmpty(memberRecList)) {
			for(V3xOrgMember member : memberRecList) {
				if(!map.containsKey(member.getId())) {
					map.put(member.getId(), member.getId());
					memberList.add(member);
				}
			}	
		}
		return memberList;
	}
	/**
	 * 获取部门公文送文员
	 * @param deptId
	 * @return
	 * @throws BusinessException
	 */
	public static List<V3xOrgMember> getDepartmentSendExchangeUsers(Long deptId) throws BusinessException {
		return orgManager.getMembersByRole(deptId, DEPARTMENT_GOVDOC_SEND);
	}
	/**
	 * 获取部门公文收文员
	 * @param deptId
	 * @return
	 * @throws BusinessException
	 */
	public static List<V3xOrgMember> getDepartmentRecExchangeUsers(Long deptId) throws BusinessException {
		return orgManager.getMembersByRole(deptId, DEPARTMENT_GOVDOC_REC);
	}
	
	/**
	 * 
	 * @param userId
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public static String getSendExchangeAccountIds(Long userId, Long accountId) throws BusinessException {
		StringBuilder buffer = new StringBuilder();
		List<MemberRole> roleList = orgManager.getMemberRoles(userId, V3xOrgEntity.VIRTUAL_ACCOUNT_ID);
		for(MemberRole m:roleList) {
			//角色过滤，只有和exchangeRoleName相等的角色才能留下
			if(m.getRole().getCode().equals(ACCOUNT_GOVDOC_SEND)) {
				buffer.append(m.getAccountId());
			}
		}
		String ids = buffer.toString();
		if(Strings.isNotBlank(ids)) {
			ids = ids.substring(0, ids.length() - 1);
		}
		return ids;
	}
	/**
	 * 获取部门交换的部门ID串
	 * @return
	 * @throws BusinessException
	 */
	public static String getUserExchangeDepartmentIds()  throws BusinessException {
		User user = AppContext.getCurrentUser();
		return getUserExchangeDepartmentIds(user.getId(), user.getLoginAccount());
	}
	/**
	 * 获取部门交换的部门ID串
	 * @param memberId
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public static String getUserExchangeDepartmentIds(Long memberId, Long accountId)  throws BusinessException {
		Map<String, String> map = new HashMap<String, String>();		
		StringBuilder buffer = new StringBuilder();
		String sendDepartmentIds = getUserSendExchangeDepartmentIds(memberId, accountId);
		if(Strings.isNotEmpty(sendDepartmentIds.toString())) {
			String[] deptIds = sendDepartmentIds.toString().split(",");
			for(String m : deptIds) {
				map.put(m, m);
			}
			buffer.append(sendDepartmentIds);
		}
		
		String recDepartmentIds = getUserRecExchangeDepartmentIds(memberId, accountId);
		if(Strings.isNotEmpty(recDepartmentIds.toString())) {
			String[] deptIds = recDepartmentIds.toString().split(",");
			for(String m : deptIds) {
				if(!map.containsKey(m)) {
					map.put(m, m);
					buffer.append(m + ",");
				}
			}
		}
		
		String ids = buffer.toString();
		if(Strings.isNotBlank(ids)) {
			ids = ids.substring(0, ids.length() - 1);
		}
		return ids;
	}
	/**
	 * 获取送文部门交换的部门ID串
	 * @param userId
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public static String getUserSendExchangeDepartmentIds(Long userId, Long accountId) throws BusinessException {		
		StringBuilder buffer = new StringBuilder();
		List<MemberRole> roleList = orgManager.getMemberRoles(userId, V3xOrgEntity.VIRTUAL_ACCOUNT_ID);
		for(MemberRole m:roleList) {
			//角色过滤，只有和exchangeRoleName相等的角色才能留下
			if(m.getRole().getCode().equals(DEPARTMENT_GOVDOC_SEND)) {
				buffer.append(m.getDepartment().getId());
			}
		}
		String ids = buffer.toString();
		if(Strings.isNotBlank(ids)) {
			ids = ids.substring(0, ids.length() - 1);
		}
		return ids;
	}
	/**
	 * 获取收文部门交换的部门ID串
	 * @param userId
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public static String getUserRecExchangeDepartmentIds(Long userId, Long accountId) throws BusinessException {		
		StringBuilder buffer = new StringBuilder();
		List<MemberRole> roleList = orgManager.getMemberRoles(userId, V3xOrgEntity.VIRTUAL_ACCOUNT_ID);
		for(MemberRole m:roleList) {
			//角色过滤，只有和exchangeRoleName相等的角色才能留下
			if(m.getRole().getCode().equals(DEPARTMENT_GOVDOC_REC)) {
				buffer.append(m.getDepartment().getId());
			}
		}
		String ids = buffer.toString();
		if(Strings.isNotBlank(ids)) {
			ids = ids.substring(0, ids.length() - 1);
		}
		return ids;
	}

	/**
	 * 某部门是否有部门收文员
	 * @param deptId
	 * @return
	 * @throws BusinessException
	 */
	public static String hasDepartmentRecExchangeMember(Long deptId) throws BusinessException {
		List<V3xOrgMember> list =  orgManager.getMembersByRole(deptId, DEPARTMENT_GOVDOC_REC);
		return list.size()>0?"true":"false";
	}
	
	/**
	 * 某单位是否有单位收文员
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public static String hasAccountRecExchangeMember(Long accountId) throws BusinessException {
		List<V3xOrgMember> list =  orgManager.getMembersByRole(accountId, ACCOUNT_GOVDOC_REC);
		return list.size()>0?"true":"false";
	}
	
	/**
	 * 判断是否某人代录人
	 * @param affairMemberId
	 * @return
	 */
	public static boolean checkPishi(Long affairMemberId) {
		Long userId = AppContext.currentUserId();
		String result = govdocPishiManager.checkLeaderPishi(userId, affairMemberId);
		if("pishi".equals(result)) {
			return true;
		}
		return false;
	}
	



	/**
	 * 判断当前登录人员是否督办人员
	 */
	public static boolean isSuperviseStaff() {
		User u = AppContext.getCurrentUser();
		List<MemberRole> roleList = null;
		try {
			roleList = orgManager.getMemberRoles(u.getId(), u.getAccountId());
		} catch (BusinessException e) {
			LOGGER.error(e);
			return false;
		}
		if (roleList != null) {
			for (MemberRole m : roleList) {
				// 督办人员角色
				if (m.getRole().getCode().equals(OrgConstants.Role_NAME.SuperviseStaff.name())) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 是否有文档中心的资源权限
	 * @param currentUser
	 * @return
	 */
	public static boolean hasDocResource(User currentUser) {
		return currentUser.hasResourceCode("F04_docIndex")
				|| currentUser.hasResourceCode("F04_myDocLibIndex") 
				|| currentUser.hasResourceCode("F04_accDocLibIndex")
				|| currentUser.hasResourceCode("F04_proDocLibIndex") 
				|| currentUser.hasResourceCode("F04_eDocLibIndex") 
				|| currentUser.hasResourceCode("F04_docLibsConfig");
	}
	
}
