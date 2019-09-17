package com.seeyon.ctp.form.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.edoc.bo.GovdocTemplateDepAuthBO;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.modules.engin.base.formBase.GovdocTemplateDepAuthDao;
import com.seeyon.ctp.form.po.GovdocTemplateDepAuth;
import com.seeyon.ctp.form.util.Enums.FormType;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.CommonTools;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.ctp.util.json.JSONUtil;

import edu.emory.mathcs.backport.java.util.Arrays;

public class GovdocTemplateDepAuthManagerImpl implements GovdocTemplateDepAuthManager{
	
	private GovdocTemplateDepAuthDao govdocTemplateDepAuthDao;
	private OrgManager orgManager;
	private FormApi4Cap3 formApi4Cap3;
	private TemplateManager templateManager;
	private static final Log LOGGER = LogFactory.getLog(GovdocTemplateDepAuthManagerImpl.class);
	
	public TemplateManager getTemplateManager() {
		return templateManager;
	}

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }

	@Override
	public List<GovdocTemplateDepAuth> findByTemplateId(long templateId) {
		return  govdocTemplateDepAuthDao.findByTemplateId(templateId);
	}

	@Override
	public void saveAll(List<GovdocTemplateDepAuth> list) {
		
		for (GovdocTemplateDepAuth govdocTemplateDepAuth : list) {
			govdocTemplateDepAuthDao.deleteByOrgIdAndTypeId(govdocTemplateDepAuth.getOrgId(),GovdocTemplateDepAuth.AUTH_TYPE_EXCHANGE);
		}
		govdocTemplateDepAuthDao.saveAll(list);
	}

	public GovdocTemplateDepAuthDao getGovdocTemplateDepAuthDao() {
		return govdocTemplateDepAuthDao;
	}

	public void setGovdocTemplateDepAuthDao(GovdocTemplateDepAuthDao govdocTemplateDepAuthDao) {
		this.govdocTemplateDepAuthDao = govdocTemplateDepAuthDao;
	}

	public OrgManager getOrgManager() {
		return orgManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	@Override
	public void deleteGovdocTemplateDepAuth(Long id, int authTypeExchange) {
		govdocTemplateDepAuthDao.deleteByTemplateAndAuthType(id,authTypeExchange);
	}
	
	
	@Override
	@AjaxAccess
	public void deleteGovdocTemplateDepAuthBytIds(String ids, int authTypeExchange) throws BusinessException {
		List<Long> idList = CommonTools.parseStr2Ids(ids);
		List<CtpTemplate> templateList = new ArrayList<CtpTemplate>();
		for (Long id : idList) {
			templateList.addAll(formApi4Cap3.getFormSystemTemplate(id));
		}
		for (CtpTemplate ctpTemplate : templateList) {
			govdocTemplateDepAuthDao.deleteByTemplateAndAuthType(ctpTemplate.getId(),authTypeExchange);
		}
	}
	/**
	 * 验证授权部门是否已经存在
	 * @param id  模板id
	 * @param saveOrUpdateValidate   修改之前的授权单位/部门id
	 * @param depAuthSet	修改之后的授权单位/部门id
	 * @return
	 * @throws BusinessException
	 */
	@Override
	@AjaxAccess
	public String validateTemplateDepAuth(String id,String depAuthSet) throws BusinessException{
		if (Strings.isNotBlank(depAuthSet) && depAuthSet.indexOf(OrgConstants.GROUPID.toString()) > 0) {
			return "depAuthSetIsOrg";
		}
		FormBean fb = formApi4Cap3.getEditingForm();
		List<String> exitsOrgName = new ArrayList<String>();
		List<Long> exitsOrgId = new ArrayList<Long>();
		//List<GovdocTemplateDepAuth> dupList = new ArrayList<GovdocTemplateDepAuth>();//重复
		if(fb.getGovDocFormType()==FormType.govDocExchangeForm.getKey()){
			if(Strings.isNotBlank(depAuthSet)){
				String[] ids = depAuthSet.split(",");
				List<Long> newIds = new ArrayList<Long>();
				for(String dId:ids){
					if(Strings.isNotBlank(dId)){
						newIds.add(Long.parseLong(dId.split("\\|")[1]));
					}
				}
				List<CtpTemplate> templates = fb.getBind().getFlowTemplateList();
				for (CtpTemplate ctpTemplate : templates) {
					Object obj = ctpTemplate.getExtraAttr("depAuthList");
					if(null!=obj && !ctpTemplate.getId().toString().equals(id)){
						List<Object> depAuthList = (List<Object>)obj;
						for (Object temp : depAuthList) {
							GovdocTemplateDepAuth govdocTemplateDepAuth = null;
							if(temp instanceof GovdocTemplateDepAuthBO){
								govdocTemplateDepAuth = GovdocUtil.toGovdocTemplateDepAuth((GovdocTemplateDepAuthBO)temp);
							}else{
								govdocTemplateDepAuth = (GovdocTemplateDepAuth)temp;
							}
							for(Long dId:newIds){
								if(dId.toString().equals(govdocTemplateDepAuth.getOrgId().toString())){
									if(!exitsOrgId.contains(dId)){
										exitsOrgId.add(dId);
										exitsOrgName.add(govdocTemplateDepAuth.getOrgName());
									}
								}
							}
						}
					}
				} 
				//数据库中的模板
				if (newIds.size() > 0) {
					List<GovdocTemplateDepAuth> gds = govdocTemplateDepAuthDao.findByOrgId(newIds);
					if(null!=gds && !gds.isEmpty()){
						for (GovdocTemplateDepAuth govdocTemplateDepAuth : gds) {
							for(Long dId:newIds){
								if(dId.toString().equals(govdocTemplateDepAuth.getOrgId().toString()) && !id.equals(govdocTemplateDepAuth.getTemplateId().toString())){
									if(!exitsOrgId.contains(dId)){
										exitsOrgId.add(dId);
										exitsOrgName.add(govdocTemplateDepAuth.getOrgName());
									}
								}
							}
						}
					}
				}
				if(exitsOrgName.size()>0){
					Map<String, String> result = new HashMap<String, String>();
					result.put("existOrg", Strings.join(exitsOrgName, "，"));
					//result.put("existAuthId", String.join(",", exitsOrgId));
					return JSONUtil.toJSONString(result);
				}
			}
		}
		return "";
	}
	@Override
	public GovdocTemplateDepAuth findExchangeByOrgId(Long orgId) {
		return govdocTemplateDepAuthDao.findExchangeByOrgId(orgId);
	}
	
	@Override
	public List<GovdocTemplateDepAuth> findByOrgIdAndAccountId4Lianhe(
			long orgId, long accountId) {
		List<GovdocTemplateDepAuth> authList = govdocTemplateDepAuthDao.findByOrgIdAndAccountId(orgId,accountId,GovdocTemplateDepAuth.AUTH_TYPE_LIANHE);
		List<GovdocTemplateDepAuth> result = new ArrayList<GovdocTemplateDepAuth>();
		if(authList!=null && authList.size()>0){
			for(GovdocTemplateDepAuth auth:authList){
				try {
					CtpTemplate ctpTemplate = templateManager.getCtpTemplate(auth.getTemplateId());
					if(ctpTemplate!=null&&!ctpTemplate.isDelete()&&ctpTemplate.getState() == 0){
						result.add(auth);
					}
				} catch (BusinessException e) {
					LOGGER.error(e);
				}
			}
		}
		return result;
	}
	
	@Override
	@AjaxAccess
	public int saveAll4Lianhe(long templateId) {
		GovdocTemplateDepAuth auth = new GovdocTemplateDepAuth();
		auth.setNewId();
		auth.setAuthType(GovdocTemplateDepAuth.AUTH_TYPE_LIANHE);
		auth.setOrgId(AppContext.currentAccountId());
		auth.setOrgType("Account");
		auth.setTemplateId(templateId);
		saveAll4Lianhe(auth);
		return 1;
	}
	
	@Override
	public void saveAll4Lianhe(GovdocTemplateDepAuth govdocTemplateDepAuth) {
		if(govdocTemplateDepAuth!=null){
			govdocTemplateDepAuthDao.deleteByAccountIdAndTypeId(govdocTemplateDepAuth.getOrgId(),GovdocTemplateDepAuth.AUTH_TYPE_LIANHE);
			govdocTemplateDepAuthDao.saveAll(Arrays.asList(new GovdocTemplateDepAuth[]{govdocTemplateDepAuth}));
		}
		
	}
	
	@Override
	@AjaxAccess
	public String validateAuthTemplate(Long[] accountIds) {
		StringBuilder sb = new StringBuilder("");
		if(accountIds!=null&&accountIds.length>0){
			for(long id:accountIds){
				List<GovdocTemplateDepAuth> result = findByOrgIdAndAccountId4Lianhe(id,id);
				if(result==null||result.size()<=0){
					try {
						V3xOrgAccount account = orgManager.getAccountById(id);
						sb.append(account.getName()).append("，");
					} catch (BusinessException e) {
						LOGGER.error(e);
					}
				}
			}
		}
		return Strings.isBlank(sb.toString())?"":sb.substring(0, sb.length()-1);
	}
	
	@Override
	@AjaxAccess
	public int deleteCurrentLianhe() {
		govdocTemplateDepAuthDao.deleteByAccountIdAndTypeId(AppContext.currentAccountId(),GovdocTemplateDepAuth.AUTH_TYPE_LIANHE);
		return 1;
	}
}
