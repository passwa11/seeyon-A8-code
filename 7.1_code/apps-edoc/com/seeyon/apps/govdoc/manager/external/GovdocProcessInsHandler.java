package com.seeyon.apps.govdoc.manager.external;

import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.template.manager.ProcessInsHandler;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public class GovdocProcessInsHandler extends ProcessInsHandler {
	
	@Override
	public ApplicationCategoryEnum getAppEnum() throws BusinessException {
		return ApplicationCategoryEnum.edoc;
	}
	
	@Override
	public Long getDeadlineNumber(String summaryXMl) {
		EdocSummary summary = (EdocSummary)XMLCoder.decoder(summaryXMl);
		return summary.getDeadline();
	}
}
