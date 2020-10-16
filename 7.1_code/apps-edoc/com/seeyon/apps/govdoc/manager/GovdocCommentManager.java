package com.seeyon.apps.govdoc.manager;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.govdoc.constant.GovdocEnum.OperationType;
import com.seeyon.apps.govdoc.vo.GovdocCommentVO;
import com.seeyon.apps.govdoc.vo.GovdocDealVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.comment.CtpCommentAll;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocSummary;

/**
 * 公文意见管理接口(公文单中及单外)
 * @author 唐桂林
 */
public interface GovdocCommentManager {
	
	/*************************** 00000 公文单意见查看 start ***************************/
	public Map<String, String> getOptionPo(GovdocSummaryVO summaryVO) throws BusinessException;
	/*************************** 00000 公文单意见查看   end ***************************/
	
	
	/*************************** 11111 公文单意见保存 start ***************************/
	/**
     * 将处理意见变成草稿状态
     * @param id
     * @param summary
     */
	public Long updateOpinion2Draft(Long id, EdocSummary summary) throws BusinessException;
	/**
	 * 存为草稿：操作者的操作意见被保存在意见框中，包含态度、意见、附件、关联、意见隐藏(2013-12-09 产品经理杨圆确认)；
	 * 将处理意见保存为草稿状态
	 *
	 * @param id
	 * @param summary
	 */
	public void saveOpinionDraft(Long affairId, Long summaryId) throws BusinessException;
	public void saveOpinionPo(EdocOpinion po) throws BusinessException;
	
	public void finishOpinion(EdocSummary summary, CtpAffair affair, FormBean fb, Comment comment, String operation, ColHandleType handleType, String chooseOpinionType);
    public void setOpinionValue(EdocOpinion signOpinion, HttpServletRequest request, CtpAffair affair, EdocSummary summary, Comment comment) throws Exception;
    /*************************** 11111 公文单意见保存   end ***************************/
    
    
    /*************************** 22222 公文单落款电子签名 start ***************************/
    /**
   	 * 物理删除文单审批落款电子签名
   	 */
   public void saveEdocPicSign(EdocOpinion edocOpinion);
   public void deleteEdocPicSign(EdocOpinion edocOpinion);
    /*************************** 22222 公文单落款电子签名 start ***************************/
    
    
    /*************************** 33333 公文单意见删除 start ***************************/
    /**
     * 删除原有意见
     * @throws BusinessException 
     */
    public void deleteOldCommentAndOpinion(Long affairId, EdocSummary summary) throws BusinessException;
    public void deleteOpinionBySummaryId(Long summaryId);
	/*************************** 33333 公文单意见删除   end ***************************/
	
    
	/*************************** 44444 公文单外意见查看 start ***************************/
	/**
	 * 从前端参数-获取公文意见对象
	 * @param optType
	 * @param affairMemberId
	 * @param moduleId
	 * @return
	 */
	public Comment getCommnetFromRequest(OperationType optType, Long affairMemberId, Long moduleId);
	/**
	 * 获取附言
	 * @param moduleType
	 * @param moduleId
	 * @return
	 * @throws BusinessException
	 */
	public List<Long> getSenderCommentIdByModuleIdAndCtype(ModuleType moduleType, Long moduleId) throws BusinessException;

	public List<Comment> getCommentAllByModuleId(ModuleType moduleType, Long moduleId) throws BusinessException;
	public List<Comment> getCommentAllByModuleId(ModuleType moduleType, Long moduleId,boolean isHistoryFlag) throws BusinessException;
    public List<Comment> getCommentList(ModuleType moduleType, Long moduleId) throws BusinessException;
	
	public List<CtpCommentAll> getDealOpinion(Long affairId) throws BusinessException;
	public CtpCommentAll getDrfatComment(Long affairId)  throws BusinessException;
	public Comment getDraftOpinion(Long affairId);
	/**
	 * 构造一个空的Comment对象
	 * @param affairId
	 * @param summaryId
	 * @return
	 */
    public Comment getNullDealComment(Long affairId, Long summaryId);
    /**
     * 获取公文意见对应的操作日志
     * 1.变量所有日志放入Map
     * 2.按commentId遍历日志合并相同操作的日志
     * 3.所有操作日志放入Map中
     * @param processId
     * @return
     * @throws BusinessException
     */
    public Map<String,List<String>> getCommentLog(String processId) throws BusinessException;
	/*************************** 44444 公文单外意见查看   end ***************************/
	
	
	/*************************** 55555 公文单外意见保存 start ***************************/
    /**
	 * 从前端参数-获取公文意见对象并保存
	 * @param optType
	 * @param affairMemberId
	 * @param moduleId
	 * @return
	 * @throws BusinessException
	 */
	public Comment saveCommentFromRequest(OperationType optType, Long affairMemberId, Long moduleId) throws BusinessException;
	
    public void saveComment(GovdocNewVO info, CtpAffair affair) throws BusinessException;
    public void updateComment(Comment comment) throws BusinessException;
    public Comment saveOrUpdateComment(Comment c) throws BusinessException;
    
	/**
	 * 非回复 和 非发起人附言的时候走这个地方，比如撤销，回退，终止，指定回退等。
	 * @param c
	 * @return
	 * @throws BusinessException
	 */
	public Comment saveComment4Repeal(Comment comment, String repealCommentTOHTML, User user, EdocSummary summary, CtpAffair currentAffair) throws BusinessException;
	/**
	 * 保存拟办意见
	 * @param comment
	 * @return
	 */
	public Comment saveDealNibanComment(GovdocDealVO dealVo,Comment comment, CtpAffair affair) throws BusinessException;
	public Comment saveNibanComment(Comment comment);
	public Comment insertNibanComment(Comment comment) throws BusinessException;
	
	/**
	 * 回复意见 及发起人附言的时候走这个方法，前台Ajax请求
	 * @param comment
	 * @param openFrom
	 * @return
	 * @throws BusinessException
	 */
	public Comment insertComment(Comment comment,String openFrom) throws BusinessException;
	public Comment insertComment(Comment comment,CtpAffair affair) throws BusinessException;
    public Comment insertComment(Comment comment) throws BusinessException;
	/*************************** 55555 公文单外意见保存   end ***************************/
	
	
	/*************************** 66666 公文单外意见删除 start ***************************/
	/**
	 * 删除拟办意见
	 * @param moduleType
	 * @param moduleId
	 * @throws BusinessException
	 */
	public void deleteNibanCommentAllByModuleIdAndCtype(ModuleType moduleType, Long moduleId) throws BusinessException;
	public  void deleteCommentAllByModuleIdAndCtypes(ModuleType moduleType, Long moduleId,List<Comment.CommentType> types ) throws BusinessException;
    public void deleteCommentAllByModuleId(ModuleType moduleType, Long moduleId) throws BusinessException;
    	
	public void deleteCommentAllByModuleIdAndCtype(ModuleType moduleType, Long moduleId) throws BusinessException;
	public void deleteComment(ModuleType moduleType, Long commentId) throws BusinessException;
	/*************************** 66666 公文单外意见删除   end ***************************/
	
	public Map<String,Object> findsSummaryComments(Map<String,String> params) throws BusinessException;
	
	public void editComment(GovdocCommentVO commentVO) throws BusinessException;
	
	public String delComment(String commentId) throws BusinessException;
	
	public List<EdocOpinion> findEdocOpinionByAffairId(Long affairId) throws BusinessException;
	
	public List<Map<String, String>> getAttitudeList(String permissionId);
	    
}
