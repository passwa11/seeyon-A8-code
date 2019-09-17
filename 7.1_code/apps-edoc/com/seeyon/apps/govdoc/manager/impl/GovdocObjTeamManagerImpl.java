package com.seeyon.apps.govdoc.manager.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.govdoc.manager.GovdocObjTeamManager;
import com.seeyon.apps.govdoc.vo.OrgTeamVo;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgUnit;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.v3x.edoc.domain.EdocObjTeam;
import com.seeyon.v3x.edoc.domain.EdocObjTeamMember;
import com.seeyon.v3x.edoc.manager.EdocObjTeamManager;

/**
 * 新公文机构组管理类
 * @author 唐桂林
 *
 */
public class GovdocObjTeamManagerImpl implements GovdocObjTeamManager {
	
	private EdocObjTeamManager edocObjTeamManager;
	private OrgManager orgManager;
	
	@Override
	@SuppressWarnings("unchecked")
	public FlipInfo findList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		if(flipInfo == null) {
			flipInfo = new FlipInfo();
		}
		Long domainId = null;
		if(condition!=null && condition.containsKey("domainId")) {
			domainId = Long.parseLong(condition.get("domainId"));
		}
		if(domainId == null) {
			domainId = AppContext.currentAccountId();
		}
		
		String hql = "from EdocObjTeam where orgAccountId = :orgAccountId and state=:state order by sortId asc, createTime desc";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("orgAccountId", domainId);
		paramMap.put("state", EdocObjTeam.STATE_USE);
		List<EdocObjTeam> result = DBAgent.find(hql, paramMap,flipInfo);
		
		if(Strings.isNotEmpty(result)) {
			for(EdocObjTeam bean : result) {
				if(Strings.isNotEmpty(bean.getEdocObjTeamMembers())) {
					StringBuilder members = new StringBuilder();
					for(EdocObjTeamMember member : bean.getEdocObjTeamMembers()) {
						if(Strings.isNotBlank(members.toString())) {
							members.append("、");
						}
						V3xOrgUnit unit = orgManager.getUnitById(member.getMemberId());
						if(unit != null) {
							members.append(unit.getName());
						}
					}
					bean.setSelObjsStr(members.toString());
				}
			}
			flipInfo.setData(result);
		}
		
		return flipInfo;
	}
	
	@Override
	public EdocObjTeam getObjTeamById(Long teamId) throws BusinessException {
		return edocObjTeamManager.getById(teamId);
	}

	@Override
    public EdocObjTeam getObjTeamById(Long teamId, String type) throws BusinessException {
        EdocObjTeam eot = getObjTeamById(teamId);
        List<OrgTeamVo> tVos = new ArrayList<OrgTeamVo>();
        for (EdocObjTeamMember etm : eot.getEdocObjTeamMembers()) {
            OrgTeamVo tVo = new OrgTeamVo();
            //需要判断当前单位或部门是否已经停用
			V3xOrgEntity entity = orgManager.getEntity(etm.getTeamType() + "|" + etm.getMemberId());
			if (entity != null) {
				tVo.setEnabled(entity.getEnabled());
			}
            //ocip 如果组装的名字为空，ocip服务可能断掉了，跳过
            String showOrgEntities = Functions.showOrgEntities(etm.toObjStr(), "|");
            if(null==showOrgEntities||showOrgEntities.isEmpty()){
            	continue;
            }
			tVo.setTitle(showOrgEntities);
            tVo.setAccount(AppContext.currentAccountId());
            if ("department".equalsIgnoreCase(etm.getTeamType()))
                tVo.setAccount(Functions.getDepartment(etm.getMemberId()).getOrgAccountId());
            tVo.setType(etm.getTeamType());
            tVo.setValue(etm.getMemberId());
            tVos.add(tVo);
        }
        eot.setOrgTeamVos(tVos);
        return eot;
    }
    @Override
	public void deleteByMemberId(Long memberId){
		String hql = "delete from EdocObjTeamMember where memberId= :memberId";
		Map  pMap = new HashMap();
		pMap.put("memberId", memberId);
		DBAgent.bulkUpdate(hql,pMap);
	}
    @Override
    public void saveObjTeam(EdocObjTeam po) throws BusinessException {
		edocObjTeamManager.save(po);
	}
	
    @Override
	public void updateObjTeam(EdocObjTeam po) throws BusinessException {
		edocObjTeamManager.update(po);
	}
    
    @AjaxAccess
    @Override
    public String ajaxGetByName(String orgName, String accountIdStr) {
    	return edocObjTeamManager.ajaxGetByName(orgName, accountIdStr);
    }
	
    @Override
	public void deleteObjTeam(String ids) throws BusinessException {
		edocObjTeamManager.delete(ids);
	}

	public void setEdocObjTeamManager(EdocObjTeamManager edocObjTeamManager) {
		this.edocObjTeamManager = edocObjTeamManager;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	
}
