package com.seeyon.ctp.common.filemanager.event;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.event.Event;

/**
 * 文件下载事件，在文件下载设置输出流之前触发。<BR/>
 * 适用场景：压缩或加密要下载的文件。
 * 重新设置response的Content-Type，通过setInputStream改变下载文件的内容。
 * 
 * @author wangwenyou
 * 
 */
public class FileDownloadEvent extends Event {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4386004921008295096L;
	private final FileManager fileManager;
	private final long fileId;
	private final V3XFile file;
	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private InputStream stream;
	public FileDownloadEvent(Object source, V3XFile file,long fileId,
			HttpServletRequest request, HttpServletResponse response, FileManager fileManager) {
		super(source);
		this.file = file;
		this.request = request;
		this.response = response;
		this.fileId = fileId;
		this.fileManager = fileManager;
	}

	public V3XFile getFile() {
		return file;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public InputStream getInputStream() {
		return stream;
	}

	public void setInputStream(InputStream stream) {
		this.stream = stream;
	}

	public FileManager getFileManager() {
		return fileManager;
	}

	public long getFileId() {
		return fileId;
	}

}
