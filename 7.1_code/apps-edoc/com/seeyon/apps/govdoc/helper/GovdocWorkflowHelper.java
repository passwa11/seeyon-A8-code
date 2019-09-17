package com.seeyon.apps.govdoc.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.vo.SeeyonPolicy;
import com.seeyon.apps.govdoc.vo.GovdocListVO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.bo.WorkflowAnalysisParam;
import com.seeyon.ctp.common.content.ContentConfig;
import com.seeyon.ctp.common.content.ContentViewRet;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.mainbody.CtpContentAllBean;
import com.seeyon.ctp.common.content.mainbody.MainbodyStatus;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;

import net.joinwork.bpm.definition.BPMAbstractNode;
import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;
import net.joinwork.bpm.definition.BPMTransition;	

public class GovdocWorkflowHelper extends GovdocHelper {

	private static final Log LOGGER = LogFactory.getLog(GovdocWorkflowHelper.class);
	
	private static CollaborationApi collaborationApi;
    
	/**
     * 内容查看相关处理，返回内容查看列表（支持多正文）
     * 
     * @param moduleType 模块类型，默认值ModuleType.collaboration.getKey()
     * @param moduleId 模块ID，默认值-1（新建），否则为模块多内容新建
     * @param rightId 新建内容权限ID，默认值-1，例如表单模板权限ID
     * @return 内容查看Content对象列表（支持多正文）
     * @throws BusinessException 内容查看相关异常
     */
    @SuppressWarnings({ "deprecation", "unchecked" })
	public static ContentViewRet contentView(ModuleType moduleType, Long moduleId, Long affairId, int viewState,
            String rightId) throws BusinessException {
        if (moduleType == null)
            moduleType = ModuleType.collaboration;
        if (moduleId == null)
            moduleId = -1l;
        HttpServletRequest request = (HttpServletRequest) AppContext
                .getThreadContext(GlobalNames.THREAD_CONTEXT_REQUEST_KEY);
        List<CtpContentAllBean> contentList = ctpMainbodyManager.transContentViewResponse(moduleType, moduleId, viewState,
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
            Map<String,Object> commentsMap = ctpCommentManager.getCommentsWithForward(moduleType,moduleId);
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
    
    /**
	 * 取当前节点的父人工节点
	 * @param activity   当前人工节点
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List getParent(BPMActivity activity){
		List humenList = new ArrayList();
        List<BPMTransition> transitions = activity.getUpTransitions();
        for (BPMTransition trans : transitions) {
            BPMAbstractNode parent = trans.getFrom();
            BPMSeeyonPolicy policy= parent.getSeeyonPolicy();
        	String policyId= policy.getId();
        	String isDelete= policy.getIsDelete();
        	if("false".equals(isDelete)){
        		if (parent.getNodeType() == BPMAbstractNode.NodeType.humen) {
                	//处理空节点及知会节点
                	if("zhihui".equals(policyId) || "inform".equals(policyId)){
                		humenList.addAll(getParent((BPMActivity) parent));
                	}else{
                		humenList.add((BPMHumenActivity) parent);
                	}
                } else if(parent.getNodeType() == BPMAbstractNode.NodeType.start){
                	humenList.add(parent);
                } else if (parent.getNodeType() == BPMAbstractNode.NodeType.join || parent.getNodeType() == BPMAbstractNode.NodeType.split) {
                    humenList.addAll(getParent((BPMActivity) parent));
                }
        	}
        }
        return humenList;
	}
    
    /**
	 * 取当前节点的父人工节点
	 * @param activity   当前人工节点
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List getNext(BPMActivity activity) {
		List humenList = new ArrayList();
		List<BPMTransition> transitions = activity.getDownTransitions();
        for (BPMTransition trans : transitions) {
            BPMAbstractNode next = trans.getTo();
            BPMSeeyonPolicy policy= next.getSeeyonPolicy();
        	String policyId= policy.getId();
        	String isDelete= policy.getIsDelete();
        	if("false".equals(isDelete)){
        		if (next.getNodeType() == BPMAbstractNode.NodeType.humen) {
                	//处理空节点及知会节点
                	if("zhihui".equals(policyId) || "inform".equals(policyId)){
                		humenList.addAll(getNext((BPMActivity) next));
                	}else{
                		humenList.add((BPMHumenActivity) next);
                	}
                } else if(next.getNodeType() == BPMAbstractNode.NodeType.start){
                	humenList.add(next);
                } else if (next.getNodeType() == BPMAbstractNode.NodeType.join || next.getNodeType() == BPMAbstractNode.NodeType.split) {
                    humenList.addAll(getNext((BPMActivity) next));
                }
        	}
        }
        return humenList;
	}
    
	private static String getNodePolicyName(String key){
    	if(key == null){
    		return "";
    	}
    	if(key.startsWith("fenban_zhihui")){
    		return "知会";
    	}
    	if(Pattern.compile("[\\w]+").matcher(key).matches()){
    		return ResourceUtil.getString("node.policy."+key);
    	}
    	return key;
    }
	
	/**
	 * 验证流程节点权限节点权限
	 * @param affair
	 * @param pageNodePolicy
	 * @return
	 * @throws BusinessException
	 */
	public static String checkNodePolicyChange(CtpAffair affair, String pageNodePolicy) throws BusinessException {
		String errorMsg = "";
		if(affair == null
        		|| affair.getState().equals(StateEnum.col_waitSend.getKey())
        		|| affair.getState().equals(StateEnum.col_sent.getKey())){
			return errorMsg;
		}
		SeeyonPolicy affairNodePolicy = getCollaborationApi().getPolicyByAffair(affair);
		if(Strings.isNotBlank(pageNodePolicy) && !affairNodePolicy.getId().equals(pageNodePolicy)){
			errorMsg = ResourceUtil.getString("collaboration.nodePolicy.change");
		}
		return errorMsg;
	}
	
	public static CollaborationApi getCollaborationApi() {
        if (collaborationApi == null) {
            collaborationApi = (CollaborationApi) AppContext.getBean("collaborationApi");
        }
        return collaborationApi;
    }
	
}
