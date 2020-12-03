package com.seeyon.cap4.form.util;

import com.seeyon.ctp.common.log.CtpLogFactory;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;

import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author fq
 * @description 页面工具类
 * @date 2019/5/29
 */
public class PageUtil {

    private static Log log = CtpLogFactory.getLog(PageUtil.class);
    public static void render(ServletResponse response, String text, String contentType) {
        response.setContentType(contentType);
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.print(text);
        } catch (IOException e) {
            log.error("渲染页面失败:\n" + e.getMessage());
        } finally {
            if (out != null) {
                out.flush();
                out.close();
                out = null;
            }
        }
    }

    public static void renderText(ServletResponse response, String text) {
        render(response, text, "text/plain;charset=UTF-8");
    }

    public static void renderHtml(ServletResponse response, String text) {
        render(response, text, "text/html;charset=UTF-8");
    }

    public static void renderXML(ServletResponse response, String text) {
        render(response, text, "text/xml;charset=UTF-8");
    }

    public static void renderJSON(ServletResponse response, String text) {
        render(response, text, "text/json;charset=UTF-8");
    }

    /**
     * @description 获取不带data的json返回值
     * @param code  code代码
     * @param msg   描述信息
     * @return
     */
    public static String getResult(String code, String msg) {
        return _getResult(code, msg).toString();
    }

    /**
     * @description 获取带data的json返回值
     * @param code  code代码
     * @param msg   描述信息
     * @param data  data对象
     * @return
     */
    public static String getResult(String code, String msg, Object data) {
        JSONObject rsJson = _getResult(code, msg);
        rsJson.put("data", data);
        return rsJson.toString();
    }

    private static JSONObject _getResult(String code, String msg) {
        JSONObject rsJson = new JSONObject();
        rsJson.put("code", code);
        rsJson.put("msg", msg);
        return rsJson;
    }
}
