package com.seeyon.apps.trustdo.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.imageio.ImageIO;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import sun.misc.BASE64Encoder;

import com.seeyon.apps.trustdo.constants.XRDPhoneConstants;
import com.seeyon.apps.trustdo.exceptions.XRDUserException;
import com.seeyon.apps.trustdo.manager.impl.XRDManagerImpl;
import com.seeyon.apps.trustdo.model.XRDBindModel;
import com.seeyon.apps.trustdo.model.XRDPhoneResult;
import com.seeyon.apps.trustdo.model.sdk.CancelData;
import com.seeyon.apps.trustdo.model.sdk.KeyIdData;
import com.seeyon.apps.trustdo.model.sdk.Result;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.json.JSONUtil;

public class XRDAppUtils {
	
	private static final Log LOGGER = CtpLogFactory.getLog(XRDAppUtils.class);
	/**
	 * 判断str格式是否为手机号
	 * @param str
	 * @return
	 * @throws PatternSyntaxException
	 */
	public static boolean isChinaPhoneLegal(String str) throws PatternSyntaxException {  
        String regExp = "^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$";  
        Pattern p = Pattern.compile(regExp);  
        Matcher m = p.matcher(str);  
        return m.matches();  
    }  
	
	public static XRDPhoneResult toBind(List<XRDBindModel> list){
		/** 一.准备请求手机盾接口参数 */
		// 签名值
		String appKeySign = null;
		// 对参数签名进行组包,将加密参数封装为一个Map集合
		HashMap<String, Object> signMap = new HashMap<String, Object>();
		signMap.put("appKey", XRDPhoneConstants.APP_KEY);
		signMap.put("list", JSONUtil.toJSONString(list));
		try {
			appKeySign = XRDAppUtils.getMd5Sign(signMap,
					XRDPhoneConstants.APP_SECRET);
		} catch (XRDUserException e1) {
			LOGGER.error(e1);
			//e1.printStackTrace();
		}
		/** 二.对请求参数组包 */
		NameValuePair[] data = {
				new NameValuePair("appKey", XRDPhoneConstants.APP_KEY),
				new NameValuePair("list", JSONUtil.toJSONString(list)),
				new NameValuePair("sign", appKeySign) };
		/** 三.调用手机盾接口,发送http请求 */
		XRDPhoneResult phoneResult = XRDHttpUtils.repMobileShieldServer2(data,
				XRDPhoneConstants.USER_BINDING);
		return phoneResult;
	}
	
	public static Result toNewBind(XRDBindModel bindModel){
		/** 一.准备请求手机盾接口参数 */
		// 签名值
		String appKeySign = null;
		// 对参数签名进行组包,将加密参数封装为一个Map集合
		HashMap<String, Object> signMap = new HashMap<String, Object>();
		signMap.put("appKey", XRDPhoneConstants.APP_KEY);
		signMap.put("account", bindModel.getAccount());
		signMap.put("phone", bindModel.getPhone());
		try {
			appKeySign = XRDAppUtils.getMd5Sign(signMap,
					XRDPhoneConstants.APP_SECRET);
		} catch (XRDUserException e1) {
			LOGGER.error(e1);
			//e1.printStackTrace();
		}
		/** 二.对请求参数组包 */
		NameValuePair[] data = {
				new NameValuePair("appKey", XRDPhoneConstants.APP_KEY),
				new NameValuePair("account", bindModel.getAccount()),
				new NameValuePair("phone", bindModel.getPhone()),
				new NameValuePair("sign", appKeySign) };
		/** 三.调用手机盾接口,发送http请求 */
		Result bindResult = XRDHttpUtils.sdkConnectMobileShieldServer(data, XRDPhoneConstants.NEW_USER_BINDING, KeyIdData.class);
		return bindResult;
	}
	
/*	public static XRDPhoneResult removeBind(List<String> list){
		*//** 一.准备请求手机盾接口参数 *//*
		// 签名值
		String appKeySign = null;
		// 对参数签名进行组包,将加密参数封装为一个Map集合
		HashMap<String, Object> signMap = new HashMap<String, Object>();
		signMap.put("appKey", XRDPhoneConstants.APP_KEY);
		signMap.put("list", JSONUtil.toJSONString(list));
		try {
			appKeySign = XRDAppUtils.getMd5Sign(signMap,XRDPhoneConstants.APP_SECRET);
		} catch (XRDUserException e1) {
			e1.printStackTrace();
		}
		*//** 二.对请求参数组包 *//*
		NameValuePair[] data = {
				new NameValuePair("appKey", XRDPhoneConstants.APP_KEY),
				new NameValuePair("list", JSONUtil.toJSONString(list)),
				new NameValuePair("sign", appKeySign) };
		*//** 三.调用手机盾接口,发送http请求 *//*
		XRDPhoneResult phoneResult = XRDHttpUtils.repMobileShieldServer2(data,
				XRDPhoneConstants.USER_REMMOVE_BINDING);
		return phoneResult;
	}*/
	
	public static Result removeBind(String account){
		/** 一.准备请求手机盾接口参数 */
		// 签名值
		String appKeySign = null;
		// 对参数签名进行组包,将加密参数封装为一个Map集合
		HashMap<String, Object> signMap = new HashMap<String, Object>();
		signMap.put("appKey", XRDPhoneConstants.APP_KEY);
		signMap.put("account", account);
		try {
			appKeySign = XRDAppUtils.getMd5Sign(signMap,XRDPhoneConstants.APP_SECRET);
		} catch (XRDUserException e1) {
			LOGGER.error(e1);
			//e1.printStackTrace();
		}
		/** 二.对请求参数组包 */
		NameValuePair[] data = {
				new NameValuePair("appKey", XRDPhoneConstants.APP_KEY),
				new NameValuePair("account", account),
				new NameValuePair("sign", appKeySign) };
		/** 三.调用手机盾接口,发送http请求 */
		Result removeResult = XRDHttpUtils.sdkConnectMobileShieldServer(data, XRDPhoneConstants.NEW_REMOVE_USER, CancelData.class);
		return removeResult;
	}
	
	/**
	 * MD5加密方法
	 * @param s
	 * @return
	 * @throws UserException
	 */
	public static String MD5Code(String s) throws XRDUserException {
		char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};       
        try {
            byte[] btInput = s.getBytes("utf-8");
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
        	LOGGER.error(e);
        	//e.printStackTrace();
            return null;
        }
	}
	
	/**
	   * map转成TreeMap
	   * 
	   * @param map
	   * @return
	   * @throws Exception
	   */
	  private static TreeMap<String, Object> toTreeMap(Map<String, Object> map) {
	    if(map == null){
	      throw new NullArgumentException("map");
	    }
	    TreeMap<String, Object> newTreeMap = new TreeMap<String, Object>();
	    Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
	    while (iterator.hasNext()) {
	      Entry<String, Object> next = iterator.next();
	      newTreeMap.put(next.getKey(), next.getValue());
	    }
	    return newTreeMap;
	  }

	  /**
	   * map参数计算签名：按照字典顺序对value拼装字符串，计算签名
	   * @param retMap 返回Map
	   * @param bizKey 应用系统secretKey
	   * @return
	 * @throws UserException 
	   */
	  public static String getMd5Sign(Map<String, Object> retMap, String bizKey) throws XRDUserException {
	    TreeMap<String, Object> treeMap = toTreeMap(retMap);
	    StringBuilder sb = new StringBuilder();
	    Set<Entry<String, Object>> entrySet = treeMap.entrySet();
	    for (Entry<String, Object> entry : entrySet) {
	      if (entry.getValue() != null && !StringUtils.equals(entry.getKey(), "sign")) {
	        sb.append(entry.getValue());
	      }
	    }
	    String toSignData = sb.append(bizKey).toString();
	    String sign = MD5Code(toSignData);
	    return sign;
	  }
	  
	  /**
	   * 将图片转换成base64格式进行存储
	   * @param imagePath
	   * @return
	   */
	  public static String bufferImageToBase64String(BufferedImage image) throws IOException {
	      String imageString = null;
	      ByteArrayOutputStream bos = new ByteArrayOutputStream();
	      try {
	          ImageIO.write(image, "png", bos);
	          byte[] imageBytes = bos.toByteArray();
	          BASE64Encoder encoder = new BASE64Encoder();
	          imageString = encoder.encode(imageBytes);
	          bos.close();
	      } catch (IOException e) {
	    	  LOGGER.error(e);
	    	  //e.printStackTrace();
	      }
	      return imageString;
	  }

}
