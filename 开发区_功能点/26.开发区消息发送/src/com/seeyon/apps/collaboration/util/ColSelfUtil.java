package com.seeyon.apps.collaboration.util;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.seeyon.apps.collaboration.event.CollaborationAutoSkipEvent;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.supervise.vo.SuperviseMessageParam;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.json.JSONUtil;
/**
 * 仅限协同（apps-collabration）内部使用，不能被其他模块调用，一旦调用，出问题概不负责。
 * @author Administrator
 */
public class ColSelfUtil {
	/**
	 * 发送自动跳过的事件
	 * @param source
	 */
	public static void fireAutoSkipEvent(Object source,Map<String,String> map) {
		if (map != null && !map.isEmpty()) {
			java.util.Queue<Map<String, String>> affairsQueue = new LinkedList<Map<String, String>>();
			//获取自动跳过的json串
			String skipJsonString = map.get("skipJsonString");
			List<Map<String,String>> skipJsonArray = (List<Map<String, String>>) JSONUtil.parseJSONString(skipJsonString);
			if (skipJsonArray != null && !skipJsonArray.isEmpty()) {
				for (Map<String,String> beanObj : skipJsonArray) {
					Map<String, String> dataMap = cloneMap(map);
					dataMap.put("_affairIds", beanObj.get("affairId"));
					dataMap.put("_skipAgentId", beanObj.get("skipAgentId"));
					dataMap.put("mergeDealType", beanObj.get("mergeDealType"));
					dataMap.put("_commentId", beanObj.get("_commentId"));
					affairsQueue.offer(dataMap);
				}
			}
			CollaborationAutoSkipEvent event = new CollaborationAutoSkipEvent(source);
			event.setDataQueues(affairsQueue);
			EventDispatcher.fireEventAfterCommit(event);
		}
	}
	
	public static void sortAttachmentList(List<Attachment> mainAtt) {
        Collections.sort(mainAtt,new Comparator<Attachment>(){  
            @Override  
            public int compare(Attachment a1, Attachment a2) {  
            	//附件列表需倒序
        		Date d1=a1.getCreatedate();
        		Date d2=a2.getCreatedate();
        		int res=0;
        		if(d1!=null&&d2!=null){
        			res=d1.compareTo(d2);
        			if(res == 0){
	        			if(a1.getSort() > a2.getSort())	{
	        				res = 1;
	        			} else if (a1.getSort()< a2.getSort()){
	        				res = -1 ;
	        			} else{
	        				res = 0;
	        			}
        			}
        		}else if(d1 == null&&d2!=null){
        			res=-1;
        		}else if(d1!=null){
        			res=1;
        		}
        		return res==0?0:res>0?1:-1;  
            }  
              
        });
	}
	
	
	private static Map<String,String> cloneMap(Map<String,String> sourceMap){
		if(sourceMap == null){
			return null;
		}
		Set<String> keys = sourceMap.keySet();
		Map<String,String> newMap = new HashMap<String,String>();
		for(String key :keys){
			newMap.put(key,sourceMap.get(key));
		}
		return newMap;
	}
	

	
    
    /**
     * 超期跳过、重复跳过、批处理、AI处理共有的能否处理的校验结果类
     * @param affair
     * @return
     */
    public static CanBackgroundDealResult publicCheckCanBackgroundDeal(CanBackgroundDealParam param) {
        CtpAffair affair = param.getAffair();

        CanBackgroundDealResult result = new CanBackgroundDealResult();
        String currentNodeLast = "";
        if (param.getPr() != null) {
            currentNodeLast = param.getPr().getCurrentNodeLast();
        }
        // 处理前事件
        String msg = ColUtil.executeWorkflowBeforeEvent(affair, "BeforeFinishWorkitem", currentNodeLast,param.getComment());
        if (Strings.isNotBlank(msg)) {
            result.setCan(false);
            result.setMsg(msg);
            return result;
        }
        return result;
    }
    
    public static SuperviseMessageParam convertTOSMP(ColSummary summary){
        SuperviseMessageParam smp = new SuperviseMessageParam();
        smp.setImportantLevel(summary.getImportantLevel());
        smp.setForwardMember(summary.getForwardMember());
        smp.setSendMessage(true);
        smp.setSubject(summary.getSubject());
        smp.setMemberId(summary.getStartMemberId());

        smp.setSummaryId(summary.getId());
        
        return smp;
        
    }
    
    
    /**
     * 创建一个没有态度的空意见，只有处理人、处理时间
     * @param affair
     * @param summary
     * @param extParam
     * @return
     */

    public static Comment createNullCommentWithoutAttitude(CtpAffair affair, ColSummary summary) {
        
    	Comment comment = new Comment();
        
    	comment.setHidden(false);
        comment.setId(UUIDLong.longUUID());
        

        comment.setCtype(Comment.CommentType.comment.getKey());
        comment.setPid(0L);
        comment.setPraiseNumber(0);
        comment.setPraiseToComment(false);
        comment.setPraiseToComment(false);
        comment.setModuleType(ModuleType.collaboration.getKey());
        comment.setModuleId(summary.getId());
        comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
        comment.setAffairId(null);
        comment.setCreateId(affair.getMemberId());
        comment.setForwardCount(0);
        comment.setPath(AppContext.getCurrentUser().getUserAgentFrom()==null?"pc":AppContext.getCurrentUser().getUserAgentFrom());
        
        comment.setDepartmentId(affair.getMatchDepartmentId());
        comment.setPostId(affair.getMatchPostId());
        comment.setAccountId(affair.getMatchAccountId());
        
        return comment;
    }
    
}
