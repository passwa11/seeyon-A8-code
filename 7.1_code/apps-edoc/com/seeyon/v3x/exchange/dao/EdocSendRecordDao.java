package com.seeyon.v3x.exchange.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.exchange.domain.EdocSendRecord;
import com.seeyon.ctp.util.Datetimes;

public class EdocSendRecordDao extends BaseHibernateDao<EdocSendRecord>{
	private static final Log log = LogFactory.getLog(EdocSendRecordDao.class);
	public List<EdocSendRecord> getEdocSendRecords(int status) {
		String hsql = "from EdocSendRecord as a where a.status=? order by a.createTime";
		return super.findVarargs(hsql, status);
	}
	
	/**
	 * 获得已经分发的公文交换记录
	 * @param edocId
	 * @return
	 */
	public EdocSendRecord getEdocSendRecordByEdocId(long edocId){
		return getEdocSendRecordByEdocId(edocId,1);
	}
	
	public EdocSendRecord getEdocSendRecordByEdocId(long edocId,int status){
		String hsql = "from EdocSendRecord as a where a.status=:status and edocId=:edocId order by a.createTime";
		Map map = new HashMap();
		map.put("status", status);
		map.put("edocId", edocId);
		EdocSendRecord result = null;
		List<EdocSendRecord> records = super.find(hsql, map);
		if(records != null && records.size() == 1){
			result = records.get(0);
		}
		return result;
	}
	
	public List<EdocSendRecord> findEdocSendRecordsByEdocId(long edocId){
		String hsql = "from EdocSendRecord as a where edocId=:edocId order by a.createTime";
		Map map = new HashMap();
		map.put("edocId", edocId);
		EdocSendRecord result = null;
		List<EdocSendRecord> records = super.find(hsql, map);
		return records;
	}
	
	
	/**
	 * 获得公文交换记录
	 * @param edocId
	 * @return
	 */
	public List<EdocSendRecord> getEdocSendRecordOnlyByEdocId(long edocId){
		String hsql = "from EdocSendRecord as a where  edocId=:edocId order by a.createTime";
		Map map = new HashMap();
		map.put("edocId", edocId);
		EdocSendRecord result = null;
		List<EdocSendRecord> records = super.find(hsql, map);

		return records;
	}
	
	
	/**
	 * 根据以下参数获得公文交换记录
	 * @param edocId
	 * @param exchangeOrgId
	 * @param status
	 * @return
	 */
	public List<EdocSendRecord> getEdocSendRecordByEdocIdAndOrgIdAndStatus(long edocId, long exchangeOrgId,int status){
		List<Integer> statusList = new ArrayList<Integer>();
		if (status == EdocSendRecord.Exchange_iStatus_Tosend) {// 待发送列表
			statusList.add(status);
			statusList.add(EdocSendRecord.Exchange_iStatus_Send_New_StepBacked);
			statusList.add(EdocSendRecord.Exchange_iStatus_Send_New_Cancel);
		}else{
			statusList.add(status);
			statusList.add(EdocSendRecord.Exchange_iStatus_Send_StepBacked);
		}
		
		String hsql = "from EdocSendRecord as a where  a.edocId=:edocId and a.exchangeOrgId=:exchangeOrgId and a.status in (:statusList) ";
		
		Map map = new HashMap();
		map.put("edocId", edocId);
		map.put("exchangeOrgId", exchangeOrgId);
		map.put("statusList", statusList);
		EdocSendRecord result = null;
		List<EdocSendRecord> records = super.find(hsql, map);

		return records;
	}
	
	/**
	 * 根据以下参数获得公文交换记录
	 * @param exchangeOrgId
	 * @param status
	 * @return
	 */
	public List<EdocSendRecord> getEdocSendRecordByOrgIdAndStatus(long exchangeOrgId,int status){
		String hsql = "from EdocSendRecord as a where a.exchangeOrgId=:exchangeOrgId and a.status in (:statusList)  ";
		List<Integer> statusList = new ArrayList<Integer>();
		Map map = new HashMap();
		map.put("exchangeOrgId", exchangeOrgId);
		if (status == EdocSendRecord.Exchange_iStatus_Tosend) {// 待发送列表
			statusList.add(status);
			statusList.add(EdocSendRecord.Exchange_iStatus_Send_New_StepBacked);
			statusList.add(EdocSendRecord.Exchange_iStatus_Send_New_Cancel);
		}else{
			statusList.add(status);
			statusList.add(EdocSendRecord.Exchange_iStatus_Send_StepBacked);
		}
		map.put("statusList", statusList);
		EdocSendRecord result = null;
		List<EdocSendRecord> records = super.find(hsql, map);

		return records;
	}
	
	/**
	 * 获得已经分发的公文交换记录
	 * @param edocId
	 * @return
	 */
	public EdocSendRecord getEdocSendRecordById(long id){
		String hsql = "from EdocSendRecord as a where a.status=:status and id=:id order by a.createTime";
		Map map = new HashMap();
		map.put("status", 1);
		map.put("id", id);
		EdocSendRecord result = null;
		List<EdocSendRecord> records = super.find(hsql, map);
		if(records != null && records.size() == 1){
			result = records.get(0);
		}
		return result;
	}
	
	
	/**
	 * 查询待交换记录
	 * @param isAccount：是否为单位收发员
	 * @param accountId：当前单位ID
	 * @param departIds：任部门收发员的部门id串，以逗号分割
	 * @param status
	 * @return
	 */
	public List<EdocSendRecord> findEdocSendRecords(String accountIds,String departIds,int status,String condition,String value) {
		String accWhere=null;
		String depWhere=null;
		boolean bool = false;
		if((accountIds==null || "".equalsIgnoreCase(accountIds)) && (departIds==null || "".equalsIgnoreCase(departIds)))
		{
			return null;
		}
		String hsql = "";
		if (status == EdocSendRecord.Exchange_iStatus_Tosend) {
			// 待发送，包括发送已回退的
			hsql = "from EdocSendRecord as a where (a.status=? or a.status=2)";
		} else {
			// 已发送
			hsql = "from EdocSendRecord as a where (a.status=?)";
		}
		if(null!=condition && !"".equals(condition) && null!=value && !"".equals(value)){
			hsql += " and a."+ condition + " like ? ";
			bool = true;
		}
		if(accountIds!=null && !"".equals(accountIds))
		{
			accWhere=" (exchangeType="+ EdocSendRecord.Exchange_Send_iExchangeType_Org;
			accWhere+=" and exchangeOrgId in ("+accountIds+"))";
		}
		if(departIds!=null && !"".equals(departIds))
		{
			depWhere=" (exchangeType="+ EdocSendRecord.Exchange_Send_iExchangeType_Dept;
			depWhere+=" and exchangeOrgId in ("+departIds+"))";
		}
		if(accWhere!=null && depWhere!=null)
		{
			hsql+=" and ("+accWhere+" or "+depWhere+")";
		}
		else if(accWhere!=null && depWhere==null)
		{
			hsql+=" and "+accWhere;
		}
		else if(accWhere==null && depWhere!=null)
		{
			hsql+=" and "+depWhere;
		}
		if(status==0){
			//未发送
			hsql+=" order by a.createTime desc";
		}else{
			//已发送
			hsql+=" order by a.sendTime desc";
		}
		if(bool){
			return super.findVarargs(hsql, status, "%"+value+"%");
		}else{
			return super.findVarargs(hsql, status);
		}
	}	
	
	
	/**
	 * 查询待交换记录
	 * @param isAccount：是否为单位收发员
	 * @param accountId：当前单位ID
	 * @param departIds：任部门收发员的部门id串，以逗号分割
	 * @param status
	 * @param dateType 时间类型（本日，昨日，本周等）
	 * @return
	 */
	public List<EdocSendRecord> findEdocSendRecords(String accountIds,String departIds,int status,String condition,String value,int dateType,long userId) {
		String accWhere=null;
		String depWhere=null;
		if((accountIds==null || "".equalsIgnoreCase(accountIds)) && (departIds==null || "".equalsIgnoreCase(departIds))) {
			return null;
		}
		List<Integer> statusList = new ArrayList<Integer>();
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		String hsql = "";
		if (status == EdocSendRecord.Exchange_iStatus_Tosend) {// 待发送列表
			hsql = "select a from EdocSendRecord as a where";
			statusList.add(status);
			statusList.add(EdocSendRecord.Exchange_iStatus_Send_New_StepBacked);
			statusList.add(EdocSendRecord.Exchange_iStatus_Send_New_Cancel);
			
			if(accountIds!=null && !"".equals(accountIds)) {
				//因为oracle的clob字段不支持distinct，所以这里改为子查询，在子查询中先idstinct出id
//				hsql = "select a from EdocSendRecord as a where a.id in ( select distinct b.id from EdocSendRecord b, Affair c where " +
//						" b.id = c.subObjectId and (a.status=:status) and c.memberId = :memberId  and c.app = :app )";
				
				//GOV-4892 发文--分发，对补发的公文进行撤销，撤销后，该公文丢失，在分发--"待分发""已分发"中均无此公文
				//在已分发列表中 进行补发时，现在的业务逻辑不会向v3x_affair表中增加记录
				//因此将补发的撤销后，上面的sql中通过 b.id = c.subObjectId 关联就查不出撤销的分发记录了
				/*hsql = "select a from EdocSendRecord as a where a.id in ( select distinct b.id from EdocSendRecord b, CtpAffair c,EdocSummary d where " +
				" c.objectId = d.id and b.edocId = d.id and (a.status=:status)  and c.app = :app )";
				
				//parameterMap.put("memberId", userId);
				parameterMap.put("app", ApplicationCategoryEnum.exSend.key());*/
			}
		} else {// 已发送，包括发送已回退的
			hsql = "from EdocSendRecord as a where";
			statusList.add(status);
			statusList.add(EdocSendRecord.Exchange_iStatus_Send_StepBacked);
		}
		hsql += " a.status in (:statusList) and (a.sendUserId=0 or a.sendUserId=:sendUserId)";
		parameterMap.put("statusList", statusList);
		parameterMap.put("sendUserId", userId);
		
		if(dateType>0) {
			String[] dateFields=com.seeyon.v3x.edoc.util.DateUtil.getTimeTextFiledByTimeEnum(dateType);
  			if (StringUtils.isNotBlank(dateFields[0])) {
  				java.util.Date stamp1 = Datetimes.getTodayFirstTime(dateFields[0]);
  				hsql = hsql + " and a.createTime >=:timestamp1 " ;
  				parameterMap.put("timestamp1", stamp1);
  			}
  			if (StringUtils.isNotBlank(dateFields[1])) {
  				java.util.Date stamp2 = Datetimes.getTodayLastTime(dateFields[1]);
  				hsql = hsql + " and a.createTime <=:timestamp2 " ;
  				parameterMap.put("timestamp2", stamp2);
  			}
		}
		
		if(null!=condition && !"".equals(condition) && null!=value && !"".equals(value)){
			hsql += " and a."+ condition + " like :condition ";
			parameterMap.put("condition", "%"+value+"%");
		}
		
		if(accountIds!=null && !"".equals(accountIds)) {
			accWhere=" (a.exchangeType="+ EdocSendRecord.Exchange_Send_iExchangeType_Org;
			accWhere+=" and a.exchangeOrgId in ("+accountIds+"))";
		}
		if(departIds!=null && !"".equals(departIds)) {
			depWhere=" (a.exchangeType="+ EdocSendRecord.Exchange_Send_iExchangeType_Dept;
			depWhere+=" and a.exchangeOrgId in ("+departIds+"))";
		}
		if(accWhere!=null && depWhere!=null) {
			hsql+=" and ("+accWhere+" or "+depWhere+")";
		} else if(accWhere!=null && depWhere==null) {
			hsql+=" and "+accWhere;
		} else if(accWhere==null && depWhere!=null) {
			hsql+=" and "+depWhere;
		}
		if(status==0){//未发送
			hsql+=" order by a.createTime desc";
		}else{//已发送
			hsql+=" order by a.sendTime desc";
		}		
		return super.find(hsql,parameterMap);
	}
	
	/********************************************公文交换列表       start*************************************************************/
	/**
	 * 查询待交换记录 5.0sp1
	 * @param type
	 * @param condition
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<EdocSendRecord> findEdocSendRecordList(int type, Map<String, Object> condition) {
		String conditionKey = condition.get("conditionKey")==null?null:(String)condition.get("conditionKey");;
		String textfield = condition.get("textfield")==null?null:(String)condition.get("textfield");
		String textfield1 = condition.get("textfield1")==null?null:(String)condition.get("textfield1");
		String accountIds = condition.get("accountIds")==null?null:(String)condition.get("accountIds");
		String departIds = condition.get("departIds")==null?null:(String)condition.get("departIds");
		List<Integer> statusList = condition.get("statusList")==null?null:(List<Integer>)condition.get("statusList");
		int state = condition.get("state")==null ? 3 : (Integer)condition.get("state");
		User user = (User)condition.get("user");

		Map<String, Object> parameterMap = new HashMap<String, Object>();
		StringBuffer buffer = new StringBuffer();
		buffer.append("from EdocSendRecord as a where (a.sendUserId=:sendUserId or a.assignType=:assignType)");
//		if(state == StateEnum.col_pending.key()) {//待发送列表
//			buffer.append(" and (a.status in (:statusList) or (a.status=:status and a.sendUserId=:sendUserId))");
//			parameterMap.put("status", EdocSendRecord.Exchange_iStatus_Send_New_Cancel);
		buffer.append(" and (a.status in (:statusList) )");
//		} else {//已发送列表
//			buffer.append(" and a.status in (:statusList)");
//		}
		parameterMap.put("sendUserId", user.getId());
		parameterMap.put("assignType", EdocSendRecord.Exchange_Assign_To_All);
		parameterMap.put("statusList", statusList);

		if(Strings.isNotBlank(accountIds) && Strings.isBlank(departIds)) {//单位公文收发员
			buffer.append(" and (a.exchangeType=:exchangeType and a.exchangeOrgId in (:accountIds))");
			parameterMap.put("exchangeType", EdocSendRecord.Exchange_Send_iExchangeType_Org);
			List<Long> accountIdArray = com.seeyon.v3x.edoc.util.StringUtils.convertStringToLongList(accountIds, ",");
			parameterMap.put("accountIds", accountIdArray);
		} else if(Strings.isBlank(accountIds) && Strings.isNotBlank(departIds)) {//部门公文收发员
			buffer.append(" and (a.exchangeType=:exchangeType and a.exchangeOrgId in (:departIds))");
			parameterMap.put("exchangeType", EdocSendRecord.Exchange_Send_iExchangeType_Dept);
			List<Long> departIdsArray = com.seeyon.v3x.edoc.util.StringUtils.convertStringToLongList(departIds, ",");
			parameterMap.put("departIds", departIdsArray);
		} else if(Strings.isNotBlank(accountIds) && Strings.isNotBlank(departIds)) {//单位公文收发员、部门公文收发员
			buffer.append(" and (");
			buffer.append(" (a.exchangeType=:exchangeTypeOrg and a.exchangeOrgId in (:accountIds))");
			buffer.append(" or ");
			buffer.append(" (a.exchangeType=:exchangeTypeDept and a.exchangeOrgId in (:departIds))");
			buffer.append(" )");
			parameterMap.put("exchangeTypeOrg", EdocSendRecord.Exchange_Send_iExchangeType_Org);
			parameterMap.put("exchangeTypeDept", EdocSendRecord.Exchange_Send_iExchangeType_Dept);
			List<Long> accountIdArray = com.seeyon.v3x.edoc.util.StringUtils.convertStringToLongList(accountIds, ",");
			parameterMap.put("accountIds", accountIdArray);
			List<Long> departIdsArray = com.seeyon.v3x.edoc.util.StringUtils.convertStringToLongList(departIds, ",");
			parameterMap.put("departIds", departIdsArray);
		} else { //两个角色都不是，做防护
			buffer.append(" and 1=0 ");
		}
		if(Strings.isNotBlank(conditionKey)) {
			if("createTime".equals(conditionKey)) {
				if(Strings.isNotBlank(textfield)) {
					java.util.Date stamp1 = null;
					try {
						stamp1 = DateUtil.parse(textfield);
					}catch (Exception e) {
						log.error("", e);
					}
					buffer.append(" and a.createTime >=:timestamp1");
					parameterMap.put("timestamp1", stamp1);
				}
				if(Strings.isNotBlank(textfield1)) {
					java.util.Date stamp2 = null;
					try {
						stamp2 = DateUtil.parse(textfield1);
					}catch (Exception e) {
						log.error("", e);
					}
					buffer.append(" and a.createTime <=:timestamp2");
					parameterMap.put("timestamp2", stamp2);
				}
			} 
			else if("exchangeType".equals(conditionKey)) {
				if("0".equals(textfield)||"1".equals(textfield)){
					buffer.append(" and a.isTurnRec = :isTurnRec ");
					parameterMap.put("isTurnRec", Integer.parseInt(textfield));
				}
			}else if("exchangeMode".equals(conditionKey)) {
				if("0".equals(textfield)||"1".equals(textfield)){
					buffer.append(" and a.exchangeMode = :exchangeMode ");
					parameterMap.put("exchangeMode", Integer.parseInt(textfield));
				}
			}
			else {
				if(Strings.isNotBlank(textfield)) {
					buffer.append(" and a."+ conditionKey + " like :conditionKey ");
					parameterMap.put("conditionKey", "%"+textfield+"%");
				}
			}
		}
		if(state == StateEnum.col_pending.key()) {//待发送列表
			buffer.append(" order by a.createTime desc");
		} else {//已发送列表
			buffer.append(" order by a.sendTime desc");
		}
		return super.find(buffer.toString(), parameterMap);
	}
	/**
	 * 查询待交换记录 5.0sp1
	 * @param type
	 * @param condition
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public int findEdocSendRecordCount(int type, Map<String, Object> condition) {
		String conditionKey = condition.get("conditionKey")==null?null:(String)condition.get("conditionKey");;
		String textfield = condition.get("textfield")==null?null:(String)condition.get("textfield");
		String textfield1 = condition.get("textfield1")==null?null:(String)condition.get("textfield1");
		String accountIds = condition.get("accountIds")==null?null:(String)condition.get("accountIds");
		String departIds = condition.get("departIds")==null?null:(String)condition.get("departIds");
		List<Integer> statusList = condition.get("statusList")==null?null:(List<Integer>)condition.get("statusList");
		int state = condition.get("state")==null ? 3 : (Integer)condition.get("state");
		User user = (User)condition.get("user");

		Map<String, Object> parameterMap = new HashMap<String, Object>();
		StringBuffer buffer = new StringBuffer();
		buffer.append("from EdocSendRecord as a where (a.sendUserId=:sendUserId or a.assignType=:assignType)");
//		if(state == StateEnum.col_pending.key()) {//待发送列表
//			buffer.append(" and (a.status in (:statusList) or (a.status=:status and a.sendUserId=:sendUserId))");
//			parameterMap.put("status", EdocSendRecord.Exchange_iStatus_Send_New_Cancel);
		buffer.append(" and (a.status in (:statusList) )");
//		} else {//已发送列表
//			buffer.append(" and a.status in (:statusList)");
//		}
		parameterMap.put("sendUserId", user.getId());
		parameterMap.put("assignType", EdocSendRecord.Exchange_Assign_To_All);
		parameterMap.put("statusList", statusList);

		if(Strings.isNotBlank(accountIds) && Strings.isBlank(departIds)) {//单位公文收发员
			buffer.append(" and (a.exchangeType=:exchangeType and a.exchangeOrgId in (:accountIds))");
			parameterMap.put("exchangeType", EdocSendRecord.Exchange_Send_iExchangeType_Org);
			List<Long> accountIdArray = com.seeyon.v3x.edoc.util.StringUtils.convertStringToLongList(accountIds,",");
//			String[] accountIdArray = accountIds.split(",");
			parameterMap.put("accountIds", accountIdArray);
		} else if(Strings.isBlank(accountIds) && Strings.isNotBlank(departIds)) {//部门公文收发员
			buffer.append(" and (a.exchangeType=:exchangeType and a.exchangeOrgId in (:departIds))");
			parameterMap.put("exchangeType", EdocSendRecord.Exchange_Send_iExchangeType_Dept);
			List<Long> departIdsArray = com.seeyon.v3x.edoc.util.StringUtils.convertStringToLongList(departIds, ",");
			parameterMap.put("departIds", departIdsArray);
		} else if(Strings.isNotBlank(accountIds) && Strings.isNotBlank(departIds)) {//单位公文收发员、部门公文收发员
			buffer.append(" and (");
			buffer.append(" (a.exchangeType=:exchangeTypeOrg and a.exchangeOrgId in (:accountIds))");
			buffer.append(" or ");
			buffer.append(" (a.exchangeType=:exchangeTypeDept and a.exchangeOrgId in (:departIds))");
			buffer.append(" )");
			parameterMap.put("exchangeTypeOrg", EdocSendRecord.Exchange_Send_iExchangeType_Org);
			parameterMap.put("exchangeTypeDept", EdocSendRecord.Exchange_Send_iExchangeType_Dept);
			List<Long> accountIdArray = com.seeyon.v3x.edoc.util.StringUtils.convertStringToLongList(accountIds, ",");
			parameterMap.put("accountIds", accountIdArray);
			List<Long> departIdsArray = com.seeyon.v3x.edoc.util.StringUtils.convertStringToLongList(departIds, ",");
			parameterMap.put("departIds", departIdsArray);
		} else { //两个角色都不是，做防护
			buffer.append(" and 1=0 ");
		}
		if(state == StateEnum.col_pending.key()) {//待发送列表
			buffer.append(" order by a.createTime desc");
		} else {//已发送列表
			buffer.append(" order by a.sendTime desc");
		}
		return super.count(buffer.toString(), parameterMap);
	}
	/**
	 * 逻辑删除发文交换数据
	 * @param ids
	 * @param status
	 * @throws BusinessException
	 */
	public void deleteEdocSendRecordByLogic(Object[] ids, int status) throws BusinessException {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		String hql = "update EdocSendRecord set status=:status where id in (:ids)";
		parameterMap.put("status", status);
		parameterMap.put("ids", ids);
		super.bulkUpdate(hql, parameterMap);
	}
	/********************************************公文交换列表       end*************************************************************/
	
	public EdocSendRecord getSourceSendRecord(long edocId){
	    String hql = "from EdocSendRecord r where r.edocId = :edocId and r.isBase = :isBase ";
	    Map<String, Object> map = new HashMap<String, Object>();
	    map.put("edocId", edocId);
	    map.put("isBase", 1);
	    List<EdocSendRecord> sends = super.find(hql, map);
	    return sends.get(0);
	}
	
	public EdocSendRecord getFirstSendRecord(Long edocId){
		String hsql = "from EdocSendRecord as a where a.edocId=:edocId order by a.createTime";
		Map map = new HashMap();
		map.put("edocId", edocId);
		EdocSendRecord result = null;
		List<EdocSendRecord> records = super.find(hsql, map);
		if(records != null && records.size() > 1){
			result = records.get(0);
		}
		return result;
	}
}
