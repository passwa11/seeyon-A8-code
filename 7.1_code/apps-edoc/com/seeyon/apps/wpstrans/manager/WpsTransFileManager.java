package com.seeyon.apps.wpstrans.manager;

import java.io.File;
import java.util.Map;

import com.seeyon.ctp.common.exceptions.BusinessException;

/**
 * 内网文件传输服务实现
 * @author 唐桂林
 *
 */
public interface WpsTransFileManager {
	
	/**
	 * 上传文件
	 * @param paramMap
	 * @param sourceFile
	 * @return
	 * @throws BusinessException
	 */
	public String upload(Map<String, String> paramMap, File sourceFile) throws BusinessException;
	
	/**
	 * 下载文件
	 * @param targetPath
	 * @return
	 * @throws BusinessException
	 */
	public String download(String targetPath, String newFilePath) throws BusinessException;
	
	/**
	 * 实现握手
	 * @return
	 * @throws BusinessException
	 */
	public String handshake() throws BusinessException;
	
	/**
	 * 是否握手成功
	 * @param result
	 * @return
	 * @throws BusinessException
	 */
	public boolean isHandshakeSuccess(String result) throws BusinessException;

}
