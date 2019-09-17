/**
 * 
 */
package com.seeyon.v3x.edoc.manager;

import java.util.List;

import com.seeyon.apps.edoc.enums.EdocEnum.MarkCategory;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.v3x.edoc.domain.EdocMarkCategory;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.webmodel.EdocMarkModel;
import com.seeyon.v3x.edoc.webmodel.EdocMarkNoModel;

/**
 * 类描述：
 * 创建日期：
 *
 * @author liaoj
 * @version 1.0 
 * @since JDK 5.0
 */
public interface EdocMarkDefinitionManager {
	
	/**
	 * 根据id返回公文文号定义。
	 * @param id 公文文号定义id
	 * @return EdocMarkDefinition
	 */
	public String checkExistEdocMarkDefinition(String id);
	
	/**
	 * 根据id返回公文文号定义。
	 * @param id 公文文号定义id
	 * @return EdocMarkDefinition
	 */
	public EdocMarkDefinition getMarkDefinition(long id);

	/**
     * 方法描述：保存公文文号定义
     */
	public void saveMarkDefinition(EdocMarkDefinition edocMarkDefinition) ;
	
	/**
     * 方法描述：修改公文文号定义
     */
	public void updateMarkDefinition(EdocMarkDefinition edocMarkDefinition);
	
	/**
	 * 修改公文文号发布状态
	 * @param markDefId
	 * @param status
	 */
	public void updateMarkDefStatus(Long markDefId, short status);
	
   	/**
     * 方法描述：删除公文文号定义
     */
	public void deleteMarkDefinition(EdocMarkDefinition edocMarkDefinition);
	
	/**
	 * 设置文号定义已经使用
	 * @param markDefId
	 */
	public void setEdocMarkDefinitionUsed(Long markDefId) ;
	/**
     * 方法描述：根据公文文号定义ID查询公文文号定义
     */
	public EdocMarkDefinition queryMarkDefinitionById(Long edocMarkDefinitionId);
	
	public List<EdocMarkDefinition> queryMarkDefinitionListById(List<Long> edocMarkDefinitionIdList);
	
//	/**
//     * 方法描述：根据公文字号查询公文文号定义
//     */
//	public List<String> queryMarksByWordNo (String wordNo);
	
//	/**
//     * 方法描述：查询全部公文文号
//     */
//	public List<EdocMarkModel> queryAllMarkDefinitions () throws BusinessException ;
	
	/**
	 * 方法描述：保存公文文号定义，同时保存公文文号类型和文号授权
	 */
	public void saveMarkDefinition(EdocMarkDefinition def,EdocMarkCategory cat);
	
	/**
	 * 获取某单位公文文号及内部编号机构字
	 * @param domainId
	 * @return
	 */
	public List<EdocMarkModel> findEdocMarkAndSerinalDefList(Long domainId) throws BusinessException;
	
	public List<EdocMarkModel> getEdocMarkDefs(Long domainId, Long depId, String condition, String textfield) throws BusinessException;
	
	/*客开 项目名称：贵州市政府-G6V580省级专版 作者：mtech 修改日期：2017-08-11 [修改功能：公文库-机构代字]start*/
	public List<EdocMarkDefinition> getEdocDocMarkDefinitions(Integer markType) throws BusinessException;
	/*客开 项目名称：贵州市政府-G6V580省级专版 作者：mtech 修改日期：2017-08-11 [修改功能：公文库-机构代字]end*/
	
	/**
	 * 根据授权部门查找公文文号定义。
	 * @param deptIds  文号授权部门id（以,号分隔）
	 * @return List<EdocMarkModel>
	 */
	public List<EdocMarkModel> getEdocMarkDefinitions(String deptIds,int markType);
	public List<EdocMarkModel> getEdocMarkDefinitions(String deptIds,int markType,Long markDefId);
	
	/**
	 * 查找模板是否绑定了某种的字号
	 * @param templeteId  ：公文模板
	 * @param category ： 分类
	 * @return
	 */
	public EdocMarkModel getEdocMarkByTempleteId(Long templeteId,MarkCategory category);
	public EdocMarkModel  getEdocMarkDefinitionById(Long definitionId);

	/**
	 * 解析公文文号
	 * @param docMark
	 * @param markDef
	 * @return
	 */
	public EdocMarkNoModel analyzeEdocMarkVo(String docMark, EdocMarkDefinition markDef);
	
	public Short judgeStreamType(Long definitionId)throws BusinessException;
	
//	/**
//	 * 根据文号定义和当前编号，返回该文号定义对应的当前文号。
//	 * @param definitionId
//	 * @param currentNo
//	 * @return String
//	 */
//	public String getEdocMark(long definitionId, Integer currentNo);
	/**
	 * 判断公文文号定义是否存在
	 */
	public int judgeEdocDefinitionExsit(Long definitionId);
	/**
	 * 是否包含指定字号的公文文号（新建公文文号时调用）。
	 * @param wordNo 字号
	 * @param domainId 单位id
	 * @return Boolean
	 */
	public Boolean containEdocMarkDefinition(String wordNo, long domainId,int markType);
	
	/**
	 * 是否包含指定字号的公文文号（修改公文文号时调用）。
	 * @param markDefId 公文文号定义id
	 * @param wordNo 字号
	 * @param domainId 单位id
	 * @return Boolean
	 */
	public Boolean containEdocMarkDefinition(long markDefId, String wordNo, long domainId,int markType);
	
	/**
	 * 判断在指定文号类别中是否包含公文文号定义。
	 * @param categoryId
	 * @return Boolean
	 */
	public boolean containEdocMarkDefInCategory(long categoryId);
	
	/**
	 * 逻辑删除文号定义记录（将状态置为已删除）
	 * @param defId  文号定义Id
	 * @param status 状态
	 */
	public void logicalDeleteMarkDefinition(long defId, short status);
	
	public List<EdocMarkDefinition> getEdocMarkDefinitionsByCategory(Long categoryId);
	
	public EdocMarkModel markDef2Mode(EdocMarkDefinition markDef,String yearNo,Integer currentNo);
	public EdocMarkModel markDef2Mode(EdocMarkDefinition markDef,Integer currentNo);
	public EdocMarkModel markDef2Mode(Long markDefId);
	  /**
     * 将EdocMarkCategory自增长
     * @param markDefinitionId
     */
    public void setEdocMarkCategoryIncrement(Long markDefinitionId); 
    
    public boolean isEdocMarkAclByDefinitionId(User user, EdocMarkDefinition edocMarkDefinition) throws Exception;
    
    public boolean isEdocMarkAclByDefinitionId(User user, EdocMarkDefinition edocMarkDefinition,long templateOrgAccountId) throws Exception;
    
    /**
     * 统计本单位的文号数量
     * @param accountId
     * @return
     * @throws BusinessException
     */
    public Integer getAccountMarkCount(Long accountId) throws BusinessException;
    
}
