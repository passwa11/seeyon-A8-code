package com.seeyon.v3x.edoc.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.dao.BaseDao;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.domain.EdocSummaryExtend;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.edoc.webmodel.EdocSearchModel;

public class EdocSummaryExtendDao extends BaseDao<EdocSummaryExtend> {

	@SuppressWarnings("unchecked")
	public List<EdocSummary> getAllEdocSummaryList() throws BusinessException {
		List<EdocSummary> result = super.findVarargs("from EdocSummary");
		return result;
	}

	@SuppressWarnings("unchecked")
	public Integer getEdocSummaryExtendCount() throws BusinessException {
		List<Object[]> result = super.findVarargs("select count(*) from EdocSummaryExtend");
		if(Strings.isNotEmpty(result)) {
			return result.size();
		}
		return 0;
	}
	
	public void deleteAllEdocSummaryExtend() throws BusinessException {
		super.bulkUpdate("delete from EdocSummaryExtend", null);
	}
	
	public void deleteEdocSummaryExtend(EdocSummaryExtend summaryExtend) throws BusinessException {
		super.delete(summaryExtend);
	}

	public void deleteEdocSummaryExtendBySummaryId(Long summaryId) throws BusinessException {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("summaryId", summaryId);
		super.bulkUpdate("delete from EdocSummaryExtend where summaryId=:summaryId", paramMap);
	}
	
	public void saveEdocSummaryExtend(List<EdocSummaryExtend> summaryExtendList) throws BusinessException {
		super.savePatchAll(summaryExtendList);
	}
	
	public void saveEdocSummaryExtend(EdocSummaryExtend summaryExtend) throws BusinessException {
		super.save(summaryExtend);
	}
	
	public void updateEdocSummaryExtend(EdocSummaryExtend summaryExtend) throws BusinessException {
		super.update(summaryExtend);
	}
	
	@SuppressWarnings("unchecked")
	public EdocSummaryExtend getEdocSummaryExtend(Long summaryId) throws BusinessException {
		EdocSummaryExtend summaryExtend = null;
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("summaryId", summaryId);
		List<EdocSummaryExtend> list = super.find("from EdocSummaryExtend where summaryId=:summaryId", paramMap);
		if(Strings.isNotEmpty(list)) {
			summaryExtend = (EdocSummaryExtend)list.get(0);
		}
		return summaryExtend;
	}
	
	@SuppressWarnings("unchecked")
	public List<EdocSummaryExtend> getEdocSummaryExtendBySummaryIds(List<Long> summaryIds) throws BusinessException {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("summaryIds", summaryIds);
		List<EdocSummaryExtend> list = super.find("from EdocSummaryExtend where summaryId in (:summaryIds)",-1,-1,paramMap);
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<EdocSummaryExtend> getEdocSummaryExtendBySummaryIds(long curUserId,EdocSearchModel em) throws BusinessException {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		String hql = "";
        hql += "select summaryExtend from EdocSummaryExtend as summaryExtend,  EdocSummary as summary , CtpAffair as affair "
        		+ " where (summaryExtend.summaryId = summary.id and affair.objectId=summary.id) and (affair.memberId=:curUserId)"
        		+ " and (affair.state in ("+StateEnum.col_done.getKey()+","+StateEnum.col_pending.getKey()+","+StateEnum.col_sent.getKey()+") )"
                + " and summary.state<> " + EdocConstant.flowState.deleted.ordinal();	
        paramMap.put("curUserId", curUserId);
        
        if(em.getEdocType()==3){//全部
        	hql+= " and (affair.app in ("+ApplicationCategoryEnum.edocSend.key()+","+ApplicationCategoryEnum.edocRec.key()+","+ApplicationCategoryEnum.edocSign.key()+"))";
        }else{
        	hql+= " and (affair.app=:app)";
        	paramMap.put("app", EdocUtil.getAppCategoryByEdocType(em.getEdocType()).key());
        }

        
        if (!Strings.isBlank(em.getSubject())) {        	
                hql += " and (summary.subject like :subject)";
                paramMap.put("subject", "%" + SQLWildcardUtil.escape(em.getSubject()) + "%");
            }
        if (!Strings.isBlank(em.getDocMark())) {
                hql += " and (summary.docMark like :docMark or summary.docMark2 like :docMark2)";
                paramMap.put("docMark", "%" + SQLWildcardUtil.escape(em.getDocMark()) + "%");
                paramMap.put("docMark2", "%" + SQLWildcardUtil.escape(em.getDocMark()) + "%");
            }
        if (!Strings.isBlank(em.getSerialNo())) {
                hql += " and (summary.serialNo like :serialNo)";
                paramMap.put("serialNo", "%" + SQLWildcardUtil.escape(em.getSerialNo()) + "%");
            }
        if (!Strings.isBlank(em.getKeywords())) {
            hql += " and (summary.keywords like :keywords)";
            paramMap.put("keywords", "%" + SQLWildcardUtil.escape(em.getKeywords()) + "%");
        }
        if (!Strings.isBlank(em.getDocType())) {
        	hql += " and (summary.docType = :docType)";
        	paramMap.put("docType", em.getDocType());
        }
        if (!Strings.isBlank(em.getSendType())) {
            hql += " and (summary.sendType = :sendType)";
            paramMap.put("sendType", em.getSendType());
        }
        if (!Strings.isBlank(em.getCreatePerson())) {
            hql += " and (summary.createPerson like :createPerson)";
            paramMap.put("createPerson", "%" + SQLWildcardUtil.escape(em.getCreatePerson()) + "%");
        }
        if (em.getCreateTimeB()!=null) {
            hql += " and (summary.createTime >= :createTimeB)";
            paramMap.put("createTimeB", Datetimes.getTodayFirstTime(em.getCreateTimeB()));
        }
        if (em.getCreateTimeE()!=null) {
            hql += " and (summary.createTime <= :createTimeE)";
            paramMap.put("createTimeE", Datetimes.getTodayFirstTime(em.getCreateTimeE()));
        }
        if (!Strings.isBlank(em.getSendToId())) {
        	//主送
            hql += " and ((summary.sendToId like :sendToId or summary.sendToId2 like :sendToId2)";
            paramMap.put("sendToId", "%" + SQLWildcardUtil.escape(em.getSendToId()) + "%");
            paramMap.put("sendToId2", "%" + SQLWildcardUtil.escape(em.getSendToId()) + "%");
            //抄送
            hql += " or (summary.copyToId like :copyToId or summary.copyToId2 like :copyToId2)";
            paramMap.put("copyToId", "%" + SQLWildcardUtil.escape(em.getSendToId()) + "%");
            paramMap.put("copyToI2", "%" + SQLWildcardUtil.escape(em.getSendToId()) + "%");
            //抄报
            hql += " or (summary.reportToId like :reportToId or summary.reportToId2 like :reportToId2))";
            paramMap.put("reportToId", "%" + SQLWildcardUtil.escape(em.getSendToId()) + "%");
            paramMap.put("reportToId2", "%" + SQLWildcardUtil.escape(em.getSendToId()) + "%");
        }
        if (!Strings.isBlank(em.getSendUnitId())) {
            hql += " and (summary.sendUnitId like :sendUnitId or summary.sendUnitId2 like :sendUnitId2)";
            paramMap.put("sendUnitId", "%" + SQLWildcardUtil.escape(em.getSendUnitId()) + "%");
            paramMap.put("sendUnitId2", "%" + SQLWildcardUtil.escape(em.getSendUnitId()) + "%");
        }
        //发文部门
        if (!Strings.isBlank(em.getSendDepartmentId())) {
            hql += " and (summary.sendDepartmentId like :sendDepartmentId or summary.sendDepartmentId2 like :sendDepartmentId2)";
            paramMap.put("sendDepartmentId", "%" + SQLWildcardUtil.escape(em.getSendDepartmentId()) + "%");
            paramMap.put("sendDepartmentId2", "%" + SQLWildcardUtil.escape(em.getSendDepartmentId()) + "%");
        }
        if (!Strings.isBlank(em.getIssuer())) {
            hql += " and (summary.issuer like :issuer)";
            paramMap.put("issuer", "%" + SQLWildcardUtil.escape(em.getIssuer()) + "%");
        }
        if (em.getSigningDateA()!=null) {
            hql += " and (summary.signingDate >= :signingDateA)";
            paramMap.put("signingDateA", Datetimes.getTodayFirstTime(em.getSigningDateA()));
        }
        if (em.getSigningDateB()!=null) {
            hql += " and (summary.signingDate <= :signingDateB)";
            paramMap.put("signingDateB", Datetimes.getTodayFirstTime(em.getSigningDateB()));
        }
        List<EdocSummaryExtend> list = DBAgent.find(hql,paramMap);
		return list;
	}
	
}
