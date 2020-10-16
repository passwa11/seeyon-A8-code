package com.seeyon.apps.govdoc.option.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.option.dao.FormOptionSortDao;
import com.seeyon.apps.govdoc.po.FormOptionSort;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormAuthViewBean;
import com.seeyon.ctp.form.bean.FormAuthViewFieldBean;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormViewBean;
import com.seeyon.ctp.form.po.FormPermissionConfig;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.edoc.constants.EdocOpinionDisplayEnum.OpinionDisplaySetEnum;
import com.seeyon.v3x.edoc.dao.EdocOpinionDao;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.util.SharedWithThreadLocal;
import com.seeyon.v3x.edoc.webmodel.EdocOpinionModel;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;
import com.seeyon.v3x.system.signet.domain.V3xHtmDocumentSignature;
import com.seeyon.v3x.system.signet.enums.V3xHtmSignatureEnum;
import com.seeyon.v3x.system.signet.manager.V3xHtmDocumentSignatManager;

public class FormOptionSortManagerImpl implements FormOptionSortManager{
	public FormOptionSortDao formOptionSortDao;
	private final static Log log = LogFactory.getLog(FormOptionSortManagerImpl.class);
	public EdocOpinionDao edocOpinionDao;
	private V3xHtmDocumentSignatManager htmSignetManager;
	
	public V3xHtmDocumentSignatManager getHtmSignetManager() {
		return htmSignetManager;
	}
	public void setHtmSignetManager(V3xHtmDocumentSignatManager htmSignetManager) {
		this.htmSignetManager = htmSignetManager;
	}
	public EdocOpinionDao getEdocOpinionDao() {
		return edocOpinionDao;
	}
	public void setEdocOpinionDao(EdocOpinionDao edocOpinionDao) {
		this.edocOpinionDao = edocOpinionDao;
	}
	
	public FormOptionSortDao getFormOptionSortDao() {
		return formOptionSortDao;
	}
	public void setFormOptionSortDao(FormOptionSortDao formOptionSortDao) {
		this.formOptionSortDao = formOptionSortDao;
	}
	public List<FormOptionSort> findBoundByFormId(long formId, String processName)throws Exception{
		  
		  return  formOptionSortDao.findByFormIdAndProcessName(formId,processName);	 
		  
	}
	@Override
	public List<FormOptionSort> findBoundByFormId(long formId,
			String processName, long accountId) {
		return  formOptionSortDao.findByFormIdAndProcessNameAndAccount(formId,processName,accountId);	
	}
	@Override
	public void saveOrUpdateList(List<FormOptionSort> list) {
		formOptionSortDao.saveOrUpdateList(list);
		
	}
	@Override
	public Map<String, EdocOpinionModel> getGovdocOpinion(long formId,
			EdocSummary colSummary, FormOpinionConfig displayConfig) {
		Map<String,Object> map = getEdocOpinionObjectList(colSummary, displayConfig);
    	Hashtable<String, String> locHs = (Hashtable<String, String>)map.get("locHs");
    	SharedWithThreadLocal.setLocHs(locHs);
    	List<EdocOpinion> list = (List<EdocOpinion>)map.get("edocOpinionList");
    	return getEdocOpinion(formId,list,colSummary,locHs);
	}
	
	//根据表单id和单位id查对应公文元素的排序方式
	public Hashtable<String,String> getOpinionLocation(Long formId,Long accountId)
	{
		Hashtable<String, String> hs=new Hashtable<String,String>();
		List <FormOptionSort> ls=formOptionSortDao.findByFormId(formId);
		for(FormOptionSort eb:ls)
		{
			// hs.put(eb.getFlowPermName(),eb.getProcessName());
			// 公文元素名称为value_公文元素的排序方式
		    if(eb.getProcessName() != null){
		        hs.put(eb.getProcessName(), eb.getProcessName() + "_"
	                    + eb.getSortType());
		    }
		}
		return hs;
	}
	private Map<String,Object> getEdocOpinionObjectList(EdocSummary summary, FormOpinionConfig displayConfig) {
		Map<String,Object> edocOpinionMap = new HashMap<String,Object>();
		long flowPermAccout = AppContext.currentAccountId(); 
		Hashtable<String, String> locHs = getOpinionLocation(summary.getFormAppid(),flowPermAccout);
		edocOpinionMap.put("locHs", locHs); 
		AttachmentManager attachmentManager = (AttachmentManager)AppContext.getBean("attachmentManager");
		List <Attachment> tempAtts = attachmentManager.getByReference(summary.getId());
		Hashtable <Long,List<Attachment>> attHas = com.seeyon.ctp.common.filemanager.manager.Util.sortBySubreference(tempAtts);
	    
		Long formAccountId = summary.getOrgAccountId();
		//查询当前表单所有排序 
		List<FormOptionSort> bList = formOptionSortDao.findByFormId(summary.getFormAppid());
		Set<String> eList = new HashSet<String>();//当前表单所有公文元素
		for(FormOptionSort b : bList){
		    eList.add(b.getProcessName());
		}
		List<EdocOpinion> tempResult = new ArrayList<EdocOpinion>();   //查询出来的意见
		List<String> 	boundFlowPerm =new ArrayList<String>();   //绑定的节点权限
		//Map<意见元素名称，List<绑定的节点权限>>因为一个意见元素可以绑定多个节点权限
		Map<String,List<String>> map = new HashMap<String,List<String>>();  
		Map<String,String> sortMap = new HashMap<String,String>();
		//绑定部分的意见
		for (Iterator keyName = locHs.keySet().iterator(); keyName.hasNext();) {
			String flowPermName = (String) keyName.next();
			if(!boundFlowPerm.contains(flowPermName))boundFlowPerm.add(flowPermName);
			// tempLoacl格式 公文元素名称为value_公文元素的排序方式
			String tempLocal = locHs.get(flowPermName);
			String elementOpinion = tempLocal.split("_")[0];//公文元素名,例如公文单上的shenpi这个公文元素
			//取到指定公文元素绑定的节点权限列表
			List<String> flowPermsOfSpecialElement = map.get(elementOpinion);
			if(flowPermsOfSpecialElement == null){
				flowPermsOfSpecialElement = new ArrayList<String>();
			}
			flowPermsOfSpecialElement.add(flowPermName);
			
			for(String e:eList){
				if(com.seeyon.ctp.util.Strings.isNotBlank(e)){
					if(e.equals(elementOpinion)){
						map.put(elementOpinion,flowPermsOfSpecialElement);
						break;
					}
				}
			}
			String sortType = tempLocal.split("_")[1];
			sortMap.put(elementOpinion,sortType);
		}
				
		Set<String> bound = map.keySet(); //绑定的公文元素
		if(eList.size()>0){
			for(String s:bound){
				boolean isShowLast = OpinionDisplaySetEnum.DISPLAY_LAST.getValue().equals(displayConfig.getOpinionType());
				if(s.equals(EdocOpinion.REPORT)){
					isShowLast = false;
				}
				List<EdocOpinion> opinions = edocOpinionDao.findLastSortOpinionBySummaryIdAndPolicy(summary.getId(), map.get(s), sortMap.get(s),isShowLast ,true,displayConfig.getOpinionType());
				if(isShowLast){
					Iterator<EdocOpinion> iter = opinions.iterator();
					while(iter.hasNext()){
						EdocOpinion edocOpinion = iter.next();
						if(!edocOpinion.getPolicy().equals(map.get(s).get(0))){
							iter.remove();
						}
					}
				}
				tempResult.addAll(opinions);
			}
		}
		
		//未绑定的意见全部显示
		List<EdocOpinion> opinions = edocOpinionDao.findLastSortOpinionBySummaryIdAndPolicy(summary.getId(), boundFlowPerm, "0", false,false,displayConfig.getOpinionType());
		//将下级汇报意见取出来
		List<EdocOpinion> feedbacks = new ArrayList<EdocOpinion>();
		List<EdocOpinion> others = new ArrayList<EdocOpinion>();
		
		//查询文单是否包含其他意见元素
		boolean hasOtherOpinion = false;
		List<EdocElement> elementList;
		/*    cx 修改其他意见
        try {
            elementList = edocFormManager.getEdocFormElementByFormIdAndFieldName(summary.getFormId(), "otherOpinion");
            if(Strings.isNotEmpty(elementList)) {
                hasOtherOpinion = true;
            }
        } catch (Exception e) {
        	log.error("", e);
        }
        */
		for(EdocOpinion op : opinions){
			if(EdocOpinion.FEED_BACK.equals(op.getPolicy())){
				feedbacks.add(op);
			}else{
				others.add(op);
			}
			op.setBound(hasOtherOpinion || false);//非绑定意见
		}
		List<EdocOpinion> opinions2 = new ArrayList<EdocOpinion>();
		if(com.seeyon.ctp.util.Strings.isNotEmpty(others)){
			opinions2.addAll(others);
		}
		if(com.seeyon.ctp.util.Strings.isNotEmpty(feedbacks)){
			feedbacks = GovdocHelper.getFeedBackOptions(feedbacks);
			opinions2.addAll(feedbacks);
		}
		
		tempResult.addAll(opinions2);
		List<EdocOpinion> edocOpionionList = new ArrayList<EdocOpinion>();
		for(EdocOpinion edocOpinion: tempResult){
			edocOpionionList.add(edocOpinion);
			edocOpinion.setOpinionAttachments(attHas.get(edocOpinion.getId()));
		}
		edocOpinionMap.put("edocOpinionList", edocOpionionList);
		return edocOpinionMap;
	}
	private Map<String ,V3xHtmDocumentSignature> getEdocOpinionSignatureMap(Long summaryId) {
		//查找印章数据
 	    List<V3xHtmDocumentSignature> ls = htmSignetManager.findBySummaryIdAndType(summaryId, V3xHtmSignatureEnum.HTML_SIGNATURE_DOCUMENT.getKey());
		Map<String ,V3xHtmDocumentSignature> signMap = new HashMap <String ,V3xHtmDocumentSignature>();
		for(V3xHtmDocumentSignature htmlSign : ls){
			String fieldName = htmlSign.getFieldName();
			if(com.seeyon.ctp.util.Strings.isNotBlank(fieldName)){
				String[] name =fieldName.split("hw");
				if(name.length>1){
					signMap.put(name[1],htmlSign);
				}
			}
		}
		return signMap;
	}
	public Map<String, EdocOpinionModel> getEdocOpinion(long formId ,List<EdocOpinion> edocOpinions, EdocSummary summary, Map<String,String> opinionLocation){
    	Long summaryId = summary.getId();
		Map<String, EdocOpinionModel> map = new HashMap<String, EdocOpinionModel>();
		Map<String,V3xHtmDocumentSignature> signMap = getEdocOpinionSignatureMap(summaryId);
		//拟文或者登记意见是否被绑定到意见框中显示了。
		for(EdocOpinion edocOpinion : edocOpinions){
			//TODO 标准产品需要澄清功能   文单不显示回退意见
			if (edocOpinion.getOpinionType() == EdocOpinion.OpinionType.backOpinion.ordinal()) {
				continue;
			}
			//节点权限
			String policy = edocOpinion.getPolicy();
			if (policy == null) {
				policy = summary.getGovdocType() == 2 ? "dengji" : "niwen";
			}
			//公文元素_排序方式
			String location = opinionLocation.get(policy);
			
			//没有设置意见放置位置的，统一放到其它意见，不再按节点权限放置到前台匹配;发起人附言单独处理,如果没有设置绑定就不放入公文单
			if(com.seeyon.ctp.util.Strings.isBlank(location)){
				if(policy.startsWith("field")){
					location = policy;
				}else if("niwen".equals(policy) || "dengji".equals(policy) || edocOpinion.getOpinionType() == EdocOpinion.OpinionType.senderOpinion.ordinal())	{
					//发起人附言
					location = "senderOpinion";
				}else if(policy.equals(EdocOpinion.FEED_BACK)){
					location=EdocOpinion.FEED_BACK;
				}
				else{
					//其他意见。
					location="otherOpinion";
				}
			}else{
				location = location.split("[_]")[0];
			}
			
			List<FormOptionSort> lists = formOptionSortDao.findByFormIdAndFlowPermName(formId, location);
			if(null!=lists&&lists.size()>0){
				location = lists.get(0).getProcessName();
			}
			EdocOpinionModel model = map.get(location);
			if(model == null){
				model = new EdocOpinionModel();
			}
			if(model.getOpinions() == null){
				model.setOpinions(new ArrayList<EdocOpinion>()); 
			}
			model.getOpinions().add(edocOpinion);
			//签章加载到意见框       
			V3xHtmDocumentSignature v3xHtmDocumentSignature = signMap.get(location);
			List<V3xHtmDocumentSignature> signList = new ArrayList<V3xHtmDocumentSignature>();
			signList.add(v3xHtmDocumentSignature);
			model.setV3xHtmDocumentSignature(signList);
			map.put(location, model);
		}
		return map;
	}
	@Override
	public List<FormOptionSort> findByFormId(Long id) {
		return formOptionSortDao.findByFormId(id);
	}
	@Override
	public Map<String, String> getOpinionLocation(Long formAppid) {
		Hashtable<String, String> hs=new Hashtable<String,String>();
		List <FormOptionSort> ls=formOptionSortDao.findByFormId(formAppid);
		for(FormOptionSort eb:ls)
		{ 
			// hs.put(eb.getFlowPermName(),eb.getProcessName());
			// 公文元素名称为value_公文元素的排序方式
		    if(eb.getProcessName() != null){
		        hs.put(eb.getProcessName(), eb.getProcessName() + "_"
	                    + eb.getSortType());
		    }
		}
		return hs;
	}
	public String getDisOpsition(Long formAppid,EdocSummary summary,CtpAffair affair) throws BusinessException{
		String a = "";
		List <FormOptionSort> ls=formOptionSortDao.findByFormId(formAppid);
		FormApi4Cap3 formApi4Cap3 = (FormApi4Cap3) AppContext.getBean("formApi4Cap3");;
		FormBean fb = formApi4Cap3.getForm(formAppid);
		String item = "";
		if (summary.getGovdocType() == ApplicationSubCategoryEnum.edoc_fawen.getKey()) {
			item = EnumNameEnum.edoc_new_send_permission_policy.name();
		} else if (summary.getGovdocType() == ApplicationSubCategoryEnum.edoc_shouwen.getKey()) {
			item = EnumNameEnum.edoc_new_rec_permission_policy.name();
		}else if(summary.getGovdocType() == ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()){
			item = EnumNameEnum.edoc_new_change_permission_policy.name();
		}else if(summary.getGovdocType() == ApplicationSubCategoryEnum.edoc_qianbao.getKey()){
			item = EnumNameEnum.edoc_new_qianbao_permission_policy.name();
		}
		PermissionManager permissionManager = (PermissionManager)AppContext.getBean("permissionManager");
		FormPermissionConfig f = formApi4Cap3.getConfigByFormId(fb.getId());
        Map<String,String> map = JSONUtil.parseJSONString(f.getConfig())==null?null:(Map<String, String>) JSONUtil.parseJSONString(f.getConfig());
        Permission permission = permissionManager.getPermission(item, affair.getNodePolicy(), AppContext.currentAccountId());
        String s = map != null ? map.get(permission.getFlowPermId().toString()) : "" ;
        String[] viewAndRight = s.split("\\.");
        String operationId = viewAndRight[1];//操作权限id
		List<FormAuthViewFieldBean> formAuthViewFieldBeans = new ArrayList<FormAuthViewFieldBean>();
        for (FormViewBean formViewBean : fb.getAllViewList()) {
			for (FormAuthViewBean formAuthViewBean : formViewBean.getAllOperations()) {
				if(formAuthViewBean.getId()==Long.parseLong(operationId)){
					for (FormAuthViewFieldBean favfb : formAuthViewBean.getFormAuthorizationFieldList()) {
						if(favfb.getAccess().equals("edit")){//如果可以编辑则保存
							formAuthViewFieldBeans.add(favfb);
						}
					}
				}
			}
		}
		if(null!=ls){
        	for (FormOptionSort govdocFormOpinionSort : ls) {
        		for (FormAuthViewFieldBean favfb : formAuthViewFieldBeans) {
					if(favfb.getFieldName().equals(govdocFormOpinionSort.getProcessName())){
						a += govdocFormOpinionSort.getProcessName()+",";
					}
        		}
        	}
		}
		if(a.endsWith(",")){
			a = a.substring(0,a.length()-1);
		} 
		return a;
	}
	@Override
	public List<String> getOpinionElementLocationNames(Long formAppid) {
		List<String> hs=new ArrayList<String>();
		List <FormOptionSort> ls=formOptionSortDao.findByFormId(formAppid);
		for(FormOptionSort eb:ls)
		{
			// hs.put(eb.getFlowPermName(),eb.getProcessName());
			// 公文元素名称为value_公文元素的排序方式
		    if(eb.getProcessName() != null){
		    	hs.add(eb.getProcessName());
		    }
		}
		return hs;
	}
	@Override
	public void deleteByFormId(Long formId){
			formOptionSortDao.deleteByFormId(formId);
	}
}
