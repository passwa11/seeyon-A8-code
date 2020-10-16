package com.seeyon.ctp.form.manager;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.form.po.GovdocFormOpinionSort;

public interface GovdocFormOpinionSortManager {

	public List<GovdocFormOpinionSort> findByFormId(Long id);

	void deleteByFormId(Long formId);

	void saveOrUpdateList(List<GovdocFormOpinionSort> list);

}
