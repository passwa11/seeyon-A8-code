package com.seeyon.ctp.common.filemanager.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.filemanager.Constants;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;

/**
 * 
 * @author <a href="mailto:tanmf@seeyon.com">Tanmf</a>
 * @version 1.0 2006-11-15
 */
public class AttachmentDAOImpl extends BaseHibernateDao<Attachment> implements AttachmentDAO {

    private final static String HQL_UPDATE_REFSUBREF = "update " + Attachment.class.getName() + " a set  a." + Attachment.PROP_REFERENCE
            + "=? , " + Attachment.PROP_SUB_REFERENCE + "=? where  a.fileUrl=? ";
    private final static String HQL_UPDATE_REF= "update " + Attachment.class.getName() + " a set  a." + Attachment.PROP_REFERENCE
            + "=?  where  a.fileUrl=? ";         
    
    public void delete(Long id) {
        String[] columns = { Attachment.PROP_ID };
        Object[] values = { id };

        super.delete(columns, values);
    }

    @SuppressWarnings("unchecked")
    public List<Attachment> findAll(Long reference) {
        String queryString = "from " + Attachment.class.getName() + " a where a." + Attachment.PROP_REFERENCE
                + "=? order by " + Attachment.PROP_SORT + "," + Attachment.PROP_CREATEDATE;

        Object[] values = { reference };
        return super.getHibernateTemplate().find(queryString, values);
    }
    
    @Override
    public java.util.List<Attachment> find(List<Long> parentids) {
        String queryString = "from " + Attachment.class.getName() + " a where a." + Attachment.PROP_FILE_URL
                + " in (:ids) ";

        Map<String, Object> namedParameterMap = new HashMap<String, Object>();
        namedParameterMap.put("ids", parentids);
        return  super.find(queryString, -1, -1, namedParameterMap);
    }
    
    @SuppressWarnings("unchecked")
    public List<Attachment> findAll(Long reference, Long subReference) {
    	if(subReference == null){
    		return this.findAll(reference);
    	}
        String queryString = "from " + Attachment.class.getName() + " a where a." + Attachment.PROP_REFERENCE
                + "=? and " + Attachment.PROP_SUB_REFERENCE + "=? order by " + Attachment.PROP_SORT  + ","
                + Attachment.PROP_CREATEDATE;

        Object[] values = { reference, subReference };
        return super.getHibernateTemplate().find(queryString, values);
    }
    @SuppressWarnings("unchecked")
	public List<Object[]> findAll(Long reference, Integer type,FlipInfo flipInfo) {
		 StringBuilder hql = new StringBuilder();
	        Map<String, Object> params = new HashMap<String, Object>();
	        hql.append("SELECT f.id,f.filename,f.size,f.mimeType,f.createDate, f.createMember");
	        hql.append(" FROM Attachment as a,V3XFile as f");
	        hql.append(" WHERE a.fileUrl=f.id  ");
	        if (reference != null) {
	            hql.append(" AND a.reference=:reference ");
	            params.put("reference", reference);
	        }
	        if (type != null) {
	            hql.append(" AND a.type=:type ");
	            params.put("type", type);
	        }
	        hql.append(" ORDER BY a.createdate DESC");

	        return DBAgent.find(hql.toString(), params, flipInfo);
	}
    @SuppressWarnings("unchecked")
    public List<Attachment> findAll(Long reference, Long... subReference) {
        DetachedCriteria criteria = DetachedCriteria.forClass(Attachment.class)
                .add(Expression.eq(Attachment.PROP_REFERENCE, reference))
                .add(Expression.in(Attachment.PROP_SUB_REFERENCE, subReference))
                .addOrder(Order.asc(Attachment.PROP_SORT)).addOrder(Order.asc(Attachment.PROP_CREATEDATE));

        return super.executeCriteria(criteria, -1, -1);
    }

    public Attachment get(Long id) {
        return super.get(id);
    }

    /**
     * 用户上传的附件，为了防止名称超长，截取120字节
     */
    public void save(Attachment attachment) {
        if (attachment.getType().intValue() == Constants.ATTACHMENT_TYPE.FILE.ordinal()) {
            String fileName = attachment.getFilename();
            if (fileName.getBytes().length > Constants.FILE_NAME_MAX_LENGTH) {
                int pointIndex = fileName.lastIndexOf(".");
                String ext ="";
                if(pointIndex>0) {
                    ext = fileName.substring(pointIndex);
                    fileName = fileName.substring(0, pointIndex);
                }
                fileName = Strings.getLimitLengthString(fileName, Constants.FILE_NAME_MAX_LENGTH, "..") + ext;
                attachment.setFilename(fileName);
            }
        }

        super.getHibernateTemplate().merge(attachment);
    }

    public void deleteByReference(Long reference) {
        String[] columns = { Attachment.PROP_REFERENCE };
        Object[] values = { reference };

        super.delete(columns, values);
    }

    public void deleteByReference(Long reference, Long subReference) {
        String[] columns = { Attachment.PROP_REFERENCE, Attachment.PROP_SUB_REFERENCE };
        Object[] values = { reference, subReference };

        super.delete(columns, values);
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> findAllFileUrl(Long reference) {
        String queryString = "select a." + Attachment.PROP_FILE_URL + ",a." + Attachment.PROP_CREATEDATE + " from "
                + Attachment.class.getName() + " a where a." + Attachment.PROP_REFERENCE + "=?";

        Object[] values = { reference };
        return super.getHibernateTemplate().find(queryString, values);
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> findAllFileUrl(Long reference, Long subReference) {
        String queryString = "select a." + Attachment.PROP_FILE_URL + ",a." + Attachment.PROP_CREATEDATE + " from "
                + Attachment.class.getName() + " a where a." + Attachment.PROP_REFERENCE + "=? and "
                + Attachment.PROP_SUB_REFERENCE + "=?";

        Object[] values = { reference, subReference };
        return super.getHibernateTemplate().find(queryString, values);
    }

    public boolean hasAttachments(Long reference, Long subReference) {
        String queryString = "select count(*) from " + Attachment.class.getName() + " a where a."
                + Attachment.PROP_REFERENCE + "=? and " + Attachment.PROP_SUB_REFERENCE + "=?";

        Object[] values = { reference, subReference };
        int number = ((Number) super.getHibernateTemplate().find(queryString, values).get(0)).intValue();

        return number > 0;
    }

    @SuppressWarnings("unchecked")
    public Attachment getFirst(Long reference, Long subReference, Constants.ATTACHMENT_TYPE type) {
        DetachedCriteria criteria = DetachedCriteria.forClass(Attachment.class)
                .add(Expression.eq(Attachment.PROP_REFERENCE, reference))
                .add(Expression.eq(Attachment.PROP_SUB_REFERENCE, subReference))
                .add(Expression.eq(Attachment.PROP_TYPE, type.ordinal()))
                .addOrder(Order.asc(Attachment.PROP_CREATEDATE));

        List<Attachment> attachments = super.executeCriteria(criteria, 0, 1);
        if (attachments != null && !attachments.isEmpty()) {
            return attachments.get(0);
        }

        return null;
    }

    public Attachment getAttachmentByFileURL(Long fileURL) {
        DetachedCriteria criteria = DetachedCriteria.forClass(Attachment.class).add(
                Expression.eq(Attachment.PROP_FILE_URL, fileURL));

        Attachment attachment = (Attachment) super.executeUniqueCriteria(criteria);

        return attachment;
    }

    public void update(Attachment attachment) {
        super.update(attachment);
    }
    
    public void updateReferenceSubReference(Long fileUrl, Long referenceId, Long subReference){
        super.getHibernateTemplate().bulkUpdate(HQL_UPDATE_REFSUBREF,referenceId,subReference,fileUrl);
    }
    
    public void updateReference(Long fileUrl, Long referenceId){
        super.getHibernateTemplate().bulkUpdate(HQL_UPDATE_REF,referenceId,fileUrl);
    }
    
    public boolean checkIsLicitGenesis(Long referenceId, Long genesisId) {
        String queryString = "select count(*) from " + Attachment.class.getName() + " a where a."
                + Attachment.PROP_REFERENCE + "=? and " + Attachment.Prop_GenesisId + "=?";
        Object[] values = { referenceId, genesisId };
        Long number = (Long) super.getHibernateTemplate().find(queryString, values).get(0);
        return number > 0;
    }
    
    @Override
	public void updateFileNameByAffairIds(String fileName, List<Long> affairIdList) {
    	if(affairIdList != null && affairIdList.size() > 0){
    	    Map<String,Object> params = new HashMap<String, Object>();
    	    params.put("fileName", fileName);
    	    if (affairIdList.size() > 999) {
    	        List<Long>[] idsList = Strings.splitList(affairIdList, 1000);
    	        for (List<Long> ids:idsList) {
    	            params.put("affairIds", ids);
    	            DBAgent.bulkUpdate("update Attachment set fileName =:fileName where fileUrl in (:affairIds)", params);
    	        }
    	    } else {
    	        params.put("affairIds", affairIdList);
    	        DBAgent.bulkUpdate("update Attachment set fileName =:fileName where fileUrl in (:affairIds)", params);
    	    }
		}
	}
    
    @Override
	public List<Long> getBySubReference(Long subReference) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("subReference", subReference);
		return DBAgent.find("select fileUrl from " + Attachment.class.getName() + " where subReference=:subReference", params);
	}
    @Override
	public void deleteByIds(List<Long> attachmentIds) {
		 String hql = "delete from "+Attachment.class.getName()+" as a where a.id in(:id)" ;
	        Map<String,Object> params = new HashMap<String,Object>();
	        params.put("id", attachmentIds);
	        this.bulkUpdate(hql, params);
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Attachment> getBySubReference(List<Long> subReferences) {
        String queryString = "from " + Attachment.class.getName() + " a where a." + Attachment.PROP_SUB_REFERENCE
                + " in (:subReferences) ";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("subReferences", subReferences);
        return  super.find(queryString, -1, -1, map);
	}
}