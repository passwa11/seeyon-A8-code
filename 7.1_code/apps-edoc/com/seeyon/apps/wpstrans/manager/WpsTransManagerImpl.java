package com.seeyon.apps.wpstrans.manager;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.seeyon.apps.govdoc.helper.GovdocContentHelper;
import com.seeyon.apps.wpstrans.listener.WpsTransEvent;
import com.seeyon.apps.wpstrans.po.WpsTransRecord;
import com.seeyon.apps.wpstrans.util.WpsTransConstant;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.filemanager.manager.Util;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.annotation.ListenEvent;
import com.seeyon.v3x.edoc.domain.EdocSummary;

import kingsoft.WPS_ERROR_NO;
import kingsoft.WpsConvertTool;

/**
 * Wps后台转版实现
 * 
 * @author 唐桂林
 *
 */
public class WpsTransManagerImpl implements WpsTransManager {

	private static final Log LOGGER = LogFactory.getLog(WpsTransManagerImpl.class);

	private MainbodyManager ctpMainbodyManager;
	private AffairManager affairManager;
	private FileManager fileManager;
	private WpsTransFileManager wpsTransFileManager;
	private WpsTransRecordManager wpsTransRecordManager;
	private ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 10, 30, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>());

	/**
	 * 异步转换
	 * 
	 * @param event
	 */
	@Override
	public void transToOfdAsyn(final WpsTransEvent event) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				EventDispatcher.fireEvent(event);
			}
		});
	}

	@ListenEvent(event = WpsTransEvent.class)
	public boolean transToOfd(WpsTransEvent event) {
		Long summaryId = null;
		Long affairId = null;
		Long memberId = null;
		Long wpsFileId = null;
		Integer app = ApplicationCategoryEnum.edoc.key();
		int contentType = -1;

		try {
			/** 1 获取并验证转版对象 */
			if (event == null) {
				return false;
			}

			EdocSummary summary = event.getSummary();
			if (summary == null) {
				return false;
			}

			// CtpAffair affair = colManager.getAffairById(affairId);
			CtpAffair affair = event.getAffair();
			if (affair == null) {
				return false;
			}

			summaryId = summary.getId();
			affairId = affair.getId();
			memberId = affair.getMemberId();

			CtpAffair senderAffair = affairManager.getSenderAffair(summaryId);
			if (senderAffair == null) {
				return false;
			}

			/** 2 获取G6表单公文解密过后的wps文件 */
			if (senderAffair.getApp().intValue() == ApplicationCategoryEnum.edoc.key()) {
				List<CtpContentAll> contentList = ctpMainbodyManager
						.getContentListByModuleIdAndModuleType(ModuleType.edoc, summaryId);
				if (Strings.isNotEmpty(contentList)) {
					for (CtpContentAll bean : contentList) {
						contentType = bean.getContentType();
						if (contentType == MainbodyType.HTML.getKey() || contentType == MainbodyType.TXT.getKey()) {
							break;
						}
						if (contentType == MainbodyType.WpsWord.getKey()
								|| contentType == MainbodyType.WpsExcel.getKey()) {
							try {
								wpsFileId = Long.parseLong(bean.getContent());
							} catch (NumberFormatException nfe) {
								LOGGER.info("Wps正文fileId获取失败！");
								break;
							}
							break;
						}
					}
				}
			}

			if (wpsFileId == null) {
				LOGGER.info("该公文正文找不到！");
				return false;
			}
			if (contentType == MainbodyType.HTML.getKey() || contentType == MainbodyType.TXT.getKey()) {
				LOGGER.info("该正文类型不支持转版！");
				return false;
			}
			if (contentType == MainbodyType.Ofd.getKey()) {
				LOGGER.info("该公文已转过版！");
				return false;
			}

			/** 3 保存Wps转版记录(默认状态为失败) */
			WpsTransRecord record = wpsTransRecordManager.saveEdocTransRecord(summaryId, summary.getSubject(),
					wpsFileId, app, affairId, memberId);

			/** 4 将wps文件改名并保存到临时文件夹 */
			File file = fileManager.getFile(wpsFileId);
			// 保存到临时目录base/temporary/wpstrans
			File wpsTransFolder = new File(WpsTransConstant.WPSTRANS_FOLDER_PATH);
			if (!wpsTransFolder.exists()) {
				wpsTransFolder.mkdirs();
			}
			String newFilePath = wpsTransFolder.getAbsolutePath() + "/" + wpsFileId + ".wps";
			Util.jinge2StandardOffice(file.getAbsolutePath(), newFilePath);
//			FileUtils.copyFile(file, newFile);
			File newFile = new File(newFilePath);

			/** 5 将OA服务器的wps文件传送至转版服务器(通过文件传输服务-自己写的) */
			Map<String, String> paramMap = new HashMap<String, String>();
			paramMap.put("filepath", WpsTransConstant.WPSTRANS_SERVICE_PATH);
			paramMap.put("filename", newFile.getName());
			String result = wpsTransFileManager.upload(paramMap, newFile);
			if (!wpsTransFileManager.isHandshakeSuccess(result)) {
				wpsTransRecordManager.saveFairlureInfo(summaryId, "无法连接Wps文件上传服务");
				return false;
			}

			/** 6 下指令给转版服务器，进行转版 */
			String ofdFileName = newFile.getName().substring(0, newFile.getName().lastIndexOf(".")) + ".ofd";
			String serviceSourcePath = WpsTransConstant.WPSTRANS_SERVICE_PATH + "/" + newFile.getName();
			String serviceTargetPath = WpsTransConstant.WPSTRANS_SERVICE_PATH + "/" + ofdFileName;
			new WpsTransThread(serviceSourcePath, serviceTargetPath, record).run();
		} catch (Exception e) {
			LOGGER.error("Wps转版出错", e);
			return false;
		}
		return true;
	}

	@Override
	public void downloadAndSaveFile(String serviceTargetPath, WpsTransRecord record) {
		File wpsTransFolder = new File(WpsTransConstant.WPSTRANS_FOLDER_PATH);
		Long newFileId = UUIDLong.longUUID();
		String newFilePath = wpsTransFolder.getAbsolutePath()
				+ serviceTargetPath.substring(serviceTargetPath.lastIndexOf("/"));

		try {
			/** 7 将转版服务器上成功转版后的文件传送到OA服务器相应位置 */
			LOGGER.info("下载ofd临时文件路径filePath=" + newFilePath);
			String result = wpsTransFileManager.download(serviceTargetPath, newFilePath);
			if (!wpsTransFileManager.isHandshakeSuccess(result)) {
				wpsTransRecordManager.saveFairlureInfo(record.getObjectId(), "无法下载Wps转版后文件");
				return;
			}

			// 8 成功转版后保存到正文数据表
			File ofdFile = new File(newFilePath);
			if (ofdFile.exists()) {
				String filename = record.getSubject() + ".ofd";
				V3XFile v3xFile = fileManager.save(ofdFile, ApplicationCategoryEnum.edoc, filename, new Date(), false);
				if (v3xFile != null) {
					v3xFile.setMimeType("application/ofd");
					fileManager.save(v3xFile);

					CtpContentAll contentAll = GovdocContentHelper.getBodyContentByModuleIdAndType(record.getObjectId(),MainbodyType.Ofd);

					if (null == contentAll) {
						contentAll = new CtpContentAll();
						contentAll.setIdIfNew();
					}
					contentAll.setContentDataId(v3xFile.getId());
					contentAll.setContent(String.valueOf(v3xFile.getId()));
					contentAll.setCreateDate(new Date());
					contentAll.setCreateId(record.getMemberId());
					contentAll.setContentType(MainbodyType.Ofd.getKey());
					contentAll.setModuleType(record.getApp()); // 公文类型
					contentAll.setModuleId(record.getObjectId());
					contentAll.setSort(3);
					contentAll.setContentTemplateId(0L);
					ctpMainbodyManager.saveOrUpdateContentAll(contentAll);
				}
			}

			// 9 Wps转版成功记录
			wpsTransRecordManager.saveSuccessInfo(record.getObjectId(), "Wps转版成功", newFileId);
		} catch (Exception e) {
			LOGGER.error("filePath=" + newFilePath);
			LOGGER.error("CoderFactory.getInstance() Exception:", e);
		}
	}

	@Override
	public boolean isWpsTransServiceEnable() {
		// 1 判断Wps文件服务是否可用TODO

		// 2判断Wps转版服务是否可用
		TTransport transport = null;
		try {
			transport = new TSocket(WpsTransConstant.WPSTRANS_SERVICE_IP,
					Integer.parseInt(WpsTransConstant.WPSTRANS_SERVICE_PORT));
			transport.open();

			TProtocol protocol = new TBinaryProtocol(transport);
			WpsConvertTool.Client client = new WpsConvertTool.Client(protocol);

			// 获取服务器状态
			if (client.getServerState() != WPS_ERROR_NO.WPS_ERROR_OK) {
				LOGGER.info("Wps转版服务不可用!");
				return false;
			}
		} catch (Exception e) {
			LOGGER.info("获取Wps转版服务出错", e);
			return false;
		} finally {
			if(transport != null){				
				try {
					transport.close();
				} catch (Exception e) {
					LOGGER.error("Wps转版流关闭失败，请注意！！！");
				}
			}
		}
		return true;
	}


	public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
		this.ctpMainbodyManager = ctpMainbodyManager;
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setWpsTransFileManager(WpsTransFileManager wpsTransFileManager) {
		this.wpsTransFileManager = wpsTransFileManager;
	}

	public void setWpsTransRecordManager(WpsTransRecordManager wpsTransRecordManager) {
		this.wpsTransRecordManager = wpsTransRecordManager;
	}

}
