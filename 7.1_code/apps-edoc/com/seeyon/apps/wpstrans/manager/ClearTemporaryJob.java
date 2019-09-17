package com.seeyon.apps.wpstrans.manager;

import java.io.File;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.quartz.QuartzJob;
import com.seeyon.ctp.util.Strings;

/**
 * 清空临时文件定时任务
 * 
 * @author 廖迎
 *
 */
public class ClearTemporaryJob implements QuartzJob {
	private static final Log log = LogFactory.getLog(ClearTemporaryJob.class);
	public static final String CLEARTEMPORARY_JOBNAME = "ClearTemporary_JobName_";
	public static final String CLEARTEMPORARY_GROUPNAME = "ClearTemporary_GroupName";
	public static final String CLEARTEMPORARY_JOBBEANID = "clearTemporaryJob";

	@Override
	public void execute(Map<String, String> arg0) {
		String temporyDir = arg0.get("temporyDir");
		if (Strings.isBlank(temporyDir)) {
			log.info("没有指定临时目录路径，无法清空临时目录。");
			return;
		}
		File file = new File(temporyDir);
		if (file.exists() && file.isDirectory()) {
			deleteFile(file);
			log.info("清空临时目录成功！");
		}
	}

	private void deleteFile(File file) {
		if (file.isDirectory()) {
			for (File subFile : file.listFiles()) {
				deleteFile(subFile);
			}
		} else {
			file.delete();
		}
	}
}
