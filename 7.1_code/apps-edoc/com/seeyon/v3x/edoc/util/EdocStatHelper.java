package com.seeyon.v3x.edoc.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.govdoc.helper.GovdocRoleHelper;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocStatCondition;
import com.seeyon.v3x.edoc.manager.EdocRoleHelper;
import com.seeyon.v3x.edoc.util.EdocStatEnum.EdocStatDisplayTimeTypeEnum;
import com.seeyon.v3x.edoc.webmodel.EdocStatVO;

public class EdocStatHelper {

    public static String getEdocStatConditionUrl(EdocStatCondition stat){
        
        String url = null;
        
        if(stat.getIsOld() == null || stat.getIsOld()) {
            
            int timeType = stat.getTimeType();
            String starttime = stat.getStarttime();
            String endtime = stat.getEndtime();
            StringBuilder timeParam = new StringBuilder();
            String[] startarr = null;
            String[] endarr = null;
            switch(timeType){
                case 1 :
                    timeParam.append("&yeartype-startyear=").append(starttime)
                             .append("&yeartype-endyear=").append(endtime);
                    break;
                case 2 :
                    startarr = starttime.split(" ");
                    endarr = endtime.split(" ");
                    timeParam.append("&seasontype-startyear=")
                             .append(startarr[0])
                             .append("&seasontype-endyear=")
                             .append(endarr[0])
                             .append("&seasontype-startseason=")
                             .append(startarr[1])
                             .append("&seasontype-endseason=")
                             .append(endarr[1]);
                    break;
                case 3 :
                    startarr = starttime.split(" ");
                    endarr = endtime.split(" ");
                    timeParam.append("&monthtype-startyear=")
                             .append(startarr[0])
                             .append("&monthtype-endyear=")
                             .append(endarr[0])
                             .append("&monthtype-startmonth=")
                             .append(startarr[1])
                             .append("&monthtype-endmonth=")
                             .append(endarr[1]);
                    break;
                case 4 :
                    timeParam.append("&daytype-startday=")
                             .append(starttime)
                             .append("&daytype-endday=")
                             .append(endtime);
                    break;
            }
            url = new StringBuilder("/edocStat.do?method=doStat&openType=firstPage&statisticsDimension=")
                            .append(stat.getStatisticsDimension())
                            .append("&organizationId=")
                            .append(stat.getOrganizationId())
                            .append("&timeType=")
                            .append(timeType)
                            .append("&sendContentId=")
                            .append(stat.getSendContentId())
                            .append("&workflowNodeId=")
                            .append(stat.getWorkflowNodeId())
                            .append("&processSituationId=")
                            .append(stat.getProcessSituationId())
                            .append("&sendNodeCode=")
                            .append(stat.getSendNodeCode())
                            .append("&recNodeCode=")
                            .append(stat.getRecNodeCode())
                            .append(timeParam).toString();
        } else {
            url = new StringBuilder("/edocStatNew.do?method=edocStatResult&edocType=")
                          .append(stat.getEdocType())
                          .append("&statConditionId=")
                          .append(stat.getId())
                          .toString();
        }
        return url;
    }
    
    public static String getEdocStatConditionUrlNew(EdocStatCondition stat) {
        return new StringBuilder("/edocStatNew.do?method=edocStatResult&edocType=")
                          .append(stat.getEdocType()).toString();
    }
    
    /**
	 * 获取当前人员的当前部门及子部门集合
	 * @param user
	 * @return
	 * @throws BusinessException
	 */
	public static List<Long> getCurrentUserBubDeptIdList(User user) throws BusinessException {
		return EdocStatHelper.getSelfAndSubDepartmentList(user.getDepartmentId());
	}
    
    /**
     * 获取某人所属部门及子部门
     * @param deptId
     * @return
     * @throws BusinessException
     */
    public static List<Long> getSelfAndSubDepartmentList(Long deptId) throws BusinessException {
    	OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
        List<V3xOrgDepartment> deptList = orgManager.getChildDepartments(deptId, false);
        List<Long> deptIdList = new ArrayList<Long>();
        deptIdList.add(deptId);
        if(Strings.isNotEmpty(deptList)) {
        	for(V3xOrgDepartment dept : deptList) {
        		deptIdList.add(dept.getId());
        	}
        }
        return deptIdList;
    }
    
    /**
     * 获取子部门id集
     * @param deptId
     * @return
     * @throws BusinessException
     */
    public static List<Long> getSubDepartmentList(Long deptId) throws BusinessException {
    	OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
        List<V3xOrgDepartment> deptList = orgManager.getChildDepartments(deptId, false);
        List<Long> deptIdList = new ArrayList<Long>();
        if(Strings.isNotEmpty(deptList)) {
        	for(V3xOrgDepartment dept : deptList) {
        		deptIdList.add(dept.getId());
        	}
        }
        return deptIdList;
    }
    
    /**
     * 获取某人子部门
     * @param deptId
     * @param childFlag
     * @return
     * @throws BusinessException
     */
    public static List<Long> getSubDepartmentList(Long deptId, boolean childFlag) throws BusinessException {
    	OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
        List<V3xOrgDepartment> deptList = orgManager.getChildDepartments(deptId, childFlag);
        List<Long> deptIdList = new ArrayList<Long>();
        if(Strings.isNotEmpty(deptList)) {
        	for(V3xOrgDepartment dept : deptList) {
        		deptIdList.add(dept.getId());
        	}
        }
        return deptIdList;
    }

    /**
     *  获取某人所属部门及子部门
     * @param deptId
     * @param childFlag
     * @return
     * @throws BusinessException
     */
    public static List<Long> getSelfAndSubDepartmentList(Long deptId, boolean childFlag) throws BusinessException {
    	OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
        List<V3xOrgDepartment> deptList = orgManager.getChildDepartments(deptId, childFlag);
        List<Long> deptIdList = new ArrayList<Long>();
        deptIdList.add(deptId);
        if(Strings.isNotEmpty(deptList)) {
        	for(V3xOrgDepartment dept : deptList) {
        		deptIdList.add(dept.getId());
        	}
        }
        return deptIdList;
    }
    
    /**
     *  获取某人所属部门及子部门
     * @param memberId
     * @return
     * @throws BusinessException
     */
    public static List<Long> getSelfSubDepartmentListByMemberId(Long memberId) throws BusinessException {
    	OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
    	V3xOrgMember member = orgManager.getMemberById(memberId);
        List<V3xOrgDepartment> deptList = orgManager.getChildDepartments(member.getOrgDepartmentId(), false);
        List<Long> deptIdList = new ArrayList<Long>();
        deptIdList.add(member.getOrgDepartmentId());
        if(Strings.isNotEmpty(deptList)) {
        	for(V3xOrgDepartment dept : deptList) {
        		deptIdList.add(dept.getId());
        	}
        }
        return deptIdList;
    }
    
    /**
     * 获取某人子部门
     * @param memberId
     * @return
     * @throws BusinessException
     */
    public static List<Long> getSubDepartmentListByMemberId(Long memberId) throws BusinessException {
    	OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
    	V3xOrgMember member = orgManager.getMemberById(memberId);
        List<V3xOrgDepartment> deptList = orgManager.getChildDepartments(member.getOrgDepartmentId(), false);
        List<Long> deptIdList = new ArrayList<Long>();
        if(Strings.isNotEmpty(deptList)) {
        	for(V3xOrgDepartment dept : deptList) {
        		deptIdList.add(dept.getId());
        	}
        }
        return deptIdList;
    }
    
    /**
	 * 获取统计选中范围
	 * @param params
	 * @param theRangeType
	 * @return
	 * @throws BusinessException
	 */
	public static List<Long> getSelectedDeptIdList(String rangeIds, String theRangeType) throws BusinessException {
		List<Long> selectDeptIdList = new ArrayList<Long>();
		if(Strings.isNotBlank(rangeIds)) {
			OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
			String[] ranges = rangeIds.split(",");
			for(String range : ranges) {
				String rangeType = range.split("[|]")[0];
				Long rangeId = Long.parseLong(range.split("[|]")[1]);
				if("Member".equals(rangeType) && "Member".equals(theRangeType)) {
					V3xOrgMember member = orgManager.getMemberById(rangeId);
					selectDeptIdList.add(member.getOrgDepartmentId());
				} else if("Department".equals(rangeType) && "Department".equals(theRangeType)) {
					boolean childFlag = EdocStatHelper.hasChildDeptFlag(range);
					if(!childFlag) {
						selectDeptIdList.addAll(EdocStatHelper.getSelfAndSubDepartmentList(rangeId));
					} else {
						selectDeptIdList.add(rangeId);
					}
				}
			}
		}
		return selectDeptIdList;
	}
    
	/**
	 * 获取公文的app值
	 * @param edocType
	 * @return
	 */
	public static Integer getEdocApp(int edocType) {
		int app = 19;
        if(edocType == 1) {
        	app = 20;	
        } else if(edocType == 2) {
        	app = 21;
        }
        return app;
	}
	
	/**
	 * 获取公文的subApp值
	 * @param listType
	 * @return
	 */
	public static Integer gerAffairSubApp(int listType) {
	    
	    Integer ret = ApplicationSubCategoryEnum.edocRecHandle.key();
	    
        if(listType == EdocStatEnum.EdocStatListTypeEnum.doAll.key()//总办件
                || listType == EdocStatEnum.EdocStatListTypeEnum.finished.key()//已办结
                || listType == EdocStatEnum.EdocStatListTypeEnum.wait_finished.key()//未办结
                || listType == EdocStatEnum.EdocStatListTypeEnum.done.key()//已办
                || listType == EdocStatEnum.EdocStatListTypeEnum.pending.key()//待办
                || listType == EdocStatEnum.EdocStatListTypeEnum.zcdb.key()//在办
                || listType == EdocStatEnum.EdocStatListTypeEnum.undertaker.key()//承办数
                ) {
            
            return ApplicationSubCategoryEnum.edocRecHandle.key();
            
        } else if(listType == EdocStatEnum.EdocStatListTypeEnum.sent.key()) {//已发
            return null;
        } else if(listType == EdocStatEnum.EdocStatListTypeEnum.readAll.key()//总阅件
                || listType == EdocStatEnum.EdocStatListTypeEnum.readed.key()//已阅
                || listType == EdocStatEnum.EdocStatListTypeEnum.reading.key()//待阅
                ){
            return ApplicationSubCategoryEnum.edocRecRead.key();
        }
        return ret;
    }
	
	/**
	 * 筛选统计条件-公文的流程状态
	 * @param listType
	 * @return
	 */
    public static List<Integer> getSummaryStateList(int listType) {
        List<Integer> summaryStateList = new ArrayList<Integer>();
        if (listType == EdocStatEnum.EdocStatListTypeEnum.doAll.key()// 总办件
                || listType == EdocStatEnum.EdocStatListTypeEnum.sent.key()// 已发
                || listType == EdocStatEnum.EdocStatListTypeEnum.done.key()// 已办
                || listType == EdocStatEnum.EdocStatListTypeEnum.pending.key()// 待办
                || listType == EdocStatEnum.EdocStatListTypeEnum.zcdb.key()// 在办
                || listType == EdocStatEnum.EdocStatListTypeEnum.readAll.key()// 总阅件
                || listType == EdocStatEnum.EdocStatListTypeEnum.readed.key()// 已阅
                || listType == EdocStatEnum.EdocStatListTypeEnum.reading.key()// 待阅
        ) {
            
            summaryStateList.add(EdocConstant.flowState.run.ordinal());
            summaryStateList.add(EdocConstant.flowState.finish.ordinal());
            summaryStateList.add(EdocConstant.flowState.terminate.ordinal());
            
        } else if (listType == EdocStatEnum.EdocStatListTypeEnum.finished.key()) {// 已办结
            summaryStateList.add(EdocConstant.flowState.finish.ordinal());
            summaryStateList.add(EdocConstant.flowState.terminate.ordinal());
        } else if (listType == EdocStatEnum.EdocStatListTypeEnum.wait_finished.key()) {// 未办结
            summaryStateList.add(EdocConstant.flowState.run.ordinal());
        } else if (listType == EdocStatEnum.EdocStatListTypeEnum.undertaker.key()) {// 承办数(承办不统计终止的数据)
            summaryStateList.add(EdocConstant.flowState.run.ordinal());
            summaryStateList.add(EdocConstant.flowState.finish.ordinal());
        }
        if (Strings.isNotEmpty(summaryStateList)) {
            summaryStateList.add(112);// 一个特殊数据的存在，表示公文归档过
        }
        return summaryStateList;
    }
	
	/**
	 * 筛选统计条件-公文的处理状态
	 * @param listType
	 * @return
	 */
	public static List<Integer> gerAffairStateList(int listType) {
		List<Integer> stateList = new ArrayList<Integer>();
		if(listType == EdocStatEnum.EdocStatListTypeEnum.doAll.key()//总办件
				|| listType == EdocStatEnum.EdocStatListTypeEnum.finished.key()//已办结
				|| listType == EdocStatEnum.EdocStatListTypeEnum.readAll.key()) {//总阅件 
			stateList.add(StateEnum.col_pending.key());
			stateList.add(StateEnum.col_done.key());
			stateList.add(StateEnum.col_stepStop.key());
			//stateList.add(StateEnum.col_competeOver.key());
        } else if(listType == EdocStatEnum.EdocStatListTypeEnum.wait_finished.key()) {//未办结
        	stateList.add(StateEnum.col_pending.key());
			stateList.add(StateEnum.col_done.key());
			//stateList.add(StateEnum.col_competeOver.key());
        } else if(listType == EdocStatEnum.EdocStatListTypeEnum.sent.key()) {//已发
        	stateList.add(StateEnum.col_sent.key());
        } else if(listType == EdocStatEnum.EdocStatListTypeEnum.pending.key()//待办
        		|| listType == EdocStatEnum.EdocStatListTypeEnum.zcdb.key()//在办
        		|| listType == EdocStatEnum.EdocStatListTypeEnum.reading.key()) {//待阅
        	stateList.add(StateEnum.col_pending.key());
        } else if(listType == EdocStatEnum.EdocStatListTypeEnum.readed.key()//已阅
        		|| listType == EdocStatEnum.EdocStatListTypeEnum.undertaker.key()//承办数
        		|| listType == EdocStatEnum.EdocStatListTypeEnum.done.key()) {//已办
        	stateList.add(StateEnum.col_done.key());
        } 
		return stateList;
	}
	
	/**
	 * 筛选统计条件-公文的处理子状态
	 * @param listType
	 * @return
	 */
	public static List<Integer> gerAffairSubStateList(int listType) {
		List<Integer> subStateList = new ArrayList<Integer>();
		if(listType == EdocStatEnum.EdocStatListTypeEnum.doAll.key()) {//总办件
        } else if(listType == EdocStatEnum.EdocStatListTypeEnum.finished.key()) {//已办结
        } else if(listType == EdocStatEnum.EdocStatListTypeEnum.wait_finished.key()) {//未办结
        } else if(listType == EdocStatEnum.EdocStatListTypeEnum.sent.key()) {//已发
        } else if(listType == EdocStatEnum.EdocStatListTypeEnum.done.key()) {//已办
        } else if(listType == EdocStatEnum.EdocStatListTypeEnum.pending.key()) {//待办
        	//需求变更，将在办的数据合并到待办中
        	/*subStateList.add(SubStateEnum.col_pending_unRead.key());
	        subStateList.add(SubStateEnum.col_pending_read.key());
	        subStateList.add(SubStateEnum.col_pending_assign.key());
	        subStateList.add(SubStateEnum.col_pending_specialBack.key());
	        subStateList.add(SubStateEnum.col_pending_specialBacked.key());
	        subStateList.add(SubStateEnum.col_pending_specialBackCenter.key());
	        subStateList.add(SubStateEnum.col_pending_specialBackToSenderCancel.key());
	        subStateList.add(SubStateEnum.col_pending_specialBack.key());
	        subStateList.add(SubStateEnum.col_pending_ZCDB.key());
        	subStateList.add(SubStateEnum.col_pending_specialBackToSenderReGo.key());*/
        } else if(listType == EdocStatEnum.EdocStatListTypeEnum.zcdb.key()) {//在办
        	subStateList.add(SubStateEnum.col_pending_ZCDB.key());
        	subStateList.add(SubStateEnum.col_pending_specialBackToSenderReGo.key());
        } else if(listType == EdocStatEnum.EdocStatListTypeEnum.readAll.key()) {//总阅件        	
        } else if(listType == EdocStatEnum.EdocStatListTypeEnum.readed.key()) {//已阅
        } else if(listType == EdocStatEnum.EdocStatListTypeEnum.reading.key()) {//待阅
        	subStateList.add(SubStateEnum.col_pending_unRead.key());
	        subStateList.add(SubStateEnum.col_pending_read.key());
	        subStateList.add(SubStateEnum.col_pending_assign.key());
	        subStateList.add(SubStateEnum.col_pending_specialBack.key());
	        subStateList.add(SubStateEnum.col_pending_specialBacked.key());
	        subStateList.add(SubStateEnum.col_pending_specialBackCenter.key());
	        subStateList.add(SubStateEnum.col_pending_specialBackToSenderCancel.key());
	        subStateList.add(SubStateEnum.col_pending_specialBackToSenderReGo.key());
	        subStateList.add(SubStateEnum.col_pending_ZCDB.key());
        	subStateList.add(SubStateEnum.col_pending_specialBack.key());
        } else if(listType == EdocStatEnum.EdocStatListTypeEnum.undertaker.key()) {//承办数
        }
		return subStateList;
	}
	

	/**
	 * 获取公文的subApp值
	 * @param listType
	 * @return
	 */
	public static Integer getResultAffairSubApp(int resultType) {
		if(resultType == EdocStatEnum.EdocStatResultTypeEnum.readAll.key()) {//总办件
        	return ApplicationSubCategoryEnum.edocRecRead.key();
        } else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.pendingAndDone.key()) {//待办/已办
        	return ApplicationSubCategoryEnum.edocRecHandle.key();
        } else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.readingAndReaded.key()) {//待阅/已阅
        	return ApplicationSubCategoryEnum.edocRecRead.key();
        }
		return ApplicationSubCategoryEnum.edocRecHandle.key();
	}
	
	/**
	 * 筛选统计条件-公文的处理状态
	 * @param listType
	 * @return
	 */
	public static List<Integer> getResultAffairStateList(int resultType) {
		List<Integer> stateList = new ArrayList<Integer>();
		if(resultType == EdocStatEnum.EdocStatResultTypeEnum.zcdb.key()//总办件
				|| resultType == EdocStatEnum.EdocStatResultTypeEnum.pending.key()//待办
				|| resultType == EdocStatEnum.EdocStatResultTypeEnum.reading.key()) {//待阅
			stateList.add(StateEnum.col_pending.key());
        } else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.undertaker.key()//总办件
        		|| resultType == EdocStatEnum.EdocStatResultTypeEnum.done.key()//总办件
        		|| resultType == EdocStatEnum.EdocStatResultTypeEnum.readed.key()) {//已阅
			stateList.add(StateEnum.col_done.key());
        } else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.sent.key()) {//已发
        	stateList.add(StateEnum.col_sent.key());
        } else {
        	stateList.add(StateEnum.col_pending.key());
			stateList.add(StateEnum.col_done.key());
			stateList.add(StateEnum.col_stepStop.key());
        }
		return stateList;
	}
	
	/**
	 * 筛选统计条件-公文的处理状态
	 * @param listType
	 * @return
	 */
    public static Integer getResultAffairState(int resultType) {
        Integer ret = StateEnum.col_done.key();
        if (resultType == EdocStatEnum.EdocStatResultTypeEnum.zcdb.key()// 总办件
                || resultType == EdocStatEnum.EdocStatResultTypeEnum.pending.key()// 待办
                || resultType == EdocStatEnum.EdocStatResultTypeEnum.reading.key()// 待阅
        ) {

            ret = StateEnum.col_pending.key();

        } else if (resultType == EdocStatEnum.EdocStatResultTypeEnum.undertaker.key()// 总办件
                || resultType == EdocStatEnum.EdocStatResultTypeEnum.done.key()// 总办件
                || resultType == EdocStatEnum.EdocStatResultTypeEnum.readed.key()// 已阅
        ) {
            ret = StateEnum.col_done.key();
        } else if (resultType == EdocStatEnum.EdocStatResultTypeEnum.sent.key()) {// 已发
            ret = StateEnum.col_sent.key();
        }

        return ret;
    }
	
	public static List<Integer> getResultAffairSubStateList(int resultType) {
		List<Integer> subStateList = new ArrayList<Integer>();
		if(resultType == EdocStatEnum.EdocStatResultTypeEnum.zcdb.key()) {//在办
			subStateList.add(SubStateEnum.col_pending_ZCDB.key());
        	subStateList.add(SubStateEnum.col_pending_specialBackToSenderReGo.key());
        } 
		return subStateList;
	}
	
	
	/**
	 * 设置年/季/月/日的key值
	 * @param startTimeC
	 * @param displayTimeType
	 * @return
	 */
    public static String getTimeDisplayId(Calendar startTimeC, int displayTimeType) {
        int year = startTimeC.get(Calendar.YEAR);
        int month = startTimeC.get(Calendar.MONTH) + 1;
        int day = startTimeC.get(Calendar.DATE);
        
        StringBuilder ret = new StringBuilder(displayTimeType).append("-").append(year);
        
        if (displayTimeType == EdocStatDisplayTimeTypeEnum.year.key()) {// 年
        } else if (displayTimeType == EdocStatDisplayTimeTypeEnum.quarter.key()) {// 季
            ret.append("-").append(getQuarter(month));
        } else if (displayTimeType == EdocStatDisplayTimeTypeEnum.month.key()) {// 月
            ret.append("-").append(getString2Gigit(month));
        } else {// 日
            ret.append("-").append(getString2Gigit(month)).append("-").append(getString2Gigit(day));
        }
        
        return ret.toString();
    }
	
	/**
	 * 
	 * @param displayTimeType
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public static String getDisplayTimeTypeKey(int displayTimeType, int year, int month, int day) {
		if(displayTimeType == EdocStatDisplayTimeTypeEnum.year.key()) {//年
			return displayTimeType + "-" + year;
		} else if(displayTimeType == EdocStatDisplayTimeTypeEnum.quarter.key()) {//季
			return displayTimeType + "-" +year + "-" + getQuarter(month);
		} else if(displayTimeType == EdocStatDisplayTimeTypeEnum.month.key()) {//月
			return displayTimeType + "-" + year + "-" + getString2Gigit(month);
		} else {//日
			return displayTimeType + "-" + year + "-" + getString2Gigit(month) + "-" + getString2Gigit(day);
		}
	}
	
	public static String getString2Gigit(int m) {
		String month = String.valueOf(m);
		if(m < 10) {
			month = "0" + m;
		}
		return month;
	}
	
	/**
	 * 通过月份获取季节
	 * @param month
	 * @return
	 */
	public static Integer getQuarter(int month)  {
		int quarter = 1;
		if(month==1||month==2||month==3) {
			quarter = 1;
		} else if(month==4||month==5||month==6) {
			quarter = 2;
		} else if(month==7||month==8||month==9) {
			quarter = 3;
		} else {
			quarter = 4;
		}
		return quarter;
	}
	
	/**
	 * 获取某季度的第一个月份
	 * @param quarter
	 * @return
	 */
	public static String getQuarterFirstMonth(int quarter)  {
		String month = "01";
		if(quarter==1) {
			month = "01";
		} else if(quarter==2) {
			month = "04";
		} else if(quarter==3) {
			month = "07";
		} else {
			month = "10";
		}
		return month;
	}
	
	/**
	 * 获取某季度的最后月份
	 * @param quarter
	 * @return
	 */
	public static String getQuarterLastMonth(int quarter)  {
		String month = "01";
		if(quarter==1) {
			month = "03";
		} else if(quarter==2) {
			month = "06";
		} else if(quarter==3) {
			month = "09";
		} else {
			month = "12";
		}
		return month;
	}
	
	/**
	 * 获取年/季/月/日的开始结束时间
	 * @param displayTimeType
	 * @param displayTimeView
	 * @return
	 */
	public static Date[] getStartAndEndTime(int displayTimeType, String timeDisplayId) {
		timeDisplayId = timeDisplayId.substring(timeDisplayId.indexOf("-") + 1, timeDisplayId.length());
		Calendar startTimeC = Calendar.getInstance();
    	Calendar startTimeE = Calendar.getInstance();
    	if(displayTimeType == EdocStatDisplayTimeTypeEnum.year.key()) {//年        		
    		startTimeC.setTime(Datetimes.parseDatetime(timeDisplayId+"-01-01 00:00:00"));
    		startTimeE.setTime(Datetimes.parseDatetime(timeDisplayId+"-12-31 23:59:59"));
    	} else if(displayTimeType == EdocStatDisplayTimeTypeEnum.quarter.key()) {//季
    		int year = Integer.parseInt(timeDisplayId.split("-")[0]);
    		String monthC = getQuarterFirstMonth(Integer.parseInt(timeDisplayId.split("-")[1]));
    		String monthE = getQuarterLastMonth(Integer.parseInt(timeDisplayId.split("-")[1]));
    		startTimeC.setTime(Datetimes.parseDatetime(year+"-"+monthC+"-01 00:00:00"));
    		startTimeE.setTime(Datetimes.parseDatetime(year+"-"+monthE+"-31 23:59:59"));
    	} else if(displayTimeType == EdocStatDisplayTimeTypeEnum.month.key()) {//月
    		int year = Integer.parseInt(timeDisplayId.split("-")[0]);
    		int month = Integer.parseInt(timeDisplayId.split("-")[1]);
    		startTimeC.setTime(Datetimes.parseDatetime(year+"-"+month+"-01 00:00:00"));
    		startTimeE.setTime(Datetimes.parseDatetime(year+"-"+month+"-31 23:59:59"));
    	} else {//日
    		startTimeC.setTime(Datetimes.parseDatetime(timeDisplayId+" 00:00:00"));
    		startTimeE.setTime(Datetimes.parseDatetime(timeDisplayId+" 23:59:59"));
    	}
    	return new Date[] {startTimeC.getTime(), startTimeE.getTime()};
	}


	/**
	 * 获取统计选中范围
	 * @param params
	 * @param theRangeType
	 * @return
	 * @throws BusinessException
	 */
	public static List<Long> getRangeIdList(Map<String, String> params, String theRangeType) throws BusinessException {
		String rangeIds = params.get("rangeIds");
		List<Long> deptIdList = new ArrayList<Long>();
		List<Long> memberIdList = new ArrayList<Long>();
		if(Strings.isNotEmpty(rangeIds)) {
			String[] ranges = rangeIds.split(",");
			for(int i=0; i<ranges.length; i++) {
				String range = ranges[i];
				String rangeType  = range.split("[|]")[0];
				Long rangeId  = Long.parseLong(range.split("[|]")[1]);
				if("Department".equals(rangeType)) {
					boolean childFlag = EdocStatHelper.hasChildDeptFlag(range);
					if(!childFlag) {
						deptIdList.addAll(EdocStatHelper.getSelfAndSubDepartmentList(rangeId));
					} else {
						deptIdList.add(rangeId);
					}
 				} else if("Member".equals(rangeType)) {
					memberIdList.add(rangeId);
				}
			}
		}
		if("Department".equals(theRangeType)) {
			return deptIdList;
		} else {
			return memberIdList;
		}
	}
	
	/**
	 * 是否有子部门
	 * @param range
	 * @return true无 false有
	 */
	public static boolean hasChildDeptFlag(String range) {
		boolean childFlag = Boolean.FALSE;
		if(range.split("[|]").length > 2) {
			childFlag = "1".equals(range.split("[|]")[2]);
		}
		return childFlag;
	}
	
	/**
	 * 获取用户的具有交换权限的所有部门
	 * @Date        : 2015年2月5日上午10:02:14
	 * @param userId
	 * @return
	 * @throws BusinessException
	 */
	public static String getUserAllDeptIds(Long userId) throws BusinessException {
		StringBuilder allDeptIds  = new StringBuilder();
		OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
		List<V3xOrgDepartment> allUserDeptList = orgManager.getDepartmentsByUser(userId);
		for(V3xOrgDepartment dept : allUserDeptList) {
			List<V3xOrgDepartment> subDeptList = orgManager.getChildDepartments(dept.getId(), false);
			if(Strings.isNotEmpty(subDeptList)) {
				for(V3xOrgDepartment subDept : subDeptList) {
					if(GovdocRoleHelper.isDepartmentExchange(userId, dept.getId(), dept.getOrgAccountId())) {
						allDeptIds.append(subDept.getId());
						allDeptIds.append(",");
					}
				}
			}
		}
		return allDeptIds.toString();
	}
	
	public static List<V3xOrgDepartment> getCurrentUserAllDeptList() throws BusinessException {
		User user = AppContext.getCurrentUser();
		OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
		List<V3xOrgDepartment> userAllDeptList = orgManager.getDepartmentsByUser(user.getId());
		return userAllDeptList;
	}
	
	public static List<Long> getCurrentUserAllDeptIdList() throws BusinessException {
		List<Long> userAllDeptIdList = new ArrayList<Long>();
		User user = AppContext.getCurrentUser();
		OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
		List<V3xOrgDepartment> userAllDeptList = orgManager.getDepartmentsByUser(user.getId());
		if(Strings.isNotEmpty(userAllDeptList)) {
			for(V3xOrgDepartment dept : userAllDeptList) {
				List<V3xOrgDepartment> subDeptList = orgManager.getChildDepartments(dept.getId(), false);
				if(Strings.isNotEmpty(subDeptList)) {
					for(V3xOrgDepartment subDept : subDeptList) {
						userAllDeptIdList.add(subDept.getId());
					}
				}
				userAllDeptIdList.add(dept.getId());
			}
		}
		return userAllDeptIdList;
	}
	
	public static List<Long> getCurrentUserAllExchangeDeptIdList() throws BusinessException {
		List<Long> userAllDeptIdList = new ArrayList<Long>();
		User user = AppContext.getCurrentUser();
		OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
		List<V3xOrgDepartment> userAllDeptList = orgManager.getDepartmentsByUser(user.getId());
		if(Strings.isNotEmpty(userAllDeptList)) {
			for(V3xOrgDepartment dept : userAllDeptList) {
				if(GovdocRoleHelper.isDepartmentExchange(user.getId(), dept.getId(), user.getLoginAccount())) {
					List<V3xOrgDepartment> subDeptList = orgManager.getChildDepartments(dept.getId(), false);
					if(Strings.isNotEmpty(subDeptList)) {
						for(V3xOrgDepartment subDept : subDeptList) {
							userAllDeptIdList.add(subDept.getId());
						}
					}
					userAllDeptIdList.add(dept.getId());
				}
			}
		}
		return userAllDeptIdList;
	}
	
	/**
	 * 
	 * @param statVo
	 * @param memberDeptId
	 * @return
	 */
	public static boolean isSameOrDeptFlag(EdocStatVO statVo, Long memberDeptId) {
		boolean isSameDeptFlag = memberDeptId!=null && statVo.getDisplayId().equals(String.valueOf(memberDeptId));
		boolean isSubDeptFlag = memberDeptId!=null && statVo.getSubDeptIdList()!=null && statVo.getSubDeptIdList().contains(memberDeptId);
		return isSameDeptFlag || isSubDeptFlag;
	}
	
	/**
	 * 
	 * @param selectDeptIdList
	 * @param deptId
	 * @return
	 */
	public static boolean isContainsSelectDept(List<Long> selectDeptIdList, Long deptId) {
		return Strings.isNotEmpty(selectDeptIdList) && selectDeptIdList.contains(deptId);
	}
	
	/**
	 * 
	 * @param subDeptIdList
	 * @param deptId
	 * @return
	 */
	public static boolean isContainsSelfOrSubDept(List<Long> subDeptIdList, Long deptId) {
		return Strings.isNotEmpty(subDeptIdList) && subDeptIdList.contains(deptId);
	}
	
	/**
     * List去重
     * @Author      : xuqiangwei
     * @Date        : 2015年2月4日下午3:15:34
     * @param list
     */
    public static <T> void removeRepeatItem(List<T> list){
        
        if(Strings.isNotEmpty(list)){
            Set<T> tempSet = new HashSet<T>();
            List<T> toDelete = new ArrayList<T>();
            for(T item : list){
                
                if(tempSet.contains(item)){
                    toDelete.add(item);
                }else {
                    tempSet.add(item);
                }
            }
            for(T item : toDelete){
                list.remove(item);
            }
        }
    }
    
    /**
	 * 获取公文的subapp值
	 * 新发的表单公文在ctp_affair中的app是4（公文），
	 * 且sub_app是对应ApplicationSubCategoryEnum中的枚举
	 * @param edocType 公文类型 0发文 1收文 2
	 * @return
	 */
	public static Integer getEdocSubApp(int edocType) {
		int subApp = com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum.edoc_fawen.key();
		if(edocType == 1) {
			subApp = com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum.edoc_shouwen.key();
		} else if(edocType == 2) {
			subApp = com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum.edoc_qianbao.key();
		}
		return subApp;
	}
	public static Integer getOldEdocSubApp(int edocType) {
		int subApp = com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum.old_edocSend.key();
		if(edocType == 1) {
			subApp = com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum.old_edocRec.key();
		} else if(edocType == 2) {
			subApp = com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum.old_edocSign.key();
		}
		return subApp;
	}
}
