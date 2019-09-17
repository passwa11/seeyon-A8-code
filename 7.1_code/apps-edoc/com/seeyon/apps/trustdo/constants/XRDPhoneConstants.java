package com.seeyon.apps.trustdo.constants;

import com.seeyon.ctp.common.AppContext;

public class XRDPhoneConstants {
	
	//手机盾服务器IP
	public static final String REQUEST_RUL = AppContext.getSystemProperty("trustdo.mokeyUrl");
	//商户ID                              
	public static final String APP_KEY = AppContext.getSystemProperty("trustdo.appkey");
	//私钥
	public static final String APP_SECRET = AppContext.getSystemProperty("trustdo.secret");
	//待签名数据（测试）
	public static final String ORIGINAL_DATA = "88888888";
	//根据eventId获取token
	public static final String GET_TOKEN = REQUEST_RUL + "/v2/biz/longUserCheck";
	//登录二维码请求路径
	public static final String CREATE_LOGIN_URL = REQUEST_RUL + "/biz/createLogin";
	//登录请求回调路径
	public static final String LOGIN_CALL_BACK_URL =REQUEST_RUL + "/Http_phone/toLogin";
	//绑定二维码请求路径
	public static final String CREATE_BIND_URL = REQUEST_RUL + "/biz/realAuthBind";
	//签名二维码请求路径
	public static final String CREATE_SIGN_URL = REQUEST_RUL + "/biz/createSign";
	//签章二维码请求路径
	public static final String CREATE_STAMP_URL = REQUEST_RUL + "/biz/createStamp";
	//签批二维码请求路径
	public static final String CREATE_COMMENT_URL = REQUEST_RUL + "/biz/createComment";
	//重置二维码请求路径
	public static final String CREATE_RESET_URL = REQUEST_RUL + "/biz/createReset";
	//批量导入接口
	public static final String REAL_AUTH_IMPORT_URL = REQUEST_RUL + "/biz/realAuthImport";
	//重设手机
	public static final String CREATE_CHANGE_DEVICE = REQUEST_RUL + "/biz/createChangeDevice";
	//手机根据票据置换接口路径replacement
	public static final String REPLACEMENT_URL = REQUEST_RUL + "/v2/biz/scanResult";
	//绑定请求回调路径
	public static final String PHONE_CALL_BACK_URL = REQUEST_RUL + "/Http_phone/toPhoneUrl";
	//用户绑定手机盾请求路径
	public static final String USER_BINDING = REQUEST_RUL +"/biz/batchImport";
	//用户绑定手机盾请求路径
	public static final String NEW_USER_BINDING = REQUEST_RUL +"/biz/import/anonymity";
	//用户绑定手机盾请求路径
	public static final String NEW_REMOVE_USER = REQUEST_RUL +"/biz/account/cancellation";
	//用户解除绑定手机盾请求路径
	public static final String USER_REMMOVE_BINDING = REQUEST_RUL +"/biz/batchReleaseBinding";
	//获取手机盾用户keyId请求路径
	public static final String GET_KEYID_URL = REQUEST_RUL + "/biz/account/info";
	
}
