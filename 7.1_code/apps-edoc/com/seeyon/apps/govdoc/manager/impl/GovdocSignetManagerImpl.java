package com.seeyon.apps.govdoc.manager.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.common.isignaturehtml.manager.ISignatureHtmlManager;
import com.seeyon.apps.govdoc.manager.GovdocSignetManager;
import com.seeyon.apps.govdoc.vo.GovdocComponentVO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.isignaturehtml.CtpIsignatureHtml;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.system.signet.domain.V3xDocumentSignature;
import com.seeyon.v3x.system.signet.domain.V3xHtmDocumentSignature;
import com.seeyon.v3x.system.signet.domain.V3xSignet;
import com.seeyon.v3x.system.signet.manager.SignetManager;
import com.seeyon.v3x.system.signet.manager.V3xHtmDocumentSignatManager;

public class GovdocSignetManagerImpl implements GovdocSignetManager {

	private FileManager fileManager;
	private SignetManager signetManager;
	private ISignatureHtmlManager iSignatureHtmlManager;
	private V3xHtmDocumentSignatManager htmSignetManager;
	
	public void fillFormSignData(GovdocComponentVO summaryVO) {
		boolean signatrueShowFlag = summaryVO.getSwitchVo().isHasDealArea();
		List<CtpIsignatureHtml> htmlSigns = iSignatureHtmlManager.getISignatureByDocumentId(summaryVO.getSummary().getId());
		Map<String,Boolean> govCanOperatingSign = new HashMap<String,Boolean>();
		if(htmlSigns != null && org.apache.commons.collections.CollectionUtils.isNotEmpty(htmlSigns)){
			for (CtpIsignatureHtml ctpIsignatureHtml : htmlSigns) {
				if(ctpIsignatureHtml != null && ctpIsignatureHtml.getUserId()!=null && ctpIsignatureHtml.getUserId() == AppContext.currentUserId()){
					govCanOperatingSign.put(ctpIsignatureHtml.getId().toString(), true);
				}else {
					govCanOperatingSign.put(ctpIsignatureHtml.getId().toString(), false);
				}
			}
		}
		summaryVO.setGovCanOperatingSign(JSONUtil.toJSONString(govCanOperatingSign));
		summaryVO.setCanMoveISignature(signatrueShowFlag);
		summaryVO.setCanDeleteISigntureHtml(signatrueShowFlag);
		// 是否显示移动签章按钮
		summaryVO.setShowMoveMenu(signatrueShowFlag);
		// 是否显示锁定签章按钮
		summaryVO.setShowDocLockMenu(signatrueShowFlag);
	}
	
	@Override
	public void copySignet(Long oldFileId, Long newFileId) throws BusinessException {
		try {
			V3XFile contentFile = fileManager.getV3XFile(oldFileId);
			List<V3xDocumentSignature> signlist = signetManager.findDocumentSignatureByDocumentId(contentFile.getId().toString());
			if (signlist != null && signlist.size() > 0) {
				for (V3xDocumentSignature v3xDocumentSignature : signlist) {
					V3xDocumentSignature documentSign = (V3xDocumentSignature) v3xDocumentSignature.clone();
					documentSign.setId(UUIDLong.longUUID());
					documentSign.setRecordId(String.valueOf(newFileId));
					signetManager.save(documentSign);
				}
			}
		} catch(Exception e) {
			throw new BusinessException(e);
		}
	}
	
	public int getISignCount(Long documentId) {
		return iSignatureHtmlManager.getISignCount(documentId);
	}

	@Override
	public void deleteAllByDocumentId(Long summaryId) throws BusinessException {
		iSignatureHtmlManager.deleteAllByDocumentId(summaryId);
	}
	
	@Override
	public void deleteBySummaryIdAffairIdAndType(Long summaryId, Long affairId, int signType) {
		htmSignetManager.deleteBySummaryIdAffairIdAndType(summaryId, affairId, signType);
	}
	
	public void deleteBySummaryId(Long summaryId) {
		htmSignetManager.deleteBySummaryId(summaryId);
	}
	
	public V3xHtmDocumentSignature getBySummaryIdAffairIdAndType(Long summaryId, Long affairId, int signType) {
		return htmSignetManager.getBySummaryIdAffairIdAndType(summaryId, affairId, signType);
	}
	
	/**
	 * 更新签批对象
	 * @param htmSignate
	 */
	public void update(V3xHtmDocumentSignature htmSignate) {
		htmSignetManager.update(htmSignate);
	}
	
	/**
	 * 
	 * @Description : 保存签名或印章
	 * @Author      : xuqiangwei
	 * @Date        : 2014年11月14日上午10:25:19
	 * @param htmSignate
	 */
	public void save(V3xHtmDocumentSignature htmSignate) {
		htmSignetManager.save(htmSignate);
	}
	
	/**
     * 得到某人印章
     * @param memberId
     * @return
     * @throws Exception
     */
    public List<V3xSignet> findSignetByMemberId(Long memberId) {
    	return signetManager.findSignetByMemberId(memberId);
    }
	
    /**
     * 拷贝专业签章数据，并存储在数据库里面。
     * @param originalDocumentId 原始文档id
     * @param newDocumentId 新文档Id
     * @throws BusinessException
     */
     public void saveSignature(Long originalDocumentId,Long newDocumentId) throws BusinessException {
    	 iSignatureHtmlManager.save(originalDocumentId, newDocumentId);
     }
    
	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}
	public void setSignetManager(SignetManager signetManager) {
		this.signetManager = signetManager;
	}
	public void setiSignatureHtmlManager(ISignatureHtmlManager iSignatureHtmlManager) {
		this.iSignatureHtmlManager = iSignatureHtmlManager;
	}
	public void setHtmSignetManager(V3xHtmDocumentSignatManager htmSignetManager) {
		this.htmSignetManager = htmSignetManager;
	}	
}
