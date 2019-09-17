package com.seeyon.apps.govdoc.manager;

import java.util.List;

import com.seeyon.apps.edoc.constants.EdocConstant.SendType;
import com.seeyon.apps.govdoc.vo.GovdocBaseVO;
import com.seeyon.apps.govdoc.vo.GovdocDealVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.processlog.po.ProcessLog;
import com.seeyon.ctp.common.processlog.po.ProcessLogDetail;
import com.seeyon.v3x.edoc.domain.EdocSummary;

/**
 * 公文日志记录接口
 * 应用日志、流程日志
 * @author 唐桂林
 *
 */
public interface GovdocLogManager {
	
	/**
	 * 保存公文发送流程日志及应用日志
	 * @param info
	 * @param sendType
	 * @throws BusinessException
	 */
	public void saveSendLogs(GovdocNewVO info, SendType sendType) throws BusinessException;
	
	/**
	 * 保存公文公送(交换)日志
	 * @param info
	 * @param sendtype
	 * @param user
	 * @param summary
	 * @param isSpecialBackReMe
	 * @param isspecialbackrerun
	 * @param isForm
	 * @param toMmembers
	 */
	public void saveSendExchangeLogs(GovdocNewVO info, SendType sendtype, User user, EdocSummary summary, 
			boolean isSpecialBackReMe, boolean isspecialbackrerun, boolean isForm,
			String toMmembers);
	
	/**
	 * 公文回退-记录公文流程及应用日志
	 * @param dealVo
	 * @throws BusinessException
	 */
	public void saveStepbackLog(GovdocDealVO dealVo) throws BusinessException;
	
	/**
	 * 公文指定回退-记录公文流程及应用日志
	 * @param dealVo
	 * @throws BusinessException
	 */
	public void saveAppointStepbackLog(GovdocDealVO dealVo) throws BusinessException;
	
	/**
	 * 公文撤销日志
	 * @param baseVo
	 * @throws BusinessException
	 */
	public void saveCancelLog(GovdocBaseVO baseVo) throws BusinessException;
	
	/**
	 * 保存附件修改日志
	 * @param dealVo
	 * @throws BusinessException
	 */
	public void saveAttUpdateLog(GovdocBaseVO baseVo) throws BusinessException;
	
	/**
	 * 保存流程日志
	 * @param user
	 * @param processeId
	 * @param activityId
	 * @param action
	 * @param params
	 */
	public void insertProcessLog(User user, long processeId, long activityId, ProcessLogAction action, String... params);
	public void insertProcessLog(User user, long processeId, long activityId, ProcessLogAction action, Long commentId,String... params);
	public void insertProcessLog(List<ProcessLog> logs);
	public void updateByHQL(Long newId, Long oldID);
	
	/**
	 * 删除流程日志
	 * @param processId
	 */
	public void deleteProcessLog(Long processId);
	
	/**
	 * 
	 * @param processId
	 * @param actionIds
	 * @return
	 */
	public List<ProcessLog> getLogsByProcessIdAndActionId(Long processId,List<Integer> actionIds);

	/**
	 * 保存应用日志
	 * @param user
	 * @param actionId
	 * @param params
	 */
    public void insertAppLog(User user, Integer actionId, String... params);

    /**
     * 
     * @param currentUser
     * @param parseLong
     * @param activityId
     * @param commit
     * @param allProcessLogDetailList
     * @param memberAndPolicys
     */
	public void insertProcessLog(User user, long processeId, long activityId, ProcessLogAction action,List<ProcessLogDetail> allProcessLogDetailList, String... params); 
	
}
