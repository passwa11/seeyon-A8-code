package com.seeyon.ctp.common.filemanager.dao;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.filemanager.Constants;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.util.Strings;

/**
 * 
 * @author <a href="mailto:tanmf@seeyon.com">Tanmf</a>
 * @version 1.0 2006-11-15
 */
public class V3XFileDAOImpl extends BaseHibernateDao<V3XFile> implements V3XFileDAO {
    /*
     * (non-Javadoc)
     * @see com.seeyon.v3x.common.filemanager.dao.V3XFileDAO#save(com.seeyon.v3x.common.filemanager.V3XFile)
     */
    public void save(V3XFile file) {
        if (file.getUpdateDate() == null) {
            file.setUpdateDate(file.getCreateDate());
        }
        
        String fileName = file.getFilename();
        if (fileName.getBytes().length > Constants.FILE_NAME_MAX_LENGTH) {
            int pointIndex = fileName.lastIndexOf(".");
            String ext = "";
            if(pointIndex > 0){
                ext = fileName.substring(pointIndex);
                fileName = fileName.substring(0, pointIndex);
            }
            
            fileName = Strings.getSafeLimitLengthString(fileName, Constants.FILE_NAME_MAX_LENGTH, "..") + ext;
            file.setFilename(fileName);
        }
        
        super.getHibernateTemplate().merge(file);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.seeyon.v3x.common.filemanager.dao.FileMappingDAO#save(java.util.List)
     */
    public void save(List<V3XFile> files) {
        for (V3XFile file : files) {
            this.save(file);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.seeyon.v3x.common.filemanager.dao.FileMappingDAO#delete(java.lang.Long)
     */
    public void delete(Long id) {
        super.delete(id.longValue());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.seeyon.v3x.common.filemanager.dao.FileMappingDAO#get(java.lang.Long)
     */
    public V3XFile get(Long id) {
        try {
            return super.get(id);
        } catch (Exception e) {
            return null;
        }

    }

    @SuppressWarnings("unchecked")
    public List<V3XFile> get(Long[] ids) {
        if (ids == null || ids.length < 1) {
            return null;
        }

        DetachedCriteria criteria = DetachedCriteria.forClass(V3XFile.class).add(Expression.in(V3XFile.PROP_ID, ids));

        return super.executeCriteria(criteria, -1, -1);
    }

    @SuppressWarnings("unchecked")
    public List<V3XFile> findByFileName(String fileName) {
        DetachedCriteria criteria = DetachedCriteria.forClass(V3XFile.class).add(
                Expression.eq(V3XFile.PROP_FILENAME, fileName));

        return super.executeCriteria(criteria, -1, -1);
    }

    public void update(V3XFile id) {
        super.update(id);
    }

}