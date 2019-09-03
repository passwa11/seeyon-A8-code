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
//import java.util.List;
//import java.util.Map;
//
//import org.springframework.web.servlet.ModelAndView;
//
//import com.seeyon.ctp.common.exceptions.BusinessException;
//import com.seeyon.ctp.portal.section.bo.SectionTreeNode;
///**
// * <p>Title: 栏目前端Ajax请求发送接口</p>
// * <p>Description: </p>
// * <p>Copyright: Copyright (c) 2012</p>
// * <p>Company: seeyon.com</p>
// * @since CTP2.0
// */
//public interface SectionManager {
//    /**
//     * 栏目Ajax数据加载方法
//     * @param params【String sectionBeanId,String entityId, String ordinal, String spaceType, String ownerId, int x, int y, int width,String panelId,String[] paramKeys,String[] paramValues】
//     * @return
//     */
//    public Map<String, Object> doProjection(Map params);
//    /**
//     * 栏目组件Ajax加载备选栏目
//     * @param params key:sectionType,spaceType,spaceId
//     * @return
//     * @throws BusinessException
//     */
//    public List<SectionTreeNode> selectSectionTreeNode(Map params) throws BusinessException;
//    /**
//     * 栏目组件Ajax加载已选栏目
//     * @param params key：空间加载已选传pagePath,多频道加载已选传entityId
//     * @return
//     * @throws BusinessException
//     */
//    public List<SectionTreeNode> selectedSectionTreeNode(Map params)throws BusinessException;
//    /**
//     * 栏目组件Ajax加载备选栏目
//     * @param params key:sectionType,spaceType,spaceId
//     * @return
//     * @throws BusinessException
//     */
//    public List<SectionTreeNode> selectAsyncSectionTreeNode(Map params) throws BusinessException;
//    /**
//     * 同步判断当前空间是否存在
//     */
//    public boolean isThisSpaceExist(String pagePath);
//    /**
//     * 生成空间栏目数据，返回两种格式的数据：一种栏目渲染需要的，一种栏目选择器弹出框回填需要的数据
//     * @param params
//     * @return
//     * @throws BusinessException
//     */
//    public Map<String,Object> generateSpacePortlets(Map<String,String> spaceEditRequest) throws BusinessException;
//
//    /**
//     * 生成空间栏目数据，返回两种格式的数据：一种栏目渲染需要的，一种栏目选择器弹出框回填需要的数据
//     * @param params
//     * @return
//     * @throws BusinessException
//     */
//    public Map<String,Object> generateSectionsToPortlet(Map<String,String> spaceEditRequest) throws BusinessException;
//
//    /**
//     *
//     * @param portletId
//     * @return
//     * @throws BusinessException
//     */
//    public String[][] getPortletTotals(Map<String,String> params) throws BusinessException;
//
//    /**
//     *
//     * @param spaceEditRequest
//     * @return
//     * @throws BusinessException
//     */
//    public Map<String, Object> deleteSectionFromPortlet(Map<String, String> spaceEditRequest)throws BusinessException;
//
//    public String getUuid();
//}
