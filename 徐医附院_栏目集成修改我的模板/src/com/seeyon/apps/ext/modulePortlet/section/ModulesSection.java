package com.seeyon.apps.ext.modulePortlet.section;

import com.seeyon.ctp.portal.section.BaseSectionImpl;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete;
import com.seeyon.ctp.portal.section.templete.HtmlTemplete;
import com.seeyon.ctp.util.Strings;

import java.util.Map;

/**
 * 周刘成   2019-12-16
 */
public class ModulesSection extends BaseSectionImpl {
    @Override
    public String getId() {
        return "sectionPortal";
    }

    @Override
    public String getName(Map<String, String> map) {
        String name = "流程审批";
        if (Strings.isBlank(name)) {
            return "流程审批";
        } else {
            return name;
        }
    }

    @Override
    public Integer getTotal(Map<String, String> map) {
        return null;
    }

    @Override
    public String getIcon() {
        return null;
    }

    /**
     * 栏目模板列表
     * <p>
     * HtmlTemplete 直接输出HTML代码片断
     * CalendarFourColumnTemplete 日程事件的四列模板：标题 开始时间结束时间 状态
     * ChessboardTemplete 棋盘式 * 左边小图标(默认16*16)+右边标题 *上边大图标(默认32*32)+下边标题 * 标题可以有浮动菜单
     * ChessMultiRowThreeColumnTemplete 棋盘式 、3列
     * MonthCalendarTemplate 月历式栏目
     * MoveMultiRowThreeColumnTemplete 多行3列滚动式
     * MultiIconCategoryItem 多行的，图标，分类，文本的展现形式图表是32px * 32px的，在左边，右边的上面是分类(Category),下面是若干个项
     * MultiRowFourColumnTemplete 多行3列模板，依次是：subject createDate createMemberName category
     * MultiRowThreeColumnTemplete 多行3列模板，依次是：subject createDate category
     * MultiRowVariableColumnTemplete 成倍行,不定列 模板 适用于　三或四列标准列表模板满足不了需要的情况下* 可以自定义列数、宽度、单元格样式、链接地址
     * MultiSubjectSummary 多行的，显示标题和摘要，常用新闻、公告
     * OneImageAndListTemplete 图片加列表模板 * 第一列为图片居左 右边为标题加摘要下面为列表 * 列表内容为 * 标题 发起时间 所属板块 * 列表参数可配置
     * OneItemUseTwoRowTemplete 两行展现一项 模板适用于　如集团空间调查栏目 * 第１行　标题，另起一行　发布时间和类型
     * OnePictureTemplete 图片滚动式
     * OneSummaryAndMultiList 显示模式：一条显示为“标题+时间+(类别)+摘要”，下面是若干行列表
     * PictureTemplete图片基础模板
     * PictureTitleAndBriefTemplete 标题加摘要的新闻模板
     */
    @Override
    public BaseSectionTemplete projection(Map<String, String> preference) {
        String sectionUrl = "/seeyon/ext/modulePortlet.do?method=index";
        if (Strings.isNotBlank(preference.get("sectionUrl"))) {
            sectionUrl = preference.get("sectionUrl");
        }
        HtmlTemplete ht = new HtmlTemplete();
        StringBuilder html = new StringBuilder();
        html.append("<iframe scrolling='no' style='width:100%;height:" + (this.getHeight(preference) - 23) + "px;' frameborder='no' border='0' src='" + sectionUrl + "'></iframe>");
        ht.setHtml(html.toString());
        ht.setModel(HtmlTemplete.ModelType.inner);
        ht.setShowBottomButton(true);
        ht.addBottomButton("common_more_label", "/template/template.do?method=moreTreeTemplate&type=more", HtmlTemplete.OPEN_TYPE.href.name());
        return ht;
    }
}
