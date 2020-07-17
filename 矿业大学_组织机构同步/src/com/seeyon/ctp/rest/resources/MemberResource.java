/**
 * $Author:Macx$
 * $Rev: 1.0 $
 * $Date:: 2013-10-18 下午1:55:51#$:
 *
 * Copyright (C) 2013 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */

package com.seeyon.ctp.rest.resources;

import com.seeyon.apps.ldap.event.OrganizationLdapEvent;
import com.seeyon.apps.ldap.manager.LdapBindingMgr;
import com.seeyon.apps.ldap.util.LdapUtils;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.Constants.LoginOfflineOperation;
import com.seeyon.ctp.common.customize.manager.CustomizeManager;
import com.seeyon.ctp.common.encrypt.CoderFactory;
import com.seeyon.ctp.common.enums.LoginSignEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.NoSuchPartitionException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.fontimage.FontImageManger;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.safetyprotection.enums.SafetyProtectionHandleResultEnum;
import com.seeyon.ctp.common.safetyprotection.manager.SafetyProtectionLogManager;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.common.web.util.WebUtil;
import com.seeyon.ctp.login.online.OnlineRecorder;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.*;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.memberleave.manager.MemberLeaveDataInterface;
import com.seeyon.ctp.organization.memberleave.manager.MemberLeaveManager;
import com.seeyon.ctp.rest.util.MemberUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.TextEncoder;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation;
import org.apache.commons.logging.Log;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <p>
 * Title: REST 人员资源
 * </p>
 * <p>
 * </p>
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 * <p>
 * Company: seeyon.com
 * </p>
 *
 * @since 5.0Sp2
 */
@Path("orgMember")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces(MediaType.APPLICATION_JSON)
public class MemberResource extends OrganizationSingularBaseResource<V3xOrgMember> {
	private static Log         log                      = CtpLogFactory.getLog(MemberResource.class);
	final int CACHE_DURATION_IN_SECOND = 24*3600; // 24 hour
	final long CACHE_DURATION_IN_MS = CACHE_DURATION_IN_SECOND  * 1000L;
	@Context HttpServletResponse response;
    private static CustomizeManager customizeManager = null;
    private static LdapBindingMgr ldapBindingMgr = null;
	private Pattern regex = Pattern.compile("fileId=((-)*\\w*)", Pattern.MULTILINE);
    private FileManager fileManager = null;
    private OrganizationLdapEvent organizationLdapEvent;
    private MemberLeaveManager memberLeaveManager;
    private UserMessageManager userMessageManager;
    private AppLogManager appLogManager;
    private SafetyProtectionLogManager safetyProtectionLogManager;
    public SafetyProtectionLogManager getSafetyProtectionLogManager() {
    	 if(safetyProtectionLogManager == null){
             safetyProtectionLogManager = (SafetyProtectionLogManager) AppContext.getBean("safetyProtectionLogManager");
         }
         return safetyProtectionLogManager;
	}
	public void setSafetyProtectionLogManager(SafetyProtectionLogManager safetyProtectionLogManager) {
		this.safetyProtectionLogManager = safetyProtectionLogManager;
	}
	public OrganizationLdapEvent getOrganizationLdapEvent() {
    	organizationLdapEvent = (OrganizationLdapEvent) AppContext.getBean("organizationLdapEvent");
        return organizationLdapEvent;
    }
    public FileManager getFileManager() {
    	if(fileManager==null) {
        	fileManager = (FileManager) AppContext.getBean("fileManager");
    	}
    	return this.fileManager;
    }
    public static CustomizeManager getCustomizeManager() {
        if(customizeManager == null){
            customizeManager = (CustomizeManager) AppContext.getBean("customizeManager");
        }

        return customizeManager;
    }
    public MemberLeaveManager getMemberLeaveManager() {
        if(memberLeaveManager == null){
        	memberLeaveManager = (MemberLeaveManager) AppContext.getBean("memberLeaveManager");
        }

        return memberLeaveManager;
    }
    public UserMessageManager getUserMessageManager() {
        if(userMessageManager == null){
        	userMessageManager = (UserMessageManager) AppContext.getBean("userMessageManager");
        }

        return userMessageManager;
    }
    public AppLogManager getAppLogManager() {
        appLogManager = (AppLogManager) AppContext.getBean("appLogManager");
        return appLogManager;
    }

    public static LdapBindingMgr getLdapBindingMgr() {
        if(ldapBindingMgr == null){
        	ldapBindingMgr = (LdapBindingMgr) AppContext.getBean("ldapBindingMgr");
        }
        return ldapBindingMgr;
    }

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@RestInterfaceAnnotation
	/**
	 * @api {GET} /orgMember 按登录名获取人员信息
	 * @apiGroup Organization
	 * @apiParam {String} [loginName] 人员登录名
	 * @apiHeader {String} [token] 访问令牌
	 * @apiSampleRequest /orgMember?loginName=test
	 *
	 */
	public Response getMemberByLoginName(
			@QueryParam("loginName") String loginName) throws Exception {
//		V3xOrgMember member =getOrgManager().getMemberByLoginName(decode(loginName));
		if(getLdapBindingMgr()!=null){
			loginName = getLdapBindingMgr().getLoginName(decode(loginName));
		}
		return ok(getOrgManager().getMemberByLoginName(loginName));
	}


	@GET
    @Produces(MediaType.APPLICATION_JSON)
	@Path("telephone/{telephoneNumber}")
	@RestInterfaceAnnotation
	/**
	 * @api {GET} /orgMember/:telephoneNumber 按电话号码查找人员
	 * @apiGroup Organization
	 * @apiParam  {Number} [telephoneNumber] 人员电话号码
	 * @apiSampleRequest /orgMember/13333333333
	 */
	public Response getMemberByTel(@PathParam("telephoneNumber") String telephoneNumber, @DefaultValue("0") @QueryParam("unitId") Long unitId) throws BusinessException {
	    if (unitId == 0) {
	        if((Boolean)(SysFlag.sys_isGroupVer.getFlag())){
	            unitId = OrgConstants.GROUPID;
	        } else {
	            unitId = OrgConstants.ACCOUNTID;
	        }
	    }
	    return ok(getOrgManager().getMembersByMobile(telephoneNumber, unitId));
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	@RestInterfaceAnnotation
	/**
	 * 新增人员
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public Response addMember(Map data) throws Exception {
		OrganizationMessage result = new OrganizationMessage();
		try {
			V3xOrgMember member = getAddMember(data);
			return ok(getOrgManagerDirect().addMember(member));
		} catch (BusinessException e) {
			result.addErrorMsg(null, ResourceUtil.getString("org.member_form.loginName.label")+" "+
										(data.get("loginName")==null?"":data.get("loginName").toString())+" "+
										e.getMessage());
			return ok(result);
		}
	}

	@POST
	@Path("addMembers")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	@RestInterfaceAnnotation
	/**
	 * 批量新增人员
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public Response addMembers(List<Map> datas) throws Exception {
		OrganizationMessage result = new OrganizationMessage();
		for(Map data : datas){
			try {
				V3xOrgMember member = getAddMember(data);
				MemberUtil.mergeMessage(result, getOrgManagerDirect().addMember(member));
			} catch (BusinessException e) {
				result.addErrorMsg(null, ResourceUtil.getString("org.member_form.loginName.label")+" "+
										(data.get("loginName")==null?"":data.get("loginName").toString())+" "+
										e.getMessage());
			}
		}
		return ok(result);
	}

	private V3xOrgMember getAddMember(Map data) throws Exception{
		Long orgAccountId = convertId(V3xOrgAccount.class,data.get("orgAccountId"),null);
		data.put("orgAccountId",orgAccountId);
		data.put("orgDepartmentId",convertId(V3xOrgDepartment.class,data.get("orgDepartmentId"),orgAccountId));
		data.put("orgPostId",convertId(V3xOrgPost.class,data.get("orgPostId"),orgAccountId));
		data.put("orgLevelId",convertId(V3xOrgLevel.class,data.get("orgLevelId"),orgAccountId));

		V3xOrgMember member = MemberUtil.createMember(data);
		judgeSortId(data,member);
		required(member,new String[]{"orgAccountId","orgDepartmentId","orgLevelId","loginName","orgPostId","name"});
		/**单独处理副岗****/
		data.put("id", member.getId());
		List<MemberPost> secondPost = setSecondInfo(data);
		member.setSecond_post(secondPost);
		MemberUtil.setMemberProperties(member, data);
		checkMember(member);
		return member;
	}

	@PUT
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	@RestInterfaceAnnotation
	/**
	 * 更新人员信息。
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public Response updateMember(Map data) throws Exception {
		OrganizationMessage result = new OrganizationMessage();
		try {
			V3xOrgMember member = getUpdateMember(data);
			if(member == null){
				return ok(null);
			}
			return ok(getOrgManagerDirect().updateMember(member));
		} catch (BusinessException e) {
			result.addErrorMsg(null,"id "+(data.get("id")==null?"":data.get("id").toString())+" "+
					e.getMessage());
			return ok(result);
		}
	}

	@POST
	@Path("updateMembers")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	@RestInterfaceAnnotation
	/**
	 * 批量更新人员信息。
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public Response updateMembers(List<Map> datas) throws Exception {
		OrganizationMessage result = new OrganizationMessage();
		for(Map data : datas){
			try {
				V3xOrgMember member = getUpdateMember(data);
				if(member == null){
					continue;
				}
				MemberUtil.mergeMessage(result, getOrgManagerDirect().updateMember(member));
			} catch (BusinessException e) {
				result.addErrorMsg(null,"id "+
						(data.get("id")==null?"":data.get("id").toString())+" "+
						e.getMessage());
			}
		}
		return ok(result);
	}

	private V3xOrgMember getUpdateMember(Map data) throws Exception{
		String ldapbind=(String) data.get("ldapbind");
		V3xOrgEntity entity = matchEntity(data);
		if (entity == null) {
			return null;
		}

		Long orgAccountId = convertId(V3xOrgAccount.class,data.get("orgAccountId"),null);
		data.put("orgAccountId",orgAccountId);
		data.put("orgDepartmentId",convertId(V3xOrgDepartment.class,data.get("orgDepartmentId"),orgAccountId));
		data.put("orgPostId",convertId(V3xOrgPost.class,data.get("orgPostId"),orgAccountId));
		data.put("orgLevelId",convertId(V3xOrgLevel.class,data.get("orgLevelId"),orgAccountId));

		V3xOrgMember member = (V3xOrgMember)entity;
		data.put("id",entity.getId());// 将data中的id替换为真实的id。未替换之前的id可能是String、Long、Map三种类型。
		/**单独处理副岗****/
		List<MemberPost> secondPost = setSecondInfo(data);

		member = copyProperties(data, member);
		MemberUtil.updatePrincipal(data, member);
		MemberUtil.setMemberProperties(member, data);
		required(member,new String[]{"id"});
		if(secondPost.size()>0){
			member.setSecond_post(secondPost);
		}
		/**清空副岗**/
		if(data.get("del_second_post")!=null&&true==Boolean.parseBoolean((String) data.get("del_second_post"))){
			List<MemberPost> secondPostinfo=new ArrayList<MemberPost>();
			member.setSecond_post(secondPostinfo);
		}
		checkMember(member);
		if(LdapUtils.isLdapEnabled()&&ldapbind!=null&&!"".equals(ldapbind)){

	        User user = new User();
	        user.setName("api-rest");
	        user.setAccountId(member.getOrgAccountId());
	        user.setLoginAccount(member.getOrgAccountId());
	        AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY,user);

		List<V3xOrgMember> memberList=new ArrayList<V3xOrgMember>();
		memberList.add(member);
		getOrganizationLdapEvent().deleteAllBinding(getOrgManagerDirect(), memberList);
		getOrganizationLdapEvent().addMember(member,ldapbind);
		}
		return member;
	}

	private List<MemberPost> setSecondInfo(Map data) throws BusinessException {
		List second_post_list =new ArrayList();
		List<MemberPost> secondPost=new ArrayList<MemberPost>();
		Map second_post_info=new HashMap();

		if(data.get("second_post")!=null){
			second_post_list= (List) data.get("second_post");
			data.remove("second_post");
		}

		String orgAccountId = data.get("orgAccountId").toString();
		for(int i=0;i<second_post_list.size();i++){
			second_post_info=(Map) second_post_list.get(i);

			Long orgAccountIdTemp = convertId(V3xOrgAccount.class,second_post_info.get("orgAccountId"),null);
			second_post_info.put("orgAccountId",orgAccountIdTemp);
			second_post_info.put("deptId",convertId(V3xOrgDepartment.class,second_post_info.get("deptId"),orgAccountIdTemp));
			second_post_info.put("postId",convertId(V3xOrgPost.class,second_post_info.get("postId"),orgAccountIdTemp));

			Object deptId = second_post_info.get("deptId");
			Object postId = second_post_info.get("postId");
			if((data.get("del_second_post")!=null&&true==Boolean.parseBoolean((String) data.get("del_second_post")))||deptId==null){
				return secondPost;
			}

			if(postId == null){
				continue;
			}

			V3xOrgDepartment dept = getOrgManager().getDepartmentById(Long.valueOf(deptId.toString()));
			if(dept == null || !dept.isValid() || !orgAccountId.equals(dept.getOrgAccountId().toString())){
				continue;
			}

			V3xOrgPost post = getOrgManager().getPostById(Long.valueOf(postId.toString()));
			if(post == null || !post.isValid() || !orgAccountId.equals(post.getOrgAccountId().toString())){
				continue;
			}

			Long memberId=null!=second_post_info.get("memberId")?Long.valueOf(second_post_info.get("memberId").toString()):Long.valueOf(data.get("id").toString());
			secondPost.add(MemberPost.createSecondPost(memberId,
					Long.valueOf(deptId.toString())
					, Long.valueOf(postId.toString()), Long.valueOf(second_post_info.get("orgAccountId").toString()),
					1));
		}
		return secondPost;
	}

	@DELETE
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	@RestInterfaceAnnotation
	/**
	 * 按登录名删除人员。
	 * @param loginName
	 * @return
	 * @throws Exception
	 */
	public Response deleteMemberByLoginName(
			@QueryParam("loginName") String loginName) throws Exception {
		V3xOrgMember member = getOrgManager().getMemberByLoginName(decode(loginName));
		return del(member);
	}

	@DELETE
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	@RestInterfaceAnnotation
	/**
	 * 按id删除人员。
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Response deleteMember(@PathParam("id") Long id) throws Exception {
		V3xOrgMember member = getOrgManager().getMemberById(id);
		return del(member);
	}

	@POST
	@Path("removeMembers")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	@RestInterfaceAnnotation
	/**
	 * 按id批量删除人员。
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Response removeMembers(List<Long> ids) throws Exception {
		OrganizationMessage result = new OrganizationMessage();
		for(Long id : ids){
			V3xOrgMember member = getOrgManager().getMemberById(id);
			if(member==null){
				continue;
			}
			MemberUtil.mergeMessage(result, getOrgManagerDirect().deleteMember(member));
		}
		return ok(result);
	}


	/**
	 * 按人员编码删除人员
	 * @param code
	 * @return
	 * @throws Exception
	 */
	@DELETE
	@Path("code/{code}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	@RestInterfaceAnnotation
	public Response deleteMemberByCode(@PathParam("code")String code) throws Exception{
		V3xOrgEntity v3xOrgEntity=getAllEntityByCode(V3xOrgMember.class, code);
		V3xOrgMember member=(V3xOrgMember)v3xOrgEntity;
		return del(member);
	}
	private Response del(V3xOrgMember member) throws BusinessException {
		if (member == null) {
			return ok(null);
		}
		return ok(getOrgManagerDirect().deleteMember(member));
	}

	@PUT
	@Path("{id}/enabled/{enabled}")
	@Produces(MediaType.APPLICATION_JSON)
	@RestInterfaceAnnotation
	/**
	 * 按id启用/停用人员。
	 * @param id
	 * @param enabled
	 * @return
	 * @throws Exception
	 */
	public Response enabledMember(@PathParam("id") Long id,
			@PathParam("enabled") Boolean enabled) throws Exception {
		V3xOrgMember member = getOrgManager().getMemberById(id);
		if (member == null) {
			return ok(null);
		}
		member.setEnabled(enabled);
		return ok(getOrgManagerDirect().updateMember(member));
	}
	/**
	 * 按人员编码启用/停用人员
	 * @param code
	 * @param enabled
	 * @return
	 * @throws Exception
	 */
	@PUT
	@Path("code/{code}/enabled/{enabled}")
	@Produces(MediaType.APPLICATION_JSON)
	@RestInterfaceAnnotation
	public Response enabledMemberByCode(@PathParam("code") String code,
			@PathParam("enabled")Boolean enabled) throws Exception{
		V3xOrgEntity v3xOrgEntity=getAllEntityByCode(V3xOrgMember.class, code);
		if(v3xOrgEntity==null){
			return ok(null);
		}
		v3xOrgEntity.setEnabled(enabled);
		V3xOrgMember member=(V3xOrgMember)v3xOrgEntity;
		return ok(getOrgManagerDirect().updateMember(member));
	}

	@PUT
	@Path("{id}/password/{password}")
	@Produces(MediaType.APPLICATION_JSON)
	@RestInterfaceAnnotation
	/**
	 * 修改人员密码。
	 * @param id
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public Response changePassword(@PathParam("id") Long id,
	        @PathParam("password") String password) throws Exception {
		V3xOrgMember member = getOrgManager().getMemberById(id);
		if (member == null) {
			return ok(null);
		}
		member.getV3xOrgPrincipal().setPassword(password);
		return ok(getOrgManagerDirect().updateMember(member));
	}
	/**
	 *  按人员编码修改人员密码
	 * @param code
	 * @param password
	 * @return
	 * @throws Exception
	 */
	@PUT
	@Path("code/{code}/password/{password}")
	@Produces(MediaType.APPLICATION_JSON)
	@RestInterfaceAnnotation
	public Response memberPasswordByCode(@PathParam("code")String code,
			@PathParam("password")String password) throws Exception{
		V3xOrgEntity v3xOrgEntity=getAllEntityByCode(V3xOrgMember.class, code);
		if(v3xOrgEntity==null){
			return null;
		}
		V3xOrgMember member=(V3xOrgMember)v3xOrgEntity;
		member.getV3xOrgPrincipal().setPassword(password);
		return ok(getOrgManagerDirect().updateMember(member));
	}
	@GET
    @Path("effective/password/encoded")
    @Produces(MediaType.APPLICATION_JSON)
	@RestInterfaceAnnotation
	public Response validateRequiredEncodedPassword() {
		return ok(true);
	}
	@GET
    @Path("effective/loginName/{loginName}")
    @Produces(MediaType.APPLICATION_JSON)
	@RestInterfaceAnnotation
    /**
     * 验证用户的有效性。
     * @param loginName
     * @param password
     * @return
     * @throws Exception
     */
    public Response validateUser(@PathParam("loginName") String loginName,
            @QueryParam("password") String password) throws Exception {
		String pwd = password;
		if(pwd.startsWith("/1.0/")) {
			pwd = TextEncoder.decode(pwd);
		}
	    String name = decode(loginName);
	    String[] s = LdapUtils.authenticate(name, pwd);
	    //继续往下走验证用户
	    if(s == null){
	        V3xOrgMember member = getOrgManager().getMemberByLoginName(name);
	        if (member == null || !member.isValid()) {
				saveSafetyProtectionLog(name);
	            return ok(false);
	        }
	        if (getPrincipalManager().authenticate(member.getLoginName(), pwd)) {
	            return ok(true);
	        }

			saveSafetyProtectionLog(name);
	    } else {
	        return ok(true);
	    }
        return ok(false);
    }
	/**
	 * 获取人员的头像原图
	 * @param memberId 人员id
	 * @return
	 * @throws Exception
	 */
	@GET
	@Path("avatar/{memberId}/large")
	@Produces("image/*")
	@RestInterfaceAnnotation
	public Response getAvatar(@PathParam("memberId") Long memberId,@QueryParam("maxWidth")@DefaultValue("400") int maxWidth) throws Exception {

		File f=null;
    	try{
    		f = getAvatarFile(memberId, "normal", maxWidth);
		}
		catch (Exception e){
    		throw e;
		}

        if ((f==null)||(!f.exists())) {
			throw new BusinessException("头像文件不存在");
        }
        // 为避免后台修改了头像前台未感知，ETAG判断前需检查一遍
        String ETag = memberId + "_" + f.getName().hashCode();
        if(WebUtil.checkEtag(req, response, ETag)){
        	return Response.notModified().build();
        }
        WebUtil.writeETag(req, response, ETag);
		FileInputStream	fileInfo=null;
		try {
			fileInfo=new FileInputStream(f);
			CoderFactory.getInstance().download(fileInfo, response.getOutputStream());
			return Response.status(200).build();
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(),e);
			throw e;
		}finally{
			if (fileInfo != null) {
				try{
					fileInfo.close();
				}catch(IOException e){
					log.error(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * 获取人员的头像
	 * @param memberId 人员id
	 * @param showType  是否显示为缩略图，默认显示：small
	 * @param maxWidth  缩略图最大宽度，默认为（400）
	 * @return
	 * @throws Exception
	 */
	@GET
	@Path("avatar/{memberId}")
	@Produces("image/*")
	@RestInterfaceAnnotation
	public Response getAvatar(@PathParam("memberId") Long memberId,@QueryParam("showType")@DefaultValue("small") String showType,@QueryParam("maxWidth")@DefaultValue("400") int maxWidth) throws Exception {

		File f=null;
    	try{
    		f = getAvatarFile(memberId, showType, maxWidth);
		}
		catch (Exception e){
    		throw e;
		}

        if ((f==null)||(!f.exists())) {
			throw new BusinessException("头像文件不存在");
        }
/*		// 当前用户头像使用Etag，不使用Cache
		if(memberId.equals(AppContext.currentUserId())){
	        // 为避免后台修改了头像前台未感知，ETAG判断前需检查一遍
	        String ETag = memberId + "_" + f.getName().hashCode();
	        if(WebUtil.checkEtag(req, response, ETag)){
	        	return Response.notModified().build();
	        }
	        WebUtil.writeETag(req, response, ETag);
		}else{*/
			long now = System.currentTimeMillis();
			response.addHeader("Cache-Control", "max-age=" + CACHE_DURATION_IN_SECOND);
//			response.addHeader("Cache-Control", "must-revalidate");//optional
			response.setDateHeader("Last-Modified", now);
			response.setDateHeader("Expires", now + CACHE_DURATION_IN_MS);
//		}
		FileInputStream	fileInfo=null;
		try {
			fileInfo=new FileInputStream(f);
			CoderFactory.getInstance().download(fileInfo, response.getOutputStream());
			return Response.status(200).build();
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(),e);
			throw e;
		}finally{
			if (fileInfo != null) {
				try{
					fileInfo.close();
				}catch(IOException e){
					log.error(e.getMessage(), e);
				}
			}
		}
	}
	private File getAvatarFile(Long memberId, String showType, int maxWidth) {
		String path =null;
		File f = null;
        try {
            //String fileName = getCustomizeManager().getCustomizeValue(memberId, "avatar");
            String fileName = OrgHelper.getAvatarImageUrl(memberId);

            if(fileName.startsWith("fileId")){
            	String sFileId = "-1";
            	try {
            		Matcher regexMatcher = regex.matcher(fileName);
            		if (regexMatcher.find()) {
            			sFileId = regexMatcher.group(1);
            		}
            		long fileId = Long.parseLong(sFileId);
            		if("small".equals(showType)){
            			Date createDate = null;
            			int index = fileName.indexOf("createDate");
						if(index>0){
							createDate = Datetimes.parseDate(fileName.substring(index+11));
            			}
						if(createDate == null){
							V3XFile v3xFile = getFileManager().getV3XFile(fileId);
							createDate = v3xFile.getCreateDate();
						}
						f = getFileManager().getThumFile(fileId, createDate,maxWidth);
            		}else{
            			f = getFileManager().getFile(fileId);
            		}
            	} catch (Exception ex) {
            		// Syntax error in the regular expression
            		log.warn(ex.getLocalizedMessage(),ex);
            	}
            }else if(fileName.startsWith("/seeyon/fileUpload.do")){// 管理员上传的头像 /seeyon/fileUpload.do?method=showRTE&fileId=-5580864447645593618&createDate=2018-09-29&type=image
        		int firstpost=fileName.indexOf("&");
        		int thirdpost=fileName.lastIndexOf("&");
        		String fileIdAndCreateDate=fileName.substring(firstpost+1, thirdpost);
        		String[] fileIdAndCreateDateArray=fileIdAndCreateDate.split("&");
        		String fileIdStr=fileIdAndCreateDateArray[0].substring(7);
        		String createDateStr=fileIdAndCreateDateArray[1].substring(11);
        		long fileId = Long.parseLong(fileIdStr);
        		if("small".equals(showType)){
        			Date createDate = null;
					if(createDateStr!=null){
						createDate = Datetimes.parseDate(createDateStr);
        			}
					if(createDate == null){
						V3XFile v3xFile = getFileManager().getV3XFile(fileId);
						createDate = v3xFile.getCreateDate();
					}
					f = getFileManager().getThumFile(fileId, createDate,maxWidth);
        		}else{
        			f = getFileManager().getFile(fileId);
        		}
        	}else if(fileName.startsWith("/seeyon/commonimage.do")){// 个人上传的头像 /seeyon/commonimage.do?method=showImage&id=5907775013756223995&createDate=2018-09-17 17:31:31&size=custom&w=100&h=100
        		int index0 = fileName.indexOf("&id");
        		int index1 = fileName.indexOf("&createDate");
           		if(index0>0 && index1>0){
           			long fileId = Long.parseLong(fileName.substring(index0+4,index1));
           			Date createDate = Datetimes.parseDate(fileName.substring(index1+12,index1+22));
            		if("small".equals(showType)){
    					f = getFileManager().getThumFile(fileId, createDate,maxWidth);
            		}else{
            			f = getFileManager().getFile(fileId);
            		}
        		}

        	}else if(fileName.endsWith(".png") || fileName.endsWith(".gif") ){//文字头像或者是系统预制的头像  /seeyon/fontimage/person/afd87728913c8fe45fc8e7ca84ae07af.png
        		path = fileName.substring(7);
        	}else if(Strings.isNotBlank(fileName)) {
        		//兼容历史老数据
    			f =  new File(SystemEnvironment.getApplicationFolder() + "/apps_res/v3xmain/images/personal/"+fileName.substring(fileName.lastIndexOf("/")+1,fileName.length())); //从未登陆过的人员是 蓝底头像
    		}else {
    			path = "/apps_res/v3xmain/images/personal/pic.gif"; //从未登陆过的人员是 蓝底头像
    		}
        }catch(IllegalArgumentException e) {
        	//无附件的时候疯狂输出无用的日志堆栈，改下逻辑，减少日志输出
        	log.error("生成头像错误，改用默认头像替代："+e.getLocalizedMessage());
        	path = "/apps_res/v3xmain/images/personal/pic.gif"; //使用默认蓝底头像代替
        }catch(NoSuchPartitionException e) {
        	//无附件的时候疯狂输出无用的日志堆栈，改下逻辑，减少日志输出
        	log.error("生成头像错误，分区文件不存在，改用默认头像替代："+e.getLocalizedMessage());
        	path = "/apps_res/v3xmain/images/personal/pic.gif"; //使用默认蓝底头像代替
        } catch (Exception e) {
        	log.error(e.getLocalizedMessage(),e);
        }

        f = f==null ? new File(SystemEnvironment.getApplicationFolder() + path) : f;
		return f;
	}

	private void checkMember(V3xOrgMember member) throws Exception{
		OrgManager orgManager = getOrgManager();
		V3xOrgDepartment dept = orgManager.getDepartmentById(member.getOrgDepartmentId());
		if(dept == null){
			throw new BusinessException("指定的部门不存在："+member.getOrgDepartmentId());
		}
		V3xOrgPost post = orgManager.getPostById(member.getOrgPostId());
		if(post == null){
			throw new BusinessException("指定的岗位不存在："+member.getOrgPostId());
		}
		V3xOrgLevel level = orgManager.getLevelById(member.getOrgLevelId());
		if(level == null){
			throw new BusinessException("指定的职务级别不存在："+member.getOrgLevelId());
		}
		long accountId1 = member.getOrgAccountId();
		long accountId2 = dept.getOrgAccountId();
		long accountId3 = post.getOrgAccountId();
		long accountId4 = level.getOrgAccountId();

		if((accountId1!=accountId2)||(accountId1!=accountId3)||(accountId1!=accountId4)){
			throw new BusinessException("人员的部门、岗位和职务级别必须属于同一单位。");
		}
	}
	@POST
	@Path("groupavatar")
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)
	@RestInterfaceAnnotation
	public Response getGroupAvatar(@FormParam("groupId") String groupId,@FormParam("groupName") String groupName,@FormParam("memberIds") String memberIds,@FormParam("maxWidth") Integer maxWidth) throws IOException {
		String path =null;
		FontImageManger fontImageManger=(FontImageManger) AppContext.getBean("fontImageManger");
		String fullPath=null;
		if(memberIds!=null){
			//根据人员Id重新生成
			String[] memberIdsArray=memberIds.split(";");
			List<Long> memeberIdsList=new ArrayList<Long>();
			for(String memberId:memberIdsArray){
				if(Strings.isNotEmpty(memberId)){
					memeberIdsList.add(Long.valueOf(memberId));
				}
			}
			try {
				Long[] memberIdLongArray=new Long[memeberIdsList.size()];
				fullPath = fontImageManger.getGroupImagePath(groupId,memeberIdsList.toArray(memberIdLongArray),maxWidth);
				path=fullPath.substring(SystemEnvironment.getContextPath().length(),fullPath.length());
				return ok(true);
			} catch (Exception e) {
				log.error(e.fillInStackTrace());
				return ok(false);
			}
		}
		else{
			//没有人员的话,就根据groupId和groupName生成
			try {
				fullPath = fontImageManger.getGroupImagePath(groupId, groupName,maxWidth);
				path=fullPath.substring(SystemEnvironment.getContextPath().length(),fullPath.length());
				return ok(true);
			} catch (Exception e) {
				log.error(e.fillInStackTrace());
				return ok(false);
			}

		}

}
	@GET
	@Path("groupavatar")
	@Produces("image/*")
	@RestInterfaceAnnotation
	public Response getGroupAvatarForGet(@QueryParam("groupId") String groupId,@QueryParam("groupName") String groupName,@QueryParam("maxWidth")@DefaultValue("400") int maxWidth,@QueryParam("ucFlag")@DefaultValue("no") String ucFlag) throws IOException
	{
		String path =null;
		File f = null;
		FontImageManger fontImageManger=(FontImageManger) AppContext.getBean("fontImageManger");
		String fullPath=null;
		if(Strings.isNotEmpty(groupId))
		{
			if(Strings.isNotEmpty(groupName))
			{
				/**
				 * 配合M3前端使用img直接展示,M3在前端会使用
				 * url = encodeURI(encodeURI(url)),在服务器端需要进行2次解码还原原来的内容
				 */
				String firstDecode=URLDecoder.decode(groupName,"utf-8");
				String realGroupName="";
				try{
					realGroupName=URLDecoder.decode(firstDecode,"utf-8");
				}
				catch(Exception e)
				{
					log.error("Second decoding error, direct use of the first decoding content!",e);
					realGroupName=firstDecode;
				}
				try {
					if("no".equals(ucFlag))
					{
						//针对M3
						fullPath = fontImageManger.getImagePathForCommonUse(groupId,realGroupName, maxWidth);
						path=fullPath.substring(SystemEnvironment.getContextPath().length(),fullPath.length());
					}
					if("yes".equals(ucFlag))
					{
						fullPath=fontImageManger.getGroupImagePath(groupId,realGroupName, maxWidth);
						path=fullPath.substring(SystemEnvironment.getContextPath().length(),fullPath.length());
						fullPath= SystemEnvironment.getApplicationFolder()+path;
					}

				} catch (Exception e) {
					log.error(e.fillInStackTrace());
					return null;
				}
			}
			else
			{
				//致信传递过来的groupName可以是空
				try{
					fullPath=fontImageManger.getGroupImagePath(groupId, "NULL", maxWidth);
//					path=fullPath.substring(SystemEnvironment.getContextPath().length(),fullPath.length());
				}
				catch(Exception e)
				{
					log.error(e.fillInStackTrace());
					return null;
				}
			}

			 if("no".equals(ucFlag))
             {
			     f = f==null ? new File(SystemEnvironment.getApplicationFolder() +path) : f;
             }else{
                 f = f==null ? new File(fullPath) : f;
             }


	        if (!f.exists()) {
	        	log.error("don't find image by groupId :" + groupId);
	        	return null;
	        }
			final EntityTag etag = new EntityTag(groupId + "_" + f.getName().hashCode());
			final CacheControl cacheControl = new CacheControl();
			int timeout = 3600;
			cacheControl.setMaxAge(timeout);
			ResponseBuilder builder = request.evaluatePreconditions(etag);
			// the resoruce's information was modified, return it
			if (builder == null) {
				String mt = new MimetypesFileTypeMap().getContentType(f);
				return Response.ok(f, mt).cacheControl(cacheControl).expires(new Date(System.currentTimeMillis()+timeout*1000)).build();
			}
			// the resource's information was not modified, return a 304
			return builder.cacheControl(cacheControl).tag(etag).build();
		}
		else
		{
			return null;
		}

	}

	/**
	 * 人员离职
	 * @param memberId  离职人员Id
	 * @param agentMemberId  代理人Id。
	 * 0:清空代理人。
	 * -1或代理人无效:不做代理人设置处理,维持现状.
	 * 不为空且代理人有效，设置所有代办事项的代理人为此人。
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("leave/{memberId}/{agentMemberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@RestInterfaceAnnotation
	public Response leave(@PathParam("memberId") Long memberId,@PathParam("agentMemberId") Long agentMemberId)throws Exception{
		V3xOrgMember leaveMember  = this.getOrgManager().getMemberById(memberId);
		if(leaveMember == null ||leaveMember.getIsDeleted()){
			return ok("调用接口离职人员时，找不到人员："+memberId);
		}

        //将离职用户踢下下线
        OnlineRecorder.moveToOffline(leaveMember.getLoginName(), LoginOfflineOperation.adminKickoff);
    	leaveMember.setEnabled(false);
    	leaveMember.setState(OrgConstants.MEMBER_STATE.RESIGN.ordinal());

        OrganizationMessage mes = this.getOrgManagerDirect().updateMember(leaveMember);

        if(mes.isSuccess()){
        	User user = new User();
        	user.setId(-1L);
        	user.setName("rest-api");
        	getAppLogManager().insertLog4Account(user, leaveMember.getOrgAccountId(), AppLogAction.Organization_MemberLeave, user.getName(), leaveMember.getName());

        	int agentdo = 0;
			if(agentMemberId == 0){//直接清空代理人
				agentdo = 1;
			}else{
				V3xOrgMember agentMember = this.getOrgManager().getMemberById(agentMemberId);
				if(agentMember == null ||(agentMember!=null && !agentMember.isValid())){//传递的代理人无效，维持不变，不做处理
					agentdo = 2;
				}else{
					agentdo = 3;//设置代理人
				}
			}

        	//离职工作交接
			if(agentdo != 2){
				Map<String, MemberLeaveDataInterface> _datas = AppContext.getBeansOfType(MemberLeaveDataInterface.class);
				for (MemberLeaveDataInterface d : _datas.values()) {
					if(d.isEnabled()){
                  	   //如果是文化建设待审批代办为0， 就不设置代理人了
                  	   if("MemberLeaveDataInterfacePubinfoImpl".equals(d.getAppKey()) && d.getCount(memberId)==0){
                  		   continue;
                  	   }
						if(agentdo == 1){//没有传代理人 直接清空
							d.removeAgent(memberId);
						}else if(agentdo == 3){
							V3xOrgMember agentMember = this.getOrgManager().getMemberById(agentMemberId);
							if(agentMember == null ||(agentMember!=null && !agentMember.isValid())){//传递的代理人无效，维持不变，不做处理
								break;
							}else{
								d.doHandle(memberId, agentMemberId);

								//消息要带上后缀：(来自离职办理)
								MessageContent c = MessageContent.get("agent.setting.msg.remind.from.memberleave", leaveMember.getName(), getFlag(d));
								MessageReceiver receiver = MessageReceiver.get(-1L, agentMemberId);
								getUserMessageManager().sendSystemMessage(c, ApplicationCategoryEnum.organization, user.getId(), receiver);
							}
						}

					}
				}
			}

        }
        return ok(mes);
	}

    private static int getFlag(MemberLeaveDataInterface memberLeaveDataInterface){
        if(Strings.equals(memberLeaveDataInterface.getLabel(), "member.leave.freecollaboration.title")){
            return 0;
        }
        if(Strings.equals(memberLeaveDataInterface.getLabel(), "member.leave.colformtemplate.title")){
            return 1;
        }
        if(Strings.equals(memberLeaveDataInterface.getLabel(), "member.leave.documenttodo.title")){
            return 2;
        }
        if(Strings.equals(memberLeaveDataInterface.getLabel(), "member.leave.publicinfopending.title")){
            return 3;
        }

        return -1;
    }

	/**
	 * 保存异常登录日志
	 * @param loginName
	 */
	private void saveSafetyProtectionLog(String loginName){
		try{
			getSafetyProtectionLogManager().saveAbnormalLoginLog(loginName, SafetyProtectionHandleResultEnum.FAILURE.getKey(), LoginSignEnum.wechat.getKey());
		}catch(Exception e){// 出现异常方法内部处理，不影响其它业务功能
			log.error("",e);
		}
	}
}
