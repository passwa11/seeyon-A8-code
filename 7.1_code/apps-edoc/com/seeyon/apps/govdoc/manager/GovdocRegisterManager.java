package com.seeyon.apps.govdoc.manager;

import com.seeyon.apps.govdoc.po.GovdocExchangeDetail;
import com.seeyon.v3x.edoc.domain.EdocSummary;

/**
 * 新公文收文登记接口
 * @author 唐桂林
 *
 */
public interface GovdocRegisterManager {

	/**
	 * 保存收文登记簿数据(电子登记)
	 * @param detail
	 */
	public void saveByDetail(GovdocExchangeDetail detail);
	
	/**
	 * 保存收文登记簿数据(纸质登记)
	 * @param summaryId
	 */
	public void saveBySummary(EdocSummary summary);
		
	/**
	 * 撤销收文登记簿数据
	 * @param summaryId
	 */
	public void saveCancelRegister(EdocSummary summary);
	
}
