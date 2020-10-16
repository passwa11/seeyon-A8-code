package com.seeyon.apps.govdoc.manager;

import java.util.Map;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.edoc.domain.EdocObjTeam;

/**
 * 新公文机构组接口
 * @author 唐桂林
 *
 */
public interface GovdocObjTeamManager {

	/**
	 * 公文应用设置-机构组列表
	 * @param flipInfo
	 * @param condition
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo findList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException;
	
	/**
	 * 机构组查看
	 * @param teamId
	 * @return
	 * @throws BusinessException
	 */
	public EdocObjTeam getObjTeamById(Long teamId) throws BusinessException;

	/**
	 * 机构组显示-选人界面
	 * @param teamId
	 * @param type
	 * @return
	 * @throws BusinessException
	 */
	public EdocObjTeam getObjTeamById(Long teamId, String type) throws BusinessException;

	/**
	 * 机构组新增保存
	 * @param po
	 * @throws BusinessException
	 */
	public void saveObjTeam(EdocObjTeam po) throws BusinessException;
	
	/**
	 * 机构组修改保存
	 * @param po
	 * @throws BusinessException
	 */
	public void updateObjTeam(EdocObjTeam po) throws BusinessException;
	
	/**
	 * 
	 * @param orgName
	 * @param accountIdStr
	 * @return
	 */
	public String ajaxGetByName(String orgName, String accountIdStr);
	
	/**
	 * 机构组删除
	 * @param ids
	 * @throws BusinessException
	 */
	public void deleteObjTeam(String ids) throws BusinessException;
	/**
	 * 删除机构组成员
	 * @param ids
	 * @throws BusinessException
	 */
	public void deleteByMemberId(Long memberId) throws BusinessException;
	
}
