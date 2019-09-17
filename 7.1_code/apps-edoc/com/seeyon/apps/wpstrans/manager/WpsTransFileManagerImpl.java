package com.seeyon.apps.wpstrans.manager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.wpstrans.util.WpsTransConstant;
import com.seeyon.ctp.common.exceptions.BusinessException;

/**
 * 内网文件传输服务-上传、下载等
 * @author tanggl
 *
 */
public class WpsTransFileManagerImpl implements WpsTransFileManager {

	private static final Log LOGGER = LogFactory.getLog(WpsTransFileManagerImpl.class);

	@Override
	public String upload(Map<String, String> paramMap, File sourceFile) throws BusinessException {
		BufferedOutputStream bos = null;
		BufferedInputStream bis = null;
        HttpURLConnection conn = null;
        String result = "";
        try {

        	StringBuilder urlBuffer = new StringBuilder();
        	urlBuffer.append(WpsTransConstant.WPSTRANS_FILE_SERVICE_URL);
        	urlBuffer.append("?operation=" + WpsTransConstant.WPSTRANS_FILE_SERVICE_UPLOAD);
        	urlBuffer.append("&filepath=" + paramMap.get("filepath"));
        	urlBuffer.append("&filename=" + paramMap.get("filename"));

            URL realUrl = new URL(urlBuffer.toString());
            // 打开和URL之间的连接
            conn = (HttpURLConnection) realUrl.openConnection();
            conn.setRequestProperty("Content-Type","txt/html");
            conn.setRequestProperty("Cache-Control","no-cache");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(true);
            conn.connect();
            conn.setConnectTimeout(10000);

            // 获取URLConnection对象对应的输出流
            bos = new BufferedOutputStream(conn.getOutputStream());
            //定义文件输出流(从相应目录读文件)
            bis = new BufferedInputStream(new FileInputStream(sourceFile));
            int len = 0;
            byte[] b = new byte[1024];
            while ((len = bis.read(b)) != -1) {
            	bos.write(b, 0, len);
            }
            bos.flush();

            //定义输入流来读取URL的响应
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            	bis = new BufferedInputStream(conn.getInputStream());

            	len = 0;
                b = new byte[1024];
                while ((len = bis.read(b)) != -1) {
                	result += new String(b, 0, len);
                }
            }

            conn.disconnect();
        } catch (Exception e) {
        	LOGGER.error("发送文件出现异常", e);
        } finally {
        	if(conn != null){
        		conn.disconnect();
        	}
        	IOUtils.closeQuietly(bos);
        	IOUtils.closeQuietly(bis);
        }

        return result;
    }

	@Override
	public String download(String serviceTargetPath, String newFilePath) throws BusinessException {
		String result = "";

		HttpURLConnection conn = null;
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
        try {

        	StringBuilder urlBuffer = new StringBuilder();
        	urlBuffer.append(WpsTransConstant.WPSTRANS_FILE_SERVICE_URL);
        	urlBuffer.append("?operation=" + WpsTransConstant.WPSTRANS_FILE_SERVICE_DOWNLOAD);
        	urlBuffer.append("&filepath=" + serviceTargetPath);

            URL realUrl = new URL(urlBuffer.toString());
            // 打开和URL之间的连接
            conn = (HttpURLConnection) realUrl.openConnection();
            conn.setRequestProperty("Content-Type", "application/octet-stream;charset=UTF-8");
            conn.setRequestProperty("Cache-Control","no-cache");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();

            //conn.getOutputStream().write(WpsTransConstant.WPSTRANS_FILE_SERVICE_DOWNLOAD.getBytes());

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            	bis = new BufferedInputStream(conn.getInputStream());
            	fos = new FileOutputStream(newFilePath);

                
            	byte[] b = new byte[1024];
                int len = 0;
                while ((len = bis.read(b)) != -1) {
                	fos.write(b, 0, len);

                	//LOGGER.info(new String(b));
                }
            	fos.flush();

            	result = "success";
            }

            conn.disconnect();
        } catch (Exception e) {
        	LOGGER.error("发送文件出现异常", e);
        } finally {
        	IOUtils.closeQuietly(fos);
        	IOUtils.closeQuietly(bis);
        }
		return result;
	}

	@Override
	public String handshake() throws BusinessException {
		HttpURLConnection conn = null;
		BufferedInputStream bis = null;
        String result = "";
        try {

        	StringBuilder urlBuffer = new StringBuilder();
        	urlBuffer.append(WpsTransConstant.WPSTRANS_FILE_SERVICE_URL);
        	urlBuffer.append("?operation=" + WpsTransConstant.WPSTRANS_FILE_SERVICE_HANDSHAKE);

            URL realUrl = new URL(urlBuffer.toString());
            // 打开和URL之间的连接
            conn = (HttpURLConnection) realUrl.openConnection();
            conn.setRequestProperty("Content-Type","txt/html");
            conn.setRequestProperty("Cache-Control","no-cache");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(true);
            conn.connect();
            conn.setConnectTimeout(10000);

            // 定义BufferedReader输入流来读取URL的响应
            bis = new BufferedInputStream(conn.getInputStream());

            byte[] b = new byte[1024];
            int len = 0;
            while ((len = bis.read(b)) != -1) {
            	result += new String(b, 0, len);
            }

            conn.disconnect();
        } catch (Exception e) {
        	LOGGER.error("发送文件出现异常", e);
        } finally {
        	IOUtils.closeQuietly(bis);
        }

		return result;
	}

	@Override
	public boolean isHandshakeSuccess(String result) throws BusinessException {
		return "success".equals(result);
	}

}
