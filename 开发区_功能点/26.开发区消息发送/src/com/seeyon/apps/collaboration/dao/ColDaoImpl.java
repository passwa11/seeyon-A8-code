package com.seeyon.apps.collaboration.dao;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.seeyon.apps.agent.bo.AgentDetailModel;
import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.AgentScopeModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.collaboration.bo.QuerySummaryParam;
import com.seeyon.apps.collaboration.bo.QuerySummaryParam.SortFieldEnum;
import com.seeyon.apps.collaboration.enums.ColQueryCondition;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.collaboration.enums.ProjectQueryEnum;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.collaboration.vo.ColSummaryVO;
import com.seeyon.apps.project.api.ProjectApi;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.PerformanceConfig;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.supervise.enums.SuperviseEnum;
import com.seeyon.ctp.common.supervise.vo.SuperviseModelVO;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.track.enums.TrackEnum;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.po.OrgMember;
import com.seeyon.ctp.organization.po.OrgUnit;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.IdentifierUtil;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.dao.paginate.Pagination;

public class ColDaoImpl extends BaseHibernateDao<ColSummary> implements ColDao {
	private static final Log log = LogFactory.getLog(ColDaoImpl.class);
	private static final Integer daysOverDue = 7;//七天内超期
	private OrgManager            orgManager;
	private TemplateManager       templateManager;
	private ProjectApi projectApi;
	
	
	public ProjectApi getProjectApi() {
		return projectApi;
	}

	public void setProjectApi(ProjectApi projectApi) {
		this.projectApi = projectApi;
	}

	public OrgManager getOrgManager() {
		return orgManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

    @Override
    public void saveColSummary(ColSummary colSummary) {
        DBAgent.save(colSummary);

    }

    public TemplateManager getTemplateManager() {
        return templateManager;
    }

    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    public void saveColInfo() {
        // TODO Auto-generated method stub

    }
	@Override
	public void updateColSummary(ColSummary colSummary) {
		DBAgent.update(colSummary);
	}
	@Override
    public void updateColSummarys(List<ColSummary> colSummarys) {
        DBAgent.updateAll(colSummarys);
    }
	@Override
	public void deleteColSummary(ColSummary colSummary) {
		DBAgent.delete(colSummary);
	}


	@Override
	public ColSummary getColSummaryById(Long id) {
	  return DBAgent.get(ColSummary.class, id);
	}
	
	public ColSummary getColSummaryByIdHis(Long id) {
    return super.get(id);
  }

   public ColSummary getColSummaryByFormRecordId(Long fromRecordId){
       String hql =" from ColSummary where formRecordid = :FormRecordid ";
       Map<String, Object> parameterMap = new HashMap<String, Object>();
       parameterMap.put("FormRecordid", fromRecordId);
       List l = DBAgent.find(hql, parameterMap);
       ColSummary summary = null;
       if(Strings.isNotEmpty(l)){
           summary = (ColSummary)l.get(0);
       }
       return summary;
   }
   
   @Override
    public Map<Long, Long> getColSummaryIdByFormRecordIds(List<Long> formRecords) {
       
       if(Strings.isEmpty(formRecords)){
           return Collections.emptyMap();
       }
       
       String hql ="select id,formRecordid from ColSummary where formRecordid in (:FormRecordids) and bodyType=20";
       Map<String, Object> parameterMap = new HashMap<String, Object>();
       parameterMap.put("FormRecordids", formRecords);
       @SuppressWarnings("unchecked")
       List<Object[]> l = DBAgent.find(hql, parameterMap);
       
       Map<Long, Long> ret = null;
       
       if(Strings.isNotEmpty(l)){
           
           ret = new HashMap<Long, Long>(l.size() * 2);
           for(Object[] obj : l){
               Long id =  ((Number)obj[0]).longValue();
               Long formRecordid =  ((Number)obj[1]).longValue();
               if(!ret.containsKey(formRecordid)){
                   ret.put(formRecordid, id);
               }
           }
       }else {
           ret = Collections.emptyMap();
       }
       
       return ret;
    }
   
	@SuppressWarnings("unchecked")
	public List<ColSummaryVO> queryByCondition(FlipInfo flipInfo,Map<String, String> condition) throws BusinessException {
		//同一流程只显示最后一条
        if ("true".equals(condition.get(ColQueryCondition.deduplication.name()))) {
			return (List<ColSummaryVO>)queryDeduplication(flipInfo, condition);
		}else if(Strings.isNotBlank(condition.get(ColQueryCondition.receiver.name()))){//按接收人查询
			return (List<ColSummaryVO>)queryByReceiver(flipInfo, condition);
		}else {
			return (List<ColSummaryVO>)queryByCondition0(flipInfo, condition, false);
		}
	}
	private Object queryByReceiver(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException{
		Map<String, Object> parameterMap = new HashMap<String, Object>();
	    String userIdStr = condition.get(ColQueryCondition.currentUser.name());
	    Long userId = 0l;
	    /**表单分类查询开始**/
	    String templateName = condition.get(ColQueryCondition.templateName.name());
	    String templateCategory = condition.get(ColQueryCondition.templeteCategorys.name());
        if (Strings.isNotBlank(templateCategory)) {
            List<Long> lcategory = new ArrayList<Long>();
            List<String> lscategory = Arrays.asList(condition.get(ColQueryCondition.templeteCategorys.name()).split(","));
            for (String s : lscategory) {
            	if(s.contains("C_")){
	        		s = s.substring(2,s.length());
	        	}
                lcategory.add(Long.valueOf(s));
            }
            String hqlCategory = " select t.id from CtpTemplate t,CtpTemplateCategory ca where t.categoryId = ca.id and t.bodyType=20 and ca.id in (:categoryIds) ";
            Map<String, Object> categoryMap = new HashMap<String, Object>();
            categoryMap.put("categoryIds", lcategory);
            List<Long> categoryList = DBAgent.find(hqlCategory.toString(), categoryMap);
            StringBuilder ids = new StringBuilder();
            for (int i = 0; i < categoryList.size(); i++) {
                if (i > 0) {
                    ids.append(",");
                }
                Long objectCate = categoryList.get(i);
                ids.append(objectCate);
            }
            if (!categoryList.isEmpty()) {
                condition.put(ColQueryCondition.templeteIds.name(), ids.toString());
            }
        }else if(Strings.isNotBlank(templateName)){
        	String hqlCategory = " select t.id from CtpTemplate t where t.subject like :templateName ";
            Map<String, Object> categoryMap = new HashMap<String, Object>();
            categoryMap.put("templateName", "%" + SQLWildcardUtil.escape(templateName) + "%");
            List<Long> categoryList = DBAgent.find(hqlCategory.toString(), categoryMap);
            StringBuilder ids = new StringBuilder();
            if(Strings.isEmpty(categoryList)){
                if(categoryList.size()>1000){
                    categoryList = categoryList.subList(0, 1000);
                }
            }
            for (int i = 0; i < categoryList.size(); i++) {
                if (i > 0) {
                    ids.append(",");
                }
                Long objectCate = categoryList.get(i);
                ids.append(objectCate);
            }
            if (!categoryList.isEmpty()) {
                condition.put(ColQueryCondition.templeteIds.name(), ids.toString());
            }else {
            	condition.put(ColQueryCondition.templeteIds.name(), "-1");
            }
        }
	    /**表单分类查询结束**/
        if(Strings.isNotBlank(userIdStr)){
            userId = Long.valueOf(userIdStr);
        }
        List myState = new ArrayList();
        myState.add(StateEnum.col_pending.key());
        myState.add(StateEnum.col_done.key());
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        sb.append(selectSqlAffair);
        if(sb.indexOf(" from ")==-1){
            	sb.append(" from ctp_affair  affair,col_summary  summary,");
            	sb.append(" ( select distinct myAffair.object_Id as id  from ctp_affair  myAffair,org_member  myMember ");
				sb.append(" where myAffair.member_Id =myMember.id and myMember.name like :receiver and myAffair.state in (:myState) )  tAffair ");
            	sb.append(" where affair.object_id=summary.id and tAffair.id = affair.object_Id  ");
			parameterMap.put("receiver", "%" + SQLWildcardUtil.escape(condition.get(ColQueryCondition.receiver.name())) + "%");
			parameterMap.put("myState", myState);
            
        }
        
        //事项状态
        List<Integer> states = new ArrayList<Integer>();
        String state = condition.get(ColQueryCondition.state.name());
        if(Strings.isNotBlank(state)){
        	String[] stateStrs =state.split(",");
        	if(stateStrs != null && stateStrs.length > 0){
        	    for(int i=0;i<stateStrs.length;i++){
                    states.add(Integer.parseInt(stateStrs[i]));
                }
        	}
        }
        
        //是否需要查询代理
        boolean hasAgent = "true".equals(condition.get("hasNeedAgent"));
        
        String formRelation = condition.get(ColQueryCondition.formRelation.name());
        StringBuilder agentMemberSql = new StringBuilder();
        agentMemberSql.append(" AND (").append("affair").append(".member_Id=:memberId   ");
		parameterMap.put("memberId", userId);
		StringBuilder agentMemberIdSql = new StringBuilder();
		StringBuilder agentMemberIdHql = getMemberIdIncludeAgent(userId, "affair", parameterMap, hasAgent);
		agentMemberIdSql.append(agentMemberIdHql.toString().
				replace("affair.memberId", "affair.member_id").
				replace("affair.receiveTime", "affair.receive_time").
				replace("affair.templeteId", "affair.templete_id").
                replace("affair.proxyMemberId", "affair.PROXY_MEMBER_ID"));
		if(null != agentMemberIdSql && Strings.isNotBlank(agentMemberIdSql.toString())){//代理sql为空时不查询
			agentMemberSql.append(agentMemberIdSql);
		}
		agentMemberSql.append(")");
		StringBuilder childrenHql3 = getChildrenFilterHql(condition, parameterMap, userId, states, hasAgent, null, formRelation,"affair");
		StringBuffer childrenSql3 = new StringBuffer();
		childrenSql3.append(childrenHql3.toString().
				replace("affair.delete", "affair.is_delete").
				replace("affair.archiveId", "affair.archive_id").
				replace("affair.completeTime", "affair.complete_time"));
		agentMemberSql.append(childrenSql3);
		
    	sb.append(agentMemberSql);
    	
        /** 流程状态：未结束、已结束、已终止   0 : 未结束   1 : 已结束 2 : 已终止*/
        if (Strings.isNotBlank(condition.get(ColQueryCondition.workflowState.name()))) {
            String workflowState = condition.get(ColQueryCondition.workflowState.name());
            sb.append(" and summary.state=:summaryState ");
            if ("0".equals(workflowState)) {
                parameterMap.put("summaryState",CollaborationEnum.flowState.run.ordinal());
            } else if ("1".equals(workflowState)) {
                parameterMap.put("summaryState",CollaborationEnum.flowState.finish.ordinal());
            } else if ("2".equals(workflowState)) {
                parameterMap.put("summaryState",CollaborationEnum.flowState.terminate.ordinal());
            }
        }
        //表单授权 查询状态是正常结束和终止(即：流程结束的)的  ，或者核定通过的
        if (Strings.isNotBlank(formRelation)) {
            if ("1".equals(formRelation)) {
                sb.append(" and (summary.state=:summaryState1 or summary.vouch = 1) ");
                parameterMap.put("summaryState1",CollaborationEnum.flowState.finish.ordinal());
            }
        }
        String formAppId = condition.get(ColQueryCondition.formAppId.name());
        if(Strings.isNotBlank(formAppId)){
            sb.append(" and summary.form_appid=:formAppid ");
            parameterMap.put("formAppid",Long.parseLong(formAppId));
        }
    
        //发起者允许关联已发的协同，即使关联的协同没有设置“允许转发”
        if(condition.get(ColQueryCondition.list4Quote.name()) != null){
        	if(Strings.isNotEmpty(states) && !Integer.valueOf(StateEnum.col_sent.key()).equals(states.get(0))){
        		sb.append(" and summary.can_Forward=:canForward ");
        		parameterMap.put("canForward",Boolean.TRUE);
        	}
           /* hql.append(" and (summary.canForward=true or ((summary.canForward=null or summary.canForward =false) and affair.state = :state4Quote)) ");
            parameterMap.put("state4Quote",StateEnum.col_sent.key());*/
        }
       
        //类型：协同、表单
        String bodyType = condition.get(ColQueryCondition.bodyType.name());
        if( Strings.isNotBlank(bodyType)){
        	//穿透查询
        	if(condition.get("statistic") != null){
        		if(ColUtil.isForm(bodyType)){
        			sb.append(" and summary.body_type=:bodyType");
        		}else{
        			sb.append(" and summary.body_type!=:bodyType");
        		}
        		parameterMap.put("bodyType",String.valueOf(MainbodyType.FORM.getKey()));
        	}else if("col".equals(bodyType)){
        		sb.append(" and summary.body_type!=:bodyType");
        		parameterMap.put("bodyType",String.valueOf(MainbodyType.FORM.getKey()));
        	} else {
        		sb.append(" and summary.body_type=:bodyType");
                parameterMap.put("bodyType",bodyType);
        	}
        }
        String _templateAll = condition.get(ColQueryCondition.templeteAll.name());
        if("all".equals(_templateAll)){
          sb.append(" and summary.body_type=20");
        }
        
        if(Strings.isNotBlank(condition.get(ColQueryCondition.formRecordId.name()))){
          sb.append(" and summary.form_Recordid in(:formRecordIds) ");
          List<Long> l = new ArrayList<Long>();
          List<String> ls = Arrays.asList(condition.get(ColQueryCondition.formRecordId.name()).split(","));
          for(String s :ls){
              l.add(Long.valueOf(s));
          }
          parameterMap.put("formRecordIds",l);
        }
        String createDate = condition.get(ColQueryCondition.createDate.name());
        if (createDate != null) {
            String[] date = createDate.split("#");
            if(date != null && date.length > 0){
                if (Strings.isNotBlank(date[0])) {
                    Date stamp = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(date[0]));
                    sb.append(" and affair.create_date >= :timestampC1 ");
                    parameterMap.put("timestampC1", stamp);
                }
                if(date.length > 1){
                    if (Strings.isNotBlank(date[1])) {
                        Date stamp = Datetimes.parseDatetime(Datetimes.getLastTimeStr(date[1]));
                        sb.append(" and affair.create_date <= :timestampC2 ");
                        parameterMap.put("timestampC2", stamp);
                    }
                }
            }
        }
        //流程期限
        String deadlineDatetime=condition.get(ColQueryCondition.deadlineDatetime.name());
        if(Strings.isNotBlank(deadlineDatetime)){
        	 String[] date = deadlineDatetime.split("#");
             if(date != null && date.length > 0){
                 if (Strings.isNotBlank(date[0])) {
                     Date stamp = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(date[0]));
                     sb.append(" and summary.deadline_datetime >= :timestamp1 ");
                     parameterMap.put("timestamp1", stamp);
                 }
                 if(date.length > 1){
                     if (Strings.isNotBlank(date[1])) {
                         Date stamp = Datetimes.parseDatetime(Datetimes.getLastTimeStr(date[1]));
                         sb.append(" and summary.deadline_datetime >= :timestamp2 ");
                         parameterMap.put("timestamp2", stamp);
                     }
                 }
             }
        }
        
        if (Strings.isNotBlank(condition.get(ColQueryCondition.subject.name()))) {
            sb.append(" and summary.subject like :subject ");
            parameterMap.put("subject", "%" + SQLWildcardUtil.escape(condition.get(ColQueryCondition.subject.name())) + "%");
        }
        if (Strings.isNotBlank(condition.get(ColQueryCondition.importantLevel.name()))) {
            sb.append(" and summary.important_level=:importantLevel ");
            parameterMap.put("importantLevel",Integer.parseInt(condition.get(ColQueryCondition.importantLevel.name())));
        }
        //类型：是否超期
        String isCoverTime = condition.get(ColQueryCondition.coverTime.name());
        if(Strings.isNotBlank(isCoverTime)){
        //F8的日常工作统计中，“已归档”和“已发”的超期统计按流程超期来计算，其他应用情况按节点超期来统计
            if(condition.get("statistic") !=null
                    && (condition.get(ColQueryCondition.archiveId.name())!=null
                            ||String.valueOf(StateEnum.col_sent.getKey()).equals(condition.get(ColQueryCondition.state.name())))){
                sb.append(" and summary.cover_time=:coverTime");
                sb.append(" and affair.is_process_over_time=1");
                if("1".equals(isCoverTime)){
                    parameterMap.put("coverTime",true);
                }else{
                    parameterMap.put("coverTime",false);
                }
            }else{
                sb.append(" and affair.cover_time=:coverTime");
                if("1".equals(isCoverTime)){
                	parameterMap.put("coverTime",true);
                }else{
                	parameterMap.put("coverTime",false);
                }
            }
        }
        //已归档
        if("archived".equals(condition.get(ColQueryCondition.archiveId.name()))){//统计查询有已归档条件
        	sb.append(" and (affair.archive_id is not null or (summary.archive_id is not null and (affair.state=2 or affair.state=1))) and affair.is_delete=0 ");
        }
        
        //是否归档(针对协同列表中的归档查询，只判断affair的archiveId)
        if ("0".equals(condition.get(ColQueryCondition.affairArchiveId.name()))) {
            sb.append(" and (affair.archive_id is null and affair.is_delete=0 ) ");
        } else if ("1".equals(condition.get(ColQueryCondition.affairArchiveId.name()))) {
            sb.append(" and (affair.archive_id is not null and affair.is_delete=0 ) ");
        }
        
        //子状态是否有暂存待办条件
        
        List<Integer> subStateIds1 = new ArrayList<Integer>();
       
        subStateIds1.add(SubStateEnum.col_pending_specialBack.getKey());
        subStateIds1.add(SubStateEnum.col_pending_specialBackCenter.getKey());
        
        subStateIds1.add(SubStateEnum.col_pending_specialBackToSenderCancel.getKey());
        subStateIds1.add(SubStateEnum.col_pending_specialBackToSenderReGo.getKey());
 	    
        List<Integer> subStateIds2 = new ArrayList<Integer>();
        subStateIds2.add(SubStateEnum.col_pending_specialBacked.getKey());
 	    
        String subSate = condition.get(ColQueryCondition.subState.name());
        boolean isWaitSend = String.valueOf(StateEnum.col_waitSend.getKey()).equals(state);
        
        /**
         * 待办查询规则如下：
         * 被回退：普通回退  && 指定回退流程冲走  && 指定回退提交回退者的被回退方
         * 指定回退：指定回退——提交回退者的主动方
         */
        
        if(Strings.isNotBlank(subSate)){
        	if(String.valueOf(SubStateEnum.col_pending_unRead.getKey()).equals(subSate) 
        			||String.valueOf(SubStateEnum.col_pending_ZCDB.getKey()).equals(subSate) 
        			||String.valueOf(SubStateEnum.col_waitSend_cancel.getKey()).equals(subSate) 
        			||String.valueOf(SubStateEnum.col_waitSend_draft.getKey()).equals(subSate) ){
        		    
        		//未读 && 暂存待办  && 撤销 && 草稿
        		  sb.append(" and (affair.sub_State = :queryState )");
        		  parameterMap.put("queryState", Integer.valueOf(subSate));
        		  
        	}else if(String.valueOf(SubStateEnum.col_pending_read.getKey()).equals(subSate) ){
        		
        		//已读
        		sb.append(" and (affair.sub_State != :queryState )");
      		  	parameterMap.put("queryState", SubStateEnum.col_pending_unRead.getKey());
      		  	
        	}else if(isWaitSend && String.valueOf(SubStateEnum.col_waitSend_stepBack.getKey()).equals(subSate) ){
        	   
        	   sb.append(" and (affair.sub_State in (:subStateIds))");
        	   subStateIds1.add(SubStateEnum.col_waitSend_stepBack.getKey());
        	   subStateIds1.add(SubStateEnum.col_pending_specialBacked.getKey());
           	   parameterMap.put("subStateIds", subStateIds1);
           	   
        	}else if(String.valueOf(SubStateEnum.col_waitSend_stepBack.getKey()).equals(subSate) ){
        	   //指定回退不允许暂存待办   普通回退经过暂存待办的  查询被回退的时候也应该过滤。
        	   subStateIds1.add(SubStateEnum.col_pending_ZCDB.getKey());
         	   //普通回退
         	   sb.append(" and (affair.sub_State not in (:subStateIds) and affair.back_From_Id is not null )");
         	   parameterMap.put("subStateIds", subStateIds1);
         	  
        	}else if(String.valueOf(SubStateEnum.col_pending_specialBack.getKey()).equals(subSate) ){
        		
          	   //指定回退
          	    sb.append(" and (affair.sub_State in (:subStateIds))");
          	   parameterMap.put("subStateIds", subStateIds1);
        	}
        }
        //协同穿透条件 end
        
        //是否是模版协同
        String colType = condition.get(ColQueryCondition.CollType.name());
        if(colType != null){
            if("Templete".equals(colType)){ //模版协同
                sb.append(" and (summary.templete_Id is not null) ");
            }
            else if("Self".equals(colType)){ //自由协同
                sb.append(" and (summary.templete_Id is null) ");
            }
        }
        String templeteIds = condition.get(ColQueryCondition.templeteIds.name());
        if(Strings.isNotBlank(templeteIds)){
            sb.append(" and summary.templete_Id in(:templeteIds) ");
            List<Long> l = new ArrayList<Long>();
            List<String> ls = Arrays.asList(condition.get(ColQueryCondition.templeteIds.name()).split(","));
            for(String s :ls){
                l.add(Long.valueOf(s));
            }
            parameterMap.put("templeteIds",l);
        }
        
        if ("true".equals(condition.get(ColQueryCondition.affairFavorite.name()))) {
            sb.append(" and affair.delete =:affairDelete and affair.has_Favorite=:hasFavorite ");
            parameterMap.put("affairDelete", Boolean.FALSE);
            parameterMap.put("hasFavorite", Boolean.TRUE);
        }
        if(Strings.isNotBlank(condition.get("objectId"))){
            String objectId = condition.get("objectId");
            sb.append(" and ").append(" affair.object_Id =:objectId");
            parameterMap.put("objectId",Long.parseLong(objectId));
         }
      
        //按节点权限查询
        if(condition.get(ColQueryCondition.nodePolicy.name())!=null){
       	 	sb.append(" and affair.node_Policy = :nodePolicy");
            parameterMap.put("nodePolicy",condition.get(ColQueryCondition.nodePolicy.name()));
       }
        if(condition.get(ColQueryCondition.removeSummary.name())!=null){
        	sb.append(" and affair.object_Id not in(:summaryId)");
        	List<Long> summaryIds = new ArrayList<Long>();
            List<String> summaryIdString = Arrays.asList(condition.get(ColQueryCondition.removeSummary.name()).split(","));
            for(String s :summaryIdString){
            	summaryIds.add(Long.valueOf(s));
            }
            parameterMap.put("summaryId",summaryIds);
        }
        if(condition.get(ColQueryCondition.transactorId.name())!=null){
        	sb.append(" and affair.transactor_Id in(:transactorId)");
        	List<Long> transactorIds = new ArrayList<Long>();
            List<String> transactorIdString = Arrays.asList(condition.get(ColQueryCondition.transactorId.name()).split(","));
            for(String s :transactorIdString){
            	transactorIds.add(Long.valueOf(s));
            }
            parameterMap.put("transactorId",transactorIds);
        }
        
        //是否为智能处理
        if("true".equals(condition.get("aiProcessing"))) {
        	sb.append(" and affair.ai_processing =:aiProcessing");
        	parameterMap.put("aiProcessing", Boolean.TRUE);
        }
        
        if(!"true".equals(condition.get("isColQuoteCount"))) {
            if(states.contains(StateEnum.col_pending.key())){
            	if("true".equals(condition.get(ColQueryCondition.aiSort.name()))) {
            		sb.append(" order by affair.sort_weight,affair.receive_Time desc");
            	}else {
            		sb.append(" order by affair.receive_Time desc");
            	}
            }else if(states.contains(StateEnum.col_done.key())){
                sb.append(" order by affair.complete_Time desc");
            }else{
                sb.append(" order by affair.create_Date desc");
            }
        }
        if ("true".equals(condition.get("isColQuoteCount"))) {
        	sb.append(" and affair.state in(:quoteState)");
        	parameterMap.put("quoteState", states);
        	sb.append(" group by affair.state ");
        }
        JDBCAgent jdbc = new JDBCAgent();
        try {
            flipInfo  = jdbc.findNameByPaging(sb.toString(),parameterMap, flipInfo);
		} catch (Exception e) {
			logger.error("接收人查询错误！",e);
		} finally {
			jdbc.close();
		}
        List<Map> l = flipInfo.getData();
        
        List<Object[]> querySet = new ArrayList<Object[]>();
        
    	List<String> booleanList = new ArrayList<String>();
    	booleanList.add("can_archive");
    	booleanList.add("is_cover_time");
    	booleanList.add("can_edit");
    	booleanList.add("can_forward");
    	booleanList.add("affaircovertime");
    	booleanList.add("has_favorite");
        for (Map<String,Object> map : l) {
        	Object[] obj = new Object[map.size()];
        	Iterator ite = map.keySet().iterator();
        	int j = 0;
        	while (ite.hasNext()) {
            	String key = (String) ite.next();
            	Object a =map.get(key);
            	if(a != null && booleanList.contains(key) && Strings.isNotBlank(String.valueOf(a))) {
            		a = Integer.valueOf(String.valueOf(a)) == 0 ? false : true;
            	}
            	obj[j] = a;
            	j++;
            }
        	querySet.add(obj);
        }
        
        return convertSelectAffair2ColSummaryModel(querySet, hasAgent,null,true,null);
        
    
	}

	public List<ColSummaryVO> queryByConditionHis(FlipInfo flipInfo,Map<String, String> condition) throws BusinessException {
		return (List<ColSummaryVO>)queryByCondition0His(flipInfo, condition, false);
	}
	
	public int countByCondition(Map<String, String> condition) throws BusinessException {
        return (Integer)queryByCondition0(null, condition, true);
    }
	/**
	 * 查询关联文档已发、已办、待办的总数
	 * @param condition
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public Map<String,Integer> getColQuoteCount(Map<String, String> condition) throws BusinessException {
		//是否是总数查询
		condition.put("isColQuoteCount", "true");
		//是否是关联文档
		condition.put(ColQueryCondition.list4Quote.name(), String.valueOf(Boolean.TRUE));
		List result = (List)queryByCondition0(null, condition, false);
		Map<String,Integer> map = new HashMap<String,Integer>();
		for (int i=0;i<result.size();i++) {
			Object[] object = (Object[]) result.get(i);
			map.put(String.valueOf(object[0]),Integer.valueOf(String.valueOf(object[1])));
		}
		return map;
	}
	
	//同一个流程只显示一条记录
	private Object queryDeduplication(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		
		Long userId = Long.valueOf(condition.get(ColQueryCondition.currentUser.name()));
		
		//事项状态
        List<Integer> states = new ArrayList<Integer>();
        String state = condition.get(ColQueryCondition.state.name());
        if(Strings.isNotBlank(state)){
        	String[] stateStrs =state.split(",");
        	if(stateStrs != null && stateStrs.length > 0){
        	    for(int i=0;i<stateStrs.length;i++){
                    states.add(Integer.parseInt(stateStrs[i]));
                }
        	}
        }
		
		StringBuilder sb = new StringBuilder("select ");
		sb.append(selectSqlAffair);
		sb.append(" from ctp_affair affair,col_summary summary,");
		sb.append(" (select max(affair2.id) as id from ctp_affair affair2 where 1=1 ");
		//中间查询条件
		StringBuilder agentMemberSql = new StringBuilder();
		agentMemberSql.append(" and (").append("affair2").append(".member_id=:memberId   ");
		//查询条件组装
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("memberId", userId);
		
		//是否需要查询代理
        boolean hasAgent = "true".equals(condition.get("hasNeedAgent"));
        
        StringBuffer agentMemberIdSql = new StringBuffer();
		if (hasAgent) {

	        StringBuilder agentMemberIdHql = new StringBuilder();
			agentMemberIdHql = getMemberIdIncludeAgent(userId, "affair2", parameterMap, hasAgent);
			//转化agentMemberIdHql 为sql: .memberId  .receiveTime .templeteId
			agentMemberIdSql.append(agentMemberIdHql.toString().
					replace("affair2.memberId", "affair2.member_id").
					replace("affair2.receiveTime", "affair2.receive_time").
					replace("affair2.templeteId", "affair2.templete_id").
                    replace("affair2.proxyMemberId", "affair2.PROXY_MEMBER_ID"));

		}
		if(null != agentMemberIdSql && Strings.isNotBlank(agentMemberIdSql.toString())){//代理sql为空时不查询
			agentMemberSql.append(agentMemberIdSql);
		}
		agentMemberSql.append(")");
		
		
		String formRelation = condition.get(ColQueryCondition.formRelation.name());
		StringBuilder childrenHql2 = getChildrenFilterHql(condition, parameterMap, userId, states, hasAgent, null, formRelation,"affair2");
		//转化childrenHql2 为sql: delete archiveId completeTime
		StringBuffer childrenSql2 = new StringBuffer();
		childrenSql2.append(childrenHql2.toString().
				replace("affair2.delete", "affair2.is_delete").
				replace("affair2.archiveId", "affair2.archive_id").
				replace("affair2.completeTime", "affair2.complete_time"));
		
		agentMemberSql.append(childrenSql2);
		
		sb.append(agentMemberSql);
		
		//是否为智能处理
        if("true".equals(condition.get("aiProcessing"))) {
        	sb.append(" and affair2.ai_Processing =:aiProcessing");
        	parameterMap.put("aiProcessing", Boolean.TRUE);
        }
        
		sb.append(" GROUP BY affair2.object_id  ");
		
		
		
		sb.append(" ) taffair ");
		if( Strings.isEmpty(condition.get(ColQueryCondition.startMemberName.name()))  && Strings.isEmpty(condition.get(ColQueryCondition.preApproverName.name()))){
			sb.append(" where  affair.object_id=summary.id and taffair.id=affair.id");
        }else{
        	if( Strings.isNotEmpty(condition.get(ColQueryCondition.startMemberName.name()))){
        		sb.append(" ,org_member mem");
            	sb.append(" where affair.object_id=summary.id and taffair.id=affair.id and affair.sender_id=mem.id ");
        	}else if(Strings.isNotEmpty(condition.get(ColQueryCondition.preApproverName.name()))){
        		sb.append(" ,org_member mem2");
            	sb.append(" where affair.object_id=summary.id and taffair.id=affair.id and affair.pre_Approver=mem2.id ");
        	}
        	
        }
		
		String templeteIds = condition.get(ColQueryCondition.templeteIds.name());
		String templateName = condition.get(ColQueryCondition.templateName.name());
        if(Strings.isNotBlank(templeteIds)){
            sb.append(" and summary.templete_id in(:templeteIds) ");
            List<Long> l = new ArrayList<Long>();
            List<String> ls = Arrays.asList(condition.get(ColQueryCondition.templeteIds.name()).split(","));
            for(String s :ls){
                l.add(Long.valueOf(s));
            }
            parameterMap.put("templeteIds",l);
        }else if(Strings.isNotBlank(templateName)){
            	String hqlCategory = " select t.id from CtpTemplate t where t.subject like :templateName ";
                Map<String, Object> templateNameParams = new HashMap<String, Object>();
                templateNameParams.put("templateName", "%" + SQLWildcardUtil.escape(templateName) + "%");
                List<Long> tempalateIdsList = DBAgent.find(hqlCategory.toString(), templateNameParams);
           if(Strings.isNotEmpty(tempalateIdsList)){
        	   sb.append(" and summary.templete_id in(:templeteIds) ");
        	   parameterMap.put("templeteIds",tempalateIdsList);
           }else{//如果查出来模板id是空，结果肯定为空，将参数设为-1
        	   sb.append(" and summary.templete_id in(:templeteIds) ");
        	   List<Long> l = new ArrayList<Long>();
        	   l.add(-1L);
        	   parameterMap.put("templeteIds",l);
           }
        }
		
		/** 流程状态：未结束、已结束、已终止   0 : 未结束   1 : 已结束 2 : 已终止*/
        if (Strings.isNotBlank(condition.get(ColQueryCondition.workflowState.name()))) {
            String workflowState = condition.get(ColQueryCondition.workflowState.name());
            sb.append(" and summary.state=:summaryState ");
            if ("0".equals(workflowState)) {
                parameterMap.put("summaryState",CollaborationEnum.flowState.run.ordinal());
            } else if ("1".equals(workflowState)) {
                parameterMap.put("summaryState",CollaborationEnum.flowState.finish.ordinal());
            } else if ("2".equals(workflowState)) {
                parameterMap.put("summaryState",CollaborationEnum.flowState.terminate.ordinal());
            }
        }
        if (Strings.isNotBlank(condition.get(ColQueryCondition.subject.name()))) {
            sb.append(" and summary.subject like :subject ");
            parameterMap.put("subject", "%" + SQLWildcardUtil.escape(condition.get(ColQueryCondition.subject.name())) + "%");
        }
        if (Strings.isNotBlank(condition.get(ColQueryCondition.importantLevel.name()))) {
            sb.append(" and summary.important_level=:importantLevel");
            parameterMap.put("importantLevel",Integer.parseInt(condition.get(ColQueryCondition.importantLevel.name())));
        }
		
        StringBuilder completeTimeQueryHql = getCompleteTimeQueryHql("affair",condition, parameterMap);
        sb.append(completeTimeQueryHql.toString().replace("affair.completeTime", "affair.complete_time"));
        
        
        //待发,已发协同按创建日期查询,待办按接收时间查询,已办按完成时间查询
        String createDate = condition.get(ColQueryCondition.createDate.name());
        if (createDate != null) {
            String[] date = createDate.split("#");
            if(date != null && date.length > 0){
                if (Strings.isNotBlank(date[0])) {
                    Date stamp = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(date[0]));
                    sb.append(" and affair.create_date >=:timestamp1");
                    parameterMap.put("timestamp1", stamp);
                }
                if(date.length > 1){
                    if (Strings.isNotBlank(date[1])) {
                        Date stamp = Datetimes.parseDatetime(Datetimes.getLastTimeStr(date[1]));
                        sb.append(" and affair.create_date <=:timestamp2");
                        parameterMap.put("timestamp2", stamp);
                    }
                }
            }
        }
        
        if(Strings.isNotEmpty(condition.get(ColQueryCondition.startMemberName.name())) ){
            sb.append(" and mem.name like :startMemberName");
            parameterMap.put("startMemberName", "%" + SQLWildcardUtil.escape(condition.get(ColQueryCondition.startMemberName.name())) + "%");
        }
        
        if(Strings.isNotEmpty(condition.get(ColQueryCondition.preApproverName.name()))){
            sb.append(" and mem2.name like :preApproverName");
            parameterMap.put("preApproverName", "%" + SQLWildcardUtil.escape(condition.get(ColQueryCondition.preApproverName.name())) + "%");
        }
        
        if (Strings.isNotBlank(condition.get(ColQueryCondition.isOverdue.name()))) {
        	if("1".equals(condition.get(ColQueryCondition.isOverdue.name()))){
        		//已发查询流程超期
        		if (String.valueOf(StateEnum.col_sent.getKey()).equals(state)) {
        			sb.append(" and (summary.is_cover_time=1 and affair.is_process_over_time=1) ");
        		} else { //待办、已办查询节点超期
        			sb.append(" and affair.is_cover_time=1 ");
        		}
            }else{
            	//已发查询流程超期
        		if (String.valueOf(StateEnum.col_sent.getKey()).equals(state)) {
        			sb.append(" and summary.is_cover_time=0 ");
        		} else { //待办、已办查询节点超期
        			sb.append(" and affair.is_cover_time=0 ");
        		}
            }
        }
        //是否归档(针对协同列表中的归档查询，只判断affair的archiveId)
        if ("0".equals(condition.get(ColQueryCondition.affairArchiveId.name()))) {
        	sb.append(" and (affair.archive_Id is null and affair.is_delete=0 ) ");
        } else if ("1".equals(condition.get(ColQueryCondition.affairArchiveId.name()))) {
        	sb.append(" and (affair.archive_Id is not null and affair.is_delete=0 ) ");
        }
        
        //扫一扫
        if(Strings.isNotBlank(condition.get("objectId"))){
            String objectId = condition.get("objectId");
            sb.append(" and ").append(" affair.object_id =:objectId");
            parameterMap.put("objectId",Long.parseLong(objectId));
        }
        
        sb.append(" order by affair.complete_Time desc");
        
        JDBCAgent jdbc = new JDBCAgent();
        try {
            flipInfo  = jdbc.findNameByPaging(sb.toString(),parameterMap, flipInfo);
		} catch (Exception e) {
			logger.error("合并同一流程查询错误！",e);
		} finally {
			jdbc.close();
		}
        List<Map> l = flipInfo.getData();
        
        List<Object[]> querySet = new ArrayList<Object[]>();
        
    	List<String> booleanList = new ArrayList<String>();
    	booleanList.add("can_archive");
    	booleanList.add("is_cover_time");
    	booleanList.add("can_edit");
    	booleanList.add("can_forward");
    	booleanList.add("affaircovertime");
    	booleanList.add("has_favorite");
        for (Map<String,Object> map : l) {
        	Object[] obj = new Object[map.size()];
        	Iterator ite = map.keySet().iterator();
        	int j = 0;
        	while (ite.hasNext()) {
            	String key = (String) ite.next();
            	Object a =map.get(key);
            	if(a != null && booleanList.contains(key) && Strings.isNotBlank(String.valueOf(a))) {
            		a = Integer.valueOf(String.valueOf(a)) == 0 ? false : true;
            	}
            	obj[j] = a;
            	j++;
            }
        	querySet.add(obj);
        }
        
        return convertSelectAffair2ColSummaryModel(querySet, hasAgent,null,true,null);
	}
	
	private Object queryByCondition0(FlipInfo flipInfo, Map<String, String> condition, boolean isCounter) throws BusinessException {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		StringBuilder hql = new StringBuilder();
        //是否需要查询代理,我是代理人
        boolean hasAgent = false;
        User user = AppContext.getCurrentUser();
		List<AgentModel> modelList = MemberAgentBean.getInstance().getAgentModelList(user.getId());
		if(modelList != null && modelList.size() >0){
			hasAgent = true;
		}
		//待办不查询
		if(condition.containsKey("hasNeedAgent") && "false".equals(condition.get("hasNeedAgent"))){
			hasAgent = false;
		}
        //协同列表优化，是否需要关联summary控制参数
        boolean needSummary = false;

        if(condition.get("needSummary") != null &&  "1".equals(condition.get("needSummary"))){
            needSummary = true;
        }
		queryByCondition0_condition(hql, condition, parameterMap, isCounter, hasAgent ,needSummary);
        //事项状态
        String stateStr = condition.get(ColQueryCondition.state.name());

        if(isCounter) {
            return super.count(hql.toString(), parameterMap);
        } else {//计数   
        	String isShowTotal = PerformanceConfig.getInstance().getConfig("col.grid.total");
        	if("0".equals(isShowTotal) && null != flipInfo) {
        		flipInfo.setNeedTotal(false);
        	}
            List l = DBAgent.find(hql.toString(), parameterMap, flipInfo);
            if("true".equals(condition.get("isColQuoteCount"))){
                return l;
            }else{    
                if(hasAgent == false ){
                    //如果我是被代理人也需要转换代理事项
                    List<AgentModel> myAgentModel = MemberAgentBean.getInstance().getAgentModelToList(user.getId());
                    if(myAgentModel!= null && myAgentModel.size() >0){
                        hasAgent = true;
                    }
                }
                //待办不查询
                if(condition.containsKey("hasNeedAgent") && "false".equals(condition.get("hasNeedAgent"))){
                    hasAgent = false;
                }
                return convertSelectAffair2ColSummaryModel(l, hasAgent,null,needSummary,stateStr);
            }            
        }
    }
	
	private Object queryByCondition0His(FlipInfo flipInfo, Map<String, String> condition, boolean isCounter) throws BusinessException {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		StringBuilder hql = new StringBuilder();
        //是否需要查询代理
        boolean hasAgent = "true".equals(condition.get("hasNeedAgent"));
        
		queryByCondition0_condition(hql, condition, parameterMap, isCounter, hasAgent ,true);

		//设置分页信息
		Pagination.setNeedCount(true); 
        Pagination.setFirstResult(flipInfo.getStartAt());
        Pagination.setMaxResults(flipInfo.getSize());
		List l = super.find(hql.toString(), parameterMap);
		flipInfo.setTotal(Pagination.getRowCount(false));
		if(flipInfo.getPage() > flipInfo.getPages().intValue()){
			flipInfo.setPage(flipInfo.getPages());
	        Pagination.setFirstResult(flipInfo.getStartAt());
			l = super.find(hql.toString(), parameterMap);
		}

        if(isCounter) { //计数
            return (l == null || l.isEmpty()) ? 0 : ((Number)l.get(0)).intValue();
        }else if("true".equals(condition.get("isColQuoteCount"))){
        	return l;
        }else{
            return convertSelectAffair2ColSummaryModel(l, hasAgent,null,true,null);
        }
    }
	
	private void queryByCondition0_condition(StringBuilder hql, Map<String, String> condition, Map<String, Object> parameterMap, boolean isCounter, boolean hasAgent, boolean needSummary){
	    String userIdStr = condition.get(ColQueryCondition.currentUser.name());
	    Long userId = 0l;

	    /**表单分类查询开始**/
	    String templateName = condition.get(ColQueryCondition.templateName.name());
	    String _templateCategory = condition.get(ColQueryCondition.templeteCategorys.name());
        if (Strings.isNotBlank(_templateCategory)) {
            List<Long> lcategory = new ArrayList<Long>();
            List<String> lscategory = Arrays.asList(condition.get(ColQueryCondition.templeteCategorys.name()).split(","));
            for (String s : lscategory) {
            	if(s.contains("C_")){
	        		s = s.substring(2,s.length());
	        	}
                lcategory.add(Long.valueOf(s));
            }
            String hqlCategory = " select t.id from CtpTemplate t,CtpTemplateCategory ca where t.categoryId = ca.id and t.bodyType=20 and ca.id in (:categoryIds) ";
            Map<String, Object> categoryMap = new HashMap<String, Object>();
            categoryMap.put("categoryIds", lcategory);
            List<Long> categoryList = DBAgent.find(hqlCategory.toString(), categoryMap);
            StringBuilder ids = new StringBuilder();
            for (int i = 0; i < categoryList.size(); i++) {
                if (i > 0) {
                    ids.append(",");
                }
                Long objectCate = categoryList.get(i);
                ids.append(objectCate);
            }
            if (!categoryList.isEmpty()) {
                condition.put(ColQueryCondition.templeteIds.name(), ids.toString());
            }
        }else if(Strings.isNotBlank(templateName)){
        	String hqlCategory = " select t.id from CtpTemplate t where t.subject like :templateName ";
            Map<String, Object> templateNameParams = new HashMap<String, Object>();
            templateNameParams.put("templateName", "%" + SQLWildcardUtil.escape(templateName) + "%");
            List<Long> templateIdsList = DBAgent.find(hqlCategory.toString(), templateNameParams);
            StringBuilder ids = new StringBuilder();
            String tIds =  condition.get(ColQueryCondition.templeteIds.name());
            if(Strings.isEmpty(templateIdsList)){
                if(templateIdsList.size()>1000){
                    templateIdsList = templateIdsList.subList(0, 1000);
                }
            }
            for (int i = 0; i < templateIdsList.size(); i++) {
                
                Long objectCate = templateIdsList.get(i);
                if(Strings.isNotBlank(tIds) &&  tIds.indexOf(objectCate.toString()) > -1){
                	if (Strings.isNotBlank(ids.toString())) {
                        ids.append(",");
                    }
                	ids.append(objectCate);
                }else if(Strings.isBlank(tIds)){
                	if (Strings.isNotBlank(ids.toString())) {
                        ids.append(",");
                    }
                	ids.append(objectCate);
                }
            }
            if (Strings.isNotBlank(ids.toString())) {
                condition.put(ColQueryCondition.templeteIds.name(), ids.toString());
            }else {
            	condition.put(ColQueryCondition.templeteIds.name(), "-1");
            }
        }
	    /**表单分类查询结束**/
	    
        if(Strings.isNotBlank(userIdStr)){
            userId = Long.valueOf(userIdStr);
        }
        
        //事项状态
        List<Integer> states = new ArrayList<Integer>();
        String state = condition.get(ColQueryCondition.state.name());
        if(Strings.isNotBlank(state)){
            String[] stateStrs =state.split(",");
            if(stateStrs != null && stateStrs.length > 0){
                for(int i=0;i<stateStrs.length;i++){
                    states.add(Integer.parseInt(stateStrs[i]));
                }
            }
        }
        StringBuilder endHql = new StringBuilder();
        /** 流程状态：未结束、已结束、已终止   0 : 未结束   1 : 已结束 2 : 已终止*/
        if (Strings.isNotBlank(condition.get(ColQueryCondition.workflowState.name()))) {
            String workflowState = condition.get(ColQueryCondition.workflowState.name());
            endHql.append(" and affair.summaryState=:summaryState ");
            if ("0".equals(workflowState)) {
                parameterMap.put("summaryState",CollaborationEnum.flowState.run.ordinal());
            } else if ("1".equals(workflowState)) {
                parameterMap.put("summaryState",CollaborationEnum.flowState.finish.ordinal());
            } else if ("2".equals(workflowState)) {
                parameterMap.put("summaryState",CollaborationEnum.flowState.terminate.ordinal());
            }
        }

        String formAppId = condition.get(ColQueryCondition.formAppId.name());
        if(Strings.isNotBlank(formAppId)){
            endHql.append(" and affair.formAppId=:formAppid ");
            parameterMap.put("formAppid",Long.parseLong(formAppId));
        }
        String deadlineDatetime=condition.get(ColQueryCondition.deadlineDatetime.name());//流程期限

    	if (deadlineDatetime != null) {
            String[] date = deadlineDatetime.split("#");
            if(date != null && date.length > 0){
                if (Strings.isNotBlank(date[0])) {
                    Date stamp = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(date[0]));
                    endHql.append(" and affair.processDeadlineTime >= :timestamp1");
                    parameterMap.put("timestamp1", stamp);
                }
                if(date.length > 1){
                    if (Strings.isNotBlank(date[1])) {
                        Date stamp = Datetimes.parseDatetime(Datetimes.getLastTimeStr(date[1]));
                        endHql.append(" and affair.processDeadlineTime <= :timestamp2");
                        parameterMap.put("timestamp2", stamp);
                    }
                }
            }
        }

        //7天内超期
        String sevenDayOverdue = condition.get(ColQueryCondition.sevenDayOverdue.name());
        if (Strings.isNotBlank(sevenDayOverdue)) {
            Date sevenDatDate = DateUtil.newDate();
            parameterMap.put("timestampE1", sevenDatDate);
            Calendar c = Calendar.getInstance();
            c.setTime(sevenDatDate);
            c.add(Calendar.DAY_OF_MONTH, 7);
            sevenDatDate = c.getTime();
            endHql.append(
                    " and affair.expectedProcessTime is not null and affair.expectedProcessTime>:timestampE1 and affair.expectedProcessTime<= :timestampE2 ");
            parameterMap.put("timestampE2", sevenDatDate);
        }
        
        //liuchen期限
        String expectprocesstime=condition.get(ColQueryCondition.expectprocesstime.name());
        if(Strings.isNotBlank(expectprocesstime)){
        	 String[] date = expectprocesstime.split("#");
             if(date != null && date.length > 0){
                 if (Strings.isNotBlank(date[0])) {
                     Date stamp = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(date[0]));
                     endHql.append(" and affair.expectedProcessTime >= :timestampE1 and affair.expectedProcessTime is not null");
                     parameterMap.put("timestampE1", stamp);
                 }
                 if(date.length > 1){
                     if (Strings.isNotBlank(date[1])) {
                         Date stamp = Datetimes.parseDatetime(Datetimes.getLastTimeStr(date[1]));
                         endHql.append(" and affair.expectedProcessTime<= :timestampE2 and affair.expectedProcessTime is not null");
                         parameterMap.put("timestampE2", stamp);
                     }
                 }
             }
        }
        
        if (Strings.isNotBlank(condition.get(ColQueryCondition.subject.name()))) {
            endHql.append(" and affair.subject like :subject ").append(SQLWildcardUtil.setEscapeCharacter());
            parameterMap.put("subject", "%" + SQLWildcardUtil.escape(condition.get(ColQueryCondition.subject.name())) + "%");
        }
        if (Strings.isNotBlank(condition.get(ColQueryCondition.importantLevel.name()))) {
            endHql.append(" and affair.importantLevel=:importantLevel");
            parameterMap.put("importantLevel",Integer.parseInt(condition.get(ColQueryCondition.importantLevel.name())));
        }

        //类型：协同、表单
        String bodyType = condition.get(ColQueryCondition.bodyType.name());
        if( Strings.isNotBlank(bodyType)){
        	//穿透查询
        	if(condition.get("statistic") != null){
        		if(ColUtil.isForm(bodyType)){
        			endHql.append(" and affair.bodyType=:bodyType");
        		}else{
        			endHql.append(" and affair.bodyType!=:bodyType");
        		}
        		parameterMap.put("bodyType",String.valueOf(MainbodyType.FORM.getKey()));
        	}else if("col".equals(bodyType)){
        		endHql.append(" and affair.bodyType!=:bodyType");
        		parameterMap.put("bodyType",String.valueOf(MainbodyType.FORM.getKey()));
        	} else {
        		endHql.append(" and affair.bodyType=:bodyType");
                parameterMap.put("bodyType",bodyType);
        	}
        }
        String _templateAll = condition.get(ColQueryCondition.templeteAll.name());
        if("all".equals(_templateAll)){
          endHql.append(" and affair.bodyType=20");
        }
        
        if(Strings.isNotBlank(condition.get(ColQueryCondition.formRecordId.name()))){
          endHql.append(" and affair.formRecordid in(:formRecordIds) ");
          List<Long> l = new ArrayList<Long>();
          List<String> ls = Arrays.asList(condition.get(ColQueryCondition.formRecordId.name()).split(","));
          for(String s :ls){
              l.add(Long.valueOf(s));
          }
          parameterMap.put("formRecordIds",l);
        }

        //是否归档(针对协同列表中的归档查询，只判断affair的archiveId)
        if ("0".equals(condition.get(ColQueryCondition.affairArchiveId.name()))) {
            endHql.append(" and (affair.archiveId is null and affair.delete=false ) ");
        } else if ("1".equals(condition.get(ColQueryCondition.affairArchiveId.name()))) {
            endHql.append(" and (affair.archiveId is not null and affair.delete=false ) ");
        }
        
        //子状态是否有暂存待办条件
        
        List<Integer> subStateIds1 = new ArrayList<Integer>();
       
        subStateIds1.add(SubStateEnum.col_pending_specialBack.getKey());
        subStateIds1.add(SubStateEnum.col_pending_specialBackCenter.getKey());
        
        subStateIds1.add(SubStateEnum.col_pending_specialBackToSenderCancel.getKey());
        subStateIds1.add(SubStateEnum.col_pending_specialBackToSenderReGo.getKey());
 	    
        List<Integer> subStateIds2 = new ArrayList<Integer>();
        subStateIds2.add(SubStateEnum.col_pending_specialBacked.getKey());
 	    
        String subSate = condition.get(ColQueryCondition.subState.name());
        boolean isWaitSend = String.valueOf(StateEnum.col_waitSend.getKey()).equals(state);
        
        /**
         * 待办查询规则如下：
         * 被回退：普通回退  && 指定回退流程冲走  && 指定回退提交回退者的被回退方
         * 指定回退：指定回退——提交回退者的主动方
         */
        
        if(Strings.isNotBlank(subSate)){
        	if(String.valueOf(SubStateEnum.col_pending_unRead.getKey()).equals(subSate) 
        			||String.valueOf(SubStateEnum.col_pending_ZCDB.getKey()).equals(subSate) 
        			||String.valueOf(SubStateEnum.col_waitSend_cancel.getKey()).equals(subSate) 
        			||String.valueOf(SubStateEnum.col_waitSend_draft.getKey()).equals(subSate) ){
        		    
        		//未读 && 暂存待办  && 撤销 && 草稿
        		  endHql.append(" and (affair.subState = :queryState )");
        		  parameterMap.put("queryState", Integer.valueOf(subSate));
        		  
        	}else if(String.valueOf(SubStateEnum.col_pending_read.getKey()).equals(subSate) ){
        		
        		//已读
        		endHql.append(" and (affair.subState != :queryState )");
      		  	parameterMap.put("queryState", SubStateEnum.col_pending_unRead.getKey());
      		  	
        	}else if(isWaitSend && String.valueOf(SubStateEnum.col_waitSend_stepBack.getKey()).equals(subSate) ){
        	   
        	   endHql.append(" and (affair.subState in (:subStateIds))");
        	   subStateIds1.add(SubStateEnum.col_waitSend_stepBack.getKey());
        	   subStateIds1.add(SubStateEnum.col_pending_specialBacked.getKey());
           	   parameterMap.put("subStateIds", subStateIds1);
           	   
        	}else if(String.valueOf(SubStateEnum.col_waitSend_stepBack.getKey()).equals(subSate) ){
        	   //指定回退不允许暂存待办   普通回退经过暂存待办的  查询被回退的时候也应该过滤。
//        	   subStateIds1.add(SubStateEnum.col_pending_ZCDB.getKey());
//         	   //普通回退
//         	   endHql.append(" and (affair.subState not in (:subStateIds) and affair.backFromId is not null )");
//         	   parameterMap.put("subStateIds", subStateIds1);
         	  endHql.append(" and (affair.subState = :queryState )");
    		  parameterMap.put("queryState", Integer.valueOf(subSate));
         	  
        	}else if(String.valueOf(SubStateEnum.col_pending_specialBack.getKey()).equals(subSate)){
          	   //指定回退
          	    endHql.append(" and (affair.subState in (:subStateIds))");
          	    parameterMap.put("subStateIds", subStateIds1);
        	} else if (String.valueOf(SubStateEnum.col_pending_Back.getKey()).equals(subSate)) {
        	    List<Integer> subStateIds3 = new ArrayList<Integer>();
        	    subStateIds3.add(SubStateEnum.col_pending_Back.getKey());
        	    subStateIds3.add(SubStateEnum.col_pending_specialBacked.getKey());
        	    subStateIds3.add(SubStateEnum.col_pending_unRead.getKey());
                subStateIds3.add(SubStateEnum.col_pending_read.getKey());
        	    endHql.append(" and (affair.subState in (:subStateIds)) and (affair.backFromId is not null ) ");
                parameterMap.put("subStateIds", subStateIds3);
        	}else if(String.valueOf(SubStateEnum.col_pending_takeBack.getKey()).equals(subSate)){
        		//取回
        		endHql.append(" and (affair.subState = :queryState ) ");
        		parameterMap.put("queryState", SubStateEnum.col_pending_takeBack.getKey());
        	}
        }
        
        //协同穿透条件 end

        //待发,已发协同按创建日期查询,待办按接收时间查询,已办按完成时间查询
        String createDate = condition.get(ColQueryCondition.createDate.name());
        if (createDate != null) {
            String[] date = createDate.split("#");
            if(date != null && date.length > 0){
                if (Strings.isNotBlank(date[0])) {
                    Date stamp = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(date[0]));
                    endHql.append(" and affair.createDate >= :timestampC1");
                    parameterMap.put("timestampC1", stamp);
                }
                if(date.length > 1){
                    if (Strings.isNotBlank(date[1])) {
                        Date stamp = Datetimes.parseDatetime(Datetimes.getLastTimeStr(date[1]));
                        endHql.append(" and affair.createDate <= :timestampC2");
                        parameterMap.put("timestampC2", stamp);
                    }
                }
            }
        }
        //待办 按照接收时间
        String receiveDates = condition.get(ColQueryCondition.receiveDate.name());
        if(receiveDates != null){
            String[] date = receiveDates.split("#");
            if(date != null && date.length > 0){
                if (Strings.isNotBlank(date[0])) {
                    Date stamp = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(date[0]));
                    endHql.append(" and affair.receiveTime >= :timestampR1");
                    parameterMap.put("timestampR1", stamp);
                }
                if(date.length > 1){
                    if (Strings.isNotBlank(date[1])) {
                        Date stamp = Datetimes.parseDatetime(Datetimes.getLastTimeStr(date[1]));
                        endHql.append(" and affair.receiveTime <= :timestampR2");
                        parameterMap.put("timestampR2", stamp);
                    }
                }
            }
        }
        //已办按完成时间查询
        String completeDates = condition.get(ColQueryCondition.completeDate.name());
        if(completeDates != null){
            String[] date = completeDates.split("#");
            if(date != null && date.length > 0){
                if (Strings.isNotBlank(date[0])) {
                    Date stamp = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(date[0]));
                    endHql.append(" and affair.completeTime >= :timestamp1");
                    parameterMap.put("timestamp1", stamp);
                }
                if(date.length > 1){
                    if (Strings.isNotBlank(date[1])) {
                        Date stamp = Datetimes.parseDatetime(Datetimes.getLastTimeStr(date[1]));
                        endHql.append(" and affair.completeTime <= :timestamp2");
                        parameterMap.put("timestamp2", stamp);
                    }
                }
            }
        }
        StringBuilder completeTimeQueryHql = getCompleteTimeQueryHql("affair",condition, parameterMap);
        endHql.append(completeTimeQueryHql);
        
        //绩效考核统计条件
        String staticSql = getStatisticHql4SendAndComplete(condition, parameterMap, "affair");
        endHql.append(staticSql);
        
        //绩效考核统计条件：超期
        String staticSqlCovertime = getStatisticHql4CoverTime(condition, parameterMap, "affair");
        endHql.append(staticSqlCovertime);
        
        //暂存待办按修改时间查询
        String updateDates = condition.get(ColQueryCondition.updateDate.name());
        if(updateDates != null){
            String[] date = updateDates.split("#");
            if(date != null && date.length > 0){
                if (Strings.isNotBlank(date[0])) {
                    Date stamp = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(date[0]));
                    endHql.append(" and affair.updateDate >= :timestamp1");
                    parameterMap.put("timestamp1", stamp);
                }
                if(date.length > 1){
                    if (Strings.isNotBlank(date[1])) {
                        Date stamp =Datetimes.parseDatetime(Datetimes.getLastTimeStr(date[1]));
                        endHql.append(" and affair.updateDate <= :timestamp2");
                        parameterMap.put("timestamp2", stamp);
                    }
                }
            }
        }
        //是否是模版协同
        String colType = condition.get(ColQueryCondition.CollType.name());
        if(colType != null){
            if("Templete".equals(colType)){ //模版协同
                endHql.append(" and (affair.templeteId is not null) ");
            }
            else if("Self".equals(colType)){ //自由协同
                endHql.append(" and (affair.templeteId is null) ");
            }
        }
        String templeteIds = condition.get(ColQueryCondition.templeteIds.name());
        if (Strings.isNotBlank(templeteIds)) {
            endHql.append(" and affair.templeteId in(:templeteIds) ");
            List<Long> l = new ArrayList<Long>();
            List<String> ls = Arrays.asList(condition.get(ColQueryCondition.templeteIds.name()).split(","));
            for (String s : ls) {
                l.add(Long.valueOf(s));
                if (l.size() > 999) {
                    break;
                }
            }
            parameterMap.put("templeteIds", l);
        }
        
        if ("true".equals(condition.get(ColQueryCondition.affairFavorite.name()))) {
            endHql.append(" and affair.delete =:affairDelete and affair.hasFavorite=:hasFavorite ");
            parameterMap.put("affairDelete", Boolean.FALSE);
            parameterMap.put("hasFavorite", Boolean.TRUE);
        }
        
        if(Strings.isNotBlank(condition.get(ColQueryCondition.startMemberName.name()))){
            endHql.append(" and mem.name like :startMemberName");
            parameterMap.put("startMemberName", "%" + SQLWildcardUtil.escape(condition.get(ColQueryCondition.startMemberName.name())) + "%");
        }
        
        if(Strings.isNotBlank(condition.get(ColQueryCondition.preApproverName.name()))){
        	endHql.append(" and mem2.name like :preApproverName");
            parameterMap.put("preApproverName", "%" + SQLWildcardUtil.escape(condition.get(ColQueryCondition.preApproverName.name())) + "%");
        	
        }
        //按发起人id查询
        if(Strings.isNotBlank(condition.get(ColQueryCondition.startMemberId.name()))){
        	if(condition.get(ColQueryCondition.fromId.name())!=null){
        		endHql.append(" and (affair.senderId in (:senderId) or affair.fromId in(:fromId))");
        		
        		List<Long> fromIds = new ArrayList<Long>();
                List<String> fromIdString = Arrays.asList(condition.get(ColQueryCondition.fromId.name()).split(","));
                for(String s :fromIdString){
               	 fromIds.add(Long.valueOf(s));
                }
                parameterMap.put("fromId",fromIds);
                
        	}else{
        		endHql.append(" and affair.senderId in (:senderId)");
        	}
        	 List<Long> senderMemberIds = new ArrayList<Long>();
             List<String> senderMemberString = Arrays.asList(condition.get(ColQueryCondition.startMemberId.name()).split(","));
             for(String s :senderMemberString){
            	 senderMemberIds.add(Long.valueOf(s));
             }
             parameterMap.put("senderId",senderMemberIds);
        }
        
        //按领导发的查询
        if(Strings.isNotBlank(condition.get(ColQueryCondition.myLeader.name()))){
             endHql.append(" and affair.senderId in (:senderId)");
             List<V3xOrgMember> members = new ArrayList<V3xOrgMember>();
             try {
                 members = orgManager.getMembersByDepartmentRoleOfAll(AppContext.getCurrentUser().getDepartmentId(), Role_NAME.DepManager.name());
                 members.addAll(orgManager.getMembersByDepartmentRoleOfAll(AppContext.getCurrentUser().getDepartmentId(), Role_NAME.DepLeader.name()));
             } catch (Exception e) {
                 log.error("", e);
             }
             List<Long> ids = new ArrayList<Long>();
             if (Strings.isNotEmpty(members)) {
                 for (V3xOrgMember m : members) {
                     if (!m.getId().equals(AppContext.getCurrentUser().getId())) {// 当自己是领导或分管领导，排除掉自己
                         ids.add(m.getId());
                     }
                 }
             }
             if(Strings.isEmpty(ids)){
                 ids.add(-1L);
             }
             parameterMap.put("senderId",ids);
        }
        
        //按本部门的查询
        if (Strings.isNotBlank(condition.get(ColQueryCondition.myDept.name()))) {
            List<V3xOrgMember> myDeptMembers = new ArrayList<V3xOrgMember>();
            try {
                myDeptMembers = orgManager.getMembersByDepartment(AppContext.getCurrentUser().getDepartmentId(), true);
            } catch (BusinessException e) {
                log.error("", e);
            }

            List<Long> myDeptMembersIds = new ArrayList<Long>();
            if (Strings.isNotEmpty(myDeptMembers)) {
                for (V3xOrgMember m : myDeptMembers) {
                    myDeptMembersIds.add(m.getId());
                }
            }
            if (Strings.isNotEmpty(myDeptMembersIds)) {
                endHql.append(" and affair.senderId in (:senderId)");
                parameterMap.put("senderId", myDeptMembersIds);
            } else {
                endHql.append(" and affair.senderId is null ");
            }
        }
        if(Strings.isNotBlank(condition.get("objectId"))){
            String objectId = condition.get("objectId");
            endHql.append(" and ").append(" affair.objectId =:objectId");
            parameterMap.put("objectId",Long.parseLong(objectId));
         }
        //按节点权限查询
        if(condition.get(ColQueryCondition.nodePolicy.name())!=null){
       	 	endHql.append(" and affair.nodePolicy = :nodePolicy");
            parameterMap.put("nodePolicy",condition.get(ColQueryCondition.nodePolicy.name()));
       }
        if(condition.get(ColQueryCondition.removeSummary.name())!=null){
        	endHql.append(" and affair.objectId not in(:summaryId)");
        	List<Long> summaryIds = new ArrayList<Long>();
            List<String> summaryIdString = Arrays.asList(condition.get(ColQueryCondition.removeSummary.name()).split(","));
            for(String s :summaryIdString){
            	summaryIds.add(Long.valueOf(s));
            }
            parameterMap.put("summaryId",summaryIds);
        }
        if(condition.get(ColQueryCondition.transactorId.name())!=null){
        	endHql.append(" and affair.transactorId in(:transactorId)");
        	List<Long> transactorIds = new ArrayList<Long>();
            List<String> transactorIdString = Arrays.asList(condition.get(ColQueryCondition.transactorId.name()).split(","));
            for(String s :transactorIdString){
            	transactorIds.add(Long.valueOf(s));
            }
            parameterMap.put("transactorId",transactorIds);
        }
        //是否为智能处理
        if("true".equals(condition.get(ColQueryCondition.aiProcessing.name()))) {
        	endHql.append(" and affair.aiProcessing =:aiProcessing");
        	parameterMap.put("aiProcessing", Boolean.TRUE);
        }
        
        String formRelation = condition.get(ColQueryCondition.formRelation.name());
        //表单授权 查询状态是正常结束和终止(即：流程结束的)的  ，或者核定通过的
        if (Strings.isNotBlank(formRelation)) {
            if ("1".equals(formRelation)) {
                endHql.append(" and (summary.state=:summaryState1 or summary.vouch = 1) ");
                parameterMap.put("summaryState1",CollaborationEnum.flowState.finish.ordinal());
                needSummary = true;
            }
        } 
        
        //发起者允许关联允许转发的已发协同
        if(condition.get(ColQueryCondition.list4Quote.name()) != null){
            endHql.append(" and summary.canForward=:canForward ");
            parameterMap.put("canForward",true);
            needSummary = true;
        }
        
        //已归档
        if("archived".equals(condition.get(ColQueryCondition.archiveId.name()))){//统计查询有已归档条件
            endHql.append(" and (affair.archiveId is not null or (summary.archiveId is not null and (affair.state=2 or affair.state=1))) and affair.delete=false ");
            needSummary = true;
        }
        
        //类型：是否超期
        String isCoverTime = condition.get(ColQueryCondition.coverTime.name());
        if(Strings.isNotBlank(isCoverTime)){
        //F8的日常工作统计中，“已归档”和“已发”的超期统计按流程超期来计算，其他应用情况按节点超期来统计
            if(condition.get("statistic") !=null
                    && (condition.get(ColQueryCondition.archiveId.name())!=null
                            ||String.valueOf(StateEnum.col_sent.getKey()).equals(condition.get(ColQueryCondition.state.name())))){
                endHql.append(" and summary.coverTime=:coverTime");
                endHql.append(" and affair.processOverTime=1");
                if("1".equals(isCoverTime)){
                    parameterMap.put("coverTime",true);
                }else{
                    parameterMap.put("coverTime",false);
                }
                needSummary = true;
            }else{
                if("1".equals(isCoverTime)){
                    endHql.append(" and affair.expectedProcessTime<= :coverTime");
                    parameterMap.put("coverTime",DateUtil.newDate());
                }else{
                    endHql.append(" and affair.coverTime=:coverTime");
                    parameterMap.put("coverTime",false);
                }
            }
        }
        
        if (Strings.isNotBlank(condition.get(ColQueryCondition.isOverdue.name()))) {
            if("1".equals(condition.get(ColQueryCondition.isOverdue.name()))){
                //已发查询流程超期
                if ("true".equals(condition.get("dumpData")) || String.valueOf(StateEnum.col_sent.getKey()).equals(state)) {
                    endHql.append(" and (summary.coverTime=1 and affair.processOverTime=1) ");
                    needSummary = true;
                } else { //待办、已办查询节点超期
                    endHql.append(" and affair.expectedProcessTime <= :nowDate ");
                    parameterMap.put("nowDate", DateUtil.newDate());
                }
            }else{
                //已发查询流程超期
                if ("true".equals(condition.get("dumpData")) || String.valueOf(StateEnum.col_sent.getKey()).equals(state)) {
                    endHql.append(" and ((summary.coverTime=0 or summary.coverTime is null) and (affair.processOverTime is null or  affair.processOverTime =0)) ");
                    needSummary = true;
                } else { //待办、已办查询节点超期
                    endHql.append(" and affair.coverTime=:affairCoverTime");
                    parameterMap.put("affairCoverTime",false);
                }
            }
        }
        
        if(Strings.isNotBlank(condition.get(ColQueryCondition.receiver.name()))){
        	endHql.append(" and exists(select 1 from CtpAffair ca1, ").append(OrgMember.class.getName()).append(" mem1 where affair.objectId = ca1.objectId and ca1.memberId = mem1.id ");
        	endHql.append(" and mem1.name like :receiver and ca1.state in (:ca1State)) ");
        	parameterMap.put("receiver", "%" + SQLWildcardUtil.escape(condition.get(ColQueryCondition.receiver.name())) + "%");
            List<Integer> caState = new ArrayList<Integer>();
            caState.add(StateEnum.col_pending.key());
            caState.add(StateEnum.col_done.key());
            parameterMap.put("ca1State", caState);
        }
        
        if(!isCounter && !"true".equals(condition.get("isColQuoteCount"))) {
            if(states.contains(StateEnum.col_pending.key())){
            	if("true".equals(condition.get(ColQueryCondition.aiSort.name()))) {
            		endHql.append(" order by affair.sortWeight desc,affair.receiveTime desc");
            	}else {
            		endHql.append(" order by affair.receiveTime desc");
            	}
            }else if(states.contains(StateEnum.col_done.key())){
                endHql.append(" order by affair.completeTime desc");
            }else{
                endHql.append(" order by affair.createDate desc");
            }
        }
        if ("true".equals(condition.get("isColQuoteCount"))) {
        	endHql.append(" and affair.state in(:quoteState)");
        	parameterMap.put("quoteState", states);
        	endHql.append(" group by affair.state ");
        }
        //------------------这里开始组装ctp_affair 查询
        StringBuilder headHql = new StringBuilder();
        if(isCounter) {
            //headHql.append(" count(affair.id) ");
        }else if("true".equals(condition.get("isColQuoteCount"))){
            headHql.append("select ");
            headHql.append(" affair.state,count(affair.id) ");
        }else{
            headHql.append("select ");
            headHql.append(selectAffair);                
            if(needSummary) {
                headHql.append(selectAffairAndSummaryLast);
            }
        }
        boolean needAndFlag = true;
        if(condition.get(ColQueryCondition.startMemberDept.name())!=null){
            if(needSummary) {
                headHql.append(" from CtpAffair as affair,ColSummary as summary,").append(OrgMember.class.getName()).append(" as mem,");
                headHql.append(" "+OrgUnit.class.getName()).append(" as dept");
                headHql.append(" where affair.objectId=summary.id and affair.senderId=mem.id and mem.orgDepartmentId = dept.id");
            } else {
                headHql.append(" from CtpAffair as affair,").append(OrgMember.class.getName()).append(" as mem,");
                headHql.append(" "+OrgUnit.class.getName()).append(" as dept");
                headHql.append(" where affair.senderId=mem.id and mem.orgDepartmentId = dept.id");   
            }
            headHql.append(" and dept.id in (:deptId)");
            List<String> deptIdString = Arrays.asList(condition.get(ColQueryCondition.startMemberDept.name()).split(","));
            List<Long> deptIdLong = new ArrayList<Long>();
            for(String s :deptIdString){
                deptIdLong.add(Long.valueOf(s));
            }
            parameterMap.put("deptId",deptIdLong);
        }
        if(headHql.indexOf(" from ")==-1){
            if(Strings.isBlank(condition.get(ColQueryCondition.startMemberName.name())) && Strings.isBlank(condition.get(ColQueryCondition.preApproverName.name()))){
                if(needSummary) {
                    headHql.append(" from CtpAffair as affair,ColSummary as summary ");
                    headHql.append(" where affair.objectId=summary.id ");
                } else {
                    headHql.append(" from CtpAffair as affair where "); 
                    needAndFlag = false;
                }
            }else{
            	if(Strings.isNotBlank(condition.get(ColQueryCondition.startMemberName.name())) && Strings.isNotBlank(condition.get(ColQueryCondition.preApproverName.name()))) {
            		if(needSummary) {
                        headHql.append(" from CtpAffair as affair,ColSummary as summary,").append(OrgMember.class.getName()).append(" as mem,").append(OrgMember.class.getName()).append(" as mem2 ");
                        headHql.append(" where affair.objectId=summary.id and affair.senderId=mem.id and affair.preApprover=mem2.id ");
                    } else {
                        headHql.append(" from CtpAffair as affair,").append(OrgMember.class.getName()).append(" as mem,").append(OrgMember.class.getName()).append(" as mem2 ");
                        headHql.append(" where affair.senderId=mem.id and affair.preApprover=mem2.id ");
                    }
            	}else if(Strings.isNotBlank(condition.get(ColQueryCondition.startMemberName.name()))){
                    if(needSummary) {
                        headHql.append(" from CtpAffair as affair,ColSummary as summary,").append(OrgMember.class.getName()).append(" as mem");
                        headHql.append(" where affair.objectId=summary.id and affair.senderId=mem.id ");
                    } else {
                        headHql.append(" from CtpAffair as affair,").append(OrgMember.class.getName()).append(" as mem");
                        headHql.append(" where affair.senderId=mem.id ");
                    }
                }else if(Strings.isNotBlank(condition.get(ColQueryCondition.preApproverName.name()))){ 
                    if(needSummary) {
                        headHql.append(" from CtpAffair as affair,ColSummary as summary,").append(OrgMember.class.getName()).append(" as mem2");
                        headHql.append(" where affair.objectId=summary.id and affair.preApprover=mem2.id ");
                    } else {
                        headHql.append(" from CtpAffair as affair,").append(OrgMember.class.getName()).append(" as mem2");
                        headHql.append(" where affair.preApprover=mem2.id ");
                    }
                }
            }
        }

        //这里添加关联字段relation
		String relation = condition.get(ColQueryCondition.relation.name());
		if(Strings.isNotBlank(relation)){
			headHql.append("  and ").append("summary.relationId = :relation ");
			parameterMap.put("relation",Long.parseLong(relation));
		}

        StringBuilder agentMemberHql = new StringBuilder();
        if(needAndFlag) {
            agentMemberHql.append(" AND ");
        }
        agentMemberHql.append(" (").append("affair").append(".memberId=:memberId   ");
        parameterMap.put("memberId", userId);
//        parameterMap.put("proxyMemberId", 0L);//自己的数据默认为null and  affair.proxyMemberId = :proxyMemberId
        StringBuilder agentMemberIdHql = getMemberIdIncludeAgent(userId, "affair", parameterMap, hasAgent);

        if(null != agentMemberIdHql && Strings.isNotBlank(agentMemberIdHql.toString())){//代理sql为空时不查询
            agentMemberHql.append(agentMemberIdHql);
        }
        agentMemberHql.append(")");
        StringBuilder childrenHql3 = getChildrenFilterHql(condition, parameterMap, userId, states, hasAgent, null, formRelation,"affair");
        agentMemberHql .append(childrenHql3);
        headHql.append(agentMemberHql);
        hql.append(headHql.append(endHql));
	}
	
	@Override
	public List<ColSummaryVO> queryByCondition4DataRelation(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
	    Map<String, Object> parameterMap = new HashMap<String, Object>();
        //当前用户的id
        String userIdStr = condition.get(ColQueryCondition.currentUser.name());
	    Long userId = 0l;
	    if(Strings.isNotBlank(userIdStr)){
            userId = Long.valueOf(userIdStr);
        }
        //查询affairMemberId 的事项
        String affrirMemberIdStr = condition.get(ColQueryCondition.affairMemberId.name());
        Long affairMemberId = 0L;
        if(Strings.isNotBlank(affrirMemberIdStr)){
        	affairMemberId = Long.valueOf(affrirMemberIdStr);
        }
        StringBuilder hql = new StringBuilder();
        hql.append("select ");
        hql.append(sql4DataRelation);
        
        
        boolean isNeedAnd = true;
        if(condition.get(ColQueryCondition.startMemberDept.name())!=null){
            hql.append(" from CtpAffair as affair,").append(OrgMember.class.getName()).append(" as mem,");
            hql.append(" "+OrgUnit.class.getName()).append(" as dept");
            hql.append(" where affair.senderId=mem.id and mem.orgDepartmentId = dept.id");
            hql.append(" and dept.id in (:deptId)");
            List<String> deptIdString = Arrays.asList(condition.get(ColQueryCondition.startMemberDept.name()).split(","));
            List<Long> deptIdLong = new ArrayList<Long>();
            for(String s :deptIdString){
            	deptIdLong.add(Long.valueOf(s));
            }
            parameterMap.put("deptId",deptIdLong);
        }
        if(hql.indexOf(" from ")==-1){
        	if(condition.get(ColQueryCondition.startMemberName.name())==null){
                hql.append(" from CtpAffair as affair ");
                hql.append(" where ");
                isNeedAnd = false;
            }else{
            	hql.append(" from CtpAffair as affair,").append(OrgMember.class.getName()).append(" as mem");
                hql.append(" where affair.senderId=mem.id ");
            }
        }
        
        
      //事项状态
        List<Integer> states = new ArrayList<Integer>();
        String state = condition.get(ColQueryCondition.state.name());
        if(Strings.isNotBlank(state)){
        	String[] stateStrs =state.split(",");
        	if(stateStrs != null && stateStrs.length > 0){
        	    for(int i=0;i<stateStrs.length;i++){
                    states.add(Integer.parseInt(stateStrs[i]));
                }
        	}
        }
        //是否需要查询代理
        boolean hasAgent = "true".equals(condition.get("hasNeedAgent"));
    	if(isNeedAnd){
    		hql.append(" AND ");
    	}
        hql.append(" (").append("affair").append(".memberId =:memberId   ");
		parameterMap.put("memberId", affairMemberId);
//		StringBuilder agentMemberIdHql = getMemberIdIncludeAgent(userId, "affair", parameterMap, hasAgent);
		hql.append(")");
		
		//state
		if(states.size() == 1){
			hql.append(" and affair.state = :affairState ");
		    parameterMap.put("affairState", states.get(0));
		}else if(states.size() > 1){
			hql.append(" and affair.state in(:affairState) ");
		    parameterMap.put("affairState", states);
		}
		
		//app
		hql.append(" and affair.app = :queryApp ");
		parameterMap.put("queryApp",ModuleType.collaboration.getKey());
        
        if(condition.get(ColQueryCondition.startMemberId.name())!=null){
        	List<Long> senderMemberIds = new ArrayList<Long>();
        	List<String> senderMemberString = Arrays.asList(condition.get(ColQueryCondition.startMemberId.name()).split(","));
        	for(String s :senderMemberString){
        		senderMemberIds.add(Long.valueOf(s));
        	}
        	if(Strings.isNotEmpty(senderMemberIds) && senderMemberIds.size() == 1){
        		hql.append(" and affair.senderId = :senderId");
        		parameterMap.put("senderId",senderMemberIds.get(0));
        	}else{
        		hql.append(" and affair.senderId in (:senderId)");
        		parameterMap.put("senderId",senderMemberIds);
        	}
        }
        
        if (Strings.isNotBlank(condition.get(ColQueryCondition.delete.name()))) {
        	hql.append(" and affair.delete = :queryDelete ");
        	parameterMap.put("queryDelete", Boolean.FALSE);
        }
		
        if (Strings.isNotBlank(condition.get(ColQueryCondition.subject.name()))) {
            hql.append(" and affair.subject like :subject ");
            parameterMap.put("subject", "%" + SQLWildcardUtil.escape(condition.get(ColQueryCondition.subject.name())) + "%");
        }
        
        if (Strings.isNotBlank(condition.get(ColQueryCondition.importantLevel.name()))) {
            hql.append(" and affair.importantLevel=:importantLevel");
            parameterMap.put("importantLevel",Integer.parseInt(condition.get(ColQueryCondition.importantLevel.name())));
        }
        //类型：协同、表单
        String bodyType = condition.get(ColQueryCondition.bodyType.name());
        if( Strings.isNotBlank(bodyType)){
        	//穿透查询
        	if(condition.get("statistic") != null){
        		if(ColUtil.isForm(bodyType)){
        			hql.append(" and affair.bodyType=:bodyType");
        		}else{
        			hql.append(" and affair.bodyType!=:bodyType");
        		}
        		parameterMap.put("bodyType",String.valueOf(MainbodyType.FORM.getKey()));
        	}else if("col".equals(bodyType)){
        		hql.append(" and affair.bodyType!=:bodyType");
        		parameterMap.put("bodyType",String.valueOf(MainbodyType.FORM.getKey()));
        	} else {
        		hql.append(" and affair.bodyType=:bodyType");
                parameterMap.put("bodyType",bodyType);
        	}
        }
        String templateAll = condition.get(ColQueryCondition.templeteAll.name());
        if("all".equals(templateAll)){
          hql.append(" and affair.bodyType=20");
        }

        //待发,已发协同按创建日期查询,待办按接收时间查询,已办按完成时间查询
        String createDate = condition.get(ColQueryCondition.createDate.name());
        if (createDate != null) {
            String[] date = createDate.split("#");
            if(date != null && date.length > 0){
                if (Strings.isNotBlank(date[0])) {
                    Date stamp = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(date[0]));
                    hql.append(" and affair.createDate >= :timestamp1");
                    parameterMap.put("timestamp1", stamp);
                }
                if(date.length > 1){
                    if (Strings.isNotBlank(date[1])) {
                        Date stamp = Datetimes.parseDatetime(Datetimes.getLastTimeStr(date[1]));
                        hql.append(" and affair.createDate <= :timestamp2");
                        parameterMap.put("timestamp2", stamp);
                    }
                }
            }
        }
        //待办 按照接收时间
        String receiveDates = condition.get(ColQueryCondition.receiveDate.name());
        if(receiveDates != null){
            String[] date = receiveDates.split("#");
            if(date != null && date.length > 0){
                if (Strings.isNotBlank(date[0])) {
                    Date stamp = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(date[0]));
                    hql.append(" and affair.receiveTime >= :timestampR1");
                    parameterMap.put("timestampR1", stamp);
                }
                if(date.length > 1){
                    if (Strings.isNotBlank(date[1])) {
                        Date stamp = Datetimes.parseDatetime(Datetimes.getLastTimeStr(date[1]));
                        hql.append(" and affair.receiveTime <= :timestampR2");
                        parameterMap.put("timestampR2", stamp);
                    }
                }
            }
        }
        //已办按完成时间查询
        String completeDates = condition.get(ColQueryCondition.completeDate.name());
        if(completeDates != null){
            String[] date = completeDates.split("#");
            if(date != null && date.length > 0){
                if (Strings.isNotBlank(date[0])) {
                    Date stamp = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(date[0]));
                    hql.append(" and affair.completeTime >= :timestamp1");
                    parameterMap.put("timestamp1", stamp);
                }
                if(date.length > 1){
                    if (Strings.isNotBlank(date[1])) {
                        Date stamp = Datetimes.parseDatetime(Datetimes.getLastTimeStr(date[1]));
                        hql.append(" and affair.completeTime <= :timestamp2");
                        parameterMap.put("timestamp2", stamp);
                    }
                }
            }
        }
        StringBuilder completeTimeQueryHql = getCompleteTimeQueryHql("affair",condition, parameterMap);
        hql.append(completeTimeQueryHql);
        
        //是否是模版协同
        String colType = condition.get(ColQueryCondition.CollType.name());
        if(colType != null){
            if("Templete".equals(colType)){ //模版协同
                hql.append(" and (affair.templeteId is not null) ");
            }
            else if("Self".equals(colType)){ //自由协同
                hql.append(" and (affair.templeteId is null) ");
            }
        }
        String templeteIds = condition.get(ColQueryCondition.templeteIds.name());
        if(Strings.isNotBlank(templeteIds)){
            hql.append(" and affair.templeteId in(:templeteIds) ");
            List<Long> l = new ArrayList<Long>();
            List<String> ls = Arrays.asList(condition.get(ColQueryCondition.templeteIds.name()).split(","));
            for(String s :ls){
                l.add(Long.valueOf(s));
            }
            parameterMap.put("templeteIds",l);
        }
        
        if(condition.get(ColQueryCondition.startMemberName.name())!=null){
            hql.append(" and mem.name like :startMemberName");
            parameterMap.put("startMemberName", "%" + SQLWildcardUtil.escape(condition.get(ColQueryCondition.startMemberName.name())) + "%");
        }
       
        //按节点权限查询
       if(condition.get(ColQueryCondition.nodePolicy.name())!=null){
       	 	hql.append(" and affair.nodePolicy = :nodePolicy");
            parameterMap.put("nodePolicy",condition.get(ColQueryCondition.nodePolicy.name()));
       }
       //移除某个协同
        if(condition.get(ColQueryCondition.removeSummary.name())!=null){
        	hql.append(" and affair.objectId <> :summaryId");
        	Long summaryIds = null;
            List<String> summaryIdString = Arrays.asList(condition.get(ColQueryCondition.removeSummary.name()).split(","));
            for(String s :summaryIdString){
                summaryIds = Long.valueOf(s);
            }
            parameterMap.put("summaryId",summaryIds);
        }
        //查询代理人
        if(condition.get(ColQueryCondition.transactorId.name())!=null){
        	hql.append(" and ((affair.transactorId in(:transactorId) and affair.state=4) ");
        	if(states.contains(StateEnum.col_pending.getKey())){
        		hql.append(" or affair.state=3 ");
        	}
        	if(states.contains(StateEnum.col_sent.getKey())){
        		hql.append(" or affair.state=2 ");
        	}
        	hql.append(")");
        	List<Long> transactorIds = new ArrayList<Long>();
            List<String> transactorIdString = Arrays.asList(condition.get(ColQueryCondition.transactorId.name()).split(","));
            for(String s :transactorIdString){
            	transactorIds.add(Long.valueOf(s));
            }
            parameterMap.put("transactorId",transactorIds);
        }
        
        if(condition.get("currentProxy")!=null){
        	hql.append(" and (affair.memberId in(:currentProxyMemberIds) )");
        	List<Long> memberIds = new ArrayList<Long>();
            List<String> memberIdString = Arrays.asList(condition.get("currentProxy").split(","));
            for(String s :memberIdString){
            	memberIds.add(Long.valueOf(s));
            }
            parameterMap.put("currentProxyMemberIds",memberIds);
        }
        if(!"true".equals(condition.get("isColQuoteCount"))) {
            if(states.contains(StateEnum.col_pending.key())){
                hql.append(" order by affair.receiveTime desc");
            }else if(states.contains(StateEnum.col_done.key())){
                hql.append(" order by affair.completeTime desc");
            }else{
                hql.append(" order by affair.createDate desc");
            }
        }
        if ("true".equals(condition.get("isColQuoteCount"))) {
        	hql.append(" and affair.state in(:quoteState)");
        	parameterMap.put("quoteState", states);
        	hql.append(" group by affair.state ");
        }

        log.info("数据关联日志：hql.toString()="+hql.toString());
        StringBuffer bufferLog = new StringBuffer();
        if(null != parameterMap){
        	Set<String> keySet = parameterMap.keySet();
        	Iterator<String> iterator = keySet.iterator();
        	while(iterator.hasNext()){
        		String next = iterator.next();
        		bufferLog.append("key="+next+",value="+
        		(null != parameterMap.get(next) ? parameterMap.get(next).toString():null));
        	}
        }
        log.info("bufferLog="+bufferLog.toString());
        List l = DBAgent.find(hql.toString(), parameterMap, flipInfo);

        return convertAffair2VO(l);
    }
	

	private void transLogAgent(Long userId, AgentModel agent)throws BusinessException {
		String agentName = "";
		String agentToName = "";
		if(agent.getAgentId()!= null){
			V3xOrgMember member =  orgManager.getMemberById(agent.getAgentId());
			if(member!=null){
				agentName = member.getName();
			}
		}

		if(agent.getAgentToId()!= null){
			V3xOrgMember member =  orgManager.getMemberById(agent.getAgentToId());
			if(member!=null){
				agentToName = member.getName();
			}
		}

		String username="";
		if(userId!=null){
			V3xOrgMember member =  orgManager.getMemberById(userId);
			if(member!=null){
				username = member.getName();
			}
		}
		StringBuilder info = new StringBuilder();
		info.append("协同列表查询，有效代理:")
		.append("id:"+agent.getId())
		.append(",AgentId:"+agent.getAgentId())
		.append(",agentName:"+agentName)
		.append(",AgentToId:"+agent.getAgentToId())
		.append(",agentToName:"+agentToName)
		.append(",当前用户："+userId)
		.append(",username:"+username)
		.append(",开始时间:"+agent.getStartDate())
		.append(",结束时间:"+agent.getEndDate())
		.append(",创建时间:"+agent.getCreateDate());
		log.info(info.toString());
	}

    private static void make(Object[] object, ColSummary summary, CtpAffair affair, boolean needSummary) {
        int n = 0;
        summary.setId(((Number) object[n++]).longValue());
        String subject = (String) object[n++];
        summary.setImportantLevel(object[n] == null ? 0 : ((Number) object[n]).intValue());
        n++;
        summary.setStartMemberId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        summary.setForwardMember((String) object[n++]);
        summary.setCreateDate((Date) object[n]);
        summary.setStartDate((Date) object[n++]);
        summary.setResentTime(object[n] == null ? 0 : ((Number) object[n]).intValue());
        n++;
        summary.setBodyType((String) object[n++]);
        summary.setIdentifier((String) object[n++]);
        summary.setCaseId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        summary.setProcessId((String) object[n++]);
        summary.setTempleteId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        summary.setSubject(subject);
        summary.setState(object[n] == null ? 0 : ((Number) object[n]).intValue());
        n++;
        summary.setCoverTime((Boolean) object[n++]);
        affair.setId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        affair.setState(object[n] == null ? 0 : ((Number) object[n]).intValue());
        n++;
        affair.setSubState(object[n] == null ? 0 : ((Number) object[n]).intValue());
        n++;
        affair.setTrack(object[n] == null ? 0 : ((Number) object[n]).intValue());
        n++;
        affair.setHastenTimes(object[n] == null ? 0 : ((Number) object[n]).intValue());
        n++;
        affair.setCoverTime((Boolean) object[n++]);
        affair.setRemindDate(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        affair.setDeadlineDate(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        affair.setReceiveTime((Date) object[n++]);
        affair.setUpdateDate(affair.getReceiveTime());
        affair.setCompleteTime((Date) object[n++]);
        affair.setCreateDate((Date) object[n++]);
        affair.setMemberId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        affair.setTransactorId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        affair.setNodePolicy((String) object[n++]);
        affair.setIdentifier((String) object[n++]);
        affair.setSubObjectId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        affair.setActivityId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        affair.setArchiveId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        affair.setSubject(summary.getSubject());
        affair.setForwardMember(summary.getForwardMember());
        affair.setResentTime(summary.getResentTime());
        summary.setOrgAccountId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        affair.setUpdateDate((Date) object[n++]);
        affair.setExpectedProcessTime((Date) object[n++]);
        affair.setHasFavorite((Boolean) object[n++]);
        affair.setBackFromId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        summary.setFormAppid(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        summary.setFormRecordid(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        affair.setFromId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        if (object[n] instanceof Number) {
            summary.setAutoRun(object[n] == null ? null : ((Number) object[n]).intValue() == 1 ? true : false);
        } else {
            summary.setAutoRun(object[n] == null ? null : (Boolean) object[n]);
        }
        n++;
        affair.setPreApprover(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        affair.setFormOperationId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        summary.setDeadlineDatetime((Date) object[n++]);
		affair.setProxyMemberId(object[n] == null ? null : ((Number) object[n]).longValue());
	    n++;
		affair.setPrint(object[n] == null ? null : ((Number) object[n]).intValue());
		n++;
		//根据这里断前面是selectAffair 查询 后面是selectAffairAndSummaryLast 查询
        
        if (needSummary) {
            summary.setCanArchive((Boolean) object[n++]);
            summary.setFinishDate((Date) object[n++]);
            summary.setDeadline(object[n] == null ? null : ((Number) object[n]).longValue());
            n++;
            summary.setDeadlineDatetime((Date) object[n++]);
            String source = (String) object[n++];
            if (Strings.isNotBlank(source)) {
                subject += "(" + source + ")";
                summary.setSource(source);
            }
            summary.setArchiveId(object[n] == null ? null : ((Number) object[n]).longValue());
            n++;
            summary.setNewflowType(object[n] == null ? 0 : ((Number) object[n]).intValue());
            n++;
            summary.setCurrentNodesInfo((String) object[n++]);
            summary.setParentformSummaryid(object[n] == null ? null : ((Number) object[n]).longValue());
            n++;
            summary.setCanEdit((Boolean) object[n++]);
            summary.setCanForward((Boolean) object[n++]);
            summary.setPermissionAccountId(object[n] == null ? null : ((Number) object[n]).longValue());
            n++;
            summary.setReplyCounts(object[n] == null ? 0 : ((Number) object[n]).intValue());
            n++;
        }
    }

   /**
    * 将查询的affair集合转换成vo，并且添加代理的特殊处理
    * @param result
    * @param showProxyInfo4A
    * @return
    * @throws BusinessException
    */
    private List<ColSummaryVO> convertSelectAffair2ColSummaryModel(List result, boolean showProxyInfo4A,Long suserId,boolean needSummary,String stateStr) throws BusinessException{

    	Long userId = suserId;
    	if(suserId == null){
    		userId = AppContext.getCurrentUser().getId();
        }

        /*
         * A设B为代理
         *
         * B登录，获取我的被代理人A的事项，并把这些事项标记为蓝色（只要“事项的MemberId!=我”即可）
         * A登录，仅按照"member=我的id"即可，但要把我代理出去的事项标记为蓝色（事项是否落入到代理条件内）
         *
         * 查询我的代理列表(我为被代理人，我找别人干活)   key: 模板ID,自由协同为AllSelf,全部模板为AllTemplate
         */
        Map<Object, AgentModel> agentToModelColl = new HashMap<Object, AgentModel>();
        List<Long> senderMembers = new ArrayList<Long>();
        if(showProxyInfo4A){ //A登录
        	//查询我的代理人
            List<AgentModel> agentToModelList = MemberAgentBean.getInstance().getAgentModelToList(userId);
            if(!Strings.isEmpty(agentToModelList)){
                for (AgentModel agentTo : agentToModelList) {
                	List<AgentScopeModel> agentScopeModels = agentTo.getAgentScope();
                    if(agentScopeModels != null && agentScopeModels.size() > 0){
                        //代理中的发起人设置
                        List<Long> senderDepartments = new ArrayList<Long>();
                        for(AgentScopeModel agentScopeMode: agentScopeModels){
                            if("Member".equals(agentScopeMode.getEntityType())){
                                senderMembers.add(agentScopeMode.getEntityId());
                            }
                            if("Department".equals(agentScopeMode.getEntityType())){
                                senderDepartments.add(agentScopeMode.getEntityId());
                            }
                    
                        }
                        //发起人部门
                        if(senderDepartments.size() >0 ){
                            for( Long parentId : senderDepartments){
                                List<V3xOrgMember> members = this.getOrgManager().getMembersByDepartment(parentId, true);
                                if(members != null &&  members.size() > 0){
                                    for (V3xOrgMember xOrgMember :members){
                                        senderMembers.add(xOrgMember.getId());
                                    }
                                }
                            }
                        }
                    }
                    if(agentTo.getStartDate().before(new Date()) && agentTo.getEndDate().after(new Date())){
                        if(agentTo.isHasCol()){
                            agentToModelColl.put("AllSelf", agentTo);  //表示自由协同
                        }
                        if(agentTo.isHasTemplate()){
                            List<AgentDetailModel> models = agentTo.getAgentDetail(ApplicationCategoryEnum.collaboration.key());
                            if(!Strings.isEmpty(models)){
                                for (AgentDetailModel agentDetailModel : models) {
                                    agentToModelColl.put(agentDetailModel.getEntityId(), agentTo);  //指定模板
                                }
                            }
                            else{
                                agentToModelColl.put("AllTemplate", agentTo);  //表示全部模板
                            }
                        }
                    }
                }
            }
        }

        List<ColSummaryVO> models = new ArrayList<ColSummaryVO>();
        if(result == null || result.isEmpty()){
            return models;
        }
        List<Long> summaryIds = new ArrayList<Long>();
        for(int i = 0; i < result.size(); i++) {
            Object[] object = (Object[]) result.get(i);
            ColSummary summary = new ColSummary();
            CtpAffair affair = new CtpAffair();
            make(object, summary, affair,needSummary);
            summaryIds.add(summary.getId());
            affair.setFormAppId(summary.getFormAppid());
            affair.setFormRecordid(summary.getFormRecordid());
            affair.setSenderId(summary.getStartMemberId());
            
            long summaryId = summary.getId();
            // 开始组装最后返回的结果
            ColSummaryVO model = new ColSummaryVO();
            model.setStartDate(summary.getStartDate());
            model.setCaseId(summary.getCaseId() + "");
            model.setWorkitemId(affair.getSubObjectId());
            model.setProcessId(summary.getProcessId());
            model.setReceiveTime(affair.getReceiveTime());
            model.setSummary(summary);
            model.setSummaryId(String.valueOf(summaryId));
            //设置当前处理人信息
            model.setCurrentNodesInfo(ColUtil.parseCurrentNodesInfo(summary));
            //模版id
            model.setTempleteId(summary.getTempleteId());
            model.setAffairId(affair.getId());
            model.setBodyType(summary.getBodyType());
            model.setTrack(affair.getTrack());
            //节点期限
            model.setDeadLineDate(affair.getDeadlineDate());
            //节点期限名称
            if(affair.getExpectedProcessTime()!= null){
            	model.setDeadLineDateName(ColUtil.getDeadLineName(affair.getExpectedProcessTime()));
            }else{
            	model.setDeadLineDateName(ColUtil.getDeadLineName(model.getDeadLineDate()));
            }
            //节点是否超期
            model.setIsCoverTime(ColUtil.checkAffairIsOverTime(affair, summary));
            //节点超期提示语
            if(model.getIsCoverTime()){
                model.setCoverTime(ResourceUtil.getString("workflow.nodeProperty.time.isovertoptime.true"));//已超期
            }else{
                model.setCoverTime(ResourceUtil.getString("workflow.nodeProperty.time.isovertoptime.false"));//未超期
            }
            //重要程度
            model.setImportantLevel(summary.getImportantLevel());

            Long templateId = summary.getTempleteId();

            
            //代理
            String proxyMemberName = "";
            if(showProxyInfo4A && (Strings.isEmpty(senderMembers) || senderMembers.contains(affair.getSenderId()))){
                if(!affair.getMemberId().equals(userId)){
                    model.setProxyName(Functions.showMemberNameOnly(affair.getMemberId()));
                    model.setProxy(true);
                }
                else if(agentToModelColl != null && !agentToModelColl.isEmpty()){
                	
                    Date early = null;
                    if(templateId == null && agentToModelColl.get("AllSelf") != null){ //自由协同
                        early = agentToModelColl.get("AllSelf").getStartDate();
                    }
                    else if(templateId != null && agentToModelColl.get("AllTemplate") != null){
                        early = agentToModelColl.get("AllTemplate").getStartDate();
                    }
                    else if(templateId != null && agentToModelColl.get(templateId) != null){
                        early = agentToModelColl.get(templateId).getStartDate();
                    }

                    // 如果有接收时间就取接收时间比较，否则取创建时间，有些数据升级上来没有接收时间
                    Date affairTime = affair.getReceiveTime();
                    if(affairTime == null) {
                    	affairTime = affair.getCreateDate();
                    }
                    
                    if(early != null && early.before(affairTime)){
                        model.setProxy(true);
                    }
                }
                if(Strings.isNotBlank(model.getProxyName())) {
                	
                	proxyMemberName = model.getProxyName();
                	
                }else if (affair.getTransactorId() != null){
                	
                	proxyMemberName = Functions.showMemberNameOnly(affair.getTransactorId());
                	
                } else if (affair.getProxyMemberId() != null && !Integer.valueOf(StateEnum.col_done.key()).equals(affair.getState())) {
                	proxyMemberName = Functions.showMemberNameOnly(affair.getProxyMemberId());
                }
                //没有代理人信息,标题列也不能标蓝显示
                if (Strings.isBlank(proxyMemberName)) {
                	model.setProxy(false);
                }
            }
			
            //设置标题 包含代理信息
            String subject = summary.getSubject();
            
            //已办,无论当前有没有有效的代理,都是需要设置是否是被代理人处理
            if(affair.getTransactorId() != null && (Strings.isEmpty(senderMembers) || senderMembers.contains(affair.getSenderId())) && Integer.valueOf(StateEnum.col_done.key()).equals(affair.getState())){
                if(null != summary.getAutoRun() && summary.getAutoRun()){
                	subject = ResourceUtil.getString("collaboration.newflow.fire.subject",subject);
            		affair.setSubject(subject);
                }
            	subject = ColUtil.showSubjectOfSummary4Done(affair, -1);
            } else { 
            	subject = ColUtil.showSubjectOfSummary(summary, model.isProxy(), -1, proxyMemberName);
            }
            model.setSubject(subject);
            //设置发起人ID
            model.setStartMemberId(summary.getStartMemberId());
            //设置发起人姓名
            model.setStartMemberName(ColUtil.getMemberName(summary.getStartMemberId()));
            
            model.setPreApproverName(null == affair.getPreApprover() ? "" : ColUtil.getMemberName(affair.getPreApprover()));
            
            //创建时间
            model.setCreateDate(summary.getCreateDate());
            //设置父协同id
            model.setParentformSummaryid(summary.getParentformSummaryid());
            //能否编辑正文
            model.setCanEdit(summary.getCanEdit());
            //设置流程状态
            Integer state = summary.getState();
            model.setState(state);
            //设置子状态
            Integer sub_state = affair.getSubState();
            if (sub_state != null) {
                String subStateName = ResourceUtil.getString("collaboration.substate."+sub_state+".label");
                
                
                boolean isWaitSend = Integer.valueOf(StateEnum.col_waitSend.getKey()).equals(affair.getState());
                boolean isSpecicalback  = Integer.valueOf(SubStateEnum.col_pending_specialBackToSenderCancel.getKey()).equals(affair.getSubState())
										 ||Integer.valueOf(SubStateEnum.col_pending_specialBacked.getKey()).equals(affair.getSubState());
     		   		
                if(isWaitSend && isSpecicalback){
                	//待发
                	subStateName = ResourceUtil.getString("collaboration.substate."+SubStateEnum.col_waitSend_stepBack.getKey()+".label");
                }
                
                model.setSubState(sub_state);
                model.setSubStateName(subStateName);
                
            }
            model.setIsTrack(AffairUtil.isTrack(affair));
            //催办次数
            if(affair.getHastenTimes()!=null){
                model.setHastenTimes(affair.getHastenTimes());
            }else{
                model.setHastenTimes(Integer.parseInt("0"));
            }
            model.setOvertopTime(affair.isCoverTime());
            model.setAdvanceRemindTime(affair.getRemindDate());
            //流程期限时间点
            model.setDeadLineDateTime2(summary.getDeadlineDatetime());
            //流程期限名称
            if(summary.getDeadlineDatetime()!=null){
            	model.setDeadLineName(ColUtil.getDeadLineName(model.getDeadLineDateTime2()));
            }else{//兼容老数据，还是按时间段显示
            	//流程期限
                model.setDeadLine(summary.getDeadline());
                //流程期限名称
                model.setDeadLineName(ColUtil.getDeadLineName(model.getDeadLine()));
            }
            //计算流程是否超期
            if (affair.getState().intValue() == StateEnum.col_sent.key()) {
                boolean isOverTime = summary.isCoverTime() == null ? false : summary.isCoverTime();
                model.setWorklfowIsTimeout(isOverTime);
                model.setWorklfowTimeout(ResourceUtil.getString("collaboration.process.mouseover.overtop."+isOverTime+".title"));

            }
            //是否有附件
            model.setHasAttsFlag(ColUtil.isHasAttachments(summary));
            
            //是否是超级节点
            model.setSuperNode(AffairUtil.isSuperNode(affair));
            
            //处理时间
            model.setDealTime(affair.getCompleteTime());
            //完成时间
            model.setFinishDate(affair.getCompleteTime());
            //流程结束
            model.setFlowFinished(model.isFinshed());
            model.setNodePolicy(affair.getNodePolicy());
            //设置是否显示表单授权图标的变量
            model.setShowAuthorityButton(AffairUtil.getIsRelationAuthority(affair));
            model.setDeadLineDateTime(affair.getExpectedProcessTime());
            model.setDeadLineDateTime1();
            model.setActivityId(affair.getActivityId());
            model.setAffair(affair);
            model.setHasFavorite(affair.getHasFavorite());
            model.setCanForward(summary.getCanForward());
            getSubjectIncludeIcon(summary);
            model.setReplyCounts(summary.getReplyCounts());
            models.add(model);
        }
        if (Strings.isNotBlank(stateStr)) {
            String[] stateStrs = stateStr.split(",");
            if (stateStrs != null && stateStrs.length == 1 && (stateStrs[0].equals(String.valueOf(StateEnum.col_sent.key()))
                    || stateStrs[0].equals(String.valueOf(StateEnum.col_done.key())))) {
                Map<String, ColSummary> colSummaryMap = new HashMap<String, ColSummary>();
                if (Strings.isNotEmpty(summaryIds)) {
                    Map<String, Object> params = new HashMap<String, Object>();
                    StringBuilder hql = new StringBuilder();
                    hql.append(" select summary.id,summary.currentNodesInfo,summary.finishDate,summary.state ");
                    hql.append(" from ColSummary as summary where summary.id in (:summaryIds) ");
                    params.put("summaryIds", summaryIds);
                    List<Object[]> summaryInfoList = DBAgent.find(hql.toString(), params);
                    if (Strings.isNotEmpty(summaryInfoList)) {
                        for (int i = 0; i < summaryInfoList.size(); i++) {
                            int j = 0;
                            ColSummary summaryInfo = new ColSummary();
                            Object[] res = (Object[]) summaryInfoList.get(i);
                            summaryInfo.setId((Long) res[j++]);
                            summaryInfo.setCurrentNodesInfo((String) res[j++]);
                            summaryInfo.setFinishDate((Date) res[j++]);
                            summaryInfo.setState((Integer)res[j++]);
                            colSummaryMap.put(String.valueOf(summaryInfo.getId()), summaryInfo);
        
                        }
                    }
                }
                for (ColSummaryVO colSummaryVo : models) {
                    ColSummary col = colSummaryMap.get(String.valueOf(colSummaryVo.getSummaryId()));
                    //设置当前处理人信息
                    if (col != null) {
                        colSummaryVo.setCurrentNodesInfo(ColUtil.parseCurrentNodesInfo(col));
                        colSummaryVo.setFlowFinished(col.getFinishDate() != null);
                    }
                }
            }
        }
        return models;
    }
    
    /**
     * 将查询的affair集合转换成vo，并且添加代理的特殊处理, 数据关联拼接使用
     * @param result
     * @param showProxyInfo4A
     * @return
     * @throws BusinessException
     */
     private List<ColSummaryVO> convertAffair2VO(List result) throws BusinessException{
         List<ColSummaryVO> models = new ArrayList<ColSummaryVO>();
         if(Strings.isEmpty(result)){
             return models;
         }
         List<Long> summaryIds = new ArrayList<Long>(result.size());
         for(int i = 0; i < result.size(); i++) {
             Object[] object = (Object[]) result.get(i);
             Long summaryId = (Long)object[5];
             summaryIds.add(summaryId);
          }

         for(int i = 0; i < result.size(); i++) {
             Object[] object = (Object[]) result.get(i);
             CtpAffair affair = new CtpAffair();
             affair.setId((Long)object[0]);
             affair.setSubject((String)object[1]);
             affair.setCreateDate((Date)object[2]);
             affair.setReceiveTime((Date)object[3]);
             affair.setUpdateDate(affair.getReceiveTime());
             affair.setSenderId((Long)object[4]);
             affair.setObjectId((Long)object[5]);
             affair.setCaseId(object[6] == null ? null  : ((Number)object[6]).longValue());
             affair.setProcessId((String)object[7]);
             affair.setTempleteId(object[8] == null ? null : ((Number)object[8]).longValue());
             affair.setBodyType((String)object[9]);
             affair.setImportantLevel(object[10] == null ? 1 : ((Number)object[10]).intValue());
             affair.setIdentifier((String)object[11]);
             affair.setMemberId(((Number)object[12]).longValue());
             affair.setFormRecordid(object[13] == null ? null : ((Number)object[13]).longValue());
             affair.setFormAppId(object[14] == null ? null :  ((Number)object[14]).longValue());
             affair.setResentTime(object[15] == null ? null :  ((Number)object[15]).intValue());  
             affair.setForwardMember((String)object[16]);  
             
             // 开始组装最后返回的结果
             ColSummaryVO model = new ColSummaryVO();
             model.setAffairId(affair.getId());
             model.setReceiveTime(affair.getReceiveTime());
             //设置发起人姓名
             model.setStartMemberName(ColUtil.getMemberName(affair.getSenderId()));
             model.setStartMemberId(affair.getSenderId());
             String subject = ColUtil.showSubjectOfAffair(affair, model.isProxy(), -1);
             model.setSubject(subject);
             model.setAffair(affair);
             model.setStartDate(affair.getCreateDate());
             model.setCaseId(affair.getCaseId() + "");
             model.setProcessId(affair.getProcessId());
             model.setSummaryId(String.valueOf(affair.getObjectId()));
             //模版id
             model.setTempleteId(affair.getTempleteId());
             model.setBodyType(affair.getBodyType());
             //重要程度
             model.setImportantLevel(affair.getImportantLevel());
             //创建时间
             model.setCreateDate(affair.getCreateDate());
             //是否有附件
             model.setHasAttsFlag(AffairUtil.isHasAttachments(affair));
             
             // 是否是超级节点
             model.setSuperNode(AffairUtil.isSuperNode(affair));
             
             models.add(model);
          }
          return models;
     }
    
    private String getSubjectIncludeIcon(ColSummary summary) throws BusinessException{
	    StringBuilder sb = new StringBuilder();
	    //流程状态
        if(!Integer.valueOf(0).equals(summary.getState())){
            sb.append("<span class='ico16  flow")
            .append(summary.getState())
            .append("_16 '></span>");
        }
	    if(!Integer.valueOf(1).equals(summary.getImportantLevel())){
	        sb.append("<span class='ico16 important")
	        .append(summary.getImportantLevel())
	        .append("_16 '></span>");
        }
	    sb.append(summary.getSubject());
       
	    //附件
	  
	    boolean isHasAttachments = ColUtil.isHasAttachments(summary);
	    
        if(isHasAttachments){
            sb.append("<span class='ico16 affix_16'></span>") ;
        }
        
        //协同类型
        if(!String.valueOf(MainbodyType.HTML.getKey()).equals(summary.getBodyType())
                &&!String.valueOf(MainbodyType.TXT.getKey()).equals(summary.getBodyType())){
            sb.append("<span class='ico16 office")
            .append(summary.getBodyType())
            .append("_16'></span>");
        }
        
        return sb.toString();
     }
    
    
    /**
     * 原生sql查询字段。用于同一流程显示最后一条
     */
    private static final String selectSqlAffair = "summary.id," +
	        "summary.subject," +
	        "summary.important_level," +
	        "summary.start_member_id," +
	        "summary.forward_member," +
	        "affair.create_date," +
	        "summary.resent_time," +
	        "summary.body_type," +
	        "summary.identifier," +
	        "summary.case_id," +
	        "summary.process_id," +
	        "summary.templete_id," +
	        "summary.state," +
	        "summary.is_cover_time," +
	        "affair.id as affairId," +
	        "affair.state as affairState," +
	        "affair.sub_state," +
	        "affair.track," +
	        "affair.hasten_times," +
	        "affair.is_cover_time as affairCoverTime," +
	        "affair.remind_date as affairRemindDate," +
	        "affair.deadline_date as affairDeadlineDate," +
	        "affair.receive_time as affairReceiveTime," +
	        "affair.complete_time as affairCompleteTime," +
	        "affair.create_date as affairCreateDate," +
	        "affair.member_id as affairMemberId," +
	        "affair.transactor_id as affairTransactorId," +
	        "affair.node_policy as affairNodePolicy," +
	        "affair.identifier as affairIdentifier,"+
	        "affair.sub_object_id as affairSubObjectId,"+
	        "affair.activity_id as affairActivityId,"+
	        "affair.archive_id as affairArchiveId,"+
	        "summary.org_account_id,"+
	        "affair.update_date as affairUpdateDate," +
	        "affair.expected_process_time," +
	        "affair.has_favorite," +
	        "affair.back_from_id as affairBackFromId,"+
	        "summary.form_appid,"+
	        "summary.form_recordid,"+
	        "affair.from_Id,"+
	        "summary.auto_Run,"+
	        "affair.pre_Approver, "+
	        "affair.FORM_OPERATION_ID, "+
	        "affair.PROCESS_DEADLINE_TIME, "+
	        "affair.PROXY_MEMBER_ID, " + 
	        "affair.PRINT_NM, "+
            "summary.can_archive," +
            "summary.finish_date," +
            "summary.deadline," +
            "summary.deadline_datetime," +
            "summary.source," +
            "summary.archive_id," +
            "summary.newflow_type," +
            "summary.current_nodes_info," +
            "summary.parentform_summaryid," +
            "summary.can_edit," +
            "summary.can_forward," +
            "summary.permission_account_id," +
            "summary.reply_counts" ;
    
    private static final String selectAffair = "affair.objectId," +
            "affair.subject," +
            "affair.importantLevel," +
            "affair.senderId," +
            "affair.forwardMember," +
            "affair.createDate," +
            "affair.resentTime," +
            "affair.bodyType," +
            "affair.identifier," +
            "affair.caseId," +
            "affair.processId," +
            "affair.templeteId," +
            "affair.summaryState," +
            "affair.processOverTime," +
            "affair.id," +
            "affair.state," +
            "affair.subState," +
            "affair.track," +
            "affair.hastenTimes," +
            "affair.coverTime," +
            "affair.remindDate," +
            "affair.deadlineDate," +
            "affair.receiveTime," +
            "affair.completeTime," +
            "affair.createDate," +
            "affair.memberId," +
            "affair.transactorId," +
            "affair.nodePolicy," +
            "affair.identifier,"+
            "affair.subObjectId,"+
            "affair.activityId,"+
            "affair.archiveId,"+
            "affair.orgAccountId,"+ 
            "affair.updateDate," +
            "affair.expectedProcessTime," +
            "affair.hasFavorite," +
            "affair.backFromId,"+
            "affair.formAppId,"+
            "affair.formRecordid,"+
            "affair.fromId,"+
            "affair.autoRun,"+
            "affair.preApprover, "+
            "affair.formOperationId, "+
            "affair.processDeadlineTime, "+
            "affair.proxyMemberId, "+
			"affair.print ";
    
    
	private static final String selectAffairAndSummaryLast = ",summary.canArchive,"+
            "summary.finishDate," +
            "summary.deadline," +
            "summary.deadlineDatetime," +
            "summary.source," +
            "summary.archiveId," +
            "summary.newflowType," +
            "summary.currentNodesInfo," +
            "summary.parentformSummaryid," +
            "summary.canEdit," +
            "summary.canForward," +
            "summary.permissionAccountId," +
            "summary.replyCounts" ;
	
	/** 数据关联查询SQL **/
	private static final String sql4DataRelation = new StringBuilder()
			 .append("affair.id,")
 	         .append("affair.subject,")
 	         .append("affair.createDate,")
             .append("affair.receiveTime,")
             .append("affair.senderId,")
             .append("affair.objectId,")
             .append("affair.caseId,")
             .append("affair.processId,")
             .append("affair.templeteId,")
             .append("affair.bodyType,")
             .append("affair.importantLevel,")
             .append("affair.identifier,")
             .append("affair.memberId, ")
             .append("affair.formRecordid, ")
             .append("affair.formAppId, ")
             .append("affair.resentTime, ")
             .append("affair.forwardMember")
            .toString();
	
	@Override
	public void deleteColSummaryById(Long id) {
		String hql = "delete from ColSummary where id= :id";
		Map  pMap = new HashMap();
		pMap.put("id", id);
		DBAgent.bulkUpdate(hql,pMap);
	}
	
	public List<ColSummary> getColSummarysByFormAppId(Long formAppId) throws BusinessException{
	    StringBuilder hql=new StringBuilder();
        hql.append(" from ColSummary where formAppid = :formAppid ");
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("formAppid", formAppId);
        
        @SuppressWarnings("unchecked")
        List<ColSummary> summarys = super.find(hql.toString(),-1,-1, map); 
        return summarys;
	}
	
	@Override
	public ColSummary getColSummaryByProcessId(Long processId) {
		StringBuilder hql=new StringBuilder();
		hql.append("from ColSummary where processId=:processId");
		Map<String,Object> map=new HashMap<String, Object>();
		map.put("processId", processId.toString());
		//List<ColSummary> l = DBAgent.find(hql.toString(), map);
		List<ColSummary> l = super.find(hql.toString(),-1,-1, map); 
		if(Strings.isNotEmpty(l)){
			return l.get(0);
		}
		return null;
	}
    @SuppressWarnings("unchecked")
    public FlipInfo getColSummaryByCondition(FlipInfo flipInfo, Map<String, String> queryMap) throws BusinessException {
        StringBuilder hql = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();

//        hql.append("select a,s from " + ColSummary.class.getName() + " as s," + CtpAffair.class.getName() + " as a where s.id=a.objectId ");
        hql.append("select a,s from " + ColSummary.class.getName() + " as s," + CtpAffair.class.getName() + " as a ");
        Set<String> keys = queryMap.keySet();
    	if(keys.contains(ProjectQueryEnum.author.name())){
    		hql.append(",").append(OrgMember.class.getName()).append(" as mem");
    		 hql.append(" where s.id=a.objectId and a.senderId=mem.id ");
    	}
    	else{
 			 hql.append(" where s.id=a.objectId ");
    	}
        hql.append("and a.memberId=:memberId and a.state in (2,3,4)  and a.delete=false ");
        params.put("memberId", Long.valueOf(queryMap.get(ColQueryCondition.currentUser.name())));
       
        if(keys.contains(ProjectQueryEnum.projectId.name())){
            hql.append(" and s.projectId=:projectId ");
            params.put("projectId", Long.valueOf(queryMap.get("projectId")));
        }
        if (keys.contains(ProjectQueryEnum.title.name())) {
            hql.append(" and s.subject like :title ");
            params.put("title", "%" + SQLWildcardUtil.escape(queryMap.get("title").toString()) + "%");
        }
        if (keys.contains(ProjectQueryEnum.author.name())) {
            String author = queryMap.get("author");
            if(Strings.isNotBlank(author)){
                hql.append(" and mem.name like :author ");
                params.put("author","%" + SQLWildcardUtil.escape(author)+ "%");
            }else{
                hql.append(" and s.startMemberId is null ");
            }
        }
        if (Strings.isNotBlank(queryMap.get("beginTime"))) {
        	hql.append(" and s.createDate>=:begin  ");
        	params.put("begin", Datetimes.parseDatetime(Datetimes.getFirstTimeStr(queryMap.get("beginTime"))));
        }
        if(Strings.isNotBlank(queryMap.get("endTime"))){
        	hql.append("  and s.createDate<=:end ");
        	params.put("end", Datetimes.parseDatetime(Datetimes.getLastTimeStr(queryMap.get("endTime"))));
        }
        
        
        //phase == 1  表示全阶段，只需要通过projectId来过滤就可以了，不需要再根据阶段ID来过滤，projectId的过滤前面已经过滤完毕了，这个地方不需要再单独出来了。
        String phaseIds =  queryMap.get("phaseId");
        if (phaseIds != null && keys.contains(ProjectQueryEnum.phaseId.name()) && !"1".equals(phaseIds)) {
        	Long phaseId = Long.valueOf(phaseIds);
        	List<Long> summaryIds = projectApi.findProjectPhaseEvent(phaseId, ApplicationCategoryEnum.collaboration);
        	if(Strings.isNotEmpty(summaryIds)) {
        		hql.append("and s.id in (:summaryIds) ");
        		params.put("summaryIds", summaryIds.size() > 1000 ? summaryIds.subList(0, 1000) : summaryIds );
        	}else if(keys.contains(ProjectQueryEnum.projectId.name())){
        		//OA-182623
		        hql.append(" and 1=0 ");
	        }
        }
        hql.append("order by a.createDate desc");
        List<CtpAffair> resultList = new ArrayList<CtpAffair>();
        List<Object[]> listAll=  null;
        if(keys.contains(ProjectQueryEnum.author.name())){
        	listAll=  new ArrayList();
    		List<Object[]> find = DBAgent.find(hql.toString(), params,flipInfo);
    		listAll.addAll(find);
        }else{
        	listAll =  DBAgent.find(hql.toString(), params,flipInfo);
        }

        for (Object[] o : listAll) {
            CtpAffair a = (CtpAffair) o[0];
            ColSummary s = (ColSummary) o[1];
            String forwardMember = a.getForwardMember();
            Integer resentTime = a.getResentTime();
            String subject = ColUtil.mergeSubjectWithForwardMembers(a.getSubject(), forwardMember, resentTime, null,-1);
            //TODO a.setCanForward(s.getCanForward());// 是否允许转发
            a.setAddition(subject); // 取协同名称的全部显示信息，放到additon中作为项目标题在前端显示
            resultList.add(a);
        }
        if (flipInfo != null) {
            flipInfo.setData(resultList);
        }
        return flipInfo;
    }
    @Override
    public int getColCount(int state, Long userId,List<Long> templeteIds) throws BusinessException {
        Map<String, String> condition = new HashMap<String,String>();
        condition.put(ColQueryCondition.state.name(), String.valueOf(state));
        condition.put(ColQueryCondition.currentUser.name(), String.valueOf(userId));
        condition.put(ColQueryCondition.templeteIds.name(),Strings.join(templeteIds, ","));
        Object obj = queryByCondition0(new FlipInfo(), condition, true);
        return ((Number)obj).intValue();
    }
    public List<ColSummaryVO>  getTrackList4BizConfig(Long memberId,List<Long> formTemplateIds) throws BusinessException {
        Map<String, Object> params = new HashMap<String, Object>();
        StringBuilder hql = new StringBuilder();
        hql.append(" select ");
        setHqlAndParams(hql, params, false, memberId, formTemplateIds);
        List l = DBAgent.find(hql.toString(), params);
        return convertSelectAffair2ColSummaryModel(l, false,null,true,null);
    }

    private void setHqlAndParams(StringBuilder hql, Map<String, Object> params, boolean queryCount, Long memberId, List<Long> tempIds) {
        hql.append(selectAffair).append( selectAffairAndSummaryLast);
        hql.append(" from CtpAffair as affair, ColSummary as summary where ");
        hql.append(" affair.app=:col and affair.memberId=:memberId and affair.state in (:state) ");
        hql.append(" and affair.delete=:isDelete and affair.archiveId is null and affair.finish=:isFinish and affair.track in (:isTrack)")
           .append(" and affair.objectId=summary.id and summary.templeteId in (:formTempIds)");
        if(!queryCount) {
           hql.append(" order by affair.createDate desc");
        }

        params.put("col", ApplicationCategoryEnum.collaboration.key());
        params.put("memberId", memberId);
        params.put("isFinish", Boolean.FALSE);
        params.put("isDelete", Boolean.FALSE);
        List<Integer> l = new ArrayList<Integer>();
        l.add(TrackEnum.all.ordinal());
        l.add(TrackEnum.part.ordinal());
        params.put("isTrack", l);
        List<Integer> states = new ArrayList<Integer>();
        states.add(StateEnum.col_sent.key());
        states.add(StateEnum.col_done.key());
        params.put("state",states);
        //当列表为空的时候增加一个不可能匹配上的值避免unexpected end of subtree异常
        if(tempIds.size()==0){
        	tempIds.add(-1L);
        }
        params.put("formTempIds", tempIds);
    }
	@Override
	public ColSummary getSummaryByCaseId(Long caseId) throws BusinessException {
		StringBuilder hql=new StringBuilder();
		hql.append("from ColSummary where caseId=:caseId");
		Map<String,Object> params=new HashMap<String, Object>();
		params.put("caseId", caseId);
		List result= DBAgent.find(hql.toString(), params);
		if(null!=result && !result.isEmpty()){
			ColSummary summary= (ColSummary)result.get(0);
			return summary;
		}
		log.warn("caseId:="+caseId);
		return null;
	}
	/**
     * 根据搜索条件condition、field、field1和UserId、Status取得协同督办信息
     * @param userId
     * @param status
     * @return
     */
    public List<SuperviseModelVO> getColSuperviseModelList(FlipInfo filpInfo,Map<String,String> map){
         Set<String> keys = map.keySet();
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        StringBuilder hql = new StringBuilder("select summ.subject,")
                            .append("summ.startMemberId,")
                            .append("summ.startDate,")
                            .append("summ.importantLevel,")
                            .append("summ.deadline,")
                            .append("summ.finishDate,")
                            .append("summ.newflowType,")
                            .append("de.id,")
                            .append("de.awakeDate,")
                            .append("de.count,")
                            //.append("de.supervisors,")
                            .append("de.entityId,")
                            .append("de.description,")
                            .append("de.status,")
                            .append("de.entityType,")
                            .append("summ.resentTime,")
                            .append("summ.forwardMember,")
                            .append("summ.bodyType,")
                            .append("summ.identifier");

        hql.append(" from " ).append( CtpSuperviseDetail.class.getName() ).append( " as de," )
            .append( CtpSupervisor.class.getName() ).append( " as su, " )
            .append( ColSummary.class.getName() ).append( " as summ ");
        if(keys.contains("startMemberName")){
            hql.append(",").append(V3xOrgMember.class.getName()).append(" as mem ");
        }
        hql.append(" where su.superviseId=de.id and de.entityId=summ.id ");

        if(keys.contains("startMemberName")){
            //流程的发起人，不是指谁设置的督办。
            hql.append(" and summ.startMemberId=mem.id and su.supervisorId=:userId and de.entityType=:entityType and de.status=:status ");
        }else if(keys.contains("subject") || keys.contains("importantLevel")){
            hql.append(" and su.supervisorId=:userId and de.entityType=:entityType and de.status=:status ");
        }else{
            hql.append(" and su.supervisorId=:userId and de.entityType=:entityType and de.status=:status ");
        }
        boolean hasTemplete =keys.contains("templeteIds")
                && map.get("templeteIds")!=null
                && Strings.isNotEmpty(Arrays.asList(map.get("templeteIds").split(",")));
        if(hasTemplete){
            hql.append(" and summ.templeteId in(:templeteIds) ");
            List<Long> l = new ArrayList<Long>();
            List<String> ls = Arrays.asList(map.get("templeteIds").split(","));
            for(String s :ls){
                l.add(Long.valueOf(s));
            }
            parameterMap.put("templeteIds",l);
        }
        parameterMap.put("userId", Long.valueOf(map.get("userId").toString()));
        parameterMap.put("entityType",SuperviseEnum.EntityType.summary.ordinal());
        parameterMap.put("status", Integer.valueOf(map.get("status").toString()));

        //标题
        if(keys.contains("subject")){
            hql.append(" and summ.subject like :subject ");
            parameterMap.put("subject", "%" + SQLWildcardUtil.escape(map.get("subject")) + "%");
        }else if(keys.contains("importantLevel")){
            hql.append(" and summ.importantLevel=:importantLevel");
            parameterMap.put("importantLevel", Integer.parseInt(map.get("importantLevel").toString()));
        }else if(keys.contains("startMemberName")){
            hql.append(" and mem.name like :startMemberName");
            parameterMap.put("startMemberName", "%" + SQLWildcardUtil.escape(map.get("startMemberName")) + "%");
        }else if(keys.contains("createDate")){
            String dates = map.get("createDate");
            String[] field = dates.split(",");
            if (StringUtils.isNotBlank(field[0])) {
                Date stamp = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(field[0]));
                hql.append(" and summ.createDate >= :timestamp1");
                parameterMap.put("timestamp1", stamp);
            }
            if (StringUtils.isNotBlank(field[1])) {
                Date stamp =  Datetimes.parseDatetime(Datetimes.getLastTimeStr(field[1]));
                hql.append(" and summ.createDate <= :timestamp2");
                parameterMap.put("timestamp2", stamp);
            }
        }

        hql.append(" order by de.createDate desc");
        List<Object[]> result = DBAgent.find(hql.toString(), parameterMap);
        //List<Object[]> result = DBAgent.find(hql.toString(), parameterMap, filpInfo);
        List<SuperviseModelVO> modelList = null;
        if(result != null && !result.isEmpty()){
            modelList = new ArrayList<SuperviseModelVO>();
            for (int i = 0; i < result.size(); i++) {
                Object[] res = (Object[]) result.get(i);
                SuperviseModelVO model = new SuperviseModelVO();
                int j = 0;
                model.setTitle((String)res[j++]);
                model.setSender((Long)res[j++]);
                Date startDate = (Date)res[j++];
                model.setSendDate(startDate);
                model.setImportantLevel((Integer)res[j++]);
                Long deadline = (Long)res[j++];
                model.setDeadline(deadline);
                Date finishDate = (Date)res[j++];
                //流程是否超期
                if(deadline != null && deadline > 0){
                    Date now = new Date();
                    if(finishDate == null){
                        if((now.getTime()-startDate.getTime()) > deadline*60000){
                            model.setWorkflowTimeout(true);
                        }
                    }
                    else{
                        Long expendTime = finishDate.getTime() - startDate.getTime();
                        if((deadline-expendTime) < 0){
                            model.setWorkflowTimeout(true);
                        }
                    }
                }
                model.setAppType(ApplicationCategoryEnum.collaboration.ordinal());
                model.setNewflowType((Integer)res[j++]);
                Date now = new Date(System.currentTimeMillis());
                model.setId((Long)res[j++]);
                Date awakeDate = (Date)res[j++];
                if(awakeDate != null && now.after(awakeDate)){
                    model.setIsRed(true);
                }
                model.setAwakeDate(awakeDate);
                model.setCount((Long)res[j++]);
                model.setSummaryId((Long)res[j++]);
                model.setContent(Strings.toHTML((String)res[j++]));
                model.setStatus((Integer)res[j++]);
                model.setEntityType((Integer)res[j++]);
                model.setResendTime((Integer)res[j++]);
                model.setForwardMember((String)res[j++]);
                model.setBodyType((String)res[j++]);
                Boolean hasAtt = IdentifierUtil.lookupInner(res[j++].toString(),0, '1');
                model.setHasAttachment(hasAtt);
                modelList.add(model);
            }
        }
        return modelList;
    }

    /**
     * yangwulin Sprint5  2012-12-04  全文检索
     * 获取在某一段时期内，某一板块下已发布的协同总数
     * @param beginDate 开始时间
     * @param endDate 结束时间
     * @param isForm 是否引用表单
     * @return
     * @throws BusinessException
     */
    @Override
    public Integer findIndexResumeCount(Date beginDate, Date endDate,boolean isForm) throws BusinessException {

        StringBuilder sbHql = new StringBuilder("select count(b.id) from ColSummary ");
        sbHql.append(" as b where b.createDate >= ? and b.createDate <= ? ");

        Number result = 0;
        //是否引用表单
        if (isForm) {
            sbHql.append(" and b.bodyType=?");
        }else{
            sbHql.append(" and b.bodyType<>?");
        }
        result = (Number) this.findUnique(sbHql.toString(), null, beginDate,endDate,String.valueOf(MainbodyType.FORM.getKey()));
        return result==null?0:result.intValue();
    }

    /**
     * yangwulin Sprint5  2012-12-04  全文检索
     * @param starDate
     * @param endDate
     * @param firstRow
     * @param pageSize
     * @param isForm 是否引用表单
     * @return
     * @throws BusinessException
     */
    @SuppressWarnings("unchecked")
    public List<Long> findIndexResumeIDList(final Date starDate,final Date endDate,final Integer firstRow,final Integer pageSize,final boolean isForm)
            throws BusinessException {
        return (List<Long>) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                StringBuilder hql = new StringBuilder("select b.id  from ColSummary as b where b.createDate >= ? and b.createDate <= ?");
                //是否引用表单
                if (isForm) {
                    hql.append(" and b.bodyType=?");
                }else{
                    hql.append(" and b.bodyType<>?");
                }
                Query query = null;
                query = session.createQuery(hql.toString());
                query.setParameter(0, starDate);
                query.setParameter(1, endDate);
                query.setParameter(2, String.valueOf(MainbodyType.FORM.getKey()));
                query.setFirstResult(firstRow);
                query.setMaxResults(pageSize);

                return query.list();
            }
        });
    }

	@Override
	public List<ColSummaryVO> getMyCollDeadlineNotEmpty(Map<String,Object> tempMap) throws BusinessException{
		Long currentUserId = (Long) tempMap.get("currentUserID");
		StringBuilder hql = new StringBuilder();
		hql.append("select ");
		hql.append(selectAffair).append(selectAffairAndSummaryLast);
		hql.append(" from ColSummary summary,CtpAffair affair where summary.id=affair.objectId "
				+ "and affair.memberId=:memberId and affair.state in(:affairSate) and affair.delete=0");
				  //"and affair.memberId=:memberId and affair.deadlineDate is not null and affair.state in(:affairSate) and affair.delete=0");

		hql.append(
				" and affair.expectedProcessTime is not null and affair.expectedProcessTime >= :beginDate and affair.expectedProcessTime <= :endDate");
		
		Map<String, Object> params = new HashMap<String, Object>();
		List<Integer> states = new ArrayList<Integer>();
		Date beginDate = (Date) tempMap.get("beginDate");
		Date endDate = (Date) tempMap.get("endDate");
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
		params.put("memberId", currentUserId);
		states.add(StateEnum.col_pending.key());
		params.put("affairSate", states);
		List l = DBAgent.find(hql.toString(), params);

		return convertSelectAffair2ColSummaryModel(l, false, currentUserId,true,null);
	}
	@SuppressWarnings("unchecked")
	public List selectPageWorkflowDataByCondition(Map<String,Object> conditionParam, FlipInfo fi) throws BusinessException{
    	Map<String,Object>  map=buildHql(conditionParam);
		String selectHql=(String) map.get("selectHql");
		Map<String, Object> namedParameterMap=(Map<String, Object>) map.get("namedParameterMap");
        List result = DBAgent.find(selectHql, namedParameterMap,fi);
        List<ColSummary> list = new ArrayList<ColSummary>();
        if(Strings.isNotEmpty(result)){
            for(Object field :result){
                int n = 0;
                Object[] fields  = (Object[]) field;
                ColSummary s = new ColSummary();
                s.setId(((Number)fields[n++]).longValue());
                s.setSubject((String)fields[n++]);
                s.setProcessId(fields[n] == null ? null : ((String)fields[n]));
                n++;
                s.setImportantLevel(fields[n] == null ? null : ((Number)fields[n]).intValue());
                n++;
                s.setState(fields[n] == null ? null : ((Number)fields[n]).intValue());
                n++;
                s.setFinishDate((Date)fields[n++]);
                s.setTempleteId(fields[n] == null ? null : ((Number)fields[n]).longValue());
                n++;
                s.setForwardMember((String)fields[n++]);
                s.setFormRecordid(fields[n] == null ? null : ((Number)fields[n]).longValue());
                n++;
                s.setStartMemberId(fields[n] == null ? null : ((Number)fields[n]).longValue());
                n++;
                s.setOrgAccountId(fields[n] == null ? null : ((Number)fields[n]).longValue());
                n++;
                s.setCaseId(fields[n] == null ? null : ((Number)fields[n]).longValue());
                n++;
                s.setStartDate((Date)fields[n++]);
                s.setResentTime(fields[n] == null ? null : ((Number)fields[n]).intValue());
                n++;
                s.setDeadlineDatetime((Date)fields[n++]);
                s.setAdvanceRemind(fields[n] == null ? null : ((Number)fields[n]).longValue());
                n++;
                s.setNewflowType(fields[n] == null ? null : ((Number)fields[n]).intValue());
                n++;
                s.setBodyType((String)fields[n++]);
                s.setFormAppid(fields[n] == null ? null : ((Number)fields[n]).longValue());
                n++;
                s.setVouch(fields[n] == null ? null : ((Number)fields[n]).intValue());
                n++;
                s.setOrgDepartmentId(fields[n] == null ? null : ((Number)fields[n]).longValue());
                n++;
                s.setCurrentNodesInfo((String)fields[n++]);
                list.add(s);
            }
        }
        return list;
    }
	

	@SuppressWarnings("unchecked")
	public List selectWorkflowDataByCondition(Map<String,Object> conditionParam) throws BusinessException{
	    return selectPageWorkflowDataByCondition(conditionParam,null);
	}
	

	

	 
	 
	 
	 
	
    private String getColSummaryFieldsNotClob() {
        StringBuilder fields = new StringBuilder();
        fields.append("col.id");
        fields.append(",col.subject");
        fields.append(",col.processId");
        fields.append(",col.importantLevel");
        fields.append(",col.state");
        fields.append(",col.finishDate");
        fields.append(",col.templeteId");
        fields.append(",col.forwardMember");
        fields.append(",col.formRecordid");
        fields.append(",col.startMemberId");
        fields.append(",col.orgAccountId");
        fields.append(",col.caseId");
        fields.append(",col.startDate");
        fields.append(",col.resentTime");
        fields.append(",col.deadlineDatetime");
        fields.append(",col.advanceRemind");
        fields.append(",col.newflowType");
        fields.append(",col.bodyType");
        fields.append(",col.formAppid");
        fields.append(",col.vouch");
        fields.append(",col.orgDepartmentId");
        fields.append(",col.currentNodesInfo");

        return fields.toString();
    }
    
    public String getMyManagerTemplateCount() throws BusinessException {
        StringBuilder sb = new StringBuilder("from ColSummary where 1=1 ");
        sb.append(" and bodyType = '20' and  state in (0,1,3) ");
        List<Long> formIdList = new ArrayList<Long>();
        List<CtpTemplate> formTemList = templateManager.getSystemFormTemplates(AppContext.currentUserId());
        if(Strings.isNotEmpty(formTemList)) {
            for(CtpTemplate tem :formTemList) {
                formIdList.add(tem.getId());
            }
        } else {
            return "0";
        }
        sb.append(" and templeteId in (:formIdList) ");
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("formIdList", formIdList);
        int count = DBAgent.count(sb.toString(), params);
        return String.valueOf(count);
    }
    
	private Map<String,Object> buildHql(Map<String,Object> param) throws BusinessException{
		User user=AppContext.getCurrentUser();
		Map<String, Object> namedParameterMap=new HashMap<String, Object>();
        int paramNum=0;
		String hql = " select "+this.getColSummaryFieldsNotClob()+" from ColSummary as col ";
		 //发起人
        String startMemberName = (String) param.get("startMemberName");
        if(Strings.isNotBlank(startMemberName)){
            hql += "," + OrgMember.class.getName() + " as mem";
        }
        
        hql += " where 1=1 ";
        Map<String, Object> params = new HashMap<String, Object>();
        if(Strings.isNotBlank(startMemberName)){
            hql += " and col.startMemberId=mem.id and mem.name like :startMemberName ";
            namedParameterMap.put("startMemberName", "%" + SQLWildcardUtil.escape(startMemberName) + "%");
        }
		
        String sqlStr1 = "";
        String operationTypeIdsStr =(String) param.get("operationTypeIds");
        String operationType = (String) param.get("operationType");
        String[] operationTypeIds = null;
        if(operationTypeIdsStr!=null&&!"".equals(operationTypeIdsStr.trim())){
            operationTypeIds = operationTypeIdsStr.split(",");
        }
        if(operationType!= null){
            if("".equals(operationType.trim()))
                operationType= null;
        }
        String con = (String)param.get("condition");
        int condition = ApplicationCategoryEnum.collaboration.key();
        if(Strings.isNotBlank(con)){
            condition = Integer.parseInt(con);
        }
        String subject = (String) param.get("subject");
        String beginDate = (String) param.get("beginDate");
        String endDate = (String) param.get("endDate");
        String senders = (String) param.get("senders");
        String importantLevel = (String) param.get("importantLevel");
        String deadlineBeginDate = (String) param.get("deadlineBeginDate");
        String deadlineEndDate = (String) param.get("deadlineEndDate");
        String currentNodesInfo = (String) param.get("currentNodesInfo");
        if(Strings.isNotBlank(senders)) {
            Set<V3xOrgMember> members = orgManager.getMembersByTypeAndIds(senders);
            List<V3xOrgMember> memberList = new ArrayList<V3xOrgMember>(members);
            if(Strings.isNotEmpty(memberList)) {
                if(memberList.size() > 1000) {
                    memberList =  memberList.subList(0,1000);
                }
                List<Long> memberIdList = new ArrayList<Long>();
                for(V3xOrgMember mem : memberList) {
                    memberIdList.add(mem.getId());
                }
                sqlStr1 += " and col.startMemberId in (:param"+paramNum + ") ";
                namedParameterMap.put("param"+paramNum, memberIdList);
                paramNum++;
            }
        }
//        String[] currentNodesInfoArr = currentNodesInfo.split(",");
//        List<String> currentNodesInfoList = new ArrayList<String>();
//        if(currentNodesInfoArr != null && currentNodesInfoArr.length>0){
//            for(int i=0; i<currentNodesInfoArr.length; i++){
//                if(!"".equals(currentNodesInfoArr[i])){
//                    currentNodesInfoList.add(currentNodesInfoArr[i]);
//                }
//            }
//        }
//        if(currentNodesInfoList != null && currentNodesInfoList.size() != 0){
//            sqlStr1 += " and state=:param"+paramNum;
//            namedParameterMap.put("param"+paramNum, flowstate);
//            paramNum++;
//        }
        if(user.isAdministrator()){
            sqlStr1 += " and (col.orgAccountId = :orgac or col.permissionAccountId =:orgac) ";
        	namedParameterMap.put("orgac", user.getLoginAccount());
    		paramNum++;
        }
        
        String flow = (String)param.get("flowstate");
        int flowstate = 0;
        if(Strings.isNotBlank(flow)){
            flowstate = Integer.parseInt(flow);
            sqlStr1 += " and col.state=:param"+paramNum;
            namedParameterMap.put("param"+paramNum, flowstate);
            paramNum++;
        } else {
            sqlStr1 += " and col.state in (0,1,3) ";
        }        
        
        if("template".equals(operationType)){
        	if(operationTypeIds!=null&&!"".equals(operationTypeIds[0])){
	    		for(int i=0; i<operationTypeIds.length; i++){
	        		Long templeteId = Long.parseLong(operationTypeIds[i]);
		        	if(operationTypeIds.length == 1){
		        		sqlStr1 += " and (col.templeteId=:param"+paramNum+")";
	        			namedParameterMap.put("param"+paramNum, templeteId);
	            		paramNum++;
		        	}else{
		        		if(i == 0){
			        		sqlStr1 += " and (col.templeteId=:param"+paramNum+" or";
			        		namedParameterMap.put("param"+paramNum, templeteId);
		            		paramNum++;
			        	}else if(i == (operationTypeIds.length-1)){
			        		sqlStr1 += " col.templeteId=:param"+paramNum+")";
			        		namedParameterMap.put("param"+paramNum, templeteId);
		            		paramNum++;
			        	}else{
			        		sqlStr1 += " col.templeteId=:param"+paramNum+" or";
			        		namedParameterMap.put("param"+paramNum, templeteId);
		            		paramNum++;
			        	}
		        	}
	    		}
        	} else if ("formAdmin".equals( String.valueOf(param.get("managerType")))){
        	    List<Long> formIdList = (List) param.get("formIdList");
        	    if(Strings.isNotEmpty(formIdList)) {
        	    	List<Long> formTemplateIs = formIdList;
        	    	//某个人管理的模板超过1000个时只取1000个模板查询。oracle下 in不能 超过1000
        	        if(formIdList.size() > 1000) {
                        formTemplateIs = formIdList.subList(0, 999);
        	        } 
        	    	sqlStr1 += " and col.templeteId in (:param"+paramNum + ") ";
        	        namedParameterMap.put("param"+paramNum, formTemplateIs);
        	        paramNum++;
        	    }
        	}else{
        		sqlStr1 += " and col.templeteId is not null ";
	    	}
        }else if("self".equals(operationType)){
    		sqlStr1 += " and col.templeteId is null ";
        }

    	if (Strings.isNotBlank(subject)) {
			sqlStr1 += " and (col.subject like :param"+paramNum+ SQLWildcardUtil.setEscapeCharacter() +")";
            namedParameterMap.put("param"+paramNum, "%" + SQLWildcardUtil.escape(subject) + "%");
    		paramNum++;
        }
        if (Strings.isNotBlank(beginDate)) {
    		sqlStr1 += " and col.createDate >= :param"+paramNum;
            Date stamp = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(beginDate));
            namedParameterMap.put("param"+paramNum, stamp);
    		paramNum++;
        }
        if (Strings.isNotBlank(endDate)){
    		sqlStr1 += " and col.createDate <= :param"+paramNum;
            Date stamp = Datetimes.parseDatetime(Datetimes.getLastTimeStr(endDate));
            namedParameterMap.put("param"+paramNum, stamp);
    		paramNum++;
        }
        
        if (Strings.isNotBlank(deadlineBeginDate)) {
            sqlStr1 += " and col.deadlineDatetime >= :param"+paramNum;
            Date stamp = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(deadlineBeginDate));
            namedParameterMap.put("param"+paramNum, stamp);
            paramNum++;
        }
        if (Strings.isNotBlank(deadlineEndDate)){
            sqlStr1 += " and col.deadlineDatetime <= :param"+paramNum;
            Date stamp = Datetimes.parseDatetime(Datetimes.getLastTimeStr(deadlineEndDate));
            namedParameterMap.put("param"+paramNum, stamp);
            paramNum++;
        }
        if (Strings.isNotBlank(importantLevel)) {
            sqlStr1 += " and (col.importantLevel = :param"+paramNum+") ";
            namedParameterMap.put("param"+paramNum, Integer.valueOf(importantLevel));
            paramNum++;
        }
        
        if(!"deptAdmin".equals( String.valueOf(param.get("managerType"))) && !"formAdmin".equals( String.valueOf(param.get("managerType")))) {
            if (condition == ApplicationCategoryEnum.collaboration.key()) {
                sqlStr1 += " and col.bodyType <> '20'";
            } else {
                sqlStr1 += " and col.bodyType = '20'";
            }            
        }
		
		sqlStr1 += " and  col.caseId is not null and col.processId is not null  ";
		
        String selectHql =hql + sqlStr1 +" order by col.createDate desc";
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("selectHql", selectHql);
        map.put("namedParameterMap", namedParameterMap);
        return map;
	}

    @Override
    public void transSetFinishedFlag(ColSummary summary) throws BusinessException {
        Timestamp finishDate = new Timestamp(System.currentTimeMillis());
        summary.setFinishDate(finishDate);
        StringBuilder sb  = new StringBuilder();
        sb.append("UPDATE " + ColSummary.class.getCanonicalName() + " set finishDate=? , ");
        sb.append("overTime = ?, overWorktime = ?,");
        sb.append("runTime = ? ,runWorktime = ?, state=? ");
        sb.append("where id=? ");
        super.bulkUpdate(sb.toString(), null, finishDate,
                summary.getOverTime() == null? 0:summary.getOverTime(),
                summary.getOverWorktime() == null ? 0:summary.getOverWorktime(),
                summary.getRunTime() == null ? 0: summary.getRunTime(),
                summary.getRunWorktime() == null ?0 :summary.getRunWorktime(),
                summary.getState(),summary.getId());
    }

    
  

	@Override
	public List<ColSummaryVO> getArchiveAffair(FlipInfo flipInfo,
			Map<String, String> query) throws BusinessException {
		StringBuilder hql = new StringBuilder();
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		hql.append("select ");
		hql.append(selectAffair).append(selectAffairAndSummaryLast);
		hql.append(" from CtpAffair as affair,ColSummary as summary,DocResourcePO dr ") ;
		hql.append(" where affair.objectId=summary.id and affair.id = dr.sourceId and dr.favoriteSource is null ");
		hql.append(" and affair.app = :queryApp ");
        parameterMap.put("queryApp",ModuleType.collaboration.getKey());

        Long userId = null;
		String userIdStr = query.get(ColQueryCondition.currentUser.name());
        if(Strings.isNotBlank(userIdStr)){
            userId = Long.valueOf(userIdStr);
        }

        if(userId != null){
        	hql.append(" and affair.memberId=:memberId ");
        	parameterMap.put("memberId", userId);
        }
        //类型：是否超期
        String isCoverTime = query.get(ColQueryCondition.coverTime.name());
        if(Strings.isNotBlank(isCoverTime)){
        	hql.append(" and summary.coverTime=:coverTime");
            if("1".equals(isCoverTime)){
                parameterMap.put("coverTime",true);
            }else{
                parameterMap.put("coverTime",false);
            }
        }
        String statisticDate = query.get("statisticDate");
    	Date stamp1 = null;
    	Date stamp2 = null;
    	if (Strings.isNotBlank(statisticDate)) {
            String[] date = statisticDate.split("#");
            if(date != null && date.length > 0){
                if (Strings.isNotBlank(date[0])) {
                    stamp1 = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(date[0]));
                }
                if(date.length > 1){
                    if (Strings.isNotBlank(date[1])) {
                        stamp2 = Datetimes.parseDatetime(Datetimes.getLastTimeStr(date[1]));
                    }
                }
            }
    	}
    	if (stamp1 != null) {
            hql.append(" and dr.createTime >= :timestamp1");
            parameterMap.put("timestamp1", stamp1);
        }
		if (stamp2 != null) {
            hql.append(" and dr.createTime <= :timestamp2");
            parameterMap.put("timestamp2", stamp2);
        }
		hql.append(" order by affair.createDate desc");
		List list =  DBAgent.find(hql.toString(), parameterMap,flipInfo);

		return this.convertSelectAffair2ColSummaryModel(list, false,null,true,null);
	}

	public List<ColSummary> findColSummarysByIds(List<Long> ids) throws BusinessException{
	    
	    if(Strings.isEmpty(ids)){
	        return new ArrayList<ColSummary>();
	    }
	    
		String hql = "from ColSummary where id in(:ids) order by startDate desc";
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("ids", ids);
		return DBAgent.find(hql.toString(), parameterMap,null);
	}
	
	public List<ColSummary> findColSummarysByParentId(Long parentId) throws BusinessException{
		if(parentId == null){
	        return new ArrayList<ColSummary>();
	    }
	    
		String hql = "from ColSummary where parentformSummaryid = :parentformSummaryid ";
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("parentformSummaryid", parentId);
		return DBAgent.find(hql.toString(), parameterMap,null);
	}
	
	public List<ColSummaryVO> getSummaryByTemplateId(Long templateId,Integer state)throws BusinessException{
		StringBuilder hql =new StringBuilder();
		hql.append("select ");
		hql.append(selectAffair).append(selectAffairAndSummaryLast);
		hql.append(" from CtpAffair as affair,ColSummary as summary");
		hql.append(" where summary.id = affair.objectId  And summary.templeteId =:templteId And affair.state=:state ");
		Map parameterMap = new HashMap();
		parameterMap.put("templteId", templateId);
		parameterMap.put("state", state);

		List list =  DBAgent.find(hql.toString(), parameterMap);
		list = this.convertSelectAffair2ColSummaryModel(list, false,null,true,null);
		return list;
	}
	
	@SuppressWarnings("unchecked")
    @Override
	public List<ColSummary> findColSummarys(QuerySummaryParam param, FlipInfo flip) {

	    Map<String, Object> queryMap = new HashMap<String, Object>();
	    
	    StringBuilder hql = new StringBuilder("from ");
	    hql.append(ColSummary.class.getName())
	       .append(" where 1=1 ");
	    
	    //运行时长
	    Long minRunWorktime = param.getMinRunWorktime();
	    Long maxRunWorktime = param.getMaxRunWorktime();
	    if(minRunWorktime != null && maxRunWorktime != null){
	        hql.append(" and runWorktime between :minRunWorktime and :maxRunWorktime ");
	        queryMap.put("minRunWorktime", minRunWorktime);
	        queryMap.put("maxRunWorktime", maxRunWorktime);
	    }else if(minRunWorktime != null){
	        hql.append(" and runWorktime >= :minRunWorktime ");
	        queryMap.put("minRunWorktime", minRunWorktime);
	    }else if(maxRunWorktime != null){
	        hql.append(" and runWorktime <= :maxRunWorktime ");
            queryMap.put("maxRunWorktime", maxRunWorktime);
	    }
	    
	    //是否超期
	    Boolean isOverTime = param.getIsOverTime();
	    if(isOverTime != null){
	        hql.append(" and coverTime = :coverTime ");
	        queryMap.put("coverTime", isOverTime);
	    }
		//添加关联ID 查询
		Long relationId = param.getRelationId();
		if(relationId != null){
			hql.append(" and relationId = :relationId ");
			queryMap.put("relationId", relationId);
		}
	    
	    //开始时间
	    Date startBeginTime = param.getStartBeginTime();
	    Date startEndTime = param.getStartEndTime();
	    if(startBeginTime != null && startEndTime != null){
	        
	        hql.append(" and startDate between :startBeginTime and :startEndTime ");
            queryMap.put("startBeginTime", startBeginTime);
            queryMap.put("startEndTime", startEndTime);
	    }else if(startBeginTime != null){
	        hql.append(" and startDate >= :startBeginTime ");
            queryMap.put("startBeginTime", startBeginTime);
	    }else if(startEndTime != null){
	        hql.append(" and startDate <= :startEndTime ");
            queryMap.put("startEndTime", startEndTime);
	    }
	    
	    //结束时间
	    Date finishBeginTime = param.getFinishBeginTime();
        Date finishEndTime = param.getFinishEndTime();
        if(finishBeginTime != null && finishEndTime != null){
            
            hql.append(" and finishDate between :finishBeginTime and :finishEndTime ");
            queryMap.put("finishBeginTime", finishBeginTime);
            queryMap.put("finishEndTime", finishEndTime);
        }else if(finishBeginTime != null){
            hql.append(" and finishDate >= :finishBeginTime ");
            queryMap.put("finishBeginTime", finishBeginTime);
        }else if(finishEndTime != null){
            hql.append(" and finishDate <= :finishEndTime ");
            queryMap.put("finishEndTime", finishEndTime);
        }
        
        //模板ID
        Long templateId = param.getTemplateId();
        if(templateId != null){
            hql.append(" and templeteId = :templeteId ");
            queryMap.put("templeteId", templateId);
        }
        
        //所属单位
        Long accountId = param.getAccountId();
        if(accountId != null){
            hql.append(" and orgAccountId = :orgAccountId ");
            queryMap.put("orgAccountId", accountId);
        }
        
        //状态
        List<CollaborationEnum.flowState> states = param.getStates();
        if(Strings.isNotEmpty(states)){
            List<Integer> sList = new ArrayList<Integer>();
            for(CollaborationEnum.flowState state : states){
                sList.add(state.ordinal());
            }
            hql.append(" and state in (:states) ");
            queryMap.put("states", sList);
        }
        
        boolean onlyQueryCount = param.isOnlyQueryCount();
        if(onlyQueryCount){
            int count = DBAgent.count(hql.toString(), queryMap);
            if(flip != null){
                flip.setTotal(count);
            }
            return new ArrayList<ColSummary>(0);
        }else {
            
          //排序
            List<SortFieldEnum> sortField = param.getSortField();
            if(Strings.isNotEmpty(sortField)){
                hql.append(" order by ");
                for(int i = 0, len = sortField.size(); i < len; i++){

                    SortFieldEnum s = sortField.get(i);
                    String field = s.getFiled();
                    String type = s.getType();
                    
                    if(i > 0){
                        hql.append(",");
                    }
                    hql.append(field).append(" ").append(type);
                }
            }
            return DBAgent.find(hql.toString(), queryMap, flip);
        }
	}

	/**
     * 获取绩效穿透已办和已发的查询条件
     * @Author      : xuqw
     * @Date        : 2015年10月10日下午12:02:10
     * @param condition
     * @return
     */
    private String getStatisticHql4SendAndComplete(Map<String, String> condition, Map<String, Object> parameterMap, String alias){
        
        StringBuilder hql = new StringBuilder();
        
        String state = condition.get(ColQueryCondition.state.name());
        String statistic = condition.get("statistic");
        
      //时间区间
        String dealDates = condition.get("statisticDate");
        
        if("true".equals(statistic) && "2,4".equals(state) && Strings.isNotBlank(dealDates)){
            String[] date = dealDates.split("#");
            if(date != null && date.length > 0){
                
                Date begin = null;
                Date end = null;
                if (Strings.isNotBlank(date[0])) {
                    begin = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(date[0]));
                    
                }
                if(date.length > 1 && Strings.isNotBlank(date[1])){
                    end = Datetimes.parseDatetime(Datetimes.getLastTimeStr(date[1]));
                }
                if(begin != null){
                    
                    parameterMap.put("staticTimestamp1", begin);
                    parameterMap.put("staticTimestamp2", end);
                    
                    hql.append(" and (");
                    
                    hql.append("(");
                    hql.append(alias).append(".state=:staticAffairState4 ");
                    parameterMap.put("staticAffairState4", StateEnum.col_done.getKey());
                    
                    hql.append(" and (").append(alias).append(".completeTime >= :staticTimestamp1");
                    if(end != null){
                        hql.append(" and ").append(alias).append(".completeTime <= :staticTimestamp2");
                    }
                    hql.append(")");
                    
                    hql.append(")");
                    
                    hql.append(" or ");
                    
                    hql.append("(");
                    
                    hql.append(alias).append(".state=:staticAffairState2 ");
                    parameterMap.put("staticAffairState2", StateEnum.col_sent.getKey());
                    
                    hql.append(" and (").append(alias).append(".createDate >= :staticTimestamp1");
                    if(end != null){
                        hql.append(" and ").append(alias).append(".createDate <= :staticTimestamp2");
                    }
                    hql.append(")");
                    
                    hql.append(")");
                    
                    hql.append(") ");
                }
            }
        }
        return hql.toString();
    }
    
    /**
     * 获取绩效穿透超期的查询条件
     * @Author      : xuqw
     * @Date        : 2015年10月10日下午12:02:10
     * @param condition
     * @return
     */
    private String getStatisticHql4CoverTime(Map<String, String> condition, Map<String, Object> parameterMap, String alias){
        
        StringBuilder hql = new StringBuilder();
        
        String statistic = condition.get("statistic");
        String isCoverTime = condition.get(ColQueryCondition.coverTime.name());
        
      //时间区间    
        String dealDates = condition.get("statisticDate");
        
        if("true".equals(statistic) && Strings.isNotBlank(isCoverTime) && Strings.isNotBlank(dealDates)){
            String[] date = dealDates.split("#");
            if(date != null && date.length > 0){
                
                Date begin = null;
                Date end = null;
                if (Strings.isNotBlank(date[0])) {
                    begin = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(date[0]));
                    
                }
                if(date.length > 1 && Strings.isNotBlank(date[1])){
                    end = Datetimes.parseDatetime(Datetimes.getLastTimeStr(date[1]));
                }
                if(begin != null){
                    
                    parameterMap.put("staticTimestamp1", begin);
                    parameterMap.put("staticTimestamp2", end);
                    
                    hql.append(" and (");
                    
                    hql.append("(");
                    hql.append(alias).append(".state=:staticAffairState3 ");
                    parameterMap.put("staticAffairState3", StateEnum.col_pending.getKey());
                    hql.append(")");
                    hql.append(" or ");
                    hql.append("(");
                    hql.append(alias).append(".state=:staticAffairState4 ");
                    parameterMap.put("staticAffairState4", StateEnum.col_done.getKey());
                    
                    hql.append(" and (").append(alias).append(".completeTime >= :staticTimestamp1");
                    if(end != null){
                        hql.append(" and ").append(alias).append(".completeTime <= :staticTimestamp2");
                    }
                    hql.append(")");
                    
                    hql.append(")");
                    
                    hql.append(") ");
                }
            }
        }
        return hql.toString();
    }
	
    /**
     * 
     * @param condition
     * @param parameterMap
     * @param affairMemberId 
     * @param states
     * @param hasAgent
     * @param agentMemberId 如果不为空， 只代理指定人的数据， 为空代理所有人数据
     * @param formRelation
     * @param alias
     * @param joinChart 和代理查询的连接字符(只能是AND 和  OR)
     * @return 
     */
	private StringBuilder getChildrenFilterHql(Map<String, String> condition, Map<String, Object> parameterMap, Long userId,
			List<Integer> states, boolean hasAgent,Long agentMemberId, String formRelation, String alias ) {
		
		StringBuilder childrenHql = new StringBuilder();
		if(states.size() == 1){
			childrenHql.append(" and ").append(alias).append(".state = :affairState ");
			parameterMap.put("affairState", states.get(0));
		}else if(states.size() > 1){
			childrenHql.append(" and ").append(alias).append(".state in(:affairState) ");
			parameterMap.put("affairState", states);
		}
		
		childrenHql.append(" and ").append(alias).append(".app = :queryApp ");
		parameterMap.put("queryApp",ModuleType.collaboration.getKey());
		
		//不是穿透查询和关联流程时 ，添加过滤
		if(Strings.isBlank(condition.get("statistic")) && Strings.isBlank(formRelation)){
			childrenHql.append(" and ").append(alias).append(".delete = :queryDelete ");
			parameterMap.put("queryDelete", Boolean.FALSE);
//		    if(!states.contains(StateEnum.col_waitSend.key()) && condition.get(ColQueryCondition.archiveId.name()) == null){//已归档的待发需要抽取出来
//		    	childrenHql.append(" and ").append(alias).append(".archiveId is null");
//		    }
		}
		StringBuilder completeTimeQueryHql1 = getCompleteTimeQueryHql(alias,condition, parameterMap);
		childrenHql.append(completeTimeQueryHql1);
		
		return childrenHql;
	}

	/**
	 * 
	 * @param userId
	 * @param alias
	 * @param parameterMap
	 * @param hasAgent
	 * @param agentMemberId 如果不为空， 只代理指定人（指定被代理人）的数据， 为空代理所有人数据
	 * @param states
	 * @return
	 */
	private StringBuilder getMemberIdIncludeAgent(long currentUserId , String alias,Map<String, Object> parameterMap , boolean hasAgent){
		StringBuilder sb = new StringBuilder();
        if(hasAgent){//存在代理
            sb.append("OR "+alias+".proxyMemberId= :proxyMemberId1 ");
            parameterMap.put("proxyMemberId1", currentUserId);
        }
        return sb;
	}
	

	private StringBuilder getCompleteTimeQueryHql(String alias ,Map<String, String> condition, Map<String, Object> parameterMap) {

		StringBuilder hql = new StringBuilder();
		//已办按处理时间查询
		String dealDates = condition.get(ColQueryCondition.dealDate.name());
		if(dealDates != null){
		    String[] date = dealDates.split("#");
		    if(date != null && date.length > 0){
		        if (Strings.isNotBlank(date[0])) {
		        	Date stamp = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(date[0]));
		        	if("aiProcess".equals(condition.get("openFrom"))) {
		        		stamp = Datetimes.parseDatetime(date[0]);
		        	}
		            hql.append(" and ").append(alias).append(".completeTime >= :timestamp1");
		            parameterMap.put("timestamp1", stamp);
		        }
		        if(date.length > 1){
		            if (Strings.isNotBlank(date[1])) {
		                Date stamp = Datetimes.parseDatetime(Datetimes.getLastTimeStr(date[1]));
		                if("aiProcess".equals(condition.get("openFrom"))) {
			        		stamp = Datetimes.parseDatetime(date[1]);
			        	}
		                hql.append(" and ").append(alias).append(".completeTime <= :timestamp2");
		                parameterMap.put("timestamp2", stamp);
		            }
		        }
		    }
		} 
		return hql;
	}
	
	public Map<String,Integer> getAffairsCount() {
		Map<String,Integer> map = new HashMap<String,Integer>();
		return map;
	}
	public void updateColSummaryProcessNodeInfos(String processId,String processNodesInfo){       
		String s  = "update ColSummary set processNodesInfo = :processNodesInfo where processId = :processId ";
		Map<String,Object> m  = new HashMap<String,Object>();
		m.put("processId",processId);
		m.put("processNodesInfo",processNodesInfo);
		DBAgent.bulkUpdate(s,m);
	}
	public void updateColSummaryProcessNodeInfos(Long summaryId,String processNodesInfo){       
		String s  = "update ColSummary set processNodesInfo = :processNodesInfo where id = :summaryId ";
		Map<String,Object> m  = new HashMap<String,Object>();
		m.put("summaryId",summaryId);
		m.put("processNodesInfo",processNodesInfo);
		DBAgent.bulkUpdate(s,m);
	}
	public void updateColSummaryReplyCounts(Long summaryId,int replyCounts){       
        String s  = "update ColSummary set replyCounts = :replyCounts where id = :id ";
        Map<String,Object> m  = new HashMap<String,Object>();
        m.put("replyCounts",replyCounts);
        m.put("id",summaryId);
        DBAgent.bulkUpdate(s,m);
    }

	@Override
	public FlipInfo getSummarysAndTemplates(FlipInfo flipInfo,Map<String, Object> condition)
			throws BusinessException {
		StringBuilder sb = new StringBuilder();
		 Map<String,Object> m  = new HashMap<String,Object>();
		 sb.append("select s.id as summaryId,s.subject as summarySubject,s.start_member_id as startMemberId,s.create_date as createDate,s.templete_id as templeteId,t.subject as templeteSubject,o.name as name from col_summary s, ctp_template t, org_member o where t.id=s.templete_id and t.id = :templateId and o.id = s.start_member_id ");
		m.put("templateId", condition.get("templateId"));
		if(condition.containsKey("subject")) {
			sb.append(" and s.subject like :subject ");
			m.put("subject", "%"+SQLWildcardUtil.escape(String.valueOf(condition.get("subject")))+"%");
		}
		if(condition.containsKey("startMemberId")) {
			String startMemberId = (String) condition.get("startMemberId");
			String[] ids = startMemberId.split(",");
			List<Long> idList = new ArrayList<Long>();
			for(int i=0;i<ids.length;i++) {
				if(Strings.isNotBlank(ids[i])) {
					idList.add(Long.parseLong(ids[i]));
				}
			}
			sb.append(" and s.start_member_id in (:startMemberId) ");
			m.put("startMemberId", idList);
		}
		if(condition.containsKey("createDate")) {
			String createDate = (String) condition.get("createDate");
			if (Strings.isNotBlank(createDate)) {
	            String[] date = createDate.split("#");
	            if(date != null && date.length > 0){
	                if (Strings.isNotBlank(date[0])) {
	                    Date stamp = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(date[0]));
	                    sb.append(" and s.create_date >= :timestampC1 ");
	                    m.put("timestampC1", stamp);
	                }
	                if(date.length > 1){
	                    if (Strings.isNotBlank(date[1])) {
	                        Date stamp = Datetimes.parseDatetime(Datetimes.getLastTimeStr(date[1]));
	                        sb.append(" and s.create_date <= :timestampC2 ");
	                        m.put("timestampC2", stamp);
	                    }
	                }
	            }
	        }
		}
		sb.append(" order by s.create_date desc");
		JDBCAgent jdbc = new JDBCAgent();
        try {
            flipInfo  = jdbc.findNameByPaging(sb.toString(),m, flipInfo);
		} catch (Exception e) {
			logger.error("数据查询错误！",e);
		} finally {
			jdbc.close();
		}
		return flipInfo;
	}
	
    public List getOverdueOrDaysOverdueList(Long userId,List<Long> templateIds) throws BusinessException{
        StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<String, Object>();
        hql.append("select affair.expectedProcessTime ");
        hql.append(" from CtpAffair affair where affair.memberId=:memberId and affair.state =3 and affair.delete=0 and affair.app = 1 ");
        if(templateIds.size() >0){//注意添加顺序
			hql.append("  and affair.templeteId in (:tempalteId) ");
			params.put("tempalteId", templateIds);
		}
        hql.append(" and affair.expectedProcessTime  <= :sevenDatDate ");
        Date sevenDatDate = DateUtil.newDate();
        Calendar c = Calendar.getInstance();  
        c.add(Calendar.DAY_OF_MONTH, daysOverDue);
        sevenDatDate = c.getTime();
        params.put("sevenDatDate", sevenDatDate);
        params.put("memberId", userId);

        List l = DBAgent.find(hql.toString(), params);
        return l;
    }
    
    public int getFromleader(List<Long> templeteIds) throws BusinessException{
    	//按领导发的查询
    	Map<String,Object> parameterMap = new HashMap<String, Object>();
    	String hql = "select count(*) from CtpAffair a where a.memberId=:memberId and a.state=3 and a.delete=0 and a.app=1 and a.senderId in (:senderId) ";
		if(templeteIds.size() >0){//注意添加顺序
			hql = hql+"  and a.templeteId in (:templeteId) ";
			parameterMap.put("templeteId", templeteIds);
		}
         List<V3xOrgMember> members = new ArrayList<V3xOrgMember>();
         try {
             members = orgManager.getMembersByDepartmentRoleOfAll(AppContext.getCurrentUser().getDepartmentId(), Role_NAME.DepManager.name());
             members.addAll(orgManager.getMembersByDepartmentRoleOfAll(AppContext.getCurrentUser().getDepartmentId(), Role_NAME.DepLeader.name()));
         } catch (Exception e) {
             log.error("", e); 
         }
         List<Long> ids = new ArrayList<Long>();
         if (Strings.isNotEmpty(members)) {
             for (V3xOrgMember m : members) {
                 if (!m.getId().equals(AppContext.getCurrentUser().getId())) {// 当自己是领导或分管领导，排除掉自己
                     ids.add(m.getId());
                 }
             }
         }
         if(Strings.isEmpty(ids)){
             ids.add(-1L);
         }
         parameterMap.put("memberId", AppContext.currentUserId());
         parameterMap.put("senderId",ids);
        List l = DBAgent.find(hql.toString(), parameterMap);
        int count = 0;
        if(Strings.isNotEmpty(l)) {
        	count = ((Number)l.get(0)).intValue();
        }
        return count;
    }
    
    public int getMydeptDataCount(List<Long> templeteIds) throws BusinessException{
    	Map<String,Object> parameterMap = new HashMap<String, Object>();
    	StringBuilder hql = new StringBuilder();
    	hql.append("select count(*) from CtpAffair a where a.memberId=:memberId and a.state=3 and a.delete=0 and a.app=1 ");
		if(templeteIds.size() >0){//注意添加顺序
			hql.append("  and a.templeteId in (:templeteId) ");
			parameterMap.put("templeteId", templeteIds);
		}
    	parameterMap.put("memberId", AppContext.currentUserId());
    	 List<V3xOrgMember> myDeptMembers = new ArrayList<V3xOrgMember>();
         try {
             myDeptMembers = orgManager.getMembersByDepartment(AppContext.getCurrentUser().getDepartmentId(), true);
         } catch (BusinessException e) {
             log.error("", e);
         }

         List<Long> myDeptMembersIds = new ArrayList<Long>();
         if (Strings.isNotEmpty(myDeptMembers)) {
             for (V3xOrgMember m : myDeptMembers) {
                 myDeptMembersIds.add(m.getId());
             }
         }
         if (Strings.isNotEmpty(myDeptMembersIds)) {
             if(myDeptMembersIds.size() > 999) {
                 List<Long>[] idsArr = Strings.splitList(myDeptMembersIds, 999);
                 
                 myDeptMembersIds = idsArr[0];
             }
             
             hql.append(" and a.senderId in (:senderId)");
             parameterMap.put("senderId", myDeptMembersIds);                 
         } else {
             hql.append(" and a.senderId is null ");
         }
         
         if(log.isDebugEnabled()) {
             if(Strings.isNotEmpty(myDeptMembersIds)) {
                 log.debug("myDeptMembersIds size : " + myDeptMembersIds.size());    
             }
             log.debug("hql : " + hql);
         }
         
         List l = DBAgent.find(hql.toString(), parameterMap);
         int count = 0;
         if(Strings.isNotEmpty(l)) {
         	count = ((Number)l.get(0)).intValue();
         }
         return count;
    }

}
