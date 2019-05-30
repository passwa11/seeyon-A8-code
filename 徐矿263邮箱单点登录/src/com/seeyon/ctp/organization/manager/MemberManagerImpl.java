package com.seeyon.ctp.organization.manager;

import java.io.File;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.seeyon.apps.ext.xk263Email.axis.xmapi.XmapiImpl;
import com.seeyon.apps.ext.xk263Email.axis.xmapi.XmapiImplServiceLocator;
import com.seeyon.apps.ext.xk263Email.util.Mail263Util;
import com.seeyon.apps.ext.xk263Email.util.SignUtil;
import com.seeyon.apps.ext.xk263Email.util.ZCommonUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import com.seeyon.apps.addressbook.manager.AddressBookCustomerFieldInfoManager;
import com.seeyon.apps.addressbook.manager.AddressBookManager;
import com.seeyon.apps.addressbook.po.AddressBook;
import com.seeyon.apps.ldap.config.LDAPConfig;
import com.seeyon.apps.ldap.event.OrganizationLdapEvent;
import com.seeyon.apps.ldap.util.LdapUtils;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.Constants.LoginOfflineOperation;
import com.seeyon.ctp.common.constants.CustomizeConstants;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.customize.manager.CustomizeManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.Constants;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.LocaleContext;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.metadata.bo.MetadataColumnBO;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.login.online.OnlineRecorder;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.MemberPost;
import com.seeyon.ctp.organization.bo.MemberRole;
import com.seeyon.ctp.organization.bo.OrganizationMessage;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgPost;
import com.seeyon.ctp.organization.bo.V3xOrgPrincipal;
import com.seeyon.ctp.organization.bo.V3xOrgRelationship;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.organization.dao.OrgCache;
import com.seeyon.ctp.organization.dao.OrgDao;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.event.UpdateMemberRoleEvent;
import com.seeyon.ctp.organization.inexportutil.DataUtil;
import com.seeyon.ctp.organization.inexportutil.ResultObject;
import com.seeyon.ctp.organization.po.OrgMember;
import com.seeyon.ctp.organization.principal.PrincipalManager;
import com.seeyon.ctp.organization.webmodel.WebV3xOrgMember;
import com.seeyon.ctp.portal.api.SpaceApi;
import com.seeyon.ctp.privilege.manager.PrivilegeMenuManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.UniqueList;
import com.seeyon.ctp.util.ZipUtil;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;

import javax.xml.rpc.ServiceException;

@CheckRoleAccess(roleTypes={Role_NAME.GroupAdmin, Role_NAME.AccountAdministrator,Role_NAME.DepAdmin,Role_NAME.HrAdmin})
public class MemberManagerImpl implements MemberManager {

    private final static Log      logger = LogFactory.getLog(MemberManagerImpl.class);
    protected OrgDao              orgDao;
    protected OrgCache            orgCache;
    protected OrgManagerDirect    orgManagerDirect;
    protected OrgManager          orgManager;
    protected PrincipalManager    principalManager;
    protected AppLogManager       appLogManager;
    protected EnumManager         enumManagerNew;
    protected RoleManager         roleManager;
    private SpaceApi          spaceApi;
    private OrganizationLdapEvent organizationLdapEvent;
    private CustomizeManager customizeManager;
    protected AddressBookManager addressBookManager;
    protected AddressBookCustomerFieldInfoManager addressBookCustomerFieldInfoManager;

    private PrivilegeMenuManager privilegeMenuManager;
    private FileManager fileManager;
    private AttachmentManager attachmentManager;
    
    
    public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}
    
    protected SpaceApi getSpaceApi() {
        if (spaceApi == null) {
            spaceApi = (SpaceApi) AppContext.getBean("spaceApi");
        }
        return spaceApi;
    }

    public void setOrganizationLdapEvent(OrganizationLdapEvent organizationLdapEvent) {
        this.organizationLdapEvent = organizationLdapEvent;
    }

    public void setRoleManager(RoleManager roleManager) {
        this.roleManager = roleManager;
    }

    public void setEnumManagerNew(EnumManager enumManagerNew) {
        this.enumManagerNew = enumManagerNew;
    }

    public void setOrgManagerDirect(OrgManagerDirect orgManagerDirect) {
        this.orgManagerDirect = orgManagerDirect;
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

    public void setPrincipalManager(PrincipalManager principalManager) {
        this.principalManager = principalManager;
    }

    public void setAppLogManager(AppLogManager appLogManager) {
        this.appLogManager = appLogManager;
    }

	public void setCustomizeManager(CustomizeManager customizeManager) {
		this.customizeManager = customizeManager;
	}
	
	public void setAddressBookManager(AddressBookManager addressBookManager) {
		this.addressBookManager = addressBookManager;
	}

	public void setAddressBookCustomerFieldInfoManager(
			AddressBookCustomerFieldInfoManager addressBookCustomerFieldInfoManager) {
		this.addressBookCustomerFieldInfoManager = addressBookCustomerFieldInfoManager;
	}

	/***************************/

    @Override
    @CheckRoleAccess(roleTypes={Role_NAME.GroupAdmin, Role_NAME.AccountAdministrator,Role_NAME.DepAdmin,Role_NAME.HrAdmin})
    public FlipInfo showByAccount(FlipInfo fi, Map params) throws BusinessException {
        if (params.containsKey("showByType") && null != params.get("showByType")
                && "showByDepartment".equals(params.get("showByType").toString())) {
            return showByDepartment(fi, params);//按照部门展现
        }

        /********过滤和条件搜索*******/
        Map queryParams = new HashMap<String, Object>();
        Boolean enabled = null;
        Long secondPostId = null;
        String workLocal = null;
        String condition = String.valueOf(params.get("condition"));
        Object value = params.get("value") == null ? "" : params.get("value");
        if ("state".equals(condition)) {//在职/离职过滤
            value = "1".equals(String.valueOf(params.get("value"))) ? Integer.valueOf(1) : Integer.valueOf(2);
            queryParams.put("state", value);
        }

        //选人界面过来的查询条件和值
        if ("orgDepartmentId".equals(condition) || "orgPostId".equals(condition) || "orgLevelId".equals(condition)) {
        	List<String> strs = (List<String>)params.get("value");
            if (Strings.isEmpty(strs)) {
                value = null;
            } else {
            	String s1= strs.get(1).trim();
            	if(Strings.isEmpty(s1)){
            		value = null;
            	}else{
            		String[] strs2 = s1.split("[|]");
            		value = Long.valueOf(strs2[1].trim());
            	}
            }
            enabled = true;
            queryParams.put(condition, value);
        }
        if("name".equals(condition)) {
            queryParams.put("name", value);
        }
        if("loginName".equals(condition)) {
            queryParams.put("loginName", value);
        }
        if (params.containsKey("state") && Strings.isNotBlank(String.valueOf(params.get("state")))) {
            queryParams.put("state", Integer.parseInt(String.valueOf(params.get("state"))));
        }
        if (params.containsKey("enabled") && Strings.isNotBlank(String.valueOf(params.get("enabled")))) {
            enabled = (Boolean) params.get("enabled");
        }
        if (params.containsKey("orgDepartmentId") && Strings.isNotBlank(String.valueOf(params.get("orgDepartmentId")))) {
            queryParams.put("orgDepartmentId", Long.parseLong(String.valueOf(params.get("orgDepartmentId"))));
        }
        if("secPostId".equals(condition)) {
        	List<String> strs = (List<String>)params.get("value");
            if (Strings.isEmpty(strs)) {
                value = null;
            }else {
            	String s1= strs.get(1).trim();
            	if(Strings.isEmpty(s1)){
            		value = null;
            	}else{
            		String[] strs2 = s1.split("[|]");
            		value = Long.valueOf(strs2[1].trim());
            	}
            }
            enabled = true;
            secondPostId = (Long) value;
        }
        if("search_workLocalId".equals(condition)) {
            String str = (String)params.get("value");
            if(Strings.isNotBlank(str)) {
            	workLocal= enumManagerNew.parseToIds(str,OrgConstants.WORKLOCAL_ID);
            	queryParams.put("workLocal", workLocal + "%");
            }
        }
        
        if("code".equals(condition)) {
        	queryParams.put("code", value);
        }
        /********************/

        Long accountId = Long.parseLong(params.get("accountId").toString());
        if(null !=  secondPostId) {
            orgDao.getAllMemberPOByAccountIdAndSecondPostId(accountId, secondPostId, true, enabled, queryParams, fi);
        } else {
            orgDao.getAllMemberPOByAccountId(accountId, true, enabled, queryParams, fi);
        }
        return this.dealResult(fi);
    }

    /**
     * 点击某一部门展示人员
     */
    @Override
    public FlipInfo showByDepartment(FlipInfo fi, Map params) throws BusinessException {

        /********过滤和条件搜索*******/
        Boolean enabled = null;
        //String condition = "state";
        String condition = String.valueOf(params.get("condition"));
        //Object value = new Integer(1);
        Object value = null;
        if (params.containsKey("enabled") && Strings.isNotBlank(String.valueOf(params.get("enabled")))) {
            enabled = (Boolean) params.get("enabled");
        }
        if (params.containsKey("condition") && Strings.isNotBlank(String.valueOf(params.get("value")))) {
            value = Integer.parseInt(String.valueOf(params.get("value")));
        }
        /********************/

        if (!params.containsKey("deptId") || params.get("deptId") == null) {
            throw new BusinessException("按照部门展示id异常");
        }
        Long departmentId = Long.valueOf(params.get("deptId").toString());
        orgDao.getAllMemberPOByDepartmentId(departmentId, false, null, true, enabled, condition, value, fi);
        return this.dealResult(fi);
    }

    @Override
    public HashMap<String, String> noDeptRoles(Long memberId) throws BusinessException {
        HashMap<String, String> map = new HashMap<String, String>();
        V3xOrgMember oldMember = orgManager.getMemberById(memberId);
        //回写角色
        List<MemberRole> memberRoles = orgManager.getMemberRoles(memberId, oldMember.getOrgAccountId());
        StringBuilder roles = new StringBuilder();
        StringBuilder roleIds = new StringBuilder();
        for (MemberRole m : memberRoles) {
            if (OrgConstants.ROLE_BOND.ACCOUNT.ordinal() == m.getRole().getBond()) {
                if (!Strings.equals(memberId, m.getMemberId()))
                    continue;
                if (!oldMember.getOrgAccountId().equals(m.getAccountId()))
                    continue;
                roles.append(m.getRole().getShowName()).append(",");
                roleIds.append(m.getRole().getId()).append(",");
            }
        }
        String showRoles = new String();
        if (0 == roles.length()) {
            showRoles = "";
        } else {
            showRoles = roles.substring(0, roles.length() - 1);
        }
        map.put("roles", showRoles);
        map.put("roleIds", roleIds.toString());
        return map;
    }

    @SuppressWarnings("unchecked")
	@Override
    public HashMap viewOne(Long memberId) throws BusinessException {
        HashMap map = new HashMap();
        V3xOrgMember member = orgManager.getMemberById(memberId);
        ParamUtil.beanToMap(member, map, false);
        
        //回写工作地信息
        String local = member.getLocation();
        if(Strings.isNotBlank(local)) {
        	map.put("workLocal", local);
        	String workspace = "";
        	String[] localArray = local.split(",");
        	for(String itemId : localArray) {
        		try{
        			Long.valueOf(itemId);
        		}catch(Exception e){
        			workspace = "";
        			logger.info("人员工作地信息有误!",e);
        			break;
        		}
        		CtpEnumItem enumItem = enumManagerNew.getEnumItem(Long.valueOf(itemId));
        		if(enumItem != null) {
        			if("".equals(workspace)) {
        				workspace = enumItem.getLabel();
        			} else {
        				workspace += "-" + enumItem.getLabel();
        			}
        		}
        	}
        	map.put("workspace", workspace);
        }
        map.putAll(member.getProperties());
        if(null != map.get("birthday")
                && Strings.isNotBlank(String.valueOf(map.get("birthday")))) {
            Date birth = (Date) map.get("birthday");
            String birthday = Datetimes.format(birth, Datetimes.dateStyle, TimeZone.getDefault());
            map.put("birthday", birthday);
        }
        if(null != map.get("hiredate")
                && Strings.isNotBlank(String.valueOf(map.get("hiredate")))) {
            Date hire = (Date) map.get("hiredate");
            String hiredate = Datetimes.formatDate(hire);
            map.put("hiredate", hiredate);
        }
        if(null!=map.get("reporter")
        		&& Strings.isNotBlank(String.valueOf(map.get("reporter")))){
        	Long reporterId = (Long)map.get("reporter");
        	V3xOrgMember reporter = orgManager.getMemberById(reporterId);
        	if(null !=reporter){
        		map.put("reporterName", reporter.getName());
        	}else{
        		map.put("reporterName", null);
        	}
        }

        //回写副岗信息
        StringBuilder secondPost = new StringBuilder();
        StringBuilder secondPostIds = new StringBuilder();
        List<MemberPost> secondPostList = member.getSecond_post();
        for (int i = 0; i < secondPostList.size(); i++) {
            MemberPost m = secondPostList.get(i);
            //拼接成[部门名称-岗位名称]前台显示
            V3xOrgDepartment _secondDept = orgManager.getDepartmentById(m.getDepId());
            if(_secondDept == null || !_secondDept.isValid()) continue;
            V3xOrgPost _secondPost = orgManager.getPostById(m.getPostId());
            if(_secondPost == null || !_secondPost.isValid()) continue;
            secondPost.append(_secondDept.getName());
            secondPost.append("-");
            secondPost.append(OrgHelper.showOrgPostName(m.getPostId()));
            //拼接成[部门id_岗位id]前台隐藏回写
            secondPostIds.append("Department_Post|");
            secondPostIds.append(m.getDepId());
            secondPostIds.append("_");
            secondPostIds.append(m.getPostId());
            if(i != secondPostList.size()-1) {
                secondPost.append("、");
                secondPostIds.append(",");
            }
        }
        map.put("secondPost", secondPost.toString());
        map.put("secondPostIds", secondPostIds.toString());

        User user = AppContext.getCurrentUser();
        boolean isShowRoleByCategory = true;
        if(user.isAdmin()) {//增加集团管理员，为了兼职管理
            isShowRoleByCategory = false;
        } else if(orgManager.isHRAdmin() || orgManager.isDepartmentAdmin()) {
            isShowRoleByCategory = true;
        }
        //回写角色
        List<MemberRole> memberRoles = orgManager.getMemberRoles(memberId, member.getOrgAccountId());
        StringBuilder roles = new StringBuilder();
        StringBuilder roleIds = new StringBuilder();
        for (MemberRole m : memberRoles) {
            // OA-27888 回写角色也只显示单位角色和部门角色
            if(OrgConstants.ROLE_BOND.DEPARTMENT.ordinal() == m.getRole().getBond()
                    || OrgConstants.ROLE_BOND.ACCOUNT.ordinal() == m.getRole().getBond()) {
                if(!Strings.equals(memberId, m.getMemberId())) {
                    continue;//OA-18699只显示个人的角色
                }
                if(Role_NAME.DepLeader.name().equals(m.getRole().getCode())) {
                    continue;
                }
                if(isShowRoleByCategory) {
                    if (!"0".equals(m.getRole().getCategory().trim())) {
                        //OA-12920 人员列表要显示部门角色,但只显示本部门担任的部门角色
                        if(OrgConstants.ROLE_BOND.DEPARTMENT.ordinal() == m.getRole().getBond()) {
                            V3xOrgDepartment d = m.getDepartment();
                            if(null == d) continue;//OA-15627
                            if(!member.getOrgDepartmentId().equals(d.getId())) {
                                continue;
                            }
                        }
                        if(!member.getOrgAccountId().equals(m.getAccountId()) || roleIds.indexOf(m.getRole().getId().toString())!=-1) continue;
                        roles.append(m.getRole().getShowName()).append(",");
                        roleIds.append(m.getRole().getId()).append(",");
                    }
                } else {
                    //OA-12920 人员列表要显示部门角色,但只显示本部门担任的部门角色
                    if(OrgConstants.ROLE_BOND.DEPARTMENT.ordinal() == m.getRole().getBond()) {
                        V3xOrgDepartment d = m.getDepartment();
                        if(null == d) continue;//OA-15627
                        if(!member.getOrgDepartmentId().equals(d.getId())) {
                            continue;
                        }
                    }
                    if(!member.getOrgAccountId().equals(m.getAccountId()) || roleIds.indexOf(m.getRole().getId().toString())!=-1) continue;
                    roles.append(m.getRole().getShowName()).append(",");
                    roleIds.append(m.getRole().getId()).append(",");
                }
            }
        }
        //显示角色，去除最后那个逗号
        String showRoles = new String();
        if (0 == roles.length()) {
            showRoles = "";
        } else {
            showRoles = roles.substring(0, roles.length() - 1);
        }
        
        List<V3xOrgRole> deptRoles= orgManager.getDepartmentRolesByAccount(member.getOrgAccountId());
        String allDeptRoles = "";
        int n=0;
        for(V3xOrgRole dp : deptRoles){
        	//显示部门角色，去除最后那个逗号
        	if (n==0) {
        		allDeptRoles = dp.getShowName();
        	} else {
        		allDeptRoles = allDeptRoles+","+dp.getShowName();
        	}
        	n++;
        }
        
        if (member.getIsInternal()) {
            //内部人员
            map.put("roles", showRoles);
            map.put("allDeptRoles", allDeptRoles);
            map.put("roleIds", roleIds.toString());
        } else {
            //外部人员
            map.put("extRoles", showRoles);
            map.put("extRoleIds", roleIds.toString());
        }

        //显示兼职信息 Fix OA-29962
        StringBuilder conPostsInfo = new StringBuilder();
        List<MemberPost> memberPosts = orgManager.getMemberConcurrentPosts(member.getId());
        for (MemberPost conrel : memberPosts) {
            V3xOrgAccount a = orgManager.getAccountById(conrel.getOrgAccountId());
            V3xOrgDepartment d = orgManager.getDepartmentById(conrel.getDepId());
            V3xOrgPost p = orgManager.getPostById(conrel.getPostId());
            if (null != a) {
                conPostsInfo.append(ResourceUtil.getString("org.member_form.cnt.account")).append(":")
                        .append(a.getName()).append("\r\n");

                conPostsInfo.append(ResourceUtil.getString("org.member_form.cnt.post")).append(":");
                if (null != d) {
                    conPostsInfo.append(d.getName()).append("(").append(a.getName()).append(")");
                }
                conPostsInfo.append("-");
                if (null != p) {
                    conPostsInfo.append(p.getName()).append("(").append(a.getName()).append(")");
                }
                conPostsInfo.append("\r\n");
            }
        }

        map.put("conPostsInfo", conPostsInfo.toString());

        //如果是外部人员回写工作范围
        if(!member.getIsInternal()) {
            List<V3xOrgEntity> extWorks = orgManager.getExternalMemberWorkScope(member.getId(), false);
            StringBuilder extWorkScope = new StringBuilder();
            StringBuilder extWorkScopeValue = new StringBuilder();
            for (int i = 0; i < extWorks.size(); i++) {
                extWorkScope.append(extWorks.get(i).getName());
                extWorkScopeValue.append(OrgHelper.getEntityTypeByClassSimpleName(extWorks.get(i).getClass().getSimpleName()));
                extWorkScopeValue.append("|");
                extWorkScopeValue.append(extWorks.get(i).getId());
                if(i != extWorks.size()-1) {
                    extWorkScope.append("、");
                    extWorkScopeValue.append(",");
                }
            }
            map.put("extWorkScope", extWorkScope.toString());
            map.put("extWorkScopeValue", extWorkScopeValue.toString());

            //外部人员回写主岗和职务信息，将数据库中的p:岗位,l:职务拆分开来回写到页面上去
            map.put("extprimaryPost", OrgHelper.getExtMemberPriPost(member));
            map.put("extlevelName", OrgHelper.getExtMemberLevel(member));

            if(null != map.get("birthday")
                    && Strings.isNotBlank(String.valueOf(map.get("birthday")))) {
               // Date birth = (Date) map.get("birthday");Datetimes.parse((String)map.get("birthday"))
                String birthday = Datetimes.format(Datetimes.parse(map.get("birthday").toString(), TimeZone.getDefault(),Datetimes.dateStyle), Datetimes.dateStyle, TimeZone.getDefault());
                map.put("extbirthday", birthday);
            }
            map.put("extgender", map.get("gender"));
            map.put("extdescription", map.get("description"));
        }

        map.put("primaryLanguange", String.valueOf(orgManagerDirect.getMemberLocaleById(member.getId())));
        map.put("loginName", member.getLoginName());
        map.put("password", OrgConstants.DEFAULT_INTERNAL_PASSWORD);
        map.put("password2", OrgConstants.DEFAULT_INTERNAL_PASSWORD);
        
        //自定义的通讯录字段
        AddressBook addressBook = addressBookCustomerFieldInfoManager.getByMemberId(memberId);
        List<MetadataColumnBO> metadataColumnList=addressBookManager.getCustomerAddressBookList();
        for(MetadataColumnBO metadataColumn : metadataColumnList){
        	 String key=metadataColumn.getId().toString();
        	 String columnName=metadataColumn.getColumnName();
				 try {
					 Method method=addressBookManager.getGetMethod(columnName);
					 if(null==method){
						 throw new BusinessException("自定义通讯录字段: "+metadataColumn.getLabel()+"不存在！");
					 }
					 if(null!=addressBook){
						 Object value=method.invoke(addressBook, new Object[] {});
						 if(metadataColumn.getType()==0){
							 String saveValue=null==value?"":String.valueOf(value);
							 map.put(key,saveValue);
						 }
						 if(metadataColumn.getType()==1){
							 //Double saveValue=null==value?null:Double.valueOf(String.valueOf(value));
							 if(null==value){
								 map.put(key,"");
							 }else{
								 BigDecimal bd = new BigDecimal(String.valueOf(value)); 
								 DecimalFormat df = new DecimalFormat("########.####");
								 map.put(key,df.format(bd));  
							 }
						 }
						 if(metadataColumn.getType()==2){
							 String saveValue=null==value?"":Datetimes.formatDate((Date)value);
							 map.put(key,saveValue);  
						 }
					 }else{
						 map.put(key,"");  
					 }
				} catch (Exception e) {
					logger.error("查看人员通讯录信息失败！", e);
				}
        }
        

        /********ldap/ad*******/
        if (LdapUtils.isLdapEnabled()) {
            try {
                map.put("ldapUserCodes", organizationLdapEvent.getLdapAdExUnitCode(member.getLoginName()));
            } catch (Exception e) {
                logger.error("显示ldap_ad帐号出错",e);
                //这里与老版本略不同，V3.50SP1直接将Exception抛出去，5.0这里做了修改直接抛出BusinessException异常，由框架处理
                throw new BusinessException("显示ldap_ad帐号出错", e);
            }
        }

        return map;
    }

    @Override
    @CheckRoleAccess(roleTypes={Role_NAME.GroupAdmin,Role_NAME.AccountAdministrator,Role_NAME.HrAdmin})
    public Object createExtMember(String accountId, Map map) throws BusinessException {
        map.put("isInternal", false);
        //map.put("orgDepartmentId", -5995997637631785260L);//TODO
        return createMember(accountId, map);
    }

    public XmapiImplServiceLocator xmapiImplServiceLocator = new XmapiImplServiceLocator();

    @Override
    @CheckRoleAccess(roleTypes={Role_NAME.GroupAdmin,Role_NAME.AccountAdministrator,Role_NAME.HrAdmin, Role_NAME.DepAdmin})
    public Object createMember(String accountId, Map map) throws BusinessException {
        Long currentAccountId = Long.parseLong(accountId);
        User user = AppContext.getCurrentUser();
        V3xOrgMember member = new V3xOrgMember();
        ParamUtil.mapToBean(map, member, false);
        if(null == map.get("birthday")
                || Strings.isBlank((String)map.get("birthday"))) {
            map.put("birthday", null);
        } else {
            map.put("birthday", Datetimes.parse(String.valueOf(map.get("birthday")),TimeZone.getDefault(), "yyyy-MM-dd"));
        }

        member.setId(UUIDLong.longUUID());
        member.setOrgAccountId(currentAccountId);
        //loginName
        V3xOrgPrincipal p = new V3xOrgPrincipal(
                member.getId(),
                map.get("loginName").toString(),
                map.get("password").toString());
        member.setV3xOrgPrincipal(p);

        //外部人员工作范围保存
        if(!member.getIsInternal()) {

            StringBuilder extPostLevel = new StringBuilder();
            //外部人员保存主岗和职务信息，保存在扩展字段ext10中，保存的方式p:岗位,l:职务
            if(Strings.isNotBlank((String)map.get("extprimaryPost"))) {
                extPostLevel.append("p:").append((String)map.get("extprimaryPost")).append(",");
            }
            if(Strings.isNotBlank((String)map.get("extlevelName"))) {
                extPostLevel.append("l:").append((String)map.get("extlevelName")).append(",");
            }
            map.put("extPostLevel", extPostLevel.toString().trim());

            if(null == map.get("extbirthday")
                    || Strings.isBlank((String)map.get("extbirthday"))) {
                map.put("birthday", null);
            } else {
                map.put("birthday", Datetimes.parse(String.valueOf(map.get("extbirthday")),TimeZone.getDefault(), "yyyy-MM-dd"));
            }
            map.put("gender", map.get("extgender"));
            member.setDescription((null == map.get("extdescription")) ? "" :String.valueOf(map.get("extdescription")));//OA-61483
        }

        //second_post 副岗信息不是必选，且可以多选
        if (null != map.get("secondPostIds") &&
                Strings.isNotBlank((String)map.get("secondPostIds"))) {
            String[] secondPosts = map.get("secondPostIds").toString().split(",");
            List<MemberPost> secondPostList = new ArrayList<MemberPost>();
            for (String sp : secondPosts) {
                Long deptId = Long.valueOf((sp.split("[|]")[1]).split("_")[0]);
                Long postId = Long.valueOf((sp.split("[|]")[1]).split("_")[1]);
                Long secPostSortId = member.getSortId();//20130630lilong修改副岗关系的排序号也依照主岗的一致，采用人的排序号
                secondPostList.add(MemberPost.createSecondPost(
                        member.getId(), deptId, postId, currentAccountId, secPostSortId.intValue()));
            }
            member.setSecond_post(secondPostList);
        }
        map.put("imageid", map.get("imageid"));
        
        
        //更新人员类型枚举引用状态
        try{
            //枚举调用记录
            enumManagerNew.updateEnumItemRef("org_property_member_state", String.valueOf(map.get("state")));
            enumManagerNew.updateEnumItemRef("org_property_member_type", String.valueOf(map.get("type")));

        }catch(Exception e){
        	logger.error("调用更新人员类型引用状态错误：",e);
        }
        
        
        //增加工作地，汇报人，入职时间
        if(map.get("workLocal") != null){
        	String workLocal = (String)map.get("workLocal");
        	if(Strings.isNotBlank(workLocal)){
        		//修改枚举引用状态
        		refEnum(workLocal);
        		member.setLocation(workLocal);
        	}
        }
        
        Long reporter = null;
        if(null!= map.get("reporter") && !"".equals(map.get("reporter"))){
        	reporter = Long.valueOf(map.get("reporter").toString());
        	map.put("reporter", reporter);
        }else{
        	map.put("reporter", null);
        }
        if(null == map.get("hiredate")
                || Strings.isBlank((String)map.get("hiredate"))) {
            map.put("hiredate", null);
        } else {
            map.put("hiredate", Datetimes.parse(String.valueOf(map.get("hiredate")), "yyyy-MM-dd"));
        }
        member.setProperties(map);

        //排序號處理
        if (OrgConstants.SORTID_TYPE_INSERT.equals((String) map.get("sortIdtype1"))
                && orgManagerDirect.isPropertyDuplicated(V3xOrgMember.class.getSimpleName(), "sortId",
                        member.getSortId(), member.getOrgAccountId())) {
            orgManagerDirect.insertRepeatSortNum(V3xOrgMember.class.getSimpleName(), member.getOrgAccountId(),
                    member.getSortId(), member.getIsInternal());
        }

        //primaryLanguange
        String pLang = (String)map.get("primaryLanguange");
        if(Strings.isNotBlank(pLang)) {
            orgManagerDirect.setMemberLocale(member, LocaleContext.parseLocale(pLang));
        }

        //自定义的通讯录字段
        List<String> customerAddressBooklist = new ArrayList<String>();
		List<MetadataColumnBO> metadataColumnList=addressBookManager.getCustomerAddressBookList();
        for(MetadataColumnBO metadataColumn : metadataColumnList){
    		if(null!=map.get(metadataColumn.getId().toString()) && !"".equals(String.valueOf(map.get(metadataColumn.getId().toString())))){
				String value=String.valueOf(map.get(metadataColumn.getId().toString()));
				customerAddressBooklist.add(value);
				}else{
					customerAddressBooklist.add("");
				}
        }
        member.setCustomerAddressBooklist(customerAddressBooklist);
        
        OrganizationMessage returnMessage = orgManagerDirect.addMember(member);
        Map result = OrgHelper.getBusinessExceptionMessage(returnMessage);
        if("false".equals(result.get(OrganizationMessage.MessageStatus.SUCCESS.name()))){
        	return result;
        }
        //OrgHelper.throwBusinessExceptionTools(returnMessage);

        //人员创建成功后，再进行条目绑定
        if(returnMessage.isSuccess()){
            /**ldap*/
            this.newMemberBingLdap(member, map);
            //zlc  判断是否需要创建263邮箱
            String create263 = (String) map.get("create263");
            if(null != create263 && !"".equals(create263)){
                if(create263.equals("true")){
                    try {
                        XmapiImpl apiImpl = xmapiImplServiceLocator.getxmapi();

                        // （ userid + domain + passwd + 接口密钥 ）
                        String sign = SignUtil.sign((String) map.get("loginName"),ZCommonUtil.MAIL_DOMAIN, ZCommonUtil.USER_PWD, ZCommonUtil.API_KEY);

                        int createResult = apiImpl.regUser_New((String) map.get("loginName"),
                                ZCommonUtil.MAIL_DOMAIN,
                                ZCommonUtil.USER_PWD,
                                ZCommonUtil.USER_CRYPTTYPE_0,
                                ZCommonUtil.USER_GID_33,
                                -1,
                                ZCommonUtil.GBKToBase64((String) map.get("name")),
                                "",
                                (String) map.get("telnumber"),
                                (String) map.get("officenumber"),
                                "", "", "",
                                ZCommonUtil.USER_ROLE_ID,
                                ZCommonUtil.USER_CHANGEPWD_ON,
                                ZCommonUtil.API_ACCOUNT, sign);

                    } catch (ServiceException e) {
                        logger.error("创建263邮箱出错了,错误信息为："+e.getMessage());
                    } catch (Exception e){

                    }
                }
            }
        }
        //role
        if (member.getIsInternal()) {//内部人员读取roleIds
            if (null != map.get("roleIds") && Strings.isNotBlank((String) map.get("roleIds"))) {
                String roleIds = (String) map.get("roleIds");
                List<Long> ids = new UniqueList<Long>();
                for (String r : roleIds.split(",")) {
                	ids.add(Long.valueOf(r.trim()));
                }
                    
                orgManagerDirect.isCanDeleteMembertoRole(member, currentAccountId, ids);
                dealRoles(currentAccountId, member, roleIds);
            }
        } else {//外部人员读取extRoleIds
            if (null != map.get("extRoles") && Strings.isNotBlank((String) map.get("extRoleIds"))) {
                String roleIds = (String) map.get("extRoleIds");
                dealRoles(currentAccountId, member, roleIds);
            }
        }
        //外部人员工作范围
        if(!member.getIsInternal()
                && null != map.get("extWorkScopeValue")
                && Strings.isNotBlank((String)map.get("extWorkScopeValue"))) {
            String extWorkScopeValue = (String)map.get("extWorkScopeValue");
            String[] entityIds = extWorkScopeValue.split(",");
            List<V3xOrgRelationship> relList = new ArrayList<V3xOrgRelationship>();
            for (String strTemp : entityIds) {
                String[] typeAndId = strTemp.split("[|]");
                V3xOrgRelationship rel = new V3xOrgRelationship();
                rel.setKey(OrgConstants.RelationshipType.External_Workscope.name());
                rel.setSortId(member.getSortId());
                rel.setSourceId(member.getId());
                rel.setObjective0Id(Long.valueOf(typeAndId[1]));
                rel.setOrgAccountId(member.getOrgAccountId());
                rel.setObjective5Id(typeAndId[0]);
                relList.add(rel);
            }
            orgManagerDirect.addOrgRelationships(relList);
        }
        
        if(member.getIsInternal()) {
            appLogManager.insertLog4Account(user, user.isGroupAdmin() ? currentAccountId : AppContext.currentAccountId(), AppLogAction.Organization_NewMember, user.getName(), member.getName());
        } else {
            appLogManager.insertLog4Account(user, user.isGroupAdmin() ? currentAccountId : AppContext.currentAccountId(), AppLogAction.Organization_NewExternalMember, user.getName(), member.getName());
        }
        
        //添加监听用户角色（专指 肖霖）
        UpdateMemberRoleEvent event= new UpdateMemberRoleEvent(this);
    	event.setMember(member);
    	event.setOldMemberRole(new ArrayList<MemberRole>());
    	event.setNewMemberRole(orgManager.getMemberRoles(member.getId(), member.getOrgAccountId()));
    	EventDispatcher.fireEvent(event);
        return returnMessage.getSuccessMsgs().get(0).getEnt().getId();
    }

    /**
     * 修改人员校验，如果所在部门已经开启了部门空间，必须保留这个人部门主管角色
     * @param map
     * @throws BusinessException
     */
    private void checkRole4DeptSpace(Map map) throws BusinessException {
        boolean isDeptManager = false;
        Long id = Long.valueOf((String) map.get("id"));
        Long orgDepartmentId = Long.valueOf((String) map.get("orgDepartmentId"));
        Long orgAccountId = Long.valueOf((String) map.get("orgAccountId"));
        if(null == id || null == orgDepartmentId || null == orgAccountId ) {
            throw new BusinessException("传入ID为空！");
        }
        List<MemberRole> memberRoles = orgManager.getMemberRoles(id, orgAccountId);
        for (MemberRole memberRole : memberRoles) {
            if(Role_NAME.DepManager.name().equals(memberRole.getRole().getCode())) {
                isDeptManager = true;
                break;
            }
        }
        String roleIdStrs = (String) map.get("roleIds");
        HashSet<Long> rIds = new HashSet<Long>();
        V3xOrgRole deptAdmin = orgManager.getRoleByName(Role_NAME.DepManager.name(), orgAccountId);
        for (String r : roleIdStrs.split(",")) {
            if(Strings.isNotBlank(r)) {
                rIds.add(Long.valueOf(r));
            }
        }
        if (isDeptManager && !rIds.contains(deptAdmin.getId())
                && orgManager.isRole(id, orgDepartmentId, Role_NAME.DepManager.name(), null)) {
            Long deptId = Long.valueOf(String.valueOf(map.get("orgDepartmentId")));
            if (getSpaceApi().isCreateDepartmentSpace(deptId)) {
            	List<V3xOrgMember> members = orgManager.getMembersByRole(orgDepartmentId, deptAdmin.getId());
            	if(Strings.isNotEmpty(members) && members.size()==1 && members.get(0).getId().equals(id)){//如果只有当前这一个人是部门主管，则不能取消
            		throw new BusinessException(ResourceUtil.getString("dept.space.mustmanager.member",deptAdmin.getShowName()));
            	}
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    @CheckRoleAccess(roleTypes={Role_NAME.GroupAdmin,Role_NAME.AccountAdministrator,Role_NAME.HrAdmin,Role_NAME.DepAdmin})
    public Object updateMember(Map map) throws BusinessException {
        User user = AppContext.getCurrentUser();
        V3xOrgMember updateMember = new V3xOrgMember();
        ParamUtil.mapToBean(map, updateMember, false);
        if(null == map.get("birthday") || Strings.isBlank((String)map.get("birthday"))) {
            map.put("birthday", null);
        } else {
            map.put("birthday", Datetimes.parse(String.valueOf(map.get("birthday")), TimeZone.getDefault(),"yyyy-MM-dd"));
        }

        V3xOrgMember oldMember = orgManager.getMemberById(updateMember.getId());
        Long currentAccountId = oldMember.getOrgAccountId();
        //如果人员已经绑定了手机号码和邮箱，管理员修改了手机号码和邮箱后，解除绑定 
        if(null==oldMember.getEmailAddress() || null ==map.get("emailaddress") || 
        	(null!=oldMember.getEmailAddress() && null!=map.get("emailaddress") && !oldMember.getEmailAddress().equals(map.get("emailaddress")))){
        	customizeManager.saveOrUpdateCustomize(oldMember.getId(), CustomizeConstants.BIND_EMAIL, "false");
        }
        
        
        if(null==oldMember.getTelNumber() || null ==map.get("telnumber") || 
            	(null!=oldMember.getTelNumber() && null!=map.get("telnumber") && !oldMember.getTelNumber().equals(map.get("telnumber")))){
        	     customizeManager.saveOrUpdateCustomize(oldMember.getId(), CustomizeConstants.BIND_PHONENUMBER, "false");
        }
              
        
        V3xOrgPrincipal oldPrincipal = oldMember.getV3xOrgPrincipal();
        boolean passwordchange = (null != map.get("isChangePWD") && Boolean.valueOf(map.get("isChangePWD").toString()))
                || null == oldPrincipal || !oldPrincipal.getLoginName().equals(map.get("loginName").toString())
                || !OrgConstants.DEFAULT_INTERNAL_PASSWORD.equals(map.get("password").toString());
        if (passwordchange) {
            //loginName
            V3xOrgPrincipal p = new V3xOrgPrincipal(updateMember.getId(), map.get("loginName").toString(), map.get(
                    "password").toString());
            updateMember.setV3xOrgPrincipal(p);
        }


        //外部人员工作范围修改
        if(!updateMember.getIsInternal()) {
            StringBuilder extPostLevel = new StringBuilder();
            //外部人员保存主岗和职务信息，保存在扩展字段ext10中，保存的方式p:岗位,l:职务
            if(Strings.isNotBlank((String)map.get("extprimaryPost"))) {
                extPostLevel.append("p:").append((String)map.get("extprimaryPost")).append(",");
            }
            if(Strings.isNotBlank((String)map.get("extlevelName"))) {
                extPostLevel.append("l:").append((String)map.get("extlevelName")).append(",");
            }
            map.put("extPostLevel", extPostLevel.toString().trim());
            if(null == map.get("extbirthday")
                    || Strings.isBlank((String)map.get("extbirthday"))) {
                map.put("birthday", null);
                updateMember.setProperty("birthday", null);
            } else {
                map.put("birthday", Datetimes.parse(String.valueOf(map.get("extbirthday")),TimeZone.getDefault(), "yyyy-MM-dd"));
                updateMember.setProperty("birthday", Datetimes.parse(String.valueOf(map.get("extbirthday")), "yyyy-MM-dd"));
            }
            map.put("gender", map.get("extgender"));
            map.put("description", map.get("extdescription"));
            updateMember.setProperty("gender", String.valueOf(map.get("extgender")));
            updateMember.setDescription(String.valueOf(map.get("extdescription")));

        }

        //second_post 副岗信息不是必选，且可以多选
        List<MemberPost> secondPostList = new ArrayList<MemberPost>();
        if (null != map.get("secondPostIds")
                && Strings.isNotBlank((String)map.get("secondPostIds"))) {
            String[] secondPosts = map.get("secondPostIds").toString().split(",");
            for (String sp : secondPosts) {
                Long deptId = Long.valueOf((sp.split("[|]")[1]).split("_")[0]);
                Long postId = Long.valueOf((sp.split("[|]")[1]).split("_")[1]);
                Long secPostSortId = updateMember.getSortId();//20130630与副岗关系排序号保持一致
                secondPostList.add(MemberPost.createSecondPost(
                        updateMember.getId(), deptId, postId, updateMember.getOrgAccountId(), secPostSortId.intValue()));
            }
            updateMember.setSecond_post(secondPostList);
        } else {
            updateMember.setSecond_post(secondPostList);
        }
        
        //更新人员类型枚举引用状态
        try{
        	enumManagerNew.updateEnumItemRef("org_property_member_type", (String)map.get("type"));
        }catch(Exception e){
        	logger.error("调用更新人员类型引用状态错误：",e);
        }

        updateMember.setProperties(oldMember.getProperties());
        //增加工作地，汇报人，入职时间
        if(map.get("workLocal") != null){
        	String workLocal = (String)map.get("workLocal");
        	if(Strings.isNotBlank(workLocal)){
        		//修改枚举引用状态
        		refEnum(workLocal);
        		updateMember.setLocation(workLocal);
        	}else{
        		updateMember.setLocation("");
        	}
        }
        Long reporter = null;
        if(null!= map.get("reporter") && !"".equals(map.get("reporter"))){
        	reporter = Long.valueOf(map.get("reporter").toString());
        	map.put("reporter", reporter);
        }else{
        	map.put("reporter", null);
        }
        if(null == map.get("hiredate")
                || Strings.isBlank((String)map.get("hiredate"))) {
            map.put("hiredate", null);
        } else {
            map.put("hiredate", Datetimes.parse(String.valueOf(map.get("hiredate")), "yyyy-MM-dd"));
        }
        updateMember.setProperties(map);

/*        if(!updateMember.getEnabled()
                && OrgConstants.MEMBER_STATE.RESIGN.ordinal() == updateMember.getState()
                && Strings.isNotBlank(String.valueOf(map.get("loginName")))) {
            //Fix OA-9575，由于离职人员要被删除帐号，因此，如果想要修改人员将离职人员填写帐号在启用时
            //增加一些处理，1.前台自动置为启用;2.后台判断如果这个人是离职且还是停用，不进行人员修改直接抛出业务异常
            throw new BusinessException("操作失败！该离职人员帐号已经删除，如果想重新为该人员增加帐号，请先设置为启用！");
        }*/

        //Fix OA-3089 离职人员启用后状态改为在职
        if(updateMember.getEnabled()) {
            updateMember.setState(OrgConstants.MEMBER_STATE.ONBOARD.ordinal());
        }

        //primaryLanguange
        String pLang = (String)map.get("primaryLanguange");
        if(Strings.isNotBlank(pLang)) {
            orgManagerDirect.setMemberLocale(updateMember, LocaleContext.parseLocale(pLang));
        }

        List<MemberRole> memberRoles = orgManager.getMemberRoles(updateMember.getId(), updateMember.getOrgAccountId());

        //OA-47747
        boolean isShowRoleByCategory = true;
        if(user.isAdmin()) {
            isShowRoleByCategory = false;
        } else if(orgManager.isHRAdmin() || orgManager.isDepartmentAdmin()) {
            isShowRoleByCategory = true;
        }
        //前台不允许授权的角色集合catortyRoles
        List<Long> categoryRoles = new UniqueList<Long>();
        //更新人员时新老角色局部存储设定
        StringBuilder oldRoles = new StringBuilder();
        Set<Long> oldRoleIds = new HashSet<Long>();
        //不允许前台分配的角色变量
        StringBuilder oldRolesTemp = new StringBuilder();
        StringBuilder newRoles = new StringBuilder();
        Set<Long> newRoleIds = new HashSet<Long>();
        
        boolean canSetDepAdmin = true;
        for (MemberRole m : memberRoles) {
        	V3xOrgRole role = m.getRole();
            if("0".equals(role.getCategory().trim())) {
            	if(Role_NAME.DepManager.name().equals(role.getCode())){
            		canSetDepAdmin = false;
            	}
                categoryRoles.add(role.getId());//此处将这个人拥有的【不允许前台授权的角色】收集起来
                oldRolesTemp.append(role.getShowName()).append("、");
            }
            if(
	    		  (OrgConstants.ROLE_BOND.DEPARTMENT.ordinal() == role.getBond() || OrgConstants.ROLE_BOND.ACCOUNT.ordinal() == role.getBond()) 
	              && Strings.equals(updateMember.getId(), m.getMemberId())
	              && (null==m.getDepartment() || (null!=m.getDepartment() &&updateMember.getOrgDepartmentId().equals(m.getDepartment().getId())))
              ){
                Long roleId = role.getId();
                //    OA-72355 HR管理元修改一个人a的角色，后台日志记录人员a修改前有3个普通人员的角色
                if(!oldRoleIds.contains(roleId)){
                    oldRoles.append(role.getShowName()).append("、");
                    oldRoleIds.add(roleId);
                }
            }

        }
        //将角色的修改调整到后面，否则这个人的角色缓存不同步
        //role 校验
        boolean hasInterRole = null != map.get("roleIds") && Strings.isNotBlank((String) map.get("roleIds"));
        boolean hasOuterRole = null != map.get("extRoles") && Strings.isNotBlank((String) map.get("extRoleIds"));
        if(updateMember.getIsInternal()) {//内部人员读取roleIds
            // OA-37612 人员部门主管角色取消判断
        	if(!isShowRoleByCategory || (isShowRoleByCategory && canSetDepAdmin)){
        		//单位管理员或者hr管理员并且可以设置部门主管时，才校验部门主管角色是否能被取消掉。
        		checkRole4DeptSpace(map);
        	}
            if (hasInterRole) {
                String roleIdStrs = (String) map.get("roleIds");

                List<Long> roleIds = new UniqueList<Long>();
                for (String r : roleIdStrs.split(",")) {
                    roleIds.add(Long.valueOf(r.trim()));
                    newRoles.append(orgManager.getRoleById(Long.valueOf(r.trim())).getShowName()).append("、");
                    newRoleIds.add(Long.valueOf(r.trim()));
                }
                if(isShowRoleByCategory) {
                    roleIds.addAll(categoryRoles);
                    newRoles.append(oldRolesTemp);
                    newRoleIds.addAll(oldRoleIds);
                }
                orgManagerDirect.isCanDeleteMembertoRole(updateMember, currentAccountId, roleIds);
            }
        } else {//外部人员读取extRoleIds
            if(hasOuterRole) {
                String roleIds = (String) map.get("extRoleIds");

                List<Long> rolesIds = new UniqueList<Long>();
                for (String r : roleIds.split(",")) {
                    rolesIds.add(Long.valueOf(r));
                    newRoles.append(orgManager.getRoleById(Long.valueOf(r.trim())).getShowName()).append("、");
                    newRoleIds.add(Long.valueOf(r.trim()));
                }
                if(isShowRoleByCategory) {
                    rolesIds.addAll(categoryRoles);
                    newRoles.append(oldRolesTemp);
                    newRoleIds.addAll(oldRoleIds);
                }
                orgManagerDirect.isCanDeleteMembertoRole(updateMember, currentAccountId, rolesIds);
            }
        }
        
        
        //自定义的通讯录字段
        List<String> customerAddressBooklist = new ArrayList<String>();
		List<MetadataColumnBO> metadataColumnList=addressBookManager.getCustomerAddressBookList();
        for(MetadataColumnBO metadataColumn : metadataColumnList){
    		if(null!=map.get(metadataColumn.getId().toString()) && !"".equals(String.valueOf(map.get(metadataColumn.getId().toString())))){
				String value=String.valueOf(map.get(metadataColumn.getId().toString()));
				customerAddressBooklist.add(value);
				}else{
					customerAddressBooklist.add("");
				}
        }
        updateMember.setCustomerAddressBooklist(customerAddressBooklist);

        OrganizationMessage returnMessage = orgManagerDirect.updateMember(updateMember);
        Map result = OrgHelper.getBusinessExceptionMessage(returnMessage);
        if("false".equals(result.get(OrganizationMessage.MessageStatus.SUCCESS.name()))){
        	return result;
        }
        //OrgHelper.throwBusinessExceptionTools(returnMessage);
        //人员修改 
        if(returnMessage.isSuccess()){
        	//排序號處理
        	if (OrgConstants.SORTID_TYPE_INSERT.equals((String) map.get("sortIdtype1"))
        			&& orgManagerDirect.isPropertyDuplicated(V3xOrgMember.class.getSimpleName(), "sortId",
        					updateMember.getSortId(), updateMember.getOrgAccountId(), updateMember.getId())) {
        		orgManagerDirect.insertRepeatSortNum(V3xOrgMember.class.getSimpleName(), updateMember.getOrgAccountId(),
        				updateMember.getSortId(), updateMember.getIsInternal(),updateMember.getId());
        	}
        	//外部人员转内部人员排序号处理
        	if(!oldMember.getIsInternal() && updateMember.getIsInternal()
        			&& OrgConstants.SORTID_TYPE_INSERT.equals((String) map.get("sortIdtype1"))
        			&& orgManagerDirect.isPropertyDuplicated(V3xOrgMember.class.getSimpleName(), "sortId",
        					updateMember.getSortId(), updateMember.getOrgAccountId())) {
        		orgManagerDirect.insertRepeatSortNum(V3xOrgMember.class.getSimpleName(), updateMember.getOrgAccountId(),
        				updateMember.getSortId(), updateMember.getIsInternal(),updateMember.getId());
        	}
        }
        
        /***ldap/ad绑定登录名，存储在ctp_org_user_mapper****/
        try {
            this.bindLdap(orgManager.getMemberById(updateMember.getId()), map);
        } catch (BusinessException b) {
            throw b;
        }

        //role  操作
        if(updateMember.getIsInternal()) {//内部人员读取roleIds
            if (hasInterRole) {
                String roleIdStrs = (String) map.get("roleIds");
                orgManagerDirect.cleanMemberAccAndSelfDeptRoles(updateMember,this.canDelRoles(updateMember.getOrgAccountId()));
                dealRoles(currentAccountId, updateMember, roleIdStrs);
            } else {
                orgManagerDirect.cleanMemberAccAndSelfDeptRoles(updateMember,this.canDelRoles(updateMember.getOrgAccountId()));
            }
        } else {//外部人员读取extRoleIds
            if(hasOuterRole) {
                String roleIds = (String) map.get("extRoleIds");
                orgManagerDirect.cleanMemberAccAndSelfDeptRoles(updateMember, this.canDelRole4Outer(updateMember.getOrgAccountId()));
                dealRoles(currentAccountId, updateMember, roleIds);
            } else {
                orgManagerDirect.cleanMemberAccAndSelfDeptRoles(updateMember, this.canDelRole4Outer(updateMember.getOrgAccountId()));
            }
        }
        if(!updateMember.getIsInternal()
                && null != map.get("extWorkScopeValue")
                && Strings.isNotBlank((String)map.get("extWorkScopeValue"))) {
            //更新工作范围
            orgDao.deleteOrgRelationshipPO(OrgConstants.RelationshipType.External_Workscope.name(), updateMember.getId(), null, null);
            String extWorkScopeValue = (String)map.get("extWorkScopeValue");
            String[] entityIds = extWorkScopeValue.split(",");
            List<V3xOrgRelationship> relList = new ArrayList<V3xOrgRelationship>();
            for (String strTemp : entityIds) {
                String[] typeAndId = strTemp.split("[|]");
                V3xOrgRelationship rel = new V3xOrgRelationship();
                rel.setKey(OrgConstants.RelationshipType.External_Workscope.name());
                rel.setSortId(updateMember.getSortId());
                rel.setSourceId(updateMember.getId());
                rel.setObjective0Id(Long.valueOf(typeAndId[1]));
                rel.setOrgAccountId(updateMember.getOrgAccountId());
                rel.setObjective5Id(typeAndId[0]);
                relList.add(rel);

            }
            orgManagerDirect.addOrgRelationships(relList);
        }
        
        //角色变化了
        if(!newRoleIds.equals(oldRoleIds)){
        	
            String onames=oldRoles.toString();
            String nnames=newRoles.toString();
            appLogManager.insertLog4Account(user, user.isGroupAdmin() ? currentAccountId : AppContext.currentAccountId(), AppLogAction.Organization_UpdateMemberRole, user.getName(),updateMember.getName(),
                    onames.length()==0?"":onames.substring(0, onames.length()-1),nnames.length()==0?"":nnames.substring(0, nnames.length()-1));
          //添加监听用户角色（专指 肖霖）
            UpdateMemberRoleEvent event= new UpdateMemberRoleEvent(this);
        	event.setMember(updateMember);
        	event.setOldMemberRole(memberRoles);
        	event.setNewMemberRole(orgManager.getMemberRoles(updateMember.getId(), updateMember.getOrgAccountId()));
        	EventDispatcher.fireEvent(event);
        }
        if (passwordchange) {
            OnlineRecorder.moveToOffline(oldMember.getLoginName(), LoginOfflineOperation.adminKickoff);
        }
        privilegeMenuManager.updateMemberMenuLastDate(updateMember.getId(), updateMember.getOrgAccountId());
        privilegeMenuManager.updateInnerMemberMenuLastDate(updateMember.getId(), updateMember.getOrgAccountId());
        return returnMessage.getSuccessMsgs().get(0).getEnt().getId();
    }

    private void delMemberRole(V3xOrgMember updateMember) throws BusinessException {
        List<Long> sourceIds = new ArrayList<Long>(1);
        sourceIds.add(updateMember.getId());
        orgManagerDirect.deleteRelsInList(sourceIds, OrgConstants.RelationshipType.Member_Role.name());
    }

    private void dealRoles(Long currentAccountId, V3xOrgMember updateMember, String roleIds) throws BusinessException {
        String[] roles = roleIds.split(",");
        Set<Long> validRole  = new HashSet<Long>();
        //当前单位的默认角色
        V3xOrgRole defultRole = roleManager.getDefultRoleByAccount(updateMember.getOrgAccountId());
        //先清理掉改人员的除部门角色外的所有角色信息
        for (String roleId : roles) {
            //部门角色需要保存这个人所在部门的关系
            V3xOrgRole r = orgManager.getRoleById(Long.valueOf(roleId.trim()));
            if(null != r) {
                if(OrgConstants.ROLE_BOND.DEPARTMENT.ordinal() == r.getBond()) {//部门角色
                    orgManagerDirect.addRole2Entity(r.getId(), currentAccountId, updateMember, orgManager.getDepartmentById(updateMember.getOrgDepartmentId()));
                } else {
                    orgManagerDirect.addRole2Entity(r.getId(), currentAccountId, updateMember);
                }
                validRole.add(r.getId());
            } else {
                continue;
            }
        }
        //删除未选择的默认角色 BUG_普通_V5_V5.0sp2_中农立华生物科技股份有限公司_在人员管理-新建一个人员，给了该人员一个角色，确定后，查看该人员除了给了的角色外还多了一个普通角色
        if (!validRole.isEmpty() && !validRole.contains(defultRole.getId())) {
            EnumMap<OrgConstants.RelationshipObjectiveName, Object> objectiveIds = new EnumMap<OrgConstants.RelationshipObjectiveName, Object>(
                    OrgConstants.RelationshipObjectiveName.class);
            objectiveIds.put(OrgConstants.RelationshipObjectiveName.objective1Id, defultRole.getId());
            if (OrgConstants.ROLE_BOND.DEPARTMENT.ordinal() == defultRole.getBond()) {
                objectiveIds.put(OrgConstants.RelationshipObjectiveName.objective0Id, updateMember.getOrgDepartmentId());
            } else {
                objectiveIds.put(OrgConstants.RelationshipObjectiveName.objective0Id, currentAccountId);
            }

            orgDao.deleteOrgRelationshipPO(OrgConstants.RelationshipType.Member_Role.name(), updateMember.getId(),
                    currentAccountId, objectiveIds);
        }
    }

    @Override
    @CheckRoleAccess(roleTypes={Role_NAME.GroupAdmin,Role_NAME.AccountAdministrator,Role_NAME.HrAdmin,Role_NAME.DepAdmin})
    public Object deleteMembers(Long[] ids) throws BusinessException {
        List<V3xOrgMember> members = new ArrayList<V3xOrgMember>();
        for (Long s : ids) {
            members.add(orgManager.getMemberById(s));
        }
        OrganizationMessage returnMessage = orgManagerDirect.deleteMembers(members);
        Map result = OrgHelper.getBusinessExceptionMessage(returnMessage);
        if("false".equals(result.get(OrganizationMessage.MessageStatus.SUCCESS.name()))){
        	return result;
        }
        //OrgHelper.throwBusinessExceptionTools(returnMessage);

        //应用日志
        User user = AppContext.getCurrentUser();
        for (V3xOrgMember member : members) {
            if (member.getIsInternal()) {
                appLogManager.insertLog4Account(user, user.isGroupAdmin() ? member.getOrgAccountId() : AppContext.currentAccountId(), AppLogAction.Organization_DeleteMember, user.getName(), member.getName());
            } else {
                appLogManager.insertLog4Account(user, user.isGroupAdmin() ? member.getOrgAccountId() : AppContext.currentAccountId(), AppLogAction.Organization_DeleteExternalMember, user.getName(),
                        member.getName());
            }
        }
        return true;
    }

    @Override
    @CheckRoleAccess(roleTypes={Role_NAME.GroupAdmin,Role_NAME.AccountAdministrator,Role_NAME.HrAdmin})
    public Object cancelMember(Long[] ids) throws BusinessException {
        List<V3xOrgMember> members = new ArrayList<V3xOrgMember>();
        Long accountId = null;
        for (Long s : ids) {
            V3xOrgMember m = orgManager.getMemberById(s);
            accountId = m.getOrgAccountId();
            m.setEnabled(false);
            m.setIsAssigned(false);
            //清空自己的汇报人
            m.setReporter(null);
            members.add(m);
        }
        //清空把调出人员设置为汇报人的汇报人信息。
        List<OrgMember> reportMember = orgDao.getAllMembersByReportToMember(ids);
        if(null!=reportMember){
        	for(OrgMember m : reportMember){
        		m.setExtAttr37(-1L);
        	}
        	orgDao.update(reportMember);
        }
        
        OrganizationMessage returnMessage = orgManagerDirect.updateMembers(members);
        Map result = OrgHelper.getBusinessExceptionMessage(returnMessage);
        if("false".equals(result.get(OrganizationMessage.MessageStatus.SUCCESS.name()))){
        	return result;
        }
        
        //OrgHelper.throwBusinessExceptionTools(returnMessage);
        //ldap
        if (LdapUtils.isLdapEnabled()) {
            try {
                organizationLdapEvent.deleteAllBinding(orgManagerDirect, members);
            } catch (Exception e) {
                logger.error("LDAP_AD 人员调出删除LDAP/AD绑定不成功！", e);
            }
        }

        //日志信息
        List<String[]> appLogs = new ArrayList<String[]>();
        User user = AppContext.getCurrentUser();
        V3xOrgAccount account = orgManager.getAccountById(accountId);
        for (V3xOrgMember memberLog : members) {
            String[] appLog = new String[3];
            appLog[2] = user.getName();
            appLog[0] = memberLog.getName();
            appLog[1] = account.getName();
            appLogs.add(appLog);
        }
        appLogManager.insertLogs4Account(user, user.isGroupAdmin() ? members.get(0).getOrgAccountId() : AppContext.currentAccountId(), AppLogAction.Organization_MoveMember, appLogs);
        return true;
    }


    @Override
    @CheckRoleAccess(roleTypes={Role_NAME.GroupAdmin,Role_NAME.AccountAdministrator,Role_NAME.HrAdmin,Role_NAME.DepAdmin})
    public HashMap<String,String> checkMember4DeptRole(String memberIds) throws BusinessException {
        HashMap<String,String> map = new HashMap<String, String>();
        StringBuilder deptName = new StringBuilder();
        StringBuilder deptIds = new StringBuilder();
        //OA-27264  修改成UniqueList为了避免既是部门主管又是部门管理员，会显示重复
        List<V3xOrgMember> list4Names = new UniqueList<V3xOrgMember>();

        String[] mIds = memberIds.split(",");
        for (String mId : mIds) {
            Long memberId = Long.valueOf(mId.trim());
            V3xOrgMember member = orgManager.getMemberById(memberId);
            List<MemberRole> memberRoles = orgManager.getMemberRoles(memberId, orgManager.getMemberById(memberId)
                    .getOrgAccountId());
            for (int i = 0; i < memberRoles.size(); i++) {
                if (Role_NAME.DepAdmin.name().equals(memberRoles.get(i).getRole().getCode())
                        || Role_NAME.DepManager.name().equals(memberRoles.get(i).getRole().getCode())) {
                    list4Names.add(member);
                    continue;
                }
            }
        }

        for (V3xOrgMember o : list4Names) {
            deptName.append(o.getName() + " ");
            deptIds.append(o.getId()).append(",");
        }
        map.put("deptName", deptName.toString());
        map.put("deptIds", deptIds.toString());
        return map;
    }

    @Override
    @CheckRoleAccess(roleTypes={Role_NAME.GroupAdmin,Role_NAME.AccountAdministrator,Role_NAME.HrAdmin})
    public FlipInfo showExtMember(FlipInfo fi, Map params) throws BusinessException {
        /********过滤和条件搜索*******/
        Boolean enabled = null;
        String condition = String.valueOf(params.get("condition"));
        Object value = params.get("value")==null?"":params.get("value");
        if ("state".equals(condition)) {//在职/离职过滤
            value = "1".equals(String.valueOf(params.get("value"))) ? Integer.valueOf(1) : Integer.valueOf(2);
        }
        //选人界面过来的查询条件和值
        if("orgDepartmentId".equals(condition)) {
            String str = String.valueOf(params.get("value")).replace("]", "").replace("[", "").replace("\"", "");
            String[] strs = str.trim().split(",");
            if(strs.length==0) {//Fix BUG OA-3448 查询条件选人界面，不选直接查询结果数组越界
                condition = null;
                value = null;
            } else {
                String[] strs2 = strs[1].split("[|]");
                value = Long.valueOf(strs2[1].trim());
            }
        }
        if ("enable".equals(condition)) {//启用/停用查询
            value = "true".equals(String.valueOf(params.get("value"))) ? true : false;
        }
        /********************/

        Long accountId = Long.parseLong(params.get("accountId").toString());
        List<OrgMember> members = orgDao.getAllMemberPOByAccountId(accountId, null, false, enabled, condition, value, null);
        List<V3xOrgMember> extMembers = (List<V3xOrgMember>) OrgHelper.listPoTolistBo(members);
        List<WebV3xOrgMember> result = new ArrayList<WebV3xOrgMember>(extMembers.size());
        for (V3xOrgMember bo : extMembers) {
        	if(!bo.isV5External()){
        		continue;
        	}
            WebV3xOrgMember o = new WebV3xOrgMember();
            o.setAccountName(orgManager.getAccountById(bo.getOrgAccountId()).getName());
            o.setDepartmentName((orgManager.getDepartmentById(bo.getOrgDepartmentId())) == null ? "--" : orgManager
                    .getDepartmentById(bo.getOrgDepartmentId()).getName());
            o.setCode(bo.getCode());
            o.setSortId(bo.getSortId());
            o.setName(bo.getName());
            o.setLoginName(bo.getLoginName());
            o.setV3xOrgMember(bo);
            o.setId(bo.getId());
            this.showLdapLoginName(bo, o);
            result.add(o);
        }
        
        // 工作范围
        DBAgent.memoryPaging(result, fi);
        
        List<WebV3xOrgMember> list = fi.getData();
        for(WebV3xOrgMember o : list){
            StringBuilder workscopeStr = new StringBuilder();
            List<V3xOrgEntity> extWorks = orgManager.getExternalMemberWorkScope(o.getId(), false);
            for (int i=0;i<extWorks.size();i++) {
                workscopeStr.append(extWorks.get(i).getName());
                if(i!=extWorks.size()-1) {
                    workscopeStr.append("、");
                }
            }
            o.setWorkscopeName(workscopeStr.toString());
        }
        return fi;
    }

    @Override
    @CheckRoleAccess(roleTypes={Role_NAME.DepAdmin})
    public FlipInfo show4DeptAdmin(FlipInfo fi, Map params) throws BusinessException {
        User currentUser = AppContext.getCurrentUser();
        Long accountId = Long.parseLong(params.get("accountId").toString());
        List<V3xOrgDepartment> depts = orgManager.getDeptsByAdmin(currentUser.getId(),accountId);
        List<Long> deptIds = new UniqueList<Long>();
        for (V3xOrgDepartment bo : depts) {
            deptIds.add(bo.getId());
            //父部门管理员可以管理子部门的人员
            List<V3xOrgDepartment> childs = orgManager.getChildDepartments(bo.getId(), false);
            for (V3xOrgDepartment child : childs) {
                deptIds.add(child.getId());
            }
        }

        /********过滤和条件搜索*******/
        Map queryParams = new HashMap<String, Object>();
        Long secondPostId = null;
        Boolean enabled = null;
        String workLocal = null;
        String condition = String.valueOf(params.get("condition"));
        Object value = params.get("value")==null?"":params.get("value");
        if ("state".equals(condition)) {//在职/离职过滤
            value = "1".equals(String.valueOf(params.get("value"))) ? Integer.valueOf(1) : Integer.valueOf(2);
            queryParams.put("state", value);
            params.remove("enabled");//OA-59386
        }
        //选人界面过来的查询条件和值
        if("orgPostId".equals(condition)
                || "orgLevelId".equals(condition)) {
            String str = String.valueOf(params.get("value")).replace("]", "").replace("[", "").replace("\"", "");
            String[] strs = str.trim().split(",");
            if(strs.length==0) {//Fix BUG OA-3448 查询条件选人界面，不选直接查询结果数组越界
                value = null;
            } else {
                String[] strs2 = strs[1].split("[|]");
                value = Long.valueOf(strs2[1].trim());
            }
            enabled = true;
            queryParams.put(condition, value);
        }
        if("name".equals(condition)) {
            queryParams.put("name", value);
        }
        if("loginName".equals(condition)) {
            queryParams.put("loginName", value);
        }
        if (params.containsKey("state") && Strings.isNotBlank(String.valueOf(params.get("state")))) {
            queryParams.put("state", Integer.parseInt(String.valueOf(params.get("state"))));
        }
        if (params.containsKey("enabled") && Strings.isNotBlank(String.valueOf(params.get("enabled")))) {
            enabled = (Boolean) params.get("enabled");
        }
        if (params.containsKey("orgDepartmentId") && Strings.isNotBlank(String.valueOf(params.get("orgDepartmentId")))) {
            queryParams.put("orgDepartmentId", Long.parseLong(String.valueOf(params.get("orgDepartmentId"))));
        }
        if (params.containsKey("enabled")) {
            enabled = (Boolean) params.get("enabled");
            queryParams.put("enable", enabled);
        }
        if ("enable".equals(condition)) {//启用/停用查询
            value = "true".equals(String.valueOf(params.get("value"))) ? true : false;
            queryParams.put("enable", value);
        }
        if("search_workLocalId".equals(condition)) {
            String str = (String)params.get("value");
            if(Strings.isNotBlank(str)) {
            	workLocal= enumManagerNew.parseToIds(str,OrgConstants.WORKLOCAL_ID);
            	queryParams.put("workLocal", workLocal + "%");
            }
        }
        if("code".equals(condition)) {
        	queryParams.put("code", value);
        }

        if(null != enabled && !enabled) queryParams.remove("state");//OA-58548
        orgDao.getAllMemberPOByDepartmentId(deptIds, null, enabled, queryParams, fi);
        /********************/

        return this.dealResult(fi);
    }

    /**
     * 将结果处理内部使用方法
     * @param fi
     * @return
     * @throws BusinessException
     */
    private FlipInfo dealResult(FlipInfo fi) throws BusinessException {
        List<OrgMember> members = fi.getData();
        List<V3xOrgMember> memberBOs = (List<V3xOrgMember>) OrgHelper.listPoTolistBo(members);
        List<WebV3xOrgMember> result = new ArrayList<WebV3xOrgMember>(memberBOs.size());
        String noPost = ResourceUtil.getString("org.member.noPost");
        for (V3xOrgMember bo : memberBOs) {
            WebV3xOrgMember o = new WebV3xOrgMember();
            o.setAccountName(orgManager.getAccountById(bo.getOrgAccountId()).getName());
            o.setDepartmentName((orgManager.getDepartmentById(bo.getOrgDepartmentId()) == null)?noPost:OrgHelper.showDepartmentFullPath(bo.getOrgDepartmentId()));
            o.setPostName((orgManager.getPostById(bo.getOrgPostId()) == null)?noPost:orgManager.getPostById(bo.getOrgPostId()).getName());
            o.setLevelName((orgManager.getLevelById(bo.getOrgLevelId()))==null?noPost:orgManager.getLevelById(bo.getOrgLevelId()).getName());
            o.setCode(bo.getCode());
            o.setSortId(bo.getSortId());
            o.setName(bo.getName());
            o.setLoginName(bo.getLoginName());
            o.setV3xOrgMember(bo);
            o.setId(bo.getId());
            o.setTypeName(bo.getType()==null?"":bo.getType().toString());
            o.setStateName(bo.getState()==null?"":bo.getState().toString());
            //ldap
            this.showLdapLoginName(bo, o);
            result.add(o);
        }
        fi.setData(result);
        return fi;
    }

    @Override
    public Set<Long> canDelRoles(Long accountId) throws BusinessException {
        User user = AppContext.getCurrentUser();
        boolean isShowRoleByCategory = true;
        if (orgManager.isAdministratorById(user.getId(), accountId) || user.isGroupAdmin()) {
            isShowRoleByCategory = false;
        } else if (orgManager.isHRAdmin() || orgManager.isDepartmentAdmin()) {
            isShowRoleByCategory = true;
        }
        Set<Long> result = new HashSet<Long>();
        //单位角色列表
        List<V3xOrgRole> allRoles = orgManager.getAllRoles(accountId);
        //过滤插件判断的角色列表
        List<V3xOrgRole> disRoleList = orgManager.getPlugDisableRole(accountId);
        for (V3xOrgRole r1 : disRoleList) {
            for (int i = 0; i < allRoles.size(); i++) {
                V3xOrgRole role = allRoles.get(i);
                if (role.getCode().trim().equals(r1.getCode().trim())) {
                    allRoles.remove(i--);
                }
            }
        }

        for (V3xOrgRole role : allRoles) {
            if (Role_NAME.DepLeader.name().equals(role.getCode())) {
                continue;
            }
            //OA-27264 人员管理角色列表只显示单位和部门角色 && 允许前台勾选的角色
            if ((OrgConstants.ROLE_BOND.ACCOUNT.ordinal() == role.getBond()
                    || OrgConstants.ROLE_BOND.DEPARTMENT.ordinal() == role.getBond())) {
                if (isShowRoleByCategory) {
                    if (!"0".equals(role.getCategory().trim())) {
                        result.add(role.getId());
                    }
                } else {
                    result.add(role.getId());
                }
            }

        }
        return result;
    }

    @Override
    @CheckRoleAccess(roleTypes={Role_NAME.GroupAdmin,Role_NAME.AccountAdministrator,Role_NAME.HrAdmin,Role_NAME.DepAdmin})
    public FlipInfo findRolesWithoutAdmin(FlipInfo fi, Map param) throws BusinessException {
        User user = AppContext.getCurrentUser();
        Long accountId = Long.parseLong(param.get("orgAccountId").toString());
        boolean isShowRoleByCategory = true;
        if(orgManager.isAdministratorById(user.getId(), accountId)
                || orgManager.isGroupAdminById(user.getId())) {//增加集团管理员，为了兼职管理
            isShowRoleByCategory = false;
        } else if(orgManager.isHRAdmin() || orgManager.isDepartmentAdmin()) {
            isShowRoleByCategory = true;
        }
        List<V3xOrgRole> result = new UniqueList<V3xOrgRole>();
        //单位角色列表
        List<V3xOrgRole> allRoles = orgManager.getAllRoles(accountId);
        //过滤插件判断的角色列表
        List<V3xOrgRole> disRoleList = orgManager.getPlugDisableRole(accountId);
        Set<String> disRoleCodeSet = new HashSet<String>();
        for (V3xOrgRole r : disRoleList) {
        	disRoleCodeSet.add(r.getCode());
        }

        for (V3xOrgRole role : allRoles) {
        	if(disRoleCodeSet.contains(role.getCode())){
        		continue;
        	}
            if(Role_NAME.DepLeader.name().equals(role.getCode())) {
                continue;
            }
			if (!AppContext.hasPlugin("edoc")) { // 当前版本下是否需要显示公文相关人员

				if (role.getCode().equals(Role_NAME.Accountexchange.name())
						|| role.getCode().equals(Role_NAME.SendEdoc.name())
						|| role.getCode().equals(Role_NAME.SignEdoc.name())
						|| role.getCode().equals(Role_NAME.RecEdoc.name())
						|| role.getCode().equals(Role_NAME.EdocModfiy.name())
						|| role.getCode().equals(Role_NAME.Departmentexchange.name())) {
					continue;
				}
			}
            //OA-27264 人员管理角色列表只显示单位和部门角色 && 允许前台勾选的角色
            if ((OrgConstants.ROLE_BOND.ACCOUNT.ordinal() == role.getBond()
                    || OrgConstants.ROLE_BOND.DEPARTMENT.ordinal() == role.getBond())) {
                if (isShowRoleByCategory) {
                    if (!"0".equals(role.getCategory().trim())) {
                        if (null != param.get("bond") && Strings.isNotBlank(param.get("bond").toString())) {
                            if (role.getBond() == Integer.parseInt(param.get("bond").toString())) {
                                result.add(role);
                            }
                        } else {
                            result.add(role);
                        }
                    }
                } else {
                    if (null != param.get("bond") && Strings.isNotBlank(param.get("bond").toString())) {
                        if (role.getBond() == Integer.parseInt(param.get("bond").toString())) {
                            result.add(role);
                        }
                    } else {
                        result.add(role);
                    }
                }
            }

        }
        fi.setData(result);
        return fi;
    }

    @Override
    public FlipInfo findRoles4ExtMember(FlipInfo fi, Map param) throws BusinessException {
        Long accountId = Long.parseLong(param.get("orgAccountId").toString());
        User user = AppContext.getCurrentUser();
        List<V3xOrgRole> result = new UniqueList<V3xOrgRole>();

        boolean isShowRoleByCategory = true;
        if(orgManager.isAdministratorById(user.getId(), accountId)) {
            isShowRoleByCategory = false;
        } else if(orgManager.isHRAdmin() || orgManager.isDepartmentAdmin()) {
            isShowRoleByCategory = true;
        }
        //单位角色列表
        List<V3xOrgRole> allRoles = orgManager.getAllRoles(accountId);
        //过滤插件判断的角色列表
        List<V3xOrgRole> disRoleList = orgManager.getPlugDisableRole(accountId);
        Set<String> disRoleCodeSet = new HashSet<String>();
        for (V3xOrgRole r : disRoleList) {
        	disRoleCodeSet.add(r.getCode());
        }
        
        for (V3xOrgRole aRole : allRoles) {
        	if(!disRoleCodeSet.contains(aRole.getCode())){
                //自定义角色 && 不是部门角色 && 允许前台勾选的角色
                if ((aRole.getType() == 3 && OrgConstants.ROLE_BOND.DEPARTMENT.ordinal() != aRole.getBond())
                        // OA-69296 建立外部人员的时候可以选择到默认的普通人员权限了，bug，外部人员只能选择外部人员和自定义的角色
                        // || OrgConstants.Role_NAME.GeneralStaff.name().equals(aRole.getCode())
                        || Role_NAME.ExternalStaff.name().equals(aRole.getCode())) {
                    if(isShowRoleByCategory) {
                        if(!"0".equals(aRole.getCategory().trim())) {
                            result.add(aRole);
                        } else {
                            continue;
                        }
                    } else {
                        result.add(aRole);
                    }
                }
        	}
        }
        fi.setData(result);

        return fi;
    }

    private Set<Long> canDelRole4Outer(Long accountId) throws BusinessException {
        User user = AppContext.getCurrentUser();
        Set<Long> result = new HashSet<Long>();

        boolean isShowRoleByCategory = true;
        if(orgManager.isAdministratorById(user.getId(), accountId)) {
            isShowRoleByCategory = false;
        } else if(orgManager.isHRAdmin() || orgManager.isDepartmentAdmin()) {
            isShowRoleByCategory = true;
        }

        //单位角色列表
        List<V3xOrgRole> allRoles = orgManager.getAllRoles(accountId);
        //过滤插件判断的角色列表
        List<V3xOrgRole> disRoleList = orgManager.getPlugDisableRole(accountId);
        for (V3xOrgRole r1 : disRoleList) {
            for (int i = 0; i < allRoles.size(); i++) {
                V3xOrgRole role = allRoles.get(i);
                if(role.getCode().trim().equals(r1.getCode().trim())) {
                    allRoles.remove(i--);
                }
            }
        }
        for (V3xOrgRole aRole : allRoles) {
            //自定义角色 && 不是部门角色 && 允许前台勾选的角色
            if ((aRole.getType() == 3 && OrgConstants.ROLE_BOND.DEPARTMENT.ordinal() != aRole.getBond())
                    || Role_NAME.GeneralStaff.name().equals(aRole.getCode())
                    || Role_NAME.ExternalStaff.name().equals(aRole.getCode())) {
                if(isShowRoleByCategory) {
                    if(!"0".equals(aRole.getCategory().trim())) {
                        result.add(aRole.getId());
                    } else {
                        continue;
                    }
                } else {
                    result.add(aRole.getId());
                }
            }
        }
        return result;
    }


    /**
     * 组装LDAP/AD帐号
     * @param member
     * @param webMember
     */
    private void showLdapLoginName(V3xOrgMember member, WebV3xOrgMember webMember) {
        if (LdapUtils.isLdapEnabled()) {
            try {
                webMember.setLdapLoginName(organizationLdapEvent.getLdapAdLoginName(member.getLoginName()));
            } catch (Exception e) {
                logger.error("ldap/ad 显示ldap帐号异常！", e);
            }

        }
    }

    /**
     * 绑定ldap登录名
     * @param oldMember
     * @param map
     * @throws BusinessException
     */
    private void bindLdap(V3xOrgMember oldMember, Map map) throws BusinessException {
    	List<V3xOrgMember> memberList = null;
        if (LdapUtils.isLdapEnabled()) {
            String ldapUserCodes = String.valueOf(map.get("ldapUserCodes"));
            //现在如果开启了ad功能，也并不一定要进行绑定。如果绑定，登录时按照ad账号登录，如果没有绑定按照oa账号登录。
            if(Strings.isBlank(ldapUserCodes)){
            	memberList = new ArrayList<V3xOrgMember>(1);
                memberList.add(oldMember);
                organizationLdapEvent.deleteAllBinding(orgManagerDirect, memberList);
            	return ;
            }
            User user = AppContext.getCurrentUser();
            String loginName = map.get("loginName").toString();
            String password = map.get("password").toString();
            String oldLoginName = null;
           // String oldPassword = null;
            try {
                oldLoginName = principalManager.getLoginNameByMemberId(Long.valueOf(map.get("id").toString()));
                //oldPassword = principalManager.getPassword(Long.valueOf(map.get("id").toString()));
            } catch (Exception e) {
                logger.error("获取登录名异常", e);
                return;
            }
            //boolean isLoginNameModifyed = Boolean.valueOf(map.get("isLoginNameModifyed").toString());
            boolean isLoginNameModifyed = false;
            V3xOrgPrincipal oldPrincipal = oldMember.getV3xOrgPrincipal();
            if(!loginName.equals(oldPrincipal.getLoginName())) {
                isLoginNameModifyed = true;
            }
            try {
                V3xOrgMember memberLdap = new V3xOrgMember();
                BeanUtils.copyProperties(memberLdap, oldMember);
                memberLdap.setEnabled(Boolean.valueOf(map.get("enabled").toString()));
                if (isLoginNameModifyed) {
                    memberList = new ArrayList<V3xOrgMember>(1);
                    V3xOrgPrincipal p = new V3xOrgPrincipal(oldMember.getId(), oldLoginName, password);
                    memberLdap.setV3xOrgPrincipal(p);
                    memberList.add(memberLdap);
                    organizationLdapEvent.deleteAllBinding(orgManagerDirect, memberList);
                } else {
                    V3xOrgPrincipal p = new V3xOrgPrincipal(oldMember.getId(), oldLoginName, password);
                    memberLdap.setV3xOrgPrincipal(p);
                }
                if (organizationLdapEvent.getLdapAdExUnitCode(map.get("loginName").toString()).equals(ldapUserCodes)
                        && !isLoginNameModifyed && !LDAPConfig.getInstance().isDisabledModifyPassWord()
                        && !Strings.isEmpty(password) && !password.equals(OrgConstants.DEFAULT_INTERNAL_PASSWORD)) {
                	LDAPConfig config = LDAPConfig.getInstance();
                    String type = SystemProperties.getInstance().getProperty("ldap.ad.enabled", "ldap");
                    if(("ad".equals(type) && config.getIsEnableLdap()) || "ldap".equals(type)){
                    	organizationLdapEvent.changePassword(oldMember, memberLdap);
                        //审计日志
                        appLogManager.insertLog4Account(user, user.isGroupAdmin() ? oldMember.getOrgAccountId() : AppContext.currentAccountId(), AppLogAction.LDAP_PassWord_Update, memberLdap.getName(), ldapUserCodes);
                    }
                } else 
//                	if (!organizationLdapEvent.getLdapAdExUnitCode(memberLdap.getLoginName()).equals(ldapUserCodes)) 
                	{
                    memberList = new ArrayList<V3xOrgMember>(1);
                    V3xOrgPrincipal p = new V3xOrgPrincipal(oldMember.getId(), loginName, password);
                    memberLdap.setV3xOrgPrincipal(p);
                    memberList.add(memberLdap);
                	organizationLdapEvent.deleteAllBinding(orgManagerDirect, memberList);
                    String[] errorResult = organizationLdapEvent.addMember(memberLdap, ldapUserCodes);
                    if (errorResult != null && errorResult.length > 0) {
                        StringBuilder jsContent = new StringBuilder();
                        for (int i = 0; i < errorResult.length; i++) {
                            jsContent.append(errorResult[i]).append("\n");
                        }
                        logger.error(jsContent.toString());
                        //TODO 这个提示无用，这里抛出异常会导致无法绑定
                        //throw new BusinessException(jsContent.toString());
                    }
                    //审计日志
                    appLogManager.insertLog4Account(user, user.isGroupAdmin() ? oldMember.getOrgAccountId() : AppContext.currentAccountId(), AppLogAction.LDAP_Account_Bing_Create, memberLdap.getName(),
                            ldapUserCodes);
                    if (!LDAPConfig.getInstance().isDisabledModifyPassWord() && !Strings.isEmpty(password)
                            && !password.equals(OrgConstants.DEFAULT_INTERNAL_PASSWORD)) {
                        LDAPConfig config = LDAPConfig.getInstance();
                        String type = config.getSys().getProperty("ldap.ad.enabled");
                        if(("ad".equals(type) && config.getIsEnableLdap()) || "ldap".equals(type)){
                        	organizationLdapEvent.changePassword(oldMember, memberLdap);
                            //审计日志
                            appLogManager.insertLog4Account(user, user.isGroupAdmin() ? oldMember.getOrgAccountId() : AppContext.currentAccountId(), AppLogAction.LDAP_PassWord_Update, memberLdap.getName(),
                                    ldapUserCodes);
                        }
                    }
                }
            } catch (BusinessException b) {
                throw b;
            } catch (Exception e) {
                logger.error("ldap/ad 添加人员绑定不成功！", e);
            }
        } else {
            return;
        }
    }

    /**
     * 新建人员时创建条目
     * @param newMember
     * @param map
     * @throws BusinessException
     */
    private void newMemberBingLdap(V3xOrgMember newMember, Map map) throws BusinessException {
        if (LdapUtils.isLdapEnabled()) {
            User user = AppContext.getCurrentUser();
            String ldapEntry = String.valueOf(map.get("ldapUserCodes"));
            String selectOU = String.valueOf(map.get("selectOU"));
            try {
                if (Strings.isBlank(ldapEntry)) {
                	if(Strings.isNotBlank(selectOU)){
                		organizationLdapEvent.newAddLdapPerson(newMember, selectOU);
                		appLogManager.insertLog4Account(user, user.isGroupAdmin() ? newMember.getOrgAccountId() : AppContext.currentAccountId(), AppLogAction.LDAP_Account_Create, newMember.getName(), selectOU);
                	}
                } else {
                    String[] errorResult = organizationLdapEvent.addMember(newMember, ldapEntry);
                    if (errorResult != null && errorResult.length > 0) {
                        String jsContent = "";
                        for (int i = 0; i < errorResult.length; i++) {
                            jsContent += errorResult[i] + "\\n";
                        }
                        throw new BusinessException(jsContent);
                    }
                    appLogManager
                            .insertLog(user, AppLogAction.LDAP_Account_Bing_Create, newMember.getName(), ldapEntry);
                }
            } catch (Exception e) {
                logger.error("ldap/ad 添加人员绑定不成功！", e);
                if(null != e.getMessage()) {
                    if (e.getMessage().indexOf("error code 19") != -1) {
                        throw new BusinessException(ResourceUtil.getString("ldap.log.error.tip"));
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @CheckRoleAccess(roleTypes={Role_NAME.GroupAdmin,Role_NAME.AccountAdministrator,Role_NAME.HrAdmin,Role_NAME.DepAdmin})
    public FlipInfo showMemberAllRoles(FlipInfo fi, Map param) throws BusinessException {
        if(null == param.get("memberId")) {
            throw new BusinessException("传入的人员ID为空！");
        }
        Long memberId = Long.valueOf(String.valueOf(param.get("memberId")));
        V3xOrgMember member = orgManager.getMemberById(memberId);
        List<MemberRole> memberAccRoles = orgManager.getMemberRoles(memberId, member.getOrgAccountId());
        List<MemberRole> memberGroupRoles = orgManager.getMemberRoles(memberId, OrgConstants.GROUPID);
        List<MemberRole> memberRoles = new UniqueList<MemberRole>();
        memberRoles.addAll(memberAccRoles);
        memberRoles.addAll(memberGroupRoles);
        List<HashMap> result = new ArrayList<HashMap>(memberRoles.size());
        for (MemberRole m : memberRoles) {
            HashMap map = new HashMap();
            map.put("showName", m.getRole().getShowName());
            if(OrgConstants.ROLE_BOND.GROUP.ordinal() == m.getRole().getBond()) {
                map.put("bond", ResourceUtil.getString("role.group"));
            } else if(OrgConstants.ROLE_BOND.ACCOUNT.ordinal() == m.getRole().getBond()){
                map.put("bond", ResourceUtil.getString("role.unit"));
            } else if(OrgConstants.ROLE_BOND.DEPARTMENT.ordinal() == m.getRole().getBond()) {
                map.put("bond", ResourceUtil.getString("role.dept"));
            } else {
                continue;
            }
            map.put("source", orgManager.getEntityOnlyById(m.getMemberId())==null?"":orgManager.getEntityOnlyById(m.getMemberId()).getName());
            result.add(map);
        }
        fi.setData(result);
        return fi;
    }

    public boolean checkNoRoles(String entityIds) throws BusinessException {
        List<Long> sourceId = new UniqueList<Long>();
        if (Strings.isNotBlank(entityIds)) {
            for (String entityId : entityIds.split(",")) {
                if (Strings.isNotBlank(entityId)) {
                    if(entityId.contains("Department_Post")) {//追加副岗的判断Department_Post|123456_654321
                        sourceId.add(Long.valueOf(entityId.split("[|]")[1].split("_")[1]));
                    } else {
                        sourceId.add(Long.valueOf(entityId.trim()));
                    }
                }
            }
        }
        List<V3xOrgRelationship> rels = orgCache.getV3xOrgRelationship(OrgConstants.RelationshipType.Member_Role, sourceId, null, null);
        //这里要对角色进行区分，例如DeptSpace等bond=4这个角色不算实体已经拥有的角色
        for (V3xOrgRelationship r : rels) {
            V3xOrgRole tempRole = orgManager.getRoleById(r.getObjective1Id());
            if(null != tempRole
                    && OrgConstants.ROLE_BOND.NULL2.ordinal() != tempRole.getBond()
                    && OrgConstants.ROLE_BOND.BUSINESS.ordinal() != tempRole.getBond()
                    && OrgConstants.ROLE_BOND.SSO.ordinal() != tempRole.getBond()
                    && OrgConstants.ROLE_BOND.NULL1.ordinal() != tempRole.getBond()) {
                return false;
            }
        }
        return true;
    }
    /**
     * 修改枚举引用状态
     * @param workLocal
     */
    private void refEnum(String workLocal){
    	String[] localIds = workLocal.split(",");
        int index_id =0;
		for (String id : localIds) {
			try {
				if (index_id == 0) {
					if(Strings.isBlank(id)){
						return ;
					}
					enumManagerNew.updateEnumItemRef(Long.valueOf(id));
				} else {
					enumManagerNew.updateEnumItemRef(Long.valueOf(id));
				}
			} catch (Exception e) {
				logger.error("parse 枚举引用状态错误：", e);
			}
			index_id++;
		}
    }

	public void setPrivilegeMenuManager(PrivilegeMenuManager privilegeMenuManager) {
		this.privilegeMenuManager = privilegeMenuManager;
	}

	/**
	 * 解压压缩文件，并关联人员信息，返回导出报告数据
	 */
	public List<ResultObject> uploadMemberPicAttachment(String zipFileName, V3XFile v3xFile, Long accountId,
			Boolean override) throws BusinessException {
		String fileUrl = String.valueOf(v3xFile.getId());
		String rootPath = fileManager.getFolder(v3xFile.getCreateDate(), false);
		String filePath = rootPath + File.separator + fileUrl;
		//压缩文件上传到服务器后的存储文件形式
		File disFile = new File(filePath);
		File directory = new File(rootPath + File.separator + "myTemp" + File.separator);

		List<ResultObject> resultList = new ArrayList<ResultObject>();
		try {// 解压文件
			//获取原始压缩文件
			File zipFile = fileManager.getFile(v3xFile.getId(), v3xFile.getCreateDate());
			logger.info("------------系统默认编码格式："+java.nio.charset.Charset.defaultCharset().toString());
			List<File> files = ZipUtil.unzipBycode(zipFile, directory,"GBK");
			
			if (files == null || Strings.isEmpty(files)) {// 如果文件为空
				ResultObject ro = newResultObject(zipFileName, ResourceUtil.getString("import.report.fail"),
						ResourceUtil.getString("member.photo.unzip.error"));
				resultList.add(ro);
				return resultList;
			}
			// 存放已更新的登陆名
			List<String> loginNames = new ArrayList<String>();
			List<Attachment> atts = new ArrayList<Attachment>();
			// 只查询根据单位id的已启用的内部人员，包括在职/不在职
			List<V3xOrgMember> allMembersByAccountId = orgManager.getAllMembersByAccountId(accountId, null, true, true,null, null, null);
			// 存放登录名和人员的对应关系map
			Map<String, V3xOrgMember> loginNameToMemberMap = new HashMap<String, V3xOrgMember>();
			if (allMembersByAccountId != null && Strings.isNotEmpty(allMembersByAccountId)) {
				for (V3xOrgMember member : allMembersByAccountId) {
					loginNameToMemberMap.put(member.getLoginName(), member);
				}
			}
			// 要更新的人员列表
			List<V3xOrgMember> toUpdateMembers = new ArrayList<V3xOrgMember>();
			for (File f : files) {
				logger.info("-----------fileName: " + f.getName());
				// 递归读取文件
				readFile(override, f, rootPath, v3xFile.getCreateDate(), loginNames, atts, loginNameToMemberMap,
						toUpdateMembers, resultList);
			}
			//OA-146973 人员管理--批量上传人员照片，提示为文件解压失败，但实际上传人员照片成功了。
			//调整了附件表和人员表批量更新顺序，（原操作顺序显示事务出错）
			if (Strings.isNotEmpty(atts)) {// 存储附件列表
				attachmentManager.create(atts);
			}
			if (!toUpdateMembers.isEmpty()) {
				orgManagerDirect.updateMembers(toUpdateMembers);
			}

		} catch (Exception e) {
			logger.error("文件转换失败：", e);
			ResultObject ro = newResultObject(zipFileName, ResourceUtil.getString("import.report.fail"),
					ResourceUtil.getString("member.photo.unzip.error"));
			resultList.add(ro);
		}finally{
			// 删除临时文件夹
			deleteFile(directory);
			// 删除压缩文件
			if (disFile.exists()) {
				disFile.delete();
			}
		}
		return resultList;
	}

	/**
	 * 递归删除临时文件
	 * 
	 * @param file
	 *            文件或文件夹
	 */
	private void deleteFile(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] ff = file.listFiles();
				for (File f : ff) {
					deleteFile(f);
				}
			}
			file.delete();
		}
	}
	/**
	 * 递归读取文件
	 * @param override 是否覆盖
	 * @param file		文件
	 * @param rootPath	文件存放路径
	 * @param createDate 文件创建时间
	 * @param loginNames 已存登陆名
	 * @param atts		  附件列表
	 * @param loginNameToMemberMap 登陆名与人员对应的map
	 * @param toUpdateMembers	将更新的人员列表
	 * @param resultList		结果列表
	 * @throws Exception
	 */
	private void readFile(Boolean override, File file, String rootPath, Date createDate, List<String> loginNames,
			List<Attachment> atts, Map<String, V3xOrgMember> loginNameToMemberMap, List<V3xOrgMember> toUpdateMembers,
			List<ResultObject> resultList) throws Exception {
		if (!file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			File[] fileArray = file.listFiles();
			for (File f : fileArray) {
				readFile(override, f, rootPath, createDate, loginNames, atts, loginNameToMemberMap, toUpdateMembers,
						resultList);
			}
		}
		List<String> allowExtensions = new ArrayList<String>(Arrays.asList("gif", "jpg", "jpeg", "png", "bmp"));
		String filePath = file.getCanonicalPath();
		//过滤mac系统压缩的文件解压后产生的多余文件
		//OA-150820人员管理批量导入人员照片时，导入异常中会有多余的照片数据，入图所示
		if(filePath.toLowerCase().indexOf("macosx") != -1){
			return;
		}
		String fileName= file.getName();
		if(Strings.isNotBlank(fileName)){
			//文件名称编码重新转换UTF-8
			byte[] ln = fileName.getBytes();
			fileName = new String(ln,"UTF-8");
		}
		// 扩展名
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
		long size = file.length();
		if (size >= 512000) {
			ResultObject ro = newResultObject(fileName, ResourceUtil.getString("import.report.fail"),
					ResourceUtil.getString("fileupload.exception.MaxSize", "500k"));
			resultList.add(ro);
			return;
		}
		if (!allowExtensions.contains(extension)) {
			ResultObject ro = newResultObject(fileName, ResourceUtil.getString("import.report.fail"),
					ResourceUtil.getString("fileupload.exception.UnallowedExtension", "gif,jpg,jpeg,png,bmp"));
			resultList.add(ro);
			return;
		}
		// 获取登陆名
		String loginName = fileName.substring(0, fileName.lastIndexOf("."));
		if (Strings.isNotBlank(loginName)) {// 如果不是空
			if (Strings.isNotEmpty(loginNames) && loginNames.contains(loginName)) {// 如果不为空并且已有同名的
				ResultObject ro = newResultObject(fileName, ResourceUtil.getString("import.report.fail"),
						ResourceUtil.getString("member.photo.upload.same.exist"));
				resultList.add(ro);
				return;
			}
			if (!loginNameToMemberMap.isEmpty() && loginNameToMemberMap.containsKey(loginName)) {// 若存在此人，则将其属性关联
				V3xOrgMember member = loginNameToMemberMap.get(loginName);
				// 如果不覆盖,判断是否原来有此属性, 如果原来已有此属性
				if (!override && null != member.getProperty("imageid") && !"".equals(member.getProperty("imageid"))) {
					ResultObject ro = newResultObject(fileName, ResourceUtil.getString("import.report.fail"),
							ResourceUtil.getString("fileupload.page.skip"));
					resultList.add(ro);
					return;
				}
				long fileId = UUIDLong.longUUID();
				File destF = new File(rootPath + File.separator + fileId);
				// 复制文件
				DataUtil.CopyFile(file, destF);
				String imageid = "/fileUpload.do?method=showRTE&fileId=" + fileId + "&createDate="
						+ Datetimes.formatDatetimeWithoutSecond(createDate) + "&type=image";
				member.setProperty("imageid", imageid);
				// 添加到人员更新列表
				toUpdateMembers.add(member);
				Attachment att = new Attachment();
				att.setType(Constants.ATTACHMENT_TYPE.FILE.ordinal());
				att.setFileUrl(fileId);
				att.setCategory(ApplicationCategoryEnum.organization.key());
				att.setFilename(fileName);
				att.setCreatedate(createDate);
				att.setSize(size);
				String mimeType = "image/" + extension;
				att.setMimeType(mimeType);
				atts.add(att);
				// 更新完之后将其加入已成功列表
				loginNames.add(loginName);
			} else {
				ResultObject ro = newResultObject(fileName, ResourceUtil.getString("import.report.fail"),
						ResourceUtil.getString("member.photo.upload.no.person"));
				resultList.add(ro);
			}
		} else {// 查不到对应的人员信息
			ResultObject ro = newResultObject(fileName, ResourceUtil.getString("import.report.fail"),
					ResourceUtil.getString("member.photo.upload.no.person"));
			resultList.add(ro);
		}

	}
	/**
	 * 私有新建结果类
	 * @param name
	 * @param success
	 * @param des
	 * @return
	 */
	private ResultObject newResultObject(String name, String success, String des) {
		ResultObject ro = new ResultObject();
		ro.setName(name);
		if (StringUtils.hasText(success))
			ro.setSuccess(success);
		if (StringUtils.hasText(des))
			ro.setDescription(des);
		return ro;
	}
}
