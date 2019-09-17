package com.seeyon.apps.govdoc.manager;

import java.util.List;
import java.util.Map;

import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.govdoc.constant.GovdocEnum.ExchangeDetailStatus;
import com.seeyon.apps.govdoc.po.GovdocExchangeDetail;
import com.seeyon.apps.govdoc.po.GovdocExchangeDetailLog;
import com.seeyon.apps.govdoc.po.GovdocExchangeMain;
import com.seeyon.apps.govdoc.po.JointlyIssyedVO;
import com.seeyon.apps.govdoc.vo.GovdocBaseVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ocip.exchange.exceptions.BussinessException;
import com.seeyon.v3x.edoc.domain.EdocSummary;

/**
 * 新公文交换接口
 * @author 唐桂林
 *
 */
public interface GovdocExchangeManager {
	
	/*************************** 111111 公文交换页面查询 start ******************************/
	/**
	 * 公文交换记录查看页面
	 * @param flipInfo
	 * @param conditionMap
	 * @return
	 */
	public FlipInfo findGovdocExchangeDetail(FlipInfo flipInfo, Map<String, String> conditionMap);
	/**
	 * 
	 * @param flipInfo
	 * @param conditionMap
	 * @return
	 */
	public List<GovdocExchangeDetail> getGovdocExchangeDetailList(FlipInfo flipInfo, Map<String, String> conditionMap);
	/**
	 * 公文交换日志查看页面
	 * @param flipInfo
	 * @param conditionMap
	 * @return
	 */
	public FlipInfo findGovdocExchangeDetailLog(FlipInfo flipInfo, Map<String, String> conditionMap);
	/*************************** 111111 公文交换页面查询   end ******************************/
	
	
	/*************************** 222222 公文交换对象获取 start ******************************/
	/**
     * 获取交换主对象
     * @param mainId
     * @return
     */
	public List<GovdocExchangeMain> findByReferenceIdId(Long summaryId, Integer exchangerType);
	public GovdocExchangeMain getGovdocExchangeMainById(Long mainId);
    public GovdocExchangeMain findBySummaryId(Long summaryId, Integer exchangeType) throws BusinessException;
    /**
	 * 
	* @Title: findRelationBySummaryIdAndReference
	* @Description: 根据主公文ID和关联ID查询是否已存在关联关系
	* @param summaryId
	* @param referenceId
	 */
	public int findRelationBySummaryIdAndReference(Long summaryId,Long referenceId) throws BusinessException;
	
	/**
	 * 获取公文交换对象
	 * @param mainId
	 * @return
	 * @throws BusinessException
	 */
    public List<GovdocExchangeDetail> getDetailByMainId(Long mainId)  throws BusinessException;
	public GovdocExchangeDetail findDetailBySummaryId(Long summaryId) throws BusinessException;
	public GovdocExchangeDetail findDetailByRecSummaryId(Long recSummaryId);
	public GovdocExchangeDetail getExchangeDetailById(Long detailId);
	public String govdocExchangeDetailCount(Long summaryId) throws BusinessException;
	public void showExchangeState(GovdocSummaryVO summaryVO);
	/**
	 * 已发查看交换记录
	 * @param summaryVO
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, Object> getChuantouchakanId(GovdocSummaryVO summaryVO) throws BusinessException;
	/*************************** 222222 公文交换对象获取   end ******************************/
	
	
	/*************************** 333333 公文交换对象保存  start ******************************/
	/**
	 * 保存公文交换对象
	 * @param main
	 * @throws BusinessException
	 */
	public void saveOrUpdateMain(GovdocExchangeMain main) throws BusinessException;
	public void saveDetailList(List<GovdocExchangeDetail> details) throws BusinessException;
	public void updateDetailList(List<GovdocExchangeDetail> details);
	public void updateDetail(GovdocExchangeDetail govdocExchangeDetail);
	public void updateExchangeDetailState(Long detailId, ExchangeDetailStatus state);
	public void saveDetailLogList(List<GovdocExchangeDetailLog> logs);
	public void saveDetailLog(GovdocExchangeDetailLog detailLog);
	/*************************** 333333 公文交换对象保存    end ******************************/
	
	
	/*************************** 444444 公文交换操作  start ******************************/
	/**
	 * 交换发送
	 * @param summary
	 * @param currentUserId
	 * @param sendAffairId
	 * @throws BusinessException
	 */
	public void exchangeSend(EdocSummary summary,Long currentUserId,Long sendAffairId, Map<String,Object> extendParam) throws BusinessException;
	/**
	 * 公文交换-重发
	 * @param flipInfo
	 * @param conditionMap
	 * @return
	 * @throws BusinessException 
	 */
	public String reSend(Map<String, String> params) throws BusinessException;
	/**
     * 公文交换-补发
     * @param summaryId
     * @param orgStr
     * @return
     * @throws BusinessException
     */
    public String exchangeReissue(Long summaryId, String orgStr) throws BusinessException;
    /**
	 * 公文交换-签收
	 * @param summary
	 * @param currentUserId
	 * @throws BusinessException
	 */
	public void exchangeSign(EdocSummary summary,CtpAffair currentAffair, Long currentUserId) throws BusinessException;
	public void exchangeSign(GovdocBaseVO baseVo) throws BusinessException;
	/**
	 * 公文交换-分办
	 * @param summary
	 * @param currentAffair
	 * @param currentUserId
	 * @throws BusinessException
	 */
	public void exchangeDistribute(EdocSummary exchangeSummary,CtpAffair senderAffair,CtpAffair distributeAffair,Long currentUserId, Long recSummaryId) throws BusinessException;
	/**
	 * 公文交换-撤销
	 * @param detailId
	 * @throws BusinessException
	 */
	public String exchangeCancel(Long detailId,String repealComment) throws BusinessException;
	/**
	 * 收文流程撤销，修改状态分办待发
	 * @param id
	 * @param repealComment
	 */
	public String recSummaryCancelUpdateExchangeStatus(Long detailId, String repealComment) throws BusinessException;
	/**
	 * 公文交换-回退
	 * @param detailId
	 * @throws BusinessException
	 */
	public String exchangeReturn(Long summaryId,CtpAffair cuurentAffair,String repealComment) throws BusinessException;
	/**
	 * 公文交换-指定回退
	 * @param summaryId
	 * @param stepBackDistribute 
	 * @throws BussinessException 
	 * @throws BusinessException 
	 */
	public void exchangeTakeBack(Long summaryId, CtpAffair currentAffair, boolean stepBackDistribute) throws BusinessException;
	/**
     * 公文交换-终止
     * @param summaryId
     */
    public void GovdocExchangeStop(GovdocExchangeDetail govdocExchangeDetail) throws BusinessException;
    /**
	 * 公文交换-催办
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	public String press(Map<String, String> params) throws BusinessException;
	/*************************** 444444 公文交换操作    end ******************************/
	
	
	/*************************** 555555 公文转办相关  start ******************************/
	/**
     * 公文转办列表页面
     * @param flipInfo
     * @param query
     * @return
     * @throws BusinessException
     */
    public FlipInfo findListByPage(FlipInfo flipInfo, Map<String, String> query)throws BusinessException;
    /**
     * 公文移交时，移交对应的交换数据(联合发文，转发文，转收文)
     * @param summaryId
     * @param memberId
     * @throws BusinessException
     */
    public void saveTransferByExchange(Long summaryId, Long memberId) throws BusinessException;
	/*************************** 555555 公文转办相关   end ******************************/
	
	
	/*************************** 666666 联合发文相关 start ******************************/
	/**
	 * 联合发文相关
	 * @param summaryId
	 * @param needSubject
	 * @return
	 * @throws BusinessException
	 */
	public List<JointlyIssyedVO> findMainBySummaryId4Lianhe(long summaryId,boolean needSubject) throws BusinessException; 
	public String[] validateJointlyHasNextPersion(Long summaryId, String units) throws BusinessException;
	public String[] validateJointlyIssyedUnit(Long summaryId,String units) throws BusinessException;
	/**
	 * 更新联合发文的状态
	 * @param summaryId
	 * @param status
	 * @throws BusinessException
	 */
	public void transUpdateLianheStatus(Long summaryId, ExchangeDetailStatus status) throws BusinessException;
	/*************************** 666666 联合发文相关   end ******************************/
	
	
	/*************************** 888888 公文交换工具方法 start ******************************/
	/**
	 * 查询交换单位的名称
	 * @param idArrays
	 * @return
	 * @throws BusinessException
	 */
	public String getExchangeUnit(String idArrays) throws BusinessException;
	/**
	 * 查询ocip单位时判断是否连接正常
	 * @param ids
	 * @return
	 * @throws BusinessException
	 */
	public String isConnectWhenSend(String ids) throws BusinessException;
	/**
	 * 验证公文交换状态等
	 * @param orgIdNames(格式为orgId|orgName)
	 * @return
	 * @throws BusinessException
	 */
	public String validateExistAccountOrDepartment(String orgIdNames) throws BusinessException;
	/**
	 * 
	 * @param orgString(格式为  orgType|orgId)
	 * @return
	 * @throws BusinessException
	 */
	public String validateExistAccount(String orgString) throws BusinessException;
	/**
	 * 重发重复
	 * @param summaryId
	 * @param orgString
	 * @return
	 * @throws BusinessException
	 */
	public String validateReissueeRepeat(Long summaryId, String orgString) throws BusinessException;
	/*************************** 888888 公文交换工具方法   end ******************************/
	
}
