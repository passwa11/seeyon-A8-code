package com.seeyon.apps.restFileUpload.entity;

/**
 * @author kkdo
 * @date 2019年4月29日 上午10:51:30
 * <pre>文件属性</pre>
 */

public class FileEntity {

	/**
	 * 文件名,不带后缀
	 * 比如   test.pdf 那么 此字段为test  suffix为pdf
	 */
	private String fileName;

	/**
	 * 文件后缀名
	 */
	private String suffix;

	/**
	 * 文件的base64
	 */
	private String base64Str;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getBase64Str() {
		return base64Str;
	}

	public void setBase64Str(String base64Str) {
		this.base64Str = base64Str;
	}

}
