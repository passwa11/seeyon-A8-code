package com.seeyon.apps.ext.copyFile.controller;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.apps.ext.copyFile.manager.KDocBackupManager;
import com.seeyon.apps.ext.copyFile.util.UtilString;
import com.seeyon.ctp.common.controller.BaseController;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

public class BackupFileController extends BaseController {
	private static final Log log = LogFactory.getLog(BackupFileController.class);

	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
		log.info("进入文件下载方法");
		System.out.println("进入文件下载方法");
		long docLibID = Long.parseLong(request.getParameter("docLibId"));
		String ids = request.getParameter("ids");
		String[] newIds = ids.split("\\|");
		for (String id : newIds) {
			String[] folderIDs = UtilString.tokenize(id, "|");
			KDocBackupManager temp = new KDocBackupManager(docLibID, folderIDs);
			temp.download();
		}
		log.info("下载方法结束");
		return null;
	}
}