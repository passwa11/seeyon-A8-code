package com.seeyon.apps.govdoc.manager;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.workflowmanage.vo.WorkflowData;
import com.seeyon.ctp.util.FlipInfo;

/**
 * 新公文列表接口
 * @author 唐桂林
 *
 */
public interface GovdocListManager {

	/**
	 * 公文待办列表
	 * @param flipInfo 分页对象
	 * @param condition 
	 * 	必填参数：govdocType公文类型  数值(1 发文/2,4 收文/3 签报/空或不传表示所有)
	 * 	必填参数：listType参见GovdocListTypeEnum  
	 * 			数值([待办列表-全部]listPendingAllRoot/[待办列表-待办]listPendingRoot
	 * 				/[发文管理/收文管理/签报管理-待办+待阅]listPendingAll
	 * 				/[发文管理/收文管理/签报管理-待办]listPending
	 * 				/[发文管理/收文管理/签报管理-待阅]listReading)
	 *  必填参数：memberId人员ID
	 *  选填参数：hasAgent是否包含代理数据，默认无   数值(true/false)
	 *  选填参数：needTotal是否需要查询列表总数，默认为true   数值(true/false)
	 *  选填参数：configId待办列表-列表分类配置ID
	 * 	选填参数-查询类型：condition为空不按条件查询   数值GovdocListConditionEnum(choose列表查询/com高级查询/query公文查询)
	 *  选填参数-查询条件：subject不为空，按标题查询等，请参照GovdocListVO中属性名
	 * @return FlipInfo data数据 (GovdocListVO类型)
	 * @throws BusinessException
	 */
	public FlipInfo findPendingList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException;
	
	/**
	 * 公文已办列表
	 * @param flipInfo 分页对象
	 * @param condition 
	 * 	必填参数：govdocType公文类型  数值(1 发文/2,4 收文/3 签报/空或不传表示所有)
	 * 	必填参数：listType参见GovdocListTypeEnum  
	 * 		数值([已办列表-全部]listDoneAllRoot/[已办列表-在办]listDoneRoot/[已办列表-已办结]listFinishedRoot
	 * 			/[发文管理/收文管理/签报管理-已办]listDoneAll
	 * 			/[发文管理/收文管理/签报管理-在办]listDone
	 * 			/[发文管理/收文管理/签报管理-已办结]listFinished)
	 *  必填参数：memberId人员ID
	 *  选填参数：hasAgent是否包含代理数据，默认无   数值(true/false)
	 *  选填参数：needTotal是否需要查询列表总数，默认为true   数值(true/false)
	 *  选填参数：configId待办列表-列表分类配置ID
	 * 	选填参数-查询类型：condition为空不按条件查询   数值GovdocListConditionEnum(choose列表查询/com高级查询/query公文查询)
	 *  选填参数-查询条件：subject不为空，按标题查询等，请参照GovdocListVO中属性名
	 * @return FlipInfo data数据  (GovdocListVO类型)
	 * @throws BusinessException
	 */
	public FlipInfo findDoneList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException;

	/**
	 * 公文已发列表
	 * @param flipInfo 分页对象
	 * @param condition 
	 * 	必填参数：govdocType公文类型  数值(1 发文/2,4 收文/3 签报/空或不传表示所有)
	 * 	必填参数：listType参见GovdocListTypeEnum  
	 * 			数值([已发列表]listSentAllRoot/[发文管理/收文管理/签报管理-已发]listSent)
	 *  必填参数：memberId人员ID
	 *  选填参数：needTotal是否需要查询列表总数，默认为true   数值(true/false)
	 * 	选填参数-查询类型：condition为空不按条件查询   数值GovdocListConditionEnum(choose列表查询/com高级查询/query公文查询)
	 *  选填参数-查询条件：subject不为空，按标题查询等，请参照GovdocListVO中属性名
	 * @return FlipInfo data数据  (GovdocListVO类型)
	 * @throws BusinessException
	 */
	public FlipInfo findSentList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException;

	/**
	 * 公文待发列表
	 * @param flipInfo 分页对象
	 * @param condition 
	 * 	必填参数：govdocType公文类型  数值(1 发文/2,4 收文/3 签报/空或不传表示所有)
	 * 	必填参数：listType参见GovdocListTypeEnum  
	 * 			数值([待发列表]listWaitSendAllRoot/[发文管理/收文管理/签报管理-待发或待登记]listWaitSend)
	 *  必填参数：memberId人员ID
	 *  选填参数：needTotal是否需要查询列表总数，默认为true   数值(true/false)
	 * 	选填参数-查询类型：condition为空不按条件查询   数值GovdocListConditionEnum(choose列表查询/com高级查询/query公文查询)
	 *  选填参数-查询条件：subject不为空，按标题查询等，请参照GovdocListVO中属性名
	 * @return FlipInfo data数据  (GovdocListVO类型)
	 * @throws BusinessException
	 */
	public FlipInfo findWaitSendList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException;
	
	/**
	 * 关联文档-公文列表
	 * @param flipInfo
	 * @param condition
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo find4QuoteList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException;
	
	/**
	 * 公文查询
	 * @param flipInfo 分页对象
	 * @param condition 
	 * 	必填参数：govdocType公文类型  数值(1 发文/2,4 收文/3 签报/空或不传都可以)
	 * 	必填参数：listType参见GovdocListTypeEnum  数值(listQuery)
	 *  必填参数：memberId人员ID
	 *  选填参数：needTotal是否需要查询列表总数，默认为true   数值(true/false)
	 * 	选填参数-查询类型：condition为空不按条件查询   数值(query公文查询)
	 *  选填参数-查询条件：subject不为空，按标题查询等，请参照GovdocListVO中属性名
	 * @return FlipInfo data数据  (GovdocListVO类型)
	 * @throws BusinessException
	 */
	public FlipInfo findQueryResultList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException;
	
	/**
	 * 发文登记簿
	 * @param flipInfo 分页对象
	 * @param condition
	 * 	必填参数：listType参见GovdocListTypeEnum  数值(listSendRegister)
	 *  必填参数：orgAccountId单位ID
	 *  选填参数：needTotal是否需要查询列表总数，默认为true   数值(true/false)
	 * 	选填参数-查询类型：condition为空不按条件查询   数值(register 登记簿)
	 *  选填参数-查询条件：subject不为空，按标题查询等，请参照GovdocListVO中属性名
	 * @return FlipInfo data数据  (GovdocListVO类型)
	 * @throws BusinessException
	 */
	public FlipInfo findSendRegisterList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException;
	
	/**
	 * 收文登记簿
	 * @param flipInfo 分页对象
	 * @param condition
	 * 	必填参数：listType参见GovdocListTypeEnum  数值(listRecRegister)
	 *  必填参数：orgAccountId单位ID
	 *  选填参数：needTotal是否需要查询列表总数，默认为true   数值(true/false)
	 * 	选填参数-查询类型：condition为空不按条件查询   数值(register 登记簿)
	 *  选填参数-查询条件：subject不为空，按标题查询等，请参照GovdocListVO中属性名
	 * @return FlipInfo data数据  (GovdocListVO类型)
	 * @throws BusinessException
	 */
	public FlipInfo findRecRegisterList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException;
	
	/**
	 * 签报登记簿
	 * @param flipInfo 分页对象
	 * @param condition 
	 * 	必填参数：listType参见GovdocListTypeEnum  数值(listSignRegister)
	 *  必填参数：orgAccountId单位ID
	 *  选填参数：needTotal是否需要查询列表总数，默认为true   数值(true/false)
	 * 	选填参数-查询类型：condition为空不按条件查询   数值(register 登记簿)
	 *  选填参数-查询条件：subject不为空，按标题查询等，请参照GovdocListVO中属性名
	 * @return FlipInfo data数据  (GovdocListVO类型)
	 * @throws BusinessException
	 */
	public FlipInfo findSignRegisterList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException;
	
	/**
	 * 
	 * @param flipInfo
	 * @param subject
	 * @param beginDate
	 * @param endDate
	 * @param senderMap
	 * @param flowstate
	 * @param edocType
	 * @param operationType
	 * @param operationTypeIds
	 * @param accountId
	 * @param isPage
	 * @param fi
	 * @return
	 */
	public List<WorkflowData> getAdminWfDataList(FlipInfo flipInfo, Map<String,Object> conditionParam,long accountId,boolean isPage,FlipInfo fi) throws BusinessException;
	
	/**
	 * 查询本单位收文发文的数量
	 * @param condition
	 * @return
	 * @throws BusinessException
	 */
	public Integer getRegisterCount(Map<String, String> condition) throws BusinessException;
}
