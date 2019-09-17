package com.seeyon.apps.govdoc.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.MemberPost;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.po.OrgRole;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.constants.WorkFlowConstants;
import com.seeyon.v3x.edoc.domain.EdocMarkAcl;
import com.seeyon.v3x.edoc.domain.EdocObjTeam;
import com.seeyon.v3x.exchange.domain.ExchangeAccount;

public class GovdocOrgHelper extends GovdocHelper {

	private static final Log LOGGER = LogFactory.getLog(GovdocOrgHelper.class);

	/**
	 * 
	 * @param value
	 * @return
	 * @throws BusinessException
	 */
	public static Map<String, String> getUnitNameAndUnitId(String value) throws BusinessException {
		Map<String, String> result = new HashMap<String, String>();
		String unitName = value;
		StringBuilder unitId = new StringBuilder();
		Pattern pattern = Pattern.compile("([a-zA-Z]+[\\|])?([-]?[0-9]{10,20})");
		Matcher matcher = pattern.matcher(value);
		String group = null;
		Long id = null;
		while (matcher.find()) {
			try {
				group = matcher.group(0);
				id = Long.parseLong(matcher.group(2));
				if (group.startsWith("Account|")) {
					V3xOrgAccount account = orgManager.getAccountById(id);
					if (account != null) {
						unitName = unitName.replace(group, account.getName());
						unitId.append(group).append(",");
					}
				} else if (group.startsWith("OrgTeam|")) {
					EdocObjTeam team = govdocObjTeamManager.getObjTeamById(id);
					if (team != null) {
						unitName = unitName.replace(group, team.getName());
						unitId.append(group).append(",");
					}
				} else if (group.startsWith("Department|")) {
					V3xOrgDepartment department = orgManager.getDepartmentById(id);
					if (department != null) {
						unitName = unitName.replace(group, department.getName());
						unitId.append(group).append(",");
					}
				} else if (group.startsWith("ExchangeAccount|")) {
					ExchangeAccount account2 = govdocExchangeAccountManager.getExchangeAccount(id);
					if (account2 != null) {
						unitName = unitName.replace(group, account2.getName());
						unitId.append(group).append(",");
					}
				} else {
					V3xOrgEntity entity = orgManager.getEntityOnlyById(id);
					if (entity != null) {
						unitName = unitName.replace(group, entity.getName());
						unitId.append(group).append(",");
					}
				}
			} catch (Exception e) {
				LOGGER.error("getUnitNameAndUnitId：" + value);
			}
		}
		unitName = unitName.replaceAll("[,]+", ",");
		result.put("unitName", unitName);
		result.put("unitId", unitId.toString());
		return result;
	}

	/**
	 * 
	 * @param value
	 * @return
	 * @throws BusinessException
	 */
	public static Map<String, String> getUnitNameAndUnitId1(String value) throws BusinessException {
		Map<String, String> result = new HashMap<String, String>();
		if (value.contains("hiddenValue")) {
			value = value.replace("undefined", "");
			String[] str = value.split("hiddenValue");
			if (str.length == 1) {
				result.put("unitName", str[0]);
				result.put("unitId", "");
			} else if (str.length == 2) {
				result.put("unitName", str[0]);
				result.put("unitId", str[1]);
			}
		} else {
			return getUnitNameAndUnitId(value);
		}
		return result;
	}

	/**
	 * 解析相对角色并将其添加到集合
	 * 
	 * @param members
	 *            集合
	 * @param startMember
	 *            发起者
	 * @param memberRole
	 *            相对角色字符串
	 */
	@SuppressWarnings("rawtypes")
	public static void parseRoleMember(List<Map<String, Object>> members, Long startMember, String memberRole) {
		String startRole = "";
		String role = "";
		Long memberId = 0L;
		// 发起者
		if (memberRole.startsWith(WorkFlowConstants.ORGENT_META_KEY_SEDNER)) {
			startRole = memberRole.substring(0, WorkFlowConstants.ORGENT_META_KEY_SEDNER.length());
			role = memberRole.substring(WorkFlowConstants.ORGENT_META_KEY_SEDNER.length());
			memberId = startMember;
		} else if (memberRole.startsWith(WorkFlowConstants.ORGENT_META_KEY_NODEUSER)) {// 上节点
			startRole = memberRole.substring(0, WorkFlowConstants.ORGENT_META_KEY_NODEUSER.length());
			role = memberRole.substring(WorkFlowConstants.ORGENT_META_KEY_NODEUSER.length());
			memberId = AppContext.currentUserId();
		}
		try {
			V3xOrgMember tempMember = orgManager.getMemberById(memberId);
			Long accountId = tempMember.getOrgAccountId();
			Long departmentId = tempMember.getOrgDepartmentId();
			// 如果是上级部门 重新获取部门id
			if (role.startsWith(WorkFlowConstants.ORGENT_META_KEY_SUPERDEPT)) {
				role = role.substring(WorkFlowConstants.ORGENT_META_KEY_SUPERDEPT.length());
				String path = orgManager.getDepartmentById(departmentId).getPath();
				V3xOrgDepartment tempDepartment = orgManager.getDepartmentByPath(path.substring(0, path.length() - 4));
				if (tempDepartment != null) {
					departmentId = tempDepartment.getId();
				}
			}
			// 如果是发起者部门成员
			if (WorkFlowConstants.ORGENT_META_KEY_DEPMEMBER.equals(role)) {
				List<V3xOrgMember> memberList = orgManager.getMembersByDepartment(departmentId, true);
				for (V3xOrgMember v3xOrgMember : memberList) {
					Map<String, Object> memberMap = new HashMap<String, Object>();
					memberMap.put("id", v3xOrgMember.getId());
					memberMap.put("name", v3xOrgMember.getName());
					memberMap.put("orgAccountId", v3xOrgMember.getOrgAccountId());
					if (!members.contains(memberMap)) {
						members.add(memberMap);
					}
				}
				return;
			}
			if (Strings.isBlank(role) && Strings.isNotEmpty(startRole)) {
				if (WorkFlowConstants.ORGENT_META_KEY_SEDNER.equals(startRole)) {
					Map<String, Object> memberMap = new HashMap<String, Object>();
					memberMap.put("id", tempMember.getId());
					memberMap.put("name", tempMember.getName());
					memberMap.put("orgAccountId", tempMember.getOrgAccountId());
					if (!members.contains(memberMap)) {
						members.add(memberMap);
					}
				}
				return;
			}
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("code", role);
			List<OrgRole> orgRoleList = orgDao.getAllRolePO(accountId, true, params, new FlipInfo());
			if (!orgRoleList.isEmpty()) {
				params.clear();
				params.put("id", orgRoleList.get(0).getId());
				List<V3xOrgMember> orgMembers = orgManager.getMembersByRole(departmentId, orgRoleList.get(0).getId());
				if (!orgMembers.isEmpty()) {
					String ids = "";
					for (Map map : members) {
						ids += map.get("id") + ",";
					}
					for (V3xOrgMember member : orgMembers) {
						// 如果已经有了,或者不是本部门的
						if (ids.contains(member.getId().toString())) {
							continue;
						}
						Map<String, Object> memberMap = new HashMap<String, Object>();
						memberMap.put("id", member.getId());
						memberMap.put("name", member.getName());
						memberMap.put("orgAccountId", member.getOrgAccountId());
						if (!members.contains(memberMap)) {
							members.add(memberMap);
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(e);
			// e.printStackTrace();
			// logger.error(e.getMessage(), e);
		}
	}

	public static List<Long> getDeptIdList(Long currentAccountId) throws BusinessException {
		List<Long> deptIds = new ArrayList<Long>();
		deptIds.add(currentAccountId);
		deptIds.add(OrgConstants.GROUPID);
		List<V3xOrgDepartment> departmentIds = orgManager.getChildDepartments(Long.valueOf(currentAccountId), false);
		for (V3xOrgDepartment dept : departmentIds) {
			deptIds.add(dept.getId());
		}
		return deptIds;
	}

	public static List<Long> getUserDepIds(Long userId, Long currentAccountId, boolean isAdmin) throws BusinessException {
		StringBuilder depIdStr = new StringBuilder();

		List<Long> allUnitIdList = new ArrayList<Long>();
		allUnitIdList.add(OrgConstants.GROUPID);
		allUnitIdList.add(currentAccountId);
		depIdStr.append(OrgConstants.GROUPID);
		depIdStr.append("," + currentAccountId);

		try {
			Long currentDeptId = orgManager.getCurrentDepartment().getId();
			allUnitIdList.add(currentDeptId);
			depIdStr.append("," + currentDeptId);

			List<V3xOrgDepartment> parentList = orgManager.getAllParentDepartments(currentDeptId);
			if (Strings.isNotEmpty(parentList)) {
				for (V3xOrgDepartment bean : parentList) {
					if (!allUnitIdList.contains(bean.getId())) {
						allUnitIdList.add(bean.getId());
						depIdStr.append("," + bean.getId());
					}
				}
			}
		} catch (Exception e) {
		}

		List<MemberPost> postList = orgManager.getMemberPosts(currentAccountId, AppContext.currentUserId());
		for (MemberPost post : postList) {
			if (post.getDepId() != null && !allUnitIdList.contains(post.getDepId())) {
				allUnitIdList.add(post.getDepId());
				depIdStr.append("," + post.getDepId());
			}
		}
		if (isAdmin) {
			List<V3xOrgDepartment> dList = orgManager.getChildDepartments(currentAccountId, true);
			for (V3xOrgDepartment subDep : dList) {
				if (!allUnitIdList.contains(subDep.getId())) {
					allUnitIdList.add(subDep.getId());
					depIdStr.append(",").append(subDep.getId());
				}
			}
		}
		return allUnitIdList;
	}

	public static String getMarkAclEntityName(List<EdocMarkAcl> aclList, Long markDefId) throws BusinessException {
		String entityName = "";
		List<V3xOrgEntity> aclEntity = new ArrayList<V3xOrgEntity>();
		// 客开 项目名称： [修改功能：如果不是本单位加上区隔] 作者：fzc 修改日期：2018-5-10 start
		Long currentUserAccountId = AppContext.getCurrentUser().getAccountId();
		V3xOrgEntity orgEntity = null;
		for (EdocMarkAcl markAcl : aclList) {
			if (markAcl.getMarkDefId().longValue() == markDefId.longValue()) {
				orgEntity = orgManager.getEntity(markAcl.getAclType(), markAcl.getDeptId());
				if (orgEntity != null && currentUserAccountId.equals(orgEntity.getOrgAccountId())) {
					entityName += orgEntity.getName() + "、";
				} else if (orgEntity != null) {
					V3xOrgAccount temp = orgManager.getAccountById(orgEntity.getOrgAccountId());
					entityName += orgEntity.getName() + "(" + temp.getShortName() + ")" + "、";
				}
				// 客开 项目名称： [修改功能：如果不是本单位加上区隔] 作者：fzc 修改日期：2018-5-10 end
				aclEntity.add(orgEntity);
			}
		}
		if (Strings.isNotBlank(entityName)) {
			entityName = entityName.substring(0, entityName.length() - 1);
		}
		return entityName;
	}

	public static V3xOrgEntity getOrgEntity(String entityType, Long entityId) throws BusinessException {
		return orgManager.getEntity(entityType, entityId);
	}

	public static V3xOrgMember getMemberById(Long memberId) {
		try {
			V3xOrgMember member = orgManager.getMemberById(memberId);
			return member;
		} catch (Exception e) {

		}
		return null;
	}

	public static String getMemberNameById(Long memberId) {
		try {
			V3xOrgMember member = orgManager.getMemberById(memberId);
			if (member != null) {
				return member.getName();
			}
		} catch (Exception e) {

		}
		return null;
	}

	/*
	 * 获取人员id
	 */
	public static List<V3xOrgMember> getMembersByTypeAndIds(String typeAndIds) {
		Set<V3xOrgMember> members = new HashSet<V3xOrgMember>();
		try {
			members = orgManager.getMembersByTypeAndIds(typeAndIds);
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			LOGGER.error(e);
		}
		List<V3xOrgMember> memberList = new ArrayList<V3xOrgMember>(members);
		return memberList;
	}

	/**
	 * 获取组织结构等对象
	 * 
	 * @param memberId
	 * @param entityClass
	 * @return
	 */
	public static <T extends V3xOrgEntity> T getEntityById(Class<T> classType, Long id) throws BusinessException {
		return orgManager.getEntityById(classType, id);
	}

}
