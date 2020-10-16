package com.seeyon.apps.govdoc.manager;

import java.util.List;

import com.seeyon.apps.govdoc.vo.GovdocComponentVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.v3x.system.signet.domain.V3xHtmDocumentSignature;
import com.seeyon.v3x.system.signet.domain.V3xSignet;

public interface GovdocSignetManager {

	public void fillFormSignData(GovdocComponentVO summaryVO);
	
	/****************************************** 11111 印章管理 start ******************************************************/
	/**
	 * 复制签章
	 * @param oldFileId
	 * @param newFileId
	 * @throws BusinessException
	 */
	public void copySignet(Long oldFileId, Long newFileId) throws BusinessException;
	
	/****************************************** 33333 印章管理 start ******************************************************/
	
	public int getISignCount(Long documentId);
	
	
	
	/**
     * 删除edocSummary具体事项的签名或印章方式
     * @Author      : xuqiangwei
     * @Date        : 2014年11月7日下午3:15:28
     * @param summaryId
     * @param affairId
     * @param signType
     */
    public void deleteBySummaryIdAffairIdAndType(Long summaryId, Long affairId, int signType);
    
    public void deleteBySummaryId(Long summaryId);
    
    
    public V3xHtmDocumentSignature getBySummaryIdAffairIdAndType(Long summaryId, Long affairId, int signType);
	
    /**
	 * 更新签批对象
	 * @param htmSignate
	 */
	public void update(V3xHtmDocumentSignature htmSignate);
	
	/**
	 * 
	 * @Description : 保存签名或印章
	 * @Author      : xuqiangwei
	 * @Date        : 2014年11月14日上午10:25:19
	 * @param htmSignate
	 */
	public void save(V3xHtmDocumentSignature htmSignate);
	
	/**
     * 得到某人印章
     * @param memberId
     * @return
     * @throws Exception
     */
    public List<V3xSignet> findSignetByMemberId(Long memberId);
    
    /****************************************** 33333 专业章 start ******************************************************/
    /**
     * 拷贝专业签章数据，并存储在数据库里面。
     * @param originalDocumentId 原始文档id
     * @param newDocumentId 新文档Id
     * @throws BusinessException
     */
     public void saveSignature(Long originalDocumentId,Long newDocumentId) throws BusinessException;
     /**
 	 * 删除ISIgnatureHTML专业签章
 	 * @param summaryId
 	 * @throws BusinessException
 	 */
 	public void deleteAllByDocumentId(Long summaryId) throws BusinessException;
     /****************************************** 33333 专业章   end ******************************************************/
}
