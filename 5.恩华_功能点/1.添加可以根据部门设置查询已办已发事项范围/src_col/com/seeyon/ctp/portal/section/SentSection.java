/**
 * $Author翟锋$
 * $Rev$
 * $Date::2012-11-13$:
 * <p>
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 * <p>
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.portal.section;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.ext.accessSeting.manager.AccessSetingManager;
import com.seeyon.apps.ext.accessSeting.manager.AccessSetingManagerImpl;
import com.seeyon.apps.ext.accessSeting.po.DepartmentViewTimeRange;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.collaboration.enums.ColOpenFrom;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.edoc.api.EdocApi;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.affair.bo.AffairCondition;
import com.seeyon.ctp.common.affair.enums.AffairExtPropEnums;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.manager.PendingManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete.HANDLER_PARAMETER;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete.OPEN_TYPE;
import com.seeyon.ctp.portal.section.templete.MultiRowVariableColumnTemplete;
import com.seeyon.ctp.portal.section.templete.mobile.MListTemplete;
import com.seeyon.ctp.portal.section.util.SectionUtils;
import com.seeyon.ctp.portal.util.PortletPropertyContants.PropertyName;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.manager.ConfigGrantManager;

/**
 * 已发事项栏目
 * @author zhaifeng
 *
 */
public class SentSection extends BaseSectionImpl {
    private static final Log log = LogFactory.getLog(SentSection.class);
    private AffairManager affairManager;
    private EdocApi edocApi;
    private ConfigGrantManager configGrantManager;
    private OrgManager orgManager;
    private ColManager colManager;
    private CommonAffairSectionUtils commonAffairSectionUtils;

    private PendingManager pendingManager;

    public void setPendingManager(PendingManager pendingManager) {
        this.pendingManager = pendingManager;
    }

    public CommonAffairSectionUtils getCommonAffairSectionUtils() {
        return commonAffairSectionUtils;
    }

    public void setCommonAffairSectionUtils(CommonAffairSectionUtils commonAffairSectionUtils) {
        this.commonAffairSectionUtils = commonAffairSectionUtils;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setColManager(ColManager colManager) {
        this.colManager = colManager;
    }

    public ConfigGrantManager getConfigGrantManager() {
        return configGrantManager;
    }

    public void setConfigGrantManager(ConfigGrantManager configGrantManager) {
        this.configGrantManager = configGrantManager;
    }

    public EdocApi getEdocApi() {
        return edocApi;
    }

    public void setEdocApi(EdocApi edocApi) {
        this.edocApi = edocApi;
    }

    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

    @Override
    public String getId() {
        return "sentSection";
    }

    @Override
    public boolean isAllowUsed() {
        User user = AppContext.getCurrentUser();
        if (AppContext.isGroupAdmin()) {
            return false;
        }
        if (user.isV5Member()) {
            return true;
        } else {
            return AppContext.isAdmin() || AppContext.hasResourceCode("F01_listSent");
        }
    }

    @Override
    public void init() {
        super.init();
        if (AppContext.hasPlugin("edoc")) {
            return;
        }

        //不展示公文相关配置信息
        List<SectionProperty> properties = this.getProperties();
        for (SectionProperty sp : properties) {
            SectionReference[] references = sp.getReference();
            for (SectionReference ref : references) {
                if ("rowList".equals(ref.getName())) {
                    SectionReferenceValueRange[] valueRanges = ref.getValueRanges();
                    List<SectionReferenceValueRange> result = new ArrayList<SectionReferenceValueRange>();
                    for (SectionReferenceValueRange val : valueRanges) {
                        if (!"edocMark".equals(val.getValue())) {
                            result.add(val);
                        }
                    }
                    ref.setValueRanges(result.toArray(new SectionReferenceValueRange[0]));
                }
            }
        }
    }

    @Override
    public String getBaseName() {
        return ResourceUtil.getString("common.my.sent.title");
    }

    @Override
    public String getBaseNameI18nKey() {
        return "common.my.sent.title";
    }

    @Override
    public String getName(Map<String, String> preference) {
        //栏目显示的名字，必须实现国际化，在栏目属性的“columnsName”中存储
        String name = preference.get("columnsName");
        if (Strings.isBlank(name)) {
            return ResourceUtil.getString("common.my.sent.title");//已发事项
        } else {
            return name;
        }
    }

    @Override
    public Integer getTotal(Map<String, String> preference) {
        return null;
    }

    @Override
    public String getIcon() {
        return "sent";
    }

    @Override
    public BaseSectionTemplete projection(Map<String, String> preference) {
        String rowStr = preference.get("rowList");
        //发起时间、处理期限是必须显示的，而不是通过配置的，同时为了保证顺序问题，因此要如此处理(将deadline放到category前面)
        if (Strings.isBlank(rowStr)) {
            rowStr = "subject,publishDate,category";
        }

        AffairCondition condition = new AffairCondition();
        FlipInfo fi = new FlipInfo();
        fi.setNeedTotal(false);
        MultiRowVariableColumnTemplete c = new MultiRowVariableColumnTemplete();
        int count = c.getPageSize(preference)[0];
        //单列表
        fi.setSize(count);
        //OA-27551首页事项中去除已发已办栏目名称后面的数字显示
        fi.setNeedTotal(false);
        List<CtpAffair> affairs = new ArrayList<CtpAffair>();
        List<CtpAffair> newAffairs = new ArrayList<>();

        try {
            affairs = pendingManager.querySectionAffair(condition, fi, preference, ColOpenFrom.listSent.name(), new HashMap<String, String>(), false);

            //【恩华药业】zhou:协同过滤掉设定范围内的数据【开始】
            AccessSetingManager manager = new AccessSetingManagerImpl();
            for (CtpAffair affair : affairs) {
                if (affair.getApp() == 1) {
                    Long senderId = affair.getSenderId();
                    V3xOrgMember member = orgManager.getMemberById(senderId);
                    Long userId = member.getId();
                    Map<String, Object> map = new HashMap<>();
                    map.put("memberId", userId);
                    List<DepartmentViewTimeRange> list = manager.getDepartmentViewTimeRange(map);
                    if (list.size() > 0) {
                        DepartmentViewTimeRange range = list.get(0);
                        if (range.getDayNum() > 0) {
                            LocalDateTime end = LocalDateTime.now();
                            LocalDateTime start = LocalDateTime.now().minusDays(range.getDayNum());
                            Long startTime = start.toInstant(ZoneOffset.of("+8")).toEpochMilli();
                            Long endTime = end.toInstant(ZoneOffset.of("+8")).toEpochMilli();
                            Long objectId = affair.getObjectId();
                            ColSummary colSummary = colManager.getColSummaryById(objectId);
                            Date createDate = colSummary.getCreateDate();
                            if (startTime.longValue() != 0l && endTime.longValue() != 0l) {
                                if (createDate.getTime() > startTime.longValue() && createDate.getTime() < endTime.longValue()) {
                                    newAffairs.add(affair);
                                }
                            }
                        }else{
                            newAffairs.add(affair);
                        }
                    } else {
                        newAffairs.add(affair);
                    }
                } else {
                    newAffairs.add(affair);
                }
            }

            //【恩华药业】zhou:协同过滤掉设定范围内的数据【结束】
        } catch (BusinessException e1) {
            log.error("获取已发事项报错:", e1);
        }

        //String s =   Functions.escapeJavascript(Functions.toHTML(Functions.toHTML(this.getName(preference) .replaceAll("#", "%25").replaceAll("&", "%23").replaceAll("=", "%3D"))));
        String s = "";
        try {
            s = URLEncoder.encode(this.getName(preference), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("", e);
        }
        //单列表
        //zhou:修改第二个参数
        c = this.getTemplete(c, newAffairs, preference);
        // 【更多】

        c.addBottomButton(BaseSectionTemplete.BOTTOM_BUTTON_LABEL_MORE,
                "/portalAffair/portalAffairController.do?method=moreSent" + "&fragmentId="
                        + preference.get(PropertyName.entityId.name()) + "&ordinal="
                        + preference.get(PropertyName.ordinal.name()) + "&rowStr=" + rowStr + "&columnsName=" + s);
        c.setDataNum(count);
        return c;
    }

    @Override
    public BaseSectionTemplete mProjection(Map<String, String> preference) {
        User user = AppContext.getCurrentUser();
        FlipInfo fi = new FlipInfo();
        AffairCondition condition = new AffairCondition();
        Integer count = SectionUtils.getSectionCount(3, preference);
        fi.setSize(count);
        fi.setNeedTotal(false);

        //condition.setVjoin(true);//移动端只查询协同数据

        List<CtpAffair> affairs = new ArrayList<CtpAffair>();
        try {
            affairs = pendingManager.querySectionAffair(condition, fi, preference, ColOpenFrom.listSent.name(), new HashMap<String, String>(), false);
        } catch (BusinessException e1) {
            log.error("获取已发栏目数据报错:", e1);
        }
        MListTemplete c = new MListTemplete();
        if (Strings.isNotEmpty(affairs)) {
            for (CtpAffair affair : affairs) {
                MListTemplete.Row row = c.addRow();
                String subject = affair.getSubject();
                if (affair.getAutoRun() != null && affair.getAutoRun()) {
                    subject = ResourceUtil.getString("collaboration.newflow.fire.subject", subject);
                }
                row.setSubject(subject.replaceAll("\\r\\n", ""));
                //设置重要程度图标
                if (affair.getImportantLevel() != null && affair.getImportantLevel() > 1 && affair.getImportantLevel() < 6) {
                    //3:非常重要 2重要
                    row.setIcon("important-" + affair.getImportantLevel());
                }
                ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(affair.getApp());
                switch (appEnum) {
                    case collaboration:
                        row.setLink("/seeyon/m3/apps/v5/collaboration/html/details/summary.html?affairId=" + affair.getId()
                                + "&openFrom=listSent&VJoinOpen=VJoin&summaryId=" + affair.getObjectId() + "&r="
                                + System.currentTimeMillis());
                        break;
                    case edoc:
                        ApplicationSubCategoryEnum subAppEnum = ApplicationSubCategoryEnum.valueOf(affair.getApp(), affair.getSubApp());
                        switch (subAppEnum) {
                            case edoc_fawen://G6新公文发文
                            case edoc_qianbao://G6新公文签报
                            case edoc_shouwen://G6新公文收文
                            case edoc_jiaohuan://G6新公文交换
                                row.setLink("/seeyon/m3/apps/v5/edoc/html/details/summary.html?affairId=" + affair.getId()
                                        + "&openFrom=listSent&VJoinOpen=VJoin&summaryId=" + affair.getObjectId() + "&r="
                                        + System.currentTimeMillis());
                                break;
                            case old_edocSend://G6老公文发文
                            case old_edocRec://G6老公文收文
                            case old_edocSign://G6老公文签报
                                row.setLink("/seeyon/m3/apps/v5/edoc/html/edocSummary.html?affairId=" + affair.getId()
                                        + "&openFrom=listSent&VJoinOpen=VJoin&summaryId=" + affair.getObjectId() + "&r="
                                        + System.currentTimeMillis());
                                break;
                            default:
                                row.setLink("noSupport");
                                break;
                        }
                        break;
                    case meeting:
                        row.setLink("/seeyon/m3/apps/v5/meeting/html/meetingDetail.html?meetingId=" + affair.getObjectId());
                        break;
                    case meetingroom:
                        row.setLink("/seeyon/m3/apps/v5/meeting/html/meetingRoomApprove.html?openFrom=mrAuditList&roomAppId=" + affair.getObjectId());
                        break;
                    default:
                        row.setLink("noSupport");
                        break;
                }
                row.setCreateDate(MListTemplete.showDate(affair.getCreateDate()));
                String memberName = Functions.showMemberName(affair.getSenderId());
                if (memberName == null && (affair.getSenderId() == null || affair.getSenderId() == -1)) {
                    memberName = Strings.escapeNULL(affair.getExtProps(), "");
                }
                row.setReadFlag("true");
                row.setState(affair.getSummaryState() == null ? "0" : affair.getSummaryState().toString());
                row.setCreateMemberId(affair.getSenderId().toString());
                row.setHasAttachments(AffairUtil.isHasAttachments(affair));
            }
        }
        String moreLink = "";
        if (user.isVJoinMember()) {
            moreLink = "/seeyon/m3/apps/v5/collaboration/html/colAffairs.html?openFrom=listSent&VJoinOpen=VJoin&r=" + System.currentTimeMillis();
        } else {
            String entityId = preference.get("entityId");// entityId
            String ordinal = preference.get("ordinal");
            moreLink = "/seeyon/m3/apps/m3/todo/layout/todo-list.html?openFrom=listSent&entityId=" + entityId + "&ordinal=" + ordinal + "&VJoinOpen=VJoin";
        }
        c.setMoreLink(moreLink);
        return c;
    }

    /**
     * 获得列表模版
     * @param affairs
     * @return
     */
    private MultiRowVariableColumnTemplete getTemplete(MultiRowVariableColumnTemplete c, List<CtpAffair> affairs, Map<String, String> preference) {
        User user = AppContext.getCurrentUser();
        String widthStr = preference.get("width");
        int width = 10;
        if (Strings.isNotBlank(widthStr)) {
            width = Integer.valueOf(widthStr);
        }
        //显示列
        String rowStr = preference.get("rowList");
        if (Strings.isBlank(rowStr)) {//门户新增当前待办人默认勾选
            if (user.isV5Member()) {
                rowStr = "subject,publishDate,category";
            } else {
                rowStr = "subject,publishDate,currentNodesInfo";
            }
        }
        String[] rows = rowStr.split(",");
        List<String> list = Arrays.asList(rows);
        //判断是否选择‘标题’
        boolean isSubject = list.contains("subject");
        //判断是否选择‘发起时间’
        boolean isCreateDatee = list.contains("publishDate");
        //判断是否选择'公文文号'
        boolean isEdocMark = list.contains("edocMark");
        //判断是否选择‘分类’
        boolean isCategory = list.contains("category");
        //判断是否选择‘当前待办人’
        boolean isCurrentNodesInfo = list.contains("currentNodesInfo");
        //判断是否选择‘催办’
        boolean isHastenInfo = list.contains("hastenInfo");

        Boolean isGov = (Boolean) (SysFlag.is_gov_only.getFlag());
        if (isGov == null) {
            isGov = false;
        }
        boolean hasInfoReportGrant = false;
        //默认为8条记录
        int count = 8;
        String coun = preference.get("count");
        if (Strings.isNotBlank(coun)) {
            count = Integer.parseInt(coun);
        }
        count = affairs.size();
        Map<Long, String> currentNodeInfos = commonAffairSectionUtils.parseCurrentNodeInfos(affairs);
        for (int i = 0; i < count; i++) {
            MultiRowVariableColumnTemplete.Row row = c.addRow();
            //标题
            MultiRowVariableColumnTemplete.Cell subjectCell = null;
            //发起时间
            MultiRowVariableColumnTemplete.Cell createDateCell = null;
            //公文文号
            MultiRowVariableColumnTemplete.Cell edocMarkCell = null;
            //分类
            MultiRowVariableColumnTemplete.Cell categoryCell = null;
            //当前待办人
            MultiRowVariableColumnTemplete.Cell currentNodesInfoCell = null;
            //按钮
            MultiRowVariableColumnTemplete.Cell buttonCell = null;

            if (isSubject) {
                subjectCell = row.addCell();
                subjectCell.setCellWidth(100);
                int cellWidth = 50;
                if (rows.length == 3) {
                    cellWidth = 65;
                } else if (rows.length == 2) {
                    cellWidth = 90;
                } else if (rows.length == 1) {
                    cellWidth = 100;
                }
                subjectCell.setCellContentWidth(cellWidth);
            }
            if (isCurrentNodesInfo) {
                currentNodesInfoCell = row.addCell();
            }
            if (isCreateDatee) {
                createDateCell = row.addCell();
            }
            if (isEdocMark) {
                edocMarkCell = row.addCell();
            }
            if (isCategory) {
                categoryCell = row.addCell();
            }
            if (isHastenInfo) {
                buttonCell = row.addCell();
            }
            if (affairs == null || affairs.size() < 1) {
                continue;
            }
            if (i < affairs.size()) {
                CtpAffair affair = affairs.get(i);
                String currentNodesInfoStr = currentNodeInfos.get(affair.getObjectId());
                if (Strings.isNotBlank(currentNodesInfoStr) && isCurrentNodesInfo) {
                    String currentInfo = Strings.getSafeLimitLengthString(currentNodesInfoStr, 10, "..");
                    if (Integer.valueOf(StateEnum.col_sent.getKey()).equals(affair.getState()) && !affair.isFinish() && !ResourceUtil.getString("collaboration.list.finished.label").equals(currentNodesInfoStr)) {
                        currentInfo = ResourceUtil.getString("collaboration.pending.CreateMember.label", currentInfo);//待XX处理
                    }
                    currentInfo = "<span title='" + currentNodesInfoStr + "' >" + currentInfo + "</span>";
                    currentNodesInfoCell.setCellContentHTML(currentInfo);
                }
                if (isHastenInfo && Integer.valueOf(StateEnum.col_sent.getKey()).equals(affair.getState()) && !affair.isFinish() && !ResourceUtil.getString("collaboration.list.finished.label").equals(currentNodesInfoStr)) {

                    String hastenUrl = SystemEnvironment.getContextPath() + "/hasten.do?method=openHastenDialog&openFrom=send&affairId=" + affair.getId() + "&memberId=" + affair.getMemberId();
                    Map<String, Map<String, String>> buttonHandler = new HashMap<String, Map<String, String>>();
                    Map<String, String> clickHandler = new HashMap<String, String>();
                    clickHandler.put(HANDLER_PARAMETER.name.name(), "showHastenDialog");
                    clickHandler.put(HANDLER_PARAMETER.parameter.name(), hastenUrl);
                    buttonHandler.put(OPEN_TYPE.click.name(), clickHandler);
                    buttonCell.setCellContentHTML(ResourceUtil.getString("common.portal.section.affair.remind.label"));
                    buttonCell.setHandler(buttonHandler);
                    buttonCell.setOpenType(OPEN_TYPE.dialog);
                    Map<String, String> custom = new HashMap<String, String>();
                    custom.put(HANDLER_PARAMETER.type.name(), "button");
                    buttonCell.setCustomParameter(custom);
                }
                String url = "";
                String forwardMember = affair.getForwardMember();
                Integer resentTime = affair.getResentTime();
                String subject = WFComponentUtil.showSubjectOfAffair(affair, false, -1).replaceAll("\r\n", "").replaceAll("\n",
                        "");
                if (isSubject) {
//	                OA-175634首页栏目中，已发事项中自动发起的事项有两个自动发起
//                    if (affair.getAutoRun() != null && affair.getAutoRun()) {
////                        subject = ResourceUtil.getString("collaboration.newflow.fire.subject", subject);
////                    }
                    subjectCell.setAlt(WFComponentUtil.mergeSubjectWithForwardMembers(affair.getSubject(), forwardMember,
                            resentTime, null, -1));
                    //设置重要程度图标
                    if (affair.getImportantLevel() != null && affair.getImportantLevel() > 1
                            && affair.getImportantLevel() < 6) {
                        subjectCell.addExtPreClasses("ico16 important" + affair.getImportantLevel() + "_16");
                    }
                    //设置附件图标
                    if (AffairUtil.isHasAttachments(affair)) {
                        subjectCell.addExtClasses("ico16 vp-attachment");
                    }
                    //表单授权
                    if (AffairUtil.getIsRelationAuthority(affair)) {
                        subjectCell.addExtClasses("ico16 authorize_16");
                    }
                    //流程状态
                    if (Integer.valueOf(CollaborationEnum.flowState.finish.ordinal())
                            .equals(affair.getSummaryState())) {
                        subjectCell.addExtPreClasses("ico16 flow3_16");
                    } else if (Integer.valueOf(CollaborationEnum.flowState.terminate.ordinal())
                            .equals(affair.getSummaryState())) {
                        subjectCell.addExtPreClasses("ico16 flow1_16");
                    }
                    //设置正文类型图标
                    if (affair.getBodyType() != null && !"10".equals(affair.getBodyType())
                            && !"30".equals(affair.getBodyType()) && !"HTML".equals(affair.getBodyType())) {
                        String bodyType = affair.getBodyType();
                        String bodyTypeClass = convertPortalBodyType(bodyType);
                        if (!"html_16".equals(bodyTypeClass) && !"meeting_video_16".equals(bodyTypeClass)) {
                            subjectCell.addExtClasses("ico16 office" + bodyTypeClass);
                        }
                    }

                    subjectCell.setCellContent(subject.replaceAll("\\r\\n", ""));
                }
                int app = affair.getApp();
                String categoryName = ResourceUtil.getString("application." + app + ".label");
                ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(app);
                String from = null;
                switch (StateEnum.valueOf(affair.getState())) {
                    case col_sent:
                        from = "listSent";
                        break;
                    case col_pending:
                        from = "listPending";
                        break;
                    case col_done:
                        from = "listDone";
                        break;
                    default:
                        from = "listDone";
                }
                switch (appEnum) {
                    case collaboration:
                        if (subjectCell != null) {
                            subjectCell.setLinkURL(
                                    "/collaboration/collaboration.do?method=summary&openFrom=listSent&affairId="
                                            + affair.getId());
                        }
                        if (categoryCell != null) {
                            //判断是否有资源菜单权限
                            if (WFComponentUtil.checkByReourceCode("F01_listSent")) {
                                url = "/collaboration/collaboration.do?method=listSent";
                                categoryCell.setOpenType(OPEN_TYPE.openWorkSpace);
                                Map<String, Map<String, String>> categoryHandler = new HashMap<String, Map<String, String>>();
                                Map<String, String> clickHandler = new HashMap<String, String>();
                                clickHandler.put(HANDLER_PARAMETER.name.name(), "open_link");
                                clickHandler.put(HANDLER_PARAMETER.parameter.name(), url);
                                categoryHandler.put(OPEN_TYPE.click.name(), clickHandler);
                                categoryCell.setHandler(categoryHandler);
                            }
                            categoryCell.setCellContentHTML(categoryName);
                        }
                        break;
                    case edoc://G6公文
                        ApplicationSubCategoryEnum subAppEnum = ApplicationSubCategoryEnum.valueOf(app, affair.getSubApp());
                        switch (subAppEnum) {
                            case edoc_fawen://G6新公文发文
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/govdoc/govdoc.do?method=summary&openFrom=listSent&affairId=" + affair.getId() + "&app=" + affair.getApp() + "&summaryId=" + affair.getObjectId());
                                }
                                if (categoryCell != null) {
                                    //分类链接：判断是否有公文管理-待办列表，若有则链接到发文管理-已发列表，若没有则链接到公文管理-已发列表
                                    boolean f11 = user.hasResourceCode("F20_govDocSendManage");
                                    url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&govdocType=1&listType=listSent&_resourceCode=F20_govDocSendManage";
                                    if (!f11) {
                                        f11 = user.hasResourceCode("F20_gocDovSend");
                                        url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&listType=listSentAllRoot&_resourceCode=F20_gocDovSend";
                                    }
                                    categoryName = ResourceUtil.getString("govdoc.edocSend.label");
                                    if (f11) {
                                        Map<String, Map<String, String>> categoryHandler = new HashMap<String, Map<String, String>>();
                                        Map<String, String> clickHandler = new HashMap<String, String>();
                                        clickHandler.put(HANDLER_PARAMETER.name.name(), "open_link");
                                        clickHandler.put(HANDLER_PARAMETER.parameter.name(), url);
                                        categoryHandler.put(OPEN_TYPE.click.name(), clickHandler);
                                        categoryCell.setHandler(categoryHandler);
                                        categoryCell.setOpenType(OPEN_TYPE.openWorkSpace);
                                    }
                                    categoryCell.setCellContentHTML(categoryName);
                                }
                                break;
                            case edoc_qianbao://G6新公文签报
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/govdoc/govdoc.do?method=summary&openFrom=listSent&affairId=" + affair.getId() + "&app=" + affair.getApp() + "&summaryId=" + affair.getObjectId());
                                }
                                if (categoryCell != null) {
                                    boolean f11 = user.hasResourceCode("F20_signReport");
                                    url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&govdocType=3&listType=listSent&_resourceCode=F20_signReport";
                                    if (!f11) {
                                        f11 = user.hasResourceCode("F20_gocDovSend");
                                        url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&listType=listSentAllRoot&app=4&_resourceCode=F20_gocDovSend";
                                    }
                                    categoryName = ResourceUtil.getString("govdoc.edocSign.label");
                                    if (f11) {
                                        Map<String, Map<String, String>> categoryHandler = new HashMap<String, Map<String, String>>();
                                        Map<String, String> clickHandler = new HashMap<String, String>();
                                        clickHandler.put(HANDLER_PARAMETER.name.name(), "open_link");
                                        clickHandler.put(HANDLER_PARAMETER.parameter.name(), url);
                                        categoryHandler.put(OPEN_TYPE.click.name(), clickHandler);
                                        categoryCell.setHandler(categoryHandler);
                                        categoryCell.setOpenType(OPEN_TYPE.openWorkSpace);
                                    }
                                    categoryCell.setCellContentHTML(categoryName);
                                }
                                break;
                            case edoc_shouwen://G6新公文收文
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/govdoc/govdoc.do?method=summary&openFrom=listSent&affairId=" + affair.getId() + "&app=" + affair.getApp() + "&summaryId=" + affair.getObjectId());
                                }
                                if (categoryCell != null) {
                                    boolean f11 = user.hasResourceCode("F20_receiveManage");
                                    url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&govdocType=2,4&listType=listSent&_resourceCode=F20_receiveManage";
                                    if (!f11) {
                                        f11 = user.hasResourceCode("F20_gocDovSend");
                                        url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&listType=listSentAllRoot&_resourceCode=F20_gocDovSend";
                                    }
                                    categoryName = ResourceUtil.getString("govdoc.edocRec.label");
                                    if (f11) {
                                        Map<String, Map<String, String>> categoryHandler = new HashMap<String, Map<String, String>>();
                                        Map<String, String> clickHandler = new HashMap<String, String>();
                                        clickHandler.put(HANDLER_PARAMETER.name.name(), "open_link");
                                        clickHandler.put(HANDLER_PARAMETER.parameter.name(), url);
                                        categoryHandler.put(OPEN_TYPE.click.name(), clickHandler);
                                        categoryCell.setHandler(categoryHandler);
                                        categoryCell.setOpenType(OPEN_TYPE.openWorkSpace);
                                    }
                                    categoryCell.setCellContentHTML(categoryName);
                                }
                                break;
                            case edoc_jiaohuan://G6新公文交换
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/govdoc/govdoc.do?method=summary&openFrom=listSent&affairId=" + affair.getId() + "&app=" + affair.getApp() + "&summaryId=" + affair.getObjectId());
                                }
                                if (categoryCell != null) {
                                    boolean f11 = user.hasResourceCode("F20_receiveManage");
                                    url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&govdocType=2,4&listType=listSent&_resourceCode=F20_receiveManage";
                                    if (!f11) {
                                        f11 = user.hasResourceCode("F20_gocDovSend");
                                        url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&listType=listSentAllRoot&_resourceCode=F20_gocDovSend";
                                    }
                                    categoryName = ResourceUtil.getString("govdoc.edocRec.label");
                                    if (f11) {
                                        Map<String, Map<String, String>> categoryHandler = new HashMap<String, Map<String, String>>();
                                        Map<String, String> clickHandler = new HashMap<String, String>();
                                        clickHandler.put(HANDLER_PARAMETER.name.name(), "open_link");
                                        clickHandler.put(HANDLER_PARAMETER.parameter.name(), url);
                                        categoryHandler.put(OPEN_TYPE.click.name(), clickHandler);
                                        categoryCell.setHandler(categoryHandler);
                                        categoryCell.setOpenType(OPEN_TYPE.openWorkSpace);
                                    }
                                    categoryCell.setCellContentHTML(categoryName);
                                }
                                break;
                            case old_edocSend://G6老公文发文
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/edocController.do?method=detailIFrame&from=" + from + "&affairId=" + affair.getId() + "");
                                }
                                if (categoryCell != null) {
                                    //分类链接：判断是否有公文管理-待办列表，若有则链接到发文管理-已发列表，若没有则链接到公文管理-已发列表
                                    boolean f11 = user.hasResourceCode("F20_govDocSendManage");
                                    url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&govdocType=1&listType=listSent&_resourceCode=F20_govDocSendManage";
                                    if (!f11) {
                                        f11 = user.hasResourceCode("F20_gocDovSend");
                                        url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&listType=listSentAllRoot&_resourceCode=F20_gocDovSend";
                                    }
                                    categoryName = ResourceUtil.getString("govdoc.edocSend.label");
                                    if (f11) {
                                        Map<String, Map<String, String>> categoryHandler = new HashMap<String, Map<String, String>>();
                                        Map<String, String> clickHandler = new HashMap<String, String>();
                                        clickHandler.put(HANDLER_PARAMETER.name.name(), "open_link");
                                        clickHandler.put(HANDLER_PARAMETER.parameter.name(), url);
                                        categoryHandler.put(OPEN_TYPE.click.name(), clickHandler);
                                        categoryCell.setHandler(categoryHandler);
                                        categoryCell.setOpenType(OPEN_TYPE.openWorkSpace);
                                    }
                                    categoryCell.setCellContentHTML(categoryName);
                                }
                                break;
                            case old_edocRec://G6老公文收文
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/edocController.do?method=detailIFrame&from=" + from + "&affairId=" + affair.getId() + "");
                                }
                                if (categoryCell != null) {
                                    boolean f11 = user.hasResourceCode("F20_receiveManage");
                                    url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&govdocType=2,4&listType=listSent&_resourceCode=F20_receiveManage";
                                    if (!f11) {
                                        f11 = user.hasResourceCode("F20_gocDovSend");
                                        url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&listType=listSentAllRoot&_resourceCode=F20_gocDovSend";
                                    }
                                    categoryName = ResourceUtil.getString("govdoc.edocRec.label");
                                    if (f11) {
                                        Map<String, Map<String, String>> categoryHandler = new HashMap<String, Map<String, String>>();
                                        Map<String, String> clickHandler = new HashMap<String, String>();
                                        clickHandler.put(HANDLER_PARAMETER.name.name(), "open_link");
                                        clickHandler.put(HANDLER_PARAMETER.parameter.name(), url);
                                        categoryHandler.put(OPEN_TYPE.click.name(), clickHandler);
                                        categoryCell.setHandler(categoryHandler);
                                        categoryCell.setOpenType(OPEN_TYPE.openWorkSpace);
                                    }
                                    categoryCell.setCellContentHTML(categoryName);
                                }
                                break;
                            case old_edocSign://G6老公文签报
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/edocController.do?method=detailIFrame&from=" + from + "&affairId=" + affair.getId() + "");
                                }
                                if (categoryCell != null) {
                                    boolean f11 = user.hasResourceCode("F20_signReport");
                                    url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&govdocType=3&listType=listSent&_resourceCode=F20_signReport";
                                    if (!f11) {
                                        f11 = user.hasResourceCode("F20_gocDovSend");
                                        url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&listType=listSentAllRoot&app=4&_resourceCode=F20_gocDovSend";
                                    }
                                    categoryName = ResourceUtil.getString("govdoc.edocSign.label");
                                    if (f11) {
                                        Map<String, Map<String, String>> categoryHandler = new HashMap<String, Map<String, String>>();
                                        Map<String, String> clickHandler = new HashMap<String, String>();
                                        clickHandler.put(HANDLER_PARAMETER.name.name(), "open_link");
                                        clickHandler.put(HANDLER_PARAMETER.parameter.name(), url);
                                        categoryHandler.put(OPEN_TYPE.click.name(), clickHandler);
                                        categoryCell.setHandler(categoryHandler);
                                        categoryCell.setOpenType(OPEN_TYPE.openWorkSpace);
                                    }
                                    categoryCell.setCellContentHTML(categoryName);
                                }
                                break;
                            case old_exSend://G6老公文交换(老公文交换只展示待办数据)
                                break;
                            case old_exSign://G6老公文签收(老公文交换只展示待办数据)
                                break;
                            case old_edocRegister://G6老公文登记
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/edocController.do?method=detailIFrame&from=" + from + "&affairId=" + affair.getId() + "");
                                }
                                break;
                            case old_edocRecDistribute://G6老公文分发
                                break;
                        }
                        getEdocExtField(affair, edocMarkCell, width);
                        break;
                    case info:
                        if (subjectCell != null) {
                            subjectCell.setLinkURL("/infoDetailController.do?method=detail&summaryId="
                                    + affair.getObjectId() + "&from=" + from + "&affairId=" + affair.getId() + "");
                        }
                        if (categoryCell != null) {
                            if (configGrantManager != null) {
                                hasInfoReportGrant = configGrantManager.hasConfigGrant(user.getLoginAccount(),
                                        user.getId(), "info_config_grant", "info_config_grant_report");
                                if (hasInfoReportGrant) {
                                    url = "/infoNavigationController.do?method=indexManager&entry=infoReport&toFrom=listInfoReported&affairId="
                                            + affair.getObjectId();
                                }
                            }
                            Map<String, Map<String, String>> categoryHandler = new HashMap<String, Map<String, String>>();
                            Map<String, String> clickHandler = new HashMap<String, String>();
                            clickHandler.put(HANDLER_PARAMETER.name.name(), "open_link");
                            clickHandler.put(HANDLER_PARAMETER.parameter.name(), url);
                            categoryHandler.put(OPEN_TYPE.click.name(), clickHandler);
                            categoryCell.setHandler(categoryHandler);
                            categoryCell.setOpenType(OPEN_TYPE.openWorkSpace);
                            categoryCell.setCellContentHTML(categoryName);
                        }
                        break;
                    default:
                        break;
                }
                if (createDateCell != null) {
                    String dateTime = WFComponentUtil.getDateTime(affair.getCreateDate(), "yyyy-MM-dd HH:mm");
                    createDateCell.setCellContentHTML(
                            "<span class='color_gray' title='" + dateTime + "'>" + dateTime + "</span>");
                }
                //当前处理人
//                List<Integer> edocApps = new ArrayList<Integer>();
//                edocApps.add(ApplicationCategoryEnum.edocSend.getKey());//发文 19
//                edocApps.add(ApplicationCategoryEnum.edocRec.getKey());//收文 20
//                edocApps.add(ApplicationCategoryEnum.edocSign.getKey());//签报21
//                edocApps.add(ApplicationCategoryEnum.exSend.getKey());//待发送公文22
//                edocApps.add(ApplicationCategoryEnum.exSign.getKey());//待签收公文 23
//                edocApps.add(ApplicationCategoryEnum.edocRegister.getKey());//待登记公文 24
//                edocApps.add(ApplicationCategoryEnum.edocRecDistribute.getKey());//收文分发34


            }
        }
        return c;
    }

    /**
     * 取出公文扩展字段
     * @param affair CtpAffair对象
     * @param edocMarkCell 公文文号
     */
    private void getEdocExtField(CtpAffair affair, MultiRowVariableColumnTemplete.Cell edocMarkCell, int width) {
        Map<String, Object> extParam = AffairUtil.getExtProperty(affair);
        if (null != extParam && null != extParam.get(AffairExtPropEnums.edoc_edocMark.name()) && edocMarkCell != null) {
            Object obj = extParam.get(AffairExtPropEnums.edoc_edocMark.name());
            if (obj != null) {
                String str = obj.toString();
                if (str.length() > 7 && width < 10)
                    str = str.substring(0, 7) + "...";
                edocMarkCell.setCellContentHTML("<span title='" + obj.toString() + "' >" + str + "</span>");
            }
        }
    }

    @Override
    public boolean isAllowMobileCustomSet() {
        return true;
    }

    @Override
    public String getResolveFunction(Map<String, String> preference) {
        return MultiRowVariableColumnTemplete.RESOLVE_FUNCTION;
    }

    private String convertPortalBodyType(String bodyType) {
        String bodyTypeClass = "html_16";
        if ("FORM".equals(bodyType) || "20".equals(bodyType)) {
            bodyTypeClass = "form_text_16";
        } else if ("TEXT".equals(bodyType) || "30".equals(bodyType)) {
            bodyTypeClass = "txt_16";
        } else if ("OfficeWord".equals(bodyType) || "41".equals(bodyType)) {
            bodyTypeClass = "doc_16";
        } else if ("OfficeExcel".equals(bodyType) || "42".equals(bodyType)) {
            bodyTypeClass = "xls_16";
        } else if ("WpsWord".equals(bodyType) || "43".equals(bodyType)) {
            bodyTypeClass = "wps_16";
        } else if ("WpsExcel".equals(bodyType) || "44".equals(bodyType)) {
            bodyTypeClass = "xls2_16";
        } else if ("Pdf".equals(bodyType) || "45".equals(bodyType)) {
            bodyTypeClass = "pdf_16";
        } else if ("Ofd".equals(bodyType) || "46".equals(bodyType)) {
            bodyTypeClass = "ofd_16";
        } else if ("videoConf".equals(bodyType)) {
            bodyTypeClass = "meeting_video_16";
        }
        return bodyTypeClass;
    }
}
