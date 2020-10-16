package com.seeyon.apps.govdoc.exchange.util;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.manager.GovdocFormExtendManager;
import com.seeyon.ctp.form.manager.GovdocFormOpinionSortManager;
import com.seeyon.ctp.form.po.GovdocFormExtend;
import com.seeyon.ctp.form.po.GovdocFormOpinionSort;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;

public class GovDocUtil {


	private static final Logger  LOGGER     = Logger.getLogger(GovDocUtil.class);


	public static void saveGovdocExtendAndSort(FormBean fb,GovdocFormExtendManager govdocFormExtendManager,GovdocFormOpinionSortManager govdocFormOpinionSortManager){
		List<GovdocFormOpinionSort> govdocFormOpinionSorts = govdocFormOpinionSortManager.findByFormId(fb.getId());
        GovdocFormExtend govdocFormExtend = govdocFormExtendManager.findByFormId(fb.getId());
        if(govdocFormExtend==null){
        	govdocFormExtend = new GovdocFormExtend();
        	govdocFormExtend.setAccountId(AppContext.currentAccountId());
        	govdocFormExtend.setId(UUIDLong.longUUID());
        	govdocFormExtend.setFormId(fb.getId());
        	govdocFormExtend.setOptionFormatSet(FormOpinionConfig.getDefualtConfig());
        	govdocFormExtendManager.saveOrUpdate(govdocFormExtend);
        }
        if(null==govdocFormOpinionSorts||govdocFormOpinionSorts.size()<=0){
        	List<FormFieldBean> formFieldBeans = fb.getAllFieldBeans();
        	govdocFormOpinionSorts = new ArrayList<GovdocFormOpinionSort>();
            for (FormFieldBean formFieldBean : formFieldBeans) {
				if("edocflowdealoption".equals(formFieldBean.getInputType())){
					GovdocFormOpinionSort govdocFormOpinionSort = new GovdocFormOpinionSort();
					govdocFormOpinionSort.setId(UUIDLong.longUUID());
					govdocFormOpinionSort.setDomainId(AppContext.currentAccountId());
					govdocFormOpinionSort.setProcessName(formFieldBean.getName());
					govdocFormOpinionSort.setFormId(fb.getId());
					govdocFormOpinionSorts.add(govdocFormOpinionSort);
				}
			}
            govdocFormOpinionSortManager.saveOrUpdateList(govdocFormOpinionSorts);
        }else{
        	String fieldNum = "";
        	boolean isOldOpinionField = false;
        	for(int i=0;i<govdocFormOpinionSorts.size();i++){
        		fieldNum = fieldNum + govdocFormOpinionSorts.get(i).getProcessName()+",";
        	}
        	List<FormFieldBean> formFieldBeans = fb.getAllFieldBeans();
        	List<GovdocFormOpinionSort> govdocFormOpinionSorts2 = new ArrayList<GovdocFormOpinionSort>();
        	for (FormFieldBean formFieldBean : formFieldBeans) {
				if("edocflowdealoption".equals(formFieldBean.getInputType())){
					GovdocFormOpinionSort govdocFormOpinionSort1 = new GovdocFormOpinionSort();
					govdocFormOpinionSort1.setId(UUIDLong.longUUID());
					govdocFormOpinionSort1.setDomainId(AppContext.currentAccountId());
					govdocFormOpinionSort1.setProcessName(formFieldBean.getName());
					govdocFormOpinionSort1.setFormId(fb.getId());
					for(GovdocFormOpinionSort govdocFormOpinionSort:govdocFormOpinionSorts){
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
        		govdocFormOpinionSortManager.deleteByFormId(fb.getId());
        	}else{
        		govdocFormOpinionSortManager.saveOrUpdateList(govdocFormOpinionSorts2);	
        	}
        }
	}	
}
