package com.seeyon.apps.govdoc.mark.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.govdoc.constant.GovdocMarkEnum.MarkDefStateEnum;
import com.seeyon.apps.govdoc.constant.GovdocMarkEnum.SelectTypeEnum;
import com.seeyon.apps.govdoc.helper.GovdocOrgHelper;
import com.seeyon.apps.govdoc.mark.vo.GovdocMarkVO;
import com.seeyon.apps.govdoc.po.GovdocMarkRecord;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.dao.EdocMarkDefinitionDAO;
import com.seeyon.v3x.edoc.domain.EdocMark;
import com.seeyon.v3x.edoc.domain.EdocMarkCategory;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocMarkHistory;
import com.seeyon.v3x.edoc.domain.EdocMarkReserveNumber;
import com.seeyon.v3x.edoc.util.Constants;

public class GovdocMarkDao extends EdocMarkDefinitionDAO {
	
	public static final String MARK_DEF_LIST = "markDef.id,markDef.wordNo,markDef.markType,markDef.expression,markDef.length,markDef.sortNo";
	
	public static final String MARK_CATEGORY_LIST = "category.id,category.categoryName,category.codeMode,category.currentNo,category.minNo,category.maxNo,category.readonly,category.yearEnabled,category.twoYear,category.domainId"; 
	
	public static final String MARK_LIST =  MARK_DEF_LIST + "," + MARK_CATEGORY_LIST;
	
	/**
	 * 公文文号管理列表
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<GovdocMarkVO> findList(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		List<GovdocMarkVO> voList = new ArrayList<GovdocMarkVO>();
		
		Long currentAccountId = Long.parseLong(params.get("currentAccountId"));
		List<Long> deptIds = GovdocOrgHelper.getDeptIdList(currentAccountId);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("select distinct " + MARK_LIST);
		buffer.append(" from EdocMarkDefinition markDef, EdocMarkCategory category");
		buffer.append(" where markDef.categoryId=category.id"); 
		buffer.append(" and markDef.status != :stat");
		buffer.append(" and (");
		buffer.append("	  markDef.domainId=:domainId");
		if(Strings.isNotEmpty(deptIds)) {
			String deptCondition = "";
			//对in里面的数据进行拆分，以300个为单位生成一个list循环传递参数
			List<List<Long>> result = GovdocUtil.createList(deptIds, 300); 
			for(int i=0; i<result.size(); i++) {
				deptCondition += " or markDef.id in (";
				deptCondition += "    select acl.markDefId from EdocMarkAcl acl where ";
				deptCondition += " 		acl.markDefId = markDef.id";
				deptCondition += " 		and acl.deptId in (:deptids" + i + ") ";
				deptCondition += ")";
				paramMap.put("deptids"+i, result.get(i));
			}
			buffer.append(deptCondition);
		}
		buffer.append(" )");
		
		if(Strings.isNotBlank(params.get("condition"))) {//小查询
			if(Strings.isNotBlank(params.get("markType"))) {
				buffer.append(" and markDef.markType = :markType");
				paramMap.put("markType", Integer.parseInt(params.get("markType")));
			}
		}
		
		buffer.append(" order by markDef.markType, markDef.sortNo, markDef.wordNo");
		
	    paramMap.put("domainId", currentAccountId);
	    paramMap.put("stat", MarkDefStateEnum.deleted.key());

	    List<Object[]> result = DBAgent.find(buffer.toString(), paramMap, flipInfo);
	    if(Strings.isNotEmpty(result)) {
			for(Object[] object : result) {
				GovdocMarkVO vo = new GovdocMarkVO();
				vo.toObject(object);
				voList.add(vo);
			}
		}
		
		return voList;
	}
	
	/**
	 * 公文单中显示文号，当前人员能看到的文号列表
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<GovdocMarkVO> getListByUserId(Map<String, String> params) throws BusinessException {
		List<GovdocMarkVO> voList = new ArrayList<GovdocMarkVO>();
		Long templateMarkDefId = Strings.isBlank(params.get("templateMarkDefId")) ? null : Long.parseLong(params.get("templateMarkDefId"));
		Long userId = Long.parseLong(params.get("userId"));
		Long currentAccountId = Long.parseLong(params.get("currentAccountId"));
		Boolean isAdmin = params.get("isAdmin")==null ? false : Boolean.parseBoolean(params.get("isAdmin"));
		List<Long> deptIds = null;
		if(!isAdmin) {
			deptIds = GovdocOrgHelper.getUserDepIds(userId, currentAccountId, isAdmin);	
		}
		List<Integer> markTypeList = new ArrayList<Integer>();
		String[] markTypes = params.get("markType").split(",");
		if(markTypes.length > 1) {
			for(String markType : markTypes) {
				markTypeList.add(GovdocUtil.getMarkTypeValueByType(markType));
			}
		} else if(markTypes.length > 0) {
			markTypeList.add(GovdocUtil.getMarkTypeValueByType(markTypes[0]));
		}
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("select distinct " + MARK_LIST);
		buffer.append(" from EdocMarkDefinition markDef, EdocMarkCategory category");
		buffer.append(" where markDef.categoryId=category.id");
		buffer.append(" and markDef.status != :status");
		if(templateMarkDefId != null) {
			buffer.append(" and markDef.id = :templateMarkDefId");
			paramMap.put("templateMarkDefId", templateMarkDefId);
		} else {
			if(markTypeList.size() > 1) {
				buffer.append(" and markDef.markType in (:markType)");
				paramMap.put("markType", markTypeList);
			} else if(markTypeList.size() > 0) {
				buffer.append(" and markDef.markType = :markType");
				paramMap.put("markType", markTypeList.get(0));
			}
			buffer.append(" and (");
			if(isAdmin) {//单位管理员/公文管理员取本单位所有启用的文号
				buffer.append(" markDef.domainId = :domainId");
				paramMap.put("domainId", currentAccountId);
			} else {
				if(Strings.isNotEmpty(deptIds)) {
					String deptCondition = "";
					//对in里面的数据进行拆分，以300个为单位生成一个list循环传递参数
					List<List<Long>> result = GovdocUtil.createList(deptIds, 300); 
					for(int i=0; i<result.size(); i++) {
						deptCondition += " markDef.id in (";
						deptCondition += "    select acl.markDefId from EdocMarkAcl acl where ";
						deptCondition += " 		acl.markDefId = markDef.id";
						deptCondition += " 		and acl.deptId in (:deptids" + i + ") ";
						deptCondition += ")";
						paramMap.put("deptids"+i, result.get(i));
					}
					buffer.append(deptCondition);
				}
			}
			buffer.append(" )");
		}
		paramMap.put("status", Constants.EDOC_MARK_DEFINITION_DELETED);
		buffer.append(" order by markDef.markType, markDef.sortNo, markDef.wordNo");
		
	    List<Object[]> result = DBAgent.find(buffer.toString(), paramMap);
	    if(Strings.isNotEmpty(result)) {
			for(Object[] object : result) {
				GovdocMarkVO vo = new GovdocMarkVO();
				vo.toObject(object);
				if(GovdocUtil.isH5()) {
					vo.setMarkNumber(vo.getCurrentNo());
				}
				voList.add(vo);
			}
		}
		
		return voList;
	}
	
	@SuppressWarnings("unchecked")
	public List<EdocMarkDefinition> getMarkDefListById(List<Long> markDefIdList) throws BusinessException {
		List<EdocMarkDefinition> markDefList = new ArrayList<EdocMarkDefinition>();
		if(Strings.isNotEmpty(markDefIdList)) {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("markDefId", markDefIdList);
			String hql = "select markDef, category from EdocMarkDefinition markDef, EdocMarkCategory category where markDef.categoryId=category.id";
			if(markDefIdList.size() == 1) {
				hql += " and markDef.id = :markDefId";
			} else {
				hql += " and markDef.id in (:markDefId)";
			}
			List<Object[]> list = DBAgent.find(hql, paramMap);
			if(Strings.isNotEmpty(list)) {
				EdocMarkDefinition markDef = null;
				EdocMarkCategory category = null;
				for(Object[] objs : list) {
					markDef = (EdocMarkDefinition)objs[0];
					category = (EdocMarkCategory)objs[1];
					markDef.setEdocMarkCategory(category);
					markDefList.add(markDef);
				}
			}
		}
		return markDefList;
	}
	
	public void updateMarkCurrentNo(Long categoryId, Integer currentNo) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("categoryId", categoryId);
		paramMap.put("currentNo", currentNo);
		DBAgent.bulkUpdate("update EdocMarkCategory set currentNo = :currentNo where id = :categoryId", paramMap);
	}
	
	public EdocMarkCategory getEdocMarkCategory(Long categoryId) throws BusinessException {
		return DBAgent.get(EdocMarkCategory.class, categoryId);
	}
	
	/*************************************** 公文使用记录 start *********************************************/
	public GovdocMarkRecord getMarkRecordById(Long recordId) throws BusinessException { 
		return DBAgent.get(GovdocMarkRecord.class, recordId);
	}
	@SuppressWarnings("unchecked")
	public List<GovdocMarkRecord> getMarkRecordBySummaryId(String summaryIds, Integer markType) throws BusinessException {
		List<Long> summaryIdList = GovdocUtil.getIdList(summaryIds);
		if(Strings.isNotEmpty(summaryIdList)) {
			String hsql = "from GovdocMarkRecord where summaryId in (:summaryId)";
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("summaryId", summaryIdList);
			if(markType.intValue() != -1) {
				hsql += " and markType=:markType";
				paramMap.put("markType", markType);	
			}
			List<GovdocMarkRecord> recordList = DBAgent.find(hsql, paramMap);
			return recordList;
		}
		return null;
	}
	/**
	 * 获取某公文文号使用记录
	 * @param summaryId
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<GovdocMarkRecord> getMarkRecord(Long summaryId) throws BusinessException { 
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("summaryId", summaryId);
		return DBAgent.find("from GovdocMarkRecord where summaryId = :summaryId order by markType", paramMap);
	}
	/**
	 * 获取某公文某文号类型使用记录
	 * @param summaryId
	 * @param markType
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("rawtypes")
	public GovdocMarkRecord getMarkRecord(Long summaryId, Integer markType) throws BusinessException { 
		String hsql = "from GovdocMarkRecord where summaryId = :summaryId";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("summaryId", summaryId);
		if(markType.intValue() != -1) {
			hsql += " and markType=:markType";
			paramMap.put("markType", markType);	
		}
		List list = DBAgent.find(hsql, paramMap);
		if(Strings.isNotEmpty(list)) {
			return (GovdocMarkRecord)list.get(0);
		}
		return null;
	}
	
	public GovdocMarkRecord getParentMarkRecord(Long formDataId, Integer markType) throws BusinessException {
		List<GovdocMarkRecord> list = getMarkRecordByFormDataId(formDataId, markType, EdocConstant.NewflowType.main.ordinal());
		if(Strings.isNotEmpty(list)) {
			return list.get(0);
		}
		return null;
	}
	
	public List<GovdocMarkRecord> getChildMarkRecordList(Long formDataId, Integer markType) throws BusinessException {
		return getMarkRecordByFormDataId(formDataId, markType, EdocConstant.NewflowType.child.ordinal());
	}
	
	@SuppressWarnings({ "unchecked" })
	private List<GovdocMarkRecord> getMarkRecordByFormDataId(Long formDataId, Integer markType, Integer newflowType) throws BusinessException {
		String hsql = "from GovdocMarkRecord where formDataId = :formDataId and newflowType = :newflowType";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("formDataId", formDataId);
		paramMap.put("newflowType", newflowType);
		if(markType.intValue() != -1) {
			hsql += " and markType=:markType";
			paramMap.put("markType", markType);
		}
		return DBAgent.find(hsql, paramMap);
	}
	
	/**
	 * 某公文文号保存使用记录
	 * @param po
	 */
	public void saveMarkRecord(GovdocMarkRecord po) {
		DBAgent.saveOrUpdate(po);
	}
	public void updateMarkRecord(List<GovdocMarkRecord> list) {
		DBAgent.updateAll(list);
	}
	/*************************************** 公文使用记录   end *********************************************/

	
	/*************************************** 公文断号相关 start *********************************************/
	/**
	 * 获取没有占用的公文断号
	 * @param markDefId
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<EdocMark> findMarkNotUsed(Long markDefId, Integer minNo, Boolean isYearEnabled, Boolean isTwoYear) throws BusinessException {
		String hql = "from EdocMark where markDefId=:markDefId and isLast=1 and docMarkNo >= :minNo";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("markDefId", markDefId);
		paramMap.put("minNo", minNo);
		if(isYearEnabled) {
			Integer yearNo = Calendar.getInstance().get(Calendar.YEAR);
			if(isTwoYear) {
				hql += " and yearNo >= :yearNo";
				yearNo =  yearNo - 1;
			} else {
				hql += " and yearNo = :yearNo";
			}
			paramMap.put("yearNo", yearNo);
		}
		hql += " order by yearNo desc, docMarkNo asc";
		return DBAgent.find(hql, paramMap);
	}
	
	@SuppressWarnings("unchecked")
	public List<EdocMark> findMarkByEdocId(Long edocId, Integer markType) throws BusinessException {
		String hql = "from EdocMark as mark where mark.edocId = :edocId";
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("edocId", edocId);
    	if(markType != null) {
    		hql += " and mark.markType = :markType";
    		paramMap.put("markType", markType);
    	}
    	return DBAgent.find(hql, paramMap);
	}
	
	/**
	 * 
	 * @param edocId
	 * @param docMark
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<EdocMark> findMarkByEdocId(Long edocId, List<String> markstrList) throws BusinessException {
    	String hql = "from EdocMark as mark where mark.edocId = :edocId";
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("edocId", edocId);
    	if(Strings.isNotEmpty(markstrList)) {
    		hql += " or mark.docMark in (:markstr)";
    		paramMap.put("markstr", markstrList);
    	}
    	return DBAgent.find(hql, paramMap);
    }
	
	/**
	 * 修改断号状态为是否最后修改
	 * @param markstrList
	 * @param isLast 1是 0否
	 * @throws BusinessException
	 */
	public void updateMarkIsLast(List<String> markstrList, Integer markType, int isLast) throws BusinessException {
		String hsql = "update EdocMark set isLast = :isLast where docMark in (:markstr)";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("markstr", markstrList);
		paramMap.put("isLast", isLast);
		if(markType.intValue() != -1) {
			hsql += " and markType=:markType";
			paramMap.put("markType", markType);
		}
		DBAgent.bulkUpdate(hsql, paramMap);
	}
	/**
	 * 修改断号状态为是否最后修改
	 * @param markstrList
	 * @param isLast 1是 0否
	 * @throws BusinessException
	 */
	public void updateMarkIsLast(Long summaryId, int isLast) throws BusinessException {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("summaryId", summaryId);
		paramMap.put("isLast", isLast);
		DBAgent.bulkUpdate("update EdocMark set isLast = :isLast where edocId = :summaryId", paramMap);
	}

	/**
	 * 断号与某公文解除绑定
	 * @param markstrList
	 * @param isLast 1是 0否
	 * @throws BusinessException
	 */
	public void unbindMark(Long summaryId) throws BusinessException {
		unbindMark(summaryId, null);
	}
	public void unbindMark(Long summaryId, Integer markType) throws BusinessException {
		String hsql = "update EdocMark set edocId = -1 where edocId = :summaryId";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("summaryId", summaryId);
		if(markType != null) {
			hsql += " and markType=:markType";
			paramMap.put("markType", markType);
		}
		DBAgent.bulkUpdate(hsql, paramMap);
	}
	
	/**
	 * 删除某公文的手工断号
	 * @param markstrList
	 * @param isLast 1是 0否
	 * @throws BusinessException
	 */
	public void deleteMarkHandinput(Long summaryId) throws BusinessException {
		deleteMarkHandinput(summaryId, null);
	}
	public void deleteMarkHandinput(Long summaryId, Integer markType) throws BusinessException {
		String hsql = "delete from EdocMark where edocId = :summaryId and markDefId=-1";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("summaryId", summaryId);
		if(markType != null) {
			hsql += " and markType=:markType";
			paramMap.put("markType", markType);
		}
		DBAgent.bulkUpdate(hsql, paramMap);
	}
	
	/**
	 * 通过公文ID删除占号
	 * @param edocId
	 */
	public void deleteEdocMarkByEdocId(Long edocId) {
		deleteEdocMarkByEdocId(edocId, null);
    }
	public void deleteEdocMarkByEdocId(Long edocId, Integer markType) {
		String hql="delete from EdocMark as mark where mark.edocId = :edocId";
    	Map<String,Object> paramMap = new HashMap<String,Object>();
    	paramMap.put("edocId", edocId);
    	if(markType != null) {
    		hql += " and markType=:markType";
    		paramMap.put("markType", markType);
    	}
    	super.bulkUpdate(hql, paramMap);
	}
	
	/**
	 * 通过公文文号删除占号
	 * @param edocId
	 */
    public void deleteEdocMarkByMarkstr(List<String> markstrList) {
    	deleteEdocMarkByMarkstr(markstrList, null);
    }
    public void deleteEdocMarkByMarkstr(List<String> markstrList, Integer markType) {
    	if(Strings.isNotEmpty(markstrList)) {
    		String hql = "delete from EdocMark";
    		Map<String, Object> paramMap = new HashMap<String, Object>();
        	if(markstrList.size() > 1) {
    			hql += "  where docMark in (:markstr)";
    			paramMap.put("markstr", markstrList);
    		} else {
    			hql += "  where docMark = :markstr";
    			paramMap.put("markstr", markstrList.get(0));
    		}
    		if(markType != null) {
        		hql += " and markType=:markType";
        		paramMap.put("markType", markType);	
        	}
    		DBAgent.bulkUpdate(hql, paramMap);
    	}
    }
    
	@SuppressWarnings("unchecked")
	public boolean checkRecMarkIsCalled(String markType, String govdocType, String markstr, String summaryId, String orgAccountId) {
		Long edocId = Strings.isBlank(summaryId) ? 0L : Long.valueOf(summaryId);
    	Long domainId = Strings.isBlank(orgAccountId) ? 0L : Long.valueOf(orgAccountId);
    	if(Strings.isNotBlank(orgAccountId)) {
    		domainId = Long.valueOf(orgAccountId);
    	}
    	String hsql = "select count(*) from EdocSummary where docMark=:markstr and id<>:edocId and govdocType=:govdocType and orgAccountId=:orgAccountId and state in (:summaryState) and newflowType != :newflowTypeChild";
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("markstr", GovdocUtil.getSQLStr(markstr));
    	paramMap.put("edocId", edocId);
    	paramMap.put("orgAccountId", domainId);
    	paramMap.put("govdocType", Integer.parseInt(govdocType));
    	paramMap.put("newflowTypeChild", EdocConstant.NewflowType.child.ordinal());
    	List<Integer> summaryStateList = new ArrayList<Integer>();
    	summaryStateList.add(0);
    	summaryStateList.add(1);
    	summaryStateList.add(3);
    	paramMap.put("summaryState", summaryStateList);
    	List<Object> list = DBAgent.find(hsql, paramMap);
    	if (Strings.isNotEmpty(list)) {
    		return ((Long)list.get(0)).longValue() > 0;
    	}
    	return false;    	
    }
	@SuppressWarnings("unchecked")
	public boolean checkMarkIsCalled(String markType, String govdocType, String markstr, String summaryId, String orgAccountId) {
		Long edocId = Strings.isBlank(summaryId) ? 0L : Long.valueOf(summaryId);
    	Long domainId = Strings.isBlank(orgAccountId) ? 0L : Long.valueOf(orgAccountId);
    	if(Strings.isNotBlank(orgAccountId)) {
    		domainId = Long.valueOf(orgAccountId);
    	}
    	String hsql = "select count(*) from EdocMark where docMark=:markstr and edocId<>:edocId and edocId<>-1 and domainId=:domainId and markType=:markType";
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("markstr", GovdocUtil.getSQLStr(markstr));
    	paramMap.put("edocId", edocId);
    	paramMap.put("domainId", domainId);
    	if(!"-1".equals(markType)) {
    		hsql += " and markType=:markType";
    		paramMap.put("markType", Integer.parseInt(markType));
    	}
    	List<Object> list = DBAgent.find(hsql, paramMap);
    	if (Strings.isNotEmpty(list)) {
    		return ((Long)list.get(0)).longValue() > 0;
    	}
    	return false;    	
    }
	@SuppressWarnings("unchecked")
	public boolean checkMarkIsUsed(String markType, String govdocType, String markstr, String summaryId, String orgAccountId) {
		Long edocId = Strings.isBlank(summaryId) ? 0L : Long.valueOf(summaryId);
    	Long domainId = Strings.isBlank(orgAccountId) ? 0L : Long.valueOf(orgAccountId);
    	if(Strings.isNotBlank(orgAccountId)) {
    		domainId = Long.valueOf(orgAccountId);
    	}
    	String hsql = "select count(*) from EdocMarkHistory where docMark=:markstr and edocId<>:edocId and domainId=:domainId";
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("markstr", GovdocUtil.getSQLStr(markstr));
    	paramMap.put("edocId", edocId);
    	paramMap.put("domainId", domainId);
    	if(!"-1".equals(markType)) {
    		hsql += " and markType=:markType";
    		paramMap.put("markType", Integer.parseInt(markType));
    	}
    	if("2".equals(govdocType) && "1".equals(markType)) {//serial_no收文编号验证发文、收文、签报流程
    		hsql += " and govdocType in (:govdocType)";
    		List<Integer> govdocTypeList = new ArrayList<Integer>();
    		govdocTypeList.add(1);//因线下占用govdocType为1，需要与线下占用的进行匹配
    		govdocTypeList.add(2);
    		govdocTypeList.add(3);
    		paramMap.put("govdocType", govdocTypeList);
    	} else if("2".equals(govdocType) && "2".equals(markType)) {//sign_mark收文编号只验证收文流程
    		hsql += " and govdocType = :govdocType";
    		paramMap.put("govdocType", Integer.parseInt(govdocType));
    	}
    	if("4".equals(govdocType) && "2".equals(markType)) {//sign_mark签收编号只验证交换流程
    		hsql += " and govdocType = :govdocType";
    		paramMap.put("govdocType", Integer.parseInt(govdocType));
    	}
    	List<Object> list = DBAgent.find(hsql, paramMap);
    	if (Strings.isNotEmpty(list)) {
    		return ((Long)list.get(0)).longValue() > 0;
    	}
    	return false;    	
    }
	
	@SuppressWarnings("unchecked")
	public boolean checkMarkIsUsedForReserver(Integer markType, Long markDefId, String markstr, Long domainId) {
    	String hsql = "select count(*) from EdocMarkHistory where docMark=:markstr and edocId<>-1 and domainId=:domainId and markDefId=:markDefId";
    	
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("markstr", GovdocUtil.getSQLStr(markstr));
    	paramMap.put("markDefId", markDefId);
    	paramMap.put("domainId", domainId);
    	if(markType.intValue() != -1) {
    		hsql += " and markType=:markType";
    		paramMap.put("markType", markType);
    	}
    	List<Object> list = DBAgent.find(hsql, paramMap);
    	
    	if (Strings.isNotEmpty(list)) {
    		return ((Long)list.get(0)).longValue() > 0;
    	}
    	
    	return false;    	
    }
	
	@SuppressWarnings("unchecked")
	public boolean checkMarkIsUsedForReserver(Integer markType, List<Long> markDefIdList, List<String> markstrList, Long domainId) {
    	String hsql = "select count(*) from EdocMarkHistory where docMark in (:markstr) and edocId<>-1 and domainId=:domainId and markDefId in (:markDefId)";
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("markstr", markstrList);
    	paramMap.put("markDefId", markDefIdList);
    	paramMap.put("domainId", domainId);
    	if(markType.intValue() != -1) {
    		hsql += " and markType=:markType";
    		paramMap.put("markType", markType);
    	}
    	List<Object> list = DBAgent.find(hsql, paramMap);
    	
    	if (Strings.isNotEmpty(list)) {
    		return ((Long)list.get(0)).longValue() > 0;
    	}
    	
    	return false;    	
    }
	
	@SuppressWarnings("unchecked")
	public boolean checkMarkIsCalledForReserver(Integer markType, Long markDefId, String markstr, Long domainId) {
    	String hsql = "select count(*) from EdocMark where docMark=:markstr and edocId<>-1 and domainId=:domainId and markDefId=:markDefId";
    	
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("markstr", GovdocUtil.getSQLStr(markstr));
    	paramMap.put("markDefId", markDefId);
    	paramMap.put("domainId", domainId);
    	if(markType.intValue() != -1) {
    		hsql += " and markType=:markType";
    		paramMap.put("markType", markType);
    	}
    	List<Object> list = DBAgent.find(hsql, paramMap);
    	
    	if (Strings.isNotEmpty(list)) {
    		return ((Long)list.get(0)).longValue() > 0;
    	}
    	
    	return false;    	
    }
	
	@SuppressWarnings("unchecked")
	public boolean checkMarkIsCalledForReserver(Integer markType, List<Long> markDefIdList, List<String> markstrList, Long domainId) {
    	String hsql = "select count(*) from EdocMark where docMark in (:markstr) and edocId<>-1 and domainId=:domainId and markDefId in (:markDefId)";
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("markstr", markstrList);
    	paramMap.put("markDefId", markDefIdList);
    	paramMap.put("domainId", domainId);
    	if(markType.intValue() != -1) {
    		hsql += " and markType=:markType";
    		paramMap.put("markType", markType);
    	}
    	List<Object> list = DBAgent.find(hsql, paramMap);
    	
    	if (Strings.isNotEmpty(list)) {
    		return ((Long)list.get(0)).longValue() > 0;
    	}
    	
    	return false;    	
    }
	/*************************************** 公文断号相关   end *********************************************/
	
	
	/*************************************** 公文占号相关 start *********************************************/
	/**
	 * 公文文号管理列表
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<EdocMarkHistory> findUsedList(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		Long currentAccountId = Long.parseLong(params.get("currentAccountId"));
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		StringBuilder buffer = new StringBuilder();
		buffer.append(" from EdocMarkHistory where domainId=:domainId");
		
		if(Strings.isNotBlank(params.get("condition"))) {
			if(Strings.isNotBlank(params.get("selectType")) && !"-1".equals(params.get("selectType"))) {
				if("0".equals(params.get("selectType"))) {
					buffer.append(" and selectType in (:selectType)");
					List<Integer> selectTypeList = new ArrayList<Integer>();
					selectTypeList.add(SelectTypeEnum.zidong.ordinal());
					selectTypeList.add(SelectTypeEnum.duanhao.ordinal());
					selectTypeList.add(SelectTypeEnum.yuliu.ordinal());
					paramMap.put("selectType", selectTypeList);
				} else {
					buffer.append(" and selectType = :selectType");
					paramMap.put("selectType", Integer.parseInt(params.get("selectType")));
				}
			}
			if(Strings.isNotBlank(params.get("yearNo"))) {
				buffer.append(" and yearNo = :yearNo and selectType != :unselecType");
				paramMap.put("yearNo", Integer.parseInt(params.get("yearNo")));
				paramMap.put("unselecType", 1);
			}
			if(Strings.isNotBlank(params.get("markstr"))) {
				buffer.append(" and docMark like :markstr");
				paramMap.put("markstr", "%" + params.get("markstr") + "%");
			}
			if(Strings.isNotBlank(params.get("subject"))) {
				buffer.append(" and subject like :subject");
				paramMap.put("subject", "%" + params.get("subject") + "%");
			}
		}
		buffer.append(" order by createTime desc, docMark desc");
		
	    paramMap.put("domainId", currentAccountId);

	    return DBAgent.find(buffer.toString(), paramMap, flipInfo);
	}
	
	@SuppressWarnings("rawtypes")
	public EdocMarkHistory getMarkHistoryByEdocId(Long edocId, Integer markType, String markstr) throws BusinessException {
		String hql = "from EdocMarkHistory as mark where mark.edocId = :edocId";
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("edocId", edocId);
    	if(markType != null) {
    		hql += " and mark.markType = :markType";
    		paramMap.put("markType", markType);
    	}
    	if(Strings.isNotBlank(markstr)) {
    		hql += " and mark.docMark = :markstr";
    		paramMap.put("markstr", markstr);
    	}
    	List list = DBAgent.find(hql, paramMap);
    	if(Strings.isNotEmpty(list)) {
    		return (EdocMarkHistory)list.get(0);
    	}
    	return null;
	}
	
	@SuppressWarnings("rawtypes")
	public EdocMark getMarkByEdocId(Long edocId, Integer markType, String markstr) throws BusinessException {
		String hql = "from EdocMark as mark where mark.edocId = :edocId";
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("edocId", edocId);
    	if(markType != null) {
    		hql += " and mark.markType = :markType";
    		paramMap.put("markType", markType);
    	}
    	if(Strings.isNotBlank(markstr)) {
    		hql += " and mark.docMark = :markstr";
    		paramMap.put("markstr", markstr);
    	}
    	List list = DBAgent.find(hql, paramMap);
    	if(Strings.isNotEmpty(list)) {
    		return (EdocMark)list.get(0);
    	}
    	return null;
	}
	
	/**
	 * 
	 * @param edocId
	 * @param docMark
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<EdocMarkHistory> findMarkHistoryByEdocId(Long edocId, List<String> markstrList) throws BusinessException {
    	String hql = "from EdocMarkHistory as mark where mark.edocId = :edocId";
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("edocId", edocId);
    	if(Strings.isNotEmpty(markstrList)) {
    		hql += " or mark.docMark in (:markstr)";
    		paramMap.put("markstr", markstrList);
    	}
    	return DBAgent.find(hql, paramMap);
    }
	
	/**
	 * 删除文号占号
	 * @param selectType
	 * @param delReservedIdList
	 * @throws BusinessException
	 */
	public void deleteMarkHistoryByReserve(Integer selectType, List<Long> delReservedIdList) throws BusinessException {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("reserveId", delReservedIdList);
		paramMap.put("selectType", selectType);
		DBAgent.bulkUpdate("delete from EdocMarkHistory where reserveId in (:reserveId) and selectType=:selectType", paramMap);
	}
	
	/**
	 * 删除文号占号
	 * @param selectType
	 * @param delReservedIdList
	 * @throws BusinessException
	 */
	public void deleteMarkHistoryByReserve(Integer selectType, Long markDefId) throws BusinessException {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("markDefId", markDefId);
		paramMap.put("selectType", selectType);
		DBAgent.bulkUpdate("delete from EdocMarkHistory where markDefId=:markDefId and selectType=:selectType", paramMap);
	}
	
	/**
	 * 通过公文ID删除占号
	 * @param edocId
	 */
	public void deleteMarkHistoryByEdocId(Long edocId) {
    	this.deleteMarkHistoryByEdocId(edocId, null);
    }
	public void deleteMarkHistoryByEdocId(Long edocId, Integer markType) {
    	String hql = "delete from EdocMarkHistory as mark where mark.edocId = :edocId";
    	Map<String,Object> paramMap = new HashMap<String,Object>();
    	paramMap.put("edocId", edocId);
    	if(markType != null) {
    		hql += " and markType=:markType";
    		paramMap.put("markType", markType);
    	}
    	DBAgent.bulkUpdate(hql, paramMap);
    }
	
	/**
	 * 通过公文文号删除占号
	 * @param markstrList
	 */
	public void deleteMarkHistoryByMarkstr(List<String> markstrList) {
		deleteMarkHistoryByMarkstr(markstrList, null);
    }
	public void deleteMarkHistoryByMarkstr(List<String> markstrList, Integer markType) {
		if(Strings.isNotEmpty(markstrList)) {
    		String hql = "delete from EdocMarkHistory";
    		Map<String, Object> paramMap = new HashMap<String, Object>();
        	if(markstrList.size() > 1) {
    			hql += "  where docMark in (:markstr)";
    			paramMap.put("markstr", markstrList);
    		} else {
    			hql += "  where docMark = :markstr";
    			paramMap.put("markstr", markstrList.get(0));
    		}
    		if(markType != null) {
        		hql += " and markType=:markType";
        		paramMap.put("markType", markType);	
        	}
    		DBAgent.bulkUpdate(hql, paramMap);
    	}
	}
	/*************************************** 公文占号相关   end *********************************************/
	
	
	/*************************************** 公文预留相关 start *********************************************/
	/**
	 * 获取没有占用的公文预留文号
	 * @param markDefId
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<EdocMarkReserveNumber> findAllNotUsed(Integer type, Long markDefId, Boolean isYearEnabled, Boolean isTwoYear) throws BusinessException {
		String hql = "from EdocMarkReserveNumber where type = :type and markDefineId = :markDefId and isUsed = :isUsed";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("type", type);
		paramMap.put("markDefId", markDefId);
		paramMap.put("isUsed", false);
		if(isYearEnabled) {
			Integer yearNo = Calendar.getInstance().get(Calendar.YEAR);
			if(isTwoYear) {
				hql += " and yearNo >= :yearNo";
				yearNo =  yearNo - 1;
			} else {
				hql += " and yearNo = :yearNo";
			}
			paramMap.put("yearNo", yearNo);
		}
		hql += " order by yearNo desc, markNo asc";
		return DBAgent.find(hql, paramMap);
	}
	/*************************************** 公文预留相关   end *********************************************/
	
}
