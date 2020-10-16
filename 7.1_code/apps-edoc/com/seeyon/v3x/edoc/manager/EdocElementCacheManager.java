package com.seeyon.v3x.edoc.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.AbstractSystemInitializer;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.cache.CacheAccessable;
import com.seeyon.ctp.common.cache.CacheFactory;
import com.seeyon.ctp.common.cache.CacheMap;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.dao.EdocElementDao;
import com.seeyon.v3x.edoc.domain.EdocElement;


public class EdocElementCacheManager extends AbstractSystemInitializer{

	public static Long groupDomainId = 0L;
	
	private EdocElementDao edocElementDao;
    
	private static CacheMap<Long, EdocElement> elementIdTable = null;
	private static CacheMap<String, EdocElement> elementTable = null;
    private static CacheMap<String, EdocElement> fieldElementTable = null;
    private static CacheMap<Long, ArrayList<EdocElement>> domainElementsTable = null;
    
    /** 获取缓存下某单位的公文元素 */
    public List<EdocElement> getEdocElementCacheListByDomainId(Long domainId) {
    	initEdocElementCache(domainId);
    	List<EdocElement> list = domainElementsTable.get(domainId);
    	List<EdocElement> cmpList = new ArrayList<EdocElement>();
		for (EdocElement element : list) {
			if(element.getType() == 7 || (element.getType() == 6 && !element.getIsSystem()) || (element.getIsSystem() && element.getFieldName().indexOf("2") > -1)){
				continue;
			}
            cmpList.add(element);
		}
		return cmpList;
    }
    
    /** 获取缓存下单个公文元素-通过ID */
    public EdocElement getEdocElementCacheById(Long id) {
    	initEdocElementCache();
    	return elementIdTable.get(id);
    }
    
    /** 获取缓存下单个公文元素-通过elementId和单位ID */
    public EdocElement getEdocElementCacheByElementId(String elementId, Long domainId) {
    	initEdocElementCache(domainId);
    	return elementTable.get(elementId + domainId);
    }
    
    /** 获取缓存下单个公文元素-通过fieldName和单位ID */
    public EdocElement getEdocElementCacheByFieldName(String fieldName, Long domainId) {
    	initEdocElementCache(domainId);
    	return fieldElementTable.get(fieldName + domainId);
    }
    
    /** 更新缓存中单个公文元素 */
    public void updateEdocElementCache(EdocElement tempElement) {
    	if(tempElement != null) {
    		initEdocElementCache(tempElement.getDomainId());
    		List<EdocElement> elementList = domainElementsTable.get(tempElement.getDomainId());
    		if(Strings.isNotEmpty(elementList)) {
		    	for (int i = 0; i < elementList.size(); i++) {
		    	    EdocElement element = elementList.get(i);
		    		if(element.getId().longValue() == tempElement.getId().longValue()) {
		    			elementList.set(i, tempElement);
			            break;
		    		}
		        }
		    	fieldElementTable.put(tempElement.getFieldName() + tempElement.getDomainId(), tempElement);
		    	elementTable.put(tempElement.getElementId() + tempElement.getDomainId(), tempElement);
		    	elementIdTable.put(tempElement.getId(), tempElement);
		    	domainElementsTable.put(tempElement.getDomainId(), (ArrayList<EdocElement>)elementList);
    		}
    	}
    }
    
    /** 批量将公文元素添加到缓存中 */
    public void addEdocElementCache(Long domainId, List<EdocElement> addElementList) {
    	if(Strings.isNotEmpty(addElementList)) {
	    	for (EdocElement element : addElementList) {
	    		fieldElementTable.put(element.getFieldName() + element.getDomainId(), element);
	            elementTable.put(element.getElementId() + element.getDomainId(), element);
	            elementIdTable.put(element.getId(), element);
	        }
	    	if(domainElementsTable.get(domainId) != null) {
	    		domainElementsTable.get(domainId).addAll(addElementList);
	    	} else {
	    		domainElementsTable.put(domainId, (ArrayList<EdocElement>)addElementList);
	    	}
    	}
    }
    
    public synchronized void loadEdocElementCache(Long domainId) {
    	initEdocElementCache(domainId);
    }
	
	/**
     * 刷新单位元素缓存
     * 
     * @param domainId
     *
     * @Since A8-V5 6.1
     * @Author      : xuqw
     * @Date        : 2017年5月3日下午10:44:48
     *
     */
    public synchronized void refreshEdocElementCache(Long domainId){
        
        List<EdocElement> elementList = getEdocElementCacheListByDomainId(domainId);
        if(Strings.isNotEmpty(elementList)){
            for (EdocElement element : elementList) {
                elementIdTable.remove(element.getId());
            }
        }
        domainElementsTable.remove(domainId);
        
        //重新从数据库初始化
        initEdocElementCache(domainId);
    }
    
    /***************************** 方法仅供内部使用 start ******************************************/
    /** 缓存集团公文元素,  manager 的 init方法调用!! */
    @SuppressWarnings("unused")
	public void initialize() {
    	CacheAccessable factory = CacheFactory.getInstance(EdocElementManager.class);
    	fieldElementTable = factory.createMap("fieldElementTable");
    	elementTable = factory.createMap("elementTable");
        elementIdTable = factory.createMap("elementIdTable");
        domainElementsTable = factory.createMap("domainElementsTable");
        initEdocElementCache(groupDomainId);
    }
    
    /** 缓存当前单位公文元素 */
    private void initEdocElementCache() {
    	initEdocElementCache(AppContext.getCurrentUser().getLoginAccount());
    }
    /** 缓存某单位公文元素 */
    private void initEdocElementCache(Long domainId) {
    	if(domainElementsTable.get(domainId) == null) {
	    	List<EdocElement> elementList = getEdocElementListByDomainId(domainId);
	    	putEdocElementCache(domainId, elementList);
    	}
    }
    /** 批量缓存某单位公文元素 */
    private void putEdocElementCache(Long domainId, List<EdocElement> elementList) {
    	if(Strings.isNotEmpty(elementList)) {
	    	for (EdocElement element : elementList) {
	    		fieldElementTable.put(element.getFieldName() + element.getDomainId(), element);
	            elementTable.put(element.getElementId() + element.getDomainId(), element);
	            elementIdTable.put(element.getId(), element);
	        }
	    	domainElementsTable.put(domainId, (ArrayList<EdocElement>)elementList);
    	}
    }
    /** 数据库里获取某单位公文元素集合 */
	private List<EdocElement> getEdocElementListByDomainId(Long domainId) {
    	return (List<EdocElement>)edocElementDao.getEdocElementListByDomainId(domainId);
    }
	/***************************** 方法仅供内部使用  end ******************************************/
	
    public void setEdocElementDao(EdocElementDao edocElementDao) {
		this.edocElementDao = edocElementDao;
	}	
	
    public List<EdocElement> getEdocElementsByGovdocRightView(long domainId, Map<String, String> params) {
		initEdocElementCache(domainId);
    	List<EdocElement> list = domainElementsTable.get(domainId);
    	List<EdocElement> cmpList = new ArrayList<EdocElement>();
		for (EdocElement element : list) {
			if(element.getType() == 7 || element.getType() == 6 || element.getType() == 3 || (element.getIsSystem() && element.getFieldName().indexOf("2") > -1)){
				continue;
			}
			//TODO 标准产品需要澄清功能     右侧元素，放开标题， 文号
			if("attachments".equals(element.getFieldName())
					//|| element.getFieldName().equals("doc_mark")
					//|| element.getFieldName().equals("serial_no")
					//|| element.getFieldName().equals("sign_mark")
					|| "feedback".equals(element.getFieldName())
					|| "baopiyijian".equals(element.getFieldName())
					|| "nibanyijian".equals(element.getFieldName())
					|| "sign_reply".equals(element.getFieldName())
					|| "createdate".equals(element.getFieldName())
					//|| element.getFieldName().equals("subject")
					){
				continue;
			}
            cmpList.add(element);
		}
		return cmpList;
	}
    
}
