package com.seeyon.v3x.edoc.manager;

import java.util.List;

import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.v3x.edoc.dao.EdocSummaryExtendDao;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.domain.EdocSummaryExtend;
import com.seeyon.v3x.edoc.webmodel.EdocSearchModel;

public class EdocSummaryExtendManagerImpl implements EdocSummaryExtendManager {

	private EdocSummaryExtendDao edocSummaryExtendDao;
	
	public List<EdocSummary> getAllEdocSummaryList() throws BusinessException  {
		return edocSummaryExtendDao.getAllEdocSummaryList();
	}
	
	public Integer getEdocSummaryExtendCount() throws BusinessException {
		return edocSummaryExtendDao.getEdocSummaryExtendCount();
	}
	
	public void saveEdocSummaryExtend(List<EdocSummaryExtend> summaryExtendList) throws BusinessException {
		edocSummaryExtendDao.saveEdocSummaryExtend(summaryExtendList);
	}
	
	public void saveEdocSummaryExtendBySummary(EdocSummary summary) throws BusinessException {
		if(summary != null) {
			boolean isNew = false;
			EdocSummaryExtend summaryExtend = getEdocSummaryExtend(summary.getId());
			if(summaryExtend == null) {
				isNew = true;
				summaryExtend = new EdocSummaryExtend();
				summaryExtend.setNewId();
			}
			summaryExtend = transSetSummaryToExtend(summary, summaryExtend);
			if(isNew) {
				edocSummaryExtendDao.saveEdocSummaryExtend(summaryExtend);
			} else {
				edocSummaryExtendDao.updateEdocSummaryExtend(summaryExtend);
			}
		}
	}
	
	public void saveEdocSummaryExtend(EdocSummaryExtend summaryExtend) throws BusinessException {
		edocSummaryExtendDao.saveEdocSummaryExtend(summaryExtend);
	}
	
	public void updateEdocSummaryExtend(EdocSummaryExtend summaryExtend) throws BusinessException {
		edocSummaryExtendDao.updateEdocSummaryExtend(summaryExtend);
	}

	public void deleteAllEdocSummaryExtend() throws BusinessException {
		edocSummaryExtendDao.deleteAllEdocSummaryExtend();
	}
	
	public EdocSummaryExtend getEdocSummaryExtend(Long summaryId) throws BusinessException {
		return edocSummaryExtendDao.getEdocSummaryExtend(summaryId);
	}
	
	public EdocSummary transSetSummaryExtendValue(EdocSummary summary) throws BusinessException {
		if(summary == null) {
			return null;
		}
		EdocSummaryExtend summaryExtend = getEdocSummaryExtend(summary.getId());
		if(summaryExtend != null) {
			summary = transSetExtendToSummary(summary, summaryExtend);
		}
		return summary;
	}
	
	public EdocSummaryExtend transSetSummaryToExtend(EdocSummary summary, EdocSummaryExtend summaryExtend) throws BusinessException {
		summaryExtend.setSummaryId(summary.getId());
		summaryExtend.setAppType(ApplicationCategoryEnum.edoc.key());
		summaryExtend.setText1(summary.getText1());
		summaryExtend.setText2(summary.getText2());
		summaryExtend.setText3(summary.getText3());
		summaryExtend.setText4(summary.getText4());
		summaryExtend.setText5(summary.getText5());
		summaryExtend.setText6(summary.getText6());
		summaryExtend.setText7(summary.getText7());
		summaryExtend.setText8(summary.getText8());
		summaryExtend.setText9(summary.getText9());
		summaryExtend.setText10(summary.getText10());
		summaryExtend.setText11(summary.getText11());
		summaryExtend.setText12(summary.getText12());
		summaryExtend.setText13(summary.getText13());
		summaryExtend.setText14(summary.getText14());
		summaryExtend.setText15(summary.getText15());
		summaryExtend.setVarchar1(summary.getVarchar1());
		summaryExtend.setVarchar2(summary.getVarchar2());
		summaryExtend.setVarchar3(summary.getVarchar3());
		summaryExtend.setVarchar4(summary.getVarchar4());
		summaryExtend.setVarchar5(summary.getVarchar5());
		summaryExtend.setVarchar6(summary.getVarchar6());
		summaryExtend.setVarchar7(summary.getVarchar7());
		summaryExtend.setVarchar8(summary.getVarchar8());
		summaryExtend.setVarchar9(summary.getVarchar9());
		summaryExtend.setVarchar10(summary.getVarchar10());
		summaryExtend.setVarchar11(summary.getVarchar11());
		summaryExtend.setVarchar12(summary.getVarchar12());
		summaryExtend.setVarchar13(summary.getVarchar13());
		summaryExtend.setVarchar14(summary.getVarchar14());
		summaryExtend.setVarchar15(summary.getVarchar15());
		summaryExtend.setVarchar16(summary.getVarchar16());
		summaryExtend.setVarchar17(summary.getVarchar17());
		summaryExtend.setVarchar18(summary.getVarchar18());
		summaryExtend.setVarchar19(summary.getVarchar19());
		summaryExtend.setVarchar20(summary.getVarchar20());
		summaryExtend.setVarchar21(summary.getVarchar21());
		summaryExtend.setVarchar22(summary.getVarchar22());
		summaryExtend.setVarchar23(summary.getVarchar23());
		summaryExtend.setVarchar24(summary.getVarchar24());
		summaryExtend.setVarchar25(summary.getVarchar25());
		summaryExtend.setVarchar26(summary.getVarchar26());
		summaryExtend.setVarchar27(summary.getVarchar27());
		summaryExtend.setVarchar28(summary.getVarchar28());
		summaryExtend.setVarchar29(summary.getVarchar29());
		summaryExtend.setVarchar30(summary.getVarchar30());
		summaryExtend.setInteger1(summary.getInteger1());
		summaryExtend.setInteger2(summary.getInteger2());
		summaryExtend.setInteger3(summary.getInteger3());
		summaryExtend.setInteger4(summary.getInteger4());
		summaryExtend.setInteger5(summary.getInteger5());
		summaryExtend.setInteger6(summary.getInteger6());
		summaryExtend.setInteger7(summary.getInteger7());
		summaryExtend.setInteger8(summary.getInteger8());
		summaryExtend.setInteger9(summary.getInteger9());
		summaryExtend.setInteger10(summary.getInteger10());
		summaryExtend.setInteger11(summary.getInteger11());
		summaryExtend.setInteger12(summary.getInteger12());
		summaryExtend.setInteger13(summary.getInteger13());
		summaryExtend.setInteger14(summary.getInteger14());
		summaryExtend.setInteger15(summary.getInteger15());
		summaryExtend.setInteger16(summary.getInteger16());
		summaryExtend.setInteger17(summary.getInteger17());
		summaryExtend.setInteger18(summary.getInteger18());
		summaryExtend.setInteger19(summary.getInteger19());
		summaryExtend.setInteger20(summary.getInteger20());
		summaryExtend.setDecimal1(summary.getDecimal1());
		summaryExtend.setDecimal2(summary.getDecimal2());
		summaryExtend.setDecimal3(summary.getDecimal3());
		summaryExtend.setDecimal4(summary.getDecimal4());
		summaryExtend.setDecimal5(summary.getDecimal5());
		summaryExtend.setDecimal6(summary.getDecimal6());
		summaryExtend.setDecimal7(summary.getDecimal7());
		summaryExtend.setDecimal8(summary.getDecimal8());
		summaryExtend.setDecimal9(summary.getDecimal9());
		summaryExtend.setDecimal10(summary.getDecimal10());
		summaryExtend.setDecimal11(summary.getDecimal11());
		summaryExtend.setDecimal12(summary.getDecimal12());
		summaryExtend.setDecimal13(summary.getDecimal13());
		summaryExtend.setDecimal14(summary.getDecimal14());
		summaryExtend.setDecimal15(summary.getDecimal15());
		summaryExtend.setDecimal16(summary.getDecimal16());
		summaryExtend.setDecimal17(summary.getDecimal17());
		summaryExtend.setDecimal18(summary.getDecimal18());
		summaryExtend.setDecimal19(summary.getDecimal19());
		summaryExtend.setDecimal20(summary.getDecimal20());
		summaryExtend.setDate1(summary.getDate1());
		summaryExtend.setDate2(summary.getDate2());
		summaryExtend.setDate3(summary.getDate3());
		summaryExtend.setDate4(summary.getDate4());
		summaryExtend.setDate5(summary.getDate5());
		summaryExtend.setDate6(summary.getDate6());
		summaryExtend.setDate7(summary.getDate7());
		summaryExtend.setDate8(summary.getDate8());
		summaryExtend.setDate9(summary.getDate9());
		summaryExtend.setDate10(summary.getDate10());
		summaryExtend.setDate11(summary.getDate11());
		summaryExtend.setDate12(summary.getDate12());
		summaryExtend.setDate13(summary.getDate13());
		summaryExtend.setDate14(summary.getDate14());
		summaryExtend.setDate15(summary.getDate15());
		summaryExtend.setDate16(summary.getDate16());
		summaryExtend.setDate17(summary.getDate17());
		summaryExtend.setDate18(summary.getDate18());
		summaryExtend.setDate19(summary.getDate19());
		summaryExtend.setDate20(summary.getDate20());
		summaryExtend.setList1(summary.getList1());
		summaryExtend.setList2(summary.getList2());
		summaryExtend.setList3(summary.getList3());
		summaryExtend.setList4(summary.getList4());
		summaryExtend.setList5(summary.getList5());
		summaryExtend.setList6(summary.getList6());
		summaryExtend.setList7(summary.getList7());
		summaryExtend.setList8(summary.getList8());
		summaryExtend.setList9(summary.getList9());
		summaryExtend.setList10(summary.getList10());
		summaryExtend.setList11(summary.getList11());
		summaryExtend.setList12(summary.getList12());
		summaryExtend.setList13(summary.getList13());
		summaryExtend.setList14(summary.getList14());
		summaryExtend.setList15(summary.getList15());
		summaryExtend.setList16(summary.getList16());
		summaryExtend.setList17(summary.getList17());
		summaryExtend.setList18(summary.getList18());
		summaryExtend.setList19(summary.getList19());
		summaryExtend.setList20(summary.getList20());
		return summaryExtend;
	}
	
	public EdocSummary transSetExtendToSummary(EdocSummary summary, EdocSummaryExtend summaryExtend) throws BusinessException {
		summary.setText1(summaryExtend.getText1());
		summary.setText2(summaryExtend.getText2());
		summary.setText3(summaryExtend.getText3());
		summary.setText4(summaryExtend.getText4());
		summary.setText5(summaryExtend.getText5());
		summary.setText6(summaryExtend.getText6());
		summary.setText7(summaryExtend.getText7());
		summary.setText8(summaryExtend.getText8());
		summary.setText9(summaryExtend.getText9());
		summary.setText10(summaryExtend.getText10());
		summary.setText11(summaryExtend.getText11());
		summary.setText12(summaryExtend.getText12());
		summary.setText13(summaryExtend.getText13());
		summary.setText14(summaryExtend.getText14());
		summary.setText15(summaryExtend.getText15());
		summary.setVarchar1(summaryExtend.getVarchar1());
		summary.setVarchar2(summaryExtend.getVarchar2());
		summary.setVarchar3(summaryExtend.getVarchar3());
		summary.setVarchar4(summaryExtend.getVarchar4());
		summary.setVarchar5(summaryExtend.getVarchar5());
		summary.setVarchar6(summaryExtend.getVarchar6());
		summary.setVarchar7(summaryExtend.getVarchar7());
		summary.setVarchar8(summaryExtend.getVarchar8());
		summary.setVarchar9(summaryExtend.getVarchar9());
		summary.setVarchar10(summaryExtend.getVarchar10());
		summary.setVarchar11(summaryExtend.getVarchar11());
		summary.setVarchar12(summaryExtend.getVarchar12());
		summary.setVarchar13(summaryExtend.getVarchar13());
		summary.setVarchar14(summaryExtend.getVarchar14());
		summary.setVarchar15(summaryExtend.getVarchar15());
		summary.setVarchar16(summaryExtend.getVarchar16());
		summary.setVarchar17(summaryExtend.getVarchar17());
		summary.setVarchar18(summaryExtend.getVarchar18());
		summary.setVarchar19(summaryExtend.getVarchar19());
		summary.setVarchar20(summaryExtend.getVarchar20());
		summary.setVarchar21(summaryExtend.getVarchar21());
		summary.setVarchar22(summaryExtend.getVarchar22());
		summary.setVarchar23(summaryExtend.getVarchar23());
		summary.setVarchar24(summaryExtend.getVarchar24());
		summary.setVarchar25(summaryExtend.getVarchar25());
		summary.setVarchar26(summaryExtend.getVarchar26());
		summary.setVarchar27(summaryExtend.getVarchar27());
		summary.setVarchar28(summaryExtend.getVarchar28());
		summary.setVarchar29(summaryExtend.getVarchar29());
		summary.setVarchar30(summaryExtend.getVarchar30());
		summary.setInteger1(summaryExtend.getInteger1());
		summary.setInteger2(summaryExtend.getInteger2());
		summary.setInteger3(summaryExtend.getInteger3());
		summary.setInteger4(summaryExtend.getInteger4());
		summary.setInteger5(summaryExtend.getInteger5());
		summary.setInteger6(summaryExtend.getInteger6());
		summary.setInteger7(summaryExtend.getInteger7());
		summary.setInteger8(summaryExtend.getInteger8());
		summary.setInteger9(summaryExtend.getInteger9());
		summary.setInteger10(summaryExtend.getInteger10());
		summary.setInteger11(summaryExtend.getInteger11());
		summary.setInteger12(summaryExtend.getInteger12());
		summary.setInteger13(summaryExtend.getInteger13());
		summary.setInteger14(summaryExtend.getInteger14());
		summary.setInteger15(summaryExtend.getInteger15());
		summary.setInteger16(summaryExtend.getInteger16());
		summary.setInteger17(summaryExtend.getInteger17());
		summary.setInteger18(summaryExtend.getInteger18());
		summary.setInteger19(summaryExtend.getInteger19());
		summary.setInteger20(summaryExtend.getInteger20());
		summary.setDecimal1(summaryExtend.getDecimal1());
		summary.setDecimal2(summaryExtend.getDecimal2());
		summary.setDecimal3(summaryExtend.getDecimal3());
		summary.setDecimal4(summaryExtend.getDecimal4());
		summary.setDecimal5(summaryExtend.getDecimal5());
		summary.setDecimal6(summaryExtend.getDecimal6());
		summary.setDecimal7(summaryExtend.getDecimal7());
		summary.setDecimal8(summaryExtend.getDecimal8());
		summary.setDecimal9(summaryExtend.getDecimal9());
		summary.setDecimal10(summaryExtend.getDecimal10());
		summary.setDecimal11(summaryExtend.getDecimal11());
		summary.setDecimal12(summaryExtend.getDecimal12());
		summary.setDecimal13(summaryExtend.getDecimal13());
		summary.setDecimal14(summaryExtend.getDecimal14());
		summary.setDecimal15(summaryExtend.getDecimal15());
		summary.setDecimal16(summaryExtend.getDecimal16());
		summary.setDecimal17(summaryExtend.getDecimal17());
		summary.setDecimal18(summaryExtend.getDecimal18());
		summary.setDecimal19(summaryExtend.getDecimal19());
		summary.setDecimal20(summaryExtend.getDecimal20());
		summary.setDate1(summaryExtend.getDate1());
		summary.setDate2(summaryExtend.getDate2());
		summary.setDate3(summaryExtend.getDate3());
		summary.setDate4(summaryExtend.getDate4());
		summary.setDate5(summaryExtend.getDate5());
		summary.setDate6(summaryExtend.getDate6());
		summary.setDate7(summaryExtend.getDate7());
		summary.setDate8(summaryExtend.getDate8());
		summary.setDate9(summaryExtend.getDate9());
		summary.setDate10(summaryExtend.getDate10());
		summary.setDate11(summaryExtend.getDate11());
		summary.setDate12(summaryExtend.getDate12());
		summary.setDate13(summaryExtend.getDate13());
		summary.setDate14(summaryExtend.getDate14());
		summary.setDate15(summaryExtend.getDate15());
		summary.setDate16(summaryExtend.getDate16());
		summary.setDate17(summaryExtend.getDate17());
		summary.setDate18(summaryExtend.getDate18());
		summary.setDate19(summaryExtend.getDate19());
		summary.setDate20(summaryExtend.getDate20());
		summary.setList1(summaryExtend.getList1());
		summary.setList2(summaryExtend.getList2());
		summary.setList3(summaryExtend.getList3());
		summary.setList4(summaryExtend.getList4());
		summary.setList5(summaryExtend.getList5());
		summary.setList6(summaryExtend.getList6());
		summary.setList7(summaryExtend.getList7());
		summary.setList8(summaryExtend.getList8());
		summary.setList9(summaryExtend.getList9());
		summary.setList10(summaryExtend.getList10());
		summary.setList11(summaryExtend.getList11());
		summary.setList12(summaryExtend.getList12());
		summary.setList13(summaryExtend.getList13());
		summary.setList14(summaryExtend.getList14());
		summary.setList15(summaryExtend.getList15());
		summary.setList16(summaryExtend.getList16());
		summary.setList17(summaryExtend.getList17());
		summary.setList18(summaryExtend.getList18());
		summary.setList19(summaryExtend.getList19());
		summary.setList20(summaryExtend.getList20());
		return summary;
	}
	
	public void setEdocSummaryExtendDao(EdocSummaryExtendDao edocSummaryExtendDao) {
		this.edocSummaryExtendDao = edocSummaryExtendDao;
	}

	@Override
	public void deleteEdocSummaryExtend(EdocSummaryExtend summaryExtend)
			throws BusinessException {
		edocSummaryExtendDao.deleteEdocSummaryExtend(summaryExtend);
	}

	@Override
	public void deleteEdocSummaryExtendBySummaryId(Long summaryId)
			throws BusinessException {
		edocSummaryExtendDao.deleteEdocSummaryExtendBySummaryId(summaryId);
	}

	@Override
	public List<EdocSummaryExtend> getEdocSummaryExtendBySummaryIds(
			List<Long> summaryIds) throws BusinessException {
		return edocSummaryExtendDao.getEdocSummaryExtendBySummaryIds(summaryIds);
	}
	
	@Override
	public List<EdocSummaryExtend> getEdocSummaryExtendBySummaryIds(long curUserId, EdocSearchModel em) throws BusinessException {
		return edocSummaryExtendDao.getEdocSummaryExtendBySummaryIds(curUserId, em);
	}
	
}
