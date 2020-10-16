package com.seeyon.v3x.edoc.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AbstractSystemInitializer;
import com.seeyon.ctp.common.cache.CacheAccessable;
import com.seeyon.ctp.common.cache.CacheFactory;
import com.seeyon.ctp.common.cache.CacheMap;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.dao.EdocMarkReserveDao;
import com.seeyon.v3x.edoc.dao.EdocMarkReserveNumberDao;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocMarkReserve;
import com.seeyon.v3x.edoc.domain.EdocMarkReserveNumber;
import com.seeyon.v3x.edoc.util.EdocMarkUtil;
import com.seeyon.v3x.edoc.webmodel.EdocMarkReserveVO;

/**
 * 预留文号缓存管理
 * @author tanggl
 *
 */
public class EdocMarkReserveCacheManager extends AbstractSystemInitializer{
	
	private static final Log log = LogFactory.getLog(EdocMarkReserveCacheManager.class);

	private EdocMarkReserveDao edocMarkReserveDao;
	private EdocMarkReserveNumberDao edocMarkReserveNumberDao;
	
	/** 预留文号ID&预留文号对象 */
	private CacheMap<Long, EdocMarkReserveVO> markReservedVoMap = null;//文号当年的预留文号
	private CacheMap<Long, EdocMarkReserveVO> historyMarkReservedVoMap = null;//文号往年的预留文号
	
	/**
	 * 初始化数据
	 */
	public void initialize() {
		CacheAccessable factory = CacheFactory.getInstance(EdocMarkReserveCacheManager.class); 
        //初始化缓存数据
        if(markReservedVoMap != null) {
        	markReservedVoMap.clear();
        	historyMarkReservedVoMap.clear();
        } else {
        	markReservedVoMap = factory.createMap("markReservedVoMap");
        	historyMarkReservedVoMap = factory.createMap("historyMarkReservedVoMap");
        }
        try {
        	List<EdocMarkReserve> reserveList =  edocMarkReserveDao.findAll();
        	List<EdocMarkReserveNumber> allReserveNumberList =  edocMarkReserveNumberDao.findAll();
        	if(Strings.isNotEmpty(reserveList)) {
        		Integer yearNo = Calendar.getInstance().get(Calendar.YEAR);
        		List<EdocMarkReserveNumber> reserveNumberList = null;
        		for(EdocMarkReserve reserved : reserveList) {
        			reserveNumberList = null;
        			if(reserved.getYearNo() != null) {
        				EdocMarkReserveVO reserveVO = new EdocMarkReserveVO();
						reserveVO.setFieldValue(reserved);
						reserveVO.setEdocMarkReserve(reserved);
						//当年的预留文号
        				if(yearNo.intValue() == reserved.getYearNo()) {
        					EdocMarkReserveVO cacheVO = markReservedVoMap.get(reserved.getId());
        					if(cacheVO != null) {
    							reserveNumberList = cacheVO.getReserveNumberList();
    						}
    						if(reserveNumberList == null) {
    							reserveNumberList = new ArrayList<EdocMarkReserveNumber>();
    						}
    						if(Strings.isNotEmpty(allReserveNumberList)) {
            					for(EdocMarkReserveNumber reserveNumber : allReserveNumberList) {
            						if(reserved.getId().longValue() == reserveNumber.getReserveId().longValue()) {
            							reserveNumberList.add(reserveNumber);
            						}
            					}
            				}
    						reserveVO.setReserveNumberList(reserveNumberList);
        					markReservedVoMap.put(reserved.getId(), reserveVO);
        				} else {//往年的预留文号
        					EdocMarkReserveVO cacheVO = historyMarkReservedVoMap.get(reserved.getId());
        					if(cacheVO != null) {
    							reserveNumberList = cacheVO.getReserveNumberList();
    						}
    						if(reserveNumberList == null) {
    							reserveNumberList = new ArrayList<EdocMarkReserveNumber>();
    						}
    						if(Strings.isNotEmpty(allReserveNumberList)) {
            					for(EdocMarkReserveNumber reserveNumber : allReserveNumberList) {
            						if(reserved.getId().longValue() == reserveNumber.getReserveId().longValue()) {
            							reserveNumberList.add(reserveNumber);
            						}
            					}
            				}
    						reserveVO.setReserveNumberList(reserveNumberList);
    						historyMarkReservedVoMap.put(reserved.getId(), reserveVO);
        				}
        			}
        		}
        	}
        } catch(BusinessException e) {
        	log.error("公文文号预留获取出错", e);
        }
	}
	
	/**
	 * 获取某个预留文号对象
	 * @param reserveId
	 * @return
	 * @throws BusinessException
	 */
	public EdocMarkReserve getCacheEdocMarkReserveById(Long reserveId) throws BusinessException {
		reloadByAcrossYears();
		EdocMarkReserveVO reserveVO = markReservedVoMap.get(reserveId);
		return reserveVO == null ? null : reserveVO.getEdocMarkReserve();
	}
	
	/**
	 * 获取某个预留文号对象
	 * @param reserveId
	 * @return
	 * @throws BusinessException
	 */
	public EdocMarkReserveVO getCacheEdocMarkReserveVoById(Long reserveId) throws BusinessException {
		reloadByAcrossYears();
		return markReservedVoMap.get(reserveId);
	}
	
	/**
	 * 判断某预留文号是否已存在
	 * @param reserveId
	 * @return
	 * @throws BusinessException
	 */
	public boolean containsReserveId(Long reserveId) throws BusinessException {
		reloadByAcrossYears();
		return markReservedVoMap.contains(reserveId);
	}
	
	/**
	 * 添加预留文号对象，往缓存中添加数据
	 * @param reserve
	 */
	public void addCachEdocMarkReserve(EdocMarkReserveVO reserveVO) {
		reloadByAcrossYears();
		this.markReservedVoMap.put(reserveVO.getEdocMarkReserve().getId(), reserveVO);
	}
	
	/**
	 * 删除预留对象
	 * @param markDef
	 * @param delReservedId
	 */
	public void delCachEdocMarkReserve(Long delReservedId) {
		reloadByAcrossYears();
		if(markReservedVoMap.contains(delReservedId)) {
			markReservedVoMap.remove(delReservedId);
		}
	}

	public void updateCachEdocMarkReserveByMarkstr(List<Long> markDefIdList, List<String> markstrList, boolean isUsed) {
		reloadByAcrossYears();
		for(EdocMarkReserveVO reserve : markReservedVoMap.values()) {
			for(EdocMarkReserveNumber number : reserve.getReserveNumberList()) {
				/*if(markDefIdList.contains(number.getMarkDefineId()) && markstrList.contains(number.getDocMark())) {
					number.setIsUsed(isUsed);
				}*/
				if(markstrList.contains(number.getDocMark())) {
					number.setIsUsed(isUsed);
				}
			}
		}
		for(EdocMarkReserveVO reserve : historyMarkReservedVoMap.values()) {
			for(EdocMarkReserveNumber number : reserve.getReserveNumberList()) {
				if(markstrList.contains(number.getDocMark())) {
					number.setIsUsed(isUsed);
				}
			}
		}
	}
	
	/**
	 * 获取某文号的所有预留文号段集合(该处返回预留文号段)
	 * @param markDefineId
	 * @return
	 * @throws BusinessException
	 */
	public List<EdocMarkReserveVO> getReserveVoListByDefineId(Long defineId, Boolean yearEnabled, Integer startNo, Integer endNo) throws BusinessException {
		reloadByAcrossYears();
		List<EdocMarkReserveVO> reserveList = new ArrayList<EdocMarkReserveVO>();
		for(EdocMarkReserveVO reserve : markReservedVoMap.values()) {
			if(defineId.longValue() == reserve.getMarkDefineId().longValue()) {
				if(reserve.getStartNo().intValue() == startNo.intValue() && reserve.getEndNo().intValue() == endNo.intValue()) {
					reserveList.add(reserve);
				}
			}
		}
		if(!yearEnabled) {
			for(EdocMarkReserveVO reserve : historyMarkReservedVoMap.values()) {
				if(defineId.longValue() == reserve.getMarkDefineId().longValue()) {
					if(reserve.getStartNo().intValue() == startNo.intValue() && reserve.getEndNo().intValue() == endNo.intValue()) {
						reserveList.add(reserve);
					}
				}
			}
		}
		return reserveList;
	}
	
	/**
	 * 获取某文号的所有预留文号段集合(该处返回预留文号段)
	 * @param markDefineId
	 * @return
	 * @throws BusinessException
	 */
	public List<EdocMarkReserveVO> getReserveVoListByDefineId(Long defineId, Boolean yearEnabled) throws BusinessException {
		reloadByAcrossYears();
		List<EdocMarkReserveVO> reserveList = new ArrayList<EdocMarkReserveVO>();
		for(EdocMarkReserveVO reserve : markReservedVoMap.values()) {
			if(defineId.longValue() == reserve.getMarkDefineId().longValue()) {
				reserveList.add(reserve);
			}
		}
		if(!yearEnabled) {
			for(EdocMarkReserveVO reserve : historyMarkReservedVoMap.values()) {
				if(defineId.longValue() == reserve.getMarkDefineId().longValue()) {
					reserveList.add(reserve);
				}
			}
		}
		return reserveList;
	}
	
	/**
	 * 获取某文号的所有预留文号段集合(该处返回预留文号段)
	 * @param markDefineId
	 * @return
	 * @throws BusinessException
	 */
	public List<EdocMarkReserveVO> getAllReserveVoList() throws BusinessException {
		reloadByAcrossYears();
		List<EdocMarkReserveVO> reserveList = new ArrayList<EdocMarkReserveVO>();
		for(EdocMarkReserveVO reserve : markReservedVoMap.values()) {
			reserveList.add(reserve);
		}
		for(EdocMarkReserveVO reserve : historyMarkReservedVoMap.values()) {
			reserveList.add(reserve);
		}
		return reserveList;
	}
	
	/**
	 * 获取所有(不包含指定预留文号)预留文号流水号集合：预留文号&预留文号流水集合
	 * @return
	 */
	public List<Integer> getReserveMarkNumberListByDefineId(EdocMarkDefinition markDef) throws BusinessException {
		reloadByAcrossYears();
		List<Integer> markNumberList = new ArrayList<Integer>();
		List<EdocMarkReserveVO> reserveVoList = getReserveVoListByDefineId(markDef.getId(), markDef.getEdocMarkCategory().getYearEnabled());
		if(Strings.isNotEmpty(reserveVoList)) {
			for(EdocMarkReserveVO reserveVO : reserveVoList) {
				if(Strings.isNotEmpty(reserveVO.getReserveNumberList())) {
					for(EdocMarkReserveNumber reserveNumber : reserveVO.getReserveNumberList()) {
						if(EdocMarkUtil.isNeedExcludeDocMarkToSelect(markDef, reserveNumber.getDocMark())) continue;
						markNumberList.add(reserveNumber.getMarkNo());
					}
				}
			}
		}
		return markNumberList;
	}
	
	/**
	 * 获取所有(不包含指定预留文号)预留文号流水号集合：预留文号&预留文号流水集合
	 * @param delReservedIdList 同时删除的文号
	 * @return
	 */
	public List<Integer> getReserveMarkNumberListByDefineId_NotContains(EdocMarkDefinition markDef, List<Long> thisReservedIdList, List<Long> delReservedIdList) throws BusinessException {
		reloadByAcrossYears();
		List<Integer> markNumberList = new ArrayList<Integer>();
		List<EdocMarkReserveVO> reserveVoList = getReserveVoListByDefineId(markDef.getId(), markDef.getEdocMarkCategory().getYearEnabled());
		if(Strings.isNotEmpty(reserveVoList)) {
			for(EdocMarkReserveVO reserveVO : reserveVoList) {
				if(Strings.isNotEmpty(reserveVO.getReserveNumberList())) {
				    
				    if(delReservedIdList.contains(reserveVO.getEdocMarkReserve().getId())){
				        continue;//同时删除和新增，新增判重，不考虑删除的列表
				    }
				    
					for(EdocMarkReserveNumber reserveNumber : reserveVO.getReserveNumberList()) {
						if(EdocMarkUtil.isNeedExcludeDocMarkToSelect(markDef, reserveNumber.getDocMark())) {
						    continue;
						}
						boolean checkFlag = false;//预留文号段不与自己做对比
						if(Strings.isNotEmpty(thisReservedIdList)) {
							if(!thisReservedIdList.contains(reserveNumber.getMarkNo().longValue())) {
								checkFlag = true;
							}
						} else {
							checkFlag = true;
						}
						if(checkFlag) {
							markNumberList.add(reserveNumber.getMarkNo());
						}
					}
				}
			}
		}
		return markNumberList;
	}
	
	/**
	 * 
	 * @param markDef
	 * @param currentNo
	 * @return
	 * @throws BusinessException
	 */
	public  List<Integer> getReserveMarkNumberList_Morethan(EdocMarkDefinition markDef, Integer currentNo) throws BusinessException {
		reloadByAcrossYears();
		List<EdocMarkReserveVO> reserveVoList = getReserveVoListByDefineId(markDef.getId(), markDef.getEdocMarkCategory().getYearEnabled());
		List<Integer> markNumberList = new ArrayList<Integer>();
		if(Strings.isNotEmpty(reserveVoList)) {
			for(EdocMarkReserveVO reserveVO : reserveVoList) {
				if(Strings.isNotEmpty(reserveVO.getReserveNumberList())) {
					for(EdocMarkReserveNumber reserveNumber : reserveVO.getReserveNumberList()) {
						if(reserveNumber.getMarkNo().intValue() >= currentNo) {
							if(EdocMarkUtil.isNeedExcludeDocMarkToSelect(markDef, reserveNumber.getDocMark())) continue;
							markNumberList.add(reserveNumber.getMarkNo());
						}
					}
				}
			}
		}
		return markNumberList;
	}
	
	/**
	 * 文号预留文号列表
	 * @param markDefineId
	 * @param queryNumber
	 * @return
	 * @throws BusinessException
	 */
	public List<EdocMarkReserveVO> queryEdocMarkReserveList(Long markDefineId, Boolean yearEnabled, Integer queryNumber) throws BusinessException {
		return getReserveVoMap_Query(-1, yearEnabled, queryNumber).get(markDefineId);
	}
	public List<EdocMarkReserveVO> queryEdocMarkReserveList(Integer type, Long markDefineId, Boolean yearEnabled, Integer queryNumber) throws BusinessException {
		return getReserveVoMap_Query(type, yearEnabled, queryNumber).get(markDefineId);
	}
	public Map<Long, List<EdocMarkReserveVO>> getAllEdocMarkReserveListMap() throws BusinessException {
		return getReserveVoMap_Query(-1, Boolean.FALSE, -1);
	}
	private Map<Long, List<EdocMarkReserveVO>> getReserveVoMap_Query(Integer type, Boolean yearEnable, Integer queryNumber) throws BusinessException {
		reloadByAcrossYears();
		Map<Long, List<EdocMarkReserveVO>> reserveListMap = new HashMap<Long, List<EdocMarkReserveVO>>();
		for(EdocMarkReserveVO reserveVO : markReservedVoMap.values()) {
			if(type != null && type.intValue() == 1) {//过滤线上预留文号
				if(reserveVO.getType().intValue() != 1) {
					continue;
				}
			} else if(type != null && type.intValue() == 2) {//过滤线下预留文号
				if(reserveVO.getType().intValue() != 2) {
					continue;
				}
			}
			EdocMarkReserve reserve = reserveVO.getEdocMarkReserve();
			reserveVO.setFieldValue(reserve);
			if(reserve.getStartNo().intValue() != reserve.getEndNo().intValue()) {
				reserveVO.setDocMarkDisplay(reserve.getDocMark() + " "+ResourceUtil.getString("edoc.oper.to")+" " + reserve.getDocMarkEnd());
			} else {
				reserveVO.setDocMarkDisplay(reserve.getDocMark());
			}
			if(queryNumber.intValue()==-1 || (queryNumber.intValue()>=reserveVO.getStartNo().intValue() && queryNumber.intValue()<=reserveVO.getEndNo().intValue())) {
				if(Strings.isEmpty(reserveListMap.get(reserveVO.getMarkDefineId()))) {
					List<EdocMarkReserveVO> reserveList = new ArrayList<EdocMarkReserveVO>();
					reserveList.add(reserveVO);
					reserveListMap.put(reserveVO.getMarkDefineId(), reserveList);
				} else {
					reserveListMap.get(reserveVO.getMarkDefineId()).add(reserveVO);
				}
			}
		}
		if(!yearEnable) {//如果不按年度编号，则显示所有的预留文号
			for(EdocMarkReserveVO reserveVO : historyMarkReservedVoMap.values()) {
				EdocMarkReserve reserve = reserveVO.getEdocMarkReserve();
				if(type != null && type.intValue() == 1) {//过滤线上预留文号
					if(reserveVO.getType().intValue() != 1) {
						continue;
					}
				} else if(type != null && type.intValue() == 2) {//过滤线下预留文号
					if(reserveVO.getType().intValue() != 2) {
						continue;
					}
				}
				if(reserve.getStartNo().intValue() != reserve.getEndNo().intValue()) {
					reserveVO.setDocMarkDisplay(reserve.getDocMark() + " "+ResourceUtil.getString("edoc.oper.to")+" " + reserve.getDocMarkEnd());
				} else {
					reserveVO.setDocMarkDisplay(reserve.getDocMark());
				}
				if(queryNumber.intValue()==-1 || (queryNumber.intValue()>=reserveVO.getStartNo().intValue() && queryNumber.intValue()<=reserveVO.getEndNo().intValue())) {
					if(Strings.isEmpty(reserveListMap.get(reserveVO.getMarkDefineId()))) {
						List<EdocMarkReserveVO> reserveList = new ArrayList<EdocMarkReserveVO>();
						reserveList.add(reserveVO);
						reserveListMap.put(reserveVO.getMarkDefineId(), reserveList);
					} else {
						reserveListMap.get(reserveVO.getMarkDefineId()).add(reserveVO);
					}
				}
			}
		}
		return reserveListMap;
	}
	
	private void reloadByAcrossYears() {
		boolean reLoadFlag = false;
		if(Strings.isNotEmpty(markReservedVoMap.values())) {
			Integer yearNo = Calendar.getInstance().get(Calendar.YEAR);
			for(EdocMarkReserveVO reserveVO : markReservedVoMap.values()) {
				EdocMarkReserve reserve = reserveVO.getEdocMarkReserve();
				if(reserve.getYearNo() != yearNo.intValue()) {
					reLoadFlag = true;
					break;
				}
			}
		}
		if(reLoadFlag) {
			initialize();
		}
	}
	
	public void setEdocMarkReserveDao(EdocMarkReserveDao edocMarkReserveDao) {
		this.edocMarkReserveDao = edocMarkReserveDao;
	}

	public void setEdocMarkReserveNumberDao(EdocMarkReserveNumberDao edocMarkReserveNumberDao) {
		this.edocMarkReserveNumberDao = edocMarkReserveNumberDao;
	}
	
}
