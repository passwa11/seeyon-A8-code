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
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.bo.AffairCondition;
import com.seeyon.ctp.common.affair.enums.AffairExtPropEnums;
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

/**
 * @author zhaifeng
 * 已办事项栏目
 */
public class DoneSection extends BaseSectionImpl {
    private static final Log log = LogFactory.getLog(DoneSection.class);
    private PendingManager pendingManager;
    private OrgManager orgManager;
    private ColManager colManager;
    private CommonAffairSectionUtils commonAffairSectionUtils;

    public void setPendingManager(PendingManager pendingManager) {
        this.pendingManager = pendingManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setColManager(ColManager colManager) {
        this.colManager = colManager;
    }

    public void setCommonAffairSectionUtils(CommonAffairSectionUtils commonAffairSectionUtils) {
        this.commonAffairSectionUtils = commonAffairSectionUtils;
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
                        if (!"edocMark".equals(val.getValue()) && !"sendUnit".equals(val.getValue())) {
                            result.add(val);
                        }
                    }
                    ref.setValueRanges(result.toArray(new SectionReferenceValueRange[0]));
                }
            }
        }
    }

    @Override
    public String getIcon() {
        return "done";
    }

    @Override
    public String getId() {
        //栏目ID，与配置文件中的ID相同
        return "doneSection";
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
            return AppContext.isAdmin() || AppContext.hasResourceCode("F01_listDone");
        }
    }

    @Override
    public String getBaseNameI18nKey() {
        return "common.my.done.title";
    }

    @Override
    public String getBaseName() {
        return ResourceUtil.getString("common.my.done.title");
    }

    @Override
    public String getName(Map<String, String> preference) {
        //栏目显示的名字，必须实现国际化，在栏目属性的“columnsName”中存储
        String name = preference.get("columnsName");
        if (Strings.isBlank(name)) {
            return ResourceUtil.getString("common.my.done.title");//已办事项
        } else {
            return name;
        }

    }

    public Integer getTotal(Map<String, String> preference) {
        //return this.total;
        return null;
    }

    @Override
    public boolean isAllowMobileCustomSet() {
        return true;
    }

    @Override
    public BaseSectionTemplete projection(Map<String, String> preference) {
        String rowStr = preference.get("rowList");
        boolean isGroupBy = true;
        // 数据显示
        String dateList = preference.get("dateList");
        //判断是否选择‘同一流程只显示最后一条 ’
        if (Strings.isBlank(dateList)) {
            isGroupBy = false;
        } else {
            isGroupBy = true;
        }
        //传到更多页面时，发起时间、处理期限是必须显示的，而不是通过配置的，同时为了保证顺序问题，因此要如此处理(将deadline放到category前面)
        if (Strings.isBlank(rowStr)) {
            rowStr = "subject,createDate,receiveTime,sendUser,deadline,category";
        } else {
            //如果rowStr 不为空的 要添加 处理期限,并且要放到category之前
            int index1 = rowStr.indexOf(",category");
            if (index1 != -1) {
                rowStr = rowStr.substring(0, index1) + ",deadline,category";
            } else {
                rowStr = rowStr + ",deadline";
            }
            //添加 发起时间  放到标题之后
            rowStr = rowStr.substring(0, 8) + "createDate," + rowStr.substring(8);
        }

        AffairCondition condition = new AffairCondition();
        FlipInfo fi = new FlipInfo();
        fi.setNeedTotal(false);
        // 显示行数
        String count = preference.get("count");
        //默认行数
        int coun = 8;
        if (Strings.isNotBlank(count)) {
            coun = Integer.parseInt(count);
        }
        MultiRowVariableColumnTemplete c = new MultiRowVariableColumnTemplete();
        coun = c.getPageSize(preference)[0];
        fi.setSize(coun);

        fi.setNeedTotal(false);
        List<CtpAffair> affairs = new ArrayList<CtpAffair>();
        List<CtpAffair> newAffairs = new ArrayList<>();
        try {
            affairs = pendingManager.querySectionAffair(condition, fi, preference, ColOpenFrom.listDone.name(), new HashMap<String, String>(), false);
            //【恩华药业】zhou:协同过滤掉设定范围内的数据【开始】
            AccessSetingManager manager = new AccessSetingManagerImpl();
            for (CtpAffair affair : affairs) {
                if (affair.getApp() == 1) {
                    Long senderId = affair.getMemberId();
                    V3xOrgMember member = orgManager.getMemberById(senderId);
                    Long userId = member.getId();
                    Map<String, Object> map = new HashMap<>();
                    map.put("memberId", userId);
                    List<DepartmentViewTimeRange> list = manager.getDepartmentViewTimeRange(map);
                    if (list.size() > 0) {
                        DepartmentViewTimeRange range = list.get(0);
                        if (!"".equals(range.getDayNum()) && null !=range.getDayNum()  && Long.parseLong(range.getDayNum()) > 0l) {
                            LocalDateTime end = LocalDateTime.now();
                            LocalDateTime start = LocalDateTime.now().minusDays(Long.parseLong(range.getDayNum()));
                            Long startTime = start.toInstant(ZoneOffset.of("+8")).toEpochMilli();
                            Long endTime = end.toInstant(ZoneOffset.of("+8")).toEpochMilli();
                            Long objectId = affair.getObjectId();
                            ColSummary colSummary = colManager.getColSummaryById(objectId);
                            Date createDate = colSummary.getCreateDate();
                            if (createDate.getTime() > startTime.longValue() && createDate.getTime() < endTime.longValue()) {
                                newAffairs.add(affair);
                            }
                        } else if (!"".equals(range.getDayNum()) && null !=range.getDayNum()  && Long.parseLong(range.getDayNum()) == 0l) {
                        } else {
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
            log.error("", e1);
        }
        String s = "";
        try {
            s = URLEncoder.encode(this.getName(preference), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("", e);

        }
        //zhou:修改第二个参数
        this.getTemplete(c, newAffairs, preference);
        //【更多】
        c.addBottomButton(BaseSectionTemplete.BOTTOM_BUTTON_LABEL_MORE, "/portalAffair/portalAffairController.do?method=moreDone" + "&fragmentId=" + preference.get(PropertyName.entityId.name())
                + "&ordinal=" + preference.get(PropertyName.ordinal.name()) + "&rowStr=" + rowStr + "&columnsName=" + s + "&isGroupBy=" + isGroupBy);
        c.setDataNum(coun);
        return c;
    }

    @Override
    public BaseSectionTemplete mProjection(Map<String, String> preference) {
        User user = AppContext.getCurrentUser();
        FlipInfo fi = new FlipInfo();
        AffairCondition condition = new AffairCondition();
        Integer count = SectionUtils.getSectionCount(3, preference);
        fi.setPage(1);
        fi.setSize(count);
        fi.setNeedTotal(false);
        List<CtpAffair> affairs = new ArrayList<CtpAffair>();

        //condition.setVjoin(true);
        try {
            affairs = pendingManager.querySectionAffair(condition, fi, preference, ColOpenFrom.listDone.name(), new HashMap<String, String>(), false);
        } catch (BusinessException e1) {
            log.error("", e1);
        }
        MListTemplete c = new MListTemplete();
        if (Strings.isNotEmpty(affairs)) {
            for (CtpAffair affair : affairs) {
                MListTemplete.Row row = c.addRow();
                String subject = WFComponentUtil.showSubjectOfAffair(affair, false, -1).replaceAll("\r\n", "").replaceAll("\n", "");
                //设置标题代理信息
                if (Integer.valueOf(StateEnum.col_done.key()).equals(affair.getState())) {
                    subject = WFComponentUtil.showSubjectOfSummary4Done(affair, -1);
                }
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
                                + "&openFrom=listDone&VJoinOpen=VJoin&summaryId=" + affair.getObjectId() + "&r="
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
                                        + "&openFrom=listDone&VJoinOpen=VJoin&summaryId=" + affair.getObjectId() + "&r="
                                        + System.currentTimeMillis());
                                break;
                            case old_edocSend://G6老公文发文
                            case old_edocRec://G6老公文收文
                            case old_edocSign://G6老公文签报
                                row.setLink("/seeyon/m3/apps/v5/edoc/html/edocSummary.html?affairId=" + affair.getId()
                                        + "&openFrom=listDone&VJoinOpen=VJoin&summaryId=" + affair.getObjectId() + "&r="
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
                row.setCreateDate(MListTemplete.showDate(affair.getReceiveTime()));
                String memberName = Functions.showMemberName(affair.getSenderId());
                if (memberName == null && (affair.getSenderId() == null || affair.getSenderId() == -1)) {
                    memberName = Strings.escapeNULL(affair.getExtProps(), "");
                }
                row.setCreateMember(memberName);
                row.setReadFlag("true");
                row.setState(affair.getSummaryState() == null ? "0" : affair.getSummaryState().toString());
                row.setCreateMemberId(affair.getSenderId().toString());
                row.setHasAttachments(AffairUtil.isHasAttachments(affair));

                int app = affair.getApp();
                String categoryName = ResourceUtil.getString("application." + app + ".label");
                //分类
                switch (ApplicationCategoryEnum.valueOf(app)) {
                    case templateApprove:
                    case collaboration:
                    case meetingroom:
                    case info:
                        row.setCategory(categoryName);
                        break;
                    case meeting:
                        String meetingCategory = ResourceUtil.getString("pending.meeting.label");
                        row.setCategory(meetingCategory);
                        break;
                    case edoc://G6公文
                        Integer subApp = affair.getSubApp();
                        ApplicationSubCategoryEnum subAppEnum = ApplicationSubCategoryEnum.valueOf(app, subApp);
                        switch (subAppEnum) {
                            case edoc_fawen://G6新公文发文
                                categoryName = ResourceUtil.getString("govdoc.edocSend.label");
                                break;
                            case edoc_qianbao://G6新公文签报
                                categoryName = ResourceUtil.getString("govdoc.edocSign.label");
                                break;
                            case edoc_shouwen://G6新公文收文
                                categoryName = ResourceUtil.getString("govdoc.edocRec.label");
                                break;
                            case edoc_jiaohuan://G6新公文交换
                                categoryName = ResourceUtil.getString("govdoc.edocRec.label");
                                break;
                            case old_edocSend://G6老公文发文
                                categoryName = ResourceUtil.getString("govdoc.edocSend.label");
                                break;
                            case old_edocRec://G6老公文收文
                                categoryName = ResourceUtil.getString("govdoc.edocRec.label");
                                break;
                            case old_edocSign://G6老公文签报
                                categoryName = ResourceUtil.getString("govdoc.edocSign.label");
                                break;
                            case old_exSend://G6老公文交换
                                categoryName = ResourceUtil.getString("govdoc.done.exSend.label");//已发送
                                break;
                            case old_exSign://G6老公文签收
                                categoryName = ResourceUtil.getString("govdoc.done.exSign.label");//已签收
                                break;
                            case old_edocRegister: //G6老公文登记
                                categoryName = ResourceUtil.getString("application." + subApp + ".label");
                                break;
                            case old_edocRecDistribute: //G6老公文分发
                                categoryName = ResourceUtil.getString("application." + subApp + ".label");
                                break;
                        }
                        row.setCategory(categoryName);
                        break;
                    default:
                        break;
                }
            }
        }
        String moreLink = "";
        if (user.isVJoinMember()) {
            moreLink = "/seeyon/m3/apps/v5/collaboration/html/colAffairs.html?openFrom=listDone&VJoinOpen=VJoin&r=" + System.currentTimeMillis();
        } else {
            String entityId = preference.get("entityId");// entityId
            String ordinal = preference.get("ordinal");
            moreLink = "/seeyon/m3/apps/m3/todo/layout/todo-list.html?openFrom=listDone&entityId=" + entityId + "&ordinal=" + ordinal + "&VJoinOpen=VJoin";
        }
        c.setMoreLink(moreLink);
        return c;
    }

    /**
     * 获得列表模版
     *
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
        // 显示列
        String rowStr = preference.get("rowList");
        if (Strings.isBlank(rowStr)) {
            if (user.isV5Member()) {
                rowStr = "subject,receiveTime,sendUser,category";
            } else {
                rowStr = "subject,receiveTime,sendUser";
            }
        }
        String[] rows = rowStr.split(",");
        List<String> list = Arrays.asList(rows);
        //判断是否选择‘标题’
        boolean isSubject = list.contains("subject");
        //判断是否选择‘上一处理人’
        boolean isPreApproverName = list.contains("preApproverName");
        //判断是否选择‘处理时间/召开时间’
        boolean isCompleteTime = list.contains("receiveTime");
        //判断是否选择'公文文号'
        boolean isEdocMark = list.contains("edocMark");
        //判断是否选择'发文单位'
        boolean isSendUnit = list.contains("sendUnit");
        //判断是否选择‘发起人’
        boolean isSendUser = list.contains("sendUser");
        //判断是否选择‘分类’
        boolean isCategory = list.contains("category");
        //判断是否选择‘当前待办人’
        boolean isCurrentNodesInfo = list.contains("currentNodesInfo");

        Boolean isGov = (Boolean) (SysFlag.is_gov_only.getFlag());
        if (isGov == null) {
            isGov = false;
        }
        boolean mtAppAuditFlag = true;
        boolean hasMtAppAuditGrant = false;
        boolean edocDistributeFlag = true;
        boolean hasEdocDistributeGrant = false;
        //默认为8条记录
        int count = 8;
        String coun = preference.get("count");
        if (Strings.isNotBlank(coun)) {
            count = Integer.parseInt(coun);
        }
        if (null != affairs) {
            count = affairs.size();//新需求，不加空行了
        }

        Map<Long, String> currentNodeInfos = commonAffairSectionUtils.parseCurrentNodeInfos(affairs);
        for (int i = 0; i < count; i++) {
            MultiRowVariableColumnTemplete.Row row = c.addRow();
            //标题
            MultiRowVariableColumnTemplete.Cell subjectCell = null;
            //上一处理人
            MultiRowVariableColumnTemplete.Cell preApproverNameCell = null;
            //处理时间
            MultiRowVariableColumnTemplete.Cell completeTimeCell = null;
            //公文文号
            MultiRowVariableColumnTemplete.Cell edocMarkCell = null;
            //发文单位
            MultiRowVariableColumnTemplete.Cell sendUnitCell = null;
            //发起人
            MultiRowVariableColumnTemplete.Cell createMemberCell = null;
            //分类
            MultiRowVariableColumnTemplete.Cell categoryCell = null;
            //当前待办人
            MultiRowVariableColumnTemplete.Cell currentNodesInfoCell = null;

            if (isSubject) {
                subjectCell = row.addCell();
            }
            if (isCompleteTime) {
                completeTimeCell = row.addCell();
            }
            if (isEdocMark) {
                edocMarkCell = row.addCell();
            }
            if (isSendUnit) {
                sendUnitCell = row.addCell();
            }
            if (isCurrentNodesInfo) {
                currentNodesInfoCell = row.addCell();
            }
            if (isSendUser) {
                createMemberCell = row.addCell();
            }
            if (isPreApproverName) {
                preApproverNameCell = row.addCell();
            }
            if (isCategory) {
                categoryCell = row.addCell();
            }

            //如果为空则添加默认空行
            if (affairs == null || affairs.size() == 0) {
                continue;
            }
            if (i < affairs.size()) {
                CtpAffair affair = affairs.get(i);
                String forwardMember = affair.getForwardMember();
                Integer resentTime = affair.getResentTime();
                String subject = WFComponentUtil.showSubjectOfAffair(affair, false, -1).replaceAll("\r\n", "").replaceAll("\n", "");
                //设置标题代理信息
                if (Integer.valueOf(StateEnum.col_done.key()).equals(affair.getState())) {
                    subject = WFComponentUtil.showSubjectOfSummary4Done(affair, -1);
                }
                if (isSubject) {
                    String showName = WFComponentUtil.mergeSubjectWithForwardMembers(affair.getSubject(), forwardMember, resentTime, null, -1);
                    if (affair.getAutoRun() != null && affair.getAutoRun()) {
                        showName = ResourceUtil.getString("collaboration.newflow.fire.subject", showName);
                        subject = ResourceUtil.getString("collaboration.newflow.fire.subject", subject);
                    }
                    subjectCell.setAlt(showName);

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
                    //设置重要程度图标
                    if (affair.getImportantLevel() != null && affair.getImportantLevel() > 1 && affair.getImportantLevel() < 6) {
                        subjectCell.addExtPreClasses("ico16 important" + affair.getImportantLevel() + "_16");
                    }
                    //设置附件图标
                    if (AffairUtil.isHasAttachments(affair)) {
                        subjectCell.addExtClasses("ico16 vp-attachment");
                    }
                    Map<String, Object> extMap = Strings.escapeNULL(AffairUtil.getExtProperty(affair), new HashMap<String, Object>());
                    if (extMap != null && extMap.get(AffairExtPropEnums.meeting_videoConf.name()) != null) {
                        String meetingNature = (String) extMap.get(AffairExtPropEnums.meeting_videoConf.name());
                        //会议方式 1普通会议 2视频会议
                        if ("2".equals(meetingNature)) {
                            subjectCell.addExtClasses("ico16 meeting_video_16");
                        }
                    }

                    //设置正文类型图标
                    if (affair.getBodyType() != null && !"10".equals(affair.getBodyType()) && !"30".equals(affair.getBodyType()) && !"HTML".equals(affair.getBodyType())) {
                        String bodyType = affair.getBodyType();
                        String bodyTypeClass = convertPortalBodyType(bodyType);
                        if (!"html_16".equals(bodyTypeClass) && !"meeting_video_16".equals(bodyTypeClass)) {
                            subjectCell.addExtClasses("ico16 office" + bodyTypeClass);
                        }
                    }
                    //流程状态
                    if (Integer.valueOf(CollaborationEnum.flowState.finish.ordinal()).equals(affair.getSummaryState())) {
                        subjectCell.addExtPreClasses("ico16 flow3_16");
                    } else if (Integer.valueOf(CollaborationEnum.flowState.terminate.ordinal()).equals(affair.getSummaryState())) {
                        subjectCell.addExtPreClasses("ico16 flow1_16");
                    }
                    //害怕未处理\r\n的条件
                    subject = subject.replaceAll("\\r\\n", "");
                    subject = subject.replaceAll("\r", "");
                    subject = subject.replaceAll("\n", "");
                    subject = subject.replaceAll("\\r", "");
                    subject = subject.replaceAll("\\n", "");
                    subjectCell.setCellContent(subject);
                }
                int app = affair.getApp();
                String url = "";
                String categoryName = ResourceUtil.getString("application." + app + ".label");
                ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(app);
                String from = null;
                switch (StateEnum.valueOf(affair.getState())) {
                    case col_sent:
                        from = "Sent";
                        break;
                    case col_pending:
                        from = "Pending";
                        break;
                    case col_done:
                        from = "Done";
                        break;
                    default:
                        from = "Done";
                }
                switch (appEnum) {
                    case templateApprove:
                        if (subjectCell != null) {
                            subjectCell.setLinkURL("/templateApproveController.do?method=dealWithApproveWorkFlowInfo&state=" + affair.getState() + "&objectId=" + affair.getObjectId() + "&affairId=" + affair.getId() + "&templateId=" + affair.getTempleteId() + "&formId=" + affair.getFromId());
                            subjectCell.setAlt("(" + ResourceUtil.getString("workflow.approve.release") + ")" + subjectCell.getCellContent());
                            subjectCell.setCellContent(subjectCell.getAlt());
                        }

                        if (categoryCell != null) {
                            //判断是否有资源菜单权限
                            boolean hasResPerm = pendingManager.hasResPerm(affair, user);
                            if (hasResPerm) {
//                                url = AppContext.getRawRequest().getContextPath() +"/templateApproveController.do?method=dealWithApproveWorkFlowInfo&state="+affair.getState()+"&objectId="+affair.getObjectId()+"&affairId="+affair.getId()+"&templateId="+affair.getTempleteId()+"&formId="+affair.getFromId();
//                                Map<String, Map<String, String>> categoryHandler = new HashMap<String, Map<String, String>>();
//                                Map<String,String> clickHandler = new HashMap<String,String>();
//                                clickHandler.put(HANDLER_PARAMETER.name.name(), "open_link");
//                                clickHandler.put(HANDLER_PARAMETER.parameter.name(), url);
//                                categoryHandler.put(OPEN_TYPE.click.name(), clickHandler);
//                                categoryCell.setHandler(categoryHandler);
                                categoryCell.setOpenType(OPEN_TYPE.openWorkSpace);
                            }

                            categoryCell.setHandler(null);
                            categoryCell.setCellContentHTML(categoryName);
                        }


                        if (isPreApproverName) {
                            preApproverNameCell.setCellContentHTML(Functions.showMemberName(affair.getPreApprover()));
                        }
                        break;
                    case collaboration:
                        if (subjectCell != null) {
                            subjectCell.setLinkURL("/collaboration/collaboration.do?method=summary&openFrom=listDone&affairId=" + affair.getId());
                        }
                        if (categoryCell != null) {
                            //判断是否有资源菜单权限
                            boolean hasResPerm = pendingManager.hasResPerm(affair, user);
                            if (hasResPerm) {
                                url = AppContext.getRawRequest().getContextPath() + "/collaboration/collaboration.do?method=listDone";
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

                        if (isPreApproverName) {
                            preApproverNameCell.setCellContentHTML(Functions.showMemberName(affair.getPreApprover()));
                        }


                        break;
                    case meeting:
                        String linkURL = "";
                        if (affair.getSubApp() == ApplicationSubCategoryEnum.meetingAudit.key()) {//会议审核
                            linkURL = "/mtAppMeetingController.do?method=mydetail&id=" + affair.getObjectId() + "&affairId=" + affair.getId();
                            if (mtAppAuditFlag) {
                                //hasMtAppAuditGrant = configGrantManager.hasConfigGrant(user.getLoginAccount(), user.getId(), "v3x_meeting_create_acc", "v3x_meeting_create_acc_review");
                                hasMtAppAuditGrant = false;//兼容老G6数据升级，因为v5会议已经没有会议审核功能，故这里不允许链接
                            }
                            if (hasMtAppAuditGrant) {
                                url = AppContext.getRawRequest().getContextPath() + "/mtMeeting.do?method=entryManager&entry=meetingManager&listMethod=listAudit&listType=listAppAuditingMeetingAudited";
                            }
                            mtAppAuditFlag = false;
                        } else if (affair.getSubApp() == ApplicationSubCategoryEnum.minutesAudit.key()) {//会议纪要
                            linkURL = "/mtSummary.do?method=mydetail&recordId=" + affair.getObjectId() + "&affairId=" + affair.getId();
                            url = AppContext.getRawRequest().getContextPath() + "/mtSummary.do?method=listHome&from=audit&listType=audited";
                        } else if (affair.getSubApp() == ApplicationSubCategoryEnum.meetingNotification.key()) {//会议通知
                            linkURL = "/mtMeeting.do?method=mydetail&id=" + affair.getObjectId();
                            url = AppContext.getRawRequest().getContextPath() + "/meetingNavigation.do?method=entryManager&entry=meetingDone";
                        }
                        if (subjectCell != null) {
                            subjectCell.setLinkURL(linkURL);
                        }
                        if (categoryCell != null) {
                            String meetingCategory = ResourceUtil.getString("pending.meeting.label");
                            Map<String, Map<String, String>> categoryHandler = new HashMap<String, Map<String, String>>();
                            Map<String, String> clickHandler = new HashMap<String, String>();
                            clickHandler.put(HANDLER_PARAMETER.name.name(), "open_link");
                            clickHandler.put(HANDLER_PARAMETER.parameter.name(), url);
                            categoryHandler.put(OPEN_TYPE.click.name(), clickHandler);
                            categoryCell.setHandler(categoryHandler);
                            categoryCell.setOpenType(OPEN_TYPE.openWorkSpace);
                            categoryCell.setCellContentHTML(meetingCategory);
                        }
                        break;
                    case meetingroom:
                        if (subjectCell != null) {
                            subjectCell.setLinkURL("/meetingroom.do?method=createPerm&openWin=1&id=" + affair.getObjectId());
                        }
                        if (categoryCell != null) {
                            url = "/meetingroom.do?method=index";
                            if (user.hasResourceCode("F09_meetingDone")) {
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
                    case edoc://G6公文
                        Integer subApp = affair.getSubApp();
                        ApplicationSubCategoryEnum subAppEnum = ApplicationSubCategoryEnum.valueOf(app, subApp);
                        switch (subAppEnum) {
                            case edoc_fawen://G6新公文发文
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/govdoc/govdoc.do?method=summary&openFrom=listDone&affairId=" + affair.getId() + "&app=" + affair.getApp() + "&summaryId=" + affair.getObjectId());
                                }
                                if (categoryCell != null) {
                                    //分类链接：判断是否有公文管理-已办列表资源，若有则链接到发文管理-已办列表，若没有则链接到公文管理-已办列表
                                    boolean f11 = user.hasResourceCode("F20_govDocSendManage");
                                    url = "/govdoc/govdoc.do?method=index&govdocType=1&listType=listDoneAll&_resourceCode=F20_govDocSendManage";
                                    if (!f11) {
                                        f11 = user.hasResourceCode("F20_govDocDone");
                                        url = "/govdoc/govdoc.do?method=index&listType=listDoneAllRoot&_resourceCode=F20_govDocDone";
                                    }
                                    categoryName = ResourceUtil.getString("govdoc.edocSend.label");
                                }
                                break;
                            case edoc_qianbao://G6新公文签报
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/govdoc/govdoc.do?method=summary&openFrom=listDone&affairId=" + affair.getId() + "&app=" + affair.getApp() + "&summaryId=" + affair.getObjectId());
                                }
                                if (categoryCell != null) {
                                    //分类链接：判断是否有公文管理-已办列表资源，若有则链接到签报管理-已办列表，若没有则链接到公文管理-已办列表
                                    boolean f11 = user.hasResourceCode("F20_signReport");
                                    url = "/govdoc/govdoc.do?method=index&govdocType=3&listType=listDoneAll&_resourceCode=F20_signReport";
                                    if (!f11) {
                                        f11 = user.hasResourceCode("F20_govDocDone");
                                        url = "/govdoc/govdoc.do?method=index&listType=listDoneAll&_resourceCode=F20_govDocDone";
                                    }
                                    categoryName = ResourceUtil.getString("govdoc.edocSign.label");
                                }
                                break;
                            case edoc_shouwen://G6新公文收文
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/govdoc/govdoc.do?method=summary&openFrom=listDone&affairId=" + affair.getId() + "&app=" + affair.getApp() + "&summaryId=" + affair.getObjectId());
                                }
                                if (categoryCell != null) {
                                    //分类链接：判断是否有公文管理-已办列表资源，若有则链接到收文管理-已办列表，若没有则链接到公文管理-已办列表
                                    boolean f11 = user.hasResourceCode("F20_receiveManage");
                                    url = "/govdoc/govdoc.do?method=index&govdocType=2,4&listType=listDoneAll&_resourceCode=F20_receiveManage";
                                    if (!f11) {
                                        f11 = user.hasResourceCode("F20_govDocDone");
                                        url = "/govdoc/govdoc.do?method=index&listType=listDoneAllRoot&_resourceCode=F20_govDocDone";
                                    }
                                    categoryName = ResourceUtil.getString("govdoc.edocRec.label");
                                }
                                break;
                            case edoc_jiaohuan://G6新公文交换
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/govdoc/govdoc.do?method=summary&openFrom=listDone&affairId=" + affair.getId() + "&app=" + affair.getApp() + "&summaryId=" + affair.getObjectId());
                                }
                                if (categoryCell != null) {
                                    //分类链接：判断是否有公文管理-已办列表资源，若有则链接到收文管理-已办列表，若没有则链接到公文管理-已办列表
                                    boolean f11 = user.hasResourceCode("F20_receiveManage");
                                    url = "/govdoc/govdoc.do?method=index&govdocType=2,4&listType=listDoneAll&_resourceCode=F20_receiveManage";
                                    //链接优先级：公文管理-收文管理、公文管理-公文交换、公文管理-已办列表
                                    if (!f11) {
                                        f11 = user.hasResourceCode("F20_govdocExchange");
                                        url = "/govdoc/exchange.do?method=exchangeIndex&_resourceCode=F20_govdocExchange";
                                    }
                                    if (!f11) {
                                        f11 = user.hasResourceCode("F20_govDocDone");
                                        url = "/govdoc/govdoc.do?method=index&listType=listDoneAllRoot&_resourceCode=F20_govDocDone";
                                    }
                                    categoryName = ResourceUtil.getString("govdoc.edocRec.label");
                                }
                                break;
                            case old_edocSend://G6老公文发文
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/edocController.do?method=detailIFrame&from=" + from + "&affairId=" + affair.getId() + "");
                                }
                                if (categoryCell != null) {
                                    //分类链接：判断是否有公文管理-已办列表资源，若有则链接到发文管理-已办列表，若没有则链接到公文管理-已办列表
                                    boolean f11 = user.hasResourceCode("F20_govDocSendManage");
                                    url = "/govdoc/govdoc.do?method=index&govdocType=1&listType=listDoneAll&_resourceCode=F20_govDocSendManage";
                                    if (!f11) {
                                        f11 = user.hasResourceCode("F20_govDocDone");
                                        url = "/govdoc/govdoc.do?method=index&listType=listDoneAllRoot&_resourceCode=F20_govDocDone";
                                    }
                                    categoryName = ResourceUtil.getString("govdoc.edocSend.label");
                                }
                                break;
                            case old_edocRec://G6老公文收文
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/edocController.do?method=detailIFrame&from=" + from + "&affairId=" + affair.getId() + "");
                                }
                                if (categoryCell != null) {
                                    //分类链接：判断是否有公文管理-已办列表资源，若有则链接到收文管理-已办列表，若没有则链接到公文管理-已办列表
                                    boolean f11 = user.hasResourceCode("F20_receiveManage");
                                    url = "/govdoc/govdoc.do?method=index&govdocType=2,4&listType=listDoneAll&_resourceCode=F20_receiveManage";
                                    if (!f11) {
                                        f11 = user.hasResourceCode("F20_govDocDone");
                                        url = "/govdoc/govdoc.do?method=index&listType=listDoneAllRoot&_resourceCode=F20_govDocDone";
                                    }
                                    categoryName = ResourceUtil.getString("govdoc.edocRec.label");
                                }
                                break;
                            case old_edocSign://G6老公文签报
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/edocController.do?method=detailIFrame&from=" + from + "&affairId=" + affair.getId() + "");
                                }
                                if (categoryCell != null) {
                                    //分类链接：判断是否有公文管理-已办列表资源，若有则链接到签报管理-已办列表，若没有则链接到公文管理-已办列表
                                    boolean f11 = user.hasResourceCode("F20_signReport");
                                    url = "/govdoc/govdoc.do?method=index&govdocType=3&listType=listDoneAll&_resourceCode=F20_signReport";
                                    if (!f11) {
                                        f11 = user.hasResourceCode("F20_govDocDone");
                                        url = "/govdoc/govdoc.do?method=index&listType=listDoneAll&_resourceCode=F20_govDocDone";
                                    }
                                    categoryName = ResourceUtil.getString("govdoc.edocSign.label");
                                }
                                break;
                            case old_exSend://G6老公文交换
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/exchangeEdoc.do?method=edit&modelType=sent&id=" + affair.getSubObjectId());
                                }
                                if (categoryCell != null) {
                                    categoryName = ResourceUtil.getString("govdoc.done.exSend.label");//已发送
                                }
                                break;
                            case old_exSign://G6老公文签收
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/exchangeEdoc.do?method=edit&modelType=received&userId=&id=" + affair.getSubObjectId() + "&affairId=" + affair.getId());
                                }
                                if (categoryCell != null) {
                                    categoryName = ResourceUtil.getString("govdoc.done.exSign.label");//已签收
                                }
                                break;
                            case old_edocRegister: //G6老公文登记
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/edocController.do?method=edocRegisterDetail&forwardType=registered&recieveId=" + affair.getSubObjectId() + "&registerId=" + affair.getObjectId());
                                }
                                if (categoryCell != null) {
                                    categoryName = ResourceUtil.getString("done.edocRegister.label");
                                }
                                break;
                            case old_edocRecDistribute: //G6老公文分发
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/edocController.do?method=detailIFrame&from=Sent&&affairId=" + affair.getId());
                                }
                                if (categoryCell != null) {
                                    categoryName = ResourceUtil.getString("application." + affair.getSubApp() + ".label");
                                }
                                break;
                        }
                        if (categoryCell != null) {
                            if (subAppEnum.key() == ApplicationSubCategoryEnum.edoc_fawen.key()
                                    || subAppEnum.key() == ApplicationSubCategoryEnum.edoc_shouwen.key()
                                    || subAppEnum.key() == ApplicationSubCategoryEnum.edoc_qianbao.key()
                                    || subAppEnum.key() == ApplicationSubCategoryEnum.edoc_jiaohuan.key()
                                    || subAppEnum.key() == ApplicationSubCategoryEnum.old_edocSend.key()
                                    || subAppEnum.key() == ApplicationSubCategoryEnum.old_edocRec.key()
                                    || subAppEnum.key() == ApplicationSubCategoryEnum.old_edocSign.key()) {
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

                        getEdocExtField(affair, edocMarkCell, sendUnitCell, width);
                        if (isPreApproverName) {
                            preApproverNameCell.setCellContentHTML(Functions.showMemberName(affair.getPreApprover()));
                        }
                        break;
                    case info:
                        if (subjectCell != null) {
                            subjectCell.setLinkURL("/infoDetailController.do?method=detail&summaryId=" + affair.getObjectId() + "&from=" + from + "&affairId=" + affair.getId() + "");
                        }
                        url = "/infoNavigationController.do?method=indexManager&entry=infoAuditing&toFrom=listInfoAuditDone&affairId=" + affair.getObjectId();
                        if (categoryCell != null) {
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
                //处理时间/召开时间
                if (completeTimeCell != null) {
                    if (affair.getApp() == ApplicationCategoryEnum.meeting.key() || affair.getApp() == ApplicationCategoryEnum.meetingroom.key()) {
                        String dateTime = WFComponentUtil.getDateTime(affair.getReceiveTime(), "yyyy-MM-dd HH:mm");
                        completeTimeCell.setCellContentHTML("<span class='color_gray' title='" + dateTime + "'>" + dateTime + "</span>");
                    } else {
                        if (affair.getCompleteTime() != null) {
                            String dateTime = WFComponentUtil.getDateTime(affair.getCompleteTime(), "yyyy-MM-dd HH:mm");
                            completeTimeCell.setCellContentHTML("<span class='color_gray' title='" + dateTime + "'>" + dateTime + "</span>");
                        }
                    }
                }
                //当前处理人
//                List<Integer> edocApps =new ArrayList<Integer>();
//                edocApps.add(ApplicationCategoryEnum.edocSend.getKey());//发文 19
//                edocApps.add(ApplicationCategoryEnum.edocRec.getKey());//收文 20
//                edocApps.add(ApplicationCategoryEnum.edocSign.getKey());//签报21
//                edocApps.add(ApplicationCategoryEnum.exSend.getKey());//待发送公文22
//                edocApps.add(ApplicationCategoryEnum.exSign.getKey());//待签收公文 23
//                edocApps.add(ApplicationCategoryEnum.edocRegister.getKey());//待登记公文 24
//                edocApps.add(ApplicationCategoryEnum.edocRecDistribute.getKey());//收文分发34

                String currentNodesInfoStr = currentNodeInfos.get(affair.getObjectId());
                if (Strings.isNotBlank(currentNodesInfoStr) && isCurrentNodesInfo) {
                    String currentInfo = Strings.getSafeLimitLengthString(currentNodesInfoStr, 10, "..");
                    currentInfo = "<span title='" + currentNodesInfoStr + "' >" + currentInfo + "</span>";
                    currentNodesInfoCell.setCellContentHTML(currentInfo);
                }

                //发起人
                if (createMemberCell != null) {
                    String memberName = Functions.showMemberName(affair.getSenderId());
                    createMemberCell.setAlt(memberName);
                    if (Strings.isNotBlank(memberName) && memberName.length() > 4) {
                        memberName = memberName.substring(0, 4) + "...";
                    }
                    //知会，加签图标
                    if (affair.getFromId() != null && !String.valueOf(affair.getApp()).equals(String.valueOf(ApplicationCategoryEnum.templateApprove.key()))) {
                        String title = ResourceUtil.getString("collaboration.pending.addOrJointly.label", Functions.showMemberName(affair.getFromId()));
                        createMemberCell.addExtClasses("ico16 signature_16");
                        createMemberCell.addExtClassesAlt(title);
                    }
                    createMemberCell.setCellContentHTML(memberName);
                }
            }
        }
        return c;
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

    /**
     * 取出公文扩展字段
     *
     * @param affair       CtpAffair对象
     * @param edocMarkCell 公文文号
     * @param sendUnitCell 公文发文单位
     */
    public static void getEdocExtField(CtpAffair affair, MultiRowVariableColumnTemplete.Cell edocMarkCell, MultiRowVariableColumnTemplete.Cell sendUnitCell, int width) {
        Map<String, Object> extParam = AffairUtil.getExtProperty(affair);
        if (null != extParam && null != extParam.get(AffairExtPropEnums.edoc_edocMark.name()) && edocMarkCell != null) {
            String str = String.valueOf(extParam.get(AffairExtPropEnums.edoc_edocMark.name()));
            if (str.length() > 7 && width < 10)
                str = str.substring(0, 7) + "...";
            edocMarkCell.setCellContentHTML("<span title='" + String.valueOf(extParam.get(AffairExtPropEnums.edoc_edocMark.name())) + "' >" + str + "</span>");
        }
        if (null != extParam && null != extParam.get(AffairExtPropEnums.edoc_sendUnit.name()) && sendUnitCell != null) {
            String val = String.valueOf(extParam.get(AffairExtPropEnums.edoc_sendUnit.name()));
            sendUnitCell.setCellContent(val);
            if (val.length() > 7 && width < 10) {
                val = val.substring(0, 7) + "...";
            }
            sendUnitCell.setCellContentHTML("<span title='" + String.valueOf(extParam.get(AffairExtPropEnums.edoc_sendUnit.name())) + "' >" + val + "</span>");
        }
    }

    @Override
    public String getResolveFunction(Map<String, String> preference) {
        return MultiRowVariableColumnTemplete.RESOLVE_FUNCTION;
    }

}
