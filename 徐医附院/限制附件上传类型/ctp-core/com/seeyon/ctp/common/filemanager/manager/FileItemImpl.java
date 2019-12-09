package com.seeyon.ctp.common.filemanager.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.springframework.web.multipart.MultipartFile;

import com.seeyon.ctp.common.filemanager.event.FileItem;
import com.seeyon.ctp.common.log.CtpLogFactory;

class FileItemImpl implements FileItem {
	private static final Log log = CtpLogFactory.getLog(FileItemImpl.class);
	private final MultipartFile fileItem;
	private InputStream in = null;
	private List<String> messages = new ArrayList<String>();

	public FileItemImpl(MultipartFile fileItem) {
		this.fileItem = fileItem;
	}

	@Override
	public String getName() {
		return fileItem.getName();
	}

	@Override
	public String getOriginalFilename() {
		return fileItem.getOriginalFilename();
	}

	@Override
	public String getContentType() {
		return fileItem.getContentType();
	}

	@Override
	public long getSize() {
		return fileItem.getSize();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return fileItem.getInputStream();
	}

	@Override
	public void appendMessage(String message) {
		this.messages.add(message);
	}

	@Override
	public void setInputStream(InputStream stream) throws IOException {
		this.in = stream;
	}

	@Override
	public Collection<String> getMessages() {
		return this.messages;
	}

	@Override
	public void saveAs(File file) throws IOException, IllegalStateException {
		if (in == null) {
			
			fileItem.transferTo(file);
		} else {
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			try {
				IOUtils.copy(in, fileOutputStream);
			} catch (Exception e) {
				log.error(e.getLocalizedMessage(), e);
			} finally {
				fileOutputStream.close();
			}
		}
	}

	

}
