package com.seeyon.v3x.edoc.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.dao.AbstractHibernateDao;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseTemplateRole;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.po.template.CtpTemplateAuth;
import com.seeyon.ctp.common.po.template.CtpTemplateCategory;
import com.seeyon.ctp.common.po.template.CtpTemplateOrg;
import com.seeyon.ctp.common.template.enums.Approve;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormAuthViewBean;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.bean.FormTableBean;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.workflow.po.ProcessTemplete;
import com.seeyon.v3x.edoc.domain.EdocCategory;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.domain.EdocFormFileRelation;
import com.seeyon.v3x.edoc.domain.EdocSummary;

import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMTransition;
import net.joinwork.bpm.engine.execute.BPMCase;

public class UpgradeDaoImpl extends AbstractHibernateDao<Object> implements UpgradeDao{

	private List<String> tempList = new ArrayList<String>();
	
	private final Map<Long, Long> dataT = new HashMap<Long, Long>();
	private static final String TYPE = "template"; 
	private FormApi4Cap3 formApi4Cap3;
	private EnumManager enumManagerNew = (EnumManager)AppContext.getBean("enumManagerNew");
	private static Log logger =  CtpLogFactory.getLog(UpgradeDaoImpl.class);
	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }
	
	public List<String> getTempList() throws BusinessException{
		return tempList;
	}


	public void setTempList(List<String> tempList) {
		this.tempList = tempList;
	}
	
	@Override
	public List<EdocCategory> getAllEdocCategory() {
		String hsql = "from EdocCategory where rootCategory = 19";
		return DBAgent.find(hsql);
	}
	
	@Override
	public void deleteRoleMember(V3xOrgRole role) throws BusinessException, SQLException {
		//OrgConstants.RelationshipType.Member_Role
		if (role != null && role.getId() !=null) {
			String sql = "delete from org_relationship where type = 'Membber_Role' and objective1_id = '" + role.getId() + "'";
			JDBCAgent agent = null;
			try{
				agent = new JDBCAgent(true);
				agent.execute(sql);
			}finally{
				if(agent != null){
					try{
						agent.close();
					}catch (Exception e){
						logger.error(e);
					}	
				}
			}
			
		}
	}
	
	private CtpTemplate getTemplate(CtpTemplate sourTemp,Map<Long, CtpTemplateCategory> categories,Map<Long, Long> formIds){
		CtpTemplate temp = new CtpTemplate();
        try {
        	temp = (CtpTemplate) BeanUtils.cloneBean(sourTemp); 
        	temp.setNewId();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        if(sourTemp.getCategoryId().equals(19l)){
        	temp.setCategoryId(401l);
        	temp.setModuleType(401);
        }else if(sourTemp.getCategoryId().equals(20l)){
        	temp.setCategoryId(402l);
        	temp.setModuleType(402);
        }else if(sourTemp.getCategoryId().equals(21l)){
        	temp.setCategoryId(404l);
        	temp.setModuleType(404);
        }else {
        	temp.setCategoryId(categories.get(temp.getCategoryId()).getId());
		}
        
        if(sourTemp.getModuleType().equals(19)){
        	temp.setModuleType(401);
        }else if(sourTemp.getModuleType().equals(20)){
        	temp.setModuleType(402);
        }else if(sourTemp.getModuleType().equals(21)){
        	temp.setModuleType(404);
        }
        temp.setSystem(sourTemp.isSystem());
        temp.setDelete(sourTemp.isDelete());
        EdocSummary edocSummary = (EdocSummary) XMLCoder.decoder(sourTemp.getSummary());
        edocSummary.setCanModify(true);
        edocSummary.setCanArchive(true);
        temp.setSummary(XMLCoder.encoder(edocSummary));
        temp.setModifyDate(new Date());  
        String curTem = temp.getSummary();
        curTem = curTem.replaceAll("<__canArchive>false</__canArchive>", "<__canArchive>true</__canArchive>").replaceAll("<__canModify>false</__canModify>", "<__canModify>true</__canModify>");
        temp.setSummary(curTem);
        temp.setType(TemplateEnum.Type.template.name());
        return temp;
	}
	
	private List<CtpTemplateAuth> getAuthsByTempID(CtpTemplate sourTemp,CtpTemplate temp,Map<Long, Long> formIds){
		// 授权
        String authSql = "from CtpTemplateAuth auth where auth.moduleId = :mId";
        EdocSummary edocSummary = (EdocSummary) XMLCoder.decoder(sourTemp.getSummary());
        Map pMap = new HashMap();
        pMap.put("mId",sourTemp.getId());
        List<CtpTemplateAuth> auths = DBAgent.find(authSql,pMap);
        List<CtpTemplateAuth> authsRes = new ArrayList<CtpTemplateAuth>();
        for(CtpTemplateAuth authTemp:auths){
	        CtpTemplateAuth authT = new CtpTemplateAuth();
	        try {
	        	authT = (CtpTemplateAuth) BeanUtils.cloneBean(authTemp); 
	        	authT.setNewId();
	        } catch (Exception e) {
	            logger.error(e.getLocalizedMessage(),e);
	        }
	        authT.setModuleId(temp.getId());
	        int formType = formApi4Cap3.getForm(formIds.get(edocSummary.getFormId())).getFormType();
	        if(formType == 5){
	        	authT.setModuleType(401);
	        }else if(formType == 7){
	        	authT.setModuleType(402);
			}else if(formType == 6){
	        	authT.setModuleType(401);
			}else if(formType == 8){
	        	authT.setModuleType(404);
			}
	        authsRes.add(authT);
        }
        return authsRes;
	}
	
	private List<CtpContentAll> getContentListByTemp(CtpTemplate sourTemp, CtpTemplate temp,Map<Long, Long> formIds) {
		String contentSql = "from CtpContentAll content where content.moduleId = :mId";
		List<CtpContentAll> contentAlls =  new ArrayList<CtpContentAll>();
		Map pMap = new HashMap();
        pMap.put("mId",sourTemp.getId());
		EdocSummary edocSummary = (EdocSummary) XMLCoder.decoder(sourTemp.getSummary());
		List<CtpContentAll> content = super.find(contentSql,pMap);
		for(CtpContentAll c:content){
			CtpContentAll cc = new CtpContentAll();
		    try {
		    	cc = (CtpContentAll) BeanUtils.cloneBean(c); 
		    	cc.setNewId();
		    } catch (Exception e) {
		        logger.error(e.getLocalizedMessage(),e);
		    }
		    
		    cc.setModuleId(temp.getId());
		    if (c.getModuleType() == 19) {
		    	cc.setModuleType(401);
			}else if (c.getModuleType() == 20) {
				cc.setModuleType(402);
			}else if (c.getModuleType() == 21) {
				cc.setModuleType(404);
			}
		    cc.setTitle(temp.getSubject());
		    cc.setModuleTemplateId(temp.getId());
		    cc.setModifyId(temp.getId());
		    cc.setSort(2);
		    cc.setModifyDate(temp.getModifyDate());
		    cc.setContentTemplateId(formIds.get(edocSummary.getFormId()));
		    if(c.getContentDataId()!=null){
		    	cc.setContent(String.valueOf(c.getContentDataId()));
		    	c.setContentDataId(-1l);
			}
		    CtpContentAll formT = new CtpContentAll();
		    try {
		    	formT = (CtpContentAll) BeanUtils.cloneBean(cc); 
		    	formT.setNewId();
		    } catch (Exception e) {
		        logger.error("正文内容数据升级错误！"+e.getLocalizedMessage(),e);
		    }
		    formT.setContentType(20);
		    formT.setModuleId(temp.getId());
		    formT.setContentDataId(0l);
		    formT.setModuleTemplateId(-1l);
		    formT.setContent(StringUtils.EMPTY);
		    formT.setSort(1);
		    contentAlls.add(cc);
		    contentAlls.add(formT);
		    temp.setBody(formT.getId());
		}
		return contentAlls;
	}
	
	@Override
	public void insterEdocElements(List<String> sqls) throws BusinessException, SQLException {
		JDBCAgent jdbcAgent = null;
		try{
			jdbcAgent = new JDBCAgent(true);
			jdbcAgent.executeBatch(sqls);
		}finally{
			if(jdbcAgent != null){
				try{
					jdbcAgent.close();
				}catch(Exception e){
					logger.error(e);					
				}
			}
		}	
	}
	
	@Override
	public void saveDataRelation2(List<Long> data, String type) throws BusinessException, SQLException{
		if (data == null || data.size() < 1) {
			return;
		}
		List<String> sqls = new ArrayList<String>();
		for (Long newId : data) {
			String sql = "insert into upgrade_data_relation values(" + UUIDLong.longUUID() + ",-1," + newId + ",'" + type + "','')";
			sqls.add(sql);
		}
		JDBCAgent jdbcAgent = null;
		try{
			jdbcAgent = new JDBCAgent(true);
			jdbcAgent.executeBatch(sqls);
		}finally{
			if(jdbcAgent != null){
				try{
					jdbcAgent.close();
				}catch(Exception e){
					logger.error(e);					
				}
			}
		}	
	}
	
	@Override
	public void doBasejdqx(List<ConfigItem> sourList,List<Long> accounts) {
		String sql = "from ConfigItem c where c.configType = :configType  and (c.configCategory = 'edoc_qianbao_permission_policy' or c.configCategory = 'edoc_send_permission_policy' or c.configCategory = 'edoc_rec_permission_policy') and c.orgAccountId = :orgAccountId ";
		HashMap<String, Object> params = new HashMap<String, Object>();
		for(Long temp:accounts){
			if (temp.equals(-1730833917365171641l)) {
				continue;
			}
			params.clear();
			params.put("orgAccountId", temp);
			params.put("configType", "1");
			sourList.addAll(DBAgent.find(sql, params));
		}

		List<ConfigItem> result = new ArrayList<ConfigItem>();
		for(Long id:accounts){
			for(ConfigItem sourTemp:sourList){
				if("1".equals(sourTemp.getConfigType()) && !sourTemp.getOrgAccountId().equals(id)){
					continue;
				}

				ConfigItem temp = new ConfigItem();
		        try {
		        	temp = (ConfigItem) BeanUtils.cloneBean(sourTemp); 
		        } catch (Exception e) {
		            logger.error(e.getLocalizedMessage(),e);
		        }
		        String extConfigValue = sourTemp.getExtConfigValue();
	        	if(EnumNameEnum.edoc_send_permission_policy.name().equals(sourTemp.getConfigCategory())){
	        		temp.setConfigCategory(EnumNameEnum.edoc_new_send_permission_policy.name());
	        	}else if (EnumNameEnum.edoc_rec_permission_policy.name().equals(sourTemp.getConfigCategory())) {
	        		temp.setConfigCategory(EnumNameEnum.edoc_new_rec_permission_policy.name());
	        		extConfigValue = extConfigValue.replaceAll(",TanstoPDF,", ",").replaceAll(",TanstoPDF", "").replaceAll("TanstoPDF,", "");
	        		temp.setExtConfigValue(extConfigValue);
				}else if (EnumNameEnum.edoc_qianbao_permission_policy.name().equals(sourTemp.getConfigCategory())) {
	        		temp.setConfigCategory(EnumNameEnum.edoc_new_qianbao_permission_policy.name());
				}
	        	extConfigValue = extConfigValue.replaceAll(", Sign,", ", ContentSign,");
	        	temp.setExtConfigValue(extConfigValue);
		        temp.setNewId();
		        temp.setOrgAccountId(id);
		        result.add(temp);
			}
		}
		DBAgent.saveAll(result);
	}
	
	public void templateUpgrade(Map<Long, Long> formIds,Map<Long, CtpTemplateCategory> categories) throws BusinessException{
		// 格式模板由于没有流程所以不做流程绑定
		String sql = "from CtpTemplate where moduleType in (19,20,21) and type != :type";
		Map pMap = new HashMap();
        pMap.put("type","text");
		List<CtpTemplate> temList = super.find(sql,-1,-1,pMap);
		List<CtpTemplate> upgrade =  new ArrayList<CtpTemplate>();
		List<CtpTemplate> oldPTempArray =  new ArrayList<CtpTemplate>();
		List<CtpContentAll> contentAlls =  new ArrayList<CtpContentAll>();
		List<Attachment> attResult = new ArrayList<Attachment>();
		List<CtpSupervisor> supervisorResult = new ArrayList<CtpSupervisor>();
        List<CtpSuperviseDetail> detailsResult = new ArrayList<CtpSuperviseDetail>();
        List<CtpTemplateAuth> authsRes = new ArrayList<CtpTemplateAuth>();
        List<ProcessTemplete> pTempletes = new ArrayList<ProcessTemplete>();
        List<ProcessTemplete> oldProcessTemplates = new ArrayList<ProcessTemplete>();
        List<CtpSuperviseTemplateRole> ctpTemplateRoles = new ArrayList<CtpSuperviseTemplateRole>();
		for(CtpTemplate sourTemp:temList){ // 系统模板不执行
			EdocSummary edocSummary = (EdocSummary) XMLCoder.decoder(sourTemp.getSummary());
			if(formIds.get(edocSummary.getFormId()) == null){
				logger.info("temList.size()=" + temList.size() +",sourTemp.getId()="+sourTemp.getId()+",edocSummary.getFormId()="+edocSummary.getFormId());
	        	continue;
	        }
			//模板对象
			CtpTemplate temp = getTemplate(sourTemp,categories,formIds);
			dataT.put(sourTemp.getId(), temp.getId());
	        //正文
	        contentAlls.addAll(getContentListByTemp(sourTemp,temp,formIds));
	        //授权
	        authsRes.addAll(getAuthsByTempID(sourTemp, temp,formIds));
	        // 督办
	        List<Object> curList = getSuDetailsByTempID(sourTemp,temp);
	        if(CollectionUtils.isNotEmpty(curList)){
	        	//督办人
	        	supervisorResult.addAll((List<CtpSupervisor>)curList.get(0));
	        	//督办详情
	        	detailsResult.addAll((List<CtpSuperviseDetail>)curList.get(1));
	        }
	        temp.setCanSupervise(true);
	        //附件
	        attResult.addAll(getAttByTempID(sourTemp, temp));
	        if (temp.getWorkflowId() != null) {
	        	//升级新模板
	        	pTempletes.addAll(getNewProcessTempletes(sourTemp,temp,formIds));
	        	//升级旧的模板
	        	oldProcessTemplates.addAll(getOldProcessTemplate(sourTemp));
			}
	        if (temp.getFormParentid()!=null) {
				temp.setFormParentid(dataT.get(temp.getFormParentid()));
			}
	        ctpTemplateRoles.addAll(getRoles(sourTemp, temp));
	        temp.setBodyType("20");
	        upgrade.add(temp);
	        
	        if(temp.getState().intValue() == TemplateEnum.State.invalidation.ordinal()){ //未发布
	        	if(temp.getPublishTime() != null && temp.getPublishTime().after(new Date())){
	        		temp.setSubstate(Approve.ApproveType.toBeReleased.getKey());
	        	}else{
	        		temp.setSubstate(Approve.ApproveType.haveReleased.getKey());
	        	}
	        }else{
	        	temp.setSubstate(Approve.ApproveType.haveReleased.getKey());
	        }
	        temp.setFormAppId(formIds.get(edocSummary.getFormId()));// form_definitionID
	        
	        //升级RACI信息
	        StringBuilder orgsql = new StringBuilder();
	    	orgsql.append(" from CtpTemplateOrg cto where cto.templateId =:templateId ");
	    	Map<String, Object> queryParams = new HashMap<String, Object>();
	    	queryParams.put("templateId", sourTemp.getId());
	    	List<CtpTemplateOrg> list = DBAgent.find(orgsql.toString(), queryParams);
	    	List<CtpTemplateOrg> listNew = new ArrayList();
	    	if(Strings.isNotEmpty(list)){
	    		CtpTemplateOrg org = null;
	    		for(int a =0; a < list.size(); a ++) {
	    			try {
	    				org = list.get(a);
						CtpTemplateOrg clone = (CtpTemplateOrg)org.clone();
						clone.setId(UUIDLong.longUUID());
						clone.setTemplateId(temp.getId());
						listNew.add(clone);
					} catch (CloneNotSupportedException e) {
						logger.error("升级模板raci信息报错",e);
					}
	    		}
	    		if(Strings.isNotEmpty(listNew)){
	    			DBAgent.saveAll(listNew);
	    		}
	    	}
	        if(!sourTemp.isSystem()){
	        	sourTemp.setDelete(true);
	        	oldPTempArray.add(sourTemp);
	        }
		}
		// 保存 以上所有数据都保存
		if (CollectionUtils.isNotEmpty(upgrade)) {
			DBAgent.saveAll(upgrade);
		}
		if (CollectionUtils.isNotEmpty(contentAlls)) {
			DBAgent.saveAll(contentAlls);
		}
		if (CollectionUtils.isNotEmpty(authsRes)) {
			DBAgent.saveAll(authsRes);
		}
		if (CollectionUtils.isNotEmpty(supervisorResult)) {
			DBAgent.saveAll(supervisorResult);
		}
		if (CollectionUtils.isNotEmpty(detailsResult)) {
			DBAgent.saveAll(detailsResult);
		}
		if (CollectionUtils.isNotEmpty(attResult)) {
			DBAgent.saveAll(attResult);
		}
		if (CollectionUtils.isNotEmpty(pTempletes)) {
			DBAgent.saveAll(pTempletes);
			try {
				saveDataRelation(dataT,TYPE);
			} catch (SQLException e) {
				throw new BusinessException(e);
			}
		}
		if (CollectionUtils.isNotEmpty(oldPTempArray)) {
			DBAgent.updateAll(oldPTempArray);
		}
		if (CollectionUtils.isNotEmpty(ctpTemplateRoles)) {
			DBAgent.saveAll(ctpTemplateRoles);
		}
		
		if(CollectionUtils.isNotEmpty(oldProcessTemplates)){
			DBAgent.updateAll(oldProcessTemplates);
		}
	
	}
	
	private List<Object> getSuDetailsByTempID(CtpTemplate sourTemp,CtpTemplate temp){
		List<Object> resuList = new ArrayList<Object>();
		//督办
		String superSql = "from CtpSuperviseDetail super where entityId = :enId ";
        Map pMap = new HashMap();
        pMap.put("enId",sourTemp.getId());
        List<CtpSuperviseDetail> details = super.find(superSql,pMap);
        List<CtpSupervisor> supervisorResult = new ArrayList<CtpSupervisor>();
        List<CtpSuperviseDetail> detailsResult = new ArrayList<CtpSuperviseDetail>();
        for(CtpSuperviseDetail detailTemp:details){
        	CtpSuperviseDetail authT = new CtpSuperviseDetail();
	        try {
	        	authT = (CtpSuperviseDetail) BeanUtils.cloneBean(detailTemp); 
	        	authT.setNewId();
	        } catch (Exception e) {
	            logger.error("升级督办数据错误"+e.getLocalizedMessage(),e);
	        }
	        authT.setEntityId(temp.getId());
	        
	        superSql = "from CtpSupervisor super where superviseId = :deId";
	        Map p2Map = new HashMap();
	        p2Map.put("deId", detailTemp.getId());
	        List<CtpSupervisor> supervisors = super.find(superSql,p2Map);
	        for(CtpSupervisor t:supervisors){
	        	CtpSupervisor dd = new CtpSupervisor();
		        try {
		        	dd = (CtpSupervisor) BeanUtils.cloneBean(t); 
		        	dd.setNewId();
		        } catch (Exception e) {
		            logger.error(e.getLocalizedMessage(),e);
		        }
		        dd.setSuperviseId(authT.getId());
		        if (t.getSupervisorId().equals(0l)) {
					t.setSuperviseId(null);
				}
		        supervisorResult.add(dd);
	        }
	        detailsResult.add(authT);
        }
        if (CollectionUtils.isNotEmpty(detailsResult)) {
        	 resuList.add(supervisorResult);
             resuList.add(detailsResult);
		}
		return resuList;
	}
	
	
	private List<Attachment> getAttByTempID(CtpTemplate sourTemp,CtpTemplate temp){
        String attSql = "from Attachment t where t.category = 4 and t.reference = :objId and t.subReference = :objId";
        List<Attachment> attResult = new ArrayList<Attachment>();
        Map pMap = new HashMap();
        pMap.put("objId",sourTemp.getId());
        List<Attachment> attachments = super.find(attSql,pMap);
        for(Attachment attTemp:attachments){
        	Attachment dd = new Attachment();
	        try {
	        	dd = (Attachment) BeanUtils.cloneBean(attTemp); 
	        	dd.setNewId();
	        } catch (Exception e) {
	            logger.error("附件升级错误",e);
	        }
	        dd.setReference(temp.getId());
	        dd.setSubReference(temp.getId());
	        dd.setCategory(1);
	        attResult.add(dd);
        }
        return attResult;
	}
	
	private List<ProcessTemplete> getNewProcessTempletes(CtpTemplate sourTemp,CtpTemplate temp,Map<Long, Long> formIds){
		String jdSql = "from ProcessTemplete where id = :pId";
		Map map  = new HashMap();
		map.put("pId", temp.getWorkflowId());
        List<ProcessTemplete> pTempletes = new ArrayList<ProcessTemplete>();
        List<ProcessTemplete> ddList = super.find(jdSql,map);
        for(ProcessTemplete temp1:ddList){
       	 ProcessTemplete curTemplete = new ProcessTemplete();
            try {
           	 curTemplete = (ProcessTemplete) BeanUtils.cloneBean(temp1); 
           	 curTemplete.setId(System.currentTimeMillis());
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(),e);
            }
            String word = temp1.getWorkflow();
            if (sourTemp.getModuleType().equals(ApplicationCategoryEnum.edocSend.getKey())) {
            	 word = word.replaceAll("fengfa", "faxing").replaceAll("封发", "分送");
			}else if(sourTemp.getModuleType().equals(ApplicationCategoryEnum.edocRec.getKey())){
				word = word.replaceAll("dengji", "fenban").replaceAll("分发", "分办");
			}
            EdocSummary edocSummary = (EdocSummary) XMLCoder.decoder(sourTemp.getSummary());
            FormBean fBean = formApi4Cap3.getForm(formIds.get(edocSummary.getFormId()));
           
	        if(formIds.get(edocSummary.getFormId())!=null){
	        	curTemplete.setAppId(formIds.get(edocSummary.getFormId()));
	        	word = word.replaceAll(" f=\"\"", " f=\""+formIds.get(edocSummary.getFormId())+"\"");
	        	word = word.replaceAll(" e=\"\"", " e=\""+fBean.getFormViewList().get(0).getId()+"\"");
	        	List<FormTableBean> tableBeans = fBean.getTableList();
	        	if (CollectionUtils.isNotEmpty(tableBeans)) {
	        		List<FormAuthViewBean> operationList = fBean.getAllFormAuthViewBeans();
	        		word = word.replaceAll(" r=\"\"", " r=\""+operationList.get(0).getId()+"\"");
				}
	        }
	        
	        int formType = fBean.getFormType();
	        if(formType == 6 ||formType == 9){
	        	word = word.replaceAll("Departmentexchange", "DepartmentGovdocSend").replaceAll("公文收发员", "公文送文员");
	        }else if(formType == 8){
	        	word = word.replaceAll("Departmentexchange", "DepartmentGovdocRec").replaceAll("公文收发员", "公文收文员");
			}
	        
	        //替换分支条件
	        Map<String,FormFieldBean> fieldMapping	=  getAllFieldMapping(fBean);
	        BPMProcess process = BPMProcess.fromXML(word);
	        List<BPMTransition> transitions = process.getLinks();
	        for (BPMTransition bpmTransition : transitions) {
				//手动分支和无分支条件的不进行替换
	        	if(bpmTransition.getConditionType() ==2 || bpmTransition.getConditionType()==3){
					continue;
				}
				String condition = bpmTransition.getFormCondition();
				
				//公文单分支条件替换
				Pattern p = Pattern.compile("([^\\s()\\[\\]!=<>&|\\^+*/%\\$#'\":;,?\\\\]+)\\s*(==|!=|>|<)\\s*(\\d+)");
		    	Matcher m = p.matcher(condition);
		        StringBuffer sb = new StringBuffer();
		        boolean hasFind = false;
		    	while(m.find()){
		    		hasFind = true;
		    		String first = m.group(1);
		    		String second = m.group(2);
		    		String third = m.group(3);
		    				            
		            if(first.contains("{")){
		                first = first.replace("{", "").replace("}", "");
		            }
		    		
		            FormFieldBean formFieldBean = fieldMapping.get(first);
		            if(formFieldBean!=null){
		            	if(formFieldBean.getEnumId()!=0L && formFieldBean.getEnumId()!=-1L){
		            		//找枚举项
		            		CtpEnumItem enumItem = null;
		            		try {
		            			EnumManager enumManagerNew = (EnumManager)AppContext.getBean("enumManagerNew");
		            			//公文枚举通通使用第一层枚举
		            			enumItem = enumManagerNew.getCtpEnumItem(formFieldBean.getEnumId(), 0, third);
		            		} catch (BusinessException e1) {
		            			logger.error(e1);
		            		}
		            		if (enumItem!=null) {
		            			third = enumItem.getId()+"";
		            		}
		            		String  translateGroup = "compareField('"+second+"',"+formFieldBean.getName() + ",'"+ third+"')";
		            		m.appendReplacement(sb, translateGroup);
		            	}else{
		            		String  translateGroup = formFieldBean.getName() + " " + second + " " + third;
		            		m.appendReplacement(sb, translateGroup);
		            	}
		            }
		    	}
				if(hasFind){
					condition = sb.toString();
				}
				for (Map.Entry<String,FormFieldBean> entry : fieldMapping.entrySet()) {
					String key = entry.getKey();
					FormFieldBean bean = entry.getValue();
					if(condition.contains(key)){
						condition = condition.replace(key, bean.getName());
					}
				}
				bpmTransition.setFormCondition(condition);
			}
	        
	        BPMCase theCase = null;
	        String dbProcessXml = process.toDBXML(theCase, true);
            curTemplete.setWorkflow(dbProcessXml);
            temp.setWorkflowId(curTemplete.getId());
            pTempletes.add(curTemplete);
        }
        return pTempletes;
	}
	
	//升级旧的模板流程
	private List<ProcessTemplete> getOldProcessTemplate(CtpTemplate sourTemp){
		String jdSql = "from ProcessTemplete where id = :pId";
		Map map  = new HashMap();
		map.put("pId", sourTemp.getWorkflowId());
        List<ProcessTemplete> oldProcessTemplates = new ArrayList<ProcessTemplete>();
        List<ProcessTemplete> ddList = super.find(jdSql,map);
        for(ProcessTemplete temp1:ddList){
            String word = temp1.getWorkflow();
            if (sourTemp.getModuleType().equals(ApplicationCategoryEnum.edocSend.getKey())) {
            	 word = word.replaceAll("fengfa", "faxing").replaceAll("封发", "分送");
			}else if(sourTemp.getModuleType().equals(ApplicationCategoryEnum.edocRec.getKey())){
				word = word.replaceAll("dengji", "fenban").replaceAll("分发", "分办");
			}
	        
	        if(sourTemp.getModuleType().equals(ApplicationCategoryEnum.edocSend.getKey()) ||sourTemp.getModuleType().equals(ApplicationCategoryEnum.edocSign.getKey())){
	        	word = word.replaceAll("Departmentexchange", "DepartmentGovdocSend").replaceAll("公文收发员", "公文送文员");
	        }else if(sourTemp.getModuleType().equals(ApplicationCategoryEnum.edocRec.getKey())){
	        	word = word.replaceAll("Departmentexchange", "DepartmentGovdocRec").replaceAll("公文收发员", "公文收文员");
			}
	        
	        temp1.setWorkflow(word);
            oldProcessTemplates.add(temp1);
        }
        return oldProcessTemplates;
	}
	
	private Map<String,FormFieldBean> getAllFieldMapping(FormBean fb){
		Map<String,FormFieldBean> map = new HashMap<String,FormFieldBean>();
		if(fb==null || Strings.isEmpty(fb.getAllFieldBeans())){
			return map;
		}
		List<FormFieldBean> fieldBeans = fb.getAllFieldBeans();
		for (FormFieldBean formFieldBean : fieldBeans) {
			if(Strings.isBlank(formFieldBean.getMappingField())){
				continue;
			}
			map.put(formFieldBean.getMappingField(), formFieldBean);
		}
		return map;
	}
	
	public String getDbType(){
		JDBCAgent agent = new JDBCAgent(true);
		String dbType = agent.getDBType().toLowerCase();
		agent.close();
		return dbType;
	}
	
	@Override
	public void excuteSql(String sql) throws BusinessException, SQLException {
		JDBCAgent agent = new JDBCAgent(true);
		try {
			agent.execute(sql);
		}catch (Exception e) {
		}finally{
			agent.close();
		}
	}
	
	@Override
	public List<String> getTableName(String tables) throws Exception{
		List<String> tableNames = new ArrayList<String>();
		String mysql = "SELECT DISTINCT TABLE_NAME from information_schema.tables where  table_name like '"+tables+"%' and TABLE_SCHEMA=(select DATABASE())";
		String oracle = "select Table_Name from dba_tables where OWNER = (select user from dual) AND table_name like '"+tables+"%'";
		String sqlserver = "SELECT DISTINCT NAME from sys.tables where name like '"+tables+"%' AND type='U'";
		JDBCAgent agent = null;
		try{
			agent = new JDBCAgent(true);
			String dbType = agent.getDBType().toLowerCase();
			if(dbType.indexOf("oracle") > -1 && null != oracle){
				agent.execute(oracle);
			}else if (dbType.indexOf("mysql") > -1 && null != mysql){
				agent.execute(mysql);
			}else if(dbType.indexOf("sqlserver") > -1 && null != sqlserver){
				agent.execute(sqlserver);
			}else{//兼容 达梦 等其他数据库  据说和sqlserver相同
				agent.execute(sqlserver);
			}
			ResultSet res = agent.getQueryResult();
			while(res.next()){
				tableNames.add(res.getString(1));
			}
		} finally{
			try{
				if(agent != null){					
					agent.close();
				}
			} catch(Exception e){
				logger.error(e);
			}		
		}
		return tableNames;
	}
	
	@Override
	public Object getDataByHql(String hql,	Map<String, Object> map, FlipInfo fileinfo) {
		return DBAgent.find(hql,map ,fileinfo);
	}
	
	@Override
	public List<CtpAffair> getAffairBySubObjectIds(List ids) {
		String hql = "from CtpAffair as affair where affair.subObjectId in (:subObjectId)";
		Map map = new HashMap();
		map.put("subObjectId", ids);
		List list= DBAgent.find(hql, map);
		return list;
	}
	
	@Override
	public void updateAll(List listObj) {
		if(null != listObj && !listObj.isEmpty()){
			DBAgent.updateAll(listObj);
		}
	}
	@Override
	public void saveAll(List listObj) {
		if(null != listObj && !listObj.isEmpty()){
			DBAgent.saveAll(listObj);
		}
	}
	
	@Override
	public void saveOrUpdateAll(List listObj) {
		if(null != listObj){
		  for(int i = 0; i < listObj.size(); i++)
	       {
	           DBAgent.saveOrUpdate(listObj.get(i));//防止重复执行升级程序，造成ID重复
	       }
		}
	}
	
	public void saveDataRelation(Map<Long, Long> data, String type) throws BusinessException, SQLException{
		if (data == null || data.size() < 1) {
			return;
		}
		List<String> sqls = new ArrayList<String>();
		for (Long oldId : data.keySet()) {
			String sql = "insert into upgrade_data_relation values(" + UUIDLong.longUUID() + "," + oldId + "," + data.get(oldId) + ",'" + type + "','')";
			sqls.add(sql);
		}
		JDBCAgent jdbcAgent = null;
		try{
			jdbcAgent = new JDBCAgent(true);
			jdbcAgent.executeBatch(sqls);
		}finally{
			if(jdbcAgent != null){
				try{
					jdbcAgent.close();
				}catch(Exception e){
					logger.error(e);					
				}
			}
		}	
	}
	
	private List<CtpSuperviseTemplateRole> getRoles(CtpTemplate sourTemp,CtpTemplate temp){
		List<CtpSuperviseTemplateRole> ctpTemplateRoles = new ArrayList<CtpSuperviseTemplateRole>();
		String sql = "from CtpSuperviseTemplateRole where superviseTemplateId = :ctrID";
		Map map = new HashMap();
		map.put("ctrID", sourTemp.getId());
        List<CtpSuperviseTemplateRole> templateRoles = super.find(sql,map);
        if (CollectionUtils.isNotEmpty(templateRoles)) {
           	 for(CtpSuperviseTemplateRole detailTemp:templateRoles){
           		CtpSuperviseTemplateRole authT = new CtpSuperviseTemplateRole();
        	        try {
        	        	authT = (CtpSuperviseTemplateRole) BeanUtils.cloneBean(detailTemp); 
        	        	authT.setNewId();
        	        } catch (Exception e) {
        	            logger.error("升级督办数据错误"+e.getLocalizedMessage(),e);
        	        }
        	        authT.setSuperviseTemplateId(temp.getId());
        	        ctpTemplateRoles.add(authT);
			  }
        }
		
		return ctpTemplateRoles;
	}
	
	/**
     * 获取所有单位的在用的文单
     * @return
     */
    public List<EdocForm> getAllEdocForm(){
    	String hsql = "select a from EdocForm as a left join a.edocFormExtendInfo as info  where info.status<> :efs ";
		hsql+=" order by info.status desc,a.type asc , a.lastUpdate asc";
		Map map  = new HashMap();
		map.put("efs",EdocForm.C_iStatus_Deleted);
		return super.find(hsql,-1,-1,map);
	}
    
    @Override
	public List<PermissionVO> getPermission(String category,Long unitId) {
		String sql = "from ConfigItem c where c.configCategory = :category  and c.orgAccountId = :orgAccountId";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("category", category);
		params.put("orgAccountId", unitId);
		List<ConfigItem> list = DBAgent.find(sql,params);
		List<PermissionVO> result = new ArrayList<PermissionVO>();
		for (ConfigItem item : list) {
			PermissionVO permissionVO = new PermissionVO();
			permissionVO.setFlowPermId(item.getId());
            permissionVO.setCategory(item.getConfigCategory());
            permissionVO.setCategoryName(ResourceUtil.getString("permission."+item.getConfigCategory()));
            permissionVO.setDescription(item.getConfigDescription());
            permissionVO.setOrgAccountId(item.getOrgAccountId());
            permissionVO.setName(item.getConfigItem());
            String configType = item.getConfigType();
            if(Strings.isNotBlank(configType)){
                permissionVO.setType(Integer.valueOf(configType));
                if(Permission.Node_Type_System.toString().equals(configType)){
                    //系统预置 类型
                    permissionVO.setTypeName(ResourceUtil.getString("permission.type.system"));
                    //权限名称
                    String label = enumManagerNew.getEnumItemLabel(EnumNameEnum.valueOf(item.getConfigCategory()), item.getConfigItem());
                    permissionVO.setLabel(ResourceUtil.getString(label));
                }else{
                    permissionVO.setTypeName(ResourceUtil.getString("permission.type.custome"));
                    permissionVO.setLabel(item.getConfigItem());
                }
            }
			result.add(permissionVO);
		}
		return result;
	}

    @Override
	public void upgradeAIP(Long oldFormId, Long newFormId) {
		String hql = "from EdocFormFileRelation where formId = :ofId";
		Map map = new HashMap();
		map.put("ofId",oldFormId);
		List<EdocFormFileRelation> list = super.find(hql, map);
		if (list != null && list.size() > 0) {
			EdocFormFileRelation  relation = list.get(0);
			EdocFormFileRelation relation2;
			try {
				relation2 = (EdocFormFileRelation) relation.clone();
				relation2.setNewId();
				relation2.setFormId(newFormId);
				super.save(relation2);
			} catch (CloneNotSupportedException e) {
				logger.error(e);
				//e.printStackTrace();
			}
		}
	}
    
    @SuppressWarnings("unchecked")
	public EdocSummary getSummarySerialNo(Long summaryI) throws BusinessException {
		EdocSummary summary = new EdocSummary();
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("summaryId", summaryI);
		List<Object[]> list = (List<Object[]>)super.find("select id, serialNo from EdocSummary where id =:summaryId", parameterMap);
		if(Strings.isNotEmpty(list)) {
			Object[] object = list.get(0);
			summary.setId((Long)object[0]);
			summary.setSerialNo((String)object[1]);
		}
		return summary;
	}

}
