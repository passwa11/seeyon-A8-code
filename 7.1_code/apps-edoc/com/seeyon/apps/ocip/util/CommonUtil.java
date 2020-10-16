package com.seeyon.apps.ocip.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.bzip2.CBZip2OutputStream;

import com.alibaba.fastjson.JSONObject;
//import com.seeyon.apps.ocip.entity.OcipReturnData;
import com.seeyon.apps.ocip.mock.MockHttpServletRequest;
import com.seeyon.apps.ocip.mock.MockHttpSession;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ocip.common.exceptions.InterfaceException;
import com.seeyon.v3x.common.web.login.CurrentUser;

public class CommonUtil {
	private final static Log logger = LogFactory.getLog(CommonUtil.class);

	private static String DEFAUT_NAME;

	/**
	 * 统一处理平台下发的报文
	 * 
	 * @param data
	 * @return
	 * @authur wxt.touxin
	 * @version 2017年6月15日
	 */
//	public static String parseData(String data) throws InterfaceException {
//		OcipReturnData returnData = null;
//		try {
//			returnData = JSONObject.parseObject(data, OcipReturnData.class);
//		} catch (Exception e) {
//			logger.error("解析平台下发数据出错，终止当前操作！", e);
//			throw new InterfaceException(e.getMessage(), e);
//		}
//		if (!"0".equals(returnData.getCode())) {
//			logger.error("平台下发数据处理失败!");
//			throw new InterfaceException(returnData.getMsg());
//		}
//		return returnData.getObj();
//	}

	/**
	 * 模拟使用所属单位的单位管理员登录
	 * 
	 * @authur wxt.touxin
	 * @version 2017年3月28日
	 */
	public static boolean userLogin(OrgManager orgManager, Long accountId) throws BusinessException {
		try {
			User user = AppContext.getCurrentUser();
			// 模拟用户登录
			if (user == null) {
				user = new User();
				if (accountId != null) {

					V3xOrgMember member = orgManager
							.getAdministrator(accountId);
					if (member == null) {
						member = orgManager.getGroupAdmin();
					}
					user.setAccountId(accountId);
					user.setLoginAccount(accountId);
					if (member != null) {
						user.setId(member.getId());
						user.setName(member.getName());
						user.setLoginName(member.getLoginName());
					}

				} else {
					String loginName = getDefautName(orgManager);
					V3xOrgMember member = null;
					member = orgManager.getMemberByLoginName(loginName);
					user.setId(member.getId());
					user.setAccountId(member.getOrgAccountId());
					user.setLoginAccount(member.getOrgAccountId());
					user.setLoginName(member.getLoginName());
					user.setName(member.getName());
				}
				CommonUtil.setCurrentUser(user);
			}
			return true;
		} catch (BusinessException e) {
			throw new BusinessException("模拟登录失败");
		}
		
	}
	
	/**
	 * 模拟登陆指定用户
	 * @param orgManager
	 * @param memberId
	 * @return
	 * @throws BusinessException
	 */
	public static boolean ocipUserLogin(OrgManager orgManager, Long memberId) throws BusinessException {
		try {
			V3xOrgMember members = orgManager.getMemberById(memberId);
			User usert = new User();
			usert.setId(memberId);
			usert.setLoginAccount(members.getOrgAccountId());
			usert.setAccountId(members.getOrgAccountId());
			usert.setDepartmentId(members.getOrgDepartmentId());
			usert.setName(members.getName());
			usert.setLoginName(members.getLoginName());
			AppContext.putThreadContext("SESSION_CONTEXT_USERINFO_KEY", usert);
			return true;
		}catch (BusinessException e) {
			throw new BusinessException("模拟登录失败");
		}
	}
	
	private static String getDefautName(OrgManager orgManager) {
		if (DEFAUT_NAME == null) {
			try {
				// 获取根单位管理员，多组织为org-admin，单组织为admin1
				// V3xOrgAccount account = orgManager.getRootAccount();
				// TODO
				V3xOrgMember member = orgManager.getGroupAdmin();
				if (member == null) {
					DEFAUT_NAME = "org-admin";
				} else {
					DEFAUT_NAME = member.getLoginName();
				}
			} catch (Exception e) {
				logger.error("根单位管理员失败！" + e);
			}
			// 当前面出现异常时使用都有的组织管理员
			if (Strings.isBlank(DEFAUT_NAME)) {
				DEFAUT_NAME = "system";
			}
		}
		return DEFAUT_NAME;
	}

	// /**
	// * 压缩数据
	// *
	// * @param object
	// * @return
	// * @throws IOException
	// */
	// public static byte[] jzlib(byte[] object) {
	// byte[] data = null;
	// try {
	// ByteArrayOutputStream out = new ByteArrayOutputStream();
	// ZOutputStream zOut = new ZOutputStream(out,
	// JZlib.Z_DEFAULT_COMPRESSION);
	// DataOutputStream objOut = new DataOutputStream(zOut);
	// objOut.write(object);
	// objOut.flush();
	// zOut.close();
	// data = out.toByteArray();
	// out.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return data;
	// }
	// /**
	// * 解压被压缩的数据
	// *
	// * @param object
	// * @return
	// * @throws IOException
	// */
	// public static byte[] unjzlib(byte[] object) {
	// byte[] data = null;
	// try {
	// ByteArrayInputStream in = new ByteArrayInputStream(object);
	// ZInputStream zIn = new ZInputStream(in);
	// byte[] buf = new byte[1024];
	// int num = -1;
	// ByteArrayOutputStream baos = new ByteArrayOutputStream();
	// while ((num = zIn.read(buf, 0, buf.length)) != -1) {
	// baos.write(buf, 0, num);
	// }
	// data = baos.toByteArray();
	// baos.flush();
	// baos.close();
	// zIn.close();
	// in.close();
	//
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return data;
	// }
	public static String getOptUser() throws BusinessException {

		OrgManager orgDao = (OrgManager) AppContext.getBean("orgManager");
		User user = CurrentUser.get();
		String optUser = "";
		if (user != null) {
			V3xOrgMember orgMember = orgDao.getMemberById(user.getId());
			if (orgMember != null) {
				String str = AppContext.getSystemProperty("ocip.sysCode");
				optUser = orgMember.getName() + "(" + str + ")";
			}
		} else {

			return AppContext.getSystemProperty("ocip.sysCode");
		}
		return optUser;

	}

	/***
	 * 压缩BZip2
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] bZip2(byte[] data) {
		byte[] b = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			CBZip2OutputStream bzip2 = new CBZip2OutputStream(bos);
			bzip2.write(data);
			bzip2.flush();
			bzip2.close();
			b = bos.toByteArray();
			bos.close();
		} catch (Exception ex) {
			logger.error("压缩失败！", ex);
			return null;
		}
		return b;
	}

	/***
	 * 解压BZip2
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] unBZip2(byte[] data) {
		byte[] b = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			CBZip2InputStream bzip2 = new CBZip2InputStream(bis);
			byte[] buf = new byte[1024];
			int num = -1;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((num = bzip2.read(buf, 0, buf.length)) != -1) {
				baos.write(buf, 0, num);
			}
			b = baos.toByteArray();
			baos.flush();
			baos.close();
			bzip2.close();
			bis.close();
		} catch (Exception ex) {
			logger.error("解压失败！", ex);
			return null;
		}
		return b;
	}
	/**
	 * 设置当前用户对象，用于模拟用户登录
	 * 
	 * @param user 用户对象
	 * @author renx 2018-2-1
	 */
	public static void setCurrentUser(User user) {
		AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY, user);
		Object request = AppContext.getThreadContext(GlobalNames.THREAD_CONTEXT_REQUEST_KEY);
		Object session = AppContext.getThreadContext(GlobalNames.THREAD_CONTEXT_SESSION_KEY);
		if (request instanceof HttpServletRequest) {
			if (session == null) {
				AppContext.putThreadContext(GlobalNames.THREAD_CONTEXT_SESSION_KEY, ((HttpServletRequest) request).getSession(true));
			}
		}else {
			AppContext.putThreadContext(GlobalNames.THREAD_CONTEXT_REQUEST_KEY, new MockHttpServletRequest());
			if (session == null) {
				AppContext.putThreadContext(GlobalNames.THREAD_CONTEXT_SESSION_KEY, new MockHttpSession());
			}
		}
		AppContext.putSessionContext(Constants.SESSION_CURRENT_USER, user);
	}
}
