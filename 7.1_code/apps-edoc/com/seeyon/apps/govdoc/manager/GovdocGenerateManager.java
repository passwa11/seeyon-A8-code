package com.seeyon.apps.govdoc.manager;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;

public interface GovdocGenerateManager {

	/**
	 * 新建单位复制公文数据
	 * @param accountId
	 * @throws BusinessException
	 */
	public void generate(V3xOrgAccount accountId) throws BusinessException;
	
	/**
	 * 
	 * @param adminId
	 * @throws BusinessException
	 */
	public void generateGovform(V3xOrgMember admin) throws BusinessException;
	
	/**
	 * 
	 * @param evt
	 * @throws BusinessException
	 */
	public void deleteEdocObjTeam(V3xOrgDepartment department)throws BusinessException;
	
}
