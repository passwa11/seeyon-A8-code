package com.seeyon.v3x.edoc.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocMarkCategory;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocMarkReserveNumber;
import com.seeyon.v3x.edoc.webmodel.EdocMarkReserveVO;

public class EdocMarkUtil {

	public enum ReserveTypeEnum {  
		reserve_all(-1), reserve_up(1), reserve_down(2);
		// 定义私有变量
		Integer reserveType;
	    ReserveTypeEnum(Integer reserveType) {
	        this.reserveType =reserveType;
	    }
	    public Integer getReserveType() {
	    	return this.reserveType;
	    }
	}
	
	public static boolean isNeedExcludeDocMarkToSelect(EdocMarkDefinition markDef, String markNo){
		Calendar cal = Calendar.getInstance();
		String cyear =  String.valueOf(cal.get(Calendar.YEAR));
		
		boolean isYearEnable = true;
		if(markDef != null){
		    EdocMarkCategory category = markDef.getEdocMarkCategory();
		    isYearEnable = category == null ? Boolean.TRUE : category.getYearEnabled();
		}
		return isYearEnable && markNo.indexOf(cyear) == -1;
	}	
	
	public static String[] getMarkReserveUpAndDown(EdocMarkDefinition markDef, List<EdocMarkReserveVO> reserveVOList) {
		StringBuilder markReserveUp = new StringBuilder();
		StringBuilder markReserveDown = new StringBuilder();
		if(Strings.isNotEmpty(reserveVOList)) {
			for(EdocMarkReserveVO reserveVO : reserveVOList) {
				List<EdocMarkReserveNumber> reserveNumberList = reserveVO.getReserveNumberList();
				if(Strings.isNotEmpty(reserveNumberList)) {
					if(EdocMarkUtil.isNeedExcludeDocMarkToSelect(markDef, reserveNumberList.get(0).getDocMark())) continue;
				}
				if(reserveVO.getEdocMarkReserve().getType().intValue() == 1) {//线上预留文号
					markReserveUp.append(reserveVO.getDocMarkDisplay());
					markReserveUp.append("、");
				} else {//线下占用
				    markReserveDown.append(reserveVO.getDocMarkDisplay());
				    markReserveDown.append("、");
				}
			}
			if(markReserveUp.length()>0) {
				markReserveUp.deleteCharAt(markReserveUp.length()-1);
			}
			if(markReserveDown.length()>0) {
				markReserveDown.deleteCharAt(markReserveDown.length()-1);
			}
		}
		return new String[] {markReserveUp.toString(), markReserveDown.toString()};
	}
	
	public static List<String> getMarkReserveNumberList(String condition, EdocMarkDefinition markDef, List<EdocMarkReserveNumber> allReserveNumberList) {
		List<String> reserveDocMarkList = new ArrayList<String>();
		if("markReserveUp".equals(condition)) {
			 if(Strings.isNotEmpty(allReserveNumberList)) {
				 for(EdocMarkReserveNumber reserveNumber : allReserveNumberList) {
					 if(reserveNumber.getMarkDefineId().longValue()==markDef.getId().longValue() 
							 && reserveNumber.getType().intValue() == 1) {
						 if(isNeedExcludeDocMarkToSelect(markDef, reserveNumber.getDocMark())) continue;
						 reserveDocMarkList.add(reserveNumber.getDocMark());
					 }
				 }
			 }
		 } else if("markReserveDown".equals(condition)) {
			 if(Strings.isNotEmpty(allReserveNumberList)) {
				 for(EdocMarkReserveNumber reserveNumber : allReserveNumberList) {
					 if(reserveNumber.getMarkDefineId().longValue()==markDef.getId().longValue() 
							 && reserveNumber.getType().intValue() == 2) {//线下占用
						 if(isNeedExcludeDocMarkToSelect(markDef, reserveNumber.getDocMark())) continue;
						 reserveDocMarkList.add(reserveNumber.getDocMark());
					 }
				 }
			 }
		}
		return reserveDocMarkList;
	}
	
	
	
}
