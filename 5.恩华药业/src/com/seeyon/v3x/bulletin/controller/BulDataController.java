package com.seeyon.v3x.bulletin.controller;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.seeyon.v3x.bulletin.domain.*;
import com.seeyon.v3x.bulletin.manager.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.agent.utils.AgentUtil;
import com.seeyon.apps.bulletin.event.BulletinAddEvent;
import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.index.api.IndexApi;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.cache.etag.ETagCacheManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyService;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.excel.DataCell;
import com.seeyon.ctp.common.excel.DataRecord;
import com.seeyon.ctp.common.excel.DataRow;
import com.seeyon.ctp.common.excel.FileToExcelManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.domain.ReplaceBase64Result;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.office.trans.util.OfficeTransHelper;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.security.SecurityHelper;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.common.web.util.WebUtil;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.OrgConstants.RelationshipObjectiveName;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.MemberPost;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgRelationship;
import com.seeyon.ctp.organization.bo.V3xOrgTeam;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.portal.api.PortalApi;
import com.seeyon.ctp.portal.po.PortalSpaceFix;
import com.seeyon.ctp.portal.util.Constants.SecurityType;
import com.seeyon.ctp.portal.util.Constants.SpaceType;
import com.seeyon.ctp.util.CommonTools;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.UniqueList;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.bulletin.util.BulConstants;
import com.seeyon.v3x.bulletin.util.BulDataLock;
import com.seeyon.v3x.bulletin.util.BulDataLockAction;
import com.seeyon.v3x.bulletin.util.BulReadCount;
import com.seeyon.v3x.bulletin.util.BulletinUtils;
import com.seeyon.v3x.bulletin.vo.BulTypeModel;
import com.seeyon.v3x.common.security.AccessControlBean;
import com.seeyon.v3x.common.security.SecurityCheck;
import com.seeyon.v3x.contentTemplate.domain.ContentTemplate;
import com.seeyon.v3x.contentTemplate.manager.ContentTemplateManager;

/**
 * 公告模块最重要的Controller，包括了普通用户、公告发起者、公告审核员、公告管理员的各种操作，全部方法的编排遵循以下的顺序：<br>
 * <b>1.普通用户：</b><br>
 * 1.1访问单位公告首页、集团公告首页、单位最新公告更多页面、集团最新公告更多页面、部门公告更多页面、某一特定板块公告更多页面；<br>
 * 1.2在以上各种页面按照公告发起者、公告标题及公告发布日期进行查询；<br>
 * 1.3用户点击已经发布的公告进行阅读（满足条件时也可查看该公告的阅读情况）。<br>
 * <b>2.公告发起者：</b><br>
 * 2.1点击"发布公告"按钮进入查看自己发起的全部公告；<br>
 * 2.2新建公告、编辑公告、保存公告、发布审核通过的公告、删除公告（已发布或未发布的、真实删除）。<br>
 * <b>3.公告审核员：</b><br>
 * 3.1在单位空间或集团空间中点击公共信息管理，进入其要审核的公告列表页面；<br>
 * 3.2查看待审核的公告详细信息、进行审核操作（直接发布、审核通过、审核不通过）、将审核通过的公告取消审核<br>
 * <b>4.公告管理员：</b><br>
 * 4.1点击"板块管理"按钮进入查看该板块下已发布的全部公告；<br>
 * 4.2对已发布的公告进行：置顶、取消发布、删除（逻辑删除）、归档、授权（发起公告权限）、统计（根据阅读次数、发起者、发起月份和状态（发布或已归档）进行统计）<br>。
 * @author wolf -- Edited by Rookie Young from 2009-04-08 on
 */
public class BulDataController extends BaseController {
	private static String 		   splitFlg = "&";
    private BulDataManager               bulDataManager;
    private AttachmentManager            attachmentManager;
    private OrgManager                   orgManager;
    private IndexApi                     indexApi;
    private BulReadManager               bulReadManager;
    private AffairManager                affairManager;
    private AppLogManager                appLogManager;
    private DocApi docApi;
    private PortalApi portalApi;                                    //部门空间的访问者
    private ContentTemplateManager       contentTemplateManager;                          //公告格式，可以由单位管理员或集团管理员制定
    private UserMessageManager           userMessageManager;
    private BulTypeManager               bulTypeManager;
    private BulletinUtils 				 bulletinUtils;
	private static final Log             log = LogFactory.getLog(BulDataController.class);
    private CollaborationApi                   collaborationApi;
    private MainbodyManager              ctpMainbodyManager;
    private BulIssueManager              bulIssueManager;
    private FileToExcelManager           fileToExcelManager;
    private ETagCacheManager             eTagCacheManager;
    private FileManager                  fileManager;

    public void seteTagCacheManager(ETagCacheManager eTagCacheManager) {
        this.eTagCacheManager = eTagCacheManager;
    }

    private String convertContent(String content) throws BusinessException {
        if (content == null) {
            return content;
        }
        int xslStart = content.indexOf("&&&&&&&  xsl_start  &&&&&&&&");
        int dataStart = content.indexOf("&&&&&&&&  data_start  &&&&&&&&");
        int inputStart = content.indexOf("&&&&&&&&  input_start  &&&&&&&&");
        if (xslStart == -1 || dataStart == -1 || inputStart == -1) {
            return null;
        }

        String data = content.substring(dataStart + 30, inputStart);

        String recordid = null;
        Pattern pRecordid = Pattern.compile("recordid=\\\\\"([-]{0,1}\\d+?)\\\\\"");
        Matcher matcherRecordid = pRecordid.matcher(data);
        if (matcherRecordid.find()) {
            recordid = matcherRecordid.group(1);
        }

        if (recordid != null) {
            ColSummary summary = collaborationApi.getColSummaryByFormRecordId(Long.parseLong(recordid));
            List<CtpContentAll> contentList = ctpMainbodyManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, summary.getId());
            if (Strings.isNotEmpty(contentList)) {
                CtpContentAll body = contentList.get(0);
                String htmlContent = MainbodyService.getInstance().getContentHTML(body.getModuleType(), body.getModuleId());
                //替换表单正文中的附件、关联文档（新框架和老框架不兼容）
                htmlContent = bulIssueManager.replaceFileHtml("fileupload", htmlContent);
                htmlContent = bulIssueManager.replaceFileHtml("assdoc", htmlContent);
                return htmlContent;
            }
        }

        return null;
    }

    private String convertContentV(String content) throws BusinessException {
        if (content == null) {
            return content;
        }
        StringBuffer sb = new StringBuffer();
        Pattern pDiv = Pattern.compile("method=download&fileId=(.+?)&v=fromForm&createDate=(.+?)&filename=(.+?)\"");
        Matcher matcherDiv = pDiv.matcher(content);
        while (matcherDiv.find()) {
            try {
                String fileId = matcherDiv.group(1);
                String createDate = matcherDiv.group(2);
                String fileName = matcherDiv.group(3);
                String replace = "method=download&fileId=" + fileId + "&v=" + SecurityHelper.digest(fileId)
                        + "&createDate=" + createDate + "&filename=" + URLEncoder.encode(fileName, "UTF-8") + "\"";
                matcherDiv.appendReplacement(sb, replace.toString());
            } catch (UnsupportedEncodingException e) {
            }
        }
        matcherDiv.appendTail(sb);
        return convertFormFileUrl(sb.toString());
    }

    /** 显示阅读信息的列表，与查看公告基本信息分离开来，避免因为阅读信息过多导致公告查看产生性能问题  */
    public ModelAndView showReadList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("bulletin/user/data_read_view");
        Long dataId = NumberUtils.toLong(request.getParameter("id"));
        BulData bean = bulDataManager.getBulDataCache().getDataCache().get(dataId);
        if (bean == null) {
            bean = bulDataManager.getById(dataId);
        }
        this.setBulDataReadInfo(bean, mav, null);
        return mav;
    }

    private final Object readCountLock = new Object();

    /**
     * 记录阅读公告信息
     */
    public void recordBulRead(long dataId, BulData bean, boolean hasCache, User user) {
        this.bulReadManager.setReadState(bean, user.getId());
        if (hasCache) {
        	bulDataManager.clickCache(dataId, AppContext.getCurrentUser().getId());
        } else {
            BulBody body = bulDataManager.getBody(bean.getId());
            bean.setContent(body.getContent());
            bean.setContentName(body.getContentName());

            // 增加阅读次数
            int readCount = 0;
            synchronized (readCountLock) {
                readCount = bean.getReadCount() == null ? 0 : bean.getReadCount().intValue();
                bean.setReadCount(readCount + 1);
            }
            // 保存到缓存
            bulDataManager.syncCache(bean, readCount + 1);
        }
    }


    public boolean check(Long spaceId, Long auditId) throws BusinessException {
        boolean flag = true;
        List<Object[]> entityObj = portalApi.getSecuityOfSpace(spaceId);
        if(CollectionUtils.isEmpty(entityObj)){
            entityObj=portalApi.getSecuityOfDepartment(spaceId);
        }
        Set<Long> entityIds = new HashSet<Long>(); //取当前空间使用范围内的所有人员
        String scopeStr = "";
        for (Object[] objects : entityObj) {
            scopeStr += objects[0] + "|" + objects[1] + ",";
        }
        if (Strings.isNotBlank(scopeStr)) {
            scopeStr = scopeStr.substring(0, scopeStr.length() - 1);
        }
        Set<V3xOrgMember> members = orgManager.getMembersByTypeAndIds(scopeStr);
        for (V3xOrgMember org : members) {
            entityIds.add(org.getId());
        }
        if (entityIds.contains(auditId)) {
            flag = false;
        }
        return flag;
    }

    /**
     * 发布公告时，发送消息对象中加入当前公告板块的管理员
     * @param receivers   发布范围内的消息接受对象
     * @param bulData	  所发布的公告
     */
    private void addAdmins2MsgReceivers(Collection<Long> receivers, BulData bulData) {
        BulType bulType = this.bulTypeManager.getById(bulData.getTypeId());
        this.addAdmins2MsgReceivers(receivers, bulType);
    }

    /**
     * 发布公告时，发送消息对象中加入当前公告板块的管理员
     * @param receivers   发布范围内的消息接受对象
     * @param bulType	  所发布的公告所在的公告板块
     */
    private void addAdmins2MsgReceivers(Collection<Long> receivers, BulType bulType) {
        String managerIds = bulType.getManagerUserIds();
        if (Strings.isNotBlank(managerIds)) {
            String[] ids = managerIds.split(",");
            for (String id : ids) {
                Long addId = Long.parseLong(id);
                if (receivers != null && !receivers.contains(addId)) {
                    receivers.add(addId);
                }
            }
        }
    }

    /**
     * 辅助方法：获取公告发布范围内的全部人员ID集合
     */
    private Set<Long> getAllMembersinPublishScope(BulData bean, Boolean showVjoinMember) throws BusinessException {
        String publishScope = bean.getPublishScope();

        Set<V3xOrgMember> membersInScope = this.orgManager.getMembersByTypeAndIds(publishScope);
        Set<Long> memberIdsInScope = new HashSet<Long>();
        if (membersInScope != null && membersInScope.size() > 0) {
            for (V3xOrgMember member : membersInScope) {
                if ((!member.isVJoinExternal() || showVjoinMember)) {
                    memberIdsInScope.add(member.getId());
                }
            }
        }

        Long loginAccountId = AppContext.getCurrentUser().getLoginAccount();
        //处理跨单位兼职情况
        String[][] bulAuditIds = Strings.getSelectPeopleElements(publishScope);
        for (String[] typeAndId : bulAuditIds) {
            if (typeAndId[0].equals(V3xOrgEntity.ORGENT_TYPE_TEAM)) {//发布范围为组时,单位公告不可以给组中外单位人员发送消息
                V3xOrgTeam team = orgManager.getTeamById(Long.valueOf(typeAndId[1]));
                // 剔除非本单位人员
                boolean needFilterOthers = team != null && (bean.getType().getSpaceType() == SpaceType.department.ordinal() && bean.getType().getSpaceType() == SpaceType.corporation.ordinal());
                if (needFilterOthers) {
                    List<V3xOrgMember> teamMember = orgManager.getMembersByTeam(team.getId());
                    for (V3xOrgMember member : teamMember) {
                        if (!member.getOrgAccountId().equals(loginAccountId) && orgManager.getConcurentPostsByMemberId(loginAccountId, member.getId()).isEmpty()) {
                            memberIdsInScope.remove(member.getId());
                        }
                    }
                }
            } else if (typeAndId[0].equals(V3xOrgEntity.ORGENT_TYPE_DEPARTMENT)) {
                List<V3xOrgMember> listMember = orgManager.getMembersByDepartment(Long.valueOf(typeAndId[1]), false);
                for (V3xOrgMember member : listMember) {
                    if (!memberIdsInScope.contains(member.getId()) && (!member.isVJoinExternal() || showVjoinMember)) {
                        memberIdsInScope.add(member.getId());
                    }
                }
            } else if (typeAndId[0].equals(V3xOrgEntity.ORGENT_TYPE_ACCOUNT)) {//仅当发送范围为单位时,才将兼职人员悉数加入,在发布范围为其他类型时,兼职人员已包含在范围内
                Map<Long, List<V3xOrgMember>> accJian = orgManager.getConcurentPostByAccount(Long.valueOf(typeAndId[1]));
                Set<Entry<Long, List<V3xOrgMember>>> accSet = accJian.entrySet();
                for (Iterator<Entry<Long, List<V3xOrgMember>>> iter = accSet.iterator(); iter.hasNext();) {
                    Map.Entry<Long, List<V3xOrgMember>> ele = (Entry<Long, List<V3xOrgMember>>) iter.next();
                    for (Iterator<V3xOrgMember> iterator = ele.getValue().iterator(); iterator.hasNext();) {
                        V3xOrgMember mem = (V3xOrgMember) iterator.next();
                        if (!memberIdsInScope.contains(mem.getId())) {
                            memberIdsInScope.add(mem.getId());
                        }
                    }
                }
            }
        }
        return memberIdsInScope;
    }

    /**
     *	 辅助方法：获取公告的阅读信息并加入到ModelAndView中进行下一步的数据传输，用于查看公告和查看公告阅读情况时使用
     * @param bean 发布的公告
     * @param deptId 只显示一个部门的已阅未读情况时，该值不为空，否则为空
     * @see #userView   查看公告时，发起者或管理员查看阅读信息时的展现：部门   已阅总数   未阅总数
     * @see #bulReadIframe  在查看公告页面点击某一部门查看该部门的阅读详细情况：已阅人群（总数）|未阅人群（总数）；每一条阅读信息：（部门、人员姓名、阅读时间）
     */
    private void setBulDataReadInfo(BulData bean, ModelAndView mav, String deptId) throws Exception {
        List<BulRead> readList = this.bulDataManager.getReadListByData(bean.getId());
        if (readList != null) {

        	//当前登录单位可见的单位
        	List<V3xOrgAccount> accAccounts = orgManager.accessableAccountsByUnitId(AppContext.currentAccountId());
        	Set<Long> accessAccountIds = new HashSet<Long>();//当前单位可见的单位id
        	for(V3xOrgAccount account : accAccounts) {
        		if(!account.getIsGroup()) {
        			accessAccountIds.add(account.getId());
        		}
        	}

        	//发布范围部门下有哪些人
        	Map<Long,Set<Long>> deptMembers  = new HashMap<Long, Set<Long>>();

        	//发布范围内的部门
        	Set<Long> deptIds = new HashSet<Long>();
        	String publishScope = bean.getPublishScope();//公告发布范围
        	String[] publishScopeArr = publishScope.split(",");
        	for(String arr : publishScopeArr) {
        		String[] entity = arr.split("\\|");
        		String type = entity[0];
        		String id = entity[1];
        		if(OrgConstants.ORGENT_TYPE.Department.name().equals(type)) {
        			Long departmentId = Long.valueOf(id);
        			deptIds.add(departmentId);
        			List<V3xOrgDepartment> childDepts = orgManager.getChildDepartments(departmentId, false);
        			for(V3xOrgDepartment dept : childDepts) {
        				deptIds.add(dept.getId());
        			}
        		}else if(OrgConstants.ORGENT_TYPE.Account.name().equals(type)){//发布范围里含有单位(集团)
        			if(OrgConstants.GROUPID.toString().equals(id)) {// 发布范围里含有集团
        				List<V3xOrgAccount> accounts = orgManager.getAllAccounts();
        				for(V3xOrgAccount account : accounts) {
        					List<V3xOrgDepartment> departments = orgManager.getAllDepartments(account.getId());
        					for(V3xOrgDepartment department : departments) {
        						if(department.getIsInternal()) {
        							deptIds.add(department.getId());
        						}
        					}
        				}
        				break;
        			} else {// 发布范围里含有单位
        				List<V3xOrgDepartment> depts = orgManager.getAllDepartments(Long.valueOf(id));
            			for(V3xOrgDepartment dept : depts) {
            				if(dept.getIsInternal()) {
            					Long departmentId = dept.getId();
            					deptIds.add(departmentId);
            				}
            			}
        			}
        		}else if(OrgConstants.ORGENT_TYPE.Post.name().equals(type)){//发布范围里含有岗位
    		        EnumMap<RelationshipObjectiveName, Object> enummap = new EnumMap<RelationshipObjectiveName, Object>(RelationshipObjectiveName.class);
    		        enummap.put(OrgConstants.RelationshipObjectiveName.objective1Id, Long.valueOf(id));
    				List<V3xOrgRelationship> memberPostRels = orgManager.getV3xOrgRelationship(OrgConstants.RelationshipType.Member_Post, null, null, enummap);
            		for(V3xOrgRelationship rel : memberPostRels) {
            			Long memberId = rel.getSourceId();
            			Long departmentId = rel.getObjective0Id();
            			Long postId = rel.getObjective1Id();
            			//部门岗位信息不完整
            			if(departmentId == null || departmentId == -1L || postId == null || postId ==-1L) {
            				continue;
            			}

            			V3xOrgMember member = orgManager.getMemberById(memberId);
            			if(member == null || !member.isValid()) {
            				continue;
            			}
                		Set<Long> ids;
                		if(deptMembers.containsKey(departmentId)) {
                			ids = deptMembers.get(departmentId);
                		}else {
                			ids = new HashSet<Long>();
                			deptMembers.put(departmentId, ids);
                		}
                		ids.add(memberId);
            		}
        		}else if(OrgConstants.ORGENT_TYPE.Level.name().equals(type)){//发布范围里含有职务级别
    		        EnumMap<RelationshipObjectiveName, Object> enummap = new EnumMap<RelationshipObjectiveName, Object>(RelationshipObjectiveName.class);
    		        enummap.put(OrgConstants.RelationshipObjectiveName.objective2Id, Long.valueOf(id));
    				List<V3xOrgRelationship> memberPostRels = orgManager.getV3xOrgRelationship(OrgConstants.RelationshipType.Member_Post, null, null, enummap);
            		for(V3xOrgRelationship rel : memberPostRels) {
            			Long memberId = rel.getSourceId();
            			Long departmentId = rel.getObjective0Id();
            			Long postId = rel.getObjective1Id();
            			//部门岗位信息不完整
            			if(departmentId == null || departmentId == -1L || postId == null || postId ==-1L) {
            				continue;
            			}

            			V3xOrgMember member = orgManager.getMemberById(memberId);
            			if(member == null || !member.isValid()) {
            				continue;
            			}

                		Set<Long> ids;
                		if(deptMembers.containsKey(departmentId)) {
                			ids = deptMembers.get(departmentId);
                		}else {
                			ids = new HashSet<Long>();
                			deptMembers.put(departmentId, ids);
                		}
                		ids.add(memberId);
            		}
        		}else{//发布范围里含有直接选的人
        			Set<V3xOrgMember> members = orgManager.getMembersByTypeAndIds(arr);
        			for(V3xOrgMember member : members) {
        				List<MemberPost> memberPostList = orgManager.getMemberPosts(null, member.getId());
        				for(MemberPost mp : memberPostList) {
        					Long departmentId = mp.getDepId();
        					Long postId = mp.getPostId();
        					Long accountId = mp.getOrgAccountId();
        					if(!accessAccountIds.contains(accountId)) {
        						continue;
        					}
        					if(departmentId == null || postId == null) {
        						continue;
        					}
                    		Set<Long> ids;
                    		if(deptMembers.containsKey(departmentId)) {
                    			ids = deptMembers.get(departmentId);
                    		}else {
                    			ids = new HashSet<Long>();
                    			deptMembers.put(departmentId, ids);
                    		}
                    		ids.add(mp.getMemberId());
        				}
        			}
        		}
        	}

        	for(Long departmentId : deptIds) {// 将各个部门对应的本部门人员
        		Set<Long> ids;
        		if(deptMembers.containsKey(departmentId)) {//已存在的部门添加人员
        			ids = deptMembers.get(departmentId);
        		}else {//不存在的部门创建
        			ids = new HashSet<Long>();
        			deptMembers.put(departmentId, ids);
        		}
        		List<V3xOrgMember> members = orgManager.getMembersByDepartment(departmentId, true);
        		for(V3xOrgMember member : members) {
        			ids.add(member.getId());
        		}
        	}

            //可见单位下的，发布范围部门下有哪些人
        	List<Long> accessMemberIds = new UniqueList<Long>();// 可见人员
            Map<Long,Set<Long>> visibleMembersByDept = new HashMap<Long, Set<Long>>();
            for(Long departmentId : deptMembers.keySet()) {
            	V3xOrgDepartment dept = orgManager.getDepartmentById(departmentId);
            	if(accessAccountIds.contains(dept.getOrgAccountId())) {
            		Set<Long> memberSet;
            		if(visibleMembersByDept.containsKey(departmentId)) {
            			memberSet = visibleMembersByDept.get(departmentId);
            		}else {
            			memberSet = new HashSet<Long>();
            			visibleMembersByDept.put(departmentId, memberSet);
            		}
            		memberSet.addAll(deptMembers.get(departmentId));
            		accessMemberIds.addAll(deptMembers.get(departmentId));
            	}
            }

            Set<Long> scopeList = this.getAllMembersinPublishScope(bean,false);//公告发布范围内的人员
        	List<Long> noAccessMmeberIds = (List<Long>) CollectionUtils.subtract(scopeList, accessMemberIds);// 不可见人员=发布范围cap访问权限的人员

            //新闻发起人是否包含在发布范围中(发起人即便不在发布范围中，仍可以阅读新闻并生成阅读记录)
            boolean isCreatorInPublishScope = scopeList.contains(bean.getCreateUser());

            //将已分组的部门中的人员，筛选出已阅和未读的人数
            List<BulReadCount> bulreadcountList = new UniqueList<BulReadCount>();
            int visibleReadCount = 0;// 可见已读总人数
            List<Long> readIdsList = new UniqueList<Long>();
            for(Long readDeptId :visibleMembersByDept.keySet()) {
            	if(visibleMembersByDept.get(readDeptId).size()==0) {
            		continue;
            	}
            	BulReadCount brcount = new BulReadCount();
            	brcount.setMemberCount(visibleMembersByDept.get(readDeptId).size());
            	brcount.setDeptId(readDeptId);
            	int readCountByDept = 0;
            	for(Long readMemId : visibleMembersByDept.get(readDeptId)) {
            		for (BulRead br : readList) {
            			readIdsList.add(br.getManagerId());
            			V3xOrgMember member = orgManager.getMemberById(br.getManagerId());
            			boolean isManagerInPublishScope = true;
                        if(bulTypeManager.isManagerOfType(bean.getTypeId(), member.getId())){
                            isManagerInPublishScope = scopeList.contains(member.getId());
                        }

            			//如果阅读信息对应的用户不为正常状态，则不加入，如果阅读信息对应的用户是新闻创建者，而其并不在发布范围中，也不加入
                        if (member == null || !member.isValid()
                                || (member.getId().equals(bean.getCreateUser()) && !isCreatorInPublishScope)
                                || !isManagerInPublishScope) {
                            continue;
                        }
                		if(readMemId.equals(br.getManagerId())) {
                			readCountByDept++;
                			visibleReadCount++;
                		}
                	}
            	}
            	brcount.setEndReadCount(readCountByDept);
        		brcount.setShowFlag(1);
            	brcount.setEndReadCount(readCountByDept);
            	if (brcount.getDeptId().toString().equals(deptId)) {
                    mav.addObject("brc", brcount);
                }
            	brcount.setNotReadCount(visibleMembersByDept.get(readDeptId).size()-readCountByDept);
            	bulreadcountList.add(brcount);
            }
            if (log.isDebugEnabled()) {
            	log.debug("visibleReadCount: "+visibleReadCount);
            }
            List<BulReadCount> bulreadcountLists = bulreadcountList;
            int invisibleCount = noAccessMmeberIds.size();// 不可见总人数
            List<Long> invisibleReadList = Strings.getIntersection(noAccessMmeberIds,readIdsList);
            int invisibleReadCount = invisibleReadList!=null?invisibleReadList.size():0;//不可见已读总人数

			// 页面多个被星星单位合并
            if(invisibleCount>0) {
            	if (log.isDebugEnabled()) {
            		logger.debug("noAccessMmeberIds0:"+noAccessMmeberIds.get(0));
            	}
            	BulReadCount brc = new BulReadCount();
            	brc.setDeptId(0l);
            	brc.setMemberCount(invisibleReadCount);
            	brc.setNotReadCount(invisibleCount-invisibleReadCount>0?invisibleCount-invisibleReadCount:0);
            	brc.setEndReadCount(invisibleReadCount);
            	brc.setShowFlag(0);
    			bulreadcountLists.add(brc);
            }

            //转换为List以便保持顺序,否则将会导致读取页面时顺序时常变动：按照已阅人数总数、部门id、人员id排序
            Collections.sort(bulreadcountLists);
            mav.addObject("bulreadcount", CommonTools.pagenate(bulreadcountLists));
            mav.addObject("bulreadcountAll", bulreadcountList);
            //总的已读人员数
            int bulendread = readList.size();

            //如果阅读量小于已阅读人数,设置阅读量等于阅读人数
            if (bean.getReadCount() < bulendread) {
                bean.setReadCount(bulendread);
            }
//            if(isGroup){//集团版
//            }else{//企业版
//        	  }
         // 处理是否有查看阅读信息权限
            Boolean readRecord = false;
            if(bean.getExt1() != null
            		&& "1".equals(bean.getExt1())
            		&& (bean.getCreateUser() != null && bean.getCreateUser() == AppContext.getCurrentUser().getId())
//            		&& !isGuest
            		&& !orgManager.getMemberById(AppContext.getCurrentUser().getId()).isVJoinExternal()){
    			readRecord = true;
    		}
            // 判断是否是板块管理员
            boolean isManager = false;
            if (Integer.parseInt(bean.getExt1()) == 1) {
                if (AppContext.getCurrentUser().getId().longValue() == bean.getCreateUser().longValue()) {
                    isManager = true;
                } else {
                    isManager = this.bulTypeManager.isManagerOfType(bean.getTypeId(), AppContext.getCurrentUser().getId());
                }
            }
            mav.addObject("readRecord", readRecord);
            mav.addObject("isManager", isManager);
            mav.addObject("bulendread", bulendread);
        }
    }

    /**
     * 公告阅读页面iframe
     */
    public ModelAndView bulReadView(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("bulletin/user/bulReadView");
    }

    /**
     * 公告阅读页面点击部门查看，其页面结构是显示上方的已读和未读页签及切换部门下拉选框，下方有一个iframe指向:
     * @see #bulReadProperty
     */
    public ModelAndView bulReadIframe(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("bulletin/user/data_bulReadIframe");
        String deptId = request.getParameter("deptId");
        String beanId = request.getParameter("beanId");
        BulData bean = Strings.isBlank(beanId) ? new BulData() : bulDataManager.getById(Long.valueOf(beanId));
        mav.addObject("bean", bean);
        this.setBulDataReadInfo(bean, mav, deptId);

        mav.addObject("deptId", deptId); // 阅读的人的部门ID
        mav.addObject("beanId", beanId); // 公告的ID
        mav.addObject("spaceType", bean.getType().getSpaceType()); // 公告所属公告板块所在空间类型
        return mav;
    }

    /**
     * 公告阅读页面点击部门查看阅读情况
     */
    public ModelAndView bulReadProperty(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("bulletin/user/data_bulReadProperties");
        String deptId = request.getParameter("deptId"); // 取到部门的ID，这个是阅读信息部门显示的ID
        String beanId = request.getParameter("beanId"); // 公告ID
        String mode = request.getParameter("mode"); // 模式  normal已读，borrow未读
        BulData bean = Strings.isBlank(beanId) ? new BulData() : bulDataManager.getById(Long.valueOf(beanId));

        //当前登录单位可见的单位
		List<V3xOrgAccount> accAccounts = orgManager.accessableAccountsByUnitId(AppContext.currentAccountId());
		Set<Long> accessAccountIds = new HashSet<Long>();//当前单位可见的单位id
		for(V3xOrgAccount account : accAccounts) {
			if(!account.getIsGroup()) {
				accessAccountIds.add(account.getId());
			}
		}

        //取得已经阅读的所有的人员的列表
        List<BulRead> readList = this.bulDataManager.getReadListByData(bean.getId());
        Set<BulReadCount> bulreadcount = new HashSet<BulReadCount>(); // 已读

        Long currentDepartmentId = Long.valueOf(deptId);
        V3xOrgDepartment department = orgManager.getDepartmentById(currentDepartmentId);
        List<V3xOrgMember> memberList = new UniqueList<V3xOrgMember>();

    	String publishScope = bean.getPublishScope();
    	String[] publishScopeArr = publishScope.split(",");
    	for(String arr : publishScopeArr) {
    		String[] entity = arr.split("\\|");
    		String type = entity[0];
    		String id = entity[1];
    		if(OrgConstants.ORGENT_TYPE.Department.name().equals(type)) {
    			Long departmentId = Long.valueOf(id);
    			if(departmentId.equals(currentDepartmentId)) {
    				memberList = orgManager.getMembersByDepartment(currentDepartmentId, true);
    				break;
    			}else {
    				List<V3xOrgDepartment> childDepts = orgManager.getChildDepartments(departmentId, false);
    				for(V3xOrgDepartment dept : childDepts) {
    					if(dept.getId().equals(currentDepartmentId)) {
    	    				memberList = orgManager.getMembersByDepartment(currentDepartmentId, true);
    	    				break;
    					}
    				}
    			}
    		}else if(OrgConstants.ORGENT_TYPE.Account.name().equals(type)){
    			if(OrgConstants.GROUPID.equals(Long.valueOf(id))) {
    				memberList = orgManager.getMembersByDepartment(currentDepartmentId, true);
    				break;
    			} else {
    				if(Long.valueOf(id).equals(department.getOrgAccountId())) {
        				memberList = orgManager.getMembersByDepartment(currentDepartmentId, true);
        				break;
    				}
    			}
    		}else if(OrgConstants.ORGENT_TYPE.Post.name().equals(type)){
		        EnumMap<RelationshipObjectiveName, Object> enummap = new EnumMap<RelationshipObjectiveName, Object>(RelationshipObjectiveName.class);
		        enummap.put(OrgConstants.RelationshipObjectiveName.objective1Id, Long.valueOf(id));
				List<V3xOrgRelationship> memberPostRels = orgManager.getV3xOrgRelationship(OrgConstants.RelationshipType.Member_Post, null, null, enummap);
        		for(V3xOrgRelationship rel : memberPostRels) {
        			Long memberId = rel.getSourceId();
        			Long departmentId = rel.getObjective0Id();
        			Long postId = rel.getObjective1Id();
        			//部门岗位信息不完整
        			if(departmentId == null || departmentId == -1L || postId == null || postId ==-1L) {
        				continue;
        			}

        			if(departmentId.equals(currentDepartmentId)) {
        				V3xOrgMember member = orgManager.getMemberById(memberId);
        				if(member != null && member.isValid()) {
        					memberList.add(member);
        				}
        			}
        		}
    		}else if(OrgConstants.ORGENT_TYPE.Level.name().equals(type)){
		        EnumMap<RelationshipObjectiveName, Object> enummap = new EnumMap<RelationshipObjectiveName, Object>(RelationshipObjectiveName.class);
		        enummap.put(OrgConstants.RelationshipObjectiveName.objective2Id, Long.valueOf(id));
				List<V3xOrgRelationship> memberPostRels = orgManager.getV3xOrgRelationship(OrgConstants.RelationshipType.Member_Post, null, null, enummap);
        		for(V3xOrgRelationship rel : memberPostRels) {
        			Long memberId = rel.getSourceId();
        			Long departmentId = rel.getObjective0Id();
        			Long postId = rel.getObjective1Id();
        			//部门岗位信息不完整
        			if(departmentId == null || departmentId == -1L || postId == null || postId ==-1L) {
        				continue;
        			}
        			if(departmentId.equals(currentDepartmentId)) {
        				V3xOrgMember member = orgManager.getMemberById(memberId);
        				if(member != null && member.isValid()) {
        					memberList.add(member);
        				}
        			}
        		}
    		}else{
    			Set<V3xOrgMember> members = orgManager.getMembersByTypeAndIds(arr);
    			for(V3xOrgMember member : members) {
    				List<MemberPost> memberPostList = orgManager.getMemberPosts(null, member.getId());
    				for(MemberPost mp : memberPostList) {
    					Long departmentId = mp.getDepId();
    					Long postId = mp.getPostId();
    					Long accountId = mp.getOrgAccountId();
    					if(!accessAccountIds.contains(accountId)) {
    						continue;
    					}
    					if(departmentId == null || postId == null) {
    						continue;
    					}
            			if(departmentId.equals(currentDepartmentId)) {
            				if(member != null && member.isValid()) {
            					memberList.add(member);
            				}
            			}
    				}
    			}
    		}
    	}

        ArrayList<BulReadCount> noRead = new ArrayList<BulReadCount>();
        for (V3xOrgMember vm : memberList) { //遍历里面的人和已经阅读的人的ID 是否相等
        	BulReadCount brcnot = new BulReadCount(); // 未读
        	BulReadCount brc = new BulReadCount(); // 已读
            int readFlagNum = 0;
            for (BulRead nr : readList) {
                V3xOrgMember member = orgManager.getMemberById(nr.getManagerId());
                if (member.getId().equals(vm.getId())) {// 已读--取已读人的信息
                	brc.setDeptId(currentDepartmentId);
                	brc.setUserId(member.getId());
                	brc.setReadDate(brc.getReadDate());
                	bulreadcount.add(brc);
                    readFlagNum++;
                }
            }
            //此人未读，加入到未读群中
            if (readFlagNum == 0) {
            	brcnot.setDeptId(currentDepartmentId);
            	brcnot.setUserId(vm.getId());
                noRead.add(brcnot);
            }
        }
        mav.addObject("mode",mode);
        if("normal".equals(mode)){
            mav.addObject("bulreadcount", CommonTools.pagenate(this.convert2OrderedList(bulreadcount)));
        }else{

            mav.addObject("bulnotreadcount", CommonTools.pagenate(noRead));
        }
        // 判断是否是板块管理员
        boolean isManager = false;
        if (Integer.parseInt(bean.getExt1()) == 1) {
            if (AppContext.getCurrentUser().getId().longValue() == bean.getCreateUser().longValue()) {
                isManager = true;
            } else {
                isManager = this.bulTypeManager.isManagerOfType(bean.getTypeId(), AppContext.getCurrentUser().getId());
            }
        }
        mav.addObject("isManager", isManager);
        return mav;
    }

    /**
     * 将无序的集合转换为有序排列，以便前端展现
     * @param bulreadcount
     */
    private List<BulReadCount> convert2OrderedList(Set<BulReadCount> bulreadcount) {
        List<BulReadCount> bulReadCountList = new ArrayList<BulReadCount>();
        if (bulreadcount != null && bulreadcount.size() > 0) {
            bulReadCountList.addAll(bulreadcount);
            Collections.sort(bulReadCountList);
        }
        return bulReadCountList;
    }

    /**
     * 公告管理员查看公告统计情况页面框架
     */
    public ModelAndView statisticsIframe(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("bulletin/manager/statisticsIframe");
    }

    /**
     * 公告管理员查看公告统计情况
     */
    public ModelAndView statistics(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String type = request.getParameter("type");
        String sbulTypeId = request.getParameter("bulTypeId");
        String listIframe = request.getParameter("listIframe");
        ModelAndView mav = new ModelAndView("bulletin/manager/statistics");
        if ("listIframe".equals(listIframe)) {
            mav = new ModelAndView("bulletin/manager/statisticsList");
            List<Object[]> list = new ArrayList<Object[]>();
            if (StringUtils.isNotBlank(sbulTypeId)) {
                // 强制把所有的数据更新到数据库，以保证阅读次数与数据库记录一致（不实时亦可...）
                bulDataManager.getBulDataCache().getDataCache().updateAll();
                long bulTypeId = Long.valueOf(sbulTypeId);
                list = bulDataManager.statistics(type, bulTypeId);
            }
            mav.addObject("list", list);
        }
        return mav;
    }

    /**
     * 关联文档
     */
    public ModelAndView showList4QuoteFrame(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("bulletin/user/list4QuoteFrame");
        return modelAndView;
    }

    public ModelAndView list4QuoteFrame(HttpServletRequest request, HttpServletResponse response) {
        return new ModelAndView("collaboration/list4QuoteFrame");
    }

    public void destoryOperate(HttpServletRequest request) {
        request.getSession().removeAttribute("bulletin.typeId");
        request.getSession().removeAttribute("bulletin.type");
        request.getSession().removeAttribute("bulletin.isWriter");
        request.getSession().removeAttribute("bulletin.isAuditer");
        request.getSession().removeAttribute("bulletin.isManager");
        request.getSession().removeAttribute("bulletin.isShowPublish");
        request.getSession().removeAttribute("bulletin.isShowAudit");
        request.getSession().removeAttribute("bulletin.isShowManage");
        request.getSession().removeAttribute("bulletin.groupSign");
        request.getSession().removeAttribute("bulletin.deptSign");
        request.getSession().removeAttribute("bulletin.spaceType");
    }

    public void initOperate(HttpServletRequest request, String typeIdStr) {
        Long userId = AppContext.getCurrentUser().getId();
        if (typeIdStr == null || "".equals(typeIdStr.trim()))
            return;
        //设置管理按钮
        Long typeId = Long.valueOf(typeIdStr);
        boolean isShowPublish = false;
        boolean isShowAudit = false;
        boolean isShowManage = false;
        boolean isWriter = false;
        boolean isAuditer = false;
        boolean isManager = false;

        //发起员
        List<BulType> typeList = this.bulDataManager.getTypeListByWrite(AppContext.getCurrentUser().getId(), true);
        for (BulType type : typeList) {
            if (type.getId().longValue() == typeId.longValue()) {
                isWriter = true;
                break;
            }
        }
        BulType type = this.bulTypeManager.getById(typeId);
        if (type != null) {
            if (type.isAuditFlag() && type.getAuditUser().longValue() == userId.longValue()) {
                isAuditer = true;
            }
        }

        String groupSign = (String) request.getSession().getAttribute("bulletin.groupSign");// 集团标志
        if (groupSign != null) {
            isShowAudit = !bulTypeManager.getAuditGroupBulType(userId).isEmpty();
        } else {
            isShowAudit = !bulTypeManager.getAuditUnitBulType(userId).isEmpty();
        }

        try {
            isShowManage = this.bulDataManager.getTypeList(userId, true).size() > 0;
        } catch (Exception e) {
            log.error("", e);
        }

        if (type != null && type.getManagerUserIds().indexOf(userId.toString()) > -1) {
            isManager = true;
        }

        isShowPublish = isWriter || isManager;
        request.getSession().setAttribute("bulletin.typeId", typeId);
        request.getSession().setAttribute("bulletin.type", type);
        request.getSession().setAttribute("bulletin.isWriter", isWriter);
        request.getSession().setAttribute("bulletin.isAuditer", isAuditer);
        request.getSession().setAttribute("bulletin.isManager", isManager);
        request.getSession().setAttribute("bulletin.isShowPublish", isShowPublish);
        request.getSession().setAttribute("bulletin.isShowAudit", isShowAudit);
        request.getSession().setAttribute("bulletin.isShowManage", isShowManage);
    }

    /**
     * portal显示板块列表
     */
    public ModelAndView showDesignated(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("bulletin/user/showDesignated");
        List<BulType> groupTypes = null;
        List<BulType> accountTypes = null;
        String group = request.getParameter("group");
        String textfield = request.getParameter("textfield");

        if ("group".equals(group)) {
            groupTypes = bulTypeManager.groupFindAll();
        } else if ("myBul".equals(group)) {
            groupTypes = bulTypeManager.groupFindAll();
            accountTypes = bulTypeManager.boardFindAll();
        } else {
            accountTypes = bulTypeManager.boardFindAll();
        }

        List<BulType> groupList = new ArrayList<BulType>();
        List<BulType> accountList = new ArrayList<BulType>();
        if (Strings.isNotBlank(textfield)) {
            if (Strings.isNotEmpty(groupTypes)) {
                for (BulType type : groupTypes) {
                    if (type.getTypeName().contains(textfield)) {
                        groupList.add(type);
                    }
                }
            }
            if (Strings.isNotEmpty(accountTypes)) {
                for (BulType type : accountTypes) {
                    if (type.getTypeName().contains(textfield)) {
                        accountList.add(type);
                    }
                }
            }
        } else {
            groupList = groupTypes;
            accountList = accountTypes;
        }

        mav.addObject("groupList", groupList);
        mav.addObject("accountList", accountList);
        return mav;
    }

    public void setAppLogManager(AppLogManager appLogManager) {
        this.appLogManager = appLogManager;
    }

    public void setContentTemplateManager(ContentTemplateManager contentTemplateManager) {
        this.contentTemplateManager = contentTemplateManager;
    }

    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setUserMessageManager(UserMessageManager userMessageManager) {
        this.userMessageManager = userMessageManager;
    }

    public void setBulTypeManager(BulTypeManager bulTypeManager) {
        this.bulTypeManager = bulTypeManager;
    }

    public void setBulReadManager(BulReadManager bulReadManager) {
        this.bulReadManager = bulReadManager;
    }

    public void setIndexApi(IndexApi indexApi) {
        this.indexApi = indexApi;
    }

    public void setBulDataManager(BulDataManager bulDataManager) {
        this.bulDataManager = bulDataManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    public void setPortalApi(PortalApi portalApi) {
        this.portalApi = portalApi;
    }

    /**
     * 根据可操作列表展示
     *
     * */
    public ModelAndView listType(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String typeIdsStr = request.getParameter("typeIds");
        String ids = request.getParameter("ids");
        ModelAndView mav = new ModelAndView("bulletin/manager/moveTo");
        // 类型列表
        BulType type = null;
        List<BulType> typeList = new ArrayList<BulType>();
        if (Strings.isNotEmpty(typeIdsStr)) {
        	String[] str = typeIdsStr.split(",");
	        for (int i=0;i<str.length;i++){
	        	type=bulTypeManager.getById(Long.valueOf(str[i]));
	        	if(type != null){
	        		typeList.add(type);
	        	}
	        }
        } else{
        	String typeId = request.getParameter("typeId");
            User user = AppContext.getCurrentUser();
            BulType bulType = bulTypeManager.getById(Long.valueOf(typeId));
            typeList = getCanAdminTypes(user.getId(), bulType);
        }
        //启用排序 2013-08-09
        Collections.sort(typeList);
        mav.addObject("typeList", typeList);
        mav.addObject("ids", ids);

        return mav;
    }

    /**
     * 移动到新分类方法
     *
     * */
    public ModelAndView moveToType(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String idStr = request.getParameter("ids");
        String typeId = request.getParameter("typeId");
        BulData bean = null;
        if (StringUtils.isBlank(idStr)) {
            throw new Exception("bbs_not_exists");
        }else {
            String[] ids = idStr.split(",");
            Map<String, Object> summ = new HashMap<String, Object>();
            for (String id : ids) {
                if (StringUtils.isNotBlank(id)) {
                    bean = bulDataManager.getBulDataCache().getDataCache().get(Long.valueOf(id));
                    if (bean == null) {
                        bean = bulDataManager.getById(Long.valueOf(id));
                    }

                    eTagCacheManager.updateETagDate(BulConstants.ETAG_BUL_TYPE, String.valueOf(bean.getTypeId()));

                    //版块移动时取消置顶
                    bean.setTopOrder(Byte.valueOf("0"));
                    bean.setTypeId(Long.valueOf(typeId));
                    BulType type = this.bulTypeManager.getById(Long.valueOf(typeId));
                    bean.setType(type);
                    summ.put("typeId", bean.getTypeId());
                    summ.put("topOrder", Byte.valueOf("0"));
                    this.bulDataManager.update(bean.getId(), summ);
                    //从缓存中移除当前公告
                    this.bulDataManager.removeCache(bean.getId());

                    eTagCacheManager.updateETagDate(BulConstants.ETAG_BUL_TYPE, String.valueOf(bean.getTypeId()));

                    eTagCacheManager.updateETagDate(BulConstants.ETAG_BUL_ACCOUNT, String.valueOf(bean.getAccountId()));
                    eTagCacheManager.updateETagDate(BulConstants.ETAG_BUL_ACCOUNT, "-1");
                }
            }
        }
        super.rendJavaScript(response,"alert(\""+ResourceUtil.getString("bbs.board.moved")+"\");parent.cloWithSuccess();");
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
        Map<String, Object> modeType = bulDataManager.getTypeByMode(mode, spceTypeId);
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
        DataRecord record = bulDataManager.expStcToXls(param);
        fileToExcelManager.save(response, record.getTitle(), record);
        return null;
    }

    public void setDocApi(DocApi docApi) {
        this.docApi = docApi;
    }

    public BulletinUtils getBulletinUtils() {
		return bulletinUtils;
	}

	public void setBulletinUtils(BulletinUtils bulletinUtils) {
		this.bulletinUtils = bulletinUtils;
	}

    public void setCollaborationApi(CollaborationApi collaborationApi) {
        this.collaborationApi = collaborationApi;
    }

    public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
        this.ctpMainbodyManager = ctpMainbodyManager;
    }

    public void setBulIssueManager(BulIssueManager bulIssueManager) {
        this.bulIssueManager = bulIssueManager;
    }

    public FileToExcelManager getFileToExcelManager() {
        return fileToExcelManager;
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

    /******************************6.0******************************/

    /**
     * 公告首页/版块首页
     */
    public ModelAndView bulIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("bulletin/bulIndex");
		User user = AppContext.getCurrentUser();
		int spaceType = NumberUtils.toInt(request.getParameter("spaceType"));
		Long spaceId = NumberUtils.toLong(request.getParameter("spaceId"));
		String typeIds = request.getParameter("typeId");

        // 版块首页
        if (Strings.isNotBlank(typeIds)) {
            BulType bulType = bulTypeManager.getById(Long.valueOf(typeIds));
            if(bulType == null){
                super.rendJavaScript(response, "alert('"+ResourceUtil.getString("bultype.deleted.label")+"');window.close()");
                return null;
            }
            BulTypeModel bulTypeModel = new BulTypeModel(bulType, user.getId(), orgManager.getAllUserDomainIDs(user.getId()));
            bulTypeModel.setId(bulType.getId());
            bulTypeModel.setBulName(bulType.getTypeName());
            bulTypeModel.setTopNumber(String.valueOf(bulType.getTopCount()));
            // 版块审核员
            if (bulType.getAuditUser() != 0) {
                bulTypeModel.setAuditName(Functions.showMemberName(bulType.getAuditUser()));
            } else {
                bulTypeModel.setAuditName("");
            }

            StringBuilder typeAdmins = new StringBuilder();
            Set<Long> adminIds = new HashSet<Long>();
            if (spaceType == SpaceType.department.ordinal()) {
                //部门主管+空间管理员
                List<V3xOrgMember> deptManagers = orgManager.getMembersByRole(bulType.getId(), Role_NAME.DepManager.name());
                if (Strings.isNotEmpty(deptManagers)) {
                    for (V3xOrgMember member : deptManagers) {
                        if (member != null) {
                            adminIds.add(member.getId());
                        }
                    }
                }
                List<Object[]> secuitys = portalApi.getSecuityOfDepartment(bulType.getId(), SecurityType.manager.ordinal());
                if (Strings.isNotEmpty(secuitys)) {
                    for (Object[] object : secuitys) {
                        adminIds.add((Long) object[1]);
                    }
                }
            } else if (bulType.getSpaceType() == SpaceType.custom.ordinal()) {
                List<Long> managers = portalApi.getSpaceMemberBySecurity(bulType.getId(), SecurityType.manager.ordinal());
                for (Long member : managers) {
                    if (member != null) {
                        adminIds.add(member);
                    }
                }
            } else {
                for (BulTypeManagers tm : bulType.getBulTypeManagers()) {
                    if ("write".equals(tm.getExt1())) {
                        continue;
                    }
                    adminIds.add(tm.getManagerId());
                }
            }

            boolean flag = false;
            if (Strings.isNotEmpty(adminIds)) {
                for (Long adminId : adminIds) {
                    if (adminId.equals(user.getId())) {
                        bulTypeModel.setCanAdminOfCurrent(true);
                        bulTypeModel.setCanNewOfCurrent(true);
                    }
                    if (flag) {
                        typeAdmins.append("、");
                    } else {
                        flag = true;
                    }
                    typeAdmins.append(Functions.showMemberName(adminId));
                }
            }

            // 版块管理员
            bulTypeModel.setAdminsName(typeAdmins.toString());
            mav.addObject("bulTypeMessage", bulTypeModel);
            // 判断版块移动
            List<BulType> canAdminTypes = getCanAdminTypes(user.getId(), bulType);
            mav.addObject("canMove", Strings.isNotEmpty(canAdminTypes));
        }
        List<Long> myBulTypeIds = null;
        if (spaceType == SpaceType.group.ordinal() || spaceType == SpaceType.corporation.ordinal()) {
            myBulTypeIds = bulDataManager.getMyBulTypeIds(0, spaceId);
        } else {
            myBulTypeIds = bulDataManager.getMyBulTypeIds(spaceType, spaceId);
        }
		// 我发布的，我收藏的，公告审核
		mav.addObject("myIssueCount", bulDataManager.getMyIssueCount(user.getId(), myBulTypeIds));
		if (AppContext.hasPlugin("doc")) {
		    mav.addObject("myCollectCount", bulDataManager.getMyCollectCount(user.getId(), myBulTypeIds));
		}
		mav.addObject("myAuditCount", bulDataManager.getMyAuditCount(user.getId(), myBulTypeIds));
		// 是否允许发布公告
		boolean hasIssue = false;
		// 判断是否是公告审核员
		boolean auditManager = false;
		Map<String, List<BulTypeModel>> bulTypeModelMap = new LinkedHashMap<String, List<BulTypeModel>>();
		if (spaceType == SpaceType.public_custom.ordinal()) {// 自定义单位版块
			List<BulType> customBulTypeList = bulTypeManager.customAccBoardAllBySpaceId(spaceId, spaceType);
			List<BulTypeModel> customBulTypeModelList = new ArrayList<BulTypeModel>();
			boolean[] _per = this.toBulTypeModel(customBulTypeList, customBulTypeModelList);
			hasIssue = _per[0];
			auditManager = _per[1];
			bulTypeModelMap.put("bulletin.type.public.custom", customBulTypeModelList);
		} else if (spaceType == SpaceType.public_custom_group.ordinal()) {// 自定义集团版块
			List<BulType> customBulTypeList = bulTypeManager.customAccBoardAllBySpaceId(spaceId, spaceType);
			List<BulTypeModel> customBulTypeModelList = new ArrayList<BulTypeModel>();
			boolean[] _per = this.toBulTypeModel(customBulTypeList, customBulTypeModelList);
			hasIssue = _per[0];
			auditManager = _per[1];
			bulTypeModelMap.put("bulletin.type.public.custom.group", customBulTypeModelList);
		} else if (spaceType == SpaceType.custom.ordinal()) {// 自定义团队版块
			List<BulType> customBulTypeList = bulTypeManager.customAccBoardAllBySpaceId(spaceId, spaceType);
			List<BulTypeModel> customBulTypeModelList = new ArrayList<BulTypeModel>();
			boolean[] _per = this.toBulTypeModel(customBulTypeList, customBulTypeModelList);
			hasIssue = _per[0];
			auditManager = _per[1];
			bulTypeModelMap.put("bulletin.type.custom", customBulTypeModelList);
		} else if (spaceType == SpaceType.department.ordinal()) {// 部门版块
			List<PortalSpaceFix> deptSpaces = new ArrayList<PortalSpaceFix>();
			PortalSpaceFix Spaces = portalApi.getDeptSpaceIdByDeptId(spaceId);
			deptSpaces.add(Spaces);
			List<BulTypeModel> deptBoardModelList = new ArrayList<BulTypeModel>();
			boolean[] _per = this.getDeptBulModelList(deptSpaces, user.getId(), deptBoardModelList);
			hasIssue = _per[0];
			auditManager = _per[1];
			bulTypeModelMap.put("bulletin.type.department", deptBoardModelList);
		} else {
			// 集团版块
			boolean hasGroupIssue = false;
			boolean hasGroupAudit = false;
			if ((Boolean) (SysFlag.sys_isGroupVer.getFlag()) && user.isInternal()) {
				List<BulType> groupBulTypeList = bulTypeManager.groupFindAll();
				List<BulTypeModel> groupBulTypeModelList = new ArrayList<BulTypeModel>();
				boolean[] _perGroup = this.toBulTypeModel(groupBulTypeList, groupBulTypeModelList);
				hasGroupIssue = _perGroup[0];
				hasGroupAudit = _perGroup[1];
				bulTypeModelMap.put("bulletin.type.group", groupBulTypeModelList);
			}

			// 单位版块
			boolean hasAccountIssue = false;
			boolean hasAccountAudit = false;
			List<BulType> accountBulTypeList = bulTypeManager.boardFindAllByAccountId(user.getLoginAccount());
			List<BulTypeModel> accountNewsTypeModelList = new ArrayList<BulTypeModel>();
			boolean[] _perAccount = this.toBulTypeModel(accountBulTypeList, accountNewsTypeModelList);
			hasAccountIssue = _perAccount[0];
			hasAccountAudit = _perAccount[1];
			bulTypeModelMap.put("bulletin.type.corporation", accountNewsTypeModelList);

			hasIssue = hasGroupIssue || hasAccountIssue;
			auditManager = hasGroupAudit || hasAccountAudit;
		}

		mav.addObject("bulTypeModelMap", bulTypeModelMap);
		mav.addObject("hasIssue", hasIssue);
		mav.addObject("auditManager", auditManager);
		return mav;
	}

    /**
     * 我发布的/我收藏的/公告审核
     */
    public ModelAndView bulMyInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("bulletin/bulMyInfo");
        User user= AppContext.getCurrentUser();
        int spaceType = NumberUtils.toInt(request.getParameter("spaceType"));
        Long spaceId = NumberUtils.toLong(request.getParameter("spaceId"));
        List<Long> myBulTypeIds = null;
        if (spaceType == SpaceType.group.ordinal() || spaceType == SpaceType.corporation.ordinal()) {
            myBulTypeIds = bulDataManager.getMyBulTypeIds(0, spaceId);
        } else {
            myBulTypeIds = bulDataManager.getMyBulTypeIds(spaceType, spaceId);
        }
        mav.addObject("type",request.getParameter("type"));
        boolean hasIssue = bulDataManager.hasPermission(spaceType, spaceId, user)[0];
        mav.addObject("hasIssue",hasIssue);
        mav.addObject("auditManager",bulTypeManager.isAuditorOfBul(user.getId()));
        mav.addObject("myIssueCount", bulDataManager.getMyIssueCount(user.getId(),myBulTypeIds));
        if (AppContext.hasPlugin("doc")) {
            mav.addObject("myCollectCount", bulDataManager.getMyCollectCount(user.getId(),myBulTypeIds));
        }
        mav.addObject("myAuditCount", bulDataManager.getMyAuditCount(user.getId(),myBulTypeIds));
        return mav;
    }

    /**
     * 公告搜索
     */
    public ModelAndView bulSearch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("bulletin/bulSearch");
        return mav;
    }

    private EhSendRangeManager sendRangeManager=new EhSendRangeManagerImpl();

    public EhSendRangeManager getSendRangeManager() {
        return sendRangeManager;
    }

    /**
     * 公告新建/公告修改
     */
    public ModelAndView bulEdit(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        ModelAndView mav = new ModelAndView("bulletin/bulEdit");
        String bulTypeStr = request.getParameter("bulTypeId");
        String bulId = request.getParameter("bulId");
        String spaceTypeStr = request.getParameter("spaceType");
        String spaceId = request.getParameter("spaceId");
        mav.addObject("spaceType",spaceTypeStr);
        mav.addObject("spaceId",spaceId);
//      恩华药业 zhou start
        Map map=new HashMap();
        map.put("moduleId",Long.parseLong(bulTypeStr));
        List<EhSendRange> ehSendRanges=sendRangeManager.findEhSendRangeByCondition(map);
        EhSendRange ehSendRange=null;
        if(ehSendRanges.size()>0){
            ehSendRange= ehSendRanges.get(0);
            mav.addObject("range",ehSendRange);
        } else {
            mav.addObject("range",null);
        }
//      恩华药业 zhou end

        String oper = request.getParameter("form_oper");
        BulData bean = new BulData();
//        BulType type = this.bulTypeManager.getById(Long.valueOf(bulTypeStr));
//        bean.setType(type);
//        bean.setTypeId(Long.valueOf(bulTypeStr));
        int spaceTypeInt = SpaceType.corporation.ordinal();
        if (Strings.isNotBlank(spaceTypeStr)) {
            spaceTypeInt = Integer.valueOf(spaceTypeStr);
        }else if(Strings.isNotBlank(bulTypeStr)){//如果类型不是空
       	 	BulType bulType1 = bulTypeManager.getById(Long.valueOf(bulTypeStr));
       	 	if(bulType1 != null && bulType1.getSpaceType() != null){
                mav.addObject("spaceType",String.valueOf(bulType1.getSpaceType()));
       	 	}
       }
        List<Attachment> attachments = null;
        String bul_id = request.getParameter("id");
        if(Strings.isBlank(bulId) && Strings.isNotBlank(bul_id)){
            bulId = bul_id;
        }
        if (Strings.isNotBlank(bulId)) {
        	Long bulDataId = NumberUtils.toLong(bulId);
        	bean = bulDataManager.getBulDataCache().getDataCache().get(bulDataId);
        	if(bean == null){
        		bean = bulDataManager.getById(bulDataId);
        	}
            //如果管理员已将公告删除
            if (bean == null || bean.isDeletedFlag()) {
                super.rendJavaScript(response,
                        "alert('" + ResourceBundleUtil.getString(BulConstants.BUL_RESOURCE_BASENAME, "bul.data.noexist")
                                + "');window.close();");
                return null;
            }
            BulBody body = bulDataManager.getBody(bean.getId());
            bean.setContent(body.getContent());
            bean.setContentName(body.getContentName());
            /**
             * 检测当前操作是否可以继续：
             * 1.如果当前公告被审核员操作锁定，则不允许进行编辑操作，返回；
             * 2.如果编辑页面解锁失败，当前公告仍被当前用户的编辑操作锁定，由于编辑操作只有当前用户才能进行，也允许其进行编辑操作，避免自己被自己锁住。
             */
            String action = BulDataLockAction.NEWLOCK_EDITING;
            BulDataLock bullock = bulDataManager.lock(Long.valueOf(bulId), action);
            if (bullock != null && bullock.getUserid() != user.getId()) {
                V3xOrgMember orm = orgManager.getMemberById(bullock.getUserid());
                String lockmessage = bullock.getAction();
                super.rendJavaScript(
                        response,
                        "alert('"
                                + ResourceBundleUtil.getString(BulConstants.BUL_RESOURCE_BASENAME, lockmessage, orm.getName())
                                + "');window.close();");

                return null;
            }
            bulTypeStr = bean.getTypeId().toString();
            BulType bulType = bulTypeManager.getById(Long.valueOf(bulTypeStr));
            spaceTypeStr = bulType.getSpaceType().toString();
            bean.setContent(bulDataManager.getBody(bulDataId).getContent());
            attachments = attachmentManager.getByReference(bean.getId(), bean.getId());
            boolean isAduit = false;
            if(null != bean.getAuditUserId() && 0 != bean.getAuditUserId()){
                isAduit = true;
            }
            mav.addObject("isAduit", isAduit);

            Integer bulAuditPublishChoose = null;
            Integer finalPublish = null;
            String showPublisherName = null;
            String isAuditEdit = request.getParameter("isAuditEdit");
            if(isAuditEdit != null && "true".equals(isAuditEdit)){
                mav.addObject("isAuditEdit","true");
            	Map<String , String> option = new HashMap<String, String>();
            	option.put("1", bulType.getId().toString());
            	option.put("2", bulType.getTypeName());
            	String spaceTypeName ="";
            	if("1".equals(bulType.getSpaceType().toString())){
            		spaceTypeName = ResourceUtil.getString("bulletin.type.department");
            	}else if("2".equals(bulType.getSpaceType().toString())){
            		spaceTypeName = ResourceUtil.getString("bulletin.type.corporation");
            	}else if("3".equals(bulType.getSpaceType().toString())){
            		spaceTypeName = ResourceUtil.getString("bulletin.type.group");
            	}else if("4".equals(bulType.getSpaceType().toString())){
            		spaceTypeName = ResourceUtil.getString("bulletin.type.custom");
            	}else if("17".equals(bulType.getSpaceType().toString())){
            		spaceTypeName = ResourceUtil.getString("bulletin.type.public.custom");
            	}else if("18".equals(bulType.getSpaceType().toString())){
            		spaceTypeName = ResourceUtil.getString("bulletin.type.public.custom.group");
            	}
            	option.put("3", bulType.getSpaceType().toString());
            	option.put("4", spaceTypeName);
            	mav.addObject("option", JSONUtil.toJSONString(option));
            	// 审核时修改公告发布人为可选
            	if(bean.getPublishChoose() != null){
            		bulAuditPublishChoose = bean.getPublishChoose();
            		if(bulType.getFinalPublish() != null
            		   && bulType.getWritePermit() != null
            		   && bulType.getWritePermit()){
            			finalPublish = bulType.getFinalPublish();
            			if(Integer.valueOf("0").equals(bulAuditPublishChoose) && bean.getCreateUser() != null){
            				if(Integer.valueOf("0").equals(finalPublish)){
            					showPublisherName = orgManager.getMemberById(bean.getCreateUser()).getName();
            				} else if(Integer.valueOf("1").equals(finalPublish) && bean.getCreateUser() != null){
            					showPublisherName = orgManager.getMemberById(bean.getCreateUser()).getName();
            				} else if(Integer.valueOf("2").equals(finalPublish) && bean.getAuditUserId() != null){
            					showPublisherName = orgManager.getMemberById(bean.getAuditUserId()).getName();
            				}
            			} else if(Integer.valueOf("1").equals(finalPublish) && bean.getWritePublish() != null && Strings.isDigits(bean.getWritePublish())){
        					showPublisherName = orgManager.getMemberById(Long.valueOf(bean.getWritePublish())).getName();
        				} else if(Integer.valueOf("2").equals(finalPublish) && bean.getWritePublish() != null){
        					showPublisherName = bean.getWritePublish();
        				}
            		} else if(Integer.valueOf("1").equals(bulAuditPublishChoose) && bean.getWritePublish() != null && Strings.isDigits(bean.getWritePublish())){
    					showPublisherName = orgManager.getMemberById(Long.valueOf(bean.getWritePublish())).getName();
    				} else if(Integer.valueOf("2").equals(bulAuditPublishChoose) && bean.getWritePublish() != null){
    					showPublisherName = bean.getWritePublish();
    				}
            	}
            }
            mav.addObject("writePermit", bulType.getWritePermit());
            mav.addObject("finalPublish", finalPublish);
            mav.addObject("bulAuditPublishChoose", bulAuditPublishChoose);
            mav.addObject("showPublisherName", showPublisherName);
            mav.addObject("DEPARTMENTissueArea",bean.getPublishScope());
            String isBulEdit = request.getParameter("isBulEdit");//OA-188898
            if(Strings.isNotBlank(isBulEdit) && "isBulEdit".equals(isBulEdit)) {
            	mav.addObject("spaceType", request.getParameter("spaceType"));
            }else {
            	mav.addObject("spaceType", bean.getSpaceType().toString());
            }
            //添加发布人的各种控制，这里处理手动输入发布人或者选择发布人情况
            if(bean.isShowPublishUserFlag() && bean.getPublishChoose() == 1){
            	bean.setChoosePublshId(bean.getWritePublish());
            	bean.setWritePublish(null);
            }
        } else {
            bean.setCreateDate(new Timestamp(System.currentTimeMillis()));
            bean.setCreateUser(user.getId());
            bean.setState(BulConstants.DATA_STATE_NO_SUBMIT);
            bean.setDataFormat(com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_HTML);
            bean.setReadCount(0);

            // 设置发布部门 ---登录人员在兼职单位
            if (!user.getLoginAccount().equals(user.getAccountId())) {
                List<MemberPost> memberPostList = orgManager.getMemberConcurrentPostsByAccountId(user.getId(),
                        user.getLoginAccount());
                // 取兼职单位所在的部门
                if (Strings.isNotEmpty(memberPostList)) {
                    bean.setPublishDepartmentId(memberPostList.get(0).getDepId());
                }
            } else {
            	// 如果没有兼职信息，设置发布部门为当前用户登录单位所在的部门
                bean.setPublishDepartmentId(user.getDepartmentId());
            }
            bean.setContent(null);

            //处理附件,默认的是不管从正常切换到正常的格式,还是正常的格式转换到格式附件都不应该丢的
            String attaFlag = null;
            //第一次或者说新建的时候bean.getId()肯定是空的
            Long attRefId = null;
            //是不是第一次,true为第一次,false为不是第一次
            String attFlagStr = request.getParameter("attFlag");
            boolean attFlag = true;
            if ("false".equalsIgnoreCase(attFlagStr)) {
                attFlag = false;
            }
            //点击修改时候进来的,这个时候公告已有ID了
            String idStr = request.getParameter("id");
            if (Strings.isNotBlank(idStr)) {
                bean.setId(Long.valueOf(idStr));
                attachmentManager.deleteByReference(bean.getId(), bean.getId());
                attaFlag = attachmentManager.create(ApplicationCategoryEnum.bulletin, bean.getId(), bean.getId(), request);
                attFlag = false;
                mav.addObject("attRefId", attRefId);
                mav.addObject("attFlag", attFlag);
                attachments = attachmentManager.getByReference(bean.getId(), bean.getId());
                mav.addObject("attachments", attachments);
            } else {
            	if (attFlag) {
                    //第一次切换时执行以下方法
                    Long newId = UUIDLong.longUUID();
                    attaFlag = attachmentManager.create(ApplicationCategoryEnum.bulletin, newId, newId, request);
                    attRefId = newId;
                    attFlag = false;
                    mav.addObject("attRefId", attRefId);
                    mav.addObject("attFlag", attFlag);
                } else {
                    //第二次切换,会传递一个ID,应该先删除原来的附件,再创建一个ID;
                    attRefId = Long.valueOf(request.getParameter("attRefId"));
                    attachmentManager.deleteByReference(attRefId, attRefId);
                    attaFlag = attachmentManager.create(ApplicationCategoryEnum.bulletin, attRefId, attRefId, request);
                    attFlag = false;
                    mav.addObject("attRefId", attRefId);
                    mav.addObject("attFlag", attFlag);
                }
                attachments = attachmentManager.getByReference(attRefId, attRefId);
                mav.addObject("attachments", attachments);
            }
            if (com.seeyon.ctp.common.filemanager.Constants.isUploadLocaleFile(attaFlag)) {
                bean.setAttachmentsFlag(true);
            }
        }

        mav.addObject("bulTypeId", bulTypeStr);
        mav.addObject("attachments", attachments);
        //处理模板加载
        if (StringUtils.isNotBlank(oper) && "loadTemplate".equals(oper)) {
            super.bind(request, bean);
            bean.setShowPublishUserFlag("true".equals(request.getParameter("showPublish")));

            if (request.getParameterValues("noteCallInfo") != null && "1".equals(request.getParameterValues("noteCallInfo")[0])) {
                bean.setExt1("1");// 选中
            } else {
                bean.setExt1("0");// 未选中
            }
            if (request.getParameterValues("printAllow") != null && "1".equals(request.getParameterValues("printAllow")[0])) {
                bean.setExt2("1");// 选中
            } else {
                bean.setExt2("0");// 未选中
            }
            String templateId = request.getParameter("templateId");
            if (StringUtils.isNotBlank(templateId)) {
                ContentTemplate template = contentTemplateManager.getById(Long.valueOf(templateId));
                if (template != null) {
                    bean.setDataFormat(template.getTemplateFormat());
                    bean.setContent(template.getContent());
                    bean.setCreateDate(new Timestamp(template.getCreateDate().getTime()));
                    mav.addObject("templateId", template.getId());
                }
                mav.addObject("originalNeedClone", true);
            } else {
                bean.setDataFormat(com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_HTML);
                bean.setContent(null);
            }
        }
        //公告格式
        if(bulTypeStr!=null&&StringUtils.isNotBlank(bulTypeStr)){
        	BulType type = this.bulTypeManager.getById(Long.valueOf(bulTypeStr));
        	bean.setType(type);
        	bean.setTypeId(Long.valueOf(bulTypeStr));
        }
        //切换集团、单位版块数据保留
        String spaceType_change = request.getParameter("spaceType_change");
        if(Strings.isNotBlank(spaceType_change)&&"1".equals(spaceType_change)){
        	Long publistDepartIdDef = bean.getPublishDepartmentId();
        	super.bind(request, bean);
        	bean.setPublishDepartmentId(publistDepartIdDef);
        	bean.setPublishScope(null);
        	if(StringUtils.isNotBlank(request.getParameter("bulTempl"))){
        		bean.setDataFormat(com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_HTML);
                bean.setContent(null);
        	}
        	mav.addObject("bulTypeId", "");
        }

//      恩华药业 zhou start
        if(null !=ehSendRange){
            bean.setPublishScope(ehSendRange.getRangeId());
        }
//      恩华药业 zhou end
        mav.addObject("bean", bean);
        mav.addObject("constants", new BulConstants());
        //预览
        String bulPreview =request.getParameter("bulPreview");
        if(StringUtils.isNotBlank(bulPreview) && "bulPreview".equals(bulPreview)){
        	String bulSyle=bean.getType().getExt1();
        	ModelAndView mavPre = new ModelAndView("bulletin/bulView_detail"+Integer.valueOf(bulSyle));
        	mavPre.addObject("bean", bean);
        	mavPre.addObject("attachments", attachments);
        	return mavPre;
        }

        //版块
        Map<String, List<Map<String, String>>> boardListMap = new HashMap<String, List<Map<String, String>>>();

        List<BulType> bulTypeListGroup = new ArrayList<BulType>();
        List<BulType> bulTypeListAcc = new ArrayList<BulType>();
        List<BulType> bulTypeListCustom = new ArrayList<BulType>();
        List<Map<String, String>> newBulTypeListGroup = new ArrayList<Map<String, String>>();
        List<Map<String, String>> newBulTypeListAcc = new ArrayList<Map<String, String>>();
        List<Map<String, String>> newBulTypeListCustom = new ArrayList<Map<String, String>>();
        String groupKey = "";
        String accountkey = "";
        if (Strings.isNotBlank(spaceId) && (Integer.valueOf(spaceTypeStr)==SpaceType.public_custom_group.ordinal()
                || Integer.valueOf(spaceTypeStr)==SpaceType.public_custom.ordinal()
                || Integer.valueOf(spaceTypeStr)==SpaceType.custom.ordinal())) {
            if(Integer.valueOf(spaceTypeStr) == SpaceType.public_custom_group.ordinal()){
                bulTypeListGroup = this.bulTypeManager.getTypesCanCreate(user.getId(), BulConstants.valueOfSpaceType(18), Long.valueOf(spaceId));
                groupKey = "bulletin.type.public.custom.group";

                StringBuilder publisthScopeSpace = new StringBuilder();
                List<Object[]> issueAreas = portalApi.getSecuityOfSpace(Long.valueOf(spaceId));
                for(Object[] arr : issueAreas) {
                    publisthScopeSpace.append(StringUtils.join(arr, "|") + ",");
                }
                mav.addObject("entity",publisthScopeSpace.substring(0, publisthScopeSpace.length() - 1));
                mav.addObject("DEPARTMENTissueArea", publisthScopeSpace.substring(0, publisthScopeSpace.length() - 1));
            }else if(Integer.valueOf(spaceTypeStr) == SpaceType.public_custom.ordinal()){
                bulTypeListAcc = this.bulTypeManager.getTypesCanCreate(user.getId(), BulConstants.valueOfSpaceType(17), Long.valueOf(spaceId));
                accountkey = "bulletin.type.public.custom";

                StringBuilder publisthScopeSpace = new StringBuilder();
                List<Object[]> issueAreas = portalApi.getSecuityOfSpace(Long.valueOf(spaceId));
                for(Object[] arr : issueAreas) {
                    publisthScopeSpace.append(StringUtils.join(arr, "|") + ",");
                }
                mav.addObject("entity",publisthScopeSpace.substring(0, publisthScopeSpace.length() - 1));
                mav.addObject("DEPARTMENTissueArea", publisthScopeSpace.substring(0, publisthScopeSpace.length() - 1));
            }else if(Integer.valueOf(spaceTypeStr) == SpaceType.custom.ordinal()){
                BulType bul = this.bulTypeManager.getById(Long.valueOf(spaceId));
                bulTypeListCustom.add(bul);

                StringBuilder publisthScopeSpace = new StringBuilder();
                List<Object[]> issueAreas = portalApi.getSecuityOfSpace(Long.valueOf(spaceId));
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
            }
        }else if (spaceTypeInt == SpaceType.department.ordinal()) {
            List<PortalSpaceFix> deptSpaceModels = new ArrayList<PortalSpaceFix>();
            PortalSpaceFix departSpace = portalApi.getDeptSpaceIdByDeptId(Long.valueOf(spaceId));
            departSpace.setSpacename(ResourceUtil.getString(departSpace.getSpacename()));
            deptSpaceModels.add(departSpace);
            //bean.setPublishDepartmentId(Long.valueOf(bulTypeIds));
            mav.addObject("deptSpaceModels", deptSpaceModels);
            mav.addObject("deptSpaceModelsLength", deptSpaceModels.size());
            mav.addObject("DEPARTMENTissueArea","Department|"+bulTypeStr);
            mav.addObject("ChildDeptissueArea","Department|"+bulTypeStr);
        }else{
            bulTypeListGroup = this.bulTypeManager.getTypesCanCreate(user.getId(), BulConstants.valueOfSpaceType(3), user.getLoginAccount());
            groupKey = "bulletin.type.group";
            bulTypeListAcc = this.bulTypeManager.getTypesCanCreate(user.getId(), BulConstants.valueOfSpaceType(2), user.getLoginAccount());
            accountkey = "bulletin.type.corporation";

        }
        if(bulTypeListGroup != null && bulTypeListGroup.size() > 0){
            mav.addObject("groupSize", "true");
            for(BulType bulType1 : bulTypeListGroup){
                Map<String , String> mapType= new HashMap<String, String>();
                mapType.put("id", String.valueOf(bulType1.getId()));
                mapType.put("typeName", bulType1.getTypeName());
                newBulTypeListGroup.add(mapType);
            }
            boardListMap.put(groupKey, newBulTypeListGroup);
        }else {
            mav.addObject("groupSize", "false");
        }
        if(bulTypeListAcc != null && bulTypeListAcc.size() > 0){
            mav.addObject("corporationSize", "true");
            for(BulType bulType1 : bulTypeListAcc){
                Map<String , String> mapType= new HashMap<String, String>();
                mapType.put("id", String.valueOf(bulType1.getId()));
                mapType.put("typeName", bulType1.getTypeName());
                newBulTypeListAcc.add(mapType);
            }
            boardListMap.put(accountkey, newBulTypeListAcc);
        }else {
            mav.addObject("corporationSize", "false");
        }
        if(bulTypeListCustom != null && bulTypeListCustom.size() > 0){
            mav.addObject("customSize", "true");
            for(BulType bulType1 : bulTypeListCustom){
                Map<String , String> mapType= new HashMap<String, String>();
                mapType.put("id", String.valueOf(bulType1.getId()));
                mapType.put("typeName", bulType1.getTypeName());
                newBulTypeListCustom.add(mapType);
            }
            boardListMap.put("bulletin.type.custom", newBulTypeListCustom);
        }else {
            mav.addObject("customSize", "false");
        }

        mav.addObject("boardListMap",boardListMap);
        mav.addObject("boardListMapJson", JSONUtil.toJSONString(boardListMap));

        return mav;
    }

    /**
     * 创建、修改公告正文部分-6.0新版
     * @throws Exception
     */
    public ModelAndView createBulContent(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("bulletin/bulContent");
        String idStr = request.getParameter("bulId");
        String content = request.getParameter("content");
        if(!Strings.isBlank(idStr)){
        	Long bulId = Long.parseLong(idStr);
        	//V3xBbsArticle article = bbsArticleManager.getArticleById(articleId);
        	BulBody body = bulDataManager.getBody(bulId);
        	if(body!=null){
        		content = body.getContent();
        		mav.addObject("body",body);
        	}
        }
        mav.addObject("content",content);
        return mav;
    }

    /**
     * 老页面跳转兼容（勿删）
     */
    public ModelAndView userView(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return super.redirectModelAndView("/bulData.do?method=bulView&bulId=" + request.getParameter("id"));
    }

    /**
     * 公告查看
     */
    public ModelAndView bulView(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //from=list,首页，版块首页，换一换
        //from=message，从消息打开
        //from=pigeonhole，从归档打开
        //from=colCube，从协同立方打开
        //from=myCreate,从我发起的打开
        //from=myCollect,从我收藏的打开
        //from=myAudit,从公告审核打开
        ModelAndView mav = new ModelAndView("bulletin/bulView");
        String from = request.getParameter("from");
        // h5打印调用获取正文
        if ("h5print".equals(from)) {
            mav = new ModelAndView("bulletin/bulViewContent");
        }
        User user = AppContext.getCurrentUser();
        boolean isGuest = user.isDefaultGuest() || user.isScreenGuest(); //Guest帐号
        mav.addObject("isGuest",isGuest);
        String idStr = request.getParameter("bulId");
        Long bulId = Long.valueOf(idStr);
        BulData bean = bulDataManager.getBulDataCache().getDataCache().get(bulId);
        boolean hasCache = false;
        if (bean == null) {
            bean = bulDataManager.getById(bulId);
        } else {
            hasCache = true;
        }
        String alertInfo = ResourceUtil.getString("bulletin.dataStateError");
        if (bean == null || bean.getType().getUsedFlag() != BulConstants.TYPE_FLAG_NORMAL || bean.isDeletedFlag()) {
            super.rendJavaScript(response, "alert(\"" + alertInfo + "\");window.close();");
            return null;
        }

        if(isGuest){ //因为有阅读量，故仅仅针对guest做etag
            Date date = bean.getUpdateDate();
            if (date == null) {
                date = bean.getCreateDate();
            }
	        String ETag = String.valueOf(date.getTime());
	        if(WebUtil.checkEtag(request, response, ETag)){
	        	return null;
	        }

	        WebUtil.writeETag(request, response, ETag);
    	}

        if(!isGuest){
        	//更新消息状态
        	userMessageManager.updateSystemMessageStateByUserAndReference(user.getId(), bulId);
        }

        BulBody body = bulDataManager.getBody(bean.getId());
        bean.setContent(body.getContent());
        bean.setContentName(body.getContentName());
        this.getBulletinUtils().initData(bean);
		// V6.1发布人版块控制管理
        if(bean.getType() != null && bean.isShowPublishUserFlag()){
        	if(Strings.isNotBlank(bean.getWritePublish()) && bean.getPublishChoose() != 0){
        		if(bean.getPublishChoose() == 1){
        			bean.setPublishMemberName(Functions.showMemberName(Long.valueOf(bean.getWritePublish())));
        		} else if(bean.getPublishChoose() == 2) {
        			bean.setPublishMemberName(bean.getWritePublish());
        		}
        	} else if(bean.getPublishUserId()!=null){
//        		if (bean.getType().isAuditFlag()) {
//    				if (bean.getType().getFinalPublish() == 0 && bean.getPublishUserId() != null) {
//    					bean.setPublishMemberName(orgManager.getMemberById(bean.getPublishUserId()).getName());
//    				} else if (bean.getType().getFinalPublish() == 2) {
//    					if (bean.getAuditUserId() != null) {
//    						bean.setPublishMemberName(orgManager.getMemberById(bean.getAuditUserId()).getName());
//    					} else {
//    						bean.setPublishMemberName(orgManager.getMemberById(bean.getCreateUser()).getName());
//    					}
//    				} else {
//    					bean.setPublishMemberName(orgManager.getMemberById(bean.getCreateUser()).getName());
//    				}
//    			} else {
//    				bean.setPublishMemberName(orgManager.getMemberById(bean.getCreateUser()).getName());
//    			}
        		bean.setPublishMemberName(Functions.showMemberName(bean.getPublishUserId()));
        	} else {
        		bean.setPublishMemberName(Functions.showMemberName(bean.getCreateUser()));
        	}
        }
//        mav.addObject("bean", bean);
        if (bean.getAttachmentsFlag()) {
            List<Attachment> attachments = attachmentManager.getByReference(bean.getId(), bean.getId());
            String attListJSON = attachmentManager.getAttListJSON(attachments);
            mav.addObject("attListJSON", attListJSON);
        } else {
        	mav.addObject("attListJSON", "");
        }

        //发布状态以及归档以外详情页面打开
        boolean state_noPublish = false;
        if (bean.getState().intValue() != BulConstants.DATA_STATE_ALREADY_PUBLISH && bean.getState().intValue() != BulConstants.DATA_STATE_ALREADY_PIGEONHOLE) {
            state_noPublish = true;
        }
        //公告样式
        String bulStyle = bean.getType().getExt1();
        String showPublishName = ""; // 发布人
        if (state_noPublish) {
        	mav.setViewName("bulletin/bulView_detail" + Integer.valueOf(bulStyle));
            if ("list".equals(from) || "myCollect".equals(from) || ("message".equals(from) && bean.getState().intValue() == BulConstants.DATA_STATE_NO_SUBMIT)) {
                super.rendJavaScript(response, "alert('" + alertInfo + "');window.close();");
                return null;
            }
            mav.addObject("dataExist", Boolean.TRUE);
            Long bulType_auditUser = bean.getType().getAuditUser();
            Long agentId = AgentUtil.getAgentByApp(bulType_auditUser, ApplicationCategoryEnum.bulletin.getKey());
            if (("myAudit".equals(from) || ("message".equals(from)))
                    && ((user.getId()).equals(bulType_auditUser) || (user.getId()).equals(agentId))) {
                if (bean.getState().intValue() == BulConstants.DATA_STATE_ALREADY_CREATE) {
                    //检验文件是否加锁
                    String action = BulDataLockAction.NEWLOCK_AUDITING;
                    BulDataLock bullock = bulDataManager.lock(bulId, action);
                    if (bullock != null && bullock.getUserid() != AppContext.currentUserId()) {
                        String lockaction = bullock.getAction();
                        V3xOrgMember orm = orgManager.getMemberById(bullock.getUserid());
                        super.rendJavaScript(response, "alert('" + ResourceBundleUtil.getString(BulConstants.BUL_RESOURCE_BASENAME, lockaction, orm.getName(), 2) + "');window.close();");
                        return null;
                    }

                    List<CtpAffair> updateaffs = affairManager.getAffairs(ApplicationCategoryEnum.bulletin, Long.valueOf(idStr));
                    if (!updateaffs.isEmpty() && updateaffs.get(0).getSubState() != SubStateEnum.col_pending_read.key()) {
                        //因为只有一条，因此取0
                        CtpAffair updateaff = updateaffs.get(0);
                        updateaff.setSubState(SubStateEnum.col_pending_read.key());
                        affairManager.updateAffair(updateaff);
                    }

                    mav.addObject("lockAuditFlag", true);
                } else if (bean.getState().intValue() == BulConstants.DATA_STATE_ALREADY_AUDIT) {
                    mav.addObject("lockAuditFlag", false);
                }
            }

            // 审核员从消息进入到审核
            boolean auditerFlag = (("message".equals(from)
                    && bean.getState().intValue() == BulConstants.DATA_STATE_ALREADY_CREATE)
                    || ("message".equals(from) && (bean.getState().intValue() == BulConstants.DATA_STATE_ALREADY_AUDIT
                            || bean.getState().intValue() == BulConstants.DATA_STATE_NOPASS_AUDIT)))
                    && ((user.getId()).equals(bean.getType().getAuditUser()) || (user.getId()).equals(agentId));
            //发起人从消息进入到审核
            boolean createrFlag = "message".equals(from) && (bean.getCreateUser()).equals(user.getId());
            if ("myAudit".equals(from) || auditerFlag) {
                boolean already_audit = false;
                boolean already_create = false;
                if (bean.getState().intValue() == BulConstants.DATA_STATE_ALREADY_AUDIT || bean.getState().intValue() == BulConstants.DATA_STATE_NOPASS_AUDIT) {
                    //审核通过待发布：	审核人打开，页尾显示审核信息
                    already_audit = true;
                    mav.addObject("already_audit", already_audit);
                    //审核通过，发起人打开，页首显示审核信息，发布按钮
                    if (user.getId().equals(bean.getCreateUser()) && bean.getState().intValue() == BulConstants.DATA_STATE_ALREADY_AUDIT) {
                        mav.addObject("already_audit_myCreate", true);
                    }
                } else if (bean.getState().intValue() == BulConstants.DATA_STATE_ALREADY_CREATE) {
                    //待审核：		审核人打开，页首显示处理窗口
                    already_create = true;
                    mav.addObject("already_create", already_create);
                }
            } else if ("myCreate".equals(from) || createrFlag) {
                boolean noPass_audit = false;
                if (bean.getState().intValue() == BulConstants.DATA_STATE_ALREADY_AUDIT) {
                    //审核通过，发起人打开，页首显示审核信息，发布按钮
                    mav.addObject("already_audit_myCreate", true);
                } else if (bean.getState().intValue() == BulConstants.DATA_STATE_NOPASS_AUDIT) {
                    //审核不通过、发起人打开，页首显示审核信息
                    noPass_audit = true;
                    mav.addObject("noPass_audit", noPass_audit);
                }
            }
            // 发布人
            if(bean.getType() != null && bean.isShowPublishUserFlag()
            		&& bean.getType().getWritePermit() != null
        			&& bean.getType().getWritePermit()
        			&& bean.getPublishChoose() != null){
            	if(bean.getPublishChoose().equals(0)){
            		if(bean.getType().getFinalPublish() != null){
            			Integer finalPublish = bean.getType().getFinalPublish();
            			if(finalPublish.equals(0) && bean.getPublishUserId() != 0){
            				showPublishName = orgManager.getMemberById(bean.getPublishUserId()).getName();
            			} else if(finalPublish.equals(1) && bean.getCreateUser() != null){
            				showPublishName = orgManager.getMemberById(bean.getCreateUser()).getName();
            			} else if(finalPublish.equals(2) && bean.getAuditUserId() != null){
            				showPublishName = orgManager.getMemberById(bean.getAuditUserId()).getName();
            			} else if(bean.getPublishUserId()!=null){
            				showPublishName = orgManager.getMemberById(bean.getPublishUserId()).getName();
            			}
            		}
            	} else if(bean.getPublishChoose().equals(1)){
            		if(bean.getPublishUserId() != null){
            			showPublishName = orgManager.getMemberById(bean.getPublishUserId()).getName();
            		} else if(Strings.isNotBlank(bean.getWritePublish()) && Strings.isDigits(bean.getWritePublish())){
            			showPublishName = orgManager.getMemberById(Long.valueOf(bean.getWritePublish())).getName();
            		}
            	} else if(bean.getPublishChoose().equals(2) && Strings.isNotBlank(bean.getWritePublish())) {
            		showPublishName =  bean.getWritePublish();
            	} else if(bean.getPublishUserId()!=null){
            		showPublishName = orgManager.getMemberById(bean.getPublishUserId()).getName();
    			}
            }
        } else {// 已经发布的、归档的这里打开
            mav.addObject("bulStyle", bulStyle);

            eTagCacheManager.updateETagDate(BulConstants.ETAG_BUL_USER, String.valueOf(user.getId()));

            if (from != null) {
                //防护：已发布的公告被删除或被管理员归档，其他用户点击系统消息链接时候给出提示<归档的公告，在文档中心处可以正常查看>
                if (!"pigeonhole".equals(from)) {
                    if (bean.getState().intValue() != BulConstants.DATA_STATE_ALREADY_PUBLISH || !bulDataManager.checkScope(user.getId(), bulId)) {
                        // 来自协同立方的话，这样关闭，
                        if ("colCube".equals(from)) {
                            super.rendJavaScript(response, "alert('" + alertInfo + "');window.close();parent.window.parentDialogObj.url.closeParam.handler();");
                        } else {
                            super.rendJavaScript(response, "alert('" + alertInfo + "');window.close();");
                        }
                        return null;
                    }
                }
            }

            // SECURITY 访问安全检查
            if (!SecurityCheck.isLicit(request, response, ApplicationCategoryEnum.bulletin, user, bulId, null, null)) {
                return null;
            }
            if (bean.getState().intValue() != BulConstants.DATA_STATE_ALREADY_PIGEONHOLE) {
                recordBulRead(bulId, bean, hasCache, user);
            }

            if (com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_FORM.equals(bean.getDataFormat())) {
                String content = this.convertContent(bean.getContent());
                if (content != null) {
                    bean.setDataFormat(com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_HTML);
                    bean.setContent(content);
                }
            }

            if (com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_HTML.equals(bean.getDataFormat())) {
                String content = this.convertContentV(bean.getContent());
                if (content != null) {
                    bean.setContent(content);
                }
            }

            Long userId = AppContext.getCurrentUser().getId();
            Boolean isManager = this.bulTypeManager.isManagerOfType(bean.getTypeId(), userId);
            Boolean readRecord = false;
            if(bean.getOpenRecordRead() != null && bean.getOpenRecordRead() == 1 && user.isV5Member()){
            	readRecord = true;
    		} else if(Strings.isNotBlank(bean.getExt1()) && "1".equals(bean.getExt1()) && user.isV5Member() && (isManager || userId.longValue() == bean.getCreateUser().longValue())) {
    			readRecord = true;
    		}
        	if (bean.getCreateUser() != null && userId.longValue() == bean.getCreateUser().longValue() && user.isV5Member()) {
                isManager = true;
            }
            mav.addObject("isManager", isManager);
            mav.addObject("readRecord",readRecord);
            if (AppContext.hasPlugin("doc")) {
                String collectFlag = SystemProperties.getInstance().getProperty("doc.collectFlag");
                if ("true".equals(collectFlag)) {
                    List<Map<String, Long>> collectMap = docApi.findFavorites(userId, CommonTools.newArrayList(bean.getId()));
                    if (!collectMap.isEmpty()) {
                        mav.addObject("isCollect", true);
                        mav.addObject("collectDocId", collectMap.get(0).get("id"));
                    }
                }
                mav.addObject("docCollectFlag", collectFlag);
            }
            // 发布人
            if(bean.getType() != null && bean.isShowPublishUserFlag()
            		&& bean.getType().getWritePermit() != null
        			&& bean.getType().getWritePermit()
        			&& bean.getPublishChoose() != null){
            	if(bean.getPublishChoose().equals(0)){
            		if(bean.getType().getFinalPublish() != null){
            			Integer finalPublish = bean.getType().getFinalPublish();
            			if(finalPublish.equals(0) && bean.getPublishUserId() != 0){
            				showPublishName = orgManager.getMemberById(bean.getPublishUserId()).getName();
            			} else if(finalPublish.equals(1) && bean.getCreateUser() != null){
            				showPublishName = orgManager.getMemberById(bean.getCreateUser()).getName();
            			} else if(finalPublish.equals(2) && bean.getAuditUserId() != null){
            				showPublishName = orgManager.getMemberById(bean.getAuditUserId()).getName();
            			} else if(bean.getPublishUserId()!=null){
            				showPublishName = orgManager.getMemberById(bean.getPublishUserId()).getName();
            			}
            		}
            	} else if(bean.getPublishChoose().equals(1)){
            		if(bean.getPublishUserId() != null){
            			showPublishName = orgManager.getMemberById(bean.getPublishUserId()).getName();
            		} else if(Strings.isNotBlank(bean.getWritePublish()) && Strings.isDigits(bean.getWritePublish())){
            			showPublishName = orgManager.getMemberById(Long.valueOf(bean.getWritePublish())).getName();
            		}
            	} else if(bean.getPublishChoose().equals(2) && Strings.isNotBlank(bean.getWritePublish())) {
            		showPublishName =  bean.getWritePublish();
            	} else if(bean.getPublishUserId()!=null){
            		showPublishName = orgManager.getMemberById(bean.getPublishUserId()).getName();
    			}
            }
        }
        bean.setShowPublishName(showPublishName);
        mav.addObject("bean", bean);
        AccessControlBean.getInstance().addAccessControl(ApplicationCategoryEnum.bulletin, String.valueOf(bean.getId()), AppContext.currentUserId());
        return mav;
    }

    /**
     * 公告预览
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView bulPreview(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	BulData bean = new BulData();
        String title = request.getParameter("previewTitle");
        String createUserId = request.getParameter("createUser");
        String publishChoose = request.getParameter("publishChoose");
        String choosePublshId = request.getParameter("choosePublshId");
        String writePublish = request.getParameter("writePublish");
        super.bind(request, bean);
        //是否显示发布人
        String showPublishUserFlag = request.getParameter("showPublish");
        bean.setShowPublishUserFlag(Boolean.valueOf(showPublishUserFlag));
        Long typeId = bean.getTypeId();
        BulType type = this.bulTypeManager.getById(typeId);
        bean.setType(type);
        bean.setTitle(title);
        String bulStyle = bean.getType().getExt1();
        ModelAndView mav = new ModelAndView("bulletin/bulView_detail"+Integer.valueOf(bulStyle));
        Long newId = UUIDLong.longUUID();
        Long attRefId = newId;
        List<Attachment> attachments = attachmentManager.getByReference(attRefId, attRefId);
        bean.setReadCount(0);
        bean.setId(attRefId);
        if(Strings.isNotBlank(createUserId)){
        	bean.setCreateUser(Long.valueOf(createUserId));
        }else {
        	bean.setCreateUser(AppContext.currentUserId());
        }
        bean.setPublishDeptName(request.getParameter("publishDepartmentName"));
        if(Strings.isNotBlank(publishChoose)){
        	if("0".equals(publishChoose) && type.getFinalPublish() != null){
        		Integer finalPublish = type.getFinalPublish();
        		if(finalPublish.intValue() == 0){
        			bean.setShowPublishName(AppContext.getCurrentUser().getName());
        		} else if(finalPublish.intValue() == 1 && bean.getCreateUser() != null){
        			bean.setShowPublishName(orgManager.getMemberById(bean.getCreateUser()).getName());
        		} else if(finalPublish.intValue() == 2 && type.getAuditUserName()!= null){
        			bean.setShowPublishName(type.getAuditUserName());
        		}
        	} else if("1".equals(publishChoose) && Strings.isNotBlank(choosePublshId)){
        		bean.setShowPublishName(orgManager.getMemberById(Long.parseLong(choosePublshId)).getName());
        	} else if("2".equals(publishChoose) && Strings.isNotBlank(writePublish)){
        		bean.setShowPublishName(writePublish);
        	}
        } else {
        	bean.setShowPublishName("");
        }
        mav.addObject("bean", bean);
        mav.addObject("attachments", attachments);
        return mav;
    }


    private boolean[] toBulTypeModel(List<BulType> bulTypeList, List<BulTypeModel> bulTypeModelList) throws Exception {
        User user = AppContext.getCurrentUser();
        boolean hasIssue = false;
        boolean hasAudit = false;
        for (BulType bulType : bulTypeList) {
        	BulTypeModel bulTypeModel = new BulTypeModel(bulType, user.getId(), orgManager.getAllUserDomainIDs(user.getId()));
            bulTypeModel.setId(bulType.getId());
            bulTypeModel.setBulName(bulType.getTypeName());
        	if (bulTypeModel.getCanNewOfCurrent()) {
                hasIssue = true;
            }
        	if (bulTypeModel.isCanAuditOfCurrent()) {
        	    hasAudit = true;
        	}
        	bulTypeModel.setFlag(true);
            bulTypeModelList.add(bulTypeModel);
        }
        return new boolean[]{hasIssue,hasAudit};
    }

    private boolean[] getDeptBulModelList(List<PortalSpaceFix> spaces, Long userId, List<BulTypeModel> bulModelList) throws Exception {
        boolean hasIssue = false;
        boolean hasAudit = false;
        for (PortalSpaceFix portalSpaceFix : spaces) {
            BulTypeModel bulModel = new BulTypeModel();
            bulModel.setId(portalSpaceFix.getEntityId());
            bulModel.setBulName(ResourceUtil.getString(portalSpaceFix.getSpacename()));
            bulModel.setFlag(false);
            bulModel.setSpaceId(portalSpaceFix.getId());
            bulModel.setSpaceType(portalSpaceFix.getType());
            //部门没有审核人
            bulModel.setCanAuditOfCurrent(false);
            // 判断当前用户是否是管理员
            if (portalApi.isManagerOfThisSpace(userId, portalSpaceFix.getId())) {
                bulModel.setCanAdminOfCurrent(true);
                bulModel.setCanNewOfCurrent(true);
                hasIssue = true;
            } else {
                bulModel.setCanAdminOfCurrent(false);
                bulModel.setCanNewOfCurrent(false);
            }
            bulModelList.add(bulModel);
        }
        return new boolean[] { hasIssue, hasAudit };
    }

    public List<BulType> getCanAdminTypes(Long memberId, BulType bulType){
        Integer spaceType = bulType.getSpaceType();
        Long accountId = bulType.getAccountId();
        List<BulType> bulTypeList = new ArrayList<BulType>();
        if (spaceType == SpaceType.public_custom.ordinal()) {// 自定义单位版块
        	bulTypeList = bulTypeManager.getManagerTypeByMember(memberId, SpaceType.public_custom, accountId);
        } else if (spaceType == SpaceType.public_custom_group.ordinal()) {// 自定义集团版块
        	bulTypeList = bulTypeManager.getManagerTypeByMember(memberId, SpaceType.public_custom_group, accountId);
        } else if (spaceType == SpaceType.group.ordinal()) {// 集团版块
        	bulTypeList = bulTypeManager.getManagerTypeByMember(memberId, SpaceType.group, accountId);
        } else if (spaceType == SpaceType.corporation.ordinal()) {// 单位版块
        	bulTypeList = bulTypeManager.getManagerTypeByMember(memberId, SpaceType.corporation, accountId);
        }
        if (Strings.isNotEmpty(bulTypeList)) {
        	bulTypeList.remove(bulType);
        }
        return bulTypeList;
    }

    /**
     * 公告发起员保存公告的操作
     */
    public ModelAndView bulSave(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String dataformat = request.getParameter("dataFormat");
        String ext5 = request.getParameter("ext5");
        String spaceId = request.getParameter("spaceId");
        boolean isAuditEdit = "true".equals(request.getParameter("isAuditEdit"));
        BulData bean = null;
        User user = AppContext.getCurrentUser();
        Long userId = user.getId();
        String userName = user.getName();
        String title = request.getParameter("title");
        boolean isPublish = false;
        String idStr = request.getParameter("id");
        int oldState = BulConstants.DATA_STATE_NO_SUBMIT;
        Boolean oldTypeAudit = false;
        if (StringUtils.isBlank(idStr)) {
            bean = new BulData();
        } else {
            bean = bulDataManager.getBulDataCache().getDataCache().get(Long.valueOf(idStr));
            if (bean == null) {
                bean = bulDataManager.getById(Long.valueOf(idStr));
            }
            if (bean == null) {
                super.rendJavaScript(response,
                        "alert('" + ResourceBundleUtil.getString(BulConstants.BUL_RESOURCE_BASENAME, "bul.data.noexist")
                                + "');" + "window.close();");
                return null;
            }
            oldTypeAudit = bean.getType().isAuditFlag();
            //为下面发消息做判断：被修改的公告是未审核、还是审核未通过，以便发送不同的系统消息
            if (bean.getState() == BulConstants.DATA_STATE_ALREADY_CREATE) {
                oldState = BulConstants.DATA_STATE_ALREADY_CREATE;
            } else if (bean.getState() == BulConstants.DATA_STATE_NOPASS_AUDIT) {
                oldState = BulConstants.DATA_STATE_NOPASS_AUDIT;
            } else if (bean.getState() == BulConstants.DATA_STATE_ALREADY_PUBLISH) {
                oldState = BulConstants.DATA_STATE_ALREADY_PUBLISH;
            }
        }
        Long oldTypeId = bean.getTypeId();
        super.bind(request, bean);

        ReplaceBase64Result result = fileManager.replaceBase64Image(bean.getContent());
        bean.setContent(result.getHtml());

        if("true".equals(bean.getShowPublishName()) && bean.getPublishChoose() == 1 && Strings.isNotBlank(bean.getChoosePublshId())){
        	bean.setWritePublish(bean.getChoosePublshId());
        }
        Long typeId = bean.getTypeId();
        boolean flag = (Strings.isNotBlank(spaceId) && bean.getAccountId() == null);
        BulType type = this.bulTypeManager.getById(typeId);
        bean.setType(type);
        String spaceType = request.getParameter("spaceType");
        if(Strings.isNotBlank(spaceType)) {
        	bean.setSpaceType(Integer.parseInt(spaceType));
        } else {
        	bean.setSpaceType(type.getSpaceType());
        }
        bean.setAccountId(type.getAccountId());
        bean.setTitle(title);
        String form_oper = request.getParameter("form_oper");
        if (StringUtils.isNotBlank(form_oper)) {
            if ("draft".equals(form_oper)){
            	  bean.setState(BulConstants.DATA_STATE_NO_SUBMIT);
                  bean.setState(BulConstants.DATA_STATE_NO_SUBMIT);
                  bean.setAuditAdvice(null);
                  bean.setPublishDate(null);
                  bean.setPublishUserId(null);
                  bean.setReadCount(0);
                  bean.setUpdateDate(null);
                  bean.setUpdateUser(null);
            }else if ("submit".equals(form_oper)) {
                //如当前公告类型有审核员，用户点击"发送"按钮时，公告状态设定为"已经提交，还未审核"
                if (type.isAuditFlag()) {
                    bean.setState(BulConstants.DATA_STATE_ALREADY_CREATE);
                } else {
                    //如果当前公告类型无审核员，用户点击"发送"按钮时，即为直接发布公告，公告状态设定为"已经发布，还未归档"
                    //此时将该公告最终审核记录设置为"无审核"
                    bean.setExt3(String.valueOf(BulConstants.AUDIT_RECORD_NO));
                    bean.setState(BulConstants.DATA_STATE_ALREADY_PUBLISH);
                    if(bean.getPublishDate()==null){
                    	bean.setPublishDate(new Timestamp(new Date().getTime()));
                    }
                    bean.setPublishUserId(AppContext.getCurrentUser().getId());
                    isPublish = true;
                }
            }
        } else {
            bean.setState(BulConstants.DATA_STATE_NO_SUBMIT);
            bean.setState(BulConstants.DATA_STATE_NO_SUBMIT);
            bean.setAuditAdvice(null);
            bean.setPublishDate(null);
            bean.setPublishUserId(null);
            bean.setReadCount(0);
            bean.setUpdateDate(null);
            bean.setUpdateUser(null);
        }
        Boolean firstIndexFlag = false;
        if (bean.isNew()) {
            bean.setCreateUser(AppContext.getCurrentUser().getId());
            bean.setTopOrder(Byte.valueOf("0"));
            bean.setCreateDate(new Timestamp(System.currentTimeMillis()));
            firstIndexFlag = true;
        } else {
            if (!oldTypeId.equals(typeId)) {
                bean.setTopOrder(Byte.valueOf("0"));
            }
        }
        //访问记录信息
        if ("1".equals(request.getParameter("recordReadName"))) {
            bean.setExt1("1");// 选中
        } else {
            bean.setExt1("0");// 未选中
        }
        //公开阅读信息 0：不公开1：公开
        if ("1".equals(request.getParameter("openRecordsRead"))) {
            bean.setOpenRecordRead(1);// 选中
        } else {
            bean.setOpenRecordRead(0);// 未选中
        }

        //是否允许打印
        if ("1".equals(request.getParameterValues("printAllow")[0])) {
            bean.setExt2("1");// 选中
        } else {
            bean.setExt2("0");// 未选中
        }
        //是否显示发布人
        String showPublishUserFlag = request.getParameter("showPublish");
        bean.setShowPublishUserFlag(Boolean.valueOf(showPublishUserFlag));
        //由发布人控制选项产生的连锁反应之---发布人到底是谁！！！
        if("true".equals(showPublishUserFlag) && type.getWritePermit()){
        	if(bean.getPublishChoose() == 1){
        		bean.setWritePublish(bean.getChoosePublshId());
        	} else if(bean.getPublishChoose() == 0) {
        		if(type.getFinalPublish() == 2){
        			bean.setPublishUserId(type.getAuditUser());
        		} else {
        			bean.setPublishUserId(userId);
        		}
        	}
        }
        bean.setUpdateDate(new Date());
        bean.setUpdateUser(userId);
        if(bean.getReadCount() == null){
        	bean.setReadCount(0);
        }
        bean.setDataFormat(dataformat);
        bean.setExt5(ext5);
        if (type.isAuditFlag() && (type.getSpaceType() == SpaceType.public_custom.ordinal() || type.getSortNum() == SpaceType.public_custom_group.ordinal())) {
            Long auditId = bean.getType().getAuditUser();
            if (auditId != -1 && Strings.isNotBlank(spaceId)) {
                boolean isAlert = check(Long.parseLong(spaceId), auditId);
                if (isAlert) {
                    response.setContentType("text/html;charset=UTF-8");
                    response.setCharacterEncoding("UTF-8");
                    PrintWriter out = response.getWriter();
                    out.println("<script type='text/javascript'>");
                    out.println("alert("+ ResourceUtil.getString("bulletin.audit.not.reset") +")");
                    out.println("</script>");
                    out.flush();
                    return null;
                }
            }
        }
        //已经提交审核的如果修改，需要发送消息
        boolean editAfterAudit = (oldState == BulConstants.DATA_STATE_ALREADY_CREATE);
        //审核没有通过的修改
        boolean noAuditEdit = (oldState == BulConstants.DATA_STATE_NOPASS_AUDIT);
        //不需要审核的板块发起者可直接修改已发布的公告, 不需要管理员先撤消
        boolean noAuditPublishEdit = (oldState == BulConstants.DATA_STATE_ALREADY_PUBLISH);

        boolean isNew = false;
        String attaFlag = null;
        if (bean.isNew()) {
            bean.setIdIfNew();
            isNew = true;
            long attRefId = Long.valueOf(request.getParameter("attRefId"));
            attachmentManager.deleteByReference(attRefId, attRefId);
            bean.setAttachmentsFlag(false);
            attaFlag = attachmentManager.create(ApplicationCategoryEnum.bulletin, bean.getId(), bean.getId(), request);
        } else {
            attachmentManager.deleteByReference(bean.getId(), bean.getId());
            bean.setAttachmentsFlag(false);
            attaFlag = attachmentManager.create(ApplicationCategoryEnum.bulletin, bean.getId(), bean.getId(), request);
        }

        if (com.seeyon.ctp.common.filemanager.Constants.isUploadLocaleFile(attaFlag))
            bean.setAttachmentsFlag(true);
        if (flag) {
            bulDataManager.saveCustomBul(bean, isNew);
        } else {
            bulDataManager.save(bean, isNew);
        }

        if (bean.getState() == BulConstants.DATA_STATE_ALREADY_PUBLISH) {
            //触发发布事件
            BulletinAddEvent bulletinAddEvent = new BulletinAddEvent(this);
            bulletinAddEvent.setBulDataBO(BulletinUtils.bulDataPOToBO(bean));
            EventDispatcher.fireEvent(bulletinAddEvent);
        }

        if (!isNew && bulDataManager.getBulDataCache().getDataCache().get(bean.getId()) != null) {
            bulDataManager.getBulDataCache().getDataCache().save(bean.getId(), bean, bean.getPublishDate().getTime(),
                    (bean.getReadCount() == null ? 0 : bean.getReadCount()));
        }
        if (isPublish) {
            if (bean.getState() == BulConstants.DATA_STATE_ALREADY_PUBLISH)
                this.bulReadManager.deleteReadByData(bean);
        }

        try {
            if (bean.getState() == BulConstants.DATA_STATE_ALREADY_PUBLISH) {
                if (AppContext.hasPlugin("index")) {
                    if (firstIndexFlag) {
                        indexApi.add(bean.getId(), ApplicationCategoryEnum.bulletin.getKey());
                    } else {
                        indexApi.update(bean.getId(), ApplicationCategoryEnum.bulletin.getKey());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Full-text retrieval:", e);
        }

        //发布不需要经过审核的公告后，发送给公告范围内用户消息
        if (bean.getState().equals(BulConstants.DATA_STATE_ALREADY_PUBLISH)) {
            Set<Long> msgReceiverIds = this.getAllMembersinPublishScope(bean,true);
            this.addAdmins2MsgReceivers(msgReceiverIds, bean);
            this.getBulletinUtils().initDataFlag(bean, true);
            String deptName  = bean.getPublishDeptName();
            userMessageManager.sendSystemMessage(
                    MessageContent.get(noAuditPublishEdit ? "bul.publishEdit" : "bul.auditing", bean.getTitle(),
                    		deptName).setBody(bean.getContent(), bean.getDataFormat(),
                            bean.getCreateDate()), ApplicationCategoryEnum.bulletin, userId, MessageReceiver.getReceivers(
                            bean.getId(), msgReceiverIds, "message.link.bul.alreadyauditing",
                            String.valueOf(bean.getId())), bean.getTypeId());

            //直接发布加日志
            appLogManager.insertLog(user, noAuditPublishEdit ? AppLogAction.Bulletin_Modify
                    : AppLogAction.Bulletin_Publish, userName, bean.getTitle());
          //如果是从有审核的板块切换到无审核的板块，需要删除之前的待办事项
            if (oldTypeAudit) {
            	this.affairManager.deleteByObjectId(ApplicationCategoryEnum.bulletin, bean.getId());
            }

            if (!isNew && !oldTypeId.equals(typeId)) {
                eTagCacheManager.updateETagDate(BulConstants.ETAG_BUL_TYPE, String.valueOf(oldTypeId));
            }
            eTagCacheManager.updateETagDate(BulConstants.ETAG_BUL_TYPE, String.valueOf(bean.getTypeId()));
            eTagCacheManager.updateETagDate(BulConstants.ETAG_BUL_ACCOUNT, String.valueOf(bean.getAccountId()));
            eTagCacheManager.updateETagDate(BulConstants.ETAG_BUL_ACCOUNT, "-1");
        }

        //发送需要经过审核的公告消息消息
        if (bean.getState().equals(BulConstants.DATA_STATE_ALREADY_CREATE) && !isAuditEdit ) {
            Long agentId = AgentUtil.getAgentByApp(type.getAuditUser(), ApplicationCategoryEnum.bulletin.getKey());
            if (editAfterAudit) {
                //提交后修改
                Set<Long> msgReceiverIds = new HashSet<Long>();
                msgReceiverIds.add(bean.getCreateUser());
                this.addAdmins2MsgReceivers(msgReceiverIds, bean);
                userMessageManager.sendSystemMessage(
                        MessageContent.get("bul.edit", bean.getTitle(), userName),
                        ApplicationCategoryEnum.bulletin,
                        userId,
                        MessageReceiver.getReceivers(bean.getId(), msgReceiverIds, "message.link.bul.auditing",
                                String.valueOf(bean.getId())), type.getId());

                if (agentId != null) {//给代理人发消息,后缀(代理)
                    userMessageManager.sendSystemMessage(
                            MessageContent.get("bul.edit", bean.getTitle(), userName).add("col.agent"),
                            ApplicationCategoryEnum.bulletin,
                            userId,
                            MessageReceiver.get(bean.getId(), agentId, "message.link.bul.auditing",
                                    String.valueOf(bean.getId())), type.getId());
                }

                //待审核如果修改加日志
                appLogManager.insertLog(user, AppLogAction.Bulletin_Modify, userName, bean.getTitle());

                // 审核员未进行审核前修改公告并再次发送，需另行增加一条待办事项记录 added by Meng Yang at 2009-07-14
                // 先删除旧有记录，再生成新的记录
                this.affairManager.deleteByObjectId(ApplicationCategoryEnum.bulletin, bean.getId());
                if (bean.getType().isAuditFlag()) {
                	bulDataManager.addPendingAffair(type, bean,ApplicationSubCategoryEnum.bulletin_audit);
                }
            } else if (noAuditEdit) {
                userMessageManager.sendSystemMessage(
                        MessageContent.get("bul.send", bean.getTitle(), userName),
                        ApplicationCategoryEnum.bulletin,
                        userId,
                        MessageReceiver.get(bean.getId(), type.getAuditUser(), "message.link.bul.auditing",
                                String.valueOf(bean.getId())), type.getId());

                if (agentId != null) {//给代理人发消息,后缀(代理)
                    userMessageManager.sendSystemMessage(
                            MessageContent.get("bul.send", bean.getTitle(), userName).add("col.agent"),
                            ApplicationCategoryEnum.bulletin,
                            userId,
                            MessageReceiver.get(bean.getId(), agentId, "message.link.bul.auditing",
                                    String.valueOf(bean.getId())), type.getId());
                }

                //审核不通过修改后再发送至审核加日志
                appLogManager.insertLog(user, AppLogAction.Bulletin_Modify, userName, bean.getTitle());
                if (bean.getType().isAuditFlag()) {
                	bulDataManager.addPendingAffair(type, bean,ApplicationSubCategoryEnum.bulletin_audit);
                }
            } else {
            	bulDataManager.addPendingAffair(type, bean,ApplicationSubCategoryEnum.bulletin_audit);
                userMessageManager.sendSystemMessage(
                        MessageContent.get("bul.send", bean.getTitle(), userName),
                        ApplicationCategoryEnum.bulletin,
                        userId,
                        MessageReceiver.get(bean.getId(), type.getAuditUser(), "message.link.bul.auditing",
                                String.valueOf(bean.getId())), type.getId());

                if (agentId != null) {//给代理人发消息,后缀(代理)
                    userMessageManager.sendSystemMessage(
                            MessageContent.get("bul.send", bean.getTitle(), userName).add("col.agent"),
                            ApplicationCategoryEnum.bulletin,
                            userId,
                            MessageReceiver.get(bean.getId(), agentId, "message.link.bul.auditing",
                                    String.valueOf(bean.getId())), type.getId());
                }

                //新建保存后发送至审核添加日志
                appLogManager.insertLog(user, AppLogAction.Bulletin_New, userName, bean.getTitle());
            }

        } else if (bean.getState().equals(BulConstants.DATA_STATE_NO_SUBMIT)) {
            //保存修改加日志
            appLogManager.insertLog(user, AppLogAction.Bulletin_Modify, userName, bean.getTitle());
        }else if (isAuditEdit && !bean.getCreateUser().equals(AppContext.currentUserId())) {//审核修改后，给发起人发送消息包括代理
            userMessageManager.sendSystemMessage(MessageContent.get("bul.edit", bean.getTitle(), userName),
                    ApplicationCategoryEnum.bulletin, user.getId(),
                    MessageReceiver.get(bean.getId(), bean.getCreateUser(), "message.link.bul.writedetail",String.valueOf(bean.getId())));
            Long agentId = AgentUtil.getAgentByApp(bean.getCreateUser(), ApplicationCategoryEnum.bulletin.getKey());
            if (agentId != null) {//给代理人发消息,后缀(代理)
                MessageReceiver receiver = MessageReceiver.get(bean.getId(), agentId, "message.link.bul.writedetail", String.valueOf(bean.getId()));
                userMessageManager.sendSystemMessage(MessageContent.get("bul.edit",bean.getTitle(), userName)
                        .add("col.agent"), ApplicationCategoryEnum.bulletin, user.getId(),receiver);
            }
        }
        String alertSuccess = isAuditEdit?"alert('"+ResourceBundleUtil.getString(BulConstants.BUL_RESOURCE_BASENAME, "bul.modify.succces")+"');":"";
        super.rendJavaScript(response,alertSuccess + "try{if(window.opener){if (window.opener.getCtpTop().isCtpTop) {} else {window.opener.location.reload();}}}catch(e){}window.close();");
        return null;
    }
    /**
     * 导出公告的阅读信息
     * @param request
     * @param response
     * @throws BusinessException
     */
    @SuppressWarnings("unchecked")
	public void exportReadInfo(HttpServletRequest request, HttpServletResponse response) throws BusinessException{
		// 已读
		String read = ResourceUtil.getString("label.read");
		// 未读
		String noRead = ResourceUtil.getString("label.noread");
		// 部门
		String deptName = ResourceUtil.getString("bul.type.spaceType.1");
		// 阅读情况
		String readState = ResourceUtil.getString("bul.read");
		// 人数
		String memberCount = ResourceUtil.getString("bulletin.export.readInfo.count.label");
		// 人员
		String userName = ResourceUtil.getString("bul.userName");
		// 阅读时间
		String readDate = ResourceUtil.getString("bul.read.readDate");
		// 公告
		String bulLabel = ResourceUtil.getString("bul.label");
		// 导出时间
		String exportTimeLabel = ResourceUtil.getString("bulletin.export.time");
		// 发布时间
		String bulPublishLabel = ResourceUtil.getString("bul.data.publishDate");
		// 阅读信息
		String readInfo = ResourceUtil.getString("bulletin.read.info");
		// 公告阅读信息
		String exportTitle = ResourceUtil.getString("bulletin.export.readInfo");
		DataRecord dataRecord = new DataRecord();
		User user = AppContext.getCurrentUser();
		// 本单位id
		Long accountId = user.getAccountId();
		Long dataId = NumberUtils.toLong(request.getParameter("bulId"));
		BulData bean = bulDataManager.getBulDataCache().getDataCache().get(dataId);
		if (bean == null)
			bean = bulDataManager.getById(dataId);
		List<BulRead> readList = this.bulDataManager.getReadListByData(dataId);
		if (readList != null) {

			//当前登录单位可见的单位
			List<V3xOrgAccount> accAccounts = orgManager.accessableAccountsByUnitId(AppContext.currentAccountId());
			Set<Long> accessAccountIds = new HashSet<Long>();//当前单位可见的单位id
			for(V3xOrgAccount account : accAccounts) {
				if(!account.getIsGroup()) {
					accessAccountIds.add(account.getId());
				}
			}

			//发布范围部门下有哪些人
        	Map<Long,Set<Long>> deptMembers  = new HashMap<Long, Set<Long>>();

        	//发布范围内的部门
        	Set<Long> deptIds = new HashSet<Long>();
        	String publishScope = bean.getPublishScope();
        	String[] publishScopeArr = publishScope.split(",");
        	for(String arr : publishScopeArr) {
        		String[] entity = arr.split("\\|");
        		String type = entity[0];
        		String id = entity[1];
        		if(OrgConstants.ORGENT_TYPE.Department.name().equals(type)) {
        			Long departmentId = Long.valueOf(id);
        			deptIds.add(departmentId);
        			List<V3xOrgDepartment> childDepts = orgManager.getChildDepartments(departmentId, false);
        			for(V3xOrgDepartment dept : childDepts) {
        				deptIds.add(dept.getId());
        			}
        		}else if(OrgConstants.ORGENT_TYPE.Account.name().equals(type)){
        			if(OrgConstants.GROUPID.toString().equals(id)) {
        				List<V3xOrgAccount> accounts = orgManager.getAllAccounts();
        				for(V3xOrgAccount account : accounts) {
        					List<V3xOrgDepartment> departments = orgManager.getAllDepartments(account.getId());
        					for(V3xOrgDepartment department : departments) {
        						if(department.getIsInternal()) {
        							deptIds.add(department.getId());
        						}
        					}
        				}
        				break;
        			} else {
        				List<V3xOrgDepartment> depts = orgManager.getAllDepartments(Long.valueOf(id));
            			for(V3xOrgDepartment dept : depts) {
            				if(dept.getIsInternal()) {
            					Long departmentId = dept.getId();
            					deptIds.add(departmentId);
            				}
            			}
        			}
        		}else if(OrgConstants.ORGENT_TYPE.Post.name().equals(type)){
    		        EnumMap<RelationshipObjectiveName, Object> enummap = new EnumMap<RelationshipObjectiveName, Object>(RelationshipObjectiveName.class);
    		        enummap.put(OrgConstants.RelationshipObjectiveName.objective1Id, Long.valueOf(id));
    				List<V3xOrgRelationship> memberPostRels = orgManager.getV3xOrgRelationship(OrgConstants.RelationshipType.Member_Post, null, null, enummap);
            		for(V3xOrgRelationship rel : memberPostRels) {
            			Long memberId = rel.getSourceId();
            			Long departmentId = rel.getObjective0Id();
            			Long postId = rel.getObjective1Id();
            			//部门岗位信息不完整
            			if(departmentId == null || departmentId == -1L || postId == null || postId ==-1L) {
            				continue;
            			}

            			V3xOrgMember member = orgManager.getMemberById(memberId);
            			if(member == null || !member.isValid()) {
            				continue;
            			}
                		Set<Long> ids;
                		if(deptMembers.containsKey(departmentId)) {
                			ids = deptMembers.get(departmentId);
                		}else {
                			ids = new HashSet<Long>();
                			deptMembers.put(departmentId, ids);
                		}
                		ids.add(memberId);
            		}
        		}else if(OrgConstants.ORGENT_TYPE.Level.name().equals(type)){
    		        EnumMap<RelationshipObjectiveName, Object> enummap = new EnumMap<RelationshipObjectiveName, Object>(RelationshipObjectiveName.class);
    		        enummap.put(OrgConstants.RelationshipObjectiveName.objective2Id, Long.valueOf(id));
    				List<V3xOrgRelationship> memberPostRels = orgManager.getV3xOrgRelationship(OrgConstants.RelationshipType.Member_Post, null, null, enummap);
            		for(V3xOrgRelationship rel : memberPostRels) {
            			Long memberId = rel.getSourceId();
            			Long departmentId = rel.getObjective0Id();
            			Long postId = rel.getObjective1Id();
            			//部门岗位信息不完整
            			if(departmentId == null || departmentId == -1L || postId == null || postId ==-1L) {
            				continue;
            			}

            			V3xOrgMember member = orgManager.getMemberById(memberId);
            			if(member == null || !member.isValid()) {
            				continue;
            			}

                		Set<Long> ids;
                		if(deptMembers.containsKey(departmentId)) {
                			ids = deptMembers.get(departmentId);
                		}else {
                			ids = new HashSet<Long>();
                			deptMembers.put(departmentId, ids);
                		}
                		ids.add(memberId);
            		}
        		}else{
        			Set<V3xOrgMember> members = orgManager.getMembersByTypeAndIds(arr);
        			for(V3xOrgMember member : members) {
        				List<MemberPost> memberPostList = orgManager.getMemberPosts(null, member.getId());
        				for(MemberPost mp : memberPostList) {
        					Long departmentId = mp.getDepId();
        					Long postId = mp.getPostId();
        					Long orgAccountId = mp.getOrgAccountId();
        					if(!accessAccountIds.contains(orgAccountId)) {
        						continue;
        					}
        					if(departmentId == null || postId == null) {
        						continue;
        					}
                    		Set<Long> ids;
                    		if(deptMembers.containsKey(departmentId)) {
                    			ids = deptMembers.get(departmentId);
                    		}else {
                    			ids = new HashSet<Long>();
                    			deptMembers.put(departmentId, ids);
                    		}
                    		ids.add(mp.getMemberId());
        				}
        			}
        		}
        	}

        	for(Long departmentId : deptIds) {
        		Set<Long> ids;
        		if(deptMembers.containsKey(departmentId)) {
        			ids = deptMembers.get(departmentId);
        		}else {
        			ids = new HashSet<Long>();
        			deptMembers.put(departmentId, ids);
        		}
        		List<V3xOrgMember> members = orgManager.getMembersByDepartment(departmentId, true);
        		for(V3xOrgMember member : members) {
        			ids.add(member.getId());
        		}
        	}

            //可见单位下的，发布范围部门下有哪些人
        	List<Long> accessMemberIds = new UniqueList<Long>();// 可见人员
            Map<Long,Set<Long>> visibleMembersByDept = new HashMap<Long, Set<Long>>();
            for(Long departmentId : deptMembers.keySet()) {
            	V3xOrgDepartment dept = orgManager.getDepartmentById(departmentId);
            	if(accessAccountIds.contains(dept.getOrgAccountId())) {
            		Set<Long> memberSet;
            		if(visibleMembersByDept.containsKey(departmentId)) {
            			memberSet = visibleMembersByDept.get(departmentId);
            		}else {
            			memberSet = new HashSet<Long>();
            			visibleMembersByDept.put(departmentId, memberSet);
            		}
            		memberSet.addAll(deptMembers.get(departmentId));
            		accessMemberIds.addAll(deptMembers.get(departmentId));
            	}
            }

            Set<Long> scopeList = this.getAllMembersinPublishScope(bean,false);
        	List<Long> noAccessMmeberIds = (List<Long>) CollectionUtils.subtract(scopeList, accessMemberIds);// 不可见人员

			// 已读的map，key-部门id,value-部门发布范围内的人员id列表
			Map<Long, List<Long>> readMap = new TreeMap<Long, List<Long>>();
			// 人员id与bulData的对应关系map
			Map<Long, BulRead> idToRead = new HashMap<Long, BulRead>();
			// 新闻发起人是否包含在发布范围中(发起人即便不在发布范围中，仍可以阅读新闻并生成阅读记录)
			boolean isCreatorInPublishScope = scopeList.contains(bean.getCreateUser());
			// 有权限未读的map，key-部门id,value-部门发布范围内的人员id列表
			Map<Long, List<Long>> notReadVisibleMap = new TreeMap<Long, List<Long>>();

			Set<Long> readMemberIds = new HashSet<Long>();
			for(BulRead br : readList) {
				readMemberIds.add(br.getManagerId());
			}
			// 将已分组的部门中的人员，筛选出已阅和未读的人数
			for (BulRead br : readList) {
				for(Long departmentId : visibleMembersByDept.keySet()) {//有权限的部门
					for(Long memberId : visibleMembersByDept.get(departmentId)) {
						V3xOrgMember member = orgManager.getMemberById(memberId);
						// 如果阅读信息对应的用户不为正常状态，则不加入，如果阅读信息对应的用户是新闻创建者，而其并不在发布范围中，也不加入
						if (member == null || !member.isValid() || (member.getId().equals(bean.getCreateUser()) && !isCreatorInPublishScope))
							continue;
						if(readMemberIds.contains(memberId)) {
							if (readMap.containsKey(departmentId)) {
								List<Long> readIdsByDept = readMap.get(departmentId);
								if(!readIdsByDept.contains(memberId)) {
									readIdsByDept.add(memberId);
									readMap.put(departmentId, readIdsByDept);
									idToRead.put(memberId, br);
								}
							} else {
								List<Long> readIdsByDept = new ArrayList<Long>();
								readIdsByDept.add(memberId);
								readMap.put(departmentId, readIdsByDept);
								idToRead.put(memberId, br);
							}
						}else {
							if (notReadVisibleMap.containsKey(departmentId)) {
								List<Long> readIdsByDept = notReadVisibleMap.get(departmentId);
								if(!readIdsByDept.contains(memberId)) {
									readIdsByDept.add(memberId);
									notReadVisibleMap.put(departmentId, readIdsByDept);
								}
							} else {
								List<Long> readIdsByDept = new ArrayList<Long>();
								readIdsByDept.add(memberId);
								notReadVisibleMap.put(departmentId, readIdsByDept);
							}
						}

					}
				}
			}

			// 总的已读人员数
			// 循环范围内所有有权限部门
			for (Long deptId : visibleMembersByDept.keySet()) {// 有权限的
				V3xOrgDepartment dept = orgManager.getDepartmentById(deptId);
				// 是否本部门的
				Boolean isSelfDept = true;
				V3xOrgAccount otherAccount = null;
				// 外部门的单位简称
				String shortName = "";
				Long theAccId = dept.getOrgAccountId();
				if (accountId.longValue() != theAccId.longValue()) {// 如果不是本单位
					isSelfDept = false;
					otherAccount = orgManager.getAccountById(theAccId);
					shortName = otherAccount.getShortName();
				}
				List<Long> notReadVisibleMembers = new ArrayList<Long>();
				if(notReadVisibleMap.containsKey(deptId)) {
					notReadVisibleMembers = notReadVisibleMap.get(deptId);
				}
				int notReadVisibleMembersSize = notReadVisibleMembers.size();
				if (readMap.containsKey(deptId)) {// 部门内有已读
					List<Long> readIdListByDept = readMap.get(deptId) != null ? readMap.get(deptId):new ArrayList<Long>();
					// 该部门内未读人员列表
					List<Long> notReadIdListByDept = notReadVisibleMembers;
					// 循环已读的
					int index = 0;
					for(Long readMemId : readIdListByDept) {
						BulRead news = idToRead.get(readMemId);
						V3xOrgMember member = orgManager.getMemberById(readMemId);
						if (news != null) {
							DataRow row = new DataRow();
							if (index == 0 && (notReadVisibleMembersSize + (readIdListByDept != null?readIdListByDept.size():0)) > 1) {// 合并部门
								if (isSelfDept) {// 该部门所有合并（包含已读和未读）
									row.addDataCell(createMergeCell(dept.getName(), 1, (notReadVisibleMembersSize + readIdListByDept.size())- 1));
								} else {
									row.addDataCell(createMergeCell(dept.getName() + "(" + shortName + ")", 1, (notReadVisibleMembersSize + readIdListByDept.size()) - 1));
								}
							} else {
								row.addDataCell(dept.getName(), 1);
							}
							if (index == 0 && readIdListByDept.size() > 1) {// 已读的列合并以及已读人数列合并
								row.addDataCell(createMergeCell(read, 1, readIdListByDept.size() - 1));
								row.addDataCell(createMergeCell(String.valueOf(readIdListByDept.size()), 1, readIdListByDept.size() - 1));
							} else {
                                if (index == 0) {
                                    row.addDataCell(read, 1);
                                }else{
                                    row.addDataCell("", 1);
                                }
								row.addDataCell(String.valueOf(readIdListByDept.size()), 1);
							}
							row.addDataCell(member.getName(), 1);
							row.addDataCell(Datetimes.formatDatetimeWithoutSecond(news.getReadDate()), 1);
							dataRecord.addDataRow(row);
						}
						index++;
					}
					// 循环未读人员列表
					if (CollectionUtils.isNotEmpty(notReadIdListByDept)) {
						index = 0;
						for (Long id : notReadIdListByDept) {
							V3xOrgMember member = orgManager.getMemberById(id);
							if (member != null) {
								DataRow row = new DataRow();
								// 部门全部是空，因为上面已经全部合并了单元格
								if(index == 0 && CollectionUtils.isEmpty(readIdListByDept)) {
									if (isSelfDept) {// 该部门所有合并（包含已读和未读）
										row.addDataCell(createMergeCell(dept.getName(), 1, notReadVisibleMembersSize - 1));
									} else {
										row.addDataCell(createMergeCell(dept.getName() + "(" + shortName + ")", 1, notReadVisibleMembersSize - 1));
									}
								}else {
									row.addDataCell("", 1);
								}
								if (index == 0 && notReadVisibleMembersSize > 1) {// 未读的合并以及未读人数
									row.addDataCell(createMergeCell(noRead, 1, notReadVisibleMembersSize - 1));
									row.addDataCell(createMergeCell(String.valueOf(notReadVisibleMembersSize), 1, notReadVisibleMembersSize - 1));
								} else {
                                    if (index == 0) {
                                        row.addDataCell(noRead, 1);
                                    }else{
                                        row.addDataCell("", 1);
                                    }
                                    row.addDataCell(String.valueOf(notReadVisibleMembersSize), 1);
								}
								row.addDataCell(member.getName(), 1);
								row.addDataCell("", 1);
								dataRecord.addDataRow(row);
							}
							index++;
						}
					}
				} else {// 如果该部门内没有已读人员
					int index = 0;
					for (Long id : notReadVisibleMembers) {
						V3xOrgMember member = orgManager.getMemberById(id);
						if (member != null) {
							DataRow row = new DataRow();
							if (index == 0 && notReadVisibleMembersSize > 1) {// 合并部门
								if (isSelfDept) {// 该部门所有合并（包含已读和未读）
									row.addDataCell(createMergeCell(dept.getName(), 1, notReadVisibleMembersSize - 1));
								} else {
									row.addDataCell(createMergeCell(dept.getName() + "(" + shortName + ")", 1, notReadVisibleMembersSize - 1));
								}
							} else {
                                if (isSelfDept) {// 该部门所有合并（包含已读和未读）
                            		row.addDataCell(dept.getName(), 1);
                                }else{
                            		row.addDataCell(dept.getName() + "(" + shortName + ")", 1);
                                }
							}
							if (index == 0 && notReadVisibleMembersSize > 1) {// 未读的合并
								row.addDataCell(createMergeCell(noRead, 1, notReadVisibleMembersSize - 1));
								row.addDataCell(createMergeCell(String.valueOf(notReadVisibleMembersSize), 1, notReadVisibleMembersSize - 1));
							} else {// 用空的填充
								row.addDataCell(noRead, 1);
								row.addDataCell(String.valueOf(notReadVisibleMembersSize), 1);
							}
							row.addDataCell(member.getName(), 1);
							row.addDataCell("", 1);
							dataRecord.addDataRow(row);
						}
						index++;
					}

				}
			}

			// 循环范围内所有无权限单位
			int invisibleReadCount = readList.size()-idToRead.size();//无权限已读
			invisibleReadCount = invisibleReadCount>0?invisibleReadCount:0;
			int invisibleNotReadCount = noAccessMmeberIds.size()-invisibleReadCount;
			invisibleNotReadCount = invisibleNotReadCount>0?invisibleNotReadCount:0;
			if((invisibleReadCount + invisibleNotReadCount) > 0){//统一设置  - 分级设置    - 可见全部单位不显示星星
				DataRow readRow = new DataRow();
				if (invisibleNotReadCount > 0) {
					if(invisibleNotReadCount > 0) {
						readRow.addDataCell(createMergeCell("***", 1, 1));
					}else {
						readRow.addDataCell("***", 1);
					}
					readRow.addDataCell(read, 1);
					readRow.addDataCell(String.valueOf(invisibleReadCount), 1);
				} else {
					readRow.addDataCell(createMergeCell("***", 1, 1));
					readRow.addDataCell(read, 1);
					readRow.addDataCell(String.valueOf(invisibleNotReadCount), 1);
				}
				readRow.addDataCell("***", 1);
				readRow.addDataCell("", 1);
				dataRecord.addDataRow(readRow);
				DataRow notReadRow = new DataRow();
				notReadRow.addDataCell("***", 1);
				notReadRow.addDataCell(noRead, 1);
				notReadRow.addDataCell(String.valueOf(invisibleNotReadCount), 1);
				notReadRow.addDataCell("***", 1);
				notReadRow.addDataCell("", 1);
				dataRecord.addDataRow(notReadRow);
			}

		}
		String publishTime = Datetimes.formatDatetimeWithoutSecond(bean.getCreateDate());
		String exportTime = Datetimes.formatDatetimeWithoutSecond(new Date());
		String subTitle = exportTimeLabel + ":" + exportTime + "\r\n" + bulPublishLabel + ":" + publishTime+ "\r\n" + ResourceUtil.getString("news.read.tips");
		short height = 1000;
		dataRecord.setSubHeight(height);
		dataRecord.setSubTitle(subTitle);
		dataRecord.setTitle(bulLabel + "《" + bean.getTitle() + "》" + readInfo);
		String[] columnName = { deptName, readState, memberCount, userName, readDate };
		dataRecord.setColumnName(columnName);
		try {
			fileToExcelManager.save(response, exportTitle, dataRecord);
		} catch (Exception e) {
			log.error("Failed to export reading information:", e);
		}
    }
    /**
     * 合并单元格
     * @param content  内容
     * @param cellType 单元格类型
     * @param rowspan 合并的列的数量
     * @return
     */
	public DataCell createMergeCell(String content, int cellType, final int rowspan) {
		DataCell d = new DataCell(content, cellType) {
			public void afterCellRender(Object cell) {
				org.apache.poi.ss.usermodel.Cell c = (org.apache.poi.ss.usermodel.Cell) cell;
				Sheet sheet = c.getSheet();
				sheet.addMergedRegion(new CellRangeAddress(c.getRowIndex(), c.getRowIndex() + rowspan, c.getColumnIndex(), c.getColumnIndex()));
			}
		};
		return d;
	}
	/**
	 * 转换PC上的表单附件下载链接
	 * @param content
	 * @return
	 */
	private String convertFormFileUrl(String content) {
		StringBuffer sb = new StringBuffer();
		Pattern pDiv = Pattern.compile("<div class=\"comp\"[^>]+?comp=\"type:'fileupload',.+?</div>");
		Matcher matcherDiv = pDiv.matcher(content);
		while (matcherDiv.find()) {
			// 匹配的字段
			String temp = matcherDiv.group();
			Pattern dDiv = Pattern.compile("\\{(.*?)\"fileUrl\":\"(.+?)\",\"filename\"(.*?),\"officeTransformEnable\":\"(.*?)\",(.*?)\"v\":(.*?)\\}");
			Matcher dMatcherDiv = dDiv.matcher(temp);
			StringBuffer sb2 = new StringBuffer();
			while (dMatcherDiv.find()) {
				String preString = dMatcherDiv.group(1);
				String fileUrl = dMatcherDiv.group(2);
				String fileName = dMatcherDiv.group(3);
				String group5 = dMatcherDiv.group(5);
				//动态判断附件office附件支持情况
				Attachment att = attachmentManager.getAttachmentByFileURL(Long.valueOf(fileUrl));
				boolean isAllowTrans = OfficeTransHelper.allowTrans(att);
				String officeTransformEnable = isAllowTrans ? "enable" : "disable";
				String replace = "{" + preString + "\"fileUrl\":\"" + fileUrl + "\",\"filename\"" + fileName + ",\"officeTransformEnable\":\""
						+ officeTransformEnable + "\"," + group5 + "\"v\":\"" + SecurityHelper.digest(fileUrl) + "\"}";
				dMatcherDiv.appendReplacement(sb2, replace.toString());
			}
			dMatcherDiv.appendTail(sb2);
			matcherDiv.appendReplacement(sb, sb2.toString());
		}
		matcherDiv.appendTail(sb);
		return sb.toString();
	}

}
