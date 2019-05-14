package com.seeyon.apps.jzMsg.po;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fangaowei
 * 
 *         <pre>
 * schoolCode   是   调用接口的学校对应的学校Id
sign    是   请求签名:（accessToken  + schoolCode + 第一个receivers 的userID ）的MD5值（32位小写）
appId   否   应用的appId，创建应用时，在应用管理平台生成，用来区分消息的来源，可能和头里面的不一样
senderId    否   消息发送者的用户ID，当消息为人发送而非系统发送的时候需要此字段
senderName  否   消息发送者的姓名
mailSender  否   邮件发件人，当sendType为3邮件时才有效，为空则发件人为系统默认。另外邮件发件人必须为系统设置的邮件服务器的用户，并开启了SMTP服务，每个学校对应一个邮件服务器，公司测试环境统一用**@wisedu.com
mailSenderPw    否   邮件发件人密码
subject 否   消息主题，发送短信时参数为空，其他消息类型必填
content 是   消息的容
pcUrl   否   pc端点击消息时的链接url
mobileUrl   否   手机端点击消息时的链接url
urlDesc 否   Url链接的描述，如“查看详情”“去处理”
sendType    是   消息发送类型，发送方式: 0.PC门户通知和移动校园同时发送（通常为此种方式） 1.只发送PC门户 2.只发送移动校园 3邮件 4短信 5微信
支持组合发送，组合编号为1、2、3、4、5的任意组合，例如135、234、35……，将分别发送对应的消息。 
sendNow 是   发送方式，是否立即发送，0:定时发送 1:立即发送 默认1
sendTime    否   选择定时发送模式时，填写消息发送的时间，10位时间戳，定时发送模式时必须
tagId   是   消息标签，通用标签对应关系详见下面的tagId对应关系表，如果要获取所有请调用接口10
attachments 否   附件id，调用附件上传接口后返回的id，暂时只有邮件和微信支持附件,上传附件接口为18，对于微信图片大小限制为<2M 普通文件的限制为<20M
receivers   是   消息接收人，receiver json数组，reciever的结构见下面的Receiver数据说明
wxSendType  否   当发送类型为5时此字段才会生效，此字段值为必须为：text（文本消息）、file（文件）、video（视频）、voice（声音）、image（图像）、news（新闻）、mpnews（图文）中之一。字段为空时默认值为mpnews。当此字段值为file、video、voice或image时必须上传附件且只能发送该附件，如果是mpnews则必须上传封面图片。
 *         </pre>
 * 
 * @date 2018年10月22日 上午10:42:25 @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class JzMsgPo {

    private String schoolCode;

    private String sign;

    private String appId;

    private String senderId;

    private String senderName;

    private String mailSender;

    private String mailSenderPw;

    private String subject;

    private String content;

    private String pcUrl;

    private String mobileUrl;

    private String urlDesc = "查看详情";

    private int sendType = 1;

    private boolean sendNow = true;

    private Long sendTime;

    private int tagId = 1012;

    private List<JzReceiver> receivers;

    public String getSchoolCode() {
        return schoolCode;
    }

    public void setSchoolCode(String schoolCode) {
        this.schoolCode = schoolCode;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMailSender() {
        return mailSender;
    }

    public void setMailSender(String mailSender) {
        this.mailSender = mailSender;
    }

    public String getMailSenderPw() {
        return mailSenderPw;
    }

    public void setMailSenderPw(String mailSenderPw) {
        this.mailSenderPw = mailSenderPw;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPcUrl() {
        return pcUrl;
    }

    public void setPcUrl(String pcUrl) {
        this.pcUrl = pcUrl;
    }

    public String getMobileUrl() {
        return mobileUrl;
    }

    public void setMobileUrl(String mobileUrl) {
        this.mobileUrl = mobileUrl;
    }

    public String getUrlDesc() {
        return urlDesc;
    }

    public void setUrlDesc(String urlDesc) {
        this.urlDesc = urlDesc;
    }

    public int getSendType() {
        return sendType;
    }

    public void setSendType(int sendType) {
        this.sendType = sendType;
    }

    public boolean isSendNow() {
        return sendNow;
    }

    public void setSendNow(boolean sendNow) {
        this.sendNow = sendNow;
    }

    public Long getSendTime() {
        return sendTime;
    }

    public void setSendTime(Long sendTime) {
        this.sendTime = sendTime;
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    public List<JzReceiver> getReceivers() {
        return receivers;
    }
    
    public void addReceiver(JzReceiver receiver) {
        if(null == receivers) {
            receivers = new ArrayList<JzReceiver>();
        }
        receivers.add(receiver);
    }
}
