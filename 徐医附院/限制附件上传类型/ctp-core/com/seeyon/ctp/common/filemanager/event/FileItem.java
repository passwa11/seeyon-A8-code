package com.seeyon.ctp.common.filemanager.event;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public interface FileItem {
	/**
	 * 返回multipart form中的参数名称.
	 * 
	 * @return 参数名称(never {@code null} or empty)
	 */
	String getName();

	/**
	 * 返回客户端原始文件的文件名.
	 * <p>
	 * This may contain path information depending on the browser used, but it
	 * typically will not with any other than Opera.
	 * 
	 * @return the original filename, or the empty String if no file has been
	 *         chosen in the multipart form, or {@code null} if not defined or
	 *         not available
	 */
	String getOriginalFilename();

	/**
	 * 返回文件的content type。
	 * 
	 * @return the content type, or {@code null} if not defined (or no file has
	 *         been chosen in the multipart form)
	 */
	String getContentType();

	/**
	 * 返回文件的大小（字节）.
	 * 
	 * @return the size of the file, or 0 if empty
	 */
	long getSize();

	/**
	 * Return an InputStream to read the contents of the file from. The user is
	 * responsible for closing the stream.
	 * 
	 * @return the contents of the file as stream, or an empty stream if empty
	 * @throws IOException
	 *             in case of access errors (if the temporary store fails)
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * 处理过程中，要在上传完毕后提示前端的信息，常用于查到病毒但杀毒成功的信息。
	 * 
	 * @param message
	 *            提示信息。
	 */
	void appendMessage(String message);
	/**
	 * 改变上传的文件，用于使用清除病毒以后的流替换原上传的文件流。
	 * @param stream 新的文件流。
	 * @throws IOException
	 */
	void setInputStream(InputStream stream) throws IOException;

	/**
	 * 获取所有的前台提示信息。
	 * @return 提示信息集合。
	 */
	Collection getMessages();

	/**
	 * Transfer the received file to the given destination file.
	 * <p>
	 * This may either move the file in the filesystem, copy the file in the
	 * filesystem, or save memory-held contents to the destination file. If the
	 * destination file already exists, it will be deleted first.
	 * <p>
	 * If the file has been moved in the filesystem, this operation cannot be
	 * invoked again. Therefore, call this method just once to be able to work
	 * with any storage mechanism.
	 * 
	 * @param dest
	 *            the destination file
	 * @throws IOException
	 *             in case of reading or writing errors
	 * @throws IllegalStateException
	 *             if the file has already been moved in the filesystem and is
	 *             not available anymore for another transfer
	 */
	void saveAs(File file) throws IOException, IllegalStateException;
}
