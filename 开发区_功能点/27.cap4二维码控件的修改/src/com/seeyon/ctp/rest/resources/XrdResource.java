package com.seeyon.ctp.rest.resources;

import com.seeyon.apps.mplus.api.MplusApi;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.rest.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.songjian.utils.json.JSONArray;
import org.songjian.utils.json.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * 信任度合同，即电子合同
 * */
@Path("xrd")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces(MediaType.APPLICATION_JSON)
public class XrdResource extends BaseResource {
	private static final Log LOGGER = CtpLogFactory.getLog(XrdResource.class);

	/* 企业appkey */
	private String appKey = "";
	private String appSecret = "";
	/* 平台校验码 **/
	private static String check = "a4236bfe412311e8953f00163e06c15s";
	private static String serviceCode = "m20000000000003004";

	private String getMplusUrl(){
		String mplusUrl = null;
		MplusApi mplusApi = (MplusApi) AppContext.getBean("mplusApi");
		try {
			mplusUrl = mplusApi.getDomain()+"/svr/";
		} catch (BusinessException e) {
			LOGGER.error(e);
		}
		return mplusUrl;
	}

	private String getTicket() {
		String ticket = null;
		try {
			MplusApi mplusApi = (MplusApi)AppContext.getBean("mplusApi");
			ticket = mplusApi.getTicket(serviceCode);
			LOGGER.info("获取到的ticket:"+ticket);
		} catch (Exception e) {
			LOGGER.error(e);
		}
		return ticket;
	}
	
	
	/**
	 * 授权
	 */
	private void authorization(String comEmail) {
		String ticket = getTicket();
		if(StringUtils.isBlank(ticket)) {
			appKey =null;
			appSecret = null;
			return;
		}
		JSONObject json = new JSONObject();
		json.put("ticket", ticket);
		json.put("type", "2");
		json.put("comEmail", comEmail);
		json.put("sign", check);
		json.put("channel", "1");
		String rt = CommonUtil.post(getMplusUrl()+"contract_1", json);
		JSONObject retJson = JSONObject.parseObject(rt);
		if ("1000".equals(retJson.getString("code"))&&retJson.getJSONObject("data")!=null) {
			appKey = retJson.getJSONObject("data").getString("appKey");
			appSecret = retJson.getJSONObject("data").getString("appSecret");
		}

	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("longin")
	public Response longin(@QueryParam("comEmail") String comEmail, @QueryParam("password") String password,
			@QueryParam("signObj") String signObj) {
		String rt = null;
		try {
			LOGGER.info("测试更新版本是否正常========================================================================");
			authorization(comEmail);
			String ticket = getTicket();
			if(StringUtils.isBlank(ticket)) {
				return fail("Please contact the Enterprise Manager to configure mplus setting ");
			}
			JSONObject json = new JSONObject();
			json.put("comEmail", comEmail);
			json.put("password", password);
			json.put("channel", signObj);
			json.put("appKey", appKey);
			Map<String, Object> retMap = CommonUtil.parseJSON2Map(json.toJSONString());
			String sign = CommonUtil.getMd5B2bSign(retMap, appSecret);
			json.put("ticket", ticket);
			json.put("type", "1");
			json.put("sign", sign);
			LOGGER.info("请求报文为：" + json.toJSONString());
			rt = CommonUtil.post(getMplusUrl()+"contract_2", json);
			if (rt == null) {
				JSONObject reJson = new JSONObject();
				String comName = CommonUtil.comName;
				reJson.put("comName", comName);
				Long id = CommonUtil.id;
				String suffix = comName.substring(comName.indexOf(".") + 1, comName.length());
				JSONArray array = new JSONArray();
				FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
				File file = fileManager.getFile(id, new Date());
				if ("zip".equals(suffix)) {
					reJson.put("comName", comName.replace(".zip", ""));
					Map<String, String> map = CommonUtil.dozip(file);
					Set<String> ids = map.keySet();
					for (String str : ids) {
						JSONObject officialUrl = new JSONObject();
						officialUrl.put("officialUrl",
								str);
						officialUrl.put("date",
								CommonUtil.date);
						officialUrl.put("value", map.get(str));
						array.add(officialUrl);
					}
					reJson.put("officialUrls", array);
				} else {
					
					JSONObject officialUrl = new JSONObject();
					officialUrl.put("officialUrl",
							id.toString());
					officialUrl.put("value", comName);
					officialUrl.put("date",
							CommonUtil.date);
					array.add(officialUrl);
					reJson.put("officialUrls", array);
				}
				reJson.put("resultCode", "200");
				LOGGER.info("返回报文为：" + reJson.toJSONString());
				return success(reJson.toJSONString());
			}
		} catch (Exception e) {
			LOGGER.error(e);
			fail("SystemException");
		}
		return success(JSONObject.parse(rt));
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("notifyContract")
	public Response notifyContract(@QueryParam("data") String data, @QueryParam("fileid") String fileid,
			@QueryParam("formid") String formid, @QueryParam("corresponding") String corresponding) {
		String rt = null;
		try {
			JSONObject json = JSONObject.parseObject(data);
			authorization(json.getString("comEmail"));
			FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
			File file = fileManager.getFile(Long.parseLong(fileid), new Date());
			if(StringUtils.isBlank(com.seeyon.ctp.rest.util.FileType.getFileByFile(file))) {
				return fail("Please upload the PDF file");
			};
			json.put("appKey", appKey);
			json.put("contractType", "pdf");
			json.put("signType", "2");
			
			
			String ticket = getTicket();
			if(StringUtils.isBlank(ticket)) {
				return fail("Please contact the Enterprise Manager to configure mplus setting ");
			}
			rt = CommonUtil.formUpload(getMplusUrl()+ "contract_upload?type=7&channel=" + json.getString("channel")+"&ticket="+ticket, file, "pdf");
			JSONObject ls = JSONObject.parseObject(rt);
			if(!"1000".equals(ls.getString("code"))) {
				return fail("Please contact the Enterprise Manager to configure mplus setting ");
			}
			json.put("contract", ls.getJSONObject("data").getString("contract"));
			ticket = getTicket();
			if(StringUtils.isBlank(ticket)) {
				return fail("Please contact the Enterprise Manager to configure mplus setting ");
			}
		
			Map<String, Object> retMap = CommonUtil.parseJSON2Map(json.toJSONString());
			json.put("ticket", ticket);
			json.put("type", "3");
			String sign = CommonUtil.getMd5B2bSign(retMap, appSecret);
			json.put("sign", sign);
			rt = CommonUtil.post(getMplusUrl() + "contract_1", json);
			CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
			FormDataMasterBean dataBean = cap4FormManager.getSessioMasterDataBean(Long.valueOf(formid));
			ls = JSONObject.parseObject(rt);
			JSONObject correspondingJson = JSONObject.parseObject(corresponding);
			Set<String> strs = correspondingJson.keySet();
			for (String str : strs) {
				dataBean.addFieldValue(correspondingJson.getString(str), ls.getJSONObject("data").getString(str));
			}
		} catch (Exception e) {
			LOGGER.error(e);
			fail("Failed to obtain file");
		}
		return success(JSONObject.parse(rt));
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("enterSign")
	public Response enterSign(@QueryParam("data") String data) {
		JSONObject json = JSONObject.parseObject(data);
		authorization(json.getString("comEmail"));
		String ticket = getTicket();
		if(StringUtils.isBlank(ticket)) {
			return fail("Please contact the Enterprise Manager to configure mplus setting ");
		}
		json.put("appKey", appKey);
		Map<String, Object> retMap = CommonUtil.parseJSON2Map(json.toJSONString());
		json.put("ticket", ticket);
		json.put("type", "4");
		String sign = CommonUtil.getMd5B2bSign(retMap, appSecret);
		json.put("sign", sign);
		String rt = CommonUtil.post(getMplusUrl() + "contract_1", json);
		return success(JSONObject.parse(rt));
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("dow")
	public Response dow(@QueryParam("data") String data) {
		try {
			JSONObject json = JSONObject.parseObject(data);
			authorization(json.getString("comEmail"));
			json.remove("comEmail");
			json.put("appKey", appKey);
			String ticket = getTicket();
			if(StringUtils.isBlank(ticket)) {
				return fail("Please contact the Enterprise Manager to configure mplus setting ");
			}
			Map<String, Object> retMap = CommonUtil.parseJSON2Map(json.toJSONString());
			String sign = CommonUtil.getMd5B2bSign(retMap, appSecret);
			json.put("ticket", ticket);
			json.put("type", "6");
			json.put("sign", sign);
			String rt = CommonUtil.post(getMplusUrl() + "contract_2", json);
			if (rt == null) { 
				JSONObject reJson = new JSONObject();
				String comName = CommonUtil.comName;
				reJson.put("comName", comName); 
				Long id = CommonUtil.id; 
				FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
				File file = fileManager.getFile(id, new Date());
				reJson.put("pdf",id.toString());
				reJson.put("v",
						CommonUtil.v);
				CommonUtil.pdf2multiImage(file);
				id = CommonUtil.id;
				reJson.put("pdfurl",
						id.toString());
				reJson.put("pdfv",
						CommonUtil.v);
				reJson.put("date",
						CommonUtil.date);
				reJson.put("resultCode", "200");
				LOGGER.info("返回报文为：" + reJson.toJSONString());
				return success(reJson.toJSONString());
			}
			return fail("Get file exceptions");
		} catch (Exception e) {
			LOGGER.error(e);
			return fail("SystemException");
		}

	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("signState")
	public Response signState(@QueryParam("data") String data) {
		try {
			JSONObject json = JSONObject.parseObject(data);
			authorization(json.getString("comEmail"));
			json.put("appKey", appKey);
			json.remove("comEmail");
			Map<String, Object> retMap = CommonUtil.parseJSON2Map(json.toJSONString());
			String sign = CommonUtil.getMd5B2bSign(retMap, appSecret);
			json.put("type", "8");
			String ticket = getTicket(); 
			if(StringUtils.isBlank(ticket)) {
				return fail("Please contact the Enterprise Manager to configure mplus setting ");
			}
			json.put("ticket", ticket);
			json.put("sign", sign);
			String rt = CommonUtil.post(getMplusUrl() + "contract_1", json);
			return success(rt);
		} catch (Exception e) {
			LOGGER.error(e);
			return fail("SystemException");
		}

	}

}
