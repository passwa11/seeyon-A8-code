package com.seeyon.ctp.common;


public enum ResultCode {

    INVALID_RESULT_VALUE("return value is invalid", -1),
    /**
     * 数据返回成功
     */
    SUCCESS("success", 200),
    /**
     * Session 过期  重新登录
     */
    RESULT_CODE_REQUEST_ERROR_SESSION_OVERDUE("answer has expired, please login again", 401),
    /**
     * 服务器异常
     */
    RESULT_CODE_SEVER_ERROR("internal server error", 500),
    /**
     * 请求参数为空illegal
     */
    EMPTER_REQUEST_PARAMETERS_ERROR("request parameters cannot be empty", 500001),
    /**
     * 非法请求参数
     */
    ILLEGAL_REQUEST_PARAMETERS_ERROR("illegal request parameters", 500002),
    /**
     * 无效的请求地址
     */
    RESULT_CODE_NOT_FOUND("not found", 404),
    /**
     * 注册推送设备失败
     */
    RESULT_CODE_PNS_REGISTER_DEVICE_ERROR("Register Device Error", 6001),
    /**
     * 无效的MD5码
     */
    INVALID_MD5("invalid md5", 500003),
    /**
     * 无法获取消息推送设置信息
     */
    NOT_PNS_SETTINGS("Not PNS Settings", 500005),
    /**
     * md5有变化
     */
    CHENGED_MD5("chenged md5", 200001),
    /**
     * md5无变化
     */
    NOT_CHENGE_MD5("chenged md5", 304001),
    /**
     * 无法获取应用信息
     */
    NOT_APP_INFO("not app info", 500004),
    /**
     * 用户未授权
     */
    USER_NOT_AUTHORIZED("m3.user.not.authorized", -3003),
    /**
     * 用户不存在
     */
    USER_NOT_EXIST("m3.user.not.exist", -3001),

    USER_NOT_EXIST_LOIGN("login.label.ErrorCode.1", -3001),
    /**
     * 限制绑定设备登录
     */
    LIMIT_BINDED_LOGIN("m3.limit.binded.login", -3004),
    /**
     * 安全级别高，需要申请绑定
     */
    HIGH_SAFE_LEVEL("m3.high.safe.level", -3010),
    /**
     * 设备被其他用户绑定
     */
    BINDED_BY_OTHER("m3.binded.by.other", -3005),
    /**
     * 已绑定，但是被禁用
     */
    BINDED_BUT_LOGIN_FORBIDDEN("m3.bind.login.forbidden", -3002),
    /**
     * 设备型号与绑定信息不符
     */
    BINDED_BUT_MATCH_TYPE("m3.binded.match.type", -3006),

    /**
     * 请求类型正确
     */
    REQUEST_TYPE_ERROR("m3.request.type.error", -3011),

    /**
     * @description 离线推送服务器配置不存在
     * @author shawnyang
     * @date 2018/4/10 下午7:45
     **/
    PUSH_SERVER_CONFIG_NOT_EXIST("pushServer config is not exist", 10000),
    /**
     * @description 离线推送服务器链接失败
     * @author shawnyang
     * @date 2018/4/10 下午7:46
     **/
    PUSH_SERVER_CONNECT_FAIL("pushServer connect fail", 10001),

    /**
     * 错误的密码强度
     */
    PASSWORD_STRONG_ERROR("Password strong error", -1003),

    /**
     * M3 登录校验错误码，该设备被锁定
     */
    M3_LOGIN_ERROR_DEV_LOCK("m3.login.label.ErrorCode.lock", -3201),

    /**
     * M3 登录校验错误码，密码错误
     */
    M3_LOGIN_ERROR_PDERROR("m3.login.label.ErrorCode.ErrorAccount", -3202),

    /**
     * 登录错误，无效的验证码
     */
    M3_LOGIN_ERROR_VERIFYCODE("m3.login.label.ErrorCode.ErrorVerifcode", -3203),

    /**
     * 请求trustdo异常错误码
     */
    REQUEST_TRUSTDO_SDK_SERVER_EXCEPTION("request trustdo sdkServer exception", 9527),

    /****************************************热部署相关******************************************/

    HOT_DEPLOYMENT_SUCCESS("hotDeployment success",60000),

    HOT_DEPLOYMENT_M3FILES_NOT_EXIST("m3files app is not exist",60001),

    HOT_DEPLOYMENT_INIT_LOAD_APP_ERROR("initLoadApps is error",60002),

    HOT_DEPLOYMENT_UPLOAD_APP_ERROR("get upload app error",60003);


    /****************************************热部署相关******************************************/


    private String name;
    private int code;
    private String parms[];

    ResultCode() {
    }

    ResultCode(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static String getName(int code) {
        for (ResultCode l : ResultCode.values()) {
            if (l.getCode() == code) {
                return l.name;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public static ResultCode getResult(int code) {
        for (ResultCode result : ResultCode.values()) {
            if (result.getCode() == code) {
                return result;
            }
        }
        return INVALID_RESULT_VALUE;
    }

    public String[] getParms() {
        return parms;
    }

    public void setParms(String... parms) {
        this.parms = parms;
    }
}