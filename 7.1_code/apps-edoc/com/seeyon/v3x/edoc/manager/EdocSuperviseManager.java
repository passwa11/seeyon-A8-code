package com.seeyon.v3x.edoc.manager;

import java.util.List;

import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.supervise.vo.CtpSuperviseVO;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.domain.EdocSuperviseRemind;
import com.seeyon.v3x.edoc.exception.EdocException;
import com.seeyon.v3x.edoc.webmodel.EdocSuperviseDealModel;

public interface EdocSuperviseManager {


	public void supervise(String remindMode,String supervisorMemberId,String supervisorNames,String superviseDate,EdocSummary summary) throws BusinessException; 


	public void createSuperviseRemind(EdocSuperviseRemind edocSuperviseRemind);

	public void changeSuperviseDetail(CtpSuperviseDetail detail);


	public CtpSuperviseDetail getSuperviseById(Long id);
	public void sendMessage(Long superviseId,String mode,String processId, String activityId, String additional_remark, long[] people,String summaryId);
	//public void updateAllDetail(List list) throws BusinessException;
    public void updateBySummaryId(long summaryId);
	public void pigeonhole(EdocSummary summary)throws EdocException;
    public void deleteSuperviseDetailAndSupervisors(EdocSummary summary)throws EdocException;

	public CtpSuperviseDetail getSuperviseBySummaryId(long summaryId);
	public int getHastenTimes(long superviseId);
	public void deleteSuperviseDetail(String superviseIds);
	/**
	 * 根据督办明细的ID和督办人的ID删除该督办人在特定督办条目下的下的督办人记录
	 * @param detailId
	 * @param supervisorId
	 * @throws Exception
	 */
	public void superviseForTemplate(String remindMode,String supervisorMemberId,String supervisorNames,String superviseDate,EdocSummary summary,String title) throws Exception;
	public boolean ajaxCheckIsSummaryOver(Long summaryId);
	public String checkColSupervisor(Long summaryId, CtpAffair senderAffair);
	public long saveForTemplate(int importantLevel, String summarySubject,String title,long senderId,String senderName,String supervisorNames
			,long[] supervisorIds,long superviseDate,int entityType,long entityId,boolean sendMessage);
	public void saveSuperviseTemplateRole(long templateId, String supervisors);
	public void updateForTemplate(int importantLevel, String summarySubject,String title,long senderId,String senderName
			,String supervisorNames,long[] supervisorIds,long superviseDate,int entityType,long entityId,boolean sendMessage) throws BusinessException;
	public void updateSuperviseTemplateRole(long templateId, String supervisors) throws BusinessException;
	public List<EdocSuperviseDealModel> getAffairModel(long summaryId);
	public List<CtpSupervisor> listSupervies(Long detailId);
	public List<CtpAffair> getALLAvailabilityAffairList(ApplicationCategoryEnum app,Long objectId, boolean needPagination);
	public List<CtpAffair> getALLAvailabilityAffairList(ApplicationCategoryEnum app,ApplicationSubCategoryEnum subApp,Long objectId, boolean needPagination);
	 public void updateStatusAndNoticeSupervisor(long entityId, int entityType, ApplicationCategoryEnum app, String summarySubject,
	         long userId, String userName, int status, String messageKey, String repealComment, String forwardMemberIdStr) throws BusinessException;
	 
	 public void update(CtpSuperviseVO ctpSuperviseVO,long summaryId) throws BusinessException;
	 public void save(CtpSuperviseVO ctpSuperviseVO) throws BusinessException;
}
