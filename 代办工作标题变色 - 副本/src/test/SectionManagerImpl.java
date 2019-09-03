///**
// * $Author:  $
// * $Rev:  $
// * $Date:: #$:
// *
// * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
// *
// * This software is the proprietary information of Seeyon, Inc.
// * Use is subject to license terms.
// */
//package com.seeyon.ctp.portal.section;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.EnumMap;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.lang.StringUtils;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import com.alibaba.fastjson.JSONObject;
//import com.seeyon.ctp.common.AppContext;
//import com.seeyon.ctp.common.authenticate.domain.User;
//import com.seeyon.ctp.common.exceptions.BusinessException;
//import com.seeyon.ctp.common.i18n.ResourceUtil;
//import com.seeyon.ctp.portal.manager.PortalCacheManager;
//import com.seeyon.ctp.portal.po.PortalPagePortlet;
//import com.seeyon.ctp.portal.po.PortalSpaceFix;
//import com.seeyon.ctp.portal.po.PortalSpacePage;
//import com.seeyon.ctp.portal.section.bo.SectionTreeNode;
//import com.seeyon.ctp.portal.section.manager.BaseSectionSelector;
//import com.seeyon.ctp.portal.space.manager.PageManager;
//import com.seeyon.ctp.portal.space.manager.PortletEntityPropertyManager;
//import com.seeyon.ctp.portal.space.manager.SpaceManager;
//import com.seeyon.ctp.portal.util.Constants;
//import com.seeyon.ctp.portal.util.PortletPropertyContants;
//import com.seeyon.ctp.portal.util.PortletPropertyContants.PropertyName;
//import com.seeyon.ctp.portal.util.SpaceFixUtil;
//import com.seeyon.ctp.util.Strings;
//import com.seeyon.ctp.util.UUIDLong;
//import com.seeyon.ctp.util.json.JSONUtil;
//
///**
// * <p>Title: 栏目前端Ajax请求发送实现</p>
// * <p>Description: </p>
// * <p>Copyright: Copyright (c) 2012</p>
// * <p>Company: seeyon.com</p>
// * @since CTP2.0
// */
//public class SectionManagerImpl implements SectionManager{
//
//    private static final Log       log = LogFactory.getLog(SectionManagerImpl.class);
//    private PageManager            pageManager;
//    private SpaceManager           spaceManager;
//    private SectionRegisterManager sectionRegisterManager;
//    private PortalCacheManager			     portalCacheManager;
//    private PortletEntityPropertyManager     portletEntityPropertyManager;
//
//    public void setPortalCacheManager(PortalCacheManager portalCacheManager) {
//        this.portalCacheManager = portalCacheManager;
//    }
//    public void setPageManager(PageManager pageManager) {
//        this.pageManager = pageManager;
//    }
//
//    public void setSpaceManager(SpaceManager spaceManager) {
//        this.spaceManager = spaceManager;
//    }
//
//    public void setSectionRegisterManager(SectionRegisterManager sectionRegisterManager) {
//        this.sectionRegisterManager = sectionRegisterManager;
//    }
//
//    @Override
//    public Map<String, Object> doProjection(Map params) {
//        String sectionBeanId = (String) params.get("sectionBeanId");
//        String entityId = (String) params.get("entityId");
//        String ordinal = (String) params.get("ordinal");
//        String spaceType = (String) params.get("spaceType");
//        String spaceId = (String) params.get("spaceId");
//        String ownerId = (String) params.get("ownerId");
//        String x = String.valueOf(params.get("x"));
//        String y = String.valueOf(params.get("y"));
//        String width = String.valueOf(params.get("width"));
//        String panelId = String.valueOf(params.get("panelId"));
//        String sprint = (String) params.get("sprint");
//        String cityName = (String) params.get("cityName");
//        String rf = (String) params.get("rf");
//        String sectionsStyle = (String) params.get("b_s");
//        String sectionWidth = (String) params.get("sectionWidth");
//        String pageLoad = String.valueOf(params.get("pageLoad"));
//        List<String> paramKeys = (List<String>) params.get("paramKeys");
//        String[] keys = null;
//        if (CollectionUtils.isEmpty(paramKeys)) {
//            paramKeys = new ArrayList<String>();
//        }
//        paramKeys.add("rf");
//        paramKeys.add("sections_style");
//        paramKeys.add("sectionWidth");
//        paramKeys.add("spaceId");
//        paramKeys.add("pageLoad");
//        if (Strings.isNotBlank(sprint)) {
//            paramKeys.add("sprint");
//        }
//        keys = new String[paramKeys.size()];
//        for (int i = 0; i < paramKeys.size(); i++) {
//            keys[i] = paramKeys.get(i);
//        }
//
//        List<String> paramValues = (List<String>) params.get("paramValues");
//        String[] values = null;
//        if (CollectionUtils.isEmpty(paramValues)) {
//            paramValues = new ArrayList<String>();
//        }
//        paramValues.add(rf);
//        paramValues.add(sectionsStyle);
//        paramValues.add(sectionWidth);
//        paramValues.add(spaceId);
//        paramValues.add(pageLoad);
//        if (Strings.isNotBlank(sprint)) {
//            paramValues.add(sprint);
//        }
//        values = new String[paramValues.size()];
//        for (int i = 0; i < paramValues.size(); i++) {
//            values[i] = paramValues.get(i);
//        }
//        if (Strings.isBlank(sectionBeanId)) {
//            return null;
//        }
//        BaseSection baseSection = sectionRegisterManager.getSection(sectionBeanId);
//        return baseSection.doProjection(entityId, ordinal, spaceType, ownerId, x, y, width, panelId, keys, values, cityName);
//    }
//
//    @Override
//    public List<SectionTreeNode> selectSectionTreeNode(Map params) throws BusinessException {
//        String sectionType = (String)params.get("sectionType");
//        String spaceType = (String)params.get("spaceType");
//        String spaceId = (String)params.get("spaceId");
//        String isMulti = (String)params.get("isMulti");
//        String searchWord = (String)params.get("searchWord");
//        //后续有判断要依靠空间id type
//        AppContext.putSessionContext("tempSpaceId",spaceId);
//        AppContext.putSessionContext("tempSpaceType",spaceType);
//        ArrayList<BaseSectionSelector> selectors = portalCacheManager.getSectionsBySelector(sectionType);
//        List<SectionTreeNode> trees = new ArrayList<SectionTreeNode>();
//        if(CollectionUtils.isNotEmpty(selectors)){
//           for(BaseSectionSelector selector : selectors){
//        	   List<SectionTreeNode> tree = new ArrayList<SectionTreeNode>();
//        	   if(Strings.isNotBlank(searchWord)){
//        		   tree = selector.selectSectionTreeDataSearch(spaceType, spaceId, searchWord);
//        	   }else{
//        		   tree = selector.selectSectionTreeData(spaceType, spaceId);
//        	   }
//                if (CollectionUtils.isNotEmpty(tree)) {
//                    for (SectionTreeNode treeNode : tree) {
//                        String sectionName = treeNode.getSectionName();
//                        String sectionId = treeNode.getId();
//                        if (Strings.isBlank(sectionName)) {
//                            log.error("栏目" + sectionId + "名称为空，被移除。");
//                        } else {
//                            if ("true".equals(isMulti)) {
//                                BaseSection sectionMgr = sectionRegisterManager.getSection(treeNode.getSectionBeanId());
//                                if (sectionMgr != null && sectionMgr.isNoHeaderSection()) {
//                                    continue;
//                                }
//                            }else if("portlet".equals(isMulti)){
//                            	BaseSection sectionMgr = sectionRegisterManager.getSection(treeNode.getSectionBeanId());
//                                if (sectionMgr != null && sectionMgr.isNoHeaderSection()) {
//                                	treeNode.setNoHeaderSection(true);
//                                }
//                            }
//                            trees.add(treeNode);
//                        }
//                    }
//                }
//           }
//        }
//        AppContext.removeSessionArrribute("tempSpaceId");
//        AppContext.removeSessionArrribute("tempSpaceType");
//        return trees;
//    }
//
//    @Override
//    public List<SectionTreeNode> selectedSectionTreeNode(Map params) throws BusinessException {
//        String pagePath = (String)params.get("pagePath");
//        String entityId = (String)params.get("entityId");
//        String editKeyId = (String)params.get("editKeyId");
//        String ownerId = (String)params.get("ownerId");
//        String spaceId = (String)params.get("spaceId");
//        List<SectionTreeNode> trees = new ArrayList<SectionTreeNode>();
//        if(Strings.isNotBlank(pagePath)){
//            Long userId = AppContext.currentUserId();
//            List<Map<String,String>> portletProperties = new ArrayList<Map<String,String>>();
//            if(Strings.isNotBlank(editKeyId)){
//                portletProperties = this.pageManager.selectPortletPropertyByPagePath(pagePath, Long.valueOf(editKeyId), userId);
//            }else{
//                portletProperties = this.pageManager.selectPortletPropertyByPagePath(pagePath, UUIDLong.longUUID(), userId);
//            }
//            if(CollectionUtils.isNotEmpty(portletProperties)){
//                for(Map<String,String> portletProperty : portletProperties){
//                    String sectionBeanId = portletProperty.get(PropertyName.sections.name());
//                    String sectionUuidStr = portletProperty.get(PropertyName.sectionUuids.name());
//                    if(Strings.isBlank(sectionBeanId)){
//                    	continue;
//                    }
//                    entityId = portletProperty.get(PropertyName.entityId.name());
//                    String[] sectionBeanIds = sectionBeanId.split(",");
//                    String[] sectionUuids= null;
//    	            if(Strings.isNotBlank(sectionUuidStr)){
//    	            	sectionUuids= sectionUuidStr.split(",");
//    	            }
//    	            String newSectionUuids= "";
//                    String newSectionBeanId = "";
//                    String name = "";
//                    String title = "";
//                    int j=0;
//                    for(int i=0; i < sectionBeanIds.length; i++){
//                        String sectionName = portletProperty.get(PropertyName.columnsName.name()+":"+i);
//                        String singleBoardId = portletProperty.get(PropertyName.singleBoardId.name()+":"+i);
//                        String nameTitle = "";
//                        BaseSection curSection = ((BaseSection)AppContext.getBean(sectionBeanIds[i]));
//                        if(curSection==null||!curSection.isAllowUserUsed(singleBoardId)){
//                            continue;
//                        }
//
//                        Map<String,String> preference = new HashMap<String,String>();
//                        sectionName = Strings.isNotBlank(sectionName) ? ResourceUtil.getString(sectionName) : sectionName;
//                        preference.put(PropertyName.columnsName.name(), sectionName);
//                        preference.put(PropertyName.ownerId.name(), ownerId);
//                        preference.put(PropertyName.spaceId.name(), spaceId);
//                        if(Strings.isBlank(singleBoardId)){
//                            String curSectionName = curSection.getName(preference);
//                            if(Strings.isBlank(curSectionName)){
//                            	continue;
//                            }
//                            //空间成员需要spaceId
//                            Map<String,String> preference1 = new HashMap<String,String>();
//                            preference1.put(PropertyName.spaceId.name(), spaceId);
//                            String baseSectionName = curSection.getBaseName(preference1);
//                            if(Strings.isBlank(baseSectionName)){
//	                        	baseSectionName= sectionName;
//	                        }
//                            sectionName = Strings.getSafeLimitLengthString(curSectionName, 16, "...")+"("+baseSectionName+")";
//                            nameTitle = curSectionName +"("+baseSectionName+")";
//                        }else{
//                            preference.put(PropertyName.singleBoardId.name(), singleBoardId);
//                            String curSectionName = curSection.getName(preference);
//                            if(Strings.isBlank(curSectionName)){
//                            	continue;
//                            }
//                            String baseSectionName = curSection.getBaseName(preference);
//                            if(Strings.isBlank(baseSectionName)){
//	                        	baseSectionName= curSectionName;
//	                        }
//                            sectionName = Strings.getSafeLimitLengthString(curSectionName, 16, "...")+"("+baseSectionName+")";
//                            nameTitle = curSectionName +"("+baseSectionName+")";
//                        }
//                        if(j!=0){
//    	                    newSectionBeanId += ","+sectionBeanIds[i];
//    	                    if(null==sectionUuids){
//    	                    	newSectionUuids += ","+UUIDLong.longUUID();
//    	                    }else{
//    	                    	newSectionUuids += ","+sectionUuids[i];
//    	                    }
//    	                    name +="|" + sectionName;
//    	                    title += "|" + nameTitle;
//    	                }else{
//    	                    newSectionBeanId += sectionBeanIds[i];
//    	                    if(null==sectionUuids){
//    	                    	newSectionUuids += UUIDLong.longUUID();
//    	                    }else{
//    	                    	newSectionUuids += sectionUuids[i];
//    	                    }
//    	                    name +=sectionName;
//    	                    title += nameTitle;
//    	                }
//    	                j++;
//                    }
//                    if(Strings.isNotBlank(name)&&Strings.isNotBlank(newSectionBeanId)){
//                        SectionTreeNode node = new SectionTreeNode();
//                        node.setId(newSectionUuids);
//                        node.setSectionBeanId(newSectionBeanId);
//                        node.setSectionName(name);
//                        node.setEntityId(entityId);
//                        node.setTitle(title);
//                        trees.add(node);
//                    }
//                }
//            }
//        }else if(Strings.isNotBlank(entityId)){
//            Map<String,String> portletProperty = this.pageManager.selectPortletProperty(Long.valueOf(entityId));
//            if(portletProperty != null){
//                String sectionBeanId = portletProperty.get(PropertyName.sections.name());
//                String sectionUuidStr = portletProperty.get(PropertyName.sectionUuids.name());
//                String[] sectionUuids= null;
//	            if(Strings.isNotBlank(sectionUuidStr)){
//	            	sectionUuids= sectionUuidStr.split(",");
//	            }
//                entityId = portletProperty.get(PropertyName.entityId.name());
//                if(null!=sectionBeanId){
//	                String[] sectionBeanIds = sectionBeanId.split(",");
//	                for(int i=0; i < sectionBeanIds.length; i++){
//	                    String sectionName = portletProperty.get(PropertyName.columnsName.name()+":"+i);
//	                    String singleBoardId = portletProperty.get(PropertyName.singleBoardId.name()+":"+i);
//	                    String title = sectionName;
//	                    BaseSection curSection = ((BaseSection)AppContext.getBean(sectionBeanIds[i]));
//	                    if(curSection==null||!curSection.isAllowUserUsed(singleBoardId)){
//	                        continue;
//	                    }
//
//	                    Map<String,String> preference = new HashMap<String,String>();
//	                    sectionName = Strings.isNotBlank(sectionName) ? ResourceUtil.getString(sectionName) : sectionName;
//	                    preference.put(PropertyName.columnsName.name(), sectionName);
//	                    preference.put(PropertyName.ownerId.name(), ownerId);
//	                    if(Strings.isBlank(singleBoardId)){
//	                        String curSectionName = curSection.getName(preference);
//	                        if(Strings.isBlank(curSectionName)){
//	                        	continue;
//	                        }
//	                        String baseSectionName = curSection.getBaseName(Collections.<String, String> emptyMap());
//	                        if(Strings.isBlank(baseSectionName)){
//	                        	baseSectionName= curSectionName;
//	                        }
//	                        sectionName = Strings.getSafeLimitLengthString(curSectionName, 32, "...")+"("+baseSectionName+")";
//	                        title = curSectionName + "("+baseSectionName+")";
//	                    }else{
//	                        preference.put(PropertyName.singleBoardId.name(), singleBoardId);
//	                        String curSectionName = curSection.getName(preference);
//	                        if(Strings.isBlank(curSectionName)){
//	                        	continue;
//	                        }
//	                        String baseSectionName = curSection.getBaseName(preference);
//	                        if(Strings.isBlank(baseSectionName)){
//	                        	baseSectionName= sectionName;
//	                        }
//	                        sectionName = Strings.getSafeLimitLengthString(curSectionName, 32, "...")+"("+baseSectionName+")";
//	                        title = curSectionName + "("+baseSectionName+")";
//	                    }
//	                    SectionTreeNode node = new SectionTreeNode();
//	                    if(null==sectionUuids){
//		                	node.setId(UUIDLong.longUUID()+"");
//		                }else{
//		                	node.setId(sectionUuids[i]);
//		                }
//	                    node.setSectionBeanId(sectionBeanIds[i]);
//	                    node.setSectionName(sectionName);
//	                    node.setTitle(title);
//	                    node.setEntityId(entityId);
//	                    node.setSingleBoardId(singleBoardId);
//	                    node.setOrdinal(String.valueOf(i));
//	                    node.setNoHeaderSection(curSection.isNoHeaderSection());
//	                    trees.add(node);
//	                }
//                }
//            }
//        }
//        return trees;
//    }
//
//    @Override
//    public Map<String,Object> generateSpacePortlets(Map<String,String> spaceEditRequest) throws BusinessException{
//    	Map<String,Object> resultDataMap= new HashMap<String, Object>();
//    	User user = AppContext.getCurrentUser();
//    	String currentSpaceEditData= spaceEditRequest.get("currentSpaceEditData");
//    	Map<String,String> spaceEditRequestParams= JSONUtil.parseJSONString(currentSpaceEditData, Map.class);
//
//        String pagePath = spaceEditRequestParams.get("pagePath");
//        PortalSpacePage page = pageManager.getPage(pagePath);
//		if(null==page){
//			 throw new BusinessException("管理员禁止您自定义空间信息!");
//		 }
//        String spaceId = spaceEditRequestParams.get("space_id");
//        String spaceType = spaceEditRequestParams.get("spaceType");
//        String entityId = spaceEditRequestParams.get("entityId");
//        String ownerId = spaceEditRequestParams.get("ownerId");
//        String decoration = spaceEditRequestParams.get("decorationId");
//        String size = spaceEditRequestParams.get("size");
//        //全部清空栏目时，size为0
//        if (Strings.isNotBlank(size)) {
//            int length = Integer.parseInt(size);
//            String[] sectionIds = new String[length];
//            String[] sectionUuids = new String[length];
//            String[] sectionNames = new String[length];
//            String[] entityIds = new String[length];
//            String[] ordinals = new String[length];
//            String[] sections_styles = new String[length];
//            String[] sections_bigtitles = new String[length];
//            String[] sections_showbigtitles = new String[length];
//
//            Map<String,String[]> sectionXYMap= new HashMap<String, String[]>();
//            List<Map<String,String>> allPortletProperties = new ArrayList<Map<String, String>>();
//            if (length > 0) {
//                for (int i = 0; i < length; i++) {
//                    sectionIds[i] = spaceEditRequestParams.get("sections_" + i);
//                    sectionUuids[i] = spaceEditRequestParams.get("sectionUuids_" + i);
//                    sectionNames[i] = spaceEditRequestParams.get("columnsName_" + i);
//                    entityIds[i] = spaceEditRequestParams.get("entityId_" + i);
//                    String portletId = entityIds[i];
//                    if(Strings.isNotBlank(portletId)){
//    	                String x = spaceEditRequestParams.get("x_" + i);
//    	                String y = spaceEditRequestParams.get("y_" + i);
//    	                sectionXYMap.put(portletId, new String[]{x,y});
//                    }
//                    ordinals[i] = spaceEditRequestParams.get("ordinal_" + i);
//                    sections_styles[i]= spaceEditRequestParams.get("sections_style_" + i);
//                    sections_bigtitles[i]= spaceEditRequestParams.get("sections_bigtitle_" + i);
//                    sections_showbigtitles[i]= spaceEditRequestParams.get("sections_showbigtitle_" + i);
//
//                    String mySectionIds = sectionIds[i];
//                    String[] mySectionIdsArr= mySectionIds.split(",");
//                    Map<String,String> myPortletProperties = new HashMap<String, String>();
//                    for(int j=0;j<mySectionIdsArr.length;j++){
//                    	String mySectionId= mySectionIdsArr[j];//第i个portlet下的第j个栏目页签beanid
//
//                    	String myColumnsNameKey= "columnsName_"+i+"_"+j;//第i个portlet下的第j个栏目页签名称
//                    	String myColumnsNameKey_Inner= "columnsName_"+j;
//
//                    	String mySingleBoardIdKey= "singleBoardId_"+i+"_"+j;//第i个portlet下的第j个栏目页签singleBoardId
//                    	String mySingleBoardIdKey_Inner= "singleBoardId_"+j;
//
//                    	String myPropertyLengthKey= "property_"+i+"_"+j;
//                    	String myPropertyLengthKey_Inner= "property_"+j;
//
//                    	String myColumnsNameValue = spaceEditRequestParams.get(myColumnsNameKey);
//                    	String mySingleBoardIdValue = spaceEditRequestParams.get(mySingleBoardIdKey);
//                    	String myPropertyLengthValue= spaceEditRequestParams.get(myPropertyLengthKey);
//                    	int myPropertyLength = Integer.parseInt(myPropertyLengthValue);
//
//                    	myPortletProperties.put(myColumnsNameKey_Inner, myColumnsNameValue);
//                    	myPortletProperties.put(mySingleBoardIdKey_Inner, mySingleBoardIdValue);
//                    	myPortletProperties.put(myPropertyLengthKey_Inner, myPropertyLength+"");
//
//                    	for(int k=0;k<myPropertyLength;k++){
//                    		String myPropertyKey_Key= "property_"+i+"_"+j+"_"+k+"_key";
//                    		String myPropertyKey_Key_Inner= "property_"+j+"_"+k+"_key";
//
//                    		String myPropertyValue_Key= "property_"+i+"_"+j+"_"+k+"_value";
//                    		String myPropertyValue_Key_Inner= "property_"+j+"_"+k+"_value";
//
//                    		String myPropertyKey_Value= spaceEditRequestParams.get(myPropertyKey_Key);
//                    		String myPropertyValue_Value= spaceEditRequestParams.get(myPropertyValue_Key);
//
//                    		myPortletProperties.put(myPropertyKey_Key_Inner, myPropertyKey_Value);
//                    		myPortletProperties.put(myPropertyValue_Key_Inner, myPropertyValue_Value);
//                    	}
//                    }
//                    allPortletProperties.add(i,myPortletProperties);
//                }
//            }
//            List<PortalPagePortlet> childPortlets= pageManager.generatePortlets(pagePath,spaceId,spaceType,ownerId,sectionXYMap,
//            		sectionIds,sectionUuids,sections_styles,sections_bigtitles,sections_showbigtitles, sectionNames, entityIds,allPortletProperties,decoration);
//        	resultDataMap.put("childPortlets", childPortlets);
//        	List<SectionTreeNode> selectedSectionTreeNode = pageManager.generateSectionTreeNodes(true,"",spaceId, entityId, ownerId, childPortlets);
//        	resultDataMap.put("selectedSectionTreeNode", selectedSectionTreeNode);
//        }
//        return resultDataMap;
//    }
//
//    @Override
//	public Map<String, Object> deleteSectionFromPortlet(Map<String, String> spaceEditRequest)
//			throws BusinessException {
//    	Map<String,Object> resultDataMap= new HashMap<String, Object>();
//    	String removedIndexStr= spaceEditRequest.get("removedIndex");
//    	String ownerId= spaceEditRequest.get("ownerId");
//    	int removedIndex= Integer.parseInt(removedIndexStr);
//    	String portletJsonData= spaceEditRequest.get("portletJsonData");
//    	JSONObject childPortletJSONObject= JSONUtil.parseJSONString(portletJsonData, JSONObject.class);
//
//    	String portletId= childPortletJSONObject.getString("portletId");
//
//    	Long entityId= Long.parseLong(portletId);
//
//    	String parentId= childPortletJSONObject.getString("parentId");
//    	String y= childPortletJSONObject.getString("y");
//    	String x= childPortletJSONObject.getString("x");
//    	int column= Integer.parseInt(y);
//    	int row= Integer.parseInt(x);
//
//    	PortalPagePortlet childrenPortlet = new PortalPagePortlet();
//    	childrenPortlet.setId(entityId);
//		childrenPortlet.setType(PortletPropertyContants.Type.portlet.name());
//		childrenPortlet.setLayoutColumn(column);
//		childrenPortlet.setLayoutRow(row);
//		childrenPortlet.setName("seeyon::sectionPortlet");
//		childrenPortlet.setParentId(Long.parseLong(parentId));
//
//    	JSONObject childPortletPorperties= childPortletJSONObject.getJSONObject("properties");
//
//    	String sections= childPortletPorperties.getString(PropertyName.sections.name());
//    	String sectionUuid= childPortletPorperties.getString(PropertyName.sectionUuids.name());
//
//    	String sections_style= childPortletPorperties.getString(PropertyName.sections_style.name());
//
//    	String sections_bigtitle= childPortletPorperties.getString(PropertyName.sections_bigtitle.name());
//
//    	String sections_showbigtitle= childPortletPorperties.getString(PropertyName.sections_showbigtitle.name());
//
//
//    	String[] sectionsArr= sections.split(",");
//    	String[] sectionUuidArr= null;
//    	if(Strings.isNotBlank(sectionUuid)){
//    		sectionUuidArr= sectionUuid.split(",");
//    	}
//    	String newSections= "";
//    	String newSectionUuids= "";
//    	Map<Integer,Integer> sectionPropertyIndexMap= new HashMap<Integer, Integer>();
//        int m=0;
//        for(int i=0; i<sectionsArr.length; i++){
//            if(i!=removedIndex){
//            	newSections = newSections + sectionsArr[i]+",";
//            	if(null==sectionUuidArr){
//            		newSectionUuids = newSectionUuids +UUIDLong.longUUID()+",";
//            	}else{
//            		newSectionUuids = newSectionUuids +sectionUuidArr[i]+",";
//            	}
//                sectionPropertyIndexMap.put(i, m);
//                m++;
//            }
//        }
//        newSections = newSections.substring(0,newSections.lastIndexOf(","));
//        newSectionUuids= newSectionUuids.substring(0,newSectionUuids.lastIndexOf(","));
//        Map<String,String> newProperties = new HashMap<String,String>();
//        newProperties.put(PropertyName.sections.name(), newSections);
//        newProperties.put(PropertyName.sectionUuids.name(), newSectionUuids);
//        newProperties.put(PropertyName.sections_style.name(), sections_style);
//        newProperties.put(PropertyName.sections_bigtitle.name(), sections_bigtitle);
//        newProperties.put(PropertyName.sections_showbigtitle.name(), sections_showbigtitle);
//    	for (int j = 0; j < sectionsArr.length; j++) {
//    		String sectionId = sectionsArr[j];
//    		if(j==removedIndex){
//    			continue;
//    		}
//    		int newIndex= sectionPropertyIndexMap.get(j);
//    		String sectionName= childPortletPorperties.getString("columnsName_"+j);
//    		newProperties.put("columnsName_"+newIndex, sectionName);
//
//			String singleBoardId= childPortletPorperties.getString("singleBoardId_"+j);
//			newProperties.put("singleBoardId_"+newIndex, singleBoardId);
//
//			String propertyLengthStr= childPortletPorperties.getString("property_"+j);
//			int propertyLength= 0;
//			if(Strings.isNotBlank(propertyLengthStr)){
//				propertyLength= Integer.parseInt(propertyLengthStr);
//			}
//			for(int k=0;k<propertyLength;k++){
//				String propertyKey_key= "property_"+j+"_"+k+"_key";
//				String propertyKey= childPortletPorperties.getString(propertyKey_key);
//
//				String propertyValue_Key= "property_"+j+"_"+k+"_value";
//				String propertyValue= childPortletPorperties.getString(propertyValue_Key);
//
//				newProperties.put("property_"+newIndex+"_"+k+"_key", propertyKey);
//				newProperties.put("property_"+newIndex+"_"+k+"_value", propertyValue);
//			}
//			newProperties.put("property_"+newIndex, propertyLengthStr);
//		}
//    	childrenPortlet.getExtraMap().putAll(newProperties);
//
//    	resultDataMap.put("childPortlet", childrenPortlet);
//        List<PortalPagePortlet> childPortlets= new ArrayList<PortalPagePortlet>();
//        childPortlets.add(childrenPortlet);
//    	List<SectionTreeNode> selectedSectionTreeNode = pageManager.generateSectionTreeNodes(true,"","", "", ownerId, childPortlets);
//    	resultDataMap.put("selectedSectionTreeNode", selectedSectionTreeNode);
//		return resultDataMap;
//	}
//
//    @Override
//	public Map<String, Object> generateSectionsToPortlet(Map<String, String> spaceEditRequest)
//			throws BusinessException {
//    	Map<String,Object> resultDataMap= new HashMap<String, Object>();
//    	User user = AppContext.getCurrentUser();
//    	String currentSpaceEditData= spaceEditRequest.get("currentSpaceEditData");
//    	Map<String,String> spaceEditRequestParams= JSONUtil.parseJSONString(currentSpaceEditData, Map.class);
//
//        String pagePath = spaceEditRequestParams.get("pagePath");
//        PortalSpacePage page = pageManager.getPage(pagePath);
//		if(null==page){
//			 throw new BusinessException("管理员禁止您自定义空间信息!");
//		 }
//        String spaceId = spaceEditRequestParams.get("space_id");
//        String spaceType = spaceEditRequestParams.get("spaceType");
//        String portletIdStr = spaceEditRequestParams.get("portletId");
//        Long portletId= null;
//        if(Strings.isNotBlank(portletIdStr)){
//        	portletId= Long.parseLong(portletIdStr);
//        }
//        String ownerId = spaceEditRequestParams.get("ownerId");
//        String decoration = spaceEditRequestParams.get("decorationId");
//        String size = spaceEditRequestParams.get("size");
//        String editKeyId = spaceEditRequestParams.get("editKeyId");
//        String yStr = spaceEditRequestParams.get("y");
//        String xStr = spaceEditRequestParams.get("x");
//        String swidth = spaceEditRequestParams.get("swidth");//portlet宽度
//        int y=0;
//        if(Strings.isNotBlank(yStr)){
//        	y= Integer.parseInt(yStr);
//        }
//        int x= 0;
//        if(Strings.isNotBlank(xStr)){
//        	x= Integer.parseInt(xStr);
//        }
//        int width= 0;
//        if(Strings.isNotBlank(swidth)){
//        	width= Integer.parseInt(swidth);
//        }
//        if (Strings.isNotBlank(size)) {
//        	int length = Integer.parseInt(size);
//            String sectionIds = spaceEditRequestParams.get("sectionIds");
//            String sectionUuids = spaceEditRequestParams.get("sectionUuids");
//            String sections_style = spaceEditRequestParams.get("sections_style");
//            String sections_bigtitle = spaceEditRequestParams.get("sections_bigtitle");
//            String sections_showbigtitle = spaceEditRequestParams.get("sections_showbigtitle");
//            String[] sectionNames = new String[length];
//            String[] singleBoards = new String[length];
//            String[] entityIds = new String[length];
//            String[] ordinals = new String[length];
//            String[] sections_styles = new String[length];
//            String[] sections_bigtitles = new String[length];
//            String[] sections_showbigtitles = new String[length];
//            Map<String,String> properties = new HashMap<String,String>();
//            for (int i = 0; i < length; i++) {
//                sectionNames[i] = spaceEditRequestParams.get("columnsName_" + i);
//                singleBoards[i] = spaceEditRequestParams.get("singleBoardId_" + i);
//                entityIds[i] = spaceEditRequestParams.get("entityId_" + i);
//                ordinals[i] = spaceEditRequestParams.get("ordinal_" + i);
//
//                String propertyLengthKey= "property_"+i;
//                String propertyLength = spaceEditRequestParams.get(propertyLengthKey);
//                if("0".equals(propertyLength)){
//                	properties.put(propertyLengthKey, propertyLength);
//                }else{
//                    int propLength = Integer.parseInt(propertyLength);
//                    for(int j=0; j<propLength; j++){
//                    	String keyId= "property_"+i+"_"+j+"_key";
//                        String key = spaceEditRequestParams.get(keyId);
//                        String valueId= "property_"+i+"_"+j+"_value";
//                        String value = spaceEditRequestParams.get(valueId);
//                        properties.put(keyId, key);
//                        properties.put(valueId, value);
//                    }
//                    properties.put(propertyLengthKey, propertyLength);
//                }
//            }
//            properties.put(PortletPropertyContants.PropertyName.width.name(), swidth);
//            //编辑状态添加多频道
//            PortalPagePortlet childPortlet= pageManager.generateSectionsToPortlet(pagePath,spaceId,spaceType,ownerId,portletId,y,x,
//            		sectionIds, sectionUuids,sections_style,sections_bigtitle,sections_showbigtitle,sectionNames,
//            		singleBoards,ordinals, properties, user.getId());
//            resultDataMap.put("childPortlet", childPortlet);
//            List<PortalPagePortlet> childPortlets= new ArrayList<PortalPagePortlet>();
//            childPortlets.add(childPortlet);
//        	List<SectionTreeNode> selectedSectionTreeNode = pageManager.generateSectionTreeNodes(true,"",spaceId, portletIdStr, ownerId, childPortlets);
//        	resultDataMap.put("selectedSectionTreeNode", selectedSectionTreeNode);
//        }
//		return resultDataMap;
//	}
//
//	@Override
//	public List<SectionTreeNode> selectAsyncSectionTreeNode(Map params)
//			throws BusinessException {
//		String sectionType = (String)params.get("sectionType");
//        String spaceType = (String)params.get("spaceType");
//        String spaceId = (String)params.get("spaceId");
//        String isMulti = (String)params.get("isMulti");
//        String nodeId = (String)params.get("nodeId");
//        ArrayList<BaseSectionSelector> selectors = portalCacheManager.getSectionsBySelector(sectionType);
//        List<SectionTreeNode> trees = new ArrayList<SectionTreeNode>();
//        if(CollectionUtils.isNotEmpty(selectors)){
//           for(BaseSectionSelector selector : selectors){
//               List<SectionTreeNode> tree = selector.selectSectionTreeData(spaceType, spaceId, nodeId);
//               if(CollectionUtils.isNotEmpty(tree)){
//                   for(SectionTreeNode treeNode : tree){
//                       String sectionName = treeNode.getSectionName();
//                       String sectionId = treeNode.getId();
//                       if(Strings.isBlank(sectionName)){
//                           log.error("栏目"+sectionId+"名称为空，被移除。");
//                       }else{
//                           if("true".equals(isMulti)&&"banner".equals(treeNode.getSectionBeanId())){
//                                   continue;
//                           }else{
//                               trees.add(treeNode);
//                           }
//                       }
//                   }
//               }
//           }
//        }
//        return trees;
//	}
//
//	@Override
//	public boolean isThisSpaceExist(String pagePath) {
//		User user = AppContext.getCurrentUser();
//		if(user.isAdmin()){
//			return true;
//		}
//		PortalSpaceFix space = spaceManager.getSpaceFix(pagePath);
//		if(space!=null){
//			SpaceFixUtil util = new SpaceFixUtil(space.getExtAttributes());
//			if(util.isAllowdefined() && space.getState() == Constants.SpaceState.normal.ordinal()){
//				return true;
//			}else{
//				return false;
//			}
//		}else{
//			return false;
//		}
//	}
//
//	@Override
//	public String[][] getPortletTotals(Map<String,String> params) throws BusinessException {
//		String pagePath= params.get("pagePath");
//		String portletId= params.get("portletId");
//		String tabStr= params.get("tab");
//		int tabIndex= Integer.parseInt(tabStr);
//		Long entityId= Long.parseLong(portletId);
//		Map<String, String> props = portletEntityPropertyManager.getPropertys(entityId);
//		String sections = props.get(PortletPropertyContants.PropertyName.sections.name());
//		boolean isNotNull = StringUtils.isNotBlank(sections) && !"undefined".equals(sections);
//		if (isNotNull) {
//			EnumMap<PortletPropertyContants.PropertyName, String>  pageParams = spaceManager.getPortletEntityProperty(pagePath);
//			if(pageParams == null){
//				return null;
//			}
//			String spaceTypeStr = pageParams.get(PortletPropertyContants.PropertyName.spaceType);
//			String spaceEntityId = pageParams.get(PortletPropertyContants.PropertyName.ownerId);
//			String[] sectionIds = sections.split(",");
//			String[][] totalArr= new String[sectionIds.length][2];
//			for (int i = 0; i < sectionIds.length; i++) {
//				String sectionId = sectionIds[i];
//				if(StringUtils.isBlank(sectionId)){
//					continue;
//				}
//				BaseSection sectionMgr = sectionRegisterManager.getSection(sectionId);
//				if(null==sectionMgr){
//					log.warn("sectionId:="+sectionId);
//					continue;
//				}
//				String singleBoardId = props.get("singleBoardId:"+i);
//				if(!sectionMgr.isAllowUserUsed(singleBoardId)){
//					log.warn("singleBoardId:="+singleBoardId);
//					continue;
//				}
//				if(i==tabIndex){//第一个页签的总数不需要查
//					totalArr[i]= new String[]{"0",""};
//					continue;
//				}
//
//				Integer total = sectionMgr.doGetTotal(portletId, String.valueOf(i), spaceTypeStr, spaceEntityId);
//				String TotalUnit = sectionMgr.doGetTotalUnit(portletId, String.valueOf(i), spaceTypeStr, spaceEntityId);
//				TotalUnit= (TotalUnit == null ? "" : TotalUnit);
//				String totalStr= "";
//				if(null!=total){
//					totalStr= total.toString();
//				}
//				totalArr[i]= new String[]{totalStr,TotalUnit};
//			}
//			return totalArr;
//		}
//		return null;
//	}
//
//	public String getUuid(){
//		return UUIDLong.longUUID()+"";
//	}
//
//	public PortletEntityPropertyManager getPortletEntityPropertyManager() {
//		return portletEntityPropertyManager;
//	}
//	public void setPortletEntityPropertyManager(PortletEntityPropertyManager portletEntityPropertyManager) {
//		this.portletEntityPropertyManager = portletEntityPropertyManager;
//	}
//}
