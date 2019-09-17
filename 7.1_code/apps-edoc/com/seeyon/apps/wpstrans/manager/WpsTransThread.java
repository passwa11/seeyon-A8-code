package com.seeyon.apps.wpstrans.manager;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.seeyon.apps.wpstrans.po.WpsTransRecord;
import com.seeyon.apps.wpstrans.util.WpsTransConstant;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.quartz.MutiQuartzJobNameException;
import com.seeyon.ctp.common.quartz.NoSuchQuartzJobBeanException;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.util.Strings;

import kingsoft.APP_MODE_TYPE;
import kingsoft.WPS_ERROR_NO;
import kingsoft.WpsConvertTool;

/**
 * Wps后台转版实现
 * 
 * @author 唐桂林
 *
 */
public class WpsTransThread extends Thread {

	private static final Log LOGGER = LogFactory.getLog(WpsTransThread.class);

	private WpsTransRecordManager wpsTransRecordManager;
	private WpsTransManager wpsTransManager;

	private String serviceSourcePath;
	private String serviceTargetPath;
	private WpsTransRecord record;
	private APP_MODE_TYPE mode_type;

	static {
		if (!Strings.isBlank(WpsTransConstant.WPSTRANS_SERVICE_CRON)) {
			// 定时清理临时文件
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("temporyDir", WpsTransConstant.WPSTRANS_FOLDER_PATH);
			try {
				QuartzHolder.newCronQuartzJob(ClearTemporaryJob.CLEARTEMPORARY_GROUPNAME, ClearTemporaryJob.CLEARTEMPORARY_JOBNAME, WpsTransConstant.WPSTRANS_SERVICE_CRON, null,
						null, ClearTemporaryJob.CLEARTEMPORARY_JOBBEANID, parameters);
			} catch (MutiQuartzJobNameException e) {
				// TODO Auto-generated catch block
				LOGGER.error(e);
			} catch (NoSuchQuartzJobBeanException e) {
				// TODO Auto-generated catch block
				LOGGER.error(e);
			}
		}
	}

	public WpsTransThread(String serviceSourcePath, String serviceTargetPath, WpsTransRecord record) {
		this.serviceSourcePath = serviceSourcePath;
		this.serviceTargetPath = serviceTargetPath;
		this.record = record;
		this.mode_type = getAppModeType(serviceSourcePath.split("[.]")[1]);
	}

	@Override
	public void run() {

		TTransport transport = null;
		try {
			transport = new TSocket(WpsTransConstant.WPSTRANS_SERVICE_IP, Integer.parseInt(WpsTransConstant.WPSTRANS_SERVICE_PORT));
			transport.open();

			TProtocol protocol = new TBinaryProtocol(transport);
			WpsConvertTool.Client client = new WpsConvertTool.Client(protocol);

			// 获取服务器状态
			if (client.getServerState() != WPS_ERROR_NO.WPS_ERROR_OK) {
				LOGGER.info("Wps转版服务不可用!");
			}

			LOGGER.info("Wps转版开始。。。。");
			wpsTransRecordManager = (WpsTransRecordManager) AppContext.getBean("wpsTransRecordManager");
			wpsTransManager = (WpsTransManager) AppContext.getBean("wpsTransManager");
			// 调用方法
			WPS_ERROR_NO result = client.saveAs(serviceSourcePath, serviceTargetPath, mode_type);
			if (result != WPS_ERROR_NO.WPS_ERROR_OK) {
				LOGGER.info("Wps转版失败：" + result);
				wpsTransRecordManager.saveFairlureInfo(record.getObjectId(), "Wps转版失败");
			} else {
				wpsTransManager.downloadAndSaveFile(serviceTargetPath, record);
			}

		} catch (TException e) {
			LOGGER.error("(TException)Wps转版出错", e);
		} catch (Exception e) {
			LOGGER.error("(Exception)Wps转版出错", e);
		} finally {
			if(transport != null){				
				try {
					transport.close();
				} catch (Exception e) {
					LOGGER.error("Wps转版流关闭失败，请注意！！！");
				}
			}
		}
	}

	/**
	 *
	 * @param filetype
	 * @return
	 */
	private APP_MODE_TYPE getAppModeType(String filetype) {
		if ("et".equals(filetype)) {
			return APP_MODE_TYPE.APP_MODE_TYPE_ET;
		}
		return APP_MODE_TYPE.APP_MODE_TYPE_WPS;
	}

	public void setWpsTransRecordManager(WpsTransRecordManager wpsTransRecordManager) {
		this.wpsTransRecordManager = wpsTransRecordManager;
	}

	public void setWpsTransManager(WpsTransManager wpsTransManager) {
		this.wpsTransManager = wpsTransManager;
	}
}