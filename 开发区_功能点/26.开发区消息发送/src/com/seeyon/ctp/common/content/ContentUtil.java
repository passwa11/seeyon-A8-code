/**
 * $Author: muj $
 * $Rev: 6317 $
 * $Date:: 2013-04-22 14:52:32#$:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.common.content;

import com.seeyon.apps.collaboration.bo.BackgroundDealParamBO;
import com.seeyon.apps.collaboration.constants.ColConstant;
import com.seeyon.apps.collaboration.listener.WorkFlowEventListener;
import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.content.mainbody.CtpContentAllBean;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyStatus;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.event.WorkflowEventData;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.wapi.RunCaseResult;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Title: T1开发框架</p>
 * <p>Description: 内容组件工具类，对内容组件相关处理逻辑封装</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: seeyon.com</p>
 * @since CTP2.0
 */
public class ContentUtil {
    private static final Log log = LogFactory.getLog(ContentUtil.class);
    private static final MainbodyManager    contentManager    = (MainbodyManager) AppContext
                                                                      .getBean("ctpMainbodyManager");
    private static final CommentManager     commentManager    = (CommentManager) AppContext
                                                                      .getBean("ctpCommentManager");
    private static final AffairManager      affairManager     = (AffairManager) AppContext.getBean("affairManager");
    private static final WorkflowApiManager wapi              = (WorkflowApiManager) AppContext.getBean("wapi");
    private static final TemplateManager    templateManager   = (TemplateManager) AppContext.getBean("templateManager");
    private static final PermissionManager  permissionManager = (PermissionManager) AppContext
                                                                      .getBean("permissionManager");
    private static final OrgManager         orgManager        = (OrgManager) AppContext.getBean("orgManager");
    
    public enum OperationType {
        send, //发送
        save, //存为草稿，保存待发
        wait, //暂存待办
        finish, //正常提交
        template, //模板
        noworkflow,
        //不存流程
        personalTemplate,
        pTemplateText,
        stepBack,//回退
        stepStop,//终止
        appointStepBack //指定回退
    }

    /**
     * 根据HttpServletRequest请求中的参数处理内容新建，获取参数包括：
     * -moduleType，模块类型，默认值ModuleType.collaboration.getKey()
     * -moduleId，模块ID，默认值-1（新建），否则为模块多内容新建
     * -contentType，新建内容类型，默认值ContentType.html.getKey()
     * -contentTemplateId，新建内容模板ID，默认值-1，例如表单模板ID
     * -rightId，新建内容权限ID，默认值-1，例如表单模板权限ID
     * 
     * @return 内容新建Content对象
     * @throws BusinessException 内容新建相关异常
     */
    public static CtpContentAllBean contentNewByRequest() throws BusinessException {
        HttpServletRequest request = (HttpServletRequest) AppContext
                .getThreadContext(GlobalNames.THREAD_CONTEXT_REQUEST_KEY);
        Map params = request.getParameterMap();
        int moduleType = ParamUtil.getInt(params, "moduleType", ModuleType.collaboration.getKey());
        long moduleId = ParamUtil.getLong(params, "moduleId", -1l);
        int contentType = ParamUtil.getInt(params, "contentType", MainbodyType.HTML.getKey());
        return contentNew(ModuleType.getEnumByKey(moduleType), moduleId, MainbodyType.getEnumByKey(contentType));
    }

    public static CtpContentAllBean contentNew(ModuleType moduleType, Long moduleId) throws BusinessException {
        return contentNew(moduleType, moduleId, null);
    }

    /**
     * 内容新建处理，返回新建内容对象
     * 
     * @param moduleType 模块类型，默认值ModuleType.collaboration.getKey()
     * @param moduleId 模块ID，默认值-1（新建），否则为模块多内容新建
     * @param contentType 新建内容类型，默认值ContentType.html.getKey()
     * @param contentTemplateId 新建内容模板ID，默认值-1，例如表单模板ID
     * @param rightId 新建内容权限ID，默认值-1，例如表单模板权限ID
     * @return 内容新建Content对象
     * @throws BusinessException 内容新建相关异常
     */
    public static CtpContentAllBean contentNew(ModuleType moduleType, Long moduleId, MainbodyType contentType)
            throws BusinessException {
        if (moduleType == null)
            moduleType = ModuleType.collaboration;
        if (moduleId == null)
            moduleId = -1l;
        if (contentType == null)
            contentType = MainbodyType.HTML;

        HttpServletRequest request = (HttpServletRequest) AppContext
                .getThreadContext(GlobalNames.THREAD_CONTEXT_REQUEST_KEY);
        Map params = request.getParameterMap();
        CtpContentAllBean content = contentManager.transContentNewResponse(moduleType, moduleId, contentType, "",new HashMap<String, Object>(1));
        List<CtpContentAllBean> contentList = new ArrayList<CtpContentAllBean>();
        contentList.add(content);
        request.setAttribute("contentList", contentList);
        request.setAttribute("contentCfg", ContentConfig.getConfig(moduleType));

        ContentViewRet context = new ContentViewRet();
        context.setModuleId(moduleId);
        context.setModuleType(moduleType.getKey());
        context.setCommentMaxPath("00");
        request.setAttribute("contentContext", context);
        return content;
    }

    /**
     * 根据HttpServletRequest请求中的参数处理内容查看，获取参数包括：
     * -moduleType，模块类型，默认值ModuleType.collaboration.getKey()
     * -moduleId，模块ID，默认值-1（新建），否则为模块多内容新建
     * -rightId，新建内容权限ID，默认值-1，例如表单模板权限ID
     * 
     * @return 内容查看Content对象列表（支持多正文）
     * @throws BusinessException 内容查看相关异常
     */
   /* public static ContentViewRet contentViewByRequest() throws BusinessException {
        HttpServletRequest request = (HttpServletRequest) AppContext
                .getThreadContext(GlobalNames.THREAD_CONTEXT_REQUEST_KEY);
        Map params = request.getParameterMap();
        int moduleType = ParamUtil.getInt(params, "moduleType", ModuleType.collaboration.getKey());
        long moduleId = ParamUtil.getLong(params, "moduleId", -1l);
        long affairId = ParamUtil.getLong(params, "affairId", -1l);
        String rightId = ParamUtil.getString(params, "rightId", "-1");
        int viewState = ParamUtil.getInt(params, "viewState", CtpContentAllBean.viewState_readOnly);
        return contentView(ModuleType.getEnumByKey(moduleType), moduleId, affairId, viewState, rightId);
    }*/

    /**
     * 内容查看相关处理，返回内容查看列表（支持多正文）
     * 
     * @param moduleType 模块类型，默认值ModuleType.collaboration.getKey()
     * @param moduleId 模块ID，默认值-1（新建），否则为模块多内容新建
     * @param rightId 新建内容权限ID，默认值-1，例如表单模板权限ID
     * @return 内容查看Content对象列表（支持多正文）
     * @throws BusinessException 内容查看相关异常
     */
    public static ContentViewRet contentView(ModuleType moduleType, Long moduleId, Long affairId, int viewState,
            String rightId) throws BusinessException {
        if (moduleType == null)
            moduleType = ModuleType.collaboration;
        if (moduleId == null)
            moduleId = -1l;
        HttpServletRequest request = (HttpServletRequest) AppContext
                .getThreadContext(GlobalNames.THREAD_CONTEXT_REQUEST_KEY);
        List<CtpContentAllBean> contentList = contentManager.transContentViewResponse(moduleType, moduleId, viewState,
                rightId);
        if(contentList.size() == 0){//流程模板查看增加个空正文返回
        		CtpContentAllBean content_null = new CtpContentAllBean();
        		content_null.setViewState(viewState);
        		content_null.setStatus(MainbodyStatus.STATUS_RESPONSE_VIEW);
        		content_null.setRightId(rightId);
        		content_null.setContentType(10);
                contentList.add(content_null);
        }
        
        request.setAttribute("contentList", contentList);
		
        ContentViewRet context = new ContentViewRet();
        context.setModuleId(moduleId);
        context.setModuleType(moduleType.getKey());
        context.setAffairId(affairId);
        ContentConfig contentCfg = ContentConfig.getConfig(moduleType);

        if (contentCfg.getCommentTemplate() != null) {
            Map<String,Object> commentsMap = commentManager.getCommentsWithForward(moduleType,
                    moduleId);
            Comment commentDraft = null;

            List<Comment> draftList = (List<Comment>) commentsMap.get("commentDraftList");
            if(Strings.isNotEmpty(draftList)){
                for (Comment tmpDraft : draftList) {
                    //获取当前用户评论草稿
                    if (AppContext.currentUserId() == tmpDraft.getCreateId()
                            && (affairId == null || (affairId != null && affairId.equals(tmpDraft.getAffairId())))) {
                        commentDraft = tmpDraft;
                        commentDraft.setId(UUIDLong.longUUID());
                        break;
                    }
                }
            }

            request.setAttribute("commentDraft", commentDraft);
            
            List<StateEnum> states = new ArrayList<StateEnum>();
            states.add(StateEnum.col_sent);
            states.add(StateEnum.col_done);
            states.add(StateEnum.col_pending);
            states.add(StateEnum.col_waitSend);
            
            List<CtpAffair> pushMessageList = affairManager.getAffairs(moduleId, states);
            //排序顺序规则，发起人、已办、暂存待办
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
            //过滤掉自己和重复项
            List<Long> memberIdList = new ArrayList<Long>();
            //消息推送
            List<CtpAffair> pushMessageListAffair = new ArrayList<CtpAffair>();
            for (CtpAffair r : pushMessageList) {
                //只显示已发、暂存待办和、已办的、回退者
                if ((r.getSubState() == SubStateEnum.col_pending_ZCDB.key() && r.getState() == StateEnum.col_pending
                        .key()) || r.getState() == StateEnum.col_done.key() || r.getState() == StateEnum.col_sent.key()
                        || r.getSubState() == SubStateEnum.col_pending_specialBack.key()
                        || r.getSubState() == SubStateEnum.col_pending_specialBacked.key()
                        || r.getSubState() == SubStateEnum.col_pending_specialBackCenter.key()) {
                    if (!r.getMemberId().equals(AppContext.currentUserId()) && !memberIdList.contains(r.getMemberId())) {
                        memberIdList.add(r.getMemberId());
                        pushMessageListAffair.add(r);
                    }
                }
            }
            request.setAttribute("commentPushMessageToMembersList", pushMessageListAffair);

            context.setCommentMaxPath((String) AppContext.getRequestContext("commentMaxPathStr"));
        }
        request.setAttribute("__huanhang", "\r\n");
        request.setAttribute("contentContext", context);
        request.setAttribute("contentCfg", contentCfg);
        return context;
    }

    public static void commentView(HttpServletRequest request,ContentConfig contentCfg,ModuleType moduleType, Long moduleId,Long affairId) throws BusinessException {
       if(null == contentCfg){
         return;
       }
    if (contentCfg.getCommentTemplate() != null) {
        
    	Map<String,Object> commentsMap = commentManager.getCommentsWithForward(moduleType, moduleId);
        Comment commentDraft = null;

        List<Comment> draftList = (List<Comment>) commentsMap.get("commentDraftList");
        
        if(Strings.isNotEmpty(draftList)){
            for (Comment tmpDraft : draftList) {
                //获取当前用户评论草稿
                if (AppContext.currentUserId() == tmpDraft.getCreateId()
                        && (affairId == null ||  affairId.equals(tmpDraft.getAffairId()))) {
                    commentDraft = tmpDraft;
                    commentDraft.setId(UUIDLong.longUUID());
                    break;
                }
            }
        }

        request.setAttribute("commentDraft", commentDraft);
        
      }
      request.setAttribute("__huanhang", "\r\n");
    }
    
    public static void findSenderCommentLists(HttpServletRequest request,ContentConfig contentCfg,ModuleType moduleType, Long moduleId,Long affairId) throws BusinessException {
        if(null == contentCfg){
          return;
        }
        List<Comment> commentSenderList = commentManager.getCommentAllByCTYPE(moduleType, moduleId, Comment.CommentType.sender);
        request.setAttribute("commentSenderList", commentSenderList);
        request.setAttribute("__huanhang", "\r\n");
     }
    
    /*
    *//**
     * 内容查看相关处理，返回内容查看列表（支持多正文）
     * 
     * @param moduleType 模块类型，默认值ModuleType.collaboration.getKey()
     * @param moduleId 模块ID，默认值-1（新建），否则为模块多内容新建
     * @param rightId 新建内容权限ID，默认值-1，例如表单模板权限ID
     * @return 内容查看Content对象列表（支持多正文）
     * @throws BusinessException 内容查看相关异常
     *//*
    public static ContentViewRet contentViewForDetail(ModuleType moduleType, Long moduleId, Long affairId, int viewState,
            String rightId,boolean isHistoryFlag) throws BusinessException {
        if (moduleType == null)
            moduleType = ModuleType.collaboration;
        if (moduleId == null)
            moduleId = -1l;
        HttpServletRequest request = (HttpServletRequest) AppContext.getThreadContext(GlobalNames.THREAD_CONTEXT_REQUEST_KEY);
        
        ContentViewRet context = new ContentViewRet();
        context.setModuleId(moduleId);
        context.setModuleType(moduleType.getKey());
        context.setAffairId(affairId);
        ContentConfig contentCfg = ContentConfig.getConfig(moduleType);

        if (contentCfg.getCommentTemplate() != null) {
            Map<Integer, Map<Integer, List<Comment>>> commentsMap = commentManager.getCommentsWithForward(moduleType,moduleId,isHistoryFlag);
            Comment commentDraft = null;

            List<Comment> draftList = (List<Comment>) request.getAttribute("commentDraftList");
            if(Strings.isNotEmpty(draftList)){
                for (Comment tmpDraft : draftList) {
                    //获取当前用户评论草稿
                    if (AppContext.currentUserId() == tmpDraft.getCreateId()
                            && (affairId == null || (affairId != null && affairId.equals(tmpDraft.getAffairId())))) {
                        commentDraft = tmpDraft;
                        commentDraft.setId(UUIDLong.longUUID());
                        break;
                    }
                }
            }

            request.setAttribute("commentDraft", commentDraft);
            
            
            
            List<StateEnum> states = new ArrayList<StateEnum>();
            states.add(StateEnum.col_sent);
            states.add(StateEnum.col_done);
            states.add(StateEnum.col_pending);
            states.add(StateEnum.col_waitSend);
        
            List<CtpAffair> pushMessageList = affairManager.getAffairs(moduleId, states);
            //排序顺序规则，发起人、已办、暂存待办
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
            //过滤掉自己和重复项
            Map<Long,Boolean> memberIdMap = new HashMap<Long, Boolean>(pushMessageList.size());
            //消息推送
            List<CtpAffair> pushMessageListAffair = new ArrayList<CtpAffair>();
            Long currentUserId = AppContext.currentUserId();
            for (CtpAffair r : pushMessageList) {
                //只显示已发、暂存待办和、已办的、回退者
                int subState = r.getSubState();
                int state = r.getState();
                if ((subState == SubStateEnum.col_pending_ZCDB.key() && state == StateEnum.col_pending
                        .key()) || state == StateEnum.col_done.key() || state == StateEnum.col_sent.key()
                        || subState == SubStateEnum.col_pending_specialBack.key()
                        || subState == SubStateEnum.col_pending_specialBacked.key()
                        || subState == SubStateEnum.col_pending_specialBackCenter.key()
                        || state == StateEnum.col_pending.key()) {
                    Long memberId = r.getMemberId();
                    if (!memberId.equals(currentUserId) && memberIdMap.get(memberId) == null && !memberIdList.contains(r.getMemberId())) {
                        memberIdMap.put(memberId, Boolean.TRUE);
                        pushMessageListAffair.add(r);
                    }
                }
            }
            request.setAttribute("commentPushMessageToMembersList", pushMessageListAffair);

            context.setCommentMaxPath((String) AppContext.getRequestContext("commentMaxPathStr"));
        }
        request.setAttribute("__huanhang", "\r\n");
        request.setAttribute("contentContext", context);
        request.setAttribute("contentCfg", contentCfg);
        return context;
    }
    */
    
    
    /**
     * 存为草稿，保存待发的时候使用
     * @return
     * @throws BusinessException
     */
    public static ContentSaveOrUpdateRet contentSave() throws BusinessException {
        return contentSaveOrUpdate(OperationType.save, new AffairData(), null, false);
    }

    /**
     * 只有信息报送调用，内容新增保存或更新保存
     * @param readyObjectJson 
     * @param processXml 
     * 
     * @return 新增或更新的内容对象
     * @throws BusinessException 内容新增或修改相关异常
     */
    public static ContentSaveOrUpdateRet contentSaveOrUpdate4Info(OperationType optType, AffairData affairData,
            Object wfContextBizObject, boolean resendFlag) throws BusinessException {

        ContentSaveOrUpdateRet result = new ContentSaveOrUpdateRet();
        boolean checkformRule = true;
        if(ContentUtil.OperationType.save.equals(optType) || ContentUtil.OperationType.template.equals(optType)){
            checkformRule = false;
        }
        CtpContentAllBean content = saveContentFromRequest(resendFlag,checkformRule);

        ContentConfig contentConfig = ContentConfig.getConfig(ModuleType.getEnumByKey(1));
        Comment comment = null;
        if(!ContentUtil.OperationType.noworkflow.equals(optType)){
            if (contentConfig.getCommentTemplate() != null) {
                comment = saveCommentFromRequest(optType, affairData.getMemberId(), content.getModuleId());
                result.setComment(comment);
            }
        }
        
        //工作流组件调用
        //新建的时候只能在此时设置contentType
        if(Strings.isBlank(affairData.getContentType())) affairData.setContentType(String.valueOf(content.getContentType()));
        affairData.setFormAppId(content.getContentTemplateId());//表单ID
        affairData.setFormRecordId(content.getContentDataId());//form主数据ID
        WFInfo  wfinfo =  transWorkflowHandle(optType, affairData, wfContextBizObject,comment);
        result.setCaseId(wfinfo.getCaseId());
        result.setProcessId(wfinfo.getProcessId());
        result.setNextNodeNames(wfinfo.getNextNodeNames());
       
        result.setContent(content);
        return result;
    }
    
    
    /**
     * 内容新增保存或更新保存
     * @param readyObjectJson 
     * @param processXml 
     * 
     * @return 新增或更新的内容对象
     * @throws BusinessException 内容新增或修改相关异常
     */
    public static ContentSaveOrUpdateRet contentSaveOrUpdate(OperationType optType, AffairData affairData,
            Object wfContextBizObject, boolean resendFlag) throws BusinessException {

        ContentSaveOrUpdateRet result = new ContentSaveOrUpdateRet();
        boolean checkformRule = true;
        if(ContentUtil.OperationType.save.equals(optType) || ContentUtil.OperationType.template.equals(optType)
            || ContentUtil.OperationType.personalTemplate.equals(optType)){
        	checkformRule = false;
        }
        CtpContentAllBean content = new CtpContentAllBean();
        if(ContentUtil.OperationType.template.equals(optType) || ContentUtil.OperationType.noworkflow.equals(optType)){
          content = saveContentFromRequest(resendFlag,checkformRule);
        }

        ContentConfig contentConfig = ContentConfig.getConfig(ModuleType.getEnumByKey(1));
        Comment comment = null;
        if(!ContentUtil.OperationType.noworkflow.equals(optType)){
        	if (contentConfig.getCommentTemplate() != null) {
        		comment = saveCommentFromRequest(optType, affairData.getMemberId(), content.getModuleId());
        		result.setComment(comment);
        	}
        }
        
        //工作流组件调用
        //新建的时候只能在此时设置contentType
        if(Strings.isBlank(affairData.getContentType()) && ContentUtil.OperationType.template.equals(optType) ) {
          affairData.setContentType(String.valueOf(content.getContentType()));
        }
        if(ContentUtil.OperationType.template.equals(optType) || ContentUtil.OperationType.noworkflow.equals(optType)){
          affairData.setFormAppId(content.getContentTemplateId());//表单ID
          affairData.setFormRecordId(content.getContentDataId());//form主数据ID
          result.setContent(content);
        }
        //模板(个人和系统)保存待发都是在这里存流程
        //if(ContentUtil.OperationType.personalTemplate.equals(optType)){
           //optType = ContentUtil.OperationType.template;
        //}
        if(ContentUtil.OperationType.pTemplateText.equals(optType)){
          optType = ContentUtil.OperationType.noworkflow;
       }
        WFInfo  wfinfo =  transWorkflowHandle(optType, affairData, wfContextBizObject,comment);
        result.setCaseId(wfinfo.getCaseId());
        result.setProcessId(wfinfo.getProcessId());
        result.setNextNodeNames(wfinfo.getNextNodeNames());
       
        //result.setContent(content);
        return result;
    }

    /**
     * @param resendFlag
     * @return
     * @throws BusinessException
     */
    public static CtpContentAllBean saveContentFromRequest(boolean resendFlag,boolean checkformRule) throws BusinessException {
        Map<String, String> div = ParamUtil.getJsonDomain("_currentDiv");
        Map params = ParamUtil.getJsonDomain("colMainData");
        String curDiv = div.get("_currentDiv");
        
        CtpContentAllBean content = new CtpContentAllBean();
        String viewState = String.valueOf(params.get("contentViewState"));
        String summaryId = String.valueOf(params.get("id"));
        String parentSummaryId = String.valueOf(params.get("parentSummaryId"));
        //如果正文是只读的则只需要取原正文
        if (!"null".equals(viewState) && !"null".equals(summaryId) 
                && Strings.isNotBlank(viewState) && Strings.isNotBlank(summaryId)
                && Integer.parseInt(viewState) == CtpContentAllBean.viewState_readOnly ) {
            List<CtpContentAll> contentList = contentManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, Long.valueOf(summaryId));
            if(Strings.isNotEmpty(contentList)){
                content = new CtpContentAllBean(contentList.get(0));
                if(resendFlag){
	                content.setId(null);
	                content.setModuleId(Long.valueOf(summaryId));
	                contentManager.transContentSaveOrUpdate(content);
                }
            } else if(Strings.isNotBlank(parentSummaryId)){
            	  List<CtpContentAll> clist = contentManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, Long.valueOf(parentSummaryId));
            	  if(Strings.isNotEmpty(clist)){
                      content = new CtpContentAllBean(clist.get(0));
                      if(resendFlag){
	                      content.setId(null);
	                      content.setModuleId(Long.valueOf(summaryId));
	                      contentManager.transContentSaveOrUpdate(content);
                      }
                  } 
            }
            
        } else {
            String processId = null;
            Long caseId = -1l;
            String useForSaveTemplate =(String)ParamUtil.getJsonDomain("colMainData").get("useForSaveTemplate");
            
            ParamUtil.getJsonDomainToBean("mainbodyDataDiv_" + curDiv, content);
            
            if(!checkformRule){//保存待发的时候不需要验证规则
            	content.putExtraMap("needCheckRule","false");
            }
            String tId = (String) ParamUtil.getJsonDomain("colMainData").get("tId");
            String useforStr = (String) ParamUtil.getJsonDomain("templateMainData").get("id");
            //String useForSaveTemplate =(String)ParamUtil.getJsonDomain("colMainData").get("useForSaveTemplate");
            /**
             * 存模板的时候   useForSaveTemplate ="yes"
             * 发送 保存待发的时候均走下面
             * 模板的时候必须走上面  tId 为 空 设置正文类型为-1
             */
            if("yes".equals(useForSaveTemplate) || Strings.isNotBlank(useforStr) ){
                content.putExtraMap("moduleTemplateId", Long.valueOf("-1"));//前台掉模板存个人模板的时候
            }else if(Strings.isBlank(tId)){//不存在模板的时候  正文为没有调用模板产生的业务功能数据  设置为0
        		content.putExtraMap("moduleTemplateId", Long.valueOf("0"));
        	}else{
        		content.putExtraMap("moduleTemplateId",Long.valueOf(tId));
        	}
            
            //如果是重复发起，这里讲content的ID和moduleId 全部重置为空 
            if (resendFlag == true) {
                content.setId(null);
            }
            /**
             * add by libing  at 2012-11-12 des:后台修改模板的时候，这里如果templateId不为空,将templateId设置为content的moduleId，
             * 避免新正文ID修改模板ID导致 更新的时候出错。
             */
    
            if (Strings.isNotBlank((String) ParamUtil.getJsonDomain("templateMainData").get("templateId"))) {
                content.setModuleId(Long.parseLong((String) ParamUtil.getJsonDomain("templateMainData").get("templateId")));
            }
            if (content.isEditable()) {
                //正文可编辑时进行正文内容保存
                contentManager.transContentSaveOrUpdate(content);
            }
        }
        return content;
    }

    /**
     * @param optType
     * @param affairData
     * @param user
     * @param content
     * @param result
     * @return
     * @throws BusinessException
     */
    public static Comment saveCommentFromRequest(OperationType optType,Long affairMemberId,Long moduleId) throws BusinessException {
        if(ContentUtil.OperationType.personalTemplate.equals(optType)){
            Long perTemMId = (Long)AppContext.getThreadContext("_perTemModuleId");
            moduleId = perTemMId;
        }
        Comment comment = getCommnetFromRequest(optType,affairMemberId,moduleId);
        Long moduleId2 = comment.getModuleId();
        if(moduleId2!=null){
        	deleteCommentAllByModuleIdAndCtype(ModuleType.collaboration,moduleId2);
        }
        if((optType == OperationType.save 
        		||optType == OperationType.pTemplateText
        		||optType == OperationType.personalTemplate
        		||optType == OperationType.template)
        		
        		&& Strings.isBlank(comment.getContent())){
            //保存待发，如果内容为空就不保存了。
        }else{
        	comment.setId(UUIDLong.longUUID());
        	
        	if(comment.getModuleId()!= null && comment.getModuleType()!=null){
        		commentManager.insertComment(comment);
        	}
        }
        
        return comment;
    }
    
    
    public static void deleteCommentAllByModuleIdAndCtype(ModuleType moduleType, Long moduleId) throws BusinessException {
        commentManager.deleteCommentAllByModuleIdAndCtype(moduleType, moduleId);
    }
    public static List<Long> getSenderCommentIdByModuleIdAndCtype(ModuleType moduleType, Long moduleId) throws BusinessException {
        return commentManager.getSenderCommentIdByModuleIdAndCtype(moduleType, moduleId);
    }
    
    
    
    public static Comment getCommnetFromRequest(OperationType optType,Long affairMemberId,Long moduleId){
        Comment comment = null;
        User user = AppContext.getCurrentUser();
        Map para = ParamUtil.getJsonDomain("comment_deal");
        comment = (Comment) ParamUtil.mapToBean(para, new Comment(), false);
        if(para.get("content_coll")!= null){
        	//发起人意见
			String content_coll=(String)para.get("content_coll");
			//如果附言内容是 "附言(500字以内)" 说明是没有设置附言，则需要把附言置空
			if(ResourceUtil.getString("collaboration.newcoll.fywbzyl").equals(content_coll.replace("\n", ""))){
				content_coll="";
			}
        	comment.setContent(content_coll);
        }
        if("1".equals((String)para.get("praiseInput"))){
          comment.setPraiseToSummary(true);
        }else{
          comment.setPraiseToSummary(false);
        }
        //判断是否代理人
        Long userId = user.getId();
        if(!userId.equals(affairMemberId)){
            comment.setExtAtt2(user.getName());
        }
        if (comment.getModuleId() == null || comment.getModuleId() == 0 || comment.getModuleId() == -1)
            comment.setModuleId(moduleId);
        if (optType == OperationType.wait) {
        	comment.setExtAtt1("");
        	comment.setExtAtt4("");
            comment.setExtAtt3("collaboration.dealAttitude.temporaryAbeyance");
        }
        return comment;
    }
    /**
     * @param optType
     * @param affairData
     * @param wfContextBizObject
     * @param content
     * @param result
     * @param processId
     * @param caseId
     * @param comment
     * @throws BusinessException
     */
  
    public static WFInfo transWorkflowHandle(OperationType optType, AffairData affairData, Object wfContextBizObject,Comment comment) throws BusinessException {
            String processId = null;
            Long caseId = null;
            String[] nextNodeNames = null;
            if (affairData == null)
                affairData = new AffairData();
          
            if (optType == OperationType.save) { //存为草稿，保存待发
                //TODO 保存暂存待办的流程图
                processId = workflowDraf();
                caseId = -1l;
                
            } else if (optType == OperationType.send) { //发送协同
            	RunCaseResult runCaseResult = workflowNew(affairData, wfContextBizObject);
                caseId = Long.valueOf(runCaseResult.getCaseId());
                processId = runCaseResult.getProcessId();
                //设置下一个节点的节点名和节点权限
                nextNodeNames = runCaseResult.getNextMembers().split("[,]");
            }/* else if (optType == OperationType.finish) { //提交协同
                //TODO 工作项ID
                Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
                String subOjectId = wfdef.get("subObjectId");
                Long itemId = Long.valueOf(subOjectId);
                if(affairData!=null){
                    String[] caseProcessIds = workflowFinish(comment, affairData, itemId, null, null,null,null);
                    //设置下一个节点的节点名和节点权限
                    nextNodeNames = caseProcessIds[0].split("[,]");
                }
            } else if (optType == OperationType.wait) {//暂存待办
                Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
                String subOjectId = wfdef.get("subObjectId");
                Long itemId = Long.valueOf(subOjectId);
                workflowWait(affairData, itemId,(ColSummary)wfContextBizObject);
            } */else if (optType == OperationType.template) {//保存模版
                processId = workflowTemplate((String) wfContextBizObject).toString();
            }else if (optType == OperationType.personalTemplate) {//保存个人模版
                processId = workflowPerTemplate((String) wfContextBizObject).toString();
            }
            if (optType == OperationType.noworkflow) {
                caseId = null;
                processId = null;
            }  
            
            WFInfo wfInfo = new WFInfo();
            wfInfo.setCaseId(caseId);
            wfInfo.setProcessId(processId);
            wfInfo.setNextNodeNames(nextNodeNames);
            return wfInfo;
    }
    /**
     * 更新节点权限引用状态
     * @param modulType 应用类型
     * @param processXml 工作流流程ID
     * @param processId 工作流流程ID
     * @param processTemplateId 工作流流程模版ID
     * @throws BusinessException
     */
    public static void updatePermissinRef(Integer modulType, String processXml, String processId,String processTemplateId,Long accountId) throws BusinessException {
        //更新节点权限引用状态
        ModuleType type = ModuleType.getEnumByKey(modulType);
        String configCategory = "";
        if (ModuleType.collaboration.name().equals(type.name()) || ModuleType.form.name().equals(type.name())) {
            configCategory = EnumNameEnum.col_flow_perm_policy.name();
        } else if (ModuleType.edocSend.name().equals(type.name())) {
            configCategory = EnumNameEnum.edoc_send_permission_policy.name();
        } else if (ModuleType.edocRec.name().equals(type.name())) {
            configCategory = EnumNameEnum.edoc_rec_permission_policy.name();
        } else if (ModuleType.edocSign.name().equals(type.name())) {
            configCategory = EnumNameEnum.edoc_qianbao_permission_policy.name();
		} else if (ModuleType.info.name().equals(type.name())) {
	     	configCategory = EnumNameEnum.info_send_permission_policy.name();
        }
        List<String> list = wapi.getWorkflowUsedPolicyIds(type.name(), processXml, processId, processTemplateId);
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                permissionManager.updatePermissionRef(configCategory, list.get(i), accountId);
            }
        }
    }
    
    public static void updatePermissinRef(Integer modulType, String[] policyLists ,Long accountId) throws BusinessException {
        //更新节点权限引用状态
        ModuleType type = ModuleType.getEnumByKey(modulType);
        String configCategory = "";
        if (ModuleType.collaboration.name().equals(type.name()) || ModuleType.form.name().equals(type.name())) {
            configCategory = EnumNameEnum.col_flow_perm_policy.name();
        } else if (ModuleType.edocSend.name().equals(type.name())) {
            configCategory = EnumNameEnum.edoc_send_permission_policy.name();
        } else if (ModuleType.edocRec.name().equals(type.name())) {
            configCategory = EnumNameEnum.edoc_rec_permission_policy.name();
        } else if (ModuleType.edocSign.name().equals(type.name())) {
            configCategory = EnumNameEnum.edoc_qianbao_permission_policy.name();
		} else if (ModuleType.info.name().equals(type.name())) {
	     	configCategory = EnumNameEnum.info_send_permission_policy.name();
        }
      //  List<String> list = wapi.getWorkflowUsedPolicyIds(type.name(), processXml, processId, processTemplateId);
        if (policyLists != null && policyLists.length> 0) {
            for (int i = 0; i < policyLists.length; i++) {
                permissionManager.updatePermissionRef(configCategory, policyLists[i], accountId);
            }
        }
    }
    /**
     * 更新文单中节点权限引用状态
     * @param modulType 应用类型
     * @param flowperm_name 权限名
     * @throws BusinessException
     */
    public static void updatePermissinRef(Integer modulType, String flowperm_name, Long accountId) throws BusinessException {
        //更新节点权限引用状态
        ModuleType type = ModuleType.getEnumByKey(modulType);
        String configCategory = "";
        if (ModuleType.collaboration.name().equals(type.name()) || ModuleType.form.name().equals(type.name())) {
            configCategory = EnumNameEnum.col_flow_perm_policy.name();
        } else if (ModuleType.edocSend.name().equals(type.name())) {
            configCategory = EnumNameEnum.edoc_send_permission_policy.name();
        } else if (ModuleType.edocRec.name().equals(type.name())) {
            configCategory = EnumNameEnum.edoc_rec_permission_policy.name();
        } else if (ModuleType.edocSign.name().equals(type.name())) {
            configCategory = EnumNameEnum.edoc_qianbao_permission_policy.name();
		} else if (ModuleType.info.name().equals(type.name())) {
	     	configCategory = EnumNameEnum.info_send_permission_policy.name();
        }
        String[] boundList = flowperm_name.split(",");
        if (boundList != null && boundList.length > 0) {
            for (int i = 0; i < boundList.length; i++) {
                permissionManager.updatePermissionRef(configCategory, boundList[i], accountId);
            }
        }
    }
    /**
     * 旧的公文类型edocType转换成ModuleType中的对应的元素
     * @param edocType
     * @return
     * @throws BusinessException
     */
    public static Integer edocTypeToModuleType(Integer edocType) throws BusinessException{
    	Integer moduleType = Integer.valueOf(0);
    	if(null!=edocType){
    		if(edocType.equals(Integer.valueOf(0))){
    			moduleType = ModuleType.getEnumByName("edocSend").getKey();
    		}else if(edocType.equals(Integer.valueOf(1))){
    			moduleType = ModuleType.getEnumByName("edocRec").getKey();
    		}else if(edocType.equals(Integer.valueOf(2))){
    			moduleType = ModuleType.getEnumByName("edocSign").getKey();
    		}
    	}
		return moduleType;
    }
    public static Long workflowTemplate(String templateName) throws BusinessException {
        Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
        String processId = wfdef.get("processId");
        Long pid = StringUtils.isNotBlank(processId) ? Long.valueOf(processId) : 0;
        String processXml = wfdef.get("process_xml");
        String moduleType = wfdef.get("moduleType");
        String process_rulecontent = wfdef.get("process_rulecontent");
        String processEventJson = wfdef.get("process_event");
        
        //H5参数处理：从流程临时表中取出流程变更之后的processXml
        processXml = wapi.getTempProcessXml(processXml);
        
        if (Strings.isBlank(processXml)) {
            return pid;
        }
        Long id = wapi.saveWorkflowTemplate(moduleType, templateName, pid, processXml, process_rulecontent,
                String.valueOf(AppContext.getCurrentUser().getId()), processEventJson);
        updatePermissinRef(Integer.valueOf(moduleType), processXml, processId, "-1",AppContext.currentAccountId());
        return id;
    }
    
    public static Long workflowPerTemplate(String templateName) throws BusinessException {
      Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
      String processId = wfdef.get("processId");
      long pid =0l;
      String processXml = wfdef.get("process_xml");
      String moduleType = wfdef.get("moduleType");
      String process_rulecontent = wfdef.get("process_rulecontent");
      String processEventJson = wfdef.get("process_event");
      
      //H5参数处理：从流程临时表中取出流程变更之后的processXml
      processXml = wapi.getTempProcessXml(processXml);
      
      if (Strings.isBlank(processXml)) {
          return pid;
      }
      Long id = wapi.saveWorkflowTemplate(moduleType, templateName, pid, processXml, process_rulecontent,
              String.valueOf(AppContext.getCurrentUser().getId()), processEventJson);
      updatePermissinRef(Integer.valueOf(moduleType), processXml, processId, "-1",AppContext.currentAccountId());
      return id;
  }

    

    /**
     * 保存草稿
     */
    private static String workflowDraf() throws BusinessException {
        Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
        String processId = wfdef.get("processId");
        String processXml = wfdef.get("process_xml");
        String moduleType = wfdef.get("moduleType");
        
        //H5参数处理：从流程临时表中取出流程变更之后的processXml
        processXml = wapi.getTempProcessXml(processXml);
        
        if (Strings.isBlank(processXml)
                && (Strings.isBlank(processId) || (Strings.isNotBlank(processId) && "-1".equals(processId))))
            return "";
        String result = wapi.saveProcessXmlDraf(processId, processXml, moduleType);
        updatePermissinRef(Integer.parseInt(moduleType), processXml, processId, "",AppContext.currentAccountId());
        return result;
    }

    public static RunCaseResult workflowNew(AffairData affairData, Object wfContextBizObject) throws BusinessException {
        Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
        String process_xml = wfdef.get("process_xml");
        String processId = wfdef.get("processId");
        String caseId = wfdef.get("caseId");
        String popNodeSubProcessJson = wfdef.get("workflow_newflow_input");
        String selectedPeoplesOfNodes = wfdef.get("workflow_node_peoples_input");
        String conditionsOfNodes = wfdef.get("workflow_node_condition_input");
        Integer moduleType = ParamUtil.getInt(wfdef, "moduleType", ModuleType.collaboration.getKey());
        
        //H5参数处理：从流程临时表中取出流程变更之后的processXml
        process_xml = wapi.getTempProcessXml(process_xml);
        
        return workflowNew(affairData, wfContextBizObject, process_xml, moduleType, processId, caseId,
                popNodeSubProcessJson, selectedPeoplesOfNodes, conditionsOfNodes);
    }

    public static RunCaseResult workflowNew(AffairData affairData, Object wfContextBizObject, String process_xml,
            Integer moduleType, String process_id, String caseId2, String popNodeSubProcessJson,
            String selectedPeoplesOfNodes, String conditionsOfNodes) throws BusinessException {

        User user = AppContext.getCurrentUser();

        WorkflowBpmContext context = new WorkflowBpmContext();
        context.setAppName(ModuleType.getEnumByKey(moduleType).name());
        context.setDebugMode(false);
        if (process_id != null && !"".equals(process_id.trim()) && !"-1".equals(process_id)) {
            context.setProcessId(process_id);
        }
        context.setProcessXml(process_xml);
        context.setStartUserId(String.valueOf(user.getId()));
        context.setStartUserName(user.getName());
        context.setStartAccountId(String.valueOf(user.getLoginAccount()));
        context.setStartAccountName("seeyon");
        context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
        context.setBusinessData("bizObject", wfContextBizObject);
        context.setBusinessData(WorkFlowEventListener.COLSUMMARY_CONSTANT,wfContextBizObject);
        context.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_MEMBER_ID, user.getId());
        context.setBusinessData("operationType", affairData.getBusinessData().get("operationType"));
        context.setPopNodeSubProcessJson(popNodeSubProcessJson);
        context.setSelectedPeoplesOfNodes(selectedPeoplesOfNodes);
        context.setConditionsOfNodes(conditionsOfNodes);
        context.setMastrid("" + affairData.getFormRecordId());
        context.setFormData("" + affairData.getFormAppId());
        context.setAppObject(wfContextBizObject);
        // 指定回退到发起者后发起者再发送
        if (affairData.getCaseId() != null) {
            context.setCurrentActivityId("start");
            context.setCaseId(affairData.getCaseId());
        }
        RunCaseResult runCaseResult = null;
        CtpTemplate ct = null;
        if(null != affairData.getTemplateId()){
        	 ct = templateManager.getCtpTemplate(affairData.getTemplateId());
        }


        Long flowAccount =  AppContext.currentAccountId();
        if(affairData.getBusinessData(ColConstant.FlowPermAccountId) != null){
            flowAccount = (Long)affairData.getBusinessData(ColConstant.FlowPermAccountId);
        }
        
        //协同V5.0OA-29506从协同待发列表，勾选一个协同模板,发送,报错 去掉第三个 && Strings.isNotBlank(context.getProcessId())的判断条件
        if (null != affairData.getTemplateId() && null != ct
                && null != ct.getWorkflowId()) {
            context.setProcessTemplateId(ct.getWorkflowId().toString());
            runCaseResult = wapi.transRunCaseFromTemplate(context);
            
            updatePermissinRef(moduleType, process_xml, "-1", process_id,flowAccount);
        } else {
            //缓存caseId
        	runCaseResult = wapi.transRunCase(context);
            updatePermissinRef(moduleType, process_xml, process_id, "-1",flowAccount);
        }

        return runCaseResult;
    }
    public static  Map<String,Object> workflowFinish(Comment c, AffairData affairData,long subObjectId,CtpAffair affair,ColSummary summary,String conditionsOfNodes,String subState,Map<String,Object> params) throws BusinessException {

        User user = AppContext.getCurrentUser();
        WorkflowBpmContext context = new WorkflowBpmContext();
        context.setDebugMode(false);
        context.setAppObject(summary);
        context.setBussinessId(String.valueOf(affair.getObjectId())); 
        context.setAffairId(String.valueOf(affair.getId()));
        context.setBusinessData("CtpAffair", affair);
        context.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_MEMBER_ID, affair.getMemberId());
        context.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_COMMENT_ID,c.getId());
        context.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_AFFAIR_ID,affair.getId());
        Map<String,Object> globalMap = new HashMap<String,Object>();
        context.setBusinessData(WorkFlowEventListener.WF_APP_GLOBAL,globalMap);
        context.setBusinessData(WorkFlowEventListener.COLSUMMARY_CONSTANT,summary);
        context.setCurrentWorkitemId(subObjectId);
        if (!user.getId().equals(affair.getMemberId())) {
            context.setCurrentUserId(String.valueOf(affair.getMemberId()));
            V3xOrgMember affairMember= orgManager.getMemberById(affair.getMemberId());
            context.setCurrentUserName(affairMember.getName());
            context.setCurrentAccountId(String.valueOf(affairMember.getOrgAccountId()));
        }else{
            context.setCurrentUserId(String.valueOf(user.getId()));
            context.setCurrentUserName(user.getName());
            context.setCurrentAccountId(String.valueOf(user.getLoginAccount()));
        }
        context.setCurrentAccountName("");
        context.setBusinessData("comment", c);
        context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
        context.setBusinessData(WorkFlowEventListener.AFFAIR_SUB_STATE, subState);
        //是否在工作流外部执行了onworkItemFinished的相关逻辑，外部执行了，工作流内部就不执行了。当初移动到外部是性能考虑，避免内外两次更新affair对象。
        context.setBusinessData("__hasExeWorkItemFinishedOuter", true);
        @SuppressWarnings("unchecked")
        Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
        String processXml = wfdef.get("process_xml");
        String dynamicFormMasterIds = wfdef.get("dynamicFormMasterIds");
        boolean isRego  = "true".equals(wfdef.get("toReGo"));
        context.setDynamicFormMasterIds(dynamicFormMasterIds);
        context.setToReGo(isRego);
        
        String currentNodeLast = (String)params.get("currentNodeLast");
        if(currentNodeLast == null){
            currentNodeLast  = wfdef.get("workflow_thisNodelast_input");
        }
        context.setCurrentNodeLast(currentNodeLast);
        
        boolean isMobile = "true".equals((String)params.get("isMobile"));
        context.setMobile(isMobile);
        if(isRego){
        	CtpAffair stepBackAffair = null;
        	List<CtpAffair>  affairs = affairManager.getAffairs(affair.getObjectId(), StateEnum.col_pending,SubStateEnum.col_pending_specialBack);
        	if(Strings.isNotEmpty(affairs)){
        		for(CtpAffair _affair : affairs){
        			stepBackAffair = _affair;
        			break;
        		}
        	}
        	context.setBusinessData("_ReMeToReGo_operationType",WorkFlowEventListener.SPECIAL_BACK_RERUN);
        	context.setBusinessData("_ReMeToReGo_stepBackAffair",stepBackAffair);
        	context.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_TRACK_FLOW,"1");
        }
        else{
        	context.setBusinessData("operationType", affairData.getBusinessData().get("operationType"));
        }
        context.setBusinessData(BackgroundDealParamBO.EXTPARAM_IS_MODIFYWORKFLOW_MODEL,params.get(BackgroundDealParamBO.EXTPARAM_IS_MODIFYWORKFLOW_MODEL));
        //H5参数处理：从流程临时表中取出流程变更之后的processXml
        processXml = wapi.getTempProcessXml(processXml);
        
        if (processXml != null && !"".equals(processXml.trim())) {
            context.setProcessXml(processXml);
        }
        String readyObjectJSON = wfdef.get("readyObjectJSON");
        if (readyObjectJSON != null && !"".equals(readyObjectJSON.trim())) {
            context.setReadyObjectJson(readyObjectJSON);
        }
        String popNodeSubProcessJson = wfdef.get("workflow_newflow_input");
        String selectedPeoplesOfNodes = wfdef.get("workflow_node_peoples_input");
        if(Strings.isBlank(conditionsOfNodes)){
        	conditionsOfNodes = wfdef.get("workflow_node_condition_input");
        } 
        String processChangeMessage = wfdef.get("processChangeMessage");
        context.setPopNodeSubProcessJson(popNodeSubProcessJson);
        context.setSelectedPeoplesOfNodes(selectedPeoplesOfNodes);
        if (Strings.isBlank(conditionsOfNodes)) {
        	context.setConditionsOfNodes("{\"condition\":[{\"nodeId\":\"end\",\"isDelete\":\"false\"}]}");
        } else {
        	context.setConditionsOfNodes(conditionsOfNodes);
		}
        String newGenerateNodeId = wfdef.get("newGenerateNodeId");
        context.setNewGenerateNodeId(newGenerateNodeId);
        
        String newGenerateOtherNodeIds = wfdef.get("newGenerateOtherNodeIds");
        context.setNewGenerateOtherNodeIds(newGenerateOtherNodeIds);
        
        context.setChangeMessageJSON(processChangeMessage);
        context.setMastrid(affairData.getFormRecordId() == null ? null : String.valueOf(affairData.getFormRecordId()));
        context.setFormData(affairData.getFormAppId() == null ? null : String.valueOf(affairData.getFormAppId()));
        context.setVersion("2.0");
        if(null==affairData.getTemplateId()){//自由流程提交
        	context.setFreeFlow(true);
        }
        
        //获取意见
        String attitude = "";
        String content = "";
        if (c != null) {
            attitude = c.getExtAtt1();
            content = c.getContent();
        }
        context.setBusinessData(WorkflowEventData.WORKFLOW_EVENT_ATTRITUDE, attitude);
        context.setBusinessData(WorkflowEventData.WORKFLOW_EVENT_COMMENT_CONTENT, content);
        
        String[] result = wapi.finishWorkItem(context);
        
        Long flowAccount =  AppContext.currentAccountId();
        if(affairData.getBusinessData(ColConstant.FlowPermAccountId) != null){
            flowAccount = (Long)affairData.getBusinessData(ColConstant.FlowPermAccountId);
        }
        String[] policyIds = null;
        if(result!=null && result.length == 3){
       	 	policyIds = result[2].split(","); 
        }
        updatePermissinRef(affairData.getModuleType(), policyIds,flowAccount);
        
        Map<String,Object> wfRetMap =  new HashMap<String,Object>();
        wfRetMap.put("isRego", isRego ? "true" : "false");
        wfRetMap.put("nextMembers", result!=null && result.length >0?result[0]:"");
        wfRetMap.put("nextMembersWithoutPolicyInfo", result!=null && result.length >1?result[1]:"");
        wfRetMap.put(WorkFlowEventListener.WF_APP_GLOBAL_REPEAT_AFFMAP, globalMap.get(WorkFlowEventListener.WF_APP_GLOBAL_REPEAT_AFFMAP)); 
        
        wfRetMap.put(WorkFlowEventListener.BUSINESS_DATA, context.getBusinessData());
		/* String submitConfirmMsg= context.getSubmitConfirmMsg();
		wfRetMap.put("submitConfirmMsg", submitConfirmMsg);*/
        wfRetMap.put("processLogDetails", context.getProcessLogDetails());
        wfRetMap.put("currentNodeLast", currentNodeLast);
        wfRetMap.put("currentActivityChangedId", context.getCurrentActivityChangedId());
        return wfRetMap;
    }

    public static  Map<String,Object>   workflowWait(AffairData affairData, Long itemId,ColSummary summary,Map<String,Object> params) throws BusinessException {
        User user = AppContext.getCurrentUser();
        WorkflowBpmContext context = new WorkflowBpmContext();
        context.setAppObject(summary);
        context.setDebugMode(false);
        //context.setAppName(ModuleType.getEnumByKey(content.getModuleType()).name());
        context.setCurrentWorkitemId(itemId);
        context.setCurrentUserId(String.valueOf(user.getId()));
        context.setCurrentUserName(user.getName());
        context.setCurrentAccountId(String.valueOf(user.getAccountId()));
        context.setCurrentAccountName("");
        context.setBusinessData("operationType", affairData.getBusinessData().get("operationType"));
        context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
        context.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_MEMBER_ID, user.getId());
        context.setBusinessData(WorkFlowEventListener.COLSUMMARY_CONSTANT,summary); 
        context.setMastrid(summary.getFormRecordid() == null ? null : String.valueOf(summary.getFormRecordid()));
        context.setFormData(summary.getFormAppid() == null ? null : String.valueOf(summary.getFormAppid()));
        context.setVersion("2.0");
        //保存修改后的流程数据
        Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
        String processXml = wfdef.get("process_xml");
        String readyObjectJSON = wfdef.get("readyObjectJSON");
        String processChangeMessage = wfdef.get("processChangeMessage");
        context.setChangeMessageJSON(processChangeMessage);
        boolean isMobile = "true".equals((String)params.get("isMobile"));
        context.setMobile(isMobile);
        //H5参数处理：从流程临时表中取出流程变更之后的processXml
        processXml = wapi.getTempProcessXml(processXml);
        
        if (Strings.isNotBlank(processXml)) {
            context.setProcessXml(processXml);
        }
        if (Strings.isNotBlank(readyObjectJSON)) {
            context.setReadyObjectJson(readyObjectJSON);
        }
        
        String newGenerateNodeId = wfdef.get("newGenerateNodeId");
        context.setNewGenerateNodeId(newGenerateNodeId);
        
        String newGenerateOtherNodeIds = wfdef.get("newGenerateOtherNodeIds");
        context.setNewGenerateOtherNodeIds(newGenerateOtherNodeIds);
        
        wapi.temporaryPending(context);
        
        
        Map<String,Object> wfRetMap  = new HashMap<String,Object>();
        wfRetMap.put(WorkFlowEventListener.BUSINESS_DATA, context.getBusinessData());
        wfRetMap.put("currentActivityChangedId", context.getCurrentActivityChangedId());
        return wfRetMap;
    }



    /**
     * 撤销流程 1:流程已结束，不许撤消 0:撤消完成
     * @param affairData 
     * @param caseId
     * @return
     * @throws BPMException 
     */
    public static int cancelCase(ColSummary summary,AffairData affairData, Long caseId,boolean cancelFinishedProcess,String commentContent) throws BPMException {
        WorkflowBpmContext context = new WorkflowBpmContext();
        context.setCaseId(caseId);
        context.setBussinessId(String.valueOf(summary.getId()));
        context.setAppObject(summary);
        context.setBusinessData(affairData.getBusinessData());
        context.setMastrid(affairData.getFormRecordId() == null ? null : String.valueOf(affairData.getFormRecordId()));
        context.setFormData(affairData.getFormAppId() == null ? null : String.valueOf(affairData.getFormAppId()));
        context.setCancelFinishedProcess(cancelFinishedProcess);
       
        Long affairId = (Long)affairData.getBusinessData(WorkFlowEventListener.CURRENT_OPERATE_AFFAIR_ID);
        context.setAffairId(affairId == null ? null : String.valueOf(affairId));
        
        Long summaryId = (Long)affairData.getBusinessData(WorkFlowEventListener.CURRENT_OPERATE_SUMMARY_ID);
        context.setBussinessId(summaryId == null ? null : String.valueOf(summaryId));
        context.setBusinessData(WorkflowEventData.WORKFLOW_EVENT_COMMENT_CONTENT, commentContent);
        return wapi.cancelCase(context);
    }
    



   
    public static String findRightIdbyAffairIdOrTemplateId(CtpAffair affair,Long templateId) throws BusinessException {
    	  CtpTemplate template = templateManager.getCtpTemplate(templateId);
    	  return findRightIdbyAffairIdOrTemplateId(affair,template,false,"");
    }
    
    public static String findRightIdbyAffairIdOrTemplate(CtpAffair affair,CtpTemplate template) throws BusinessException {
        return findRightIdbyAffairIdOrTemplateId(affair,template,false,"");
    }
    
    /**
     * 待办，已办直接从Affair中去，如果取不到从工作流中取
     * 已发新建从工作流中取
     * @param affair
     * @param templateId
     * @param isFromDocLib :是否从文档中心打开的预归档文件
     * @return 
     * @throws BusinessException
     */
    public static String findRightIdbyAffairIdOrTemplateId(CtpAffair affair,CtpTemplate template,boolean isFromDocLibByAccountPighole,String wfOperationId) throws BusinessException {
    	
        String operationId = "-1";
        try{
        	if( isFromDocLibByAccountPighole
        		&& ColUtil.isForm(affair.getBodyType())
        		&& Integer.valueOf(StateEnum.col_sent.getKey()).equals(affair.getState())){
        		
        		if(null != template){
					ColSummary tsummary =  XMLCoder.decoder(template.getSummary(),ColSummary.class);
					if(null != tsummary.getArchiveId()){
						String s = (String)tsummary.getExtraAttr("archiverFormid");
						if(Strings.isNotBlank(s)){
							if(s.endsWith("|")){
								s = s.substring(0,s.length()-1);
							}
							operationId = s;
						}
					}
				}
        	}
        	if("-1".equals(operationId)){
        		if(affair != null 
                    && (Integer.valueOf(StateEnum.col_pending.getKey()).equals(affair.getState()) 
                    		|| Integer.valueOf(StateEnum.col_done.getKey()).equals(affair.getState())
                    		|| Integer.valueOf(StateEnum.col_sent.getKey()).equals(affair.getState())
                    		|| Integer.valueOf(StateEnum.col_pending_repeat_auto_deal.getKey()).equals(affair.getState())
        					|| Integer.valueOf(StateEnum.col_stepBack.getKey()).equals(affair.getState())
        					|| Integer.valueOf(StateEnum.col_cancel.getKey()).equals(affair.getState()))){ //回退、撤销是流程赘述查看的时候使用
                    
        			if(Strings.isNotBlank(affair.getMultiViewStr())) {
        			    operationId = affair.getMultiViewStr();
                    }else{
                        ColManager colManager  = (ColManager)AppContext.getBean("colManager");
                        ColSummary summary = colManager.getColSummaryById(affair.getObjectId());
                        if(summary!= null){
                        	
                        	if(Strings.isBlank(wfOperationId)){
                        		String nodeId = affair.getActivityId() == null? null : String.valueOf( affair.getActivityId());
                            	String [] arr = wapi.getNodePolicyInfos(summary.getProcessId(),nodeId );
                            	operationId = arr[0];
                        	}else{
                        		operationId = wfOperationId;
                        	}
                        }
                    }
                    
                }else{//待发
                    if (template != null) {
                		if(template.getWorkflowId() != null){
                			
                			if(Strings.isBlank(wfOperationId)){
                            	String [] arr = wapi.getNodePolicyInfosFromTemplate(template.getWorkflowId(), null);
                            	operationId = arr[0];
                        	}else{
                        		operationId = wfOperationId;
                        	}
                		}
                    }
                }
        	}
            
        }catch(Throwable t){
            log.error("",t);
            throw new BusinessException(t);
        }
        operationId =operationId==null ? null : operationId.replaceAll("[|]","_");
        return operationId;
    }
    
    
    /**
     * 删除正文组件相关内容（正文、意见/回复、流程）
     * 
     * @param moduleType
     * @param moduleId
     * @throws BusinessException
     */
    public static void contentDelete(ModuleType moduleType, Long moduleId, String processId) throws BusinessException {
        User user = AppContext.getCurrentUser();

        contentManager.deleteContentAllByModuleId(moduleType, moduleId);
        //TODO 删除表单数据
        commentManager.deleteCommentAllByModuleId(moduleType, moduleId);

        if (Strings.isNotBlank(processId)) {
            wapi.deleteProcessXmlDraf(processId, moduleType.name(), user.getId(), user.getName());
        }
    }

}
