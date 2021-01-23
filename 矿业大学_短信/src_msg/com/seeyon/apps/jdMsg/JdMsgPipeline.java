package com.seeyon.apps.jdMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.usermessage.pipeline.Message;
import com.seeyon.ctp.common.usermessage.pipeline.MessagePipeline;
import com.seeyon.v3x.mobile.message.domain.AppMessageRule;
import com.seeyon.v3x.mobile.message.manager.MobileMessageManager;
import com.seeyon.v3x.mobile.message.manager.MobileMessageManagerImpl;

import www.seeyon.com.utils.MD5Util;


public class JdMsgPipeline implements MessagePipeline {

    private static final Log log = LogFactory.getLog(JdMsgPipeline.class);

    private MobileMessageManager mobileMessageManager = new MobileMessageManagerImpl();

    private String appId;

    private String accessToken;

    private String schoolCode;

    private String msgUrl;

    @Override
    //得到哪些应用类别可以配置(参考 ApplicationCategoryEnum),
    //如果采用系统默认,请直接返回null,如果不允许配置请参考 isAllowSetting(User currentUser)
    public List<Integer> getAllowSettingCategory(User currentUser) {

        List<Integer> enabledAppEnum = new ArrayList<Integer>();
        Map<Integer, AppMessageRule> messageRuleMap = mobileMessageManager.getAppMessageRules();
        for (Integer i : messageRuleMap.keySet()) {
            if (messageRuleMap.get(i) != null) {
                enabledAppEnum.add(i);
            }
        }
        //ApplicationCategoryEnum applicationCategoryEnum = ApplicationCategoryEnum.global;
        //applicationCategoryEnum.getKey();
        return enabledAppEnum;

        //return null
    }

    @Override
    public String getName() {
        return "jdMsgPush";
    }

    @Override
    public String getShowName() {
        return "金迪消息通道";
    }

    @Override
    public int getSortId() {
        return 100;
    }

    @Override
    public void invoke(Message[] messages) {
//        System.out.println("短信内容：");
    }

    @Override
    //是否允许用户在“个人设置-消息提示设置”中进行配 置；
    //如：短信提示，需要后台管理员授权，并且设置了个人 的手机号码，人员是当前登录者，允许配置返回 null， 否则返回提示信息（注意国际化）
    public String isAllowSetting(User user) {
        return null;
    }

    @Override
    public boolean isAvailability() {
        return true;
    }

    @Override
    //默认是否发送，如果为 false，则个人需要在“个人设 置-消息提示设置”中进行配置
    public boolean isDefaultSend() {
        return true;
    }

    @Override
    //  如果 isDefaultSend 设置为false,则个人需要设置为true
    public boolean isShowSetting() {
        return true;
    }

    private String getSign(String userId) {
        String key = accessToken + schoolCode + userId;
        return MD5Util.MD5(key).toLowerCase();
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getSchoolCode() {
        return schoolCode;
    }

    public void setSchoolCode(String schoolCode) {
        this.schoolCode = schoolCode;
    }

    public String getMsgUrl() {
        return msgUrl;
    }

    public void setMsgUrl(String msgUrl) {
        this.msgUrl = msgUrl;
    }
}
