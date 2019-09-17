package com.seeyon.apps.leaderwindow.section;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.portal.section.BaseSectionImpl;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete;
import com.seeyon.ctp.portal.sso.thirdpartyintegration.UrlBuilder;
import com.seeyon.ctp.util.Strings;

/**
 * <p>Title: 栏目抽象类，所有的栏目继承该抽象类</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: seeyon.com</p>
 * @since CTP2.0
 */
public class GenericIframeSection extends BaseSectionImpl {

    private static final Log log         = LogFactory.getLog(GenericIframeSection.class);

    private String           id;

    private String           icon;

    private String           name;

    private String           url;

    private String           scrolling   = "no";

    private String           frameborder = "0";

    private String           height      = "100%";

    private UrlBuilder       urlBuilder;

    /**
     * 设置栏目显示的名称
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * 设置栏目的唯一id，必须和Spring bean的id一致
     * 
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Iframe的地址
     * 
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 设置页面地址的生成器，<code>url</code>优先
     * 
     * @param urlBuilder
     */
    public void setUrlBuilder(UrlBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
    }

    private String getURL(long memberId, long loginAccountId) {
        if (Strings.isNotBlank(url)) {
            return url;
        }

        if (this.urlBuilder != null) {
            try {
                return this.urlBuilder.builder(memberId, loginAccountId);
            } catch (Exception e) {
                log.error("得到地址[" + this.name + "]", e);
            }
        }

        return null;
    }

    /**
     * 边框，默认为0
     * 
     * @param frameborder
     */
    public void setFrameborder(String frameborder) {
        this.frameborder = frameborder;
    }

    /**
     * 滚动条，默认“no”
     * 
     * @param scrolling
     */
    public void setScrolling(String scrolling) {
        this.scrolling = (scrolling);
    }

    /**
     * iframe的高度，默认100%,如果采用像素，最大是438px
     * 
     * @param height
     */
    public void setHeight(String height) {
        this.height = (height);
    }

    public String getIcon() {
        return icon;
    }

    public String getId() {
        return id;
    }

    public String getName(Map<String, String> preference) {
        return name;
    }

    public Integer getTotal(Map<String, String> preference) {
        return null;
    }

    public BaseSectionTemplete projection(Map<String, String> preference) {
        User user = AppContext.getCurrentUser();

        IframeTemplete h = new IframeTemplete();
        h.setFrameborder(frameborder);
        h.setHeight(height);
        h.setScrolling(scrolling);

        h.setUrl(this.getURL(user.getId(), user.getLoginAccount()));

        return h;
    }

    public class IframeTemplete extends BaseSectionTemplete {

        private String            url;

        private String            scrolling        = "no";

        private String            frameborder      = "0";

        private String            height           = "100%";

        public String getResolveFunction() {
            return "iframeTemplete";
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public String getFrameborder() {
            return frameborder;
        }

        public void setFrameborder(String frameborder) {
            this.frameborder = frameborder;
        }

        public String getScrolling() {
            return scrolling;
        }

        public void setScrolling(String scrolling) {
            this.scrolling = scrolling;
        }

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

    }

}
