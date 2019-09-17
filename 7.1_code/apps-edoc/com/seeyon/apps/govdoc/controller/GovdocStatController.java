package com.seeyon.apps.govdoc.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.helper.GovdocRoleHelper;
import com.seeyon.apps.govdoc.helper.GovdocSwitchHelper;
import com.seeyon.apps.govdoc.manager.GovdocElementManager;
import com.seeyon.apps.govdoc.manager.GovdocStatManager;
import com.seeyon.apps.govdoc.manager.GovdocStatPushManager;
import com.seeyon.apps.govdoc.manager.GovdocStatSetManager;
import com.seeyon.apps.govdoc.mark.manager.GovdocMarkManager;
import com.seeyon.apps.govdoc.po.EdocStatSet;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.excel.DataRecord;
import com.seeyon.ctp.common.excel.DataRow;
import com.seeyon.ctp.common.excel.FileToExcelManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgUnit;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.domain.EdocStatCondition;
import com.seeyon.v3x.edoc.manager.EdocStatManager;
import com.seeyon.v3x.edoc.util.EdocStatEnum.EdocStatRoleTypeEnum;
import com.seeyon.v3x.edoc.util.EdocStatHelper;
import com.seeyon.v3x.edoc.util.GovdocStatEnum.GovdocOverTimeEnum;
import com.seeyon.v3x.edoc.util.GovdocStatEnum.GovdocStatListTypeEnum;
import com.seeyon.v3x.edoc.util.GovdocStatEnum.GovdocStatTypeEnum;
import com.seeyon.v3x.edoc.util.GovdocStatEnum.StatEdocTypeEnum;
import com.seeyon.v3x.edoc.util.GovdocStatUtil;
import com.seeyon.v3x.edoc.webmodel.EdocMarkModel;
import com.seeyon.v3x.edoc.webmodel.EdocStatListVO;
import com.seeyon.v3x.edoc.webmodel.EdocStatVO;
import com.seeyon.v3x.edoc.webmodel.EdocWorkStatDetialVO;
import com.seeyon.v3x.edoc.webmodel.GovdocStatConditionVO;
import com.seeyon.v3x.edoc.webmodel.GovdocStatDisplayVO;
import com.seeyon.v3x.edoc.webmodel.WebSignCount;

/**
 * 新公文统计控制器
 * @author 唐桂林
 *
 */
public class GovdocStatController extends BaseController {

	private static final Log LOGGER = CtpLogFactory.getLog(GovdocStatController.class);
	
	private GovdocStatManager govdocStatManager;
	private GovdocStatSetManager govdocStatSetManager;
	private GovdocStatPushManager govdocStatPushManager;
	private GovdocMarkManager govdocMarkManager;
	private EdocStatManager edocStatManager;
	private OrgManager orgManager;
	private PermissionManager permissionManager;
	private FileToExcelManager fileToExcelManager;
	
	private static Map<String,Map<String,String>> seasonMap = new HashMap<String, Map<String,String>>();
	
	static {
		Map<String,String> map1 = new HashMap<String, String>();
		map1.put("start", "1");
		map1.put("end", "3");
		seasonMap.put("1", map1);
		Map<String,String> map2 = new HashMap<String, String>();
		map2.put("start", "4");
		map2.put("end", "6");
		seasonMap.put("2", map2);
		Map<String,String> map3 = new HashMap<String, String>();
		map3.put("start", "7");
		map3.put("end", "9");
		seasonMap.put("3", map3);
		Map<String,String> map4 = new HashMap<String, String>();
		map4.put("start", "10");
		map4.put("end", "12");
		seasonMap.put("4", map4);
	}
	
	/**
	 * 签收统计-统计说明
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView statSignDesc(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String statType = request.getParameter("statType");
		ModelAndView mav = new ModelAndView("govdoc/stat/statsign_desc");
		mav.addObject("statType", statType);
		return mav;
	}
	
	/**
	 *工作统计统计说明
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView showInstruction(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("edoc/docstat/edoc_stat_workcount_instruction");
		return mav;
	}
	
	/**
	 *工作统计统计说明
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView statWorkDesc(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/stat/statwork_desc");
		return mav;
	}
	
	/**
	 * 签收/工作-统计页面
	 * @param request
	 * @param response
	 * @return
	 */
	public ModelAndView statCondition(HttpServletRequest request,HttpServletResponse response) throws Exception{
		 ModelAndView mav = new ModelAndView("govdoc/stat/statwork_condition");
		 User user = AppContext.getCurrentUser(); 
		 String statType = request.getParameter("statType");
		 if("work_count".equals(statType)) {
		     List<EdocMarkModel> markModelList = govdocMarkManager.getMarkVoListForStat(user.getLoginAccount());
		     if(Strings.isNotEmpty(markModelList)) {
		    	 List<EdocMarkModel> docMarkModelList = new ArrayList<EdocMarkModel>();
		    	 List<EdocMarkModel> serialNoModelList = new ArrayList<EdocMarkModel>();
		    	 for(EdocMarkModel bean : markModelList) {
		    		 if(bean.getMarkType() == 0) {//公文文号
		    			 docMarkModelList.add(bean);
		    		 } else {//内部文号
		    			 serialNoModelList.add(bean);
		    		 }
		    	 }
		    	 mav.addObject("docMarkModelList", docMarkModelList);
		    	 mav.addObject("serialNoModelList", serialNoModelList);
		     }
		 } else {
			 mav = new ModelAndView("govdoc/stat/statsign_condition");
		 }
	     
	     //验证预制数据
	     this.govdocStatSetManager.checkStatInitData();
	     
	     //查找所有签收统计配置信息
	     List<EdocStatSet> statSetList = govdocStatSetManager.findEdocStatSetByAccount(user.getLoginAccount(), statType);
         for(EdocStatSet e:statSetList)	{
        	 if("签收统计".equals(e.getName())){
        		 e.setName("发文签收统计");
        	 }
        	 if("退文统计".equals(e.getName())) {
				e.setName("收文签收统计");
			}
        	 
         }     
	     mav.addObject("statSetList", statSetList);
	     String[] curDates = GovdocStatUtil.getCurDates();
	     List<Integer> curYears = new ArrayList<Integer>();
	     for(int i=1990;i<=Integer.valueOf(curDates[0]);i++){
	    	 curYears.add(i);
	     }
	     mav.addObject("curDates", curYears);
	     mav.addObject("curYear", Integer.valueOf(curDates[0]));
	     mav.addObject("curMonth", Integer.valueOf(curDates[1]));
	     mav.addObject("curSeason", Integer.valueOf(curDates[2]));	     
	     mav.addObject("curDay", curDates[3]);
	     return mav;
	}
	
	/**
	 * 签收/工作统计-统计结果
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView statResult(HttpServletRequest request,HttpServletResponse response)throws Exception{
		ModelAndView mav = new ModelAndView("govdoc/stat/statwork_result");
		String statType = request.getParameter("statType");
		Long statId = Long.parseLong(request.getParameter("statId"));
		Long statRangeId = request.getParameter("statRangeId")==null ? null: Long.parseLong(request.getParameter("statRangeId"));
		String statRangeType = "";
		String statRangeName = "";
		
		//签收统计
		if(!"work_count".equals(statType)) {
			mav = new ModelAndView("govdoc/stat/statsign_result");
			statRangeId = AppContext.currentAccountId();
			statRangeType = "Account";
			statRangeName = "单位";
		} else {
			if(GovdocStatUtil.checkIdIsNull(statRangeId)) {
				statRangeType = GovdocStatTypeEnum.All.name();
				statRangeName = "部门";
			} else {
				V3xOrgUnit unit = orgManager.getUnitById(statRangeId);
				if(unit != null) {
					statRangeType = unit.getEntityType();
				}
				statRangeName = "人员";
			}
		}
		
		int timeType = Integer.valueOf(request.getParameter("timeType"));
		Date startDate = null;
		Date endDate = null;
		List<Date> dateList = initCheckDate(startDate,endDate,timeType,request);
		if(dateList ==null){
			throw new Exception("查询工作统计数据错误");
		}
		EdocStatSet govdocStatSet = govdocStatSetManager.getEdocStatSet(statId);
		if(govdocStatSet == null) {
			throw new Exception("查询工作统计设置错误");
		}
		GovdocStatConditionVO conditionVo = new GovdocStatConditionVO();
		conditionVo.setStatId(statId);
		conditionVo.setStatType(govdocStatSet.getStatType());
		conditionVo.setStatName(govdocStatSet.getName());
		conditionVo.setStatRangeId(statRangeId);
		conditionVo.setStatRangeType(statRangeType);
		conditionVo.setStatRangeName(statRangeName);
		conditionVo.setDocMark(request.getParameter("docMark_txt"));
		conditionVo.setDocMarkDefId(request.getParameter("docMark"));
		conditionVo.setSerialNo(request.getParameter("serialNo_txt"));
		conditionVo.setSerialNoDefId(request.getParameter("serialNo"));
		conditionVo.setStartDate(dateList.get(0));
		conditionVo.setEndDate(dateList.get(1));
		conditionVo.setStartTime(DateUtil.format(dateList.get(0)));
		conditionVo.setEndTime(DateUtil.format(dateList.get(1)));
		conditionVo.setStatTitle(request.getParameter("statTitle"));
		GovdocStatUtil.setGovdocStatSetVO(conditionVo, govdocStatSet);
		mav.addObject("conditionVo", conditionVo);
		
		if(conditionVo.getStatSetVo() != null && Strings.isNotEmpty(conditionVo.getStatSetVo().getStatRangeList())) {
			List<GovdocStatDisplayVO> statVoList = null;
			try {
				if("work_count".equals(govdocStatSet.getStatType())) {
					statVoList = this.govdocStatManager.findGovdocStatResult(conditionVo);
				} else if("v3x_edoc_sign_count".equals(govdocStatSet.getStatType())){
					statVoList = this.govdocStatManager.findGovdocStatSignResult(conditionVo);
				} else if("v3x_edoc_sign_self_count".equals(govdocStatSet.getStatType())){
					statVoList = this.govdocStatManager.findGovdocStatSignSelfResult(conditionVo);
				}				
			} catch(Exception e) {
				LOGGER.error("新公文统计出错", e);
			}
			
			if(Strings.isNotEmpty(statVoList)) {
				mav.addObject("statVoList", statVoList);
			}
		}
		return mav; 
	}
	
	/**
	 * 签收/工作统计-统计结果-导出EXCEL
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView statResultExport(HttpServletRequest request,HttpServletResponse response) throws Exception{
		String statType = request.getParameter("statType");
		Long statId = Long.parseLong(request.getParameter("statId"));
		Long statRangeId = request.getParameter("statRangeId")==null ? null: Long.parseLong(request.getParameter("statRangeId"));
		String statRangeType = "";
		String statRangeName = "";
		
		//签收统计
		if(!"work_count".equals(statType)) {
			statRangeId = AppContext.currentAccountId();
			statRangeType = "Account";
			statRangeName = "单位";
		} else {
			if(GovdocStatUtil.checkIdIsNull(statRangeId)) {
				statRangeType = GovdocStatTypeEnum.All.name();
				statRangeName = "部门";
			} else {
				V3xOrgUnit unit = orgManager.getUnitById(statRangeId);
				if(unit != null) {
					statRangeType = unit.getEntityType();
				}
			}
		}
		
		int timeType = Integer.valueOf(request.getParameter("timeType"));
		Date startDate = null;
		Date endDate = null;
		List<Date> dateList = initCheckDate(startDate,endDate,timeType,request);
		if(dateList ==null){
			throw new Exception("查询工作统计数据错误");
		}
		EdocStatSet govdocStatSet = govdocStatSetManager.getEdocStatSet(statId);
		if(govdocStatSet == null) {
			throw new Exception("查询工作统计设置错误");
		}
		GovdocStatConditionVO conditionVo = new GovdocStatConditionVO();
		conditionVo.setStatId(statId);
		conditionVo.setStatType(govdocStatSet.getStatType());
		conditionVo.setStatName(govdocStatSet.getName());
		conditionVo.setStatRangeId(statRangeId);
		conditionVo.setStatRangeType(statRangeType);
		conditionVo.setStatRangeName(statRangeName);
		conditionVo.setDocMark(request.getParameter("docMark_txt"));
		conditionVo.setDocMarkDefId(request.getParameter("docMark"));
		conditionVo.setSerialNo(request.getParameter("serialNo_txt"));
		conditionVo.setSerialNoDefId(request.getParameter("serialNo"));
		conditionVo.setStartDate(dateList.get(0));
		conditionVo.setEndDate(dateList.get(1));
		conditionVo.setStartTime(DateUtil.format(dateList.get(0),"yyyy-MM-dd HH:mm:ss"));
		conditionVo.setEndTime(DateUtil.format(dateList.get(1),"yyyy-MM-dd HH:mm:ss"));
		conditionVo.setStatTitle(request.getParameter("statTitle"));
		GovdocStatUtil.setGovdocStatSetVO(conditionVo, govdocStatSet);
		List<GovdocStatDisplayVO> statVoList = null;
		try {
			if("work_count".equals(govdocStatSet.getStatType())) {
				statVoList = this.govdocStatManager.findGovdocStatResult(conditionVo);
			} else if("v3x_edoc_sign_count".equals(govdocStatSet.getStatType())){
				statVoList = this.govdocStatManager.findGovdocStatSignResult(conditionVo);
			} else if("v3x_edoc_sign_self_count".equals(govdocStatSet.getStatType())){
				statVoList = this.govdocStatManager.findGovdocStatSignSelfResult(conditionVo);
			}				
		} catch(Exception e) {
			LOGGER.error("新公文统计出错", e);
		}  
			//导出Excel
		DataRecord dr = GovdocStatUtil.getDataToExcport(conditionVo, statVoList);
		this.fileToExcelManager.save(response, dr.getSheetName(), new DataRecord[] { dr });
        
		return null;
	}
	
	/**
	 * 工作统计-统计结果穿透列表
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView statWorkResultToList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/stat/stat_result_list");
		mav.addObject("isG6Version", GovdocHelper.isG6Version());
		
    	Map<String, String> conditionMap = new HashMap<String, String>();
		conditionMap.put("statId", request.getParameter("statId"));
    	conditionMap.put("statType", request.getParameter("statType"));
    	conditionMap.put("listType", request.getParameter("listType"));
    	conditionMap.put("displayType", request.getParameter("displayType"));
    	conditionMap.put("displayId", request.getParameter("displayId"));
    	conditionMap.put("displayName", request.getParameter("displayName"));
    	conditionMap.put("startTime", request.getParameter("startTime"));
    	conditionMap.put("endTime", request.getParameter("endTime"));
    	conditionMap.put("docMark", request.getParameter("docMark"));
    	conditionMap.put("docMark_txt", request.getParameter("docMark_txt"));
    	conditionMap.put("serialNo", request.getParameter("serialNo"));
    	conditionMap.put("serialNo_txt", request.getParameter("serialNo_txt"));
    	FlipInfo fi = govdocStatManager.statToListGovdoc(new FlipInfo(), conditionMap);
    	request.setAttribute("ffedocStatListRec", fi);
		return mav;
	}

	/**
	 * 工作统计-统计结果穿透列表-导出EXCEL
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ModelAndView statWorkResultToListExport(HttpServletRequest request, HttpServletResponse response) throws Exception {
		int listType = Integer.parseInt(request.getParameter("listType"));
		int govdocType = GovdocStatListTypeEnum.getEnumByKey(listType).value();
		int overTime = GovdocStatListTypeEnum.getEnumByKey(listType).overTime();
		
		Map<String, String> conditionMap = new HashMap<String, String>();
		conditionMap.put("statId", request.getParameter("statId"));
		conditionMap.put("statType", request.getParameter("statType"));
		conditionMap.put("listType", request.getParameter("listType"));
		conditionMap.put("displayType", request.getParameter("displayType"));
		conditionMap.put("displayId", request.getParameter("displayId"));
		conditionMap.put("startTime", request.getParameter("startTime"));
		conditionMap.put("endTime", request.getParameter("endTime"));
		conditionMap.put("docMark", request.getParameter("docMark"));
		conditionMap.put("docMark_txt", request.getParameter("docMark_txt"));
		conditionMap.put("serialNo", request.getParameter("serialNo"));
		conditionMap.put("serialNo_txt", request.getParameter("serialNo_txt"));
		conditionMap.put("displayName", request.getParameter("displayName"));		
		FlipInfo finfo = govdocStatManager.statToListGovdoc(null, conditionMap);
		
		List<EdocWorkStatDetialVO> edocVoList = finfo.getData();
		
		DataRow[] datarow = new DataRow[edocVoList.size()];
		DataRecord dr = new DataRecord();
		if(Strings.isNotEmpty(edocVoList)) {
			int row = 0;
			for(EdocWorkStatDetialVO listVo : edocVoList) {
				datarow[row] = new DataRow();
				 //超期显示
				 if(overTime == GovdocOverTimeEnum.yes.key()) {
					 datarow[row].addDataCell(listVo.getSubject(), 1);//标题
					 datarow[row].addDataCell(listVo.getDocMark(),1);//来文文号
					 datarow[row].addDataCell(listVo.getMaxOverPerson(),1);//超期最长人员
					 datarow[row].addDataCell(listVo.getMaxNodePolicy(),1);//超期最长节点
					 datarow[row].addDataCell(listVo.getOperDept(),1);//所在处室
					 datarow[row].addDataCell(listVo.getDeadlineOverView(),1);//超期总时长 		
				 } else {
					 datarow[row].addDataCell(listVo.getSubject(), 1);//标题
					 datarow[row].addDataCell(listVo.getStartUserName(),1);//发起人
					 datarow[row].addDataCell(listVo.getCurrentNodesInfo(),1);//当前待办人员
					 datarow[row].addDataCell(listVo.getStartTimeView(),1);//拟稿日期
					 datarow[row].addDataCell(listVo.getSerialNo(),1);//内部文号
					 datarow[row].addDataCell(listVo.getOperDept(),1);//处理部门
					 datarow[row].addDataCell(listVo.getSendUnit(),1);//来文单位
					 datarow[row].addDataCell(listVo.getDocMark(),1);//来文文号
					 datarow[row].addDataCell(listVo.getCompleteTimeView(),1);//办结时间
				 }
				 row++;
			}
			dr.addDataRow(datarow);
		}
		int row = 0;
		String[] columnName = null;
		if(overTime == GovdocOverTimeEnum.yes.key()) {
			columnName = new String[7];
			columnName[row++] = "超期公文标题";//超期公文标题
			columnName[row++] = ResourceUtil.getString("edoc.element.wordno.label");//公文文号
			columnName[row++] = "超期最长人员";//超期最长人员
			columnName[row++] = "超期最长节点";//超期最长节点
			columnName[row++] = "所在处室";//所在处室
			columnName[row++] = "超期总时长";//超期总时长
		} else {
			columnName = new String[10];
			columnName[row++] = ResourceUtil.getString("edoc.element.subject");//公文标题
			columnName[row++] = "发起人";//发起人
			columnName[row++] = ResourceUtil.getString("edoc.list.currentNodesInfo.label");//当前待办人
			if(govdocType == ApplicationSubCategoryEnum.edoc_fawen.key()) {
				columnName[row++] = ResourceUtil.getString("edoc.element.createdate");//拟稿日期	
			} else if(govdocType == ApplicationSubCategoryEnum.edoc_shouwen.key()) {
				columnName[row++] ="来文日期";//来文日期
			} else {
				columnName[row++] ="来文/拟稿日期";//来文日期
			}
			columnName[row++] = "内部文号";//内部文号
			columnName[row++] = "处理部门";//处理部门
			columnName[row++] = "来文单位";//来文单位
			if(govdocType == ApplicationSubCategoryEnum.edoc_fawen.key()) {
				columnName[row++] = "公文文号";//公文文号
			} else {
				columnName[row++] = "来文文号";//来文文号	
			}
			columnName[row++] = "办结时间";//办结时间
		}
		dr.setColumnName(columnName);
		dr.setSheetName(request.getParameter("listTitle"));
		dr.setTitle(request.getParameter("listTitle"));
		//信息列表
        this.fileToExcelManager.save(response, dr.getSheetName(), new DataRecord[] { dr });
		return null;
	}
	
	/**
	 * 签收统计-统计结果(暂无用，公用statResult)
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView statSignResult(HttpServletRequest request,HttpServletResponse response)throws Exception{
		ModelAndView mav = new ModelAndView("govdoc/stat/statsign_result");
		String timeTypeStr = request.getParameter("timeType");
		long statId = Long.parseLong(request.getParameter("stat"));
		Date startDate = null;
		Date endDate = null;
		int timeType = Integer.valueOf(timeTypeStr);
		List<Date> reList = initCheckDate(startDate,endDate,timeType,request);
		if(reList ==null){
			throw new Exception("查询签收统计数据错误");
		}
		startDate = reList.get(0);
		endDate = reList.get(1);
		EdocStatSet edocStatSet = govdocStatSetManager.getEdocStatSet(statId);
		if(edocStatSet.getState() == 1){
			throw new Exception("统计已经停用");
		}
		//分析配置是否显示 相关时间段统计值 
		String tTypes = edocStatSet.getTimeType();
		String[] times = tTypes.split(",");
		int showTwo = 0;
		int showThree = 0;
		int showFive = 0;
		int showNoRec = 0;
		for(String time:times){
			if("1".equals(time)){
				showTwo = 1;
			}else if("2".equals(time)){
				showThree = 1;
			}else if("3".equals(time)){
				showFive = 1;
			}else if("4".equals(time)){
				showNoRec = 1;
			}
		}
		
		mav.addObject("statName", edocStatSet.getName());
		mav.addObject("showTwo", showTwo);
		mav.addObject("showThree", showThree);
		mav.addObject("showFive",showFive);
		mav.addObject("showNoRec", showNoRec);
		mav.addObject("startTime", DateUtil.format(startDate));
		mav.addObject("endTime", DateUtil.format(endDate));
		mav.addObject("list", this.govdocStatManager.findMtechSignCount(startDate, endDate,statId));
		return mav; 
	}
	
	/**
	 * 签收统计-统计结果-导出EXCEL(暂无用，公用statResultExport)
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView statSignResultExport(HttpServletRequest request,HttpServletResponse response) throws Exception{
		String timeTypeStr = request.getParameter("timeType");
		long statId = Long.parseLong(request.getParameter("stat"));
		Date startDate = null;
		Date endDate = null;
		int timeType = Integer.valueOf(timeTypeStr);
		EdocStatSet edocStatSet = govdocStatSetManager.getEdocStatSet(statId);
		List<Date> reList = initCheckDate(startDate,endDate,timeType,request);
		if(reList ==null){
			throw new Exception("导出签收统计数据错误");
		}
		startDate = reList.get(0);
		endDate = reList.get(1);
		List<WebSignCount>  exportList = this.govdocStatManager.findMtechSignCount(startDate, endDate,statId);
		DataRow[] datarow = new DataRow[exportList.size()];
		DataRecord dr = new DataRecord();
		if(Strings.isNotEmpty(exportList)) {
			int row = 0;
			for(WebSignCount listVo : exportList) {
				 datarow[row] = new DataRow();			
				 datarow[row].addDataCell(listVo.getDeptName(), 1);//单位名称
				 datarow[row].addDataCell(String.valueOf(listVo.getAllNum()),1);//总数 
				 if(listVo.getSfShowTwo() == 1){
				 datarow[row].addDataCell(String.valueOf(listVo.getTwoSign()),1);//2个工作日内接收总数
				 datarow[row].addDataCell(listVo.getTwoSignPer(),1);//2个工作日内接收百分比
				 }
				 if(listVo.getSfShowThree() == 1){
					 datarow[row].addDataCell(String.valueOf(listVo.getThreeSign()),1);//3至5个工作日内接收数量
					 datarow[row].addDataCell(listVo.getThreeSignPer(),1);//3至5个工作日内接收百分比
				 }
				 if(listVo.getSfShowFive() == 1){
					 datarow[row].addDataCell(String.valueOf(listVo.getFiveSign()),1);//5个工作日后接收数量
					 datarow[row].addDataCell(listVo.getFiveSignPer(),1);//5个工作日后接收百分比
				 }
				 if(listVo.getSfShowNoRec() == 1){
					 datarow[row].addDataCell(String.valueOf(listVo.getNoRecSignNum()),1);//仍未签收数量 
					 datarow[row].addDataCell(listVo.getNoRecSignNumPer(),1);//仍未签收百分比
				 }
				 row++;
			}
			dr.addDataRow(datarow);
		}
		int row = 0;
		String[] columnName = null;
		columnName = new String[11];
		columnName[row++] ="单位";//单位
		columnName[row++] ="发文总数";//文总数
		//分析配置是否显示统计的字段
		String tTypes = edocStatSet.getTimeType();
		String[] times = tTypes.split(",");
		for(String time:times){
			if("1".equals(time)){
				columnName[row++] = "2个工作日内接收数量";//2个工作日内接收数量
				columnName[row++] = "2个工作日内接收百分比";//2个工作日内接收百分比
			}else if("2".equals(time)){
				columnName[row++] = "3至5个工作日内接收数量";//3至5个工作日内接收数量
				columnName[row++] = "3至5个工作日内接收百分比";//3至5个工作日内接收百分比
			}else if("3".equals(time)){
				columnName[row++] = "5个工作日后接收数量";//5个工作日后接收数量
				columnName[row++] = "5个工作日后接收百分比";//5个工作日后接收百分比
			}else if("4".equals(time)){
				columnName[row++] = "仍未签收数量";//仍未签收数量
				columnName[row++] = "仍未签收百分比";//仍未签收百分比
			}
		}
		dr.setColumnName(columnName);
		dr.setSheetName(edocStatSet.getName());
		dr.setTitle(edocStatSet.getName());
		//信息列表
        this.fileToExcelManager.save(response, dr.getSheetName(), new DataRecord[] { dr });
		return null;
	}
	
	/**
	 *签收统计-统计结果穿透列表
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView statSignResultToList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/stat/statsign_result_list");
		Map<String, String> conditionMap = new HashMap<String, String>();
    	conditionMap.put("statId", request.getParameter("statId"));
    	conditionMap.put("statType", request.getParameter("statType"));
    	conditionMap.put("listType", request.getParameter("listType"));
    	conditionMap.put("displayType", request.getParameter("displayType"));
    	conditionMap.put("displayId", request.getParameter("displayId"));
    	conditionMap.put("displayName", request.getParameter("displayName"));
    	conditionMap.put("startTime", request.getParameter("startTime"));
    	conditionMap.put("endTime", request.getParameter("endTime"));
    	conditionMap.put("statRangeId", request.getParameter("statRangeId"));
    	
    	
    	FlipInfo fi = govdocStatManager.signStatToListGovdoc(new FlipInfo(), conditionMap);
    	request.setAttribute("ffsignStatListRec", fi);
		return mav;
	}
	
	/*************************************************** 老功能收发统计与G6统计分割线 start *****************************************************/
	/**
	 * 收发统计
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView statOldCondition(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/stat/statold_condition");
		User user = AppContext.getCurrentUser();
		String conditionId = request.getParameter("conditionId");
		
		//首页点击更多进入，需要回写查询条件
		EdocStatCondition edocStatCondition = null;
		if (Strings.isNotBlank(conditionId)) {
			edocStatCondition = edocStatManager.getEdocStatConditionById(Long.valueOf(conditionId));
		}
		if(edocStatCondition != null){
			mav.addObject("startRangeTime", edocStatCondition.getStarttime());
			mav.addObject("endRangeTime", edocStatCondition.getEndtime());
			mav.addObject("defaultRangeIds", edocStatCondition.getOrganizationId());
			StringBuilder rangsNames = new StringBuilder();
			//统计范围
			if(edocStatCondition.getOrganizationId() != null){
				List<V3xOrgEntity> entityList = orgManager.getEntities(edocStatCondition.getOrganizationId());
				for (int i=0;i<entityList.size();i++) {
					rangsNames.append(entityList.get(i).getName());
					if(i<entityList.size()-1){
						rangsNames.append("、");
					}
				}
			}
			mav.addObject("defaultRangeNames", rangsNames.toString());
			mav.addObject("displayType", edocStatCondition.getStatisticsDimension());
			mav.addObject("timeType", edocStatCondition.getTimeType());
			mav.addObject("edocType", edocStatCondition.getEdocType());
			StringBuilder templateNames = new StringBuilder();
			//流程模板
			if(Strings.isNotBlank(edocStatCondition.getOperationTypeIds())){
				mav.addObject("operationTypeIds", edocStatCondition.getOperationTypeIds());
				List<Long> operationTypeIds = new ArrayList<Long>();
				TemplateManager templateManager = (TemplateManager) AppContext.getBean("templateManager");
				String[] operationTypeIdsStr = edocStatCondition.getOperationTypeIds().split(",");
				for (String templateId : operationTypeIdsStr) {
					operationTypeIds.add(Long.valueOf(templateId));
				}
				List<CtpTemplate>  tempList = templateManager.getSystemTemplates(operationTypeIds);
				for (int i=0;i<tempList.size();i++) {
					templateNames.append(tempList.get(i).getSubject());
					if(i<tempList.size()-1){
						templateNames.append("、");
					}
				}
				mav.addObject("templateNames", templateNames.toString());
			}
			mav.addObject("operationType", edocStatCondition.getOperationType());
			//流程类型
			if(Strings.isNotBlank(edocStatCondition.getOperationType())){
				if(edocStatCondition.getOperationType().contains("self")){
					mav.addObject("workflowByPersonal", "1");
				}
				if(edocStatCondition.getOperationType().contains("template")){
					mav.addObject("workflowBySystem", "1");
				}
			}
			GovdocElementManager govdocElementManager = (GovdocElementManager) AppContext.getBean("govdocElementManager");
			EnumManager enumManagerNew = (EnumManager) AppContext.getBean("enumManagerNew");
			//公文级别
			if(Strings.isNotBlank(edocStatCondition.getUnitLevel())){
				String[] u = edocStatCondition.getUnitLevel().split(",");
				List<String> unitLevelList =  Arrays.asList(u);
				StringBuilder unitLevelNames = new StringBuilder();
				EdocElement element = govdocElementManager.getByFieldName("unit_level", user.getLoginAccount());
				if(element != null && element.getMetadataId()!=null) {
					List<CtpEnumItem> enumItemList = enumManagerNew.getEnumItemInDatabse(element.getMetadataId());
					if(enumItemList != null && enumItemList.size()>0){
						for(int i=0;i<enumItemList.size();i++){
							String enumValue = enumItemList.get(i).getEnumvalue();
							if(unitLevelList.contains(enumValue)){
								unitLevelNames.append(enumItemList.get(i).getShowvalue());
								if(i<unitLevelList.size()-1){
									unitLevelNames.append("、");
								}
							}
						}	
					}
				}
				mav.addObject("unitLevelName", unitLevelNames.toString());
				mav.addObject("unitLevelId", edocStatCondition.getUnitLevel());
			}
			//公文类型
			if(Strings.isNotBlank(edocStatCondition.getSendType())){
				String[] u = edocStatCondition.getSendType().split(",");
				List<String> sendTypeList =  Arrays.asList(u);
				StringBuilder sendTypeNames = new StringBuilder();
				EdocElement element = govdocElementManager.getByFieldName("send_type", user.getLoginAccount());
				if(element != null && element.getMetadataId()!=null) {
					List<CtpEnumItem> enumItemList = enumManagerNew.getEnumItemInDatabse(element.getMetadataId());
					if(enumItemList != null && enumItemList.size()>0){
						for(int i=0;i<enumItemList.size();i++){
							String enumValue = enumItemList.get(i).getEnumvalue();
							if(sendTypeList.contains(enumValue)){
								sendTypeNames.append(enumItemList.get(i).getShowvalue());
								if(i<sendTypeList.size()-1){
									sendTypeNames.append("、");
								}
							}
						}	
					}
				}
				mav.addObject("sendTypeName", sendTypeNames.toString());
				mav.addObject("sendTypeId", edocStatCondition.getSendType());
				
			}
		}else{
			Date date = Datetimes.getFirstDayInMonth(new Date());
			mav.addObject("startRangeTime", Datetimes.format(date, "yyyy-MM-dd"));
			mav.addObject("endRangeTime", Datetimes.format(new Date(),"yyyy-MM-dd"));
			mav.addObject("defaultRangeIds", "Account|"+user.getLoginAccount());
			mav.addObject("defaultRangeNames", user.getLoginAccountName());
			mav.addObject("displayType", "1");
			mav.addObject("edocType", StatEdocTypeEnum.edoc_rec.key());//默认为收文
			mav.addObject("workflowByPersonal", "1");
			mav.addObject("workflowBySystem", "1");
		}
		boolean isDeptExchange = false;
		mav.addObject("isAccountExchange", true);
		mav.addObject("sectionId",Strings.isNotBlank(request.getParameter("sectionId"))?request.getParameter("sectionId"):"");
		mav.addObject("currentDeptId", user.getDepartmentId());
		mav.addObject("currentAccountId", user.getLoginAccount());
		mav.addObject("isG6Version", GovdocHelper.isG6Version());
		mav.addObject("isDeptExchange", isDeptExchange);
		return mav;
	}
	
	/**
	 * 收发统计-统计结果
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView statOldResult(HttpServletRequest request, HttpServletResponse response) throws Exception {
		int edocType = Strings.isBlank(request.getParameter("edocType")) ? StatEdocTypeEnum.edoc_rec.key() : Integer.parseInt(request.getParameter("edocType"));
		User user = AppContext.getCurrentUser();
		ModelAndView mav = null;
		if(edocType == StatEdocTypeEnum.edoc_send.key()) {
			mav = new ModelAndView("govdoc/stat/statold_result_send");
			mav.addObject("showBanwenYuewen", GovdocSwitchHelper.showBanwenYuewen(user.getLoginAccount()));
		} else if(edocType == StatEdocTypeEnum.edoc_sign.key()) {
			mav = new ModelAndView("govdoc/stat/statold_result_sign");
		} else {//默认收文统计
			mav = new ModelAndView("govdoc/stat/statold_result_rec");
		}
		mav.addObject("isG6Version", GovdocHelper.isG6Version());
		if(Strings.isNotBlank(request.getParameter("statConditionId"))) {
			EdocStatCondition statCondition = edocStatManager.getEdocStatConditionById(Long.parseLong(request.getParameter("statConditionId")));
			mav.addObject("statCondition", statCondition);
		}
		return mav;
	}
	
	/**
	 * 收发统计-统计结果-导出Excel
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ModelAndView statOldResultExport(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("edocType", request.getParameter("edocType"));
		params.put("displayId", request.getParameter("displayId"));
		params.put("displayType", request.getParameter("displayType"));
		params.put("displayTimeType", request.getParameter("displayTimeType"));
		params.put("rangeIds", request.getParameter("rangeIds"));
		params.put("unitLevelId", request.getParameter("unitLevelId"));
		params.put("sendTypeId", request.getParameter("sendTypeId"));
		params.put("startRangeTime", request.getParameter("startRangeTime"));
		params.put("endRangeTime", request.getParameter("endRangeTime"));
		params.put("operationTypeIds", request.getParameter("operationTypeIds"));
		params.put("operationType", request.getParameter("operationType"));
		params.put("isPager", "false");
		int edocType = Integer.parseInt(params.get("edocType"));
		boolean showBanwenYuewen = GovdocSwitchHelper.showBanwenYuewen(AppContext.getCurrentUser().getLoginAccount());
		
		FlipInfo filpInfo = new FlipInfo();
		filpInfo = govdocStatManager.getGovdocStatVoList(filpInfo, params);
		List<EdocStatVO> statVoList = (List<EdocStatVO>)filpInfo.getData();
		DataRow[] datarow = new DataRow[statVoList.size()];
		DataRecord dr = new DataRecord();
		if(Strings.isNotEmpty(statVoList)) {
			int row = 0;
			for(EdocStatVO statVo : statVoList) {
				datarow[row] = new DataRow();
				datarow[row].addDataCell(statVo.getDisplayName(), 1);//统计范围
				if(edocType == 1) {
					 datarow[row].addDataCell(String.valueOf(statVo.getCountDoAll()), 1);//总办件
					 datarow[row].addDataCell(String.valueOf(statVo.getCountFinish()), 1);//已办结
					 datarow[row].addDataCell(String.valueOf(statVo.getCountWaitFinish()), 1);//未办结
					 datarow[row].addDataCell(String.valueOf(statVo.getCountSent()), 1);//已发
					 datarow[row].addDataCell(String.valueOf(statVo.getCountDone()), 1);//已办
					 datarow[row].addDataCell(String.valueOf(statVo.getCountPending()), 1);//待办
					 if(showBanwenYuewen) {
						 datarow[row].addDataCell(String.valueOf(statVo.getCountReadAll()), 1);//总阅件
						 datarow[row].addDataCell(String.valueOf(statVo.getCountReading()), 1);//待阅
						 datarow[row].addDataCell(String.valueOf(statVo.getCountReaded()), 1);//已阅
					 }
				 } else {
					 datarow[row].addDataCell(String.valueOf(statVo.getCountHandleAll()), 1);//总经办
					 datarow[row].addDataCell(String.valueOf(statVo.getCountFinish()), 1);//已办结
					 datarow[row].addDataCell(String.valueOf(statVo.getCountWaitFinish()), 1);//未办结
					 datarow[row].addDataCell(String.valueOf(statVo.getCountSent()), 1);//已发
					 datarow[row].addDataCell(String.valueOf(statVo.getCountDone()), 1);//已办
					 datarow[row].addDataCell(String.valueOf(statVo.getCountPending()), 1);//待办
				 }
				 row++;
			}
			dr.addDataRow(datarow);
		}
		String doAllLabel = "";
		int row = 0;
		String[] columnName = null;
		if(edocType == 1 && showBanwenYuewen) {
			columnName = new String[11];
			doAllLabel = "edoc.stat.result.list.totalEdocType1";
		} else {
			columnName = new String[8];
			doAllLabel = "edoc.stat.result.list.totalEdocType0";
		}
		columnName[row++] = ResourceUtil.getString("edoc.stat.org");//统计范围
		columnName[row++] = ResourceUtil.getString(doAllLabel);//总经办/总办件
		columnName[row++] = ResourceUtil.getString("edoc.stat.result.list.finish");//已办结
		columnName[row++] = ResourceUtil.getString("edoc.stat.result.list.waitFinish");//未办结
		columnName[row++] = ResourceUtil.getString("edoc.stat.result.list.sent");//已发
		columnName[row++] = ResourceUtil.getString("edoc.stat.result.list.done");//已办
		columnName[row++] = ResourceUtil.getString("edoc.stat.result.list.pending");//待办
		if(edocType == 1) {//收文
			if(showBanwenYuewen) {//区分办/阅文
				columnName[row++] = ResourceUtil.getString("edoc.stat.result.list.allread");//总阅件
				columnName[row++] = ResourceUtil.getString("edoc.stat.result.list.reading");//待阅
				columnName[row++] = ResourceUtil.getString("edoc.stat.result.list.readed");//已阅
			}
		}
		dr.setColumnName(columnName);
		dr.setSheetName(request.getParameter("statTitle"));
		dr.setTitle(request.getParameter("statTitle"));
		//信息列表
        this.fileToExcelManager.save(response, dr.getSheetName(), new DataRecord[] { dr });
		return null;
	}
	
	/**
	 * 收发统计-统计结果穿透列表
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView statOldResultToList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Integer edocType = Strings.isBlank(request.getParameter("edocType")) ? 0 : Integer.parseInt(request.getParameter("edocType"));
		ModelAndView mav = new ModelAndView("govdoc/stat/statold_result_list_send");
		FlipInfo fi = new FlipInfo();
		fi.setParams(null);
		String sendTypeCategory = "edoc_new_send_permission_policy";
		if(edocType.intValue() == 1) {
			mav = new ModelAndView("govdoc/stat/statold_result_list_rec");
			sendTypeCategory = "edoc_new_rec_permission_policy";
		} else if(edocType.intValue() == 2) {
			mav = new ModelAndView("govdoc/stat/statold_result_list_sign");
			sendTypeCategory = "edoc_new_qianbao_permission_policy";
		}
		mav.addObject("isG6Version", GovdocHelper.isG6Version());
		mav.addObject("nodeList", permissionManager.getPermissionsByCategory(sendTypeCategory, AppContext.getCurrentUser().getLoginAccount()));
		return mav;
	}
	
	/**
	 * 收发统计-统计结果穿透列表-导出Excel
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ModelAndView statOldResultToListExport(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("edocType", request.getParameter("edocType"));
		params.put("listType", request.getParameter("listType"));
		params.put("displayId", request.getParameter("displayId"));
		params.put("displayType", request.getParameter("displayType"));
		params.put("displayTimeType", request.getParameter("displayTimeType"));
		params.put("rangeIds", request.getParameter("rangeIds"));
		params.put("unitLevelId", request.getParameter("unitLevelId"));
		params.put("sendTypeId", request.getParameter("sendTypeId"));
		params.put("startRangeTime", request.getParameter("startRangeTime"));
		params.put("endRangeTime", request.getParameter("endRangeTime"));
		params.put("operationTypeIds", request.getParameter("operationTypeIds"));
		params.put("operationType", request.getParameter("operationType"));
		params.put("condition", request.getParameter("condition"));
		params.put("subject", request.getParameter("subject"));
		params.put("docMark", request.getParameter("docMark"));
		params.put("serialNo", request.getParameter("serialNo"));
		params.put("sendUnit", request.getParameter("sendUnit"));
		params.put("coverTime", request.getParameter("coverTime"));
		params.put("nodePolicy", request.getParameter("nodePolicy"));
		params.put("isPager", "false");
		int edocType = Integer.parseInt(params.get("edocType"));
		
		FlipInfo filpInfo = new FlipInfo();
		filpInfo = govdocStatManager.getEdocVoList(filpInfo, params);
		List<EdocStatListVO> edocVoList = (List<EdocStatListVO>)filpInfo.getData();
		DataRow[] datarow = new DataRow[edocVoList.size()];
		DataRecord dr = new DataRecord();
		if(Strings.isNotEmpty(edocVoList)) {
			int row = 0;
			for(EdocStatListVO listVo : edocVoList) {
				datarow[row] = new DataRow();
				 if(edocType == 1) {
					 datarow[row].addDataCell(listVo.getSubject(), 1);//标题
					 datarow[row].addDataCell(listVo.getDocMark(),1);//来文文号
					 datarow[row].addDataCell(listVo.getSerialNo(),1);//收文编号
					 datarow[row].addDataCell(listVo.getSendUnit(),1);//来文单位
					 datarow[row].addDataCell(DateUtil.format(listVo.getStartTime()),1);//分发日期
					 datarow[row].addDataCell(listVo.getStartUserName(),1);//分发人
					 datarow[row].addDataCell(listVo.getCurrentNodesInfo(),1);//当前待办人
					 datarow[row].addDataCell(listVo.getCoverTimeView(),1);//是否超期
					 datarow[row].addDataCell(listVo.getDeadlineTimeView(),1);//流程期限
					 datarow[row].addDataCell(listVo.getDeadlineOverView(),1);//流程超期时长
					 datarow[row].addDataCell(listVo.getIsFinishView(),1);//是否办结
					 datarow[row].addDataCell(listVo.getHasArchiveView(),1);//是否单位归档
					 datarow[row].addDataCell(listVo.getArchiveName(),1);//单位归档路径
					 datarow[row].addDataCell(listVo.getUndertakenoffice(),1);//承办机构
					 datarow[row].addDataCell(listVo.getUnitLevelName(),1);//公文级别
				 } else {
					 datarow[row].addDataCell(listVo.getSubject(), 1);//标题
					 datarow[row].addDataCell(listVo.getDocMark(),1);//公文文号
					 datarow[row].addDataCell(listVo.getSerialNo(),1);//内部编号
					 datarow[row].addDataCell(listVo.getSendUnit(),1);//发文单位
					 datarow[row].addDataCell(DateUtil.format(listVo.getStartTime()),1);//拟稿日期
					 datarow[row].addDataCell(listVo.getStartUserName(),1);//拟稿人
					 datarow[row].addDataCell(listVo.getCurrentNodesInfo(),1);//当前待办人
					 datarow[row].addDataCell(listVo.getCoverTimeView(),1);//是否超期
					 datarow[row].addDataCell(listVo.getDeadlineTimeView(),1);//流程期限
					 datarow[row].addDataCell(listVo.getDeadlineOverView(),1);//流程超期时长
					 datarow[row].addDataCell(listVo.getIsFinishView(),1);//是否办结
					 datarow[row].addDataCell(listVo.getHasArchiveView(),1);//是否单位归档
					 datarow[row].addDataCell(listVo.getArchiveName(),1);//单位归档路径
					 datarow[row].addDataCell(listVo.getSendDepartment(),1);//发文部门
					 datarow[row].addDataCell(listVo.getUnitLevelName(),1);//公文级别
					 datarow[row].addDataCell(listVo.getIssuer(),1);//签发人
					 datarow[row].addDataCell(listVo.getSigningDate(),1);//签发日期
				 }
				 row++;
			}
			dr.addDataRow(datarow);
		}
		int row = 0;
		String[] columnName = null;
		if(edocType == 1) {
			columnName = new String[15];
			columnName[row++] = ResourceUtil.getString("edoc.element.subject");//公文标题
			columnName[row++] = ResourceUtil.getString("edoc.element.fromWordNo");//来文文号
			columnName[row++] = ResourceUtil.getString("edoc.element.receive.serial_no");//收文编号
			columnName[row++] = ResourceUtil.getString("edoc.edoctitle.fromUnit.label");//来文单位
			if(GovdocHelper.isG6Version()) {
				columnName[row++] = ResourceUtil.getString("edoc.edoctitle.disDate.label");//分发日期
				columnName[row++] = ResourceUtil.getString("edoc.element.receive.distributer");//分发人
			 } else {
				columnName[row++] = ResourceUtil.getString("edoc.element.registration_date");//登记日期
				columnName[row++] = ResourceUtil.getString("edoc.edoctitle.regPerson.label");//登记人
			}
			columnName[row++] = ResourceUtil.getString("edoc.list.currentNodesInfo.label");//当前待办人
			columnName[row++] = ResourceUtil.getString("node.isovertoptime");//是否超期
			columnName[row++] = ResourceUtil.getString("process.deadlineTime.label");//流程期限
			columnName[row++] = ResourceUtil.getString("edoc.stat.result.list.coverTime");//流程超期时长
			columnName[row++] = ResourceUtil.getString("edoc.stat.result.list.isFinish");//是否办结
			columnName[row++] = ResourceUtil.getString("edoc.stat.result.list.hasAchrive");//是否单位归档
			columnName[row++] = ResourceUtil.getString("edoc.stat.result.list.achriveName");//单位归档路径
			columnName[row++] = ResourceUtil.getString("edoc.element.undertakeUnit");//承办机构
			columnName[row++] = ResourceUtil.getString("edoc.element.unitLevel");//公文级别
		} else {
			columnName = new String[17];
			columnName[row++] = ResourceUtil.getString("edoc.element.subject");//公文标题
			columnName[row++] = ResourceUtil.getString("edoc.element.wordno.label");//公文文号
			columnName[row++] = ResourceUtil.getString("edoc.element.wordinno.label");//内部文号
			columnName[row++] = ResourceUtil.getString("edoc.element.sendunit");//发文单位
			columnName[row++] = ResourceUtil.getString("edoc.element.createdate");//拟稿日期
			columnName[row++] = ResourceUtil.getString("edoc.element.author");//拟稿人
			columnName[row++] = ResourceUtil.getString("edoc.list.currentNodesInfo.label");//当前待办人
			columnName[row++] = ResourceUtil.getString("node.isovertoptime");//是否超期
			columnName[row++] = ResourceUtil.getString("process.deadlineTime.label");//流程期限
			columnName[row++] = ResourceUtil.getString("edoc.stat.result.list.coverTime");//流程超期时长
			columnName[row++] = ResourceUtil.getString("edoc.stat.result.list.isFinish");//是否办结
			columnName[row++] = ResourceUtil.getString("edoc.stat.result.list.hasAchrive");//是否单位归档
			columnName[row++] = ResourceUtil.getString("edoc.stat.result.list.achriveName");//单位归档路径
			columnName[row++] = ResourceUtil.getString("edoc.element.senddepartment");//发文部门
			columnName[row++] = ResourceUtil.getString("edoc.stat.condition.unitLevel");//公文级别
			columnName[row++] = ResourceUtil.getString("edoc.element.issuer");//签发人
			columnName[row++] = ResourceUtil.getString("edoc.element.sendingdate");//签发日期
		}
		dr.setColumnName(columnName);
		dr.setSheetName(request.getParameter("listTitle"));
		dr.setTitle(request.getParameter("listTitle"));
		//信息列表
        this.fileToExcelManager.save(response, dr.getSheetName(), new DataRecord[] { dr });
		return null;
	}
	
	/**
	 * 首页推送
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public ModelAndView statOldResultPush(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map mainDataMap =  ParamUtil.getJsonDomain("statConditionForm");
		User user = AppContext.getCurrentUser();
		int pushRole = EdocStatRoleTypeEnum.account.key();
		long pushFrom = 0;
		if(GovdocRoleHelper.isAccountExchange()) {
			pushRole = EdocStatRoleTypeEnum.account.key();
			pushFrom = user.getLoginAccount();
		} else {
			List<Long> userAllExchangeDeptIdList = EdocStatHelper.getCurrentUserAllExchangeDeptIdList();
			if(Strings.isNotEmpty(userAllExchangeDeptIdList)) {
				if(userAllExchangeDeptIdList.size()>1) {
					pushRole = EdocStatRoleTypeEnum.dept_multi.key();
					pushFrom = user.getLoginAccount();
				} else {
					pushRole = EdocStatRoleTypeEnum.dept.key();
					pushFrom = userAllExchangeDeptIdList.get(0);
				}
			}
		}
		EdocStatCondition condition = new EdocStatCondition();
		condition.setStatisticsDimension(Integer.parseInt((String)mainDataMap.get("displayType")));
		condition.setTimeType(Integer.parseInt((String)mainDataMap.get("displayTimeType")));
		condition.setOrganizationId((String)mainDataMap.get("rangeIds"));
		condition.setEdocType(Integer.parseInt((String)mainDataMap.get("edocType")));
		condition.setSendType((String)mainDataMap.get("sendTypeId"));
		condition.setUnitLevel((String)mainDataMap.get("unitLevelId"));
		condition.setStarttime((String)mainDataMap.get("startRangeTime"));
		condition.setEndtime((String)mainDataMap.get("endRangeTime"));
		condition.setOperationType((String)mainDataMap.get("operationType"));
		condition.setOperationTypeIds((String)mainDataMap.get("operationTypeIds"));
		condition.setTitle((String)mainDataMap.get("statTitle"));
		condition.setIsOld(Boolean.FALSE);
		condition.setPushRole(pushRole);
		condition.setPushFrom(pushFrom);
		condition.setUserId(user.getId());
		condition.setAccountId(user.getLoginAccount());
		condition.setIdIfNew();
		//先删除以前推送到首页的数据
		edocStatManager.delEdocStatConditionByAccountId(user.getAccountId());
		edocStatManager.saveEdocStatCondition(condition);
		return null;
	}
	 /**
     * 插入推送到首页的 登记簿的查询条件
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView saveRegisterCondition(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	try {
	        govdocStatPushManager.saveRegisterCondition(request.getParameter("listType"), AppContext.getCurrentUser());
    	} catch(Exception e) {
    		LOGGER.error("登记簿推送到首页出错", e);
    	}
        return null;
    }
    
	/**
	 * 打开行文类型选择框
	 * @param request
	 * @param response
	 * @return 
	 * @throws Exception
	 */
	public ModelAndView openSendTypeDialog(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/stat/statold_send_type_dialog");
		return mav;
	}
	
	/**
	 * 打开公文级别选择框
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView openUnitLevelDialog(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/stat/statold_unit_level_dialog");
		return mav;
	}
	/*************************************************** 老功能收发统计与G6统计分割线   end *****************************************************/
	
	private Date getDate(String year,String month,String type){
		Calendar c= Calendar.getInstance();
		c.clear();
		if(Strings.isNotBlank(year)){
			c.set(Calendar.YEAR, Integer.valueOf(year));
		}
		if(Strings.isNotBlank(month)){
			c.set(Calendar.MONTH, Integer.valueOf(month) -1);
		}
		if("start".equals(type)){
			return c.getTime();
		}else if("end".equals(type)){
			//设置每天的最大小时
			c.set(Calendar.HOUR_OF_DAY, c.getActualMaximum(Calendar.HOUR_OF_DAY));
			//设置每小时最大分钟
			c.set(Calendar.MINUTE, c.getActualMaximum(Calendar.MINUTE));
			//设置每分钟最大秒
			c.set(Calendar.SECOND, c.getActualMaximum(Calendar.SECOND));
			if(Strings.isNotBlank(month)){
				c.roll(Calendar.DAY_OF_MONTH, -1);
				return c.getTime();
			}else{
				c.roll(Calendar.DAY_OF_YEAR, -1);
				return c.getTime();
			}
		}
		return c.getTime();
	}
	
	/**
	 * 生成查询时间
	 * @param startDate
	 * @param endDate
	 * @param timeType
	 * @param request
	 * @throws Exception
	 */
	private List<Date> initCheckDate(Date startDate,Date endDate,int timeType,HttpServletRequest request) throws Exception{
	   List<Date> re = new ArrayList<Date>();
	   switch(timeType){
		case 1:
			String startYear = request.getParameter("yeartype-startyear");
			String endYear = request.getParameter("yeartype-endyear");
			startDate = this.getDate(startYear, null, "start");
			endDate = this.getDate(endYear, null, "end");		
			break;
		case 2:
			String seasonStartYear = request.getParameter("seasontype-startyear");
			String startSeason =request.getParameter("seasontype-startseason");
			String seasonEndYear = request.getParameter("seasontype-endyear");
			String endSeason =request.getParameter("seasontype-endseason");
			Map<String,String> startMap = seasonMap.get(startSeason);
			Map<String,String> endMap = seasonMap.get(endSeason);
			startDate = this.getDate(seasonStartYear, startMap.get("start"), "start");
			endDate = this.getDate(seasonEndYear, endMap.get("end"), "end");
			break;
		case 3:
			String monthStartYear = request.getParameter("monthtype-startyear");
			String startMonth = request.getParameter("monthtype-startmonth");
			String monthEndYear = request.getParameter("monthtype-endyear");
			String endMonth = request.getParameter("monthtype-endmonth");
			startDate = this.getDate(monthStartYear, startMonth, "start");
			endDate = this.getDate(monthEndYear, endMonth, "end");
			break;
		case 4:
			String startTime = request.getParameter("daytype-startday");
			String endTime = request.getParameter("daytype-endday");
			startDate =DateUtil.getByStandard19DateAndTime(startTime+" 00:00:00");
			endDate =DateUtil.getByStandard19DateAndTime(endTime+" 23:59:59");
			break;
		}
		re.add(startDate);
		re.add(endDate);
	    return re;
	}

	public void setGovdocStatManager(GovdocStatManager govdocStatManager) {
		this.govdocStatManager = govdocStatManager;
	}
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}
	public void setFileToExcelManager(FileToExcelManager fileToExcelManager) {
		this.fileToExcelManager = fileToExcelManager;
	}
	public void setEdocStatManager(EdocStatManager edocStatManager) {
		this.edocStatManager = edocStatManager;
	}
	public void setGovdocMarkManager(GovdocMarkManager govdocMarkManager) {
		this.govdocMarkManager = govdocMarkManager;
	}
	public void setGovdocStatSetManager(GovdocStatSetManager govdocStatSetManager) {
		this.govdocStatSetManager = govdocStatSetManager;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public void setGovdocStatPushManager(GovdocStatPushManager govdocStatPushManager) {
		this.govdocStatPushManager = govdocStatPushManager;
	}
	
}