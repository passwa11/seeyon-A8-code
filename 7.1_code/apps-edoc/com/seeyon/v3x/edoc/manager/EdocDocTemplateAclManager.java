package com.seeyon.v3x.edoc.manager;

import java.util.List;

import com.seeyon.v3x.edoc.domain.EdocDocTemplateAcl;



public interface EdocDocTemplateAclManager {
	
	public List<EdocDocTemplateAcl> getEdocDocTemplateAcl(String templateId);
	
	public void saveEdocDocTemplateAcl(EdocDocTemplateAcl templateAcl);
	
	public void saveEdocDocTemplateAcl(Long id,Long templateId,String[] departmentIds) throws Exception;
	
	public void updateEdocDocTemplateAcl(Long id,Long templateId,String[] departmentIds) throws Exception;
	
	public void deleteAclByTemplateId(Long templateId);
	
}
