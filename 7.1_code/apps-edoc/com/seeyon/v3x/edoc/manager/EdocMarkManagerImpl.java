/**
 * 
 */
package com.seeyon.v3x.edoc.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.govdoc.constant.GovdocEnum.MarkTypeEnum;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.dao.EdocMarkDAO;
import com.seeyon.v3x.edoc.dao.EdocMarkHistoryDAO;
import com.seeyon.v3x.edoc.dao.EdocMarkReserveNumberDao;
import com.seeyon.v3x.edoc.dao.EdocSummaryDao;
import com.seeyon.v3x.edoc.domain.EdocMark;
import com.seeyon.v3x.edoc.domain.EdocMarkCategory;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocMarkHistory;
import com.seeyon.v3x.edoc.domain.EdocMarkReserve;
import com.seeyon.v3x.edoc.domain.EdocParam;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.exception.EdocMarkHistoryExistException;
import com.seeyon.v3x.edoc.util.Constants;
import com.seeyon.v3x.edoc.util.SharedWithThreadLocal;
import com.seeyon.v3x.edoc.webmodel.EdocMarkModel;
import com.seeyon.v3x.edoc.webmodel.EdocMarkNoModel;
import com.seeyon.v3x.edoc.webmodel.EdocMarkReserveVO;

/**
 * 类描述：
 * 创建日期：
 *
 * @author liaoj
 * @version 1.0 
 * @since JDK 5.0
 */
public class EdocMarkManagerImpl implements EdocMarkManager {
	private static final Log LOGGER = LogFactory.getLog(EdocMarkManagerImpl.class);
	private EdocMarkDAO edocMarkDAO;	
	private EdocMarkHistoryDAO edocMarkHistoryDAO;
	private EdocSummaryDao edocSummaryDao;
	private EdocMarkCategoryManager edocMarkCategoryManager;
	private EdocMarkDefinitionManager edocMarkDefinitionManager;
	private EdocMarkReserveManager edocMarkReserveManager;
	private EdocMarkHistoryManager edocMarkHistoryManager;
    private ConfigManager configManager;
	
	public List<EdocMark> findListByMarkDefineId(Long markDefineId) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("markDefineId", markDefineId);
		return (List<EdocMark>)edocMarkDAO.find("from EdocMark where markDefId=:markDefineId", paramMap);
	}
	
	public List<EdocMarkHistory> findHistoryListByMarkDefineId(Long markDefineId) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("markDefineId", markDefineId);
		return (List<EdocMarkHistory>)edocMarkHistoryDAO.find("from EdocMarkHistory where markDefId = :markDefineId", paramMap);
	}
	
	public void save(List<EdocMark> edocMarkList) {
		this.edocMarkDAO.savePatchAll(edocMarkList);
	}
	
	public void setConfigManager(ConfigManager configManager) {
		this.configManager = configManager;
	}

	/**
     * 方法描述：保存公文文号
     */
    public void save(EdocMark edocMark) {
    	this.edocMarkDAO.save(edocMark);
    }
    
    /**
     * 根据ID返回EdocMark对象
     * @param edocMarkId  ID
     * @return
     */
    public EdocMark getEdocMark(Long edocMarkId){
    	return this.edocMarkDAO.get(edocMarkId);
    }
    
    /*客开 项目名称：贵州市政府-G6V580省级专版 作者：mtech 修改日期：2017-08-11 [修改功能：公文库-公文文号对象的最小创建时间]start*/
    public EdocMark getMinCreaateDateEdocMark(){
    	return this.edocMarkDAO.getMinCreaateDateEdocMark();
    }
    /*客开 项目名称：贵州市政府-G6V580省级专版 作者：mtech 修改日期：2017-08-11 [修改功能：公文库-公文文号对象的最小创建时间]end*/
    
    @Override
	public EdocMark getEdocMarkByEdocID(Long edocId) {
		return this.edocMarkDAO.getEdocMarkByEdocID(edocId);
	}
    
    /**
     * 方法描述：保存公文文号，并更新当前值
     * @param edocMark  公文文号对象
     * @param catId     公文类别id
     * @param currentNo 提供给用户选择的公文文号的当前值
     */
    public void save(EdocMark edocMark,Long catId,int currentNo){
    	this.save(edocMark);
        //    	更新当前值
    	this.edocMarkCategoryManager.increaseCurrentNo(catId, currentNo);
    }
    
    public String registDocMark(Long summaryId,String markStr,int markNum,int edocType,boolean checkId,int markType) throws EdocMarkHistoryExistException {
    	return registDocMark(null, summaryId, markStr, markNum, edocType, checkId, markType);
    }
    public String registDocMark(EdocSummary summary,Long summaryId,String markStr,int markNum,int edocType, boolean checkId,int markType) throws EdocMarkHistoryExistException {
    	int govdocType = GovdocUtil.getGovdocTypeByEdocType(edocType);
    	if(Strings.isNotBlank(markStr)){
    		markStr = markStr.replaceAll(String.valueOf((char)160), String.valueOf((char)32));
    	}
        
    	EdocMarkModel em=EdocMarkModel.parse(markStr);
        if (em!=null)
        {
        	Integer t = em.getDocMarkCreateMode();//0:未选择文号，1：下拉选择的文号，2：选择的断号，3.手工输入 4.预留文号
        	String _edocMark = em.getMark(); //需要保存到数据库中的公文文号      
        	Long markDefinitionId = em.getMarkDefinitionId();
        	Long edocMarkId = em.getMarkId();
        	User user = AppContext.getCurrentUser();
	        if(markType== MarkTypeEnum.doc_mark.getKey()){//公文文号
	        	if(t!=0){//等于0的时候没有进行公文文号修改
	        		this.disconnectionEdocSummary(summary,summaryId,markNum);
	        	}
	        	if(edocType != com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_SIGN) {
	        		if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_NEW) { // 选择了一个新的公文文号
	        			Integer currentNo = em.getCurrentNo();
	        			this.createMark(markStr,markDefinitionId, currentNo, _edocMark, summaryId,markNum,govdocType);
	        		}
	        		else if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_OLD) { // 选择了一个断号
	        			this.createMarkByChooseNo(markStr,edocMarkId, summaryId,markNum,govdocType);
	        		}
	        		else if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_RESERVE) { // 选择了一个预留文号
	        			this.createMarkByChooseReserveNo(markStr,edocMarkId, summaryId, em.getCurrentNo(), markNum,govdocType);
	        		}
	        		else if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_INPUT) { // 手工输入一个公文文号
	        			this.createMark(markStr,_edocMark, summaryId,markNum,govdocType);
	        		}
	        	}else {//签报处理
	        		if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_NEW) {
	        			Integer currentNo = em.getCurrentNo();
	        			//下拉选择的文号，文号id不会为空
	        			if(em.getMarkDefinitionId()!=null && currentNo!=null) {
	        				this.edocMarkHistoryManager.save(summaryId,currentNo,_edocMark,markDefinitionId,markNum,govdocType,user.getId(),user.getId(),checkId,true);
	        			}
	        		}else if(t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_OLD) {
	        			this.edocMarkHistoryManager.saveMarkHistorySelectOld(edocMarkId,_edocMark,summaryId, user.getId(),checkId);
	        		}else if(t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_INPUT) {
	        			this.edocMarkHistoryManager.save(summaryId,_edocMark,markDefinitionId,markNum,govdocType,user.getId(),user.getId(),checkId,false);
	        		}
	        	}
	        }else if(markType== MarkTypeEnum.serial_no.getKey()) {//内部文号
	        	if(t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_NEW){
	        		//内部文号且下拉的情况下需要更新当前选了几个了
	        		updateNextCurrentNoByMarkDefId(markDefinitionId, em.getCurrentNo());
	        	}
	        }else if(markType== MarkTypeEnum.sign_mark.getKey()){//签收编号
	        	//签收编号且下拉的情况下需要更新当前选了几个了
	        	updateNextCurrentNoByMarkDefId(markDefinitionId, em.getCurrentNo());
	        }
	        return _edocMark;
        }    	
    	return null;
    }
    
    /**
     * 删除公文文号
     * @param id  公文文号id
     */
    public void deleteEdocMark(long id){
    	this.edocMarkDAO.delete(id);
    }
    
    public void deleteEdocMarkByEdocId(long edocId) {
    	this.edocMarkDAO.deleteEdocMarkByEdocId(edocId);
    }
    
    public void deleteEdocMarkByMarkstr(String markstr) {
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("markstr", markstr);
    	DBAgent.bulkUpdate("delete from EdocMark where docMark = :markstr", paramMap);
    }
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
    
    /**
     * 方法描述：拟文时创建文号
     * 查询当前文号是否被使用，如果已经被使用，则不创建文号记录 add by handy,2007-10-16
     */    
    public void createMark(String memo,Long definitionId, Integer currentNo, String docMark, Long edocId,int markNum,int govdocType) {
    	//检查公文年度编号变更
    	EdocHelper.checkDocmarkByYear();
    	User user = AppContext.getCurrentUser();
    	
    	//----------性能优化，从SharedWithThreadLocal中取文号定义对象
//    	EdocMarkDefinition markDef = edocMarkDefinitionManager.queryMarkDefinitionById(definitionId);
    	EdocMarkDefinition markDef = SharedWithThreadLocal.getMarkDefinition(definitionId);
    	if(markDef == null){
    		markDef = edocMarkDefinitionManager.queryMarkDefinitionById(definitionId);
    	}
    	 
    	if(markDef==null) {
    		return;
    	}
    	
    	EdocMark edocMark = new EdocMark();
    	edocMark.setIdIfNew();
    	edocMark.setMemo(memo);
    	edocMark.setEdocMarkDefinition(markDef);
    	edocMark.setCreateTime(new Date());
    	edocMark.setEdocId(edocId);
    	edocMark.setDocMark(docMark);
    	edocMark.setCreateUserId(user.getId());
    	edocMark.setStatus(Constants.EDOC_MARK_USED);    	
    	edocMark.setDocMarkNo(currentNo);
    	EdocMarkCategory edocMarkCategory = markDef.getEdocMarkCategory();
    	edocMark.setCategoryId(edocMarkCategory.getId());
    	edocMark.setDomainId(user.getLoginAccount());
    	edocMark.setMarkNum(markNum);
    	edocMark.setGovdocType(govdocType);
    	this.save(edocMark);
    	
    	List<EdocMarkDefinition> mds=edocMarkDefinitionManager.getEdocMarkDefinitionsByCategory(edocMarkCategory.getId());
    	if(mds!=null && mds.size()>1) {//多个公文模板共用一个流水号
    		for(EdocMarkDefinition def:mds) {
    			if(definitionId.longValue()==def.getId().longValue()){continue;}
    			edocMark = new EdocMark();
    	    	edocMark.setIdIfNew();
    	    	edocMark.setEdocMarkDefinition(def);
    	    	edocMark.setCreateTime(new Date());
    	    	edocMark.setEdocId(edocId);
    	    	edocMark.setDocMark(edocMarkDefinitionManager.markDef2Mode(def,null,currentNo).getMark());
    	    	edocMark.setCreateUserId(user.getId());
    	    	edocMark.setStatus(Constants.EDOC_MARK_USED);    	
    	    	edocMark.setDocMarkNo(currentNo);
    	    	// 这里不需要再次读取分类，下面行可以注释
    	    	edocMarkCategory = def.getEdocMarkCategory();
    	    	edocMark.setCategoryId(edocMarkCategory.getId());
    	    	edocMark.setDomainId(user.getLoginAccount());
    	    	edocMark.setMarkNum(markNum);
    	    	this.save(edocMark);
    		}
    	}
    	
    	updateNextCurrentNo(markDef, currentNo);
    }



	@Override
	public void createMark(Long definitionId, Integer currentNo, String docMark, Long edocId, int markNum) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createMark(String docMark, Long edocId, int markNum) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createMarkByChooseNo(Long edocMarkId, Long edocId, int markNum) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createMarkByChooseReserveNo(Long edocMarkId, Long edocId, Integer markNumber, int markNum) {
		// TODO Auto-generated method stub
		
	}
    
    /**
     * 
     * @param definitionId
     * @param thisNo
     */
    private void updateNextCurrentNoByMarkDefId(Long definitionId, int thisNo) {
    	if(definitionId == null) {
    		return;
    	}
    	//检查公文年度编号变更
    	EdocHelper.checkDocmarkByYear();
    	
    	EdocMarkDefinition markDef = SharedWithThreadLocal.getMarkDefinition(definitionId);
    	if(markDef == null){
    		markDef = edocMarkDefinitionManager.queryMarkDefinitionById(definitionId);
    	}
    	 
    	if(markDef==null) {
    		return;
    	}
    	
    	updateNextCurrentNo(markDef, thisNo);
    }
    //修改文单的时候，若修改了文号，断开当前公文与前文号的联系。
    public void disconnectionEdocSummary(Long edocSummaryId, int markNum) {
    	disconnectionEdocSummary(null, edocSummaryId, markNum);
    }
    //修改文单的时候，若修改了文号，断开当前公文与前文号的联系。
    public void disconnectionEdocSummary(EdocSummary summary, long edocSummaryId,int markNum)
    {
    	/*if(summary == null) {
    		summary = edocSummaryDao.getEdocSummaryById(edocSummaryId);	
    	}
    	//签报修改文号后释放文号 GOV-1660
    	boolean isSignReport=null!=summary&&null!=summary.getGovdocType()&&3==summary.getGovdocType();
    	List<EdocMark> list = edocMarkDAO.findEdocMarkByEdocSummaryIdAndNum(edocSummaryId,markNum);
    	if(list!=null&&list.size()!=0){
    		for(EdocMark edocMark:list){
    			if(edocMark==null)continue;
    			else{
    				edocMark.setEdocId(-1L);
    				if(isSignReport){
    					edocMark.setStatus(1);
    				}
    			}
//    			edocMarkDAO.save(edocMark);
    			edocMarkDAO.update(edocMark);
    		}
    	}*/
    	//TODO
    }
    
    /**
     * 创建手工输入的公文文号。
     * @param docMark 公文文号
     * @param edocId 公文id
     */
    public void createMark(String memo,String docMark, Long edocId,int markNum, int govdocType) {
    	this.createMark(AppContext.getCurrentUser(), memo, docMark, edocId, markNum, govdocType);
    }
    
    /**
     * 方法描述：拟文时创建文号,选断号的情况下
     * 文号如果已经被使用了呢？？？？？？？？add by handy,2007-10-16
     */    
//    public void createMarkByChooseNo(Long edocMarkId, Long edocId,int markNum) {
//   	检查公文年度编号变更
//    	EdocHelper.checkDocmarkByYear();
//		EdocMark edocMark = edocMarkDAO.get(edocMarkId);
//		edocMark.setEdocId(edocId);
//		edocMark.setStatus(Constants.EDOC_MARK_USED);
//		edocMark.setMarkNum(markNum);
//		this.save(edocMark);    	
//    } 
    /**
     * 断号也直接插入条记录 add at310sp2
     */
    public void createMarkByChooseNo(String memo,Long edocMarkId, Long edocId,int markNum, int govdocType) {
    	createMarkByChooseNo(AppContext.getCurrentUser(), memo, edocMarkId, edocId, markNum, govdocType);
    } 
    
    /**
     * 断号也直接插入条记录 add at310sp2
     */
    public void createMarkByChooseReserveNo(String memo,Long edocMarkId, Long edocId, Integer markNumber, int markNum, int govdocType) {
    	createMarkByChooseReserveNo(AppContext.getCurrentUser(),memo,edocMarkId,edocId,markNumber,markNum,govdocType);
    }
    
    /**
     * 判断文号是否被占用
     * @param edocId     公文id
     * @param EDOCMARK   文号
     * @return   true 被占用 false 未占用
     */
    public boolean isUsed(Long edocId){
//    	检查公文年度编号变更
    	EdocHelper.checkDocmarkByYear();
    	return edocMarkDAO.isUsed(edocId);
    }
    
    @SuppressWarnings("rawtypes")
	public boolean markIsUsed(String markStr,String edocId,String summaryOrgAccountId) {
    	String sql = "select count(*) from EdocMark where status=:status and docMark=:docMark and domainId=:summaryOrgAccountId";
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("status", 110);
    	paramMap.put("docMark", markStr);
    	paramMap.put("summaryOrgAccountId", Long.parseLong(summaryOrgAccountId));
    	if(Strings.isNotBlank(edocId) && !"0".equals(edocId)) {
    		sql += " and edocId=:edocId";
    		paramMap.put("edocId", edocId);
    	}
    	List result = DBAgent.find(sql, paramMap);
    	if(Strings.isNotEmpty(result) && (Long)(result.get(0))>0) {
			return true;
		}
    	return false;
    }
   
    public boolean isUsed(String markStr,String edocId,String summaryOrgAccountId) {
    	return isUsed(markStr, edocId, summaryOrgAccountId, null); 	
    }
    public boolean isUsed(List<String> markStr,String edocId,String summaryOrgAccountId) {
    	//检查公文年度编号变更
    	EdocHelper.checkDocmarkByYear();
    	if(Strings.isBlank(summaryOrgAccountId)|| "0".equals(summaryOrgAccountId)){
    		LOGGER.error("文号判断重复校验，单位ID为空："+markStr+"|"+edocId+"|"+summaryOrgAccountId);
    	}
    	return edocMarkDAO.isUsed(markStr,edocId,summaryOrgAccountId,null);    	
    }
    public boolean markIsUsed(List<String> markStr,String edocId,String summaryOrgAccountId) {
    	//检查公文年度编号变更
    	EdocHelper.checkDocmarkByYear();
    	if(Strings.isBlank(summaryOrgAccountId)|| "0".equals(summaryOrgAccountId)){
    		LOGGER.error("文号判断重复校验，单位ID为空："+markStr+"|"+edocId+"|"+summaryOrgAccountId);
    	}
    	return edocMarkDAO.markIsUsed(markStr,edocId,summaryOrgAccountId,null);    	
    }
    public boolean isUsed(String markStr,String edocId,String summaryOrgAccountId, String govdocType) {
    	//检查公文年度编号变更
    	EdocHelper.checkDocmarkByYear();
    	if(Strings.isBlank(summaryOrgAccountId)|| "0".equals(summaryOrgAccountId)){
    		LOGGER.error("文号判断重复校验，单位ID为空："+markStr+"|"+edocId+"|"+summaryOrgAccountId);
    	}
    	return edocMarkDAO.isUsed(markStr,edocId,summaryOrgAccountId,govdocType);    	
    }
    
    /**
     * 按年度把公文文号归为最小值
     */
    
    public void turnoverCurrentNoAnnual(){
    	
    	User user = AppContext.getCurrentUser();
    	edocMarkCategoryManager.turnoverCurrentNoAnnual();
    //	List<EdocMarkCategory> list = edocMarkCategoryManager.findByTypeAndDomainId(Constants.EDOC_MARK_CATEGORY_BIGSTREAM,user.getLoginAccount());
//    	List<EdocMarkCategory> list = edocMarkCategoryManager.findAll();
//    	for(EdocMarkCategory category:list){
//    		if(category.getYearEnabled()){
//	    		category.setCurrentNo(category.getMinNo());
//	    		edocMarkCategoryManager.updateCategory(category);
//    		}
//    	}
    }
    
    public List<EdocMarkNoModel> getDiscontinuousMarkNos(Long edocMarkDefinitionId){
    	EdocMarkDefinition markDef = edocMarkDefinitionManager.getMarkDefinition(edocMarkDefinitionId);
    	if(markDef != null) {
    		return getDiscontinuousMarkNos(markDef);
    	}
    	return null;
    }
    public List<EdocMarkNoModel> getDiscontinuousMarkNos(EdocMarkDefinition markDef){
    	List<EdocMarkNoModel> results = new ArrayList<EdocMarkNoModel>();
    	//EdocMarkDefinition edocMarkDefinition = edocMarkDefinitionManager.getMarkDefinition(edocMarkDefinitionId);    	
    	//Long categoryId = edocMarkDefinition.getEdocMarkCategory().getId();
    	List<EdocMark> edocMarks = edocMarkDAO.findEdocMarkByMarkDefId4Discontin(markDef.getId());
    	for (EdocMark edocMark:edocMarks) {
    		EdocMarkNoModel markNoVo = edocMarkDefinitionManager.analyzeEdocMarkVo(edocMark.getDocMark(), markDef);
    		markNoVo.setEdocMarkId(edocMark.getId());
    		markNoVo.setMarkNo(edocMark.getDocMark());
    		markNoVo.setMarkNumber(edocMark.getDocMarkNo());
    		markNoVo.setDefinitionId(markDef.getId());
    		markNoVo.setStatus(edocMark.getStatus());
    		results.add(markNoVo);
    	}
    	    	
    	return results;
    } 
    
    public List<EdocMark> findByCategoryAndNo(Long categoryId,Integer docMarkNo)
    {
    	return edocMarkDAO.findEdocMarkByCategoryId(categoryId,docMarkNo);
    }
    /**
     * 发起人撤销流程后，已经调用的文号（如果是最大号）可以恢复，下次发文时可继续调用。
     * @param summary 		公文对象
     * @return
     */
   
    public void edocMarkCategoryRollBack(EdocSummary summary){
    	//签报文号撤销
        /*if(summary.getEdocType() == 2) {
        	// 第一套文号(占号需进入断号)
        	if(Strings.isNotBlank(summary.getDocMark())) {
        		EdocMarkHistory edocMarkHistory = edocMarkHistoryDAO.findEdocMarkHistoryByEdocSummaryIdAndEdocMark(summary.getId(), summary.getDocMark(), 1);
        		rollBackOperation(summary, edocMarkHistory, 1);
        	}
        	// 第二套文号(占号需进入断号)
        	if(summary.getIsunit()&&Strings.isNotBlank(summary.getDocMark2())) {
        		EdocMarkHistory edocMarkHistory = edocMarkHistoryDAO.findEdocMarkHistoryByEdocSummaryIdAndEdocMark(summary.getId(), summary.getDocMark2(), 2);
        		rollBackOperation(summary, edocMarkHistory, 2);
        	}
        }
        //发文内部文号、收文编号、签报都需要撤销文号
        if(Strings.isNotBlank(summary.getSerialNo())) {
    		EdocMarkHistory edocMarkHistory = edocMarkHistoryDAO.findEdocMarkHistoryByEdocSummaryIdAndEdocMark(summary.getId(), summary.getSerialNo(), 1);
    		if(edocMarkHistory != null) {
    			edocMarkHistoryManager.deleteMarkHistoryNew(edocMarkHistory);
    		}
    		//公文撤销后 且最大值流水开关打开，修改当前值为最大值+1
    		ConfigItem configItem = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, IConfigPublicKey.INNER_MARK_BY_MAX, CurrentUser.get().getAccountId());
        	boolean docMarkMaxSwitchOn = configItem!=null&&"yes".equals(configItem.getConfigValue());
    		if(docMarkMaxSwitchOn){
    			int maxUsedDocMark = edocMarkHistoryDAO.getMaxUsedDocMark(edocMarkHistory.getEdocMarkDefinition().getId());
    			this.updateNextCurrentNo(edocMarkHistory.getEdocMarkDefinition(), maxUsedDocMark+1, docMarkMaxSwitchOn);
    		}
    	}

    	ConfigItem configItem = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, IConfigPublicKey.DOC_MARK_BY_MAX, CurrentUser.get().getAccountId());
    	boolean docMarkMaxSwitchOn = configItem!=null&&"yes".equals(configItem.getConfigValue());
		if(docMarkMaxSwitchOn){
			//获取除了该公文以外的最大文号
			EdocMark edocMark = edocMarkDAO.getMaxMark(summary.getId());
			int maxDocMarkNo=-1;
			int updateDocMarkNo=-1;
			if(null==edocMark){
				//为空，文号可能是第一次被使用，重新查询该公文的文号
				edocMark = edocMarkDAO.getEdocMarkByEdocID(summary.getId());
				if(null==edocMark){
					return;
				}
				//设置文号区间的最小值为最大使用值
				EdocMarkCategory docMarkGategory = edocMarkCategoryManager.findById(edocMark.getCategoryId());
				maxDocMarkNo=docMarkGategory.getMinNo()-1;
				edocMark.getEdocMarkDefinition().setEdocMarkCategory(docMarkGategory);
				updateDocMarkNo=docMarkGategory.getMinNo()-1;
			}
			if(null==edocMark.getEdocMarkDefinition().getEdocMarkCategory()){
				edocMark.getEdocMarkDefinition().setEdocMarkCategory(edocMarkCategoryManager.findById(edocMark.getCategoryId()));
			}
			EdocMark currentSummaryMark=this.getEdocMarkByEdocID(summary.getId());
			if(null!=edocMark&&null!=currentSummaryMark){
				//清除多余断号
				List<String> markStrList=new ArrayList<String>();
				if(-1==maxDocMarkNo){
					maxDocMarkNo = edocMark.getDocMarkNo();
					updateDocMarkNo=edocMark.getDocMarkNo();
				}
				for(int tempNo=maxDocMarkNo+1;tempNo<=currentSummaryMark.getDocMarkNo();tempNo++){
					String markstr = edocMarkDefinitionManager.markDef2Mode(edocMark.getEdocMarkDefinition(),null,tempNo).getMark();
					markStrList.add(markstr);
				}
				if(!markStrList.isEmpty()){
					this.deleteEdocMarkByMarkstr(markStrList);
				}
				//如果文号被占用,+1
				String markStr = edocMarkDefinitionManager.markDef2Mode(edocMark.getEdocMarkDefinition(),null,updateDocMarkNo).getMark();
				while(edocMarkHistoryManager.isUsed(markStr, null)){
					markStr = edocMarkDefinitionManager.markDef2Mode(edocMark.getEdocMarkDefinition(),null,++updateDocMarkNo).getMark();
				}
				//重置文号当前值为最大值+1
  				this.updateNextCurrentNo(edocMark.getEdocMarkDefinition(), updateDocMarkNo, docMarkMaxSwitchOn);
			}
		}
//		EdocMarkHistory edocMarkHistory = edocMarkHistoryDAO.findEdocMarkHistoryByEdocSummaryIdAndEdocMark(summary.getId(), summary.getDocMark(), 1);
//		//撤销公文文号
//		if(edocMarkHistory!=null&&edocMarkHistory.getEdocMarkDefinition().getMarkType()==EdocEnum.MarkType.edocMark.ordinal()){
//			ConfigItem configItem = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, IConfigPublicKey.DOC_MARK_BY_MAX, CurrentUser.get().getAccountId());
//        	boolean docMarkMaxSwitchOn = configItem!=null&&"yes".equals(configItem.getConfigValue());
//    		if(docMarkMaxSwitchOn){
//    			//获取最大文号
//  				int maxUsedDocMark = edocMarkHistoryDAO.getMaxUsedDocMark(edocMarkHistory.getEdocMarkDefinition().getId());
//    			//重置文号当前值为最大值+1
//  				this.updateNextCurrentNo(edocMarkHistory.getEdocMarkDefinition(), maxUsedDocMark+1, docMarkMaxSwitchOn);
//  				//清除多余断号
//  				List<String> markStrList=new ArrayList<String>();
//  				for(int tempDocMarkNo=maxUsedDocMark+1;tempDocMarkNo<=edocMarkHistory.getDocMarkNo();tempDocMarkNo++){
//  					String markstr = edocMarkDefinitionManager.markDef2Mode(edocMarkHistory.getEdocMarkDefinition(),null,tempDocMarkNo).getMark();
//  					markStrList.add(markstr);
//  				}
//  				if(!markStrList.isEmpty()){
//  					this.deleteEdocMarkByMarkstr(markStrList);
//  				}
//    		}
//		}*/
    	//TODO
    }
    
    /**
     * 签收编号撤销
     * @param summaryId
     */
    @Override
    public void rollBackRecNo(Long summaryId) {
    	EdocMarkHistory edocMarkHistory = edocMarkHistoryDAO.findEdocMarkHistoryByEdocSummaryId(summaryId);
		if(edocMarkHistory != null) {
			edocMarkHistoryManager.deleteMarkHistoryNew(edocMarkHistory);
		}
    }
    
    //文号回滚具体操作
  	private void rollBackOperation(EdocSummary summary, EdocMarkHistory edocMarkHistory, int num) {
  		User user = AppContext.getCurrentUser();
  		//	当其他公文使用此断号时就查找不到记录
  		if(edocMarkHistory != null) {
  			List<EdocMarkModel> markModelList = edocMarkHistoryManager.deleteMarkHistoryNew(edocMarkHistory);
  			List<EdocMark> markList = new ArrayList<EdocMark>();
  			if(Strings.isNotEmpty(markModelList)) {	
  				for(EdocMarkModel model : markModelList) {
  					EdocMark edocMark = new EdocMark();
  	  				edocMark.setNewId();
  	  				edocMark.setEdocId(-1L);
  	  				edocMark.setCreateTime(DateUtil.currentDate());
  	  				edocMark.setCreateUserId(user.getId());
  	  				edocMark.setDocMark(model.getMark());
  	  				edocMark.setDocMarkNo(edocMarkHistory.getDocMarkNo());
  	  				edocMark.setDomainId(user.getLoginAccount());
  	  				edocMark.setEdocMarkDefinition(model.getMarkDef());
  	  				edocMark.setCategoryId(model.getMarkDef().getCategoryId());
  	  				edocMark.setMarkNum(num);
  	  				edocMark.setGovdocType(edocMarkHistory.getGovdocType());
  	  				edocMark.setStatus(0);
  	  				markList.add(edocMark);
  				}
  			}
  			if(Strings.isNotEmpty(markList)) {
  				edocMarkDAO.saveAll(markList);
  			}
  			
  			//公文文号 按最大值流水开关开启时,释放文号后，当前值设置为最大值+1，并且清除多余的断号
	    	/*ConfigItem configItem = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, IConfigPublicKey.DOC_MARK_BY_MAX, CurrentUser.get().getAccountId());
	    	boolean docMarkMaxSwitchOn = configItem!=null&&"yes".equals(configItem.getConfigValue());
  			if(docMarkMaxSwitchOn
  					&&edocMarkHistory.getEdocMarkDefinition().getMarkType()==EdocEnum.MarkType.edocMark.ordinal()){
  				//获取最大文号
  				int maxUsedDocMark = edocMarkHistoryDAO.getMaxUsedDocMark(edocMarkHistory.getEdocMarkDefinition().getId());
    			//重置文号当前值为最大值+1
  				this.updateNextCurrentNo(edocMarkHistory.getEdocMarkDefinition(), maxUsedDocMark+1, docMarkMaxSwitchOn);
  				//清除多余断号
  				List<String> markStrList=new ArrayList<String>();
  				for(int tempDocMarkNo=maxUsedDocMark+1;tempDocMarkNo<=edocMarkHistory.getDocMarkNo();tempDocMarkNo++){
  					String markstr = edocMarkDefinitionManager.markDef2Mode(edocMarkHistory.getEdocMarkDefinition(),null,tempDocMarkNo).getMark();
  					markStrList.add(markstr);
  				}
  				if(!markStrList.isEmpty()){
  					this.deleteEdocMarkByMarkstr(markStrList);
  				}
  			}*/
  			//TODO
	    	/*
  			//设置公文的文号为空
			if(num==1) {
				summary.setDocMark(null);
			} else {
				summary.setDocMark2(null);
			}
			edocSummaryDao.update(summary);*/
  		}
  	}
	
	public EdocMarkReserve getEdocMarkReserve(Long reserveId) throws BusinessException {
		return this.edocMarkReserveManager.getEdocMarkReserveById(reserveId);
	}
	
	public EdocMarkReserveNumberDao edocMarkReserveNumberDao;
	
	public EdocMarkReserveNumberDao getEdocMarkReserveNumberDao() {
		return edocMarkReserveNumberDao;
	}

	public void setEdocMarkReserveNumberDao(
			EdocMarkReserveNumberDao edocMarkReserveNumberDao) {
		this.edocMarkReserveNumberDao = edocMarkReserveNumberDao;
	}

	public void setEdocSummaryDao(EdocSummaryDao edocSummaryDao) {
		this.edocSummaryDao = edocSummaryDao;
	}
	public void setEdocMarkDefinitionManager(EdocMarkDefinitionManager edocMarkDefinitionManager) {
		this.edocMarkDefinitionManager = edocMarkDefinitionManager;
	}
	public void setEdocMarkDAO(EdocMarkDAO edocMarkDAO) {
		this.edocMarkDAO = edocMarkDAO;
	}
	public void setEdocMarkCategoryManager(EdocMarkCategoryManager edocMarkCategoryManager){
		this.edocMarkCategoryManager = edocMarkCategoryManager;
	}
	public void setEdocMarkReserveManager(EdocMarkReserveManager edocMarkReserveManager) {
		this.edocMarkReserveManager = edocMarkReserveManager;
	}

	public void setEdocMarkHistoryDAO(EdocMarkHistoryDAO edocMarkHistoryDAO) {
		this.edocMarkHistoryDAO = edocMarkHistoryDAO;
	}

	public void setEdocMarkHistoryManager(EdocMarkHistoryManager edocMarkHistoryManager) {
		this.edocMarkHistoryManager = edocMarkHistoryManager;
	}

	@Override
	public List<EdocMark> getEdocMarkByMemo(String memo,User user) {
		if (memo.split("\\|").length<2) {
			return null;
		}
	   	return edocMarkDAO.getEdocMarkByMemo(memo.split("\\|"),user);
	}
	
	@Override
	public List<EdocMark> getAllEdocMark(String memo,Long summaryID) {
		if (memo.split("\\|").length<2) {
			return null;
		}
	   	return edocMarkDAO.getAllEdocMark(memo.split("\\|"),summaryID);
	}
	
	@Override
	public void updateStatus(String memo,User user) throws BusinessException{
		String[] curMemo = memo.split("\\|");
	   	edocMarkDAO.updateStatus(curMemo,user);
	   	edocMarkReserveNumberDao.updateStatus(curMemo);
	}

	@Override
	public void update(List<EdocMark> edocMarkList,User user) {
		try {
			StringBuilder temp = new StringBuilder();
			for(EdocMark eMark:edocMarkList){
				if (temp.indexOf(eMark.getDocMark())>-1) {
					continue;
				}
				temp.append(eMark.getDocMark());
				updateStatus(eMark.getMemo(),user);
			}
		} catch (BusinessException e) {
			LOGGER.error(e);
		}
	}

	@Override
	public void saveDocMark(EdocParam edocParam, User user)
			throws EdocMarkHistoryExistException {

    	/*EdocMarkModel edocMarkModel=EdocMarkModel.parse(edocParam.getMarkStr());
    	if (edocMarkModel!=null)
    	{
    		Integer chooseType = edocMarkModel.getDocMarkCreateMode();//0:未选择文号，1：下拉选择的文号，2：选择的断号，3.手工输入 4.预留文号
    		String _edocMark = edocMarkModel.getMark(); //需要保存到数据库中的公文文号      
    		Long markDefinitionId = edocMarkModel.getMarkDefinitionId();
    		Long edocMarkId = edocMarkModel.getMarkId();
    		if (chooseType!=0 && (edocParam.getMarkType()==FormFieldComEnum.BASE_MARK || edocParam.getMarkType()==FormFieldComEnum.BASE_SIGN_MARK)) {
    			//chooseType等于0的时候没有进行公文文号修改
				this.disconnectionEdocSummary(null,edocParam.getSummaryId(),edocParam.getMarkNum());
			}
    		Integer currentNo = edocMarkModel.getCurrentNo();
    		if(edocParam.getMarkType()==FormFieldComEnum.BASE_MARK){//公文文号
    			if (chooseType == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_NEW) { // 选择了一个新的公文文号
					this.createMark(user,edocParam.getMarkStr(),markDefinitionId, currentNo, _edocMark, edocParam.getSummaryId(),edocParam.getMarkNum(), edocParam.getGovdocType());
				}else if (chooseType == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_OLD) { // 选择了一个断号
					this.createMarkByChooseNo(user,edocParam.getMarkStr(),edocMarkId, edocParam.getSummaryId(),edocParam.getMarkNum(), edocParam.getGovdocType());
				}else if (chooseType == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_RESERVE) { // 选择了一个预留文号
					this.createMarkByChooseReserveNo(user,edocParam.getMarkStr(),edocMarkId, edocParam.getSummaryId(), edocMarkModel.getCurrentNo(), edocParam.getMarkNum(), edocParam.getGovdocType());
				}else if (chooseType == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_INPUT) { // 手工输入一个公文文号
					this.createMark(user,edocParam.getMarkStr(),_edocMark, edocParam.getSummaryId(),edocParam.getMarkNum(), edocParam.getGovdocType());
				}
    		}else if(edocParam.getMarkType()==FormFieldComEnum.BASE_INNER_MARK
    				&& chooseType == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_NEW){
    			//内部文号且下拉的情况下需要更新当前选了几个了
    			updateNextCurrentNoByMarkDefId(markDefinitionId, currentNo);
    		}else if(edocParam.getMarkType()==FormFieldComEnum.BASE_SIGN_MARK
    				&& chooseType == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_NEW){
    			//签收编号且下拉的情况下需要更新当前选了几个了
    			updateNextCurrentNoByMarkDefId(markDefinitionId, currentNo);
    		}
    	}    	
    	 */
		//TODO
		
	}

	private void createMarkByChooseReserveNo(User user, String memo,
			Long edocMarkId, Long edocId, Integer markNumber, int markNum, int govdocType) {
		EdocMarkDefinition edocMarkDefinition = edocMarkDefinitionManager.getMarkDefinition(edocMarkId);
    	if(edocMarkDefinition == null) return;
    	EdocMarkReserveVO reserveVO = edocMarkReserveManager.getMarkReserveByFormat(edocMarkDefinition, markNumber);
    	//检查公文年度编号变更
    	EdocHelper.checkDocmarkByYear();
		//EdocMark mark = edocMarkDAO.get(edocMarkId);
		EdocMark edocMark=new EdocMark();
		edocMark.setIdIfNew();
		edocMark.setEdocMarkDefinition(edocMarkDefinition);
		edocMark.setCreateTime(new Date());
		edocMark.setEdocId(edocId);
		edocMark.setMemo(memo);
		if (Strings.isNotBlank(memo)) {
			if (memo.split("\\|").length<2) {
				edocMark.setDocMark(memo.split("\\|")[0]);
			}else {
				edocMark.setDocMark(memo.split("\\|")[1]);
			}
		}else {
			edocMark.setDocMark(reserveVO.getReserveNo());
		}
		edocMark.setCreateUserId(user.getId());
		edocMark.setStatus(Constants.EDOC_MARK_USED);
		edocMark.setDocMarkNo(markNumber);
		edocMark.setCategoryId(edocMarkDefinition.getEdocMarkCategory().getId());
		edocMark.setDomainId(user.getLoginAccount());
		edocMark.setMarkNum(markNum);
		edocMark.setGovdocType(govdocType);
		this.save(edocMark);  
		
	}

	private void createMarkByChooseNo(User user, String memo,
			Long edocMarkId, Long edocId, int markNum, int govdocType) {
		//检查公文年度编号变更
    	EdocHelper.checkDocmarkByYear();
		EdocMark mark = edocMarkDAO.get(edocMarkId);
		if(mark != null) {
			EdocMark edocMark=new EdocMark();
			edocMark.setIdIfNew();
			edocMark.setCreateTime(new Date());
			edocMark.setEdocId(edocId);
			edocMark.setMemo(memo);
			edocMark.setCreateUserId(user.getId());
			edocMark.setStatus(Constants.EDOC_MARK_USED);
			edocMark.setDomainId(user.getLoginAccount());
			edocMark.setMarkNum(markNum);
			edocMark.setGovdocType(govdocType);
			edocMark.setEdocMarkDefinition(mark.getEdocMarkDefinition());
			edocMark.setDocMark(mark.getDocMark());
			edocMark.setDocMarkNo(mark.getDocMarkNo());
			edocMark.setCategoryId(mark.getCategoryId());
		
			this.save(edocMark);
		}
	}

	private void createMark(User user, String memo, String docMark,
			Long edocId, int markNum, int govdocType) {
		//检查公文年度编号变更
    	EdocHelper.checkDocmarkByYear();
    	EdocMark edocMark = new EdocMark();
    	edocMark.setIdIfNew();
    	edocMark.setCreateTime(new Date());
    	edocMark.setEdocId(edocId);
    	edocMark.setDocMark(docMark);
    	edocMark.setMemo(memo);
    	edocMark.setCreateUserId(user.getId());
    	edocMark.setStatus(Constants.EDOC_MARK_USED);
    	edocMark.setDocMarkNo(0);
    	edocMark.setCategoryId(0L);    	
    	edocMark.setDomainId(user.getLoginAccount());
    	edocMark.setMarkNum(markNum);
    	edocMark.setGovdocType(govdocType);
    	this.save(edocMark);
		
	}

	private void createMark(User user,String memo,Long definitionId, Integer currentNo, String docMark, Long edocId,int markNum,int govdocType) {
		//检查公文年度编号变更
		EdocHelper.checkDocmarkByYear();
		
		EdocMarkDefinition markDef = SharedWithThreadLocal.getMarkDefinition(definitionId);
		if(markDef == null) {
			markDef = edocMarkDefinitionManager.queryMarkDefinitionById(definitionId);
		}
		 
		if(markDef==null) {
			return;
		}
		
		EdocMark edocMark = new EdocMark();
		edocMark.setIdIfNew();
		edocMark.setMemo(memo);
		edocMark.setEdocMarkDefinition(markDef);
		edocMark.setCreateTime(new Date());
		edocMark.setEdocId(edocId);
		edocMark.setDocMark(docMark);
		edocMark.setCreateUserId(user.getId());
		edocMark.setStatus(Constants.EDOC_MARK_USED);    	
		edocMark.setDocMarkNo(currentNo);
		EdocMarkCategory edocMarkCategory = markDef.getEdocMarkCategory();
		edocMark.setCategoryId(edocMarkCategory.getId());
		edocMark.setDomainId(user.getLoginAccount());
		edocMark.setMarkNum(markNum);
		edocMark.setGovdocType(govdocType);
		this.save(edocMark);
		
		List<EdocMarkDefinition> mds=edocMarkDefinitionManager.getEdocMarkDefinitionsByCategory(edocMarkCategory.getId());
		if(mds!=null && mds.size()>1) {//多个公文模板共用一个流水号
			for(EdocMarkDefinition def:mds) {
				if(definitionId.longValue()==def.getId().longValue()){continue;}
				edocMark = new EdocMark();
		    	edocMark.setIdIfNew();
		    	edocMark.setEdocMarkDefinition(def);
		    	edocMark.setCreateTime(new Date());
		    	edocMark.setEdocId(edocId);
		    	edocMark.setDocMark(edocMarkDefinitionManager.markDef2Mode(def,null,currentNo).getMark());
		    	edocMark.setCreateUserId(user.getId());
		    	edocMark.setStatus(Constants.EDOC_MARK_USED);    	
		    	edocMark.setDocMarkNo(currentNo);
		    	// 这里不需要再次读取分类，下面行可以注释
		    	edocMarkCategory = def.getEdocMarkCategory();
		    	edocMark.setCategoryId(edocMarkCategory.getId());
		    	edocMark.setDomainId(user.getLoginAccount());
		    	edocMark.setMarkNum(markNum);
		    	edocMark.setGovdocType(govdocType);
		    	this.save(edocMark);
			}
		}
		
		updateNextCurrentNo(markDef, currentNo);
	}
	
	
	/**
     * 保存文号断号、占号及跳转(20170721)
     * @param edocParam 封装文号保存的参数类，参数太多了
     * @throws EdocMarkHistoryExistException
     */
	public String saveDocMark(EdocParam edocParam) throws EdocMarkHistoryExistException {
    	/*int govdocType = edocParam.getGovdocType();
    	int edocType = GovdocUtil.getEdocTypeByGovdoc(edocParam.getGovdocType());
    	int markType = GovdocUtil.getMarkTypeByField(edocParam.getMarkType());
    	
    	String docMark = edocParam.getDocMark();
    	if(Strings.isNotBlank(docMark)) {
    		docMark = docMark.replaceAll(String.valueOf((char)160), String.valueOf((char)32));
    	}
    	//公文文号交换到收文时，只保存文号文字，不生成文号序号
    	if(markType == 0 && (govdocType==2 || govdocType==4)) {
    		return docMark;
    	}
        
    	EdocMarkModel model = EdocMarkModel.parse(docMark);
        if (model != null) {
        	String markstr = model.getMark(); //需要保存到数据库中的公文文号
        	if(Strings.isBlank(markstr)||(model.getCurrentNo()!=null && model.getCurrentNo()==com.seeyon.ctp.common.constants.Constants.EDOC_MARK_NULL_NUMBER)) {
        		return markstr;
        	}
        	User currentUser = AppContext.getCurrentUser();
	        if(markType == EdocEnum.MarkType.edocMark.ordinal()) {//公文文号
	        	if(edocType != com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_SIGN) {
	        		if(edocParam.isExchange()) {//公文有交换，需要将占号
	        			this.edocMarkHistoryManager.saveMarkHistoryNew(edocParam, model, currentUser);
	        		} else {//公文无交换，只需要进断号
	        			this.saveMarkNew(edocParam, model, currentUser);
	        		}
	        	} else {
	        		this.edocMarkHistoryManager.saveMarkHistoryNew(edocParam, model, currentUser);	
	        	}
	        } else if(markType == EdocEnum.MarkType.edocInMark.ordinal()) {//内部文号
	        	this.edocMarkHistoryManager.saveMarkHistoryNew(edocParam, model, currentUser);
	        } else if(markType == EdocEnum.MarkType.edocSignMark.ordinal()) {//签收编号
	        	this.edocMarkHistoryManager.saveMarkHistoryNew(edocParam, model, currentUser);
	        }
	        return markstr;
        }*/
		//TODO
    	return null;
    }
	
	
	/**
	 * 文号断号保存、跳号(20170721)
	 * @param edocParam
	 * @param model
	 * @param currentUser
	 */
	private void saveMarkNew(EdocParam edocParam, EdocMarkModel model, User currentUser) {
		//检查公文年度编号变更
    	/*EdocHelper.checkDocmarkByYear();
    	User user = AppContext.getCurrentUser();
    	
    	Long summaryId = edocParam.getSummaryId();
    	Long definitionId = model.getMarkDefinitionId();
    	Integer currentNo = model.getCurrentNo();
    	String markstr = model.getMark();
    	
		boolean needUpdateMark = true;
    	Integer createMode = model.getDocMarkCreateMode();//0:未选择文号，1：下拉选择的文号，2：选择的断号，3.手工输入 4.预留文号
		if(createMode == 2) {//选择断号
	    	EdocMark mark = this.getEdocMark(definitionId);
			if(mark != null) {
				if(mark.getDocMark().equals(markstr) && mark.getEdocId().longValue()==summaryId.longValue()) {
					boolean isSameZidongMark = mark.getEdocMarkDefinition()!=null && mark.getEdocMarkDefinition().getId().longValue()==definitionId.longValue();
					needUpdateMark = !isSameZidongMark;
				}
				//若是选择的是断号，则获取新的definitionId
				definitionId = mark.getEdocMarkDefinition().getId();
			}
		} else {
			EdocMark mark = this.getEdocMarkByEdocID(summaryId);
			if(mark != null) {
				//当文号相同、或者手写的文号相同，则不需要更改文号相关信息
				if(mark.getDocMark().equals(markstr) && 
						(mark.getEdocMarkDefinition()!=null && mark.getEdocMarkDefinition().getId().longValue()==definitionId.longValue()
						|| mark.getEdocMarkDefinition()==null && createMode==3)) {
					needUpdateMark = false;
				}
			}			
		}
		if(!needUpdateMark) {
			return;
		}
		
		List<String> markstrList = new ArrayList<String>();
		if(Strings.isNotEmpty(markstr)) {
			markstrList.add(markstr);
		}
		
		EdocMarkCategory edocMarkCategory = null;
    	EdocMarkDefinition markDef = SharedWithThreadLocal.getMarkDefinition(definitionId);
    	if(markDef == null){
    		markDef = edocMarkDefinitionManager.queryMarkDefinitionById(definitionId);
    	}
    	
    	List<EdocMark> markList = new ArrayList<EdocMark>();
    	int markNum = 1;
    	
    	EdocMark edocMark = new EdocMark();
    	edocMark.setIdIfNew();
    	edocMark.setMemo(edocParam.getDocMark());
    	edocMark.setEdocMarkDefinition(markDef);
    	Date now = new Date();
		edocMark.setCreateTime(now);
    	edocMark.setEdocId(summaryId);
    	edocMark.setDocMark(markstr);
    	edocMark.setCreateUserId(user.getId());
    	edocMark.setStatus(Constants.EDOC_MARK_USED);    	
    	edocMark.setDocMarkNo(currentNo);
    	if(markDef != null && markDef.getEdocMarkCategory()!=null) {
	    	edocMarkCategory = markDef.getEdocMarkCategory();
	    	edocMark.setCategoryId(edocMarkCategory.getId());
    	}
    	edocMark.setDomainId(user.getLoginAccount());
    	edocMark.setMarkNum(markNum);
    	edocMark.setGovdocType(edocParam.getGovdocType());
    	markList.add(edocMark);
    	//公文文号 按最大值流水开关开启时，当前值+1，隔代文号都放入断号
    	int markType = GovdocUtil.getMarkTypeByField(edocParam.getMarkType());
    	boolean docMarkMaxSwitchOn=false;
    	if(markType == EdocEnum.MarkType.edocMark.ordinal()){
	    	ConfigItem configItem = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, IConfigPublicKey.DOC_MARK_BY_MAX, currentUser.getAccountId());
	    	docMarkMaxSwitchOn = configItem!=null&&"yes".equals(configItem.getConfigValue());
    	}
    	if(docMarkMaxSwitchOn&&markDef!=null){
    		for(int tempCurrentNo=markDef.getEdocMarkCategory().getCurrentNo();tempCurrentNo<currentNo;tempCurrentNo++){
    			markstr=edocMarkDefinitionManager.markDef2Mode(markDef,null,tempCurrentNo).getMark();
    			//如果文号已占用，不放入断号
    			boolean isUsed = edocMarkHistoryManager.isUsed(markstr, -1L);
    			if(isUsed){
    				continue;
    			}
    			markstrList.add(markstr);
    			
    			EdocMark tempMark = new EdocMark();
	    		tempMark.setIdIfNew();
	    		tempMark.setMemo(edocParam.getDocMark());
	    		tempMark.setEdocMarkDefinition(markDef);
	    		tempMark.setCreateTime(now);
	    		tempMark.setEdocId(-1l);
	    		
	    		tempMark.setDocMark(markstr);
	    		tempMark.setCreateUserId(user.getId());
	    		tempMark.setStatus(Constants.EDOC_MARK_USED);    	
	    		tempMark.setDocMarkNo(tempCurrentNo);
	        	if(markDef != null && markDef.getEdocMarkCategory()!=null) {
	    	    	edocMarkCategory = markDef.getEdocMarkCategory();
	    	    	tempMark.setCategoryId(edocMarkCategory.getId());
	        	}
	        	tempMark.setDomainId(user.getLoginAccount());
	        	tempMark.setMarkNum(markNum);
	        	tempMark.setGovdocType(edocParam.getGovdocType());
	        	markList.add(tempMark);
    		}
    	}

    	//同大流水号的文号也进入断号
    	if(markDef != null && edocMarkCategory!=null) {
	    	List<EdocMarkDefinition> mds = edocMarkDefinitionManager.getEdocMarkDefinitionsByCategory(edocMarkCategory.getId());
	    	if(mds!=null && mds.size()>1) {//多个公文模板共用一个流水号
	    		for(EdocMarkDefinition def : mds) {
	    			if(definitionId.longValue() == def.getId().longValue()) {
	    				continue;
	    			}
	    			
	    			markstr = edocMarkDefinitionManager.markDef2Mode(def,null,currentNo).getMark();
	    			if(Strings.isNotBlank(markstr)) {
	    				markstrList.add(markstr);
	    			}
	    			
	    			edocMark = new EdocMark();
	    	    	edocMark.setIdIfNew();
	    	    	edocMark.setEdocMarkDefinition(def);
	    	    	edocMark.setCreateTime(now);
	    	    	edocMark.setEdocId(summaryId);
	    	    	edocMark.setDocMark(markstr);
	    	    	edocMark.setCreateUserId(user.getId());
	    	    	edocMark.setStatus(Constants.EDOC_MARK_USED);    	
	    	    	edocMark.setDocMarkNo(currentNo);
	    	    	edocMark.setCategoryId(edocMarkCategory.getId());
	    	    	edocMark.setDomainId(user.getLoginAccount());
	    	    	edocMark.setMarkNum(markNum);
	    	    	markList.add(edocMark);
	    	    	
	    	    	//公文文号按最大值流水开关开启时，隔代文号都放入断号
	    	    	if(docMarkMaxSwitchOn){
	    	    		for(int tempCurrentNo=def.getEdocMarkCategory().getCurrentNo();tempCurrentNo<currentNo;tempCurrentNo++){
	    	    			markstr = edocMarkDefinitionManager.markDef2Mode(def,null,tempCurrentNo).getMark();
	    	    			//如果文号已占用，不放入断号
	    	    			boolean isUsed = edocMarkHistoryManager.isUsed(markstr, -1L);
	    	    			if(isUsed){
	    	    				continue;
	    	    			}
	    	    			
	    	    			if(Strings.isNotBlank(markstr)) {
	    	    				markstrList.add(markstr);
	    	    			}
	    	    			
	    	    			EdocMark tempMark = new EdocMark();
	    		    		tempMark.setIdIfNew();
	    		    		tempMark.setEdocMarkDefinition(def);
	    		    		tempMark.setCreateTime(now);
	    		    		tempMark.setEdocId(-1l);
	    		    		tempMark.setDocMark(markstr);
	    		    		tempMark.setCreateUserId(user.getId());
	    		    		tempMark.setStatus(Constants.EDOC_MARK_USED);    	
	    		    		tempMark.setDocMarkNo(tempCurrentNo);
	    		    		tempMark.setCategoryId(edocMarkCategory.getId());
	    		    		tempMark.setDomainId(user.getLoginAccount());
	    		    		tempMark.setMarkNum(markNum);
	    		        	markList.add(tempMark);
	    	    		}
	    	    	}
	    		}
	    	}
    	}
    	
    	//释放断号
		//this.deleteEdocMarkByEdocId(summaryId);
		if(Strings.isNotEmpty(markstrList)) {
			this.deleteEdocMarkByMarkstr(markstrList);
		}
		 //保存断号
    	if(Strings.isNotEmpty(markList)) {
    		this.save(markList);
    	}
    	
    	//选择文号，则更新文号序号
    	if(markDef != null && currentNo >= markDef.getEdocMarkCategory().getCurrentNo()) {
    		updateNextCurrentNo(markDef, currentNo,docMarkMaxSwitchOn);
    	}*/
		//TODO
	}
	/**
     * 文号跳号(20170721)
     * @param markDef
     * @param thisNo
     */
    public void updateNextCurrentNo(EdocMarkDefinition markDef, Integer thisNo) {
    	//本次使用的文号非当前文号序号，则不用自增
    	/*int currentNo = markDef.getEdocMarkCategory().getCurrentNo(); 
    	if(thisNo==null || currentNo != thisNo) {
    		return;
    	}
    	int addOneNo = edocMarkHistoryManager.increatementCurrentNo(markDef, currentNo, markDef.getEdocMarkCategory());
		String markStr = edocMarkDefinitionManager.markDef2Mode(markDef, null, addOneNo).getMark();
		try {
    		//若自增序号文号已经使用，则跳号，直到跳到本单位没有使用过的文号为止
	    	boolean isUsed = edocSummaryDao.isMarkUsedForStep(markStr, markDef.getMarkType(), String.valueOf(AppContext.currentAccountId()));   	
	    	int count = 0;
	    	while(isUsed && count <1000) {
	    		count++;
	    		addOneNo = edocMarkHistoryManager.increatementCurrentNo(markDef, addOneNo, markDef.getEdocMarkCategory());
		    	markStr = edocMarkDefinitionManager.markDef2Mode(markDef,null,addOneNo).getMark();
		    	
		    	isUsed = edocSummaryDao.isMarkUsedForStep(markStr, markDef.getMarkType(), String.valueOf(AppContext.currentAccountId()));
	    	}
    	} catch(Exception e) {
    		LOGGER.error("公文跳号出错", e);
    	}
    	
    	//使用文号序号大于当前文号时
    	boolean isIncreatement = currentNo < addOneNo;
    	if(isIncreatement) {
    		EdocMarkCategory edocMarkCategory = markDef.getEdocMarkCategory();
    		edocMarkCategory.setCurrentNo(addOneNo);
    		edocMarkCategoryManager.updateCategory(edocMarkCategory);
    	    //edocMarkCategoryManager.updateCategoryCurrentNo(markDef.getCategoryId(), addOneNo);
    	}
    	
    	setEdocMarkDefinitionPublished(markDef);*/
    	//TODO
    }
    public void updateNextCurrentNo(boolean check,EdocMarkDefinition markDef, Integer thisNo) {
    	//本次使用的文号非当前文号序号，则不用自增
    	/*int currentNo = markDef.getEdocMarkCategory().getCurrentNo(); 
    	if(thisNo==null || currentNo != thisNo) {
    		return;
    	}
    	int addOneNo = edocMarkHistoryManager.increatementCurrentNo(markDef, currentNo, markDef.getEdocMarkCategory());
		String markStr = edocMarkDefinitionManager.markDef2Mode(markDef, null, addOneNo).getMark();
		if(check){
			try {
	    		//若自增序号文号已经使用，则跳号，直到跳到本单位没有使用过的文号为止
		    	boolean isUsed = edocSummaryDao.isMarkUsedForStep(markStr, markDef.getMarkType(), String.valueOf(AppContext.currentAccountId()));   	
		    	int count = 0;
		    	while(isUsed && count <1000) {
		    		count++;
		    		addOneNo = edocMarkHistoryManager.increatementCurrentNo(markDef, addOneNo, markDef.getEdocMarkCategory());
			    	markStr = edocMarkDefinitionManager.markDef2Mode(markDef,null,addOneNo).getMark();
			    	
			    	isUsed = edocSummaryDao.isMarkUsedForStep(markStr, markDef.getMarkType(), String.valueOf(AppContext.currentAccountId()));
		    	}
	    	} catch(Exception e) {
	    		LOGGER.error("公文跳号出错", e);
	    	}
		}
    	
    	//使用文号序号大于当前文号时
    	boolean isIncreatement = currentNo < addOneNo;
    	if(isIncreatement) {
    		EdocMarkCategory edocMarkCategory = markDef.getEdocMarkCategory();
    		edocMarkCategory.setCurrentNo(addOneNo);
    		edocMarkCategoryManager.updateCategory(edocMarkCategory);
    	    //edocMarkCategoryManager.updateCategoryCurrentNo(markDef.getCategoryId(), addOneNo);
    	}
    	
    	setEdocMarkDefinitionPublished(markDef);*/
    	//TODO
    }
    public void updateNextCurrentNo(EdocMarkDefinition markDef, Integer thisNo,boolean checkCurrentNo) {
    	if(null==markDef.getEdocMarkCategory()){
    		markDef.setEdocMarkCategory(edocMarkCategoryManager.findById(markDef.getCategoryId()));
    	}
    	if(!checkCurrentNo){
    		this.updateNextCurrentNo(markDef, thisNo);
    	}else{
    		if(thisNo==null ){
    			return;
    		//回退最大值+1
    		}else{
    			markDef.getEdocMarkCategory().setCurrentNo(thisNo);
    			this.updateNextCurrentNo(!checkCurrentNo,markDef, thisNo);
    		}
    	}
    }
    /**
     * 文号发布状态修改为已发布/已使用(20170721)
     * @param markDef
     */
	private void setEdocMarkDefinitionPublished(EdocMarkDefinition markDef) {
		//设置已经使用。
    	if(markDef.getStatus().shortValue() == Constants.EDOC_MARK_DEFINITION_DRAFT){
    		markDef.setStatus(Constants.EDOC_MARK_DEFINITION_PUBLISHED);
    		edocMarkDefinitionManager.updateMarkDefStatus(markDef.getId(), Constants.EDOC_MARK_DEFINITION_PUBLISHED);
    	}
	}
	
	/**
     * 验校EdocMarkHistory文号占用表中是否有该文号
     * @param markType
     * @param govdocType
     * @param markStr
     * @param summaryId
     * @param summaryOrgAccountId
     * @return
     */
	@Override
	public boolean isGovdocUsedNew(String markType, String govdocType, String markStr, String summaryId,String summaryOrgAccountId) {
    	//检查公文年度编号变更
    	EdocHelper.checkDocmarkByYear();
    	
    	if(Strings.isBlank(summaryOrgAccountId)|| "0".equals(summaryOrgAccountId)){
    		LOGGER.error("文号判断重复校验，单位ID为空："+markStr+"|"+summaryId+"|"+summaryOrgAccountId);
    	}
    	return edocMarkDAO.isGovdocUsedNew(markType, govdocType, markStr,summaryId,summaryOrgAccountId);    	
    }
	
}

