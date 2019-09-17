package com.seeyon.apps.govdoc.manager;

import java.util.List;

import com.seeyon.apps.collaboration.vo.AttachmentVO;
import com.seeyon.apps.edoc.constants.EdocConstant.SendType;
import com.seeyon.apps.govdoc.vo.GovdocAttachmentVO;
import com.seeyon.apps.govdoc.vo.GovdocBaseVO;
import com.seeyon.apps.govdoc.vo.GovdocComponentVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.ContentViewRet;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.v3x.edoc.domain.EdocSummary;

/**
 * 新公文内容管理接口
 * 公文正文、附件、文件
 * @author 唐桂林
 * 
 */
public interface GovdocContentManager {
	
	/*************************** 11111 公文正文查看 start ***************************/
	/**
	 * 内容查看相关处理，返回内容查看列表（支持多正文）
	 * @param moduleType 模块类型，默认值ModuleType.edoc.getKey()
	 * @param moduleId 模块ID，默认值-1（新建），否则为模块多内容新建
	 * @param rightId 新建内容权限ID，默认值-1，例如表单模板权限ID
	 * @return 内容查看Content对象列表（支持多正文）
	 * @throws BusinessException 内容查看相关异常
	 */
	public ContentViewRet contentViewForDetail(ModuleType moduleType, GovdocComponentVO compVO) throws BusinessException;
	
	public void fillSummaryVoByAtt(GovdocSummaryVO summaryVO) throws BusinessException;
    /*************************** 11111 公文正文查看   end ***************************/
    
    /*************************** 22222 公文正文保存 start ***************************/
	/**
	 * 保存公文正文
	 * @param info
	 * @throws BusinessException
	 */
	public void saveBodyContent(GovdocNewVO info) throws BusinessException;
    /**
	 * 分办的时候复制签章 正文等
	 * 
	 * @param newVo
	 * @throws BusinessException
	 */
	public void updateContentByFenban(GovdocNewVO newVo) throws Exception;
	
	public void saveExchangeFormContent(GovdocNewVO newVo) throws BusinessException;
	public void saveExchangeBodyContent(GovdocNewVO newVo) throws BusinessException;
	/*************************** 22222 公文正文保存   end ***************************/	
	
	
	/*************************** 33333 公文附件查看 start ***************************/
	/**
	 * 协同展示附件列表 一、附件来源根据区域划分为：标题区、正文区、附言区、意见区
	 * @param summaryId
	 * @param memberId
	 * @return
	 * @throws BusinessException
	 */
	public List<AttachmentVO> getAttachmentListBySummaryId(Long summaryId, Long memberId) throws BusinessException;
	
	/**
	 * 协同展示附件列表不包含表单中的附件(移动端使用接口)
	 * @param summaryId
	 * @param formAttrUrls 表单附件url，多个附件则以逗号分隔
	 * @param isHistoryFlag
	 * @return
	 * @throws BusinessException
	 */
	public List<GovdocAttachmentVO> getAttachmentListBySummaryIdForMobile(Long summaryId, String formAttrUrls, String isHistoryFlag) throws BusinessException;
	
	/**
	 * 或者正文的附件，包括发起人附言的，主要用于重复发起，编辑待发两种情况下，将发起人附言的附件直接显示到正文区域。
	 * @param summaryId summary的ID
	 * @param info 传入的GovdocNewVO对象，主要用于设置附件说明的参数，为NULL的话就不设置附件说明相关参数
	 * @return
	 * @throws BusinessException
	 */
	public String getSummaryAttachmentJsonsIncludeSender(long summaryId,GovdocNewVO info) throws BusinessException;
	/*************************** 33333 公文附件查看 start ***************************/
	
	
	/*************************** 44444 公文附件保存 start ***************************/
	/**
	 * 保存公文附件
	 * @param info
	 * @param sendType
	 * @throws BusinessException
	 */
	public void saveAttachments(GovdocNewVO info, SendType sendType) throws BusinessException;
	/**
     * 保存附件并发送消息和记录应用日志
     * @param user
     * @param summary
     * @param affair
     * @param commentId
     * @throws BusinessException
     * @Author      : xuqw
     * @Date        : 2016年5月30日下午8:13:56
     */
    public void saveAttDatas(GovdocBaseVO baseVo) throws BusinessException;
    public void saveAttDatas(User user, EdocSummary summary, CtpAffair affair, Long commentId) throws BusinessException;
    
    public String saveAttachment(EdocSummary summary, CtpAffair affair )throws BusinessException;
	public String saveAttachment(EdocSummary summary, CtpAffair affair ,Long commentId) throws BusinessException;
    /**
     * 保存edocOpinion的关联文档信息
     * @param i
     * @param reference
     * @param subReference
     * @param type
     * @param cotegory
     * @param fileName
     * @param mimeType
     * @param fileUrl
     * @throws Exception
     */
	public void saveEdocOpinionRelationAttachment(int i, long reference, long subReference, int type, ApplicationCategoryEnum cotegory, String fileName, String mimeType, long fileUrl) throws Exception;
	/**
	 * 获取前台页面的附件
	 *
	 * @return
	 * @throws BusinessException
	 */
    public String saveAttachmentFromDomain(ApplicationCategoryEnum type, Long module_id) throws BusinessException;
	/*************************** 44444 公文附件保存   end ***************************/
	
    
    /*************************** 55555 公文附件删除 start ***************************/
	/**
     * 删除发起人附件
     * @param id
     */
    public void deleteAttachment4SpecialBack(Long id);
    /*************************** 55555 公文附件删除   end ***************************/
    
    public boolean checkContent(GovdocNewVO newVo) throws BusinessException;
    
    public void setAttachmentJSON(GovdocNewVO newVo,List<Attachment> atts) throws BusinessException;

}
