
package com.seeyon.ctp.organization.manager;

import com.seeyon.apps.addressbook.manager.AddressBookCustomerFieldInfoManager;
import com.seeyon.apps.addressbook.manager.AddressBookManager;
import com.seeyon.apps.addressbook.po.AddressBook;
import com.seeyon.apps.ldap.config.LDAPConfig;
import com.seeyon.apps.ldap.util.Authenticator;
import com.seeyon.apps.ldap.util.LDAPTool;
import com.seeyon.apps.ldap.util.LdapUtils;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.Constants.LoginOfflineOperation;
import com.seeyon.ctp.common.constants.CustomizeConstants;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.LocaleContext;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.init.MclclzUtil;
import com.seeyon.ctp.common.metadata.bo.MetadataColumnBO;
import com.seeyon.ctp.common.po.usermapper.CtpOrgUserMapper;
import com.seeyon.ctp.common.usermapper.dao.UserMapperDao;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.login.online.OnlineRecorder;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.OrgConstants.*;
import com.seeyon.ctp.organization.bo.*;
import com.seeyon.ctp.organization.bo.OrganizationMessage.MessageStatus;
import com.seeyon.ctp.organization.bo.OrganizationMessage.OrgMessage;
import com.seeyon.ctp.organization.dao.OrgCache;
import com.seeyon.ctp.organization.dao.OrgDao;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.enums.RoleTypeEnum;
import com.seeyon.ctp.organization.event.*;
import com.seeyon.ctp.organization.po.*;
import com.seeyon.ctp.organization.principal.NoSuchPrincipalException;
import com.seeyon.ctp.organization.principal.PrincipalManager;
import com.seeyon.ctp.organization.util.CheckMemberDelete;
import com.seeyon.ctp.organization.util.CheckPrivUpdate;
import com.seeyon.ctp.permission.bo.LicenseConst;
import com.seeyon.ctp.portal.api.SpaceApi;
import com.seeyon.ctp.privilege.dao.RoleMenuDao;
import com.seeyon.ctp.privilege.po.PrivRoleMenu;
import com.seeyon.ctp.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.*;

public class OrgManagerDirectImpl implements OrgManagerDirect {

    private final static Log   log = LogFactory.getLog(OrgManagerDirectImpl.class);

    protected OrgDao           orgDao;
    protected OrgManager       orgManager;
    protected PrincipalManager principalManager;
    protected OrgCache         orgCache;
    protected RoleMenuDao  roleMenuDao;
    protected RoleManager      roleManager;
    protected SpaceApi     spaceApi;
    protected EnumManager      enumManagerNew;
    protected AppLogManager    appLogManager;
    private AddressBookCustomerFieldInfoManager addressBookCustomerFieldInfoManager;
    private AddressBookManager addressBookManager;
    
    private List<CheckPrivUpdate> checkPrivUpdates;

    public AppLogManager getAppLogManager() {
        return appLogManager;
    }

    public void setEnumManagerNew(EnumManager enumManagerNew) {
        this.enumManagerNew = enumManagerNew;
    }
    
    public void setAppLogManager(AppLogManager appLogManager) {
        this.appLogManager = appLogManager;
    }
    public void setPrincipalManager(PrincipalManager principalManager) {
        this.principalManager = principalManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setOrgDao(OrgDao orgDao) {
        this.orgDao = orgDao;
    }

    public void setOrgCache(OrgCache orgCache) {
        this.orgCache = orgCache;
    }
    public void setRoleMenuDao(RoleMenuDao roleMenuDao) {
        this.roleMenuDao = roleMenuDao;
    }

    public void setRoleManager(RoleManager roleManager) {
        this.roleManager = roleManager;
    }
    public void setSpaceApi(SpaceApi spaceApi) {
        this.spaceApi = spaceApi;
    }

    public void setAddressBookCustomerFieldInfoManager(
			AddressBookCustomerFieldInfoManager addressBookCustomerFieldInfoManager) {
		this.addressBookCustomerFieldInfoManager = addressBookCustomerFieldInfoManager;
	}

	public void setAddressBookManager(AddressBookManager addressBookManager) {
		this.addressBookManager = addressBookManager;
	}

	private List<CheckPrivUpdate> getCheckPrivUpdates(){
        if(checkPrivUpdates == null){
            Map<String, CheckPrivUpdate> initsMap = AppContext.getBeansOfType(CheckPrivUpdate.class);
            checkPrivUpdates = new ArrayList<CheckPrivUpdate>(initsMap.values());
        }
        
        return checkPrivUpdates;
    }
    private static final Class<?> c1 = MclclzUtil.ioiekc("com.seeyon.ctp.permission.bo.LicensePerInfo");

    /**
     * 
     * @throws BusinessException
     */
    public void init() throws BusinessException{
        //初始化系統預置角色
//        List<OrgUnit> allAccounts = orgDao.getAllUnitPO(OrgConstants.UnitType.Account, null, true, true, null, null, null);
//        for (OrgUnit account : allAccounts) {
//            if(!account.isGroup()){
//                addAccountDefaultRoles(account.getId());
//            }
//        }
    }

    /***************************************/
    
    @Override
    public void deleteOrgRelationshipById(Long id) throws BusinessException {
        if(null == id) {
            throw new BusinessException("实体关系id为空异常！");
        }
        orgDao.deleteOrgRelationshipPOById(id);
    }

    @Override
    public void deleteOrgRelationship(V3xOrgRelationship rel) throws BusinessException {
        if (null == rel || null == rel.getKey()) {
            throw new BusinessException("关系实体异常，实体或实体属性key为空!");
        }
        orgDao.deleteOrgRelationshipPO(rel.getKey(), rel.getSourceId(), rel.getOrgAccountId(), null);
    }
    public void deleteOrgRelationships(List<V3xOrgRelationship> rels) throws BusinessException {
        List<OrgRelationship> porels = new UniqueList<OrgRelationship>();
        for(V3xOrgRelationship rel:rels){
            porels.add((OrgRelationship)rel.toPO());
        }
       if(rels.size()>0){
           orgDao.deleteOrgRelationshipPOs(porels);
       }

    }

    @Override
    public void deleteRelsInList(List<Long> sourceIds, String key) throws BusinessException {
        for (Long sourceId : sourceIds) {
            orgDao.deleteOrgRelationshipPO(key, sourceId, null, null);
        }
    }

    @Override
    public V3xOrgTeam addTeam(V3xOrgTeam team) throws BusinessException {
        if (null == team) {
            throw new BusinessException("新增组实体为空！");
        }
        /**
         * 重名比较原则
         * 1.同一个单位（集团）下的组比较
         * 2.除了个人组，其他组之间名称不能重复
         * 3.不同人的个人组名称可以重复
         */
      	List<V3xOrgTeam> allEnts = orgManager.getAllTeams(team.getOrgAccountId());
      	int teamType = team.getType();
      	String teamName = team.getName();
      	Long teamOwnerId = team.getOwnerId();
      	for(V3xOrgTeam ent : allEnts){
      		int type = ent.getType();
      		String name = ent.getName();
      		if(OrgConstants.TEAM_TYPE.PERSONAL.ordinal() == teamType){
      			//个人组只和自己创建的个人组之间进行比较
      			if(OrgConstants.TEAM_TYPE.PERSONAL.ordinal() == type && teamName.equals(name) && teamOwnerId.equals(ent.getOwnerId())){
      				throw new BusinessException(ResourceUtil.getString("org.validate.team1"));
      			}
      		}else{
      			if(OrgConstants.TEAM_TYPE.PERSONAL.ordinal() != type && teamName.equals(name)){
      				throw new BusinessException(ResourceUtil.getString("org.validate.team2"));
      			}
      			
      		}
      		
      	}
      	
        List<OrgTeam> list = new ArrayList<OrgTeam>(1);
        list.add((OrgTeam) team.toPO());
        // 实例化
        orgDao.insertOrgTeam(list);
        // 实例化组员到关系表中
        dealTeamMemberRels(team);
        
        // 3、触发事件
        AddTeamEvent event = new AddTeamEvent(this);
        event.setTeam(team);
        EventDispatcher.fireEvent(event);
        
        return team;
    }
    
    /**
     * 组装组的组员的数据更新到关系表数据
     * 新建组和更新组
     * @throws BusinessException 
     */
    private void dealTeamMemberRels(V3xOrgTeam team) throws BusinessException {
        // 添加组关系表
        List<V3xOrgRelationship> rels = new ArrayList<V3xOrgRelationship>();
        if (team.getDepId().equals(V3xOrgEntity.DEFAULT_NULL_ID)) {
            team.setDepId(team.getOrgAccountId());
        }
        List<OrgTypeIdBO> members = team.getMemberList(OrgConstants.TeamMemberType.Member.ordinal());
        List<OrgTypeIdBO> leaders = team.getMemberList(OrgConstants.TeamMemberType.Leader.ordinal());
        List<OrgTypeIdBO> supervs = team.getMemberList(OrgConstants.TeamMemberType.SuperVisor.ordinal());
        List<OrgTypeIdBO> relatives = team.getMemberList(OrgConstants.TeamMemberType.Relative.ordinal());

        long i = 1;
        
        for (Iterator<OrgTypeIdBO> it = leaders.iterator(); it.hasNext();) {
            V3xOrgRelationship rel = new V3xOrgRelationship();
            OrgTypeIdBO bo = it.next();
            rel.setSourceId(team.getId());
            rel.setObjective0Id(bo.getLId());
            rel.setKey(RelationshipType.Team_Member.name());
            rel.setObjective5Id(OrgConstants.TeamMemberType.Leader.name());//objectId5==Leader
            rel.setObjective6Id(bo.getType());
            rel.setOrgAccountId(team.getOrgAccountId());
            rel.setSortId(i++);
            rels.add(rel);
        }
        for (Iterator<OrgTypeIdBO> it = members.iterator(); it.hasNext();) {
            V3xOrgRelationship rel = new V3xOrgRelationship();
            OrgTypeIdBO bo = it.next();
            String type = bo.getType();
            rel.setSourceId(team.getId());
            if(ORGENT_TYPE.Department_Post.name().equals(type)){
            	rel.setObjective0Id(bo.getDepartmentId());
            	rel.setObjective1Id(bo.getPostId());
            }else{
            	rel.setObjective0Id(bo.getLId());
            }
            rel.setKey(RelationshipType.Team_Member.name());//key==Team_Member
            rel.setObjective5Id(OrgConstants.TeamMemberType.Member.name());//objectId5==Member
            rel.setOrgAccountId(team.getOrgAccountId());
            rel.setObjective6Id(bo.getType());
            rel.setObjective7Id(bo.getInclude());
            rel.setSortId(i++);
            rels.add(rel);
        }
        for (Iterator<OrgTypeIdBO> it = supervs.iterator(); it.hasNext();) {
            V3xOrgRelationship rel = new V3xOrgRelationship();
            OrgTypeIdBO bo = it.next();
            rel.setSourceId(team.getId());
            rel.setObjective0Id(bo.getLId());
            rel.setKey(RelationshipType.Team_Member.name());
            rel.setObjective5Id(OrgConstants.TeamMemberType.SuperVisor.name());//objectId5==SuperVisor
            rel.setOrgAccountId(team.getOrgAccountId());
            rel.setObjective6Id(bo.getType());
            rel.setSortId(i++);
            rels.add(rel);
        }
        for (Iterator<OrgTypeIdBO> it = relatives.iterator(); it.hasNext();) {
            V3xOrgRelationship rel = new V3xOrgRelationship();
            OrgTypeIdBO bo = it.next();
            rel.setSourceId(team.getId());
            rel.setObjective0Id(bo.getLId());
            rel.setKey(RelationshipType.Team_Member.name());
            rel.setObjective5Id(OrgConstants.TeamMemberType.Relative.name());//objectId5==Relative
            rel.setOrgAccountId(team.getOrgAccountId());
            rel.setObjective6Id(bo.getType());
            rel.setSortId(i++);
            rels.add(rel);
        }
        addOrgRelationships(rels);
    }

    @Override
    public V3xOrgMember getUnAssignedMemberById(Long id) throws BusinessException {
        if (null == id) {
            throw new BusinessException("人员id为空！");
        }
        OrgMember memberPO = orgDao.getOrgMemberPO(id);
        if (null == memberPO) {
            throw new BusinessException("按照id没有查询到指定的人！");
        }
        V3xOrgMember member = new V3xOrgMember(memberPO);
        if (!member.getIsAssigned() && member.isValid()) {
            return member;
        }
        throw new BusinessException("按照id没有找到符合条件的人！");
    }

    //批量新增关系
    @Override
    public void addOrgRelationships(List<V3xOrgRelationship> rels) throws BusinessException {
        List<OrgRelationship> poList = new ArrayList<OrgRelationship>();
        for (V3xOrgRelationship bo : rels) {
            poList.add((OrgRelationship) bo.toPO());
        }
        // 添加实体到数据库
        orgDao.insertOrgRelationship(poList);
    }

    @Override
    public V3xOrgRole addRole(V3xOrgRole role) throws BusinessException {
        if (null == role) {
            throw new BusinessException("实体对象为空！");
        }
        //实例化
        List<OrgRole> poList = new ArrayList<OrgRole>();
        poList.add((OrgRole) role.toPO());
        orgDao.insertOrgRole(poList);
        return role;
    }


    @Override
    public void addDepartmentPost(List<V3xOrgPost> posts, Long depId) throws BusinessException {
        if (null == depId) {
            throw new BusinessException("传入部门ID为空");
        }
        V3xOrgDepartment dept = orgManager.getDepartmentById(depId);
        if (dept == null) {
            throw new BusinessException("部门不存在！");
        }

        List<V3xOrgPost> oldPosts = orgManager.getDepartmentPost(depId);
        if(CollectionUtils.isEqualCollection(oldPosts, posts)){
        	return;
        }

        //List<OrgPost> postPOList = new ArrayList<OrgPost>();
        List<OrgRelationship> relPOList = new ArrayList<OrgRelationship>();
        //删除已有关系
        orgDao.deleteOrgRelationshipPO(RelationshipType.Department_Post.name(), dept.getId(), null, null);
        //传入空则只做删除
        if (posts == null) {
            return;
        }
        //主要维护关系表
        Long i=0L;
        for (V3xOrgPost post : posts) {
            OrgRelationship rel = new OrgRelationship();
            rel.setIdIfNew();
            rel.setType(RelationshipType.Department_Post.name());
            rel.setSourceId(depId);
            rel.setObjective0Id(post.getId());
            rel.setOrgAccountId(dept.getOrgAccountId());
            rel.setCreateTime(new Date());
            rel.setUpdateTime(new Date());
            rel.setSortId(i++);
            relPOList.add(rel);
        }
        orgDao.insertOrgRelationship(relPOList);
    }

    @Override
    public void incrementDepartmentPost(List<V3xOrgPost> posts, Long depId) throws BusinessException {
        if (null == depId) {
            throw new BusinessException("传入部门ID为空");
        }
        V3xOrgDepartment dept = orgManager.getDepartmentById(depId);
        if (dept == null) {
            throw new BusinessException("部门不存在！");
        }
        //List<OrgPost> postPOList = new ArrayList<OrgPost>();
        List<OrgRelationship> relPOList = new ArrayList<OrgRelationship>();
        //先删除已经存在的部门和要增量添加的岗位之间的关系
        List<Long> postIds = new ArrayList<Long>();
        for(V3xOrgPost p : posts){
        	postIds.add(p.getId());
        }

        EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(RelationshipObjectiveName.class);
        objectiveIds.put(RelationshipObjectiveName.objective0Id, postIds);
        orgDao.deleteOrgRelationshipPO(RelationshipType.Department_Post.name(), dept.getId(), null, objectiveIds);
        //传入空则只做删除
        if (posts == null) {
            return;
        }
        //主要维护关系表
        for (V3xOrgPost post : posts) {
            OrgRelationship rel = new OrgRelationship();
            rel.setIdIfNew();
            rel.setType(RelationshipType.Department_Post.name());
            rel.setSourceId(depId);
            rel.setObjective0Id(post.getId());
            rel.setOrgAccountId(dept.getOrgAccountId());
            rel.setCreateTime(new Date());
            rel.setUpdateTime(new Date());
            relPOList.add(rel);
        }
        orgDao.insertOrgRelationship(relPOList);
    }

    @Override
    public void addTeamScope(List<? extends V3xOrgEntity> ents, V3xOrgTeam team) throws BusinessException {

        List<OrgRelationship> relPOList = new ArrayList<OrgRelationship>();
        //删除已有关系
        orgDao.deleteOrgRelationshipPO(RelationshipType.Team_PubScope.name(), team.getId(), team.getOrgAccountId(), null);
        //主要维护关系表
        for (Object ent : ents) {
            OrgRelationship rel = new OrgRelationship();
            rel.setIdIfNew();
            rel.setType(RelationshipType.Team_PubScope.name());
            rel.setSourceId(team.getId());
            rel.setObjective0Id(((V3xOrgEntity)ent).getId());
            rel.setOrgAccountId(team.getOrgAccountId());
            rel.setObjective5Id(OrgHelper.boTostr((V3xOrgEntity)ent));
            rel.setCreateTime(new Date());
            rel.setUpdateTime(new Date());
            relPOList.add(rel);
        }

        orgDao.insertOrgRelationship(relPOList);
    }
    @Override
    public void addTeamMembers(List<V3xOrgMember> members, V3xOrgTeam team,String membertype) throws BusinessException {

    	//组成员最多为500
    	if(members.size()>500){
    		throw new BusinessException("组成员最大人数不能超过500，操作失败！");
    	}
        //List<OrgTeam> teamPOList = new ArrayList<OrgTeam>();
        List<OrgRelationship> relPOList = new ArrayList<OrgRelationship>();
        EnumMap<RelationshipObjectiveName, Object> enummap = new EnumMap<RelationshipObjectiveName, Object>(RelationshipObjectiveName.class);
        enummap.put(RelationshipObjectiveName.objective5Id, membertype);
        //删除已有关系
        orgDao.deleteOrgRelationshipPO(RelationshipType.Team_Member.name(), team.getId(), team.getOrgAccountId(), enummap);
        //主要维护关系表
        long i = 1;
        for (V3xOrgMember member : members) {
            OrgRelationship rel = new OrgRelationship();
            rel.setIdIfNew();
            rel.setSortId(i++);
            rel.setType(RelationshipType.Team_Member.name());
            rel.setSourceId(team.getId());
            rel.setObjective0Id(member.getId());
            rel.setOrgAccountId(team.getOrgAccountId());
            rel.setObjective5Id(membertype);
            rel.setCreateTime(new Date());
            rel.setUpdateTime(new Date());
            relPOList.add(rel);
        }
        orgDao.insertOrgRelationship(relPOList);
    }
	@Override
    public List<V3xOrgEntity> getTeamMembers(V3xOrgTeam team,String membertype) throws BusinessException {

    	List<V3xOrgEntity> postlist = new ArrayList<V3xOrgEntity>();
    	EnumMap<RelationshipObjectiveName, Object> enummap = new EnumMap<RelationshipObjectiveName, Object>(RelationshipObjectiveName.class);
        enummap.put(RelationshipObjectiveName.objective5Id, membertype);
    	List<V3xOrgRelationship> rellist = orgCache.getV3xOrgRelationship(RelationshipType.Team_Member, team.getId(), team.getOrgAccountId(), enummap);
    	Collections.sort(rellist, CompareSortRelationship.getInstance());
    	for (V3xOrgRelationship orgRelationship : rellist) {
    		postlist.add(orgManager.getEntityById(OrgHelper.getV3xClass(orgRelationship.getObjective6Id()), orgRelationship.getObjective0Id()));
		}
    	return postlist;
    }

	@Override
    public Map<String,String> getTeamsMember(V3xOrgTeam team,String membertype) throws BusinessException {
		Map map = new HashMap<String, String>();
		String ids="";
		String names="";
    	EnumMap<RelationshipObjectiveName, Object> enummap = new EnumMap<RelationshipObjectiveName, Object>(RelationshipObjectiveName.class);
        enummap.put(RelationshipObjectiveName.objective5Id, membertype);
        List<V3xOrgRelationship> rellist = orgCache.getV3xOrgRelationship(RelationshipType.Team_Member, team.getId(), team.getOrgAccountId(), enummap);
        Collections.sort(rellist, CompareSortRelationship.getInstance());
    	for (V3xOrgRelationship orgRelationship : rellist) {
    		String type  = orgRelationship.getObjective6Id();
    		if(ORGENT_TYPE.Department_Post.name().equals(type)){
    			Long deptId = orgRelationship.getObjective0Id();
    			Long postId = orgRelationship.getObjective1Id();
    			V3xOrgEntity dept = orgManager.getEntityById(OrgHelper.getV3xClass(ORGENT_TYPE.Department.name()), deptId);
    			if(dept == null){
    				log.warn("组成员,部门下的岗位，部门为空：" + team.getId() + "," + orgRelationship.getObjective6Id() + "," + deptId);
    				continue;
    			}
    			V3xOrgEntity post = orgManager.getEntityById(OrgHelper.getV3xClass(ORGENT_TYPE.Post.name()), postId);
    			if(post == null){
    				log.warn("组成员，部门下的岗位，岗位为空：" + team.getId() + "," + orgRelationship.getObjective6Id() + "," + postId);
    				continue;
    			}

    			if(Strings.isBlank(ids)){
    				ids = type+"|"+deptId+"_"+postId;
    				names = dept.getName()+"-"+post.getName();
    			}else{
    				ids = ids + "," + type+"|"+deptId+"_"+postId;
    				names = names + "," + dept.getName()+"-"+post.getName();
    			}

    		}else{
    			List<EntityIdTypeDsBO> postlist = new ArrayList<EntityIdTypeDsBO>();
    			V3xOrgEntity entity = orgManager.getEntityById(OrgHelper.getV3xClass(orgRelationship.getObjective6Id()), orgRelationship.getObjective0Id());
    			if(entity == null){
    				log.warn("组成员为空：" + team.getId() + "," + orgRelationship.getObjective6Id() + "," + orgRelationship.getObjective0Id());
    				continue;
    			}

    			EntityIdTypeDsBO en = new EntityIdTypeDsBO();
    			en.setId(entity.getId());
    			en.setEntity(entity);
    			en.setDsc(orgRelationship.getObjective6Id());
    			en.setDscType(orgRelationship.getObjective7Id());
    			postlist.add(en);
    			if(Strings.isBlank(ids)){
    				ids = OrgHelper.parseElementsExt(postlist, "id", "dsc","dscType");
    				names = OrgHelper.showOrgEntitiesExt(postlist, "id", "dsc","dscType", null);
    			}else{
    				ids = ids +","+OrgHelper.parseElementsExt(postlist, "id", "dsc","dscType");
    				names = names +","+OrgHelper.showOrgEntitiesExt(postlist, "id", "dsc","dscType", null);
    			}
    		}
		}
    	map.put("value", ids);
    	map.put("text", names);

    	return map;
    }

    @Override
    public List<V3xOrgEntity> getTeamScope(V3xOrgTeam team) throws BusinessException {

    	List<V3xOrgEntity> postlist = new ArrayList<V3xOrgEntity>();
    	List<V3xOrgRelationship> rellist = orgCache.getV3xOrgRelationship(RelationshipType.Team_PubScope, team.getId(), team.getOrgAccountId(),null);
    	for (V3xOrgRelationship orgRelationship : rellist) {
    		postlist.add(orgManager.getEntity(orgRelationship.getObjective5Id(), orgRelationship.getObjective0Id()));
		}
    	return postlist;
    }


    @Override
    public void deleteConcurrentPost(Long id) throws BusinessException {
        if (null == id) {
            throw new BusinessException("具体关系实体id为空！");
        }
        OrgRelationship rel = orgDao.getEntity(OrgRelationship.class, id);
        if (null == rel)
            throw new BusinessException("根据ID查询关系实体为空！");
        orgDao.deleteOrgRelationshipPOById(id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void insertRepeatSortNum(String entityClassName, Long accountId, Long sortNum, Boolean isInternal) throws BusinessException {
    	this.insertRepeatSortNum(entityClassName, accountId, sortNum, isInternal,null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void insertRepeatSortNum(String entityClassName, Long accountId, Long sortNum, Boolean isInternal,Long selfEntityId) throws BusinessException {
        @SuppressWarnings("rawtypes")
        Class clazz = null;
        if (V3xOrgMember.class.getSimpleName().equals(entityClassName)) {
            clazz = V3xOrgMember.class;
        } else if (V3xOrgAccount.class.getSimpleName().equals(entityClassName)) {
            clazz = V3xOrgAccount.class;
        } else if (V3xOrgDepartment.class.getSimpleName().equals(entityClassName)) {
            clazz = V3xOrgDepartment.class;
        } else if (V3xOrgTeam.class.getSimpleName().equals(entityClassName)) {
            clazz = V3xOrgTeam.class;
        } else if (V3xOrgLevel.class.getSimpleName().equals(entityClassName)) {
            clazz = V3xOrgLevel.class;
        } else if (V3xOrgPost.class.getSimpleName().equals(entityClassName)) {
            clazz = V3xOrgPost.class;
        } else if (V3xOrgRole.class.getSimpleName().equals(entityClassName)) {
            clazz = V3xOrgRole.class;
        }
        //政务职务级别先不做考虑
        orgDao.insertRepeatSortNum(clazz, accountId, sortNum, isInternal, OrgConstants.ExternalType.Inner.ordinal(),selfEntityId);
    }

    @Override
    public void addUnOrganiseMember(V3xOrgMember member) throws BusinessException {
        this.addMember(member);
    }

    @Override
    public void updateUnOrganiseMember(V3xOrgMember member) throws BusinessException {
        this.updateMember(member);
    }

    @Override
    public void addBenchMarkPostRel(Long BenchMarkPostId, Long accountId) throws BusinessException {
        if (BenchMarkPostId != null && accountId != null) {
            V3xOrgPost benchMarkPost = orgManager.getPostById(BenchMarkPostId);
            if (benchMarkPost != null) {
                //在单位创建岗位
                V3xOrgPost post = new V3xOrgPost();
                post.setName(benchMarkPost.getName());
                post.setCode(benchMarkPost.getCode());
                post.setOrgAccountId(accountId);
                post.setTypeId(benchMarkPost.getTypeId());
                post.setSortId(benchMarkPost.getSortId());
                List<OrgPost> postPOList = new ArrayList<OrgPost>(1);
                postPOList.add((OrgPost) post.toPO());
                orgDao.insertOrgPost(postPOList);
                //创建基准岗关系
                V3xOrgRelationship rel = new V3xOrgRelationship();
                rel.setKey((RelationshipType.Banchmark_Post.name()));
                rel.setOrgAccountId(accountId);
                rel.setSourceId(post.getId());
                rel.setObjective0Id(BenchMarkPostId);
                rel.setCreateTime(new Date());
                rel.setUpdateTime(new Date());
                List<OrgRelationship> relPOList = new ArrayList<OrgRelationship>(1);
                relPOList.add((OrgRelationship) rel.toPO());
                orgDao.insertOrgRelationship(relPOList);
            }
        }
    }

    @Override
    public void updateExternalMemberWorkScope(Long memberId, List<V3xOrgRelationship> rels) throws BusinessException {
        if (null == memberId) {
            log.error("人员id为空！");
            throw new BusinessException("人员id为空！");
        }
        if (null != rels && rels.size() > 1) {
            orgDao.deleteOrgRelationshipPO(RelationshipType.External_Workscope.name(), memberId, null,
                    null);
            List<OrgRelationship> relPOList = new ArrayList<OrgRelationship>();
            for (V3xOrgRelationship rel : rels) {
                relPOList.add((OrgRelationship) rel.toPO());
            }
            orgDao.insertOrgRelationship(relPOList);
        }
    }

    @Override
    public void bandBmPost(Long postId, Long bmPostId) throws BusinessException {
        V3xOrgPost post = orgManager.getPostById(postId);
        orgDao.deleteOrgRelationshipPO(RelationshipType.Banchmark_Post.name(), post.getId(), null, null);
        V3xOrgRelationship rel = new V3xOrgRelationship();
        rel.setKey(RelationshipType.Banchmark_Post.name());
        rel.setOrgAccountId(post.getOrgAccountId());
        rel.setSourceId(post.getId());
        rel.setSortId(Long.valueOf(post.getSortId()));
        rel.setObjective0Id(bmPostId);
        rel.setCreateTime(new Date());
        rel.setUpdateTime(new Date());
        List<OrgRelationship> relPOList = new ArrayList<OrgRelationship>(1);
        relPOList.add((OrgRelationship) rel.toPO());
        orgDao.insertOrgRelationship(relPOList);
    }

    @Override
    public OrganizationMessage addAccount(V3xOrgAccount account, V3xOrgMember adminMember) throws BusinessException {
        OrganizationMessage message = new OrganizationMessage();
        // 校验登录名是否已存在
        if (principalManager.isExist(adminMember.getV3xOrgPrincipal().getLoginName())) {
            message.addErrorMsg(adminMember, MessageStatus.PRINCIPAL_REPEAT_NAME);
            return message;
        }

        //上級單位為空或者無效，不能建立下級單位
        if(null==account.getSuperior()){
        	message.addErrorMsg(account, MessageStatus.ACCOUNT_VALID_SUPERACCOUNT_DISABLE);
        	return message;
        }else{
        	V3xOrgAccount superAccount = orgManager.getAccountById(account.getSuperior());
        	if(null==superAccount || !superAccount.isValid()){
        		message.addErrorMsg(account, MessageStatus.ACCOUNT_VALID_SUPERACCOUNT_DISABLE);
        		return message;
        	}

        }

        message = this.addAccount(account);
        if(!message.getErrorMsgs().isEmpty()){
            return message;
        }


        adminMember.setIsAdmin(true);
        adminMember.setOrgAccountId(account.getId());

        //实例化单位管理员人员信息
        adminMember.setIsAdmin(true);
        adminMember.setOrgAccountId(account.getId());
        adminMember.setName(ResourceUtil.getString("org.account_form.adminName.value"));
        List<OrgMember> _relMember = new ArrayList<OrgMember>(1);
        _relMember.add((OrgMember) adminMember.toPO());
        orgDao.insertOrgMember(_relMember);
        //实例化管理员账号
        principalManager.insert(adminMember.getV3xOrgPrincipal());
        //TODO 这里ID临时写死，由于整个系统只有一个单位管理员
        addRole2Entity(-2989205846588111483L, account.getId(), adminMember);

        return message;
    }

    @Override
    public OrganizationMessage addAccount(V3xOrgAccount account) throws BusinessException {
        if(null == account) {
            log.error("创建单位失败，传入的单位实体为空！");
            throw new BusinessException("创建单位失败，传入的单位实体为空！");
        }
        /*****注册数校验*****/
        if(account.isValid()) {// OA-64434 单位注册数控制-启用单位数量达到单位注册数后，再新建1个停用的单位依然提示超过了系统许可数，期望能够新建成功
            int accountAllNums = orgCache.getAllAccounts().size() - 1;//不包括集团根
            Object info1;
            info1 = MclclzUtil.invoke(c1, "getInstance", new Class[] { String.class }, null, new Object[] { "" });
            //集团新建单位注册数
            int maxCompanySum = ((Integer) MclclzUtil.invoke(c1, "getMaxCompany", null, info1, null)).intValue();
            if(maxCompanySum != 0 && maxCompanySum < (accountAllNums + 1)) {//0代表无限制
                throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_MAX_PERISSION", maxCompanySum));
            }
        }
        /*****注册数校验结束*****/
        OrganizationMessage message = new OrganizationMessage();
        account.setIdIfNew();
        String path = OrgHelper.getPathByPid4Add(V3xOrgUnit.class, account.getSuperior());
        account.setPath(path);
        account.setType(UnitType.Account);
        //1、校验数据
        boolean isDuplicated = false;
        isDuplicated = isPropertyDuplicated(V3xOrgAccount.class.getSimpleName(), "name", account.getName());
        if (isDuplicated) {
            message.addErrorMsg(account, MessageStatus.ACCOUNT_REPEAT_NAME);
            return message;
        }
        //判断单位简称重复
        isDuplicated = isPropertyDuplicated(V3xOrgAccount.class.getSimpleName(), "shortName", account.getShortName());
        if (isDuplicated) {
            message.addErrorMsg(account, MessageStatus.ACCOUNT_REPEAT_SHORT_NAME);
            return message;
        }
        //判断单位编码是否重复
        isDuplicated = isPropertyDuplicated(V3xOrgAccount.class.getSimpleName(), "code", account.getCode());
        if (isDuplicated) {
            message.addErrorMsg(account, MessageStatus.ACCOUNT_REPEAT_CODE);
            return message;
        }
        //单位独立登录页地址校验
        if(account.isCustomLogin() && isCustomLoginDuplicated(account.getCustomLoginUrl(), null)) {
            message.addErrorMsg(account, MessageStatus.ACCOUNT_CUSTOM_LOGIN_URL_DUPLICATED);
            return message;
        }

        //sortId处理
        if(OrgConstants.SORTID_TYPE_INSERT.equals(account.getSortIdType())
                && this.isPropertyDuplicated(V3xOrgAccount.class.getSimpleName(), "sortId", account.getSortId())) {//插入
            orgDao.insertRepeatSortNum(V3xOrgAccount.class, null, account.getSortId(),null);
        }

        //新建单位初始化一些系统预置角色
        //this.addAccountDefaultRoles(account.getId());
        //同步集团角色
        saveSycGroupRole(account);

        //修改單位可見範圍處理代碼，步驟先刪除關係再新建
        List<OrgRelationship> accessScopePOs = this.dealAccessIds2Relationship(account);
        orgDao.deleteOrgRelationshipPO(RelationshipType.Account_AccessScope.name(), account.getId(), account.getId(), null);
        orgDao.insertOrgRelationship(accessScopePOs);
        account.setLevelScope(-1);
        //2、实例化
        List<OrgUnit> poList = new ArrayList<OrgUnit>(1);
        poList.add((OrgUnit) account.toPO());
        orgDao.insertOrgUnit(poList);

        //3、触发事件
        AddAccountEvent event = new AddAccountEvent(this);
        event.setAccount(account);
        try {
			EventDispatcher.fireEventWithException(event);
		} catch (Throwable e) {
			throw new BusinessException(e);
		}
        message.addSuccessMsg(account);
        return message;
    }

    /**
     * 检查单位自定义登录页地址保证不重复
     * @param customLoginUrl 单位登录地址
     * @param accountId 当前单位id
     * @return
     */
    private boolean isCustomLoginDuplicated(String customLoginUrl, Long accountId) {
        Map<String, Long> url2IDMap = new HashMap<String, Long>();
        List<V3xOrgAccount> allAccounts = orgCache.getAllAccounts();
        for (V3xOrgAccount a : allAccounts) {
            /* 1.单位开启自定义登录地址 0或null为未开启，1为开启
            *  2.customLoginUrl不为空
            */
            if(a.getId().equals(accountId)) continue;//OA-63061
            if(null != a.getProperty("isCustomLoginUrl")
                    && ((Long)a.getProperty("isCustomLoginUrl")).longValue() == 1
                    && null != a.getProperty("customLoginUrl")) {

                url2IDMap.put((String)a.getProperty("customLoginUrl"), a.getId());
            }
        }
        return url2IDMap.containsKey(customLoginUrl);
    }

    /**
     * 处理单位可见范围的id创建成关系POs列表数据用于保存<br>
     * 接口内部使用方法，请不要随意修改
     * @param account
     * @return
     */
    private List<OrgRelationship> dealAccessIds2Relationship(V3xOrgAccount account) {
        List<OrgRelationship> accessScopePOs = new ArrayList<OrgRelationship>();
        List<Long> accessIds = account.getAccessIds();
        if (null == accessIds || accessIds.size() == 0) {
            //实例化单位间访问权限，默认"全是"
            V3xOrgRelationship accessScope = new V3xOrgRelationship();
            accessScope.setKey(RelationshipType.Account_AccessScope.name());
            accessScope.setSourceId(account.getId());
            accessScope.setObjective0Id(null);//默认全部可见
            accessScope.setOrgAccountId(account.getId());
            accessScope.setObjective5Id(OrgConstants.Account_AccessScope_Type.CAN_ACCESS.name());
            accessScopePOs.add((OrgRelationship) accessScope.toPO());
        } else {
            //管理界面选择了具体单位id
            for (Long accessId : accessIds) {
                V3xOrgRelationship accessScope = new V3xOrgRelationship();
                accessScope.setKey(RelationshipType.Account_AccessScope.name());
                accessScope.setSourceId(account.getId());
                accessScope.setObjective0Id(accessId);//可见不可见的单位id
                accessScope.setOrgAccountId(account.getId());
                if (account.getIsCanAccess()) {
                    accessScope.setObjective5Id(OrgConstants.Account_AccessScope_Type.CAN_ACCESS.name());
                } else {
                    accessScope.setObjective5Id(OrgConstants.Account_AccessScope_Type.NOT_ACCESS.name());
                }
                accessScopePOs.add((OrgRelationship) accessScope.toPO());
            }
        }
        return accessScopePOs;
    }

    /**
     * 单位注册数校验任务项，不包含停用单位
     * 启用一个单位只有两个入口，集团管理员组织机构信息修改，单位管理员单位信息修改
     */
    private void checkAccountPrission4Update(V3xOrgAccount account) throws BusinessException {
        Object info1;
        info1 = MclclzUtil.invoke(c1, "getInstance", new Class[] { String.class }, null, new Object[] { "" });
        //集团启用单位的注册数
        int maxCompanySum = ((Integer) MclclzUtil.invoke(c1, "getMaxCompany", null, info1, null)).intValue();
        if(maxCompanySum != 0) {//0代表无限制
            int accountAllNums = orgCache.getAllAccounts().size() - 1;//不包含集团根，且包含停用的单位
            if(!account.isGroup() && account.isValid() && !(orgManager.getUnitById(account.getId()).isValid())) {
                if (maxCompanySum < (accountAllNums + 1)) {
                    throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_MAX_PERISSION", maxCompanySum));
                }
            }
        }
    }

    @Override
    public OrganizationMessage updateAccount(V3xOrgAccount account) throws BusinessException {
        List<V3xOrgAccount> accounts = new ArrayList<V3xOrgAccount>(1);
        accounts.add(account);
        this.checkAccountPrission4Update(account);
        return this.updateAccounts(accounts);
    }

    @Override
    public OrganizationMessage updateAccounts(List<V3xOrgAccount> accounts) throws BusinessException {
    	return updateAccounts(accounts,true);
    }

    @Override
    public OrganizationMessage updateAccounts(List<V3xOrgAccount> accounts,boolean dealAccess) throws BusinessException {
        OrganizationMessage message = new OrganizationMessage();
        for (V3xOrgAccount account : accounts) {
            if(!account.getEnabled()) {
                //单位停用的校验
                V3xOrgMember admin = orgManager.getAdministrator(account.getId());
                // 单位下存在未删除、启用的组织模型时不允许删除
                List<V3xOrgMember> members = getAllMembers(account.getId(), false);
                //单位管理人员中排除该单位的管理员
                if(members.contains(admin)) members.remove(admin);
                List<V3xOrgDepartment> depts = getAllDepartments(account.getId(), true, null, null, null, null);
                List<V3xOrgPost> posts = getAllPosts(account.getId(), false);
                List<V3xOrgTeam> teams = getAllTeams(account.getId(), false);

                List<V3xOrgAccount> accountsList = orgManager.getChildAccount(account.getId(), false);
                //存在未删除的人员
                if(members != null && members.size() > 0) {
                    message.addErrorMsg(account, MessageStatus.ACCOUNT_EXIST_MEMBER_ENABLE);
                    continue;
                }
                //存在未删除的部门
                if(depts != null && depts.size() > 0) {
                    message.addErrorMsg(account, MessageStatus.ACCOUNT_EXIST_DEPARTMENT_ENABLE);
                    continue;
                }
                //存在未删除的岗位
                if(posts != null && posts.size() > 0) {
                    message.addErrorMsg(account, MessageStatus.ACCOUNT_EXIST_POST_ENABLE);
                    continue;
                }
                //存在未删除的组
                if(teams != null && teams.size() > 0) {
                    List lteams = new ArrayList(teams.size());
                    for (V3xOrgTeam team : teams) {
                      if (team.isValid() && team.getType() == OrgConstants.TEAM_TYPE.SYSTEM.ordinal()) {
                        lteams.add(team);
                      }
                    }
                    if (!lteams.isEmpty()) {
                      message.addErrorMsg(account, MessageStatus.ACCOUNT_EXIST_TEAM);
                      continue;
                    }
                }
                //存在未删除的子单位
                if(accountsList != null && accountsList.size() > 0) {
                    message.addErrorMsg(account, MessageStatus.ACCOUNT_EXIST_CHILDACCOUNT_ENABLE);
                    continue;
                }
            }else if(!account.isGroup()){
            	V3xOrgAccount superAccount = orgManager.getAccountById(account.getSuperior());
            	if(superAccount == null || !superAccount.isValid()){
            		message.addErrorMsg(account, MessageStatus.ACCOUNT_VALID_SUPERACCOUNT_DISABLE);
            		continue;
            	}
            }
            /********************/

            account.setType(UnitType.Account);
            V3xOrgAccount oldAccount = orgManager.getEntityById(V3xOrgAccount.class, account.getId());
            if(AppContext.hasPlugin("accountbind")){
            	boolean isGroup = (Boolean) SysFlag.sys_isGroupVer.getFlag();
            	if(isGroup){
            		if(OrgConstants.GROUPID.equals(account.getId())){
            			account.setName(oldAccount.getName());
            		}
            	}else{
            		if(OrgConstants.ACCOUNTID.equals(account.getId())){
            			account.setName(oldAccount.getName());
            		}
            	}
            }
            //1、校验数据
            boolean isDuplicated = false;
            if(!oldAccount.getName().equals(account.getName())) {
                //判断重名
                isDuplicated = isPropertyDuplicated(V3xOrgAccount.class.getSimpleName(), "name", account.getName(),account.getId(),account.getId());
                if (isDuplicated) {
                    message.addErrorMsg(account, MessageStatus.ACCOUNT_REPEAT_NAME);
                    return message;
                }
            }
            if(null != oldAccount.getShortName() && !oldAccount.getShortName().equals(account.getShortName())) {
                //判断单位简称重复
                isDuplicated = isPropertyDuplicated(V3xOrgAccount.class.getSimpleName(), "shortName", account.getShortName(),account.getId(),account.getId());
                if (isDuplicated) {
                    message.addErrorMsg(account, MessageStatus.ACCOUNT_REPEAT_SHORT_NAME);
                    return message;
                }
            }
            if(null != oldAccount.getCode() && !oldAccount.getCode().equals(account.getCode())) {
              //判断单位编码是否重复
                isDuplicated = isPropertyDuplicated(V3xOrgAccount.class.getSimpleName(), "code", account.getCode(),account.getId(),account.getId());
                if (isDuplicated) {
                    message.addErrorMsg(account, MessageStatus.ACCOUNT_REPEAT_CODE);
                    return message;
                }
            }
            //单位独立登录地址保证不重复
            if(account.isCustomLogin() && null != account.getCustomLoginUrl()) {
                if(isCustomLoginDuplicated(account.getCustomLoginUrl(), account.getId())) {
                    message.addErrorMsg(account, MessageStatus.ACCOUNT_CUSTOM_LOGIN_URL_DUPLICATED);
                    return message;
                }
            }
            if(!oldAccount.getSuperior().equals(account.getSuperior())
                    || (!oldAccount.isValid() && account.isValid())) {
            	account.setPath(OrgHelper.getPathByPid4Add(V3xOrgAccount.class, account.getSuperior()));
            	//更新单位下子单位和部门的path
                List<V3xOrgUnit> childUnits = this.getChildUnitIncludeDis(account.getId());
            	for (V3xOrgUnit c : childUnits) {
            	    V3xOrgUnit oldC = orgCache.getV3xOrgUnitByPath(c.getPath());
            	    if(null == oldC) {
                        oldC = (V3xOrgUnit) OrgHelper.poTobo(orgDao.getEntity(OrgUnit.class, c.getId()));
                    }
                    c.setPath(c.getPath().replaceFirst(oldAccount.getPath(), account.getPath()));
                    orgDao.update((OrgUnit)OrgHelper.boTopo(c));
                    //UC发部门修改事件 //OA-52019
                    if (UnitType.Department.equals(c.getType())) {
                        // 触发事件
                        UpdateDepartmentEvent event = new UpdateDepartmentEvent(this);
                        event.setDept((V3xOrgDepartment) c);
                        event.setOldDept((V3xOrgDepartment) oldC);
                        EventDispatcher.fireEvent(event);
                    }
                    if (UnitType.Account.equals(c.getType())) {
                        // 触发事件
                        UpdateAccountEvent event = new UpdateAccountEvent(this);
                        event.setAccount((V3xOrgAccount) c);
                        event.setOldAccount((V3xOrgAccount) oldC);
                        EventDispatcher.fireEvent(event);
                    }
                }

            }
            //sortId处理
            if (OrgConstants.SORTID_TYPE_INSERT.equals(account.getSortIdType())
                    && this.isPropertyDuplicated(V3xOrgAccount.class.getSimpleName(), "sortId", account.getSortId(),
                            account.getId(), account.getId()) && !account.isGroup()) {//插入
                orgDao.insertRepeatSortNum(V3xOrgAccount.class, account.getOrgAccountId(), account.getSortId(), null);
            }
            if(account.isGroup()) {
                account.setSortId(1L);//OA-57895
            }
            // 实例化数据
            account.setCreateTime(oldAccount.getCreateTime());
            orgDao.update((OrgUnit)OrgHelper.boTopo(account));

            //修改單位可見範圍處理代碼，步驟先刪除關係再新建
            if(dealAccess){
            	List<OrgRelationship> accessScopePOs = this.dealAccessIds2Relationship(account);
            	orgDao.deleteOrgRelationshipPO(RelationshipType.Account_AccessScope.name(), account.getId(), account.getId(), null);
            	orgDao.insertOrgRelationship(accessScopePOs);
            }

            message.addSuccessMsg(account);
            // 触发事件
            UpdateAccountEvent event = new UpdateAccountEvent(this);
            event.setAccount(account);
            event.setOldAccount(oldAccount);
            EventDispatcher.fireEvent(event);
        }
        return message;
    }


    @Override
    public OrganizationMessage deleteAccount(V3xOrgAccount account) throws BusinessException {
        List<V3xOrgAccount> accounts = new ArrayList<V3xOrgAccount>();
        accounts.add(account);
        return this.deleteAccounts(accounts);
    }

    /**
     * 刪除单位下默认的角色
     * @param accountId
     * @throws BusinessException
     */
    private void destroyAccountDefaultRole(Long accountId) throws BusinessException {
        List<V3xOrgRole> roles = this.getSystemRoleDefinitions(V3xOrgEntity.ROLETYPE_FIXROLE);
        for (V3xOrgRole v3xOrgRole : roles) {
            this.deleteRole(orgManager.getRoleByName(v3xOrgRole.getName(), accountId));
        }
    }

    /**
     * 根据单位ID获取根据xml配置新建单位时创建的单位默认角色列表
     * @param accountId 单位id
     * @return 某单位的默认单位角色列表
     * @throws BusinessException
     */
//    private List<V3xOrgRole> getAccountDefaultRoles(Long accountId) throws BusinessException {
//        List<V3xOrgRole> defaultRoles = new UniqueList<V3xOrgRole>();
//        List<V3xOrgRole> roles = this.getSystemRoleDefinitions(V3xOrgEntity.ROLETYPE_FIXROLE);
//        for (V3xOrgRole v3xOrgRole : roles) {
//            defaultRoles.add(orgManager.getRoleByName(v3xOrgRole.getName(), accountId));
//        }
//        return defaultRoles;
//    }
    private List<V3xOrgAccount> getChildAccountIncludeDis(Long accountId) throws BusinessException {
    	V3xOrgAccount currentAccount = orgCache.getV3xOrgEntity(V3xOrgAccount.class, accountId);
        //增加空防护，获取某个停用单位的子单位时，从缓存中获取不到
        if(null == currentAccount) {
        	currentAccount = (V3xOrgAccount) OrgHelper.poTobo(orgDao.getEntity(OrgUnit.class, accountId));
        }
        List<V3xOrgAccount> result = new ArrayList<V3xOrgAccount>(100);
        List<OrgUnit> allAccounts = orgDao.getAllUnitPO(UnitType.Account, null, null, null, null, null, null);
        for (OrgUnit o : allAccounts) {
            if(o.getPath().startsWith(currentAccount.getPath())&&!o.getPath().equals(currentAccount.getPath())){
                result.add(OrgHelper.cloneEntity((V3xOrgAccount)OrgHelper.poTobo(o)));
            }
        }
        return result;
    }
    @Override
    public OrganizationMessage deleteAccounts(List<V3xOrgAccount> accounts) throws BusinessException {
        OrganizationMessage message = new OrganizationMessage();

        aaa:
        for (V3xOrgAccount account : accounts) {

            V3xOrgMember admin = orgManager.getAdministrator(account.getId());
            // 单位下存在未删除、启用的组织模型时不允许删除
            List<V3xOrgMember> members = getAllMembers(account.getId(), false);
            //单位管理人员中排除该单位的管理员
            if(members.contains(admin)) members.remove(admin);
            List<V3xOrgDepartment> depts = getAllDepartments(account.getId(), true, null, null, null, null);
            List<V3xOrgPost> posts = getAllPosts(account.getId(), false);
            List<V3xOrgTeam> teams = getAllTeams(account.getId(), false);
            //角色需要特殊处理，新建单位时创建的角色在删除单位时不判断，单位新建的角色不允许删除
            //List<V3xOrgRole> roles = new UniqueList<V3xOrgRole>(getAllRoles(account.getId(), false));
            //List<V3xOrgRole> defaultRoles = getAccountDefaultRoles(account.getId());
            //roles.removeAll(defaultRoles);

            List<V3xOrgLevel> levels = getAllLevels(account.getId(), false);

            List<V3xOrgAccount> accountsList = getChildAccountIncludeDis(account.getId());
            //存在未删除的人员
            if(null != members && members.size() > 0) {
            	message.addErrorMsg(account, MessageStatus.ACCOUNT_EXIST_MEMBER);
                continue;
            }
            //存在未删除的部门
            if(null != depts && depts.size() > 0) {
            	message.addErrorMsg(account, MessageStatus.ACCOUNT_EXIST_DEPARTMENT);
            	continue;
            }
            //存在未删除的岗位
            if(null != posts && posts.size() > 0) {
            	message.addErrorMsg(account, MessageStatus.ACCOUNT_EXIST_POST);
            	continue;
            }
            //存在未删除的组
            if (null != teams && teams.size() > 0) {
                for (V3xOrgTeam team : teams) {
                    if (team.isValid() && team.getType() == OrgConstants.TEAM_TYPE.SYSTEM.ordinal()) {
                        message.addErrorMsg(account, MessageStatus.ACCOUNT_EXIST_TEAM);
                        continue aaa;
                    }
                }
            }
            //存在未删除的单位自建角色
//            if(roles != null && roles.size() > 0) {
//            	message.addErrorMsg(account, OrganizationMessage.MessageStatus.ACCOUNT_EXIST_ROLE);
//            	continue;
//            }
            //存在未删除的职务级别
            if(levels != null && levels.size() > 0) {
            	message.addErrorMsg(account, MessageStatus.ACCOUNT_EXIST_LEVEL);
            	continue;
            }
            //存在未删除的子单位
            if(accountsList != null && accountsList.size() > 0) {
            	message.addErrorMsg(account, MessageStatus.ACCOUNT_EXIST_CHILDACCOUNT);
            	continue;
            }
            this.updateEntity2Deleted(account);
            destroyAccountDefaultRole(account.getId());
            this.deleteMemberRoleRels2Entity(ORGENT_TYPE.Account, account.getId());
            //实例化
            orgDao.update((OrgUnit) account.toPO());
            //删除单位管理员用户和账号
            this.deleteMember(admin);
            //删除某单位内在关系表中得所有关系
            orgDao.deleteOrgRelationshipPOByAccountId(account.getId());
            message.addSuccessMsg(account);
            //触发事件
            DeleteAccountEvent event = new DeleteAccountEvent(this);
            event.setAccount(account);
            EventDispatcher.fireEvent(event);
        }

        return message;
    }

    @Override
    public OrganizationMessage addDepartment(V3xOrgDepartment dept) throws BusinessException {
        List<V3xOrgDepartment> depts = new ArrayList<V3xOrgDepartment>();
        depts.add(dept);
        return this.addDepartments(depts);
    }

    @Override
    public OrganizationMessage addDepartments(List<V3xOrgDepartment> depts) throws BusinessException {
        OrganizationMessage message = new OrganizationMessage();
        List<V3xOrgDepartment> newEnts = new ArrayList<V3xOrgDepartment>(depts);
        List<OrgUnit> poList = new ArrayList<OrgUnit>();

        //1、校验 数据（如果检查出错误数据，就从newEnts中移除掉，不进行操作。）
        for (V3xOrgDepartment dept : depts) {
            if (Strings.isNotBlank(dept.getCode()) && dept.getIsInternal()) {
                //判断部门编码是否重复
                boolean isDuplicated = isPropertyDuplicated(V3xOrgDepartment.class.getSimpleName(), "code",
                        dept.getCode(), dept.getOrgAccountId());
                if (isDuplicated) {
                    message.addErrorMsg(dept, MessageStatus.DEPARTMENT_REPEAT_CODE);
                    return message;
                }

                if(dept.getCode().length()>50){
                	throw new BusinessException(ResourceUtil.getString("import.code.length.beyond",ResourceUtil.getString("department.label"),"50"));
                }
            }
            if (Strings.isBlank(dept.getName())) {
            	continue;
            }

            V3xOrgUnit parent = orgManager.getUnitById(dept.getSuperior());

            if (parent == null || !parent.isValid()) {
                message.addErrorMsg(dept, MessageStatus.DEPARTMENT_PARENTDEPT_DISABLED);
                return message;
            }

            //校验同级部门名称是否重复
            List<V3xOrgDepartment> brother;
            if (dept.getIsInternal()) {
                brother = this.orgManager.getChildDepartments(dept.getSuperior(), true, true);
            } else {
                brother = this.orgManager.getChildDepartments(dept.getSuperior(), true, false);
            }
            boolean isduplicated = isExistRepeatProperty(brother, "name", dept.getName(), dept);
            if (isduplicated && dept.getIsInternal()) {
                //throw new BusinessException(OrganizationMessage.MessageStatus.DEPARTMENT_REPEAT_NAME.name());
                throw new BusinessException(ResourceUtil.getString("dept.name.repeat"));
            } else if (isduplicated && !dept.getIsInternal()) {
                throw new BusinessException(ResourceUtil.getString("dept.out.name.repeat"));
            }

            if(null == dept.getSortId()) {
                dept.setSortId(Long.valueOf(getMaxSortNum(V3xOrgDepartment.class.getSimpleName(), dept.getOrgAccountId())) + 1);
            }

            String path = OrgHelper.getPathByPid4Add(V3xOrgUnit.class, dept.getSuperior(),null);
            dept.setPath(path);
            dept.setIdIfNew();
            dept.setType(UnitType.Department);
            message.addSuccessMsg(dept);
            poList.add((OrgUnit) dept.toPO());

            this.orgCache.cacheUpdate(dept);
        }

        //实例化
        orgDao.insertOrgUnit(poList);

        for (V3xOrgDepartment dept : newEnts) {
            // 触发事件
            AddDepartmentEvent event = new AddDepartmentEvent(this);
            event.setDept(dept);
            EventDispatcher.fireEvent(event);
        }
        return message;
    }

    @Override
    public OrganizationMessage updateDepartmentNoEvent(V3xOrgDepartment dept) throws BusinessException {
        OrganizationMessage message = new OrganizationMessage();
        V3xOrgDepartment oldDept = orgManager.getEntityById(V3xOrgDepartment.class, dept.getId());
        List<V3xOrgDepartment> child = this.orgManager.getChildDepartments(dept.getId(), false);
        //如果更改了启用标志为启用，则要判断上级部门是否启用
        if(dept.getEnabled()){
            if(dept.getSuperior()==-1L||!orgManager.getUnitById(dept.getSuperior()).getEnabled()){
                message.addErrorMsg(dept, MessageStatus.DEPARTMENT_PARENTDEPT_DISABLED);
            }
        } else {
            //如果更改了启用标志停用，要停用所有的下级部门
            for (V3xOrgDepartment v3xOrgDepartment : child) {
                v3xOrgDepartment.setEnabled(false);
                updateDepartment(v3xOrgDepartment);
            }
            //如果更改了启用标志为停用，则部门下存在人员时不允许
            if(orgManager.getAllMembersByDepartmentBO(dept.getId()).size()>0){
                message.addErrorMsg(dept, MessageStatus.DEPARTMENT_EXIST_MEMBER);
            }

        }
        if(orgManager.getUnitById(dept.getSuperior()).getPath().contains(orgManager.getDepartmentById(dept.getId()).getPath())){
            message.addErrorMsg(dept, MessageStatus.DEPARTMENT_PARENTDEPT_ISCHILD);
        }
        String path = OrgHelper.getPathByPid4Add(V3xOrgUnit.class, dept.getSuperior());
        dept.setPath(path);
        //批量更新子部门的path
        List<V3xOrgUnit> childUnits = orgCache.getChildUnitsByPid(V3xOrgUnit.class, dept.getId());
        for (V3xOrgUnit c : childUnits) {
            c.setPath(c.getPath().replaceFirst(oldDept.getPath(), dept.getPath()));
            orgDao.update((OrgUnit)OrgHelper.boTopo(c));
        }
        //校验同级部门名称是否重复
        List<V3xOrgDepartment> brother = this.orgManager.getChildDepartments(dept.getSuperior(), true);

        boolean isduplicated = isExistRepeatProperty(brother, "name", dept.getName(), dept);
        if (isduplicated) {
            message.addErrorMsg(dept, MessageStatus.DEPARTMENT_REPEAT_NAME);
        }
        dept.setCreateTime(oldDept.getCreateTime());
        orgDao.update((OrgUnit) dept.toPO());
        message.addSuccessMsg(dept);
        return message;
    }

    @Override
    public OrganizationMessage updateDepartment(V3xOrgDepartment dept) throws BusinessException {
        List<V3xOrgDepartment> departments = new ArrayList<V3xOrgDepartment>(1);
        departments.add(dept);
        return this.updateDepartments(departments);
    }

    @Override
    public OrganizationMessage updateDepartments(List<V3xOrgDepartment> depts) throws BusinessException {
        OrganizationMessage message = new OrganizationMessage();

        for (V3xOrgDepartment dept : depts) {
            if (Strings.isNotBlank(dept.getCode()) && dept.getIsInternal()) {
                //判断部门编码是否重复
                boolean isDuplicated = isPropertyDuplicated(V3xOrgDepartment.class.getSimpleName(), "code",
                        dept.getCode(), dept.getOrgAccountId(), dept.getId());
                if (isDuplicated) {
                    message.addErrorMsg(dept, MessageStatus.DEPARTMENT_REPEAT_CODE);
                    return message;
                }

                if(dept.getCode().length()>50){
                	throw new BusinessException(ResourceUtil.getString("import.code.length.beyond",ResourceUtil.getString("department.label"),"50"));
                }
            }
            V3xOrgDepartment oldDept = orgManager.getEntityById(V3xOrgDepartment.class, dept.getId());
            //如果更改了启用标志为启用，则要判断上级部门是否启用
            if (dept.getEnabled()) {
                if (dept.getSuperior() == -1L || !orgManager.getUnitById(dept.getSuperior()).getEnabled()) {
                    message.addErrorMsg(dept, MessageStatus.DEPARTMENT_PARENTDEPT_DISABLED);
                    continue;
                }
            } else {
                //如果更改了启用标志停用，要停用所有的下级部门
            	List<V3xOrgDepartment> child = this.orgManager.getChildDepartments(dept.getId(), false);
                for (V3xOrgDepartment v3xOrgDepartment : child) {
                    if (v3xOrgDepartment.isValid()) {
                        message.addErrorMsg(dept, MessageStatus.DEPARTMENT_EXIST_DEPARTMENT_ENABLE);
                        OrgHelper.throwBusinessExceptionTools(message);
                    }
                    v3xOrgDepartment.setEnabled(false);
                    //v3xOrgDepartment.setPath(OrgHelper.getPathByPid4Add(V3xOrgUnit.class, dept.getId()));
                    OrgHelper.throwBusinessExceptionTools(updateDepartment(v3xOrgDepartment));
                }

                List<V3xOrgDepartment> extChild = this.orgManager.getChildDepartments(dept.getId(), true, false);
                //如果更改了启用标志停用,需要判断是否有外部的子单位存在
                for (V3xOrgDepartment v3xOrgDepartment : extChild) {
                    if (v3xOrgDepartment.isValid()) {
                        message.addErrorMsg(dept, MessageStatus.DEPARTMENT_EXIST_EXTDEPARTMENT_ENABLE);
                        OrgHelper.throwBusinessExceptionTools(message);
                    }
                }
                //如果更改了启用标志为停用，则部门下存在人员时不允许
                if (isExistMemberByDept(dept)) {
                    message.addErrorMsg(dept, MessageStatus.DEPARTMENT_EXIST_MEMBER);
                    continue;
                }
            }
        	if(orgManager.getUnitById(dept.getSuperior()).getPath().contains(orgManager.getDepartmentById(dept.getId()).getPath())){
        	    //message.addErrorMsg(dept, "");
        		message.addErrorMsg(dept, MessageStatus.DEPARTMENT_PARENTDEPT_ISCHILD);
        		continue;
            	//throw new BusinessException("上级部门不允许为本部门或其下级部门！");
            }

            //如果修改了上级单位再重新计算Path
        	List<V3xOrgUnit> childUnits = new ArrayList<V3xOrgUnit>();
            if (!Strings.equals(dept.getSuperior(), oldDept.getSuperior())
                    || (!oldDept.isValid() && dept.isValid())) {
                String path = OrgHelper.getPathByPid4Add(V3xOrgUnit.class, dept.getSuperior(),null);
                dept.setPath(path);
                //批量更新子部门的path
                if(!dept.getOrgAccountId().equals(oldDept.getOrgAccountId())){
                    childUnits = orgCache.getChildUnitsByPath(V3xOrgUnit.class, oldDept.getPath());
                }else{
                    childUnits = orgCache.getChildUnitsByPid(V3xOrgUnit.class, dept.getId());
                }

            } else {
                dept.setPath(oldDept.getPath());
            }

            //校验同级部门名称是否重复
            List<V3xOrgDepartment> brother;
            if (dept.getIsInternal()) {
                brother = this.orgManager.getChildDepartments(dept.getSuperior(), true, true);
            } else {
                brother = this.orgManager.getChildDepartments(dept.getSuperior(), true, false);
            }
            boolean isduplicated = isExistRepeatProperty(brother, "name", dept.getName(), dept);

            if (isduplicated) {

            	message.addErrorMsg(dept, MessageStatus.DEPARTMENT_REPEAT_NAME);
            	continue;
                //throw new BusinessException("同一级别不允许添加相同名称部门");
            }

            boolean isCDeptSp = dept.CreateDeptSpace();
            dept.setCreateTime(oldDept.getCreateTime());
            orgDao.update((OrgUnit) dept.toPO());
            message.addSuccessMsg(dept);
            dept.setCreateDeptSpace(isCDeptSp);
            //触发事件
            UpdateDepartmentEvent event = new UpdateDepartmentEvent(this);
            event.setDept(dept);
            event.setOldDept(oldDept);
            EventDispatcher.fireEvent(event);

            for (V3xOrgUnit c : childUnits) {
                V3xOrgUnit oldC = orgCache.getV3xOrgUnitByPath(c.getPath());
                c.setPath(c.getPath().replaceFirst(oldDept.getPath(), dept.getPath()));
                c.setOrgAccountId(dept.getOrgAccountId());
                orgDao.update((OrgUnit) OrgHelper.boTopo(c));
                //UC发部门修改事件 //OA-52019
                if(UnitType.Department.equals(c.getType())) {
                    // 触发事件
                    UpdateDepartmentEvent event1 = new UpdateDepartmentEvent(this);
                    event1.setDept((V3xOrgDepartment)c);
                    event1.setOldDept((V3xOrgDepartment)oldC);
                    EventDispatcher.fireEvent(event1);
                }
            }
        }
        return message;
    }

    @Override
    public OrganizationMessage deleteDepartment(V3xOrgDepartment dept) throws BusinessException {
        if (dept == null) {
            throw new BusinessException("实体对象为空！");
        }
        List<V3xOrgDepartment> deps = new ArrayList<V3xOrgDepartment>(1);
        deps.add(dept);
        return this.deleteDepartments(deps);
    }

    @Override
    public OrganizationMessage deleteDepartments(List<V3xOrgDepartment> depts) throws BusinessException {
        OrganizationMessage message = new OrganizationMessage();
        List<V3xOrgDepartment> deleteLists = new UniqueList<V3xOrgDepartment>();
        //1、校验 数据
        for (V3xOrgDepartment dept : depts) {
            //检查部门下是否存在成员
            if (isExistMemberByDept(dept)) {
            	//throw new BusinessException("部门下面存在人员，不允许删除！");
            	if(dept.getIsInternal()){
            		message.addErrorMsg(dept, MessageStatus.DEPARTMENT_EXIST_MEMBER);
            	}else{
            		message.addErrorMsg(dept, MessageStatus.ACCOUNT_EXIST_MEMBER_ENABLE);
            	}


                continue;
            }

            boolean hasExtChild = false;
            List<V3xOrgDepartment> extChild = this.orgManager.getChildDepartments(dept.getId(), true, false);
            //如果更改了启用标志停用,需要判断是否有外部的子单位存在
            for (V3xOrgDepartment v3xOrgDepartment : extChild) {
                if (v3xOrgDepartment.isValid()) {
                	hasExtChild = true;
                    message.addErrorMsg(dept, MessageStatus.DEPARTMENT_EXIST_EXTDEPARTMENT_ENABLE);
                    break;
                }
            }
            if(hasExtChild){
            	continue;
            }
            //检查部门下是否存在组
            if (isExistTeamByDept(dept)) {
            	//throw new BusinessException("部门下面存在组，不允许删除！");
                message.addErrorMsg(dept, MessageStatus.DEPARTMENT_EXIST_TEAM);
                continue;
            }

            // 增加删除部门下子部门的方法
            List<V3xOrgDepartment> childs = orgManager.getChildDepartments(dept.getId(), false, true);
            for (V3xOrgDepartment c : childs) {
                if (isExistMemberByDept(c)) {
                	//throw new BusinessException("部门下面存在人员，不允许删除！");
                	if(c.getIsInternal()){
                		message.addErrorMsg(c, MessageStatus.DEPARTMENT_EXIST_MEMBER);
                	}else{
                		message.addErrorMsg(c, MessageStatus.ACCOUNT_EXIST_MEMBER_ENABLE);
                	}
                    continue;
                }
                if (isExistTeamByDept(c)) {
                	//throw new BusinessException("部门下面存在组，不允许删除！");
                    message.addErrorMsg(c, MessageStatus.DEPARTMENT_EXIST_TEAM);
                    continue;
                }
                deleteLists.add(c);
            }
            deleteLists.add(dept);
            message.addSuccessMsg(dept);
        }

        //如果存在不能删除的部门或者子部门，则不做删除操作，只返回错误的提示信息。
        List<OrgMessage> errorMessage = message.getErrorMsgs();
        if(Strings.isNotEmpty(errorMessage)){
        	OrganizationMessage eMessage = new OrganizationMessage();
        	eMessage.addAllErrorMsg(errorMessage);
        	return eMessage;
        }else{
        	for(V3xOrgDepartment dept : deleteLists){
        		//删除符合条件的实体
        		this.updateEntity2Deleted(dept);
        		this.deleteMemberRoleRels2Entity(ORGENT_TYPE.Department, dept.getId());
                //删除这个部门角色下的所有角色关系数据
            	EnumMap<RelationshipObjectiveName, Object> enummap = new EnumMap<RelationshipObjectiveName, Object>(RelationshipObjectiveName.class);
                enummap.put(RelationshipObjectiveName.objective0Id, dept.getId());
                List<V3xOrgRelationship> rels = orgCache.getV3xOrgRelationship(RelationshipType.Member_Role,null, dept.getOrgAccountId(), enummap);
                this.deleteOrgRelationships(rels);

        		orgDao.update((OrgUnit) dept.toPO());
        		//触发事件
        		DeleteDepartmentEvent event = new DeleteDepartmentEvent(this);
        		event.setDept(dept);
        		EventDispatcher.fireEvent(event);
        	}
        }
        return message;
    }

    @Override
    public OrganizationMessage addPost(V3xOrgPost post) throws BusinessException {
        if (post == null)
            throw new BusinessException("实体对象为空！");
        List<V3xOrgPost> deps = new ArrayList<V3xOrgPost>(1);
        deps.add(post);
        return this.addPosts(deps);
    }

    @Override
    public OrganizationMessage addPosts(List<V3xOrgPost> posts) throws BusinessException {
        if (null == posts) {
            throw new BusinessException("岗位实体对象列表为空！");
        }
        OrganizationMessage message = new OrganizationMessage();
        List<V3xOrgPost> newEnts = new ArrayList<V3xOrgPost>();
        List<OrgPost> poList = new ArrayList<OrgPost>();

        Set<String> types = new HashSet<String>();
        for (V3xOrgPost post : posts) {
            //判断名称是否重复
            boolean isduplicated = isPropertyDuplicated(V3xOrgPost.class, "name", post.getName(), post.getOrgAccountId());
            if (isduplicated) {
                message.addErrorMsg(post, MessageStatus.POST_REPEAT_NAME);
                continue;
            }

            /**默认初始化一些属性，外部传入可以不必考虑这些初始化属性*/
            post.setIdIfNew();
            post.setStatus(OrgConstants.ORGENT_STATUS.NORMAL.ordinal());
            if(null == post.getSortId()) {
                post.setSortId(Long.valueOf(getMaxSortNum(V3xOrgPost.class.getSimpleName(), post.getOrgAccountId())) + 1);
            }
            /*********/
            newEnts.add(post);
            poList.add((OrgPost) post.toPO());
            message.addSuccessMsg(post);
            types.add(String.valueOf(post.getTypeId()));
        }
        // 2、实例化岗位
        orgDao.insertOrgPost(poList);

        //枚举调用记录
        for(String type : types){
        	enumManagerNew.updateEnumItemRef("organization_post_types", type);
        }

        // 3、触发事件
        for (V3xOrgPost post : newEnts) {
            AddPostEvent event = new AddPostEvent(this);
            event.setPost(post);
            EventDispatcher.fireEvent(event);
        }

        return message;
    }

    @Override
    public OrganizationMessage updatePost(V3xOrgPost post) throws BusinessException {
        List<V3xOrgPost> posts = new ArrayList<V3xOrgPost>();
        posts.add(post);
        return this.updatePosts(posts);
    }

    /**
     * OA-49751 集团基准岗修改名称时，需要校验引用的各单位的名称在单位内是否重复
     */
    private void checkDuplicatePostName(V3xOrgPost groupPost, List<OrgRelationship> reslist) throws BusinessException {
        if(!groupPost.getName().equals(orgManager.getPostById(groupPost.getId()).getName())){
            for (OrgRelationship orgRelationship : reslist) {
                V3xOrgPost accpost = orgManager.getPostById(orgRelationship.getSourceId());
                boolean isduplicated = isPostNameDuplicatedByCache(accpost.getId(), groupPost.getName(), accpost.getOrgAccountId());
                if (isduplicated) {
                    throw new BusinessException(ResourceUtil.getString("MessageStatus.POST_REPEAT_NAME", groupPost.getName()));
                }
            }
        }
    }

    /**
     * OA-49751  内存中比较是否属性重复
     */
    private boolean isPostNameDuplicatedByCache(Long entityId, String name, Long accountId) {
        List<V3xOrgPost> entitis = (List<V3xOrgPost>) orgCache.getAllV3xOrgEntityNoClone(V3xOrgPost.class, accountId);
        for (V3xOrgPost o : entitis) {
            if(o.getId().equals(entityId)) {
                continue;
            } else {
                if(o.getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public OrganizationMessage updatePosts(List<V3xOrgPost> posts) throws BusinessException {
        OrganizationMessage message = new OrganizationMessage();

        for (V3xOrgPost post : posts) {
            V3xOrgPost oldPost = orgManager.getPostById(post.getId());
            // 校验数据
            //如果是集团基准岗，则同步修改各单位绑定或引用的基准岗
            if(post.getOrgAccountId().equals(OrgConstants.GROUPID)){
                EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(
                        RelationshipObjectiveName.class);

                objectiveIds.put(RelationshipObjectiveName.objective0Id, post.getId());
                List<OrgRelationship> reslist = orgDao.getOrgRelationshipPO(RelationshipType.Banchmark_Post, null, null, objectiveIds, null);
                this.checkDuplicatePostName(post, reslist);// OA-49751
                for (OrgRelationship orgRelationship : reslist) {
                    V3xOrgPost accpost = orgManager.getPostById(orgRelationship.getSourceId());
                    accpost.setName(post.getName());
                    accpost.setCode(post.getCode());
                    accpost.setTypeId(post.getTypeId());
                    accpost.setDescription(post.getDescription());
                    accpost.setEnabled(post.getEnabled());
                    OrgHelper.throwBusinessExceptionTools(updatePost(accpost));
                }
            }
            // 判断名称重复
            if(!post.getName().equals(orgManager.getPostById(post.getId()).getName())){
                boolean isduplicated = isPropertyDuplicated(V3xOrgPost.class, "name", post.getName(), post.getOrgAccountId());
                if (isduplicated) {
                    message.addErrorMsg(post, MessageStatus.POST_REPEAT_NAME);
                    continue;
                }
            }

            //岗位下如果有人员,则不能停用
        	List<V3xOrgMember> _tempMemberList = orgManager.getMembersByPost(post.getId());
            if (!post.getEnabled() && oldPost.getEnabled() && _tempMemberList != null && _tempMemberList.size() > 0) {
                message.addErrorMsg(post, MessageStatus.POST_EXIST_MEMBER);
                continue;
            }
            post.setCreateTime(oldPost.getCreateTime());
            orgDao.update((OrgPost) post.toPO());
            message.addSuccessMsg(post);
            //触发事件
            UpdatePostEvent event = new UpdatePostEvent(this);
            event.setPost(post);
            event.setOldPost(oldPost);
            EventDispatcher.fireEvent(event);
        }
        return message;
    }

    @Override
    public OrganizationMessage deletePost(V3xOrgPost post) throws BusinessException {
        if (post == null)
            throw new BusinessException("实体对象为空！");
        List<V3xOrgPost> posts = new ArrayList<V3xOrgPost>(1);
        posts.add(post);
        return this.deletePosts(posts);
    }

    @Override
    public OrganizationMessage deletePosts(List<V3xOrgPost> posts) throws BusinessException {
        return this.deletePosts(posts,false);
    }

    @Override
    public OrganizationMessage deletePosts(List<V3xOrgPost> posts, boolean failfast) throws BusinessException {
        if (posts == null)
            throw new BusinessException("实体对象列表为空！");
        OrganizationMessage message = new OrganizationMessage();
        Set<V3xOrgPost> posts2delete = new HashSet<V3xOrgPost>();
        //TODO 1、校验岗位，集团基准岗等，同时删除关系
        for (V3xOrgPost post : posts) {

            //岗位下如果有人员,则不能删除
            List<V3xOrgMember> _tempMemberList = orgManager.getMembersByPost(post.getId());
            if(_tempMemberList != null && _tempMemberList.size() > 0){
                message.addErrorMsg(post, MessageStatus.POST_EXIST_MEMBER);
                continue;
            }
            //如果基准岗被引用,则不能删除
            EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(
                    RelationshipObjectiveName.class);

            objectiveIds.put(RelationshipObjectiveName.objective0Id, post.getId());
            List<OrgRelationship> reslist = orgDao.getOrgRelationshipPO(RelationshipType.Banchmark_Post, null, null, objectiveIds, null);
            if(reslist.size()>0){
                message.addErrorMsg(post, MessageStatus.POST_EXIST_BENCHMARK);
                continue;
            }

            posts2delete.add(post);
        }
        if(failfast && message.getErrorMsgs()!=null && message.getErrorMsgs().size()>0){
            return message;
        }
        for (V3xOrgPost post : posts2delete) {

            this.updateEntity2Deleted(post);
            //删除集团基准岗关系
            orgDao.deleteOrgRelationshipPO(RelationshipType.Banchmark_Post.name(), post.getId(), post.getOrgAccountId(), null);
            this.deleteMemberRoleRels2Entity(ORGENT_TYPE.Post, post.getId());
            // 2、实例化
            orgDao.update((OrgPost) post.toPO());
            message.addSuccessMsg(post);
            // 3、触发事件
            DeletePostEvent event = new DeletePostEvent(this);
            event.setPost(post);
            EventDispatcher.fireEvent(event);

        }
        return message;
    }

    @Override
    public OrganizationMessage addLevel(V3xOrgLevel level) throws BusinessException {
        List<V3xOrgLevel> levels = new ArrayList<V3xOrgLevel>();
        levels.add(level);
        return this.addLevels(levels);
    }

    @Override
    public OrganizationMessage addLevels(List<V3xOrgLevel> levels) throws BusinessException {
        if (null == levels)
            throw new BusinessException("职务级别实体对象列表为空！");
        OrganizationMessage message = new OrganizationMessage();
        List<OrgLevel> poList = new ArrayList<OrgLevel>(levels.size());
        for (V3xOrgLevel level : levels) {
        	//判断名称重复
            boolean isduplicated = this.isPropertyDuplicated(V3xOrgLevel.class, "name", level.getName(), level.getOrgAccountId());
            if (isduplicated) {
            	message.addErrorMsg(level, MessageStatus.LEVEL_REPEAT_NAME);
            	continue;
            }
            if(null == level.getSortId()) {
                level.setSortId(Long.valueOf(getMaxSortNum(V3xOrgLevel.class.getSimpleName(), level.getOrgAccountId())) + 1);
            }
            poList.add((OrgLevel) level.toPO());
            message.addSuccessMsg(level);
        }
        // 实例化
        orgDao.insertOrgLevel(poList);
        // 触发事件
        for (V3xOrgLevel level : levels) {
            AddLevelEvent event = new AddLevelEvent(this);
            event.setLevel(level);
            EventDispatcher.fireEvent(event);
        }
        return message;
    }

    @Override
    public OrganizationMessage updateLevel(V3xOrgLevel level) throws BusinessException {
        List<V3xOrgLevel> levels = new ArrayList<V3xOrgLevel>(1);
        levels.add(level);
        return this.updateLevels(levels);
    }

    @Override
    public OrganizationMessage updateLevels(List<V3xOrgLevel> levels) throws BusinessException {
        OrganizationMessage message = new OrganizationMessage();
        for (V3xOrgLevel level : levels) {
        	if(!level.getEnabled()&&orgManager.getLevelById(level.getId()).getEnabled()&&orgDao.isGroupLevelUsed(level.getId())){
        		message.addErrorMsg(level, MessageStatus.LEVEL_EXIST_MAPPING);
        		continue;
        	}

        	//判断名称重复
        	if(!level.getName().equals(orgManager.getLevelById(level.getId()).getName())){
        		boolean isduplicated = this.isPropertyDuplicated(V3xOrgLevel.class, "name", level.getName(), level.getOrgAccountId());
        		if (isduplicated) {
        			message.addErrorMsg(level, MessageStatus.LEVEL_REPEAT_NAME);
        			continue;
        		}
        	}
        	//判断停用存在人员
        	List<V3xOrgMember> _tempMemberList = this.getMembersByLevel(level.getId(), false);
        	if(!level.getEnabled()&&orgManager.getLevelById(level.getId()).getEnabled()&&_tempMemberList != null && _tempMemberList.size() > 0){
        		message.addErrorMsg(level, MessageStatus.LEVEL_EXIST_MEMBER);
        		continue;
        	}
            // 实例化
        	V3xOrgLevel oldLevel = orgManager.getEntityById(V3xOrgLevel.class, level.getId());
        	level.setCreateTime(oldLevel.getCreateTime());
            orgDao.update((OrgLevel) level.toPO());
            message.addSuccessMsg(level);
            // 触发事件
            UpdateLevelEvent event = new UpdateLevelEvent(this);
            event.setLevel(level);
            event.setOldLevel(oldLevel);
            EventDispatcher.fireEvent(event);
        }
        return message;
    }

    @Override
    public OrganizationMessage deleteLevel(V3xOrgLevel level) throws BusinessException {
        List<V3xOrgLevel> levels = new ArrayList<V3xOrgLevel>(1);
        levels.add(level);
        return this.deleteLevels(levels);
    }

    @Override
    public OrganizationMessage deleteLevels(List<V3xOrgLevel> levels) throws BusinessException {
        OrganizationMessage message = new OrganizationMessage();

        for (V3xOrgLevel level : levels) {
            //校验职务下是否存在人员
            List<V3xOrgMember> _tempMemberList = this.getMembersByLevel(level.getId(), false);
            if (_tempMemberList != null && _tempMemberList.size() > 0) {
                //throw new BusinessException(level.getName()+" 该职务级别下存在人员，不能删除！");
                message.addErrorMsg(level, MessageStatus.LEVEL_EXIST_MEMBER);
                continue;
            }
            if(orgDao.isGroupLevelUsed(level.getId())){
                //throw new BusinessException(level.getName()+" 该集团职务级别已被单位引用，不能删除！");
                message.addErrorMsg(level, MessageStatus.LEVEL_EXIST_MAPPING);
                continue;
            }
        }

        if(message.getErrorMsgs()!=null && message.getErrorMsgs().size()>0){
            return message;
        }

        for (V3xOrgLevel level : levels) {
            //实例化
            this.updateEntity2Deleted(level);
            this.deleteMemberRoleRels2Entity(ORGENT_TYPE.Level, level.getId());
            orgDao.update((OrgLevel) level.toPO());
            message.addSuccessMsg(level);
            //触发事件
            DeleteLevelEvent event = new DeleteLevelEvent(this);
            event.setLevel(level);
            EventDispatcher.fireEvent(event);
        }
        return message;
    }

    @Override
    public OrganizationMessage addMember(V3xOrgMember member) throws BusinessException {
        List<V3xOrgMember> members = new ArrayList<V3xOrgMember>(1);
        members.add(member);
        return this.addMembers(members);
    }

    @Override
    public void setMemberLocale(V3xOrgMember member, Locale locale) throws BusinessException {
        OrgHelper.getCustomizeManager().saveOrUpdateCustomize(member.getId(), CustomizeConstants.LOCALE, locale.toString());
    }

    @Override
    public Locale getMemberLocaleById(Long memberId) throws BusinessException {
        String pLang = OrgHelper.getCustomizeManager().getCustomizeValue(memberId, CustomizeConstants.LOCALE);
        pLang = Strings.escapeNULL(pLang, "zh_CN");

        return LocaleContext.parseLocale(pLang);
    }

    @Override
    public OrganizationMessage addMembers(List<V3xOrgMember> members) throws BusinessException {
        OrganizationMessage message = new OrganizationMessage();
        if(Strings.isEmpty(members)) {return message;}
        int size = members.size();
        List<OrgMember> poList = new ArrayList<OrgMember>(size);
        List<OrgRelationship> rels = new ArrayList<OrgRelationship>(size);
        List<V3xOrgPrincipal> principals = new ArrayList<V3xOrgPrincipal>(size);
        List<V3xOrgMember> sucessMembers = new ArrayList<V3xOrgMember>(size);

        /** map中对应的key为部门id，value为岗位列表 */
        Map<Long, Set<Long>> deptPosts = new HashMap<Long, Set<Long>>();
        //许可数校验
        int membernums = 0;
        for (V3xOrgMember v3xOrgMember : members) {
			if(v3xOrgMember.isValid()){
				membernums++;
			}
		}
        Object info ;
        Object info1 = MclclzUtil.invoke(c1, "getInstance",new Class[]{String.class},null,new Object[]{""});
        if("1".equals(MclclzUtil.invoke(c1, "getServerPermissionType"))){
        	info = MclclzUtil.invoke(c1, "getInstance",new Class[]{String.class},null,new Object[]{""});
        }else{
            info = MclclzUtil.invoke(c1, "getInstance",new Class[]{String.class},null,new Object[]{String.valueOf(members.get(0).getOrgAccountId())});
        }

        if(((Integer)MclclzUtil.invoke(c1, "getserverType",null,info1,null)).intValue()==LicenseConst.PERMISSION_TYPE_RES){
        	if(((Long)MclclzUtil.invoke(c1, "getUnuseservernum",null,info1,null)).intValue()<membernums){
        		log.info("剩余可用的注册数,ServerPermissionType-1："+MclclzUtil.invoke(c1, "getUnuseservernum",null,info1,null));
        		throw new BusinessException(ResourceUtil.getString("org.validate.permission2"));
        		//message.addErrorMsg(null, OrganizationMessage.MessageStatus.OUT_PER_NUM);
        		//return message;
        	}
        }

        if(((Integer)MclclzUtil.invoke(c1, "getserverType",null,info,null)).intValue()==LicenseConst.PERMISSION_TYPE_RES){
        	if(((Long)MclclzUtil.invoke(c1, "getUnuseservernum",null,info,null)).intValue()<membernums){
        		log.info("剩余可用的注册数,ServerPermissionType-0："+MclclzUtil.invoke(c1, "getUnuseservernum",null,info,null));
        		throw new BusinessException(ResourceUtil.getString("org.validate.permission2"));
        		//message.addErrorMsg(null, OrganizationMessage.MessageStatus.OUT_PER_NUM);
        		//return message;
        	}
        }

        for (V3xOrgMember member : members) {
        	try {
        		member.setIdIfNew();
        		//2013-04-02需求变更人员编码要保证唯一
        		if (Strings.isNotBlank(member.getCode())) {
        			//判断人员编码是否重复
        			boolean isDuplicated = isPropertyDuplicated(V3xOrgMember.class.getSimpleName(), "code", member.getCode());
        			if (isDuplicated) {
        				message.addErrorMsg(member, MessageStatus.MEMBER_REPEAT_CODE);
        				return message;
        			}
        		}
        		// 检查主岗和与副岗是否重复
        		if (!this.checkSecondPost(member)) {
        			message.addErrorMsg(member, MessageStatus.MEMBER_REPEAT_POST);
        			continue;
        		}
        		if(null == member.getV3xOrgPrincipal() || Strings.isBlank(member.getV3xOrgPrincipal().getLoginName())) {
        			message.addErrorMsg(member, MessageStatus.PRINCIPAL_NOT_EXIST);
        			continue;
        		}
        		// 校验登录名是否已存在
        		if (principalManager.isExist(member.getV3xOrgPrincipal().getLoginName())) {
        			message.addErrorMsg(member, MessageStatus.PRINCIPAL_REPEAT_NAME);
        			continue;
        		}
        		if(null == member.getSortId()) {
        			member.setSortId(Long.valueOf(getMaxSortNum(V3xOrgMember.class.getSimpleName(), member.getOrgAccountId())) + 1);
        		}
        		poList.add((OrgMember) member.toPO());
        		//实例化账号
        		principals.add(member.getV3xOrgPrincipal());
        		//关系表维护，主岗关系
        		MemberPost mainPost = MemberPost.createMainPost(member);
        		rels.add((OrgRelationship) mainPost.toRelationship().toPO());
        		//内部人员再创建岗位的关系，外部人员不创建岗位关系
        		if(member.getIsInternal() && !member.getIsAdmin()) {
        			//主岗与部门之间的关系表维护，主要逻辑先查询一下是否存在该部门与岗位之间的关系，如果有不做操作，如果没有新增一条这样的关系
        			Set<Long> priPostLists = deptPosts.get(member.getOrgDepartmentId());
        			setDeptPost(deptPosts, member.getOrgDepartmentId(), member.getOrgPostId(), priPostLists);

        			//副岗关系
        			List<MemberPost> secondPosts = member.getSecond_post();
        			if (secondPosts != null && secondPosts.size() > 0) {
        				for (MemberPost second : secondPosts) {
        					//解决Nc同步过来的副岗排序号为0问题
        					second.setSortId(member.getSortId());
        					V3xOrgRelationship secondPostRel = second.toRelationship();
        					rels.add((OrgRelationship) secondPostRel.toPO());
        					Set<Long> priPostListsSecd = deptPosts.get(second.getDepId());
        					setDeptPost(deptPosts, second.getDepId(), second.getPostId(), priPostListsSecd);
        				}
        			}
        		}

        		updateAddressBookinfo(member);

        		sucessMembers.add(member);
        		message.addSuccessMsg(member);

        		if (AppContext.hasPlugin("i18n")) {
        			if(Strings.isNotBlank(member.getPrimaryLanguange())){
        				String primaryLanguange = member.getPrimaryLanguange();
        				Locale locale = LocaleContext.parseLocale(primaryLanguange);
        				setMemberLocale(member, locale);
        			}
        		}
			} catch (Exception e) {
				log.error(e.getMessage());
				message.addErrorMsg(member, "添加人员出现异常: "+e.getMessage());
				continue;
			}
        }
        if (poList == null || poList.size() == 0) {
            return message;
        }
        //批量插入人员信息表
        orgDao.insertOrgMember(poList);
        //同步过来的人员需要增加一个默认角色
        defaultMemberRole4Add(sucessMembers);

        orgDao.insertOrgRelationship(rels);
        principalManager.insertBatch(principals);

        //新增人员时需要将postId和departmentId保存到关系表中key为Department_Post
        dealDeptPost(deptPosts);

        for (V3xOrgMember v3xOrgMember : sucessMembers) {
            // 触发事件
            AddMemberEvent event = new AddMemberEvent(this);
            if(sucessMembers.size() > 1) {
                event.setBatch(true);
            }
            event.setMember(v3xOrgMember);
            EventDispatcher.fireEvent(event);
        }

        if(sucessMembers.size() > 1) {
            //批量新建事件
            AddBatchMemberEvent event = new AddBatchMemberEvent(this);
            event.setBatchMembers(sucessMembers);
            EventDispatcher.fireEvent(event);
        }
        return message;
    }



    @Override
    public OrganizationMessage addGuest(V3xOrgMember member) throws BusinessException {
        OrganizationMessage message = new OrganizationMessage();
    	try {
    		member.setIdIfNew();
    		member.setIsInternal(false);
    		member.setExternalType(OrgConstants.ExternalType.Interconnect2.ordinal());

    		if(null == member.getV3xOrgPrincipal() || Strings.isBlank(member.getV3xOrgPrincipal().getLoginName())) {
    			message.addErrorMsg(member, MessageStatus.PRINCIPAL_NOT_EXIST);
    			return message;
    		}
    		// 校验登录名是否已存在
    		if (principalManager.isExist(member.getV3xOrgPrincipal().getLoginName())) {
    			message.addErrorMsg(member, MessageStatus.PRINCIPAL_REPEAT_NAME);
    			return message;
    		}

    		if(null == member.getSortId()) {//排序号不做处理，都是0;
    			member.setSortId(0L);
    		}
    		//实例化账号
    		OrgMember memberPo = (OrgMember)member.toPO();
    		List<OrgMember> orgMemberPO = new ArrayList<OrgMember>();
    		orgMemberPO.add(memberPo);
    		orgDao.insertOrgMember(orgMemberPO);
    		message.addSuccessMsg(member);

    		//添加特殊账号角色
    		addRole2Entity(OrgConstants.GUEST_ROLE_ID, member.getOrgAccountId(), member);
		} catch (Exception e) {
			log.error(e.getMessage());
			message.addErrorMsg(member, "添加人员出现异常: "+e.getMessage());
			return message;
		}
    	//添加账号
    	principalManager.insert(member.getV3xOrgPrincipal());
    	//TODO 添加默认角色

        return message;
    }

    private void setDeptPost(Map<Long, Set<Long>> deptPosts, Long deptId, Long postId, Set<Long> priPostLists) {
        if(priPostLists == null){
            priPostLists = new HashSet<Long>();
            deptPosts.put(deptId, priPostLists);
        }
        priPostLists.add(postId);
    }

    /**
     * 调用接口同步过来<b>新增</b>的人员、或者没有角色的人，默认给一个单位的默认角色
     * 单独处理是因为，新建一个人的时候无法校验这个人所在单位，部门，主岗，副岗，职务级别所拥有的角色（缓存中也不存在这个人的任何角色）
     * @param memberList
     * @throws BusinessException
     */
    private void defaultMemberRole4Add(List<V3xOrgMember> memberList) throws BusinessException {
        if(null == memberList || memberList.size() == 0){
            return;
        }

        List<OrgRelationship> relPOs = new ArrayList<OrgRelationship>(memberList.size());

        //为了提升性能, 同一个单位的只取一次
        Map<Long, V3xOrgRole> defultRoleByAccount = new HashMap<Long, V3xOrgRole>();

        for (V3xOrgMember member : memberList) {
            if(!member.getIsInternal()) {
                continue;
            }

            V3xOrgRole defultRole = defultRoleByAccount.get(member.getOrgAccountId());
            if(defultRole == null){
                defultRole = roleManager.getDefultRoleByAccount(member.getOrgAccountId());
                defultRoleByAccount.put(member.getOrgAccountId(), defultRole);
            }

            if(null == defultRole){
                continue;
            }

            Long unitId = OrgConstants.ROLE_BOND.DEPARTMENT.ordinal() == defultRole.getBond() ? member.getOrgDepartmentId() : member.getOrgAccountId();

            V3xOrgRelationship relBO = new V3xOrgRelationship();
            relBO.setId(UUIDLong.longUUID());
            relBO.setKey(RelationshipType.Member_Role.name());
            relBO.setSourceId(member.getId());
            relBO.setObjective0Id(unitId);
            relBO.setObjective1Id(defultRole.getId());
            relBO.setObjective5Id(ORGENT_TYPE.Member.name());//保存Member/Department/Post/Level等
            relBO.setOrgAccountId(member.getOrgAccountId());

            relPOs.add((OrgRelationship) relBO.toPO());
        }

        orgDao.insertOrgRelationship(relPOs);
    }

    /**
     * 调用接口同步过来<b>修改</b>的人员、或者没有角色的人，默认给一个单位的默认角色
     *
     * @param memberList
     * @throws BusinessException
     */
    private void defaultMemberRole4Update(List<V3xOrgMember> memberList) throws BusinessException {
    	if(null == memberList || memberList.size() == 0){
    		return;
    	}
    	List<Long> accoutIds = new ArrayList<Long>();
    	accoutIds.add(memberList.get(0).getOrgAccountId());

    	List<OrgRelationship> relPOs = new ArrayList<OrgRelationship>(memberList.size());

    	List<V3xOrgMember> memberIds = new ArrayList<V3xOrgMember>(0);
    	Set<Long> memberRole = new HashSet<Long>(); //有角色的人员，就不要加默认角色了
    	//OA-42270 任务项，人员角色要取消，这里如果人员其他属性也有角色，就不自动给这个人追加角色了，外部页面不限制角色必填
        for (V3xOrgMember member : memberList) {
        	if(member.getIsAdmin()) continue;
            List<Long> sourceIds = new ArrayList<Long>();
            sourceIds.add(member.getOrgAccountId()==null?-1L:member.getOrgAccountId());//单位已经有的角色
            sourceIds.add(member.getOrgDepartmentId()==null?-1L:member.getOrgDepartmentId());//部门已经有的角色
            sourceIds.add(member.getOrgPostId()==null?-1L:member.getOrgPostId());//岗位已经有的角色
            sourceIds.add(member.getOrgLevelId()==null?-1L:member.getOrgLevelId());//职务已经有的角色
            sourceIds.add(member.getId());
            //副岗已经有的角色
            if(member.getIsInternal() && !member.getIsAdmin()) {
                List<MemberPost> secondPosts = member.getSecond_post();
                if (secondPosts != null && secondPosts.size() > 0) {
                    for (MemberPost second : secondPosts) {
                        sourceIds.add(second.getPostId());
                    }
                }
            }
            List<V3xOrgRelationship> rels = orgCache.getV3xOrgRelationship(RelationshipType.Member_Role,
                    sourceIds, accoutIds, null);
            if(rels.size() == 0) {
                memberIds.add(member);//没角色
            } else {
                for (V3xOrgRelationship r : rels) {
                    V3xOrgRole role = orgCache.getV3xOrgEntity(V3xOrgRole.class, r.getObjective1Id());
                    if (null != role && (OrgConstants.ROLE_BOND.NULL2.ordinal() != role.getBond()
                            && OrgConstants.ROLE_BOND.SSO.ordinal() != role.getBond() && OrgConstants.ROLE_BOND.BUSINESS.ordinal() != role.getBond())) {
                        memberRole.add(member.getId());//有角色，就不自动增加角色
                    } else {
                        if(!memberIds.contains(member)){
                            memberIds.add(member);//没有角色，就自动增加一个默认角色
                        }
                    }
                }
            }
        }
//    	List<V3xOrgRelationship> rs = this.orgCache.getV3xOrgRelationship(RelationshipType.Member_Role, memberIds, accoutIds, null);
//    	for (V3xOrgRelationship r : rs) {
//    	    memberRole.add(r.getSourceId());
//        }

        //为了提升性能, 同一个单位的只取一次
        Map<Long, V3xOrgRole> defultRoleByAccount = new HashMap<Long, V3xOrgRole>();

        for (V3xOrgMember member : memberIds) {
        	if(member.getIsAdmin()) continue;
            if(memberRole.contains(member.getId())){
                continue;
            }
            if(!member.getIsInternal()) {
                continue;
            }
            if(member.getIsAdmin() || member.getIsVirtual()) {//OA-49905
                continue;
            }

            V3xOrgRole defultRole = defultRoleByAccount.get(member.getOrgAccountId());
            if(defultRole == null){
                defultRole = roleManager.getDefultRoleByAccount(member.getOrgAccountId());
                defultRoleByAccount.put(member.getOrgAccountId(), defultRole);
            }

            if(null == defultRole){
                continue;
            }

            Long unitId = OrgConstants.ROLE_BOND.DEPARTMENT.ordinal() == defultRole.getBond() ? member.getOrgDepartmentId() : member.getOrgAccountId();

            V3xOrgRelationship relBO = new V3xOrgRelationship();
            relBO.setId(UUIDLong.longUUID());
            relBO.setKey(RelationshipType.Member_Role.name());
            relBO.setSourceId(member.getId());
            relBO.setObjective0Id(unitId);
            relBO.setObjective1Id(defultRole.getId());
            relBO.setObjective5Id(ORGENT_TYPE.Member.name());//保存Member/Department/Post/Level等
            relBO.setOrgAccountId(member.getOrgAccountId());

            relPOs.add((OrgRelationship) relBO.toPO());
        }

        orgDao.insertOrgRelationship(relPOs);
    }

    /**
     * 用于处理部门与岗位之间的方法，调用dao创建关系
     * @param deptPosts <depId, Set<PostId>>
     * @throws BusinessException
     */
    private void dealDeptPost(Map<Long, Set<Long>> deptPosts) throws BusinessException {
        List<OrgRelationship> rels = new ArrayList<OrgRelationship>();

        for (Long departmentId : deptPosts.keySet()) {
            V3xOrgDepartment department = this.orgManager.getDepartmentById(departmentId);
            if(null == department) {
                continue;
            }

            List<Long> posts = new ArrayList<Long>(deptPosts.get(departmentId));

            EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(RelationshipObjectiveName.class);
            objectiveIds.put(RelationshipObjectiveName.objective0Id, posts);

            List<V3xOrgRelationship> _tempRels = orgCache.getV3xOrgRelationship(RelationshipType.Department_Post, departmentId, null, objectiveIds);
            List<Long> existPost = new ArrayList<Long>();

            for (V3xOrgRelationship v3xOrgRelationship : _tempRels) {
                existPost.add(v3xOrgRelationship.getObjective0Id());
            }

            posts.removeAll(existPost);

            for (Long postId : posts) {
                V3xOrgRelationship department_post_rel = new V3xOrgRelationship();
                department_post_rel.setId(UUIDLong.longUUID());
                department_post_rel.setKey(RelationshipType.Department_Post.name());
                department_post_rel.setSourceId(departmentId);
                department_post_rel.setObjective0Id(postId);
                department_post_rel.setCreateTime(new Date());
                department_post_rel.setUpdateTime(new Date());
                department_post_rel.setOrgAccountId(department.getOrgAccountId());

                rels.add((OrgRelationship)department_post_rel.toPO());
            }
        }

        orgDao.insertOrgRelationship(rels);
    }

    @Override
    public OrganizationMessage updateMember(V3xOrgMember member) throws BusinessException {
        List<V3xOrgMember> members = new ArrayList<V3xOrgMember>(1);
        members.add(member);
        return this.updateMembers(members);
    }

    @Override
    public OrganizationMessage updateMembers(List<V3xOrgMember> members) throws BusinessException {
        OrganizationMessage message = new OrganizationMessage();
        if(members.isEmpty()) {
            return message;
        }
        Map<Long, V3xOrgMember> oldMembers = new HashMap<Long, V3xOrgMember>();
        Map<Long, List<MemberPost>> oldMemberPost = new HashMap<Long, List<MemberPost>>();
        List<V3xOrgMember> ents = new ArrayList<V3xOrgMember>();
        List<OrgRelationship> rels = new ArrayList<OrgRelationship>(members.size());

        /** map中对应的key为部门id，value为岗位列表 */
        Map<Long, Set<Long>> deptPosts = new HashMap<Long, Set<Long>>();

        /**
         * 许可数校验
         * 1.不计算管理员
         * 2.停用-->启用   +1
         * 3.启用-->停用（有可能不允许停用或停用时异常，保险起见不做-1）
         */
        int membernums = 0;
        Object info ;
        // 1：不分单位，2：分单位
        if("1".equals(MclclzUtil.invoke(c1, "getServerPermissionType"))){
        	info = MclclzUtil.invoke(c1, "getInstance",new Class[]{String.class},null,new Object[]{""});
            for (V3xOrgMember v3xOrgMember : members) {
                if ((v3xOrgMember.isValid() == true && !v3xOrgMember.getIsAdmin()
                		&& orgManager.getMemberById(v3xOrgMember.getId()).isValid() == false) ) {
                    membernums++;
                }
            }
        }else{
            info = MclclzUtil.invoke(c1, "getInstance",new Class[]{String.class},null,new Object[]{String.valueOf(members.get(0).getOrgAccountId())});
            for (V3xOrgMember v3xOrgMember : members) {
                if ((v3xOrgMember.isValid() == true && !v3xOrgMember.getIsAdmin()
                	 && orgManager.getMemberById(v3xOrgMember.getId()).isValid() == false)
                   || (v3xOrgMember.isValid() == true && !v3xOrgMember.getIsAdmin()
                     && !orgManager.getMemberById(v3xOrgMember.getId()).getOrgAccountId().equals(v3xOrgMember.getOrgAccountId()))
                     ) {
                    membernums++;
                }
            }
        }

        if(((Integer)MclclzUtil.invoke(c1, "getserverType",null,info,null)).intValue()==LicenseConst.PERMISSION_TYPE_RES){
        	if(((Long)MclclzUtil.invoke(c1, "getUnuseservernum",null,info,null)).intValue()<membernums){
        		throw new BusinessException(ResourceUtil.getString("org.validate.permission"));
        		//message.addErrorMsg(null, OrganizationMessage.MessageStatus.OUT_PER_NUM);
        		//return message;
        	}
        }

        EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(
                RelationshipObjectiveName.class);
        objectiveIds.put(RelationshipObjectiveName.objective5Id, OrgConstants.MemberPostType.Concurrent.name());

        Set<Long> changePwdMemberIdSet = new HashSet<Long>();
        for (V3xOrgMember member : members) {
        	try {
        		//2013-04-02需求变更人员编码要保证唯一
        		if (Strings.isNotBlank(member.getCode()) && member.isValid()) {
        			//判断人员编码是否重复
        			boolean isDuplicated = isPropertyDuplicated(V3xOrgMember.class.getSimpleName(), "code",
        					member.getCode(), null, member.getId());
        			if (isDuplicated) {
        				message.addErrorMsg(member, MessageStatus.MEMBER_REPEAT_CODE);
        				continue;
        			}
        		}
        		long memberId = member.getId();
        		// 检查主岗和与副岗是否重复
        		if ((null != member.getOrgPostId()
        				|| !Long.valueOf(-1).equals(member.getOrgPostId()))//单位管理员没有岗位没有部门，值为-1或者null
        				&& !this.checkSecondPost(member)) {
        			message.addErrorMsg(member, MessageStatus.MEMBER_REPEAT_POST);
        			continue;
        		}
        		//检验人员所在部门、岗位、职务不可用直接不能启用 //OA-59629
        		if(!member.getIsAdmin() && member.getEnabled()) {
        			V3xOrgDepartment dept = orgManager.getDepartmentById(member.getOrgDepartmentId());
        			if(null == dept || !dept.isValid()) {
        				if(member.getIsInternal()){
        					message.addErrorMsg(member, MessageStatus.MEMBER_DEPARTMENT_DISABLED);
        				}else{
        					message.addErrorMsg(member, MessageStatus.MEMBER_EXTERNALACCOUNT_DISABLED);
        				}
        				continue;
        			}

        			V3xOrgPost post = orgManager.getPostById(member.getOrgPostId());
        			if(null == post || !post.isValid()) {
        				if(member.getIsInternal()){
        					message.addErrorMsg(member, MessageStatus.MEMBER_POST_DISABLED);
        					continue;
        				}
        			}

        			if(member.getIsInternal()) {//OA-60005
        				// 不能判断岗位，NC同步会同步过来待定岗-1，同时也不能判断职务为-1待定职务 OA-63377
        				if(!(new Long(-1)).equals(member.getOrgLevelId())) {
        					V3xOrgLevel level = orgManager.getLevelById(member.getOrgLevelId());
        					if(null == level || !level.isValid()) {
        						message.addErrorMsg(member, MessageStatus.MEMBER_LEVEL_DISABLED);
        						continue;
        					}
        				}
        			}
        		}
        		//部门空间开启，并且是部门主管时，不能修改为无效人员。
        		if(!member.isValid()){
        			boolean isContinue = false;
        			List<V3xOrgDepartment> departments = orgManager.getDeptsByManager(member.getId(), member.getOrgAccountId());
        			if(Strings.isNotEmpty(departments)){
        				for(V3xOrgDepartment department : departments){
        					boolean createDeptSpace = spaceApi.isCreateDepartmentSpace(department.getId());
        					if(createDeptSpace){
        						String deptName = department.getName();
        						V3xOrgRole role = orgManager.getRoleByName(Role_NAME.DepManager.name(), department.getOrgAccountId());
        						String roleName = role.getShowName();
        						message.addErrorMsg(member, ResourceUtil.getString("MessageStatus.DEPMANAGER_EXIST_CANNOT_DEL", deptName,roleName));
        						isContinue = true;
        						break;
        					}
        				}
        			}
        			if(isContinue){
        				continue;
        			}
        		}

        		//更新人员登录名之后再获取oldMember，导致oldMember中没有保存老的登录名
        		V3xOrgMember oldMember = new V3xOrgMember();
        		oldMember = orgManager.getEntityById(V3xOrgMember.class, member.getId());
        		V3xOrgPrincipal oldOrgPrincipal =new V3xOrgPrincipal();
        		oldOrgPrincipal=oldMember.getV3xOrgPrincipal();
        		oldMember.setV3xOrgPrincipal(oldOrgPrincipal);
        		//处理副岗的部门角色关系
        		//this.dealSecondPostDeptRoles(oldMember,member);
        		//将数据库中的原有人员取出添加到oldMembers中以备触发事件时使用
        		oldMembers.put(oldMember.getId(), oldMember);
        		oldMemberPost.put(oldMember.getId(), oldMember.getSecond_post());
        		boolean isChangePwd = false;
        		//人员无效（人员删除、离职），删除账号；未分配账号保留
        		if(member.getIsDeleted() || !member.getIsLoginable() || member.getState().intValue() == OrgConstants.MEMBER_STATE.RESIGN.ordinal()){
        			//Fix OA-11374 增加判断，有可能人员离职帐号就已经被删除了，此时再删除会引起事务报错
        			if(principalManager.isExist(member.getId())) {
        				principalManager.delete(member.getId());
        				isChangePwd = true;
        			}
        		} else{
        			V3xOrgPrincipal principal = member.getV3xOrgPrincipal();
        			if(null != principal) {
        				if(!principalManager.isExist(memberId)){ //之前没有账号，给补一个
        					if(principalManager.isExist(principal.getLoginName())){
        						message.addErrorMsg(member, MessageStatus.PRINCIPAL_REPEAT_NAME);
        						continue;
        					}
        					principalManager.insert(principal);
        					isChangePwd = true;
        				}
        				else{
        					//判断登陆名重名
        					String oldPrincipal = null;
        					try {
        						oldPrincipal = principalManager.getLoginNameByMemberId(memberId);
        					}
        					catch (NoSuchPrincipalException e) {
        						//ignore
        					}

        					if(!principal.getLoginName().equals(oldPrincipal)) {
        						if(principalManager.isExist(principal.getLoginName())) {
        							message.addErrorMsg(member, MessageStatus.PRINCIPAL_REPEAT_NAME);
        							continue;
        						}
        					}

        					principalManager.update(member.getV3xOrgPrincipal());
        					if(OrgConstants.DEFAULT_INTERNAL_PASSWORD.equals(member.getV3xOrgPrincipal().getPassword())){
        						isChangePwd = false;
        					}else{
        						isChangePwd = this.isOldPasswordCorrect(member.getV3xOrgPrincipal().getLoginName(),member.getV3xOrgPrincipal().getPassword());
        					}
        				}
        				//分发修改密码事件
        				if(isChangePwd){
        					OnlineRecorder.moveToOffline(oldMember.getLoginName(), LoginOfflineOperation.adminKickoff);
        					changePwdMemberIdSet.add(member.getId());
        					ChangePwdEvent event = new ChangePwdEvent(this);
        					event.setMember(member);
        					EventDispatcher.fireEvent(event);
        				}
        			}
        		}
        		if(!member.getIsAdmin()) {//管理员不建立岗位关系
        			//关系表维护，主岗关系
        			MemberPost mainPost = MemberPost.createMainPost(member);
        			rels.add((OrgRelationship) mainPost.toRelationship().toPO());
        		}
        		//内部人员再创建岗位的关系，外部人员不创建岗位关系
        		if(member.getIsInternal() && !member.getIsAdmin()) {
        			Set<Long> priPostLists = deptPosts.get(member.getOrgDepartmentId());
        			setDeptPost(deptPosts, member.getOrgDepartmentId(), member.getOrgPostId(), priPostLists);

        			//副岗关系
        			List<MemberPost> secondPosts = member.getSecond_post();
        			if (secondPosts != null && secondPosts.size() > 0) {
        				for (MemberPost second : secondPosts) {
        					if(!member.getOrgAccountId().equals(second.getOrgAccountId())){
        						continue;
        					}
        					//解决Nc同步过来的副岗排序号为0问题
        					second.setSortId(member.getSortId());
        					V3xOrgRelationship secondPostRel = second.toRelationship();
        					rels.add((OrgRelationship) secondPostRel.toPO());

        					//副岗设置的时候也增加部门岗位
        					Set<Long> priPostListsSecd = deptPosts.get(second.getDepId());
        					setDeptPost(deptPosts, second.getDepId(), second.getPostId(), priPostListsSecd);
        				}
        			}
        		}

                //如果电话或邮箱与修改前的不一样，解除绑定
        		String oldEmail = oldMember.getEmailAddress() == null ? "" : oldMember.getEmailAddress();
        		String newEmail = member.getEmailAddress() == null ? "" : member.getEmailAddress();
                if (!oldEmail.equals(newEmail)) {
                	OrgHelper.getCustomizeManager().saveOrUpdateCustomize(memberId, CustomizeConstants.BIND_EMAIL, "false");
                }

        		String oldTelnum = oldMember.getTelNumber() == null ? "" : oldMember.getTelNumber();
        		String newTelnum = member.getTelNumber() == null ? "" : member.getTelNumber();
                if (!oldTelnum.equals(newTelnum)) {
                    OrgHelper.getCustomizeManager().saveOrUpdateCustomize(memberId, CustomizeConstants.BIND_PHONENUMBER, "false");
                }

        		//实例化
        		member.setCreateTime(oldMember.getCreateTime());
        		orgDao.update((OrgMember) member.toPO());

        		//更新通讯录字段
        		updateAddressBookinfo(member);

        		if (AppContext.hasPlugin("i18n")) {
        			String primaryLanguange = getMemberLocaleById(member.getId()).toString();
        			if(Strings.isNotBlank(member.getPrimaryLanguange())){
        				primaryLanguange = member.getPrimaryLanguange();
        			}
        			Locale locale = LocaleContext.parseLocale(primaryLanguange);
        			setMemberLocale(member, locale);
        		}

        		//修改主岗方法 steven
        		List<V3xOrgRelationship> deleteRels = new UniqueList<V3xOrgRelationship>();
        		//如果不是跨单位修改人员，删除在这个单位下这个人的所有岗位信息
        		deleteRels.addAll(orgCache.getV3xOrgRelationship(RelationshipType.Member_Post, member.getId(), oldMember.getOrgAccountId(), null));
        		if(!oldMember.getOrgAccountId().equals(member.getOrgAccountId())){
        			//如果是跨单位，删除这个人在新单位原来的信息（兼职信息等）
        			deleteRels.addAll(orgCache.getV3xOrgRelationship(RelationshipType.Member_Post, member.getId(), member.getOrgAccountId(), null));
        		}
        		this.deleteOrgRelationships(deleteRels);

/*        		//可能存在多余的缓存数据，在缓存中再删一遍。
        		List<V3xOrgRelationship> relationshipList = orgManager.getMemberPostRelastionships(member.getId(), member.getOrgAccountId(), null);
        		relationshipList.addAll(orgManager.getMemberPostRelastionships(member.getId(), null, objectiveIds));
        		if(Strings.isNotEmpty(relationshipList)){
        			List<OrgRelationship> rs = new ArrayList<OrgRelationship>();
        			for(V3xOrgRelationship vr : relationshipList){
        				rs.add((OrgRelationship) vr.toPO());
        			}
        			orgCache.cacheRemoveRelationship(rs);
        		}*/

        		ents.add(member);
        		message.addSuccessMsg(member);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
				message.addErrorMsg(member, "更新人员出现异常: "+e.getMessage());
				continue;
			}
        }
        orgDao.insertOrgRelationship(rels);

        //修改人员时需要将postId和departmentId保存到关系表中key为Department_Post
        dealDeptPost(deptPosts);
        //OA-23804 NC同步修改的人员如果没有角色，默认给一个单位的默认角色
        defaultMemberRole4Update(ents);

        for (V3xOrgMember member : ents) {
            // 人员修改事件 UpdateMemberEvent
            UpdateMemberEvent event = new UpdateMemberEvent(this);
            event.setOldMember(oldMembers.get(member.getId()));
            event.setMember(member);
            EventDispatcher.fireEvent(event);
            // 跨单位调动事件MemberAccountChangeEvent
            Long oldAccountId = oldMembers.get(member.getId()).getOrgAccountId();
            Long newAccountId = member.getOrgAccountId();
            if (!oldAccountId.equals(newAccountId)) {// Long类型不能直接用==和!=来对比值，对象对比
                MemberAccountChangeEvent evt = new MemberAccountChangeEvent(this);
                evt.setOldMember(oldMembers.get(member.getId()));
                evt.setMember(member);
                evt.setOldAccount(oldAccountId);
                evt.setAccount(newAccountId);
                EventDispatcher.fireEvent(evt);
            }
            // 人员修改部门事件 MemberUpdateDeptEvent
            Long oldDeptId = oldMembers.get(member.getId()).getOrgDepartmentId();
            Long newDeptId = member.getOrgDepartmentId();
            if(!Strings.equals(oldDeptId, newDeptId)) {
                MemberUpdateDeptEvent muEvent = new MemberUpdateDeptEvent(this);
                muEvent.setMember(member);
                muEvent.setOldDepartmentId(oldDeptId);
                muEvent.setNewDepartmentId(newDeptId);
                EventDispatcher.fireEvent(muEvent);
            }
            User user = AppContext.getCurrentUser();
            if (user != null) {
                V3xOrgMember oldM = oldMembers.get(member.getId());
                //人员启用/停用
                if (!Strings.equals(oldM.getEnabled(), member.getEnabled())) {
                    String oldState = ResourceUtil.getString("org.member_form.end.rep");
                    String newState = ResourceUtil.getString("org.member_form.start.rep");
                    if (oldM.getEnabled()) {
                        oldState = ResourceUtil.getString("org.member_form.start.rep");
                        newState = ResourceUtil.getString("org.member_form.end.rep");
                    }
                    appLogManager.insertLog(user, 879, user.getName(), member.getName(), oldState, newState);
                }
                //工作地变化了
                if((Strings.isBlank(oldM.getLocation()) && Strings.isNotBlank(member.getLocation()))
                		|| (Strings.isNotBlank(oldM.getLocation()) && Strings.isBlank(member.getLocation()))
                		 || (Strings.isNotBlank(oldM.getLocation()) && Strings.isNotBlank(member.getLocation()) &&
                		!Strings.equals(oldM.getLocation(), member.getLocation()))){
                	appLogManager.insertLog4Account(user, user.isGroupAdmin() ? oldM.getOrgAccountId() : AppContext.currentAccountId(), AppLogAction.Organization_UpdateMemberLocal, user.getName(), member.getName(),enumManagerNew.parseToName(oldM.getLocation()),enumManagerNew.parseToName(member.getLocation()));
                }
                //匯報人变化了
                if(!Strings.equals(oldM.getReporter(), member.getReporter())){
                	V3xOrgMember oldR =  orgManager.getMemberById(oldM.getReporter());
                	V3xOrgMember newR =  orgManager.getMemberById(member.getReporter());
                	appLogManager.insertLog4Account(user, user.isGroupAdmin() ? oldM.getOrgAccountId() : AppContext.currentAccountId(), AppLogAction.Organization_UpdateMemberReporter, user.getName(), member.getName(),
                			oldR != null ? oldR.getName() : "", newR != null ? newR.getName() : "");
                }
                //部门变化了
                if (!Strings.equals(oldDeptId, newDeptId)) {
                    V3xOrgDepartment oDept = orgManager.getDepartmentById(oldMembers.get(member.getId()).getOrgDepartmentId());
                    V3xOrgDepartment nDept = orgManager.getDepartmentById(member.getOrgDepartmentId());
                    appLogManager.insertLog4Account(user, user.isGroupAdmin() ? oldM.getOrgAccountId() : AppContext.currentAccountId(), AppLogAction.Organization_UpdateMemberDept, user.getName(), member.getName(),
                            oDept != null ? oDept.getName() : "", nDept != null ? nDept.getName() : "");
                }
                //岗位变化了
                if (!Strings.equals(oldM.getOrgPostId(), member.getOrgPostId())) {
                    V3xOrgPost  post = orgManager.getPostById(oldM.getOrgPostId());
                    V3xOrgPost  npost = orgManager.getPostById(member.getOrgPostId());
                    appLogManager.insertLog4Account(user, user.isGroupAdmin() ? oldM.getOrgAccountId() : AppContext.currentAccountId(), AppLogAction.Organization_UpdateMemberPost, user.getName(), member
                            .getName(), post == null ? "" :post.getName(),npost == null ? "" :npost.getName());
                }
                //职务变化了
                if (!Strings.equals(oldM.getOrgLevelId(), member.getOrgLevelId())) {
                    V3xOrgLevel  level = orgManager.getLevelById(oldM.getOrgLevelId());
                    V3xOrgLevel  nlevel = orgManager.getLevelById(member.getOrgLevelId());
                    appLogManager.insertLog4Account(user, user.isGroupAdmin() ? oldM.getOrgAccountId() : AppContext.currentAccountId(), AppLogAction.Organization_UpdateMemberLevel, user.getName(), member
                            .getName(), level == null ? "" :level.getName(), nlevel == null ? "" :nlevel.getName());
                }
                //副岗变化了
                if (!Strings.equals(oldMemberPost.get(oldM.getId()), member.getSecond_post())) {
                    StringBuffer sbo = new StringBuffer();
                    for (MemberPost mp : oldM.getSecond_post()) {
                        sbo.append(orgManager.getPostById(mp.getPostId()).getName()).append("、");
                    }
                    StringBuffer sbn = new StringBuffer();
                    for (MemberPost mp : member.getSecond_post()) {
                        sbn.append(orgManager.getPostById(mp.getPostId()).getName()).append("、");
                    }
                    String so = sbo.toString();
                    String sn = sbn.toString();
                    appLogManager.insertLog4Account(user, user.isGroupAdmin() ? oldM.getOrgAccountId() : AppContext.currentAccountId(), AppLogAction.Organization_UpdateMemberSecP, user.getName(), member
                            .getName(), so.length() > 0 ? so.substring(0, so.length() - 1) : "",
                            sn.length() > 0 ? sn.substring(0, sn.length() - 1) : "");
                }

                if(changePwdMemberIdSet.contains(member.getId())){
                	appLogManager.insertLog4Account(user, user.isGroupAdmin() ? oldM.getOrgAccountId() : AppContext.currentAccountId(), AppLogAction.Systemmanager_UpdateUserPassWord, user.getName(), member.getName());
                }
            }else{
            	if(changePwdMemberIdSet.contains(member.getId())){
            		log.info("*********接口修改了人员《"+member.getName()+"》的密码！******");
            	}
            }
        }
        return message;
    }

    @Override
    public OrganizationMessage updateGuest(V3xOrgMember member) throws BusinessException {
        OrganizationMessage message = new OrganizationMessage();
    	try {
    		member.setIsInternal(false);
    		member.setExternalType(OrgConstants.ExternalType.Interconnect2.ordinal());

    		long memberId = member.getId();
    		V3xOrgMember oldMember = orgManager.getEntityById(V3xOrgMember.class, member.getId());

    		//人员无效（人员删除、离职），删除账号；未分配账号保留
    		if(member.getIsDeleted()){
    			//Fix OA-11374 增加判断，有可能人员离职帐号就已经被删除了，此时再删除会引起事务报错
    			if(principalManager.isExist(member.getId())) {
    				principalManager.delete(member.getId());
    			}
    		} else{
    			V3xOrgPrincipal principal = member.getV3xOrgPrincipal();
    			if(null != principal) {
    				if(!principalManager.isExist(memberId)){ //之前没有账号，给补一个
    					if(principalManager.isExist(principal.getLoginName())){
    						message.addErrorMsg(member, MessageStatus.PRINCIPAL_REPEAT_NAME);
    						return message;
    					}
    					principalManager.insert(principal);
    				}
    				else{
    					//判断登陆名重名
    					String oldLoginName = null;
    					try {
    						oldLoginName = principalManager.getLoginNameByMemberId(memberId);
    					}
    					catch (NoSuchPrincipalException e) {
    						//ignore
    					}

    					if(!principal.getLoginName().equals(oldLoginName)) {
    						if(principalManager.isExist(principal.getLoginName())) {
    							message.addErrorMsg(member, MessageStatus.PRINCIPAL_REPEAT_NAME);
    							return message;
    						}
    					}

    					principalManager.update(member.getV3xOrgPrincipal());
    				}
    			}
    		}

    		//实例化
    		member.setCreateTime(oldMember.getCreateTime());
    		orgDao.update((OrgMember) member.toPO());
    		message.addSuccessMsg(member);
		} catch (Exception e) {
			log.error(e.getMessage());
			message.addErrorMsg(member, "更新人员出现异常: "+e.getMessage());
			return message;
		}
        //TODO 设置默认角色
        return message;
    }

    /**
     * Fix OA-25676 lilong
     * 用于处理人员副岗关系变更，部门角色维护，其副岗部门角色关系的维护
     * @param oldMember
     * @param updateMember
     * @throws BusinessException
     */
    private void dealSecondPostDeptRoles(V3xOrgMember oldMember, V3xOrgMember updateMember) throws BusinessException {
        if(oldMember.getSecond_post().size() == 0) {
            //之前就没有副岗，直接不处理
            return;
        } else if (updateMember.getSecond_post().size() == 0
                || (oldMember.getSecond_post().size() > 0 && updateMember.getSecond_post().size() > 0)) {
            //修改后，无副岗，或者新增修改了副岗
            List<MemberPost> second = updateMember.getSecond_post();
            Set<Long> deptIdSet = new HashSet<Long>();
            for (MemberPost memberPost : second) {
                deptIdSet.add(memberPost.getDepId());
                //补充子部门
                List<V3xOrgDepartment> childDepts = orgManager.getChildDepartments(memberPost.getDepId(), false);
                for (V3xOrgDepartment child : childDepts) {
                    deptIdSet.add(child.getId());
                }
                //补充父部门
                List<V3xOrgDepartment> parentDepts = orgManager.getAllParentDepartments(memberPost.getDepId());
                for (V3xOrgDepartment parent : parentDepts) {
                    deptIdSet.add(parent.getId());
                }
            }

            //如果修改的人，修改后无副岗，直接清空这个人在本单位其他部门的所有部门角色
            List<V3xOrgRelationship> relList = orgCache.getV3xOrgRelationship(
                    RelationshipType.Member_Role, updateMember.getId(), updateMember.getOrgAccountId(),
                    null);
            for (V3xOrgRelationship bo : relList) {
                V3xOrgRole role = orgManager.getRoleById(bo.getObjective1Id());
                if (null != role) {
                    if (OrgConstants.ROLE_BOND.DEPARTMENT.ordinal() == role.getBond()) {
                        //Fix OA-31762 取消副岗其他部门角色时保留部门分管领导这个角色
                        if (Role_NAME.DepLeader.name().equals(role.getCode())) {
                            continue;
                        }
                        //只删除非所在部门的副岗部门角色
                        if (!bo.getObjective0Id().equals(updateMember.getOrgDepartmentId())
                                && !deptIdSet.contains(bo.getObjective0Id())) {
                            orgDao.deleteOrgRelationshipPOById(bo.getId());
                        }
                    }
                }
            }
        } else {
            //ignore
            return;
        }

    }

    @Override
    public OrganizationMessage deleteMember(V3xOrgMember member) throws BusinessException {
        List<V3xOrgMember> members = new ArrayList<V3xOrgMember>(1);
        members.add(member);
        return this.deleteMembers(members);
    }

    @Override
    public OrganizationMessage deleteMembers(List<V3xOrgMember> members) throws BusinessException {
        OrganizationMessage message = new OrganizationMessage();
        Map<String, CheckMemberDelete> initsMap = AppContext.getBeansOfType(CheckMemberDelete.class);
        Iterator<String> i = initsMap.keySet().iterator();
        for (V3xOrgMember member : members) {
            if(member == null){
                continue;
            }
            String oldLoginName = member.getLoginName();

            //OA-26950 人员删除时的外部模块校验
            while (i.hasNext()) {
                CheckMemberDelete deleteMemberCheckImpl = initsMap.get((String) i.next());
                if(!deleteMemberCheckImpl.canDeleteMember(member)) {
                    message.addErrorMsg(member, MessageStatus.MEMBER_CANNOT_DELETE);
                    return message;
                }
            }
        	List<V3xOrgDepartment> departments = orgManager.getDeptsByManager(member.getId(), member.getOrgAccountId());
        	if(Strings.isNotEmpty(departments)){
        		for(V3xOrgDepartment department : departments){
        			boolean createDeptSpace = spaceApi.isCreateDepartmentSpace(department.getId());
        			if(createDeptSpace){
        				String deptName = department.getName();
        				V3xOrgRole role = orgManager.getRoleByName(Role_NAME.DepManager.name(), department.getOrgAccountId());
        				String roleName = role.getShowName();
        				throw new BusinessException(ResourceUtil.getString("MessageStatus.DEPMANAGER_EXIST_CANNOT_DEL", deptName,roleName));
        			}
        		}
        	}

            //信息表逻辑删除
            this.updateEntity2Deleted(member);
            member.setIsLoginable(false);
            //实例化
            orgDao.update((OrgMember) member.toPO());
            //账号表物理删除
            //20121228lilong增加判断，如果是离职人员帐号已经被删除了这里再次删除人员时会有报错导致事务回滚
            if(principalManager.isExist(member.getId())) {
                principalManager.delete(member.getId());
            }
          //修改主岗方法 steven
            //清除这个人在关系表中的记录
            orgDao.deleteOrgRelationshipPO(RelationshipType.Member_Post.name(), member.getId(), null, null);
            orgDao.deleteOrgRelationshipPO(RelationshipType.Member_Role.name(), member.getId(), null, null);
            //组成员和组发布范围更新
            EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(
                    RelationshipObjectiveName.class);
            objectiveIds.put(RelationshipObjectiveName.objective0Id, member.getId());
            orgDao.deleteOrgRelationshipPO(RelationshipType.Team_Member.name(), null, null, objectiveIds);
            orgDao.deleteOrgRelationshipPO(RelationshipType.Team_PubScope.name(), null, null, objectiveIds);
            //如果该人员外部人员工作范围
            orgDao.deleteOrgRelationshipPO(RelationshipType.External_Workscope.name(), member.getId(), null, null);

            //将用户踢下下线
            OnlineRecorder.moveToOffline(oldLoginName, LoginOfflineOperation.adminKickoff);

            message.addSuccessMsg(member);
            //触发事件
            DeleteMemberEvent event = new DeleteMemberEvent(this);
            event.setMember(member);
            EventDispatcher.fireEvent(event);
        }
        return message;
    }

    @Override
    public OrganizationMessage deleteGuests(List<V3xOrgMember> members) throws BusinessException {
        OrganizationMessage message = new OrganizationMessage();
        for (V3xOrgMember member : members) {
            if(member == null){
                continue;
            }
            String oldLoginName = member.getLoginName();
            member.setIsLoginable(false);
            member.setIsDeleted(true);
            member.setEnabled(false);
            //实例化
            orgDao.update((OrgMember) member.toPO());
            //账号表物理删除
            if(principalManager.isExist(member.getId())) {
                principalManager.delete(member.getId());
            }
            orgDao.deleteOrgRelationshipPO(RelationshipType.Member_Role.name(), member.getId(), null, null);

            //将用户踢下下线
            OnlineRecorder.moveToOffline(oldLoginName, LoginOfflineOperation.adminKickoff);
            message.addSuccessMsg(member);
        }
        return message;
    }

    @Override
    public V3xOrgMember getMemberByLoginName(String loginName, boolean includeDisabled) throws BusinessException {
        V3xOrgMember member = null;
        if (null == loginName)
            throw new BusinessException("根据登录名查询人员，登录名为空！");
        long memberId;
        try {
            memberId = principalManager.getMemberIdByLoginName(loginName);
            if (includeDisabled) {//包含无效人员直接查询数据库
                member = new V3xOrgMember(orgDao.getOrgMemberPO(memberId));
            } else {//不包含无效人员，直接从缓存里面取
                member = orgCache.getV3xOrgEntity(V3xOrgMember.class, memberId);
            }
            if (null != member) {
                return member;
            } else {
                throw new BusinessException("根据登录名没有查询到人员！");
            }
        } catch (NoSuchPrincipalException e) {
            throw new BusinessException("根据登录名没有查询到具体的人员！");
        }
    }

    @Override
    public List<V3xOrgMember> getMemberByName(String memberName, Long accountId, boolean includeDisabled)
            throws BusinessException {
        if (memberName == null) {
            throw new BusinessException("根据人员姓名查询姓名为空！");
        }
        List<V3xOrgMember> list = orgManager.getAllMembers(accountId);
        List<V3xOrgMember> memberList = new ArrayList<V3xOrgMember>();
        for (Iterator<V3xOrgMember> it = list.iterator(); it.hasNext();) {
            V3xOrgMember member = (V3xOrgMember) it.next();
            if (memberName.equals(member.getName()) && (member.isValid() || includeDisabled) && member.getIsInternal()) // 必须是内部人员
            {
                memberList.add((V3xOrgMember) member);
            }
        }
        Collections.sort(memberList, CompareSortEntity.getInstance());
        return memberList;
    }

    @Override
    public List<V3xOrgTeam> getDepartmentTeam(Long depId, boolean includeDisabled) throws BusinessException {
    	V3xOrgDepartment dept = orgManager.getDepartmentById(depId);
    	Boolean enable = true;
        if(includeDisabled){
            enable = null;
        }
    	@SuppressWarnings("unchecked")
        List<V3xOrgTeam> teamList = (List<V3xOrgTeam>) OrgHelper.listPoTolistBo(orgDao.getAllTeamPO(dept.getOrgAccountId(), null, enable, "ownerId", depId, null));

//        List<V3xOrgTeam> list = orgCache.getAllV3xOrgEntity(V3xOrgTeam.class, dept.getOrgAccountId());
//        List<V3xOrgTeam> teamList = new ArrayList<V3xOrgTeam>();
//        for (Iterator<V3xOrgTeam> it = list.iterator(); it.hasNext();) {
//            V3xOrgTeam team = (V3xOrgTeam) it.next();
//            if (includeDisabled) {// 包含已停用未删除的组
//                teamList.add((V3xOrgTeam) team);
//            } else {
//                if (team.isValid()) {// 职务状态为启用的未删除
//                    teamList.add((V3xOrgTeam) team);
//                } else {
//                    continue;
//                }
//            }
//        }
        Collections.sort(teamList, CompareSortEntity.getInstance());// 默认按照sort排序号排序
        return teamList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<V3xOrgDepartment> getAllDepartments(Long accountID, Boolean enable, Boolean isInternal,
            String condition, Object feildvalue, FlipInfo flipInfo) throws BusinessException {
        return (List<V3xOrgDepartment>) OrgHelper.listPoTolistBo(orgDao.getAllUnitPO(UnitType.Department,
                accountID, enable, isInternal, condition, feildvalue, flipInfo));
    }

    @Override
    public List<V3xOrgLevel> getAllLevels(Long accountId, boolean includeDisabled) throws BusinessException {
        List<OrgLevel> levelPOs = orgDao.getAllLevelPO(accountId, null, null, null, null);
        @SuppressWarnings("unchecked")
        List<V3xOrgLevel> list = (List<V3xOrgLevel>) OrgHelper.listPoTolistBo(levelPOs);
        List<V3xOrgLevel> levelList = new ArrayList<V3xOrgLevel>();
        for (Iterator<V3xOrgLevel> it = list.iterator(); it.hasNext();) {
            V3xOrgLevel level = (V3xOrgLevel) it.next();
            if (includeDisabled) {// 包含已停用未删除的职务
                levelList.add((V3xOrgLevel) level);
            } else {
                if (level.isValid()) {//有效启用的职务
                    levelList.add((V3xOrgLevel) level);
                } else {
                    continue;
                }
            }
        }

		Collections.sort(levelList, new Comparator<V3xOrgLevel>() {
            public int compare(V3xOrgLevel l1, V3xOrgLevel l2) {
            	int id1 = l1.getLevelId();
            	int id2 = l2.getLevelId();
            	return id1 > id2 ? 1 : (id1 < id2 ? -1 : 0);
            }
        });

        return levelList;
    }

    @Override
    public List<? extends V3xOrgEntity > getUnenabledEntities(String entityTypeName, Long accountId) throws BusinessException {
        if(entityTypeName.equals(V3xOrgAccount.class.getSimpleName())) {
            List<OrgUnit> units = orgDao.getAllUnenabledAccounts(accountId, null);
            List<V3xOrgUnit> list = (List<V3xOrgUnit>) OrgHelper.listPoTolistBo(units);
            List<V3xOrgAccount> resultList = new ArrayList<V3xOrgAccount>();
            for (Iterator<V3xOrgUnit> it = list.iterator(); it.hasNext();) {
                V3xOrgUnit u = it.next();
                if(UnitType.Account.name().equals(u.getType().name())) {
                    resultList.add((V3xOrgAccount)u);
                }
            }
            Collections.sort(resultList, CompareSortEntity.getInstance());
            return resultList;
        } else if(entityTypeName.equals(V3xOrgDepartment.class.getSimpleName())) {
            List<OrgUnit> units = orgDao.getAllUnenabledDepartments(accountId, null);//getAllUnenabledAccounts(accountId, null);
            List<V3xOrgUnit> list = (List<V3xOrgUnit>) OrgHelper.listPoTolistBo(units);
            List<V3xOrgDepartment> resultList = new ArrayList<V3xOrgDepartment>();
            for (Iterator<V3xOrgUnit> it = list.iterator(); it.hasNext();) {
                V3xOrgUnit u = it.next();
                if(UnitType.Department.name().equals(u.getType().name())) {
                    resultList.add((V3xOrgDepartment)u);
                }
            }
            Collections.sort(resultList, CompareSortEntity.getInstance());
            return resultList;
        } else if(entityTypeName.equals(V3xOrgPost.class.getSimpleName())) {
            List<OrgPost> units = orgDao.getAllUnenabledPosts(accountId, null);
            return (List<V3xOrgPost>) OrgHelper.listPoTolistBo(units);
        }else if(entityTypeName.equals(V3xOrgLevel.class.getSimpleName())) {
            List<OrgLevel> units = orgDao.getAllUnenabledLevels(accountId, null);
            return (List<V3xOrgLevel>) OrgHelper.listPoTolistBo(units);
        }else if(entityTypeName.equals(V3xOrgMember.class.getSimpleName())) {
            List<OrgMember> units = orgDao.getAllUnenabledMembers(accountId, null);
            return (List<V3xOrgMember>) OrgHelper.listPoTolistBo(units);
        }else if(entityTypeName.equals(V3xOrgTeam.class.getSimpleName())) {
            List<OrgTeam> units = orgDao.getAllUnenabledTeams(accountId, null);
            return (List<V3xOrgTeam>) OrgHelper.listPoTolistBo(units);
        }else {
            return null;
        }
    }


    @Override
    public List<V3xOrgMember> getAllMembers(Long accountId, boolean includeDisabled) throws BusinessException {
    	List<V3xOrgMember> memberList = new ArrayList<V3xOrgMember>();
    	if(accountId!=null){
    		V3xOrgAccount account = orgManager.getAccountById(accountId);
    		if(account==null){
    			return memberList;
    		}else if(account.isGroup()){
    			accountId = null;
    		}
    	}
    	List<OrgMember> memberPOs = orgDao.getAllMemberPOByAccountId(accountId, null, null, null, null, null, null);
        @SuppressWarnings("unchecked")
        List<V3xOrgMember> list = (List<V3xOrgMember>) OrgHelper.listPoTolistBo(memberPOs);
        for (Iterator<V3xOrgMember> it = list.iterator(); it.hasNext();) {
            V3xOrgMember member = (V3xOrgMember) it.next();
            if (includeDisabled) {// 包含无效人员
                memberList.add((V3xOrgMember) member);
            } else {
                if (member.isValid()) {// 有效的人员
                    memberList.add((V3xOrgMember) member);
                } else {
                    continue;
                }
            }
        }
        Collections.sort(memberList, CompareSortEntity.getInstance());// 默认按照sort排序号排序
        return memberList;
    }

    @Override
    public List<V3xOrgPost> getAllPosts(Long accountId, boolean includeDisabled) throws BusinessException {
        List<OrgPost> postPOs = orgDao.getAllPostPO(accountId, null, null, null, null);
        @SuppressWarnings("unchecked")
        List<V3xOrgPost> list = (List<V3xOrgPost>) OrgHelper.listPoTolistBo(postPOs);
        List<V3xOrgPost> postList = new ArrayList<V3xOrgPost>();
        for (Iterator<V3xOrgPost> it = list.iterator(); it.hasNext();) {
            V3xOrgPost post = (V3xOrgPost) it.next();
            if (includeDisabled) {// 包含停用岗位
                postList.add((V3xOrgPost) post);
            } else {
                if (post.isValid()) {// 有效的岗位
                    postList.add((V3xOrgPost) post);
                } else {
                    continue;
                }
            }
        }
        Collections.sort(postList, CompareSortEntity.getInstance());// 默认按照sort排序号排序
        return postList;
    }

    @Override
    public List<V3xOrgRole> getAllRoles(Long accountId, boolean includeDisabled) throws BusinessException {
        List<OrgRole> rolePOs = orgDao.getAllRolePO(accountId, null, null, null, null);
        @SuppressWarnings("unchecked")
        List<V3xOrgRole> list = (List<V3xOrgRole>) OrgHelper.listPoTolistBo(rolePOs);
        List<V3xOrgRole> roleList = new ArrayList<V3xOrgRole>();
        for (Iterator<V3xOrgRole> it = list.iterator(); it.hasNext();) {
            V3xOrgRole role = (V3xOrgRole) it.next();
            if (includeDisabled) {// 包含停用角色
                roleList.add((V3xOrgRole) role);
            } else {
                if (role.isValid()) {// 有效启用的角色
                    roleList.add((V3xOrgRole) role);
                } else {
                    continue;
                }
            }
        }
        Collections.sort(roleList, CompareSortEntity.getInstance());// 默认按照sort排序号排序
        return roleList;
    }


    @Override
    public List<V3xOrgTeam> getAllTeams(Long accountId, boolean includeDisabled) throws BusinessException {
        List<OrgTeam> teamPOs = orgDao.getAllTeamPO(accountId, null, null, null, null, null);
        @SuppressWarnings("unchecked")
        List<V3xOrgTeam> list = (List<V3xOrgTeam>) OrgHelper.listPoTolistBo(teamPOs);
        List<V3xOrgTeam> teamList = new ArrayList<V3xOrgTeam>();
        for (Iterator<V3xOrgTeam> it = list.iterator(); it.hasNext();) {
            V3xOrgTeam team = (V3xOrgTeam) it.next();
            if (includeDisabled) {// 包含停用的组
                teamList.add((V3xOrgTeam) team);
            } else {
                if (team.isValid()) {// 状态为启用的组
                    teamList.add((V3xOrgTeam) team);
                } else {
                    continue;
                }
            }
        }
        Collections.sort(teamList, CompareSortEntity.getInstance());// 默认按照sort排序号排序
        return teamList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<V3xOrgMember> getMembersByDepartment(Long departmentId, Long accountId, Boolean firtLayer,
    		Boolean includeDisabled, Boolean includeOuterworker) throws BusinessException {
        //此处需要汇总主岗、副岗、兼职三个列表的结果综合起来
        List<V3xOrgMember> resultList = new UniqueList<V3xOrgMember>();

        List<V3xOrgMember> list = new ArrayList<V3xOrgMember>();

        if(null == includeOuterworker || includeOuterworker) {
            includeOuterworker = null;//如果要包含外部人员，dao中不传internal的条件
        } else {
            includeOuterworker = true;//不包含外部人员，internal是true
        }
        if(null == includeDisabled || includeDisabled) {
            includeDisabled = null;//包含无效人员，dao中不传enable的条件
        } else {
            includeDisabled = true;//不包含无效人员则传入true
        }

        List<OrgMember> poList = orgDao.getAllMemberPOByDepartmentId(departmentId, !firtLayer, null, includeOuterworker,
                includeDisabled, null, null, null);//此处已经针对无效人员过滤，下面不要判断了

        list = (List<V3xOrgMember>) OrgHelper.listPoTolistBo(poList);
        //这里不能进行部门id的校验，否则无法实现根据是否包含子部门的功能，况且DAO中已经根据传入的部门id做了过滤
        resultList.addAll(list);
//        for (Iterator<V3xOrgMember> it = list.iterator(); it.hasNext();) {
//            V3xOrgMember member = (V3xOrgMember) it.next();
//            if (departmentId.equals(member.getOrgDepartmentId())) {
//                resultList.add(member);
//            }
//        }

        //考虑兼职
        EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(
                RelationshipObjectiveName.class);
        objectiveIds.put(RelationshipObjectiveName.objective5Id, OrgConstants.MemberPostType.Second.name());
        List<V3xOrgRelationship> rels = orgCache.getV3xOrgRelationship(RelationshipType.Member_Post, null,
                accountId, objectiveIds);
        for (V3xOrgRelationship rel : rels) {
            if (rel.getObjective0Id() != null && rel.getObjective0Id().equals(departmentId)) {
                V3xOrgMember member;
                if (null == includeDisabled || includeDisabled) {
                	includeDisabled = true;
                    member = new V3xOrgMember(orgDao.getEntity(OrgMember.class, rel.getSourceId()));
                } else {
                    member = orgCache.getV3xOrgEntity(V3xOrgMember.class, rel.getSourceId());
                }
                if (null != member
                		&& (member.isValid() || null == includeDisabled || includeDisabled)
                		&& (null!=member.getIsInternal() && member.getIsInternal())) {
                    resultList.add(member);
                }
            }
        }
        //考虑副岗
        EnumMap<RelationshipObjectiveName, Object> objectiveIds2 = new EnumMap<RelationshipObjectiveName, Object>(
                RelationshipObjectiveName.class);
        objectiveIds2.put(RelationshipObjectiveName.objective5Id, OrgConstants.MemberPostType.Second.name());
        List<V3xOrgRelationship> rels2 = orgCache.getV3xOrgRelationship(RelationshipType.Member_Post,
                null, accountId, objectiveIds2);
        for (V3xOrgRelationship rel : rels2) {
            if (rel.getObjective0Id() != null && rel.getObjective0Id().equals(departmentId)) {
                V3xOrgMember member;
                if (null == includeDisabled ||includeDisabled) {
                	includeDisabled = true;
                    member = new V3xOrgMember(orgDao.getEntity(OrgMember.class, rel.getSourceId()));
                } else {
                    member = orgCache.getV3xOrgEntity(V3xOrgMember.class, rel.getSourceId());
                }
                if (member != null && (member.isValid() || includeDisabled) && member.getIsInternal()) {
                    resultList.add(member);
                }
            }
        }
        Collections.sort(resultList, CompareSortEntity.getInstance());
        return resultList;
    }

    @Override
    public List<V3xOrgMember> getMembersByLevel(Long levelId, boolean includeDisabled) throws BusinessException {
        List<V3xOrgMember> memberList = new ArrayList<V3xOrgMember>();
        V3xOrgLevel level = orgCache.getV3xOrgEntity(V3xOrgLevel.class, levelId);
        if (level == null) {
            return memberList;
        }
        List<V3xOrgMember> list = orgCache.getAllV3xOrgEntity(V3xOrgMember.class, level.getOrgAccountId());
        for (Iterator<V3xOrgMember> it = list.iterator(); it.hasNext();) {
            V3xOrgMember member = (V3xOrgMember) it.next();
            if (levelId.equals(member.getOrgLevelId()) && (member.isValid() || includeDisabled)
                    && member.getIsInternal()) // 必须是内部人员
            {
                memberList.add(member);
            }
        }
        //考虑兼职
        EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(
                RelationshipObjectiveName.class);
        objectiveIds.put(RelationshipObjectiveName.objective5Id, OrgConstants.MemberPostType.Concurrent.name());
        List<V3xOrgRelationship> rels = orgCache.getV3xOrgRelationship(RelationshipType.Member_Post, null,
                level.getOrgAccountId(), objectiveIds);
        for (V3xOrgRelationship rel : rels) {
            if (rel.getObjective2Id() != null && rel.getObjective2Id().equals(levelId)) {
                V3xOrgMember member;
                if (includeDisabled) {
                    member = new V3xOrgMember(orgDao.getEntity(OrgMember.class, rel.getSourceId()));
                } else {
                    member = orgCache.getV3xOrgEntity(V3xOrgMember.class, rel.getSourceId());
                }
                if (member != null && (member.isValid() || includeDisabled) && member.getIsInternal()) {
                    member.setSortId(rel.getSortId());
                    memberList.add(member);
                }
            }
        }
        Collections.sort(memberList, CompareSortEntity.getInstance());
        return memberList;
    }

    @Override
    public List<V3xOrgMember> getAllMembers(Long accountId, boolean includeDisabled, boolean isPaginate)
            throws BusinessException {
    	List<V3xOrgMember> memberList = new ArrayList<V3xOrgMember>();
    	if(accountId!=null){
    		V3xOrgAccount account = orgManager.getAccountById(accountId);
    		if(account==null){
    			return memberList;
    		}else if(account.isGroup()){
    			accountId = null;
    		}
    	}

        List<V3xOrgMember> list = orgCache.getAllV3xOrgEntity(V3xOrgMember.class, accountId);
        for (Iterator<V3xOrgMember> it = list.iterator(); it.hasNext();) {
            V3xOrgMember member = (V3xOrgMember) it.next();
            if ((member.isValid() //账号有效
                    && member.getState() == OrgConstants.MEMBER_STATE.ONBOARD.ordinal()) //在职
                    || includeDisabled) //包含无效账号
            {
                memberList.add((V3xOrgMember) member);
            }

        }
        Collections.sort(memberList, CompareSortEntity.getInstance());
        return memberList;
    }

    @Override
    public List<V3xOrgMember> getMembersByPost(Long postId, boolean includeDisabled) throws BusinessException {
        //TODO
        //V3xOrgPost post = orgCache.getV3xOrgEntity(V3xOrgPost.class, postId);
        return this.getMembersByPost(null, postId, includeDisabled);
    }

    @Override
    public List<V3xOrgMember> getMembersByPost(Long depId, Long postId, boolean includeDisabled)
            throws BusinessException {
        //TODO 增加无效人员
        V3xOrgPost post = orgManager.getPostById(postId);
        List<V3xOrgMember> list = orgCache.getAllV3xOrgEntity(V3xOrgMember.class, post.getOrgAccountId());
        List<V3xOrgMember> memberList = new ArrayList<V3xOrgMember>();
        for (Iterator<V3xOrgMember> it = list.iterator(); it.hasNext();) {
            V3xOrgMember member = (V3xOrgMember) it.next();
            if ((depId==null||depId.equals(member.getOrgDepartmentId())) && postId.equals(member.getOrgPostId()) && (member.isValid())
                    && member.getIsInternal()) // 必须是内部人员
            {
                memberList.add(member);
            } else {
                //考虑副岗
                if (MemberHelper.isSndPostContain(member, depId, postId) && (member.isValid())
                        && member.getIsInternal()) {
                    memberList.add(member);
                }
            }
        }
        //考虑兼职
        EnumMap<RelationshipObjectiveName, Object> enummap = new EnumMap<RelationshipObjectiveName, Object>(
                RelationshipObjectiveName.class);
        enummap.put(RelationshipObjectiveName.objective5Id, "Concurrent");
        List<V3xOrgRelationship> rels = orgCache.getV3xOrgRelationship(RelationshipType.Member_Post, (Long)null,
                (Long)null, enummap);
        for (V3xOrgRelationship rel : rels) {
            if (postId.equals(rel.getObjective1Id()) && (depId==null||depId.equals(rel.getObjective0Id()))) {
                V3xOrgMember member = (V3xOrgMember) orgCache.getV3xOrgEntity(V3xOrgMember.class, rel.getSourceId());
                if (member == null) {
                    continue;
                }

                if ((member.isValid()) && member.getIsInternal()) {
                    //member = member.decorate();
                    //兼职人员要按兼职单位排序号排序
                    member.setSortId(rel.getSortId());
                    memberList.add(member);
                }
            }
        }
        Collections.sort(memberList, CompareSortEntity.getInstance());
        return memberList;
    }

    @Override
    public void updateTeam(V3xOrgTeam team) throws BusinessException {
        if (null == team)
            throw new BusinessException("更新组实体对象为空！");
        /**
         * 重名比较原则
         * 1.同一个单位（集团）下的组比较
         * 2.除了个人组，其他组之间名称不能重复
         * 3.不同人的个人组名称可以重复
         */
      	List<V3xOrgTeam> allEnts = orgManager.getAllTeams(team.getOrgAccountId());
      	int teamType = team.getType();
      	String teamName = team.getName();
      	Long teamOwnerId = team.getOwnerId();
      	Long teamId = team.getId();
      	for(V3xOrgTeam ent : allEnts){
      		int type = ent.getType();
      		String name = ent.getName();
      		Long id = ent.getId();
      		if(OrgConstants.TEAM_TYPE.PERSONAL.ordinal() == teamType){
      			//个人组只和自己创建的个人组之间进行比较
      			if(!teamId.equals(id) && OrgConstants.TEAM_TYPE.PERSONAL.ordinal() == type && teamName.equals(name) && teamOwnerId.equals(ent.getOwnerId())){
      				throw new BusinessException(ResourceUtil.getString("org.validate.team1"));
      			}
      		}else{
      			if(!teamId.equals(id) && OrgConstants.TEAM_TYPE.PERSONAL.ordinal() != type && teamName.equals(name)){
      				throw new BusinessException(ResourceUtil.getString("org.validate.team2"));
      			}

      		}

      	}

        //TODO 校验数据
        // 实例化
        orgDao.update((OrgTeam) team.toPO());
        // 更新关系表数据
        // 先删除这个组的组员的所有关系
        orgDao.deleteOrgRelationshipPO(RelationshipType.Team_Member.name(), team.getId(), null, null);
        // 实例化组员到关系表中
        dealTeamMemberRels(team);
        // 触发事件
        UpdateTeamEvent event = new UpdateTeamEvent(this);
        event.setTeam(team);
        EventDispatcher.fireEvent(event);
    }

    @Override
    public boolean isPropertyDuplicated(String entityClassName, String property, Object value) throws BusinessException {
        return orgDao.isPropertyDuplicated(OrgHelper.getEntityTypeBySimpleName(entityClassName), property, value);
    }

    private <T extends V3xOrgEntity> boolean isPropertyDuplicated(Class<T> entityClassName, String property, Object value, Long accountId){
        return orgDao.isPropertyDuplicated(entityClassName, property, value, accountId);
    }

    public boolean isPropertyDuplicated(String entityClassName, String property, Object value, Long accountId)
            throws BusinessException {
        return this.isPropertyDuplicated(OrgHelper.getEntityTypeBySimpleName(entityClassName), property, value, accountId);
    }

    @Override
    public boolean isPropertyDuplicated(String entityClassName, String property, Object value, Long accountId, Long entId)
            throws BusinessException {
        return orgDao.isPropertyDuplicated(OrgHelper.getEntityTypeBySimpleName(entityClassName), property, value, accountId, entId);
    }

    @Override
    public Integer getExtMemberMaxSortNum(Long accountId) throws BusinessException {
        return orgDao.getExtMemberMaxSortId(accountId);
    }

    @Override
    public Integer getMaxSortNum(String entityClassName, Long accountId) throws BusinessException {
        return orgDao.getMaxSortId(OrgHelper.getEntityTypeBySimpleName(entityClassName), accountId);
    }
    @Override
    public Integer getMaxOutternalDeptSortNum(Long accountId) throws BusinessException {
        return orgDao.getMaxOutternalDeptSortId(accountId);
    }

    @Override
    public void cleanMemberAccAndSelfDeptRoles(V3xOrgMember member, Set<Long> roleIds) throws BusinessException {
        V3xOrgMember oldMember = orgManager.getMemberById(member.getId());
        Long accountId = oldMember.getOrgAccountId();
        EnumMap<RelationshipObjectiveName, Object> enummap = new EnumMap<RelationshipObjectiveName, Object>(RelationshipObjectiveName.class);
        List<Long> objective0Ids = new ArrayList<Long>(0);
        objective0Ids.add(oldMember.getOrgAccountId());
        objective0Ids.add(oldMember.getOrgDepartmentId());
        enummap.put(RelationshipObjectiveName.objective0Id, objective0Ids);

        List<Long> sourceIds = orgManager.getUserDomainIDs(oldMember.getId(), oldMember.getOrgAccountId(),
                ORGENT_TYPE.Member.name());

        List<Long> _accountIds = new ArrayList<Long>(1);
        if(accountId != null && !accountId.equals(V3xOrgEntity.VIRTUAL_ACCOUNT_ID)){
            _accountIds.add(accountId);
        }

        boolean isEmpRole = false;
        if(null == roleIds || roleIds.size() == 0) {
            isEmpRole = true;
        }

        List<V3xOrgRelationship> relList = orgCache.getV3xOrgRelationship(RelationshipType.Member_Role, sourceIds, _accountIds, enummap);
        //过滤插件角色列表
        List<V3xOrgRole> disRoleList = orgManager.getPlugDisableRole(accountId);
        for (V3xOrgRelationship bo : relList) {
            V3xOrgRole role = orgManager.getRoleById(bo.getObjective1Id());
            if(null == role) continue;//OA-56390
            if(!isEmpRole && (roleIds.contains(role.getId()) || disRoleList.contains(role))) {
                if (null != role) {
                    //只删除这个人所在部门的部门角色
                    if(OrgConstants.ROLE_BOND.DEPARTMENT.ordinal() == role.getBond()) {
                        if(bo.getObjective0Id().equals(oldMember.getOrgDepartmentId())
                                //OA-31919 人员调整部门需要保留部门分管领导角色
                                && !Role_NAME.DepLeader.name().equals(role.getCode())) {
                            orgDao.deleteOrgRelationshipPOById(bo.getId());
                        } else {
                            continue;
                        }
                    } else{
                        orgDao.deleteOrgRelationshipPOById(bo.getId());
                    }
                }
            } else {
                continue;
            }
        }
    }

    @Override
    public void addRole2Entity(Long roleId, Long unitId, V3xOrgEntity entity) throws BusinessException {
        V3xOrgRelationship relBO = new V3xOrgRelationship();
        relBO.setId(UUIDLong.longUUID());
        relBO.setKey(RelationshipType.Member_Role.name());
        relBO.setSourceId(entity.getId());
        //V3xOrgRole role = orgManager.getRoleById(roleId);
        V3xOrgUnit unit = orgManager.getUnitById(unitId);
        relBO.setObjective0Id(unitId);
        //这里不要对部门角色处理，如果是部门角色要使用同名函数，传入部门对象
//        if(role.getBond()==OrgConstants.ROLE_BOND.DEPARTMENT.ordinal()){
//        	List<V3xOrgMember> memberlist= orgManager.getMembersByType(OrgHelper.getEntityTypeByClassSimpleName(entity.getClass().getSimpleName()), entity.getId());
//        	if(memberlist.size()>0){
//        		relBO.setObjective0Id(memberlist.get(0).getOrgDepartmentId());
//        	}
//        }
        relBO.setObjective1Id(roleId);
        relBO.setObjective5Id(entity.getEntityType());//保存Member/Department/Post/Level等
        relBO.setOrgAccountId(unit.getOrgAccountId());
        List<OrgRelationship> relPOs = new ArrayList<OrgRelationship>(1);
        relPOs.add((OrgRelationship) relBO.toPO());
        /** 先将这些实体与这些角色关系先删除 */

        EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(
                RelationshipObjectiveName.class);
        objectiveIds.put(RelationshipObjectiveName.objective1Id, roleId);
        objectiveIds.put(RelationshipObjectiveName.objective0Id, unitId);

        orgDao.deleteOrgRelationshipPO(RelationshipType.Member_Role.name(), entity.getId(), unit.getOrgAccountId(),
                objectiveIds);
        /** 插入关系表实体与角色之间的关系 */
        orgDao.insertOrgRelationship(relPOs);
    }

    @Override
    public void deleteEntityRole(Long roleId, Long unitId, V3xOrgEntity entity) throws BusinessException {
        V3xOrgUnit unit  = orgManager.getUnitById(unitId);
        if(unit == null){
        	return;
        }
        EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(
        		RelationshipObjectiveName.class);
        objectiveIds.put(RelationshipObjectiveName.objective1Id, roleId);
        objectiveIds.put(RelationshipObjectiveName.objective0Id, unitId);

        List<V3xOrgRelationship> rels = orgCache.getV3xOrgRelationship(RelationshipType.Member_Role, entity.getId(), unit.getOrgAccountId(), objectiveIds);
        this.deleteOrgRelationships(rels);
    }

    @Override
    public void addRole2Entity(Long roleId, Long unitId, V3xOrgEntity entity,List<V3xOrgRelationship> delRels,List<V3xOrgRelationship> addRels) throws BusinessException {
        V3xOrgRelationship relBO = new V3xOrgRelationship();
        relBO.setId(UUIDLong.longUUID());
        relBO.setKey(RelationshipType.Member_Role.name());
        relBO.setSourceId(entity.getId());

        V3xOrgUnit unit = orgManager.getUnitById(unitId);
        relBO.setObjective0Id(unitId);
        relBO.setObjective1Id(roleId);
        relBO.setObjective5Id(entity.getEntityType());//保存Member/Department/Post/Level等
        relBO.setOrgAccountId(unit.getOrgAccountId());
        /** 先将这些实体与这些角色关系先删除 */
        EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(
                RelationshipObjectiveName.class);
        objectiveIds.put(RelationshipObjectiveName.objective1Id, roleId);
        objectiveIds.put(RelationshipObjectiveName.objective0Id, unitId);

        delRels.addAll(orgCache.getV3xOrgRelationship(RelationshipType.Member_Role, entity.getId(), unit.getOrgAccountId(),
                objectiveIds));
        /** 插入关系表实体与角色之间的关系 */
        addRels.add(relBO);
    }

    @Override
    public void addRole2Members(Long roleId, Long unitId, List<V3xOrgMember> members) throws BusinessException {
    	V3xOrgUnit unit = orgManager.getUnitById(unitId);
    	List<V3xOrgRelationship> deleteRels = new ArrayList<V3xOrgRelationship>();
    	List<OrgRelationship> addRelPos = new ArrayList<OrgRelationship>();
    	for(V3xOrgMember member : members){
    		//要删除的关系
    		EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(
    				RelationshipObjectiveName.class);
    		objectiveIds.put(RelationshipObjectiveName.objective1Id, roleId);
    		objectiveIds.put(RelationshipObjectiveName.objective0Id, unitId);
    		List<V3xOrgRelationship> rels = orgCache.getV3xOrgRelationship(RelationshipType.Member_Role, member.getId(), unit.getOrgAccountId(),
    				objectiveIds);
    		deleteRels.addAll(rels);

    		//要新增的关系
    		V3xOrgRelationship relBO = new V3xOrgRelationship();
    		relBO.setId(UUIDLong.longUUID());
    		relBO.setKey(RelationshipType.Member_Role.name());
    		relBO.setSourceId(member.getId());

    		relBO.setObjective0Id(unitId);
    		relBO.setObjective1Id(roleId);
    		relBO.setObjective5Id(ORGENT_TYPE.Member.name());
    		relBO.setOrgAccountId(unit.getOrgAccountId());
    		addRelPos.add((OrgRelationship) relBO.toPO());
    	}
    	 /** 先将这些实体与这些角色关系先删除 */
    	deleteOrgRelationships(deleteRels);
    	/** 插入关系表实体与角色之间的关系 */
    	orgDao.insertOrgRelationship(addRelPos);
    }

    @Override
    public void addRole2EntitywithoutDel(Long roleId, Long unitId, V3xOrgEntity entity) throws BusinessException {
        V3xOrgRelationship relBO = new V3xOrgRelationship();
        relBO.setId(UUIDLong.longUUID());
        relBO.setKey(RelationshipType.Member_Role.name());
        relBO.setSourceId(entity.getId());
        //V3xOrgRole role = orgManager.getRoleById(roleId);
        V3xOrgUnit unit = orgManager.getUnitById(unitId);
        relBO.setObjective0Id(unitId);
        //这里不要对部门角色处理，如果是部门角色要使用同名函数，传入部门对象
//        if(role.getBond()==OrgConstants.ROLE_BOND.DEPARTMENT.ordinal()){
//        	List<V3xOrgMember> memberlist= orgManager.getMembersByType(OrgHelper.getEntityTypeByClassSimpleName(entity.getClass().getSimpleName()), entity.getId());
//        	if(memberlist.size()>0){
//        		relBO.setObjective0Id(memberlist.get(0).getOrgDepartmentId());
//        	}
//        }
        relBO.setObjective1Id(roleId);
        relBO.setObjective5Id(entity.getEntityType());//保存Member/Department/Post/Level等
        relBO.setOrgAccountId(unit.getOrgAccountId());
        List<OrgRelationship> relPOs = new ArrayList<OrgRelationship>(1);
        relPOs.add((OrgRelationship) relBO.toPO());
        /** 先将这些实体与这些角色关系先删除 *//*
        EnumMap<OrgConstants.RelationshipObjectiveName, Object> objectiveIds = new EnumMap<OrgConstants.RelationshipObjectiveName, Object>(
                OrgConstants.RelationshipObjectiveName.class);
        objectiveIds.put(OrgConstants.RelationshipObjectiveName.objective1Id, roleId);
        objectiveIds.put(OrgConstants.RelationshipObjectiveName.objective0Id, unitId);
        orgDao.deleteOrgRelationshipPO(OrgConstants.RelationshipType.Member_Role.name(), entity.getId(), unit.getOrgAccountId(),
                objectiveIds);*/
        /** 插入关系表实体与角色之间的关系 */
        orgDao.insertOrgRelationship(relPOs);
    }

    @Override
    public void addConcurrentRoles2Entity(List<Long> roleIds, Long unitId, V3xOrgEntity entity) throws BusinessException {
        List<OrgRelationship> relPOs = new ArrayList<OrgRelationship>(1);
        for (Long roleId : roleIds) {
            V3xOrgRelationship relBO = new V3xOrgRelationship();
            relBO.setId(UUIDLong.longUUID());
            relBO.setKey(RelationshipType.Member_Role.name());
            relBO.setSourceId(entity.getId());
            //V3xOrgRole role = orgManager.getRoleById(roleId);
            relBO.setObjective0Id(unitId);
            //这里不要对部门角色处理，如果是部门角色要使用同名函数，传入部门对象
            relBO.setObjective1Id(roleId);
            relBO.setObjective5Id(entity.getEntityType());//保存Member/Department/Post/Level等
            relBO.setOrgAccountId(unitId);
            relPOs.add((OrgRelationship) relBO.toPO());
        }
        /** 先将这些实体与这些角色关系先删除 */
        //这里不删除，在外面删除
//        EnumMap<OrgConstants.RelationshipObjectiveName, Object> objectiveIds = new EnumMap<OrgConstants.RelationshipObjectiveName, Object>(
//                OrgConstants.RelationshipObjectiveName.class);
//        objectiveIds.put(OrgConstants.RelationshipObjectiveName.objective0Id, unitId);
//        orgDao.deleteOrgRelationshipPO(OrgConstants.RelationshipType.Member_Role.name(), entity.getId(), unitId,
//                objectiveIds);
        /** 插入关系表实体与角色之间的关系 */
        orgDao.insertOrgRelationship(relPOs);
    }

    @Override
    public void addRole2Entity(Long roleId, Long accountId, V3xOrgEntity entity, V3xOrgDepartment deptvo) throws BusinessException {
        V3xOrgRelationship relBO = new V3xOrgRelationship();
        relBO.setId(UUIDLong.longUUID());
        relBO.setKey(RelationshipType.Member_Role.name());
        relBO.setSourceId(entity.getId());
        V3xOrgRole role = orgManager.getRoleById(roleId);
        if (role.getBond() == OrgConstants.ROLE_BOND.DEPARTMENT.ordinal()||"DeptSpace".equals(role.getCode())) {
            List<V3xOrgMember> memberlist = orgManager.getMembersByType(
                    OrgHelper.getEntityTypeByClassSimpleName(entity.getClass().getSimpleName()), entity.getId());
            if (memberlist.size() > 0) {
                relBO.setObjective0Id(memberlist.get(0).getOrgDepartmentId());
            }
            if (deptvo != null) {
                relBO.setObjective0Id(deptvo.getId());
            }
        } else {
            relBO.setObjective0Id(accountId);
        }
        relBO.setObjective1Id(roleId);
        relBO.setObjective5Id(entity.getEntityType());//保存Member/Department/Post/Level等

        relBO.setOrgAccountId(accountId);

        List<OrgRelationship> relPOs = new ArrayList<OrgRelationship>(1);
        relPOs.add((OrgRelationship) relBO.toPO());
        /** 先将这些实体与这些角色关系先删除 */


        EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(RelationshipObjectiveName.class);
        objectiveIds.put(RelationshipObjectiveName.objective1Id, roleId);
        objectiveIds.put(RelationshipObjectiveName.objective0Id, accountId);

        orgDao.deleteOrgRelationshipPO(RelationshipType.Member_Role.name(), entity.getId(), entity.getOrgAccountId(), objectiveIds);
        /** 插入关系表实体与角色之间的关系 */
        orgDao.insertOrgRelationship(relPOs);
    }

    @Override
    public void addRole2Entity(Long roleId, Long accountId, V3xOrgEntity entity, V3xOrgDepartment deptvo,List<V3xOrgRelationship> delRels,List<V3xOrgRelationship> addRels) throws BusinessException {
        V3xOrgRelationship relBO = new V3xOrgRelationship();
        relBO.setId(UUIDLong.longUUID());
        relBO.setKey(RelationshipType.Member_Role.name());
        relBO.setSourceId(entity.getId());
        V3xOrgRole role = orgManager.getRoleById(roleId);
        if (role.getBond() == OrgConstants.ROLE_BOND.DEPARTMENT.ordinal()||"DeptSpace".equals(role.getCode())) {
            List<V3xOrgMember> memberlist = orgManager.getMembersByType(
                    OrgHelper.getEntityTypeByClassSimpleName(entity.getClass().getSimpleName()), entity.getId());
            if (memberlist.size() > 0) {
                relBO.setObjective0Id(memberlist.get(0).getOrgDepartmentId());
            }
            if (deptvo != null) {
                relBO.setObjective0Id(deptvo.getId());
            }
        } else {
            relBO.setObjective0Id(accountId);
        }
        relBO.setObjective1Id(roleId);
        relBO.setObjective5Id(entity.getEntityType());//保存Member/Department/Post/Level等

        relBO.setOrgAccountId(accountId);

        /** 先将这些实体与这些角色关系先删除 */


        EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(RelationshipObjectiveName.class);
        objectiveIds.put(RelationshipObjectiveName.objective1Id, roleId);
        objectiveIds.put(RelationshipObjectiveName.objective0Id, accountId);

        delRels.addAll(orgCache.getV3xOrgRelationship(RelationshipType.Member_Role, entity.getId(), entity.getOrgAccountId(), objectiveIds));
        /** 插入关系表实体与角色之间的关系 */
        addRels.add(relBO);
    }

    @Override
    public void addRole2Entities(Long roleId, Long accountId, List<V3xOrgEntity> entities, Long departmentId)
            throws BusinessException {
        List<OrgRelationship> relPOs = new ArrayList<OrgRelationship>(entities.size());
        int i=0;
        for (V3xOrgEntity entity : entities) {
            V3xOrgRelationship relBO = new V3xOrgRelationship();
            relBO.setId(UUIDLong.longUUID());
            relBO.setKey(RelationshipType.Member_Role.name());
            relBO.setSourceId(entity.getId());
            V3xOrgRole role = orgManager.getRoleById(roleId);
            if (role.getBond() == OrgConstants.ROLE_BOND.DEPARTMENT.ordinal() || "DeptSpace".equals(role.getCode())) {
                List<V3xOrgMember> memberlist = orgManager.getMembersByType(
                        OrgHelper.getEntityTypeByClassSimpleName(entity.getClass().getSimpleName()), entity.getId());
                if (memberlist.size() > 0) {
                    relBO.setObjective0Id(memberlist.get(0).getOrgDepartmentId());
                }
                if (departmentId != null) {
                    relBO.setObjective0Id(departmentId);
                }
            } else {
                relBO.setObjective0Id(accountId);
            }
            relBO.setObjective1Id(roleId);
            relBO.setObjective5Id(entity.getEntityType());//保存Member/Department/Post/Level等
            relBO.setOrgAccountId(accountId);
            relBO.setSortId(Long.valueOf(i++));

            relPOs.add((OrgRelationship) relBO.toPO());
            /** 先将这些实体与这些角色关系先删除 */
            EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(
                    RelationshipObjectiveName.class);
            objectiveIds.put(RelationshipObjectiveName.objective1Id, roleId);
            objectiveIds.put(RelationshipObjectiveName.objective0Id, accountId);

            orgDao.deleteOrgRelationshipPO(RelationshipType.Member_Role.name(), entity.getId(),
                    entity.getOrgAccountId(), objectiveIds);
        }
        /** 插入关系表实体与角色之间的关系 */
        orgDao.insertOrgRelationship(relPOs);
    }

    @Override
    public void deleteRole2Entity(Long roleId, Long unitId, List<V3xOrgMember> members) throws BusinessException {
        isCanDeleteRoletoEnt(roleId, unitId, members);
        //如果取消部门主管角色，则需要判断是否开启了部门空间
        if(unitId!=null && orgManager.getUnitById(unitId)!=null){
        	if(UnitType.Department.equals(orgManager.getUnitById(unitId).getType())){
        		V3xOrgRole roleById = orgManager.getRoleById(roleId);
        		if(roleById.getCode().equals(Role_NAME.DepManager.name())){
        			List<V3xOrgMember> membersByRole = orgManager.getMembersByRole(unitId, roleId);
        			if(membersByRole.size()>0&&members.size()==0&&spaceApi.isCreateDepartmentSpace(unitId)){
        				throw new BusinessException(ResourceUtil.getString("org.validate.space"));
        			}
        		}
        	}
        }
        /** 先将这些实体与这些角色关系先删除 */
        EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(RelationshipObjectiveName.class);
        objectiveIds.put(RelationshipObjectiveName.objective1Id, roleId);
        if(unitId != null){
        	objectiveIds.put(RelationshipObjectiveName.objective0Id, unitId);
        }

        orgDao.deleteOrgRelationshipPO(RelationshipType.Member_Role.name(), null, null, objectiveIds);
        List<V3xOrgRelationship> remianRelationship= orgCache.getV3xOrgRelationship(RelationshipType.Member_Role, (Long)null, (Long)null, objectiveIds);
        if(Strings.isNotEmpty(remianRelationship)){
            List<OrgRelationship> rs = new ArrayList<OrgRelationship>();
            for(V3xOrgRelationship vr : remianRelationship){
                rs.add((OrgRelationship) vr.toPO());
            }
            orgCache.cacheRemoveRelationship(rs);
        }

    }

    @Override
    public List<V3xOrgMember> getmembersByEntity(String s) throws BusinessException {
    	List<V3xOrgEntity> entities = orgManager.getEntities(s);
    	List<V3xOrgMember> delmembers = new ArrayList<V3xOrgMember>();
    	if(entities==null){
    		return delmembers;
    	}
    	for (V3xOrgEntity entity : entities) {


    	Class<? extends V3xOrgEntity> entityType = OrgHelper.getEntityType(entity);
    	if(entityType==null){
    		return delmembers;
    	}
 		if("V3xOrgAccount".equals(entityType.getSimpleName())){
 			delmembers.addAll(orgManager.getAllMembers(entity.getId()));
 		}else if("V3xOrgPost".equals(entityType.getSimpleName())){
 			delmembers.addAll(this.getMembersByPost(entity.getId(), false));
 		}else if("V3xOrgDepartment".equals(entityType.getSimpleName())){
 			delmembers.addAll(orgManager.getMembersByDepartment(entity.getId(), false));
 		}else if("V3xOrgLevel".equals(entityType.getSimpleName())){
 			delmembers.addAll(this.getMembersByLevel(entity.getId(), false));
 		}else if("V3xOrgTeam".equals(entityType.getSimpleName())){
 			delmembers.addAll(orgManager.getMembersByTeam(entity.getId()));
 		}else if("V3xOrgMember".equals(entityType.getSimpleName())){
 			delmembers.add(orgManager.getMemberById(entity.getId()));
 		}
    	}
 		return delmembers;

    }
    @Override
    public List<V3xOrgMember> getmembersByEntity(V3xOrgEntity entity) throws BusinessException {

    	List<V3xOrgMember> delmembers = new ArrayList<V3xOrgMember>();
    	if(entity==null){
    		return delmembers;
    	}

    	Class<? extends V3xOrgEntity> entityType = OrgHelper.getEntityType(entity);
    	if(entityType==null){
    		return delmembers;
    	}
 		if("V3xOrgAccount".equals(entityType.getSimpleName())){
 			delmembers.addAll(orgManager.getAllMembers(entity.getId()));
 		}else if("V3xOrgPost".equals(entityType.getSimpleName())){
 			delmembers.addAll(this.getMembersByPost(entity.getId(), false));
 		}else if("V3xOrgDepartment".equals(entityType.getSimpleName())){
 			delmembers.addAll(orgManager.getMembersByDepartment(entity.getId(), false));
 		}else if("V3xOrgLevel".equals(entityType.getSimpleName())){
 			delmembers.addAll(this.getMembersByLevel(entity.getId(), false));
 		}else if("V3xOrgTeam".equals(entityType.getSimpleName())){
 			delmembers.addAll(orgManager.getMembersByTeam(entity.getId()));
 		}else if("V3xOrgMember".equals(entityType.getSimpleName())){
 			delmembers.add(orgManager.getMemberById(entity.getId()));
 		}

 		return delmembers;

    }
    @Override
    public void deleteRoleandEntity(Long roleId, Long unitId,V3xOrgEntity entity) throws BusinessException {
        isCanDeleteRoletoEnt(roleId, unitId, Strings.newArrayList(entity));

        EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(RelationshipObjectiveName.class);
        objectiveIds.put(RelationshipObjectiveName.objective1Id, roleId);
        objectiveIds.put(RelationshipObjectiveName.objective0Id, unitId);
//      EnumMap<OrgConstants.RelationshipObjectiveName, Object> objectiveIds = new EnumMap<OrgConstants.RelationshipObjectiveName, Object>(
//      OrgConstants.RelationshipObjectiveName.class);
//objectiveIds.put(OrgConstants.RelationshipObjectiveName.objective1Id, roleId);
//objectiveIds.put(OrgConstants.RelationshipObjectiveName.objective0Id, unitId);
//orgDao.deleteOrgRelationshipPO(OrgConstants.RelationshipType.Member_Role.name(), entity.getId(), unit.getOrgAccountId(),
//      objectiveIds);

        /** 先将这些实体与这些角色关系先删除 */
        orgDao.deleteOrgRelationshipPO(RelationshipType.Member_Role.name(), entity.getId(), null,
                objectiveIds);

    }

	public void isCanDeleteRoletoEnt(Long roleId, Long unitId, List<? extends V3xOrgEntity> entities) throws BusinessException {
		if(entities==null){
			return;
		}
		List<V3xOrgMember> members  = new ArrayList<V3xOrgMember>();
		for (V3xOrgEntity entity : entities) {
		    members.addAll(getmembersByEntity(entity));
        }

    	Set<V3xOrgMember> newMembers = new HashSet<V3xOrgMember>(members);


        List<V3xOrgMember> oldmembers = new ArrayList<V3xOrgMember>();
        EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(RelationshipObjectiveName.class);
        objectiveIds.put(RelationshipObjectiveName.objective1Id, roleId);
        if(unitId != null){
            objectiveIds.put(RelationshipObjectiveName.objective0Id, unitId);
        }

        //校验不允许解除角色关系
        List<OrgRelationship> orgRelationshipPO = orgDao.getOrgRelationshipPO(RelationshipType.Member_Role, null, null, objectiveIds, null);
        for (OrgRelationship orgRelationship : orgRelationshipPO) {
            oldmembers.addAll(getmembersByEntity(orgRelationship.getObjective5Id()+"|"+String.valueOf(orgRelationship.getSourceId())));
        }

        Set<V3xOrgMember> checkDelMembers = new HashSet<V3xOrgMember>();
        for (V3xOrgMember oldmember : oldmembers) {
            if(!newMembers.contains(oldmember)){
                checkDelMembers.add(oldmember);
            }
        }

        Set<V3xOrgMember> checkNewMembers = new HashSet<V3xOrgMember>();
        for(V3xOrgMember m : members) {
            if(!oldmembers.contains(m)){
                checkNewMembers.add(m);
            }
        }

        String code = orgManager.getRoleById(roleId).getCode();
        for(CheckPrivUpdate cprid : getCheckPrivUpdates()) {
            String iscandel = cprid.processUpdate(checkNewMembers, checkDelMembers, code, unitId);
            if(iscandel != null) {
                throw new BusinessException(iscandel);
            }
        }
	}
	/**
	 * 仅提供给人员修改校验角色使用
	 */
	public void isCanDeleteMembertoRole(V3xOrgMember member, Long unitId, List<Long> roleIds) throws BusinessException {
	    Long memberId = member.getId();
	    List<MemberRole> memroles = new ArrayList<MemberRole>();
		List<MemberRole> memroles0 = orgManager.getMemberRoles(memberId, AppContext.currentAccountId());
		List<Long> oldRoles = new ArrayList<Long>();
		Set<Long> newRoleIds = new HashSet<Long>(roleIds);

		for (int i = 0; i < memroles0.size(); i++) {
			MemberRole mrole = memroles0.get(i);
			oldRoles.add(mrole.getRole().getId());
			if(newRoleIds.contains(mrole.getRole().getId()) || !Strings.equals(mrole.getMemberId(),memberId)){//减少了哪些角色
			}else{
				memroles.add(mrole);
			}
		}

		for(Long newRoleId : newRoleIds){
			if(!oldRoles.contains(newRoleId)){//新增的角色，暂时只考虑部门和单位公文收发员
				V3xOrgRole role = orgManager.getRoleById(newRoleId);
	            String code = role.getCode();
	            if(!code.equals(Role_NAME.Departmentexchange.name()) && !code.equals(Role_NAME.Accountexchange.name())){
	            	continue;
	            }
	            for(CheckPrivUpdate cprid : getCheckPrivUpdates()) {
	                cprid.processUpdate(Strings.newArrayList(member),null, code, code.equals(Role_NAME.Departmentexchange.name())?member.getOrgDepartmentId():unitId);
	            }
			}
		}

		for (MemberRole checkrole : memroles) {
            String code = checkrole.getRole().getCode();
            for(CheckPrivUpdate cprid : getCheckPrivUpdates()) {
                String iscandel = cprid.processUpdate(null, Strings.newArrayList(member), code, code.equals(Role_NAME.Departmentexchange.name())?member.getOrgDepartmentId():unitId);
                if(iscandel != null) {
                    throw new BusinessException(iscandel);
                }
            }
		}
	}

    @Override
    public List<V3xOrgRelationship> getAllOutConcurrentPostByAccount(Long accountId) throws BusinessException {
        //TODO
        EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(
                RelationshipObjectiveName.class);
        objectiveIds.put(RelationshipObjectiveName.objective5Id, OrgConstants.MemberPostType.Concurrent.name());
        return orgCache.getV3xOrgRelationship(RelationshipType.Member_Post, null, accountId, objectiveIds);
    }

    @Override
    public List<V3xOrgRelationship> findAllSidelineAccountCntPost(Long accountId) {
        EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(
                RelationshipObjectiveName.class);
        objectiveIds.put(RelationshipObjectiveName.objective5Id, OrgConstants.MemberPostType.Concurrent.name());
        return orgCache.getV3xOrgRelationship(RelationshipType.Member_Post, null, accountId, objectiveIds);
    }

    @Override
    public void addOrgRelationship(V3xOrgRelationship rel) throws BusinessException {
        List<OrgRelationship> rels = new ArrayList<OrgRelationship>();
        rels.add((OrgRelationship)rel.toPO());
        orgDao.insertOrgRelationship(rels);
    }

    @Override
	public void saveSycGroupRole(V3xOrgAccount account) throws BusinessException {
		List<OrgRole> rolelist = orgDao.getBaseRole();
		List<OrgRole> newrolelist = new ArrayList<OrgRole>();

		List<V3xOrgRelationship> newRels = new ArrayList<V3xOrgRelationship>();
		List<PrivRoleMenu> newroleMenulist = new ArrayList<PrivRoleMenu>();

		for (OrgRole orgRole : rolelist) {
			//不同步系统管理角色
			OrgConstants.Role_SYSTEM_NAME systemRoleName =null;
			try{
				systemRoleName = OrgConstants.Role_SYSTEM_NAME.valueOf(orgRole.getName());
	        }
	        catch(Exception e){
	            //ignore
	        }
			if(systemRoleName!=null){
				continue;
			}
			OrgRole newroel = new OrgRole();
			newroel.setIdIfNew();
			newroel.setOrgAccountId(account.getId());
			newroel.setBenchmark(orgRole.isBenchmark());
			newroel.setBond(orgRole.getBond());

			newroel.setCategory(orgRole.getCategory());
			newroel.setCode(orgRole.getCode());
			newroel.setCreateTime(DateUtil.currentDate());
			newroel.setDeleted(orgRole.isDeleted());
			newroel.setDescription(orgRole.getDescription());
			newroel.setEnable(orgRole.isEnable());
			newroel.setName(orgRole.getName());
			newroel.setSortId(orgRole.getSortId());
			newroel.setStatus(orgRole.getStatus());
			newroel.setType(RoleTypeEnum.relativeflag.getKey());
			//newroel.setType(orgRole.getType());
			newroel.setUpdateTime(orgRole.getUpdateTime());
			newrolelist.add(newroel);
			//集团与单位角色关系建立
			V3xOrgRelationship newrel = new V3xOrgRelationship();
    		newrel.setId(UUIDLong.longUUID());
    		newrel.setSourceId(newroel.getId());
    		newrel.setObjective0Id(orgRole.getId());
    		newrel.setKey(RelationshipType.Banchmark_Role.name());

    		newRels.add(newrel);

			//同步角色与资源关系
    		List<PrivRoleMenu> roleMenus = orgDao.getRoleMenu(orgRole);
//			String pline=VersionEnums.getTextForKey();
			for (PrivRoleMenu privRoleMenu : roleMenus) {
				PrivRoleMenu newprivRoleMenu = new PrivRoleMenu();
				newprivRoleMenu.setIdIfNew();
				newprivRoleMenu.setMenuid(privRoleMenu.getMenuid());
				newprivRoleMenu.setModifiable(privRoleMenu.getModifiable());
//				newprivRoleMenu.setProductLine(Integer.parseInt(pline));
				newprivRoleMenu.setRoleid(newroel.getId());
				newroleMenulist.add(newprivRoleMenu);
			}
		}

		orgDao.insertOrgRole(newrolelist);
		addOrgRelationships(newRels);
		roleMenuDao.insertRoleMenuPatchAll(newroleMenulist);
	}

    @Override
    public OrganizationMessage deleteRole(V3xOrgRole role) throws BusinessException {
        OrganizationMessage message = new OrganizationMessage();
        if(null == role) {
            message.addErrorMsg(null, MessageStatus.ROLE_NOT_EXIST);
            return message;
        }
        role.setEnabled(false);
        role.setIsDeleted(true);
        orgDao.update((OrgRole)role.toPO());
        message.addSuccessMsg(role);
        return message;
    }

    @Override
    public OrganizationMessage updateRole(V3xOrgRole role) throws BusinessException {
        OrganizationMessage message = new OrganizationMessage();

        //如果是集团基准角色，则更新所有单位
        if(orgManager.getAccountById(role.getOrgAccountId()).isGroup()){
        	EnumMap<RelationshipObjectiveName, Object> enummap = new EnumMap<RelationshipObjectiveName, Object>(RelationshipObjectiveName.class);
            enummap.put(RelationshipObjectiveName.objective0Id, role.getId());
        	List<V3xOrgRelationship> rellist = orgCache.getV3xOrgRelationship(RelationshipType.Banchmark_Role, (Long)null, null, enummap);
        	List<OrgRole> saveacclist = new ArrayList<OrgRole>();
        	List<OrgRole> list1 = orgDao.getAllRolePO(null, null, null, null, null);
        	Map<Long,Set<OrgRole>> roleMap = new HashMap<Long,Set<OrgRole>>();
        	for(OrgRole r : list1){
        		Set<OrgRole> set = roleMap.get(r.getOrgAccountId());
        		if(null == set){
        			set = new HashSet<OrgRole>();
        		}
        		set.add(r);
        		roleMap.put(r.getOrgAccountId(), set);
        	}
        	Set<OrgRole> savelist = new HashSet<OrgRole>();
            for (V3xOrgRelationship v3xOrgRelationship : rellist) {
        		V3xOrgRole accrole = orgManager.getRoleById(v3xOrgRelationship.getSourceId());
        		if(accrole==null){
        			continue;
        		}
        		if(accrole.getIsBenchmark()&&!role.getEnabled()){
                    throw new BusinessException(ResourceUtil.getString("role.defult.cannot"));
                }
        		accrole.setName(role.getName());
        		accrole.setShowName(role.getShowName());
        		accrole.setBond(role.getBond());
        		accrole.setCategory(role.getCategory());
        		accrole.setCode(role.getCode());
        		accrole.setDescription(role.getDescription());
        		accrole.setEnabled(role.getEnabled());
        		accrole.setIsBenchmark(role.getIsBenchmark());
        		//如果更新默认角色，则其他角色不默认
        		if(role.getIsBenchmark()&&role.getBond()!=0){
        			for (OrgRole orgRole : roleMap.get(accrole.getOrgAccountId())) {
        			    orgRole.setBenchmark(false);
        	    		//orgDao.update(orgRole);
                        savelist.add(orgRole);
        			}
        		}
        		accrole.setIsDeleted(role.getIsDeleted());
        		accrole.setSortId(role.getSortId());
        		accrole.setStatus(role.getStatus());

        		//orgDao.update((OrgRole)accrole.toPO());
                saveacclist.add((OrgRole)accrole.toPO());
			}
            if(!savelist.isEmpty()){
            	orgDao.updates(new ArrayList<OrgRole>(savelist));
            }
            orgDao.updates(saveacclist);
        }
        orgDao.update((OrgRole)role.toPO());
        message.addSuccessMsg(role);
        return message;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<V3xOrgAccount> getAllAccounts(Boolean enable, Boolean isInternal, String condition, Object feildvalue,
            FlipInfo flipInfo) throws BusinessException {
        return (List<V3xOrgAccount>) OrgHelper.listPoTolistBo(orgDao.getAllUnitPO(UnitType.Account, null,
                enable, isInternal, condition, feildvalue, flipInfo));

    }

    @Override
    public void addConurrentPost(MemberPost memberPost) throws BusinessException {
        List<V3xOrgRelationship> rels = new ArrayList<V3xOrgRelationship>(1);
        V3xOrgRelationship rel= memberPost.toRelationship();
        if(null!=memberPost.getCreateTime()){
        	rel.setCreateTime(memberPost.getCreateTime());
        }
        Long currentUserId = AppContext.currentUserId();
        if(currentUserId == null || currentUserId == -1){
        	currentUserId = OrgConstants.GROUP_ADMIN_ID;
        }
        if(memberPost.getCreateUserID() != null){
        	rel.setObjective3Id(memberPost.getCreateUserID());//创建单位ID
        }else{
        	rel.setObjective3Id(currentUserId);//创建单位ID
        }
        rel.setObjective4Id(currentUserId);//最后修改单位ID
        rels.add(rel);
        addOrgRelationships(rels);

        if(null != memberPost.getDepId()
                && -1L != memberPost.getDepId()
                && null != memberPost.getPostId()
                && -1L != memberPost.getPostId()){
            Set<Long> posts = new HashSet<Long>();
            posts.add(memberPost.getPostId());

            Map<Long, Set<Long>> deptPosts = new HashMap<Long, Set<Long>>();
            deptPosts.put(memberPost.getDepId(), posts);

            dealDeptPost(deptPosts);
            //UC需求--增加兼职事件
            AddConCurrentPostEvent event = new AddConCurrentPostEvent(this);
            event.setRel(memberPost.toRelationship());
            EventDispatcher.fireEvent(event);
        }
    }

    @Override
    public void updateConurrentPost(MemberPost memberPost) throws BusinessException {
        //orgDao.updateRelationship(memberPost.toRelationshipPO());
    }

    @Override
    public OrganizationMessage deleteTeams(List<V3xOrgTeam> teams) throws BusinessException {
    	OrganizationMessage message = new OrganizationMessage();
    	for (V3xOrgTeam team : teams) {
    		if(null == team) throw new BusinessException("删除组方法组对象为空！");

            team.setUpdateTime(new Date());
            team.setIsDeleted(true);
            team.setEnabled(false);
            // 2、实例化
            orgDao.update((OrgTeam)team.toPO());

            //删除关系
            orgDao.deleteOrgRelationshipPO(RelationshipType.Team_Member.name(), team.getId(), null, null);
            orgDao.deleteOrgRelationshipPO(RelationshipType.Team_PubScope.name(), team.getId(), null, null);

            // 3、触发事件
            DeleteTeamEvent event = new DeleteTeamEvent(this);
            event.setTeam(team);
            EventDispatcher.fireEvent(event);

            message.addSuccessMsg(team);
		}

        return message;
    }
    @Override
    public OrganizationMessage deleteTeam(V3xOrgTeam team) throws BusinessException {
    	List<V3xOrgTeam> teams = new ArrayList<V3xOrgTeam>(1);
    	teams.add(team);
        return this.deleteTeams(teams);
    }

    /*--------------工具方法----------------*/
    // 检查本集合中是否存在相同的属性值的对象
    private boolean isExistRepeatProperty(List<? extends V3xOrgEntity> ents, String propertyName, Object value,
            V3xOrgEntity entity) throws BusinessException {
        try {
            for (V3xOrgEntity ent : ents) {
                if (!ent.getId().equals(entity.getId())) {
                    Object val = getEntityProperty(ent, propertyName);
                    if (val.equals(value)){
                        return true;
                    }
                }
            }
        }
        catch (Exception e) {
            log.error("实体中不存在" + propertyName + "属性的get方法。", e);
        }

        return false;
    }

    protected Object getEntityProperty(V3xOrgEntity entity, String property) throws Exception {
        return OrgHelper.getProperty(entity, property);
    }

    // 检查副岗
    protected boolean checkSecondPost(V3xOrgMember member) {
        Long deptId = member.getOrgDepartmentId();
        Long postId = member.getOrgPostId();
        List<MemberPost> secondPostList = member.getSecond_post();
        for (MemberPost secondPost : secondPostList) {
            if (secondPost.getDepId().equals(deptId) && secondPost.getPostId().equals(postId)) {
                return false;
            }
        }
        return true;
    }

    // 检查部门下是否存在成员
    protected boolean isExistMemberByDept(V3xOrgDepartment dept) throws BusinessException {
        List<V3xOrgMember> members = orgManager.getMembersByDepartment(dept.getId(), false);
        boolean isAllMemberUnEnabled = true;
        for (V3xOrgMember mem : members) {
            if (mem.isValid()) {
                isAllMemberUnEnabled = false;
                break;
            }
        }
        if (!(ListUtils.EMPTY_LIST.equals(members) || isAllMemberUnEnabled)) {
            return true;
        }
        return false;
    }

    // 检查部门下是否存在组
    protected boolean isExistTeamByDept(V3xOrgDepartment dept) throws BusinessException {
        List<V3xOrgTeam> teams = orgManager.getDepartmentTeam(dept.getId());
        List<V3xOrgDepartment> childDeps = orgManager.getChildDepartments(dept.getId(), false);
        for (V3xOrgDepartment child : childDeps) {
            teams.addAll(orgManager.getDepartmentTeam(child.getId()));
        }
        boolean isAllTeamUnEnabled = true;
        for (V3xOrgTeam team : teams) {
            if (team.isValid() && team.getType() != OrgConstants.TEAM_TYPE.PERSONAL.ordinal()) {
                isAllTeamUnEnabled = false;
                break;
            }
        }
        if (!(ListUtils.EMPTY_LIST.equals(teams) || isAllTeamUnEnabled)) {
            return true;
        }
        return false;
    }

    @Override
    public void deleteAll(){
        //做单元测试临时使用
        DBAgent.bulkUpdate("delete from OrgUnit where id <> -1730833917365171641 ");
        DBAgent.bulkUpdate("delete from OrgMember where id not in ( -7273032013234748168,-4401606663639775639,5725175934914479521 ) ");
        DBAgent.bulkUpdate("delete from OrgPost");
        DBAgent.bulkUpdate("delete from OrgPrincipal where id not in( 25,26,27,600 )");
        DBAgent.bulkUpdate("delete from OrgLevel");
        DBAgent.bulkUpdate("delete from OrgRole where id not in ( -3739927258183285747,-3280225142003054984,-1231692063532299175 )");
        DBAgent.bulkUpdate("delete from OrgRelationship where id not in ( -5329422661916671085,-4690686253734279447,-1304538535729217451)");
        DBAgent.bulkUpdate("delete from OrgTeam");
    }

    /**
     * 统一更新删除实体时三个状态同时修改
     * <code>isDeleted</code>
     * <code>enabled</code>
     * <code>updateTime</code>
     * @param entity
     */
    private void updateEntity2Deleted(V3xOrgEntity entity) {
        entity.setIsDeleted(true);
        entity.setEnabled(false);
        entity.setUpdateTime(new Date());
    }

    private List<V3xOrgRole> getSystemRoleDefinitions(int type){
        Map<String, OrgRoleDefaultDefinition> beans = AppContext.getBeansOfType(OrgRoleDefaultDefinition.class);

        Map<String, V3xOrgRole> map = new HashMap<String, V3xOrgRole>();
        for (OrgRoleDefaultDefinition def : beans.values()) {
            if(map.containsKey(def.getId())){
                log.warn("角色"+def.getId()+"已存在，不允许重复定义，被忽略。");
                continue;
            }

            if(def.getType() == type){
                map.put(def.getId(), def.toRole());
            }
        }

        return new ArrayList<V3xOrgRole>(map.values());
    }

    @Override
    public void deleteRelationById(Long id) throws BusinessException {
        orgDao.deleteOrgRelationshipPOById(id);
    }

    @Override
    public void copyGroupLevelToAccount(Long accountId) throws BusinessException {
        V3xOrgAccount rootAccount = orgManager.getRootAccount();
        if (null == rootAccount)
            throw new BusinessException("查询集团为空！");
        List<V3xOrgLevel> rootLevels = orgManager.getAllLevels(rootAccount.getId());
        List<V3xOrgLevel> levelList = new ArrayList<V3xOrgLevel>(rootLevels.size());
        for (V3xOrgLevel level : rootLevels) {
            V3xOrgLevel accountLevel = new V3xOrgLevel();
            accountLevel.setIdIfNew();
            accountLevel.setName(level.getName());
            accountLevel.setCode(level.getCode());
            accountLevel.setDescription(level.getDescription());
            accountLevel.setLevelId(level.getLevelId());
            accountLevel.setOrgAccountId(accountId);
            accountLevel.setSortId(level.getSortId());
            accountLevel.setGroupLevelId(level.getId());
            levelList.add(accountLevel);
        }
        addLevels(levelList);
    }


    @Override
    public List<V3xOrgAccount> getNeighborAccountsByAccountId(Long accountId) throws BusinessException {
        V3xOrgAccount account = orgManager.getAccountById(accountId);
        if(null==account || !account.isValid()) throw new BusinessException("查询的单位实体异常！");
        return orgCache.getSameLengthPathUnits(account.getPath());
    }

    @Override
    public List<V3xOrgAccount> getSuperiorAccountsByAccountId(Long accountId) throws BusinessException {
        V3xOrgAccount account = orgManager.getAccountById(accountId);
        if(null==account || !account.isValid()) throw new BusinessException("查询的单位实体异常！");
        return orgCache.getShorterLengthPathUnits(account.getPath());
    }

    @Override
    public void deleteRoleRelsInUnit(Long roleId, Long unitId) throws BusinessException {
        EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(
                RelationshipObjectiveName.class);
        objectiveIds.put(RelationshipObjectiveName.objective1Id, roleId);
        orgDao.deleteOrgRelationshipPO(RelationshipType.Member_Role.name(), null, unitId, objectiveIds);
    }

    @Override
    public void updateV3xOrgRelationship(V3xOrgRelationship v3xOrgRelationship) throws BusinessException {
        orgDao.updateRelationship((OrgRelationship) v3xOrgRelationship.toPO());
    }

    @Override
    public List<V3xOrgEntity> getEntityNoRelationDirect(String entityClassName, String property, Object value,
            Boolean enabled, Long accountId) {
        List<V3xOrgEntity> enList = new ArrayList<V3xOrgEntity>();

        if (entityClassName.equals(V3xOrgLevel.class.getSimpleName())) {
            enList.addAll(OrgHelper.listPoTolistBo(orgDao.getAllLevelPO(accountId, enabled, property, value, null)));
        } else if (entityClassName.equals(V3xOrgMember.class.getSimpleName())) {
            enList.addAll(OrgHelper.listPoTolistBo(orgDao.getAllMemberPOByAccountId(accountId, null, null, enabled,
                    property, value, null)));
        } else if (entityClassName.equals(V3xOrgPost.class.getSimpleName())) {
            enList.addAll(OrgHelper.listPoTolistBo(orgDao.getAllPostPO(accountId, enabled, property, value, null)));
        } else if (entityClassName.equals(V3xOrgRole.class.getSimpleName())) {
            enList.addAll(OrgHelper.listPoTolistBo(orgDao.getAllRolePO(accountId, enabled, property, value, null)));
        } else if (entityClassName.equals(V3xOrgTeam.class.getSimpleName())) {
            enList.addAll(OrgHelper.listPoTolistBo(orgDao.getAllTeamPO(accountId, null, enabled, property, value, null)));
        } else if (entityClassName.equals(V3xOrgAccount.class.getSimpleName())) {
            enList.addAll(OrgHelper.listPoTolistBo(orgDao.getAllUnitPO(UnitType.Account, null, enabled, null,
                    property, value, null)));
        } else if (entityClassName.equals(V3xOrgDepartment.class.getSimpleName())) {
            enList.addAll(OrgHelper.listPoTolistBo(orgDao.getAllUnitPO(UnitType.Department, accountId, enabled, null,
                    property, value, null)));
        }

        return enList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<V3xOrgMember> getAllMemberPOByAccountId(Long accountId, Boolean isInternal, Boolean enable,
            Map<String, Object> param, FlipInfo flipInfo){
        List<OrgMember> orgMembers = orgDao.getAllMemberPOByAccountId(accountId, isInternal, enable, param, flipInfo);
        return (List<V3xOrgMember>) OrgHelper.listPoTolistBo(orgMembers);
    }

    @Override
    public String getAccountName() throws BusinessException {
        if("true".equals(SystemProperties.getInstance().getProperty("org.isGroupVer"))) {
            return orgManager.getUnitById(OrgConstants.GROUPID).getName();
        } else {
            return orgManager.getUnitById(OrgConstants.ACCOUNTID).getName();
        }
    }

    @Override
    public boolean matchAccountName(String accountName) throws BusinessException {
        if(Strings.isBlank(accountName)) {
            return false;
        } else {
            if("true".equals(SystemProperties.getInstance().getProperty("org.isGroupVer"))) {
                return orgManager.getUnitById(OrgConstants.GROUPID).getName().equals(accountName);
            } else {
                return orgManager.getUnitById(OrgConstants.ACCOUNTID).getName().equals(accountName);
            }
        }
    }

    @Override
    public void updateAccountName(String name) throws BusinessException {
        if(Strings.isBlank(name)) {
            log.error("加密使用接口更新单位名称失败，原因：单位名称不允许为空！");
            throw new BusinessException("加密使用接口更新单位名称失败，原因：单位名称不允许为空！");
        } else if(isPropertyDuplicated(V3xOrgAccount.class.getSimpleName(), "name", name)) {
            log.error("加密使用接口更新单位名称失败，原因：单位名称重复！");
            throw new BusinessException("加密使用接口更新单位名称失败，原因：单位名称重复！");
        } else {
            V3xOrgUnit unit = null;
            if("true".equals(SystemProperties.getInstance().getProperty("org.isGroupVer"))) {
                unit = orgManager.getUnitById(OrgConstants.GROUPID);
            } else {
                unit = orgManager.getUnitById(OrgConstants.ACCOUNTID);
            }
            unit.setName(name);
            orgDao.update((OrgUnit)OrgHelper.boTopo(unit));
        }
    }

    @Override
    public List<V3xOrgDepartment> getChildDepartmentsWithInvalid(Long parentDepId, boolean firtLayer)
            throws BusinessException {
        List<V3xOrgDepartment> resultList = new UniqueList<V3xOrgDepartment>();
        V3xOrgUnit parentDep = orgManager.getEntityById(V3xOrgUnit.class, parentDepId);
        if (parentDep == null) {
            return resultList;
        }

        List<V3xOrgDepartment> depsList = this.getAllDepartments(parentDep.getOrgAccountId(), null, true, null, null,
                null);
        for (V3xOrgDepartment d : depsList) {
            if ((firtLayer && d.getParentPath().equals(parentDep.getPath())) //不包含子部门
                    || (!firtLayer && d.getParentPath().startsWith(parentDep.getPath()))) {
                resultList.add(d);
            }
        }
        return resultList;
    }

    private List<V3xOrgUnit> getChildUnitIncludeDis(Long accountId) throws BusinessException {
        V3xOrgAccount currentAccount = orgCache.getV3xOrgEntity(V3xOrgAccount.class, accountId);
        //增加空防护，获取某个停用单位的子单位时，从缓存中获取不到
        if(null == currentAccount) {
            currentAccount = (V3xOrgAccount) OrgHelper.poTobo(orgDao.getEntity(OrgUnit.class, accountId));
        }
        List<V3xOrgUnit> result = new UniqueList<V3xOrgUnit>();
        List<V3xOrgUnit> childUnits = orgCache.getChildUnitsByPid(V3xOrgUnit.class, accountId);
        result.addAll(childUnits);
        List<OrgUnit> allAccounts = orgDao.getAllUnitPO(null, accountId, null, null, null, null, null);
        for (OrgUnit o : allAccounts) {
            if(o.getPath().startsWith(currentAccount.getPath())&&!o.getPath().equals(currentAccount.getPath()) && !childUnits.contains(o)){
                result.add(OrgHelper.cloneEntity((V3xOrgUnit)OrgHelper.poTobo(o)));
            }
        }
        return result;
    }

    /**
     * 此方法只是用在判断密码是否修改，没有安全防护，不做对外接口使用
     * @param loginName
     * @param password
     * @return
     */
    private Boolean isOldPasswordCorrect(String loginName, String password) {
        V3xOrgMember member = null;
        try {
            member = orgManager.getMemberByLoginName(loginName);
        } catch (BusinessException e) {
            return Boolean.FALSE;
        }
        if(null == member) return Boolean.FALSE;
        //判断如果开启AD则输入AD密码校验 ,並且這個人綁定了，進行ad密碼驗證。否則進行oa密碼驗證
        if(LdapUtils.isLdapEnabled() && !member.getIsAdmin() && LdapUtils.isBind(member.getId())) {//OA-55561
            UserMapperDao userMapperDao = (UserMapperDao) AppContext.getBean("userMapperDao");
            Authenticator a = LDAPTool.createAuthenticator(userMapperDao);
            String ldapAdloginName = "";
            List<CtpOrgUserMapper> userMappers = userMapperDao.getExLoginNames(loginName, LDAPConfig.getInstance().getType());
            for (CtpOrgUserMapper map : userMappers) {
            	ldapAdloginName = map.getExLoginName();
            }
            CtpOrgUserMapper ep = a.auth(ldapAdloginName, password);
            if (ep != null) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } else {
            return principalManager.authenticate(loginName, password);
        }
    }

    @Override
    public void updateAddressBookinfo(V3xOrgMember m) throws BusinessException{
    	//更新自定义的通讯录字段
    	if(m == null || !m.isValid()) return;
        int cLen=0;
        List<String> customerAddressBooklist = m.getCustomerAddressBooklist();
        MetadataColumnBO metadataColumn;
        if(null!=customerAddressBooklist && customerAddressBooklist.size()>0){
        	cLen=customerAddressBooklist.size();
        }
        //自定义的通讯录字段
        AddressBook addressBook = addressBookCustomerFieldInfoManager.getByMemberId(m.getId());
        boolean isExist=true;
        if(null==addressBook){//如果沒有，新增
        	addressBook=new AddressBook();
        	addressBook.setId(UUIDLong.longUUID());
        	addressBook.setMemberId(m.getId());
        	addressBook.setCreateDate(new Date());
        	addressBook.setUpdateDate(new Date());
    		isExist=false;
        }else{//更新
        	addressBook.setUpdateDate(new Date());
        }
        List<MetadataColumnBO> metadataColumnList=addressBookManager.getCustomerAddressBookList();

        for(int i=0;i<metadataColumnList.size();i++){
        	metadataColumn=metadataColumnList.get(i);
        	String columnName=metadataColumn.getColumnName();
        	Method method=addressBookManager.getSetMethod(columnName);
			if(null==method){
				 throw new BusinessException("自定义通讯录字段: "+metadataColumn.getLabel()+"不存在！");
			}
 			String value="";
 			if(i<cLen){
 				value=customerAddressBooklist.get(i);
 			}
        	try {
        		 if(metadataColumn.getType()==0){
				 String saveValue=(null==value || Strings.isBlank(value))?"":String.valueOf(value);
					method.invoke(addressBook, new Object[] { saveValue });
				 }
				 if(metadataColumn.getType()==1){
					 Double saveValue=(null==value || Strings.isBlank(value))?null:Double.valueOf(String.valueOf(value));
					 method.invoke(addressBook, new Object[] { saveValue });
				 }
				 if(metadataColumn.getType()==2){
					 String saveValue=(null==value || Strings.isBlank(value))?"":String.valueOf(value);
					 method.invoke(addressBook, new Object[] {"".equals(saveValue)?null: Datetimes.parse(saveValue, "yyyy-MM-dd") });
				 }
				 } catch (Exception e) {
				 }
        }

        if(isExist){
        	addressBookCustomerFieldInfoManager.updateAddressBook(addressBook);
        }else{
        	addressBookCustomerFieldInfoManager.addAddressBook(addressBook);
        }

    }

    //删除单位，部门，岗位，职级后，同时删除角色有这些实体的关系数据
    private void deleteMemberRoleRels2Entity(ORGENT_TYPE type, Long entityId) throws BusinessException {
        /** 先将这些实体与这些角色关系先删除 */
        EnumMap<RelationshipObjectiveName, Object> objectiveIds = new EnumMap<RelationshipObjectiveName, Object>(RelationshipObjectiveName.class);
        objectiveIds.put(RelationshipObjectiveName.objective5Id, type.name());

        List<V3xOrgRelationship> relationships= orgCache.getV3xOrgRelationship(RelationshipType.Member_Role, entityId, (Long)null, objectiveIds);
        if(Strings.isNotEmpty(relationships)){
            List<OrgRelationship> rels = new ArrayList<OrgRelationship>();
            for(V3xOrgRelationship rel : relationships){
                rels.add((OrgRelationship) rel.toPO());
            }
            if(rels.size()>0){
                orgDao.deleteOrgRelationshipPOs(rels);
            }
        }
        
    }

    public OrgMember getFirstCreateMember() {
        return orgDao.getFirstCreateMember();
    }

}
