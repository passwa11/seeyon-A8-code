package com.seeyon.ctp.portal.section;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.seeyon.apps.govdoc.constant.GovdocEnum.GovdocResCodeEnum;
import com.seeyon.apps.govdoc.manager.GovdocStatPushManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete;
import com.seeyon.ctp.portal.section.templete.IframeTemplete;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.web.login.CurrentUser;
import com.seeyon.v3x.edoc.domain.EdocRegisterCondition;

public class EdocRecRegisterSection extends BaseSectionImpl {

    public static final String RESOLVE_FUNCTION= "edocRecRegisterSection";
	
    private GovdocStatPushManager govdocStatPushManager;

    @Override
	public String getResolveFunction(Map<String, String> preference) {
    	return IframeTemplete.RESOLVE_FUNCTION;
	}

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public String getId() {
        return "edocRecRegisterSection";
    }

    @Override
    public String getBaseName(Map<String, String> preference) {
        String name = preference.get("columnsName");
        if(Strings.isBlank(name)){
            name = ResourceUtil.getString("edoc.rec.register");//收文登记簿
        }
        return name;
    }

    @Override
    public String getName(Map<String, String> preference) {
        //栏目显示的名字，必须实现国际化，在栏目属性的“columnsName”中存储
        String name = preference.get("columnsName");
        if(Strings.isBlank(name)){
            return ResourceUtil.getString("edoc.rec.register");
        }else{
            return name;
        }
    }

    @Override
    public Integer getTotal(Map<String, String> preference) {
        //User user = AppContext.getCurrentUser();
        //int total = manager.getEdocRegisterConditionTotal(user.getLoginAccount(),2,null);
        //return total;
        return null;
    }

    @Override
    public BaseSectionTemplete projection(Map<String, String> preference) {
        int count = SectionConstants.sectionCount;
        String countStr = preference.get("count");
        if(Strings.isNotBlank(countStr)){
            count = Integer.parseInt(countStr);
        }
        
        String listType = "listRecRegister";
        Map<String,Object> paramMap = new HashMap<String,Object>();
        paramMap.put("listType", listType);
        List<EdocRegisterCondition> list =  govdocStatPushManager.getRegisterConditionList(AppContext.getCurrentUser(), paramMap);
        String conditionId = "";
        if (CollectionUtils.isNotEmpty(list)) {
            EdocRegisterCondition register = list.get(0);
            conditionId = String.valueOf(register.getId());
        }
        
        IframeTemplete h = new IframeTemplete();
        h.addBottomButton(BaseSectionTemplete.BOTTOM_BUTTON_LABEL_MORE,"/govdoc/govdoc.do?method=statIndex&listType=listRecRegister&conditionId="+conditionId);
        
        String baseUrl = AppContext.getRawRequest().getContextPath();
        String url = baseUrl + "/govdoc/list.do?method=listRecRegister&listType=" + listType + "&conditionId=" + conditionId + "&dataNum=" + count + "&sectionId=" + preference.get("panelId");
        h.setUrl(url);
        h.setFrameborder("0");
        h.setScrolling("auto");
        
        h.setHeight("300");
        String height = (String)preference.get("height");
        if (Strings.isNotBlank(height)) {
        	h.setHeight(height);
        }
        
        return h;
    }

    
    /**
	 * 是否允许添加-使用该栏目，默认允许，如果需要特别控制，需要重载该方法，当前登录信息从CurrentUser中取
	 * @return
	 */
    @Override
	public boolean isAllowUsed() {
		User user = CurrentUser.get();
		return user.hasResourceCode(GovdocResCodeEnum.F20_recandreportAuth.name()) || user.isAdmin() || user.isGroupAdmin();
	}
    
    /**
	 * 是否允许添加该栏目，默认允许，如果需要特别控制，需要重载该方法，当前登录信息从CurrentUser中取
	 * 如果不允许，将不出现在备选栏目中；但如果是管理员推送的栏目，可以访问
	 * @return
	 */
	public boolean isAllowUsed(String spaceType) {
		return isAllowUsed();
	}
	
	public void setGovdocStatPushManager(GovdocStatPushManager govdocStatPushManager) {
        this.govdocStatPushManager = govdocStatPushManager;
    }
	
}
