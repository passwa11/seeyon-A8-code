package com.seeyon.apps.govdoc.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.govdoc.po.QwqpDefaultTemplate;
import com.seeyon.apps.govdoc.po.QwqpEdocFormFileRelation;
import com.seeyon.apps.govdoc.po.QwqpEdocSendFormRelation;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.BasePO;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.util.DBAgent;

public class QwqpDao {
	
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/EMP";
	
	public void save(BasePO po) throws BusinessException {
		DBAgent.save(po);
	}
	
	public void update(BasePO po) throws BusinessException {
		DBAgent.update(po);
	}
	
	public void delete(BasePO po) throws BusinessException {
		DBAgent.delete(po);
	}
	
	// 存入默认模板至数据库dy
	public void saveDefaultTemplate(QwqpDefaultTemplate defaultTemplate) throws BusinessException {
		DBAgent.save(defaultTemplate);
	}

	// 通过默认模板id从数据库删除默认模板 dy
	public void deleteDefaultTemplateById(Long defaultTemplate_id) throws BusinessException {
	    DBAgent.bulkUpdate("delete from QwqpDefaultTemplate where id = ?", defaultTemplate_id);
	}

	// 通过单位和模板类型从数据库获取已存在默认模板 dy
	@SuppressWarnings("unchecked")
	public Long getExistDefaultTemplate(Long departement, Long templateType) throws BusinessException {
		String hql = "from QwqpDefaultTemplate as e where e.departement = :departement and e.templateType = :templateType";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("departement", departement);
		paramMap.put("templateType", templateType);
		List<QwqpDefaultTemplate> CtpTemplateList = DBAgent.find(hql, paramMap);
		Long id = null;
		if (null != CtpTemplateList && CtpTemplateList.size() > 0) {
			id = CtpTemplateList.get(0).getId();
			return id;
		} else {
			return null;
		}
	}

	// 通过默认模版id从数据库获取默认模板 dy
	public List<QwqpDefaultTemplate> getDefaultTemplateById(Long defaultTemplate_id) throws BusinessException {
		List<QwqpDefaultTemplate> ctpTemplateList = new ArrayList<QwqpDefaultTemplate>();
		if (defaultTemplate_id != null) {
			QwqpDefaultTemplate bean = DBAgent.get(QwqpDefaultTemplate.class, defaultTemplate_id);
			if(bean != null) {
				ctpTemplateList.add(bean);
			}
		}
		return ctpTemplateList;
	}

	// 从数据库获取该类型所有模板
	@SuppressWarnings("unchecked")
	public List<CtpTemplate> getTemplateByType(Long categoryId) throws BusinessException {
		String hql = "from CtpTemplate as e where e.categoryId = :categoryId and e.delete = :delete";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("categoryId", categoryId);
		paramMap.put("delete", false);
		return DBAgent.find(hql, paramMap);
	}

	// 从数据库获得该id模板
	@SuppressWarnings("unchecked")
	public List<CtpTemplate> getTemplateById(Long id) throws BusinessException {
		String hql = "from CtpTemplate as e where e.id = :id and e.delete = :delete";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("id", id);
		paramMap.put("delete", false);
		return DBAgent.find(hql, paramMap);
	}

	@SuppressWarnings("unchecked")
	public List<QwqpEdocFormFileRelation> findByEdocFormId(Long formId) {
		String hql="from QwqpEdocFormFileRelation as e where e.formId = :formId";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("formId", formId);
		return DBAgent.find(hql, paramMap);
	}

	public void deleteByFormId(Long formId) {
		String hql = "delete from QwqpEdocFormFileRelation e where e.formId = ?";
		DBAgent.bulkUpdate(hql, formId);
	}

	@SuppressWarnings("unchecked")
	public List<QwqpEdocFormFileRelation> findByFileId(Long fileId) {
		String hql="from QwqpEdocFormFileRelation as e where e.fileId = :fileId";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("fileId", fileId);
		return DBAgent.find(hql, paramMap);
	}
	
	@SuppressWarnings("unchecked")
	public List<QwqpEdocSendFormRelation> findByEdocId(Long edocId){
		String hql = "from QwqpEdocSendFormRelation e where e.edocId = :edocId";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("edocId", edocId);
		return DBAgent.find(hql, paramMap);
	}

	public void deleteByEdocId(Long edocId) {
		String hql = "delete from QwqpEdocSendFormRelation e where e.edocId = ? ";
		DBAgent.bulkUpdate(hql, edocId);
	}
	
}
