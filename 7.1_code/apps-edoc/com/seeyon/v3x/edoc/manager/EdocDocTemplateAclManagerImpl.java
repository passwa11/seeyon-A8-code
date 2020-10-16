package com.seeyon.v3x.edoc.manager;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.v3x.edoc.dao.EdocDocTemplateAclDao;
import com.seeyon.v3x.edoc.domain.EdocDocTemplateAcl;

public class EdocDocTemplateAclManagerImpl implements EdocDocTemplateAclManager{
	private static final Log log = LogFactory.getLog(EdocDocTemplateAclManagerImpl.class);

	private EdocDocTemplateAclDao edocDocTemplateAclDao;
	private OrgManager orgManager;
	
	public OrgManager getOrgManager() {
		return orgManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public EdocDocTemplateAclDao getEdocDocTemplateAclDao() {
		return edocDocTemplateAclDao;
	}

	public void setEdocDocTemplateAclDao(EdocDocTemplateAclDao edocDocTemplateAclDao) {
		this.edocDocTemplateAclDao = edocDocTemplateAclDao;
	}

	public List<EdocDocTemplateAcl> getEdocDocTemplateAcl(String templateId){
		return edocDocTemplateAclDao.findEdocDocTemplateAcl(templateId);
	}
	
	public void saveEdocDocTemplateAcl(EdocDocTemplateAcl edocDocTemplateAcl){
		edocDocTemplateAclDao.saveEdocDocTemplateAcl(edocDocTemplateAcl);
	}
	public void saveEdocDocTemplateAcl(Long id,Long templateId,String[] departmentIds) throws Exception{
		
		for(String deptId:departmentIds){
			EdocDocTemplateAcl edocDocTemplateAcl = new EdocDocTemplateAcl();
			edocDocTemplateAcl.setIdIfNew();
			edocDocTemplateAcl.setTemplateId(templateId);
			String[] bDeptId = deptId.split("\\|");
			edocDocTemplateAcl.setDepType(bDeptId[0]);	
			edocDocTemplateAcl.setDepId(Long.valueOf(bDeptId[1]));
			
			edocDocTemplateAclDao.saveEdocDocTemplateAcl(edocDocTemplateAcl);
			}
	}
	
	public void updateEdocDocTemplateAcl(Long id,Long templateId,String[] departmentIds) throws Exception{
		
		edocDocTemplateAclDao.deleteAclByTemplateId(templateId);
		
		for(String deptId:departmentIds){
			EdocDocTemplateAcl edocDocTemplateAcl = new EdocDocTemplateAcl();
			edocDocTemplateAcl.setIdIfNew();
			edocDocTemplateAcl.setTemplateId(templateId);
			String[] bDeptId = deptId.split("\\|");
			edocDocTemplateAcl.setDepType(bDeptId[0]);	
			edocDocTemplateAcl.setDepId(Long.valueOf(bDeptId[1]));
			
			edocDocTemplateAclDao.saveEdocDocTemplateAcl(edocDocTemplateAcl);
			}		
	}
	
	public void deleteAclByTemplateId(Long templateId){
		edocDocTemplateAclDao.deleteAclByTemplateId(templateId);
	}
}
