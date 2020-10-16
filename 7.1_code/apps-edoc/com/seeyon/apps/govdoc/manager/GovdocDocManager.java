package com.seeyon.apps.govdoc.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.edoc.constants.EdocConstant.SendType;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.v3x.edoc.domain.EdocSummary;

/**
 * 公文归档管理接口
 * @author tanggl
 *
 */
public interface GovdocDocManager {

	/**
	 * 归档时同一公文已被回退的文档删除
	 * @param collIds
	 * @param destFolderId
	 * @return
	 * @throws BusinessException
	 */
	public String transPigeonholeDeleteStepBackDoc(List<String> collIds, Long destFolderId) throws BusinessException;
	
	/**
	 * 检查公文是否能归档
	 * @param collIds 协同id列表
	 * @return 归档结果信息
	 * @throws BusinessException
	 */
	public String getPigeonholeRight(List<String> collIds) throws BusinessException;
	/**
	 * @param collIds 公文id列表
	 * @param destFolderId 目标文件夹ID
	 * @return
	 * @throws BusinessException
	 */
	public String getIsSamePigeonhole(List<String> collIds, Long destFolderId) throws BusinessException;
	public String checkPigeonhole(List<String> id, Long floder, String pageType);
	/**
	 * 归档公文
	 * @param collIds 协同id列表
	 * @param destFolderId 目标文件夹ID
	 * @param type 归档操作出发源类型，包括待办pending，已办done，已发sent和处理协同handle
	 * @return 归档结果信息
	 * @throws BusinessException
	 */
	public String transPigeonhole(Long affairId, Long destFolderId, String type) throws BusinessException;
	public String transPigeonhole(EdocSummary summary, CtpAffair affair,Long destFolderId, String type) throws BusinessException;
	
	public void savePigeonhole(GovdocNewVO info, SendType sendType) throws BusinessException;
	
	public void updateDocMetadata(EdocSummary summary, int app);
	
	public void updateDocMetadata(Map<Long, Long> sourceIdMap) throws BusinessException;
	
}
