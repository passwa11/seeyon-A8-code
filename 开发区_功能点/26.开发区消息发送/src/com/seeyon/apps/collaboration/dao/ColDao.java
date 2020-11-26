package com.seeyon.apps.collaboration.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.collaboration.bo.QuerySummaryParam;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.vo.ColSummaryVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.supervise.vo.SuperviseModelVO;
import com.seeyon.ctp.util.FlipInfo;

public interface ColDao {
    public void saveColSummary(ColSummary colSummary);
    public void saveColInfo();
    
    /**
   	 * 根据id获取ColSummary对象
   	 * @param id
   	 * @return
   	 */
   	public ColSummary getColSummaryById(Long id);
   	public ColSummary getColSummaryByIdHis(Long id);
   	
   	/**
     * 根据表单数据ID批量查找协同id
     * 
     * @param formRecords 表单数据Id列表
     * @return
     * @throws BusinessException
     *
     * @Since A8-V5 7.0
     */
   	public ColSummary getColSummaryByFormRecordId(Long fromRecordId);
   	public Map<Long, Long> getColSummaryIdByFormRecordIds(List<Long> formRecords);
    public void updateColSummary(ColSummary colSummary);
    public void updateColSummarys(List<ColSummary> colSummarys);
    public void deleteColSummary(ColSummary colSummary);
    public List<ColSummaryVO> queryByCondition(FlipInfo flipInfo,Map<String,String> condition) throws BusinessException ;
    
    /**
     * 查询转储数据
     * @param flipInfo
     * @param condition
     * @return
     * @throws BusinessException
     */
    public List<ColSummaryVO> queryByConditionHis(FlipInfo flipInfo,Map<String,String> condition) throws BusinessException ;
    public int countByCondition(Map<String, String> condition) throws BusinessException;
	public void deleteColSummaryById(Long id);
	/**
	 * 根据流程Id得到summary信息
	 * @param processId
	 * @return
	 */
	public ColSummary getColSummaryByProcessId(Long processId) throws BusinessException;
	/**
	 * 根据表单ID获取所有的协同的数据
	 * @param formAppId
	 * @return
	 * @throws BusinessException
	 */
	public List<ColSummary> getColSummarysByFormAppId(Long formAppId) throws BusinessException;
	/**
	 * 根据caseId得到对应的summary
	 * @param caseId
	 * @return
	 * @throws ColException
	 */
    public ColSummary getSummaryByCaseId(Long caseId) throws BusinessException;
	/**
     * 项目协同更多条件查询
     */
    public FlipInfo getColSummaryByCondition(FlipInfo flipInfo, Map<String, String> queryMap) throws BusinessException ;
    /**
	  * 在业务配置场景中，获取指定状态、表单模板对应的协同事项总数
	  * @param state		事务状态：待办、已办、已发
	  * @param templeteIds  用户进行表单业务配置时选中的表单模板
	  */
	public int getColCount(int state,Long userId, List<Long> templeteIds) throws BusinessException;
	   /**
     * 根据搜索条件condition、field、field1和UserId、Status取得协同督办信息
     * @param userId
     * @param status
     * @return
     */
    public List<SuperviseModelVO> getColSuperviseModelList(FlipInfo filpInfo,Map<String,String> map);
    /**
     * 根据协同事项所有人id和表单模板id列表查询跟踪列表
     * @param memberId //协同处理人的id
     * @param formTemplateIds //表单模板的id列表
     * @return
     * @throws BusinessException
     */
    public List<ColSummaryVO>  getTrackList4BizConfig(Long memberId,List<Long> formTemplateIds) throws BusinessException ;
    
    /**
     * yangwulin Sprint5  2012-12-04  全文检索
     * 获取在某一段时期内，某一板块下已发布的协同总数
     * @param beginDate 开始时间
     * @param endDate 结束时间
     * @return
     * @throws BusinessException
     */
    public Integer findIndexResumeCount(Date beginDate, Date endDate,boolean isForm) throws BusinessException;
    
    /**
     * yangwulin Sprint5  2012-12-04  全文检索
     * 
     * @param starDate
     * @param endDate
     * @param firstRow
     * @param pageSize
     * @return
     * @throws BusinessException
     */
    public List<Long> findIndexResumeIDList(Date starDate, Date endDate, Integer firstRow, Integer pageSize,boolean isForm) throws BusinessException;
    
	public List<ColSummaryVO> getMyCollDeadlineNotEmpty(Map<String, Object> tempMap) throws BusinessException;
	
	public List selectPageWorkflowDataByCondition(Map<String,Object> conditionParam, FlipInfo fi) throws BusinessException;


	public List selectWorkflowDataByCondition(Map<String,Object> conditionParam) throws BusinessException;
	
	
    /**
     * 流程结束时调用此方法更新一些状态(例如：结束时间)
     * @param summary
     * @throws BusinessException
     */
    public void transSetFinishedFlag(ColSummary summary) throws BusinessException;
   
    /**
     * 查询归档的信息，级联查询ctp_affair和doc_resource表
     * @param flipInfo
     * @param query
     * @return
     * @throws BusinessException
     */
    public List<ColSummaryVO> getArchiveAffair(FlipInfo flipInfo, Map<String, String> query) throws BusinessException;
    /**
     * 根据协同summary id列表获得多个协同对象(首页更多显示当前待办人使用)
     * @param ids
     * @return
     * @throws BusinessException
     */
    public List<ColSummary> findColSummarysByIds(List<Long> ids) throws BusinessException;
    
    /**
     * 找到一个协同的所有的转发的协同
     * @param ids
     * @return
     * @throws BusinessException
     */
    public List<ColSummary> findColSummarysByParentId(Long parentId) throws BusinessException;
    
    
	public List<ColSummaryVO> getSummaryByTemplateId(Long templateId,Integer state)throws BusinessException;
	
	/**
	 * 查询关联文档已发、已办、待办的总数
	 * @param condition
	 * @return
	 * @throws BusinessException
	 */
	public Map<String,Integer> getColQuoteCount(Map<String, String> condition) throws BusinessException;
	
	/**
     * 查询Summary
     * @Author      : xuqw
     * @Date        : 2016年1月25日下午4:21:56
     * @param param
     * @param flip
     * @return
     * @throws BusinessException
     */
	public List<ColSummary> findColSummarys(QuerySummaryParam param, FlipInfo flip);
	
	/**
	 * 数据关联查询协同数据
	 * @param flipInfo
	 * @param condition
	 * @param isCounter
	 * @return
	 * @throws BusinessException
	 */
	public List<ColSummaryVO> queryByCondition4DataRelation(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException; 
	
	/**
	 * 更新接受人节点
	 * @param processId
	 * @param NodeInfos
	 */
	public void updateColSummaryProcessNodeInfos(String processId,String nodeInfos);
	
	public void updateColSummaryProcessNodeInfos(Long summaryId,String nodeInfos);
	
	public void updateColSummaryReplyCounts(Long summaryId,int replyCounts);
	
	/**
     * 根据模板信息查询
     * @param flipInfo
     * @param condition
     * @return
     * @throws BusinessException
     */
    public FlipInfo getSummarysAndTemplates(FlipInfo flipInfo,Map<String,Object> condition) throws BusinessException ;
    /**
     * 获取已超期或者几天内超期的待办数据
     * @param tempMap
     * @return
     * @throws BusinessException
     */
    public List getOverdueOrDaysOverdueList(Long userId,List<Long> templateIds) throws BusinessException;
    
    public int getFromleader(List<Long> templeteIds) throws BusinessException;
    
    public int getMydeptDataCount(List<Long> templeteIds) throws BusinessException;
    
    public String getMyManagerTemplateCount() throws BusinessException;
    
    
}
