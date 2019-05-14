package com.seeyon.apps.jzMsg;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.common.kit.HttpKit;
import com.seeyon.apps.common.kit.JsonKit;
import com.seeyon.apps.jzMsg.po.JzMsgPo;
import com.seeyon.apps.jzMsg.po.JzReceiver;
import com.seeyon.apps.jzMsg.res.ReturnRes;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.usermessage.pipeline.Message;
import com.seeyon.ctp.common.usermessage.pipeline.MessagePipeline;
import com.seeyon.ctp.organization.bo.V3xOrgMember;

import www.seeyon.com.utils.MD5Util;

/**
 * @author Fangaowei
 * 
 *         <pre>
 *         </pre>
 * 
 * @date 2018年10月22日 上午10:27:44 @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class JzMsgPipeline implements MessagePipeline {

    private static final Log log = LogFactory.getLog(JzMsgPipeline.class);

    private String appId;

    private String accessToken;

    private String schoolCode;

    private String msgUrl;

    @Override
    public List<Integer> getAllowSettingCategory(User arg0) {
        return null;
    }

    @Override
    public String getName() {
        return "jzMsgPush";
    }

    @Override
    public String getShowName() {
        return "金智消息通道";
    }

    @Override
    public int getSortId() {
        return 100;
    }

    @Override
    public void invoke(Message[] messages) {
        for(Message oaMsg : messages) {
        	System.out.println(oaMsg.getBodyContent());
        	System.out.println(oaMsg.getRemoteURL());
            /*try {
                String remoteURL = oaMsg.getRemoteURL();
                V3xOrgMember receive = oaMsg.getReceiverMember();
                remoteURL += "&M=" + receive.getId();
                JzMsgPo msg = new JzMsgPo();
                // 登录名
                String receiveName = receive.getLoginName();
                msg.setSchoolCode(schoolCode);
                msg.addReceiver(new JzReceiver(receiveName));
                msg.setSign(getSign(receiveName));
                msg.setAppId(appId);
                msg.setContent(oaMsg.getContent());
                msg.setPcUrl(remoteURL);
                msg.setSubject("OA消息");
                String res = HttpKit.post(msgUrl, JsonKit.toJson(msg), appId, accessToken);
                ReturnRes returnMsg = JsonKit.parse(res, ReturnRes.class);
                if(returnMsg.getStatus() != 200) {
                    log.error("金智消息推送失败：" + returnMsg.getMsg());
                }
            } catch(Exception e) {
                log.error("金智消息推送异常", e);
            }*/
        }
    }

    @Override
    public String isAllowSetting(User user) {
        return null;
    }

    @Override
    public boolean isAvailability() {
        return true;
    }

    @Override
    public boolean isDefaultSend() {
        return true;
    }

    @Override
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
