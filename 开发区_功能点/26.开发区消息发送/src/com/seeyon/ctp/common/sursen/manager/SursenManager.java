package com.seeyon.ctp.common.sursen.manager;



import java.io.File;
import java.util.Date;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;


/**
 * 书生正文Manager
 * @author Administrator
 *
 */
public interface SursenManager {
	


	
	
	/**
	 * 查询书生文件的路径
	 * @param data
	 * @return
	 */
	public String getSursenFileLocation(String data) ;
	/**
	 * 查询书生文件是否存在
	 * @param data
	 * @return
	 */
	public String ajaxSursenFileExist(String data) ;
	
	
}