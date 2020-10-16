package com.seeyon.apps.govdoc.dao;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.govdoc.po.EdocLeaderPishiNo;
import com.seeyon.apps.govdoc.po.EdocStatSet;
import com.seeyon.apps.govdoc.po.EdocUserLeaderRelation;
import com.seeyon.apps.govdoc.po.GovdocLeaderSerialShortname;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.ctp.util.Strings;

public class GovdocPishiDao extends BaseHibernateDao<EdocStatSet> {
	
	private static final  String pishiHql = ""
			+ "s.complete_time,"
			+ "s.identifier,"
			+ "s.serial_no,"
			+ "s.doc_mark,"
			+ "s.current_nodes_info,"
			+ "s.CREATE_TIME,"
			+ "s.update_time,"
			+ "s.govdoc_type,"
			+ "c.id,"
			+ "c.member_id,"
			+ "c.OBJECT_ID,"
			+ "c.SUB_STATE,"
			+ "c.UPDATE_DATE,"
			+ "c.SUBJECT,c.app,"
			+ "c.receive_time,"
			+ "c.sub_object_id,"
			+ "c.sub_App,"
			+ "c.body_type,"
			+ "c.is_cover_Time,"
			+ "c.expected_process_time,"
			+ "c.deadline_date";

	@SuppressWarnings("unchecked")
	public Integer findMaxPishino(Long summaryId,Object pishiName,Integer pishiYear) throws BusinessException{
		String hql = "SELECT e FROM EdocLeaderPishiNo as e where e.pishiName= :pishiName and e.pishiYear= :pishiYear order by e.pishiNo desc";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("pishiName", pishiName.toString());
		map.put("pishiYear", pishiYear.toString());
		List<EdocLeaderPishiNo> list= DBAgent.find(hql,map);
		if(list.isEmpty()){
			return 1;
		}else{
			return list.get(0).getPishiNo()+1;	
		}
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Integer> findPishinoMap(String pishiName, List<String> pishiYears) throws BusinessException {
		Map<String, Integer> pishiMap = new HashMap<String, Integer>();
		String hql = "SELECT e.pishiYear,e.pishiNo FROM EdocLeaderPishiNo as e where e.pishiName= :pishiName and e.pishiYear in (:pishiYears) order by e.pishiNo";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("pishiName", pishiName.toString());
		map.put("pishiYears", pishiYears);
		List<Object[]> list= DBAgent.find(hql,map);
		if(Strings.isNotEmpty(list)) {
			for(Object[] Objects : list) {
				pishiMap.put(Objects[0].toString(), (Integer)Objects[1] + 1);
			}
		}
		for(String pishiYear:pishiYears){
			if(pishiMap.get(pishiYear) == null){
				pishiMap.put(pishiYear, 1);
			}
		}
		return pishiMap;
	}
	
	@SuppressWarnings("deprecation")
	public void insertLeaderPishiNo(EdocLeaderPishiNo edocLeaderPishiNo) throws BusinessException{
		DBAgent.saveOrUpdate(edocLeaderPishiNo);
	}
	
	@SuppressWarnings("unchecked")
	public Boolean checkPishiNo(Integer pishiNo,String pishiName,String pishiYear,Long summaryId) throws BusinessException{
		String hql="SELECT l FROM EdocLeaderPishiNo as l where l.pishiNo= :pishiNo and l.pishiName= :pishiName and l.pishiYear= :pishiYear";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("pishiNo", pishiNo);
		params.put("pishiName", pishiName);
		params.put("pishiYear", pishiYear);
		List<EdocLeaderPishiNo> leaderPishiNos = DBAgent.find(hql,params);
		if(leaderPishiNos.isEmpty()){
			return true;
		}else{
			return false;
		}
	}
	
	public void deletePishiNoBySummaryId(Long summaryId) throws BusinessException{
	    String hql = "delete from EdocLeaderPishiNo as pishiNo where pishiNo.summaryId=:summaryId ";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("summaryId", summaryId);
        DBAgent.bulkUpdate(hql, map);
	}
	public void deletePishiNoByAffairId(Long affairId,String pishiName) throws BusinessException{
        Map<String, Object> map = new HashMap<String, Object>();
	    String hql = "delete from EdocLeaderPishiNo as pishiNo where pishiNo.affairId=:affairId  ";
	    if(pishiName != null && !"".equals(pishiName)){
	    	hql += " and pishiNo.pishiName =:pishiName";
	        map.put("pishiName", pishiName);
	    }
        map.put("affairId", affairId);
        DBAgent.bulkUpdate(hql, map);
	}
	@SuppressWarnings("unchecked")
	public List<EdocLeaderPishiNo> getAllLeaderPishi(Long summaryId) {
		Map<String, Object> params = new HashMap<String, Object>();
		String hql="SELECT l FROM EdocLeaderPishiNo as l WHERE l.summaryId=:summaryId ORDER BY l.leaderSortId ASC, l.pishiNo DESC";
		params.put("summaryId", summaryId);
		return DBAgent.find(hql,params);
	}
	@SuppressWarnings("unchecked")
	public List<EdocLeaderPishiNo> getLeaderPishiByAffairIds(List<Long> affairIds) {
		Map<String, Object> params = new HashMap<String, Object>();
		String hql="SELECT l FROM EdocLeaderPishiNo as l WHERE l.affairId in (:affairIds) ORDER BY l.leaderSortId ASC, l.pishiNo DESC";
		params.put("affairIds", affairIds);
		return DBAgent.find(hql,params);
	}
	@SuppressWarnings("unchecked")
	public  List<EdocUserLeaderRelation> getEdocUserLeaderRelation(Long userId){
		Map<String, Object> params = new HashMap<String, Object>();
		String hql="SELECT m FROM EdocUserLeaderRelation as m where m.userId=:userId ";
		params.put("userId", userId);
		List<EdocUserLeaderRelation> list = DBAgent.find(hql,params);
		return list;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public  List<EdocUserLeaderRelation> getUserLeaderRelationPO(Long userId){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		List list = DBAgent.find(
				"SELECT m FROM EdocUserLeaderRelation as m where m.userId=:userId ",params);
		return list;
	}
    public void  deleteUserLeaderRelation(Long userId){
		List<EdocUserLeaderRelation> userLeaderList=this.getUserLeaderRelationPO(userId);
		for(EdocUserLeaderRelation UserLeaderRelation:userLeaderList){
			DBAgent.delete(UserLeaderRelation);
		}
    }
    public void insertUserLeaderRelation(EdocUserLeaderRelation userLeaderRelation){
    	DBAgent.save(userLeaderRelation);
    }
    public void insertUserLeaderRelation(List<EdocUserLeaderRelation> userLeaderRelation){
    	DBAgent.saveAll(userLeaderRelation);
    }
	/**
	 * @author rz
	 * 代录页面获取ctpAffair
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings({ "rawtypes", "static-access" })
	public void getCtpAffairByLeader(Map params,FlipInfo flipInfo) throws BusinessException {
		StringBuilder hqlcondition = new StringBuilder();
		StringBuilder hql = new StringBuilder();
		if (params.containsKey("delete")) {
			hqlcondition.append(" and c.is_delete  = "+params.get("delete"));
		}
		if (params.containsKey("memberId")) {
			hqlcondition.append(" and c.member_id in "+params.get("memberId"));
		}
		if (params.containsKey("state")) {
			hqlcondition.append(" and c.state =  "+params.get("state"));
		}
		String likeValue=null;
		if(params.containsKey("serialNo")){
			if("".equals(params.get("serialNo"))){
//				hqlcondition.append(" AND s.serial_no = '"+params.get("serialNo")+"' ");
			}else{
				likeValue="'%"+params.get("serialNo")+"%'".replace(" ", "");
				hqlcondition.append(" AND s.serial_no LIKE "+likeValue);
			}
		}
		if(params.containsKey("docMark")){
			if("".equals(params.get("docMark"))){
//				hqlcondition.append(" AND s.doc_mark = '"+params.get("docMark")+"' ");
			}else{
				likeValue="'%"+params.get("docMark")+"%'".replace(" ", "");
				hqlcondition.append(" AND s.doc_mark LIKE "+likeValue);
			}
		}
		if(params.containsKey("subject")){
			likeValue="'%"+params.get("subject")+"%'".replace(" ", "");
			hqlcondition.append(" AND s.subject LIKE "+likeValue);
		}
		if(params.containsKey("currentNodesInfo")&&"已结束".equals(params.get("currentNodesInfo"))) {
			hqlcondition.append(" and (s.CURRENT_NODES_INFO=NULL or s.CURRENT_NODES_INFO='')  ");
        } 
		if(params.containsKey("createDate")){
			String time=params.get("createDate").toString();
			String[] times=time.split("#");
			if(times.length>0&&!"".equals(times[0])&&times[0]!=null){
				if("oracle".equals(SystemEnvironment.getDatabaseType())){
					hqlcondition.append(" AND to_char(create_date,'YYYY-MM-DD') >= '"+times[0]+"'");
				}else{
					hqlcondition.append(" AND c.create_date >= '"+times[0]+"'");
				}
			}
			if(times.length>1&&!"".equals(times[1])&&times[1]!=null){
				if(times[1].equals(times[0])){
					SimpleDateFormat sdf =   new SimpleDateFormat("yyyy-MM-dd");
					try {
						Date date = sdf.parse(times[1]);
						Calendar calendar =Calendar.getInstance();
					    calendar.setTime(date); 
					    calendar.add(calendar.DATE,1);
					    times[1]=sdf.format(calendar.getTime());
					} catch (ParseException e) {
						logger.error(e);
					}
				}
				if("oracle".equals(SystemEnvironment.getDatabaseType())){
					hqlcondition.append(" AND to_char(create_date,'YYYY-MM-DD') < '"+times[1]+"'");
				}else{
					hqlcondition.append(" AND c.create_date > '"+times[0]+"'");
				}
			}
		}
		hql.append("select "+pishiHql+" from ctp_affair c  ");
		hql.append("JOIN edoc_summary s ON c.object_id = s.id  where 1=1 ");
		if(params.get("state").equals(StateEnum.col_done.key())){
			hql.append("  and c.EXT_PROPS like '%pishi"+AppContext.currentUserId()+"%'");
		}
		hql.append(" and c.app in (4,19,20,21) ");
		//已指定回退数据不在待办列表中查询--同步协同查询
		if(!"done".equals(params.get("leaderPishiType"))){
			String specialBackSubState = "";
			specialBackSubState+=SubStateEnum.col_pending_specialBack.getKey();
			specialBackSubState+=",";
			specialBackSubState+=SubStateEnum.col_pending_specialBackCenter.getKey();
			hql.append(" and c.sub_state not in ("+specialBackSubState+")");
		}
		hql.append(hqlcondition);
     	if("done".equals(params.get("leaderPishiType"))){
    		hql.append(" order by s.update_time desc");
     	}else{
    		hql.append(" order by c.receive_time desc");
     	}
		JDBCAgent jdbc = new JDBCAgent(true);
		try {
			jdbc.findByPaging(hql.toString(), flipInfo);
		} catch (SQLException e) {
			logger.error(e);
		} finally {
			jdbc.close();
		}
	} 
	/***********************************************************************************************************/
	/**
	 * 新增或修改批示编号简称
	 * @param govdocLNA
	 * @throws BusinessException
	 */
	@SuppressWarnings("deprecation")
	public void saveOrEditLeaderShortName(GovdocLeaderSerialShortname govdocLSS) throws BusinessException {
		DBAgent.saveOrUpdate(govdocLSS);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<GovdocLeaderSerialShortname> getGovdocLSSs(FlipInfo flipInfo,Map map) throws BusinessException{
		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<String, Object>();
		hql.append(" from  GovdocLeaderSerialShortname ");
        hql.append(" where 1 =1  ");
        if(map.get("id") != null){
        	hql.append(" and  id=:id ");
        	params.put("id", map.get("id"));
        }
        if(map.get("leaderId") != null){
        	hql.append(" and  leaderId=:leaderId ");
        	params.put("leaderId", map.get("leaderId"));
        }
        if(map.get("shortName") != null){
        	hql.append(" and  shortName=:shortName ");
        	params.put("shortName", map.get("shortName"));
        }
        if(map.get("orgAccountId") != null){
        	hql.append(" and  orgAccountId=:orgAccountId ");
        	params.put("orgAccountId", map.get("orgAccountId"));
        }
		return DBAgent.find(hql.toString(), params ,flipInfo);
	}
	/**
	 * 删除对应的批示编号简称
	 * @param id
	 * @throws BusinessException
	 */
	public void deleteLNA(List<Long> ids) throws BusinessException {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("ids", ids);
		DBAgent.bulkUpdate("delete from GovdocLeaderSerialShortname where id in (:ids)", paramMap);
	}
	public void deleteLSSByLeaderId(Long leaderId,Long orgAccountId) throws BusinessException{
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("leaderId", leaderId);
		paramMap.put("orgAccountId", orgAccountId);
		DBAgent.bulkUpdate("delete from GovdocLeaderSerialShortname where leaderId = :leaderId and orgAccountId = :orgAccountId", paramMap);
	}
}
