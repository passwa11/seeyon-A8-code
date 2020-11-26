package com.seeyon.oainterface.impl.exportdata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.authenticate.sso.SSOTicketManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.LocaleContext;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.portal.sso.SSOTicketBean;
import com.seeyon.ctp.services.ServiceException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.oainterface.exportData.affair.AffairExport;
/**
 * @author shuYang
 * @version 2013-06-07
 *  跟踪事项接口，导出xml格式数据
 */
public class TrackListExporter
{
	private static OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
	private static Log  log = LogFactory.getLog(PendingListExporter.class);
	private static final String xmlTitle = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n";
	/**
	 * 
	 * @param ticket 可以是单点登录的ticket，也可以是登录名。
	 * @param firstNum
	 * @param pageSize
	 * @return
	 * @throws ServiceException
	 */
	public AffairExport getTracks(String ticket,int firstNum,int pageSize) throws ServiceException
	{
		AffairExport trackExport = null;
		try {
			FlipInfo info = new FlipInfo();
			if(firstNum < 0 || pageSize<=0){
				log.warn("请输入合理的firstNum或pageSize！");
				return trackExport;
			}
			info.setSize(pageSize);
			info.setPage(firstNum/pageSize+1);
			if(info.getPage()==0){
				info.setPage(1);
			}
			info.setNeedTotal(false);
			info.setSortField("receive_time");
			info.setSortOrder("desc");
			SSOTicketManager.TicketInfo ticketInfo=SSOTicketBean.getTicketInfo(ticket);
			String loginName ="";
			User user = new User();
			if(ticketInfo==null)
				loginName = ticket;
			else
				loginName = ticketInfo.getUsername();
			V3xOrgMember m = orgManager.getMemberByLoginName(loginName);
			if(m==null){
				log.warn("ticket 无效");
				return null;
			}
			user.setLoginAccount(m.getOrgAccountId());
			user.setId(m.getId());
			
			user.setLocale(LocaleContext.getAllLocales().get(0));
			AppContext.putThreadContext(com.seeyon.ctp.common.GlobalNames.SESSION_CONTEXT_USERINFO_KEY, user);
			List<CtpAffair> list  = this.getAllTrackList(info, m.getId());
			
			trackExport = new AffairExport();
			if(list!=null && list.size()>0){
				this.SummaryListToExportList(ticket, trackExport, list);
			}
		}
		catch (Exception e) {
			log.error(e.getMessage(),e);
		}		
		return trackExport;
	}
	
	/**
	 * 获取所有类型的跟踪事项（跟踪事项包括已发和已办）
	 * @param flipInfo
	 * @param memberId
	 * @return
	 * @throws BusinessException
	 */
	private List<CtpAffair> getAllTrackList(FlipInfo flipInfo, Long memberId) throws BusinessException {
		Map<String, Object> params = new HashMap<String, Object>();
    	params.put("memberId", memberId);
    	params.put("state", new Integer[]{StateEnum.col_done.getKey(),StateEnum.col_sent.getKey()});
    	params.put("delete", false);
    	params.put("finish", false);
    	params.put("track", new Integer[]{1,2});
    	StringBuilder hql = new StringBuilder("from " + CtpAffair.class.getName() + " as affair where 1=1");
    	hql.append(" and affair.memberId =:memberId");
    	hql.append(" and affair.delete = :delete  ");
    	hql.append(" and affair.finish = :finish ");
    	hql.append(" and affair.state in (:state)  ");
    	hql.append(" and affair.track in (:track) ");
    	hql.append(" order by affair.receiveTime asc");
    	List<CtpAffair> list = DBAgent.find(hql.toString(), params, flipInfo);
    	return list;
	}
	private void SummaryListToExportList(String ticket,
			AffairExport trackExport, List<CtpAffair> list)
			throws BusinessException {
		if(list!=null){
			for(CtpAffair affair : list){
				this.affairToExport(ticket, trackExport, affair);
			}
		}
	}
	/**
	 * 根据summary组织跟踪export
	 * @param ticket
	 * @param pendingExport
	 * @param summary
	 * @throws BusinessException
	 */
	private void affairToExport(String ticket, AffairExport trackExport,
			CtpAffair affair) throws BusinessException {
//		if(!this.affairIsExist(trackExport,affair.getObjectId())){
			AffairExport.Export export=trackExport.addAffair();
			export.setId(affair.getId());
			if(affair.getObjectId()!=null){
				export.setObjectId(affair.getObjectId());
			}
			
			int app=affair.getApp();
			export.setApplicationCategoryKey(affair.getApp());
			String forwardMember = affair.getForwardMember();
			Integer resentTime = affair.getResentTime();
			V3xOrgMember member = orgManager.getMemberById(affair.getSenderId());
			if(member==null){
				log.error("事件"+affair.getSubject()+"找不到发送人！");
				throw new BusinessException();
			}
			String subject = ColUtil.mergeSubjectWithForwardMembers(affair.getSubject(),-1,forwardMember, resentTime, null);
			export.setSubject(subject);
			export.setCreateDate(affair.getCreateDate().getTime());
			
			export.setCreateMemberName(member.getName());
			export.setHasAttachments(AffairUtil.isHasAttachments(affair));
			if(affair.getImportantLevel()!=null){
				export.setImportantLevel(affair.getImportantLevel());
			}
			export.setBodyType(affair.getBodyType());
			Boolean isOverTime = affair.isCoverTime();
			export.setDistinct(isOverTime);
			export.setLink(SSOTicketBean.makeURLOfSSOTicket(ticket, getPath(affair,app)));
//		}
	}
//	/**
//	 * 通过主应用id，判断跟踪的事件列表中是否已存在该事件，
//	 * @param trackExport
//	 * @param objectId（主应用id）
//	 * @return boolean 表示跟踪的事件列表中是否已存在该事件
//	 */
//	private boolean affairIsExist(AffairExport trackExport, Long objectId) {
//		List<Long> objectIdList = trackExport.getObjectIdList();
//		if(objectIdList==null){
//			objectIdList = new ArrayList<Long>();
//		}
//		if(objectIdList.contains(objectId)){
//			return true;
//		}else{
//			objectIdList.add(objectId);
//		}
//		return false;
//	}
	public String getTrackList(String ticket,int firstNum,int pageSize) throws ServiceException
	{
		if(ticket==null||"".equals(ticket))
		{
			return "";
		}
		AffairExport trackExport= this.getTracks(ticket, firstNum, pageSize);
		if(trackExport==null) return "";
		return xmlTitle+trackExport.toXML();
	}
	private String getPath(CtpAffair affair,int app)
	{
	    String url="";
		ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(app);
		String from = null;
		switch (appEnum) {
		case collaboration :
			if(affair.getState()==2){
				from = "listSent";
			}else if(affair.getState()==4){
				from = "listDone";
			}else from = "listDone";
			url="/collaboration/collaboration.do?method=summary&openFrom=" + from + "&affairId=" + affair.getId();
			break;
		case edocSend:
		case edocRec:
		case edocSign: 
			url = "/edocController.do?method=summary&summaryId="+affair.getObjectId()+"&affairId="+affair.getId()+"&from=sended&docResId=&openFrom=&lenPotent=&docId=&isLibOwner=&docResId=&bodyType=OfficeWord&recType=&relSends=&relRecs=&sendSummaryId=&recEdocId=&forwardType=&archiveModifyId=&isOpenFrom=";
			break;
		}
		return url;
	}
}
