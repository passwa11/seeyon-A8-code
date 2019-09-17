/**
 * 
 */
package com.seeyon.v3x.edoc.manager;

import java.util.List;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.v3x.edoc.domain.EdocMarkAcl;

/**
 * 类描述：
 * 创建日期：
 *
 * @author liaoj
 * @version 1.0 
 * @since JDK 5.0
 */
public interface EdocMarkAclManager {
	
	/**
	 * 方法描述：保存公文文号使用授权
	 */
	public void saveMarkAcl(List<EdocMarkAcl> edocMarkAclList);	
	
	/**
	 * 方法描述：根据公文文号定义ID查询公文文号使用授权的单位
	 */
	public List<V3xOrgDepartment> queryMarkAclById(Long edocMarkDefinitionsId) throws BusinessException;
	
	/**
	 * 根据文号定义ID查询文号授权的对象
	 * @param edocMarkDefinitionId
	 * @return
	 * @throws BusinessException
	 */
	public List<EdocMarkAcl> getMarkAclById(Long edocMarkDefinitionId)throws BusinessException;
	
	/**
	 * 
	 * @param edocMarkAclList
	 */
	public void deleteMarkAcl(List<EdocMarkAcl> edocMarkAclList);
	
	/**
	 * 
	 * @param defId
	 */
	public void deleteByDefId(Long defId);
	
}
