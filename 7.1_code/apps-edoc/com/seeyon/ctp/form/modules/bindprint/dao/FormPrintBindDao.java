package com.seeyon.ctp.form.modules.bindprint.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.form.po.FromPrintBind;

/**
 * @author Administrator
 * @desc 打印单模板
 * @Date 2015-10-13上午10:10:25
 */
public class FormPrintBindDao extends BaseHibernateDao<FromPrintBind> {
	
	/**
	 * desc 新增或修改打印单对象
	 * @param insertObj 打印单模板对象
	 */
	public void saveOrUpdateEdocPrintMode(FromPrintBind insertObj){
		FromPrintBind findObj = null;
		if(insertObj!=null){
			long unitId = insertObj.getUnitId();
			long edocXsnId = insertObj.getEdocXsnId();
			if(unitId!=0 && edocXsnId!=0){
				findObj = findEdocPrintModeById(unitId,edocXsnId);
			}
			if(findObj==null){
				this.save(insertObj);
			}else{
				findObj.setFileCreateTime(insertObj.getFileCreateTime());
				findObj.setFileName(insertObj.getFileName());
				findObj.setFileUrl(insertObj.getFileUrl());
				this.update(findObj);
			}
		}
	};
	
	/**
	 * desc 根据单位Id和公文单Id查询打印单模板
	 * @param unitId 单位Id
	 * @param edocXsnId 公文单Id
	 * @return
	 */
	public FromPrintBind findEdocPrintModeById(long unitId,long edocXsnId){
		FromPrintBind formPrintBind = null;
		if(unitId==0 || edocXsnId==0) 
			return null;
		String hql = "from FromPrintBind where unitId = :unitId and edocXsnId=:edocXsnId";
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("unitId", unitId);
		param.put("edocXsnId", edocXsnId);
		List<FromPrintBind> list=super.find(hql, param);
		if(list!=null&&!list.isEmpty())
			formPrintBind = list.get(0);
		
		return formPrintBind;
		
	};
	
	/**
	 * desc 删除打印单模板
	 * @param unitId 单位Id
	 * @param edocXsnId 公文单Id
	 */
	public void deleteEdocLPrintModeById(long unitId,long edocXsnId){
		if(unitId!=0 && edocXsnId!=0){
			FromPrintBind findObj = findEdocPrintModeById(unitId,edocXsnId);
			if(findObj!=null)
			this.deleteObject(findObj);
		}
		
	};

}
