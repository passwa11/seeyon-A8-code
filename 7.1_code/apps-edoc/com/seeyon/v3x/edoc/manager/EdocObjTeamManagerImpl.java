package com.seeyon.v3x.edoc.manager;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.AbstractSystemInitializer;
import com.seeyon.ctp.common.cache.CacheAccessable;
import com.seeyon.ctp.common.cache.CacheFactory;
import com.seeyon.ctp.common.cache.CacheMap;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.v3x.edoc.dao.EdocObjTeamDao;
import com.seeyon.v3x.edoc.dao.EdocObjTeamMemberDao;
import com.seeyon.v3x.edoc.domain.EdocObjTeam;

public class EdocObjTeamManagerImpl extends AbstractSystemInitializer implements EdocObjTeamManager {
	

	private CacheMap<String, Date> modifyTimestamp = null;
	private final static String OBJTEAMMODIFYDATE = "modifyTimestamp";
	
	private EdocObjTeamDao edocObjTeamDao=null;
	private EdocObjTeamMemberDao edocObjTeamMemberDao=null;
	private OrgManager orgManager;

	
	
	public EdocObjTeamDao getEdocObjTeamDao() {
		return edocObjTeamDao;
	}

	public void setEdocObjTeamDao(EdocObjTeamDao edocObjTeamDao) {
		this.edocObjTeamDao = edocObjTeamDao;
	}

	public EdocObjTeamMemberDao getEdocObjTeamMemberDao() {
		return edocObjTeamMemberDao;
	}

	public void setEdocObjTeamMemberDao(EdocObjTeamMemberDao edocObjTeamMemberDao) {
		this.edocObjTeamMemberDao = edocObjTeamMemberDao;
	}

	public OrgManager getOrgManager() {
		return orgManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	
	public List<EdocObjTeam> findAll(Long accountId)
	{
		List<EdocObjTeam> ls=edocObjTeamDao.findAllByAccount(accountId, true);
		for(EdocObjTeam eot:ls)
		{
			eot.changeSelObjsStr();
		}
		return ls;		
	}
	public List<EdocObjTeam> findAllSimpleNotPager(Long accountId)
	{
	    return edocObjTeamDao.findAllSimpleByAccount(accountId, false);
	}
	
	public List<EdocObjTeam> findAllNotPager(Long accountId)
	{
		return edocObjTeamDao.findAllByAccount(accountId, false);
	}
	public EdocObjTeam getById(Long teamId)
	{
		EdocObjTeam eot=edocObjTeamDao.get(teamId);
		if(eot != null){
			eot.changeSelObjsStr();
		}
		return eot;
	}
	
	public void delete(String ids)
	{
		edocObjTeamDao.updateState(ids,EdocObjTeam.STATE_DEL);
		updateModifyTimestamp();
	}
	
	public void save(EdocObjTeam edocObjTeam)
	{
		edocObjTeamDao.save(edocObjTeam);
		updateModifyTimestamp();
	}
	public void update(EdocObjTeam edocObjTeam)
	{
		edocObjTeamDao.update(edocObjTeam);
		updateModifyTimestamp();
	}
	
	private void updateModifyTimestamp(){
		modifyTimestamp.put(OBJTEAMMODIFYDATE, new Date());
	}
	
	public boolean isModifyExchangeAccounts(Date orginalTimestamp){
		return !modifyTimestamp.get(OBJTEAMMODIFYDATE).equals(orginalTimestamp);
	}
	
	public Date getLastModifyTimestamp(){
		return this.modifyTimestamp.get(OBJTEAMMODIFYDATE);
	}
	
    public void initialize(){      
        
        CacheAccessable factory = CacheFactory.getInstance(EdocObjTeamManagerImpl.class); 
        
        modifyTimestamp = factory.createMap("modifyTimestamp");
        
        modifyTimestamp.put(OBJTEAMMODIFYDATE, new Date());
        
	} 
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public String ajaxGetByName(String orgName,String accountIdStr)
	{
		String retStr="-1";
		Long accountId=Long.parseLong(accountIdStr);
		EdocObjTeam et=edocObjTeamDao.findByAccountAndName(accountId, orgName);
		if(et!=null){retStr=et.getId().toString();}
		return retStr;
	}

	@Override
	public Map<Long,String> getOrgTeamForDepartment(String ids,Long loginAccountId) {
		return edocObjTeamDao.getOrgTeamForDepartment(ids,loginAccountId);
	}
}
