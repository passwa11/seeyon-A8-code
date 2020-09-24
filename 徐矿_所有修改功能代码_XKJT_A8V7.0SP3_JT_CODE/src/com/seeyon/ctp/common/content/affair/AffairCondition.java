/**
 * $Author: mujun $
 * $Rev: 6160 $
 * $Date:: 2012-11-08 12:22:54#$:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.common.content.affair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.type.Type;

import com.seeyon.apps.agent.bo.AgentDetailModel;
import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.config.IConfigPublicKey;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.affair.constants.TrackEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UniqueList;


/**
 * <p>Affair事项表查询。</p>
 * <p>传入参数集合。将结果返回</p>
 * <p>传入的参数分为<b>索引项</b>和<b> 非索引项</b></p>
 * <p>索引具体排列见 {@link com.seeyon.v3x.affair.manager.impl.AffairManagerImpl}</p>
 * <p>基本是按照第二条索引来进行组装的.索引如下：</p>
 * member_id, state, app, is_delete, archive_id, is_finish, is_track
 */
public class AffairCondition {
	private static final Log log = CtpLogFactory.getLog(AffairCondition.class);
	private Long memberId;
	private StateEnum state;
	private List<ApplicationCategoryEnum> apps = new ArrayList<ApplicationCategoryEnum>();
	private boolean isDelete = false;
	private Long archiveId;
	private Boolean isFinish = false;
	private Boolean isTrack = null;
	private Boolean isSourcesRelationOr=false;//首页栏目组合查询and or的选择
	private String[] selectColumns;
	private Boolean showAgentAffair = false;
	private Boolean vjoin = false;//移动端栏目
	
	/**
     * 设置
     * 我是代理人的情况下，将我的代办和我代理的代办一起查出来。
     * 在移动应用的时候使用到过。
     */
    private boolean containAgent = false;
   
	/** 
     * true  被代理  false 代理   null  没有代理
     */
    private Boolean agentToFlag;
    private Map<Integer,List<AgentModel>> agentList;
    private List<AgentModel> agentModelList;
    private Set<SearchModel> searchList = new HashSet<SearchModel>();
    
    //不需要做缓存
    private static List<String> auditPopedom = new ArrayList<String>();
    private static List<String> readPopedom = new ArrayList<String>();
    private Boolean trackFlag=false;//跟踪栏目
    static{
        auditPopedom.add("formaudit");
        auditPopedom.add("shenpi");
        auditPopedom.add("shenhe");
        auditPopedom.add("qianfa");
        auditPopedom.add("huiqian");
        auditPopedom.add("fuhe");

        readPopedom.add("inform");
        readPopedom.add("read");
        readPopedom.add("zhihui");
        readPopedom.add("yuedu");
    }
    public AffairCondition(){
        
    }
    public AffairCondition(Long memberId,StateEnum state,ApplicationCategoryEnum... apps){
        this.memberId = memberId;
        this.state = state;
        if(apps != null){
            for(ApplicationCategoryEnum app : apps){
                this.apps.add(app);
            }
        }
    }
    
    public AffairCondition(Long memberId,StateEnum state,List<ApplicationCategoryEnum>apps){
        this.memberId = memberId;
        this.state = state;
        if(apps != null){
            for(ApplicationCategoryEnum app : apps){
                this.apps.add(app);
            }
        }
    }
    
    public void setApp(List<ApplicationCategoryEnum> apps) {
    	if(apps != null){
            for(ApplicationCategoryEnum app : apps){
                this.apps.add(app);
            }
        }
    }
    
    public void setShowAgentAffair(Boolean showAgentAffair){
    	this.showAgentAffair = showAgentAffair;
    }
	
	public Boolean getShowAgentAffair() {
		return showAgentAffair;
	}
	
    public StateEnum getState() {
        return state;
    }
    public void setState(StateEnum state) {
        this.state = state;
    }
	public Long getArchiveId() {
		return archiveId;
	}
	public Long getMemberId() {
		return memberId;
	}
	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}
	public List<ApplicationCategoryEnum> getApps() {
		return apps;
	}
	public void setArchiveId(Long archiveId) {
		this.archiveId = archiveId;
	}
	public Boolean getIsTrack() {
		return isTrack;
	}
	public void setIsTrack(Boolean isTrack) {
		this.isTrack = isTrack;
	}
	public Boolean getIsFinish() {
		return isFinish;
	}
	public void setIsFinish(Boolean isFinish) {
		this.isFinish = isFinish;
	}
	
	public String[] getSelectColumns() {
        return selectColumns;
    }
    public void setSelectColumns(String... selectColumns) {
        this.selectColumns = selectColumns;
    }

	public Boolean getIsSourcesRelationOr() {
		return isSourcesRelationOr;
	}
	public void setIsSourcesRelationOr(Boolean isSourcesRelationOr) {
		this.isSourcesRelationOr = isSourcesRelationOr;
	}
	
	public Set<SearchModel> getSearchList(){
		return this.searchList;
	}
	
	public List<String> getAuditPopedom(){
		return AffairCondition.auditPopedom;
	}
	public List<String> getReadPopedom(){
		return AffairCondition.readPopedom;
	}

    public void setAgent(Boolean agentToFlag,Map<Integer,List<AgentModel>>  agentList){
/*    	if(agentList != null){
    		String agentInfo = "首页栏目找到代理，当前用户："+memberId;
    		for(Integer i : agentList.keySet()){
    			agentInfo += ","+i+",size:"+agentList.get(i)== null ? "null":agentList.get(i).size();
    		}
    	}*/
		this.agentToFlag = agentToFlag;
		this.agentList = agentList;
	}
    public void setAgent(Boolean agentToFlag,Map<Integer,List<AgentModel>>  agentList, List<AgentModel> agentModelList){
    	this.agentToFlag = agentToFlag;
    	this.agentList = agentList;
    	this.agentModelList = agentModelList;
    }
	public boolean isContainAgent() {
		return containAgent;
	}
	public void setContainAgent(boolean containAgent) {
		this.containAgent = containAgent;
	}

	public Boolean getAgentFlag(){
		return this.agentToFlag;
	}

	public List<AgentModel> getAppAgents(Integer app){
		if(this.agentList != null){
			return this.agentList.get(app);
		}
		return null;
	}
	public List<AgentModel> getAgentModelList(){
		return this.agentModelList;
	}
	
	public Boolean getVjoin() {
		return vjoin;
	}
	
	public void setVjoin(Boolean vjoin) {
		this.vjoin = vjoin;
	}
	
	public static enum SearchCondition{
		/**
		 * 标题
		 */
		subject,
		/**
		 * 重要程度
		 */
		importLevel,
		/**
		 * 发起人
		 */
		sender,
		/**
		 * 发起时间段
		 */
		createDate,
		/**
		 * 接收时间段 
		 */
		receiveDate,
		/**
		 * 处理时间段
		 */
		dealDate,
		/**
		 * 状态
		 */
		subState,
		/**
		 * 应用类型
		 */
		applicationEnum,

		/**
		 * 按照模板查询
		 */
		templete,
		/**
		 * 节点权限 审核。阅读
		 */
		nodePerm,
		/**
		 * 超期事项
		 */
		overTime,
		/**
		 * 首页配置的节点权限。
		 */
		policy4Portal,

		/**
		 * 自由协同 | 公文 | 协同和表单模板
		 */
		catagory,
		/**
		 * 办理状态
		 */
		handlingState,
		/**
		 * 处理期限
		 */
		expectedProcessTime,
		/**
		 * 流程状态
		 */
		workflowState,
		
		/**
		 * 协同ID
		 */
		moduleId,
		/**
		 * 我的部门成员
		 */
		myDept,  
		/**
		 * 我的领导
		 */
		myLeader, 
		/**
		 * 加签给我的
		 */
		addToMe
	}
	public class SearchModel{
		private SearchCondition searchCondition;
		private String searchValue1;
		private String searchValue2;
		private boolean isMorePageSearch;
		
		public SearchModel(SearchCondition searchCondition,String searchValue1,String searchValue2){
			this.searchCondition = searchCondition;
			this.searchValue1 = searchValue1;
			this.searchValue2 = searchValue2;
		}
		
		public SearchModel(SearchCondition searchCondition,String searchValue1,String searchValue2,boolean isMorePageSearch){
			this.searchCondition = searchCondition;
			this.searchValue1 = searchValue1;
			this.searchValue2 = searchValue2;
			this.isMorePageSearch = isMorePageSearch;
		}
		
		public boolean isMorePageSearch() {
			return isMorePageSearch;
		}

		public void setMorePageSearch(boolean isMorePageSearch) {
			this.isMorePageSearch = isMorePageSearch;
		}

		public SearchCondition getSearchCondition() {
			return searchCondition;
		}
		public String getSearchValue1() {
			return searchValue1;
		}
		public String getSearchValue2() {
			return searchValue2;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == null) return false;
			if(obj instanceof SearchModel){
				SearchModel other = (SearchModel) obj;
				if(this.searchCondition.equals(other.searchCondition)){
					return true;
				}
			}
			return false;
		}
	}

	public void addSearch(SearchCondition searchCondition,String searchValue1,String searchValue2,boolean isMorePortalQuery){
		SearchModel model = new SearchModel(searchCondition,searchValue1,searchValue2,isMorePortalQuery);
		searchList.add(model);
	}
	public void addSearch(SearchCondition searchCondition,String searchValue1,String searchValue2){
		addSearch(searchCondition, searchValue1, searchValue2,false);
	}
	public void removeConditon(SearchCondition searchCondition){
		if(this.searchList != null){
			for(SearchModel model : this.searchList){
				if(model.getSearchCondition().equals(searchCondition)){
					this.searchList.remove(model);
					break;
				}
			}
		}
	}
	public void removeAllCondition(){
		if(this.searchList != null){
			this.searchList.clear();
		}
	}

	// member_id, state, app, is_delete, archive_id, is_finish, is_track
	public DetachedCriteria getAgentSearchDetached(){
		if(this.agentList != null && !this.agentList.isEmpty()){
			DetachedCriteria criteria = DetachedCriteria.forClass(CtpAffair.class,"affair");
			Collection<List<AgentModel>> allList = this.agentList.values();
			Junction jun = getAgentJunction(allList);
			if(this.agentToFlag){//被代理了。 应该取得是我的事项
//				//criteria.add(Restrictions.eq("affair.memberId", this.memberId));
			}
	    	criteria.add(jun);
	    	criteria.add(Restrictions.eq("affair.delete",this.isDelete));
			
	    	initSearch(criteria,"affair");
			return criteria;
		}
		return null;
	}

	private Junction getAgentJunction(Collection<List<AgentModel>> allList){
		Set<AgentModel> allSet = new HashSet<AgentModel>();
		for(List<AgentModel> list : allList)	{
			allSet.addAll(list);
		}
		
		Junction junction = Restrictions.disjunction();
		for(AgentModel model : allSet){
			String options = model.getAgentOption();
			if(Strings.isBlank(options)){
				continue;
			}
			
			Junction j = Restrictions.conjunction();
			junction.add(j);
			//为了尽量使用索引，IDX_AFFAIR_TT1 ：MEMBER_ID, STATE, APP, IS_DELETE, ARCHIVE_ID, OBJECT_ID
			j.add(Restrictions.eq("memberId", model.getAgentToId())).add(Restrictions.eq("affair.state", this.state.key()));
			
			Junction appJunctions = Restrictions.disjunction();
			j.add(appJunctions);
			
			List<AgentDetailModel> details = model.getAgentDetail();

            //综合办公
            if (model.isHasOffice()) {
                List<Integer> apps = new UniqueList<Integer>();

                apps.add(ApplicationCategoryEnum.office.key());

                Junction officeJunctions = Restrictions.conjunction();
                officeJunctions.add(Restrictions.in("app", apps)).add(Restrictions.ne("subApp", ApplicationSubCategoryEnum.office_book.key()));

                appJunctions.add(officeJunctions);
            }

        	//公文
        	if(model.isHasEdoc()){
        	    List<Integer> apps = new UniqueList<Integer>();
        	    
                apps.add(ApplicationCategoryEnum.edoc.key());
                apps.add(ApplicationCategoryEnum.edocRec.key());
                apps.add(ApplicationCategoryEnum.edocRegister.key());
                apps.add(ApplicationCategoryEnum.edocSend.key());
                apps.add(ApplicationCategoryEnum.edocSign.key());
                apps.add(ApplicationCategoryEnum.exSend.key());
                apps.add(ApplicationCategoryEnum.exSign.key());
                apps.add(ApplicationCategoryEnum.exchange.key());
                apps.add(ApplicationCategoryEnum.edocRecDistribute.key());
                
        	    appJunctions.add(Restrictions.in("app", apps));
        	}
        	
        	//公共信息审批
        	if(model.isHasPubAudit()){
        	    List<Integer> apps = new UniqueList<Integer>();
        	    
                apps.add(ApplicationCategoryEnum.bulletin.key());
                apps.add(ApplicationCategoryEnum.news.key());
                apps.add(ApplicationCategoryEnum.inquiry.key());
                
                Junction inqJunctions = Restrictions.conjunction();
                inqJunctions.add(Restrictions.in("app", apps)).add(Restrictions.eq("subApp", ApplicationSubCategoryEnum.inquiry_audit.key()));
                
                appJunctions.add(inqJunctions);
        	}
        	
        	//会议
        	if(model.isHasMeeting()){
                
        	    Junction mJunc = Restrictions.disjunction();
        	    
                //会议的代理时间会和会议的
                Junction meetingRoomJunc = Restrictions.conjunction();
                
                meetingRoomJunc.add(Restrictions.eq("app", ApplicationCategoryEnum.meetingroom.key()));
                
                Junction meetingJunc = Restrictions.conjunction();
                meetingJunc.add(Restrictions.eq("app", ApplicationCategoryEnum.meeting.key()));
                
                //receiveTime是会议的开始时间
                meetingJunc.add(Restrictions.le("receiveTime", model.getEndDate()));               
                meetingJunc.add(Restrictions.ne("affair.subState",SubStateEnum.meeting_pending_periodicity.getKey()));
                mJunc.add(meetingRoomJunc);
                mJunc.add(meetingJunc);
                
                appJunctions.add(mJunc);
                
        	}
        	
        	//协同
        	boolean c = model.isHasCol();
        	boolean t = model.isHasTemplate();
        	if(c || t){
			    Junction colJunctions = Restrictions.conjunction();
			    appJunctions.add(colJunctions);
			    
			    colJunctions.add(Restrictions.eq("app", ApplicationCategoryEnum.collaboration.key()));
			    
			    if(c && t && Strings.isEmpty(details)){ //全部协同
			        //goto
			    }
			    else if(c && !t){ //仅自由协同
			        colJunctions.add(Restrictions.isNull("templeteId"));
                }
                else if(t && !c){
                	if(Strings.isEmpty(details)){
                	    //模板(全部)
                	    colJunctions.add(Restrictions.isNotNull("templeteId"));
                	}
                	else{
                	    //指定模板
                		List<Long> templateIds = new ArrayList<Long>();
                        for (AgentDetailModel agentDetailModel : details) {
                            templateIds.add(agentDetailModel.getEntityId());
                        }
                        colJunctions.add(Restrictions.in("templeteId", templateIds));
                	}
                }
                else if(t && c){
                    //自由协同+部分模板协同
                    Junction tJunction = Restrictions.disjunction();
                    tJunction.add(Restrictions.isNull("templeteId"));
                    
                	if(Strings.isNotEmpty(details)){
                		List<Long> templateIds = new ArrayList<Long>();
                        for (AgentDetailModel agentDetailModel : details) {
                            templateIds.add(agentDetailModel.getEntityId());
                        }
                        tJunction.add(Restrictions.in("templeteId", templateIds));
                	}
                	
                	colJunctions.add(tJunction);
                }
            }
        	j.add(Restrictions.ge("receiveTime", model.getStartDate()));
		}
		
		return junction;
	}

	private List<Integer> getApplicationValues(AgentModel agentModel,boolean includeColl){
    	List<Integer> apps = new UniqueList<Integer>();
//    	Agent agent = agentModel.toAgent();
    	
    	if(agentModel.isHasEdoc()){
    	    apps.add(ApplicationCategoryEnum.edoc.key());
            apps.add(ApplicationCategoryEnum.edocRec.key());
            apps.add(ApplicationCategoryEnum.edocRegister.key());
            apps.add(ApplicationCategoryEnum.edocSend.key());
            apps.add(ApplicationCategoryEnum.edocSign.key());
            apps.add(ApplicationCategoryEnum.exSend.key());
            apps.add(ApplicationCategoryEnum.exSign.key());
            apps.add(ApplicationCategoryEnum.exchange.key());
            apps.add(ApplicationCategoryEnum.edocRecDistribute.key());
    	}
    	if(agentModel.isHasMeeting()){
    	    apps.add(ApplicationCategoryEnum.meeting.key());
    	    apps.add(ApplicationCategoryEnum.meetingroom.key());
    	}
    	if(agentModel.isHasInfo()){
    	    apps.add(ApplicationCategoryEnum.info.key());
    	    apps.add(ApplicationCategoryEnum.infoStat.key());
    	}
    	if(agentModel.isHasPubAudit()){
    	    apps.add(ApplicationCategoryEnum.bulletin.key());
    	    apps.add(ApplicationCategoryEnum.news.key());
    	    apps.add(ApplicationCategoryEnum.inquiry.key());
    	}
    	if(includeColl){
    	    apps.add(ApplicationCategoryEnum.collaboration.key());
    	}

    	return apps;
    }

	public DetachedCriteria getSearchDetached(){
		if(apps == null){
			return null;
		}

		Disjunction disjunction = Restrictions.disjunction();
		if(!getShowAgentAffair() && (Boolean.TRUE.equals(this.agentToFlag) || Boolean.TRUE.equals(this.containAgent))){//过滤代理事项
		    
			disjunction.add(this.getJunction4Agent());
		
		}else{
		    
		    List<Integer> all =  getAllApp();

		    if (all.size()>0) {
		    	disjunction.add(Restrictions.in("affair.app", all.toArray()));
		    }
		    
		    /*disjunction.add(Restrictions.eq("affair.app",ApplicationCategoryEnum.collaboration.key()));
		    disjunction.add(Restrictions.eq("affair.app",ApplicationCategoryEnum.edocSend.key()));
		    disjunction.add(Restrictions.eq("affair.app",ApplicationCategoryEnum.edocRec.key()));
		    disjunction.add(Restrictions.eq("affair.app",ApplicationCategoryEnum.edocSign.key()));
		    disjunction.add(Restrictions.eq("affair.app",ApplicationCategoryEnum.edocRegister.key()));
		    disjunction.add(Restrictions.eq("affair.app",ApplicationCategoryEnum.exSend.key()));
		    disjunction.add(Restrictions.eq("affair.app",ApplicationCategoryEnum.exchange.key()));
		    disjunction.add(Restrictions.eq("affair.app",ApplicationCategoryEnum.edocRecDistribute.key()));
		    disjunction.add(Restrictions.eq("affair.app",ApplicationCategoryEnum.meeting.key()));
		    disjunction.add(Restrictions.eq("affair.app",ApplicationCategoryEnum.meetingroom.key()));
		    disjunction.add(Restrictions.eq("affair.app",ApplicationCategoryEnum.info.key()));
		    disjunction.add(Restrictions.eq("affair.app",ApplicationCategoryEnum.infoStat.key()));
		    disjunction.add(Restrictions.eq("affair.app",ApplicationCategoryEnum.bulletin.key()));
		    disjunction.add(Restrictions.eq("affair.app",ApplicationCategoryEnum.news.key()));
		    disjunction.add(Restrictions.eq("affair.app",ApplicationCategoryEnum.inquiry.key()));
		    disjunction.add(Restrictions.eq("affair.app",ApplicationCategoryEnum.office.key()));
		    disjunction.add(Restrictions.eq("affair.app",ApplicationCategoryEnum.collaboration.key()));
                  */
		}
		
		DetachedCriteria criteria = DetachedCriteria.forClass(CtpAffair.class,"affair");
	      
        if(this.getSelectColumns() != null && this.getSelectColumns().length > 0){
            ProjectionList projectionList = Projections.projectionList();
            criteria.setProjection(projectionList);
            
            for (String column : this.getSelectColumns()) {
                projectionList.add(Projections.property(column), column);
            }
            
            criteria.setResultTransformer(new AliasToBeanResultTransformer(CtpAffair.class));
        }
		
		if(memberId != null){
		    criteria.add(Restrictions.eq("affair.memberId", memberId));
		}
		criteria.add(Restrictions.eq("affair.state", this.state.key()));
		if(this.state.key() == StateEnum.col_pending.key()){
			criteria.add(Restrictions.ne("affair.subState", SubStateEnum.col_pending_specialBack.getKey()));
			criteria.add(Restrictions.ne("affair.subState", SubStateEnum.col_pending_specialBackCenter.getKey()));
		}
    	criteria.add(disjunction);
    	criteria.add(Restrictions.eq("affair.delete",this.isDelete));
    	if (getVjoin()) {
    		//移动端待办获取公文(收文\发文\签报)和协同数据
    		if (StateEnum.col_pending.equals(this.state)) {
    			List<Integer> apps = new ArrayList<Integer>();
            	apps.add(ApplicationCategoryEnum.collaboration.getKey());
            	apps.add(ApplicationCategoryEnum.edoc.key());
            	apps.add(ApplicationCategoryEnum.edocSend.key());
            	apps.add(ApplicationCategoryEnum.edocRec.key());
            	apps.add(ApplicationCategoryEnum.edocSign.key());
            	apps.add(ApplicationCategoryEnum.edocRegister.key());
            	apps.add(ApplicationCategoryEnum.edocRecDistribute.key());
            	apps.add(ApplicationCategoryEnum.exchange.key());
            	apps.add(ApplicationCategoryEnum.exSend.key());
            	apps.add(ApplicationCategoryEnum.exSign.key());
    			criteria.add(Restrictions.in("affair.app", apps));
    		} else {
    			criteria.add(Restrictions.eq("affair.app",ApplicationCategoryEnum.collaboration.key()));
    		}
    	}
    	initSearch(criteria,"affair");
		return criteria;
	}
	/**
	 * 得到跟踪的查询
	 * @return
	 */
	public DetachedCriteria getTrackSearchDetached(){
		DetachedCriteria criteria = DetachedCriteria.forClass(CtpAffair.class,"affair");
		
		if(memberId != null){
		    criteria.add(Restrictions.eq("affair.memberId", memberId));
		}
		criteria.add(Restrictions.in("affair.state", new Object[]{StateEnum.col_sent.key(), StateEnum.col_pending.key(), StateEnum.col_done.key()})); //只查已发、已办
		criteria.add(Restrictions.in("affair.track", new Object[]{TrackEnum.all.ordinal(), TrackEnum.part.ordinal()}));
		criteria.add(Restrictions.eq("affair.delete", false)); //没有删除
		criteria.add(Restrictions.eq("affair.finish", false));
		//查询条件
		trackFlag=true;
		initSearch(criteria,"affair");
		//排序
		//criteria.addOrder(Order.desc("createDate"));
		return criteria;
	}
	public DetachedCriteria getSectionSearchDetached(int state){
		return getSectionSearchDetached(state,false);
	}
	/** 栏目查询使用 */
	public DetachedCriteria getSectionSearchDetached(int state,boolean isGroupBy){
		String aliasAffair = "affair";
		DetachedCriteria criteria = initDetached(state, aliasAffair);
		//按summaryID分组查询(criteria2是外层affair，criteria为内层affair)
		if (isGroupBy) {
			DetachedCriteria criteria2 = DetachedCriteria.forClass(CtpAffair.class,"affair2");
			criteria.add(Property.forName("affair2.objectId").eqProperty("affair.objectId") );
			
			ProjectionList projectionList = Projections.projectionList(); 
            projectionList.add(Projections.max("id").as("id"));
            //不知道为什么，设置的affair在sql中变成了affair_
			Criterion c  =  Restrictions.sqlRestriction(" 1=1 group by affair_.object_id");
			criteria.add(c);
			criteria.setProjection(projectionList);
			
			criteria2.add(Property.forName("affair2.id").eq(criteria));
            return criteria2;
		}
		
		
		return criteria;
	}
	private DetachedCriteria initDetached(int state, String aliasAffair) {
		DetachedCriteria criteria = DetachedCriteria.forClass(CtpAffair.class,aliasAffair);
		if(memberId != null){
		    criteria.add(Restrictions.eq(""+aliasAffair+".memberId", memberId));
		    /**徐矿集团【已办公文首页栏目只显示当前人员已处理但流程未结束的公文数据】 wxt.dulong 2019-5-22 start*/
		    if (state == 4) {
		    	 criteria.add(Restrictions.eq(""+aliasAffair+".summaryState", 0));
		    	 criteria.add(Restrictions.ne(""+aliasAffair+".nodePolicy", "fengfa"));
			}
		    /**徐矿集团【已办公文首页栏目只显示当前人员已处理但流程未结束的公文数据】 wxt.dulong 2019-5-22 end*/
		}
		if (getVjoin() && state != StateEnum.col_pending.key()) {//移动端只查询协同
    		criteria.add(Restrictions.eq("affair.app",ApplicationCategoryEnum.collaboration.key()));
    	}
		criteria.add(Restrictions.eq(""+aliasAffair+".state", state));
		criteria.add(Restrictions.eq(""+aliasAffair+".delete", false)); //没有删除
		if (state == StateEnum.col_done.key() || state == StateEnum.col_sent.key()) {
			Junction conJunction = Restrictions.conjunction().add(Restrictions.ne("app",ApplicationCategoryEnum.collaboration.key()));
			//已办栏目不显示已登记、已分发数据，已登记、已分发数据不会处于已发状态，所以不需要过滤
			if(state == StateEnum.col_done.key()){
				conJunction.add(Restrictions.ne(""+aliasAffair+".app", ApplicationCategoryEnum.edocRegister.key()))
				.add(Restrictions.ne(""+aliasAffair+".app", ApplicationCategoryEnum.edocRecDistribute.getKey()));
			}
			//已发栏目不查询会议的
			if(state == StateEnum.col_sent.key()){
				conJunction.add(Restrictions.ne(""+aliasAffair+".app", ApplicationCategoryEnum.meeting.key()));
			}
			Junction disJunction = Restrictions.disjunction(); 
			disJunction.add(Restrictions.or(
							Restrictions.and(Restrictions.eq(""+aliasAffair+".app", ApplicationCategoryEnum.collaboration.key()),Restrictions.isNull("archiveId")),
							conJunction));
			criteria.add(disJunction);
		}else if(state == StateEnum.col_waitSend.key()){   //待发不显示政务登记数据
			criteria.add(Restrictions.ne(""+aliasAffair+".app", ApplicationCategoryEnum.edocRegister.key()));
			criteria.add(Restrictions.ne(""+aliasAffair+".app", ApplicationCategoryEnum.info.key()));
		}
		
		this.setState(StateEnum.valueOf(state));
		//查询条件
		initSearch(criteria,aliasAffair);
		return criteria;
	}

	/**
	 * 本方法是用来处理当首页栏目流程来源设置了根据发起人查询时，更多页面中查询条件的
	 * @param parameter 
	 */
	public String getSearchHql(Map<String, Object> parameter){
		StringBuilder sqlStr = new StringBuilder();
		if(this.searchList != null && !this.searchList.isEmpty()){
			for(SearchModel model : this.searchList){
				switch(model.getSearchCondition()){
				  case moduleId:
					if(Strings.isNotBlank(model.getSearchValue1())){
						sqlStr.append(" and affair.object_id =:objectId ");
						parameter.put("objectId",Long.parseLong(model.getSearchValue1()));
					}
					break;
					case subject://标题
						if(Strings.isNotBlank(model.getSearchValue1())){
							sqlStr.append(" and affair.subject like :__subjectTemp ");
							parameter.put("__subjectTemp", "%" + model.getSearchValue1() + "%");
						}
						break;
					case importLevel://重要程度
						if(Strings.isNotBlank(model.getSearchValue1())){
							sqlStr.append(" and affair.important_level =:importLevelTemp ");
							parameter.put("importLevelTemp", model.getSearchValue1());
						}
						break;
					case sender://发起人
						if(Strings.isNotBlank(model.getSearchValue1())){
							String value=model.getSearchValue1();
							sqlStr.append(" and exists (select * from org_member m where m.id=affair.sender_id and m.name like :senderNameTemp) ");
							parameter.put("senderNameTemp", "%"+value+"%");
						}
						break;
					case createDate://发起时间
						if(Strings.isNotBlank(model.getSearchValue1())){
							sqlStr.append(" and affair.create_date >= :startDateTemp ");
							Date startDate = Datetimes.parseNoTimeZone(model.getSearchValue1(), null);
							parameter.put("startDateTemp", startDate);
						}
						if(Strings.isNotBlank(model.getSearchValue2())){
							sqlStr.append(" and affair.create_date <= :endDateTemp ");
							Date endDate = Datetimes.parseNoTimeZone(model.getSearchValue2(),null);
							parameter.put("endDateTemp",endDate);
						}
						break;
	                case receiveDate://接收时间
	                    if(Strings.isNotBlank(model.getSearchValue1())){
	                    	sqlStr.append(" and affair.receive_time >=:startDateTemp ");
	                    	Date startDate = Datetimes.parseNoTimeZone(model.getSearchValue1(), null);
	                    	parameter.put("startDateTemp", startDate);
	                    }
	                    if(Strings.isNotBlank(model.getSearchValue2())){
	                    	sqlStr.append(" and affair.receive_time <=:endDateTemp ");
	                    	Date endDate = Datetimes.parseNoTimeZone(model.getSearchValue2(), null);
	                    	parameter.put("endDateTemp", endDate);
	                    }
	                    break;
	                case dealDate://处理时间
	                	if(Strings.isNotBlank(model.getSearchValue1())){
	                		sqlStr.append(" and affair.complete_time >=:startDateTemp ");
	                		Date startDate = Datetimes.parseNoTimeZone(model.getSearchValue1(), null);
	                		parameter.put("startDateTemp", startDate);
						}
						if(Strings.isNotBlank(model.getSearchValue2())){
							sqlStr.append(" and affair.complete_time >=:endDateTemp ");
							Date endDate = Datetimes.parseNoTimeZone(model.getSearchValue2(), null);
							parameter.put("endDateTemp",endDate);
						}
	                	break;
	                case subState://流程状态
    					if(Strings.isNotBlank(model.getSearchValue1())){
    						String subState = model.getSearchValue1();
    						
    						if (subState.length() == 1 && NumberUtils.isNumber(subState)) {

                                if (String.valueOf(SubStateEnum.col_waitSend_stepBack.getKey()).equals(subState) || String.valueOf(SubStateEnum.col_waitSend_sendBack.getKey()).equals(subState)) {
                                    sqlStr.append(" and (affair.sub_state = :subStateTemp1 or affair.back_From_Id is not null)");
        							parameter.put("subStateTemp1", Integer.parseInt(subState));
                                    // 去掉暂存待办的数据
        							sqlStr.append(" and (affair.sub_state != :subStateTemp2 and affair.back_From_Id is not null)");
        							parameter.put("subStateTemp2", SubStateEnum.col_pending_ZCDB.getKey());
                                }
                                else {
                                    sqlStr.append(" and affair.sub_state =:subStateTemp ");
        							parameter.put("subStateTemp", Integer.parseInt(subState));
                                }
                                // 指定回退
                            }
                            else if (String.valueOf(SubStateEnum.col_pending_specialBack.getKey()).equals(subState)) {
                                List<Integer> subStateList = new ArrayList<Integer>();
                                subStateList.add(SubStateEnum.col_pending_specialBack.getKey());
                                subStateList.add(SubStateEnum.col_pending_specialBackCenter.getKey());
                                subStateList.add(SubStateEnum.col_pending_specialBackToSenderCancel.getKey());
                                subStateList.add(SubStateEnum.col_pending_specialBackToSenderReGo.getKey());
                                
                                sqlStr.append(" and affair.sub_state in (:subStateTemp) ");
    							parameter.put("subStateTemp", subStateList);
                                // 已读
                            }
                            else if (String.valueOf(SubStateEnum.col_pending_read.getKey()).equals(subState)) {
                                List<Integer> subStateList = new ArrayList<Integer>();
                                subStateList.add(SubStateEnum.col_pending_read.getKey());
                                subStateList.add(SubStateEnum.col_pending_specialBack.getKey());
                                subStateList.add(SubStateEnum.col_pending_specialBackToSenderReGo.getKey());
                                
                                sqlStr.append(" and affair.sub_state in (:subStateTemp) ");
    							parameter.put("subStateTemp", subStateList);
                            }
                            else {
                                String[] subStates = subState.split("[,]");
                                List<Integer> subStateList = new ArrayList<Integer>();
                                for (String sState : subStates) {
                                    subStateList.add(Integer.parseInt(sState));
                                }
                                if (subStateList.contains(SubStateEnum.col_waitSend_sendBack.getKey()) || subStateList.contains(SubStateEnum.col_waitSend_stepBack.getKey())) {
                                    sqlStr.append(" and (affair.sub_state in (:subStateTemp) or affair.back_From_Id is not null)");
        							parameter.put("subStateTemp", subStateList);
                                }
                                else {
                                    sqlStr.append(" and affair.sub_state in (:subStateTemp) ");
                                    parameter.put("subStateTemp", subStateList);
                                }

                            }
    					}
    					break;
    				case applicationEnum://应用类型
    					String value=model.getSearchValue1();
    					if(Strings.isNotBlank(value)){
    						if("10".equals(value)){//当按应用类型查询调查的时候只查询带填写的调查
    							sqlStr.append(" and affair.app in (:__appTemp) and affair.sub_app=1 ");
    						}else if("7,8,9,10".equals(value)){//文化建设审批，只需要查询待审核的
    							sqlStr.append(" and affair.app in (:__appTemp) and affair.sub_app=0 ");
    						}else{
        						sqlStr.append(" and affair.app in (:__appTemp) ");
    						}
    						String[] apps = value.split(",");
    						parameter.put("__appTemp", Arrays.asList(apps));
    					}
    					break;
    				case expectedProcessTime://chuliqianxian
    					if(Strings.isNotBlank(model.getSearchValue1())){
	                		sqlStr.append(" and affair.expected_Process_Time >=:startDateTemp ");
	                		Date startDate = Datetimes.parseNoTimeZone(model.getSearchValue1(), null);
	                		parameter.put("startDateTemp", startDate);
						}
						if(Strings.isNotBlank(model.getSearchValue2())){
							sqlStr.append(" and affair.expected_Process_Time >=:endDateTemp ");
							Date endDate = Datetimes.parseNoTimeZone(model.getSearchValue2(), null);
							parameter.put("endDateTemp",endDate);
						}
    					break;	
    				default:
    					break;
				}
			}
		}
		return sqlStr.toString();
	}

    private void initSearch(DetachedCriteria criteria, String aliasAffair) {

        Junction morePortalJunction = Restrictions.conjunction(); // 栏目更多

        Junction componentJunction = Restrictions.conjunction(); // 最终的

        Junction junctionSource = Restrictions.conjunction(); // and
        if (this.isSourcesRelationOr) {
            junctionSource = Restrictions.disjunction(); // or
        }
        OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
        if (this.searchList != null && !this.searchList.isEmpty()) {
            for (SearchModel model : this.searchList) {
                boolean isPortalMore = model.isMorePageSearch;
                switch (model.getSearchCondition()) {
                case moduleId:
                    if (Strings.isNotBlank(model.getSearchValue1())) {
                        Criterion c = Restrictions.eq("" + aliasAffair + ".objectId", Long.parseLong(model.getSearchValue1()));
                        if (isPortalMore) {
                            morePortalJunction.add(c);
                        }
                        else {
                            junctionSource.add(c);
                        }
                    }
                    break;
                case subject:
                    if (Strings.isNotBlank(model.getSearchValue1())) {
                        Criterion c = Restrictions.like("" + aliasAffair + ".subject", "%" + SQLWildcardUtil.escape(model.getSearchValue1()) + "%");
                        if (isPortalMore) {
                            morePortalJunction.add(c);
                        }
                        else {
                            junctionSource.add(c);
                        }
                    }
                    break;
                case myLeader:

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
                    if (Strings.isNotEmpty(ids)) {
                    	Criterion myLeaderc = Restrictions.in("" + aliasAffair + ".senderId", ids);
                    	if (isPortalMore) {
                    		morePortalJunction.add(myLeaderc);
                    	}
                    	else {
                    		junctionSource.add(myLeaderc);
                    	}
                    }
                    break;
                case myDept:
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
                        Criterion MyDeptc = Restrictions.in("" + aliasAffair + ".senderId", myDeptMembersIds);
                        if (isPortalMore) {
                            morePortalJunction.add(MyDeptc);
                        }
                        else {
                            junctionSource.add(MyDeptc);
                        }
                    }
                    break;
                case addToMe:
                    Criterion addToMe = Restrictions.isNotNull("" + aliasAffair + ".fromId");
                    if (isPortalMore) {
                        morePortalJunction.add(addToMe);
                    }
                    else {
                        junctionSource.add(addToMe);
                    }
                    break;
                case importLevel:
                    if (Strings.isNotBlank(model.getSearchValue1())) {
                        String[] imps = model.getSearchValue1().split("[,]");
                        List<Integer> importantList = new ArrayList<Integer>();
                        List<Integer> newList = new ArrayList<Integer>();
                        for (int i = 0; i < imps.length; i++) {
                            if (NumberUtils.isNumber(imps[i])) {
                                importantList.add(Integer.valueOf(imps[i]));
                            }
                        }
                        Junction junction = Restrictions.conjunction();

                        if (importantList.contains(Integer.valueOf(6))) {// 包含其他
                            if (importantList.size() == 1) {// 只勾选了'其他'就查询出重要程度为空的
                                                            // OA-21615
                                newList.add(Integer.valueOf(1));
                                newList.add(Integer.valueOf(2));
                                newList.add(Integer.valueOf(3));
                                newList.add(Integer.valueOf(4));
                                newList.add(Integer.valueOf(5));
                                junction.add(
                                        Restrictions.or(Restrictions.not(Restrictions.in("" + aliasAffair + ".importantLevel", newList)), Restrictions.isNull("" + aliasAffair + ".importantLevel")));
                            }
                            else if (importantList.size() != 6) {// 等于4的时候就是所有的重要程度都包括，就不用加条件了。
                                newList.add(Integer.valueOf(1));
                                newList.add(Integer.valueOf(2));
                                newList.add(Integer.valueOf(3));
                                newList.add(Integer.valueOf(4));
                                newList.add(Integer.valueOf(5));
                                importantList.remove(Integer.valueOf(6));
                                newList.removeAll(importantList);
                                junction.add(
                                        Restrictions.or(Restrictions.not(Restrictions.in("" + aliasAffair + ".importantLevel", newList)), Restrictions.isNull("" + aliasAffair + ".importantLevel")));
                            }
                        }
                        else { // 不包含其他
                            if (importantList.size() > 1) {
                                junction.add(Restrictions.in("" + aliasAffair + ".importantLevel", importantList));
                            }
                            else {
                                junction.add(Restrictions.eq("" + aliasAffair + ".importantLevel", importantList.get(0)));
                            }
                        }
                        // 重要紧急程度需屏蔽信息报送
                        junction.add(Restrictions.ne("" + aliasAffair + ".app", ApplicationCategoryEnum.info.key()));
                        if (isPortalMore) {
                            morePortalJunction.add(junction);
                        }
                        else {
                            junctionSource.add(junction);
                        }
                    }
                    break;
                case applicationEnum:
                    if (Strings.isNotBlank(model.getSearchValue1())) {
                        String[] apps = model.getSearchValue1().split(",");
                        List<Integer> appList = new ArrayList<Integer>();
                        Map<Integer, List<Integer>> app2SubApp = new HashMap<Integer, List<Integer>>();
                        for (String app : apps) {
                            if (ApplicationCategoryEnum.exchange.key() == Integer.parseInt(app)) {
                                appList.add(ApplicationCategoryEnum.exSend.key());
                                appList.add(ApplicationCategoryEnum.exSign.key());
                                appList.add(ApplicationCategoryEnum.edocRegister.key());
                            }
                            else if (ApplicationCategoryEnum.inquiry.key() == Integer.parseInt(app)) {
                                int subState = ApplicationSubCategoryEnum.inquiry_audit.key();
                                // 当按应用类型查询调查的时候只查询带填写的调查
                                if (apps.length == 1) {
                                    subState = ApplicationSubCategoryEnum.inquiry_write.key();
                                }
                                Strings.addToMap(app2SubApp, ApplicationCategoryEnum.inquiry.key(), subState);

                            }
                            else if (ApplicationCategoryEnum.news.key() == Integer.parseInt(app)) {
                                // 为新闻的时候为综合信息审批，包含待审批的调查，不包括待填写的。
                                appList.add(ApplicationCategoryEnum.bulletin.key());
                                appList.add(ApplicationCategoryEnum.news.key());

                                Strings.addToMap(app2SubApp, ApplicationCategoryEnum.inquiry.key(), ApplicationSubCategoryEnum.inquiry_audit.key());

                            }
                            else {
                                appList.add(Integer.parseInt(app));
                            }
                        }
                        Junction disJunction = Restrictions.disjunction();
                        if (appList.size() > 1) {
                            disJunction.add(Restrictions.in("" + aliasAffair + ".app", appList));
                        }
                        else if (appList.size() == 1) {
                            disJunction.add(Restrictions.eq("" + aliasAffair + ".app", appList.get(0)));
                        }
                        // 更具app2SubApp来判断。
                        if (!app2SubApp.isEmpty()) {
                            for (Iterator<Map.Entry<Integer, List<Integer>>> iterator = app2SubApp.entrySet().iterator(); iterator.hasNext();) {
                                Map.Entry<Integer, List<Integer>> entry = iterator.next();
                                if (!entry.getValue().isEmpty()) {
                                    disJunction.add(Restrictions.and(Restrictions.eq("" + aliasAffair + ".app", entry.getKey()), Restrictions.in("" + aliasAffair + ".subApp", entry.getValue())));
                                }
                            }
                        }
                        if (isPortalMore) {
                            morePortalJunction.add(disJunction);
                        }
                        else {
                            junctionSource.add(disJunction);
                        }
                    }
                    break;
                case nodePerm:
                    if (Strings.isNotBlank(model.getSearchValue1())) {
                        if ("audit".equals(model.getSearchValue1())) {
                            Criterion cri = Restrictions.in("" + aliasAffair + ".nodePolicy", AffairCondition.auditPopedom);
                            Criterion c = Restrictions.or(cri, Restrictions.in("" + aliasAffair + ".app",
                                    new Integer[] { ApplicationCategoryEnum.bulletin.key(), ApplicationCategoryEnum.news.key(), ApplicationCategoryEnum.inquiry.key() }));
                            if (isPortalMore) {
                                morePortalJunction.add(c);
                            }
                            else {
                                junctionSource.add(c);
                            }
                        }
                        else if ("read".equals(model.getSearchValue1())) {
                            Criterion c = Restrictions.in("" + aliasAffair + ".nodePolicy", AffairCondition.readPopedom);
                            if (isPortalMore) {
                                morePortalJunction.add(c);
                            }
                            else {
                                junctionSource.add(c);
                            }
                        }
                    }
                    break;
                case sender:
                    if (Strings.isNotBlank(model.getSearchValue1())) {
                        Criterion c = Restrictions.sqlRestriction(" exists (select id from org_member where id={alias}.sender_id and name like ?) ",
                                "%" + SQLWildcardUtil.escape(model.getSearchValue1()) + "%", Hibernate.STRING);
                        if (isPortalMore) {
                            morePortalJunction.add(c);
                        }
                        else {
                            junctionSource.add(c);
                        }
                    }
                    break;
                case createDate:
                    Junction disJunction = Restrictions.conjunction();
                    if (Strings.isNotBlank(model.getSearchValue1())) {
                        Date startDate = Datetimes.parseNoTimeZone(model.getSearchValue1(), null);
                        disJunction.add(Restrictions.ge("" + aliasAffair + ".createDate", startDate));
                    }
                    if (Strings.isNotBlank(model.getSearchValue2())) {
                        Date startDate = Datetimes.parseNoTimeZone(model.getSearchValue2(), null);
                        disJunction.add(Restrictions.le("" + aliasAffair + ".createDate", startDate));
                    }
                    if (isPortalMore) {
                        morePortalJunction.add(disJunction);
                    }
                    else {
                        junctionSource.add(disJunction);
                    }
                    break;
                case receiveDate:
                    Junction disJunctionReceive = Restrictions.conjunction();
                    if (Strings.isNotBlank(model.getSearchValue1())) {
                        Date startDate = Datetimes.parseNoTimeZone(model.getSearchValue1(), null);
                        disJunctionReceive.add(Restrictions.ge("" + aliasAffair + ".receiveTime", startDate));
                    }
                    if (Strings.isNotBlank(model.getSearchValue2())) {
                        Date endDate = Datetimes.parseNoTimeZone(model.getSearchValue2(), null);
                        disJunctionReceive.add(Restrictions.le("" + aliasAffair + ".receiveTime", endDate));
                    }
                    if (isPortalMore) {
                        morePortalJunction.add(disJunctionReceive);
                    }
                    else {
                        junctionSource.add(disJunctionReceive);
                    }
                    break;
                case dealDate:
                    Junction disJunctionDeal = Restrictions.conjunction();
                    if (Strings.isNotBlank(model.getSearchValue1())) {
                        Date startDate = Datetimes.parseNoTimeZone(model.getSearchValue1(), null);
                        disJunctionDeal.add(Restrictions.ge("" + aliasAffair + ".updateDate", startDate));
                    }
                    if (Strings.isNotBlank(model.getSearchValue2())) {
                        Date endDate = Datetimes.parseNoTimeZone(model.getSearchValue2(), null);
                        disJunctionDeal.add(Restrictions.le("" + aliasAffair + ".updateDate", endDate));
                    }
                    if (isPortalMore) {
                        morePortalJunction.add(disJunctionDeal);
                    }
                    else {
                        junctionSource.add(disJunctionDeal);
                    }
                    break;
                case subState:
                    String subState = model.getSearchValue1();
                    if (Strings.isNotBlank(subState)) {
                        if (subState.length() == 1 && NumberUtils.isNumber(subState)) {

                            if (String.valueOf(SubStateEnum.col_waitSend_stepBack.getKey()).equals(subState) || String.valueOf(SubStateEnum.col_waitSend_sendBack.getKey()).equals(subState)) {
                                Criterion c = Restrictions.or(Restrictions.eq("" + aliasAffair + ".subState", Integer.parseInt(subState)), Restrictions.isNotNull("" + aliasAffair + ".backFromId"));
                                // 去掉暂存待办的数据
                                Criterion c2 = Restrictions.and(Restrictions.ne("" + aliasAffair + ".subState", SubStateEnum.col_pending_ZCDB.getKey()),
                                        Restrictions.isNotNull("" + aliasAffair + ".backFromId"));
                                if (isPortalMore) {
                                    morePortalJunction.add(c);
                                    morePortalJunction.add(c2);
                                }
                                else {
                                    junctionSource.add(c);
                                }
                            }
                            else {
                                Criterion c = Restrictions.eq("" + aliasAffair + ".subState", Integer.parseInt(subState));
                                if (isPortalMore) {
                                    morePortalJunction.add(c);
                                }
                                else {
                                    junctionSource.add(c);
                                }
                            }
                            // 指定回退
                        }
                        else if (String.valueOf(SubStateEnum.col_pending_specialBack.getKey()).equals(subState)) {
                            List<Integer> subStateList = new ArrayList<Integer>();
                            subStateList.add(SubStateEnum.col_pending_specialBack.getKey());
                            subStateList.add(SubStateEnum.col_pending_specialBackCenter.getKey());
                            subStateList.add(SubStateEnum.col_pending_specialBackToSenderCancel.getKey());
                            subStateList.add(SubStateEnum.col_pending_specialBackToSenderReGo.getKey());
                            Criterion c = Restrictions.in("" + aliasAffair + ".subState", subStateList);
                            if (isPortalMore) {
                                morePortalJunction.add(c);
                            }
                            // 已读
                        }
                        else if (String.valueOf(SubStateEnum.col_pending_read.getKey()).equals(subState)) {
                            List<Integer> subStateList = new ArrayList<Integer>();
                            subStateList.add(SubStateEnum.col_pending_read.getKey());
                            subStateList.add(SubStateEnum.col_pending_specialBack.getKey());
                            subStateList.add(SubStateEnum.col_pending_specialBackToSenderReGo.getKey());
                            Criterion c = Restrictions.in("" + aliasAffair + ".subState", subStateList);
                            if (isPortalMore) {
                                morePortalJunction.add(c);
                            }
                        }
                        else {
                            String[] subStates = subState.split("[,]");
                            List<Integer> subStateList = new ArrayList<Integer>();
                            for (String sState : subStates) {
                                subStateList.add(Integer.parseInt(sState));
                            }
                            if (subStateList.contains(SubStateEnum.col_waitSend_sendBack.getKey()) || subStateList.contains(SubStateEnum.col_waitSend_stepBack.getKey())) {
                                Criterion c = Restrictions.or(Restrictions.in("" + aliasAffair + ".subState", subStateList), Restrictions.isNotNull("" + aliasAffair + ".backFromId"));
                                if (isPortalMore) {
                                    morePortalJunction.add(c);
                                }
                                else {
                                    junctionSource.add(c);
                                }
                            }
                            else {
                                Criterion c = Restrictions.in("" + aliasAffair + ".subState", subStateList);
                                if (isPortalMore) {
                                    morePortalJunction.add(c);
                                }
                                else {
                                    junctionSource.add(c);
                                }
                            }

                        }
                    }
                    break;
                case templete:
                    if (Strings.isNotBlank(model.getSearchValue1())) {
                        String searchValue1 = model.getSearchValue1();
                        String[] colAndEdocTIds = searchValue1.split(",");
                        List<Long> tempIdList = new ArrayList<Long>();
                        List<Long> tempCategoryIdList = new ArrayList<Long>();
                        for (String tempId : colAndEdocTIds) {
                            if (Strings.isNotBlank(tempId)) {
                                if (tempId.contains("C_")) {
                                    //if (tempCategoryIdList.size() < 1000) {
                                        String categoryIdStr = tempId.substring(2, tempId.length());
                                        Long categoryId = Long.valueOf(categoryIdStr);
                                        tempCategoryIdList.add(categoryId);
                                   //}
                                }
                                else {
                                    if (tempIdList.size() < 1000) {
                                        Long id = Long.valueOf(tempId);
                                        tempIdList.add(id);
                                    }

                                }
                            }
                        }
                       //  PendingManager pendingManager = (PendingManager)AppContext.getBean("pendingManager");
                        //查询数据库中所有的待办 数据的templateId,templateCategoryId
                        Map<Long,Long> map = getPendingTemplateCategoryIds(AppContext.currentUserId());
                       
                        Collection<Long> templateIds = map.keySet();
                        if(Strings.isNotEmpty(templateIds)){
                            for(Iterator<Long> it = templateIds.iterator();it.hasNext();){
                                Long templateId = it.next();
                                Long categoryId = map.get(templateId);
                                if(tempCategoryIdList.contains(categoryId)){
                                	if (tempIdList.size() < 1000) {
                                		tempIdList.add(templateId);
                                	}
                                }
                            }
                        }
                        
                        if(Strings.isEmpty(tempIdList)){
                        	tempIdList.add(-1L);//没有待办时,让读取不出数据
                        }
                        
                        if (isPortalMore) {
                            if(Strings.isNotEmpty(tempIdList)){
                                morePortalJunction.add(Property.forName("" + aliasAffair + ".templeteId").in(tempIdList));
                            }
                        }
                        else {
                            if(Strings.isNotEmpty(tempIdList)){
                                junctionSource.add(Property.forName("" + aliasAffair + ".templeteId").in(tempIdList));
                            }
                        }

                    }
                    break;
                case overTime:
                    Criterion c = Restrictions.not(Restrictions.eq("" + aliasAffair + ".coverTime", false));
                    if (isPortalMore) {
                        morePortalJunction.add(c);
                    }
                    else {
                        junctionSource.add(c);
                    }
                    break;
                case handlingState:
                    if (Strings.isNotBlank(model.getSearchValue1())) {
                        String value = model.getSearchValue1();
                        String[] sv = value.split(",");
                        Junction junction = Restrictions.disjunction();
                        for (String s : sv) {
                            if (Strings.isBlank(s)) {
                                continue;
                            }
                            String[] stateStr = s.split("_");
                            if ("pending".endsWith(stateStr[1])) {// 待办
                                Junction cj = Restrictions.conjunction();
                                cj.add(Restrictions.eq("" + aliasAffair + ".state", StateEnum.col_pending.key()));
                                Junction _cj = Restrictions.conjunction();
                                _cj.add(Restrictions.ne("" + aliasAffair + ".subState", SubStateEnum.col_pending_ZCDB.key()));
                                _cj.add(Restrictions.ne("" + aliasAffair + ".subState", SubStateEnum.col_pending_specialBackToSenderReGo.key()));
                                cj.add(_cj);
                                junction.add(cj);
                            }
                            else if ("zcdb".endsWith(stateStr[1])) {// 暂存待办
                                Junction cj = Restrictions.conjunction();
                                cj.add(Restrictions.eq("" + aliasAffair + ".state", StateEnum.col_pending.key()));
                                Junction _cj = Restrictions.disjunction();
                                _cj.add(Restrictions.eq("" + aliasAffair + ".subState", SubStateEnum.col_pending_ZCDB.key()));
                                _cj.add(Restrictions.eq("" + aliasAffair + ".subState", SubStateEnum.col_pending_specialBackToSenderReGo.key()));
                                cj.add(_cj);
                                junction.add(cj);
                            }
                            else if ("draft".endsWith(stateStr[1])) {// 草稿
                                Junction cj = Restrictions.conjunction();
                                cj.add(Restrictions.eq("" + aliasAffair + ".state", StateEnum.col_waitSend.key()));
                                Junction _cj = Restrictions.conjunction();
                                _cj.add(Restrictions.eq("" + aliasAffair + ".subState", SubStateEnum.col_waitSend_draft.key()));
                                cj.add(_cj);
                                junction.add(cj);
                            }
                            else if ("rollbacked".endsWith(stateStr[1])) {// 回退
                                Junction cj = Restrictions.conjunction();
                                cj.add(Restrictions.eq("" + aliasAffair + ".state", StateEnum.col_waitSend.key()));
                                Junction _cj = Restrictions.disjunction();
                                _cj.add(Restrictions.eq("" + aliasAffair + ".subState", SubStateEnum.col_waitSend_stepBack.key()));
                                _cj.add(Restrictions.eq("" + aliasAffair + ".subState", SubStateEnum.col_pending_specialBacked.key()));
                                _cj.add(Restrictions.eq("" + aliasAffair + ".subState", SubStateEnum.col_pending_specialBackToSenderCancel.key()));
                                // _cj.add(Restrictions.eq("subState",16));
                                // _cj.add(Restrictions.eq("subState", 18));
                                cj.add(_cj);
                                junction.add(cj);
                            }
                            else if ("cancel".endsWith(stateStr[1])) {// 撤销
                                Junction cj = Restrictions.conjunction();
                                cj.add(Restrictions.eq("" + aliasAffair + ".state", StateEnum.col_waitSend.key()));
                                Junction _cj = Restrictions.conjunction();
                                _cj.add(Restrictions.eq("" + aliasAffair + ".subState", SubStateEnum.col_waitSend_cancel.key()));
                                cj.add(_cj);
                                junction.add(cj);
                            }
                        }
                        if (isPortalMore) {
                            morePortalJunction.add(junction);
                        }
                        else {
                            junctionSource.add(junction);
                        }
                    }
                    break;
                case catagory:
                    if (Strings.isNotBlank(model.getSearchValue1())) {
                        String value = model.getSearchValue1();
                        String[] sv = value.split(",");
                        Junction junction = Restrictions.disjunction();
                        for (String s : sv) {
                            if ("done_catagory_all".equals(s)) {// 已办栏目全部，需要排除会议相关
                                Junction cj = Restrictions.conjunction();
                                // cj.add(Restrictions.ne("app",ApplicationCategoryEnum.meeting.key()));
                                // cj.add(Restrictions.ne("app",ApplicationCategoryEnum.meetingroom.key()));
                                cj.add(Restrictions.ne("" + aliasAffair + ".app", ApplicationCategoryEnum.info.key()));
                                cj.add(Restrictions.ne("" + aliasAffair + ".app", ApplicationCategoryEnum.office.key()));
                                cj.add(Restrictions.ne("" + aliasAffair + ".app", ApplicationCategoryEnum.inquiry.key()));
                                junction.add(cj);
                            }
                            else if ("sent_catagory_all".equals(s) || "waitsend_catagory_all".equals(s)) {// 已办栏目全部，需要排除会议相关
                                Junction cj = Restrictions.conjunction();
                                cj.add(Restrictions.ne("" + aliasAffair + ".app", ApplicationCategoryEnum.info.key()));
                                cj.add(Restrictions.ne("" + aliasAffair + ".app", ApplicationCategoryEnum.office.key()));
                                junction.add(cj);
                            }
                            else if ("catagory_collOrFormTemplete".equals(s)) {
                                // 模板协同
                                Junction cj = Restrictions.conjunction();
                               /* cj.add(Restrictions.isNotNull("" + aliasAffair + ".templeteId"));
                                cj.add(Restrictions.eq("" + aliasAffair + ".app", ApplicationCategoryEnum.collaboration.key()));*/
                                cj.add(Restrictions.eq("" + aliasAffair + ".subApp", ApplicationSubCategoryEnum.collaboration_tempate.key()));
                                cj.add(Restrictions.eq("" + aliasAffair + ".app", ApplicationCategoryEnum.collaboration.key()));
                                junction.add(cj);
                            }
                            else if ("catagory_coll".equals(s)) {
                                // 自由协同
                                Junction cj = Restrictions.conjunction();
                               /* cj.add(Restrictions.isNull("" + aliasAffair + ".templeteId"));
                                cj.add(Restrictions.eq("" + aliasAffair + ".app", ApplicationCategoryEnum.collaboration.key()));*/
                                cj.add(Restrictions.eq("" + aliasAffair + ".subApp", ApplicationSubCategoryEnum.collaboration_self.key()));
                                cj.add(Restrictions.eq("" + aliasAffair + ".app", ApplicationCategoryEnum.collaboration.key()));
                                junction.add(cj);
                            }
                            else if ("catagory_edoc".equals(s)) {
                                // 公文
                                List<Integer> keys = new ArrayList<Integer>();
                                keys.add(ApplicationCategoryEnum.edoc.key());
                                keys.add(ApplicationCategoryEnum.edocRec.key());
                                keys.add(ApplicationCategoryEnum.edocRegister.key());
                                keys.add(ApplicationCategoryEnum.edocSend.key());
                                keys.add(ApplicationCategoryEnum.edocSign.key());
                                keys.add(ApplicationCategoryEnum.exSend.key());
                                keys.add(ApplicationCategoryEnum.exSign.key());
                                keys.add(ApplicationCategoryEnum.exchange.key());
                                keys.add(ApplicationCategoryEnum.edocRecDistribute.key());
                                Junction cj = Restrictions.conjunction();
                                cj.add(Restrictions.in("" + aliasAffair + ".app", keys));
                                junction.add(cj);
                            }
                            else if ("catagory_meet".equals(s)) {
                                // 已召开会议
                                // TODO
                                List<Integer> keys = new ArrayList<Integer>();
                                keys.add(ApplicationCategoryEnum.meeting.key());
                                keys.add(ApplicationCategoryEnum.meetingroom.key());
                                Junction cj = Restrictions.conjunction();
                                cj.add(Restrictions.in("" + aliasAffair + ".app", keys));
                                // cj.add(Restrictions.eq("app",ApplicationCategoryEnum.meeting.key()));
                                junction.add(cj);
                            }
                            else if ("catagory_meetRoom".equals(s)) {
                                List<Integer> keys = new ArrayList<Integer>();
                                keys.add(ApplicationCategoryEnum.meetingroom.key());
                                Junction cj = Restrictions.conjunction();
                                cj.add(Restrictions.in("" + aliasAffair + ".app", keys));
                                junction.add(cj);
                            }
                            else if ("catagory_inquiry".equals(s)) {// 调查
                                Junction cj = Restrictions.conjunction();
                                cj.add(Restrictions.eq("" + aliasAffair + ".app", ApplicationCategoryEnum.inquiry.key()));
                                junction.add(cj);
                            }
                            else if ("catagory_publicInfo".equals(s)) {// 公共信息
                                Junction cj = Restrictions.conjunction();
                                List<Integer> publicInformatEnums = new ArrayList<Integer>();
                                publicInformatEnums.add(ApplicationCategoryEnum.bulletin.getKey());// 公告
                                publicInformatEnums.add(ApplicationCategoryEnum.news.getKey());// 新闻
                                publicInformatEnums.add(ApplicationCategoryEnum.bbs.getKey());// 讨论
                                publicInformatEnums.add(ApplicationCategoryEnum.inquiry.getKey());// 调查
                                cj.add(Restrictions.in("" + aliasAffair + ".app", publicInformatEnums));
                                junction.add(cj);
                            }
                            else if ("catagory_comprehensiveOffice".equals(s)) {// 综合办公
                                Junction cj = Restrictions.conjunction();
                                cj.add(Restrictions.eq("" + aliasAffair + ".app", ApplicationCategoryEnum.office.key()));
                                junction.add(cj);
                            }
                        }
                        if (isPortalMore) {
                            morePortalJunction.add(junction);
                        }
                        else {
                            junctionSource.add(junction);
                        }
                    }
                    break;
                case policy4Portal:
                    if (Strings.isNotBlank(model.getSearchValue1())) {
                        // wangjingjing 子应用subapp begin
                        // key: subapp, Value:节点权限
                        Map<Integer, List<String>> subAppPolicy = new HashMap<Integer, List<String>>();
                        // wangjingjing end
                        List<Integer> category = new ArrayList<Integer>();
                        Map<Integer, List<Integer>> app2SubApp = new HashMap<Integer, List<Integer>>();
                        Map<Integer, List<String>> policy = new HashMap<Integer, List<String>>();

                        String[] all = model.getSearchValue1().split(",");
                        for (String str : all) {
                            if (Strings.isBlank(str)) {
                                continue;
                            }

                            String[] poli = str.split("___");
                            if (poli[0] != null) {
                                if (poli.length == 2) {
                                    if (poli[0].startsWith("P")) {
                                        // 3.5格式的数据：P1___shenpi,P19___shenpi,P20___shenpi
                                        // ,P21___shenpi
                                        // 3.5之前的数据：P_shenpi (包括协同，公文)
                                        String apps = poli[0].substring(1, poli[0].length());
                                        if (Strings.isNotBlank(apps)) {
                                            int app = Integer.valueOf(apps);
                                            List<String> l = null;
                                            if (policy.get(app) == null) {
                                                l = new ArrayList<String>();
                                            }
                                            else {
                                                l = policy.get(app);
                                            }
                                            l.add(poli[1]);
                                            policy.put(app, l);
                                        }
                                        else {
                                            int type[] = { ApplicationCategoryEnum.collaboration.key(), ApplicationCategoryEnum.edocSend.key(), ApplicationCategoryEnum.edocRec.key(),
                                                    ApplicationCategoryEnum.edocSign.key() };
                                            List<String> l = null;
                                            for (int i = 0; i < type.length; i++) {
                                                if (policy.get(type[i]) == null) {
                                                    l = new UniqueList<String>();
                                                }
                                                else {
                                                    l = policy.get(type[i]);
                                                }
                                                l.add(poli[1]);
                                                policy.put(type[i], l);
                                            }
                                        }
                                    }
                                    else if (poli[0].startsWith("A")) {
                                        // 公文发文
                                        if (Integer.parseInt(poli[1]) == 19) {
                                            category.add(ApplicationCategoryEnum.edocSend.getKey());
                                        }
                                        else if (Integer.parseInt(poli[1]) == 20) { // 公文收文
                                            category.add(ApplicationCategoryEnum.edocRec.getKey());
                                            if (isG6Version()) {// G6版本包含登记、分发
                                                if (isOpenRegister()) {
                                                    // G6版本,在开启了收文登记的情况下，查询收文待登记的数据
                                                    category.add(ApplicationCategoryEnum.edocRegister.getKey());
                                                }

                                                // 是V5-G6版本,则登记查询待分发的数据
                                                category.add(ApplicationCategoryEnum.edocRecDistribute.getKey());

                                            }
                                            else {
                                                category.add(ApplicationCategoryEnum.edocRegister.getKey());
                                            }
                                        }
                                        else if (Integer.parseInt(poli[1]) == 21) { // 公文签报
                                            category.add(ApplicationCategoryEnum.edocSign.getKey());
                                        }
                                        else if (Integer.parseInt(poli[1]) == 16) { // 公文交换
                                            category.add(ApplicationCategoryEnum.exSend.getKey());
                                            category.add(ApplicationCategoryEnum.exSign.getKey());
                                        }
                                        // 公共信息发布待审
                                        else if (Integer.parseInt(poli[1]) == 7 || Integer.parseInt(poli[1]) == 8 || Integer.parseInt(poli[1]) == 10) {
                                            // if((Boolean)SysFlag.is_gov_only.getFlag())
                                            // {
                                            // if (Integer.parseInt(poli[1]) ==
                                            // 7) {
                                            // category.add(7);
                                            // }//公共信息---新闻(8)
                                            // else if(Integer.parseInt(poli[1])
                                            // == 8){
                                            // category.add(8);
                                            // }//公共信息---调查(10)
                                            // else if(Integer.parseInt(poli[1])
                                            // == 10){
                                            // Strings.addToMap(app2SubApp,
                                            // ApplicationCategoryEnum.inquiry.key(),
                                            // ApplicationSubCategoryEnum.inquiry_audit.key());
                                            // Strings.addToMap(app2SubApp,
                                            // ApplicationCategoryEnum.inquiry.key(),
                                            // ApplicationSubCategoryEnum.inquiry_write.key());
                                            // }
                                            // }else{
                                            category.add(7);
                                            category.add(8);

                                            Strings.addToMap(app2SubApp, ApplicationCategoryEnum.inquiry.key(), ApplicationSubCategoryEnum.inquiry_audit.key());
                                            // }
                                        }
                                        // 公文
                                        else if (Integer.parseInt(poli[1]) == 4) {
                                            category.add(ApplicationCategoryEnum.edoc.key());
                                            category.add(ApplicationCategoryEnum.edocRec.key());
                                            category.add(ApplicationCategoryEnum.edocRegister.key());
                                            category.add(ApplicationCategoryEnum.edocSend.key());
                                            category.add(ApplicationCategoryEnum.edocSign.key());
                                            category.add(ApplicationCategoryEnum.exSend.key());
                                            category.add(ApplicationCategoryEnum.exSign.key());
                                            category.add(ApplicationCategoryEnum.exchange.key());
                                            category.add(ApplicationCategoryEnum.edocRecDistribute.key());
                                        }
                                        // 会议
                                        else if (Integer.parseInt(poli[1]) == 6) {
                                            category.add(ApplicationCategoryEnum.meeting.key());
                                        }
                                        else if (Integer.parseInt(poli[1]) == 30) {
                                            category.add(ApplicationCategoryEnum.meeting.key());
                                            category.add(ApplicationCategoryEnum.meetingroom.key());
                                        }
                                        else if (Integer.parseInt(poli[1]) == 29) {
                                            category.add(ApplicationCategoryEnum.meetingroom.key());
                                        }
                                        else {
                                            category.add(Integer.parseInt(poli[1]));
                                        }
                                    }
                                    // wangjingjing 子应用subapp begin
                                    else if (poli[0].startsWith("S")) { // S6___shenpi,
                                                                        // S6___all
                                        String apps = poli[0].substring(1, poli[0].length());
                                        if (Strings.isNotBlank(apps)) {
                                            int app = Integer.valueOf(apps);
                                            List<String> l = subAppPolicy.get(app);
                                            if (null == l) {
                                                l = new UniqueList<String>();
                                                subAppPolicy.put(app, l);
                                            }
                                            l.add(poli[1]);
                                        }
                                    }
                                    // wangjingjing end
                                }
                                else if (poli.length == 3) {
                                    if (poli[0].startsWith("A")) {
                                        if (Integer.parseInt(poli[1]) == 10) {
                                            // 调查的格式为A___10___0 (待审核)
                                            // A___10___1(待填写)
                                            Strings.addToMap(app2SubApp, Integer.parseInt(poli[1]), Integer.parseInt(poli[2]));
                                        }
                                        else if (Integer.parseInt(poli[1]) == 32) {
                                            // 信息报送
                                            if (Integer.parseInt(poli[1]) == 32) {
                                                int subApp = Integer.parseInt(poli[2]);
                                                if (subApp == 0) {
                                                    Strings.addToMap(app2SubApp, ApplicationCategoryEnum.info.key(), ApplicationSubCategoryEnum.info_self.key());
                                                    Strings.addToMap(app2SubApp, ApplicationCategoryEnum.info.key(), ApplicationSubCategoryEnum.info_tempate.key());
                                                }
                                                else {
                                                    Strings.addToMap(app2SubApp, ApplicationCategoryEnum.info.key(), subApp);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Junction disJunctionApp = Restrictions.disjunction();
                        if (!category.isEmpty()) {
                            disJunctionApp.add(Restrictions.in("" + aliasAffair + ".app", category));
                        }

                        if (!policy.isEmpty()) {

                            /**
                             * 1.Restrictions.and(Restrictions.eq("app", key)
                             * 2.nodepolicy : a.阅文，办文替换前缀
                             * b.只会的时候需要兼容inform,zhihui c.原生的，直接取传递过来的
                             * d.登记的时候，节点权限为空，IsNull 3.subApp:
                             * 如果开启了越文和办文，区分越文和办文
                             */
                            for (Iterator<Integer> iterator = policy.keySet().iterator(); iterator.hasNext();) {
                                Integer key = iterator.next();
                                if (!policy.get(key).isEmpty()) {
                                    List<String> policys = policy.get(key);
                                    for (String pol : policys) {
                                        Junction conJun = Restrictions.conjunction();
                                        // 登记节点需要单独处理
                                        if ("regist".equals(pol) || "dengji".equals(pol)) {
                                            if (isG6Version()) {// G6版本包含登记、分发
                                                if ("regist".equals(pol) && isOpenRegister()) {
                                                    // G6版本,在开启了收文登记的情况下，查询收文待登记的数据
                                                    conJun.add(Restrictions.eq("" + aliasAffair + ".app", ApplicationCategoryEnum.edocRegister.getKey()));
                                                }
                                                else {
                                                    // 是V5-G6版本,则登记查询待分发的数据
                                                    conJun.add(Restrictions.eq("" + aliasAffair + ".app", ApplicationCategoryEnum.edocRecDistribute.getKey()));
                                                }
                                            }
                                            else {
                                                if (("dengji").equals(pol)) {// A8只有登记
                                                    conJun.add(Restrictions.eq("" + aliasAffair + ".app", ApplicationCategoryEnum.edocRegister.getKey()));
                                                }
                                            }
                                        }
                                        else {
                                            conJun.add(Restrictions.eq("" + aliasAffair + ".app", key));

                                            if (pol.endsWith("_ban") || pol.endsWith("_yue")) {
                                                pol = pol.replace("_ban", "");
                                                int subapp = ApplicationSubCategoryEnum.edocRecHandle.getKey();
                                                if (pol.endsWith("_yue")) {
                                                    pol = pol.replace("_yue", "");
                                                    subapp = ApplicationSubCategoryEnum.edocRecRead.getKey();
                                                }
                                                conJun.add(Restrictions.eq("" + aliasAffair + ".subApp", subapp));
                                            }

                                            if ("zhihui".equals(pol)) {
                                                conJun.add(Restrictions.or(Restrictions.eq("" + aliasAffair + ".nodePolicy", "zhihui"), Restrictions.eq("" + aliasAffair + ".nodePolicy", "inform")));
                                            }
                                            else {
                                                conJun.add(Restrictions.eq("" + aliasAffair + ".nodePolicy", pol));
                                            }
                                        }
                                        disJunctionApp.add(conJun);
                                    }
                                }
                            }
                        }
                        // wangjingjing 子应用subapp begin
                        List<String> list;
                        Integer key;
                        if (!subAppPolicy.isEmpty()) {
                            for (Iterator<Integer> iterator = subAppPolicy.keySet().iterator(); iterator.hasNext();) {
                                key = iterator.next();
                                list = subAppPolicy.get(key);
                                if (!list.isEmpty()) {
                                    if (list.contains("all")) {
                                        disJunctionApp.add(Restrictions.eq("" + aliasAffair + ".subApp", key));
                                    }
                                    else {
                                        disJunctionApp.add(Restrictions.and(Restrictions.eq("" + aliasAffair + ".subApp", key), Restrictions.in("" + aliasAffair + ".nodePolicy", list)));
                                    }
                                }
                            }
                        }
                        // wangjingjing end

                        if (!app2SubApp.isEmpty()) {
                            for (Iterator<Map.Entry<Integer, List<Integer>>> iterator = app2SubApp.entrySet().iterator(); iterator.hasNext();) {
                                Map.Entry<Integer, List<Integer>> entry = iterator.next();
                                if (!entry.getValue().isEmpty()) {
                                    disJunctionApp.add(Restrictions.and(Restrictions.eq("" + aliasAffair + ".app", entry.getKey()), Restrictions.in("" + aliasAffair + ".subApp", entry.getValue())));
                                }
                            }
                        }
                        if (isPortalMore) {
                            morePortalJunction.add(disJunctionApp);
                        }
                        else {
                            junctionSource.add(disJunctionApp);
                        }
                    }
                    break;
                case expectedProcessTime:
                    Junction disJunctionProcessTime = Restrictions.conjunction();
                    if (Strings.isNotBlank(model.getSearchValue1())) {
                        Date startDate = Datetimes.parseNoTimeZone(model.getSearchValue1(), null);
                        disJunctionProcessTime.add(Restrictions.ge("" + aliasAffair + ".expectedProcessTime", startDate));
                    }
                    if (Strings.isNotBlank(model.getSearchValue2())) {
                        Date startDate = Datetimes.parseNoTimeZone(model.getSearchValue2(), null);
                        disJunctionProcessTime.add(Restrictions.le("" + aliasAffair + ".expectedProcessTime", startDate));
                    }
                    if (isPortalMore) {
                        morePortalJunction.add(disJunctionProcessTime);
                    }
                    else {
                        junctionSource.add(disJunctionProcessTime);
                    }
                    break;
                case workflowState://流程状态
					if(Strings.isNotBlank(model.getSearchValue1())){
						String value = model.getSearchValue1();
						String[] sv = value.split(",");
						Junction junction = Restrictions.disjunction();
						for(String s : sv){
						    if("catagory_pending".equals(s)){//进行中
						    	Junction cj = Restrictions.conjunction();
								cj.add(Restrictions.eq(""+aliasAffair+".summaryState",CollaborationEnum.flowState.run.ordinal()));
								junction.add(cj);
						    } else if ("catagory_finish".equals(s)) {//已结束(协同)/已办结(公文)
						    	Junction cj = Restrictions.conjunction();
								cj.add(Restrictions.eq(""+aliasAffair+".summaryState",CollaborationEnum.flowState.finish.ordinal()));
								junction.add(cj);
						    } else if ("catagory_terminate".equals(s)) {//已终止
						    	Junction cj = Restrictions.conjunction();
								cj.add(Restrictions.eq(""+aliasAffair+".summaryState",CollaborationEnum.flowState.terminate.ordinal()));
								junction.add(cj);
						    }
						}
						if(isPortalMore){
							morePortalJunction.add(junction);
						}else{
							junctionSource.add(junction);
						}
					}
                // case 自由协同:
                // break;
                // case 其他:
                // break;
                }
            }
        }
        componentJunction.add(junctionSource);
        componentJunction.add(morePortalJunction);
        criteria.add(componentJunction);
    }

	private class Templete2Sql{
		String sql;
		Type[] types;
		Object[] values;
	}
	private Templete2Sql parseTemplete2Sql(String templeteIds){
		StringBuilder sb = new StringBuilder(" templete_id ");
		String[] ids = templeteIds.split(",");
		Type[] types = null;
		Object[] values = null;
		try {
			if(ids.length ==1){
				sb.append("=? ");
				types = new Type[]{Hibernate.LONG};
				values = new Object[]{Long.parseLong(ids[0])};
			}else{
				types = new Type[ids.length];
				values = new Object[ids.length];
				sb.append("in (");
				for(int i = 0 ; i < ids.length ;i++){
					if(i !=0){
						sb.append(",");
					}
					sb.append("?");
					types[i] = Hibernate.LONG;
					values[i] = Long.parseLong(ids[i]);
				}
				sb.append(")");
			}
		} catch (Exception e) {
			log.error("组装首页模板流程sql:",e);
			//如果组装templeteId出了问题。说明id传入的不正确。templete_id is not null.
			sb.append(" is not null ");
			types = new Type[0];
			values = new Object[0];
		}

		Templete2Sql result = new Templete2Sql();
		result.sql = sb.toString();
		result.types = types;
		result.values = values;
		return result;
	}
	
	private Junction getJunction4AppAgent(ApplicationCategoryEnum app){
		Junction junction = Restrictions.conjunction();
		if(app.equals(ApplicationCategoryEnum.edoc)){
			Integer[] edocType = new Integer[]{
					ApplicationCategoryEnum.edoc.key(), 
					ApplicationCategoryEnum.edocSend.key(), 
					ApplicationCategoryEnum.edocRec.key(),
					ApplicationCategoryEnum.edocSign.key(),
					ApplicationCategoryEnum.edocRegister.key(),
					ApplicationCategoryEnum.exSend.key(),
					ApplicationCategoryEnum.exSign.key(),
					ApplicationCategoryEnum.exchange.key(),
					ApplicationCategoryEnum.edocRecDistribute.key()
			};
			junction.add(Restrictions.in("app", edocType));
		} else if(app.equals(ApplicationCategoryEnum.meeting)){
			Integer[] edocType = new Integer[]{
				ApplicationCategoryEnum.meeting.key(),
				ApplicationCategoryEnum.meetingroom.key(), 
			};
			junction.add(Restrictions.and(Restrictions.in("app", edocType), 
					Restrictions.ne("subState", SubStateEnum.meeting_pending_periodicity.key())));
		} else if(app.equals(ApplicationCategoryEnum.info)){//政务首页信息报送查询
			Integer[] edocType = new Integer[]{
				ApplicationCategoryEnum.info.key(), 
				ApplicationCategoryEnum.infoStat.key(), 
			};
			junction.add(Restrictions.in("app", edocType));
		} else{
			junction.add(Restrictions.eq("app", app.key()));
		}

		List<AgentModel> agentModelList = this.getAppAgents(app.key());
		if(agentModelList != null && !agentModelList.isEmpty()){
			if(this.agentToFlag != null){
				if(this.agentToFlag){//被代理了
					if (app.equals(ApplicationCategoryEnum.meeting)) {
						//receiveTime是会议的开始时间
						Junction junction1 = Restrictions.disjunction();
						junction1.add(Restrictions.lt("completeTime", agentModelList.get(0).getStartDate()));
						junction1.add(Restrictions.gt("receiveTime", agentModelList.get(0).getEndDate()));
						junction.add(junction1);
					} else {
						junction.add(Restrictions.lt("receiveTime", agentModelList.get(0).getStartDate()));
					}
				}else if(this.containAgent){//我是代理人--代理多个人
					Junction agentJun = Restrictions.disjunction();
					for(AgentModel model : agentModelList){
			 			Junction agent = Restrictions.conjunction()
			 			.add(Restrictions.eq("memberId", model.getAgentToId()))
			 			.add(Restrictions.gt("receiveTime", model.getStartDate()));
			 			agentJun.add(agent);
			 		}
					junction.add(agentJun);
				}
			}
		}
		return junction;
	}
	public String getSql4AppAgent(ApplicationCategoryEnum app,String alias,Map<String,Object> parameter){
		return getSql4AppAgent(app,alias,parameter,false);
	}
	
	public String getSql4AppAgent(ApplicationCategoryEnum app,String alias,Map<String,Object> parameter,boolean isHQL){
		
		String _receiveTime = isHQL ? "receiveTime" : "receive_time";
		String _memberId = isHQL ? "memberId" : "member_id";
		
		StringBuilder hql = new StringBuilder();
		hql.append(" ( " );
		if(app.equals(ApplicationCategoryEnum.edoc)){
			Integer[] edocType = new Integer[]{ApplicationCategoryEnum.edocSend.key(), ApplicationCategoryEnum.edocRec.key(),ApplicationCategoryEnum.edocSign.key(),ApplicationCategoryEnum.edocRegister.key(),
					ApplicationCategoryEnum.exSend.key(),ApplicationCategoryEnum.exSign.key(),ApplicationCategoryEnum.edocRecDistribute.key()};
			hql.append(alias+".app in (:getHql4AppAgentapp"+app.key()+") ");
			parameter.put("getHql4AppAgentapp"+app.key(), edocType);
		}else{
			hql.append(alias+".app = :getHql4AppAgentapp"+app.key());
			parameter.put("getHql4AppAgentapp"+app.key(), app.key());
		}

		List<AgentModel> agentModelList = this.getAppAgents(app.key());
		if(agentModelList != null && !agentModelList.isEmpty()){
			if(this.agentToFlag != null){
				if(this.agentToFlag){//被代理了
					hql.append(" and ");
					hql.append(alias+"."+_receiveTime+" < :getHql4AppAgentreceiveTime"+app.key());
					parameter.put("getHql4AppAgentreceiveTime"+app.key(), agentModelList.get(0).getStartDate());
				}else if(this.containAgent){//我是代理人--代理多个人
					hql.append(" and ");
					hql.append("(");
					int i = 0 ;
					for(AgentModel model : agentModelList){
						i++;
						if(i>1)
							hql.append(" or ");
						hql.append(" ( ");
						hql.append(alias+"."+_memberId+" = :getHql4AppAgentmemberId"+app.key()+i);
						hql.append(" and ");
						hql.append(alias+"."+_receiveTime+" >:getHql4AppAgentreceiveTime"+app.key()+i);
						hql.append(" ) ");
						parameter.put("getHql4AppAgentmemberId"+app.key()+i, model.getAgentToId());
						parameter.put("getHql4AppAgentreceiveTime"+app.key()+i, model.getStartDate());
			 		}
					hql.append(")");
				}
			}
		}
		hql.append(" ) " );
		return hql.toString();
	}
	private Junction getJunction4ColAgent(){
		Junction junction = Restrictions.conjunction();
		junction.add(Restrictions.eq("app", ApplicationCategoryEnum.collaboration.key()));
		
		List<AgentModel> agentModelList = this.getAppAgents(ApplicationCategoryEnum.collaboration.key());
		if(agentModelList != null && !agentModelList.isEmpty()){
			if(this.agentToFlag != null){
			    Junction agentJunJoin = Restrictions.conjunction();
				if(this.agentToFlag){//被代理了
					for( AgentModel model : agentModelList){
						List<AgentDetailModel> details = model.getAgentDetail();
						Junction agentJun = Restrictions.disjunction();
			            if(model.isHasCol() && model.isHasTemplate() && Strings.isEmpty(details)){
			                //全部协同
			            }
			            else{
			                boolean c = model.isHasCol();
			                boolean t = model.isHasTemplate();
			                    
			                if(c && !t){ //仅自由协同
			                    Junction con = Restrictions.conjunction();
			                    con.add(Restrictions.isNotNull("templeteId"));
			                    con.add(Restrictions.gt("receiveTime", model.getStartDate()));
			                    agentJun.add(con);
			                }
			                else if(t && !c){     //仅模板
			                    if(Strings.isEmpty(details)){
			                        //模板(全部)
			                        Junction con = Restrictions.conjunction();
		                            con.add(Restrictions.isNull("templeteId"));
		                            con.add(Restrictions.gt("receiveTime", model.getStartDate()));
		                            agentJun.add(con);
			                    }
			                    else{
			                        //指定模板
			                        List<Long> templateIds = new ArrayList<Long>();
			                        for (AgentDetailModel agentDetailModel : details) {
			                            templateIds.add(agentDetailModel.getEntityId());
			                        }
			                        Junction con = Restrictions.conjunction();
		                            con.add(Restrictions.gt("receiveTime", model.getStartDate()));
		                            con.add(Restrictions.or(Restrictions.not(Restrictions.in("templeteId", templateIds)), Restrictions.isNull("templeteId")));
		                            agentJun.add(con);
			                    }
			                }
			                else if(t && c){  //自由协同+部分模板协同
			                   
			                    List<Long> templateIds = new ArrayList<Long>();
			                    if(Strings.isNotEmpty(details)){
			                        for (AgentDetailModel agentDetailModel : details) {
			                            templateIds.add(agentDetailModel.getEntityId());
			                        }
			                    }
			                    Junction con = Restrictions.conjunction();
	                            con.add(Restrictions.gt("receiveTime", model.getStartDate()));
	                            con.add(Restrictions.and(Restrictions.not(Restrictions.in("templeteId", templateIds)), Restrictions.isNotNull("templeteId")));
	                            agentJun.add(con);
			                }
			            }
	    		       agentJun.add(Restrictions.lt("receiveTime", model.getStartDate()));
	    		       agentJunJoin.add(agentJun);
					}
    		       junction.add(agentJunJoin);
				}
			}
		} 
		return junction;
	}
	public String getSql4ColAgent(String alias,Map<String,Object> parameter){
		return getSql4ColAgent(alias,parameter,false);
	}
	
	
	
	public String getSql4ColAgent(String alias,Map<String,Object> parameter,boolean isHQL){
		
		String _templeteId =isHQL ? "templeteId":"templete_id";
		String _receiveTime =isHQL ? "receiveTime":"receive_time";
		
		StringBuilder hql = new StringBuilder();
		hql.append("(");
		hql.append(alias+".app = :getHql4ColAgentapp ");
		parameter.put("getHql4ColAgentapp", ApplicationCategoryEnum.collaboration.key());
        
		List<AgentModel> agentModelList = this.getAppAgents(ApplicationCategoryEnum.collaboration.key());
		if(agentModelList != null && !agentModelList.isEmpty()){
			if(this.agentToFlag != null){
				if(this.agentToFlag){//被代理了
					StringBuilder agentHql = new StringBuilder();
					for (int i = 0; i < agentModelList.size(); i++) {
						boolean needOR = true;
						
						if(i > 0){
							agentHql.append(" and ");
						}
						agentHql.append(" ( ");
						AgentModel model = agentModelList.get(i);
						List<AgentDetailModel> details = model.getAgentDetail();
	                    
	                    if(model.isHasCol() && model.isHasTemplate() && Strings.isEmpty(details)){
	                        //全部协同
	                        needOR = false ;
	                    }
	                    else{
	                        boolean c = model.isHasCol();
	                        boolean t = model.isHasTemplate();
	                            
	                        if(c && !t){ //仅自由协同
	                            Junction con = Restrictions.conjunction();
	                            con.add(Restrictions.isNotNull("templeteId"));
	                            con.add(Restrictions.gt("receiveTime", model.getStartDate()));
	                            agentHql.append(" ( ");
	                            agentHql.append(alias + "."+_templeteId+" is not null  AND " + alias+"."+_receiveTime+" > :agentStartDate"+i);
	                            agentHql.append(" ) ");
	                            parameter.put("agentStartDate"+i,  model.getStartDate());
	                        }
	                        else if(t && !c){     //仅模板
	                            if(Strings.isEmpty(details)){
	                              
	                            	agentHql.append(" ( ");
	                            	agentHql.append(alias + "."+_templeteId+" is null  AND " + alias+"."+_receiveTime+" > :agentStartDate"+i);
	                            	agentHql.append(" ) ");
	                                
	                                parameter.put("agentStartDate"+i,  model.getStartDate());
	                            }
	                            else{
	                                //指定模板
	                                List<Long> templateIds = new ArrayList<Long>();
	                                for (AgentDetailModel agentDetailModel : details) {
	                                    templateIds.add(agentDetailModel.getEntityId());
	                                }
	                                
	                                agentHql.append(" ( ");
	                                agentHql.append(alias+"."+_receiveTime+" > :agentStartDate"+i);
	                                agentHql.append(" AND ");
	                                agentHql.append(alias + "."+_templeteId+" NOT IN (:templateids"+i+")  OR " + alias+"."+_templeteId+"  is null ");
	                                agentHql.append(" ) ");
	                                
	                                parameter.put("agentStartDate"+i,  model.getStartDate());
	                                parameter.put("templateids"+i,  templateIds);
	                            }
	                        }
	                        else if(t && c){  //自由协同+部分模板协同
	                           
	                            List<Long> templateIds = new ArrayList<Long>();
	                            if(Strings.isNotEmpty(details)){
	                                for (AgentDetailModel agentDetailModel : details) {
	                                    templateIds.add(agentDetailModel.getEntityId());
	                                }
	                            }
	                            
	                            agentHql.append(" ( ");
	                            agentHql.append(alias+"."+_receiveTime+" > :agentStartDate"+i);
	                            agentHql.append(" AND ");
	                            agentHql.append(alias + "."+_templeteId+" NOT IN (:templateids"+i+")  AND " + alias+"."+_templeteId+"  is  not null ");
	                            agentHql.append(" ) ");
	                            
	                            parameter.put("agentStartDate"+i,  model.getStartDate());
	                            parameter.put("templateids"+i,  templateIds);
	                        }
	                    }
	                    if(needOR) {
	                    	agentHql.append(" OR ");
	                    }
	                    agentHql.append(alias+"."+_receiveTime+" < :agentStartDatelt"+i);
	                    agentHql.append("  ) ");
	                    parameter.put("agentStartDatelt"+i,  model.getStartDate());
					}
                    hql.append( " AND ( ").append(agentHql).append(" )");
                
				}
			}
		}
		hql.append(")");
		return hql.toString();
	}


	@SuppressWarnings("unchecked")
	public List<CtpAffair> getPendingAffair(AffairManager affairManager, FlipInfo fi){
	    List<CtpAffair> list = new ArrayList<CtpAffair>();
	    try {
            list = DBAgent.findByCriteria(getSearchDetached(), fi);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return list;
	}
	
	/**
	 * 获取待办事项列表
	 * @param affairManager
	 * @param fi
	 * @return
	 */
	public List<CtpAffair> getPendingAffairList(AffairManager affairManager, FlipInfo fi){
	    List<CtpAffair> list = null;
	    try {
	    	DetachedCriteria criteria = DetachedCriteria.forClass(CtpAffair.class);
	    	criteria=getSearchDetached();
	    	Junction junction = Restrictions.disjunction();
	    	junction.add(Restrictions.and(Restrictions.eq("state", StateEnum.col_pending.getKey()), Restrictions.ne("subState", SubStateEnum.col_pending_ZCDB.key())));
			criteria.add(junction);
            list =  DBAgent.findByCriteria(criteria, fi);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return list;
	}
	/**
	 * 获取暂存待办事项列表
	 * @param affairManager
	 * @param fi
	 * @return
	 */
	public List<CtpAffair> getZcdbAffairList(AffairManager affairManager, FlipInfo fi){
	    List<CtpAffair> list = null;
	    try {
	    	DetachedCriteria criteria = DetachedCriteria.forClass(CtpAffair.class);
	    	criteria=getSearchDetached();
	    	Junction junction = Restrictions.disjunction();
	    	junction.add(Restrictions.and(Restrictions.eq("state", StateEnum.col_pending.getKey()), Restrictions.eq("subState", SubStateEnum.col_pending_ZCDB.key())));
			criteria.add(junction);
            list =  DBAgent.findByCriteria(criteria, fi);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return list;
	}
	/**
	 * 根据紧急程度和应用类别获取待办列表
	 * @param affairManager
	 * @param fi
	 * @return
	 */
	public List<CtpAffair> getAffairsByCategoryAndImpLevl(AffairManager affairManager, FlipInfo fi,List<Integer> enums,int levelState){
	    List<CtpAffair> list = null;
	    try {
	    	/*//公文设置紧急程度
			if(affair.getImportantLevel()!=null&&affair.getImportantLevel()==3){//非常重要
				topExigency++;
			}else if(affair.getImportantLevel()!=null&&affair.getImportantLevel()==2){//重要
				exigency++;
			}else{//普通或者没有设置重要程度的
				commonExigency++;
			}*/
	    	DetachedCriteria criteria = DetachedCriteria.forClass(CtpAffair.class);
	    	criteria=getSearchDetached();
	    	Junction junction = Restrictions.disjunction();
	    	if(levelState==1){//普通的重要程度包含importantLevel为null的情况
	    		junction.add(Restrictions.and(Restrictions.in("app",enums), Restrictions.or(Restrictions.eq("importantLevel", levelState), Restrictions.isNull("importantLevel"))));
	    	}else{
	    		junction.add(Restrictions.and(Restrictions.in("app",enums), Restrictions.eq("importantLevel", levelState)));
	    	}
			criteria.add(junction);
            list =  DBAgent.findByCriteria(criteria, fi);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return list;
		//return DBAgent.findByCriteria(getSearchDetached(), fi);
	}
	/**
	 * 根据紧急程度和应用类别获取待办列表
	 * @param affairManager
	 * @param fi
	 * @return
	 */
	public List<CtpAffair> getAffairsIsOverTime(AffairManager affairManager, FlipInfo fi,boolean isOverTime){
	    List<CtpAffair> list = null;
	    try {
	    	DetachedCriteria criteria = DetachedCriteria.forClass(CtpAffair.class);
	    	criteria=getSearchDetached();
	    	Junction junction = Restrictions.disjunction();
	    	if(isOverTime){
	    		junction.add(Restrictions.eq("coverTime", true));
	    	}else{
	    		junction.add(Restrictions.ne("coverTime", true));
	    	}
			criteria.add(junction);
            list =  DBAgent.findByCriteria(criteria, fi);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return list;
		//return DBAgent.findByCriteria(getSearchDetached(), fi);
	}
	/**
	 * 根据重要程度获取 待办事 项列表
	 * @param affairManager
	 * @param fi
	 * @return
	 */
	public List<CtpAffair> getImptLevelAffairList(AffairManager affairManager, FlipInfo fi,int state){
	    List<CtpAffair> list = null;
	    try {
	    	DetachedCriteria criteria = DetachedCriteria.forClass(CtpAffair.class);
	    	criteria=getSearchDetached();
	    	Junction junction = Restrictions.disjunction();
	    	Junction cj = Restrictions.conjunction();
	    	cj.add(Restrictions.eq("importantLevel", state));
	    	junction.add(cj);
			criteria.add(junction);
            list =  DBAgent.findByCriteria(criteria, fi);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return list;
		//return DBAgent.findByCriteria(getSearchDetached(), fi);
	}
	public List<CtpAffair> getTrackAffair(AffairManager affairManager, FlipInfo fi){
		//return DBAgent.findByCriteria(getTrackSearchDetached(), fi);
	    List<CtpAffair> list = null;
	    if(fi.getSortField() == null){
	        fi.setSortField("createDate");
            fi.setSortOrder("desc");
        }
		try {
            list =  DBAgent.findByCriteria(getTrackSearchDetached(), fi);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
		return list;
	}
	public List<CtpAffair> getSectionAffair(AffairManager affairManager, int state, FlipInfo fi){
	     return getSectionAffair(affairManager, state, fi, false);
	}
	//防止core提交后引用报错，先添加一个方法。后面再删除
	public List<CtpAffair> getSectionAffair(AffairManager affairManager, int state, FlipInfo fi,boolean isGroupBy){
	    return getSectionAffair(affairManager, state, fi, false,false);
	}
	/**
     * 
     * @param affairManager
     * @param state 状态
     * @param fi 分页
     * @param isGroupBy 是否分组查询显示最后一条
     * @param isProtalQuery 是否直接是首页栏目的查询
     * @return
     */
    @SuppressWarnings("unchecked")
	public List<CtpAffair> getSectionAffair(AffairManager affairManager, int state, FlipInfo fi,boolean isGroupBy,boolean isProtalQuery){
	    List<CtpAffair> list = new ArrayList<CtpAffair>();
	    if(fi.getSortField() == null){
            if(state == StateEnum.col_done.key()) {
                fi.setSortField("completeTime");
            } else if(state == StateEnum.col_pending.key()){
                fi.setSortField("receiveTime");
            }else {
                fi.setSortField("createDate");
            }
            fi.setSortOrder("desc");
        }
	    try {
            if (isGroupBy) {
                DetachedCriteria detachedCriteria = getSectionSearchDetached(state,isGroupBy);
                //首页栏目
                if (isProtalQuery) {
                    list =  DBAgent.findByCriteria(detachedCriteria,fi);
                } else { //更多
                    list =  DBAgent.findByCriteria(detachedCriteria,fi);
                }
            } else {
                list =  DBAgent.findByCriteria(getSectionSearchDetached(state,isGroupBy),fi);
            }
        } catch (Exception e) {
           log.error("",e);
        }
	    return list;
	}
	public int getPendingCount(AffairManager affairManager){
		return DBAgent.count(getSearchDetached());
	}
	public int getTrackCount(AffairManager affairManager){
		return DBAgent.count(getTrackSearchDetached());
	}

	public List<CtpAffair> getAgentPendingAffair(AffairManager affairManager, FlipInfo fi){
		DetachedCriteria c = getAgentSearchDetached();
		if(c != null)
			return DBAgent.findByCriteria(c, fi);
		else
			return new ArrayList<CtpAffair>(0);
	}
	public int getAgentPendingCount(){
		DetachedCriteria c = getAgentSearchDetached();
		if(c != null)
			return DBAgent.count(c);
		else
			return 0;
	}
	/**
	 * 不分页
	 * @param affairManager
	 * @return
	 */
	public List<CtpAffair> getPendingAffair(AffairManager affairManager) {
		List<CtpAffair> list = null;
	    try {
            list =  DBAgent.findByCriteria(getSearchDetached());
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return list;
	}
	
	public List<Object[]> group(List<Integer> appEnum,String... groupByPropertyName){
		DetachedCriteria dc = getSearchDetached();
		ProjectionList pl = Projections.projectionList(); 
		if(groupByPropertyName.length==2){
			pl.add(Projections.groupProperty(groupByPropertyName[0]));
			pl.add(Projections.groupProperty(groupByPropertyName[1]));
		}else{
			pl.add(Projections.groupProperty(groupByPropertyName[0]));
		}
		pl.add(Projections.rowCount()); 
		dc.setProjection(pl);
		if(appEnum.size()>0){
			dc.add(Restrictions.in("app", appEnum));
		}
		return DBAgent.findByCriteria(dc);
	}
	
	public List<CtpAffair> getCollAffairs(AffairManager affairManager, FlipInfo fp,boolean isTemplete) {
		List<CtpAffair> list = null;
	    try {
	    	DetachedCriteria criteria = DetachedCriteria.forClass(CtpAffair.class);
	    	criteria=getSearchDetached();
	    	Junction junction = Restrictions.disjunction();
	    	Junction cj = Restrictions.conjunction();
	    	cj.add(Restrictions.eq("app", ApplicationCategoryEnum.collaboration.getKey()));
	    	if(isTemplete){
	    		cj.add(Restrictions.isNotNull("templeteId"));
	    	}else{
	    		cj.add(Restrictions.isNull("templeteId"));
	    	}
	    	junction.add(cj);
			criteria.add(junction);
            list = DBAgent.findByCriteria(criteria, fp);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return list;
		
	}
	public List<CtpAffair> getAffairCountByApp(AffairManager affairManager,
			FlipInfo fp, List<Integer> appEnums) {
		 	List<CtpAffair> list = null;
		    try {
		    	DetachedCriteria criteria = DetachedCriteria.forClass(CtpAffair.class);
		    	criteria=getSearchDetached();
		    	Junction junction = Restrictions.disjunction();//or
		    	Junction cj = Restrictions.conjunction();//and
		    	if(appEnums.size()==1&&appEnums.get(0)==ApplicationCategoryEnum.news.getKey()){
		    		//新闻
		    		junction.add(Restrictions.eq("app", ApplicationCategoryEnum.news.getKey()));
		    		//调查
		    		junction.add(Restrictions.and(Restrictions.eq("app", ApplicationCategoryEnum.inquiry.getKey()), Restrictions.eq("subApp", 0)));
		    		//讨论
		    		junction.add(Restrictions.eq("app", ApplicationCategoryEnum.bbs.getKey()));
		    		//公告
		    		junction.add(Restrictions.eq("app", ApplicationCategoryEnum.bulletin.getKey()));
		    		cj.add(junction);
		    		criteria.add(cj);
		    	}else if(appEnums.size()==1&&appEnums.get(0)==ApplicationCategoryEnum.inquiry.getKey()){
		    		//调查
		    		cj.add(Restrictions.and(Restrictions.eq("app", ApplicationCategoryEnum.inquiry.getKey()), Restrictions.eq("subApp", 1)));
			    	junction.add(cj);
					criteria.add(junction);
		    	}else{
		    		cj.add(Restrictions.in("app", appEnums));
			    	junction.add(cj);
					criteria.add(junction);
		    	}
	            list =  DBAgent.findByCriteria(criteria, fp);
	        } catch (Exception e) {
	            log.error(e.getMessage(),e);
	        }
	        return list;
	}
	public List<CtpAffair> getPublicInformationCount(AffairManager affairManager,
			FlipInfo fp,List<Integer> appEnums) {
		List<CtpAffair> list = null;
	    try {
	    	DetachedCriteria criteria = DetachedCriteria.forClass(CtpAffair.class);
	    	criteria=getSearchDetached();
	    	Junction junction = Restrictions.disjunction();
	    	Junction cj = Restrictions.conjunction();
	    	//公告，新闻，调查，讨论
	    	List<Integer> publicInformatEnums = new ArrayList<Integer>();
	    	publicInformatEnums.add(ApplicationCategoryEnum.bulletin.getKey());// 公告
	    	publicInformatEnums.add(ApplicationCategoryEnum.news.getKey());// 新闻
	    	publicInformatEnums.add(ApplicationCategoryEnum.bbs.getKey());// 讨论
	    	publicInformatEnums.add(ApplicationCategoryEnum.inquiry.getKey());// 调查
	    	cj.add(Restrictions.in("app", publicInformatEnums));
	    	junction.add(cj);
			criteria.add(junction);
            list =  DBAgent.findByCriteria(criteria, fp);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return list;
	}
    public static boolean showBanwenYuewen(Long accountId)
    {   
    	ConfigManager configManager = (ConfigManager)AppContext.getBean("configManager");
        ConfigItem configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, "banwenYuewen", accountId);
        if(configItem==null)
        {
            configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, "banwenYuewen", ConfigItem.Default_Account_Id);
        }
        if(configItem==null){return true;}
        return "yes".equals(configItem.getConfigValue());
    }
    /**
	 * G6是否开启登记功能
	 */
    public static boolean isOpenRegister()
    {   
    	ConfigManager configManager = (ConfigManager)AppContext.getBean("configManager");
        ConfigItem configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, "openRegister", AppContext.getCurrentUser().getLoginAccount());
        if(configItem==null)
        {
            configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, "openRegister", ConfigItem.Default_Account_Id);
        }
        if(configItem==null){return true;}
        return "yes".equals(configItem.getConfigValue());
    }
    /**
     * 是否为V5-G6版本
     * @return
     */
    public static boolean isG6Version(){
        String isG6=SystemProperties.getInstance().getProperty("edoc.isG6");
        if("true".equals(isG6)){
        	return true;
        }
        return false;
    }
	private  List<Integer>  getAllEdocApplicationCategoryEnumKey(){
		List<Integer> keys = new ArrayList<Integer>();
		keys.add(ApplicationCategoryEnum.edoc.key());
		keys.add(ApplicationCategoryEnum.edocRec.key());
		keys.add(ApplicationCategoryEnum.edocRegister.key());
		keys.add(ApplicationCategoryEnum.edocSend.key());
		keys.add(ApplicationCategoryEnum.edocSign.key());
		keys.add(ApplicationCategoryEnum.exSend.key());
		keys.add(ApplicationCategoryEnum.exSign.key());
		keys.add(ApplicationCategoryEnum.exchange.key());
		keys.add(ApplicationCategoryEnum.info.key());
		return keys;
	} 
	
	public  void addSourceSearchCondition(Map<String,String> preference,boolean isPortalMore){
    		String tempStr = preference.get("sources_templete_pending_value");
    		if(Strings.isNotBlank(tempStr)){
    			TemplateManager templateManager = (TemplateManager)AppContext.getBean("templateManager");
    			try {
					tempStr = templateManager.getAllTemplateCategoryIdsAndTemplateIds(tempStr);
					preference.put("sources_templete_pending_value", tempStr);
				} catch (BusinessException e) {
					 log.error(e.getMessage(),e);
				}
    			this.addSearch(SearchCondition.templete, tempStr, null);
    		}
            tempStr = preference.get("sources_Policy_value");
            if(Strings.isNotBlank(tempStr)){
            	 this.addSearch(SearchCondition.policy4Portal, tempStr, null,isPortalMore);
            }
           
            tempStr = preference.get("sources_importLevel_value");
            if(Strings.isNotBlank(tempStr)){
            	this.addSearch(SearchCondition.importLevel, tempStr, null,isPortalMore);
            }
            tempStr = preference.get("sources_catagory_value");
            if(Strings.isNotBlank(tempStr)){
            	this.addSearch(SearchCondition.catagory, tempStr, null,isPortalMore);
            }
            tempStr = preference.get("sources_track_catagory_value");
            if(Strings.isNotBlank(tempStr)){
            	this.addSearch(SearchCondition.catagory, tempStr, null,isPortalMore);
            }
            tempStr = preference.get("sources_handlingState_value");
            if(Strings.isNotBlank(tempStr)){
            	this.addSearch(SearchCondition.handlingState, tempStr, null,isPortalMore);
            }
            tempStr=preference.get("sources_overTime_name");
            if(Strings.isNotBlank(tempStr)){
            	this.addSearch(SearchCondition.overTime, null, null,isPortalMore);
            }
            tempStr=preference.get("sources_myLeader");//领导发给我的
            if (Strings.isNotBlank(tempStr)){
            	this.addSearch(SearchCondition.myLeader, null, null,isPortalMore);
            }
            tempStr=preference.get("sources_myDept");//本部门的
            if (Strings.isNotBlank(tempStr)){
            	this.addSearch(SearchCondition.myDept, null, null,isPortalMore);
            }
            tempStr=preference.get("sources_addToMe");//加签给我的
            if (Strings.isNotBlank(tempStr)){
            	this.addSearch(SearchCondition.addToMe, null, null,isPortalMore);
            }
            tempStr=preference.get("sources_processState_value");//流程状态
            if (Strings.isNotBlank(tempStr)){
            	this.addSearch(SearchCondition.workflowState, tempStr, null,isPortalMore);
            }
            String  sourcesRelation =preference.get("sources_relation_select");
            this.setIsSourcesRelationOr("0".equals(sourcesRelation));
            
	}
	
	public Map<Long,Long> getPendingTemplateCategoryIds(Long memberId){
        String hql="select t.id,t.categoryId from CtpAffair a, CtpTemplate t where a.templeteId = t.id and a.memberId = :memberId and t.categoryId is not null ";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("memberId", memberId);
        if(this.state != null){
        	hql += " and a.state = :state ";
        	map.put("state", this.state.key());
        }
        List list = DBAgent.find(hql.toString(), map);
        
        Map<Long,Long> ret = new HashMap<Long,Long>();
        if(Strings.isNotEmpty(list)) {
            for (Object obj : list) {
                Object[] ids = (Object[]) obj;
                Long templateId = (((Number) ids[0]).longValue());
                Long categoryId = (((Number) ids[1]).longValue());
                ret.put(templateId, categoryId);
            }
        }
        return ret;
    }
	
	private Disjunction getJunction4Agent ()  {
		Disjunction disjunction = Restrictions.disjunction();
		
		List<AgentModel> agentModelList = this.getAgentModelList();
		List<Integer> allAgentApp = new ArrayList<Integer>();
		for (AgentModel agentModel : agentModelList) {
			Disjunction AgentDisjunction = Restrictions.disjunction();
			List<Integer> agentApp = new ArrayList<Integer>();
			//获取app,排除协同\会议\文化建设审批 查询语句不一致,需要单独处理
			agentApp.addAll(getAppByAgentModel(agentModel));
			
			List<AgentModel> colAgentModelList = this.getAppAgents(ApplicationCategoryEnum.collaboration.key());
			if(colAgentModelList != null && !colAgentModelList.isEmpty()){//代理中选择了协同
				List<AgentDetailModel> details = agentModel.getAgentDetail();
				//全部协同
				if(agentModel.isHasCol() && agentModel.isHasTemplate() && Strings.isEmpty(details)){
					agentApp.add(ApplicationCategoryEnum.collaboration.key());
					AgentDisjunction.add(this.getAgentJunction(agentModel, agentApp));
		        } else {//代理中有协同,并且只选择了一部分
		        	//除协同外代理
		        	if(agentApp.size() > 0) {
		        		AgentDisjunction.add(this.getAgentJunction(agentModel, agentApp));
		        	}
		        	//处理协同
		        	AgentDisjunction.add(this.getAgentJunction4Col(agentModel));
		        	agentApp.add(ApplicationCategoryEnum.collaboration.key());
		        }
			} else {//代理中没有设置协同
				if(agentApp.size() > 0) {
					AgentDisjunction.add(this.getAgentJunction(agentModel, agentApp));
				}
			}
			//文化建设审批处理,需要考虑subApp
			Junction pubAuditJunction = this.getPubAuditJunction(agentModel, agentApp);
			if (pubAuditJunction != null) {
				AgentDisjunction.add(pubAuditJunction);
			}
			allAgentApp.addAll(agentApp);
			disjunction.add(AgentDisjunction);
		}
		
		//除代理外,其他应用SQL
		Junction otherJunction = this.getOtherJunction(allAgentApp);
		if (otherJunction != null) {
			disjunction.add(otherJunction);
		}
		
		return disjunction;
	}
	
	private List<Integer> getAppByAgentModel(AgentModel agentModel) {
		List<Integer> agentApp = new ArrayList<Integer>();
		if (agentModel.isHasEdoc()){
			agentApp.add(ApplicationCategoryEnum.edoc.key());
			agentApp.add(ApplicationCategoryEnum.edocSend.key());
			agentApp.add(ApplicationCategoryEnum.edocRec.key());
			agentApp.add(ApplicationCategoryEnum.edocSign.key());
			agentApp.add(ApplicationCategoryEnum.edocRegister.key());
			agentApp.add(ApplicationCategoryEnum.exSend.key());
			agentApp.add(ApplicationCategoryEnum.exSign.key());
			agentApp.add(ApplicationCategoryEnum.exchange.key());
			agentApp.add(ApplicationCategoryEnum.edocRecDistribute.key());
		}
		//标准产品中没有这两类,暂时注释掉
		/*if (agentModel.isHasInfo()){//政务首页信息报送查询
			agentApp.add(ApplicationCategoryEnum.info.key());
			agentApp.add(ApplicationCategoryEnum.infoStat.key());
		}*/
		if (agentModel.isHasOffice()) {
			agentApp.add(ApplicationCategoryEnum.office.key());
		}
		if (agentModel.isHasMeeting()) {
			agentApp.add(ApplicationCategoryEnum.meetingroom.key());
		}
		
		return agentApp;
	}
	
    private List<Integer> getAllApp() {
        
        List<Integer> allApp = new ArrayList<Integer>();
        if (this.apps.contains(ApplicationCategoryEnum.edoc)) {
            allApp.add(ApplicationCategoryEnum.edoc.key());
            allApp.add(ApplicationCategoryEnum.edocSend.key());
            allApp.add(ApplicationCategoryEnum.edocRec.key());
            allApp.add(ApplicationCategoryEnum.edocSign.key());
            allApp.add(ApplicationCategoryEnum.edocRegister.key());
            allApp.add(ApplicationCategoryEnum.exSend.key());
            allApp.add(ApplicationCategoryEnum.exSign.key());
            allApp.add(ApplicationCategoryEnum.exchange.key());
            allApp.add(ApplicationCategoryEnum.edocRecDistribute.key());
        }
        if (this.apps.contains(ApplicationCategoryEnum.meeting)) {
            allApp.add(ApplicationCategoryEnum.meeting.key());
            allApp.add(ApplicationCategoryEnum.meetingroom.key());
        }
        if (this.apps.contains(ApplicationCategoryEnum.info)) {
            allApp.add(ApplicationCategoryEnum.info.key());
            allApp.add(ApplicationCategoryEnum.infoStat.key());
        }

        for (ApplicationCategoryEnum app : this.apps) {
            allApp.add(app.getKey());
        }

        return allApp;
    }
	
	private Junction getPubAuditJunction(AgentModel agentModel, List<Integer> agentApp) {
		Junction junction = Restrictions.disjunction();
		if(agentModel.isHasPubAudit()){
			
			Junction pubAuditJunction = Restrictions.conjunction();
			List<Integer> pubAuditApp =  Arrays.asList(
					ApplicationCategoryEnum.bulletin.key(),
					ApplicationCategoryEnum.inquiry.key(),
					ApplicationCategoryEnum.news.key()
					);
			pubAuditJunction.add(Restrictions.in("app", pubAuditApp)).add(Restrictions.eq("subApp", ApplicationSubCategoryEnum.inquiry_write.key()));
			junction.add(pubAuditJunction);
			agentApp.addAll(pubAuditApp);
		} else {
			return null;
		}
		
		return junction;
	}
	
	private Junction getOtherJunction( List<Integer> agentApp) {
		Junction junction = Restrictions.disjunction();
    	
		List<Integer> allApp = this.getAllApp();
		List<Integer> otherApps = new ArrayList<Integer>();
		for (Integer app : allApp) {
			if (!agentApp.contains(app)) {
				otherApps.add(app);
			}
		}
		//代理之外应用的待办
		if (otherApps.size() > 1) {
			junction.add(Restrictions.in("app", otherApps));
		} else  if (otherApps.size() == 1) {
			junction.add(Restrictions.eq("app", otherApps.get(0)));
		} else {
			return null;
		}
		
		return junction;
	}
	
	private Junction getAgentJunction(AgentModel agentModel, List<Integer> agentApp) {
		Junction junction = Restrictions.disjunction();
		Junction otherJunction = Restrictions.conjunction();
		
		//代理应用的待办
		if (agentApp.size() > 1) {
			otherJunction.add(Restrictions.in("app", agentApp));
		} else  if (agentApp.size() == 1) {
			otherJunction.add(Restrictions.eq("app", agentApp.get(0)));
		}
		
		if(this.agentToFlag){//被代理了
			
			otherJunction.add(Restrictions.lt("receiveTime", agentModel.getStartDate()));
			junction.add(otherJunction);
			
			if(agentModel.isHasMeeting()){
				Junction meetingJunction = Restrictions.conjunction();
				meetingJunction.add(Restrictions.eq("app", ApplicationCategoryEnum.meeting.key()));
				
				Junction junction1 = Restrictions.disjunction();
				junction1.add(Restrictions.lt("completeTime", agentModel.getStartDate()));
				junction1.add(Restrictions.gt("receiveTime", agentModel.getEndDate()));
				meetingJunction.add(junction1);
				
				junction.add(meetingJunction);				
			}
			
		} else if(this.containAgent){//我是代理人--代理多个人
	 		Junction agent = Restrictions.conjunction()
	 		.add(Restrictions.eq("memberId", agentModel.getAgentToId()))		 			
	 		.add(Restrictions.gt("receiveTime", agentModel.getStartDate()));
	 		junction.add(agent);
		}
		
		return junction;
	}
	private Junction getAgentJunction4Col(AgentModel agentModel) {
		Junction junction = Restrictions.conjunction();
		junction.add(Restrictions.eq("app", ApplicationCategoryEnum.collaboration.key()));
		if(this.agentToFlag != null){
			if(this.agentToFlag){//被代理了
				List<AgentDetailModel> details = agentModel.getAgentDetail();
				Junction agentJun = Restrictions.disjunction();
		        boolean c = agentModel.isHasCol();
		        boolean t = agentModel.isHasTemplate();
		                    
		        if(c && !t){ //仅自由协同
		        	Junction con = Restrictions.conjunction();
		        	con.add(Restrictions.isNotNull("templeteId"));
		            con.add(Restrictions.gt("receiveTime", agentModel.getStartDate()));
		            agentJun.add(con);
		        } else if(t && !c){     //仅模板
		            if(Strings.isEmpty(details)){
		                //模板(全部)
		                Junction con = Restrictions.conjunction();
	                    con.add(Restrictions.isNull("templeteId"));
	                    con.add(Restrictions.gt("receiveTime", agentModel.getStartDate()));
	                    agentJun.add(con);
		            } else{
		            	//指定模板
			            List<Long> templateIds = new ArrayList<Long>();
			            for (AgentDetailModel agentDetailModel : details) {
			            	templateIds.add(agentDetailModel.getEntityId());
			            }
			            Junction con = Restrictions.conjunction();
			            con.add(Restrictions.gt("receiveTime", agentModel.getStartDate()));
			            con.add(Restrictions.or(Restrictions.not(Restrictions.in("templeteId", templateIds)), Restrictions.isNull("templeteId")));
		                agentJun.add(con);
		            }
		        } else if(t && c){  //自由协同+部分模板协同
		            List<Long> templateIds = new ArrayList<Long>();
		            if(Strings.isNotEmpty(details)){
		                for (AgentDetailModel agentDetailModel : details) {
		                	templateIds.add(agentDetailModel.getEntityId());
		                }
		            }
		            Junction con = Restrictions.conjunction();
                    con.add(Restrictions.gt("receiveTime", agentModel.getStartDate()));
                    con.add(Restrictions.and(Restrictions.not(Restrictions.in("templeteId", templateIds)), Restrictions.isNotNull("templeteId")));
                    agentJun.add(con);
		        }
		        agentJun.add(Restrictions.lt("receiveTime", agentModel.getStartDate()));
		        junction.add(agentJun);
			}
    	}
		
		return junction;
	}
}
