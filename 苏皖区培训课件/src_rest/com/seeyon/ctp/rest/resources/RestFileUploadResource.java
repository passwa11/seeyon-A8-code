package com.seeyon.ctp.rest.resources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.seeyon.apps.common.kit.Base64Kit;
import com.seeyon.apps.restFileUpload.entity.FileEntity;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.services.FileUploadExporter;
import com.seeyon.ctp.services.ServiceException;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation.External;

/**
 * @author kkdo
 * @date 2019年4月29日 上午10:30:22
 * <pre>上传文件的接口</pre>
 */
@Path("upload")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces(MediaType.APPLICATION_JSON)
public class RestFileUploadResource extends BaseResource {
	
	private static final Log log = LogFactory.getLog(RestFileUploadResource.class);

	@POST
    @Path("/file")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_JSON)
	@RestInterfaceAnnotation(OpenExternal = External.YES, StartVersion = "V7.0SP2")
	public Response upload(Map<String, Object> param) {
		String loginName = (String) param.get("loginName");
		if(null == loginName || "".equals(loginName)) {
			return fail("请传登录名[loginName]字段！");
		}
		List<?> files = (List<?>) param.get("files");
		if(null != files && files.size() > 0) {
			String installPath = SystemEnvironment.getApplicationFolder();
			// 输出pdf的目录
			installPath = installPath.split("ApacheJetspeed")[0].replaceAll("\\\\", "/");
			// 需要清理
			String filePath = installPath + "base/upload/tempload/";
			File directory = new File(filePath);
			if(!directory.exists()) {
				directory.mkdirs();
			}
			List<String> fileNames = new ArrayList<String>();
			// 处理文件 上传
			String[] paths = new String[files.size()];
			for(int i = 0; i < files.size(); i++) {
				FileEntity file = JSON.parseObject(JSON.toJSONString(files.get(i)), FileEntity.class);
				String name = file.getFileName();
				if(fileNames.contains(name)) {
					name += "_1";
				}
				fileNames.add(name);
				name += "." + file.getSuffix();
				String path = filePath + name;
				Base64Kit.base64StringToFile(file.getBase64Str(), path);
				paths[i] = path;
			}
			try {
				String fileIds = upload(loginName, paths);
				// 清理文件夾
				clean(directory);
				return success(fileIds);
			} catch (ServiceException e) {
				log.error("上传文件失败：", e);
				return fail("上传文件失败：" + e.getMessage());
			}
			// 返回数据
		} else {
			return fail("请传文件列表[files]字段！");
		}
	}
	
	private String upload(String loginName, String[] paths) throws ServiceException {
		FileUploadExporter fileUpload = new FileUploadExporter();
		String fileId = fileUpload.processUpload(loginName, paths);
		return fileId;
	}
	
	private void clean(File path) {
		try {
			FileUtils.cleanDirectory(path);
		} catch (IOException e) {
			log.error("清理文件夹失败：", e);
		}
	}
}
