package com.seeyon.v3x.edoc.dao;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.edoc.domain.EdocSupervisor;
import com.seeyon.v3x.edoc.exception.EdocException;

public class EdocSupervisorDao extends BaseHibernateDao<EdocSupervisor>{
	public void deleteByStatus(long superviseId,long supervisorId)throws Exception{
		String queryString = "delete from EdocSupervisor as sor where sor.superviseId = ? and sor.supervisorId = ?";
		Object[] values = new Object[]{superviseId, supervisorId};
		try{
			super.bulkUpdate(queryString, null, values);
		}catch(Exception e){
			throw new EdocException(e);
		}
	}
	
	/**
	 * 根据督办明细的ID删除该督办下的所有督办人记录
	 * @param superviseDetailId
	 * @throws Exception
	 */
	public void deleteSupervisorsByDetailId(long superviseDetailId)throws Exception{
		
		String queryString = "delete from EdocSupervisor as sor where sor.superviseId = ? ";
		Object[] values = new Object[]{superviseDetailId};
		try{
			super.bulkUpdate(queryString, null, values);
			}catch(Exception e){
				throw new EdocException(e);
			}		
	}
	
	/**
	 * 根据督办明细的ID和督办人的ID删除该督办人在特定督办条目下的下的督办人记录
	 * @param detailId
	 * @param supervisorId
	 * @throws Exception
	 */
	public void deleteSupervisorsBySupervisorIdAndDetailId(long detailId, long supervisorId)throws Exception{
		
		String queryString = "delete from EdocSupervisor as sor where sor.superviseId = ? and sor.supervisorId = ?";
		Object[] values = new Object[]{detailId, supervisorId};
		try{
			super.bulkUpdate(queryString, null, values);
		}catch(Exception e){
			throw new Exception(e);
		}
		
	}
}
