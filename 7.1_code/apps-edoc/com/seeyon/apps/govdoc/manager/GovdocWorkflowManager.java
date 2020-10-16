package com.seeyon.apps.govdoc.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.vo.SeeyonPolicy;
import com.seeyon.apps.edoc.constants.EdocConstant.SendType;
import com.seeyon.apps.govdoc.vo.GovdocDealVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.WFInfo;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.processlog.po.ProcessLogDetail;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.v3x.edoc.domain.EdocSummary;

import net.joinwork.bpm.definition.BPMProcess;

/**
 * 新公文流程相关接口
 * 
 * @author 唐桂林
 * 
 */
public interface GovdocWorkflowManager {

	/************************* 流程操作相关 11111 start ********************************/
	/**
	 * 公文交换/触发/子流程等生成新的公文流程
	 * 
	 * @param sendtype
	 * @param user
	 * @param summary
	 * @param template
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, String> runcase(SendType sendtype, User user, EdocSummary summary, CtpTemplate template) throws BusinessException;

	public Map<String, String> runcase(String process_id, String process_xml, String popNodeSubProcessJson, String selectedPeoplesOfNodes, String conditionsOfNodes, User user,
			EdocSummary summary, SendType sendtype, CtpTemplate template, boolean addFirstNode) throws BusinessException;

	/**
	 * 公文流程待发
	 * 
	 * @param newVo
	 * @throws BusinessException
	 */
	public WFInfo draftRuncase(GovdocNewVO newVo) throws BusinessException;

	/**
	 * 公文流程发送
	 * 
	 * @param sendType
	 * @param newVo
	 * @throws BusinessException
	 */
	public WFInfo sendRuncase(SendType sendType, GovdocNewVO newVo) throws BusinessException;

	/**
	 * 公文流程暂存
	 * 
	 * @param info
	 * @param sendType
	 * @throws BusinessException
	 */
	public WFInfo zcdbRuncase(ColHandleType handleType, GovdocDealVO dealVo) throws BusinessException;

	/**
	 * 公文流程提交
	 * 
	 * @param info
	 * @param sendType
	 * @throws BusinessException
	 */
	public WFInfo finishRuncase(ColHandleType handleType, GovdocDealVO dealVo) throws BusinessException;

	/**
	 * 撤销流程 1:流程已结束，不许撤消 0:撤消完成
	 * 
	 * @param affairData
	 * @param caseId
	 * @return
	 * @throws BPMException
	 */
	public int cancelCase(AffairData affairData, Long caseId) throws BPMException;

	/**
	 * 流程回退
	 * 
	 * @param affairData
	 * @param affair
	 * @param targetNodeId
	 * @return String[0] 表示本次回退结果： 0表示正常回退成功； -1表示流程已结束，不能回退； -2表示不允许回退；
	 *         1表示回退导致撤销，正常撤销成功； 2表示成功回退到指定节点； 3表示前面节点有子流程已结束，不能回退； String[1]
	 *         表示节点名称（权限名称）
	 * @throws BPMException
	 */
	public String[] stepBackCase(GovdocDealVO dealVo) throws BusinessException;

	/**
	 * 流程指定回退
	 * 
	 * @param affairData
	 * @param dealVo
	 * @return
	 * @throws BPMException
	 */
	public String[] appointStepBackCase(GovdocDealVO dealVo) throws BusinessException;

	/**
	 * 流程终止
	 * 
	 * @param user
	 * @param workItemId
	 * @param formRecordId
	 * @param formAppId
	 * @throws BPMException
	 */
	public void stepStopCase(User user, Long workItemId, Long formRecordId, Long formAppId) throws BPMException;

	/**
	 * 流程取回
	 * 
	 * @param workItemId
	 * @return
	 * @throws BPMException
	 */
	public int takeBackCase(Long workItemId) throws BPMException;

	/************************* 流程操作相关 11111 end ********************************/

	/************************* 节点权限相关 ********************************/
	public SeeyonPolicy getPolicyByAffair(CtpAffair affair, String processId) throws BusinessException;

	public void updatePermissinRef(Integer modulType, String[] policyLists, Long accountId) throws BusinessException;

	public void updatePermissinRef(Integer modulType, String processXml, String processId, String processTemplateId, Long accountId) throws BusinessException;

	public List<ProcessLogDetail> getAllWorkflowMatchLogAndRemoveCache();

	public List<ProcessLogDetail> getAllWorkflowMatchLogAndRemoveCache(String conditionInput);

	/************************* 流程解锁相关 ********************************/
	public void releaseWorkFlowProcessLock(String processId, String userId) throws BPMException;
	/**
	 * 获得超级节点状态:0表示为非超级节点;1表示超级节点待处理;2表示超级节点待触发;3表示超级节点待回退
	 * @param processId
	 * @param activityId
	 * @return
	 * @throws BPMException
	 */
	public int getSuperNodeStatus(String processId, String activityId) throws BPMException;

	public String getNodeFormViewAndOperationName(Long templateId, String nodeId) throws BPMException;

	public String getNodeFormViewAndOperationName(BPMProcess process, String nodeId) throws BPMException;

	public String getWorkflowNodesInfo(String templateId, String appName, CtpEnumBean subAppEnum) throws BPMException;

	public String selectWrokFlowTemplateXml(String templateId) throws BPMException;

	public String selectWrokFlowXml(String templateId) throws BPMException;

	public String[] getStartNodeFormPolicy(Long templateId) throws BPMException;

	/**
	 * 删除点节
	 * 
	 * @param currentAffair
	 * @param activityIdList
	 * @throws BusinessException
	 */
	public void deleteNode(CtpAffair currentAffair, List<String> activityIdList) throws BusinessException;

	public void fillSummaryVoByWf(GovdocSummaryVO summaryVO) throws BusinessException;

	public BPMProcess getTemplateProcess(Long processTempleteId) throws BPMException;

	public BPMProcess getBPMProcessForM1(String processId) throws BPMException;

	public void updateCaseRunState(Long caseId, int state) throws BusinessException;

	public String transBackWfRunCase(String caseId) throws BusinessException;

	public void readWorkItem(Long workitemId) throws BPMException;

	public String[] getNodePolicyIdAndName(String appName, String processId, String activityId) throws BPMException;

	public String getNodeFormOperationName(Long templateId, String nodeId) throws BPMException;

	public String getNodeFormOperationNameFromRunning(String processId, String nodeId) throws BPMException;

	public String getNodeFormOperationNameFromRunning(String processId, String nodeId, boolean isHistoryFlag) throws BPMException;

	public boolean isInSpecialStepBackStatus(long caseId, boolean isHistoryFlag) throws BPMException;

	public Object[] replaceWorkItemMembers(boolean isUpdateProcess, Long memberId, String processId, long workitemId, String activityId, List<V3xOrgMember> nextMember,
			boolean isTransfer) throws BPMException;

	public String[] getNodePolicyInfos(String processId, String nodeId) throws BPMException;

	public String[] getNodePolicyInfosFromTemplate(Long templateId, String nodeId) throws BPMException;

	public SeeyonPolicy getPolicyByAffair(CtpAffair affair, EdocSummary summary) throws BusinessException;

	public String[] lockWorkflowForRepeal(String appName, String processIds, String activityId, String currentUserId, int action) throws BPMException;
}
