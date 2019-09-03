//package com.seeyon.ctp.rest.util;
//
//import com.seeyon.ctp.common.AppContext;
//import com.seeyon.ctp.common.authenticate.sso.SSOTicketManager.TicketInfo;
//import com.seeyon.ctp.common.exceptions.BusinessException;
//import com.seeyon.ctp.organization.bo.V3xOrgMember;
//import com.seeyon.ctp.organization.manager.OrgManager;
//import com.seeyon.ctp.portal.sso.SSOTicketBean;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//public class TicketUtil {
//	private static final Log LOG = LogFactory.getLog(TicketUtil.class);
//
//	private static OrgManager orgManager;
//
//	public static OrgManager getOrgManager() {
//		if (orgManager == null) {
//			orgManager = (OrgManager) AppContext.getBean("orgManager");
//		}
//		return orgManager;
//	}
//
//	public static Long getMemberIdFromTicket(String ticket)
//			throws BusinessException {
//		TicketInfo ticketInfo = SSOTicketBean.getTicketInfo(ticket);
//		if (ticketInfo == null) {
//			V3xOrgMember m;
//			try {
//				m = getOrgManager().getMemberByLoginName(ticket);
//			} catch (BusinessException e) {
//				LOG.error(e.getMessage());
//				throw e;
//			}
//			if (m == null) {
//				throw new BusinessException("ticket 无效：" + ticket);
//			}
//			return m.getId();
//		} else {
//			return ticketInfo.getMemberId();
//		}
//	}
//}
