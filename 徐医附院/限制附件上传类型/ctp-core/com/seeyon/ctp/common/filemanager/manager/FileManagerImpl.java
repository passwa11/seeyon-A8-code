/**
 *
 */
package com.seeyon.ctp.common.filemanager.manager;

import com.aspose.imaging.internal.hC.iF;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.IConfigPublicKey;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.encrypt.CoderFactory;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.Constants;
import com.seeyon.ctp.common.filemanager.dao.V3XFileDAO;
import com.seeyon.ctp.common.filemanager.domain.ReplaceBase64Result;
import com.seeyon.ctp.common.filemanager.event.FileItem;
import com.seeyon.ctp.common.filemanager.event.FileUploadEvent;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.datasource.annotation.DataSourceName;
import com.seeyon.ctp.datasource.annotation.ProcessInDataSource;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.util.Base64;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FileUtil;
import com.seeyon.ctp.util.ImageUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.io.File.separator;

/**
 *
 * @author <a href="mailto:tanmf@seeyon.com">Tanmf</a>
 * @version 1.0 2006-11-15
 */
@ProcessInDataSource(name = DataSourceName.BASE)
public class FileManagerImpl implements FileManager {
	private static Log log = CtpLogFactory.getLog(FileManagerImpl.class);

	public static final String FORM_FIELD_DEFAULT_FILE1 = "file1";

	private PartitionManager partitionManager;

	private V3XFileDAO v3xFileDAO;

	private int maxWidth;
	private int maxHeight;
	private String fileSuffix;
	private ConfigManager configManager;

	public void setConfigManager(ConfigManager configManager) {
		this.configManager = configManager;
	}

	public void setFileSuffix(String fileSuffix) {
		this.fileSuffix = fileSuffix;
	}

	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}

	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}

	public void setPartitionManager(PartitionManager partitionManager) {
		this.partitionManager = partitionManager;
	}

	public void setV3xFileDAO(V3XFileDAO v3xFileDAO) {
		this.v3xFileDAO = v3xFileDAO;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.seeyon.ctp.common.filemanager.manager.FileManager#getNowFolder(boolean)
	 */
	public String getNowFolder(boolean createWhenNoExist) throws BusinessException {
		return this.getFolder(new Date(), createWhenNoExist);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.seeyon.ctp.common.filemanager.manager.FileManage#getFolder(java.util.
	 * Date)
	 */
	public String getFolder(Date createDate, boolean createWhenNoExist) throws BusinessException {
		return partitionManager.getFolder(createDate, createWhenNoExist);
	}

	/**
	 * 得到当前的文件
	 *
	 * @return
	 * @throws BusinessException
	 */
	protected String getDefaultFolder() throws BusinessException {
		return this.getFolder(new Date(), true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.seeyon.ctp.common.filemanager.manager.FileManager#getV3XFile(java.lang.
	 * Long)
	 */
	public V3XFile getV3XFile(Long fileId) throws BusinessException {
		return v3xFileDAO.get(fileId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.seeyon.ctp.common.filemanager.manager.FileManager#getV3XFile(java.lang.
	 * Long[])
	 */
	public List<V3XFile> getV3XFile(Long[] fileIds) throws BusinessException {
		return v3xFileDAO.get(fileIds);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.seeyon.ctp.common.filemanager.manager.FileManager#getFile(java.lang.Long)
	 */
	public File getFile(Long fileId) throws BusinessException {
		return getSpicFile(fileId, true);
	}

	@Override
	public File getSpicFile(Long fileId, boolean decryption) throws BusinessException {
		V3XFile v3xFile = this.getV3XFile(fileId);
		if (v3xFile == null) {
			throw new IllegalArgumentException("文件Id=" + fileId + "不存在");
		}

		File file = this.getNewFile(v3xFile.getCreateDate(), fileId);

		if (!file.exists()) {
			return null;
		}
		if (decryption) {
			return decryptionFile(file);
		} else {
			return file;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.seeyon.ctp.common.filemanager.manager.FileManager#getFile(java.lang.Long,
	 * java.util.Date)
	 */
	public File getFile(Long fileId, Date createDate) throws BusinessException {
		File file = this.getNewFile(createDate, fileId);
		if (!file.exists()) {
			// 多时区可能引起的问题进行防护,传入的createDate可能不是正确的
			V3XFile v3xFile = getV3XFile(fileId);
			if (v3xFile == null) {
				return null;
			}
			Date realCreateDate = v3xFile.getCreateDate();
			file = this.getNewFile(realCreateDate, fileId);
			if (!file.exists()) {
				return null;
			}
		}

		return decryptionFile(file);
//        return  file;
	}

	public File getFileForUC(Long fileId, Date createDate) throws BusinessException {
		String ucFilePath = partitionManager.getFolderForUC(createDate, true) + separator + String.valueOf(fileId);
		File file = new File(ucFilePath);
		if (!file.exists()) {
			// 多时区可能引起的问题进行防护,传入的createDate可能不是正确的
			V3XFile v3xFile = getV3XFile(fileId);
			if (v3xFile == null) {
				return null;
			}
			Date realCreateDate = v3xFile.getCreateDate();
			ucFilePath = partitionManager.getFolderForUC(realCreateDate, true) + separator + String.valueOf(fileId);
			file = new File(ucFilePath);
			if (!file.exists()) {
				return null;
			}
		}

		return decryptionFile(file);
	}

	@Override
	public File getFileDecryption(Long fileId) throws BusinessException {
		return decryptionFile(getFile(fileId));
	}

	@Override
	public File getFileDecryption(Long fileId, Date createDate) throws BusinessException {
		return decryptionFile(getFile(fileId, createDate));
	}

	public File getThumFile(Long fileId, Date createDate) throws BusinessException {
		return getThumFile(fileId, createDate, maxWidth);
	}

	private File getThumFileCommon(String resFile, int px) {
		String smallFile = resFile + "_" + px + fileSuffix;
		File file = new File(smallFile);
		if (!file.exists()) {
			try {
				String entryTempFile = CoderFactory.getInstance().decryptFileToTemp(resFile);
				String entryThumFile = entryTempFile + "_" + px + fileSuffix;
				ImageUtil.resize(entryTempFile, entryThumFile, px, px);
				// 再加密
				if (!entryTempFile.equals(resFile)) {
					CoderFactory.getInstance().encryptFile(entryThumFile, smallFile);
				}
				return new File(smallFile);
			} catch (IllegalArgumentException e) {
				log.error("文件不存在:" + e.getLocalizedMessage());
			} catch (Exception e) {
				log.error("生成缩略图:" + e.getLocalizedMessage(), e);
			}
		}
		return file;
	}

	public File getThumFile(Long fileId, Date createDate, int px) throws BusinessException {
		String resFile = this.getNewFilepath(createDate, fileId);
		// 多时区问题防护，防止由于时区问题造成的createDate传递有误
		if (!(new File(resFile).exists())) {
			V3XFile v3xFile = getV3XFile(fileId);
			if (v3xFile == null) {
				// OA-128122，null防护处理
				return null;
			}
			Date realCreateDate = v3xFile.getCreateDate();
			resFile = this.getNewFilepath(realCreateDate, fileId);
		}
		return getThumFileCommon(resFile, px);
//        String smallFile = resFile + "_" + px + fileSuffix;
//        File file = new File(smallFile);
//        if (!file.exists()) {
//            try {
//                String entryTempFile = CoderFactory.getInstance().decryptFileToTemp(resFile);
//                String entryThumFile = entryTempFile + "_" + px + fileSuffix;
//                ImageUtil.resize(entryTempFile, entryThumFile, px, px);
//                //再加密
//                if (!entryTempFile.equals(resFile)) {
//                    CoderFactory.getInstance().encryptFile(entryThumFile, smallFile);
//                }
//                return new File(smallFile);
//            } catch (Exception e) {
//                log.error("生成缩略图:" + e.fillInStackTrace());
//            }
//        }
//        return file;
	}

	@Override
	public File getThumFileForUC(Long fileId, Date createDate, String pxStr) throws BusinessException {
		String resFile = this.partitionManager.getFolderForUC(createDate, true) + separator + String.valueOf(fileId);
		// 多时区问题防护，防止由于时区问题造成的createDate传递有误
		if (!(new File(resFile).exists())) {
			V3XFile v3xFile = getV3XFile(fileId);
			Date realCreateDate = v3xFile.getCreateDate();
			resFile = this.partitionManager.getFolderForUC(realCreateDate, true) + separator + String.valueOf(fileId);
		}
		int px = maxWidth;
		if (StringUtils.isNoneBlank(pxStr)) {
			try {
				px = Integer.valueOf(pxStr);
			} catch (Exception e) {
				log.error("传入的缩略图参数不符合规范", e);
			}
		}
		return getThumFileCommon(resFile, px);
	}

	public File decryptionFile(File file) {
		try {
			return CoderFactory.getInstance().decryptFileToTemp(file);
		} catch (Exception e) {
			log.error("文件=" + file.getName() + " 解密错误！", e);
			throw new IllegalArgumentException("文件=" + file.getName() + "  解密错误！");
		}
	}

	public Map<String, V3XFile> uploadFiles(HttpServletRequest request, String allowExtensions, Long maxSize)
			throws BusinessException {
		return this.uploadFiles(request, allowExtensions, null, null, maxSize);
	}

	public Map<String, V3XFile> uploadFiles(HttpServletRequest request, String allowExtensions, String destDirectory,
			Long maxSize) throws BusinessException {
		return this.uploadFiles(request, allowExtensions, null, destDirectory, maxSize);
	}

	public Map<String, V3XFile> uploadFiles(HttpServletRequest request, String allowExtensions, File destFile,
			Long maxSize) throws BusinessException {
		Map<String, File> destFiles = new HashMap<String, File>();
		destFiles.put(FORM_FIELD_DEFAULT_FILE1, destFile);

		return this.uploadFiles(request, allowExtensions, destFiles, maxSize);
	}

	public Map<String, V3XFile> uploadFiles(HttpServletRequest request, String allowExtensions,
			Map<String, File> destFiles, Long maxSize) throws BusinessException {
		return this.uploadFiles(request, allowExtensions, destFiles, null, maxSize);
	}

	private Map<String, V3XFile> uploadFiles(HttpServletRequest request, String allowExtensions,
			Map<String, File> destFiles, String destDirectory, Long maxSize) throws BusinessException {
		String dir = destDirectory;
		String allowExt = allowExtensions;
		User user = AppContext.getCurrentUser();
		if (user == null) {
			return null;
		}

		if (!(request instanceof MultipartHttpServletRequest)) {
			throw new java.lang.IllegalArgumentException(
					"Argument request must be an instantce of MultipartHttpServletRequest. [" + request.getClass()
							+ "]");
		}

		Date createDate = new Date();

		/*
		 * if (destFiles != null) { // 存为指定的文件
		 *
		 * } else
		 */
		if (StringUtils.isNotBlank(dir)) { // 存到指定的文件夹
			dir = FilenameUtils.separatorsToSystem(dir);
		} else { // 系统默认分区
			String ucFlag = request.getParameter("ucFlag");
			if ("yes".equals(ucFlag)) {
				// 致信端特殊处理
				dir = partitionManager.getFolderForUC(createDate, true);
			} else {
				dir = this.getFolder(createDate, true);
			}
		}

		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

		// 这是来上传的异常，大小是由系统总配置，50M
		Object maxUploadSizeExceeded = multipartRequest.getAttribute("MaxUploadSizeExceeded");
		if (maxUploadSizeExceeded != null) {
			if (maxSize == null || maxSize.longValue() == 0l) {
				throw new BusinessException("fileupload.exception.MaxSize", maxUploadSizeExceeded);
			} else {
				throw new BusinessException("fileupload.exception.MaxSize", Strings.formatFileSize(maxSize, false));
			}
		}

		Object ex = multipartRequest.getAttribute("unknownException");
		if (ex != null) {
			throw new BusinessException("fileupload.exception.unknown", ex);
		}

		Map<String, V3XFile> v3xFiles = new HashMap<String, V3XFile>();

		Iterator<String> fileNames = multipartRequest.getFileNames();
		if (fileNames == null) {
			return null;
		}

		String isEncrypt = request.getParameter("isEncrypt");

		while (fileNames.hasNext()) {
			Object name = fileNames.next();

			if (name == null || "".equals(name)) {
				continue;
			}

			String fieldName = String.valueOf(name);

			// MultipartFile fileItem = multipartRequest.getFile(String.valueOf(name));
			// 在H5中一个input标签可以上传多个文件,支艳强，2016/9/2
			List<MultipartFile> fileItemList = multipartRequest.getFiles(String.valueOf(name));
			for (int fileIndex = 0; fileIndex < fileItemList.size(); fileIndex++) {
				MultipartFile fileItem = fileItemList.get(fileIndex);
				if (fileItem == null) {
					continue;
				}

				// 在具体应用中又指定的大小
				if (maxSize != null && fileItem.getSize() > maxSize.longValue()) {
					throw new BusinessException("fileupload.exception.MaxSize", Strings.formatFileSize(maxSize, false));
				}

				// 原始名称
				String filename = fileItem.getOriginalFilename().replace((char) 160, (char) 32).replace((char) 63,
						(char) 32);

				/**
				 * 扩展名验证
				 */
				String suffix = FilenameUtils.getExtension(filename).toLowerCase();
				ConfigItem configItem = configManager.getConfigItem("system_switch", "prohibit_att_suffix");
				if (null != configItem && null != configItem.getConfigValue()) {
					String value = configItem.getConfigValue();
					String[] prohibit_suffix = value.split(",");
					if (java.util.Arrays.asList(prohibit_suffix).contains(suffix)) {
						throw new BusinessException("fileupload.exception.UnallowedExtension.settings", value);
					}
				}
				if (!StringUtils.isEmpty(allowExt) && !StringUtils.isEmpty(suffix)) {
					allowExt = allowExt.replace(',', '|');

					if (!Pattern.matches(allowExt.toLowerCase(), suffix)) {
						throw new BusinessException("fileupload.exception.UnallowedExtension", allowExt);
					}
				}
				if (Strings.isNotBlank(allowExt) && Strings.isBlank(suffix)) {
					throw new BusinessException("fileupload.exception.UnallowedExtension", allowExt);
				}
				// 事件准备
				FileItem fi = new FileItemImpl(fileItem);
				FileUploadEvent event = new FileUploadEvent(this, fi);
				try {
					EventDispatcher.fireEventWithException(event);
				} catch (Throwable e) {
					if (e instanceof BusinessException) {
						throw (BusinessException) e;
					} else {
						throw new BusinessException(e.getLocalizedMessage(), e);
					}
				}
				if (fi.getMessages().size() > 0) {
					request.setAttribute("upload.event.message", fi.getMessages());
				}
				long fileId = UUIDLong.longUUID();
				File destFile = null;
				BufferedOutputStream bos = null;
				// 保存硬盘
				try {
					if (destFiles != null && destFiles.get(fieldName) != null) {
						destFile = destFiles.get(fieldName);

						destFile.getParentFile().mkdirs();
					} else {
						destFile = new File(dir + separator + String.valueOf(fileId));
					}

					String encryptVersion = null;
					// TODO: 配置组件完成后，释放下行注释
					encryptVersion = CoderFactory.getInstance().getEncryptVersion();
					if (encryptVersion != null && !IConfigPublicKey.NO.equals(encryptVersion)
							&& !"false".equals(isEncrypt)) { // 统一由系统开关配置控制，除非模块设置成false不要求加密
						bos = new BufferedOutputStream(new FileOutputStream(destFile));
						CoderFactory.getInstance().upload(fi.getInputStream(), bos, encryptVersion);
					} else {
						//zhou:update
						// zhou start
						System.out.println("ceshi");
						String zvalue="";
						InputStream zinputStream =null;
						try {
							zinputStream= fileItem.getInputStream();
							byte[] b = new byte[20];
							zinputStream.read(b, 0, b.length);
							zvalue = bytesToHexString(b);
						} catch (Exception e) {

						}finally {
							if(null != zinputStream) {
								zinputStream.close();
							}
						}

						String result = "";
						for (Entry<String, String> entry : mFileTypes.entrySet()) {
							if (zvalue.startsWith(entry.getKey())) {
								result = entry.getValue();
							}
						}
//						System.out.println("result=========:" + result);
						if(!"".equals(result)){
							fi.saveAs(destFile);
							V3XFile file = new V3XFile(fileId);
							file.setCreateDate(createDate);
							file.setFilename(filename);
							file.setSize(fi.getSize());
							file.setMimeType(fi.getContentType());
//			                 file.setMimeType("text/plain");
							file.setType(Constants.ATTACHMENT_TYPE.FILE.ordinal());
							file.setCreateMember(user.getId());
							file.setAccountId(user.getAccountId());

							// v3xFiles.put(fieldName, file);
							// 由于一个input可以上传多个文件，需要对fieldName进行特殊处理
							// eg:对于file1的input如果有2个文件，那么对应的key为file1_1,file1_2;如果是一个那么对应的key就是file1_1
							String newKeyName = fieldName + "_" + String.valueOf(fileIndex + 1);
							v3xFiles.put(newKeyName, file);
						}
						// zhou end
//						fi.saveAs(destFile);
					}
				} catch (Exception e) {
					log.error(e.getLocalizedMessage(), e);
					throw new BusinessException("附件存盘时发生错误", e);
				} finally {
					if (bos != null) {
						try {
							bos.close();
						} catch (IOException e) {
							log.warn(e.getLocalizedMessage(), e);
						}
					}
				}
				//zhou :注释
//				V3XFile file = new V3XFile(fileId);
//				file.setCreateDate(createDate);
//				file.setFilename(filename);
//				file.setSize(fi.getSize());
//				file.setMimeType(fi.getContentType());
////                 file.setMimeType("text/plain");
//				file.setType(Constants.ATTACHMENT_TYPE.FILE.ordinal());
//				file.setCreateMember(user.getId());
//				file.setAccountId(user.getAccountId());
//
//				// v3xFiles.put(fieldName, file);
//				// 由于一个input可以上传多个文件，需要对fieldName进行特殊处理
//				// eg:对于file1的input如果有2个文件，那么对应的key为file1_1,file1_2;如果是一个那么对应的key就是file1_1
//				String newKeyName = fieldName + "_" + String.valueOf(fileIndex + 1);
//				v3xFiles.put(newKeyName, file);
			}

		}

		return v3xFiles;
	}

	/**
	 * 将要读取文件头信息的文件的byte数组转换成string类型表示
	 *
	 * @param src 要读取文件头信息的文件的byte数组
	 * @return 文件头十六进制信息
	 */
	private String bytesToHexString(byte[] src) {
		StringBuilder builder = new StringBuilder();
		if (src == null || src.length <= 0) {
			return null;
		}
		String hv;
		for (int i = 0; i < src.length; i++) {
			// 以十六进制（基数 16）无符号整数形式返回一个整数参数的字符串表示形式，并转换为大写
			hv = Integer.toHexString(src[i] & 0xFF).toUpperCase();
			if (hv.length() < 2) {
				builder.append(0);
			}
			builder.append(hv);
		}
		return builder.toString();
	}

	// 缓存文件头信息-文件头信息
	public static final HashMap<String, String> mFileTypes = new HashMap<String, String>();

	static {
		mFileTypes.put("4D5343", "xsn");//.xsn
		mFileTypes.put("504B0304", "zip");// .sfp,.biz,.docx,.xlsx,.pptx,.syz,
		mFileTypes.put("D0CF11E0", "doc");//.wps,.et,.xls,.ppt
		mFileTypes.put("FFD8FF", "jpg");
		mFileTypes.put("89504E47", "png");
		mFileTypes.put("255044462D312E", "pdf");
		mFileTypes.put("424D", "bmp");
	}

	@Override
	public ReplaceBase64Result replaceBase64Image(String html) throws BusinessException {
		ReplaceBase64Result result = new ReplaceBase64Result();
		result.setConvertBase64Img(false);
		if (Strings.isBlank(html)) {
			result.setHtml(html);
			return result;
		}

		String regexImg = "<\\s*(i|I)(m|M)(g|G)[^>]+(s|S)(r|R)(c|C)\\s*=\\s*(\"|')data:image/[^>]+>";// 匹配img标签正则表达式
		Pattern pattern = Pattern.compile(regexImg);
		Matcher matcher = pattern.matcher(html);
		while (matcher.find()) { // 存在包含base64图片的标签
			String imgHtml = matcher.group();// 获取到img标签元素
			String imgHtmlNew = null;// base64字符串替换为路径后的新元素

			String regexBase64 = "(\"data:image/[^'\"]+\")|('data:image/[^'\"]+')";// 匹配src属性，包含开头与结尾的双引号或单引号
			Pattern base64Pattern = Pattern.compile(regexBase64);
			Matcher base64matcher = base64Pattern.matcher(imgHtml);
			while (base64matcher.find()) {// 存在base64位字符串
				String base64Str = base64matcher.group();// 获取src的属性，包含开头与结尾的双引号或单引号
				base64Str = base64Str.substring(1, base64Str.length() - 1);// 去除两边引号

				V3XFile v3XFile = saveBase64Img(base64Str, null, null);// 将base64信息存储到磁盘上
				StringBuilder urlStr = new StringBuilder();// 拼接图片url地址
				urlStr.append(SystemEnvironment.getContextPath());
				urlStr.append("/fileUpload.do?method=showRTE&fileId=");
				urlStr.append(v3XFile.getId().toString());
				urlStr.append("&createDate=");
				urlStr.append(Datetimes.format(v3XFile.getCreateDate(), Datetimes.dateStyle));
				urlStr.append("&type=image");

				imgHtmlNew = imgHtml.replaceFirst(regexBase64, "\"" + urlStr.toString() + "\"");
			}

			html = html.replaceFirst(regexImg, imgHtmlNew);
			result.setConvertBase64Img(true);
		}

		result.setHtml(html);
		return result;
	}

	@Override
	public V3XFile saveBase64Img(String base64Str, String fileName, Map<String, Object> param)
			throws BusinessException {
		User user = AppContext.getCurrentUser();
		Date createDate = new Date();

		String base64Body = null;
		if (base64Str.startsWith("data:image/")) {// base64编码开头带有data:image/*;base64 （*代表图片格式后缀）
			String[] base64 = base64Str.split(",");
			String base64Prefix = base64[0];
			base64Body = base64[1];

			if (Strings.isBlank(fileName)) {// 文件名为空时，根据base64前缀生成文件名
				String imgSuffix = base64Prefix.replace("data:image/", "").replace(";base64", "");
				if ("jpeg".equalsIgnoreCase(imgSuffix)) {
					imgSuffix = "jpg";
				}
				fileName = Datetimes.format(createDate, Datetimes.datetimeStyleNoSeparator) + "." + imgSuffix;// 组装文件名
			}
		} else {
			base64Body = base64Str;
		}

		// base64字符串转为输入流
		InputStream inputStream = null;
		try {
			byte[] b = Base64.decode(base64Body);// Base64解码
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {// 调整异常数据
					b[i] += 256;
				}
			}
			inputStream = new ByteArrayInputStream(b);
		} catch (UnsupportedEncodingException e) {
			log.error(e.getLocalizedMessage(), e);
			throw new BusinessException(e);
		}

		// 扩展名验证
		String suffix = FilenameUtils.getExtension(fileName).toLowerCase();
		ConfigItem configItem = configManager.getConfigItem("system_switch", "prohibit_att_suffix");
		if (null != configItem && null != configItem.getConfigValue()) {
			String value = configItem.getConfigValue();
			String[] prohibit_suffix = value.split(",");
			if (java.util.Arrays.asList(prohibit_suffix).contains(suffix)) {
				throw new BusinessException("fileupload.exception.UnallowedExtension.settings", value);
			}
		}

		long fileId = UUIDLong.longUUID();
		String dir = this.getFolder(createDate, true);
		File destFile = new File(dir + separator + String.valueOf(fileId));// 输出的目标文件
		BufferedOutputStream bos = null;
		try {// 保存硬盘
			String encryptVersion = CoderFactory.getInstance().getEncryptVersion();
			if (Strings.isNotBlank(encryptVersion) && !IConfigPublicKey.NO.equals(encryptVersion)) {
				bos = new BufferedOutputStream(new FileOutputStream(destFile));
				CoderFactory.getInstance().upload(inputStream, bos, encryptVersion);
			} else {
				IOUtils.copy(inputStream, new FileOutputStream(destFile));
			}
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
			throw new BusinessException(e);
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					log.warn(e.getLocalizedMessage(), e);
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					log.warn(e.getLocalizedMessage(), e);
				}
			}
		}

		V3XFile file = new V3XFile(fileId);
		file.setCreateDate(createDate);
		file.setFilename(fileName);
		file.setType(Constants.ATTACHMENT_TYPE.IMAGE.ordinal());
		file.setCreateMember(user.getId());
		file.setAccountId(user.getAccountId());

		return file;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.seeyon.ctp.common.filemanager.manager.FileManager#deleteFile(java.lang.
	 * Long, java.lang.Boolean)
	 */
	public void deleteFile(Long fileId, Boolean deletePhysicsFile) throws BusinessException {
		if (deletePhysicsFile) {
			File file = this.getSpicFile(fileId, false);
			if (file != null) {
				file.delete();
			}
		}
		this.v3xFileDAO.delete(fileId);
	}

	@Override
	public void deletePhysicsFile(Long fileId) throws BusinessException {
		File file = this.getSpicFile(fileId, false);
		if (file != null) {
			file.delete();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.seeyon.ctp.common.filemanager.manager.FileManager#deleteFile(java.lang.
	 * Long, java.util.Date, java.lang.Boolean)
	 */
	public void deleteFile(Long fileId, Date createDate, Boolean deletePhysicsFile) throws BusinessException {
		this.v3xFileDAO.delete(fileId);
		if (deletePhysicsFile) {
			try {
				File file = this.getFile(fileId, createDate);
				if (file != null) {
					file.delete();
				}
			} catch (Exception e) {
				// ignore exception
			}
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.seeyon.ctp.common.filemanager.manager.FileManager#getFileInputStream(java
	 * .lang.Long)
	 */
	public InputStream getFileInputStream(Long fileId) throws BusinessException {
		File file = this.getFile(fileId);
		if (file == null) {
			return null;
		}

		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			log.warn(file.getAbsolutePath() + "不存在");
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.seeyon.ctp.common.filemanager.manager.FileManager#getFileInputStream(java
	 * .lang.Long, java.util.Date)
	 */
	public InputStream getFileInputStream(Long fileId, Date createDate) throws BusinessException {
		File file = this.getFile(fileId, createDate);
		if (file == null) {
			return null;
		}

		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			log.debug(file.getAbsolutePath() + "不存在");
			return null;
		}
	}

	@Override
	public InputStream getFileInputStreamForUC(Long fileId, Date createDate) throws BusinessException {
		File file = this.getFileForUC(fileId, createDate);
		if (file == null) {
			return null;
		}

		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			log.debug(file.getAbsolutePath() + "不存在");
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.seeyon.ctp.common.filemanager.manager.FileManager#save(com.seeyon.ctp.
	 * common.filemanager.V3XFile)
	 */
	public void save(V3XFile file) {
		file.setIdIfNew();
		this.v3xFileDAO.save(file);
	}

	public void save(List<V3XFile> files) {
		this.v3xFileDAO.save(files);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.seeyon.ctp.common.filemanager.manager.FileManager#getFileBytes(java.lang.
	 * Long)
	 */
	public byte[] getFileBytes(Long fileId) throws BusinessException {
		File file = this.getFile(fileId);

		try {
			if (file != null) {
				return FileUtils.readFileToByteArray(file);
			}
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}

		return null;
	}

	public byte[] getFileBytes(Long fileId, Date createDate) throws BusinessException {
		File file = this.getFile(fileId, createDate);

		try {
			if (file != null) {
				return FileUtils.readFileToByteArray(file);
			}
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}

		return null;
	}

	public V3XFile save(String bodyData, ApplicationCategoryEnum category, String filename, Date createDate,
			Boolean isSaveToDB) throws BusinessException {
		if (bodyData == null) {
			throw new IllegalArgumentException("bodyData is null");
		}

		if (createDate == null) {
			createDate = new Date();
		}

		byte[] bs = bodyData.getBytes();

		V3XFile v3xFile = new V3XFile();

		v3xFile.setIdIfNew();
		v3xFile.setCategory(category.key());
		v3xFile.setCreateDate(createDate);
		v3xFile.setFilename(filename);
		v3xFile.setMimeType(FileUtil.getMimeType(filename));
		v3xFile.setSize(Long.valueOf(bs.length));

		try {
			File file = this.getNewFile(v3xFile.getCreateDate(), v3xFile.getId());

			FileUtils.writeByteArrayToFile(file, bs);

			if (isSaveToDB) {
				this.save(v3xFile);
			}
		} catch (Exception e) {
			throw new BusinessException(e);
		}

		return v3xFile;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.seeyon.ctp.common.filemanager.manager.FileManager#clone(java.lang.Long,
	 * boolean)
	 */
	public V3XFile clone(Long originalFileId, boolean saveDB) throws BusinessException, FileNotFoundException {
		V3XFile v3xFile = this.getV3XFile(originalFileId);
		if (v3xFile == null) {
			throw new java.io.FileNotFoundException("clone附件 : [" + originalFileId + "]不存在。");
		}

		Long newFileId = UUIDLong.longUUID();
		Date newCreateDate = new Date();

		this.clone(originalFileId, v3xFile.getCreateDate(), newFileId, newCreateDate);

		V3XFile file = new V3XFile();
		file.setId(newFileId);
		file.setCreateDate(newCreateDate);
		file.setCategory(v3xFile.getCategory());
		file.setDescription(v3xFile.getDescription());
		file.setCreateMember(v3xFile.getCreateMember());
		file.setFilename(v3xFile.getFilename());
		file.setMimeType(v3xFile.getMimeType());
		file.setSize(v3xFile.getSize());
		file.setType(v3xFile.getType());
		file.setAccountId(v3xFile.getAccountId());

		if (saveDB) {
			this.save(file);
		}

		return file;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.seeyon.ctp.common.filemanager.manager.FileManager#clone(java.lang.Long,
	 * java.util.Date)
	 */
	public void clone(Long originalFileId, Date createDate, Long newFileId, Date newCreateDate)
			throws BusinessException, FileNotFoundException {
		File srcFile = this.getFile(originalFileId, createDate);
		if (srcFile == null) {
			throw new java.io.FileNotFoundException("Clone附件 : [" + originalFileId + ", " + createDate + "]不存在。");
		}

		File destFile = this.getNewFile(newCreateDate, newFileId);

		try {
			FileUtils.copyFile(srcFile, destFile);
		} catch (IOException e) {
			log.error("Clone文件异常 " + originalFileId, e);
			throw new BusinessException("Clone文件异常" + e.getMessage());
		}
	}

	public List<V3XFile> create(ApplicationCategoryEnum category, HttpServletRequest request) throws BusinessException {
		String[] fileUrl = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_fileUrl);
		String[] mimeType = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_mimeType);
		String[] size = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_size);
		String[] createdate = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_createDate);
		String[] filename = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_filename);
		String[] type = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_type);
		String[] needClone = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_needClone);
		String[] description = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_description);

		if (fileUrl == null || mimeType == null || size == null || createdate == null || filename == null
				|| type == null || needClone == null) {
			return null;
		}

		Long userId = AppContext.getCurrentUser().getId();
		Long accountId = AppContext.getCurrentUser().getAccountId();

		List<V3XFile> files = new ArrayList<V3XFile>();

		for (int i = 0; i < fileUrl.length; i++) {
			Date originalCreateDate = Datetimes.parseDatetime(createdate[i]);
			V3XFile file = new V3XFile();

			file.setCategory(category.key());
			file.setType(Integer.valueOf(type[i]));
			file.setFilename(filename[i]);
			file.setMimeType(mimeType[i]);
			file.setSize(Long.parseLong(size[i]));
			file.setDescription(description[i]);
			file.setCreateMember(userId);
			file.setAccountId(accountId);

			boolean _needClone = Boolean.parseBoolean(needClone[i]);
			if (_needClone) {
				Long newFileId = UUIDLong.longUUID();
				Date newCreateDate = new Date();
				try {
					this.clone(Long.valueOf(fileUrl[i]), originalCreateDate, newFileId, newCreateDate);
				} catch (Exception e) {
					log.error("Clone 附件", e);
					throw new BusinessException("Clone文件异常" + e.getMessage());
				}

				file.setId(newFileId);
				file.setCreateDate(newCreateDate);
			} else {
				file.setId(Long.parseLong(fileUrl[i]));
				file.setCreateDate(originalCreateDate);
			}

			this.save(file);

			files.add(file);
		}

		return files;
	}

	public V3XFile save(File file, ApplicationCategoryEnum category, String filename, Date createDate,
			Boolean isSaveToDB) throws BusinessException {
		if (file == null || !file.exists()) {
			throw new BusinessException("FileNotFoundException");
		}

		InputStream in = null;
		try {
			in = new FileInputStream(file);
			V3XFile v3xFile = this.save(in, category, filename, createDate, isSaveToDB);

			return v3xFile;
		} catch (Exception e) {
			throw new BusinessException(e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public V3XFile save(InputStream in, ApplicationCategoryEnum category, String filename, Date createDate,
			Boolean isSaveToDB) throws BusinessException {
		Date cdate = createDate;
		if (in == null) {
			throw new java.lang.IllegalArgumentException("in is null");
		}

		User user = AppContext.getCurrentUser();
//        OA-96406初始化安装：流程表单制作切换到系统预置表单，有列表没表单模板
		/*
		 * if (user == null) { return null; }
		 */

		if (cdate == null) {
			cdate = new Date();
		}

		V3XFile v3xFile = new V3XFile();

		v3xFile.setIdIfNew();
		v3xFile.setCategory(category.key());
		v3xFile.setCreateDate(cdate);
		v3xFile.setFilename(filename);
		v3xFile.setMimeType(FileUtil.getMimeType(filename));
		v3xFile.setType(Constants.ATTACHMENT_TYPE.FILE.ordinal());
		v3xFile.setDescription("");
		if (user != null) {
			v3xFile.setCreateMember(user.getId());
			v3xFile.setAccountId(user.getAccountId());
		} else {
			log.debug("上传文件时当前用户为空。");
		}
//        v3xFile.setCreateMember(user.getId());

		File destFile = this.getNewFile(cdate, v3xFile.getId());
		OutputStream out = null;
		try {
			out = new FileOutputStream(destFile);

			int count = org.apache.commons.io.IOUtils.copy(in, out);
			v3xFile.setSize(Long.valueOf(count));
			if (isSaveToDB) {
				this.save(v3xFile);
			}
		} catch (Exception e) {
			throw new BusinessException(e);
		} finally {
			IOUtils.closeQuietly(out);
		}

		return v3xFile;
	}

	/**
	 * 根据日期、文件id，产生新的文件绝对路径
	 *
	 * @param createDate
	 * @param newFileId
	 * @return
	 * @throws BusinessException
	 */
	private String getNewFilepath(Date createDate, Long newFileId) throws BusinessException {
		return this.getFolder(createDate, true) + separator + String.valueOf(newFileId);
	}

	/**
	 * 根据日期、文件id，产生新的文件绝对路径
	 *
	 * @param createDate
	 * @param newFileId
	 * @return
	 * @throws BusinessException
	 */
	private File getNewFile(Date createDate, Long newFileId) throws BusinessException {
		return new File(this.getNewFilepath(createDate, newFileId));
	}

	public File getStandardOffice(Long fileId, Date createDate) throws BusinessException {
		File f = this.getFile(fileId, createDate);
		if (f == null) {
			f = this.getFile(fileId);
			if (f == null)
				return null;
		}
		V3XFile file = this.getV3XFile(fileId);

		String newPathName2 = SystemEnvironment.getSystemTempFolder() + separator + f.getName() + "O"
				+ f.lastModified();

		if (file != null) {
			String miniType = file.getMimeType();
			boolean miniTypeFlag = false;
			if ((miniType != null) && (miniType.toLowerCase().contains("word"))) {
				miniTypeFlag = true;
			}
			if ((file.getFilename() != null) && ((file.getFilename().toLowerCase().endsWith(".doc"))
					|| (file.getFilename().toLowerCase().endsWith(".docx")) || (miniTypeFlag))) {
				newPathName2 = newPathName2 + ".doc";
			}
		}

		File newFile = new File(newPathName2);
		if (newFile.exists()) {
			return newFile;
		}

		Util.jinge2StandardOffice(f.getAbsolutePath(), newPathName2);
		return newFile;
	}

	public InputStream getStandardOfficeInputStream(Long fileId, Date createDate)
			throws BusinessException, FileNotFoundException {
		File f = this.getStandardOffice(fileId, createDate);
		if (f == null) {
			return null;
		}

		try {
			return new FileInputStream(f);
		} catch (FileNotFoundException e) {
			log.warn(f.getAbsolutePath() + "不存在");
			throw e;
		}
	}

	public V3XFile clone(Long originalFileId) throws BusinessException, FileNotFoundException {
		V3XFile v3xFile = this.getV3XFile(originalFileId);
		if (v3xFile == null) {
			throw new java.io.FileNotFoundException("V3XFile : " + originalFileId + "不存在。");
		}
		Long newFileId = UUIDLong.longUUID();
		Date newCreateDate = new Date();
		this.clone(originalFileId, v3xFile.getCreateDate(), newFileId, newCreateDate);
		V3XFile file = new V3XFile();
		file.setId(newFileId);
		file.setCreateDate(newCreateDate);
		file.setCategory(v3xFile.getCategory());
		file.setDescription(v3xFile.getDescription());
		file.setCreateMember(v3xFile.getCreateMember());
		file.setFilename("copy" + v3xFile.getFilename());
		file.setMimeType(v3xFile.getMimeType());
		file.setSize(v3xFile.getSize());
		file.setType(v3xFile.getType());
		file.setAccountId(v3xFile.getAccountId());

		this.save(file);
		return file;
	}

	public List<V3XFile> findByFileName(String fileName) {
		return v3xFileDAO.findByFileName(fileName);
	}

	public void update(V3XFile file) {
		v3xFileDAO.update(file);
	}

	/**
	 * 在进行编辑上传类型的文件时，替换之前保存一份历史，区别于正常的替换
	 */
	public Long copyFileBeforeModify(Long fileId) {

		try {
			return this.clone(fileId, true).getId();
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
			return -1L;
		} catch (BusinessException e) {
			log.error(e.getMessage(), e);
			return -1L;
		}
	}

	/**
	 * 通过文档的sourceId获得file
	 */
	public Long getFileIdByDocResSourceId(Long fileId) {

		try {
			return this.getV3XFile(fileId).getId();
		} catch (BusinessException e) {
			log.error(e.getMessage(), e);
			return -1L;
		}
	}

	/**
	 * 备份wps的上传类型
	 */
	public void copyWPS(Long sourceId) {
		// 命名规则：原文件名_时刻（到秒）.bak
		// 存放路径：Office正文原始文件路径下
		// 比如原始office正文存放在e:\\ufseeyon\group\base\\upload\2010\01\20下，则备份文件也放到这个路径下，主要方便运维同事查找，存在的问题是不能做增量备份
		try {
			V3XFile file = this.getV3XFile(sourceId);
			String filePath = this.getFolder(file.getCreateDate(), true) + File.separator + sourceId;
			String now = Datetimes.format(new Date(), "yyyyMMddHHmmss", TimeZone.getDefault());
			String contentFileBak = filePath + "_" + now + ".bak";
			File f = new File(filePath);
			if (f.exists()) {
				FileUtils.copyFile(f, new File(contentFileBak));
			}
		} catch (Exception e) {
			log.error("WPS正文内容备份异常 ：" + sourceId, e);
		}

	}

	public File getStandardOffice(String fileAbsolutePath) throws BusinessException {
		if (StringUtils.isBlank(fileAbsolutePath)) {
			throw new BusinessException("传入文件路径为空");
		}
		String newPathName = SystemEnvironment.getSystemTempFolder() + separator + String.valueOf(UUIDLong.longUUID());

		Util.jinge2StandardOffice(fileAbsolutePath, newPathName);

		return new File(newPathName);
	}

	public static void main(String[] args) throws Exception {
		String p = "D:\\Working@seeyon\\Runtime\\ApacheJetspeed\\webapps\\seeyon\\main\\login\\default\\images\\banner2";

		for (int i = 1; i < 11; i++) {
			int s = i * 100;
			ImageUtil.resize(p + ".png", "d:\\aaa_" + s + ".png", s, s);
		}
	}

	@Override
	public Map<String, V3XFile> uploadFiles(HttpServletRequest request, Long memberId, Long accountId,
			String allowExtensions, Long maxSize) throws BusinessException {
		return this.uploadFiles(request, memberId, accountId, allowExtensions, null, null, maxSize);
	}

	private Map<String, V3XFile> uploadFiles(HttpServletRequest request, Long memberId, Long accountId,
			String allowExtensions, Map<String, File> destFiles, String destDirectory, Long maxSize)
			throws BusinessException {
		String dir = destDirectory;
		String allowExt = allowExtensions;

		if (!(request instanceof MultipartHttpServletRequest)) {
			throw new java.lang.IllegalArgumentException(
					"Argument request must be an instantce of MultipartHttpServletRequest. [" + request.getClass()
							+ "]");
		}

		Date createDate = new Date();

		/*
		 * if (destFiles != null) { // 存为指定的文件
		 *
		 * } else
		 */
		String ucFlag = request.getParameter("ucFlag");
		if (StringUtils.isNotBlank(dir)) { // 存到指定的文件夹
			dir = FilenameUtils.separatorsToSystem(dir);
		} else { // 系统默认分区
			if ("yes".equals(ucFlag)) {
				// 致信端特殊处理
				dir = partitionManager.getFolderForUC(createDate, true);
			} else {
				dir = this.getFolder(createDate, true);
			}
		}

		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

		if (!"yes".equals(ucFlag)) {// 非致信走系统的大小限制；致信的已经在致信端处理，此处不再判断文件大小
			// 这是来上传的异常，大小是由系统总配置，50M
			Object maxUploadSizeExceeded = multipartRequest.getAttribute("MaxUploadSizeExceeded");
			if (maxUploadSizeExceeded != null) {
				if (maxSize == null || maxSize.longValue() == 0l) {
					throw new BusinessException("fileupload.exception.MaxSize", maxUploadSizeExceeded);
				} else {
					throw new BusinessException("fileupload.exception.MaxSize", Strings.formatFileSize(maxSize, false));
				}
			}
		}

		Object ex = multipartRequest.getAttribute("unknownException");
		if (ex != null) {
			throw new BusinessException("fileupload.exception.unknown", ex);
		}

		Map<String, V3XFile> v3xFiles = new HashMap<String, V3XFile>();

		Iterator<String> fileNames = multipartRequest.getFileNames();
		if (fileNames == null) {
			return null;
		}

		String isEncrypt = request.getParameter("isEncrypt");

		while (fileNames.hasNext()) {
			Object name = fileNames.next();

			if (name == null || "".equals(name)) {
				continue;
			}

			String fieldName = String.valueOf(name);

			// MultipartFile fileItem = multipartRequest.getFile(String.valueOf(name));
			// 在H5中一个input标签可以上传多个文件,支艳强，2016/9/2
			List<MultipartFile> fileItemList = multipartRequest.getFiles(String.valueOf(name));
			for (int fileIndex = 0; fileIndex < fileItemList.size(); fileIndex++) {
				MultipartFile fileItem = fileItemList.get(fileIndex);
				if (fileItem == null) {
					continue;
				}

				// 在具体应用中又指定的大小
				if (maxSize != null && fileItem.getSize() > maxSize.longValue()) {
					throw new BusinessException("fileupload.exception.MaxSize", Strings.formatFileSize(maxSize, false));
				}

				// 原始名称
				String filename = fileItem.getOriginalFilename().replace((char) 160, (char) 32).replace((char) 63,
						(char) 32);

				/**
				 * 扩展名验证
				 */
				String suffix = FilenameUtils.getExtension(filename).toLowerCase();

				if (!StringUtils.isEmpty(allowExt) && !StringUtils.isEmpty(suffix)) {
					allowExt = allowExt.replace(',', '|');

					if (!Pattern.matches(allowExt.toLowerCase(), suffix)) {
						throw new BusinessException("fileupload.exception.UnallowedExtension", allowExt);
					}
				}

				// 事件准备
				FileItem fi = new FileItemImpl(fileItem);
				FileUploadEvent event = new FileUploadEvent(this, fi);
				try {
					EventDispatcher.fireEventWithException(event);
				} catch (Throwable e) {
					if (e instanceof BusinessException) {
						throw (BusinessException) e;
					} else {
						throw new BusinessException(e.getLocalizedMessage(), e);
					}
				}
				if (fi.getMessages().size() > 0) {
					request.setAttribute("upload.event.message", fi.getMessages());
				}
				long fileId = UUIDLong.longUUID();
				File destFile = null;
				BufferedOutputStream bos = null;
				// 保存硬盘
				try {
					if (destFiles != null && destFiles.get(fieldName) != null) {
						destFile = destFiles.get(fieldName);

						destFile.getParentFile().mkdirs();
					} else {
						destFile = new File(dir + separator + String.valueOf(fileId));
					}

					String encryptVersion = null;
					// TODO: 配置组件完成后，释放下行注释
					encryptVersion = CoderFactory.getInstance().getEncryptVersion();
					if (encryptVersion != null && !IConfigPublicKey.NO.equals(encryptVersion)
							&& !"false".equals(isEncrypt)) { // 统一由系统开关配置控制，除非模块设置成false不要求加密
						bos = new BufferedOutputStream(new FileOutputStream(destFile));
						CoderFactory.getInstance().upload(fi.getInputStream(), bos, encryptVersion);
					} else
						fi.saveAs(destFile);
				} catch (Exception e) {
					throw new BusinessException("附件存盘时发生错误", e);
				} finally {
					if (bos != null) {
						try {
							bos.close();
						} catch (IOException e) {
							log.warn(e.getLocalizedMessage(), e);
						}
					}
				}

				V3XFile file = new V3XFile(fileId);
				file.setCreateDate(createDate);
				file.setFilename(filename);
				file.setSize(fi.getSize());
				file.setMimeType(fi.getContentType());
				// file.setMimeType("text/plain");
				file.setType(Constants.ATTACHMENT_TYPE.FILE.ordinal());
				file.setCreateMember(memberId);
				file.setAccountId(accountId);

				// v3xFiles.put(fieldName, file);
				// 由于一个input可以上传多个文件，需要对fieldName进行特殊处理
				// eg:对于file1的input如果有2个文件，那么对应的key为file1_1,file1_2;如果是一个那么对应的key就是file1_1
				String newKeyName = fieldName + "_" + String.valueOf(fileIndex + 1);
				v3xFiles.put(newKeyName, file);
			}

		}

		return v3xFiles;
	}

	public String getOfficeSuffix(V3XFile file) throws BusinessException {

		String extension = "";

		if (file != null) {
			String mimeType = file.getMimeType();
			if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(mimeType))
				extension = "docx";
			else if ("application/msword".equals(mimeType))
				extension = "doc";
			else if ("application/vnd.ms-excel".equals(mimeType))
				extension = "xls";
			else if ("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(mimeType))
				extension = "xlsx";
		}
		return extension;
	}
}
