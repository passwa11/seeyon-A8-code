package com.seeyon.v3x.mobile.adapter.sms;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.common.kit.HttpKit;
import com.seeyon.v3x.mobile.adapter.AdapterMobileMessageManger;
import com.seeyon.v3x.mobile.message.domain.MobileReciver;

/**
 * Description
 * <pre>短信插件</pre>
 * @author FanGaowei<br>
 * Date 2018年3月9日 下午4:12:33<br>
 * Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class AdaptMobileImpl implements AdapterMobileMessageManger {
	
	private static Log log = LogFactory.getLog(AdaptMobileImpl.class);
	
	private String account;
	private String pwd;
	private String url;
	
	public void setAccount(String account) {
		this.account = account;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String getName() {
		return "短信插件";
	}

	public boolean isAvailability() {
		return true;
	}

	@Override
	public boolean isSupportQueueSend() {
		return false;
	}

	@Override
	public boolean isSupportRecive() {
		return false;
	}

	@Override
	public List<MobileReciver> recive() {
		return null;
	}

	@Override
	public boolean sendMessage(Long messageId, String srcPhone, String destPhone, String content) {
		try {
			content = URLEncoder.encode(content, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		String msgUrl = url + "?account=" + account + "&pwd=" + pwd + "&mobile=" + destPhone + "&msg=" + content;
		try {
			String res = HttpKit.get(msgUrl);
			// 记录日志，可以在ctp.log里面看到
			log.info("发送短信结果：" + res);
		} catch (Exception e) {
			log.error("发送短信网络异常：" , e);
		}
		return true;
	}

	@Override
	public boolean sendMessage(Long messageId, String srcPhone, Collection<String> destPhoneList, String content) {
		for (String dest : destPhoneList) {
			sendMessage(messageId++, srcPhone, dest, content);
		}
		log.info("----------发送短信结束------------");
		return true;
	}
	
}
