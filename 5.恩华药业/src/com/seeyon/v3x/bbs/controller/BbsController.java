package com.seeyon.v3x.bbs.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.seeyon.v3x.bulletin.domain.EhSendRange;
import com.seeyon.v3x.bulletin.manager.EhSendRangeManager;
import com.seeyon.v3x.bulletin.manager.EhSendRangeManagerImpl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.bbs.constants.BbsConstants;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.bo.DocResourceBO;
import com.seeyon.apps.project.api.ProjectApi;
import com.seeyon.apps.project.bo.ProjectBO;
import com.seeyon.apps.project.bo.ProjectMemberInfoBO;
import com.seeyon.apps.show.api.ShowApi;
import com.seeyon.apps.show.bo.ShowbarInfoBO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.cache.etag.ETagCacheManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.dao.paginate.Pagination;
import com.seeyon.ctp.common.excel.DataRecord;
import com.seeyon.ctp.common.excel.FileToExcelManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.domain.ReplaceBase64Result;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.manager.RoleManager;
import com.seeyon.ctp.portal.api.PortalApi;
import com.seeyon.ctp.portal.po.PortalSpaceFix;
import com.seeyon.ctp.portal.util.Constants.SecurityType;
import com.seeyon.ctp.portal.util.Constants.SpaceType;
import com.seeyon.ctp.util.CommonTools;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.UniqueList;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.bbs.domain.V3xBbsArticle;
import com.seeyon.v3x.bbs.domain.V3xBbsArticleIssueArea;
import com.seeyon.v3x.bbs.domain.V3xBbsArticleReply;
import com.seeyon.v3x.bbs.domain.V3xBbsBoard;
import com.seeyon.v3x.bbs.manager.BbsArticleManager;
import com.seeyon.v3x.bbs.manager.BbsBoardManager;
import com.seeyon.v3x.bbs.manager.BbsReadManager;
import com.seeyon.v3x.bbs.vo.ArticleModel;
import com.seeyon.v3x.bbs.vo.ArticleReplyModel;
import com.seeyon.v3x.bbs.vo.BoardModel;
import com.seeyon.v3x.common.security.SecurityCheck;

public class BbsController extends BaseController {

    private static Log         log = LogFactory.getLog(BbsController.class);
    private BbsBoardManager    bbsBoardManager;
    private BbsArticleManager  bbsArticleManager;
    private OrgManager         orgManager;
    private AttachmentManager  attachmentManager;
    private UserMessageManager userMessageManager;
    private PortalApi          portalApi;
    private ProjectApi         projectApi;
    private AppLogManager      appLogManager;
    private RoleManager        roleManager;
    private DocApi             docApi;
    private FileToExcelManager fileToExcelManager;
    private ShowApi            showApi;
    private BbsReadManager     bbsReadManager;
    private ETagCacheManager   eTagCacheManager;
    private FileManager fileManager;

    public void seteTagCacheManager(ETagCacheManager eTagCacheManager) {
        this.eTagCacheManager = eTagCacheManager;
    }

    public void setBbsBoardManager(BbsBoardManager bbsBoardManager) {
        this.bbsBoardManager = bbsBoardManager;
    }

    public void setBbsArticleManager(BbsArticleManager bbsArticleManager) {
        this.bbsArticleManager = bbsArticleManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    public void setUserMessageManager(UserMessageManager userMessageManager) {
        this.userMessageManager = userMessageManager;
    }

    public void setProjectApi(ProjectApi projectApi) {
        this.projectApi = projectApi;
    }

    public void setAppLogManager(AppLogManager appLogManager) {
        this.appLogManager = appLogManager;
    }

    public void setRoleManager(RoleManager roleManager) {
        this.roleManager = roleManager;
    }

    public void setDocApi(DocApi docApi) {
        this.docApi = docApi;
    }

    public void setFileToExcelManager(FileToExcelManager fileToExcelManager) {
        this.fileToExcelManager = fileToExcelManager;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void setShowApi(ShowApi showApi) {
        this.showApi = showApi;
    }

    public void setJsAction(String jsAction) {
        this.jsAction = jsAction;
    }

    // 查询版块信息框架
    @CheckRoleAccess(roleTypes = { Role_NAME.GroupAdmin, Role_NAME.AccountAdministrator }, extendRoles = { "SpaceManager" })
    public ModelAndView listBoard(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("bbs/bbsmanager/bbsmanageframe");
    }

    // 查询版块信息
    @CheckRoleAccess(roleTypes = { Role_NAME.GroupAdmin, Role_NAME.AccountAdministrator }, extendRoles = { "SpaceManager" })
    public ModelAndView listBoardMain(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        ModelAndView mav = new ModelAndView("bbs/bbsmanager/bbsmanage");
        User user = AppContext.getCurrentUser();
        Long accountId = user.getLoginAccount();
        boolean isGroup = user.isGroupAdmin();
        String spaceId = httpServletRequest.getParameter("spaceId");
        String spaceType = httpServletRequest.getParameter("spaceType");
        if (StringUtils.isNotBlank(spaceId)) {
            PortalSpaceFix spaceFix = portalApi.getSpaceFix(Long.valueOf(spaceId));
            spaceType = String.valueOf(spaceFix.getType());
        }
        List<V3xBbsBoard> bbsBoardList = new ArrayList<V3xBbsBoard>();
        if (Strings.isNotBlank(spaceId)) {
            bbsBoardList = this.bbsBoardManager.getBbsBoards4Page(Long.parseLong(spaceId), Integer.parseInt(spaceType));
        } else {
            bbsBoardList = this.bbsBoardManager.getBbsBoards4Page(isGroup, accountId);
        }
        List<String> nameList = new ArrayList<String>();
        //判断此版块下是否有帖子，有的话做标识不允许删除
        List<Boolean> delBsList = new ArrayList<Boolean>();
        for (V3xBbsBoard board : bbsBoardList) {
            nameList.add(board.getName());
            boolean hasArticle = bbsBoardManager.hasArticleByBoardId(board.getId());
            delBsList.add(hasArticle);
        }
        return mav.addObject("list", bbsBoardList).addObject("delBsList", delBsList).addObject("nameList", nameList);
    }

    // 新建版块信息框架
    @CheckRoleAccess(roleTypes = { Role_NAME.GroupAdmin, Role_NAME.AccountAdministrator }, extendRoles = { "SpaceManager" })
    public ModelAndView listBoardAdd(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("bbs/bbsmanager/bbsmanagecreate");
    }

    // 新增版块初始化信息
    @CheckRoleAccess(roleTypes = { Role_NAME.GroupAdmin, Role_NAME.AccountAdministrator }, extendRoles = { "SpaceManager" })
    public ModelAndView newBoard(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("bbs/bbsmanager/createboard");
        User user = AppContext.getCurrentUser();
        Long accountId = user.getLoginAccount();
        boolean isGroup = user.isGroupAdmin();
        String spaceId = request.getParameter("spaceId");
        String spaceType = request.getParameter("spaceType");
        List<V3xBbsBoard> bbsBoardList = new ArrayList<V3xBbsBoard>();
        if (StringUtils.isNotBlank(spaceId)) {
            PortalSpaceFix spaceFix = portalApi.getSpaceFix(Long.valueOf(spaceId));
            spaceType = String.valueOf(spaceFix.getType());
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
            mav.addObject("spaceType", spaceType);
            mav.addObject("entity", entity);
            bbsBoardList = this.bbsBoardManager.getBbsBoards4Page(Long.parseLong(spaceId), Integer.parseInt(spaceType));
        } else {
            bbsBoardList = this.bbsBoardManager.getBbsBoards4Page(isGroup, accountId);
        }
        List<String> nameList = new ArrayList<String>();
        for (V3xBbsBoard board : bbsBoardList) {
            nameList.add(board.getName());
        }
        mav.addObject("nameList", nameList);
        mav.addObject("newId", UUIDLong.longUUID());
        return mav;
    }
    private EhSendRangeManager sendRangeManager=new EhSendRangeManagerImpl();

    public EhSendRangeManager getSendRangeManager() {
        return sendRangeManager;
    }

    // 新增版块信息
    @CheckRoleAccess(roleTypes = { Role_NAME.GroupAdmin, Role_NAME.AccountAdministrator }, extendRoles = { "SpaceManager" })
    public ModelAndView createBoard(HttpServletRequest request, HttpServletResponse response) throws Exception {
        V3xBbsBoard bbsBoard = setVO(request, response);
        String spaceId = request.getParameter("spaceId");
        String spaceType = request.getParameter("spaceType");
        String imageId = request.getParameter("imageId");
        if (StringUtils.isNotBlank(spaceId)) {
            PortalSpaceFix spaceFix = portalApi.getSpaceFix(Long.valueOf(spaceId));
            bbsBoard.setAffiliateroomFlag(spaceFix.getType());
            spaceType = String.valueOf(spaceFix.getType());
        }
        if (StringUtils.isNotBlank(imageId)) {
            bbsBoard.setImageId(Long.parseLong(imageId));
        }
        String newId = request.getParameter("newId");
        bbsBoard.setId(Long.parseLong(newId));
        bbsBoard.setAuthType(BbsConstants.BBS_BOARD_AUTHTYPE_ALL);
        // 快速需求(Fast Demand): 板块可以停用，这里不在初始化默认值
        //bbsBoard.setFlag(BbsConstants.FLAG_NORMAL);
        this.bbsBoardManager.createBbsBoard(bbsBoard, request.getParameter("bbsBoardAdmin"));

//       恩华药业 zhou start
        EhSendRange sendRange=new EhSendRange();
        sendRange.setId(System.currentTimeMillis());
        sendRange.setModuleId(bbsBoard.getId());
        String rangeId=request.getParameter("sendArrangeId");
        String rangeName=request.getParameter("sendArrangeName");
        sendRange.setRangeId(rangeId);
        sendRange.setRangeName(rangeName);
        sendRangeManager.saveEhSendRange(sendRange);
//       恩华药业 zhou end

        this.saveRoleInfo(request.getParameter("bbsBoardAdmin"));

        //对管理员、审核员设定记录应用日志
        this.saveManagersChangeLog(bbsBoard, true);

        return super.redirectModelAndView("/bbs.do?method=listBoard&spaceType=" + spaceType + "&spaceId=" + spaceId + Functions.csrfSuffix(), "parent.parent");
    }

    // 查询版块信息(修改)
    @CheckRoleAccess(roleTypes = { Role_NAME.GroupAdmin, Role_NAME.AccountAdministrator }, extendRoles = { "SpaceManager" })
    public ModelAndView listBoardModify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("bbs/bbsmanager/bbsmanagemodify");
    }

    /**
     * 进入修改讨论版块信息页面
     */
    @CheckRoleAccess(roleTypes = { Role_NAME.NULL })
    public ModelAndView oldBoard(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        Long vJoinAllowAccount = OrgHelper.getVJoinAllowAccount();
        ModelAndView mav = new ModelAndView("bbs/bbsmanager/modifyboard");
        String from = request.getParameter("from");
        if (!user.isAdmin() && "index".equals(from)) {
            mav = new ModelAndView("bbs/boardSetting");
        }
        Long accountId = user.getLoginAccount();
        boolean isGroup = user.isGroupAdmin();
        String spaceType = request.getParameter("spaceType");
        String spaceId = request.getParameter("spaceId");
        List<V3xBbsBoard> bbsBoardList = new ArrayList<V3xBbsBoard>();
        V3xBbsBoard bbsBoard = bbsBoardManager.getBoardById(Long.parseLong(request.getParameter("id")));
        // 快速需求(Fast Demand):增加板块停用状态,这里的增加判断是否是正常状态的板块
        if (bbsBoard.getFlag() != BbsConstants.FLAG_NORMAL && !user.isAdmin() && "index".equals(from)) {
            super.rendJavaScript(response, "alert('" + ResourceUtil.getString("bbs.article.disable2.js")
                    + "');parent.location.href=parent.location.href;");
            return null;
        }
        if (StringUtils.isNotBlank(spaceId)) {
            PortalSpaceFix spaceFix = portalApi.getSpaceFix(Long.valueOf(spaceId));
            spaceType = String.valueOf(spaceFix.getType());
        }
        boolean showVjoin = bbsBoard.getAffiliateroomFlag() == 2 && bbsBoard.getAccountId().equals(vJoinAllowAccount);
        mav.addObject("showVjoin", showVjoin);
        mav.addObject("bbsBoard", bbsBoard);
//      恩华药业  zhou start
        Map map=new HashMap();
        map.put("moduleId",bbsBoard.getId());
        List<EhSendRange> ehSendRanges=sendRangeManager.findEhSendRangeByCondition(map);
        if(ehSendRanges.size()>0){
            mav.addObject("range", ehSendRanges.get(0));
        }else {
            mav.addObject("range", null);
        }
//      恩华药业  zhou end

        mav.addObject("authPost", bbsBoard.getAuthInfo(BbsConstants.AUTH_TO_POST));
        mav.addObject("forbiddenReply", bbsBoard.getAuthInfo(BbsConstants.FORBIDDEN_TO_REPLY));
        if (Strings.isNotBlank(spaceId)) {
            List<Object[]> entityObj = portalApi.getSecuityOfSpace(Long.parseLong(spaceId));
            String entity = "";
            for (Object[] obj : entityObj) {
                entity += obj[0] + "|" + obj[1] + ",";
            }
            if (!"".equals(entity)) {
                entity = entity.substring(0, entity.length() - 1);
            }
            mav.addObject("spaceType", spaceType);
            mav.addObject("entity", entity);
            bbsBoardList = this.bbsBoardManager.getBbsBoards4Page(Long.parseLong(spaceId), Integer.parseInt(spaceType));
        } else {
            bbsBoardList = this.bbsBoardManager.getBbsBoards4Page(isGroup, accountId);
        }
        List<String> nameList = new ArrayList<String>();
        for (V3xBbsBoard board : bbsBoardList) {
            nameList.add(board.getName());
        }
        return mav.addObject("nameList", nameList);
    }

    /**
     * 完成对讨论板块信息的修改，包括持久化与同步内存
     */
    @CheckRoleAccess(roleTypes = { Role_NAME.GroupAdmin, Role_NAME.AccountAdministrator }, extendRoles = { "SpaceManager" })
    public ModelAndView modifyBoard(HttpServletRequest request, HttpServletResponse response) throws Exception {
        V3xBbsBoard bbsBoard = setVO(request, response);
        String spaceId = request.getParameter("spaceId");
        String spaceType = request.getParameter("spaceType");
        String imageId = request.getParameter("imageId");
        if (StringUtils.isNotBlank(spaceId)) {
            PortalSpaceFix spaceFix = portalApi.getSpaceFix(Long.valueOf(spaceId));
            bbsBoard.setAffiliateroomFlag(spaceFix.getType());
            spaceType = String.valueOf(spaceFix.getType());
        }
        if (StringUtils.isNotBlank(imageId)) {
            bbsBoard.setImageId(Long.parseLong(imageId));
        }
        this.delRoleInfo(bbsBoard);

        List<Long> auth = CommonTools.parseStr2Ids(request, "bbsBoardAdmin");
        this.bbsBoardManager.updateV3xBbsBoard(bbsBoard, auth);

//        恩华药业 zhou start
        Map map=new HashMap();
        map.put("moduleId",bbsBoard.getId());
        List<EhSendRange> ehSendRanges=sendRangeManager.findEhSendRangeByCondition(map);
        if(ehSendRanges.size()>0){
            EhSendRange ehSendRange=ehSendRanges.get(0);
            String rangeId=request.getParameter("sendArrangeId");
            String rangeName=request.getParameter("sendArrangeName");
            ehSendRange.setRangeId(rangeId);
            ehSendRange.setRangeName(rangeName);
            sendRangeManager.updateEhSendRange(ehSendRange);
        }else {
            EhSendRange ehSendRange=new EhSendRange();
            String rangeId=request.getParameter("sendArrangeId");
            String rangeName=request.getParameter("sendArrangeName");
            ehSendRange.setRangeId(rangeId);
            ehSendRange.setRangeName(rangeName);
            ehSendRange.setModuleId(bbsBoard.getId());
            ehSendRange.setId(System.currentTimeMillis());
            sendRangeManager.saveEhSendRange(ehSendRange);
        }
//        恩华药业 zhou end

        this.saveRoleInfo(request.getParameter("bbsBoardAdmin"));

        //对管理员、审核员设定记录应用日志
        this.saveManagersChangeLog(bbsBoard, false);
        return super.redirectModelAndView("/bbs.do?method=listBoard&spaceId=" + spaceId + "&spaceType=" + spaceType + Functions.csrfSuffix(), "parent.parent");
    }

    // 查询版块信息
    @CheckRoleAccess(roleTypes = { Role_NAME.GroupAdmin, Role_NAME.AccountAdministrator }, extendRoles = { "SpaceManager" })
    public ModelAndView listBoardDel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("bbs/bbsmanager/bbsmanagedelete");
    }

    // 删除版块信息
    @CheckRoleAccess(roleTypes = { Role_NAME.GroupAdmin, Role_NAME.AccountAdministrator }, extendRoles = { "SpaceManager" })
    public ModelAndView delBoard(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String[] boardIdStrArray = request.getParameterValues("id");
        if (boardIdStrArray != null && boardIdStrArray.length > 0) {
            List<Long> boardIds = CommonTools.parseStrArr2Ids(boardIdStrArray);
            if (Strings.isNotEmpty(boardIds)) {
                for (Long boardId : boardIds) {
                    this.delRoleInfo(bbsBoardManager.getBoardById(boardId));
                }
            }
        }

        this.bbsBoardManager.deleteBoards(request.getParameterValues("id"));
        String spaceId = request.getParameter("spaceId");
        String spaceType = request.getParameter("spaceType");
        return super.redirectModelAndView("/bbs.do?method=listBoard&spaceType=" + spaceType + "&spaceId=" + spaceId + Functions.csrfSuffix(), "parent");
    }

    /**
     * 讨论版块排序转向页面
     */
    @CheckRoleAccess(roleTypes = { Role_NAME.GroupAdmin, Role_NAME.AccountAdministrator }, extendRoles = { "SpaceManager" })
    public ModelAndView orderBbsBoard(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        String spaceId = request.getParameter("spaceId");
        String spaceType = request.getParameter("spaceType");
        int spaceTypeInt = "public_custom".equalsIgnoreCase(spaceType) ? SpaceType.public_custom.ordinal() : SpaceType.public_custom_group.ordinal();
        if (StringUtils.isNotBlank(spaceId)) {
            PortalSpaceFix spaceFix = portalApi.getSpaceFix(Long.valueOf(spaceId));
            spaceTypeInt = spaceFix.getType();
        }
        List<V3xBbsBoard> bbsBoardList = new ArrayList<V3xBbsBoard>();
        if (Strings.isNotBlank(spaceId)) {
            bbsBoardList = bbsBoardManager.getAllCustomAccBbsBoard(Long.parseLong(spaceId), spaceTypeInt);
        } else {
            bbsBoardList = user.isGroupAdmin() ? bbsBoardManager.getAllGroupBbsBoard() : bbsBoardManager.getAllCorporationBbsBoard(user.getLoginAccount());
        }
        return new ModelAndView("bbs/bbsmanager/orderBbsBoard").addObject("bbsBoardList", bbsBoardList);
    }

    /**
     * 保存讨论版块排序结果
     */
    @CheckRoleAccess(roleTypes = { Role_NAME.GroupAdmin, Role_NAME.AccountAdministrator }, extendRoles = { "SpaceManager" })
    public ModelAndView saveOrder(HttpServletRequest request, HttpServletResponse response) throws Exception {
        bbsBoardManager.updateBbsBoardOrder(request.getParameterValues("projects"));
        super.rendJavaScript(response, "parent.location.href=parent.location;");
        return null;
    }

    /**
     * 单位、集团讨论版块管理员和审核员设置与变更时保存日志
     * @param bbsBoard
     */
    private void saveManagersChangeLog(V3xBbsBoard bbsBoard, boolean isNew) {
        User user = AppContext.getCurrentUser();
        String actionText = ResourceUtil.getString("bul.manageraction." + isNew);
        if (user.isGroupAdmin()) {
            this.appLogManager.insertLog(user, AppLogAction.Group_BbsManagers_Update, user.getName(), bbsBoard.getName(), actionText);
        } else {
            String accountName = null;
            try {
                accountName = this.orgManager.getAccountById(user.getLoginAccount()).getName();
            } catch (Exception e) {

            }
            this.appLogManager.insertLog(user, AppLogAction.Account_BbsManagers_Update, user.getName(), accountName, bbsBoard.getName(), actionText);
        }
    }

    /**
     * 取消管理员角色
     * 其它同类型（单位、集团）板块所有管理员中不包含此板块管理员的先删除增加
     * @param board
     * @throws BusinessException
     */
    private void delRoleInfo(V3xBbsBoard board) throws BusinessException {
        if (board == null) {
            return;
        }

        User user = AppContext.getCurrentUser();
        String roleName = "";
        List<V3xBbsBoard> boards = null;
        if (user.isGroupAdmin()) {
            roleName = OrgConstants.Role_NAME.GroupDiscussAdmin.name();
            boards = bbsBoardManager.getAllGroupBbsBoard();
        } else {
            roleName = OrgConstants.Role_NAME.UnitDiscussAdmin.name();
            boards = bbsBoardManager.getAllCorporationBbsBoard(user.getLoginAccount());
        }

        Set<Long> otherAdmins = new HashSet<Long>();
        if (Strings.isNotEmpty(boards)) {
            for (V3xBbsBoard v3xBbsBoard : boards) {
                if (!v3xBbsBoard.equals(board)) {
                    otherAdmins.addAll(v3xBbsBoard.getAdmins());
                }
            }
        }

        String entityIds = "";
        List<Long> oldAdmins = board.getAdmins();
        if (Strings.isNotEmpty(oldAdmins)) {
            for (Long admin : oldAdmins) {
                if (!otherAdmins.contains(admin)) {
                    entityIds += OrgConstants.ORGENT_TYPE.Member.name() + "|" + admin + ",";
                }
            }
        }

        if (Strings.isNotBlank(entityIds)) {
            roleManager.delRole2Entity(roleName, AppContext.currentAccountId(), entityIds);
        }
    }

    /**
     * 讨论管理员角色处理
     * @param adminStr
     * @throws BusinessException
     */
    private void saveRoleInfo(String adminStr) throws BusinessException {
        User user = AppContext.getCurrentUser();
        String roleName = "";
        if (user.isGroupAdmin()) {
            roleName = OrgConstants.Role_NAME.GroupDiscussAdmin.name();
        } else {
            roleName = OrgConstants.Role_NAME.UnitDiscussAdmin.name();
        }
        roleManager.batchRole2Member(roleName, AppContext.currentAccountId(), adminStr);
    }

    private V3xBbsBoard setVO(HttpServletRequest request, HttpServletResponse response) throws Exception {
        V3xBbsBoard board = null;
        if (Strings.isNotBlank(request.getParameter("id"))) {
            board = bbsBoardManager.getBoardById(Long.parseLong(request.getParameter("id")));
        } else {
            board = new V3xBbsBoard();
            //讨论板块未设定创建时间和修改时间的区分，排序时使用创建时间，在修改时，不设此值，避免修改过后排序发生变动
            board.setBoardTime(new Timestamp(System.currentTimeMillis()));
        }
        //讨论区名称
        board.setName(request.getParameter("name"));
        //讨论区描述
        board.setDescription(request.getParameter("description"));
        //置顶数
        board.setTopNumber(Integer.parseInt(request.getParameter("topNumber")));
        // 回复排序
        board.setOrderFlag(Integer.parseInt(request.getParameter("orderFlag")));
        // 快速需求(Fast Demand):增加板块停用状态,直接从页面取值
        // 是否启用
        board.setFlag(Integer.parseInt(request.getParameter("userFlag")));
        //是否允许匿名发帖
        if (Strings.isNotBlank(request.getParameter("anonymousFlag")))
            board.setAnonymousFlag(Byte.parseByte(request.getParameter("anonymousFlag")));

        //是否允许匿名回复
        if (Strings.isNotBlank(request.getParameter("anonymousReplyFlag")))
            board.setAnonymousReplyFlag(Byte.parseByte(request.getParameter("anonymousReplyFlag")));

        //设置单位
        User user = AppContext.getCurrentUser();
        Long accountId = user.getLoginAccount();
        V3xOrgAccount account = orgManager.getAccountById(accountId);
        String spaceId = request.getParameter("spaceId");
        String spaceType = request.getParameter("spaceType");
        int spaceTypeInt = "public_custom".equalsIgnoreCase(spaceType) ? SpaceType.public_custom.ordinal() : SpaceType.public_custom_group.ordinal();
        if (account.isGroup()) {
            board.setAffiliateroomFlag(SpaceType.group.ordinal());
            board.setAccountId(accountId);
        } else if (Strings.isNotBlank(spaceId)) {
            board.setAffiliateroomFlag(spaceTypeInt);
            board.setAccountId(Long.parseLong(spaceId));
        } else {
            board.setAffiliateroomFlag(SpaceType.corporation.ordinal());
            board.setAccountId(accountId);
        }
        return board;
    }

    //版块授权
    public ModelAndView authBoard(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        Long userId = user.getId();
        String userName = user.getName();

        String boardId = request.getParameter("boardId");
        String authType = request.getParameter("authType");
        String authInfo = null;
        String oldAuthInfo = null;
        //授权的ID集合
        List<Long> auth = new ArrayList<Long>();

        V3xBbsBoard board = bbsBoardManager.getBoardById(Long.valueOf(boardId));
        if (BbsConstants.AUTH_TO_POST.equals(authType)) {//授权发帖权限
            authInfo = request.getParameter("authIssueIds");
            oldAuthInfo = board.getAuthInfo(BbsConstants.AUTH_TO_POST);
        } else if (BbsConstants.FORBIDDEN_TO_REPLY.equals(authType)) {//禁止回帖权限
            authInfo = request.getParameter("authReplyIds");
            oldAuthInfo = board.getAuthInfo(BbsConstants.FORBIDDEN_TO_REPLY);
        }

        if (!oldAuthInfo.equals(authInfo)) {
            //构造消息接收者
            Set<V3xOrgMember> membersSet = orgManager.getMembersByTypeAndIds(authInfo);
            for (V3xOrgMember entity : membersSet) {
                if (!userId.equals(entity.getId())) {
                    auth.add(entity.getId());
                }
            }
            Collection<MessageReceiver> receivers = MessageReceiver.get(Long.parseLong(boardId), auth);

            String messageKey = "";
            if (BbsConstants.AUTH_TO_POST.equals(authType)) {//授权发帖权限
                messageKey = "bbs.auth";
                this.bbsBoardManager.authGeneric(Long.parseLong(boardId), auth, authInfo);
            } else if (BbsConstants.FORBIDDEN_TO_REPLY.equals(authType)) {//禁止回帖权限
                messageKey = "bbs.no.reply";
                this.bbsBoardManager.authNoReply(Long.parseLong(boardId), auth, authInfo);
            }

            //发送消息
            try {
                userMessageManager.sendSystemMessage(MessageContent.get(messageKey, board.getName(), AppContext.getCurrentUser().getName()), ApplicationCategoryEnum.bbs, AppContext.getCurrentUser().getId(), receivers, Long.parseLong(boardId));
            } catch (Exception e) {
                logger.error("send message failed", e);
            }

            appLogManager.insertLog(user, AppLogAction.Bbs_PostAuth_Update, userName, board.getName());
        }

        super.rendJavaScript(response, "parent.window.close();");
        return null;
    }

    private List<ArticleModel> getArticleModelList(List<V3xBbsArticle> articleList, boolean needIssueArea, Long boardId) throws Exception {
        V3xBbsBoard v3xBbsBoard = null;
        if (boardId != null) {
            v3xBbsBoard = this.bbsBoardManager.getBoardById(boardId);
        }

        List<ArticleModel> articleModelList = new ArrayList<ArticleModel>();

        if (articleList != null) {
            // 构造显示列表
            for (V3xBbsArticle v3xBbsArticle : articleList) {
                ArticleModel articleModel = new ArticleModel(v3xBbsArticle);
                articleModel.setClickNumber(getSyncClickNumber(v3xBbsArticle.getClickNumber(), v3xBbsArticle.getId()));
                Long issueUserId = v3xBbsArticle.getIssueUserId();
                articleModel.setIssueImage(Functions.getAvatarImageUrl(issueUserId));
                if (v3xBbsBoard == null) {
                    articleModel.setBoard(this.bbsBoardManager.getBoardById(v3xBbsArticle.getBoardId()));
                } else {
                    articleModel.setBoard(v3xBbsBoard);
                }
                articleModel.setAdminFlag(this.bbsBoardManager.validUserIsAdmin(v3xBbsArticle.getBoardId(), AppContext.getCurrentUser().getId()));
                articleModel.setIssueName(Functions.showMemberName(v3xBbsArticle.getIssueUserId()));
                articleModelList.add(articleModel);
            }
        }

        return articleModelList;
    }

    private int getSyncClickNumber(Integer dbNum, Long id) {
        V3xBbsArticle bbs = bbsArticleManager.getDataCache().get(id);
        if (bbs != null) {
            return bbs.getClickNumber();
        }
        return dbNum;
    }

    /**
     * 更多项目讨论
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView moreProjectBbs(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long projectId = Long.parseLong(request.getParameter("projectId"));
        boolean hasAccess = projectApi.hasViewPermission(AppContext.currentUserId(), projectId);
        if (!hasAccess) {
            throw new BusinessException(AppContext.currentUserName() + " 无权访问");
        }

        ModelAndView mav = new ModelAndView("bbs/project/moreProjectBbs");
        // 查看项目信息 需要加载项目进度列表
        ProjectBO projectCompose = projectApi.getProject(projectId);

        //某个阶段|当前阶段|所有阶段
        String phaseIds = request.getParameter("phaseId");
        Long phaseId = null;
        if (StringUtils.isNotBlank(phaseIds)) {
            phaseId = NumberUtils.toLong(phaseIds);
        } else {
            phaseId = projectCompose.getPhaseId();
        }

        List<V3xBbsArticle> v3xBbsArticleList = bbsArticleManager.projectQueryArticleListByCondition("choice", projectId, -1, phaseId, null);

        List<ArticleModel> bbsList = this.getArticleModelList(v3xBbsArticleList, false, null);

        Long cpid = AppContext.getCurrentUser().getId();
        V3xBbsBoard bb = bbsBoardManager.getBoardById(projectId);
        boolean isManager = false;
        List<Long> pers = bb.getAdmins();
        for (Long pid : pers) {
            if (cpid.longValue() == pid.longValue()) {
                isManager = true;
                break;
            }
        }
        if (request.getParameter("managerFlag") != null) {
            int managerFlag = Integer.valueOf(request.getParameter("managerFlag"));
            if (managerFlag == 1) {
                isManager = true;
            }
        }
        returnIsProjectNew(projectId, mav);
        mav.addObject("projectId", projectId);
        mav.addObject("phaseId", phaseId);
        mav.addObject("isProjectManager", isManager);
        mav.addObject("bbsList", bbsList);
        mav.addObject("projectCompose", projectCompose);
        mav.addObject("morePro", ApplicationCategoryEnum.bbs);
        mav.addObject("managerFlag", request.getParameter("managerFlag"));
        mav.addObject("relat", request.getParameter("relat"));
        mav.addObject("projectState", request.getParameter("projectState"));
        return mav;
    }

    /**
     * 是否有新建项目讨论的权限
     * @param projectId
     * @param mav
     * @throws BusinessException
     */
    private void returnIsProjectNew(long projectId, ModelAndView mav) throws BusinessException {
        ProjectBO project = projectApi.getProject(projectId);
        boolean isEnd = Integer.valueOf(2).equals(project.getProjectState());
        User user = AppContext.getCurrentUser();
        if (!isEnd && projectApi.hasProjectSectionNew(user.getId(), projectId) && (user.hasResourceCode("F05_bbsIndexGroup") || user.hasResourceCode("F05_bbsIndexAccount"))) {
            mav.addObject("isNewBbs", Boolean.TRUE);
        }
    }

    /**
     * 条件查询更多项目讨论
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView queryMoreProjectBbsByCondition(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("bbs/project/moreProjectBbs");

        long projectId = Long.parseLong(request.getParameter("projectId"));
        String condition = request.getParameter("condition");
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String beginTime = request.getParameter("beginTime");
        String endTime = request.getParameter("endTime");

        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("title", title);
        paramMap.put("author", author);
        paramMap.put("beginTime", beginTime);
        paramMap.put("endTime", endTime);

        // 查看项目信息 需要加载项目进度列表
        ProjectBO projectCompose = projectApi.getProject(projectId);

        //某个阶段|当前阶段|所有阶段
        String phaseIds = request.getParameter("phaseId");
        Long phaseId = null;
        if (StringUtils.isNotBlank(phaseIds)) {
            phaseId = NumberUtils.toLong(phaseIds);
        } else {
            phaseId = projectCompose.getPhaseId();
        }

        List<V3xBbsArticle> v3xBbsArticleList = bbsArticleManager.projectQueryArticleListByCondition(condition, projectId, -1, phaseId, paramMap);
        List<ArticleModel> bbsList = this.getArticleModelList(v3xBbsArticleList, false, null);

        Long cpid = AppContext.getCurrentUser().getId();
        V3xBbsBoard bb = bbsBoardManager.getBoardById(projectId);
        boolean isManager = false;
        List<Long> pers = bb.getAdmins();
        for (Long pid : pers) {
            if (cpid.longValue() == pid.longValue()) {
                isManager = true;
                break;
            }
        }
        //OA-79881
        if (request.getParameter("managerFlag") != null) {
            int managerFlag = Integer.valueOf(request.getParameter("managerFlag"));
            if (managerFlag == 1) {
                isManager = true;
            }
        }
        returnIsProjectNew(projectId, mav);
        // 查询条件回置
        mav.addObject("condition", condition);
        if ("title".equals(condition))
            mav.addObject("title", title);
        else if ("author".equals(condition))
            mav.addObject("author", author);
        else if ("publishDate".equals(condition))
            mav.addObject("beginTime", beginTime);
        mav.addObject("endTime", endTime);
        mav.addObject("projectId", projectId);
        mav.addObject("phaseId", phaseId);
        mav.addObject("isProjectManager", isManager);
        mav.addObject("bbsList", bbsList);
        mav.addObject("projectCompose", projectCompose);
        mav.addObject("morePro", ApplicationCategoryEnum.bbs);
        return mav;
    }

    private static int toInt(Integer num) {
        return num != null ? num.intValue() : 0;
    }

    /**
     * 获取关联项目讨论区发帖时的默认讨论范围
     * @param boardId 关联项目讨论区ID
     */
    private String getProjectBbsPublishScope(Long boardId) throws Exception {
        StringBuilder projectArea = new StringBuilder();

        List<ProjectMemberInfoBO> projectMembers = projectApi.findProjectMembers(boardId);

        if (CollectionUtils.isNotEmpty(projectMembers)) {
            for (ProjectMemberInfoBO projectMemberInfo : projectMembers) {
                projectArea.append(OrgConstants.ORGENT_TYPE.Member + "|" + projectMemberInfo.getMemberId() + ",");
            }
        }

        return projectArea.toString();
    }

    /**
     * 获取部门讨论区发帖时的默认讨论范围：能够访问部门空间的全体人员
     * @param boardId
     */
    private String getDeptBbsPublishScope(Long boardId) throws BusinessException {
        List<Object[]> _issueAreas = portalApi.getSecuityOfSpace(boardId);
        List<String> result = new UniqueList<String>();
        for (Object[] objects : _issueAreas) {
            if (OrgConstants.ORGENT_TYPE.Member.toString().equals(objects[0].toString())) {
                V3xOrgMember member = orgManager.getMemberById((Long) objects[1]);
                if (member == null || !member.isValid() || member.getOrgDepartmentId().equals(boardId))
                    continue;
            }
            result.add(objects[0] + "|" + objects[1]);
        }
        return StringUtils.join(result, ",");
    }

    /**
     * showPost页面防护js动作：关闭窗口、刷新其父窗口(如果能够获取到的话)
     */
    private String       jsAction      = "if(window.opener) " + "    try {window.opener.getA8Top().reFlesh();}catch(e) {}" + "window.close();";

    //用于点击次数更新同步
    private final byte[] readCountLock = new byte[0];

    /**
     * 获取帖子的讨论范围，先从缓存的帖子属性中取，如取不到，再从数据库取一次
     */
    private String getIssueAreaOfArticle(V3xBbsArticle article, V3xBbsBoard board) throws Exception {
        String result = article.getIssueArea();
        User user = AppContext.getCurrentUser();
        if (StringUtils.isBlank(result)) {
            if (board.getAffiliateroomFlag() == SpaceType.related_project.ordinal()) {
                result = this.getProjectBbsPublishScope(board.getId());
            } else {
                List<V3xBbsArticleIssueArea> areas = bbsArticleManager.getIssueArea(article.getId());
                if (CollectionUtils.isNotEmpty(areas)) {
                    StringBuilder sb = new StringBuilder();
                    for (V3xBbsArticleIssueArea area : areas) {
                        sb.append(area.getModuleType() + "|" + area.getModuleId() + ",");
                    }
                    result = sb.substring(0, sb.length() - 1);
                }
            }
            if (StringUtils.isBlank(result)) {
                if (board.getAffiliateroomFlag() == SpaceType.department.ordinal()) {
                    List<PortalSpaceFix> spacePath = portalApi.getAccessSpaceFromCache(user.getId(), user.getLoginAccount());
                    if (spacePath != null && spacePath.size() > 0) {
                        for (PortalSpaceFix portalSpaceFix : spacePath) {
                            if (portalSpaceFix.getType().intValue() == SpaceType.department.ordinal()) {
                                result = this.getDeptBbsPublishScope(portalSpaceFix.getId());
                            }
                        }
                    }
                }
            }
            article.setIssueArea(result);
        }
        return result;
    }

    /**
     * 回复讨论主题或修改自己的回复时，显示回复人姓名及其部门名称，格式："菜鸟杨(研发二部)"
     */
    private String getReplyUserNameWithDeptName() {
        String result = null;
        try {
            result = AppContext.getCurrentUser().getName() + "(" + orgManager.getEntityById(V3xOrgDepartment.class, AppContext.getCurrentUser().getDepartmentId()).getName() + ")";
        } catch (BusinessException e) {
            log.error("", e);
        }
        return result;
    }

    /**
     * 先从缓存取，如果没有，直接从数据库取
     */
    private V3xBbsArticle getArticleFromCacheOrDB(Long articleId) throws Exception {
        V3xBbsArticle article = null;
        if (articleId != null) {
            article = bbsArticleManager.getDataCache().get(articleId);
            if (article == null) {
                article = this.bbsArticleManager.getArticleById(articleId);
            }
        }
        return article;
    }

    public ModelAndView showDesignated(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("bbs/showDesignated");
        User user = AppContext.getCurrentUser();
        List<V3xBbsBoard> typeList = null;
        String group = request.getParameter("group");
        String textfield = request.getParameter("textfield");

        if (Strings.isNotBlank(group)) {
            typeList = bbsBoardManager.getAllGroupBbsBoard();
        } else {
            typeList = bbsBoardManager.getAllCorporationBbsBoard(user.getLoginAccount());
        }

        List<V3xBbsBoard> resultList = new ArrayList<V3xBbsBoard>();
        if (Strings.isNotBlank(textfield)) {
            for (V3xBbsBoard type : typeList) {
                if (type.getName().contains(textfield)) {
                    resultList.add(type);
                }
            }
        } else {
            resultList = typeList;
        }

        mav.addObject("typeList", resultList);
        return mav;
    }

    /**
     * 根据可操作列表展示
     */
    public ModelAndView listType(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        ModelAndView mav = new ModelAndView("bbs/boardmanager/sendTo");
        String ids = request.getParameter("ids");
        String boardId = request.getParameter("boardId");
        V3xBbsBoard board = bbsBoardManager.getBoardById(Long.valueOf(boardId));

        List<V3xBbsBoard> v3xBbsBoardManageList = getCanAdminBoards(user, board);
        Comparator<V3xBbsBoard> comp = new V3xBbsBoard();
        Collections.sort(v3xBbsBoardManageList, comp);
        mav.addObject("typeList", v3xBbsBoardManageList);
        mav.addObject("ids", ids);
        return mav;
    }

    private List<V3xBbsBoard> getCanAdminBoards(User user, V3xBbsBoard board) {
        int spaceType = board.getAffiliateroomFlag();
        Long spaceId = board.getAccountId();
        //获取我可以管理的板块，切换使用
        List<V3xBbsBoard> v3xBbsBoardManageList = new ArrayList<V3xBbsBoard>();
        if (spaceType == SpaceType.public_custom_group.ordinal() || spaceType == SpaceType.public_custom.ordinal()) {
            v3xBbsBoardManageList = this.bbsBoardManager.getCanAdminCustomBbsBoards(user.getId(), spaceId, spaceType);
        } else if (spaceType == SpaceType.custom.ordinal()) {
            v3xBbsBoardManageList = this.bbsBoardManager.getCanAdminCustomBbsBoards(user.getId(), user.getLoginAccount(), spaceType);
        } else if (spaceType == SpaceType.corporation.ordinal()) {
            v3xBbsBoardManageList = this.bbsBoardManager.getCanAdminGroupOrCorpBbsBoards(false, user.getId(), user.getLoginAccount());
        } else if (spaceType == SpaceType.group.ordinal()) {
            v3xBbsBoardManageList = this.bbsBoardManager.getCanAdminGroupOrCorpBbsBoards(true, user.getId(), user.getLoginAccount());
        }

        if (Strings.isNotEmpty(v3xBbsBoardManageList)) {
            // 快速需求(Fast Demand):增加板块停用状态,这里的增加判断是否是正常状态的板块,不正常移除
            Iterator iterator = v3xBbsBoardManageList.iterator();
            while(iterator.hasNext()) {
                V3xBbsBoard v3xBbsBoard = (V3xBbsBoard) iterator.next();
                if (v3xBbsBoard.getFlag() != BbsConstants.FLAG_NORMAL) {
                    iterator.remove();
                }
            }
            v3xBbsBoardManageList.remove(board);
        }
        return v3xBbsBoardManageList;
    }

    /**
     * 移动到新分类方法
     *
     * */
    public ModelAndView moveToType(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String idStr = request.getParameter("ids");
        String typeId = request.getParameter("typeId");
        V3xBbsArticle bean = null;
        if (StringUtils.isBlank(idStr)) {
            throw new Exception("bbs_not_exists");
        } else {
            String[] ids = idStr.split(",");
            for (String id : ids) {
                if (StringUtils.isNotBlank(id)) {
                    bean = bbsArticleManager.getDataCache().get(Long.valueOf(id));
                    if (bean == null) {
                        bean = bbsArticleManager.getArticleById(Long.valueOf(id));
                    }
                    //暂时不改修改时间
                    //bean.setModifyTime(new Timestamp(System.currentTimeMillis()));
                    bean.setBoardId(Long.valueOf(typeId));
                    bean.setTopSequence(BbsConstants.BBS_ARTICLE_ISNOT_TOP);
                    this.bbsArticleManager.updateArticle(bean);
                    bbsArticleManager.getDataCache().save(Long.valueOf(id), bean, System.currentTimeMillis(), (bean.getClickNumber() == null ? 0 : bean.getClickNumber()));
                    this.bbsArticleManager.syncCache(bean, bean.getClickNumber());
                }
            }
        }
        super.rendJavaScript(response, "alert(\"" + ResourceUtil.getString("bbs.board.moved") + "\");parent.cloWithSuccess();");
        return null;
    }

    /**
     * 讨论归档
     *
     */
    public ModelAndView pigeonhole(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String ids = request.getParameter("id");
        User user = AppContext.getCurrentUser();
        String userName = user.getName();
        String[] archiveIds = request.getParameterValues("archiveId");
        if (StringUtils.isNotBlank(ids)) {
            String[] idA = ids.split(",");
            List<Long> idList = new ArrayList<Long>();
            for (int i = 0; i < idA.length; i++) {
                if (StringUtils.isNotBlank(idA[i])) {
                    Long _archiveId = Long.valueOf(archiveIds[i]);
                    DocResourceBO res = docApi.getDocResource(_archiveId);
                    //归档记录应用日志 added by Meng Yang at 2009-08-20
                    if (res != null) {
                        String folderName = docApi.getDocResourceName(res.getParentFrId());
                        appLogManager.insertLog(user, AppLogAction.Bbs_Pigeonhole, userName, res.getFrName(), folderName);
                    }

                    idList.add(Long.valueOf(idA[i]));
                    // 更新缓存中的状态为归档,防止通过系统提示信息查看归档后
                    Long longID = Long.valueOf(idA[i]);
                    V3xBbsArticle bean = bbsArticleManager.getDataCache().get(longID);
                    if (bean != null) {
                        bean.setState(Byte.valueOf("" + BbsConstants.BBS_ARTICLE_PIGEONHOLE));
                        bbsArticleManager.getDataCache().save(longID, bean, bean.getModifyTime().getTime(), (bean.getClickNumber() == null ? 0 : bean.getClickNumber()));
                        this.bbsArticleManager.syncCache(bean, bean.getClickNumber());
                    }
                }
            }
            this.bbsArticleManager.pigeonhole(idList);

        }
        super.rendJavaScript(response, "parent.window.location.reload();");
        return null;
    }

    /**
     * 文化建设统计入口
     */
    public ModelAndView publishInfoStc(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("stc/publishInfoStc");
        int year = DateUtil.getYear();
        Map<String, Object> jval = new HashMap<String, Object>();
        jval.put("year", year);
        jval.put("publishDateStart", year + "-01-01");
        jval.put("publishDateEnd", DateUtil.format(new Date()));
        //那个模块的统计
        String mode = request.getParameter("mode");
        String spceTypeId = request.getParameter("typeId");
        Map<String, Object> modeType = bbsArticleManager.getTypeByMode(mode, spceTypeId);
        jval.put("isGroupStc", !(Boolean) modeType.get("hideAcc"));
        jval.put("isStcDeptHide", modeType.get("hideDept"));
        jval.put("isStcAccHide", modeType.get("hideAcc"));

        jval.put("mode", mode);
        //参数spaceType,spaceId,typeId
        jval.put("spaceType", request.getParameter("spaceType"));
        jval.put("spaceId", spceTypeId);
        jval.put("typeId", request.getParameter("typeId"));
        mav.addObject("today", DateUtil.format(new Date()));
        mav.addObject("jval", Strings.escapeJson(JSONUtil.toJSONString(jval)));
        return mav;
    }

    /**
     * 统计导出
     */
    @SuppressWarnings("unchecked")
    public ModelAndView stcExpToXls(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> param = ParamUtil.getJsonParams();
        DataRecord record = bbsArticleManager.expStcToXls(param);
        fileToExcelManager.save(response, record.getTitle(), record);
        return null;
    }

    /******************************6.0讨论******************************/

    /**
     * 讨论首页
     */
    public ModelAndView bbsIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("bbs/bbsIndex");
        User user = AppContext.getCurrentUser();

        boolean isVjoinMember = !user.isV5Member();

        int spaceType = NumberUtils.toInt(request.getParameter("spaceType"));
        Long spaceId = NumberUtils.toLong(request.getParameter("spaceId"));
        String boardIdStr = request.getParameter("boardId");
        //板块首页
        if (Strings.isNotBlank(boardIdStr)) {
            Long boardId = NumberUtils.toLong(boardIdStr);
            BoardModel boardModel = new BoardModel();
            V3xBbsBoard v3xBbsBoard = bbsBoardManager.getBoardById(boardId);
            if(v3xBbsBoard == null){
                super.rendJavaScript(response, "alert('"+ResourceUtil.getString("bbs.board.deleted")+"');window.close()");
                return null;
            }
            // 快速需求(Fast Demand):增加板块停用状态,这里的增加判断是否是正常状态的板块
            if (v3xBbsBoard.getFlag() != BbsConstants.FLAG_NORMAL) {
                String alertInfo = ResourceUtil.getString("bbs.article.disable2.js");
                super.rendJavaScript(response, "alert('" + alertInfo + "');window.close();");
                return null;
            }
            if (v3xBbsBoard.getAffiliateroomFlag() != SpaceType.group.ordinal() && v3xBbsBoard.getAffiliateroomFlag() != SpaceType.corporation.ordinal()) {
                spaceType = v3xBbsBoard.getAffiliateroomFlag();
                spaceId = v3xBbsBoard.getAccountId();
            }
            boardModel.setId(v3xBbsBoard.getId());
            boardModel.setBoardName(v3xBbsBoard.getName());
            // 判断当前用户是否有发帖权限
            List<Long> domainIds = orgManager.getAllUserDomainIDs(user.getId());
            boolean hasAuthIssue = bbsBoardManager.validIssueAuth(v3xBbsBoard.getId(), user.getId(), domainIds);
            boardModel.setHasAuthIssue(hasAuthIssue);
            List<Long> adminboardIds = new ArrayList<Long>();
            adminboardIds.add(boardId);
            Map<Long, Integer> boardsArticleNumber = bbsArticleManager.getBoardsArticleNumber4Admin(adminboardIds);
            Integer boardsReplyNumber = bbsArticleManager.getBoardsReplyNumber4Admin(boardId);
            // 计算该版块的主题数
            boardModel.setArticleNumber(toInt(boardsArticleNumber.get(boardId)));
            // 板块的回帖数
            boardModel.setReplyPostNumber(toInt(boardsReplyNumber));
            //板块描述
            boardModel.setBoardDescription(v3xBbsBoard.getDescription() == null ? "" : v3xBbsBoard.getDescription());
            //置顶个数
            boardModel.setTopNumber(v3xBbsBoard.getTopNumber());

            //版主
            List<Long> admins = v3xBbsBoard.getAdmins();
            if (spaceType == SpaceType.department.ordinal()) {
                List<Object[]> secuitys = portalApi.getSecuityOfDepartment(boardId, SecurityType.manager.ordinal());
                if (Strings.isNotEmpty(secuitys)) {
                    for (Object[] object : secuitys) {
                        Long adminId = (Long) object[1];
                        if (!admins.contains(adminId)) {
                            admins.add(adminId);
                        }
                    }
                }
            }

            if (Strings.isNotEmpty(admins)) {
                boolean flag = false;
                StringBuilder boardAdmins = new StringBuilder();
                for (Long adminId : admins) {
                    V3xOrgMember v3xOrgMember = orgManager.getMemberById(adminId);
                    if (v3xOrgMember != null) {
                        if (flag) {
                            boardAdmins.append("、");
                        } else {
                            flag = true;
                        }
                        boardAdmins.append(v3xOrgMember.getName());
                    }
                }
                mav.addObject("boardAdmins", boardAdmins.toString());
            }

            // 判断当前用户是否是管理员
            boolean isAdmin = bbsBoardManager.validUserIsAdmin(v3xBbsBoard.getId(), user.getId());
            boardModel.setIsAdminFlag(isAdmin);
            if (isAdmin) {
                List<V3xBbsBoard> v3xBbsBoardManageList = getCanAdminBoards(user, v3xBbsBoard);
                mav.addObject("canMove", Strings.isNotEmpty(v3xBbsBoardManageList));
            }
            mav.addObject("boardMessage", boardModel);
        }
        List<Long> adminBoardIds = null;
        if (spaceType == SpaceType.group.ordinal() || spaceType == SpaceType.corporation.ordinal()) {
            adminBoardIds = bbsArticleManager.getMyBoardIds(0, spaceId);
        } else {
            adminBoardIds = bbsArticleManager.getMyBoardIds(spaceType, spaceId);
        }

        if (Strings.isBlank(boardIdStr)) {
            // 热门板块
            List<V3xBbsBoard> coverBbsBoardList = bbsArticleManager.getBoardCover(adminBoardIds);
            List<BoardModel> coverBoardModelList = new ArrayList<BoardModel>();
            this.getBoardModelList(coverBbsBoardList, coverBoardModelList, user.getId());
            mav.addObject("coverBoardModelList", coverBoardModelList);
            /**秀吧最热列表*/
            if (AppContext.hasResourceCode("F05_show")) {
                List<ShowbarInfoBO> showbarHotList = showApi.findShowbarHotList(5);
                mav.addObject("showbarHotList", showbarHotList);
            }
        }

        // 我发起的，我回复的，我收藏的
        mav.addObject("articleNum", bbsArticleManager.getIssueCount(user.getId(), adminBoardIds));
        mav.addObject("replyNum", bbsArticleManager.getReplyCount(user.getId(), adminBoardIds));
        if (AppContext.hasPlugin("doc")) {
            mav.addObject("collectNum", bbsArticleManager.getCollectCount(user.getId(), adminBoardIds));
        }

        //是否允许发帖
        boolean hasIssue = false;
        Map<String, List<BoardModel>> boardModelMap = new LinkedHashMap<String, List<BoardModel>>();
        if (spaceType == SpaceType.public_custom.ordinal() || spaceType == SpaceType.public_custom_group.ordinal() || spaceType == SpaceType.custom.ordinal()) {//自定义单位、集团空间
            List<V3xBbsBoard> customBbsBoardList = this.bbsBoardManager.getAllCustomAccBbsBoard(spaceId, spaceType);
            List<BoardModel> customBoardModelList = new ArrayList<BoardModel>();
            hasIssue = this.getBoardModelList(customBbsBoardList, customBoardModelList, user.getId());
            if (spaceType == SpaceType.public_custom.ordinal()) {// 自定义单位板块
                boardModelMap.put("17", customBoardModelList);
            } else if (spaceType == SpaceType.public_custom_group.ordinal()) {// 自定义集团板块
                boardModelMap.put("18", customBoardModelList);
            } else if (spaceType == SpaceType.custom.ordinal()) {// 自定义团队版块
                boardModelMap.put("4", customBoardModelList);
            }
        } else if (spaceType == SpaceType.department.ordinal()) {
            // 部门板块
            hasIssue = true;
            List<PortalSpaceFix> deptSpaces = new ArrayList<PortalSpaceFix>();
            PortalSpaceFix space = portalApi.getDeptSpaceIdByDeptId(spaceId);
            deptSpaces.add(space);
            List<BoardModel> deptBoardModelList = this.getDeptBoardModelList(deptSpaces, user.getId());
            boardModelMap.put("1", deptBoardModelList);
        } else {
            boolean hasGroupIssue = false;
            if ((Boolean) (SysFlag.sys_isGroupVer.getFlag()) && user.isInternal()) {
                // 集团板块
                List<V3xBbsBoard> groupBbsBoardList = this.bbsBoardManager.getAllGroupBbsBoard();
                List<BoardModel> groupBoardModelList = new ArrayList<BoardModel>();
                hasGroupIssue = this.getBoardModelList(groupBbsBoardList, groupBoardModelList, user.getId());
                boardModelMap.put("3", groupBoardModelList);
            }

            // 单位板块
            List<V3xBbsBoard> accountBbsBoardList = new ArrayList<V3xBbsBoard>();
            if(user.isVJoinMember()){//vjoin人員時，根据准出单位查询版块
                accountBbsBoardList = this.bbsBoardManager.getAllCorporationBbsBoard(OrgHelper.getVJoinAllowAccount());
            }else{
                accountBbsBoardList = this.bbsBoardManager.getAllCorporationBbsBoard(user.getLoginAccount());
            }
            List<BoardModel> accountBoardModelList = new ArrayList<BoardModel>();
            boolean hasAccountIssue = this.getBoardModelList(accountBbsBoardList, accountBoardModelList, user.getId());
            boardModelMap.put("2", accountBoardModelList);

            hasIssue = hasGroupIssue || hasAccountIssue;
        }

        mav.addObject("boardModelMap", boardModelMap);
        mav.addObject("hasIssue", hasIssue);
        mav.addObject("isVjoinMember", isVjoinMember);
        return mav;
    }

    private boolean getBoardModelList(List<V3xBbsBoard> v3xBbsBoardList, List<BoardModel> boardModelList, Long userId) throws Exception {
        boolean allowPost = false;
        List<Long> domainIds = orgManager.getAllUserDomainIDs(userId);
        for (V3xBbsBoard v3xBbsBoard : v3xBbsBoardList) {
            // 快速需求(Fast Demand):增加板块停用状态,这里的增加判断是否是正常状态的板块
            if (v3xBbsBoard.getFlag() != BbsConstants.FLAG_NORMAL) {
                continue;
            }
            BoardModel boardModel = new BoardModel();
            boardModel.setId(v3xBbsBoard.getId());
            boardModel.setBoardName(v3xBbsBoard.getName());

            // 判断当前用户是否是管理员
            boolean isAdmin = bbsBoardManager.validUserIsAdmin(v3xBbsBoard.getId(), userId);
            boardModel.setIsAdminFlag(isAdmin);
            // 判断当前用户是否有发帖权限
            boolean hasAuthIssue = bbsBoardManager.validIssueAuth(v3xBbsBoard.getId(), userId, domainIds);
            if (isAdmin || hasAuthIssue) {
                allowPost = true;
            }
            boardModel.setHasAuthIssue(hasAuthIssue);
            boardModel.setImageId(v3xBbsBoard.getImageId());
            boardModel.setAffiliateroomFlag(v3xBbsBoard.getAffiliateroomFlag());
            boardModelList.add(boardModel);
        }
        return allowPost;
    }

    private List<BoardModel> getDeptBoardModelList(List<PortalSpaceFix> spaces, Long userId) throws Exception {
        List<BoardModel> boardModelList = new ArrayList<BoardModel>();
        List<Long> managerSpaces = portalApi.getCanManagerSpace(userId);
        for (PortalSpaceFix portalSpaceFix : spaces) {
            BoardModel boardModel = new BoardModel();
            boardModel.setId(portalSpaceFix.getEntityId());
            boardModel.setBoardName(ResourceUtil.getString(portalSpaceFix.getSpacename()));

            // 判断当前用户是否是管理员
            if (Strings.isNotEmpty(managerSpaces) && managerSpaces.contains(portalSpaceFix.getId())) {
                boardModel.setIsAdminFlag(true);
            } else {
                boardModel.setIsAdminFlag(false);
            }

            // 判断当前用户是否有发帖权限
            boardModel.setHasAuthIssue(true);
            boardModel.setAffiliateroomFlag(SpaceType.department.ordinal());
            boardModelList.add(boardModel);
        }
        return boardModelList;
    }

    /**
     * 老页面跳转兼容（勿删）
     */
    public ModelAndView showPost(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return super.redirectModelAndView("/bbs.do?method=bbsView&articleId=" + request.getParameter("articleId"));
    }

    public ModelAndView bbsView(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //from=message，从消息打开
        //from=pigeonhole，从归档打开
        //from=colCube，从协同立方打开
        User user = AppContext.getCurrentUser();
        Long userId = user.getId();
        ModelAndView mav = new ModelAndView("bbs/bbsInfo");
        String from = request.getParameter("from");
        String nowPagePara = request.getParameter("nowPagePara");
        Long articleId = NumberUtils.toLong(request.getParameter("articleId"));

        V3xBbsArticle article = bbsArticleManager.getDataCache().get(articleId);
        boolean hasCache = article != null;
        if (article == null) {
            article = bbsArticleManager.getArticleById(articleId);
        }

        boolean alert = false;
        String alertInfo = "";
        boolean isAdmin = false;// 判断当前用户是否是管理员("删除"总是可见）
        if (article == null || article.getState().intValue() == BbsConstants.BBS_ARTICLE_ISNOT_ACTIVE) {
            alert = true;
            alertInfo = ResourceUtil.getString("bbs.article.delorcanceled.js");
        }

        try {
            ReplaceBase64Result result = fileManager.replaceBase64Image(article.getContent());
            if (result.isConvertBase64Img()){
                article.setContent(result.getHtml());
                bbsArticleManager.updateArticle(article);
            }
        } catch (Exception e) {// 查看时，如果转换失败就不转换了
            log.error("An exception occurred when the base64 encoding picture in the text was converted to a URL!",e);
        }

        V3xBbsBoard board = null;
        if (!alert && article != null) {
            board = this.bbsBoardManager.getBoardById(article.getBoardId());
            if (board == null || board.getFlag() != BbsConstants.FLAG_NORMAL) {
                alert = true;
                // 快速需求(Fast Demand):增加板块停用状态,停用的板块提示
                alertInfo = ResourceUtil.getString("bbs.article.disable2.js");
            } else {
                isAdmin = bbsBoardManager.validUserIsAdmin(article.getBoardId(), userId);
                String issueArea = this.getIssueAreaOfArticle(article, board);
                mav.addObject("issueArea", issueArea);
                alert = (!article.getIssueUserId().equals(userId) && !inIssueArea(issueArea) && !isAdmin);
            }
        }

        if (alert || (article.getState().intValue() == BbsConstants.BBS_ARTICLE_PIGEONHOLE && !"pigeonhole".equals(from))) {
            if(Strings.isBlank(alertInfo)){
                alertInfo = ResourceUtil.getString("bbs.article.delorcanceled.js");
            }
            // 来自协同立方的话，这样关闭，
            if ("colCube".equals(from)) {
                super.rendJavaScript(response, "alert('" + alertInfo + "');window.close();parent.parent.window.parentDialogObj.url.closeParam.handler();");
            } else {
                super.rendJavaScript(response, "alert('" + alertInfo + "');window.close();");
            }
            return null;
        }

        //更新消息状态
        userMessageManager.updateSystemMessageStateByUserAndReference(userId, articleId);
        bbsReadManager.save(userId, articleId);

        //需要更新栏目中的点击数，所以不能按人
        eTagCacheManager.updateETagDate(BbsConstants.ETAG_BBS_USER, "-1");

        if (article.getState() != 3 && article.getState() != BbsConstants.BBS_ARTICLE_PIGEONHOLE) {
            if (hasCache) {
                bbsArticleManager.clickCache(articleId, userId);
            } else {
                int readCount = 0;
                synchronized (readCountLock) {
                    readCount = article.getClickNumber() == null ? 0 : article.getClickNumber().intValue();
                    article.setClickNumber(readCount + 1);
                }
                // 保存到缓存
                bbsArticleManager.syncCache(article, readCount + 1);
            }
        }

        article.setDepartment(orgManager.getMemberById(article.getIssueUserId()).getOrgDepartmentId());
        mav.addObject("article", article);
        mav.addObject("board", board);
        mav.addObject("currentUserIsAdmin", isAdmin);
        Long issueUserId = article.getIssueUserId();
        mav.addObject("issuerImage", Functions.getAvatarImageUrl(issueUserId));

        mav.addObject("canModify", userId.equals(issueUserId) && article.getState() != BbsConstants.BBS_ARTICLE_PIGEONHOLE);
        List<Long> domainIds = orgManager.getAllUserDomainIDs(userId);
        boolean canReply = bbsBoardManager.validReplyAuth(article.getBoardId(), userId, domainIds);
        mav.addObject("canReply", canReply && article.getState() != BbsConstants.BBS_ARTICLE_PIGEONHOLE);

        boolean canDeleteArticleFlag = false;
        if (article.getState() != BbsConstants.BBS_ARTICLE_PIGEONHOLE) {
            if (isAdmin) {
                canDeleteArticleFlag = true;
            } else {
                if (userId.equals(issueUserId) && !article.getEliteFlag() && article.getTopSequence() == 0) {
                    canDeleteArticleFlag = true;
                }
            }
        }
        mav.addObject("canDeleteArticleFlag", canDeleteArticleFlag);

        //讨论附件
        List<Attachment> bbsAttachments = new ArrayList<Attachment>();
        Map<Long, List<Attachment>> replyAttachementMap = new HashMap<Long, List<Attachment>>();
        List<Attachment> articleAtts = attachmentManager.getByReference(articleId);
        for (Attachment atta : articleAtts) {
            if (atta.getSubReference().equals(articleId)) {
                bbsAttachments.add(atta);
            } else {
                Strings.addToMap(replyAttachementMap, atta.getSubReference(), atta);
            }
        }
        mav.addObject("bbsAttListJSON", JSONUtil.toJSONString(bbsAttachments));

        //最赞回复
        List<V3xBbsArticleReply> hotListReplyList = bbsArticleManager.findHotReplyListById(articleId);//最赞
        List<ArticleReplyModel> hotReplyModelList = this.bbsArticleManager.getReplyModelList(hotListReplyList, article, replyAttachementMap);
        mav.addObject("hotReplyModelList", hotReplyModelList);
        //顶级回复，子回复
        List<V3xBbsArticleReply> replyAllList = bbsArticleManager.listReplyByArticleId(articleId);
        List<V3xBbsArticleReply> replyTopList = new ArrayList<V3xBbsArticleReply>();
        Map<Long, List<V3xBbsArticleReply>> top2child = new HashMap<Long, List<V3xBbsArticleReply>>();
        for (V3xBbsArticleReply reply : replyAllList) {
            try {
                // 此处是为了升级历史数据
                ReplaceBase64Result result = fileManager.replaceBase64Image(reply.getContent());
                if( result.isConvertBase64Img() ){// 替换过正文内容才执行更新
                    reply.setContent(result.getHtml());
                    DBAgent.update(reply);
                }
            } catch (Exception e) {// 查看时，如果转换失败就不转换了
                log.error("An exception occurred when the base64 encoding picture in the text was converted to a URL!",e);
            }

            if (reply.getUseReplyFlag() == 4) {
                replyTopList.add(reply);
            } else {
                Strings.addToMap(top2child, reply.getUseReplyId(), reply);
            }
        }
        int orderFlag = 0;
        try {
            orderFlag = board.getOrderFlag();
        } catch (Exception e) {
        }
        if (orderFlag == 1) {//回复排序
            Collections.reverse(replyTopList);
        }

        int nowPage = 1; // 当前页
        int size = replyTopList.size(); // 总条数(顶级回复)
        int pageSize = 50;
        int pages = 1; // 总页数
        int beginReply = 0; // 开始显示的记录

        pages = (size + pageSize - 1) / pageSize; // 总页数
        if (pages == 0) {
            pages = 1;
        }
        if (Strings.isNotBlank(nowPagePara) && !"1".equals(nowPagePara)) {
            nowPage = NumberUtils.toInt(nowPagePara, 1);
            beginReply = (nowPage - 1) * pageSize;
        } else if ("reply".equals(from)) {
            // 从回复进来的显示最后一页
            nowPage = pages;
            beginReply = (nowPage - 1) * pageSize;
        }

        Pagination.setFirstResult(beginReply);
        Pagination.setMaxResults(50);
        List<V3xBbsArticleReply> replyList = CommonTools.pagenate(replyTopList);
        List<ArticleReplyModel> replyModelList = this.bbsArticleManager.getReplyModelList(replyList, article, replyAttachementMap);
        if (Strings.isNotEmpty(replyModelList)) {
            for (ArticleReplyModel topReplyModel : replyModelList) {
                List<V3xBbsArticleReply> childReplyList = top2child.get(topReplyModel.getId());//子级
                List<ArticleReplyModel> childReplyModelList = this.bbsArticleManager.getReplyModelList(childReplyList, article, replyAttachementMap);
                topReplyModel.setChildList(childReplyModelList);
            }
        }

        mav.addObject("total", replyAllList.size());
        mav.addObject("size", replyTopList.size());
        mav.addObject("pageSize", pageSize);
        mav.addObject("pages", pages);//总页数
        mav.addObject("nowPage", nowPage);//当前页数
        mav.addObject("pageArea", (nowPage - 1) / 10);//当前页数在第几个（1-10）间
        mav.addObject("replyModelList", replyModelList);
        if (AppContext.hasPlugin("doc")) {
            String collectFlag = SystemProperties.getInstance().getProperty("doc.collectFlag");
            if ("true".equals(collectFlag)) {
                List<Map<String, Long>> collectMap = docApi.findFavorites(userId, CommonTools.newArrayList(article.getId()));
                if (!collectMap.isEmpty()) {
                    mav.addObject("isCollect", true);
                    mav.addObject("collectDocId", collectMap.get(0).get("id"));
                }
            }
            mav.addObject("docCollectFlag", article.getAnonymousFlag() ? "false" : collectFlag);
        }

        // 评论点赞标记
        String praise = article.getPraise();
        if (Strings.isBlank(praise)) {
            mav.addObject("bbsPraise", false);
        } else if (praise.contains(userId.toString())) {
            mav.addObject("bbsPraise", true);
        } else {
            mav.addObject("bbsPraise", false);
        }

        mav.addObject("memberId", userId);
        // 传入分版判断收藏标识
        // 协同加密
        if (!SecurityCheck.isLicit(AppContext.getRawRequest(), AppContext.getRawResponse(), ApplicationCategoryEnum.bbs, user, articleId, null, null)) {
            return null;
        }
        return mav;
    }

    private boolean inIssueArea(String issueArea) throws BusinessException {
        List<Long> areaIds = CommonTools.parseTypeAndIdStr2Ids(issueArea);
        List<Long> domainIds = orgManager.getAllUserDomainIDs(AppContext.currentUserId());
        return CollectionUtils.isNotEmpty(Strings.getIntersection(areaIds, domainIds));
    }

    /**
     * 预览
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ModelAndView bbsPreview(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("bbs/bbsInfo");
        String boardId = request.getParameter("preBoardId");
        String preTitle = request.getParameter("preTitle");
        String preContent = request.getParameter("preContent");
        String preScope = request.getParameter("preScope");
        String attaJSON = request.getParameter("preAttachment");
        V3xBbsArticle article = new V3xBbsArticle();
        V3xBbsBoard board = bbsBoardManager.getBoardById(Long.parseLong(boardId));
        article.setIdIfNew();

        Long userId = AppContext.currentUserId();
        String senderId = userId.toString();
        String senderImgUrl = Functions.getAvatarImageUrl(userId);

        article.setIssueUserId(userId);
        article.setDepartment(AppContext.getCurrentUser().getDepartmentId());
        article.setPost(AppContext.getCurrentUser().getPostId());
        article.setArticleName(preTitle);
        article.setIssueTime(new Timestamp(System.currentTimeMillis()));
        article.setContent(preContent);
        article.setState(BbsConstants.BBS_ARTICLE_DRAFT);
        article.setClickNumber(0);
        article.setPraiseSum(0);

        if (Strings.isNotEmpty(attaJSON)) {
            Object obj = JSONUtil.parseJSONString(attaJSON);
            List<Map> atts = new LinkedList();
            if (obj instanceof Map) {
                atts.add((Map) obj);
            } else {
                atts = (List<Map>) obj;
            }

            attachmentManager.deleteByReference(article.getId(), article.getId());
            attachmentManager.create(ApplicationCategoryEnum.bbs, article.getId(), article.getId(), atts);
            List<Attachment> attachments = attachmentManager.getByReference(article.getId());
            mav.addObject("attachments", attachments);
        }

        mav.addObject("preBoardId", boardId);
        mav.addObject("preTitle", preTitle);
        mav.addObject("preContent", preContent);
        mav.addObject("preScope", preScope);
        mav.addObject("issueArea", preScope);
        mav.addObject("memberId", senderId);
        mav.addObject("issuerImage", senderImgUrl);
        mav.addObject("article", article);
        mav.addObject("board", board);
        mav.addObject("pre", "true");

        mav.addObject("total", 0);
        mav.addObject("size", 0);
        mav.addObject("pageSize", 0);
        mav.addObject("pages", 0);//总页数
        mav.addObject("nowPage", 0);//当前页数
        mav.addObject("pageArea", 0);//当前页数在第几个（1-10）间
        return mav;
    }

    /**
     * 新讨论用正文编辑器
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView replyArticleNew(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("bbs/replyArticle");
        mav.addObject("isCollCube", request.getParameter("isCollCube"));
        Long articleId = NumberUtils.toLong(request.getParameter("articleId"));
        V3xBbsArticle article = this.getArticleFromCacheOrDB(articleId);

        if (article == null || article.getState() == BbsConstants.BBS_ARTICLE_ISNOT_ACTIVE) {
            super.rendJavaScript(response, "alert('" + ResourceUtil.getString("bbs.article.delorcanceled.js") + "');" + jsAction);
            return null;
        }
        mav.addObject("article", article);
        Long boardId = article.getBoardId();
        V3xBbsBoard board = this.bbsBoardManager.getBoardById(boardId);
        mav.addObject("board", board);
        int useReplyFlag = Integer.parseInt(request.getParameter("useReplyFlag"));
        if (Strings.isNotBlank(request.getParameter("postId"))) {
            Long postId = Long.valueOf(request.getParameter("postId"));
            if (useReplyFlag == BbsConstants.REPLY_TYPE.referReply.ordinal()) {
                mav.addObject("useReplyId", postId);
            }
        }
        return mav.addObject("useReplyFlag", useReplyFlag).addObject("replyUserName", this.getReplyUserNameWithDeptName());
    }

    /**
     * 创建新讨论-6.0新版
     */
    public ModelAndView createArticleNew(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("bbs/bbsCreateArticle");
        Long userId = AppContext.currentUserId();
        Long accountId = AppContext.currentAccountId();
        int spaceType = NumberUtils.toInt(request.getParameter("spaceType"));
        Long spaceId = NumberUtils.toLong(request.getParameter("spaceId"));
        mav.addObject("spaceType", spaceType);
        mav.addObject("spaceId", spaceId);
        String boardId = request.getParameter("boardId");
        String bType = request.getParameter("boardType");
        V3xBbsBoard bbsBoard = null;
        int boardType = 2;
        if (Strings.isNotBlank(boardId)) {
            bbsBoard = bbsBoardManager.getBoardById(Long.parseLong(boardId));
            boardType = bbsBoard.getAffiliateroomFlag();
        } else if (!Strings.isBlank(bType)) {
            boardType = Integer.parseInt(bType);
        }
        List<V3xBbsBoard> groupBoard = new ArrayList<V3xBbsBoard>();
        List<V3xBbsBoard> accountBoard = new ArrayList<V3xBbsBoard>();
        List<V3xBbsBoard> deptmentBoard = new ArrayList<V3xBbsBoard>();
        List<V3xBbsBoard> customBoard = new ArrayList<V3xBbsBoard>();
        List<V3xBbsBoard> projectBoard = new ArrayList<V3xBbsBoard>();
        if (spaceId != 0 && (spaceType == SpaceType.public_custom_group.ordinal() || spaceType == SpaceType.public_custom.ordinal() || spaceType == SpaceType.custom.ordinal())) {
            if (spaceType == SpaceType.public_custom_group.ordinal()) {
                groupBoard = bbsBoardManager.getCanIssueCustomBoard(userId, spaceId, spaceType);
                StringBuilder publisthScopeSpace = new StringBuilder();
                List<Object[]> issueAreas = portalApi.getSecuityOfSpace(spaceId);
                for (Object[] arr : issueAreas) {
                    publisthScopeSpace.append(StringUtils.join(arr, "|") + ",");
                }
                mav.addObject("entity", publisthScopeSpace.substring(0, publisthScopeSpace.length() - 1));
            } else if (spaceType == SpaceType.public_custom.ordinal()) {
                accountBoard = bbsBoardManager.getCanIssueCustomBoard(userId, spaceId, spaceType);
                StringBuilder publisthScopeSpace = new StringBuilder();
                List<Object[]> issueAreas = portalApi.getSecuityOfSpace(spaceId);
                for (Object[] arr : issueAreas) {
                    publisthScopeSpace.append(StringUtils.join(arr, "|") + ",");
                }
                mav.addObject("entity", publisthScopeSpace.substring(0, publisthScopeSpace.length() - 1));
            } else if (spaceType == SpaceType.custom.ordinal()) {// 自定义团队版块
                V3xBbsBoard custom = bbsBoardManager.getBoardById(spaceId);
                customBoard.add(custom);
                List<Object[]> entityObj = portalApi.getSecuityOfSpace(Long.valueOf(spaceId));
                String entity = "";
                for (Object[] obj : entityObj) {
                    entity += obj[0] + "|" + obj[1] + ",";
                }
                if (!"".equals(entity)) {
                    entity = entity.substring(0, entity.length() - 1);
                }
                mav.addObject("entity", entity);
            }
        } else if (spaceType == SpaceType.department.ordinal()) {
            V3xBbsBoard v3xBbsBoard = bbsBoardManager.getBoardById(spaceId);
            deptmentBoard.add(v3xBbsBoard);
            String entity = "";
            List<Object[]> issueAreas = portalApi.getSecuityOfDepartment(spaceId);
            for (Object[] arr : issueAreas) {
                entity += arr[0] + "|" + arr[1] + ",";
            }
            if (!"".equals(entity)) {
                entity = entity.substring(0, entity.length() - 1);
            }
            mav.addObject("entity", entity);
        } else if (spaceType == SpaceType.related_project.ordinal()) {// 项目讨论
            String entity = this.getProjectBbsPublishScope(Long.parseLong(boardId));
            projectBoard.add(bbsBoard);
            mav.addObject("entity", entity);
        } else {
            if ((Boolean) (SysFlag.sys_isGroupVer.getFlag())) {
                groupBoard = bbsBoardManager.getCanIssueGroupBoard(userId);
            }
            accountBoard = bbsBoardManager.getCanIssueCorporationBoard(userId, accountId);
        }
        Map<String, List<V3xBbsBoard>> boardMap = new HashMap<String, List<V3xBbsBoard>>();
        boardMap.put("group", groupBoard);
        boardMap.put("account", accountBoard);
        boardMap.put("dept", deptmentBoard);
        boardMap.put("custom", customBoard);
        boardMap.put("project", projectBoard);
        mav.addObject("boardMap", boardMap);
        mav.addObject("boardMapJson", JSONUtil.toJSONString(boardMap));
        mav.addObject("boardId", boardId);
        mav.addObject("boardType", boardType);
        return mav;
    }

    /**
     * 创建新讨论-6.0新版
     * @throws Exception
     */
    public ModelAndView createArticleEditor(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("bbs/bbsEditor");
        String idStr = request.getParameter("articleId");
        String content = request.getParameter("content");
        if (Strings.isNotBlank(idStr)) {
            Long articleId = Long.parseLong(idStr);
            V3xBbsArticle article = bbsArticleManager.getArticleById(articleId);
            if (article != null) {
                content = article.getContent();
                mav.addObject("article", article);
            }
        }
        mav.addObject("content", content);
        return mav;
    }

    /**
     * 进入修改讨论主题页面：只允许修改正文内容和附件和接收新回复通知三项内容 6.0新
     */
    public ModelAndView modifyArticleNew(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("bbs/bbsModifyArticle");
        mav.addObject("isCollCube", request.getParameter("isCollCube"));
        Long articleId = NumberUtils.toLong(request.getParameter("articleId"));
        V3xBbsArticle article = this.getArticleFromCacheOrDB(articleId);

        //帖子被删除
        if (article == null || article.getState() == BbsConstants.BBS_ARTICLE_ISNOT_ACTIVE) {
            super.rendJavaScript(response, "alert('" + ResourceUtil.getString("bbs.article.delorcanceled.js") + "');" + jsAction);
            return null;
        }
        if (Strings.isNotBlank(request.getParameter("group"))) {
            mav.addObject("group", "group");
        }

        Long boardId = article.getBoardId();
        V3xBbsBoard board = this.bbsBoardManager.getBoardById(boardId);
        User user = AppContext.getCurrentUser();
        if (board.getAffiliateroomFlag() == SpaceType.department.ordinal()) { //部门空间
            List<PortalSpaceFix> deptSpaceModels = new ArrayList<PortalSpaceFix>();
            List<PortalSpaceFix> spacePath = portalApi.getAccessSpaceFromCache(user.getId(), user.getLoginAccount());
            if (spacePath != null && spacePath.size() > 0) {
                for (PortalSpaceFix portalSpaceFix : spacePath) {
                    if (portalSpaceFix.getType().intValue() == SpaceType.department.ordinal()) {
                        deptSpaceModels.add(portalSpaceFix);
                    }
                }
            }
            mav.addObject("deptSpaceModels", deptSpaceModels);
            mav.addObject("deptSpaceModelsLength", deptSpaceModels.size());
            mav.addObject("DEPARTMENTAffiliateroomFlag", true);
            String issueArea = this.getIssueAreaOfArticle(article, board);
            mav.addObject("issueArea", issueArea);
        } else {
            List<Object[]> entityObj = portalApi.getSecuityOfSpace(boardId);
            String entity = "";
            for (Object[] obj : entityObj) {
                entity += obj[0] + "|" + obj[1] + ",";
            }
            if (!"".equals(entity)) {
                entity = entity.substring(0, entity.length() - 1);
            }
            mav.addObject("entity", entity);
            String issueArea = this.getIssueAreaOfArticle(article, board);
            mav.addObject("issueArea", issueArea);
        }

        Long userId = AppContext.currentUserId();
        Long accountId = AppContext.currentAccountId();
        int spaceType = NumberUtils.toInt(request.getParameter("spaceType"));
        Long spaceId = NumberUtils.toLong(request.getParameter("spaceId"));
        if (board != null) {
            spaceType = board.getAffiliateroomFlag();
        }
        mav.addObject("spaceType", spaceType);
        mav.addObject("spaceId", spaceId);
        List<V3xBbsBoard> groupBoard = new ArrayList<V3xBbsBoard>();
        List<V3xBbsBoard> accountBoard = new ArrayList<V3xBbsBoard>();
        List<V3xBbsBoard> deptmentBoard = new ArrayList<V3xBbsBoard>();
        List<V3xBbsBoard> customBoard = new ArrayList<V3xBbsBoard>();
        List<V3xBbsBoard> projectBoard = new ArrayList<V3xBbsBoard>();
        if (spaceId != 0 && (spaceType == SpaceType.public_custom_group.ordinal() || spaceType == SpaceType.public_custom.ordinal() || spaceType == SpaceType.custom.ordinal())) {
            if (spaceType == SpaceType.public_custom_group.ordinal()) {
                groupBoard = bbsBoardManager.getCanIssueCustomBoard(userId, spaceId, spaceType);
                StringBuilder publisthScopeSpace = new StringBuilder();
                List<Object[]> issueAreas = portalApi.getSecuityOfSpace(spaceId);
                for (Object[] arr : issueAreas) {
                    publisthScopeSpace.append(StringUtils.join(arr, "|") + ",");
                }
                mav.addObject("entity", publisthScopeSpace.substring(0, publisthScopeSpace.length() - 1));
            } else if (spaceType == SpaceType.public_custom.ordinal()) {
                accountBoard = bbsBoardManager.getCanIssueCustomBoard(userId, spaceId, spaceType);
                StringBuilder publisthScopeSpace = new StringBuilder();
                List<Object[]> issueAreas = portalApi.getSecuityOfSpace(spaceId);
                for (Object[] arr : issueAreas) {
                    publisthScopeSpace.append(StringUtils.join(arr, "|") + ",");
                }
                mav.addObject("entity", publisthScopeSpace.substring(0, publisthScopeSpace.length() - 1));
            } else if (spaceType == SpaceType.custom.ordinal()) {// 自定义团队版块
                V3xBbsBoard custom = bbsBoardManager.getBoardById(spaceId);
                customBoard.add(custom);
                List<Object[]> entityObj = portalApi.getSecuityOfSpace(Long.valueOf(spaceId));
                String entity = "";
                for (Object[] obj : entityObj) {
                    entity += obj[0] + "|" + obj[1] + ",";
                }
                if (!"".equals(entity)) {
                    entity = entity.substring(0, entity.length() - 1);
                }
                mav.addObject("entity", entity);
            }
        } else if (spaceType == SpaceType.department.ordinal()) {
            //V3xBbsBoard v3xBbsBoard = bbsBoardManager.getBoardById(boardId);
            deptmentBoard.add(board);
            String entity = "";
            List<Object[]> issueAreas = portalApi.getSecuityOfDepartment(boardId);
            for (Object[] arr : issueAreas) {
                entity += arr[0] + "|" + arr[1] + ",";
            }
            if (!"".equals(entity)) {
                entity = entity.substring(0, entity.length() - 1);
            }
            mav.addObject("entity", entity);
        } else if (spaceType == SpaceType.related_project.ordinal()) {// 项目讨论
            projectBoard.add(board);
        } else {
            groupBoard = bbsBoardManager.getCanIssueGroupBoard(userId);
            accountBoard = bbsBoardManager.getCanIssueCorporationBoard(userId, accountId);
        }
        Map<String, List<V3xBbsBoard>> boardMap = new HashMap<String, List<V3xBbsBoard>>();
        boardMap.put("group", groupBoard);
        boardMap.put("account", accountBoard);
        boardMap.put("dept", deptmentBoard);
        boardMap.put("custom", customBoard);
        boardMap.put("project", projectBoard);
        mav.addObject("boardMap", boardMap);
        mav.addObject("boardMapJson", JSONUtil.toJSONString(boardMap));
        mav.addObject("board", board);
        mav.addObject("boardId", boardId);
        mav.addObject("boardType", board.getAffiliateroomFlag());
        mav.addObject("article", article);
        mav.addObject("attachments", attachmentManager.getByReference(articleId, articleId));
        mav.addObject("attListJSON", attachmentManager.getAttListJSON(attachmentManager.getByReference(articleId, articleId)));
        return mav;
    }

    public ModelAndView modifyBoardNew(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        V3xBbsBoard board = null;
        if (Strings.isNotBlank(request.getParameter("id"))) {
            board = bbsBoardManager.getBoardById(Long.parseLong(request.getParameter("id")));
            // 快速需求(Fast Demand):增加板块停用状态,这里的增加判断是否是正常状态的板块
            if (board.getFlag() != BbsConstants.FLAG_NORMAL) {
                super.rendJavaScript(response, "alert('" + ResourceUtil.getString("bbs.article.disable2.js") + "');parent.location.href=parent.location.href;");
                return null;
            }
        }
        //置顶数
        Integer newTopNum = Integer.parseInt(request.getParameter("topNumber"));
        if(board.getTopNumber()!=null&&!board.getTopNumber().equals(newTopNum)){
            bbsArticleManager.updateTopNumberOrder(board.getTopNumber().toString(),newTopNum.toString(),board.getId());
        }
        board.setTopNumber(newTopNum);
        board.setOrderFlag(Integer.parseInt(request.getParameter("orderFlag")));
        //是否允许匿名发帖
        if (Strings.isNotBlank(request.getParameter("anonymousFlag")))
            board.setAnonymousFlag(Byte.parseByte(request.getParameter("anonymousFlag")));

        //是否允许匿名回复
        if (Strings.isNotBlank(request.getParameter("anonymousReplyFlag")))
            board.setAnonymousReplyFlag(Byte.parseByte(request.getParameter("anonymousReplyFlag")));

        //版块封面
        String imageId = request.getParameter("imageId");
        if (StringUtils.isNotBlank(imageId)) {
            board.setImageId(Long.parseLong(imageId));
        }

        //版块授权人
        Integer authType = Integer.valueOf(request.getParameter("authType"));
        Integer oldAuthType = board.getAuthType();
        if (authType == BbsConstants.BBS_BOARD_AUTHTYPE_ALL) {//选择全部
            if (!authType.equals(oldAuthType)) {
                this.bbsBoardManager.authGeneric(board.getId(), new ArrayList<Long>(), "");
            }
        } else {
            String oldAuthIssueIds = board.getAuthInfo(BbsConstants.AUTH_TO_POST);
            String authIssueIds = request.getParameter("authIssueIds");
            if (!oldAuthIssueIds.equals(authIssueIds)) {
                //授权的ID集合
                List<Long> auth = new ArrayList<Long>();
                //构造消息接收者
                Set<V3xOrgMember> membersSet = orgManager.getMembersByTypeAndIds(authIssueIds);
                for (V3xOrgMember entity : membersSet) {
                    if (!user.getId().equals(entity.getId())) {
                        auth.add(entity.getId());
                    }
                }
                Collection<MessageReceiver> receiversIssue = MessageReceiver.get(board.getId(), auth);

                this.bbsBoardManager.authGeneric(board.getId(), auth, authIssueIds);

                //发送消息
                try {
                    userMessageManager.sendSystemMessage(MessageContent.get("bbs.auth", board.getName(), user.getName()), ApplicationCategoryEnum.bbs, AppContext.getCurrentUser().getId(), receiversIssue, board.getId());
                } catch (Exception e) {
                    logger.error("", e);
                }
                appLogManager.insertLog(user, AppLogAction.Bbs_PostAuth_Update, user.getName(), board.getName());
            }
        }
        board.setAuthType(authType);
        board.setFlag(BbsConstants.FLAG_NORMAL);

        //禁止回复人
        String authReplyIds = request.getParameter("authReplyIds");
        String oldAuthReplyIds = board.getAuthInfo(BbsConstants.FORBIDDEN_TO_REPLY);
        if (!authReplyIds.equals(oldAuthReplyIds)) {
            //禁止人员集合
            List<Long> banReplyIds = new ArrayList<Long>();
            //构造消息接收者
            Set<V3xOrgMember> membersSet = orgManager.getMembersByTypeAndIds(authReplyIds);
            for (V3xOrgMember entity : membersSet) {
                if (!user.getId().equals(entity.getId())) {
                    banReplyIds.add(entity.getId());
                }
            }
            Collection<MessageReceiver> receiversReply = MessageReceiver.get(board.getId(), banReplyIds);

            this.bbsBoardManager.authNoReply(board.getId(), banReplyIds, authReplyIds);

            //发送消息
            try {
                userMessageManager.sendSystemMessage(MessageContent.get("bbs.no.reply", board.getName(), AppContext.getCurrentUser().getName()), ApplicationCategoryEnum.bbs, AppContext.getCurrentUser().getId(), receiversReply, board.getId());
            } catch (Exception e) {
                logger.error("send message failed", e);
            }
            appLogManager.insertLog(user, AppLogAction.Bbs_PostAuth_Update, user.getName(), board.getName());
        }

        this.bbsBoardManager.updateV3xBbsBoard(board, board.getAdmins());

        return super.refreshWindow("parent");
    }

    /**
     * 我发起的-- 1
     * 我回复的---2
     * 我收藏的---3
     * @throws Exception
     */
    public ModelAndView myArticles(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("bbs/bbsMyArticles");
        User user = AppContext.getCurrentUser();
        int pageSize = NumberUtils.toInt(request.getParameter("pageSize"), 20);
        int pageNo = NumberUtils.toInt(request.getParameter("pageNo"), 1);
        //页面类型
        String type = request.getParameter("type");
        int spaceType = NumberUtils.toInt(request.getParameter("spaceType"));
        Long spaceId = NumberUtils.toLong(request.getParameter("spaceId"));
        //获取我可以查看的版块
        List<Long> adminBoardIds = null;
        if (spaceType == SpaceType.group.ordinal() || spaceType == SpaceType.corporation.ordinal()) {
            adminBoardIds = bbsArticleManager.getMyBoardIds(0, spaceId);
        } else {
            adminBoardIds = bbsArticleManager.getMyBoardIds(spaceType, spaceId);
        }
        // 热门板块
        List<V3xBbsBoard> coverBbsBoardList = bbsArticleManager.getBoardCover(adminBoardIds);
        List<BoardModel> coverBoardModelList = new ArrayList<BoardModel>();
        this.getBoardModelList(coverBbsBoardList, coverBoardModelList, user.getId());
        mav.addObject("coverBoardModelList", coverBoardModelList);
        //我发起的页面
        Map<String, Object> result = new HashMap<String, Object>();
        if ("2".equals(type)) {
            result = bbsArticleManager.bbsMyReplyArticle(pageSize, pageNo, user.getId(), spaceType, spaceId);
            mav.addObject("myReplyList", result.get("list"));
        }
        // 判断当前用户是否有发帖权限
        boolean hasIssue = false;
        List<Long> domainIds = orgManager.getAllUserDomainIDs(user.getId());
        for (Long id : adminBoardIds) {
            if (!hasIssue) {
                boolean isAdmin = bbsBoardManager.validUserIsAdmin(id, user.getId());
                boolean hasAuthIssue = bbsBoardManager.validIssueAuth(id, user.getId(), domainIds);
                hasIssue = isAdmin || hasAuthIssue;
            }
        }
        mav.addObject("hasIssue", hasIssue);
        mav.addObject("pages", result.get("pages"));
        mav.addObject("pageNo", result.get("pageNo"));
        mav.addObject("currentUserName", user.getName());
        mav.addObject("myType", type);
        // 我发起的，我回复的，我收藏的
        mav.addObject("articleNum", bbsArticleManager.getIssueCount(user.getId(), adminBoardIds));
        mav.addObject("replyNum", bbsArticleManager.getReplyCount(user.getId(), adminBoardIds));
        if (AppContext.hasPlugin("doc")) {
            mav.addObject("collectNum", bbsArticleManager.getCollectCount(user.getId(), adminBoardIds));
        }
        return mav;
    }

    /**
     * 讨论创建/修改---6.0新版
     */
    public ModelAndView bbsEdit(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("bbs/bbsEdit");
        Long userId = AppContext.currentUserId();
        Long accountId = AppContext.currentAccountId();
        int spaceType = NumberUtils.toInt(request.getParameter("spaceType"));
        Long spaceId = NumberUtils.toLong(request.getParameter("spaceId"));

        String articleId = request.getParameter("articleId");
        String boardId = request.getParameter("boardId");
        V3xBbsArticle article = new V3xBbsArticle();
        V3xBbsBoard board = null;
        int boardType = 2;
        if (Strings.isNotBlank(articleId)) {
            article = this.getArticleFromCacheOrDB(Long.parseLong(articleId));
            //帖子被删除
            if (article == null || article.getState() == BbsConstants.BBS_ARTICLE_ISNOT_ACTIVE
                    || article.getState() == BbsConstants.BBS_ARTICLE_PIGEONHOLE) {
                super.rendJavaScript(response, "alert('" + ResourceUtil.getString("bbs.article.delorcanceled.js") + "');" + jsAction);
                return null;
            }
            board = bbsBoardManager.getBoardById(article.getBoardId());
            boardId = board.getId().toString();
            String issueArea = this.getIssueAreaOfArticle(article, board);
            mav.addObject("issueArea", issueArea);
            //附件
            mav.addObject("attListJSON", attachmentManager.getAttListJSON(article.getId(), article.getId()));
        }
        if (Strings.isNotBlank(boardId)) {
            board = bbsBoardManager.getBoardById(Long.parseLong(boardId));
            // 快速需求(Fast Demand):增加板块停用状态,这里的增加判断是否是正常状态的板块
            if (board.getFlag() != BbsConstants.FLAG_NORMAL) {
                super.rendJavaScript(response, "alert('" + ResourceUtil.getString("bbs.article.disable.js") + "');" + jsAction);
                return null;
            }
            boardType = board.getAffiliateroomFlag();
            spaceType = board.getAffiliateroomFlag();
            if (spaceId == 0) {
                spaceId = board.getAccountId();
            }
        }
        mav.addObject("spaceType", spaceType);
        mav.addObject("spaceId", spaceId);
        List<V3xBbsBoard> groupBoard = new ArrayList<V3xBbsBoard>();
        List<V3xBbsBoard> accountBoard = new ArrayList<V3xBbsBoard>();
        List<V3xBbsBoard> deptmentBoard = new ArrayList<V3xBbsBoard>();
        List<V3xBbsBoard> customBoard = new ArrayList<V3xBbsBoard>();
        List<V3xBbsBoard> projectBoard = new ArrayList<V3xBbsBoard>();
        if (spaceId != 0 && (spaceType == SpaceType.public_custom_group.ordinal() || spaceType == SpaceType.public_custom.ordinal() || spaceType == SpaceType.custom.ordinal())) {
            if (spaceType == SpaceType.public_custom_group.ordinal()) {
                groupBoard = bbsBoardManager.getCanIssueCustomBoard(userId, spaceId, spaceType);
                StringBuilder publisthScopeSpace = new StringBuilder();
                List<Object[]> issueAreas = portalApi.getSecuityOfSpace(spaceId);
                for (Object[] arr : issueAreas) {
                    publisthScopeSpace.append(StringUtils.join(arr, "|") + ",");
                }
                mav.addObject("entity", publisthScopeSpace.substring(0, publisthScopeSpace.length() - 1));
            } else if (spaceType == SpaceType.public_custom.ordinal()) {
                accountBoard = bbsBoardManager.getCanIssueCustomBoard(userId, spaceId, spaceType);
                StringBuilder publisthScopeSpace = new StringBuilder();
                List<Object[]> issueAreas = portalApi.getSecuityOfSpace(spaceId);
                for (Object[] arr : issueAreas) {
                    publisthScopeSpace.append(StringUtils.join(arr, "|") + ",");
                }
                mav.addObject("entity", publisthScopeSpace.substring(0, publisthScopeSpace.length() - 1));
            } else if (spaceType == SpaceType.custom.ordinal()) {// 自定义团队版块
                V3xBbsBoard custom = bbsBoardManager.getBoardById(spaceId);
                customBoard.add(custom);
                List<Object[]> entityObj = portalApi.getSecuityOfSpace(Long.valueOf(spaceId));
                String entity = "";
                for (Object[] obj : entityObj) {
                    entity += obj[0] + "|" + obj[1] + ",";
                }
                if (!"".equals(entity)) {
                    entity = entity.substring(0, entity.length() - 1);
                }
                mav.addObject("entity", entity);
            }
        } else if (spaceType == SpaceType.department.ordinal()) {
            V3xBbsBoard v3xBbsBoard = bbsBoardManager.getBoardById(spaceId);
            deptmentBoard.add(v3xBbsBoard);
            String entity = "";
            List<Object[]> issueAreas = portalApi.getSecuityOfDepartment(spaceId);
            for (Object[] arr : issueAreas) {
                entity += arr[0] + "|" + arr[1] + ",";
            }
            if (!"".equals(entity)) {
                entity = entity.substring(0, entity.length() - 1);
            }
            mav.addObject("entity", entity);
        } else if (spaceType == SpaceType.related_project.ordinal()) {// 项目讨论
            String entity = this.getProjectBbsPublishScope(Long.parseLong(boardId));
            projectBoard.add(board);
            mav.addObject("entity", entity);
        } else {
            if ((Boolean) (SysFlag.sys_isGroupVer.getFlag())) {
                groupBoard = bbsBoardManager.getCanIssueGroupBoard(userId);
            }
            if(AppContext.getCurrentUser().isVJoinMember()){
                accountBoard = bbsBoardManager.getCanIssueCorporationBoard(userId, OrgHelper.getVJoinAllowAccount());
            }else{
                accountBoard = bbsBoardManager.getCanIssueCorporationBoard(userId, accountId);
            }
        }
        Map<String, List<V3xBbsBoard>> boardMap = new HashMap<String, List<V3xBbsBoard>>();
        boardMap.put("group", groupBoard);
        boardMap.put("account", accountBoard);
        boardMap.put("dept", deptmentBoard);
        boardMap.put("custom", customBoard);
        boardMap.put("project", projectBoard);

        Long vJoinAllowAccount = OrgHelper.getVJoinAllowAccount();
        boolean showVjoin = false;
        showVjoin =vJoinAllowAccount != null && vJoinAllowAccount == AppContext.currentAccountId();
        mav.addObject("showVjoin", showVjoin);
        mav.addObject("isVjoinMember", AppContext.getCurrentUser().isVJoinMember());
        mav.addObject("boardMap", boardMap);
        mav.addObject("boardMapJson", JSONUtil.toJSONString(boardMap));
        mav.addObject("boardType", boardType);
        mav.addObject("boardName", board!=null?board.getName():"");
        mav.addObject("article", article);
        return mav;
    }

    public void setPortalApi(PortalApi portalApi) {
        this.portalApi = portalApi;
    }

	public void setBbsReadManager(BbsReadManager bbsReadManager) {
		this.bbsReadManager = bbsReadManager;
	}
}
