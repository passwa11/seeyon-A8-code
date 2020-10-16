package com.seeyon.apps.govdoc.mark.manager;

import com.seeyon.apps.govdoc.mark.vo.GovdocMarkVO;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.webmodel.EdocMarkReserveVO;

public interface GovdocMarkParseManager {

	public GovdocMarkVO markDef2Mode(GovdocMarkVO markVo, String yearNo, Integer curentno);
	public GovdocMarkVO markDef2Mode(EdocMarkDefinition markDef, String yearNo, Integer curentno);
	
	public EdocMarkReserveVO parseToReserveByFormat(GovdocMarkVO markVo, Integer markNumber);
	public EdocMarkReserveVO parseToReserveByFormat(GovdocMarkVO markVo, Integer reserveYearNo, Integer markNumber);

}
