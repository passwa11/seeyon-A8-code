package com.seeyon.apps.govdoc.mark.manager.impl;

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;

import com.seeyon.apps.govdoc.mark.manager.GovdocMarkParseManager;
import com.seeyon.apps.govdoc.mark.vo.GovdocMarkVO;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocMarkCategory;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.webmodel.EdocMarkReserveVO;

public class GovdocMarkParseManagerImpl implements GovdocMarkParseManager {

	public GovdocMarkVO markDef2Mode(GovdocMarkVO markVo, String yearNo, Integer theNo) {
		String yearNoStr=yearNo;
		if(yearNoStr==null || "".equals(yearNoStr)) {
			Calendar cal = Calendar.getInstance();
			yearNoStr = String.valueOf(cal.get(Calendar.YEAR));
		}
		if(markVo == null) {
			markVo =  new GovdocMarkVO();
		}

		String wordNo = markVo.getWordNo();
		String expression = markVo.getExpression();
		if(Strings.isNotBlank(wordNo)) {
			if(wordNo.indexOf("\\")>=0) wordNo = wordNo.replaceAll("\\\\", "\\\\\\\\");
			if(wordNo.indexOf("$")>=0) wordNo = wordNo.replaceAll("\\$", "\\\\\\$");
			expression = expression.replaceFirst("\\$WORD", wordNo);
		}
		
	    if(markVo.getYearEnabled()){
	        expression = expression.replaceFirst("\\$YEAR", yearNoStr);
	    }
		
		int currentNo = markVo!=null ? markVo.getCurrentNo() : 0;
		theNo = theNo == null ? currentNo : theNo;
		
		String flowNo = String.valueOf(theNo);
		int length = markVo.getLength();			
		int maxNo = markVo.getMaxNo() != null ? markVo.getMaxNo() : 0;
		int curNoLen = String.valueOf(theNo).length();
		int maxNoLen = String.valueOf(maxNo).length();
		if (length > 0 && length == maxNoLen) {
			flowNo = "";
			for (int j = curNoLen; j < length; j++) {
				flowNo += "0";
			}
			flowNo += String.valueOf(theNo);
		}
		expression = expression.replaceFirst("\\$NO", flowNo);
		if(expression.indexOf("$WORD")!=-1){
			if (wordNo ==null) {
				wordNo = StringUtils.EMPTY;
			}
			expression = expression.replaceFirst("\\$WORD", wordNo);
		}
		markVo.setMarkstr(expression);
		markVo.setLength(length);
		analyzeEdocMarkVo(markVo);
		return markVo;
	}
	
	public GovdocMarkVO markDef2Mode(EdocMarkDefinition markDef, String yearNo, Integer curentno) {
		String yearNoStr=yearNo;
		if(yearNoStr==null || "".equals(yearNoStr))
		{
			Calendar cal = Calendar.getInstance();
			yearNoStr = String.valueOf(cal.get(Calendar.YEAR));
		}
		if(markDef == null) {
			return null;
		}
		EdocMarkCategory category = markDef.getEdocMarkCategory();
		
		GovdocMarkVO markVo = new GovdocMarkVO();
		markVo.setCategoryId(category.getId());
		markVo.setMarkDefId(markDef.getId());
		markVo.setWordNo(markDef.getWordNo());
		markVo.setMarkType(markDef.getMarkType());
		markVo.setExpression(markDef.getExpression());
		
		String wordNo = markDef.getWordNo();
		String expression = markDef.getExpression();
		if(Strings.isNotBlank(wordNo)) {
			if(wordNo.indexOf("\\")>=0) wordNo = wordNo.replaceAll("\\\\", "\\\\\\\\");
			if(wordNo.indexOf("$")>=0) wordNo = wordNo.replaceAll("\\$", "\\\\\\$");
			expression = expression.replaceFirst("\\$WORD", wordNo);
		}
		
		if(category != null) {
		    if(category.getYearEnabled()){
		        expression = expression.replaceFirst("\\$YEAR", yearNoStr);
		    }
		    markVo.setCategoryCodeMode(category.getCodeMode());
		}
		
		int currentNo = category!=null ? category.getCurrentNo():0;
		markVo.setCurrentNo(currentNo);
		
		String flowNo = String.valueOf(curentno == null ? currentNo :curentno);
		int length = markDef.getLength();			
		int maxNo = category!=null ? category.getMaxNo():0;
		int curNoLen = String.valueOf(curentno==null ? currentNo : curentno).length();
		int maxNoLen = String.valueOf(maxNo).length();
		if (length > 0 && length == maxNoLen) {
			flowNo = "";
			for (int j = curNoLen; j < length; j++) {
				flowNo += "0";
			}
			if(curentno!=null){
				flowNo += String.valueOf(curentno);
			}else{
				flowNo += String.valueOf(currentNo);
			}
		}
		expression = expression.replaceFirst("\\$NO", flowNo);
		if(expression.indexOf("$WORD")!=-1){
			if (wordNo ==null) {
				wordNo = StringUtils.EMPTY;
			}
			expression = expression.replaceFirst("\\$WORD", wordNo);
		}
		markVo.setMarkstr(expression);
		markVo.setDomainId(category.getDomainId());
		markVo.setSortNo(markDef.getSortNo());
		markVo.setLength(length);
		markVo.setYearEnabled(category.getYearEnabled());
		analyzeEdocMarkVo(markVo);
		return markVo;
	}
	
	/**
	 * 解析公文文号
	 * @param docMark
	 * @param markDef
	 * @return
	 */
	public GovdocMarkVO analyzeEdocMarkVo(GovdocMarkVO markVo) {
		String expression = markVo.getExpression();
		if(Strings.isNotBlank(expression)) {
			int wordNoIndex = expression.indexOf("$WORD");
			int yearNoIndex = expression.indexOf("$YEAR");
			int markNoIndex = expression.indexOf("$NO");
			if(yearNoIndex != -1) {
				String left = expression.substring(wordNoIndex + 5, yearNoIndex);
				String right = expression.substring(yearNoIndex + 5, markNoIndex);
				int markRightIndex = expression.lastIndexOf(right);
				if(markRightIndex != -1) {
					int markLeftIndex = expression.substring(0, markRightIndex).lastIndexOf(left);
					String yearNo = expression.substring(markLeftIndex + left.length(), markRightIndex);
					markVo.setYearNo(yearNo);
					markVo.setLeft(left);
					markVo.setRight(right);
				}
			}
			String suffix = expression.substring(markNoIndex + 3);
			markVo.setSuffix(suffix);
		}
		return markVo;
	}
	

	/**
	 * 按格式显示某一个文号
	 * @param markDef
	 * @param markNumber 流水号
	 * @return
	 */
	public EdocMarkReserveVO parseToReserveByFormat(GovdocMarkVO markVo, Integer markNumber) {
		return parseToReserveByFormat(markVo, null, markNumber);
	}
	
	/**
	 * 按格式显示某一个文号
	 * @param markDef
	 * @param markNumber 流水号
	 * @return
	 */
	public EdocMarkReserveVO parseToReserveByFormat(GovdocMarkVO markVo, Integer reserveYearNo, Integer markNumber) {
		EdocMarkReserveVO reserveVO = new EdocMarkReserveVO();
		reserveVO.setMarkDefineId(markVo.getMarkDefId());
		reserveVO.setWordNo(markVo.getWordNo());
		reserveVO.setExpression(markVo.getExpression());
		String expression = reserveVO.getExpression();
		String yearNo = "";
		String formatA = "";
		String formatB = ""; 
		Boolean yearEnabled = reserveVO.getEdocMarkCategory()!=null ? reserveVO.getEdocMarkCategory().getYearEnabled() : false;
		if (yearEnabled) {
			formatA = expression.substring(expression.indexOf("$WORD") + 5, expression.indexOf("$YEAR"));
			if(reserveYearNo == null) {
				yearNo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
			} else {
				yearNo = String.valueOf(reserveYearNo);
			}
		}
		if (yearEnabled) { 
			formatB = expression.substring(expression.indexOf("$YEAR") + 5, expression.indexOf("$NO"));
		} else {
			formatB = expression.substring(5, expression.indexOf("$NO"));
		}
		String formatC = expression.substring(expression.indexOf("$NO") + 3);
		reserveVO.setYearNo(yearNo);
		reserveVO.setFormatA(formatA);
		reserveVO.setFormatB(formatB);
		reserveVO.setFormatC(formatC);
		if(markNumber != null) {
			String wordNo = reserveVO.getWordNo();
			if(wordNo == null) {
				wordNo = "";
			}
			String number = GovdocUtil.getNumberByFormat(markNumber, markVo.getLength());
			reserveVO.setReserveLimitNo(Strings.getLimitLengthString(wordNo, 15, "...")+reserveVO.getFormatA()+reserveVO.getYearNo()+reserveVO.getFormatB() + number + reserveVO.getFormatC());
			String markstr = wordNo+reserveVO.getFormatA()+reserveVO.getYearNo()+reserveVO.getFormatB() + number + reserveVO.getFormatC();

	    	if(Strings.isNotBlank(markstr)){
	    		markstr = markstr.replaceAll(String.valueOf((char)160), String.valueOf((char)32));
	    	}
			
			reserveVO.setReserveNo(markstr);
		}
		return reserveVO;
	}

}
