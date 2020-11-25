package com.seeyon.ctp.portal.section;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.bo.PendingRow;
import com.seeyon.ctp.common.affair.manager.PendingManager;
import com.seeyon.ctp.common.affair.vo.PendingRowComparator;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.portal.po.PortalSpaceFix;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete;
import com.seeyon.ctp.portal.section.templete.MultiRowVariableColumnColTemplete;
import com.seeyon.ctp.portal.section.templete.mobile.MListTemplete;
import com.seeyon.ctp.portal.section.util.SectionUtils;
import com.seeyon.ctp.portal.util.PortletPropertyContants.PropertyName;
import com.seeyon.ctp.util.Strings;

public class PendingSection extends BaseSectionImpl {
	private static Log LOG = CtpLogFactory.getLog(PendingSection.class);
	private PendingManager pendingManager;
	private Map<String,Integer> pendingCountCacheMaps = new ConcurrentHashMap<String,Integer>();
	
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
  	  if (AppContext.isGroupAdmin()) {
          return false;
      }
      return true;
	}
	
	@Override
    public boolean isAllowUserUsed(String singleBoardId) {
        return false;
    }
	@Override
    public boolean isAllowUserUsed(Map<String, String> preference) {
		//没有会议插件时,不显示待开会议栏目
		if (!AppContext.hasPlugin("meeting")) {
			String sourcesPolicyValue = preference.get("sources_Policy_value");
			String policeValue = preference.get("Policy_value");
			if("A___30".equals(policeValue) || "A___30".equals(sourcesPolicyValue)){
				return false;
			}
		}
		
		if (AppContext.isGroupAdmin()) {
            return false;
        }
        return true;
    }
	@Override
	public String getBaseNameI18nKey(){
		return "common.my.pending.title";
	}
	@Override
    public String getBaseName(Map<String, String> preference) {
		String name = "";
		if (preference != null) {
			name = preference.get("baseName");
		}
		if(Strings.isBlank(name)){ 
            name = ResourceUtil.getString("common.my.pending.title");
        }
		if(Strings.isBlank(name)&&(preference!=null)){
		    name=preference.get("columnsName");
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
        
        Integer total =  this.pendingManager.getPendingCount(memberId,preference,false);
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
				PortalSpaceFix personalFix= this.getPortalApi().updatePortletProperty(user, spaceId, fragmentId, ordinal, x, y,"aiSortValue",aiSortValue);
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

        c = PendingSectionUrlUtil.getTemplete(c,rowList, preference);
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
                    case edoc:
	            		ApplicationSubCategoryEnum subAppEnum = ApplicationSubCategoryEnum.valueOf(pendingRow.getApplicationCategoryKey(), pendingRow.getApplicationSubCategoryKey());
	            		switch (subAppEnum) {
	            			case edoc_fawen://G6新公文发文
	            			case edoc_qianbao://G6新公文签报
	            			case edoc_shouwen://G6新公文收文
	            			case edoc_jiaohuan://G6新公文交换
	            				row.setLink("/seeyon/m3/apps/v5/edoc/html/details/summary.html?affairId=" + pendingRow.getId()
	            				+ "&openFrom=listPending&VJoinOpen=VJoin&summaryId=" + pendingRow.getObjectId() + "&r="
	            				+ System.currentTimeMillis());
	                    		break;
	            			case old_edocSend://G6老公文发文
	            			case old_edocRec://G6老公文收文
	            			case old_edocSign://G6老公文签报
	            				row.setLink("/seeyon/m3/apps/v5/edoc/html/edocSummary.html?affairId=" + pendingRow.getId()
	            				+ "&openFrom=listPending&VJoinOpen=VJoin&summaryId=" + pendingRow.getObjectId() + "&r="
	            				+ System.currentTimeMillis());
	                    		break;
							default:
								row.setLink("noSupport");
								break;
	            		}
	            		break;
					case meeting:
						row.setLink("/seeyon/m3/apps/v5/meeting/html/meetingDetail.html?meetingId=" + pendingRow.getObjectId());
						break;
					case meetingroom:
						row.setLink("/seeyon/m3/apps/v5/meeting/html/meetingRoomApprove.html?openFrom=mrAuditList&roomAppId=" + pendingRow.getObjectId());
						break;
					default:
						row.setLink("noSupport");
						break;
                }
                int app = pendingRow.getApplicationCategoryKey();
                String categoryName = ResourceUtil.getString("application."+app+".label");
                //分类
                switch (appEnum) {
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
	                	Integer subApp = pendingRow.getApplicationSubCategoryKey();
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
	                        	categoryName = ResourceUtil.getString("application."+subApp+".label");
	                    		break;
	            			case old_edocRecDistribute: //G6老公文分发
	                        	categoryName = ResourceUtil.getString("application."+subApp+".label");
	                    		break;
	            		}
	            		row.setCategory(categoryName);
	            		break;
	                default:break;
	            }
                //row.setLink(setMobileDetailLink(pendingRow));
                
                row.setCreateDate(pendingRow.getReceiveTime());
                row.setCreateMember(pendingRow.getCreateMemberName());
                row.setReadFlag(Integer.valueOf(SubStateEnum.col_pending_unRead.key()).equals(pendingRow.getSubState()) ? "false" : "true");
                row.setState(pendingRow.getSummaryState() == null ? "0" : pendingRow.getSummaryState().toString());
                row.setCreateMemberId(pendingRow.getCreateMemberId().toString());
                row.setHasAttachments(pendingRow.getHasAttachments());
                if (pendingRow.getDealTimeout() && pendingRow.isShowClockIcon()) {//已超期
                	row.setOverTime(true);
                }
            }
        }
        String entityId = preference.get("entityId");
        String ordinal = preference.get("ordinal");
        
        String moreLink = "/seeyon/m3/apps/m3/todo/layout/todo-list.html?openFrom=listPending&entityId="+entityId+"&ordinal="+ordinal+"&VJoinOpen=VJoin";
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
}
