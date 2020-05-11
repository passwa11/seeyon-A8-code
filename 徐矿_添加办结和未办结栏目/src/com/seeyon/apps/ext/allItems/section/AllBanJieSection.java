package com.seeyon.apps.ext.allItems.section;

import com.seeyon.ctp.portal.section.BaseSectionImpl;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete;
import com.seeyon.ctp.portal.section.templete.HtmlTemplete;
import com.seeyon.ctp.util.Strings;
import org.apache.commons.lang.math.NumberUtils;

import java.util.Map;

public class AllBanJieSection extends BaseSectionImpl {
    @Override
    public String getId() {
        return "allBanJieSection";
    }

    @Override
    public String getName(Map<String, String> map) {
        String name = Strings.isNotBlank(map.get("columnsName")) ? map.get("columnsName") : "所有办结";
        return name;
    }

    @Override
    public Integer getTotal(Map<String, String> map) {
        return null;
    }

    @Override
    public String getIcon() {
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
    public BaseSectionTemplete projection(Map<String, String> map) {
        String url = "/seeyon/allItems.do?method=finishColumnDataSection&templetIds=" + map.get("template_catagory_value");
        if (Strings.isNotBlank(map.get("sectionUrl"))) {
            url = map.get("sectionUrl");
        }
        HtmlTemplete ht = new HtmlTemplete();
        StringBuilder html = new StringBuilder();
        html.append("<iframe scrolling='no' style='width:100%;height:" + (this.getHeight(map) - 23) + "px;' frameborder='no' border='0' src='" + url + "'></iframe>");
        ht.setHtml(html.toString());
        ht.setModel(HtmlTemplete.ModelType.inner);
        ht.setShowBottomButton(true);
        ht.addBottomButton("common_more_label", "/allItems.do?method=toMoreOfBanjie&templetIds=" + map.get("template_catagory_value"), BaseSectionTemplete.OPEN_TYPE.href.name());
        return ht;
    }
}
