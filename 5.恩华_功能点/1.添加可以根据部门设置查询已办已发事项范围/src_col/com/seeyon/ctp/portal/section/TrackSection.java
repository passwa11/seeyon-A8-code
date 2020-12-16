/**
 * $Author: 翟锋$
 * $Rev: $
 * $Date:: $
 * <p>
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 * <p>
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.portal.section;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.edoc.api.EdocApi;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.affair.bo.AffairCondition;
import com.seeyon.ctp.common.affair.bo.AffairCondition.SearchCondition;
import com.seeyon.ctp.common.affair.enums.AffairExtPropEnums;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.manager.PendingManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
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
 *
 */
public class TrackSection extends BaseSectionImpl {
    private static final Log log = LogFactory.getLog(TrackSection.class);
    private AffairManager affairManager;
    private PendingManager pendingManager;
    private EdocApi edocApi;
    private CommonAffairSectionUtils commonAffairSectionUtils;

    public CommonAffairSectionUtils getCommonAffairSectionUtils() {
        return commonAffairSectionUtils;
    }

    public void setCommonAffairSectionUtils(CommonAffairSectionUtils commonAffairSectionUtils) {
        this.commonAffairSectionUtils = commonAffairSectionUtils;
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

    public void setPendingManager(PendingManager pendingManager) {
        this.pendingManager = pendingManager;
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
    public String getId() {
        return "trackSection";
    }

    @Override
    public boolean isAllowUsed() {
        if (AppContext.isGroupAdmin()) {
            return false;
        }
        return true;
    }

    @Override
    public String getBaseNameI18nKey() {
        return "menu.collaboration.listTrack";
    }

    @Override
    public String getBaseName(Map<String, String> preference) {
        String name = "";
        if (preference != null) {
            name = preference.get("baseName");
        }
        if (Strings.isBlank(name)) {
            name = ResourceUtil.getString("menu.collaboration.listTrack");
        }
        if (Strings.isBlank(name) && (preference != null)) {
            name = preference.get("columnsName");
        }
        return name;
    }

    @Override
    public String getName(Map<String, String> preference) {
        //栏目显示的名字，必须实现国际化，在栏目属性的“columnsName”中存储
        String name = preference.get("columnsName");
        if (Strings.isBlank(name)) {
            return ResourceUtil.getString("menu.collaboration.listTrack");//待发事项
        } else {
            return name;
        }
    }

    @Override
    public Integer getTotal(Map<String, String> preference) {
        String panel = SectionUtils.getPanel("all", preference);
        boolean flag = false;
        if (Strings.isNotBlank(panel) && "sources".equals(panel)) {
            flag = true;
        }
        String tempStr = preference.get(panel + "_value");
        FlipInfo fi = new FlipInfo();
        fi.setNeedTotal(true);
        // 流程来源
        if (!"all".equals(panel)) {
            if ("sender".equals(panel)) {
                Long memberId = AppContext.getCurrentUser().getId();
                AffairCondition affairCondition = new AffairCondition(memberId, null,
                        ApplicationCategoryEnum.collaboration,
                        ApplicationCategoryEnum.edoc,
                        ApplicationCategoryEnum.meeting,
                        ApplicationCategoryEnum.bulletin,
                        ApplicationCategoryEnum.news,
                        ApplicationCategoryEnum.inquiry,
                        ApplicationCategoryEnum.office,
                        ApplicationCategoryEnum.info,
                        ApplicationCategoryEnum.meetingroom,
                        ApplicationCategoryEnum.edocRecDistribute,
                        ApplicationCategoryEnum.infoStat
                );
                affairCondition.setIsTrack(true);
                List<Integer> appEnum = new ArrayList<Integer>();
                // 查询指定发起人
                affairManager.getAffairListBySender(memberId, tempStr,
                        affairCondition, false, fi, appEnum);
                return fi.getTotal();
            } else {
                if (Strings.isBlank(tempStr) && !flag) {
                    return 0;
                }
            }
        }
        AffairCondition affairCondition = getSectionAffairCondition(preference);
        return affairCondition.getTrackCount(affairManager);
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public BaseSectionTemplete projection(Map<String, String> preference) {
        String rowStr = preference.get("rowList");
        if (Strings.isBlank(rowStr)) {
            //处理期限是必须显示的，而不是通过配置的，同时为了保证顺序问题，因此要如此处理(将deadline放到category前面)
            rowStr = "subject,receiveTime,sendUser,deadline,category";
        } else {
            //如果rowStr 不为空的 要添加 处理期限,并且要放到category之前
            int index = rowStr.indexOf(",category");
            if (index != -1) {
                rowStr = rowStr.substring(0, index) + ",deadline,category";
            } else {
                rowStr = rowStr + ",deadline";
            }
        }
        String panel = SectionUtils.getPanel("all", preference);
        //查询条件组装
        FlipInfo fi = new FlipInfo();
        fi.setNeedTotal(false);
        String count = preference.get("count");
        int coun = 8;
        if (Strings.isNotBlank(count)) {
            coun = Integer.parseInt(count);
        }
        MultiRowVariableColumnTemplete c = new MultiRowVariableColumnTemplete();
        coun = c.getPageSize(preference)[0];
        //单列表
        fi.setSize(coun);
        List<CtpAffair> affairs = new ArrayList<CtpAffair>();
        if (Strings.isNotBlank(panel) && "sources".equals(panel)) {
            AffairCondition affairCondition = getSectionAffairCondition(preference);
            affairs = affairCondition.getTrackAffair(affairManager, fi);
        } else {
            String tempStr = preference.get(panel + "_value");
            if (!"all".equals(panel) && Strings.isBlank(tempStr)) {
                affairs = new ArrayList<CtpAffair>();
            } else {
                if ("sender".equals(panel)) {
                    Long memberId = AppContext.getCurrentUser().getId();
                    AffairCondition affairCondition = new AffairCondition(memberId, null,
                            ApplicationCategoryEnum.collaboration,
                            ApplicationCategoryEnum.edoc);
                    affairCondition.setIsTrack(true);
                    List<Integer> appEnum = new ArrayList<Integer>();
                    //查询指定发起人
                    affairs = (List<CtpAffair>) affairManager.getAffairListBySender(memberId, tempStr, affairCondition, false, fi, appEnum);
                } else {
                    AffairCondition affairCondition = getSectionAffairCondition(preference);
                    affairs = affairCondition.getTrackAffair(affairManager, fi);
                }
            }
        }
        String s = "";
        try {
            s = URLEncoder.encode(this.getName(preference), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("", e);
        }
        //单列表
        c = this.getTemplete(c, affairs, preference);
        //【更多】
        c.addBottomButton(BaseSectionTemplete.BOTTOM_BUTTON_LABEL_MORE, "/portalAffair/portalAffairController.do?method=moreTrack" + "&fragmentId=" + preference.get(PropertyName.entityId.name())
                + "&ordinal=" + preference.get(PropertyName.ordinal.name()) + "&currentPanel=" + panel + "&rowStr=" + rowStr + "&columnsName=" + s);
        c.setDataNum(coun);
        return c;
    }

    @Override
    public BaseSectionTemplete mProjection(Map<String, String> preference) {
        FlipInfo fi = new FlipInfo();
        String rowStr = preference.get("rowList");
        if (Strings.isBlank(rowStr)) {
            //处理期限是必须显示的，而不是通过配置的，同时为了保证顺序问题，因此要如此处理(将deadline放到category前面)
            rowStr = "subject,receiveTime,sendUser,deadline,category,currentNodesInfo";
        } else {
            //如果rowStr 不为空的 要添加 处理期限,并且要放到category之前
            int index = rowStr.indexOf(",category");
            if (index != -1) {
                rowStr = rowStr.substring(0, index) + ",deadline,category";
            } else {
                rowStr = rowStr + ",deadline";
            }
        }
        String panel = SectionUtils.getPanel("all", preference);
        //查询条件组装
        fi.setNeedTotal(false);
        String count = preference.get("count");
        int coun = 8;
        if (Strings.isNotBlank(count)) {
            coun = Integer.parseInt(count);
        }
        //单列表
        fi.setSize(coun);
        List<CtpAffair> affairs = new ArrayList<CtpAffair>();
        if (Strings.isNotBlank(panel) && "sources".equals(panel)) {
            AffairCondition affairCondition = getSectionAffairCondition(preference);
            affairs = affairCondition.getTrackAffair(affairManager, fi);
        } else {
            String tempStr = preference.get(panel + "_value");
            if (!"all".equals(panel) && Strings.isBlank(tempStr)) {
                affairs = new ArrayList<CtpAffair>();
            } else {
                if ("sender".equals(panel)) {
                    Long memberId = AppContext.getCurrentUser().getId();
                    AffairCondition affairCondition = new AffairCondition(memberId, null,
                            ApplicationCategoryEnum.collaboration,
                            ApplicationCategoryEnum.edoc);
                    affairCondition.setIsTrack(true);
                    List<Integer> appEnum = new ArrayList<Integer>();
                    //查询指定发起人
                    affairs = (List<CtpAffair>) affairManager.getAffairListBySender(memberId, tempStr, affairCondition, false, fi, appEnum);
                } else {
                    AffairCondition affairCondition = getSectionAffairCondition(preference);
                    affairs = affairCondition.getTrackAffair(affairManager, fi);
                }
            }
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
                row.setLink("/seeyon/m3/apps/v5/collaboration/html/details/summary.html?affairId=" + affair.getId()
                        + "&openFrom=listDone&VJoinOpen=VJoin&summaryId=" + affair.getObjectId() + "&r="
                        + System.currentTimeMillis());
                row.setCreateDate(MListTemplete.showDate(affair.getCreateDate()));
                String memberName = Functions.showMemberName(affair.getSenderId());
                if (memberName == null && (affair.getSenderId() == null || affair.getSenderId() == -1)) {
                    memberName = Strings.escapeNULL(affair.getExtProps(), "");
                }
                row.setCreateMember(memberName);
                row.setReadFlag("true");
                row.setState(affair.getSummaryState() == null ? "0" : affair.getSummaryState().toString());
            }
        }
        /*String moreLink = "/seeyon/m3/apps/v5/collaboration/html/colAffairs.html?openFrom=listSent&VJoinOpen=VJoin&r="
                + System.currentTimeMillis();
        c.setMoreLink(moreLink);*/
        return c;
    }

    /**
     * 根据条件查询列表
     * @param preference
     * @return
     */
    private AffairCondition getSectionAffairCondition(Map<String, String> preference) {
        AffairCondition affairCondition = new AffairCondition();
        Long memberId = AppContext.getCurrentUser().getId();
        affairCondition.setMemberId(memberId);
        String panel = SectionUtils.getPanel("all", preference);
        boolean flag = false;
        if (Strings.isNotBlank(panel) && "sources".equals(panel)) {
            flag = true;
        }
        // 流程来源
        if (!"all".equals(panel)) {
            if (Strings.isNotBlank(panel) && "sources".equals(panel)) {
                affairCondition.addSourceSearchCondition(preference, false);
            } else {
                String tempStr = preference.get(panel + "_value");
                if (Strings.isNotBlank(tempStr)) {
                    // 组装查询条件
                    if ("track_catagory".equals(panel)) {//分类
                        affairCondition.addSearch(SearchCondition.catagory, tempStr, null);
                    } else if ("importLevel".equals(panel)) {//重要程度
                        affairCondition.addSearch(SearchCondition.importLevel, tempStr, null);
                    } else if ("templete_pending".equals(panel)) {
                        affairCondition.addSearch(SearchCondition.templete, tempStr, null);
                    } else if ("Policy".equals(panel)) {
                        affairCondition.addSearch(SearchCondition.policy4Portal, tempStr, null);
                    }
                }
            }
        }
        return affairCondition;
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
        //默认为8条记录
        int count = 8;
        String coun = preference.get("count");
        if (Strings.isNotBlank(coun)) {
            count = Integer.parseInt(coun);
        }
        String rowStr = preference.get("rowList");
        if (Strings.isBlank(rowStr)) {
            //处理期限是必须显示的，而不是通过配置的，同时为了保证顺序问题，因此要如此处理(将deadline放到category前面)
            rowStr = "subject,receiveTime,sendUser,category";//门户新增当前待办人默认勾选
        }
        String[] rows = rowStr.split(",");
        List<String> list = Arrays.asList(rows);
        //判断是否选择‘标题’
        boolean isSubject = list.contains("subject");
        //判断是否选择‘发起时间’
        boolean isCreateDate = list.contains("receiveTime");
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
        //判断是否选择‘催办’
        boolean isHastenInfo = list.contains("hastenInfo");

        Boolean isGov = (Boolean) (SysFlag.is_gov_only.getFlag());
        if (isGov == null) {
            isGov = false;
        }
        count = affairs.size();//新需求，不加空行了

        Map<Long, String> currentNodeInfos = Collections.EMPTY_MAP;
        if (isCurrentNodesInfo) {
            currentNodeInfos = commonAffairSectionUtils.parseCurrentNodeInfos(affairs);
        }

        for (int i = 0; i < count; i++) {
            MultiRowVariableColumnTemplete.Row row = c.addRow();
            //标题
            MultiRowVariableColumnTemplete.Cell subjectCell = null;
            //接收时间
            MultiRowVariableColumnTemplete.Cell createDateCell = null;
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
            if (isCreateDate) {
                createDateCell = row.addCell();
            }
            if (isEdocMark) {
                edocMarkCell = row.addCell();
            }
            if (isSendUnit) {
                sendUnitCell = row.addCell();
            }
            if (isSendUser) {
                createMemberCell = row.addCell();
            }
            if (isCategory) {
                categoryCell = row.addCell();
            }
            if (isHastenInfo) {
                buttonCell = row.addCell();
            }
            //如果为空则添加默认空行
            if (affairs == null || affairs.size() == 0) {
                continue;
            }
            if (i < affairs.size()) {
                CtpAffair affair = affairs.get(i);
                String memberName = Functions.showMemberName(affair.getSenderId());
                String forwardMember = affair.getForwardMember();
                Integer resentTime = affair.getResentTime();
                String subject = WFComponentUtil.mergeSubjectWithForwardMembers(affair.getSubject(), forwardMember, resentTime, null, width).replaceAll("\r\n", "").replaceAll("\n", "");
                if (subjectCell != null) {
                    subjectCell.setAlt(WFComponentUtil.mergeSubjectWithForwardMembers(affair.getSubject(), forwardMember, resentTime, null, -1).replaceAll("\r\n", "").replaceAll("\n", ""));
                }
                if (affair.getAutoRun() != null && affair.getAutoRun()) {
                    subject = ResourceUtil.getString("collaboration.newflow.fire.subject", subject);
                }
                int app = affair.getApp();
                ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(app);
                String categoryName = ResourceUtil.getString("application." + app + ".label");
                String from = null;
                String listType = "";
                if (isHastenInfo && Integer.valueOf(StateEnum.col_sent.getKey()).equals(affair.getState())) {
                    String hastenUrl = SystemEnvironment.getContextPath() + "/hasten.do?method=openHastenDialog&openFrom=track&affairId=" + affair.getId() + "&memberId=" + affair.getMemberId();
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
                if (appEnum == ApplicationCategoryEnum.edocSend ||
                        appEnum == ApplicationCategoryEnum.edocRec ||
                        appEnum == ApplicationCategoryEnum.edocSign) {
                    switch (StateEnum.valueOf(affair.getState())) {
                        case col_sent:
                            from = "sended";
                            listType = "listSent";
                            break;
                        case col_pending:
                            from = "Pending";
                            listType = "listZcdb";
                            break;
                        case col_done:
                            from = "Done";
                            listType = "listDone";
                            break;
                        default:
                            from = "Done";
                            listType = "listDone";
                    }
                } else {
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
                    listType = from;
                }
                String url = "";

                boolean hasResPerm = this.pendingManager.hasResPerm(affair, user);

                switch (appEnum) {
                    case collaboration:
                        //协同
                        if (subjectCell != null) {
                            subjectCell.setLinkURL("/collaboration/collaboration.do?method=summary&openFrom=" + from + "&affairId=" + affair.getId());
                        }
                        if (categoryCell != null) {
                            //判断是否有资源菜单权限
                            if (hasResPerm) {
                                url = "/collaboration/collaboration.do?method=" + from;
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
                        String openFrom = "";
                        String resCode = "";
                        if (appEnum == ApplicationCategoryEnum.edoc) {
                            switch (StateEnum.valueOf(affair.getState())) {
                                case col_waitSend:
                                    from = "Done";
                                    openFrom = "listSent";
                                    resCode = "F20_gocDovWaitSend";
                                    break;
                                case col_sent:
                                    from = "sended";
                                    openFrom = "listSent";
                                    resCode = "F20_gocDovSend";
                                    break;
                                case col_pending:
                                    from = "Pending";
                                    openFrom = "listPending";
                                    resCode = "F20_govdocPending";
                                    break;
                                case col_done:
                                    from = "Done";
                                    openFrom = "listDone";
                                    resCode = "F20_govDocDone";
                                    break;
                                default:
                                    from = "Done";
                                    openFrom = "listDone";
                            }
                        }
                        switch (subAppEnum) {
                            case edoc_fawen://G6新公文发文
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/govdoc/govdoc.do?method=summary&openFrom=" + openFrom + "&affairId=" + affair.getId() + "&summaryId=" + affair.getObjectId());
                                }
                                if (categoryCell != null) {
                                    //分类链接：判断是否有发文管理，若有则链接到公文管理-待办列表/已办/已发/待发
                                    boolean f11 = user.hasResourceCode("F20_govDocSendManage");
                                    url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&listType=" + openFrom + "&govdocType=1&_resourceCode=F20_govDocSendManage";
                                    if (!f11) {
                                        f11 = user.hasResourceCode(resCode);
                                        url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&listType=" + openFrom + "AllRoot&_resourceCode=" + resCode;
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
                                getEdocExtField(affair, edocMarkCell, sendUnitCell);
                                break;
                            case edoc_qianbao://G6新公文签报
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/govdoc/govdoc.do?method=summary&openFrom=" + openFrom + "&affairId=" + affair.getId() + "&summaryId=" + affair.getObjectId());
                                }
                                if (categoryCell != null) {
                                    //分类链接：判断是否有签报管理，若有则链接到公文管理-待办列表/已办/已发/待发
                                    boolean f11 = user.hasResourceCode("F20_signReport");
                                    url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&listType=" + openFrom + "&govdocType=3&_resourceCode=F20_signReport";
                                    if (!f11) {
                                        f11 = user.hasResourceCode(resCode);
                                        url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&listType=" + openFrom + "AllRoot&_resourceCode=" + resCode;
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
                                getEdocExtField(affair, edocMarkCell, sendUnitCell);
                                break;
                            case edoc_shouwen://G6新公文收文
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/govdoc/govdoc.do?method=summary&openFrom=" + openFrom + "&affairId=" + affair.getId() + "&summaryId=" + affair.getObjectId());
                                }
                                if (categoryCell != null) {
                                    //分类链接：判断是否有收文管理，若有则链接到公文管理-待办列表/已办/已发/待发
                                    boolean f11 = user.hasResourceCode("F20_receiveManage");
                                    url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&listType=" + listType + "&govdocType=2,4&_resourceCode=F20_receiveManage";
                                    if (!f11) {
                                        f11 = user.hasResourceCode(resCode);
                                        url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&listType=" + listType + "AllRoot&_resourceCode=" + resCode;
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
                                getEdocExtField(affair, edocMarkCell, sendUnitCell);
                                break;
                            case edoc_jiaohuan://G6新公文交换
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/govdoc/govdoc.do?method=summary&openFrom=" + openFrom + "&affairId=" + affair.getId() + "&summaryId=" + affair.getObjectId());
                                }
                                if (categoryCell != null) {
                                    //分类链接：判断是否有收文管理，若有则链接到公文管理-待办列表/已办/已发/待发
                                    boolean f11 = user.hasResourceCode("F20_receiveManage");
                                    url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&listType=" + listType + "&govdocType=2,4&_resourceCode=F20_receiveManage";
                                    if (!f11) {
                                        f11 = user.hasResourceCode(resCode);
                                        url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&listType=" + listType + "AllRoot&_resourceCode=" + resCode;
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
                                getEdocExtField(affair, edocMarkCell, sendUnitCell);
                                break;
                            case old_edocSend://G6老公文发文
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/edocController.do?method=detailIFrame&from=" + from + "&affairId=" + affair.getId() + "");
                                }
                                if (categoryCell != null) {
                                    //分类链接：判断是否有发文管理，若有则链接到公文管理-待办列表/已办/已发/待发
                                    boolean f11 = user.hasResourceCode("F20_govDocSendManage");
                                    url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&listType=" + openFrom + "&govdocType=1&_resourceCode=F20_govDocSendManage";
                                    if (!f11) {
                                        f11 = user.hasResourceCode(resCode);
                                        url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&listType=" + openFrom + "AllRoot&_resourceCode=" + resCode;
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
                                getEdocExtField(affair, edocMarkCell, sendUnitCell);
                                break;
                            case old_edocRec://G6老公文收文
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/edocController.do?method=detailIFrame&from=" + from + "&affairId=" + affair.getId() + "");
                                }
                                if (categoryCell != null) {
                                    //分类链接：判断是否有收文管理，若有则链接到公文管理-待办列表/已办/已发/待发
                                    boolean f11 = user.hasResourceCode("F20_receiveManage");
                                    url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&listType=" + listType + "&govdocType=2,4&_resourceCode=F20_receiveManage";
                                    if (!f11) {
                                        f11 = user.hasResourceCode(resCode);
                                        url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&listType=" + listType + "AllRoot&_resourceCode=" + resCode;
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
                                getEdocExtField(affair, edocMarkCell, sendUnitCell);
                                break;
                            case old_edocSign://G6老公文签报
                                if (subjectCell != null) {
                                    subjectCell.setLinkURL("/edocController.do?method=detailIFrame&from=" + from + "&affairId=" + affair.getId() + "");
                                }
                                if (categoryCell != null) {
                                    //分类链接：判断是否有签报管理，若有则链接到公文管理-待办列表/已办/已发/待发
                                    boolean f11 = user.hasResourceCode("F20_signReport");
                                    url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&listType=" + openFrom + "&govdocType=3&_resourceCode=F20_signReport";
                                    if (!f11) {
                                        f11 = user.hasResourceCode(resCode);
                                        url = AppContext.getRawRequest().getContextPath() + "/govdoc/govdoc.do?method=index&listType=" + openFrom + "AllRoot&_resourceCode=" + resCode;
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
                                getEdocExtField(affair, edocMarkCell, sendUnitCell);
                                break;
                        }
                        break;
                    case info:
                        String method = "";
                        switch (StateEnum.valueOf(affair.getState())) {
                            case col_sent:
                                from = "Send";
                                listType = "listInfoReported";
                                method = "infoReport";
                                break;
                            case col_pending:
                                from = "Pending";
                                listType = "listInfoPending";
                                method = "infoAudit";
                                break;
                            case col_done:
                                from = "Done";
                                listType = "listInfoDone";
                                method = "infoAudit";
                                break;
                            default:
                                from = "Done";
                                listType = "listInfoDone";
                                method = "infoAudit";
                        }
                        if (subjectCell != null) {
                            subjectCell.setLinkURL("/info/infoDetail.do?method=summary&id=" + affair.getObjectId() + "&affairId=" + affair.getId() + "&openFrom=" + from);
                        }
                        if (categoryCell != null) {
                            boolean hasDoneRole = ("Done".equals(from) || "Pending".equals(from)) && user.hasResourceCode("F18_infoAudit");
                            boolean hasSendRole = "Send".equals(from) && user.hasResourceCode("F18_infoReport");
                            if (hasDoneRole || hasSendRole) {
                                url = "/info/infomain.do?method=" + method + "&listType=" + listType;
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
                }
                if (createMemberCell != null) {
                    createMemberCell.setAlt(memberName);
                    if (Strings.isNotBlank(memberName) && memberName.length() > 4) {
                        memberName = memberName.substring(0, 4) + "...";
                    }
                    //知会，加签图标
                    if (affair.getFromId() != null) {
                        String title = ResourceUtil.getString("collaboration.pending.addOrJointly.label", Functions.showMemberName(affair.getFromId()));
                        createMemberCell.addExtClasses("ico16 signature_16");
                        createMemberCell.addExtClassesAlt(title);
                    }
                    createMemberCell.setCellContentHTML(memberName);
                }
                if (createDateCell != null) {
                    if (null != affair.getCreateDate()) {
                        String dateTime = WFComponentUtil.getDateTime(affair.getCreateDate(), "yyyy-MM-dd HH:mm");
                        createDateCell.setCellContentHTML("<span class='color_gray' title='" + dateTime + "'>" + dateTime + "</span>");
                    }
                }
                if (subjectCell != null) {
                    subjectCell.setCellContent(subject);
                    //添加‘重要程度’图标
                    if (affair.getImportantLevel() != null && affair.getImportantLevel() > 1 && affair.getImportantLevel() < 6) {//会议没有重要程度，非空判断
                        subjectCell.addExtPreClasses("ico16 important" + affair.getImportantLevel() + "_16");
                    }
                    //添加‘附件’图标
                    if (AffairUtil.isHasAttachments(affair)) {
                        subjectCell.addExtClasses("ico16 vp-attachment");
                    }
                    //添加‘表单授权’图标 只有 是已发的时候才显示表单授权
                    if ("listSent".equals(from)) {
                        if (AffairUtil.getIsRelationAuthority(affair)) {
                            subjectCell.addExtClasses("ico16 authorize_16");
                        }
                    }
                    //添加‘正文类型’图标
                    String bodyType = affair.getBodyType();
                    if (Strings.isNotBlank(bodyType) && !"10".equals(bodyType) && !"30".equals(bodyType)) {
                        String bodyTypeClass = convertPortalBodyType(bodyType);
                        if (!"meeting_video_16".equals(bodyTypeClass)) {
                            bodyTypeClass = "office" + bodyTypeClass;
                        }
                        if (!"html_16".equals(bodyTypeClass)) {
                            subjectCell.addExtClasses("ico16 " + bodyTypeClass);
                        }
                    }

                    boolean isOverTime = affair.isCoverTime() == null ? false : affair.isCoverTime();
                    //超期事件突出显示 TODO section.js已经将该属性相关逻辑注释
                    if (isOverTime) {
                        subjectCell.addExtIcon("/common/images/overTime.gif");
                    }
                    subjectCell.setCellContent(subject.replaceAll("\\r\\n", ""));
                }

                if (isCurrentNodesInfo) {
                    String currentNodesInfoStr = currentNodeInfos.get(affair.getObjectId());

                    if (Strings.isNotBlank(currentNodesInfoStr)) {
                        String currentInfo = Strings.getSafeLimitLengthString(currentNodesInfoStr, 8, "..");
                        if (!ResourceUtil.getString("collaboration.list.finished.label").equals(currentNodesInfoStr)) {
                            currentInfo = ResourceUtil.getString("collaboration.pending.CreateMember.label", currentInfo);
                        }
                        currentInfo = "<span title='" + currentNodesInfoStr + "' >" + currentInfo + "</span>";
                        currentNodesInfoCell.setCellContentHTML(currentInfo);
                    }
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
     * @param affair CtpAffair对象
     * @param edocMarkCell 公文文号
     * @param sendUnitCell 公文发文单位
     */
    private void getEdocExtField(CtpAffair affair, MultiRowVariableColumnTemplete.Cell edocMarkCell, MultiRowVariableColumnTemplete.Cell sendUnitCell) {
        Map<String, Object> extParam = AffairUtil.getExtProperty(affair);
        if (null != extParam && null != extParam.get(AffairExtPropEnums.edoc_edocMark.name()) && edocMarkCell != null) {
            edocMarkCell.setCellContent(String.valueOf(extParam.get(AffairExtPropEnums.edoc_edocMark.name())));
        }
        if (null != extParam && null != extParam.get(AffairExtPropEnums.edoc_sendUnit.name()) && sendUnitCell != null) {
            sendUnitCell.setCellContent(String.valueOf(extParam.get(AffairExtPropEnums.edoc_sendUnit.name())));
        }
    }

    @Override
    public boolean isShowTotal() {
        return true;
    }

    @Override
    public String getResolveFunction(Map<String, String> preference) {
        return MultiRowVariableColumnTemplete.RESOLVE_FUNCTION;
    }
}
