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

import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.dao.EdocMarkAclDAO;
import com.seeyon.v3x.edoc.dao.EdocMarkDAO;
import com.seeyon.v3x.edoc.dao.EdocMarkHistoryDAO;
import com.seeyon.v3x.edoc.domain.EdocMark;
import com.seeyon.v3x.edoc.domain.EdocMarkAcl;
import com.seeyon.v3x.edoc.domain.EdocMarkCategory;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocMarkHistory;
import com.seeyon.v3x.edoc.domain.EdocParam;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.exception.EdocMarkHistoryExistException;
import com.seeyon.v3x.edoc.util.Constants;
import com.seeyon.v3x.edoc.util.SharedWithThreadLocal;
import com.seeyon.v3x.edoc.webmodel.EdocMarkModel;

/**
 * 类描述：
 * 创建日期：
 *
 * @author liaoj
 * @version 1.0 
 * @since JDK 5.0
 */
public class EdocMarkHistoryManagerImpl implements EdocMarkHistoryManager{
	private static final Log LOG = CtpLogFactory.getLog(EdocMarkHistoryManagerImpl.class);
	
	private EdocMarkHistoryDAO edocMarkHistoryDAO;
	
	private EdocMarkDAO edocMarkDAO;
	
	private EdocMarkAclDAO edocMarkAclDAO;
	
	private EdocMarkManager edocMarkManager;
	
	private EdocMarkDefinitionManager edocMarkDefinitionManager;
	
	private EdocMarkCategoryManager edocMarkCategoryManager;
	
	private EdocMarkReserveManager edocMarkReserveManager;
	private ConfigManager configManager;
	
	public void setConfigManager(ConfigManager configManager) {
		this.configManager = configManager;
	}

	public void setEdocMarkCategoryManager(
			EdocMarkCategoryManager edocMarkCategoryManager) {
		this.edocMarkCategoryManager = edocMarkCategoryManager;
	}
	
	public EdocMarkDAO getEdocMarkDAO() {
		return edocMarkDAO;
	}



	public void setEdocMarkDAO(EdocMarkDAO edocMarkDAO) {
		this.edocMarkDAO = edocMarkDAO;
	}



	public void setEdocMarkHistoryDAO(EdocMarkHistoryDAO edocMarkHistoryDAO) {
		this.edocMarkHistoryDAO = edocMarkHistoryDAO;
	}
	
	public void setEdocMarkAclDAO(EdocMarkAclDAO edocMarkAclDAO) {
		this.edocMarkAclDAO = edocMarkAclDAO;
	}

	public List<EdocMarkHistory> findListByMarkDefineId(Long markDefineId) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("markDefineId", markDefineId);
		return (List<EdocMarkHistory>)edocMarkHistoryDAO.find("from EdocMarkHistory where markDefId = :markDefineId", paramMap);
	}
	
	/**
     * 方法描述：保存公文文号历史
     */
    public void save(EdocMarkHistory edocMarkHistory){
    	this.edocMarkHistoryDAO.save(edocMarkHistory);
    }
    public void save(List<EdocMarkHistory> list){
    	this.edocMarkHistoryDAO.saveAll(list);
    }
    
    
    /**
     * @方法描述: 根据公文id查找文号id
     * @param summaryId 公文Id
     */
    
    public Long findMarkIdBySummaryId(Long summaryId){
    	
    	List<EdocMark> edocMark = edocMarkDAO.findBy("edocId", summaryId);
    	
    	if(null!=edocMark && edocMark.size()>0){
    		return edocMark.get(0).getId();
    	}else{

    		return null;
    	}
    }
    
    public List<EdocMark> findMarkBySummaryId(Long summaryId)
    {
    	return edocMarkDAO.findBy("edocId", summaryId);
    }
    
    public List<EdocMark> findMarkBySummaryId(Long summaryId, Integer markType) {
    	return edocMarkDAO.findMarkBySummaryId(summaryId, markType);
    }

    /**
     * 方法描述： 封发后将edocMark转移到edocMarkHistory
     */
    public void afterSend(EdocSummary summary){
    	Long summaryId=summary.getId();
    	String docMark=summary.getDocMark();
    	User user = AppContext.getCurrentUser();    	
    	Long userId = user.getId();    	
    	//List<EdocMark> marks=this.findMarkBySummaryId(summaryId);
    	List<EdocMark> marks=null;
    	if(summary.getIsunit())
    	{//联合发文
    		marks=edocMarkDAO.findEdocMarkByEdocIdOrDocMark(summaryId,docMark,summary.getDocMark2());
    	}
    	else
    	{
    		marks=edocMarkDAO.findEdocMarkByEdocIdOrDocMark(summaryId,docMark);
    	}
    	List<EdocMark> marksAll=new ArrayList<EdocMark>();
    	if(marks==null||marks.size()==0){return;}
    	marksAll.addAll(marks);
    	int codeMode=0;//小流水0，大流水1
    	List<Long> categoryids=new ArrayList<Long>();
    	for(EdocMark mark:marks){
    		categoryids.add(mark.getCategoryId());
    	}
    	Map<Long,Integer> categoryMap=edocMarkCategoryManager.findByIds(categoryids);
    	for(EdocMark mark:marks)
    	{
    		//手写的情况不需要根据CategoryId和当前号进行查找。
    		if(mark.getCategoryId()==null||mark.getCategoryId()==0)continue;
    		//小流水不需要根据categoryID来查找。
    		if(categoryMap.get(mark.getCategoryId())!=null){
    			codeMode=categoryMap.get(mark.getCategoryId());
    		}
    		if(codeMode==0)continue;
    		List<EdocMark> marksTemp=edocMarkManager.findByCategoryAndNo(mark.getCategoryId(),mark.getDocMarkNo());
    		if(marksTemp!=null && marksTemp.size()>0)
    		{
    			marksAll.addAll(marksTemp);
    		}
    	}
    	seveEdocMarkHistory(marksAll,userId,summary,codeMode);
    }
    
    /**
     * 获取已有的EdocMark
     * @param summary
     * @return
     */
    private List<EdocMark> getNewEdocMarkList(EdocSummary summary, Integer codeMode) {
    	Long summaryId=summary.getId();
    	String docMark=summary.getDocMark();
    	List<EdocMark> marks=null;
    	if(summary.getIsunit())
    	{//联合发文
    		marks=edocMarkDAO.findEdocMarkByEdocIdOrDocMark(summaryId,docMark,summary.getDocMark2());
    	}
    	else
    	{
    		marks=edocMarkDAO.findEdocMarkByEdocIdOrDocMark(summaryId,docMark);
    	}
    	List<EdocMark> marksAll=new ArrayList<EdocMark>();
    	if(marks==null||marks.size()==0){return null;}
    	marksAll.addAll(marks);
    	List<Long> categoryids=new ArrayList<Long>();
    	for(EdocMark mark:marks){
    		categoryids.add(mark.getCategoryId());
    	}
    	Map<Long,Integer> categoryMap=edocMarkCategoryManager.findByIds(categoryids);
    	for(EdocMark mark:marks)
    	{
    		//手写的情况不需要根据CategoryId和当前号进行查找。
    		if(mark.getCategoryId()==null||mark.getCategoryId()==0)continue;
    		//小流水不需要根据categoryID来查找。
    		if(categoryMap.get(mark.getCategoryId())!=null){
    			codeMode=categoryMap.get(mark.getCategoryId());//小流水0，大流水1
    		}
    		if(codeMode==0)continue;
    		List<EdocMark> marksTemp=edocMarkManager.findByCategoryAndNo(mark.getCategoryId(),mark.getDocMarkNo());
    		if(marksTemp!=null && marksTemp.size()>0)
    		{
    			marksAll.addAll(marksTemp);
    		}
    	}
    	return marksAll;
    }
    
    public void seveEdocMarkHistory(List<EdocMark> edocMarks,Long userId,EdocSummary summary,int codeMode){
    	List <Long> markIds=new ArrayList<Long>();
    	for(EdocMark edocMark:edocMarks)
    	{
    		if(markIds.contains(edocMark.getId())){continue;}
    		else{markIds.add(edocMark.getId());}
    		
    		//生成公文文号历史对象
        	EdocMarkHistory edocMarkHistory = new EdocMarkHistory();
        	edocMarkHistory.setIdIfNew();
        	edocMarkHistory.setEdocId(summary.getId());
        	if(codeMode==0){//小流水
        		//文单上没有doc_mark，只有doc_mark2的时候，summary.getDocMark()为空，保存的时候报错。
        		//或者可以直接取edocMark.getDocMark()，不取summary.getDocMark().就没有下面这个分支了，暂时先写成这样吧。
        		if(summary.getDocMark()==null){
        			edocMarkHistory.setDocMark(edocMark.getDocMark());
        		}else{
        			edocMarkHistory.setDocMark(summary.getDocMark());
        		}
        	}else{//大流水
        		edocMarkHistory.setDocMark(edocMark.getDocMark());
        	}
        	edocMarkHistory.setDocMarkNo(edocMark.getDocMarkNo());
        	edocMarkHistory.setEdocMarkDefinition(edocMark.getEdocMarkDefinition());
        	edocMarkHistory.setCompleteTime(new Date());
        	edocMarkHistory.setCreateTime(edocMark.getCreateTime());
        	edocMarkHistory.setCreateUserId(edocMark.getCreateUserId());
        	edocMarkHistory.setLastUserId(userId);
        	edocMarkHistory.setMarkNum(edocMark.getMarkNum());
        	edocMarkHistory.setGovdocType(edocMark.getGovdocType());
        	//edocMarkHistory.setTransferStatus(summary.getTransferStatus());
        	//TODO
        	this.save(edocMarkHistory);
        	//若已分送，从断号中删除
        	/*if(summary.getTransferStatus() >= 2) {
        		edocMarkManager.deleteEdocMark(edocMark.getId());
        	}*/
        	//TODO
    	}
    }
    /**
     * 将公文文号保存到历史表，并删除此文号
     * @param edocMark  公文文号对象
     * @param userId  公文文号使用人id
     */
    public void seveEdocMarkHistory(EdocMark edocMark,Long userId){
    	
//    	if(!this.isUsed(edocMark.getEdocId(), edocMark.getDocMark())){
    		
    	//从公文文号表中删除
    	if(edocMark == null) {
    		return;
    	}

    	edocMarkManager.deleteEdocMark(edocMark.getId());
    	//生成公文文号历史对象
    	EdocMarkHistory edocMarkHistory = new EdocMarkHistory();
    	edocMarkHistory.setIdIfNew();
    	edocMarkHistory.setEdocId(edocMark.getEdocId());
    	edocMarkHistory.setDocMark(edocMark.getDocMark());
    	edocMarkHistory.setEdocMarkDefinition(edocMark.getEdocMarkDefinition());
    	edocMarkHistory.setCompleteTime(new Date());
    	edocMarkHistory.setCreateTime(edocMark.getCreateTime());
    	edocMarkHistory.setCreateUserId(edocMark.getCreateUserId());
    	edocMarkHistory.setLastUserId(userId);
    	edocMarkHistory.setMarkNum(edocMark.getMarkNum());
    	this.save(edocMarkHistory);
//    	}
    }



	public EdocMarkManager getEdocMarkManager() {
		return edocMarkManager;
	}



	public void setEdocMarkManager(EdocMarkManager edocMarkManager) {
		this.edocMarkManager = edocMarkManager;
	}
	
	/**
     * 保存公文文号历史
     * @param edocId
     * @param edocMark
     * @param markDefinitionId
     * @param markNum
     * @param createUserId
     * @param lastUserId
     */
    public void save(Long edocId,String edocMark,Long markDefinitionId,int markNum,int govdocType,Long createUserId,Long lastUserId,boolean checkId,boolean autoIncrement) throws EdocMarkHistoryExistException{
    	this.save(edocId,-1,edocMark,markDefinitionId,markNum,govdocType,createUserId,lastUserId,checkId,autoIncrement);
    }
    
    public void save(Long edocId, Integer currentNo, String edocMark,Long markDefinitionId,int markNum,int govdocType,Long createUserId,Long lastUserId,boolean checkId,boolean autoIncrement) throws EdocMarkHistoryExistException{
    	save(null, edocId, currentNo, edocMark, markDefinitionId, markNum, govdocType, createUserId, lastUserId, checkId, autoIncrement);
    }
    
	public void save(EdocSummary summary, Long edocId, Integer currentNo, String edocMark,Long markDefinitionId,int markNum,int govdocType,Long createUserId,Long lastUserId,boolean checkId,boolean autoIncrement) throws EdocMarkHistoryExistException{
		EdocMarkDefinition markDef = null;
		if(markDefinitionId!=null && markDefinitionId.longValue()!=0 && markDefinitionId.longValue()!=-1) {
			//从断号中获取，markDefinitionId为断号ID
			EdocMark mark = edocMarkManager.getEdocMark(markDefinitionId);
			if(mark != null) {
				markDefinitionId = mark.getEdocMarkDefinition().getId();
			}
			markDef = SharedWithThreadLocal.getMarkDefinition(markDefinitionId);
	    	if(markDef == null){
	    		markDef = edocMarkDefinitionManager.queryMarkDefinitionById(markDefinitionId);
	    	}
		}
		
		//断号进入占号
		Integer codeMode = 0;
		List<EdocMark> edocMarkList = null;
		if(summary != null) {
			edocMarkList = getNewEdocMarkList(summary, codeMode);
		}
		if(Strings.isNotEmpty(edocMarkList)) {
			seveEdocMarkHistory(edocMarkList, createUserId, summary,codeMode);
		} else {
			EdocMarkHistory edocMarkHistory = new EdocMarkHistory();
	    	edocMarkHistory.setIdIfNew();
	    	edocMarkHistory.setEdocId(edocId);
	    	edocMarkHistory.setDocMark(edocMark);
	    	edocMarkHistory.setDocMarkNo(currentNo);
			edocMarkHistory.setEdocMarkDefinition(markDef);
			edocMarkHistory.setCompleteTime(new Date());
	    	edocMarkHistory.setCreateTime(new Date());
	    	edocMarkHistory.setCreateUserId(createUserId);
	    	edocMarkHistory.setLastUserId(lastUserId);
	    	edocMarkHistory.setMarkNum(markNum);
	    	edocMarkHistory.setGovdocType(govdocType);
	    	this.save(edocMarkHistory);
		}
		
		if(markDef != null) {
	    	//内部文号且下拉的情况下需要更新当前选了几个了
			edocMarkManager.updateNextCurrentNo(markDef, currentNo);
    	}
		
		//释放断号
		edocMarkManager.deleteEdocMarkByEdocId(edocId);
		if(Strings.isNotBlank(edocMark)) {
			edocMarkManager.deleteEdocMarkByMarkstr(edocMark);
		}
	}
    
	public int increatementCurrentNo(EdocMarkDefinition markDef,int currentNo,EdocMarkCategory edocMarkCategory){
	
		//公文文号&小流水号支持预留文号
		
		int returnNo = currentNo;
		//int definitionMarkType = markDef.getMarkType().intValue();
		/*if(definitionMarkType == 0   //公文文号
	    	&& edocMarkCategory.getCodeMode().intValue()==0) {//小流水

//			if(currentNo >= edocMarkCategory.getCurrentNo()) {//V56-1-38 预留文号：生成新的当前文号序号
	    		Integer _currentNo = edocMarkReserveManager.autoMakeEdocMarkCurrentNo(markDef, currentNo);
	    		if(!_currentNo.equals(currentNo)){
	    			returnNo = _currentNo;
	    		}
//	    	}
			
    	} else {
    		if(currentNo >= edocMarkCategory.getCurrentNo()) {
    			returnNo = currentNo + 1;
    		}
    	}*/
		//因公文文号、内部文号、签收编号的大小流水都支持预留，所以这里需要放开
		Integer _currentNo = edocMarkReserveManager.autoMakeEdocMarkCurrentNo(markDef, currentNo);
		if(!_currentNo.equals(currentNo)){
			returnNo = _currentNo;
		}
		
		return returnNo;
	}
	
	
    private void setEdocMarkDefinitionPublished(EdocMarkDefinition markDef) {
		//设置已经使用。
    	if(markDef!=null && markDef.getStatus()!=null){
	    	if(markDef.getStatus().shortValue() == Constants.EDOC_MARK_DEFINITION_DRAFT){
	    		markDef.setStatus(Constants.EDOC_MARK_DEFINITION_PUBLISHED);
	    		edocMarkDefinitionManager.updateMarkDefinition(markDef);
//	    		edocMarkDefinitionManager.saveMarkDefinition(markDef);
	    	}
    	}
	}
    /**
     * 保存从断号中选择的文号，只用于签报
     * @param edocMarkId    断号id
     * @param edocId        公文id
     * @param markNum
     */
    public void saveMarkHistorySelectOld(Long edocMarkId,String edocMark,Long edocId,Long userId,boolean checkId) throws EdocMarkHistoryExistException{
    	
        boolean isUsed = edocMarkManager.isUsed(edocMark, checkId ? String.valueOf(edocId) : "0", String.valueOf(AppContext.currentAccountId()));
        if(isUsed){
            throw new EdocMarkHistoryExistException();
        }
    	EdocMark mark = this.edocMarkDAO.get(edocMarkId);
    	this.seveEdocMarkHistory(mark,edocId,userId);
    }

	public void setEdocMarkDefinitionManager(
			EdocMarkDefinitionManager edocMarkDefinitionManager) {
		this.edocMarkDefinitionManager = edocMarkDefinitionManager;
	}

	/**
     * 判断文号在历史表中是否使用
     * @param edocMark
     * @param edocId
     * @return true已使用；false未使用
     */
	public boolean isUsed(String edocMark,Long edocId) {
		if(Strings.isBlank(edocMark))
			return false;
		boolean flag = false;
		User user = AppContext.getCurrentUser();
	    List<EdocMarkAcl> list = this.edocMarkAclDAO.findMarkAclByMarkAndEdocId(edocMark, edocId);
	    OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
	    if(list != null && list.size() > 0){
	    	for(EdocMarkAcl acl : list){
	    		if("Account".equals(acl.getAclType())) {
	    			if(user.getLoginAccount().equals(acl.getDeptId())){
	    				flag = true;
	    				break;
	    			}
	    		}else if("Department".equals(acl.getAclType())){
	    		    //文号授权给其他部门A，模板绑定该文号，部门B下人员调用该模板时也可以使用该文号
	    		    //那么封发时是否已经使用了该文号，就要判断文号授权的部门对应的单位了
	    		    try {
	    		        long loginAccountId = user.getLoginAccount();
                        V3xOrgDepartment dep = orgManager.getDepartmentById(acl.getDeptId());
                        if(loginAccountId == dep.getOrgAccountId().longValue()){
                            flag = true;
                            break;
                        }
                    } catch (BusinessException e) {
                        LOG.error(e.getMessage(),e);
                    }
	    		}
	    	}
	    }
		return flag;
	}
	
	/**
     * 
     * @param edocMark
     * @param markDefId
     * @param edocId
     * @return
     */
    public boolean isUsedReserved(String edocMark, Long markDefId, Long edocId) {
    	if(Strings.isBlank(edocMark))
			return false;
		boolean flag = false;
		User user = AppContext.getCurrentUser();
	    List<EdocMarkAcl> list = this.edocMarkAclDAO.findMarkAclByMarkAndEdocId(edocMark, markDefId, edocId);
	    OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
	    if(list != null && list.size() > 0){
	    	for(EdocMarkAcl acl : list){
	    		if("Account".equals(acl.getAclType())) {
	    			if(user.getLoginAccount().equals(acl.getDeptId())){
	    				flag = true;
	    				break;
	    			}
	    		}else if("Department".equals(acl.getAclType())){
	    		    //文号授权给其他部门A，模板绑定该文号，部门B下人员调用该模板时也可以使用该文号
	    		    //那么封发时是否已经使用了该文号，就要判断文号授权的部门对应的单位了
	    		    try {
	    		        long loginAccountId = user.getLoginAccount();
                        V3xOrgDepartment dep = orgManager.getDepartmentById(acl.getDeptId());
                        if(loginAccountId == dep.getOrgAccountId().longValue()){
                            flag = true;
                            break;
                        }
                    } catch (BusinessException e) {
                        LOG.error(e.getMessage(),e);
                    }
	    		}
	    	}
	    }
		return flag;
    }
	
    private void seveEdocMarkHistory(EdocMark edocMark,Long edocId,Long userId){
    	//从公文文号表中删除
    	if(edocMark == null) {
    		return;
    	}
    	
    	edocMarkManager.deleteEdocMark(edocMark.getId());
    	//生成公文文号历史对象
    	EdocMarkHistory edocMarkHistory = new EdocMarkHistory();
    	edocMarkHistory.setIdIfNew();
    	edocMarkHistory.setEdocId(edocId);
    	edocMarkHistory.setDocMark(edocMark.getDocMark());
    	edocMarkHistory.setDocMarkNo(edocMark.getDocMarkNo());
    	edocMarkHistory.setEdocMarkDefinition(edocMark.getEdocMarkDefinition());
    	edocMarkHistory.setCompleteTime(new Date());
    	edocMarkHistory.setCreateTime(edocMark.getCreateTime());
    	edocMarkHistory.setCreateUserId(edocMark.getCreateUserId());
    	edocMarkHistory.setLastUserId(userId);
    	edocMarkHistory.setMarkNum(edocMark.getMarkNum());
    	edocMarkHistory.setGovdocType(edocMark.getGovdocType());
    	this.save(edocMarkHistory);
    }
    
    /**
     * @方法描述: 根据公文id删除文号
     * @param summaryId 公文Id
     */
    
    public void deleteMarkIdBySummaryId(Long summaryId){
    	edocMarkHistoryDAO.deleteMarkIdBySummaryId(summaryId);
    }

    @Override
	public void updateMarkHistoryTransferStatus(Integer transferStatus, Long summaryId) throws BusinessException {
    	edocMarkHistoryDAO.updateMarkHistoryTransferStatus(transferStatus, summaryId);
	}

    /**
	 * 保存公文占号、清除断号、跳转(2070721)
	 * @param edocParam
	 * @param model
	 * @param currentUser
	 * @throws EdocMarkHistoryExistException
	 */
    public void saveMarkHistoryNew(EdocParam edocParam, EdocMarkModel model, User currentUser) throws EdocMarkHistoryExistException {
    	Long userId = currentUser.getId();
    	Long summaryId = edocParam.getSummaryId();
    	Integer govdocType = edocParam.getGovdocType();
    	Long definitionId = model.getMarkDefinitionId();
    	Integer currentNo = model.getCurrentNo();
    	String markstr = model.getMark();
    	
    	boolean needUpdateMark = true;//是否需要修改
    	boolean needZhanghao = !"draft".equals(edocParam.getAction());//是否需要占号，保存待发不占号
    	
    	Integer createMode = model.getDocMarkCreateMode();//0:未选择文号，1：下拉选择的文号，2：选择的断号，3.手工输入 4.预留文号
    	int markType = GovdocUtil.getMarkTypeValueByType(edocParam.getMarkType().getKey());
    	if(markType == EdocEnum.MarkType.edocMark.ordinal()) {//公文文号	    	
			if(createMode == 2) {//选择断号
		    	EdocMark mark = edocMarkManager.getEdocMark(definitionId);
				if(mark != null) {
					definitionId = mark.getEdocMarkDefinition().getId();
				}
			}
    	} else {
    		EdocMarkHistory markHistory = this.getMarkHistoryByEdocID(summaryId);
			if(markHistory != null) {
				//当文号相同、或者手写的文号相同，则不需要更改文号相关信息
				if(markHistory.getDocMark().equals(markstr) && 
						(markHistory.getEdocMarkDefinition()!=null && markHistory.getEdocMarkDefinition().getId().longValue()==definitionId.longValue()
						|| markHistory.getEdocMarkDefinition()==null && createMode==3)) {
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
		
		//断号进入占号
		Integer codeMode = 0;
		List<EdocMarkHistory> markHistoryList = new ArrayList<EdocMarkHistory>();
		
		List<EdocMark> lastMarkList = null;
		//公文文号才有断号
		if(markType == EdocEnum.MarkType.edocMark.ordinal()) {//公文文号
			if(edocParam.getSummary() != null) {
				lastMarkList = getLastMark(edocParam.getSummary(), markstr, codeMode);
			}
		}
		if(needZhanghao) {
			if(Strings.isNotEmpty(lastMarkList)) {
				markHistoryList.addAll(createMarkHistory(lastMarkList, markstrList, edocParam.getSummary(), userId, codeMode));
			} else {
				int markNum = 1;
				EdocMarkHistory edocMarkHistory = new EdocMarkHistory();
		    	edocMarkHistory.setIdIfNew();
		    	edocMarkHistory.setEdocId(summaryId);
		    	edocMarkHistory.setDocMark(markstr);
		    	edocMarkHistory.setDocMarkNo(currentNo);
				edocMarkHistory.setEdocMarkDefinition(markDef);
				edocMarkHistory.setCompleteTime(new Date());
		    	edocMarkHistory.setCreateTime(new Date());
		    	edocMarkHistory.setCreateUserId(userId);
		    	edocMarkHistory.setLastUserId(userId);
		    	edocMarkHistory.setMarkNum(markNum);
		    	edocMarkHistory.setGovdocType(govdocType);
		    	markHistoryList.add(edocMarkHistory);
		    	
		    	if(markDef!=null && markDef.getEdocMarkCategory()!=null) {
		    		edocMarkCategory = markDef.getEdocMarkCategory();
		    		List<EdocMarkDefinition> mds = edocMarkDefinitionManager.getEdocMarkDefinitionsByCategory(edocMarkCategory.getId());
			    	if(mds!=null && mds.size()>1) {//多个公文模板共用一个流水号
			    		for(EdocMarkDefinition def : mds) {
			    			if(markDef.getId().longValue()==def.getId().longValue()) {
			    				continue;
			    			}
			    			markstr = edocMarkDefinitionManager.markDef2Mode(def,null,currentNo).getMark();
			    			if(Strings.isNotBlank(markstr)) {
			    				if(!markstrList.contains(markstr)) {
			    					markstrList.add(markstr);
			    				}
			    			}
			    			
			    			edocMarkHistory = new EdocMarkHistory();
			    	    	edocMarkHistory.setIdIfNew();
			    	    	edocMarkHistory.setEdocId(summaryId);
			    	    	edocMarkHistory.setDocMark(markstr);
			    	    	edocMarkHistory.setDocMarkNo(currentNo);
			    			edocMarkHistory.setEdocMarkDefinition(markDef);
			    			edocMarkHistory.setCompleteTime(new Date());
			    	    	edocMarkHistory.setCreateTime(new Date());
			    	    	edocMarkHistory.setCreateUserId(userId);
			    	    	edocMarkHistory.setLastUserId(userId);
			    	    	edocMarkHistory.setMarkNum(markNum);
			    	    	edocMarkHistory.setGovdocType(govdocType);
			    	    	markHistoryList.add(edocMarkHistory);
			    		}
			    	}	
		    	}
			}
			
			//释放断号
			if(markType == EdocEnum.MarkType.edocMark.ordinal()) {//公文文号
				edocMarkManager.deleteEdocMarkByEdocId(summaryId);
				if(Strings.isNotEmpty(markstrList)) {
					edocMarkManager.deleteEdocMarkByMarkstr(markstrList);
				}
			} else {
				this.deleteMarkHistoryByEdocId(summaryId);
				if(Strings.isNotEmpty(markstrList)) {
					this.deleteMarkHistoryByMarkstr(markstrList);
				}
			}
		}
		//发文文号、内部文号公文文号按最大值流水开关开启时，签报文号最大值+1
		boolean docMarkMaxSwitchOn=false;
		/*if(markType == EdocEnum.MarkType.edocMark.ordinal()){
			ConfigItem configItem = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, IConfigPublicKey.DOC_MARK_BY_MAX, currentUser.getAccountId());
			docMarkMaxSwitchOn = configItem!=null&&"yes".equals(configItem.getConfigValue());
			//公文文号放入断号
			if(docMarkMaxSwitchOn && markDef!=null && markDef.getEdocMarkCategory()!=null){
				List<EdocMark> markList = new ArrayList<EdocMark>();
	    		for(int tempCurrentNo=markDef.getEdocMarkCategory().getCurrentNo();tempCurrentNo<currentNo;tempCurrentNo++){
	    			markstr=edocMarkDefinitionManager.markDef2Mode(markDef,null,tempCurrentNo).getMark();
	    			//如果文号已占用，不放入断号
	    			boolean isUsed = this.isUsed(markstr, -1L);
	    			if(isUsed){
	    				continue;
	    			}
	    			markstrList.add(markstr);
	    			
	    			EdocMark tempMark = new EdocMark();
		    		tempMark.setIdIfNew();
		    		tempMark.setMemo(edocParam.getDocMark());
		    		tempMark.setEdocMarkDefinition(markDef);
		    		tempMark.setCreateTime(new Date());
		    		tempMark.setEdocId(summaryId);
		    		
		    		tempMark.setDocMark(markstr);
		    		tempMark.setCreateUserId(userId);
		    		tempMark.setStatus(Constants.EDOC_MARK_USED);    	
		    		tempMark.setDocMarkNo(tempCurrentNo);
		        	if(markDef != null && markDef.getEdocMarkCategory()!=null) {
		    	    	edocMarkCategory = markDef.getEdocMarkCategory();
		    	    	tempMark.setCategoryId(edocMarkCategory.getId());
		        	}
		        	tempMark.setDomainId(currentUser.getLoginAccount());
		        	tempMark.setMarkNum(-1);
		        	tempMark.setGovdocType(edocParam.getGovdocType());
		        	markList.add(tempMark);
	    		}
	    		if(!markList.isEmpty()){
	    			edocMarkManager.save(markList);
	    		}
			}
		}else if(markType == EdocEnum.MarkType.edocInMark.ordinal()){
			ConfigItem configItem = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, IConfigPublicKey.INNER_MARK_BY_MAX, currentUser.getAccountId());
			docMarkMaxSwitchOn = configItem!=null&&"yes".equals(configItem.getConfigValue());
		}*/
		//TODO
		if(markDef != null && currentNo>=markDef.getEdocMarkCategory().getCurrentNo()) {
	    	//内部文号且下拉的情况下需要更新当前选了几个了
			edocMarkManager.updateNextCurrentNo(markDef, currentNo,docMarkMaxSwitchOn);
    	}
		
		this.save(markHistoryList);
	}
    
    /**
     * 获取文号断号(2070721)
     * @param summary
     * @return
     */
    private List<EdocMark> getLastMark(EdocSummary summary, String markstr, Integer codeMode) {
    	List<EdocMark> marksAll = new ArrayList<EdocMark>();
    	
    	Long summaryId = summary.getId();
    	List<EdocMark> marks = null;
    	if(summary.getIsunit()) {//联合发文
    		marks = edocMarkDAO.findEdocMarkByEdocIdOrDocMark(summaryId, markstr, summary.getDocMark2());
    	} else {
    		marks = edocMarkDAO.findEdocMarkByEdocIdOrDocMark(summaryId, markstr);
    	}
    	if(Strings.isEmpty(marks)) {
    		return null;
    	}
    	
    	List<Long> categoryids = new ArrayList<Long>();
    	for(EdocMark mark : marks) {
    		categoryids.add(mark.getCategoryId());
    	}
    	
    	Map<Long,Integer> categoryMap = edocMarkCategoryManager.findByIds(categoryids);
    	for(EdocMark mark : marks) {
    		//手写的情况不需要根据CategoryId和当前号进行查找。
    		if(mark.getCategoryId()==null||mark.getCategoryId()==0) {
    			continue;
    		}
    		//小流水不需要根据categoryID来查找。
    		if(categoryMap.get(mark.getCategoryId()) != null) {
    			codeMode = categoryMap.get(mark.getCategoryId());//小流水0，大流水1
    		}
    		if(codeMode==0) {
    			continue;
    		}
    		List<EdocMark> marksTemp = edocMarkManager.findByCategoryAndNo(mark.getCategoryId(), mark.getDocMarkNo());
    		if(marksTemp!=null && marksTemp.size()>0) {
    			marksAll.addAll(marksTemp);
    		}
    	}
    	
    	if(Strings.isEmpty(marksAll)) {
    		marksAll.addAll(marks);
    	}
    	
    	return marksAll;
    }
    
    /**
     * 获取新建的公文文号占号(2070721)
     * @param markList
     * @param markstrList
     * @param summary
     * @param userId
     * @param codeMode
     * @return
     */
    private List<EdocMarkHistory> createMarkHistory(List<EdocMark> markList, List<String> markstrList, EdocSummary summary, Long userId, int codeMode) {
    	List<EdocMarkHistory> markHistoryList = new ArrayList<EdocMarkHistory>();
    	
    	List <Long> markIds = new ArrayList<Long>();
    	for(EdocMark edocMark : markList) {
    		if(markIds.contains(edocMark.getId())) {
    			continue;
    		}
    		markIds.add(edocMark.getId());
    		
    		//生成公文文号历史对象
        	EdocMarkHistory edocMarkHistory = new EdocMarkHistory();
        	edocMarkHistory.setIdIfNew();
        	edocMarkHistory.setEdocId(summary.getId());
        	if(codeMode == 0) {//小流水
        		//文单上没有doc_mark，只有doc_mark2的时候，summary.getDocMark()为空，保存的时候报错。
        		//或者可以直接取edocMark.getDocMark()，不取summary.getDocMark().就没有下面这个分支了，暂时先写成这样吧。
        		if(summary.getDocMark() == null) {
        			edocMarkHistory.setDocMark(edocMark.getDocMark());
        		} else {
        			edocMarkHistory.setDocMark(summary.getDocMark());
        		}
        	} else {//大流水
        		edocMarkHistory.setDocMark(edocMark.getDocMark());
        	}
        	edocMarkHistory.setDocMarkNo(edocMark.getDocMarkNo());
        	edocMarkHistory.setEdocMarkDefinition(edocMark.getEdocMarkDefinition());
        	edocMarkHistory.setCompleteTime(new Date());
        	edocMarkHistory.setCreateTime(edocMark.getCreateTime());
        	edocMarkHistory.setCreateUserId(edocMark.getCreateUserId());
        	edocMarkHistory.setLastUserId(userId);
        	edocMarkHistory.setMarkNum(edocMark.getMarkNum());
        	edocMarkHistory.setGovdocType(edocMark.getGovdocType());
        	//edocMarkHistory.setTransferStatus(summary.getTransferStatus());
        	//TODO
        	markHistoryList.add(edocMarkHistory);
        	
			if(!markstrList.contains(edocMarkHistory.getDocMark())) {
				markstrList.add(edocMarkHistory.getDocMark());
			}
    	}
    	return markHistoryList;
    }
    
    /**
     * 删除公文占号(2070721)
     * @param edocId
     */
    public void deleteMarkHistoryByEdocId(long edocId) {
    	this.deleteMarkHistoryByEdocId(edocId, null);
    }
    public void deleteMarkHistoryByEdocId(long edocId, Integer markType) {
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
     * 删除公文占号(2070721)
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
    
    public EdocMarkHistory getMarkHistoryByEdocID(Long summaryId) {
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("summaryId", summaryId);
    	List list = DBAgent.find("from EdocMarkHistory where edocId=:summaryId order by createTime desc", paramMap);
    	if(Strings.isNotEmpty(list)) { 
    		return (EdocMarkHistory)list.get(0);
    	}
    	return null;    	
    }
    
    public List<EdocMarkModel> deleteMarkHistoryNew(EdocMarkHistory markHistory) {
    	List<EdocMarkModel> markModelList = new ArrayList<EdocMarkModel>();
    	List<String> markstrList = new ArrayList<String>();
    	markstrList.add(markHistory.getDocMark());
    	if(markHistory.getEdocMarkDefinition() != null) {
    		List<EdocMarkDefinition> markDefList = edocMarkDefinitionManager.getEdocMarkDefinitionsByCategory(markHistory.getEdocMarkDefinition().getCategoryId());
			if(Strings.isNotEmpty(markDefList)) {
				for(EdocMarkDefinition bean : markDefList) {
					EdocMarkModel model = edocMarkDefinitionManager.markDef2Mode(bean, markHistory.getDocMarkNo());
					if(model != null && Strings.isNotBlank(model.getMark())) {
						if(!markstrList.contains(model.getMark())) {
							markstrList.add(model.getMark());
						}
						model.setMarkDef(bean);
						markModelList.add(model);
					}
				}		
			}
		}
    	if(Strings.isNotEmpty(markstrList)) {
			this.deleteMarkHistoryByMarkstr(markstrList);
			this.edocMarkManager.deleteEdocMarkByMarkstr(markstrList);
		}
		if(markHistory!=null && markHistory.getEdocId()!=null) {
  			edocMarkHistoryDAO.deleteEdocMarkHistoryByEdocId(markHistory.getEdocId());
		}
		return markModelList;
    }
    
	public void setEdocMarkReserveManager(EdocMarkReserveManager edocMarkReserveManager) {
		this.edocMarkReserveManager = edocMarkReserveManager;
	}

	@Override
	public List<EdocMarkHistory> getMarkHistorysByEdocID(Long summaryId) {
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("summaryId", summaryId);
    	List<EdocMarkHistory> list = DBAgent.find("from EdocMarkHistory where edocId=:summaryId order by completeTime desc", paramMap);
    	return list;    	
	}
	public List<EdocMarkHistory> getMarkHistorysByEdocID(Long summaryId, Integer markType) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("summaryId", summaryId);
    	paramMap.put("markType", markType);
    	List<EdocMarkHistory> list = DBAgent.find("from EdocMarkHistory where edocId=:summaryId and markType=:markType order by completeTime desc", paramMap);
    	return list;
	}

	@Override
	public void deleteMarkByModel(EdocMarkHistory edocMarkHistory) {
		edocMarkHistoryDAO.delete(edocMarkHistory);	
	}

	@Override
	public void save(Long edocId, String edocMark, Long markDefinitionId, int markNum, Long createUserId,
			Long lastUserId, boolean checkId, boolean autoIncrement) throws EdocMarkHistoryExistException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void save(Long edocId, Integer currentNo, String edocMark, Long markDefinitionId, int markNum,
			Long createUserId, Long lastUserId, boolean checkId, boolean autoIncrement)
			throws EdocMarkHistoryExistException {
		// TODO Auto-generated method stub
		
	}
    
}
