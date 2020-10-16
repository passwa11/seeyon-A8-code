package com.seeyon.apps.trustdo.constants;

import com.seeyon.ctp.common.AppContext;

public class SDKConstants {
	//手机盾服务器IP
	public static final String REQUEST_RUL = AppContext.getSystemProperty("trustdo.mokeyUrl");
	//商户ID                              
	public static final String APP_KEY = AppContext.getSystemProperty("trustdo.appkey");
	//私钥
	public static final String APP_SECRET = AppContext.getSystemProperty("trustdo.secret");
	//SDK登录事件请求路径
	public static final String SDK_EVENT_LOGIN_URL = REQUEST_RUL + "/biz/sdk/event/login";
	//SDK重置事件请求路径
	public static final String SDK_EVENT_RESET_URL = REQUEST_RUL + "/biz/sdk/event/reset";
	//SDk获取登录票据请求路径
	public static final String SDK_GET_TOKEN_URL = REQUEST_RUL + "/biz/result/login";
	//账户解锁
	public static final String ACCOUNT_UNLOCK_URL = REQUEST_RUL + "/biz/account/unlock";
	//SDk更新证书事件请求路径
	public static final String SDK_EVENT_UPDATE_CERT_URL = REQUEST_RUL + "/biz/sdk/event/update_cert";
	//Web获取登录票据请求路径
	public static final String WEB_GET_TOKEN_URL = REQUEST_RUL + "/biz/result/long_connection";
	//Web重置事件请求路径
	public static final String WEB_EVENT_RESET_URL = REQUEST_RUL + "/biz/web/event/reset";
	//Web登录二维码事件请求路径
	public static final String WEB_EVENT_LOGIN_URL = REQUEST_RUL + "/biz/web/event/login";
	//获取SDK用户keyId请求路径
	public static final String SDK_KEYID_URL = REQUEST_RUL + "/biz/account/info";
	//pc获取账户请求url
	public static final String SDK_LOGIN_NAME_URL = REQUEST_RUL + "/biz/result/login";
	//SDK客户端通讯证书 add 2018-05-31 
	public static final String SDK_COMMUNICATION_CERT = AppContext.getSystemProperty("trustdo.sdkCert");
		
}
