package com.seeyon.apps.govdoc.manager;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.edoc.constants.EdocConstant.SendType;
import com.seeyon.apps.govdoc.vo.GovdocBaseVO;
import com.seeyon.apps.govdoc.vo.GovdocComponentVO;
import com.seeyon.apps.govdoc.vo.GovdocDealVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.apps.govdoc.vo.GovdocRepealVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;

/**
 * 新公文管理接口
 *
 * @author 唐桂林
 *
 */
public interface GovdocManager {

	/*************************** 11111 拟文界面填充数据 start ***************************/
	/**
	 * 调用模板
	 * @param newVo
	 * @return
	 * @throws BusinessException
	 */
	public GovdocNewVO fillNewVoByTemplate(GovdocNewVO newVo) throws BusinessException;
	
	/**
	 * 
	 * @param newVo
	 * @throws BusinessException
	 */
	public GovdocNewVO fillNewVoByExchange(GovdocNewVO newVo) throws BusinessException;
	
	/**
	 * 已发列表-重复发起
	 * 
	 * @param newVo
	 * @return
	 */
	public GovdocNewVO fillNewVoByResend(GovdocNewVO newVo) throws BusinessException;
	/**
	 * 公文-快捷发送
	 * 
	 * @param newVo
	 * @return
	 * @throws BusinessException
	 */
	public GovdocNewVO fillNewVoByWaitSend(GovdocNewVO newVo) throws BusinessException;
	public void fillFinishPermisssion(GovdocDealVO finishVO) throws BusinessException;
	/*************************** 11111 拟文界面填充数据   end ***************************/
	
	
	/*************************** 22222 新建界面发送保存 start ***************************/
	/**
	 * 公文-发送
	 * 
	 * @param info
	 * @param sendType
	 * @throws BusinessException
	 */
	public void transSend(GovdocNewVO info, SendType sendType) throws BusinessException;
	
	/**
	 * 公文-保存待发
	 * @param info
	 * @param para
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("rawtypes")
	public boolean transSaveDraft(GovdocNewVO info, Map para) throws BusinessException;
	/*************************** 22222 新建界面发送保存   end ***************************/
	

	/*************************** 33333 公文查看界面参数 start ***************************/
	/**
	 * 公文查看
	 * @param summaryVO
	 * @throws BusinessException
	 */
	public void transShowSummary(GovdocSummaryVO summaryVO) throws BusinessException;
	/**
	 * 公文单查看
	 * @param compVO
	 * @param request
	 * @throws BusinessException
	 */
	public void transComponentPage(GovdocComponentVO compVO, HttpServletRequest request) throws BusinessException;
	/**
	 * 公文意见查看
	 * @param compVO
	 * @param request
	 * @throws BusinessException
	 */
	public void transCommentPage(GovdocComponentVO compVO, HttpServletRequest request) throws BusinessException;
	/**
	 * 查询用户是否有查看权限
	 * 
	 * @param affairId
	 * @param userId
	 * @return
	 * @throws BusinessException
	 */
	public boolean lookEnable(Long affairId, Long userId) throws BusinessException;
	/*************************** 33333 公文查看界面参数 end ***************************/
	
	
	/*************************** 44444 查看界面处理保存 start ***************************/
	/**
	 * 处理-提交
	 * @param summary
	 * @param affair
	 * @param params
	 * @throws BusinessException
	 */
	public void transFinishWorkItem(GovdocDealVO finishVO) throws BusinessException;
	/**
	 * 公文提交或暂存待办公共部分
	 * @param dealVo
	 * @param affairId
	 * @param comment
	 * @throws BusinessException
	 */
	public void transFinishWorkItemPublic(GovdocDealVO dealVo, ColHandleType handleType, Object... param) throws BusinessException;
	/**
	 * 处理-暂存待办
	 * @param affair
	 * @throws BusinessException
	 */
	public void transDoZcdb(GovdocDealVO dealVo) throws BusinessException;
	public void transDoZcdbByPdf(GovdocDealVO dealVo) throws BusinessException;
	
	/**
	 * 公文-回退
	 * @param tempMap
	 * @return
	 * @throws BusinessException
	 */
	public boolean transStepBack(GovdocDealVO dealVo) throws BusinessException;
	/**
	 *  公文-指定回退
	 * @param dealVo
	 * @return
	 * @throws BusinessException
	 */
	public boolean transAppointStepBack(GovdocDealVO dealVo	) throws BusinessException;
	/**
	 * 公文-终止
	 * @param dealVo
	 * @return
	 * @throws BusinessException
	 */
    public boolean transStepStop(GovdocDealVO dealVo) throws BusinessException;
    /**
     * 处理-移交
     * @param params
     * @return
     * @throws BusinessException
     */
	public String transfer(Map<String, String> params) throws BusinessException;
	/*************************** 44444 查看界面处理保存   end ***************************/	

	
	/*************************** 55555 公文流程回调方法 start ***************************/
	/**
     * 公文流程撤销回调
     * @param baseVo
     * @throws BusinessException
     */
    public void transRepealCallback(GovdocBaseVO baseVo) throws BusinessException;
    
    /**
     * 公文流程结束回调
     * @param baseVo
     * @throws BusinessException
     */
    public void transProcessFinishCallback(GovdocBaseVO baseVo) throws BusinessException;
    
    /**
     * 公文文号修改后回调，同步父/子流程文号数据
     * @param summaryId
     * @param markType
     * @param markstr
     * @throws BusinessException
     */
    public void transSyncParentMark(Long summaryId, Integer markType, String markstr) throws BusinessException;
	/*************************** 55555 公文流程回调方法   end ***************************/
	
    
    /*************************** 66666 公文交换相关方法 start ***************************/
    /**
     * 分办时-处理签收流程的分办流程
     * @param finishVO
     * @return
     * @throws BusinessException
     */
    public boolean transFinishWorkItemByDistrubite(GovdocDealVO finishVO)throws BusinessException;
    /**
     * 分送
     * @param finishVO
     */
    public void transDistribute(GovdocDealVO finishVO);
    /**
     * 签收
     * @param finishVO
     */
    public void transSign(GovdocDealVO finishVO);
    /*************************** 66666 公文交换相关方法   end ***************************/
	
	
	/*************************** 88888 AJAX方法 start ***************************/
    /**
     * AJAX方法：回退分办
     * @param summaryID
     * @param affairID
     * @return
     * @throws BusinessException
     */
    public String takeDeal(String summaryID, String affairID) throws BusinessException;
	/**
	 * AJAX方法：已办列表-取回
	 * @param ma
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, Object> transTakeBack(Map<String, Object> ma) throws BusinessException;
	/**
	 * AJAX方法：删除前验证流程是否允许删除
	 * @param param
	 * @return
	 * @throws BusinessException
	 */
	public String checkCanDelete(Map<String, String> param) throws BusinessException;
	/**
	 * AJAX方法：公文列表-删除
	 * @param pageType
	 * @param affairId
	 * @throws BusinessException
	 */
	public void deleteAffair(String pageType, long affairId) throws BusinessException;
	
	/**
	 *	AJAX方法：撤销前验证流程是否允许撤销 
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("rawtypes")
	public Map checkIsCanRepeal(Map params) throws BusinessException;
	/**
	 * AJAX方法：流程撤销
	 * @param repealVO
	 * @return
	 * @throws BusinessException
	 */
	public String transRepal(GovdocRepealVO repealVO) throws BusinessException;
	
	/**
	 * 记录正文修改日志
	 * @param map
	 */
	public void recoidChangeWord(Map<String,String> map);
	
	/**
	 * 根据策略名称，检查公文是否有对于的权限
	 * @param affairId 公文id列表
	 * @param actionKey 策略名称 如撤销(Cancel)\修改正文(Edit)
	 * @return 撤销结果信息
	 * @throws BusinessException
	 */
	public String getRightByAction(String affairId,String actionKey) throws BusinessException;
	public String getNodePolicyName(Long affairId)throws BusinessException;
	/*************************** 88888 AJAX方法   end ***************************/
	
	/**
	 * 
	* @Title: recallNewflowSummary
	* @Description: 子流程撤销方法
	* @param caseId
	* @param user
	* @param operationType
	 */
	public int recallNewflowSummary(Long caseId, User user, String operationType) throws BusinessException;
	public void transJointlyIssued(GovdocDealVO dealVo) throws BusinessException;
	
	String getMutiDepAndAccount(String input,String primaryInput) throws BusinessException;
	
	
	/**
	 * 公文管理界面，获取需要展示的统计信息
	 * @param accountId 当前单位ID
	 * @return
	 * @throws BusinessException
	 */
	public Map<String,Integer> getManagermentPlatformCount(Long accountId) throws BusinessException;

}
