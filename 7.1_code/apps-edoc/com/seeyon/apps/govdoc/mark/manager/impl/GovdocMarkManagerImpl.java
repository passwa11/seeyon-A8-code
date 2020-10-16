package com.seeyon.apps.govdoc.mark.manager.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.edoc.bo.TemplateMarkInfo;
import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.edoc.constants.EdocConstant.flowState;
import com.seeyon.apps.govdoc.constant.GovdocMarkEnum;
import com.seeyon.apps.govdoc.constant.GovdocMarkEnum.SelectTypeEnum;
import com.seeyon.apps.govdoc.constant.GovdocMarkEnum.UsedStateEnum;
import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.helper.GovdocOrgHelper;
import com.seeyon.apps.govdoc.manager.GovdocManager;
import com.seeyon.apps.govdoc.mark.dao.GovdocMarkDao;
import com.seeyon.apps.govdoc.mark.helper.GovdocMarkHelper;
import com.seeyon.apps.govdoc.mark.manager.GovdocMarkManager;
import com.seeyon.apps.govdoc.mark.manager.GovdocMarkOpenManager;
import com.seeyon.apps.govdoc.mark.manager.GovdocMarkParseManager;
import com.seeyon.apps.govdoc.mark.vo.GovdocMarkVO;
import com.seeyon.apps.govdoc.po.GovdocMarkRecord;
import com.seeyon.apps.govdoc.util.GovdocParamUtil;
import com.seeyon.apps.govdoc.vo.GovdocBaseVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.v3x.edoc.domain.EdocMark;
import com.seeyon.v3x.edoc.domain.EdocMarkAcl;
import com.seeyon.v3x.edoc.domain.EdocMarkCategory;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocMarkHistory;
import com.seeyon.v3x.edoc.domain.EdocMarkReserveNumber;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocMarkAclManager;
import com.seeyon.v3x.edoc.manager.EdocMarkCategoryManager;
import com.seeyon.v3x.edoc.manager.EdocMarkDefinitionManager;
import com.seeyon.v3x.edoc.manager.EdocMarkHistoryManager;
import com.seeyon.v3x.edoc.manager.EdocMarkManager;
import com.seeyon.v3x.edoc.manager.EdocMarkReserveManager;
import com.seeyon.v3x.edoc.util.EdocMarkUtil.ReserveTypeEnum;
import com.seeyon.v3x.edoc.webmodel.EdocMarkModel;
import com.seeyon.v3x.edoc.webmodel.EdocMarkReserveVO;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;

/**
 * 新公文文号管理类
 * @author 唐桂林
 *
 */
public class GovdocMarkManagerImpl implements GovdocMarkManager {

	private static final Log LOGGER = LogFactory.getLog(GovdocMarkManagerImpl.class);
	
	private GovdocMarkDao govdocMarkDao;
	private GovdocMarkParseManager govdocMarkParseManager;
	private GovdocMarkOpenManager govdocMarkOpenManager;
	private GovdocManager govdocManager;
	
	private EdocMarkCategoryManager edocMarkCategoryManager;
	private EdocMarkDefinitionManager edocMarkDefinitionManager;
	private EdocMarkAclManager edocMarkAclManager;
	private EdocMarkReserveManager edocMarkReserveManager;
	private EdocMarkManager edocMarkManager;
	private EdocMarkHistoryManager edocMarkHistoryManager;
	private TemplateManager templateManager;
	private OrgManager orgManager;
	
	/****************************** 公文文号管理相关方法 start *******************************/
	private Map<String, String> setListCondition(Map<String, String> condition) throws BusinessException {
		if(condition == null) {
			condition = new HashMap<String, String>();
		}
		User user = AppContext.getCurrentUser();
		if(Strings.isBlank(condition.get("userId"))) {
			condition.put("userId", String.valueOf(user.getId()));
		}
		if(Strings.isBlank(condition.get("currentAccountId"))) {
			condition.put("currentAccountId", String.valueOf(user.getLoginAccount()));
		}
		if(Strings.isBlank(condition.get("isAdmin"))) {
			condition.put("isAdmin", String.valueOf(user.isAdmin()));
		}
		return condition;
	}
	@Override
	public FlipInfo findList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
			GovdocHelper.checkDocmarkByYear();
			condition = setListCondition(condition);
			
			if(flipInfo == null) {
				flipInfo = new FlipInfo();
			}
			List<GovdocMarkVO> voList = new ArrayList<GovdocMarkVO>(); 
			List<GovdocMarkVO> resultList = govdocMarkDao.findList(flipInfo, condition);
			
			String yearNo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
			List<Long> markDefIdList = new ArrayList<Long>();
			for(int i = 0; i < resultList.size(); i++) {
				markDefIdList.add(resultList.get(i).getMarkDefId());
			}
			
			Map<Long, List<EdocMarkAcl>> aclMap = new HashMap<Long, List<EdocMarkAcl>>();
			List<EdocMarkAcl> aclList = null;
			if(Strings.isNotEmpty(markDefIdList)) {
				aclList = govdocMarkDao.findEdocMarkAcl(markDefIdList);
				if(Strings.isNotEmpty(aclList)) {
					for(EdocMarkAcl bean : aclList) {
						if(aclMap.get(bean.getMarkDefId()) == null) {
							aclMap.put(bean.getMarkDefId(), new ArrayList<EdocMarkAcl>());
						}
						aclMap.get(bean.getMarkDefId()).add(bean);
					}
				}
			}

			Map<Long, List<EdocMarkReserveVO>> reserveVoMap = edocMarkReserveManager.findAllEdocMarkReserveListMap();
			for(int i = 0; i < resultList.size(); i++) {
				boolean addFlag = false;
				GovdocMarkVO markVo = resultList.get(i);
				Long markDefId = markVo.getMarkDefId();
				
				govdocMarkParseManager.markDef2Mode(markVo, yearNo, null);
				//按公文文号查询
				if(Strings.isNotBlank(condition.get("condition")) && Strings.isNotBlank(condition.get("markstr"))) {//小查询
					if(!StringUtils.contains(markVo.getMarkstr(), condition.get("markstr"))) {
						continue;
					}
				}
				
				boolean isUp = Strings.isNotBlank(condition.get("markReserveUp"));
				boolean isDown = Strings.isNotBlank(condition.get("markReserveDown"));
				if(Strings.isNotBlank(condition.get("condition")) && (isUp || isDown)) {//小查询//按预留文号查询/按线下占用查询
					EdocMarkDefinition markDef = null;
					if(markVo.getMarkDef() == null) {
						markDef = GovdocMarkHelper.convertToMarkDef(markVo);
						markVo.setMarkDef(markDef);
					}
					List<EdocMarkReserveNumber>  reserveNumberList = null;
					if(Strings.isNotBlank(condition.get("markReserveUp"))) {//按预留文号查询
						reserveNumberList = edocMarkReserveManager.findEdocMarkReserveNumberList(markDef, ReserveTypeEnum.reserve_up.getReserveType());
					} else {
						reserveNumberList = edocMarkReserveManager.findEdocMarkReserveNumberList(markDef, ReserveTypeEnum.reserve_down.getReserveType());
					}
					if(Strings.isNotEmpty(reserveNumberList)) {
						for(EdocMarkReserveNumber reserveVO : reserveNumberList) {
							if(isUp) {
								if(StringUtils.contains(reserveVO.getDocMark(), condition.get("markReserveUp"))) {
									addFlag = true;
									break;
								}
							} else if(isDown) {
								if(StringUtils.contains(reserveVO.getDocMark(), condition.get("markReserveDown"))) {
									addFlag = true;
									break;
								}
							}
						}	
					}
				} else {
					addFlag = true;
				}
				
				if(!addFlag) {
					continue;
				}
				
				String[] upAndDown = GovdocMarkHelper.getMarkReserveUpAndDown(resultList.get(i), reserveVoMap.get(markDefId));
				markVo.setMarkReserveUp(upAndDown[0].replaceAll("null", ""));
				markVo.setMarkReserveDown(upAndDown[1].replaceAll("null", ""));
				
				if(Strings.isNotEmpty(aclMap.get(markDefId))) {
					markVo.setAclEntityName(GovdocOrgHelper.getMarkAclEntityName(aclMap.get(markDefId), markDefId));
				}
				voList.add(markVo);
			}
			
			flipInfo.setData(voList);
			return flipInfo;
	}
	@Override
	public FlipInfo findUsedList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		condition = setListCondition(condition);
		
		if(flipInfo == null) {
			flipInfo = new FlipInfo();
		}
		List<GovdocMarkVO> voList = new ArrayList<GovdocMarkVO>();
		List<EdocMarkHistory> historyList = govdocMarkDao.findUsedList(flipInfo, condition);
		if(Strings.isNotEmpty(historyList)) {
			if(Strings.isNotEmpty(historyList)) {
				GovdocMarkVO markVo = null;
				for(EdocMarkHistory bean : historyList) {
					markVo = new GovdocMarkVO();
					markVo.setCallId(bean.getId());
					markVo.setSelectType(bean.getSelectType());
					markVo.setMarkType(bean.getMarkType());
					markVo.setMarkstr(bean.getDocMark());
					if(bean.getRealUsed()!=null && bean.getRealUsed().intValue()==1) {//大流水时，真正使用的才显示标题
						markVo.setSubject(bean.getSubject());
					}
					markVo.setDescription(bean.getDescription());
					if(bean.getMarkDefId() == null) {
						markVo.setSelectTypeName("手工输入");
					} else if(bean.getSelectType() == 1) {
						markVo.setSelectTypeName("手工输入");
					} else if(bean.getSelectType() == 4) {
						markVo.setSelectTypeName("线下占用");
					} else {//0自动文号 2断号 3预留文号都显示为系统占号
						markVo.setSelectTypeName("系统占用");
					}
					markVo.setCreateTime(bean.getCreateTime());
					voList.add(markVo);
				}
			}
		}
		flipInfo.setData(voList);
		return flipInfo;
	}
	/****************************** 公文文号管理相关方法   end *******************************/
	
	
	/****************************** 公文文号定义相关方法 start *******************************/
	@Override
	public EdocMarkDefinition getMarkDef(long id) {
		return edocMarkDefinitionManager.getMarkDefinition(id);
	}
	@SuppressWarnings("unchecked")
	@Override
	public EdocMarkDefinition getMarkDef(String wordNo, String markType) {
		if("doc_mark".equals(markType)) {
			markType = "0";
		} else if("serial_no".equals(markType)) {
			markType = "1";
		} else if("sign_mark".equals(markType) || "doc_mark2".equals(markType)) {
			markType = "2";
		}
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("wordNo", wordNo);
		paramMap.put("markType", Integer.parseInt(markType));
		List<EdocMarkDefinition> result = DBAgent.find(" from EdocMarkDefinition where wordNo = :wordNo and markType = :markType order by status", paramMap);
		if(Strings.isNotEmpty(result)) {
			EdocMarkDefinition markDef = result.get(0);
			if(markDef != null && markDef.getCategoryId() != null) {
				markDef.setEdocMarkCategory(this.findById(markDef.getCategoryId()));
			}
			return markDef;
		}
		return null;
	}
	@Override
	public void saveMarkDef(EdocMarkDefinition po) {
		edocMarkDefinitionManager.saveMarkDefinition(po);
	}
	@Override
	public void updateMarkDef(EdocMarkDefinition po) {
		edocMarkDefinitionManager.updateMarkDefinition(po);
	}
	@Override
	public void deleteMarkDef(EdocMarkDefinition po) {
		edocMarkDefinitionManager.deleteMarkDefinition(po);
	}
	@Override
	public void logicalDeleteMarkDef(long defId, short status) {
		edocMarkDefinitionManager.logicalDeleteMarkDefinition(defId, status);
	}
	@Override
	public Boolean containMarkDef(String wordNo, long domainId, int markType) {
		return edocMarkDefinitionManager.containEdocMarkDefinition(wordNo, domainId, markType);
	}
	@Override
	public Boolean containMarkDef(long markDefId, String wordNo, long domainId, int markType) {
		return edocMarkDefinitionManager.containEdocMarkDefinition(markDefId, wordNo, domainId, markType);
	}
	@Override
	public boolean containMarkDefInCategory(long categoryId) {
		return edocMarkDefinitionManager.containEdocMarkDefInCategory(categoryId);
	}
	@Override
	public List<EdocMarkDefinition> getMarkDefsByCategory(Long categoryId) {
		return edocMarkDefinitionManager.getEdocMarkDefinitionsByCategory(categoryId);
	}
	@Override
	public List<GovdocMarkVO> getMarkVoListByMarkDefId(List<Long> markDefIdList) throws BusinessException {
		if(Strings.isNotEmpty(markDefIdList)) {
			List<GovdocMarkVO> markVoList = new ArrayList<GovdocMarkVO>();
			List<EdocMarkDefinition> list = govdocMarkDao.getMarkDefListById(markDefIdList);
			if(Strings.isNotEmpty(list)) {
				for(EdocMarkDefinition bean : list) {	
					GovdocMarkVO markVo = govdocMarkParseManager.markDef2Mode(bean, null, null);
					markVoList.add(markVo);
				}
			}
			return markVoList;
		}
		return null;
	}
	@Override
	public String checkExistMarkDef(String id) throws BusinessException {
		return edocMarkDefinitionManager.checkExistEdocMarkDefinition(id);
	}
	/****************************** 公文文号定义相关方法   end *******************************/
	
	
	/****************************** 公文文号组装相关方法 start *******************************/
	@Override
	public List<EdocMarkModel> getEdocMarkDefs(Long domainId, Long depId, String condition, String textfield)
			throws BusinessException {
		return edocMarkDefinitionManager.getEdocMarkDefs(domainId, depId, condition, textfield);
	}
	@Override
	public GovdocMarkVO markDef2Mode(EdocMarkDefinition markDef, String yearNo, Integer currentNo) {
		return govdocMarkParseManager.markDef2Mode(markDef, yearNo, currentNo);
	}
	/****************************** 公文文号组装相关方法   end *******************************/
	
	
	/****************************** 公文文号流水相关方法 start *******************************/
	@Override
	public List<EdocMarkCategory> findByPage(Short type, Long domainId) {
		return edocMarkCategoryManager.findByPage(type, domainId);
	}
	@Override
	public List<EdocMarkCategory> getMarkCategories(Long domainId) {
		return edocMarkCategoryManager.getEdocMarkCategories(domainId);
	}
	@Override
	public Map<Long, Integer> getMarkCategories(List<Long> categoryIdList) {
		return edocMarkCategoryManager.findByIds(categoryIdList);
	}
	@Override
	public List<EdocMarkCategory> findByTypeAndDomainId(Short type, Long domainId) {
		return edocMarkCategoryManager.findByTypeAndDomainId(type, domainId);
	}
	@Override
	public EdocMarkCategory findById(Long id) {
		return edocMarkCategoryManager.findById(id);
	}
	@Override
	public void saveCategory(EdocMarkCategory po) {
		edocMarkCategoryManager.saveCategory(po);
	}
	@Override
	public void updateCategory(EdocMarkCategory po) {
		edocMarkCategoryManager.updateCategory(po);
	}
	@Override
	public void deleteCategory(long categoryId) {
		edocMarkCategoryManager.deleteCategory(categoryId);
	}
	@Override
	public Boolean containEdocMarkCategory(String name, long domainId) {
		return edocMarkCategoryManager.containEdocMarkCategory(name, domainId);
	}
	@Override
	public Boolean containEdocMarkCategory(long categoryId, String name, long domainId) {
		return edocMarkCategoryManager.containEdocMarkCategory(categoryId, name, domainId);
	}
	/****************************** 公文文号流水相关方法   end *******************************/
	
	
	/****************************** 公文文号授权相关方法 start *******************************/
	@Override
	public List<EdocMarkAcl> getMarkAclById(Long edocMarkDefinitionId) throws BusinessException {
		return edocMarkAclManager.getMarkAclById(edocMarkDefinitionId);
	}
	@Override
	public void deleteByDefId(Long defId) {
		edocMarkAclManager.deleteByDefId(defId);
	}
	/****************************** 公文文号授权相关方法   end *******************************/
	

	/****************************** 公文预留文号相关方法  start *******************************/
	@Override
	public void saveMarkReserve(User user, Integer type, EdocMarkDefinition markDef, List<EdocMarkReserveVO> addReserveList,
			List<Long> delReservedIdList, List<String> delReservedNoList) throws BusinessException {
		List<EdocMarkReserveNumber> addReserveNumberList = null;
		//新增预留文号
		if(Strings.isNotEmpty(addReserveList)) {
			//新增预留数据
			addReserveNumberList = edocMarkReserveManager.saveMarkReserverNew(user, type, markDef, addReserveList, delReservedIdList, delReservedNoList);
			//新增预留文号时，更新当前文号
			int currentNo = edocMarkReserveManager.getCurrentNoForReserveNew(markDef);
			if(currentNo > markDef.getEdocMarkCategory().getCurrentNo()) {
				govdocMarkDao.updateMarkCurrentNo(markDef.getCategoryId(), currentNo);
			}			
		}
		List<EdocMarkReserveVO> delReserveVoList = null;
		//删除预留文号
		if(Strings.isNotEmpty(delReservedIdList)) {
			//删除预留数据及缓存，返回要删除的文号预留vo
			delReserveVoList = edocMarkReserveManager.deleteMarkReserveNew(user, type, markDef, delReservedIdList, delReservedNoList);
		}
		
		if(type == 2) {//线下占用时，生成占号/删除线下占用时，取消占号
			this.saveEdocMarkHistoryForReserver(user, 4, markDef, addReserveList, delReserveVoList);
		}
		
		//生成断号
		Integer selectType = type.intValue() == 1 ? 3 : 4;//1预留文号->3   2线下占用->4
		saveEdocMarkForReserver(user, selectType, markDef, addReserveNumberList, delReserveVoList);
	}
	
	@Override
	public void saveMarkReserveForCategory(User user, EdocMarkDefinition markDef) throws BusinessException {
		Long theMarkDefId = null;
		EdocMarkDefinition theMarkDef = null;
		List<EdocMarkDefinition> mds = this.getMarkDefsByCategory(markDef.getCategoryId());
		for(EdocMarkDefinition bean : mds) {
			if(bean.getId().longValue() != markDef.getId()) {
				theMarkDefId = bean.getId();
				theMarkDef = bean;
				break;
			}
		}
		if(theMarkDefId != null) {
			List<EdocMarkReserveVO> addReserveList = new ArrayList<EdocMarkReserveVO>();
			List<EdocMarkReserveVO> addReserve2List = new ArrayList<EdocMarkReserveVO>();
			List<EdocMarkReserveVO> reserveVoList = edocMarkReserveManager.findEdocMarkReserveList(theMarkDef);
			if(Strings.isNotEmpty(reserveVoList)) {
				for(EdocMarkReserveVO reserveVo : reserveVoList) {
					EdocMarkReserveVO newReserveVo = new EdocMarkReserveVO();
					newReserveVo.setType(reserveVo.getType());
					newReserveVo.setEdocMarkDefinition(markDef);
					newReserveVo.setEdocMarkCategory(reserveVo.getEdocMarkCategory());
					newReserveVo.setMarkDefineId(markDef.getId());
					newReserveVo.setYearNo(reserveVo.getYearNo());
					newReserveVo.setStartNo(reserveVo.getStartNo());
					newReserveVo.setEndNo(reserveVo.getEndNo());
					newReserveVo.setDomainId(user.getLoginAccount());
					newReserveVo.setCreateUserId(user.getId());
					newReserveVo.setCreateTime(new Timestamp(System.currentTimeMillis()));
					addReserveList.add(newReserveVo);
					if(reserveVo.getType().intValue() == 2) {
						addReserve2List.add(newReserveVo);
					}
				}
			}
			edocMarkReserveManager.saveMarkReserverNew(user, 2, markDef, addReserveList, null, null);
			
			if(Strings.isNotEmpty(addReserve2List)) {
				this.saveEdocMarkHistoryForReserver(user, 4, markDef, addReserve2List, null);
			}
		}
	}
	@Override
	public void deleteReserveByMarkDefId(Long markDefId) throws BusinessException {
		edocMarkReserveManager.deleteByMarkDefineId(markDefId);
	}
	@Override
	public void deleteMarkHistoryByReserve(Integer selectType, Long markDefId) throws BusinessException {
		govdocMarkDao.deleteMarkHistoryByReserve(selectType, markDefId);
	}
	@Override
	public List<GovdocMarkVO> findReserveListByMarkDefId(Integer type, GovdocMarkVO markDefVo) throws BusinessException {
		List<GovdocMarkVO> reserveVoList = new ArrayList<GovdocMarkVO>();
		List<EdocMarkReserveNumber> reserveNumberList = govdocMarkDao.findAllNotUsed(type, markDefVo.getMarkDefId(), markDefVo.getYearEnabled(), markDefVo.isTwoYear());
		if(Strings.isNotEmpty(reserveNumberList)) {
			//Collections.sort(reserveNumberList, new EdocMarkReserveNumber());
			try {
				for(EdocMarkReserveNumber reserveNumber : reserveNumberList) {
					GovdocMarkVO reserveVo = GovdocMarkHelper.convertToMarkVo(markDefVo);
					reserveVo.setMarkDef(markDefVo.getMarkDef());
					reserveVo.setMarkNumber(reserveNumber.getMarkNo());
					govdocMarkParseManager.markDef2Mode(reserveVo, String.valueOf(reserveNumber.getYearNo()), reserveNumber.getMarkNo());
					
					reserveVo.setMarkstr(reserveNumber.getDocMark());
					reserveVo.setYearNo(String.valueOf(reserveNumber.getYearNo()));
					
					GovdocMarkVO newMarkVo = GovdocMarkHelper.parseDocMark(reserveVo, reserveNumber.getDocMark());
					if(newMarkVo != null) {
						reserveVo.setYearNo(newMarkVo.getYearNo());
						//reserveVo.setMarkNumber(newMarkVo.getMarkNumber());
					}
					reserveVoList.add(reserveVo);
				}
			} catch(Exception e) {
				LOGGER.error("获取预留文号出错", e);
				throw new BusinessException(e);
			}
		}
		return reserveVoList;
	}
	public void updateMarkReserveIsUsed(List<Long> markDefIdList, List<String> markstrList, boolean isUsed) throws BusinessException {
		edocMarkReserveManager.updateMarkReserveIsUsedNew(markDefIdList, markstrList, isUsed);
	}
	
	@Override
	public List<EdocMarkReserveVO> findMarkReserveVoList(Integer type, EdocMarkDefinition markDef, Integer queryNumber) throws BusinessException {
		return edocMarkReserveManager.findEdocMarkReserveList(markDef, queryNumber);
	}
	@Override
	public EdocMarkReserveVO getMarkReserveByFormat(EdocMarkDefinition markDef, Integer markNumber) {
		return edocMarkReserveManager.getMarkReserveByFormat(markDef, markNumber);
	}
	@Override
	public boolean checkRepeatMarkReserved(Long markDefineId, int startNo, int endNo) throws BusinessException {
		return edocMarkReserveManager.checkRepeatMarkReserved(markDefineId, startNo, endNo);
	}
	@Override
	public boolean checkRepeatMarkReserved(Long markDefineId, int startNo, int endNo, List<Long> thisReservedIdList,
			List<Long> delReservedIdList) throws BusinessException {
		return edocMarkReserveManager.checkRepeatMarkReserved(markDefineId, startNo, endNo);
	}
	@Override
	public boolean checkRepeatMarkReserved(EdocMarkDefinition markDef, int startNo, int endNo, List<Long> thisReservedIdList, List<Long> delReservedIdList, List<String> delReservedNoList) throws BusinessException {
		return edocMarkReserveManager.checkRepeatMarkReserved(markDef, startNo, endNo);
	}
	@Override
	public void reloadCache() {
		edocMarkReserveManager.reloadCache();
	}
	/****************************** 公文预留文号相关方法    end *******************************/
	
	
	/****************************** 公文文号前端展现方法 start *******************************/
	@Override
	public List<GovdocMarkVO> getListByUserId(Map<String, Object> params) throws BusinessException {
		String markType = GovdocParamUtil.getString(params, "markType");
		Long templateMarkDefId = GovdocParamUtil.getLong(params, "templateMarkDefId");
		String yearNo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
		GovdocHelper.checkDocmarkByYear();
		
		Map<String, String> condition = setListCondition(null);
		condition.put("markType", markType);
		if(templateMarkDefId != null) {
			condition.put("templateMarkDefId", String.valueOf(templateMarkDefId));
		}
		List<GovdocMarkVO> voList = govdocMarkDao.getListByUserId(condition);
		for(int i = 0; i < voList.size(); i++){
			govdocMarkParseManager.markDef2Mode(voList.get(i), yearNo, null);
		}
		
		return voList;
	}
	
	@Override
	public Map<Integer, List<TemplateMarkInfo>> getFormBindMarkList(String markType, Long domainId)  throws BusinessException {
		Map<Integer, List<TemplateMarkInfo>> markMap = new HashMap<Integer, List<TemplateMarkInfo>>();
		GovdocHelper.checkDocmarkByYear();
		Map<String, String> condition = new HashMap<String, String>();
		condition.put("markType", markType);
		condition.put("currentAccountId", String.valueOf(domainId));
		condition.put("isAdmin", "true");
		condition = setListCondition(condition);
		List<GovdocMarkVO> voList = govdocMarkDao.getListByUserId(condition);
		if(Strings.isNotEmpty(voList)) {
			for(GovdocMarkVO markVo : voList) {
				TemplateMarkInfo markInfo = new TemplateMarkInfo();
				markInfo.setMarkType(markVo.getMarkType());
				markInfo.setMarkDefId(markVo.getMarkDefId());
				markInfo.setWordNo(markVo.getWordNo());
				if(markMap.get(markVo.getMarkType()) == null) {
					markMap.put(markVo.getMarkType(), new ArrayList<TemplateMarkInfo>());
				}
				markMap.get(markVo.getMarkType()).add(markInfo);
			}
		}
		return markMap;
	}
	
	@SuppressWarnings("unused")
	@Override
	public Map<String, Object> getFormMarkDisplayValue(Map<String, Object> params) throws BusinessException {
		Long formId = (Long)params.get("formId");
		Long summaryId = GovdocParamUtil.getLong(params, "summaryId");
		
		Integer formType = (Integer)params.get("formType");
		Long formDataId = (Long)params.get("formDataId");
		String fieldType = (String)params.get("fieldType");
		String fieldName = (String)params.get("fieldName");
		String fieldValue = (String)params.get("fieldValue");
		Object[] valuesWithDisplay = (Object[])params.get("valuesWithDisplay");
		
		//调用模板
		String templateId = AppContext.getRawRequest().getParameter("templateId") ==null ?  null : String.valueOf(AppContext.getRawRequest().getParameter("templateId"));
		//templateId为空也有可能是修改公文
		if (Strings.isBlank(templateId)) {
			Object templateIdObj = AppContext.getRawRequest().getAttribute("templateIdOfDoc");
			templateId = templateIdObj ==null ? null :templateIdObj.toString();
		}
		
		CtpTemplate template = null;
		if (Strings.isNotBlank(templateId) && !("-1").equals(templateId) && !("0").equals(templateId)) {
			template = templateManager.getCtpTemplate(Long.valueOf(templateId));
		}
		boolean isFromTemplate = false;
		if(AppContext.getRawRequest().getAttribute("isFromTemplate") != null) {
			isFromTemplate = (Boolean)AppContext.getRawRequest().getAttribute("isFromTemplate");
		}
		
		List<Long> tMarkDefIdList_doc_mark = new ArrayList<Long>();
		List<Long> tMarkDefIdList_serial_no = new ArrayList<Long>();
		List<Long> tMarkDefIdList_sign_mark = new ArrayList<Long>();
		if(template != null && template.isSystem() && Strings.isNotBlank(template.getBindMarkInfo())) {
			@SuppressWarnings("unchecked")
			List<TemplateMarkInfo> markInfoList = (List<TemplateMarkInfo>)XMLCoder.decoder(template.getBindMarkInfo());
			if(Strings.isNotEmpty(markInfoList)) {
				for(TemplateMarkInfo markObj : markInfoList) {
					if(markObj.getMarkType().intValue() == 0) {
						tMarkDefIdList_doc_mark.add(markObj.getMarkDefId());
					} else if(markObj.getMarkType().intValue() == 1) {
						tMarkDefIdList_serial_no.add(markObj.getMarkDefId());
					} else {
						tMarkDefIdList_sign_mark.add(markObj.getMarkDefId());
					}
				}
			}
		}
		
		
		String yearNo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
		
		String notSelectKey = "请选择机构代字";
		String tip = "<option value=''>"+notSelectKey+"</option>";
		StringBuilder options = new StringBuilder();
		options.append(tip);
		
		Map<String, Object> condition = new HashMap<String, Object>();
		condition.put("markType", fieldType);
		List<GovdocMarkVO> voList = getListByUserId(condition);
		
		List<GovdocMarkVO> tmvoList = null;
		boolean hasDocMarkRole = false;
		boolean hasSerialNoRole = false;
		boolean hasSignMarkRole = false;
		//公文文号
		if("edocDocMark".equals(fieldType)) {
			for (GovdocMarkVO markVo : voList)  {
				//模板绑定了文号
				if(Strings.isNotEmpty(tMarkDefIdList_doc_mark)) {
					if(tMarkDefIdList_doc_mark.contains(markVo.getMarkDefId())) {
						options.append(GovdocMarkHelper.getFormMarkOption(markVo, yearNo));
						hasDocMarkRole = true;
						break;
					}
				} else {
					options.append(GovdocMarkHelper.getFormMarkOption(markVo, yearNo));
				}
			}
			if(Strings.isNotEmpty(tMarkDefIdList_doc_mark) && !hasDocMarkRole) {
				tmvoList = this.getMarkVoListByMarkDefId(tMarkDefIdList_doc_mark);
			}
		}
		//内部文号
		else if("edocInnerMark".equals(fieldType)) {
			for (GovdocMarkVO markVo : voList)  {
				//模板绑定了文号
				if(Strings.isNotEmpty(tMarkDefIdList_serial_no)) {
					if(tMarkDefIdList_serial_no.contains(markVo.getMarkDefId())) {
						options.append(GovdocMarkHelper.getFormMarkOption(markVo, yearNo));
						hasSerialNoRole = true;
						break;
					}
				} else {
					options.append(GovdocMarkHelper.getFormMarkOption(markVo, yearNo));
				}
			}
			if(Strings.isNotEmpty(tMarkDefIdList_serial_no) && !hasSerialNoRole) {
				tmvoList = this.getMarkVoListByMarkDefId(tMarkDefIdList_serial_no);
			}
		}
		//签收编号
		else if("edocSignMark".equals(fieldType)) {
			for (GovdocMarkVO markVo : voList)  {
				//模板绑定了文号
				if(Strings.isNotEmpty(tMarkDefIdList_sign_mark)) {
					if(tMarkDefIdList_sign_mark.contains(markVo.getMarkDefId())) {
						options.append(GovdocMarkHelper.getFormMarkOption(markVo, yearNo));
						hasSignMarkRole = true;
						break;
					}
				} else {
					options.append(GovdocMarkHelper.getFormMarkOption(markVo, yearNo));
				}
			}
			if(Strings.isNotEmpty(tMarkDefIdList_sign_mark) && !hasSignMarkRole) {
				tmvoList = this.getMarkVoListByMarkDefId(tMarkDefIdList_sign_mark);
			}
		}
		
		if(Strings.isNotEmpty(tmvoList)) {
			for (GovdocMarkVO markVo : tmvoList)  {
				options.append(GovdocMarkHelper.getFormMarkOption(markVo, yearNo));
			}
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("value4Display", options.toString());
		return map;
	}
	
	public List<Map<String, Object>> getFormMarkList(Map<String, Object> params) throws BusinessException {
		List<Map<String, Object>> markList = new ArrayList<Map<String, Object>>();
		
		String markType = (String)params.get("markType");
		
		Map<String, Object> map = null;
		//if(params.get("from")!=null && !"pc".equals(params.get("from"))) {
			String markTypestr = "";
			if("0".equals(markType)) {
				markTypestr = "公文文号";
			} else if("1".equals(markType)) {
				markTypestr = "内部文号";
			} else if("2".equals(markType)) {
				markTypestr = "签收编号";
			}
			map = new HashMap<String, Object>();
			map.put("value", "");
			map.put("text", "请选择"+markTypestr);
			markList.add(map);
		//}
		
		Map<String, Object> condition = new HashMap<String, Object>();
		condition.put("markType", markType);
		List<GovdocMarkVO> voList = getListByUserId(condition);
		
		if(Strings.isNotEmpty(voList)) {
			Long summaryId = (Long)params.get("summaryId");
			GovdocMarkRecord record = govdocMarkDao.getMarkRecord(summaryId, Integer.parseInt(markType));
			if(record != null) {
				String docMark = record.getSelectType() + "|" + (record.getMarkDefId()==null ? "" : record.getMarkDefId()) + "|" + record.getMarkstr() + "|" + (record.getMarkNumber()==null ? "" : record.getMarkNumber());
				for(GovdocMarkVO markVo : voList) {
					if(markVo.getMarkstr().equals(record.getMarkstr())) {
						docMark = "";
						break;
					}
				}
				if(Strings.isNotBlank(docMark)) {
					map = new HashMap<String, Object>();
					map.put("value", docMark);
					map.put("text", record.getMarkstr());
					markList.add(map);
				}
			}
			
			for(GovdocMarkVO markVo : voList) {
				map = new HashMap<String, Object>();
				map.put("value", SelectTypeEnum.zidong.ordinal() + "|" + markVo.getMarkDefId() + "|" + markVo.getMarkstr() + "|" + markVo.getMarkNumber());
				map.put("text", markVo.getMarkstr());
				markList.add(map);
			}
		}		
		return markList;
	}
	
	/**
	 * 触发公文流程/子流程时，填充公文文号
	 */
	public void fillMarkParamByAuto(GovdocNewVO newVo, EdocSummary parentSummary) throws BusinessException {
		if(Strings.isNotBlank(parentSummary.getDocMark()) 
				|| Strings.isNotBlank(parentSummary.getSerialNo())
				|| Strings.isNotBlank(parentSummary.getDocMark2())) {
			
			Map<Integer, GovdocMarkVO> markVoMap = this.getVoBySummaryId(parentSummary.getId());
			if(Strings.isNotBlank(parentSummary.getDocMark())) {
				GovdocMarkVO parentMarkVo = markVoMap.get(0);
				if(parentMarkVo != null) {
					GovdocMarkVO markVo = new GovdocMarkVO();
					markVo.setWordNo(parentMarkVo.getWordNo());
					markVo.setMarkstr(parentMarkVo.getMarkstr());
					markVo.setMarkDefId(parentMarkVo.getMarkDefId());
					markVo.setCategoryId(parentMarkVo.getCategoryId());
					markVo.setMarkNumber(parentMarkVo.getMarkNumber());
					markVo.setMarkType(parentMarkVo.getMarkType());
					markVo.setSelectType(parentMarkVo.getSelectType());
					markVo.setNewflowType(newVo.getNewflowType());
					markVo.setParentSummaryId(parentSummary.getId());
					if(newVo.getNewflowType().intValue() == EdocConstant.NewflowType.child.ordinal()) {
						markVo.setFrom("child");	
					} else if(newVo.getNewflowType().intValue() == EdocConstant.NewflowType.auto.ordinal()) {
						markVo.setFrom("auto");
					}
					markVo.setCurrentUser(newVo.getCurrentUser());
					newVo.setDocMarkVo(markVo);
				}
			}
			if(Strings.isNotBlank(parentSummary.getSerialNo())) {
				GovdocMarkVO parentMarkVo = markVoMap.get(1);
				if(parentMarkVo != null) {
					GovdocMarkVO markVo = new GovdocMarkVO();
					markVo.setWordNo(parentMarkVo.getWordNo());
					markVo.setMarkstr(parentMarkVo.getMarkstr());
					markVo.setMarkDefId(parentMarkVo.getMarkDefId());
					markVo.setCategoryId(parentMarkVo.getCategoryId());
					markVo.setMarkNumber(parentMarkVo.getMarkNumber());
					markVo.setMarkType(parentMarkVo.getMarkType());
					markVo.setSelectType(parentMarkVo.getSelectType());
					markVo.setNewflowType(newVo.getNewflowType());
					markVo.setParentSummaryId(parentSummary.getId());
					if(newVo.getNewflowType().intValue() == EdocConstant.NewflowType.child.ordinal()) {
						markVo.setFrom("child");	
					} else if(newVo.getNewflowType().intValue() == EdocConstant.NewflowType.auto.ordinal()) {
						markVo.setFrom("auto");
					}
					markVo.setCurrentUser(newVo.getCurrentUser());
					newVo.setSerialNoVo(markVo);
				}
			}
			if(Strings.isNotBlank(parentSummary.getDocMark2())) {
				GovdocMarkVO parentMarkVo = markVoMap.get(2);
				if(parentMarkVo != null) {
					GovdocMarkVO markVo = new GovdocMarkVO();
					markVo.setWordNo(parentMarkVo.getWordNo());
					markVo.setMarkstr(parentMarkVo.getMarkstr());
					markVo.setMarkDefId(parentMarkVo.getMarkDefId());
					markVo.setCategoryId(parentMarkVo.getCategoryId());
					markVo.setMarkNumber(parentMarkVo.getMarkNumber());
					markVo.setMarkType(parentMarkVo.getMarkType());
					markVo.setSelectType(parentMarkVo.getSelectType());
					markVo.setNewflowType(newVo.getNewflowType());
					markVo.setParentSummaryId(parentSummary.getId());
					if(newVo.getNewflowType().intValue() == EdocConstant.SendType.child.ordinal()) {
						markVo.setFrom("child");	
					} else if(newVo.getNewflowType().intValue() == EdocConstant.SendType.auto.ordinal()) {
						markVo.setFrom("auto");
					}
					markVo.setCurrentUser(newVo.getCurrentUser());
					newVo.setSignMarkVo(markVo);
				}
			}
		}
	}
	
	
	@Override
	public List<GovdocMarkVO> findCallVoListByMarkDefId(GovdocMarkVO markDefVo) throws BusinessException {
		List<EdocMark> markList = govdocMarkDao.findMarkNotUsed(markDefVo.getMarkDefId(), markDefVo.getMinNo(), markDefVo.getYearEnabled(), markDefVo.isTwoYear());
		if(Strings.isNotEmpty(markList)) {
			//Collections.sort(markList, new EdocMark());
		}
		return  GovdocMarkHelper.convertToMarkVo(markList);
	}
	@Override
	public GovdocMarkVO getVoByMarkDefId(Long markDefId) throws BusinessException {
		EdocMarkDefinition markDef = this.getMarkDef(markDefId);
		if(markDef != null) {
			GovdocMarkVO markVo = GovdocMarkHelper.convertToMarkVo(markDef);
			govdocMarkParseManager.markDef2Mode(markVo, null, null);
			return markVo;
		}
		return null;
	}
	@Override
	public GovdocMarkVO getVoBySummaryId(Long summaryId, Integer markType) throws BusinessException {
		GovdocMarkRecord record = govdocMarkDao.getMarkRecord(summaryId, markType);
		if(record != null) {
			GovdocMarkVO markVo = GovdocMarkHelper.convertToMarkVo(record);
			return markVo;
		}
		return null;
	}
	@Override
	public Map<Integer, GovdocMarkVO> getVoBySummaryId(Long summaryId) throws BusinessException {
		Map<Integer, GovdocMarkVO> markMap = new HashMap<Integer, GovdocMarkVO>();
		List<GovdocMarkRecord> recordList = govdocMarkDao.getMarkRecord(summaryId);
		if(Strings.isNotEmpty(recordList)) {
			for(GovdocMarkRecord record : recordList) {
				GovdocMarkVO markVo = GovdocMarkHelper.convertToMarkVo(record);
				markMap.put(record.getMarkType(), markVo);
			}
		}
		return markMap;
	}
	/****************************** 公文文号前端展现方法   end *******************************/
	
	
	/****************************** 公文文号断号/占号/跳号保存方法 start *******************************/
	@Override
	public boolean saveUnbindMark(Long summaryId) throws BusinessException {
		govdocMarkDao.unbindMark(summaryId);
		return true;
	}
	
	@Override
	public boolean saveCancelMark(GovdocBaseVO newVo) throws BusinessException {
		Long domainId = newVo.getCurrentUser()==null ? newVo.getSummary().getOrgAccountId() : newVo.getCurrentUser().getLoginAccount();
		Long summaryId = newVo.getSummaryId();
		List<EdocMark> newMarkList = new ArrayList<EdocMark>();
		List<EdocMarkHistory> oldhistoryList = null;
		
		boolean isNew = true;		
		List<GovdocMarkRecord> recordList = govdocMarkDao.getMarkRecord(summaryId);
		if(Strings.isNotEmpty(recordList)) {
			isNew = false;
			oldhistoryList = edocMarkHistoryManager.getMarkHistorysByEdocID(summaryId);
			for(GovdocMarkRecord bean : recordList) {
				bean.setUsedState(UsedStateEnum.unused.ordinal());
				bean.setFlowState(flowState.cancel.ordinal());
			}
		}
		if(Strings.isNotEmpty(oldhistoryList)) {//若当前公文之前使用的文号已经占用过了
			//若当前公文之前使用的文号已经占用过了，文号做过修改，则要将原占用的文号进入断号
			for(EdocMarkHistory bean : oldhistoryList) {
				if(bean.getSelectType().intValue() != 1) {//非手工输入才生成断号
					if(bean.getDocMarkNo() != null) {
						EdocMark newMark = GovdocMarkHelper.convertToMark(bean);
						newMark.setEdocId(-1L);
						if(newMark.getDomainId() == null) {
							newMark.setDomainId(domainId);
						}
						newMarkList.add(newMark);
					}
				}
			}
		}
		//将当前公文之前引用的断号清除
		if(!isNew) {
			//删除手工文号断号
			govdocMarkDao.deleteMarkHandinput(newVo.getSummaryId());
			//释放掉断号
			govdocMarkDao.unbindMark(newVo.getSummaryId());
			//删除原有占号
			edocMarkHistoryManager.deleteMarkHistoryByEdocId(summaryId);
			//由占号变成断号
			if(Strings.isNotEmpty(newMarkList)) {
				edocMarkManager.save(newMarkList);
			}
			if(Strings.isNotEmpty(recordList)) {
				govdocMarkDao.updateMarkRecord(recordList);
			}
		}
		return false;
	}
	@Override
	public boolean saveDraftMark(GovdocBaseVO baseVo) throws BusinessException {
		List<EdocMark> markList = new ArrayList<EdocMark>();
		List<EdocMarkHistory> historyList = new ArrayList<EdocMarkHistory>();
		//发送时保存公文文号
		GovdocMarkVO docMarkVo = baseVo.getDocMarkVo();
		if(docMarkVo != null) {
			docMarkVo.setAction("draft");
			this.saveMark(docMarkVo);
			if(Strings.isNotEmpty(docMarkVo.getMarkList())) {
				markList.addAll(docMarkVo.getMarkList());
			}
			if(Strings.isNotEmpty(docMarkVo.getHistoryList())) {
				historyList.addAll(docMarkVo.getHistoryList());
			}
		}
		//发送时保存内部文号
		GovdocMarkVO serialNoVo = baseVo.getSerialNoVo();
		if(serialNoVo != null) {
			serialNoVo.setAction("draft");
			this.saveMark(serialNoVo);
			if(Strings.isNotEmpty(serialNoVo.getMarkList())) {
				markList.addAll(serialNoVo.getMarkList());
			}
			if(Strings.isNotEmpty(serialNoVo.getHistoryList())) {
				historyList.addAll(serialNoVo.getHistoryList());
			}
		}
		//发送时保存签收文号
		GovdocMarkVO signMarkVo = baseVo.getSignMarkVo();
		if(signMarkVo != null) {
			signMarkVo.setAction("draft");
			this.saveMark(signMarkVo);
			if(Strings.isNotEmpty(signMarkVo.getMarkList())) {
				markList.addAll(signMarkVo.getMarkList());
			}
			if(Strings.isNotEmpty(signMarkVo.getHistoryList())) {
				historyList.addAll(signMarkVo.getHistoryList());
			}
		}
		if(Strings.isNotEmpty(markList)) {
			edocMarkManager.save(markList);
		}
		if(Strings.isNotEmpty(historyList)) {
			edocMarkHistoryManager.save(historyList);
		}
		return true;
	}
	
	@Override
	public boolean saveSendMark(GovdocBaseVO baseVo) throws BusinessException {
		List<EdocMark> markList = new ArrayList<EdocMark>();
		List<EdocMarkHistory> historyList = new ArrayList<EdocMarkHistory>();
		//发送时保存公文文号
		GovdocMarkVO docMarkVo = baseVo.getDocMarkVo();
		if(docMarkVo != null) {
			docMarkVo.setAction("send");
			this.saveMark(docMarkVo);
			if(Strings.isNotEmpty(docMarkVo.getMarkList())) {
				markList.addAll(docMarkVo.getMarkList());
			}
			if(Strings.isNotEmpty(docMarkVo.getHistoryList())) {
				historyList.addAll(docMarkVo.getHistoryList());
			}
		}
		//发送时保存内部文号
		GovdocMarkVO serialNoVo = baseVo.getSerialNoVo();
		if(serialNoVo != null) {
			serialNoVo.setAction("send");
			this.saveMark(serialNoVo);
			if(Strings.isNotEmpty(serialNoVo.getMarkList())) {
				markList.addAll(serialNoVo.getMarkList());
			}
			if(Strings.isNotEmpty(serialNoVo.getHistoryList())) {
				historyList.addAll(serialNoVo.getHistoryList());
			}
		}
		//发送时保存签收文号
		GovdocMarkVO signMarkVo = baseVo.getSignMarkVo();
		if(signMarkVo != null) {
			signMarkVo.setAction("send");
			this.saveMark(signMarkVo);
			if(Strings.isNotEmpty(signMarkVo.getMarkList())) {
				markList.addAll(signMarkVo.getMarkList());
			}
			if(Strings.isNotEmpty(signMarkVo.getHistoryList())) {
				historyList.addAll(signMarkVo.getHistoryList());
			}
		}
		if(Strings.isNotEmpty(markList)) {
			edocMarkManager.save(markList);
		}
		if(Strings.isNotEmpty(historyList)) {
			edocMarkHistoryManager.save(historyList);
		}
		return true;
	}
	@Override
	public boolean saveDealMark(GovdocBaseVO baseVo) throws BusinessException {
		Object fromDistribute = AppContext.getRawRequest().getAttribute("fromDistribute");
		if(fromDistribute!=null && (Boolean)fromDistribute) {
			return true;
		}
		LOGGER.info("进入公文文号处理方法 saveDealMark。。。");
		List<EdocMark> markList = new ArrayList<EdocMark>();
		List<EdocMarkHistory> historyList = new ArrayList<EdocMarkHistory>();
		List<GovdocMarkVO> finishVoList = new ArrayList<GovdocMarkVO>();
		//处理时保存公文文号
		GovdocMarkVO docMarkVo = baseVo.getDocMarkVo();
		if(docMarkVo != null) {//若公文文号可编辑
			LOGGER.info("公文文号可编辑" + docMarkVo.getMarkstr());
			docMarkVo.setAction(baseVo.getAction());
			docMarkVo.setDealAction(baseVo.getDealAction());
			docMarkVo.setFensong(baseVo.isFensong());
			docMarkVo.setCurrentUser(baseVo.getCurrentUser());
			this.saveMark(docMarkVo);
			if(Strings.isNotEmpty(docMarkVo.getMarkList())) {
				markList.addAll(docMarkVo.getMarkList());
			}
			if(Strings.isNotEmpty(docMarkVo.getHistoryList())) {
				historyList.addAll(docMarkVo.getHistoryList());
			}
		} else {//若公文文号不可编辑
			if(baseVo.isLastNode()) { //流程即将结束
				//发文选择模式启流程占号，流程结束前进行公文文号占号
				if(baseVo.getSummary().getGovdocType().intValue() == 1 && Strings.isNotBlank(baseVo.getSummary().getDocMark())) {
					GovdocMarkVO markVo = new GovdocMarkVO();
					markVo.setMarkType(0);
					markVo.setAction("finish");
					markVo.setCurrentUser(baseVo.getCurrentUser());
					markVo.setIsIncreatement(false);
					baseVo.setDocMarkVo(markVo);
					finishVoList.add(markVo);
				}
			} else {//流程暂未结束
				boolean isFawen = baseVo.getSummary().getGovdocType().intValue() == 1;
				if(isFawen) {//发文
					String usedType = govdocMarkOpenManager.getSendUsedType("0", isFawen);
					if("2".equals(usedType)) {//发文文号占号-开启模式2
						List<EdocMarkHistory> oldhistoryList = edocMarkHistoryManager.getMarkHistorysByEdocID(baseVo.getSummary().getId(), 0);
						if(Strings.isEmpty(oldhistoryList)) {//若公文未进占号
							GovdocMarkRecord record = govdocMarkDao.getMarkRecord(baseVo.getSummary().getId(), 0);
							if(record != null) {
								GovdocMarkVO markVo = new GovdocMarkVO();
								markVo.setGovdocType(baseVo.getSummary().getGovdocType());
								markVo.setSummaryId(baseVo.getSummary().getId());
								markVo.setSubject(baseVo.getSummary().getSubject());
								markVo.setMarkType(record.getMarkType());
								markVo.setSelectType(record.getSelectType());
								markVo.setMarkstr(record.getMarkstr());
								if(record.getMarkDefId() != null) {
									markVo.setCategoryId(record.getCategoryId());
									markVo.setMarkDefId(record.getMarkDefId());
									markVo.setMarkNumber(record.getMarkNumber());
									markVo.setYearNo(record.getYearNo());
									EdocMarkDefinition markDef = this.getMarkDef(record.getMarkDefId());
									if(markDef != null) {
										markVo.setMarkDef(markDef);
										markVo.setCurrentNo(markDef.getEdocMarkCategory().getCurrentNo());
										markVo.setYearEnabled(markDef.getEdocMarkCategory().getYearEnabled());
									}
								}
								markVo.setUsedType(Integer.parseInt(usedType));
								markVo.setUsedState(record.getUsedState());
								markVo.setFlowState(record.getFlowState());
								markVo.setRecord(record);
								markVo.setCurrentUser(baseVo.getCurrentUser());
								markVo.setIsIncreatement(false);
								markVo.setNeedQueryHistory(false);
								saveEdocMarkHistory(markVo);
								if(Strings.isNotEmpty(markVo.getHistoryList())) {
									historyList.addAll(markVo.getHistoryList());
								}
							}
						}
					}
				}
			}
		}
		//处理时保存内部文号
		GovdocMarkVO serialNoVo = baseVo.getSerialNoVo();
		if(serialNoVo != null) {//若内部文号可编辑
			LOGGER.info("内部文号可编辑" + serialNoVo.getMarkstr());
			serialNoVo.setAction(baseVo.getAction());
			serialNoVo.setDealAction(baseVo.getDealAction());
			serialNoVo.setFensong(baseVo.isFensong());
			serialNoVo.setCurrentUser(baseVo.getCurrentUser());
			this.saveMark(serialNoVo);
			if(Strings.isNotEmpty(serialNoVo.getMarkList())) {
				markList.addAll(serialNoVo.getMarkList());
			}
			if(Strings.isNotEmpty(serialNoVo.getHistoryList())) {
				historyList.addAll(serialNoVo.getHistoryList());
			}
		} else {//若内部文号不可编辑
			if(baseVo.isLastNode()) {
				//发文选择模式启流程占号，流程结束前进行内部文号占号
				if(baseVo.getSummary().getGovdocType().intValue() == 1 && Strings.isNotBlank(baseVo.getSummary().getSerialNo())) {
					GovdocMarkVO markVo = new GovdocMarkVO();
					markVo.setMarkType(1);
					markVo.setAction("finish");
					markVo.setCurrentUser(baseVo.getCurrentUser());
					markVo.setIsIncreatement(false);
					baseVo.setSerialNoVo(markVo);
					finishVoList.add(markVo);
				}
			}
		}
		//处理时保存签收文号
		GovdocMarkVO signMarkVo = baseVo.getSignMarkVo();
		if(signMarkVo != null) {//若签收编号可编辑
			LOGGER.info("签收编号可编辑" + signMarkVo.getMarkstr());
			signMarkVo.setAction(baseVo.getAction());
			signMarkVo.setCurrentUser(baseVo.getCurrentUser());
			this.saveMark(signMarkVo);
			if(Strings.isNotEmpty(signMarkVo.getMarkList())) {
				markList.addAll(signMarkVo.getMarkList());
			}
			if(Strings.isNotEmpty(signMarkVo.getHistoryList())) {
				historyList.addAll(signMarkVo.getHistoryList());
			}
		}
		//流程结束时，公文文号及内部文号进行占号
		if(Strings.isNotEmpty(finishVoList)) {
			saveFinishMark(baseVo);
			docMarkVo = baseVo.getDocMarkVo();
			if(docMarkVo != null) {
				if(Strings.isNotEmpty(docMarkVo.getMarkList())) {
					markList.addAll(docMarkVo.getMarkList());
				}
				if(Strings.isNotEmpty(docMarkVo.getHistoryList())) {
					historyList.addAll(docMarkVo.getHistoryList());
				}
			}
			serialNoVo = baseVo.getSerialNoVo();
			if(serialNoVo != null) {
				if(Strings.isNotEmpty(serialNoVo.getMarkList())) {
					markList.addAll(serialNoVo.getMarkList());
				}
				if(Strings.isNotEmpty(serialNoVo.getHistoryList())) {
					historyList.addAll(serialNoVo.getHistoryList());
				}
			}
		}
		if(Strings.isNotEmpty(markList)) {
			edocMarkManager.save(markList);
		}
		if(Strings.isNotEmpty(historyList)) {
			edocMarkHistoryManager.save(historyList);
		}
		return true;
	}
	
	@Override
	public boolean saveFinishMark(GovdocBaseVO baseVo) throws BusinessException {
		boolean isFawen = baseVo.getSummary().getGovdocType().intValue() == 1;
		Long summaryId = baseVo.getSummaryId();
		if(baseVo.isChildFlow()) {
			if(baseVo.getParentSummaryId() == null) {
				LOGGER.info("saveFinishMark 该子流程未与父流程绑定，不做占号处理! summaryId = " + summaryId);
			} else {
				summaryId = baseVo.getParentSummaryId();
			}
		}
		List<GovdocMarkRecord> recordList = govdocMarkDao.getMarkRecord(summaryId);
		if(Strings.isNotEmpty(recordList)) {
			//兼容V57版公文子流程与父流程未绑定关系
			if(baseVo.isChildFlow() && baseVo.getParentSummaryId() == null) {
				GovdocMarkRecord record = recordList.get(0);
				GovdocMarkRecord parentRecord = govdocMarkDao.getParentMarkRecord(record.getFormDataId(), record.getMarkType());
				if(parentRecord != null && parentRecord.getSummaryId()!=null) {
					baseVo.setParentSummaryId(parentRecord.getSummaryId());
					summaryId = baseVo.getParentSummaryId();
					recordList = govdocMarkDao.getMarkRecord(summaryId);
				}
			}
			for(GovdocMarkRecord bean : recordList) {
				if(bean.getMarkType().intValue() == 0) {
					GovdocMarkVO docMarkVo = baseVo.getDocMarkVo();
					if(docMarkVo != null) {
						String usedType = govdocMarkOpenManager.getSendUsedType(String.valueOf(bean.getMarkType()), isFawen);
						//模式1：发起提交时不占用文号，其它文可使用-启用流程结束占号
						boolean isUsedByFs_Finish = govdocMarkOpenManager.isUsedByFensong_Finish(String.valueOf(bean.getMarkType()), isFawen);
						if(isUsedByFs_Finish) {
							docMarkVo.setAction("finish");
							docMarkVo.setRecord(bean);
							if(bean.getMarkDefId() != null) {
								docMarkVo.setMarkDef(this.getMarkDef(bean.getMarkDefId()));
								docMarkVo.setMarkDefId(bean.getMarkDefId());
								docMarkVo.setCategoryId(bean.getCategoryId());
								docMarkVo.setCallId(bean.getCallId());
								docMarkVo.setWordNo(bean.getWordNo());
								docMarkVo.setMarkNumber(bean.getMarkNumber());
								docMarkVo.setYearNo(bean.getYearNo());
							}
							docMarkVo.setSummaryId(bean.getSummaryId());
							docMarkVo.setSelectType(bean.getSelectType());
							docMarkVo.setMarkstr(bean.getMarkstr());
							docMarkVo.setYearNo(bean.getYearNo());
							docMarkVo.setUsedType(Integer.parseInt(usedType));
							docMarkVo.setUsedState(bean.getUsedState());
							docMarkVo.setCurrentUser(baseVo.getCurrentUser());
							docMarkVo.setDomainId(bean.getDomainId());
							docMarkVo.setSubject(baseVo.getSummary().getSubject());
							
							bean.setUsedState(UsedStateEnum.used.ordinal());
							bean.setFlowState(flowState.finish.ordinal());
							if(docMarkVo.getCallId() != null) {
								bean.setCallId(docMarkVo.getCallId());
							}
							//子流程与父流程未绑定的，不做占号处理
							if(baseVo.isChildFlow() && baseVo.getParentSummaryId()==null) {
								continue;
							}
							saveEdocMarkHistory(docMarkVo);
						}
					}
				} else if(bean.getMarkType().intValue() == 1) {
					GovdocMarkVO serialNoVo = baseVo.getSerialNoVo();
					if(serialNoVo != null) {
						String usedType = govdocMarkOpenManager.getSendUsedType(String.valueOf(bean.getMarkType()), isFawen);
						//模式1：发起提交时不占用文号，其它文可使用-启用流程结束占号
						boolean isUsedByFs_Finish = govdocMarkOpenManager.isUsedByFensong_Finish(String.valueOf(bean.getMarkType()), isFawen);
						if(isUsedByFs_Finish) {
							serialNoVo.setAction("finish");
							serialNoVo.setRecord(bean);
							if(bean.getMarkDefId() != null) {
								serialNoVo.setMarkDef(this.getMarkDef(bean.getMarkDefId()));
								serialNoVo.setMarkDefId(bean.getMarkDefId());
								serialNoVo.setCategoryId(bean.getCategoryId());
								serialNoVo.setWordNo(bean.getWordNo());
								serialNoVo.setMarkNumber(bean.getMarkNumber());
							}
							serialNoVo.setSummaryId(bean.getSummaryId());
							serialNoVo.setSelectType(bean.getSelectType());
							serialNoVo.setMarkstr(bean.getMarkstr());
							serialNoVo.setYearNo(bean.getYearNo());
							serialNoVo.setUsedType(Integer.parseInt(usedType));
							serialNoVo.setUsedState(bean.getUsedState());
							serialNoVo.setDomainId(bean.getDomainId());
							serialNoVo.setCurrentUser(baseVo.getCurrentUser());
							serialNoVo.setSubject(baseVo.getSummary().getSubject());
							
							bean.setUsedState(UsedStateEnum.used.ordinal());
							bean.setFlowState(flowState.finish.ordinal());
							if(serialNoVo.getCallId() != null) {
								bean.setCallId(serialNoVo.getCallId());
							}
							//子流程与父流程未绑定的，不做占号处理
							if(baseVo.isChildFlow() && baseVo.getParentSummaryId()==null) {
								continue;
							}
							saveEdocMarkHistory(serialNoVo);
						}
					}
				}
			}
		}
		if(Strings.isNotEmpty(recordList)) {
			govdocMarkDao.updateMarkRecord(recordList);
		}
		return true;
	}
	
	public boolean saveRegisterSendMarkOld(GovdocBaseVO newVo) throws BusinessException {
		EdocRegister register = newVo.getRegister();
		Long registerId = register.getId();
    	String serialNo = register.getSerialNo();
    	
    	List<String> markstrList = new ArrayList<String>();
    	List<EdocMark> markList = new ArrayList<EdocMark>();
    	List<EdocMarkHistory> historyList = new ArrayList<EdocMarkHistory>();
    	
    	boolean isUnbind = false;
    	
    	//第一个公文文号
        GovdocMarkVO serialNoVo = GovdocMarkHelper.parseDocMarkOld(serialNo);
        if(serialNoVo != null) {
        	register.setSerialNo(serialNoVo.getMarkstr());
        	markstrList.add(serialNoVo.getMarkstr());
        	if(!newVo.isNewBusiness()) {
        		isUnbind = true;
        	}
        	serialNoVo.setCurrentUser(newVo.getCurrentUser());
        	if(serialNoVo.getMarkDefId() != null && serialNoVo.getMarkDef()==null) {
        		serialNoVo.setMarkDef(this.getMarkDef(serialNoVo.getMarkDefId()));
        	}
        	if(serialNoVo.getMarkDefId() != null && serialNoVo.getMarkDef()==null) {
        		LOGGER.error("发送公文时保存内部文号获取文号定义对象为空 " + serialNoVo.getMarkDefId());
        	}
    		EdocMarkHistory history = new EdocMarkHistory();
    		history.setNewId();
    		history.setMarkType(1);//公文文号
    		history.setMarkNum(0);
    		history.setRealUsed(1);
    		history.setSubject(register.getSubject());
    		history.setCreateTime(newVo.getCurrentDate());
    		history.setCompleteTime(newVo.getCurrentDate());
    		history.setEdocId(register.getId());
    		history.setDomainId(register.getOrgAccountId());
    		history.setSubject(register.getSubject());
    		history.setGovdocType(0);
    		history.setTransferStatus(0);
    		history.setCreateUserId(register.getCreateUserId());
    		history.setLastUserId(register.getCreateUserId());
    		history.setDocMark(serialNoVo.getMarkstr());
    		history.setSelectType(serialNoVo.getSelectType());
    		if(serialNoVo.getSelectType() != GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal()) {//非手写
    			if(serialNoVo.getMarkDef() != null) {
        			history.setCategoryId(serialNoVo.getMarkDef().getCategoryId());
	    		}
        		history.setMarkDefId(serialNoVo.getMarkDefId());
        		history.setDocMarkNo(serialNoVo.getMarkNumber());
        		history.setYearNo(Integer.parseInt(serialNoVo.getYearNo()));	
    		}
    		historyList.add(history);
        } else if(Strings.isNotBlank(serialNo)) {
        	register.setSerialNo(serialNo);
        	markstrList.add(serialNo);
        }
        
    	if(Strings.isNotEmpty(markstrList)) {
    		edocMarkManager.deleteEdocMarkByMarkstr(markstrList);
    	}
    	if(isUnbind) {
    		govdocMarkDao.unbindMark(registerId);
    	}
    	if(Strings.isNotEmpty(markList)) {
    		edocMarkManager.save(markList);
    	}
    	if(Strings.isNotEmpty(historyList)) {
     		edocMarkHistoryManager.save(historyList);
     	}
    	
        //内部文号跳号
        if(serialNoVo != null) {
			if(serialNoVo.getSelectType() != 1 && serialNoVo.getMarkDef()!=null) {//非手写文号
				if(serialNoVo.getMarkNumber() != null && serialNoVo.getMarkNumber().longValue()>0) {//流水号不为空
					//不按年度编号或按年度编号，年号非本年
					String yearNo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
					if(!serialNoVo.getYearEnabled() || (serialNoVo.getYearEnabled() && yearNo.equals(serialNoVo.getYearNo()))) {
						updateNextCurrentNo(serialNoVo, serialNoVo.getMarkNumber());	
					}
				}
			}
	    }
		return true;
	}
	
	public boolean saveRegisterDraftMarkOld(GovdocBaseVO newVo) throws BusinessException {
		EdocRegister register = newVo.getRegister();
		Long registerId = register.getId();
    	String serialNo = register.getSerialNo();
    	
    	List<String> markstrList = new ArrayList<String>();
    	List<EdocMark> markList = new ArrayList<EdocMark>();
    	
    	boolean isUnbind = false;
    	
        //内部文号
        GovdocMarkVO serialNoVo = GovdocMarkHelper.parseDocMarkOld(serialNo);
        if(serialNoVo != null) {
        	register.setSerialNo(serialNoVo.getMarkstr());
        	markstrList.add(serialNoVo.getMarkstr());
        	if(!newVo.isNewBusiness()) {
        		isUnbind = true;
        	}
        	serialNoVo.setCurrentUser(newVo.getCurrentUser());
        	if(serialNoVo.getMarkDefId() != null && serialNoVo.getMarkDef()==null) {
        		serialNoVo.setMarkDef(this.getMarkDef(serialNoVo.getMarkDefId()));
        	}
        	if(serialNoVo.getMarkDefId() != null && serialNoVo.getMarkDef()==null) {
        		LOGGER.error("保存待发公文时保存内部文号获取文号定义对象为空 " + serialNoVo.getMarkDefId());
        	}
        	
        	EdocMark newMark = new EdocMark();
    		newMark.setNewId();
    		newMark.setMarkType(1);//内部编号
    		newMark.setMarkNum(0);
    		newMark.setRealUsed(1);
    		newMark.setCreateTime(newVo.getCurrentDate());
    		newMark.setEdocId(register.getId());
    		newMark.setDomainId(register.getOrgAccountId());
    		newMark.setSubject(register.getSubject());
    		newMark.setGovdocType(1);
    		newMark.setCreateUserId(register.getCreateUserId());
    		newMark.setDocMark(serialNoVo.getMarkstr());
    		newMark.setSelectType(serialNoVo.getSelectType());
    		if(serialNoVo.getSelectType() != GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal()) {//非手写
    			if(serialNoVo.getMarkDef() != null) {
	    			newMark.setCategoryId(serialNoVo.getMarkDef().getCategoryId());
	    		}
    			newMark.setMarkDefId(serialNoVo.getMarkDefId());
	    		newMark.setDocMarkNo(serialNoVo.getMarkNumber());
	    		newMark.setYearNo(Integer.parseInt(serialNoVo.getYearNo()));
    		}
    		markList.add(newMark);
        } else if(Strings.isNotBlank(serialNo)) {
        	register.setSerialNo(serialNo);
        	markstrList.add(serialNo);
        }
    	if(Strings.isNotEmpty(markstrList)) {
    		edocMarkManager.deleteEdocMarkByMarkstr(markstrList);
    	}
    	if(isUnbind) {
    		govdocMarkDao.unbindMark(registerId);
    	}
    	if(Strings.isNotEmpty(markList)) {
    		edocMarkManager.save(markList);
    	}
        //内部文号跳号
        if(serialNoVo != null) {
			if(serialNoVo.getSelectType() != 1 && serialNoVo.getMarkDef()!=null) {//非手写文号
				if(serialNoVo.getMarkNumber() != null && serialNoVo.getMarkNumber().longValue()>0) {//流水号不为空
					//不按年度编号或按年度编号，年号非本年
					String yearNo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
					if(!serialNoVo.getYearEnabled() || (serialNoVo.getYearEnabled() && yearNo.equals(serialNoVo.getYearNo()))) {
						updateNextCurrentNo(serialNoVo, serialNoVo.getMarkNumber());	
					}
				}
			}
	    }
        return true;
	}
	
	public boolean saveDraftMarkOld(GovdocBaseVO newVo) throws BusinessException {
		EdocSummary summary = newVo.getSummary();
		Long summaryId = summary.getId();
    	String docMark = summary.getDocMark();
    	String docMark2 = summary.getDocMark2();
    	String serialNo = summary.getSerialNo();
    	
    	List<String> markstrList = new ArrayList<String>();
    	List<EdocMark> markList = new ArrayList<EdocMark>();
    	
    	boolean isUnbind = false;
    	
    	//第一个公文文号
        GovdocMarkVO docMarkVo = GovdocMarkHelper.parseDocMarkOld(docMark);
        if(docMarkVo != null) {
        	summary.setDocMark(docMarkVo.getMarkstr());
        	markstrList.add(docMarkVo.getMarkstr());
        	if(!newVo.isNewBusiness()) {
        		isUnbind = true;
        	}
        	docMarkVo.setCurrentUser(newVo.getCurrentUser());
        	if(docMarkVo.getMarkDefId() != null && docMarkVo.getMarkDef()==null) {
        		docMarkVo.setMarkDef(this.getMarkDef(docMarkVo.getMarkDefId()));
        	}
        	if(docMarkVo.getMarkDefId() != null && docMarkVo.getMarkDef()==null) {
        		LOGGER.error("保存待发公文时保存公文文号获取文号定义对象为空 " + docMarkVo.getMarkDefId());
        	}
    		EdocMark newMark = new EdocMark();
    		newMark.setNewId();
    		newMark.setMarkType(0);//公文文号
    		newMark.setMarkNum(0);
    		newMark.setRealUsed(1);
    		newMark.setCreateTime(newVo.getCurrentDate());
    		newMark.setEdocId(summary.getId());
    		newMark.setDomainId(summary.getOrgAccountId());
    		newMark.setSubject(summary.getSubject());
    		newMark.setGovdocType(summary.getGovdocType());
    		newMark.setCreateUserId(summary.getStartMemberId());
    		newMark.setDocMark(docMarkVo.getMarkstr());
    		newMark.setSelectType(docMarkVo.getSelectType());
    		if(docMarkVo.getSelectType() != GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal()) {//非手写
    			if(docMarkVo.getMarkDef() != null) {
        			newMark.setCategoryId(docMarkVo.getMarkDef().getCategoryId());	
        		}
    			newMark.setMarkDefId(docMarkVo.getMarkDefId());
        		newMark.setDocMarkNo(docMarkVo.getMarkNumber());
        		newMark.setYearNo(Integer.parseInt(docMarkVo.getYearNo()));
    		}
    		markList.add(newMark);
        } else if(Strings.isNotBlank(docMark)) {
        	summary.setDocMark(docMark);
        	if(summary.getEdocType() != 1) {//收文时不对公文文号做断号占号处理
        		markstrList.add(docMark);
        	}
        }
        
        //第二个公文文号
        GovdocMarkVO docMark2Vo = GovdocMarkHelper.parseDocMarkOld(docMark2);
        if(docMark2Vo != null) {
        	summary.setDocMark2(docMark2Vo.getMarkstr());
        	markstrList.add(docMark2Vo.getMarkstr());
        	if(!newVo.isNewBusiness()) {
        		isUnbind = true;
        	}
        	docMark2Vo.setCurrentUser(newVo.getCurrentUser());
        	if(docMark2Vo.getMarkDefId() != null && docMark2Vo.getMarkDef()==null) {
        		docMark2Vo.setMarkDef(this.getMarkDef(docMark2Vo.getMarkDefId()));
        	}
        	if(docMark2Vo.getMarkDefId() != null && docMark2Vo.getMarkDef()==null) {
        		LOGGER.error("保存待发公文时保存公文文号2获取文号定义对象为空 " + docMark2Vo.getMarkDefId());
        	}
        	
    		EdocMark newMark = new EdocMark();
    		newMark.setNewId();
    		newMark.setMarkType(0);//公文文号
    		newMark.setMarkNum(1);//公文文号2
    		newMark.setRealUsed(1);
    		newMark.setCreateTime(newVo.getCurrentDate());
    		newMark.setEdocId(summary.getId());
    		newMark.setDomainId(summary.getOrgAccountId());
    		newMark.setSubject(summary.getSubject());
    		newMark.setGovdocType(summary.getGovdocType());
    		newMark.setCreateUserId(summary.getStartMemberId());
    		newMark.setDocMark(docMark2Vo.getMarkstr());
    		newMark.setSelectType(docMark2Vo.getSelectType());
    		if(docMark2Vo.getSelectType() != GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal()) {//非手写
    			if(docMark2Vo.getMarkDef() != null) {
        			newMark.setCategoryId(docMark2Vo.getMarkDef().getCategoryId());
        		}
    			newMark.setMarkDefId(docMark2Vo.getMarkDefId());
        		newMark.setDocMarkNo(docMark2Vo.getMarkNumber());
        		newMark.setYearNo(Integer.parseInt(docMark2Vo.getYearNo()));	
    		}
    		markList.add(newMark);
        } else if(Strings.isNotBlank(docMark2)) {
        	summary.setDocMark2(docMark2);
        	if(summary.getEdocType() != 1) {//收文时不对公文文号做断号占号处理
        		markstrList.add(docMark2);
        	}
        }
        
        //内部文号
        GovdocMarkVO serialNoVo = GovdocMarkHelper.parseDocMarkOld(serialNo);
        if(serialNoVo != null) {
        	summary.setSerialNo(serialNoVo.getMarkstr());
        	markstrList.add(serialNoVo.getMarkstr());
        	if(!newVo.isNewBusiness()) {
        		isUnbind = true;
        	}
        	serialNoVo.setCurrentUser(newVo.getCurrentUser());
        	if(serialNoVo.getMarkDefId() != null && serialNoVo.getMarkDef()==null) {
        		serialNoVo.setMarkDef(this.getMarkDef(serialNoVo.getMarkDefId()));
        	}
        	if(serialNoVo.getMarkDefId() != null && serialNoVo.getMarkDef()==null) {
        		LOGGER.error("保存待发公文时保存内部文号获取文号定义对象为空 " + serialNoVo.getMarkDefId());
        	}
        	
        	EdocMark newMark = new EdocMark();
    		newMark.setNewId();
    		newMark.setMarkType(1);//内部编号
    		newMark.setMarkNum(0);
    		newMark.setRealUsed(1);
    		newMark.setCreateTime(newVo.getCurrentDate());
    		newMark.setEdocId(summary.getId());
    		newMark.setDomainId(summary.getOrgAccountId());
    		newMark.setSubject(summary.getSubject());
    		newMark.setGovdocType(summary.getGovdocType());
    		newMark.setCreateUserId(summary.getStartMemberId());
    		newMark.setDocMark(serialNoVo.getMarkstr());
    		newMark.setSelectType(serialNoVo.getSelectType());
    		if(serialNoVo.getSelectType() != GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal()) {//非手写
    			if(serialNoVo.getMarkDef() != null) {
	    			newMark.setCategoryId(serialNoVo.getMarkDef().getCategoryId());
	    		}
    			newMark.setMarkDefId(serialNoVo.getMarkDefId());
	    		newMark.setDocMarkNo(serialNoVo.getMarkNumber());
	    		newMark.setYearNo(Integer.parseInt(serialNoVo.getYearNo()));
    		}
    		markList.add(newMark);
        } else if(Strings.isNotBlank(serialNo)) {
        	summary.setSerialNo(serialNo);
        	markstrList.add(serialNo);
        }
        
    	if(Strings.isNotEmpty(markstrList)) {
    		edocMarkManager.deleteEdocMarkByMarkstr(markstrList);
    	}
    	if(isUnbind) {
    		govdocMarkDao.unbindMark(summaryId);
    	}
    	//老公文保存待发-若老公文登记时文号进了占号
    	if(summary.getEdocType() == 1 && newVo.getRegister() != null && Strings.isNotBlank(newVo.getRegister().getSerialNo())) {
    		if(!(newVo.getRegister().getSerialNo().equals(summary.getSerialNo()))) {//若老公文收登记与分发收文编号不一致，则进断号，登记的占号暂不删除
	    		/*List<EdocMarkHistory> oldhistoryList = edocMarkHistoryManager.getMarkHistorysByEdocID(newVo.getRegister().getId());
	    		if(Strings.isNotEmpty(oldhistoryList)) {
	    			for(EdocMarkHistory oldhistory : oldhistoryList) {
	    				EdocMark newMark = GovdocMarkHelper.convertToMark(oldhistory);
	    				if(newMark.getDomainId() == null) {
	    					newMark.setDomainId(summary.getOrgAccountId());
	    				}
	    				markList.add(newMark);
	    			}
	    		}*/
    		}//若相同则当前内部文号不进断号，登记的占号也不删除
    	}
    	if(Strings.isNotEmpty(markList)) {
    		edocMarkManager.save(markList);
    	}
    	
    	//文号跳号
        if(docMarkVo != null) {
			if(docMarkVo.getSelectType() != 1 && docMarkVo.getMarkDef()!=null) {//非手写文号
				if(docMarkVo.getMarkNumber() != null && docMarkVo.getMarkNumber().longValue()>0) {//流水号不为空
					//不按年度编号或按年度编号，年号非本年
					String yearNo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
					if(!docMarkVo.getYearEnabled() || (docMarkVo.getYearEnabled() && yearNo.equals(docMarkVo.getYearNo()))) {
						updateNextCurrentNo(docMarkVo, docMarkVo.getMarkNumber());	
					}
				}
			}
        }
        
        //文号2跳号
        if(docMark2Vo != null) {
			if(docMark2Vo.getSelectType() != 1 && docMark2Vo.getMarkDef()!=null) {//非手写文号
				if(docMark2Vo.getMarkNumber() != null && docMark2Vo.getMarkNumber().longValue()>0) {//流水号不为空
					//不按年度编号或按年度编号，年号非本年
					String yearNo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
					if(!docMark2Vo.getYearEnabled() || (docMark2Vo.getYearEnabled() && yearNo.equals(docMark2Vo.getYearNo()))) {
						updateNextCurrentNo(docMark2Vo, docMark2Vo.getMarkNumber());	
					}
				}
			}
        }
        
        //内部文号跳号
        if(serialNoVo != null) {
			if(serialNoVo.getSelectType() != 1 && serialNoVo.getMarkDef()!=null) {//非手写文号
				if(serialNoVo.getMarkNumber() != null && serialNoVo.getMarkNumber().longValue()>0) {//流水号不为空
					//不按年度编号或按年度编号，年号非本年
					String yearNo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
					if(!serialNoVo.getYearEnabled() || (serialNoVo.getYearEnabled() && yearNo.equals(serialNoVo.getYearNo()))) {
						updateNextCurrentNo(serialNoVo, serialNoVo.getMarkNumber());	
					}
				}
			}
	    }
        return true;
	}
	
	/**
	 * 公文文号流程结束
	 * @param newVo
	 * @return
	 * @throws BusinessException
	 */
	public boolean saveDealMarkOld(GovdocBaseVO newVo) throws BusinessException {
		EdocSummary summary = newVo.getSummary();
		Long summaryId = summary.getId();
    	String docMark = summary.getDocMark();
    	String docMark2 = summary.getDocMark2();
    	String serialNo = summary.getSerialNo();
    	
    	List<String> markstrList = new ArrayList<String>();
    	//第一个公文文号
        GovdocMarkVO docMarkVo = GovdocMarkHelper.parseDocMarkOld(docMark);
        if(docMarkVo != null) {
        	summary.setDocMark(docMarkVo.getMarkstr());
        	markstrList.add(docMarkVo.getMarkstr());
        	
			docMarkVo.setCurrentUser(newVo.getCurrentUser());
			if(docMarkVo.getMarkDefId() != null && docMarkVo.getMarkDef()==null) {
				docMarkVo.setMarkDef(this.getMarkDef(docMarkVo.getMarkDefId()));
        	}
			if(docMarkVo.getMarkDefId() != null && docMarkVo.getMarkDef()==null) {
				LOGGER.error("处理公文时保存内部文号获取文号定义对象为空 " + docMarkVo.getMarkDefId());
			}
        } else if(Strings.isNotBlank(docMark)) {
        	summary.setDocMark(docMark);
        	markstrList.add(docMark);
        }
        GovdocMarkVO docMark2Vo = GovdocMarkHelper.parseDocMarkOld(docMark2);
        if(docMark2Vo != null) {
        	summary.setDocMark2(docMark2Vo.getMarkstr());
        	markstrList.add(docMark2Vo.getMarkstr());
			
			docMark2Vo.setCurrentUser(newVo.getCurrentUser());
			if(docMark2Vo.getMarkDefId() != null && docMark2Vo.getMarkDef()==null) {
				docMark2Vo.setMarkDef(this.getMarkDef(docMark2Vo.getMarkDefId()));
        	}
			if(docMark2Vo.getMarkDefId() != null && docMark2Vo.getMarkDef()==null) {
				LOGGER.error("处理公文时保存内部文号获取文号定义对象为空 " + docMark2Vo.getMarkDefId());
			}
        } else if(Strings.isNotBlank(docMark2)) {
        	summary.setDocMark2(docMark2);
        	markstrList.add(docMark2);
        }
        GovdocMarkVO serialNoVo = GovdocMarkHelper.parseDocMarkOld(serialNo);
        if(serialNoVo != null) {
        	summary.setSerialNo(serialNoVo.getMarkstr());
        	markstrList.add(serialNoVo.getMarkstr());
        	
			serialNoVo.setCurrentUser(newVo.getCurrentUser());
			if(serialNoVo.getMarkDefId() != null && serialNoVo.getMarkDef()==null) {
				serialNoVo.setMarkDef(this.getMarkDef(serialNoVo.getMarkDefId()));
        	}
			if(serialNoVo.getMarkDefId() != null && serialNoVo.getMarkDef()==null) {
				LOGGER.error("处理公文时保存内部文号获取文号定义对象为空 " + serialNoVo.getMarkDefId());
			}
        } else if(Strings.isNotBlank(serialNo)) {
        	summary.setSerialNo(serialNo);
        	markstrList.add(serialNo);
        }
    	
        List<EdocMark> markList = new ArrayList<EdocMark>();
        List<EdocMarkHistory> historyList = new ArrayList<EdocMarkHistory>();
    	if(newVo.isLastNode()) {//流程即将结束
    		Date currentDate = new Date();
    		if(Strings.isNotEmpty(markstrList)) {
    			//收文的公文文号不占号，这里不作处理
    			if(summary.getEdocType()!=1 && Strings.isNotBlank(docMark)) {
    				EdocMarkHistory history = new EdocMarkHistory();
        			history.setNewId();
        			history.setMarkType(0);//公文文号
        			history.setMarkNum(0);
        			history.setRealUsed(1);
        			history.setSubject(summary.getSubject());
        			history.setCreateTime(currentDate);
        			history.setCompleteTime(currentDate);
        			history.setEdocId(summaryId);
        			history.setDomainId(summary.getOrgAccountId());
        			history.setSubject(summary.getSubject());
        			history.setGovdocType(summary.getGovdocType());
        			history.setTransferStatus(summary.getTransferStatus());
        			history.setCreateUserId(summary.getStartUserId());
        			history.setLastUserId(summary.getStartUserId());
        			history.setDocMark(summary.getDocMark());
        			if(docMarkVo != null) {
        				history.setSelectType(docMarkVo.getSelectType());
        				if(docMarkVo.getSelectType() != GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal()) {//非手写
        					if(docMarkVo.getMarkDef() != null) {
	        					history.setCategoryId(docMarkVo.getMarkDef().getCategoryId());
	        	    		}
	        				history.setMarkDefId(docMarkVo.getMarkDefId());
	        				history.setDocMarkNo(docMarkVo.getMarkNumber());
	        				history.setYearNo(Integer.parseInt(docMarkVo.getYearNo()));	
        				}
        			} else {
        				history.setSelectType(GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal());	
        			}
        			historyList.add(history);
            	}
    			//收文的公文文号不占号
    			if(summary.getEdocType()!=1 && Strings.isNotBlank(docMark2)) {
    				EdocMarkHistory history = new EdocMarkHistory();
        			history.setNewId();
        			history.setMarkType(0);//公文文号2
        			history.setMarkNum(1);
        			history.setRealUsed(1);
        			history.setSubject(summary.getSubject());
        			history.setCreateTime(currentDate);
        			history.setCompleteTime(currentDate);
        			history.setEdocId(summaryId);
        			history.setDomainId(summary.getOrgAccountId());
        			history.setSubject(summary.getSubject());
        			history.setGovdocType(summary.getGovdocType());
        			history.setTransferStatus(summary.getTransferStatus());
        			history.setCreateUserId(summary.getStartUserId());
        			history.setLastUserId(summary.getStartUserId());
        			history.setDocMark(summary.getDocMark2());
        			if(docMark2Vo != null) {
        				history.setSelectType(docMark2Vo.getSelectType());
        				if(docMark2Vo.getSelectType() != GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal()) {//非手写
        					if(docMark2Vo.getMarkDef() != null) {
	        					history.setCategoryId(docMark2Vo.getMarkDef().getCategoryId());
	        	    		}
        					history.setMarkDefId(docMark2Vo.getMarkDefId());
	        				history.setDocMarkNo(docMark2Vo.getMarkNumber());
	        				history.setYearNo(Integer.parseInt(docMark2Vo.getYearNo()));
        				}
        			} else {
        				history.setSelectType(GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal());	
        			}
        			historyList.add(history);
            	}
    			if(Strings.isNotBlank(serialNo)) {
    				EdocMarkHistory history = new EdocMarkHistory();
        			history.setNewId();
        			history.setMarkType(1);//内部编号
        			history.setMarkNum(0);
        			history.setRealUsed(1);
        			history.setSubject(summary.getSubject());
        			history.setCreateTime(currentDate);
        			history.setCompleteTime(currentDate);
        			history.setEdocId(summaryId);
        			history.setDomainId(summary.getOrgAccountId());
        			history.setSubject(summary.getSubject());
        			history.setGovdocType(summary.getGovdocType());
        			history.setTransferStatus(summary.getTransferStatus());
        			history.setCreateUserId(summary.getStartUserId());
        			history.setLastUserId(summary.getStartUserId());
        			history.setDocMark(summary.getSerialNo());
        			if(serialNoVo != null) {
        				history.setSelectType(serialNoVo.getSelectType());
        				if(serialNoVo.getSelectType() != GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal()) {//非手写
        					if(serialNoVo.getMarkDef() != null) {
	        					history.setCategoryId(serialNoVo.getMarkDef().getCategoryId());
	        	    		}
        					history.setMarkDefId(serialNoVo.getMarkDefId());
	        				history.setDocMarkNo(serialNoVo.getMarkNumber());
	        				history.setYearNo(Integer.parseInt(serialNoVo.getYearNo()));
        				}
        			} else {
        				history.setSelectType(GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal());	
        			}
        			historyList.add(history);
            	}
    		}
    	} else {//流程还未结束
    		//收文的公文文号不占号
			if(summary.getEdocType() != 1 && docMarkVo != null) {
				EdocMark newMark = new EdocMark();
	    		newMark.setNewId();
	    		newMark.setMarkType(0);//公文文号
	    		newMark.setMarkNum(0);//公文文号2
	    		newMark.setRealUsed(1);
	    		newMark.setCreateTime(newVo.getCurrentDate());
	    		newMark.setEdocId(summary.getId());
	    		newMark.setDomainId(summary.getOrgAccountId());
	    		newMark.setSubject(summary.getSubject());
	    		newMark.setGovdocType(summary.getGovdocType());
	    		newMark.setCreateUserId(summary.getStartMemberId());
	    		newMark.setDocMark(docMarkVo.getMarkstr());
	    		newMark.setSelectType(docMarkVo.getSelectType());
	    		if(docMarkVo.getSelectType() != GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal()) {//非手写
	    			if(docMarkVo.getMarkDef() != null) {
		    			newMark.setCategoryId(docMarkVo.getMarkDef().getCategoryId());
		    		}
	    			newMark.setMarkDefId(docMarkVo.getMarkDefId());
		    		newMark.setDocMarkNo(docMarkVo.getMarkNumber());
		    		newMark.setYearNo(Integer.parseInt(docMarkVo.getYearNo()));
	    		}
	        	markList.add(newMark);
			}
			//收文的公文文号不占号
			if(summary.getEdocType()!=1 && docMark2Vo != null) {
	    		EdocMark newMark = new EdocMark();
	    		newMark.setNewId();
	    		newMark.setMarkType(0);//公文文号
	    		newMark.setMarkNum(1);//公文文号2
	    		newMark.setRealUsed(1);
	    		newMark.setCreateTime(newVo.getCurrentDate());
	    		newMark.setEdocId(summary.getId());
	    		newMark.setDomainId(summary.getOrgAccountId());
	    		newMark.setSubject(summary.getSubject());
	    		newMark.setGovdocType(summary.getGovdocType());
	    		newMark.setCreateUserId(summary.getStartMemberId());
	    		newMark.setDocMark(docMark2Vo.getMarkstr());
	    		newMark.setSelectType(docMark2Vo.getSelectType());
	    		if(docMark2Vo.getSelectType() != GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal()) {//非手写
	    			if(docMark2Vo.getMarkDef() != null) {
		    			newMark.setCategoryId(docMark2Vo.getMarkDef().getCategoryId());
		    		}
		    		newMark.setMarkDefId(docMark2Vo.getMarkDefId());
		    		newMark.setDocMarkNo(docMark2Vo.getMarkNumber());	
		    		newMark.setYearNo(Integer.parseInt(docMark2Vo.getYearNo()));
	    		}
	        	markList.add(newMark);
			}
			if(serialNoVo != null) {
				EdocMark newMark = new EdocMark();
	    		newMark.setNewId();
	    		newMark.setMarkType(0);//内部编号
	    		newMark.setMarkNum(1);
	    		newMark.setRealUsed(1);
	    		newMark.setCreateTime(newVo.getCurrentDate());
	    		newMark.setEdocId(summary.getId());
	    		newMark.setDomainId(summary.getOrgAccountId());
	    		newMark.setSubject(summary.getSubject());
	    		newMark.setGovdocType(summary.getGovdocType());
	    		newMark.setCreateUserId(summary.getStartMemberId());
	    		newMark.setDocMark(serialNoVo.getMarkstr());
	    		newMark.setSelectType(serialNoVo.getSelectType());
	    		if(serialNoVo.getSelectType() != GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal()) {//非手写
	    			if(serialNoVo.getMarkDef() != null) {
		    			newMark.setCategoryId(serialNoVo.getMarkDef().getCategoryId());
		    		}
	    			newMark.setMarkDefId(serialNoVo.getMarkDefId());
		    		newMark.setDocMarkNo(serialNoVo.getMarkNumber());
		    		newMark.setYearNo(Integer.parseInt(serialNoVo.getYearNo()));
	    		}
	        	markList.add(newMark);
			}
    	}
    	
    	if(Strings.isNotEmpty(markstrList)) {
    		edocMarkManager.deleteEdocMarkByMarkstr(markstrList);
    	}
        govdocMarkDao.unbindMark(summary.getId());
        
        if(Strings.isNotEmpty(historyList)) {
    		edocMarkHistoryManager.save(historyList);
    	}
        if(Strings.isNotEmpty(markList)) {
        	edocMarkManager.save(markList);
        }
        
        //文号跳号
        if(docMarkVo != null) {
			if(docMarkVo.getSelectType() != 1 && docMarkVo.getMarkDef()!=null) {//非手写文号
				if(docMarkVo.getMarkNumber() != null && docMarkVo.getMarkNumber().longValue()>0) {//流水号不为空
					//不按年度编号或按年度编号，年号非本年
					String yearNo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
					if(!docMarkVo.getYearEnabled() || (docMarkVo.getYearEnabled() && yearNo.equals(docMarkVo.getYearNo()))) {
						updateNextCurrentNo(docMarkVo, docMarkVo.getMarkNumber());	
					}
				}
			}
        }
        
        //文号2跳号
        if(docMark2Vo != null) {
			if(docMark2Vo.getSelectType() != 1 && docMark2Vo.getMarkDef()!=null) {//非手写文号
				if(docMark2Vo.getMarkNumber() != null && docMark2Vo.getMarkNumber().longValue()>0) {//流水号不为空
					//不按年度编号或按年度编号，年号非本年
					String yearNo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
					if(!docMark2Vo.getYearEnabled() || (docMark2Vo.getYearEnabled() && yearNo.equals(docMark2Vo.getYearNo()))) {
						updateNextCurrentNo(docMark2Vo, docMark2Vo.getMarkNumber());	
					}
				}
			}
        }
        
        //内部文号跳号
        if(serialNoVo != null) {
			if(serialNoVo.getSelectType() != 1 && serialNoVo.getMarkDef()!=null) {//非手写文号
				if(serialNoVo.getMarkNumber() != null && serialNoVo.getMarkNumber().longValue()>0) {//流水号不为空
					//不按年度编号或按年度编号，年号非本年
					String yearNo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
					if(!serialNoVo.getYearEnabled() || (serialNoVo.getYearEnabled() && yearNo.equals(serialNoVo.getYearNo()))) {
						updateNextCurrentNo(serialNoVo, serialNoVo.getMarkNumber());	
					}
				}
			}
	    }
        
        return true;
	}
	
	public boolean saveFinishMarkOld(GovdocBaseVO newVo) throws BusinessException {
		EdocSummary summary = newVo.getSummary();
		Long summaryId = summary.getId();
    	String docMark = summary.getDocMark();
    	String docMark2 = summary.getDocMark2();
    	String serialNo = summary.getSerialNo();
    	
    	boolean hasDocMark = false;
    	boolean hasDocMark2 = false;
    	boolean hasSerialNo = false;
    	boolean hasDocMarkHistory = false;
    	boolean hasDocMark2History = false;
    	boolean hasSerialNoHistory = false;
    	boolean hasDocMarkDH = false;
    	boolean hasDocMark2DH = false;
    	boolean hasSerialNoDH = false;
    	List<String> markstrList = new ArrayList<String>();
        List<EdocMarkHistory> historyList = new ArrayList<EdocMarkHistory>();
    	
        if(Strings.isNotEmpty(docMark)) {
        	markstrList.add(docMark);
        	hasDocMark = true;
        }
        if(Strings.isNotEmpty(docMark2)) {
        	markstrList.add(docMark2);
        	hasDocMark2 = true;
        }
        if(Strings.isNotEmpty(serialNo)) {
        	markstrList.add(serialNo);
        	hasSerialNo = true;
        }
        
        List<EdocMarkHistory> newhistoryList = new ArrayList<EdocMarkHistory>();
		Date currentDate = new Date();
		if(Strings.isNotEmpty(markstrList)) {
			List<EdocMarkHistory> oldhistoryList = govdocMarkDao.findMarkHistoryByEdocId(summaryId, markstrList);
			if(Strings.isNotEmpty(oldhistoryList)) {
				for(EdocMarkHistory bean : oldhistoryList) {
					if(hasDocMark) {
						if(bean.getDocMark().equals(docMark)) {
							hasDocMarkHistory = true;
						}
					}
					if(hasDocMark2) {
						if(Boolean.parseBoolean(bean.getDocMark())) {
							hasDocMark2History = true;
						}
					}
					if(hasSerialNo) {
						if(Boolean.parseBoolean(bean.getDocMark())) {
							hasSerialNoHistory = true;
						}
					}
				}
			}
			
			List<String> markstrhistoryList = new ArrayList<String>();
			if(hasDocMark && !hasDocMarkHistory) {
				markstrhistoryList.add(docMark);
			}
			if(hasDocMark2 && !hasDocMark2History) {
				markstrhistoryList.add(docMark2);
			}
			if(hasSerialNo && !hasSerialNoHistory) {
				markstrhistoryList.add(serialNo);
			}
			if(Strings.isNotEmpty(markstrhistoryList)) {
				List<EdocMark> oldmarkList = govdocMarkDao.findMarkByEdocId(summaryId, markstrhistoryList);
				for(EdocMark bean : oldmarkList) {
					if(hasDocMark && !hasDocMarkHistory) {
						if(bean.getDocMark().equals(docMark)) {
							hasDocMarkDH = true;
						}
					}
					if(hasDocMark2 && !hasDocMark2History) {
						if(Boolean.parseBoolean(bean.getDocMark())) {
							hasDocMark2DH = true;
							EdocMarkHistory newhistory = GovdocMarkHelper.convertToMarkHistory(bean, summary.getStartMemberId(), null);
							newhistoryList.add(newhistory);
						}
					}
					if(hasSerialNo && !hasSerialNoHistory) {
						if(Boolean.parseBoolean(bean.getDocMark())) {
							hasSerialNoDH = true;
							EdocMarkHistory newhistory = GovdocMarkHelper.convertToMarkHistory(bean, summary.getStartMemberId(), null);
							newhistoryList.add(newhistory);
						}
					}
				}
				for(EdocMark bean : oldmarkList) {
					if(hasDocMarkDH && bean.getMarkType()==0) {
						EdocMarkHistory newhistory = GovdocMarkHelper.convertToMarkHistory(bean, summary.getStartMemberId(), null);
						newhistoryList.add(newhistory);
					}
					else if(hasDocMark2DH && bean.getMarkType()==0) {
						EdocMarkHistory newhistory = GovdocMarkHelper.convertToMarkHistory(bean, summary.getStartMemberId(), null);
						newhistoryList.add(newhistory);
					}
					else if(hasSerialNoDH && bean.getMarkType()==1) {
						EdocMarkHistory newhistory = GovdocMarkHelper.convertToMarkHistory(bean, summary.getStartMemberId(), null);
						newhistoryList.add(newhistory);
					}
				}
			}
			if(hasDocMark && !hasDocMarkHistory && !hasDocMarkDH) {
				if(summary.getEdocType()!=1 && Strings.isNotBlank(docMark)) {
					EdocMarkHistory history = new EdocMarkHistory();
	    			history.setNewId();
	    			history.setMarkType(0);//公文文号
	    			history.setMarkNum(0);
	    			history.setRealUsed(1);
	    			history.setSubject(summary.getSubject());
	    			history.setCreateTime(currentDate);
	    			history.setCompleteTime(currentDate);
	    			history.setEdocId(summaryId);
	    			history.setDomainId(summary.getOrgAccountId());
	    			history.setSubject(summary.getSubject());
	    			history.setGovdocType(summary.getGovdocType());
	    			history.setTransferStatus(summary.getTransferStatus());
	    			history.setCreateUserId(summary.getStartUserId());
	    			history.setLastUserId(summary.getStartUserId());
	    			history.setDocMark(summary.getDocMark());
	    			history.setSelectType(GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal());
	    			historyList.add(history);
	        	}
			}
			
			//收文的公文文号不占号
			if(hasDocMark2 && !hasDocMark2History && !hasDocMark2DH) {
				if(summary.getEdocType()!=1 && Strings.isNotBlank(docMark2)) {
					EdocMarkHistory history = new EdocMarkHistory();
	    			history.setNewId();
	    			history.setMarkType(0);//公文文号2
	    			history.setMarkNum(1);
	    			history.setRealUsed(1);
	    			history.setSubject(summary.getSubject());
	    			history.setCreateTime(currentDate);
	    			history.setCompleteTime(currentDate);
	    			history.setEdocId(summaryId);
	    			history.setDomainId(summary.getOrgAccountId());
	    			history.setSubject(summary.getSubject());
	    			history.setGovdocType(summary.getGovdocType());
	    			history.setTransferStatus(summary.getTransferStatus());
	    			history.setCreateUserId(summary.getStartUserId());
	    			history.setLastUserId(summary.getStartUserId());
	    			history.setDocMark(summary.getDocMark2());
	    			history.setSelectType(GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal());
	    			historyList.add(history);
	        	}
			}
			
			if(hasSerialNo && !hasSerialNoHistory && !hasSerialNoDH) {
				if(Strings.isNotBlank(serialNo)) {
					EdocMarkHistory history = new EdocMarkHistory();
	    			history.setNewId();
	    			history.setMarkType(1);//内部编号
	    			history.setMarkNum(0);
	    			history.setRealUsed(1);
	    			history.setSubject(summary.getSubject());
	    			history.setCreateTime(currentDate);
	    			history.setCompleteTime(currentDate);
	    			history.setEdocId(summaryId);
	    			history.setDomainId(summary.getOrgAccountId());
	    			history.setSubject(summary.getSubject());
	    			history.setGovdocType(summary.getGovdocType());
	    			history.setTransferStatus(summary.getTransferStatus());
	    			history.setCreateUserId(summary.getStartUserId());
	    			history.setLastUserId(summary.getStartUserId());
	    			history.setDocMark(summary.getSerialNo());
	    			history.setSelectType(GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal());
	    			historyList.add(history);
	        	}
			}
		}
        
        if(Strings.isNotEmpty(newhistoryList)) {
    		edocMarkHistoryManager.save(newhistoryList);
    	}
        return true;
	}
	
	public boolean saveSignMarkOld(GovdocBaseVO newVo) throws BusinessException {
		EdocRecieveRecord oldRecord = newVo.getOldSignRecord();
		String oldSignMark = oldRecord.getRecNo();
		
		List<EdocMarkHistory> historyList = new ArrayList<EdocMarkHistory>();
		GovdocMarkVO signMarkVo = GovdocMarkHelper.parseDocMarkOld(oldSignMark);
		if(signMarkVo != null) {
			oldRecord.setRecNo(signMarkVo.getMarkstr());
			
			signMarkVo.setCurrentUser(newVo.getCurrentUser());
			if(signMarkVo.getMarkDefId() != null && signMarkVo.getMarkDef()==null) {
				signMarkVo.setMarkDef(this.getMarkDef(signMarkVo.getMarkDefId()));
        	}
			if(signMarkVo.getMarkDefId() != null && signMarkVo.getMarkDef()==null) {
				LOGGER.error("保存签收编号时文号定义对象为空 " + signMarkVo.getMarkDefId());
			}
			
			EdocMarkHistory history = new EdocMarkHistory();
    		history.setNewId();
    		history.setMarkType(2);//签收编号
    		history.setMarkNum(0);
    		history.setRealUsed(1);
    		history.setSubject(oldRecord.getSubject());
    		history.setCreateTime(newVo.getCurrentDate());
    		history.setCompleteTime(newVo.getCurrentDate());
    		history.setEdocId(oldRecord.getId());
    		if(oldRecord.getExchangeType() == 1) {//交换给单位
    			history.setDomainId(oldRecord.getExchangeOrgId());
    		} else if(oldRecord.getExchangeType() == 2) {//交换给部门
	            V3xOrgDepartment dept = orgManager.getDepartmentById(oldRecord.getExchangeOrgId());
	            if(dept!=null) {
	            	history.setDomainId(dept.getOrgAccountId());
	            }
    		}
    		history.setSubject(oldRecord.getSubject());
    		history.setGovdocType(0);
    		history.setTransferStatus(0);
    		history.setCreateUserId(oldRecord.getRecUserId());
    		history.setLastUserId(oldRecord.getRecUserId());
    		history.setDocMark(signMarkVo.getMarkstr());
    		history.setSelectType(signMarkVo.getSelectType());
    		if(signMarkVo.getSelectType() != GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal()) {//手写
    			history.setMarkDefId(signMarkVo.getMarkDefId());
    			history.setCategoryId(signMarkVo.getMarkDef().getCategoryId());
    			history.setDocMarkNo(signMarkVo.getMarkNumber());
    			history.setYearNo(Integer.parseInt(signMarkVo.getYearNo()));
    		}
    		historyList.add(history);
		} else if(Strings.isNotBlank(oldSignMark)) {
			oldRecord.setRecNo(oldSignMark);
		}
		 
		if(Strings.isNotEmpty(historyList)) {
    		edocMarkHistoryManager.save(historyList);
    	}
		 
		//内部文号跳号
        if(signMarkVo != null) {
			if(signMarkVo.getSelectType() != 1 && signMarkVo.getMarkDef()!=null) {//非手写文号
				if(signMarkVo.getMarkNumber() != null && signMarkVo.getMarkNumber().longValue()>0) {//流水号不为空
					//不按年度编号或按年度编号，年号非本年
					String yearNo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
					if(!signMarkVo.getYearEnabled() || (signMarkVo.getYearEnabled() && yearNo.equals(signMarkVo.getYearNo()))) {
						updateNextCurrentNo(signMarkVo, signMarkVo.getMarkNumber());	
					}
				}
			}
	    }
		return true;
	}
	
	public boolean saveSendMarkOld(GovdocBaseVO newVo) throws BusinessException {
		EdocSummary summary = newVo.getSummary();
		Long summaryId = summary.getId();
    	String docMark = summary.getDocMark();
    	String docMark2 = summary.getDocMark2();
    	String serialNo = summary.getSerialNo();
    	
    	List<String> markstrList = new ArrayList<String>();
    	List<EdocMark> markList = new ArrayList<EdocMark>();
    	List<EdocMarkHistory> historyList = new ArrayList<EdocMarkHistory>();
    	
    	boolean isUnbind = false;
    	
    	//第一个公文文号
        GovdocMarkVO docMarkVo = GovdocMarkHelper.parseDocMarkOld(docMark);
        if(docMarkVo != null) {
        	summary.setDocMark(docMarkVo.getMarkstr());
        	markstrList.add(docMarkVo.getMarkstr());
        	if(!newVo.isNewBusiness()) {
        		isUnbind = true;
        	}
        	docMarkVo.setCurrentUser(newVo.getCurrentUser());
        	if(docMarkVo.getMarkDefId() != null && docMarkVo.getMarkDef()==null) {
        		docMarkVo.setMarkDef(this.getMarkDef(docMarkVo.getMarkDefId()));
        	}
        	if(docMarkVo.getMarkDefId() != null && docMarkVo.getMarkDef()==null) {
        		LOGGER.error("发送公文时保存公文文号获取文号定义对象为空 " + docMarkVo.getMarkDefId());
        	}
        	
        	if(newVo.getIsQuickSend() || summary.getEdocType()==1 || summary.getEdocType()==2) {
        		EdocMarkHistory history = new EdocMarkHistory();
        		history.setNewId();
        		history.setMarkType(0);//公文文号
        		history.setMarkNum(0);
        		history.setRealUsed(1);
        		history.setCreateTime(newVo.getCurrentDate());
        		history.setCompleteTime(newVo.getCurrentDate());
        		history.setEdocId(summary.getId());
        		history.setDomainId(summary.getOrgAccountId());
        		history.setSubject(summary.getSubject());
        		history.setGovdocType(summary.getGovdocType());
        		history.setTransferStatus(summary.getTransferStatus());
        		history.setCreateUserId(summary.getStartMemberId());
        		history.setLastUserId(summary.getStartMemberId());
        		history.setDocMark(docMarkVo.getMarkstr());
        		history.setSelectType(docMarkVo.getSelectType());
        		if(docMarkVo.getSelectType() != GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal()) {//非手写
        			if(docMarkVo.getMarkDef() != null) {
            			history.setCategoryId(docMarkVo.getMarkDef().getCategoryId());
    	    		}
            		history.setMarkDefId(docMarkVo.getMarkDefId());
            		history.setDocMarkNo(docMarkVo.getMarkNumber());
            		history.setYearNo(Integer.parseInt(docMarkVo.getYearNo()));	
        		}
        		historyList.add(history);
        	} else {
        		EdocMark newMark = new EdocMark();
        		newMark.setNewId();
        		newMark.setMarkType(0);//公文文号
        		newMark.setMarkNum(0);
        		newMark.setRealUsed(1);
        		newMark.setCreateTime(newVo.getCurrentDate());
        		newMark.setEdocId(summary.getId());
        		newMark.setDomainId(summary.getOrgAccountId());
        		newMark.setSubject(summary.getSubject());
        		newMark.setGovdocType(summary.getGovdocType());
        		newMark.setCreateUserId(summary.getStartMemberId());
        		newMark.setDocMark(docMarkVo.getMarkstr());
        		newMark.setSelectType(docMarkVo.getSelectType());
        		if(docMarkVo.getSelectType() != GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal()) {//非手写
        			if(docMarkVo.getMarkDef() != null) {
            			newMark.setCategoryId(docMarkVo.getMarkDef().getCategoryId());
    	    		}
        			newMark.setMarkDefId(docMarkVo.getMarkDefId());
            		newMark.setDocMarkNo(docMarkVo.getMarkNumber());
            		newMark.setYearNo(Integer.parseInt(docMarkVo.getYearNo()));
        		}
        		markList.add(newMark);
        	}
        } else if(Strings.isNotBlank(docMark)) {
        	summary.setDocMark(docMark);
        	if(summary.getEdocType() != 1) {//收文时不对公文文号做断号占号处理
        		markstrList.add(docMark);
        	}
        }
        
        //第二个公文文号
        GovdocMarkVO docMark2Vo = GovdocMarkHelper.parseDocMarkOld(docMark2);
        if(docMark2Vo != null) {
        	summary.setDocMark2(docMark2Vo.getMarkstr());
        	markstrList.add(docMark2Vo.getMarkstr());
        	if(!newVo.isNewBusiness()) {
        		isUnbind = true;
        	}
        	docMark2Vo.setCurrentUser(newVo.getCurrentUser());
        	if(docMark2Vo.getMarkDefId() != null && docMark2Vo.getMarkDef()==null) {
        		docMark2Vo.setMarkDef(this.getMarkDef(docMark2Vo.getMarkDefId()));
        	}
        	if(docMark2Vo.getMarkDefId() != null && docMark2Vo.getMarkDef()==null) {
        		LOGGER.error("发送公文时保存公文文号2获取文号定义对象为空 " + docMark2Vo.getMarkDefId());
        	}
        	
        	if(newVo.getIsQuickSend() || summary.getEdocType()==1 || summary.getEdocType()==2) {
        		EdocMarkHistory history = new EdocMarkHistory();
        		history.setNewId();
        		history.setMarkType(0);
        		history.setMarkNum(1);
        		history.setRealUsed(1);
        		history.setSubject(summary.getSubject());
        		history.setCreateTime(newVo.getCurrentDate());
        		history.setCompleteTime(newVo.getCurrentDate());
        		history.setEdocId(summary.getId());
        		history.setDomainId(summary.getOrgAccountId());
        		history.setSubject(summary.getSubject());
        		history.setGovdocType(summary.getGovdocType());
        		history.setTransferStatus(summary.getTransferStatus());
        		history.setCreateUserId(summary.getStartMemberId());
        		history.setLastUserId(summary.getStartMemberId());
        		history.setDocMark(docMark2Vo.getMarkstr());
        		history.setSelectType(docMark2Vo.getSelectType());
        		if(docMark2Vo.getSelectType() != GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal()) {//非手写
        			if(docMark2Vo.getMarkDef() != null) {
            			history.setCategoryId(docMark2Vo.getMarkDef().getCategoryId());
    	    		}
        			history.setMarkDefId(docMark2Vo.getMarkDefId());
            		history.setDocMarkNo(docMark2Vo.getMarkNumber());
            		history.setYearNo(Integer.parseInt(docMark2Vo.getYearNo()));
        		}
        		historyList.add(history);
        	} else {
        		EdocMark newMark = new EdocMark();
        		newMark.setNewId();
        		newMark.setMarkType(0);
        		newMark.setMarkNum(1);
        		newMark.setRealUsed(1);
        		newMark.setCreateTime(newVo.getCurrentDate());
        		newMark.setEdocId(summary.getId());
        		newMark.setDomainId(summary.getOrgAccountId());
        		newMark.setSubject(summary.getSubject());
        		newMark.setGovdocType(summary.getGovdocType());
        		newMark.setCreateUserId(summary.getStartMemberId());
        		newMark.setDocMark(docMark2Vo.getMarkstr());
        		newMark.setSelectType(docMark2Vo.getSelectType());
        		if(docMark2Vo.getSelectType() != GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal()) {//非手写
        			if(docMark2Vo.getMarkDef() != null) {
	        			newMark.setCategoryId(docMark2Vo.getMarkDef().getCategoryId());
		    		}
        			newMark.setMarkDefId(docMark2Vo.getMarkDefId());
	        		newMark.setDocMarkNo(docMark2Vo.getMarkNumber());
	        		newMark.setYearNo(Integer.parseInt(docMark2Vo.getYearNo()));
        		}
        		markList.add(newMark);
        	}
        } else if(Strings.isNotBlank(docMark2)) {
        	summary.setDocMark2(docMark2);
        	if(summary.getEdocType() != 1) {//收文时不对公文文号做断号占号处理
        		markstrList.add(docMark2);
        	}
        }
        
        //内部文号
        GovdocMarkVO serialNoVo = GovdocMarkHelper.parseDocMarkOld(serialNo);
        if(serialNoVo != null) {
        	summary.setSerialNo(serialNoVo.getMarkstr());
        	markstrList.add(serialNoVo.getMarkstr());
        	if(!newVo.isNewBusiness()) {
        		isUnbind = true;
        	}
        	serialNoVo.setCurrentUser(newVo.getCurrentUser());
        	if(serialNoVo.getMarkDefId() != null && serialNoVo.getMarkDef()==null) {
        		serialNoVo.setMarkDef(this.getMarkDef(serialNoVo.getMarkDefId()));
        	}
        	if(serialNoVo.getMarkDefId() != null && serialNoVo.getMarkDef()==null) {
        		LOGGER.error("发送公文时保存内部文号获取文号定义对象为空 " + serialNoVo.getMarkDefId());
        	}
        	
        	//快速发文/收文/签报，文号直接进占号
        	if(newVo.getIsQuickSend() || summary.getEdocType()==1 || summary.getEdocType()==2) {
        		EdocMarkHistory history = new EdocMarkHistory();
        		history.setNewId();
        		history.setMarkType(1);//内部文号
        		history.setMarkNum(0);
        		history.setRealUsed(1);
        		history.setSubject(summary.getSubject());
        		history.setCreateTime(newVo.getCurrentDate());
        		history.setCompleteTime(newVo.getCurrentDate());
        		history.setEdocId(summary.getId());
        		history.setDomainId(summary.getOrgAccountId());
        		history.setSubject(summary.getSubject());
        		history.setGovdocType(summary.getGovdocType());
        		history.setTransferStatus(summary.getTransferStatus());
        		history.setCreateUserId(summary.getStartMemberId());
        		history.setLastUserId(summary.getStartMemberId());
        		history.setDocMark(serialNoVo.getMarkstr());
        		history.setSelectType(serialNoVo.getSelectType());
        		if(serialNoVo.getSelectType() != GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal()) {//非手写
        			if(serialNoVo.getMarkDef() != null) {
            			history.setCategoryId(serialNoVo.getMarkDef().getCategoryId());
            			history.setMarkDefId(serialNoVo.getMarkDefId());
                		history.setDocMarkNo(serialNoVo.getMarkNumber());
                		history.setYearNo(Integer.parseInt(serialNoVo.getYearNo()));
    	    		} else {//若文号为空，则重置设置为手写文号
    	    			history.setSelectType(GovdocMarkEnum.SelectTypeEnum.shouxie.ordinal());
    	    		}
        		}
        		historyList.add(history);
        	}
        } else if(Strings.isNotBlank(serialNo)) {
        	summary.setSerialNo(serialNo);
        	markstrList.add(serialNo);
        }
       
    	if(Strings.isNotEmpty(markstrList)) {
    		edocMarkManager.deleteEdocMarkByMarkstr(markstrList);
    	}
    	if(isUnbind) {
    		govdocMarkDao.unbindMark(summaryId);
    	}
    	//老公文发送-若老公文登记时文号进了占号
    	if(summary.getEdocType() == 1 && newVo.getRegister() != null && Strings.isNotBlank(newVo.getRegister().getSerialNo())) {
    		if(!(newVo.getRegister().getSerialNo().equals(summary.getSerialNo()))) {//若老公文收登记与分发收文编号不一致，则释放原登记文的文号
	    		List<EdocMarkHistory> oldhistoryList = edocMarkHistoryManager.getMarkHistorysByEdocID(newVo.getRegister().getId());
	    		if(Strings.isNotEmpty(oldhistoryList)) {
	    			for(EdocMarkHistory oldhistory : oldhistoryList) {
	    				EdocMark newMark = GovdocMarkHelper.convertToMark(oldhistory);
						if(newMark.getDomainId() == null) {
							newMark.setDomainId(newVo.getCurrentUser().getLoginAccount());
						}
	    				markList.add(newMark);
	    			}
	    			edocMarkHistoryManager.deleteMarkHistoryByEdocId(newVo.getRegister().getId());
	    		}
    		} else {
    			edocMarkHistoryManager.deleteMarkHistoryByEdocId(newVo.getRegister().getId());
    		}
    	}
    	if(Strings.isNotEmpty(markList)) {
    		edocMarkManager.save(markList);
    	}
    	if(Strings.isNotEmpty(historyList)) {
     		edocMarkHistoryManager.save(historyList);
     	}
    	
    	//文号跳号
        if(docMarkVo != null) {
			if(docMarkVo.getSelectType() != 1 && docMarkVo.getMarkDef()!=null) {//非手写文号
				if(docMarkVo.getMarkNumber() != null && docMarkVo.getMarkNumber().longValue()>0) {//流水号不为空
					//不按年度编号或按年度编号，年号非本年
					String yearNo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
					if(!docMarkVo.getYearEnabled() || (docMarkVo.getYearEnabled() && yearNo.equals(docMarkVo.getYearNo()))) {
						updateNextCurrentNo(docMarkVo, docMarkVo.getMarkNumber());	
					}
				}
			}
        }
        
        //文号2跳号
        if(docMark2Vo != null) {
			if(docMark2Vo.getSelectType() != 1 && docMark2Vo.getMarkDef()!=null) {//非手写文号
				if(docMark2Vo.getMarkNumber() != null && docMark2Vo.getMarkNumber().longValue()>0) {//流水号不为空
					//不按年度编号或按年度编号，年号非本年
					String yearNo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
					if(!docMark2Vo.getYearEnabled() || (docMark2Vo.getYearEnabled() && yearNo.equals(docMark2Vo.getYearNo()))) {
						updateNextCurrentNo(docMark2Vo, docMark2Vo.getMarkNumber());	
					}
				}
			}
        }
        
        //内部文号跳号
        if(serialNoVo != null) {
			if(serialNoVo.getSelectType() != 1 && serialNoVo.getMarkDef()!=null) {//非手写文号
				if(serialNoVo.getMarkNumber() != null && serialNoVo.getMarkNumber().longValue()>0) {//流水号不为空
					//不按年度编号或按年度编号，年号非本年
					String yearNo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
					if(!serialNoVo.getYearEnabled() || (serialNoVo.getYearEnabled() && yearNo.equals(serialNoVo.getYearNo()))) {
						updateNextCurrentNo(serialNoVo, serialNoVo.getMarkNumber());	
					}
				}
			}
	    }
		return true;
	}
	
	@Override
	public boolean saveCancelMarkOld(GovdocBaseVO newVo) throws BusinessException {
		Long domainId = newVo.getCurrentUser()==null ? newVo.getSummary().getOrgAccountId() : newVo.getCurrentUser().getLoginAccount();
		Long summaryId = newVo.getSummaryId() == null ? newVo.getSummary().getId() : null;
		
		List<EdocMark> newMarkList = new ArrayList<EdocMark>();
		List<EdocMarkHistory> oldhistoryList = edocMarkHistoryManager.getMarkHistorysByEdocID(summaryId);
		if(Strings.isNotEmpty(oldhistoryList)) {//若当前公文之前使用的文号已经占用过了
			//若当前公文之前使用的文号已经占用过了，文号做过修改，则要将原占用的文号进入断号
			for(EdocMarkHistory bean : oldhistoryList) {
				//因老公文很多占号升级上来为手写输入状态，所以这里暂不判断手工输入进断号
				//if(bean.getSelectType().intValue() != SelectTypeEnum.shouxie.ordinal()) {//非手工输入才生成断号
					if(bean.getDocMarkNo() != null) {
						EdocMark newMark = GovdocMarkHelper.convertToMark(bean);
						newMark.setEdocId(-1L);
						if(newMark.getDomainId() == null) {
							newMark.setDomainId(domainId);
						}
						newMarkList.add(newMark);
					}
				//}
			}
		}
		//删除手工文号断号
		govdocMarkDao.deleteMarkHandinput(newVo.getSummaryId());
		//释放掉断号
		govdocMarkDao.unbindMark(newVo.getSummaryId());
		//删除原有占号
		edocMarkHistoryManager.deleteMarkHistoryByEdocId(summaryId);
		//由占号变成断号
		if(Strings.isNotEmpty(newMarkList)) {
			edocMarkManager.save(newMarkList);
		}
		return false;
	}
	
	/**
	 * 模式1：发起提交时不占用文号，其它文可使用
	 * 启用分送后占号
	 * @param markVo
	 * @return
	 * @throws BusinessException
	 */
	private boolean saveMark(GovdocMarkVO markVo) throws BusinessException {
		if(markVo == null || !markVo.getIsEnable()) {
			return false;
		}
		
		//触发的公文，不保存公文断号或占号，只保存文号记录
		if(!markVo.isFromTrigger()) {
			EdocMarkDefinition markDef = null;
			if(markVo.getMarkDefId() != null) {
				markDef = this.getMarkDef(markVo.getMarkDefId());
			}
			if(markDef != null) {
				if(markDef.getEdocMarkCategory() == null) {
					EdocMarkCategory category = null;
					if(markDef.getCategoryId() != null) {
						category = govdocMarkDao.getEdocMarkCategory(markDef.getCategoryId());
					}
					if(category == null && markVo.getCategoryId()!=null) {
						category = govdocMarkDao.getEdocMarkCategory(markVo.getCategoryId());
					}
					markDef.setEdocMarkCategory(category);
				}
				markVo.setMarkDef(markDef);
				if(markVo.getMarkDefId() == null) {
					markVo.setMarkDefId(markDef.getId());
				}
				markVo.setCategoryId(markDef.getCategoryId());
				markVo.setCategoryCodeMode(markDef.getEdocMarkCategory().getCodeMode());
				markVo.setCurrentNo(markDef.getEdocMarkCategory().getCurrentNo());
			}
			if(Strings.isBlank(markVo.getYearNo())) {
				markVo.setYearNo(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
			}
			String markType = String.valueOf(markVo.getMarkType());
			String usedType = govdocMarkOpenManager.getSendUsedType(markType, markVo.isFawen());
			markVo.setUsedType(Integer.parseInt(usedType));
			
			if(markVo.isFensong() || markVo.isNeedFinish() || "fensong".equals(markVo.getDealAction())) {//分送/快速发文/流程结束
				if("deal".equals(markVo.getAction())) {
					markVo.setFlowState(flowState.finish.ordinal());
				}
			} else if(markVo.isFromDraft()) {//保存待发
				markVo.setFlowState(flowState.cancel.ordinal());
			} else {
				markVo.setFlowState(flowState.run.ordinal());
			}
			
			//模式1：发起提交时不占用文号，其它文可使用-启用分送后占号
			if(markVo.isFensong() || "fensong".equals(markVo.getDealAction())) {//分送/快速发文
				markVo.setUsedState(UsedStateEnum.used.ordinal());
				saveEdocMarkHistory(markVo);
			} else if(markVo.isFromDraft()) {//保存待发
				markVo.setUsedState(UsedStateEnum.unused.ordinal());
				saveEdocMark(markVo);
			} else if(markVo.isFromSend()) {//普通发送
				if("2".equals(usedType)) {//模式2：发起提交时占用文号，其它文不能再使用
					markVo.setUsedState(UsedStateEnum.used.ordinal());
					saveEdocMarkHistory(markVo);
				} else {
					markVo.setUsedState(UsedStateEnum.unused.ordinal());
					saveEdocMark(markVo);
				}
			} else if(markVo.isFromDeal()) {//处理
				if("2".equals(usedType)) {//模式2：发起提交时占用文号，其它文不能再使用
					markVo.setUsedState(UsedStateEnum.used.ordinal());
					saveEdocMarkHistory(markVo);
				} else {
					if(markVo.isNeedFinish()) {//本次处理后，流程结束
						//模式1：发起提交时不占用文号，其它文可使用-启用流程结束占号
						boolean isUsedByFs_Finish = govdocMarkOpenManager.isUsedByFensong_Finish(String.valueOf(markVo.getMarkType()), markVo.isFawen());
						if(isUsedByFs_Finish) {
							markVo.setUsedState(UsedStateEnum.used.ordinal());
							saveEdocMarkHistory(markVo);
						} else {	
							markVo.setUsedState(UsedStateEnum.unused.ordinal());
							saveEdocMark(markVo);
						}
					} else {//本次处理后，流程不结束
						markVo.setUsedState(UsedStateEnum.unused.ordinal());
						saveEdocMark(markVo);
					}
				}
			}
		}
		
		saveMarkRecord(markVo);
		
		return true;
	}
	/**
	 * 保存公文断号
	 * @param markVo
	 * @throws BusinessException
	 */
	@SuppressWarnings("unused")
	private void saveEdocMark(GovdocMarkVO markVo) throws BusinessException {
		Long userId = markVo.getCurrentUser().getId();
		Long summaryId = markVo.getSummaryId();
		Long markDefId = markVo.getMarkDefId();
		int selectType = markVo.getSelectType().intValue();
		
		boolean isNew = true;
		boolean isUpdateMarkInput = false;//公文文号是否有改动(文号修改/选择文号类型修改/文号使用模式修改)
		boolean isUpdateMarkstr = false;//公文文号内容是否改动
		boolean needAdd = true;
		
		if(markVo.isChildFlow()) {//若为子流程，则手工设置为主流程，当成主流程去处理
			if(markVo.getParentSummaryId() == null) {
				LOGGER.info("saveEdocMark 该子流程未与父流程绑定，不作断号处理！summaryId = " + summaryId);
				return;
			}
			summaryId = markVo.getParentSummaryId();
		}
		GovdocMarkRecord record = govdocMarkDao.getMarkRecord(summaryId, markVo.getMarkType());
		//兼容V57版公文子流程与父流程未绑定关系
		if(markVo.isChildFlow() && markVo.getParentSummaryId() == null) {
			GovdocMarkRecord parentRecord = govdocMarkDao.getParentMarkRecord(record.getFormDataId(), record.getMarkType());
			if(parentRecord != null && parentRecord.getSummaryId()!=null) {
				markVo.setParentSummaryId(parentRecord.getSummaryId());
				summaryId = markVo.getParentSummaryId();
				record = parentRecord;
			}
		}
		markVo.setRecord(record);
		
		if(markVo.isChildFlow()) {//若为子流程，则手工设置为主流程，当成主流程去处理
			markVo.setSummaryId(summaryId);
			markVo.setNewflowType(EdocConstant.NewflowType.main.ordinal());
			markVo.setChildSummaryId(record.getChildSummaryId());
		}
		
		if(record != null) {
			isNew = false;
			//公文文号内容变更
			if(!markVo.getMarkstr().equals(record.getMarkstr())) {
				isUpdateMarkInput = true;
				isUpdateMarkstr = true;
				//若上一次公文文号选择的是电子文号，本次的是手工文号，则不删除断号
				if(record.getSelectType().intValue()!=1 && record.getSelectType().intValue()==1) {
					isUpdateMarkstr = false;
				}
			}
			//公文文号选择类型变更
			if(!isUpdateMarkInput && record.getSelectType().intValue()!=markVo.getSelectType().intValue()) {
				isUpdateMarkInput = true;
			}
			//公文文号使用模式变更
			if(!isUpdateMarkInput && record.getUsedType().intValue()!=markVo.getUsedType().intValue()) {
				isUpdateMarkInput = true;
			}
			//公文文号使用状态变更
			if(!isUpdateMarkInput && record.getUsedState().intValue()!=markVo.getUsedState().intValue()) {
				isUpdateMarkInput = true;
			}
			//公文文号流程状态变更
			if(!isUpdateMarkInput && record.getFlowState().intValue()!=markVo.getFlowState().intValue()) {
				isUpdateMarkInput = true;
			}
		}
		
		List<Long> newmarkDefIdList = new ArrayList<Long>();
		List<String> newmarkstrList = new ArrayList<String>();
		List<EdocMark> newmarkList = new ArrayList<EdocMark>();
		if(isNew || isUpdateMarkInput) {//新建或修改过文号
			//文号为空时，不生成断号
			if(Strings.isBlank(markVo.getMarkstr())) {
				needAdd = false;
			}
			if(markVo.getMarkDef() == null) {//手工输入文号
				if(needAdd) {
					EdocMark newMark = GovdocMarkHelper.convertToMark(markVo);
					newmarkList.add(newMark);
					newmarkstrList.add(markVo.getMarkstr());
				}
			} else {
				//非手工文号没有流水号时，不生成断号
				boolean numberIsNull = markVo.getMarkNumber()==null || (markVo.getMarkNumber()!=null && markVo.getMarkNumber().longValue()<=0);
				if(markVo.getSelectType()!=1 && numberIsNull) {
					needAdd = false;
				}
				if(needAdd) {
					if(markVo.getCategoryCodeMode().intValue() == 0) {//小流水
						EdocMark newMark = GovdocMarkHelper.convertToMark(markVo);
						newmarkList.add(newMark);
						newmarkstrList.add(markVo.getMarkstr());
						if(newMark.getMarkDefId() != null) {
							newmarkDefIdList.add(newMark.getMarkDefId());
						}
						markVo.setCallId(newMark.getId());
					} else if(markVo.getCategoryCodeMode().intValue() == 1) {//大流水
						List<EdocMarkDefinition> mds = this.getMarkDefsByCategory(markVo.getCategoryId());
						for(EdocMarkDefinition bean : mds) {
							GovdocMarkVO newVo = govdocMarkParseManager.markDef2Mode(bean, null, markVo.getMarkNumber());
							newVo.setSelectType(markVo.getSelectType());
							newVo.setMarkDefId(bean.getId());
							newVo.setCategoryId(bean.getCategoryId());
							newVo.setMarkNumber(markVo.getMarkNumber());
							newVo.setGovdocType(markVo.getGovdocType());
							newVo.setSummaryId(markVo.getSummaryId());
							newVo.setSubject(markVo.getSubject());
							newVo.setDomainId(markVo.getDomainId());
							newVo.setCurrentUser(markVo.getCurrentUser());
							newVo.setYearNo(markVo.getYearNo());
							EdocMark newMark = GovdocMarkHelper.convertToMark(newVo);
							if(!markVo.getMarkDef().getEdocMarkCategory().getYearEnabled()) {
								newMark.setYearNo(null);
							}
							if(bean.getId().longValue() != markVo.getMarkDefId().longValue()) {
								newMark.setRealUsed(0);
								newMark.setSubject("");//大流水实际断号才有标题，同大流水的其它断号无标题
							} else {
								markVo.setCallId(newMark.getId());
							}
							newmarkList.add(newMark);
							newmarkstrList.add(newMark.getDocMark());
							if(newMark.getMarkDefId() != null) {
								newmarkDefIdList.add(newMark.getMarkDefId());
							}
						}
					}
				} else if(isUpdateMarkInput) {//本次不需要添加公文文号，则将断号删除
					if(markVo.getCategoryCodeMode().intValue() == 0) {//小流水
						newmarkstrList.add(markVo.getMarkstr());
					} else if(markVo.getCategoryCodeMode().intValue() == 1) {//大流水
						List<EdocMarkDefinition> mds = this.getMarkDefsByCategory(markVo.getCategoryId());
						for(EdocMarkDefinition bean : mds) {
							GovdocMarkVO newVo = govdocMarkParseManager.markDef2Mode(bean, null, markVo.getMarkNumber());
							newmarkstrList.add(newVo.getMarkstr());
						}
					}
				}
			}
		}
		
		if(!isNew) {//编辑文号
			if(isUpdateMarkInput) {//表示公文编辑、处理时，公文文号有改动过
				if(!isUpdateMarkstr) {//公文文号控件改动过，但内容未改过，将上次的文号删除
					edocMarkManager.deleteEdocMarkByEdocId(summaryId);
				}
				//用于切换文号占用后，开关再次改成模式1
				edocMarkHistoryManager.deleteMarkHistoryByEdocId(summaryId);
				//若本次公文修改了文号，则上一次的文号若非手工输入则与当前公文解绑，并进入断号
				govdocMarkDao.deleteMarkHandinput(markVo.getSummaryId(), markVo.getMarkType());
				//将当前公文与之前的文号解绑
				govdocMarkDao.unbindMark(markVo.getSummaryId(), markVo.getMarkType());
			}
		}
		//保存当前公文本次断号
		if(Strings.isNotEmpty(newmarkList)) {
			//将同文号及序号的其它断号，修改最后修改状态为：否
			govdocMarkDao.updateMarkIsLast(newmarkstrList, markVo.getMarkType(), 0);
			//删除占号
			govdocMarkDao.deleteMarkHistoryByMarkstr(newmarkstrList, markVo.getMarkType());
			//edocMarkManager.save(newMarkList);
			markVo.setMarkList(newmarkList);
		}
		//占号后，将预留文号占用状态修改为占用
		if(Strings.isNotEmpty(newmarkDefIdList) && Strings.isNotEmpty(newmarkstrList)) {
			edocMarkReserveManager.updateMarkReserveIsUsedNew(newmarkDefIdList, newmarkstrList, true);
		}
		//文号跳号
		if(selectType != 1 && markVo.getMarkDef()!=null) {//非手写文号
			if(markVo.getMarkNumber() != null && markVo.getMarkNumber().longValue()>0) {//流水号不为空
				//不按年度编号或按年度编号，年号非本年
				String yearNo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
				if(!markVo.getYearEnabled() || (markVo.getYearEnabled() && yearNo.equals(markVo.getYearNo()))) {
					updateNextCurrentNo(markVo, markVo.getMarkNumber());	
				}
			}
		}
	}
	/**
	 * 保存公文占号
	 * @param markVo
	 * @throws BusinessException
	 */
	private void saveEdocMarkHistory(GovdocMarkVO markVo) throws BusinessException {
		Long userId = markVo.getCurrentUser().getId();
		Long summaryId = markVo.getSummaryId();
		int selectType = markVo.getSelectType().intValue();
		
		boolean isNew = true;
		boolean isUpdateMarkInput = false;//公文文号是否有改动(文号修改/选择文号类型修改/文号使用模式修改)
		boolean needAdd = true;
		
		List<String> oldmarkstrList = new ArrayList<String>();
		List<String> newmarkstrList = new ArrayList<String>();
		List<Long> newmarkDefIdList = new ArrayList<Long>();
		
		List<EdocMark> newMarkList = new ArrayList<EdocMark>();
		List<EdocMarkHistory> newHistoryList = new ArrayList<EdocMarkHistory>();
		
		List<EdocMark> oldmarkList = null;
		List<EdocMarkHistory> oldhistoryList = null;
		
		GovdocMarkRecord record = null;
		if(markVo.getRecord() != null) {
			record = markVo.getRecord();
			if(markVo.isChildFlow() && record.getParentSummaryId()==null) {//子流程获取主流程的
				LOGGER.info("1 saveEdocMarkHistory 该子流程未与父流程绑定，不作占号处理！summaryId = " + summaryId);
				return;
			}
		} else {
			if(markVo.isChildFlow()) {//子流程获取主流程的
				 if(markVo.getParentSummaryId() == null) {//子流程获取主流程的
					 LOGGER.info("2 saveEdocMarkHistory 该子流程未与父流程绑定，不作占号处理！summaryId = " + summaryId);
					return;
				}
				summaryId = markVo.getParentSummaryId();
				markVo.setSummaryId(summaryId);
			}
			record = govdocMarkDao.getMarkRecord(summaryId, markVo.getMarkType());
		}
		//兼容V57版公文子流程与父流程未绑定关系
		if(markVo.isChildFlow() && markVo.getParentSummaryId() == null) {
			GovdocMarkRecord parentRecord = govdocMarkDao.getParentMarkRecord(record.getFormDataId(), record.getMarkType());
			if(parentRecord != null && parentRecord.getSummaryId()!=null) {
				markVo.setParentSummaryId(parentRecord.getSummaryId());
				summaryId = markVo.getParentSummaryId();
				record = parentRecord;
			}
		}
		markVo.setRecord(record);
		
		if(record != null) {
			isNew = false;
			//公文文号内容变更
			if(!markVo.getMarkstr().equals(record.getMarkstr())) {
				isUpdateMarkInput = true;
			}
			//公文文号选择类型变更
			if(!isUpdateMarkInput && record.getSelectType().intValue()!=markVo.getSelectType().intValue()) {
				isUpdateMarkInput = true;
			}
			//公文文号使用模式变更
			if(!isUpdateMarkInput && record.getUsedType().intValue()!=markVo.getUsedType().intValue()) {
				isUpdateMarkInput = true;
			}
			//公文文号使用状态变更
			if(!isUpdateMarkInput && record.getUsedState().intValue()!=markVo.getUsedState().intValue()) {
				isUpdateMarkInput = true;
			}
			//公文文号流程状态变更
			if(!isUpdateMarkInput && record.getFlowState().intValue()!=markVo.getFlowState().intValue()) {
				isUpdateMarkInput = true;
			}
			
			oldmarkList = edocMarkHistoryManager.findMarkBySummaryId(summaryId, markVo.getMarkType());
			if(isUpdateMarkInput) {
				if(markVo.isNeedQueryHistory()) {//需要查询文号占用数据
					oldhistoryList = edocMarkHistoryManager.getMarkHistorysByEdocID(summaryId, markVo.getMarkType());
				}
			}
		}
		
		if(Strings.isNotEmpty(oldhistoryList)) {//若文号修改过，并且当前公文之前使用的文号已经占用过了
			if(markVo.getSelectType().intValue() != 1) {//非手工输入才生成断号
				//若当前公文之前使用的文号已经占用过了，文号做过修改，则要将原占用的文号进入断号
				for(EdocMarkHistory bean : oldhistoryList) {
					if(bean.getDocMarkNo() != null && markVo.getCurrentNo()!=null && bean.getDocMarkNo().intValue()<=markVo.getCurrentNo()) {
						EdocMark newMark = GovdocMarkHelper.convertToMark(bean);
						if(!markVo.getMarkDef().getEdocMarkCategory().getYearEnabled()) {
							newMark.setYearNo(null);
						}
						if(newMark.getDomainId() == null) {
							newMark.setDomainId(markVo.getCurrentUser().getLoginAccount());
						}
						newMarkList.add(newMark);
					}
				}
			}
		}
		else if(Strings.isNotEmpty(oldmarkList)) {//若当前公文之前使用的文号已经进入断号
			for(EdocMark bean : oldmarkList) {
				if(isUpdateMarkInput) {//若修改过，则断号删除
					oldmarkstrList.add(bean.getDocMark());
				} else {//若没修改，直接由断号进入占号
					EdocMarkHistory newHistory = GovdocMarkHelper.convertToMarkHistory(bean, userId, markVo.getMarkType());
					if(selectType == 1) {//手工输入
						newHistory.setDocMark(markVo.getMarkstr());
						newHistory.setDocMarkNo(markVo.getMarkNumber());
						newHistoryList.add(newHistory);
						newmarkstrList.add(newHistory.getDocMark());
						markVo.setCallId(newHistory.getId());
					} else {
						if(markVo.getMarkNumber() != null && markVo.getMarkNumber().longValue()>0) {
							if(markVo.getCategoryCodeMode().intValue() == 0) {//小流水
								newHistory.setDocMark(markVo.getMarkstr());
								newHistory.setDocMarkNo(markVo.getMarkNumber());
								newHistoryList.add(newHistory);
								newmarkstrList.add(newHistory.getDocMark());
								newmarkDefIdList.add(markVo.getMarkDefId());
								markVo.setCallId(newHistory.getId());
							} else {
								if(bean.getId().longValue() != markVo.getMarkDefId().longValue()) {
									newHistory.setRealUsed(0);
									newHistory.setSubject("");//大流水实际占号才有标题，同大流水的其它断号无标题
								} else {
									markVo.setCallId(newHistory.getId());
								}
								newHistoryList.add(newHistory);
								newmarkstrList.add(newHistory.getDocMark());
								newmarkDefIdList.add(newHistory.getMarkDefId());
							}
						}
					}
				}
			}
		} 
		if(isNew || isUpdateMarkInput) {//新建或修改过文号
			//文号为空时，不生成占号
			if(Strings.isBlank(markVo.getMarkstr())) {
				needAdd = false;
			}
			if(markVo.getMarkDef() == null) {//手工输入文号
				if(needAdd) {
					EdocMarkHistory newHistory = GovdocMarkHelper.convertToMarkHistory(markVo);
					markVo.setCallId(newHistory.getId());
					newHistoryList.add(newHistory);
					newmarkstrList.add(newHistory.getDocMark());
				}
			} else {
				//非手工文号没有流水号时，不生成断号
				boolean numberIsNull = markVo.getMarkNumber()==null || (markVo.getMarkNumber()!=null && markVo.getMarkNumber().longValue()<=0);
				if(markVo.getSelectType()!=1 && numberIsNull) {
					needAdd = false;
				}
				if(needAdd) {
					List<EdocMarkDefinition> mds = this.getMarkDefsByCategory(markVo.getCategoryId());
					for(EdocMarkDefinition bean : mds) {
						GovdocMarkVO markDefVo = govdocMarkParseManager.markDef2Mode(bean, markVo.getYearNo(), markVo.getMarkNumber());
						EdocMarkHistory newHistory = GovdocMarkHelper.convertToMarkHistory(markDefVo, markVo);
						if(!markVo.getMarkDef().getEdocMarkCategory().getYearEnabled()) {
							newHistory.setYearNo(null);
						}
						if(newHistory.getDomainId() == null) {
							newHistory.setDomainId(markVo.getCurrentUser().getLoginAccount());
						}
						if(bean.getId().longValue() != markVo.getMarkDefId().longValue()) {
							newHistory.setRealUsed(0);
							newHistory.setSubject("");//大流水实际占号才有标题，同大流水的其它断号无标题
						} else {
							markVo.setCallId(newHistory.getId());
						}
						newHistoryList.add(newHistory);
						newmarkstrList.add(newHistory.getDocMark());
						newmarkDefIdList.add(newHistory.getMarkDefId());
					}
				} else if(isUpdateMarkInput) {
					List<EdocMarkDefinition> mds = this.getMarkDefsByCategory(markVo.getCategoryId());
					for(EdocMarkDefinition bean : mds) {
						GovdocMarkVO markDefVo = govdocMarkParseManager.markDef2Mode(bean, markVo.getYearNo(), markVo.getMarkNumber());
						newmarkstrList.add(markDefVo.getMarkstr());
					}
				}
			}
		}
		
		
		if(!isNew) {//将当前公文之前引用的断号清除
			if(isUpdateMarkInput) {//若文号做过修改，则释放掉断号，删除掉原有占号
				//删除手工文号
				govdocMarkDao.deleteMarkHandinput(markVo.getSummaryId(), markVo.getMarkType());
				//释放掉断号
				govdocMarkDao.unbindMark(markVo.getSummaryId(), markVo.getMarkType());
				//去掉被释放的公文占号，生成新的断号
				if(Strings.isNotEmpty(newMarkList)) {
					edocMarkManager.save(newMarkList);
				}
				//删除原有占号
				edocMarkHistoryManager.deleteMarkHistoryByEdocId(summaryId, markVo.getMarkType());
			} else {//若文号未做过修改，则删除断号
				edocMarkManager.deleteEdocMarkByEdocId(markVo.getSummaryId());
			}
		}
		
		//去掉多个文号使用同一个断号的情况
		if(Strings.isNotEmpty(newmarkstrList)) {
			govdocMarkDao.deleteEdocMarkByMarkstr(newmarkstrList, markVo.getMarkType());
			//占号后，将预留文号占用状态修改为占用
			if(Strings.isNotEmpty(newmarkDefIdList)) {
				edocMarkReserveManager.updateMarkReserveIsUsedNew(newmarkDefIdList, newmarkstrList, true);
			}
		}
		//新建或修改过文号
		if(Strings.isNotEmpty(newHistoryList)) {
			//edocMarkHistoryManager.save(newHistoryList);
			markVo.setHistoryList(newHistoryList);
		}
		//文号跳号
		if(markVo.getIsIncreatement()) {
			if(selectType != 1 && markVo.getMarkDef()!=null) {//非手写文号
				if(markVo.getMarkNumber() != null && markVo.getMarkNumber().longValue()>0) {//流水号不为空
					//不按年度编号或按年度编号，年号非本年
					String yearNo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
					if(!markVo.getYearEnabled() || (markVo.getYearEnabled() && yearNo.equals(markVo.getYearNo()))) {
						updateNextCurrentNo(markVo, markVo.getMarkNumber());	
					}
				}
			}
		}
	}
	
	/**
	 * 文号预留删除时，生成断号
	 * @param user
	 * @param selectType
	 * @param markDef
	 * @param delReserveVoList
	 * @throws BusinessException
	 */
	private void saveEdocMarkForReserver(User user, Integer selectType, EdocMarkDefinition markDef, List<EdocMarkReserveNumber> addReserveNumberList, List<EdocMarkReserveVO> delReserveVoList) throws BusinessException {
		/** 添加预留文号时，将未使用的断号删除掉，若断号已使用，则无法创建预留文号 */
		if(Strings.isNotEmpty(addReserveNumberList)) {
			List<String> markstrList = new ArrayList<String>();
			for(EdocMarkReserveNumber bean : addReserveNumberList) {
				markstrList.add(bean.getDocMark());
			}
			govdocMarkDao.deleteEdocMarkByMarkstr(markstrList, markDef.getMarkType());
		}
		
		/** 删除预留文号时，未使用、未占用的自动生成断号 */
		Map<String, Integer> usedMarkMap = new HashMap<String, Integer>();
		List<EdocMark> edocMarkedList = edocMarkManager.findListByMarkDefineId(markDef.getId());
		for(EdocMark edocMark : edocMarkedList) {
			usedMarkMap.put(edocMark.getDocMark(), edocMark.getDocMarkNo());
		}
		List<String> markstrList = new ArrayList<String>();
		List<EdocMark> edocMarkList = new ArrayList<EdocMark>();
		if(Strings.isNotEmpty(delReserveVoList)) {
			for(EdocMarkReserveVO delReserveVo : delReserveVoList) {
				for(EdocMarkReserveNumber reserveNumber : delReserveVo.getReserveNumberList()) {
					if(reserveNumber.getIsUsed()) {
						continue;
					}
					if(reserveNumber.getMarkNo().intValue() > markDef.getEdocMarkCategory().getCurrentNo()) {
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
					docMark.setCategoryId(markDef.getCategoryId());
					docMark.setMarkDefId(markDef.getId());
					docMark.setCreateTime(DateUtil.currentDate());
					docMark.setCreateUserId(user.getId());
					docMark.setEdocId(-1L);
					docMark.setDocMarkNo(reserveNumber.getMarkNo());
					docMark.setYearNo(reserveNumber.getYearNo());
					docMark.setDocMark(reserveNumber.getDocMark());
					docMark.setDomainId(user.getLoginAccount());
					docMark.setEdocMarkDefinition(markDef);
					docMark.setMarkNum(1);
					docMark.setMarkType(markDef.getMarkType());
					docMark.setCategoryId(markDef.getCategoryId());
					docMark.setSelectType(selectType);
					edocMarkList.add(docMark);
					markstrList.add(docMark.getDocMark());
				}
			}
		}
		if(Strings.isNotEmpty(edocMarkList)) {
			//断号生成前，其它断号最后修改状态改为否
			govdocMarkDao.updateMarkIsLast(markstrList, markDef.getMarkType(), 0);
			//删除预留文号时，生成断号
			edocMarkManager.save(edocMarkList);
		}
	}
	
	/**
	 * 公文线下预留占号及删除
	 * @param user
	 * @param selectType
	 * @param markDef
	 * @param addReserveList
	 * @param delReservedIdList
	 * @throws BusinessException
	 */
	private void saveEdocMarkHistoryForReserver(User user, Integer selectType, EdocMarkDefinition markDef, List<EdocMarkReserveVO> addReserveList, List<EdocMarkReserveVO> delReserveVoList) throws BusinessException {
		List<Long> reserveIdList = new ArrayList<Long>(); 
		List<String> markstrList = new ArrayList<String>(); 
		List<EdocMarkHistory> newhistoryList = new ArrayList<EdocMarkHistory>();
		if(Strings.isNotEmpty(addReserveList)) {
			String yearNo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
			for(EdocMarkReserveVO addVo : addReserveList) {
				int startNo = addVo.getStartNo();
				int endNo = addVo.getEndNo();
				for(int number = startNo; number <= endNo; number++) {
					GovdocMarkVO markDefVo = govdocMarkParseManager.markDef2Mode(markDef, addVo.getYearNo(), number);
					markDefVo.setSelectType(selectType);
					markDefVo.setMarkType(markDef.getMarkType());
					markDefVo.setCategoryId(markDef.getCategoryId());
					markDefVo.setMarkDefId(markDef.getId());
					markDefVo.setCallId(addVo.getEdocMarkReserve().getId());
					markDefVo.setSummaryId(-1L);
					markDefVo.setMarkNumber(number);
					markDefVo.setYearNo(yearNo);
					markDefVo.setDomainId(user.getLoginAccount());
					markDefVo.setDescription(addVo.getDescription());
					markDefVo.setCurrentUser(user);
					EdocMarkHistory newHistory = GovdocMarkHelper.convertToMarkHistory(markDefVo);
					newhistoryList.add(newHistory);
					markstrList.add(newHistory.getDocMark());
				}
				reserveIdList.add(addVo.getEdocMarkReserve().getId());
			}
		}
		if(Strings.isNotEmpty(delReserveVoList)) {
			List<Long> delReservedIdList = new ArrayList<Long>();
			for(EdocMarkReserveVO delVo : delReserveVoList) {
				delReservedIdList.add(delVo.getEdocMarkReserve().getId());
				//删除公文预留文号时，将文号是否占用状态修改为否，以便生成断号时，将占号释放到断号
				if(Strings.isNotEmpty(delVo.getReserveNumberList())) {
					for(EdocMarkReserveNumber number : delVo.getReserveNumberList()) {
						number.setIsUsed(false);
					}
				}
			}
			if(Strings.isNotEmpty(delReservedIdList)) {
				govdocMarkDao.deleteMarkHistoryByReserve(selectType, delReservedIdList);
			}
		}
		if(Strings.isNotEmpty(markstrList)) {
			govdocMarkDao.deleteMarkHistoryByMarkstr(markstrList, markDef.getMarkType());
		}
		if(Strings.isNotEmpty(newhistoryList)) {
			edocMarkHistoryManager.save(newhistoryList);
		}
	}

	/**
	 * 保存文号使用记录
	 * @param markVo
	 * @throws BusinessException
	 */
	private void saveMarkRecord(GovdocMarkVO markVo) throws BusinessException {
		GovdocMarkRecord record = markVo.getRecord();
		if(record == null) {
			record = govdocMarkDao.getMarkRecord(markVo.getSummaryId(), markVo.getMarkType());
		}
		if(record == null) {
			record = new GovdocMarkRecord();
			record.setCreateTime(DateUtil.currentTimestamp());
			record.setCreateUserId(markVo.getCurrentUser().getId());
			record.setIdIfNew();
		}
		record.setMarkType(markVo.getMarkType());
		record.setLastTime(DateUtil.currentTimestamp());
		record.setLastUserId(markVo.getCurrentUser().getId());
		record.setSelectType(markVo.getSelectType());
		record.setCategoryId(markVo.getCategoryId());
		record.setMarkDefId(markVo.getMarkDefId());
		record.setCallId(markVo.getCallId());
		record.setMarkstr(markVo.getMarkstr());
		record.setWordNo(markVo.getWordNo());
		record.setMarkNumber(markVo.getMarkNumber());
		record.setYearNo(markVo.getYearNo());
		record.setSummaryId(markVo.getSummaryId());
		record.setDomainId(markVo.getDomainId());
		record.setFormDataId(markVo.getFormDataId());
		record.setGovdocType(markVo.getGovdocType());
		record.setUsedState(markVo.getUsedState());
		record.setUsedType(markVo.getUsedType());
		record.setFlowState(markVo.getFlowState());
		record.setNewflowType(markVo.getNewflowType());
		record.setParentSummaryId(markVo.getParentSummaryId());
		record.setChildSummaryId(markVo.getChildSummaryId());
		
		if(markVo.isFromTriggerChild()) {//触发子流程时，设置主流程的子流程id
			record.setParentSummaryId(markVo.getParentSummaryId());
			govdocMarkDao.saveMarkRecord(record);
			
			GovdocMarkRecord parentRecord = govdocMarkDao.getMarkRecord(markVo.getParentSummaryId(), markVo.getMarkType());
			String childSummarIds = Strings.isBlank(parentRecord.getChildSummaryId()) ? "" + markVo.getSummaryId() : (parentRecord.getChildSummaryId() + "," + markVo.getSummaryId());
			parentRecord.setChildSummaryId(childSummarIds);
			govdocMarkDao.update(parentRecord);
		} else if(markVo.isFromTrigger()) {//触发流程
			record.setParentSummaryId(markVo.getParentSummaryId());
			govdocMarkDao.saveMarkRecord(record);
		} else {
			if(markVo.isParentFlow()) {//主流程
				//保存当前MarkRecord
				govdocMarkDao.saveMarkRecord(record);
				//拥有子流程
				if(Strings.isNotBlank(record.getChildSummaryId())) {
					//公文文号修改后回调，同步父流程文号数据
					govdocManager.transSyncParentMark(record.getSummaryId(), record.getMarkType(), record.getMarkstr());
					//公文文号修改后回调，同步主流程文号数据
					List<GovdocMarkRecord> subRecordList = govdocMarkDao.getMarkRecordBySummaryId(record.getChildSummaryId(), record.getMarkType());
					saveChildRecord(record, subRecordList);
				} else {
					//兼容V57版公文子流程与父流程未绑定关系
					List<GovdocMarkRecord> subRecordList = govdocMarkDao.getChildMarkRecordList(record.getFormDataId(), record.getMarkType());
					if(Strings.isNotEmpty(subRecordList)) {
						saveChildRecord(record, subRecordList);
					}
				}
			} else if(markVo.isChildFlow()) {//子流程
				//保存当前子流程MarkRecord
				govdocMarkDao.saveMarkRecord(record);
				//拥有父流程
				if(markVo.getParentSummaryId() != null) {
					GovdocMarkRecord parentRecord = govdocMarkDao.getMarkRecord(markVo.getParentSummaryId(), markVo.getMarkType());
					if(parentRecord != null) {
						parentRecord.setWordNo(record.getWordNo());
						parentRecord.setMarkstr(record.getMarkstr());
						parentRecord.setMarkNumber(record.getMarkNumber());
						parentRecord.setSelectType(record.getSelectType());
						parentRecord.setCategoryId(record.getCategoryId());
						parentRecord.setMarkDefId(record.getMarkDefId());
						parentRecord.setCallId(record.getCallId());
						parentRecord.setLastTime(new Timestamp(System.currentTimeMillis()));
						govdocMarkDao.saveMarkRecord(parentRecord);
						
						//公文文号修改后回调，同步父流程文号数据
						govdocManager.transSyncParentMark(parentRecord.getSummaryId(), parentRecord.getMarkType(), parentRecord.getMarkstr());
						
						//公文文号修改后回调，同步主流程文号数据
						List<GovdocMarkRecord> subRecordList = govdocMarkDao.getMarkRecordBySummaryId(record.getChildSummaryId(), record.getMarkType());
						saveChildRecord(parentRecord, subRecordList);
					}
				}
			}
		}
	}
	
	/**
	 * 公文文号修改后回调，同步主流程文号数据
	 * @param parentRecord
	 * @throws BusinessException
	 */
	private void saveChildRecord(GovdocMarkRecord parentRecord, List<GovdocMarkRecord> subRecordList) throws BusinessException {
		if(Strings.isNotEmpty(subRecordList)) {
			List<GovdocMarkRecord> childRecordList = new ArrayList<GovdocMarkRecord>();
			for(GovdocMarkRecord subRecord : subRecordList) {
				if(subRecord == null) {
					continue;
				}
				try {
					subRecord.setSelectType(parentRecord.getSelectType());
					subRecord.setWordNo(parentRecord.getWordNo());
					subRecord.setMarkstr(parentRecord.getMarkstr());
					subRecord.setMarkNumber(parentRecord.getMarkNumber());
					subRecord.setMarkDefId(parentRecord.getMarkDefId());
					subRecord.setCategoryId(parentRecord.getCategoryId());
					subRecord.setYearNo(parentRecord.getYearNo());
					subRecord.setUsedState(parentRecord.getUsedState());
					subRecord.setUsedType(parentRecord.getUsedType());
					subRecord.setCallId(parentRecord.getCallId());
					childRecordList.add(subRecord);
					
					//公文文号修改后回调，同步子流程文号数据
					govdocManager.transSyncParentMark(subRecord.getSummaryId(), subRecord.getMarkType(), subRecord.getMarkstr());
				} catch(Exception e) {
					LOGGER.error("公文文号修改后回调，同步子流程文号数据出错", e);
					throw new BusinessException(e);
				}
			}
			govdocMarkDao.updateMarkRecord(childRecordList);
		}
	}
	
	/**
     * 文号跳号(20170721)
     * @param markDef
     * @param thisNo
     */
    private void updateNextCurrentNo(GovdocMarkVO markVo, Integer thisNo) throws BusinessException {
    	EdocMarkDefinition markDef = markVo.getMarkDef();
    	//本次使用的文号非当前文号序号，则不用自增
    	int currentNo = markDef.getEdocMarkCategory().getCurrentNo(); 
    	if(thisNo==null) {
    		return;
    	}
    	int nextNo = 0;
		boolean isMarkMax = govdocMarkOpenManager.isMarkMax(String.valueOf(markVo.getMarkType()));
		if(!isMarkMax) {//停用-文号最大值自增，按原功能自增
			if(thisNo != currentNo) {
				return;
			}
			nextNo = currentNo;//需要自增
		} else {//启用-文号按最大值自增
			if(thisNo < currentNo) {
				return;
			}
			nextNo = thisNo;//需要自增
		}
    	Long domainId = markVo.getCurrentUser().getLoginAccount();
    	int addOneNo = this.increatementCurrentNo(markDef, nextNo);
    	GovdocMarkVO nextMarkVo = govdocMarkParseManager.markDef2Mode(markDef, markVo.getYearNo(), addOneNo);
    	if(nextMarkVo == null) {
    		LOGGER.error("文号获取下一流水号为空。。。 markDefId=" + markDef.getId() + " categoryId=" + markDef.getCategoryId() + " addOneNo=" + addOneNo);
    		return;
    	}
		String markstr = nextMarkVo.getMarkstr();
		//若自增序号文号已经使用，则跳号，直到跳到本单位没有使用过的文号为止
		boolean isUsed = govdocMarkDao.checkMarkIsUsedForReserver(markDef.getMarkType(), markDef.getId(), markstr, domainId); 
		if(!isUsed) {
			isUsed = govdocMarkDao.checkMarkIsCalledForReserver(markDef.getMarkType(), markDef.getId(), markstr, domainId);
		}
    	int count = 0;
    	while(isUsed && count <1000) {
    		count++;
    		addOneNo = this.increatementCurrentNo(markDef, addOneNo);
    		nextMarkVo = govdocMarkParseManager.markDef2Mode(markDef, markVo.getYearNo(), addOneNo);
    		markstr = nextMarkVo.getMarkstr();
    		isUsed = govdocMarkDao.checkMarkIsUsedForReserver(markDef.getMarkType(), markDef.getId(), markstr, domainId);
    		if(!isUsed) {
    			isUsed = govdocMarkDao.checkMarkIsCalledForReserver(markDef.getMarkType(), markDef.getId(), markstr, domainId);
    		}
    	}
    	
    	//使用文号序号大于当前文号时
    	boolean isIncreatement = currentNo < addOneNo;
    	if(isIncreatement) {
    	    edocMarkCategoryManager.updateCategoryCurrentNo(markDef.getCategoryId(), addOneNo);
    	}
    	
    	setEdocMarkDefinitionPublished(markDef);
    	
    	if(isMarkMax && thisNo > currentNo) {//启用-文号按最大值自增，生成断号
    		saveEdocMarkByMaxNo(markVo, thisNo);
		}
    }
    
    public int increatementCurrentNo(EdocMarkDefinition markDef, int currentNo) {
 		int returnNo = currentNo;
		//因公文文号、内部文号、签收编号的大小流水都支持预留，所以这里需要放开
		Integer _currentNo = edocMarkReserveManager.autoMakeEdocMarkCurrentNo(markDef, currentNo);
		if(!_currentNo.equals(currentNo)){
			returnNo = _currentNo;
		}
		return returnNo;
	}
    
    /**
     * 文号发布状态修改为已发布/已使用(20170721)
     * @param markDef
     */
	private void setEdocMarkDefinitionPublished(EdocMarkDefinition markDef) {
		//设置已经使用。
    	if(markDef.getStatus().shortValue() == 0){
    		markDef.setStatus(Short.valueOf("1"));
    		edocMarkDefinitionManager.updateMarkDefStatus(markDef.getId(), markDef.getStatus());
    	}
	}
	
	/**
	 * 文号流水号自增开启最大号时，当前流水号至使用号之间产生断号
	 * @param markVo
	 * @param thisNo
	 */
	private void saveEdocMarkByMaxNo(GovdocMarkVO markVo, int thisNo) {
		 EdocMarkDefinition markDef = markVo.getMarkDef();
		 int currentNo = markDef.getEdocMarkCategory().getCurrentNo();
		 Long domainId = markVo.getCurrentUser().getLoginAccount();
		
		 boolean isUsed = false;
		 boolean isCalled = false;
		 int addOneNo = 0;
		 String markstr;
		 GovdocMarkVO nextMarkVo = null;
		 List<EdocMark> newmarkList = new ArrayList<EdocMark>();
		 for(int number=currentNo; number<thisNo; number++) {
			 if(number == currentNo) {
				 addOneNo = number;
			 } else {
				 addOneNo = this.increatementCurrentNo(markDef, number - 1);
				 if(addOneNo >= thisNo) {//若需要生成断号的流水号都被预留了，则直接跳出，不再循环
					 break;
				 }
				 if(addOneNo > number) {//若需要生成断号的流水号已经被预留了，则不生成断号
					 continue;
				 }
			 }
			 
			 List<EdocMarkDefinition> mds = new ArrayList<EdocMarkDefinition>();
			 if(markDef.getEdocMarkCategory().getCodeMode() == 0) {//小流水
				 mds.add(markDef);
			 } else {
				 mds = this.getMarkDefsByCategory(markDef.getCategoryId());
			 }
			 for(EdocMarkDefinition bean : mds) {
				 nextMarkVo = govdocMarkParseManager.markDef2Mode(bean, markVo.getYearNo(), addOneNo);
				 markstr = nextMarkVo.getMarkstr();
				 isUsed = govdocMarkDao.checkMarkIsUsedForReserver(markDef.getMarkType(), markDef.getId(), markstr, domainId);
				 //若需要生成断号的流水号已经被占用了，则不生成断号
				 if(isUsed) {
					 continue;
				 }
				 isCalled = govdocMarkDao.checkMarkIsCalledForReserver(markDef.getMarkType(), markDef.getId(), markstr, domainId);
				 //若需要生成断号的流水号已经被占用了，则不生成断号
				 if(isCalled) {
					 continue;
				 }
				 nextMarkVo.setMarkNumber(addOneNo);
				 nextMarkVo.setMarkDefId(bean.getId());
				 nextMarkVo.setCategoryId(bean.getCategoryId());
				 nextMarkVo.setSelectType(markVo.getSelectType());
				 nextMarkVo.setGovdocType(markVo.getGovdocType());
				 nextMarkVo.setSummaryId(markVo.getSummaryId());
				 nextMarkVo.setSubject(markVo.getSubject());
				 nextMarkVo.setDomainId(markVo.getDomainId());
				 nextMarkVo.setCurrentUser(markVo.getCurrentUser());
				 nextMarkVo.setYearNo(markVo.getYearNo());
				 EdocMark newMark = GovdocMarkHelper.convertToMark(nextMarkVo);
				 newMark.setEdocId(-1L);//公文文号按最大值自增，中间流水号生成断号时，不绑定使用文号
				 newmarkList.add(newMark);
			 }
		 }
		 if(Strings.isNotEmpty(newmarkList)) {
			 edocMarkManager.save(newmarkList);
		 }
	}
	
	/****************************** 公文文号断号/占号/跳号保存方法   end *******************************/
	
	
	/****************************** 公文文号断号/占号校验方法 start *******************************/
	@Override
	public boolean checkMarkIsCalled(String summaryId, String orgAccountId, String govdocType, String edocType, String jianbanType, String markType, String markstr) throws BusinessException {
		try { 
			LOGGER.info("验证文号是否被使用：markstr=" + markstr + " govdocType=" + govdocType + " jianbanType=" + jianbanType+ " markType=" + markType + " summaryId=" + summaryId + " orgAccountId=" + orgAccountId);
			boolean isUsed = true;
			boolean isRecDocMark = ("2".equals(govdocType) || "4".equals(govdocType)) && ("doc_mark".equals(markType)||"0".equals(markType));//收文doc_mark
			if(isRecDocMark) {//收文/签报的文号判重
				isUsed = govdocMarkDao.checkRecMarkIsCalled(markType, govdocType, markstr, summaryId, orgAccountId);
			} else {
				isUsed = govdocMarkDao.checkMarkIsCalled(markType, govdocType, markstr, summaryId, orgAccountId);
			}
			LOGGER.info("断号表中校验 isUsed=" + isUsed);
			return isUsed;
		} catch(Exception e) {
			LOGGER.error("验证公文文号是否被使用出错", e);
			return false;
		}
	}
	@Override
	public boolean checkMarkIsUsed(String summaryId, String orgAccountId, String govdocType, String edocType, String jianbanType, String markType, String markstr) throws BusinessException {
		try { 
			LOGGER.info("验证文号是否被占用：markstr=" + markstr + " govdocType=" + govdocType + " jianbanType=" + jianbanType+ " markType=" + markType + " summaryId=" + summaryId + " orgAccountId=" + orgAccountId);
			if(Strings.isNotBlank(markstr)) {
				//markstr = markstr.trim();
				if(markstr.split("[|]").length >= 3) {
					markstr = markstr.split("[|]")[2];
				}
			}
			
			boolean isUsed = govdocMarkDao.checkMarkIsUsed(markType, govdocType, markstr, summaryId, orgAccountId);
			LOGGER.info("断号表中校验 isUsed=" + isUsed);
			return isUsed;
		} catch(Exception e) {
			LOGGER.error("验证公文文号是否被占用出错", e);
			return false;
		}
	}
	
	@Override
	public boolean checkMarkIsUsedForReserver(String markDefId, int sNo, int eNo) {
		try { 
			EdocMarkDefinition markDef = this.getMarkDef(Long.parseLong(markDefId));
			if(markDef == null) {
				return true;
			}
			if(markDef.getEdocMarkCategory() == null) {
				return true;
			}
			//int startNo = Integer.parseInt(sNo);
			//int endNo = Integer.parseInt(eNo);
			List<Long> markDefIdList = new ArrayList<Long>();
			List<String> markstrList = new ArrayList<String>();
			
			if(markDef.getEdocMarkCategory().getCodeMode().shortValue() == 0) {//小流水
				for(int number = sNo; number <= eNo; number++) {
					GovdocMarkVO newVo = govdocMarkParseManager.markDef2Mode(markDef, null, number);
					markstrList.add(newVo.getMarkstr());
				}			
				markDefIdList.add(markDef.getId());
			} else if(markDef.getEdocMarkCategory().getCodeMode().shortValue() == 1) {//大流水
				List<EdocMarkDefinition> mds = this.getMarkDefsByCategory(markDef.getCategoryId());
				for(EdocMarkDefinition bean : mds) {
					for(int number = sNo; number <= eNo; number++) {
						GovdocMarkVO newVo = govdocMarkParseManager.markDef2Mode(bean, null, number);
						markstrList.add(newVo.getMarkstr());
						markDefIdList.add(markDef.getId());
					}
				}
			}
			
			if(Strings.isNotEmpty(markstrList)) {
				boolean isUsed = govdocMarkDao.checkMarkIsUsedForReserver(markDef.getMarkType(), markDefIdList, markstrList, AppContext.currentAccountId());
				return isUsed;
			}
		} catch(Exception e) {
			LOGGER.error("预留线下占用文号时出错", e);
			return false;
		}
		return true;
	}
	
	@Override
	public boolean checkMarkIsCalledForReserver(String markDefId, int sNo, int eNo) {
		try { 
			EdocMarkDefinition markDef = this.getMarkDef(Long.parseLong(markDefId));
			if(markDef == null) {
				return true;
			}
			if(markDef.getEdocMarkCategory() == null) {
				return true;
			}
			//int startNo = Integer.parseInt(sNo);
			//int endNo = Integer.parseInt(eNo);
			List<Long> markDefIdList = new ArrayList<Long>();
			List<String> markstrList = new ArrayList<String>();
			
			if(markDef.getEdocMarkCategory().getCodeMode().shortValue() == 0) {//小流水
				for(int number = sNo; number <= eNo; number++) {
					GovdocMarkVO newVo = govdocMarkParseManager.markDef2Mode(markDef, null, number);
					markstrList.add(newVo.getMarkstr());
				}			
				markDefIdList.add(markDef.getId());
			} else if(markDef.getEdocMarkCategory().getCodeMode().shortValue() == 1) {//大流水
				List<EdocMarkDefinition> mds = this.getMarkDefsByCategory(markDef.getCategoryId());
				for(EdocMarkDefinition bean : mds) {
					for(int number = sNo; number <= eNo; number++) {
						GovdocMarkVO newVo = govdocMarkParseManager.markDef2Mode(bean, null, number);
						markstrList.add(newVo.getMarkstr());
						markDefIdList.add(markDef.getId());
					}
				}
			}
			
			if(Strings.isNotEmpty(markstrList)) {
				boolean isUsed = govdocMarkDao.checkMarkIsCalledForReserver(markDef.getMarkType(), markDefIdList, markstrList, AppContext.currentAccountId());
				return isUsed;
			}
		} catch(Exception e) {
			LOGGER.error("预留线下占用文号时出错", e);
			return false;
		}
		return true;
	}
	
	/****************************** 公文文号断号/占号校验方法   end *******************************/
	
	
	/****************************** 模板文号方法  start *******************************/
	@SuppressWarnings("rawtypes")
	@Override
	public String getTemplateMarkInfoXmlByParams(User user, Map params) throws BusinessException {
		GovdocBaseVO baseVo = new GovdocBaseVO();
		GovdocMarkHelper.fillSaveMarkParameter(baseVo, params);
		List<TemplateMarkInfo> markObjList = new ArrayList<TemplateMarkInfo>();
		
		GovdocMarkVO docMarkVo = baseVo.getDocMarkVo();
		if(docMarkVo != null) {
			TemplateMarkInfo markObj = new TemplateMarkInfo();
			markObj.setSelectType(docMarkVo.getSelectType());
			markObj.setMarkType(docMarkVo.getMarkType());
			markObj.setMarkDefId(docMarkVo.getMarkDefId());
			markObj.setWordNo(docMarkVo.getWordNo());
			markObjList.add(markObj);
		}
		GovdocMarkVO serialNoVo = baseVo.getSerialNoVo();
		if(serialNoVo != null) {
			TemplateMarkInfo markObj = new TemplateMarkInfo();
			markObj.setSelectType(serialNoVo.getSelectType());
			markObj.setMarkType(serialNoVo.getMarkType());
			markObj.setMarkDefId(serialNoVo.getMarkDefId());
			markObj.setMarkstr(serialNoVo.getMarkstr());
			markObj.setWordNo(serialNoVo.getWordNo());
			markObjList.add(markObj);
		}
		GovdocMarkVO signMarkVo = baseVo.getSignMarkVo();
		if(signMarkVo != null) {
			TemplateMarkInfo markObj = new TemplateMarkInfo();
			markObj.setSelectType(signMarkVo.getSelectType());
			markObj.setMarkType(signMarkVo.getMarkType());
			markObj.setMarkDefId(signMarkVo.getMarkDefId());
			markObj.setMarkstr(signMarkVo.getMarkstr());
			markObj.setWordNo(signMarkVo.getWordNo());
			markObjList.add(markObj);
		}
		return XMLCoder.encoder(markObjList);
	}
	/****************************** 模板文号方法     end *******************************/
	
	@Override
	public List<EdocMarkModel> getMarkVoListForStat(Long domainId) throws BusinessException {
		return edocMarkDefinitionManager.findEdocMarkAndSerinalDefList(domainId);
	}
	
	@Override
	public EdocMarkHistory getMarkHistoryByEdocId(Long summaryId, Integer markType, String markstr) throws BusinessException {
		return govdocMarkDao.getMarkHistoryByEdocId(summaryId, markType, markstr);
	}
	
	@Override
	public EdocMark getMarkByEdocId(Long summaryId, Integer markType, String markstr) throws BusinessException {
		return govdocMarkDao.getMarkByEdocId(summaryId, markType, markstr);
	}
	
	public void setEdocMarkCategoryManager(EdocMarkCategoryManager edocMarkCategoryManager) {
		this.edocMarkCategoryManager = edocMarkCategoryManager;
	}
	public void setEdocMarkDefinitionManager(EdocMarkDefinitionManager edocMarkDefinitionManager) {
		this.edocMarkDefinitionManager = edocMarkDefinitionManager;
	}
	public void setEdocMarkAclManager(EdocMarkAclManager edocMarkAclManager) {
		this.edocMarkAclManager = edocMarkAclManager;
	}
	public void setEdocMarkReserveManager(EdocMarkReserveManager edocMarkReserveManager) {
		this.edocMarkReserveManager = edocMarkReserveManager;
	}
	public void setEdocMarkManager(EdocMarkManager edocMarkManager) {
		this.edocMarkManager = edocMarkManager;
	}
	public void setGovdocMarkDao(GovdocMarkDao govdocMarkDao) {
		this.govdocMarkDao = govdocMarkDao;
	}
	public void setGovdocMarkParseManager(GovdocMarkParseManager govdocMarkParseManager) {
		this.govdocMarkParseManager = govdocMarkParseManager;
	}
	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}
	public void setGovdocMarkOpenManager(GovdocMarkOpenManager govdocMarkOpenManager) {
		this.govdocMarkOpenManager = govdocMarkOpenManager;
	}
	public void setEdocMarkHistoryManager(EdocMarkHistoryManager edocMarkHistoryManager) {
		this.edocMarkHistoryManager = edocMarkHistoryManager;
	}
	public void setGovdocManager(GovdocManager govdocManager) {
		this.govdocManager = govdocManager;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

}
