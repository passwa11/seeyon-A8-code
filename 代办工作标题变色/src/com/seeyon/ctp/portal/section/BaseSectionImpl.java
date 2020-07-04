/**
 * $Author: zhout $
 * $Rev: 1241 $
 * $Date:: 2012-08-09 17:05:03 +#$:
 * <p>
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 * <p>
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.portal.section;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.seeyon.ctp.portal.section.templete.MultiRowVariableColumnColTemplete;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.cache.etag.ETagCacheManager;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.web.util.WebUtil;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete;
import com.seeyon.ctp.portal.section.templete.ChessboardTemplete;
import com.seeyon.ctp.portal.section.util.SectionUtils;
import com.seeyon.ctp.portal.space.manager.PortletEntityDataManager;
import com.seeyon.ctp.portal.space.manager.PortletEntityPropertyManager;
import com.seeyon.ctp.portal.util.PortalCommonUtil;
import com.seeyon.ctp.portal.util.PortalConstants;
import com.seeyon.ctp.portal.util.PortletPropertyContants;
import com.seeyon.ctp.portal.util.PortletPropertyContants.PropertyName;
import com.seeyon.ctp.util.Strings;


/**
 * <p>Title: 栏目抽象类，所有的栏目继承该抽象类</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: seeyon.com</p>
 *
 * @since CTP2.0
 */
public abstract class BaseSectionImpl implements BaseSection, Comparable<BaseSectionImpl> {

    private static Log log = LogFactory.getLog(BaseSectionImpl.class);

    private String[] sectionTypes = null;//栏目类型：常用栏目、时间管理...

    private String sectionCategory;//栏目类型中的分类：常用、计划...

    private String[] spaceTypes = null;

    private boolean isRegistrer = true;

    private boolean isAllowedNarrow = false;

    private boolean isNoHeaderSection = false;

    private String baseName;//add by dongyj 显示系统默认名称(配置栏目时候显示)

    private Integer sortId;

    private ETagCacheManager eTagCacheManager;

    private PortletEntityPropertyManager portletEntityPropertyManager;

    private PortletEntityDataManager portletEntityDataManager;

    private List<SectionProperty> properties;

    //private SectionReference[] preferences;

    private String resourceBundle;

    /**
     * 是否禁止外部人员访问
     */
    private boolean isFilterOut = false;
    /**
     * 由于首页做了页面缓存。有的栏目根本不需要刷新，在这里定义一个变量。标示，切换空间的时候不刷新
     */
    private int delay = -1;

    public boolean isNoHeaderSection() {
        return isNoHeaderSection;
    }

    public boolean isNoHeaderSection(Map<String, String> preference) {
        return isNoHeaderSection;
    }

    public void setNoHeaderSection(boolean isNoHeaderSection) {
        this.isNoHeaderSection = isNoHeaderSection;
    }

    public boolean isFilterOut() {
        return isFilterOut;
    }

    public void setIsFilterOut(boolean isFilterOut) {
        this.isFilterOut = isFilterOut;
    }

    public int getDelay() {
        return delay;
    }

    /**
     * 是否有参数配置<br>
     * 见section.xml.
     *
     * @return
     */
    public boolean hasParam() {
        return CollectionUtils.isNotEmpty(properties);
    }

    /**
     * 设置显示的延迟时间<br>
     * 默认 -1 标示按照IE加载顺序显示（通过AJAX），切换空间时刷新<br>
     * 0 标示只第一次加载（通过AJAX），切换空间时永远不刷新，可以通过其他途径刷新（如：手工、消息）<br>
     *
     * @param delay
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void seteTagCacheManager(ETagCacheManager eTagCacheManager) {
        this.eTagCacheManager = eTagCacheManager;
    }

    public ETagCacheManager geteTagCacheManager() {
        if (eTagCacheManager == null) {
            eTagCacheManager = (ETagCacheManager) AppContext.getBean("eTagCacheManager");
        }
        return eTagCacheManager;
    }

    public final void setPortletEntityPropertyManager(PortletEntityPropertyManager portletEntityPropertyManager) {
        this.portletEntityPropertyManager = portletEntityPropertyManager;
    }

    public PortletEntityPropertyManager getPortletEntityPropertyManager() {
        return portletEntityPropertyManager;
    }

    public final void setPortletEntityDataManager(PortletEntityDataManager portletEntityDataManager) {
        this.portletEntityDataManager = portletEntityDataManager;
    }

    public PortletEntityDataManager getPortletEntityDataManager() {
        if (portletEntityDataManager == null) {
            portletEntityDataManager = (PortletEntityDataManager) AppContext.getBean("portletEntityDataManager");
        }
        return portletEntityDataManager;
    }

    public BaseSectionImpl() {
    }

    public String[] getSectionTypes() {
        return sectionTypes;
    }

    public void setSectionType(String[] sectionTypes) {
        this.sectionTypes = sectionTypes;
    }

    public String getSectionCategory() {
        return sectionCategory;
    }

    public void setSectionCategory(String sectionCategory) {
        this.sectionCategory = sectionCategory;
    }

    /**
     * 栏目所属空间类型：个人personal、部门department、单位corporation、集团group、自定义空间custom, 不设定，表示不限制
     * <p>
     * 请采用枚举常量BaseSection.SpaceType
     *
     * @return
     */
    public void setSpaceTypes(String[] spaceTypes) {
        this.spaceTypes = spaceTypes;
    }

    public String[] getSpaceTypes() {
        return spaceTypes;
    }

    /**
     * 是否注册到栏目管理器, 默认注册
     *
     * @return
     */
    public void setIsRegistrer(boolean isRegistrer) {
        this.isRegistrer = isRegistrer;
    }

    /**
     * 是否允许放在窄栏目中，默认不允许
     *
     * @param isAllowedNarrow
     */
    public void setAllowedNarrow(boolean isAllowedNarrow) {
        this.isAllowedNarrow = isAllowedNarrow;
    }

    /**
     * 初始化方法
     */
    public void init() {
    }

    /**
     * 栏目的唯一标示，同时也是Spring Bean定义的Id, 如：
     *
     * <pre>
     * <code>
     * ***-manager.xml
     *
     * &lt;bean id=&quot;pendingSection&quot; class=&quot;com.seeyon.v3x.main.section.PendingSection&quot;&gt;
     *   &lt;property name=&quot;id&quot; ref=&quot;pendingSection&quot; /&gt;
     * &lt;/bean&gt;
     * </code>
     *
     * 特别提示：该值作为栏目的标示将会写到数据库中去，故：
     * 1、不要随意变化
     * 2、要唯一
     * 3、必须由数字、字母、下划线构成
     * </pre>
     *
     * @return 直接返回一个有字符+数字组成的字符串，如：pendingSection
     */
    public abstract String getId();

    /**
     * 栏目名称,需要国际化,直接通过国际化key获取值输出，
     *
     * @param preference Portlet实例的配置参数
     * @return
     */
    public abstract String getName(Map<String, String> preference);

    /**
     * 总数，如果不需要显示总数，就返回null
     *
     * @param preference Portlet实例的配置参数
     * @return
     */
    public abstract Integer getTotal(Map<String, String> preference);

    /**
     * 得到这个栏目里面数据的最后修改时间
     *
     * @param preference
     * @return
     * @see #updatePreference(Map) 需要同时实现，监听更新栏目属性
     */
    public Long getLastModify(Map<String, String> preference) {
        return System.currentTimeMillis();
    }

    /**
     * 取得数量的单位，比如：个、项、条，默认“项”，注意国际化
     *
     * @param preference
     * @return
     */
    public String getTotalUnit(Map<String, String> preference) {
        return null;
    }

    /**
     * 栏目图标，统一放在 /apps_res/v3xmain/images/section下
     *
     * @return 如 /apps_res/v3xmain/images/section/pending.col.gif
     */
    public abstract String getIcon();

    /**
     * 在这里发射数据（PC端）
     *
     * @param preference Portlet实例的配置参数
     * @return
     */
    public abstract BaseSectionTemplete projection(Map<String, String> preference);

    /**
     * 在这里发射数据（移动端）
     *
     * @param preference Portlet实例的配置参数
     * @return
     */
    public BaseSectionTemplete mProjection(Map<String, String> preference) {
        return null;
    }

    public String getHTML(String entityId, String ordinal, String spaceType, String ownerId, Long spaceId) {
        return null;
    }

    /**
     * 设置栏目的排序号，用在配置页面的栏目选择中
     *
     * @param sortId
     */
    public void setSortId(Integer sortId) {
        this.sortId = sortId;
    }

    public Integer getSortId() {
        return sortId;
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
    public boolean isAllowUserUsed(Map<String, String> preference) {
        String singleBoardId = preference.get(PortletPropertyContants.PropertyName.singleBoardId.name());
        return isAllowUserUsed(singleBoardId);
    }

    public boolean isAllowedNarrow() {
        return isAllowedNarrow;
    }

    public boolean isRegistrer() {
        return isRegistrer;
    }

    public String getResourceBundle() {
        return resourceBundle;
    }

    /**
     * 对于空间的权限，是否只读。
     *
     * @param entityId
     * @param spaceType
     * @param ownerId
     * @return
     */
    public boolean isReadOnly(String spaceType, String ownerId) {
        return false;
    }

    /**
     * 国际化资源
     *
     * @param resourceBundle 如：com.seeyon.v3x.resouces.i18n.ApplicationResourceBundle
     */
    public void setResourceBundle(String resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    /**
     * 这个方法是供Ajax Service调用的
     * 2011.2.17 增加panelId 标示页签id
     * 增加传递参数
     */
    public final Map<String, Object> doProjection(String entityId, String ordinal, String spaceType, String ownerId, String x, String y, String width, String panelId, String[] paramKeys, String[] paramValues, String cityName) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            User user = AppContext.getCurrentUser();

            String userAgentFrom = user.getUserAgentFrom();
            boolean isMobile = userAgentFrom != null && (userAgentFrom.equals(Constants.login_useragent_from.iphone.name()) || userAgentFrom.equals(Constants.login_useragent_from.androidphone.name())
                    || userAgentFrom.equals(Constants.login_useragent_from.ipad.name()) || userAgentFrom.equals(Constants.login_useragent_from.androidpad.name())
                    || userAgentFrom.equals(Constants.login_useragent_from.weixin.name()));

            Map<String, String> preference = getPrefenerce(entityId, ordinal, spaceType, ownerId, x, y, width);
            boolean isError = false;
            String name = null;
            if (preference == null || preference.get(PropertyName.sections.name()) == null) {
                isError = true;
            } else {
                preference.put(PropertyName.panelId.name(), panelId);

                if (paramKeys != null && paramKeys.length > 0 && paramValues != null && paramValues.length > 0) {
                    for (int i = 0; i < paramKeys.length; i++) {
                        preference.put(paramKeys[i], paramValues[i]);
                    }
                }
                name = this.getName(preference);
            }
            if (isError || name == null) {
                result.put("error", "not_exists");
                return result;
            }

            Long lastModifyData = this.getLastModify(preference);//栏目数据发生变化
            if (lastModifyData != null) {
                Long lastModifySection = geteTagCacheManager().getETagDate("SECTION", entityId + ":" + ordinal);//栏目属性发生变化
                try {
                    String etag = "S" + user.getId() + "_" + lastModifySection + "_" + lastModifyData;
                    if (WebUtil.checkEtag(WebUtil.getRequest(), WebUtil.getResponse(), etag)) {
                        return null;
                    }
                    WebUtil.writeETag(WebUtil.getRequest(), WebUtil.getResponse(), etag);
                } catch (IOException e) {
                    log.error("", e);
                }
            }
            preference.put("cityName", cityName);
            if (!isMobile) {
                preference.put(PropertyName.height.name(), this.getHeight(preference) + "");
            }

            BaseSectionTemplete c = null;
            if (user.isDefaultGuest()) {
                String cacheKey = entityId + "_" + ordinal;
                //卡片式：换一换； 分类式：切换版块；
                String pageNo = preference.get(PortletPropertyContants.PropertyName.pageNo.name());
                String categoryId = SectionUtils.getSectionProperty("", preference, "categoryId");
                if (Strings.isNotBlank(pageNo) && !"0".equals(pageNo)) {//只缓存第一页
                    c = this.projection(preference);
                } else {
                    //棋盘式需要根据宽度算条数，根据宽度缓存，区隔100
                    String rf = preference.get("rf");
                    if (ChessboardTemplete.RESOLVE_FUNCTION.equals(rf)) {
                        String widthStr = preference.get(PropertyName.sectionWidth.name());
                        if (Strings.isNotBlank(widthStr)) {
                            cacheKey += "_" + Integer.parseInt(widthStr) / 100;
                        }
                    }
                    if (Strings.isNotBlank(categoryId)) {
                        cacheKey += "_" + categoryId;
                    }
                    c = getPortletEntityDataManager().getSectionData(cacheKey);
                    if (c == null) {
                        c = this.projection(preference);
                        getPortletEntityDataManager().putSectionData(cacheKey, c);
                    }
                }
            } else {
                if (isMobile) {
                    c = this.mProjection(preference);
                } else {
                    c = this.projection(preference);
                }
            }


            String isPageLoad = preference.get("pageLoad");
            if (Strings.isBlank(isPageLoad) || !"true".equals(isPageLoad)) {
                Integer total = this.getTotal(preference);
                if (total != null) {
                    result.put("Total", total);

                    String totalUnit = this.getTotalUnit(preference);
                    result.put("TotalUnit", totalUnit);
                }
            }

            result.put("Name", name);
            //周刘成修改
//            if (name.indexOf("待办") != -1) {
//                if(null != c){
//                    MultiRowVariableColumnColTemplete templete = (MultiRowVariableColumnColTemplete) c;
//                    List<MultiRowVariableColumnColTemplete.Row> listRows = templete.getRows();
//                    if(null != listRows && listRows.size() > 0){
//                        for (int i = 0; i < listRows.size(); i++) {
//                            MultiRowVariableColumnColTemplete.Row row = listRows.get(i);
//                            List<MultiRowVariableColumnColTemplete.Cell> rowCells = row.getCells();
//                            MultiRowVariableColumnColTemplete.Cell cell = rowCells.get(1);
//                            MultiRowVariableColumnColTemplete.Cell cellZero = rowCells.get(0);
//                            String time = cell.getCellContentHTML();
//                            String receiveTime = cellZero.getReceiveTimeAll();
//                            if (time.indexOf("今日") != -1) {
//                                cell.setCellContentHTML(time.substring(0,2));
//                                cell.setClassName("primary");
//                            } else {
//                                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//                                LocalDateTime dateTime = LocalDateTime.parse(receiveTime.concat(":00"), df);
//                                //计算日期相差的小时
//                                Duration duration = Duration.between(dateTime, LocalDateTime.now());
//                                Long hour = duration.toHours();
//                                if (hour > 24 && hour < 72) {
//                                    cell.setClassName("yellow");
//                                } else if (hour >= 72) {
//                                    cell.setClassName("red");
//                                } else {
//                                    cell.setClassName("primary");
//                                }
//                            }
//                        }
//                    }
//
//                }
//
//                //周刘成 区分代办
//                result.put("flag", "daiban");
//                result.put("Data", c);
//
//            } else {
//                result.put("flag", "other");
//                result.put("Data", c);
//
//            }
            result.put("preference", preference);
        } catch (Exception e) {
            log.error("", e);
        }

        return result;
    }

    /**
     * 这个方法是供Ajax Service调用的
     *
     * @param entityId
     * @param ordinal
     * @param layoutType
     * @return
     */
    public final Integer doGetTotal(String entityId, String ordinal, String spaceType, String ownerId) {
        Map<String, String> preference = getPrefenerce(entityId, ordinal, spaceType, ownerId, null, null, null);
        try {
            return this.getTotal(preference);
        } catch (Exception e) {
            log.error("", e);
        }

        return null;
    }

    /**
     * @param entityId
     * @param ordinal
     * @param spaceType
     * @param ownerId
     * @return
     */
    public final String doGetTotalUnit(String entityId, String ordinal, String spaceType, String ownerId) {
        Map<String, String> preference = getPrefenerce(entityId, ordinal, spaceType, ownerId, null, null, null);
        try {
            return this.getTotalUnit(preference);
        } catch (Exception e) {
            log.error("", e);
        }

        return null;
    }

    /**
     * 这个方法是供Ajax Service调用的
     *
     * @param entityId
     * @param ordinal
     * @return
     */
    public final String doGetName(String entityId, String ordinal, String spaceType, String ownerId, String spaceId) {
        Map<String, String> preference = getPrefenerce(entityId, ordinal, spaceType, ownerId, null, null, null);
        if (spaceId != null) {
            preference.put(PropertyName.spaceId.name(), spaceId);
        }
        String name = null;
        try {
            name = this.getName(preference);
        } catch (Exception e) {
            log.warn("", e);
        }

        return name;
    }

    /**
     * 这个方法是供Ajax Service调用的
     *
     * @param entityId
     * @param ordinal
     * @return
     */
    public final String doGetName(Map<String, String> preference) {
        //Map<String, String> preference = getPrefenerce(entityId, ordinal, spaceType, ownerId, null, null, null);
        String name = null;
        try {
            name = this.getName(preference);
        } catch (Exception e) {
            log.warn("", e);
        }

        return name;
    }

    /**
     * 获取栏目原始名称
     *
     * @param entityId
     * @param ordinal
     * @param spaceType
     * @param ownerId
     * @return
     */
    public final String doGetBaseName(String entityId, String ordinal, String spaceType, String ownerId) {
        Map<String, String> preference = getPrefenerce(entityId, ordinal, spaceType, ownerId, null, null, null);
        String name = null;
        try {
            name = this.getBaseName(preference);
        } catch (Exception e) {
            log.error("", e);
        }

        return name;
    }

    public Map<String, String> getPrefenerce(String entityId, String ordinal, String spaceType, String ownerId, String x, String y, String width) {
        Map<String, String> props = portletEntityPropertyManager.getPropertys(Long.parseLong(entityId));
        props = PortalConstants.getFragmentProp(props, ordinal);
        //props.put(PropertyName.layoutType.name(), layoutType);
        props.put(PropertyName.spaceType.name(), spaceType);
        props.put(PropertyName.x.name(), x);
        props.put(PropertyName.y.name(), y);
        props.put(PropertyName.width.name(), width);
        props.put(PropertyName.isNarrow.name(), Boolean.toString(PortalCommonUtil.isNarrow(width)));
        props.put(PropertyName.entityId.name(), entityId);//增加Fragment.id。
        props.put(PropertyName.ordinal.name(), ordinal);  //增加在Fragment中的排序

        if (Strings.isNotBlank(ownerId)) {
            props.put(PropertyName.ownerId.name(), ownerId);
        }
        return props;
    }

    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((getId() == null) ? 0 : getId().hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final BaseSectionImpl other = (BaseSectionImpl) obj;
        if (getId() == null) {
            if (other.getId() != null)
                return false;
        } else if (!getId().equals(other.getId()))
            return false;
        return true;
    }

    public int compareTo(BaseSectionImpl o) {
        if (this.sortId == null) {
            return 1;
        }
        if (o.getSortId() == null) {
            return -1;
        }
        return this.getSortId().compareTo(o.getSortId());
    }

    public String getBaseName() {
        return this.baseName;
    }

    public String getBaseName(Map<String, String> preference) {
        return this.getBaseName();
    }

    public List<SectionProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<SectionProperty> properties) {
        this.properties = properties;
        boolean hasBackgroundColor = false;
        for (SectionProperty property : this.properties) {
            for (SectionReference refs : property.getReference()) {
                if (PropertyName.backgroundColor.name().equals(refs.getName())) {
                    log.info("含背景色栏目：" + this.getId());
                    hasBackgroundColor = true;
                    break;
                }
            }
            property.setSectionId(this.getId());
        }
        if (!hasBackgroundColor) {//系统启动时，为所有栏目统一追加背景颜色设置属性
            SectionReference reference = new SectionReferenceImpl();
            reference.setName(PropertyName.backgroundColor.name());
            reference.setSubject("section.name.weather.background.color.label");
            reference.setValueType(11);

            SectionReference[] referenceArr = new SectionReference[1];
            referenceArr[0] = reference;

            SectionProperty property = new SectionPropertyImpl();
            property.setSectionId(this.getId());
            property.setReference(referenceArr);
            if (null == this.properties) {
                this.properties = new ArrayList<SectionProperty>();
            }
            this.properties.add(property);
        }

        //给所有栏目插入元栏目名开始
        SectionReference baseNameRef = new SectionReferenceImpl();
        baseNameRef.setName(PropertyName.baseName.name());
        baseNameRef.setSubject("cannel.base.name.label");
        baseNameRef.setValueType(2);
        baseNameRef.setReadOnly(true);
        String baseName = this.getBaseName();
        baseNameRef.setDefaultValue(baseName);

        SectionReference[] referenceArr = new SectionReference[1];
        referenceArr[0] = baseNameRef;

        SectionProperty baseNameProperty = new SectionPropertyImpl();
        baseNameProperty.setSectionId(this.getId());
        baseNameProperty.setReference(referenceArr);
        baseNameProperty.setReadOnly(true);
        if (null == this.properties) {
            this.properties = new ArrayList<SectionProperty>();
        }
        this.properties.add(0, baseNameProperty);
        //给所有栏目插入元栏目名结束

    }

    @Override
    public List<Map<String, Object>> getDesignDataForAdmin() {
        return null;
    }

    @Override
    public void updatePreference(Map<String, String> preference) {
    }

    @Override
    public boolean isShowTotal() {
        return false;
    }

    @Override
    public String getResolveFunction(Map<String, String> preference) {
        return null;
    }

    @Override
    public int getHeight(Map<String, String> preference) {
        String heightStr = preference.get(PropertyName.height.name());
        int height = 300;
        if (NumberUtils.isDigits(heightStr)) {
            height = Integer.parseInt(heightStr);
        }
        return height;
    }

    @Override
    public String getAiShort(Map<String, String> preference) {
        return "0";
    }

    @Override
    public String getAiShortValue(Map<String, String> preference) {
        return "0";
    }

    @Override
    public boolean isAllowMobileCustomSet() {
        return false;
    }
}
