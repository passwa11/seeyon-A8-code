package com.seeyon.v3x.edoc.controller;

import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.v3x.edoc.domain.EdocKeyWord;
import com.seeyon.v3x.edoc.manager.EdocKeyWordManager;

/**
 * 公文关键字操作控制器
 * @author Yang.Yinghai
 * @date 2011-10-11下午02:40:57
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class EdocKeyWordController extends BaseController {

    /** 关键词管理器对象 */
    private EdocKeyWordManager edocKeyWordManager;

    /**
     * 进入主题词管理页签
     * @param request HTTP请求
     * @param response HTTP响应
     * @return ModelAndView对象
     * @throws Exception 异常
     */
    @CheckRoleAccess(roleTypes = {Role_NAME.AccountAdministrator, Role_NAME.EdocManagement})
    public ModelAndView listMain(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("edoc/keyWordManage/keyword_main");
        if(request.getParameter("id") != null)
            mav.addObject("id", request.getParameter("id"));
        return mav;
    }

    /**
     * 系统管理，树型结构
     * @param request HTTP请求
     * @param response HTTP响应
     * @return ModelAndView对象
     * @throws Exception 异常
     */
    @CheckRoleAccess(roleTypes = {Role_NAME.AccountAdministrator, Role_NAME.EdocManagement})
    public ModelAndView tree(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/keyWordManage/keyword_tree");
        List<EdocKeyWord> keywordList = edocKeyWordManager.getTreeList();
        modelAndView.addObject("keywordList", keywordList);
        return modelAndView;
    }

    /**
     * 主题词列表页面
     * @param request HTTP请求
     * @param response HTTP响应
     * @return ModelAndView对象
     * @throws Exception 异常
     */
    @CheckRoleAccess(roleTypes = {Role_NAME.AccountAdministrator, Role_NAME.EdocManagement})
    public ModelAndView list(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/keyWordManage/keyword_list");
        String searchName = request.getParameter("searchName");
        String parentId = request.getParameter("currentNodeId");
        long iParentId = 0;
        if(parentId != null && !"".equals(parentId)) {
            iParentId = Long.parseLong(parentId);
        }
        List<EdocKeyWord> keywordList = edocKeyWordManager.queryByCondition(iParentId, searchName);
        modelAndView.addObject("keywordList", keywordList);
        return modelAndView;
    }

    /**
     * 添加主题词页面
     * @param request HTTP请求
     * @param response HTTP响应
     * @return ModelAndView对象
     * @throws Exception 异常
     */
    @CheckRoleAccess(roleTypes = {Role_NAME.AccountAdministrator, Role_NAME.EdocManagement})
    public ModelAndView addKeyword(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("edoc/keyWordManage/keyword_create");
        String parentId = request.getParameter("parentId");
        long iParentId = 0;
        if(parentId != null && !"".equals(parentId)) {
            iParentId = Long.parseLong(parentId);
        }
        mav.addObject("parentId", iParentId);
        return mav;
    }

    /**
     * 向数据库表中写入一条新的主题词
     * @param request HTTP请求
     * @param response HTTP响应
     * @return ModelAndView对象
     * @throws Exception 异常
     */
    @CheckRoleAccess(roleTypes = {Role_NAME.AccountAdministrator, Role_NAME.EdocManagement})
    public ModelAndView create(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String name = request.getParameter("name");
        long parentId = Long.parseLong(request.getParameter("parentId"));
        String sortNum = request.getParameter("sortNum");
        User user = AppContext.getCurrentUser();
        int iSortNum = 0;
        if(sortNum != null && !"".equals(sortNum)) {
            iSortNum = Integer.parseInt(sortNum);
        }
        EdocKeyWord newWord = new EdocKeyWord();
        newWord.setNewId();
        newWord.setName(name == null ? "" : name);
        newWord.setParentId(parentId);
        newWord.setSortNum(iSortNum);
        newWord.setAccountId(user.getLoginAccount());
        if(parentId == 0) {
            newWord.setLevelNum(1);
        } else {
            EdocKeyWord parent = edocKeyWordManager.getById(parentId);
            newWord.setLevelNum(parent.getLevelNum() + 1);
        }
        newWord.setIsSystem(true);
        newWord.setCreateTime(new Timestamp(System.currentTimeMillis()));
        newWord.setCreateUserId(user.getId());
        edocKeyWordManager.save(newWord);
        super.rendJavaScript(response, "parent.doEndKeyword()");
        return null;
    }

    /**
     * 编辑主题词
     * @param request HTTP请求
     * @param response HTTP响应
     * @return ModelAndView对象
     * @throws Exception 异常
     */
    @CheckRoleAccess(roleTypes = {Role_NAME.AccountAdministrator, Role_NAME.EdocManagement})
    public ModelAndView editKeyword(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("edoc/keyWordManage/keyword_detail");
        String id = request.getParameter("id");
        // 取得是否是详细页面标志
        String readOnlyStr = request.getParameter("readOnly");
        // 获取关键字对象
        EdocKeyWord keyword = edocKeyWordManager.getById(Long.parseLong(id));
        mav.addObject("keyword", keyword);
        boolean readOnly = false;
        if(readOnlyStr != null && "true".equals(readOnlyStr)) {
            readOnly = true;
            mav.addObject("readOnly", readOnly);
            mav.addObject("preview", 0);
        } else {
            mav.addObject("preview", 1);
        }
        return mav;
    }

    /**
     * 更新主题词记录
     * @param request HTTP请求
     * @param response HTTP响应
     * @return ModelAndView对象
     * @throws Exception 异常
     */
    @CheckRoleAccess(roleTypes = {Role_NAME.AccountAdministrator, Role_NAME.EdocManagement})
    public ModelAndView update(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String id = request.getParameter("id");
        if(StringUtils.isNotBlank(id)) {
            // 获取关键字对象
            EdocKeyWord keyword = edocKeyWordManager.getById(Long.parseLong(id));
            String name = request.getParameter("name");
            String sortNum = request.getParameter("sortNum");
            keyword.setName(name);
            keyword.setSortNum(Integer.parseInt(sortNum));
            keyword.setCreateTime(new Timestamp(System.currentTimeMillis()));
            keyword.setCreateUserId(AppContext.getCurrentUser().getId());
            edocKeyWordManager.update(keyword);
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script>");
//            out.println("parent.getA8Top().endProc();");
            out.println("parent.parent.location.reload();");
            out.println("</script>");
        }
        return null;
    }

    /**
     * 删除主题词
     * @param request HTTP请求
     * @param response HTTP响应
     * @return ModelAndView对象
     * @throws Exception 异常
     */
    @CheckRoleAccess(roleTypes = {Role_NAME.AccountAdministrator, Role_NAME.EdocManagement})
    public ModelAndView delete(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String ids = request.getParameter("ids");
        edocKeyWordManager.deleteByIds(ids);
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<script>");
//        out.println("parent.getA8Top().endProc();");
        out.println("parent.location.reload();");
        out.println("</script>");
        return null;
    }

    /**
     * 主题词选择
     * @param request HTTP请求
     * @param response HTTP响应
     * @return ModelAndView对象
     * @throws Exception 异常
     */
    public ModelAndView selectKeyword(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("edoc/keyWordManage/keyword_select");
        // 系统预置主题词列表
        List<EdocKeyWord> systemList = edocKeyWordManager.getList();
        List<EdocKeyWord> treeList = new ArrayList<EdocKeyWord>();//二级以上
        List<EdocKeyWord> subList = new ArrayList<EdocKeyWord>();//一级以下
        if(Strings.isNotEmpty(systemList)) {
        	for(EdocKeyWord keyword : systemList) {
        		if(keyword.getLevelNum() == 1) {
        			treeList.add(keyword);
        		} else if(keyword.getLevelNum() == 2) {
        			treeList.add(keyword);
        			subList	.add(keyword);
        		} else {
        			subList	.add(keyword);
        		}
        	}
        }
        mav.addObject("treeList", treeList);
        mav.addObject("subList", subList);
        return mav;
    }

    /**
     * 设置edocKeyWordManager
     * @param edocKeyWordManager edocKeyWordManager
     */
    public void setEdocKeyWordManager(EdocKeyWordManager edocKeyWordManager) {
        this.edocKeyWordManager = edocKeyWordManager;
    }
}
