package com.seeyon.apps.supervise.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.supervise.bo.SuperviseQueryCondition;
import com.seeyon.ctp.common.supervise.bo.SuperviseWebModel;
import com.seeyon.ctp.common.supervise.enums.SuperviseEnum;
import com.seeyon.ctp.common.supervise.handler.SuperviseAppInfoBO;
import com.seeyon.ctp.common.supervise.handler.SuperviseHandler;
import com.seeyon.ctp.common.supervise.vo.SuperviseMessageParam;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.po.OrgMember;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.IdentifierUtil;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
public class ColSuperviseHandler implements SuperviseHandler{
	private static final Log LOG = CtpLogFactory.getLog(ColSuperviseHandler.class);
    private ColManager colManager;
    private OrgManager orgManager;
    public ColManager getColManager() {
        return colManager;
    }
    public void setColManager(ColManager colManager) {
        this.colManager = colManager;
    }
    public OrgManager getOrgManager() {
        return orgManager;
    }
    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }
    @Override
    public ModuleType getModuleType() {
        return ModuleType.collaboration;
    }
    
    @Override
    public List<SuperviseWebModel> getSuperviseDataList(FlipInfo fi,EnumMap<SuperviseQueryCondition, List<String>> queryCondition) throws BusinessException {
    	return getSuperviseDataList(fi, queryCondition, false);
    }
    
    
    /**
     * 优化summmary 查询组合
     * @param keySet
     * @param result
     * @return
     */
    private Map<Long,ColSummary> getSuperviseDataSummary(Set<SuperviseQueryCondition> keySet, List result,
			EnumMap<SuperviseQueryCondition, List<String>> queryCondition) {
		// 现在开始查询col_summary表 查询出数据后整合
		Map<String, Object> params = new HashMap<String, Object>();
		StringBuilder hql = new StringBuilder(200);
		hql.append("select summ.id,");
		hql.append("summ.deadline,");// 掉线
		hql.append("summ.deadlineDatetime,");// 掉线时间
		hql.append("summ.finishDate,");
		hql.append("summ.newflowType,");
		hql.append("summ.currentNodesInfo,");
		hql.append("summ.processId,");
		hql.append("summ.caseId,");
		hql.append("summ.orgAccountId");
		hql.append(" from ");
		hql.append(ColSummary.class.getName()).append(" as summ ");
		int size = result.size();
		Object[] res = null;
		List<Long> in_ids = new ArrayList(size);
		for (int i = 0; i < size; i++) {
			res = (Object[]) result.get(i);
			Long entityId=(Long) res[7];
			in_ids.add(entityId);
		}
		hql.append(" where summ.id in (:whereID)");
		params.put("whereID", in_ids);
		Map<Long,ColSummary>  resultMap=new HashMap<Long, ColSummary>();

		List SummaryResult = DBAgent.find(hql.toString(), params);
		if(SummaryResult !=null && SummaryResult.size() > 0){
			Iterator<Object[]> iter1 = SummaryResult.iterator();
			while (iter1.hasNext()) {
				Object[] now = (Object[]) iter1.next();
				ColSummary col = new ColSummary();
				col.setId((Long)now[0]);
				col.setDeadline((Long)now[1]);
				col.setDeadlineDatetime((Date)now[2]);
				col.setFinishDate((Date)now[3]);
				col.setNewflowType((Integer)now[4]);
				col.setCurrentNodesInfo((String)now[5]);
				col.setProcessId((String)now[6]);
				col.setCaseId((Long)now[7]);
				col.setOrgAccountId((Long)now[8]);
				resultMap.put(col.getId(), col);
			}
		}
		return resultMap;
		
	}

    @Override
    public List<SuperviseWebModel> getSuperviseDataList(FlipInfo fi,EnumMap<SuperviseQueryCondition, List<String>> queryCondition, boolean onlyCount) throws BusinessException {
        /**表单分类开始**/
      List<String> listCategory = queryCondition.get("templeteCategorys");
      if(!Strings.isEmpty(listCategory)){
        String hqlCategory =" select t.id from CtpTemplate t,CtpTemplateCategory ca where t.categoryId = ca.id and t.bodyType=20 and ca.id in (:categoryIds) ";
        Map<String, Object> categoryMap  = new HashMap<String, Object>();
        categoryMap.put("categoryIds", listCategory);
        List _categoryList = DBAgent.find(hqlCategory.toString(), categoryMap);
        if(_categoryList != null && _categoryList.size()>0){
        	List listId =new ArrayList<String>();
            for(int i = 0; i < _categoryList.size(); i++) {
              listId.add(String.valueOf((Long)_categoryList.get(i)));
            }
            if(!Strings.isEmpty(listId)){
                queryCondition.put(SuperviseQueryCondition.templeteIds,listId);
              }
        }
        
       
      }
      Set<SuperviseQueryCondition>  keySet = queryCondition.keySet();
		boolean isMemberNameQuery = false;
		if(Strings.isNotEmpty(keySet)){
			for(SuperviseQueryCondition sqc : keySet){
				if(sqc == SuperviseQueryCondition.colSuperviseSender){
					isMemberNameQuery = true;
					break;
				}
			}
		}
		StringBuilder hql = new StringBuilder(300);
		hql.append("select de.subject,");
		hql.append("de.senderId as startMemberId,");
		hql.append("de.createDate as  startDate,");
		hql.append("de.importantLevel,");
		hql.append("de.id,");
		hql.append("de.awakeDate,");
		hql.append("de.count,");
		hql.append("de.entityId,");
		hql.append("de.description,");
		hql.append("de.status,");
		hql.append("de.entityType,");
		hql.append("de.resentTime,");
		hql.append("de.forwardMember,");
		hql.append("de.bodyType,");
		hql.append("de.identifier,");
		hql.append("de.templeteId,");
        hql.append("de.processDeadlineDate,");
		hql.append("de.coverTime,");
		hql.append("de.affairId ");
		hql.append(" from " );
		hql.append(CtpSuperviseDetail.class.getName() );
		hql.append( " as de," );
		hql.append(CtpSupervisor.class.getName() );
		hql.append( " as su " );
        if(isMemberNameQuery){
        	hql.append(",").append(OrgMember.class.getName()).append(" as mem ");
        }
        hql.append(" where su.superviseId=de.id  ");
        
        if(isMemberNameQuery){
        	hql.append(" and de.senderId = mem.id ");
        }
        
        hql.append(" and su.supervisorId=:userId  ");
        long userId = AppContext.currentUserId();
        Map<String,Object> params = new HashMap<String,Object>(8);
        params.put("entityType", SuperviseEnum.EntityType.summary.ordinal());
        params.put("userId", userId);
        Iterator<SuperviseQueryCondition> it = keySet.iterator();
        //根据索引来优化顺序
        if(keySet.contains(SuperviseQueryCondition.status)){
        	List<String> value = queryCondition.get(SuperviseQueryCondition.status);
        	if(value != null &&  value.size() > 0){
        		Integer status = Integer.parseInt(value.get(0));
                hql.append(" and de.status= :status ");
                params.put("status",status);
        	}
        	 
        }
        hql.append(" and de.entityType=:entityType  ");
        
        while(it.hasNext()){
            SuperviseQueryCondition condition = it.next();
            List<String> value = queryCondition.get(condition);
            switch(condition){
            	case  colSuperviseTitle: 
	        		hql.append(" and de.subject like :title");
	                params.put("title", "%"+value.get(0)+"%");
            		break;
            	case  colSuperviseSender: 
            		hql.append(" and mem.name like :startMemberName");
                    params.put("startMemberName", "%" + SQLWildcardUtil.escape(value.get(0)) + "%");
            		break;
            	case  colImportantLevel: 
            		hql.append(" and de.importantLevel = :importantLevel");
                    params.put("importantLevel", Integer.parseInt(value.get(0)));
            		break;
            	case  colSuperviseSendTime: 
            		 String startDate = value.get(0);
                     if(Strings.isNotBlank(startDate)){
                         hql.append(" and de.createDate >= :startDate");
                         java.util.Date stamp = Datetimes.getTodayFirstTime(startDate);
                         params.put("startDate",stamp);
                     }
                     if(value.size()==2){
                         String endDate = value.get(1);
                         if(Strings.isNotBlank(endDate)){
                             hql.append(" and de.createDate <= :endDate");
                             java.util.Date stamp = Datetimes.getTodayLastTime(endDate);
                             params.put("endDate",stamp);
                         }
                     }
            		break;

                case deadlineDatetime:
                    String  startDate1 = value.get(0);
                    if(Strings.isNotBlank(startDate1)){
                        hql.append(" and de.processDeadlineDate >= :startDate");
                        java.util.Date stamp = Datetimes.getTodayFirstTime(startDate1);
                        params.put("startDate",stamp);
                    }
                    if(value.size()==2){
                        String endDate = value.get(1);
                        if(Strings.isNotBlank(endDate)){
                            hql.append(" and de.processDeadlineDate <= :endDate");
                            java.util.Date stamp = Datetimes.getTodayLastTime(endDate);
                            params.put("endDate",stamp);
                        }
                    }
                    break;
            	case  templeteIds:
            		if(Strings.isNotEmpty(value)){
            			hql.append(" and de.templeteId in(:templeteIds) ");
                        List<Long> l = new ArrayList<Long>();
                        
                        for(String s : value){
                            String[] value2 = s.split(",");
                            for (String s2 : value2) {
                                l.add(Long.valueOf(s2));
                            }
                        }
                        params.put("templeteIds",l);
            		}
            		break;
            	case  templeteAll: 
            		if("templeteAll".equals(value.get(0))){
            			 hql.append(" and de.bodyType=20 ");
            		}
            		break;
            	case  moduleId: 
            		if(value.get(0) != null){
            			List<String> moduleId = queryCondition.get(SuperviseQueryCondition.moduleId);
                    	String _moduleId = moduleId.get(0);
                    	hql.append(" and de.entityId =:_moduleId ");
                    	params.put("_moduleId",Long.parseLong(_moduleId));
            		}
            		break;
            	default: 
            		break;
            		
            }
        }
        hql.append(" order by de.createDate desc");
        List result = null; 
        if (!onlyCount) {
        	if(fi != null){
        		result = DBAgent.find(hql.toString(), params, fi);
        	}else{
        		result = DBAgent.find(hql.toString(), params);
        	}
        } else {
        	if (fi != null) {
        		fi.setTotal(DBAgent.count(hql.toString(), params));
        	}
        }
        List<SuperviseWebModel> modelList = new ArrayList<SuperviseWebModel>();
        if(result != null && !result.isEmpty()){
        	Map<Long,ColSummary>  summaryMapresult = getSuperviseDataSummary(keySet,result,queryCondition);
        	int resultSize = result.size();
        	String tDeadlineName = ResourceUtil.getString("collaboration.project.nothing.label");
            for(int i = 0;i < resultSize;i++){
            	
            	
                Object[] res = (Object[]) result.get(i);
                SuperviseWebModel model = new SuperviseWebModel();
                int j = 0;
                ColSummary col = summaryMapresult.get(((Long)res[7]).longValue());
                if(col == null){
                	col =new ColSummary();
                }
                //标题
                model.setTitle((String)res[j++]);
                //发送人id
                Long sdId = (Long)res[j++];
                model.setSender(sdId);
                //发起日期
                Date startDate = (Date)res[j++];
                model.setSendDate(startDate);
                //重要程度
                model.setImportantLevel((Integer)res[j++]);
                //流程期限
                model.setDeadline(col.getDeadline());
                Date deadlineDatetime = col.getDeadlineDatetime();
                if(deadlineDatetime!=null){//新数据显示时间点
                	model.setDeadlineDatetime(deadlineDatetime);
                	model.setDeadlineName(ColUtil.getDeadLineName(deadlineDatetime));
                }else if(model.getDeadline()!=null){//兼容老数据，按时间段显示
                	model.setDeadlineName(ColUtil.getDeadLineName(model.getDeadline()));
                }else{
                	model.setDeadlineName(tDeadlineName);
                }
                //结束日期
                Date finishDate = col.getFinishDate();
                //流程是否超期
                ColSummary summary = new ColSummary();
                summary.setSubject(model.getTitle());
                summary.setDeadlineDatetime(deadlineDatetime);
                summary.setFinishDate(finishDate);
                summary.setStartDate(startDate);
                //应用类型为协同
                model.setAppType(ApplicationCategoryEnum.collaboration.ordinal());
                //流程类型
                model.setNewflowType(col.getNewflowType());
                //当前处理人信息
                String cninfo= col.getCurrentNodesInfo();
                summary.setCurrentNodesInfo(cninfo);
                summary.setState(col.getState());
                model.setCurrentNodesInfo(ColUtil.parseCurrentNodesInfo(summary));
                Date now = new Date(System.currentTimeMillis());
                //督办事项ID
                model.setId((Long)res[j++]);
                //督办日期
                Date awakeDate = (Date)res[j++];
                if(awakeDate != null && now.after(awakeDate)){
                    model.setIsRed(true);
                }
                model.setAwakeDate(awakeDate);
                //催办次数
                Object _count = res[j++];
                if(_count == null ){
                	_count = 0;
                }

                model.setCount(Integer.parseInt(String.valueOf(_count)));
                //协同id(督办应用ID)
                model.setSummaryId((Long)res[j++]);
                //督办描述
                model.setContent(Strings.toHTML((String)res[j++]));
                //督办事项状态
                model.setStatus((Integer)res[j++]);
                //督办 应用类型
                model.setEntityType((Integer)res[j++]);
                //重复发起次数
                model.setResendTime((Integer)res[j++]);
                summary.setResentTime(model.getResendTime());
                //转发人
                model.setForwardMember((String)res[j++]);
                summary.setForwardMember(model.getForwardMember());
                //正文类型
                model.setBodyType((String)res[j++]);
                //标识字符串(判断是否有附件)
                Object identifier = res[j++];
                if(identifier != null){
                    Boolean hasAtt = IdentifierUtil.lookupInner(identifier.toString(),0, '1');
                    model.setHasAttachment(hasAtt);
                }else{
                    model.setHasAttachment(false);
                }
                //发送人名称
             //   j++;
                model.setSenderName(Functions.showMemberName(sdId));
                //流程ID
                model.setProcessId(col.getProcessId());
                //caseId
                model.setCaseId(col.getCaseId());
                //模版Id
                Object tempId = res[j++];
                
                if(tempId != null){
                    model.setIsTemplate(true);
                }else{
                    model.setIsTemplate(false);
                }
                //单位id
                model.setFlowPermAccountId(col.getOrgAccountId());
                Map<String, String> defaultNodeMap = colManager.getColDefaultNode(model.getFlowPermAccountId());
                model.setDefaultNodeName(defaultNodeMap.get("defaultNodeName"));
                model.setDefaultNodeLable(defaultNodeMap.get("defaultNodeLable"));
                Date deadLineTime = (Date)res[j++];//暂时只是查询不做其它改变
                //流程超期
                Object coverTime = res[j++];
                boolean isCoverTime = false;
                if(coverTime != null){
                	isCoverTime = (Boolean)coverTime;
                }
                model.setWorkflowTimeout(isCoverTime);
                model.setDetailPageUrl(getDetailPageUrl());
                //重新加工标题
                String subject = ColUtil.showSubjectOfSummary(summary, false, -1, null);
                model.setTitle(subject);
                model.setAppName("collaboration");
                Object affairId = res[j++];
                if(affairId != null) {
                	model.setAffairId((Long)affairId);
                }
                modelList.add(model);
            }
        }
        if(fi != null){
            fi.setData(modelList);
        }
        return modelList;
    }

    @Override
    public String getDetailPageUrl() {
        return "collaboration/collaboration.do?method=summary";
    }
    @Override
    public SuperviseMessageParam getSuperviseMessageParam4SaveImmediate(Long moduleId) throws BusinessException{
    	ColSummary summary = colManager.getColSummaryById(moduleId);
    	SuperviseMessageParam smp = new SuperviseMessageParam(true,summary.getImportantLevel(),summary.getSubject(),summary.getForwardMember(),summary.getStartMemberId());
    	return smp;
    }
	@Override
	public long getFlowPermAccountId(long summaryId) throws BusinessException {
		ColSummary s = colManager.getColSummaryById(summaryId);
		if(s!=null){
			return ColUtil.getFlowPermAccountId(s.getOrgAccountId(), s);
		}
		return 0;
	}
	@Override
    public Map<Long,SuperviseAppInfoBO> getAppInfo(List<Long> summaryIds) throws BusinessException {
		
		Map<Long,SuperviseAppInfoBO> superviseAppInfoBOs = new HashMap<Long,SuperviseAppInfoBO>();
		
		List<ColSummary> colSummarys = colManager.findColSummarysByIds(summaryIds);
    	for(ColSummary summary : colSummarys){
    		SuperviseAppInfoBO superviseAppInfoBO = new SuperviseAppInfoBO();
    		superviseAppInfoBO.setCurrentNodesInfo(ColUtil.parseCurrentNodesInfo(summary));
    		superviseAppInfoBO.setCaseId(summary.getCaseId());
    		superviseAppInfoBO.setProcessId(summary.getProcessId());
    		superviseAppInfoBO.setTemplateId(summary.getTempleteId());
    		superviseAppInfoBO.setDeadlineDatetime(summary.getDeadlineDatetime());
    		superviseAppInfoBO.setAutoRun(summary.getAutoRun());
    		// 当流程期限不为空，且当前时间大于设置的流程期限时，设为超期
    		Boolean isCoverTime = summary.isCoverTime();
            boolean isDeadlineDatetime = summary.getDeadlineDatetime()!=null && new Date().after(summary.getDeadlineDatetime());
            if((isCoverTime != null && isCoverTime) || isDeadlineDatetime){
            	superviseAppInfoBO.setWorkflowTimeout(true);
            }
            
            superviseAppInfoBOs.put(summary.getId(),superviseAppInfoBO);
    	}
    	return superviseAppInfoBOs;
		
	}
	
    @Override
    public FlipInfo getAllSuperviseList4App(FlipInfo fi, Map<String, Object> queryMap) throws BusinessException {
        /**表单分类开始**/
        List<String> listCategory = (List) queryMap.get("templeteCategorys");
        if (!Strings.isEmpty(listCategory)) {
            String hqlCategory = " select t.id from CtpTemplate t,CtpTemplateCategory ca where t.categoryId = ca.id and t.bodyType=20 and ca.id in (:categoryIds) ";
            Map<String, Object> categoryMap = new HashMap<String, Object>();
            categoryMap.put("categoryIds", listCategory);
            List _categoryList = DBAgent.find(hqlCategory.toString(), categoryMap);
            List listId = new ArrayList<String>();
            for (int i = 0; i < _categoryList.size(); i++) {
                Long objectCate = (Long) _categoryList.get(i);
                listId.add(objectCate + "");
            }
            if (!Strings.isEmpty(listId)) {
                queryMap.put("templeteIds", listId);
            }
        }

        StringBuilder hql = new StringBuilder("select de.subject,")
                .append("de.entitySenderId,")
                .append("de.entityCreateDate,")
                .append("de.importantLevel,")
                .append("de.processDeadlineDate,")
                .append("de.id,")
                .append("de.awakeDate,")
                .append("de.count,")
                .append("de.entityId,")
                .append("de.description,")
                .append("de.status,")
                .append("de.entityType,")
                .append("de.forwardMember,")
                .append("de.bodyType,")
                .append("de.identifier,")
                .append("de.templeteId,")
                .append("de.coverTime ")
                .append(" from ")
                .append(CtpSuperviseDetail.class.getName()).append(" as de,")
                .append(CtpSupervisor.class.getName()).append(" as su ");
        //发起人
        String startMemberName = (String) queryMap.get("startMemberName");
        if(Strings.isNotBlank(startMemberName)){
            hql.append(",").append(OrgMember.class.getName()).append(" as mem");
        }
        
        hql.append(" where su.superviseId=de.id ");
        Map<String, Object> params = new HashMap<String, Object>();
        if(Strings.isNotBlank(startMemberName)){
            hql.append(" and de.entitySenderId=mem.id and mem.name like :startMemberName ");
            params.put("startMemberName", "%" + SQLWildcardUtil.escape(startMemberName) + "%");
        }
        hql.append(" and su.supervisorId=:userId and de.entityType=:entityType ");
        long userId = AppContext.currentUserId();
        params.put("entityType", SuperviseEnum.EntityType.summary.ordinal());
        params.put("userId", userId);
        String title = (String) queryMap.get("subject");
        if (Strings.isNotBlank(title)) {
            hql.append(" and de.subject like :title ").append(SQLWildcardUtil.setEscapeCharacter());
            params.put("title", "%" + SQLWildcardUtil.escape(title) + "%");
        }
        //重要程度
        String importantLevel = (String) queryMap.get("importantLevel");
        if (Strings.isNotBlank(importantLevel)) {
            hql.append(" and de.importantLevel like :importantLevel");
            params.put("importantLevel", Integer.valueOf(importantLevel));
        }
        //办理状态过滤
        String status = (String) queryMap.get("status");
        if (Strings.isNotBlank(status)) {
            if ("0".equals(status)) {
                hql.append(" and de.status = 0 ");

            } else if ("1".equals(status)) {
                hql.append(" and de.status = 1 ");
            }
        } else {
            hql.append(" and de.status = 0 ");
        }
        //自建流程或者模板过滤
        String operationType = (String) queryMap.get("operationType");
        String operationTypeIdsStr = (String) queryMap.get("operationTypeIds");
        String[] operationTypeIds = null;
        if (operationTypeIdsStr != null && !"".equals(operationTypeIdsStr.trim())) {
            operationTypeIds = operationTypeIdsStr.split(",");
        }
        if (operationType != null) {
            if ("".equals(operationType.trim()))
                operationType = null;
        }
        if ("template".equals(operationType)) {
            if (operationTypeIds != null && !"".equals(operationTypeIds[0])) {
                List<Long> processOperationTypeList = new ArrayList<Long>();
                if (operationTypeIds != null && operationTypeIds.length > 0) {
                    for (int i = 0; i < operationTypeIds.length; i++) {
                        if (!"".equals(operationTypeIds[i])) {
                            processOperationTypeList.add(Long.valueOf(operationTypeIds[i]));
                        }
                    }
                }
                hql.append(" and de.templeteId in (:templeteId) ");
                params.put("templeteId", processOperationTypeList);
            } else {
                hql.append(" and de.templeteId is not null ");
            }
        } else if ("self".equals(operationType)) {
            hql.append(" and de.templeteId is null ");
        }
        String senders = (String) queryMap.get("senders");
        if (Strings.isNotBlank(senders)) {
            Set<V3xOrgMember> members = orgManager.getMembersByTypeAndIds(senders);
            List<V3xOrgMember> memberList = new ArrayList<V3xOrgMember>(members);
            if (Strings.isNotEmpty(memberList)) {
                if (memberList.size() > 1000) {
                    memberList = memberList.subList(0, 1000);
                }
                List<Long> memberIdList = new ArrayList<Long>();
                for (V3xOrgMember mem : memberList) {
                    memberIdList.add(mem.getId());
                }
                hql.append(" and de.entitySenderId in (:senderId) ");
                params.put("senderId", memberIdList);
            }
        }

        //发起时间过滤
        String beginDate = (String) queryMap.get("beginDate");
        if (Strings.isNotBlank(beginDate)) {

            hql.append(" and de.entityCreateDate >= :startDate ");

            java.util.Date stamp = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(beginDate));
            params.put("startDate", stamp);
        }
        String endDate = (String) queryMap.get("endDate");
        if (Strings.isNotBlank(endDate)) {
            hql.append(" and de.entityCreateDate <= :endDate ");
            java.util.Date stamp = Datetimes.parseDatetime(Datetimes.getLastTimeStr(endDate));
            params.put("endDate", stamp);
        }
        String deadlineBeginDate = (String) queryMap.get("deadlineBeginDate");
        if (Strings.isNotBlank(deadlineBeginDate)) {
            hql.append(" and de.processDeadlineDate >= :deadlineBeginDate");
            java.util.Date stamp = Datetimes.parseDatetime(Datetimes.getFirstTimeStr(deadlineBeginDate));
            params.put("deadlineBeginDate", stamp);
        }
        String deadlineEndDate = (String) queryMap.get("deadlineEndDate");
        if (Strings.isNotBlank(deadlineEndDate)) {
            hql.append(" and de.processDeadlineDate <= :deadlineEndDate");
            java.util.Date stamp = Datetimes.parseDatetime(Datetimes.getLastTimeStr(deadlineEndDate));
            params.put("deadlineEndDate", stamp);
        }

        hql.append(" order by de.entityCreateDate desc");
        List result = null;
        if (fi != null) {
            result = DBAgent.find(hql.toString(), params, fi);
            List<SuperviseWebModel> modelList = convert2SuperviseWebModel(result);
            if (fi != null) {
                fi.setData(modelList);
            }
            return fi;
        } else {
            result = DBAgent.find(hql.toString(), params);
            fi = new FlipInfo();
            if (Strings.isNotEmpty(result)) {
                fi.setTotal(result.size());
            } else {
                fi.setTotal(0);

            }
            return fi;
        }
    }
	
	private List<SuperviseWebModel> convert2SuperviseWebModel(List result) throws BusinessException{
	    List<SuperviseWebModel> modelList = new ArrayList<SuperviseWebModel>();
        if(result != null && !result.isEmpty()){
            List<Long> summaryIds = new ArrayList<Long>();
            for(int i = 0;i < result.size();i++){
                Object[] res = (Object[]) result.get(i);
                SuperviseWebModel model = new SuperviseWebModel();
                int j = 0;
                //标题
                model.setTitle((String)res[j++]);
                //发送人id
                Long sdId = (Long)res[j++];
                if(sdId != null) {
                    model.setSender(sdId);
                }
                //发送人名称
                model.setSenderName(Functions.showMemberName(sdId));
                //发起日期
                Date startDate = (Date)res[j++];
                model.setSendDate(startDate);
                //重要程度
                model.setImportantLevel((Integer)res[j++]);
                Date deadlineDatetime = (Date)res[j++];
                if(deadlineDatetime!=null){//新数据显示时间点
                    model.setDeadlineDatetime(deadlineDatetime);
                    model.setDeadlineName(ColUtil.getDeadLineName(deadlineDatetime));
                }else if(model.getDeadline()!=null){//兼容老数据，按时间段显示
                    model.setDeadlineName(ColUtil.getDeadLineName(model.getDeadline()));
                }else{
                  model.setDeadlineName(ResourceUtil.getString("collaboration.project.nothing.label"));
                }
                //流程是否超期
                ColSummary summary = new ColSummary();
                summary.setSubject(model.getTitle());
                summary.setDeadlineDatetime(deadlineDatetime);
                summary.setStartDate(startDate);
                //应用类型为协同
                model.setAppType(ApplicationCategoryEnum.collaboration.ordinal());
                Date now = new Date(System.currentTimeMillis());
                //督办事项ID
                model.setId((Long)res[j++]);
                //督办日期
                Date awakeDate = (Date)res[j++];
                if(awakeDate != null && now.after(awakeDate)){
                    model.setIsRed(true);
                }
                model.setAwakeDate(awakeDate);
                //催办次数
                Object _count = res[j++];
                if(_count == null ){
                    _count = 0;
                }
                model.setCount(Integer.parseInt(String.valueOf(_count)));
                //协同id(督办应用ID)
                Long summaryId = (Long)res[j++];
                model.setSummaryId(summaryId);
                summaryIds.add(summaryId);
                //督办描述
                model.setContent(Strings.toHTML((String)res[j++]));
                //督办事项状态
                model.setStatus((Integer)res[j++]);
                //督办 应用类型
                model.setEntityType((Integer)res[j++]);
                //转发人
                model.setForwardMember((String)res[j++]);
                summary.setForwardMember(model.getForwardMember());
                //正文类型
                model.setBodyType((String)res[j++]);
                //标识字符串(判断是否有附件)
                Object identifier = res[j++];
                if(identifier != null){
                    Boolean hasAtt = IdentifierUtil.lookupInner(identifier.toString(),0, '1');
                    model.setHasAttachment(hasAtt);
                }else{
                    model.setHasAttachment(false);
                }
         
                //模版Id
                Object tempId = res[j++];
                if(tempId != null){
                    model.setIsTemplate(true);
                }else{
                    model.setIsTemplate(false);
                }
                Map<String, String> defaultNodeMap = colManager.getColDefaultNode(model.getFlowPermAccountId());
                model.setDefaultNodeName(defaultNodeMap.get("defaultNodeName"));
                model.setDefaultNodeLable(defaultNodeMap.get("defaultNodeLable"));
                
                //流程超期
                Object coverTime = res[j++];
                boolean isCoverTime = false;
                if(coverTime != null){
                    isCoverTime = (Boolean)coverTime;
                }
                model.setWorkflowTimeout(isCoverTime);
                model.setDetailPageUrl(getDetailPageUrl());
                model.setAppName("collaboration"); 
                modelList.add(model);
            }
            Map<String, ColSummary> colSummaryMap = new HashMap<String, ColSummary>();
            if (Strings.isNotEmpty(summaryIds)) {
                Map<String, Object> params = new HashMap<String, Object>();
                StringBuilder hql = new StringBuilder();
                hql.append(" select summary.id,summary.currentNodesInfo,summary.finishDate,summary.state,summary.subject,"
                        + "summary.deadline,summary.newflowType,summary.startDate,summary.resentTime,summary.forwardMember,"
                        + "summary.processId,summary.caseId,summary.orgAccountId,summary.formAppid,summary.formRecordid ");
                hql.append(" from ColSummary as summary where summary.id in (:summaryIds) ");
                params.put("summaryIds", summaryIds);
                List summaryInfoList = DBAgent.find(hql.toString(), params);
                if (Strings.isNotEmpty(summaryInfoList)) {
                    for (int i = 0; i < summaryInfoList.size(); i++) {
                        Object[] res = (Object[]) summaryInfoList.get(i);
                        ColSummary summaryInfo = new ColSummary();
                        int j = 0;
                        summaryInfo.setId((Long) res[j++]);
                        summaryInfo.setCurrentNodesInfo((String) res[j++]);
                        summaryInfo.setFinishDate((Date) res[j++]);
                        summaryInfo.setState((Integer)res[j++]);
                        summaryInfo.setSubject((String) res[j++]);
                        summaryInfo.setDeadline((Long)res[j++]);
                        summaryInfo.setNewflowType((Integer)res[j++]);
                        summaryInfo.setStartDate((Date) res[j++]);
                        summaryInfo.setResentTime((Integer)res[j++]);
                        summaryInfo.setForwardMember((String)res[j++]);
                        summaryInfo.setProcessId((String)res[j++]);
                        summaryInfo.setCaseId((Long)res[j++]);
                        summaryInfo.setOrgAccountId((Long)res[j++]);
                        summaryInfo.setFormAppid((Long)res[j++]);
                        summaryInfo.setFormRecordid((Long)res[j++]);
                        colSummaryMap.put(String.valueOf(summaryInfo.getId()), summaryInfo);
                    }
                }
            }
            for (SuperviseWebModel superWM : modelList) {
                ColSummary col = colSummaryMap.get(String.valueOf(superWM.getSummaryId()));
                //设置当前处理人信息
                if (col != null) {
                    superWM.setCurrentNodesInfo(ColUtil.parseCurrentNodesInfo(col));
                    superWM.setDeadline(col.getDeadline());
                    superWM.setNewflowType(col.getNewflowType());
                    superWM.setResendTime(col.getResentTime());
                    superWM.setCaseId(col.getCaseId());
                    superWM.setProcessId(col.getProcessId());
                    superWM.setFormAppId(col.getFormAppid());
                    superWM.setFormRecordId(col.getFormRecordid());
                    superWM.setFlowPermAccountId(col.getOrgAccountId());
                    superWM.setTitle(ColUtil.showSubjectOfSummary(col, false, -1, null));
                }
            }
        }
	    return modelList;
	}
	
	@Override
	public Map<String, Object> getWFParms4BugReport(Long summaryId) throws BusinessException{
		Map<String, Object> ret = new HashMap<String, Object>();

		ColSummary summary = null;
		try {
			summary = colManager.getColSummaryById(summaryId);
			Map<String, String> defaultNodeMap = colManager.getColDefaultNode(summary.getOrgAccountId());
			ret.put("defaultNodeName", defaultNodeMap.get("defaultNodeName"));
			ret.put("defaultNodeLable", defaultNodeMap.get("defaultNodeLable"));
		} catch (BusinessException e) {
			LOG.error("",e);
		}

		if (ColUtil.isForm(summary.getBodyType())) {
			ret.put("nps", "form");
		} else {
			ret.put("nps", "default");
		}

		ret.put("isTemplate", summary.getTempleteId() != null ? true : false);
		ret.put("appEnumStr", ApplicationCategoryEnum.collaboration.name());
		ret.put("flowPermAccountId", summary.getPermissionAccountId());

		ret.put("bodyType", summary.getBodyType());
		ret.put("caseId", summary.getCaseId());
		ret.put("processId", summary.getProcessId());
		return ret;
	}

}
