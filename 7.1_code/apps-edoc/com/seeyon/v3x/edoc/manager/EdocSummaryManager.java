package com.seeyon.v3x.edoc.manager;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.edoc.bo.SimpleEdocSummary;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.office.UserUpdateObject;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocSubjectWrapRecord;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.exception.EdocException;

public interface EdocSummaryManager {
	
	public EdocSummary findById(long id);
	
	public EdocSummary getEdocSummaryById(long summaryId, boolean needBody, boolean isLoadExtend) throws EdocException;
	
	/**
	 * 修改公文主表
	 * @param o
	 */
	public void updateEdocSummary(EdocSummary o);
	/**
	 * 修改公文主表(及扩展表数据)
	 * @param o
	 * @param isSaveExtend
	 */
	public void updateEdocSummary(EdocSummary o, boolean isSaveExtend);
	/**
	 * 保存公文主表
	 * @param o
	 */
	public void saveEdocSummary(EdocSummary o);
	/**
	 * 保存公文主表(及扩展表数据)
	 * @param o
	 */
	public void saveEdocSummary(EdocSummary o, boolean isSaveExtend);
	/**
	 * 保存公文主表
	 * @param o
	 */
	public void saveOrUpdateEdocSummary(EdocSummary o);
	/**
	 * 保存公文主表(及扩展表数据)
	 * @param o
	 */
	public void saveOrUpdateEdocSummary(EdocSummary o, boolean isSaveExtend);
	
	/**
	 * 物理删除公文主表及扩展字段
	 * @param summaryId
	 */
	public void deletePhysicalEdocSummary(Long summaryId);
	
	public void updateEdocSummaryState(Long edocId, int state);
	
	public void updateEdocSummaryCoverTime(Long edocId, boolean isCoverTime);
	
	public EdocSummary getSummaryByProcessId(String processId);
	
	/**
	 * 根据内部文号判断文号内部文号是否已经使用
	 * @param serialNo  内部文号
	 * @return (1：存在  0：不存在)
	 */
	public int checkSerialNoExsit(String serialNo,Long loginAccount);
	
	
	public  boolean deleteUpdateObj(String objId, String userId) ;
	public  boolean addUpdateObj(UserUpdateObject uo);
	/**
	 * 根据内部文号判断文号内部文号是否已经使用
	 * @param summaryId  公文ID
	 * @param serialNo   内部文号
	 * @param loginAccount  登录单位
	 * @return (1：存在  0：不存在)
	 */
	public int checkSerialNoExsit(String summaryId,String serialNo,Long loginAccount);
	  //附件转正文
    public Attachment getAttsList(EdocBody body,String transmitSendNewEdocId,Date createDate,EdocSummary summary)  throws BusinessException, FileNotFoundException ;
	public UserUpdateObject editObjectState(String objId);
	
	/**
	 * @param templeteId  : 模板ID
	 * @param workFlowState ： 流程状态
	 * @param startDate : 开始时间
	 * @param endDate ： 结束时间
	 * @return
	 */
	public List<EdocSummary> getEdocSummaryList(
			Long accountId,
			Long templeteId,
			List<Integer> workFlowState,Date startDate,Date endDate);
	
	
	/**
	 * 获得以公文结束时间作为查询开始或结束时间的列表，其他条件与getEdocSummaryList接口一致
	 * @param templeteId  : 模板ID
	 * @param workFlowState ： 流程状态
	 * @param startDate : 开始时间
	 * @param endDate ： 结束时间
	 * @return
	 */
	public List<EdocSummary> getEdocSummaryCompleteTimeList(
			Long accountId,
			Long templeteId,
			List<Integer> workFlowState,Date startDate,Date endDate);
	/**
	 * 根据模板得到此模板某段时间的实例数
	 * @param templeteId
	 * @param workFlowState
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public Integer getCaseCountByTempleteId (
			Long accountId,
			Long templeteId,
			List<Integer> workFlowState, Date startDate, Date endDate);
	/**
	 * 根据模板得到此模板某段时间的平均运行时长。
	 * @param templeteId
	 * @param workFlowState
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public Integer getAvgRunWorkTimeByTempleteId(
			Long accountId,
			Long templeteId,
			List<Integer> workFlowState, Date startDate, Date endDate);
	
	
	/**
	 * 处理时间大于基准时间.
	 * @param templeteId
	 * @param workFlowState
	 * @param startDate
	 * @param endDate
	 * @param standarduration
	 * @return
	 */
	public Integer  getCaseCountGTSD(
			Long accountId,
			Long templeteId,
			List<Integer> workFlowState, Date startDate, Date endDate,Integer standarduration);
	/**
	 * 得到某个模板某段时间的超期流程数
	 * @param accountId
	 * @param templeteId
	 * @param workFlowState
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public Double getOverCaseRatioByTempleteId(Long accountId,
			Long templeteId,
			List<Integer> workFlowState,
			Date startDate,
			Date endDate);
	
	/**
	 * 取消当前用户的公文标题多行显示（换行显示） --xiangfan
	 * @param AccountId 当前用户所在单位ID
	 * @param userId 当前用户ID
	 * @param listType 类型标示，详见EdocSubjectWrapRecord.java 的静态字段
	 * @param edocType 公文类型
	 */
	public void subjectWrapDisabled(Long AccountId, Long userId, int listType, int edocType);
	
	/**
	 * 设置当前用户的公文标题多行显示（换行显示）设置 --xiangfan
	 * @param subjectWrapRecord
	 */
	public void subjectWrapSetting(EdocSubjectWrapRecord subjectWrapRecord);
	
	/**
	 * 判断当前用户是否设置了公文的多行显示（换行显示）--xiangfan
	 * @param AccountId 当前单位
	 * @param userId 当前用户
	 * @param listType 列表类型，详见EdocSubjectWrapRecord.java 的静态字段
	 * @param edocType 公文类型
	 * @return
	 */
	public boolean hasSubjectWrapRecordByCurrentUser(Long AccountId, Long userId, int listType, int edocType);
	//lijl添加
	public void saveOrUpdateEdocSummaryClean(EdocSummary o);
	//GOV-4870 会签节点，竞争执行签章和文单签批前一个人执行后，后面的人再次签章/文单签批，虽然提出了已被人竞争执行，但是还是覆盖了前面的签章 
	public boolean isCompeteOver(String affairId) throws NumberFormatException, BusinessException;
	
	public UserUpdateObject editRecieveObjectState(String objId);
	
	public boolean deleteUpdateRecieveObj(String objId);
	
	/**
	 * 获取body对象
	 * @param edocId   公文id
	 * @param contentNum   body 编号
	 * @return
	 */
	public EdocBody getBodyByIdAndNum(String edocId,int contentNum);
	public List<SimpleEdocSummary> findSimpleEdocSummarysByIds(List<Long> ids);
	public List<EdocSummary> findEdocSummarysByIds(List<Long> ids);

	
	/**
	 * 获取公文的正文列表
	 * @Author      : xuqw
	 * @Date        : 2015年8月26日下午2:53:52
	 * @param summaryId 公文ID
	 * @return
	 */
	public List<EdocBody> findEdocBodys(long summaryId);
	
	public void update(Long id, Map<String, Object> columns);
	
	/**
     * 获取公文的默认节点
     * @param 公文类型
     * @param 单位id
     * @return key: defaultNodeName/defaultNodeLable
     * @throws BusinessException
     */
    public Map<String,String> getEdocDefaultNode(String edocType,Long orgAccountId) throws BusinessException;
    public Map<String,String> getGovdocDefaultNode(String edocType,Long orgAccountId) throws BusinessException;
    
    /**
     * 更新summary的归档路径ID
     * @param edocId
     * @param archiveId
     */
    public void updateEdocSummaryArchiveId(Long edocId, Long archiveId);

}
