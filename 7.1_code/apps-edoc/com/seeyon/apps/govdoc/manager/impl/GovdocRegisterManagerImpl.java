package com.seeyon.apps.govdoc.manager.impl;

import java.sql.Timestamp;

import org.apache.log4j.Logger;

import com.seeyon.apps.govdoc.constant.GovdocEnum.ExchangeDetailStatus;
import com.seeyon.apps.govdoc.dao.GovdocRegisterDao;
import com.seeyon.apps.govdoc.manager.GovdocRegisterManager;
import com.seeyon.apps.govdoc.po.GovdocExchangeDetail;
import com.seeyon.apps.govdoc.po.GovdocRegister;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocSummary;

/**
 * 新公文收文登记接口
 * @author 唐桂林
 *
 */
public class GovdocRegisterManagerImpl implements GovdocRegisterManager {

	private static final Logger  LOGGER     = Logger.getLogger(GovdocExchangeManagerImpl.class);
	
	private GovdocRegisterDao govdocRegisterDao;
	
	/**
	 * 保存收文登记簿数据(电子登记)
	 * @param detail
	 */
	@Override
	public void saveByDetail(GovdocExchangeDetail detail)  {
		try {
			if(detail == null 
					|| (detail.getSummaryId()==null && detail.getRecSummaryId()==null)) {
				return;
			}
			if(detail.getSummaryId() != null) {
				govdocRegisterDao.deleteBySummaryId(detail.getSummaryId());
			}
			if(detail.getRecSummaryId() != null) {
				govdocRegisterDao.deleteBySummaryId(detail.getRecSummaryId());
			}
			
			if(detail.getStatus() == ExchangeDetailStatus.hasSign.getKey() 
					|| detail.getStatus() == ExchangeDetailStatus.hasFenBan.getKey()
					|| detail.getStatus() == ExchangeDetailStatus.hasBack.getKey()
					|| detail.getStatus() == ExchangeDetailStatus.hasCancel.getKey()
					|| detail.getStatus() == ExchangeDetailStatus.draftFenBan.getKey()) {
				GovdocRegister po = new GovdocRegister();
				po.setNewId();
				po.setCreateTime(DateUtil.currentTimestamp());
				po.setRecUserName(detail.getRecUserName());
				po.setRecNo(detail.getRecNo());
				if(detail.getRecTime() != null) {
					po.setRecTime(new Timestamp(detail.getRecTime().getTime()));
				}
				if(detail.getStatus() == ExchangeDetailStatus.hasSign.getKey() && detail.getSummaryId() != null) {//签收
					po.setSummaryId(detail.getSummaryId());
					govdocRegisterDao.saveOrUpdate(po);
				} else if(detail.getStatus() == ExchangeDetailStatus.hasFenBan.getKey() && detail.getRecSummaryId() != null) {//分办
					po.setSummaryId(detail.getRecSummaryId());
					govdocRegisterDao.saveOrUpdate(po);
				} else if(detail.getStatus() == ExchangeDetailStatus.hasBack.getKey() && detail.getSummaryId() != null) {//分办撤销
					po.setSummaryId(detail.getSummaryId());
					govdocRegisterDao.saveOrUpdate(po);
				} else if((detail.getStatus() == ExchangeDetailStatus.draftFenBan.getKey() || detail.getStatus() == ExchangeDetailStatus.hasCancel.getKey()) && detail.getSummaryId() != null) {//分办撤销
					if(Strings.isNotBlank(detail.getRecUserName())){
						po.setSummaryId(detail.getSummaryId());
						govdocRegisterDao.saveOrUpdate(po);
					}
				}
			}			
		} catch(Exception e) {
			LOGGER.error("电子登记登记数据出错保存", e);
		}
	}
	
	/**
	 * 保存收文登记簿数据(老公文纸质登记)
	 * @param summaryId
	 */
	public void saveBySummary(EdocSummary summary) {
		try {
			//非收文数据不处理
			if(summary == null || summary.getId()==null) {
				return;
			}
			if(summary.getGovdocType()!=null && summary.getGovdocType().intValue()==0 && summary.getEdocType()!=1) {
				return;
			}
			if(summary.getGovdocType()!=null && (summary.getGovdocType().intValue()==1 || summary.getGovdocType().intValue()==3)) {
				return;
			}
			if(summary.getEdocType()!=1) {
				return;
			}
			govdocRegisterDao.deleteBySummaryId(summary.getId());
			
			GovdocRegister po = new GovdocRegister();
			po.setNewId();
			po.setCreateTime(DateUtil.currentTimestamp());
			po.setSummaryId(summary.getId());
			govdocRegisterDao.saveOrUpdate(po);
		} catch(Exception e) {
			LOGGER.error("老公文纸质登记登记数据出错保存", e);
		}
	}
	
	/**
	 * 撤销收文登记簿数据
	 * @param summaryId
	 */
	public void saveCancelRegister(EdocSummary summary) {
		try {
			//非收文数据不处理
			if(summary == null || summary.getId()==null) {
				return;
			}
			if(summary.getGovdocType()!=null && summary.getGovdocType().intValue()==0 && summary.getEdocType()!=1) {
				return;
			}
			if(summary.getGovdocType()!=null && (summary.getGovdocType().intValue()==1 || summary.getGovdocType().intValue()==3)) {
				return;
			}
			if(summary.getEdocType()!=1) {
				return;
			}
			govdocRegisterDao.deleteBySummaryId(summary.getId());	
		} catch(Exception e) {
			LOGGER.error("老公文纸质登记登记数据出错保存", e);
		}
	}
	
	public void setGovdocRegisterDao(GovdocRegisterDao govdocRegisterDao) {
		this.govdocRegisterDao = govdocRegisterDao;
	}

}
