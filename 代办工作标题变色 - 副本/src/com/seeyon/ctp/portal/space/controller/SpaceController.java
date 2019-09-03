/**
 * $Author: wangchw $
 * $Rev: 50719 $
 * $Date:: 2015-07-13 15:04:49 +#$:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.portal.space.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONObject;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.authenticate.domain.UserHelper;
import com.seeyon.ctp.common.cache.etag.ETagCacheManager;
import com.seeyon.ctp.common.constants.LayoutConstants;
import com.seeyon.ctp.common.constants.ProductEditionEnum;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.fileupload.util.FileUploadUtil;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.portal.engine.model.VPortalObj;
import com.seeyon.ctp.portal.manager.GlobalConfigManager;
import com.seeyon.ctp.portal.manager.PortalCacheManager;
import com.seeyon.ctp.portal.manager.PortalManager;
import com.seeyon.ctp.portal.po.PortalPagePortlet;
import com.seeyon.ctp.portal.po.PortalPageSecuritymodel;
import com.seeyon.ctp.portal.po.PortalSet;
import com.seeyon.ctp.portal.po.PortalSpaceFix;
import com.seeyon.ctp.portal.po.PortalSpacePage;
import com.seeyon.ctp.portal.section.BaseSection;
import com.seeyon.ctp.portal.space.decorations.PortalDecorationManager;
import com.seeyon.ctp.portal.space.manager.PageManager;
import com.seeyon.ctp.portal.space.manager.PortletEntityPropertyManager;
import com.seeyon.ctp.portal.space.manager.SpaceManager;
import com.seeyon.ctp.portal.util.Constants;
import com.seeyon.ctp.portal.util.Constants.PageSecurityModelShowType;
import com.seeyon.ctp.portal.util.Constants.SectionType;
import com.seeyon.ctp.portal.util.Constants.SpaceType;
import com.seeyon.ctp.portal.util.PortalConstants;
import com.seeyon.ctp.portal.util.PortalConstants.EditModel;
import com.seeyon.ctp.portal.util.PortletPropertyContants;
import com.seeyon.ctp.portal.util.PortletPropertyContants.PropertyName;
import com.seeyon.ctp.portal.util.SpaceFixUtil;
import com.seeyon.ctp.util.EnumUtil;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;

/**
 * <p>Title: 首页空间控制器类</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: seeyon.com</p>
 * @since CTP2.0
 */
public class SpaceController extends BaseController {
    private SpaceManager                 spaceManager;
    private PageManager                  pageManager;
    private FileManager                  fileManager;
    private PortletEntityPropertyManager portletEntityPropertyManager;
    private PortalCacheManager           portalCacheManager;
    private GlobalConfigManager          globalConfigManager;
    private PortalManager                portalManager;
    private OrgManager					 orgManager;
    private ETagCacheManager             eTagCacheManager;

    public void setSpaceManager(SpaceManager spaceManager) {
        this.spaceManager = spaceManager;
    }

    public void setPageManager(PageManager pageManager) {
        this.pageManager = pageManager;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void setPortletEntityPropertyManager(PortletEntityPropertyManager portletEntityPropertyManager) {
        this.portletEntityPropertyManager = portletEntityPropertyManager;
    }

    public void setPortalCacheManager(PortalCacheManager portalCacheManager) {
        this.portalCacheManager = portalCacheManager;
    }

    public void setGlobalConfigManager(GlobalConfigManager globalConfigManager) {
        this.globalConfigManager = globalConfigManager;
    }

    public void setPortalManager(PortalManager portalManager) {
        this.portalManager = portalManager;
    }

    public void seteTagCacheManager(ETagCacheManager eTagCacheManager) {
        this.eTagCacheManager = eTagCacheManager;
    }

    /**
     * 空间模板管理主页面（上下布局）
     * @return
     * @throws BusinessException
     */
    @SuppressWarnings("rawtypes")
    public ModelAndView spacePageMain(HttpServletRequest request, HttpServletResponse response)
            throws BusinessException {
        ModelAndView mav = new ModelAndView("ctp/portal/space/spacePageMain");
        FlipInfo fi = new FlipInfo();
        fi.setSortField("sort");
        fi = pageManager.selectDefaultSpacePage(fi, new HashMap());
        request.setAttribute("ffmytable", fi);
        return mav;
    }

    /**
     * 空间模板导入
     * @throws IOException
     */
    public ModelAndView importPage(HttpServletRequest request, HttpServletResponse response) throws BusinessException,
            IOException {
        byte[] fileBytes = fileManager.getFileBytes(Long.parseLong(request.getParameter("fileid")), new Date());
        response.setCharacterEncoding("utf-8");
        pageManager.transImportPage(new String(fileBytes,"UTF-8"));
        return null;
    }

    /**
     * 空间模板导出
     */
    public ModelAndView exportPage(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        String pageIds = request.getParameter("pageIds");
        if (pageIds == null || pageIds.trim().length() == 0) {
            throw new BusinessException("pageIds为空!");
        }
        String[] pageIdArray = pageIds.split(",");
        List<Long> pageIdList = new ArrayList<Long>();
        try {
	        for(String pageId : pageIdArray){
	        	pageIdList.add(Long.valueOf(pageId));
	        }
        } catch(NumberFormatException e){
        	throw new BusinessException("pageId不是整型! " + e);
        }
        String xmlStr = pageManager.transExportPage(pageIdList);
        ByteArrayInputStream bis = null;
        try {
            bis = new ByteArrayInputStream(xmlStr.getBytes("UTF-8"));
            request.setAttribute("filename", "spacePage.xml");
            return FileUploadUtil.downLoadStream(request, response, bis);
        } catch (Exception e) {
            throw new BusinessException(e);
        } finally {
            try {
                if(bis != null){
                    bis.close();
                }
            } catch (IOException e) {
                throw new BusinessException(e);
            }
        }
    }

    /**
     * 空间管理主页面
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @CheckRoleAccess(roleTypes = { Role_NAME.AccountAdministrator,Role_NAME.GroupAdmin})
    public ModelAndView spaceMain(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        String spaceCategory=request.getParameter("spaceCategory");
        ModelAndView view = new ModelAndView("ctp/portal/ctpDesigner/vPortalSet");
        if(Strings.isBlank(spaceCategory)){
        	spaceCategory = "pc";
        }
        long entityId=0l;
		if(AppContext.isGroupAdmin()){
			entityId = OrgConstants.GROUPID;
		}else{
			entityId = AppContext.currentAccountId();
		}
		String userChoose = portalCacheManager.getPortalGlobalConfigFromCache(PortalConstants.USER_CHOOSE_MASTER_PORTAL, entityId,AppContext.currentAccountId());
		String masterPortal = portalCacheManager.getPortalGlobalConfigFromCache(PortalConstants.MASTER_PORTAL, entityId,AppContext.currentAccountId());
        if(userChoose != null && !"".equals(userChoose)){
        	view.addObject("userChoose", userChoose);
        }else{
        	view.addObject("userChoose", "0");
        }
        if(masterPortal != null && !"".equals(masterPortal)){
        	view.addObject("masterPortal", masterPortal);
        }else{
        	view.addObject("masterPortal", "0");
        }
        view.addObject("spaceCategory", spaceCategory);
		return view;
    }

    public ModelAndView vPortalSpaceTab(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        ModelAndView mav = new ModelAndView("ctp/portal/space/vPortalSpaceTab");
        return mav;
    }

    /**
     * 空间管理主页面
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @CheckRoleAccess(roleTypes = { Role_NAME.AccountAdministrator,Role_NAME.GroupAdmin,Role_NAME.BusinessDesigner})
    public ModelAndView vPortalSpaceMain(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        ModelAndView mav = new ModelAndView("ctp/portal/space/vPortalSpaceMain");
        User user = AppContext.getCurrentUser();
        Integer productId = SystemProperties.getInstance().getIntegerProperty("system.ProductId");
        if (ProductEditionEnum.a6s.ordinal() == productId.intValue()) {
            mav.addObject("productId", "a6s");
        } else {
            mav.addObject("productId", "");
        }
        String spaceCategory=request.getParameter("spaceCategory");
        String from = request.getParameter("from");//0:个人可使用列表  1:可以管理的列表
        String openFrom= request.getParameter("openFrom");//0:个人可使用列表  1:可以管理的列表 portalManage
        String portalId = request.getParameter("portalId");
        Long accountId = user.getAccountId();

        mav.addObject("from", from);
        mav.addObject("openFrom", openFrom);
        mav.addObject("accountId", accountId);
        mav.addObject("isAdministrator", user.isAdministrator());
        mav.addObject("isGroupAdmin", user.isGroupAdmin());
        if(Strings.isNotBlank(spaceCategory)){
        	mav.addObject("spaceCategory", spaceCategory);//是什么空间,m3,微协同,vjoin等等
        }
        mav.addObject("portalId", portalId);//门户的id用来查询该门户下的空间

        boolean canSetDefaultSpace = false;
        if (Strings.isNotBlank(portalId) && !"mobile".equals(spaceCategory)) {
            PortalSet portal = portalManager.getPortalById(Long.parseLong(portalId));
            if (portal.getPortalType() == 0 && portal.getSysinit() == 1) {
                canSetDefaultSpace = true;
            }
        }
        if(!user.isAdmin()){
        	canSetDefaultSpace= false;
        }else{
        	from= "1";
        }
        mav.addObject("canSetDefaultSpace", canSetDefaultSpace);//是否允许默认空间设置
        mav.addObject("securityType", from);

        mav.addObject("showToolBar", true);
        Map params = new HashMap();
        params.put("accountId", String.valueOf(accountId));
        params.put("portalId", portalId);
        params.put("spaceCategory", spaceCategory);
        params.put("securityType", from);
        params.put("openFrom", openFrom);
        FlipInfo fi = new FlipInfo();
        fi.setSortField("psf.sortId,psf.id");
        fi = spaceManager.selectSpace(fi, params);
        request.setAttribute("ffmytable", fi);
        return mav;
    }
    /**
     * 前端用户空间列表
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ModelAndView vPortalSpaceMainForPerson(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
    	User user = AppContext.getCurrentUser();
    	String from = request.getParameter("from");//0:个人可使用列表  1:可以管理的列表
        String openFrom= request.getParameter("openFrom");//0:个人可使用列表  1:可以管理的列表 portalManage
    	if(user.isAdmin() || (Strings.isNotBlank(from) && "1".equals(from) && Strings.isNotBlank(openFrom) && "portalManage".equals(openFrom))){
    		return this.vPortalSpaceMain(request, response);
    	}
        ModelAndView mav = new ModelAndView("ctp/portal/space/vPortalSpaceMain");
        String portalType= request.getParameter("portalType");//0:个人可使用列表  1:可以管理的列表
        Integer productId = SystemProperties.getInstance().getIntegerProperty("system.ProductId");
        if (ProductEditionEnum.a6s.ordinal() == productId.intValue()) {
            mav.addObject("productId", "a6s");
        } else {
            mav.addObject("productId", "");
        }
        String spaceCategory=request.getParameter("spaceCategory");

        String portalId = request.getParameter("portalId");
        Long accountId = user.getAccountId();
        mav.addObject("accountId", accountId);
        mav.addObject("isAdministrator", user.isAdministrator());
        mav.addObject("isGroupAdmin", user.isGroupAdmin());
        if(Strings.isNotBlank(spaceCategory)){
        	mav.addObject("spaceCategory", spaceCategory);//是什么空间,m3,微协同,vjoin等等
        }
        mav.addObject("portalId", portalId);//门户的id用来查询该门户下的空间

        boolean canSetDefaultSpace = false;
        if (Strings.isNotBlank(portalId)) {
            PortalSet portal = portalManager.getPortalById(Long.parseLong(portalId));
            if (portal.getPortalType() == 0 && portal.getSysinit() == 1) {
                canSetDefaultSpace = true;
            }
        }
        if(!user.isAdmin()){
        	canSetDefaultSpace= false;
        }
        mav.addObject("canSetDefaultSpace", canSetDefaultSpace);//是否允许默认空间设置


        Map params = new HashMap();
        params.put("accountId", String.valueOf(accountId));
        params.put("portalId", portalId);
        params.put("spaceCategory", spaceCategory);
        params.put("portalType", portalType);

        if("0".equals(from)){
        	mav.addObject("securityType", 0);
        	params.put("securityType", 0);
        }else if("1".equals(from)){
        	mav.addObject("securityType", 1);
        	params.put("securityType", 1);
        }
        mav.addObject("showToolBar", false);
        mav.addObject("portalType", portalType);
        FlipInfo fi = new FlipInfo();
        fi.setSortField("psf.sortId,psf.id");
        fi = spaceManager.selectSpace(fi, params);
        request.setAttribute("ffmytable", fi);
        return mav;
    }

    /**
     * 空间新建、修改
     */
    public ModelAndView spaceEdit(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        ModelAndView mv = new ModelAndView("ctp/portal/space/spaceEdit");
        User user = AppContext.getCurrentUser();
        String editKeyId = request.getParameter("editKeyId");
		if (Strings.isBlank(editKeyId)) {
			if (user.isAdministrator() || user.isGroupAdmin()) {

			} else {
				String spaceId=request.getParameter("spaceId");
				if(Strings.isNotBlank(spaceId)){
					boolean canEdit=spaceManager.canEditThisSpace(Long.parseLong(spaceId));
					if(!canEdit){return null;}
				}else{
					return null;
				}
			}
		}
        Integer productId = SystemProperties.getInstance().getIntegerProperty("system.ProductId");
        Map<String, String> spaceTypeMap = new LinkedHashMap<String, String>();
        if (user.isGroupAdmin()) {
            if (productId.intValue() == ProductEditionEnum.government.ordinal() || productId.intValue() == ProductEditionEnum.governmentgroup.ordinal()) {
                spaceTypeMap.put(Constants.DEFAULT_PUBLIC_GROUP_PAGE_PATH, "space.default.public_custom_org.label");
            } else {
                spaceTypeMap.put(Constants.DEFAULT_PUBLIC_GROUP_PAGE_PATH, "space.default.public_custom_group.label");
            }
        } else if (ProductEditionEnum.a6s.ordinal() != productId) {
            spaceTypeMap.put(Constants.DEFAULT_CUSTUM_PERSONAL, "space.default.personal_custom.label");
            spaceTypeMap.put(Constants.DEFAULT_CUSTOM_PAGE_PATH, "space.default.custom.label");
            spaceTypeMap.put(Constants.DEFAULT_PUBLIC_PAGE_PATH, "space.default.public_custom.label");
        }
        request.setAttribute("selectableSpaceTypes", spaceTypeMap);
        request.setAttribute("allLayout", PortalDecorationManager.getAllLayoutType());
        request.setAttribute("layoutTypes", LayoutConstants.lagoutToDecorations);
        return mv;
    }

    /**
     * 选择空间模板
     */
    @SuppressWarnings("rawtypes")
    public ModelAndView selectSpacePage(HttpServletRequest request, HttpServletResponse response)
            throws BusinessException {
        ModelAndView mav = new ModelAndView("ctp/portal/space/selectSpacePage");
        User user = AppContext.getCurrentUser();
        Map params = new HashMap();
        String pathValue=request.getParameter("pathValue");
        List<String> paths = new ArrayList<String>();
        String row=null;
        if(user.isGroupAdmin()){
            String public_group_path = Constants.DEFAULT_PUBLIC_GROUP_PAGE_PATH;
            paths.add(public_group_path);
            if(!StringUtil.checkNull(pathValue)){
            	if(Constants.DEFAULT_PUBLIC_GROUP_PAGE_PATH.equals(pathValue)){
            		row="0";
            	}
            }
        }else{
            String personal_custom_path = Constants.DEFAULT_CUSTUM_PERSONAL;
            String custom_path = Constants.DEFAULT_CUSTOM_PAGE_PATH;
            String public_custom_path = Constants.DEFAULT_PUBLIC_PAGE_PATH;
            paths.add(personal_custom_path);
            paths.add(custom_path);
            paths.add(public_custom_path);
            if(!StringUtil.checkNull(pathValue)){
            	if(Constants.DEFAULT_CUSTUM_PERSONAL.equals(pathValue)){
            		row="0";
            	}else if(Constants.DEFAULT_CUSTOM_PAGE_PATH.equals(pathValue)){
            		row="2";
            	}else if(Constants.DEFAULT_PUBLIC_PAGE_PATH.equals(pathValue)){
            		row="1";
            	}
            }
        }
        params.put("paths", paths);
        FlipInfo fi = pageManager.selectSpacePage(new FlipInfo(), params);
        request.setAttribute("ffmytable", fi);
        mav.addObject("row",row);
        return mav;
    }

    /**
     * 添加栏目选择器
     */
    @SuppressWarnings("rawtypes")
    public ModelAndView portletSelector(HttpServletRequest request, HttpServletResponse response)
            throws BusinessException {
        ModelAndView modelAndView = new ModelAndView("ctp/portal/space/portletSelector");

        String spaceTypeStr = request.getParameter("spaceType");
        String spaceId = request.getParameter("spaceId");
        String pagePath = request.getParameter("pagePath");
        String entityId = request.getParameter("entityId");
        String editKeyId = request.getParameter("editKeyId");
        String ownerId = request.getParameter("ownerId");
        String isMulti = request.getParameter("isMulti");
        modelAndView.addObject("editKeyId", editKeyId);
        modelAndView.addObject("entityId", entityId);
        modelAndView.addObject("pagePath", pagePath);
        modelAndView.addObject("spaceType", spaceTypeStr);
        modelAndView.addObject("spaceId", spaceId);
        modelAndView.addObject("ownerId", ownerId);
        modelAndView.addObject("isMulti", isMulti);

        SpaceType spaceType = SpaceType.valueOf(request.getParameter("spaceType"));

        List<SectionType> spaceSectionTypes = new ArrayList<SectionType>();
        Map<String, List<String[]>> type2Sections = new LinkedHashMap<String, List<String[]>>();
        if (spaceType != null) {
            spaceType = Constants.parseDefaultSpaceType(spaceType);
//            spaceType=SpaceType.personal;
            spaceSectionTypes = Constants.getSpaceSectionTypes(spaceType);
        }

        modelAndView.addObject("allSpaceSectionTypes", Constants.getAllSpaceSectionTypes());
        Integer productId = SystemProperties.getInstance().getIntegerProperty("system.ProductId");
        if (productId.intValue() == ProductEditionEnum.a6s.ordinal()) {//a6s下不显示扩展栏目
            spaceSectionTypes.remove(SectionType.forum);
        }

        modelAndView.addObject("spaceSectionTypes", spaceSectionTypes);

        List<String[]> allSections = new ArrayList<String[]>();
        for (List<String[]> sections : type2Sections.values()) {
            allSections.addAll(sections);
        }

        return modelAndView;
    }

    /**
     * 添加栏目到空间
     * @throws BusinessException,Exception
     */
    public ModelAndView updateSpacePortlets(HttpServletRequest request, HttpServletResponse response)
            throws BusinessException, Exception {
        User user = AppContext.getCurrentUser();
        JSONObject req = readerJSON(request);

        String pagePath = req.getString("pagePath");
        String decoration = req.getString("decorationId");
            String size = req.getString("size");
            //全部清空栏目时，size为0
            if (Strings.isNotBlank(size)) {
                int length = Integer.parseInt(size);
                String[] sectionIds = new String[length];
                String[] sectionUuids = new String[length];
                String[] sectionNames = new String[length];
                String[] singleBoards = new String[length];
                String[] entityIds = new String[length];
                String[] ordinals = new String[length];
                List<Map<String,String>> properties = new ArrayList<Map<String, String>>();
                if (length > 0) {
                    for (int i = 0; i < length; i++) {
                        sectionIds[i] = req.getString("sections_" + i);
                        sectionUuids[i] = req.getString("sectionUuids_" + i);
                        sectionNames[i] = req.getString("columnsName_" + i);
                        singleBoards[i] = req.getString("singleBoardId_" + i);
                        entityIds[i] = req.getString("entityId_" + i);
                        ordinals[i] = req.getString("ordinal_" + i);
                        String propertyLength = req.getString("property_"+i);
                        if("0".equals(propertyLength)){
                            properties.add(i, null);
                        }else{
                            Map<String,String> prop = new HashMap<String, String>();
                            int propLength = Integer.parseInt(propertyLength);
                            for(int j=0; j<propLength; j++){
                                String key = req.getString("property_"+i+"_"+j+"_key");
                                String value = req.getString("property_"+i+"_"+j+"_value");
                                prop.put(key, value);
                            }
                            properties.add(i,prop);
                        }
                    }
                }
                response.setContentType("application/text;charset=UTF-8");
                response.setCharacterEncoding("UTF-8");
                PrintWriter out = response.getWriter();
                //编辑状态数据更新
                if(req.has("editKeyId")){
                    String editKeyId = req.getString("editKeyId");
                    if (Strings.isNotBlank(editKeyId)) {
                    	editKeyId= Strings.escapeJavascript(editKeyId);
                        request.setAttribute("editKeyId", editKeyId);
                        pageManager.addPortlet(pagePath, sectionIds,sectionUuids, sectionNames, singleBoards, entityIds, ordinals,properties,
                                editKeyId, user.getId(), decoration);
                        out.write(editKeyId);
                        out.flush();
                    }
                }else{
                //前端显示状态数据更新
                    String spaceId = req.getString("space_id");
                    PortalSpaceFix parentFix = spaceManager.getSpaceFix(Long.valueOf(spaceId));
                    PortalSpaceFix spaceFix = spaceManager.transCreatePersonalDefineSpace(user.getId(), user.getLoginAccount(), Long.valueOf(spaceId));
                    pageManager.transAddPortletByFront(spaceFix.getPath(), sectionIds, sectionNames, singleBoards, entityIds, ordinals,properties,
                            user.getId(), decoration,parentFix.getPath());
                    out.write(spaceFix.getPath());
                    out.flush();
                }
            }
        return null;
    }

    /**
     * 添加section到fragment
     * @throws BusinessException,Exception
     */
    public ModelAndView updateSectionsToFragment(HttpServletRequest request, HttpServletResponse response)
            throws BusinessException, Exception {
        User user = AppContext.getCurrentUser();
        JSONObject req = readerJSON(request);
        String editKeyId = req.getString("editKeyId");
        String showState = req.getString("showState");
        //fragmentId
        String entityId = req.getString("entityId");
        String size = req.getString("size");
        String pagePath= "";
        String spaceId= "";
        if (Strings.isNotBlank(size)) {
            int length = Integer.parseInt(size);
            String sectionIds = req.getString("sectionIds");
            String sectionUuids = req.getString("sectionUuids");
            String[] sectionNames = new String[length];
            String[] singleBoards = new String[length];
            String[] entityIds = new String[length];
            String[] ordinals = new String[length];
            List<Map<String,String>> properties = new ArrayList<Map<String, String>>();
            for (int i = 0; i < length; i++) {
                sectionNames[i] = req.getString("columnsName_" + i);
                singleBoards[i] = req.getString("singleBoardId_" + i);
                entityIds[i] = req.getString("entityId_" + i);
                ordinals[i] = req.getString("ordinal_" + i);
                String propertyLength = req.getString("property_"+i);
                if("0".equals(propertyLength)){
                    properties.add(i, null);
                }else{
                    Map<String,String> prop = new HashMap<String, String>();
                    int propLength = Integer.parseInt(propertyLength);
                    for(int j=0; j<propLength; j++){
                        String key = req.getString("property_"+i+"_"+j+"_key");
                        String value = req.getString("property_"+i+"_"+j+"_value");
                        prop.put(key, value);
                    }
                    properties.add(i,prop);
                }
            }
            //编辑状态添加多频道
            if (!EditModel.show.name().equals(showState)&&Strings.isNotBlank(editKeyId)) {
                request.setAttribute("editKeyId", editKeyId);
                pageManager.updateSectionsToFragment(Long.valueOf(entityId), sectionIds,sectionUuids, sectionNames, singleBoards,
                        entityIds, ordinals,properties, user.getId());
            }else{
            	//显示状态添加多频道
                String oldSpaceIdStr  = req.getString("spaceId");
                Long oldSpaceId= Long.parseLong(oldSpaceIdStr);
                PortalSpaceFix spaceFix = spaceManager.transAddPortletToSectionByFront(oldSpaceId, Long.valueOf(entityId), sectionIds,sectionUuids, sectionNames, singleBoards, entityIds, ordinals, properties);
                if(spaceFix.getId().longValue()!=oldSpaceId.longValue()){
                	spaceId= spaceFix.getId().toString();
                	pagePath= spaceFix.getPath();
                }
            }
            response.setContentType("application/text;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            JSONObject obj = new JSONObject();
            obj.put("pagePath", pagePath);
            obj.put("spaceId", spaceId);
            out.write(obj.toString());
            out.flush();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected JSONObject readerJSON(HttpServletRequest request) throws Exception {
        JSONObject jsonObject = new JSONObject();
        Map parameterMap = request.getParameterMap();
        // 通过循环遍历的方式获得key和value并set到JSONObject中
        Iterator paIter = parameterMap.keySet().iterator();
        while (paIter.hasNext()) {
            String key = paIter.next().toString();
            String[] values = (String[]) parameterMap.get(key);
            jsonObject.accumulate(key, values[0]);
        }
        return jsonObject;
    }

    /**
     * 个人用户更新栏目坐标信息
     */
    public ModelAndView updateLayoutIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {

        JSONObject jsonObj = readerJSON(request);
        User user = AppContext.getCurrentUser();
        String pagePath = jsonObj.getString("pagePath");

        String size = jsonObj.getString("size");
        int length = Integer.parseInt(size);
        List<PortalPagePortlet> fragments = new ArrayList<PortalPagePortlet>();
        for (int i = 0; i < length; i++) {
            String sectionId = jsonObj.getString("sectionId_" + i);
            String x = jsonObj.getString("x_" + i);
            String y = jsonObj.getString("y_" + i);
            PortalPagePortlet frag = new PortalPagePortlet();
            frag.setId(Long.valueOf(sectionId));
            frag.setLayoutRow(Integer.valueOf(x));
            frag.setLayoutColumn(Integer.valueOf(y));
            fragments.add(frag);
        }
        response.setContentType("application/text;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        if(jsonObj.has("editKeyId")){
            String editKeyId = jsonObj.getString("editKeyId");
            if (Strings.isNotBlank(editKeyId)) {
            	editKeyId= Strings.escapeJavascript(editKeyId);
                request.setAttribute("editKeyId", editKeyId);
                pageManager.updateLayoutIndex(pagePath, fragments, editKeyId, user.getId());
                if (pagePath != null) {
                    out.write(editKeyId);
                }
                out.flush();
            }
        }else{
            String spaceId = jsonObj.getString("space_id");
            PortalSpaceFix spaceFix = spaceManager.transCreatePersonalDefineSpace(user.getId(), user.getLoginAccount(), Long.valueOf(spaceId));
            pageManager.updatePortletLayoutIndex(spaceFix.getPath(), fragments);
            out.write(spaceFix.getPath());
            out.flush();
        }
        return null;
    }
    public ModelAndView changeFrontLayout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String decorationId = request.getParameter("decorationId");
        String spaceId = request.getParameter("spaceId");
        User user = AppContext.getCurrentUser();
        //获取个性化空间
        PortalSpaceFix spaceFix = spaceManager.transCreatePersonalDefineSpace(user.getId(), user.getLoginAccount(), Long.valueOf(spaceId));
        //更新空间布局数据
        PortalSpacePage page = pageManager.getPage(spaceFix.getPath());
        page.setDefaultLayoutDecorator(decorationId);
        pageManager.updatePage(page);

        response.setContentType("application/text;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.write(spaceFix.getPath());
        out.flush();
        return null;
    }
    /**
     * 管理员用户更新栏目坐标信息,在updateSpace方法中调用
     */
    public void updateLayoutIndex(HttpServletRequest request, String pagePath) throws Exception {
        User user = AppContext.getCurrentUser();
        String jsonData = request.getParameter("jsonData");
        if (Strings.isNotBlank(jsonData)) {
            JSONObject jsonObj = new JSONObject(jsonData);
            //String pagePath = jsonObj.getString("pagePath");
            String size = jsonObj.getString("size");
            int length = Integer.parseInt(size);
            List<PortalPagePortlet> fragments = new ArrayList<PortalPagePortlet>();
            for (int i = 0; i < length; i++) {
                String sectionId = jsonObj.getString("sectionId_" + i);
                String x = jsonObj.getString("x_" + i);
                String y = jsonObj.getString("y_" + i);
                PortalPagePortlet frag = new PortalPagePortlet();
                frag.setId(Long.valueOf(sectionId));
                frag.setLayoutRow(Integer.valueOf(x));
                frag.setLayoutColumn(Integer.valueOf(y));
                fragments.add(frag);
            }
            String editKeyId = jsonObj.getString("editKeyId");
            if (Strings.isNotBlank(editKeyId)) {
                request.setAttribute("editKeyId", editKeyId);
                pageManager.updateLayoutIndex(pagePath, fragments, editKeyId, user.getId());
            }
        }
    }

    /**
     * 个人类型空间恢复默认
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView toDefaultPersonalSpace(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String spaceId = request.getParameter("spaceId");
        String spaceType = request.getParameter("spaceType");
        String editKeyId = request.getParameter("editKeyId");
        String decorationId = request.getParameter("decorationId");
        User user = AppContext.getCurrentUser();
        if (Strings.isNotBlank(editKeyId)) {
            /**
             * TODO:空间类型判断，个人类型空间可以进行前端恢复默认
             */
                //if ("leader".equals(spaceType) || "personal".equals(spaceType) || "outer".equals(spaceType)
                //    || "personal_custom".equals(spaceType)) {
                String decoration = spaceManager.transToDefaultPersonalSpace(user.getId(), user.getAccountId(), Long.valueOf(spaceId),spaceType);
                if (Strings.isNotBlank(decoration)) {
                    decorationId = decoration;
                }
            //}
            editKeyId= Strings.escapeJavascript(editKeyId);
            decorationId= Strings.escapeJavascript(decorationId);
            JSONObject obj = new JSONObject();
            obj.put("decorationId", decorationId);
            obj.put("editKeyId", editKeyId);
            obj.put("toDefault", "toDefault");
            response.setContentType("application/text;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.write(obj.toString());
            out.flush();
            out.close();
        }
        return null;
    }

    /**
     * 修改栏目属性
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView updateProperty(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String entityId = request.getParameter("entityId");
        Map<String, String> properties = PortalConstants.doPortletEntityProperty(request, entityId);
        String personalPagePath = null;
        String showState = request.getParameter("showState");
        String tabIndex = request.getParameter("tab");
        User user = AppContext.getCurrentUser();
        JSONObject obj = new JSONObject();
        String spaceId= request.getParameter("spaceId");
        String spaceType= request.getParameter("spaceType");
        String spacePath= request.getParameter("spacePath");
        String y= request.getParameter("y");
        String x= request.getParameter("x");
        PortalPagePortlet portlet= null;
        String spaceOwnerId= "";
        if(EditModel.show.name().equals(showState)){
            PortalSpaceFix fix = spaceManager.getSpaceFix(Long.valueOf(spaceId));
            if(fix!=null&&fix.getParentId()==null){
                /**
                  * 更改个人类型空间个性化创建方法
                  */
                PortalSpaceFix personalFix = spaceManager.transCreatePersonalDefineSpace(user.getId(), user.getLoginAccount(), Long.valueOf(spaceId));
                personalPagePath = personalFix.getPath();
                portlet= portalCacheManager.getCurrentPortlet(personalPagePath, y, x);
                if(portlet != null){
                    entityId = portlet.getId().toString();
                }
                spaceId= personalFix.getId().toString();
                obj.put("pagePath", personalPagePath);
                obj.put("spaceId", spaceId);
                obj.put("result", "customed");
                portalCacheManager.updateSpaceByMember();
                spaceOwnerId= personalFix.getEntityId().toString();
            }else{
                obj.put("result", "true");
                portlet= portalCacheManager.getCurrentPortlet(fix.getPath(), y, x);
                spaceOwnerId= fix.getEntityId().toString();
            }
            portletEntityPropertyManager.save(Long.parseLong(entityId), properties,tabIndex);
        }else{
            String editKeyId = request.getParameter("editKeyId");
            if (Strings.isNotBlank(editKeyId)) {
                request.setAttribute("editKeyId", editKeyId);
                spaceManager.updateProperty(Long.parseLong(entityId), properties, tabIndex, Long.valueOf(editKeyId),
                        user.getId());
                editKeyId= Strings.escapeJavascript(editKeyId);
                obj.put("editKeyId", editKeyId);
                obj.put("result", "true");
                portlet= portalCacheManager.getCurrentPortlet(spacePath, y, x);
            }
            EnumMap<PortletPropertyContants.PropertyName, String>  pageParams = spaceManager.getPortletEntityProperty(spacePath);
			if(pageParams != null){
				spaceOwnerId= pageParams.get(PortletPropertyContants.PropertyName.ownerId);
			}
        }

        Map<String, String> preference = PortalConstants.getFragmentProp(properties, tabIndex);
        String sectionBeanId = preference.get(PropertyName.sections.name());
        if (Strings.isNotBlank(sectionBeanId)) {
            String[] sectionBeanIds = sectionBeanId.split(",");
            for (int j = 0; j < sectionBeanIds.length; j++) {
                String str = sectionBeanIds[j];
                eTagCacheManager.updateETagDate("SECTION", entityId + ":" + j);
                BaseSection baseSection = (BaseSection) AppContext.getBean(str);
                if (baseSection != null) {
                    baseSection.updatePreference(preference);
                }
            }
        }
        //返回portlet的摘要数据
        if(!NumberUtils.isNumber(spaceType)){
        	spaceType= SpaceType.valueOf(spaceType).ordinal()+"";
        }
        Set<String> sectionTemplateSet= new HashSet<String>();
        String portletPropertiesJson= portalCacheManager.getPortletPropertiesJson(y,sectionTemplateSet,spaceId, spaceType, portlet,spaceOwnerId);
        obj.put("y", y);
        obj.put("x", x);
        String sectionTemplateSetStr= Strings.join(sectionTemplateSet, ",");
        if(sectionTemplateSetStr.startsWith(",")){
			sectionTemplateSetStr= sectionTemplateSetStr.substring(1);
		}
		if(sectionTemplateSetStr.endsWith(",")){
			sectionTemplateSetStr= sectionTemplateSetStr.substring(0,sectionTemplateSetStr.length()-1);
		}
        obj.put("tplCode", sectionTemplateSetStr);
        obj.put("portletPropertiesJson", portletPropertiesJson);
        /**
         * 这里不能加response头设置
         */
        //设置编码
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.write(obj.toString());
        out.flush();
        out.close();
        return null;
    }

	public ModelAndView updateShortcutPortlet(HttpServletRequest request, HttpServletResponse response) throws Exception {

    	JSONObject jsonObj = readerJSON(request);
        String entityId = jsonObj.getString("entityId");
        String spaceId = jsonObj.getString("spaceId");
        String showState = jsonObj.getString("showState");
        String property = jsonObj.getString("property");
        Map<String, String> properties = PortalConstants.doPortletEntityProperty(request, entityId);

        properties.put("sections","shortCutSection");
        properties.put("source_value:0",property);
        String personalPagePath = null;
        String tabIndex = "0";
        User user = AppContext.getCurrentUser();
        JSONObject obj = new JSONObject();

        if(EditModel.show.name().equals(showState)){
            PortalSpaceFix fix = spaceManager.getSpaceFix(Long.valueOf(spaceId));
            if(fix!=null&&fix.getParentId()==null){
                /**
                  * 更改个人类型空间个性化创建方法
                  */
                PortalSpaceFix personalFix = spaceManager.transCreatePersonalDefineSpace(user.getId(), user.getLoginAccount(), Long.valueOf(spaceId));
                personalPagePath = personalFix.getPath();
                String x = jsonObj.getString("x");
                String y = jsonObj.getString("y");
                Map<String,Map<String,PortalPagePortlet>> fragments = spaceManager.getFragments(personalPagePath);
                if(fragments != null){
                 Map<String,PortalPagePortlet> columnF = fragments.get(y);
                 if(fragments != null){
                     PortalPagePortlet row = columnF.get(x);
                     if(row != null){
                         entityId = row.getId().toString();
                     }
                 }
                }
                obj.put("pagePath", personalPagePath);
                obj.put("spaceId", personalFix.getId() + "");
                obj.put("result", "customed");
            }else{
                obj.put("result", "true");
            }
            portletEntityPropertyManager.save(Long.parseLong(entityId), properties,tabIndex);
            eTagCacheManager.updateETagDate("SECTION", entityId + ":" + tabIndex);
        }
        /**
         * 这里不能加response头设置
         */
        PrintWriter out = response.getWriter();
        out.write(obj.toString());
        out.flush();
        out.close();
        return null;
    }
    /**
     * 栏目删除
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView deleteFrament(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String entityId = request.getParameter("entityId");
        String oldSpaceId = request.getParameter("spaceId");
        String personalPagePath = request.getParameter("pagePath");
        String showState = request.getParameter("showState");
        String index = request.getParameter("index");
        //编辑状态，缓存删除
        String editKeyId = request.getParameter("editKeyId");
        User user = AppContext.getCurrentUser();
        String pagePath= "";
        String spaceId= "";
        if("show".equals(showState)){
            PortalSpaceFix fix = spaceManager.getSpaceFix(Long.valueOf(oldSpaceId));
            if(fix!=null&&fix.getParentId()==null){
                // 更改个人类型空间个性化创建方法
                PortalSpaceFix personalFix = spaceManager.transCreatePersonalDefineSpace(user.getId(), user.getLoginAccount(), Long.valueOf(oldSpaceId));
                pagePath = personalFix.getPath();
                personalPagePath= personalFix.getPath();
                spaceId = personalFix.getId().toString();
                String x = request.getParameter("x");
                String y = request.getParameter("y");
                Map<String,Map<String,PortalPagePortlet>> fragments = spaceManager.getFragments(pagePath);
                if(fragments != null){
                    Map<String,PortalPagePortlet> columnF = fragments.get(y);
                    if(fragments != null){
                        PortalPagePortlet row = columnF.get(x);
                        if(row != null){
                            entityId = row.getId().toString();
                        }
                    }
                }
            }
            pageManager.deleteFragment(Long.valueOf(entityId),personalPagePath,Integer.parseInt(index));
        }else{
	        if (Strings.isNotBlank(editKeyId)) {
	            request.setAttribute("editKeyId", editKeyId);
	            pageManager.deleteFragment(Long.valueOf(entityId), oldSpaceId, editKeyId, user.getId(),Integer.parseInt(index));
	        }
        }
        if(Strings.isNotBlank(editKeyId)){
        	editKeyId= Strings.escapeJavascript(editKeyId);
        }
        JSONObject obj = new JSONObject();
        obj.put("editKeyId", editKeyId);
        obj.put("pagePath", pagePath);
        obj.put("spaceId", spaceId);
        //设置编码
        response.setContentType("application/text;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.write(obj.toString());
        out.flush();
        out.close();
        return null;
    }

    public ModelAndView personalSpaceSetting(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        ModelAndView mav = new ModelAndView("ctp/portal/space/personalSpaceSetting");
        User user = AppContext.getCurrentUser();
        Long userId = user.getId();
        Long loginAccountId = user.getLoginAccount();

        List<String[]> list = new ArrayList<String[]>();
        //已选空间：当前用户设置显示的访问空间
        List<String[]> spaceList = spaceManager.getSpaceSort(userId, loginAccountId, user.getLocale(), false, null);
        for (String[] space : spaceList) {
            if (("0").equals(space[4])
                    && (("0").equals(space[6]) || ("5").equals(space[6]) || ("9").equals(space[6]) || ("10").equals(space[6]) || ("13").equals(space[6]) || ("14").equals(space[6])
                            || ("15").equals(space[6]) || ("16").equals(space[6]))) {
                String[] sp = new String[6];
                sp[0] = space[0];
                sp[1] = space[1];
                sp[2] = space[3];
                String spaceId = space[0];
                PortalSpaceFix _space = spaceManager.getSpaceFix(Long.valueOf(spaceId));
                if (_space != null) {
                    SpaceFixUtil fixUtil = new SpaceFixUtil(_space.getExtAttributes());
                    boolean isAllowdefined = fixUtil.isAllowdefined();
                    if (isAllowdefined) {
                        sp[4] = "true";
                    } else {
                        sp[4] = "false";
                    }
                    boolean canAccess = _space.getState() != Constants.SpaceState.invalidation.ordinal();
                    if (canAccess) {
                        String pagePath = _space.getPath();
                        sp[3] = pagePath;
                        PortalSpacePage page = pageManager.getPage(pagePath);
                        if (page != null) {
                            sp[5] = page.getDefaultLayoutDecorator();
                        }
                        list.add(sp);
                    }
                }
            }
        }

        String themSpaceFlag = SystemProperties.getInstance().getProperty("portal.themSpaceFlag");
        if ("true".equals(themSpaceFlag)) {
            List<String[]> list2 = spaceManager.getSettingThemSpace();
            if (Strings.isNotEmpty(list2)) {
                list.addAll(list2);
            }
        }

        if (list.size() > 0) {
            String[] defaultSpace = list.get(0);
            mav.addObject("allowed", defaultSpace[4]);
            mav.addObject("defaultSpaceId", defaultSpace[0]);
            mav.addObject("defaultLayout", defaultSpace[5]);
        }

        return mav.addObject("spaceList", list);
    }

    public ModelAndView personalSpaceEdit(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        ModelAndView mav = new ModelAndView("ctp/portal/space/personalSpaceEdit");
        mav.addObject("allLayout", PortalDecorationManager.getAllLayoutType());
        mav.addObject("layoutTypes", LayoutConstants.lagoutToDecorations);
        return this.vPortalSpaceMain(request, response);
    }

    /**
     * 前端保存
     */
    public ModelAndView updateSpaceByFront(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String editKeyId = request.getParameter("editKeyId");
        String spaceId = request.getParameter("spaceId");
        String decoration = request.getParameter("decorationId");
        if(Strings.isNotBlank(decoration)){
        	decoration= Strings.escapeJavascript(decoration);
        }
        String toDefault = request.getParameter("toDefault");
        User user = AppContext.getCurrentUser();
        JSONObject obj = new JSONObject();
        if(Strings.isNotBlank(editKeyId)){
            spaceId = spaceManager.updateSpaceByCache(Long.valueOf(editKeyId),user.getId(),Long.valueOf(spaceId),decoration,toDefault);
            Map retMap = spaceManager.selectSpaceById(Long.valueOf(spaceId));
            obj.put("spaceId", spaceId);
            obj.put("path", retMap.get("path"));
            obj.put("decoration", decoration);
          //刷新用户控件列表缓存
            List<String[]> spaces = spaceManager.getSpaceSort(user.getId(), user.getLoginAccount(), user.getLocale(), false, null);
            if (spaces != null) {
            	 UserHelper.setSpaces(spaces);
            }
        }
        //设置编码
        response.setContentType("application/text;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.write(obj.toString());
        out.flush();
        out.close();
        return null;
    }
    /**
     * 空间模版-控制空间授权选人组件的页签
     */
    public ModelAndView selectPeoplePanel(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        ModelAndView mv = new ModelAndView("ctp/portal/space/selectPeoplePanel");
        //0:使用者；1：管理者
        String securityType = request.getParameter("securityType");
        String pageId = request.getParameter("pageId");
        PortalPageSecuritymodel securityModel = pageManager.getPageSecurityModel(Long.valueOf(pageId), Integer.valueOf(securityType), PageSecurityModelShowType.panels.name());
        mv.addObject("securityModel", securityModel);
        return mv;
    }
    /**
     * 空间模版-控制空间授权选人组件的页签
     */
    public ModelAndView selectPeopleSelectType(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mv = new ModelAndView("ctp/portal/space/selectPeopleSelectType");
        String securityType = request.getParameter("securityType");
        String pageId = request.getParameter("pageId");
        PortalPageSecuritymodel securityModel = pageManager.getPageSecurityModel(Long.valueOf(pageId), Integer.valueOf(securityType), PageSecurityModelShowType.selectType.name());
        mv.addObject("securityModel", securityModel);
        return mv;
    }
    /**
     * 显示二级主题空间
     */
    public ModelAndView showThemSpace(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String themType = request.getParameter("themType");
        String portalId = request.getParameter("portalId");
        String spacePath = request.getParameter("spacePath");
        String spaceIdStr = request.getParameter("spaceId");
        String sprint = request.getParameter("sprint");
        User user = AppContext.getCurrentUser();
        Long accountId = user.getLoginAccount();
        PortalSpaceFix spaceFix = null;
        if(Strings.isNotBlank(spaceIdStr)){
        	spaceFix= portalCacheManager.getPageFixIdCacheByKey(Long.parseLong(spaceIdStr));
        }
        if(Strings.isNotBlank(themType) && null==spaceFix){
        	spaceFix= this.spaceManager.transSelectThemSpace(Integer.parseInt(themType), accountId, user.getId());
        }
        if(null==spaceFix && Strings.isNotBlank(spacePath)){
        	spaceFix= spaceManager.getSpaceFix(spacePath);
        }
        String portalType= "0";
        if(spaceFix!=null){
            Long spaceId = spaceFix.getId();
            if(Strings.isBlank(portalId) || "null".equals(portalId)){
            	Long belongPortalId= portalCacheManager.getSpaceBelongPortalCache(spaceId);
            	if(null==belongPortalId){
            		portalId= portalCacheManager.getUserAcessMainPortalIdFromCache(user);
            	}else{
            		portalId= belongPortalId.toString();
            	}
            	portalType= "0";
            }else {
            	//判断如果是报表门户则用主门户的样式
            	PortalSet set=portalCacheManager.getPortalSetFromCache(Long.parseLong(portalId));
            	if(set!=null && set.getPortalType()==4) {
            		portalId = portalCacheManager.getUserAcessMainPortalIdFromCache(user);
            		portalType= "4";
            	}
            }
            VPortalObj vPortalObj= portalCacheManager.getVPortalObj(user, portalId, spaceId, true);
            vPortalObj.setType(portalType);
            SpaceType trueSpacetype = EnumUtil.getEnumByOrdinal(SpaceType.class, spaceFix.getType());
            ModelAndView modelAndView= new ModelAndView("raw:/portal/decoration/layout/layout.jsp");
            modelAndView.addObject("vPortalObj", vPortalObj);
            long random= System.currentTimeMillis();
			modelAndView.addObject("random",random);
			modelAndView.addObject("onlySpace",true);
			modelAndView.addObject("sprint",sprint);
			modelAndView.addObject("spaceName",spaceFix.getSpacename());
			modelAndView.addObject("trueSpaceType",trueSpacetype.name());
            return modelAndView;
        }
        return null;
    }

    /**
     * 前端空间编辑调整布局
     * @param request
     * @param response
     * @return
     * @throws BusinessException
     */
    public ModelAndView showSpaceLaoutByFront(HttpServletRequest request, HttpServletResponse response) throws BusinessException{
        ModelAndView mv = new ModelAndView("ctp/portal/space/spaceLayout");
        String decorationId = request.getParameter("decorationId");
        mv.addObject("allLayout", PortalDecorationManager.getAllLayoutType());
        mv.addObject("layoutTypes", LayoutConstants.lagoutToDecorations);
        mv.addObject("decorationId",decorationId);
        return mv;
    }

    /**
     * 默认空间设置入口
     */
    public ModelAndView defaultSpaceSetting(HttpServletRequest request, HttpServletResponse response) throws BusinessException{
        String portalId= request.getParameter("portalId");
    	ModelAndView mv = new ModelAndView("ctp/portal/space/defaultSpaceSetting");
    	mv.addObject("portalId",portalId);
        return mv;
    }

    /**
     * 栏目属性设置页面
     * @param request
     * @param response
     * @return
     * @throws BusinessException
     */
    public ModelAndView sectionPropertySetting(HttpServletRequest request, HttpServletResponse response) throws BusinessException{
    	ModelAndView mv = new ModelAndView("ctp/portal/ctpDesigner/sectionPropertySetting");
    	long random= System.currentTimeMillis();
    	mv.addObject("random",random);
        return mv;
    }

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

}
