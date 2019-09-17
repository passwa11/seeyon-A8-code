package com.seeyon.apps.ocip.exchange.edoc;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.govdoc.manager.GovdocExchangeManager;
import com.seeyon.apps.govdoc.po.GovdocExchangeDetail;
import com.seeyon.apps.govdoc.po.GovdocExchangeMain;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.Base64;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ocip.common.org.OcipOrgMember;
import com.seeyon.ocip.common.organization.IOrganizationManager;
import com.seeyon.ocip.common.utils.Global;
import com.seeyon.ocip.configuration.OcipConfiguration;
import com.seeyon.ocip.exchange.exceptions.TransportException;
import com.seeyon.ocip.online.OnlineChecker;

public class GovdocOcipSSOUtil {
	private static final Log log = LogFactory.getLog(GovdocOcipSSOUtil.class);

	/**
	 * 公文和协同穿透都可以走这里
	 * 
	 * @param request
	 * @return ModelAndView
	 * @throws Exception
	 */
	public static ModelAndView getOutEdocLink(HttpServletRequest request) throws Exception {
		// 取值范围{0,1}从页面穿透过来的用0表示，用于表示需要进行获取地址跳转；获取地址后访问的用1表示，用于绕过内部权限检查
		String ocipCas = request.getParameter("ocipCas");
		if ("0".equals(ocipCas)) {
			return getEdocModel(request);
		} else {
			return null;
		}

	}
	public static ModelAndView getGovdocModel(HttpServletRequest request) {
		String affairId = request.getParameter("affairId");
		String summaryId = request.getParameter("summaryId");
		String requestAddress = "";
		try {
			AffairManager affairManager = (AffairManager) AppContext.getBean("affairManager");
			CtpAffair affair = affairManager.get(Long.valueOf(affairId));
			String sendSysCode = "";
			String link = "";
			String localSystemCode = Global.getConfig("sysCode");
			Long memberId = null;
			Map<String, Object> affairExtMap = AffairUtil.getExtProperty(affair);
			if (affairExtMap != null) {
				sendSysCode = (String) affairExtMap.get("ocipSystemCode");
				memberId = (Long) affairExtMap.get("memberId_of_ocip_client");
			}
			
			// 本地系统的affair直接跳出
			if (Strings.isBlank(sendSysCode)) {
				return null;
			}
			boolean online = OnlineChecker.isOnline();
			if (!online) {
				// 本地系统没有连接上OCIP
				return rendError(OnlineChecker.OFFLINE_ERROR);
			}
			try {
				requestAddress = OcipConfiguration.getInstance().getExchangeSpi().getTransportService()
						.requestAddress(sendSysCode);
			} catch (Throwable e) {
				if (e instanceof TransportException) {
					return rendError(((TransportException) e).getErrorMsg());
				}
				return rendError("获取外部系统地址错误，请联系管理员！msg=" + e.getMessage());
			}
			if (Strings.isBlank(requestAddress)) {
				return rendError("获取外部系统地址错误，请联系管理员！");
			}
			
			String uId = encodeData(memberId != null ? memberId.toString() : "");
			String url = "%2fseeyon%2fgovdoc%2fgovdoc.do%3Fmethod%3Dsummary%26isFromHome%3Dtrue%26openFrom%3DlistPending%26affairId%3D"+affairId+"%26app%3D4&summaryId%3D"+summaryId;
			if (Strings.isNotBlank(affairId) && sendSysCode != null && sendSysCode.startsWith("0|system")) {
				link = "http://" + requestAddress + "/seeyon/login/sso?from=colSSO&t=" + uId+"&tourl="+url;
			}
			return new ModelAndView("common/redirect", "redirectURL", link);
		} catch (Exception e1) {
			log.error(e1);
			return null;
		}
	}
//	private static ModelAndView getCollaborationModel(HttpServletRequest request, IOrganizationManager organizationManager) {
//		String link = "";
//		String requestAddress = "";
//		ModelAndView modelAndView = null;
//		String affairId = request.getParameter("affairId");
//		try {
//			AffairManager affairManager = (AffairManager) AppContext.getBean("affairManager");
//			CtpAffair affair = affairManager.get(Long.valueOf(affairId));
//			String sendSysCode = "";
//			Map<String, Object> affairExtMap = AffairUtil.getExtProperty(affair);
//			if (affairExtMap != null) {
//				sendSysCode = (String) affairExtMap.get("ocipSystemCode");
//			}
//			// 本地系统的affair直接跳出
//			if (Strings.isBlank(sendSysCode)) {
//				return null;
//			}
//			boolean online = OnlineChecker.isOnline();
//			if (!online) {
//				// 本地系统没有连接上OCIP
//				return rendError(OnlineChecker.OFFLINE_ERROR);
//			}
//			try {
//				requestAddress = OcipConfiguration.getInstance().getExchangeSpi().getTransportService().requestAddress(sendSysCode);
//			} catch (Throwable e) {
//				if (e instanceof TransportException) {
//					return rendError(((TransportException) e).getErrorMsg());
//				}
//				return rendError("获取外部系统地址错误，请联系管理员！msg=" + e.getMessage());
//			}
//			if (Strings.isBlank(requestAddress)) {
//				return rendError("获取外部系统地址错误，请联系管理员！");
//			}
//			if (Strings.isNotBlank(affairId) && sendSysCode != null && sendSysCode.startsWith("0|system")) {
//				// String loginName =
//				// AppContext.getCurrentUser().getLoginName();
//				// 获取当前用户平台ID
//				OcipOrgMember ocipMember = organizationManager.getMember(String.valueOf(AppContext.getCurrentUser().getId()), Global.getConfig("sysCode"));
//				String uId = encodeData(ocipMember.getId());
//				String decodeAffairId = encodeData(affairId);
//				link = "http://" + requestAddress + "/seeyon/colView.do?ticket=" + uId + "&affairId=" + decodeAffairId;
//				// 代理处理
//				String isAgent = request.getParameter("isAgent");
//				if ("true".equals(isAgent)) {
//					link += "&isAgent=true";
//				}
//			}
//			if (Strings.isNotBlank(link)) {
//				modelAndView = new ModelAndView("common/redirect", "redirectURL", link);
//			}
//		} catch (Exception e) {
//			modelAndView = null;
//			log.error(e);
//		}
//		return modelAndView;
//	}

	private static ModelAndView getEdocModel(HttpServletRequest request) {
		String detailId = request.getParameter("detailId");
		String summaryId = request.getParameter("summaryId");
		try {
			String sendSysCode = "";
			String address = "";
			String link = "";
			String localSystemCode = Global.getConfig("sysCode");
			EdocLoginSSO loginSSo = new EdocLoginSSO();
			GovdocExchangeManager govdocExchangeManager = (GovdocExchangeManager) AppContext.getBean("govdocExchangeManager");
			if (detailId != null) {
				GovdocExchangeDetail detail = govdocExchangeManager.getExchangeDetailById(Long.parseLong(detailId));
				Map<String, Object> map = GovdocUtil.getExtProperty(detail);
				sendSysCode = (String) map.get("ocipSyscode");
				loginSSo.setDetailId(Long.valueOf(detailId));
			} else if (summaryId != null) {
				GovdocExchangeMain main = govdocExchangeManager.findBySummaryId(Long.parseLong(summaryId), GovdocExchangeMain.EXCHANGE_TYPE_JIAOHUAN);
				if(main!=null){
					sendSysCode = main.getOcipSysCode();
					loginSSo.setSummaryId(Long.parseLong(summaryId));
				}
			}
			// 不需要单点登录的公文
			if (Strings.isBlank(sendSysCode)) {
				return null;
			}
			boolean online = OnlineChecker.isOnline();
			if (!online) {
				// 本地系统没有连接上OCIP
				return rendError(OnlineChecker.OFFLINE_ERROR);
			}
			IOrganizationManager organizationManager = (IOrganizationManager) AppContext.getBean("organizationManager");
			// 获取当前人员的平台信息，用于单点登录校验
			OcipOrgMember member = organizationManager.getMember(AppContext.getCurrentUser().getId().toString(), localSystemCode);
			if (null == member) {
				return rendError("您的数据未成功上报至平台，无法查看外部系统信息！");
			}
			// 获取单点登录服务器地址
			try {
				address = OcipConfiguration.getInstance().getExchangeSpi().getTransportService().requestAddress(sendSysCode);
			} catch (Throwable e) {
				if (e instanceof TransportException) {
					return rendError(((TransportException) e).getErrorMsg());
				}
				return rendError("获取外部系统地址错误，请联系管理员！msg=" + e.getMessage());
			}
			if (Strings.isBlank(address)) {
				return rendError("获取外部系统地址错误，请联系管理员！");
			}
			// 设置单点登录信息
			loginSSo.setTicket(member.getId());
			String ticket = encodeData(JSONUtil.toJSONString(loginSSo));
			link = "http://" + address + "/seeyon/ocipEdoc.do?ticket=" + ticket;
			return new ModelAndView("redirect:"+ link);
		} catch (Exception e1) {
			log.error(e1);
			return null;
		}
	}

	private static ModelAndView rendError(String error) {
		return new ModelAndView().addObject("error", Strings.escapeJavascript(error));
	}

	/**
	 * ajax调用目标系统
	 * 
	 * @param sendSysCode
	 * @param managerName
	 * @param methodName
	 * @deprecated 暂时没有用
	 * @return
	 */
//	@Deprecated
//	public static String ajax(String sendSysCode, String managerName, String methodName, String parameters) {
//		if (Strings.isBlank(sendSysCode) || Strings.isBlank(managerName)) {
//			return "";
//		}
//		String link = "";
//		String requestAddress = "";
//
//		try {
//			try {
//				requestAddress = OcipConfiguration.getInstance().getExchangeSpi().getTransportService().requestAddress(sendSysCode);
//			} catch (Exception e) {
//
//			}
//			if (sendSysCode != null && sendSysCode.startsWith("0|system")) {
//				String loginName = AppContext.getCurrentUser().getLoginName();
//				link = "http://" + requestAddress + "/seeyon/colView.do?method=ajaxAction&ticket=" + loginName + "&managerName=" + managerName + "&managerMethod" + methodName;
//				Map<String, Object> paramMap = new HashMap<String, Object>();
//				if (parameters != null) {
//					paramMap.put("arguments", parameters);
//				}
//				String post = post(link, paramMap);
//				return post;
//			} else {
//				return "";
//			}
//
//		} catch (Exception e) {
//			log.error("->", e);
//		}
//
//		return "";
//	}
//
//
//	/**
//	 * 
//	 * @param url
//	 * @param parameters
//	 * @deprecated 暂时没有用
//	 * @return
//	 */
//	@Deprecated
//	public static String post(String url, Map<String, Object> parameters) {
//		BufferedInputStream bis = null;
//		OutputStreamWriter osw = null;
//		HttpURLConnection conn = null;
//		StringBuilder result = new StringBuilder();
//		try {
//			// 构建请求参数
//			StringBuffer sbParams = new StringBuffer();
//			if (parameters != null && parameters.size() > 0) {
//				for (Entry<String, Object> e : parameters.entrySet()) {
//					sbParams.append(e.getKey());
//					sbParams.append("=");
//					sbParams.append(e.getValue());
//					sbParams.append("&");
//				}
//			}
//
//			URL realUrl = new URL(url);
//			// 打开和URL之间的连接
//			conn = (HttpURLConnection) realUrl.openConnection();
//			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//			conn.setRequestProperty("Cache-Control", "no-cache");
//			conn.setRequestProperty("Charsert", "UTF-8");
//			conn.setRequestMethod("POST");
//			conn.setDoOutput(true);
//			conn.setDoInput(true);
//			conn.setUseCaches(false);
//			conn.setInstanceFollowRedirects(true);
//			conn.setConnectTimeout(10000);
//			conn.connect();
//			if (sbParams != null && sbParams.length() > 0) {
//				osw = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
//				osw.write(sbParams.substring(0, sbParams.length() - 1));
//				osw.flush();
//			}
//
//			// 定义输入流来读取URL的响应
//			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
//				bis = new BufferedInputStream(conn.getInputStream());
//
//				int len = 0;
//				byte[] b = new byte[1024 * 5];
//				while ((len = bis.read(b)) != -1) {
//					result.append(new String(b, 0, len));
//				}
//			}
//		} catch (Exception e) {
//			log.error("post请求异常", e);
//		} finally {
//			IOUtils.closeQuietly(osw);
//			IOUtils.closeQuietly(bis);
//			conn.disconnect();
//		}
//		return result.toString();
//	}

	/**
	 * 对给定的字符串进行base64加密操作
	 */
	public static String encodeData(String inputData) {
		try {
			if (null == inputData) {
				return null;
			}
			return new String(Base64.encodeBase64(inputData.getBytes("UTF-8"), false));
		} catch (UnsupportedEncodingException e) {
			log.error(inputData, e);
		}
		return null;
	}
}
