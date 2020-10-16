package com.seeyon.ctp.portal.section;

import com.seeyon.apps.collaboration.manager.PendingManager;
import com.seeyon.apps.collaboration.vo.PendingRowComparator;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.portal.manager.PortalCacheManager;
import com.seeyon.ctp.portal.po.PortalSpaceFix;
import com.seeyon.ctp.portal.section.*;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete;
import com.seeyon.ctp.portal.section.templete.MultiRowVariableColumnColTemplete;
import com.seeyon.ctp.portal.section.templete.mobile.MListTemplete;
import com.seeyon.ctp.portal.section.util.SectionUtils;
import com.seeyon.ctp.portal.util.PortletPropertyContants.PropertyName;
import com.seeyon.ctp.util.Strings;
import org.apache.commons.logging.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PendingSection extends BaseSectionImpl {
	private static Log LOG = CtpLogFactory.getLog(com.seeyon.ctp.portal.section.PendingSection.class);
	private PendingManager pendingManager;
	private Map<String,Integer> pendingCountCacheMaps = new ConcurrentHashMap<String,Integer>();
	private PortalCacheManager portalCacheManager;

    public void setPendingManager(PendingManager pendingManager) {
        this.pendingManager = pendingManager;
    }

    @Override
    public void init() {
    	super.init();
    	/*if(pendingCountCacheMaps != null){
            LOG.info("已经初始化待办数量，不再初始化");
        } else {
        	LOG.info("初始化待办栏目数量");

        	CacheAccessable cacheFactory = CacheFactory.getInstance(PendingSection.class);
        	pendingCountCacheMaps = cacheFactory.createLinkedMap("pendingCountCacheMaps");
        }*/


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
		return "pending";
	}

	@Override
	public String getId() {
		return "pendingSection";
	}

	@Override
    public boolean isAllowUsed() {
//        zhou 去除角色验证
//        if (AppContext.isGroupAdmin()) {
//            return false;
//        }

        return true;
    }

	@Override
    public String getBaseName(Map<String, String> preference) {
		String name = "";
		if (preference != null) {
			name = preference.get("baseName");
		}
		if(Strings.isBlank(name)&&(preference!=null)){
		    name=preference.get("columnsName");
        }
        if(Strings.isBlank(name)){
            name = ResourceUtil.getString("common.my.pending.title");
        }
        return name;
    }

    @Override
	public String getName(Map<String, String> preference) {
		//栏目显示的名字，必须实现国际化，在栏目属性的“columnsName”中存储
        String name = preference.get("columnsName");
        if(Strings.isBlank(name)){
            return ResourceUtil.getString("common.my.pending.title");//待辦事项
        }else{
            return name;
        }
	}

	@Override
	public Integer getTotal(Map<String, String> preference) {
        Long memberId = AppContext.currentUserId();
        Long fragmentId = Long.parseLong(preference.get(PropertyName.entityId.name()));
        String ordinal = preference.get(PropertyName.ordinal.name());

        Integer total =  this.pendingManager.getPendingCount(memberId, fragmentId, ordinal);
        //将总数缓存
        String cacheKey = fragmentId + "_" + ordinal;
        pendingCountCacheMaps.put(cacheKey, total);

        return total;
	}
	public Map<String,Integer> getPendingCountMaps () {
		return pendingCountCacheMaps;
	}
	public void updatePendingCountMaps(String cacheKey, int total) {
		pendingCountCacheMaps.put(cacheKey, total);
	}
    @Override
    public BaseSectionTemplete projection(Map<String, String> preference) {
    	MultiRowVariableColumnColTemplete c = new MultiRowVariableColumnColTemplete();
        User user = AppContext.getCurrentUser();
        String fragmentId = preference.get(PropertyName.entityId.name());
        String ordinal = preference.get(PropertyName.ordinal.name());
        String aiSortValue= preference.get("aiSortValue");
        String setAiSort= preference.get("setAiSort");//设置AI操作
        String spaceId= preference.get("spaceId");
        String x= preference.get("x");
        String y= preference.get("y");
        boolean hasAIPlugin = AppContext.hasPlugin("ai");
		if(hasAIPlugin && Strings.isNotBlank(aiSortValue) && Strings.isNotBlank(setAiSort) && "1".equals(setAiSort)){//将ai_sort_value值保存起来
			try{
				PortalSpaceFix personalFix= portalCacheManager.updatePortletProperty(user, spaceId, fragmentId, ordinal, x, y,"aiSortValue",aiSortValue);
				if(null!=personalFix){
					c.setRefreshSpaceId(personalFix.getId().toString());
					c.setRefreshSpacePath(personalFix.getPath());
					return c;
				}
			}catch(Throwable e){
				LOG.error("", e);
			}
		}

        int pageSize = c.getPageSize(preference)[0];

        //显示列
        String rowStr = preference.get("rowList");
        if (Strings.isBlank(rowStr)) {
            rowStr = "subject,receiveTime,sendUser,category";
        }

        String currentPanel = SectionUtils.getPanel("all", preference);
        String panels = "all";
        if (preference.get("panel") != null) {
            panels = preference.get("panel");
        }
        String[] panelValues = panels.split(",");
        if (Strings.isBlank(currentPanel)) {
            currentPanel = panelValues[0];
        }

        List<PendingRow> rowList = new ArrayList<PendingRow>();
        try {
            List<CtpAffair> affairs = new ArrayList<CtpAffair>();
            boolean isAiSort= hasAIPlugin && "1".equals(aiSortValue);
            if (isAiSort) {// 有AI插件 && 打开智能排序开关
                affairs = pendingManager.getAISortPendingList(user.getId(), Long.parseLong(fragmentId), ordinal, pageSize);
            }
            else {
                affairs = pendingManager.getPendingList(user.getId(), Long.parseLong(fragmentId), ordinal, pageSize);
            }
            rowList = pendingManager.affairList2PendingRowList(affairs, user, currentPanel, true, rowStr,StateEnum.col_pending.key());
            // 置顶排序一下
            if("0".equals(getAiShortValue(preference))) {
            	Collections.sort(rowList, new PendingRowComparator());
            }
        } catch (BusinessException e) {
        	LOG.error("", e);
        }

        String s = "";
        try {
            s = URLEncoder.encode(this.getName(preference), "UTF-8");
        } catch (UnsupportedEncodingException e) {
        	LOG.error("待办栏目名转url码异常!", e);
        }

        c = pendingManager.getTemplete(c,rowList, preference);
//        zhou:Start
        MultiRowVariableColumnColTemplete templete =  c;
        List<MultiRowVariableColumnColTemplete.Row> listRows = templete.getRows();
        if(null != listRows && listRows.size() > 0) {
            for (int i = 0; i < listRows.size(); i++) {
                MultiRowVariableColumnColTemplete.Row row = listRows.get(i);
                List<MultiRowVariableColumnColTemplete.Cell> rowCells = row.getCells();
                MultiRowVariableColumnColTemplete.Cell cell = rowCells.get(1);
                String time = cell.getCellContentHTML();
                if (time.indexOf("今日") != -1) {
                    cell.setCellContentHTML(time.substring(0, 2));
                }
            }
        }
//        zhou:End
        c.setDataNum(pageSize);
        c.addBottomButton(BaseSectionTemplete.BOTTOM_BUTTON_LABEL_MORE,
                "/collaboration/pending.do?method=morePendingCenter&fragmentId=" + preference.get(PropertyName.entityId.name()) + "&ordinal=" + preference.get(PropertyName.ordinal.name()) + "&currentPanel=" + currentPanel + "&rowStr=" + rowStr + "&columnsName=" + s + "&source=Common&section=All&spaceId="+preference.get(PropertyName.spaceId.name())+"&aiSortValue=" + aiSortValue+"&x="+x+"&y="+y, null, "sectionMoreIco");
        return c;
    }
    @Override
    public BaseSectionTemplete mProjection(Map<String, String> preference) {
        User user = AppContext.getCurrentUser();
        Integer count = SectionUtils.getSectionCount(3, preference);
        //显示列
        String rowStr = preference.get("rowList");
        if (Strings.isBlank(rowStr)) {
            rowStr = "subject,receiveTime,sendUser,category";
        }

        String currentPanel = SectionUtils.getPanel("all", preference);
        String panels = "all";
        if (preference.get("panel") != null) {
            panels = preference.get("panel");
        }

        preference.put("from", "Vjoin");//移动端只查询协同数据

        String[] panelValues = panels.split(",");
        if (Strings.isBlank(currentPanel)) {
            currentPanel = panelValues[0];
        }
        List<PendingRow> rowList = new ArrayList<PendingRow>();
        try {
            List<CtpAffair> affairs = pendingManager.getPublicPendingList(user.getId(), preference, count);
            rowList = pendingManager.affairList2PendingRowList(affairs, user, currentPanel, true, rowStr,StateEnum.col_pending.key());
        } catch (BusinessException e) {
        	LOG.error("", e);
        }
        MListTemplete c = new MListTemplete();
        if (Strings.isNotEmpty(rowList)) {
            for (PendingRow pendingRow : rowList) {
                MListTemplete.Row row = c.addRow();
                String subject = pendingRow.getSubject();
                row.setSubject(subject);
                //置顶
                if (pendingRow.getTopTime() != null) {
                	row.setTop(true);
                }
                //设置重要程度图标
                if(pendingRow.getImportantLevel() != null && pendingRow.getImportantLevel() > 1  && pendingRow.getImportantLevel() < 6){
                	//3:非常重要 2重要
                	row.setIcon("important-"+pendingRow.getImportantLevel());
                }
                ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(pendingRow.getApplicationCategoryKey());
                switch (appEnum) {
        			case collaboration :
        				row.setLink("/seeyon/m3/apps/v5/collaboration/html/details/summary.html?affairId=" + pendingRow.getId()
        				+ "&openFrom=listPending&VJoinOpen=VJoin&summaryId=" + pendingRow.getObjectId() + "&r="
        				+ System.currentTimeMillis());
        				break;
        			case edocSend:
                    case edocRec:
                    case edocSign:
                    	row.setLink("/seeyon/m3/apps/v5/edoc/html/edocSummary.html?affairId=" + pendingRow.getId()
        				+ "&openFrom=listPending&VJoinOpen=VJoin&summaryId=" + pendingRow.getObjectId() + "&r="
        				+ System.currentTimeMillis());
                        break;
					default:
						row.setLink("noSupport");
						break;
                }

                //row.setLink(setMobileDetailLink(pendingRow));

                row.setCreateDate(MListTemplete.showDate(pendingRow.getCreateDate()));
                row.setCreateMember(pendingRow.getCreateMemberName());
                row.setReadFlag("true");
                row.setState(pendingRow.getSummaryState() == null ? "0" : pendingRow.getSummaryState().toString());
                row.setCreateMemberId(pendingRow.getCreateMemberId().toString());
                row.setHasAttachments(pendingRow.getHasAttachments());
                if (pendingRow.getDealTimeout() && pendingRow.isShowClockIcon()) {//已超期
                	row.setOverTime(true);
                }
            }
        }
        String entityId = preference.get("entityId");//entityId
        String ordinal = preference.get("ordinal");
        String columnsName = ResourceUtil.getString("common.my.pending.title");
        try {
        	columnsName = URLEncoder.encode(this.getName(preference), "UTF-8");
        } catch (UnsupportedEncodingException e) {
        	LOG.error("待办栏目名转url码异常!", e);
        }

        String moreLink = "/seeyon/m3/apps/v5/portal/html/morePending.html?openFrom=listPending&entityId="+entityId+"&ordinal="+ordinal+"&columnsName="+columnsName+"&VJoinOpen=VJoin&r="
                + System.currentTimeMillis();
        c.setMoreLink(moreLink);
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

	@Override
	public String getAiShort(Map<String, String> preference) {
		boolean hasAIPlugin = AppContext.hasPlugin("ai");
		return hasAIPlugin ? "1" : "0";
	}

	@Override
	public String getAiShortValue(Map<String, String> preference){
		String aiSortValue= preference.get("aiSortValue");
		if(Strings.isBlank(aiSortValue)){
			aiSortValue= "0";
		}
		return aiSortValue;
	}

	@Override
	public boolean isAllowMobileCustomSet() {
	    return true;
	}

	public void setPortalCacheManager(PortalCacheManager portalCacheManager) {
		this.portalCacheManager = portalCacheManager;
	}
}
