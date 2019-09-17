package com.seeyon.v3x.edoc.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.edoc.domain.EdocDocTemplateAcl;

public class EdocDocTemplateAclDao extends BaseHibernateDao<EdocDocTemplateAcl>{
	
	private static final Log log = LogFactory.getLog(EdocDocTemplateAclDao.class);

	//property constants
	public static final String DEPT_ID = "deptId";
   
	/**
     * 方法描述：保存公文模板使用授权
     */
    public void saveEdocDocTemplateAcl(EdocDocTemplateAcl edocDocTemplateAcl) {
        log.debug("saving V3xEdocDocTemplateAcl instance");
        try {
        	super.save(edocDocTemplateAcl);
            log.debug("save successful");
        } catch (RuntimeException re) {
            log.error("save failed", re);
            throw re;
        }
    }
    
    /**
     * 方法描述：根据模板Id查询公文模板使用授权
     */

	public List<EdocDocTemplateAcl> findEdocDocTemplateAcl(String templateId) {
		Object[] values = new Object[]{Long.valueOf(templateId)};
        try {
           String queryString = "from EdocDocTemplateAcl as templateAcl where templateAcl.templateId= ? order by templateAcl.depId desc";
           return super.findVarargs(queryString, values);
        } catch (RuntimeException re) {
           log.error("find by property name failed", re);	
           throw re;
        }
  	}
    	
	public void deleteAclByTemplateId(Long templateId){
		Object[] values = {templateId};
		try{
			String queryString = "delete from EdocDocTemplateAcl as templateAcl where templateAcl.templateId= ? ";
			this.getHibernateTemplate().bulkUpdate(queryString, values);
		}catch(RuntimeException re){
			log.error("delete failed",re);
		}
	}
	
}
