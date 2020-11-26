package com.seeyon.apps.collaboration.manager;

import com.seeyon.apps.collaboration.bo.ColInfo;
import com.seeyon.apps.collaboration.bo.DeleteAjaxTranObj;
import com.seeyon.apps.collaboration.bo.FormLockParam;
import com.seeyon.apps.collaboration.bo.LockObject;
import com.seeyon.apps.collaboration.bo.QuerySummaryParam;
import com.seeyon.apps.collaboration.constants.ColConstant.SendType;
import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.vo.AttachmentVO;
import com.seeyon.apps.collaboration.vo.ColSummaryVO;
import com.seeyon.apps.collaboration.vo.NewCollTranVO;
import com.seeyon.apps.collaboration.vo.NodePolicyVO;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.supervise.vo.SuperviseModelVO;
import com.seeyon.ctp.common.track.bo.TrackAjaxTranObj;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.workflow.exception.BPMException;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 协同service 
 * @author libing
 *
 */
public interface ColManager {

    public String transPigeonhole(ColSummary summary, CtpAffair affair,Long destFolderId, String type) throws BusinessException;
    
    public void saveColSummary(ColSummary colSummary) throws BusinessException;
    public void updateColSummary(ColSummary colSummary) throws BusinessException;
    /**
     * 修改协同信息
     * @param info
     */
	public void updateColInfo(ColInfo info) throws BusinessException;
	 
    /**
     *  将表单ID加入到表单权限缓存中去
     */
	public void addFormRightIdToFormCache(String rightId, Long formAppId) ;
	
	
	 /**
     * 发送协同
	 * @param sendType 
     * @param startUserId
     * @param startAccountId
     * @param processXml
     * @throws BusinessException
     */
    public Map<String, String> transSend(ColInfo info, SendType sendType) throws BusinessException;
    
    
    /**
     * 存为草稿
     * @param startUserId
     * @param startAccountId
     * @param processXml
     * @throws BusinessException
     * @throws Exception 
     */
    public Map saveDraft(ColInfo info,boolean b,Map para) throws BusinessException;
    
    /**
     * 处理协同
     * @param affairId 
     * @param summaryId 
     * @param isDeleteSupervisior 是否删除督办
     * @param params 其他参数，例如跟踪，等等
     * <pre>
     *    跟踪相关参数
     *    {Map<String, String>} [trackParam] 跟踪相关参数，{@link com.seeyon.apps.collaboration.manager.ColManagerImpl#saveTrackInfo}
     * </pre>
     * @throws BusinessException
     */
    public void transFinishWorkItemPublic(Long affairId,Comment comment, Map<String, Object> params) throws BusinessException;
    
    /**
     * 
     * @param affair
     * @param summary
     * @param comment
     * @param handleType
     * @param params 其他参数，例如跟踪，等等
     * <pre>
     *    跟踪相关参数
     *    {Map<String, String>} [trackParam] 跟踪相关参数，{@link com.seeyon.apps.collaboration.manager.ColManagerImpl#saveTrackInfo}
     * </pre>
     * 
     * @throws BusinessException
     */
    public void transFinishWorkItemPublic(CtpAffair affair,ColSummary summary,Comment comment,ColHandleType handleType, Map<String, Object> params) throws BusinessException ;
    
    /**
     * 
     * @param summary
     * @param affair
     * @param params 其他参数，例如跟踪，等等
     * <pre>
     *    跟踪相关参数
     *    {Map<String, String>} [trackParam] 跟踪相关参数，{@link com.seeyon.apps.collaboration.manager.ColManagerImpl#saveTrackInfo}
     * </pre>
     * @throws BusinessException
     */
    public void transFinishWorkItem(ColSummary summary,CtpAffair affair, Map<String, Object> params) throws BusinessException;
     
     
    /**
     * 
     * @param summary
     * @param affair
     * @param params 其他参数，例如跟踪，等等
     * <pre>
     *    跟踪相关参数
     *    {Map<String, String>} [trackParam] 跟踪相关参数，{@link com.seeyon.apps.collaboration.manager.ColManagerImpl#saveTrackInfo}
     * </pre>
     * 
     * @throws BusinessException
     */
     public void transDoZcdb(ColSummary summary,CtpAffair affair, Map<String, Object> params) throws BusinessException;
    
     
     /**
      * 
      * @param summary
      * @param affair
      * @param params 其他参数，例如跟踪，等等
      * <pre>
      *    跟踪相关参数
      *    {Map<String, String>} [trackParam] 跟踪相关参数，{@link com.seeyon.apps.collaboration.manager.ColManagerImpl#saveTrackInfo}
      * </pre>
      * 
      * @throws BusinessException
      */
     public void transDoZcdbPublic(CtpAffair affair,ColSummary summary,
             Comment comment, ColHandleType handType, Map<String, Object> params) throws BusinessException;
     
     /**
     * 根据流程实例ID查找协同对象
     * @param caseId
     * @return
     * @throws BusinessException
     */
    public ColSummary getSummaryByCaseId(Long caseId) throws BusinessException;
    public ColSummary getColSummaryByIdHistory(Long id) ;
    /**
     * 根据id获取Affair对象
     * @param affairId
     * @return
     */
	public CtpAffair getAffairById(long affairId) throws BusinessException;

	/**
	 * 根据 sub_ObjectIds 查询所有的affair
	 * @param subObjects
	 * @return
	 * @throws BusinessException
	 */
	public List<CtpAffair> getAffairsBySubObjects(List<Long> subObjects) throws BusinessException;
	/**
	 * 根据id获取ColSummary对象
	 * @param id
	 * @return
	 */
	public ColSummary getColSummaryById(Long id) throws BusinessException;
	/**
	 * 根据id获取ColSummary对象(不从分库查询)
	 * @param id
	 * @return
	 */
	public ColSummary getColSummaryByIdNoFK(Long id) throws BusinessException;
	/**
     * 根据id获取processId
     * @param id 协同summary 主键
     * @return
     */
    public String getProcessId(String id) throws BusinessException;
	/**
	 * 获取转发人员名称
	 * @param forwardMember
	 * @return
	 */
	public List<String> getForwardMemberNames(String forwardMemberIds) throws BusinessException;
	/**
	 * 由id获取附件列表
	 * @param summaryId
	 * @return
	 */
	public List<Attachment> getAttachmentsById(long summaryId) throws BusinessException;
	/**
	 * 获取节点权限所在单位的id
	 * @param loginAccount
	 * @param summary
	 * @return
	 */
	public Long getPermissionAccountId(long loginAccount, ColSummary summary) throws BusinessException;
	/**
	 * 是否可以保存到本地和打印
	 * @param summary
	 * @param nodePermissionPolicy
	 * @param lenPotents
	 * @param affair
	 * @param 打开来源
	 * @return
	 */
	public Map<String, Object> getSaveToLocalOrPrintPolicy(ColSummary summary,
			String nodePermissionPolicy, 
			String lenPotents, 
			CtpAffair affair,
			String openForm) throws BusinessException;
	/**
	 * 获取已发事项
	 * @param summaryId
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo getSentAffairs(FlipInfo flipInfo,Map<String,String> query) throws BusinessException;
	public FlipInfo getSentList(FlipInfo flipInfo,Map<String,String> query) throws BusinessException;
   
	/**
	 * 获取待办事项
	 * @param summaryId
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo getPendingAffairs(FlipInfo flipInfo,Map<String,String> query) throws BusinessException;
	public FlipInfo getPendingList(FlipInfo flipInfo,Map<String,String> query) throws BusinessException;
	/**
	 * 获取已办事项
	 * @param summaryId
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo getDoneAffairs(FlipInfo flipInfo,Map<String,String> query) throws BusinessException;
	public FlipInfo getDoneList(FlipInfo flipInfo,Map<String,String> query) throws BusinessException;

	public FlipInfo getColSummaryByRelation(FlipInfo flipInfo,Map<String,Object> query) throws BusinessException;
	/**
	 * 获取待发事项
	 * @param summaryId
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo getWaitSendAffairs(FlipInfo flipInfo,Map<String,String> query) throws BusinessException;

	public FlipInfo getWaitSendList(FlipInfo flipInfo,Map<String,String> query) throws BusinessException;
	
    public List<ColSummaryVO> queryByCondition(FlipInfo flipInfo, Map<String,String> condition) throws BusinessException ;
    
    public int countByCondition(Map<String, String> condition) throws BusinessException;
    
	/**
	 * 查看属性设置信息
	 * @param args
	 * @return
	 * @throws BusinessException
	 */
	public Map getAttributeSettingInfo(Map<String,String> args) throws BusinessException;
	
	
	/**
     * @param obj 值传递对象
     * @author libing 变更跟踪状态
     */
	public void  getTrackInfo(TrackAjaxTranObj obj) throws BusinessException ;
	
	/**
	 * 检测是否有转发权限
	 * 
	 * @param affairIds 逗号分隔的字符串  eg. 12122,-999
	 * @return
	 * @throws BusinessException
	 */
	public List<String> checkForwardPermission(String affairIds) throws BusinessException;
	
	/**
	 * 
	 * @param user 转发操作者, 也就是新流程的发起者
	 * @param summaryId 要转发的协同
	 * @param affairId 要转发协同对应的事项, 用户判断转发权限和取得表单节点权限
	 * @param para
	 * forwardOriginalNote 是否转发原附言
	 * forwardOriginalopinion 是否转发原处理意见
	 * track 是否跟踪
	 * commentContent 新的附言
	 * @throws BusinessException 
	 */
	public void transDoForward(User user, Long summaryId, Long affairId, Map para) throws BusinessException;
	
    /**
     * 保存督办
     */
  //  public void saveColSupervise(ColSummary colSummary,boolean isNew,int state,boolean sendMessage) throws BusinessException;

	/**
	 * 根据Id得到summary信息
	 * @param summaryId
	 * @return
	 */
	public ColSummary getSummaryById(Long summaryId) throws BusinessException;
	/**
	 * 根据流程Id得到summary信息
	 * @param processId
	 * @return
	 */
	public ColSummary getColSummaryByProcessId(Long processId) throws BusinessException;
	
	public ColSummary getColSummaryByProcessIdHistory(Long processId) throws BusinessException;
	/**
	  * 在业务配置场景中，获取指定状态、表单模板对应的协同事项总数
	  * @param state		事务状态：待办、已办、已发
	  * @param templeteIds  用户进行表单业务配置时选中的表单模板
	  */
	 public int getColCount(long memberId, int state, List<Long> templeteIds)throws BusinessException;
	/**
	    * 删除
	    *
	    * @param request
	    * @param response
	    * @return
	 * @throws BusinessException 
	    * @throws Exception
	    */
	public String checkCanDelete(DeleteAjaxTranObj obj) throws BusinessException;
	public void   deleteAffair(String pageType, long affairId) throws BusinessException ;
	/**
	 * come from listwaitsent
	 */
	public NewCollTranVO transComeFromWaitSend(NewCollTranVO vobj) throws BusinessException;
	/**
	 * 将Affair更新成已读状态
	 * @param affair
	 * @throws BusinessException
	 */
	public void updateAffairStateWhenClick(CtpAffair affair)throws BusinessException ;
	/**
	 * 待发立即发送
	 * @param _summaryIds
	 * @param _affairIds
	 * @param sentFlag
	 * @throws BusinessException 
	 */
	public void transSendImmediate(String _summaryIds, String _affairIds,
			boolean sentFlag) throws BusinessException;
	/**
	 * 已发列表重复发起
	 * @param vobj
	 * @return
	 */
	public NewCollTranVO transResend(NewCollTranVO vobj) throws BusinessException;
	/**
	 * 协同撤销流程
	 * @param tempMap
	 * 
	 */
	public String transRepal(Map<String,Object> tempMap) throws BusinessException;
	
	 public Map<String,Object> transRepealPublic(Map<String, Object> tempMap) throws BusinessException;
	/**
	 * 协同取回操作
	 * @param _affairId
	 * @param isSaveOpinion 
	 * @return 
	 * @throws BPMException 
	 * @throws Exception 
	 */
	public String transTakeBack(Map<String,Object> tempMap) throws BusinessException;
	/**
	 * aJax调用实现流程终止操作
	 * @param tempMap
	 * @return 如果成功返回null，否则返回错误信息
	 * @throws BusinessException
	 */
	public String transStepStop(Map<String,Object> tempMap) throws BusinessException;
	/**
	 * aJax调用处理流程回退后协同的业务逻辑
	 * @param tempMap
	 * @return 如果成功返回null，否则返回错误信息
	 * @throws BusinessException
	 */
	public String transStepBack(Map<String,Object> tempMap) throws BusinessException;
	
	/**
	 * 处理流程回退后协同的业务逻辑
	 * @param tempMap
	 * @return 如果成功返回null,并返回节点信息，否则返回错误信息
	 * 
	 * @throws BusinessException
	 */
	public Map<String,String> transStepBackReturnActivityIds(Map<String,Object> tempMap) throws BusinessException;
	/**
	 * 根据流程id来查询是否核定通过
	 * @param processId
	 * @return
	 */
	public Boolean getIsVouchByProcessId(Long processId) throws BusinessException;
	
	/**
	 * 根据ID删除 
	 */
	public void deleteColSummaryUseHqlById(Long id) throws BusinessException;
	/**
	 * 协同正文展现
	 * @param summaryVO
	 * @return
	 * @throws BusinessException 
	 */
	public ColSummaryVO transShowSummary(ColSummaryVO summaryVO) throws BusinessException;
	
	/**
	 * 
	 * 表单统计/查询， 获取ColSummary 对象
	 * <ul>
	 * <li>1. 如果是子流程， 则返回父流程ColSummary</li>
	 * <li>2. 如果不是表单查询，统计， 返回null</li>
	 * <ul>
	 * 
	 * @param openFrom
	 * @param summaryId
	 * @return
	 * @throws BPMException
	 * @throws BusinessException
	 *
	 */
	public ColSummary getMainSummary4FormQueryAndStatic(String openFrom, String summaryId) throws BPMException, BusinessException;
	
	/**
	 * 
	 * 获取summary和ctpAffair
	 * 
	 * @param summaryVO
	 * @param summary
	 * @param affair
	 * @param isHistoryFlag
	 * @return
	 * @throws BusinessException
	 *
	 */
	public boolean getDisplayData2VO(ColSummaryVO summaryVO,ColSummary summary,CtpAffair affair,boolean isHistoryFlag) throws BusinessException;
	
	/**
	 * 协同调用模板
	 * @param vobj
	 * @return
	 * @throws BusinessException 
	 */
	public NewCollTranVO transferTemplate(NewCollTranVO vobj) throws BusinessException;
	/**
     * 项目协同更多条件查询
     * @param flipInfo 分页对象
     * @param queryMap {key:{@link:projectQueryEnum.name},value:}
     *//*
    public FlipInfo getColSummaryForProject(FlipInfo flipInfo, Map<String, String> queryMap) throws BusinessException ;*/
    /**
     * 根据搜索条件condition、field、field1和UserId、Status取得协同督办信息
     * @param userId
     * @param status
     * @return
     */
    public List<SuperviseModelVO> getColSuperviseModelList(FlipInfo filpInfo,Map<String,String> map);
    /**
     * 获取已经选择的指定跟踪人的信息，用于回填到选人界面
     * @param obj 里面包含一个协同的ID
     * @return
     * @throws BusinessException
     */
    public String getTrackInfosToString(TrackAjaxTranObj obj)throws BusinessException;
    
    /**
     * yangwulin 2012-12-08 Sprint5 根据条件获取关联文档中的已办、已发、待办的协同列表
     * @param flipInfo 
     * @param query 传递的参数
     * @return
     * @throws BusinessException
     */
    public FlipInfo getSentlist4Quote(FlipInfo flipInfo, Map<String, String> query) throws BusinessException;
    /**
     * 取得与表单业务配置相关的表单模板相关的跟踪事项记录
     * @param memberId  当前用户ID
     * @param tempIds   业务配置对应的表单模板ID集合
     */
    public List<ColSummaryVO>  getTrackList4BizConfig(Long memberId,List<Long> tempIds) throws BusinessException ;
    /**
     * 获取用户收到的在‘开始时间’ 和‘结束时间’ 这个时间段发起的协同列表
     * @param tempMap key包含：key currentUserID,//当前用户
     * 						key beginDate//开始时间  
     * 						key endDateStr//结束时间
     * @return
     * @throws BusinessException
     */
    public List<ColSummaryVO> getMyCollDeadlineNotEmpty(Map<String,Object> tempMap) throws BusinessException;

    /**
     * 取消跟踪
     * @param obj
     * @throws BusinessException
     */
    public void transCancelTrack(TrackAjaxTranObj obj) throws BusinessException ;
    
    /**
     * 协同转发邮件
     * @param params
     * @return
     * @throws BusinessException
     */
    public ModelAndView getforwordMail(Map params) throws BusinessException;
    /**
     * 新增模板调用历史记录
     * @param id 模板ID
     * @throws BusinessException
     */
    public void updateTempleteHistory(Long id) throws BusinessException;
    /**
     * 检查协同是否能归档
     * @param collIds 协同id列表
     * @return 归档结果信息
     * @throws BusinessException
     */
    public String getPigeonholeRight(List<String> collIds) throws BusinessException;
    /**
     * @param collIds 协同id列表
     * @param destFolderId 目标文件夹ID
     * @return
     * @throws BusinessException
     */
    public String getIsSamePigeonhole(List<String> collIds, Long destFolderId) throws BusinessException;
    /**
     * 归档协同
     * @param collIds 协同id列表
     * @param destFolderId 目标文件夹ID
     * @param type 归档操作出发源类型，包括待办pending，已办done，已发sent和处理协同handle
     * @return 归档结果信息
     * @throws BusinessException
     */
    public String transPigeonhole(Long affairId, Long destFolderId, String type) throws BusinessException ;
    /**
     * 对已经关联授权的协同事项修改IDENTIFIER字段
     * @param ids Affair的ID列表
     * @throws BusinessException
     */
    public void updateAffairIdentifierForRelationAuth(Map param) throws BusinessException;
    /**
     * 查询协同所有附件
     * @param summaryId
     * @throws BusinessException
     */
    public List<AttachmentVO> getAttachmentListBySummaryId(Long summaryId,Long memberId) throws BusinessException;
    
    public Map checkTemplateCanUse(String strID) throws BusinessException;
   
    /**
     * Long templateId,Long formAppId,Long formParentId
     * @param param
     * @return
     * @throws BusinessException
     */
    public Map checkTemplate(Map<String,String> param) throws BusinessException ;
    public Map checkTemplateCanModifyProcess(String templateId)throws BusinessException;
    
    public String getTemplateId(String tssemplateId)throws BusinessException;
    public String checkCollTemplate(String tssemplateId)throws BusinessException;
    
    /**
     * 
     */
    public String getTrackListByAffairId(TrackAjaxTranObj obj)throws BusinessException;
    
    /**
     * 将处理意见保存为草稿状态
     * @param affairId
     * @param summaryId
     */
    public void saveOpinionDraft(Long affairId, Long summaryId) throws BusinessException;
    
    public Long getSentAffairIdByFormRecordId(Long formRecordId) throws BusinessException ;
    /**
     * 校验表单协同核定和审核状态
     * return 
     */
    public Map checkVouchAudit(Map params) throws BusinessException;

    
    /**
     * 获取当前流程节点的处理说明
     * @param affairId 
     * @param templeteId 协同模版Id
     * @param processId 流程ID
     * @return 处理说明String
     */
    public String getDealExplain(Map params);
        
    /**
     * 加表单同步锁
     * @param affairId
     * @throws BusinessException
     */
    public LockObject formAddLock(FormLockParam lockParam) throws BusinessException;
    
    public void activeLockTime(Map<String,String> lockParam) throws BusinessException; 
    
    
    /**
     * 修改完流程，解除流程同步锁
     * @param affairId
     * @throws BusinessException
     */
    public void colDelLock(Long affairId) throws BusinessException;
    public void colDelLock(Long affairId,boolean delAll) throws BusinessException;
    
    public void colDelLock(ColSummary summary,CtpAffair affair) throws BusinessException;
    public void colDelLock(ColSummary summary,CtpAffair affair,boolean delAll) throws BusinessException;
    
    /**
     * 指定回退
     * @param c
     */
    public boolean updateAppointStepBack(Map<String, Object> c) throws BusinessException;
    
    /**
     * 流程结束时调用此方法更新一些状态(例如：结束时间)
     * @param summary
     * @throws BusinessException
     */
    public void transSetFinishedFlag(ColSummary summary) throws BusinessException;

	public String getContentComponentType(String string);

    /**
     * 协同更新锁检查
     * @param processId
     * @param summaryId
     * @param isLock
     * @return
     * @throws BusinessException
     */
    public String colCheckAndupdateLock(String processId, Long summaryId, boolean isLock) throws BusinessException;
    /**
	 * 统计查询协同部分的穿透，列表+详细列表
	 * @param flipInfo
	 * @param query
	 * @return FlipInfo
	 * @throws BusinessException
	 */
	public FlipInfo getStatisticSearchCols(FlipInfo flipInfo,Map<String,String> query) throws BusinessException;
	/**
     * 判断是否可以撤销<br>
     * 1.有审核(表单审核、新闻审核、公告审核)
     * 2.核定节点已处理过的，不允许撤销.<br>
     * 3.流程结束的不能撤销<br>
     */
    public Map checkIsCanRepeal(Map params) throws BusinessException;
    /**
     * 撤销子流程
     * @param caseId
     * @param user
     * @param operationType
     * @return
     * @throws BusinessException
     */
    public int recallNewflowSummary(Long caseId, User user, String operationType) throws BusinessException ;
    /**
     * 获取模板预归档路径
     * @param templateId
     * @return
     * @throws BusinessException
     */
    public boolean isTemplateHasPrePigholePath(Long templateId) throws BusinessException;
    
    /**
     * 获取当前事项的所有memberId
     * @param String
     * @return
     */
    public List<Long> getColAllMemberId(String summaryId);
    
    /**
     * 归档时同一协同已被回退的文档删除
     * @param collIds
     * @param destFolderId
     * @return
     * @throws BusinessException
     */
    public String transPigeonholeDeleteStepBackDoc(List<String> collIds, Long destFolderId) throws BusinessException;
    public Comment insertComment(Comment comment,String openFrom) throws BusinessException ;
    public String saveAttachmentFromDomain(ApplicationCategoryEnum type,Long module_id) throws BusinessException;

	public void transSendImmediate(String _summaryIds, CtpAffair affair, boolean sentFlag, String workflowNodePeoplesInput, String workflowNodeConditionInput, String workflowNewflowInput,
			String toReGo) throws BusinessException;

	public void transSendImmediate(String _summaryIds, String _affairIds, boolean sentFlag, String workflow_node_peoples_input, String workflow_node_condition_input, String workflow_newflow_input,
			String reGo) throws BusinessException;
	
	public String checkAffairValid(String affairId)  throws NumberFormatException, BusinessException;
    public String checkAffairValid(String affairId,String pageNodePolicy) throws NumberFormatException, BusinessException;
    public String checkAffairValid(CtpAffair affair,boolean isTraceValid,String pageNodePolicy) ;
    /**
     * 校验事项是否有效
     * @param affairId
     * @param isTraceValid 追述数据是否有效
     * @param pageNodePolicy 校验页面上的节点权限和数据库中的是否一致
     * @return
     * @throws BusinessException 
     * @throws NumberFormatException 
     */
    public String checkAffairValid(String affairId,boolean isTraceValid,String pageNodePolicy) throws NumberFormatException, BusinessException ;
    
    /**
     * 绩效报表穿透查询列表导出excel
     * @param flipInfo
     * @param query
     * @return
     * @throws BusinessException
     */
    public List<ColSummaryVO> exportDetaileExcel(FlipInfo flipInfo, Map<String, String> query) throws BusinessException;
    
    /**
     * 查询归档的信息，级联查询ctp_affair和doc_resource表
     * 提供此接口，是因为在穿透查询时，根据归档时间查询
     * @param flipInfo
     * @param query
     * @return
     * @throws BusinessException
     */
    public List<ColSummaryVO> getArchiveAffair(FlipInfo flipInfo, Map<String, String> query) throws BusinessException;
    
    public Long getParentProceeObjectId(Long id)  throws BusinessException;
    
    public String replaceInlineAttachment(String html);
    
    public List<ColSummary> findColSummarysByIds(List<Long> ids) throws BusinessException;
    
    
    /**
     * 用于首页更多显示当前待办人功能
     * 根据协同和公文的id获得summary对象Map数据
     * @param collIdList
     * @param EdocIdList
     * @return
     * @throws BusinessException
     */
    public Map<Long,ColSummary> getColAndEdocSummaryMap(List<Long> collIdList)throws BusinessException;
    
    public boolean isParentTextTemplete(Long templeteId) throws BusinessException;
    
    public boolean isParentWrokFlowTemplete(Long templeteId) throws BusinessException;
    
    public boolean isParentColTemplete(Long templeteId) throws BusinessException;
    /**
	 * 获取指定分钟数后的日期毫秒数
	 * @param params
	 * @return
     * @throws BusinessException  
	 */
	public Long calculateWorkDatetime(Map<String,String> params) throws BusinessException;
	
	public List<ColSummaryVO> getSummaryByTemplateIdAndState(Long templateId,Integer state)throws BusinessException;
	
	public void transUpdateCurrentInfo(Map<String, Object> ma) throws BusinessException ;
	
	public String[] getProcessIdByColSummaryId(Long id) throws BusinessException;
	
	public String getTrackName(Map params);
	
	/**
	 * 协同撤销处理：后台事件回调方法
	 * @param affairId
	 * @param repealComment
	 * @param summaryId
	 * @param trackWorkflowType
     * @param operationType 当前操作类型，WorkFlowEventListener的静态变量
	 * @throws BusinessException
	 */
	public void transRepalBackground(Long summaryId,Long _affairId,String repealComment,String trackWorkflowType,
	        Integer operationType,Boolean needSendMessage,String extAtt1,Map<String,Object> businessData) throws BusinessException;
	public void transRepalBackground(ColSummary summary,Long _affairId,String repealComment,String trackWorkflowType,
	        Integer operationType,Boolean needSendMessage,String extAtt1,Map<String,Object> businessData) throws BusinessException;
	
	public String ajaxCheckAgent(Map param);
	
	/**
	 * 处理协同页面离开时执行方法
	 * @Author      : xuqw
	 * @Date        : 2016年5月20日下午1:27:57
	 */
	public void onDealPageLeave(Map<String, Map<String, String>> params) throws BusinessException;
	
	public void ajaxColDelLock(Map<String,String> param) throws BusinessException ;

    public ColSummary getColSummaryByFormRecordId(Long formRecordId) throws BusinessException;

    /**
     * 根据表单数据ID批量查找协同id
     * 
     * @param formRecords 表单数据Id列表
     * @return
     * @throws BusinessException
     *
     * @Since A8-V5 7.0
     */
    public Map<Long, Long> getColSummaryIdByFormRecordIds(List<Long> formRecords) throws BusinessException;
    
    public Integer getColSummaryCount(Date beginDate, Date endDate, boolean isForm) throws BusinessException ;
    
    public List<Long> findIndexResumeIDList(Date starDate, Date endDate, Integer firstRow, Integer pageSize, boolean isForm) throws BusinessException ;
    
    NodePolicyVO getNewColNodePolicy(Long loginAcctountId);
	public Map<String,Object> getNewColBaseListBtns(Long loginAcctountId,Integer typeNum) throws BusinessException;
    
    Map<String,Permission> getPermissonMap(String category, Long accountId) throws BusinessException;
    
    //FlipInfo findFavoriteAffairs(FlipInfo flipInfo,Map<String,String> query) throws BusinessException;
    
    /**
     * 获取协同的默认节点
     * @return
     * @throws BusinessException
     */
    public Map<String,String> getColDefaultNode(Long orgAccountId) throws BusinessException;

    /**
     * 获取指定节点的节点权限
     * @param affair
     * @param summary
     * @param nodePermissions
     * @return
     * @throws BusinessException
     */
    public Permission getPermisson(CtpAffair affair, ColSummary summary, List<String> nodePermissions) throws BusinessException;
    
    /**
     * 获取节点权限
     * 
     * @param affair
     * @param summary
     * @return
     * @throws BusinessException
     *
     * @Since A8-V5 6.1
     * @Author      : xuqw
     * @Date        : 2017年1月14日下午1:29:19
     *
     */
    public Permission getPermisson(CtpAffair affair, ColSummary summary) throws BusinessException;

    /**
     * 协同转办
     * @param params
     *   必传递参数：<br>
     *      affairId
     *      
     * @return
     * @throws BusinessException
     */
    public String transColTransfer(Map<String,String> params) throws BusinessException;
    
    /**
     * 查询Summary
     * @Author      : xuqw
     * @Date        : 2016年1月25日下午4:21:56
     * @param param
     * @param flip
     * @return
     * @throws BusinessException
     */
    public List<ColSummary> findColSummarys(QuerySummaryParam param, FlipInfo flip) throws BusinessException;
    
    /**
     * 数据关联查询
     * @param flipInfo
     * @param summaryConditon
     * @return
     * @throws BusinessException
     */
    public List<ColSummaryVO> queryByCondition4DataRelation(FlipInfo flipInfo, Map<String,String> condition) throws BusinessException;
    
    /**
     * 获取事项状态，ajax访问
     * @param affairId
     * @return
     * @throws BusinessException
     */
    public String getAffairState(String affairId) throws BusinessException ;
    
	/**
	 * 展示更多按钮（收藏、追溯流程）
	 * @param tempMap
	 * 
	 */
	public Map<String, String> showMoreBtn(Map<String,String> tempMap) throws BusinessException;

	/**
	 * 新建页面校验affair是否已经被发送、删除，并且校验是否存在并发锁。
	 * @param summaryId
	 * @return
	 * @throws NumberFormatException
	 * @throws BusinessException
	 */
	public String checkAffairAndLock4NewCol(String summaryId,String isNeedCheckAffair) throws NumberFormatException, BusinessException;
	
	
	/**
	 * 
	 * 
	 * @param affairId
	 * @param canSubmit 是否做过加锁操作
	 * @param isReadOnly 表单是否是只读状态
	 * @param pageNodePolicy 页面上的节点权限和数据库中的是否一致
	 * @return
	 * @throws BusinessException
	 * 
	 */
	public String checkAffairValidAndIsLock(String affairId,String realLockForm,String pageNodePolicy) throws NumberFormatException, BusinessException;

	
	public void unlockCollAll(Long affairId, CtpAffair affair, ColSummary summary);
	
	/**
	 * 加载At的人员列表
	 * @param params
	 * @throws BusinessException
	 */
	public List<Map<String, Object>> pushMessageToMembersList(Map<String, String> params) throws BusinessException;
	
	/**
     * 加载At的人员列表
     * @param params
     * @param needPost 是否需要岗位信息
     * @throws BusinessException
     */
    public List<Map<String, Object>> pushMessageToMembersList(Map<String, String> params, boolean needPost) throws BusinessException;
	
	public void updateColSummaryProcessNodeInfos(String processId,String nodeInfos);
	
	public void transReMeToReGo(WorkflowBpmContext context) throws BusinessException;
	
	public String getPigeonholeRightForM3(List<String> collIds,boolean fromM3) throws BusinessException;
	
	 public Map<String,Object> findsSummaryComments(Map<String,String> params) throws BusinessException;
	 /**
     * @param collIds 协同id列表
     * @param destFolderId 目标文件夹ID
     * @return
     * @throws BusinessException
     */
    public String getIsSamePigeonhole(List<String> collIds, Long destFolderId,boolean fromPc) throws BusinessException;
    
    public String isTemplateDeleted(String templateId) throws  BusinessException;
  
    /**
     * 物理删除一个表单相关的所有的协同运行中的实例数据（意见、正文、协同、事项、流程等)，不包括模板数据
     * @param formAppId
     * @throws BusinessException
     */
    public void deleteCollDatasPhysically(Long formAppId)throws BusinessException ;
    
    /**
     * 流程是否核定通过
     * @param summary
     * @return
     * @throws BusinessException
     */
    public boolean getSummaryHasVouch(ColSummary summary) throws BusinessException;
    
    /**
     * 流程是否审核过（表单审核/公告审批/新闻审批）
     * @param summary
     * @return
     * @throws BusinessException
     */
    public boolean getSummaryHasAudit(ColSummary summary) throws BusinessException;
    
    /**
     *  查看一次流程
     * @param affairId
     * @param openFrom
     * @throws BusinessException
     */
    public void showWFCDiagram(Long affairId,String openFrom) throws BusinessException;
    
    /**
     * 记录页面js报错的错误信息
     * @param jsErrorMsg
     * @param affairId
     * @throws BusinessException
     */
    public void logErrorJs(String jsErrorMsg,Long affairId,String subject) throws BusinessException;
    
    /**
     * 
     * @Title: checkNodePolicyChange   
     * @Description: 判断节点权限是否被改变
     * @param affairId
     * @param pageNodePolicy
     * @return
     * @throws NumberFormatException
     * @throws BusinessException      
     * @return: String  
     * @date:   2018年8月28日 下午3:00:47
     * @author: xusx
     * @since   V7.0SP2	       
     * @throws
     */
	public String checkNodePolicyChange(String affairId,String pageNodePolicy) throws BusinessException;

	/**
	 *  协同发送过程中判断是否更新协同 标题
	 * @param summary
	 * @param affair
	 * @param params
	 */
	public  void checkCollSubject(ColSummary summary,CtpAffair affair,Map<String, Object> params);
	
	public String getFormFieldDisplay(Long formAppId ,String fieldName);
	
	/**
	 * 表单数据清理，根据模板ID等信息查询相关数据
	 * @param map
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo getSummarysAndTemplates(FlipInfo flipInfo,Map<String,Object> map) throws BusinessException;
	
	
	 /**
     * 物理删除一个表单相关的所有的协同运行中的实例数据（意见、正文、协同、事项、流程等)，不包括模板数据
     * @param formAppId
     * @throws BusinessException
     */
    public void deleteAllCollDatasPhysically(Long formAppIdOrTemplateId,boolean isTemplateId)throws BusinessException ;
    
    /**
     * 获取已超期或者七天内超期的待办数据
     * @param userId
     * @return
     * @throws BusinessException
     */
    public Map<String, String> getOverdueOrSevenDayOverdueMap(Long userId,String tempalteIds) throws BusinessException;

	public String getListCommentByParams(String moduleId,String userName,String opinion) throws  BusinessException;
	
	/**
	 * 批量清理表单相关数据
	 * boolean bySummaryId : true表示根据summaryId来删除数据，false表示根据formAppId来删除数据，必填
	 * List<Long> ids :协同的Ids或表单Ids，bySummaryId为true时传summaryId，为false时传formAppId
	 * @param map
	 * @throws BusinessException
	 */
	public void batchDeleteAllCollDatasPhysically(Map<String,Object> map) throws BusinessException;

	/**
     * 根据相关条件判断当前节点是否不能包含以下节点动作
     * @param summary
     * @param basicActionList
     * @param commonActionList
     * @param advanceActionList
     */
	public void checkCanAction(CtpAffair affair, ColSummary summary, List<String> basicActionList,
			List<String> commonActionList, List<String> advanceActionList, boolean isTemplete,Long permissionId);
	
	public boolean isFormNeedAddLock(CtpAffair affair,String rightId) ;
	
    public String checkAffairAndLock4NewColJson(String summaryId,String isNeedCheckAffair) throws NumberFormatException, BusinessException;
    /**
     * 
     * @Title: repalceWorkitem   
     * @Description: 替换待办节点 
     * @param ctpAffair
     * @param pageNodePolicy
     * @param currentUser
     * @param newMember
     * @param hasComment 是否有意见，有意见时会对summary数据的回复数做+1处理
     *
     * @return Map<String,Object> {errorMsg:xxx, 错误消息
     * 							   newAffairs:List<CtpAffair> 新产生的affair}
     * @throws BusinessException      
     * @return: String  
     * @date:   2019年6月11日 下午7:08:03
     * @author: xusx
     * @since   V7.1SP1	       
     * @throws
     */
    public Map<String,Object> repalceWorkitem(CtpAffair ctpAffair,String pageNodePolicy,User currentUser,V3xOrgMember newMember,boolean hasComment) throws BusinessException;


    public Map<String,Object> getNodeMemberInfos(BPMProcess process, Long summaryId, boolean isSaveDB ) throws NumberFormatException, BusinessException ;
}
