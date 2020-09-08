package com.seeyon.v3x.edoc.manager;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.apps.collaboration.vo.AttachmentVO;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.processlog.ProcessLog;
import com.seeyon.ctp.common.track.po.CtpTrackMember;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocRegisterCondition;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.exception.EdocException;
import com.seeyon.v3x.edoc.webmodel.EdocOpinionModel;
import com.seeyon.v3x.edoc.webmodel.EdocSearchModel;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryModel;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;
import com.seeyon.v3x.edoc.webmodel.MoreSignSelectPerson;
import com.seeyon.v3x.edoc.webmodel.SummaryModel;
import com.seeyon.v3x.isearch.model.ConditionModel;
import com.seeyon.v3x.isearch.model.ResultModel;

public interface EdocManager {

    /**
     * 获取默认公文单
     *
     * @return
     */
    public EdocForm getNewDefaultEdocForm(int iEdocType, Long subType, User user);

    Long runCase(EdocSummary summary, EdocBody body, EdocOpinion senderOpinion, EdocEnum.SendType sendType, Map options, String from,
                 Long agentToId, boolean isNewSent, String process_xml, String workflowNodePeoplesInput, String workflowNodeConditionInput)
            throws BusinessException;

    Long runCase(EdocSummary summary, EdocBody body, EdocOpinion senderOpinion, EdocEnum.SendType sendType, Map options, String from,
                 Long agentToId, boolean isNewSent, String process_xml, String workflowNodePeoplesInput, String workflowNodeConditionInput, String workflowId)
            throws BusinessException;

    public Long runCase(EdocSummary summary,
                        EdocBody body, EdocOpinion senderOpinion, EdocEnum.SendType sendType,
                        Map options, String from, Long agentToId) throws EdocException;

    /**
     * 重构 ， 加trans开头，进行事物运行
     *
     * @param summary
     * @param body
     * @param senderOpinion
     * @param sendType
     * @param options
     * @param from
     * @param agentToId
     * @param isNewSent
     * @param process_xml
     * @param workflowNodePeoplesInput
     * @param workflowNodeConditionInput
     * @param workflowId
     * @return
     * @throws BusinessException
     * @Author : xuqw
     * @Date : 2017年6月21日下午6:34:45
     * @see #runCase(EdocSummary, EdocBody, EdocOpinion, com.seeyon.apps.edoc.enums.EdocEnum.SendType, Map, String, Long, boolean, String, String, String, String)
     */
    public Long transRunCase(EdocSummary summary, EdocBody body, EdocOpinion senderOpinion, EdocEnum.SendType sendType, Map options, String from,
                             Long agentToId, boolean isNewSent, String process_xml, String workflowNodePeoplesInput, String workflowNodeConditionInput, String workflowId)
            throws BusinessException;

    // 签收工作项
    void claimWorkItem(int workItemId) throws EdocException;
/*
    // 完成工作项，第3个参数是当下一结点需要人工选择执行人时，由这里传入。注意：是下一结点的执行人。
    String transFinishWorkItem(EdocSummary summary,long affairId, EdocOpinion signOpinion,
           String processId, String userId, String edocMangerID
            ,String processXml,String readyObjectJSON,String workflowNodePeoplesInput,String workflowNodeConditionInput, String processChangeMessage) throws EdocException, BusinessException;
    
 // 完成工作项，第3个参数是当下一结点需要人工选择执行人时，由这里传入。注意：是下一结点的执行人。
    String transFinishWorkItem(EdocSummary summary,long affairId, EdocOpinion signOpinion,
           String processId, String userId, String edocMangerID
            ,String processXml,String readyObjectJSON,String workflowNodePeoplesInput, 
            String workflowNodeConditionInput, String processChangeMessage, boolean fromBatch) throws EdocException, BusinessException;*/

    /**
     * 判断当前事项是否能指定的操作 。
     *
     * @param affairIds     : 个人事项ID
     * @param operationName ： 操作名(如：DepartPigeonhole)
     * @return 当传入的事项都有权限的时候，返回为空值，当传入的某些事项没有指定操作的权限的时候，返回没有权限的事项的标题。
     * @throws BusinessException
     */
    public String checkHasAclNodePolicyOperation(String affairIds, String operationName) throws BusinessException;
    /**
     * 重载完成工作项，增加督办参数
     * @param edocMangerID
     */
   /* String transFinishWorkItem(EdocSummary summary,long affairId, EdocOpinion signOpinion,String title,String supervisorMemberId,
			String supervisorNames,String superviseDate, String processId, String userId, String edocMangerID,
			String processXml,String readyObjectJSON,String workflowNodePeoplesInput,String workflowNodeConditionInput, String processChangeMessage)throws Exception;*/

    /**
     * 暂存待办
     *
     * @param summaryId
     * @param opinion
     * @throws BusinessException
     * @throws ColException
     */
    public void zcdb(EdocSummary edocSummary, CtpAffair affair, EdocOpinion opinion, String processId, String userId,
                     String process_xml, String readyObjectJSON, String processChangeMessage, boolean isM1, List<Long[]> pushMsgMemberList) throws EdocException, BusinessException;

    /**
     * 暂存待办保存方式
     *
     * @param affairId
     * @param opinion
     * @param remindMode
     * @param supervisorMemberId
     * @param supervisorNames
     * @param superviseDate
     * @throws EdocException
     * @throws BusinessException
     */
    public void zcdb(CtpAffair affair, EdocOpinion opinion, String title, String supervisorMemberId, String supervisorNames,
                     String superviseDate, EdocSummary summary, String processId, String userId, String process_xml, String readyObjectJSON, String processChangeMessage, List<Long[]> pushMsgMemberList) throws EdocException, BusinessException;

    public void deleteEdocOpinion(Long opinionId) throws EdocException;

//    // 完成工作项
//    void finishWorkItem(int workItemId, String opinionContent) throws EdocException;


    Long saveBackBox(EdocSummary summary, EdocBody body, EdocOpinion senderOpinion)
            throws EdocException;

    // 保存一个流程草稿（保存待发）    
    Long saveDraft(EdocSummary summary, EdocBody body, EdocOpinion senderOpinion, String processXml)
            throws EdocException, BusinessException;

    // 查询待办列表
    List<EdocSummaryModel> queryTodoList(int edocType, Map<String, Object> params) throws EdocException;


    public String checkEdocMark(Long definitionId, String serialNo, Integer selectMode, String summaryId);

    // 查询已办列表
    List<EdocSummaryModel> queryFinishedList(int edocType, Map<String, Object> params) throws EdocException;

    // 查询已发列表
    List<EdocSummaryModel> querySentList(int edocType, Map<String, Object> params) throws EdocException;

    List<EdocSummaryModel> querySentList(int edocType, long subEdocType, Map<String, Object> params) throws EdocException;

    // 查询未发列表
    List<EdocSummaryModel> queryDraftList(int edocType, Map<String, Object> params) throws EdocException;

    List<EdocSummaryModel> queryDraftList(int edocType, long subEdocType, Map<String, Object> params, int... substateArray) throws EdocException;

    List<EdocSummaryModel> queryTrackList(int edocType, Map<String, Object> params) throws EdocException;

    /**
     * 列出我的待办、已办、已发
     *
     * @param condition
     * @param field
     * @param field1
     * @return
     */
    public List<EdocSummaryModel> queryByCondition4Quote(ApplicationCategoryEnum appEnum, String condition,
                                                         String field, String field1);


    /**
     * 通过id查找对应的EdocSummary(默认summary的扩展属性赋值)
     *
     * @param summaryId
     * @param needBody  默认false
     * @return
     * @throws EdocException
     */
    public EdocSummary getEdocSummaryById(long summaryId, boolean needBody) throws EdocException;

    /**
     * 通过id查找对应的EdocSummary(isLoadExtend为true:给summary的扩展属性赋值;isLoadExtend为false:不给summary的扩展属性赋值)
     *
     * @param summaryId
     * @param needBody
     * @param isLoadExtend
     * @return
     * @throws EdocException
     */
    public EdocSummary getEdocSummaryById(long summaryId, boolean needBody, boolean isLoadExtend) throws EdocException;

    public EdocSummary getColAllById(long summaryId) throws EdocException;

    //删除一个个人事项
    public void deleteAffair(String pageType, long affairId) throws Exception;

    //归档
    public void pigeonholeAffair(String pageType, long affairId, Long summaryId) throws EdocException, BusinessException;

    public void pigeonholeAffair(String pageType, CtpAffair affair, Long summaryId) throws EdocException;

    public void pigeonholeAffair(String pageType, CtpAffair affair, Long summaryId, Long archiveId, boolean needcheckFinish) throws EdocException;

    /**
     * 公文单位归档
     *
     * @param pageType
     * @param affairId  ：当前Affair对象的ID
     * @param summaryId : 当前Summary对象的ID
     * @param archiveId ：当前归档路径
     * @throws EdocException
     * @throws BusinessException
     */
    public void pigeonholeAffair(String pageType, long affairId, Long summaryId, Long archiveId) throws EdocException, BusinessException;

    public void pigeonholeAffair(String pageType, CtpAffair affair, Long summaryId, Long archiveId) throws EdocException;

    //根据caseId得到对应的summary
    public EdocSummary getSummaryByCaseId(long caseId) throws EdocException;


    public Long getSummaryIdByCaseId(long caseId) throws EdocException;

    //根据caseId得到对应的summary
    public EdocSummary getSummaryByWorkItemId(int workItemId) throws EdocException;

    public String getCaseLogXML(long caseId) throws EdocException;

    public String getCaseWorkItemLogXML(long caseId) throws EdocException;

    public String getCaseProcessXML(long caseId) throws EdocException;

    /**
     * @param userId
     * @param summaryId
     * @param cancelAffair  被撤销的事项，用来判断是否代理人操作，如果是管理员撤销，可传null值
     * @param repealComment
     * @param from
     * @param repealOpinion
     * @return
     * @throws EdocException
     * @throws BusinessException
     */
    public int cancelSummary(long userId, long summaryId, CtpAffair cancelAffair, String repealComment, String from, EdocOpinion... repealOpinion) throws EdocException, BusinessException;

    public int edocBackCancelSummary(long userId, long summaryId, String repealComment, String from) throws EdocException, BusinessException;

    /**
     * 根据条件查询公文列表
     *
     * @param edocType  公文类型，发文 or 收文
     * @param condition 查询条件字段
     * @param field     查询输入框值
     * @param field1    查询输入数据，第二个输入框值
     * @param state     公文状态
     * @param state     公文子状态，可变参数，目前第一个参数定义为affair表中的substate值,第二个参数：summary表中的has_archive（是否归档）。再扩展此参数请及时修改注释
     * @return List<EdocSummaryModel>
     */
    public List<EdocSummaryModel> queryByCondition(int edocType, String condition, String field, String field1, int state, Map<String, Object> params, int... substate);

    public List<EdocSummaryModel> queryByCondition1(int edocType, String condition, String field, String field1, long subEdocType, int state, Map<String, Object> params, int... substate);

    //加签,多级会签，最后一个参数设置操作类型
    //  public void insertPeople(EdocSummary summary, CtpAffair affair, FlowData flowData, BPMProcess process, String userId,String operationType) throws EdocException;

    //减签前返回可减签的人员列表
 /*   public FlowData preDeletePeople(long summaryId, long affairId, String processId, String userId) throws EdocException;

    public FlowData deletePeople(long summaryId, long affairId, List<Party> parties, String userId) throws EdocException;
*/
    //通过processId得到相应的xml定义文件
    public String getProcessXML(String processId) throws EdocException;

    //回退
    //@return true:成功回退 false:不允许回退
    public boolean stepBack(Long summaryId, Long affairId, EdocOpinion signOpinion, Map<String, Object> paramMap) throws EdocException, BusinessException;

    //终止    
    public boolean transStepStop(Long affairId, Map<String, Object> parmas) throws EdocException, BusinessException;

    //取回
    //@return true:成功取回 false:不允许取回
    public boolean takeBack(Long affairId) throws EdocException, BusinessException;

    //取回
    //@return true:成功取回 false:不允许取回
    public boolean transTakeBack(Long affairId) throws EdocException, BusinessException;

    /*  //会签
      public void colAssign(Long summaryId, Long affairId, FlowData flowData, String userId) throws EdocException;

      //知会
      public void addInform(Long summaryId, Long affairId, FlowData flowData, String userId) throws EdocException;
      //传阅
      public void addPassRead(Long summaryId, Long affairId, FlowData flowData) throws EdocException;
  */
    //催办
    public void hasten(String processId, String activityId, String additional_remark) throws EdocException;

    /**
     * 修改正文-保存
     *
     * @throws EdocException
     */
    public void saveBody(EdocBody body) throws EdocException;

    public void saveOpinion(EdocOpinion opinion, boolean isSendMessage) throws EdocException, BusinessException;

    public void saveOpinion(EdocOpinion opinion, EdocOpinion oldOpinion, boolean isSendMessage) throws BusinessException;

    public void saveOpinion(EdocOpinion opinion, EdocOpinion oldOpinion, boolean isSendMessage, String lastOperateState) throws EdocException, BusinessException;

    /**
     * 转发
     *
     * @param summaryId
     * @param forwardOriginalNode
     * @param foreardOriginalopinion
     * @return
     * @throws EdocException
     */
    /*public EdocSummary saveForward(Long summaryId, FlowData flowData, boolean forwardOriginalNode,
                                  boolean foreardOriginalopinion, EdocOpinion senderOpinion) throws EdocException;
*/
//    public StatModel PersonalStatFilter(long user_id);

    /**
     * 回复意见
     *
     * @param comment
     * @throws EdocException
     */
//    public void saveComment(ColComment comment) throws EdocException;
    public String getPolicyBySummary(EdocSummary summary) throws EdocException;

    public String getPolicyByAffair(CtpAffair affair, String processId) throws EdocException;

    /**
     * 更新EdocSummary行，指定字段
     *
     * @param affairId
     * @param columnValue key-字段名， value-值
     */
    public void update(Long summaryId, Map<String, Object> columns);

    public void update(EdocSummary summary) throws Exception;

    public void update(EdocSummary summary, boolean isSaveExtend) throws Exception;

    public void setFinishedFlag(long summaryId, int summaryState) throws EdocException;

    public boolean updateHtmlBody(long bodyId, String content) throws EdocException;

    /**
     * @param summary
     * @return
     */
    public FormOpinionConfig getEdocFormOpinionConfig(EdocSummary summary);

    /**
     * 取当前公文的所有的意见。
     * key:公文元素代码 | otherOpinion | senderOpinion（文单里面：niwen or dengji)
     *
     * @param summaryId
     * @return
     */
    public Map<String, EdocOpinionModel> getEdocOpinion(EdocSummary summary);

    public Map<String, EdocOpinionModel> getEdocOpinion(EdocSummary summary, boolean isOnlyShowLastOpinion);

    public Map<String, EdocOpinionModel> getEdocOpinion(EdocSummary summary, boolean isOnlyShowLastOpinion, String optionType);

    public Map<String, EdocOpinionModel> getEdocOpinion(EdocSummary summary, FormOpinionConfig displayConfig);

    /**
     * 项目：徐矿集团 【当前节点为待办可以看到所有】 作者：jiangchenxi 时间：2020年8月5日 start
     */
    public Map<String, EdocOpinionModel> getEdocOpinion1(EdocSummary summary, Long affairId, FormOpinionConfig displayConfig);

    /**
     * 项目：徐矿集团 【当前节点为待办可以看到所有】 作者：jiangchenxi 时间：2020年8月5日 end
     */
    public Hashtable getEdocOpinion(Long edocFormId, LinkedHashMap hsOpinion) throws EdocException;

    public Hashtable getEdocOpinion(Long edocFormId, Long aclAccountId, LinkedHashMap hsOpinion) throws EdocException;

    public LinkedHashMap getEdocOpinion(Long summaryId, Long curUser, Long sender, String from) throws EdocException;

    public LinkedHashMap getEdocOpinion(EdocSummary summary, Long aclAccountId, Long curUser, Long sender, String from) throws EdocException;

    /**
     * 待发列表，点击理解发送
     *
     * @param summary
     * @param map：    调用模版时候，角色匹配选择人员数据
     * @throws EdocException
     * @throws BusinessException
     */

    public void sendImmediate(Long affairId, EdocSummary summary) throws EdocException, BusinessException;

    /**
     * 待发列表，点击理解发送
     *
     * @param summary
     * @param 是否为快速发文
     * @throws EdocException
     * @throws BusinessException
     */

    public void sendImmediate(Long affairId, EdocSummary summary, boolean isQuickSend) throws EdocException, BusinessException;

    /**
     * 查询公文处理意见,用于咱存待办
     *
     * @param summaryId
     * @param affairId
     * @return
     */
    public EdocOpinion findBySummaryIdAndAffairId(long summaryId, long affairId);


    /**
     * 综合查询
     */
    public List<ResultModel> iSearch(ConditionModel cModel);

    /**
     * 检查是否使用了枚举值，删除枚举值的时候，调用校验
     *
     * @param metadataId
     * @param value
     * @return
     */
    public boolean useMetadataValue(Long domainId, Long metadataId, String value);


    public EdocSummary getSummaryByProcessId(String processId);

    public List<EdocSummaryModel> queryByCondition(long curUserId, EdocSearchModel em);

    /**
     * 公文查询
     *
     * @param curUserId
     * @param em
     * @param needByPage 是否需要分页
     * @return
     * @throws BusinessException
     */
    public List<EdocSummaryModel> queryByCondition(long curUserId, EdocSearchModel em, boolean needByPage) throws BusinessException;

    public List<MoreSignSelectPerson> findMoreSignPersons(String typeAndIds);

    /**
     * 供公文统计使用，仅仅查询出公文统计表中不存在的秘密级别字段
     *
     * @param ids
     * @return
     */
    public Hashtable<Long, EdocSummary> queryBySummaryIds(List<Long> ids);

    /**
     * AJAX方法记录流程日志
     * 主要是记录的是公文对正文的操作
     *
     * @param affairId
     * @param summaryId
     */
    public void recoidChangeWord(String affairId, String summaryId, String changeType, String userId);

    /**
     * 修改附件
     *
     * @param edocSummary
     * @throws Exception
     */
    public void updateAttachment(EdocSummary edocSummary, CtpAffair affair, User user, HttpServletRequest request) throws Exception;

    public void saveUpdateAttInfo(int attSize, Long summaryId, List<ProcessLog> logs);

    public String getFullArchiveNameByArchiveId(Long archiveId);

    public String getShowArchiveNameByArchiveId(Long archiveId);

    /**
     * 设置事项的ArchiveId,并且发送消息
     *
     * @param summary
     * @param needSendMessage
     */
    public void setArchiveIdToAffairsAndSendMessages(EdocSummary summary, CtpAffair affair, boolean needSendMessage);

    /**
     * 异步判断公文是否可以被撤销
     *
     * @param summaryId4Check 公文id
     * @return msgInfo(edoc.state.end.alert) ；Y 可以撤销
     */
    public String checkIsCanBeRepealed(String summaryId4Check);

    /**
     * 异步判断已发公文,注意，是已发公文，是否可以被取回
     *
     * @param summaryId4Check 公文id
     * @return msgInfo(edoc.state.end.alert) ；Y 可以撤销
     * @throws BusinessException
     */
    public String checkIsCanBeTakeBack(String summaryId4Check) throws BusinessException;

    public List<CtpTrackMember> getColTrackMembersByObjectIdAndTrackMemberId(Long objectId, Long trackMemberId) throws BusinessException;

    public void deleteColTrackMembersByObjectId(Long objectId);

    /**
     * 根据office正文附件ID反查公文正文
     * 该方法会比较耗性能，但目前公文是基于上个版本开发的，还得沿用之前的接口，
     * 下个版本将废弃此方法。
     *
     * @param fileid
     * @return
     * @throws ColException
     */
    public EdocBody getEdocBodyByFileid(long fileid);

    /**
     * 创建暂存待办提醒时间
     *
     * @param affairId   协同信息表id
     * @param remindTime 提醒时间
     * @throws EdocException
     */
    public void createZcdbQuartz(long affairId, java.util.Date remindTime) throws EdocException;

    /**
     * 删除暂存待办提醒时间
     *
     * @param affairId   协同信息表id
     * @param remindTime 提醒时间
     * @throws EdocException
     */
    public void deleteZcdbQuartz(long affairId, java.util.Date remindTime);

    /**
     * 根据时间段枚举id获得查询列表条件数组
     *
     * @param timeEnumId 时间枚举id
     * @return 数组，{0：condition，1：textfiled，2：textfiled1}
     */
    public String[] getTimeTextFiledByTimeEnum(int timeEnumId);

    /**
     * 获取发文单位名或全名
     *
     * @param tanghongSendUnitType
     * @param sendUnit
     * @param sendUnitId
     * @param accountId
     * @return
     */
    public String getSendUnitFullName(int tanghongSendUnitType, String sendUnit, String sendUnitId, long accountId);

    /**
     * 判断是否签章
     *
     * @param recordId
     * @return
     */
    public boolean isHaveHtmlSign(String recordId);

    /**
     * lijl添加,通过edocId,userId,oplicy查询同一用户同一文单的所有意见
     *
     * @param edocId 公文单ID
     * @param userId 用户ID
     * @param oplicy 审批类型,'shenpi,tuihui...'
     * @return List<EdocOpinion>
     */
    public List<EdocOpinion> findEdocOpinion(Long edocId, Long userId, String oplicy);

    /**
     * 公文管理员操作 发文登记簿查询功能
     *
     * @param em         查询条件
     * @param needByPage 是否需要分页
     * @return
     */
    public List<EdocSummaryModel> queryByDocManager(EdocSearchModel em, boolean needByPage, long accountId, String condition, String field, String field1);

    /**
     * 收发文登记薄查询
     *
     * @param edocType    0 发文， 1 收文
     * @param queryParams 查询条件
     * @return
     * @Author : xuqiangwei
     * @Date : 2014年12月17日下午3:23:00
     */
    public List<SummaryModel> queryRegisterData(int edocType, Map<String, String> queryParams, User user);


    /**
     * 组合查询
     *
     * @param edocType
     * @param edocSearchModel
     * @param state
     * @return List<EdocSummaryModel>
     */
    public List<EdocSummaryModel> combQueryByCondition(int edocType, EdocSearchModel edocSearchModel, int state, int... substateArray);

    public List<EdocSummaryModel> combQueryByCondition(int edocType, EdocSearchModel em, int state, Map<String, Object> params, int... substateArray);

    /**
     * lijl添加,根据对象修改意见的State,0表示没有删除,1表示删除
     *
     * @param edocOpinion EdcoOpinion对象
     */
    public void update(EdocOpinion edocOpinion);

    /**
     * lijl重写上边方法,退回时把以前是0的状态改为2
     *
     * @param
     */
    public void update(Long edocId, Long userid, String oplicy, int newstate, int oldstate);

    /**
     * 检查模板是否不可用 --xiangfan 添加 GOV-4824
     *
     * @param templeteId 模板Id
     * @return true:可用 ，false:不可用
     */
    public boolean checkTempleteDisabled(Long templeteId);

    //changyi moveTo5.0  A8 APP工程加的方法
    public void setTrack(Long affairId, boolean isTrack, String trackMembers);
    //changyi moveTo5.0  A8 APP工程加的方法
	/*Long runCase(FlowData flowData, EdocSummary summary, EdocBody body, EdocOpinion senderOpinion, EdocEnum.SendType sendType, Map options,String from,
            Long agentToId)
    throws EdocException;*/

    /**
     * Ajax判断是否有发布公告、新闻的权限
     *
     * @param policyName
     * @return true(有权限) false无权限
     * @throws Exception
     */

    public boolean AjaxjudgeHasPermitIssueNewsOrBull(String policyName) throws Exception;

    public String getPhysicalPath(String logicalPath, String separator, boolean needSub1, int beginIndex);

    /**
     * gongwenguidang
     */

    public void pigeonholeAffair(String pageType, long affairId, Long summaryId, Long archiveId, String docType) throws EdocException, BusinessException;

    /**
     * 判断当前公文文号是否已经被占用（除开公文自己本身占用的文号）
     *
     * @param edocSummaryId :公文ID
     * @param serialNo      :内部文号
     * @return 0:不存在，1:已存在
     */
    public String checkSerialNoExcludeSelf(String edocSummaryId, String serialNo);

    /**
     * 对外接口：时间安排_查询时间安排，设置了节点期限的公文列表
     * map
     * 三个参数主要是以map整合的。
     * map.put("beginDate", sFormat.parse(beginDateStr));
     * map.put("endDate", sFormat.parse(endDateStr));
     * map.put("createUserID", AppContext.getCurrentUser().getId());
     */
    public List<EdocSummaryModel> getMyEdocDeadlineNotEmpty(Map<String, Object> tempMap);


    public void transFinishWorkItem(CtpAffair affair, Map<String, Object> params) throws Exception;

    /**
     * 联合发文暂时不支持转化为PDF正文
     *
     * @param request
     * @param summary
     */
    public void createPdfBodies(EdocSummary summary, String newPdfId);


    /**
     * 插入推送到首页的 公文统计的查询条 件
     */
    public void saveEdocRegisterCondition(EdocRegisterCondition condition);

    public List<EdocRegisterCondition> getEdocRegisterCondition(long accountId, Map<String, Object> paramMap, User user);

    public int getEdocRegisterConditionTotal(long accountId, int type, String subject);

    public void delEdocRegisterCondition(long id);

    public EdocRegisterCondition getEdocRegisterConditionById(long id);

    public void updateAffairStateWhenClick(CtpAffair affair) throws BusinessException;

    /**
     * 判读公文是否已经被发送
     *
     * @param summaryId
     * @return
     */
    public boolean isBeSended(Long summaryId);

    /**
     * wangwei
     *
     * @param appName
     * @param processId
     * @param activityId
     * @param workitemId
     * @return 取回验证提示
     */
    public String canTakeBack(String appName, String processId, String activityId, String workitemId, String affairId);

    /**
     * wangwei 督办修改流程回调生成待办
     *
     * @param processId
     * @return
     */

    public EdocSummary getEdocSummaryByProcessId(Long processId);

    /**
     * 普通回退的时候要验证，是否能进行普通退回，比如指定退回后的数据就不可以
     *
     * @param processId
     * @param currentUser
     * @return
     */
    public String[] edocCanStepBack(String workitemId, String processId, String nodeId, String caseId, String permissionAccount, String configCategory);

    /**
     * 是否能够暂存代办
     *
     * @return
     */
    public String[] edocCanTemporaryPending(String workitemId);

    /**
     * 是否能够撤销
     */
    public String[] edocCanRepeal(String processId, String nodeId);

    /**
     * @return
     * @是否能够进行提交
     */
    public String[] edocCanWorkflowCurrentNodeSubmit(String workitemId);

    public List<EdocOpinion> findEdocOpinionByAffairId(Long edocId, Long userId, String oplicy, List<Long> affairIds);

    public String getDealExplain(String affairId, String templeteId, String processId);

    public void transSetFinishedFlag(EdocSummary summary) throws BusinessException;

    /**
     * 对事项的状态进行判断，是否是有效的数据，主要是防止同时打开的情况
     */
    public String[] edocCanChangeNode(String workitemId);

    public String[] canStopFlow(String workitemId);

    /**
     * ajax请求：检测当前节点是否有交换类型操作
     *
     * @param edocTypeStr
     * @param configItem
     * @param accountId
     * @return
     * @throws BusinessException
     */
    public String ajaxCheckNodeHasExchangeType(String edocTypeStr, String configItem, String accountId) throws BusinessException;

    /**************** 指定回退 start ******************/
    public void appointStepBack(Map<String, Object> tempMap) throws BusinessException;
    /*****************指定回退 end ********************/
    /**
     * 获得公文所在单位的部门收发员id串
     *
     * @param summaryIdStr
     * @return
     * @throws BusinessException
     */
    public String getDeptSenders(String summaryIdStr) throws BusinessException;

    /**
     * 获取指定分钟数后的日期
     *
     * @param minutes
     * @return
     */
    public String calculateWorkDatetime(String params) throws BusinessException;

    /**
     * 按自然时间计算
     *
     * @param fromDate 截止时间
     * @param minutes  提前提醒时间段
     * @return
     */
    public String ajaxCalcuteNatureDatetime(String fromDate, Long minutes);

    /**
     * 获取公文的附件和关联文档的个数
     */
    public String getEdocAttSizeAndAttDocSize(long edocId);

    /**
     * 判断正文中是否有html签章
     *
     * @param edocId
     * @param attmentList 文件ID
     * @return
     */
    public String checkHasSignaturehtml(long edocId);

    public List<AttachmentVO> getAttachmentListBySummaryId(Long summaryId, String attmentList) throws BusinessException;

    public String checkAffairValid(String affairId, String nodePolicy);

    public String getTrackName(String docMark);

    /**
     * 获取发文登记薄数据
     *
     * @param flipInfo ： 分页对象
     * @param paramMap : 查询条件
     * @return
     * @throws BusinessException
     * @Author : xuqiangwei
     * @Date : 2014年12月9日上午12:29:21
     */
    public FlipInfo getSendRegisterData(FlipInfo flipInfo, Map<String, String> paramMap) throws BusinessException;

    /**
     * 获取收文登记簿数据
     *
     * @param flipInfo 分页对象
     * @param paramMap 查询条件
     * @return
     * @throws BusinessException
     * @Author : xuqiangwei
     * @Date : 2014年12月9日下午1:28:16
     */
    public FlipInfo getRecRegisterData(FlipInfo flipInfo, Map<String, String> paramMap) throws BusinessException;

    /**
     * 收文文号判重复
     *
     * @param serialNo
     * @param loginAccout
     * @return
     */
    public int checkSerialNoExsit(String objectId, String serialNo);

    public String isQuickSend(String summaryId);

    /**
     * ajax获取公文正文类型
     *
     * @param edocId
     * @return
     * @throws BusinessException
     */
    public String getBodyType(String edocId) throws BusinessException;

    /**
     * aJax 判断登记正文类型
     *
     * @param registerId
     * @return
     * @throws BusinessException
     */
    public String getRegisterBodyType(String registerId) throws BusinessException;

    /**
     * 获取公文的属性状态
     *
     * @return
     * @throws BusinessException
     */
    public Map getAttributeSettingInfo(Map<String, String> map) throws BusinessException;

    /**
     * 离开公文处理页面时解锁, ajax方法
     *
     * @param params
     * @throws BusinessException
     * @Author : xuqw
     * @Date : 2016年5月25日下午12:34:48
     */
    public void onLeaveDealPage(String jsonParam) throws BusinessException;

    /**
     * 解除公文正文，公文单锁
     *
     * @param userId
     * @param summary
     * @throws BusinessException
     */
    public void unLockSummary(Long userId, EdocSummary summary) throws BusinessException;

    /**
     * @param summary
     * @throws BusinessException
     */
    public void h5CancelRegister(EdocSummary summary) throws BusinessException;

    /**
     * 公文解锁
     *
     * @param summaryId
     * @param summary
     */
    public void unlockEdocAll(Long summaryId, EdocSummary summary);

    /**
     * 删除意见-逻辑删除
     *
     * @param affair
     * @param optionWay
     */
    public void deleteOpinionByWay(CtpAffair affair, String optionWay);

    public void updateCurrentNodeUser(Long id, String currentUser) throws BusinessException;

    /**
     * 公文移交
     *
     * @param user
     * @param affairId
     * @param summaryId
     * @param transferMemberId
     * @param content
     * @param isHidden
     * @param attitude
     * @param request
     * @return
     * @throws BusinessException
     */
    public String transferEdoc(User user, long affairId, long summaryId, Long transferMemberId, String content, Boolean isHidden, String attitude, HttpServletRequest request, List<Long[]> pushMsgMemberList) throws BusinessException;

    /**
     * m3和pc公用的移交方法，去掉request请求
     *
     * @param user
     * @param affair
     * @param summary
     * @param transferOpinion
     * @param transferMemberId
     * @return
     * @throws BusinessException
     */
    public String transTransferEdoc(User user, CtpAffair affair, EdocSummary summary, EdocOpinion transferOpinion, Long transferMemberId, List<Long[]> pushMsgMemberList) throws BusinessException;

    /**
     * 校验是否存在创建角色权限
     *
     * @return
     * @throws BusinessException
     */
    public String checkCreateRoleAuth(String moduleType) throws BusinessException;

    /**
     * 根据公文ID获取对应已发流程的affairId
     *
     * @param edocId
     * @return
     * @throws BusinessException
     */
    public String getAffairIdByEdocId(String edocId) throws BusinessException;

    /**
     * 检查待办是否已经是已办了
     *
     * @param affairId
     * @return
     */
    public String checkAffairIsDone(String affairId);
}

