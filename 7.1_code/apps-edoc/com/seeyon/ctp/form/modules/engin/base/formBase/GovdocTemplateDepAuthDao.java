package com.seeyon.ctp.form.modules.engin.base.formBase;

import java.util.List;

import com.seeyon.ctp.form.po.GovdocTemplateDepAuth;

public interface GovdocTemplateDepAuthDao {
	List<GovdocTemplateDepAuth> findByTemplateId(long templateId);
	
	void saveAll(List<GovdocTemplateDepAuth> list);

	List<GovdocTemplateDepAuth> findByTemplateIdAndOrgId(Long templateId,
			Long orgId);

	void deleteByTemplateIdAndOrgId(Long templateId, Long orgId);
	
	/**
	 * 
	 * @param authType
	 * @return
	 */
	public List<GovdocTemplateDepAuth> findByAuthType(int authType);
	
	void deleteByTemplateAndAuthType(Long id, int authTypeExchange);

	GovdocTemplateDepAuth findExchangeByOrgId(long orgId);

	List<GovdocTemplateDepAuth> findByOrgId(List<Long> orgIds);

	void deleteByOrgIdAndTypeId(Long orgId, int authType);
	
	List<GovdocTemplateDepAuth> findByOrgIdAndAccountId(long orgId,
			long accountId,int authType);

	void deleteByAccountIdAndTypeId(Long orgId, int authTypeLianhe);
}
