package com.seeyon.apps.govdoc.option.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.govdoc.po.FormOptionExtend;
import com.seeyon.ctp.util.DBAgent;

public class FormOptionExtendDaoImpl implements FormOptionExtendDao{

	@Override
	public void saveOrUpdate(FormOptionExtend govdocFormExtend) {
		DBAgent.saveOrUpdate(govdocFormExtend);
	}

	@Override
	public FormOptionExtend findByFormId(long formId) {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		String hql = "from FormOptionExtend where formId = :formId";
		parameterMap.put("formId", formId);
		List<FormOptionExtend> list = new ArrayList<FormOptionExtend>();
		list  =DBAgent.find(hql,parameterMap);
		if(null!=list&&list.size()>0){
			return list.get(0);
		}
		return null;
	}

	@Override
	public void deleteByFormId(long formId) {
		FormOptionExtend govdocFormExtend = findByFormId(formId);
		if(null!=govdocFormExtend){
			DBAgent.delete(govdocFormExtend);
		}
	}
	

}
