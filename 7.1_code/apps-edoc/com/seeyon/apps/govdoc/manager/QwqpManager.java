package com.seeyon.apps.govdoc.manager;

import java.util.List;

import com.seeyon.apps.govdoc.po.QwqpDefaultTemplate;
import com.seeyon.apps.govdoc.po.QwqpEdocFormFileRelation;
import com.seeyon.apps.govdoc.po.QwqpEdocSendFormRelation;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.v3x.edoc.exception.EdocException;

public interface QwqpManager {
	
	public void setDepDefaultTemplate( Long defaultTemplate_id, Long department_id ,Long template_id ,Long templateType) throws BusinessException;
	public void deleteDepDefaultTemplate(Long defaultTemplate_id) throws BusinessException;
	public Long getExistDefaultTemplate(Long departement, Long templateType) throws BusinessException;
	public List<QwqpDefaultTemplate> getDefaultTemplateById(Long defaultTemplate_id) throws BusinessException;
	public List<CtpTemplate> getTemplateByType(Long templateType) throws BusinessException;
	public List<CtpTemplate> getTemplateById(Long templateId) throws BusinessException;
	
	public void addEdocFormFileRelation(QwqpEdocFormFileRelation edocFormFileRelation);
	public List<QwqpEdocFormFileRelation> findByEdocFormId(Long edocFormId);
	public List<QwqpEdocFormFileRelation> findByFileId(long parseLong);
	
	
	public void save(QwqpEdocSendFormRelation edocSendFormRelation);
	public List<QwqpEdocSendFormRelation> getByEdocId(Long edocId);	
	public QwqpEdocSendFormRelation existsSign(long summaryId) throws EdocException ;
	public void update(QwqpEdocSendFormRelation edocSendFormRelation);	
	public void deleteByEdocId(long summaryId);
	public void deleteRelation(QwqpEdocSendFormRelation edocSendFormRelation);
	
	public void saveEdocFormFileRelations(GovdocNewVO info) throws BusinessException;
	public void updateEdocFormFileRelations(Long summaryId, Long fileId) throws BusinessException;
	public void setQwqpParam(GovdocSummaryVO summaryVO);	
	
}
