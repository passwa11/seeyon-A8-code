
package com.seeyon.apps.ocip.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.security.MessageEncoder;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.OrgConstants.MemberPostType;
import com.seeyon.ctp.organization.bo.MemberPost;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgPost;
import com.seeyon.ctp.organization.bo.V3xOrgPrincipal;
import com.seeyon.ctp.organization.bo.V3xOrgRelationship;
import com.seeyon.ctp.organization.bo.V3xOrgUnit;
import com.seeyon.ctp.organization.dao.OrgCache;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.principal.NoSuchPrincipalException;
import com.seeyon.ctp.organization.principal.PrincipalManager;
import com.seeyon.ocip.common.IConstant;
import com.seeyon.ocip.org.entity.OcipDepartment;
import com.seeyon.ocip.org.entity.OcipUnit;
import com.seeyon.ocip.org.entity.OcipUser;
import com.seeyon.ocip.org.entity.OcipUser.Relation;

/**
 * 数据服务平台实体转换工具类
 * 
 * @author wxt.touxin
 * @version 2017-6-14
 */
public class TransferUtil {
	private static final Log logger = LogFactory.getLog(TransferUtil.class);

	private OrgManager orgManager;

	private OrgCache orgCache;

	private PrincipalManager principalManager;

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public void setOrgCache(OrgCache orgCache) {
		this.orgCache = orgCache;
	}

	public void setPrincipalManager(PrincipalManager principalManager) {
		this.principalManager = principalManager;
	}

	/**
	 * 将V3xOrgDepartment转化为DataDepartment
	 * 
	 * @param department
	 *            {@link V3xOrgDepartment}
	 * @return {@link DataDepartment}
	 * @authur wxt.touxin
	 * @version 2017年6月14日
	 */
	public OcipDepartment transV3xOrgDepartment2OcipDepartment(V3xOrgDepartment department) throws Exception {
		OcipDepartment dataDepartment = new OcipDepartment();
		dataDepartment.setObjectId(String.valueOf(department.getId()));
		dataDepartment.setName(department.getName());
		dataDepartment.setCode(String.valueOf(department.getId()));//G6 与慧智数据自动映射
		V3xOrgAccount orgAccount = orgManager.getAccountById(department.getOrgAccountId());
		if (orgAccount == null) {
			throw new Exception("上报部门【" + department.getName() + "】没有获取到直属单位");
		}
		// 单位Id
		dataDepartment.setUnitId(String.valueOf(orgAccount.getId()));
		// 单位名称
		dataDepartment.setUnitName(orgAccount.getName());
		if (department.getSortId() != null) {
			dataDepartment.setSortId(department.getSortId().intValue());
		}
		dataDepartment.setIsEnable(department.getEnabled() ? IConstant.ENABLE : IConstant.DISABLE);
		// 直属上级
		V3xOrgUnit parent = orgManager.getParentUnit(department);
		/*
		 * if(!department.getEnabled()){ parent =
		 * orgManager.getUnitById(department.getSuperior()); }
		 */
		if (parent == null) {
			throw new Exception("上报部门【" + department.getName() + "】没有获取到父级单位或部门,可能是父级单位或部门已经删除，将不会上报此部门");
		} else {
			dataDepartment.setParentId(String.valueOf(parent.getId()));
		}
		// 部门岗位
		List<V3xOrgRelationship> orgRelationships = orgCache
				.getV3xOrgRelationship(OrgConstants.RelationshipType.Department_Post);
		List<OcipDepartment.DeptPost> deptPosts = new ArrayList<OcipDepartment.DeptPost>();
		for (V3xOrgRelationship orgRelationship : orgRelationships) {
			if (orgRelationship.getSourceId().longValue() == department.getId().longValue()) {
				V3xOrgPost orgPost = orgManager.getPostById(orgRelationship.getObjective0Id());
				if (orgPost == null) {
					continue;
				}
				OcipDepartment.DeptPost deptPost = new OcipDepartment.DeptPost();
				deptPost.setPostId(String.valueOf(orgPost.getId()));
				deptPost.setPostName(orgPost.getName());
				deptPosts.add(deptPost);
			}
		}
		dataDepartment.setDeptPosts(deptPosts);
		return dataDepartment;
	}
	
	/**
	 * 将V3xOrgDepartment转化为DataDepartment ,以传入的部门岗位为准
	 * @param department 部门
	 * @param deptPosts 部门岗位
	 * @return
	 * @throws Exception
	 */
	public OcipDepartment transV3xOrgDepartment2OcipDepartment(V3xOrgDepartment department,List<V3xOrgPost> deptPosts) throws Exception {
		OcipDepartment dataDepartment = new OcipDepartment();
		dataDepartment.setObjectId(String.valueOf(department.getId()));
		dataDepartment.setName(department.getName());
		dataDepartment.setCode(String.valueOf(department.getId()));//G6 与慧智数据自动映射
		V3xOrgAccount orgAccount = orgManager.getAccountById(department.getOrgAccountId());
		if (orgAccount == null) {
			throw new Exception("上报部门【" + department.getName() + "】没有获取到直属单位");
		}
		// 单位Id
		dataDepartment.setUnitId(String.valueOf(orgAccount.getId()));
		// 单位名称
		dataDepartment.setUnitName(orgAccount.getName());
		if (department.getSortId() != null) {
			dataDepartment.setSortId(department.getSortId().intValue());
		}
		dataDepartment.setIsEnable(department.getEnabled() ? IConstant.ENABLE : IConstant.DISABLE);
		// 直属上级
		V3xOrgUnit parent = orgManager.getParentUnit(department);
		/*
		 * if(!department.getEnabled()){ parent =
		 * orgManager.getUnitById(department.getSuperior()); }
		 */
		if (parent == null) {
			throw new Exception("上报部门【" + department.getName() + "】没有获取到父级");
		} else {
			dataDepartment.setParentId(String.valueOf(parent.getId()));
		}
		// 部门岗位
		List<OcipDepartment.DeptPost> dataDeptPosts = new ArrayList<OcipDepartment.DeptPost>();
		if(deptPosts!=null && deptPosts.size()!=0){
			for (V3xOrgPost orgPost : deptPosts) {
				if (orgPost == null) {
					continue;
				}
				OcipDepartment.DeptPost deptPost = new OcipDepartment.DeptPost();
				deptPost.setPostId(String.valueOf(orgPost.getId()));
				deptPost.setPostName(orgPost.getName());
				dataDeptPosts.add(deptPost);
			}
		}
		dataDepartment.setDeptPosts(dataDeptPosts);
		return dataDepartment;
	}
		

	/**
	 * 将V3xOrgAccount转化为DataUnit
	 * 
	 * @param orgAccount
	 *            {@link V3xOrgAccount}
	 * @param andAdmin
	 *            是否包含管理员信息。OA在新增或者更新单位时，监听事件拦截不到单位管理员的信息。
	 * @return {@link DataUnit}
	 * @throws Exception
	 * @authur wxt.touxin
	 * @version 2017年6月14日
	 */
	public OcipUnit transV3xOrgAccount2OcipUnit(V3xOrgAccount orgAccount, boolean andAdmin) throws Exception {
		OcipUnit dataUnit = new OcipUnit();
		dataUnit.setObjectId(String.valueOf(orgAccount.getId()));
		dataUnit.setName(orgAccount.getName());
		dataUnit.setForeignName(orgAccount.getSecondName());
		dataUnit.setShortName(orgAccount.getShortName());
		dataUnit.setObjectId(String.valueOf(orgAccount.getId()));
		dataUnit.setAliasName(orgAccount.getSecondName());
		if (orgAccount.getSortId() != null) {
			dataUnit.setSortId(orgAccount.getSortId().intValue());
		}
		// modify by lmc 2017年8月19日 星期六   G6 不上报code， 也不处理下发
		//dataUnit.setCode(null);
		if (OrgConstants.GROUPID.equals(orgAccount.getId()) || OrgConstants.ACCOUNTID.equals(orgAccount.getId())) {
			dataUnit.setCode(orgAccount.getCode());
		} else {
			dataUnit.setCode(String.valueOf(orgAccount.getId()));// G6 与慧智数据自动映射
		}
		dataUnit.setIsEnable(orgAccount.getEnabled() ? IConstant.ENABLE : IConstant.DISABLE);
		if(OrgUtil.isGovermentSystem()){
			dataUnit.setParentId("0");
		}else{
			if (orgAccount.getSuperior() == -1l) {// 根单位(多组织版组织管理员)
				dataUnit.setParentId("0");
			} else {
				V3xOrgUnit parentUnit = orgManager.getParentUnit(orgAccount);
				/*
				 * if(!orgAccount.getEnabled()){ parentUnit =
				 * orgManager.getUnitById(orgAccount.getSuperior()); }
				 */
				if (parentUnit != null) {
					dataUnit.setParentId(String.valueOf(parentUnit.getId()));
				} else {
					throw new Exception("上报单位【" + orgAccount.getName() + "】没有获取到父级");
				}
			}
		}
		if (andAdmin) {
			V3xOrgMember admin = null;
			if (orgAccount.getIsGroup()) {// 根单位(多组织版组织管理员)
				// duanwei[2017-06-19]组织管理员不上报 。注释以下代码
				// admin = orgManager.getGroupAdmin();
			} else {
				// List<V3xOrgMember> allMembers =
				// orgManagerDirect.getAllMembers(null,true);

				admin = orgManager.getAdministrator(orgAccount.getId());
				if (admin == null) {
					//throw new Exception("上报单位【" + orgAccount.getName() + "】单位管理员不存在");
				}else{
					// 单位管理员id，名称，登录名，密码
					dataUnit.setAdminId(String.valueOf(admin.getId()));
					dataUnit.setAdminName(admin.getName());
					dataUnit.setAdminLoginName(admin.getLoginName());
					dataUnit.setAdminPassword(getMemberPassword(admin.getId()));
				}
			}
			/*
			 * //duanwei[2017-06-19]组织管理员不上报 。将以下代码移动至else里面以下代码 if (admin ==
			 * null) { throw new Exception("上报单位【" + orgAccount.getName() +
			 * "】单位管理员不存在"); } // 单位管理员id，名称，登录名，密码
			 * dataUnit.setAdminId(String.valueOf(admin.getId()));
			 * dataUnit.setAdminName(admin.getName());
			 * dataUnit.setAdminLoginName(admin.getLoginName());
			 * dataUnit.setAdminPassword(getMemberPassword(admin.getId()));
			 */
		}
		return dataUnit;
	}

	/**
	 * 将V3xOrgAccount转化为DataUnit
	 * 
	 * @param member
	 *            {@link V3xOrgMember}
	 * @param useDBRelation 人员关系构建来源：true-使用数据库已有的关系（主要用于全量上报）；false-使用页面修改的关系（主要用于从页面修改人员）
	 * @return {@link DataUser}
	 * @throws Exception
	 * @authur wxt.touxin
	 * @version 2017年6月14日
	 */
	public OcipUser transV3xOrgMember2OcipUser(V3xOrgMember member) throws Exception {
		OcipUser dataUser = new OcipUser();
		dataUser.setObjectId(String.valueOf(member.getId()));
		dataUser.setName(member.getName());
		dataUser.setCode(String.valueOf(member.getId()));//G6 与慧智数据自动映射
		// 人员关系
		List<OcipUser.Relation> relations = new ArrayList<OcipUser.Relation>();
		if (!member.getIsAdmin()) {
			List<V3xOrgRelationship> relationships = orgManager.getMemberPostRelastionships(member.getId(),
					member.getOrgAccountId(), null);
			List<MemberPost> concurrentPostList= member.getConcurrent_post();
			for(MemberPost mp: concurrentPostList)
			{
				mp.toRelationship();
				relationships.add(mp.toRelationship());
			}
			for (V3xOrgRelationship relationship : relationships) {
				OcipUser.Relation relation = new OcipUser.Relation();
				if(relationship.getOrgAccountId()!=null && relationship.getOrgAccountId().longValue()!=-1l){
					relation.setUnitId(String.valueOf(relationship.getOrgAccountId()));
				}
				else{//数据平台下来的数据，如果没有职级就设置为-1，因此这里如果等于-1，代表没选择单位，
					relation.setUnitId(null);
				}
				if(relationship.getObjective0Id().longValue()==-1l){//数据平台下来的数据，如果没有职级就设置为-1，因此这里如果等于-1，代表没选择部门
					relation.setDepartmentId(null);
				}
				else{
					relation.setDepartmentId(String.valueOf(relationship.getObjective0Id()));
				}
				if(relationship.getObjective1Id().longValue()==-1l){//数据平台下来的数据，如果没有职级就设置为-1，因此这里如果等于-1，代表没选择岗位
					relation.setPostId(null);
				}
				else{
					relation.setPostId(String.valueOf(relationship.getObjective1Id()));
				}
				if (relationship.getObjective5Id().equals(OrgConstants.MemberPostType.Main.name())) {
					relation.setType(Relation.RELATION_TYPE_POST_MAIN);
				} else {
					relation.setType(Relation.RELATION_TYPE_POST_SECOND);
				}
				relations.add(relation);
			}
		} else {
			OcipUser.Relation relation = new OcipUser.Relation();
			relation.setUnitId(String.valueOf(member.getOrgAccountId()));
			relation.setType(Relation.RELATION_TYPE_UNIT_ADMIN);
			relations.add(relation);
		}
		dataUser.setRelations(relations);
		if(member.getOrgLevelId().longValue()==-1l){//数据平台下来的数据，如果没有职级就设置为-1，因此这里如果等于-1，代表没选择职级
			dataUser.setLevelId(null);
		}
		else{
			dataUser.setLevelId(String.valueOf(member.getOrgLevelId()));
		}
		dataUser.setIsAdmin(member.getIsAdmin() ? OcipUser.IS_ADMIN_TRUE : OcipUser.IS_ADMIN_FALSE);
		Integer gender = member.getGender();// sex 1男2女-1未知
		dataUser.setSex(gender);
		dataUser.setBirthday(member.getBirthday());
		dataUser.setBirthplace(member.getAddress());// 籍贯
		dataUser.setAddress(member.getAddress());// 住址
		dataUser.setIdCard(member.getIdNum());// 身份证号码
		//dataUser.setSecretLevel(member.getSecretLevel());
		dataUser.setRemarks(member.getDescription());
		/**
		 * OA:0未知，1初中，2高中，3大学专科，4大学本科，5硕士，6博士，7其他，8职高，9中专，10技校
		 * 平台:1小学，2初中，3高中，4大专，5本科，6研究生，7博士，8其他
		 */
		if (member.getProperty("eduBack") != null) {
			Integer eduLevel = Integer.valueOf(member.getProperty("eduBack").toString());
			if (eduLevel == -1) {
				dataUser.setEduLevel(0);
			} else if (eduLevel == 1 || eduLevel == 2||eduLevel == 3||eduLevel == 4 ||eduLevel == 5|| eduLevel == 6) {
				dataUser.setEduLevel(eduLevel + 1);
			} else {
				dataUser.setEduLevel(8);
			}
		}
		else
		{
			dataUser.setEduLevel(8);
		}
	
		/**
		 * OA： 1党员，2群众，3共青团员，4民主党派成员 平台：1:中共党员，2:中共预备党员，3:共青团员，4:群众，5:民主党派成员
		 */
		if (member.getProperty("politics") != null) {
			Integer politics = Integer.valueOf(member.getProperty("politics").toString());
			if (politics == -1 ||politics==0) {//政治面貌未填写
				dataUser.setPoliticalPosition(0);
			} else if (politics == 1) {
				dataUser.setPoliticalPosition(1);
			} else if (politics == 2) {
				dataUser.setPoliticalPosition(4);
			} else if (politics == 3) {
				dataUser.setPoliticalPosition(3);
			} else if (politics == 4) {
				dataUser.setPoliticalPosition(5);
			}
			else
			{
				dataUser.setPoliticalPosition(0);
			}
		}
		else
		{
			dataUser.setPoliticalPosition(0);
		}
		dataUser.setWechat(member.getWeixin());
		dataUser.setEmail(member.getEmailAddress());
		/*duanwei[2017-06-24]注释，暂时屏蔽人员头像。有头像时上报出错
		 * Object photo = member.getProperty("imageid");
		if (photo != null) {
			dataUser.setPhoto(photo.toString());
		}*/
		dataUser.setTelNumber(member.getTelNumber());
		dataUser.setFixedNumber(member.getOfficeNum());
		String loginName = member.getLoginName();
		//duanwei[2017-06-26]---start---登陆名中的特殊字符处理,把登录名中的特殊字符全部去掉，
		//上报到平台的登录名为去掉特殊字符串后的登录名，如 ：a&b* c,上报的登录名为：abc  
		/*duanwei[2017-06-27]不能直接修改登录名，登录名和密码是一起加密的
		 * TODO bug oa登录名有特殊字符  未处理
		 * String   regEx  =  "[^a-zA-Z0-9]";
		Pattern   p   =   Pattern.compile(regEx);     
        Matcher   m   =   p.matcher(loginName);
        String newLoginName = m.replaceAll("").trim();*/
		//duanwei[2017-06-26]---end---
		dataUser.setLoginName(loginName);
		try{
			String pwd = getMemberPassword(member.getId());
			dataUser.setPassword(pwd);
		}catch(NoSuchPrincipalException e){
			MessageEncoder encode = new MessageEncoder();
			dataUser.setPassword(encode.encode(loginName, OrgConstants.DEFAULT_PASSWORD));
		
		}
		dataUser.setSortId(member.getSortId().intValue());
		dataUser.setIsEnable(member.getIsValid() ? IConstant.ENABLE : IConstant.DISABLE);
		return dataUser;
	}
	
	/**
	 * 将OcipUser转化为V3xOrgMember
	 * 
	 * @param ocipUser
	 *            {@link OcipUser}
	 * @param member
	 *            {@link V3xOrgMember} 新增人员时，为空；更新人员时传入原人员
	 * @return {@link V3xOrgMember}
	 * @throws Exception
	 * @authur wxt.touxin
	 * @version 2017年6月16日
	 */
	public V3xOrgMember transOcipUser2V3xOrgMember(OcipUser ocipUser, V3xOrgMember member,List<V3xOrgRelationship> listRel) throws Exception {
		if (member == null) {
			member = new V3xOrgMember();
			member.setIdIfNew();
			member.setIsValid(true);
			member.setIsDeleted(false);
			member.setIsInternal(true);
			member.setIsVirtual(false);
			member.setIsAssigned(false);
			member.setIsLoginable(true);
			member.setState(OrgConstants.MEMBER_TYPE.FORMAL.ordinal());
		}
		member.setName(ocipUser.getName());
		member.setCode(ocipUser.getCode());
		//获取组织id，方便下面设置默认值
		V3xOrgMember groupAdmin = orgManager.getGroupAdmin();
		V3xOrgAccount adminAccount = orgManager.getAccountByLoginName(groupAdmin.getLoginName());
		Long adminAccountId = adminAccount.getId();
		// relations
		List<Relation> relations = ocipUser.getRelations();
		List<MemberPost> second_post = new ArrayList<MemberPost>();
		if (relations == null || relations.size() < 1) {
			logger.warn("人员【"+ocipUser.getName()+"】组织关系为空，设置为未分配人员");
			//duanwei [2017-06-30]--start--为没有分配单位信息的默认为组织下的未分配人员
			member.setOrgAccountId(adminAccountId);
			member.setIsAssigned(false);
			//duanwei [2017-06-30]--end--
		} else {

			logger.warn("开始检查人员【"+ocipUser.getName()+"】的组织机构关系");
			
			
			for (Relation relation : relations) {
				if (relation.getType().equals(OcipUser.Relation.RELATION_TYPE_UNIT_ADMIN)) {// 单位管理员
					member.setOrgAccountId(Long.parseLong(relation.getUnitId()));
					member.setIsAssigned(true);
				} else {
					
					if (relation.getType().equals(OcipUser.Relation.RELATION_TYPE_POST_MAIN)) {// 主岗
						
						try {
							validateUnit(relation.getUnitId());
							member.setOrgAccountId(Long.parseLong(relation.getUnitId()));
							if(adminAccountId.toString().equals(relation.getUnitId())){
								member.setIsAssigned(false);
							}else{
								member.setIsAssigned(true);
							}
						} catch (Exception e) {//没有分配单位，默认为未分配人员
							logger.error(e.getMessage(), e);
							member.setOrgAccountId(adminAccount.getId());
							member.setIsAssigned(false);
						}
						
						try {
							validateLevel(ocipUser.getLevelId());
							member.setOrgLevelId(Long.parseLong(ocipUser.getLevelId()));
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
							member.setOrgLevelId(-1l);
						}
						try {
							validateUnit(relation.getDepartmentId());
							member.setOrgDepartmentId(Long.parseLong(relation.getDepartmentId()));
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
							member.setOrgDepartmentId(-1l);
						}
						
						try {
						validatePost(relation.getPostId());
						member.setOrgPostId(Long.parseLong(relation.getPostId()));
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
							member.setOrgPostId(-1L);
						}
					
					} else if (relation.getType().equals(OcipUser.Relation.RELATION_TYPE_POST_SECOND)) {// 副岗
						
						if(StringUtils.isNotBlank(relation.getPostId()) && orgManager.getPostById(Long.parseLong(relation.getPostId())) != null)
						{
							
							
							if(member.getOrgAccountId().longValue() == Long.parseLong(relation.getUnitId()))
							{
								MemberPost memberPost = new MemberPost();
								memberPost.setOrgAccountId(Long.parseLong(relation.getUnitId()));
								memberPost.setDepId(Long.parseLong(relation.getDepartmentId()));
								memberPost.setPostId(Long.parseLong(relation.getPostId()));
								memberPost.setType(MemberPostType.Second);
								memberPost.setMemberId(member.getId());
								second_post.add(memberPost);
							}
							else
							{
								V3xOrgRelationship concurrentRel = new V3xOrgRelationship();
								concurrentRel.setSourceId(member.getId());
								concurrentRel.setOrgAccountId(Long.parseLong(relation.getUnitId()));
								concurrentRel.setObjective0Id(Long.parseLong(relation.getDepartmentId()));
								concurrentRel.setObjective1Id(Long.parseLong(relation.getPostId()));
								Date date = new Date();
								concurrentRel.setCreateTime(date);
								concurrentRel.setUpdateTime(date);
								concurrentRel.setKey(OrgConstants.RelationshipType.Member_Post.name());
								concurrentRel.setObjective5Id(OrgConstants.MemberPostType.Concurrent.name());
								listRel.add(concurrentRel);
							}
							
							
						}
						
					}
					
					
					
				}
			}
		}
		member.setSecond_post(second_post);
		// isAdmin
		if (ocipUser.getIsAdmin() == OcipUser.IS_ADMIN_TRUE) {
			member.setIsAdmin(true);
		}
		// 性别 常量对应
		member.setProperty("gender", ocipUser.getSex());
		member.setProperty("birthday", ocipUser.getBirthday());
		member.setAddress(ocipUser.getAddress());
		member.setProperty("idnum", ocipUser.getIdCard());
		//member.setSecretLevel(ocipUser.getSecretLevel() == null ? 1 : ocipUser.getSecretLevel());
		member.setDescription(ocipUser.getRemarks());

		//  最高学历 常量对应
		/**
		 * OA:0未知，1初中，2高中，3大学专科，4大学本科，5硕士，6博士，7其他，8职高，9中专，10技校
		 * 平台:1小学，2初中，3高中，4大专，5本科，6研究生，7博士，8其他
		 */
		Integer eduLevel = ocipUser.getEduLevel();
		if(eduLevel!=null){
			if(eduLevel==2){
				member.setProperty("eduBack",1);
			}
			else if(eduLevel==3){
				member.setProperty("eduBack",2);
			}
			else if(eduLevel==4){
				member.setProperty("eduBack",3);
			}
			else if(eduLevel==5){
				member.setProperty("eduBack",4);
			}
			else if(eduLevel==6){
				member.setProperty("eduBack",5);
			}
			else if(eduLevel==7){
				member.setProperty("eduBack",6);
			}
			else if(eduLevel==8){
				member.setProperty("eduBack",7);
			}
			else{
				member.setProperty("eduBack",0);
			}
		}

		/**
		 * OA： 1党员，2群众，3共青团员，4民主党派成员 平台：1:中共党员，2:中共预备党员，3:共青团员，4:群众，5:民主党派成员，0未选择
		 */
		// 政治面貌 常量
		Integer politicalPosition = ocipUser.getPoliticalPosition();
		if(politicalPosition!=null){
			if(politicalPosition==1||politicalPosition==2){
				member.setProperty("politics",1);
			}
			else if(politicalPosition==3){
				member.setProperty("politics",3);
			}
			else if(politicalPosition==4){
				member.setProperty("politics",2);
			}
			else if(politicalPosition==5){
				member.setProperty("politics",4);
			}
			else{
				member.setProperty("politics",-1);
			}
		}
		member.setWeixin(ocipUser.getWechat());
		member.setProperty("emailaddress", ocipUser.getEmail());
		member.setProperty("imageid", ocipUser.getPhoto());
		member.setProperty("telnumber", ocipUser.getTelNumber());
		member.setProperty("officenumber", ocipUser.getFixedNumber());

		V3xOrgPrincipal principal = new V3xOrgPrincipal(member.getId(), ocipUser.getLoginName(), "123456");

		member.setV3xOrgPrincipal(principal);
		member.setSortId(ocipUser.getSortId() == null ? 0 : ocipUser.getSortId().longValue());
		member.setEnabled(ocipUser.getIsEnable() == IConstant.ENABLE ? true : false);
		return member;
	}

	public V3xOrgDepartment transOcipDepartment2V3xOrgDepartment(OcipDepartment ocipDepartment,
			V3xOrgDepartment department) throws Exception {
		if (department == null) {
			department = new V3xOrgDepartment();
			department.setIdIfNew();
		}
		if (StringUtils.isBlank(ocipDepartment.getName())) {
			throw new Exception("部门名称为空，部门的平台id为:" + ocipDepartment.getId());
		}
		department.setName(ocipDepartment.getName());
		// modify by lmc G6与平台不同步code
		/*if (StringUtils.isBlank(ocipDepartment.getCode())) {
			String code = ChineseConUtil.cn2FirstSpell(ocipDepartment.getName());
			department.setCode(code);
		} else {
			department.setCode(ocipDepartment.getCode());
		}*/
		if (StringUtils.isBlank(ocipDepartment.getUnitId())) {
			throw new Exception("部门转换失败,原因:部门所属单位的Id为空");
		}
		V3xOrgAccount account = orgManager.getAccountById(Long.parseLong(ocipDepartment.getUnitId()));
		if (account == null) {
			throw new Exception("部门转换失败,原因:找不到部门所属单位");
		}
		department.setOrgAccountId(account.getId());
		if (StringUtils.isBlank(ocipDepartment.getParentId())) {
			throw new Exception("部门转换失败,原因:父级Id为空");
		}
		V3xOrgUnit parent = orgManager.getUnitById(Long.parseLong(ocipDepartment.getParentId()));
		if (parent == null) {
			throw new Exception("部门转换失败,原因:找不到父级");
		}
		department.setSuperior(parent.getId());
		department.setGroup(false);
		department.setLevelScope(-1);
		department.setIsDeleted(false);
		department.setStatus(1);
		//默认不创建部门空间
		department.setCreateDeptSpace(false);
		department.setSortId(ocipDepartment.getSortId() != null ? ocipDepartment.getSortId().longValue() : 0l);
		department.setEnabled(ocipDepartment.getIsEnable() == IConstant.ENABLE ? true : false);
		return department;
	}

	/**
	 * 获取人员登录密码，已加密
	 * 
	 * @param memberId
	 *            人员id
	 * @return {@link String} 密码
	 * @throws NoSuchPrincipalException
	 * @authur wxt.touxin
	 * @version 2017年6月14日
	 */
	private String getMemberPassword(Long memberId) throws NoSuchPrincipalException {
		try {
			return principalManager.getPassword(memberId);
		} catch (NoSuchPrincipalException e) {
			throw new NoSuchPrincipalException("获取账号密码失败memberId= " + memberId);
		}
	}

	/**
	 * 验证组织机构（单位、部门）是否存在
	 * 
	 * @param unitId
	 *            组织机构id
	 * @throws BusinessException
	 * @authur wxt.touxin
	 * @version 2017年6月16日
	 */
	public void validateUnit(String unitId) throws BusinessException {
		if (StringUtils.isBlank(unitId)) {
			throw new BusinessException("组织机构id为空");
		}
		if (orgManager.getUnitById(Long.parseLong(unitId)) == null) {
			throw new BusinessException("组织机构不存在！【" + unitId + "】");
		}
	}

	/**
	 * 验证岗位是否存在
	 * 
	 * @param postId
	 *            岗位id
	 * @throws BusinessException
	 * @authur wxt.touxin
	 * @version 2017年6月16日
	 */
	public void validatePost(String postId) throws BusinessException {
		if (StringUtils.isBlank(postId)) {
			throw new BusinessException("岗位id为空");
		}
		if (orgManager.getPostById(Long.parseLong(postId)) == null) {
			throw new BusinessException("岗位不存在！【" + postId + "】");
		}
	}

	/**
	 * 验证职级是否存在
	 * 
	 * @param levelId
	 *            职级id
	 * @throws BusinessException
	 * @authur wxt.touxin
	 * @version 2017年6月16日
	 */
	public void validateLevel(String levelId) throws BusinessException {
		if (StringUtils.isBlank(levelId)) {
			throw new BusinessException("职级id为空");
		}
		if (orgManager.getLevelById(Long.parseLong(levelId)) == null) {
			throw new BusinessException("职级不存在！【" + levelId + "】");
		}
	}
}
