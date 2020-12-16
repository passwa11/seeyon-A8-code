package com.seeyon.ctp.portal.section;

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

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.utils.AgentUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.bo.AffairCondition;
import com.seeyon.ctp.common.affair.bo.PendingRow;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.manager.PendingManager;
import com.seeyon.ctp.common.affair.vo.PendingRowComparator;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.cache.CacheAccessable;
import com.seeyon.ctp.common.cache.CacheFactory;
import com.seeyon.ctp.common.cache.CacheMap;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete;
import com.seeyon.ctp.portal.section.templete.MultiRowVariableColumnColTemplete;
import com.seeyon.ctp.portal.section.util.SectionUtils;
import com.seeyon.ctp.portal.util.PortletPropertyContants.PropertyName;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;

public class AgentSection extends BaseSectionImpl {

    private static Log log = LogFactory.getLog(AgentSection.class);
    private AffairManager affairManager;
    private CacheMap<String, Integer> agentCountCacheMaps;
    private PendingManager pendingManager;
    private ColManager colManager;
    private OrgManager orgManager;

    public ColManager getColManager() {
        return colManager;
    }

    public void setColManager(ColManager colManager) {
        this.colManager = colManager;
    }

    public OrgManager getOrgManager() {
        return orgManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
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
        if (agentCountCacheMaps != null) {
            log.info("已经初始化代理待办数量，不再初始化");
        } else {
            log.info("初始化代理待办栏目数量");

            CacheAccessable cacheFactory = CacheFactory.getInstance(AgentSection.class);
            agentCountCacheMaps = cacheFactory.createLinkedMap("agentCountCacheMaps");
        }
    }

    @Override
    public String getId() {
        return "agentSection";
    }

    @Override
    public boolean isAllowUsed() {
        if (AppContext.isGroupAdmin()) {
            return false;
        }
        return true;
    }

    @Override
    public String getBaseName() {
        return ResourceUtil.getString("common.my.agent.label");
    }

    @Override
    public String getBaseNameI18nKey() {
        return "common.my.agent.label";
    }

    @Override
    public String getName(Map<String, String> preference) {
        return SectionUtils.getSectionName(this.getBaseName(), preference);
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public Integer getTotal(Map<String, String> preference) {
        Long fragmentId = Long.parseLong(preference.get(PropertyName.entityId.name()));
        String ordinal = preference.get(PropertyName.ordinal.name());

        int c = 0;
        try {
            c = affairManager.getAgentPendingCount(AppContext.currentUserId());
            //将总数缓存
            String cacheKey = fragmentId + "_" + ordinal;
            if (agentCountCacheMaps == null) {
                CacheAccessable cacheFactory = CacheFactory.getInstance(AgentSection.class);
                agentCountCacheMaps = cacheFactory.createLinkedMap("agentCountCacheMaps");
            }
            agentCountCacheMaps.put(cacheKey, c);
        } catch (Exception e) {
            log.error("", e);
        }
        return c;
    }

    public Map<String, Integer> getAgentCountMaps() {
        return agentCountCacheMaps.toMap();
    }

    @Override
    public BaseSectionTemplete projection(Map<String, String> preference) {
        MultiRowVariableColumnColTemplete c = new MultiRowVariableColumnColTemplete();
        User user = AppContext.getCurrentUser();
        Long memberId = user.getId();

        String fragmentId = preference.get(PropertyName.entityId.name());
        String ordinal = preference.get(PropertyName.ordinal.name());
        //显示列
        String rowStr = preference.get("rowList");
        if (Strings.isBlank(rowStr)) {
            rowStr = "subject,receiveTime,sendUser,category";
        }

        int pageSize = c.getPageSize(preference)[0];

        List<PendingRow> rowList = new ArrayList<PendingRow>();
        try {
            List<CtpAffair> affairs = new ArrayList<CtpAffair>();
            AffairCondition condition = new AffairCondition(memberId, StateEnum.col_pending);
            Object[] agentObj = AgentUtil.getUserAgentToMap(memberId);
            boolean agentToFlag = (Boolean) agentObj[0];
            Map<Integer, List<AgentModel>> map = (Map<Integer, List<AgentModel>>) agentObj[1];
            condition.setAgent(agentToFlag, map);

            FlipInfo fi = new FlipInfo();
            fi.setNeedTotal(false);
            fi.setPage(1);
            fi.setSize(pageSize);
            fi.setSortField("receiveTime");
            fi.setSortOrder("desc");

            affairs = condition.getAgentPendingAffair(affairManager, fi);
            //【恩华药业】zhou:协同过滤掉设定范围内的数据【开始】
            List<CtpAffair> newAffairs = new ArrayList<>();
            AccessSetingManager manager = new AccessSetingManagerImpl();
            for (CtpAffair affair : affairs) {
                if (affair.getApp() == 1) {
                    Long transactorId = affair.getMemberId();
                    V3xOrgMember member = null;
                    try {
                        member = orgManager.getMemberById(transactorId);
                    } catch (BusinessException e) {
                        e.printStackTrace();
                    }
                    if(null != member){
                        Long userId = member.getId();
                        Map<String, Object> map2 = new HashMap<>();
                        map2.put("memberId", userId);
                        List<DepartmentViewTimeRange> list = manager.getDepartmentViewTimeRange(map2);
                        if (list.size() > 0) {
                            DepartmentViewTimeRange range = list.get(0);
                            if (!"".equals(range.getDayNum()) && null != range.getDayNum() && Long.parseLong(range.getDayNum()) > 0l) {
                                LocalDateTime end = LocalDateTime.now();
                                LocalDateTime start = LocalDateTime.now().minusDays(Long.parseLong(range.getDayNum()));
                                Long startTime = start.toInstant(ZoneOffset.of("+8")).toEpochMilli();
                                Long endTime = end.toInstant(ZoneOffset.of("+8")).toEpochMilli();
                                Long objectId = affair.getObjectId();
                                ColSummary colSummary = null;
                                try {
                                    colSummary = colManager.getColSummaryById(objectId);
                                } catch (BusinessException e) {
                                    e.printStackTrace();
                                }
                                Date createDate = colSummary.getCreateDate();
                                if (startTime.longValue() != 0l && endTime.longValue() != 0l) {
                                    if (createDate.getTime() > startTime.longValue() && createDate.getTime() < endTime.longValue()) {
                                        newAffairs.add(affair);
                                    }
                                }
                            } else if (!"".equals(range.getDayNum()) && null != range.getDayNum() && Long.parseLong(range.getDayNum()) == 0l) {
                            } else {
                                newAffairs.add(affair);
                            }
                        } else {
                            newAffairs.add(affair);
                        }
                    }

                } else {
                    newAffairs.add(affair);
                }
            }

            //【恩华药业】zhou:协同过滤掉设定范围内的数据【结束】
            rowList = pendingManager.affairList2PendingRowList(newAffairs, user, "agent", true, rowStr, StateEnum.col_pending.key());
            // 置顶排序一下
            Collections.sort(rowList, new PendingRowComparator());

        } catch (BusinessException e) {
            log.error("", e);
        }
        c = PendingSectionUrlUtil.getTemplete(c, rowList, preference);
        c.setDataNum(pageSize);
        c.addBottomButton(BaseSectionTemplete.BOTTOM_BUTTON_LABEL_MORE, "/collaboration/pending.do?method=morePendingCenter&from=Agent&fragmentId=" + fragmentId + "&ordinal=" + ordinal + "&rowStr=" + rowStr + "&source=Common&section=All&spaceId=" + preference.get(PropertyName.spaceId.name()), null,
                "sectionMoreIco");
        return c;
    }

    @Override
    public boolean isShowTotal() {
        return true;
    }

    @Override
    public String getResolveFunction(Map<String, String> preference) {
        return MultiRowVariableColumnColTemplete.RESOLVE_FUNCTION;
    }

}
