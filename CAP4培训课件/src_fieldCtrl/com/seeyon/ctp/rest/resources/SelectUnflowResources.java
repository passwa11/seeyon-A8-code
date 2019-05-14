package com.seeyon.ctp.rest.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormBindAuthBean;
import com.seeyon.cap4.form.bean.FormBindBean;
import com.seeyon.cap4.form.service.CAP4FormCacheManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.template.CtpTemplateCategory;
import com.seeyon.ctp.common.template.manager.TemplateManager;

@Path("cap4/selectUnflow")
@Consumes({ "application/json" })
@Produces({ "application/json" })
public class SelectUnflowResources extends BaseResource {

	private static CAP4FormCacheManager cap4FormCacheManager = (CAP4FormCacheManager) AppContext
			.getBean("cap4FormCacheManager");

	private static TemplateManager templateManager = (TemplateManager) AppContext.getBean("templateManager");

	@GET
	@Produces({ "application/json" })
	@Path("select")
	public Response select(@QueryParam("designType") String designType) throws BusinessException {
        Map<String, CtpTemplateCategory> catgMap = new HashMap<String, CtpTemplateCategory>();
        List<Map<String, Object>> unflowList = new ArrayList<>();
		List<FormBean> formList = cap4FormCacheManager.getFormList();
		for (FormBean fbean : formList) {
			CtpTemplateCategory currentTemplate = templateManager.getCtpTemplateCategory(fbean.getCategoryId());
			FormBindBean bindBean = fbean.getBind();
			Map<String, FormBindAuthBean> templates = bindBean.getUnFlowTemplateMap();
			for (Entry<String, FormBindAuthBean> entry : templates.entrySet()) {
				FormBindAuthBean template = entry.getValue();
				if (template.checkRight(AppContext.currentUserId())) {
					catgMap.put(currentTemplate.getName(), currentTemplate);
					String templateName = template.getName();
					Map<String, Object> unflow = new HashMap<String, Object>();
					unflow.put("name", templateName);
					unflow.put("id", template.getId().toString());
					unflow.put("parentId", currentTemplate.getName());
					unflow.put("icon", "collaboration");
					unflow.put("formId", fbean.getId().toString());
					unflowList.add(unflow);
				}
			}
		}
		return success(unflowList);

	}
}
