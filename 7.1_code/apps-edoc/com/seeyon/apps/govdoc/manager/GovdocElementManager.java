package com.seeyon.apps.govdoc.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.edoc.domain.EdocElement;

/**
 * 新公文元素接口
 * @author 唐桂林
 *
 */
public interface GovdocElementManager {
	
	
	/**
	 * ajax 查询列表
	 * @param flipInfo
	 * @param condition
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo findList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException;

	/**
     * 根据查询条件，取得公文元素集合， 带分页
     * @param condition 查询条件类型
     * @param textfield 输入的查询条件值
     * @param statusSelect 选择的公文状态
     * @param paginationFlag 分页标志符 1 分页； 0 不分页
     * @return
     */
	public List<EdocElement> getEdocElementsByContidion(String condition, String textfield, String statusSelect, int paginationFlag) throws BusinessException;
	
	/**
     * 返回所有公文元素条目数。
     * @return int
     */
    public int getAllEdocElementCount() throws BusinessException;
    
    /**
     * 返回所有公文元素列表。
     * @param startIndex 显示页数
     * @param numResults 每页显示条目数
     * @return List
     */
    public List<EdocElement> getAllEdocElements(int startIndex, int numResults) throws BusinessException;
    
    /**
     * 获取某单位下的公文元素
     * @param domainId
     * @return
     */
    public List<EdocElement> listElementByAccount(Long domainId) throws BusinessException;
    
    /**
     * 获取某单位下的公文元素、启用的公文元素
     * @param accountId
     * @param status
     * @return
     * @throws BusinessException
     */
    public List<EdocElement> getEdocElementsByStatusForDoc(long accountId, int status) throws BusinessException;
    
    /**
     * 
     * @param status
     * @return
     * @throws BusinessException
     */
    public Map<String, String> getEdocElementFieldNames(Long domainId) throws BusinessException;
    
    /**
     * 根据ID返回指定的公文元素对象。
     * @param elementId 公文元素ID
     * @return 公文元素对象
     */
    public EdocElement getEdocElement(String elementId) throws BusinessException;
    
    /**
     * 更新公文元素属性。
     * @param element
     */
    public void updateEdocElement(EdocElement po) throws BusinessException;
    
    /**
     * 获取所有公文元素
     * @return
     */
    public List<EdocElement> getAllEdocElements() throws BusinessException;
    
    public EdocElement getByFieldName(String fieldName);
    public EdocElement getByFieldName(String fieldName, Long domainId);
	
    /**
	 * 新建单位时，制复公文元素
	 * @param domainId
	 * @return
	 */
    public void transGenerateElement(Long domainId) throws BusinessException;
	
}
