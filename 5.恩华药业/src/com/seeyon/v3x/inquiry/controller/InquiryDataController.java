package com.seeyon.v3x.inquiry.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.agent.utils.AgentUtil;
import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.inquiry.constants.InquiryConstants;
import com.seeyon.apps.show.api.ShowApi;
import com.seeyon.apps.show.bo.ShowbarInfoBO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.cache.etag.ETagCacheManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.excel.DataRecord;
import com.seeyon.ctp.common.excel.FileToExcelManager;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.organization.bo.MemberPost;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgPost;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.portal.api.PortalApi;
import com.seeyon.ctp.portal.util.Constants.SpaceType;
import com.seeyon.ctp.report.chart2.core.ChartRender;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.common.security.AccessControlBean;
import com.seeyon.v3x.inquiry.domain.InquiryAffair;
import com.seeyon.v3x.inquiry.domain.InquiryAuthority;
import com.seeyon.v3x.inquiry.domain.InquiryScope;
import com.seeyon.v3x.inquiry.domain.InquirySurveybasic;
import com.seeyon.v3x.inquiry.domain.InquirySurveytype;
import com.seeyon.v3x.inquiry.domain.InquirySurveytypeextend;
import com.seeyon.v3x.inquiry.domain.InquiryVotedefinite;
import com.seeyon.v3x.inquiry.manager.InquiryAffairManager;
import com.seeyon.v3x.inquiry.manager.InquiryManager;
import com.seeyon.v3x.inquiry.util.InquiryLock;
import com.seeyon.v3x.inquiry.util.InquiryLockAction;
import com.seeyon.v3x.inquiry.util.InquiryUtil;
import com.seeyon.v3x.inquiry.vo.InquiryBasicVo;
import com.seeyon.v3x.inquiry.vo.InquirySurveyVoterVO;
import com.seeyon.v3x.inquiry.vo.SurveyBasicCompose;
import com.seeyon.v3x.inquiry.vo.SurveyTypeCompose;

/**
 * 6.0新调查的主要Controller
 * @author kygz
 */
public class InquiryDataController extends BaseController{
	private static final Log    logger        = LogFactory.getLog(InquiryDataController.class);
    private InquiryManager      inquiryManager;
    private InquiryAffairManager inquiryAffairManager;
    private OrgManager          orgManager;
    private AttachmentManager   attachmentManager;
    private UserMessageManager  userMessageManager;
    private FileToExcelManager  fileToExcelManager;
    private AppLogManager       appLogManager;
    private DocApi              docApi;
    private CollaborationApi 	collaborationApi;
    private AffairManager       affairManager;
    private PortalApi           portalApi;
    private ChartRender         chartRender;
    private  ShowApi			showApi;
    private ETagCacheManager    eTagCacheManager;
    
    public void seteTagCacheManager(ETagCacheManager eTagCacheManager) {
        this.eTagCacheManager = eTagCacheManager;
    }
    	
	public void setShowApi(ShowApi showApi) {
		this.showApi = showApi;
	}
	public ChartRender getChartRender() {	return chartRender;}
	public void setChartRender(ChartRender chartRender) {
		this.chartRender = chartRender;
	}
	public InquiryManager getInquiryManager() {	return inquiryManager;}
	public void setInquiryManager(InquiryManager inquiryManager) {
		this.inquiryManager = inquiryManager;
	}
    public InquiryAffairManager getInquiryAffairManager() {
        return inquiryAffairManager;
    }
    public void setInquiryAffairManager(InquiryAffairManager inquiryAffairManager) {
        this.inquiryAffairManager = inquiryAffairManager;
    }
    public OrgManager getOrgManager() {
		return orgManager;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public AttachmentManager getAttachmentManager() {
		return attachmentManager;
	}
	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}
	public UserMessageManager getUserMessageManager() {
		return userMessageManager;
	}
	public void setUserMessageManager(UserMessageManager userMessageManager) {
		this.userMessageManager = userMessageManager;
	}
	public FileToExcelManager getFileToExcelManager() {
		return fileToExcelManager;
	}
	public void setFileToExcelManager(FileToExcelManager fileToExcelManager) {
		this.fileToExcelManager = fileToExcelManager;
	}
	public AppLogManager getAppLogManager() {
		return appLogManager;
	}
	public void setAppLogManager(AppLogManager appLogManager) {
		this.appLogManager = appLogManager;
	}
	public DocApi getDocApi() {
		return docApi;
	}
	public void setDocApi(DocApi docApi) {
		this.docApi = docApi;
	}
	public CollaborationApi getCollaborationApi() {
		return collaborationApi;
	}
	public void setCollaborationApi(CollaborationApi collaborationApi) {
		this.collaborationApi = collaborationApi;
	}
	public AffairManager getAffairManager() {
		return affairManager;
	}
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public void setPortalApi(PortalApi portalApi) {
        this.portalApi = portalApi;
    }
    /**
     * 6.0调查首页
     * 版块类型 2-单位 3-集团
     */
	public ModelAndView inquiryIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    int spaceType = NumberUtils.toInt(request.getParameter("spaceType"));
        Long spaceId = NumberUtils.toLong(request.getParameter("spaceId"));
    	ModelAndView mav = new ModelAndView("inquiry/inquiryIndex");
        
        String accountName = orgManager.getAccountById(AppContext.getCurrentUser().getLoginAccount()).getShortName();
        String groupName = orgManager.getRootAccount().getShortName();
        
        mav.addObject("accountName", accountName);
        mav.addObject("groupName", groupName);
        //授权范围  自定义空间使用权限
        if (spaceType == SpaceType.public_custom.ordinal() || spaceType == SpaceType.public_custom_group.ordinal()) {
            List<Object[]> entityObj = portalApi.getSecuityOfSpace(spaceId);
            String entity = "";
            for (Object[] obj : entityObj) {
                entity += obj[0] + "|" + obj[1] + ",";
            }
            if (!"".equals(entity)) {
                entity = entity.substring(0, entity.length() - 1);
            }
            mav.addObject("entity", entity);
        }
        mav = inquiryMavData(mav,"index",null,spaceType, spaceId);
        
        /**秀吧最热列表*/
        if(AppContext.hasResourceCode("F05_show")){
        	List<ShowbarInfoBO> showbarHotList = showApi.findShowbarHotList(5);
        	mav.addObject("showbarHotList", showbarHotList);
        }
        
        
        return mav;
    }

    /**
     * 版块首页
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView inquiryBoardIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	ModelAndView mav = new ModelAndView("inquiry/inquiryBoardIndex");
    	String manageMode = request.getParameter("manageMode");
    	String boardType = request.getParameter("boardType");
    	
    	int spaceType = NumberUtils.toInt(request.getParameter("spaceType"));
        Long spaceId = NumberUtils.toLong(request.getParameter("spaceId"));
    	
        List<SurveyTypeCompose> inquiryTypeList = new ArrayList<SurveyTypeCompose>(); // 调查类型列表
        List<SurveyTypeCompose> groupInquiryTypeList = null; // 调查类型列表
        List<SurveyTypeCompose> accountInquiryTypeList = null; // 调查类型列表
        List<SurveyTypeCompose> customInquiryTypeList = null; // 自定义团队 调查类型列表
        User user = AppContext.getCurrentUser();
        Long inquiryBoardId = Long.parseLong(request.getParameter("boardId"));
        
        String accountName = orgManager.getAccountById(AppContext.getCurrentUser().getLoginAccount()).getShortName();
        String groupName = orgManager.getRootAccount().getShortName();
        boolean hasIssue = false;
        boolean hasAuthIssue = false;
        if (spaceType == SpaceType.public_custom.ordinal()) {// 自定义单位版块
            accountInquiryTypeList = inquiryManager.getUserIndexInquiryList(spaceId, spaceType, false);
        }else if(spaceType == SpaceType.public_custom_group.ordinal()){// 自定义集团版块
            groupInquiryTypeList = inquiryManager.getUserIndexInquiryList(spaceId, spaceType, false);
        }else if(spaceType == SpaceType.custom.ordinal()){// 自定义团队版块直接进入版块首页
            customInquiryTypeList = inquiryManager.getUserIndexInquiryList(spaceId, spaceType, false);
        }else {
            groupInquiryTypeList = inquiryManager.getUserIndexInquiryList(true, false);
            accountInquiryTypeList = inquiryManager.getUserIndexInquiryList(false, false);
        }

        if(groupInquiryTypeList!=null){
        	groupInquiryTypeList = inquiryManager.complateMangerRelation(groupInquiryTypeList);
        	inquiryTypeList.addAll(groupInquiryTypeList);
        }
        if(accountInquiryTypeList!=null){
        	accountInquiryTypeList = inquiryManager.complateMangerRelation(accountInquiryTypeList);
        	inquiryTypeList.addAll(accountInquiryTypeList);
        }
        if(customInquiryTypeList!=null){
            customInquiryTypeList = inquiryManager.complateMangerRelation(customInquiryTypeList);
            inquiryTypeList.addAll(customInquiryTypeList);
        }
        List<Long> typeIdsList = new ArrayList<Long>();
        typeIdsList.add(inquiryBoardId);
        
        List<Long> typeIdsList1 = new ArrayList<Long>();
        for (SurveyTypeCompose type : inquiryTypeList) {
        	typeIdsList1.add(type.getInquirySurveytype().getId());
        }
        //抽取调查
        List<InquiryBasicVo> inquiryBasicTempListAll = new ArrayList<InquiryBasicVo>();
        List<Integer> numList = inquiryManager.getIndexMyNum(inquiryBasicTempListAll,typeIdsList1);
        List<Integer> boardNumList = inquiryManager.getBoardInquiryNum(inquiryBoardId);
        //去掉已经离职的管理员
        for (SurveyTypeCompose bt : inquiryTypeList) {
            List<Long> managersList = bt.getManagers();
            String managerId = "";
            if (managersList != null) {
                for (Long a : managersList) {
                    managerId+=a.toString()+",";
                }
                if((!Strings.isBlank(managerId))&&",".equals(managerId.substring(managerId.length()-1,managerId.length()))){
                	managerId = managerId.substring(0,managerId.length()-1);
                }
            }
            if(bt.isHasPublicAuth()){
            	hasIssue = true;
            }
            if(bt.getChecker()!=null&&user.getId().equals(bt.getChecker().getId())){
                hasAuthIssue = true;
            }
            if(bt.getInquirySurveytype().getId().equals(inquiryBoardId)){
        		mav.addObject("board", bt);
        		mav.addObject("boardManager", managerId);
        	}
        }
        int manageNum = 0;
        if("group".equals(boardType)){
        	for(SurveyTypeCompose s: groupInquiryTypeList){
        		if(s.isHasManageAuth()){
        			manageNum++;
        		}
        	}
            
        }
        if("account".equals(boardType)){
        	for(SurveyTypeCompose s: accountInquiryTypeList){
        		if(s.isHasManageAuth()){
        			manageNum++;
        		}
        	}
        }
        if("custom".equals(boardType)){
            for(SurveyTypeCompose s: customInquiryTypeList){
                if(s.isHasManageAuth()){
                    manageNum++;
                }
            }
        }
        
        mav.addObject("accountName", accountName);
        mav.addObject("groupName", groupName);
        mav.addObject("typeList", inquiryTypeList);
        mav.addObject("groupInquiryTypeList", groupInquiryTypeList);
        mav.addObject("accountInquiryTypeList", accountInquiryTypeList);
        mav.addObject("customInquiryTypeList", customInquiryTypeList);
        mav.addObject("groupInquiryTypeListPageNum", groupInquiryTypeList==null?0:Math.ceil(groupInquiryTypeList.size()/(double)5));
        mav.addObject("accountInquiryTypeListPageNum", accountInquiryTypeList==null?0:Math.ceil(accountInquiryTypeList.size()/(double)5));
        mav.addObject("customInquiryTypeListPageNum", customInquiryTypeList==null?0:Math.ceil(customInquiryTypeList.size()/(double)5));
        mav.addObject("hasIssue", hasIssue);
        mav.addObject("hasAuthIssue", hasAuthIssue);
        mav.addObject("iJoined", numList.get(0));
        mav.addObject("iPublished", numList.get(1));
        mav.addObject("iAuth", numList.get(2));
        mav.addObject("boardGoing", boardNumList.get(0));
        mav.addObject("boardTotal", boardNumList.get(1));
        mav.addObject("boardType", request.getParameter("boardType"));
        mav.addObject("boardPageNo", request.getParameter("boardPageNo"));
        mav.addObject("manageMode", manageMode);
        mav.addObject("manageMove", manageNum>1);
        return mav;
    }

    /**
     * 板块授权设置
     * @throws Exception 
     * 
     * */
    public ModelAndView inquiryTypeAuthSet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("inquiry/inquiryTypeAuthSet");
        String typeId = request.getParameter("typeId");
        String orgType = request.getParameter("orgType");
        String spaceType = request.getParameter("spaceType");
        String spaceId = request.getParameter("spaceId");
        InquirySurveytype inquirySurveytype = inquiryManager.getSurveyTypeById(NumberUtils.toLong(typeId));
        Integer authType = null;
        Integer anonymousFlag = null;
        if (inquirySurveytype != null) {
            authType = inquirySurveytype.getAuthType();
            anonymousFlag = inquirySurveytype.getAnonymousFlag();
        }
        mav.addObject("authType", authType);
        mav.addObject("anonymousFlag", anonymousFlag);
        mav.addObject("orgType", orgType);
        mav.addObject("spaceType", spaceType);
        mav.addObject("spaceId", spaceId);
        mav.addObject("typeId", typeId);
        String auList = null;
        if (authType == 1) {
            auList = inquiryManager.getAuthoritiesList(Long.parseLong(typeId)).getAuthlist();
        }
        mav.addObject("auList", auList);
        List<V3xOrgEntity> auListEntity = new ArrayList<V3xOrgEntity>();
        if (Strings.isNotBlank(auList)) {
            auListEntity = orgManager.getEntities(auList.substring(0, auList.length() - 1));
        }
        
        if (Strings.isNotBlank(spaceId)) {
            List<Object[]> entityObj = portalApi.getSecuityOfSpace(Long.parseLong(spaceId));
            String entity = "";
            for (Object[] obj : entityObj) {
                entity += obj[0] + "|" + obj[1] + ",";
            }
            if (!"".equals(entity)) {
                entity = entity.substring(0, entity.length() - 1);
            }
            mav.addObject("entity", entity);
        } 
        
        mav.addObject("auListEntity", auListEntity);
        return mav;
    }

    /**
     * 调查版块授权设置提交
     * @param request
     * @param response
     * @return
     * @throws Exception 
     */
    public ModelAndView modifyTypeAauth(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String surveytype_id = request.getParameter("typeId");
        Long surveytypeId = NumberUtils.toLong(surveytype_id);
       // 是否允许匿名投票 0:允许 1：不允许
        String anonymousFlag = request.getParameter("anonymousFlag");
        InquirySurveytype type = inquiryManager.getSurveyTypeById(surveytypeId);
        Integer authType = Integer.parseInt(request.getParameter("authType"));
        if (type != null) {
        	type.setAnonymousFlag(Integer.parseInt(anonymousFlag));
            if (authType == InquiryConstants.AUTHTYPE_ALL) {
                inquiryManager.saveInquiryAuthorities(surveytypeId, "");
                type.setAuthType(authType);
                inquiryManager.updateInquiryType(type);
            } else {
                User user = AppContext.getCurrentUser();
                String userName = user.getName();

                // 获得上次的授权人的记录
                List<InquiryAuthority> authorityList = inquiryManager.authorityList(surveytypeId);
                List<Long> exitIds = new ArrayList<Long>();
                if (authorityList != null && authorityList.size() > 0) {
                    for (InquiryAuthority authority : authorityList) {
                        exitIds.add(authority.getAuthId());
                    }
                }
                // 获取授权用户
                String authscope = request.getParameter("authListIds");
                inquiryManager.saveInquiryAuthorities(surveytypeId, authscope);
                type.setAuthType(authType);
                inquiryManager.updateInquiryType(type);
                // 发布授权消息
                List<Long> inquiryIDs = new ArrayList<Long>();
                if ("".equals(authscope) || authscope == null) {
                    return super.refreshWindow("parent");
                }
                Set<V3xOrgMember> membersSet = orgManager.getMembersByTypeAndIds(authscope);
                for (V3xOrgMember entity : membersSet) {
                    if (AppContext.getCurrentUser().getId() != entity.getId().longValue()) {
                        inquiryIDs.add(entity.getId());
                    }
                }
                // 当没有授权人的时候直接返回不做操作
                if (inquiryIDs.size() < 1) {
                    return super.refreshWindow("parent");
                }
                // 比较是否存在授权过的人员
                if (authorityList != null && authorityList.size() > 0) {
                    List<Long> sendDS = new ArrayList<Long>();
                    for (Long long1 : inquiryIDs) {
                        if (!exitIds.contains(long1)) {
                            sendDS.add(long1);
                        }
                    }
                    if (sendDS.size() > 0) {
                        userMessageManager.sendSystemMessage(MessageContent.get("inq.authorization", type.getTypeName(), userName), ApplicationCategoryEnum.inquiry, user.getId(),
                                MessageReceiver.getReceivers(type.getId(), sendDS, "", String.valueOf(type.getId())), type.getId());
                    }
                } else {
                    userMessageManager.sendSystemMessage(MessageContent.get("inq.authorization", type.getTypeName(), userName), ApplicationCategoryEnum.inquiry, user.getId(),
                            MessageReceiver.getReceivers(type.getId(), inquiryIDs, "", String.valueOf(type.getId())), type.getId());
                }
                //对整个操作记录应用日志
                this.appLogManager.insertLog(user, AppLogAction.Inquiry_PostAuth_Update, userName, type.getTypeName());
            }
        }

        
        
        return super.refreshWindow("parent");
    }

    /**
     * 根据可操作列表展示
     * 
     * */
    public ModelAndView listType(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	ModelAndView mav = new ModelAndView("inquiry/inquiryMove");
        String typeId = request.getParameter("typeId");
        String ids = request.getParameter("ids");
        // 类型列表
        List<SurveyTypeCompose> typeComposeList = null;
        if(Strings.isNotBlank(typeId)){
            Long _typeId = NumberUtils.toLong(typeId);
            InquirySurveytype inquirySurveytype = inquiryManager.getSurveyTypeById(_typeId);
            if(inquirySurveytype!=null){
                int spaceType = inquirySurveytype.getSpaceType();
                Long spaceId = inquirySurveytype.getAccountId();
                if (spaceType == SpaceType.public_custom.ordinal()) {// 自定义单位版块
                    typeComposeList = inquiryManager.getUserIndexInquiryList(spaceId, spaceType, false);
                }else if(spaceType == SpaceType.public_custom_group.ordinal()){// 自定义集团版块
                    typeComposeList = inquiryManager.getUserIndexInquiryList(spaceId, spaceType, false);
                }else if(spaceType == SpaceType.custom.ordinal()){// 自定义团队版块
                }else if(spaceType == SpaceType.group.ordinal()){
                    typeComposeList = inquiryManager.getUserIndexInquiryList(true, false);
                }else if(spaceType == SpaceType.corporation.ordinal()){
                    typeComposeList = inquiryManager.getUserIndexInquiryList(false, false);
                }
            }
        }
        List<InquirySurveytype> typeList = new ArrayList<InquirySurveytype>();
        if(typeComposeList!=null){
        	typeComposeList = inquiryManager.complateMangerRelation(typeComposeList);
        }
        for(SurveyTypeCompose typeCompose:typeComposeList){
        	String id = typeCompose.getInquirySurveytype().getId().toString();
        	if((!typeId.equals(id))&&typeCompose.isHasManageAuth()){
        		typeList.add(typeCompose.getInquirySurveytype());
        	}
        }
        //启用排序 2013-08-09
        Comparator<InquirySurveytype> comp = new InquirySurveytype();
        Collections.sort(typeList, comp);
        mav.addObject("typeList", typeList);
        mav.addObject("ids", ids);
        
        return mav;
    }
    /**
     * 我发起的 页面
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView inquiryIStart(HttpServletRequest request, HttpServletResponse response) throws Exception{
        int spaceType = NumberUtils.toInt(request.getParameter("spaceType"));
        Long spaceId = NumberUtils.toLong(request.getParameter("spaceId"));
    	ModelAndView mav = new ModelAndView("inquiry/inquiryIStart");
    	mav.addObject("type","1");
    	mav = inquiryMavData(mav,"iStart",null,spaceType, spaceId);
    	return mav;
    }
    /**
     * 审核调查 页面
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView inquiryIAuth(HttpServletRequest request, HttpServletResponse response) throws Exception{
        int spaceType = NumberUtils.toInt(request.getParameter("spaceType"));
        Long spaceId = NumberUtils.toLong(request.getParameter("spaceId"));
    	ModelAndView mav = new ModelAndView("inquiry/inquiryIStart");
    	mav.addObject("type","2");
    	mav = inquiryMavData(mav,"iAuth",null, spaceType, spaceId);
    	return mav;
    }
    /**
     * 页面共享要传递的数据,包含：
     * 1.我能查看的所有调查的数量，其中我能填写的调查数量-所有版块
     * 2.我参与的，我发起的，审核调查的数量
     * 3.当前用户是否有任意版块的发起权限
     * 4.版块列表-index boardIndex
     * @param mav
     * @param pageType 页面类型—— index调查首页 boardIndex版块首页 iStart我发起的 iAuth我调查的
     * @return
     * @throws Exception 
     */
    private ModelAndView inquiryMavData(ModelAndView mav,String pageType,Long boardId, int spaceType, Long spaceId) throws Exception{
    	List<SurveyTypeCompose> inquiryTypeList = new ArrayList<SurveyTypeCompose>(); // 调查类型列表
        List<SurveyTypeCompose> groupInquiryTypeList = null; // 调查类型列表
        List<SurveyTypeCompose> accountInquiryTypeList = null; // 调查类型列表
        List<SurveyTypeCompose> customInquiryTypeList = null; // 自定义团队 调查类型列表
        User user = AppContext.getCurrentUser();
        //数据区
        List<Integer> numList = new ArrayList<Integer>();
        boolean hasIssue = false;
        boolean hasAuthIssue = false;
        if (spaceType == SpaceType.public_custom.ordinal()) {// 自定义单位版块
            accountInquiryTypeList = inquiryManager.getUserIndexInquiryList(spaceId, spaceType, false);
        }else if(spaceType == SpaceType.public_custom_group.ordinal()){// 自定义集团版块
            groupInquiryTypeList = inquiryManager.getUserIndexInquiryList(spaceId, spaceType, false);
        }else if(spaceType == SpaceType.custom.ordinal()){// 自定义团队版块直接进入版块首页
            customInquiryTypeList = inquiryManager.getUserIndexInquiryList(spaceId, spaceType, false);
        }else {
            if ((Boolean) (SysFlag.sys_isGroupVer.getFlag())) {
                groupInquiryTypeList = inquiryManager.getUserIndexInquiryList(true, false);
            }
            accountInquiryTypeList = inquiryManager.getUserIndexInquiryList(false, false);
        }
        if(groupInquiryTypeList!=null){
        	groupInquiryTypeList = inquiryManager.complateMangerRelation(groupInquiryTypeList);
        	inquiryTypeList.addAll(groupInquiryTypeList);
        }
        if(accountInquiryTypeList!=null){
        	accountInquiryTypeList = inquiryManager.complateMangerRelation(accountInquiryTypeList);
        	inquiryTypeList.addAll(accountInquiryTypeList);
        }
        if(customInquiryTypeList!=null){
            customInquiryTypeList = inquiryManager.complateMangerRelation(customInquiryTypeList);
            inquiryTypeList.addAll(customInquiryTypeList);
        }
        List<Long> typeIdsList = new ArrayList<Long>();
        for (SurveyTypeCompose type : inquiryTypeList) {
        	typeIdsList.add(type.getInquirySurveytype().getId());
        }
        //抽取调查
        numList = inquiryManager.getIndexMyNum(null,typeIdsList);
        //去掉已经离职的管理员
        for (SurveyTypeCompose bt : inquiryTypeList) {
            if(bt.isHasPublicAuth()){
            	hasIssue = true;
            }
            if(bt.getChecker()!=null&&user.getId().equals(bt.getChecker().getId())){
            	hasAuthIssue = true;
            }
        }

        
        //1.我能查看的所有调查的数量，其中我能填写的调查数量-所有版块
        //mav.addObject("basicSize", totalSize);
        //mav.addObject("allSize", totalSize);
        //mav.addObject("canEditSize", canEditSize);
        //2.我参与的，我发起的，审核调查的数量
        mav.addObject("iJoined", numList.get(0));
        mav.addObject("iPublished", numList.get(1));
        mav.addObject("iAuth", numList.get(2));
        //3.当前用户是否有任意版块的发起权限
        mav.addObject("hasIssue", hasIssue);
        mav.addObject("hasAuthIssue", hasAuthIssue);
        //4.版块列表-index boardIndex
        if("index".equals(pageType)||"boardIndex".equals(pageType)){
        	mav.addObject("typeList", inquiryTypeList);
            mav.addObject("groupInquiryTypeList", groupInquiryTypeList);
            mav.addObject("accountInquiryTypeList", accountInquiryTypeList);
            mav.addObject("customInquiryTypeList", customInquiryTypeList);
            mav.addObject("groupInquiryTypeListPageNum", groupInquiryTypeList==null?0:Math.ceil(groupInquiryTypeList.size()/(double)5));
            mav.addObject("accountInquiryTypeListPageNum", accountInquiryTypeList==null?0:Math.ceil(accountInquiryTypeList.size()/(double)5));
            mav.addObject("customInquiryTypeListPageNum", customInquiryTypeList==null?0:Math.ceil(customInquiryTypeList.size()/(double)5));
            if("index".equals(pageType)&&(spaceType==2||spaceType==3)){//依次选 单位、集团
                mav.addObject("listSelect", spaceType);
            }
        }
    	return mav;
    }
    /**
     * 新建调查
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView inquiryCreate(HttpServletRequest request, HttpServletResponse response) throws Exception{
    	ModelAndView mav = new ModelAndView("inquiry/inquiryCreate");
    	String inquiryId = request.getParameter("inquiryId");
    	String typeId = request.getParameter("typeId");
        String isEdit = request.getParameter("isEdit");
    	int spaceType = NumberUtils.toInt(request.getParameter("spaceType"));
        Long spaceId = NumberUtils.toLong(request.getParameter("spaceId"));
        mav.addObject("spaceType",spaceType);
        mav.addObject("spaceId",spaceId);
    	InquirySurveybasic inquiry = new InquirySurveybasic();

    	if(inquiryId!=null){
    		inquiry.setId(Long.parseLong(inquiryId));
            SurveyBasicCompose temp = inquiryManager.getInquiryBasic(inquiryId);
            if(temp!=null){
                inquiry.setCensor(temp.getInquirySurveybasic().getCensor());
            }
            if(!("true".equals(isEdit)&&inquiryManager.getInquiryBasic(inquiryId).getInquirySurveybasic().getCreaterId().equals(AppContext.currentUserId()))){
                return null;
            }
            if("true".equals(isEdit)){
                //对调查进行加锁
                String action = InquiryLockAction.InQUIRY_LOCK_EDITING;
                InquiryLock inqlock = inquiryManager.lock(Long.parseLong(inquiryId), action);
                if (inqlock != null) {
                    V3xOrgMember orm = orgManager.getMemberById(inqlock.getUserid());
                    String lockmessage = inqlock.getAction();
                    response.setContentType("text/html;charset=UTF-8");
                    PrintWriter out = response.getWriter();
                    out.println("<script type='text/javascript'>");
                    out.println("alert('"+ ResourceUtil.getString(lockmessage,orm.getName()) + "');window.close();");
                    out.println("</script>");
                    out.flush();
                    return null;
                }
            }
    	}else{
    		inquiry.setIdIfNew();
    	}
    	if(typeId!=null){
    		InquirySurveytype inquiryType = inquiryManager.getSurveyTypeById(Long.parseLong(typeId));
    		mav.addObject("inquiryTypeId",typeId);
    		mav.addObject("inquiryTypeOf",inquiryType.getSpaceType());
    	}    	
    	//版块信息
    	List<SurveyTypeCompose> groupInquiryTypeList = null; // 调查类型列表
        List<SurveyTypeCompose> accountInquiryTypeList = null; // 调查类型列表
        List<SurveyTypeCompose> customInquiryTypeList = null; // 调查类型列表
        if (spaceType == SpaceType.public_custom.ordinal()) {// 自定义单位版块
            accountInquiryTypeList = inquiryManager.getUserIndexInquiryList(spaceId, spaceType, false);
            
            StringBuilder publisthScopeSpace = new StringBuilder();
            List<Object[]> issueAreas = portalApi.getSecuityOfSpace(spaceId);
            for(Object[] arr : issueAreas) {
                publisthScopeSpace.append(StringUtils.join(arr, "|") + ",");
            }
            mav.addObject("entity",publisthScopeSpace.substring(0, publisthScopeSpace.length() - 1));
            mav.addObject("DEPARTMENTissueArea", publisthScopeSpace.substring(0, publisthScopeSpace.length() - 1));
        }else if(spaceType == SpaceType.public_custom_group.ordinal()){// 自定义集团版块
            groupInquiryTypeList = inquiryManager.getUserIndexInquiryList(spaceId, spaceType, false);
            
            StringBuilder publisthScopeSpace = new StringBuilder();
            List<Object[]> issueAreas = portalApi.getSecuityOfSpace(spaceId);
            for(Object[] arr : issueAreas) {
                publisthScopeSpace.append(StringUtils.join(arr, "|") + ",");
            }
            mav.addObject("entity",publisthScopeSpace.substring(0, publisthScopeSpace.length() - 1));
            mav.addObject("DEPARTMENTissueArea", publisthScopeSpace.substring(0, publisthScopeSpace.length() - 1));
        }else if(spaceType == SpaceType.custom.ordinal()){// 自定义团队版块直接进入版块首页
            customInquiryTypeList = inquiryManager.getUserIndexInquiryList(spaceId, spaceType, false);
            
            StringBuilder publisthScopeSpace = new StringBuilder();
            List<Object[]> issueAreas = portalApi.getSecuityOfSpace(spaceId);
            for(Object[] arr : issueAreas) {
                publisthScopeSpace.append(StringUtils.join(arr, "|") + ",");
            }
            if(publisthScopeSpace.length() > 0){
                publisthScopeSpace.substring(0, publisthScopeSpace.length() - 1);
            }
            List<Object[]> entityObj= portalApi.getSecuityOfSpace(Long.valueOf(spaceId));
            String entity="";
            for (Object[] obj : entityObj) {
                entity+=obj[0]+"|"+obj[1]+",";
            }
            if(!"".equals(entity)){
                entity=entity.substring(0, entity.length()-1);
            }
            mav.addObject("entity",entity);
            mav.addObject("DEPARTMENTissueArea", publisthScopeSpace);
        }else {
            groupInquiryTypeList = inquiryManager.getUserIndexInquiryList(true, false);
            accountInquiryTypeList = inquiryManager.getUserIndexInquiryList(false, false);
        }
        List<SurveyTypeCompose> groupTypeList = new ArrayList<SurveyTypeCompose>(); // 调查类型列表
        List<SurveyTypeCompose> accountTypeList =new ArrayList<SurveyTypeCompose>(); // 调查类型列表
        List<SurveyTypeCompose> customTypeList =new ArrayList<SurveyTypeCompose>(); // 调查类型列表
        
        if(groupInquiryTypeList!=null){
        	groupInquiryTypeList = inquiryManager.complateMangerRelation(groupInquiryTypeList);
        	for (SurveyTypeCompose bt : groupInquiryTypeList) {
        	    if(bt.isHasPublicAuth()){
        	        groupTypeList.add(bt);
        	    }
        	}
        }
        if(accountInquiryTypeList!=null){
        	accountInquiryTypeList = inquiryManager.complateMangerRelation(accountInquiryTypeList);
        	for (SurveyTypeCompose bt : accountInquiryTypeList) {
        	    if(bt.isHasPublicAuth()){
        	        accountTypeList.add(bt);
        	    }
        	}
        }
        if(customInquiryTypeList!=null){
            customInquiryTypeList = inquiryManager.complateMangerRelation(customInquiryTypeList);
            for (SurveyTypeCompose bt : customInquiryTypeList) {
                if(bt.isHasPublicAuth()){
                    customTypeList.add(bt);
                }
            }
        }
        
        User member = AppContext.getCurrentUser();
        V3xOrgDepartment department=new V3xOrgDepartment();
        // 登录人员在兼职单位
        if (!member.getLoginAccount().equals(member.getAccountId())) {
            List<MemberPost> memberPostList = orgManager.getMemberConcurrentPostsByAccountId(member.getId(), member.getLoginAccount());
            // 取兼职单位所在的部门
            if (Strings.isNotEmpty(memberPostList)) {
                department = this.orgManager.getEntityById(V3xOrgDepartment.class, memberPostList.get(0).getDepId());
            }
        } else {
            // 如果没有兼职信息，设置发布部门为当前用户登录单位所在的部门
            long departmentid = member.getDepartmentId(); // 当前用户的部门ID
            department = this.orgManager.getEntityById(V3xOrgDepartment.class, departmentid); // 获取发布部门
        }    	
        //定位初始在第一步
    	mav.addObject("step","1");
    	mav.addObject("isEdit",request.getParameter("isEdit"));
    	mav.addObject("beginDeptId",department.getId());
    	mav.addObject("beginDeptName",department.getName());
    	mav.addObject("inquiryId",inquiry.getId().toString());
    	mav.addObject("inquiryState",inquiry.getCensor());
    	mav.addObject("groupInquiryTypeList",groupTypeList);
    	mav.addObject("accountInquiryTypeList",accountTypeList);
    	mav.addObject("customInquiryTypeList",customTypeList);
    	return mav;
    }
    /**
     * 调查详情
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView inquiryView(HttpServletRequest request, HttpServletResponse response) throws Exception{
    	ModelAndView mav = new ModelAndView("inquiry/inquiryInfo");
    	Long userId = AppContext.currentUserId();
    	String inquiryId = request.getParameter("inquiryId");
    	String isAuth = request.getParameter("isAuth");
    	String memberId = request.getParameter("memberId");
    	String indexFlag = request.getParameter("indexFlag");
        String theAffairId = request.getParameter("affairId");
        String infoMode = "info";
    	String authMind = "";
        String reload = "if(window.opener){" +
                            "if(window.opener.getCtpTop().isCtpTop){" +
                                "window.opener.getCtpTop().reFlesh();" +
                            "}else{" +
                                "window.opener.location.reload();" +
                            "}" +
                        "}" +
                        "window.close();";
        boolean hasAtts = false;
        boolean hasDoc = false;
//    	InquiryBasicVo inquiry = inquiryManager.getInquiryBasicVoById(Long.parseLong(inquiryId),"info");
    	if(inquiryId==null){//预览
    		String packageStr = request.getParameter("packageStr");
        	String questionStr = request.getParameter("questionStr");
        	String metaData = request.getParameter("metaData");
            String attsData = request.getParameter("preAttachment");

            String sendDate = Datetimes.formatDatetimeWithoutSecond(new Date());
	    	SurveyTypeCompose inquiryBoard = inquiryManager.getSurveyTypeComposeBYID(Long.parseLong(request.getParameter("boardList")));

            InquirySurveybasic inquirySurveybasic = new InquirySurveybasic();
            inquirySurveybasic.setIdIfNew();
            if (Strings.isNotEmpty(attsData)) {
                Object obj = JSONUtil.parseJSONString(attsData);
                List<Map> atts = new LinkedList();
                if (obj instanceof Map) {
                    atts.add((Map) obj);
                } else {
                    atts = (List<Map>) obj;
                }

                attachmentManager.deleteByReference(inquirySurveybasic.getId(), inquirySurveybasic.getId());
                attachmentManager.create(ApplicationCategoryEnum.inquiry, inquirySurveybasic.getId(), inquirySurveybasic.getId(), atts);
                List<Attachment> attachments = attachmentManager.getByReference(inquirySurveybasic.getId());
                if(attachments.size()>0){
                    for(Attachment attachment : attachments){
                        if(attachment.getType() == 0&&!hasAtts){
                            hasAtts = true;
                        }
                        if(attachment.getType() == 2&&!hasDoc){
                            hasDoc = true;
                        }
                    }
                    mav.addObject("hasAtts",false);
                    mav.addObject("hasDoc",false);
                    mav.addObject("inquiryAttListJSON", "");
                }
            }

        	Map<String,String> senderInfo = new HashMap<String,String>();
        	infoMode = "preview";
        	String senderId = userId.toString();
        	String senderName = AppContext.currentUserName();
        	String senderImgUrl = Functions.getAvatarImageUrl(userId);
        	senderInfo.put("senderId", senderId);
        	senderInfo.put("senderName", senderName);
        	senderInfo.put("senderImgUrl", senderImgUrl);

        	Map<String,String> inquiryInfo = new HashMap<String,String>();
        	inquiryInfo.put("startDeptId", request.getParameter("packageStr"));
        	inquiryInfo.put("startDeptName", request.getParameter("beginDeptName"));
        	inquiryInfo.put("inquiryScope", "");
            String scopeName = request.getParameter("inquiryScope");
            inquiryInfo.put("inquiryScopeName", scopeName);
            String inquiryScopeNameLimit = Strings.getSafeLimitLengthString(scopeName,121,"...");
            inquiryInfo.put("inquiryScopeNameLimit", inquiryScopeNameLimit);
        	inquiryInfo.put("inquiryBoardId", request.getParameter("boardList"));
        	inquiryInfo.put("inquiryBoardName", inquiryBoard.getInquirySurveytype().getTypeName());
        	inquiryInfo.put("inquiryId", "");
        	inquiryInfo.put("inquiryAuthId", inquiryBoard.getChecker()!=null?inquiryBoard.getChecker().getId().toString():"");
        	inquiryInfo.put("inquiryName", "");
        	inquiryInfo.put("inquirySendDate", sendDate);
        	inquiryInfo.put("inquiryEndDate", request.getParameter("closeDate")==null?ResourceUtil.getString("inquiry.meta.timelimitnone"):request.getParameter("closeDate"));
        	mav.addObject("inquiryInfo",inquiryInfo);
	    	mav.addObject("senderInfo",senderInfo);
	    	mav.addObject("authMind",authMind);
	    	mav.addObject("infoMode",infoMode);
	    	mav.addObject("packageStr","{"+Functions.toHTMLescapeRN(packageStr)+"}");
	    	mav.addObject("questionStr","{"+Functions.toHTMLescapeRN(questionStr)+"}");
	    	mav.addObject("metaData","{"+Functions.toHTMLescapeRN(metaData)+"}");
    		return mav;
    	}else{
            //来源性质
            String from = request.getParameter("from");
            /*
                from=message，从消息打开
                from=pigeonhole，从归档打开
                from=colCube，从协同立方打开
             */

	    	InquiryBasicVo inquiry = inquiryManager.getInquiryBasicVoById(Long.parseLong(inquiryId),"info");
	    	if(inquiry == null){
                if("message".equals(from)){
                    super.rendJavaScript(response, "alert('"+ResourceUtil.getString("inquiry.view.nopermissions")+"');window.close()");

                }else if("1".equals(indexFlag)){
                    super.rendJavaScript(response, "alert('"+ResourceUtil.getString("inquiry.view.nopermissions")+"');try {parent.window.opener.location.reload();parent.window.close()} catch(e) {}");
                }else{
                    super.rendJavaScript(response, "alert('"+ResourceUtil.getString("inquiry.view.nopermissions")+"');"+reload);
                }
                return null;
            }
	    	InquirySurveytype type = inquiryManager.getSurveyTypeById(inquiry.getSurveyTypeId());
            if(type == null || type.getFlag() != InquiryConstants.FLAG_NORMAL.intValue()){
                if("message".equals(from)){
                    super.rendJavaScript(response, "alert('"+ResourceUtil.getString("inquiry.view.nopermissions")+"');window.close()");

                }else if("1".equals(indexFlag)){
                    super.rendJavaScript(response, "alert('"+ResourceUtil.getString("inquiry.view.nopermissions")+"');try {parent.window.opener.location.reload();parent.window.close()} catch(e) {}");
                }else{
                    super.rendJavaScript(response, "alert('"+ResourceUtil.getString("inquiry.view.nopermissions")+"');"+reload);
                }
                return null;
            }
            
            //更新消息状态
            userMessageManager.updateSystemMessageStateByUserAndReference(userId, inquiry.getId());
            
            Map<String,String> inquiryInfo = new HashMap<String,String>();
            List<InquiryScope> scopeList = inquiryManager.getAllScopeForInquiry(inquiry.getId());

            String scope_range = "";
            String scope_name = "";
            for (InquiryScope scope : scopeList) {// 发布对象
                long egid = scope.getScopeId();
                String desc = scope.getScopeDesc();
                V3xOrgEntity org = this.orgManager.getEntity(desc, egid);
                scope_range += org.getEntityType() + "|" + egid + ",";
            }
            scope_name = Functions.showOrgEntities(scope_range,"、");
            V3xOrgMember member = orgManager.getMemberById(userId);
            Set<V3xOrgMember> memberSet = orgManager.getMembersByTypeAndIds(scope_range);

            //用户性质
            boolean isInScope = memberSet.contains(member);
            if(Strings.isNotBlank(theAffairId)){
                CtpAffair ctpAffair = affairManager.get(Long.valueOf(theAffairId));
                if(ctpAffair!=null){
                    isInScope = true;
                }
            }
            boolean isAuthManager = inquiry.getInquirySurveybasic().getCensorId().equals(userId);
            boolean isTypeManager = inquiryManager.isInquiryManager(inquiry.getSurveyTypeId(), userId);
            boolean isCreate = inquiry.getInquirySurveybasic().getCreaterId().equals(userId);

            boolean isAgent = false;
            Long auditId = inquiry.getInquirySurveybasic().getCensorId();
            if (auditId != null) {
                Long agentId = AgentUtil.getAgentByApp(auditId, ApplicationCategoryEnum.inquiry.getKey());
                if (agentId != null && agentId.equals(userId)) {
                    isAgent = true;
                }
            }

            if (!(isInScope || isTypeManager || isAuthManager || isAgent || isCreate)) {
                if (!"pigeonhole".equals(from)) {
                    super.rendJavaScript(response, "alert('"+ResourceUtil.getString("inquiry.user.notAuthority")+"');window.close()");
                    return null;
                }
            }

	    	if(inquiry.getInquirySurveybasic().getCensor()==InquirySurveybasic.CENSOR_NO_PASS.intValue()){
                    infoMode = "noPass";
                    authMind = inquiry.getInquirySurveybasic().getCheckMind();
	    	}else if(inquiry.getInquirySurveybasic().getCensor()==InquirySurveybasic.CENSOR_PASS_NO_SEND.intValue()){
	    		infoMode = "pass";
	    		authMind = inquiry.getInquirySurveybasic().getCheckMind();
	    	}else if(inquiry.getInquirySurveybasic().getCensor()==InquirySurveybasic.CENSOR_DRAUGHT.intValue()){
                if(isCreate&&"false".equals(isAuth)){
                    infoMode = "info";
                }else{
                    if("message".equals(from)){
                        super.rendJavaScript(response, "alert('"+ResourceUtil.getString("inquiry.view.nopermissions")+"');window.close()");
                    }else if("1".equals(indexFlag)){
                        super.rendJavaScript(response, "alert('"+ResourceUtil.getString("inquiry.view.nopermissions")+"');try {parent.window.opener.location.reload();parent.window.close()} catch(e) {}");
                    }else{
                        super.rendJavaScript(response, "alert('"+ResourceUtil.getString("inquiry.view.nopermissions")+"');"+reload);
                    }
                    return null;
                }
            }else if(inquiry.getInquirySurveybasic().getCensor()==InquirySurveybasic.CENSOR_NO.intValue()){
                if("true".equals(isAuth)){
                    //对调查进行加锁
                    String action = InquiryLockAction.InQUIRY_LOCK_AUDITING;
                    InquiryLock inqlock = inquiryManager.lock(inquiry.getId(), action);
                    if (inqlock != null) {
                        V3xOrgMember orm = orgManager.getMemberById(inqlock.getUserid());
                        String lockmessage = inqlock.getAction();
                        String alertMsg = ResourceUtil.getString(lockmessage,
                                orm.getName());
                        super.rendJavaScript(response, "alert('"+alertMsg+"');window.close()");
                        return null;
                    }
                }
                infoMode = "ready";
            }else if(inquiry.getInquirySurveybasic().getCensor()==InquirySurveybasic.CENSOR_CLOSE.intValue()){
	    		infoMode = "finish";
	    	}else if(inquiry.getInquirySurveybasic().getCensor()==InquirySurveybasic.CENSOR_PASS.intValue()){
	    		infoMode = "write";
                if(!isInScope){
                    infoMode = "info";
                    mav.addObject("isInScope",isInScope);
                }
	    	}else if(inquiry.getInquirySurveybasic().getCensor()==InquirySurveybasic.CENSOR_FILING_YES.intValue()){
                if("pigeonhole".equals(from)){
                    infoMode = "pigeonhole";
                }else{
                    if("message".equals(from)){
                        super.rendJavaScript(response, "alert('"+ResourceUtil.getString("inquiry.view.nopermissions")+"');window.close()");
                    }else if("1".equals(indexFlag)){
                        super.rendJavaScript(response, "alert('"+ResourceUtil.getString("inquiry.view.nopermissions")+"');try {parent.window.opener.location.reload();parent.window.close()} catch(e) {}");
                    }else{
                        super.rendJavaScript(response, "alert('"+ResourceUtil.getString("inquiry.view.nopermissions")+"');"+reload);
                    }
                    return null;
                }
            }

            changeAffState(theAffairId);
            inquiryAffairManager.updateAffairRead(inquiry.getId(), userId);
            //需要更新栏目中的点击数，所以不能按人
            eTagCacheManager.updateETagDate(InquiryConstants.ETAG_INQUIRY_USER, String.valueOf(userId));

            //调查附件
            List<Attachment> inquiryAttachments = new ArrayList<Attachment>();
            Map<Long, List<Attachment>> replyAttachementMap = new HashMap<Long, List<Attachment>>();
            List<Attachment> articleAtts = attachmentManager.getByReference(inquiry.getId());
            for (Attachment atta : articleAtts) {
                if (atta.getSubReference().equals(inquiry.getId())) {
                    inquiryAttachments.add(atta);
                    if(atta.getType() == 0&&!hasAtts){
                        hasAtts = true;
                    }
                    if(atta.getType() == 2&&!hasDoc){
                        hasDoc = true;
                    }
                } else {
                    Strings.addToMap(replyAttachementMap, atta.getSubReference(), atta);
                }
            }

	    	Map<String,String> senderInfo = new HashMap<String,String>();
	    	String senderId = inquiry.getIssueUserId().toString();
	    	String senderName = inquiry.getIssueUserName();
	    	String senderImgUrl = inquiry.getIssueUserImgUrl();
	    	senderInfo.put("senderId", senderId);
	    	senderInfo.put("senderName", senderName);
	    	senderInfo.put("senderImgUrl", senderImgUrl);
	    	
	    	inquiryInfo.put("startDeptId", inquiry.getDepartmentId().toString());
	    	inquiryInfo.put("startDeptName", inquiry.getDepartmentName());
	    	inquiryInfo.put("inquiryScope", scope_range.substring(0, scope_range.length() - 1));
	    	inquiryInfo.put("inquiryScopeName", scope_name);
	    	String inquiryScopeNameLimit = Strings.getSafeLimitLengthString(scope_name,121,"...");
	    	logger.info("*Scope of investigation intercepted:"+ inquiryScopeNameLimit);
	    	inquiryInfo.put("inquiryScopeNameLimit", inquiryScopeNameLimit);
	    	inquiryInfo.put("inquiryBoardId", inquiry.getSurveyTypeId().toString());
	    	inquiryInfo.put("inquiryBoardName", inquiry.getSurveyTypeName());
	    	inquiryInfo.put("inquiryId", inquiry.getInquirySurveybasic().getId().toString());
	    	inquiryInfo.put("inquiryAuthId", inquiry.getInquirySurveybasic().getCensorId().toString());
	    	inquiryInfo.put("inquiryName", inquiry.getInquirySurveybasic().getSurveyName());
			inquiryInfo.put("inquirySendDate", Datetimes.formatDate(inquiry.getInquirySurveybasic().getIssueDate()));
			inquiryInfo.put("inquiryEndDate", inquiry.getInquirySurveybasic().getCloseDate() == null ? "" : Datetimes.formatDate(inquiry.getInquirySurveybasic().getCloseDate()));
			inquiryInfo.put("inquirySpaceType", String.valueOf(inquiry.getInquirySurveybasic().getSpaceType()));
			inquiryInfo.put("surveyState", inquiry.getSurveyState());
			mav.addObject("inquiryInfo",inquiryInfo);
	    	mav.addObject("senderInfo",senderInfo);
	    	mav.addObject("authMind",authMind);
	    	mav.addObject("infoMode",infoMode);
	    	mav.addObject("memberId",memberId);
	    	mav.addObject("isAuth",isAuth);
	    	mav.addObject("isManager",isTypeManager);
	    	mav.addObject("isAuthManager",isAuthManager);
	    	mav.addObject("isSender",isCreate);
            mav.addObject("hasAtts",hasAtts);
            mav.addObject("hasDoc",hasDoc);
            mav.addObject("inquiryAttListJSON", JSONUtil.toJSONString(inquiryAttachments));

            AccessControlBean.getInstance().addAccessControl(ApplicationCategoryEnum.inquiry, String.valueOf(inquiry.getId()), AppContext.currentUserId());
        }
        return mav;
    }

    public ModelAndView inquiryResult(HttpServletRequest request, HttpServletResponse response) throws Exception{
        ModelAndView mav = new ModelAndView("inquiry/inquiryResult");
        Long userId = AppContext.currentUserId();
        String inquiryId = request.getParameter("inquiryId");

        SurveyBasicCompose inquiry = inquiryManager.getInquiryBasicByBasicID(Long.parseLong(inquiryId));
        Long typeId = inquiry.getInquirySurveybasic().getSurveyTypeId();
        boolean isVote = inquiryManager.getUserVoteBasic(userId, Long.parseLong(inquiryId));//是否投标票
        boolean isTypeManager = false;
        boolean isSender = inquiry.getInquirySurveybasic().getCreaterId().equals(userId);
        try {
            if (typeId != null && !"".equals(typeId.toString())) {
                if (inquiryManager.isInquiryManager(typeId, AppContext.currentUserId())) {
                    mav.addObject("manager", "manager");
                    isTypeManager = true;
                }
            }
        } catch (Exception e) {
            logger.error("Exception in judging whether or not it is the inquiry board administrator", e);
        }

        if(inquiry==null){//为空时
            return null;
        }else if (!(inquiry.getInquirySurveybasic().getCreaterId().equals(userId) || isTypeManager ||
                (inquiry.getInquirySurveybasic().isAllowViewResult() && !isVote/*条件含义：允许投票后查看，并且投过票*/) ||
                (inquiry.getInquirySurveybasic().isAllowViewResultAhead()))) {
            return null;
        }else{
            SurveyTypeCompose inquiryBoard = inquiryManager.getSurveyTypeComposeBYID(inquiry.getInquirySurveybasic().getSurveyTypeId());
            try {
                inquiry = inquiryManager.findVoteResultByfilter(inquiry,"all",null,null);
            } catch (Exception e) {
                logger.error("Get survey exceptions when viewing survey results", e);
                PrintWriter out = null;

                try {
                    out = response.getWriter();
                    out.println("<script type='text/javascript'>");
                    out.println("alert('" + ResourceUtil.getString("inquiry.type.notvalid") + "');");
                    out.println("window.close();");
                    out.println("</script>");
                    out.close();
                } catch (IOException e1) {
                    logger.error("", e1);
                }
            }

            String infoMode = "info";
            if(inquiry.getInquirySurveybasic().getCensor()==InquirySurveybasic.CENSOR_NO.intValue()){
                infoMode = "ready";
            }else if(inquiry.getInquirySurveybasic().getCensor()==InquirySurveybasic.CENSOR_NO_PASS.intValue()){
                infoMode = "noPass";
            }else if(inquiry.getInquirySurveybasic().getCensor()==InquirySurveybasic.CENSOR_PASS_NO_SEND.intValue()){
                infoMode = "pass";
            }else if(inquiry.getInquirySurveybasic().getCensor()==InquirySurveybasic.CENSOR_CLOSE.intValue()){
                infoMode = "finish";
            }else if(inquiry.getInquirySurveybasic().getCensor()==InquirySurveybasic.CENSOR_PASS.intValue()){
                infoMode = "write";
            }
            Map<String,String> senderInfo = new HashMap<String,String>();
            String senderId = inquiry.getSender().getId().toString();
            String senderName = Functions.showMemberName(inquiry.getSender().getId());
            String senderImgUrl = Functions.getAvatarImageUrl(inquiry.getSender().getId());
            senderInfo.put("senderId", senderId);
            senderInfo.put("senderName", senderName);
            senderInfo.put("senderImgUrl", senderImgUrl);

            Map<String,String> inquiryInfo = new HashMap<String,String>();
            Set<InquiryScope> scopeSet = inquiry.getInquirySurveybasic().getInquiryScopes();
            String scope_range = "";
            String scope_name = "";
            for (InquiryScope scope : scopeSet) {// 发布对象
                long egid = scope.getScopeId();
                String desc = scope.getScopeDesc();
                V3xOrgEntity org = this.orgManager.getEntity(desc, egid);
                scope_range += org.getEntityType() + "|" + egid + ",";
            }
            scope_name = Functions.showOrgEntities(scope_range,"、");
            List<InquirySurveyVoterVO> voteMemberList = new ArrayList<InquirySurveyVoterVO>();
            List<InquiryAffair> inquiryAffairList = inquiryAffairManager.findAffairsByObjectId(inquiry.getInquirySurveybasic().getId());
            int votedNum = 0;
            if(inquiryAffairList!=null){
                for(InquiryAffair affair : inquiryAffairList){
                    InquirySurveyVoterVO tempVoterVo = new InquirySurveyVoterVO();
                    V3xOrgMember tempMember = orgManager.getMemberById(affair.getMemberId());
                    tempVoterVo.setV3xOrgMember(tempMember);
                    tempVoterVo.setVoteDate(affair.getVoteDate());
                    tempVoterVo.setHadVoted(affair.getState().equals(InquiryConstants.AFFAIR_STATE_DONE));
                    if(affair.getState().equals(InquiryConstants.AFFAIR_STATE_DONE)){
                        votedNum++;
                    }
                    V3xOrgDepartment dept = orgManager.getDepartmentById(affair.getDepartmentId());
                    tempVoterVo.setDeptStr(dept != null ? dept.getName() : "");
                    V3xOrgPost post = orgManager.getPostById(affair.getPostId());
                    tempVoterVo.setPostStr(post != null ? post.getName() : "");
                    tempVoterVo.setDeptId(affair.getDepartmentId());
                    tempVoterVo.setPostId(affair.getPostId());
                    voteMemberList.add(tempVoterVo);
                }
                Collections.sort(voteMemberList);
                Collections.reverse(voteMemberList);
            }
            inquiry.getInquirySurveybasic().setVoteCount(votedNum);
            //----取四个列表----//
            //概况折线图数据
//            LinkedHashMap<String,Integer> overviewChartData = InquiryUtil.getResultOverviewChartData(voteMemberList,inquiry);
//            LinkedHashMap<String,List<String>> deptVoteData = InquiryUtil.getResultDeptVoteData(voteMemberList,inquiry);
//            LinkedHashMap<String,List<String>> postVoteData = InquiryUtil.getResultPostVoteData(voteMemberList,inquiry);

            //----取四个列表----//

            inquiryInfo.put("startDeptId", inquiry.getDeparmentName().getId().toString());
            inquiryInfo.put("startDeptName", inquiry.getDeparmentName().getName());
            inquiryInfo.put("inquiryScope", scope_range.substring(0, scope_range.length() - 1));
            inquiryInfo.put("inquiryScopeName", scope_name);
            inquiryInfo.put("inquiryBoardId", inquiryBoard.getInquirySurveytype().getId().toString());
            inquiryInfo.put("inquiryBoardName", inquiryBoard.getInquirySurveytype().getTypeName());
            inquiryInfo.put("inquiryId", inquiry.getInquirySurveybasic().getId().toString());
            inquiryInfo.put("inquiryAuthId", inquiry.getInquirySurveybasic().getCensorId().toString());
            inquiryInfo.put("inquiryName", inquiry.getInquirySurveybasic().getSurveyName());
			inquiryInfo.put("inquirySendDate", Datetimes.formatDate(inquiry.getInquirySurveybasic().getIssueDate()));
			inquiryInfo.put("inquiryEndDate", inquiry.getInquirySurveybasic().getCloseDate() == null ? "" : Datetimes.formatDate(inquiry.getInquirySurveybasic().getCloseDate()));
            mav.addObject("inquiryInfo",inquiryInfo);
            mav.addObject("senderInfo",senderInfo);
            mav.addObject("infoMode",infoMode);
            mav.addObject("inquiry",inquiry);
            mav.addObject("isSenderOrAdmin",isTypeManager || isSender);
            mav.addObject("voteMemberList",voteMemberList);
//            mav.addObject("overviewChartData", JSONUtil.toJSONString(overviewChartData));
//            mav.addObject("deptVoteData", deptVoteData);
//            mav.addObject("postVoteData", postVoteData);
        }

        return mav;
    }
    private boolean isUserAdmin(Long surveyTypeId, Long userId) throws Exception {
        boolean result = false;
        //拿到调查类型的ID,拿到调查类型的管理员的集合
        List<InquirySurveytypeextend> inqusurveList = inquiryManager.getSerById(surveyTypeId,
                InquiryConstants.INQUIRY_MANAGER_DESC_ADMIN);
        for (InquirySurveytypeextend surveytypeextend : inqusurveList) {
            if (surveytypeextend.getManagerId().longValue() == userId) {
                result = true;
                break;
            }
        }
        return result;
    }
    /**
     * 查看大图
     * @param request
     * @param response
     * @return inquiryViewImage
     * @throws Exception
     */
    public ModelAndView inquiryViewImage(HttpServletRequest request, HttpServletResponse response) throws Exception{
        ModelAndView mav = new ModelAndView("inquiry/inquiryViewImage");
        mav.addObject("imgId",request.getParameter("imgId"));
        return mav;
    }

    /**
     * 更新待办已读状态
     */
    private CtpAffair changeAffState(final String affairId) throws Exception {
        if (Strings.isNotBlank(affairId) && Strings.isDigits(affairId)) {
            CtpAffair updateaff = affairManager.get(Long.valueOf(affairId));
            if (null != updateaff && updateaff.getSubState() != SubStateEnum.col_pending_read.key()) {
                updateaff.setSubState(SubStateEnum.col_pending_read.key());
                affairManager.updateAffair(updateaff);
                return updateaff;
            }
        }
        return null;
    }

    /**
     * 导出所有人的答卷
     * @param request
     * @param response
     * @return inquiryViewImage
     * @throws Exception
     */
    public ModelAndView exportMemberResultExcel(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String bid = request.getParameter("bid");
        SurveyBasicCompose sbcompose = inquiryManager.getInquiryBasicByBasicID(Long.parseLong(bid));
        if (sbcompose == null) {
            super.rendJavaScript(response, "alert('" + ResourceUtil.getString("inquiry.not.found") + "');");
            return null;
        }
        boolean isTypeManager = inquiryManager.isInquiryManager(sbcompose.getInquirySurveybasic().getSurveyTypeId(), AppContext.currentUserId());
        boolean isCreate = sbcompose.getInquirySurveybasic().getCreaterId().equals(AppContext.currentUserId());
        boolean isSecret = sbcompose.getInquirySurveybasic().getCryptonym()==1;
        if(!(isTypeManager||isCreate)){
            return null;
        }
        sbcompose = inquiryManager.findVoteResultByfilter(sbcompose,"all",null,null);

        List<InquiryAffair> affairList = inquiryAffairManager.findAffairsByObjectId(Long.parseLong(bid));
        List<InquiryAffair> votedList = new ArrayList<InquiryAffair>();
        for(InquiryAffair affair : affairList){
            if(affair.getState()==4){
                votedList.add(affair);
            }
        }
        List<InquiryVotedefinite> votes = inquiryManager.findInquiryVoteListByInquiryId(Long.parseLong(bid),null);
        DataRecord inquiryResult = InquiryUtil.createMemberInquiryExcelTable(votedList,sbcompose,votes,orgManager,isSecret);

        fileToExcelManager.save(response,ResourceUtil.getString("inquiry.memberexcel"),inquiryResult);
        return null;
    }

}