package com.seeyon.apps.xkjt.section;

import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;

import com.seeyon.ctp.portal.section.BaseSectionImpl;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete;
import com.seeyon.ctp.portal.section.templete.HtmlTemplete;
import com.seeyon.ctp.util.Strings;
/**
 * 
 * @ClassName: LeaderYiYueSection  
 * @Description: 已阅栏目开发  
 * @author wxt.chenqiang
 * @date 2019年4月12日 下午3:12:31
 */
public class LeaderYiYueSection extends BaseSectionImpl {

	@Override
	public String getIcon() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean isNoHeaderSection() {
		return false;
	}
	@Override
	public String getId() {
		return "leaderYiYueSection";
	}

	@Override
	public String getBaseName(Map<String, String> preference) {
		String name = "已阅";
		if(Strings.isNotBlank(preference.get("columnsName"))) {
			name = preference.get("columnsName");
		}
		return name;
	}
	@Override
	public String getName(Map<String, String> preference) {
		String name = Strings.isNotBlank(preference.get("columnsName")) ? preference.get("columnsName") : "已阅";
		return name;
	}

	@Override
	public Integer getTotal(Map<String, String> preference) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getHeight(Map<String, String> preference) {
		String heightStr = preference.get("sectionHigt");
		int height = 460;
		if (NumberUtils.isDigits(heightStr)) {
			height = Integer.parseInt(heightStr);
		}

		return height;
	}
	@Override
	public BaseSectionTemplete projection(Map<String, String> preference) {
		String sectionUrl = "/seeyon/xkjtController.do?method=initYiYueList";
        //栏目url
		if(Strings.isNotBlank(preference.get("sectionUrl"))){
			sectionUrl = preference.get("sectionUrl");
		}

        //栏目解析主方法
        HtmlTemplete ht = new HtmlTemplete();
        StringBuilder html = new StringBuilder();
        html.append("<iframe scrolling='no' style='width:100%;height:"+(getHeight(preference)-23)+"px;' frameborder='no' border='0' src='"+sectionUrl+"'></iframe>");
        ht.setHtml(html.toString());
        ht.setModel(HtmlTemplete.ModelType.inner);
        ht.setShowBottomButton(true);
        ht.addBottomButton(BaseSectionTemplete.BOTTOM_BUTTON_LABEL_MORE,"/xkjtController.do?method=yiYueMore",BaseSectionTemplete.OPEN_TYPE.href.name());
        return ht;
	}

}
