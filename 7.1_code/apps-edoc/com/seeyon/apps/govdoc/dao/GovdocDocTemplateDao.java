package com.seeyon.apps.govdoc.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocDocTemplate;

public class GovdocDocTemplateDao {
	
	@SuppressWarnings("unchecked")
	public List<EdocDocTemplate> findList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		Long domainId = Long.parseLong(condition.get("domainId"));
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("from EdocDocTemplate as template where template.domainId=:domainId");
		
		Map<String,Object> paramMap = new HashMap<String,Object>();
		paramMap.put("domainId",domainId);
		if(Strings.isNotBlank(condition.get("condition"))) {
			if(Strings.isNotBlank(condition.get("name"))) {
				buffer.append(" and template.name like :name ");
				paramMap.put("name", "%"+SQLWildcardUtil.escape(condition.get("name"))+"%");
			}
			if(Strings.isNotBlank(condition.get("type"))) {
				buffer.append(" and template.type = :type ");
				paramMap.put("type", Integer.parseInt(condition.get("type")));
			}
			if(Strings.isNotBlank(condition.get("status"))) {
				buffer.append(" and template.status = :status ");
				paramMap.put("status", Integer.parseInt(condition.get("status")));
			}
		}
		buffer.append(" order by template.domainId");
		
		return DBAgent.find(buffer.toString(), paramMap);
	}
	
	/**
	 * 根据传入的id集合传（单位/部门），类型（正文/文单），来查找授权的模板
	 * @param ids  id集合传（单位/部门）
	 * @param type  类型（正文/文单）
	 * @param textType 正文类型
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<EdocDocTemplate> findGrantedTemplateForTaohong(boolean isAdmin, String ids, int type, String textType) {
		Map<String,Object> paramMap = new HashMap<String,Object>();
		
		StringBuilder sql = new StringBuilder("select template from EdocDocTemplate as template");
		if(!isAdmin) {
			sql.append(", EdocDocTemplateAcl as tempAcl where template.id = tempAcl.templateId and template.type = :templateType");
			if(ids != null) {
				List<Long> idList = new ArrayList<Long>();
				String[] tmps = ids.split(",");
				for(String id : tmps) {
					idList.add(Long.valueOf(id));
				}
				if(Strings.isNotEmpty(idList)) {
					sql.append(" and tempAcl.depId in (:ids)");
					paramMap.put("ids", idList);
				}
			}
		} else {
			sql.append(" where template.domainId = :domainId");
			paramMap.put("domainId", Long.parseLong(ids));
		}
		
		sql.append(" and template.type = :templateType");
		paramMap.put("templateType", type);
		
		if(textType!=null && !"".equals(textType)) {
			sql.append(" and template.textType =:textType");
			paramMap.put("textType", textType);
		}
		sql.append(" order by template.createTime");
		
		//不分页
		return DBAgent.find(sql.toString(), paramMap);
	}

}
