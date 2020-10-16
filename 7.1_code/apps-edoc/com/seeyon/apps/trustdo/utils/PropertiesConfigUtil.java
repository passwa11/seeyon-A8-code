package com.seeyon.apps.trustdo.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;

import com.seeyon.apps.trustdo.dao.impl.XRDDaoImpl;
import com.seeyon.ctp.common.log.CtpLogFactory;

public class PropertiesConfigUtil {
	
	private static final Log LOGGER = CtpLogFactory.getLog(PropertiesConfigUtil.class);
	private static Map<String,Properties> propMap = new HashMap<String,Properties>();

	public static Object getProperty(String filePath,String key){
		Properties prop = getProperties(filePath);
		if(prop != null && prop.get(key) != null){
			return prop.get(key);
		}
		return null;
	}

	public static Properties getProperties(String filePath){
		InputStream in = null;
		try {
			if(propMap.get(filePath) == null){
				Properties prop = new Properties();				  
				in = new BufferedInputStream(new FileInputStream(filePath)); 
				prop.load(new InputStreamReader(in, "utf-8"));
				propMap.put(filePath,prop);
				return prop;
			}else{
				return propMap.get(filePath);
			}
		} catch (IOException e) {
			LOGGER.error(e);
			//e.printStackTrace();
		} finally{
			if(in != null){
				try{
					in.close();
				}catch(IOException e){
					LOGGER.debug(e);
				}
			}
		}
		return null;
	}

	/**
	 * 更新配置文件
	 * @param prop
	 * @param filePath
	 */
	 public static void updateProperties(Properties prop,String filePath){

		 FileInputStream fis = null;
		 BufferedInputStream bis = null;
		 FileOutputStream fos = null;
		 try {
			 File file = new File(filePath);

			 Properties tmpProp = new Properties();
			 fis = new FileInputStream(file);
			 bis = new BufferedInputStream(fis);
			 tmpProp.load(bis);

			 fos = new FileOutputStream(file);
			 for(Object key : prop.keySet()){
				 tmpProp.setProperty(String.valueOf(key),String.valueOf(prop.get(key)));
			 }
			 tmpProp.store(fos, null);
		 } catch (Exception e) {
			 LOGGER.error(e);
			 //e.printStackTrace();
		 }finally{
			 if(fis != null){
				 try{
					 fis.close();
				 }catch(IOException e){
					 LOGGER.error(e);
				 }
			 }
			 if(bis != null){
				 try{
					 bis.close();
				 }catch(IOException e){
					 LOGGER.error(e);
				 }
			 }
			 if(fos != null){
				 try{
					 fos.close();
				 }catch(IOException e){
					 LOGGER.error(e);
				 }
			 }
		 }
	 }

	 /**
	  * 重新加载配置文件
	  * @param filePath
	  */
	 public static boolean reloadProperties(String filePath){
		 try {
			 Properties prop = new Properties();
			 prop.load(new FileInputStream(filePath));
			 propMap.put(filePath,prop);
			 return true;
		 } catch (IOException e) {
			 LOGGER.error(e);
			 //e.printStackTrace();
			 return false;
		 }
	 }
}
