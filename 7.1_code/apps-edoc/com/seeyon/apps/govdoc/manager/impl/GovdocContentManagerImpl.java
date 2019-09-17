package com.seeyon.apps.govdoc.manager.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.collaboration.vo.AttachmentVO;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.edoc.constants.EdocConstant.SendType;
import com.seeyon.apps.govdoc.constant.GovdocAppLogAction;
import com.seeyon.apps.govdoc.helper.GovdocContentHelper;
import com.seeyon.apps.govdoc.manager.GovdocCommentManager;
import com.seeyon.apps.govdoc.manager.GovdocContentManager;
import com.seeyon.apps.govdoc.manager.GovdocLogManager;
import com.seeyon.apps.govdoc.manager.GovdocMessageManager;
import com.seeyon.apps.govdoc.manager.GovdocSignetManager;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.govdoc.vo.GovdocAttachmentVO;
import com.seeyon.apps.govdoc.vo.GovdocAttachmentVO.FromType;
import com.seeyon.apps.govdoc.vo.GovdocBaseVO;
import com.seeyon.apps.govdoc.vo.GovdocComponentVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.apps.index.api.IndexApi;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.affair.util.AttachmentEditUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.content.ContentConfig;
import com.seeyon.ctp.common.content.ContentViewRet;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.mainbody.CtpContentAllBean;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.Constants;
import com.seeyon.ctp.common.filemanager.dao.AttachmentDAO;
import com.seeyon.ctp.common.filemanager.domain.ReplaceBase64Result;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.office.trans.util.OfficeTransHelper;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.processlog.po.ProcessLog;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public class GovdocContentManagerImpl implements GovdocContentManager {

	private Log LOGGER = LogFactory.getLog(GovdocManagerImpl.class);
	
	private GovdocSummaryManager govdocSummaryManager;
	private GovdocCommentManager govdocCommentManager;
	private GovdocSignetManager govdocSignetManager;
	private GovdocLogManager govdocLogManager;
	private GovdocMessageManager govdocMessageManager;
	
	private AffairManager affairManager;
	private OrgManager orgManager;
	private MainbodyManager ctpMainbodyManager;
	private IndexApi indexApi;
	private FileManager fileManager;
	private AttachmentManager attachmentManager;
	private AttachmentDAO attachmentDAO;
	private DocApi docApi;
	
	/*************************** 11111 公文正文 start ***************************/
	@Override
	public ContentViewRet contentViewForDetail(ModuleType moduleType, GovdocComponentVO compVO) throws BusinessException {
		if (moduleType == null) {
			moduleType = ModuleType.edoc;
		}
		Long moduleId = -1L;
		if (compVO.getSummary() != null) {
			moduleId = compVO.getSummary().getId();
		}
		compVO.setModuleId(moduleId);
		
		HttpServletRequest request = (HttpServletRequest) AppContext.getThreadContext(GlobalNames.THREAD_CONTEXT_REQUEST_KEY);

		
		
		ContentConfig contentCfg = ContentConfig.getConfig(ModuleType.edoc);

		List<StateEnum> states = new ArrayList<StateEnum>();
		states.add(StateEnum.col_sent);
		states.add(StateEnum.col_done);
		states.add(StateEnum.col_pending);
		states.add(StateEnum.col_waitSend);
		List<CtpAffair> pushMessageList = affairManager.getAffairs(moduleId, states);
			
		// 排序顺序规则，发起人、已办、暂存待办
		Collections.sort(pushMessageList, new Comparator<CtpAffair>() {
			@Override
			public int compare(CtpAffair o1, CtpAffair o2) {
				if (o1.getState() == StateEnum.col_sent.getKey())
					return -1;
				else if (o2.getState() == StateEnum.col_sent.getKey())
					return 1;
				else {
					if (o1.getState() == StateEnum.col_done.key())
						return -1;
					else if (o2.getState() == StateEnum.col_done.key())
						return 1;
					else
						return 0;
				}
			}
		});
		
		// 过滤掉自己和重复项
		Map<Long, Boolean> memberIdMap = new HashMap<Long, Boolean>(pushMessageList.size());
		// 消息推送
		List<CtpAffair> pushMessageListAffair = new ArrayList<CtpAffair>();
		Long currentUserId = AppContext.currentUserId();
		for (CtpAffair r : pushMessageList) {
			// 只显示已发、暂存待办和、已办的、回退者
			int subState = r.getSubState();
			int state = r.getState();
			if ((subState == SubStateEnum.col_pending_ZCDB.key() && state == StateEnum.col_pending.key()) || state == StateEnum.col_done.key()
					|| state == StateEnum.col_sent.key() || subState == SubStateEnum.col_pending_specialBack.key()
					|| subState == SubStateEnum.col_pending_specialBacked.key() || subState == SubStateEnum.col_pending_specialBackCenter.key()
					|| state == StateEnum.col_pending.key()) {
				Long memberId = r.getMemberId();
				if (!memberId.equals(currentUserId) && memberIdMap.get(memberId) == null) {
					memberIdMap.put(memberId, Boolean.TRUE);
					pushMessageListAffair.add(r);
				}
			}
		}
		
		request.setAttribute("commentPushMessageToMembersList", pushMessageListAffair);
		request.setAttribute("__huanhang", "\r\n");
		
		request.setAttribute("contentCfg", contentCfg);
		
		ContentViewRet context = new ContentViewRet();
		context.setModuleId(moduleId);
		context.setModuleType(moduleType.getKey());
		
		Long affairId = compVO.getAffairId();
		context.setAffairId(affairId);
		request.setAttribute("contentContext", context);
		return context;
	}
	public void fillSummaryVoByAtt(GovdocSummaryVO summaryVO) throws BusinessException {
		// 正文区域附件
		String arrListJSON = "";
		List<Attachment> mainAtt = new ArrayList<Attachment>();
		//表单上的附件
		List<Attachment> formAtt = new ArrayList<Attachment>();
		//流转附件
		List<Attachment> commentShowAttrs = new ArrayList<Attachment>();
		// 显示保存了草稿的意见、附件、意见隐藏不包括人
		List<Attachment> allAttachments = new ArrayList<Attachment>();
		List<Attachment> temp = attachmentManager.getByReference(summaryVO.getSummaryId());
		// add by rz 2010-11-11 [修改bug附件显示顺序不正确] start
		if (!temp.isEmpty()) {
			Collections.sort(temp, new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					Attachment stu1 = (Attachment) o1;
					Attachment stu2 = (Attachment) o2;
					return Integer.valueOf(stu1.getSort()).compareTo(Integer.valueOf(stu2.getSort()));
				}
			});
			allAttachments.addAll(temp);
		}
		comtentDraftAttAndDis(allAttachments, summaryVO);
		if (Strings.isNotEmpty(allAttachments)) {
			for (Attachment att : allAttachments) {
				if(summaryVO.getSummaryId().equals(att.getSubReference())
						&& (Integer.valueOf(com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE.ordinal()).equals(att.getType()) 
								|| Integer.valueOf(com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.DOCUMENT.ordinal()).equals(att.getType()))) {
						mainAtt.add(att);
				}else if(att.getCategory() == ApplicationCategoryEnum.form.getKey()){
					formAtt.add(att);
				}else{
					commentShowAttrs.add(att);
				}
			}
		}
		// 区分老公文还是新公文 然后设置类型 让关联文档正常打开
		for (Attachment att : mainAtt) {
			if (att.getFileUrl() != null) {
				CtpAffair aff = affairManager.get(att.getFileUrl());
				if (aff != null && aff.getApp() > 4) {
					att.setMimeType("edoc");
				}
			}
		}
		//一屏式附件
		if(!"0".equals(summaryVO.getSwitchVo().getNewGovdocView())){
			int attNum = 0;
	        List<Attachment> setAtt = new ArrayList<Attachment>();
			mainAtt.addAll(formAtt);
			for(Attachment a:mainAtt){
				try{
					Attachment clone = (Attachment) BeanUtils.cloneBean(a);
					clone.setSubReference(a.getReference());
					setAtt.add(clone);
				}catch(Exception e){
					
				}
			}
			mainAtt.clear();
			mainAtt.addAll(setAtt);
			List<Attachment> commentAttList = new ArrayList<Attachment>();
			List<Comment> commentList = govdocCommentManager.getCommentAllByModuleId(ModuleType.edoc, summaryVO.getSummary().getId());
			for (Comment comment : commentList) {
				if(comment.getCtype()==Comment.CommentType.draft.getKey()||
						comment.getCtype()==Comment.CommentType.govdocniban.getKey()){//不显示存为模板附件
					continue;
				}
				String atts = comment.getRelateInfo();
	            if(Strings.isNotBlank(atts) && atts.indexOf(":") != -1){
	                try{
	                    List list = JSONUtil.parseJSONString(atts,List.class);
	                    List<Attachment> l = ParamUtil.mapsToBeans(list, Attachment.class, false);
	                    
	                    commentAttList.addAll(l);
	                }catch(Exception e){
	                	LOGGER.error(e);
	                }
	            }
	            //add by rz 2017-09-09 [处理代录数据] start
	     		String pishiComments=comment.getContent();
	     		if(StringUtils.isNotBlank(pishiComments)){
		     		if(pishiComments.indexOf("代录")!=-1){
		     			comment.setExtAtt2("pishi");
		     		}
	     		}
	            //add by rz 2017-09-09 [处理代录数据] end
			}
			List<Attachment> commentAtt = new ArrayList<Attachment>();
			for(Attachment a:commentAttList){
				try{
					Attachment clone = (Attachment) BeanUtils.cloneBean(a);
					clone.setSubReference(a.getReference());
					commentAtt.add(clone);
				}catch(Exception e){
					
				}
			}
			commentAttList.clear();
			commentAttList.addAll(commentAtt);
			String commentShowAttrstr = attachmentManager.getAttListJSON(commentAttList);
			AppContext.putRequestContext("commentShowAttrstr",commentShowAttrstr);
			if(mainAtt!=null&&commentShowAttrs!=null){
				attNum = mainAtt.size()+commentAttList.size();
			}
			AppContext.putRequestContext("attNum",attNum);
		}
		arrListJSON = attachmentManager.getAttListJSON(mainAtt);
		AppContext.putRequestContext("attListJSON", arrListJSON);
		//表单中附件的文件名(附件说明用到)
		String formAttsName = attachmentManager.getFileNameExcludeSuffix(formAtt);
		AppContext.putRequestContext("formAttsName", formAttsName);
		//正文区附件的文件名(附件说明用到)
		String contentAttsName = attachmentManager.getFileNameExcludeSuffix(mainAtt);
		AppContext.putRequestContext("contentAttsName", contentAttsName);
	}
	/**
	 * 显示保存了草稿的意见、附件、意见隐藏不包括人
	 *
	 * @param attachments
	 * @param affair
	 */
	private void comtentDraftAttAndDis(List<Attachment> attachments, GovdocSummaryVO summaryVO) {
		CtpAffair affair = summaryVO.getAffair();
		// 处理区域附件
		String handleAttach = "";
		Long commentId = null;
		List<Attachment> dealAtt = new ArrayList<Attachment>();

		// 获取草稿
		Comment comentDraft = govdocCommentManager.getDraftOpinion(affair.getId());
		//客开 项目名称： [修改功能：] 作者：fzc 修改日期：2018-4-28 start
 		AppContext.putRequestContext("commentDraft", comentDraft);
 		//客开 项目名称： [修改功能：] 作者：fzc 修改日期：2018-4-28 end
		// 获取意见隐藏的ids
		String displayIds = "";
		// 获取意见隐藏的Names
		String displayNames = "";
		try {
			if (affair.getState().equals(StateEnum.col_pending.key())) {
				if (comentDraft != null) {
					commentId = comentDraft.getId();
					for (Attachment att : attachments) {
						if (commentId.equals(att.getSubReference())) {
							dealAtt.add(att);
						}
					}
					// 意见隐藏
					if (comentDraft.getShowToId() != null) {
						String[] ids = comentDraft.getShowToId().split(",");
						if (!"".equals(ids[0])) {
							for (int i = 0; i < ids.length; i++) {
								V3xOrgMember mem = orgManager.getMemberById(Long.valueOf(ids[i]));
								if (i == ids.length - 1) {
									displayIds = displayIds + "Member|" + ids[i];
									displayNames = displayNames + mem.getName();
								} else {
									displayIds = displayIds + "Member|" + ids[i] + ",";
									displayNames = displayNames + mem.getName() + ",";
								}
							}
						}
					}
				}
			} else {
				// 设置一个标识，用于页面的附件区域
				commentId = 20L;
			}
		} catch (Exception e) {
			LOGGER.error("获取草稿中的附件、意见隐藏人报错！", e);
		}

		summaryVO.setDisplayIds(displayIds);
		summaryVO.setDisplayNames(displayNames);
		
		handleAttach = attachmentManager.getAttListJSON(dealAtt);
		AppContext.putRequestContext("commentId", commentId);
		AppContext.putRequestContext("handleAttachJSON", handleAttach);
	}
	/*************************** 11111 公文正文查看   end ***************************/
	
	
	/*************************** 22222 公文正文保存 start ***************************/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public CtpContentAllBean saveContentFromRequest(boolean resendFlag, boolean checkformRule) throws BusinessException {
		Map<String, String> div = ParamUtil.getJsonDomain("_currentDiv");
		Map params = ParamUtil.getJsonDomain("colMainData");
		String curDiv = div.get("_currentDiv");

		CtpContentAllBean content = new CtpContentAllBean();
		String viewState = String.valueOf(params.get("contentViewState"));
		String summaryId = String.valueOf(params.get("id"));
		String parentSummaryId = String.valueOf(params.get("parentSummaryId"));
		// 如果正文是只读的则只需要取原正文
		if (!"null".equals(viewState) && !"null".equals(summaryId) && Strings.isNotBlank(viewState) && Strings.isNotBlank(summaryId)
				&& Integer.parseInt(viewState) == CtpContentAllBean.viewState_readOnly) {
			List<CtpContentAll> contentList = ctpMainbodyManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, Long.valueOf(summaryId));
			if (Strings.isNotEmpty(contentList)) {
				content = new CtpContentAllBean(contentList.get(0));
				if (resendFlag) {
					content.setId(null);
					content.setModuleId(Long.valueOf(summaryId));
					ctpMainbodyManager.transContentSaveOrUpdate(content);
				}
			} else if (Strings.isNotBlank(parentSummaryId)) {
				List<CtpContentAll> clist = ctpMainbodyManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, Long.valueOf(parentSummaryId));
				if (Strings.isNotEmpty(clist)) {
					content = new CtpContentAllBean(clist.get(0));
					if (resendFlag) {
						content.setId(null);
						content.setModuleId(Long.valueOf(summaryId));
						ctpMainbodyManager.transContentSaveOrUpdate(content);
					}
				}
			}

		} else {
			//String processId = null;
			//Long caseId = -1l;
			String useForSaveTemplate = (String) ParamUtil.getJsonDomain("colMainData").get("useForSaveTemplate");

			ParamUtil.getJsonDomainToBean("mainbodyDataDiv_" + curDiv, content);

			if (!checkformRule) {// 保存待发的时候不需要验证规则
				content.putExtraMap("needCheckRule", "false");
			}
			String tId = (String) ParamUtil.getJsonDomain("colMainData").get("tId");
			String useforStr = (String) ParamUtil.getJsonDomain("templateMainData").get("id");
			// String useForSaveTemplate
			// =(String)ParamUtil.getJsonDomain("colMainData").get("useForSaveTemplate");
			/**
			 * 存模板的时候 useForSaveTemplate ="yes" 发送 保存待发的时候均走下面 模板的时候必须走上面 tId 为
			 * 空 设置正文类型为-1
			 */
			if ("yes".equals(useForSaveTemplate) || Strings.isNotBlank(useforStr)) {
				content.putExtraMap("moduleTemplateId", Long.valueOf("-1"));// 前台掉模板存个人模板的时候
			} else if (Strings.isBlank(tId)) {// 不存在模板的时候 正文为没有调用模板产生的业务功能数据
												// 设置为0
				content.putExtraMap("moduleTemplateId", Long.valueOf("0"));
			} else {
				content.putExtraMap("moduleTemplateId", Long.valueOf(tId));
			}

			// 如果是重复发起，这里讲content的ID和moduleId 全部重置为空
			if (resendFlag == true) {
				content.setId(null);
			}
			/**
			 * add by libing at 2012-11-12
			 * des:后台修改模板的时候，这里如果templateId不为空,将templateId设置为content的moduleId，
			 * 避免新正文ID修改模板ID导致 更新的时候出错。
			 */

			if (Strings.isNotBlank((String) ParamUtil.getJsonDomain("templateMainData").get("templateId"))) {
				content.setModuleId(Long.parseLong((String) ParamUtil.getJsonDomain("templateMainData").get("templateId")));
			}
			if (content.isEditable()) {
				// 正文可编辑时进行正文内容保存
				ctpMainbodyManager.transContentSaveOrUpdate(content);
			}
		}
		return content;
	}

	
	@Override
	public void saveBodyContent(GovdocNewVO newVo) throws BusinessException {
		if(newVo.getBodyVo().getContent() == null) {
			return;
		}
		CtpContentAll newContentAll = GovdocContentHelper.getBodyContentByModuleId(newVo.getSummary().getId());
		if (null == newContentAll) {
			newContentAll = new CtpContentAll();
			newContentAll.setIdIfNew();
		} 
		newContentAll.setModifyDate(new Date());
		newContentAll.setModuleId(newVo.getSummary().getId());
		newContentAll.setCreateId(newVo.getCurrentUser().getId());
		newContentAll.setContentTemplateId(0L);
		newContentAll.setModuleTemplateId(0L);
		newContentAll.setCreateDate(new Date());
		newContentAll.setContent(String.valueOf(newVo.getBodyVo().getContent()));
		newContentAll.setSort(1);
		newContentAll.setModuleType(ApplicationCategoryEnum.edoc.getKey()); // 公文类型
		newContentAll.setContentType(newVo.getBodyVo().getContentType());
		newContentAll.setModifyId(newVo.getCurrentUser().getId());

		ReplaceBase64Result result = fileManager.replaceBase64Image(newContentAll.getContent());
		newContentAll.setContent(result.getHtml());

		ctpMainbodyManager.saveOrUpdateContentAll(newContentAll);
	}
	
	/**
	 * 分办的时候复制签章 正文等
	 * 
	 * @param newVo
	 * @throws BusinessException
	 */
	@Override
	public void updateContentByFenban(GovdocNewVO newVo) throws Exception {
		if (newVo.getSignSummaryId() != null) {
			List<CtpContentAll> ctpContentAlls = GovdocContentHelper.getBodyContentListByModuleId(newVo.getSignSummaryId());
			for (CtpContentAll ctpContentAll : ctpContentAlls) {
				if (ctpContentAll.getContentType() != MainbodyType.HTML.getKey()) {// 如果不是标准正文
					Long fileId = Long.parseLong(ctpContentAll.getContent());
					Long newFileId = fileManager.copyFileBeforeModify(fileId);
					if (newFileId != -1L) {
						ctpContentAll.setContent(String.valueOf(newFileId));
						// 复制签章
						govdocSignetManager.copySignet(fileId, newFileId);
					}
					
				}
				CtpContentAll newConetent = (CtpContentAll) ctpContentAll.clone();
				newConetent.setId(UUIDLong.longUUID());
				newConetent.setModuleId(newVo.getSummaryId());
				newConetent.setCreateDate(new Date());
				newConetent.setModifyDate(new Date());
				ctpMainbodyManager.saveOrUpdateContentAll(newConetent);
			}
			
			// 获取附件
			List<Attachment> attachments = attachmentManager.getByReference(newVo.getSignSummaryId());
			List<Attachment> attachs = new ArrayList<Attachment>();
			if (attachments != null) {
				for (Attachment attachment : attachments) {
					if (attachment.getType() != Constants.ATTACHMENT_TYPE.FILE.ordinal()) {
						continue;
					}
					if (attachment.getCategory() == ApplicationCategoryEnum.edoc.getKey()) {
						attachs.add(attachment);
					}
				}
				newVo.setFenbanFile(1);
				String attListJSON = attachmentManager.getAttListJSON(attachs);
				newVo.setAttListJSON(attListJSON);
			}
		}
	}
	
    @Override
    public void saveExchangeFormContent(GovdocNewVO newVo) throws BusinessException {
    	CtpContentAll formContent = newVo.getBodyVo().getFormContent();
        if(formContent != null) {
        	formContent.setModuleId(newVo.getSummary().getId());
        	formContent.setCreateId(newVo.getCurrentUser().getId());
        	formContent.setContentTemplateId(newVo.getSummary().getFormAppid());
        	formContent.setModuleTemplateId(newVo.getSummary().getTempleteId());
        	formContent.setTitle(newVo.getSummary().getSubject());
            ctpMainbodyManager.saveOrUpdateContentAll(formContent);
        }
    }
    
    @Override
    public void saveExchangeBodyContent(GovdocNewVO newVo) throws BusinessException {
    	CtpContentAll bodyContent = newVo.getBodyVo().getBodyContent();
        if(bodyContent != null) {
        	bodyContent.setNewId();
        	bodyContent.setTitle(null);
        	bodyContent.setContentDataId(null);
        	bodyContent.setContentTemplateId(0L);
        	bodyContent.setModuleTemplateId(newVo.getSummary().getTempleteId());
        	bodyContent.setCreateId(newVo.getCurrentUser().getId());
        	bodyContent.setModuleId(newVo.getSummary().getId());
//        	bodyContent.setTransId(null);
            ctpMainbodyManager.saveOrUpdateContentAll(bodyContent);
        }
    }
	/*************************** 22222 公文正文保存   end ***************************/
	
	
	/*************************** 33333 公文附件查看 start ***************************/
	@SuppressWarnings("rawtypes")
	@Override
	public List<AttachmentVO> getAttachmentListBySummaryId(Long summaryId, Long memberId) throws BusinessException {
		List<Attachment> tempattachments = null;
		List<Attachment> attachments = new ArrayList<Attachment>();
		List<AttachmentVO> attachmentVOs = new ArrayList<AttachmentVO>();
		// 收藏时候传入的文件Id
		List<Long> attmentIds = new ArrayList<Long>();
		AttachmentVO vo = null;
		HttpServletRequest request = AppContext.getRawRequest();
		String formAttrId = request.getParameter("formAttrId");
		List formAttrIds = new ArrayList<String>();
		// 获取正文组件中自带的附件
		String attmentContent = request.getParameter("attmentList");
		List<String> attmentList = new ArrayList<String>();
		List<String> _attmentList = new ArrayList<String>();
		if (Strings.isNotBlank(attmentContent) && !"null".equals(attmentContent)) {
			_attmentList = Arrays.asList(attmentContent.split(","));
			for (int a = _attmentList.size() - 1; a > -1; a--) {
				if (!attmentList.contains(_attmentList.get(a))) {
					attmentList.add(_attmentList.get(a));
				}
			}
		}
		if (Strings.isNotBlank(formAttrId)) {
			formAttrIds = Arrays.asList(formAttrId.split(","));
		}
		EdocSummary colSummary = govdocSummaryManager.getSummaryById(summaryId);
		tempattachments = attachmentManager.getByReference(summaryId);
		
		//附件列表显示顺序按照sort显示
		if (!tempattachments.isEmpty()) {
			Collections.sort(tempattachments, new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					Attachment stu1 = (Attachment) o1;
					Attachment stu2 = (Attachment) o2;
					return Integer.valueOf(stu1.getSort()).compareTo(Integer.valueOf(stu2.getSort()));
				}
			});
		}

		// 控制隐藏的评论对发起人可见
		AppContext.putThreadContext(Comment.THREAD_CTX_NOT_HIDE_TO_ID_KEY, colSummary.getStartMemberId());

		// 添加附件到对象中，附件的type为0，关联文档的type为2（不显示关联文档在附件列表中）
		for (Attachment attachment : tempattachments) {
			if (attachment.getType() == 0) {
				attachments.add(attachment);
			}
		}
		if (attachments != null && attachments.size() > 0) {
			boolean isHistoryFlag = "true".equals(request.getParameter("isHistoryFlag")) ? true : false;
			List<Comment> comments = govdocCommentManager.getCommentAllByModuleId(ModuleType.edoc, summaryId, isHistoryFlag);
			//List<Comment> childComments = new ArrayList<Comment>();
			for (Attachment attachment : attachments) {
				vo = new AttachmentVO();
				createAttachmentVO(vo, attachment);
				// 创建人
				Long commentId = attachment.getSubReference();
				Comment curComment = null;
				for (Comment comment : comments) {
					if (comment.getId().longValue() == commentId.longValue()) {
						curComment = comment;
						break;
					}
				}
				/**
				 * 1、发起人上传：标题下方的由发起人上传的附件，如果流程中间的节点跟发起人是同一个人，也算”处理人回复“
				 * 2、处理人回复：意见区域的附件（评论&回复），通过修改附件区域上传的附件 3、表单控件：表单正文里面的附件
				 * 4、附言补充：发起人附言区域里面的附件
				 */

				if (curComment != null) {
					if (!curComment.isCanView())
						continue;
					String agentName = curComment.getExtAtt2();
					Long createId = curComment.getCreateId();
					if (Strings.isNotBlank(agentName)) {
						vo.setUserName(agentName);
					} else {
						V3xOrgMember member = orgManager.getMemberById(createId);
						vo.setUserName(member.getName());
					}

					String fromType = "";
					// 来源
					Integer cType = curComment.getCtype();
					// 如果是草稿状态时，不显示附件
					if (cType != null) {
						int ct = cType.intValue();
						if (ct == -2) {
							continue;
						}
						if (curComment.getCtype().intValue() == Comment.CommentType.govdocniban.getKey())// 如果是拟办意见的，不显示
							continue;
						// 如果是转发的则显示成正文区
						if (curComment.getForwardCount() > 0) {
							fromType = ResourceUtil.getString("collaboration.att.form");
						} else {
							if (ct == -1) {
								fromType = ResourceUtil.getString("collaboration.att.sender");// "附言区";
							} else if (ct == 1 || ct == 0) {
								fromType = ResourceUtil.getString("collaboration.att.reply");// "处理区";
							}
						}
					}
					attmentIds.add(attachment.getFileUrl());
					vo.setFromType(fromType);
					attachmentVOs.add(vo);
				} else if (summaryId.equals(attachment.getSubReference())) {
					// 标题区
					Date attaDate = attachment.getCreatedate();
					Date colDate = colSummary.getCreateTime();
					// 将协同创建时间加上2秒,以判断是否是发起人上传，因为在发起人上传时，必须保证附件的创建时间小于协同的创建时间
					// 但是在实际情况中由于保存时都是用的new Date()方式存储日期，导致时间有误差(ms级)
					if (attaDate.getTime() < colDate.getTime() + 2000) {
						try {
							V3xOrgMember member = orgManager.getMemberById(colSummary.getStartMemberId());
							vo.setUserName(member.getName());
						} catch (Exception e) {
							LOGGER.error("", e);
						}
						vo.setFromType(ResourceUtil.getString("collaboration.att.titleArea"));// 标题区
					} else {
						V3XFile file = fileManager.getV3XFile(attachment.getFileUrl());
						String name = "";
						if (file != null) {
							try {
								V3xOrgMember member = orgManager.getMemberById(file.getCreateMember());
								name = member.getName();
							} catch (Exception e) {
								LOGGER.error("", e);
							}
						}
						vo.setUserName(name);
						// 处理人回复（处理区）
						vo.setFromType(ResourceUtil.getString("collaboration.att.titleArea"));
					}
					attmentIds.add(attachment.getFileUrl());
					attachmentVOs.add(vo);
				} else if (AttachmentEditUtil.CONTENT_ATTACHMENTSUBRE.equals(attachment.getSubReference().toString())) { // 来之转发表单控件的附件
					V3XFile file = fileManager.getV3XFile(attachment.getFileUrl());
					String name = "";
					if (file != null) {
						try {
							V3xOrgMember member = orgManager.getMemberById(file.getCreateMember());
							name = member.getName();
						} catch (Exception e) {
							LOGGER.error("", e);
						}
					}
					vo.setUserName(name);
					// （来自表单转发控件的属于正文区域）
					vo.setFromType(ResourceUtil.getString("collaboration.att.form"));
					attmentIds.add(attachment.getFileUrl());
					attachmentVOs.add(vo);
				}
			}
		}
		// 表单中的附件
		if (formAttrIds != null && formAttrIds.size() > 0) {
			List<CtpContentAll> contentList = GovdocContentHelper.getContentListByModuleId(summaryId);
			if(contentList!=null && contentList.size()>0){
				for (int k = 0; k < contentList.size(); k++) {	
					Integer contentType=contentList.get(k).getContentType();
					if(contentType!=null && contentType.intValue()==20){//当其中一个正文是表单时则过滤表单中的附件
						for (int i = 0; i < formAttrIds.size(); i++) {
							vo = new AttachmentVO();
							try {
								Attachment attachment = this.attachmentManager.getAttachmentByFileURL(Long.valueOf(formAttrIds.get(i).toString()));
								createAttachmentVO(vo, attachment);
								V3XFile file = fileManager.getV3XFile(Long.valueOf(formAttrIds.get(i).toString()));
								String name = "";
								if (file != null) {
									V3xOrgMember member = orgManager.getMemberById(file.getCreateMember());
									name = member.getName();
								}
								vo.setUserName(name);
								// 表单控件（来自表单控件的属于正文区域）
								vo.setFromType(ResourceUtil.getString("collaboration.att.form"));
								attmentIds.add(attachment.getFileUrl());
								attachmentVOs.add(vo);
							} catch (Exception e) {
								LOGGER.error("获取表单中的附件报错！", e);
							}
						}
					}
				}
			}
		}

		// 添加正文区域中的附件
		if (attmentList != null && attmentList.size() > 0) {
			for (int i = 0; i < attmentList.size(); i++) {
				vo = new AttachmentVO();
				Long fileId = Long.parseLong(attmentList.get(i));
				if (fileId != null) {
					try {
						Attachment attachment = this.attachmentManager.getAttachmentByFileURL(fileId);
						createAttachmentVO(vo, attachment);
						V3XFile file = fileManager.getV3XFile(fileId);
						V3xOrgMember member = orgManager.getMemberById(file.getCreateMember());
						String name = member.getName();
						vo.setUserName(name);
						// 正文区
						vo.setFromType(ResourceUtil.getString("collaboration.att.form"));
						attmentIds.add(attachment.getFileUrl());
						attachmentVOs.add(vo);
					} catch (Exception e) {
						LOGGER.error("获取正文区域中的附件报错！", e);
					}
				}

			}

		}
		//收藏
        String collectFlag = SystemProperties.getInstance().getProperty("doc.collectFlag");
        if("true".equals(collectFlag) && AppContext.hasPlugin("doc")){
            List<Map<String,Long>> collectMap = docApi.findFavorites(AppContext.currentUserId(),attmentIds);
            Map<Long,Long> doc2SourceId = new HashMap<Long,Long>();
            for (Map<String, Long> map : collectMap) {
                doc2SourceId.put(map.get("sourceId"), map.get("id"));
            }
            for (AttachmentVO attachment : attachmentVOs) {
                if(doc2SourceId.get(Long.valueOf(attachment.getFileUrl()))!=null){
                    attachment.setCollect(true);
                }
            }
        }
		// 排序
		Collections.sort(attachmentVOs, new Comparator<AttachmentVO>(){

			@Override
			public int compare(AttachmentVO o1, AttachmentVO o2) {
				int n = 1;
				if(o1.getUploadTime().before(o2.getUploadTime())){
					n = -1;
				}
				return n;
			}
			
		});
		return attachmentVOs;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public List<GovdocAttachmentVO> getAttachmentListBySummaryIdForMobile(Long summaryId, String formAttrUrls, String isHistoryFlag) throws BusinessException {
		List<Attachment> tempattachments = null;
		List<Attachment> attachments = new ArrayList<Attachment>();
		List<GovdocAttachmentVO> attachmentVOs = new ArrayList<GovdocAttachmentVO>();
		// 收藏时候传入的文件Id
		List<Long> attmentIds = new ArrayList<Long>();
		GovdocAttachmentVO vo = null;
		HttpServletRequest request = AppContext.getRawRequest();
		List formAttrIdList = null;
		// 获取正文组件中自带的附件
		String attmentContent = request.getParameter("attmentList");
		List<String> attmentList = new ArrayList<String>();
		List<String> _attmentList = new ArrayList<String>();
		if (Strings.isNotBlank(attmentContent) && !"null".equals(attmentContent)) {
			_attmentList = Arrays.asList(attmentContent.split(","));
			for (int a = _attmentList.size() - 1; a > -1; a--) {
				if (!attmentList.contains(_attmentList.get(a))) {
					attmentList.add(_attmentList.get(a));
				}
			}
		}
		if (Strings.isNotBlank(formAttrUrls)) {
			formAttrIdList = Arrays.asList(formAttrUrls.split(","));
		}
		EdocSummary colSummary = govdocSummaryManager.getSummaryById(summaryId);
		tempattachments = attachmentManager.getByReference(summaryId);
		
		//附件列表显示顺序按照sort显示
		if (!tempattachments.isEmpty()) {
			Collections.sort(tempattachments, new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					Attachment stu1 = (Attachment) o1;
					Attachment stu2 = (Attachment) o2;
					return Integer.valueOf(stu1.getSort()).compareTo(Integer.valueOf(stu2.getSort()));
				}
			});
		}

		// 控制隐藏的评论对发起人可见
		AppContext.putThreadContext(Comment.THREAD_CTX_NOT_HIDE_TO_ID_KEY, colSummary.getStartMemberId());

		// 添加附件到对象中，附件的type为0，关联文档的type为2（不显示关联文档在附件列表中）
		for (Attachment attachment : tempattachments) {
//			if (attachment.getType() == 0) {
				attachments.add(attachment);
//			}
		}
		if (attachments != null && attachments.size() > 0) {
			List<Comment> comments = govdocCommentManager.getCommentAllByModuleId(ModuleType.edoc, summaryId, "true".equals(isHistoryFlag));
			//List<Comment> childComments = new ArrayList<Comment>();
			for (Attachment attachment : attachments) {
				vo = new GovdocAttachmentVO();
				createGovdocAttachmentVO(vo, attachment);
				// 创建人
				Long commentId = attachment.getSubReference();
				Comment curComment = null;
				for (Comment comment : comments) {
					if (comment.getId().longValue() == commentId.longValue()) {
						curComment = comment;
						break;
					}
				}
				/**
				 * 1、发起人上传：标题下方的由发起人上传的附件，如果流程中间的节点跟发起人是同一个人，也算”处理人回复“
				 * 2、处理人回复：意见区域的附件（评论&回复），通过修改附件区域上传的附件 3、表单控件：表单正文里面的附件
				 * 4、附言补充：发起人附言区域里面的附件
				 */

				if (curComment != null) {
					if (!curComment.isCanView())
						continue;
					// 来源
					FromType fromType = FromType.title;
					Integer cType = curComment.getCtype();
					// 如果是草稿状态时，不显示附件
					if (cType != null) {
						int ct = cType.intValue();
						if (ct == -2) {
							continue;
						}
						if (curComment.getCtype().intValue() == Comment.CommentType.govdocniban.getKey())// 如果是拟办意见的，不显示
							continue;
						// 如果是转发的则显示成正文区
						if (curComment.getForwardCount() > 0) {
							fromType = FromType.form;
						} else {
							if (ct == -1) {
								fromType = FromType.sender;
							} else if (ct == 1 || ct == 0) {
								fromType = FromType.reply;// "处理区";
							}
						}
					}
					attmentIds.add(attachment.getFileUrl());
					vo.setFrom(fromType.ordinal());
					attachmentVOs.add(vo);
				} else if (summaryId.equals(attachment.getSubReference())) {
					// 标题区
					Date attaDate = attachment.getCreatedate();
					Date colDate = colSummary.getCreateTime();
					// 将协同创建时间加上2秒,以判断是否是发起人上传，因为在发起人上传时，必须保证附件的创建时间小于协同的创建时间
					// 但是在实际情况中由于保存时都是用的new Date()方式存储日期，导致时间有误差(ms级)
					FromType fromType = FromType.title;
					if (attaDate.getTime() < colDate.getTime() + 2000) {
						fromType = FromType.title;
					}
					vo.setFrom(fromType.ordinal());
					attmentIds.add(attachment.getFileUrl());
					attachmentVOs.add(vo);
				} else if (AttachmentEditUtil.CONTENT_ATTACHMENTSUBRE.equals(attachment.getSubReference().toString())) { // 来之转发表单控件的附件
					// （来自表单转发控件的属于正文区域）
					vo.setFrom(FromType.form.ordinal());
					attmentIds.add(attachment.getFileUrl());
					attachmentVOs.add(vo);
				}
			}
		}
		// 表单中的附件
		if (formAttrIdList != null && formAttrIdList.size() > 0) {
			List<CtpContentAll> contentList = GovdocContentHelper.getContentListByModuleId(summaryId);
			if(contentList!=null && contentList.size()>0){
				for (int k = 0; k < contentList.size(); k++) {	
					Integer contentType=contentList.get(k).getContentType();
					if(contentType != null && contentType.intValue() == 20){//当其中一个正文是表单时则过滤表单中的附件
						for (int i = 0; i < formAttrIdList.size(); i++) {
							vo = new GovdocAttachmentVO();
							try {
								Attachment attachment = this.attachmentManager.getAttachmentByFileURL(Long.valueOf(formAttrIdList.get(i).toString()));
								vo = new GovdocAttachmentVO();
								createGovdocAttachmentVO(vo, attachment);
								// 表单控件（来自表单控件的属于正文区域）
								vo.setFrom(FromType.form.ordinal());
								attmentIds.add(attachment.getFileUrl());
								attachmentVOs.add(vo);
							} catch (Exception e) {
								LOGGER.error("获取表单中的附件报错！", e);
							}
						}
					}
				}
			}
		}		

		// 添加正文区域中的附件
		if (attmentList != null && attmentList.size() > 0) {
			for (int i = 0; i < attmentList.size(); i++) {
				vo = new GovdocAttachmentVO();
				Long fileId = Long.parseLong(attmentList.get(i));
				if (fileId != null) {
					try {
						Attachment attachment = this.attachmentManager.getAttachmentByFileURL(fileId);
						createGovdocAttachmentVO(vo, attachment);
						// 正文区
						vo.setFrom(FromType.form.ordinal());
						attmentIds.add(attachment.getFileUrl());
						attachmentVOs.add(vo);
					} catch (Exception e) {
						LOGGER.error("获取正文区域中的附件报错！", e);
					}
				}

			}
		}
		return attachmentVOs;
	}
	
	@Override
	public String getSummaryAttachmentJsonsIncludeSender(long summaryId,GovdocNewVO info) throws BusinessException {
		// 取正文的附件和发起人附言的。
		List<Attachment> showAtts = new ArrayList<Attachment>();
		//表单上的附件
		List<Attachment> formAtt = new ArrayList<Attachment>();
		//正文区除去关联文档的附件
		List<Attachment> contentAtt = new ArrayList<Attachment>();
		List<Attachment> list = attachmentManager.getByReference(summaryId);
		List<Comment> comments = govdocCommentManager.getCommentList(ModuleType.edoc, summaryId);
		List<Long> showlds = new ArrayList<Long>();
		showlds.add(summaryId);
		if (Strings.isNotEmpty(comments)) {
			for (Comment c : comments) {
				if (Integer.valueOf(Comment.CommentType.sender.getKey()).equals(c.getCtype())) {
					showlds.add(c.getId());
				}
			}
		}
		if (Strings.isNotEmpty(list)) {
			for (Attachment a : list) {
				if (showlds.contains(a.getSubReference())) {
					Attachment aclone = null;
					try {
						aclone = (Attachment) a.clone();
						aclone.setSubReference(a.getReference());
					} catch (CloneNotSupportedException e) {
						LOGGER.error("", e);
					}
					if (aclone != null) {
						showAtts.add(aclone);
						if(aclone.getType() != 2){
							contentAtt.add(aclone);
						}
					}
				}else{
					if(a.getCategory() == ApplicationCategoryEnum.form.getKey()){
						formAtt.add(a);
					}
				}
			}
		}
		if(info != null){
			//表单中附件的文件名(附件说明用到)
			String formAttsName = attachmentManager.getFileNameExcludeSuffix(formAtt);
			info.setFilesmFormAttsName(formAttsName);
			//正文区附件的文件名(附件说明用到)
			String contentAttsName = attachmentManager.getFileNameExcludeSuffix(contentAtt);
			info.setFilesmContentAttsName(contentAttsName);
			
		}
		
		String attListJSON = attachmentManager.getAttListJSON(showAtts);
		return attListJSON;
	}
	/*************************** 33333 公文附件查看 start ***************************/
	
	
	/*************************** 44444 公文附件保存 start ***************************/
	@Override
	public void saveAttachments(GovdocNewVO info, SendType sendType) throws BusinessException {
		// 保存上传的附件
		boolean attaFlag = false;
		EdocSummary summary = info.getSummary();
		if (!EdocConstant.SendType.auto.equals(sendType) && !EdocConstant.SendType.child.equals(sendType)) {
			try {
				String attaFlagStr = "";
				ApplicationCategoryEnum appenum = ApplicationCategoryEnum.edoc;
				if (sendType == EdocConstant.SendType.forward) {
					HttpServletRequest request = (HttpServletRequest) AppContext.getThreadContext(GlobalNames.THREAD_CONTEXT_REQUEST_KEY);
					request.removeAttribute("HASSAVEDATTACHMENT");// 保存附件之前清空这个标记
					attaFlagStr = this.attachmentManager.create(appenum, summary.getId(), summary.getId());
				} else if (sendType == EdocConstant.SendType.resend || sendType == EdocConstant.SendType.normal) {
					if (info.getNibanComment() != null) {// 如果有拟办意见，则将附件也保存到拟办意见中
						attaFlagStr = saveAttachmentFromDomain(appenum, info);
						if (Strings.isNotBlank(info.getNibanComment().getRelateInfo())) {// 先将该值置为空，否则保存两次附件会出错
							info.getNibanComment().setRelateInfo("");
						}
					} else {
						attaFlagStr = saveAttachmentFromDomain(appenum, info);
					}

				}
				attaFlag = Constants.isUploadLocaleFile(attaFlagStr);
				if (attaFlag) {
					summary.setHasAttachments(attaFlag);
				}
			} catch (Exception e) {
				LOGGER.info("保存上传的附件报错!", e);
			}
		}
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String saveAttachmentFromDomain(ApplicationCategoryEnum type, GovdocNewVO info) throws BusinessException {
		List assDocGroup = ParamUtil.getJsonDomainGroup("assDocDomain");
		int assDocSize = assDocGroup.size();
		Map assDocMap = ParamUtil.getJsonDomain("assDocDomain");
		if (assDocSize == 0 && assDocMap.size() > 0) {
			assDocGroup.add(assDocMap);
		}

		List attFileGroup = ParamUtil.getJsonDomainGroup("attFileDomain");
		int attFileSize = attFileGroup.size();
		Map attFileMap = ParamUtil.getJsonDomain("attFileDomain");
		if (attFileSize == 0 && attFileMap.size() > 0) {
			attFileGroup.add(attFileMap);
		}

		assDocGroup.addAll(attFileGroup);

		List result;
		try {
			Long moduleId = info.getSummary().getId();
			result = attachmentManager.getAttachmentsFromAttachList(type, moduleId, moduleId, assDocGroup);
			if (org.apache.commons.collections.CollectionUtils.isNotEmpty(result)) {
				List<Attachment> newAtts = new ArrayList<Attachment>();
				for (Attachment att : (List<Attachment>) result) {
					Attachment newAtt = (Attachment) att.clone();
					newAtt.setId(UUIDLong.longUUID());
					newAtt.setReference(moduleId);
					newAtt.setFilename(att.getFilename());
					newAtt.setFilename(newAtt.getFilename().replace(' ', ' ').replace("\'", "\\\'"));
					newAtt.setCategory(ApplicationCategoryEnum.edoc.getKey());
					if (info.getNibanComment() != null) {
						newAtt.setSubReference(info.getNibanComment().getId());
					}
					newAtts.add(newAtt);
				}
				if (info.getNibanComment() != null) {
					info.getNibanComment().setRelateInfo(JSONUtil.toJSONString(attachmentManager.setOfficeTransformEnable(newAtts)));
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new BusinessException("创建附件出错");
		}

		return attachmentManager.create(result);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void saveAttDatas(GovdocBaseVO baseVo) throws BusinessException{
	      //保存附件和正文修改
        Map<String, String> colSummaryDomian = (Map<String, String>)ParamUtil.getJsonDomain("colSummaryData");
        if(colSummaryDomian != null){

            AttachmentEditUtil attUtil = new AttachmentEditUtil("attActionLogDomain");
            boolean modifyContent = "1".equals(colSummaryDomian.get("modifyFlag"));
            boolean modifyAtt = attUtil.hasEditAtt();

            if(modifyAtt) {
                saveAttachment(baseVo.getSummary(), baseVo.getAffair(), !modifyContent, baseVo.getComment().getId());
            }

            if(modifyContent){
                
                //修改正文发送消息
                if(!modifyAtt){
                    //colMessageManager.sendMessage4ModifyBodyOrAtt(summary, affair.getMemberId(), 1);
                }
                
                govdocLogManager.saveAttUpdateLog(baseVo);
            }

            //同时修改了正文和附件发送消息
            if(modifyAtt && modifyContent){
                //colMessageManager.sendMessage4ModifyBodyOrAtt(summary, affair.getMemberId(), 2);
            }
        }
    }
    /**
     * 修改附件的保存
     * @param summary
     * @param attaFlag
     * @param affair
     * @param toSendMsg 是否发送消息
     * @return
     * @throws BusinessException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private String saveAttachment(EdocSummary summary, CtpAffair affair, boolean toSendMsg,Long commentId) throws BusinessException {
        String attaFlag = "";
        try {
            //保存附件之前先删除原来到附件

        	List attFileGroup = ParamUtil.getJsonDomainGroup("attFileDomain");
            int attFileSize = attFileGroup.size();
            Map attFileMap = ParamUtil.getJsonDomain("attFileDomain");
            if (attFileSize == 0 && attFileMap.size() > 0) {
            	attFileGroup.add(attFileMap);
            }
            List assDocGroup = ParamUtil.getJsonDomainGroup("assDocDomain");
            int assDocSize = assDocGroup.size();
            Map assDocMap = ParamUtil.getJsonDomain("assDocDomain");
            if (assDocSize == 0 && assDocMap.size() > 0) {
            	assDocGroup.add(assDocMap);
            }
            attFileGroup.addAll(assDocGroup);
            List<Attachment> result = attachmentManager.getAttachmentsFromAttachList(ApplicationCategoryEnum.collaboration, summary.getId(), summary.getId(), attFileGroup);

            List<Attachment> oldAtts = attachmentManager.getByReference(summary.getId(), summary.getId());
            List<Attachment> needAddAtts = new ArrayList<Attachment>();
            List<Long> newAttFUIds = new ArrayList<Long>();
            Map<Long,Attachment> nm = new HashMap<Long,Attachment>();
            Map<Long,Long> oldFUID = new HashMap<Long,Long>();
            Set<Long> oldAttFUIds = new HashSet<Long>();

            LOGGER.info("===============当前附件：");
            for(Attachment newAtt :result ){
            	if(!newAttFUIds.contains(newAtt.getFileUrl())){
            		newAttFUIds.add(newAtt.getFileUrl());
            	}
                nm.put(newAtt.getFileUrl(), newAtt);
                LOGGER.info("文件名：" + newAtt.getFilename()+",ID："+newAtt.getId()+"创建时间："+newAtt.getCreatedate());
            }

            LOGGER.info("原来的附件：");
            for(Attachment oldAtt :oldAtts ){
                oldAttFUIds.add(oldAtt.getFileUrl());
                oldFUID.put(oldAtt.getFileUrl(),oldAtt.getId());
                LOGGER.info("文件名：" + oldAtt.getFilename()+",ID："+oldAtt.getId()+"创建时间："+oldAtt.getCreatedate());
            }

            for(Long id :newAttFUIds){
                if(!oldAttFUIds.contains(id)) {
                	if(!needAddAtts.contains(nm.get(id))){
                		needAddAtts.add(nm.get(id));
                	}
                	LOGGER.info("添加的附件fileUrl：" + nm.get(id));
                }
            }

            for(Long id : oldAttFUIds){
                if(!newAttFUIds.contains(id)){
                    attachmentManager.deleteById(oldFUID.get(id));
                    LOGGER.info("删除附件 ："+oldFUID.get(id));
                }
            }

            attaFlag = attachmentManager.create(needAddAtts);
            LOGGER.info("添加附件成功返回的attaFlag:" + attaFlag);

        	AttachmentEditUtil attUtil = new AttachmentEditUtil("attActionLogDomain");
        	List<ProcessLog> logs = attUtil.parseProcessLog(Long.valueOf(summary.getProcessId()), affair.getActivityId());
        	for(ProcessLog log : logs){
        		log.setCommentId(commentId);
        	}
            govdocLogManager.insertProcessLog(logs);

        	updateSummaryAttachment(result.size(),summary,affair, toSendMsg);

        	//修改附件更新全文检索库
            if(AppContext.hasPlugin("index")) {
                indexApi.update(affair.getObjectId(), ApplicationCategoryEnum.edoc.getKey());
            }
        } catch (Exception e) {
        	LOGGER.error("创建附件出错，位于方法ColManagerImpl.saveAttachment", e);
        	throw new BusinessException("创建附件出错");
        }
        return attaFlag;
    }
    private void updateSummaryAttachment(int attSize, EdocSummary summary, CtpAffair affair, boolean toSendMsg) throws BusinessException{
    	//Map<String, String> colSummaryDomian = (Map<String, String>)ParamUtil.getJsonDomain("colSummaryData");
    	//int type=0;
    	boolean needUpdate = false;
    	boolean isHasAtt=summary.isHasAttachments();
    	if(!isHasAtt && attSize > 0){
    		needUpdate=true;
    		//更新summary的附件标志位
    		//GovdocHelper.setHasAttachments(summary, true);
    	}else if(isHasAtt && attSize == 0){
    		//更新summary的附件标志位
    		needUpdate=true;
    		//ColUtil.setHasAttachments(summary, false);
    	}
    	if(needUpdate){
    		//更新该流程中所有的待办标志位
			Map<String,Object> parameter = new HashMap<String,Object>();
			parameter.put("identifier", summary.getIdentifier());
			//this.updateColSummary(summary);
			affairManager.updateAffairs(ApplicationCategoryEnum.collaboration, summary.getId(), parameter);
		}
    	if(toSendMsg){
    	    //colMessageManager.sendMessage4ModifyBodyOrAtt(summary, affair.getMemberId(), 0);
    	}
	}
    
    /**
	 * 修改附件的保存
	 *
	 * @param summary
	 * @param attaFlag
	 * @param affair
	 * @return
	 * @throws BusinessException
	 */
	public String saveAttachment(EdocSummary summary, CtpAffair affair )throws BusinessException{
		return saveAttachment(summary,affair,null);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String saveAttachment(EdocSummary summary, CtpAffair affair ,Long commentId) throws BusinessException {
		String attaFlag = "";
		try {
			// 保存附件之前先删除原来到附件

			List attFileGroup = ParamUtil.getJsonDomainGroup("attFileDomain");
			List<Object> removeList = new ArrayList<Object>();
			for(Object att :attFileGroup){
				//chenyq 一屏式正式附件里面有表单附件
				if(att instanceof Map){
				   Map map = (Map)att;
				   if("2".equals(map.get("attachment_category"))){
					   removeList.add(att);
					   //attFileGroup.remove(att);
				   }
				}
			}
			for(Object att :removeList){
				attFileGroup.remove(att);
			}
			int attFileSize = attFileGroup.size();
			Map attFileMap = ParamUtil.getJsonDomain("attFileDomain");
			if (attFileSize == 0 && attFileMap.size() > 0) {
				attFileGroup.add(attFileMap);
			}
			List assDocGroup = ParamUtil.getJsonDomainGroup("assDocDomain");
			int assDocSize = assDocGroup.size();
			Map assDocMap = ParamUtil.getJsonDomain("assDocDomain");
			if (assDocSize == 0 && assDocMap.size() > 0) {
				assDocGroup.add(assDocMap);
			}
			attFileGroup.addAll(assDocGroup);
			List<Attachment> result = attachmentManager.getAttachmentsFromAttachList(ApplicationCategoryEnum.edoc, summary.getId(), summary.getId(),
					attFileGroup);
			// Object obj = summary.getExtraAttr("opinionId");
			// if (null != obj) {
			//
			// result =
			// attachmentManager.getAttachmentsFromRequest(ApplicationCategoryEnum.edoc,
			// summary.getId(),
			// Long.parseLong(obj.toString()), AppContext.getRawRequest());
			// }
			List<Attachment> oldAtts = attachmentManager.getByReference(summary.getId(), summary.getId());
			Set<Attachment> needAddAtts = new HashSet<Attachment>();
			Set<Long> newAttFUIds = new HashSet<Long>();
			Map<Long, Attachment> nm = new HashMap<Long, Attachment>();
			Map<Long, Long> oldFUID = new HashMap<Long, Long>();
			Set<Long> oldAttFUIds = new HashSet<Long>();

			LOGGER.info("===============当前附件：");
			for (Attachment newAtt : result) {
				newAttFUIds.add(newAtt.getFileUrl());
				nm.put(newAtt.getFileUrl(), newAtt);
				LOGGER.info("文件名：" + newAtt.getFilename() + ",ID：" + newAtt.getId() + "创建时间：" + newAtt.getCreatedate());
			}

			LOGGER.info("原来的附件：");
			for (Attachment oldAtt : oldAtts) {
				oldAttFUIds.add(oldAtt.getFileUrl());
				oldFUID.put(oldAtt.getFileUrl(), oldAtt.getId());
				LOGGER.info("文件名：" + oldAtt.getFilename() + ",ID：" + oldAtt.getId() + "创建时间：" + oldAtt.getCreatedate());
			}

			for (Long id : newAttFUIds) {
				if (!oldAttFUIds.contains(id)) {
					needAddAtts.add(nm.get(id));
					LOGGER.info("添加的附件fileUrl：" + nm.get(id));
				}
			}

			for (Long id : oldAttFUIds) {
				if (!newAttFUIds.contains(id)) {
					attachmentManager.deleteById(oldFUID.get(id));
					LOGGER.info("删除附件 ：" + oldFUID.get(id));
				}
			}

			attaFlag = attachmentManager.create(needAddAtts,false);
			LOGGER.info("添加附件成功返回的attaFlag:" + attaFlag);

			AttachmentEditUtil attUtil = new AttachmentEditUtil("attActionLogDomain");

			if(commentId != null){
	        	List<ProcessLog> logs = attUtil.parseProcessLog(Long.valueOf(summary.getProcessId()), affair.getActivityId());
	        	for(ProcessLog log : logs){
	        		log.setCommentId(commentId);
	        	}
	            govdocLogManager.insertProcessLog(logs);
			}

			updateSummaryAttachment(result.size(), summary, affair);
			// 修改附件更新全文检索库
			if (AppContext.hasPlugin("index")) {
			    indexApi.update(affair.getObjectId(), ApplicationCategoryEnum.edoc.getKey());
			}
		} catch (Exception e) {
			LOGGER.error("创建附件出错，位于方法ColManagerImpl.saveAttachment", e);
			throw new BusinessException("创建附件出错");
		}
		return attaFlag;
	}
	
	private void updateSummaryAttachment(int attSize, EdocSummary summary, CtpAffair affair) throws BusinessException {
		boolean needUpdate = false;
		boolean isHasAtt = summary.isHasAttachments();
		if (!isHasAtt && attSize > 0) {
			needUpdate = true;
			// 更新summary的附件标志位
			summary.setHasAttachments(true);
			AffairUtil.setHasAttachments(affair, true);
		} else if (isHasAtt && attSize == 0) {
			// 更新summary的附件标志位
			needUpdate = true;
			summary.setHasAttachments(false);
			AffairUtil.setHasAttachments(affair, false);
		}
		if (needUpdate) {
			// 更新该流程中所有的待办标志位
			Map<String, Object> parameter = new HashMap<String, Object>();
			parameter.put("identifier", summary.getIdentifier());
			govdocSummaryManager.updateEdocSummary(summary,false);
			affairManager.updateAffairs(ApplicationCategoryEnum.edoc, summary.getId(), parameter);
		}

		// 发送消息
		// colMessageManager.saveUpdataAttMessage(affairManager,
		// userMessageManager, orgManager, summary, affair);
	}
	
	@Override
	public void saveEdocOpinionRelationAttachment(int i, long reference, long subReference, int type, ApplicationCategoryEnum cotegory, String fileName, String mimeType, long fileUrl) throws Exception {
		Attachment e = new Attachment();
		e.setIdIfNew();
		e.setSort(i);
		e.setReference(reference);
		e.setSubReference(subReference);
		e.setCategory(Integer.valueOf(cotegory.getKey()));
		e.setType(type);
		e.setFilename(fileName);
		e.setMimeType(mimeType);
		e.setFileUrl(fileUrl);
		e.setCreatedate(new Date());
		e.setSize(0L);
		e.setDescription(String.valueOf(fileUrl));
		this.attachmentDAO.save(e);
	}

	@SuppressWarnings("deprecation")
	private void createAttachmentVO(AttachmentVO vo, Attachment attachment) {
		vo.setUploadTime(attachment.getCreatedate());
		// 转换附件大小显示格式(转为K且省略小数点后位)
		Long size = attachment.getSize().longValue() / 1024 + 1;
		vo.setFileSize(size.toString());
		// 附件后缀
		String extension = attachment.getExtension();
		vo.setFileType(extension);
		// 附件是否可查看
		if (OfficeTransHelper.isOfficeTran() && OfficeTransHelper.allowTrans(attachment)) {
			vo.setCanLook(true);
		} else {
			vo.setCanLook(false);
		}
		// 附件名称去掉后缀
		String fileName = attachment.getFilename();
		if (!StringUtil.checkNull(extension)) {
			fileName = fileName.substring(0, fileName.lastIndexOf("."));
		}
		vo.setFileFullName(fileName);
		vo.setFileName(Strings.getLimitLengthString(fileName, 25, "..."));
		vo.setFileUrl(String.valueOf(attachment.getFileUrl()));
		vo.setV(String.valueOf(attachment.getV()));
	}
	
	private void createGovdocAttachmentVO(GovdocAttachmentVO vo, Attachment attachment) {
		vo.setCategory(attachment.getCategory());
		vo.setCreatedate(attachment.getCreatedate());
		vo.setDescription(attachment.getDescription());
		vo.setExtension(attachment.getExtension());
		vo.setFilename(attachment.getFilename());
		vo.setFileUrl(attachment.getFileUrl());
		vo.setGenesisId(attachment.getGenesisId());
		vo.setIcon(attachment.getIcon());
		vo.setMimeType(attachment.getMimeType());
		vo.setSize(attachment.getSize());
		vo.setType(attachment.getType());
		vo.setSort(attachment.getSort());
		vo.setReference(attachment.getReference());
		vo.setOfficeTransformEnable(attachment.getOfficeTransformEnable());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public String saveAttachmentFromDomain(ApplicationCategoryEnum type, Long module_id) throws BusinessException {
		List assDocGroup = ParamUtil.getJsonDomainGroup("assDocDomain");
		int assDocSize = assDocGroup.size();
		Map assDocMap = ParamUtil.getJsonDomain("assDocDomain");
		if (assDocSize == 0 && assDocMap.size() > 0) {
			assDocGroup.add(assDocMap);
		}

		List attFileGroup = ParamUtil.getJsonDomainGroup("attFileDomain");
		int attFileSize = attFileGroup.size();
		Map attFileMap = ParamUtil.getJsonDomain("attFileDomain");
		if (attFileSize == 0 && attFileMap.size() > 0) {
			attFileGroup.add(attFileMap);
		}

		assDocGroup.addAll(attFileGroup);

		List result;
		try {
			result = attachmentManager.getAttachmentsFromAttachList(ApplicationCategoryEnum.edoc, module_id, module_id, assDocGroup);
		} catch (Exception e) {
			LOGGER.error("", e);
			throw new BusinessException("创建附件出错");
		}

		return attachmentManager.create(result);
	}

	@Override
    public void saveAttDatas(User user, EdocSummary summary, CtpAffair affair, Long commentId) throws BusinessException{
      //保存附件和正文修改
        @SuppressWarnings("unchecked")
		Map<String, String> colSummaryDomian = (Map<String, String>)ParamUtil.getJsonDomain("colSummaryData");
        if(colSummaryDomian != null){

            AttachmentEditUtil attUtil = new AttachmentEditUtil("attActionLogDomain");
            boolean modifyContent = "1".equals(colSummaryDomian.get("modifyFlag"));
            boolean modifyAtt = attUtil.hasEditAtt();

            if(modifyAtt){
                this.saveAttachment(summary, affair,commentId);
            }

            if(modifyContent){
                //修改正文后记录流程日志
                govdocLogManager.insertProcessLog(AppContext.getCurrentUser(), Long.parseLong(summary.getProcessId()),
                        affair.getActivityId(), ProcessLogAction.processColl,commentId,String.valueOf(ProcessLogAction.ProcessEdocAction.modifyBody.getKey()));

                //修改正文发送消息
                if(!modifyAtt){
                    govdocMessageManager.sendMessage4ModifyBodyOrAtt(summary, affair.getMemberId(), 1);
                }
                //如果修改正文的时候导入了新文件则记录应用日志
                if("1".equals(colSummaryDomian.get("isLoadNewFile"))){
                    govdocLogManager.insertAppLog(user, GovdocAppLogAction.COLL_CONTENT_EDIT_LOADNEWFILE.key(), user.getName(), affair.getSubject());
                }
            }

            //同时修改了正文和附件发送消息
            if(modifyAtt && modifyContent){
            	govdocMessageManager.sendMessage4ModifyBodyOrAtt(summary, affair.getMemberId(), 2);
            }
        }
    }
	

	/*************************** 44444 公文附件保存   end ***************************/
	
    
    /*************************** 55555 公文附件删除 start ***************************/
    @Override
    public void deleteAttachment4SpecialBack(Long id) {
		try {
			this.attachmentManager.deleteByReference(id, id);
			List<Long> commentIds = govdocCommentManager.getSenderCommentIdByModuleIdAndCtype(ModuleType.edoc, id);
			if (Strings.isNotEmpty(commentIds)) {
				for (Long cid : commentIds) {
					attachmentManager.deleteByReference(id, cid);
				}
			}
		} catch (BusinessException e) {
			LOGGER.error("指定回退删除附件报错" + e.getLocalizedMessage(), e);
		}
	}
    /*************************** 55555 公文附件删除   end ***************************/

    @Override
    public boolean checkContent(GovdocNewVO newVo) throws BusinessException {
    	// 陈应强 判断流程发起时，正文是否存在 start
		if (newVo.getBodyVo().getContent() != null) {
			if (null != newVo.getBodyVo().getContentType() && 10 != newVo.getBodyVo().getContentType()) {// html正文
				// 没有文件
				V3XFile file = fileManager.getV3XFile(Long.parseLong(newVo.getBodyVo().getContent()));
				if (file != null) {
					File f = fileManager.getFile(file.getId());
					if (f == null || !f.exists()) {
						LOGGER.info("正文文件不存在，V3Xfileid为:" + file.getId());
						newVo.setErrorMsg("发起公文时正文文件丢失！请重新发起。");
						return false;
					}
					if (Strings.isNotBlank(newVo.getBodyVo().getFileName())) {
						file.setFilename(newVo.getBodyVo().getFileName());
						fileManager.save(file);
					}
				} else {
					LOGGER.info("没有获取到文件信息");
				}
			}
		}
		return true;
    }
    
        
	@Override
	public void setAttachmentJSON(GovdocNewVO newVo, List<Attachment> atts) throws BusinessException {
		String attListJSON = attachmentManager.getAttListJSON(atts);
		newVo.setAttListJSON(attListJSON);
		//循环附件，过滤出关联文档。附件说明用到
		List<Attachment> contentAtts = new ArrayList<Attachment>();
		for (Attachment attachment : atts) {
			if(attachment.getType() != 2){
				contentAtts.add(attachment);
			}
		}
		String contentFilesmAttName = attachmentManager.getFileNameExcludeSuffix(contentAtts);
		newVo.setFilesmContentAttsName(contentFilesmAttName);
	}

	/*************************** 99999 Spring注入，请将业务写在上面 start ******************************/
	public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
		this.ctpMainbodyManager = ctpMainbodyManager;
	}
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}
	public void setGovdocSignetManager(GovdocSignetManager govdocSignetManager) {
		this.govdocSignetManager = govdocSignetManager;
	}
	public void setGovdocLogManager(GovdocLogManager govdocLogManager) {
		this.govdocLogManager = govdocLogManager;
	}
	public void setIndexApi(IndexApi indexApi) {
        this.indexApi = indexApi;
    }
	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}
	public void setGovdocCommentManager(GovdocCommentManager govdocCommentManager) {
		this.govdocCommentManager = govdocCommentManager;
	}
	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}
	public void setAttachmentDAO(AttachmentDAO attachmentDAO) {
		this.attachmentDAO = attachmentDAO;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public void setDocApi(DocApi docApi) {
		this.docApi = docApi;
	}
	public void setGovdocMessageManager(GovdocMessageManager govdocMessageManager) {
		this.govdocMessageManager = govdocMessageManager;
	}
	/*************************** 99999 Spring注入，请将业务写在上面   end ******************************/	

}
