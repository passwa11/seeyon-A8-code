package com.seeyon.v3x.edoc.dao;

import java.util.List;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.domain.EdocFormElement;

public class EdocFormElementDao extends BaseHibernateDao<EdocFormElement> {
	  public List<EdocFormElement> getAllEdocFormElements()
	    {
	        String hsql = "from EdocFormElement as a";
	        return super.findVarargs(hsql);        
	    }
	  public List<EdocFormElement> getEdocFormElementByFormId(long formId){
		  	String hsql = "from EdocFormElement as a where a.formId = ? order by a.elementId";
		  	return super.findVarargs(hsql, formId);
	  }
	  
	  public List<EdocFormElement> getEdocFormElementByFormIdNotInSubject(long formId){
		  	String hsql = "from EdocFormElement as a where a.formId = ? and elementId!=1 and required=?";
		  	return super.findVarargs(hsql, formId, true);
	  }
	  
	  public List<EdocElement> getEdocFormElementByFormIdAndFieldName(long formId, String fieldName){
		  	String hsql = "select b from EdocElement b,EdocFormElement a where b.id=a.elementId and a.formId = ? and b.fieldName=?";
		  	return super.findVarargs(hsql, formId, fieldName);
	  }

	  public List<EdocElement> getEdocElementByRequired(long formId, Boolean required) {
		  String hsql = "select b from EdocElement b,EdocFormElement a where b.id=a.elementId and a.formId = ? and a.required=?";
		  return super.findVarargs(hsql, formId, required);
	  }
	  
	  public List<EdocFormElement>  getEdocFormElementByElementIdAndFormId(Long elementId, Long formId){
		  	String hsql = "from EdocFormElement as a where elementId=? and formId=?";
		  	return super.findVarargs(hsql, elementId, formId);
	  }
	  
}
