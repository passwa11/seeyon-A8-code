package com.seeyon.ctp.form.modules.template;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.edoc.api.EdocApi;
import com.seeyon.apps.edoc.bo.GovdocTemplateBO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.form.manager.GovdocFormTemplateRelationManager;
import com.seeyon.ctp.form.po.GovdocFormTemplateRelation;
import com.seeyon.ctp.util.Strings;

/**
 * 文单模板绑定控制器
 * @author 
 *
 */
public class GovdocFormTemplateController extends BaseController {

	private static final Log LOGGER = CtpLogFactory.getLog(GovdocFormTemplateController.class);
	
	private GovdocFormTemplateRelationManager govdocFormTemplateRelationManager;
	private EdocApi edocApi;
	
	/**
	 * 获取文单套红页面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView taoHong(HttpServletRequest request,HttpServletResponse response)throws Exception{
		User user = AppContext.getCurrentUser();
		
		String edocType = request.getParameter("templateType");
		String bodyType = request.getParameter("bodyType");
		//公文所属单位
		String orgAccountId = request.getParameter("orgAccountId");
		String formId = request.getParameter("formId");
		ModelAndView mav = new ModelAndView("ctp/form/design/fieldDesign/govdocFormTemplate");	
		
		try {
			//获取所有模板
			List<GovdocTemplateBO> list = edocApi.getEdocDocTemplateList("true", Long.parseLong(orgAccountId),user,edocType,bodyType);
			if(null==list || list.size()==0){
				mav.addObject("haveRecord", true);
				return mav;
			}
			
			//获取当前绑定的模板名称
			GovdocFormTemplateRelation currChooseRelation = null;
			GovdocTemplateBO currEdocDocTemplate = null;
			if(Strings.isNotBlank(formId)){
				currChooseRelation = govdocFormTemplateRelationManager.findByFormId(Long.valueOf(formId));
			}
			if(currChooseRelation != null){
				for(int i=0;i<list.size();i++){
					if(list.get(i).getId().longValue() == currChooseRelation.getTemplateId().longValue()){
						currEdocDocTemplate = list.get(i);
						break;
					}
				}	
			}
			mav.addObject("currEdocDocTemplate", currEdocDocTemplate);
			mav.addObject("templateList", list);
			mav.addObject("formId", formId);
		} catch (Exception e) {
			LOGGER.error("公文文单设置文单套红绑定出错：" + e.getMessage());
		}
		return mav;
	}	

	public void setGovdocFormTemplateRelationManager(
			GovdocFormTemplateRelationManager govdocFormTemplateRelationManager) {
		this.govdocFormTemplateRelationManager = govdocFormTemplateRelationManager;
	}

	public void setEdocApi(EdocApi edocApi) {
		this.edocApi = edocApi;
	}
	
}
