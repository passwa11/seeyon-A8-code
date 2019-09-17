package com.seeyon.apps.trustdo.model;

/**
 * 二维码接口返回值模型
 * @author zhaopeng
 *
 */
public class XRDPhoneResult {
	private String version;//版本
	private int code;//状态码
	private String msg;//返回信息
	private XRDData data;//结果签名数据

	public XRDPhoneResult() {
		super();
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public XRDData getData() {
		return data;
	}
	public void setData(XRDData data) {
		this.data = data;
	}
	@Override
	public String toString() {
		return "PhoneResult [version=" + version + ", code=" + code + ", msg=" + msg + ", data=" + data + "]";
	}

}
