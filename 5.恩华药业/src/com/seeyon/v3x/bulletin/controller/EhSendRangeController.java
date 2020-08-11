package com.seeyon.v3x.bulletin.controller;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.v3x.bulletin.domain.EhSendRange;
import com.seeyon.v3x.bulletin.manager.EhSendRangeManager;
import com.seeyon.v3x.bulletin.manager.EhSendRangeManagerImpl;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EhSendRangeController extends BaseController {

    private EhSendRangeManager sendRangeManager=new EhSendRangeManagerImpl();

    public EhSendRangeManager getSendRangeManager() {
        return sendRangeManager;
    }

    public ModelAndView getSendRange(HttpServletRequest request, HttpServletResponse response){
        Map map=new HashMap();
        try {
            String moduleId=request.getParameter("id");
            Map param=new HashMap();
            param.put("moduleId",moduleId);
            List<EhSendRange> ehSendRanges=sendRangeManager.findEhSendRangeByCondition(param);
            if(ehSendRanges.size()>0){
                map.put("data",ehSendRanges.get(0));
            }
            map.put("code",0);
        }catch (Exception e){
            e.printStackTrace();
            map.put("code",-1);
            map.put("data",null);
        }
        JSONObject json=new JSONObject(map);
        render(response,json.toJSONString());
        return null;
    }
    public void render(HttpServletResponse response,String text){
        try {
            response.setContentType("application/json;charset=UTF-8");
            response.setContentLength(text.getBytes("UTF-8").length);
            response.getWriter().write(text);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
