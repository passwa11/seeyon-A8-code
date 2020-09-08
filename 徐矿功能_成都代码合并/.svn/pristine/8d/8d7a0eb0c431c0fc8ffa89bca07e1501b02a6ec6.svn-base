package com.seeyon.v3x.addressbook.controller;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.addressbook.constants.AddressbookConstants;
import com.seeyon.apps.addressbook.manager.AddressBookCustomerFieldInfoManager;
import com.seeyon.apps.addressbook.manager.AddressBookManager;
import com.seeyon.apps.addressbook.po.AddressBook;
import com.seeyon.apps.addressbook.po.AddressBookMember;
import com.seeyon.apps.addressbook.po.AddressBookSet;
import com.seeyon.apps.addressbook.po.AddressBookTeam;
import com.seeyon.apps.addressbook.po.Csv;
import com.seeyon.apps.addressbook.po.VCard;
import com.seeyon.apps.addressbook.webmodel.WebSimpleV3xOrgMember;
import com.seeyon.apps.addressbook.webmodel.WebWithPropV3xOrgMember;
import com.seeyon.apps.peoplerelate.api.PeopleRelateApi;
import com.seeyon.apps.peoplerelate.bo.PeopleRelateBO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.dao.paginate.Pagination;
import com.seeyon.ctp.common.excel.DataCell;
import com.seeyon.ctp.common.excel.DataRecord;
import com.seeyon.ctp.common.excel.DataRow;
import com.seeyon.ctp.common.excel.FileToExcelManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.LocaleContext;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.metadata.bo.MetadataCategoryBO;
import com.seeyon.ctp.common.metadata.bo.MetadataColumnBO;
import com.seeyon.ctp.common.metadata.manager.MetadataCategoryManager;
import com.seeyon.ctp.common.metadata.manager.MetadataColumnManager;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.OrgConstants.TeamMemberType;
import com.seeyon.ctp.organization.bo.CompareSortEntity;
import com.seeyon.ctp.organization.bo.CompareUnitPath;
import com.seeyon.ctp.organization.bo.MemberHelper;
import com.seeyon.ctp.organization.bo.MemberPost;
import com.seeyon.ctp.organization.bo.OrgTypeIdBO;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgLevel;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgPost;
import com.seeyon.ctp.organization.bo.V3xOrgRelationship;
import com.seeyon.ctp.organization.bo.V3xOrgTeam;
import com.seeyon.ctp.organization.bo.V3xOrgUnit;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.inexportutil.DataUtil;
import com.seeyon.ctp.organization.manager.AccountManager;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.manager.OrgManagerDirect;
import com.seeyon.ctp.organization.manager.OuterWorkerAuthUtil;
import com.seeyon.ctp.organization.webmodel.WebV3xOrgAccount;
import com.seeyon.ctp.organization.webmodel.WebV3xOrgDepartment;
import com.seeyon.ctp.organization.webmodel.WebV3xOrgTeam;
import com.seeyon.ctp.portal.manager.PortalCacheManager;
import com.seeyon.ctp.privilege.manager.PrivilegeManager;
import com.seeyon.ctp.util.CommonTools;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ListSearchHelper;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UniqueList;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.common.web.util.ExportHelper;
import com.seeyon.v3x.hr.webmodel.WebStaffInfo;
import com.seeyon.v3x.mobile.message.manager.MobileMessageManager;

public class AddressBookController extends BaseController {

    private transient static final Log log                      = LogFactory.getLog(AddressBookController.class);

    private AccountManager             accountManager;
    private OrgManager                 orgManager;
    private OrgManagerDirect           orgManagerDirect;
    private AddressBookManager         addressBookManager;
    private FileToExcelManager         fileToExcelManager;
    private FileManager                fileManager;
    private PeopleRelateApi            peopleRelateApi;
    private String                     jsonView;
    private String                     clientAbortExceptionName = "ClientAbortException";

    private String                     contentTypeCharset       = "UTF-8";
    private com.seeyon.ctp.common.ctpenumnew.manager.EnumManager  enumManagerNew;
    private MobileMessageManager       mobileMessageManager;
    private AppLogManager appLogManager;
    private MetadataColumnManager metadataColumnManager;
    private MetadataCategoryManager metadataCategoryManager;
    private AddressBookCustomerFieldInfoManager addressBookCustomerFieldInfoManager;
    private PrivilegeManager  privilegeManager;
    private PortalCacheManager portalCacheManager;

    public void setEnumManagerNew(com.seeyon.ctp.common.ctpenumnew.manager.EnumManager enumManagerNew) {
		this.enumManagerNew = enumManagerNew;
	}
    
    public void setPeopleRelateApi(PeopleRelateApi peopleRelateApi) {
        this.peopleRelateApi = peopleRelateApi;
    }

    public void setMobileMessageManager(MobileMessageManager mobileMessageManager) {
        this.mobileMessageManager = mobileMessageManager;
    }

    public void setMetadataColumnManager(MetadataColumnManager metadataColumnManager) {
		this.metadataColumnManager = metadataColumnManager;
	}

	public void setMetadataCategoryManager(
			MetadataCategoryManager metadataCategoryManager) {
		this.metadataCategoryManager = metadataCategoryManager;
	}

    public void setPortalCacheManager(PortalCacheManager portalCacheManager) {
        this.portalCacheManager = portalCacheManager;
    }

    /**
     * 用户关闭下载窗口时候，有servlet容器抛出的异常
     * @param clientAbortExceptionName 类的simapleName，如<code>ClientAbortException</code>
     */
    public void setClientAbortExceptionName(String clientAbortExceptionName) {
        this.clientAbortExceptionName = clientAbortExceptionName;
    }

    public String getJsonView() {
        return jsonView;
    }

    public void setJsonView(String jsonView) {
        this.jsonView = jsonView;
    }

    public FileToExcelManager getFileToExcelManager() {
        return fileToExcelManager;
    }

    public void setFileToExcelManager(FileToExcelManager fileToExcelManager) {
        this.fileToExcelManager = fileToExcelManager;
    }

    public OrgManager getOrgManager() {
        return orgManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public AddressBookManager getAddressBookManager() {
        return addressBookManager;
    }

    public void setAddressBookManager(AddressBookManager addressBookManager) {
        this.addressBookManager = addressBookManager;
    }

    public AppLogManager getAppLogManager() {
        return appLogManager;
    }

    public void setAppLogManager(AppLogManager appLogManager) {
        this.appLogManager = appLogManager;
    }

	public void setAddressBookCustomerFieldInfoManager(
			AddressBookCustomerFieldInfoManager addressBookCustomerFieldInfoManager) {
		this.addressBookCustomerFieldInfoManager = addressBookCustomerFieldInfoManager;
	}

	public void setAccountManager(AccountManager accountManager) {
		this.accountManager = accountManager;
	}
	
	public PrivilegeManager getPrivilegeManager() {
		return privilegeManager;
	}

	public void setPrivilegeManager(PrivilegeManager privilegeManager) {
		this.privilegeManager = privilegeManager;
	}

	@Override
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {

        return null;
    }

    public ModelAndView selectPrivatedPeople(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView(jspView(2, "selectPrivatedPeople"));
        User user = AppContext.getCurrentUser();
        List<AddressBookMember> members = addressBookManager.getMembersByCreatorId(user.getId());
        mav.addObject("members", members);
        return mav;
    }

    /**
     * 根据addressbookType参数来判断是员工通讯录/外部联系人
     * 如果root==true,则不判断addressbookType，直接返回根目录+page
     * 如果root==false,则根据addressbookType参数来判断是员工通讯录/外部联系人,返回根目录+public/private+page
     * 
     * @param addressbookType
     *            通讯录类型
     * @param page
     *            显示页面名称
     * @param root
     *            表示是否根目录
     * @return 显示页面上一层路径(格式如下：addressbook/public/)
     */
    private String jspView(int addressbookType, String page, boolean root) {
        String path = "addressbook/home";
        if (root)
            path = "addressbook/" + page;
        else {
            if (1 == addressbookType || 3 == addressbookType || 4 == addressbookType) //员工通讯录
                path = "addressbook/public/" + page;
            else if (2 == addressbookType) //私人通讯录
                path = "addressbook/private/" + page;
        }
        return path;
    }

    /**
     * 页面跳转
     */
    private String myJspView(int addressbookType, String page,String showType) {
        String path = "";
      //员工通讯录
        if (1 == addressbookType) {
        	if("card".equals(showType)){
        		path = "addressbook/public/addressbookOrgStructure_card";
        	}else if("tree".equals(showType)){
                path = "addressbook/public/addressbookOrgStructure_tree";
            }else{
        		path = "addressbook/public/" + page;
        	}
        }
        else if (3 == addressbookType){
        	if("card".equals(showType)){
        		path = "addressbook/public/addressbookOrgStructure_card";
        	}else{
        		path = "addressbook/public/listSysMember";
        	}
        }
        else if (4 == addressbookType){
        	if("card".equals(showType)){
        		path = "addressbook/public/addressbookOrgStructure_card";
        	}else{
        		path = "addressbook/public/listOwnGroup";
        	}
        }//私人通讯录
        else if (2 == addressbookType) {
        	if("card".equals(showType)){
        		path = "addressbook/private/private_card";
        	}else{
        		path = "addressbook/private/listMembers";
        	}
        }

        return path;
    }

    private String jspView(int addressbookType, String page) {
        return jspView(addressbookType, page, false);
    }

    private String jspView(String page) {
        return jspView(1, page, true);
    }

    /**
     * 获取人的附加信息
     */
    private List<WebWithPropV3xOrgMember> parse(V3xOrgMember member, Long accountId, Long depId, AddressBookSet addressBookSet, List<MetadataColumnBO> metadataColumnList, String showType) throws Exception {
        List<WebWithPropV3xOrgMember> listWebMember = translateV3xOrgMemberToWebV3xOrgMember(member, accountId, depId);

        if (Strings.isNotEmpty(listWebMember)) {
            for (WebWithPropV3xOrgMember web : listWebMember) {
                web.setFamilyPhone(member.getOfficeNum());
                web.setMobilePhone(member.getTelNumber());

                if (!addressBookManager.checkLevel(AppContext.currentUserId(), member.getId(), accountId, addressBookSet)) {
                    web.setLevelName(AddressbookConstants.ADDRESSBOOK_INFO_REPLACE);
                }

                if (!addressBookManager.checkPhone(AppContext.currentUserId(), member.getId(), accountId, addressBookSet)) {
                    web.setMobilePhone(AddressbookConstants.ADDRESSBOOK_INFO_REPLACE);
                }

                if ("card".equals(showType)) {
                    web.setMemberName(OrgHelper.showMemberName(member.getId()));
                }

                if (Strings.isNotEmpty(metadataColumnList)) {
                    //添加展示在列头的自定义通讯录字段信息
                    Map<String, String> customerAddressbookValueMap = getCustomerAddressbookValueMap(metadataColumnList, member.getId());
                    web.setCustomerAddressbookValueMap(customerAddressbookValueMap);
                }
            }
        }

        return listWebMember;
    }

    private Map<String,String> getCustomerAddressbookValueMap(List<MetadataColumnBO> metadataColumnList,Long memberId) throws BusinessException{
        Map<String,String> customerAddressbookValueMap = new HashMap<String,String>();
        AddressBook addressBook = addressBookCustomerFieldInfoManager.getByMemberId(memberId);
        if(null!=addressBook){
        	Map<String, Object> abValue = addressBook.getExtAttrMap();
        	for(MetadataColumnBO metadataColumn : metadataColumnList){
        		String key=metadataColumn.getId().toString();
        		String columnName=metadataColumn.getColumnName();
        		try {
        			Object value = abValue.get(columnName);

        			if(metadataColumn.getType()==0){
        				String showValue=(null==value || "".equals(value))?"":String.valueOf(value);
        				customerAddressbookValueMap.put(key,showValue);
        			}
        			if(metadataColumn.getType()==1){
        				String showValue="";
        				if(null!=value && !"".equals(value)){
        					DecimalFormat df = new DecimalFormat();
        					df.setMinimumFractionDigits(0);
        					df.setMaximumFractionDigits(4);
        					showValue = df.format(Double.valueOf(String.valueOf(value)));
        					showValue = showValue.replaceAll(",", "");
        				}
        				customerAddressbookValueMap.put(key,showValue);  
        			}
        			if(metadataColumn.getType()==2){
        				String showValue=(null==value || "".equals(value))?"":Datetimes.formatDate((Date)value);
        				customerAddressbookValueMap.put(key,showValue);  
        			}
        		} catch (Exception e) {
        			logger.error("查看人员通讯录信息失败！", e);
        		}
        	}
        }
        
        return customerAddressbookValueMap;
    }

    private List<WebWithPropV3xOrgMember> translateList(List<V3xOrgMember> members, int type, Long accountId,String showType) throws Exception {
        User user = AppContext.getCurrentUser();
        Map<Long, List<MemberPost>> map = orgManager.getConcurentPostsByMemberId(user.getAccountId(), user.getId());
        List<WebWithPropV3xOrgMember> results = new ArrayList<WebWithPropV3xOrgMember>();
        if (Strings.isNotEmpty(members)) {
            AddressBookSet addressBookSet = addressBookManager.getAddressbookSetByAccountId(accountId);
            List<MetadataColumnBO> metadataColumnList=addressBookManager.getCurrentAccountEnableCustomerFields(addressBookSet);
            AddressBookSet addressBookSet1 = addressBookManager.getAddressbookSetByAccountId(AppContext.getCurrentUser().getAccountId());
            for (V3xOrgMember member : members) {
                if (type == 3 || type == 4 || Functions.checkLevelScope(user.getId(), member.getId())) {//检测 个人组不需要检测
                    Set<Long> set = map.keySet();
                    boolean isPartTime = false;
                    if (set != null) {
                        for (Long l : set) {
                            V3xOrgDepartment dep = orgManager.getDepartmentById(l);
                            if (orgManager.isInDomain(l, member.getId()) && !member.getOrgAccountId().equals(dep.getOrgAccountId())) {
                                results.addAll(parse(member, AppContext.getCurrentUser().getAccountId(), l, addressBookSet1,metadataColumnList,showType));
                                isPartTime = true;
                            }
                        }
                    }
                    if (!isPartTime) {//非兼职的人员
                        results.addAll(parse(member, accountId, null, addressBookSet,metadataColumnList,showType));
                    }
                }
            }
        }
        return results;
    }

    /**
     * 分别取人的属性添加到list里
     */
    private List<WebWithPropV3xOrgMember> parseList(List<V3xOrgMember> members, Long accountId, Long depId, AddressBookSet addressBookSet,String showType) throws Exception {
        List<WebWithPropV3xOrgMember> results = new ArrayList<WebWithPropV3xOrgMember>();
        List<MetadataColumnBO> metadataColumnList=addressBookManager.getCurrentAccountEnableCustomerFields(addressBookSet);
        if (Strings.isNotEmpty(members)) {
        	Map<Long,AddressBookSet> map = new HashMap<Long,AddressBookSet>();
            for (V3xOrgMember member : members) {
                if (addressBookSet == null) {//集团下查询，走各个单位的通讯录设置
                	AddressBookSet groupAddressBookSet = new AddressBookSet();
                	Long accId=member.getOrgAccountId();
                	if(map.containsKey(accId)){
                		groupAddressBookSet = map.get(accId);
                	}else{
                		groupAddressBookSet = addressBookManager.getAddressbookSetByAccountId(accId);
                		map.put(accId, groupAddressBookSet);
                	}
                    results.addAll(parse(member, accountId, depId, groupAddressBookSet,metadataColumnList,showType));
                } else {
                    results.addAll(parse(member, accountId, depId, addressBookSet,metadataColumnList,showType));
                }
            }
        }
        return results;
    }

    private List<WebSimpleV3xOrgMember> parsePost(List<V3xOrgMember> members, Long accountId, Long depId) throws BusinessException {
        List<WebSimpleV3xOrgMember> results = new ArrayList<WebSimpleV3xOrgMember>();
        if (Strings.isNotEmpty(members)) {
            for (V3xOrgMember member : members) {
                Long deptId = member.getOrgDepartmentId();
                Long postId = member.getOrgPostId() == null ? 0L : member.getOrgPostId();
                Long levelId = member.getOrgLevelId() == null ? 0L : member.getOrgLevelId();

                boolean isRootAccount = OrgConstants.GROUPID.equals(accountId);

                if (!isRootAccount && depId != null && !deptId.equals(depId)) {
                    deptId = depId;
                    List<MemberPost> secondPost = member.getSecondPostByDeptId(depId);
                    for (MemberPost post : secondPost) {
                        if (post.getDepId().equals(deptId)) {
                            postId = post.getPostId();
                        }
                    }
                }

                if (!isRootAccount && accountId != null && !member.getOrgAccountId().equals(accountId)) {
                    Map<Long, List<MemberPost>> map = orgManager.getConcurentPostsByMemberId(accountId, member.getId());
                    if (map != null && !map.isEmpty()) {
                    	Long tempDeptId = depId; 
                        if (depId == null) {
                        	tempDeptId = map.keySet().iterator().next();// 只选择兼职单位根时取兼职的第一个信息
                        }
                        List<MemberPost> list = map.get(tempDeptId);
                        if (list != null && !list.isEmpty()) {
                            for (MemberPost p : list) {
                                Long orgAccountId = p.getOrgAccountId() == null ? 0L : p.getOrgAccountId();
                                Long orgDepId = p.getDepId() == null ? 0L : p.getDepId();
                                Long orgMemberId = p.getMemberId() == null ? 0L : p.getMemberId();
                                Long mId = member.getId() == null ? 0L : member.getId();
                                if (orgAccountId.equals(accountId) && orgDepId.equals(tempDeptId) && orgMemberId.equals(mId)) {
                                    Long deId = p.getDepId();
                                    Long posId = p.getPostId();
                                    Long levId = p.getLevelId();
                                    deptId = deId != null ? deId.longValue() : 0L;
                                    postId = posId != null ? posId.longValue() : 0L;
                                    levelId = levId != null ? levId.longValue() : 0L;
                                    results.add(new WebSimpleV3xOrgMember(member, deptId, postId, levelId));
                                    break;//遇到第一个兼职就跳出来，只显示在这个部门下的第一个兼职信息
                                }
                            }
                        } else {
                            results.add(new WebSimpleV3xOrgMember(member, deptId, postId, levelId));
                        }
                    } else {
                        results.add(new WebSimpleV3xOrgMember(member, deptId, postId, levelId));
                    }
                } else {
                    results.add(new WebSimpleV3xOrgMember(member, deptId, postId, levelId));
                }
            }
        }

        return results;
    }

    private List<WebWithPropV3xOrgMember> parseMember(List<WebSimpleV3xOrgMember> members, Long accountId, Long deptId, AddressBookSet addressBookSet, String showType) throws Exception {
        List<WebWithPropV3xOrgMember> results = new ArrayList<WebWithPropV3xOrgMember>();
        List<MetadataColumnBO> metadataColumnList = addressBookManager.getCurrentAccountEnableCustomerFields(addressBookSet);
        if (Strings.isNotEmpty(members)) {
            AddressBookSet checkAddressBookSet = addressBookSet;
            for (WebSimpleV3xOrgMember webSimpleV3xOrgMember : members) {
                V3xOrgMember member = webSimpleV3xOrgMember.getV3xOrgMember();
                WebWithPropV3xOrgMember webWithPropV3xOrgMember = setWebMemberPLD(member, webSimpleV3xOrgMember.getPostId(), webSimpleV3xOrgMember.getLevelId(), webSimpleV3xOrgMember.getDeptId());

                if (addressBookSet == null) {//集团下查询，走各个单位的通讯录设置
                    checkAddressBookSet = addressBookManager.getAddressbookSetByAccountId(member.getOrgAccountId());
                }

                webWithPropV3xOrgMember.setFamilyPhone(member.getOfficeNum());
                webWithPropV3xOrgMember.setMobilePhone(member.getTelNumber());

                if (!addressBookManager.checkLevel(AppContext.currentUserId(), member.getId(), accountId, checkAddressBookSet)) {
                    webWithPropV3xOrgMember.setLevelName(AddressbookConstants.ADDRESSBOOK_INFO_REPLACE);
                }

                if (!addressBookManager.checkPhone(AppContext.currentUserId(), member.getId(), accountId, checkAddressBookSet)) {
                    webWithPropV3xOrgMember.setMobilePhone(AddressbookConstants.ADDRESSBOOK_INFO_REPLACE);
                }

                if ("card".equals(showType)) {
                    webWithPropV3xOrgMember.setMemberName(OrgHelper.showMemberName(member.getId()));
                }

                if (Strings.isNotEmpty(metadataColumnList)) {
                    //添加展示在列头的自定义通讯录字段信息
                    Map<String, String> customerAddressbookValueMap = getCustomerAddressbookValueMap(metadataColumnList, member.getId());
                    webWithPropV3xOrgMember.setCustomerAddressbookValueMap(customerAddressbookValueMap);
                }

                results.add(webWithPropV3xOrgMember);
            }
        }
        return results;
    }

    /**
     * 取人的部门岗位职务的方法
     * @param member 兼职人员
     * @param webMember 
     * @param accountId 兼职的单位ID
     * @param depId 兼职的部门ID
     * @throws BusinessException
     */
    private List<WebWithPropV3xOrgMember> translateV3xOrgMemberToWebV3xOrgMember(V3xOrgMember member, Long accountId, Long depId) throws BusinessException {
        boolean isRootAccount = OrgConstants.GROUPID.equals(accountId);
        List<WebWithPropV3xOrgMember> listWebMember = new ArrayList<WebWithPropV3xOrgMember>();
        Long deptId = member.getOrgDepartmentId();
        Long levelId = member.getOrgLevelId() == null ? 0L : member.getOrgLevelId();
        Long postId = member.getOrgPostId() == null ? 0L : member.getOrgPostId();
        if(!isRootAccount && member.getOrgAccountId().equals(accountId)){
        	if(!deptId.equals(depId)){
        		//主岗的部门信息和当前部门信息不一致，但还是能看到这个人，有2种可能
        		List<MemberPost> secondPost = member.getSecondPostByDeptId(depId);
        		if(Strings.isNotEmpty(secondPost)){
        			//1.副岗在当前部门
        			postId = secondPost.get(0).getPostId();
        		}else{
        			//2.主岗或者副岗在下级部门
        			List<V3xOrgDepartment> childDept = orgManager.getChildDepartments(depId, false);
        			Set<Long> cdSet = new HashSet<Long>();
        			for(V3xOrgDepartment cd : childDept){
        				cdSet.add(cd.getId());
        			}
        			
        			if(Strings.isNotEmpty(childDept)){
        				//主岗
        				boolean isMain = false;
        				if(cdSet.contains(member.getOrgDepartmentId())){
    						isMain = true;
        				}
        				//副岗
        				if(!isMain){
        		            List<MemberPost> allSecondPost = member.getSecond_post();
        		            //随机取一组副岗信息
        		            if(Strings.isNotEmpty(allSecondPost)){
        		            	for (MemberPost mp : allSecondPost) {
        		            		if (cdSet.contains(mp.getDepId())) {
        		            			deptId = mp.getDepId();
        		            			postId = mp.getPostId();
        		            			break;
        		            		}
        		            	}
        		            }
        				}
        				
        			}
        		}
        	}
            WebWithPropV3xOrgMember webMembernew = setWebMemberPLD(member, postId, levelId, deptId);
            listWebMember.add(webMembernew);
        }else if (accountId != null && !isRootAccount && !member.getOrgAccountId().equals(accountId)) {
            Map<Long, List<MemberPost>> map = orgManager.getConcurentPostsByMemberId(accountId, member.getId());
            if (map != null && !map.isEmpty()) {
                if (depId == null)
                    depId = map.keySet().iterator().next();// 2012-04-24 AEIGHT-5137 by lilong 只选择兼职单位根时取兼职的第一个信息
                List<MemberPost> list = map.get(depId);
                if (Strings.isEmpty(list)) {
                	//说明勾选了包含子部门，并且兼职在子部门中，随机取一个子部门的兼职。
                	list = new ArrayList<MemberPost>();
                	List<V3xOrgDepartment> cDepts = orgManager.getChildDepartments(depId, false);
                	for(V3xOrgDepartment cDept : cDepts){
                		if(Strings.isNotEmpty(map.get(cDept.getId()))){
                			list.addAll(map.get(cDept.getId()));
                			break;
                		}
                	}
                }
                for (MemberPost p : list) {
                    Long orgAccountId = p.getOrgAccountId() == null ? 0L : p.getOrgAccountId();
                    Long orgMemberId = p.getMemberId() == null ? 0L : p.getMemberId();
                    Long mId = member.getId() == null ? 0L : member.getId();
                    if (orgAccountId.equals(accountId) && orgMemberId.equals(mId)) {
                        Long deId = p.getDepId();
                        Long posId = p.getPostId();
                        Long levId = p.getLevelId();
                        deptId = deId != null ? deId.longValue() : 0L;
                        postId = posId != null ? posId.longValue() : 0L;
                        levelId = levId != null ? levId.longValue() : 0L;
                        WebWithPropV3xOrgMember webMembernew = setWebMemberPLD(member, postId, levelId, deptId);
                        listWebMember.add(webMembernew);
                        break;//遇到第一个兼职就跳出来，只显示在这个部门下的第一个兼职信息 2012-04-24 AEIGHT-5137 by lilong 
                    }
                }
            } else {
                WebWithPropV3xOrgMember webMembernew = setWebMemberPLD(member, postId, levelId, deptId);
                listWebMember.add(webMembernew);
            }
        } else {
            WebWithPropV3xOrgMember webMembernew = setWebMemberPLD(member, postId, levelId, deptId);
            listWebMember.add(webMembernew);
        }

        return listWebMember;
    }

    private WebWithPropV3xOrgMember setWebMemberPLD(V3xOrgMember member, Long postId, Long levelId, Long deId) throws BusinessException {
        WebWithPropV3xOrgMember webMember = new WebWithPropV3xOrgMember();

        V3xOrgDepartment dept = orgManager.getDepartmentById(deId);
        if (dept != null) {
            webMember.setDepartmentName(dept.getName());
            webMember.setDepartmentFullName(dept.getWholeName().replace(",", "/"));
        }

        V3xOrgLevel level = orgManager.getLevelById(levelId);
        if (null != level) {
            webMember.setLevelId(levelId);
            webMember.setLevelName(level.getName());
        } else {
        	if(member.getIsInternal()){
        		webMember.setLevelName(member.getProperty("levelName") == null ? "" : member.getProperty("levelName").toString());
        	}else{
        		webMember.setLevelName(OrgHelper.getExtMemberLevel(member));
        	}
        }

        V3xOrgPost post = orgManager.getPostById(postId);
        if (null != post) {
            if (postId == -1) {
                webMember.setPostName(member.getProperty("postName").toString());
            } else {
                webMember.setPostId(postId);
                webMember.setPostName(post.getName());
            }
            //内部人员副岗
            //回写副岗信息
            StringBuilder secondPost = new StringBuilder();
            List<MemberPost> secondPostList = member.getSecond_post();
            for (int i = 0; i < secondPostList.size(); i++) {
                MemberPost m = secondPostList.get(i);
                //拼接成[部门名称-岗位名称]前台显示
                V3xOrgDepartment _secondDept = orgManager.getDepartmentById(m.getDepId());
                if(_secondDept == null) continue;
                secondPost.append(_secondDept.getName());
                secondPost.append("-");
                secondPost.append(OrgHelper.showOrgPostName(m.getPostId()));
                if(i != secondPostList.size()-1) {
                    secondPost.append("、");
                }
            }
            webMember.setSecondPosts(secondPost.toString());
        }else if(!member.getIsInternal()){
        	webMember.setPostName(OrgHelper.getExtMemberPriPost(member));
        }
        webMember.setWorklocalStr(enumManagerNew.parseToName(member.getLocation()));
        webMember.setV3xOrgMember(member);
        webMember.setMemberName(member.getName());
        //webMember.setFileName(Functions.getAvatarImageUrl(member.getId()));
        return webMember;
    }

    /**
     * 根据传组list 返回解析后的组成员
     * @author lucx
     *
     */
    private List<WebV3xOrgTeam> translateV3xOrgTeam(List<V3xOrgTeam> teams) throws BusinessException {
        List<WebV3xOrgTeam> result = new ArrayList<WebV3xOrgTeam>();
        if (Strings.isEmpty(teams)) {
            return result;
        }
        for (V3xOrgTeam team : teams) {
            WebV3xOrgTeam webTeam = new WebV3xOrgTeam();
            webTeam.setV3xOrgTeam(team);
            // 取得组的部门名称
            /*V3xOrgDepartment dept = orgManager.getDepartmentById(team
            		.getDepId());
            if (null != dept) {
            	webTeam.setDeptName(dept.getName());
            }
            // 取得组的成员
             List<V3xOrgMember> teamMembers = orgManager.getTeamMember(team.getId());
            if (null != teamMembers) {
            	StringBuilder strBuffNames = new StringBuilder();
            	StringBuilder strBuffIDs = new StringBuilder();
            	for (V3xOrgMember member : teamMembers) {
            		strBuffNames.append(member.getName());
            		strBuffNames.append(",");
            		strBuffIDs.append(member.getId());
            		strBuffIDs.append(",");
            	}
            	if (strBuffIDs.length() > 0) {
            		String memNames = strBuffNames.toString();
            		memNames = memNames.substring(0, memNames.lastIndexOf(","));
            		String memIDs = strBuffIDs.toString();
            		memIDs = memIDs.substring(0, memIDs.lastIndexOf(","));
            		webTeam.setMemberIDs(memIDs);
            	}
            }*/

            result.add(webTeam);
        }
        return result;
    }

    /**
     * 转换类型
     */
    private List<Long> toLongList(String mIdStr) throws BusinessException {
        List<Long> mIds = new ArrayList<Long>();
        if (null != mIdStr && !"".equals(mIdStr)) {
            String[] memIds = mIdStr.split(",");
            for (String strId : memIds) {
                if (null == strId || "".equals(strId))
                    continue;
                OrgTypeIdBO bo = new OrgTypeIdBO();
                Long id = Long.parseLong(strId);
                mIds.add(id);
            }
        }
        return mIds;
    }
    
    /**
     * 转换类型
     */
    private List<OrgTypeIdBO> toOrgTypeIdBOList(String mIdStr) throws BusinessException {
        List<OrgTypeIdBO> mIds = new ArrayList<OrgTypeIdBO>();
        if (null != mIdStr && !"".equals(mIdStr)) {
            String[] memIds = mIdStr.split(",");
            for (String strId : memIds) {
                if (null == strId || "".equals(strId))
                    continue;
                OrgTypeIdBO bo = new OrgTypeIdBO();
                Long id = Long.parseLong(strId);
                bo.setId(id);
                bo.setType(OrgConstants.ORGENT_TYPE.Member.name());
                mIds.add(bo);
            }
        }
        return mIds;
    }

    /**
     * 进去通讯录的框架
     */
    public ModelAndView home(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String accountId = ServletRequestUtils.getStringParameter(request, "accountId");
        ModelAndView mav = new ModelAndView();
        int addressbookType = 0;
        
    	addressbookType = ServletRequestUtils.getIntParameter(request, "addressbookType", 0);
    	if(addressbookType==0){
    		//联系人
    		mav= new ModelAndView("addressbook/contact/contacts");
    		String contactFrame = ServletRequestUtils.getStringParameter(request, "contactFrame");
    		if(Strings.isNotBlank(contactFrame) && "allContacts".equals(contactFrame)){
    			//联系人列表页面
    			mav= new ModelAndView("addressbook/contact/allContacts");
    		}
    	}else if(addressbookType==1){
    		//组织架构
            String markType = ServletRequestUtils.getStringParameter(request, "markType");
            String changeType = ServletRequestUtils.getStringParameter(request, "changeType");
            if("markII".equals(markType)){
                //家谱树图展示
                mav= new ModelAndView("addressbook/public/addressbookOrgStructure_markII");
            }else{
                //卡片 列表式展示
                mav= new ModelAndView("addressbook/public/addressbookOrgStructure");
                if(Strings.isNotBlank(changeType)){
                    mav.addObject("changeType",changeType);
                }
            }
    	}else if(addressbookType==2){
    		//私人组
    		mav= new ModelAndView("addressbook/private/privateAddressBook");
    	}else if(addressbookType==3){
    		//系统组 
    		mav= new ModelAndView("addressbook/public/systemTeam");
    	}else if(addressbookType==4){
    		//个人组
    		mav= new ModelAndView("addressbook/public/personalAddressBook");
    	}
    	
		mav.addObject("loginAccount", AppContext.getCurrentUser().getLoginAccount());
		mav.addObject("loginAccountName", AppContext.getCurrentUser().getLoginAccountShortName());
		
		boolean isGroupVer = (Boolean) (SysFlag.sys_isGroupVer.getFlag());
		mav.addObject("isGroupVer", isGroupVer);
		
        
        mav.addObject("addressbookType", addressbookType);
        return mav;
    }

    /**
     * 框架中转
     */
    public ModelAndView homeEntry(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	//校验当前用户是否用通讯录的菜单
    	if(!AppContext.getCurrentUser().hasResourceCode("addressbook")){
    		return null;
    	}
    	
    	ModelAndView mav = new ModelAndView("addressbook/addressbookFrame");
        return mav;
    }

    /**
     * 进入通讯录显示页签的方法
     */
    public ModelAndView initSpace(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int addressbookType = ServletRequestUtils.getIntParameter(request, "addressbookType", 1);
        ModelAndView mav = new ModelAndView(jspView("space"));
        mav.addObject("addressbookType", Integer.valueOf(addressbookType));
        return mav;
    }
    
    /**
     * 根据查询条件，模糊匹配任意项
     */
    private Collection<WebWithPropV3xOrgMember> newFiltWebWithPropV3xOrgMembers(HttpServletRequest request,AddressBookSet addressBookSet,Collection<WebWithPropV3xOrgMember> members,boolean isRoot,String showType) throws Exception {
    		String searchContent=request.getParameter("searchContent");
    		if(Strings.isBlank(searchContent)){
    			return members;
    		}else{
    			searchContent = searchContent.toLowerCase();
				List<MetadataColumnBO> metadataColumnList  =  addressBookManager.getCurrentAccountEnableCustomerFields(addressBookSet);    			
    			Iterator<WebWithPropV3xOrgMember> it = members.iterator();
    			while (it.hasNext()) {
    				WebWithPropV3xOrgMember member = it.next();
    				String memberinfostr="";
    				//列表模式，查询列头有的字段。卡片模式，查询小卡片中有的字段
    				if("list".equals(showType)){
    					if(addressBookSet.isMemberName() && null != member.getMemberName()){
    						memberinfostr+=member.getMemberName()+" ";
    					}
    					if(isRoot){
    						memberinfostr+=member.getAccountName()+" ";
    					}else if(addressBookSet.isMemberDept() && null != member.getDepartmentName()){
    						memberinfostr+=member.getDepartmentName()+" ";
    					}
    					if(addressBookSet.isMemberPost() && null != member.getPostName()){
    						memberinfostr+=member.getPostName()+" ";
    					}
    					//副岗
    					if(addressBookSet.isMemberSecondPost() && null != member.getSecondPosts()){
    						memberinfostr+=member.getSecondPosts()+" ";
    					}
    					if(addressBookSet.isMemberLevel() && null != member.getLevelName()){
    						memberinfostr+=member.getLevelName()+" ";
    					}
    					if(addressBookSet.isWorkLocal() && null != member.getWorklocalStr()){
    						memberinfostr+=member.getWorklocalStr()+" ";
    					}
    					if(addressBookSet.isMemberPhone() && null != member.getV3xOrgMember().getOfficeNum()){
    						memberinfostr+=member.getV3xOrgMember().getOfficeNum()+" ";
    					}
    					if(addressBookSet.isMemberMobile() && null != member.getMobilePhone()){
    						memberinfostr+=member.getMobilePhone()+" ";
    					}
    					if(addressBookSet.isMemberEmail() && null != member.getV3xOrgMember().getEmailAddress()){
    						memberinfostr+=member.getV3xOrgMember().getEmailAddress()+" ";
    					}
    					if(addressBookSet.isMemberWX() && null != member.getV3xOrgMember().getWeixin()){
    						memberinfostr+=member.getV3xOrgMember().getWeixin()+" ";
    					}
    					if(addressBookSet.isMemberWB() && null != member.getV3xOrgMember().getWeibo()){
    						memberinfostr+=member.getV3xOrgMember().getWeibo()+" ";
    					}
    					if(addressBookSet.isMemberHome() && null != member.getV3xOrgMember().getAddress()){
    						memberinfostr+=member.getV3xOrgMember().getAddress()+" ";
    					}
    					if(addressBookSet.isMemberCode() && null != member.getV3xOrgMember().getPostalcode()){
    						memberinfostr+=member.getV3xOrgMember().getPostalcode()+" ";
    					}
    					if(addressBookSet.isMemberAddress() && null != member.getV3xOrgMember().getPostAddress()){
    						memberinfostr+=member.getV3xOrgMember().getPostAddress()+" ";
    					}
    					
    					if(!metadataColumnList.isEmpty()){
    					    AddressBook addressBook = addressBookCustomerFieldInfoManager.getByMemberId(member.getV3xOrgMember().getId());
    					    if(addressBook != null){
    					        Map<String, Object> abValue = addressBook.getExtAttrMap();
    					        for(MetadataColumnBO metadataColumn : metadataColumnList){
    					            String columnName = metadataColumn.getColumnName();
    					            Object value = abValue.get(columnName);
    					            if(null != value){
    					                memberinfostr+=value.toString()+" ";
    					            }
    					        }
    					    }
    					}
    				}else if("card".equals(showType)){
						memberinfostr+=
								(null!=member.getMemberName()?member.getMemberName():"")+" "+
							    (null!=member.getDepartmentName()?member.getDepartmentName():"")+" "+
								(null!=member.getPostName()?member.getPostName():"")+" "+
								(null!=member.getSecondPosts()?member.getSecondPosts():"")+" "+
							    (null!=member.getV3xOrgMember().getOfficeNum()?member.getV3xOrgMember().getOfficeNum():"")+" "+
								(null!=member.getMobilePhone()?member.getMobilePhone():"")+" "+
							    (null!=member.getV3xOrgMember().getEmailAddress()?member.getV3xOrgMember().getEmailAddress():"")+" ";
    				}
    				
    				memberinfostr = memberinfostr.toLowerCase();
    				if(memberinfostr.indexOf(searchContent)<0){
    					it.remove();
    				}
    			}
    		}
            
        return members;
    }

    /**
     * 根据列表查询条件过滤外部联系人集合
     * 
     * @param members
     * @return
     */
    private Collection<AddressBookMember> filtAddressBookMembers(Collection<AddressBookMember> members) {
        String expressionType = ListSearchHelper.getExpressionType();
        if (expressionType != null) {
            Iterator<AddressBookMember> it = members.iterator();
            while (it.hasNext()) {
                AddressBookMember member = it.next();
                if ("name".equals(expressionType)) { // 姓名，模糊匹配
                    if (member.getName() == null || member.getName().indexOf(ListSearchHelper.getExpressionValueString()) < 0) {
                        it.remove();
                    }
                } else if ("abPost".equals(expressionType)) { // 岗位，模糊匹配
                    if (member.getCompanyPost() == null || member.getCompanyPost().indexOf(ListSearchHelper.getExpressionValueString()) < 0) {
                        it.remove();
                    }
                } else if ("abLevel".equals(expressionType)) { // 职务级别，模糊匹配
                    if (member.getCompanyLevel() == null || member.getCompanyLevel().indexOf(ListSearchHelper.getExpressionValueString()) < 0) {
                        it.remove();
                    }
                } else if ("mobilePhone".equals(expressionType)) { // 手机号码，模糊匹配
                    if (member.getMobilePhone() == null || member.getMobilePhone().indexOf(ListSearchHelper.getExpressionValueString()) < 0) {
                        it.remove();
                    }
                } else if ("companyPhone".equals(expressionType)) {// 办公电话，模糊匹配
                    if (member.getCompanyPhone() == null || member.getCompanyPhone().indexOf(ListSearchHelper.getExpressionValueString()) < 0) {
                        it.remove();
                    }
                }
            }
        }
        return members;
    }
    
    /**
     * 根据列表查询条件过滤外部联系人集合(不指定查询字段，模糊查询)
     * 
     * @param members
     * @return
     */
    private Collection<AddressBookMember> newFiltAddressBookMembers(HttpServletRequest request,Collection<AddressBookMember> members) {
    	String searchContent=request.getParameter("searchContent");
		if(Strings.isBlank(searchContent)){
			return members;
		}else{
			searchContent = searchContent.toLowerCase();
            Iterator<AddressBookMember> it = members.iterator();
            while (it.hasNext()) {
            	String memberinfostr = "";
                AddressBookMember member = it.next();
                memberinfostr=
                		(null!=member.getName()?member.getName():"")+" "+
                        (null!=member.getCompanyName()?member.getCompanyName():"")+" "+
                		(null!=member.getCompanyLevel()?member.getCompanyLevel():"")+" "+
                        (null!=member.getCompanyPhone()?member.getCompanyPhone():"")+" "+
                		(null!=member.getMobilePhone()?member.getMobilePhone():"")+" ";
                memberinfostr = memberinfostr.toLowerCase();
				if(memberinfostr.indexOf(searchContent)<0){
					it.remove();
				}
            }
        }
        return members;
    }
    
    public ModelAndView initList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int addressbookType = ServletRequestUtils.getIntParameter(request, "addressbookType", 1);
        String showType = request.getParameter("showType");
        ModelAndView mav = new ModelAndView(myJspView(addressbookType, "listMembers",showType));
        User user = AppContext.getCurrentUser();
        Long loginAccId = user.getLoginAccount();
        String accountId = request.getParameter("accountId");
        if (StringUtils.isNotBlank(accountId)) {
            loginAccId = Long.parseLong(accountId);
        }
        List<V3xOrgLevel> levellist = orgManager.getAllLevels(loginAccId);
        if (levellist == null) {
            levellist = new ArrayList<V3xOrgLevel>();
        }
        mav.addObject("levellist", levellist);

        // 判断是否具有发送手机短信的权限(是否显示手机图标)
        boolean isCanSendSMS = false;
        if (SystemEnvironment.hasPlugin("sms")) {
            if (mobileMessageManager.isCanSend(user.getId(), user.getLoginAccount())) {
                isCanSendSMS = true;
            }
        }
        mav.addObject("isCanSendSMS", isCanSendSMS);

        if (1 == addressbookType) { // 员工通讯录
            V3xOrgAccount selectAccount = orgManager.getAccountById(loginAccId);
            boolean isRoot = selectAccount != null && selectAccount.isGroup();
            mav.addObject("isRoot", isRoot);
            if (!isRoot) {
                return this.listAllMembers(request, response);
            }

            AddressBookSet addressBookSet = this.getAddressBookSet(mav, user.getId(), loginAccId);
            List<V3xOrgMember> members2 = new ArrayList<V3xOrgMember>();
            List<V3xOrgMember> members = new ArrayList<V3xOrgMember>();
            //选择全集团，没有数据。但支持查询
            String searchContent=request.getParameter("searchContent");
            Map<Long,AddressBookSet> map = new HashMap<Long,AddressBookSet>();
            if(Strings.isNotBlank(searchContent)){
            	members = orgManager.getAllMembers(V3xOrgEntity.VIRTUAL_ACCOUNT_ID);
            	
            	Map<Long, V3xOrgAccount> accessableAccountsMap = new HashMap<Long, V3xOrgAccount>();
            	List<V3xOrgAccount> accessableAccounts = orgManager.accessableAccounts(user.getId());
            	for (V3xOrgAccount account : accessableAccounts) {
            		accessableAccountsMap.put(account.getId(), account);
            	}
            	
            	for (V3xOrgMember member : members) {
            		if (accessableAccountsMap.get(member.getOrgAccountId()) != null) {
            			AddressBookSet addressBookSet1 = new AddressBookSet();
            			Long accId=member.getOrgAccountId();
            			if(map.containsKey(accId)){
            			    addressBookSet1 = map.get(accId);
            			}else{
            			    addressBookSet1 = addressBookManager.getAddressbookSetByAccountId(accId);
            				map.put(accId, addressBookSet1);
            			}
            				members2.add(member);
            		}
            	}
            }

             mav.addObject("isRootQuery", true);
             List<WebWithPropV3xOrgMember> allresultsTemp = (List<WebWithPropV3xOrgMember>) newFiltWebWithPropV3xOrgMembers(request,(AddressBookSet)((mav.getModel()).get("addressBookSet")),parseList(members2, user.getAccountId(), user.getDepartmentId(), null,showType),false,showType);
             List<WebWithPropV3xOrgMember> allresults = new ArrayList<WebWithPropV3xOrgMember>();
            Map<Long, List<V3xOrgDepartment>> deptsMap = new HashMap<Long, List<V3xOrgDepartment>>();
            Map<Long, Set<Long>> deptIdsMap = new HashMap<Long, Set<Long>>();
            V3xOrgMember currentMember = orgManager.getMemberById(user.getId());
             for(WebWithPropV3xOrgMember  m :allresultsTemp){
            	 Long accId = orgManager.getMemberById(m.getV3xOrgMember().getId()).getOrgAccountId();
            	 if (addressBookManager.checkLevelScope(currentMember, m.getV3xOrgMember(), accId, map.get(accId), deptsMap, deptIdsMap)) {
            		 allresults.add(m);
            	 }
             }
             List<WebWithPropV3xOrgMember> results = this.pagenate(request,allresults);
            if("card".equals(showType)){
            	//mav.addObject("members",allresults);
            	mav.addObject("memberStr",getMemberJson(allresults));            	
            	mav.addObject("resultCount",allresults.size());
            }else{
            	 mav.addObject("members", results);
            }
            
            List<MetadataColumnBO> metadataColumnList = addressBookManager.getCurrentAccountEnableCustomerFields(addressBookSet);
            mav.addObject("bean", metadataColumnList);
        } else if (2 == addressbookType) { // 私人通讯录
            List<AddressBookMember> members = addressBookManager.getMembersByCreatorId(user.getId());
            newFiltAddressBookMembers(request,members);
            
            if("card".equals(showType)){
            	mav.addObject("members",members);
            }else{
            	mav.addObject("members", this.pagenate(request, members));
            }
            mav.addObject("resultCount", members.size());
        } else if (3 == addressbookType) {//系统组 
            List<V3xOrgTeam> teamlist = orgManager.getTeamsByMember(user.getId(), loginAccId);
            AddressBookSet addressBookSet = this.getAddressBookSet(mav, user.getId(), loginAccId);
            if(teamlist.size()>0){
            	for (Iterator<V3xOrgTeam> iter = teamlist.iterator(); iter.hasNext();) {
            		V3xOrgTeam team = iter.next();
            		if (team.getType() == OrgConstants.TEAM_TYPE.PERSONAL.ordinal() || team.getType() == OrgConstants.TEAM_TYPE.DISCUSS.ordinal() || team.getType() == OrgConstants.TEAM_TYPE.COLTEAM.ordinal()) {
            			iter.remove();
            		}
            		if (!team.isValid()) {
            			iter.remove();
            		}
            		//修改 去掉公开系统组不能被不在部门范围内的人看到。by wusb at 2010-12-21
            		if (team.getDepId() != null && team.getDepId() != -1) {
            			List<V3xOrgDepartment> depts = orgManager.getChildDepartments(team.getDepId(), false);
            			List<Long> deptIds = new ArrayList<Long>();
            			deptIds.add(team.getDepId());
            			for (V3xOrgDepartment dept : depts) {
            				deptIds.add(dept.getId());
            			}
            			if (!deptIds.contains(user.getDepartmentId()) && !isMyTeam(user.getId(), team)) {
            				iter.remove();
            			}
            		}
            	}
            	
        		Collections.sort(teamlist, new Comparator<V3xOrgTeam>() {
        			public int compare(V3xOrgTeam c1, V3xOrgTeam c2) {
        				//type: 1个人组; 3项目组; 2系统组; 4讨论组
        				int type1 = c1.getType();
        				int type2 = c2.getType();

                        if (type1 == 1) type1 = -2;
                        if (type1 == 3) type1 = -1;
                        if (type2 == 1) type2 = -2;
                        if (type2 == 3) type2 = -1;

        				if(type1 == type2){
                          if ((c1.getSortId() == null) && (c2.getSortId() == null)) {
                            return 0;
        				}
                          if (c1.getSortId() == null) {
                            return 1;
                          }
                          if (c2.getSortId() == null) {
                            return -1;
        				}
                          long id1 = c1.getSortId().longValue();
                          long id2 = c2.getSortId().longValue();
                          if(id1==id2){
                        	  return c1.getName().compareToIgnoreCase(c2.getName());
                          }else{
                        	  return id1 < id2 ? -1 : id1 > id2 ? 1 : 0;
                          }
                        }
                        return type1 < type2 ? -1 : 1;
        			}
        		});
            	
            	List<WebV3xOrgTeam> teams = translateV3xOrgTeam(teamlist);
            	Long teamId=null;
            	if(null!=teams && teams.size()>0){
            		teamId=teams.get(0).getV3xOrgTeam().getId();
            		List<V3xOrgMember> members = new ArrayList<V3xOrgMember>();
            		List<WebWithPropV3xOrgMember> results = new ArrayList<WebWithPropV3xOrgMember>();
            		V3xOrgTeam team = orgManager.getTeamById(teamId);
            		List<OrgTypeIdBO> leaderIds = team.getLeaders();
//            		List<OrgTypeIdBO> memberIds = team.getMembers();
            		List<OrgTypeIdBO> relations = team.getRelatives();
            		List<OrgTypeIdBO> supervisors = team.getSupervisors();
//            		leaderIds.addAll(memberIds);
            		leaderIds.addAll(relations);
            		leaderIds.addAll(supervisors);
            		for (OrgTypeIdBO memberId : leaderIds) {
            			V3xOrgMember member = orgManager.getMemberById(memberId.getLId());
            			if (member != null)
            				if (members.isEmpty()) {
            					members.add(member);
            				} else {
            					if (!members.contains(member))
            						members.add(member);
            				}
            		}
            		
                    //组中 可能包含团队，直接取出团队中所有的人
                    List<V3xOrgMember> itemMembers = orgManager.getMembersByTeam(teamId);
                    for(V3xOrgMember m : itemMembers){
                        if (members.isEmpty()) {
                            members.add(m);
                        } else {
                            if (!members.contains(m))
                                members.add(m);
                        }
                    }
                    
            		List<V3xOrgMember> temp = this.filterMemberByAssigned(members);
            		List<WebWithPropV3xOrgMember> allresults = new ArrayList<WebWithPropV3xOrgMember>();
            		if (members.size() != 0) {
            			 allresults = (List<WebWithPropV3xOrgMember>) newFiltWebWithPropV3xOrgMembers(request,(AddressBookSet)((mav.getModel()).get("addressBookSet")),translateList(temp, addressbookType, loginAccId, showType),false,showType);
            			 results = this.pagenate(request, allresults);
            		}
            		mav.addObject("resultCount", temp.size());
                    if("card".equals(showType)){
                    	//mav.addObject("members",allresults);
                    	mav.addObject("memberStr",getMemberJson(allresults));  
                    }else{
                   	    mav.addObject("members", results);
                    }
            	}            	
            }
            
            List<MetadataColumnBO> metadataColumnList = addressBookManager.getCurrentAccountEnableCustomerFields(addressBookSet);
            mav.addObject("bean", metadataColumnList);

        } else if (4 == addressbookType) {//个人组
            AddressBookSet addressBookSet = this.getAddressBookSet(mav, user.getId(), loginAccId);
            Long teamId = null;
            List<V3xOrgTeam> teamlist = orgManager.getTeamsByOwner(user.getId(), loginAccId);
            //TODO 这期个人组还是只能选人
            if (teamlist.size() > 0) {
                teamId = teamlist.get(0).getId();
                V3xOrgTeam team = orgManager.getTeamById(teamId);
                List<OrgTypeIdBO> memberIds = new ArrayList<OrgTypeIdBO>(team.getLeaders());
                memberIds.addAll(team.getMembers());
                List<V3xOrgMember> mems = new ArrayList<V3xOrgMember>();
                for (OrgTypeIdBO memberId : memberIds) {
                    mems.add(orgManager.getMemberById(memberId.getLId()));
                }
                List<V3xOrgMember> members = this.filterMemberByAssigned(mems);
                List<WebWithPropV3xOrgMember> allresults = (List<WebWithPropV3xOrgMember>) newFiltWebWithPropV3xOrgMembers(request,(AddressBookSet)((mav.getModel()).get("addressBookSet")),translateList(members, 4, loginAccId,showType),false,showType);
                List<WebWithPropV3xOrgMember> results = this.pagenate(request, allresults);
                mav.addObject("resultCount", members.size());
                if("card".equals(showType)){
                	//mav.addObject("members",allresults);
                	mav.addObject("memberStr",getMemberJson(allresults));  
                }else{
               	    mav.addObject("members", results);
                }
            } else {
                List<WebWithPropV3xOrgMember> members = new ArrayList<WebWithPropV3xOrgMember>();
                mav.addObject("members", this.pagenate(request, members));
                mav.addObject("resultCount", members.size());
            }
            
            List<MetadataColumnBO> metadataColumnList = addressBookManager.getCurrentAccountEnableCustomerFields(addressBookSet);
            mav.addObject("bean", metadataColumnList);
        }
        return mav;
    }

    public ModelAndView initDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView(jspView("detail"));
        return mav;
    }

    public ModelAndView initTree(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return null;
    }

    // 显示员工列表(全部/部门/系统组/个人组)
    public ModelAndView listAllMembers(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int addressbookType = ServletRequestUtils.getIntParameter(request, "addressbookType", 1);
        ModelAndView mav = new ModelAndView(jspView(addressbookType, "listMembers"));
        String showType = request.getParameter("showType");
    	if("card".equals(showType)){
    		mav = new ModelAndView("addressbook/public/addressbookOrgStructure_card");
    	}
    	if("tree".equals(showType)){
    		mav = new ModelAndView("addressbook/public/addressbookOrgStructure_tree");
    	}
        User user = AppContext.getCurrentUser();
        V3xOrgMember currentMember = orgManager.getMemberById(user.getId());
        Long accountId = Strings.isNotBlank(request.getParameter("accountId")) ? Long.parseLong(request.getParameter("accountId")) : user.getLoginAccount();

        // 判断是否具有发送手机短信的权限(是否显示手机图标)
        boolean isCanSendSMS = false;
        if (SystemEnvironment.hasPlugin("sms")) {
            if (mobileMessageManager.isCanSend(user.getId(), user.getLoginAccount())) {
                isCanSendSMS = true;
            }
        }
        mav.addObject("isCanSendSMS", isCanSendSMS);

        AddressBookSet addressBookSet = this.getAddressBookSet(mav, user.getId(), accountId);

        if (user.isInternal()) {
            if (1 == addressbookType) { // 员工
                List<V3xOrgMember> inList = orgManager.getAllMembers(accountId);// 单位人员，包括兼职人员
                List<V3xOrgMember> outList = new ArrayList<V3xOrgMember>();
                List<V3xOrgMember> outMembers = orgManager.getMemberWorkScopeForExternal(user.getId(), false);// 获取我能看的外部人员
                if (Strings.isNotEmpty(outMembers)) {
                    for (V3xOrgMember member : outMembers) {
                        if (member.getOrgAccountId().equals(accountId)) {
                            outList.add(member);
                        }
                    }
                }

                List<V3xOrgMember> allList = new ArrayList<V3xOrgMember>();
                allList.addAll(inList);
                allList.addAll(outList);
                Map<Long, List<V3xOrgDepartment>> deptsMap = new HashMap<Long, List<V3xOrgDepartment>>();
                Map<Long, Set<Long>> deptIdsMap = new HashMap<Long, Set<Long>>();
                String searchContent = request.getParameter("searchContent");
                if (Strings.isBlank(searchContent)) {
                    List<V3xOrgMember> allresults = new ArrayList<V3xOrgMember>();
                    for (V3xOrgMember m : allList) {
                    	if(!m.getIsInternal()){
                    		 allresults.add(m);
                    	}else if (addressBookManager.checkLevelScope(currentMember, m, accountId, addressBookSet, deptsMap, deptIdsMap)) {
                            allresults.add(m);
                        }
                    }

                    if ("card".equals(showType)) {
                        List<WebSimpleV3xOrgMember> parsePosts = parsePost(allresults, accountId, null);
                        List<WebWithPropV3xOrgMember> parseMembers = parseMember(parsePosts, accountId, null, addressBookSet, showType);
                        mav.addObject("memberStr", getMemberJson(parseMembers));
                        mav.addObject("resultCount", parseMembers.size());
                    }else if("tree".equals(showType)){
                        V3xOrgAccount rootAccount = orgManager.getAccountById(accountId);
                        Map<String,Object> rootAccountObj = new HashMap<String, Object>();
                        rootAccountObj.put("id",rootAccount.getId().toString());
                        rootAccountObj.put("name",rootAccount.getName());

                        List<Map<String,Object>> treeList = new ArrayList<Map<String, Object>>();
                        List<V3xOrgDepartment> allDepartments = new UniqueList<V3xOrgDepartment>();
                        List<V3xOrgDepartment> allInternalDepartments = orgManager.getAllInternalDepartments(accountId);
                        List<V3xOrgDepartment> allExtDepartments = orgManager.getAllExtDepartments(accountId);
                        Collections.sort(allInternalDepartments, CompareUnitPath.getInstance2());
                        Collections.sort(allExtDepartments, CompareUnitPath.getInstance2());
                        allDepartments.addAll(allInternalDepartments);
                        allDepartments.addAll(allExtDepartments);
                        for(V3xOrgDepartment dept : allDepartments){
                            int level = orgManager.getDeptLevel(dept.getId());
                            if(level > 2){
                                continue;
                            }
                            int num = 0;
                            try {
                                num = addressBookManager.getDeptMemberSize(dept.getId(),false);
                            } catch (Exception e) {
                                log.error("取人数异常", e);
                            }

                            Map<String,Object> tempDept = new HashMap<String, Object>();
                            tempDept.put("id",dept.getId().toString());
                            tempDept.put("pId",dept.getSuperior().toString());
                            tempDept.put("name",dept.getName());
                            tempDept.put("hasChildren",dept.hasChildren(true));
                            tempDept.put("num",num);
                            tempDept.put("level",level);
                            treeList.add(tempDept);
                        }
                        mav.addObject("treeList",JSONUtil.toJSONString(treeList));
                        mav.addObject("rootAccount",JSONUtil.toJSONString(rootAccountObj));
                    } else {
                        List<V3xOrgMember> pageList = pagenate(request, allresults);
                        List<WebSimpleV3xOrgMember> parsePosts = parsePost(pageList, accountId, null);
                        List<WebWithPropV3xOrgMember> parseMembers = parseMember(parsePosts, accountId, null, addressBookSet, showType);
                        mav.addObject("members", parseMembers);
                    }
                } else {
                    List<WebSimpleV3xOrgMember> parsePosts = parsePost(allList, accountId, null);
                    List<WebWithPropV3xOrgMember> parseMembers = parseMember(parsePosts, accountId, null, addressBookSet, showType);
                    newFiltWebWithPropV3xOrgMembers(request, (AddressBookSet) ((mav.getModel()).get("addressBookSet")), parseMembers, false, showType);
                    List<WebWithPropV3xOrgMember> allresults = new ArrayList<WebWithPropV3xOrgMember>();
                    for (WebWithPropV3xOrgMember m : parseMembers) {
                    	if(!m.getV3xOrgMember().getIsInternal()){
                    		allresults.add(m);
                    	}else if (addressBookManager.checkLevelScope(currentMember, m.getV3xOrgMember(), accountId, addressBookSet, deptsMap, deptIdsMap)) {
                            allresults.add(m);
                        }
                    }

                    if ("card".equals(showType)) {
                        mav.addObject("memberStr", getMemberJson(allresults));
                        mav.addObject("resultCount", allresults.size());
                    } else {
                        mav.addObject("members", pagenate(request, allresults));
                    }
                }

                List<MetadataColumnBO> metadataColumnList = addressBookManager.getCurrentAccountEnableCustomerFields(addressBookSet);
                mav.addObject("bean", metadataColumnList);
            } else if (2 == addressbookType) { // 外部联系人
                List<AddressBookMember> members = addressBookManager.getMembersByCreatorId(user.getId());
                filtAddressBookMembers(members);
                mav.addObject("members", members);
            }
        } else { //外部单位人员
            List<WebWithPropV3xOrgMember> allresults = new ArrayList<WebWithPropV3xOrgMember>();
            if (addressBookSet == null) {
                addressBookSet = new AddressBookSet();
            }            
            List<V3xOrgMember> memberList = (List<V3xOrgMember>) OuterWorkerAuthUtil.getCanAccessMembers(AppContext.getCurrentUser().getId(), AppContext.getCurrentUser().getDepartmentId(), AppContext.currentAccountId(), orgManager);
            List<WebWithPropV3xOrgMember> parseList = parseList(memberList, accountId, null, addressBookSet,showType);
            allresults.addAll(parseList);
            newFiltWebWithPropV3xOrgMembers(request,addressBookSet,allresults,false,showType);
            if("card".equals(showType)){
            	//mav.addObject("members",allresults);
            	mav.addObject("memberStr",getMemberJson(allresults));  
            	mav.addObject("resultCount",allresults.size());
            }else if("tree".equals(showType)){
                V3xOrgAccount rootAccount = orgManager.getAccountById(accountId);
                Map<String,Object> rootAccountObj = new HashMap<String, Object>();
                rootAccountObj.put("id",rootAccount.getId().toString());
                rootAccountObj.put("name",rootAccount.getName());

                List<Map<String,Object>> treeList = new ArrayList<Map<String, Object>>();
                List<V3xOrgDepartment> allDepartments = OuterWorkerAuthUtil.getCanAccessDep(user.getId(), user.getDepartmentId(), accountId, orgManager, true);
                Collections.sort(allDepartments, CompareUnitPath.getInstance2());
                for(V3xOrgDepartment dept : allDepartments){
                    int level = orgManager.getDeptLevel(dept.getId());
                    if(level > 2){
                        continue;
                    }
                    int num = 0;
                    try {
                        num = addressBookManager.getDeptMemberSize(dept.getId(),false);
                    } catch (Exception e) {
                        log.error("取人数异常", e);
                    }

                    Map<String,Object> tempDept = new HashMap<String, Object>();
                    tempDept.put("id",dept.getId().toString());
                    tempDept.put("pId",dept.getSuperior().toString());
                    tempDept.put("name",dept.getName());
                    tempDept.put("hasChildren",dept.hasChildren(true));
                    tempDept.put("num",num);
                    tempDept.put("level",level);
                    treeList.add(tempDept);
                }
                mav.addObject("treeList",JSONUtil.toJSONString(treeList));
                mav.addObject("rootAccount",JSONUtil.toJSONString(rootAccountObj));
            }else{
            	mav.addObject("members", this.pagenate(request, allresults));
            }
        }

        return mav;
    }

    
    private boolean showExportPrint(Long userId, AddressBookSet addressBookSet){
        boolean showExportPrint = true;
        if (null != addressBookSet) {
            if (addressBookSet.getExportPrint() != 1) {
                Set<Long> exportPrintMemberSet = addressBookSet.getExportPrintMemberSet();
                if (Strings.isEmpty(exportPrintMemberSet)) {
                    showExportPrint = false;
                } else {
                    showExportPrint = exportPrintMemberSet.contains(userId);
                }
            }
        }
        return showExportPrint;
    }

    private AddressBookSet getAddressBookSet(ModelAndView mav, Long userId, Long accountId) {
        AddressBookSet addressBookSet = addressBookManager.getAddressbookSetByAccountId(accountId);
        boolean showExportPrint = this.showExportPrint(userId, addressBookSet);
        mav.addObject("addressBookSet", addressBookSet != null ? addressBookSet : new AddressBookSet());
        mav.addObject("showExportPrint", showExportPrint);
        return addressBookSet;
    }

    /**
     * 点击树结构的部门，显示部门的人员
     */
    public ModelAndView listDeptMembers(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	User user = AppContext.getCurrentUser();
    	V3xOrgMember currentMember = orgManager.getMemberById(user.getId());
        ListSearchHelper.pickupExpression(request, null, "condition", "textfield");
        int addressbookType = ServletRequestUtils.getIntParameter(request, "addressbookType", 1);
        ModelAndView mav = new ModelAndView(jspView(addressbookType, "listMembers"));
        int isDepartment = ServletRequestUtils.getIntParameter(request, "isDepartment", 1);
        Long deptId = ServletRequestUtils.getLongParameter(request, "pId");
        Long accountId = ServletRequestUtils.getLongParameter(request, "accountId"); 
        String showType = request.getParameter("showType");
    	if("card".equals(showType)){
    		mav = new ModelAndView("addressbook/public/addressbookOrgStructure_card");
    	}

        // 判断是否具有发送手机短信的权限(是否显示手机图标)
        boolean isCanSendSMS = false;
        if (SystemEnvironment.hasPlugin("sms")) {
            if (mobileMessageManager.isCanSend(AppContext.currentUserId(), AppContext.currentAccountId())) {
                isCanSendSMS = true;
            }
        }
        mav.addObject("isCanSendSMS", isCanSendSMS);

        AddressBookSet addressBookSet = this.getAddressBookSet(mav, AppContext.currentUserId(), accountId);

        List<V3xOrgMember> members = new ArrayList<V3xOrgMember>();
        V3xOrgDepartment dep = orgManager.getDepartmentById(deptId);
        if(user.isInternal()){
        	if (dep.getIsInternal()) {
        		 List<V3xOrgMember> internalMember = new ArrayList<V3xOrgMember>();
                if (isDepartment == 1) {
                	internalMember = orgManager.getMembersByDepartment(dep.getId(), false);
                } else {
                	internalMember = orgManager.getMembersByDepartment(dep.getId(), true);
                }
                
                Map<Long,List<V3xOrgDepartment>> deptsMap = new HashMap<Long,List<V3xOrgDepartment>>();
                Map<Long,Set<Long>> deptIdsMap = new HashMap<Long,Set<Long>>();
                if (Strings.isNotEmpty(internalMember)) {
                    for (V3xOrgMember member : internalMember) {
                        if (member.getOrgDepartmentId() == null) {
                            continue;
                        }
                        if (addressBookManager.checkLevelScope(currentMember, member, accountId, (AddressBookSet)((mav.getModel()).get("addressBookSet")),deptsMap,deptIdsMap)) {
                        	members.add(member);
                        }
                    }
                }
                
                if (isDepartment == 1) {
            		List<V3xOrgMember>  externalList = orgManager.getMemberWorkScopeForExternal(user.getId(), false);// 获取我能看的外部人员
            		List<V3xOrgDepartment> childDepts = orgManager.getChildDepartments(dep.getId(),false,null);
            		Set<Long> cdIds = new HashSet<Long>();
            		for(V3xOrgDepartment cd : childDepts){
            			cdIds.add(cd.getId());
            		}
            		for (V3xOrgMember member : externalList) {
            			if (cdIds.contains(member.getOrgDepartmentId())) {
            				members.add(member);
            			}
            		}
                }
                
        	} else {
        		List<V3xOrgMember>  externalList = orgManager.getMemberWorkScopeForExternal(user.getId(), false);// 获取我能看的外部人员
        		for (V3xOrgMember member : externalList) {
        			if (member.getOrgDepartmentId().equals(dep.getId())) {
        				members.add(member);
        			}
        		}
        	}
        	
        }else{
        	if (dep.getIsInternal()) {
        		List<V3xOrgMember> memberList = (List<V3xOrgMember>) OuterWorkerAuthUtil.getCanAccessMembers(AppContext.getCurrentUser().getId(), AppContext.getCurrentUser().getDepartmentId(), AppContext.currentAccountId(), orgManager);
        		if (isDepartment == 1) {//isDepartment==1包含子部门人员
        			String deptIds = dep.getId().toString();
        			List<V3xOrgDepartment> deps = orgManager.getChildDepartments(dep.getId(), false);
        			deps.addAll(orgManager.getChildDepartments(dep.getId(), false, false));
        			for (V3xOrgDepartment d : deps) {
        				deptIds+=d.getId();
        				List<V3xOrgDepartment> externalDept = orgManager.getChildDepartments(d.getId(), false, false); //外部单位
        				for(V3xOrgDepartment ed : externalDept) {
        					deptIds+=ed.getId();
        				}
        			}
        			for(V3xOrgMember m : memberList){
        				if(deptIds.indexOf(m.getOrgDepartmentId().toString())>=0){
        					members.add(m);
        				}else{
        					if(dep.getOrgAccountId().equals(m.getOrgAccountId())){
        						//查看副岗
        						List<MemberPost> secondPostList = m.getSecond_post();
        						for(MemberPost mp : secondPostList){
        							Long secDeptId = mp.getDepId();
        							if(secDeptId != -1 && deptIds.indexOf(secDeptId.toString())>=0){
        								members.add(m);
        								break;
        							}
        						}
        					}else{
        						//查看兼职
        						List<MemberPost> concurrentPostList = m.getConcurrent_post();
        						for(MemberPost mp : concurrentPostList){
        							Long conDeptId = mp.getDepId();
        							if(conDeptId != -1 && deptIds.indexOf(conDeptId.toString())>=0){
        								members.add(m);
        								break;
        							}
        						}
        						
        					}
        				}
        			}
        		} else {//isDepartment==0不包含子部门人员
        			for(V3xOrgMember m : memberList){
        				if(m.getOrgDepartmentId().compareTo(dep.getId())==0){
        					members.add(m);
        				}else{
        					if(dep.getOrgAccountId().equals(m.getOrgAccountId())){
        						//查看副岗
        						List<MemberPost> secondPostList = m.getSecond_post();
        						for(MemberPost mp : secondPostList){
        							Long secDeptId = mp.getDepId();
        							if(secDeptId != -1 && dep.getId().equals(secDeptId)){
        								members.add(m);
        								break;
        							}
        						}
        					}else{
        						//查看兼职
        						List<MemberPost> concurrentPostList = m.getConcurrent_post();
        						for(MemberPost mp : concurrentPostList){
        							Long conDeptId = mp.getDepId();
        							if(conDeptId != -1 && dep.getId().equals(conDeptId)){
        								members.add(m);
        								break;
        							}
        						}
        						
        					}
        				}
        			}
        		}
        	} else {
        		List<V3xOrgMember> externalList = orgManager.getAllExtMembers(accountId);
        		for (V3xOrgMember member : externalList) {
        			if (member.getOrgDepartmentId().equals(dep.getId())) {
        				members.add(member);
        			}
        		}
        	}
        }

        List<V3xOrgMember> member1 = addressBookManager.listDeptMembers(deptId, accountId, members);
        List<WebWithPropV3xOrgMember> allresults = (List<WebWithPropV3xOrgMember>) newFiltWebWithPropV3xOrgMembers(request,(AddressBookSet)((mav.getModel()).get("addressBookSet")),parseList(member1, accountId, deptId, addressBookSet,showType),false,showType);
        List<WebWithPropV3xOrgMember> results = this.pagenate(request, allresults);
        if("card".equals(showType)){
        	//mav.addObject("members",allresults);
        	mav.addObject("memberStr",getMemberJson(allresults));  
        	mav.addObject("resultCount",allresults.size());
        }else{
        	mav.addObject("members", results);
        	mav.addObject("resultCount", results.size());
        }
        
        List<MetadataColumnBO> metadataColumnList = addressBookManager.getCurrentAccountEnableCustomerFields(addressBookSet);
        mav.addObject("bean", metadataColumnList);
        
        return mav;
    }

    /**
     * 显示系统组的人员
     */
    public ModelAndView listSysTeamMembers(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ListSearchHelper.pickupExpression(request, null, "condition", "textfield");
        int addressbookType = ServletRequestUtils.getIntParameter(request, "addressbookType", 1);
        String showType = request.getParameter("showType");
        ModelAndView mav = new ModelAndView(jspView(addressbookType, "listSysMember"));
    	if("card".equals(showType)){
    		mav = new ModelAndView("addressbook/public/addressbookOrgStructure_card");
    	}
        Long teamId = ServletRequestUtils.getLongParameter(request, "tId");
        Long accountId = StringUtils.isNotBlank(request.getParameter("accountId")) ? Long.parseLong(request.getParameter("accountId")) : AppContext.currentAccountId();

        // 判断是否具有发送手机短信的权限(是否显示手机图标)
        boolean isCanSendSMS = false;
        if (SystemEnvironment.hasPlugin("sms")) {
            if (mobileMessageManager.isCanSend(AppContext.currentUserId(), AppContext.currentAccountId())) {
                isCanSendSMS = true;
            }
        }
        mav.addObject("isCanSendSMS", isCanSendSMS);

        AddressBookSet addressBookSet = this.getAddressBookSet(mav, AppContext.currentUserId(), accountId);

        List<V3xOrgMember> members = new ArrayList<V3xOrgMember>();
        List<WebWithPropV3xOrgMember> results = new ArrayList<WebWithPropV3xOrgMember>();
        V3xOrgTeam team = orgManager.getTeamById(teamId);
        List<OrgTypeIdBO> leaderIds = team.getLeaders();
        //List<OrgTypeIdBO> memberIds = team.getMembers();
        List<OrgTypeIdBO> relations = team.getRelatives();
        List<OrgTypeIdBO> supervisors = team.getSupervisors();
        //leaderIds.addAll(memberIds);
        leaderIds.addAll(relations);
        leaderIds.addAll(supervisors);
        for (OrgTypeIdBO memberId : leaderIds) {
            V3xOrgMember member = orgManager.getMemberById(memberId.getLId());
            if (member != null)
                if (members.isEmpty()) {
                    members.add(member);
                } else {
                    if (!members.contains(member))
                        members.add(member);
                }
        }
        //组中 可能包含团队，直接取出团队中所有的人
        List<V3xOrgMember> itemMembers = orgManager.getMembersByTeam(teamId);
        for(V3xOrgMember m : itemMembers){
            if (members.isEmpty()) {
                members.add(m);
            } else {
                if (!members.contains(m))
                    members.add(m);
            }
        }
        
        List<V3xOrgMember> temp = this.filterMemberByAssigned(members);
        List<WebWithPropV3xOrgMember> allresults = new ArrayList<WebWithPropV3xOrgMember>();
        if (members.size() != 0) {
        	allresults = (List<WebWithPropV3xOrgMember>) newFiltWebWithPropV3xOrgMembers(request,(AddressBookSet)((mav.getModel()).get("addressBookSet")),translateList(temp, addressbookType, accountId,showType),false,showType);
            results = this.pagenate(request, allresults);
        }
        
		mav.addObject("resultCount", temp.size());
        if("card".equals(showType)){
        	//mav.addObject("members",allresults);
        	mav.addObject("memberStr",getMemberJson(allresults));  
        }else{
       	    mav.addObject("members", results);
        }
     
        List<MetadataColumnBO> metadataColumnList = addressBookManager.getCurrentAccountEnableCustomerFields(addressBookSet);
        mav.addObject("bean", metadataColumnList);

        return mav;
    }

    /**
     * 显示个人组的人员
     */
    public ModelAndView listOwnTeamMembers(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ListSearchHelper.pickupExpression(request, null, "condition", "textfield");
        int addressbookType = ServletRequestUtils.getIntParameter(request, "addressbookType", 1);
        Long teamId = ServletRequestUtils.getLongParameter(request, "tId");
        String showType = request.getParameter("showType");

        if (1 == addressbookType) {
            ModelAndView mav = new ModelAndView(jspView(addressbookType, "listOwnGroup"));
        	if("card".equals(showType)){
        		mav = new ModelAndView("addressbook/public/addressbookOrgStructure_card");
        	}
            // 判断是否具有发送手机短信的权限(是否显示手机图标)
            boolean isCanSendSMS = false;
            if (SystemEnvironment.hasPlugin("sms")) {
                if (mobileMessageManager.isCanSend(AppContext.currentUserId(), AppContext.currentAccountId())) {
                    isCanSendSMS = true;
                }
            }
            mav.addObject("isCanSendSMS", isCanSendSMS);

            AddressBookSet addressBookSet = this.getAddressBookSet(mav, AppContext.currentUserId(), AppContext.currentAccountId());
            V3xOrgTeam team = orgManager.getTeamById(teamId);
            List<OrgTypeIdBO> memberIds = new UniqueList<OrgTypeIdBO>();
            memberIds.addAll(team.getLeaders());
            memberIds.addAll(team.getMembers());
            List<V3xOrgMember> mems = new ArrayList<V3xOrgMember>();
            for (OrgTypeIdBO memberId : memberIds) {
                mems.add(orgManager.getMemberById(memberId.getLId()));
            }
            List<V3xOrgMember> members = this.filterMemberByAssigned(mems);
            List<WebWithPropV3xOrgMember> allresults = (List<WebWithPropV3xOrgMember>) newFiltWebWithPropV3xOrgMembers(request,(AddressBookSet)((mav.getModel()).get("addressBookSet")),translateList(members, 4, AppContext.currentAccountId(),showType),false,showType);
            List<WebWithPropV3xOrgMember> results = this.pagenate(request, allresults);
            mav.addObject("resultCount", members.size());
            if("card".equals(showType)){
            	//mav.addObject("members",allresults);
            	mav.addObject("memberStr",getMemberJson(allresults));  
            }else{
           	    mav.addObject("members", results);
            }
            
            List<MetadataColumnBO> metadataColumnList = addressBookManager.getCurrentAccountEnableCustomerFields(addressBookSet);
            mav.addObject("bean", metadataColumnList);
            return mav;
        } else if (2 == addressbookType) {
            ModelAndView mav = new ModelAndView(myJspView(addressbookType, "listMembers",showType));
        	if("card".equals(showType)){
        		mav = new ModelAndView("addressbook/private/private_card");
        	}

            // 判断是否具有发送手机短信的权限(是否显示手机图标)
            boolean isCanSendSMS = false;
            if (SystemEnvironment.hasPlugin("sms")) {
                if (mobileMessageManager.isCanSend(AppContext.currentUserId(), AppContext.currentAccountId())) {
                    isCanSendSMS = true;
                }
            }
            mav.addObject("isCanSendSMS", isCanSendSMS);
          
            List<AddressBookMember> members = (List<AddressBookMember>) this.newFiltAddressBookMembers(request,this.addressBookManager.getMembersByTeamId(teamId));
            if("card".equals(showType)){
            	mav.addObject("members",members);
            }else{
            	mav.addObject("members", this.pagenate(request, members));
            }
            mav.addObject("resultCount", members.size());
            
            return mav;
        }
        return null;
    }

    // 显示左边树(部门/系统组/个人组)
    public ModelAndView treeDept(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	int addressbookType = ServletRequestUtils.getIntParameter(request, "addressbookType", 1);

		// best 通讯录支持全集团查看 2020年4月29日 14:02:59 start
    	if (addressbookType == 1) {
    		boolean isRoot = false;
			User user = AppContext.getCurrentUser();
			boolean isGroupVer = true;
			boolean isSameAccount = false;
			Long accId = OrgConstants.GROUPID;
			Long accId2 = user.getAccountId();
			ModelAndView result = new ModelAndView(jspView(addressbookType, "treeDept"));
			List<WebV3xOrgDepartment> resultlist = new ArrayList<WebV3xOrgDepartment>();
	
			List<WebV3xOrgDepartment> inlist = new ArrayList<WebV3xOrgDepartment>();
			List<WebV3xOrgDepartment> externallist = new ArrayList<WebV3xOrgDepartment>();
			List<V3xOrgAccount> childAccountListNew = new ArrayList<V3xOrgAccount>();
			boolean isMyAccount = false;
			List<V3xOrgDepartment> deptlist = null;
	
			List<V3xOrgAccount> childAccountList = orgManager.getChildAccount(accId, false);
			List<V3xOrgAccount> accessableAccounts = orgManager.accessableAccounts(user.getId());
			Map<Long, Boolean> accessaAccountMap = new HashMap<Long, Boolean>();
			for (V3xOrgAccount account : accessableAccounts) {
				accessaAccountMap.put(account.getId(), true);
			}
			for (V3xOrgAccount account : childAccountList) {
				if (accessaAccountMap.get(account.getId()) != null && accessaAccountMap.get(account.getId())) {
					childAccountListNew.add(account);
				}
			}
			Collections.sort(childAccountListNew, CompareSortEntity.getInstance());
			if (isGroupVer) {
				isRoot = true;
			}
			result.addObject("isRoot", isRoot);
			result.addObject("childAccountList", childAccountListNew);
	
			// 根据列表切换到的单位下的所有的部门
			for (V3xOrgAccount account : childAccountListNew) {
				if (deptlist == null) {
					deptlist = new ArrayList<V3xOrgDepartment>();
				}
				deptlist.addAll(orgManager.getAllDepartments(account.getId()));
			}
			if (accId == user.getLoginAccount()) {
				isSameAccount = true;
			}
			if (accId2.longValue() == accId) {
				isMyAccount = true;
			}
	
			Map<String, V3xOrgDepartment> path2DepartmentMap = new HashMap<String, V3xOrgDepartment>();
			for (V3xOrgDepartment v3xOrgDepartment : deptlist) {
				path2DepartmentMap.put(v3xOrgDepartment.getPath(), v3xOrgDepartment);
			}
	
			List<Long> canAccessDepts = orgManager.getDepartmentWorkScopeForExternal(user.getId());
	
			for (V3xOrgDepartment dept : deptlist) {
				if (isMyAccount) {
					if (dept.getIsInternal()) {
						WebV3xOrgDepartment webdept = getWebOrgDepartmentObj(dept, accId, path2DepartmentMap);
						resultlist.add(webdept);
					} else if (canAccessDepts.contains(dept.getId())) {
						WebV3xOrgDepartment webdept = getWebOrgDepartmentObj(dept, accId, path2DepartmentMap);
						resultlist.add(webdept);
					}
				} else {
					if (dept.getIsInternal()) {// 当前登录用户只能看到外单位的内部部门
						WebV3xOrgDepartment webdept = getWebOrgDepartmentObj(dept, accId, path2DepartmentMap);
						resultlist.add(webdept);
					}
				}
			}
			V3xOrgAccount account = orgManager.getAccountById(accId);
			for (WebV3xOrgDepartment d : resultlist) {
				WebV3xOrgDepartment deptTreeNode = new WebV3xOrgDepartment();
				deptTreeNode.setId(d.getV3xOrgDepartment().getId());
				deptTreeNode.setParentId((null == d.getParentId()) ? account.getId() : d.getParentId());
				deptTreeNode.setName(d.getV3xOrgDepartment().getName());
				if (d.getV3xOrgDepartment().getIsInternal() == true) {
					inlist.add(deptTreeNode);
				} else {
					deptTreeNode.setIconSkin("new");
					externallist.add(deptTreeNode);
				}
			}
	
			List<WebV3xOrgAccount> accountTreelist = new ArrayList<WebV3xOrgAccount>();
			WebV3xOrgAccount treeRoot = new WebV3xOrgAccount();
			treeRoot.setParentId(null);
			treeRoot.setId(account.getId());
			treeRoot.setName(account.getName());
			accountTreelist.add(treeRoot);
			for (V3xOrgAccount a : childAccountListNew) {
				WebV3xOrgAccount accountNode = new WebV3xOrgAccount();
				accountNode.setId(a.getId());
				accountNode.setParentId(a.getSuperior());
				accountNode.setName(a.getName());
				accountTreelist.add(accountNode);
				List<V3xOrgDepartment> departments = orgManager.getAllDepartments(a.getId());
				if (departments != null) {
					initChildrenDept(accountTreelist, departments, a.getId(), a.getPath(), a.getId());
				}
			}
			
			try {
				List<WebV3xOrgAccount> removeList = new ArrayList<WebV3xOrgAccount>();
				List<WebV3xOrgAccount> roots = new ArrayList<WebV3xOrgAccount>();
				for (WebV3xOrgAccount each : accountTreelist) {
					if (each.getParentId() == null) {
						roots.add(each);
					}
				}
				outter : for (WebV3xOrgAccount root : roots) {
					int count = 0;
					for (WebV3xOrgAccount each : accountTreelist) {
						if (root.getId() != null && root.getId().equals(each.getParentId())) {
							continue outter;
						} else {
							count++;
						}
					}
					if (count == accountTreelist.size()) {
						removeList.add(root);
					}
				}
				if (!removeList.isEmpty()) {
					for (WebV3xOrgAccount remove : removeList) {
						accountTreelist.remove(remove);
					}
				}
			} catch (Exception e) {
				logger.error("best--过滤数据异常：", e);
			}
			
			request.setAttribute("ffaccountTree", accountTreelist);
			result.addObject("isSameAccount", isSameAccount);
			result.addObject("isGroupVer", isGroupVer);
			result.addObject("account", account);
			result.addObject("deptlist", resultlist);
			result.addObject("inlist", inlist);
			result.addObject("externallist", externallist);
			result.addObject("changeView", true);
	
			request.setAttribute("addressbookType", addressbookType);
			request.setAttribute("accountId", account.getId());
	
			return result;
		} else {
	// best 通讯录支持全集团查看 2020年4月29日 14:02:59 end
	    	
	    	boolean isRoot = false;
	        User user = AppContext.getCurrentUser();
	        //判断是否是集团版本---做集团化通讯录
	        boolean isGroupVer = (Boolean) (SysFlag.sys_isGroupVer.getFlag());//判断是否为集团版 并且不是外部人员
	
	        boolean isSameAccount = false;
	        Long accId = user.getLoginAccount();
	        Long accId2 = user.getAccountId();
	        boolean isAccId = accId.equals(accId2);
	        ModelAndView result = new ModelAndView(jspView(addressbookType, "treeDept"));
	
	        List<WebV3xOrgDepartment> resultlist = null;
	
	        List<WebV3xOrgDepartment> inlist = null;
	        List<WebV3xOrgDepartment> externallist = null;
	        inlist = new ArrayList<WebV3xOrgDepartment>();
	        externallist = new ArrayList<WebV3xOrgDepartment>();
	        //根据树上的下拉列表传来的单位ID
	        //当前登录者是内部人员，可以看到所有部门
	        List<V3xOrgAccount> childAccountListNew = new ArrayList<V3xOrgAccount>();
	        boolean isMyAccount = false;
	        if (user.isInternal()) {
	            resultlist = new ArrayList<WebV3xOrgDepartment>();
	            List<V3xOrgDepartment> deptlist = null;
	
	            String accountId = request.getParameter("accountId");
	            if (Strings.isNotBlank(accountId)) {
	                accId = Long.parseLong(accountId);
	
	                if (orgManager.getRootAccount().getId().equals(accId)) {
	                    List<V3xOrgAccount> childAccountList = orgManager.getChildAccount(accId, false);
	                    List<V3xOrgAccount> accessableAccounts = orgManager.accessableAccounts(user.getId());
	                    Map<Long, Boolean> accessaAccountMap = new HashMap<Long, Boolean>();
	                    for (V3xOrgAccount account : accessableAccounts) {
	                        accessaAccountMap.put(account.getId(), true);
	                    }
	                    for (V3xOrgAccount account : childAccountList) {
	                        if (accessaAccountMap.get(account.getId()) != null && accessaAccountMap.get(account.getId())) {
	                            childAccountListNew.add(account);
	                        }
	                    }
	                    Collections.sort(childAccountListNew, CompareSortEntity.getInstance());
	                    if(isGroupVer){
	                    	isRoot=true;
	                    }
	                    result.addObject("isRoot", isRoot);
	                    result.addObject("childAccountList", childAccountListNew);
	                }
	
	                //根据列表切换到的单位下的所有的部门
	                deptlist = orgManager.getAllDepartments(accId);
	                if (isAccId && accId == user.getLoginAccount()) {
	                    isSameAccount = true;
	                }
	                if (accId2.longValue() == accId) {
	                    isMyAccount = true;
	                }
	            } else {
	                //当前登陆人的单位下所有的部门
	                deptlist = orgManager.getAllDepartments(accId);
	                if (isAccId) {
	                    isSameAccount = true;
	                }
	                isMyAccount = true;
	            }
	
	            Map<String, V3xOrgDepartment> path2DepartmentMap = new HashMap<String, V3xOrgDepartment>();
	            for (V3xOrgDepartment v3xOrgDepartment : deptlist) {
	                path2DepartmentMap.put(v3xOrgDepartment.getPath(), v3xOrgDepartment);
	            }
	
	            List<Long> canAccessDepts = orgManager.getDepartmentWorkScopeForExternal(user.getId());
	
	            for (V3xOrgDepartment dept : deptlist) {
	                if (isMyAccount) {
	                    if (dept.getIsInternal()) {
	                        WebV3xOrgDepartment webdept = getWebOrgDepartmentObj(dept, accId, path2DepartmentMap);
	                        resultlist.add(webdept);
	                    } else if (canAccessDepts.contains(dept.getId())) {
	                        WebV3xOrgDepartment webdept = getWebOrgDepartmentObj(dept, accId, path2DepartmentMap);
	                        resultlist.add(webdept);
	                    }
	                } else {
	                    if (dept.getIsInternal()) {//当前登录用户只能看到外单位的内部部门
	                        WebV3xOrgDepartment webdept = getWebOrgDepartmentObj(dept, accId, path2DepartmentMap);
	                        resultlist.add(webdept);
	                    }
	                }
	            }
	        } else {
	            resultlist = OuterWorkerAuthUtil.getOuterSubDeptList(result, user, accId, orgManager);
	        }
	
	        /******************/
	        /*
	        List<V3xOrgAccount> accessableAccounts = null;
	        Set<Long> accessableAccountIds  = new HashSet<Long>();
	        V3xOrgAccount rootAccount = this.orgManager.getRootAccount();
	        List<V3xOrgAccount> _accessableAccounts = this.orgManager.accessableAccounts(user.getId());//获取可访问的单位列表
	        for (V3xOrgAccount a : _accessableAccounts) {
	        	accessableAccountIds.add(a.getId());
	        }
	        boolean isAccountInGroup = this.orgManager.isAccountInGroupTree(user.getLoginAccount());
	        accessableAccounts = new ArrayList<V3xOrgAccount>(_accessableAccounts.size());
	        for (V3xOrgAccount a : _accessableAccounts) {
	        	V3xOrgAccount _account = new V3xOrgAccount(a);//此处修改上级id,采用new一个单位的方法,以免影响缓存
	        	if(_account.getSuperior().longValue() != -1 && !accessableAccountIds.contains(_account.getSuperior())){
	        		_account.setSuperior(isAccountInGroup ? rootAccount.getId() : -1);//处理无根单位
	        	}
	        	accessableAccounts.add(_account);
	        }
	        if (user.getLoginAccount() != 1 && isAccountInGroup) {
	        	accessableAccounts.add(new V3xOrgAccount(rootAccount));
	        }
	        boolean isGroupAccessable = Functions.isGroupAccessable(user.getLoginAccount());
	        if(!isGroupAccessable){
	        	accessableAccounts.remove(rootAccount);
	        }
	        result.addObject("accountsList", accessableAccounts);
	        */
	        /**********************/
	        V3xOrgAccount account = orgManager.getAccountById(accId);
	        for (WebV3xOrgDepartment d : resultlist) {
	        	WebV3xOrgDepartment deptTreeNode = new WebV3xOrgDepartment();
	        	deptTreeNode.setId(d.getV3xOrgDepartment().getId());
	        	deptTreeNode.setParentId((null==d.getParentId())?account.getId():d.getParentId());
	        	deptTreeNode.setName(d.getV3xOrgDepartment().getName());
	            if (d.getV3xOrgDepartment().getIsInternal() == true) {
	                inlist.add(deptTreeNode);
	            } else {
	            	deptTreeNode.setIconSkin("new");
	                externallist.add(deptTreeNode);
	            }
	        }
	        
	        if(isRoot && isGroupVer){
	        	List<WebV3xOrgAccount> accountTreelist = new ArrayList<WebV3xOrgAccount>();
	        	WebV3xOrgAccount treeRoot = new WebV3xOrgAccount();
	            treeRoot.setParentId(null);
	            treeRoot.setId(account.getId());
	            treeRoot.setName(account.getName());
	            accountTreelist.add(treeRoot);
	            for (V3xOrgAccount a : childAccountListNew) {
	            	WebV3xOrgAccount accountNode = new WebV3xOrgAccount();
	            	accountNode.setId(a.getId());
	            	accountNode.setParentId(a.getSuperior());
	            	accountNode.setName(a.getName());
	            	accountTreelist.add(accountNode);
	            }
	            request.setAttribute("ffaccountTree", accountTreelist);
	        }else{
	        	List<WebV3xOrgDepartment> deptTreelist = new ArrayList<WebV3xOrgDepartment>();
	            WebV3xOrgDepartment treeRoot = new WebV3xOrgDepartment();
	            treeRoot.setParentId(null);
	            treeRoot.setId(account.getId());
	            treeRoot.setName(account.getName());
	            deptTreelist.add(treeRoot);
	        	deptTreelist.addAll(inlist);
	        	deptTreelist.addAll(externallist);
	        	request.setAttribute("ffaccountTree", deptTreelist);
	        }
	        //判断是否是集团版本---做集团化通讯录
	        /*boolean isGroupAccessable = Functions.isGroupAccessable(user.getLoginAccount());
	        if (isGroupVer && user.isInternal()) {
	            List<V3xOrgAccount> accountsList = orgManager.accessableAccounts(user.getId());
	            V3xOrgAccount rootAccount = orgManager.getRootAccount();
	
	            if (isGroupAccessable) {
	                accountsList.add(rootAccount);
	            }
	
	            Collections.sort(accountsList, CompareSortEntity.getInstance());
	            result.addObject("accountsList", accountsList);
	        }*/
	        result.addObject("isSameAccount", isSameAccount);
	        result.addObject("isGroupVer", isGroupVer);
	        result.addObject("account", account);
	        result.addObject("deptlist", resultlist);
	        result.addObject("inlist", inlist);
	        result.addObject("externallist", externallist);
	 
	        request.setAttribute("addressbookType", addressbookType);
	        request.setAttribute("accountId", account.getId());
	
	        return result;
    	}
    }
    
    private void initChildrenDept(List<WebV3xOrgAccount> accounts, List<V3xOrgDepartment> departments, Long parentId, String parnetPath, Long belongAccountId) throws Exception {
    	if (departments != null && parentId != null && Strings.isNotBlank(parnetPath) && belongAccountId != null) {
    		for (V3xOrgDepartment dept : departments) {
    			if (dept.getId() != null && parnetPath.equals(dept.getParentPath())) {
					WebV3xOrgAccount departmentNode = new WebV3xOrgAccount();
					departmentNode.setId(dept.getId());
					departmentNode.setParentId(parentId);
					departmentNode.setCode("true");
					departmentNode.setAccountLevel(belongAccountId.toString());
					departmentNode.setName(dept.getName());
					accounts.add(departmentNode);
    				List<V3xOrgDepartment> childDepartments = orgManager.getChildDepartments(dept.getId(), true);
    				if (childDepartments != null && !childDepartments.isEmpty()) {
    					initChildrenDept(accounts, childDepartments, dept.getId(), dept.getPath(), belongAccountId);
    				}
    			}
    		}
    	}
    }

    private WebV3xOrgDepartment getWebOrgDepartmentObj(V3xOrgDepartment dept, Long accId, Map<String, V3xOrgDepartment> path2DepartmentMap) {
        V3xOrgDepartment parent = null;

        String path = dept.getPath();
        if (Strings.isNotBlank(path)) {
            if (path.length() > 4) {
                String parentpath = path.substring(0, path.length() - 4);
                parent = path2DepartmentMap.get(parentpath);
            }
        }

        WebV3xOrgDepartment webdept = new WebV3xOrgDepartment();
        webdept.setV3xOrgDepartment(dept);
        if (null != parent) {
            webdept.setParentId(parent.getId());
            webdept.setParentName(parent.getName());
        }
        return webdept;
    }

    /**
     * 系统组。左边树结构数据显示
     * @author lucx
     *
     */
    public ModelAndView treeSysTeam(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int addressbookType = ServletRequestUtils.getIntParameter(request, "addressbookType", 1);
        ModelAndView mav = new ModelAndView(jspView(addressbookType, "treeSysTeam"));
        User user = AppContext.getCurrentUser();
        Long accId = user.getLoginAccount();

        boolean isSameAccount = false;

        //根据树上的下拉列表传来的单位ID
        //		当前登录者是内部人员，可以看到所有部门

        List<V3xOrgTeam> teamlist = null;
        String accountId = request.getParameter("accountId");
        if (accountId != null && !"".equals(accountId)) {
            accId = Long.parseLong(accountId);
            //根据列表切换到的单位下的所有的组
            teamlist = orgManager.getTeamsByMember(user.getId(), accId);
            if (accId == user.getLoginAccount()) {
                isSameAccount = true;
            }
        } else {
            //当前登陆人的单位下所有的组
            teamlist = orgManager.getTeamsByMember(user.getId(), accId);
            isSameAccount = true;
        }

        for (Iterator<V3xOrgTeam> iter = teamlist.iterator(); iter.hasNext();) {
            V3xOrgTeam team = iter.next();
            if (team.getType() == OrgConstants.TEAM_TYPE.PERSONAL.ordinal() || team.getType() == OrgConstants.TEAM_TYPE.DISCUSS.ordinal() || team.getType() == OrgConstants.TEAM_TYPE.COLTEAM.ordinal()) {
                iter.remove();
            }
            if (!team.isValid()) {
                iter.remove();
            }
            //修改 去掉公开系统组不能被不在部门范围内的人看到。by wusb at 2010-12-21
            if (team.getDepId() != null && team.getDepId() != -1) {
                List<V3xOrgDepartment> depts = orgManager.getChildDepartments(team.getDepId(), false);
                List<Long> deptIds = new ArrayList<Long>();
                deptIds.add(team.getDepId());
                for (V3xOrgDepartment dept : depts) {
                    deptIds.add(dept.getId());
                }
                if (!deptIds.contains(user.getDepartmentId()) && !isMyTeam(user.getId(), team)) {
                    iter.remove();
                }
            }
        }

		Collections.sort(teamlist, new Comparator<V3xOrgTeam>() {
			public int compare(V3xOrgTeam c1, V3xOrgTeam c2) {
				//type: 1个人组; 3项目组; 2系统组; 4讨论组
				int type1 = c1.getType();
				int type2 = c2.getType();

                if (type1 == 1) type1 = -2;
                if (type1 == 3) type1 = -1;
                if (type2 == 1) type2 = -2;
                if (type2 == 3) type2 = -1;

				if(type1 == type2){
                  if ((c1.getSortId() == null) && (c2.getSortId() == null)) {
                    return 0;
				}
                  if (c1.getSortId() == null) {
                    return 1;
                  }
                  if (c2.getSortId() == null) {
                    return -1;
				}
                  long id1 = c1.getSortId().longValue();
                  long id2 = c2.getSortId().longValue();
                  if(id1==id2){
                	  return c1.getName().compareToIgnoreCase(c2.getName());
                  }else{
                	  return id1 < id2 ? -1 : id1 > id2 ? 1 : 0;
                  }
                }
                return type1 < type2 ? -1 : 1;
			}
		});

        // 判断是否是集团版本---做集团化通讯录
        boolean isGroupVer = (Boolean) (SysFlag.sys_isGroupVer.getFlag());// 判断是否为集团版
        if (isGroupVer) {
            List<V3xOrgAccount> accountsList = orgManager.accessableAccounts(user.getId());
            mav.addObject("accountsList", accountsList);
        }

        mav.addObject("isSameAccount", isSameAccount);
        mav.addObject("isGroupVer", isGroupVer);

        List<WebV3xOrgTeam> results = translateV3xOrgTeam(teamlist);
        V3xOrgAccount account = orgManager.getAccountById(accId);
        mav.addObject("account", account);
        mav.addObject("teamlist", results);
        
        //root jiedian
        List<WebV3xOrgTeam> treeTeamList = new ArrayList<WebV3xOrgTeam>();
        WebV3xOrgTeam teamRoot = new WebV3xOrgTeam();
        teamRoot.setId(-1L);
        teamRoot.setParentId(null);
        teamRoot.setName(account.getName());
        treeTeamList.add(teamRoot);
        
        for(WebV3xOrgTeam w : results){
        	WebV3xOrgTeam teamNode = new WebV3xOrgTeam();
        	teamNode.setId(w.getV3xOrgTeam().getId());
        	teamNode.setName(w.getV3xOrgTeam().getName());
        	teamNode.setParentId(-1L);
        	treeTeamList.add(teamNode);
        }
        
        request.setAttribute("ffaccountTree", treeTeamList);
        request.setAttribute("addressbookType", addressbookType);
        request.setAttribute("accountId", accountId);
        return mav;
    }

    private boolean isMyTeam(Long memberId, V3xOrgTeam team) {
        if (team.getMembers().contains(memberId))
            return true;
        if (team.getLeaders().contains(memberId))
            return true;
        if (team.getSupervisors().contains(memberId))
            return true;
        if (team.getRelatives().contains(memberId))
            return true;
        return false;
    }

    /**
     * 个人组，私人通讯录，左边树结构数据
     * @author lucx
     *
     */
    public ModelAndView treeOwnTeam(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int addressbookType = ServletRequestUtils.getIntParameter(request, "addressbookType", 1);
        ModelAndView mav = new ModelAndView(jspView(addressbookType, "treeOwnTeam"));
        User user = AppContext.getCurrentUser();

        //root jiedian
        List<WebV3xOrgTeam> treeTeamList = new ArrayList<WebV3xOrgTeam>();
        WebV3xOrgTeam teamRoot = new WebV3xOrgTeam();
        teamRoot.setId(-2L);
        teamRoot.setParentId(null);
        if (1 == addressbookType) {// 员工
        	teamRoot.setName(ResourceUtil.getString("addressbook.team.personal.label"));
        }else{
        	teamRoot.setName(ResourceUtil.getString("addressbook.zu.label"));
        }
        treeTeamList.add(teamRoot);
        
        if (1 == addressbookType) {// 员工
            V3xOrgAccount account = orgManager.getAccountById(user.getLoginAccount());
            List<V3xOrgTeam> teamlist = orgManager.getTeamsByOwner(user.getId(), user.getLoginAccount());
            List<WebV3xOrgTeam> results = translateV3xOrgTeam(teamlist);
            mav.addObject("account", account);
            mav.addObject("teamlist", results);
            for(WebV3xOrgTeam w : results){
            	WebV3xOrgTeam teamNode = new WebV3xOrgTeam();
            	teamNode.setId(w.getV3xOrgTeam().getId());
            	teamNode.setName(w.getV3xOrgTeam().getName());
            	teamNode.setParentId(-2L);
            	treeTeamList.add(teamNode);
            }
        } else if (2 == addressbookType) { // 外部联系人
            List<AddressBookTeam> teamList = new ArrayList<AddressBookTeam>();
            teamList = addressBookManager.getTeamsByCreatorId(user.getId());
            AddressBookTeam abt = new AddressBookTeam();
            abt.setId(-1L);
            abt.setType(2);
            Locale locale = LocaleContext.getLocale(request);
            String resource = "com.seeyon.v3x.addressbook.resource.i18n.AddressBookResources";
            abt.setName(ResourceBundleUtil.getString(resource, locale, "addressbook.unfiled.address.label"));
            abt.setCreatorId(user.getId());
            abt.setCreatorName(user.getName());
            if (teamList != null) {
                teamList.add(abt);
            } else {
                teamList = new ArrayList<AddressBookTeam>();
                teamList.add(abt);
            }

            mav.addObject("teamlist", teamList);
            
            for(AddressBookTeam a : teamList){
            	WebV3xOrgTeam teamNode = new WebV3xOrgTeam();
            	teamNode.setId(a.getId());
            	teamNode.setName(a.getName());
            	teamNode.setParentId(-2L);
            	treeTeamList.add(teamNode);
            }
        }
        request.setAttribute("ffaccountTree", treeTeamList);
        request.setAttribute("addressbookType", addressbookType);
        return mav;
    }

    // 显示个人详细信息(包含附加属性)
    public ModelAndView viewMember(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int addressbookType = ServletRequestUtils.getIntParameter(request, "addressbookType", 1);
        ModelAndView mav = new ModelAndView(jspView(addressbookType, "viewMember"));
        String memberid = request.getParameter("mId");
        String teamid = request.getParameter("tId");
        Long memberId = 0L;
        if (Strings.isNotBlank(memberid)) {
            memberId = Long.parseLong(memberid);
        }
        if (1 == addressbookType) { // 员工
            //获取员工基本信息
            V3xOrgMember member = orgManager.getMemberById(memberId);
            if (member == null) {
                response.setContentType("text/html;charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.println("<script type='text/javascript'>");
                out.println("alert(parent.v3x.getMessage(\"ADDRESSBOOKLang.addressbook_delete_member_label\"))");
                out.println("parent.window.close();");
                out.println("</script>");
                return null;
            }
            String aId = request.getParameter("accountId");
            Long accountId = 0l;
            if (Strings.isBlank(aId)) {
                accountId = AppContext.getCurrentUser().getAccountId();
            }

            //兼职岗位
            StringBuilder deptpostbuffer = new StringBuilder();
            if (accountId != null) {
                //得到所有兼职单位
                List<V3xOrgAccount> conAccount = orgManager.concurrentAccount(member.getId());
                for (V3xOrgAccount account : conAccount) {
                    Long conAccountId = account.getId();
                    //单位下的兼职
                    Map<Long, List<MemberPost>> map = orgManager.getConcurentPostsByMemberId(conAccountId, member.getId());
                    if (!map.isEmpty()) {
                        Set<Long> depList = map.keySet();
                        for (Long depid : depList) {
                            V3xOrgDepartment v3xdept = orgManager.getDepartmentById(depid);
                            List<MemberPost> conPostList = map.get(depid);
                            for (MemberPost conPost : conPostList) {
                                StringBuilder sbsuffer = new StringBuilder();
                                Long conPostId = conPost.getPostId();
                                if (conPostId == null)
                                    continue;
                                V3xOrgPost v3xpost = orgManager.getPostById(conPostId);
                                sbsuffer.append("(" + account.getName() + ")");
                                sbsuffer.append(v3xdept.getName());
                                sbsuffer.append("-");
                                sbsuffer.append(v3xpost.getName());
                                if (deptpostbuffer.length() != 0) {
                                    deptpostbuffer.append(",");
                                }
                                deptpostbuffer.append(sbsuffer.toString());
                            }

                        }
                    }
                }
            }
            mav.addObject("specialSecondPost", deptpostbuffer.toString());

            //获取员工联系方式
            //ContactInfoBO contactInfo = hrApi.getContactInfoByMemberId(memberId);
            WebStaffInfo webStaffInfos = this.translateV3xOrgMembers(member, null);
            //~~~~~~~~~~~~~办公电话的扩展属性
            //orgManager.loadEntityProperty(member);
            String officeNum = member.getProperty("officeNum") == null ? "" : member.getProperty("officeNum").toString();
            mav.addObject("officeNum", officeNum);
            //~~~~~~~~~~~~~~~`
            mav.addObject("member", webStaffInfos);
            //.addObject("contact", contactInfo);
            mav.addObject("tel", member);
            String isDetail = request.getParameter("isDetail");
            boolean readOnly = false;
            if (null != isDetail && "readOnly".equals(isDetail)) {
                readOnly = true;
                mav.addObject("readOnly", readOnly);
            }

        } else if (2 == addressbookType) { // 外部联系人
            AddressBookMember member = new AddressBookMember();
            User user = AppContext.getCurrentUser();
            if (0L != memberId)
                member = this.addressBookManager.getMember(memberId);
            mav.addObject("member", member);
            mav.addObject("memberId", user.getId());//人员的ID
            mav.addObject("readOnly", false);
            if (Strings.isNotBlank(teamid)) {
                mav.addObject("tId", teamid);
            }
            boolean isCreated = false;
            if (0L == memberId) {
                isCreated = true;
            } else {
                if (request.getParameter("edit") != null) {
                    mav.addObject("disabled", false);
                } else {
                    mav.addObject("disabled", true);
                }
            }
            List<AddressBookTeam> teams = this.addressBookManager.getTeamsByCreatorId(AppContext.getCurrentUser().getId());
            mav.addObject("teams", teams);
            mav.addObject("isCreated", isCreated);
        }

        //HR枚举
        Map<String, CtpEnumBean> hrMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.hr);
        mav.addObject("hrMetadata", hrMetadata);
        return mav;
    }

    /**
     * 验证
     */
    public ModelAndView isExist(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int type = ServletRequestUtils.getIntParameter(request, "type", 0);
        String mail = request.getParameter("mail");
        String memberId = request.getParameter("memberId");
        String category = request.getParameter("category");
        String ownTeam = request.getParameter("ownTeam");
        User user = AppContext.getCurrentUser();
        Long createId = user.getId();
        Long accountId = user.getLoginAccount();
        boolean isAjax = ServletRequestUtils.getRequiredBooleanParameter(request, "ajax");
        boolean isExist = false;
        if (type == 3) {
            isExist = addressBookManager.isExist(type, mail, createId, accountId, memberId);
        } else if (type == 2) {
            isExist = addressBookManager.isExist(type, category, createId, accountId, memberId);
        } else if (type == 1) {
            isExist = addressBookManager.isExist(type, ownTeam, createId, accountId, memberId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt("isExist", isExist);

        String view = null;
        if (isAjax)
            view = this.getJsonView();
        return new ModelAndView(view, "json", jsonObject);
    }

    /**
     * 更新人员信息，和私人通讯录的人员信息
     * @author lucx
     *
     */
    public ModelAndView updateMember(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int addressbookType = ServletRequestUtils.getIntParameter(request, "addressbookType", 1);
        if (1 == addressbookType) { // 员工
            WebWithPropV3xOrgMember model = new WebWithPropV3xOrgMember();
            bind(request, model);
            Long memberId = ServletRequestUtils.getLongParameter(request, "id");
            Long orgAccountId = ServletRequestUtils.getLongParameter(request, "orgAccountId");
            V3xOrgMember member = orgManager.getMemberById(memberId);
            member.setOrgAccountId(orgAccountId);
            member.setProperties(model.properties());
            orgManagerDirect.updateMember(member);
        } else if (2 == addressbookType) { // 外部联系人
            boolean isCreated = ServletRequestUtils.getBooleanParameter(request, "isCreated", false);
            String crtId = request.getParameter("categoryId");
            AddressBookMember member = new AddressBookMember();
            bind(request, member);
            member.setCategory(NumberUtils.toLong(crtId, -1l));
            member.setCreatorId(AppContext.getCurrentUser().getId());
            member.setCreatorName(AppContext.getCurrentUser().getLoginName());
            Date operatingTime = new Date();
            member.setCreatedTime(operatingTime);
            member.setModifiedTime(operatingTime);

            // 修改到其它联系组时
            boolean categoryChanged = !isCreated && (NumberUtils.toLong(request.getParameter("oldCategoryId")) != member.getCategory().longValue());

            // 修改姓名时
            boolean nameChanged = false;
            String oldName = request.getParameter("oldName");
            if (StringUtils.isNotBlank(oldName)) {
                nameChanged = !isCreated && !oldName.equals(member.getName());
            }

            if ((isCreated || categoryChanged || nameChanged) && addressBookManager.isExistSameUserName(member, AppContext.getCurrentUser().getId())) {
                response.setContentType("text/html;charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.println("<script type='text/javascript'>");
                out.println("alert(parent.v3x.getMessage(\"ADDRESSBOOKLang.addressbook_isexist_name\"));");
                out.println("</script>");
                out.flush();
                return null;
            }

            if (isCreated) {
                this.addressBookManager.addMember(member);
            } else {
                this.addressBookManager.updateMember(member);
            }

            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script type='text/javascript'>");
            out.println("var rv = [\"" + member.getId() + "\", \"" + Strings.escapeJavascript(member.getName()) + "\"];");
            out.println("parent.window.transParams.parentWin.newMemberCollBack(rv);");
            out.println("</script>");

            return null;
        }
        // 提示用户操作成功
        return super.redirectModelAndView("/addressbook.do?method=home&addressbookType=" + addressbookType+Functions.csrfSuffix(), "parent.parent");
    }

    /**
     * 删除私人通讯录的人员
     * @author lucx
     *
     */
    public ModelAndView destroyMember(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int addressbookType = ServletRequestUtils.getIntParameter(request, "addressbookType", 1);
        if (2 == addressbookType) {
            String mIdStr = request.getParameter("mIds");
            this.addressBookManager.removeCategoryMembersByIds(AppContext.getCurrentUser().getId(), this.toLongList(mIdStr));
            this.addressBookManager.removeMembersByIds(AppContext.getCurrentUser().getId(), this.toLongList(mIdStr));
        }
        return super.redirectModelAndView("/addressbook.do?method=home&addressbookType=" + addressbookType+Functions.csrfSuffix(), "parent");
    }

    // 个人组/类别
    public ModelAndView viewOwnTeam(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int addressbookType = ServletRequestUtils.getIntParameter(request, "addressbookType", 1);

        ModelAndView mav = new ModelAndView(jspView(addressbookType, "viewOwnTeam"));

        String id = request.getParameter("tId");
        if (id != null && !"".equals(id)) {
            if (addressbookType != 2) {
                V3xOrgTeam team = orgManager.getTeamById(Long.valueOf(id));
                List<OrgTypeIdBO> teamLeaderIDs = team.getMemberList(OrgConstants.TeamMemberType.Leader.ordinal());
                List<OrgTypeIdBO> teamMemIDs = team.getMemberList(OrgConstants.TeamMemberType.Member.ordinal());
                StringBuilder str1 = new StringBuilder();
                StringBuilder str2 = new StringBuilder();
                StringBuilder str3 = new StringBuilder();
                StringBuilder str4 = new StringBuilder();
                for (OrgTypeIdBO leaderid : teamLeaderIDs) {
                    str1.append(orgManager.getMemberById(leaderid.getLId()).getName());
                    str1.append(",");
                    str3.append(leaderid.getId());
                    str3.append(",");
                }
                for (OrgTypeIdBO memberid : teamMemIDs) {
                    str2.append(orgManager.getMemberById(memberid.getLId()).getName());
                    str2.append(",");
                    str4.append(memberid.getId());
                    str4.append(",");
                }
                mav.addObject("teamLeaderIDs", Strings.isNotBlank(str3.toString()) ? str3.deleteCharAt(str3.lastIndexOf(",")).toString() : null);
                mav.addObject("teamMemIDs", Strings.isNotBlank(str4.toString()) ? str4.deleteCharAt(str4.lastIndexOf(",")).toString() : null);
                mav.addObject("leaderNames", Strings.isNotBlank(str1.toString()) ? str1.deleteCharAt(str1.lastIndexOf(",")).toString() : null);
                mav.addObject("memberNames", Strings.isNotBlank(str2.toString()) ? str2.deleteCharAt(str2.lastIndexOf(",")).toString() : null);
                mav.addObject("team", team);
                mav.addObject("tId", team.getId());
            } else {
                AddressBookTeam team = addressBookManager.getTeam(Long.valueOf(id));
                mav.addObject("team", team);
                mav.addObject("tId", team.getId());
            }
        } else
            mav.addObject("isNew", "New");

        return mav;
    }

    /**
     * 新建\更新   --个人组，私人组
     * @author lucx
     *
     */
    public ModelAndView newOwnTeam(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int addressbookType = ServletRequestUtils.getIntParameter(request, "addressbookType", 1);
        User user = AppContext.getCurrentUser();
        String id = "";
        String name = "";
        if (4 == addressbookType) {
            V3xOrgTeam team = new V3xOrgTeam();
            bind(request, team);

            team.setOrgAccountId(user.getLoginAccount());
            team.setOwnerId(user.getId());
            team.setType(OrgConstants.TEAM_TYPE.PERSONAL.ordinal());
            team.setDepId(user.getDepartmentId());

            String leaderStr = request.getParameter("teamLeaderIDs");
            List<OrgTypeIdBO> leaders = toOrgTypeIdBOList(leaderStr);
            team.addTeamMember(leaders, OrgConstants.TeamMemberType.Leader.ordinal());

            String memberStr = request.getParameter("teamMemIDs");
            List<OrgTypeIdBO> members = toOrgTypeIdBOList(memberStr);
            team.addTeamMember(members, OrgConstants.TeamMemberType.Member.ordinal());
            team.setDescription(request.getParameter("memo"));
            String isNew = request.getParameter("isNew");
            if (isNew != null && "New".equals(isNew)) {
                team.setIdIfNew();
                orgManagerDirect.addTeam(team);
                isNew = "true";
            } else {
            	if(!(team.getName()).equals(orgManager.getTeamById(team.getId()).getName())){
            		isNew = "true";
            	}
                orgManagerDirect.updateTeam(team);
            }
            id = team.getId().toString();
            name = team.getName();
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script type='text/javascript'>");
            out.println("var rv = [\"" + id + "\", \"" + Strings.escapeJavascript(name) + "\", \"" + Strings.escapeJavascript(isNew) + "\"];");
            out.println("parent.window.transParams.parentWin.newCategoryCollBack(rv);");
            out.println("</script>");
        } else if (2 == addressbookType) {
            AddressBookTeam category = new AddressBookTeam();
            bind(request, category);

            // add category
            category.setCreatorId(user.getId());
            category.setCreatorName(user.getLoginName());
            category.setType(AddressBookTeam.TYPE_CATEGORY);
            Date createdTime = new Date();
            category.setCreatedTime(createdTime);
            category.setModifiedTime(createdTime);

            String isNew = request.getParameter("isNew");
            if (isNew != null && "New".equals(isNew)) {
                this.addressBookManager.addTeam(category);
            } else {
                this.addressBookManager.updateTeam(category);
            }
            id = category.getId().toString();
            name = category.getName();
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script type='text/javascript'>");
            out.println("var rv = [\"" + id + "\", \"" + Strings.escapeJavascript(name) + "\"];");
            out.println("parent.window.transParams.parentWin.newPrivCategoryCollBack(rv);");
            out.println("</script>");
        }

        return null;
    }

    /**
     * 删除个人组和私人通讯录组
     * @author lucx
     *
     */
    public ModelAndView destroyOwnTeam(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int addressbookType = ServletRequestUtils.getIntParameter(request, "addressbookType", 1);
        Long teamId = ServletRequestUtils.getLongParameter(request, "tId");
        if (4 == addressbookType) {
            V3xOrgTeam team = orgManager.getTeamById(teamId);
            orgManagerDirect.deleteTeam(team);
        } else if (2 == addressbookType) {
            this.addressBookManager.removeTeamById(teamId);
        }
        return super.redirectModelAndView("/addressbook.do?method=home&addressbookType=" + addressbookType+Functions.csrfSuffix(), "parent");
    }

    @SuppressWarnings("unchecked")
    /**
     * 查询方法
     */
    public ModelAndView search(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int addressbookType = ServletRequestUtils.getIntParameter(request, "addressbookType", 1);
        String condition = request.getParameter("condition");
        String searchType = request.getParameter("textfield").trim();
        String accId = request.getParameter("accountId");//判断是否是切换单位传来的ID 。
        String click = request.getParameter("click");
        ModelAndView mav = new ModelAndView(myJspView(addressbookType, "listMembers",""));
        User user = AppContext.getCurrentUser();
        Long accountId = AppContext.getCurrentUser().getLoginAccount();
        if (Strings.isNotBlank(accId)) {
            accountId = Long.parseLong(accId);
        }
        List<V3xOrgLevel> levellist = orgManager.getAllLevels(accountId);
        if (levellist == null) {
            levellist = new ArrayList<V3xOrgLevel>();
        }
        mav.addObject("levellist", levellist);

        if (null != condition && ("name".equals(condition) || "tel".equals(condition))) { //根据姓名查询
            if (1 == addressbookType) {//1.代表员工通讯录
                List<V3xOrgMember> members = null;

                if (Strings.isNotBlank(click) && !"[object]".equals(click)) {
                    if ("dept".equals(click)) {
                        String mem = request.getParameter("mem");
                        if (mem != null && "all".equals(mem))
                            members = this.filterMemberByAssigned(this.getAllMembersByLoginAccount(accountId));
                        else {
                            Long deptId = ServletRequestUtils.getLongParameter(request, "deptId");
                            V3xOrgDepartment department = orgManager.getDepartmentById(deptId);
                            if (department != null) {
                                if (department.getIsInternal()) {
                                    members = this.filterMemberByAssigned(orgManagerDirect.getMembersByDepartment(deptId, accountId, false, true, false));
                                } else {
                                    members = this.filterMemberByAssigned(orgManager.getExtMembersByDepartment(deptId, false));
                                }
                            }
                        }
                    }
                } else if (Strings.isBlank(click)) {
                    members = this.filterMemberByAssigned(orgManagerDirect.getMembersByDepartment(user.getDepartmentId(), user.getAccountId(), false, true, false));
                }

                List<V3xOrgMember> result = this.departmentByName(condition, members, searchType);
                List<WebWithPropV3xOrgMember> results = translateList(CommonTools.pagenate(result), 1, accountId,"");
                mav.addObject("members", results);
            }
            if (2 == addressbookType) {//2.代表私人通讯录
                List<AddressBookMember> members = new ArrayList<AddressBookMember>();
                List<AddressBookMember> result = new ArrayList<AddressBookMember>();
                if ("name".equals(condition)) {
                    result = this.addressBookManager.getMemberByName(searchType);
                } else {
                    result = this.addressBookManager.getMemberByTel(searchType);
                }
                members = CommonTools.pagenate(result);
                mav.addObject("members", members);
            }
            if (3 == addressbookType) {//3.代表系统组
                List<WebWithPropV3xOrgMember> results = new ArrayList<WebWithPropV3xOrgMember>();
                List<V3xOrgTeam> teamlist = new ArrayList<V3xOrgTeam>();
                List<WebWithPropV3xOrgMember> members = new ArrayList<WebWithPropV3xOrgMember>();
                teamlist = orgManager.getTeamsByMember(user.getId(), accountId);//有我的项目组和系统私有组，和公开组

                members = this.getAllMembersByName(teamlist, searchType, condition);
                results = CommonTools.pagenate(members);
                mav.addObject("members", results);
            }
            if (4 == addressbookType) {//4.代表个人组
                List<WebWithPropV3xOrgMember> results = new ArrayList<WebWithPropV3xOrgMember>();
                List<V3xOrgTeam> teamlist = new ArrayList<V3xOrgTeam>();
                List<WebWithPropV3xOrgMember> members = new ArrayList<WebWithPropV3xOrgMember>();
                teamlist = orgManager.getAllTeams(AppContext.getCurrentUser().getId());
                members = this.getAllMembersByName(teamlist, searchType, condition);
                results = CommonTools.pagenate(members);
                mav.addObject("members", results);
            }
        }
        if (null != condition && "level".equals(condition)) { //根据职务级别查询
            if (1 == addressbookType) {
                List<V3xOrgMember> members = this.filterMemberByAssigned(this.getAllMembersByLoginAccount(accountId));
                List<V3xOrgMember> result = this.departmentByLevel(members, searchType);
                List<WebWithPropV3xOrgMember> results = translateList(CommonTools.pagenate(result), addressbookType, accountId,"");
                mav.addObject("members", results);

            }
            if (2 == addressbookType) {
                List<AddressBookMember> members = CommonTools.pagenate(this.addressBookManager.getMemberByLevelName(searchType));
                mav.addObject("members", members);
            }
            if (3 == addressbookType) {
                List<WebWithPropV3xOrgMember> results = new ArrayList<WebWithPropV3xOrgMember>();
                List<V3xOrgTeam> teamlist = new ArrayList<V3xOrgTeam>();
                List<WebWithPropV3xOrgMember> members = new ArrayList<WebWithPropV3xOrgMember>();
                teamlist = orgManager.getTeamsByMember(user.getId(), accountId);//有我的项目组和系统私有组，和公开组

                members = this.getAllMembersByName(teamlist, searchType, condition);
                results = CommonTools.pagenate(members);
                mav.addObject("members", results);
            }
            if (4 == addressbookType) {
                List<WebWithPropV3xOrgMember> results = new ArrayList<WebWithPropV3xOrgMember>();
                List<V3xOrgTeam> teamlist = new ArrayList<V3xOrgTeam>();
                List<WebWithPropV3xOrgMember> members = new ArrayList<WebWithPropV3xOrgMember>();
                teamlist = orgManager.getAllTeams(AppContext.getCurrentUser().getId());
                members = this.getAllMembersByName(teamlist, searchType, condition);
                results = CommonTools.pagenate(members);
                mav.addObject("members", results);
            }
        }
        return mav;
    }

    public OrgManagerDirect getOrgManagerDirect() {
        return orgManagerDirect;
    }

    public void setOrgManagerDirect(OrgManagerDirect orgManagerDirect) {
        this.orgManagerDirect = orgManagerDirect;
    }

    private WebStaffInfo translateV3xOrgMembers(V3xOrgMember member, Long accountId) throws BusinessException {
        Long accId = member.getOrgAccountId();
        Long deptId = member.getOrgDepartmentId();
        Long levelId = member.getOrgLevelId();
        Long postId = member.getOrgPostId();
        WebStaffInfo webstaffinfo = new WebStaffInfo();
        V3xOrgAccount acc = orgManager.getAccountById(accId);
        if (acc != null) {
            webstaffinfo.setOrg_name(acc.getName());

        }
        V3xOrgDepartment dept = orgManager.getDepartmentById(deptId);
        if (dept != null) {
            webstaffinfo.setDepartment_name(OrgHelper.showDepartmentFullPath(deptId));
        }
        V3xOrgLevel level = orgManager.getLevelById(levelId);
        if (null != level) {
            webstaffinfo.setLevel_name(level.getName());
            webstaffinfo.setOrgLevelId(level.getId());
        }

        //政务版-HR新增职级
        if ((Boolean) SysFlag.is_gov_only.getFlag()) {
            Long dutyLevelId = member.getOrgLevelId();
            V3xOrgLevel dutyLevel = orgManager.getLevelById(dutyLevelId);
            if (null != dutyLevel) {
                webstaffinfo.setDutyLevelName(dutyLevel.getName());
                webstaffinfo.setOrgLevelId(dutyLevel.getId());
            }
        }

        V3xOrgPost post = orgManager.getPostById(postId);
        if (null != post) {
            webstaffinfo.setPost_name(post.getName());
            webstaffinfo.setOrgPostId(post.getId());
        }

        webstaffinfo.setName(Functions.showMemberName(member));
        webstaffinfo.setType(Byte.valueOf(member.getType().toString()));
        webstaffinfo.setCode(member.getCode());
        webstaffinfo.setState(Byte.valueOf(member.getState().toString()));
        webstaffinfo.setId(member.getId());
        webstaffinfo.setPeople_type(member.getIsInternal());

        // 取得人员的副岗
        List<MemberPost> memberPosts = member.getSecond_post();
        StringBuilder deptpostbuffer = new StringBuilder();
        if (null != memberPosts && !memberPosts.isEmpty()) {
            for (MemberPost memberPost : memberPosts) {
                StringBuilder sbuffer = new StringBuilder();
                Long deptid = memberPost.getDepId();
                V3xOrgDepartment v3xdept = orgManager.getDepartmentById(deptid);
                sbuffer.append(v3xdept.getName());
                sbuffer.append("-");
                Long postid = memberPost.getPostId();
                V3xOrgPost v3xpost = orgManager.getPostById(postid);
                sbuffer.append(v3xpost.getName());
                if (deptpostbuffer.length() != 0) {
                    deptpostbuffer.append(",");
                }
                deptpostbuffer.append(sbuffer.toString());
            }
        }
        //是否去兼职
        if (accountId != null) {
            //得到所有兼职单位
            List<V3xOrgAccount> conAccount = orgManager.concurrentAccount(member.getId());
            for (V3xOrgAccount account : conAccount) {
                Long conAccountId = account.getId();
                //单位下的兼职
                Map<Long, List<MemberPost>> map = orgManager.getConcurentPostsByMemberId(conAccountId, member.getId());
                if (!map.isEmpty()) {
                    Set<Long> depList = map.keySet();
                    for (Long depid : depList) {
                        V3xOrgDepartment v3xdept = orgManager.getDepartmentById(depid);
                        List<MemberPost> conPostList = map.get(depid);
                        for (MemberPost conPost : conPostList) {
                            StringBuilder sbsuffer = new StringBuilder();
                            Long conPostId = conPost.getPostId();
                            if (conPostId == null)
                                continue;
                            V3xOrgPost v3xpost = orgManager.getPostById(conPostId);
                            sbsuffer.append("(" + account.getShortName() + ")");
                            sbsuffer.append(v3xdept.getName());
                            sbsuffer.append("-");
                            sbsuffer.append(v3xpost.getName());
                            if (deptpostbuffer.length() != 0) {
                                deptpostbuffer.append(",");
                            }
                            deptpostbuffer.append(sbsuffer.toString());
                        }

                    }
                }
            }
        }
        if (deptpostbuffer.length() > 0) {
            webstaffinfo.setSecond_posts(deptpostbuffer.toString());
        }
        return webstaffinfo;
    }

    /**
     * 导出数据（excel/csv）
     */
    @SuppressWarnings("rawtypes")
    public ModelAndView export(HttpServletRequest request, HttpServletResponse response) throws Exception {
        AddressBookSet addressBookSet = addressBookManager.getAddressbookSetByAccountId(AppContext.currentAccountId());
    	boolean showExportPrint  = showExportPrint(AppContext.currentUserId(), addressBookSet);
    	String exp_addressbookType = request.getParameter("exp_addressbookType");
    	if(!showExportPrint && "1".equals(exp_addressbookType)){
    		return null;
    	}
    	
        String exportType = request.getParameter("exportType");
        String exp_deptId = request.getParameter("exp_deptId");
        String exp_accountId = request.getParameter("exp_accountId");
        if ("page".equals(exportType)) {
            return ExportHelper.excutePageMethod(this, request, response, "pageMethod");
        } else {
            // 不分页
            Pagination.withoutPagination(null);
            Pagination.setFirstResult(0);
            Pagination.setMaxResults(Integer.MAX_VALUE);
            ModelAndView mav = ExportHelper.excutePageMethod(this, request, response, "pageMethod");
            if (mav != null) {
                // 从ModelAndView中获得List<WebWithPropV3xOrgMember>或List<AddressBookMember>
                List data = (List) mav.getModel().get("members");
                if (data != null && !data.isEmpty()) {
                    if ("excel".equals(exportType)) {
                        // 参考download()
                        exportAsExcel(request, response, data);
                    } else if ("csv".equals(exportType)) {
                        // 参考csvExport()
                    	if("2".equals(exp_addressbookType)){
                    		exportAsCsv(request, response, data);
                    	}else{
                    		exportAsAddressbookCsv(request, response, data);
                    	}
                    }
                    
                    if(data.size()>0 && Strings.isNotEmpty(exp_addressbookType) && "1".equals(exp_addressbookType)){
                    	insertExpLog(exp_deptId,exp_accountId,String.valueOf(data.size()));
                    }
                }
                
            }
            return null;
        }
    }

    /**
     * 参照download()导出Excel
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void exportAsExcel(HttpServletRequest request, HttpServletResponse response, List data) throws Exception {
        List<WebWithPropV3xOrgMember> webOrgMembers = null;
        List<AddressBookMember> addressMembers = null;
        if (data == null || data.isEmpty()) {
            return;
        } else if (data.get(0) instanceof WebWithPropV3xOrgMember) {
            webOrgMembers = data;
        } else if (data.get(0) instanceof AddressBookMember) {
            addressMembers = data;
        }
        // 以下参照download()
        String resource = "com.seeyon.v3x.addressbook.resource.i18n.AddressBookResources";
        Locale locale = LocaleContext.getLocale(request);
        DataRecord record = new DataRecord();
        String fileName;

        if (webOrgMembers != null) {
            AddressBookSet addressBookSet = addressBookManager.getAddressbookSetByAccountId(AppContext.currentAccountId());
            if (addressBookSet == null) {
                addressBookSet = new AddressBookSet();
            }
            String displayColumn = addressBookSet.getDisplayColumn();
            String[] displayColumns = null!=displayColumn?displayColumn.split(",") : null;
            
			//自定义的通讯录字段
            List<MetadataColumnBO> metadataColumnList=addressBookManager.getCustomerAddressBookList();
            Long accountId = Strings.isBlank(request.getParameter("accountId")) ? null : Long.valueOf(request.getParameter("accountId"));

            initDataRecord(record, request, addressBookSet, true);
            for (WebWithPropV3xOrgMember webOrgMember : webOrgMembers) {
                V3xOrgMember orgMember = webOrgMember.getV3xOrgMember();
                Map customerAddressbookValueMapMap = new HashMap();
                if(Strings.isNotEmpty(metadataColumnList)){
                	customerAddressbookValueMapMap = webOrgMember.getCustomerAddressbookValueMap();
                }
                WebStaffInfo webStaffInfo = this.translateV3xOrgMembers(orgMember, accountId);
                //				~~~~~~~~~~~~~办公电话的扩展属性
                String officeNum = orgMember.getOfficeNum() == null ? "" : orgMember.getOfficeNum();
                DataRow row = new DataRow();
                row.addDataCell(webStaffInfo.getName() == null ? "" : webStaffInfo.getName(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(webStaffInfo.getCode() == null ? "" : webStaffInfo.getCode(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(OrgHelper.showDepartmentFullPath(orgMember.getId(),orgMember.getOrgAccountId()), DataCell.DATA_TYPE_TEXT);
                
                if (addressBookSet.isMemberPost()) {
                    row.addDataCell(webStaffInfo.getPost_name() == null ? "" : webStaffInfo.getPost_name(), DataCell.DATA_TYPE_TEXT);
                    row.addDataCell(webStaffInfo.getSecond_posts() == null ? "" : webStaffInfo.getSecond_posts(), DataCell.DATA_TYPE_TEXT);
                }
                if (addressBookSet.isMemberLevel()) {
                    row.addDataCell(webOrgMember.getLevelName() == null ? "" : webOrgMember.getLevelName(), DataCell.DATA_TYPE_TEXT);
                }
                if (addressBookSet.isMemberEmail()) {
                    row.addDataCell(Strings.isBlank(orgMember.getEmailAddress()) ? "" : orgMember.getEmailAddress(), DataCell.DATA_TYPE_TEXT);
                }

                row.addDataCell(orgMember.getBlog() == null ? "" : orgMember.getBlog(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(orgMember.getWebsite() == null ? "" : orgMember.getWebsite(), DataCell.DATA_TYPE_TEXT);

                if (addressBookSet.isMemberPhone()) {
                    row.addDataCell(Strings.isBlank(officeNum) ? "" : orgMember.getOfficeNum(), DataCell.DATA_TYPE_TEXT);
                }
                if (addressBookSet.isMemberMobile()) {
                    row.addDataCell(Strings.isBlank(webOrgMember.getMobilePhone()) ? "" : webOrgMember.getMobilePhone(), DataCell.DATA_TYPE_TEXT);
                }
                if (addressBookSet.isMemberHome()) {
                    row.addDataCell(orgMember.getAddress() == null ? "" : orgMember.getAddress(), DataCell.DATA_TYPE_TEXT);
                }
                if (addressBookSet.isMemberCode()) {
                    row.addDataCell(orgMember.getPostalcode() == null ? "" : orgMember.getPostalcode(), DataCell.DATA_TYPE_TEXT);
                }
                
                //工作地
                if (addressBookSet.isWorkLocal()) {
                	String location = (String) orgMember.getLocation();
                	if(location==null){
                		location ="";
                	}else{
                		location = enumManagerNew.parseToName(location);
                	}
                	row.addDataCell(location, DataCell.DATA_TYPE_TEXT);
                }
                //自定義的通訊錄字段
                if(null!=displayColumns && displayColumns.length>0){
                	for(int i=0; i<displayColumns.length; i++){
                		if(customerAddressbookValueMapMap.containsKey(displayColumns[i])){
                			row.addDataCell(customerAddressbookValueMapMap.get(displayColumns[i]) == null ? "" : String.valueOf(customerAddressbookValueMapMap.get(displayColumns[i])), DataCell.DATA_TYPE_TEXT);
                		}
                	}
                }
                
                record.addDataRow(row);
                
            }
            fileName = ResourceBundleUtil.getString(resource, locale, "addressbook.download.filename");
            this.fileToExcelManager.save(response, fileName, record);
        } else if (addressMembers != null) {
            initDataRecord(record, request, null, false);
            for (AddressBookMember addressMember : addressMembers) {
                boolean isNotCategories = true;//标识未分类
                DataRow row = new DataRow();
                row.addDataCell(addressMember.getName(), DataCell.DATA_TYPE_TEXT);
                List<AddressBookTeam> teams = this.addressBookManager.getTeamsByCreatorId(AppContext.getCurrentUser().getId());
                if (teams != null && teams.size() > 0) {
                    for (AddressBookTeam team : teams) {
                        if (addressMember.getCategory().equals(team.getId())) {
                            row.addDataCell(team.getName(), DataCell.DATA_TYPE_TEXT);
                            isNotCategories = false;
                            break;
                        }
                    }
                    if (isNotCategories)
                        row.addDataCell("未分类联系人", DataCell.DATA_TYPE_TEXT);
                } else {
                    row.addDataCell("未分类联系人", DataCell.DATA_TYPE_TEXT);
                }

                row.addDataCell(addressMember.getCompanyName() == null ? "" : addressMember.getCompanyName(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getCompanyDept() == null ? "" : addressMember.getCompanyDept(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getCompanyPost() == null ? "" : addressMember.getCompanyPost(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getCompanyLevel() == null ? "" : addressMember.getCompanyLevel(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getEmail() == null ? "" : addressMember.getEmail(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getBlog() == null ? "" : addressMember.getBlog(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getWebsite() == null ? "" : addressMember.getWebsite(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getMsn() == null ? "" : addressMember.getMsn(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getQq() == null ? "" : addressMember.getQq(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getCompanyPhone() == null ? "" : addressMember.getCompanyPhone(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getFamilyPhone() == null ? "" : addressMember.getFamilyPhone(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getMobilePhone() == null ? "" : addressMember.getMobilePhone(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getFax() == null ? "" : addressMember.getFax(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getAddress() == null ? "" : addressMember.getAddress(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getPostcode() == null ? "" : addressMember.getPostcode(), DataCell.DATA_TYPE_TEXT);
                record.addDataRow(row);
            }
            fileName = ResourceBundleUtil.getString(resource, locale, "addressbook.download.personfilename");
            this.fileToExcelManager.save(response, fileName, record);
        }
    }
    
    
    private void exportAsAddressbookCsv(HttpServletRequest request, HttpServletResponse response, List data) throws Exception {
        List<WebWithPropV3xOrgMember> webOrgMembers = null;
        if (data == null || data.isEmpty()) {
            return;
        } else if (data.get(0) instanceof WebWithPropV3xOrgMember) {
            webOrgMembers = data;
        } else {
        	log.info("---没有导出的数据----");
        	return;
        }

        AddressBookSet addressBookSet = addressBookManager.getAddressbookSetByAccountId(AppContext.currentAccountId());
        if (addressBookSet == null) {
            addressBookSet = new AddressBookSet();
        }
        String displayColumn = addressBookSet.getDisplayColumn();
        String[] displayColumns = null!=displayColumn?displayColumn.split(",") : null;
        
		//自定义的通讯录字段
        List<MetadataColumnBO> metadataColumnList=addressBookManager.getCustomerAddressBookList();
        Long accountId = Strings.isBlank(request.getParameter("accountId")) ? null : Long.valueOf(request.getParameter("accountId"));

        DataRecord record = new DataRecord();
        initDataRecord(record, request, addressBookSet, true);
        String[] columnName = record.getColumnName();
        String title = "";
        for(String cn : columnName){
        	title = title + cn +",";
        }
        StringBuilder buffer = new StringBuilder(title);
        buffer.append("\r\n");
        for (WebWithPropV3xOrgMember webOrgMember : webOrgMembers) {
            V3xOrgMember orgMember = webOrgMember.getV3xOrgMember();
            Map customerAddressbookValueMapMap = new HashMap();
            if(Strings.isNotEmpty(metadataColumnList)){
            	customerAddressbookValueMapMap = webOrgMember.getCustomerAddressbookValueMap();
            }
            WebStaffInfo webStaffInfo = this.translateV3xOrgMembers(orgMember, accountId);
            //				~~~~~~~~~~~~~办公电话的扩展属性
            String officeNum = orgMember.getOfficeNum() == null ? "" : orgMember.getOfficeNum();
            buffer.append(webStaffInfo.getName() == null ? "" : webStaffInfo.getName().replace(",", "")).append(",");
            buffer.append(webStaffInfo.getCode() == null ? "" : webStaffInfo.getCode().replace(",", "")).append(",");
            buffer.append(OrgHelper.showDepartmentFullPath(orgMember.getId(),orgMember.getOrgAccountId()).replace(",", "")).append(",");
            
            if (addressBookSet.isMemberPost()) {
            	buffer.append(webStaffInfo.getPost_name() == null ? "" : webStaffInfo.getPost_name().replace(",", "")).append(",");
            	buffer.append(webStaffInfo.getSecond_posts() == null ? "" : webStaffInfo.getSecond_posts().replace(",", "")).append(",");
            }
            if (addressBookSet.isMemberLevel()) {
            	buffer.append(webOrgMember.getLevelName() == null ? "" : webOrgMember.getLevelName().replace(",", "")).append(",");
            }
            if (addressBookSet.isMemberEmail()) {
            	buffer.append(Strings.isBlank(orgMember.getEmailAddress()) ? "" : orgMember.getEmailAddress().replace(",", "")).append(",");
            }

            buffer.append(orgMember.getBlog() == null ? "" : orgMember.getBlog().replace(",", "")).append(",");
            buffer.append(orgMember.getWebsite() == null ? "" : orgMember.getWebsite().replace(",", "")).append(",");

            if (addressBookSet.isMemberPhone()) {
            	buffer.append(Strings.isBlank(officeNum) ? "" : orgMember.getOfficeNum().replace(",", "")).append(",");
            }
            if (addressBookSet.isMemberMobile()) {
            	buffer.append(Strings.isBlank(webOrgMember.getMobilePhone()) ? "" : webOrgMember.getMobilePhone().replace(",", "")).append(",");
            }
            if (addressBookSet.isMemberHome()) {
            	buffer.append(orgMember.getAddress() == null ? "" : orgMember.getAddress().replace(",", "")).append(",");
            }
            if (addressBookSet.isMemberCode()) {
            	buffer.append(orgMember.getPostalcode() == null ? "" : orgMember.getPostalcode().replace(",", "")).append(",");
            }
            
            //工作地
            if (addressBookSet.isWorkLocal()) {
            	String location = (String) orgMember.getLocation();
            	if(location==null){
            		location ="";
            	}else{
            		location = enumManagerNew.parseToName(location);
            	}
            	buffer.append(location.replace(",", "")).append(",");
            }
            //自定義的通訊錄字段
            if(null!=displayColumns && displayColumns.length>0){
            	for(int i=0; i<displayColumns.length; i++){
            		if(customerAddressbookValueMapMap.containsKey(displayColumns[i])){
            			buffer.append(customerAddressbookValueMapMap.get(displayColumns[i]) == null ? "" : String.valueOf(customerAddressbookValueMapMap.get(displayColumns[i])).replace(",", "")).append(",");
            		}
            	}
            }
            
            buffer.append("\r\n");
        }
        String exportStr = buffer.toString();
        String title1 = "WLMContacts" + ".csv";
        //request.setCharacterEncoding(contentTypeCharset);
        //response.setCharacterEncoding(contentTypeCharset);
        title1 = URLEncoder.encode(title1, "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=\"" + title1 + "\"");
        response.setContentType("text/csv; charset=" + contentTypeCharset);
        //response.setContentType("application/x-msdownload; charset=" + contentTypeCharset);
        String charset = "UTF-8";
        response.setCharacterEncoding(charset);
        OutputStream out = null;
        try {
            //			byte[] b = exportStr.getBytes();
            out = response.getOutputStream();
            //			IOUtils.write(b, out);
            // 写UTF-8 BOM信息 http://en.wikipedia.org/wiki/Byte_order_mark
            out.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
            out.write(exportStr.getBytes(charset));
            out.flush();
            out.close();
        } catch (Exception e) {
            if (e.getClass().getSimpleName().equals(this.clientAbortExceptionName)) {
                log.error("用户关闭下载窗口: " + e);
            } else {
                log.error("", e);
            }
        } finally {
            IOUtils.closeQuietly(out);
        }
    }
    
    private void exportAsPrivateCsv(HttpServletRequest request, HttpServletResponse response, List data) throws Exception {
        List<AddressBookMember> webOrgMembers = null;
        if (data == null || data.isEmpty()) {
            return;
        } else if (data.get(0) instanceof AddressBookMember) {
            webOrgMembers = data;
        } else {
        	log.info("---没有导出的数据----");
        	return;
        }

        DataRecord record = new DataRecord();
        initDataRecord(record, request, null, false);
        String[] columnName = record.getColumnName();
        String title = "";
        for(String cn : columnName){
        	title = title + cn +",";
        }
        StringBuilder buffer = new StringBuilder(title);
        buffer.append("\r\n");
        for (AddressBookMember addressMember : webOrgMembers) {
            boolean isNotCategories = true;//标识未分类
            buffer.append(addressMember.getName().replace(",", "")).append(",");
            List<AddressBookTeam> teams = this.addressBookManager.getTeamsByCreatorId(AppContext.getCurrentUser().getId());
            if (teams != null && teams.size() > 0) {
                for (AddressBookTeam team : teams) {
                    if (addressMember.getCategory().equals(team.getId())) {
                    	buffer.append(team.getName().replace(",", "")).append(",");
                        isNotCategories = false;
                        break;
                    }
                }
                if (isNotCategories)
                	buffer.append("未分类联系人".replace(",", "")).append(",");
            } else {
            	buffer.append("未分类联系人".replace(",", "")).append(",");
            }

            buffer.append(addressMember.getCompanyName() == null ? "" : addressMember.getCompanyName().replace(",", "")).append(",");
            buffer.append(addressMember.getCompanyDept() == null ? "" : addressMember.getCompanyDept().replace(",", "")).append(",");
            buffer.append(addressMember.getCompanyPost() == null ? "" : addressMember.getCompanyPost().replace(",", "")).append(",");
            buffer.append(addressMember.getCompanyLevel() == null ? "" : addressMember.getCompanyLevel().replace(",", "")).append(",");
            buffer.append(addressMember.getEmail() == null ? "" : addressMember.getEmail().replace(",", "")).append(",");
            buffer.append(addressMember.getBlog() == null ? "" : addressMember.getBlog().replace(",", "")).append(",");
            buffer.append(addressMember.getWebsite() == null ? "" : addressMember.getWebsite().replace(",", "")).append(",");
            buffer.append(addressMember.getMsn() == null ? "" : addressMember.getMsn().replace(",", "")).append(",");
            buffer.append(addressMember.getQq() == null ? "" : addressMember.getQq().replace(",", "")).append(",");
            buffer.append(addressMember.getCompanyPhone() == null ? "" : addressMember.getCompanyPhone().replace(",", "")).append(",");
            buffer.append(addressMember.getFamilyPhone() == null ? "" : addressMember.getFamilyPhone().replace(",", "")).append(",");
            buffer.append(addressMember.getMobilePhone() == null ? "" : addressMember.getMobilePhone().replace(",", "")).append(",");
            buffer.append(addressMember.getFax() == null ? "" : addressMember.getFax().replace(",", "")).append(",");
            buffer.append(addressMember.getAddress() == null ? "" : addressMember.getAddress().replace(",", "")).append(",");
            buffer.append(addressMember.getPostcode() == null ? "" : addressMember.getPostcode().replace(",", "")).append(",");
            
            buffer.append("\r\n");
        }
        String exportStr = buffer.toString();
        String title1 = "WLMContacts" + ".csv";
        //request.setCharacterEncoding(contentTypeCharset);
        //response.setCharacterEncoding(contentTypeCharset);
        title1 = URLEncoder.encode(title1, "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=\"" + title1 + "\"");
        response.setContentType("text/csv; charset=" + contentTypeCharset);
        //response.setContentType("application/x-msdownload; charset=" + contentTypeCharset);
        String charset = "UTF-8";
        response.setCharacterEncoding(charset);
        OutputStream out = null;
        try {
            //			byte[] b = exportStr.getBytes();
            out = response.getOutputStream();
            //			IOUtils.write(b, out);
            // 写UTF-8 BOM信息 http://en.wikipedia.org/wiki/Byte_order_mark
            out.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
            out.write(exportStr.getBytes(charset));
            out.flush();
            out.close();
        } catch (Exception e) {
            if (e.getClass().getSimpleName().equals(this.clientAbortExceptionName)) {
                log.error("用户关闭下载窗口: " + e);
            } else {
                log.error("", e);
            }
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * 下载
     * @author lucx
     * @deprecated 请用export()
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ModelAndView download(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map<String, List> map = getDownList(request);
        Locale locale = LocaleContext.getLocale(request);
        String resource = "com.seeyon.v3x.addressbook.resource.i18n.AddressBookResources";
        DataRecord record = new DataRecord();
        String fileName = "";
        List<V3xOrgMember> orgMembers = map.get("orgMembers");
        List<AddressBookMember> addressMembers = map.get("addressMembers");

        if (orgMembers != null) {
            initDataRecord(record, request, null, true);
            for (V3xOrgMember orgMember : orgMembers) {
                WebStaffInfo webStaffInfo = this.translateV3xOrgMembers(orgMember, null);
                //				~~~~~~~~~~~~~办公电话的扩展属性
                String officeNum = orgMember.getProperty("officeNum") == null ? "" : orgMember.getProperty("officeNum").toString();
                DataRow row = new DataRow();
                row.addDataCell(webStaffInfo.getName() == null ? "" : webStaffInfo.getName(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(webStaffInfo.getCode() == null ? "" : webStaffInfo.getCode(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(webStaffInfo.getDepartment_name() == null ? "" : webStaffInfo.getDepartment_name(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(webStaffInfo.getPost_name() == null ? "" : webStaffInfo.getPost_name(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(webStaffInfo.getSecond_posts() == null ? "" : webStaffInfo.getSecond_posts(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(webStaffInfo.getLevel_name() == null ? "" : webStaffInfo.getLevel_name(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(Strings.isBlank(orgMember.getEmailAddress()) ? "" : orgMember.getEmailAddress(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(orgMember.getBlog() == null ? "" : orgMember.getBlog(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(orgMember.getWebsite() == null ? "" : orgMember.getWebsite(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(Strings.isBlank(officeNum) ? "" : officeNum, DataCell.DATA_TYPE_TEXT);
                row.addDataCell(Strings.isBlank(orgMember.getTelNumber()) ? "" : orgMember.getTelNumber(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(orgMember.getAddress() == null ? "" : orgMember.getAddress(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(orgMember.getPostalcode() == null ? "" : orgMember.getPostalcode(), DataCell.DATA_TYPE_TEXT);
                record.addDataRow(row);
            }
            fileName = ResourceBundleUtil.getString(resource, locale, "addressbook.download.filename");
            this.fileToExcelManager.save(response, fileName, record);
        } else if (addressMembers != null) {
            initDataRecord(record, request, null, false);
            for (AddressBookMember addressMember : addressMembers) {
                boolean isNotCategories = true;//标识未分类
                DataRow row = new DataRow();
                row.addDataCell(addressMember.getName(), DataCell.DATA_TYPE_TEXT);
                List<AddressBookTeam> teams = this.addressBookManager.getTeamsByCreatorId(AppContext.getCurrentUser().getId());
                if (teams != null && teams.size() > 0) {
                    for (AddressBookTeam team : teams) {
                        if (addressMember.getCategory().equals(team.getId())) {
                            row.addDataCell(team.getName(), DataCell.DATA_TYPE_TEXT);
                            isNotCategories = false;
                            break;
                        }
                    }
                    if (isNotCategories)
                        row.addDataCell("未分类联系人", DataCell.DATA_TYPE_TEXT);
                } else {
                    row.addDataCell("未分类联系人", DataCell.DATA_TYPE_TEXT);
                }

                row.addDataCell(addressMember.getCompanyName() == null ? "" : addressMember.getCompanyName(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getCompanyDept() == null ? "" : addressMember.getCompanyDept(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getCompanyPost() == null ? "" : addressMember.getCompanyPost(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getCompanyLevel() == null ? "" : addressMember.getCompanyLevel(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getEmail() == null ? "" : addressMember.getEmail(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getBlog() == null ? "" : addressMember.getBlog(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getWebsite() == null ? "" : addressMember.getWebsite(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getMsn() == null ? "" : addressMember.getMsn(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getQq() == null ? "" : addressMember.getQq(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getCompanyPhone() == null ? "" : addressMember.getCompanyPhone(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getFamilyPhone() == null ? "" : addressMember.getFamilyPhone(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getMobilePhone() == null ? "" : addressMember.getMobilePhone(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getFax() == null ? "" : addressMember.getFax(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getAddress() == null ? "" : addressMember.getAddress(), DataCell.DATA_TYPE_TEXT);
                row.addDataCell(addressMember.getPostcode() == null ? "" : addressMember.getPostcode(), DataCell.DATA_TYPE_TEXT);
                record.addDataRow(row);
            }
            fileName = ResourceBundleUtil.getString(resource, locale, "addressbook.download.personfilename");
            this.fileToExcelManager.save(response, fileName, record);
        } else {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script type='text/javascript'>");
            out.println("alert(parent.v3x.getMessage(\"ADDRESSBOOKLang.addressbook_group_download_label\"))");
            out.println("</script>");
        }
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map<String, List> getDownList(HttpServletRequest request) throws Exception {

        String condition = request.getParameter("condition");
        String click = request.getParameter("click");
        String accountId = request.getParameter("accountId");//判断是否是切换单位传来的ID 。
        int type = ServletRequestUtils.getIntParameter(request, "type", 1);

        List<V3xOrgMember> orgMembers = null;
        List<AddressBookMember> addressMembers = null;
        User user = AppContext.getCurrentUser();
        Long accId = user.getLoginAccount();
        if (Strings.isNotBlank(accountId)) {
            accId = Long.parseLong(accountId);
        }
        Long deptId = null;
        if (Strings.isNotBlank(condition) && !"[object]".equals(condition)) {
            String searchType = ServletRequestUtils.getStringParameter(request, "textfield");
            if ("name".equals(condition)) {
                if (type == 1) {
                    orgMembers = this.filterMemberByAssigned(this.departmentByName("name", this.getAllMembersByLoginAccount(accId), searchType));
                } else if (type == 2) {
                    addressMembers = this.addressBookManager.getMemberByName(searchType);
                } else if (type == 3) {
                    List<V3xOrgTeam> teamlist = new ArrayList<V3xOrgTeam>();
                    teamlist = orgManager.getTeamsByMember(user.getId(), accId);
                    orgMembers = this.getAllMembers(teamlist, searchType, "name");
                } else if (type == 4) {
                    List<V3xOrgTeam> teamlist = new ArrayList<V3xOrgTeam>();
                    teamlist = orgManager.getAllTeams(AppContext.getCurrentUser().getId());
                    orgMembers = this.getAllMembers(teamlist, searchType, "name");
                }

            } else if ("level".equals(condition)) {
                if (type == 1) {
                    //by Yongzhang 2008-11-14
                    orgMembers = this.departmentByLevel(this.filterMemberByAssigned(this.getAllMembersByLoginAccount(accId)), searchType);
                } else if (type == 2) {
                    addressMembers = this.addressBookManager.getMemberByLevelName(searchType);
                } else if (type == 3) {
                    List<V3xOrgTeam> teamlist = new ArrayList<V3xOrgTeam>();
                    teamlist = orgManager.getTeamsByMember(user.getId(), accId);
                    orgMembers = this.getAllMembers(teamlist, searchType, "level");
                } else if (type == 4) {
                    List<V3xOrgTeam> teamlist = new ArrayList<V3xOrgTeam>();
                    teamlist = orgManager.getAllTeams(AppContext.getCurrentUser().getId());
                    orgMembers = this.getAllMembers(teamlist, searchType, "level");
                }
            } else if ("tel".equals(condition)) {
                if (type == 1) {
                    orgMembers = this.filterMemberByAssigned(this.departmentByName("tel", this.getAllMembersByLoginAccount(accId), searchType));
                } else if (type == 2) {
                    addressMembers = this.addressBookManager.getMemberByTel(searchType);
                } else if (type == 3) {
                    List<V3xOrgTeam> teamlist = new ArrayList<V3xOrgTeam>();
                    teamlist = orgManager.getTeamsByMember(user.getId(), accId);
                    orgMembers = this.getAllMembers(teamlist, searchType, "tel");
                } else if (type == 4) {
                    List<V3xOrgTeam> teamlist = new ArrayList<V3xOrgTeam>();
                    teamlist = orgManager.getAllTeams(AppContext.getCurrentUser().getId());
                    orgMembers = this.getAllMembers(teamlist, searchType, "tel");
                }

            }
        } else if (Strings.isNotBlank(click) && !"[object]".equals(click)) {
            if ("dept".equals(click)) {
                deptId = ServletRequestUtils.getLongParameter(request, "deptId");
                String mem = request.getParameter("mem");
                if (mem != null && "all".equals(mem)) {
                    List<V3xOrgMember> members = new ArrayList<V3xOrgMember>();
                    members.addAll(getAllMembersByLoginAccount(deptId));

                    orgMembers = this.filterMemberByAssigned(members);

                } else {

                    orgMembers = this.filterMemberByAssigned(orgManagerDirect.getMembersByDepartment(deptId, accId, false, true, true));

                }
            } else if ("sysTeam".equals(click)) {
                Long sysId = ServletRequestUtils.getLongParameter(request, "sysId");
                V3xOrgTeam team = orgManager.getTeamById(sysId);
                List<V3xOrgMember> members = new ArrayList<V3xOrgMember>();
                List<OrgTypeIdBO> leaderIds = team.getLeaders();
                //List<OrgTypeIdBO> memberIds = team.getMembers();
                List<OrgTypeIdBO> supervisors = team.getSupervisors();
                List<OrgTypeIdBO> relations = team.getRelatives();
                //leaderIds.addAll(memberIds);
                leaderIds.addAll(supervisors);
                leaderIds.addAll(relations);
                for (OrgTypeIdBO memberId : leaderIds) {
                    V3xOrgMember member = orgManager.getMemberById(memberId.getLId());
                    if (member != null)
                        if (members.isEmpty())
                            members.add(member);
                        else {
                            if (!members.contains(member))
                                members.add(member);
                        }
                }
                
                //组中 可能包含团队，直接取出团队中所有的人
                List<V3xOrgMember> itemMembers = orgManager.getMembersByTeam(sysId);
                for(V3xOrgMember m : itemMembers){
                    if (members.isEmpty()) {
                        members.add(m);
                    } else {
                        if (!members.contains(m))
                            members.add(m);
                    }
                }
                orgMembers = this.filterMemberByAssigned(members);
            } else if ("ownTeam".equals(click)) {
                Long otId = ServletRequestUtils.getLongParameter(request, "otId");
                if (type == 4) {
                	List<V3xOrgEntity> orgEntitys = orgManager.getTeamMember(otId);
                } else {
                    addressMembers = this.addressBookManager.getMembersByTeamId(otId);
                }
            }
        } else {
            if (type == 1) {
                if (user.getAccountId() != user.getLoginAccount()) {
                    orgMembers = this.loadMembers(user/*,false*/);
                } else
                    orgMembers = this.filterMemberByAssigned(orgManager.getMembersByDepartment(user.getDepartmentId(), true));
            } else if (type == 2) {
                final Long creatorId = Long.valueOf(user.getId());
                addressMembers = addressBookManager.getMembersByCreatorId(creatorId);
            }
        }
        V3xOrgMember currentMember = orgManager.getMemberById(user.getId());
        boolean isInner = currentMember.getIsInternal();

        List<V3xOrgEntity> outerRight = null;
        if (!isInner) {
            outerRight = orgManager.getExternalMemberWorkScope(currentMember.getId(), false);
        }
        List<V3xOrgMember> member1 = null;
        if (orgMembers != null) {
            member1 = new ArrayList<V3xOrgMember>();
            for (V3xOrgMember member : orgMembers) {
                boolean cls = this.checkLevelScope(member, currentMember, outerRight, deptId);
                if (cls)
                    member1.add(member);
            }
        }

        Map<String, List> map = new HashMap<String, List>();
        map.put("orgMembers", member1);
        map.put("addressMembers", addressMembers);
        return map;
    }

    private void initDataRecord(DataRecord record, HttpServletRequest request, AddressBookSet addressBookSet, boolean bool) {
        Locale locale = LocaleContext.getLocale(request);
        String resource = "com.seeyon.v3x.addressbook.resource.i18n.AddressBookResources";
        //员工通讯录
        String state_Public = ResourceBundleUtil.getString(resource, locale, "addressbook.menu.private.label");
        //私人通讯录
        String state_Private = ResourceBundleUtil.getString(resource, locale, "addressbook.menu.public.label");
        //姓名
        String state_Name = ResourceBundleUtil.getString(resource, locale, "addressbook.username.label");
        //部门
        String state_Department = ResourceBundleUtil.getString(resource, locale, "addressbook.company.department.label");
        //主要岗位
        String state_PrimaryPost = ResourceBundleUtil.getString(resource, locale, "addressbook.account_form.primaryPost.label");
        //副岗
        String state_SecondPost = ResourceBundleUtil.getString(resource, locale, "addressbook.account_form.secondPost.label")+"/"+ResourceBundleUtil.getString(resource, locale, "addressbook.account_form.conCurrentPost.label");
        //职务
        //branches_a8_v350_r_gov GOV-1891 lijl添加判断是不是政务版本,如果是则显示为职务,否则显示为职务级别---------------------------------------------------Start
        String state_Level = "";
        boolean isGovVersion = (Boolean) SysFlag.is_gov_only.getFlag();
        if (isGovVersion) {
            state_Level = ResourceBundleUtil.getString(resource, locale, "addressbook.company.level.label.GOV");
        } else {
            state_Level = ResourceBundleUtil.getString(resource, locale, "addressbook.company.level.label");
        }
        //lijl添加判断是不是政务版本,如果是则显示为职务,否则显示为职务级别---------------------------------------------------End
        //电子邮件
        String state_Email = ResourceBundleUtil.getString(resource, locale, "addressbook.email.label");
        //博客
        String state_Blog = ResourceBundleUtil.getString(resource, locale, "addressbook.account_form.blog.label");
        //网址
        String state_Website = ResourceBundleUtil.getString(resource, locale, "addressbook.account_form.website.labe");
        //电话
        String state_Familyphone = ResourceBundleUtil.getString(resource, locale, "addressbook.company.telephone.label");
        //手机
        String state_Mobilephone = ResourceBundleUtil.getString(resource, locale, "addressbook.mobilephone.label");
        //家庭住址
        String state_Address = ResourceBundleUtil.getString(resource, locale, "addressbook.account_form.address.label");
        //邮政编码
        String state_Postcode = ResourceBundleUtil.getString(resource, locale, "addressbook.account_form.postcode.label");
        //工号
        String state_Code = ResourceBundleUtil.getString(resource, locale, "addressbook.account_form.code.label");
        //类别
        String state_Category = ResourceBundleUtil.getString(resource, locale, "addressbook.account_form.category.label");
        //单位名称
        String state_CompanyName = ResourceBundleUtil.getString(resource, locale, "addressbook.account_form.companyName.label");
        //岗位
        String state_Post = ResourceBundleUtil.getString(resource, locale, "addressbook.company.post.label");
        //MSN
        String state_MSN = ResourceBundleUtil.getString(resource, locale, "addressbook.account_form.msn.label");
        //QQ
        String state_QQ = ResourceBundleUtil.getString(resource, locale, "addressbook.account_form.qq.label");
        //单位电话
        String state_Telephone = ResourceBundleUtil.getString(resource, locale, "addressbook.company.telephone.label");
        //传真
        String state_Fax = ResourceBundleUtil.getString(resource, locale, "addressbook.account_form.fax.labe");
        //工作地
        String member_Address = ResourceBundleUtil.getString(resource, locale, "member.location");
        if (bool) {
            record.setSheetName(state_Public);
            record.setTitle(state_Public);

            List<String> columnNames = new ArrayList<String>();
            columnNames.add(state_Name);
            columnNames.add(state_Code);
            columnNames.add(state_Department);
            if (addressBookSet.isMemberPost()) {
                columnNames.add(state_PrimaryPost);
                columnNames.add(state_SecondPost);
            }
            if (addressBookSet.isMemberLevel()) {
                columnNames.add(state_Level);
            }
            if (addressBookSet.isMemberEmail()) {
                columnNames.add(state_Email);
            }
            columnNames.add(state_Blog);
            columnNames.add(state_Website);
            if (addressBookSet.isMemberPhone()) {
                columnNames.add(state_Familyphone);
            }
            if (addressBookSet.isMemberMobile()) {
                columnNames.add(state_Mobilephone);
            }
            if (addressBookSet.isMemberHome()) {
                columnNames.add(state_Address);
            }
            if (addressBookSet.isMemberCode()) {
                columnNames.add(state_Postcode);
            }
            
            if (addressBookSet.isWorkLocal()) {
                columnNames.add(member_Address);
            }
            try {
    			List<MetadataColumnBO> metadataColumnList=addressBookManager.getCurrentAccountEnableCustomerFields(addressBookSet);
    			for(MetadataColumnBO metadataColumnBO : metadataColumnList){
    				columnNames.add(metadataColumnBO.getLabel());
    			}
    		} catch (BusinessException e) {
    		}
            

            String[] columns = new String[columnNames.size()];
            columnNames.toArray(columns);
            record.setColumnName(columns);
        } else {
            record.setSheetName(state_Private);
            record.setTitle(state_Private);
            String[] columnNames = { state_Name, state_Category, state_CompanyName, state_Department, state_Post, state_Level, state_Email, state_Blog, state_Website, state_MSN, state_QQ, state_Telephone, state_Familyphone, state_Mobilephone, state_Fax, state_Address, state_Postcode };
            record.setColumnName(columnNames);
        }
    }

    /**
     * 取得个人组内的所有成员--过滤重复的
     */
    private List<WebWithPropV3xOrgMember> getAllMembersByName(List<V3xOrgTeam> teamlist, String searchType, String type) throws Exception {
        List<WebWithPropV3xOrgMember> webMembers = new ArrayList<WebWithPropV3xOrgMember>();
        Set<V3xOrgMember> teams2 = new HashSet<V3xOrgMember>();
        List<V3xOrgMember> teams3 = new ArrayList<V3xOrgMember>();
        for (V3xOrgTeam team : teamlist) {
            List<V3xOrgMember> teams = null;

            teams = orgManager.getMembersByTeam(team.getId(), null, 
            		new OrgConstants.TeamMemberType[]{TeamMemberType.Leader, TeamMemberType.Member, TeamMemberType.SuperVisor, TeamMemberType.Relative}); //获取组的成员。由组长、组员构成
            teams2.addAll(teams);

        }
        teams3.addAll(teams2);//过滤掉重复的的人员显示
        List<WebWithPropV3xOrgMember> searchMembers = translateList(this.filterMemberByAssigned(teams3), -1, AppContext.currentAccountId(),"");
        if (searchMembers != null) {
            for (WebWithPropV3xOrgMember member : searchMembers) {
                String con = "";
                if ("name".equals(type)) {
                    con = member.getV3xOrgMember().getName();
                    if (con.contains(searchType)) {
                        webMembers.add(member);
                    }
                } else if ("level".equals(type)) {
                    con = String.valueOf(member.getV3xOrgMember().getOrgLevelId());
                    if (Strings.isNotBlank(con) && con.equals(searchType)) {
                        webMembers.add(member);
                    }
                } else {
                    con = member.getV3xOrgMember().getTelNumber();
                    if (Strings.isNotBlank(con) && con.contains(searchType)) {
                        webMembers.add(member);
                    }
                }
            }
        }

        return webMembers;
    }

    /**
     * 取的所有的成员
     */
    private List<V3xOrgMember> getAllMembers(List<V3xOrgTeam> teamlist, String searchType, String type) throws Exception {
        List<V3xOrgMember> members = new ArrayList<V3xOrgMember>();
        List<WebWithPropV3xOrgMember> webMembers = this.getAllMembersByName(teamlist, searchType, type);
        for (WebWithPropV3xOrgMember webMember : webMembers) {
            members.add(webMember.getV3xOrgMember());
        }
        return members;
    }

    /**
     * 获取当前登陆单位下所有的人员
     */
    private List<V3xOrgMember> getAllMembersByLoginAccount(Long accountId) throws BusinessException {
        List<V3xOrgMember> members = null;
        List<V3xOrgMember> members1 = null;
        members = orgManager.getAllMembers(accountId);
        members1 = orgManager.getAllExtMembers(accountId);//单位下所以外部的人员
        members.addAll(members1);
        List<V3xOrgRelationship> MemberPosts = orgManagerDirect.findAllSidelineAccountCntPost(accountId);//获取但前登录单位的兼职列表，不分页
        if (MemberPosts != null && !MemberPosts.isEmpty()) {
            for (V3xOrgRelationship MemberPost : MemberPosts) {
                members.add(orgManager.getMemberById(MemberPost.getSourceId()));
            }
        }
        return members;
    }

    /**
     * 检测 当前登录用户是否看到被检测者
     * @see com.seeyon.v3x.common.taglibs.functions.Functions
     */
    public boolean checkLevelScope(long currentMemberId, long memberId) {
        try {
            V3xOrgMember currentMember = orgManager.getMemberById(currentMemberId); // 当前登录者
            V3xOrgMember member = orgManager.getMemberById(memberId); // 被检测的人

            if (currentMember == null || member == null) {
                return false;
            }

            //同一个部门
            if (currentMember.getOrgDepartmentId().longValue() == member.getOrgDepartmentId().longValue()) {
                return true;
            }

            //相同的职务级别
            if (currentMember.getOrgLevelId().longValue() == member.getOrgLevelId().longValue()) {
                return true;
            }

            //内部人员都可以看到外部人员
            if (currentMember.getOrgLevelId() == -1 || member.getOrgLevelId() == -1) {
                return currentMember.getOrgLevelId() != -1;
            }

            // 副岗在这个部门的有权限
            if (MemberHelper.isSndPostContainDept(currentMember, member.getOrgDepartmentId())) {
                return true;
            }

            V3xOrgLevel currentMemberLevel = orgManager.getLevelById(currentMember.getOrgLevelId());
            int currentMemberLevelSortId = currentMemberLevel != null ? currentMemberLevel.getLevelId() : 0;
            V3xOrgLevel level = orgManager.getLevelById(member.getOrgLevelId());
            V3xOrgLevel lowerLevel = orgManager.getLowestLevel(currentMember.getOrgAccountId());
            int memberLevelSortId = level != null ? level.getLevelId() : lowerLevel != null ? lowerLevel.getLevelId() : -1000;

            int currentAccountLevelScope = orgManager.getAccountById(currentMember.getOrgAccountId()).getLevelScope();

            if ((currentMember.getOrgDepartmentId().equals(member.getOrgDepartmentId())) || currentAccountLevelScope < 0) {
                return true;
            }

            if (currentMemberLevelSortId - memberLevelSortId <= currentAccountLevelScope) {
                return true;
            }
        } catch (Exception e) {
            log.error("检测工作范围", e);
        }

        return false;
    }

    /**
     * 在人员列表中得到包含name的人员
     */
    private List<V3xOrgMember> departmentByName(String condition, List<V3xOrgMember> members, String value) {
        List<V3xOrgMember> results = new ArrayList<V3xOrgMember>();
        CharSequence charBuffer = new StringBuilder(value);
        if (members != null && !members.isEmpty()) {
            for (V3xOrgMember member : members) {
                if ("name".equals(condition)) {
                    if (member.getName().contains(charBuffer) && !results.contains(member)) {
                        results.add(member);
                    }
                } else {
                    if (Strings.isNotBlank(member.getTelNumber()) && member.getTelNumber().contains(charBuffer) && !results.contains(member)) {
                        results.add(member);
                    }
                }
            }
        }
        return results;
    }

    /**
     * 在人员列表中得到包含level的人员
     */
    private List<V3xOrgMember> departmentByLevel(List<V3xOrgMember> members, String level) {
        List<V3xOrgMember> results = new ArrayList<V3xOrgMember>();
        if (members != null && !members.isEmpty()) {
            for (V3xOrgMember member : members) {
                if (member.getOrgLevelId() == Long.parseLong(level))
                    results.add(member);
            }
        }
        return results;
    }

    /**
     * 得到进入兼职单位时默认的人员列表
     */
    private List<V3xOrgMember> loadMembers(User user/* ,  boolean isPagenate*/) throws BusinessException {
        List<V3xOrgMember> members = new ArrayList<V3xOrgMember>();
        List<V3xOrgDepartment> listdept = orgManager.getDepartmentsByUser(user.getId());
        for (V3xOrgDepartment dept : listdept) {
            if (dept.getOrgAccountId() == user.getLoginAccount()) {
                List<V3xOrgMember> temp = orgManager.getMembersByDepartment(dept.getId(), true);
                if (temp != null && !temp.isEmpty())
                    members.addAll(temp);
                break;
            }
        }
        members.remove(orgManager.getMemberById(user.getId()));
        return this.filterMemberByAssigned(members);
    }

    /**
     * 人员过滤--去掉离职的人员
     */
    private List<V3xOrgMember> filterMemberByAssigned(List<V3xOrgMember> members) {
        List<V3xOrgMember> results = new ArrayList<V3xOrgMember>();
        if (members != null && !members.isEmpty()) {
            for (V3xOrgMember member : members) {
                if (member.isValid())
                    results.add(member);
            }
        }
        return results;
    }

    /**
     * 过滤职务级别权限</br>
     * 不要使用此方法，使用统一Functions中方法
     * @author lucx
     * @deprecated
     */
    private boolean checkLevelScope(V3xOrgMember member, V3xOrgMember currentMember, List<V3xOrgEntity> outerRight, Long departmentId) {
        try {

            if (currentMember == null || member == null) {
                return false;
            }

            //同一个部门
            if (currentMember.getOrgDepartmentId().longValue() == member.getOrgDepartmentId().longValue()) {
                return true;
            }

            //相同的职务级别
            if (currentMember.getOrgLevelId().longValue() == member.getOrgLevelId().longValue()) {
                return true;
            }

            //内部人员查看外部人员 1.跨靠部门 2.外部人员有权限 3.同一组
            if (currentMember.getIsInternal() && !member.getIsInternal()) {
                V3xOrgDepartment curDep = orgManager.getDepartmentById(currentMember.getOrgDepartmentId());
                V3xOrgDepartment memDep = orgManager.getDepartmentById(member.getOrgDepartmentId());
                //跨靠部门
                if (curDep.getOrgAccountId().longValue() == memDep.getOrgAccountId() && curDep.getParentPath().equals(memDep.getParentPath())) {
                    return true;
                }
                //外部人员权限内部
                Long lonAccountId = AppContext.getCurrentUser().getLoginAccount();
                List<V3xOrgEntity> canReadList = orgManager.getExternalMemberWorkScope(member.getId(), false);
                List<Long> depIds = orgManager.getUserDomainIDs(currentMember.getId(), V3xOrgEntity.VIRTUAL_ACCOUNT_ID, V3xOrgEntity.ORGENT_TYPE_DEPARTMENT);
                for (V3xOrgEntity entity : canReadList) {
                    if (entity.getId().longValue() == currentMember.getId() || entity.getId().longValue() == currentMember.getOrgDepartmentId() || entity.getId().longValue() == currentMember.getOrgAccountId() || entity.getId().longValue() == lonAccountId || depIds.contains(entity.getId().longValue())) {
                        return true;
                    } else {
                        Map<Long, List<MemberPost>> map = orgManager.getConcurentPostsByMemberId(lonAccountId, currentMember.getId());
                        if (map != null && map.containsKey(entity.getId())) {
                            return true;
                        }
                    }
                }
                //同一组
                List<V3xOrgTeam> teams = orgManager.getTeamsByMember(member.getId(), member.getOrgAccountId());
                for (V3xOrgTeam t : teams) {
                    List<OrgTypeIdBO> m1 = t.getAllMembers();
                    for (OrgTypeIdBO mm : m1) {
                        if (mm.getLId().longValue() == currentMember.getId()) {
                            return true;
                        }
                    }
                }
                return false;
            } else if (!currentMember.getIsInternal() && member.getIsInternal()) {//外部访问内部
                V3xOrgDepartment curDep = orgManager.getDepartmentById(currentMember.getOrgDepartmentId());
                V3xOrgDepartment memDep = orgManager.getDepartmentById(member.getOrgDepartmentId());
                //跨靠部门
                if (curDep.getOrgAccountId().longValue() == memDep.getOrgAccountId() && curDep.getParentPath().equals(memDep.getParentPath())) {
                    return true;
                }
                List<Long> depIds = orgManager.getUserDomainIDs(member.getId(), V3xOrgEntity.VIRTUAL_ACCOUNT_ID, V3xOrgEntity.ORGENT_TYPE_DEPARTMENT);
                List<Long> accountIds = orgManager.getUserDomainIDs(member.getId(), V3xOrgEntity.VIRTUAL_ACCOUNT_ID, V3xOrgEntity.ORGENT_TYPE_ACCOUNT);
                if (outerRight != null && !outerRight.isEmpty()) {
                    for (V3xOrgEntity entity : outerRight) {
                        if (entity.getId().longValue() == member.getId()) {
                            return true;
                        }
                        if (entity.getId().longValue() == member.getOrgDepartmentId()) {
                            return true;
                        }
                        if (entity.getId().longValue() == member.getOrgAccountId()) {
                            return true;
                        }
                        if (depIds.contains(entity.getId().longValue())) {
                            return true;
                        }
                        if (accountIds.contains(entity.getId().longValue())) {
                            return true;
                        }
                    }
                }
                //同一组
                List<V3xOrgTeam> teams = orgManager.getTeamsByMember(currentMember.getId(), currentMember.getOrgAccountId());
                for (V3xOrgTeam t : teams) {
                    List<OrgTypeIdBO> m1 = t.getAllMembers();
                    for (OrgTypeIdBO mm : m1) {
                        if (mm.getLId().longValue() == member.getId()) {
                            return true;
                        }
                    }
                }
                return false;
            }

            // 副岗在这个部门的有权限
            if (MemberHelper.isSndPostContainDept(currentMember, member.getOrgDepartmentId())) {
                return true;
            }
            /*
            			int currentAccountLevelScope = orgManager.getAccountById(currentMember.getOrgAccountId()).getLevelScope();
            			if ((currentMember.getOrgDepartmentId().equals(member.getOrgDepartmentId()))
            					|| currentAccountLevelScope < 0) {
            				return true;
            			}
            			//切换的单位级别
            			int newAccountLevelScope = orgManager.getAccountById(member.getOrgAccountId()).getLevelScope();
            			if (newAccountLevelScope < 0) {
            				return true;
            			}
            			int currentMemberLevelSortId=0;
            			int accountLevelScope=0;
            			V3xOrgLevel memberLevel = orgManager.getLevelById(member.getOrgLevelId());
            			int memberLevelSortId = memberLevel!=null ? memberLevel.getLevelId() : 0;
            			if(currentMember.getOrgAccountId().equals(member.getOrgAccountId())){
            				V3xOrgLevel currentMemberLevel = orgManager.getLevelById(currentMember.getOrgLevelId());
            				currentMemberLevelSortId = currentMemberLevel!=null ? currentMemberLevel.getLevelId() : 0;
            				accountLevelScope = currentAccountLevelScope;
            			}else{
            				currentMemberLevelSortId = mappingLevelSortId(member,currentMember);
            				accountLevelScope = newAccountLevelScope;
            			}
            			if (currentMemberLevelSortId - memberLevelSortId <= accountLevelScope) {
            				return true;
            			}
            */return canAccess(departmentId, member);
        } catch (Exception e) {
            log.error("检测工作范围", e);
        }

        return false;
    }

    private List<MemberPost> getConcurentPosts(Long memberId, Long accountId, Long departmentId) throws BusinessException {
        Map<Long, List<MemberPost>> concurentPostsByMemberId = orgManager.getConcurentPostsByMemberId(accountId, memberId);
        if (concurentPostsByMemberId != null && !concurentPostsByMemberId.isEmpty()) {
            return concurentPostsByMemberId.get(departmentId);
        } else {
            return null;
        }
    }

    /**
     * 当前用户能否在部门departmentId内访问observedMember
     */
    private boolean canAccess(Long departmentId, V3xOrgMember observedMember) throws BusinessException {
        User user = AppContext.getCurrentUser();
        // 当前单位
        V3xOrgAccount currentAccount = orgManager.getAccountById(user.getLoginAccount());
        // 当前单位的工作范围设置
        int currentAccountLevelScope = currentAccount.getLevelScope();
        if (currentAccountLevelScope < 0) { // 无限制，则不必检查了
            return true;
        }
        if (departmentId != null) { // 在指定部门内是否可访问
            // 当前用户在当前部门的职务级别，取最高（数值最小）的那个
            int currentUserLevelId = Integer.MAX_VALUE;
            if (user.getAccountId() == currentAccount.getId().longValue()) { // 当前单位是当前用户的主岗单位
                currentUserLevelId = orgManager.getLevelById(user.getLevelId()).getLevelId();
            } else { // 当前单位是当前用户的兼职单位
                // 当前用户在当前部门的兼职职务级别
                List<MemberPost> concurentPosts = getConcurentPosts(user.getId(), currentAccount.getId(), departmentId);
                if (concurentPosts != null) {
                    for (MemberPost cp : concurentPosts) {
                        if (cp.getLevelId() != null) {
                            int s = orgManager.getLevelById(cp.getLevelId()).getLevelId();
                            if (s < currentUserLevelId) {
                                currentUserLevelId = s;
                            }
                        }
                    }
                }
            }
            // 当前待检人员在当前部门的职务级别，取最低（数值最大）的那个
            int observedLevelId = Integer.MIN_VALUE;
            if (observedMember.getOrgAccountId() == currentAccount.getId().longValue()) { // 当前单位是当前待检人员的主岗单位
                observedLevelId = orgManager.getLevelById(observedMember.getOrgLevelId()).getLevelId();
            } else { // 当前单位是当前待检人员的兼职单位
                // 当前待检人员在当前部门的兼职职务级别
                List<MemberPost> concurentPosts = getConcurentPosts(observedMember.getId(), currentAccount.getId(), departmentId);
                if (concurentPosts != null) {
                    for (MemberPost cp : concurentPosts) {
                        if (cp.getLevelId() != null) {
                            int s = orgManager.getLevelById(cp.getLevelId()).getLevelId();
                            if (s > observedLevelId) {
                                observedLevelId = s;
                            }
                        }
                    }
                }
            }
            if (currentUserLevelId - currentAccountLevelScope <= observedLevelId) {
                return true;
            } else {
                return false;
            }
        } else { // 在指定单位内，逐个检查下属部门
            List<V3xOrgDepartment> departments = orgManager.getAllDepartments(currentAccount.getId());
            if (departments != null) {
                for (V3xOrgDepartment department : departments) {
                    boolean canAccess = canAccess(department.getId(), observedMember);
                    if (canAccess) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public ModelAndView vcard(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int addressbookType = ServletRequestUtils.getIntParameter(request, "addressbookType");
        Long memberId = ServletRequestUtils.getLongParameter(request, "memberId");
        String exp_deptId = request.getParameter("exp_deptId");
        String exp_accountId = request.getParameter("exp_accountId");
        WebStaffInfo webStaffInfos = new WebStaffInfo();
        String officeNum = "";
        String filename = "";
        String filename1 = "";
        V3xOrgMember member = new V3xOrgMember();
        AddressBookMember ABmember = new AddressBookMember();

        VCard vc = new VCard();
        if (1 == addressbookType) {
            AddressBookSet addressBookSet = addressBookManager.getAddressbookSetByAccountId(AppContext.currentAccountId());
            if (addressBookSet == null) {
                addressBookSet = new AddressBookSet();
            }

            if (memberId != null) {
                member = orgManager.getMemberById(memberId);
                webStaffInfos = this.translateV3xOrgMembers(member, null);
                officeNum = member.getOfficeNum() == null ? "" : member.getOfficeNum();

            }
            String orgName = webStaffInfos.getOrg_name();
            String depName = webStaffInfos.getDepartment_name();
            filename = member.getName();
            filename1 = member.getLoginName();
            vc.setName(member.getName());
            vc.setComment(Strings.isBlank(webStaffInfos.getRemark()) ? "" : webStaffInfos.getRemark());
            if (addressBookSet.isMemberLevel()) {
                vc.setTitle(Strings.isBlank(webStaffInfos.getLevel_name()) ? "" : webStaffInfos.getLevel_name()); // 职务 （ 例 ：初级，高级）
                if (!addressBookManager.checkLevel(AppContext.currentUserId(), member.getId(), AppContext.currentAccountId(), addressBookSet)) {
                    vc.setTitle(AddressbookConstants.ADDRESSBOOK_INFO_REPLACE);
                }
            }
            vc.setOrganisation((Strings.isBlank(orgName) ? "" : orgName) + ";" + (Strings.isBlank(depName) ? "" : depName));//用友致远软件技术有限公司;研发二部
            if (addressBookSet.isMemberHome()) {
                vc.setAddress(Strings.isBlank(webStaffInfos.getAddress()) ? "" : webStaffInfos.getAddress());//家庭住址
            }
            if (addressBookSet.isMemberPhone()) {
                vc.setPhone(Strings.isBlank(officeNum) ? "" : officeNum);//商务电话
            }
            vc.setFax("");//商务传真
            vc.setHomeP(Strings.isBlank(webStaffInfos.getTelephone()) ? "" : webStaffInfos.getTelephone());//住宅电话
            if (addressBookSet.isMemberMobile()) {
                vc.setMobilePhone(Strings.isBlank(member.getTelNumber()) ? "" : member.getTelNumber());////移动电话
                if (!addressBookManager.checkPhone(AppContext.currentUserId(), member.getId(), AppContext.currentAccountId(), addressBookSet)) {
                    vc.setMobilePhone(AddressbookConstants.ADDRESSBOOK_INFO_REPLACE);
                }
            }
            if (addressBookSet.isMemberEmail()) {
                vc.setEmail(Strings.isBlank(member.getEmailAddress()) ? "" : member.getEmailAddress());
            }
        } else {//外部联系人
            if (memberId != null) {
                ABmember = addressBookManager.getMember(memberId);
            }
            String companyName = ABmember.getCompanyName();
            String departName = ABmember.getCompanyDept();
            String company = Strings.isNotBlank(companyName) ? companyName : "";
            String depart = Strings.isNotBlank(departName) ? departName : "";
            filename = ABmember.getName();//登陆名
            filename1 = ABmember.getName();//姓名
            vc.setName(Strings.isBlank(ABmember.getName()) ? "" : ABmember.getName());//卢呈祥
            vc.setComment(Strings.isBlank(ABmember.getMemo()) ? "" : ABmember.getMemo());
            vc.setTitle(Strings.isBlank(ABmember.getCompanyLevel()) ? "" : ABmember.getCompanyLevel()); //    职务    （ 例 ：初级，高级）
            vc.setOrganisation(company + ";" + depart);//      用友致远软件技术有限公司;研发二部
            vc.setAddress(Strings.isBlank(ABmember.getAddress()) ? "" : ABmember.getAddress());//家庭住址
            vc.setPhone(Strings.isBlank(ABmember.getCompanyPhone()) ? "" : ABmember.getCompanyPhone());//商务电话
            vc.setFax(Strings.isBlank(ABmember.getFax()) ? "" : ABmember.getFax());//商务传真
            vc.setHomeP(Strings.isBlank(ABmember.getFamilyPhone()) ? "" : ABmember.getFamilyPhone());//住宅电话
            vc.setMobilePhone(Strings.isBlank(ABmember.getMobilePhone()) ? "" : ABmember.getMobilePhone());////移动电话
            vc.setEmail(Strings.isBlank(ABmember.getEmail()) ? "" : ABmember.getEmail());//email

        }

        String name1 = "";
        String name2 = "";
        char[] tempChar = filename.toCharArray();
        for (int kk = 0; kk < tempChar.length; kk++) {
            if (kk == 0) {
                name1 += tempChar[kk];
            } else {
                name2 += tempChar[kk];
            }

        }
        vc.setNName(name1 + ";" + name2);//    卢;呈祥

        String str = vc.getVCard();

        String title1 = filename1 + ".vcf";
        request.setCharacterEncoding(contentTypeCharset);
        response.setCharacterEncoding(contentTypeCharset);
        response.setContentType("application/x-msdownload; charset=" + contentTypeCharset);
        title1 = URLEncoder.encode(title1, "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=\"" + title1 + "\"");

        OutputStream out = null;
        try {
            out = response.getOutputStream();
            if(1 == addressbookType){
                insertExpLog(exp_deptId,exp_accountId,String.valueOf(1));
            }
            IOUtils.write(str, out, "UTF-8");
        } catch (Exception e) {
            if (e.getClass().getSimpleName().equals(this.clientAbortExceptionName)) {
                log.error("用户关闭下载窗口: " + e.getMessage());
            } else {
                log.error("", e);
            }
        } finally {
            IOUtils.closeQuietly(out);
        }

        return null;
    }

    /**
     * 参照csvExport()导出CSV
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void exportAsCsv(HttpServletRequest request, HttpServletResponse response, List data) throws Exception {
        List<WebWithPropV3xOrgMember> colleagues = null;
        List<AddressBookMember> personals = null;
        
		int addressbookType = ServletRequestUtils.getIntParameter(request,
				"addressbookType", 1);
		
		AddressBookSet addressBookSet = addressBookManager.getAddressbookSetByAccountId(AppContext.currentAccountId());
		if (addressBookSet == null) {
		    addressBookSet = new AddressBookSet();
		}
        
        if (data == null || data.isEmpty()) {
        	cvsModel(request,response,addressbookType,addressBookSet);
            return;
        } else if (data.get(0) instanceof WebWithPropV3xOrgMember) {
            colleagues = data;
        } else if (data.get(0) instanceof AddressBookMember) {
            personals = data;
        }

        // 以下参照csvExport()
        List<Csv> csvs = null;
        if (colleagues != null) {
            csvs = generateCsvByColleague(colleagues, addressBookSet);
        } else if (personals != null) {
            csvs = generateCsvByPersonals(personals);
        }
        String exportStr = getCsvStringByCsvs(csvs, addressBookSet,addressbookType);
        String title1 = "WLMContacts" + ".csv";
        //request.setCharacterEncoding(contentTypeCharset);
        //response.setCharacterEncoding(contentTypeCharset);
        title1 = URLEncoder.encode(title1, "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=\"" + title1 + "\"");
        response.setContentType("text/csv; charset=" + contentTypeCharset);
        //response.setContentType("application/x-msdownload; charset=" + contentTypeCharset);
        String charset = "UTF-8";
        response.setCharacterEncoding(charset);
        OutputStream out = null;
        try {
            //			byte[] b = exportStr.getBytes();
            out = response.getOutputStream();
            //			IOUtils.write(b, out);
            // 写UTF-8 BOM信息 http://en.wikipedia.org/wiki/Byte_order_mark
            out.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
            out.write(exportStr.getBytes(charset));
            out.flush();
            out.close();
        } catch (Exception e) {
            if (e.getClass().getSimpleName().equals(this.clientAbortExceptionName)) {
                log.error("用户关闭下载窗口: " + e);
            } else {
                log.error("", e);
            }
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * CSV 导出
     * @deprecated 请用export()
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ModelAndView csvExport(HttpServletRequest request, HttpServletResponse response) throws Exception {
        AddressBookSet addressBookSet = addressBookManager.getAddressbookSetByAccountId(AppContext.currentAccountId());
    	boolean showExportPrint  = showExportPrint(AppContext.currentUserId(), addressBookSet);
    	if(!showExportPrint){
    		return null;
    	}
        Map<String, List> map = getDownList(request);
        if (map != null) {
            List<WebWithPropV3xOrgMember> colleagues = map.get("orgMembers");
            List<AddressBookMember> personals = map.get("addressMembers");
            List<Csv> csvs = null;
            if (colleagues != null) {
                csvs = generateCsvByColleague(colleagues, null);
            } else {
                if (personals != null) {
                    csvs = generateCsvByPersonals(personals);
                }
            }
            String exportStr = getCsvStringByCsvs(csvs, null,null);
            String title1 = "WLMContacts" + ".csv";
            //request.setCharacterEncoding(contentTypeCharset);
            //response.setCharacterEncoding(contentTypeCharset);
            title1 = URLEncoder.encode(title1, "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=\"" + title1 + "\"");
            response.setContentType("text/csv; charset=" + contentTypeCharset);
            //response.setContentType("application/x-msdownload; charset=" + contentTypeCharset);

            OutputStream out = null;
            try {
                byte[] b = exportStr.getBytes();
                out = response.getOutputStream();
                IOUtils.write(b, out);
            } catch (Exception e) {
                if (e.getClass().getSimpleName().equals(this.clientAbortExceptionName)) {
                    log.error("用户关闭下载窗口: " + e);
                } else {
                    log.error("", e);
                }
            } finally {
                IOUtils.closeQuietly(out);
            }
        }

        return null;
    }

    private List<Csv> generateCsvByColleague(List<WebWithPropV3xOrgMember> list, AddressBookSet addressBookSet) throws Exception {
        List<Csv> csvs = new ArrayList<Csv>();
        if (list != null) {
            for (WebWithPropV3xOrgMember m : list) {
                Csv c = new Csv();
                c.setCompayName(orgManager.getAccountById(m.getV3xOrgMember().getOrgAccountId()).getName());
                c.setDepartmentName(OrgHelper.showDepartmentFullPath(m.getV3xOrgMember().getOrgDepartmentId()));
                if (addressBookSet.isMemberLevel()) {
                    c.setLevelName(m.getLevelName());
                }
                c.setName(Functions.showMemberName(m.getV3xOrgMember()));
                if (addressBookSet.isMemberEmail()) {
                    c.setEmail(m.getV3xOrgMember().getEmailAddress());
                }
                if (addressBookSet.isMemberPhone()) {
                    c.setOfficePhone(m.getV3xOrgMember().getOfficeNum());
                }
                if (addressBookSet.isMemberMobile()) {
                    c.setMobilePhone(m.getMobilePhone());
                }
                csvs.add(c);
            }
        }
        return csvs;
    }

    private List<Csv> generateCsvByPersonals(List<AddressBookMember> list) {
        List<Csv> csvs = new ArrayList<Csv>();
        if (list != null) {
            for (AddressBookMember m : list) {
                Csv c = new Csv();
                c.setCompayName(m.getCompanyName());
                c.setMobilePhone(m.getMobilePhone());
                c.setEmail(m.getEmail());
                c.setBlogUrl(m.getBlog());
                c.setDepartmentName(m.getCompanyDept());
                c.setFamilyAddress(m.getAddress());
                c.setFamilyPost(m.getPostcode());
                c.setLevelName(m.getCompanyLevel());
                c.setMobilePhone(m.getMobilePhone());
                c.setName(m.getName());
                c.setOfficePhone(m.getCompanyPhone());
                csvs.add(c);
            }
        }
        return csvs;
    }

    private String getCsvStringByCsvs(List<Csv> csvs, AddressBookSet addressBookSet,Integer addressbookType) {
        StringBuilder buffer = new StringBuilder(getTitle(addressBookSet,addressbookType));
        buffer.append("\r\n");
        if (csvs != null) {
            for (Csv c : csvs) {
                if(addressbookType==2){
                    buffer.append(c.getCsv());
                }else{
                    buffer.append(c.getCsv(addressBookSet));
                }
            }
        }
        return buffer.toString();
    }

    private String getTitle(AddressBookSet addressBookSet,Integer addressbookType) {
        String resource = "com.seeyon.v3x.addressbook.resource.i18n.AddressBookResources";

        List<String> s = new ArrayList<String>();
        s.add(ResourceBundleUtil.getString(resource, "export.csv.name.title"));
        s.add(ResourceBundleUtil.getString(resource, "export.csv.name1.title"));
        s.add(ResourceBundleUtil.getString(resource, "export.csv.chinese.title"));
        s.add(ResourceBundleUtil.getString(resource, "export.csv.unit.title"));
        s.add(ResourceBundleUtil.getString(resource, "export.csv.dept.title"));
        if (addressbookType ==2 || addressBookSet.isMemberLevel()) {
            s.add(ResourceBundleUtil.getString(resource, "export.csv.level.title"));
        }
        if (addressbookType ==2 || addressBookSet.isMemberHome()) {
            s.add(ResourceBundleUtil.getString(resource, "export.csv.address.title"));
            s.add(ResourceBundleUtil.getString(resource, "export.csv.address1.title"));
        }
        if (addressbookType ==2 || addressBookSet.isMemberPhone()) {
            s.add(ResourceBundleUtil.getString(resource, "export.csv.telephone.title"));
        }
        if (addressbookType ==2 || addressBookSet.isMemberMobile()) {
            s.add(ResourceBundleUtil.getString(resource, "export.csv.mobile.title"));
        }
        if (addressbookType ==2 || addressBookSet.isMemberEmail()) {
            s.add(ResourceBundleUtil.getString(resource, "export.csv.email.title"));
            s.add(ResourceBundleUtil.getString(resource, "export.csv.email1.title"));
            s.add(ResourceBundleUtil.getString(resource, "export.csv.email2.title"));
        }
        s.add(ResourceBundleUtil.getString(resource, "export.csv.web.title"));

        StringBuilder ss = new StringBuilder();
        for (String st : s) {
            if (Strings.isNotBlank(st)) {
                ss.append("\"");
                ss.append(st);
                ss.append("\"");
                ss.append(",");
            }
        }
        return ss.toString();
    }

    public ModelAndView importCard(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("addressbook/io/importVcard");
        return mav;
    }

    public ModelAndView importExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("addressbook/io/importExcel");
        return mav;
    }

    public ModelAndView doVcardImoprt(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String memberId = request.getParameter("memberId");
        String categoryId = request.getParameter("categoryId");
        File file = uploadFile(request);
        String path = file.getAbsolutePath() + ".vcf";
        File realfile = new File(path);
        DataUtil.CopyFile(file, realfile);

        String result = addressBookManager.doImport(realfile, categoryId, memberId);

        if ("OK".equals(result)) {
            super.rendJavaScript(response, "parent.window.location.reload();");
        } else if ("ExistSameName".equals(result)) {
            super.rendJavaScript(response, "alert(parent.v3x.getMessage(\"ADDRESSBOOKLang.addressbook_isexist_name\"));");
        }

        return null;
    }

    public ModelAndView doCSVImoprt(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String memberId = request.getParameter("memberId");
        String categoryId = request.getParameter("categoryId");
        File file = uploadFile(request);
        String path = file.getAbsolutePath() + ".csv";
        File realfile = new File(path);
        DataUtil.CopyFile(file, realfile);

        addressBookManager.doCsvImport(realfile, categoryId, memberId);
        super.rendJavaScript(response, "parent.parent.location.href=parent.parent.location;");
        return null;
    }

    public ModelAndView creatVcardByUc(HttpServletRequest request, HttpServletResponse response) throws Exception {
        boolean isCreated = ServletRequestUtils.getBooleanParameter(request, "isCreated", false);
        String crtId = request.getParameter("categoryId");
        AddressBookMember member = new AddressBookMember();
        bind(request, member);
        member.setCategory(NumberUtils.toLong(crtId, -1l));
        member.setCreatorId(AppContext.getCurrentUser().getId());
        member.setCreatorName(AppContext.getCurrentUser().getLoginName());
        Date operatingTime = new Date();
        member.setCreatedTime(operatingTime);
        member.setModifiedTime(operatingTime);
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        if ((isCreated) && addressBookManager.isExistSameUserName(member, AppContext.getCurrentUser().getId())) {
            response.setCharacterEncoding("utf-8");
            PrintWriter out = response.getWriter();
            jsonObject.put("res", "2");
            jsonArray.put(jsonObject);
            out.write(jsonArray.toString());
            return null;
        }
        if (isCreated) {
            this.addressBookManager.addMember(member);
        }
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        jsonObject.put("res", "1");
        jsonArray.put(jsonObject);
        out.write(jsonArray.toString());
        return null;
    }

    @SuppressWarnings("rawtypes")
    private File uploadFile(HttpServletRequest request) throws Exception {
        File fil = null;
        try {
            V3XFile v3x = null;
            List v3xFiles = fileManager.create(ApplicationCategoryEnum.edoc, request);
            if (null != v3xFiles && v3xFiles.size() > 0) {
                v3x = (V3XFile) v3xFiles.get(0);
                fil = fileManager.getFile(v3x.getId(), v3x.getCreateDate());
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return fil;
    }
    @CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator})
    public ModelAndView showAddSet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("addressbook/showAddSet");
        Long accountId = AppContext.currentAccountId();
        AddressBookSet bean = addressBookManager.getAddressbookSetByAccountId(accountId);
        if (bean == null) {
            bean = new AddressBookSet();
        }
        mav.addObject("bean", bean);
        
    	//查看通讯录分类的id
    	Long categoryId=0L;
    	Map<String, Object> categoryParams =new HashMap<String, Object>();
    	categoryParams.put("moduleType", 14);
    	FlipInfo categoryFi = metadataCategoryManager.findMetadataCategoryList(new FlipInfo(), categoryParams);
    	List<MetadataCategoryBO> l=categoryFi.getData();
    	if(null!=l && l.size()>0){
    		 categoryId=l.get(0).getId();
    	}
    	
        FlipInfo fi=new FlipInfo();
        fi.setSize(100);
        Map<String, Object> sqlParams =new HashMap<String, Object>();
        sqlParams.put("isEnable", 1);
        sqlParams.put("categoryId", categoryId);
        List<MetadataColumnBO> metadataColumnBOList =new ArrayList<MetadataColumnBO>();
        FlipInfo result=metadataColumnManager.findCtpMetadataColumnList(fi, sqlParams);
        if(null!=result){
        	metadataColumnBOList=result.getData();
        }
        
        mav.addObject("customerBean", metadataColumnBOList);
        return mav;
    }

    @CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator})
    public ModelAndView updateAddSet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long accountId = AppContext.currentAccountId();

        AddressBookSet bean = addressBookManager.getAddressbookSetByAccountId(accountId);
        boolean isNew = true;
        if (bean == null) {
            bean = new AddressBookSet();
            bean.setAccountId(accountId);
        } else {
            isNew = false;
        }
        super.bind(request, bean);

        String displayColumn = "memberName,memberDept";
        String[] displayColumns = request.getParameterValues("displayColumn");
        if (displayColumns != null && displayColumns.length > 0) {
            for (String str : displayColumns) {
                displayColumn += "," + str;
            }
        }
        bean.setDisplayColumn(displayColumn);
        
        if(bean.getDisplayColumn()!=null && bean.getDisplayColumn().indexOf("workLocal")==-1){
        	bean.setWorkLocal(false);
        }else{
        	bean.setWorkLocal(true);
        }

        if (bean.getViewScope() == AddressbookConstants.ADDRESSBOOK_VIEWSCOPE_1) {
            bean.setViewScopeIds(request.getParameter("followingHideS"));
        } else if (bean.getViewScope() == AddressbookConstants.ADDRESSBOOK_VIEWSCOPE_2) {
            bean.setViewScopeIds(request.getParameter("followingViewS"));
        }
        if (bean.getKeyInfo() == AddressbookConstants.ADDRESSBOOK_KEYINFO_2) {
            bean.setKeyInfoIds(request.getParameter("followingNotOpenK"));
        } else if (bean.getKeyInfo() == AddressbookConstants.ADDRESSBOOK_KEYINFO_3) {
            bean.setKeyInfoIds(request.getParameter("followingOpenK"));
        }
        if (bean.getExportPrint() == AddressbookConstants.ADDRESSBOOK_EXPORTPRINT_2) {
            bean.setExportPrintIds(request.getParameter("followingCanEp"));
        }

        addressBookManager.saveAddressbookSet(bean, isNew);

        return super.redirectModelAndView("addressbook.do?method=showAddSet"+Functions.csrfSuffix());
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
	}

    /**
     * 下载csv模版
     * @param request
     * @param response
     * @param addressbookType
     * @param addressBookSet
     * @throws Exception
     */
    public void cvsModel (HttpServletRequest request,
            HttpServletResponse response,Integer addressbookType,AddressBookSet addressBookSet) throws Exception{
        String exportStr = getCsvStringByCsvs(null, addressBookSet,addressbookType);
        String title1 = "WLMContacts"+".csv";
        title1 = URLEncoder.encode(title1, "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=\"" + title1 + "\"");
        response.setContentType("text/csv; charset=" + contentTypeCharset);
        String charset = "UTF-8";
        response.setCharacterEncoding(charset);
        OutputStream out = null;
        try {
            out = response.getOutputStream();
            out.write(new byte[] { (byte) 0xEF, (byte) 0xBB,(byte) 0xBF });
            out.write(exportStr.getBytes(charset));
            out.flush();
            out.close();
        }
        catch (Exception e) {
            if (e.getClass().getSimpleName().equals(this.clientAbortExceptionName)) {
                log.error("用户关闭下载窗口: " + e);
            }
            else{
                log.error("", e);
            }
        }
        finally{
            IOUtils.closeQuietly(out);
        }
    }

    public <T> List<T> pagenate(HttpServletRequest request, List<T> list) {
    	if("export".equals(request.getParameter("method"))){
    		return list;
    	}
        if (CollectionUtils.isNotEmpty(list)) {
            int size = list.size();

            int pageSize = NumberUtils.toInt(request.getParameter("pageSize"), Pagination.getMaxResults());
            if (pageSize < 1) {
                pageSize = Pagination.getMaxResults();
            }

            int pages = (size + pageSize - 1) / pageSize;
            if (pages < 1) {
                pages = 1;
            }

            int page = NumberUtils.toInt(request.getParameter("page"), 1);
            if (page < 1) {
                page = 1;
            } else if (page > pages) {
                page = pages;
            }

            Integer first = (page - 1) * pageSize;
            Pagination.setFirstResult(first);
            Pagination.setMaxResults(first + pageSize);
            return CommonTools.pagenate(list);
        }
        return list;
    }
    
    //张三导出了研发中心（致远软件）100条通讯录
    //张三导出了致远软件1条通讯录
    private void insertExpLog(String exp_deptId,String exp_accountId,String dataSize) throws BusinessException{
        String deptName="";
        String accountName="";
        if(Strings.isNotEmpty(exp_deptId)){
            V3xOrgAccount account=orgManager.getAccountById(Long.valueOf(exp_accountId));
            accountName="("+(Strings.isNotEmpty(account.getShortName())?account.getShortName():account.getName())+")";
            deptName=orgManager.getDepartmentById(Long.valueOf(exp_deptId)).getName();
        }else if(Strings.isNotEmpty(exp_accountId)){
            V3xOrgAccount account=orgManager.getAccountById(Long.valueOf(exp_accountId));
            accountName=(Strings.isNotEmpty(account.getShortName())?account.getShortName():account.getName());
        }else if(Strings.isEmpty(exp_deptId) && Strings.isEmpty(exp_accountId)){
            accountName=Strings.isNotEmpty(AppContext.getCurrentUser().getLoginAccountShortName())?AppContext.getCurrentUser().getLoginAccountShortName():AppContext.getCurrentUser().getLoginAccountName();
        }
        appLogManager.insertLog(AppContext.getCurrentUser(), 1141, AppContext.getCurrentUser().getName(),deptName+accountName,dataSize);
    }
    
    public void getMemberInfoById(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Map<String,String> content = new HashMap<String, String>();
        String memberId =request.getParameter("memberId");
        String accountId =request.getParameter("accountId");
        V3xOrgMember member = orgManager.getMemberById(Long.valueOf(memberId));
        String deptName = "";
        String postName = "";
        String secondPostStr = "";
        String levelName = "";
        String mobilePhone = member.getTelNumber();
        if(member.getIsInternal()){
        	content.put("isInternal", "true");
        	if(String.valueOf(orgManager.getRootAccount().getId()).equals(accountId) || Strings.isBlank(accountId)){
        		accountId=String.valueOf(member.getOrgAccountId());
        	}
        	List<MemberPost> result = orgManager.getMemberPosts(Long.valueOf(accountId), Long.valueOf(memberId));
        	if(null==result || result.size()<=0){
        		 deptName = makedeptlevelstr(Long.valueOf(member.getOrgDepartmentId().toString()));
        		 postName = orgManager.getPostById(member.getOrgPostId()).getName();
        		 levelName = orgManager.getLevelById(member.getOrgLevelId()).getName();
        	}else{
        		for(MemberPost mp : result){
        			if(null!=mp.getDepId() && null!=mp.getPostId() && null!= mp.getLevelId()){
        				deptName = makedeptlevelstr(Long.valueOf(mp.getDepId().toString()));
        				postName = orgManager.getPostById(mp.getPostId()).getName();
        				levelName = orgManager.getLevelById(mp.getLevelId()).getName();
        				break;
        			}
        		}
        	}
        	 //内部人员副岗
            //回写副岗信息
            StringBuilder secondPost = new StringBuilder();
            List<MemberPost> secondPostList = member.getSecond_post();
            for (int i = 0; i < secondPostList.size(); i++) {
                MemberPost m = secondPostList.get(i);
                //拼接成[部门名称-岗位名称]前台显示
                V3xOrgDepartment _secondDept = orgManager.getDepartmentById(m.getDepId());
                if(_secondDept == null) continue;
                secondPost.append(_secondDept.getName());
                secondPost.append("-");
                secondPost.append(OrgHelper.showOrgPostName(m.getPostId()));
                if(i != secondPostList.size()-1) {
                    secondPost.append("、");
                }
            }
            secondPostStr = secondPost.toString();
        	AddressBookSet addressBookSet = addressBookManager.getAddressbookSetByAccountId(Long.valueOf(accountId));
        	if (!addressBookManager.checkLevel(AppContext.currentUserId(), member.getId(), AppContext.getCurrentUser().getAccountId(), addressBookSet)) {
        		levelName = AddressbookConstants.ADDRESSBOOK_INFO_REPLACE;
        	}
        	
        	if (!addressBookManager.checkPhone(AppContext.currentUserId(), member.getId(), AppContext.getCurrentUser().getAccountId(), addressBookSet)) {
        		mobilePhone = AddressbookConstants.ADDRESSBOOK_INFO_REPLACE;
        	}
        }else{
        	content.put("isInternal", "false");
        	deptName = makedeptlevelstr(Long.valueOf(member.getOrgDepartmentId().toString()));
        	postName = OrgHelper.getExtMemberPriPost(member);
        	levelName = OrgHelper.getExtMemberLevel(member);
        }
        
        
	        String memberName = OrgHelper.showMemberName(member.getId());
	        String officeNum = member.getOfficeNum();
	        String email = member.getEmailAddress();
	        String gender = member.getGender().toString();
	        String code = member.getCode();
	        String postCode = member.getPostalcode();
	        String website = member.getWebsite();
	        String blog = member.getBlog();
	        String weixin = member.getWeixin();
	        String memo = member.getDescription();
	        String fileName = Functions.getAvatarImageUrl(Long.valueOf(memberId));
            PeopleRelateBO relate = peopleRelateApi.getPeopleRelate(member.getId(),AppContext.currentUserId());
            String relateType = "";
            if(relate!=null){
                int tempType = (null==relate.getRelateType())?0:relate.getRelateType();
                switch(tempType){
                    case 1:
                        relateType = ResourceUtil.getString("relate.type.leader");
                        break;
                    case 2:
                        relateType = ResourceUtil.getString("relate.type.junior");
                        break;
                    case 3:
                        relateType = ResourceUtil.getString("relate.type.assistant");
                        break;
                    case 4:
                        relateType = ResourceUtil.getString("relate.type.confrere");
                        break;
                }
            }
            String location = "";
            String reporter = "";
            if(Strings.isNotBlank(member.getLocation())){
            	location=enumManagerNew.parseToName(member.getLocation());
            }
            if(null!=member.getReporter()){
            	if(member.getReporter().compareTo(-1L)!=0){
            		reporter=orgManager.getMemberById(member.getReporter()).getName();
            	}
            }
	        content.put("memberName", memberName);
	        content.put("memberId", memberId);
	        content.put("deptName", deptName);
	        content.put("postName", postName);
	        content.put("secondPost", secondPostStr);
	        content.put("officeNum", (null==officeNum)?"":officeNum);
	        content.put("mobilePhone", (null==mobilePhone)?"":mobilePhone);
	        content.put("email", (null==email)?"":email);
	        content.put("gender", gender);
	        content.put("code", (null==code)?"":code);
	        content.put("levelName", levelName);
	        content.put("postCode", (null==postCode)?"":postCode);
	        content.put("website", (null==website)?"":website);
	        content.put("blog", (null==blog)?"":blog);
	        content.put("weixin", (null==weixin)?"":weixin);
	        content.put("memo", (null==memo)?"":memo);
	        content.put("fileName", fileName);
	        content.put("relateType", relateType);
	        content.put("location", location);
	        content.put("reporter", reporter);
	        int customerAddressbookSize=0;
	        //集团设置的可用的自定义字段,全部显示在人员卡片
	        List<MetadataColumnBO> metadataColumnList=addressBookManager.getCustomerAddressBookList();
	        if(null != metadataColumnList && metadataColumnList.size()>0){
	        	customerAddressbookSize=metadataColumnList.size();
	        	Map<String,String> customerAddressbookValueMap=getCustomerAddressbookValueMap(metadataColumnList,Long.valueOf(memberId));
	        	for(int i=0;i<customerAddressbookSize;i++){
	        		content.put("customerName"+i, metadataColumnList.get(i).getLabel());
	        		content.put("customerValue"+i, customerAddressbookValueMap.get(String.valueOf(metadataColumnList.get(i).getId())));
	        	}
	        }
	        content.put("customerAddressbookSize", String.valueOf(customerAddressbookSize));
        super.rendText(response, JSONUtil.toJSONString(content));
    }
    
    /**
     * 获取部门上级部门全路径
     * @param deptid
     * @return
     * @throws BusinessException
     */
    private String makedeptlevelstr(Long deptid) throws BusinessException{

    	V3xOrgUnit self = orgManager.getUnitById(deptid);
    	String str=self.getName();
    	V3xOrgUnit paraent = orgManager.getUnitById(self.getSuperior());
        if (null != paraent && OrgConstants.UnitType.Department.equals(paraent.getType())) {
            str = makedeptlevelstr(paraent.getId()) + "/" + str;
        }

    	return str;
    }
    
    
    private String getMemberJson(List<WebWithPropV3xOrgMember> memberList){
    	StringBuilder json = new StringBuilder();
    	json.append("[");
    	int i = 0;
    	for(WebWithPropV3xOrgMember m : memberList){
    		String Id = (null==m.getV3xOrgMember().getId())?"":m.getV3xOrgMember().getId().toString();
    		String name = (null==m.getMemberName())?"":m.getMemberName();
    		String postName = (null==m.getPostName())?"":m.getPostName();
    		String secondPost = (null==m.getSecondPosts())?"":m.getSecondPosts();
    		String deptName = (null==m.getDepartmentName())?"":m.getDepartmentName();
 			String officeNum = (null==m.getV3xOrgMember().getOfficeNum())?"":m.getV3xOrgMember().getOfficeNum();
 			String mobilePhone = (null==m.getMobilePhone())?"":m.getMobilePhone();
 			String emailAddress = (null==m.getV3xOrgMember().getEmailAddress())?"":m.getV3xOrgMember().getEmailAddress();
 			String fileName = (null==m.getFileName())?"":m.getFileName();
 			String memberInfo = Id+"_"+name;
 			
 			if(i++ > 0){
 				json.append(",");
 			}
 			
 			json.append("{");
 			json.append("\"I\":\""+Id+"\",");
 			json.append("\"N\":\""+Strings.escapeJavascript(name)+"\",");
 			json.append("\"P\":\""+Strings.escapeJavascript(postName)+"\",");
 			json.append("\"SP\":\""+Strings.escapeJavascript(secondPost)+"\",");
 			json.append("\"D\":\""+Strings.escapeJavascript(deptName)+"\",");
 			json.append("\"O\":\""+Strings.escapeJavascript(officeNum)+"\",");
 			json.append("\"M\":\""+Strings.escapeJavascript(mobilePhone)+"\",");
 			json.append("\"F\":\""+fileName+"\",");
 			json.append("\"E\":\""+Strings.escapeJavascript(emailAddress)+"\",");
 			json.append("\"MI\":\""+Strings.escapeJavascript(memberInfo)+"\"");
 			
 			json.append("}");
    	}
    	json.append("]");
    	
    	return json.toString();
    }
}

