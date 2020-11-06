package com.seeyon.apps.ext.messageSend.message;

import com.seeyon.apps.ext.messageSend.manager.oauthLoginManager;
import com.seeyon.apps.ext.messageSend.manager.oauthLoginManagerImpl;
import com.seeyon.apps.ext.messageSend.util.AppH5MessageUtil;
import com.seeyon.apps.ext.messageSend.util.PropUtils;
import com.seeyon.apps.weixin.usermessage.MessageUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.usermessage.pipeline.Message;
import com.seeyon.ctp.common.usermessage.pipeline.MessagePipeline;
import com.seeyon.ctp.organization.manager.MemberManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HwPushMessagePipeline implements MessagePipeline {

    Logger logger = LoggerFactory.getLogger(HwPushMessagePipeline.class);

    private oauthLoginManager manager = new oauthLoginManagerImpl();

    @Override
    public void invoke(Message[] messages) {
        //在这里推送消息
        int[] arr = {1, 2, 3, 4, 6};
        List list = Arrays.asList(arr);
        PropUtils propUtils = new PropUtils();
        for (Message message : messages) {
            //pcurl
            String pc = message.getRemoteURL();
            //h5地址
            String h5 = AppH5MessageUtil.getMessageJson(message, "weixin", null);
            String appDealUrl = propUtils.getOaUrl() + h5;
            Map<String, Object> map = new HashMap<>();
            map.put("clientId", "8809823444");
            map.put("fromUser", manager.selectCodeByLoginName(message.getSenderMember().getLoginName()));
            map.put("toUsers", manager.selectCodeByLoginName(message.getReceiverMember().getLoginName()));
            map.put("msg", message.getContent());
            map.put("url", pc);
            map.put("url1", appDealUrl);
            map.put("type", "other");
            map.put("title", "消息提醒");
            map.put("push", "1");
            if (list.contains(message.getCategory())) {
                map.put("msgType", "0");
            } else {
                map.put("msgType", "1");
            }
            HttpClientUtil.toPost(map);
        }

    }

    @Override
    public int getSortId() {
        return 6;
    }

    @Override
    public String getName() {
        return "hw";
    }

    @Override
    public String getShowName() {
        return "消息推送给华网";
    }

    @Override
    public boolean isAvailability() {
        return true;
    }

    @Override
    public boolean isShowSetting() {
        return true;
    }

    @Override
    public String isAllowSetting(User user) {
        return null;
    }

    @Override
    public List<Integer> getAllowSettingCategory(User user) {
        return null;
    }

    @Override
    public boolean isDefaultSend() {
        return true;
    }
}
