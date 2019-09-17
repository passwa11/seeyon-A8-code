package com.seeyon.apps.wpstrans.util;

/**
 * Wps后台转版全局变量定义
 * @author 唐桂林
 *
 */
public class WpsTransConstant {
	public static Boolean WPSTRANS_ENABLE = false;

	/** Wps转版状态 */
	//Wps转版开始
	public static final Integer WPSTRANS_STATUS_START = 0;
	//Wps转版成功
	public static final Integer WPSTRANS_STATUS_SUCCESS = 1;
	//Wps转版失败
	public static final Integer WPSTRANS_STATUS_FAILURE = 2;
	//Wps转版记录删除
	public static final Integer WPSTRANS_STATUS_DELETED = 3;

	/** Wps转版文件服务地址 */
	public static String WPSTRANS_FOLDER_PATH = "";

	/** Wps转版文件服务地址 */
	public static String WPSTRANS_FILE_SERVICE_URL = "";

	/** Wps转版文件服务上传方法 */
	public static String WPSTRANS_FILE_SERVICE_UPLOAD = "";

	/** Wps转版文件服务下载方法 */
	public static String WPSTRANS_FILE_SERVICE_DOWNLOAD = "";

	/** Wps转版文件服务握手方法 */
	public static String WPSTRANS_FILE_SERVICE_HANDSHAKE = "";

	/** Wps转版服务IP */
	public static String WPSTRANS_SERVICE_IP = "";

	/** Wps转版服务端口 */
	public static String WPSTRANS_SERVICE_PORT = "";

	/** Wps转版服务路径 */
	public static String WPSTRANS_SERVICE_PATH = "";
	/**
	 * 清空临时文件夹的时间表达式
	 */
	public static String WPSTRANS_SERVICE_CRON="";

}
