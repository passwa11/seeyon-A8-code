package com.seeyon.v3x.edoc.manager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.govdoc.dao.GovdocStatSetDao;
import com.seeyon.apps.govdoc.helper.GovdocRoleHelper;
import com.seeyon.apps.govdoc.po.EdocStatSet;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.dao.OrgCache;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.v3x.common.web.login.CurrentUser;
import com.seeyon.v3x.edoc.dao.EdocStatNewDao;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.util.EdocStatEnum;
import com.seeyon.v3x.edoc.util.EdocStatEnum.EdocStatDisplayTimeTypeEnum;
import com.seeyon.v3x.edoc.util.EdocStatEnum.EdocStatDisplayTypeEnum;
import com.seeyon.v3x.edoc.util.EdocStatEnum.EdocStatListTypeEnum;
import com.seeyon.v3x.edoc.util.EdocStatEnum.EdocStatResultTypeEnum;
import com.seeyon.v3x.edoc.util.EdocStatHelper;
import com.seeyon.v3x.edoc.webmodel.EdocStatListVO;
import com.seeyon.v3x.edoc.webmodel.EdocStatParamVO;
import com.seeyon.v3x.edoc.webmodel.EdocStatResultVO;
import com.seeyon.v3x.edoc.webmodel.EdocStatVO;
import com.seeyon.v3x.worktimeset.exception.WorkTimeSetExecption;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

public class EdocStatNewManagerImpl implements EdocStatNewManager {
	
	private static final Log LOGGER = LogFactory.getLog(EdocStatNewManagerImpl.class);
	private EdocStatNewDao edocStatNewDao;
	private OrgManager orgManager;
	private EdocElementManager edocElementManager;
	private EnumManager enumManagerNew;
	private WorkTimeManager       workTimeManager;
	private TemplateManager templateManager;
	private DocApi docApi;
	private EdocManager edocManager;
	private OrgCache orgCache;
	
	public void setOrgCache(OrgCache orgCache) {
		this.orgCache = orgCache;
	}

	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}

	public DocApi getDocApi() {
		return docApi;
	}

	public void setDocApi(DocApi docApi) {
		this.docApi = docApi;
	}

	private GovdocStatSetDao govdocStatSetDao;
	
	public GovdocStatSetDao getGovdocStatSetDao() {
		return govdocStatSetDao;
	}

	public void setGovdocStatSetDao(GovdocStatSetDao govdocStatSetDao) {
		this.govdocStatSetDao = govdocStatSetDao;
	}

	@Override
	public void checkStatInitData() throws Exception {
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
	
	/**
	 * 获取公文统计数据
	 * @param conditionMap
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public FlipInfo getEdocStatVoList(FlipInfo flipInfo, Map<String, String> conditionMap) throws BusinessException {
		long t_start = System.currentTimeMillis();
		User user = AppContext.getCurrentUser();		
		//收发文
		int edocType = Strings.isBlank(conditionMap.get("edocType"))  ? 0 : Integer.parseInt(conditionMap.get("edocType"));
		//将模版的子模板添加到选择的模板里面
		setConditionOperationTypeIds(conditionMap);
		List<Long> selectDeptIdList = EdocStatHelper.getSelectedDeptIdList(conditionMap.get("rangeIds"), "Department");
		List<Long> selectMemberDeptIdList = EdocStatHelper.getSelectedDeptIdList(conditionMap.get("rangeIds"), "Member");
		if(Strings.isNotEmpty(selectDeptIdList) && Strings.isNotEmpty(selectMemberDeptIdList)) {
			selectDeptIdList.addAll(selectMemberDeptIdList);
		}
		//去重处理
		EdocStatHelper.removeRepeatItem(selectDeptIdList);
		/** 获取统计范围的显示行 */
		
		long t_m = 0;
		if(Strings.isNotBlank(conditionMap.get("rangeIds"))) {
		    Map<String, Object> statVoMap = getEdocStatVoListView(conditionMap);
			try {
				boolean showBanwenYuewen = EdocSwitchHelper.showBanwenYuewen(user.getLoginAccount());
				conditionMap.put("showBanwenYuewen", String.valueOf(showBanwenYuewen));
				//LOGGER.info("获取列头_________________:"+(System.currentTimeMillis() - t_start));
				
				t_m = System.currentTimeMillis();
				conditionMap.put("resultType", String.valueOf(EdocStatResultTypeEnum.pendingAndDone.key()));
				setEdocStatCount_subQuery(flipInfo, conditionMap, statVoMap);
				//LOGGER.info("待办已办____:"+(System.currentTimeMillis() - t_m));
				
				if(edocType == 1) {//收文
					if(showBanwenYuewen) {						
						t_m = System.currentTimeMillis();
						conditionMap.put("resultType", String.valueOf(EdocStatResultTypeEnum.readingAndReaded.key()));
						setEdocStatCount_subQuery(flipInfo, conditionMap, statVoMap);
						//LOGGER.info("待阅已阅____:"+(System.currentTimeMillis() - t_m));
						
						t_m = System.currentTimeMillis();
						conditionMap.put("resultType", String.valueOf(EdocStatResultTypeEnum.readAll.key()));
						setEdocStatCount_subQuery(flipInfo, conditionMap, statVoMap);
						//LOGGER.info("总阅件____:"+(System.currentTimeMillis() - t_m));
					}
				}

				//总经办/未办结/已办结
				t_m = System.currentTimeMillis();
				conditionMap.put("resultType", String.valueOf(EdocStatResultTypeEnum.doAll.key()));
				setEdocStatCount_subQuery(flipInfo, conditionMap, statVoMap);
				//LOGGER.info("总经办/未办结/已办结____:"+(System.currentTimeMillis() - t_m));
				
				//计算已发数据（添加部门下兼职/副岗已发数据）
				t_m = System.currentTimeMillis();
				conditionMap.put("resultType", String.valueOf(EdocStatResultTypeEnum.sent.key()));
				setEdocStatResultCount(flipInfo, conditionMap, statVoMap);
				//LOGGER.info("已发____:"+(System.currentTimeMillis() - t_m));
				
			} catch(BusinessException e) {
				LOGGER.error("",e);
			}
			List<EdocStatVO> statVoList = (List<EdocStatVO>)statVoMap.get("statVoList");
			flipInfo.setData(statVoList);			
			//LOGGER.info("统计方法getEdocStatVoList____:"+(System.currentTimeMillis() - t_start));
		}
		return flipInfo;
	}
	
	/**
	 * 返回的结果集可直接用于显示(子查询)-目前待办/已办/待阅/已阅用到
	 * @param flipInfo
	 * @param conditionMap
	 * @param statVoList
	 * @throws BusinessException
	 */
	private void setEdocStatCount_subQuery(FlipInfo flipInfo, Map<String, String> conditionMap, Map<String, Object> statVoMap) throws BusinessException {
		int displayType = Strings.isBlank(conditionMap.get("displayType")) ? EdocStatDisplayTypeEnum.department.key() : Integer.parseInt(conditionMap.get("displayType"));
		int displayTimeType = Strings.isBlank(conditionMap.get("displayTimeType")) ? EdocStatDisplayTimeTypeEnum.year.key() : Integer.parseInt(conditionMap.get("displayTimeType"));
		int resultType = Strings.isBlank(conditionMap.get("resultType")) ? -1 : Integer.parseInt(conditionMap.get("resultType"));
		if(statVoMap != null && statVoMap.size() > 0) {
		    List<Object[]> result = edocStatNewDao.findEdocStatResultBySql(flipInfo, conditionMap);
			if(Strings.isNotEmpty(result)) {
			    EdocStatResultVO resultVo = null;
				for(Object[] object : result) {
					resultVo = new EdocStatResultVO();
					resultVo.setDisplayRowValue(object, displayType, displayTimeType, resultType);
					String displayId  = resultVo.getDisplayId();
					if(statVoMap.containsKey(displayId)) {
						EdocStatVO statVo = (EdocStatVO)statVoMap.get(displayId);
						if(statVo == null) continue;
						if(resultType == EdocStatEnum.EdocStatResultTypeEnum.pendingAndDone.key()) {	//待办/已办/待阅/已阅
							if(resultVo.getDisplayRow().equals(String.valueOf(StateEnum.col_pending.key()))) {
								statVo.setCountPending(resultVo.getDisplayRowCount());
							} else {
								statVo.setCountDone(resultVo.getDisplayRowCount());
							}
						} else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.readingAndReaded.key()) {	//待办/已办/待阅/已阅
							if(resultVo.getDisplayRow().equals(String.valueOf(StateEnum.col_pending.key()))) {
								statVo.setCountReading(resultVo.getDisplayRowCount());
							} else {
								statVo.setCountReaded(resultVo.getDisplayRowCount());
							}
						} else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.doAll.key()) {//总经办/总办件/未办结/已办结
							if(resultVo.getDisplayRow().equals(String.valueOf(EdocConstant.flowState.finish.ordinal()))
									|| resultVo.getDisplayRow().equals(String.valueOf(EdocConstant.flowState.terminate.ordinal()))) {
								statVo.setCountFinish(resultVo.getDisplayRowCount());
							} else if(resultVo.getDisplayRow().equals(String.valueOf(EdocConstant.flowState.run.ordinal()))) {
								statVo.setCountWaitFinish(resultVo.getDisplayRowCount());
							}
							statVo.setCountHandleAll(statVo.getCountFinish() + statVo.getCountWaitFinish());	
							statVo.setCountDoAll(statVo.getCountFinish() + statVo.getCountWaitFinish());//总办结
							
						} else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.readAll.key()) {//总阅件
							if(resultVo.getDisplayRow().equals(String.valueOf(ApplicationSubCategoryEnum.edocRecRead.key()))) {
								statVo.setCountReadAll(resultVo.getDisplayRowCount());
							}
						}
					}
				}
			}
			result = null;
		}
	}
	
	/**
	 * 返回的结果集还需要再做一次过滤(非子查询)，目前已发用到
	 * @param flipInfo
	 * @param conditionMap
	 * @param statVoList
	 * @throws BusinessException
	 */
	private void setEdocStatResultCount(FlipInfo flipInfo, Map<String, String> conditionMap, Map<String, Object> statVoMap) throws BusinessException {
		int displayType = Strings.isBlank(conditionMap.get("displayType")) ? EdocStatDisplayTypeEnum.department.key() : Integer.parseInt(conditionMap.get("displayType"));
		int displayTimeType = Strings.isBlank(conditionMap.get("displayTimeType")) ? EdocStatDisplayTimeTypeEnum.year.key() : Integer.parseInt(conditionMap.get("displayTimeType"));
		int resultType = Strings.isBlank(conditionMap.get("resultType")) ? -1 : Integer.parseInt(conditionMap.get("resultType"));
		if(statVoMap != null && statVoMap.size() > 0) {
			List<Object[]> result = null;
			if(resultType == EdocStatEnum.EdocStatResultTypeEnum.sent.key()) {
				result = edocStatNewDao.findEdocStatResultBySql_Sent(flipInfo, conditionMap);
			} else {
				result = edocStatNewDao.findEdocStatResultBySql_DoAndRead(flipInfo, conditionMap);
			}
			if(Strings.isNotEmpty(result)) {
				EdocStatResultVO resultVo = null;
				for(Object[] object : result) {
					resultVo = new EdocStatResultVO();
					resultVo.setDisplayRowResultValue(object, displayType, displayTimeType, resultType);
					String displayId = resultVo.getDisplayId();
					if(statVoMap.containsKey(displayId)) {
						EdocStatVO statVo = (EdocStatVO)statVoMap.get(displayId);
						if(statVo == null) continue;
						if(resultType == EdocStatEnum.EdocStatResultTypeEnum.pending.key()) {//待办
							statVo.setCountPending(statVo.getCountPending()+resultVo.getDisplayRowCount());
						} else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.done.key()) {//已办
							statVo.setCountDone(statVo.getCountDone()+resultVo.getDisplayRowCount());
						} else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.reading.key()) {//待阅
							statVo.setCountReading(statVo.getCountReading()+resultVo.getDisplayRowCount());
						} else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.readed.key()) {//已阅
							statVo.setCountReaded(statVo.getCountReaded()+resultVo.getDisplayRowCount());
						} else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.sent.key()) {//已发
							statVo.setCountSent(statVo.getCountSent()+resultVo.getDisplayRowCount());
						} else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.undertaker.key()) {//承办数
							statVo.setCountUndertaker(statVo.getCountUndertaker()+resultVo.getDisplayRowCount());
						}
					}
				}
			}
			result = null;
		}
	}
	
	/**
	 * 获取公文统计数据(老方法，暂时无用)
	 * @param conditionMap
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	private void setEdocStatVoListBak(FlipInfo flipInfo, Map<String, String> conditionMap, Map<String, Object> statVoMap) throws BusinessException {
		int displayType = Strings.isBlank(conditionMap.get("displayType")) ? EdocStatDisplayTypeEnum.department.key() : Integer.parseInt(conditionMap.get("displayType"));
		int displayTimeType = Strings.isBlank(conditionMap.get("displayTimeType")) ? EdocStatDisplayTimeTypeEnum.year.key() : Integer.parseInt(conditionMap.get("displayTimeType"));
		User user = AppContext.getCurrentUser();
		List<Long> userSelfAndSubDeptIdList = EdocStatHelper.getSelfAndSubDepartmentList(user.getDepartmentId());
		List<Long> selectDeptIdList = EdocStatHelper.getSelectedDeptIdList(conditionMap.get("rangeIds"), "Department");
		List<Long> selectMemberDeptIdList = EdocStatHelper.getSelectedDeptIdList(conditionMap.get("rangeIds"), "Member");
		if(Strings.isNotEmpty(selectDeptIdList) && Strings.isNotEmpty(selectMemberDeptIdList)) {
			selectDeptIdList.addAll(selectMemberDeptIdList);
		}
    	boolean isCurrentAccountExchange = GovdocRoleHelper.isAccountExchange();
		boolean isCurrentDeptExchange = GovdocRoleHelper.isDepartmentExchange();
		boolean showBanwenYuewen = EdocSwitchHelper.showBanwenYuewen(user.getLoginAccount());
		Map<String, Long> statAffairMap = new HashMap<String, Long>();
		List<EdocStatVO> statVoList = (List<EdocStatVO>)statVoMap.get("statVoList");
		long t_m = System.currentTimeMillis();
		List<Object[]> result = edocStatNewDao.findEdocStatResultByHql(flipInfo, conditionMap);
		LOGGER.info("其它查询____:"+(System.currentTimeMillis() - t_m));
		
		t_m = System.currentTimeMillis();
		if(Strings.isNotEmpty(result)) {
			Calendar startTimeC = Calendar.getInstance();
			EdocStatParamVO paramVo = null;
			for(Object[] object : result) {
				paramVo = new EdocStatParamVO();
				paramVo.setStatDoneParam(object);
				if(Strings.isNotEmpty(statVoList)) {
					if(displayType==EdocStatDisplayTypeEnum.time.key() && displayTimeType==EdocStatDisplayTimeTypeEnum.day.key()) {//按日统计
						startTimeC.setTime(paramVo.getStartTime());
						String timeDisplayId = EdocStatHelper.getTimeDisplayId(startTimeC, displayTimeType);
						if(statVoMap.containsKey(timeDisplayId)) {
							EdocStatVO statVo = (EdocStatVO)statVoMap.get(timeDisplayId);
							statVo = getCountEdocStatVO(statVo, paramVo, showBanwenYuewen, statAffairMap);
						}
					} else {//按人、部门、时间-年、时间-季、时间-月统计
						for(EdocStatVO statVo : statVoList) {
							if(displayType == EdocStatDisplayTypeEnum.department.key()) {//部门
								if(EdocStatHelper.isSameOrDeptFlag(statVo, paramVo.getMemberDeptId())) {
									statVo = getCountEdocStatVO(statVo, paramVo, showBanwenYuewen, statAffairMap);
								}
							} else if(displayType == EdocStatDisplayTypeEnum.member.key()) {//人员
								if(statVo.getDisplayId().equals(String.valueOf(paramVo.getAffairMemberId()))) {
									statVo = getCountEdocStatVO(statVo, paramVo, showBanwenYuewen, statAffairMap);
								}
							} else {//时间
								V3xOrgMember member = orgManager.getMemberById(paramVo.getAffairMemberId());
								if(isCurrentAccountExchange) {//单位收发员
									//经办人是否是当前登陆人员的单位||经办人部门是否为选中部门
									if(member.getOrgAccountId().longValue() != user.getLoginAccount().longValue() 
											|| (Strings.isNotEmpty(selectDeptIdList) && !selectDeptIdList.contains(member.getOrgDepartmentId()))) {
										continue;
									}
								} else if(isCurrentDeptExchange) {//部门收发员
									//经办人是否是当前登陆人员的部门
									if(!userSelfAndSubDeptIdList.contains(member.getOrgDepartmentId())) {
										continue;
									}
								}
								String timeDisplayId = EdocStatHelper.getTimeDisplayId(startTimeC, displayTimeType);
								if(statVo.getDisplayId().equals(timeDisplayId)) {
									statVo = getCountEdocStatVO(statVo, paramVo, showBanwenYuewen, statAffairMap);
								}
							}
						} 
					}
				}
			}
		}
		LOGGER.info("缓存过滤____:"+(System.currentTimeMillis() - t_m));
	}
	
	/**
	 * 获取公文统计穿透列表
	 * @param flipInfo
	 * @param conditionMap
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo getEdocVoList(FlipInfo flipInfo, Map<String, String> conditionMap) throws BusinessException {
		long t_start = System.currentTimeMillis();
		User user = AppContext.getCurrentUser();
		int listType = Integer.parseInt(conditionMap.get("listType"));
		setConditionOperationTypeIds(conditionMap);
		boolean showBanwenYuewen = EdocSwitchHelper.showBanwenYuewen(user.getLoginAccount());
		List<Integer> listTypeList = new ArrayList<Integer>();
		if(showBanwenYuewen) {
			listTypeList.add(EdocStatEnum.EdocStatListTypeEnum.readAll.key());
			listTypeList.add(EdocStatEnum.EdocStatListTypeEnum.readed.key());
			listTypeList.add(EdocStatEnum.EdocStatListTypeEnum.reading.key());
		}
		List<Integer> finishStatList = new ArrayList<Integer>();
		finishStatList.add(EdocConstant.flowState.finish.ordinal());
		finishStatList.add(EdocConstant.flowState.terminate.ordinal());
		List<EdocStatListVO> edocVoList = new ArrayList<EdocStatListVO>();
		
		long t_m = System.currentTimeMillis();
		List<Object[]> result = edocStatNewDao.findStatEdocList(flipInfo, conditionMap);
		LOGGER.info("已发穿透数据库查询_________________:"+(System.currentTimeMillis() - t_m));
		
		if(Strings.isNotEmpty(result)) {
			t_m = System.currentTimeMillis();
			Map<Long, String> members = getCurrentNodesInfoOfMembers(result);
			LOGGER.info("获取公文当前待办人____:"+(System.currentTimeMillis() - t_m));
			
			List<Long> summaryIdList = new ArrayList<Long>();
			for(Object[] object : result) {
				summaryIdList.add((Long)object[0]);
			}
			
			t_m = System.currentTimeMillis();
			List<Object[]> longtextFields = edocStatNewDao.getEdocLongtextFields(summaryIdList);
			LOGGER.info("获取当前页公文的大字段____:"+(System.currentTimeMillis() - t_m));
			
			t_m = System.currentTimeMillis();
			Long orgAccountId = 0L;
			for(Object[] object : result) {
			    EdocStatListVO listVo = new EdocStatListVO();
				listVo.setListValue(object);
				//公文大字段内容设置
				if(Strings.isNotEmpty(longtextFields)) {
					for(Object[] fields : longtextFields) {
						Long summaryId = (Long)fields[0];
						if(listVo.getSummaryId().longValue() == summaryId.longValue()) {
							listVo.setSendUnit((String)fields[1]);
							listVo.setCurrentNodesInfo((String)fields[2]);
							listVo.setUndertakenoffice((String)fields[3]);
							break;
						}
					}
				}				
				//拟稿人
				V3xOrgMember member = orgManager.getMemberById(listVo.getStartUserId());
				listVo.setStartUserName(member==null ? "" : member.getName());
				//办结状态
				if(showBanwenYuewen && listTypeList.contains(listType)) {
					listVo.setIsFinishView("");
				} else {
					if(finishStatList.contains(listVo.getState())) {
						listVo.setIsFinishView(ResourceUtil.getString("edoc.stat.result.list.label.yes"));
					} else {
						listVo.setIsFinishView(ResourceUtil.getString("edoc.stat.result.list.label.no"));
					}
				}
				//归档状态
				if(listVo.getHasArchive()) {
					listVo.setHasArchiveView(ResourceUtil.getString("edoc.stat.result.list.label.yes"));
				} else {
					listVo.setHasArchiveView(ResourceUtil.getString("edoc.stat.result.list.label.no"));
				}
				String archiveName = "";
		        String fullArchiveName = "";
				if(listVo.getHasArchive() && listVo.getArchiveId() != null && AppContext.hasPlugin("doc")) {
	                archiveName =  edocManager.getShowArchiveNameByArchiveId(listVo.getArchiveId());
	                fullArchiveName = edocManager.getFullArchiveNameByArchiveId(listVo.getArchiveId());
				}
				listVo.setArchiveName(archiveName);
				listVo.setArchiveAllName(fullArchiveName);
				//超期时长
				String isNull = ResourceUtil.getString("collaboration.project.nothing.label"); //无
				Long deadline = listVo.getDeadline();
	            if(listVo.getDeadlineDatetime()==null) {
	            	listVo.setDeadlineTimeView((deadline == null || deadline == 0) ? isNull : WFComponentUtil.getDeadLineNameForEdoc(deadline, listVo.getStartTime()));
	            } else {
	            	listVo.setDeadlineTimeView(EdocHelper.getDeadLineName(listVo.getDeadlineDatetime()));
	            }
				//设置流程是否超期标志
				Date finishDate = listVo.getCompleteTime();
				Date now = new Date(System.currentTimeMillis());
				Long expendTime = 0L;
				if(listVo.getDeadlineDatetime() != null) {
					orgAccountId = EdocHelper.getFlowPermAccountId(user.getLoginAccount(), listVo.getTempleteId(), listVo.getOrgAccountId(), templateManager);
					if(finishDate == null) {
						expendTime = workTimeManager.getDealWithTimeValue(listVo.getDeadlineDatetime(), now, orgAccountId);
						if(expendTime > 0) {
							listVo.setWorklfowTimeout(true);
						}
					} else {
						expendTime = workTimeManager.getDealWithTimeValue(listVo.getDeadlineDatetime(), listVo.getCompleteTime(), orgAccountId);
						if(expendTime > 0) {
							listVo.setWorklfowTimeout(true);
						}
					}
					if(expendTime < 0) {
						listVo.setDeadlineOverView("");
					} else {
						listVo.setDeadlineOverView(String.valueOf(expendTime));
					}
				}
				if(listVo.getWorklfowTimeout()) {
					listVo.setCoverTimeView(ResourceUtil.getString("edoc.stat.result.list.label.yes"));
					if(expendTime > 0) {
						expendTime = (expendTime/1000)/60;
						listVo.setDeadlineOverView(showDate(Integer.parseInt(expendTime.toString()), true));
					}
				} else {
					listVo.setCoverTimeView(ResourceUtil.getString("edoc.stat.result.list.label.no"));
					listVo.setDeadlineOverView("");
				}				
				//当前待办人
				listVo.setCurrentNodesInfo(EdocHelper.parseCurrentNodesInfo(listVo.getCompleteTime(), listVo.getCurrentNodesInfo(), members));
				//公文级别
				if(Strings.isNotBlank(listVo.getUnitLevel())) {
					String unitLevelName = "";
					EdocElement element = edocElementManager.getByFieldName("unit_level", user.getLoginAccount());
					if(element != null && element.getMetadataId()!=null) {
						List<CtpEnumItem> enumItemList = enumManagerNew.getEnumItemInDatabse(element.getMetadataId());
						if(Strings.isNotEmpty(enumItemList)) {
							for(CtpEnumItem item : enumItemList) {
								if(listVo.getUnitLevel().equals(item.getValue())) {
									unitLevelName = item.getLabel();
									break;
								}
							}
						}
						listVo.setUnitLevelName(unitLevelName);
					}
				}
				edocVoList.add(listVo);
			}
			LOGGER.info("批量设置当前页公文的值____:"+(System.currentTimeMillis() - t_m));
		}
		flipInfo.setData(edocVoList);
		LOGGER.info("getEdocVoList方法使用时长____:"+(System.currentTimeMillis() - t_start));
		return flipInfo;
	}
	
	/**
	 * 公文统计界面-选择枚举值
	 * @param flipInfo
	 * @param conditionMap
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo getEdocEnumitemList(FlipInfo flipInfo, Map<String, String> conditionMap) throws BusinessException {
		String fieldName = conditionMap.get("fieldName");
		User user = AppContext.getCurrentUser();
		EdocElement element = edocElementManager.getByFieldName(fieldName, user.getLoginAccount());
		if(element != null && element.getMetadataId()!=null) {
			List<CtpEnumItem> enumItemList = enumManagerNew.getEnumItemInDatabse(element.getMetadataId());
			flipInfo.setData(enumItemList);
		}
		return flipInfo;
	}
	
	/**
	 * 显示统计行
	 * @param conditionMap
	 * @param isCurrentAccountExchange
	 * @param isCurrentDeptExchange
	 * @param userSelfAndSubDeptIdList
	 * @return
	 * @throws BusinessException
	 */
	private Map<String, Object> getEdocStatVoListView(Map<String, String> conditionMap) throws BusinessException {
	    
		User user = AppContext.getCurrentUser();
		
		int displayType = Strings.isBlank(conditionMap.get("displayType")) 
		        ? EdocStatDisplayTypeEnum.department.key() : Integer.parseInt(conditionMap.get("displayType"));
		        
		int displayTimeType = Strings.isBlank(conditionMap.get("displayTimeType")) 
		        ? EdocStatDisplayTimeTypeEnum.year.key() : Integer.parseInt(conditionMap.get("displayTimeType"));
		
		//开始结束时间
		String startRangeTime = conditionMap.get("startRangeTime");
		String endRangeTime = conditionMap.get("endRangeTime");
		
		//统计范围
		String rangeIds =  conditionMap.get("rangeIds");
		String[] ranges = Strings.isNotEmpty(rangeIds) ? rangeIds.split(",") : new String[0];
		
//		List<Long> userSelfAndSubDeptIdList = EdocStatHelper.getSelfAndSubDepartmentList(user.getDepartmentId());
		boolean isCurrentAccountExchange = GovdocRoleHelper.isAccountExchange();
//		boolean isCurrentDeptExchange = GovdocRoleHelper.isDepartmentExchange();
		
		Map<String, Object> statVoMap = new HashMap<String, Object>();
		List<EdocStatVO> statVoList = new ArrayList<EdocStatVO>();
		
		if(displayType == EdocStatDisplayTypeEnum.department.key()) {//部门
		    
		    //有交换权限的部门
			String allDeptIds  = EdocStatHelper.getUserAllDeptIds(user.getId());
			
			Map<Long, String> deptMap = new HashMap<Long, String>();
			
			for(int i=0; i < ranges.length; i++) {
			    
				String range = ranges[i];
				
				String rangeType = range.split("[|]")[0];
				Long rangeId = Long.parseLong(range.split("[|]")[1]);
				
				Long deptId = rangeId;
				
				List<Long> selfAndSubDeptIdList = new ArrayList<Long>();
				Map<Long, List<Long>> subDeptIdMap = new HashMap<Long, List<Long>>();
				
				if("Member".equals(rangeType)) {
				    
					V3xOrgMember member = orgManager.getMemberById(rangeId);
					deptId = member.getOrgDepartmentId();
					selfAndSubDeptIdList.add(deptId);
					
				} else if("Department".equals(rangeType)) {
				    
					selfAndSubDeptIdList.add(deptId);
					if(!EdocStatHelper.hasChildDeptFlag(range)) {
						selfAndSubDeptIdList.addAll(EdocStatHelper.getSubDepartmentList(deptId));
					}
					
				} else if("Account".equals(rangeType)) {
				    
					List<V3xOrgDepartment> deptList = orgManager.getAllDepartments(rangeId);
					if(Strings.isNotEmpty(deptList)) {
						for(V3xOrgDepartment dept : deptList)  {
							if(dept.getIsInternal()) {
								selfAndSubDeptIdList.add(dept.getId());
							}
						}
					}
				}
				
				if(!deptMap.containsKey(deptId)) {
				    
					boolean isDeptExchange = false;
					
					if(!isCurrentAccountExchange) {
					    
						isDeptExchange = GovdocRoleHelper.isDepartmentExchange(user.getId(), deptId, user.getLoginAccount());
						
						if(!isDeptExchange) {
							if(Strings.isNotBlank(allDeptIds) && allDeptIds.indexOf(deptId+",") >= 0) {
								isDeptExchange = true;
							}
						}
					}
					
					if(isCurrentAccountExchange || (isDeptExchange && selfAndSubDeptIdList.contains(deptId))) {
					    
						if(Strings.isNotEmpty(selfAndSubDeptIdList)) {
						    
							for(Long subDeptId : selfAndSubDeptIdList) {
								//兼职人员的部门不显示
								V3xOrgDepartment dept = orgManager.getDepartmentById(subDeptId);
								if(dept.getOrgAccountId().longValue() != user.getLoginAccount().longValue()) {
									continue;
								}
								deptMap.put(subDeptId, dept.getName());
								EdocStatVO edocStatVo = getDisplayEdocStatVO(statVoMap, displayType, String.valueOf(subDeptId), 
								        dept.getId(), rangeType, rangeId, subDeptIdMap.get(deptId), dept.getName(), -1);
								statVoList.add(edocStatVo);
							}
							
						} else {//这个分支应该是一段死代码
						    
							//兼职人员的部门不显示
							V3xOrgDepartment dept = orgManager.getDepartmentById(deptId);
							if(dept != null && dept.getOrgAccountId().longValue() == user.getLoginAccount().longValue()) {
							    
							    deptMap.put(deptId, dept.getName());
							    
							    EdocStatVO edocStatVo = getDisplayEdocStatVO(statVoMap, displayType, String.valueOf(deptId), dept.getId(), 
							            rangeType, rangeId, subDeptIdMap.get(deptId), dept.getName(), -1);
	                            statVoList.add(edocStatVo);
							}
						}
					}
				}
			}
		} else if(displayType == EdocStatDisplayTypeEnum.member.key()) {//人员
			String allDeptIds  = EdocStatHelper.getUserAllDeptIds(user.getId());
			Map<Long, String> memberMap = new HashMap<Long, String>();
			for(int i=0; i<ranges.length; i++) {
				String range = ranges[i];
				String rangeType = range.split("[|]")[0];
				Long rangeId = Long.parseLong(range.split("[|]")[1]);
				if("Department".equals(rangeType)) {
					boolean childFlag = EdocStatHelper.hasChildDeptFlag(range);
					List<Long> subDeptIdList = EdocStatHelper.getSelfAndSubDepartmentList(rangeId, childFlag);
					List<V3xOrgMember> memberList = orgManager.getMembersByDepartment(rangeId, childFlag);
					if(Strings.isNotEmpty(memberList)) {
					    
					    boolean isDeptExchange = false;
                        if(!isCurrentAccountExchange) {
                            isDeptExchange = GovdocRoleHelper.isDepartmentExchange(user.getId(), rangeId, user.getLoginAccount());
                            if(!isDeptExchange) {
                                if(Strings.isNotBlank(allDeptIds) && allDeptIds.indexOf(rangeId+",") >= 0) {
                                    isDeptExchange = true;
                                }
                            }
                        }
                        
						for(V3xOrgMember member : memberList) {
							if(!memberMap.containsKey(member.getId())) {
							    
								//兼职人员的部门不显示
								if(member.getOrgAccountId().longValue() == user.getLoginAccount().longValue() 
										&& !subDeptIdList.contains(member.getOrgDepartmentId())) {
									continue;
								}
								
								if(isCurrentAccountExchange || isDeptExchange) {
									memberMap.put(member.getId(), member.getName());
									EdocStatVO edocStatVO = getDisplayEdocStatVO(statVoMap, displayType, String.valueOf(member.getId()), member.getOrgDepartmentId(), rangeType, rangeId, null, member.getName(), -1);
									statVoList.add(edocStatVO);
								}
							}
						}
					}
				} else if("Member".equals(rangeType)) {
					V3xOrgMember member = orgManager.getMemberById(rangeId);
					if(!memberMap.containsKey(member.getId())) {
						memberMap.put(member.getId(), member.getName());
						EdocStatVO edocStatVO = getDisplayEdocStatVO(statVoMap, displayType, String.valueOf(member.getId()), member.getOrgDepartmentId(), rangeType, rangeId, null, member.getName(), -1);
						statVoList.add(edocStatVO);
					}
				} else if("Account".equals(rangeType)) {
					List<V3xOrgMember> memberList = orgManager.getAllMembers(rangeId);
					if(Strings.isNotEmpty(memberList)) {
						for(V3xOrgMember member : memberList) {
							memberMap.put(member.getId(), member.getName());
							EdocStatVO edocStatVO = getDisplayEdocStatVO(statVoMap, displayType, String.valueOf(member.getId()), member.getOrgDepartmentId(), rangeType, rangeId, null, member.getName(), -1);
							statVoList.add(edocStatVO);
						}
					}
				}
			}
		} else {//时间
			if(Strings.isNotBlank(startRangeTime) && Strings.isNotBlank(endRangeTime)) {
			    
			    startRangeTime = startRangeTime + " 00:00:00";
			    endRangeTime = endRangeTime + " 23:59:59";
			    
				Date startTime = Datetimes.parseDatetime(startRangeTime);
				Date endTime = Datetimes.parseDatetime(endRangeTime);
				Calendar startC = Calendar.getInstance();
				startC.setTime(startTime);
				Calendar endC = Calendar.getInstance();
				endC.setTime(endTime);
				if(displayTimeType == EdocStatDisplayTimeTypeEnum.year.key()) {//年
					for(int year=startC.get(Calendar.YEAR); year<=endC.get(Calendar.YEAR); year++) {
						statVoList.add(getStatDisplayTimeTypeView(statVoMap, displayTimeType, year, 0, 0));
					}
				} else if(displayTimeType == EdocStatDisplayTimeTypeEnum.quarter.key()) {//季度
					for(int year=startC.get(Calendar.YEAR); year<=endC.get(Calendar.YEAR); year++) {
						Map<Integer, Integer> quarterMap = new HashMap<Integer, Integer>();
						if(startC.get(Calendar.YEAR) == endC.get(Calendar.YEAR)) {//当年
							for(int month=startC.get(Calendar.MONTH)+1; month<=endC.get(Calendar.MONTH)+1; month++) {
								Integer quarter = EdocStatHelper.getQuarter(month);
								if(!quarterMap.containsKey(quarter)) {
									quarterMap.put(quarter, quarter);
									statVoList.add(getStatDisplayTimeTypeView(statVoMap, displayTimeType, year, month, 0));
								}
							}
						} else {
							if(year == startC.get(Calendar.YEAR)) {
								for(int month=startC.get(Calendar.MONTH)+1; month<=12; month++) {
									Integer quarter = EdocStatHelper.getQuarter(month);
									if(!quarterMap.containsKey(quarter)) {
										quarterMap.put(quarter, quarter);
										statVoList.add(getStatDisplayTimeTypeView(statVoMap, displayTimeType, year, month, 0));
									}
								}
							} else if(year > startC.get(Calendar.YEAR)) {
								if(year < endC.get(Calendar.YEAR)) {
									for(int month=1; month<=12; month++) {
										Integer quarter = EdocStatHelper.getQuarter(month);
										if(!quarterMap.containsKey(quarter)) {
											quarterMap.put(quarter, quarter);
											statVoList.add(getStatDisplayTimeTypeView(statVoMap, displayTimeType, year, month, 0));
										}
									}
								} else {
									for(int month=1; month<=endC.get(Calendar.MONTH)+1; month++) {
										Integer quarter = EdocStatHelper.getQuarter(month);
										if(!quarterMap.containsKey(quarter)) {
											quarterMap.put(quarter, quarter);
											statVoList.add(getStatDisplayTimeTypeView(statVoMap, displayTimeType, year, month, 0));
										}
									}
								}
							}
						}
						quarterMap = null;
					}
				} else if(displayTimeType == EdocStatDisplayTimeTypeEnum.month.key()) {//月
					for(int year=startC.get(Calendar.YEAR); year<=endC.get(Calendar.YEAR); year++) {
						if(startC.get(Calendar.YEAR) == endC.get(Calendar.YEAR)) {//当年
							for(int month=startC.get(Calendar.MONTH)+1; month<=endC.get(Calendar.MONTH)+1; month++) {
								statVoList.add(getStatDisplayTimeTypeView(statVoMap, displayTimeType, year, month, 0));
							}
						} else {
							if(year == startC.get(Calendar.YEAR)) {//开始日期年
								for(int month=startC.get(Calendar.MONTH)+1; month<=12; month++) {
									statVoList.add(getStatDisplayTimeTypeView(statVoMap, displayTimeType, year, month, 0));
								}
							}
							else if(year > startC.get(Calendar.YEAR)) {//
								if(year < endC.get(Calendar.YEAR)) {
									for(int month=1; month<=12; month++) {
										statVoList.add(getStatDisplayTimeTypeView(statVoMap, displayTimeType, year, month, 0));
									}
								} else {
									for(int month=1; month<=endC.get(Calendar.MONTH)+1; month++) {
										statVoList.add(getStatDisplayTimeTypeView(statVoMap, displayTimeType, year, month, 0));
									}
								}
							}
						}
					}
				} else {//日
					// 测试此日期是否在指定日期之后  
					Calendar tempC = Calendar.getInstance();
					tempC.setTime(startC.getTime());
					while (endC.compareTo(tempC) >= 0) {
						statVoList.add(getStatDisplayTimeTypeView(statVoMap, displayTimeType, tempC.get(Calendar.YEAR), tempC.get(Calendar.MONTH)+1, tempC.get(Calendar.DATE)));
						// 根据日历的规则，为给定的日历字段添加或减去指定的时间量    
						tempC.add(Calendar.DAY_OF_MONTH, 1);
					}
					//statVoList.add(getStatDisplayTimeTypeView(statVoMap, displayTimeType, tempC.get(Calendar.YEAR), tempC.get(Calendar.MONTH)+1, tempC.get(Calendar.DATE)));
				}
			}
		}
		statVoMap.put("statVoList", statVoList);
		return statVoMap;
	}
	
	/**
	 * 获取公文当前待办人
	 * @param result
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private Map<Long, String> getCurrentNodesInfoOfMembers(List result) {
		//TODO 临时方案解决性能问题
		Map<Long, String> members = new HashMap<Long, String>();
		if(result.size() > 100) {
		    try{
    		    List<V3xOrgMember> allMember =  orgCache.getAllV3xOrgEntity(V3xOrgMember.class, null);
    		    for (V3xOrgMember m : allMember) {
    		        members.put(m.getId(), m.getName());
    		    }
		    }
		    catch(Exception e) {
		        LOGGER.error("", e);
		    }
		}
		return members;
	}
	
	/**
	 * 拼装公文统计对象
	 * @param displayType
	 * @param displayId
	 * @param subDeptIdList
	 * @param displayName
	 * @param displayTimeType
	 * @return
	 */
	private EdocStatVO getDisplayEdocStatVO(Map<String, Object> statVoMap, Integer displayType, 
	        String displayId, Long displayDeptId, String statRangeType, 
	        Long statRangeId, List<Long> subDeptIdList, String displayName, Integer displayTimeType) {
	    
		EdocStatVO statVo = new EdocStatVO();
		statVo.setDisplayType(displayType);
		statVo.setDisplayId(displayId);
		statVo.setDisplayDeptId(displayDeptId);
		statVo.setSubDeptIdList(subDeptIdList);
		statVo.setDisplayName(displayName);
		statVo.setDisplayTimeType(displayTimeType);
		statVo.setStatRangeType(statRangeType);
		statVo.setStatRangeId(statRangeId);
		statVoMap.put(displayId, statVo);
		
		return statVo;
	}
	
	/**
	 * 
	 * @param displayTimeType
	 * @param statVoMap
	 */
	private EdocStatVO getStatDisplayTimeTypeView(Map<String, Object> statVoMap, int displayTimeType, int year, int month, int day) {
		int displayType = EdocStatEnum.EdocStatDisplayTypeEnum.time.key();
		String timeDisplayId = EdocStatHelper.getDisplayTimeTypeKey(displayTimeType, year, month, day);
		String displayName = "";
		if(displayTimeType == 1) {//年
			displayName = ResourceUtil.getString("edoc.stat.result.range.year", String.valueOf(year));
		} else if(displayTimeType ==2) {//季
			Integer quarter = EdocStatHelper.getQuarter(month);
			displayName = ResourceUtil.getString("edoc.stat.result.range.yearOrquarter", String.valueOf(year), quarter);
		} else if(displayTimeType == 3) {
			displayName = ResourceUtil.getString("edoc.stat.result.range.yearOrMonth", String.valueOf(year), month);
		} else {
			displayName = year + "-" + EdocStatHelper.getString2Gigit(month) + "-" + EdocStatHelper.getString2Gigit(day);
		}
		EdocStatVO statVo = getDisplayEdocStatVO(statVoMap, displayType, timeDisplayId, -1L, "", -1L, null, displayName, displayTimeType);
		return statVo;
	}
	
	/**
	 * 拼装公文统计数据
	 * @param statVo
	 * @param summaryVO
	 * @param statAffairMap
	 * @return
	 */
	private EdocStatVO getCountEdocStatVO(EdocStatVO statVo, EdocStatParamVO paramVo, boolean showBanwenYuewen, Map<String, Long> statAffairMap) {
		Long affairMemberId = paramVo.getAffairMemberId();
		Long summaryId = paramVo.getSummaryId();
		int state = paramVo.getState();
		int affairState = paramVo.getAffairState();
		String affairNodePolicy = paramVo.getAffairNodePolicy();
		int affairSubState = paramVo.getAffairSubState()==null? 0 : paramVo.getAffairSubState();
		int affairSubApp = paramVo.getAffairSubApp();
		Boolean isDo = affairSubApp==ApplicationSubCategoryEnum.edocRecHandle.key() || !showBanwenYuewen;
		Boolean isRead = affairSubApp==ApplicationSubCategoryEnum.edocRecRead.key() && showBanwenYuewen;
		Boolean isFinish = state == EdocConstant.flowState.finish.ordinal() || state == EdocConstant.flowState.terminate.ordinal();
		String keyDisplayId = "";
		if(statVo.getDisplayType().intValue()==1) {
			keyDisplayId = String.valueOf(paramVo.getMemberDeptId());
			if(Strings.isNotEmpty(statVo.getSubDeptIdList()) && statVo.getSubDeptIdList().contains(Long.parseLong(keyDisplayId))) {
				keyDisplayId = String.valueOf(statVo.getDisplayId());
			}
		} else if(statVo.getDisplayType().intValue()==2) {
			keyDisplayId = String.valueOf(affairMemberId);
		} else if(statVo.getDisplayType().intValue()==3) {
			keyDisplayId = statVo.getDisplayId();
		}
		String statKey = "";
		if(isFinish) {
			statKey = EdocStatListTypeEnum.finished.key()+"_"+keyDisplayId+"_"+summaryId;//已办结
			if(isDo && (affairState == StateEnum.col_pending.key() 
					|| affairState == StateEnum.col_done.key() 
					|| affairState == StateEnum.col_stepStop.key()
					|| affairState == StateEnum.col_competeOver.key())) {
				if(!statAffairMap.containsKey(statKey)) {
					statAffairMap.put(statKey, summaryId);
					statVo.setCountFinish(statVo.getCountFinish() + 1);
				}
			}
		} else {
			statKey = EdocStatListTypeEnum.wait_finished.key()+"_"+keyDisplayId+"_"+summaryId;//未办结
			if(isDo && (affairState == StateEnum.col_pending.key() 
					|| affairState == StateEnum.col_done.key()
					|| affairState == StateEnum.col_competeOver.key())) {
				if(!statAffairMap.containsKey(statKey)) {
					statAffairMap.put(statKey, summaryId);
					statVo.setCountWaitFinish(statVo.getCountWaitFinish() + 1);
				}
			}
		}
		/*if(affairState.intValue() == StateEnum.col_sent.key()) {
			statKey = EdocStatListTypeEnum.sent.key()+"_"+keyDisplayId+"_"+summaryId;//已发
			if(!statAffairMap.containsKey(statKey)) {
				statAffairMap.put(statKey, summaryId);
				statVo.setCountSent(statVo.getCountSent() + 1);
			}
		}*/
		if(isDo && affairState == StateEnum.col_done.key()) {
			statKey = EdocStatListTypeEnum.done.key()+"_"+keyDisplayId+"_"+summaryId;//已办
			if(!statAffairMap.containsKey(statKey)) {
				statAffairMap.put(statKey, summaryId);
				statVo.setCountDone(statVo.getCountDone() + 1);
			}
		}
		if(isDo && affairState == StateEnum.col_pending.key()) {
			if(affairSubState == SubStateEnum.col_pending_ZCDB.key() || affairSubState== SubStateEnum.col_pending_specialBackToSenderReGo.key()) {
				statKey = EdocStatListTypeEnum.zcdb.key()+"_"+keyDisplayId+"_"+summaryId;//在办
				if(!statAffairMap.containsKey(statKey)) {
					statAffairMap.put(statKey, summaryId);
					statVo.setCountZcdb(statVo.getCountZcdb() + 1);
				}
			} else {
				statKey = EdocStatListTypeEnum.pending.key()+"_"+keyDisplayId+"_"+summaryId;//待办
				if(!statAffairMap.containsKey(statKey)) {
					statAffairMap.put(statKey, summaryId);
					statVo.setCountPending(statVo.getCountPending() + 1);
				}
			}
		}
		if(paramVo.getEdocType().intValue() == 1) {//收文
			if(affairState == StateEnum.col_done.key() && isRead) {
				statKey = EdocStatListTypeEnum.readed.key()+"_"+keyDisplayId+"_"+summaryId;//已阅
				if(!statAffairMap.containsKey(statKey)) {
					statAffairMap.put(statKey, summaryId);
					statVo.setCountReaded(statVo.getCountReaded() + 1);
				}
			}
			if(affairState == StateEnum.col_pending.key() && isRead) {
				statKey = EdocStatListTypeEnum.reading.key()+"_"+keyDisplayId+"_"+summaryId;//待阅
				if(!statAffairMap.containsKey(statKey)) {
					statAffairMap.put(statKey, summaryId);
					statVo.setCountReading(statVo.getCountReading() + 1);
				}
			}
			if(isDo && state!=EdocConstant.flowState.terminate.ordinal() 
					&& affairState==StateEnum.col_done.key() && "chengban".equals(affairNodePolicy)) {
				statKey = EdocStatListTypeEnum.undertaker.key()+"_"+keyDisplayId+"_"+summaryId;//承办数
				if(!statAffairMap.containsKey(statKey)) {
					statAffairMap.put(statKey, summaryId);
					statVo.setCountUndertaker(statVo.getCountUndertaker() + 1);
				}
			}
			if(isRead) {
				statKey = EdocStatListTypeEnum.readAll.key()+"_"+keyDisplayId+"_"+summaryId;//总阅件
				if(!statAffairMap.containsKey(statKey)) {
					statAffairMap.put(statKey, summaryId);
					statVo.setCountReadAll(statVo.getCountReadAll() + 1);
				}
			}
			statVo.setCountDoAll(statVo.getCountFinish() + statVo.getCountWaitFinish());//总办结
		} else {
			statVo.setCountHandleAll(statVo.getCountFinish() + statVo.getCountWaitFinish());//总经办
		}
		return statVo;
	}
		
	/**
	 * 统计条件中，模板id添加系统模板另存的
	 * @param conditionMap
	 * @throws BusinessException
	 */
	private void setConditionOperationTypeIds(Map<String, String> conditionMap) throws BusinessException {
		if(Strings.isNotBlank(conditionMap.get("operationTypeIds"))) {
			String temp = conditionMap.get("operationTypeIds");
			
			List<Long> idList = new ArrayList<Long>();
            for(String id : temp.split(",")) {
                idList.add(Long.parseLong(id));
            }
            
	        List<CtpTemplate> ctpTemplates = templateManager.getCtpTemplateListByIdsWithSub(idList);
			
	        StringBuilder operationTypeIds = new StringBuilder() ;
	        if(Strings.isNotEmpty(ctpTemplates)) {
	            
	            for(CtpTemplate object : ctpTemplates) {
	                operationTypeIds.append(object.getId()); 
	                operationTypeIds.append(",");
	            }
	            
                if(operationTypeIds.length()>0) {
                    operationTypeIds.deleteCharAt(operationTypeIds.length() - 1 );
                }
	        }
			
			conditionMap.put("operationTypeIds", operationTypeIds.toString());
		}
	}
	
	
    private static Integer workTime = 0;
    private static int year;
    /**
     * 将分钟数按当前工作时间转化为按天表示的时间。
     * 例如 1天7小时2分。
     */
    private  String showDate(Integer minutes, boolean isWork) {
        if(minutes == null || minutes == 0) 
            return "－";
        int dayH = 24*60;
        if(isWork) {
            Calendar cal = Calendar.getInstance();
            int y = cal.get(Calendar.YEAR);
            if(year != y || workTime.intValue() == 0 ){ //需要取工作时间
                workTime = getCurrentYearWorkTime();
                year = y;
            }
            if(workTime == null || workTime.intValue() == 0){
            	return "－";
            }
            dayH = workTime;
        }
        long m = minutes.longValue();
        long day = m/dayH;
        long d1 = m%dayH;
        long hour = d1/60;
        long minute = d1%60;
        //{0}{1,choice,0#|1#\u5929}{2}{3,choice,0#|1#\u5C0F\u65F6}{4}{5,choice,0#|1#\u5206}
        String display = ResourceUtil.getStringByParams("collaboration.date.display", day>0 ? day: "" ,  day > 0 ? 1:0, hour>0 ? hour : "" ,  hour >0 ?1:0, minute >0 ? minute : "", minute >0 ? 1 : 0);
        return display;
    }
    
    private int  getCurrentYearWorkTime() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int t = 0;
        try {
            t = workTimeManager.getEachDayWorkTime(year, AppContext.getCurrentUser().getLoginAccount());
        } catch (WorkTimeSetExecption e) {
            LOGGER.error("", e);
        }
        return t;
    }

	public void setEdocStatNewDao(EdocStatNewDao edocStatNewDao) {
		this.edocStatNewDao = edocStatNewDao;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public void setEdocElementManager(EdocElementManager edocElementManager) {
		this.edocElementManager = edocElementManager;
	}

	public void setEnumManagerNew(EnumManager enumManager) {
		this.enumManagerNew = enumManager;
	}


	public void setWorkTimeManager(WorkTimeManager workTimeManager) {
		this.workTimeManager = workTimeManager;
	}

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}
	
}
