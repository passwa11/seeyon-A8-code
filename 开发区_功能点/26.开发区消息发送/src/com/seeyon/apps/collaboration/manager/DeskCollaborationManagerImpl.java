package com.seeyon.apps.collaboration.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.utils.AgentUtil;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.affair.bo.AffairCondition;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.customize.manager.CustomizeManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.portal.portlet.bo.CollaborationInfo;
import com.seeyon.ctp.portal.portlet.manager.DeskCollaborationManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.AjaxAccess;

public class DeskCollaborationManagerImpl implements DeskCollaborationManager {
	private static Log LOG = CtpLogFactory.getLog(DeskCollaborationManagerImpl.class);
	public CustomizeManager customizeManager;
	private ColManager colManager;
	public ColManager getColManager() {
		return colManager;
	}


	public void setColManager(ColManager colManager) {
		this.colManager = colManager;
	}


	public void setCustomizeManager(CustomizeManager customizeManager) {
		this.customizeManager = customizeManager;
	}

	public List<CtpAffair> getByConditionsPagination(FlipInfo flipInfo,Map params) throws BusinessException {
		StringBuilder hql = new StringBuilder("from "+ CtpAffair.class.getName() + " as affair where 1=1  ");
		Map<String,Object> map = new HashMap<String,Object>();
		if (params.containsKey("memberId")) {
			hql.append(" and affair.memberId=:memberId  ");
			map.put("memberId", params.get("memberId"));
		}
		
		if (params.containsKey("state")) {
			hql.append(" and affair.state in (:state)  ");
			map.put("state", params.get("state"));
		}
		
		if (params.containsKey("delete")) {
            hql.append(" and affair.delete = :delete  ");
            map.put("delete", params.get("delete"));
        }
		
		hql.append(" and (affair.subState != :substate or affair.subState is null) ");
		map.put("substate",SubStateEnum.meeting_pending_periodicity.getKey());
    	
		
	
		
		
		AffairCondition condition = new AffairCondition((Long)(params.get("memberId")), StateEnum.col_pending,
                ApplicationCategoryEnum.collaboration,
                ApplicationCategoryEnum.edoc,
                ApplicationCategoryEnum.meeting,
                ApplicationCategoryEnum.bulletin,
                ApplicationCategoryEnum.news,
                ApplicationCategoryEnum.inquiry,
                ApplicationCategoryEnum.office,
                ApplicationCategoryEnum.info,
                ApplicationCategoryEnum.meetingroom,
                ApplicationCategoryEnum.edocRecDistribute,
                ApplicationCategoryEnum.infoStat
        );
		
		Object[] agentObj = AgentUtil.getUserAgentToMap((Long)(params.get("memberId")));
        boolean agentToFlag = (Boolean)agentObj[0];
        Map<Integer,List<AgentModel>> ma = (Map<Integer,List<AgentModel>>)agentObj[1];
        condition.setAgent(agentToFlag, ma);
		
		int len = condition.getApps().size();
    	if(len>0){
    		hql.append(" and (");
    	}
    	int count = 0;
    	for(ApplicationCategoryEnum app : condition.getApps()){
			switch(app){
			case collaboration:
				 count++;
				 if(count>1)
					 hql.append(" or ");
				 hql.append(condition.getSql4ColAgent("affair",map,true));
				 break;
			case info:
				 count++;
				 if(count>1)
					 hql.append(" or ");
				 hql.append("(");
				 hql.append("affair.app = :getHql32ColAgentapp  and affair.subApp != :subApp  ");
				 hql.append(")");
				 map.put("getHql32ColAgentapp", ApplicationCategoryEnum.info.key());
				 map.put("subApp",ApplicationSubCategoryEnum.info_magazine_publish.getKey());
				 break;
			case edoc:
			case meeting:
			case bulletin:
			case news:
			case inquiry:
			case meetingroom:
			case office:
				 count++;
				 if(count>1)
					 hql.append(" or ");
				 hql.append(condition.getSql4AppAgent(app,"affair",map,true));
				break;
			}
		}
    	if(len>0){
    		hql.append(")");
    	}
		
		if(params.containsKey("receiveTimeAscOrDesc")){
			hql.append(" order by affair.receiveTime ").append(params.get("receiveTimeAscOrDesc"));
		}else{
			hql.append(" order by affair.receiveTime desc ");
		}
		return DBAgent.find(hql.toString(), map, flipInfo);
	} 
	//@Override
	@SuppressWarnings("unchecked")
	@AjaxAccess
	public FlipInfo getCollaborationList(String size)throws BusinessException {
		List<CollaborationInfo> list = new ArrayList<CollaborationInfo>();
		FlipInfo flipInfo = new FlipInfo(); 
		

		if(Strings.isBlank(size)){
			return flipInfo;
		}
		
		flipInfo.setSize(50);
		
		Map map = new HashMap();
		map.put("memberId", AppContext.currentUserId());
		List<Integer> states = new ArrayList<Integer>();
		states.add(StateEnum.col_pending.key());
		map.put("state", states);
		map.put("delete", Boolean.FALSE);
		map.put("receiveTimeAscOrDesc", " desc ");
		List<CtpAffair> affairs = getByConditionsPagination(flipInfo, map);
		LOG.info("工作桌面：条数："+affairs.size()+",size："+size+",用户："+AppContext.getCurrentUser().getName());
		//过滤不需要的affair
//		for(Iterator<CtpAffair> it = affairs.iterator() ;it.hasNext();){
//			CtpAffair a  = it.next();
//			if(Integer.valueOf(ApplicationCategoryEnum.meeting.key()).equals(a.getApp()) 
//					|| Integer.valueOf(ApplicationCategoryEnum.meetingroom.key()).equals(a.getApp())){
//				if(Integer.valueOf(SubStateEnum.meeting_pending_periodicity.key()).equals(a.getSubState())){
//					it.remove();
//				}
//			}
//		}
		
		List<Long> objectIds = new ArrayList<Long>();
		if(Strings.isNotEmpty(affairs)){
			for(CtpAffair aff : affairs){
				   boolean isHtml = String.valueOf(MainbodyType.HTML.getKey()).equals(aff.getBodyType());
				   if(isHtml){
					   objectIds.add(aff.getObjectId());
				   }
			}
		}
		 Map<Long,String> m = getHtmlContent(objectIds);
		
		if(Strings.isNotEmpty(affairs)){
			for(CtpAffair aff : affairs){
				CollaborationInfo info = new CollaborationInfo();
				info.setId(aff.getId());
				String _subject = ColUtil.showSubjectOfAffair(aff, false, -1).replaceAll("\r\n", "").replaceAll("\n", "");
				info.setSubject(_subject);
				info.setStartMember(aff.getSenderId());
				info.setModuleType(aff.getApp());
				info.setSubAppId(aff.getSubApp());
				info.setFormViewOperation(aff.getMultiViewStr());
				info.setObjectId(aff.getObjectId());
				info.setSubObjectId(aff.getSubObjectId());
				info.setAvatarImgUrl(this.getSenderAvatarImageUrl(aff.getSenderId()));
				info.setContent(m.get(aff.getObjectId()));
				info.setTemplateId(aff.getTempleteId());
				list.add(info);
				if(list.size() >= Integer.valueOf(size).intValue()){
					break;
				}
			}
		}
		
		flipInfo.setData(list);
		
		return flipInfo;
	}
	private Map<Long,String> getHtmlContent(List<Long> objectIds){
		 Map m = new HashMap();
		 if(Strings.isEmpty(objectIds)){
			 return m;
		 }
		 m.put("objectIds",objectIds );
		 
		 String s = "from CtpContentAll where moduleId in (:objectIds) ";
		 List<CtpContentAll> contentPoList = DBAgent.find(s,m); 
		 List<Long> contentList = new ArrayList<Long>();
		 Map<Long,String> map = new HashMap<Long,String>();
		 try {
			 List<ColSummary> colSummary = colManager.findColSummarysByIds(objectIds);
			 for(CtpContentAll all : contentPoList){ 
				 for (ColSummary summary : colSummary) {
					 
					 if(!summary.getCanEdit()  && null != summary.getParentformSummaryid()){
						 continue;
					 } else {
						 contentList.add(summary.getId());
					 }
				 }
			 }
			 for(CtpContentAll all : contentPoList){
				 if (contentList.contains(all.getModuleId())) {
					 String content = (Integer.valueOf(10)).equals(all.getContentType()) ? all.getContent() : "";
					 map.put(all.getModuleId(), content);
				 } else {
					 map.put(all.getModuleId(), "");
				 }
			 }
		} catch (Exception e) {
			LOG.error("",e);
		}
		 
		 
		 
		 return map;
	}
	private String getSenderAvatarImageUrl(Long memberId){
		String contextPath = SystemEnvironment.getContextPath();
		try {
            String fileName = customizeManager.getCustomizeValue(memberId, "avatar");
            if(fileName!=null){
                if(fileName.startsWith("fileId")){
                    return contextPath +"/fileUpload.do?method=showRTE&"+fileName+"&type=image";
                }
                else{
                    return contextPath +"/apps_res/v3xmain/images/personal/"+fileName;
                }
            }
            
        }
        catch (Exception e) {
            LOG.error("", e);
        }
        
        return contextPath + "/apps_res/v3xmain/images/personal/pic.gif";
	}
}
