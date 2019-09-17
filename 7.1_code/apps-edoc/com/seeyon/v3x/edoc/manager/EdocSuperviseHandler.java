package com.seeyon.v3x.edoc.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.seeyon.apps.edoc.bo.SimpleEdocSummary;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.supervise.bo.SuperviseQueryCondition;
import com.seeyon.ctp.common.supervise.bo.SuperviseWebModel;
import com.seeyon.ctp.common.supervise.enums.SuperviseEnum;
import com.seeyon.ctp.common.supervise.handler.SuperviseAppInfoBO;
import com.seeyon.ctp.common.supervise.vo.SuperviseMessageParam;
import com.seeyon.ctp.common.supervise.vo.SuperviseModelVO;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.organization.po.OrgMember;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.IdentifierUtil;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public class EdocSuperviseHandler {
	private EnumManager enumManagerNew = null;
	private EdocManager edocManager;
	public void setEnumManagerNew(EnumManager enumManager) {
        this.enumManagerNew = enumManager;
    }

	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}

	public ModuleType getModuleType() {
		return ModuleType.edoc;
	}
 
    public List<SuperviseWebModel> getSuperviseDataList(FlipInfo fi,EnumMap<SuperviseQueryCondition, List<String>> queryCondition) throws BusinessException {
    	return getSuperviseDataList(fi, queryCondition, false);
    }
	
	public List<SuperviseWebModel> getSuperviseDataList(FlipInfo fi,
		EnumMap<SuperviseQueryCondition, 
		List<String>> queryCondition, boolean onlyCount) throws BusinessException {
		
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
		
		
		StringBuffer hql = new StringBuffer("select summ.subject,")
        .append("summ.startUserId,")	
        .append("summ.createTime,")
        .append("summ.urgentLevel,")
        .append("summ.deadline,")
        .append("summ.deadlineDatetime,")
        .append("summ.completeTime,")
        .append("summ.currentNodesInfo,")
        .append("de.id,")
        .append("de.awakeDate,")
        .append("de.count,")
        .append("de.entityId,")
        .append("de.description,")
        .append("de.status,")
        .append("de.entityType,")
        .append("summ.identifier,")
        .append("summ.processId,")
        .append("summ.caseId,")
        .append("summ.docType,")
        .append("summ.edocType,")
        .append("summ.orgAccountId,")
		.append("summ.govdocType,")
        .append("summ.templeteId,")
        .append("de.supervisors,")
		.append("summ.govdocType");

        hql.append(" from " ).append(CtpSuperviseDetail.class.getName() ).append( " as de," )
        .append(CtpSupervisor.class.getName() ).append( " as su, " )
        .append( EdocSummary.class.getName() ).append( " as summ ");
      
        
        if(isMemberNameQuery){
            hql.append(",").append(OrgMember.class.getName()).append(" as mem ");
        }
        //当查询发起人姓名时，需要关联人员表
        
        
        hql.append(" where su.superviseId=de.id and de.entityId=summ.id ");
        hql.append(" and su.supervisorId=:userId ");
       
        if(isMemberNameQuery){
        	hql.append(" and summ.startUserId = mem.id ");
        }
        
        Map<String,Object> params = new HashMap<String,Object>();
        hql.append(" and (de.entityType=:entityType or  de.entityType=:newEntityType) ");
        params.put("entityType", SuperviseEnum.EntityType.edoc.ordinal());
        params.put("newEntityType", SuperviseEnum.EntityType.govdoc.ordinal());
        long userId = AppContext.currentUserId();
        params.put("userId", userId);
     
        Iterator<SuperviseQueryCondition> it = keySet.iterator();
        while(it.hasNext()){
            SuperviseQueryCondition condition = it.next();
            List<String> value = queryCondition.get(condition);
            if(condition == SuperviseQueryCondition.colSuperviseTitle){
                hql.append(" and summ.subject like :title");
                params.put("title", "%"+value.get(0)+"%");
            }else if(condition == SuperviseQueryCondition.colSuperviseSender){
                hql.append(" and mem.name like :startMemberName");
                params.put("startMemberName", "%" + SQLWildcardUtil.escape(value.get(0)) + "%");
            }else if(condition == SuperviseQueryCondition.colImportantLevel){
                hql.append(" and summ.urgentLevel = :urgentLevel");
                params.put("urgentLevel", value.get(0));
            }else if(condition == SuperviseQueryCondition.colSuperviseSendTime){
                String startDate = value.get(0);
                
                if(startDate != null && !"".equals(startDate)){
                    hql.append(" and summ.createTime >= :startDate");
                    java.util.Date stamp = Datetimes.getTodayFirstTime(startDate);
                    params.put("startDate",stamp);
                }
                if(value.size()==2){
                    String endDate = value.get(1);
                    if(endDate != null && !"".equals(endDate)){
                        hql.append(" and summ.createTime <= :endDate");
                        java.util.Date stamp = Datetimes.getTodayLastTime(endDate);
                        params.put("endDate",stamp);
                    }
                }
            }else if(condition == SuperviseQueryCondition.status && value.get(0) != null){
                Integer status = Integer.parseInt(value.get(0));
                hql.append(" and de.status= :status");
                params.put("status",status);
            }else if(condition == SuperviseQueryCondition.edocDocMark){
            	 hql.append(" and (summ.docMark like :docMark or summ.docMark2 like :docMark ) ");
                 params.put("docMark", "%"+value.get(0)+"%");
            }else if(condition == SuperviseQueryCondition.edocDocInMark){
            	hql.append(" and summ.serialNo like :docInMark");
            	params.put("docInMark", "%"+value.get(0)+"%");
            }else if(condition == SuperviseQueryCondition.edocSupervisors){
            	hql.append(" and de.supervisors like :supervisors");
            	params.put("supervisors", "%"+value.get(0)+"%");
            }else if(condition == SuperviseQueryCondition.edocAwakeDate){
            	 String startDate = value.get(0);
                 
                 if(startDate != null && !"".equals(startDate)){
                     hql.append(" and de.awakeDate >= :startDate");
                     java.util.Date stamp = Datetimes.getTodayFirstTime(startDate);
                     params.put("startDate",stamp);
                 }
                 if(value.size()==2){
                     String endDate = value.get(1);
                     if(endDate != null && !"".equals(endDate)){
                         hql.append(" and de.awakeDate <= :endDate");
                         java.util.Date stamp = Datetimes.getTodayLastTime(endDate);
                         params.put("endDate",stamp);
                     }
                 }
            }else if(condition == SuperviseQueryCondition.edocKeywords){
            	hql.append(" and summ.keywords like :keywords");
            	params.put("keywords", "%"+value.get(0)+"%");
            }else if(condition == SuperviseQueryCondition.edocSecretLevel){
                hql.append(" and summ.secretLevel like :secretLevel");
                params.put("secretLevel", "%"+value.get(0)+"%");
            }else if(condition == SuperviseQueryCondition.edocUrgentLevel){
                hql.append(" and summ.urgentLevel like :urgentLevel");
                params.put("urgentLevel", "%"+value.get(0)+"%");
            }else if(condition == SuperviseQueryCondition.deadlineDatetime){
                String startDate = value.get(0); 
                if(Strings.isNotBlank(startDate)){
                    hql.append(" and summ.deadlineDatetime >= :startDate");
                    java.util.Date stamp = Datetimes.getTodayFirstTime(startDate);
                    params.put("startDate",stamp);
                }
                if(value.size()==2){
                    String endDate = value.get(1);
                    if(Strings.isNotBlank(endDate)){
                        hql.append(" and summ.deadlineDatetime <= :endDate");
                        java.util.Date stamp = Datetimes.getTodayLastTime(endDate);
                        params.put("endDate",stamp);
                    }
                }
            
            
            }else if(condition == SuperviseQueryCondition.edocType){
        	    String app = value.get(0);
        	    if(Strings.isNotBlank(app)){
        	    	hql.append(" and  summ.edocType = :edocType ");
        	    	params.put("edocType",Integer.valueOf(app));
        	    }
            }else if(condition == SuperviseQueryCondition.templeteIds){
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
            }
        }
        hql.append(" order by summ.createTime desc ");
        List result = DBAgent.find(hql.toString(), params, fi);
        
        List<SuperviseWebModel> modelList = new ArrayList<SuperviseWebModel>();
        if(result != null && !result.isEmpty()){
            for(int i=0;i<result.size();i++){
                Object[] res = (Object[]) result.get(i);
                SuperviseWebModel model = new SuperviseWebModel();
                int j = 0;
                model.setTitle((String)res[j++]);
                Long sdId = (Long)res[j++];
                if(sdId != null){
                	model.setSender(sdId);
                }
                Date startDate = (Date)res[j++];
                model.setSendDate(startDate);
                String level=(String)res[j++];
                if(Strings.isNotBlank(level)){
                	 model.setImportantLevel(Integer.valueOf(level));
                }else{
                	 model.setImportantLevel(1);
                }
                Long deadline =(Long)res[j++];
                model.setDeadline(deadline);
                Date deadlineDatetime=(Date)res[j++];
                
                if(deadlineDatetime!=null){//新数据显示时间点
                    model.setDeadlineName(WFComponentUtil.getDeadLineName(deadlineDatetime));
                }else if(model.getDeadline()!=null){//兼容老数据，按时间段显示
                    model.setDeadlineName(WFComponentUtil.getDeadLineName(model.getDeadline()));
                }else{
                    model.setDeadlineName(EdocHelper.getDeadLineName(deadlineDatetime));
                }
                
                Date finishDate = (Date)res[j++];
                if(null != finishDate){
            	   model.setFinished(true); 
                }
                model.setAppType(ApplicationCategoryEnum.edoc.ordinal());
               
               //当前处理人信息
               EdocSummary summary = new EdocSummary();
               String cninfo=(String)res[j++];
               summary.setCurrentNodesInfo(cninfo);
               model.setCurrentNodesInfo(EdocHelper.parseCurrentNodesInfo(summary));
               
                Date now = new Date(System.currentTimeMillis());
                model.setId((Long)res[j++]);
                Date awakeDate = (Date)res[j++];
                if(awakeDate != null && now.after(awakeDate)){
                    model.setIsRed(true);
                }
                model.setAwakeDate(awakeDate);
                model.setCount(Integer.parseInt(String.valueOf(res[j++])));
                model.setSummaryId((Long)res[j++]);
                model.setContent(Strings.toHTML((String)res[j++]));
                model.setStatus((Integer)res[j++]);
                Integer entityType = (Integer)res[j++];
                model.setEntityType(entityType);
                //产品的问题 没有查询ResendTime对于的数据,又少了entityType的取值，那就先把两个赋值一样 2018-1-30 xuym
                model.setResendTime(entityType); 
                model.setBodyType("HTML");
                Object identifier = res[j++];
                if(identifier != null){
                    Boolean hasAtt = IdentifierUtil.lookupInner(identifier.toString(),0, '1');
                    model.setHasAttachment(hasAtt);
                }
               
                model.setSenderName(Functions.showMemberName(sdId));
                model.setProcessId((String)res[j++]);
                model.setCaseId((Long)res[j++]);
                model.setDocType(getDocType((String)res[j++]));
                int edocType=(Integer)res[j++];
                model.setFlowPermAccountId((Long)res[j++]);
                String govdocType = res[j++].toString();
                model.setGovdocType(govdocType);
                model.setAppName(ApplicationCategoryEnum.edoc.name());
                model.setAppName(this.getAppName(edocType,govdocType));
                EdocSummaryManager edocSummaryManager =(EdocSummaryManager) AppContext.getBean("edocSummaryManager");
            	if (model.getAppName() != null && edocSummaryManager != null) {
					Map<String,String> defaultNodeMap = null;
					if("0".equals(model.getGovdocType())){
						defaultNodeMap = edocSummaryManager.getEdocDefaultNode(model.getAppName(),model.getFlowPermAccountId());
					}else{
						defaultNodeMap = edocSummaryManager.getGovdocDefaultNode(model.getAppName(),model.getFlowPermAccountId());
					}
					model.setDefaultNodeName(defaultNodeMap.get("defaultNodeName"));
					model.setDefaultNodeLable(defaultNodeMap.get("defaultNodeLable"));
            	}
                //模版Id
                Object tempId = res[j++];
                if(tempId != null){
                    model.setIsTemplate(true);
                }else{
                    model.setIsTemplate(false);
                }
                model.setSupervisors(String.valueOf(res[j++]));
                int govDocType = -1;
                try {
                	 govDocType = (Integer) res[j++];
				} catch (Exception e) {	}
                model.setDeadlineTime(showDeadlineTime(startDate,deadline));
                if (model.getEntityType().equals(SuperviseEnum.EntityType.edoc.ordinal())){
                    model.setDetailPageUrl(getDetailPageUrl());
                }else if (model.getEntityType().equals(SuperviseEnum.EntityType.govdoc.ordinal()) || govDocType == ApplicationSubCategoryEnum.edoc_qianbao.getKey() || govDocType == ApplicationSubCategoryEnum.edoc_fawen.getKey() || govDocType == ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()||govDocType==ApplicationSubCategoryEnum.edoc_shouwen.getKey()){
                    model.setDetailPageUrl(getNewDetailPageUrl());
                    model.setEntityType(SuperviseEnum.EntityType.govdoc.ordinal());
                }
              //设置流程是否超期标志
				if(deadlineDatetime != null){
					if(finishDate==null){
						Long expendTime = now.getTime() - deadlineDatetime.getTime();
						if(expendTime > 0){
							model.setWorkflowTimeout(true);
						}
					}else{
						Long expendTime = finishDate.getTime() - deadlineDatetime.getTime();
						if(expendTime > 0){
							model.setWorkflowTimeout(true);
						}
					}
				}
				if(model.getIsTemplate()){
					long _flowPermAccountId = getFlowPermAccountId(model.getSummaryId());
					model.setFlowPermAccountId(_flowPermAccountId);
				}
                modelList.add(model);
            }
        }
        if(fi != null){
          fi.setData(modelList);
        }
        
        return modelList;
    }

	public String getDetailPageUrl() {
		return "edocController.do?method=detailIFrame";
	}
	
	public String getNewDetailPageUrl() {
        return "govdoc/govdoc.do?method=summary";
    }

	public List<SuperviseModelVO> getSuperviseModelList(FlipInfo arg0,
			Map<String, String> arg1) {
		return null;
	}
	public String getDocType(String str){
	    
		String edocType = "";
		
		if(Strings.isNotBlank(str)){
		    
		    CtpEnumItem item = enumManagerNew.getEnumItem(EnumNameEnum.edoc_doc_type, str);
	        if(item != null){
	            edocType = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", item.getLabel());
	        }
		}
		
		return edocType;
	}
	/**
	 * 流程期限换算成时间点
	 * @param createTime 创建时间
	 * @param deadline 流程期限，单位是分钟
	 * @return 创建时间+流程期限（分钟）换算出来流程期限时间点
	 */
	public static String  showDeadlineTime(Date date,Long deadline){
		if(deadline==null || deadline<=0){
			return ResourceUtil.getString("edoc.deadline.no");//无
		}
		try {
			Date afterDate = new Date(date.getTime() +deadline*60*1000);
            return Datetimes.formatDate(afterDate);
		} catch (Exception e) {
          return ResourceUtil.getString("edoc.deadline.no");//无
		}

	}
	private String getAppName(int appType,String govdocType){
		String appName="edoc";
		if("0".equals(govdocType)){
			 if(appType == 0){
				 appName=ApplicationCategoryEnum.edocSend.name();
			 }else if(appType == 1){
				 appName=ApplicationCategoryEnum.edocRec.name();
			 }else if(appType == 2){
				 appName=ApplicationCategoryEnum.edocSign.name();
			 }
		}else{
			 if(appType == 0){
				 appName=ApplicationCategoryEnum.govdocSend.name();
			 }else if(appType == 1){
				 appName=ApplicationCategoryEnum.govdocRec.name();
			 }else if(appType == 2){
				 appName=ApplicationCategoryEnum.govdocSign.name();
			 }
		}
		return appName;
	}

	public SuperviseMessageParam getSuperviseMessageParam4SaveImmediate(Long arg0)
			throws BusinessException {
		EdocSummary summary = edocManager.getEdocSummaryById(arg0, false);
		if(summary == null){
			return null;
		}
    	SuperviseMessageParam smp = new SuperviseMessageParam(true,summary.getImportantLevel(),summary.getSubject(),summary.getForwardMember(),summary.getStartMemberId());
    	return smp;
	}
	
	public long getFlowPermAccountId(long summaryId) throws BusinessException {
		EdocSummary  s = edocManager.getEdocSummaryById(summaryId,false);
		if(s!=null){
			return EdocHelper.getFlowPermAccountId(s, s.getOrgAccountId());
		}
		return 0;
	}
	
    public Map<Long,SuperviseAppInfoBO> getAppInfo(List<Long> summaryIds) throws BusinessException {
		
		Map<Long,SuperviseAppInfoBO> superviseAppInfoBOs = new HashMap<Long,SuperviseAppInfoBO>();
		EdocSummaryManager edocSummaryManager = (EdocSummaryManager)AppContext.getBean("edocSummaryManager");
    	List<SimpleEdocSummary> edocSummarys = edocSummaryManager.findSimpleEdocSummarysByIds(summaryIds);
    	for(SimpleEdocSummary summary : edocSummarys){
    		SuperviseAppInfoBO superviseAppInfoBO = new SuperviseAppInfoBO();
    		superviseAppInfoBO.setCurrentNodesInfo( EdocHelper.parseCurrentNodesInfo(summary.getCompleteTime(),summary.getCurrentNodesInfo(), Collections.EMPTY_MAP));
    		superviseAppInfoBO.setCaseId(summary.getCaseId());
    		superviseAppInfoBO.setProcessId(summary.getProcessId());
    		superviseAppInfoBO.setTemplateId(summary.getTempleteId());
    		superviseAppInfoBO.setDeadlineDatetime(summary.getDeadlineDatetime());
    		// 当流程期限不为空，且当前时间大于设置的流程期限时，设为超期
    		Boolean isCoverTime = summary.getCoverTime();
            boolean isDeadlineDatetime = summary.getDeadlineDatetime()!=null && new Date().after(summary.getDeadlineDatetime());
            if((isCoverTime != null && isCoverTime) || isDeadlineDatetime){
            	superviseAppInfoBO.setWorkflowTimeout(true);
            }
            
            superviseAppInfoBOs.put(summary.getId(),superviseAppInfoBO);
    	}
    	return superviseAppInfoBOs;
		
	}
	
    public FlipInfo getAllSuperviseList4App(FlipInfo flipInfo,Map<String,Object> params) throws BusinessException{
        return flipInfo;
    }
}
