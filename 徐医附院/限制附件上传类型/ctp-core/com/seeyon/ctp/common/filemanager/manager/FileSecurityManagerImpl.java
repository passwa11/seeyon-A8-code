package com.seeyon.ctp.common.filemanager.manager;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.cache.CacheAccessable;
import com.seeyon.ctp.common.cache.CacheFactory;
import com.seeyon.ctp.common.cache.CacheSet;
import com.seeyon.ctp.common.log.CtpLogFactory;
/**
 * 项目名称：ctp-core    
 * 文件名称：FileSecurityManagerImpl.java    
 * 功能描述： 文件内容是否需要登录的实现类，这个类应该是单例的(通过Spring配置的bean默认就是单例的)
 * 创建人：zhiyanqiang  
 * 创建时间：2016年4月20日
 */
public class FileSecurityManagerImpl implements FileSecurityManager {
	//日志对象log
    private static final Log log = CtpLogFactory.getLog(FileSecurityManagerImpl.class);
    CacheAccessable factory = CacheFactory.getInstance(FileSecurityManagerImpl.class);
    //记录不需要登录的文件Id的Set,存在修改的情况，考虑集群的情况使用缓存组件
    private CacheSet<Long> needLessLoginCache = factory.createSet("_File_SECURITY_");
	@Override
	public boolean isNeedlessLogin(Long fileId) {
		boolean isNeedlessLogin=needLessLoginCache.contains(fileId);
		log.info("对文件:" + fileId +"进行判断是否需要登录-->" + isNeedlessLogin);
		return isNeedlessLogin;
	}

	@Override
	public void addNeedlessLogin(Long fileId) {
		needLessLoginCache.add(fileId);

	}
	@Override
	public void addNeedlessLogin(Collection<Long> fileIds) {
		needLessLoginCache.addAll(fileIds);
	}

	@Override
	public void addNeedlessLogin(String fileIdStr) {
		if(isNumeric(fileIdStr))
		{
			//此处的添加白名单只是权限的校验，不影响业务的处理，所以可以直接在内部进行处理
			//即使是添加出错了，不应该影响业务逻辑
			try{
				Long fileId=Long.valueOf(fileIdStr);
				needLessLoginCache.add(fileId);
			}
			catch(Exception e)
			{
				log.error("将文件Id添加到白名单时出错了！"+e.fillInStackTrace());
			}
			
		}
		
	}
	/**
	 * 判定给出的字符串是否为数字格式的（含负数的情况(-)）
	 * 创建人:zhiyanqiang	
	 * 对功能的增强：现在解析文件Id时不规范，添加该校验，增强容错性 
	 * 创建时间：2016年4月22日 下午4:11:39    
	 * @param str
	 * @return 
	 * boolean
	 */
	private boolean isNumeric(String str){ 
		   Pattern pattern = Pattern.compile("-?[0-9]+"); 
		   Matcher isNum = pattern.matcher(str);
		   if( !isNum.matches() ){
		       return false; 
		   } 
		   return true; 
		}

}
