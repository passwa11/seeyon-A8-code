package com.seeyon.apps.trustdo.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.logging.Log;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.ctp.util.annotation.NeedlessCheckLogin;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.apps.trustdo.constants.XRDPhoneConstants;
import com.seeyon.apps.trustdo.exceptions.XRDUserException;
import com.seeyon.apps.trustdo.model.XRDPhoneResult;
import com.seeyon.apps.trustdo.model.XRDScanResult;
import com.seeyon.apps.trustdo.model.XRDTokenResult;
import com.seeyon.apps.trustdo.utils.XRDAppUtils;
import com.seeyon.apps.trustdo.utils.XRDHttpUtils;

/**
 * 
 * 手机盾插件登录控制器
 * @author zhaop
 *
 */
@NeedlessCheckLogin
public class XRDMobileShieldQRCodeController extends BaseController{

	private static final Log LOGGER = CtpLogFactory.getLog(XRDMobileShieldQRCodeController.class);
	
	/**
	 * 全局变量
	 */
	String appUrl = "";
	String charset = "utf-8";
	private Map<String , Boolean> resultMap = new HashMap<String , Boolean>();
	
	/**
	 * 获取手机盾登录请求的二维码
	 * @throws IOException 
	 */
	@AjaxAccess
	@NeedlessCheckLogin
	public void getLoginQRCodeFromMobileShield(HttpServletRequest request, HttpServletResponse response) throws IOException{
		/*
		 * 一.准备请求手机盾接口参数
		 */
		//签名值
		String appKeySign = null;
		//对参数签名进行组包,将加密参数封装为一个Map集合
		HashMap<String, Object> signMap =  new HashMap<String, Object>();
		signMap.put("appKey", XRDPhoneConstants.APP_KEY);
		try {
			appKeySign = XRDAppUtils.getMd5Sign(signMap, XRDPhoneConstants.APP_SECRET);
		} catch (XRDUserException e1) {
			LOGGER.debug("getLoginQRCodeFromMobileShield getMd5Sign Error!");
			//e1.printStackTrace();
		}
		//请求路径
		String url = XRDPhoneConstants.CREATE_LOGIN_URL;

		/*
		 * 二.对请求参数组包
		 */
		NameValuePair[] data = { new NameValuePair("appKey", XRDPhoneConstants.APP_KEY) //应用系统注册手机盾ID,有手机盾系统发给应用系统
				,new NameValuePair("sign", appKeySign)//签名数据
		};

		/*
		 * 三.调用手机盾接口,发送http请求
		 */
		XRDPhoneResult phoneResult = XRDHttpUtils.connectMobileShieldServer(data, url);

		/*
		 * 四.处理返回结果
		 */
		//判断返回路径是否为空
		String eventId = "";
		if (phoneResult != null && phoneResult.getCode() == 200) {
			//修改全局变量
			appUrl = phoneResult.getData().getUrl();
			resultMap.put(phoneResult.getData().getEventId(), false);
			eventId = phoneResult.getData().getEventId();
		}
		//对返回页面参数组包,将参数封装成json数据
		if(phoneResult != null){
			String resultDate = XRDHttpUtils.resultData(phoneResult.getCode(), phoneResult.getMsg(), appUrl, eventId, "");
			this.trustdoOutput(response, resultDate);
		}		
	}
	
	/**
	 * 根据事件eventId获取token
	 * @throws IOException 
	 * @throws ServletException 
	 */
	@AjaxAccess
	@NeedlessCheckLogin
	public void getLoginAcctokenFromMobileShield(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String eventId = request.getParameter("eventId");
		
		XRDTokenResult token = null;
		String url = XRDPhoneConstants.GET_TOKEN;
		if (eventId!=null && !"".equals(eventId)){
			NameValuePair[] data = { new NameValuePair("eventId", eventId)};
			//获取token
			token = XRDHttpUtils.getToken(url, data);
		}
		this.trustdoOutput(response, JSONUtil.toJSONString(token==null?"":token));
	}
	
	/**
	 * 根据token置换用户信息并登录
	 * @throws IOException 
	 * @throws ServletException 
	 */
	@AjaxAccess
	@NeedlessCheckLogin
	public void getLoginAccountFromMobileShield(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String token = request.getParameter("accToken");
		XRDScanResult scanResult = null;
		if (token!=null){
			//获取用户信息并登录
			scanResult = this.getLoginUserInfoFromMobileShield(token, request, response);
		}
		this.trustdoOutput(response, JSONUtil.toJSONString(scanResult==null?"":scanResult));
	}
	
	/**
	 * 获取手机盾扫码登录的客户信息
	 */
	private XRDScanResult getLoginUserInfoFromMobileShield(String token, HttpServletRequest request, HttpServletResponse response){
		/*
		 * 一.准备请求手机盾接口参数
		 */
		//签名值
		String appKeySign = null;
		//对参数签名进行组包,将加密参数封装为一个Map集合
		HashMap<String, Object> signMap =  new HashMap<String, Object>();
		signMap.put("appKey", XRDPhoneConstants.APP_KEY);
		signMap.put("accToken", token);
		try {
			appKeySign = XRDAppUtils.getMd5Sign(signMap, XRDPhoneConstants.APP_SECRET);
		} catch (XRDUserException e1) {
			LOGGER.debug("getLoginUserInfoFromMobileShield getMd5Sign Error!");
			//e1.printStackTrace();
		}
		//请求路径
		String url = XRDPhoneConstants.REPLACEMENT_URL;

		/*
		 * 二.对请求参数组包
		 */
		NameValuePair[] data = { new NameValuePair("appKey", XRDPhoneConstants.APP_KEY) 
				,new NameValuePair("accToken", token)
				,new NameValuePair("sign", appKeySign)
		};

		/*
		 * 三.调用手机盾接口,发送http请求
		 */
		XRDScanResult scanResult = XRDHttpUtils.repMobileShieldServer(data, url);
		return scanResult;
		
	}
	
	public ModelAndView test(HttpServletRequest request, HttpServletResponse response){
		ModelAndView view = new ModelAndView("plugin/xrd/login");
		return view;
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
}
