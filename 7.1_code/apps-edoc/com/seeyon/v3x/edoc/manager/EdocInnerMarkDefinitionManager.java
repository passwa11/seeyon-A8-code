package com.seeyon.v3x.edoc.manager;

import java.util.List;

import com.seeyon.v3x.edoc.domain.EdocInnerMarkDefinition;

public interface EdocInnerMarkDefinitionManager {
	public Boolean isUnificationType(Long id);
	
	public void create(EdocInnerMarkDefinition mark);
		
	public void deleteAll(Long domainId);
		
//	public String getInnerMarkDefinition(int type,Long domainId);
	
	/**
	 * 获取一个内部文号。
	 * @param edocType 公文类型：发文|收文|签报
	 * @param domainId 单位id
	 * @param incremental 是否将内部文号的当前编号增1
	 * @return 内部文号
	 */
	public String getInnerMark(Integer edocType, Long domainId, boolean incremental);
	
	/**
	 * 获取一个内部文号（拟文、登记、新建签报时调用该方法）。
	 * @param edocType 公文类型：发文|收文|签报
	 * @param domainId 单位id
	 * @return 内部文号
	 */
	public String getInnerMark(Integer edocType, Long domainId);
	
	/**
	 * 获得内部文号，根据单位Id
	 * @param domainId
	 * @return
	 */
	public List<EdocInnerMarkDefinition> getEdocInnerMarkDefsList(long domainId);
	
	public List<EdocInnerMarkDefinition> getEdocInnerMarkDefs(int type, long domainId);
	
	/**
	 * 获得内部文号的设置状态：0-未设置；1-统一内部文号；2-独立内部文号
	 * @param domainId 单位id
	 * @return int
	 */
	public int getInnerMarkStatus(long domainId);
	
	/**
	 * 设置内部文号时，修改内存中的内部文号状态。
	 * @param domainId 单位id
	 * @param status 内部文号状态
	 */
	public void setInnerMarkStatus(long domainId, int status);
	
}
