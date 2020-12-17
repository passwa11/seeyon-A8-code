package com.seeyon.apps.ext.accessSeting.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.accessSeting.manager.AccessSetingManager;
import com.seeyon.apps.ext.accessSeting.manager.AccessSetingManagerImpl;
import com.seeyon.apps.ext.accessSeting.po.DepartmentViewTimeRange;
import com.seeyon.apps.ext.accessSeting.po.ZorgMember;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.template.CtpTemplateCategory;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.template.vo.TemplateCategoryComparator;
import com.seeyon.ctp.util.Strings;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class AccessSetingControlller extends BaseController {

    private AccessSetingManager manager = new AccessSetingManagerImpl();

    private TemplateManager templateManager;

    public TemplateManager getTemplateManager() {
        return templateManager;
    }

    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    //****************************************

    public ModelAndView toTemplateConfigPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("apps/ext/accessSeting/templateConfig");
    }

    /**
     * 获取table列表数据
     * @param request
     * @param response
     * @param departmentId
     * @return
     */
    public ModelAndView getTemplateInfos(HttpServletRequest request, HttpServletResponse response, String departmentId) {
        String id = request.getParameter("categoryId");
        Map<String, String> params = new HashMap<>();
        params.put("categoryId", id);
        params.put("subject", request.getParameter("subject"));
        List<Map<String, Object>> list = manager.getTemplateInfos(params);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("code", 0);
        map2.put("message", "");
        map2.put("count", list.size());
        map2.put("data", list);
        JSONObject json = new JSONObject(map2);
        render(response, json.toJSONString());
        return null;
    }

    /**
     * 跳转到流程禁用查询页面
     */
    public ModelAndView toTemplateStop(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<CtpTemplateCategory> categories = getCollaborationTemplate();
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = null;
        for (CtpTemplateCategory c : categories) {
            map = new HashMap<>();
            map.put("id", c.getId() + "");
            map.put("pId", null == c.getParentId() ? "0" : c.getParentId() + "");
            map.put("name", c.getName());
            list.add(map);
        }
        request.setAttribute("list", JSON.toJSONString(list));
        return new ModelAndView("apps/ext/accessSeting/templateStop");
    }

    private List<CtpTemplateCategory> getCollaborationTemplate() throws BusinessException {
        long orgAccountId = AppContext.currentAccountId();
        List<CtpTemplateCategory> categories = templateManager.getCategoryByAuth(orgAccountId);
        if (Strings.isNotEmpty(categories)) {
        }
        List<CtpTemplateCategory> formCategories = templateManager.getCategoryByAuth(orgAccountId, ModuleType.form.ordinal());
        Long collaborationType = Long.parseLong(String.valueOf(ModuleType.collaboration.getKey()));
        Long formType = Long.parseLong(String.valueOf(ModuleType.form.getKey()));
        for (CtpTemplateCategory ctpTemplateCategory : formCategories) {
            if (categories.contains(ctpTemplateCategory)) {
                continue;
            }
            categories.add(ctpTemplateCategory);
        }
        for (int count = categories.size() - 1; count > -1; count--) {//双重防护,避免出现两个协同模板
            CtpTemplateCategory ctpTemplateCategory = categories.get(count);
            if (ctpTemplateCategory.getId() == 1L) {
                categories.remove(count);
                break;
            }
        }
        // 协同模版类型根结点
        CtpTemplateCategory root = new CtpTemplateCategory(collaborationType,
                ResourceUtil.getString("collaboration.template.category.type.0"), null);
        categories.add(root);
        for (CtpTemplateCategory ctpTemplateCategory : categories) {
            if (formType.equals(ctpTemplateCategory.getParentId())) {
                ctpTemplateCategory.setParentId(collaborationType);
            }
        }
        Collections.sort(categories, new TemplateCategoryComparator());
        return categories;
    }


    //****************************************

    /**
     * 根据部门id获取部门人员列表
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView getMemberByDepartmentId(HttpServletRequest request, HttpServletResponse response) {
        try {
            String departmentId = request.getParameter("departmentId");
            if (null == departmentId || departmentId.equals("")) {
                departmentId = "";
            }
            Map map = new HashMap<>();
            map.put("departmentId", departmentId);
            map.put("name", request.getParameter("name"));
            List<ZorgMember> list = null;
            list = manager.showPeople(map);
            Map<String, Object> map2 = new HashMap<>();
            map2.put("code", 0);
            map2.put("message", "");
            map2.put("count", list.size());
            map2.put("data", list);
            JSONObject json = new JSONObject(map2);
            render(response, json.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * 跳转到查询设置页面
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        Long accountId = user.getAccountId();
        List<Map<String, Object>> list = manager.queryAllUnit(accountId);
        request.setAttribute("list", JSON.toJSONString(list));
        return new ModelAndView("apps/ext/accessSeting/index");
    }

    /**
     * 弹出设置页面
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView setting(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("apps/ext/accessSeting/setConfig");
    }

    public ModelAndView getDepartmentRange(HttpServletRequest request, HttpServletResponse response, String departmentId) {
        String id = request.getParameter("departmentId");
        Map<String, Object> params = new HashMap<>();
        params.put("deptmentId", Long.parseLong(id));
        List<DepartmentViewTimeRange> list = manager.getDepartmentViewTimeRange(params);
        Map<String, Object> map = new HashMap<>();
        if (null != list && list.size() > 0) {
            map.put("data", list.get(0));
        } else {
            map.put("data", null);
        }
        JSONObject json = new JSONObject(map);
        render(response, json.toJSONString());
        return null;
    }

    public ModelAndView saveDepartmentViewTimeRange(HttpServletRequest request, HttpServletResponse response, DepartmentViewTimeRange range) {
        Map<String, Object> map2 = new HashMap<>();
        try {
            String[] memberIds = (range.getIds()).split(",");
            Map<String, Object> params = null;
            for (int i = 0; i < memberIds.length; i++) {
                if (!"".equals(memberIds[i])) {
                    params = new HashMap<>();
                    params.put("memberId", Long.parseLong(memberIds[i]));
                    List<DepartmentViewTimeRange> list = manager.getDepartmentViewTimeRange(params);
                    if (list.size() > 0) {
                        DepartmentViewTimeRange upRange = list.get(0);
                        upRange.setDayNum(range.getDayNum());
                        manager.updateDepartmentViewTimeRange(upRange);
                    } else {
                        range.setId(System.currentTimeMillis());
                        range.setMemberId(Long.parseLong(memberIds[i]));
                        manager.saveDepartmentViewTimeRange(range);
                    }
                }
            }
            map2.put("code", 0);
        } catch (Exception e) {
            map2.put("code", -1);
        }
        JSONObject json = new JSONObject(map2);
        render(response, json.toJSONString());
        return null;
    }


    private void render(HttpServletResponse response, String text) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.setContentLength(text.getBytes("UTF-8").length);
            response.getWriter().write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
