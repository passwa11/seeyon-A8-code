package com.seeyon.ctp.form.manager;

import java.util.List;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.form.po.GovdocTemplateDepAuth;

public interface GovdocTemplateDepAuthManager {
	List<GovdocTemplateDepAuth> findByTemplateId(long templateId);
	
	void saveAll(List<GovdocTemplateDepAuth> list);

	void deleteGovdocTemplateDepAuth(Long id, int authTypeExchange);
	
	public String validateTemplateDepAuth(String id,String depAuthSet) throws BusinessException;
	
	void deleteGovdocTemplateDepAuthBytIds(String ids, int authTypeExchange) throws BusinessException;

	GovdocTemplateDepAuth findExchangeByOrgId(Long orgId);
	
	List<GovdocTemplateDepAuth> findByOrgIdAndAccountId4Lianhe(long orgId,long accountId);

	/**
	 * 保存为当前单位的联合发文模板
	 * @param templateId
	 * @return
	 */
	int saveAll4Lianhe(long templateId);
	
	void saveAll4Lianhe(GovdocTemplateDepAuth govdocTemplateDepAuth);
	
	/**
	 * 验证是否存在联合发文模板
	 * @param accountIds
	 * @return
	 */
	String validateAuthTemplate(Long[] accountIds);
	
	/**
	 * 删除当前单位的联合发文配置信息
	 * @return
	 */
	public int deleteCurrentLianhe();
}
