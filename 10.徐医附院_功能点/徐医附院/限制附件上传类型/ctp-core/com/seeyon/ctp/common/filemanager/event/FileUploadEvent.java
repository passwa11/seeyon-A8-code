package com.seeyon.ctp.common.filemanager.event;

import com.seeyon.ctp.event.Event;

/**
 * 文件上传事件，在文件上传到服务器存储在文件系统中并保存到数据库之前触发。<BR/>
 * 可用于文件病毒扫描扩展开发。
 * 
 * @author wangwenyou
 * 
 */
public class FileUploadEvent extends Event {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3447261251110219089L;
	private FileItem fileItem;

	public FileUploadEvent(Object source, FileItem file) {
		super(source);
		this.fileItem = file;
	}
	
	public FileItem getFileItem() {
		return fileItem;
	}

}
