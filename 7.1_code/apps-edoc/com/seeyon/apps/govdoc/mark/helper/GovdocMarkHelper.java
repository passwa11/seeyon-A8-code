package com.seeyon.apps.govdoc.mark.helper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.edoc.bo.TemplateMarkInfo;
import com.seeyon.apps.govdoc.constant.GovdocEnum.NewGovdocFrom;
import com.seeyon.apps.govdoc.constant.GovdocMarkEnum.EdocMarkStateEnum;
import com.seeyon.apps.govdoc.constant.GovdocMarkEnum.GovdocJianbanTypeEnum;
import com.seeyon.apps.govdoc.constant.GovdocMarkEnum.SelectTypeEnum;
import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.mark.vo.GovdocMarkVO;
import com.seeyon.apps.govdoc.po.GovdocMarkRecord;
import com.seeyon.apps.govdoc.util.GovdocParamUtil;
import com.seeyon.apps.govdoc.vo.GovdocBaseVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.v3x.edoc.domain.EdocMark;
import com.seeyon.v3x.edoc.domain.EdocMarkCategory;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocMarkHistory;
import com.seeyon.v3x.edoc.domain.EdocMarkReserveNumber;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.util.Constants;
import com.seeyon.v3x.edoc.webmodel.EdocMarkModel;
import com.seeyon.v3x.edoc.webmodel.EdocMarkNoModel;
import com.seeyon.v3x.edoc.webmodel.EdocMarkReserveVO;

public class GovdocMarkHelper extends GovdocHelper {
	
	private static final Log LOGGER = LogFactory.getLog(GovdocMarkHelper.class);
	
	public static List<String> getMarkReserveNumberList(String condition, GovdocMarkVO markVo, List<EdocMarkReserveNumber> allReserveNumberList) {
		List<String> reserveDocMarkList = new ArrayList<String>();
		if("markReserveUp".equals(condition)) {
			 if(Strings.isNotEmpty(allReserveNumberList)) {
				 for(EdocMarkReserveNumber reserveNumber : allReserveNumberList) {
					 if(reserveNumber.getMarkDefineId().longValue() == markVo.getMarkDefId().longValue() 
							 && reserveNumber.getType().intValue() == 1) {
						 if(isNeedExcludeDocMarkToSelect(markVo, reserveNumber.getDocMark())) continue;
						 reserveDocMarkList.add(reserveNumber.getDocMark());
					 }
				 }
			 }
		 } else if("markReserveDown".equals(condition)) {
			 if(Strings.isNotEmpty(allReserveNumberList)) {
				 for(EdocMarkReserveNumber reserveNumber : allReserveNumberList) {
					 if(reserveNumber.getMarkDefineId().longValue()== markVo.getMarkDefId().longValue()  
							 && reserveNumber.getType().intValue() == 2) {//线下占用
						 if(isNeedExcludeDocMarkToSelect(markVo, reserveNumber.getDocMark())) continue;
						 reserveDocMarkList.add(reserveNumber.getDocMark());
					 }
				 }
			 }
		}
		return reserveDocMarkList;
	}
	
	public static String[] getMarkReserveUpAndDown(GovdocMarkVO markVo, List<EdocMarkReserveVO> reserveVOList) {
		StringBuilder markReserveUp = new StringBuilder();
		StringBuilder markReserveDown = new StringBuilder();
		if(Strings.isNotEmpty(reserveVOList)) {
			for(EdocMarkReserveVO reserveVO : reserveVOList) {
				List<EdocMarkReserveNumber> reserveNumberList = reserveVO.getReserveNumberList();
				if(Strings.isNotEmpty(reserveNumberList)) {
					if(isNeedExcludeDocMarkToSelect(markVo, reserveNumberList.get(0).getDocMark())) continue;
				}
				if(reserveVO.getEdocMarkReserve().getType()!=null 
						&& reserveVO.getEdocMarkReserve().getType().intValue() == 1) {//线上预留文号
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
	
	public static boolean isNeedExcludeDocMarkToSelect(GovdocMarkVO markVo, String markNo) {
		Calendar cal = Calendar.getInstance();
		String cyear =  String.valueOf(cal.get(Calendar.YEAR));
		
		boolean isYearEnable = true;
		if(markVo != null) {
		    isYearEnable = markVo.getYearEnabled();
		}
		return isYearEnable && markNo.indexOf(cyear) == -1;
	}
	
	public static EdocMarkHistory convertToMarkHistory(EdocMark mark, Long userId, Integer markType) {
		EdocMarkHistory edocMarkHistory = new EdocMarkHistory();
    	edocMarkHistory.setIdIfNew();
    	edocMarkHistory.setEdocId(mark.getEdocId());
    	edocMarkHistory.setSubject(mark.getSubject());
    	edocMarkHistory.setDocMark(mark.getDocMark());
    	edocMarkHistory.setDocMarkNo(mark.getDocMarkNo());
    	edocMarkHistory.setYearNo(mark.getYearNo());
		edocMarkHistory.setMarkDefId(mark.getMarkDefId());
		edocMarkHistory.setCategoryId(mark.getCategoryId());
		edocMarkHistory.setCompleteTime(new Date());
    	edocMarkHistory.setCreateTime(new Date());
    	edocMarkHistory.setCreateUserId(userId);
    	edocMarkHistory.setLastUserId(userId);
    	edocMarkHistory.setMarkNum(1);
    	if(markType != null) {
    		edocMarkHistory.setMarkType(markType);
    	} else {
    		edocMarkHistory.setMarkType(mark.getMarkType());
    	}
    	edocMarkHistory.setGovdocType(mark.getGovdocType());
    	edocMarkHistory.setSelectType(mark.getSelectType());
    	edocMarkHistory.setDomainId(mark.getDomainId());
    	return edocMarkHistory;
	}
	
	public static EdocMark convertToMark(EdocMarkHistory mark) {
		EdocMark edocMark = new EdocMark();
		edocMark.setIdIfNew();
		edocMark.setEdocId(mark.getEdocId());
		edocMark.setGovdocType(mark.getGovdocType());
		edocMark.setDomainId(mark.getDomainId());
		edocMark.setCreateUserId(mark.getCreateUserId());
		edocMark.setCreateTime(new Date());
		edocMark.setMarkType(mark.getMarkType());
		edocMark.setMarkNum(mark.getMarkNum());
		edocMark.setSelectType(mark.getSelectType());
		edocMark.setMarkDefId(mark.getMarkDefId());
    	edocMark.setCategoryId(mark.getCategoryId());
    	edocMark.setDocMark(mark.getDocMark());
    	edocMark.setDocMarkNo(mark.getDocMarkNo());
    	edocMark.setYearNo(mark.getYearNo());
    	edocMark.setStatus(EdocMarkStateEnum.used.key());
    	edocMark.setIsLast(1);
    	edocMark.setRealUsed(mark.getRealUsed());
    	return edocMark;
	}
	
	public static EdocMark convertToMark(GovdocMarkVO markVo) {
		EdocMark edocMark = new EdocMark();
    	edocMark.setIdIfNew();
    	//edocMark.setMemo(memo);
    	edocMark.setSelectType(markVo.getSelectType());
    	edocMark.setGovdocType(markVo.getGovdocType());
    	edocMark.setEdocId(markVo.getSummaryId());
    	edocMark.setSubject(markVo.getSubject());
    	edocMark.setDocMark(markVo.getMarkstr());
    	edocMark.setMarkDefId(markVo.getMarkDefId());
    	edocMark.setCategoryId(markVo.getCategoryId());
    	edocMark.setDocMarkNo(markVo.getMarkNumber());
    	edocMark.setYearNo(Integer.parseInt(markVo.getYearNo()));
    	edocMark.setStatus(Constants.EDOC_MARK_USED);
    	edocMark.setIsLast(1);
    	edocMark.setMarkNum(1);
    	edocMark.setMarkType(markVo.getMarkType());
    	edocMark.setDomainId(markVo.getDomainId());
    	edocMark.setCreateUserId(markVo.getCurrentUser().getId());
    	edocMark.setCreateTime(new Date());
		return edocMark;
	}
	
	public static EdocMarkHistory convertToMarkHistory(GovdocMarkVO markDefVo, GovdocMarkVO markVo) {
		EdocMarkHistory edocMarkHistory = new EdocMarkHistory();
		edocMarkHistory.setIdIfNew();
		edocMarkHistory.setSelectType(markVo.getSelectType());
		edocMarkHistory.setEdocId(markVo.getSummaryId());
    	edocMarkHistory.setSubject(markVo.getSubject());
    	edocMarkHistory.setDocMark(markDefVo.getMarkstr());
    	edocMarkHistory.setDocMarkNo(markVo.getMarkNumber());
    	if(markVo.getYearNo() != null) {
    		edocMarkHistory.setYearNo(Integer.parseInt(markVo.getYearNo()));
    	}
    	edocMarkHistory.setCategoryId(markVo.getCategoryId());
		edocMarkHistory.setMarkDefId(markDefVo.getMarkDefId());
		edocMarkHistory.setReserveId(markVo.getCallId());
		edocMarkHistory.setMarkNum(1);
    	edocMarkHistory.setGovdocType(markVo.getGovdocType());
    	edocMarkHistory.setMarkType(markVo.getMarkType());
    	edocMarkHistory.setDomainId(markVo.getDomainId());
    	edocMarkHistory.setDescription(markVo.getDescription());
    	if(markVo.getCurrentUser() != null) {
	    	edocMarkHistory.setCreateUserId(markVo.getCurrentUser().getId());
	    	edocMarkHistory.setLastUserId(markVo.getCurrentUser().getId());
    	}
    	edocMarkHistory.setCreateTime(new Date());
    	edocMarkHistory.setCompleteTime(new Date());
    	return edocMarkHistory;
	}
	
	public static EdocMarkHistory convertToMarkHistory(GovdocMarkVO markVo) {
		EdocMarkHistory edocMarkHistory = new EdocMarkHistory();
		edocMarkHistory.setIdIfNew();
		edocMarkHistory.setSelectType(markVo.getSelectType());
		edocMarkHistory.setEdocId(markVo.getSummaryId());
    	edocMarkHistory.setSubject(markVo.getSubject());
    	edocMarkHistory.setDocMark(markVo.getMarkstr());
    	edocMarkHistory.setDocMarkNo(markVo.getMarkNumber());
    	edocMarkHistory.setYearNo(Integer.parseInt(markVo.getYearNo()));
    	edocMarkHistory.setCategoryId(markVo.getCategoryId());
		edocMarkHistory.setMarkDefId(markVo.getMarkDefId());
		edocMarkHistory.setReserveId(markVo.getCallId());
		edocMarkHistory.setMarkNum(1);
    	edocMarkHistory.setGovdocType(markVo.getGovdocType());
    	edocMarkHistory.setMarkType(markVo.getMarkType());
    	edocMarkHistory.setDomainId(markVo.getDomainId());
    	edocMarkHistory.setDescription(markVo.getDescription());
    	if(markVo.getCurrentUser() != null) {
	    	edocMarkHistory.setCreateUserId(markVo.getCurrentUser().getId());
	    	edocMarkHistory.setLastUserId(markVo.getCurrentUser().getId());
    	}
    	edocMarkHistory.setCreateTime(new Date());
    	edocMarkHistory.setCompleteTime(new Date());
    	return edocMarkHistory;
	}
	
	public static GovdocMarkVO convertToMarkVo(GovdocMarkVO markVo) {
		GovdocMarkVO newVo = new GovdocMarkVO();
		newVo.setWordNo(markVo.getWordNo());
		newVo.setExpression(markVo.getExpression());
		newVo.setLength(markVo.getLength());
		newVo.setMaxNo(markVo.getMaxNo());
		newVo.setMaxNo(markVo.getMaxNo());
		newVo.setCurrentNo(markVo.getCurrentNo());
		newVo.setMarkType(markVo.getMarkType());
		return newVo;
	}
	
	public static GovdocMarkVO convertToMarkVo(EdocMarkDefinition markDef) {
		GovdocMarkVO markVo = new GovdocMarkVO();
		markVo.setMarkDefId(markDef.getId());
		markVo.setCategoryId(markDef.getCategoryId());
		markVo.setWordNo(markDef.getWordNo());
		markVo.setExpression(markDef.getExpression());
		markVo.setLength(markDef.getLength());
		markVo.setYearEnabled(markDef.getEdocMarkCategory().getYearEnabled());
		markVo.setTwoYear(markDef.getEdocMarkCategory().getTwoYear());
		markVo.setCurrentNo(markDef.getEdocMarkCategory().getCurrentNo());
		markVo.setMaxNo(markDef.getEdocMarkCategory().getMaxNo());
		markVo.setMinNo(markDef.getEdocMarkCategory().getMinNo());
		return markVo;
	}
	
	public static GovdocMarkVO convertToMarkVo(GovdocMarkRecord record) {
		GovdocMarkVO markVo = new GovdocMarkVO();
		markVo.setCallId(record.getCallId());
		markVo.setMarkDefId(record.getMarkDefId());
		markVo.setCategoryId(record.getCategoryId());
		markVo.setMarkNumber(record.getMarkNumber());
		markVo.setSelectType(record.getSelectType());
		markVo.setMarkstr(record.getMarkstr());
		markVo.setMarkType(record.getMarkType());
		markVo.setYearNo(record.getYearNo());
		markVo.setWordNo(record.getWordNo());
		markVo.setNewflowType(record.getNewflowType());
		markVo.setParentSummaryId(record.getParentSummaryId());
		markVo.setChildSummaryId(record.getChildSummaryId());
		return markVo;
	}	
	
	public static List<GovdocMarkVO> convertToMarkHistory(List<EdocMarkHistory> historyList) {
		List<GovdocMarkVO> markVoList = new ArrayList<GovdocMarkVO>();
		if(Strings.isNotEmpty(historyList)) {
			GovdocMarkVO markVo = null;
			for(EdocMarkHistory bean : historyList) {
				markVo = new GovdocMarkVO();
				
				markVo.setCallId(bean.getId());
				markVo.setMarkstr(bean.getDocMark());
				markVo.setMarkNumber(bean.getDocMarkNo());
				markVoList.add(markVo);
			}
		}
		return markVoList;
	}
	
	public static List<GovdocMarkVO> convertToMarkVo(List<EdocMark> markList) {
		List<GovdocMarkVO> markVoList = new ArrayList<GovdocMarkVO>();
		if(Strings.isNotEmpty(markList)) {
			GovdocMarkVO markVo = null;
			for(EdocMark bean : markList) {
				markVo = new GovdocMarkVO();
				markVo.setCallId(bean.getId());
				markVo.setMarkstr(bean.getDocMark());
				markVo.setMarkNumber(bean.getDocMarkNo());
				markVo.setYearNo(String.valueOf(bean.getYearNo()));
				markVoList.add(markVo);
			}
		}
		return markVoList;
	}
	
	public static EdocMarkDefinition convertToMarkDef(GovdocMarkVO markVo) {
		EdocMarkDefinition markDef = new EdocMarkDefinition();
		markDef.setId(markVo.getMarkDefId());
		markDef.setMarkType(markVo.getMarkType());
		markDef.setExpression(markVo.getExpression());
		markDef.setWordNo(markVo.getWordNo());
		markDef.setLength(markVo.getLength());
		EdocMarkCategory markCategory = new EdocMarkCategory();
		markCategory.setCurrentNo(markVo.getCurrentNo());
		markCategory.setMinNo(markVo.getMinNo());
		markCategory.setMaxNo(markVo.getMaxNo());
		markCategory.setYearEnabled(markVo.getYearEnabled());
		markDef.setEdocMarkCategory(markCategory);
		return markDef;
	}

	/**
	 * 进入公文新建界面填充文号VO
	 * @param newVo
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public static void fillNewMarkParameter(GovdocBaseVO newVo) throws BusinessException {
		if(newVo.getTemplate() != null && Strings.isNotBlank(newVo.getTemplate().getBindMarkInfo())) {
			List<Long> tMarkDefIdList = new ArrayList<Long>();
			List<TemplateMarkInfo> markInfoList = (List<TemplateMarkInfo>)XMLCoder.decoder(newVo.getTemplate().getBindMarkInfo());
			String yearNo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
			if(Strings.isNotEmpty(markInfoList)) {
				for(TemplateMarkInfo markObj : markInfoList) {
					GovdocMarkVO markVo = new GovdocMarkVO();
					markVo.setSelectType(markObj.getSelectType());
					markVo.setMarkType(markObj.getMarkType());
					markVo.setMarkDefId(markObj.getMarkDefId());
					markVo.setMarkstr(markObj.getMarkstr());
					markVo.setYearNo(yearNo);
					markVo.setWordNo(markObj.getWordNo());
					markVo.setIsTemplate(true);
					markVo.setIsSysTemplate(newVo.getTemplate().isSystem() ? true : newVo.getTemplate().getFormParentid()!=null);
					if(GovdocParamUtil.isNotNull(markObj.getMarkDefId())) {
						tMarkDefIdList.add(markObj.getMarkDefId());
						markVo.setTemplateMarkDefId(markObj.getMarkDefId());
					}
					if(markObj.getMarkType()!=null) {
						if(markObj.getMarkType().intValue()==0) {//公文文号
							newVo.setTemplateDocMarkVo(markVo);	
						} else if(markObj.getMarkType().intValue()==1) {//内部文号
							newVo.setTemplateSerialNoVo(markVo);	
						} else {//签收编号
							newVo.setTemplateSignMarkVo(markVo);
						}
					}
				}// for end
				if(Strings.isNotEmpty(tMarkDefIdList)) {
					List<GovdocMarkVO> markVoList = govdocMarkManager.getMarkVoListByMarkDefId(tMarkDefIdList);
					for(GovdocMarkVO markVo : markVoList) {
						if(markVo.getMarkType().intValue() == 0) {
							if(newVo.getTemplateDocMarkVo() != null) {
								newVo.getTemplateDocMarkVo().setWordNo(markVo.getWordNo());
								newVo.getTemplateDocMarkVo().setCurrentNo(markVo.getCurrentNo());
								newVo.getTemplateDocMarkVo().setMarkNumber(markVo.getCurrentNo());
								newVo.getTemplateDocMarkVo().setYearEnabled(markVo.getYearEnabled());
								newVo.getTemplateDocMarkVo().setLeft(markVo.getLeft());
								newVo.getTemplateDocMarkVo().setRight(markVo.getRight());
								newVo.getTemplateDocMarkVo().setLength(markVo.getLength());
								newVo.getTemplateDocMarkVo().setSuffix(markVo.getSuffix());
							}
						} else if(markVo.getMarkType().intValue()  == 1) {
							if(newVo.getTemplateSerialNoVo() != null) {
								newVo.getTemplateSerialNoVo().setWordNo(markVo.getWordNo());
								newVo.getTemplateSerialNoVo().setCurrentNo(markVo.getCurrentNo());
								newVo.getTemplateSerialNoVo().setMarkNumber(markVo.getCurrentNo());
								newVo.getTemplateSerialNoVo().setYearEnabled(markVo.getYearEnabled());
								newVo.getTemplateSerialNoVo().setLeft(markVo.getLeft());
								newVo.getTemplateSerialNoVo().setRight(markVo.getRight());
								newVo.getTemplateSerialNoVo().setLength(markVo.getLength());
								newVo.getTemplateSerialNoVo().setSuffix(markVo.getSuffix());
							}
						} else {
							if(newVo.getTemplateSignMarkVo() != null) {
								newVo.getTemplateSignMarkVo().setWordNo(markVo.getWordNo());
								newVo.getTemplateSignMarkVo().setCurrentNo(markVo.getCurrentNo());
								newVo.getTemplateSignMarkVo().setMarkNumber(markVo.getCurrentNo());
								newVo.getTemplateSignMarkVo().setYearEnabled(markVo.getYearEnabled());
								newVo.getTemplateSignMarkVo().setLeft(markVo.getLeft());
								newVo.getTemplateSignMarkVo().setRight(markVo.getRight());
								newVo.getTemplateSignMarkVo().setLength(markVo.getLength());
								newVo.getTemplateSignMarkVo().setSuffix(markVo.getSuffix());
							}
						}
					}//for end
				}//Strings.isNotEmpty(tMarkDefIdList) end
			}//Strings.isNotEmpty(markInfoList)
		}
		
		if("template".equals(newVo.getAction())) {
			newVo.setDocMarkVo(newVo.getTemplateDocMarkVo());
			newVo.setSerialNoVo(newVo.getTemplateSerialNoVo());
			newVo.setSignMarkVo(newVo.getTemplateSignMarkVo());
		} else if(!newVo.isNewBusiness() || "summary".equals(newVo.getAction()) || NewGovdocFrom.distribute.name().equals(newVo.getFrom())) {//来自编辑/查看/分办
			//公文使用的文号定义ID
			List<Long> tMarkDefIdList = new ArrayList<Long>();
			
			Long summaryId = newVo.getSummary().getId();
			if(NewGovdocFrom.distribute.name().equals(newVo.getFrom())) {
				summaryId = newVo.getSignSummaryId();
			}
			Map<Integer, GovdocMarkVO> markMap = govdocMarkManager.getVoBySummaryId(summaryId);
			
			if(markMap != null && markMap.get(0)!=null) {//该公文已有公文文号
				newVo.setDocMarkVo(markMap.get(0));
			} else if(newVo.getTemplateDocMarkVo() != null && newVo.getTemplateDocMarkVo().getIsSysTemplate()) {//公文文号为空，系统模板文号绑定不为空
				newVo.setDocMarkVo(newVo.getTemplateDocMarkVo());
			}
			if(newVo.getDocMarkVo() !=null) {
				tMarkDefIdList.add(newVo.getDocMarkVo().getMarkDefId());
				if(newVo.getTemplateDocMarkVo() != null) {//将模板相关参数赋值
					newVo.getDocMarkVo().setIsTemplate(newVo.getTemplateDocMarkVo().getIsTemplate());
					newVo.getDocMarkVo().setIsSysTemplate(newVo.getTemplateDocMarkVo().getIsSysTemplate());
					newVo.getDocMarkVo().setTemplateMarkDefId(newVo.getTemplateDocMarkVo().getTemplateMarkDefId());	
				}	
			}
			
			if(markMap != null && markMap.get(1)!=null) {//该公文已有内部文号
				newVo.setSerialNoVo(markMap.get(1));
			} else if(newVo.getTemplateSerialNoVo() != null && newVo.getTemplateSerialNoVo().getIsSysTemplate()) {//内部文号为空，系统模板文号绑定不为空
				newVo.setSerialNoVo(newVo.getTemplateSerialNoVo());
			}
			if(newVo.getSerialNoVo() !=null) {
				tMarkDefIdList.add(newVo.getSerialNoVo().getMarkDefId());
				if(newVo.getTemplateSerialNoVo() != null) {//将模板相关参数赋值
					newVo.getSerialNoVo().setIsTemplate(newVo.getTemplateSerialNoVo().getIsTemplate());
					newVo.getSerialNoVo().setIsSysTemplate(newVo.getTemplateSerialNoVo().getIsSysTemplate());
					newVo.getSerialNoVo().setTemplateMarkDefId(newVo.getTemplateSerialNoVo().getTemplateMarkDefId());	
				}
			}
			
			if(markMap != null && markMap.get(2)!=null) {//该公文已有签收文号
				newVo.setSignMarkVo(markMap.get(2));
			} else if(newVo.getTemplateSignMarkVo() != null && newVo.getTemplateSignMarkVo().getIsSysTemplate()) {//签收编号为空，系统模板文号绑定不为空
				newVo.setSignMarkVo(newVo.getTemplateSignMarkVo());
			}
			if(newVo.getSignMarkVo() !=null) {//将模板相关参数赋值
				tMarkDefIdList.add(newVo.getSignMarkVo().getMarkDefId());
				if(newVo.getTemplateSignMarkVo() != null) {//将模板相关参数赋值
					newVo.getSignMarkVo().setIsTemplate(newVo.getTemplateSignMarkVo().getIsTemplate());
					newVo.getSignMarkVo().setIsSysTemplate(newVo.getTemplateSignMarkVo().getIsSysTemplate());
					newVo.getSignMarkVo().setTemplateMarkDefId(newVo.getTemplateSignMarkVo().getTemplateMarkDefId());
				}
			}
			
			if(Strings.isNotEmpty(tMarkDefIdList)) {//若公文已有文号或已绑定文号，若文号定义做了更改，则使用最新的文号
				List<GovdocMarkVO> markDefVoList = govdocMarkManager.getMarkVoListByMarkDefId(tMarkDefIdList);
				for(GovdocMarkVO markDefVo : markDefVoList) {
					if(markDefVo.getMarkType().intValue() == 0) {
						if(newVo.getDocMarkVo() != null) {
							newVo.getDocMarkVo().setWordNo(markDefVo.getWordNo());
							newVo.getDocMarkVo().setCurrentNo(markDefVo.getCurrentNo());
							//newVo.getDocMarkVo().setMarkNumber(markVo.getCurrentNo());//编辑时markNumber原文号流水号
							newVo.getDocMarkVo().setYearEnabled(markDefVo.getYearEnabled());
							newVo.getDocMarkVo().setLeft(markDefVo.getLeft());
							newVo.getDocMarkVo().setRight(markDefVo.getRight());
							newVo.getDocMarkVo().setLength(markDefVo.getLength());
							newVo.getDocMarkVo().setSuffix(markDefVo.getSuffix());
						}
					} else if(markDefVo.getMarkType().intValue()  == 1) {
						if(newVo.getSerialNoVo() != null) {
							newVo.getSerialNoVo().setWordNo(markDefVo.getWordNo());
							newVo.getSerialNoVo().setCurrentNo(markDefVo.getCurrentNo());
							//newVo.getSerialNoVo().setMarkNumber(markVo.getCurrentNo());
							newVo.getSerialNoVo().setYearEnabled(markDefVo.getYearEnabled());
							newVo.getSerialNoVo().setLeft(markDefVo.getLeft());
							newVo.getSerialNoVo().setRight(markDefVo.getRight());
							newVo.getSerialNoVo().setLength(markDefVo.getLength());
							newVo.getSerialNoVo().setSuffix(markDefVo.getSuffix());
						}
					} else {
						if(newVo.getSignMarkVo() != null) {
							newVo.getSignMarkVo().setWordNo(markDefVo.getWordNo());
							newVo.getSignMarkVo().setCurrentNo(markDefVo.getCurrentNo());
							//newVo.getSignMarkVo().setMarkNumber(markVo.getCurrentNo());
							newVo.getSignMarkVo().setYearEnabled(markDefVo.getYearEnabled());
							newVo.getSignMarkVo().setSuffix(markDefVo.getSuffix());
							newVo.getSignMarkVo().setLength(markDefVo.getLength());
							newVo.getSignMarkVo().setLeft(markDefVo.getLeft());
							newVo.getSignMarkVo().setRight(markDefVo.getRight());
							newVo.getSignMarkVo().setLength(markDefVo.getLength());
							newVo.getSignMarkVo().setSuffix(markDefVo.getSuffix());
						}
					}
				}
			}
		}
		
		GovdocMarkVO openVo = new GovdocMarkVO();
		if(newVo.getSubApp() != null) {
			openVo.setGovdocType(Integer.parseInt(newVo.getSubApp()));
		} else {
			openVo.setGovdocType(newVo.getSummary().getGovdocType());
		}
		String markType = "0";
		openVo.setIsMarkShowCall_0(govdocMarkOpenManager.isMarkShowCall(markType));
		openVo.setIsMarkHandInput_0(govdocMarkOpenManager.isMarkHandInput(markType));
		if(openVo.isFawen()) {
			openVo.setIsMarkCheckCall_0_1(govdocMarkOpenManager.isMarkCheckCall(markType, openVo.isFawen()));
			openVo.setMarkUsedType_0_1(govdocMarkOpenManager.getSendUsedType(markType, openVo.isFawen()));
			openVo.setIsMarkFinish_0_1(govdocMarkOpenManager.isUsedByFensong_Finish(markType, openVo.isFawen()));
		} else {
			openVo.setIsMarkCheckCall_0_2(govdocMarkOpenManager.isMarkCheckCall(markType, openVo.isFawen()));
			openVo.setMarkUsedType_0_2(govdocMarkOpenManager.getSendUsedType(markType, openVo.isFawen()));
		}

		markType = "1";
		openVo.setIsMarkShowCall_1(govdocMarkOpenManager.isMarkShowCall(markType));
		openVo.setIsMarkHandInput_1(govdocMarkOpenManager.isMarkHandInput(markType));
		if(openVo.isFawen()) {
			//openVo.setIsMarkCheckCall_1_1(govdocMarkOpenManager.isMarkCheckCall(markType, openVo.isFawen()));
			//openVo.setMarkUsedType_1_1(govdocMarkOpenManager.getSendUsedType(markType, openVo.isFawen()));
		} else {
			openVo.setIsMarkCheckCall_1_2(govdocMarkOpenManager.isMarkCheckCall(markType, openVo.isFawen()));
			openVo.setMarkUsedType_1_2(govdocMarkOpenManager.getSendUsedType(markType, openVo.isFawen()));
		}
		
		markType = "2";
		openVo.setIsMarkShowCall_2(govdocMarkOpenManager.isMarkShowCall(markType));
		openVo.setIsMarkHandInput_2(govdocMarkOpenManager.isMarkHandInput(markType));
		openVo.setIsMarkCheckCall_2(govdocMarkOpenManager.isMarkCheckCall(markType, openVo.isFawen()));
		if(openVo.isFawen()) {
			openVo.setIsMarkCheckCall_2_1(govdocMarkOpenManager.isMarkCheckCall(markType, openVo.isFawen()));
			openVo.setMarkUsedType_2_1(govdocMarkOpenManager.getSendUsedType(markType, openVo.isFawen()));
		} else {
			openVo.setIsMarkCheckCall_2_2(govdocMarkOpenManager.isMarkCheckCall(markType, openVo.isFawen()));
			openVo.setMarkUsedType_2_2(govdocMarkOpenManager.getSendUsedType(markType, openVo.isFawen()));
		}
		newVo.setMarkOpenVo(openVo);
	}
	
	/**
	 * 公文保存时，填充文号VO
	 * @param newVo
	 * @param para
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void fillSaveMarkParameter(GovdocBaseVO newVo, Map para) {
		Map paraMain = ParamUtil.getJsonDomain("colSummaryData");
		String isLastNode = GovdocParamUtil.getString(paraMain, "workflow_last_input");
		
		para = ParamUtil.getJsonDomain("markParam");
		Integer jianbanType = GovdocJianbanTypeEnum.no.key();
		if(para.get("jianbanType")!= null && !("".equals(para.get("jianbanType")))) {
			jianbanType = Integer.parseInt(para.get("jianbanType").toString());
		}
		
		int markType = 0;
		boolean hasDocMark = GovdocParamUtil.getBoolean(para, "hasMark_" + markType);
		if(hasDocMark) {
			Long markDefId = GovdocParamUtil.getLong(para, "markDefId_" + markType);
			Long categoryId = GovdocParamUtil.getLong(para, "categoryId_" + markType);
			Long callId = GovdocParamUtil.getLong(para, "callId_" + markType);
			Integer selectType = GovdocParamUtil.getInteger(para, "selectType_" + markType);
			Integer markNumber = GovdocParamUtil.getInteger(para, "markNumber_" + markType);
			String yearNo = GovdocParamUtil.getString(para, "yearNo_" + markType);
			String markstr = GovdocParamUtil.getString(para, "markstr_" + markType);
			String wordNo = GovdocParamUtil.getString(para, "wordNo_" + markType);
			String fieldName = GovdocParamUtil.getString(para, "fieldName_" + markType);
			Integer newflowType = GovdocParamUtil.getInteger(para, "newflowType_" + markType);
			Long parentSummaryId = GovdocParamUtil.getLong(para, "parentSummaryId_" + markType);
			String childSummaryId = GovdocParamUtil.getString(para, "childSummaryId_" + markType);
			Boolean yearEnabled = GovdocParamUtil.getBoolean(para, "yearEnabled_" + markType);
			GovdocMarkVO markVo = new GovdocMarkVO();
			markVo.setMarkstr(markstr);
			markVo.setWordNo(wordNo);
			markVo.setCategoryId(categoryId);
			markVo.setMarkDefId(markDefId);
			markVo.setCallId(callId);
			markVo.setSelectType(selectType == null ? 0 : selectType);
			markVo.setYearNo(yearNo);
			markVo.setMarkNumber(markNumber);
			markVo.setMarkType(markType);
			markVo.setCurrentUser(newVo.getCurrentUser());
			markVo.setFensong(newVo.getIsQuickSend());
			markVo.setLastNode("true".equals(isLastNode));
			markVo.setFieldName(fieldName);
			markVo.setJianbanType(jianbanType);
			markVo.setNewflowType(newflowType);
			markVo.setParentSummaryId(parentSummaryId);
			markVo.setChildSummaryId(childSummaryId);
			markVo.setYearEnabled(yearEnabled);
			
			newVo.setDocMarkVo(markVo);
			newVo.setNewflowType(newflowType);
			newVo.setParentSummaryId(parentSummaryId);
			newVo.setChildSummaryId(childSummaryId);
		}
		
		markType = 1;
		hasDocMark = GovdocParamUtil.getBoolean(para, "hasMark_" + markType);
		if(hasDocMark) {
			Long markDefId = GovdocParamUtil.getLong(para, "markDefId_" + markType);
			Long categoryId = GovdocParamUtil.getLong(para, "categoryId_" + markType);
			Long callId = GovdocParamUtil.getLong(para, "callId_" + markType);
			Integer selectType = GovdocParamUtil.getInteger(para, "selectType_" + markType);
			Integer markNumber = GovdocParamUtil.getInteger(para, "markNumber_" + markType);
			String yearNo = GovdocParamUtil.getString(para, "yearNo_" + markType);
			String markstr = GovdocParamUtil.getString(para, "markstr_" + markType);
			String wordNo = GovdocParamUtil.getString(para, "wordNo_" + markType);
			String fieldName = GovdocParamUtil.getString(para, "fieldName_" + markType);
			Integer newflowType = GovdocParamUtil.getInteger(para, "newflowType_" + markType);
			Long parentSummaryId = GovdocParamUtil.getLong(para, "parentSummaryId_" + markType);
			String childSummaryId = GovdocParamUtil.getString(para, "childSummaryId_" + markType);
			Boolean yearEnabled = GovdocParamUtil.getBoolean(para, "yearEnabled_" + markType);
			GovdocMarkVO markVo = new GovdocMarkVO();
			markVo.setMarkstr(markstr);
			markVo.setWordNo(wordNo);
			markVo.setCategoryId(categoryId);
			markVo.setMarkDefId(markDefId);
			markVo.setCallId(callId);
			markVo.setSelectType(selectType == null ? 0 : selectType);
			markVo.setYearNo(yearNo);
			markVo.setMarkNumber(markNumber);
			markVo.setMarkType(markType);
			markVo.setCurrentUser(newVo.getCurrentUser());
			markVo.setFensong(newVo.getIsQuickSend());
			markVo.setLastNode("true".equals(isLastNode));
			markVo.setFieldName(fieldName);
			markVo.setJianbanType(jianbanType);
			markVo.setNewflowType(newflowType);
			markVo.setParentSummaryId(parentSummaryId);
			markVo.setChildSummaryId(childSummaryId);
			markVo.setYearEnabled(yearEnabled);
			
			newVo.setSerialNoVo(markVo);
			newVo.setNewflowType(newflowType);
			newVo.setParentSummaryId(parentSummaryId);
			newVo.setChildSummaryId(childSummaryId);
		}
		
		markType = 2;
		hasDocMark = GovdocParamUtil.getBoolean(para, "hasMark_" + markType);
		if(hasDocMark) {
			Long markDefId = GovdocParamUtil.getLong(para, "markDefId_" + markType);
			Long categoryId = GovdocParamUtil.getLong(para, "categoryId_" + markType);
			Long callId = GovdocParamUtil.getLong(para, "callId_" + markType);
			Integer selectType = GovdocParamUtil.getInteger(para, "selectType_" + markType);
			Integer markNumber = GovdocParamUtil.getInteger(para, "markNumber_" + markType);
			String yearNo = GovdocParamUtil.getString(para, "yearNo_" + markType);
			String markstr = GovdocParamUtil.getString(para, "markstr_" + markType);
			String wordNo = GovdocParamUtil.getString(para, "wordNo_" + markType);
			String fieldName = GovdocParamUtil.getString(para, "fieldName_" + markType);
			Integer newflowType = GovdocParamUtil.getInteger(para, "newflowType_" + markType);
			Long parentSummaryId = GovdocParamUtil.getLong(para, "parentSummaryId_" + markType);
			String childSummaryId = GovdocParamUtil.getString(para, "childSummaryId_" + markType);
			Boolean yearEnabled = GovdocParamUtil.getBoolean(para, "yearEnabled_" + markType);
			GovdocMarkVO markVo = new GovdocMarkVO();
			markVo.setMarkstr(markstr);
			markVo.setWordNo(wordNo);
			markVo.setCategoryId(categoryId);
			markVo.setMarkDefId(markDefId);
			markVo.setCallId(callId);
			markVo.setSelectType(selectType == null ? 0 : selectType);
			markVo.setYearNo(yearNo);
			markVo.setMarkNumber(markNumber);
			markVo.setMarkType(markType);
			markVo.setCurrentUser(newVo.getCurrentUser());
			markVo.setFensong(newVo.getIsQuickSend());
			markVo.setLastNode("true".equals(isLastNode));
			markVo.setFieldName(fieldName);
			markVo.setJianbanType(jianbanType);
			markVo.setNewflowType(newflowType);
			markVo.setParentSummaryId(parentSummaryId);
			markVo.setChildSummaryId(childSummaryId);
			markVo.setYearEnabled(yearEnabled);
			
			newVo.setSignMarkVo(markVo);
			newVo.setNewflowType(newflowType);
			newVo.setParentSummaryId(parentSummaryId);
			newVo.setChildSummaryId(childSummaryId);
		}
		
		GovdocMarkVO markOpenVo = new GovdocMarkVO();
		markOpenVo.setLastNode("true".equals(isLastNode));
		newVo.setMarkOpenVo(markOpenVo);
		newVo.setJianbanType(jianbanType);
	}
	
	@SuppressWarnings("rawtypes")
	public static void fillSaveMarkH5Parameter(GovdocBaseVO newVo, Map para) {
		EdocSummary summary = newVo.getSummary();
		if(Strings.isNotBlank(summary.getDocMark())) {
			LOGGER.info("从H5获取公文文号值为：" + summary.getDocMark());
			GovdocMarkVO markVo = parseDocMark(summary.getDocMark());
			if(markVo != null) {
				markVo.setMarkType(0);
				markVo.setCurrentUser(newVo.getCurrentUser());
				markVo.setIsEnable(true);
				newVo.setDocMarkVo(markVo);
			}
		}
		
		if(Strings.isNotBlank(summary.getSerialNo())) {
			LOGGER.info("从H5获取内部文号值为：" + summary.getSerialNo());
			GovdocMarkVO markVo = parseDocMark(summary.getSerialNo());
			if(markVo != null) {
				markVo.setMarkType(1);
				markVo.setCurrentUser(newVo.getCurrentUser());
				markVo.setIsEnable(true);
				newVo.setSerialNoVo(markVo);
			}
		}
		
		if(Strings.isNotBlank(summary.getDocMark2())) {
			LOGGER.info("从H5获取签收编号值为：" + summary.getDocMark2());
			GovdocMarkVO markVo = parseDocMark(summary.getDocMark2());
			if(markVo != null) {
				markVo.setMarkType(2);
				markVo.setCurrentUser(newVo.getCurrentUser());
				markVo.setIsEnable(true);
				newVo.setSignMarkVo(markVo);
			}
		}
	}
	
	public static GovdocMarkVO parseDocMark(GovdocMarkVO markVo, String markstr) {
		if (Strings.isBlank(markstr)) {
		    return null;
		}
		EdocMarkDefinition markDef = markVo.getMarkDef();
		if(markDef==null || markDef.getEdocMarkCategory()==null || !markDef.getEdocMarkCategory().getYearEnabled()) {
			return null;
		}
		String mleft = markVo.getLeft();
		String mright = markVo.getRight();
		//String suffix = markVo.getSuffix();
		String yearNo = markVo.getYearNo();
		if(markstr.indexOf(yearNo) >= 0) {
			return null;
		}
		
		int start = markstr.indexOf(mleft);
		int end = markstr.indexOf(mright);
		if(start > 0 && end - start == 4 + 1) {
			String myearNo = markstr.substring(start + 1, end);
			//int sIndex = markstr.lastIndexOf(suffix);
			//String number = markstr.substring(end + 1, sIndex);
			
			if(markDef != null) {
				/*try {
					markVo.setMarkNumber(Integer.parseInt(number));
				} catch(NumberFormatException nfe) {}*/
				try {
					markVo.setYearNo(myearNo);
				} catch(NumberFormatException nfe) {}
			}
		}
		return markVo;
	}
	
	public static GovdocMarkVO parseDocMark(String docMark) {
		GovdocMarkVO markVo = null;
		if(Strings.isBlank(docMark)) {
		    return null;
		}
		String[] arr = docMark.split("\\|");
		if (arr == null || arr.length < 3) {
		    return markVo;
		}
		markVo = new GovdocMarkVO();
		if(!"".equals(arr[0])) {
			markVo.setSelectType(Integer.valueOf(arr[0]));
		}
		if(Strings.isNotBlank(arr[1])) {
			markVo.setMarkDefId(Long.valueOf(arr[1]));
			markVo.setCallId(Long.valueOf(arr[1]));
		}
		markVo.setMarkstr(arr[2]);
		if(arr.length==4 && Strings.isNotBlank(arr[3])) {
			markVo.setMarkNumber(Integer.valueOf(arr[3]));
	    }
		return markVo;
	}
	
	public static GovdocMarkVO parseDocMarkOld(String docMark) {
		GovdocMarkVO markVo = null;
		if(Strings.isBlank(docMark)) {
		    return null;
		}
		String[] arr = docMark.split("\\|");
		if (arr == null || arr.length < 3) {
		    return markVo;
		}
		markVo = new GovdocMarkVO();
		if(arr.length==4 && Strings.isNotBlank(arr[3])) {
			int mode = Integer.valueOf(arr[3]);
			int selectType = getSelectTypeByMode(mode);
			markVo.setSelectType(selectType);
			if(selectType != SelectTypeEnum.shouxie.ordinal()) {
				markVo.setYearNo(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
			}
		}
		markVo.setMarkstr(arr[1]);
		if(Strings.isNotBlank(arr[0])) {
			markVo.setMarkDefId(Long.valueOf(arr[0]));
			markVo.setCallId(Long.valueOf(arr[0]));
		}
		if(Strings.isNotBlank(arr[2])) {
			markVo.setMarkNumber(Integer.valueOf(arr[2]));
	    }
		return markVo;
	}
	
	public static EdocMarkModel parseMarkstrOld(String markstr, String markType) {
		EdocMarkModel model = new EdocMarkModel();
		model.setMark(Strings.toHTML(Strings.toXmlStr(markstr)));
		
		String mleft = "〔";
		String mright = "〕";
		String suffix = "号";
		int start = markstr.indexOf(mleft);
		int end = markstr.indexOf(mright);
		if(start > 0 && end - start == 4 + 1) {
			String mywordNo = markstr.substring(0, start);
			String myearNo = markstr.substring(start + 1, end);
			int sIndex = markstr.lastIndexOf(suffix);
			String number = markstr.substring(end + 1, sIndex);
			
			EdocMarkDefinition markDef = govdocMarkManager.getMarkDef(mywordNo, markType);
			if(markDef != null) {
				model.setMarkDef(markDef);
				model.setMarkDefinitionId(markDef.getId());
				model.setMarkType(markDef.getMarkType());
				EdocMarkNoModel markNoVo = new EdocMarkNoModel();
				try {
					model.setCurrentNo(Integer.parseInt(number));
					markNoVo.setMarkNumber(Integer.parseInt(number));
				} catch(NumberFormatException nfe) {}
				try {
					markNoVo.setYearNo(myearNo);
				} catch(NumberFormatException nfe) {}
				model.setMarkNoVo(markNoVo);
				model.setCodeMode((short)1);
				
				String docMark = markDef.getId() + "|" + model.getMark() + "|" + model.getCurrentNo() + "|" + com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_NEW;
				model.setWordNo(docMark);
			}
		}
		if(model.getMarkNoVo() == null) {
			model.setCodeMode((short)3);//手写
		}
		return model;
	}
	
	public static int getSelectTypeByMode(int mode) {
		int selectType = 0;
		if(mode == Constants.EDOC_MARK_EDIT_SELECT_NEW) {//下拉选择的文号
			selectType = SelectTypeEnum.zidong.ordinal();
		} else if(mode == Constants.EDOC_MARK_EDIT_SELECT_OLD) {// 选择的断号
			selectType = SelectTypeEnum.duanhao.ordinal();
		} else if(mode == Constants.EDOC_MARK_EDIT_INPUT) {// 手工输入的文号
			selectType = SelectTypeEnum.shouxie.ordinal();
		} else if(mode == Constants.EDOC_MARK_EDIT_SELECT_RESERVE) {//选择的预留文号
			selectType = SelectTypeEnum.yuliu.ordinal();
		}
		return selectType;
	}
	
	public static int getModeBySelectType(int selectType) {
		int mode = 0;
		if(selectType == SelectTypeEnum.zidong.ordinal()) {//下拉选择的文号
			mode = Constants.EDOC_MARK_EDIT_SELECT_NEW;
		} else if(selectType == SelectTypeEnum.duanhao.ordinal()) {// 选择的断号
			mode = Constants.EDOC_MARK_EDIT_SELECT_OLD;
		} else if(selectType == SelectTypeEnum.shouxie.ordinal()) {// 手工输入的文号
			mode = Constants.EDOC_MARK_EDIT_INPUT;
		} else if(selectType == SelectTypeEnum.yuliu.ordinal()) {//选择的预留文号
			mode = Constants.EDOC_MARK_EDIT_SELECT_RESERVE;
		}
		return mode;
	}
	
	public static String getFormMarkOption(GovdocMarkVO markVo, String yearNo) {
		StringBuilder options = new StringBuilder();
		options.append("<option title ='").append(markVo.getWordNo()).append("'");
		options.append(" value='").append(markVo.getMarkstr()).append("'");
		options.append(" markDefId='").append(markVo.getMarkDefId()).append("'");
		options.append(" categoryId='").append(markVo.getCategoryId()).append("'");
		options.append(" currentNo='").append(markVo.getCurrentNo()).append("'");
		options.append(" yearNo='").append(yearNo).append("'");
		options.append(" left='").append(markVo.getLeft()).append("'");
		options.append(" right='").append(markVo.getRight()).append("'");
		options.append(" suffix='").append(markVo.getSuffix()).append("'");
		options.append(" markNumber='").append(markVo.getCurrentNo()).append("'");
		options.append(" oldmarkNumber='").append(markVo.getMarkNumber()).append("'");
		options.append(" markLength='").append(markVo.getLength()).append("'");
		options.append(" yearEnabled='").append(markVo.getYearEnabled()).append("'");
		options.append(" twoYear='").append(markVo.isTwoYear()).append("'");
		options.append(">");
		options.append(markVo.getWordNo());
		options.append("</option>");
		return options.toString();
	}
	
}
