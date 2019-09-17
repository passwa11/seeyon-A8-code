package com.seeyon.v3x.edoc.manager;

import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public class EdocProcessInsExtendImpl {

	public ApplicationCategoryEnum getAppEnum() throws BusinessException {
		return ApplicationCategoryEnum.edoc;
	}
	
	public Long getDeadlineNumber(String summaryXMl) {
		EdocSummary summary = (EdocSummary)XMLCoder.decoder(summaryXMl);
		return summary.getDeadline();
	}

}
