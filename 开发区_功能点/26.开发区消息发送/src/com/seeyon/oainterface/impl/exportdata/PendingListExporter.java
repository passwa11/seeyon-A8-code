package com.seeyon.oainterface.impl.exportdata;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.enums.AffairExtPropEnums;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.authenticate.sso.SSOTicketManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.LocaleContext;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.security.SecurityHelper;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.portal.sso.SSOTicketBean;
import com.seeyon.ctp.portal.util.Constants.SpaceType;
import com.seeyon.ctp.services.ServiceException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.oainterface.exportData.affair.AffairExport;
/**
 * @author 舒杨
 * @version 2013-06-03
 * 跟踪事项接口，导出xml格式数据
 */
public class PendingListExporter {
    private static Log  log = LogFactory.getLog(PendingListExporter.class);
    private static OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
    private static AffairManager affairManager = (AffairManager) AppContext.getBean("affairManager");
    private static final String xmlTitle = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n";
    
    public AffairExport getPendings(String ticket,int firstNum,int pageSize) throws ServiceException
    {
        AffairExport pendingExport=null;
        try {
            FlipInfo info = new FlipInfo();
            boolean flag = this.initInfo(ticket, firstNum, pageSize, info);
            if(!flag){
                return null;
            }
            List<CtpAffair> list = this.getAllPendingList(info, AppContext.getCurrentUser().getId());
            if(list!=null && list.size()>0){
                int size = list.size();
                if(size > firstNum){
                    List<CtpAffair> temp = list.subList(firstNum, size);
                    pendingExport= new AffairExport();
                    for(CtpAffair affair : temp){
                        if(affair.getTransactorId()==null){
                            this.affairToExport(ticket, pendingExport, affair);
                        }
                    }
                } else {
                    return null;
                }
            }
        } catch (BusinessException e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(-1,e.getMessage());
        }       
        return pendingExport;
    }
    /**
     * 组织分页类FlipInfo
     * @param ticket
     * @param firstNum
     * @param pageSize
     * @param info
     * @throws BusinessException
     */
    private boolean initInfo(String ticket, int firstNum, int pageSize,
            FlipInfo info) throws BusinessException {
        info.setSize(firstNum + pageSize);
        if(pageSize<=0 || firstNum<0){
            log.warn("请输入合理的firstNum或pageSize");
            return false;
        }
        info.setPage(1);
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
            return false;
        }
        user.setLoginAccount(m.getOrgAccountId());
        user.setId(m.getId());
        
        user.setLocale(LocaleContext.getAllLocales().get(0));
        AppContext.putThreadContext(com.seeyon.ctp.common.GlobalNames.SESSION_CONTEXT_USERINFO_KEY, user);
        return true;
    }
    /**
     * 根据summary组织待办export
     * @param ticket
     * @param pendingExport
     * @param summary
     * @throws BusinessException
     */
    private void affairToExport(String ticket, AffairExport pendingExport,
            CtpAffair affair) throws BusinessException {
        AffairExport.Export export=pendingExport.addAffair();
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
    }
    public AffairExport getAgentPendings(String ticket,int firstNum,int pageSize) throws ServiceException
    {
        AffairExport pendingExport=null;
        try {
            FlipInfo info = new FlipInfo();
            boolean flag = this.initInfo(ticket, firstNum, pageSize, info);
            if(!flag){
                return null;
            }
            List<CtpAffair> list = this.getAllAgentPendingList(info, AppContext.getCurrentUser().getId());
            if(list!=null && list.size()>0){
                pendingExport= new AffairExport();
                for(CtpAffair affair : list){
                    this.affairToExport(ticket, pendingExport, affair);
                }
            }
        } catch (BusinessException e) {
            log.error(e.getMessage(),e);
            throw new ServiceException(-1,e.getMessage());
        }       
        return pendingExport;
    }
    
    /**
     * 获取所有类型的待办事项
     * @param memberId
     * @return
     * @throws BusinessException 
     */
    private List<CtpAffair> getAllPendingList(FlipInfo flipInfo,Long memberId) throws BusinessException{
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("memberId", memberId);
        params.put("state", StateEnum.col_pending.getKey());
        params.put("delete", false);
        params.put("receiveTimeAscOrDesc", "desc");
        //List<CtpAffair> list = affairManager.getByConditionsPagination(flipInfo,params);
        List<CtpAffair> list = affairManager.getByConditions(flipInfo,params);
        return list;
    }
    /**
     * 获取所有类型的代理代办事项
     * @param info
     * @param memberId
     * @return
     * @throws BusinessException 
     */
    private List<CtpAffair> getAllAgentPendingList(FlipInfo flipInfo, Long memberId) throws BusinessException {
        Map<String, Object> params = new HashMap<String, Object>();
        StringBuilder hql = new StringBuilder("from " + CtpAffair.class.getName() + " as affair where");
        //查询我所代理的人(我为代理人，我给别人干活)列表
        List<AgentModel> agentModelList = MemberAgentBean.getInstance().getAgentModelList(memberId);
        //查询我为被代理人列表
        List<AgentModel> agentModelToList = MemberAgentBean.getInstance().getAgentModelToList(memberId);
        hql.append(" (");
        hql.append(" 1=0  ");
        if(agentModelList!=null && agentModelList.size()>0){
            int index = 0;
            for(AgentModel agent : agentModelList){
                index++;
                if((agent.isHasCol() || agent.isHasTemplate()) && agent.getStartDate().before(new Date()) && agent.getEndDate().after(new Date())){
                    //有效代理
                }
                else{
                    continue;
                }
                hql.append(" OR (");
                hql.append("affair.memberId=:memId"+index+" AND affair.receiveTime>=:startDate"+index);
                params.put("memId"+index, agent.getAgentToId());
                params.put("startDate"+index, agent.getStartDate());
                hql.append(")");
            }
        }else if(agentModelToList!=null && agentModelToList.size()==1){
            AgentModel agentModel = agentModelToList.get(0);
            hql.append(" OR (");
            hql.append(" affair.receiveTime>=:startDate");
            hql.append(" and affair.receiveTime<=:endDate");
            params.put("startDate", agentModel.getCreateDate());
            params.put("endDate", agentModel.getEndDate());
            hql.append(")");
        }
        hql.append(")");
        
        params.put("state", StateEnum.col_pending.getKey());
        hql.append(" and affair.state = :state ");
        params.put("delete", false);
        hql.append(" and affair.delete = :delete  ");
        
        hql.append(" order by affair.receiveTime desc ");
        //查询的是我为被代理时，我的被代理事项
        List<CtpAffair> list = DBAgent.find(hql.toString(), params, flipInfo);
        return list;
    }
    public String getPendingList(String ticket,int firstNum,int pageSize) throws ServiceException
    {
        if(ticket==null||"".equals(ticket))
        {
            return "";
        }
        AffairExport pendingExport = getPendings(ticket, firstNum, pageSize);
        if(pendingExport==null) return "";
        return xmlTitle+pendingExport.toXML();
    }
    public String getAgentPendingList(String ticket,int firstNum,int pageSize) throws ServiceException
    {
        if(ticket==null||"".equals(ticket))
        {
            return "";
        }
        AffairExport pendingExport = getAgentPendings(ticket, firstNum, pageSize);
        if(pendingExport==null) return "";
        return xmlTitle+pendingExport.toXML();
    }   
    
    private String getPath(CtpAffair affair,int app){
        boolean isGov = (Boolean)SysFlag.is_gov_only.getFlag(); 
        String url="";
        ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(app);
        switch (appEnum) {
        case collaboration :
            url = "/collaboration/collaboration.do?method=summary&openFrom=listPending&affairId=" + affair.getId();
            break;
        case meeting :
            url = "/mtMeeting.do?method=mydetail&id=" + affair.getObjectId() + "&affairId="+affair.getId() + "&state=10";// + com.seeyon.v3x.meeting.util.Constants.DATA_STATE_SEND;
            break;
            //发文
        case edocSend:
        case edocRec:
            //签报
        case edocSign: 
        	//流程BUG 20170531037404 无法处理修正将summary改为detailIFrame
            url = "/edocController.do?method=detailIFrame&summaryId="+affair.getObjectId()+"&affairId="+affair.getId()+"&from=Pending&docResId=&openFrom=&lenPotent=&docId=&isLibOwner=&docResId=&bodyType=OfficeWord&recType=&relSends=&relRecs=&sendSummaryId=&recEdocId=&forwardType=&archiveModifyId=&isOpenFrom=";
            break;
            //待发送
        case exSend:
            url = "/exchangeEdoc.do?method=edit&upAndDown=&id=" + affair.getSubObjectId() +"&modelType=toSend&reSend=&affairId="+affair.getId()+"&fromlist=";
            break;
            //待签收
        case exSign:
            url = "/exchangeEdoc.do?method=receiveDetail&id=" + affair.getSubObjectId() +"&modelType=toReceive&reSend=&affairId="+affair.getId()+"&fromlist=";
            break;
            //待登记
        case edocRegister:
            if(isGov) {
                boolean isEdocRegister = false;
                url = "/edocController.do?method=entryManager&entry=recManager&toFrom=newEdocRegister&edocType="+EdocEnum.edocType.recEdoc.ordinal()+"&exchangeId="+affair.getSubObjectId()+"&edocId="+affair.getObjectId()+"&affairId="+affair.getId();
            } else {
                url = "/edocController.do?method=entryManager&entry=recManager&listType=newEdoc&comm=distribute&recieveId="+affair.getSubObjectId()+"&edocId="+affair.getObjectId()+"&affairId=" + affair.getId();
            }
            break;
        case office:
              String[] officeLinks = getPendingCategoryLinkByOffice(affair);
              url = officeLinks[0];
            break ;
        case bulletin:
            try{
                String[] bulLinks = getPendingCategoryLink(affair);
                url = bulLinks[0];
                break;
            }catch(Exception e){
                //e.printStackTrace();
                log.error(e.getMessage(), e);
            }
            break;
        case news:
            String[] newsLinks = getPendingCategoryLink(affair);
            url = newsLinks[0];
            break;
        case inquiry:
            String[] links = getPendingCategoryLink(affair);
            url = links[0];
            break;
        case meetingroom:
            url = "/meetingroom.do?method=createPerm&openWin=1&id="+affair.getObjectId() ;
            break;
        }
        return url;
    }
    
    
    private static String[]getPendingCategoryLinkByOffice (CtpAffair affair) {
        Integer subApp = affair.getSubApp();
        Long objectId = affair.getObjectId();
        Long affairId = affair.getId();
        String link = "";
        String categoryLink = "";
        if (ApplicationSubCategoryEnum.office_auto.key() == subApp.intValue()) { 
            link ="/office/autoUse.do?method=autoAuditEdit&affairId=" + affair.getId();
            categoryLink = "/office/autoUse.do?method=index&tgt=autoAudit";
        } else if (ApplicationSubCategoryEnum.office_asset.key() == subApp.intValue()){
            link = "/office/assetUse.do?method=assetAuditEdit&operate=audit&affairId=" + affair.getId();
            categoryLink = "/office/assetUse.do?method=index&tgt=assetAudit";
        } else if (ApplicationSubCategoryEnum.office_book.key() == subApp.intValue()) {
            affairId = objectId;
            link = "/office/bookUse.do?method=bookAuditDetail&bookApplyId=" + objectId;
            categoryLink = "/office/bookUse.do?method=index&tgt=bookAudit";
        } else {
            link = "/office/stockUse.do?method=stockAuditEdit&affairId=" + affair.getId();
            categoryLink = "/office/stockUse.do?method=index&tgt=stockAudit";
        }
        link = link + "&v=" + SecurityHelper.func_digest(affairId); 
        return new String[] { link, categoryLink };
    }
    
    private static String[] getPendingCategoryLink(CtpAffair affair) {
        Map<String, Object> extMap=Strings.escapeNULL(AffairUtil.getExtProperty(affair),new HashMap<String, Object>());
        Integer subApp = affair.getSubApp();
        Long objectId = affair.getObjectId();

        String link = null;
        String categoryLink = null;

        ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(affair.getApp());
        Integer spaceType = (Integer) extMap.get(AffairExtPropEnums.spaceType.name());
        Long spaceId = (Long)extMap.get(AffairExtPropEnums.spaceId.name());
        Long typeId = (Long) extMap.get(AffairExtPropEnums.typeId.name());
        
        switch (appEnum) {
        case inquiry:
            if (extMap.isEmpty()) { // 说明是老数据，只有单位、集团调查，并且只是待审核调查
                link = "/inquirybasic.do?method=survey_check&affairId=" + affair.getId() + "&bid=" + objectId;
                categoryLink = "/inquirybasic.do?method=recent_or_check";
                break;
            }
            String group = SpaceType.group.ordinal() == spaceType ? "group" : "";
            String spaceTypes = "";
            String spaceIds = "";
            if (spaceType != SpaceType.group.ordinal() && spaceType != SpaceType.corporation.ordinal() && spaceType != SpaceType.department.ordinal()) {
                spaceTypes = String.valueOf(spaceType);
                spaceIds = String.valueOf(spaceId);
            }
            if (ApplicationSubCategoryEnum.inquiry_audit.key() == subApp.intValue()) { // 调查审核
                link ="/inquiryData.do?method=inquiryView&inquiryId=" + objectId + "&affairId=" + affair.getId() + "&isAuth=true";
                categoryLink ="/inquiryData.do?method=inquiryIAuth" + "&spaceType=" + spaceType + "&spaceId=" + spaceId;
            } else if (ApplicationSubCategoryEnum.inquiry_write.key() == subApp.intValue()) { // 调查填写
                link = "/inquiryData.do?method=inquiryView&inquiryId=" + objectId + "&affairId=" + affair.getId();
                categoryLink ="/inquiryData.do?method=inquiryBoardIndex&boardId=" + typeId +"&spaceType=" + spaceType + "&spaceId=" + spaceId;
            }
            break;
        case bulletin:
            if (extMap.isEmpty()) { // 说明是老数据，只有单位、集团公告
                boolean isGroup = (affair.getSubObjectId().intValue() == 0);//com.seeyon.v3x.bulletin.util.BulConstants.BulTypeSpaceType.group.ordinal());
                link = "/bulData.do?method=audit&id=" + affair.getObjectId();
                categoryLink = "/bulData.do?method=auditListMain&spaceType=" + (isGroup ? 0 : 1) + "&spaceId=&bulTypeId=";
                break;
            }
            link = "/bulData.do?method=bulView&bulId=" + objectId + "&affairId=" + affair.getId() + "&from=myAudit";
            categoryLink = "/bulData.do?method=bulMyInfo&type=3" + "&spaceType=" + spaceType + "&spaceId=" + spaceId;
            break;
        case news:
            if (extMap.isEmpty()) { // 说明是老数据，只有单位、集团新闻
                boolean isGroupNews = (affair.getSubObjectId().intValue() == 0);//com.seeyon.v3x.news.util.Constants.NewsTypeSpaceType.group.ordinal());
                link = "/newsData.do?method=audit&id=" + affair.getObjectId();
                categoryLink = "/newsData.do?method=auditListMain&spaceType=" + (isGroupNews ? 0 : 1) + "&spaceId=&type=";
                break;
            }
            link ="/newsData.do?method=newsView&newsId=" + objectId + "&affairId=" + affair.getId() + "&from=myAudit";
            categoryLink = "/newsData.do?method=newsMyInfo&type=4" + "&spaceType=" + spaceType + "&spaceId=" + spaceId;
            break;
        }

        return new String[] { link, categoryLink };
    }
}
