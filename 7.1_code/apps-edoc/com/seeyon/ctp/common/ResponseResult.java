/*
 * $Revision: 53353 $
 * $Date: 2017-03-17 15:04:44 +0800 (周五, 17 三月 2017) $
 * $Id: ResponseResult.java 53353 2017-03-17 07:04:44Z xinx $
 * ====================================================================
 * Copyright © 2012 Beijing seeyon software Co..Ltd..All rights reserved.
 *
 * This software is the proprietary information of Beijing seeyon software Co..Ltd.
 * Use is subject to license terms.
 */

package com.seeyon.ctp.common;

import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * M3统一数据返回接口
 * @author hejianliang
 *
 * @param 
 */
public class ResponseResult implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String M3_VERSION = SystemProperties.getInstance().getProperty("m3.version");

	private String key_code =  "code";
	private String key_message = "message";
	private String key_data = "data";
	private String key_time ="time";
	private String key_version="version";
	private String key_identifier="identifier";//本地请求的数据的md5值
	private String key_total="total";
	private String key_pageNo="pageNo";
	private String key_pageSize="pageSize";
	private Map<String,Object> result= null;
	public ResponseResult() {
		result= new HashMap<String, Object>();
	}

	public ResponseResult(ResultCode resultCode) {
		this(null, resultCode.getCode(), resultCode.getName(), new Date(), M3_VERSION);
	}

	public ResponseResult(ResultCode resultCode, Object data) {
		this(data, resultCode.getCode(), resultCode.getName(), new Date(), M3_VERSION);
	}
	public ResponseResult(int code, String message) {
		this(null, code, message, new Date(), M3_VERSION);
	}

	public ResponseResult(Object data) {
		this(data, ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getName(), new Date(), M3_VERSION);
	}
	public ResponseResult(Object data, int code, String message, Date time,
                          String version) {
		result= new HashMap<String, Object>();
		if(data != null) {
			result.put(key_data, data);
		}
		result.put(key_code, code);
		if(Strings.isNotBlank(message)){
			result.put(key_message, message);
		}
		if(time != null) {
			result.put(key_time, time);
		}
		if(Strings.isNotBlank(version)){
			result.put(key_version, version);
		}else {
			result.put(key_version, M3_VERSION);
		}
	}
	//组装分页数据格式
	public ResponseResult(FlipInfo flipInfo, int code, String message){
		result= new HashMap<String, Object>();
		List flipData = flipInfo.getData();
		if(flipData != null){
			result.put(key_data, flipData);
		}
		result.put(key_code, code);
		if(Strings.isNotBlank(message)){
			result.put(key_message, message);
		}
		result.put(key_version, M3_VERSION);
		result.put(key_time, new Date());
		result.put(key_total,flipInfo.getTotal());
		result.put(key_pageNo,flipInfo.getPage());
		result.put(key_pageSize,flipInfo.getSize());
	}
	public ResponseResult(int code, Object data, String identifier) {
		this(data, code, ResultCode.SUCCESS.getName(), new Date(), M3_VERSION,identifier);
	}
	public ResponseResult(Object data, int code, String message, Date time,
                          String version, String identifier) {
		result= new HashMap<String, Object>();
		if(data != null) {
			result.put(key_data, data);
		}
		result.put(key_code, code);
		if(Strings.isNotBlank(message)){
			result.put(key_message, message);
		}
		if(time != null) {
			result.put(key_time, time);
		}
		if(Strings.isNotBlank(version)){
			result.put(key_version, version);
		}
		if(Strings.isNotBlank(identifier)){
			result.put(key_identifier, identifier);
		}
	}
	public Map<String,Object> build(){
		return result;
	}
//	public static void main(String[] args) {
//		System.out.println(new ResponseResult(new Date()).build());
//	}
}
