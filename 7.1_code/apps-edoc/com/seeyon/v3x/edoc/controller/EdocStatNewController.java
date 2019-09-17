package com.seeyon.v3x.edoc.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.govdoc.helper.GovdocRoleHelper;
import com.seeyon.apps.govdoc.manager.GovdocStatSetManager;
import com.seeyon.apps.govdoc.po.EdocStatSet;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.excel.DataRecord;
import com.seeyon.ctp.common.excel.DataRow;
import com.seeyon.ctp.common.excel.FileToExcelManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.MemberRole;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocStatCondition;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocMarkDefinitionManager;
import com.seeyon.v3x.edoc.manager.EdocRoleHelper;
import com.seeyon.v3x.edoc.manager.EdocStatManager;
import com.seeyon.v3x.edoc.manager.EdocStatNewManager;
import com.seeyon.v3x.edoc.manager.EdocSwitchHelper;
import com.seeyon.v3x.edoc.util.EdocStatEnum.EdocStatRoleTypeEnum;
import com.seeyon.v3x.edoc.util.EdocStatHelper;
import com.seeyon.v3x.edoc.util.GovdocStatUtil;
import com.seeyon.v3x.edoc.webmodel.EdocMarkModel;
import com.seeyon.v3x.edoc.webmodel.EdocStatListVO;
import com.seeyon.v3x.edoc.webmodel.EdocStatVO;

/**
 * 公文收发统计-20141224
 * @author tanggl
 *
 */
public class EdocStatNewController extends BaseController {
	
	private EdocStatNewManager edocStatNewManager;	
	private EdocStatManager edocStatManager;
	private PermissionManager permissionManager;
	private FileToExcelManager fileToExcelManager;
	private OrgManager orgManager;
	
	/**
	 * 公文统计
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView edocStat(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("edoc/docstat/edoc_stat");
		User user = AppContext.getCurrentUser();
		boolean isAccountExchange = GovdocRoleHelper.isAccountExchange();
		boolean isDeptExchange = false;
		mav.addObject("isAccountExchange", isAccountExchange);
		mav.addObject("currentDeptId", user.getDepartmentId());
		mav.addObject("currentAccountId", user.getLoginAccount());
		Date date = Datetimes.getFirstDayInMonth(new Date());
		mav.addObject("startRangeTime", Datetimes.format(date, "yyyy-MM-dd"));
		mav.addObject("endRangeTime", Datetimes.format(new Date(),"yyyy-MM-dd"));
		mav.addObject("isG6Version", EdocHelper.isG6Version());
		if(isAccountExchange) {
			mav.addObject("defaultRangeNames", user.getLoginAccountName());
			mav.addObject("defaultRangeIds", "Account|"+user.getLoginAccount());
		} else {
			StringBuilder defaultRangeIds = new StringBuilder();
			StringBuilder defaultRangeNames = new StringBuilder();
			StringBuilder currentDeptIds =new StringBuilder();
			StringBuilder allDeptIds = new StringBuilder();
			
			//只找到当前发起者单位的
            List<V3xOrgDepartment> temp = orgManager.getDeptsByManager(user.getId(), user.getLoginAccount());
            
            List<MemberRole> mRoles = orgManager.getMemberRoles(user.getId(), user.getLoginAccount());
            List<V3xOrgDepartment> userAllDeptList = new ArrayList<V3xOrgDepartment>();
            if(Strings.isNotEmpty(mRoles)){
                for(MemberRole r : mRoles){
                    if(r.getRole().getCode().equals(Role_NAME.Departmentexchange.name())){
                        userAllDeptList.add(r.getDepartment());
                    }
                }
            }
			
			for(V3xOrgDepartment dept : userAllDeptList) {
				if(dept.getOrgAccountId().longValue() == user.getLoginAccount().longValue()) {
					if(GovdocRoleHelper.isDepartmentExchange(user.getId(), dept.getId(), dept.getOrgAccountId())) {
						isDeptExchange = true;
						defaultRangeIds.append("Department|")
						               .append(dept.getId())
						               .append(",");
						defaultRangeNames.append(dept.getName())
						                 .append("、");
						currentDeptIds.append(dept.getId())
						              .append(",");
						allDeptIds.append(dept.getId())
						          .append(",");
						List<V3xOrgDepartment> subDeptList = orgManager.getChildDepartments(dept.getId(), false);
						if(Strings.isNotEmpty(subDeptList)) {
							for(V3xOrgDepartment subDept : subDeptList) {
								allDeptIds.append(subDept.getId())
								          .append(",");
							}
						}
					}
				}
			}
			if(currentDeptIds.length() > 0) {
			    currentDeptIds.deleteCharAt(currentDeptIds.length() - 1);
			}
			if(defaultRangeIds.length() > 0) {
			    defaultRangeIds.deleteCharAt(defaultRangeIds.length() - 1);
			}
			if(defaultRangeNames.length() > 0) {
			    defaultRangeNames.deleteCharAt(defaultRangeNames.length() - 1);
			}
			mav.addObject("defaultRangeIds", defaultRangeIds.toString());
			mav.addObject("defaultRangeNames", defaultRangeNames.toString());
			mav.addObject("currentDeptIds", currentDeptIds.toString());
			mav.addObject("allDeptIds", allDeptIds.toString());
		}
		mav.addObject("isDeptExchange", isDeptExchange);
		return mav;
	}
	
	/**
	 * 打开行文类型选择框
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView openUnitLevelDialog(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("edoc/docstat/stat_unit_level_dialog");
		return mav;
	}
	
	
	/**
	 * 打开行文类型选择框
	 * @param request
	 * @param response
	 * @return 
	 * @throws Exception
	 */
	public ModelAndView openSendTypeDialog(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("edoc/docstat/stat_send_type_dialog");
		return mav;
	}
	
	/**
	 * 打开行文类型选择框
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView openPushTitleDialog(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("edoc/docstat/stat_push_title_dialog");
		return mav;
	}
	
	/**
	 * 公文统计结果
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView edocStatResult(HttpServletRequest request, HttpServletResponse response) throws Exception {
		int edocType = Strings.isBlank(request.getParameter("edocType")) ? 0 : Integer.parseInt(request.getParameter("edocType"));
		User user = AppContext.getCurrentUser();
		ModelAndView mav = new ModelAndView("edoc/docstat/edoc_stat_result_send");
		if(edocType == 1) {
			mav = new ModelAndView("edoc/docstat/edoc_stat_result_rec");
			mav.addObject("showBanwenYuewen", EdocSwitchHelper.showBanwenYuewen(user.getLoginAccount()));
		} else if(edocType == 2) {
			mav = new ModelAndView("edoc/docstat/edoc_stat_result_sign");
		}
		mav.addObject("isG6Version", EdocHelper.isG6Version());
		if(Strings.isNotBlank(request.getParameter("statConditionId"))) {
			EdocStatCondition statCondition = edocStatManager.getEdocStatConditionById(Long.parseLong(request.getParameter("statConditionId")));
			mav.addObject("statCondition", statCondition);
		}
		return mav;
	}

	/**
	 * 公文统计穿透列表
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView statEdocList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Integer edocType = Strings.isBlank(request.getParameter("edocType")) ? 0 : Integer.parseInt(request.getParameter("edocType"));
		ModelAndView mav = new ModelAndView("edoc/docstat/edoc_stat_list_send");
		FlipInfo fi = new FlipInfo();
		fi.setParams(null);
		String sendTypeCategory = "edoc_send_permission_policy";
		if(edocType.intValue() == 1) {
			mav = new ModelAndView("edoc/docstat/edoc_stat_list_rec");
			sendTypeCategory = "edoc_rec_permission_policy";
		} else if(edocType.intValue() == 2) {
			mav = new ModelAndView("edoc/docstat/edoc_stat_list_sign");
			sendTypeCategory = "edoc_qianbao_permission_policy";
		}
		mav.addObject("isG6Version", EdocHelper.isG6Version());
		mav.addObject("nodeList", permissionManager.getPermissionsByCategory(sendTypeCategory, AppContext.getCurrentUser().getLoginAccount()));
		return mav;
	}
	
	private EdocMarkDefinitionManager edocMarkDefinitionManager;
	
	public EdocMarkDefinitionManager getEdocMarkDefinitionManager() {
		return edocMarkDefinitionManager;
	}

	public void setEdocMarkDefinitionManager(EdocMarkDefinitionManager edocMarkDefinitionManager) {
		this.edocMarkDefinitionManager = edocMarkDefinitionManager;
	}
	
	private GovdocStatSetManager govdocStatSetManager;

	public GovdocStatSetManager getGovdocStatSetManager() {
		return govdocStatSetManager;
	}

	public void setGovdocStatSetManager(GovdocStatSetManager govdocStatSetManager) {
		this.govdocStatSetManager = govdocStatSetManager;
	}

	/****************************** 新公文统计-华丽丽的分割线 *******************************/
	/**
	 * 签收/工作统计页面
	 * @param request
	 * @param response
	 * @return
	 */
	public ModelAndView govdocStat(HttpServletRequest request,HttpServletResponse response) throws Exception{
		 ModelAndView mav = new ModelAndView("edoc/docstat/govdoc_stat");
		 User user = AppContext.getCurrentUser(); 
		 String statType = request.getParameter("statType");
		 if("work_count".equals(statType)) {
		     List<EdocMarkModel> markModelList = edocMarkDefinitionManager.findEdocMarkAndSerinalDefList(user.getLoginAccount());
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
			 mav = new ModelAndView("edoc/docstat/govdoc_stat_sign");
		 }
	     
	     //验证预制数据
	     this.edocStatNewManager.checkStatInitData();
	     
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
	     mav.addObject("curYear", Integer.valueOf(curDates[0]));
	     mav.addObject("curMonth", Integer.valueOf(curDates[1]));
	     mav.addObject("curSeason", Integer.valueOf(curDates[2]));	     
	     mav.addObject("curDay", curDates[3]);
	     return mav;
	}
	
	/**
	 * 首页推送
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public ModelAndView pushStatResult(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
		edocStatManager.saveEdocStatCondition(condition);
		return null;
	}

	/**
	 * 公文统计穿透列表-导出Excel
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ModelAndView exportStatEdocList(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
		filpInfo = edocStatNewManager.getEdocVoList(filpInfo, params);
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
			if(EdocHelper.isG6Version()) {
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
	 * 公文统计结果-导出Excel
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ModelAndView exportStatResult(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
		boolean showBanwenYuewen = EdocSwitchHelper.showBanwenYuewen(AppContext.getCurrentUser().getLoginAccount());
		
		FlipInfo filpInfo = new FlipInfo();
		filpInfo = edocStatNewManager.getEdocStatVoList(filpInfo, params);
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

	public void setEdocStatNewManager(EdocStatNewManager edocStatNewManager) {
		this.edocStatNewManager = edocStatNewManager;
	}

	public void setFileToExcelManager(FileToExcelManager fileToExcelManager) {
		this.fileToExcelManager = fileToExcelManager;
	}

	public void setEdocStatManager(EdocStatManager edocStatManager) {
		this.edocStatManager = edocStatManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}

}
