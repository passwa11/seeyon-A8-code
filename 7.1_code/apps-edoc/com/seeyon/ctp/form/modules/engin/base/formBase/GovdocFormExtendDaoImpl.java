package com.seeyon.ctp.form.modules.engin.base.formBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.form.po.GovdocFormExtend;
import com.seeyon.ctp.util.DBAgent;

public class GovdocFormExtendDaoImpl implements GovdocFormExtendDao{

	@Override
	public void saveOrUpdate(GovdocFormExtend govdocFormExtend) {
		DBAgent.saveOrUpdate(govdocFormExtend);
	}

	@Override
	public GovdocFormExtend findByFormId(long formId) {
		
		String hql = "from GovdocFormExtend where formId =:formId";
		List<GovdocFormExtend> list = new ArrayList<GovdocFormExtend>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("formId", formId);
		list  =DBAgent.find(hql,map);
		if(null!=list&&list.size()>0){
			return list.get(0);
		}
		return null;
	}

	@Override
	public void deleteByFormId(long formId) {
		GovdocFormExtend govdocFormExtend = findByFormId(formId);
		if(null!=govdocFormExtend){
			DBAgent.delete(govdocFormExtend);
		}
	}
	

}
