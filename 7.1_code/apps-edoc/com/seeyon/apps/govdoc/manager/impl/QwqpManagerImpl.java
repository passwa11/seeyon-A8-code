package com.seeyon.apps.govdoc.manager.impl;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.seeyon.apps.govdoc.dao.QwqpDao;
import com.seeyon.apps.govdoc.manager.QwqpManager;
import com.seeyon.apps.govdoc.po.QwqpDefaultTemplate;
import com.seeyon.apps.govdoc.po.QwqpEdocFormFileRelation;
import com.seeyon.apps.govdoc.po.QwqpEdocSendFormRelation;
import com.seeyon.apps.govdoc.util.QwqpUtil;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.v3x.edoc.exception.EdocException;

public class QwqpManagerImpl implements QwqpManager {
	
	private static final Logger LOGGER = Logger.getLogger(QwqpManagerImpl.class);
	
	private QwqpDao qwqpDao;
	
	//存入单位默认模板
	public void setDepDefaultTemplate( Long defaultTemplate_id, Long department_id ,Long template_id ,Long templateType) throws BusinessException{
		//User user =AppContext.getCurrentUser();
		QwqpDefaultTemplate defaultTemplate=new QwqpDefaultTemplate();
		defaultTemplate.setId(defaultTemplate_id);
		defaultTemplate.setDepartement(department_id);
		defaultTemplate.setTemplate(template_id);
		defaultTemplate.setTemplateType(templateType);
		//查询是否存在该单位该类型默认模板，如果存在则删除原来的默认模板
		if (qwqpDao.getExistDefaultTemplate(department_id, templateType)!=null) {
			Long deleteId = qwqpDao.getExistDefaultTemplate(department_id, templateType);
			qwqpDao.deleteDefaultTemplateById(deleteId);
		}
		//新建默认模板
		qwqpDao.saveDefaultTemplate(defaultTemplate);
	}
	
	//删除单位默认模板
	public void deleteDepDefaultTemplate(Long defaultTemplate_id) throws BusinessException{
		qwqpDao.deleteDefaultTemplateById(defaultTemplate_id);
	}
	
	public Long getExistDefaultTemplate(Long departement, Long templateType) throws BusinessException {
		return qwqpDao.getExistDefaultTemplate(departement, templateType);
	}
	
	public List<QwqpDefaultTemplate> getDefaultTemplateById(Long defaultTemplate_id) throws BusinessException {
		return qwqpDao.getDefaultTemplateById(defaultTemplate_id);
	}
	
	//获取所有该类型模板
	public List<CtpTemplate> getTemplateByType(Long templateType) throws BusinessException{
		return qwqpDao.getTemplateByType(templateType);
	}
	
	//通过模板id获得模板
	public List<CtpTemplate> getTemplateById(Long templateId) throws BusinessException{
		return qwqpDao.getTemplateById(templateId);
	} 
		
	public void addEdocFormFileRelation(QwqpEdocFormFileRelation edocFormFileRelation) {
		try {
			List<QwqpEdocFormFileRelation> relationList = findByEdocFormId(edocFormFileRelation.getFormId());
			//如果有和EdocForm关联，先删除关联，在进行添加
			if(null!=relationList&&relationList.size()>0) {
				qwqpDao.deleteByFormId(edocFormFileRelation.getFormId());
			}
			qwqpDao.save(edocFormFileRelation);
		} catch(Exception e) {
			LOGGER.error("", e);
		}
	}
	
	//根据edocFormId查询对应的关系表
	public List<QwqpEdocFormFileRelation> findByEdocFormId(Long edocFormId) {
		List<QwqpEdocFormFileRelation> edocFormFileRelation = null;
		edocFormFileRelation = qwqpDao.findByEdocFormId(edocFormId);
		return edocFormFileRelation;
	}

	@Override
	public List<QwqpEdocFormFileRelation> findByFileId(long parseLong) {
		List<QwqpEdocFormFileRelation> edocFormFileRelation = null;
		edocFormFileRelation = qwqpDao.findByFileId(parseLong);
		return edocFormFileRelation;
	}

	public void setqwqpDao(QwqpDao qwqpDao) {
		this.qwqpDao = qwqpDao;
	}
	
	//添加发送的公文与file关联
	public void save(QwqpEdocSendFormRelation edocSendFormRelation){
		try {
			qwqpDao.deleteByEdocId(edocSendFormRelation.getEdocId());
			qwqpDao.save(edocSendFormRelation);
		} catch(Exception e) {
			LOGGER.error("", e);
		}
	}
		
	public void update(QwqpEdocSendFormRelation edocSendFormRelation) {
		try {
			qwqpDao.update(edocSendFormRelation);
		} catch(Exception e) {
			LOGGER.error("", e);
		}
	}
	
	//根据发送的公文id查询
	public List<QwqpEdocSendFormRelation> getByEdocId(Long edocId){
		List<QwqpEdocSendFormRelation> list = qwqpDao.findByEdocId(edocId);
		return list;
	}
		
	public void deleteByEdocId(Long edocId,String fileType){
		qwqpDao.deleteByEdocId(edocId);
	}
	
	@Override
	public QwqpEdocSendFormRelation existsSign(long summaryId) throws EdocException {
		List<QwqpEdocSendFormRelation> list = qwqpDao.findByEdocId(summaryId);
		if(null!=list&&list.size()>0){
			return list.get(0);
		}else{
			return null;
		}
	}
	
	@Override
	public void deleteByEdocId(long summaryId) {
		qwqpDao.deleteByEdocId(summaryId);
	}
		
	@Override
	public void deleteRelation(QwqpEdocSendFormRelation edocSendFormRelation) {
		try {
			qwqpDao.delete(edocSendFormRelation);
		} catch(Exception e) {
			LOGGER.error("", e);
		}
	}
	
	@Override
	public void saveEdocFormFileRelations(GovdocNewVO info) throws BusinessException {
		if(info.getIsQuickSend()){
			return;
		}
    	List<QwqpEdocFormFileRelation> edocFormFileRelations = this.findByEdocFormId(info.getSummary().getFormAppid());
    	if(null!=edocFormFileRelations&&edocFormFileRelations.size()>0) {
    		deleteByEdocId(info.getSummary().getId());
    		FileManager fileManager = (FileManager)AppContext.getBean("fileManager");
    		if(edocFormFileRelations.get(0).getDoubleForm()){
    			V3XFile v3xFile = fileManager.getV3XFile(edocFormFileRelations.get(0).getFileId());
    			long newFileId = fileManager.copyFileBeforeModify(v3xFile.getId());
    			QwqpEdocSendFormRelation edocSendFormRelation = new QwqpEdocSendFormRelation();
    			edocSendFormRelation.setFileId(newFileId);
    			edocSendFormRelation.setEdocId(info.getSummary().getId());
    			edocSendFormRelation.setFileType(QwqpUtil.AIP_FILE);
    			edocSendFormRelation.setIdIfNew();
    			this.save(edocSendFormRelation);
    		}
    	}
	}
	
	@Override
	public void updateEdocFormFileRelations(Long summaryId, Long fileId) throws BusinessException {
		QwqpEdocSendFormRelation edocSendFormRelation = new QwqpEdocSendFormRelation();
		edocSendFormRelation.setFileId(fileId);
		edocSendFormRelation.setEdocId(summaryId);
		edocSendFormRelation.setFileType(QwqpUtil.AIP_FILE);
		edocSendFormRelation.setIdIfNew();
		this.save(edocSendFormRelation);
	}

	@Override
	public void setQwqpParam(GovdocSummaryVO summaryVO) {
		List<QwqpEdocSendFormRelation> edocSendFormRelations = this.getByEdocId(summaryVO.getSummary().getId());
		if(CollectionUtils.isNotEmpty(edocSendFormRelations)){
			summaryVO.setAipFileId(edocSendFormRelations.get(0).getFileId().toString());
		}
	}
	
}
