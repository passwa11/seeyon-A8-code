/**
 * EdocElementManager.java
 * Created on 2007-4-19
 */
package com.seeyon.v3x.edoc.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.edoc.bo.EdocElementBO;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.domain.EdocFormElement;
 
/**
 *
 * @author <a href="mailto:handy@seeyon.com">Han Dongyou</a>
 *
 */
public interface EdocElementManager
{        
    
    /**
     * 更新公文元素属性。
     * @param element
     */
    public void updateEdocElement(EdocElement element);  
    
    /**
     * 根据ID返回指定的公文元素对象。
     * @param elementId 公文元素ID
     * @return 公文元素对象
     */
    public EdocElement getEdocElement(String elementId);
    
    /**
     * 返回所有已启用的公文元素列表。
     *  
     * @return List
     */
    public List<EdocElement> getEdocElements();
    /**
     * 返回所有公文元素条目数。
     * 
     * @return int
     */
    public int getAllEdocElementCount(long accountId) ;
    /**
     * 返回所有公文元素条目数。
     * 
     * @return int
     */
    public int getAllEdocElementCount();
    
    /**
     * 返回所有公文元素列表。
     * 
     * @param startIndex 显示页数
     * @param numResults 每页显示条目数
     * 
     * @return List
     */
    public List<EdocElement> getAllEdocElements(int startIndex, int numResults);
    public List<EdocElement> getAllEdocElements();
    /**
     * 返回指定状态的公文元素条目数。
     * 
     * @param status 公文元素状态
     * @return int
     */
    public int getEdocElementCount(int status);
    
    /**
     * 返回指定状态的公文元素列表。
     * 
     * @param status 公文元素状态
     * @return List
     */
    public List<EdocElement> getEdocElementsByStatus(int status, int startIndex, int numResults);
    public List<EdocElement> getEdocElementsByStatus(int status);   
    /**
     * 返回指定 Id的公文元素
     * add by lindb
     * @param id
     * @return
     */
    public EdocElement getEdocElementsById(long id);
    
    public long getIdByFieldName(String fieldName);
    
    public EdocElement getByFieldName(String fieldName);
    
    public EdocElement getByFieldName(String fieldName,Long userAccountId ) ;
    
    public void initCmpElement();
    
    /**
     * 根据状态和类别（数字，字符，意见...）返回公文元素集合
     * @param status
     * @param type
     * @return
     */
    public List<EdocElement> getByStatusAndType(int status, int type);
    
    /**
     * 检查枚举值是否被引用
     * @param domainId：单位ID
     * @param metadataId
     * @return 引用字段名称 “”：没有引用；
     */
    public String getRefMetadataFieldName(Long domainId,Long metadataId);
    
    /**
     * 通过单位取得
     * @param accountId
     * @return
     */
    public List<EdocElement> listElementByAccount(Long accountId);

    
    /**
     * 根据查询条件，取得公文元素集合， 带分页
     * @param condition 查询条件类型
     * @param textfield 输入的查询条件值
     * @param statusSelect 选择的公文状态
     * @param paginationFlag 分页标志符 1 分页； 0 不分页
     * @return
     */
	public List<EdocElement> getEdocElementsByContidion(String condition,
			String textfield, String statusSelect, int paginationFlag);
	/**
     * 根据状态查询可以使用的公文元素（文档中心使用）
     * @param status
     * @return
     */
    public List<EdocElement> getEdocElementsByStatusForDoc(long accountId, int status);
    
    
    /**
     * 根据公文元素id获取公文元素（文档中心使用）（并返还公文元素字段对应的实体类和属性名）
     * @param id(公文元素id)
     * @return
     */
    public EdocElement  getEdocElementByIdForDoc(Long id );
    
	/**
	 * 根据枚举id 查询枚举值(文档中心使用)
	 * @param id：枚举id
	 * @return
	 */
	public List<CtpEnumItem> getDocElementEnumListForDoc(Long parentEnumId);
	/**
	 * 根据po属性值获取edoc_element对象
	 * @param poFiledName
	 * @return
	 */
   public EdocElement getEdocElementByPoFiledName(String poFiledName);

	
   public void transCopyGroupElement2NewAccout(Long accountId);
   
   public List<EdocFormElement> getEdocFormElementByElementIdAndFormId(Long elementId, Long formId);
	
	/**
	 * 缓存公文元素-供公文单调用
	 * @param domainId
	 */
	public void loadEdocElementByDomainId(Long domainId);
	/**
	 * 计算数据库中的公文元素
	 * @param domainId
	 * @return
	 */
	public int countEdocElementsFromDB(Long domainId) ;
	
	
	/**
	 * 修复公文元素，   元素缺少的情况， 这个输入修复方法，非业务方法
	 * 
	 * @param fixAccountId 需要修复的单位ID， 如果不传值则修复所有单位的元素，并加载到缓存中
	 * @return
	 *
	 * @Since A8-V5 6.1
	 * @Author      : xuqw
	 * @Date        : 2017年4月20日下午1:33:33
	 *
	 */
	public String fixElements(Long fixAccountId);
	
	public EdocElement getByFieldName4upgrade(String fieldName,Long userAccountId);

	public List<EdocElement> getEdocElementsByGovdocRightView(Map<String, String> params);

	public List<EdocElementBO> getEdocElementsByAccount(Long accountId);
	
}
