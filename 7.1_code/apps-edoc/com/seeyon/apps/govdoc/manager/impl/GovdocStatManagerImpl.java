package com.seeyon.apps.govdoc.manager.impl;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.bo.DocResourceBO;
import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.govdoc.dao.GovdocStatDao;
import com.seeyon.apps.govdoc.dao.GovdocStatSetDao;
import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.helper.GovdocRoleHelper;
import com.seeyon.apps.govdoc.helper.GovdocSwitchHelper;
import com.seeyon.apps.govdoc.manager.GovdocElementManager;
import com.seeyon.apps.govdoc.manager.GovdocFormManager;
import com.seeyon.apps.govdoc.manager.GovdocPubManager;
import com.seeyon.apps.govdoc.manager.GovdocStatManager;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.govdoc.po.EdocStatSet;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgUnit;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.domain.EdocStat;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocStatManager;
import com.seeyon.v3x.edoc.util.EdocStatEnum;
import com.seeyon.v3x.edoc.util.EdocStatEnum.EdocStatDisplayTimeTypeEnum;
import com.seeyon.v3x.edoc.util.EdocStatEnum.EdocStatDisplayTypeEnum;
import com.seeyon.v3x.edoc.util.EdocStatEnum.EdocStatListTypeEnum;
import com.seeyon.v3x.edoc.util.EdocStatEnum.EdocStatResultTypeEnum;
import com.seeyon.v3x.edoc.util.EdocStatHelper;
import com.seeyon.v3x.edoc.util.GovdocStatEnum;
import com.seeyon.v3x.edoc.util.GovdocStatEnum.GovdocFinishStateEnum;
import com.seeyon.v3x.edoc.util.GovdocStatEnum.GovdocOverTimeEnum;
import com.seeyon.v3x.edoc.util.GovdocStatEnum.GovdocStatListTypeEnum;
import com.seeyon.v3x.edoc.util.GovdocStatEnum.GovdocStatTypeEnum;
import com.seeyon.v3x.edoc.util.GovdocStatUtil;
import com.seeyon.v3x.edoc.webmodel.EdocSignStatDetailVO;
import com.seeyon.v3x.edoc.webmodel.EdocStatListVO;
import com.seeyon.v3x.edoc.webmodel.EdocStatParamVO;
import com.seeyon.v3x.edoc.webmodel.EdocStatResultVO;
import com.seeyon.v3x.edoc.webmodel.EdocStatVO;
import com.seeyon.v3x.edoc.webmodel.EdocWorkStatDetialVO;
import com.seeyon.v3x.edoc.webmodel.GovdocStatConditionVO;
import com.seeyon.v3x.edoc.webmodel.GovdocStatDisplayVO;
import com.seeyon.v3x.edoc.webmodel.WebSignCount;
import com.seeyon.v3x.worktimeset.exception.WorkTimeSetExecption;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

/**
 * 新公文统计接口
 * @author 唐桂林
 *
 */
public class GovdocStatManagerImpl implements GovdocStatManager {
	
	private static final Log LOGGER = LogFactory.getLog(GovdocStatManagerImpl.class);
	
	private GovdocStatSetDao govdocStatSetDao;
	private GovdocStatDao govdocStatDao;
	private GovdocElementManager govdocElementManager;
	private GovdocPubManager govdocPubManager;
	private EdocStatManager edocStatManager;
	
	private EnumManager enumManagerNew;
	private WorkTimeManager       workTimeManager;
	private TemplateManager templateManager;
	private OrgManager orgManager;
	private AffairManager affairManager;
	private DocApi docApi;
	private GovdocFormManager govdocFormManager;
	private GovdocSummaryManager govdocSummaryManager;
	
	
	/**
	 * 新公文签收统计穿透列表
	 * @param flipInfo
	 * @param conditionMap
	 * @return
	 * @throws Exception
	 */
	public FlipInfo signStatToListGovdoc(FlipInfo flipInfo, Map<String, String> conditionMap) throws Exception {
		int listType = ParamUtil.getInt(conditionMap, "listType");
		Long statId = ParamUtil.getLong(conditionMap, "statId");		
		GovdocStatConditionVO conditionVo = new GovdocStatConditionVO(); 
		conditionVo.setDisplayId(ParamUtil.getLong(conditionMap, "displayId"));
		if (conditionMap.get("statRangeId")  != null) {
			conditionVo.setStatRangeId(ParamUtil.getLong(conditionMap, "statRangeId"));
		}
		conditionVo.setDisplayType(ParamUtil.getString(conditionMap, "displayType"));
		conditionVo.setStatType(ParamUtil.getString(conditionMap, "statType"));
		conditionVo.setStartDate(Strings.isNotBlank(conditionMap.get("startTime"))?DateUtil.parse(conditionMap.get("startTime"), "yyyy-MM-dd"):null);
		conditionVo.setEndDate(Strings.isNotBlank(conditionMap.get("endTime"))?DateUtil.parse(conditionMap.get("endTime")+" 23:59:59", "yyyy-MM-dd HH:mm:ss"):null);
		GovdocStatUtil.setGovdocStatSetVO(conditionVo, govdocStatSetDao.get(statId));
		//按顺序将统计范围分类
		Map<Long, List<Object[]>> recOrgIdMap = new HashMap<Long, List<Object[]>>();
		recOrgIdMap.put(conditionVo.getDisplayId(), new ArrayList<Object[]>());	
		List<Object[]> resultList = govdocStatDao.signStatToListGovdoc(flipInfo, listType, conditionVo);	
		/*if(Strings.isNotEmpty(resultList)) {
			for(Object[] objects : resultList) {
				Long recOrgId = Long.parseLong((String)objects[1]);
				if(recOrgIdMap.get(recOrgId) != null) {
					recOrgIdMap.get(recOrgId).add(objects);
				}
			}
		}
		resultList = recOrgIdMap.get(conditionVo.getDisplayId());*/
		if(Strings.isNotEmpty(resultList)) {
			List<EdocSignStatDetailVO> edocVoList = new ArrayList<EdocSignStatDetailVO>();
			if (listType == 1) {
				edocVoList = initRecDetailVO(resultList);
			}else{
				edocVoList = this.convertToSignListVO(listType,resultList,conditionVo);
			}
			if(edocVoList.size() > flipInfo.getTotal()){
				flipInfo.setTotal(edocVoList.size());
				int page = flipInfo.getPage();
				int size = flipInfo.getSize();
				int minPage = (page-1) * size;
				int maxPage = page * size;
				if(edocVoList.size() >= maxPage){
					edocVoList = edocVoList.subList(minPage, maxPage);
				}else{
					edocVoList = edocVoList.subList(minPage, edocVoList.size());
				}
			}
			flipInfo.setData(edocVoList);
		}
		return flipInfo;
	}
	
	 @Override
	public List<WebSignCount> findMtechSignCount(Date startDate, Date endDate,
			Long statId) throws Exception {
        List<WebSignCount> reList = new ArrayList<WebSignCount>();
		List<Long> deptList = new ArrayList<Long>();
		EdocStatSet edocStatSet = govdocStatSetDao.get(statId);
		String deptsIds = edocStatSet.getDeptIds();
		if (Strings.isNotBlank(deptsIds)) {
			String[] s = deptsIds.split(",");
			String dept[] = null;
			for (int i = 0; i < s.length; i++) {
				if (Strings.isNotBlank(s[i])) {
					dept = s[i].trim().split("[|]");
					deptList.add(Long.valueOf(dept[1]));
				}
			}
		}
		for(Long accountId : deptList){
			WebSignCount webSignCount =  new WebSignCount();
			V3xOrgAccount account = this.orgManager.getAccountById(accountId);
			if(account == null){
				V3xOrgDepartment dept = this.orgManager.getDepartmentById(accountId);
				if(dept == null){
					continue;
				}
				webSignCount.setDeptName(dept.getName());
			}else{
				webSignCount.setDeptName(account.getName());
			}			
			int countNumSize = 0;
			int noRecSignNumSize = 0;
		    List<Object[]> countNum = govdocStatDao.findMtechSignCountNum(startDate, endDate, accountId);
			List<Object[]> noRecSignNumList = govdocStatDao.findMtechNoRecSignCountNum(startDate, endDate, accountId);
			countNumSize = countNum.size();
			noRecSignNumSize = noRecSignNumList.size();
			webSignCount.setAllNum(countNumSize);
			webSignCount.setNoRecSignNum(noRecSignNumSize);
			if (countNumSize == 0) {
				webSignCount.setNoRecSignNumPer("0%");
			} else {
				String result = NumberFormat.getInstance().format(((float) noRecSignNumSize / (float) countNumSize)*100);
				webSignCount.setNoRecSignNumPer(result+"%");
			}
			//统计各个时间段值的数量
			int twoSign = 0;
			int threeSign = 0;
			int fiveSign = 0;
			for(Object[] objects:countNum){
				int state = (Integer)objects[5];
				if(state == 1)continue;
				if(objects[4] == null ||  objects[3] == null) continue;
				Timestamp receiveTime = (Timestamp) objects[4];
				Timestamp sendTime = (Timestamp) objects[3]; 
				int days = GovdocHelper.getWorkDayCount(sendTime, receiveTime);
				if(days <= 2){
				  twoSign+= 1;					
				}else if(days > 2 && days <=5){
				  threeSign += 1;			
				} else if (days > 5) {
				  fiveSign +=1;
				}
			}
			webSignCount.setTwoSign(twoSign);
			webSignCount.setThreeSign(threeSign);
			webSignCount.setFiveSign(fiveSign);
			if (countNumSize == 0) {
				webSignCount.setThreeSignPer("0%");
				webSignCount.setTwoSignPer("0%");
				webSignCount.setFiveSignPer("0%");			
			}else {
				webSignCount.setTwoSignPer(NumberFormat.getInstance().format(((float)twoSign/(float)countNumSize)*100)+"%");
				webSignCount.setThreeSignPer(NumberFormat.getInstance().format(((float)threeSign/(float)countNumSize)*100)+"%");
				webSignCount.setFiveSignPer(NumberFormat.getInstance().format(((float)fiveSign/(float)countNumSize)*100)+"%");
			}
			//分析配置是否显示 相关时间段统计值 
			String timeType = edocStatSet.getTimeType();
			String[] times = timeType.split(",");
			for(String time:times){
				if("1".equals(time)){
					webSignCount.setSfShowTwo(1);
				}else if("2".equals(time)){
					webSignCount.setSfShowThree(1);
				}else if("3".equals(time)){
					webSignCount.setSfShowFive(1);
				}else if("4".equals(time)){
					webSignCount.setSfShowNoRec(1);
				}
			}
		    
			reList.add(webSignCount);
		}
		return reList;
	}
	
	/**
	 * 分析返回结果得到VO列表
	 * @param result
	 * @param listType
	 * @return
	 * @throws Exception
	 */
	private List<EdocSignStatDetailVO> convertToSignListVO(int listType,List<Object[]> result,GovdocStatConditionVO conditionVo) throws Exception {
		List<EdocSignStatDetailVO> totalList = new ArrayList<EdocSignStatDetailVO>();			
		List<EdocSignStatDetailVO> summaryIdTwoList = new ArrayList<EdocSignStatDetailVO>();
		List<EdocSignStatDetailVO> summaryIdThreeList = new ArrayList<EdocSignStatDetailVO>();
		List<EdocSignStatDetailVO> summaryIdFiveList = new ArrayList<EdocSignStatDetailVO>();
		List<EdocSignStatDetailVO> backList = new ArrayList<EdocSignStatDetailVO>();
		List<EdocSignStatDetailVO> summaryIdNoRecList = new ArrayList<EdocSignStatDetailVO>();			
		for(Object[] objects : result) {
			Long summaryId = (Long)objects[0];
			Long recOrgId=Long.parseLong(objects[1].toString());
			Date sendTime = (Date)objects[2];
			Date recTime = (Date)objects[3];
			Integer status = (Integer)objects[4];
			if(recTime != null && status>1&&status!=10) {
				Long expendTime = workTimeManager.getDealWithTimeValue(sendTime, recTime, conditionVo.getDisplayId());
				if(expendTime > 0) {
					expendTime = expendTime/60/1000;
					long day = GovdocStatUtil.getWorkTimeDay(Integer.parseInt(expendTime.toString()), true);
					if(day <= 2) {//两个工作日内							 
						summaryIdTwoList.add(this.initRecDetailVO(summaryId, 2, objects,conditionVo));	
					} else if(day>=3 && day<=5) {//三至5个工作日
						summaryIdThreeList.add(this.initRecDetailVO(summaryId, 3, objects,conditionVo));
					} else if(day > 5) {//大于5个工作日签收
						summaryIdFiveList.add(this.initRecDetailVO(summaryId, 4, objects,conditionVo));
					}
				} else {//两个工作日内
					summaryIdTwoList.add(this.initRecDetailVO(summaryId, 2, objects,conditionVo));
				}
			} else if(status == 10&&"v3x_edoc_sign_count".equals(conditionVo.getStatType())&&conditionVo.getDisplayId().equals(recOrgId)){//发文签收统计-退回
				backList.add(this.initRecDetailVO(summaryId, 6, objects,conditionVo));
			}else if(status == 10&&"v3x_edoc_sign_self_count".equals(conditionVo.getStatType())){//收文签收统计-退回
				backList.add(this.initRecDetailVO(summaryId, 6, objects,conditionVo));
			} else {//没有签收
				summaryIdNoRecList.add(this.initRecDetailVO(summaryId, 5, objects,conditionVo));
			}
		}
		//发文总数
		totalList.addAll(summaryIdTwoList);
		totalList.addAll(summaryIdThreeList);
		totalList.addAll(summaryIdFiveList);
		totalList.addAll(backList);
		totalList.addAll(summaryIdNoRecList);
		//根据前台点击穿透类型返回相应的数据列表 
		if(listType == 1){
			return totalList;
		}else if(listType == 2){
			return summaryIdTwoList;
		}else if(listType == 3){
			return summaryIdThreeList;
		}else if(listType == 4){
			return summaryIdFiveList;
		}else if(listType == 5){
			return summaryIdNoRecList;
		}else if(listType == 6){
			return backList;
		}
		return new ArrayList<EdocSignStatDetailVO>();
	}
	
	/**
	 * 生成签收数据穿透VO对象
	 * @param summaryId
	 * @param type
	 * @param objects
	 * @return
	 * @throws Exception
	 */
    private EdocSignStatDetailVO initRecDetailVO(long summaryId,int type,Object[] objects,GovdocStatConditionVO conditionVo)throws Exception{
    	EdocSignStatDetailVO dVo = new EdocSignStatDetailVO();
    	dVo.setSummaryId(summaryId);
		dVo.setRecTime((Timestamp)objects[3]);
		try {
			Long exchangeSummaryId =Long.valueOf((String) objects[6]);
			dVo.setExchangeSummaryId(exchangeSummaryId);
		} catch (Exception e) {
		}
		if(dVo.getRecTime() == null){
			dVo.setRecTimeView("");
		}else {
			dVo.setRecTimeView(DateUtil.format(dVo.getRecTime(), "yyyy-MM-dd HH:mm"));
        }
        if (objects.length > 5) {
			dVo.setRecUserName((String) objects[5]);
			//加入退文相关元素VO值
			if (type == 6 && objects[1] != null) {
				dVo.setSummaryId((Long) objects[6]);
				if (objects.length > 7) {
					try {
						dVo.setBackTimeView(DateUtil.format((Timestamp) objects[7], "yyyy-MM-dd HH:mm"));
					} catch (Exception e) {
						LOGGER.info(objects[7]);
						LOGGER.info("日期转换出错", e);
					}
					dVo.setBackOpinion((String) objects[8]);
				}
			} 
		}
		List<Object[]> largeObjects = govdocSummaryManager.getInfoField2RecVo(summaryId);
		if(largeObjects !=null&&largeObjects.size()>0) {
			Object[] edocSInfo = largeObjects.get(0);
			dVo.setDocMark((String)edocSInfo[1]);
			dVo.setSubject((String)edocSInfo[2]);
			dVo.setIssuer((String)edocSInfo[3]);
			dVo.setSendUnit((String)edocSInfo[4]);
			dVo.setSendDepartment((String)edocSInfo[5]);
			dVo.setStartTime((Timestamp)edocSInfo[6]);
			if(dVo.getStartTime() == null){
				dVo.setStartTimeView("");
			} else {
				dVo.setStartTimeView(DateUtil.format(dVo.getStartTime(), "yyyy-MM-dd HH:mm"));
	        }
		}
    	return dVo;
    }
	
	/**
     * 生成收文总数数据穿透VO对象
     * @param SUMMARYID
     * @param type
     * @param objects
     * @return
     * @throws Exception
     */
    private List<EdocSignStatDetailVO> initRecDetailVO(List<Object[]> objects)throws Exception{
    	List<EdocSignStatDetailVO> vos = new ArrayList<EdocSignStatDetailVO>();
    	for (Object[] edocSInfo : objects) {
    		EdocSignStatDetailVO dVo = new EdocSignStatDetailVO();
    		dVo.setSummaryId((Long)edocSInfo[0]);
			dVo.setDocMark((String)edocSInfo[1]);
			dVo.setSubject((String)edocSInfo[2]);
			dVo.setIssuer((String)edocSInfo[3]);
			dVo.setSendUnit((String)edocSInfo[4]);
			dVo.setSendDepartment((String)edocSInfo[5]);
			dVo.setStartTime((Timestamp)edocSInfo[6]);
			if(dVo.getStartTime() == null){
				dVo.setStartTimeView("");
			}else {
				dVo.setStartTimeView(DateUtil.format(dVo.getStartTime(), "yyyy-MM-dd HH:mm"));
	        }
			vos.add(dVo);
		}
    	return vos;
    }
	
	/**
	 * 新公文统计穿透列表
	 * @param flipInfo
	 * @param conditionMap
	 * @return
	 * @throws Exception
	 */
	public FlipInfo statToListGovdoc(FlipInfo flipInfo, Map<String, String> conditionMap) throws Exception {

		int listType = ParamUtil.getInt(conditionMap, "listType");
		Long statId = ParamUtil.getLong(conditionMap, "statId");
		
		GovdocStatConditionVO conditionVo = new GovdocStatConditionVO(); 
		conditionVo.setDisplayId(ParamUtil.getLong(conditionMap, "displayId"));
		conditionVo.setDisplayType(ParamUtil.getString(conditionMap, "displayType"));
		conditionVo.setDocMark(conditionMap.get("docMark_txt"));
		conditionVo.setDocMarkDefId(conditionMap.get("docMark"));
		conditionVo.setSerialNo(conditionMap.get("serialNo_txt"));
		conditionVo.setSerialNoDefId(conditionMap.get("serialNo"));
		conditionVo.setStartDate(Strings.isNotBlank(conditionMap.get("startTime"))?DateUtil.parse(conditionMap.get("startTime"), "yyyy-MM-dd"):null);
		conditionVo.setEndDate(Strings.isNotBlank(conditionMap.get("endTime"))?DateUtil.parse(conditionMap.get("endTime")+" 23:59:59", "yyyy-MM-dd hh:mm:ss"):null);
		GovdocStatUtil.setGovdocStatSetVO(conditionVo, govdocStatSetDao.get(statId));
		
		int overTime = GovdocStatListTypeEnum.getEnumByKey(listType).overTime();
		//穿透到超期链接按特殊方式统计，穿透到非超期链接按一般的统计方式
		if(overTime == 1) {
			//查询的id不需要过滤超期的公文数据
			int newListType = listType;
			if(listType == GovdocStatListTypeEnum.fawenCoverTime.key()) {
				newListType = GovdocStatListTypeEnum.fawenAll.key();
			} else if(listType == GovdocStatListTypeEnum.shouwenCoverTime.key()) {
				newListType = GovdocStatListTypeEnum.shouwenAll.key();
			} else if(listType == GovdocStatListTypeEnum.govdocCoverTime.key()) {
				newListType = GovdocStatListTypeEnum.govdocAll.key();
			}
			List<Object[]> result = govdocStatDao.findGovdocStatResult(newListType, conditionVo);
	    	if(Strings.isNotEmpty(result)) {
	    		List<Long> summaryIdList = new ArrayList<Long>();
	    		for(Object[] objects : result) {
	    			summaryIdList.add((Long)objects[1]);
	    		}
	    		conditionVo.setSummaryIdList(summaryIdList);
	    	}
		}
		List<Object[]> resultList = govdocStatDao.statToListGovdoc(flipInfo, listType, conditionVo);
		
		if(Strings.isNotEmpty(resultList)) {
			List<EdocWorkStatDetialVO> edocVoList = this.convertToListVO(resultList, conditionMap);
			if(flipInfo == null) {
				flipInfo = new FlipInfo();
			}
			flipInfo.setData(edocVoList);
		}
		return flipInfo;
	
	}
	
	/**
	 * 分析返回结果得到VO列表
	 * @param result
	 * @param listType
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private List<EdocWorkStatDetialVO> convertToListVO(List<Object[]> result, Map<String, String> conditionMap) throws Exception {
		String displayName = conditionMap.get("displayName");
		String displayType = conditionMap.get("displayType");
		String displayId = conditionMap.get("displayId");
		
		int listType = Integer.parseInt(conditionMap.get("listType"));
		int state = GovdocStatListTypeEnum.getEnumByKey(listType).state();
		int overTime = GovdocStatListTypeEnum.getEnumByKey(listType).overTime();
		
		Date nowDate = new Date();
		
		User user = AppContext.getCurrentUser();
		List<EdocWorkStatDetialVO> edocVoList = new ArrayList<EdocWorkStatDetialVO>();
		if(Strings.isNotEmpty(result)) {
			List<Long> summaryIdList = new ArrayList<Long>();
			for(Object[] object : result) {
				summaryIdList.add((Long)object[0]);
			}
			
			List<Object[]> largeObjects = govdocSummaryManager.getLargeFieldSummaryList(summaryIdList);
			
			List<CtpAffair> affairList = null;
			Map<Long, List<CtpAffair>> affairMap = new HashMap<Long, List<CtpAffair>>();

			if(Strings.isNotEmpty(summaryIdList)) {
				List<Integer> stateList = new ArrayList<Integer>();
				stateList.add(3);
				stateList.add(4);
				Map<String,Object> conditions = new HashMap<String, Object>();
				conditions.put("objectId", summaryIdList);
				conditions.put("state", stateList);
				affairList = affairManager.getByConditions(null, conditions);
				if(Strings.isNotEmpty(affairList)) {
					for(CtpAffair bean : affairList) {
						if(affairMap.get(bean.getObjectId()) == null) {
							affairMap.put(bean.getObjectId(), new ArrayList<CtpAffair>());
						}
						affairMap.get(bean.getObjectId()).add(bean);
					}
				}
			}
			
			Long orgAccountId = 0L;
			for(Object[] object : result) {
				Long summaryId = (Long)object[0];
				EdocWorkStatDetialVO listVo = new EdocWorkStatDetialVO();
				listVo.setListValue(object);
				for(Object[] largeObject : largeObjects) {
					if(listVo.getSummaryId().longValue() == ((Long)largeObject[0]).longValue()) {
						listVo.setSendDepartment((String)largeObject[1]);
						listVo.setSendUnit((String)largeObject[2]);
						listVo.setCurrentNodesInfo((String)largeObject[3]);
						break;
					}
				}
				listVo.setSubject(govdocFormManager.makeSubject(listVo.getSummaryId()));
				if("Member".equals(displayType)) {
					V3xOrgMember member = orgManager.getMemberById(Long.parseLong(displayId));
					if(member != null) {
						V3xOrgDepartment dept = orgManager.getDepartmentById(member.getOrgDepartmentId());
						listVo.setOperDept(dept == null ? "" : dept.getName());
					}
				} else {
					listVo.setOperDept(displayName);	
				}
				
				//拟稿人
				Map<Long, String> members = Collections.EMPTY_MAP;
				String memberName = members.get(listVo.getStartUserId());
				if(memberName == null){
				    memberName = GovdocHelper.getMemberName(listVo.getStartUserId());
				}
				//V3xOrgMember member = orgManager.getMemberById(listVo.getStartUserId());
				listVo.setStartUserName(memberName);
				
				//归档目录
				if(listVo.getHasArchive() && listVo.getArchiveId() != null) {
					DocResourceBO resourceBO = docApi.getDocResource(listVo.getArchiveId());
					if(resourceBO != null){
						listVo.setArchiveName(docApi.getPhysicalPath(resourceBO.getLogicalPath(), "\\", false, 0));
					}
				}
				
				//超期时长
				String isNull = ResourceUtil.getString("collaboration.project.nothing.label"); //无
				Long deadline = listVo.getDeadline();
	            if(listVo.getDeadlineDatetime()==null) {
	            	listVo.setDeadlineTimeView((deadline == null || deadline == 0) ? isNull : WFComponentUtil.getDeadLineNameForEdoc(deadline, listVo.getStartTime()));
	            } else {
	            	listVo.setDeadlineTimeView(GovdocHelper.getDeadLineName(listVo.getDeadlineDatetime()));
	            }
	            
				affairList = affairMap.get(summaryId);
				//设置流程是否超期标志
				if(overTime == GovdocOverTimeEnum.yes.key()) {
					orgAccountId = GovdocHelper.getFlowPermAccountId(user.getLoginAccount(), listVo.getTempleteId(), listVo.getOrgAccountId(), templateManager);
					GovdocStatUtil.setEdocStatOverTime(listVo, affairMap.get(summaryId), orgAccountId, displayType, displayId, nowDate);
		        }
		        
				//当前待办人
				//如果是已办结,当前代办人为已结束
				if(state == GovdocFinishStateEnum.finished.key()) {
		        	listVo.setCurrentNodesInfo(ResourceUtil.getString("collaboration.list.finished.label"));
		        } else {
		        	listVo.setCurrentNodesInfo(GovdocHelper.parseCurrentNodesInfo(listVo.getCompleteTime(), listVo.getCurrentNodesInfo(), Collections.EMPTY_MAP));
		        }
				
				edocVoList.add(listVo);
			}
		}
		return edocVoList;
	}

	@Override
	public List<V3xOrgDepartment> initWorkStatDeptList(long statId)
			throws Exception {
     List<V3xOrgDepartment> showDepts = new ArrayList<V3xOrgDepartment>();
   	 EdocStatSet statSet = govdocStatSetDao.get(statId);
	 if(statSet != null){
	   	 String deptsIds = statSet.getDeptIds();
	   	 if (Strings.isNotBlank(deptsIds) && !"-1".equals(deptsIds)) {
				String[] s = deptsIds.split(",");
				String dept[] = null;
				for (int i = 0; i < s.length; i++) {
					if (Strings.isNotBlank(s[i])) {
						dept = s[i].trim().split("[|]");
						//如果是单位则要查询其一级部门
						Long accountId = Long.valueOf(dept[1]);
						if("Account".equals(dept[0])){	 						
							List<V3xOrgDepartment> depparts = orgManager.getChildDeptsByAccountId(accountId, true);
							showDepts.addAll(depparts);
						}else{
							V3xOrgDepartment loneDept = this.orgManager.getDepartmentById(accountId);
							showDepts.add(loneDept);
						}
					}
				}
			}
	  }
	  GovdocStatUtil.deptToSort(showDepts);
	  return showDepts;
	}

	
	public GovdocStatSetDao getGovdocStatSetDao() {
		return govdocStatSetDao;
	}


	public void setGovdocStatSetDao(GovdocStatSetDao govdocStatSetDao) {
		this.govdocStatSetDao = govdocStatSetDao;
	}


	public GovdocStatDao getGovdocStatDao() {
		return govdocStatDao;
	}


	/**
	 * 新公文签收统计
	 * @param conditionVo
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<GovdocStatDisplayVO> findGovdocStatSignResult(GovdocStatConditionVO conditionVo) throws Exception {

		List<GovdocStatDisplayVO> statVoList = new ArrayList<GovdocStatDisplayVO>();
		
		User user = AppContext.getCurrentUser();
		
		//按顺序将统计范围分类
		Map<Long, List<Object[]>> recOrgIdMap = new HashMap<Long, List<Object[]>>();
		for(V3xOrgUnit unit : conditionVo.getStatSetVo().getStatRootList()) {
			if(recOrgIdMap.get(unit.getId()) == null) {
    			recOrgIdMap.put(unit.getId(), new ArrayList<Object[]>());
    			
    			String displayName = unit.getName();
    			if(!"Account".equals(unit.getEntityType())) {
    				if(unit.getOrgAccountId().longValue() != user.getLoginAccount().longValue()) {
    					V3xOrgUnit outerAccount = orgManager.getAccountById(unit.getOrgAccountId());
    					if(outerAccount != null) {
    						displayName += "(" + outerAccount.getShortName() + ")";
    					}
    				}
    			}
				
				GovdocStatDisplayVO statVo = new GovdocStatDisplayVO();
				statVo.setDisplayId(String.valueOf(unit.getId()));
				statVo.setDisplayName(displayName);
				statVoList.add(statVo);
			}
		}

		//查询所有统计范围签收数据
		List<Object[]> result = govdocStatDao.findGovdocStatSignResult(GovdocStatListTypeEnum.qianshouSend.key(), conditionVo);
		if(Strings.isNotEmpty(result)) {
			for(Object[] objects : result) {
				Long recOrgId = (Long)objects[1];
				if(recOrgIdMap.get(recOrgId) != null) {
					recOrgIdMap.get(recOrgId).add(objects);
				}
			}
		}

		List<Object[]> SendBackCount = govdocStatDao.findGovdocStatSendBackCount(conditionVo);
		Map<Long, Integer> SendBackCountMap = new HashMap<Long, Integer>();
		for (Object[] objects : SendBackCount) {
			Long recOrgId = (Long)objects[0];
			if (SendBackCountMap.keySet().contains(recOrgId)) {
				 SendBackCountMap.put(recOrgId, SendBackCountMap.get(recOrgId) + 1);
			}else{
				SendBackCountMap.put(recOrgId, 1);
			}
		}
		
		
		//将某统计范围的签收数据按工作时间匹配
		for(GovdocStatDisplayVO statVo : statVoList) {
			Long recOrgId = Long.parseLong(statVo.getDisplayId());
			List<Object[]> recOrgList = recOrgIdMap.get(recOrgId);
			if (SendBackCountMap.keySet().contains(recOrgId)) {
				statVo.setBackCount(SendBackCountMap.get(recOrgId));
			}else{
				statVo.setBackCount(0);
			}
			initSignResultVO(statVo,recOrgList,conditionVo);
		}		
		return statVoList;
	
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> findGovdocStatSendBackCount(GovdocStatConditionVO conditionVo) {

		Map<String, Object> parameterMap = new HashMap<String, Object>();
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("select detail.recOrgId, log.id");
		buffer.append(" from GovdocExchangeMain main");
		buffer.append(",GovdocExchangeDetail detail");
		buffer.append(",GovdocExchangeDetailLog log");
		buffer.append(",EdocSummary summary");
		buffer.append(" where summary.id=main.summaryId and detail.mainId=main.id and detail.id = log.detailId");
		buffer.append(" and summary.state in (0,1,2,3)");
		buffer.append(" and main.type <> 1");
		buffer.append(" and ((summary.govdocType!=0) or (summary.govdocType=0 and summary.state in (0,1,3)))");
		buffer.append(" and log.status = 10");
		buffer.append(" and main.startTime is not null");
		
		//buffer.append(" and detail.sendAccountId = :orgAccountId");
		//parameterMap.put("orgAccountId", String.valueOf(conditionVo.getStatRangeId()));
		if("v3x_edoc_sign_count".equals(conditionVo.getStatType())){//只有本单位发文各单位/部门签收情况才会带发送方条件
			buffer.append(" and summary.orgAccountId = :orgAccountId");
			parameterMap.put("orgAccountId", conditionVo.getStatRangeId());
		}
		
		List<Long> statRangeList = conditionVo.getStatSetVo().getStatRangeList();
		if(Strings.isNotEmpty(statRangeList)) {
			buffer.append(" and (");
			for(int i=0; i<statRangeList.size(); i++) {
				if(i != 0) {
					buffer.append(" or");
				}
				buffer.append(" detail.recOrgId = :recOrgId" + i);
				parameterMap.put("recOrgId" + i, String.valueOf(statRangeList.get(i)));
			}
			buffer.append(")");
		}
		
		if(conditionVo.getStartDate() != null) {
			buffer.append(" and main.startTime >= :startDate");
			parameterMap.put("startDate", conditionVo.getStartDate());
		}
		if(conditionVo.getEndDate() != null) {
			buffer.append(" and main.startTime <= :endDate");
			parameterMap.put("endDate", conditionVo.getEndDate());
		}
		return (List<Object[]>)DBAgent.find(buffer.toString(), parameterMap);
	}
	
	/**
	 * 生成签收统计和退文统计VO对象
	 * @param statVo
	 * @param recOrgList
	 * @param conditionVo
	 * @throws Exception
	 */
	private void initSignResultVO(GovdocStatDisplayVO statVo,List<Object[]> recOrgList,GovdocStatConditionVO conditionVo) throws Exception{		
		List<Long> summaryIdTwoList = new ArrayList<Long>();
		List<Long> summaryIdThreeList = new ArrayList<Long>();
		List<Long> summaryIdFiveList = new ArrayList<Long>();
		List<Long> summaryIdNoRecList = new ArrayList<Long>();
		List<Long> backList = new ArrayList<Long>();
		if(Strings.isNotEmpty(recOrgList)) {
			for(Object[] objects : recOrgList) {
				Long summaryId = (Long)objects[0];
				Date sendTime = (Date)objects[2];
				Date recTime = (Date)objects[3];
				Integer status = (Integer)objects[4];
				if(recTime != null && status>1 && status !=10) {
					Long expendTime = workTimeManager.getDealWithTimeValue(sendTime, recTime, conditionVo.getStatRangeId());
					if(expendTime > 0) {
						expendTime = expendTime/60/1000;
						long day = GovdocStatUtil.getWorkTimeDay(Integer.parseInt(expendTime.toString()), true);
						if(day <= 2) {
							summaryIdTwoList.add(summaryId);	
						} else if(day>=3 && day<=5) {
							summaryIdThreeList.add(summaryId);
						} else if(day > 5) {
							summaryIdFiveList.add(summaryId);
						}
					} else {
						summaryIdTwoList.add(summaryId);
					}
				} else if(status == 10){
					backList.add(summaryId);
				} else {
					summaryIdNoRecList.add(summaryId);
				}
			}
		}
		statVo.setTwoSign(summaryIdTwoList.size());
		statVo.setThreeSign(summaryIdThreeList.size());
		statVo.setFiveSign(summaryIdFiveList.size());
		statVo.setNoRecSignNum(summaryIdNoRecList.size());
		//statVo.setBackCount(backList.size());
		statVo.setAllNum(statVo.getTwoSign() + statVo.getThreeSign() + statVo.getFiveSign() + statVo.getNoRecSignNum()+statVo.getBackCount());
		
		if(statVo.getBackCount() > 0){
			String backPer = NumberFormat.getInstance().format(((float)statVo.getBackCount()/(float)statVo.getAllNum())*100)+"%";
			statVo.setBackSignPer(backPer);
		}
		if(statVo.getTwoSign() > 0) {
			String twoSignPer = NumberFormat.getInstance().format(((float)statVo.getTwoSign()/(float)statVo.getAllNum())*100)+"%";
			statVo.setTwoSignPer(twoSignPer);
		}
		
		if(statVo.getThreeSign() > 0) {
			String threeSignPer = NumberFormat.getInstance().format(((float)statVo.getThreeSign()/(float)statVo.getAllNum())*100)+"%";
			statVo.setThreeSignPer(threeSignPer);
		}
		
		if(statVo.getFiveSign() > 0) {
			String fiveSignPer = NumberFormat.getInstance().format(((float)statVo.getFiveSign()/(float)statVo.getAllNum())*100)+"%";
			statVo.setFiveSignPer(fiveSignPer);
		}
		
		if(statVo.getNoRecSignNum() > 0) {
			String noRecSignNumPer = NumberFormat.getInstance().format(((float)statVo.getNoRecSignNum()/(float)statVo.getAllNum())*100)+"%";
			statVo.setNoRecSignNumPer(noRecSignNumPer);
		}
	}
	
	
	/**
	* 新公文工作统计
	* @param listType
	* @param conditionVo
	* @return
	* @throws Exception
	*/
	@Override
	public List<GovdocStatDisplayVO> findGovdocStatResult(GovdocStatConditionVO conditionVo) throws Exception {
    	Map<String, GovdocStatDisplayVO> statVoMap = new HashMap<String, GovdocStatDisplayVO>();
    	List<GovdocStatDisplayVO> statVoList = new ArrayList<GovdocStatDisplayVO>();
    	GovdocStatDisplayVO statVo = null;
    	
    	//统计某一部门，显示人员
    	if("Department".equals(conditionVo.getStatRangeType())) {
    		List<V3xOrgMember> memberList = orgManager.getMembersByDepartment(conditionVo.getStatRangeId(), false);
    		for(V3xOrgMember member : memberList) {
    			statVo = new GovdocStatDisplayVO();
				statVo.setDisplayId(String.valueOf(member.getId()));
				statVo.setDisplayType("Member");
				statVo.setDisplayName(member.getName());
				statVoMap.put(statVo.getDisplayId(), statVo);
				statVoList.add(statVo);
    		}
    	} else {//统计全部，显示部门
    		if(conditionVo.getStatSetVo()==null || Strings.isEmpty(conditionVo.getStatSetVo().getStatRootList())) {
    			return new ArrayList<GovdocStatDisplayVO>();
    		}
    		
    		//统计本单位时，显示所有部门总计
    		if(conditionVo.getStatSetVo().isSfShowZj()){
    			statVo = new GovdocStatDisplayVO();
    			statVo.setSfClickOn(false);
    			statVo.setDisplayId(String.valueOf(conditionVo.getStatRangeId()));
				statVo.setDisplayType("Account");
				statVo.setDisplayName("合计");
				statVoMap.put(statVo.getDisplayId(), statVo);
				statVoList.add(statVo);
    		}
    		for(V3xOrgUnit unit : conditionVo.getStatSetVo().getStatRootList()) {
				statVo = new GovdocStatDisplayVO();
				statVo.setDisplayId(String.valueOf(unit.getId()));
				statVo.setDisplayType(unit.getEntityType());
				statVo.setDisplayName(unit.getName());
				statVoMap.put(statVo.getDisplayId(), statVo);
				statVoList.add(statVo);
    		}
    	}

    	if(Strings.isEmpty(statVoList)) {
    		return new ArrayList<GovdocStatDisplayVO>();
    	}
    	
    	/** 获取公文数summaryId */
    	List<Long> summaryIdList = new ArrayList<Long>();
    	
    	/** 统计发文办理中 */
    	List<Object[]> resultList = (List<Object[]>)govdocStatDao.findGovdocStatResult(GovdocStatListTypeEnum.fawenPending.key(), conditionVo);
    	Map<String, List<Long>> fawenPendingMap = setStatDisplayVoCount(GovdocStatListTypeEnum.fawenPending.key(), conditionVo, resultList, statVoMap, summaryIdList);
    	
    	/** 统计发文已办理 */
    	resultList = (List<Object[]>)govdocStatDao.findGovdocStatResult(GovdocStatListTypeEnum.fawenFinished.key(), conditionVo);
    	Map<String, List<Long>> fawenDoneMap = setStatDisplayVoCount(GovdocStatListTypeEnum.fawenFinished.key(), conditionVo, resultList, statVoMap, summaryIdList);
    	
    	
    	if(Strings.isNotEmpty(summaryIdList)) {
	    	conditionVo.setSummaryIdList(summaryIdList);
	    	
//	    	/** 获取所有公文字数 */
//	    	resultList = govdocStatDao.findFontSizeBySummaryId(summaryIdList);
//	    	
//	    	/** 将公文id与是否超期映射起来 */
//	    	Map<Long, Long> sizeMap = new HashMap<Long, Long>();
//	    	if(Strings.isNotEmpty(resultList)) {
//		    	for(Object[] result : resultList) {
//	    			Long summaryId = (Long)result[0];
//	    			if(result[1] != null) {
//	    				sizeMap.put(summaryId, (Long)result[1]);
//	    			}
//	    		}
//		    	if(sizeMap.size() > 0) {
//			    	if(fawenPendingMap.size() > 0) {
//			    		setStatDisplayVoFontSize(GovdocStatListTypeEnum.fawenPending.key(), conditionVo, resultList, statVoMap, sizeMap, fawenPendingMap);
//			    	}
//			    	if(fawenDoneMap.size() > 0) {
//			    		setStatDisplayVoFontSize(GovdocStatListTypeEnum.fawenFinished.key(), conditionVo, resultList, statVoMap, sizeMap, fawenDoneMap);
//			    	}
//		    	}
//		    }
    	}
    	
    	/** 统计收文办理中 */
    	resultList = (List<Object[]>)govdocStatDao.findGovdocStatResult(GovdocStatListTypeEnum.shouwenPending.key(), conditionVo);
    	Map<String, List<Long>> shouwenPendingMap = setStatDisplayVoCount(GovdocStatListTypeEnum.shouwenPending.key(), conditionVo, resultList, statVoMap, summaryIdList);
    	
    	/** 统计收文已办结 */
    	resultList = (List<Object[]>)govdocStatDao.findGovdocStatResult(GovdocStatListTypeEnum.shouwenFinished.key(), conditionVo);
    	Map<String, List<Long>> shouwenDoneMap = setStatDisplayVoCount(GovdocStatListTypeEnum.shouwenFinished.key(), conditionVo, resultList, statVoMap, summaryIdList);
    	
    	if(Strings.isNotEmpty(summaryIdList)) {
	    	conditionVo.setSummaryIdList(summaryIdList);
	    	
	    	/** 获取所有公文超期件数 */
	    	resultList = govdocStatDao.findAffairOverList(GovdocStatListTypeEnum.fawenCoverTime.key(), conditionVo);
	    	
	    	/** 将公文id与是否超期映射起来 */
	    	Map<Long, Long> overMap = new HashMap<Long, Long>();
	    	if(Strings.isNotEmpty(resultList)) {
		    	for(Object[] result : resultList) {
	    			Long summaryId = (Long)result[0];
	    			overMap.put(summaryId, (Long)result[1]);
	    		}
		    	
		    	if(overMap.size() > 0) {
		    		/** 统计发文超期数 */
			    	if(fawenPendingMap.size() > 0) {
			    		setStatDisplayVoOverCount(GovdocStatListTypeEnum.fawenPending.key(), conditionVo, resultList, statVoMap, overMap, fawenPendingMap);
			    	}
			    	if(fawenDoneMap.size() > 0) {
			    		setStatDisplayVoOverCount(GovdocStatListTypeEnum.fawenFinished.key(), conditionVo, resultList, statVoMap, overMap, fawenDoneMap);
			    	}
			    	
			    	/** 统计收文超期数 */
			    	if(shouwenPendingMap.size() > 0) {
			    		setStatDisplayVoOverCount(GovdocStatListTypeEnum.shouwenPending.key(), conditionVo, resultList, statVoMap, overMap, shouwenPendingMap);
			    	}
			    	if(shouwenDoneMap.size() > 0) {
			    		setStatDisplayVoOverCount(GovdocStatListTypeEnum.shouwenFinished.key(), conditionVo, resultList, statVoMap, overMap, shouwenDoneMap);
			    	}
		    	}
	    	}
    	}
    	
    	GovdocStatDisplayVO listVo = null;
    	GovdocStatDisplayVO totalListVo = null;
    	/** 需要统计总和时，将第一条预留给总和 */
    	int index = 0;
    	if(conditionVo.getStatSetVo().isSfShowZj()) {
    		totalListVo = statVoList.get(index);
    		index++;
    	}
    	for(; index<statVoList.size(); index++) {
    		listVo = statVoMap.get(statVoList.get(index).getDisplayId());
    		
    		listVo.setSendCount(listVo.getSendPendingCount() + listVo.getSendDoneCount());
			listVo.setRecCount(listVo.getRecPendingCount() + listVo.getRecDoneCount());
			listVo.setAllOverCount(listVo.getSendOverCount() + listVo.getRecOverCount());
			listVo.setAllPendingCount(listVo.getSendPendingCount() + listVo.getRecPendingCount());
			listVo.setAllDoneCount(listVo.getSendDoneCount() + listVo.getRecDoneCount());
			listVo.setAllCount(listVo.getAllPendingCount() + listVo.getAllDoneCount());
			setStatDisplayVoPer(listVo);
			
			if(totalListVo != null && conditionVo.getStatSetVo().isSfShowZj()) {
				totalListVo.setSendPendingCount(totalListVo.getSendPendingCount() + listVo.getSendPendingCount());
				totalListVo.setSendDoneCount(totalListVo.getSendDoneCount() + listVo.getSendDoneCount());
				totalListVo.setRecPendingCount(totalListVo.getRecPendingCount() + listVo.getRecPendingCount());
				totalListVo.setRecDoneCount(totalListVo.getRecDoneCount() + listVo.getRecDoneCount());
				totalListVo.setSendCount(totalListVo.getSendCount() + listVo.getSendCount());
				totalListVo.setRecCount(totalListVo.getRecCount() + listVo.getRecCount());
				totalListVo.setAllOverCount(totalListVo.getAllOverCount() + listVo.getAllOverCount());
				totalListVo.setAllPendingCount(totalListVo.getAllPendingCount() + listVo.getAllPendingCount());
				totalListVo.setAllDoneCount(totalListVo.getAllDoneCount() + listVo.getAllDoneCount());
				totalListVo.setAllCount(totalListVo.getAllCount() + listVo.getAllCount());
				totalListVo.setSendOverCount(totalListVo.getSendOverCount() + listVo.getSendOverCount());
				totalListVo.setRecOverCount(totalListVo.getRecOverCount() + listVo.getRecOverCount());
				totalListVo.setFontSize(totalListVo.getFontSize() + listVo.getFontSize());
				setStatDisplayVoPer(totalListVo);
			}
    	}
    	
    	return statVoList;
    }
	
	/**
	 * 计算公文字数
	 * @param listType
	 * @param conditionVo
	 * @param resultList
	 * @param statVoMap
	 * @param pendingMap
	 * @param doneMap
	 */
	@SuppressWarnings("unused")
	private void setStatDisplayVoFontSize(int listType, GovdocStatConditionVO conditionVo, List<Object[]> resultList, Map<String, GovdocStatDisplayVO> statVoMap, Map<Long, Long> sizeMap, Map<String, List<Long>> rangeMap) {
		for(String displayId : rangeMap.keySet()) {
			if(statVoMap.get(displayId) != null) {
	    		List<Long> summaryIdList = rangeMap.get(displayId);
	    		//某统计范围下的公文字数
	    		if(Strings.isNotEmpty(summaryIdList)) {
	    			for(Long summaryId : summaryIdList) {
	    				if(sizeMap.get(summaryId) != null && sizeMap.get(summaryId).longValue() > 0) {
    						statVoMap.get(displayId).setFontSize(statVoMap.get(displayId).getFontSize() + sizeMap.get(summaryId).longValue());
	    				}
	    			}
	    		}
			}
    	}
	}
	
	/**
	 * 计算公文超期数
	 * @param listType
	 * @param conditionVo
	 * @param resultList
	 * @param statVoMap
	 * @param pendingMap
	 * @param doneMap
	 */
	private void setStatDisplayVoOverCount(int listType, GovdocStatConditionVO conditionVo, List<Object[]> resultList, Map<String, GovdocStatDisplayVO> statVoMap, Map<Long, Long> overMap, Map<String, List<Long>> rangeMap) {
		int govdocType = GovdocStatListTypeEnum.getEnumByKey(listType).value();
		for(String displayId : rangeMap.keySet()) {
			if(statVoMap.get(displayId) != null) {
	    		List<Long> summaryIdList = rangeMap.get(displayId);
	    		//某统计范围下的公文是否超期
	    		if(Strings.isNotEmpty(summaryIdList)) {
	    			for(Long summaryId : summaryIdList) {
	    				if(overMap.get(summaryId) != null && overMap.get(summaryId).longValue() > 0) {
	    					if(govdocType == 1) {//发文超期数
	    						statVoMap.get(displayId).setSendOverCount(statVoMap.get(displayId).getSendOverCount() + 1);
	    					} else if(govdocType == 2) {//收文超期数
	    						statVoMap.get(displayId).setRecOverCount(statVoMap.get(displayId).getRecOverCount() + 1);
	    					}
	    				}
	    			}
	    		}
			}
    	}
	}
	
	
	/**
	 * 计算公文统计结果百分比
	 * @param listVo
	 */
	private void setStatDisplayVoPer(GovdocStatDisplayVO listVo) {
		if(listVo.getSendCount() != 0) {
			String sendDonePer = NumberFormat.getInstance().format(((float)listVo.getSendDoneCount()/(float)listVo.getSendCount())*100)+"%";
			listVo.setSendDonePer(sendDonePer);//发文办结率
		}
		
		if(listVo.getSendOverCount() != 0) {
			String sendOverPer = NumberFormat.getInstance().format(((float)listVo.getSendOverCount()/(float)listVo.getSendCount())*100)+"%";
			listVo.setSendOverPer(sendOverPer);//发文超期率
		}
		
		if(listVo.getRecCount() != 0) {
			String recCount = NumberFormat.getInstance().format(((float)listVo.getRecDoneCount()/(float)listVo.getRecCount())*100)+"%";
			listVo.setRecDonePer(recCount);//收文办结率
		}
		
		if(listVo.getRecOverCount() != 0) {
			String recOverPer = NumberFormat.getInstance().format(((float)listVo.getRecOverCount()/(float)listVo.getRecCount())*100)+"%";
			listVo.setRecOverPer(recOverPer);//收文超期率
		}
		
		if(listVo.getAllCount() != 0) {
			String allDonePer = NumberFormat.getInstance().format(((float)listVo.getAllDoneCount()/(float)listVo.getAllCount())*100)+"%";
			listVo.setAllDonePer(allDonePer);//总办结率
			
			String allOverPer = NumberFormat.getInstance().format(((float)listVo.getAllOverCount()/(float)listVo.getAllCount())*100)+"%";
			listVo.setAllOverPer(allOverPer);//总超期率
		}
	}
	
	/**
	 * 计算公文统计结果条数
	 * @param listType
	 * @param conditionVo
	 * @param resultList
	 * @param statVoMap
	 * @param summaryIdList
	 */
	private Map<String, List<Long>> setStatDisplayVoCount(int listType, GovdocStatConditionVO conditionVo, List<Object[]> resultList, Map<String, GovdocStatDisplayVO> statVoMap, List<Long> summaryIdList) {
		Map<String, List<Long>> rangeMap = new HashMap<String, List<Long>>();
		if(Strings.isNotEmpty(resultList)) {
    		for(Object[] result : resultList) {
    			Long rangeId = (Long)result[0];
    			Long summaryId = (Long)result[1];
    			
    			String displayId = rangeId.toString();
    			//若统计的是部门，则需要将子部门的数据统计到父级部门
    			if(!GovdocStatTypeEnum.Department.name().equals(conditionVo.getStatRangeType())) {
    				Long rootRangeId = conditionVo.getStatSetVo().getStatRangeMap().get(rangeId);    			
        		    displayId = rootRangeId.toString();
    			}
    			if(rangeMap.get(displayId) == null) {
    				rangeMap.put(displayId, new ArrayList<Long>());
    			}
    			rangeMap.get(displayId).add(summaryId);
    			
    			//部门对应的公文数，用于统计公文字数
    			summaryIdList.add(summaryId);
    		}
    		for(String displayId : rangeMap.keySet()) {
    			if(statVoMap.get(displayId) != null) {
    				int newCount = rangeMap.get(displayId).size();
    				//发文办理中
    				if(listType == GovdocStatEnum.GovdocStatListTypeEnum.fawenPending.key()) {
    					statVoMap.get(displayId).setSendPendingCount(statVoMap.get(displayId).getSendPendingCount() + newCount);
    				} else if(listType == GovdocStatEnum.GovdocStatListTypeEnum.fawenFinished.key()) {
    					statVoMap.get(displayId).setSendDoneCount(statVoMap.get(displayId).getSendDoneCount() + newCount);
    				} else if(listType == GovdocStatEnum.GovdocStatListTypeEnum.shouwenPending.key()) {
    					statVoMap.get(displayId).setRecPendingCount(statVoMap.get(displayId).getRecPendingCount() + newCount);
    				} else if(listType == GovdocStatEnum.GovdocStatListTypeEnum.shouwenFinished.key()) {
    					statVoMap.get(displayId).setRecDoneCount(statVoMap.get(displayId).getRecDoneCount() + newCount);
    				}  
    			}
    		}
    	}
		return rangeMap;
	}
	
	/**
	 * 获取公文统计数据
	 * @param conditionMap
	 * @return
	 * @throws BusinessException
	 */
	@AjaxAccess
	@SuppressWarnings("unchecked")
	public FlipInfo getGovdocStatVoList(FlipInfo flipInfo, Map<String, String> conditionMap) throws BusinessException {
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
		
		if(Strings.isNotBlank(conditionMap.get("rangeIds"))) {
		    Map<String, Object> statVoMap = getEdocStatVoListView(conditionMap);
			try {
				boolean showBanwenYuewen = GovdocSwitchHelper.showBanwenYuewen(user.getLoginAccount());
				conditionMap.put("showBanwenYuewen", String.valueOf(showBanwenYuewen));
				//LOGGER.info("获取列头_________________:"+(System.currentTimeMillis() - t_start));
				
				conditionMap.put("resultType", String.valueOf(EdocStatResultTypeEnum.pendingAndDone.key()));
				setEdocStatCount_subQuery(flipInfo, conditionMap, statVoMap);
				//LOGGER.info("待办已办____:"+(System.currentTimeMillis() - t_m));
				
				if(edocType == 1) {//收文
					if(showBanwenYuewen) {						
						conditionMap.put("resultType", String.valueOf(EdocStatResultTypeEnum.readingAndReaded.key()));
						setEdocStatCount_subQuery(flipInfo, conditionMap, statVoMap);
						//LOGGER.info("待阅已阅____:"+(System.currentTimeMillis() - t_m));
						
						conditionMap.put("resultType", String.valueOf(EdocStatResultTypeEnum.readAll.key()));
						setEdocStatCount_subQuery(flipInfo, conditionMap, statVoMap);
						//LOGGER.info("总阅件____:"+(System.currentTimeMillis() - t_m));
					}
				}

				//总经办/未办结/已办结
				conditionMap.put("resultType", String.valueOf(EdocStatResultTypeEnum.doAll.key()));
				setEdocStatCount_subQuery(flipInfo, conditionMap, statVoMap);
				//LOGGER.info("总经办/未办结/已办结____:"+(System.currentTimeMillis() - t_m));
				
				//计算已发数据（添加部门下兼职/副岗已发数据）
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
		    List<Object[]> result = govdocStatDao.findEdocStatResultBySql(flipInfo, conditionMap);
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
								statVo.setCountFinish(statVo.getCountFinish() + resultVo.getDisplayRowCount());
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
				result = govdocStatDao.findEdocStatResultBySql_Sent(flipInfo, conditionMap);
			} else {
				result = govdocStatDao.findEdocStatResultBySql_DoAndRead(flipInfo, conditionMap);
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
		boolean showBanwenYuewen = GovdocSwitchHelper.showBanwenYuewen(user.getLoginAccount());
		Map<String, Long> statAffairMap = new HashMap<String, Long>();
		List<EdocStatVO> statVoList = (List<EdocStatVO>)statVoMap.get("statVoList");
		long t_m = System.currentTimeMillis();
		List<Object[]> result = govdocStatDao.findEdocStatResultByHql(flipInfo, conditionMap);
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
	@AjaxAccess
	public FlipInfo getEdocVoList(FlipInfo flipInfo, Map<String, String> conditionMap) throws BusinessException {
		long t_start = System.currentTimeMillis();
		User user = AppContext.getCurrentUser();
		int listType = Integer.parseInt(conditionMap.get("listType"));
		setConditionOperationTypeIds(conditionMap);
		boolean showBanwenYuewen = GovdocSwitchHelper	.showBanwenYuewen(user.getLoginAccount());
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
		List<Object[]> result = govdocStatDao.findStatEdocList(flipInfo, conditionMap);
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
			List<Object[]> longtextFields = govdocStatDao.getEdocLongtextFields(summaryIdList);
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
	                archiveName =  govdocPubManager.getShowArchiveNameByArchiveId(listVo.getArchiveId());
	                fullArchiveName = govdocPubManager.getFullArchiveNameByArchiveId(listVo.getArchiveId());
				}
				listVo.setArchiveName(archiveName);
				listVo.setArchiveAllName(fullArchiveName);
				//超期时长
				String isNull = ResourceUtil.getString("collaboration.project.nothing.label"); //无
				Long deadline = listVo.getDeadline();
	            if(listVo.getDeadlineDatetime()==null) {
	            	listVo.setDeadlineTimeView((deadline == null || deadline == 0) ? isNull : WFComponentUtil.getDeadLineNameForEdoc(deadline, listVo.getStartTime()));
	            } else {
	            	listVo.setDeadlineTimeView(GovdocHelper.getDeadLineName(listVo.getDeadlineDatetime()));
	            }
				//设置流程是否超期标志
				Date finishDate = listVo.getCompleteTime();
				Date now = new Date(System.currentTimeMillis());
				Long expendTime = 0L;
				if(listVo.getDeadlineDatetime() != null) {
					orgAccountId = GovdocHelper.getFlowPermAccountId(user.getLoginAccount(), listVo.getTempleteId(), listVo.getOrgAccountId(), templateManager);
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
				listVo.setCurrentNodesInfo(GovdocHelper.parseCurrentNodesInfo(listVo.getCompleteTime(), listVo.getCurrentNodesInfo(), members));
				//公文级别
				if(Strings.isNotBlank(listVo.getUnitLevel())) {
					String unitLevelName = "";
					EdocElement element = govdocElementManager.getByFieldName("unit_level", user.getLoginAccount());
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
	@AjaxAccess
	public FlipInfo getEdocEnumitemList(FlipInfo flipInfo, Map<String, String> conditionMap) throws BusinessException {
		String fieldName = conditionMap.get("fieldName");
		User user = AppContext.getCurrentUser();
		EdocElement element = govdocElementManager.getByFieldName(fieldName, user.getLoginAccount());
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
		//boolean isCurrentAccountExchange = GovdocRoleHelper.isAccountExchange();
		boolean isCurrentAccountExchange = true;
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
		    	List<V3xOrgMember> allMember = orgManager.getAllMembersWithOuter(V3xOrgEntity.VIRTUAL_ACCOUNT_ID);
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

    /**
     * 保存或更新公文统计数据
     * @param summary
     * @throws BusinessException
     */
    public void saveOrUpdateEdocStat(EdocSummary summary, User user) throws BusinessException {
    	User currUser = AppContext.getCurrentUser();
    	if(currUser == null) {
    		currUser = user;
    	}
    	if(summary.getState() == EdocConstant.flowState.run.ordinal()
    			|| summary.getState() == EdocConstant.flowState.finish.ordinal()
    			|| summary.getState() == EdocConstant.flowState.terminate.ordinal()) {
    		EdocStat edocStat = null;
    		if(summary.getExtraDataContainer().get(GovdocHelper._IsNewGovdoc) == null){
    			edocStat = edocStatManager.getEdocStatByEdocId(summary.getId());
    		}
    		try {
    			if(edocStat != null) {
    				int state = summary.getState();
    				edocStatManager.updateFlowState(summary.getId(),state); //防止重复插入数据,处理时更新统计状态
    			} else {
    				edocStatManager.createState(summary,currUser);
    			}
    		} catch (Exception e) {
    			LOGGER.error("保存公文统计数据时出错！", e);
    			throw new BusinessException(e);
    		}
    	}	
    }
    
	public void deleteEdocStat(Long summaryId)throws Exception {
		edocStatManager.deleteEdocStat(summaryId);
	}

	/**
	 * 新公文退文统计
	 * @param conditionVo
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<GovdocStatDisplayVO> findGovdocStatSignSelfResult(GovdocStatConditionVO conditionVo) throws Exception {
		List<GovdocStatDisplayVO> statVoList = new ArrayList<GovdocStatDisplayVO>();
		User user = AppContext.getCurrentUser();
		//按顺序将统计范围分类
		Map<Long, List<Object[]>> recOrgIdMap = new HashMap<Long, List<Object[]>>();
		for(V3xOrgUnit unit : conditionVo.getStatSetVo().getStatRootList()) {
			if(recOrgIdMap.get(unit.getId()) == null) {
    			recOrgIdMap.put(unit.getId(), new ArrayList<Object[]>());
    			
    			String displayName = unit.getName();
    			if(!"Account".equals(unit.getEntityType())) {
    				if(unit.getOrgAccountId().longValue() != user.getLoginAccount().longValue()) {
    					V3xOrgUnit outerAccount = orgManager.getAccountById(unit.getOrgAccountId());
    					if(outerAccount != null) {
    						displayName += "(" + outerAccount.getShortName() + ")";
    					}
    				}
    			}
				
				GovdocStatDisplayVO statVo = new GovdocStatDisplayVO();
				statVo.setDisplayId(String.valueOf(unit.getId()));
				statVo.setDisplayName(displayName);
				statVoList.add(statVo);
			}
		}
		List<Object[]> result = govdocStatDao.findGovdocStatSignResult(GovdocStatListTypeEnum.qianshouSend.key(), conditionVo);
		Integer backCount = govdocStatDao.findGovdocStatBackCount(GovdocStatListTypeEnum.qianshouSend.key(), conditionVo);
		//将某统计范围的签收数据按工作时间匹配
		for(GovdocStatDisplayVO statVo : statVoList) {
			statVo.setBackCount(backCount);
			initSignResultVO(statVo,result,conditionVo);
		}		
		return statVoList;
	}

	public void setGovdocStatDao(GovdocStatDao govdocStatDao) {
		this.govdocStatDao = govdocStatDao;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public void setGovdocElementManager(GovdocElementManager govdocElementManager) {
		this.govdocElementManager = govdocElementManager;
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
	public void setEdocStatManager(EdocStatManager edocStatManager) {
		this.edocStatManager = edocStatManager;
	}
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public void setDocApi(DocApi docApi) {
		this.docApi = docApi;
	}
	public void setGovdocFormManager(GovdocFormManager govdocFormManager) {
		this.govdocFormManager = govdocFormManager;
	}
	public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}
	public void setGovdocPubManager(GovdocPubManager govdocPubManager) {
		this.govdocPubManager = govdocPubManager;
	}
	
}
