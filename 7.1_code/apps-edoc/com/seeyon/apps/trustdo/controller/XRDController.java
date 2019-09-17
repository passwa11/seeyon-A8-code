package com.seeyon.apps.trustdo.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.trustdo.exceptions.XRDUserException;
import com.seeyon.apps.trustdo.model.sdk.*;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.logging.Log;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.trustdo.constants.SDKConstants;
import com.seeyon.apps.trustdo.constants.TrustdoErrorMsg;
import com.seeyon.apps.trustdo.constants.XRDPhoneConstants;
import com.seeyon.apps.trustdo.manager.XRDManager;
import com.seeyon.apps.trustdo.po.XRDUserPO;
import com.seeyon.apps.trustdo.utils.QRCodeUtil;
import com.seeyon.apps.trustdo.utils.XRDAppUtils;
import com.seeyon.apps.trustdo.utils.XRDHttpUtils;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.annotation.NeedlessCheckLogin;
import com.seeyon.ctp.util.json.JSONUtil;

/**
 * 绑定关系、密码重置控制器
 *
 * @author zhaopeng
 */
public class XRDController extends BaseController {

    private static final Log LOGGER = CtpLogFactory.getLog(XRDController.class);

    private XRDManager xrdManager;

    private final static String SHOW_INFO = "手机盾移动身份认证系统";

    @Override
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelView = new ModelAndView("plugin/trustdo/index");
        boolean isGroupAdmin = AppContext.getCurrentUser().isGroupAdmin();
        modelView.addObject("isGroupAdmin", Boolean.valueOf(isGroupAdmin));
        return modelView;
    }


    /**
     * 纯SDK重置手机盾密码
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @NeedlessCheckLogin
    public void resetPW(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String bindUserId = request.getParameter("bindUserId");
        LOGGER.debug("resetPW xrdUserPo id is :" + bindUserId);
        Map<String, Object> map = new HashMap<String, Object>();
        if (bindUserId != null || !"".equals(bindUserId)) {
            XRDUserPO xrdUserPo = xrdManager.get(bindUserId);
            if (xrdUserPo != null) {
                /** 一.准备请求手机盾接口参数*/
                //签名值
                String appKeySign = null;
                //对参数签名进行组包,将加密参数封装为一个Map集合
                HashMap<String, Object> signMap = new HashMap<String, Object>();
                signMap.put("appKey", XRDPhoneConstants.APP_KEY);
                signMap.put("account", xrdUserPo.getTrustdoAccount());
                signMap.put("isRespQrData", true);
                signMap.put("showInfo", SHOW_INFO);
                appKeySign = XRDAppUtils.getMd5Sign(signMap, XRDPhoneConstants.APP_SECRET);
                /** 二.对请求参数组包*/
                NameValuePair[] data = {
                        new NameValuePair("appKey", XRDPhoneConstants.APP_KEY)
                        , new NameValuePair("account", xrdUserPo.getTrustdoAccount())
                        , new NameValuePair("isRespQrData", true + "")
                        , new NameValuePair("showInfo", SHOW_INFO)
                        , new NameValuePair("sign", appKeySign)
                };
                /** 三.调用手机盾接口,发送http请求*/
                String url = SDKConstants.SDK_EVENT_RESET_URL;
                Result<?> resetResult = XRDHttpUtils.sdkConnectMobileShieldServer(data, url, ResetData.class);
                LOGGER.debug("getSDKResetEvent resetResult : " + JSONUtil.toJSONString(resetResult == null ? "" : resetResult));
                /** 四.将重置返回数据生成二维码*/
                if (resetResult != null && resetResult.getErrCode().equals(TrustdoErrorMsg.SUCCESS_CODE)) {
                    String dataJson = JSONUtil.toJSONString(resetResult);
                    LOGGER.debug("getSDKResetEvent resetResult-data : " + JSONUtil.toJSONString(resetResult.getData()));
                    BufferedImage image = QRCodeUtil.createImage(dataJson);
                    map.put("code", Integer.parseInt(resetResult.getErrCode()));
                    map.put("data", XRDAppUtils.bufferImageToBase64String(image));
                }
            } else {
                map.put("code", 201);
            }

            String resultData = JSONUtil.toJSONString(map);
            this.trustdoOutput(response, resultData);
        }
    }

    /**
     * 业务pc展示重置手机盾密码
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @NeedlessCheckLogin
    public void resetWebPW(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String result = "";
        String bindUserId = request.getParameter("bindUserId");
        LOGGER.debug("resetWebPW xrdUserPo id is :" + bindUserId);
        if (bindUserId != null || !"".equals(bindUserId)) {
            XRDUserPO xrdUserPo = xrdManager.get(bindUserId);
            if (xrdUserPo != null) {
                /** 一.准备请求手机盾接口参数*/
                //签名值
                String appKeySign = null;
                //对参数签名进行组包,将加密参数封装为一个Map集合
                HashMap<String, Object> signMap = new HashMap<String, Object>();
                signMap.put("appKey", XRDPhoneConstants.APP_KEY);
                signMap.put("account", xrdUserPo.getTrustdoAccount());
                signMap.put("showInfo", SHOW_INFO);
                appKeySign = XRDAppUtils.getMd5Sign(signMap, XRDPhoneConstants.APP_SECRET);
                /** 二.对请求参数组包*/
                NameValuePair[] data = {
                        new NameValuePair("appKey", XRDPhoneConstants.APP_KEY)
                        , new NameValuePair("account", xrdUserPo.getTrustdoAccount())
                        , new NameValuePair("showInfo", SHOW_INFO)
                        , new NameValuePair("sign", appKeySign)
                };
                /** 三.调用手机盾接口,发送http请求*/
                String url = SDKConstants.WEB_EVENT_RESET_URL;
                Result<?> resetResult = XRDHttpUtils.sdkConnectMobileShieldServer(data, url, ResetData.class);
                if (resetResult != null) {
                    LOGGER.debug("getWebResetEvent resetResult : " + JSONUtil.toJSONString(resetResult));
                    if (resetResult.getErrCode().equals(TrustdoErrorMsg.SUCCESS_CODE)) {
                        result = JSONUtil.toJSONString(resetResult.getData());
                    } else {
                        result = JSONUtil.toJSONString(resetResult);
                    }
                    LOGGER.debug("getWebResetEvent resultData : " + result);
                }
            }

            this.trustdoOutput(response, result);
        }
    }

    /**
     * 业务pc展示登录二维码
     *
     * @param request
     * @param response
     */
    @NeedlessCheckLogin
    public void webLogin(HttpServletRequest request, HttpServletResponse response) {
        /** 一.准备请求手机盾接口参数*/
        //签名值
        String appKeySign = null;
        String result = "";
        //对参数签名进行组包,将加密参数封装为一个Map集合
        HashMap<String, Object> signMap = new HashMap<String, Object>();
        signMap.put("appKey", XRDPhoneConstants.APP_KEY);
        signMap.put("isRespQrData", true + "");
        try {
            appKeySign = XRDAppUtils.getMd5Sign(signMap, XRDPhoneConstants.APP_SECRET);
        } catch (XRDUserException e) {
            e.printStackTrace();
        }
        /** 二.对请求参数组包*/
        NameValuePair[] data = {
                new NameValuePair("appKey", XRDPhoneConstants.APP_KEY)
                , new NameValuePair("isRespQrData", true + "")
                , new NameValuePair("sign", appKeySign)
        };
        /** 三.调用手机盾接口,发送http请求*/
        String url = SDKConstants.WEB_EVENT_LOGIN_URL;
        Result<String> webLoginResult = (Result<String>) XRDHttpUtils.sdkConnectMobileShieldServer(data, url, WebLoginData.class);
        if (webLoginResult != null && webLoginResult.getErrCode().equals(TrustdoErrorMsg.SUCCESS_CODE)) {
            WebLoginData resetData = JSONUtil.parseJSONString(JSONUtil.toJSONString(webLoginResult.getData()), WebLoginData.class);
            BufferedImage image = null;
            try {
                image = QRCodeUtil.createImage(resetData.getEventData());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            try {
               resetData.setEventData(XRDAppUtils.bufferImageToBase64String(image));
                webLoginResult.setData(JSONUtil.toJSONString(resetData));
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
            LOGGER.debug("getWebLoginEvent resultData : " + webLoginResult);
        }
        try {
            this.trustdoOutput(response, JSONUtil.toJSONString(webLoginResult));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 业务pc展示解锁功能
     *
     * @param request
     * @throws Exception
     */
    @NeedlessCheckLogin
    public void unlockWebPW(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String result = null;
        String bindUserId = request.getParameter("bindUserId");
        LOGGER.debug("resetWebPW xrdUserPo id is :" + bindUserId);
        if (bindUserId != null || !"".equals(bindUserId)) {
            XRDUserPO xrdUserPo = xrdManager.get(bindUserId);
            if (xrdUserPo != null) {
                //签名值
                String appKeySign = null;
                //对参数签名进行组包,将加密参数封装为一个Map集合
                HashMap<String, Object> signMap = new HashMap<String, Object>(1);
                signMap.put("appKey", XRDPhoneConstants.APP_KEY);
                signMap.put("account", xrdUserPo.getTrustdoAccount());
                appKeySign = XRDAppUtils.getMd5Sign(signMap, XRDPhoneConstants.APP_SECRET);
                NameValuePair[] data = {
                        new NameValuePair("appKey", XRDPhoneConstants.APP_KEY)
                        , new NameValuePair("account", xrdUserPo.getTrustdoAccount())
                        , new NameValuePair("sign", appKeySign)
                };
                String url = SDKConstants.ACCOUNT_UNLOCK_URL;
                Result<?> resetResult = XRDHttpUtils.sdkConnectMobileShieldServer(data, url, Result.class);
                if (resetResult != null) {
                    LOGGER.debug("unlockWebPW resetResult : " + JSONUtil.toJSONString(resetResult));
                    result = JSONUtil.toJSONString(resetResult);
                    LOGGER.debug("getWebResetEvent resultData : " + result);
                }
            }
        }
        this.trustdoOutput(response, result);
    }

    /**
     * 根据事件eventId获取token
     *
     * @throws IOException
     * @throws ServletException
     */
    @NeedlessCheckLogin
    public void getLoginAcctoken(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String eventId = request.getParameter("eventId");
        //签名值
        String appKeySign = null;
        String result = "";
        if (eventId != null && !"".equals(eventId)) {
            //对参数签名进行组包,将加密参数封装为一个Map集合
            HashMap<String, Object> signMap = new HashMap<String, Object>();
            signMap.put("appKey", XRDPhoneConstants.APP_KEY);
            signMap.put("eventId", eventId);
            appKeySign = XRDAppUtils.getMd5Sign(signMap, XRDPhoneConstants.APP_SECRET);
            /** 二.对请求参数组包*/
            NameValuePair[] data = {
                    new NameValuePair("appKey", XRDPhoneConstants.APP_KEY)
                    , new NameValuePair("eventId", eventId)
                    , new NameValuePair("sign", appKeySign)
            };
            /** 三.调用手机盾接口,发送http请求*/
            String url = SDKConstants.WEB_GET_TOKEN_URL;
            Result<?> tokenResult = XRDHttpUtils.sdkConnectMobileShieldServer(data, url, TokenData.class);
            if (tokenResult != null && tokenResult.getErrCode().equals(TrustdoErrorMsg.SUCCESS_CODE)) {
                LOGGER.debug("getLoginAcctoken tokenResult : " + JSONUtil.toJSONString(tokenResult));
                result = JSONUtil.toJSONString(tokenResult.getData());
                LOGGER.debug("getLoginAcctoken resultData : " + result);
            }
        }
        this.trustdoOutput(response, result);
    }

    /**
     * 根据token置换用户信息并登录
     *
     * @throws IOException
     * @throws ServletException
     */
    @NeedlessCheckLogin
    public void getLoginAccount(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String appKeySign = null;
        String result = "";
        String accToken = request.getParameter("accToken");
        LOGGER.debug("getBizLoginName accToken:" + accToken);

        if (accToken != null && !"".equals(accToken)) {
            Map<String, Object> signMap = new HashMap<String, Object>();
            signMap.put("appKey", XRDPhoneConstants.APP_KEY);
            signMap.put("accToken", accToken);
            appKeySign = XRDAppUtils.getMd5Sign(signMap, XRDPhoneConstants.APP_SECRET);

            String url = SDKConstants.SDK_LOGIN_NAME_URL;
            LOGGER.debug("getSDKLoginName url:" + url);

            NameValuePair[] data = {
                    new NameValuePair("appKey", XRDPhoneConstants.APP_KEY),
                    new NameValuePair("accToken", accToken),
                    new NameValuePair("sign", appKeySign)};

            Result<?> loginResult = XRDHttpUtils.sdkConnectMobileShieldServer(data, url, LoginData.class);
            if (loginResult != null && loginResult.getErrCode().equals(TrustdoErrorMsg.SUCCESS_CODE)) {
                LOGGER.debug("getBizLoginName loginResult:" + JSONUtil.toJSONString(loginResult));
                result = JSONUtil.toJSONString(loginResult.getData());
                LOGGER.debug("getBizLoginName resultData : " + result);
            }
        }
        this.trustdoOutput(response, result);
    }

    @NeedlessCheckLogin
    public void getTrustdoIsOpen(HttpServletRequest request,
                                 HttpServletResponse response) throws IOException {
        String result = "false";
        //判断是否加载了trustdo插件
        if (AppContext.hasPlugin("trustdo")) {
            result = "true";
        }
        this.trustdoOutput(response, result);
    }

    private void trustdoOutput(HttpServletResponse response, String result) throws IOException {
        Cookie c = new Cookie("resultData", URLEncoder.encode(result, "utf-8"));
        c.setHttpOnly(true);
        c.setSecure(false);
        response.addCookie(c);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter pw = response.getWriter();
        pw.print(result);
        pw.close();
    }

    public XRDManager getXrdManager() {
        return xrdManager;
    }

    public void setXrdManager(XRDManager xrdManager) {
        this.xrdManager = xrdManager;
    }

}
