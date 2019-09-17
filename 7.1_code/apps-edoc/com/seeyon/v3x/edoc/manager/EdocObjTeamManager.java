package com.seeyon.v3x.edoc.manager;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.seeyon.v3x.edoc.domain.EdocObjTeam;

public interface EdocObjTeamManager {
	
	public List<EdocObjTeam> findAll(Long accountId);
	
	/**
	 * 取得所有机构组，不分页
	 * 
	 * @param accountId
	 * @return
	 */
	public List<EdocObjTeam> findAllNotPager(Long accountId);
	public List<EdocObjTeam> findAllSimpleNotPager(Long accountId);
	public EdocObjTeam getById(Long teamId);
	public void update(EdocObjTeam edocObjTeam);
	public void save(EdocObjTeam edocObjTeam);
	public void delete(String ids);
	/**
	 * 判断交换单位数据是否有变化
	 * 
	 * @param orginalTimestamp
	 * @return
	 */
	public boolean isModifyExchangeAccounts(Date orginalTimestamp);
	
	public Date getLastModifyTimestamp();
	public String ajaxGetByName(String orgName,String accountIdStr);
	public Map<Long,String> getOrgTeamForDepartment(String ids,Long loginAccountId);

}
