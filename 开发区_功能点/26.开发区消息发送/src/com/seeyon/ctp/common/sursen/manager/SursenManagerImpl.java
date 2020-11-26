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
public class SursenManagerImpl implements SursenManager{
	

	private static Log log = LogFactory.getLog(SursenManagerImpl.class);
	
	private static String rc="com.seeyon.v3x.common.resources.i18n.SeeyonCommonResources";
	
	private Long fileId;

	private Date createDate;
	
	private Long originalFileId;
	
	private Date originalCreateDate;
	
	private boolean needClone = false;
	
	private boolean needReadFile = false;

	private FileManager fileManager;
	
	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}
	
	/**
	 * 查询书生文件的路径
	 * @param data
	 * @return
	 */
	public String getSursenFileLocation(String data) {
	    String sursenFileLocation = "";
	    boolean isFileExsit = false;
	    if ((Strings.isNotBlank(data))) {
	    	try {
				readJsonData(data);
				Long loadFileId = originalFileId != null ? originalFileId : fileId;
				Date loadCreateDate = originalCreateDate != null ? originalCreateDate : createDate;
				String filePath = this.fileManager.getFolder(loadCreateDate, true) + File.separator ;
				V3XFile	tempFile = fileManager.getV3XFile(loadFileId);
				if(tempFile != null){
					if(tempFile.getUpdateDate() != null){
						filePath = this.fileManager.getFolder(tempFile.getUpdateDate(), true)+ File.separator ;
						isFileExsit = isFileExsit(filePath,loadFileId);
					}else{
						filePath = this.fileManager.getFolder(tempFile.getCreateDate(), true)+ File.separator ;
					}
				}
				if(!isFileExsit){
					filePath = "" ;
				}else{
					filePath += loadFileId;
				}
				if(needReadFile){
					 sursenFileLocation = filePath;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
//				log.error("获取书生文件路径错误：", e);
				return sursenFileLocation;
			}
				
		}
	    return sursenFileLocation;
	  }
	
	public String ajaxSursenFileExist(String data) {
	    boolean sursenFileIsExist = false;
	    if ((Strings.isNotBlank(data))) {
	    	try {
				readJsonData(data);
				Long loadFileId = originalFileId != null ? originalFileId : fileId;
				Date loadCreateDate = originalCreateDate != null ? originalCreateDate : createDate;
				String filePath = this.fileManager.getFolder(loadCreateDate, true) + File.separator ;
				V3XFile	tempFile = fileManager.getV3XFile(loadFileId);
				if(tempFile != null){
					if(tempFile.getUpdateDate() != null){
						filePath = this.fileManager.getFolder(tempFile.getUpdateDate(), true)+ File.separator ;
						sursenFileIsExist = isFileExsit(filePath,loadFileId);
					}
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
//				log.error("获取书生文件路径错误：", e);
				return sursenFileIsExist+"";
			}
		}
	    return sursenFileIsExist+"";
	  }
	
	private boolean isFileExsit(String path ,Long fileId){
		File ftemp = new File(path+fileId);	
		if(ftemp.exists() && ftemp.isFile()){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 解析查询书生文件data
	 * @param data
	 */
	public void readJsonData(String data) throws Exception {
		Map<String,String> map = (Map<String,String>)JSONUtil.parseJSONString(data);
		fileId = Long.valueOf(map.get("RECORDID"));
		createDate = Datetimes.parseDatetime(map.get("CREATEDATE"));
		String _originalFileId = map.get("ORIGINALFILEID");
		needClone = _originalFileId != null && !"".equals(_originalFileId.trim());
		needReadFile = Boolean.parseBoolean(map.get("NEEDREADFILE"));
		if(needClone){
		    String _originalCreateDate = map.get("ORIGINALCREATEDATE");
			originalFileId = Long.valueOf(_originalFileId);
			originalCreateDate = Datetimes.parseDatetime(_originalCreateDate);
		}
	}
}