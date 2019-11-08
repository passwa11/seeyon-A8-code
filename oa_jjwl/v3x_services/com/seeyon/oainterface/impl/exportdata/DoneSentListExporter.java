package com.seeyon.oainterface.impl.exportdata;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.authenticate.sso.SSOTicketManager;
import com.seeyon.ctp.common.content.affair.AffairUtil;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.LocaleContext;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.portal.sso.SSOTicketBean;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.oainterface.exportData.affair.AffairExport;
import com.seeyon.v3x.services.ServiceException;

/**
 * 参考PendingListExporter
 * 已办列表
 * */
public class DoneSentListExporter {

	private static Log log = LogFactory.getLog(DoneSentListExporter.class);
	private static OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");

	public String getPendingList(String ticket, int firstNum, int pageSize,String subject,String sender,String sendtime,String revietime) throws ServiceException{
		if(null == ticket || "".equals(ticket)){
			return "";
		}
		AffairExport pendExport = null;

		try{
			FlipInfo flipInfo = new FlipInfo();
		    boolean flag = initInfo(ticket, firstNum, pageSize, flipInfo, "receive_time");
		    if (flag) {
		    	StringBuilder hql = new StringBuilder("from " + CtpAffair.class.getName() + " as affair where affair.receiveTime is not null  ");
		    	Map map = new HashMap();

			    hql.append(" and affair.memberId=:memberId  ");
			    map.put("memberId", AppContext.getCurrentUser().getId());
			    hql.append(" and affair.state in (:state)  ");
			    map.put("state", Integer.valueOf(StateEnum.col_pending.getKey()));
			    hql.append(" and affair.delete = :delete  ");
			    map.put("delete", Boolean.valueOf(false));

			    if(null != subject && !"".equals(subject)){
			    	hql.append(" and affair.subject like :subject  ");
				    map.put("subject", "%"+subject+"%");
			    }
			    if(null != sender && !"".equals(sender)){
			    	hql.append(" and affair.senderId in (select m.id from OrgMember m where m.name like :membername)  ");
				    map.put("membername", "%"+sender+"%");
			    }
			    if(null != sendtime && !"".equals(sendtime)){
			    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			    	String[] times = sendtime.split(",");
			    	hql.append(" and affair.createDate >= :sendtime1  ");
				    map.put("sendtime1", sdf.parse(times[0]+" 00:00:00"));
				    if(times.length==2){
				    	hql.append(" and affair.createDate <= :sendtime2  ");
					    map.put("sendtime2", sdf.parse(times[1]+" 23:59:59"));
				    }
			    }
			    if(null != revietime && !"".equals(revietime)){
			    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			    	String[] times = revietime.split(",");
			    	hql.append(" and affair.receiveTime >= :dotime1  ");
				    map.put("dotime1", sdf.parse(times[0]+" 00:00:00"));
				    if(times.length==2){
				    	hql.append(" and affair.receiveTime <= :dotime2  ");
					    map.put("dotime2", sdf.parse(times[1]+" 23:59:59"));
				    }
			    }

			    hql.append(" order by affair.receiveTime desc ");

			    List<CtpAffair> list = DBAgent.find(hql.toString(), map, flipInfo);

			    if(null != list && list.size()>0){
			    	int size = list.size();
			    	if(size > firstNum){
			    		List<CtpAffair> temp = list.subList(firstNum, size);
			    		pendExport = new AffairExport();
			    		for (CtpAffair affair : temp){
			    			if (affair.getTransactorId() == null){
			    				affairToExport(ticket, pendExport, affair);
			    			}
			    		}
			    	}
			    }
		    }
		}catch(Exception e){
			log.error(e.getMessage(), e);
		    throw new ServiceException(e.getMessage());
		}

		if(null == pendExport){
			return "";
		}else{
            try {
                StringBuilder hqlTotal = new StringBuilder("from " + CtpAffair.class.getName() + " as affair where affair.receiveTime is not null  ");
                Map mapTotal=new HashMap();
                hqlTotal.append(" and affair.memberId=:memberId  ");
                mapTotal.put("memberId", AppContext.getCurrentUser().getId());
                hqlTotal.append(" and affair.state in (:state)  ");
                mapTotal.put("state", Integer.valueOf(StateEnum.col_pending.getKey()));
                hqlTotal.append(" and affair.delete = :delete  ");
                mapTotal.put("delete", Boolean.valueOf(false));
                List<CtpAffair> listTotal = DBAgent.find(hqlTotal.toString(), mapTotal);
                pendExport.setTotal(listTotal.size());
            } catch (Exception e) {
                System.out.println("获取代办总数出错了："+e.getMessage());

                e.printStackTrace();
            }
            return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + pendExport.toXML();
		}
	}

	public String getDoneList(String ticket, int firstNum, int pageSize,String subject,String sender,String sendtime,String dotime) throws ServiceException{

		if(null == ticket || "".equals(ticket)){
			return "";
		}
		AffairExport doneExport = null;

		try{
			FlipInfo flipInfo = new FlipInfo();
		    boolean flag = initInfo(ticket, firstNum, pageSize, flipInfo, "complete_time");
		    if (flag) {
		    	StringBuilder hql = new StringBuilder("from " + CtpAffair.class.getName() + " as affair where affair.completeTime is not null  ");
		    	Map map = new HashMap();

			    hql.append(" and affair.memberId=:memberId  ");
			    map.put("memberId", AppContext.getCurrentUser().getId());
			    hql.append(" and affair.state in (:state)  ");
			    map.put("state", Integer.valueOf(StateEnum.col_done.getKey()));
			    hql.append(" and affair.delete = :delete  ");
			    map.put("delete", Boolean.valueOf(false));

			    if(null != subject && !"".equals(subject)){
			    	hql.append(" and affair.subject like :subject  ");
				    map.put("subject", "%"+subject+"%");
			    }
			    if(null != sender && !"".equals(sender)){
			    	hql.append(" and affair.senderId in (select m.id from OrgMember m where m.name like :membername)  ");
				    map.put("membername", "%"+sender+"%");
			    }
			    if(null != sendtime && !"".equals(sendtime)){
			    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			    	String[] times = sendtime.split(",");
			    	hql.append(" and affair.createDate >= :sendtime1  ");
				    map.put("sendtime1", sdf.parse(times[0]+" 00:00:00"));
				    if(times.length==2){
				    	hql.append(" and affair.createDate <= :sendtime2  ");
					    map.put("sendtime2", sdf.parse(times[1]+" 23:59:59"));
				    }
			    }
			    if(null != dotime && !"".equals(dotime)){
			    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			    	String[] times = dotime.split(",");
			    	hql.append(" and affair.completeTime >= :dotime1  ");
				    map.put("dotime1", sdf.parse(times[0]+" 00:00:00"));
				    if(times.length==2){
				    	hql.append(" and affair.completeTime <= :dotime2  ");
					    map.put("dotime2", sdf.parse(times[1]+" 23:59:59"));
				    }
			    }

			    hql.append(" order by affair.completeTime desc ");

			    List<CtpAffair> list = DBAgent.find(hql.toString(), map, flipInfo);

			    if(null != list && list.size()>0){
			    	int size = list.size();
			    	if(size > firstNum){
			    		List<CtpAffair> temp = list.subList(firstNum, size);
			    		doneExport = new AffairExport();
			    		for (CtpAffair affair : temp){
			    			if (affair.getTransactorId() == null){
			    				affairToExport(ticket, doneExport, affair);
			    			}
			    		}
			    	}
			    }
		    }
		}catch(Exception e){
			log.error(e.getMessage(), e);
		    throw new ServiceException(e.getMessage());
		}

		if(null == doneExport){
			return "";
		}else{
            try {
                StringBuilder hqlTotal = new StringBuilder("from " + CtpAffair.class.getName() + " as affair where affair.completeTime is not null  ");
                Map maptotal=new HashMap();
                hqlTotal.append(" and affair.memberId=:memberId  ");
                maptotal.put("memberId", AppContext.getCurrentUser().getId());
                hqlTotal.append(" and affair.state in (:state)  ");
                maptotal.put("state", Integer.valueOf(StateEnum.col_done.getKey()));
                hqlTotal.append(" and affair.delete = :delete  ");
                maptotal.put("delete", Boolean.valueOf(false));
                List<CtpAffair> listTotal = DBAgent.find(hqlTotal.toString(), maptotal);
                doneExport.setTotal(listTotal.size());
            } catch (Exception e) {
                System.out.println("获取已办总数出错了："+e.getMessage());
                e.printStackTrace();
            }
            return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + doneExport.toXML();
		}
	}

	public String getSentList(String ticket, int firstNum, int pageSize,String subject,String revier,String sendtime) throws ServiceException{

		if(null == ticket || "".equals(ticket)){
			return "";
		}
		AffairExport sentExport = null;

		try{
			FlipInfo flipInfo = new FlipInfo();
		    boolean flag = initInfo(ticket, firstNum, pageSize, flipInfo, "create_date");
		    if (flag) {
		    	StringBuilder hql = new StringBuilder("from " + CtpAffair.class.getName() + " as affair where affair.createDate is not null  ");
		    	Map map = new HashMap();

			    hql.append(" and affair.memberId=:memberId  ");
			    map.put("memberId", AppContext.getCurrentUser().getId());
			    hql.append(" and affair.state in (:state)  ");
			    map.put("state", Integer.valueOf(StateEnum.col_sent.getKey()));
			    hql.append(" and affair.delete = :delete  ");
			    map.put("delete", Boolean.valueOf(false));

			    if(null != subject && !"".equals(subject)){
			    	hql.append(" and affair.subject like :subject  ");
				    map.put("subject", "%"+subject+"%");
			    }
			    if(null != revier && !"".equals(revier)){
			    	hql.append(" and affair.objectId in (select distinct c.objectId from CtpAffair c where c.memberId in (select m.id from OrgMember m where m.name like :membername)) ");
				    map.put("membername", "%"+revier+"%");
			    }
			    if(null != sendtime && !"".equals(sendtime)){
			    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			    	String[] times = sendtime.split(",");
			    	hql.append(" and affair.createDate >= :sendtime1  ");
				    map.put("sendtime1", sdf.parse(times[0]+" 00:00:00"));
				    if(times.length==2){
				    	hql.append(" and affair.createDate <= :sendtime2  ");
					    map.put("sendtime2", sdf.parse(times[1]+" 23:59:59"));
				    }
			    }

			    hql.append(" order by affair.createDate desc ");

			    List<CtpAffair> list = DBAgent.find(hql.toString(), map, flipInfo);

			    if(null != list && list.size()>0){
			    	int size = list.size();
			    	if(size > firstNum){
			    		List<CtpAffair> temp = list.subList(firstNum, size);
			    		sentExport = new AffairExport();
			    		for (CtpAffair affair : temp){
			    			if (affair.getTransactorId() == null){
			    				affairToExport(ticket, sentExport, affair);
			    			}
			    		}
			    	}
			    }
		    }
		}catch(Exception e){
			log.error(e.getMessage(), e);
		    throw new ServiceException(e.getMessage());
		}

		if(null == sentExport){
			return "";
		}else{
            try {
                StringBuilder hqlTotal = new StringBuilder("from " + CtpAffair.class.getName() + " as affair where affair.createDate is not null  ");
                Map maptotal=new HashMap();
                hqlTotal.append(" and affair.memberId=:memberId  ");
                maptotal.put("memberId", AppContext.getCurrentUser().getId());
                hqlTotal.append(" and affair.state in (:state)  ");
                maptotal.put("state", Integer.valueOf(StateEnum.col_sent.getKey()));
                hqlTotal.append(" and affair.delete = :delete  ");
                maptotal.put("delete", Boolean.valueOf(false));
                List<CtpAffair> list = DBAgent.find(hqlTotal.toString(), maptotal);
                sentExport.setTotal(list.size());
            } catch (Exception e) {
                System.out.println("获取已发总数出错了："+e.getMessage());

                e.printStackTrace();
            }
            return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + sentExport.toXML();
		}
	}

	private boolean initInfo(String ticket, int firstNum, int pageSize, FlipInfo info,String sort) throws BusinessException{

	    info.setSize(firstNum + pageSize);
	    if ((pageSize <= 0) || (firstNum < 0)) {
	      log.warn("请输入合理的firstNum或pageSize");
	      return false;
	    }
	    info.setPage(1);
	    info.setNeedTotal(false);
	    info.setSortField(sort);
	    info.setSortOrder("desc");
	    SSOTicketManager.TicketInfo ticketInfo = SSOTicketBean.getTicketInfo(ticket);
	    String loginName = "";
	    User user = new User();
	    if (ticketInfo == null){
	    	loginName = ticket;
	    }else{
	    	loginName = ticketInfo.getUsername();
	    }

	    V3xOrgMember m = orgManager.getMemberByLoginName(loginName);
	    if (m == null) {
	    	log.warn("ticket 无效");
	    	return false;
	    }
	    user.setLoginAccount(m.getOrgAccountId());
	    user.setId(m.getId());

	    user.setLocale((Locale)LocaleContext.getAllLocales().get(0));
	    AppContext.putThreadContext("SESSION_CONTEXT_USERINFO_KEY", user);
	    return true;
	}

	private void affairToExport(String ticket, AffairExport pendingExport, CtpAffair affair) throws BusinessException{

	    AffairExport.Export export = pendingExport.addAffair();
	    export.setId(affair.getId());
	    if (affair.getObjectId() != null) {
	    	export.setObjectId(affair.getObjectId());
	    }

	    int app = affair.getApp().intValue();
	    export.setApplicationCategoryKey(affair.getApp().intValue());
	    String forwardMember = affair.getForwardMember();
	    Integer resentTime = affair.getResentTime();
	    V3xOrgMember member = orgManager.getMemberById(affair.getSenderId());
	    if (member == null) {
	    	log.error("事件" + affair.getSubject() + "找不到发送人！");
	    	throw new BusinessException();
	    }
	    String subject = ColUtil.mergeSubjectWithForwardMembers(affair.getSubject(), -1, forwardMember, resentTime, null);
	    export.setSubject(subject);
	    export.setCreateDate(affair.getCreateDate().getTime());
	    export.setCreateMemberName(member.getName());
	    export.setHasAttachments(AffairUtil.isHasAttachments(affair));
	    if (affair.getImportantLevel() != null) {
	    	export.setImportantLevel(affair.getImportantLevel().intValue());
	    }
	    export.setBodyType(affair.getBodyType());
	    Boolean isOverTime = affair.isCoverTime();
	    export.setDistinct(isOverTime.booleanValue());
	    // 连接不处理
//	    export.setLink(SSOTicketBean.makeURLOfSSOTicket(ticket, getPath(affair, app)));
	    export.setLink("");
	}

}
