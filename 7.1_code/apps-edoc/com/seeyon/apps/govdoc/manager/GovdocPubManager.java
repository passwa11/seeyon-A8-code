package com.seeyon.apps.govdoc.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.edoc.constants.EdocConstant.SendType;
import com.seeyon.apps.govdoc.bo.SendGovdocResult;
import com.seeyon.apps.govdoc.constant.GovdocEnum.TransferStatus;
import com.seeyon.apps.govdoc.vo.GovdocBaseVO;
import com.seeyon.apps.govdoc.vo.GovdocComponentVO;
import com.seeyon.apps.govdoc.vo.GovdocDealVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.processlog.po.ProcessLog;
import com.seeyon.v3x.edoc.domain.EdocSummary;

/**
 * 新公文公共内容管理接口
 * 附言、意见、当前待办人、督办、超期、归档、全文检索
 * @author 唐桂林
 */
public interface GovdocPubManager {
	
	/*************************** 11111 新建界面填充数据 start ***************************/
	public String getTemplateSubject(String subject);
	public void CopySuperviseFromSummary(GovdocNewVO newVo, Long summaryId) throws BusinessException;
	@SuppressWarnings("rawtypes")
	public Map getAttributeSettingInfo(Map<String, String> args) throws BusinessException;
    /*************************** 11111 新建界面填充数据   end ***************************/

	
	/*************************** 22222 新建界面发送保存 start ***************************/
	/**
	 * 公文发送时设置对象到VO
	 * @param info
	 * @param sendType
	 * @throws BusinessException
	 */
	public void fillSendObj(GovdocNewVO info, SendType sendType) throws BusinessException;
	/**
	 * 公文保存待发时设置对象到VO
	 * @param newVo
	 * @param para
	 * @throws BusinessException
	 */
	@SuppressWarnings("rawtypes")
	public void fillDraftObj(GovdocNewVO newVo, Map para) throws BusinessException;
	/**
	 * 处理映射关系 从文单中同步密级之类的数据
	 * @param summary
	 * @throws BusinessException
	 */
	public void fillSummaryByMapping(GovdocBaseVO newVo) throws BusinessException;
	/**
	 * 删除公文相关信息
	 * @param info
	 * @throws BusinessException
	 */
	public void delSummary(GovdocNewVO info) throws BusinessException;
	/**
	 * 保存督办信息
	 * @param info
	 * @throws BusinessException
	 */
	public void saveSupervise(GovdocNewVO info, SendType sendType,boolean sendMessage) throws BusinessException;
	/*************************** 22222 新建界面发送保存   end ***************************/
	
	
	/*************************** 33333 公文查看界面显示 start ***************************/
	public boolean fillSummaryObj(GovdocSummaryVO summaryVO) throws BusinessException;
	public boolean fillComponentObj(GovdocComponentVO summaryVO) throws BusinessException;
	public void fillSummaryVoByPermission(GovdocSummaryVO summaryVO) throws BusinessException;
	public void fillSummaryVoByOpinions(GovdocSummaryVO summaryVO) throws NumberFormatException, BusinessException;
	public void fillSummaryVoByExchange(GovdocSummaryVO summaryVO) throws BusinessException;
	/*************************** 33333 公文查看界面显示    end ***************************/
	
	
	/*************************** 44444 查看界面处理保存 start ***************************/
	/**
	 * 公文处理提交时设置参数对象
	 * @param dealVo
	 * @return
	 * @throws BusinessException
	 */
	public boolean fillFinishObj(GovdocDealVO dealVo) throws BusinessException;
	/**
	 * 公文暂存待办时设置参数对象
	 * @param dealVo
	 * @return
	 * @throws BusinessException
	 */
	public boolean fillZcdbObj(GovdocDealVO dealVo) throws BusinessException;
	/**
	 * 公文回退设置参数对象
	 * @param dealVo
	 * @return
	 * @throws BusinessException
	 */
	public boolean fillStepBackObj(GovdocDealVO dealVo) throws BusinessException;
	/**
	 * 公文指定回退设置参数对象
	 * @param dealVo
	 * @return
	 * @throws BusinessException
	 */
	public boolean fillAppointStepBackObj(GovdocDealVO dealVo) throws BusinessException;
	/**
	 * 公文终止设置参数对象
	 * @param dealVo
	 * @return
	 * @throws BusinessException
	 */
	public boolean fillStepStopObj(GovdocDealVO dealVo) throws BusinessException;
	/**
	 * 修改状态和sendDate
	 * @param edocSummaryId
	 * @param transferStatus
	 * @param signSummaryId
	 * @throws BusinessException
	 */
	public void directUpdateEdocSummaryAttr(Long edocSummaryId, TransferStatus transferStatus, Long signSummaryId) throws BusinessException;
	/**
	 * 公文提交时，保存督办设置
	 * @param summary
	 * @param affair
	 * @throws BusinessException
	 */
	public void saveSuperviseByDeal(EdocSummary summary, CtpAffair affair) throws BusinessException;
	/**
	 * 公文暂存时，保存督办设置
	 * @param summary
	 * @param affair
	 * @throws BusinessException
	 */
	public void saveSuperviseByZcdb(EdocSummary summary, CtpAffair affair) throws BusinessException;
	/*************************** 44444 查看界面处理保存   end ***************************/
		
	
	/*************************** 66666 公文交换相关方法 start ***************************/
	/**
	 * 处理快速发文时在收文单位收文的触发
	 * @param info
	 */
	public void sendIsQuickGovdoc(GovdocNewVO info) throws BusinessException;
	/**
	 * 公文交换时，生成签收流程
	 * @param sendType
	 * @param templateId
	 * @param senderId
	 * @param formMasterId
	 * @param recieveOrgId
	 * @param bodyType
	 * @param hasAtts 
	 * @return
	 * @throws BusinessException
	 */
	public SendGovdocResult transSendColl(
	    	SendType sendType, 
	    	Long templateId, 
	    	Long senderId, 
	    	Long formMasterId, 
	    	Long parentSummaryId,
	    	Long summaryId,
	    	Long recieveOrgId, 
	    	Integer bodyType, 
	    	Boolean hasAtts,
	    	Integer govdocExchangeType) throws BusinessException;

	/**
	 * 更新edocSummary流转状态
	 * @param colSummaryId
	 * @param transferStatus
	 * @throws BusinessException
	 */
	public void updateEdocSummaryTransferStatus(Long colSummaryId, TransferStatus transferStatus) throws BusinessException;
	
	/**
	 * 在保存summary前设置 一些属性值，防止后续再update影响性能
	 * @param info
	 * @throws BusinessException
	 */
	public void fillSummaryBeforeSave(GovdocBaseVO info) throws BusinessException;
	/*************************** 66666 公文交换相关方法   end ***************************/    

	/**
	 * 
	 * @param attSize
	 * @param summaryId
	 * @param logs
	 */
	public void saveUpdateAttInfo(int attSize,Long summaryId,List<ProcessLog> logs) throws BusinessException;

	public String getFullArchiveNameByArchiveId(Long archiveId) throws BusinessException;
	public String getShowArchiveNameByArchiveId(Long archiveId) throws BusinessException;
	public void setArchiveIdToAffairsAndSendMessages(EdocSummary summary,CtpAffair affair,boolean needSendMessage) throws BusinessException;
}
