package com.seeyon.ctp.common.filemanager.dao;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.po.filemanager.Partition;

/**
 * 
 * @author <a href="mailto:tanmf@seeyon.com">Tanmf</a>
 * @version 1.0 2006-11-15
 */
public class PartitionDAOImpl extends BaseHibernateDao<Partition> implements PartitionDAO {

    @SuppressWarnings("unchecked")
    public List<Partition> findAll() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Partition.class);
        detachedCriteria.addOrder(Order.asc(Partition.PROP_START_DATE));

        return super.executeCriteria(detachedCriteria, -1, -1);
    }

    public void save(Partition partition) {
        super.getHibernateTemplate().save(partition);
    }

    public void update(Partition partition) {
        super.getHibernateTemplate().update(partition);
    }

}