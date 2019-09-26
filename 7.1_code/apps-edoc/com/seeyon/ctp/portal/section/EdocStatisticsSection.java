package com.seeyon.ctp.portal.section;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.apps.govdoc.constant.GovdocEnum.GovdocResCodeEnum;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete;
import com.seeyon.ctp.portal.section.templete.ChessboardTemplete;
import com.seeyon.ctp.portal.section.templete.IframeTemplete;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.web.login.CurrentUser;
import com.seeyon.v3x.edoc.manager.EdocStatManager;

public class EdocStatisticsSection extends BaseSectionImpl {
	
    private static final Log LOG = CtpLogFactory.getLog(EdocStatisticsSection.class);
    
    public static final String RESOLVE_FUNCTION= "edocStatisticsSection";

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
        return "edocStatisticsSection";
    }

    @Override
    public String getBaseName(Map<String, String> preference) {
        String name = preference.get("columnsName");
        if(Strings.isBlank(name)){
            name = ResourceUtil.getString("edoc.stat.label");//公文统计
        }
        return name;
    }

    @Override
    public String getName(Map<String, String> preference) {
        //栏目显示的名字，必须实现国际化，在栏目属性的“columnsName”中存储
        String name = preference.get("columnsName");
        if(Strings.isBlank(name)){
            return ResourceUtil.getString("edoc.stat.label");
        }else{
            return name;
        }
    }

    @Override
    public Integer getTotal(Map<String, String> preference) {
        //User user = AppContext.getCurrentUser();
        //int total = manager.getEdocStatConditionTotal(user.getLoginAccount(),null);
        //return total;
        return null;
    }

    @Override
    public BaseSectionTemplete projection(Map<String, String> preference) {
    	EdocStatManager manager = (EdocStatManager)AppContext.getBean("edocStatManager");
        ChessboardTemplete c = new ChessboardTemplete();
        int[] chessBoardInfo= c.getPageSize(preference); 
        int count= chessBoardInfo[0];
        int row = chessBoardInfo[1];
        int column = chessBoardInfo[2];
        c.setLayout(row, column);
        c.setDataNum(count);
        
        User user = AppContext.getCurrentUser();
        Map<String,Object> paramMap = new HashMap<String,Object>();
        paramMap.put("userId", user.getId());
        paramMap.put("count", count);
        /*List<EdocStatCondition> list =  manager.getEdocStatCondition(user.getLoginAccount(),paramMap);
        String statConditionId = "";
        String statConditionEdocType = "";
        if (Strings.isNotEmpty(list)) {
        	statConditionId = list.get(0).getId()+"";
        	statConditionEdocType =  list.get(0).getEdocType()==null ? "" : list.get(0).getEdocType()+"";
        }*/
        
        //【更多】
        String s = "";
        try {
            s = URLEncoder.encode(this.getName(preference),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        IframeTemplete h = new IframeTemplete();
        h.addBottomButton(BaseSectionTemplete.BOTTOM_BUTTON_LABEL_MORE,"/govdoc/govdoc.do?method=statIndex&columnsName=" + s + "&sectionId=" + preference.get("panelId"));
        
        String baseUrl = AppContext.getRawRequest().getContextPath();
        String url =  baseUrl + "/govdoc/stat.do?method=statOldCondition&dataNum=" + count + "&sectionId=" + preference.get("panelId");
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
		return user.hasResourceCode(GovdocResCodeEnum.F20_stat.name()) || user.isAdmin() || user.isGroupAdmin();
	}
    
    /**
	 * 是否允许添加该栏目，默认允许，如果需要特别控制，需要重载该方法，当前登录信息从CurrentUser中取
	 * 如果不允许，将不出现在备选栏目中；但如果是管理员推送的栏目，可以访问
	 * @return
	 */
	public boolean isAllowUsed(String spaceType) {
		return isAllowUsed();
	}
	
}