package com.seeyon.ctp.common.filemanager.manager;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.exceptions.BusinessException;

/**
 * 文件迁移工具。标记正式环境需要迁移到测试环境的文件，保存列表，为S1文件迁移工具服务。
 * 业务应用需在启动初始化时按下面的方式调用
 * <pre>
 * for(...){
 * 		FileMigrateUtil.addFile(fileId,createDate);
 * }
 * FileMigrateUtil.updateFileList();
 * </pre>
 * 
 * @author wangwenyou
 *
 */
public class FileMigrateUtil {
	private static Set<String> fileSet = new HashSet<String>();

	/**
	 * 增加文件到迁移列表。
	 * 
	 * @param fileId     文件Id
	 * @param createDate 创建日期，格式为2019-08-01
	 */
	public static void addFile(Long fileId, String createDate) {
		fileSet.add(fileId + "," + createDate);
	}
	
	/**
	 * 保存提交的文件列表。
	 * @throws BusinessException
	 * @throws IOException
	 */
	public static void updateFileList() throws BusinessException, IOException {
		File dir = SystemEnvironment.getClusterPublicFolder("migrate");
		File file = new File(dir,"migrate.lst");
		StringBuilder sb = new StringBuilder();
		for (String entry : fileSet) {
			sb.append(entry).append("\n");
		}
		FileUtils.writeStringToFile(file, sb.toString(),"UTF-8");
	}
}
