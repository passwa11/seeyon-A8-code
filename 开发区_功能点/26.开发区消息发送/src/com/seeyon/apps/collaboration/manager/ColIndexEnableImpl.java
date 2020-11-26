/**
 * $Author$
 * $Rev$
 * $Date::                     $:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */

package com.seeyon.apps.collaboration.manager;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.index.bo.IndexInfo;
import com.seeyon.apps.index.manager.IndexEnable;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.filemanager.manager.PartitionManager;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.filemanager.Partition;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.util.Strings;

/**
 * @author wulin
 *
 */
public class ColIndexEnableImpl extends ColIndexEnable implements IndexEnable{
	private static Log LOG = CtpLogFactory.getLog(ColIndexEnableImpl.class);
    private PartitionManager partitionManager ;
    private MainbodyManager ctpMainbodyManager;
    private FileManager fileManager;
    private CollaborationApi collaborationApi;
    private ColIndexEnable formIndexEnableImpl;

   

    public ColIndexEnable getFormIndexEnableImpl() {
		return formIndexEnableImpl;
	}

	public void setFormIndexEnableImpl(ColIndexEnable formIndexEnableImpl) {
		this.formIndexEnableImpl = formIndexEnableImpl;
	}

	public void setPartitionManager(PartitionManager partitionManager) {
		this.partitionManager = partitionManager;
	}

	public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
		if (ctpMainbodyManager == null) {
			ctpMainbodyManager = (MainbodyManager) AppContext.getBean("ctpMainbodyManager");
		}
		this.ctpMainbodyManager = ctpMainbodyManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setCollaborationApi(CollaborationApi collaborationApi) {
		this.collaborationApi = collaborationApi;
	}

    @Override
    public Integer getAppEnumKey() {
        return ApplicationCategoryEnum.collaboration.getKey();
    }
    
    @Override
    public IndexInfo getIndexInfo(Long id) throws BusinessException {
        IndexInfo info = super.getIndexInfo(id);
        
        setIndexContent(id, info);
       
        return info;
    }

	/**
	 * 设置正文
	 * 
	 * @param id
	 * @param info
	 * @throws BusinessException
	 */
	private void setIndexContent(Long id, IndexInfo info) throws BusinessException {
	    ColSummary colSummary = (ColSummary)AppContext.getThreadContext("IndexInfo_summary");
		if(colSummary==null){
		    return;
		}
		if(id == null || info == null){
			LOG.error("setIndexContent:id:"+id+",info is null:"+(info == null));
			return;
		}
		List<CtpContentAll> contentList = ctpMainbodyManager.getContentList(ModuleType.collaboration,
				colSummary.getId(), "-1");
		CtpContentAll content = null;
		if (Strings.isNotEmpty(contentList)) {
			content = contentList.get(0);
			if (String.valueOf(MainbodyType.HTML.getKey()).equals(colSummary.getBodyType())) {
				info.setContentType(IndexInfo.CONTENTTYPE_HTMLSTR);
				info.setContent(content.getContent());
			}
			else if (ColUtil.isForm(colSummary.getBodyType())) {
				IndexInfo ii = formIndexEnableImpl.getIndexInfo(colSummary.getId());
				info.setContent(ii.getContent());
			}
			else {
				Long fileId = content.getContentDataId();
				if(fileId != null) {
					info.setContentID(fileId);
				}
				Date date = getCreateData(fileId);
				info.setContentCreateDate(date);
				Partition partition = partitionManager.getPartition(date, true);
				info.setContentAreaId(partition.getId().toString());
				String contentPath = this.fileManager.getFolder(date, false);
				if (Strings.isNotBlank(contentPath)) {
					info.setContentPath(
							contentPath.substring(contentPath.length() - 11) + System.getProperty("file.separator"));
				}
				else {
					info.setContentPath("");
				}
				if (String.valueOf(MainbodyType.OfficeWord.getKey()).equals(colSummary.getBodyType())
						|| String.valueOf(MainbodyType.OfficeExcel.getKey()).equals(colSummary.getBodyType())) {
					if (String.valueOf(MainbodyType.OfficeWord.getKey()).equals(colSummary.getBodyType())) {
						info.setContentType(IndexInfo.CONTENTTYPE_WORD);
					}
					else {
						info.setContentType(IndexInfo.CONTENTTYPE_XLS);
					}
				}
				else if (String.valueOf(MainbodyType.WpsWord.getKey()).equals(colSummary.getBodyType())
						|| String.valueOf(MainbodyType.WpsExcel.getKey()).equals(colSummary.getBodyType())) {
					if ((String.valueOf(MainbodyType.WpsWord.getKey()).equals(colSummary.getBodyType()))) {
						info.setContentType(IndexInfo.CONTENTTYPE_WPS_Word);
					}
					else {
						info.setContentType(IndexInfo.CONTENTTYPE_WPS_EXCEL);
					}
				}
			}
		}
	}
	

	
	/**
     * 根据fileId获取文件创建时间
     * @param fileId
     * @return
     */
    public Date getCreateData(Long fileId){
    	Date date = new Date();
    	try {
    		if(null!=fileId){
    			 V3XFile file = fileManager.getV3XFile(fileId);
	   			 if(null!=file && null!=file.getUpdateDate()){
	   				 date = file.getUpdateDate();
	   			 }else if(null!=file && null!=file.getCreateDate()){
	   				 date = file.getCreateDate();
	   			 }
    		}
			 
		} catch (BusinessException e) {
			LOG.error("", e);
		}
		return date;
    }

    @Override
    public boolean isShowIndexSummary(Long id, Map<String, String> extendProperties) throws BusinessException {
        return true;
    }

}
