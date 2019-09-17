package com.seeyon.apps.govdoc.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.edoc.api.GovdocApi;
import com.seeyon.apps.edoc.bo.GovdocTemplateDepAuthBO;
import com.seeyon.apps.govdoc.option.manager.FormOptionExtendManager;
import com.seeyon.apps.govdoc.option.manager.FormOptionSortManager;
import com.seeyon.apps.govdoc.po.FormOptionExtend;
import com.seeyon.apps.govdoc.po.FormOptionSort;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.modules.engin.base.formBase.GovdocTemplateDepAuthDao;
import com.seeyon.ctp.form.po.GovdocTemplateDepAuth;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;

/**
 * 新公文API实现
 * @author tanggl
 *
 */
public class GovdocApiImpl implements GovdocApi {

	private FormApi4Cap3 formApi4Cap3;
    private FormOptionExtendManager formOptionExtendManager;
    private FormOptionSortManager formOptionSortManager;
    
    private GovdocTemplateDepAuthDao govdocTemplateDepAuthDao;
	
	@Override
	public void saveGovdocExtendAndSort() throws BusinessException {
		FormBean fb = formApi4Cap3.getEditingForm();
		if (fb != null) {
			List<FormOptionSort> govdocFormOpinionSorts = formOptionSortManager.findByFormId(fb.getId());
	        FormOptionExtend govdocFormExtend = formOptionExtendManager.findByFormId(fb.getId());
	        if(govdocFormExtend==null){
	        	govdocFormExtend = new FormOptionExtend();
	        	govdocFormExtend.setAccountId(AppContext.currentAccountId());
	        	govdocFormExtend.setId(UUIDLong.longUUID());
	        	govdocFormExtend.setFormId(fb.getId());
	        	govdocFormExtend.setOptionFormatSet(FormOpinionConfig.getDefualtConfig());
	        	formOptionExtendManager.saveOrUpdate(govdocFormExtend);
	        }
	        if(null==govdocFormOpinionSorts||govdocFormOpinionSorts.size()<=0){
	        	List<FormFieldBean> formFieldBeans = fb.getAllFieldBeans();
	        	govdocFormOpinionSorts = new ArrayList<FormOptionSort>();
	            for (FormFieldBean formFieldBean : formFieldBeans) {
					if("edocflowdealoption".equals(formFieldBean.getInputType())){
						FormOptionSort govdocFormOpinionSort = new FormOptionSort();
						govdocFormOpinionSort.setId(UUIDLong.longUUID());
						govdocFormOpinionSort.setDomainId(AppContext.currentAccountId());
						govdocFormOpinionSort.setProcessName(formFieldBean.getName());
						govdocFormOpinionSort.setFormId(fb.getId());
						govdocFormOpinionSorts.add(govdocFormOpinionSort);
					}
				}
	            formOptionSortManager.saveOrUpdateList(govdocFormOpinionSorts);
	        }else{
	        	String fieldNum = "";
	        	boolean isOldOpinionField = false;
	        	for(int i=0;i<govdocFormOpinionSorts.size();i++){
	        		fieldNum = fieldNum + govdocFormOpinionSorts.get(i).getProcessName()+",";
	        	}
	        	List<FormFieldBean> formFieldBeans = fb.getAllFieldBeans();
	        	List<FormOptionSort> govdocFormOpinionSorts2 = new ArrayList<FormOptionSort>();
	        	for (FormFieldBean formFieldBean : formFieldBeans) {
					if("edocflowdealoption".equals(formFieldBean.getInputType())){
						FormOptionSort govdocFormOpinionSort1 = new FormOptionSort();
						govdocFormOpinionSort1.setId(UUIDLong.longUUID());
						govdocFormOpinionSort1.setDomainId(AppContext.currentAccountId());
						govdocFormOpinionSort1.setProcessName(formFieldBean.getName());
						govdocFormOpinionSort1.setFormId(fb.getId());
						for(FormOptionSort govdocFormOpinionSort:govdocFormOpinionSorts){
							if(govdocFormOpinionSort.getFormId()==fb.getId()&&govdocFormOpinionSort.getProcessName().equals(formFieldBean.getName())){
								govdocFormOpinionSort1.setSortType(govdocFormOpinionSort.getSortType());
							}
						}
						govdocFormOpinionSorts2.add(govdocFormOpinionSort1);
					}else if(!"edocflowdealoption".equals(formFieldBean.getInputType()) && fieldNum.contains(formFieldBean.getName())){
						isOldOpinionField = true;
					}
				}
	        	if(isOldOpinionField && (govdocFormOpinionSorts2==null || govdocFormOpinionSorts2.size()==0)){
	        		formOptionSortManager.deleteByFormId(fb.getId());
	        	}else{
	        		formOptionSortManager.saveOrUpdateList(govdocFormOpinionSorts2);	
	        	}
	        }
		}
	}

	@Override
	public List<GovdocTemplateDepAuthBO> findDepAuthListByTemplateId(long templateId) {
		List<GovdocTemplateDepAuth> list = govdocTemplateDepAuthDao.findByTemplateId(templateId);
		List<GovdocTemplateDepAuthBO> result = new ArrayList<GovdocTemplateDepAuthBO>();
		for (GovdocTemplateDepAuth auth : list) {
			result.add(GovdocUtil.toGovdocTemplateDepAuthBO(auth));
		}
		return result;
	}

	@Override
	public void deleteDepAuthByTemplateAndAuthType(Long id, int authTypeExchange) {
		govdocTemplateDepAuthDao.deleteByTemplateAndAuthType(id, authTypeExchange);
	}

	@Override
	public void deleteDepAuthByOrgIdAndTypeId(Long id, int authTypeExchange) {
		govdocTemplateDepAuthDao.deleteByOrgIdAndTypeId(id, authTypeExchange);
	}

	@Override
	public void saveDepAuthList(List<GovdocTemplateDepAuthBO> list) {
		List<GovdocTemplateDepAuth> result = new ArrayList<GovdocTemplateDepAuth>();
		for (GovdocTemplateDepAuthBO bo : list) {
			result.add(GovdocUtil.toGovdocTemplateDepAuth(bo));
		}
		DBAgent.saveAll(result);
	}

	@SuppressWarnings({ "deprecation", "rawtypes" })
	@Override
	public ColSummary getColSummaryByEdocSummary(String summaryStr) {
		ColSummary summary = new ColSummary();
		if(summaryStr.contains("com.seeyon.apps.collaboration.po.ColSummary")){
			summary = (ColSummary) XMLCoder.decoder(summaryStr);
		}else{
			EdocSummary edocSummary = (EdocSummary) XMLCoder.decoder(summaryStr);
			summary.setId(edocSummary.getId());
			summary.setCanArchive(edocSummary.get_canArchive());
			summary.setCanTrack(edocSummary.getCanTrack()==1?true:false);
			summary.setFormid(edocSummary.getFormId());
			summary.setSubject(edocSummary.getSubject());
			summary.setCanEdit(edocSummary.getCanEdit());
			summary.setCanArchive(edocSummary.getCanArchive());
			summary.setCanModify(edocSummary.getCanModify());
			summary.setCanForward(edocSummary.getCanForward());
			summary.setCanDueReminder(edocSummary.getCanDueReminder());
			summary.setAwakeDate(edocSummary.getAwakeDate());
			summary.setCanAutostopflow(edocSummary.getCanAutostopflow());
			summary.setFormAppid(edocSummary.getFormAppid());
			summary.setFormRecordid(edocSummary.getFormRecordid());
			summary.setBodyType(edocSummary.getBodyType());
			summary.setAdvanceRemind(edocSummary.getAdvanceRemind());
			summary.setAdvancePigeonhole(edocSummary.getAdvancePigeonhole());
			summary.setArchiveId(edocSummary.getArchiveId());
			summary.setImportantLevel(edocSummary.getImportantLevel());
			summary.setCanEditAttachment(edocSummary.getCanEditAttachment());
			summary.setUpdateSubject(edocSummary.getUpdateSubject());
			summary.setCanAnyMerge(edocSummary.getCanAnyMerge());
			summary.setCanMergeDeal(edocSummary.getCanMergeDeal());
			summary.setCoverTime(edocSummary.getCoverTime());
			summary.setDeadline(edocSummary.getDeadline());
			summary.setCanAutostopflow(edocSummary.getCanAutostopflow());
			summary.setTempleteId(edocSummary.getTempleteId());
			summary.setImportantLevel(edocSummary.getImportantLevel());
			summary.setAttachmentArchiveId(edocSummary.getAttachmentArchiveId());
			Map extraMap = edocSummary.getExtraMap();
			Iterator it = extraMap.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry m = (Map.Entry)it.next();
				summary.putExtraAttr((String)m.getKey(), (String)m.getValue());
			}
			
		}
		return summary;
	}

	@SuppressWarnings({ "deprecation", "rawtypes" })
	@Override
	public void setEdocSumamryToTemplate(CtpTemplate template, ColSummary colSummary) throws BusinessException {
		EdocSummary summary=new EdocSummary();
		if(template!=null && colSummary!=null){
			summary.setId(colSummary.getId());
			summary.setCanTrack(colSummary.getCanTrack()?0:1);
			summary.setFormId(colSummary.getFormid());
			summary.setSubject(colSummary.getSubject());
			summary.set_canEdit(colSummary.getCanEdit());
			summary.set_canArchive(colSummary.getCanArchive());
			summary.set_canModify(colSummary.getCanModify());
			summary.set_canForward(colSummary.getCanForward());
			summary.set_canDueReminder(colSummary.getCanDueReminder());
			summary.setCanArchive(colSummary.getCanArchive());
			summary.setAwakeDate(colSummary.getAwakeDate());
			summary.setCanAutostopflow(colSummary.getCanAutostopflow());
			summary.setFormAppid(colSummary.getFormAppid());
			summary.setFormRecordid(colSummary.getFormRecordid());
			summary.setBodyType(colSummary.getBodyType());
			summary.setAdvanceRemind(colSummary.getAdvanceRemind());
			summary.setImportantLevel(colSummary.getImportantLevel());
			summary.setAdvancePigeonhole(colSummary.getAdvancePigeonhole());
			summary.setArchiveId(colSummary.getArchiveId());
			summary.setCanEditAttachment(colSummary.getCanEditAttachment());
			summary.setUpdateSubject(colSummary.getUpdateSubject());
			summary.setDeadline(colSummary.getDeadline());
			summary.setTempleteId(colSummary.getTempleteId());
			summary.setImportantLevel(colSummary.getImportantLevel());
			summary.setCoverTime(colSummary.isCoverTime());
			summary.setUpdateSubject(colSummary.getUpdateSubject());
			summary.setCanAnyMerge(colSummary.getCanAnyMerge());
			summary.setCanMergeDeal(colSummary.getCanMergeDeal());
			summary.setAttachmentArchiveId(colSummary.getAttachmentArchiveId());
			 /*********** v7.1sp1 新增字段 strat  ************************/
			summary.setMergeDealType(colSummary.getMergeDealType());
			summary.setProcessTermType(colSummary.getProcessTermType());
			summary.setRemindInterval(colSummary.getRemindInterval());
			 /*********** v7.1sp1 新增字段 end  ************************/
			Map extraMap = colSummary.getExtraMap();
			Iterator it = extraMap.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry m = (Map.Entry)it.next();
				summary.putExtraAttr((String)m.getKey(), (String)m.getValue());
			}
			
			template.setSummary(XMLCoder.encoder(summary));
		}
	}
	

	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }
	public void setFormOptionExtendManager(FormOptionExtendManager formOptionExtendManager) {
		this.formOptionExtendManager = formOptionExtendManager;
	}
	public void setFormOptionSortManager(FormOptionSortManager formOptionSortManager) {
		this.formOptionSortManager = formOptionSortManager;
	}
	public void setGovdocTemplateDepAuthDao(GovdocTemplateDepAuthDao govdocTemplateDepAuthDao) {
		this.govdocTemplateDepAuthDao = govdocTemplateDepAuthDao;
	}

}

