package com.seeyon.apps.trustdo.controller;

import com.seeyon.apps.trustdo.constants.SDKConstants;
import com.seeyon.apps.trustdo.constants.TrustdoErrorMsg;
import com.seeyon.apps.trustdo.constants.XRDPhoneConstants;
import com.seeyon.apps.trustdo.exceptions.XRDUserException;
import com.seeyon.apps.trustdo.model.sdk.AccountData;
import com.seeyon.apps.trustdo.model.sdk.LoginData;
import com.seeyon.apps.trustdo.model.sdk.Result;
import com.seeyon.apps.trustdo.model.sdk.SDKLoginEventData;
import com.seeyon.apps.trustdo.utils.M3HttpServletRequest;
import com.seeyon.apps.trustdo.utils.MDesUtil;
import com.seeyon.apps.trustdo.utils.XRDAppUtils;
import com.seeyon.apps.trustdo.utils.XRDHttpUtils;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.LoginConstants;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.annotation.NeedlessCheckLogin;
import com.seeyon.ctp.util.json.JSONUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.logging.Log;

/**
 * 
 * M３-手机盾扫码登录控制器
 * @author zhaop
 *
 */
@NeedlessCheckLogin
public class XRDM3Controller extends BaseController {
	
	private static final Log LOGGER = CtpLogFactory.getLog(XRDM3Controller.class);
	
	private M3HttpServletRequest fillRequest(HttpServletRequest request)
			throws BusinessException {
		LOGGER.debug("m3Account~~~~~~~~~~"+request.getParameter("m3Account"));
		M3HttpServletRequest mRequest = new M3HttpServletRequest(request);
		Enumeration<String> paramsNames = request.getParameterNames();
		while (paramsNames.hasMoreElements()) {
			String key = paramsNames.nextElement();
			String value = request.getParameter(key);
			LOGGER.debug("key~~~~~~~~~~"+key);
			LOGGER.debug("value~~~~~~~~~~"+value);
			if (LoginConstants.PASSWORD.equals(key)
					|| LoginConstants.USERNAME.equals(key)) {
				mRequest.setParameter(key, MDesUtil.decode(value));
			} else {
				mRequest.setParameter(key, value);
			}
		}
		return mRequest;
	}
	
	/**
	 * MOKEY-SDK获取登录事件二维码
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	@NeedlessCheckLogin
	public void getSDKLoginEvent(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		LOGGER.debug("getSDKLoginEvent--------------------------start!");
		String appKeySign = null;
		String result = "";
		Map<String, Object> signMap = new HashMap<String, Object>();
		signMap.put("appKey", XRDPhoneConstants.APP_KEY);
		try {
			appKeySign = XRDAppUtils.getMd5Sign(signMap, XRDPhoneConstants.APP_SECRET);
		} catch (XRDUserException e1) {
			LOGGER.debug("getLoginUserInfoFromMobileShield getMd5Sign Error!");
			//e1.printStackTrace();
		}

		String url = SDKConstants.SDK_EVENT_LOGIN_URL;
		LOGGER.debug("getSDKLoginEvent url:" + url);
		
		NameValuePair[] data = {
				new NameValuePair("appKey", XRDPhoneConstants.APP_KEY),
				new NameValuePair("sign", appKeySign) };

		Result<?> sdkLoginEventResult = XRDHttpUtils.sdkConnectMobileShieldServer(data, url, SDKLoginEventData.class);
		if (sdkLoginEventResult != null && sdkLoginEventResult.getErrCode().equals(TrustdoErrorMsg.SUCCESS_CODE)) {
			LOGGER.debug("getSDKLoginEvent sdkLoginEventResult:" + JSONUtil.toJSONString(sdkLoginEventResult));
			result = JSONUtil.toJSONString(sdkLoginEventResult);
		}
		this.trustdoOutput(response, result);
	}
	
	/**
	 * 获取keyId
	 * @param request
	 * @param response
	 * @throws IOException 
	 * @throws BusinessException 
	 */
	@NeedlessCheckLogin
	public void getSDKKeyId(HttpServletRequest request, HttpServletResponse response) throws IOException, BusinessException {
		LOGGER.debug("getKeyId~~~~~~~~~~~~~~~~~~~~~~~~~~~~start!");
		String appKeySign = null;
		String account = "";
		String result = "";
		M3HttpServletRequest mRequest = fillRequest(request);
		account = mRequest.getParameter("m3Account");
		LOGGER.debug("getKeyId account:" + account);
		
		Map<String, Object> signMap = new HashMap<String, Object>();
		signMap.put("appKey", XRDPhoneConstants.APP_KEY);
		signMap.put("account", account);
		try {
			appKeySign = XRDAppUtils.getMd5Sign(signMap, XRDPhoneConstants.APP_SECRET);
		} catch (XRDUserException e1) {
			LOGGER.debug("getKeyId getMd5Sign Error!");
			//e1.printStackTrace();
		}

		String url = SDKConstants.SDK_KEYID_URL;
		LOGGER.debug("getKeyId url:" + url);
		
		NameValuePair[] data = {
				new NameValuePair("appKey", XRDPhoneConstants.APP_KEY),
				new NameValuePair("account", account),
				new NameValuePair("sign", appKeySign) };

		Result<?> keyIdResult = XRDHttpUtils.sdkConnectMobileShieldServer(data, url, AccountData.class);
		if (keyIdResult != null && keyIdResult.getErrCode().equals(TrustdoErrorMsg.SUCCESS_CODE)) {
			LOGGER.debug("getKeyId keyIdResult:" + JSONUtil.toJSONString(keyIdResult));
			result = JSONUtil.toJSONString(keyIdResult);
		}
		this.trustdoOutput(response, result);
	}
	
	/**
	 * 证书更新
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	@NeedlessCheckLogin
	public void getSDKUpdateCertEvent(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		LOGGER.debug("getSDKUpdateCertEvent~~~~~~~~~~~~~~~~~~~~~~~~~~~~start!");
		String appKeySign = null;
		String result = "";
		String account = request.getParameter("m3Account");
		LOGGER.debug("getSDKUpdateCertEvent account:" + account);
		
		Map<String, Object> signMap = new HashMap<String, Object>();
		signMap.put("appKey", XRDPhoneConstants.APP_KEY);
		signMap.put("account", account);
		try {
			appKeySign = XRDAppUtils.getMd5Sign(signMap, XRDPhoneConstants.APP_SECRET);
		} catch (XRDUserException e1) {
			LOGGER.debug("getSDKUpdateCertEvent getMd5Sign Error!");
			//e1.printStackTrace();
		}

		String url = SDKConstants.SDK_EVENT_UPDATE_CERT_URL;
		LOGGER.debug("getSDKUpdateCertEvent url:" + url);
		
		NameValuePair[] data = {
				new NameValuePair("appKey", XRDPhoneConstants.APP_KEY),
				new NameValuePair("account", account),
				new NameValuePair("sign", appKeySign) };

		Result<?> updateCertEventResult = XRDHttpUtils.sdkConnectMobileShieldServer(data, url, AccountData.class);
		if (updateCertEventResult != null && updateCertEventResult.getErrCode().equals(TrustdoErrorMsg.SUCCESS_CODE)) {
			LOGGER.debug("getSDKUpdateCertEvent updateCertEventResult:" + JSONUtil.toJSONString(updateCertEventResult));
			result = JSONUtil.toJSONString(updateCertEventResult);
		}
		
		this.trustdoOutput(response, result);
	}
	
	/**
	 * 获取M3账户名
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	@NeedlessCheckLogin
	public void getSDKLoginName(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		LOGGER.debug("getSDKLoginName~~~~~~~~~~~~~~~~~~~~~~~~~~~~start!");
		String appKeySign = null;
		String result = null;
		String accToken = request.getParameter("accToken");
		LOGGER.debug("getSDKLoginName accToken:" + accToken);
		
		Map<String, Object> signMap = new HashMap<String, Object>();
		signMap.put("appKey", XRDPhoneConstants.APP_KEY);
		signMap.put("accToken", accToken);
		try {
			appKeySign = XRDAppUtils.getMd5Sign(signMap, XRDPhoneConstants.APP_SECRET);
		} catch (XRDUserException e1) {
			LOGGER.debug("getSDKLoginName getMd5Sign Error!");
			//e1.printStackTrace();
		}

		String url = SDKConstants.SDK_GET_TOKEN_URL;
		LOGGER.debug("getSDKLoginName url:" + url);
		
		NameValuePair[] data = {
				new NameValuePair("appKey", XRDPhoneConstants.APP_KEY),
				new NameValuePair("accToken", accToken),
				new NameValuePair("sign", appKeySign) };

		Result<?> loginResult = XRDHttpUtils.sdkConnectMobileShieldServer(data, url, LoginData.class);
		if (loginResult != null && loginResult.getErrCode().equals(TrustdoErrorMsg.SUCCESS_CODE)) {
			LOGGER.debug("getSDKLoginName loginResult:" + JSONUtil.toJSONString(loginResult));
			result = JSONUtil.toJSONString(loginResult);
			LOGGER.debug("getBizLoginName resultData : " + result);
		}
		
		this.trustdoOutput(response, result);
	}
	
	/**
	 * 返回是否开启（加载）了trustdo插件
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	@NeedlessCheckLogin
	public void getTrustdoIsOpen(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		LOGGER.debug("getTrustdoIsOpen~~~~~~~~~~~~~~~~~~~~~~~~~~~~start!");
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

	/**
	 * 根据token置换手机盾用户信息
	 * @param request
	 * @param response
	 * @return
	 *//*
	@NeedlessCheckLogin
	public void getLoginAccountFromMobileShield(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String token = request.getParameter("accToken");
		XRDScanResult scanResult = null;
		String resultJson = null;
		String login_username = null;
		Map<String, String> loginMap = new HashMap<String, String>();
		if (token != null) {
			scanResult = this.getLoginUserInfoFromMobileShield(token, request,response);
		}
		if (scanResult != null) {
			login_username = scanResult.getBizAccount();
		} 
		if ((login_username != null) && (!("".equals(login_username)))) {
			V3xOrgMember orgMember = null;
			try {
				orgMember = this.orgManager.getMemberByLoginName(login_username);
			} catch (BusinessException e) {
				e.printStackTrace();
			}
			if (orgMember != null) {
				V3xOrgPrincipal orgPrincipal = orgMember.getV3xOrgPrincipal();
				String loginName = orgPrincipal.getLoginName();
				if ((loginName != null) && (!("".equals(loginName)))) {
					loginMap.put("username", loginName);
				}
			}
		}
		resultJson = JSONUtil.toJSONString(loginMap);
		Cookie c = new Cookie("resultJson", resultJson);
		response.addCookie(c);
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=utf-8");
		PrintWriter pw = response.getWriter();
		pw.print(resultJson);
		pw.close();
	}*/
	

	/**
	 * 根据token置换手机盾用户信息
	 * @param token
	 * @param request
	 * @param response
	 * @return
	 *//*
	private XRDScanResult getLoginUserInfoFromMobileShield(String token,
			HttpServletRequest request, HttpServletResponse response) {
		String appKeySign = null;
		Map<String, Object> signMap = new HashMap<String, Object>();
		signMap.put("appKey", XRDPhoneConstants.APP_KEY);
		signMap.put("accToken", token);
		try {
			appKeySign = XRDAppUtils.getMd5Sign(signMap, XRDPhoneConstants.APP_SECRET);
		} catch (XRDUserException e1) {
			//LOGGER.debug("getLoginUserInfoFromMobileShield getMd5Sign Error!");
			e1.printStackTrace();
		}
		String url = XRDPhoneConstants.REPLACEMENT_URL;
		NameValuePair[] data = {
				new NameValuePair("appKey", XRDPhoneConstants.APP_KEY),
				new NameValuePair("accToken", token),
				new NameValuePair("sign", appKeySign) };
		XRDScanResult scanResult = XRDHttpUtils.repMobileShieldServer(data, url);
		return scanResult;
	}*/
	
}