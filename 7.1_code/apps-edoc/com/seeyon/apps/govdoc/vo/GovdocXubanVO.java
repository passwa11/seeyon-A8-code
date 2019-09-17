package com.seeyon.apps.govdoc.vo;

import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.organization.bo.V3xOrgMember;

public class GovdocXubanVO {
	
	private String customDealWith;
	private Object customDealWithPermission;
	private Object customDealWithMemberId;
	private int returnPermissionsLength;
	private List<PermissionVO> permissions;
	private List<Map<String,Object>> members;
	private String memberJson;
	private String currentPolicyId;
	private String currentPolicyName;
	private boolean notExistChengban;
	private String currentMember;
	private V3xOrgMember nextMember;
	
	public String getCustomDealWith() {
		return customDealWith;
	}
	public void setCustomDealWith(String customDealWith) {
		this.customDealWith = customDealWith;
	}
	public Object getCustomDealWithPermission() {
		return customDealWithPermission;
	}
	public void setCustomDealWithPermission(Object customDealWithPermission) {
		this.customDealWithPermission = customDealWithPermission;
	}
	public Object getCustomDealWithMemberId() {
		return customDealWithMemberId;
	}
	public void setCustomDealWithMemberId(Object customDealWithMemberId) {
		this.customDealWithMemberId = customDealWithMemberId;
	}
	public int getReturnPermissionsLength() {
		return returnPermissionsLength;
	}
	public void setReturnPermissionsLength(int returnPermissionsLength) {
		this.returnPermissionsLength = returnPermissionsLength;
	}
	public List<PermissionVO> getPermissions() {
		return permissions;
	}
	public void setPermissions(List<PermissionVO> permissions) {
		this.permissions = permissions;
	}
	public List<Map<String, Object>> getMembers() {
		return members;
	}
	public void setMembers(List<Map<String, Object>> members) {
		this.members = members;
	}
	public String getMemberJson() {
		return memberJson;
	}
	public void setMemberJson(String memberJson) {
		this.memberJson = memberJson;
	}
	public String getCurrentPolicyId() {
		return currentPolicyId;
	}
	public void setCurrentPolicyId(String currentPolicyId) {
		this.currentPolicyId = currentPolicyId;
	}
	public String getCurrentPolicyName() {
		return currentPolicyName;
	}
	public void setCurrentPolicyName(String currentPolicyName) {
		this.currentPolicyName = currentPolicyName;
	}
	public boolean isNotExistChengban() {
		return notExistChengban;
	}
	public void setNotExistChengban(boolean notExistChengban) {
		this.notExistChengban = notExistChengban;
	}
	public String getCurrentMember() {
		return currentMember;
	}
	public void setCurrentMember(String currentMember) {
		this.currentMember = currentMember;
	}
	public V3xOrgMember getNextMember() {
		return nextMember;
	}
	public void setNextMember(V3xOrgMember nextMember) {
		this.nextMember = nextMember;
	}
}
