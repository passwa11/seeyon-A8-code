package com.seeyon.v3x.edoc.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.govdoc.po.EdocStatSet;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.excel.DataRecord;
import com.seeyon.ctp.common.excel.DataRow;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgUnit;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.manager.EdocMarkDefinitionManager;
import com.seeyon.v3x.edoc.quartz.EdocNodeOverTimeManagerImpl;
import com.seeyon.v3x.edoc.util.GovdocStatEnum.GovdocStatTypeEnum;
import com.seeyon.v3x.edoc.webmodel.EdocWorkStatDetialVO;
import com.seeyon.v3x.edoc.webmodel.GovdocStatConditionVO;
import com.seeyon.v3x.edoc.webmodel.GovdocStatDisplayVO;
import com.seeyon.v3x.edoc.webmodel.GovdocStatSetVO;
import com.seeyon.v3x.worktimeset.exception.WorkTimeSetExecption;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

public class GovdocStatUtil {
	
	private static EdocMarkDefinitionManager edocMarkDefinitionManager = (EdocMarkDefinitionManager)AppContext.getBean("edocMarkDefinitionManager");
	private final static Log LOGGER = LogFactory.getLog(GovdocStatUtil.class);
	private static OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
	
	private static Integer workTime = 0;
    private static int year;
    private static WorkTimeManager workTimeManager = (WorkTimeManager)AppContext.getBean("workTimeManager");
    
    private static PermissionManager permissionManager = (PermissionManager)AppContext.getBean("permissionManager");
    
    public static void setEdocStatOverTime(EdocWorkStatDetialVO listVo, List<CtpAffair> affairList, 
			Long orgAccountId, String displayType, String displayId, Date nowDate) throws BusinessException{
		Long expendTime = 0L;
		Long maxTime = 0L;
        Long sumTime = 0L;
        CtpAffair maxAffair = null;
		for(CtpAffair ctpAffair : affairList) {
	        if(ctpAffair.isCoverTime()){
	        	//Long addOver = ctpAffair.getDeadlineDate()*60*1000;
	        	//Date deadDate= new Date(ctpAffair.getExpectedProcessTime().getReceiveTime().getTime()+addOver);
				if(ctpAffair.getCompleteTime() == null) {
					expendTime = workTimeManager.getDealWithTimeValue(ctpAffair.getExpectedProcessTime(), nowDate, orgAccountId);
				} else {
					expendTime = workTimeManager.getDealWithTimeValue(ctpAffair.getExpectedProcessTime(), ctpAffair.getCompleteTime(), orgAccountId);
				}
				if(expendTime > maxTime) {
					maxTime = expendTime;
					maxAffair = ctpAffair;
				}
				sumTime +=expendTime;
	        }
				
		}
		sumTime = (sumTime/1000)/60;
		listVo.setDeadlineOverView(showDate(Integer.parseInt(sumTime.toString()), true));
		
		String appCategor = "";
		if(maxAffair != null){
			if(maxAffair.getApp().intValue() == 4) {
				if(maxAffair.getSubApp() == 1){
					appCategor = EnumNameEnum.edoc_new_send_permission_policy.name();
				} else if(maxAffair.getSubApp() == 2) {
					appCategor = EnumNameEnum.edoc_new_rec_permission_policy.name();
				} else if(maxAffair.getSubApp() == 3) {
					appCategor = EnumNameEnum.edoc_new_qianbao_permission_policy.name();
				}else if(maxAffair.getApp().intValue() == 19) {
					appCategor = EnumNameEnum.edoc_send_permission_policy.name();
				} else if(maxAffair.getApp().intValue() == 20) {
					appCategor = EnumNameEnum.edoc_rec_permission_policy.name();
				} else if(maxAffair.getApp().intValue() == 21) {
					appCategor = EnumNameEnum.edoc_qianbao_permission_policy.name();
				}
			} else { 
				if(maxAffair.getApp().intValue() == 19) {
					appCategor = EnumNameEnum.edoc_send_permission_policy.name();
				} else if(maxAffair.getApp().intValue() == 20) {
					appCategor = EnumNameEnum.edoc_rec_permission_policy.name();
				} else if(maxAffair.getApp().intValue() == 21) {
					appCategor = EnumNameEnum.edoc_qianbao_permission_policy.name();
				}
			}
			
			Permission permission = permissionManager.getPermission(appCategor, maxAffair.getNodePolicy(), orgAccountId);
			if (permission != null) {
				listVo.setMaxNodePolicy(permission.getLabel());
			}else if(Strings.isNotBlank(maxAffair.getNodePolicy())){
				listVo.setMaxNodePolicy(maxAffair.getNodePolicy());
			}
			
			V3xOrgMember cmOver = orgManager.getMemberById(maxAffair.getMemberId());
			listVo.setMaxOverPerson(cmOver.getName());
			
			V3xOrgDepartment overDept = orgManager.getDepartmentById(cmOver.getOrgDepartmentId());
			listVo.setOperDept(overDept.getName());
		} else {
			listVo.setMaxNodePolicy("");
			listVo.setMaxOverPerson("");
			listVo.setOperDept("");
		}
	}
    
    /**
     * 将分钟数按当前工作时间转化为按天表示的时间。
     * 例如 1天7小时2分。
     */
    public static  String showDate(Integer minutes, boolean isWork) {
        if(minutes == null || minutes == 0) 
            return "－";
        int dayH = 24*60;
        if(isWork) {
            Calendar cal = Calendar.getInstance();
            int y = cal.get(Calendar.YEAR);
            if(year != y || workTime.intValue() == 0 ){ //需要取工作时间
                workTime = getCurrentYearWorkTime(workTimeManager);
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
    
    /**
	 * 
	 * @param deptList
	 * @return
	 */
	public static List<V3xOrgDepartment> deptToSort(List<V3xOrgDepartment> deptList) {
		if(Strings.isNotEmpty(deptList)) {
			 Collections.sort(deptList, new Comparator<V3xOrgDepartment>(){
	            public int compare(V3xOrgDepartment arg0, V3xOrgDepartment arg1) {
	                return arg0.getSortId().compareTo(arg1.getSortId());
	            }
	        });
		}
		return deptList;
	}
    
    public static int getCurrentYearWorkTime(WorkTimeManager workTimeManager) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int t = 0;
        try {
            t = workTimeManager.getEachDayWorkTime(year, AppContext.getCurrentUser().getLoginAccount());
        } catch (WorkTimeSetExecption e) {
            LOGGER.error(e);
        }
        return t;
    }
    
    /**
     * 将分钟数按工作日转化为天、小时、分
     * @param minutes
     * @param isWork
     * @return
     */
    public static long getWorkTimeDay(Integer minutes, boolean isWork) {
        if(minutes == null || minutes == 0) 
            return 0;
        int dayH = 24*60;
        if(isWork) {
            Calendar cal = Calendar.getInstance();
            int y = cal.get(Calendar.YEAR);
            if(year != y || workTime.intValue() == 0 ){ //需要取工作时间
                workTime = getCurrentYearWorkTime(workTimeManager);
                year = y;
            }
            if(workTime == null || workTime.intValue() == 0){
            	return 0;
            }
            dayH = workTime;
        }
        long m = minutes.longValue();
        long day = m/dayH;
        long d1 = m%dayH;
        long hour = d1/60;
        long minute = d1%60;
        //若1天多，则算2天
        if(m>0 || d1>0 || hour>0 || minute>0) {
        	return day + 1;
        }
        return day;
    }
	
	/**
	 * 
	 * @param conditionVo
	 * @param statVoList
	 * @return
	 */
	public static DataRecord getDataToExcport(GovdocStatConditionVO conditionVo, List<GovdocStatDisplayVO> statVoList) {
		int size = 0; 
		if (statVoList == null || CollectionUtils.isEmpty(statVoList)) {
			size = 1;
		}else {
			size = statVoList.size();
		}
		DataRow[] datarow = new DataRow[size];
		DataRecord dr = new DataRecord();
		/**************** 工作统计导出 ******************/
		if("work_count".equals(conditionVo.getStatType())) {
			if(Strings.isNotEmpty(statVoList)) {
				int row = 0;
				for(GovdocStatDisplayVO listVo : statVoList) {
					 datarow[row] = new DataRow();			
					 datarow[row].addDataCell(listVo.getDisplayName(), 1);//单位名称
					 if(conditionVo.getStatSetVo().isShowSendCount()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getSendCount()),1);//发文总数 
					 }
					 /*if(conditionVo.getStatSetVo().isShowFontSize()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getFontSize()),1);//发文字数 
					 }*/
					 if(conditionVo.getStatSetVo().isShowSendDCount()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getSendDoneCount()),1);//发文已办结
					 }
					 if(conditionVo.getStatSetVo().isShowSendPCount()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getSendPendingCount()),1);//发文办理中
					 }
					 if(conditionVo.getStatSetVo().isShowSendDPer()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getSendDonePer()),1);//发文办结率
					 }
					 if(conditionVo.getStatSetVo().isShowSendOCount()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getSendOverCount()),1);//发文超期件数
					 }
					 if(conditionVo.getStatSetVo().isShowSendOper()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getSendOverPer()),1);//发文超期率
					 }
					 if(conditionVo.getStatSetVo().isShowRecCount()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getRecCount()),1);//收文数
					 }
					 if(conditionVo.getStatSetVo().isShowRecDCount()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getRecDoneCount()),1);//收文已办结
					 }
					 if(conditionVo.getStatSetVo().isShowRecPCount()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getRecPendingCount()),1);//收文办理中
					 }
					 if(conditionVo.getStatSetVo().isShowRecDper()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getRecDonePer()),1);//收文办结率
					 }
					 if(conditionVo.getStatSetVo().isShowRecOCount()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getRecOverCount()),1);//收文超期数
					 }
					 if(conditionVo.getStatSetVo().isShowRecOper()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getRecOverPer()),1);//收文超期率
					 }
					 if(conditionVo.getStatSetVo().isShowAllCount()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getAllCount()),1);//总计
					 }
					 if(conditionVo.getStatSetVo().isShowAllDCount()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getAllDoneCount()),1);//已办结
					 }
					 if(conditionVo.getStatSetVo().isShowAllPCount()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getAllPendingCount()),1);//办理中
					 }
					 if(conditionVo.getStatSetVo().isShowAllDPer()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getAllDonePer()),1);//办结率
					 }
					 if(conditionVo.getStatSetVo().isShowAllOCount()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getAllOverCount()),1);//超期件数
					 }
					 if(conditionVo.getStatSetVo().isShowAllOper()){
						 datarow[row].addDataCell(String.valueOf(listVo.getAllOverPer()),1);//超期率
					 }
					 row++;
				}
				dr.addDataRow(datarow);
			}
			
			int row = 0;
			String[] columnName = null;
			int totalR = 1;
			if (conditionVo.getStatSetVo()!=null) {
				totalR = conditionVo.getStatSetVo().getTotalTdSize()+conditionVo.getStatSetVo().getRecTdSize()+conditionVo.getStatSetVo().getSendTdSize()+1;
			}
			columnName = new String[totalR];
			columnName[row++] = conditionVo.getStatName();//单位/人员
			if (conditionVo.getStatSetVo() != null) {
				if(conditionVo.getStatSetVo().isShowSendCount()) {
					columnName[row++] = "发文数";//发文数
				}
//				if(conditionVo.getStatSetVo().isShowFontSize()) {
//					columnName[row++] = "发文字数";//字数
//				}
				if(conditionVo.getStatSetVo().isShowSendDCount()) {
					columnName[row++] = "发文已办结";//已办结
				}
				if(conditionVo.getStatSetVo().isShowSendPCount()) {
					columnName[row++] = "发文办理中";//办理中
				}
				if(conditionVo.getStatSetVo().isShowSendDPer()) {
					columnName[row++] = "发文办结率";//办结率
				}
				if(conditionVo.getStatSetVo().isShowSendOCount()) {
					columnName[row++] = "发文超期件数";//超期件数
				}
				if(conditionVo.getStatSetVo().isShowSendOper()) {
					columnName[row++] = "发文超期率";//超期率
				}
				if(conditionVo.getStatSetVo().isShowRecCount()) {
					columnName[row++] = "收文数";//收文数
				}
				if(conditionVo.getStatSetVo().isShowRecDCount()) {
					columnName[row++] = "收文已办结";//已办结
				}
				if(conditionVo.getStatSetVo().isShowRecPCount()) {
					columnName[row++] = "收文办理中";//办理中
				}
				if(conditionVo.getStatSetVo().isShowRecDper()) {
					columnName[row++] = "收文办结率";//办结率
				}
				if(conditionVo.getStatSetVo().isShowRecOCount()) {
					columnName[row++] = "收文超期件数";//超期件数
				}
				if(conditionVo.getStatSetVo().isShowRecOper()) {
					columnName[row++] = "收文超期率";//超期率
				}
				if(conditionVo.getStatSetVo().isShowAllCount()) {
					columnName[row++] = "总计";//总计
				}
				if(conditionVo.getStatSetVo().isShowAllDCount()) {
					columnName[row++] = "总计已办结";//已办结
				}
				if(conditionVo.getStatSetVo().isShowAllPCount()) {
					columnName[row++] = "总计办理中";//办理中
				}
				if(conditionVo.getStatSetVo().isShowAllDPer()) {
					columnName[row++] = "总计办结率";//办结率
				}
				if(conditionVo.getStatSetVo().isShowAllOCount()) {
					columnName[row++] = "总计超期件数";//超期件数
				}
				if(conditionVo.getStatSetVo().isShowAllOper()) {
					columnName[row++] = "总计超期率";//超期率
				}
			}
			dr.setColumnName(columnName);
		} 
		/**************** 签收统计导出 ******************/
		else {
			if(Strings.isNotEmpty(statVoList)) {
				int row = 0;
				for(GovdocStatDisplayVO listVo : statVoList) {
					 datarow[row] = new DataRow();			
					 datarow[row].addDataCell(listVo.getDisplayName(), 1);//单位名称
					 datarow[row].addDataCell(String.valueOf(listVo.getAllNum()),1);//总数 
					 if(conditionVo.getStatSetVo().isSfShowTwo()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getTwoSign()),1);//2个工作日内接收总数
						 datarow[row].addDataCell(listVo.getTwoSignPer(),1);//2个工作日内接收百分比
					 }
					 if(conditionVo.getStatSetVo().isSfShowThree()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getThreeSign()),1);//3至5个工作日内接收数量
						 datarow[row].addDataCell(listVo.getThreeSignPer(),1);//3至5个工作日内接收百分比
					 }
					 if(conditionVo.getStatSetVo().isSfShowFive()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getFiveSign()),1);//5个工作日后接收数量
						 datarow[row].addDataCell(listVo.getFiveSignPer(),1);//5个工作日后接收百分比
					 }
					 if(conditionVo.getStatSetVo().isSfShowNoRec()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getNoRecSignNum()),1);//仍未签收数量 
						 datarow[row].addDataCell(listVo.getNoRecSignNumPer(),1);//仍未签收百分比
					 }
					 if(conditionVo.getStatSetVo().isSfShowback()) {
						 datarow[row].addDataCell(String.valueOf(listVo.getBackCount()),1);//退文数量 
						 datarow[row].addDataCell(listVo.getBackSignPer(),1);//退文百分比 
					 }
					 row++;
				}
				dr.addDataRow(datarow);
			}
			int row = 0;
			String[] columnName = null;
			columnName = new String[12];
			columnName[row++] ="单位";//单位
			columnName[row++] ="发文总数";//文总数
			
			if(conditionVo.getStatSetVo().isSfShowTwo()) {
				columnName[row++] = "2个工作日内接收数量";//2个工作日内接收数量
				columnName[row++] = "2个工作日内接收百分比";//2个工作日内接收百分比
			}
			if(conditionVo.getStatSetVo().isSfShowThree()) {
				columnName[row++] = "3至5个工作日内接收数量";//3至5个工作日内接收数量
				columnName[row++] = "3至5个工作日内接收百分比";//3至5个工作日内接收百分比
			}
			if(conditionVo.getStatSetVo().isSfShowFive()) {
				columnName[row++] = "5个工作日后接收数量";//5个工作日后接收数量
				columnName[row++] = "5个工作日后接收百分比";//5个工作日后接收百分比
			}
			if(conditionVo.getStatSetVo().isSfShowNoRec()) {
				columnName[row++] = "仍未签收数量";//仍未签收数量
				columnName[row++] = "仍未签收百分比";//仍未签收百分比
			}
			if(conditionVo.getStatSetVo().isSfShowback()) {
				columnName[row++] = "退文数量";//退文数量 
				columnName[row++] = "退文百分比";//退文百分比 
			}
			String[] newColumnName = (String[])resizeArray(columnName,row);
			dr.setColumnName(newColumnName);
		}
		dr.setSheetName(conditionVo.getStatName());
		dr.setTitle(conditionVo.getStatTitle());
		return dr;
	}
	
	@SuppressWarnings("rawtypes")
	private static  Object resizeArray (Object oldArray, int newSize) {      
		int oldSize = java.lang.reflect.Array.getLength(oldArray);      
		Class elementType = oldArray.getClass().getComponentType();      
		Object newArray = java.lang.reflect.Array.newInstance(elementType, newSize);      
		int preserveLength = Math.min(oldSize,newSize);      
		if (preserveLength > 0) {
			System.arraycopy (oldArray,0,newArray,0,preserveLength);
		}
		return newArray;   
	}	
	
	/**
	 * 
	 * @param parameterMap
	 * @param docMark
	 * @param docMarkDefId
	 * @param serialNo
	 * @param serialNoDefId
	 * @param isHql
	 * @return
	 */
	public static String getStatDocMarkSQL(int govdocType, GovdocStatConditionVO conditionVo, boolean isSQL) {
		StringBuilder buffer = new StringBuilder();
		//公文文号过滤发文
		if(govdocType == ApplicationSubCategoryEnum.edoc_fawen.key()) {
			if(conditionVo.getDocMarkDefId()!=null && !"-1".equals(conditionVo.getDocMarkDefId()) && conditionVo.getDocMark() != null) {
				EdocMarkDefinition markDef = conditionVo.getDocMarkDef();
				if(markDef != null) {
					String expression = markDef.getExpression();
					expression = expression.replace("$WORD", "");
					expression = expression.replace("$YEAR", "%");
					expression = expression.replace("$NO", "%");
					expression = markDef.getWordNo() + expression;
					buffer.append(" and summary." + getSQLColumn("doc_Mark", isSQL) + " like '" + expression + "'");
				}
			}
		}
		//内部文号过滤收文	
		if(govdocType == ApplicationSubCategoryEnum.edoc_shouwen.key()) {
			if(conditionVo.getSerialNoDefId()!=null && !"-1".equals(conditionVo.getSerialNoDefId()) && conditionVo.getSerialNo() != null) {
				EdocMarkDefinition markDef = conditionVo.getSerialNoMarkDef();
				if(markDef != null) {
					String expression = markDef.getExpression();
					expression = expression.replace("$WORD", "");
					expression = expression.replace("$YEAR", "%");
					expression = expression.replace("$NO", "%");
					expression = markDef.getWordNo() + expression;
					buffer.append(" and summary." + getSQLColumn("serial_No", isSQL) + " like '" + expression + "'");
				}
			}
		}
		return buffer.toString();
	}
	
	
	public static String getSQLColumn(String column, boolean isSQL) {
		if(isSQL) {
			return column;
		} else {
			return column.replace("_", ""); 
		}
	}
	public static String getSQLColumn(String column, String field, boolean isSQL) {
		if(isSQL) {
			return column;
		} else {
			return field;
		}
	}
	public static String getSerialNoSql(GovdocStatConditionVO conditionVo, boolean isSQL){
		StringBuilder buffer = new StringBuilder();
		if(conditionVo.getSerialNoDefId()!=null && !"-1".equals(conditionVo.getSerialNoDefId()) && conditionVo.getSerialNo() != null) {
			EdocMarkDefinition markDef = conditionVo.getSerialNoMarkDef();
			if(markDef != null) {
				String expression = markDef.getExpression();
				expression = expression.replace("$WORD", "");
				expression = expression.replace("$YEAR", "%");
				expression = expression.replace("$NO", "%");
				expression = markDef.getWordNo() + expression;
				buffer.append(" and summary." + getSQLColumn("serial_No", isSQL) + " like '" + expression + "'");
			}
		}
		return buffer.toString();
	}
	/**
	 * 
	 * @param statSetVo
	 * @param edocStatSet
	 */
	public static void setGovdocStatShowParam(GovdocStatSetVO statSetVo, EdocStatSet edocStatSet) {
		int showSendTdSize = 0;
		int showRecTdSize = 0;
		int showTotalTdSize = 0;
		if("work_count".equals(edocStatSet.getStatType())) { 
			if(Strings.isNotBlank(edocStatSet.getGovType())) {
				for(String gtype : edocStatSet.getGovType().split(",")) {
					//发文
					if("1".equals(gtype)) {
						statSetVo.setShowSend(true);
					} else if("2".equals(gtype)){
						showSendTdSize++;
						statSetVo.setShowSendCount(true);
//					} else if(gtype.equals("3")) {
//						showSendTdSize++;
//						statSetVo.setShowFontSize(true);
					} else if("4".equals(gtype)){
						showSendTdSize++;
						statSetVo.setShowSendDCount(true);
					} else if("5".equals(gtype)) {
						showSendTdSize++;
						statSetVo.setShowSendPCount(true);
					} else if("6".equals(gtype)) {
						showSendTdSize++;
						statSetVo.setShowSendDPer(true);
					} else if("7".equals(gtype)) {
						showSendTdSize++;
						statSetVo.setShowSendOCount(true);
					} else if("8".equals(gtype)) {
						showSendTdSize++;
						statSetVo.setShowSendOper(true);
					} 
					//收文
					else if("9".equals(gtype)) {
						statSetVo.setShowRec(true);
					} else if("10".equals(gtype)) {
						showRecTdSize++;
						statSetVo.setShowRecCount(true);
					} else if("11".equals(gtype)) {
						showRecTdSize++;
						statSetVo.setShowRecDCount(true);
					} else if("12".equals(gtype)) {
						showRecTdSize++;
						statSetVo.setShowRecPCount(true);
					} else if("13".equals(gtype)) {
						showRecTdSize++;
						statSetVo.setShowRecDper(true);
					} else if("14".equals(gtype)) {
						showRecTdSize++;
						statSetVo.setShowRecOCount(true);
					} else if("15".equals(gtype)) {
						showRecTdSize++;
						statSetVo.setShowRecOper(true);
					} 
					//总和
					else if("16".equals(gtype)) {
						statSetVo.setShowTotal(true);
					} else if("17".equals(gtype)) {
						showTotalTdSize++;
						statSetVo.setShowAllCount(true);
					} else if("18".equals(gtype)) {
						showTotalTdSize++;
						statSetVo.setShowAllDCount(true);
					} else if("19".equals(gtype)) {
						showTotalTdSize++;
						statSetVo.setShowAllPCount(true);
					} else if("20".equals(gtype)) {
						showTotalTdSize++;
						statSetVo.setShowAllDPer(true);
					} else if("21".equals(gtype)) {
						showTotalTdSize++;
						statSetVo.setShowAllOCount(true);
					} else if("22".equals(gtype)) {
						showTotalTdSize++;
						statSetVo.setShowAllOper(true);
					}
				}
			}
			statSetVo.setSendTdSize(showSendTdSize);
			statSetVo.setRecTdSize(showRecTdSize);
			statSetVo.setTotalTdSize(showTotalTdSize);
			
			statSetVo.setFawenNodePolicy(edocStatSet.getSendNode());
			statSetVo.setShouwenNodePolicy(edocStatSet.getRecNode());
		} else {
			if(Strings.isNotBlank(edocStatSet.getTimeType())) {
				for(String gtype : edocStatSet.getTimeType().split(",")) {
					if("1".equals(gtype)) {
						statSetVo.setSfShowTwo(true);
					} else if("2".equals(gtype)) {
						statSetVo.setSfShowThree(true);
					} else if("3".equals(gtype)) {
						statSetVo.setSfShowFive(true);
					} else if("4".equals(gtype)) {
						statSetVo.setSfShowNoRec(true);
					} else if("5".equals(gtype)) {
						statSetVo.setSfShowback(true);
					}
				}				
			}
		}
	}
	
	
	/**
	 * 根据配置信息获取工作统计查询相关条件
	 * @param edocStatSet
	 * @return
	 * @throws Exception
	 */
	public static void setGovdocStatSetVO(GovdocStatConditionVO conditionVo, EdocStatSet edocStatSet) throws Exception {
		if(edocStatSet == null) {
			return;
		}
		
		GovdocStatSetVO statSetVo = new GovdocStatSetVO(); 
		setGovdocStatShowParam(statSetVo, edocStatSet);
		
		//公文文号为空时，通过断号占号来匹配
    	if(!"-1".equals(conditionVo.getDocMarkDefId()) && conditionVo.getDocMark() != null) {
    		conditionVo.setDocMarkDef(edocMarkDefinitionManager.getMarkDefinition(Long.parseLong(conditionVo.getDocMarkDefId())));
		}
    	//内部文号为空时，通过断号占号来匹配
    	if(!"-1".equals(conditionVo.getSerialNoDefId()) && conditionVo.getSerialNoDefId() != null) {
			conditionVo.setSerialNoMarkDef(edocMarkDefinitionManager.getMarkDefinition(Long.parseLong(conditionVo.getSerialNoDefId())));
    	}
		
		List<V3xOrgUnit> statRootList = new ArrayList<V3xOrgUnit>();
		List<Long> statRangeList = new ArrayList<Long>();
		Map<Long, Long> statRangeMap = new HashMap<Long,Long>();
		if(conditionVo.getDisplayId() != null) {
			//穿透页面-统计范围添加部门
			statRangeList.add(conditionVo.getDisplayId());
			if((Boolean)SysFlag.sys_isG6S.getFlag() && "v3x_edoc_sign_count".equals(edocStatSet.getStatType())){
				
			}else{
				//穿透页面-统计范围添加部门下的所有子部门
				List<V3xOrgDepartment> deptList = orgManager.getChildDepartments(conditionVo.getDisplayId(), false);
				for(V3xOrgDepartment dept : deptList) {		
					statRangeList.add(dept.getId());
				}
			}
		} else {
			//统计类型为全部或单位，展示结果为单位或部门
			if(GovdocStatTypeEnum.All.name().equals(conditionVo.getStatRangeType()) || GovdocStatTypeEnum.Account.name().equals(conditionVo.getStatRangeType())) {
				String deptsIds = edocStatSet.getDeptIds();
				if(Strings.isBlank(deptsIds) || "-1".equals(deptsIds)) {
					return;
				}
				
				String[] rangeArr = deptsIds.split(",");
				String rangeIds[] = null;
				V3xOrgUnit unit = null;
				for (int i = 0; i < rangeArr.length; i++) {
					if (Strings.isBlank(rangeArr[i])) {
						continue;
					}
					if (Strings.isNotBlank(rangeArr[i])) {
						rangeIds = rangeArr[i].trim().split("[|]");
						Long rangeId = Long.parseLong(rangeIds[1]);
						
						//签收统计 或 工作统计范围为单位
						if("Account".equals(rangeIds[0])) {
							unit = orgManager.getUnitById(rangeId);
							
							//是否显示工作统计的总计行
							if("work_count".equals(edocStatSet.getStatType())) {
								statSetVo.setSfShowZj(true);
							} else {//签收统计才有集团统计，集团统计需要重新计算
								if(unit!= null && unit.isGroup()) {
									statRootList = new ArrayList<V3xOrgUnit>();
									statRangeList = new ArrayList<Long>();
									List<V3xOrgAccount> accountList = orgManager.getAllAccounts();
									for(V3xOrgAccount account : accountList) {
										if(!account.isGroup()) {
											statRootList.add(orgManager.getUnitById(account.getId()));
											statRangeList.add(account.getId());
										}
									}
									break;
								}
								//统计范围非集团时，统计所属单位
								if(unit != null){
									statRootList.add(unit);									
								}
							}
							
							//统计范围添加单位
							if(unit != null){								
								statRangeList.add(unit.getId());
							}
							//统计范围添加该单位下的所有一级部门
							List<V3xOrgDepartment> depparts = orgManager.getChildDeptsByAccountId(rangeId, true);
							for(V3xOrgDepartment dept : depparts) {						 
								//工作统计将该单位下的一级部门做为显示列
								if("work_count".equals(edocStatSet.getStatType())) {
									statRootList.add(orgManager.getUnitById(dept.getId()));
								}
								statRangeList.add(dept.getId());
								statRangeMap.put(dept.getId(), dept.getId());
								
								//统计范围添加该单位下的所有一级部门的子部门
								List<V3xOrgDepartment>  childDepts = orgManager.getChildDepartments(dept.getId(), false);
								for(V3xOrgDepartment dpt: childDepts){
									statRangeList.add(dpt.getId());
									statRangeMap.put(dpt.getId(), dept.getId());			
								}
							}
						} else {
							//统计列为部门
							statRootList.add(orgManager.getUnitById(rangeId));
							//统计范围添加部门
							statRangeList.add(rangeId);
							statRangeMap.put(rangeId, rangeId);
							//统计范围添加部门下的所有子部门
							List<V3xOrgDepartment> deptList = orgManager.getChildDepartments(rangeId, false);
							if(Strings.isNotEmpty(deptList)) {
								for(V3xOrgDepartment bean : deptList) {
									statRangeList.add(bean.getId());
									statRangeMap.put(bean.getId(), rangeId);
								}
							}
						}
					}
				}
			}
			//统计类型为部门（展示结果为人员，过滤条件为部门）
			else if(GovdocStatTypeEnum.Department.name().equals(conditionVo.getStatRangeType())) {
				//统计范围添加部门
				statRangeList.add(conditionVo.getStatRangeId());
				statRangeMap.put(conditionVo.getStatRangeId(), conditionVo.getStatRangeId());
				//统计范围添加部门下的所有子部门
				List<V3xOrgDepartment> depparts = orgManager.getChildDepartments(conditionVo.getStatRangeId(), false);
				for(V3xOrgDepartment dept : depparts) {
					statRangeList.add(dept.getId());
					statRangeMap.put(dept.getId(), dept.getId());
				}
			}
		}
		
		statRootList = unitToSort(statRootList);
		
    	statSetVo.setStatRootList(statRootList);
    	statSetVo.setStatRangeList(statRangeList);
    	statSetVo.setStatRangeMap(statRangeMap);
    	
    	conditionVo.setStatSetVo(statSetVo);
	}
	
	/**
	 * 
	 * @param statRootList
	 * @return
	 */
	public static List<V3xOrgUnit> unitToSort(List<V3xOrgUnit> statRootList) {
		if(Strings.isNotEmpty(statRootList)) {
			 Collections.sort(statRootList, new Comparator<V3xOrgUnit>(){
	            public int compare(V3xOrgUnit arg0, V3xOrgUnit arg1) {
	                return arg0.getSortId().compareTo(arg1.getSortId());
	            }
	        });
		}
		return statRootList;
	}
	
	public static boolean checkIdIsNull(Long id) {
		return id==null || id.longValue()==0 || id.longValue()==-1;
	}
	
	/**
	 * 获取当季
	 * @return
	 */
	public static String[] getCurDates() {
		String[] dates = new String[4];
		Calendar cal = Calendar.getInstance();
	    int curYear = cal.get(1);
	    int curMonth = cal.get(2) + 1;
	    int curSeason = 0;
	    if ((curMonth >= 0) && (curMonth <= 2)) {
	    	curSeason = 1;
	    }
	    else if ((curMonth >= 3) && (curMonth <= 5)) {
	    	curSeason = 2;
	    }
	    else if ((curMonth >= 6) && (curMonth <= 8)) {
	    	curSeason = 3;
	    }
	    else {
	    	curSeason = 4;
	    }
	    dates[0] = String.valueOf(curYear);
	    dates[1] = String.valueOf(curMonth);
	    dates[2] = String.valueOf(curSeason);
	    dates[3] = Datetimes.formatDate(new Date());
	    return dates;
	}
}
