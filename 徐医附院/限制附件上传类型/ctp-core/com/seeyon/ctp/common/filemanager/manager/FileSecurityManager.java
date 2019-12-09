package com.seeyon.ctp.common.filemanager.manager;

import java.util.Collection;

/**
 * 附件权限管理，管理附件的授权。
 * @author wangwenyou
 *
 */
public interface FileSecurityManager {
	/**
	 * 判断访问该文件是否需要登录。
	 * @param fileId CtpFile的Id。
	 * @return 不需要登录返回<code>true</code>，否则返回<code>false</code>。
	 */
	boolean isNeedlessLogin(Long fileId);
	/**
	 * 增加无需登录的文件白名单。
	 * @param fileId CtpFile的Id。
	 */
	void addNeedlessLogin(Long fileId);
	/**
	 * 增加无需登录的文件白名单。
	 * @param fileIds CtpFile的Id集合。
	 */	
	void addNeedlessLogin(Collection<Long> fileIds);
	/**
	 * 增加无需登录的文件白名单
	 * 创建人:zhiyanqiang	
	 * 功能描述:   
	 * 创建时间：2016年4月22日 下午4:10:31    
	 * @param fileIdStr 字符串格式的文件Id
	 * void
	 */
	void addNeedlessLogin(String fileIdStr);
}
