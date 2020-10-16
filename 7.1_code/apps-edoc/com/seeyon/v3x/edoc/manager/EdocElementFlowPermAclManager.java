package com.seeyon.v3x.edoc.manager;

import java.util.List;
import java.util.Hashtable;

import com.seeyon.v3x.edoc.dao.EdocElementFlowPermAclDao;
import com.seeyon.v3x.edoc.domain.EdocElementFlowPermAcl;

public interface EdocElementFlowPermAclManager {
	
	public void saveEdocElementFlowPermAcls(List<EdocElementFlowPermAcl> list);
	
	public void updateEdocElementFlowPermAcl(Long id,Integer access);
	
	public List<EdocElementFlowPermAcl> getEdocElementFlowPermAcls(Long flowPermId);
	
	/**
	 * 得到公文处理时,对公文元素的操作权限
	 * @param flowPermId 如果<0,直接返回null
	 * @return
	 */
	public Hashtable<Long,EdocElementFlowPermAcl> getEdocElementFlowPermAclsHs(Long flowPermId);
	
	public EdocElementFlowPermAcl getEdocElementFlowPermAcl(Long id);
	
	public void deleteEdocElementFlowPermAcl(Long flowPermId);
}
