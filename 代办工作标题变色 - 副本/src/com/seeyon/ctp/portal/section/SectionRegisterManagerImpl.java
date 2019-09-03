/**
 * $Author: zhout $
 * $Rev: 1241 $
 * $Date:: 2012-08-09 17:05:03 +#$:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.portal.section;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.portal.section.util.SectionPortletFunction;
import com.seeyon.ctp.portal.space.manager.PortletEntityPropertyManager;
import com.seeyon.ctp.portal.util.Constants;
import com.seeyon.ctp.portal.util.Constants.SectionType;
import com.seeyon.ctp.portal.util.Constants.SpaceType;
import com.seeyon.ctp.portal.util.PortletPropertyContants.PropertyName;
import com.seeyon.ctp.util.Strings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.Map.Entry;

/**
 * <p>Title: 栏目抽象类，所有的栏目继承该抽象类</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: seeyon.com</p>
 * @since CTP2.0
 */
public class SectionRegisterManagerImpl implements SectionRegisterManager{
	private final Log log = LogFactory.getLog(SectionRegisterManagerImpl.class);

	private static final String DEFAULT_DERACTOR = "A8Standard";

	private static PortletEntityPropertyManager portletEntityPropertyManager;

	/**
	 * 可以在任何空间配置的栏目
	 */
	private List<String> noInSpaceType = new ArrayList<String>();

	/**
	 * 空间类型下的栏目
	 */
	private EnumMap<SpaceType, List<String>> spaceTypeOfSections = new EnumMap<SpaceType, List<String>>(SpaceType.class);

	/**
	 * key - section.id; value-beanId
	 */
	private Map<String, String> sectionId2Bean = new HashMap<String, String>();


	public void init(){
		Map<String, BaseSection> sectionBeans = AppContext.getBeansOfType(BaseSection.class);
		Set<Entry<String, BaseSection>> enities = sectionBeans.entrySet();

		for (Entry<String, BaseSection> entry : enities) {
		    BaseSection section = entry.getValue();

			if(!Strings.isWord(section.getId())){
				log.warn("栏目id[" + section.getId() + "]不合法，必须是由数字、字母、下划线构成");
				continue;
			}

			sectionId2Bean.put(section.getId(), entry.getKey());
			add(section);
		}

	}

	private void add(BaseSection section) {
		String sectionId = section.getId();
		String[] spaceTypes = section.getSpaceTypes();

		if(spaceTypes != null){
			for (String spaceType : spaceTypes) {
				SpaceType type = SpaceType.valueOf(spaceType);
				if(type == null){
					log.warn("栏目[" + sectionId + "]的spaceTypes属性\"" + spaceType + "\"配置不正确.");
					break;
				}

				List<String> t = spaceTypeOfSections.get(type);
				if (t == null) {
					t = new ArrayList<String>();
					spaceTypeOfSections.put(type, t);
				}

				t.add(sectionId);
			}
		}
		else{ //所有空间都可以配置
			noInSpaceType.add(sectionId);
		}
	}

	public BaseSection getSection(String sectionId){
		if("imageNewsNoHeaderSection".equals(sectionId)){
			sectionId= "imageNewsSection";
		}else if("groupImageNewsNoHeaderSection".equals(sectionId)){
			sectionId= "groupImageNewsSection";
		}
		String beanId = sectionId2Bean.get(sectionId);
		if(beanId != null){
			try {
				return (BaseSection) AppContext.getBean(beanId);
			}
			catch (Throwable e) {
			}
		}

		return null;
	}

	public String getSectionBeanId(String sectionId){
		return sectionId2Bean.get(sectionId);
	}

    @SuppressWarnings("unchecked")
    public Map<String, List<String[]>> getSections(SpaceType spaceType) throws BusinessException {
        List<String> list = new ArrayList<String>();
        List<String> ss = spaceTypeOfSections.get(spaceType);
        if (ss != null) {
            list.addAll(ss);
        }
        list.addAll(noInSpaceType);

        if (list.isEmpty()) {
            return Collections.EMPTY_MAP;
        }

        List<BaseSection> baseSections = new ArrayList<BaseSection>();
        User user = AppContext.getCurrentUser();
        boolean isInternal = user.isInternal();
        for (String sectionId : list) {
        	try {
	            BaseSection section = getSection(sectionId);
	            if (section != null && (isInternal || !section.isFilterOut())) {
	                if (section.isAllowUsed()) {
	                    baseSections.add(section);
	                }
	            }
        	} catch (RuntimeException e) {
                log.warn("栏目|" +sectionId + "|异常：", e);
            } catch (Exception e) {
                log.warn("栏目|" +sectionId + "|异常：", e);
            }
        }
        Comparator<BaseSection> comparator = new Comparator<BaseSection>() {
            @Override
            public int compare(BaseSection o1, BaseSection o2) {
                Integer sort1 = o1.getSortId();
                Integer sort2 = o2.getSortId();
                if (sort1 != null && sort2 != null) {
                    return sort1 - sort2;
                } else if (sort1 == null && sort2 != null) {
                    return -1;
                } else if (sort2 == null && sort1 != null) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };
        Collections.sort(baseSections, comparator);

        Map<String, List<String[]>> map = new LinkedHashMap<String, List<String[]>>();
        for (BaseSection section : baseSections) {
        	try {
	            String[] sectionTypes = section.getSectionTypes();

	            if (sectionTypes == null || sectionTypes.length <= 0) {
	                sectionTypes = new String[1];
	                sectionTypes[0] = String.valueOf(SectionType.common);
	            }

	            String name = null;
	            if (section.isRegistrer()) { // 注册栏目才取显示名称

	                    name = section.getName(Collections.EMPTY_MAP);
	            }
	            for (String sectionType : sectionTypes) {
	                List<String[]> t = map.get(sectionType);
	                if (t == null) {
	                    t = new ArrayList<String[]>();
	                    map.put(sectionType, t);
	                }

	                t.add(new String[] { section.getId(), name, section.getSectionCategory() });
	            }
        	} catch (RuntimeException e) {
                log.error("栏目|" + section.getId() + "|异常：", e);
            } catch (Throwable e) {
                log.error("栏目|" + section.getId() + "|异常：", e);
            }
        }

        return map;
    }

	public String getPortletTitle(String portletUniqueName, Locale locale){
		return null;
	}

	public String getPortletDecorator(String portletUniqueName){
		return DEFAULT_DERACTOR;
	}

	public String getSectionPreferences(String sectionId,String spaceType,Map<String,String> preference,String ordinal){
		StringBuilder sb = new StringBuilder();
		if(Strings.isNotBlank(sectionId)){
			sb.append(SectionPortletFunction.printPortletParams(sectionId, spaceType,preference,ordinal));
		}
		return sb.toString();
	}

	public String getFragmentProp(String entityId,String sectionId,String spaceType,Boolean containSection,String ownerId,String ordinal){
	    Map<String, String> props = portletEntityPropertyManager.getPropertys(Long.parseLong(entityId));
        Map<String,String> result = new HashMap<String,String>();
        for(String key : props.keySet()){
            result.put(key, (props.get(key)));
        }
		StringBuilder sb = new StringBuilder();
		if(containSection){
			sb.append(getSectionPreferences(sectionId,spaceType,result,ordinal));
		}
		Map<String,String> properties = SectionPortletFunction.showSectionProperties(Long.parseLong(entityId), spaceType,ownerId,result);
		if(properties != null){
			sb.append("var property = new Properties();\n");
			for(String key : properties.keySet()){
			    String value = properties.get(key);
			    if(key.startsWith("slogan")||key.startsWith(PropertyName.columnsName.name())){
			        value = ResourceUtil.getString(value);
			    }
				sb.append("property.put(\""+key+"\",\""+Functions.escapeJavascript(value)+"\");\n");
			}
		}
		return sb.toString();
	}

	@Override
	public List<String[]> getSections(SpaceType spaceType, long memberId,
                                      long loginAccountId, boolean showBanner) {
		if(spaceType == null){
			return Collections.emptyList();
		}
		spaceType = Constants.parseDefaultSpaceType(spaceType);
		List<String> list = new ArrayList<String>();
		List<String> ss = spaceTypeOfSections.get(spaceType);
		if(ss != null){
			list.addAll(ss);
		}
		list.addAll(noInSpaceType);

		if(list.isEmpty()){
			return Collections.emptyList();
		}

		List<BaseSection> baseSections = new ArrayList<BaseSection>();
		boolean isInternal = AppContext.getCurrentUser().isInternal();
		for (String sectionId : list) {
			BaseSection section = getSection(sectionId);
			if(section != null && (isInternal || !section.isFilterOut()) && (!"banner".equals(sectionId) || !"collaborationRemindSection".equals(sectionId) || showBanner)){
				try {
					if(section.isAllowUsed()){
						baseSections.add(section);
					}
				}
				catch (Exception e) {
					log.warn("", e);
				}
			}
		}

		//Collections.sort(sectionImpls);

		List<String[]> result = new ArrayList<String[]>();
		for (BaseSection section : baseSections) {
			String name = null;
			if(section.isRegistrer()){ //注册栏目才取显示名称
				try {
					name = section.getName(Collections.<String, String> emptyMap());
				}
				catch (Throwable e) {
					log.error("", e);
				}
			}
			result.add(new String[]{section.getId(), name});
		}

		return result;
	}

    @Override
    public List<String[]> getSpaceTypeOfSections(SpaceType spaceType) {
        if (spaceType == null) {
            return Collections.emptyList();
        }

        List<String> list = new ArrayList<String>();
        List<String> ss = spaceTypeOfSections.get(spaceType);
        if (ss != null) {
            list.addAll(ss);
        }
        list.addAll(noInSpaceType);

        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        List<BaseSection> baseSections = new ArrayList<BaseSection>();
        for (String sectionId : list) {
            BaseSection section = getSection(sectionId);
            if (section != null && section.isAllowUsed()) {
                baseSections.add(section);
            }
        }

        Comparator<BaseSection> comparator = new Comparator<BaseSection>() {
            @Override
            public int compare(BaseSection o1, BaseSection o2) {
                Integer sort1 = o1.getSortId();
                Integer sort2 = o2.getSortId();
                if (sort1 != null && sort2 != null) {
                    return sort1 - sort2;
                } else if (sort1 == null && sort2 != null) {
                    return -1;
                } else if (sort2 == null && sort1 != null) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };
        Collections.sort(baseSections, comparator);

        List<String[]> result = new ArrayList<String[]>();
        for (BaseSection section : baseSections) {
            String name = null;
            if (section.isRegistrer()) {
                try {
                    name = section.getName(Collections.<String, String> emptyMap());
                } catch (Throwable e) {
                    log.error("", e);
                }
            }
            result.add(new String[] { section.getId(), name });
        }

        return result;
    }

	public static PortletEntityPropertyManager getPortletEntityPropertyManager() {
		return portletEntityPropertyManager;
	}

	public static void setPortletEntityPropertyManager(PortletEntityPropertyManager portletEntityPropertyManager) {
		SectionRegisterManagerImpl.portletEntityPropertyManager = portletEntityPropertyManager;
	}

}
