package com.seeyon.v3x.edoc.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.dao.EdocMarkReserveDao;
import com.seeyon.v3x.edoc.dao.EdocMarkReserveNumberDao;
import com.seeyon.v3x.edoc.domain.EdocMark;
import com.seeyon.v3x.edoc.domain.EdocMarkCategory;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocMarkReserve;
import com.seeyon.v3x.edoc.domain.EdocMarkReserveNumber;
import com.seeyon.v3x.edoc.util.EdocMarkUtil.ReserveTypeEnum;
import com.seeyon.v3x.edoc.webmodel.EdocMarkNoModel;
import com.seeyon.v3x.edoc.webmodel.EdocMarkReserveVO;

import edu.emory.mathcs.backport.java.util.Collections;

public class EdocMarkReserveManagerImpl implements EdocMarkReserveManager {

	private static final Log log = LogFactory.getLog(EdocMarkReserveManagerImpl.class);
	
	private EdocMarkReserveDao edocMarkReserveDao;
	private EdocMarkReserveNumberDao edocMarkReserveNumberDao;
	private EdocMarkCategoryManager edocMarkCategoryManager;
	private EdocMarkDefinitionManager edocMarkDefinitionManager;
	private EdocMarkManager edocMarkManager;
	private EdocMarkHistoryManager edocMarkHistoryManager;
	private EdocMarkReserveCacheManager edocMarkReserveCacheManager;
	private AppLogManager appLogManager;
	
	/**
	 * 重新加载预留文号的缓存
	 * 使用点：保存预留文号出异常时调用
	 */
	public void reloadCache() {
		edocMarkReserveCacheManager.initialize();
	}
	
	/**
	 * 校验预留文号是否重复
	 * @param startNo
	 * @param endNo
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public boolean checkRepeatMarkReserved(Long markDefineId, int startNo, int endNo) throws BusinessException {
		return checkRepeatMarkReserved(markDefineId, startNo, endNo, new ArrayList<Long>(), new ArrayList<Long>());
	}
	@Override
	public boolean checkRepeatMarkReserved(EdocMarkDefinition markDef, int startNo, int endNo) throws BusinessException {
		return checkRepeatMarkReserved(markDef, startNo, endNo, new ArrayList<Long>(), new ArrayList<Long>());
	}
	@Override
	public boolean checkRepeatMarkReserved(Long markDefineId, int startNo, int endNo, List<Long> thisReservedIdList,  List<Long> delReservedIdList) throws BusinessException {
		EdocMarkDefinition markDef = edocMarkDefinitionManager.getMarkDefinition(markDefineId);
		return checkRepeatMarkReserved(markDef, startNo, endNo, thisReservedIdList, delReservedIdList);
	}
	@Override
	public boolean checkRepeatMarkReserved(EdocMarkDefinition markDef, int startNo, int endNo, List<Long> thisReservedIdList,  List<Long> delReservedIdList) throws BusinessException {
		boolean flag = false;
		List<Integer> markNumberList = edocMarkReserveCacheManager.getReserveMarkNumberListByDefineId_NotContains(markDef, thisReservedIdList, delReservedIdList);
		if(Strings.isNotEmpty(markNumberList)) {
			for(int number = startNo; number <= endNo; number++) {
				if(markNumberList.contains(number)) {
					flag = true;
					break;
				}
			}
		}
		return flag;
	}
	
	/**
	 * 校验预留文号值域是否重复
	 * @param markDefineId
	 * @param startNo
	 * @param endNo
	 * @param type
	 * @return
	 * @throws BusinessException
	 */
	public boolean checkRepeatMarkReserved(Long markDefineId, int startNo, int endNo, Integer type) throws BusinessException {
		boolean flag = false;
		EdocMarkDefinition markDef = edocMarkDefinitionManager.getMarkDefinition(markDefineId);
		List<Integer> markNumberList = edocMarkReserveCacheManager.getReserveMarkNumberListByDefineId(markDef);
		if(Strings.isNotEmpty(markNumberList)) {
			for(int number = startNo; number <= endNo; number++) {
				if(markNumberList.contains(number)) {
					flag = true;
					break;
				}
			}
		}
		return flag;
	}
	
	/**
	 * 文号管理-设置预留文号-添加预留文号
	 * @param edocMarkReserveVO
	 * @throws BusinessException
	 */
	public void saveEdocMarkReserve(EdocMarkDefinition markDef, List<EdocMarkReserveVO> addReserveList, List<Long> delReservedIdList) throws BusinessException {
		User user = AppContext.getCurrentUser();
		Boolean yearEnabled = markDef.getEdocMarkCategory()!=null ? markDef.getEdocMarkCategory().getYearEnabled() : false;
		/** 新增删除预留文号 */
		List<EdocMarkReserve> saveReserveList = new ArrayList<EdocMarkReserve>();
		List<EdocMarkReserveNumber> saveReserveNumberList = new ArrayList<EdocMarkReserveNumber>();
		List<EdocMarkReserveNumber> reserveNumberList = null;
		for(EdocMarkReserveVO reserveVO : addReserveList) {
			reserveNumberList = new ArrayList<EdocMarkReserveNumber>();
			EdocMarkReserve reserve = reserveVO.convert(reserveVO);
			reserve.setNewId();
			reserve.setYearEnabled(yearEnabled);
			String docMark = "";
			String docMarkEnd = "";
			for(int number = reserveVO.getStartNo().intValue(); number<=reserveVO.getEndNo().intValue(); number++) {
				EdocMarkReserveNumber reserveNumber = new EdocMarkReserveNumber();
				reserveNumber.setNewId();
				reserveNumber.setDocMark(this.getMarkReserveByFormat(markDef, number).getReserveNo());
				reserveNumber.setMarkNo(number);
				reserveNumber.setMarkDefineId(reserve.getMarkDefineId());
				reserveNumber.setReserveId(reserve.getId());
				reserveNumber.setType(reserve.getType());
				reserveNumber.setIsUsed(Boolean.FALSE);
				reserveNumberList.add(reserveNumber);
				saveReserveNumberList.add(reserveNumber);
				if(number == reserveVO.getStartNo().intValue()) {
					docMark = reserveNumber.getDocMark();
				}
			}
			if(reserveVO.getStartNo().intValue() != reserveVO.getEndNo().intValue()) {
				docMarkEnd = getMarkNumberFormatC(markDef, reserveVO.getEndNo().intValue());
			}
			reserve.setDocMark(docMark);
			reserve.setDocMarkEnd(docMarkEnd);
			reserveVO.setEdocMarkReserve(reserve);
			reserveVO.setReserveNumberList(reserveNumberList);
			saveReserveList.add(reserve);
		}
		/** 批量保存预留文号 */
		edocMarkReserveDao.savePatchAll(saveReserveList);
		/** 批量保存预留文号-具体 */
		edocMarkReserveNumberDao.savePatchAll(saveReserveNumberList);
		/** 批量删除预留文号 */
		edocMarkReserveDao.delete(delReservedIdList);
		/** 批量删除预留文号-具体 */
		edocMarkReserveNumberDao.deleteByReservedId(delReservedIdList);
		/** 缓存中删除预留文号 */
		List<EdocMarkReserveVO> delReserveVoList = new ArrayList<EdocMarkReserveVO>();
		for(Long delReservedId : delReservedIdList) {
			EdocMarkReserveVO reserveVO = this.getEdocMarkReserveVoById(delReservedId);
			if(reserveVO == null) {
				continue;
			}
			EdocMarkReserve reserve = reserveVO.getEdocMarkReserve();
			delReserveVoList.add(reserveVO);
			edocMarkReserveCacheManager.delCachEdocMarkReserve(delReservedId);
			//预留文号日志
			EdocMarkReserveVO showReserveVO = this.getMarkReserveByFormat(markDef, reserve.getYearNo(), reserve.getStartNo());
			showReserveVO.setFieldValue(reserve);
			String max = getNumberByFormat(reserve.getEndNo(), markDef.getLength());
			if(reserve.getStartNo()!=null && reserve.getEndNo()!=null && reserve.getStartNo().intValue() < reserve.getEndNo().intValue()) {
				showReserveVO.setReserveNo(showReserveVO.getReserveNo() + " "+ResourceUtil.getString("edoc.oper.to")+" " + max + showReserveVO.getFormatC());
			}
			appLogManager.insertLog(user, 316, user.getName(), showReserveVO.getReserveNo());
		}
		/** 缓存中保存预留文号 */
		for(EdocMarkReserveVO reserveVO : addReserveList) {
			edocMarkReserveCacheManager.addCachEdocMarkReserve(reserveVO);
		}
		/** 新增预留文号时，更新当前文号 */
		EdocMarkCategory edocMarkCategory = markDef.getEdocMarkCategory();
		int currentNo = edocMarkCategory.getCurrentNo();
		List<Integer> markNumberList =  edocMarkReserveCacheManager.getReserveMarkNumberList_Morethan(markDef, currentNo);
		if(Strings.isNotEmpty(markNumberList)) {//有大于=当前文号的预留文号
			if(markNumberList.contains(currentNo)) {//此处不用+1
				currentNo = getEdocMarkReservedCurrentNo(markNumberList, currentNo);
			}
		}
		if(edocMarkCategory != null) {
			edocMarkCategory.setCurrentNo(currentNo);
			edocMarkCategoryManager.updateCategory(edocMarkCategory);
		}
		/** 删除预留文号时，未使用、未占用的自动生成断号 */
		Map<String, Integer> usedMarkMap = new HashMap<String, Integer>();
		List<EdocMark> edocMarkedList = edocMarkManager.findListByMarkDefineId(markDef.getId());
		for(EdocMark edocMark : edocMarkedList) {
			usedMarkMap.put(edocMark.getDocMark(), edocMark.getDocMarkNo());
		}
		List<EdocMark> edocMarkList = new ArrayList<EdocMark>();
		for(EdocMarkReserveVO delReserveVo : delReserveVoList) {
			for(EdocMarkReserveNumber reserveNumber : delReserveVo.getReserveNumberList()) {
				if(reserveNumber.getMarkNo().intValue() > currentNo) {
					continue;
				}
				//若断号中已经包括被删除的预留文号，则不再进断号
				if(usedMarkMap.containsKey(reserveNumber.getMarkNo().toString())) {
					continue;
				}
				//校验被删除的预留文号是否已被占用，若已被占用则不进断号
				if(	edocMarkHistoryManager.isUsed(reserveNumber.getDocMark(), -1L)) {
					continue;
				}
				EdocMark docMark = new EdocMark();
				docMark.setNewId();
				docMark.setCategoryId(edocMarkCategory.getId());
				docMark.setCreateTime(DateUtil.currentDate());
				docMark.setCreateUserId(user.getId());
				docMark.setEdocId(-1L);
				docMark.setDocMarkNo(reserveNumber.getMarkNo());
				docMark.setDocMark(reserveNumber.getDocMark());
				docMark.setDomainId(user.getLoginAccount());
				docMark.setEdocMarkDefinition(markDef);
				docMark.setMarkNum(1);
				edocMarkList.add(docMark);
			}
		}
		edocMarkManager.save(edocMarkList);
	}
	
	/**
	 * 获取预留文号
	 * @param reserveId
	 * @return
	 * @throws BusinessException
	 */
	public EdocMarkReserve getEdocMarkReserveById(Long reserveId) throws BusinessException {
		return this.edocMarkReserveCacheManager.getCacheEdocMarkReserveById(reserveId);
	}
	
	/**
	 * 获取预留文号Vo
	 * @param reserveId
	 * @return
	 * @throws BusinessException
	 */
	public EdocMarkReserveVO getEdocMarkReserveVoById(Long reserveId) throws BusinessException {
		return this.edocMarkReserveCacheManager.getCacheEdocMarkReserveVoById(reserveId);
	}
	
	/**
	 * 获取文号某类型的预留文号，通过参数判断是否获取所有预留文号
	 * @param markDefineId
	 * @param type 1线上 2线下 -1所有
	 * @return
	 * @throws BusinessException
	 */
	public List<EdocMarkReserveNumber> findAllEdocMarkReserveNumberList() throws BusinessException {
		return findEdocMarkReserveNumberList(null, ReserveTypeEnum.reserve_all.getReserveType());
	}
	public List<EdocMarkReserveNumber> findEdocMarkReserveNumberList(Integer type) throws BusinessException {
		return findEdocMarkReserveNumberList(null, type);
	}
	public List<EdocMarkReserveNumber> findEdocMarkReserveNumberList(EdocMarkDefinition markDef, Integer type) throws BusinessException {
		
		List<EdocMarkReserveNumber> reserveNumberList = new ArrayList<EdocMarkReserveNumber>();
		
		List<EdocMarkReserveVO> reserveVoList = null;
		
		if(markDef == null) {
			reserveVoList = new ArrayList<EdocMarkReserveVO>();
		} else {
			reserveVoList = edocMarkReserveCacheManager.getReserveVoListByDefineId(markDef.getId(), markDef.getEdocMarkCategory().getYearEnabled());
		}
		if(Strings.isNotEmpty(reserveVoList)) {
			for(EdocMarkReserveVO reserveVO : reserveVoList) {
				boolean flag = (type != null && reserveVO.getType().intValue() == type.intValue()) 
				        || (type == null || type.equals(ReserveTypeEnum.reserve_all.getReserveType()));
				if(flag) {
					for(EdocMarkReserveNumber bean : reserveVO.getReserveNumberList()) {
						EdocMarkNoModel markNoVo = edocMarkDefinitionManager.analyzeEdocMarkVo(bean.getDocMark(), markDef);
						bean.setMarkNoVo(markNoVo);
					}
					reserveNumberList.addAll(reserveVO.getReserveNumberList());
				}
			}
		}
		return reserveNumberList;
	}
	
	/**
	 * 显示某预留文号段(按格式显示)
	 */
	public Map<Long, List<EdocMarkReserveVO>> findAllEdocMarkReserveListMap() throws BusinessException {
		return edocMarkReserveCacheManager.getAllEdocMarkReserveListMap();
	}
	public List<EdocMarkReserveVO> findEdocMarkReserveList(EdocMarkDefinition markDef) throws BusinessException {
		return findEdocMarkReserveList(markDef, -1);
	}
	public List<EdocMarkReserveVO> findEdocMarkReserveList(EdocMarkDefinition markDef, Integer queryNumber) throws BusinessException {
		return findEdocMarkReserveList(-1, markDef, queryNumber);
	}
	public List<EdocMarkReserveVO> findEdocMarkReserveList(Integer type, EdocMarkDefinition markDef, Integer queryNumber) throws BusinessException {
		List<EdocMarkReserveVO> reserveVoList = edocMarkReserveCacheManager.queryEdocMarkReserveList(type, markDef.getId(), markDef.getEdocMarkCategory().getYearEnabled(), queryNumber);
		if(Strings.isNotEmpty(reserveVoList)) {
			Collections.sort(reserveVoList, new EdocMarkReserveVO());
		}
		return reserveVoList;
	}
	
	@Override
	public List<EdocMarkReserveNumber> saveMarkReserverNew(User user, Integer type, EdocMarkDefinition markDef, List<EdocMarkReserveVO> addReserveList, List<Long> delReservedIdList, List<String> delReservedNoList) throws BusinessException {
		List<EdocMarkReserveNumber> saveReserveNumberList = new ArrayList<EdocMarkReserveNumber>();
		Boolean yearEnabled = markDef.getEdocMarkCategory()!=null ? markDef.getEdocMarkCategory().getYearEnabled() : false;
		/** 新增预留文号 */
		if(Strings.isNotEmpty(addReserveList)) {
			List<EdocMarkReserve> saveReserveList = new ArrayList<EdocMarkReserve>();
			List<EdocMarkReserveNumber> reserveNumberList = null;
			for(EdocMarkReserveVO reserveVO : addReserveList) {
				reserveNumberList = new ArrayList<EdocMarkReserveNumber>();
				EdocMarkReserve reserve = reserveVO.convert(reserveVO);
				reserve.setNewId();
				reserve.setYearEnabled(yearEnabled);
				reserve.setMarkDefineId(markDef.getId());
				String docMark = "";
				String docMarkEnd = "";
				for(int number = reserveVO.getStartNo().intValue(); number<=reserveVO.getEndNo().intValue(); number++) {
					EdocMarkReserveNumber reserveNumber = new EdocMarkReserveNumber();
					reserveNumber.setNewId();
					reserveNumber.setDocMark(this.getMarkReserveByFormat(markDef, number).getReserveNo());
					reserveNumber.setMarkNo(number);
					reserveNumber.setYearNo(reserve.getYearNo());
					reserveNumber.setMarkDefineId(reserve.getMarkDefineId());
					reserveNumber.setReserveId(reserve.getId());
					reserveNumber.setType(reserve.getType());
					if(reserveVO.getType().intValue() == 1) {//线上预留
						reserveNumber.setIsUsed(Boolean.FALSE);
					} else {//线下预留
						reserveNumber.setIsUsed(Boolean.TRUE);	
					}
					reserveNumberList.add(reserveNumber);
					saveReserveNumberList.add(reserveNumber);
					if(number == reserveVO.getStartNo().intValue()) {
						docMark = reserveNumber.getDocMark();
					}
				}
				if(reserveVO.getStartNo().intValue() != reserveVO.getEndNo().intValue()) {
					docMarkEnd = this.getMarkNumberFormatC(markDef, reserveVO.getEndNo().intValue());
				}
				reserve.setDocMark(docMark);
				reserve.setDocMarkEnd(docMarkEnd);
				reserve.setDescription(reserveVO.getDescription());
				reserveVO.setEdocMarkReserve(reserve);
				reserveVO.setReserveNumberList(reserveNumberList);
				saveReserveList.add(reserve);
			}
			/** 批量保存预留文号 */
			edocMarkReserveDao.savePatchAll(saveReserveList);
			/** 批量保存预留文号-具体 */
			edocMarkReserveNumberDao.savePatchAll(saveReserveNumberList);
			/** 缓存中保存预留文号 */
			for(EdocMarkReserveVO reserveVO : addReserveList) {
				edocMarkReserveCacheManager.addCachEdocMarkReserve(reserveVO);
			}
		}
		return saveReserveNumberList;
	}
	
	@Override
	public Integer getCurrentNoForReserveNew(EdocMarkDefinition markDef) throws BusinessException {
		int currentNo = markDef.getEdocMarkCategory().getCurrentNo();
		List<Integer> markNumberList =  edocMarkReserveCacheManager.getReserveMarkNumberList_Morethan(markDef, currentNo);
		if(Strings.isNotEmpty(markNumberList)) {//有大于=当前文号的预留文号
			if(markNumberList.contains(currentNo)) {//此处不用+1
				currentNo = getEdocMarkReservedCurrentNo(markNumberList, currentNo);
			}
		}
		return currentNo;
	}
	
	@Override
	public void updateMarkReserveIsUsedNew(List<Long> markDefIdList, List<String> markstrList, boolean isUsed) throws BusinessException {
		if(Strings.isNotEmpty(markDefIdList) && Strings.isNotEmpty(markstrList)) {
			edocMarkReserveNumberDao.updateMarkReserveIsUsedNew(markDefIdList, markstrList,  isUsed);
			edocMarkReserveCacheManager.updateCachEdocMarkReserveByMarkstr(markDefIdList, markstrList, isUsed);
		}
	}
	
	@Override
	public List<EdocMarkReserveVO> deleteMarkReserveNew(User user, Integer type, EdocMarkDefinition markDef, List<Long> delReservedIdList, List<String> delReservedNoList) throws BusinessException {
		List<EdocMarkReserveVO> delReserveVoList = new ArrayList<EdocMarkReserveVO>();
		
		//为解决删除同大流水其它机构代字的预留文号
		List<Long> thisDelReservedIdList = new ArrayList<Long>();
		Map<Long, EdocMarkReserveVO> delReserveMap = new HashMap<Long, EdocMarkReserveVO>();
		if(Strings.isNotEmpty(delReservedNoList)) {
			for(int i=0; i<delReservedNoList.size(); i++) {
				String[] startAndEnd =  delReservedNoList.get(i).split("-");
				Integer startNo = Integer.parseInt(startAndEnd[0]);
				Integer endNo = Integer.parseInt(startAndEnd[1]);
				List<EdocMarkReserveVO> delVoList = edocMarkReserveCacheManager.getReserveVoListByDefineId(markDef.getId(), markDef.getEdocMarkCategory().getYearEnabled(), startNo, endNo);
				for(EdocMarkReserveVO delVo : delVoList) {
					if(!thisDelReservedIdList.contains(delVo.getId())) {
						thisDelReservedIdList.add(delVo.getId());
						delReserveVoList.add(delVo);
						delReserveMap.put(delVo.getId(), delVo);
					}
				}
			}
		} else if(Strings.isNotEmpty(delReservedIdList)) {
			thisDelReservedIdList.addAll(delReservedIdList);
		}
		if(Strings.isNotEmpty(thisDelReservedIdList)) {
			/** 批量删除预留文号 */
			edocMarkReserveDao.delete(thisDelReservedIdList);
			/** 批量删除预留文号-具体 */
			edocMarkReserveNumberDao.deleteByReservedId(thisDelReservedIdList);
		}
		/** 缓存中删除预留文号 */
		for(Long delReservedId : thisDelReservedIdList) {
			EdocMarkReserveVO reserveVO = delReserveMap.get(delReservedId);
			if(reserveVO == null) {
				reserveVO = this.getEdocMarkReserveVoById(delReservedId);
			}
			if(reserveVO == null) {
				continue;
			}
			EdocMarkReserve reserve = reserveVO.getEdocMarkReserve();
			edocMarkReserveCacheManager.delCachEdocMarkReserve(delReservedId);
			//预留文号日志
			EdocMarkReserveVO showReserveVO = this.getMarkReserveByFormat(markDef, reserve.getYearNo(), reserve.getStartNo());
			showReserveVO.setFieldValue(reserve);
			String max = getNumberByFormat(reserve.getEndNo(), markDef.getLength());
			if(reserve.getStartNo()!=null && reserve.getEndNo()!=null && reserve.getStartNo().intValue() < reserve.getEndNo().intValue()) {
				showReserveVO.setReserveNo(showReserveVO.getReserveNo() + " "+ResourceUtil.getString("edoc.oper.to")+" " + max + showReserveVO.getFormatC());
			}
			appLogManager.insertLog(user, 316, user.getName(), showReserveVO.getReserveNo());
		}
		return delReserveVoList;
	}
	
	/**
	 * 按格式显示某一个文号
	 * @param markDef
	 * @param markNumber 流水号
	 * @return
	 */
	public EdocMarkReserveVO getMarkReserveByFormat(EdocMarkDefinition markDef, Integer reserveYearNo, Integer markNumber) {
		EdocMarkReserveVO reserveVO = new EdocMarkReserveVO();
		reserveVO.setMarkDefineId(markDef.getId());
		reserveVO.setWordNo(markDef.getWordNo());
		reserveVO.setExpression(markDef.getExpression());
		reserveVO.setEdocMarkDefinition(markDef);
		reserveVO.setEdocMarkCategory(markDef.getEdocMarkCategory());
		String expression = reserveVO.getExpression();
		String yearNo = "";
		String formatA = "";
		String formatB = ""; 
		Boolean yearEnabled = reserveVO.getEdocMarkCategory()!=null ? reserveVO.getEdocMarkCategory().getYearEnabled() : false;
		if (yearEnabled) {
			formatA = expression.substring(expression.indexOf("$WORD") + 5, expression.indexOf("$YEAR"));
			if(reserveYearNo == null) {
				Calendar cal = Calendar.getInstance();
				yearNo = String.valueOf(cal.get(Calendar.YEAR));
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
			String number = this.getNumberByFormat(markNumber, markDef.getLength());
			reserveVO.setReserveLimitNo(Strings.getLimitLengthString(wordNo, 15, "...")+reserveVO.getFormatA()+reserveVO.getYearNo()+reserveVO.getFormatB() + number + reserveVO.getFormatC());
			String markstr = wordNo+reserveVO.getFormatA()+reserveVO.getYearNo()+reserveVO.getFormatB() + number + reserveVO.getFormatC();

	    	if(Strings.isNotBlank(markstr)){
	    		markstr = markstr.replaceAll(String.valueOf((char)160), String.valueOf((char)32));
	    	}
			
			reserveVO.setReserveNo(markstr);
		}
		return reserveVO;
	}
	
	/**
	 * 按格式显示某一个文号
	 * @param markDef
	 * @param markNumber 流水号
	 * @return
	 */
	public EdocMarkReserveVO getMarkReserveByFormat(EdocMarkDefinition markDef, Integer markNumber) {
		return getMarkReserveByFormat(markDef, null, markNumber);
	}
	
	/**
	 * 自动跳过预留文号，生成当前文号
	 * @param markDef
	 * @param currentNo
	 * @return
	 */
	public Integer autoMakeEdocMarkCurrentNo(EdocMarkDefinition markDef, Integer currentNo) {
		try {
			List<Integer> markNumberList =  edocMarkReserveCacheManager.getReserveMarkNumberList_Morethan(markDef, currentNo);
			if(Strings.isNotEmpty(markNumberList)) {//有大于=当前文号的预留文号
				currentNo = currentNo + 1;
				if(markNumberList.contains(currentNo)) {
					currentNo = getEdocMarkReservedCurrentNo(markNumberList, currentNo);
				}
			} else {//无预留文号
				currentNo = currentNo + 1;
			}
		} catch(BusinessException e) {
			log.error("获取下一文号出错", e);
		}
		return currentNo;
	}
	
	/**
	 * 递归调用，获取非预留文号的当前文号
	 * @param markNumberList
	 * @param currentNo
	 * @return
	 * @throws BusinessException
	 */
	private Integer getEdocMarkReservedCurrentNo(List<Integer> markNumberList, Integer currentNo) throws BusinessException {
		if(Strings.isNotEmpty(markNumberList) && markNumberList.contains(currentNo)) {
			currentNo = getEdocMarkReservedCurrentNo(markNumberList, currentNo+1).intValue();
		}
		return currentNo;
	}
	
	/**
	 * 
	 * @param markDef
	 * @param markNumber
	 * @return
	 */
	private String getMarkNumberFormatC(EdocMarkDefinition markDef, Integer markNumber) {
		String expression = markDef.getExpression();
		String formatC = expression.substring(expression.indexOf("$NO") + 3);
		String number = "";
		if(markNumber != null) {
			number = this.getNumberByFormat(markNumber, markDef.getLength());
		}
		return number + formatC;
	}
	
	/**
	 * 按文号长度在流水号前补0
	 * @param number
	 * @param length
	 * @return
	 */
	private String getNumberByFormat(int number, int length) {
		String number_str = "";
		String number_s = String.valueOf(number);
		for(int i=number_s.length(); i<length; i++) {
			number_str += "0";
		}
		number_str += number;
		return number_str;
	}

	public void setEdocMarkDefinitionManager(EdocMarkDefinitionManager edocMarkDefinitionManager) {
		this.edocMarkDefinitionManager = edocMarkDefinitionManager;
	}

	public void setEdocMarkReserveDao(EdocMarkReserveDao edocMarkReserveDao) {
		this.edocMarkReserveDao = edocMarkReserveDao;
	}

	public void setEdocMarkCategoryManager(EdocMarkCategoryManager edocMarkCategoryManager) {
		this.edocMarkCategoryManager = edocMarkCategoryManager;
	}

	public void setEdocMarkManager(EdocMarkManager edocMarkManager) {
		this.edocMarkManager = edocMarkManager;
	}

	public void setEdocMarkHistoryManager(EdocMarkHistoryManager edocMarkHistoryManager) {
		this.edocMarkHistoryManager = edocMarkHistoryManager;
	}

	public void setEdocMarkReserveCacheManager(EdocMarkReserveCacheManager edocMarkReserveCacheManager) {
		this.edocMarkReserveCacheManager = edocMarkReserveCacheManager;
	}

	public void setAppLogManager(AppLogManager appLogManager) {
		this.appLogManager = appLogManager;
	}

	public void setEdocMarkReserveNumberDao(EdocMarkReserveNumberDao edocMarkReserveNumberDao) {
		this.edocMarkReserveNumberDao = edocMarkReserveNumberDao;
	}

	@Override
	public String hasMarkReserve(String markDefId) throws BusinessException {
		Long markId = 0L;
		if(Strings.isNotBlank(markDefId)){
			markId = Long.valueOf(markDefId);
		}
		List<EdocMarkReserve> markReserves=edocMarkReserveDao.findList(markId);
		if(markReserves != null && markReserves.size() > 0){
			return "true";
		}
		return "false";
	}

	@Override
	public void deleteByMarkDefineId(Long markDefineId)
			throws BusinessException {
		List<Long> delReservedIdList = new ArrayList<Long>();
		List<EdocMarkReserve> markReserves = edocMarkReserveDao.findAll();
		for(EdocMarkReserve markRes : markReserves){
			if(markRes != null && markDefineId.equals(markRes.getMarkDefineId())){
				delReservedIdList.add(markRes.getId());
				edocMarkReserveCacheManager.delCachEdocMarkReserve(markRes.getId());//清缓存
			}
		}
		//从数据库删除
		edocMarkReserveDao.delete(delReservedIdList);
		edocMarkReserveNumberDao.deleteByReservedId(delReservedIdList);
	}

	@Override
	public List<EdocMarkReserveNumber> findEdocMarkReserveNumberListByDB(EdocMarkDefinition markDef, Integer type) throws BusinessException {
		List<EdocMarkReserveNumber> reserveNumberList = new ArrayList<EdocMarkReserveNumber>();
		reserveNumberList.addAll(edocMarkReserveNumberDao.findAllNotUsed(markDef));
		return reserveNumberList;
	}
	
}
