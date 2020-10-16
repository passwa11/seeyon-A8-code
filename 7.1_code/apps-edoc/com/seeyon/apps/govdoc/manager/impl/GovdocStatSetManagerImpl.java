package com.seeyon.apps.govdoc.manager.impl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.seeyon.apps.govdoc.dao.GovdocStatSetDao;
import com.seeyon.apps.govdoc.manager.GovdocStatSetManager;
import com.seeyon.apps.govdoc.po.EdocStatSet;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.v3x.common.web.login.CurrentUser;

/**
 * 新公文统计设置接口
 * @author 唐桂林
 *
 */
public class GovdocStatSetManagerImpl implements GovdocStatSetManager {

	private GovdocStatSetDao govdocStatSetDao;


	@Override
	public void checkStatInitData() throws BusinessException {
		//工作统计预制信息验证
		DetachedCriteria workStatCriteria = DetachedCriteria.forClass(EdocStatSet.class);
		workStatCriteria.add(Restrictions.eq("accountId", CurrentUser.get().getLoginAccount()));
		workStatCriteria.add(Restrictions.eq("statType", "work_count"));
		workStatCriteria.add(Restrictions.eq("initState", 1));
		if(govdocStatSetDao.getCountByCriteria(workStatCriteria)==0){
			EdocStatSet workState = new EdocStatSet();
			workState.setId(UUIDLong.longUUID());
			workState.setOrderNo(1);
			workState.setParentId(3L);
			workState.setName("工作统计");
			workState.setAccountId(CurrentUser.get().getLoginAccount());
			workState.setStatType("work_count");
			workState.setGovType("1,2,3,9,10,14,15,");
			workState.setRecNode("chengban");
			if(!(Boolean)SysFlag.sys_isG6S.getFlag()){
			 workState.setSendNode("fuhe");
			 workState.setStatNodePolicy("复核");
			}
			workState.setModifyTime(new Timestamp(new Date().getTime()));
			workState.setRecNodePolicy("承办");			
			workState.setState(0);
			workState.setComments("工作统计");
			workState.setInitState(1);
			govdocStatSetDao.save(workState);
			//分类统计预制信息验证
			EdocStatSet cateState = new EdocStatSet();
			cateState.setId(UUIDLong.longUUID());
			cateState.setOrderNo(2);
			cateState.setParentId(3L);
			cateState.setName("分类统计");
			cateState.setAccountId(CurrentUser.get().getLoginAccount());
			cateState.setStatType("work_count");
			cateState.setGovType("1,2,4,5,9,10,11,12,16,17,18,19,20,21,");
			cateState.setRecNode("chengban");
			cateState.setModifyTime(new Timestamp(new Date().getTime()));
			cateState.setRecNodePolicy("承办");
			cateState.setState(0);
			cateState.setComments("分类统计");
			cateState.setInitState(1);
			govdocStatSetDao.save(cateState);
		}		
		//签收统计预制信息验证
		DetachedCriteria signCriteria = DetachedCriteria.forClass(EdocStatSet.class);
		signCriteria.add(Restrictions.eq("accountId", CurrentUser.get().getLoginAccount()));
		signCriteria.add(Restrictions.eq("statType", "v3x_edoc_sign_count"));
		signCriteria.add(Restrictions.eq("initState", 1));
		if(govdocStatSetDao.getCountByCriteria(signCriteria)==0){
			EdocStatSet signState = new EdocStatSet();
			signState.setId(UUIDLong.longUUID());
			signState.setOrderNo(1);
			signState.setParentId(2L);
			signState.setName("发文签收统计");
			signState.setAccountId(CurrentUser.get().getLoginAccount());
			signState.setDeptIds("Account|-1730833917365171641");
			signState.setDeptNames("一级单位");
			signState.setStatType("v3x_edoc_sign_count");
			signState.setTimeType("0,1,2,3,4,5,");
			signState.setModifyTime(new Timestamp(new Date().getTime()));
			signState.setState(0);
			signState.setComments("");
			signState.setInitState(1);
			govdocStatSetDao.save(signState);
		}
		//签收统计预制信息验证
		DetachedCriteria backCriteria = DetachedCriteria.forClass(EdocStatSet.class);
		backCriteria.add(Restrictions.eq("accountId", CurrentUser.get().getLoginAccount()));
		backCriteria.add(Restrictions.eq("statType", "v3x_edoc_sign_self_count"));
		backCriteria.add(Restrictions.eq("initState", 1));
		if(govdocStatSetDao.getCountByCriteria(backCriteria)==0){
			EdocStatSet signState = new EdocStatSet();
			signState.setId(UUIDLong.longUUID());
			signState.setOrderNo(1);
			signState.setParentId(2L);
			signState.setName("收文签收统计");
			signState.setAccountId(CurrentUser.get().getLoginAccount());
			signState.setStatType("v3x_edoc_sign_self_count");
			signState.setTimeType("0,1,2,3,4,5,");
			signState.setDeptIds("Account|"+CurrentUser.get().getLoginAccount());
			signState.setDeptNames(CurrentUser.get().getLoginAccountName());
			signState.setModifyTime(new Timestamp(new Date().getTime()));
			signState.setState(0);
			signState.setComments("");
			signState.setInitState(1);
			govdocStatSetDao.save(signState);
		}
	}
	
	@Override
	public List<EdocStatSet> findEdocStatSetByAccount(long accountId, String statType) throws BusinessException {
		return govdocStatSetDao.findEdocStatSetByAccount(accountId,statType);
	}

	@Override
	public FlipInfo findEdocStatListByAccount(FlipInfo fi, Map<String,String> params) throws BusinessException {
		List<EdocStatSet> reList = govdocStatSetDao.findEdocStatSetByAccount(params,fi);
		if((Boolean)SysFlag.sys_isG6S.getFlag()){
			for(int i=0;i<reList.size();i++){
				if("签收统计".equals(reList.get(i).getName())&&("一级单位").equals(reList.get(i).getDeptNames())){
					reList.get(i).setDeptIds("Account|670869647114347");
					reList.get(i).setDeptNames("单位");
				}
			}
		}
		fi.setData(reList);
		return fi;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void createState(Map map) throws BusinessException {
		Long currentAccountId = AppContext.currentAccountId();
		EdocStatSet newStat = new EdocStatSet();
		ParamUtil.mapToBean(map, newStat, false);
		newStat.setId(UUIDLong.longUUID());
		newStat.setStatType(newStat.getParentId()==2?"v3x_edoc_sign_count":"work_count");
		newStat.setAccountId(currentAccountId);
		//如果是交换统计，还要判断是哪种类型的统计
		if(newStat.getParentId()==2){
			String signtype = (String)map.get("signType");
			if("1".equals(signtype)){
				newStat.setStatType("v3x_edoc_sign_self_count");
				newStat.setDeptIds("Account|"+String.valueOf(currentAccountId));
				newStat.setDeptNames(AppContext.currentAccountName());
			}
		}
		String timeTypes = "";
		String govTypes = "";
		Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
		while (it.hasNext()) {
				Map.Entry<String,Object> entry = it.next();
	           if(entry.getKey().startsWith("timeType")){
	           	if(entry.getValue() !=null){
	           	timeTypes+=entry.getValue()+",";
	           	}
	           }
	           if(entry.getKey().startsWith("govType")){
	           	if(entry.getValue() !=null){
	           	govTypes+=entry.getValue()+",";
	           	}
	           }			         
		}
		newStat.setTimeType(timeTypes);
		newStat.setGovType(govTypes);
		newStat.setInitState(0);
		newStat.setModifyTime(new java.sql.Timestamp(new Date().getTime()));
		List<EdocStatSet> check = govdocStatSetDao.checkNameExist(newStat.getName(),0L);
		if(check != null && !check.isEmpty()){
			throw new BusinessException("存在相同名称的统计");
		}
		govdocStatSetDao.save(newStat);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void updateState(Map map) throws BusinessException {
		Long currentAccountId = AppContext.currentAccountId();
		EdocStatSet editStat = new EdocStatSet();
		Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
		String timeTypes = "";
		String govTypes = "";
		while (it.hasNext()) {
			 Map.Entry<String,Object> entry = it.next();
             if(entry.getKey().startsWith("timeType")){
            	 if(entry.getValue() !=null){
            	 timeTypes+=entry.getValue()+",";
            	 }
             }
             if(entry.getKey().startsWith("govType")){
            	 if(entry.getValue() !=null){
            	 govTypes+=entry.getValue()+",";
            	 }
             }
		}
		ParamUtil.mapToBean(map, editStat, false);
		editStat.setAccountId(currentAccountId);
		editStat.setTimeType(timeTypes);
		editStat.setGovType(govTypes);
		editStat.setStatType(editStat.getParentId()==2?"v3x_edoc_sign_count":"work_count");
		//如果是交换统计，还要判断是哪种类型的统计
		if(editStat.getParentId()==2){
			String signtype = (String)map.get("signType");
			if("1".equals(signtype)){
				editStat.setStatType("v3x_edoc_sign_self_count");
				editStat.setDeptIds("Account|"+String.valueOf(currentAccountId));
				editStat.setDeptNames(AppContext.currentAccountName());
			}
		}
		editStat.setModifyTime(new java.sql.Timestamp(new Date().getTime()));
		List<EdocStatSet> check = govdocStatSetDao.checkNameExist(editStat.getName(),editStat.getId());
		if(check != null && !check.isEmpty()){
			throw new BusinessException("存在相同名称的统计");
		}
		govdocStatSetDao.update(editStat);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public HashMap viewOne(Long id) throws BusinessException {
		HashMap<String,Object> map = new HashMap<String,Object>();
		EdocStatSet stat = govdocStatSetDao.get(id);
		if(stat != null){
			map.put("id", stat.getId());
			map.put("parentId", stat.getParentId());
			map.put("state", stat.getState());
			map.put("initState", stat.getInitState());								
			map.put("statType", stat.getStatType());
			map.put("recNode", stat.getRecNode());
			map.put("sendNode", stat.getSendNode());		
			map.put("name", stat.getName());
			map.put("statNodePolicy", stat.getStatNodePolicy());
			map.put("recNodePolicy", stat.getRecNodePolicy());
			map.put("comments", stat.getComments());
			map.put("orderNo", stat.getOrderNo());
			map.put("timeType", stat.getTimeType());
			map.put("govType", stat.getGovType());
			if((Boolean)SysFlag.sys_isG6S.getFlag()){				
					if("签收统计".equals(stat.getName())&&("一级单位").equals(stat.getDeptNames())){
						stat.setDeptIds("Account|670869647114347");
						stat.setDeptNames("单位");
					}				
			}				
			map.put("deptNames", stat.getDeptNames());
			map.put("deptIds", stat.getDeptIds());
		}
		return map;
	}

	@Override
	public Boolean deleteStat(Long[] ids) throws BusinessException {
		boolean isError = false;
		//验证删除的数据中是否包含预制数据
		for (Long delId : ids) {
			if(govdocStatSetDao.get(delId).getInitState()==1){
				isError = true;
				break;
			}
		}
		if(isError) return isError;
		try{
		for (Long s : ids) {
			if(govdocStatSetDao.get(s).getInitState()==1)continue;
			govdocStatSetDao.delete(s);
	    }
		}catch(Exception e){
			 isError = true;
			 //logger.error("删除配置信息出错",e);
		}
		return isError;
	}
	
	@Override
	public Boolean checkStat(Long[] ids) throws BusinessException {
		boolean isError = false;
		//验证删除的数据中是否包含预制数据
		for (Long delId : ids) {
			if(govdocStatSetDao.get(delId).getInitState()==1){
				isError = true;
				break;
			}
		}
		return isError;
	}

	/**
	 * @author rz
	 *
	 *
	 */
	@SuppressWarnings("rawtypes")
	public FlipInfo pendLeaderPishi(FlipInfo flipInfo,Map params) throws BusinessException{
       
		//TODO
		return null;
	}
	/**
	 * @author rz
	 *
	 *
	 */
	@SuppressWarnings("rawtypes")
	public FlipInfo doneLeaderPishi(FlipInfo flipInfo,Map params) throws BusinessException {
		return null;
	}

	@Override
	public List<EdocStatSet> findEdocStatTreeList(String statType) throws BusinessException {
		// TODO Auto-generated method stub
		return govdocStatSetDao.findEdocStatTreeList(statType);
	}

	@Override
	public EdocStatSet getEdocStatSet(Long statId) throws BusinessException {
		return DBAgent.get(EdocStatSet.class, statId);
	}
	
	@Override
	public void updateEdocStatSet(EdocStatSet bean) {
		govdocStatSetDao.update(bean);
	}

	@Override
	public void saveEdocStatSet(EdocStatSet bean) {
		govdocStatSetDao.save(bean);
	}
	
	public void setGovdocStatSetDao(GovdocStatSetDao govdocStatSetDao) {
		this.govdocStatSetDao = govdocStatSetDao;
	}

}
