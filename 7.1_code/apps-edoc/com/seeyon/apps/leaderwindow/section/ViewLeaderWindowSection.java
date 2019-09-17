/**
 * $Author xiongfeifei$
 * $Rev$
 * $Date::2015-9-9$:
 *
 * Copyright (C) 2015 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.apps.leaderwindow.section;

import java.util.Map;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete.OPEN_TYPE;
import com.seeyon.ctp.portal.util.Constants.SpaceType;

/**
 * <p>
 * Title: 领导之窗查看Section类
 * </p>
 * <p>
 * Description: 代码描述
 * </p>
 * <p>
 * Copyright: Copyright (c) 2015
 * </p>
 * <p>
 * Company: seeyon.com
 * </p>
 */
public class ViewLeaderWindowSection extends GenericIframeSection{

	    @Override
	    public String getBaseName(Map<String, String> preference) {
	    	if (preference.get("columnsName") != null) {
				return ((String) preference.get("columnsName"));
			}
	    	return ResourceUtil.getString("edoc.leader.window.section.name");
	    }
	    
		public String getBaseName() {
			return ResourceUtil.getString("edoc.leader.window.section.name");
		}
	  
		public String getName(Map<String, String> preference) {
			return getBaseName(preference);
		}
		 
	    @Override
	    public boolean isAllowUsed() {
	        return true;
	    }

	    @Override
	    public boolean isAllowUserUsed(String singleBoardId) {
	        return isAllowUsed();
	    }
	    
	    @Override
	    public String getId() {
	        return "iframeViewLeaderWindowSection";
	    }
	    
	    @Override
	    public BaseSectionTemplete projection(Map<String, String> preference) {
	    	String spaceType=preference.get("spaceType");//corporation  group
	    	Long accountId=AppContext.currentAccountId();
	    	if(SpaceType.group.name().equals(spaceType)){//公共空间栏目
	    		accountId=OrgConstants.GROUPID;
	    	}
            String  url = SystemEnvironment.getContextPath() + "/portal/leaderWindowController.do?method=getLeaderWindow&accountId="+accountId;
            super.setUrl(url);
            super.setFrameborder("0");
            super.setHeight(preference.get("height"));
            super.setScrolling("auto");           
            BaseSectionTemplete h = super.projection(preference);
            h.setDataNum(8);
            String moreLabel = ResourceUtil.getString("common.more.label");
            String moreLink = "/portal/leaderWindowController.do?method=moreLeaderWindow&accountId="+accountId;
            h.addBottomButton(moreLabel, moreLink, OPEN_TYPE.href_blank.name(), "sectionMoreIco");
            return h;
	    }
}
