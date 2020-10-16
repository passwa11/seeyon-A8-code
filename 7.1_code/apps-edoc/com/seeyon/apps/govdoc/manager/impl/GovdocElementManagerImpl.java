package com.seeyon.apps.govdoc.manager.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.govdoc.manager.GovdocElementManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.v3x.edoc.constants.EdocElementConstants;
import com.seeyon.v3x.edoc.constants.EdocQueryColConstants;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.manager.EdocElementManager;

/**
 * 新公文元素管理类
 * @author 唐桂林
 *
 */
public class GovdocElementManagerImpl implements GovdocElementManager {

	private EdocElementManager edocElementManager;
	
	@Override
	public List<EdocElement> getEdocElementsByContidion(String condition, String textfield, String statusSelect, int paginationFlag) throws BusinessException {
		return edocElementManager.getEdocElementsByContidion(condition, textfield, statusSelect, paginationFlag);
	}

	@Override
	public int getAllEdocElementCount() throws BusinessException {
		return edocElementManager.getAllEdocElementCount();
	}

	@Override
	public List<EdocElement> getAllEdocElements(int startIndex, int numResults) throws BusinessException {
		return edocElementManager.getAllEdocElements(startIndex, numResults);
	}
	
	@Override
    public List<EdocElement> listElementByAccount(Long domainId) throws BusinessException {
    	return edocElementManager.listElementByAccount(domainId);
    }

	public List<EdocElement> getEdocElementsByStatusForDoc(long accountId, int status) throws BusinessException {
		return edocElementManager.getEdocElementsByStatusForDoc(accountId, status);
	}
	
	@Override
    public Map<String, String> getEdocElementFieldNames(Long domainId) throws BusinessException {
    	List<EdocElement> elementList = edocElementManager.listElementByAccount(domainId);
    	
    	Map<String, String> fieldMap = new HashMap<String, String>();
    	
    	if(Strings.isNotEmpty(elementList)) {
			for(EdocElement bean : elementList) {
				if(!bean.getIsSystem()) {
					continue;
				}
				int type = bean.getType();
				String fieldName = bean.getFieldName();
				boolean isComment = type==EdocElementConstants.EDOC_ELEMENT_TYPE_COMMENT;
				boolean isImg = type==EdocElementConstants.EDOC_ELEMENT_TYPE_IMG;
				boolean isKeyWord = EdocElementConstants.EDOC_ELEMENT_KEYWORD.equals(fieldName);
				boolean isFileSm = EdocElementConstants.EDOC_ELEMENT_FILESM.equals(fieldName);
				boolean isFileFz = EdocElementConstants.EDOC_ELEMENT_FILEFZ.equals(fieldName);
				boolean isAttachment = EdocElementConstants.EDOC_ELEMENT_ATTACHMENTS.equals(fieldName);
				boolean isNibanyijian = "nibanyijian".equals(fieldName);
				boolean isReceiveUnit = "receive_unit".equals(fieldName);
				if(!isComment
						&& !isImg
						&& !isKeyWord
						&& !isFileSm
						&& !isFileFz
						&& !isAttachment
						&& !isNibanyijian
						&& !isReceiveUnit) {
					fieldMap.put(bean.getFieldName(), ResourceUtil.getString(bean.getName()));
				}
			}
		}
    	fieldMap.put("hasArchive", ResourceUtil.getString("govdoc.canArchive.label"));
    	fieldMap.put("archiveName", ResourceUtil.getString("govdoc.archiveName.label"));
    	return fieldMap;
    }
	
	@Override
	public EdocElement getEdocElement(String elementId) throws BusinessException {
		return edocElementManager.getEdocElement(elementId);
	}

	@Override
	public void updateEdocElement(EdocElement po) throws BusinessException {
		edocElementManager.updateEdocElement(po);
	}

	@Override
	public List<EdocElement> getAllEdocElements() throws BusinessException {
		return edocElementManager.getAllEdocElements();
	}

	@Override
	public EdocElement getByFieldName(String fieldName)  {
		return edocElementManager.getByFieldName(fieldName);
	}
	
	public EdocElement getByFieldName(String fieldName, Long domainId) {
		return edocElementManager.getByFieldName(fieldName, domainId);
	}
	
	@Override
	public void transGenerateElement(Long domainId) throws BusinessException {
		int count = edocElementManager.countEdocElementsFromDB(domainId);
	    if(count == 0) {
	        edocElementManager.transCopyGroupElement2NewAccout(domainId);
	    } else {
	        //集团的一起修复下
            edocElementManager.fixElements(0l);
	        //做数据修复， 修复集团
	        edocElementManager.fixElements(domainId);
	    }
	}
	
	public void setEdocElementManager(EdocElementManager edocElementManager) {
		this.edocElementManager = edocElementManager;
	}

	@Override
	@AjaxAccess
	public FlipInfo findList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		String con = condition.get("condition");
		String textfield = condition.get("textfield");
		flipInfo.setPagination();
		List<EdocElement> eList = this.getEdocElementsByContidion(con,textfield,null,1);
		flipInfo.setData(eList);
		return flipInfo;
	}

}
