package com.seeyon.ctp.rest.util;

import com.seeyon.ctp.common.log.CtpLogFactory;
import org.apache.commons.logging.Log;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class FileType {
	private static final Log logger = CtpLogFactory.getLog(FileType.class);
	public final static Map<String, String> FILE_TYPE_MAP = new HashMap<String, String>();

	private FileType() {
	}
 
	static {
		getAllFileType(); // 初始化文件类型信息
	}

	/**
	 * Created on 2010-7-1
	 * <p>
	 * Discription:[getAllFileType,常见文件头信息]
	 * </p>
	 * 
	 * @author:[shixing_11@sina.com]
	 */
	private static void getAllFileType() {
		FILE_TYPE_MAP.put("pdf", "255044462D312E"); // Adobe Acrobat (pdf)
		FILE_TYPE_MAP.put("jpg", "FFD8FF"); 
		FILE_TYPE_MAP.put("png", "89504E47"); 
		FILE_TYPE_MAP.put("gif", "47494638"); 
		FILE_TYPE_MAP.put("tif", "49492A00"); 
		FILE_TYPE_MAP.put("bmp", "424D"); 
	}

	/*public static void main(String[] args) throws Exception {
		File f = new File("C:\\Users\\h1567\\Desktop\\00000000633e931b01633e9bb07e0001.pdf");
		if (f.exists()) {
			String filetype1 = getImageFileType(f);
			String filetype2 = getFileByFile(f);
		}
	}*/

    /**
     * 获取图片文件实际类型,若不是图片则返回null
     * @param f
     * @return
     */
	public final static String getImageFileType(File f) {
		if (isImage(f)) {
			try {
				ImageInputStream iis = ImageIO.createImageInputStream(f);
				Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
				if (!iter.hasNext()) {
					return null;
				}
				ImageReader reader = iter.next();
				iis.close();
				return reader.getFormatName();
			} catch (IOException e) {
				return null;
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Created on 2010-7-1
	 * <p>
	 * Discription:[getFileByFile,获取文件类型,包括图片,若格式不是已配置的,则返回null]
	 * </p>
	 * 
	 * @param file
	 * @return fileType
	 * @author:[shixing_11@sina.com]
	 */
	public final static String getFileByFile(File file) {
		String filetype = null;
		byte[] b = new byte[50];
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			is.read(b);
			filetype = getFileTypeByStream(b);

		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				if(is != null){
					is.close();
				}
			} catch (IOException e) {
				logger.error(e);
			}
		}
		return filetype;
	}

	/**
	 * Created on 2010-7-1
	 * <p>
	 * Discription:[getFileTypeByStream]
	 * </p>
	 * 
	 * @param b
	 * @return fileType
	 * @author:[shixing_11@sina.com]
	 */
	public final static String getFileTypeByStream(byte[] b) {
		String filetypeHex = String.valueOf(getFileHexString(b));
		Iterator<Entry<String, String>> entryiterator = FILE_TYPE_MAP.entrySet().iterator();
		while (entryiterator.hasNext()) {
			Entry<String, String> entry = entryiterator.next();
			String fileTypeHexValue = entry.getValue();
			if (filetypeHex.toUpperCase().startsWith(fileTypeHexValue)) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * Created on 2010-7-2
	 * <p>
	 * Discription:[isImage,判断文件是否为图片]
	 * </p>
	 * 
	 * @param file
	 * @return true 是 | false 否
	 * @author:[shixing_11@sina.com]
	 */
	public static final boolean isImage(File file) {
		boolean flag = false;
		try {
			BufferedImage bufreader = ImageIO.read(file);
			int width = bufreader.getWidth();
			int height = bufreader.getHeight();
			if (width == 0 || height == 0) {
				flag = false;
			} else {
				flag = true;
			}
		} catch (IOException e) {
			flag = false;
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	/**
	 * Created on 2010-7-1
	 * <p>
	 * Discription:[getFileHexString]
	 * </p>
	 * 
	 * @param b
	 * @return fileTypeHex
	 * @author:[shixing_11@sina.com]
	 */
	public final static String getFileHexString(byte[] b) {
		StringBuilder stringBuilder = new StringBuilder();
		if (b == null || b.length <= 0) {
			return null;
		}
		for (int i = 0; i < b.length; i++) {
			int v = b[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}
}