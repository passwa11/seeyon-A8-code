package com.seeyon.apps.govdoc.manager.impl;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.govdoc.dao.GovdocPishiDao;
import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.manager.GovdocPishiManager;
import com.seeyon.apps.govdoc.po.EdocLeaderPishiNo;
import com.seeyon.apps.govdoc.po.EdocUserLeaderRelation;
import com.seeyon.apps.govdoc.po.GovdocLeaderSerialShortname;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.apps.govdoc.vo.GovdocDealVO;
import com.seeyon.apps.govdoc.vo.GovdocPishiVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.dao.paginate.Pagination;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.IdentifierUtil;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.v3x.edoc.domain.EdocPishiSummary;
import com.seeyon.v3x.edoc.domain.EdocSummary;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * 领导批示相关部分入口
 * 领导批示编号，代领导批示等处理
 */
public class GovdocPishiManagerImpl implements GovdocPishiManager{
	
	private static final Log LOGGER = LogFactory.getLog(GovdocManagerImpl.class);
	
	private OrgManager orgManager;
	
	private GovdocPishiDao govdocPishiDao;
	
	@Override
	public void fillSummaryVoByLeaderPishiNo(GovdocSummaryVO summaryVO) throws BusinessException {
		CtpAffair affair = summaryVO.getAffair();
    	Object pishiname = getLSS(affair.getMemberId());
    	if(pishiname == null || "".equals(pishiname)) {
    		return;
    	}
    	GovdocPishiVO pishiVo = new GovdocPishiVO();
    	pishiVo.setPishiname(pishiname);
    	
    	summaryVO.setPishiname(pishiname);
    	
		Integer nowyear = Calendar.getInstance().get(Calendar.YEAR);
		List<Integer> pishiyear = new ArrayList<Integer>();
		List<String> pishiyears = new ArrayList<String>();
		for(Integer i=2015; i<=nowyear; i++) {
			pishiyear.add(i);
			pishiyears.add(i.toString());
		}
		pishiVo.setNowyear(nowyear);
		pishiVo.setPishiyear(pishiyear);
    		
		summaryVO.setNowyear(nowyear);
		summaryVO.setPishiyear(pishiyear);
		
		Map<String,Integer> pishiNos = govdocPishiDao.findPishinoMap(pishiname.toString(), pishiyears);
		if(pishiNos !=null && pishiNos.size()>0) {
			summaryVO.setNowpishiNo(pishiNos.get(nowyear.toString()));
		}
		
		JsonConfig config = new JsonConfig();  
		String rs = JSONObject.fromObject(pishiNos, config).toString();
		summaryVO.setPishiNos(rs);
		pishiVo.setPishiNos(rs);
		
        Date date=new Date();
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        String time=format.format(date);
		summaryVO.setProxydate(time);
		pishiVo.setProxydate(time);
		
		pishiVo.setPishiType(summaryVO.getLeaderPishiType());
		summaryVO.setPishiVo(pishiVo);
	}
	
	@Override
	public void saveLeaderPishiNo(GovdocDealVO dealVo) throws BusinessException {
		try {
			Comment comment = dealVo.getComment();
			Map<String, Object> pishiParams = dealVo.getPishiParams();
			if(pishiParams != null && !pishiParams.isEmpty() && pishiParams.get("pishiName") != null 
					&& !"".equals(pishiParams.get("pishiName"))){
				EdocLeaderPishiNo edocLeaderPishiNo =new EdocLeaderPishiNo();
				V3xOrgMember v3xOrgMember=orgManager.getMemberById(comment.getCreateId());
				edocLeaderPishiNo.setId(UUIDLong.longUUID());
				edocLeaderPishiNo.setLeaderId(v3xOrgMember.getId());
				edocLeaderPishiNo.setLeaderSortId(v3xOrgMember.getSortId());
				edocLeaderPishiNo.setSummaryId(dealVo.getSummary().getId());
				edocLeaderPishiNo.setAffairId(dealVo.getAffairId());
				edocLeaderPishiNo.setPishiNo(Integer.parseInt(String.valueOf(pishiParams.get("pishiNo"))));
				edocLeaderPishiNo.setPishiName((String)pishiParams.get("pishiName"));
				edocLeaderPishiNo.setPishiYear((String)pishiParams.get("pishiyear"));
				SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd");
				edocLeaderPishiNo.setProxyDate(dateFormat.parse(pishiParams.get("proxyDate").toString()));
				edocLeaderPishiNo.setcreateTime(new Date());
				edocLeaderPishiNo.setIsRelease(Boolean.FALSE);
				govdocPishiDao.insertLeaderPishiNo(edocLeaderPishiNo);
			}
		} catch (Exception e) {
			LOGGER.error("保存批示编号错误");
		}
	}
	@Override
	public void saveOrEditPishiNo(EdocLeaderPishiNo edocLeaderPishiNo) throws BusinessException {
			govdocPishiDao.insertLeaderPishiNo(edocLeaderPishiNo);
	}
	@AjaxAccess
	@Override
	public Boolean checkPishiNo(Integer pishiNo, String pishiName,String pishiYear ,Long summaryId) throws BusinessException{
		return govdocPishiDao.checkPishiNo(pishiNo,pishiName,pishiYear,summaryId);
	}
	@Override
	public void emptyPishiNo(Long summaryId) throws BusinessException {
    	 govdocPishiDao.deletePishiNoBySummaryId(summaryId);
	}
	@Override
	public void emptyPishiNoByAffairId(Long affairId,String pishiName) throws BusinessException{
		govdocPishiDao.deletePishiNoByAffairId(affairId, pishiName);
	}
	
	@Override
	public List<String> getAllLeaderName(Long accountId) throws BusinessException {
		 List<String> list= new ArrayList<String>();
		 Map<String, Object> params = new HashMap<String, Object>();
		 params.put("orgAccountId", AppContext.currentAccountId());
		 List<GovdocLeaderSerialShortname> gLSSs = govdocPishiDao.getGovdocLSSs(null,params);
		 for(GovdocLeaderSerialShortname gLSS:gLSSs){
			 list.add(gLSS.getShortName());
		 }
		return list;
	}
	@AjaxAccess
	@Override
	public FlipInfo leaderPishiUser(FlipInfo flipInfo,Map params)throws BusinessException{
		String orlTyp=(String) params.get("leaderPishiType");
		V3xOrgMember user=orgManager.getMemberById(AppContext.getCurrentUser().getId());
		List<V3xOrgRole> orgRoles = orgManager.getRoleByCode(orlTyp,AppContext.currentAccountId());
		List<V3xOrgMember> list = null;
		if (!orgRoles.isEmpty()) {
			list=orgManager.getMembersByRole(AppContext.currentAccountId(), orgRoles.get(0).getId());
		}
		List<EdocUserLeaderRelation> userLeaderRelationList=new ArrayList<EdocUserLeaderRelation>();
		List<EdocUserLeaderRelation> userLeaderList=new ArrayList<EdocUserLeaderRelation>();
		EdocUserLeaderRelation userLeaderRelation;
		if(list!=null){
			for(V3xOrgMember orgMember:list){
				userLeaderRelation=new EdocUserLeaderRelation();
				if(orgMember!=null){
					userLeaderList=govdocPishiDao.getEdocUserLeaderRelation(Long.valueOf(orgMember.getId()));
					if(userLeaderList.isEmpty()){
						userLeaderRelation.setUserId(orgMember.getId());
						userLeaderRelation.setUserName(orgMember.getName());
						userLeaderRelationList.add(userLeaderRelation);
					}else if(userLeaderList.size()==1){
						userLeaderRelation.setUserId(userLeaderList.get(0).getUserId());
						userLeaderRelation.setUserName(userLeaderList.get(0).getUserName());
						userLeaderRelation.setLeaderId(userLeaderList.get(0).getLeaderId());
						userLeaderRelation.setLeaderName(userLeaderList.get(0).getLeaderName());
						userLeaderRelationList.add(userLeaderRelation);
					}else{
						userLeaderRelation.setUserId(userLeaderList.get(0).getUserId());
						userLeaderRelation.setUserName(userLeaderList.get(0).getUserName());
						String leaderNames="";
						String leaderIds="";
						for(EdocUserLeaderRelation userleader:userLeaderList){
							 leaderIds+=userleader.getLeaderId()+",";
							 leaderNames+=userleader.getLeaderName()+"、  ";
						}
						userLeaderRelation.setLeaderId(leaderIds);
						userLeaderRelation.setLeaderName(leaderNames);
						userLeaderRelationList.add(userLeaderRelation);
					}
				}
			}
			flipInfo.setTotal(userLeaderRelationList.size());
			int page = flipInfo.getPage();
			int size = flipInfo.getSize();
			int minPage = (page-1) * size;
			int maxPage = page * size;
			if(userLeaderRelationList.size() >= maxPage){
				userLeaderRelationList = userLeaderRelationList.subList(minPage, maxPage);
			}else{
				userLeaderRelationList = userLeaderRelationList.subList(minPage, userLeaderRelationList.size());
			}
			flipInfo.setData(userLeaderRelationList);
		}
		return flipInfo;
	}
	@AjaxAccess
	@Override
	public void updateLeaderPishi(Long[] ids,String value)throws BusinessException{
		List<EdocUserLeaderRelation> list=govdocPishiDao.getUserLeaderRelationPO(Long.valueOf(ids[0]));
		if(ids.length!=0 & !list.isEmpty()){
			govdocPishiDao.deleteUserLeaderRelation(ids[0]);
		}

		V3xOrgMember orgUser=orgManager.getMemberById(ids[0]);
		EdocUserLeaderRelation userLeaderRelation=new EdocUserLeaderRelation();
		if(value.isEmpty()){
			userLeaderRelation.setId(UUIDLong.longUUID());
			userLeaderRelation.setUserId(orgUser.getId());
			userLeaderRelation.setUserName(orgUser.getName());
			govdocPishiDao.insertUserLeaderRelation(userLeaderRelation);
		}else{
			String[] strings = value.split(",");
			V3xOrgMember orgLeader;
			List<EdocUserLeaderRelation> userLeaderRelations = new ArrayList<EdocUserLeaderRelation>();
			for(String user:strings){
				userLeaderRelation = new EdocUserLeaderRelation();
				String[] leader = user.split("\\|");
				orgLeader=orgManager.getMemberById(Long.valueOf(leader[1]));
				userLeaderRelation.setId(UUIDLong.longUUID());
				userLeaderRelation.setUserId(orgUser.getId());
				userLeaderRelation.setUserName(orgUser.getName());
				userLeaderRelation.setLeaderId(user);
				userLeaderRelation.setLeaderName(orgLeader.getName());
				userLeaderRelations.add(userLeaderRelation);
			}
			govdocPishiDao.insertUserLeaderRelation(userLeaderRelations);
		}

	}
	@AjaxAccess
	@Override
	public FlipInfo getlistPishiList(FlipInfo flipInfo, Map params) throws BusinessException, ParseException{
     	User user = AppContext.getCurrentUser();
     	String typeFlag="";
     	if("done".equals(params.get("leaderPishiType"))){
     		params.put("state", StateEnum.col_done.key());
     		params.put("transactorId", user.getId());
     		typeFlag="done";
     	}else{
     		params.put("state", StateEnum.col_pending.key());
     		typeFlag="pinding";
     	}
		List<EdocUserLeaderRelation> leaders = null;
		try {
			leaders = govdocPishiDao.getEdocUserLeaderRelation(user.getId());
		} catch (Exception e1) {
			LOGGER.error(e1);
			//e1.printStackTrace();
		}
		List<EdocSummary> queryLists = new LinkedList<EdocSummary>();
		List<EdocSummary> updateTimeList = new LinkedList<EdocSummary>();
		List<EdocSummary> createTimeList = new LinkedList<EdocSummary>();
		String ids="(";
		int i=1;
		for(EdocUserLeaderRelation userLeaderRelation:leaders){
			if(userLeaderRelation.getLeaderId()!=null && !"".equals(userLeaderRelation.getLeaderId())){
				String[] leaderIds = userLeaderRelation.getLeaderId().split("\\|");
				if(params.get("currentNodesInfo")!=null&&!"已结束".equals(params.get("currentNodesInfo"))){
					if(!userLeaderRelation.getLeaderName().contains(params.get("currentNodesInfo").toString())){
						continue;
					}
				}
				if(i==1){
					ids+=leaderIds[1];
				}else{
					ids+=", "+leaderIds[1];
				}
				i++;
			}
		}
		ids+=")";
		params.put("delete", 0);
		if(!"()".equals(ids)){
			params.put("memberId",ids);
			govdocPishiDao.getCtpAffairByLeader(params,flipInfo);
		}
		FlipInfo fi=new FlipInfo();
		fi.setPage(flipInfo.getPage());
		fi.setSize(flipInfo.getSize());
		List<Map>  list= flipInfo.getData();
		Pagination.setRowCount(flipInfo.getTotal());
		EdocPishiSummary edocPishiSummary=null;
		if(!list.isEmpty()){
			for(Map map:list){
				//edocSummary=this.getEdocSummaryById(Long.valueOf(map.get("object_id").toString()));
				//if(edocSummary!=null){
				edocPishiSummary=new EdocPishiSummary();
				edocPishiSummary.setSubject(map.get("subject").toString());
				edocPishiSummary.setId(Long.valueOf(map.get("object_id").toString()));
				if(map.get("serial_no")!=null && !"".equals(map.get("sub_state"))){
					edocPishiSummary.setSerialNo(map.get("serial_no").toString());
				}
				if(map.get("doc_mark")!=null && !"".equals(map.get("sub_state"))){
					edocPishiSummary.setDocMark(map.get("doc_mark").toString());
				}
				if(map.get("govdoc_type")!=null && !"".equals(map.get("govdoc_type"))){
					edocPishiSummary.setGovdocType(Integer.valueOf(map.get("govdoc_type").toString()));
				}
				if(map.get("sub_state")!=null && !"".equals(map.get("sub_state")) && 
						Integer.valueOf(map.get("sub_state").toString())!= SubStateEnum.col_pending_unRead.getKey()){
					edocPishiSummary.setState(SubStateEnum.col_pending_read.getKey());
				}else{
					edocPishiSummary.setState(SubStateEnum.col_pending_unRead.getKey());
				}
				if(map.get("app")!=null && !"".equals(map.get("app"))){
					edocPishiSummary.setAffairApp(Integer.valueOf(map.get("app").toString()));
				}
				if(map.get("sub_app")!=null && !"".equals(map.get("sub_app"))){
					edocPishiSummary.setSubApp(Integer.valueOf(map.get("sub_app").toString()));
				}
				if(map.get("id")!=null && !"".equals(map.get("id"))){
					edocPishiSummary.setAffairId(Long.valueOf(map.get("id").toString()));
				}
				if(map.get("object_id")!=null && !"".equals(map.get("object_id"))){
					edocPishiSummary.setSummaryId(Long.valueOf(map.get("object_id").toString()));
				}
				if(map.get("sub_object_id")!=null && !"".equals(map.get("sub_object_id"))){
					edocPishiSummary.setSubObjectId(Long.valueOf(map.get("sub_object_id").toString()));
				}
				if(map.get("current_nodes_info")!=null && !"".equals(map.get("current_nodes_info"))){
					edocPishiSummary.setCurrentNodesInfo(GovdocHelper.parseCurrentNodesInfo((Timestamp)map.get("complete_time"), map.get("current_nodes_info").toString(), Collections.EMPTY_MAP));
				}
				if(map.get("body_type")!=null && !"".equals(map.get("body_type"))){
					int bodyType=GovdocUtil.getContentType(map.get("body_type").toString());
					edocPishiSummary.setBodyType(String.valueOf(bodyType));
				}
				Object date=map.get("update_date");
				if(date!=null && !"".equals(date)){
					SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					try {
						edocPishiSummary.setUpdateTime(new Timestamp(sdf.parse(date.toString()).getTime()));
					} catch (ParseException e) {
						LOGGER.error(e);
						//e.printStackTrace();
					}
				}
				//创建时间
				Object createdate=map.get("create_time");
				if(createdate!=null&&!"".equals(createdate)){
					SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					try {
						edocPishiSummary.setCreateTime(new Timestamp(sdf.parse(createdate.toString()).getTime()));
					} catch (ParseException e) {
						//e.printStackTrace();
					}
				}
				//附件
				if(map.get("identifier")!=null && !"".equals(map.get("identifier"))){
					edocPishiSummary.setHasAtt(IdentifierUtil.lookupInner(map.get("identifier").toString(), EdocSummary.INENTIFIER_INDEX.HAS_ATTACHMENTS.ordinal(), '1'));
				}
				//流程超期
				SimpleDateFormat format = new SimpleDateFormat("yyyy-M-dd HH:mm:ss"); 
				if(map.get("expected_process_time")!=null && !"".equals(map.get("expected_process_time"))){
					edocPishiSummary.setAffairDeadLineName(WFComponentUtil.getDeadLineName(format.parse(map.get("expected_process_time").toString())));
				}else if(map.get("deadline_date")!=null && !"".equals(map.get("deadline_date"))){
					edocPishiSummary.setAffairDeadLineName(WFComponentUtil.getDeadLineName(Long.valueOf(map.get("deadline_date").toString())));
				}else{
					edocPishiSummary.setAffairDeadLineName(ResourceUtil.getString("collaboration.project.nothing.label"));
				}
				if(map.get("is_cover_time")!=null && !"".equals(map.get("is_cover_time")) && "1".equals(map.get("is_cover_time").toString())){
					edocPishiSummary.setAffairIsCoverTime(Boolean.TRUE);
				}
				updateTimeList.add(edocPishiSummary);
			}
		}
		fi.setData(updateTimeList);
		fi.setTotal(updateTimeList.size());
		return fi;
	}
	@Override
	public String checkLeaderPishi(Long userId, Long affairMemberId){
		try {
		String pishiFlag="";
        List<EdocUserLeaderRelation> leaders = govdocPishiDao.getUserLeaderRelationPO(userId);
        if(!leaders.isEmpty()){
        	for(EdocUserLeaderRelation userLeaderRelation:leaders){
        		if(!"".equals(userLeaderRelation.getLeaderId())&&userLeaderRelation.getLeaderId()!=null){
            		String[] leaderIds=userLeaderRelation.getLeaderId().split("\\|");
            		if(leaderIds[1].equals(String.valueOf(affairMemberId))){
            			pishiFlag="pishi";
            		}
        		}
        	}
        }
		return pishiFlag;
		} catch (Exception e) {
			LOGGER.error(e);
			return "";
		}
	}
	@Override
	public List<EdocUserLeaderRelation> getEdocUserLeaderRelation(Long userId) {
		return govdocPishiDao.getUserLeaderRelationPO(userId);
	}
	@Override
	public  List<Long> getEdocUserLeaderId(Long userId){
		List<EdocUserLeaderRelation> list = this.getEdocUserLeaderRelation(userId);
		List<Long> listId = new ArrayList<Long>();
		if(!list.isEmpty()){
			for(EdocUserLeaderRelation edocUserLeaderRelation : list){
				if(!"".equals(edocUserLeaderRelation.getLeaderId()) && edocUserLeaderRelation.getLeaderId() != null){
					String str= edocUserLeaderRelation.getLeaderId().split("\\|")[1];
					listId.add(Long.valueOf(str));
				}
			}
		}
		return listId;
	}
	@AjaxAccess
	@Override
	public FlipInfo getLeaderShortName(FlipInfo flipInfo,Map params)throws BusinessException {
		 List<GovdocLeaderSerialShortname> list= new ArrayList<GovdocLeaderSerialShortname>();
		 params.put("orgAccountId", AppContext.currentAccountId());
		 list = govdocPishiDao.getGovdocLSSs(flipInfo,params);
		flipInfo.setData(list);
		return flipInfo;
		
	}

	@Override
	public void saveOrEditLeaderShortName(GovdocLeaderSerialShortname govdocLSS) throws BusinessException {
		govdocPishiDao.saveOrEditLeaderShortName(govdocLSS);
	}
	@AjaxAccess
	@Override
	public int checkLSS(String leaderName, Long leaderId, String shortName) throws BusinessException {
		List<GovdocLeaderSerialShortname> govdocLNAs = new ArrayList<GovdocLeaderSerialShortname>();
		if(leaderId != null){
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("leaderId", leaderId);
			govdocLNAs = govdocPishiDao.getGovdocLSSs(null,map);
			if(!govdocLNAs.isEmpty()){
				return 1;
			}
		}
		if(shortName != null){
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("shortName", shortName);
			govdocLNAs = govdocPishiDao.getGovdocLSSs(null,map);
			if(!govdocLNAs.isEmpty()){
				return 2;
			}
		}
		return -1;
	}
	@Override
	public List<GovdocLeaderSerialShortname> getGovdocLSSs(FlipInfo flipInfo,Map map) throws BusinessException{
		return govdocPishiDao.getGovdocLSSs(flipInfo,map);
	}
	@Override
	public Map<Long, EdocLeaderPishiNo> getLeaderPishiByAffairIds(List<Long> affairIds) throws BusinessException{
		if(!affairIds.isEmpty()){
			List<EdocLeaderPishiNo> lists = govdocPishiDao.getLeaderPishiByAffairIds(affairIds);
			Map<Long, EdocLeaderPishiNo> map = new HashMap<Long, EdocLeaderPishiNo>();
			for(EdocLeaderPishiNo edocLeaderPishiNo:lists){
				map.put(edocLeaderPishiNo.getAffairId(), edocLeaderPishiNo);
			}
			return map;
		}else{
			return null;
		}
	}
	@AjaxAccess
	@Override
	public void deleteLSS(String strings) throws BusinessException {
		String[] listS=strings.split(",");
		if(listS.length>0){
			List<Long> ids = new ArrayList<Long>();
			for(String id : listS){
				ids.add(Long.valueOf(id));
			}
			govdocPishiDao.deleteLNA(ids);
		}
	}
	@Override
	public void deleteLSSByLeaderId(Long leaderId,Long orgAccountId) throws BusinessException{
		govdocPishiDao.deleteLSSByLeaderId(leaderId, orgAccountId);
	}
	@Override
	public Object getLSS(Long leaderId) throws BusinessException {
		Object pishiName = "";
		Map<String ,Long> map = new HashMap<String, Long>();
		map.put("leaderId", leaderId);
		List<GovdocLeaderSerialShortname> lists = govdocPishiDao.getGovdocLSSs(null, map);
		if(!lists.isEmpty()){
			pishiName = lists.get(0).getShortName();
		}
		return pishiName;
	}
	@Override
	public List<EdocLeaderPishiNo> getAllLeaderPishi(Long summaryId) throws BusinessException {
		return govdocPishiDao.getAllLeaderPishi(summaryId);
	}
	/*******************************************  本类内部使用的方法   **********************************************/
	public String getCurrentNodesInfoS(String crrentNodesInfoIds) {
		try {
			if(crrentNodesInfoIds!=null && !"".equals(crrentNodesInfoIds)){
				String[] ids=crrentNodesInfoIds.split(";");
				if(ids.length>0){
					String crrentNodesInfos="";
					if(ids.length==1){
						crrentNodesInfos+=orgManager.getMemberById(Long.valueOf(ids[0])).getName();
					}else{
						int i=1;
						for(String id:ids){
							if(i==ids.length){
								crrentNodesInfos+=orgManager.getMemberById(Long.valueOf(id)).getName();
							}else{
								crrentNodesInfos+=orgManager.getMemberById(Long.valueOf(id)).getName()+",";
							}
							i++;
						}
					}
					return crrentNodesInfos;
				}
			}
		} catch (Exception e) { 
			LOGGER.info("查询人员失败",e);
		}
		return "已结束";
	}
	/*******************************************  本类内部使用的方法    **********************************************/
	/*******************************************  Spring注入    **********************************************/
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public void setGovdocPishiDao(GovdocPishiDao govdocPishiDao) {
		this.govdocPishiDao = govdocPishiDao;
	}
	/*******************************************  Spring注入    **********************************************/
}
