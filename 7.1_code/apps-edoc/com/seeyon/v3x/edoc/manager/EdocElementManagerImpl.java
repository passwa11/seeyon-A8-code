/**
 * EdocElementManagerImpl.java
 * Created on 2007-4-19
 */
package com.seeyon.v3x.edoc.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.apps.edoc.bo.EdocElementBO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.v3x.common.dao.paginate.Pagination;
import com.seeyon.v3x.edoc.constants.EdocElementConstants;
import com.seeyon.v3x.edoc.consts.EdocElementEnum;
import com.seeyon.v3x.edoc.dao.EdocElementDao;
import com.seeyon.v3x.edoc.dao.EdocFormElementDao;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.domain.EdocFormElement;
import com.seeyon.v3x.edoc.util.DataTransUtil;

/**
 *
 * @author <a href="mailto:handy@seeyon.com">Han Dongyou</a>
 *
 */
public class EdocElementManagerImpl implements EdocElementManager {
    
    private static final Log LOG = CtpLogFactory.getLog(EdocElementManagerImpl.class);
    
	/**
     * 将 EdocSummary对应的属性名转换成 公文元素中的file_name
     * @param filedName
     */
    private static final  String filedNames[] = {"packdate","createdate","doc_mark","serial_no","create_person","send_unit","send_to","copy_to","report_to","print_unit","doc_mark2","send_to2","copy_to2","report_to2","send_unit2","send_department","send_department2","signing_date","receipt_date","registration_date","doc_type","send_type","secret_level","urgent_level","keep_period","unit_level"};
    private static final   String poFieldNames[] = {"packTime","createTime","docMark","serialNo","createPerson","sendUnit","sendTo","copyTo","reportTo","printUnit","docMark2","sendTo2","copyTo2","reportTo2","sendUnit2","sendDepartment","sendDepartment2","signingDate","receiptDate","registrationDate","docType","sendType","secretLevel","urgentLevel","keepPeriod","unitLevel"};
	
    private EdocElementDao edocElementDao;
    private EdocFormElementDao edocFormElementDao;
    private EdocElementCacheManager edocElementCacheManager;
    private EnumManager enumManagerNew;
    
    
    public void setEnumManagerNew(EnumManager enumManagerNew) {
		this.enumManagerNew = enumManagerNew;
	}

	/**
     * 构造函数。
     *
     */
    public EdocElementManagerImpl(){        
    }

    public void updateEdocElement(EdocElement element) {      
    	edocElementDao.update(element);
        edocElementCacheManager.updateEdocElementCache(element);
    }
    
    public EdocElement getEdocElement(String elementId){
    	Long domainId = AppContext.currentAccountId();
        if(AppContext.getCurrentUser().isGroupAdmin()) { 
        	domainId = EdocElementCacheManager.groupDomainId;
        } 
        return edocElementCacheManager.getEdocElementCacheByElementId(elementId, domainId);
    }

    public int getAllEdocElementCount() {
    	List<EdocElement> list = edocElementCacheManager.getEdocElementCacheListByDomainId(AppContext.getCurrentUser().getLoginAccount());
    	if(Strings.isNotEmpty(list)) {
    		return list.size();
    	}
    	return 0;
    }
    public int getAllEdocElementCount(long accountId) {
    	List<EdocElement> list = edocElementCacheManager.getEdocElementCacheListByDomainId(accountId);
    	if(Strings.isNotEmpty(list)) {
    		return list.size();
    	}
    	return 0;
    }
    public List<EdocElement> getAllEdocElements(int startIndex, int numResults){
    	List<EdocElement> _elements = new ArrayList<EdocElement>();
        List<EdocElement> allElements = edocElementCacheManager.getEdocElementCacheListByDomainId(AppContext.currentAccountId());
        for (int i = 0; i < allElements.size(); i++) {
        	if ( (i >= (startIndex - 1) * numResults) && (i < startIndex * numResults) ) {
				_elements.add(allElements.get(i));		
			}
        }
        return _elements;
    }  
    
    public List<EdocElement> getAllEdocElements() {
    	return edocElementCacheManager.getEdocElementCacheListByDomainId(AppContext.currentAccountId());
    }
    
    public int getEdocElementCount(int status){
    	List<EdocElement> allElements = edocElementCacheManager.getEdocElementCacheListByDomainId(AppContext.currentAccountId());
    	int count = 0;    	
    	for (int i = 0; i < allElements.size(); i++) {
    		EdocElement element = (EdocElement)allElements.get(i);
    		if (element.getStatus() == status) {
    			count++;
    		}
    	}
    	return count;
    }

    public List<EdocElement> getEdocElementsByStatus(int status, int startIndex, int numResults) {
    	List<EdocElement> _elements = new ArrayList<EdocElement>();
    	List<EdocElement> allElements = edocElementCacheManager.getEdocElementCacheListByDomainId(AppContext.currentAccountId());
    	int j = 0;
    	for (int i = 0; i < allElements.size(); i++) {
    		EdocElement element = (EdocElement)allElements.get(i);
    		if (element.getStatus() == status) {
    			if ( (j >= (startIndex - 1) * numResults) && (j < startIndex * numResults) ) {
    				_elements.add(element);    				
    			}
    			j++;
    		}
    	}
    	return _elements;
    }
    
    public List<EdocElement> getEdocElementsByStatus(int status) {
    	List<EdocElement> _elements = new ArrayList<EdocElement>();
    	List<EdocElement> allElements = edocElementCacheManager.getEdocElementCacheListByDomainId(AppContext.currentAccountId());
    	for (int i = 0; i < allElements.size(); i++) {
    		EdocElement element = (EdocElement)allElements.get(i);
    		if (element.getStatus() == status) {
    			_elements.add(element);    				
    		}
    	}
    	return _elements;
    }
    
    public EdocElement getEdocElementsById(long id){
    	return edocElementCacheManager.getEdocElementCacheById(id);
    }
    
    
    /**
     * add by lindb
     * accroding field_name to get id.
     */
    public long getIdByFieldName(String fieldName){
    	return Long.parseLong(getByFieldName(fieldName).getElementId());
    }
    
    public EdocElement getByFieldName(String fieldName) {
    	Long domianId = AppContext.currentAccountId();
        if(AppContext.getCurrentUser().isGroupAdmin()) {
        	domianId = EdocElementCacheManager.groupDomainId;
        }
        return edocElementCacheManager.getEdocElementCacheByFieldName(fieldName, domianId);
    }
    /**
     * getByFieldName
     * fieldName 列表元素的名称
     * userAccountId 发文人的单位ID
     */
    public EdocElement getByFieldName(String fieldName, Long userAccountId) {
    	Long domainId = AppContext.currentAccountId();
        if(AppContext.getCurrentUser().isGroupAdmin()) {
        	domainId = EdocElementCacheManager.groupDomainId;
        } else if(userAccountId != null) {
        	domainId = userAccountId;
        }
        return edocElementCacheManager.getEdocElementCacheByFieldName(fieldName, domainId);		
    }    
    
    public List<EdocElement> listElementByAccount(Long accountId){
    	return edocElementCacheManager.getEdocElementCacheListByDomainId(accountId);
    }
    
    public List<EdocElement> getByStatusAndType(int status, int type) {
    	List<EdocElement> _elements = new ArrayList<EdocElement>();
    	List<EdocElement> allElements = edocElementCacheManager.getEdocElementCacheListByDomainId(AppContext.getCurrentUser().getLoginAccount());
    	for (int i = 0; i < allElements.size(); i++) {
    		EdocElement element = (EdocElement)allElements.get(i);
    		if (element.getStatus() == status && element.getType() == type) {
    			_elements.add(element);    				
    		}
    	}
    	return _elements;
    }
    
    /**
     * 取得满足手动输入过滤条件的数据数
     * @param condition
     * @param textfield
     * @return
     */
    private Integer getAllEdocElementCount(String condition, String textfield) {
    	List<EdocElement> list = edocElementCacheManager.getEdocElementCacheListByDomainId(AppContext.currentAccountId());
    	if(Strings.isNotEmpty(list)) {
    		return list.size();
    	}
    	return 0;
    }
    
	public List<EdocElement> getEdocElementsByContidion(String condition,
			String textfield, String statusSelect, int paginationFlag) {
	    
		List<EdocElement> list = null;
		Integer startIndex = 0;
		Integer first = 0;
		Integer pageSize = 0;
		Integer listCount = 0;
		
		List<EdocElement> alls = edocElementCacheManager.getEdocElementCacheListByDomainId(AppContext.currentAccountId());
    	
		//计算listcount
		if (("elementStatus".equals(condition)) && (Strings.isNotBlank(statusSelect))) {
			//状态
			listCount = this.getEdocElementCount(Integer.parseInt(statusSelect));
		}else {
			//取得符合查询条件的总记录数
			listCount = this.getAllEdocElementCount(condition,textfield);
		}
		//计算分页信息
		Pagination.setRowCount(listCount);
		first = Pagination.getFirstResult();
		pageSize = Pagination.getMaxResults();
		if ((first + 1) % pageSize == 0){
			startIndex = first / pageSize;
		}
		else{
			startIndex = first / pageSize + 1;
		}
		if (pageSize == 1){ 
			startIndex = (first+1) / pageSize;
		}
		
		if (("elementStatus".equals(condition)) && (Strings.isNotBlank(statusSelect))) {
			if(paginationFlag == 1){
				//根据公文元素状态进行过滤，带分页
				list = this.getEdocElementsByStatus(Integer.parseInt(statusSelect),startIndex,pageSize);
			}
			else{
				//根据公文元素状态进行过滤，不带分页
				if("1".equals(statusSelect)){
					list = this.getEdocElementsByStatus(EdocElement.C_iStatus_Active);//所有启用的公文元素
				}else if("0".equals(statusSelect)){
					list = this.getEdocElementsByStatus(EdocElement.C_iStatus_Inactive);//所有停用的公文元素
				}
			}
		}
		else {
			List<EdocElement> allElements = new ArrayList<EdocElement>();//非国际化key的数据
			if(alls!=null && alls.size()>0){
				for(EdocElement e:alls){
					EdocElement eTemp = null;
					try {
						eTemp = (EdocElement) e.clone();
						eTemp.setName(ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",eTemp.getName()));
					} catch (CloneNotSupportedException e1) {
						LOG.error(e1);
						//e1.printStackTrace();
					}
					allElements.add(eTemp);
				}
			}
			//根据手动录入条件数据过滤后的数据
			list = new ArrayList<EdocElement>();
			List<EdocElement> selectAll = new ArrayList<EdocElement>();//查询之后的结果
			
	    	if (Strings.isNotBlank(textfield)) {
	    		//输入了过滤条件
	    		if ("elementName".equals(condition)) {
	    			//元素名称
	    			int j = 0;
	    			for (int i = 0; i < allElements.size(); i++)
	    			{
	    				EdocElement element = (EdocElement)allElements.get(i);
	    				//元素名称
	    				String name = element.getName();
	    				if(name.contains(textfield)){
	    					selectAll.add(element);
	    				}
	    			}
	    		}
	    		if ("elementfieldName".equals(condition)) {
	    			//元素代码
	    			int j = 0;
	    			for (int i = 0; i < allElements.size(); i++)
	    			{
	    				EdocElement element = (EdocElement)allElements.get(i);
	    				//元素代码
	    				String fildName = element.getFieldName();
	    				//fildName = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",fildName);
	    				if(fildName.contains(textfield)){
	    					selectAll.add(element);
	    				}
	    			}
	    		}
	    		Pagination.setRowCount(selectAll.size());
			}else{
				selectAll = allElements;
			}
			int j = 0;
			for (int i = 0; i < selectAll.size(); i++) {
				EdocElement element = (EdocElement) selectAll.get(i);
				// 元素名称
				if (paginationFlag == 1) {
					if ((j >= (startIndex - 1) * pageSize) && (j < startIndex * pageSize)) {
						// 带分页
						list.add(element);
					}
					j++;
				} else {
					// 不带分页
					list.add(element);
				}
			}
		}
		
		return list;
	}
 	
	/**
     * 根据状态查询可以使用的公文元素（文档中心使用）
     * @param status
     * @return
     */
	@Override
	public List<EdocElement> getEdocElementsByStatusForDoc(long accountId, int status) {
		List<EdocElement> _elements = new ArrayList<EdocElement>();
		List<EdocElement> allElements = edocElementCacheManager.getEdocElementCacheListByDomainId(accountId);

		if(Strings.isNotEmpty(allElements)){
		    for (int i = 0; i < allElements.size(); i++) {
	            EdocElement element = allElements.get(i);
	            int edocElementType = element.getType();
	            String edocElementFieldName = element.getFieldName();
	            boolean isComment = edocElementType == EdocElementConstants.EDOC_ELEMENT_TYPE_COMMENT;
	            boolean isImg = edocElementType == EdocElementConstants.EDOC_ELEMENT_TYPE_IMG;
	            boolean isKeyWord = EdocElementConstants.EDOC_ELEMENT_KEYWORD.equals(edocElementFieldName);
	            boolean isFileSm = EdocElementConstants.EDOC_ELEMENT_FILESM.equals(edocElementFieldName);
	            boolean isFileFz = EdocElementConstants.EDOC_ELEMENT_FILEFZ.equals(edocElementFieldName);
	            boolean isAttachment = EdocElementConstants.EDOC_ELEMENT_ATTACHMENTS.equals(edocElementFieldName);
	            if ((element.getStatus() == status || status == com.seeyon.v3x.edoc.util.Constants.EDOC_All)
	                    && !isComment
	                    && !isImg
	                    && !isKeyWord
	                    && !isFileFz
	                    && !isAttachment && !isFileSm) {
	                _elements.add(getEdocElementByIdForDoc(element.getId()));
	            }
	        }
		}
		return _elements;
	}
    
    /**
     * 根据状态查询可以使用的公文元素（文档中心使用）
     * @param status
     * @return
     */
	public String getDocTableNameByFileName(String tableFiledName) {
		boolean isString = tableFiledName.startsWith("string");
		boolean iStext = tableFiledName.startsWith("text");
		boolean isList = tableFiledName.startsWith("list");
		boolean isInteger = tableFiledName.startsWith("integer");
		boolean isDecimal = tableFiledName.startsWith("decimal");
		boolean isDate = tableFiledName.startsWith("date");
		String table = "";
		if (isString || iStext || isList || isInteger || isDecimal || isDate) {
			table = "EdocSummaryExtend";
		} else {
			table = "EdocSummary";
		}
		return table;
	}
	
    /**
     * 根据枚举id 查询枚举值
     * @param edocElementId
     * @return
     */
    public List<CtpEnumItem> getDocElementEnumListForDoc(Long parentEnumId) {
        EdocElement edocElement = this.getEdocElementsById(parentEnumId);
        List<CtpEnumItem> ctpEnumItems = new ArrayList<CtpEnumItem>();
        if (null != edocElement && null!=edocElement.getMetadataId()) {
            CtpEnumBean listMeta = enumManagerNew.getEnum(edocElement.getMetadataId());
            ctpEnumItems = listMeta.getItems();
        }
        return ctpEnumItems;
    }
    
    /**
     * 根据公文元素id获取公文元素（文档中心使用）（并返还公文元素字段对应的实体类和属性名）
     * @param id(公文元素id)
     * @return
     */
    public EdocElement getEdocElementByIdForDoc(Long id) {
        EdocElement edocElement = new EdocElement();
        if (null == id) {
            return edocElement;
        }
        edocElement = this.getEdocElementsById(id);
        if (null != edocElement) {
            String filedName = edocElement.getFieldName();
            String poName = this.getDocTableNameByFileName(filedName);
            String poFiledName = this.transTableFiledNameToPoFiledName(filedName);

            edocElement.setPoFieldName(poFiledName);
            edocElement.setPoName(poName);
        }
        return edocElement;
    }
    
    public EdocElement getEdocElementByPoFiledName(String poFiledName){
    	if(Strings.isBlank(poFiledName)){
    		return null;
    	}
    	String filedName = this.transPoFiledNameToTableFiledName(poFiledName);
    	EdocElement edocElement = this.getByFieldName(filedName);
    	if(null!=edocElement){
    		String filedNameStr = edocElement.getFieldName();
    		String poName = this.getDocTableNameByFileName(filedNameStr);
    		String poFiledNameStr = this.transTableFiledNameToPoFiledName(filedNameStr);
    		
    		edocElement.setPoFieldName(poFiledNameStr);
        	edocElement.setPoName(poName);
    	}
    	return edocElement;
    }
    
    /**
     * 将公文元素中的file_name 转换成EdocSummary对应的属性名
     * @param filedName
     * @return
     */
    private String transTableFiledNameToPoFiledName(String filedName) {
        if (filedName != null) {
            int index = -1;
            for (int i = 0; i < filedNames.length; i++) {
                if (filedNames[i].equals(filedName)) {
                    index = i;
                    break;
                }
            }
            if (index > -1) {
                return poFieldNames[index];
            }
            
            if(filedName.startsWith("string")){
            	return filedName.replaceFirst("string", "varchar");
            }
        }
        return filedName;
    }

    private String transPoFiledNameToTableFiledName(String poFiledName) {
        if (poFiledName != null) {
            int index = -1;
            for (int i = 0; i < poFieldNames.length; i++) {
                if(poFieldNames[i].equals(poFiledName)){
                    index=i;
                    break;
                }
            }
            if (index > -1) {
                return filedNames[index];
            }
            
            if(poFiledName.startsWith("varchar")){
            	return poFiledName.replaceFirst("varchar", "string");
            }
        }
        return poFiledName;
    }
	
	@Override
	public void loadEdocElementByDomainId(Long domainId) {
		edocElementCacheManager.loadEdocElementCache(domainId);
	}
	
	@Override
	public void transCopyGroupElement2NewAccout(Long domainId) {
		List<EdocElement> addElementList = new ArrayList<EdocElement>();
		List<EdocElement> groupElementList = edocElementCacheManager.getEdocElementCacheListByDomainId(EdocElementCacheManager.groupDomainId);
		for(EdocElement groupElement : groupElementList) {
			addElementList.add(groupElement.clone(domainId));
		}
		edocElementDao.savePatchAll(addElementList);
		edocElementCacheManager.addEdocElementCache(domainId, addElementList);
	}
	
	@Override
	public List<EdocFormElement> getEdocFormElementByElementIdAndFormId(Long elementId, Long formId){
		return edocFormElementDao.getEdocFormElementByElementIdAndFormId(elementId, formId);
	}
	
	/***************************** 方法系统中未使用或调用了未被使用 start ******************************************/
	/** 老方法， V5.6升级的时候做了元素补全，已经不需要这个方法进行判断 */
	public synchronized void initCmpElement(){
    }
	public String getRefMetadataFieldName(Long domainId, Long metadataId) {
    	return null;
    }
	/** 此方法现已无用 */
	public List<EdocElement> getEdocElements() {
    	return edocElementCacheManager.getEdocElementCacheListByDomainId(AppContext.currentAccountId());
    }
	/***************************** 方法系统中未使用或调用了未被使用  end *******************************************/
	
	
	public void setEdocElementDao(EdocElementDao edocElementDao) {
        this.edocElementDao = edocElementDao;
    }    
    public void setEdocFormElementDao(EdocFormElementDao edocFormElementDao) {
		this.edocFormElementDao = edocFormElementDao;
	}
	public void setEdocElementCacheManager(EdocElementCacheManager edocElementCacheManager) {
		this.edocElementCacheManager = edocElementCacheManager;
	}
	public int countEdocElementsFromDB(Long domainId) {
    	return edocElementDao.countEdocElementsFromDB(domainId);
    }
	
	@Override
	public String fixElements(Long fixAccountId){
	    
        LOG.info("开始公文元素补全修复");
        
        StringBuilder infoMsg = new StringBuilder("修复元素情况：");
        
        //Map<String, Object[]> edocElementsMap = new HashMap<String, Object[]>();
        
        //edocElementsMap.put("subject", new Object[]{1, "001", "edoc.element.subject", "subject", "0", null, "1", "1","0",""});
        
        
        //现在总元素有214
        String queryAccountSql = "select domain_id from edoc_element group by domain_id having count(*) != " + EdocElementEnum.size();
        String queryToFixFieldsSql = "select field_name,id from edoc_element where DOMAIN_ID = ?";
        
        //不做分页处理
        try {
            List<Map<String, Object>> toFixList = exeSQL(queryAccountSql, null, true);
            
            if(Strings.isNotEmpty(toFixList)){
                
                //所有的公文元素名称集合
                //Set<String> allFields = edocElementsMap.keySet();
                
                LOG.info("需要进行公文元素补全的单位个数为:" + toFixList.size());
                
                for(Map<String, Object> accountIdMap : toFixList){
                    Long accountId = ((Number)accountIdMap.get("domain_id")).longValue();
                    
                    //传值的话就修复指定单位的元素
                    if(fixAccountId != null && !fixAccountId.equals(accountId)){
                        continue;
                    }
                    
                    List<Long> params = new ArrayList<Long>();
                    params.add(accountId);
                    List<Map<String, Object>> fieldsList = exeSQL(queryToFixFieldsSql, params, true);
                    List<String> accountFields = new ArrayList<String>();//单位有的元素
                    List<Long> deleEle = new ArrayList<Long>();
                    Map<String, Long> file2Id = new HashMap<String, Long>();
                    
                    for(Map<String, Object> result : fieldsList){
                        
                        
                        String filedName = (String) result.get("field_name");
                        Long eleId = ((Number)result.get("id")).longValue();
                        
                        if(file2Id.containsKey(filedName)){
                            Long preId = file2Id.get(filedName);
                            
                            //保留系统预置的ID
                            if(preId < 0 || (preId > eleId && eleId > 0)){
                                deleEle.add(preId);
                                file2Id.put(filedName, eleId);
                            }else{
                                deleEle.add(eleId);
                                file2Id.put(filedName, preId);
                            }
                            String m = "单位:" + accountId + " 的 " + filedName + " 元素重复，将被删除.";
                            infoMsg.append(m);
                            
                        }else{
                            file2Id.put(filedName, eleId);
                            accountFields.add(filedName);
                        }
                    }
                    
                    List<EdocElement> insertEles = new ArrayList<EdocElement>();
                    
                    for(EdocElementEnum ele : EdocElementEnum.values()){
                        if(!accountFields.contains(ele.getFieldName())){
                            
                            EdocElement e = ele.trans2Element();
                            if(!accountId.equals(0L)){
                                e.setId(UUIDLong.longUUID());
                            }
                            e.setDomainId(accountId);
                            
                            insertEles.add(e);
                            
                            String m = "单位:" + accountId + " 需要补全 " + ele.getFieldName() + " 元素, ";
                            infoMsg.append(m);
                        }
                    }
                    
                    //删除重复的元素
                    if(Strings.isNotEmpty(deleEle)){
                        
                      //Oracle数据超过1000报错兼容
                        int pageSize = 999;
                        int index = 0;
                        int size = deleEle.size();
                        
                        while(index < size){
                            
                            int toIndex = Math.min(index + pageSize, size);
                            
                            List<Long> subList = deleEle.subList(index, toIndex);
                            
                            StringBuilder delAclSQL = new StringBuilder("delete from EDOC_ELEMENT_FLOWPERM_ACL where ELEMENT_ID in (?");
                            StringBuilder delEleSQL = new StringBuilder("delete from EDOC_ELEMENT where id in (?");
                            //-11110000
                            List<Long> sqlParams = new ArrayList<Long>();
                            sqlParams.add(-11110000L);
                            for(Long dId : subList){
                                delAclSQL.append(",").append("?");
                                delEleSQL.append(",").append("?");
                                sqlParams.add(dId);
                            }
                            delAclSQL.append(")");
                            delEleSQL.append(")");
                            
                            exeSQL(delAclSQL.toString(), sqlParams, false);
                            exeSQL(delEleSQL.toString(), sqlParams, false);
                            
                            index = toIndex;
                        }
                    }
                    
                    if(Strings.isNotEmpty(insertEles)){
                        edocElementDao.savePatchAll(insertEles);
                        edocElementCacheManager.refreshEdocElementCache(accountId);
                    }
                }
            }else{
                infoMsg.append("所有单位公文元素正常");
            }
            
            LOG.info("结束公文元素补全修复:" + infoMsg.toString());
        } catch (Exception e) {
            LOG.error("公文元素补全修复异常", e);
        }
    
        return infoMsg.toString();
	}
	
	
    
    /**
     * 
     * @param sql
     * @param params
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List<Map<String, Object>> exeSQL(String sql, List params, boolean hasRest) throws Exception {
        
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        JDBCAgent jdbcAgent =  null;
        
        try {
            jdbcAgent = new JDBCAgent();
            if (Strings.isNotEmpty(params)) {
                jdbcAgent.execute(sql, params);
            } else {
                jdbcAgent.execute(sql);
            }
            if(hasRest){
                result = (List<Map<String, Object>>) jdbcAgent.resultSetToList();
            }
        }finally {
            if (jdbcAgent != null) {
                jdbcAgent.close();
            }
        }
        return result;
    }
    
    @Override
	public EdocElement getByFieldName4upgrade(String fieldName, Long userAccountId) {
        return edocElementCacheManager.getEdocElementCacheByFieldName(fieldName, userAccountId);	
    
	}

	@Override
	public List<EdocElement> getEdocElementsByGovdocRightView(Map<String, String> params) {
		return edocElementCacheManager.getEdocElementsByGovdocRightView(AppContext.currentAccountId(), params);
	}
    
	@Override
	public List<EdocElementBO> getEdocElementsByAccount(Long accountId) {
		List<EdocElement> list = (List<EdocElement>) edocElementDao.getEdocElementListByDomainId(accountId);
		List<EdocElementBO> result = new ArrayList<EdocElementBO>();
		for (EdocElement edocElement : list) {
			result.add(DataTransUtil.truansEdocElement2BO(edocElement));
		}
		return result;
	}
}