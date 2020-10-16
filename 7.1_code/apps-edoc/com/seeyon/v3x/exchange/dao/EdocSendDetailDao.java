package com.seeyon.v3x.exchange.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.util.Constants;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.domain.EdocSendDetail;

public class EdocSendDetailDao extends BaseHibernateDao<EdocSendDetail>{
	public EdocSendDetail findDetailBySendId(Long sendId){
		
		String sql =  "from EdocSendDetail as detail where detail.sendRecordId = ? ";
		Object[] values = {sendId};
		
		List<EdocSendDetail> list = super.findVarargs(sql, values); 
		
		if(null!=list && list.size()>0){
			
			return list.get(0);
		}else{
			return null;
		}
	}
	
	public List<EdocSendDetail> findDetailListBySendId(Long sendId){
		String sql =  "from EdocSendDetail as detail where detail.sendRecordId = ? order by detail.recTime";
		Object[] values = {sendId};
		
		List<EdocSendDetail> list = super.findVarargs(sql, values); 
		return list;		
	}
	public List<EdocSendDetail> getDetailBySendRecOrgId(String recOrgId){
		String sql =  "from EdocSendDetail as detail where detail.recOrgId = ? order by detail.recTime";
		Object[] values = {recOrgId};
		
		List<EdocSendDetail> list = super.findVarargs(sql, values); 
		return list;
	}
	/**
	 * 检查如果不是自己单位建的签收编号，如果不是的话是没有修改权限的
	 * @param edocRecieveRecordId
	 * @return
	 */
	public String isEditEdocMark(String markId,String accountId){
		String flag = "false";
		Map<String, Object> params = new HashMap<String, Object>();
		StringBuilder sb = new StringBuilder();
		sb.append("select id from EdocMarkDefinition where id=:markId and domainId=:accountId");
		params.put("markId", Long.parseLong(markId));
		params.put("accountId", Long.parseLong(accountId));
		List<EdocMarkDefinition> list = super.find(sb.toString(), params);
		if(list.size()>0){
			flag = "true";
		}
		return flag;
	}
	/**
	 * 查询本单位的签收编号
	 */
	public List<Object[]> getMarkList1(Long accountId,Long depId){
		String sql =  "select e.id,e.wordNo,e.expression,c.yearEnabled,c.maxNo,e.length,c.currentNo from EdocMarkDefinition as e,EdocMarkCategory as c,EdocMarkAcl as a  where e.id=a.markDefId and c.id = e.categoryId and e.domainId = ? and e.markType=2 and(a.deptId=? or a.deptId=?) and e.status!=?";
		Object[] values = {accountId,accountId,depId, Constants.EDOC_MARK_DEFINITION_DELETED};
		List<Object[]> list = super.findVarargs(sql, values); 
		return list;	
	}
	/**
	 * 查询外单位授权给自己的签收编号
	 */
	public List<Object[]> getMarkList2(Long accountId,Long depId){
		String sql =  "select e.id,e.wordNo,e.expression,c.yearEnabled,c.maxNo,e.length,c.currentNo from EdocMarkDefinition as e,EdocMarkAcl as a,EdocMarkCategory as c  where e.id=a.markDefId and c.id = e.categoryId and (a.deptId = ? or a.deptId = ?) and e.markType=2 and e.status!=?";
		Object[] values = {accountId,depId, Constants.EDOC_MARK_DEFINITION_DELETED};
		List<Object[]> list = super.findVarargs(sql, values); 
		return list;	
	}
	
	/**
	 * 查询自己能看到的签收编号
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> getMarkList(List<Long> deptIdList) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("select e.id,e.wordNo,e.expression,c.yearEnabled,c.maxNo,e.length,c.currentNo from EdocMarkDefinition as e,EdocMarkAcl as a,EdocMarkCategory as c");
		buffer.append(" where e.id=a.markDefId and c.id = e.categoryId");
		buffer.append(" and a.deptId in (:deptId)");
		buffer.append(" and e.markType=2 and e.status!=:status");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("deptId", deptIdList);
		paramMap.put("status", Constants.EDOC_MARK_DEFINITION_DELETED);
		List<Object[]> list = super.find(buffer.toString(), paramMap); 
		return list;	
	}
	
	
	/**
	 * 检查签收编号是否重复
	 * @param edocRecieveRecordId
	 * @return
	 */
	//判断签收编号是否重复 已经改为调用findReceiveRecordsByEdocMark方法先根据签收编号获取所有的签收记录
	//然后在manager层在进行业务判断
	@Deprecated
	public String isEditEdocMarkExist(String recNo,String accountId,String depId){
		String flag = "false";
		Map<String, Object> params = new HashMap<String, Object>();
		StringBuilder sb = new StringBuilder();
		sb.append("select id from EdocRecieveRecord where recNo=:recNo and (exchangeOrgId=:accountId or exchangeOrgId=:depId)");
		params.put("recNo", recNo);
		params.put("accountId", Long.parseLong(accountId));
		params.put("depId", Long.parseLong(depId));
		List<EdocRecieveRecord> list = super.find(sb.toString(), params);
		if(list.size()>0){
			flag = "true";
		}
		return flag;
	}
	
	/**
	 * 根据签收编号获取签收记录
	 * @param recNo
	 * @return
	 */  
	public List<EdocRecieveRecord> findReceiveRecordsByEdocMark(String recNo, Long recieveId) {
	    String flag = "false";
        Map<String, Object> params = new HashMap<String, Object>();
        StringBuilder sb = new StringBuilder();
        sb.append("from EdocRecieveRecord where recNo=:recNo and id!=:recieveId");
        params.put("recNo", recNo);
        params.put("recieveId", recieveId);
        List<EdocRecieveRecord> list = super.find(sb.toString(), params);
	    return list;
	}
	
	/**
	 * 检查签收编号是否重复
	 * @param edocRecieveRecordId
	 * @return
	 */
	public List<String> getMarkList(Long accountId,Long depId){
		Map<String, Object> params = new HashMap<String, Object>();
		StringBuilder sb = new StringBuilder();
		sb.append("select recNo from EdocRecieveRecord where exchangeOrgId=:accountId or exchangeOrgId=:depId");
		params.put("accountId", accountId);
		params.put("depId", depId);
		List<String> list = super.find(sb.toString(),-1,-1, params);
		return list;
	}
}
