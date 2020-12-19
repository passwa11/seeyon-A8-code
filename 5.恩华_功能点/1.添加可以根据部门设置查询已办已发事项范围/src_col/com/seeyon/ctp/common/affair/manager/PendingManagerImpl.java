package com.seeyon.ctp.common.affair.manager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.ext.accessSeting.manager.AccessSetingManager;
import com.seeyon.apps.ext.accessSeting.manager.AccessSetingManagerImpl;
import com.seeyon.apps.ext.accessSeting.po.DepartmentViewTimeRange;
import com.seeyon.apps.ext.accessSeting.po.TempTemplateStop;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.agent.utils.AgentUtil;
import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.enums.ColOpenFrom;
import com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.edoc.api.EdocApi;
import com.seeyon.apps.edoc.bo.SimpleEdocSummary;
import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.meeting.api.MeetingApi;
import com.seeyon.apps.meeting.bo.MeetingBO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.affair.bo.AffairCondition;
import com.seeyon.ctp.common.affair.bo.PendingRow;
import com.seeyon.ctp.common.affair.bo.AffairCondition.SearchCondition;
import com.seeyon.ctp.common.affair.dao.PendingDao;
import com.seeyon.ctp.common.affair.enums.AffairExtPropEnums;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.affair.vo.OrgDepartmentTreeVo;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.constants.CustomizeConstants;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.customize.manager.CustomizeManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.bo.DetailAttitude;
import com.seeyon.ctp.common.permission.bo.NodePolicy;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.permission.vo.AttitudeVO;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.po.template.CtpTemplateCategory;
import com.seeyon.ctp.common.security.SecurityHelper;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.template.enums.TemplateCategoryConstant;
import com.seeyon.ctp.common.template.manager.TemplateCategoryManager;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.template.vo.TemplateTreeVo;
import com.seeyon.ctp.organization.bo.MemberPost;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgUnit;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.portal.api.PortalApi;
import com.seeyon.ctp.portal.section.AgentSection;
import com.seeyon.ctp.portal.section.CommonAffairSectionUtils;
import com.seeyon.ctp.portal.section.PendingSection;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete.OPEN_TYPE;
import com.seeyon.ctp.portal.section.util.SectionUtils;
import com.seeyon.ctp.portal.util.Constants.SpaceType;
import com.seeyon.ctp.report.chart2.bo.ChartBO;
import com.seeyon.ctp.report.chart2.bo.Title;
import com.seeyon.ctp.report.chart2.bo.serie.PieSerie;
import com.seeyon.ctp.report.chart2.bo.serie.SerieItem;
import com.seeyon.ctp.report.chart2.core.ChartRender;
import com.seeyon.ctp.report.chart2.vo.ChartVO;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.FlipInfo.Order;
import com.seeyon.ctp.util.FlipInfo.SortPair;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.menu.manager.MenuFunction;
import com.seeyon.v3x.worktimeset.exception.WorkTimeSetExecption;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

import net.joinwork.bpm.definition.BPMSeeyonPolicy;

/**
 * @author <a href="tanmf@seeyon.com">Tanmf</a>
 * @date 2013-1-25
 */
public class PendingManagerImpl implements PendingManager {
    private static final Log log = LogFactory.getLog(PendingManagerImpl.class);
    private OrgManager orgManager;
    private PortalApi portalApi;
    private AffairManager affairManager;
    private WorkTimeManager workTimeManager;
    private SuperviseManager superviseManager;
    private EdocApi edocApi;
    private CollaborationApi collaborationApi;
    private MeetingApi meetingApi;
    private PermissionManager permissionManager;
    private ChartRender chartRender;
    private CommonAffairSectionUtils commonAffairSectionUtils;
    private PendingDao pendingDao;
    private CustomizeManager customizeManager;
    private TemplateCategoryManager templateCategoryManager;
    private PendingManager pendingManager;
    private TemplateManager templateManager;
    private PendingSection pendingSection;
    private AgentSection agentSection;
    private AffairAgentMangager affairAgentMangager;


    public AffairAgentMangager getAffairAgentMangager() {
        return affairAgentMangager;
    }

    public void setAffairAgentMangager(AffairAgentMangager affairAgentMangager) {
        this.affairAgentMangager = affairAgentMangager;
    }

    public AgentSection getAgentSection() {
        return agentSection;
    }

    public void setAgentSection(AgentSection agentSection) {
        this.agentSection = agentSection;
    }


    public PendingSection getPendingSection() {
        return pendingSection;
    }

    public void setPendingSection(PendingSection pendingSection) {
        this.pendingSection = pendingSection;
    }

    public TemplateManager getTemplateManager() {
        return templateManager;
    }

    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    public PendingManager getPendingManager() {
        return pendingManager;
    }

    public void setPendingManager(PendingManager pendingManager) {
        this.pendingManager = pendingManager;
    }

    public TemplateCategoryManager getTemplateCategoryManager() {
        return templateCategoryManager;
    }

    public void setTemplateCategoryManager(TemplateCategoryManager templateCategoryManager) {
        this.templateCategoryManager = templateCategoryManager;
    }

    public PendingDao getPendingDao() {
        return pendingDao;
    }

    public void setPendingDao(PendingDao pendingDao) {
        this.pendingDao = pendingDao;
    }

    public CommonAffairSectionUtils getCommonAffairSectionUtils() {
        return commonAffairSectionUtils;
    }

    public void setCommonAffairSectionUtils(CommonAffairSectionUtils commonAffairSectionUtils) {
        this.commonAffairSectionUtils = commonAffairSectionUtils;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public void setMeetingApi(MeetingApi meetingApi) {
        this.meetingApi = meetingApi;
    }

    public void setCollaborationApi(CollaborationApi collaborationApi) {
        this.collaborationApi = collaborationApi;
    }

    public void setEdocApi(EdocApi edocApi) {
        this.edocApi = edocApi;
    }

    public SuperviseManager getSuperviseManager() {
        return superviseManager;
    }

    public void setSuperviseManager(SuperviseManager superviseManager) {
        this.superviseManager = superviseManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setPortalApi(PortalApi portalApi) {
        this.portalApi = portalApi;
    }

    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

    public void setWorkTimeManager(WorkTimeManager workTimeManager) {
        this.workTimeManager = workTimeManager;
    }

    public void setChartRender(ChartRender chartRender) {
        this.chartRender = chartRender;
    }

    public int getUnReadPendingCount(Long memberId, Long fragmentId, String ordinal) {
        return getPendingCount(memberId, fragmentId, ordinal, true);
    }

    public int getPendingCount(Long memberId, Long fragmentId, String ordinal) {
        return getPendingCount(memberId, fragmentId, ordinal, false);
    }

    public int getPendingCount(Long memberId, Map<String, String> preference, boolean isUnRead) {
        String currentPanel = "";
        if (preference != null && preference.size() > 0) {
            currentPanel = SectionUtils.getPanel("all", preference);
        }
        return getPendingCount(memberId, preference, isUnRead, currentPanel);
    }

    private int getPendingCount(Long memberId, Map<String, String> preference, boolean isUnRead, String currentPanel) {
        AffairCondition condition = getPendingSectionAffairCondition(memberId, preference);
//		String showAgentAffair = customizeManager.getCustomizeValue(memberId, CustomizeConstants.SHOW_AGENT_AFFAIR);
//        if("true".equals(showAgentAffair)){
//        	condition.setShowAgentAffair(true);
//        }else{
//        	condition.setShowAgentAffair(false);
//        }

        if ("Vjoin".equals(preference.get("from"))) {//V7.0SP2-F179T4-20移动栏目 : 待办增加公文来源以及公文更多页面移动栏目     移动端待办只获取协同和公文数据\其他栏目只获取协同
            condition.setVjoin(true);
        }
        if (isUnRead) {
            condition.addSearch(SearchCondition.subState, String.valueOf(SubStateEnum.col_pending_unRead.getKey()), "");
        }
        if ("sender".equals(currentPanel)) {
            List<Integer> appEnum = new ArrayList<Integer>();
            // 查询指定发起人，用于查询指定发起人的时候查询比较复杂，所以采用HQL的方式进行查询，其他情况维持原来的逻辑不变
            String tempStr = preference.get(currentPanel + "_value");
            return (Integer) affairManager.getAffairListBySender(memberId, tempStr, condition, true, null, appEnum);
        } else {
            return condition.getPendingCount(affairManager);
        }
    }

    private int getPendingCount(Long memberId, Long fragmentId, String ordinal, boolean isUnRead) {
        Map<String, String> preference = new HashMap<String, String>();
        String currentPanel = "";
        if (fragmentId != null && Strings.isNotBlank(ordinal)) {
            preference = portalApi.getPropertys(fragmentId, ordinal);
            currentPanel = SectionUtils.getPanel("all", preference);
        }
        return getPendingCount(memberId, preference, isUnRead, currentPanel);
    }

    @SuppressWarnings("unchecked")
    public List<CtpAffair> getPendingList(Long memberId, Long fragmentId, String ordinal, int count) {
        Map<String, String> preference = portalApi.getPropertys(fragmentId, ordinal);
        return getPublicPendingList(memberId, preference, count);
    }

    @SuppressWarnings("unchecked")
    public List<CtpAffair> getAISortPendingList(Long memberId, Long fragmentId, String ordinal, int count) {
        Map<String, String> preference = portalApi.getPropertys(fragmentId, ordinal);
        return transPublicPendingList(memberId, preference, count, true);
    }

    @SuppressWarnings("unchecked")
    public List<CtpAffair> getPublicPendingList(Long memberId, Map<String, String> preference, int count) {
        return transPublicPendingList(memberId, preference, count, false);
    }

    /**
     * @param memberId   登录人员ID
     * @param preference
     * @param count
     * @param aiSort     是否启用智能排序
     * @return
     */
    private List<CtpAffair> transPublicPendingList(Long memberId, Map<String, String> preference, int count, boolean aiSort) {
        String currentPanel = SectionUtils.getPanel("all", preference);
        AffairCondition condition = getPendingSectionAffairCondition(memberId, preference);
//        String showAgentAffair = customizeManager.getCustomizeValue(memberId, CustomizeConstants.SHOW_AGENT_AFFAIR);
//        if("true".equals(showAgentAffair)){
//        	condition.setShowAgentAffair(true);
//        }else{
//        	condition.setShowAgentAffair(false);
//        }
        if ("Vjoin".equals(preference.get("from"))) {//V7.0SP2-F179T4-20移动栏目 : 待办增加公文来源以及公文更多页面移动栏目     移动端待办只获取协同和公文数据\其他栏目只获取协同
            condition.setVjoin(true);
        }
        /*String columnStyle=preference.get("columnStyle");
        //待开会议栏目默认值Policy_value=A___30
        String policeValue = preference.get("Policy_value");
        //待开会议栏目编辑为来源是会议后，sources_Policy_value=A___30
        String sourcesPolicyValue=preference.get("sources_Policy_value");*/
        //排序类型默认降序
        Order orderType = Order.DESC;
        //会导致待办栏目中只要配置了【应用或节点权限】选择的内容为会议,那么会影响整个待办的排序,暂时先去掉该逻辑
        /*//如果是待开会议，排序时按升序排列
        if(Strings.isNotBlank(policeValue) && "A___30".equals(policeValue) || Strings.isNotBlank(sourcesPolicyValue) && "A___30".equals(sourcesPolicyValue)){
        	orderType = Order.ASC;
        }*/
        FlipInfo fi = new FlipInfo();
        fi.setNeedTotal(false);
        fi.setPage(1);
        fi.setSize(count);

        //置顶时间排序
        SortPair TopTime = fi.new SortPair("topTime");
        TopTime.setSortOrder(Order.DESC);
        fi.addSortPair(TopTime);
        if (aiSort) {
            //智能排序的权重
            SortPair sortWeight = fi.new SortPair("sortWeight");
            sortWeight.setSortOrder(Order.DESC);
            fi.addSortPair(sortWeight);
        }
        //接收时间降序排序
        SortPair sortReceiveTime = fi.new SortPair("receiveTime");
        sortReceiveTime.setSortOrder(Order.DESC);
        fi.addSortPair(sortReceiveTime);

        List<Integer> appEnum = new ArrayList<Integer>();
        if ("sender".equals(currentPanel)) {
            //查询指定发起人，用于查询指定发起人的时候查询比较复杂，所以采用HQL的方式进行查询，其他情况维持原来的逻辑不变,这个里面会对receiveTime排序
            String tempStr = preference.get(currentPanel + "_value");
            return (List<CtpAffair>) affairManager.getAffairListBySender(memberId, tempStr, condition, false, fi, appEnum);
        } else {
            return condition.getPendingAffair(affairManager, fi);
        }
    }

    private static boolean hasValue(Map query) {
        return query.get("state") != null && Strings.isNotBlank(String.valueOf(query.get("state"))) && !"".equals(query.get("state"));
    }

    private ColManager colManager;

    public void setColManager(ColManager colManager) {
        this.colManager = colManager;
    }

    @SuppressWarnings("unchecked")
    public FlipInfo getMoreList4SectionContion(FlipInfo fi, Map query) throws BusinessException {

        Boolean fromMore = true;
        String TemplatePanel = (String) query.get("templatePanel");
        String senderPanel = (String) query.get("senderPanel");

        if (fi == null || query == null) {
            return fi;
        }
        Boolean isTrack = false;
        if (query.get("isTrack") != null && "true".equals(query.get("isTrack").toString())) {
            isTrack = true;
        }
        //如果不是跟踪的 时候，必须传 state值
        if (!isTrack && query.get("state") == null) {
            return fi;
        }
        //待办更多-没有对应模板待办协同时,需返回空数据
        if (Boolean.TRUE.equals(query.get("hasNoDate"))) {
            return fi;
        }
        User user = AppContext.getCurrentUser();
        Long memberId = AppContext.currentUserId();
        Map<String, String> preference = new HashMap<String, String>();
        String fromM1 = (String) query.get("fromM1");
        String fragmentId = (String) query.get("fragmentId");
        Object proertyFrom = query.get("proertyFrom");
        Object propertyId = query.get("propertyId");
        if (Strings.isNotBlank(fromM1)) {
            preference = (Map<String, String>) JSONUtil.parseJSONString(fromM1);
        } else if (!"Agent".equals(fragmentId) && !"All".equals(fragmentId) && (fragmentId != null && query.get("ordinal") != null)
                && Strings.isBlank(TemplatePanel) && Strings.isBlank(senderPanel)) {
            String fragmentIdStr = query.get("fragmentId").toString();
            if (Strings.isNotBlank(fragmentIdStr)) {
                Long fragmentIdL = Long.parseLong(fragmentIdStr);
                String ordinal = query.get("ordinal").toString();
                preference = portalApi.getPropertys(fragmentIdL, ordinal);
            }
        } else if (proertyFrom != null && propertyId != null) {
            String proertyFromStr = proertyFrom.toString();
            String propertyIdStr = propertyId.toString();
            if (Strings.isDigits(propertyIdStr)) {
                preference = portalApi.getPropertyByCategoryAndId(proertyFromStr, Long.valueOf(propertyIdStr));
            }
        }

        //我的提醒：超期待办
        if (query.get("myRemind") != null && "overTime".equals(query.get("myRemind").toString())) {
            preference.put("sources_overTime_name", "overTime");
            preference.put("panel", "sources");
        }
        //我的提醒：待办会议
        if (query.get("myRemind") != null && "meeting".equals(query.get("myRemind").toString())) {
            preference.put("sources_Policy_value", "A___6");
            preference.put("panel", "sources");
        }

        String currentPanel = SectionUtils.getPanel("all", preference);
        //全部待办
        String panel = (String) query.get("panel");
        if (Strings.isNotBlank(panel) && "all".equals(panel)) {
            preference.put("panel", "all");
            currentPanel = "all";
        }
        //模板分类待办
        if (Strings.isNotBlank(TemplatePanel)) {
            preference.put("panel", "sources");
            preference.put("sources_templete_pending_value", TemplatePanel);
            currentPanel = "sources";
        }
        //组织分类待办
        if (Strings.isNotBlank(senderPanel)) {
            preference.put("panel", "sender");
            preference.put("sender_value", senderPanel);
            currentPanel = "sender";
        }

        List<CtpAffair> affairList = null;
        preference.put("isFromMore", String.valueOf(fromMore));
        AffairCondition condition = getPendingSectionAffairCondition(memberId, preference);

        if ("Vjoin".equals(query.get("from"))) {//移动端待办只显示协同和公文
            condition.setVjoin(true);
        }
        boolean aiSort = false;
        if (AppContext.hasPlugin("ai") && ("true".equals(preference.get("aiSort")) || "1".equals(query.get("aiSortValue")))) {
            aiSort = true;
        }

        //待办栏目要增加是否显示被代理的待办的设置
        if (query.get("state") != null && Strings.isNotBlank(query.get("state").toString()) && Integer.parseInt(query.get("state").toString()) == StateEnum.col_pending.getKey()) {
//        	String showAgentAffair = customizeManager.getCustomizeValue(memberId, CustomizeConstants.SHOW_AGENT_AFFAIR);
//            if("true".equals(showAgentAffair)){
//            	condition.setShowAgentAffair(true);
//            }else{
//            	condition.setShowAgentAffair(false);
//            }
        }
        if (!"pendingSection".equals(query.get("section")) && !"trackSection".equals(query.get("section"))) {
            if (hasValue(query)) {
                int state = Integer.parseInt(query.get("state").toString());
                if (state == StateEnum.col_waitSend.key()) {//待发事项排除信息报送
                    condition.addSearch(SearchCondition.catagory, "waitsend_catagory_all", null, fromMore);
                } else if (state == StateEnum.col_sent.key()) {//已发事项排除信息报送
                    condition.addSearch(SearchCondition.catagory, "sent_catagory_all", null, fromMore);
                } else if (state == StateEnum.col_done.key()) {//已办事项
                    condition.addSearch(SearchCondition.catagory, "done_catagory_all", null, fromMore);
                }
            }
        }
        // 流程来源,拼装来自栏目编辑页面的条件
        String tempStr = preference.get(currentPanel + "_value");
        if ("meeting".equals(query.get("meeting_category"))) {//如果是来至 【已办会议】更多(moreMeeting)，就只查询出会议(会议通知+会议室)相关信息
            condition.addSearch(SearchCondition.catagory, "catagory_meet", null, fromMore);
        } else if (!"all".equals(currentPanel)) {
            if (StringUtils.isNotBlank(tempStr)) {
                // 组装查询条件
                if ("track_catagory".equals(currentPanel)) {//分类
                    condition.addSearch(SearchCondition.catagory, tempStr, null, fromMore);
                } else if ("importLevel".equals(currentPanel)) {//重要程度
                    condition.addSearch(SearchCondition.importLevel, tempStr, null, fromMore);
                } else if ("Policy".equals(currentPanel)) {
                    condition.addSearch(SearchCondition.policy4Portal, tempStr, null, fromMore);
                }
            }
        }
        String conditions = (String) query.get("condition");
        if (Strings.isNotBlank(conditions)) {
            if ("comQuery".equals(conditions)) {
                getCondition4Pending(condition, query);
            } else {
                String textField1 = (String) query.get("textfield");
                String textField2 = (String) query.get("textfield1");
                SearchCondition con = AffairCondition.SearchCondition.valueOf(conditions);
                if (con != null) {
                    //对时间进行特殊处理，当开始日期时，将时间置为最小，当是结束时间时，将时间置为最大
                    if (AffairCondition.SearchCondition.createDate.name().equals(conditions) ||
                            AffairCondition.SearchCondition.receiveDate.name().equals(conditions) ||
                            AffairCondition.SearchCondition.dealDate.name().equals(conditions) ||
                            AffairCondition.SearchCondition.expectedProcessTime.name().equals(conditions)) {
                        if (Strings.isNotBlank(textField1)) {
                            textField1 = Datetimes.getServiceFirstTime(textField1);
                        }
                        if (Strings.isNotBlank(textField2)) {
                            textField2 = Datetimes.getServiceLastTime(textField2);
                        }
                    }
                    if (AffairCondition.SearchCondition.categoryEnum.name().equals(conditions)) {
                        if (String.valueOf(ApplicationCategoryEnum.collaboration.getKey()).equals(textField1)) {
                            condition.addSearch(AffairCondition.SearchCondition.applicationEnum, textField1, "", true);
                        } else if (String.valueOf(ApplicationCategoryEnum.govdocSend.getKey()).equals(textField1)) {
                            condition.addSearch(AffairCondition.SearchCondition.applicationEnum, String.valueOf(ApplicationCategoryEnum.edoc.getKey()), "", true);
                            condition.addSearch(AffairCondition.SearchCondition.categoryEnum, String.valueOf(ApplicationCategoryEnum.edocSend.key()) + ",1", "", true);//老公文数据,1为新公文发文数据
                        } else if (String.valueOf(ApplicationCategoryEnum.govdocRec.getKey()).equals(textField1)) {
                            condition.addSearch(AffairCondition.SearchCondition.applicationEnum, String.valueOf(ApplicationCategoryEnum.edoc.getKey()), "", true);
                            condition.addSearch(AffairCondition.SearchCondition.categoryEnum, String.valueOf(ApplicationCategoryEnum.edocRec.key()) + ",2", "", true);//老公文数据
                        } else if (String.valueOf(ApplicationCategoryEnum.govdocSign.getKey()).equals(textField1)) {
                            condition.addSearch(AffairCondition.SearchCondition.applicationEnum, String.valueOf(ApplicationCategoryEnum.edoc.getKey()), "", true);
                            condition.addSearch(AffairCondition.SearchCondition.categoryEnum, String.valueOf(ApplicationCategoryEnum.edocSign.key()) + ",3", "", true);//老公文数据
                        } else {
                            condition.addSearch(con, textField1, textField2, true);
                        }
                    } else {
                        condition.addSearch(con, textField1, textField2, true);
                    }
                }
            }
        }

        String objectId = (String) query.get("objectId");
        if (Strings.isNotBlank(objectId)) {
            condition.addSearch(AffairCondition.SearchCondition.moduleId, query.get("objectId").toString(), null);
        }

        int state = hasValue(query) ? Integer.parseInt(query.get("state").toString()) : StateEnum.col_pending.key();
        if (isTrack) {
            if ("sender".equals(currentPanel)) {

                condition.addSearch(AffairCondition.SearchCondition.applicationEnum, "1,4", null);
                condition.setIsTrack(true);
                List<Integer> appEnum = new ArrayList<Integer>();
                //查询指定发起人
                affairList = (List<CtpAffair>) affairManager.getAffairListBySender(memberId, tempStr, condition, false, fi, appEnum);
            } else {
                if (query.get("onlyCount") != null && "true".equals(query.get("onlyCount").toString())) {
                    affairList = new ArrayList<CtpAffair>();
                    fi.setTotal(condition.getTrackCount(affairManager));//磁贴栏目只需要查询数量
                } else {
                    affairList = condition.getTrackAffair(affairManager, fi);
                }
            }
        } else {
            //根据前端传递过来的页码重置页码
            if (query.get("page") != null) {
                fi.setPage(Integer.parseInt(query.get("page").toString()));
            }
            condition.setState(StateEnum.valueOf(state));
            if (fi.getSortField() == null) {
                if (state == StateEnum.col_pending.key()) {//如果是待办事项，先按照置顶排序
                    SortPair topTime = fi.new SortPair("topTime");
                    topTime.setSortOrder(Order.DESC);
                    fi.addSortPair(topTime);

                    if (aiSort) {
                        SortPair sortWeight = fi.new SortPair("sortWeight");
                        sortWeight.setSortOrder(Order.DESC);
                        fi.addSortPair(sortWeight);
                    }
                }

                SortPair sortPair = null;
                if (state == StateEnum.col_done.key()) {
                    if ("meeting".equals(query.get("meeting_category"))) {
                        sortPair = fi.new SortPair("receiveTime");
                    } else {
                        sortPair = fi.new SortPair("completeTime");
                    }
                } else if (state == StateEnum.col_pending.key()) {
                    sortPair = fi.new SortPair("receiveTime");
                } else {
                    sortPair = fi.new SortPair("createDate");
                }
                //会导致待办栏目中只要配置了【应用或节点权限】选择的内容为会议,那么会影响整个待办的排序,暂时先去掉该逻辑
                /*if(state ==StateEnum.col_pending.key()){//如果是待办事项， 查找会议
                	 //待开会议栏目默认值Policy_value=A___30
                    String policeValue = preference.get("Policy_value");
                    //待开会议栏目编辑为来源是会议后，sources_Policy_value=A___30
                    String sourcesPolicyValue=preference.get("sources_Policy_value");
                    //如果是待开会议，排序时按升序排列
                    if(Strings.isNotBlank(policeValue) && "A___30".equals(policeValue) || Strings.isNotBlank(sourcesPolicyValue) && "A___30".equals(sourcesPolicyValue)){
                    	sortPair.setSortOrder(Order.ASC);
                    }else{
                    	sortPair.setSortOrder(Order.DESC);
                    }
                }*/
                sortPair.setSortOrder(Order.DESC);
                if (sortPair != null) {
                    fi.addSortPair(sortPair);
                }
            }
            List<Integer> appEnum = new ArrayList<Integer>();
            String groupBy = String.valueOf(query.get("isGroupBy"));
            boolean isGroupBy = false;
            if (!"".equals(groupBy) && Strings.isNotBlank(groupBy)) {
                isGroupBy = Boolean.parseBoolean(groupBy);
            }
            if ("sender".equals(currentPanel)) {
                //查询指定发起人，用于查询指定发起人的时候查询比较复杂，所以采用HQL的方式进行查询，其他情况维持原来的逻辑不变
                affairList = (List<CtpAffair>) affairManager.getAffairListBySender(memberId, tempStr, condition, false, fi, appEnum, isGroupBy);
            } else {
                if (state == StateEnum.col_pending.key()) {
                    if (query.get("onlyCount") != null && "true".equals(query.get("onlyCount").toString())) {
                        affairList = new ArrayList<CtpAffair>();
                        fi.setTotal(condition.getPendingCount(affairManager));//磁贴栏目只需要查询数量
                    } else {
                        affairList = condition.getPendingAffair(affairManager, fi);
                    }
                } else {
                    if (state == StateEnum.col_done.key() && isGroupBy) {
                        affairList = (List<CtpAffair>) affairManager.getDeduplicationAffairs(memberId, condition, false, fi);
                    } else {
                        affairList = condition.getSectionAffair(affairManager, state, fi, isGroupBy, false);
                    }
                }
            }
        }
        String rowStr = "";
        if (query.get("rowStr") != null) {
            rowStr = (String) query.get("rowStr");
        } else {
            if (Strings.isBlank(preference.get("rowList"))) {
                rowStr = "subject,receiveTime,sendUser,category,currentNodesInfo";
            } else {
                rowStr = preference.get("rowList");
            }
        }
        List<CtpAffair> affairListClone = new ArrayList<CtpAffair>();
        CtpAffair ctpAffairORG = null;
        CtpAffair ctpAffairclo = null;
        for (int a = 0; a < affairList.size(); a++) {
            ctpAffairORG = affairList.get(a);
            try {
                ctpAffairclo = (CtpAffair) ctpAffairORG.clone();
                ctpAffairclo.setId(ctpAffairORG.getId());
            } catch (CloneNotSupportedException e) {
            }
            affairListClone.add(ctpAffairclo);
        }
        boolean processingProgress = Boolean.TRUE.equals(query.get("processingProgress"));
        if ((query.containsKey("showCurNodesInfo") && (Boolean) query.get("showCurNodesInfo")) || isTrack) {
            if (Strings.isNotBlank(rowStr) && rowStr.indexOf("currentNodesInfo") == -1) {
                rowStr += ",currentNodesInfo";
            }
        }
        if (processingProgress) {
            if (Strings.isNotBlank(rowStr) && rowStr.indexOf("processingProgress") == -1) {
                rowStr += ",processingProgress";//当前回复数
            }
        }
        if (isG6Version()) {//政务版本,适配M3首页显示文号显示
            rowStr += ",edocMark";
        }
        //【恩华药业】zhou:协同过滤掉设定范围内的数据【开始】
        FlipInfo flipInfo = new FlipInfo();
        flipInfo.setSize(10000);
        List<CtpAffair> _affairlist = condition.getSectionAffair(affairManager, state, flipInfo, false, false);

        List<CtpAffair> newAffairs = new ArrayList<>();
        AccessSetingManager manager = new AccessSetingManagerImpl();
        for (CtpAffair affair : _affairlist) {
            if (affair.getApp() == 1) {
                //已办根据当前代办人过滤数据。
                Long currentUserId = affair.getMemberId();
                V3xOrgMember member = orgManager.getMemberById(currentUserId);
                Long userId = member.getId();
                Map<String, Object> map = new HashMap<>();
                map.put("memberId", userId);
                List<DepartmentViewTimeRange> list = manager.getDepartmentViewTimeRange(map);
                if (list.size() > 0) {
                    DepartmentViewTimeRange range = list.get(0);
                    if (!"".equals(range.getDayNum()) && null != range.getDayNum() && Long.parseLong(range.getDayNum()) > 0l) {
                        LocalDateTime end = LocalDateTime.now();
                        LocalDateTime start = LocalDateTime.now().minusDays(Long.parseLong(range.getDayNum()));
                        Long startTime = start.toInstant(ZoneOffset.of("+8")).toEpochMilli();
                        Long endTime = end.toInstant(ZoneOffset.of("+8")).toEpochMilli();
                        Long objectId2 = affair.getObjectId();
                        ColSummary colSummary = colManager.getColSummaryById(objectId2);
                        Date createDate = colSummary.getCreateDate();
                        if (createDate.getTime() > startTime.longValue() && createDate.getTime() < endTime.longValue()) {
                            newAffairs.add(affair);
                        }
                    } else if (!"".equals(range.getDayNum()) && null != range.getDayNum() && Long.parseLong(range.getDayNum()) == 0l) {
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
        //zhou:修改第一个参数

        //[恩华] zhou: 模板停用流程【开始】
        List<TempTemplateStop> stops = manager.getStatusIsZero();
        List<String> templates = new ArrayList<>();
        for (int i = 0; i < stops.size(); i++) {
            templates.add(stops.get(i).getTemplateId());
        }
        List<CtpAffair> tempList = new ArrayList<>();
        for (int i = 0; i < newAffairs.size(); i++) {
            CtpAffair vo = newAffairs.get(i);
            String templateId = "";
            if (null != vo.getTempleteId() && !"".equals(vo.getTempleteId())) {
                templateId = Long.toString(vo.getTempleteId());
                if (!templates.contains(templateId)) {
                    tempList.add(vo);
                }
            } else {
                tempList.add(vo);
            }
        }
        List<CtpAffair> lastList = new ArrayList<>();
        int page = fi.getPage();
        int pagesize = fi.getSize();
        if (tempList.size() < fi.getSize()) {
            for (int i = 0; i < tempList.size(); i++) {
                lastList.add(tempList.get(i));
            }
        } else {
            if ((tempList.size() - ((page - 1) * pagesize)) < fi.getSize()) {
                int val = ((page - 1) * pagesize) + (tempList.size() - ((page - 1) * pagesize));
                for (int i = (page - 1) * pagesize; i < val; i++) {
                    lastList.add(tempList.get(i));
                }
            } else {
                int val = ((page - 1) * pagesize) + fi.getSize();
                for (int i = (page - 1) * pagesize; i < val; i++) {
                    lastList.add(tempList.get(i));
                }
            }

        }
        fi.setTotal(tempList.size());
        //[恩华] zhou: 模板停用流程【结束】
        List<PendingRow> voList = affairList2PendingRowList(lastList, user, null, false, rowStr, state, query);
//        List<PendingRow> voList  = affairList2PendingRowList(affairListClone, user, null, false,rowStr, state,query);
        fi.setData(voList);
        return fi;
    }

    /**
     * 当前用户的代理事项查询
     *
     * @param query: rowStr:栏目列配置时的key,多个列用,分割(比如"subject,receiveTime,sendUser,category")		Y
     *               condition:查询条件key		N
     *               textfield:用户输入查询值1(比如标题\开始时间)		N
     *               textfield1:用户输入查询值2(比如结束时间)			N
     *               aiSortValue:1AI智能排序			N
     * @return fi
     */
    public FlipInfo getMoreAgentList4SectionContion(FlipInfo fi, Map query) throws BusinessException {
        User user = AppContext.getCurrentUser();
        Long memberId = user.getId();
        Object[] agentObj = AgentUtil.getUserAgentToMap(memberId);
        boolean agentToFlag = (Boolean) agentObj[0];
        Map<Integer, List<AgentModel>> ma = (Map<Integer, List<AgentModel>>) agentObj[1];

        AffairCondition condition = new AffairCondition(memberId, StateEnum.col_pending);
        condition.setAgent(agentToFlag, ma);

        //更多待办的条件查询
        String conditions = (String) query.get("condition");
        if (Strings.isNotBlank(conditions)) {
            if ("comQuery".equals(conditions)) {
                getCondition4Pending(condition, query);
            } else {
                String textField1 = (String) query.get("textfield");
                String textField2 = (String) query.get("textfield1");

                SearchCondition con = AffairCondition.SearchCondition.valueOf(conditions);
                if (con != null) {
                    //对时间进行特殊处理，当开始日期时，将时间置为最小，当是结束时间时，将时间置为最大
                    if (AffairCondition.SearchCondition.createDate.name().equals(conditions) ||
                            AffairCondition.SearchCondition.receiveDate.name().equals(conditions) ||
                            AffairCondition.SearchCondition.dealDate.name().equals(conditions) ||
                            AffairCondition.SearchCondition.expectedProcessTime.name().equals(conditions)) {
                        if (Strings.isNotBlank(textField1)) {
                            textField1 = Datetimes.getServiceFirstTime(textField1);
                        }
                        if (Strings.isNotBlank(textField2)) {
                            textField2 = Datetimes.getServiceLastTime(textField2);
                        }
                    }
                    condition.addSearch(con, textField1, textField2);
                }
            }
        }
        if (fi.getSortField() == null) {
            SortPair TopTime = fi.new SortPair("topTime");
            TopTime.setSortOrder(Order.DESC);
            fi.addSortPair(TopTime);

            if (AppContext.hasPlugin("ai") && "1".equals(query.get("aiSortValue"))) {
                SortPair sortWeight = fi.new SortPair("sortWeight");
                sortWeight.setSortOrder(Order.DESC);
                fi.addSortPair(sortWeight);
            }

            SortPair receiveTime = fi.new SortPair("receiveTime");
            receiveTime.setSortOrder(Order.DESC);
            fi.addSortPair(receiveTime);
        }

        List<CtpAffair> affairList = condition.getAgentPendingAffair(affairManager, fi);

        List<PendingRow> voList = affairList2PendingRowList(affairList, user, null, true, String.valueOf(query.get("rowStr")), StateEnum.col_pending.key(), query);
        fi.setData(voList);


        return fi;
    }

    public Map<String, Integer> getGroupByApp(Long memberId, StateEnum state, Map<String, String> preference) {
        Map<String, Integer> result = new HashMap<String, Integer>();
        AffairCondition condition = getPendingSectionAffairCondition(memberId, preference);
        List<Integer> appEnum = new ArrayList<Integer>();
        List<Object[]> temp = new ArrayList<Object[]>();
        String currentPanel = SectionUtils.getPanel("all", preference);
        if ("sender".equals(currentPanel)) {
            //查询指定发起人，用于查询指定发起人的时候查询比较复杂，所以采用HQL的方式进行查询，其他情况维持原来的逻辑不变
            String tempStr = preference.get(currentPanel + "_value");
            temp = (List<Object[]>) affairManager.getAffairListBySender(memberId, tempStr, condition, true, null, appEnum, "app", "sub_app");
        } else {
            temp = condition.group(appEnum, "app", "subApp");
        }

        int allColl = 0; //所有协同
        int zyxt = 0;//自由协同，
        int xtbdmb = 0;//协同/表单模板，
        int shouWen = 0;//收文
        int faWen = 0;//发文
        int daiFaEdoc = 0;//待发送
        int daiQianShou = 0;//待签收
        int daiDengJi = 0;//待登记
        int daiFenfa = 0;//待分发
        int qianBao = 0;//签报
        int huiYi = 0;//会议
        int huiYiShi = 0;//会议室审批
        int diaoCha = 0; //调查
        int daiShenPiGGXX = 0;//待审批公共信息 (公告，新闻，调查，讨论= 0
        int daiShenPiZHBG = 0;//待审批综合办公审批
        if (!temp.isEmpty()) {
            for (int _count = 0; _count < temp.size(); _count++) {
                Object[] os = null;
                try {
                    os = temp.get(_count);
                } catch (Exception e) {
                    log.error("*****" + e.getLocalizedMessage() + "currentPanel:" + currentPanel, e);
                    continue;
                }
                Integer appInt = ((Number) os[0]).intValue();
                Integer subApp = 0;
                if (os[1] != null) {
                    subApp = ((Number) os[1]).intValue();
                }
                int count = ((Number) os[2]).intValue();

                ApplicationCategoryEnum app = ApplicationCategoryEnum.valueOf(appInt);
                if (app == null) {
                    continue;
                }
                switch (app) {
                    case collaboration:
                        allColl += count;
                        if (Strings.equals(subApp, ApplicationSubCategoryEnum.collaboration_self.key())) {
                            zyxt = count; //待审核
                        } else if (Strings.equals(subApp, ApplicationSubCategoryEnum.collaboration_tempate.key())) {
                            xtbdmb = count; //填写
                        }
                        break;
                    case edoc://G6公文
                        if (Strings.equals(subApp, ApplicationSubCategoryEnum.edoc_fawen.key())
                                || Strings.equals(subApp, ApplicationSubCategoryEnum.old_edocSend.key())) {
                            faWen += count; //G6新公文-发文/G6老公文-发文
                        } else if (Strings.equals(subApp, ApplicationSubCategoryEnum.edoc_shouwen.key())
                                || Strings.equals(subApp, ApplicationSubCategoryEnum.edoc_jiaohuan.key())
                                || Strings.equals(subApp, ApplicationSubCategoryEnum.old_edocRec.key())) {
                            shouWen += count; //G6新公文-收文/交换/G6老公文-收文
                        } else if (Strings.equals(subApp, ApplicationSubCategoryEnum.edoc_qianbao.key())
                                || Strings.equals(subApp, ApplicationSubCategoryEnum.old_edocSign.key())) {
                            qianBao += count; //G6新公文-签报/G6老公文-签报
                        } else if (Strings.equals(subApp, ApplicationSubCategoryEnum.old_exSend.key())) {
                            daiFaEdoc += count; //G6老公文-交换
                        } else if (Strings.equals(subApp, ApplicationSubCategoryEnum.old_exSign.key())) {
                            daiQianShou += count; //G6老公文-签收
                        } else if (Strings.equals(subApp, ApplicationSubCategoryEnum.old_edocRegister.key())) {
                            daiDengJi += count; //G6老公文-登记
                        } else if (Strings.equals(subApp, ApplicationSubCategoryEnum.old_edocRecDistribute.key())) {
                            daiFenfa += count;//待分发
                        }
                        break;
                    case meeting:
                        huiYi += count;
                        break;
                    case meetingroom:
                        huiYiShi += count;
                        break;
                    case news:
                    case bulletin:
                        daiShenPiGGXX += count;
                        break;
                    case inquiry:
                        if (Strings.equals(subApp, ApplicationSubCategoryEnum.inquiry_audit.key())) {
                            daiShenPiGGXX += count; //待审核
                        } else if (Strings.equals(subApp, ApplicationSubCategoryEnum.inquiry_write.key())) {
                            diaoCha += count; //填写
                        }
                        break;
                    case office:
                        daiShenPiZHBG += count;
                        break;
                    default:
                        break;
                }
            }
        }

        result.put("allColl", allColl); //所有协同
        result.put("zyxt", zyxt);//自由协同，
        result.put("xtbdmb", xtbdmb);//协同/表单模板，
        result.put("shouWen", shouWen);//收文
        result.put("faWen", faWen);//发文
        result.put("qianBao", qianBao);//签报
        result.put("daiFaEdoc", daiFaEdoc);//待发送
        result.put("daiQianShou", daiQianShou);//待签收
        result.put("daiDengJi", daiDengJi);//待登记
        result.put("daiFenfa", daiFenfa);//待分发
        result.put("huiYi", huiYi);//会议审批
        result.put("huiYiShi", huiYiShi);//会议室审批
        result.put("diaoCha", diaoCha);//调查
        result.put("daiShenPiGGXX", daiShenPiGGXX);//待审批公共信息 (公告，新闻，调查，讨论)
        result.put("daiShenPiZHBG", daiShenPiZHBG);//待审批综合办公审批

        return result;
    }

    public Map<Integer, Integer> getGroupByImportment(Long memberId, StateEnum state, Map<String, String> preference, Integer... appKeys) {
        Map<Integer, Integer> result = new HashMap<Integer, Integer>();
        List<Object[]> temp = new ArrayList<Object[]>();
        temp = getCalcuteResult(memberId, preference, "important_level", "importantLevel", appKeys);
        int other = 0;
        for (int _count = 0; _count < temp.size(); _count++) {
            Object[] o = null;
            try {
                o = temp.get(_count);
            } catch (Exception e) {
                log.error("*****" + e.getLocalizedMessage() + "***memeberId:" + memberId + "***preference=" + preference + "***appKeys=" + appKeys, e);
                continue;
            }
            if (o != null && o[0] != null) {
                result.put(((Number) o[0]).intValue(), ((Number) o[1]).intValue());
            } else if (o != null && o[0] == null && o[1] != null) {
                other += ((Number) o[1]).intValue();
            }
        }
        result.put(-1, other);
        return result;
    }

    public Map<Integer, Integer> getGroupBySubState(Long memberId, StateEnum state, Map<String, String> preference, Integer... appKeys) {
        Map<Integer, Integer> result = new HashMap<Integer, Integer>();
        List<Object[]> temp = new ArrayList<Object[]>();
        temp = getCalcuteResult(memberId, preference, "sub_state", "subState", appKeys);
        for (Object[] o : temp) {
            if (o != null && o[0] != null) {
                result.put(((Number) o[0]).intValue(), ((Number) o[1]).intValue());
            } else if (o != null && o[0] == null && o[1] != null) {
                result.put(0, ((Number) o[1]).intValue());
            }
        }
        return result;
    }

    /**
     * 计算统计结果
     *
     * @param memberId
     * @param preference
     * @param senderBroupByName
     * @param groupByPropertyName
     * @param appKeys
     * @return
     */
    private List<Object[]> getCalcuteResult(Long memberId,
                                            Map<String, String> preference, String senderBroupByName, String groupByPropertyName, Integer... appKeys) {
        List<Object[]> temp = new ArrayList<Object[]>();
        AffairCondition condition = getPendingSectionAffairCondition(memberId, preference);
        boolean hasApp = appKeys != null && appKeys.length > 0;
        List<Integer> appEnum = new ArrayList<Integer>();
        if (hasApp) {
            appEnum = Strings.newArrayList(appKeys);
        }
        String currentPanel = SectionUtils.getPanel("all", preference);
        if ("sender".equals(currentPanel)) {
            //查询指定发起人，用于查询指定发起人的时候查询比较复杂，所以采用HQL的方式进行查询，其他情况维持原来的逻辑不变
            String tempStr = preference.get(currentPanel + "_value");
            temp = (List<Object[]>) affairManager.getAffairListBySender(memberId, tempStr, condition, true, null, appEnum, senderBroupByName);
        } else {
            temp = condition.group(appEnum, groupByPropertyName);
        }
        return temp;
    }

    public Map<String, Integer> getGroupByIsOverTime(Long memberId, StateEnum state, Map<String, String> preference, Integer... appKeys) {
        Map<String, Integer> result = new HashMap<String, Integer>();
        String currentPanel = SectionUtils.getPanel("all", preference);
        List<Object[]> temp = new ArrayList<Object[]>();
        temp = getCalcuteResult(memberId, preference, "IS_COVER_TIME", "coverTime", appKeys);
        int nullSize = 0;
        int falseSize = 0;
        int trueSize = 0;
        for (Object[] o : temp) {
            if (o != null) {
                if (o[0] == null) {
                    nullSize = ((Integer) o[1]).intValue();
                } else if ("sender".equals(currentPanel)) {
                    if (((Number) o[0]).byteValue() == 0) {
                        falseSize = ((Number) o[1]).intValue();
                    }
                    if (((Number) o[0]).byteValue() == 1) {
                        trueSize = ((Number) o[1]).intValue();
                    }
                } else {
                    if (!(Boolean) o[0]) {
                        falseSize = ((Number) o[1]).intValue();
                    }
                    if ((Boolean) o[0]) {
                        trueSize = ((Number) o[1]).intValue();
                    }
                }
            }
        }
        result.put("noOverdue", nullSize + falseSize);
        result.put("overdue", trueSize);

        return result;
    }

    @SuppressWarnings("unchecked")
    private AffairCondition getPendingSectionAffairCondition(Long memberId, Map<String, String> preference) {

        String currentPanel = SectionUtils.getPanel("all", preference);
        String isFromMore = preference.get("isFromMore");
        boolean fromMore = false;
        if (Strings.isNotBlank(isFromMore)) {
            fromMore = Boolean.valueOf(fromMore);
        }
        Object[] agentObj = AgentUtil.getUserAgentToMap(memberId);
        boolean agentToFlag = (Boolean) agentObj[0];
        Map<Integer, List<AgentModel>> ma = (Map<Integer, List<AgentModel>>) agentObj[1];
        List<AgentModel> agentModelList = (List<AgentModel>) agentObj[2];
        /*
         * 标准产品中,没有该类待办,暂时注释掉
         * ApplicationCategoryEnum.info,ApplicationCategoryEnum.infoStat
         * */

        List<ApplicationCategoryEnum> apps = new ArrayList<ApplicationCategoryEnum>();
        apps.add(ApplicationCategoryEnum.collaboration);
        apps.add(ApplicationCategoryEnum.edoc);
        apps.add(ApplicationCategoryEnum.meeting);
        apps.add(ApplicationCategoryEnum.bulletin);
        apps.add(ApplicationCategoryEnum.news);
        apps.add(ApplicationCategoryEnum.inquiry);
        apps.add(ApplicationCategoryEnum.office);
        apps.add(ApplicationCategoryEnum.meetingroom);
        apps.add(ApplicationCategoryEnum.edocRecDistribute);
        apps.add(ApplicationCategoryEnum.info);

        if ("all".equals(currentPanel)) {
            apps.add(ApplicationCategoryEnum.templateApprove);
        }

        AffairCondition condition = new AffairCondition(memberId, StateEnum.col_pending,
                apps

        );

        if ("all".equals(currentPanel)) {
        } else if ("overTime".equals(currentPanel)) {
            condition.addSearch(SearchCondition.overTime, null, null, fromMore);
        } else if ("freeCol".equals(currentPanel)) {//自由协同
            condition.addSearch(SearchCondition.catagory, "catagory_coll", null, fromMore);
        } else if (!"agent".equals(currentPanel)) {
            if (Strings.isNotBlank(currentPanel) && "sources".equals(currentPanel)) {
                condition.addSourceSearchCondition(preference, fromMore);
            } else {
                String tempStr = preference.get(currentPanel + "_value");
                if (Strings.isBlank(tempStr) || "null".equalsIgnoreCase(tempStr)) {
                    //没有重要程度为-1的数据，加上这个条件就是为了查不出数据
                    //   condition.addSearch(SearchCondition.importLevel, "-1", null,fromMore);
                } else {
                    if ("templete_pending".equals(currentPanel)) {
                        condition.addSearch(SearchCondition.templete, tempStr, null, fromMore);
                    } else if ("Policy".equals(currentPanel)) {
                        condition.addSearch(SearchCondition.policy4Portal, tempStr, null, fromMore);
                    } else if ("importLevel".equals(currentPanel)) {
                        condition.addSearch(SearchCondition.importLevel, tempStr, null, fromMore);
                    } else if ("catagory".equals(currentPanel)) {
                        condition.addSearch(SearchCondition.catagory, tempStr, null, fromMore);
                    } else if ("track_catagory".equals(currentPanel)) {
                        condition.addSearch(SearchCondition.catagory, tempStr, null, fromMore);
                    } else if ("handlingState".equals(currentPanel)) {
                        condition.addSearch(SearchCondition.handlingState, tempStr, null, fromMore);
                    }
                }
            }
        }
        condition.setAgent(agentToFlag, ma, agentModelList);
        return condition;
    }

    private boolean isG6Version() {
        String isG6 = SystemProperties.getInstance().getProperty("edoc.isG6");
        if ("true".equals(isG6)) {
            return true;
        }
        return false;
    }

    public List<PendingRow> affairList2PendingRowList(List<CtpAffair> affairs, User user, String currentPanel, boolean isProxy, String rowStr, int state) throws BusinessException {
        return affairList2PendingRowList(affairs, user, currentPanel, isProxy, rowStr, state, new HashMap());
    }

    private List<PendingRow> affairList2PendingRowList(List<CtpAffair> affairs, User user, String currentPanel,
                                                       boolean isProxy, String rowStr, int state, Map paramObj) throws BusinessException {
        /**
         * 用于显示当前待办人
         * 将affairs中公文和协同的取出来放在不同的List中，然后通过in sql语句将各自的summary的currentNodesInfo取出保存在PendingRow中
         */
        List<Long> collIdList = new ArrayList<Long>();
        List<Long> edocIdList = new ArrayList<Long>();

        //获取权限
        Map<String, Permission> permissonMap = new HashMap<String, Permission>();
        List<Permission> permissonList = permissionManager.getPermissionsByCategory(EnumNameEnum.col_flow_perm_policy.name(), AppContext.currentAccountId());
        for (Permission permission : permissonList) {
            permissonMap.put(permission.getName(), permission);
        }

        List<Long> stepBackAffairIds = new ArrayList<Long>();
        for (CtpAffair affair : affairs) {
            //处理affair中虚拟的回退协同数据
            if (affair.getApp() == ApplicationCategoryEnum.stepBackData.key()) {
                Map<String, Object> map = new HashMap<String, Object>();
                map = (Map<String, Object>) XMLCoder.decoder(affair.getExtProps());
                if (map != null && map.get(AffairExtPropEnums.col_pending_relation_id.name()) != null) {
                    Long affairId = (Long) map.get(AffairExtPropEnums.col_pending_relation_id.name());
                    stepBackAffairIds.add(affairId);
                    //存储数据,后续就不用解析了
                    affair.putExtraAttr(AffairExtPropEnums.col_pending_relation_id.name(), affairId);
                }
            }
        }
        Map<Long, CtpAffair> stepBackAffairMap = new HashMap<Long, CtpAffair>();
        if (stepBackAffairIds.size() > 0) {
            List<CtpAffair> stepBackAffairs = affairManager.get(stepBackAffairIds);
            for (CtpAffair affair : stepBackAffairs) {
                stepBackAffairMap.put(affair.getId(), affair);
            }
        }

        boolean isGov = isG6Version();
        boolean needCheckIsEdocRegister = false;
        boolean needCheckIsExchangeRole = false;
        boolean needCheckHasEdocDistributeGrant = false;
        List<CtpAffair> newAffairs = new ArrayList<CtpAffair>();
        for (CtpAffair affair : affairs) {
            //处理affair中虚拟的回退协同数据
            if (affair.getApp() == ApplicationCategoryEnum.stepBackData.key()) {
                Long affairId = (Long) affair.getExtraAttr(AffairExtPropEnums.col_pending_relation_id.name());
                if (stepBackAffairMap.get(affairId) != null) {
                    affair = stepBackAffairMap.get(affairId);
                }
            }
            //协同
            if (affair.getApp() == ApplicationCategoryEnum.collaboration.key()) {
                collIdList.add(affair.getObjectId());
            }
            //公文
            if (affair.getApp() == ApplicationCategoryEnum.edoc.key()) {
                ApplicationSubCategoryEnum subAppEnum = ApplicationSubCategoryEnum.valueOf(affair.getSubApp());
                if (subAppEnum.equals(ApplicationSubCategoryEnum.old_edocRegister)) {
                    needCheckIsEdocRegister = true;
                } else if (subAppEnum.equals(ApplicationSubCategoryEnum.old_exSend)) {
                    needCheckIsExchangeRole = true;
                } else if (subAppEnum.equals(ApplicationSubCategoryEnum.old_edocRecDistribute)) {
                    needCheckHasEdocDistributeGrant = true;
                }
                edocIdList.add(affair.getObjectId());
            }
            newAffairs.add(affair);
        }
        affairs = newAffairs;

        Map<Long, ColSummary> colSummaryMap = new HashMap<Long, ColSummary>();

        boolean isCurrentNodesInfo = rowStr.indexOf("currentNodesInfo") != -1;
        boolean isProcessingProgress = rowStr.indexOf("processingProgress") != -1; //回复数
        boolean isPlaceOfMeeting = rowStr.indexOf("placeOfMeeting") != -1; //会议地点
        //TODO M3待办要查询
        boolean isM3todo = false;
        if (null != paramObj && "true".equals(paramObj.get("isM3todo"))) {
            isM3todo = true;
        }
        if (isCurrentNodesInfo || isProcessingProgress || isM3todo) { //需要查询当前待办人和回复人数的时候才去查下affair
            if (Strings.isNotEmpty(collIdList)) {
                List<ColSummary> colSummarys = collaborationApi.findColSummarys(collIdList);
                for (ColSummary summary : colSummarys) {
                    colSummaryMap.put(summary.getId(), summary);
                }
            }
        }

        Map<Long, SimpleEdocSummary> edocMap = new HashMap<Long, SimpleEdocSummary>();
        boolean isEdocRegister = false;
        boolean isExchangeRole = false;
        boolean hasEdocDistributeGrant = false;

        if (Strings.isNotEmpty(edocIdList) && SystemEnvironment.hasPlugin("edoc")) {
            if (isCurrentNodesInfo) {
                List<SimpleEdocSummary> summarys = edocApi.findSimpleEdocSummarysByIds(edocIdList);
                for (SimpleEdocSummary summary : summarys) {
                    edocMap.put(summary.getId(), summary);
                }
            }

            try {
                if (needCheckIsEdocRegister) {
                    isEdocRegister = edocApi.isEdocCreateRole(user.getId(), user.getLoginAccount(), EdocEnum.edocType.recEdoc.ordinal());
                }
            } catch (Exception e) {
                log.error("", e);
            }
            if (needCheckIsExchangeRole) {
                isExchangeRole = edocApi.isExchangeRole(user.getId(), user.getLoginAccount());
            }

            try {
                if (needCheckHasEdocDistributeGrant) {
                    hasEdocDistributeGrant = edocApi.isEdocCreateRole(user.getId(), user.getLoginAccount(), EdocEnum.edocType.distributeEdoc.ordinal());
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }

        //缓存会议地点信息
        Map<Long, String> mapMeeting = new HashMap<Long, String>();
        //控制是否查询会议回执统计信息
        Map<Long, Map<String, Integer>> map_replyCount = new HashMap<Long, Map<String, Integer>>();
        if ((isProcessingProgress || isPlaceOfMeeting) && meetingApi != null) {
            Map<String, Integer> map_reply = new HashMap<String, Integer>();
            List<Long> meetingIds = new ArrayList<Long>();
            for (CtpAffair affair : affairs) {
                if (affair.getApp().equals(ApplicationCategoryEnum.meeting.getKey())) {
                    if (DateUtil.currentDate().after(affair.getCompleteTime())) {
                        continue;
                    }
                    meetingIds.add(affair.getObjectId());
                }
            }

            Map<Long, MeetingBO> meetingBOMaps = meetingApi.getMeetingsMap(meetingIds);
            for (CtpAffair affair : affairs) {
                if (affair.getApp().equals(ApplicationCategoryEnum.meeting.getKey())) {
                    Set<Long> set = map_replyCount.keySet();
                    if (set.contains(affair.getObjectId()) || DateUtil.currentDate().after(affair.getCompleteTime())) {
                        continue;
                    }
                    MeetingBO meeting = meetingBOMaps.get(affair.getObjectId());
                    if (meeting != null) {
                        map_reply = new HashMap<String, Integer>();
                        map_reply.put("allCount", meeting.getAllCount());
                        map_reply.put("joinCount", meeting.getJoinCount());
                        map_reply.put("unjoinCount", meeting.getUnjoinCount());
                        map_reply.put("pendingCount", meeting.getPendingCount());
                        map_replyCount.put(affair.getObjectId(), map_reply);

                        mapMeeting.put(meeting.getId(), meeting.getMeetPlace());
                    }
                }
            }

        }

        List<PendingRow> rowList = new ArrayList<PendingRow>();
        for (CtpAffair affair : affairs) {
            //已经过了会议时间的会议不显示
            if (state == StateEnum.col_pending.key() && affair.getApp().equals(ApplicationCategoryEnum.meeting.getKey()) && DateUtil.currentDate().after(affair.getCompleteTime())) {
                //清理meeting和affair状态
                if (meetingApi != null) {
                    meetingApi.repairMeeting(affair.getObjectId());
                }
                continue;
            }
            PendingRow row = new PendingRow();
            String currentNodesInfo = "";
            boolean isHasAttachments = AffairUtil.isHasAttachments(affair);
            Permission permisson = permissonMap.get(affair.getNodePolicy());
            if (permisson != null) {
                row.setDisAgreeOpinionPolicy(permisson.getNodePolicy().getDisAgreeOpinionPolicy());
            }
            //催办次数
            row.setHastenTimes(affair.getHastenTimes());
            //置顶时间
            row.setTopTime(affair.getTopTime());
            /**
             * 以下情况显示催办按钮
             * 1、协同、公文的：已发数据（在已发栏目、跟踪栏目的已发数据）
             * 2、协同的督办人，查看已办数据。\
             * 3、全部改为前段处理的方式，已发栏目直接true,跟踪栏目判断状态，已办栏目ajax判断是否是督办人
             */
            if (affair.getState() == StateEnum.col_sent.getKey()) {
                row.setSpervisor(true);
            }
            if (affair.getApp() == ApplicationCategoryEnum.collaboration.key()) {
                if (AppContext.hasPlugin("collaboration")) {
                    ColSummary summary = colSummaryMap.get(affair.getObjectId());
                    if (summary != null) {
                        if (isCurrentNodesInfo) {
                            currentNodesInfo = WFComponentUtil.parseCurrentNodesInfo(summary.getFinishDate(), summary.getCurrentNodesInfo(), summary.getState());
                        }
                        if (isProcessingProgress) {
                            if (summary.getReplyCounts() != null) {
                                row.setReplyCounts(summary.getReplyCounts());
                            }
                        }
                    }

                    row.setActivityId(affair.getActivityId());
                    row.setCaseId(affair.getCaseId());
                    row.setProcessId(affair.getProcessId());
                    row.setTemplateId(affair.getTempleteId() + "");
                    row.setPreApproverName(null == affair.getPreApprover() ? "   " : Functions.showMemberName(affair.getPreApprover()));
                }
            } else if (affair.getApp() == ApplicationCategoryEnum.edoc.key()) {//G6新老公
                if (AppContext.hasPlugin(ApplicationCategoryEnum.edoc.name())) {
                    SimpleEdocSummary summary = edocMap.get(affair.getObjectId());
                    if (summary != null) {
                        if (isCurrentNodesInfo) {
                            currentNodesInfo = commonAffairSectionUtils.parseCurrentNodesInfo(summary.getCompleteTime(),
                                    summary.getCurrentNodesInfo(), Collections.<Long, String>emptyMap());
                        }
                    }
                    row.setActivityId(affair.getActivityId());
                    row.setCaseId(affair.getCaseId());
                    row.setProcessId(affair.getProcessId());
                    row.setTemplateId(affair.getTempleteId() == null ? "" : String.valueOf(affair.getTempleteId()));

                    isHasAttachments = AffairUtil.isHasAttachments(affair);
                }
                row.setPreApproverName(null == affair.getPreApprover() ? Functions.showMemberName(affair.getSenderId()) : Functions.showMemberName(affair.getPreApprover()));
            }

            if (Strings.isNotBlank(currentNodesInfo)) {
                row.setCurrentNodesInfo(currentNodesInfo);
            }

            Map<String, Object> extMap = null;
            String forwardMember = affair.getForwardMember();
            Integer resentTime = affair.getResentTime();
            String subject = WFComponentUtil.showSubjectOfAffair(affair, isProxy, -1).replaceAll("\r\n", "").replaceAll("\n", "");
            Integer subApp = affair.getSubApp();
            Long objectId = affair.getObjectId();
            Long templeteId = affair.getTempleteId();
            if (null != templeteId) {
                row.setTemplateId(templeteId.toString());
            }
            row.setSubject(WFComponentUtil.showSubjectOfSummary4Done(affair, -1));
            if (Integer.valueOf(StateEnum.col_pending.getKey()).equals(affair.getState())) {
                row.setSubject(subject);
            }
            row.setSubState(affair.getSubState());
            row.setState(affair.getState());
            Long deadLineDate = affair.getDeadlineDate();
            if (deadLineDate == null) {
                deadLineDate = 0L;
            }
            row.setDeadLineDate(deadLineDate);

            String memberName = Functions.showMemberName(affair.getSenderId());

            if (memberName == null && (affair.getSenderId() == null || affair.getSenderId() == -1)) {
                memberName = Strings.escapeNULL(affair.getExtProps(), "");
            }

            row.setMemberId(affair.getMemberId());
            row.setCreateMemberName(memberName);
            row.setCreateMemberAlt(memberName);
            row.setCreateMemberId(affair.getSenderId());
            row.setCreateDate(affair.getCreateDate());
            if (null != affair.getReceiveTime()) {//修复OA-15330 空指针异常，做防护
                // 更多页面页显示全部时间
                row.setReceiveTimeAll(Datetimes.format(affair.getReceiveTime(), "yyyy-MM-dd HH:mm"));
                // 首页显示今日、明日...
                String receiveTimeStr = WFComponentUtil.getDateTime(affair.getReceiveTime(), "yyyy-MM-dd HH:mm");
                row.setReceiveTime(receiveTimeStr);
                row.setCompleteTime(receiveTimeStr);
            }
            //加签、知会、当前会签  ps:回退优先，如果这条记录也被回退过，则优先显示回退图标，加签图标不显示
            if (affair.getFromId() != null && affair.getBackFromId() == null && !String.valueOf(affair.getApp()).equals(String.valueOf(ApplicationCategoryEnum.templateApprove.key()))) {
                row.setFromName(ResourceUtil.getString("collaboration.pending.addOrJointly.label", Functions.showMemberName(affair.getFromId())));
            }
            //回退、指定回退,OA-86015
            if (affair.getBackFromId() != null) {
                row.setBackFromName(ResourceUtil.getString("collaboration.pending.stepBack.label", Functions.showMemberName(affair.getBackFromId())));
            }

            row.setId(affair.getId());
            row.setObjectId(objectId);
            row.setBodyType(affair.getBodyType());
            row.setImportantLevel(affair.getImportantLevel());
            row.setHasAttachments(isHasAttachments);
            // 首页  显示处理期限   今日，明日，否则显示正常日期
            if (affair.getExpectedProcessTime() != null) {
                row.setDealLineName(WFComponentUtil.getDateTime(affair.getExpectedProcessTime(), "yyyy-MM-dd HH:mm"));
                // 更多页面
                row.setDeadLine(WFComponentUtil.getDeadLineName(affair.getExpectedProcessTime()));
            } else {
                // 兼容老数据
                // 分别给首页和更多页面传值
                String deadLineName = WFComponentUtil.getDeadLineName(affair.getDeadlineDate());
                row.setDealLineName(deadLineName);
                row.setDeadLine(deadLineName);
            }
            //处理时间
            if (affair.getCompleteTime() != null) {
                row.setCompleteTime(Datetimes.format(affair.getCompleteTime(), "yyyy-MM-dd HH:mm"));
            }
            //设置流程超期状态0未超期、1即将超期、2已超期
            Boolean isOverTime = false;
            if ((affair.getDeadlineDate() != null && affair.getDeadlineDate() > 0) || affair.getExpectedProcessTime() != null) {
                isOverTime = affair.isCoverTime() == null ? false : affair.isCoverTime();
                //超期事件突出显示
                row.setDistinct(isOverTime);
                row.setOverTime(isOverTime);
            }
            if (affair.getExpectedProcessTime() != null || (affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0)) {
                row.setShowClockIcon(true);
            }

            // 处理是否超期  （处理期限是否大于当前期限）
            Date now = new Date(System.currentTimeMillis());
            Boolean isCoverTime = affair.isCoverTime();
            Date _expectedProcessTime = affair.getExpectedProcessTime();
            if (_expectedProcessTime == null && affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0) {
                _expectedProcessTime = workTimeManager.getCompleteDate4Nature(affair.getReceiveTime(), affair.getDeadlineDate(), AppContext.currentAccountId());
            }
            boolean isExpectedOvertime = _expectedProcessTime != null && now.after(_expectedProcessTime);

            if ((isCoverTime != null && isCoverTime) || isExpectedOvertime) {
                row.setDealTimeout(true);
            }

            //设置了节点期限的才需要计算. 计算超长时长
            if (_expectedProcessTime != null) {
                try {
                    //超期时间
                    long time = workTimeManager.getDealWithTimeValue(new Date(), _expectedProcessTime, affair.getOrgAccountId());
                    time = time / (60 * 1000); //毫秒转化为分钟

                    String deadTimeString = showDate(Integer.parseInt(Math.abs(time) + ""), true);
                    if (time < 0) {
                        row.setDeadlineTime(deadTimeString);
                    } else {
                        row.setNodeadlineTime(deadTimeString);
                    }
                    //计算提前提醒
                    Long remindTime = affair.getRemindDate();
                    if (remindTime != null && !Long.valueOf(-1).equals(remindTime) && affair.getExpectedProcessTime() != null) {
                        Date advanceRemindTime = workTimeManager.getRemindDate(_expectedProcessTime, remindTime);
                        if (now.after(advanceRemindTime)) {
                            row.setShowDealLineTime(true);
                        }
                    }

                } catch (Exception e) {
                    log.error("", e);
                }
            }

            //设置“文号”和“发文单位”
            String sendUnitName = null;
            if (affair.getApp() == ApplicationCategoryEnum.edoc.key() && (rowStr.indexOf("edocMark") != -1 || rowStr.indexOf("sendUnit") != -1)) {
                if (extMap == null) {
                    extMap = Strings.escapeNULL(AffairUtil.getExtProperty(affair), new HashMap<String, Object>());
                }
                String edocMark = (String) extMap.get(AffairExtPropEnums.edoc_edocMark.name());
                if ("null".equals(edocMark) || edocMark == null) {
                    edocMark = "";
                }
                sendUnitName = (String) extMap.get(AffairExtPropEnums.edoc_sendUnit.name());
                row.setEdocMark(edocMark);
                row.setSendUnit(sendUnitName == null ? "" : sendUnitName);
            }

            if (affair.getApp().equals(ApplicationCategoryEnum.meeting.getKey()) && AppContext.hasPlugin("meeting")) {
                boolean showMeetingDetail = rowStr.indexOf("placeOfMeeting") != -1 || rowStr.indexOf("theConferenceHost") != -1 || rowStr.indexOf("processingProgress") != -1;
                if ("inform".equals(affair.getNodePolicy())) {
                    row.setMeetingImpart(ResourceUtil.getString("collaboration.pending.meetingImpart.lable"));
                }
                Long meetingEmccId = null;
                if (extMap == null) {
                    extMap = Strings.escapeNULL(AffairUtil.getExtProperty(affair), new HashMap<String, Object>());
                }
                //获取会议参会人数，全部人数，不参加人数，待定人数

                Map<String, Integer> countMap = map_replyCount.get(affair.getObjectId());
                if (countMap != null) {
                    row.setProcessedNumber(countMap.get("joinCount"));
                    row.setUnJoinNumber(countMap.get("unjoinCount"));
                    row.setPendingNumber(countMap.get("pendingCount"));
                    row.setTotalNumber(countMap.get("allCount"));
                    row.setProcessingProgress(row.getProcessedNumber() + "|" + row.getTotalNumber());
                }

                if (showMeetingDetail) {
                    meetingEmccId = objToLong(extMap.get(AffairExtPropEnums.meeting_emcee_id.name()));//主持人ID
                    V3xOrgMember emccMember = orgManager.getMemberById(meetingEmccId);
                    row.setTheConferenceHost(Functions.showMemberName(emccMember));
                    row.setTheConferenceHostId(meetingEmccId);
                }
                if (Strings.isNotBlank(mapMeeting.get(affair.getObjectId()))) {
                    row.setPlaceOfMeeting(mapMeeting.get(affair.getObjectId()));
                }
                String meetingNature = (String) extMap.get(AffairExtPropEnums.meeting_videoConf.name());
                row.setMeetingNature(meetingNature);
            }
            if (isOverTime) {
                row.addExtIcons("/common/images/timeout.gif");
            } else if (affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0) {
                row.addExtIcons("/common/images/overTime.gif");
            }
            //会议告知人待办会显示为知会
            if (affair.getApp() != ApplicationCategoryEnum.meeting.getKey()) {
                row.setPolicyName(getPolicyName(affair));
            }
            //branches_a8_v350_r_gov GOV-3865 唐桂林  首页-个人空间-自定义待办栏目，设置显示节点权限列，权限为"协同"的权限显示为国际资源化key值 start
            if (isGov) {
                String str = row.getPolicyName();
                if (Strings.isNotBlank(str)) {
                    if (str.length() > 8) {
                        str = str.substring(0, 8);
                        row.setPolicyName(str + "...");
                    }
                }
            }
            int app = affair.getApp();
            String url = "";
            boolean flagTemp = false;
            ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(app);
            switch (appEnum) {
                case collaboration:
                    row.setLink("/collaboration/collaboration.do?method=summary&openFrom=listPending&affairId=" + affair.getId());
                    //待办中显示回退给发起人的数据，穿透为编辑页面
                    row.setOpenType(OPEN_TYPE.multiWindow.ordinal());
                    if (affair.getState() == StateEnum.col_waitSend.getKey()
                            && (affair.getSubState() == SubStateEnum.col_waitSend_stepBack.key()
                            || affair.getSubState() == SubStateEnum.col_pending_specialBackToSenderCancel.key()
                            || affair.getSubState() == SubStateEnum.col_pending_specialBacked.key()
                            || affair.getSubState() == SubStateEnum.col_waitSend_draft.key())) {
                        row.setLink("/collaboration/collaboration.do?method=newColl&openFrom=listPending&from=waitSend&affairId="
                                + affair.getId() + "&summaryId=" + affair.getObjectId());
                        url = "/collaboration/collaboration.do?method=listWaitSend&openFrom=listPending";
                        row.setOpenType(OPEN_TYPE.openWorkSpace.ordinal());
                    } else {
                        row.setLink("/collaboration/collaboration.do?method=summary&openFrom=listPending&affairId="
                                + affair.getId());
                        url = "/collaboration/collaboration.do?method=listPending&openFrom=listPending";
                        row.setOpenType(OPEN_TYPE.openWorkSpace.ordinal());

                    }
                    row.setCategory(app, url);
                    row.setCategory(ResourceUtil.getString("pending.collaboration.label"), url);
                    break;
                case meetingroom:
                    if (null != affair.getSubApp() && affair.getSubApp().equals(ApplicationSubCategoryEnum.meetingRoomAudit.getKey())) {

                        row.setLink("/meetingroom.do?method=createPerm&openWin=1&id=" + objectId + "&affairId=" + affair.getId());
                        url = "/meetingroom.do?method=index";
                        row.setCategory(app, url);
                        row.setReceiveTime(WFComponentUtil.getDateTime(affair.getReceiveTime(), "yyyy-MM-dd HH:mm"));
                        row.setCategory(ResourceUtil.getString("pending.meetingroom.label"), url);
                    }
                    if (null != affair.getSubApp()) row.setApplicationSubCategoryKey(affair.getSubApp());
                    break;
                case meeting:
                    row.setLink("/mtMeeting.do?method=mydetail&id=" + objectId + "&affairId=" + affair.getId() + "&state=10");
                    url = "/meetingNavigation.do?method=entryManager&entry=meetingPending";


                    if (affair.getReceiveTime() != null && affair.getCompleteTime() != null) {
                        row.setCompleteTime(convertMeetingTime(affair.getReceiveTime(), affair.getCompleteTime()));
                    }
                    if (null != affair.getReceiveTime()) {
                        row.setReceiveTime(WFComponentUtil.getDateTime(affair.getReceiveTime(), "yyyy-MM-dd HH:mm"));
                    }
                    row.setCategory(app, subApp, url);
                    row.setCategory(ResourceUtil.getString("pending.meeting.label"), url);
                    break;
                case templateApprove:
                    row.setSubject("(" + ResourceUtil.getString("workflow.approve.release") + ")" + affair.getSubject());
                    row.setLink("/templateApproveController.do?method=dealWithApproveWorkFlowInfo&state=" + affair.getState() + "&objectId=" + affair.getObjectId() + "&templateId=" + affair.getTempleteId() + "&affairId=" + affair.getId() + "&formId=" + affair.getFromId(), OPEN_TYPE.openWorkSpace.ordinal());
                    row.setApplicationCategoryKey(app);
                    row.setCategory(ResourceUtil.getString("pending.template.approve"), null);
                    break;
                case edoc://G6公文
                    if (subApp == null) {
                        break;
                    }
                    ApplicationSubCategoryEnum subAppEnum = ApplicationSubCategoryEnum.valueOf(app, subApp);
                    switch (subAppEnum) {
                        case edoc_fawen://G6新公文发文
                            row.setLink("/govdoc/govdoc.do?method=summary&isFromHome=true&openFrom=listPending&affairId=" + affair.getId() + "&app=" + affair.getApp() + "&summaryId=" + affair.getObjectId());
                            //分类链接：判断是否有公文管理-待办列表资源，若有则链接到发文管理-待办列表，若没有则链接到公文管理-待办列表
                            boolean f11 = user.hasResourceCode("F20_govDocSendManage");
                            url = "/govdoc/govdoc.do?method=index&govdocType=1&_resourceCode=F20_govDocSendManage";
                            if (!f11) {
                                f11 = user.hasResourceCode("F20_govdocPending");
                                url = "/govdoc/govdoc.do?method=index&listType=listPendingAllRoot&_resourceCode=F20_govdocPending";
                            }
                            row.setHasResPerm(f11);
                            row.setCategory(app, subApp, url);
                            row.setCategory(ResourceUtil.getString("govdoc.edocSend.label"), url);
                            break;
                        case edoc_qianbao://G6新公文签报
                            row.setLink("/govdoc/govdoc.do?method=summary&isFromHome=true&openFrom=listPending&affairId=" + affair.getId() + "&app=" + affair.getApp() + "&summaryId=" + affair.getObjectId());
                            //分类链接：判断是否有公文管理-待办列表资源，若有则链接到签报管理-待办列表，若没有则链接到公文管理-待办列表
                            f11 = user.hasResourceCode("F20_signReport");
                            url = "/govdoc/govdoc.do?method=index&govdocType=3&_resourceCode=F20_signReport";
                            if (!f11) {
                                f11 = user.hasResourceCode("F20_govdocPending");
                                url = "/govdoc/govdoc.do?method=index&listType=listPendingAllRoot&_resourceCode=F20_govdocPending";
                            }
                            row.setHasResPerm(f11);
                            row.setCategory(app, subApp, url);
                            row.setCategory(ResourceUtil.getString("govdoc.edocSign.label"), url);
                            break;
                        case edoc_shouwen://G6新公文收文
                            row.setLink("/govdoc/govdoc.do?method=summary&openFrom=listPending&isFromHome=true&affairId=" + affair.getId() + "&app=" + affair.getApp() + "&summaryId=" + affair.getObjectId());
                            //分类链接：判断是否有公文管理-待办列表资源，若有则链接到收文管理-待办列表，若没有则链接到公文管理-待办列表
                            f11 = user.hasResourceCode("F20_receiveManage");
                            url = "/govdoc/govdoc.do?method=index&govdocType=2,4&_resourceCode=F20_receiveManage";
                            if (!f11) {
                                f11 = user.hasResourceCode("F20_govdocPending");
                                url = "/govdoc/govdoc.do?method=index&listType=listPendingAllRoot&_resourceCode=F20_govdocPending";
                            }
                            row.setHasResPerm(f11);
                            row.setCategory(app, subApp, url);
                            row.setCategory(ResourceUtil.getString("govdoc.edocRec.label"), url);
                            break;
                        case edoc_jiaohuan://G6新公文交换
                            row.setLink("/govdoc/govdoc.do?method=summary&isFromHome=true&openFrom=listPending&affairId=" + affair.getId() + "&app=" + affair.getApp() + "&summaryId=" + affair.getObjectId());
                            //分类链接：判断是否有公文管理-待办列表资源，若有则链接到签报管理-待办列表，若没有则链接到公文管理-待办列表
                            f11 = user.hasResourceCode("F20_receiveManage");
                            url = "/govdoc/govdoc.do?method=index&govdocType=2,4&_resourceCode=F20_receiveManage";
                            //链接优先级：公文管理-收文管理、公文管理-公文交换、公文管理-待办列表
                            if (!f11) {
                                f11 = user.hasResourceCode("F20_govdocExchange");
                                url = "/govdoc/exchange.do?method=exchangeIndex&_resourceCode=F20_govdocExchange";
                            }
                            if (!f11) {
                                f11 = user.hasResourceCode("F20_govdocPending");
                                url = "/govdoc/govdoc.do?method=index&listType=listPendingAllRoot&_resourceCode=F20_govdocPending";
                            }
                            row.setHasResPerm(f11);
                            row.setCategory(app, subApp, url);
                            row.setCategory(ResourceUtil.getString("govdoc.edocRec.label"), url);
                            break;
                        case old_edocSend://G6老公文发文
                            row.setLink("/edocController.do?method=detailIFrame&from=Pending&affairId=" + affair.getId());
                            //分类链接：判断是否有公文管理-待办列表资源，若有则链接到签报管理-待办列表，若没有则链接到公文管理-待办列表
                            f11 = user.hasResourceCode("F20_govDocSendManage");
                            url = "/govdoc/govdoc.do?method=index&govdocType=1&_resourceCode=F20_govDocSendManage";
                            if (!f11) {
                                f11 = user.hasResourceCode("F20_govdocPending");
                                url = "/govdoc/govdoc.do?method=index&listType=listPendingAllRoot&_resourceCode=F20_govdocPending";
                            }
                            row.setHasResPerm(f11);
                            row.setCategory(app, subApp, url);
                            row.setCategory(ResourceUtil.getString("govdoc.edocSend.label"), url);
                            break;
                        case old_edocRec://G6老公文收文
                            row.setLink("/edocController.do?method=detailIFrame&from=Pending&affairId=" + affair.getId());
                            //分类链接：判断是否有公文管理-待办列表资源，若有则链接到收文管理-待办列表，若没有则链接到公文管理-待办列表
                            f11 = user.hasResourceCode("F20_receiveManage");
                            url = "/govdoc/govdoc.do?method=index&govdocType=2,4&_resourceCode=F20_receiveManage";
                            if (!f11) {
                                f11 = user.hasResourceCode("F20_govdocPending");
                                url = "/govdoc/govdoc.do?method=index&listType=listPendingAllRoot&_resourceCode=F20_govdocPending";
                            }
                            row.setHasResPerm(f11);
                            row.setCategory(app, subApp, url);
                            row.setCategory(ResourceUtil.getString("govdoc.edocRec.label"), url);
                            break;
                        case old_edocSign://G6老公文签报
                            row.setLink("/edocController.do?method=detailIFrame&from=Pending&affairId=" + affair.getId());
                            //分类链接：判断是否有公文管理-待办列表资源，若有则链接到签报管理-待办列表，若没有则链接到公文管理-待办列表
                            f11 = user.hasResourceCode("F20_signReport");
                            url = "/govdoc/govdoc.do?method=index&govdocType=3&_resourceCode=F20_signReport";
                            if (!f11) {
                                f11 = user.hasResourceCode("F20_govdocPending");
                                url = "/govdoc/govdoc.do?method=index&listType=listPendingAllRoot&_resourceCode=F20_govdocPending";
                            }
                            row.setHasResPerm(f11);
                            row.setCategory(app, subApp, url);
                            row.setCategory(ResourceUtil.getString("govdoc.edocSign.label"), url);
                            break;
                        case old_exSend://G6老公文交换(老公文交换只展示待办数据)
                            String category = "govdoc.pending.exSend.label";
                            if (affair.getState() != 3) {
                                row.setLink("/exchangeEdoc.do?method=edit&modelType=sent&id=" + affair.getSubObjectId() + "&affairId=" + affair.getId());
                                category = "govdoc.done.exSend.label";
                            } else {
                                row.setLink("/exchangeEdoc.do?method=sendDetail&modelType=toSend&id=" + affair.getSubObjectId() + "&affairId=" + affair.getId());
                            }
                            row.setCategory(ResourceUtil.getString(category), "");//待发送/已发送
                            //G6不支持老公文链接到交换分类
                            url = "";
                            row.setHasResPerm(false);
                            row.setCategory(app, subApp, url);
                            row.setSubObjectId(affair.getSubObjectId());
                            break;
                        case old_exSign://G6老公文签收(老公文交换只展示待办数据)
                            if (affair.getState() != 3) {
                                row.setLink("/exchangeEdoc.do?method=edit&id=" + affair.getSubObjectId() + "&affairId=" + affair.getId() + "&modelType=received");
                                category = "govdoc.done.exSign.label";
                            } else {
                                row.setLink("/exchangeEdoc.do?method=receiveDetail&id=" + affair.getSubObjectId() + "&affairId=" + affair.getId() + "&modelType=toReceive");
                                category = "govdoc.pending.exSign.label";
                            }
                            if (extMap == null) {
                                extMap = Strings.escapeNULL(AffairUtil.getExtProperty(affair), new HashMap<String, Object>());
                            }
                            Object edocRecieveRetreat = extMap.get(AffairExtPropEnums.edoc_edocRecieveRetreat.name());
                            if (affair.getApp() == ApplicationCategoryEnum.exSign.key() && edocRecieveRetreat != null) {
                                row.setSubject(subject + "(" + ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", "edoc.gov.retreat.label") + ")");
                            }
                            row.setCategory(ResourceUtil.getString(category), "");//待签收/已签收
                            //G6不支持老公文链接到交换分类
                            url = "";
                            row.setHasResPerm(false);
                            row.setCategory(app, subApp, url);
                            row.setSubObjectId(affair.getSubObjectId());
                            break;
                        case old_edocRegister://G6老公文登记
                            if (affair.getState() != 3) {//已登记
                                String link = "/edocController.do?method=edocRegisterDetail&forwardType=registered&registerId=" + affair.getObjectId();
                                if (affair.getSubObjectId() != null) {
                                    link += "&recieveId=" + affair.getSubObjectId();
                                }
                                row.setLink(link, OPEN_TYPE.href.ordinal());
                                category = "done.edocRegister.label";
                            } else {
                                category = "pending.edocRegister.label";//待登记
                            }

                            if (affair.getState() == 3) {//栏目待办
                                if (isEdocRegister) {
                                    if (extMap == null) {
                                        extMap = Strings.escapeNULL(AffairUtil.getExtProperty(affair), new HashMap<String, Object>());
                                    }
                                    Object edocRegisterRetreat = extMap.get(AffairExtPropEnums.edoc_edocRegisterRetreat.name());
                                    if (edocRegisterRetreat != null) {
                                        row.setSubject(subject + "(" + ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", "edoc.gov.retreat.label") + ")");
                                    }
                                    String link = "/edocController.do?method=entryManager&entry=recManager&toFrom=newEdocRegister&edocType=" + EdocEnum.edocType.recEdoc.ordinal() +
                                            "&edocId=" + affair.getObjectId() + "&affairId=" + affair.getId() + "&registerType=1&comm=create" +
                                            "&recListType=registerPending";
                                    if (affair.getSubObjectId() != null) {
                                        link += "exchangeId=" + affair.getSubObjectId();
                                    }
                                    //被回退的待登记，再登记时需要进行编辑修改操作，所以传comm=edit
                                    row.setLink(link, OPEN_TYPE.href.ordinal());
                                }
                            }
                            //G6不支持老公文链接到交换分类
                            url = "";
                            row.setHasResPerm(false);
                            row.setCategory(app, subApp, url);
                            row.setCategory(ResourceUtil.getString(category), url);
                            row.setSubObjectId(affair.getSubObjectId());
                            break;
                        case old_edocRecDistribute://G6老公文分发
                            if (affair.getState() == 3) {
                                row.setLink("/edocController.do?method=entryManager&isFromHome=true&entry=recManager&toFrom=newEdoc&listType=newEdoc&recListType=listDistribute&app=34&id=" + affair.getObjectId() + "&affairId=" + affair.getId(), OPEN_TYPE.href.ordinal());
                                category = "edoc.receive.toAttribute";
                            } else {
                                row.setLink("/edocController.do?method=detailIFrame&from=Sent&&affairId=" + affair.getId());
                                category = "done.edocDistribute.label";
                            }

                            //G6不支持老公文链接到交换分类
                            url = "";

                            row.setHasResPerm(false);
                            row.setCategory(app, subApp, url);
                            row.setCategory(ResourceUtil.getString(category), url);
                            break;
                    }
                    row.setCategoryOpenType(OPEN_TYPE.href.ordinal());
                    break;
                case info://信息报送
                    if (SystemEnvironment.hasPlugin("infosend")) {
                        subject = WFComponentUtil.mergeSubjectWithForwardMembers(affair.getSubject(), forwardMember, resentTime, null, -1);
                        row.setSubject(subject);
                        row.setCreateDate(affair.getReceiveTime());
                        row.setApplicationCategoryKey(affair.getApp());
                        row.setApplicationSubCategoryKey(affair.getSubApp());
                        if (affair.getSubApp() == null || affair.getSubApp() == ApplicationSubCategoryEnum.info_self.key()
                                || affair.getSubApp() == ApplicationSubCategoryEnum.info_tempate.key()) {
                            row.setLink("/info/infoDetail.do?method=summary&id=" + affair.getObjectId() + "&openFrom=Pending&affairId=" + affair.getId() + "");
                            if (user.hasResourceCode("F18_infoAudit")) {
                                url = "/info/infoMain.do?method=infoAudit&listType=listInfoPending";
                            }
                        } else if (affair.getSubApp() == ApplicationSubCategoryEnum.info_magazine.key()) {
                            row.setLink("/info/magazine.do?method=summary&magazineId=" + affair.getObjectId() + "&openFrom=Pending&affairId=" + affair.getId() + "");
                            if (user.hasResourceCode("F18_magazineAudit")) {
                                url = "/info/infoMain.do?method=magazineAudit&listType=listMagazineAuditPending";
                            }
                        } else if (affair.getSubApp() == ApplicationSubCategoryEnum.info_magazine_publish.key()) {
                            row.setLink("/info/magazine.do?method=openMagazinePublishDialog&openFromType=2&fromPortal=1&affairId=" + affair.getId());
                            if (user.hasResourceCode("F18_magazinePublish")) {
                                url = "/info/infoMain.do?method=magazineAudit&listType=listMagazinePublishPending";
                            }
                        }
                        row.setCategory(ResourceUtil.getString("menu.info.report"), url);
                        break;
                    }
                case bulletin:
                    String[] bulLinks = getPendingCategoryLink(affair);
                    row.setLink(bulLinks[0], OPEN_TYPE.href_blank.ordinal());
                    row.setApplicationCategoryKey(app);
                    if ("agent".equals(currentPanel) && !user.getId().equals(affair.getMemberId())) { // 代理人查看公告审核事项，不显示后面的应用链接
                        row.setCategory(ResourceUtil.getString("collaboration.pending.bulletin.label"), null);
                    } else {
                        row.setCategory(ResourceUtil.getString("collaboration.pending.bulletin.label"), bulLinks[1], OPEN_TYPE.href_blank.ordinal());
                    }
                    break;
                case news:
                    String[] newsLinks = getPendingCategoryLink(affair);
                    row.setLink(newsLinks[0], OPEN_TYPE.href_blank.ordinal());
                    row.setApplicationCategoryKey(app);
                    if ("agent".equals(currentPanel) && !user.getId().equals(affair.getMemberId())) { // 代理人查看新闻审核事项，不显示后面的应用链接
                        row.setCategory(ResourceUtil.getString("collaboration.pending.news.label"), null);
                    } else {
                        row.setCategory(ResourceUtil.getString("collaboration.pending.news.label"), newsLinks[1], OPEN_TYPE.href_blank.ordinal());
                    }
                    break;
                case inquiry:
                    String[] inquiryLinks = getPendingCategoryLink(affair);
                    row.setLink(inquiryLinks[0], OPEN_TYPE.href_blank.ordinal());
                    row.setApplicationCategoryKey(app);
                    row.setApplicationSubCategoryKey(affair.getSubApp());
                    if ("agent".equals(currentPanel) && !user.getId().equals(affair.getMemberId())) { // 代理人查看新闻审核事项，不显示后面的应用链接
                        row.setCategory(ResourceUtil.getString("collaboration.pending.inquiry.label"), null);
                    } else {
                        row.setCategory(ResourceUtil.getString("collaboration.pending.inquiry.label"), inquiryLinks[1], OPEN_TYPE.href_blank.ordinal());
                    }
                    break;
                case office: // 综合办公审批
                    try {
                        row.setApplicationCategoryKey(app);
                        row.setApplicationSubCategoryKey(affair.getSubApp());
                        if (ApplicationSubCategoryEnum.office_auto.key() == subApp.intValue()) { // 车辆
                            row.setLink("/office/autoUse.do?method=autoAuditEdit&affairId=" + affair.getId() + "&v=" + SecurityHelper.func_digest(affair.getId()));
                            row.setCategory(ResourceUtil.getString("office.app.auto.js"), "/office/autoUse.do?method=index&tgt=autoAudit");
                        } else if (ApplicationSubCategoryEnum.office_stock.key() == subApp.intValue()) { // 办公用品
                            row.setLink("/office/stockUse.do?method=stockAuditEdit&affairId=" + affair.getId() + "&v=" + SecurityHelper.func_digest(affair.getId()));
                            row.setCategory(ResourceUtil.getString("office.app.stock.js"), "/office/stockUse.do?method=index&tgt=stockAudit");
                        } else if (ApplicationSubCategoryEnum.office_asset.key() == subApp.intValue()) { // 办公设备
                            row.setLink("/office/assetUse.do?method=assetAuditEdit&operate=audit&affairId=" + affair.getId() + "&v=" + SecurityHelper.func_digest(affair.getId()));
                            row.setCategory(ResourceUtil.getString("office.app.asset.js"), "/office/assetUse.do?method=index&tgt=assetAudit");
                        } else if (ApplicationSubCategoryEnum.office_book.key() == subApp.intValue()) { // 图书资料
                            row.setLink("/office/bookUse.do?method=bookAuditDetail&bookApplyId=" + objectId + "&v=" + SecurityHelper.func_digest(objectId));
                            row.setCategory(ResourceUtil.getString("office.app.book.js"), "/office/bookUse.do?method=index&tgt=bookAudit");
                        }
                        break;
                    } catch (Exception e) {
                        log.info(e.getLocalizedMessage());
                    }
            }
            row.setSummaryState(affair.getSummaryState());
            row.setState(affair.getState());
            row.setHasResPerm(this.hasResPerm(affair, user, row.isHasResPerm()));
            //当前用户不是公文收发员设置为false
            if (flagTemp) {
                row.setHasResPerm(false);
            }

            Map<String, String> attitudeMap = new HashMap<String, String>();
            //wxj  修复jira bug OA-109617上一处理人：待办栏目更多列表'会议、新闻、公告、调查'数据的上一处理人显示为"undefined"
            if (null == row.getPreApproverName()) {
                row.setPreApproverName(" ");
            }

            if (rowStr.indexOf("processingProgress") != -1) {
                Map<String, String> extParam = this.getPendingDetailContent(row);
                if (isM3todo) {
                    if (affair.getApp().intValue() == ApplicationCategoryEnum.collaboration.getKey()) {
                        String configCategory = EnumNameEnum.col_flow_perm_policy.name();
                        String configItem = affair.getNodePolicy();
                        ColSummary colSummary = colSummaryMap.get(affair.getObjectId());
                        if (null != colSummary) {
                            Long accountId = colSummary.getPermissionAccountId();
                            Permission permission = permissionManager.getPermission(configCategory, configItem, accountId);
                            if (null != permission) {
                                NodePolicy nodePolicy = permission.getNodePolicy();
                                if (null != nodePolicy) {
                                    String attitude = nodePolicy.getAttitude();
                                    DetailAttitude detailAttitude = nodePolicy.getDatailAttitude();

                                    Map attMap = new LinkedHashMap();//M3快速处理时,使用;key:处理时态度保存值;value:处理时展现值
                                    if (Strings.isNotBlank(attitude) && detailAttitude != null) {
                                        List<String> chooseAttitude = Arrays.asList(attitude.split(","));
                                        String[] attitudeArr = {"haveRead", "agree", "disagree"};
                                        for (String att : attitudeArr) {
                                            if (!chooseAttitude.contains(att)) {
                                                continue;
                                            }
                                            String key = "";
                                            Map<String, String> returnAtt = new HashMap<String, String>();

                                            if ("haveRead".equals(att)) {
                                                key = CommentExtAtt1Enum.haveRead.i18nLabel();
                                                returnAtt.put("value", detailAttitude.getHaveRead());
                                                returnAtt.put("display", ResourceUtil.getString(detailAttitude.getHaveRead()));
                                            } else if ("agree".equals(att)) {
                                                key = CommentExtAtt1Enum.agree.i18nLabel();
                                                returnAtt.put("value", detailAttitude.getAgree());
                                                returnAtt.put("display", ResourceUtil.getString(detailAttitude.getAgree()));
                                            } else if ("disagree".equals(att)) {
                                                key = CommentExtAtt1Enum.disagree.i18nLabel();
                                                returnAtt.put("value", detailAttitude.getDisagree());
                                                returnAtt.put("display", ResourceUtil.getString(detailAttitude.getDisagree()));
                                            }

                                            returnAtt.put("key", key);
                                            attMap.put(key, returnAtt);
                                        }
                                    }
                                    extParam.put("attitude", JSONUtil.toJSONString(attMap));
                                    Integer opinion = nodePolicy.getOpinionPolicy();
                                    boolean canDeleteORarchive = (opinion != null && opinion.intValue() == 1);
                                    extParam.put("canDeleteORarchive", String.valueOf(canDeleteORarchive));
                                    String baseAction = nodePolicy.getBaseAction();
                                    boolean canReMove = true;
                                    if (baseAction != null) {
                                        canReMove = baseAction.indexOf("ReMove") != -1;
                                    }
                                    extParam.put("canReMove", String.valueOf(canReMove));
                                    extParam.put("workitemId", null != affair.getSubObjectId() ? affair.getSubObjectId().toString() : "");
                                    //如果他存在非强制校验信息那么弹出提示 --不能快速处理，请您手动处理该协同！
                                    //如果他存在 处理节点设置为态度不同意弹出操作项 --不能快速处理，请您手动处理该协同！
                                    extParam.put("canFastProcess", "true");

            						/*if(!checkFastProcess(colSummary,affair, nodePolicy )){
                                extParam.put("showMsg",attitudeMap.get(DIS_FASTPROCESS));
                                extParam.put("canFastProcess", "false");
                            }*/
                                }
                            }
                        }
                    }
                }

                if (affair.getApp().intValue() == ApplicationCategoryEnum.meeting.getKey()) {
                    //判断发起人
                    boolean isSender = affair.getMemberId().equals(affair.getSenderId());
                    //判断告知人
                    boolean isImpart = "inform".equals(affair.getNodePolicy());
                    //是否显示快速回执
                    extParam.put("replyShowFlag", (isImpart || isSender) ? "0" : "1");
                }

                row.setExtParam(extParam);
            }

            rowList.add(row);
        }
        return rowList;
    }

    public static final String ATT_HAVEREAD = "collaboration.dealAttitude.haveRead";
    public static final String ATT_AGREE = "collaboration.dealAttitude.agree";
    public static final String ATT_DISAGREE = "collaboration.dealAttitude.disagree";
    public static final String DIS_FASTPROCESS = "collaboration.dealAttitude.disfastProcess";//"该协同不能快速处理，请您手动处理该协同！"
    public static final String CAN_FASTPROCESS = "canFastProcess";//能否快捷处理
    public static final String SHOW_MSG = "showMsg";//不能快捷处理给出


    //将Object类型的数据转换为Long类型
    private static Long objToLong(Object obj) {
        return obj == null ? null : ((Number) obj).longValue();
    }

    //将Object类型的数据转换为Integer类型
    private static Integer objToInteger(Object obj) {
        return obj == null ? null : ((Number) obj).intValue();
    }

    private static String[] getPendingCategoryLink(CtpAffair affair) {
        Map<String, Object> extMap = Strings.escapeNULL(AffairUtil.getExtProperty(affair), new HashMap<String, Object>());
        Integer subApp = affair.getSubApp();
        Long objectId = affair.getObjectId();

        String link = null;
        String categoryLink = null;

        ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(affair.getApp());
        Integer spaceType = objToInteger(extMap.get(AffairExtPropEnums.spaceType.name()));
        Long spaceId = objToLong(extMap.get(AffairExtPropEnums.spaceId.name()));
        Long typeId = objToLong(extMap.get(AffairExtPropEnums.typeId.name()));

        String from = "";
        if (!Integer.valueOf(SpaceType.corporation.ordinal()).equals(spaceType) && !Integer.valueOf(SpaceType.group.ordinal()).equals(spaceType)) {
            from = "&spaceType=" + spaceType + "&spaceId=" + spaceId;
        }

        switch (appEnum) {
            case news:
                link = "/newsData.do?method=newsView&newsId=" + objectId + "&affairId=" + affair.getId() + "&from=myAudit";
                categoryLink = "/newsData.do?method=newsMyInfo&type=4" + from;
                break;
            case bulletin:
                link = "/bulData.do?method=bulView&bulId=" + objectId + "&affairId=" + affair.getId() + "&from=myAudit";
                if (ApplicationSubCategoryEnum.bulletin_to_publish.key() == subApp.intValue()) {
                    categoryLink = "/bulData.do?method=bulMyInfo&type=1" + from;
                } else {
                    categoryLink = "/bulData.do?method=bulMyInfo&type=3" + from;
                }
                break;
            case inquiry:
                if (ApplicationSubCategoryEnum.inquiry_audit.key() == subApp.intValue()) { // 调查审核
                    link = "/inquiryData.do?method=inquiryView&inquiryId=" + objectId + "&affairId=" + affair.getId() + "&isAuth=true";
                    categoryLink = "/inquiryData.do?method=inquiryIAuth" + from;
                } else if (ApplicationSubCategoryEnum.inquiry_write.key() == subApp.intValue()) { // 调查填写
                    link = "/inquiryData.do?method=inquiryView&inquiryId=" + objectId + "&affairId=" + affair.getId();
                    categoryLink = "/inquiryData.do?method=inquiryBoardIndex&boardId=" + typeId + from;
                }
                break;
        }

        return new String[]{link, categoryLink};
    }

    private String getPolicyName(CtpAffair affair) {
        String policy = affair.getNodePolicy();
        if (Strings.isNotBlank(policy)) {
            return BPMSeeyonPolicy.getShowName(policy);
        }
        return "";
    }

    public boolean hasResPerm(CtpAffair affair, User user) {
        return hasResPerm(affair, user, true);
    }

    public boolean hasResPerm(CtpAffair affair, User user, boolean defaultHasResPerm) {
        ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(affair.getApp());
        Integer subApp = affair.getSubApp();

        boolean f = true;

        switch (appEnum) {
            case collaboration:
                //待办
                if (affair.getState() == StateEnum.col_pending.getKey()) {
                    f = user.hasResourceCode("F01_listPending");
                } else if (affair.getState() == StateEnum.col_done.getKey()) {
                    f = user.hasResourceCode("F01_listDone");
                } else if (affair.getState() == StateEnum.col_waitSend.getKey()) {
                    f = user.hasResourceCode("F01_listWaitSend");
                } else if (affair.getState() == StateEnum.col_sent.getKey()) {
                    f = user.hasResourceCode("F01_listSent");
                }
                break;
            case edoc:
                f = defaultHasResPerm;
                break;
            case bulletin:
                f = (user.hasResourceCode("F05_bulIndexGroup") || user.hasResourceCode("F05_bulIndexAccount"));
                break;
            case news:
                f = (user.hasResourceCode("F05_newsIndexGroup") || user.hasResourceCode("F05_newsIndexAccount"));
                break;
            case inquiry:
                f = (user.hasResourceCode("F05_inquiryIndexAccount") || user.hasResourceCode("F05_inquiryIndexGroup"));
                break;
            case office:
                if (ApplicationSubCategoryEnum.office_auto.key() == subApp.intValue()) { // 车辆
                    f = (user.hasResourceCode("F03_officeAutoUse"));
                } else if (ApplicationSubCategoryEnum.office_stock.key() == subApp.intValue()) { // 办公用品
                    f = (user.hasResourceCode("F03_officeStockUse"));
                } else if (ApplicationSubCategoryEnum.office_asset.key() == subApp.intValue()) { // 办公设备
                    f = (user.hasResourceCode("F03_officeAssetUse"));
                } else if (ApplicationSubCategoryEnum.office_book.key() == subApp.intValue()) { // 图书资料
                    f = (user.hasResourceCode("F03_officeBookUse"));
                }
                break;
            case meeting:
                if (affair.getState() == StateEnum.col_pending.getKey()) {
                    f = user.hasResourceCode("F09_meetingPending");
                } else if (affair.getState() == StateEnum.col_done.getKey()) {
                    f = user.hasResourceCode("F09_meetingDone");
                }
                break;
            case meetingroom:
                f = (user.hasResourceCode("F09_meetingRoom"));
                break;

            case info:
                if (affair.getState() == StateEnum.col_pending.getKey() || affair.getState() == StateEnum.col_done.getKey()) {//待办，已办
                    if (subApp.intValue() == ApplicationSubCategoryEnum.info_magazine.key()) {
                        f = user.hasResourceCode("F18_magazineAudit");
                    } else if (subApp.intValue() == ApplicationSubCategoryEnum.info_magazine_publish.key()) {
                        f = user.hasResourceCode("F18_magazinePublish");
                    } else {
                        f = user.hasResourceCode("F18_infoAudit");
                    }
                } else if (affair.getState() == StateEnum.col_sent.getKey()) {//已发
                    f = user.hasResourceCode("F18_infoReport");
                }
                break;

            default:
                f = true;
                break;
        }

        return f;
    }

    /**
     * 获取会议参会人员数量及总人数
     *
     * @return
     * @throws BusinessException
     */
    public Integer[] getJoinMeetingCount(CtpAffair affair) throws BusinessException {
        int processedNumber = 0;
        int totalNumber = 0;
        if (meetingApi != null) {
            processedNumber = meetingApi.getProcessedNumberByObjectId(affair.getObjectId());
            totalNumber = meetingApi.getTotalNumberByObjectId(affair.getObjectId());
        }
        return new Integer[]{processedNumber, totalNumber};
    }

    @Override
    public List<CtpAffair> getPendingAffairList(FlipInfo fp, Long memberId, Map<String, String> preference) {
        AffairCondition condition = getPendingSectionAffairCondition(memberId, preference);
        List<CtpAffair> affairs = new ArrayList<CtpAffair>();
        String currentPanel = SectionUtils.getPanel("all", preference);
        List<Integer> appEnum = new ArrayList<Integer>();
        if ("sender".equals(currentPanel)) {
            //查询指定发起人，用于查询指定发起人的时候查询比较复杂，所以采用HQL的方式进行查询，其他情况维持原来的逻辑不变
            String tempStr = preference.get(currentPanel + "_value");
            affairs = (List<CtpAffair>) affairManager.getAffairListBySender(memberId, tempStr, condition, false, fp, appEnum);
        } else {
            affairs = condition.getPendingAffairList(affairManager, fp);
        }
        return affairs;
    }

    @Override
    public List<CtpAffair> getZcdbAffairList(FlipInfo fp, Long memberId, Map<String, String> preference) {
        AffairCondition condition = getPendingSectionAffairCondition(memberId, preference);
        List<CtpAffair> affairs = new ArrayList<CtpAffair>();
        String currentPanel = SectionUtils.getPanel("all", preference);
        List<Integer> appEnum = new ArrayList<Integer>();
        if ("sender".equals(currentPanel)) {
            //查询指定发起人，用于查询指定发起人的时候查询比较复杂，所以采用HQL的方式进行查询，其他情况维持原来的逻辑不变
            String tempStr = preference.get(currentPanel + "_value");
            affairs = (List<CtpAffair>) affairManager.getAffairListBySender(memberId, tempStr, condition, false, fp, appEnum);
        } else {
            affairs = condition.getZcdbAffairList(affairManager, fp);
        }
        return affairs;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CtpAffair> getAffairsByCategoryAndImpLevl(FlipInfo fp,
                                                          List<Integer> colEnums, int i, Long memberId, Map<String, String> preference) {
        AffairCondition condition = getPendingSectionAffairCondition(memberId, preference);
        List<CtpAffair> affairs = new ArrayList<CtpAffair>();
        String currentPanel = SectionUtils.getPanel("all", preference);
        List<Integer> appEnum = new ArrayList<Integer>();
        if ("sender".equals(currentPanel)) {

            condition.addSearch(SearchCondition.importLevel, String.valueOf(i), "");
            //查询指定发起人，用于查询指定发起人的时候查询比较复杂，所以采用HQL的方式进行查询，其他情况维持原来的逻辑不变
            String tempStr = preference.get(currentPanel + "_value");
            affairs = (List<CtpAffair>) affairManager.getAffairListBySender(memberId, tempStr, condition, false, fp, appEnum);
        } else {
            affairs = condition.getAffairsByCategoryAndImpLevl(affairManager, fp, colEnums, i);
        }
        return affairs;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CtpAffair> getCollAffairs(FlipInfo fp, Long memberId, Map<String, String> preference, boolean isTemplete) {
        AffairCondition condition = getPendingSectionAffairCondition(memberId, preference);
        List<CtpAffair> affairs = new ArrayList<CtpAffair>();
        String currentPanel = SectionUtils.getPanel("all", preference);
        List<Integer> appEnum = new ArrayList<Integer>();
        if ("sender".equals(currentPanel)) {
            //查询指定发起人，用于查询指定发起人的时候查询比较复杂，所以采用HQL的方式进行查询，其他情况维持原来的逻辑不变
            String tempStr = preference.get(currentPanel + "_value");
            affairs = (List<CtpAffair>) affairManager.getAffairListBySender(memberId, tempStr, condition, false, fp, appEnum);
        } else {
            affairs = condition.getCollAffairs(affairManager, fp, isTemplete);
        }
        return affairs;
    }

    @Override
    public List<CtpAffair> getAffairCountByApp(FlipInfo fp, Long memberId, Map<String, String> preference, List<Integer> appEnums) {
        AffairCondition condition = getPendingSectionAffairCondition(memberId, preference);
        List<CtpAffair> affairs = new ArrayList<CtpAffair>();
        String currentPanel = SectionUtils.getPanel("all", preference);
        List<Integer> appEnum = new ArrayList<Integer>();
        if ("sender".equals(currentPanel)) {
            //查询指定发起人，用于查询指定发起人的时候查询比较复杂，所以采用HQL的方式进行查询，其他情况维持原来的逻辑不变
            String tempStr = preference.get(currentPanel + "_value");
            affairs = (List<CtpAffair>) affairManager.getAffairListBySender(memberId, tempStr, condition, false, fp, appEnum);
        } else {
            affairs = condition.getAffairCountByApp(affairManager, fp, appEnums);
        }
        return affairs;
    }

    @Override
    public Map transInitChartData(Map params) throws BusinessException {
        Map resultMap = new HashMap();
        String _fragmentId = (String) params.get("fragmentId");
        Long fragmentId = Long.valueOf(_fragmentId);
        String ordinal = (String) params.get("ordinal");
        Long memberId = AppContext.currentUserId();
        List<String> graphicalList = new ArrayList<String>();
        Map<String, String> preference = portalApi.getPropertys(fragmentId, ordinal);
        // 选择的统计图
        String graphical = preference.get("graphical_value");
        if (Strings.isBlank(graphical)) {
            graphical = "importantLevel,overdue,handlingState,handleType,exigency";
        }
        String[] graphicalArr = graphical.split(",");
        graphicalList = Arrays.asList(graphicalArr);

        //查询统计图所需要的数据
        for (String chartValue : graphicalList) {
            //办理类型
            if ("handleType".equals(chartValue)) {
                Map<String, Integer> pendingGroups = getGroupByApp(memberId, StateEnum.col_pending, preference);
                resultMap.put("allColl", pendingGroups.get("allColl")); //所有协同
                resultMap.put("zyxt", pendingGroups.get("zyxt"));//自由协同，
                resultMap.put("xtbdmb", pendingGroups.get("xtbdmb"));//协同/表单模板，
                resultMap.put("shouWen", pendingGroups.get("shouWen"));//收文
                resultMap.put("faWen", pendingGroups.get("faWen"));//发文
                resultMap.put("daiFaEdoc", pendingGroups.get("daiFaEdoc"));//待发送
                resultMap.put("daiQianShou", pendingGroups.get("daiQianShou"));//待签收
                resultMap.put("daiDengJi", pendingGroups.get("daiDengJi"));//待登记
                resultMap.put("qianBao", pendingGroups.get("qianBao"));//签报
                resultMap.put("huiYi", pendingGroups.get("huiYi"));//会议
                resultMap.put("huiYiShi", pendingGroups.get("huiYiShi"));//会议室审批
                resultMap.put("diaoCha", pendingGroups.get("diaoCha"));//调查
                resultMap.put("daiShenPiGGXX", pendingGroups.get("daiShenPiGGXX"));//待审批公共信息 (公告，新闻，调查，讨论)
                resultMap.put("daiShenPiZHBG", pendingGroups.get("daiShenPiZHBG"));//待审批综合办公审批

                resultMap.put("handleType", this.createHandleTypeChart(resultMap)); //图表参数
            }
            //重要程度
            else if ("importantLevel".equals(chartValue)) {
                Map<Integer, Integer> pendingGroups = getGroupByImportment(memberId, StateEnum.col_pending, preference, ApplicationCategoryEnum.collaboration.key());

                resultMap.put("import3Count", Strings.escapeNULL(pendingGroups.get(3), 0));//非常重要
                resultMap.put("import2Count", Strings.escapeNULL(pendingGroups.get(2), 0));//重要
                resultMap.put("import1Count", Strings.escapeNULL(pendingGroups.get(1), 0) + Strings.escapeNULL(pendingGroups.get(-1), 0));//普通，important_level为null的也按普通统计

                resultMap.put("importantLevel", this.createImportantLevelChart(resultMap)); //图表参数
            }
            //紧急程度（公文）
            else if ("exigency".equals(chartValue)) {
                List<Integer> edocEnums = new ArrayList<Integer>();
                edocEnums.add(ApplicationCategoryEnum.edocSend.getKey());//发文 19
                edocEnums.add(ApplicationCategoryEnum.edocRec.getKey());//收文 20
                edocEnums.add(ApplicationCategoryEnum.edocSign.getKey());//签报21
                edocEnums.add(ApplicationCategoryEnum.exSend.getKey());//待发送公文22
                edocEnums.add(ApplicationCategoryEnum.exSign.getKey());//待签收公文 23
                edocEnums.add(ApplicationCategoryEnum.edocRegister.getKey());//待登记公文 24
                edocEnums.add(ApplicationCategoryEnum.edocRecDistribute.getKey());//收文分发34

                Map<Integer, Integer> pendingGroups = getGroupByImportment(memberId, StateEnum.col_pending, preference, edocEnums.toArray(new Integer[edocEnums.size()]));

                resultMap.put("commonExigency", Strings.escapeNULL(pendingGroups.get(1), 0) + Strings.escapeNULL(pendingGroups.get(-1), 0));//普通，important_level为null的也按普通统计
                resultMap.put("pingAnxious", Strings.escapeNULL(pendingGroups.get(2), 0));//平急
                resultMap.put("expedited", Strings.escapeNULL(pendingGroups.get(3), 0));//加急
                resultMap.put("urgent", Strings.escapeNULL(pendingGroups.get(4), 0));//特急 exigency
                resultMap.put("teTi", Strings.escapeNULL(pendingGroups.get(5), 0));//特提 topExigency

                resultMap.put("exigency", this.createExigencyChart(resultMap)); //图表参数
            }
            //是否超期
            else if ("overdue".equals(chartValue)) {
                Map<String, Integer> map = getGroupByIsOverTime(memberId, StateEnum.col_pending, preference);

                resultMap.put("overdue", map.get("overdue"));//已超期
                resultMap.put("noOverdue", map.get("noOverdue"));//未超期

                resultMap.put("overdue", this.createOverdueChart(resultMap)); //图表参数
            }
            //办理状态
            else if ("handlingState".equals(chartValue)) {
                Map<Integer, Integer> pendingGroups = getGroupBySubState(memberId, StateEnum.col_pending, preference);

                int count = 0;
                for (Map.Entry<Integer, Integer> c : pendingGroups.entrySet()) {
                    if (!c.getKey().equals(SubStateEnum.col_pending_ZCDB.key())) {
                        count += c.getValue();
                    }
                }

                resultMap.put("pendingCount", count);//待办
                resultMap.put("zcdbCount", Strings.escapeNULL(pendingGroups.get(SubStateEnum.col_pending_ZCDB.key()), 0));//暂存待办

                resultMap.put("handlingState", this.createHandlingStateChart(resultMap)); //图表参数
            }
        }
        return resultMap;
    }

    //办理类型图表
    private ChartVO createHandleTypeChart(Map resultMap) throws BusinessException {
        if (MapUtils.isEmpty(resultMap)) {
            return null;
        }
        ChartBO bo = new ChartBO();
        bo.setNoDataText("collaboration.pending.noDataAlert2");
        //标题
        bo.setTitle(new Title().setText(ResourceUtil.getString("collaboration.pending.handleType")));
        //颜色列表
        List<String> colorList = new ArrayList<String>();
        //系列
        PieSerie pieSerie = new PieSerie();
        pieSerie.setSymbol("rectangle"); //长方形图例
        List<SerieItem> serieData = new ArrayList<SerieItem>();
        if (Integer.valueOf(resultMap.get("zyxt").toString()) != 0) {
            serieData.add(new SerieItem(resultMap.get("zyxt")).setId("zyxt").setName("collaboration.eventsource.category.collaboration"));
            colorList.add("#f2693d");
        }
        if (Integer.valueOf(resultMap.get("xtbdmb").toString()) != 0) {
            serieData.add(new SerieItem(resultMap.get("xtbdmb")).setId("xtbdmb").setName("collaboration.eventsource.category.collOrFormTemplete"));
            colorList.add("#8cc52b");
        }
        if (Integer.valueOf(resultMap.get("shouWen").toString()) != 0) {
            serieData.add(new SerieItem(resultMap.get("shouWen")).setId("shouWen").setName("collaboration.pending.lable1"));
            colorList.add("#ff7c1c");
        }
        if (Integer.valueOf(resultMap.get("faWen").toString()) != 0) {
            serieData.add(new SerieItem(resultMap.get("faWen")).setId("faWen").setName("collaboration.pending.lable2"));
            colorList.add("#efa900");
        }
        if (Integer.valueOf(resultMap.get("daiFaEdoc").toString()) != 0) {
            serieData.add(new SerieItem(resultMap.get("daiFaEdoc")).setId("daiFaEdoc").setName("collaboration.pending.lable3"));
            colorList.add("#f12924");
        }
        if (Integer.valueOf(resultMap.get("daiQianShou").toString()) != 0) {
            serieData.add(new SerieItem(resultMap.get("daiQianShou")).setId("daiQianShou").setName("collaboration.pending.lable4"));
            colorList.add("#b22600");
        }
        if (Integer.valueOf(resultMap.get("daiDengJi").toString()) != 0) {
            serieData.add(new SerieItem(resultMap.get("daiDengJi")).setId("daiDengJi").setName("collaboration.pending.lable5"));
            colorList.add("#e461a7");
        }
        if (Integer.valueOf(resultMap.get("qianBao").toString()) != 0) {
            serieData.add(new SerieItem(resultMap.get("qianBao")).setId("qianBao").setName("collaboration.pending.lable6"));
            colorList.add("#e02b9c");
        }
        if (Integer.valueOf(resultMap.get("huiYi").toString()) != 0) {
            serieData.add(new SerieItem(resultMap.get("huiYi")).setId("huiYi").setName("collaboration.pending.lable7"));
            colorList.add("#1e8bd0");
        }
        if (Integer.valueOf(resultMap.get("huiYiShi").toString()) != 0) {
            serieData.add(new SerieItem(resultMap.get("huiYiShi")).setId("huiYiShi").setName("collaboration.pending.lable8"));
            colorList.add("#077bbb");
        }
        if (Integer.valueOf(resultMap.get("daiShenPiGGXX").toString()) != 0) {
            serieData.add(new SerieItem(resultMap.get("daiShenPiGGXX")).setId("daiShenPiGGXX").setName("collaboration.pending.lable9"));
            colorList.add("#00585c");
        }
        if (Integer.valueOf(resultMap.get("daiShenPiZHBG").toString()) != 0) {
            serieData.add(new SerieItem(resultMap.get("daiShenPiZHBG")).setId("daiShenPiZHBG").setName("collaboration.pending.lable10"));
            colorList.add("#418700");
        }
        if (Integer.valueOf(resultMap.get("diaoCha").toString()) != 0) {
            serieData.add(new SerieItem(resultMap.get("diaoCha")).setId("diaoCha").setName("collaboration.pending.inquiry.label"));
            colorList.add("#14d6c0");
        }
        pieSerie.setData(serieData);
        bo.setSeries(pieSerie);
        bo.setColor(colorList.toArray(new String[]{}));
        return this.chartRender.render(bo);
    }

    //重要程度图表
    private ChartVO createImportantLevelChart(Map resultMap) throws BusinessException {
        if (MapUtils.isEmpty(resultMap)) {
            return null;
        }
        ChartBO bo = new ChartBO();
        bo.setNoDataText("collaboration.pending.noDataAlert2");
        //标题
        bo.setTitle(new Title().setText(ResourceUtil.getString("collaboration.pending.importantLevel")));
        //颜色列表
        bo.setColor("#f2693d", "#efa900", "#8cc52b");
        //系列
        PieSerie pieSerie = new PieSerie();
        pieSerie.setSymbol("rectangle"); //长方形图例
        List<SerieItem> serieData = new ArrayList<SerieItem>();
        serieData.add(new SerieItem(resultMap.get("import3Count")).setId("import3").setName("collaboration.newcoll.veryimportant"));
        serieData.add(new SerieItem(resultMap.get("import2Count")).setId("import2").setName("collaboration.newcoll.important"));
        serieData.add(new SerieItem(resultMap.get("import1Count")).setId("import1").setName("collaboration.pendingsection.importlevl.normal"));
        pieSerie.setData(serieData);
        bo.setSeries(pieSerie);
        return this.chartRender.render(bo);
    }

    //紧急程度（公文）图表
    private ChartVO createExigencyChart(Map resultMap) throws BusinessException {
        if (MapUtils.isEmpty(resultMap)) {
            return null;
        }
        ChartBO bo = new ChartBO();
        bo.setNoDataText("collaboration.pending.noDataAlert1");
        //标题
        bo.setTitle(new Title().setText(ResourceUtil.getString("collaboration.pending.exigencyGraph")));
        //颜色列表
        bo.setColor("#f12924", "#f2693d", "#ffde00", "#cc66ff", "#a9e051");
        //系列
        PieSerie pieSerie = new PieSerie();
        pieSerie.setSymbol("rectangle"); //长方形图例
        List<SerieItem> serieData = new ArrayList<SerieItem>();
        serieData.add(new SerieItem(resultMap.get("teTi")).setId("teTi").setName("collaboration.pending.exigencyNames3"));
        serieData.add(new SerieItem(resultMap.get("urgent")).setId("urgent").setName("collaboration.pending.exigencyNames1"));
        serieData.add(new SerieItem(resultMap.get("expedited")).setId("expedited").setName("collaboration.pending.exigencyNames2"));
        serieData.add(new SerieItem(resultMap.get("pingAnxious")).setId("pingAnxious").setName("collaboration.pending.exigencyNames4"));
        serieData.add(new SerieItem(resultMap.get("commonExigency")).setId("commonExigency").setName("collaboration.pendingsection.importlevl.normal"));
        pieSerie.setData(serieData);
        bo.setSeries(pieSerie);
        return this.chartRender.render(bo);
    }

    //是否超期图表
    private ChartVO createOverdueChart(Map resultMap) throws BusinessException {
        if (MapUtils.isEmpty(resultMap)) {
            return null;
        }
        ChartBO bo = new ChartBO();
        bo.setNoDataText("collaboration.pending.noDataAlert2");
        //标题
        bo.setTitle(new Title().setText(ResourceUtil.getString("collaboration.pending.overdueGraph")));
        //颜色列表
        bo.setColor("#1e8bd0", "#f2693d");
        //系列
        PieSerie pieSerie = new PieSerie();
        pieSerie.setSymbol("rectangle"); //长方形图例
        List<SerieItem> serieData = new ArrayList<SerieItem>();
        serieData.add(new SerieItem(resultMap.get("noOverdue")).setId("noOverdue").setName("collaboration.pending.overdueNames3"));
        serieData.add(new SerieItem(resultMap.get("overdue")).setId("overdue").setName("collaboration.pending.overdueNames2"));
        pieSerie.setData(serieData);
        bo.setSeries(pieSerie);
        return this.chartRender.render(bo);
    }

    //办理状态图表
    private ChartVO createHandlingStateChart(Map resultMap) throws BusinessException {
        if (MapUtils.isEmpty(resultMap)) {
            return null;
        }
        ChartBO bo = new ChartBO();
        bo.setNoDataText("collaboration.pending.noDataAlert2");
        //标题
        bo.setTitle(new Title().setText(ResourceUtil.getString("collaboration.pending.handlingState.name")));
        //颜色列表
        bo.setColor("#8cc52b", "#1e8bd0");
        //系列
        PieSerie pieSerie = new PieSerie();
        pieSerie.setSymbol("rectangle"); //长方形图例
        List<SerieItem> serieData = new ArrayList<SerieItem>();
        serieData.add(new SerieItem(resultMap.get("pendingCount")).setId("pending").setName("collaboration.pending.handlingState.pending"));
        serieData.add(new SerieItem(resultMap.get("zcdbCount")).setId("zcdb").setName("collaboration.pending.handlingState.zcdb"));
        pieSerie.setData(serieData);
        bo.setSeries(pieSerie);
        return this.chartRender.render(bo);
    }

    @Override
    public List<CtpAffair> getAffairsIsOverTime(FlipInfo fp, Long memberId,
                                                Map<String, String> preference, boolean isOverTime) {
        AffairCondition condition = getPendingSectionAffairCondition(memberId, preference);
        List<CtpAffair> affairs = new ArrayList<CtpAffair>();
        String currentPanel = SectionUtils.getPanel("all", preference);
        List<Integer> appEnum = new ArrayList<Integer>();
        if ("sender".equals(currentPanel)) {
            //查询指定发起人，用于查询指定发起人的时候查询比较复杂，所以采用HQL的方式进行查询，其他情况维持原来的逻辑不变
            String tempStr = preference.get(currentPanel + "_value");
            affairs = (List<CtpAffair>) affairManager.getAffairListBySender(memberId, tempStr, condition, false, fp, appEnum);
        } else {
            affairs = condition.getAffairsIsOverTime(affairManager, fp, isOverTime);
        }
        return affairs;
    }

    /**
     * 首页会议召开时间显示
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public String convertMeetingTime(java.util.Date beginDate, java.util.Date endDate) {
        String beginDatetime = DateUtil.format(beginDate, "yyyy-MM-dd HH:mm");
        String endDatetime = DateUtil.format(endDate, "yyyy-MM-dd HH:mm");
        String startDateYearMD = beginDatetime.substring(0, 10);
        String endDateYearMD = endDatetime.substring(0, 10);

        /**
         * 判断今天，明天
         */
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        String today = Datetimes.format(calendar.getTime(), Datetimes.dateStyle);
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        calendar.add(Calendar.DATE, 1);
        String tomorrow = Datetimes.format(calendar.getTime(), Datetimes.dateStyle);
        String beginDateI18n = beginDatetime.substring(5, 10), endDateI18n = endDatetime.substring(5, 10);
        String beginYear = beginDatetime.substring(0, 4), endYear = endDatetime.substring(0, 4);
        if (startDateYearMD.equals(today)) {
            beginDateI18n = ResourceUtil.getString("meeting.list.date.today");
        } else if (startDateYearMD.equals(tomorrow)) {
            beginDateI18n = ResourceUtil.getString("meeting.list.date.tomorrow");
        } else if (!beginYear.equals(year) || !beginYear.equals(endYear)) {
            beginDateI18n = beginDatetime.substring(0, 10);
        }

        if (endDateYearMD.equals(today)) {
            endDateI18n = ResourceUtil.getString("meeting.list.date.today");
        } else if (endDateYearMD.equals(tomorrow)) {
            endDateI18n = ResourceUtil.getString("meeting.list.date.tomorrow");
        } else if (!endYear.equals(year) || !endYear.equals(beginYear)) {
            endDateI18n = endDatetime.substring(0, 10);
        }

        String showTime = "";
        //同一天显示：例： 12-20 13:50 - 16:50 ，同年显示：12-20 13:50 - 12-21 13:50,不同年显示全部
        if (startDateYearMD.equals(endDateYearMD)) {
            showTime = beginDateI18n + " " + beginDatetime.substring(11, 16) + " - "
                    + endDatetime.substring(11, 16);
        } else { //日期不同、跨年都是显示月份
            showTime = beginDateI18n + " " + beginDatetime.substring(11, 16) + " - "
                    + endDateI18n + " "
                    + endDatetime.substring(11, 16);
        }
        return showTime;
    }

    /**
     * 两个时间是否为同一天
     *
     * @param oneDay
     * @param twoDate
     * @return
     */
    @SuppressWarnings("deprecation")
    public boolean isTheSameDay(java.util.Date oneDay, java.util.Date twoDate) {
        return oneDay.getYear() == twoDate.getYear() && oneDay.getMonth() == twoDate.getMonth() && oneDay.getDate() == twoDate.getDate();
    }

    /**
     * 判断两天是否是在同一年
     *
     * @param oneDay
     * @param twoDate
     * @return
     */
    public boolean isTheSameYear(java.util.Date oneDay, java.util.Date twoDate) {
        return oneDay.getYear() == twoDate.getYear();
    }

    @Override
    public int getAgentPendingCount(long memberId) throws BusinessException {
        // TODO Auto-generated method stub
        return 0;
    }

    public static String getMenuIdByApp(int appEnum) {
        String menuResource = "";
        if (appEnum == ApplicationSubCategoryEnum.edoc_fawen.getKey()
                || appEnum == ApplicationSubCategoryEnum.old_edocSend.getKey()
                || appEnum == ApplicationSubCategoryEnum.old_exSend.getKey()) {
            menuResource = "F20_govDocSendManage";
        } else if (appEnum == ApplicationSubCategoryEnum.edoc_shouwen.getKey()
                || appEnum == ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()
                || appEnum == ApplicationSubCategoryEnum.old_edocRec.getKey()
                || appEnum == ApplicationSubCategoryEnum.old_exSign.getKey()
                || appEnum == ApplicationSubCategoryEnum.old_edocRegister.getKey()
                || appEnum == ApplicationSubCategoryEnum.old_edocRecDistribute.getKey()) {
            menuResource = "F20_receiveManage";
        } else if (appEnum == ApplicationSubCategoryEnum.edoc_qianbao.getKey()) {
            menuResource = "F20_signReport";
        }
        return menuResource;
    }

    private Map<String, String> getMeetingPendingCount(List<CtpAffair> meetingAppAffairList) throws BusinessException {
        Map<String, String> countMap = new HashMap<String, String>();
        try {
            List<CtpAffair> joinList = new ArrayList<CtpAffair>();
            List<CtpAffair> unJoinList = new ArrayList<CtpAffair>();
            List<CtpAffair> pendingList = new ArrayList<CtpAffair>();
            if (Strings.isNotEmpty(meetingAppAffairList)) {
                for (CtpAffair ctpAffair : meetingAppAffairList) {
                    if (ctpAffair.getMemberId().longValue() == ctpAffair.getSenderId().longValue()) {
                        joinList.add(ctpAffair);
                    } else {
                        if (ctpAffair.getState() == StateEnum.mt_unAttend.key()) {// 不参加
                            unJoinList.add(ctpAffair);
                        } else {
                            if (ctpAffair.getSubState() == SubStateEnum.meeting_pending_join.key()) {// 参加
                                joinList.add(ctpAffair);
                            } else if (ctpAffair.getSubState() == SubStateEnum.meeting_pending_unJoin.key()) {// 不参加
                                unJoinList.add(ctpAffair);
                            } else if (ctpAffair.getSubState() == SubStateEnum.meeting_pending_pause.key()) {// 待定
                                pendingList.add(ctpAffair);
                            }
                        }
                    }
                }
            }
            countMap.put("joinCount", String.valueOf(joinList.size()));
            countMap.put("unjoinCount", String.valueOf(unJoinList.size()));
            countMap.put("pendingCount", String.valueOf(pendingList.size()));
            countMap.put("allCount", String.valueOf(meetingAppAffairList.size()));
        } catch (Exception e) {
            log.info("m3首页获取会议状态出错", e);
        }
        return countMap;
    }

    /**
     * 将分钟数按当前工作时间转化为按天表示的时间。
     * 超过一天单位为天，没超过一天的单位为小时
     * 例如 1天、7小时。
     */
    private String showDate(Integer minutes, boolean isWork) {
        if (minutes == null || minutes == 0)
            return "－";
        int dayH = 24 * 60;
        if (isWork) {
            Calendar cal = Calendar.getInstance();
            Integer workTime = getCurrentYearWorkTime();
            if (workTime == null || workTime.intValue() == 0) {
                return "－";
            }
            dayH = workTime;
        }

        long m = minutes.longValue();
        long day = m / dayH;
        long d1 = 0;
        long hour = 0;
        if (day <= 0) {
            d1 = m % dayH;
            hour = d1 / 60;
        }

        String display
                = ResourceUtil.getStringByParams("collaboration.date.pending",
                day > 0 ? day : "",
                day > 0 ? 1 : 0,
                hour > 0 ? hour : "",
                hour > 0 ? 1 : 0);
        //{0}{1,choice,0#|1#\u5929}{2}{3,choice,0#|1#\u5C0F\u65F6}
        return display;
    }

    private int getCurrentYearWorkTime() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int t = 0;
        try {
            t = workTimeManager.getEachDayWorkTime(year, AppContext.getCurrentUser().getLoginAccount());
        } catch (WorkTimeSetExecption e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return t;
    }

    @Override
    @AjaxAccess
    public String toTheTop(Map<String, String> parm) throws BusinessException {
        try {
            String affairId = parm.get("affairId");
            boolean isTop = Boolean.parseBoolean(parm.get("isTop"));
            boolean res = true;
            // isTop:true置顶、false取消置顶
            if (Strings.isEmpty(affairId)) {
                // "校验参数不合法！";
                return "failure";
            }

            if (Strings.isNotBlank(affairId)) {
                CtpAffair affair = affairManager.get(Long.valueOf(affairId));
                if (isTop) {// 置顶
                    Date now = new Date();
                    affair.setTopTime(now);
                } else {// 取消置顶
                    Date date = new java.sql.Timestamp(-28800000l); //"1970-01-01 00:00:00"
                    affair.setTopTime(date);//初始值
                }
                affairManager.updateAffair(affair);
                return "success";// 操作成功
            }

            return "";
        } catch (Exception e) {
            log.error("", e);
            return "failure";
        }
    }

    /**
     * 获取模板分类待办总数
     */
    @Override
    public int getPendingTemplateCount(Long memberId) throws BusinessException {
        try {
            return pendingDao.getPendingTemplateCount(memberId);
        } catch (Exception e) {
            log.error("获取模板分类待办总数异常", e);
            return 0;
        }
    }

    /**
     * 获取模板分类具体待办数
     */
    @Override
    public Map<Long, Integer> getPendingTemplateDetailCount(Long memberId) throws BusinessException {
        try {
            AffairCondition condition = new AffairCondition();
            condition.setMemberId(memberId);
            Map<String, Object> parameter = new HashMap<String, Object>();

            StringBuilder hql = getAgentHql(memberId, condition, parameter);

            return pendingDao.getPendingTemplateDetailCount(condition, hql, parameter);
        } catch (Exception e) {
            log.error("获取模板分类具体待办数异常", e);
            return new HashMap<Long, Integer>();
        }
    }

    /**
     * 获取组织分类具体待办数
     *
     * @return Map<memberId, count>
     */
    @Override
    public Map<Long, Integer> getPendingStaffDetailCount(Long memberId) throws BusinessException {
        try {
            AffairCondition condition = new AffairCondition();
            condition.setMemberId(memberId);
            Map<String, Object> parameter = new HashMap<String, Object>();

            //StringBuilder hql = getAgentHql(memberId,condition, parameter);
            return pendingDao.getPendingStaffDetailCount(condition, null, parameter);
        } catch (Exception e) {
            log.error("获取组织分类具体待办数异常", e);
            return new HashMap<Long, Integer>();
        }
    }

    @Override
    public List<CtpAffair> getRecentTemplate(Long memberId) {
        try {
            AffairCondition condition = new AffairCondition();
            condition.setMemberId(memberId);
            Map<String, Object> parameter = new HashMap<String, Object>();

            StringBuilder hql = getAgentHql(memberId, condition, parameter);
            return pendingDao.getRecentTemplate(condition, hql, parameter, null);

        } catch (Exception e) {
            log.error("获取组织分类具体待办数异常", e);
            return new ArrayList<CtpAffair>();
        }
    }

    @Override
    public Map<Long, CtpTemplate> getTemplateById(List<Long> templateIds) throws BusinessException {
        try {
            Map<Long, CtpTemplate> TemplateMap = new HashMap<Long, CtpTemplate>();
            List<CtpTemplate> templates = pendingDao.getTemplateById(templateIds);
            if (templates.size() > 0) {
                for (CtpTemplate template : templates) {
                    TemplateMap.put(template.getId(), template);
                }
            }

            return TemplateMap;
        } catch (Exception e) {
            log.error("获取模板分类具体待办数异常", e);
            return new HashMap<Long, CtpTemplate>();
        }
    }

    @Override
    public List<CtpTemplateCategory> getTemplateCategoryByIds(List<Long> categoryIds) throws BusinessException {
        try {
            return pendingDao.getTemplateCategoryByIds(categoryIds);
        } catch (Exception e) {
            log.error("获取模板分类具体待办数异常", e);
            return new ArrayList<CtpTemplateCategory>();
        }
    }

    @Override
    public void createDepartmentParentTreeVO(Map<Long, OrgDepartmentTreeVo> treeMap, V3xOrgMember member, Integer affairCount, int allCount, List<OrgDepartmentTreeVo> memberTreeList) throws BusinessException {

        List<MemberPost> allMemberPost = orgManager.getMemberPosts(null, member.getId());
        List<Long> memberDepIds = new ArrayList<Long>();//排除主岗和副岗在同一个部门下面
        for (MemberPost post : allMemberPost) {
            Long depId = post.getDepId();
            if (memberDepIds.contains(depId)) {
                continue;
            }
            memberDepIds.add(depId);
            V3xOrgDepartment department = orgManager.getDepartmentById(depId);
            if (!department.isValid()) {
                continue;
            }
            //构造当前人员
            OrgDepartmentTreeVo memberTree = new OrgDepartmentTreeVo(member.getId(), member.getName(), department.getId(), "Member", affairCount);
            // 排序号
            memberTree.setSortId(member.getSortId());

            memberTreeList.add(memberTree);

            //构造当前部门
            if (treeMap.get(post.getDepId()) == null) {
                Long parentId = this.getDepartmentParentId(department.getPath(), department.getOrgAccountId());
                OrgDepartmentTreeVo departmentTree = new OrgDepartmentTreeVo(department.getId(), department.getName(), parentId, department.getType().toString(), affairCount);
                departmentTree.setSortId(department.getSortId());
                treeMap.put(department.getId(), departmentTree);
            } else {
                Integer count = treeMap.get(department.getId()).getAffairCount();
                treeMap.get(department.getId()).setAffairCount(count + affairCount);
            }

            //构造父类树
            Long unitId = post.getDepId();
            while (true) {
                V3xOrgUnit prentUnit = orgManager.getParentUnitById(unitId);

                if (prentUnit == null || !prentUnit.isValid()) {
                    break;
                }
                unitId = prentUnit.getId();
                if (treeMap.get(prentUnit.getId()) == null) {
                    if (prentUnit.getPath().length() > 4) {
                        Long parentId = this.getDepartmentParentId(prentUnit.getPath(), prentUnit.getOrgAccountId());
                        OrgDepartmentTreeVo tree = new OrgDepartmentTreeVo(prentUnit.getId(), prentUnit.getName(), parentId, prentUnit.getType().toString(), affairCount);
                        tree.setSortId(prentUnit.getSortId());
                        treeMap.put(prentUnit.getId(), tree);
                    } else {//集团
                        boolean isGroupVer = (Boolean) (SysFlag.sys_isGroupVer.getFlag());// 判断是否为集团版
                        if (isGroupVer) {
                            Long parentId = this.getDepartmentParentId(prentUnit.getPath(), prentUnit.getOrgAccountId());
                            OrgDepartmentTreeVo tree = new OrgDepartmentTreeVo(prentUnit.getId(), prentUnit.getName(), parentId, prentUnit.getType().toString(), allCount);
                            tree.setSortId(prentUnit.getSortId());
                            treeMap.put(prentUnit.getId(), tree);
                        }
                    }
                } else {
                    if (prentUnit.getPath().length() > 4) {//排除集团，集团总待办数allCount
                        Integer count = treeMap.get(prentUnit.getId()).getAffairCount();
                        treeMap.get(prentUnit.getId()).setAffairCount(count + affairCount);
                    }
                }
            }
        }
    }

    private Long getDepartmentParentId(String path, Long accountId) throws BusinessException {

        Long parentId = accountId;
        if (path.length() > 8) {
            String subPath = path.substring(0, path.length() - 4);//截取掉后4位
            V3xOrgDepartment deparent = orgManager.getDepartmentByPath(subPath);
            if (deparent != null) {
                parentId = deparent.getId();
            } else {
                V3xOrgAccount account = orgManager.getAccountByPath(subPath);
                if (null != account) {
                    parentId = account.getId();
                }
            }
        } else {//Account
            V3xOrgAccount account = orgManager.getRootAccount(accountId);
            if (null != account) {
                parentId = account.getId();
            }
        }

        return parentId;
    }

    @Override
    public Map<String, String> getPendingDetailContent(PendingRow pendingRow) {
        Map<String, String> extParam = new HashMap<String, String>();

        //回复数（共有{0}条回复)
        List<Integer> replyEnums = new ArrayList<Integer>();
        //replyEnums.add(ApplicationCategoryEnum.edocSend.getKey());//发文 19
        //replyEnums.add(ApplicationCategoryEnum.edocRec.getKey());//收文 20
        //replyEnums.add(ApplicationCategoryEnum.edocSign.getKey());//签报21
        replyEnums.add(ApplicationCategoryEnum.collaboration.getKey());//协同
        if (replyEnums.contains(pendingRow.getApplicationCategoryKey())) {
            Integer count = pendingRow.getReplyCounts();
            if (null == count || count.equals(0)) {
                extParam.put("replyCount", ResourceUtil.getString("common.pending.replyCountNoData"));
            } else {
                extParam.put("replyCount", ResourceUtil.getString("common.pending.replyCount", pendingRow.getReplyCounts()));
            }
        }
        //超期（已超期、未超期）
        if (pendingRow.isShowClockIcon()) {
            if (pendingRow.getDealTimeout() && pendingRow.getDeadlineTime() != null && !"".equals(pendingRow.getDeadlineTime()) && !"－".equals(pendingRow.getNodeadlineTime())) {
                extParam.put("detailOvertime", ResourceUtil.getString("common.pending.detailOvertime", pendingRow.getDeadlineTime()));
            } else if (pendingRow.getShowDealLineTime() && pendingRow.getNodeadlineTime() != null && !"".equals(pendingRow.getNodeadlineTime()) && !"－".equals(pendingRow.getNodeadlineTime())) {
                extParam.put("detailNoOvertime", ResourceUtil.getString("common.pending.detailNoOvertime", pendingRow.getNodeadlineTime()));
            }
        }
        //会议：参加、不参加、待定
        if (pendingRow.getApplicationCategoryKey() != null && (ApplicationCategoryEnum.meeting.getKey() == pendingRow.getApplicationCategoryKey())) {
            extParam.put("meetingCountJoin", ResourceUtil.getString("common.pending.meetingCountJoin", pendingRow.getProcessedNumber()));
            extParam.put("meetingCountNoJoin", ResourceUtil.getString("common.pending.meetingCountNoJoin", pendingRow.getUnJoinNumber()));
            extParam.put("meetingCountPending", ResourceUtil.getString("common.pending.meetingCountPending", pendingRow.getPendingNumber()));
        }
        //催办次数
        Integer categoryKey = pendingRow.getApplicationCategoryKey();
        List<Integer> hastenEnums = new ArrayList<Integer>();
        hastenEnums.add(ApplicationCategoryEnum.edocSend.getKey());//发文 19
        hastenEnums.add(ApplicationCategoryEnum.edocRec.getKey());//收文 20
        hastenEnums.add(ApplicationCategoryEnum.edocSign.getKey());//签报21
        hastenEnums.add(ApplicationCategoryEnum.exSign.getKey());//待签收公文 23
        hastenEnums.add(ApplicationCategoryEnum.collaboration.getKey());//协同1
        hastenEnums.add(ApplicationCategoryEnum.meeting.getKey());//会议6
        hastenEnums.add(ApplicationCategoryEnum.meetingroom.getKey());//会议室29
        hastenEnums.add(ApplicationCategoryEnum.inquiry.getKey());//调查10
        Integer hastenTime = pendingRow.getHastenTimes();
        if (hastenEnums.contains(categoryKey) && hastenTime != null) {
            extParam.put("RemindCount", ResourceUtil.getString("common.pending.RemindCount", hastenTime));
        }

        return extParam;
    }

    private void getCondition4Pending(AffairCondition condition, Map query) {
        String[] conditionText = new String[]{"subject", "importLevel", "sender", "createDate", "receiveDate", "subState", "applicationEnum", "expectedProcessTime"};
        for (int i = 0; i < conditionText.length; i++) {
            String textField = (String) query.get(conditionText[i]);
            if (Strings.isNotBlank(textField)) {
                String textField1 = "";
                SearchCondition con = AffairCondition.SearchCondition.valueOf(conditionText[i]);
                if (con != null) {
                    //对时间进行特殊处理，当开始日期时，将时间置为最小，当是结束时间时，将时间置为最大
                    if (AffairCondition.SearchCondition.createDate.name().equals(conditionText[i]) ||
                            AffairCondition.SearchCondition.receiveDate.name().equals(conditionText[i]) ||
                            AffairCondition.SearchCondition.dealDate.name().equals(conditionText[i]) ||
                            AffairCondition.SearchCondition.expectedProcessTime.name().equals(conditionText[i])) {
                        String arr[] = textField.split("#");
                        textField = arr[0];
                        if (Strings.isNotBlank(textField)) {
                            textField = Datetimes.getServiceFirstTime(textField);
                        }
                        if (arr.length > 1) {
                            textField1 = arr[1];
                            if (Strings.isNotBlank(textField1)) {
                                textField1 = Datetimes.getServiceLastTime(textField1);
                            }
                        }
                    }
                    condition.addSearch(con, textField, textField1, true);
                }
            }
        }
    }

    @AjaxAccess
    @Override
    public FlipInfo getMoreRecentList4SectionContion(FlipInfo fi, Map query) throws BusinessException {
        User user = AppContext.getCurrentUser();
        Long memberId = user.getId();
        //根据前端传递过来的页码重置页码
        if (query.get("page") != null) {
            fi.setPage(Integer.parseInt(query.get("page").toString()));
        }
        if (fi.getSortField() == null) {
            SortPair TopTime = fi.new SortPair("affair.topTime");
            TopTime.setSortOrder(Order.DESC);
            fi.addSortPair(TopTime);

            if (AppContext.hasPlugin("ai") && "1".equals(query.get("aiSortValue"))) {
                SortPair sortWeight = fi.new SortPair("affair.sortWeight");
                sortWeight.setSortOrder(Order.DESC);
                fi.addSortPair(sortWeight);
            }

            SortPair receiveTime = fi.new SortPair("affair.receiveTime");
            receiveTime.setSortOrder(Order.DESC);
            fi.addSortPair(receiveTime);
        }
        AffairCondition condition = new AffairCondition();
        condition.setMemberId(memberId);
        Map<String, Object> parameter = new HashMap<String, Object>();
        //查询
        String conditions = (String) query.get("condition");
        StringBuilder hql = getAgentHql(memberId, condition, parameter);

        if (Strings.isNotBlank(conditions)) {
            StringBuilder str = getRecentTemplateSearcnHql(query, parameter);
            if (Strings.isNotBlank(str.toString())) {
                if (Strings.isNotBlank(hql.toString())) {
                    hql.append(" and ");
                }
                hql.append("( ");
                hql.append(str);
                hql.append(" )");
            }
        }

        List<CtpAffair> affairList = pendingDao.getRecentTemplate(condition, hql, parameter, fi);

        Map<String, String> preference = new HashMap<String, String>();
        if (query.get("fragmentId") != null && query.get("ordinal") != null) {
            String fragmentIdStr = query.get("fragmentId").toString();
            if (Strings.isNotBlank(fragmentIdStr)) {
                Long fragmentId = Long.parseLong(fragmentIdStr);
                String ordinal = query.get("ordinal").toString();
                preference = portalApi.getPropertys(fragmentId, ordinal);
            }
        }
        String rowStr = preference.get("rowList");
        if (Strings.isBlank(rowStr)) {
            rowStr = "subject,receiveTime,sendUser,category,currentNodesInfo";
        }
        boolean isNeedReplayCounts = Boolean.TRUE.equals(query.get("isNeedReplayCounts"));
        boolean processingProgress = Boolean.TRUE.equals(query.get("processingProgress"));
        if ((query.containsKey("showCurNodesInfo") && (Boolean) query.get("showCurNodesInfo"))) {
            if (Strings.isNotBlank(rowStr) && rowStr.indexOf("currentNodesInfo") == -1) {
                rowStr += ",currentNodesInfo";
            }
        }
        if (processingProgress) {
            if (Strings.isNotBlank(rowStr) && rowStr.indexOf("processingProgress") == -1) {
                rowStr += ",processingProgress";//当前回复数
            }
        }
        List<PendingRow> voList = affairList2PendingRowList(affairList, user, null, false, rowStr, StateEnum.col_pending.key());
        fi.setData(voList);
        return fi;
    }

    /**
     * 待办查询组装代理sql
     *
     * @param memberId
     * @param condition
     * @param parameter
     * @return
     */
    private StringBuilder getAgentHql(Long memberId, AffairCondition condition, Map<String, Object> parameter) {
        StringBuilder sbHql = new StringBuilder();
        condition = new AffairCondition(memberId, StateEnum.col_pending,
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

        Object[] agentObj = AgentUtil.getUserAgentToMap(memberId);
        boolean agentToFlag = (Boolean) agentObj[0];
        Map<Integer, List<AgentModel>> agentList = (Map<Integer, List<AgentModel>>) agentObj[1];

        if (agentToFlag) {
            condition.setAgent(agentToFlag, agentList);

            sbHql.append(affairManager.getCondition4Agent(condition, parameter, true));
        }


        return sbHql;
    }

    public StringBuilder getRecentTemplateSearcnHql(Map query, Map<String, Object> parameter) {
        StringBuilder hql = new StringBuilder();
        String condition = (String) query.get("condition");


        if (!"comQuery".equals(condition)) {
            String textField1 = (String) query.get("textfield");
            String textField2 = (String) query.get("textfield1");
            String value = textField1;
            if (Strings.isNotBlank(textField2)) {
                value = textField1 + "#" + textField2;
            }
            query.put(condition, value);
        }
        String[] conditionText = new String[]{"subject", "importLevel", "sender", "createDate", "receiveDate", "subState", "applicationEnum", "expectedProcessTime"};
        for (int i = 0; i < conditionText.length; i++) {
            String textField = (String) query.get(conditionText[i]);
            SearchCondition sc = SearchCondition.valueOf(conditionText[i]);
            if (Strings.isNotBlank(textField)) {
                switch (sc) {
                    case subject:
                        if (Strings.isNotBlank(hql.toString())) {
                            hql.append(" and ");
                        }
                        hql.append(" (affair.subject like :subject) ");
                        parameter.put("subject", "%" + SQLWildcardUtil.escape(textField) + "%");
                        break;
                    case importLevel:
                        if (Strings.isNotBlank(hql.toString())) {
                            hql.append(" and ");
                        }
                        hql.append(" (affair.importantLevel = :importantLevel) ");
                        parameter.put("importantLevel", Integer.parseInt(textField));
                        //重要紧急程度需屏蔽信息报送
                        hql.append(" and affair.app != (:infoApp)");
                        parameter.put("infoApp", ApplicationCategoryEnum.info.key());
                        break;
                    case sender:
                        if (Strings.isNotBlank(hql.toString())) {
                            hql.append(" and ");
                        }
                        hql.append(" exists (select org.id from OrgMember as org where org.id=affair.senderId and org.name like :sender) ");
                        parameter.put("sender", "%" + SQLWildcardUtil.escape(textField) + "%");
                        break;
                    case createDate:
                        if (Strings.isNotBlank(hql.toString())) {
                            hql.append(" and ");
                        }
                        String arr[] = textField.split("#");
                        textField = arr[0];
                        if (Strings.isNotBlank(textField)) {
                            textField = Datetimes.getServiceFirstTime(textField);
                            Date startDate = Datetimes.parseNoTimeZone(textField, null);
                            hql.append(" (affair.createDate >= :createDate1) ");
                            parameter.put("createDate1", startDate);
                        }
                        if (arr.length > 1) {
                            String textField1 = arr[1];
                            if (Strings.isNotBlank(textField) && Strings.isNotBlank(textField1)) {
                                hql.append(" and ");
                            }
                            if (Strings.isNotBlank(textField1)) {
                                textField1 = Datetimes.getServiceLastTime(textField1);
                                Date startDate = Datetimes.parseNoTimeZone(textField1, "yyyy-MM-dd HH:mm:ss");
                                hql.append(" (affair.createDate <= :createDate2) ");
                                parameter.put("createDate2", startDate);
                            }
                        }
                        break;
                    case receiveDate:
                        if (Strings.isNotBlank(hql.toString())) {
                            hql.append(" and ");
                        }
                        String arr1[] = textField.split("#");
                        textField = arr1[0];
                        if (Strings.isNotBlank(textField)) {
                            textField = Datetimes.getServiceFirstTime(textField);
                            Date receiveTime = Datetimes.parseNoTimeZone(textField, null);
                            hql.append(" (affair.receiveTime >= :receiveTime1) ");
                            parameter.put("receiveTime1", receiveTime);
                        }
                        if (arr1.length > 1) {
                            String textField1 = arr1[1];
                            if (Strings.isNotBlank(textField) && Strings.isNotBlank(textField1)) {
                                hql.append(" and ");
                            }
                            if (Strings.isNotBlank(textField1)) {
                                textField1 = Datetimes.getServiceLastTime(textField1);
                                Date receiveTime = Datetimes.parseNoTimeZone(textField1, "yyyy-MM-dd HH:mm:ss");
                                hql.append(" (affair.receiveTime <= :receiveTime2) ");
                                parameter.put("receiveTime2", receiveTime);
                            }
                        }
                        break;
                    case subState:
                        if (Strings.isNotBlank(hql.toString())) {
                            hql.append(" and ");
                        }
                        String subState = textField;
                        if (subState.length() == 1 && NumberUtils.isNumber(subState)) {
                            if (String.valueOf(SubStateEnum.col_waitSend_stepBack.getKey()).equals(subState)
                                    || String.valueOf(SubStateEnum.col_waitSend_sendBack.getKey()).equals(subState)) {
                                hql.append(" (affair.subState = :subState or affair.backFromId is not null) ");
                                parameter.put("subState", Integer.parseInt(subState));
                                //去掉暂存待办的数据
                                hql.append(" and (affair.subState != :zcSubState and affair.backFromId is not null) ");
                                parameter.put("zcSubState", SubStateEnum.col_pending_ZCDB.getKey());
                            } else {
                                hql.append(" affair.subState = :subState ");
                                parameter.put("subState", Integer.parseInt(subState));
                            }
                            //指定回退
                        } else if (String.valueOf(SubStateEnum.col_pending_specialBack.getKey()).equals(subState)) {
                            List<Integer> subStateList = new ArrayList<Integer>();
                            subStateList.add(SubStateEnum.col_pending_specialBack.getKey());
                            subStateList.add(SubStateEnum.col_pending_specialBackCenter.getKey());
                            subStateList.add(SubStateEnum.col_pending_specialBackToSenderCancel.getKey());
                            subStateList.add(SubStateEnum.col_pending_specialBackToSenderReGo.getKey());
                            hql.append(" (affair.sunState in (:subStateList)) ");
                            parameter.put("subStateList", subStateList);
                            //已读
                        } else if (String.valueOf(SubStateEnum.col_pending_read.getKey()).equals(subState)) {
                            List<Integer> subStateList = new ArrayList<Integer>();
                            subStateList.add(SubStateEnum.col_pending_read.getKey());
                            subStateList.add(SubStateEnum.col_pending_specialBack.getKey());
                            subStateList.add(SubStateEnum.col_pending_specialBackToSenderReGo.getKey());
                            hql.append(" (affair.subState in (:subStateList)) ");
                            parameter.put("subStateList", subStateList);
                        } else {
                            String[] subStates = subState.split("[,]");
                            List<Integer> subStateList = new ArrayList<Integer>();
                            for (String sState : subStates) {
                                subStateList.add(Integer.parseInt(sState));
                            }
                            if (subStateList.contains(SubStateEnum.col_waitSend_sendBack.getKey())
                                    || subStateList.contains(SubStateEnum.col_waitSend_stepBack.getKey())) {
                                hql.append(" (affair.subState in (:subStateList) or affair.backFromId is not null ) ");
                                parameter.put("subStateList", subStateList);
                            } else {
                                hql.append(" (affair.subState in (:subStateList)) ");
                                parameter.put("subStateList", subStateList);
                            }
                        }
                        break;
                    case applicationEnum:
                        if (Strings.isNotBlank(hql.toString())) {
                            hql.append(" and ");
                        }
                        String[] apps = textField.split(",");
                        List<Integer> appList = new ArrayList<Integer>();
                        Map<Integer, List<Integer>> app2SubApp = new HashMap<Integer, List<Integer>>();
                        for (String app : apps) {
                            if (ApplicationCategoryEnum.exchange.key() == Integer.parseInt(app)) {
                                appList.add(ApplicationCategoryEnum.exSend.key());
                                appList.add(ApplicationCategoryEnum.exSign.key());
                                appList.add(ApplicationCategoryEnum.edocRegister.key());
                            } else if (ApplicationCategoryEnum.inquiry.key() == Integer.parseInt(app)) {
                                int subState1 = ApplicationSubCategoryEnum.inquiry_audit.key();
                                //当按应用类型查询调查的时候只查询带填写的调查
                                if (apps.length == 1) {
                                    subState1 = ApplicationSubCategoryEnum.inquiry_write.key();
                                }
                                Strings.addToMap(app2SubApp, ApplicationCategoryEnum.inquiry.key(), subState1);

                            } else if (ApplicationCategoryEnum.news.key() == Integer.parseInt(app)) {
                                //为新闻的时候为综合信息审批，包含待审批的调查，不包括待填写的。
                                appList.add(ApplicationCategoryEnum.bulletin.key());
                                appList.add(ApplicationCategoryEnum.news.key());

                                Strings.addToMap(app2SubApp, ApplicationCategoryEnum.inquiry.key(), ApplicationSubCategoryEnum.inquiry_audit.key());

                            } else {
                                appList.add(Integer.parseInt(app));
                            }
                        }
                        if (appList.size() > 1) {
                            hql.append(" (affair.app in (:appList)) ");
                            parameter.put("appList", appList);
                        } else if (appList.size() == 1) {
                            hql.append(" (affair.app = (:app)) ");
                            parameter.put("app", appList.get(0));
                        }
                        //更具app2SubApp来判断。
                        if (!app2SubApp.isEmpty()) {
                            int k = 0;
                            for (Iterator<Map.Entry<Integer, List<Integer>>> iterator = app2SubApp.entrySet().iterator(); iterator.hasNext(); ) {
                                Map.Entry<Integer, List<Integer>> entry = iterator.next();
                                if (!entry.getValue().isEmpty()) {
                                    hql.append(" and ( affair.app = :(app" + k + ") and affair.subApp in (:subApp" + k + ")");
                                    parameter.put("app" + k, entry.getKey());
                                    parameter.put("subApp" + k, entry.getValue());
                                    k++;
                                }
                            }
                        }
                        break;
                    case expectedProcessTime:
                        if (Strings.isNotBlank(hql.toString())) {
                            hql.append(" and ");
                        }
                        String arr2[] = textField.split("#");
                        textField = arr2[0];
                        if (Strings.isNotBlank(textField)) {
                            textField = Datetimes.getServiceFirstTime(textField);
                            Date expectedProcessTime = Datetimes.parseNoTimeZone(textField, null);
                            hql.append(" (affair.expectedProcessTime >= :expectedProcessTime1) ");
                            parameter.put("expectedProcessTime1", expectedProcessTime);
                        }
                        if (arr2.length > 1) {
                            String textField1 = arr2[1];
                            if (Strings.isNotBlank(textField) && Strings.isNotBlank(textField1)) {
                                hql.append(" and ");
                            }
                            if (Strings.isNotBlank(textField1)) {
                                textField1 = Datetimes.getServiceLastTime(textField1);
                                Date expectedProcessTime = Datetimes.parseNoTimeZone(textField1, "yyyy-MM-dd HH:mm:ss");
                                hql.append(" (affair.expectedProcessTime <= :expectedProcessTime2) ");
                                parameter.put("expectedProcessTime2", expectedProcessTime);
                            }
                        }
                        break;
                }

            }
        }

        return hql;

    }

    @Override
    public void updateAISortValue(Map<String, String> map) {
        try {
            String openFrom = map.get("openFrom");
            String source = map.get("source");
            String aiSortValue = map.get("aiSortValue");
            if (("pendingCenter".equals(openFrom) || "listPending".equals(openFrom)) &&
                    ("Staff".equals(source) || "Template".equals(source) || "listPending".equals(source)) ||
                    ("Common".equals(source) && "All".equals(map.get("section"))) || ("Common".equals(source) && "Agent".equals(map.get("section")))
                    || "pendingWork".equals(openFrom)) {
                Map<String, String> aiSortMap = new HashMap<String, String>();
                if ("Template".equals(source)) {
                    aiSortMap.put("template", aiSortValue);
                } else if ("Staff".equals(source)) {
                    aiSortMap.put("staff", aiSortValue);
                } else if ("listPending".equals(source)) {
                    aiSortMap.put("listPending", aiSortValue);
                } else if ("Common".equals(source) && "All".equals(map.get("section"))) {
                    aiSortMap.put("sectionAll", aiSortValue);
                } else if ("Common".equals(source) && "Agent".equals(map.get("section"))) {
                    aiSortMap.put("sectionAgent", aiSortValue);
                } else if ("pendingWork".equals(openFrom)) {
                    aiSortMap.put("pendingWork", aiSortValue);
                }
                customizeManager.saveOrUpdateCustomize(AppContext.currentUserId(), CustomizeConstants.PENDING_CENTER_AI_SORT, JSONUtil.toJSONString(aiSortMap));
            } else {
                String spaceId = map.get("spaceId");
                String fragmentId = map.get("fragmentId");
                String ordinal = map.get("ordinal");
                String x = map.get("x");
                String y = map.get("y");
                if (!Strings.isBlank(spaceId)) {
                    portalApi.updatePortletProperty(AppContext.getCurrentUser(), spaceId, fragmentId, ordinal, "".equals(x) ? "0" : x, "".equals(y) ? "0" : y, "aiSortValue", aiSortValue);
                }
            }
        } catch (BusinessException e) {
            log.error("更新智能排序开关状态异常：", e);
        }

    }

    @Override
    @AjaxAccess
    public List<TemplateTreeVo> getTreeData4PendingTemplate(Map<String, String> parm) throws BusinessException {
        String memberIdStr = parm.get("memberId");
        if (Strings.isBlank(memberIdStr)) {
            return new ArrayList<TemplateTreeVo>();
        }

        Long memberId = Long.valueOf(memberIdStr);
        //获取模板对应的待办数
        Map<Long, Integer> templateDetailCount = this.getPendingTemplateDetailCount(memberId);
        List<TemplateTreeVo> listTreeVo = new ArrayList<TemplateTreeVo>();
        //通过模板ID构建模板树
        List<Long> templateIdList = new ArrayList<Long>();
        if (templateDetailCount != null && !templateDetailCount.isEmpty()) {
            Set<Map.Entry<Long, Integer>> keys = templateDetailCount.entrySet();
            for (Map.Entry<Long, Integer> entry : keys) {
                if (!templateIdList.contains(entry.getKey())) {
                    templateIdList.add(entry.getKey());
                }
            }
            Map<Long, CtpTemplate> templates = pendingManager.getTemplateById(templateIdList);
            Map<Long, TemplateTreeVo> treeMap = new HashMap<Long, TemplateTreeVo>();
            List<TemplateTreeVo> templateTreeList = new ArrayList<TemplateTreeVo>();
            List<TemplateTreeVo> recentTreeList = new ArrayList<TemplateTreeVo>();
            Long currentAccountId = AppContext.currentAccountId();
            for (Long templateId : templateIdList) {
                CtpTemplate thisTemplate = templates.get(templateId);
                if (thisTemplate == null) {
                    continue;
                }
                if (thisTemplate.getModuleType() != null && (thisTemplate.getModuleType() == ModuleType.edocSend.getKey()
                        || thisTemplate.getModuleType() == ModuleType.edocRec.getKey()
                        || thisTemplate.getModuleType() == ModuleType.edocSign.getKey()
                        || thisTemplate.getModuleType() == ModuleType.govdocExchange.getKey())) {
                    // 公文模板不查询老公文及交换签收模板
                    continue;
                }
                CtpTemplateCategory category = templateCategoryManager.get(thisTemplate.getCategoryId());
                Integer affairCount = templateDetailCount.get(templateId);
                String templateName = thisTemplate.getSubject();
                if (null != thisTemplate.getOrgAccountId() && !thisTemplate.getOrgAccountId().equals(currentAccountId)) {
                    templateName += "（" + orgManager.getAccountById(thisTemplate.getOrgAccountId()).getName() + "）";
                }
                TemplateTreeVo templateTree = new TemplateTreeVo(templateId, templateName, "template", thisTemplate.getCategoryId(), "", affairCount);
                templateTreeList.add(templateTree);
                if (category != null) {
                    //构造模板分类树
                    templateManager.createCategoryParentTreeVO(category, affairCount, treeMap);
                }
            }

            //最近处理模板
            List<CtpAffair> recentAffair = this.getRecentTemplate(memberId);
            if (recentAffair != null && !recentAffair.isEmpty()) {
                String name = ResourceUtil.getString("template.choose.category.recent.deal.label") + "(" + recentAffair.size() + ")";
                Long recentDealId = Long.valueOf(TemplateCategoryConstant.recentDeal.getKey());
                TemplateTreeVo treeVO = new TemplateTreeVo(recentDealId, name, "category", recentDealId, "");
                listTreeVo.add(treeVO);
                List<Long> recentTemplateId = new ArrayList<Long>();
                for (CtpAffair affair : recentAffair) {
                    Long templateId = affair.getTempleteId();
                    if (!recentTemplateId.contains(templateId)) {
                        CtpTemplate thisTemplate = templates.get(templateId);
                        Integer affairCount = templateDetailCount.get(templateId);
                        String templateName = thisTemplate.getSubject();
                        if (!thisTemplate.getOrgAccountId().equals(currentAccountId)) {
                            templateName += "（" + orgManager.getAccountById(thisTemplate.getOrgAccountId()).getName() + "）";
                        }
                        TemplateTreeVo templateTree = new TemplateTreeVo(templateId, templateName, "template", recentDealId, "", affairCount);
                        recentTreeList.add(templateTree);
                        recentTemplateId.add(templateId);
                    }
                }
            }
            if (treeMap != null && !treeMap.isEmpty()) {
                Set<Map.Entry<Long, TemplateTreeVo>> key = treeMap.entrySet();

                Map<String, Long> categoryName = new HashMap<String, Long>();
                List<Long> returnIdList = new ArrayList<Long>();
                //模板分类同名称的合并
                for (Map.Entry<Long, TemplateTreeVo> entry : key) {
                    String name = entry.getValue().getName();
                    if (categoryName.get(name) != null && "category".equals(entry.getValue().getType())
                            && !entry.getValue().getId().equals(Long.valueOf(TemplateCategoryConstant.recentDeal.getKey()))) {
                        TemplateTreeVo treeVo = treeMap.get(categoryName.get(name));//留下的分类
                        TemplateTreeVo removeTreeVo = entry.getValue();//合并掉的分类
                        treeVo.setAffairCount(treeVo.getAffairCount() + removeTreeVo.getAffairCount());
                        treeMap.put(treeVo.getId(), treeVo);
                        //合并之后的分类需要修改下面的具体模板的PId
                        Long pId = treeVo.getId();//留下的分类Id
                        for (TemplateTreeVo templateTree : templateTreeList) {
                            if (templateTree.getpId() != null && templateTree.getpId().equals(removeTreeVo.getId())) {
                                templateTree.setpId(pId);
                            }
                        }
                        //合并之后的分类修改下面的分类PId
                        for (Map.Entry<Long, TemplateTreeVo> category : key) {
                            TemplateTreeVo catagoryValue = category.getValue();
                            if (catagoryValue.getpId() != null && catagoryValue.getpId().equals(removeTreeVo.getId())) {
                                catagoryValue.setpId(pId);
                            }
                        }
                    } else {
                        Long id = entry.getValue().getId();
                        returnIdList.add(id);
                        categoryName.put(name, id);
                    }
                }
                //组装返回值
                for (Map.Entry<Long, TemplateTreeVo> entry : key) {
                    if (returnIdList.contains(entry.getValue().getId())) {
                        entry.getValue().setName(entry.getValue().getName() + "(" + entry.getValue().getAffairCount() + ")");
                        listTreeVo.add(entry.getValue());
                    }
                }
                if (templateTreeList != null && !templateTreeList.isEmpty()) {
                    for (TemplateTreeVo tree : templateTreeList) {
                        tree.setName(tree.getName() + "(" + tree.getAffairCount() + ")");
                        listTreeVo.add(tree);
                    }
                }
                if (recentTreeList != null && !recentTreeList.isEmpty()) {
                    for (TemplateTreeVo tree : recentTreeList) {
                        tree.setName(tree.getName() + "(" + tree.getAffairCount() + ")");
                        listTreeVo.add(tree);
                    }
                }
            }
            return listTreeVo;
        } else {
            return new ArrayList<TemplateTreeVo>();
        }
    }

    @Override
    @AjaxAccess
    public List<OrgDepartmentTreeVo> getTreeData4PendingStaff(Map<String, Object> parm) throws BusinessException {
        String memberIdStr = (String) parm.get("memberId");
        Integer allCount = (Integer) parm.get("allCount");
        if (allCount == null) {
            allCount = 0;
        }
        if (Strings.isBlank(memberIdStr)) {
            return Collections.emptyList();
        }

        Long memberId = Long.valueOf(memberIdStr);
        //获取待办中
        Map<Long, Integer> staffDetailCount = pendingManager.getPendingStaffDetailCount(memberId);
        List<Long> memberIdList = new ArrayList<Long>();

        if (staffDetailCount != null && !staffDetailCount.isEmpty()) {
            Set<Map.Entry<Long, Integer>> keys = staffDetailCount.entrySet();
            for (Map.Entry<Long, Integer> entry : keys) {
                if (!memberIdList.contains(entry.getKey())) {
                    memberIdList.add(entry.getKey());
                }
            }
            //构建组织树
            Map<Long, OrgDepartmentTreeVo> treeMap = new HashMap<Long, OrgDepartmentTreeVo>();
            List<OrgDepartmentTreeVo> memberTreeList = new ArrayList<OrgDepartmentTreeVo>();
            for (Long id : memberIdList) {
                Integer affairCount = staffDetailCount.get(id);
                if (null != id) {
                    V3xOrgMember member = orgManager.getMemberById(id);
                    this.createDepartmentParentTreeVO(treeMap, member, affairCount, allCount, memberTreeList);
                }
            }


            // 排序号，按照升序排序
            Comparator<OrgDepartmentTreeVo> comparator = new Comparator<OrgDepartmentTreeVo>() {

                @Override
                public int compare(OrgDepartmentTreeVo o1, OrgDepartmentTreeVo o2) {

                    Long sort1 = o1.getSortId();
                    Long sort2 = o2.getSortId();

                    if (sort1 == null && sort2 == null) {
                        return 0;
                    }

                    if (sort1 == null) {
                        return -1;
                    }

                    if (sort2 == null) {
                        return 1;
                    }

                    if (sort1 > sort2) {
                        return 1;
                    } else if (sort1 < sort2) {
                        return -1;
                    }

                    return 0;
                }
            };

            List<OrgDepartmentTreeVo> listTreeVo = new ArrayList<OrgDepartmentTreeVo>(memberTreeList.size() * 2);

            if (treeMap != null && !treeMap.isEmpty()) {
                Set<Map.Entry<Long, OrgDepartmentTreeVo>> key = treeMap.entrySet();
                for (Map.Entry<Long, OrgDepartmentTreeVo> entry : key) {
                    entry.getValue().setName(entry.getValue().getName() + "(" + entry.getValue().getAffairCount() + ")");
                    listTreeVo.add(entry.getValue());
                }
            }


            // 按照组织模型排序
            Collections.sort(listTreeVo, comparator);


            if (!memberTreeList.isEmpty()) {

                // 按照组织模型排序
                Collections.sort(memberTreeList, comparator);

                for (OrgDepartmentTreeVo memberTree : memberTreeList) {
                    if (treeMap.get(memberTree.getpId()) == null) {//没有有效部门则Pid为单位或者集团
                        Long pId = orgManager.getRootAccount().getId();
                        memberTree.setpId(pId);

                    }
                    memberTree.setName(memberTree.getName() + "(" + memberTree.getAffairCount() + ")");
                    listTreeVo.add(memberTree);
                }
            }

            // 排序


            return listTreeVo;
        } else {
            //return new ArrayList<OrgDepartmentTreeVo>();
            return Collections.emptyList();
        }
    }

    @Override
    @AjaxAccess
    public Map<String, Object> getTreeData4PendingCommon(Map<String, String> parm) throws BusinessException {
        String memberIdStr = parm.get("memberId");
        String spaceId = parm.get("spaceId");
        if (Strings.isBlank(memberIdStr) || Strings.isBlank(spaceId)) {
            return new HashMap<String, Object>();
        }
        Map<String, Object> returnMap = new HashMap<String, Object>();

        Long memberId = Long.valueOf(memberIdStr);
        //栏目待办数量
        Map<String, Integer> pendingCountCacheMaps = pendingSection.getPendingCountMaps();

        //空间配置的所有待办类栏目集合
        List<Map<String, String>> spacePendingPortal = portalApi.getPortalPagePortletPorperties(spaceId, "pendingSection");
        //是否配置了来源为全部待办的栏目
        boolean hasAllSection = false;
        //用户配置的所有待办类
        List<Map<String, String>> returnList = new ArrayList<Map<String, String>>();
        for (Map<String, String> spacePending : spacePendingPortal) {
            //没有值和值为all都代表全部
            if ("all".equals(spacePending.get("panel")) || "".equals(spacePending.get("panel")) || null == spacePending.get("panel")) {
                hasAllSection = true;
            }
            int count = pendingCountCacheMaps.get(spacePending.get("uuid")) == null ? 0 : pendingCountCacheMaps.get(spacePending.get("uuid"));
            Map<String, String> detail = new HashMap<String, String>();
            detail.put("id", spacePending.get("uuid"));
            detail.put("name", spacePending.get("columnsName"));
            detail.put("count", String.valueOf(count));
            detail.put("x", spacePending.get("x"));
            detail.put("y", spacePending.get("y"));
            returnList.add(detail);
        }
        returnMap.put("spacePendingPortal", returnList);

        if (!hasAllSection) {//默认追加全部待办
            returnMap.put("showAllSection", true);
            //获取全部待办总数
            Map<String, String> allPreference = new HashMap<String, String>();
            allPreference.put("panel", "all");
            int allCount = pendingManager.getPendingCount(memberId, allPreference, false);

            returnMap.put("allCount", allCount);
        }

        //代理栏目逻辑处理
        boolean hasAgentSection = false;
        boolean isAgent = false;
        if (Strings.isNotEmpty(MemberAgentBean.getInstance().getAgentModelList(memberId)) || Strings.isNotEmpty(MemberAgentBean.getInstance().getAgentModelToList(memberId))) {
            isAgent = true;
        }
        //配置的代理栏目
        List<Map<String, String>> agentPortal = portalApi.getPortalPagePortletPorperties(spaceId, "agentSection");
        Map<String, Integer> agentCountCacheMaps = new HashMap<String, Integer>();
        if (Strings.isNotEmpty(agentPortal)) {
            hasAgentSection = true;
            //代理栏目待办数量
            agentCountCacheMaps = agentSection.getAgentCountMaps();
        }
        if (!hasAgentSection && isAgent) {//没有代理栏目并且存在代理关系，前端追加
            //代理事项的数量
            int agentCount = affairManager.getAgentPendingCount(AppContext.currentUserId());
            returnMap.put("agentCount", agentCount);
            returnMap.put("showAgentSection", true);
        } else if (hasAgentSection) { //设置了代理栏目
            //封装返回值
            List<Map<String, String>> spaceAgentList = new ArrayList<Map<String, String>>();
            for (Map<String, String> spaceAgent : agentPortal) {
                Map<String, String> detail = new HashMap<String, String>();
                detail.put("id", spaceAgent.get("uuid"));
                detail.put("name", spaceAgent.get("columnsName"));
                detail.put("count", String.valueOf(agentCountCacheMaps.get(spaceAgent.get("uuid"))));
                detail.put("x", spaceAgent.get("x"));
                detail.put("y", spaceAgent.get("y"));
                spaceAgentList.add(detail);
            }
            returnMap.put("spaceAgentList", spaceAgentList);
        }

        return returnMap;
    }


    @Override
    public List<CtpAffair> querySectionAffair(AffairCondition affairCondition, FlipInfo fi, Map<String, String> preference, String openFrom, Map<String, String> query, boolean isAll) throws BusinessException {
        Long memberId = AppContext.getCurrentUser().getId();
        affairCondition.setMemberId(memberId);

        String panel = "all";
        String panelString = preference.get("panel");
        if (Strings.isNotBlank(panelString)) {
            panel = panelString;
        }

        boolean isListDone = false;
        boolean isListSent = false;
        boolean isListWaitSend = false;

        if (ColOpenFrom.listDone.name().equals(openFrom)) {
            isListDone = true;
        } else if (ColOpenFrom.listSent.name().equals(openFrom)) {
            isListSent = true;
        } else if (ColOpenFrom.listWaitSend.name().equals(openFrom)) {
            isListWaitSend = true;
        }

        boolean isGroupBy = false;
        boolean isProtal = false;
        // 数据显示
        String dateList = preference.get("dateList");
        //判断是否选择‘同一流程只显示最后一条 ’
        if (Strings.isNotBlank(dateList)) {
            isGroupBy = true;
            isProtal = true;
        }

        if (query.containsKey("condition")) {
            String conditions = query.get("condition");
            String conditionValue = query.get("conditionValue");

            String textField1 = "";
            String textField2 = "";
            if (Strings.isNotBlank(conditions) && Strings.isNotBlank(conditionValue)) {
                if (AffairCondition.SearchCondition.createDate.name().equals(conditions)) {
                    String[] values = conditionValue.split("#");
                    //对时间进行特殊处理，当开始日期时，将时间置为最小，当是结束时间时，将时间置为最大
                    textField1 = Datetimes.getServiceFirstTime(values[0]);
                    textField2 = Datetimes.getServiceLastTime(values[1]);
                } else {
                    textField1 = conditionValue;
                }
                SearchCondition con = AffairCondition.SearchCondition.valueOf(conditions);
                if (con != null) {
                    affairCondition.addSearch(con, textField1, textField2, true);
                }
            }
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
                        if (isListDone) {
                            //已办 重要程度，排除会议
                            affairCondition.addSearch(SearchCondition.catagory, "done_catagory_all", null);
                        }
                    } else if ("templete_pending".equals(panel)) {
                        affairCondition.addSearch(SearchCondition.templete, tempStr, null);
                    } else if ("Policy".equals(panel)) {
                        affairCondition.addSearch(SearchCondition.policy4Portal, tempStr, null);
                    } else if ("sender".equals(panel) && isListDone) {//已办栏目才能根据发起人查询
                        affairCondition = new AffairCondition(memberId, StateEnum.col_done,
                                ApplicationCategoryEnum.collaboration,
                                ApplicationCategoryEnum.edoc,
                                ApplicationCategoryEnum.meeting);
                        if (!isAll) {
                            int pageSize = 8;
                            String countStr = preference.get("count");
                            if (Strings.isNotBlank(countStr)) {
                                pageSize = Integer.parseInt(countStr);//设置行数
                            } else if (null != fi) {
                                pageSize = fi.getSize();
                            }
                            String columnStyle = preference.get("columnStyle");

                            if ("doubleList".equals(columnStyle)) {
                                pageSize = pageSize * 2;
                            }
                            fi = new FlipInfo();
                            fi.setNeedTotal(false);
                            fi.setPage(1);
                            fi.setSize(pageSize);
                        }
                        List<Integer> appEnum = new ArrayList<Integer>();
                        //查询指定发起人
                        return (List<CtpAffair>) affairManager.getAffairListBySender(memberId, tempStr, affairCondition, false, fi, appEnum, isGroupBy);
                    }
                } else {
                    return new ArrayList<CtpAffair>();
                }
            }
        } else {
            if (isListDone) {
                //'全部'，但要排除掉会议 详见：AffaiCondition的initSearch()
                affairCondition.addSearch(SearchCondition.catagory, "done_catagory_all", null);
            } else if (isListSent) {
                affairCondition.addSearch(SearchCondition.catagory, "sent_catagory_all", null);
            } else if (isListWaitSend) {
                affairCondition.addSearch(SearchCondition.catagory, "waitsend_catagory_all", null);
            }

        }


        List<CtpAffair> affairs = null;
        if (isGroupBy) {
            affairs = (List<CtpAffair>) affairManager.getDeduplicationAffairs(memberId, affairCondition, false, fi);
        } else {
            if (isListDone) {
                affairs = affairCondition.getSectionAffair(affairManager, StateEnum.col_done.key(), fi, isGroupBy, isProtal);
            } else if (isListSent) {
                affairs = affairCondition.getSectionAffair(affairManager, StateEnum.col_sent.key(), fi);
            } else if (isListWaitSend) {
                affairs = affairCondition.getSectionAffair(affairManager, StateEnum.col_waitSend.key(), fi);
            }
        }
        if (null == affairs) {
            affairs = new ArrayList<CtpAffair>();
        }

        return affairs;
    }

    @Override
    public List<CtpAffair> querySectionAffair(FlipInfo fi, Map<String, String> map) throws BusinessException {
        AffairCondition condition = new AffairCondition();
        if (map.containsKey("createDate")) {
            String textField = (String) map.get("createDate");
            if (Strings.isNotBlank(textField)) {
                String textField1 = "";
                SearchCondition con = AffairCondition.SearchCondition.valueOf("createDate");
                if (con != null) {
                    String arr[] = textField.split("#");
                    textField = arr[0];
                    if (Strings.isNotBlank(textField)) {
                        textField = Datetimes.getServiceFirstTime(textField);
                    }
                    if (arr.length > 1) {
                        textField1 = arr[1];
                        if (Strings.isNotBlank(textField1)) {
                            textField1 = Datetimes.getServiceLastTime(textField1);
                        }
                    }
                    condition.addSearch(con, textField, textField1, true);
                }
            }
        }
        if (map.containsKey("subject")) {
            String textField = (String) map.get("subject");
            if (Strings.isNotBlank(textField)) {
                String textField1 = "";
                SearchCondition con = AffairCondition.SearchCondition.valueOf("subject");
                if (con != null) {
                    condition.addSearch(con, textField, textField1, true);
                }
            }
        }
        if (map.containsKey("startMemberName")) {
            String textField = (String) map.get("startMemberName");
            if (Strings.isNotBlank(textField)) {
                String textField1 = "";
                SearchCondition con = AffairCondition.SearchCondition.valueOf("sender");
                if (con != null) {
                    condition.addSearch(con, textField, textField1, true);
                }
            }
        }

        List<CtpAffair> affairList = this.querySectionAffair(condition, fi, map, (String) map.get("openFrom"), new HashMap<String, String>(), true);
        List<CtpAffair> affairListClone = new ArrayList<CtpAffair>();
        CtpAffair ctpAffairORG = null;
        CtpAffair ctpAffairclo = null;
        for (int a = 0; a < affairList.size(); a++) {
            ctpAffairORG = affairList.get(a);
            try {
                ctpAffairclo = (CtpAffair) ctpAffairORG.clone();
                ctpAffairclo.setId(ctpAffairORG.getId());
            } catch (CloneNotSupportedException e) {
            }
            affairListClone.add(ctpAffairclo);
        }
        String rowStr = "subject,createDate,receiveTime,sendUser,deadline,category";
        if (map.containsKey("rowList")) {
            rowStr = map.get("rowList");
        }
        List<PendingRow> voList = affairList2PendingRowList(affairListClone, AppContext.getCurrentUser(), null, false, rowStr, 0, null);
        fi.setData(voList);
        return affairList;
    }

    public void setCustomizeManager(CustomizeManager customizeManager) {
        this.customizeManager = customizeManager;
    }

}
