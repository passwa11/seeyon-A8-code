package com.seeyon.ctp.form.modules.bindprint.manager;

import com.seeyon.ctp.form.po.FromPrintBind;

public interface FormPrintBindManager {
	/**
	 * desc 新增或修改打印单对象
	 * @param printObj
	 */
	public void saveOrUpdatePrintMode(FromPrintBind printObj);
	/**
	 * desc 根据单位Id和公文单Id查询打印模板
	 * @param unitId 单位Id
	 * @param edocXsnId 公文单Id
	 * @return
	 */
	public FromPrintBind findPrintMode(long unitId,long edocXsnId);
	/**
	 * desc 删除打印模板
	 * @param unitId 单位Id
	 * @param edocXsnId 公文单Id
	 */
	public void deletePrintMode(long unitId,long edocXsnId);
    
}
