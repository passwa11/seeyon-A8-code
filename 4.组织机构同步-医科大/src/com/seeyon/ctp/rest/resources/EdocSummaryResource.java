package com.seeyon.ctp.rest.resources;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.seeyon.ctp.rest.util.TicketUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.index.manager.IndexManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.authenticate.sso.SSOTicketManager;
import com.seeyon.ctp.common.authenticate.sso.SSOTicketManager.TicketInfo;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.dao.paginate.Pagination;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.organization.bo.MemberPost;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.portal.sso.SSOTicketBean;
import com.seeyon.ctp.services.ServiceException;
import com.seeyon.ctp.util.CommonTools;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation;
import com.seeyon.oainterface.ExternalUse;
import com.seeyon.oainterface.common.OAInterfaceException;
import com.seeyon.oainterface.exportData.util.IDataExportUtils;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.domain.EdocFormElement;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.exception.EdocMarkHistoryExistException;
import com.seeyon.v3x.edoc.manager.EdocElementManager;
import com.seeyon.v3x.edoc.manager.EdocFormManager;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocListManager;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.manager.EdocMarkDefinitionManager;
import com.seeyon.v3x.edoc.manager.EdocMarkHistoryManager;
import com.seeyon.v3x.edoc.manager.EdocMarkManager;
import com.seeyon.v3x.edoc.util.DataUtil;
import com.seeyon.v3x.edoc.util.SharedWithThreadLocal;
import com.seeyon.v3x.edoc.webmodel.EdocMarkModel;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryModel;
import com.seeyon.v3x.services.document.DocumentService;
import com.seeyon.v3x.services.document.impl.DocumentServiceImpl;


/**
 * 不要在这个里面添加rest接口了， 后续接口请统一放在EdocResource.java
 *
 */
@Path("/edoc")
@Deprecated
public class EdocSummaryResource extends BaseResource{

	private static Log log = LogFactory.getLog(EdocSummaryResource.class);
    private OrgManager                 orgManager                 = (OrgManager) AppContext.getBean("orgManager");
    private EdocListManager            edocListManager            = (EdocListManager) AppContext.getBean("edocListManager");
    private EdocManager                edocManager                = (EdocManager) AppContext.getBean("edocManager");
    private EdocFormManager            edocFormManager            = (EdocFormManager) AppContext.getBean("edocFormManager");
    private AttachmentManager          attachmentManager          = (AttachmentManager) AppContext.getBean("attachmentManager");
    private EdocMarkDefinitionManager  edocMarkDefinitionManager  = (EdocMarkDefinitionManager) AppContext.getBean("edocMarkDefinitionManager");
    private TemplateManager            templateManager            = (TemplateManager) AppContext.getBean("templateManager");
    private EdocMarkHistoryManager     edocMarkHistoryManager     = (EdocMarkHistoryManager) AppContext.getBean("edocMarkHistoryManager");
    private EdocMarkManager            edocMarkManager            = (EdocMarkManager) AppContext.getBean("edocMarkManager");
    private IndexManager               indexManager               = (IndexManager) AppContext.getBean("indexManager");
    private EdocElementManager         elementManager             = (EdocElementManager) AppContext.getBean("edocElementManager");
    private FileManager                fileManager                = (FileManager) AppContext.getBean("fileManager");
	public final static int edocSend = 0;//发文
	public final static int edocRec = 1;//收文
	public final static int edocSign = 2;//签报


	/**
	 * 获取待办发文
	 *
	 * @param ticket
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("receipt/pending")
	@RestInterfaceAnnotation
	public Response getSendPendingList(@QueryParam("ticket") String ticket,
												 @QueryParam("memberId") Long memberId,
												 @QueryParam("pageNo") int pageNo,
												 @QueryParam("pageSize") int pageSize) throws BusinessException {
		User user = getUser(ticket,memberId);
		List<EdocSummaryModel> summarys = null;
		if(Strings.isNotBlank(user.getName())){
			 super.setPagination();
			 Map<String, Object> condition = createCondition(user,EdocSummaryResource.edocSend,StateEnum.col_pending.key(),"listPending");
			 Pagination.setFirstResult((pageNo-1)*pageSize);
             Pagination.setNeedCount(true);
             Pagination.setMaxResults((pageNo)*pageSize);
			 try {
				 summarys = edocListManager.findEdocPendingList(10, condition);
			 } catch (BusinessException e) {
				 log.error("获得待办发文数据报错!",e);
			 }
		}
		 TicketInfo info = SSOTicketBean.getTicketInfo(ticket);
		 if (info != null) {
			 Map<String,Object> dataLink = new HashMap<String,Object>();
			 Map<String,Object> affairLink = new HashMap<String,Object>();
			 if(summarys==null||summarys.size()==0){
				 return ok(dataLink);
			 }
			 for(EdocSummaryModel edoc:summarys){
				 String url = "/edocController.do?method=detailIFrame&affairId="+edoc.getAffairId()+"&from=Pending&docResId=&openFrom=&lenPotent=&docId=&isLibOwner=&docResId=&bodyType=OfficeWord&recType=&relSends=&relRecs=&sendSummaryId=&recEdocId=&forwardType=&archiveModifyId=&isOpenFrom=";
				 affairLink.put(edoc.getAffairId().toString(), SSOTicketBean.makeURLOfSSOTicket(ticket, url));
			 }
			 dataLink.put("urllink", affairLink);
			 dataLink.put("data", summarys);

			 return ok(dataLink);
		 }
		return ok(summarys);
	}

	/**
	 * 获取在办发文
	 *
	 * @param ticket
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("receipt/running")
	@RestInterfaceAnnotation
	public Response getSendZcdbList(@QueryParam("ticket") String ticket,
												 @QueryParam("memberId") Long memberId,
												 @QueryParam("pageNo") int pageNo,
												 @QueryParam("pageSize") int pageSize) throws BusinessException {
		User user = getUser(ticket,memberId);
		List<EdocSummaryModel> summarys = null;
		if(Strings.isNotBlank(user.getName())){
			 super.setPagination();
			 Map<String, Object> condition = createCondition(user,EdocSummaryResource.edocSend,StateEnum.col_pending.key(),"listZcdb");
		     try {
		    	 Pagination.setFirstResult((pageNo-1)*pageSize);
	             Pagination.setNeedCount(true);
	             Pagination.setMaxResults((pageNo)*pageSize);
				 summarys = edocListManager.findEdocPendingList(10, condition);
			 } catch (BusinessException e) {
				 log.error("获得在办发文数据报错!",e);
			 }
		}
		TicketInfo info = SSOTicketBean.getTicketInfo(ticket);
		 if (info != null) {
			 Map<String,Object> dataLink = new HashMap<String,Object>();
			 Map<String,Object> affairLink = new HashMap<String,Object>();
			 if(summarys==null||summarys.size()==0){
				 return ok(dataLink);
			 }
			 for(EdocSummaryModel edoc:summarys){
				 String url = "/edocController.do?method=detailIFrame&affairId="+edoc.getAffairId()+"&from=Pending&docResId=&openFrom=&lenPotent=&docId=&isLibOwner=&docResId=&bodyType=OfficeWord&recType=&relSends=&relRecs=&sendSummaryId=&recEdocId=&forwardType=&archiveModifyId=&isOpenFrom=";
				 affairLink.put(edoc.getAffairId().toString(), SSOTicketBean.makeURLOfSSOTicket(ticket, url));
			 }
			 dataLink.put("urllink", affairLink);
			 dataLink.put("data", summarys);

			 return ok(dataLink);
		 }
		return ok(summarys);
	}


	/**
	 * 获取已发发文
	 *
	 * @param ticket
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("receipt/sent")
	@RestInterfaceAnnotation
	public Response getSendSentList(@QueryParam("ticket") String ticket,
												 @QueryParam("memberId") Long memberId,
												 @QueryParam("pageNo") int pageNo,
												 @QueryParam("pageSize") int pageSize) throws BusinessException {
		User user = getUser(ticket,memberId);
		List<EdocSummaryModel> summarys = null;
		if(Strings.isNotBlank(user.getName())){
			 super.setPagination();
			 Map<String, Object> condition = createCondition(user,EdocSummaryResource.edocSend,StateEnum.col_pending.key(),"listSent");
		     try {
		    	 Pagination.setFirstResult((pageNo-1)*pageSize);
	             Pagination.setNeedCount(true);
	             Pagination.setMaxResults((pageNo)*pageSize);
				 summarys = edocListManager.findEdocPendingList(40, condition);
			 } catch (BusinessException e) {
				 log.error("获得已发发文数据报错!",e);
			 }
		}
		TicketInfo info = SSOTicketBean.getTicketInfo(ticket);
		if (info != null) {
			 Map<String,Object> dataLink = new HashMap<String,Object>();
			 Map<String,Object> affairLink = new HashMap<String,Object>();
			 if(summarys==null||summarys.size()==0){
				 return ok(dataLink);
			 }
			 for(EdocSummaryModel edoc:summarys){
				 String url = "/edocController.do?method=detailIFrame&affairId="+edoc.getAffairId()+"&from=listSent&detailType=listSent&edocType=0&edocId="+edoc.getEdocId();
				 affairLink.put(edoc.getAffairId().toString(), SSOTicketBean.makeURLOfSSOTicket(ticket, url));
			 }
			 dataLink.put("urllink", affairLink);
			 dataLink.put("data", summarys);

			 return ok(dataLink);
		 }
		return ok(summarys);
	}

	@POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/sendByTemplete/{ticket}/{templeteId}")
	@RestInterfaceAnnotation
    public Map<String, Object> sendByTemplete(@PathParam("ticket") String ticket, @PathParam("templeteId") Long templeteId,
            Map<String, Object> param) throws Exception {
        //取得公文发送人的信息
        User user = getUser(ticket,null);
        // 来文登记,更新登记时间，给签收人发送消息
        Long agentToId = null;
        CtpTemplate templete =  templateManager.getCtpTemplate(templeteId);
        EdocSummary edocSummary  = (EdocSummary) XMLCoder.decoder(templete.getSummary());
        edocSummary.setIdIfNew();
        Long formId = edocSummary.getFormId();
        edocSummary.setTempleteId(templeteId);
        String deadlineDatetime = (String) param.get("deadLineDateTime");
        if (Strings.isNotBlank(deadlineDatetime)) {
            edocSummary.setDeadlineDatetime(DateUtil.parse(deadlineDatetime, "yyyy-MM-dd HH:mm"));
        }
        //新建公文页面流程期限这里加一个隐藏域，后台保存的是这里的值，因为如果从模板加载设了流程期限的话，就disabled了，后台就取不到值了
        String deadline2 = (String) param.get("deadline2");
        if (Strings.isNotBlank(deadline2)) {//OA-20265 调用格式模板，发送后报错。
            edocSummary.setDeadline(Long.valueOf(deadline2));
        }
        String advanceRemind2 = (String) param.get("advanceRemind2");
        if (Strings.isNotBlank(advanceRemind2)) {
            edocSummary.setAdvanceRemind(Long.valueOf(advanceRemind2));
        }

        //设置公文类型,党务还是政务的
        String edocGovType = (String) param.get("edocGovType");
        if ("party".equals(edocGovType)) {
            String party = (String) param.get("my:party");
            edocSummary.setParty(party);
        } else if ("administrative".equals(edocGovType)) {
            String administrative = (String) param.get("my:administrative");
            edocSummary.setAdministrative(administrative);
        }

        SharedWithThreadLocal.setCanUse();
        requestToSummary(param, edocSummary, formId);
        if (Strings.isBlank((String) param.get("my:send_unit"))) {
            edocSummary = setEdocDefaultSendInfo(edocSummary, user, "0");
        }
        if (Strings.isBlank((String) param.get("my:send_department"))) {
            edocSummary = setEdocDefaultSendInfo(edocSummary, user, "1");
        }
        //设置summary中的跟踪，因为DataUtil.requestToSummary方法中取跟踪值有点问题，但该方法在处理回退时也被调用了，修改害怕引起其他问题
        String isTrack = (String) param.get("isTrack");
        if (Strings.isNotBlank(isTrack)) {
            edocSummary.setCanTrack(Integer.parseInt(isTrack));
        }

        List<Long> markDefinitionIdList = new ArrayList<Long>();
        EdocMarkModel em = null;
        Calendar cal = Calendar.getInstance();
        String yearNo = String.valueOf(cal.get(Calendar.YEAR));
        if(Strings.isNotBlank(ParamUtil.getString(param, "docMark"))){//第一个公文文号
            edocSummary.setDocMark(ParamUtil.getString(param, "docMark"));
        }

        if (Strings.isNotBlank(ParamUtil.getString(param, "docMark2"))) {//第二个公文文号
            edocSummary.setDocMark2(ParamUtil.getString(param, "docMark2"));
        }

        if (Strings.isNotBlank(ParamUtil.getString(param, "serialNo"))) {//内部文号
            edocSummary.setSerialNo(ParamUtil.getString(param, "serialNo"));
        }

        em = EdocMarkModel.parse(edocSummary.getDocMark());
        if (em != null) {
            markDefinitionIdList.add(em.getMarkDefinitionId());
            EdocMarkDefinition def = edocMarkDefinitionManager.getMarkDefinition(em.getMarkDefinitionId());
            if (def != null) {
                EdocMarkModel model = edocMarkDefinitionManager.markDef2Mode(def, yearNo, null);
                edocMarkDefinitionManager.setEdocMarkCategoryIncrement(em.getMarkDefinitionId());
                edocSummary.setDocMark(model.getMark());
            }
        }

        em = EdocMarkModel.parse(edocSummary.getDocMark2());
        if (em != null) {
            markDefinitionIdList.add(em.getMarkDefinitionId());
            EdocMarkDefinition def = edocMarkDefinitionManager.getMarkDefinition(em.getMarkDefinitionId());
            if (def != null) {
                EdocMarkModel model = edocMarkDefinitionManager.markDef2Mode(def, yearNo, null);
                edocMarkDefinitionManager.setEdocMarkCategoryIncrement(em.getMarkDefinitionId());
                edocSummary.setDocMark2(model.getMark());
            }
        }

        em = EdocMarkModel.parse(edocSummary.getSerialNo());
        if (em != null) {
            markDefinitionIdList.add(em.getMarkDefinitionId());
            EdocMarkDefinition def = edocMarkDefinitionManager.getMarkDefinition(em.getMarkDefinitionId());
            if (def != null) {
                EdocMarkModel model = edocMarkDefinitionManager.markDef2Mode(def, yearNo, null);
                edocMarkDefinitionManager.setEdocMarkCategoryIncrement(em.getMarkDefinitionId());
                edocSummary.setSerialNo(model.getMark());
            }
        }

        if (!markDefinitionIdList.isEmpty()) {
            List<EdocMarkDefinition> markDefinitions = edocMarkDefinitionManager.queryMarkDefinitionListById(markDefinitionIdList);
            //----------性能优化，存入SharedWithThreadLocal
            SharedWithThreadLocal.setMarkDefinition(markDefinitions);
        }
        // 处理公文文号，如果为空，不做任何处理
        String docMark = edocSummary.getDocMark();
        try {
            docMark = registDocMark(edocSummary.getId(), docMark, 1, edocSummary.getEdocType(), false, EdocEnum.MarkType.edocMark.ordinal());
        } catch (EdocMarkHistoryExistException e) {
            log.error("",e);
        }
        if (docMark != null) {
            docMark = docMark.replaceAll(String.valueOf((char) 160), String.valueOf((char) 32));
            edocSummary.setDocMark(docMark);
        }
        //处理第二个公文文号
        docMark = edocSummary.getDocMark2();
        docMark = registDocMark(edocSummary.getId(), docMark, 2, edocSummary.getEdocType(), false, EdocEnum.MarkType.edocMark.ordinal());
        if (docMark != null) {
            docMark = docMark.replaceAll(String.valueOf((char) 160), String.valueOf((char) 32));
            edocSummary.setDocMark2(docMark);
        }
        //内部文号
        String serialNo = edocSummary.getSerialNo();
        serialNo = registDocMark(edocSummary.getId(), serialNo, 3, edocSummary.getEdocType(), false, EdocEnum.MarkType.edocInMark.ordinal());
        if (serialNo != null) {
            serialNo = serialNo.replaceAll(String.valueOf((char) 160), String.valueOf((char) 32));
            edocSummary.setSerialNo(serialNo);
        }

        Map<String, Object> options = new HashMap<String, Object>();
        EdocEnum.SendType sendType = EdocEnum.SendType.normal;
        //是否重复发起
        if (null != param.get("resend") && !"".equals(param.get("resend"))) {
            sendType = EdocEnum.SendType.resend;
        }

        //是否转发
        if (null != param.get("forward") && !"".equals(param.get("forward"))) {
            sendType = EdocEnum.SendType.forward;
            //是否转发意见
            boolean isForwardOpinion = "true".equals(param.get("isForwardOpinion"));
            //转发人附言
            String additionalComment = (String) param.get("additionalComment");
            // 转发人追加的附件
            options.put("isForwardOpinion", isForwardOpinion);
            options.put("additionalComment", additionalComment);
        }

        String note = (String) param.get("note");//发起人附言
        EdocOpinion senderOninion = new EdocOpinion();
        senderOninion.setIdIfNew();
        senderOninion.setContent(note);
        String trackMode = (String) param.get("isTrack");
        boolean track = "1".equals(trackMode);
        //跟踪
        String trackMembers = (String) param.get("trackMembers");
        String trackRange = (String) param.get("trackRange");
        //如果设置了跟踪指定人，但是指定人为空，则把跟踪设置为false
        if (Strings.isNotBlank(trackRange) && "0".endsWith(trackRange) && Strings.isBlank(trackMembers)) {
            track = false;
        }
        senderOninion.affairIsTrack = track;
        senderOninion.setNodeId(0);
        senderOninion.setAttribute(1);
        senderOninion.setIsHidden(false);
        senderOninion.setCreateUserId(user.getId());
        senderOninion.setCreateTime(new Timestamp(System.currentTimeMillis()));
        senderOninion.setPolicy((String) param.get("policy"));
        senderOninion.setOpinionType(EdocOpinion.OpinionType.senderOpinion.ordinal());

        String bodyType = ParamUtil.getString(param, "bodyType");
        EdocBody body = new EdocBody();
        ParamUtil.mapToBean(param, body, false);
        body.setIdIfNew();
        if (!"HTML".equals(bodyType) && Strings.isNotBlank(body.getContent())) {
            Long[] fileIds = { org.apache.commons.lang.math.NumberUtils.toLong(body.getContent()) };
            List<V3XFile> v3xFileList = fileManager.getV3XFile(fileIds);
            if (!Strings.isEmpty(v3xFileList)) {
                body.setCreateTime(new Timestamp(v3xFileList.get(0).getCreateDate().getTime()));
            }
            body.setContentType(bodyType);
        } else {
            body.setContentType(bodyType);
            body.setCreateTime(new Timestamp(System.currentTimeMillis()));
        }
        //-------性能优化，该方法在新建公文单发送，编辑发送，退件箱编辑后发送都会调用，希望在新建公文发送时就不执行删除附件操作了
        String hasSummaryId = (String) param.get("newSummaryId");
        boolean isNewSent = Strings.isBlank(hasSummaryId); //是否新建公文发送

        edocSummary.setState(0);
        edocSummary.setCreateTime(new Timestamp(System.currentTimeMillis()));
        //流程期限具体时间需要做版本控制

        String deadlineTime = (String) param.get("deadlineTime"); //如果选的是流程期限具体时间，在这里进行运算，会更精确。
        Long deadlineValue = getMinValueByDeadlineTime(deadlineTime, edocSummary.getCreateTime());
        if (deadlineValue != -1L) {
            edocSummary.setDeadline(deadlineValue);
        }

        if (edocSummary.getStartTime() == null) {
            edocSummary.setStartTime(new Timestamp(System.currentTimeMillis()));
        }
        edocSummary.setStartUserId(agentToId == null ? user.getId() : agentToId);
        edocSummary.setFormId(formId);
        V3xOrgMember member = orgManager.getEntityById(V3xOrgMember.class, edocSummary.getStartUserId());
        edocSummary.setStartMember(member);
        //如果公文单无登记人，自动赋上登记节为发起人。yangzd
        if (param.get("my:create_person") == null) {
            edocSummary.setCreatePerson(user.getName());
        }
        //yangzd
        if (edocSummary.getOrgAccountId() == null) {
            edocSummary.setOrgAccountId(user.getLoginAccount());
        }
        edocSummary.setOrgDepartmentId(getEdocOwnerDepartmentId(edocSummary.getOrgAccountId(), agentToId));

        if (edocSummary.getEdocType() == 1 && (String) param.get("my:send_unit") != null) {
            edocSummary.setSendUnit((String) param.get("my:send_unit"));
        }

        if (edocSummary.getEdocType() == 1 && param.get("my:send_to") != null) {
            edocSummary.setSendTo((String) param.get("my:send_to"));
        }

        if (body.getCreateTime() == null) {
            body.setCreateTime(new Timestamp(System.currentTimeMillis()));
        }
        body.setLastUpdate(new Timestamp(System.currentTimeMillis()));
        String fileUrlIds = ParamUtil.getString(param, "fileUrlIds", null);
        if (fileUrlIds != null) {
            Long[] fileIds = {};
            if (fileUrlIds.length() > 2) {
                fileIds = CommonTools.parseStr2Ids(fileUrlIds.substring(0, fileUrlIds.length()), ",").toArray(new Long[] {});
            }
            List<V3XFile> v3xFileList = fileManager.getV3XFile(fileIds);
            addAttachments(v3xFileList, edocSummary.getId(), edocSummary.getId());
        }

        boolean isNew = edocSummary.isNew();
        if (!isNew && Strings.isNotBlank(hasSummaryId)) {
            //待发编辑发送时，这里需要设置为之前的id，不然在manager中就不会更新affair了
            edocSummary.setId(Long.parseLong(hasSummaryId));
        }
        Long detailId = null;
        if (!isNewSent) {
            detailId = edocSummary.getId();
        }
        Map<String, String> superviseMap = new HashMap<String, String>();
        superviseMap.put("detailId", detailId == null ? null : String.valueOf(detailId));
        superviseMap.put("supervisorIds", (String) param.get("supervisorId"));
        superviseMap.put("supervisorNames", (String) param.get("supervisors"));
        superviseMap.put("awakeDate", (String) param.get("awakeDate"));
        String title = (String) param.get("title");
        if (Strings.isBlank(title)) {
            title = (String) param.get("superviseTitle");
        }
        superviseMap.put("title", title);
        edocSummary.setSuperviseMap(superviseMap);

        String process_xml = (String) param.get("process_xml");
        String templeteProcessId = String.valueOf(templete.getWorkflowId());
        Long affairId = 0L;
        try {
            affairId = edocManager.runCase(edocSummary, body, senderOninion, sendType, options, "", agentToId, isNewSent, process_xml, null, null, templeteProcessId);
        } catch (Exception e) {
            log.error("发起公文流程异常", e);
        }

        //不跟踪 或者 全部跟踪的时候不向部门跟踪表中添加数据，所以将下面这个参数串设置为空。
        if (!track || "1".equals(trackRange)) {
            trackMembers = "";
        }
        edocManager.setTrack(affairId, track, trackMembers);
        //全文检索入库
        add2Index(edocSummary.getEdocType(), edocSummary.getId());

        //性能优化，删除实例对象
        SharedWithThreadLocal.remove();
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("affairId", affairId);
        return map;
    }


	/**
	 * 获取待发发文
	 *
	 * @param ticket
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("receipt/draft")
	@RestInterfaceAnnotation
	public Response getSendDraftList(@QueryParam("ticket") String ticket,
												 @QueryParam("memberId") Long memberId,
												 @QueryParam("pageNo") int pageNo,
												 @QueryParam("pageSize") int pageSize) throws BusinessException {
		User user = getUser(ticket,memberId);
		List<EdocSummaryModel> summarys = null;
		if(Strings.isNotBlank(user.getName())){
			 super.setPagination();
			 Map<String, Object> condition = createCondition(user,EdocSummaryResource.edocSend,StateEnum.col_pending.key(),"listWaitSend");
		     try {
		    	 Pagination.setFirstResult((pageNo-1)*pageSize);
	             Pagination.setNeedCount(true);
	             Pagination.setMaxResults((pageNo)*pageSize);
				 summarys = edocListManager.findEdocWaitSendList(30, condition);
			 } catch (BusinessException e) {
				 log.error("获得待发发文数据报错!",e);
			 }
		}
		TicketInfo info = SSOTicketBean.getTicketInfo(ticket);
		if (info != null) {
			 Map<String,Object> dataLink = new HashMap<String,Object>();
			 Map<String,Object> affairLink = new HashMap<String,Object>();
			 if(summarys==null||summarys.size()==0){
				 return ok(dataLink);
			 }
			 for(EdocSummaryModel edoc:summarys){
				 String url = "/edocController.do?method=detailIFrame&affairId="+edoc.getAffairId()+"&from=listWaitSend";
				 affairLink.put(edoc.getAffairId().toString(), SSOTicketBean.makeURLOfSSOTicket(ticket, url));
			 }
			 dataLink.put("urllink", affairLink);
			 dataLink.put("data", summarys);

			 return ok(dataLink);
		 }
		return ok(summarys);
	}

	/**
	 * 获取已办发文
	 *
	 * @param ticket
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("receipt/done")
	@RestInterfaceAnnotation
	public Response getSendDoneList(@QueryParam("ticket") String ticket,
												 @QueryParam("memberId") Long memberId,
												 @QueryParam("pageNo") int pageNo,
												 @QueryParam("pageSize") int pageSize) throws BusinessException {
		User user = getUser(ticket,memberId);
		List<EdocSummaryModel> summarys = null;
		if(Strings.isNotBlank(user.getName())){
			 super.setPagination();

			 Map<String, Object> condition = createCondition(user,EdocSummaryResource.edocSend,StateEnum.col_pending.key(),"listDoneAll");
		     try {
		    	 Pagination.setFirstResult((pageNo-1)*pageSize);
	             Pagination.setNeedCount(true);
	             Pagination.setMaxResults((pageNo)*pageSize);
				 summarys = edocListManager.findEdocDoneList(20, condition);
			 } catch (BusinessException e) {
				 log.error("获得已办发文数据报错!",e);
			 }
		}

		TicketInfo info = SSOTicketBean.getTicketInfo(ticket);
		 if (info != null) {
			 Map<String,Object> dataLink = new HashMap<String,Object>();
			 Map<String,Object> affairLink = new HashMap<String,Object>();
			 if(summarys==null||summarys.size()==0){
				 return ok(dataLink);
			 }
			 for(EdocSummaryModel edoc:summarys){
				 String url = "/edocController.do?method=detailIFrame&affairId="+edoc.getAffairId()+"&from=Done";
				 affairLink.put(edoc.getAffairId().toString(), SSOTicketBean.makeURLOfSSOTicket(ticket, url));
			 }
			 dataLink.put("urllink", affairLink);
			 dataLink.put("data", summarys);

			 return ok(dataLink);
		 }
		return ok(summarys);
	}

	//-----------------------------------------以上是发文------------------------------------------------






	/**
	 * 获取待办收文
	 *
	 * @param ticketId
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("dispatch/pending")
	@RestInterfaceAnnotation
	public Response getRecPendingList(@QueryParam("ticket") String ticket,
												 @QueryParam("memberId") Long memberId,
												 @QueryParam("pageNo") int pageNo,
												 @QueryParam("pageSize") int pageSize) throws BusinessException {
		User user = getUser(ticket,memberId);
		List<EdocSummaryModel> summarys = null;
		if(Strings.isNotBlank(user.getName())){
			 super.setPagination();
			 Map<String, Object> condition = createCondition(user,1,StateEnum.col_pending.key(),"listPending");
		     try {
		    	 Pagination.setFirstResult((pageNo-1)*pageSize);
	             Pagination.setNeedCount(true);
	             Pagination.setMaxResults((pageNo)*pageSize);
				 summarys = edocListManager.findEdocPendingList(10, condition);
			 } catch (BusinessException e) {
				 log.error("获得待办收文数据报错!",e);
			 }
		}
		TicketInfo info = SSOTicketBean.getTicketInfo(ticket);
		 if (1==1) {
			 Map<String,Object> dataLink = new HashMap<String,Object>();
			 Map<String,Object> affairLink = new HashMap<String,Object>();
			 if(summarys==null||summarys.size()==0){
				 return ok(dataLink);
			 }
			 for(EdocSummaryModel edoc:summarys){
				 String url = "/edocController.do?method=detailIFrame&affairId="+edoc.getAffairId()+"&from=Pending";
				 affairLink.put(edoc.getAffairId().toString(), SSOTicketBean.makeURLOfSSOTicket(ticket, url));
			 }
			 dataLink.put("urllink", affairLink);
			 dataLink.put("data", summarys);

			 return ok(dataLink);
		 }
		return ok(summarys);
	}


	/**
	 * 获取在办收文
	 *
	 * @param ticket
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("dispatch/running")
	@RestInterfaceAnnotation
	public Response getRecZcdbList(@QueryParam("ticket") String ticket,
												 @QueryParam("memberId") Long memberId,
												 @QueryParam("pageNo") int pageNo,
												 @QueryParam("pageSize") int pageSize) throws BusinessException {
		User user = getUser(ticket,memberId);
		List<EdocSummaryModel> summarys = null;
		if(Strings.isNotBlank(user.getName())){
			 super.setPagination();
			 Map<String, Object> condition = createCondition(user,EdocSummaryResource.edocRec,StateEnum.col_pending.key(),"listZcdb");
		     try {
		    	 Pagination.setFirstResult((pageNo-1)*pageSize);
	             Pagination.setNeedCount(true);
	             Pagination.setMaxResults((pageNo)*pageSize);
				 summarys = edocListManager.findEdocPendingList(10, condition);
			 } catch (BusinessException e) {
				 log.error("获得在办收文数据报错!",e);
			 }
		}
		TicketInfo info = SSOTicketBean.getTicketInfo(ticket);
		 if (info != null) {
			 Map<String,Object> dataLink = new HashMap<String,Object>();
			 Map<String,Object> affairLink = new HashMap<String,Object>();
			 if(summarys==null||summarys.size()==0){
				 return ok(dataLink);
			 }
			 for(EdocSummaryModel edoc:summarys){
				 String url = "/edocController.do?method=detailIFrame&affairId="+edoc.getAffairId()+"&from=Pending";
				 affairLink.put(edoc.getAffairId().toString(), SSOTicketBean.makeURLOfSSOTicket(ticket, url));
			 }
			 dataLink.put("urllink", affairLink);
			 dataLink.put("data", summarys);

			 return ok(dataLink);
		 }
		return ok(summarys);
	}


	/**
	 * 获取已发收文
	 *
	 * @param ticket
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("dispatch/sent")
	@RestInterfaceAnnotation
	public Response getRecSentList(@QueryParam("ticket") String ticket,
												 @QueryParam("memberId") Long memberId,
												 @QueryParam("pageNo") int pageNo,
												 @QueryParam("pageSize") int pageSize) throws BusinessException {
		User user = getUser(ticket,memberId);
		List<EdocSummaryModel> summarys = null;
		if(Strings.isNotBlank(user.getName())){
			 super.setPagination();
			 Map<String, Object> condition = createCondition(user,EdocSummaryResource.edocRec,StateEnum.col_pending.key(),"listSent");
		     try {
		    	 Pagination.setFirstResult((pageNo-1)*pageSize);
	             Pagination.setNeedCount(true);
	             Pagination.setMaxResults((pageNo)*pageSize);
				 summarys = edocListManager.findEdocPendingList(40, condition);
			 } catch (BusinessException e) {
				 log.error("获得已发收文数据报错!",e);
			 }
		}
		TicketInfo info = SSOTicketBean.getTicketInfo(ticket);
		if (info != null) {
			 Map<String,Object> dataLink = new HashMap<String,Object>();
			 Map<String,Object> affairLink = new HashMap<String,Object>();
			 if(summarys==null||summarys.size()==0){
				 return ok(dataLink);
			 }
			 for(EdocSummaryModel edoc:summarys){
				 String url = "/edocController.do?method=detailIFrame&affairId="+edoc.getAffairId()+"&from=listSent&detailType=listSent&edocType=0&edocId="+edoc.getEdocId();
				 affairLink.put(edoc.getAffairId().toString(), SSOTicketBean.makeURLOfSSOTicket(ticket, url));
			 }
			 dataLink.put("urllink", affairLink);
			 dataLink.put("data", summarys);

			 return ok(dataLink);
		}
		return ok(summarys);
	}


	/**
	 * 获取待发收文
	 *
	 * @param ticket
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("dispatch/draft")
	@RestInterfaceAnnotation
	public Response getRecDraftList(@QueryParam("ticket") String ticket,
												 @QueryParam("memberId") Long memberId,
												 @QueryParam("pageNo") int pageNo,
												 @QueryParam("pageSize") int pageSize) throws BusinessException {
		User user = getUser(ticket,memberId);
		List<EdocSummaryModel> summarys = null;
		if(Strings.isNotBlank(user.getName())){
			 super.setPagination();
			 Map<String, Object> condition = createCondition(user,EdocSummaryResource.edocRec,StateEnum.col_pending.key(),"listWaitSend");
		     try {
		    	 Pagination.setFirstResult((pageNo-1)*pageSize);
	             Pagination.setNeedCount(true);
	             Pagination.setMaxResults((pageNo)*pageSize);
				 summarys = edocListManager.findEdocWaitSendList(30, condition);
			 } catch (BusinessException e) {
				 log.error("获得待发收文数据报错!",e);
			 }
		}
		TicketInfo info = SSOTicketBean.getTicketInfo(ticket);
		if (info != null) {
			 Map<String,Object> dataLink = new HashMap<String,Object>();
			 Map<String,Object> affairLink = new HashMap<String,Object>();
			 if(summarys==null||summarys.size()==0){
				 return ok(dataLink);
			 }
			 for(EdocSummaryModel edoc:summarys){
				 String url = "/edocController.do?method=detailIFrame&affairId="+edoc.getAffairId()+"&from=listWaitSend";
				 affairLink.put(edoc.getAffairId().toString(), SSOTicketBean.makeURLOfSSOTicket(ticket, url));
			 }
			 dataLink.put("urllink", affairLink);
			 dataLink.put("data", summarys);

			 return ok(dataLink);
		 }
		return ok(summarys);
	}

	/**
	 * 获取已办收文
	 *
	 * @param ticket
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("dispatch/done")
	@RestInterfaceAnnotation
	public Response getRecDoneList(@QueryParam("ticket") String ticket,
												 @QueryParam("memberId") Long memberId,
												 @QueryParam("pageNo") int pageNo,
												 @QueryParam("pageSize") int pageSize) throws BusinessException {
		User user = getUser(ticket,memberId);
		List<EdocSummaryModel> summarys = null;
		if(Strings.isNotBlank(user.getName())){
			 super.setPagination();

			 Map<String, Object> condition = createCondition(user,EdocSummaryResource.edocRec,StateEnum.col_pending.key(),"listDoneAll");
		     try {
		    	 Pagination.setFirstResult((pageNo-1)*pageSize);
	             Pagination.setNeedCount(true);
	             Pagination.setMaxResults((pageNo)*pageSize);
				 summarys = edocListManager.findEdocDoneList(20, condition);
			 } catch (BusinessException e) {
				 log.error("获得已办收文数据报错!",e);
			 }
		}
		TicketInfo info = SSOTicketBean.getTicketInfo(ticket);
		 if (info != null) {
			 Map<String,Object> dataLink = new HashMap<String,Object>();
			 Map<String,Object> affairLink = new HashMap<String,Object>();
			 if(summarys==null||summarys.size()==0){
				 return ok(dataLink);
			 }
			 for(EdocSummaryModel edoc:summarys){
				 String url = "/edocController.do?method=detailIFrame&affairId="+edoc.getAffairId()+"&from=Done";
				 affairLink.put(edoc.getAffairId().toString(), SSOTicketBean.makeURLOfSSOTicket(ticket, url));
			 }
			 dataLink.put("urllink", affairLink);
			 dataLink.put("data", summarys);

			 return ok(dataLink);
		 }
		return ok(summarys);
	}

	//-------------------------------------------以上是收文-------------------------------------------------



	/**
	 * 获取待办签报
	 *
	 * @param ticketId
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("sign/pending")
	@RestInterfaceAnnotation
	public Response getSignPendingList(@QueryParam("ticket") String ticket,
												 @QueryParam("memberId") Long memberId,
												 @QueryParam("pageNo") int pageNo,
												 @QueryParam("pageSize") int pageSize) throws BusinessException {
		User user = getUser(ticket,memberId);
		List<EdocSummaryModel> summarys = null;
		if(Strings.isNotBlank(user.getName())){
			 super.setPagination();
			 Map<String, Object> condition = createCondition(user,EdocSummaryResource.edocSign,StateEnum.col_pending.key(),"listPending");
		     try {
		    	 Pagination.setFirstResult((pageNo-1)*pageSize);
	             Pagination.setNeedCount(true);
	             Pagination.setMaxResults((pageNo)*pageSize);
				 summarys = edocListManager.findEdocPendingList(10, condition);
			 } catch (BusinessException e) {
				 log.error("获得待办签报数据报错!",e);
			 }
		}
		TicketInfo info = SSOTicketBean.getTicketInfo(ticket);
		 if (info != null) {
			 Map<String,Object> dataLink = new HashMap<String,Object>();
			 Map<String,Object> affairLink = new HashMap<String,Object>();
			 if(summarys==null||summarys.size()==0){
				 return ok(dataLink);
			 }
			 for(EdocSummaryModel edoc:summarys){
				 String url = "/edocController.do?method=detailIFrame&affairId="+edoc.getAffairId()+"&from=Pending";
				 affairLink.put(edoc.getAffairId().toString(), SSOTicketBean.makeURLOfSSOTicket(ticket, url));
			 }
			 dataLink.put("urllink", affairLink);
			 dataLink.put("data", summarys);

			 return ok(dataLink);
		 }
		return ok(summarys);
	}


	/**
	 * 获取在办签报
	 *
	 * @param ticket
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("sign/running")
	@RestInterfaceAnnotation
	public Response getSignZcdbList(@QueryParam("ticket") String ticket,
												 @QueryParam("memberId") Long memberId,
												 @QueryParam("pageNo") int pageNo,
												 @QueryParam("pageSize") int pageSize) throws BusinessException {
		User user = getUser(ticket,memberId);
		List<EdocSummaryModel> summarys = null;
		if(Strings.isNotBlank(user.getName())){
			 super.setPagination();
			 Map<String, Object> condition = createCondition(user,EdocSummaryResource.edocSign,StateEnum.col_pending.key(),"listZcdb");
		     try {
		    	 Pagination.setFirstResult((pageNo-1)*pageSize);
	             Pagination.setNeedCount(true);
	             Pagination.setMaxResults((pageNo)*pageSize);
				 summarys = edocListManager.findEdocPendingList(10, condition);
			 } catch (BusinessException e) {
				 log.error("获得在办签报数据报错!",e);
			 }
		}
		TicketInfo info = SSOTicketBean.getTicketInfo(ticket);
		 if (info != null) {
			 Map<String,Object> dataLink = new HashMap<String,Object>();
			 Map<String,Object> affairLink = new HashMap<String,Object>();
			 if(summarys==null||summarys.size()==0){
				 return ok(dataLink);
			 }
			 for(EdocSummaryModel edoc:summarys){
				 String url = "/edocController.do?method=detailIFrame&affairId="+edoc.getAffairId()+"&from=Pending";
				 affairLink.put(edoc.getAffairId().toString(), SSOTicketBean.makeURLOfSSOTicket(ticket, url));
			 }
			 dataLink.put("urllink", affairLink);
			 dataLink.put("data", summarys);

			 return ok(dataLink);
		 }
		return ok(summarys);
	}


	/**
	 * 获取已发签报
	 *
	 * @param ticket
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("sign/sent")
	@RestInterfaceAnnotation
	public Response getSignSentList(@QueryParam("ticket") String ticket,
												 @QueryParam("memberId") Long memberId,
												 @QueryParam("pageNo") int pageNo,
												 @QueryParam("pageSize") int pageSize) throws BusinessException {
		User user = getUser(ticket,memberId);
		List<EdocSummaryModel> summarys = null;
		if(Strings.isNotBlank(user.getName())){
			 super.setPagination();
			 Map<String, Object> condition = createCondition(user,EdocSummaryResource.edocSign,StateEnum.col_pending.key(),"listSent");
		     try {
		    	 Pagination.setFirstResult((pageNo-1)*pageSize);
	             Pagination.setNeedCount(true);
	             Pagination.setMaxResults((pageNo)*pageSize);
				 summarys = edocListManager.findEdocPendingList(40, condition);
			 } catch (BusinessException e) {
				 log.error("获得已发签报数据报错!",e);
			 }
		}
		TicketInfo info = SSOTicketBean.getTicketInfo(ticket);
		if (info != null) {
			 Map<String,Object> dataLink = new HashMap<String,Object>();
			 Map<String,Object> affairLink = new HashMap<String,Object>();
			 if(summarys==null||summarys.size()==0){
				 return ok(dataLink);
			 }
			 for(EdocSummaryModel edoc:summarys){
				 String url = "/edocController.do?method=detailIFrame&affairId="+edoc.getAffairId()+"&from=listSent&detailType=listSent&edocType=0&edocId="+edoc.getEdocId();
				 affairLink.put(edoc.getAffairId().toString(), SSOTicketBean.makeURLOfSSOTicket(ticket, url));
			 }
			 dataLink.put("urllink", affairLink);
			 dataLink.put("data", summarys);

			 return ok(dataLink);
		 }
		return ok(summarys);
	}


	/**
	 * 获取待发签报
	 *
	 * @param ticket
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("sign/draft")
	@RestInterfaceAnnotation
	public Response getSignDraftList(@QueryParam("ticket") String ticket,
												 @QueryParam("memberId") Long memberId,
												 @QueryParam("pageNo") int pageNo,
												 @QueryParam("pageSize") int pageSize) throws BusinessException {
		User user = getUser(ticket,memberId);
		List<EdocSummaryModel> summarys = null;
		if(Strings.isNotBlank(user.getName())){
			 super.setPagination();
			 Map<String, Object> condition = createCondition(user,EdocSummaryResource.edocSign,StateEnum.col_pending.key(),"listWaitSend");
		     try {
		    	 Pagination.setFirstResult((pageNo-1)*pageSize);
	             Pagination.setNeedCount(true);
	             Pagination.setMaxResults((pageNo)*pageSize);
				 summarys = edocListManager.findEdocWaitSendList(30, condition);
			 } catch (BusinessException e) {
				 log.error("获得待发签报数据报错!",e);
			 }
		}
		TicketInfo info = SSOTicketBean.getTicketInfo(ticket);
		if (info != null) {
			 Map<String,Object> dataLink = new HashMap<String,Object>();
			 Map<String,Object> affairLink = new HashMap<String,Object>();
			 if(summarys==null||summarys.size()==0){
				 return ok(dataLink);
			 }
			 for(EdocSummaryModel edoc:summarys){
				 String url = "/edocController.do?method=detailIFrame&affairId="+edoc.getAffairId()+"&from=listWaitSend";
				 affairLink.put(edoc.getAffairId().toString(), SSOTicketBean.makeURLOfSSOTicket(ticket, url));
			 }
			 dataLink.put("urllink", affairLink);
			 dataLink.put("data", summarys);

			 return ok(dataLink);
		 }
		return ok(summarys);
	}

	/**
	 * 获取已办签报
	 *
	 * @param ticket
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("sign/done")
	@RestInterfaceAnnotation
	public Response getSignDoneList(@QueryParam("ticket") String ticket,
												 @QueryParam("memberId") Long memberId,
												 @QueryParam("pageNo") int pageNo,
												 @QueryParam("pageSize") int pageSize) throws BusinessException {
		User user = getUser(ticket,memberId);
		List<EdocSummaryModel> summarys = null;
		if(Strings.isNotBlank(user.getName())){
			 super.setPagination();

			 Map<String, Object> condition = createCondition(user,EdocSummaryResource.edocSign,StateEnum.col_pending.key(),"listDoneAll");
		     try {
		    	 Pagination.setFirstResult((pageNo-1)*pageSize);
	             Pagination.setNeedCount(true);
	             Pagination.setMaxResults((pageNo)*pageSize);
				 summarys = edocListManager.findEdocDoneList(20, condition);
			 } catch (BusinessException e) {
				 log.error("获得已办签报数据报错!",e);
			 }
		}
		TicketInfo info = SSOTicketBean.getTicketInfo(ticket);
		 if (info != null) {
			 Map<String,Object> dataLink = new HashMap<String,Object>();
			 Map<String,Object> affairLink = new HashMap<String,Object>();
			 if(summarys==null||summarys.size()==0){
				 return ok(dataLink);
			 }
			 for(EdocSummaryModel edoc:summarys){
				 String url = "/edocController.do?method=detailIFrame&affairId="+edoc.getAffairId()+"&from=Done";
				 affairLink.put(edoc.getAffairId().toString(), SSOTicketBean.makeURLOfSSOTicket(ticket, url));
			 }
			 dataLink.put("urllink", affairLink);
			 dataLink.put("data", summarys);

			 return ok(dataLink);
		 }
		return ok(summarys);
	}



	/**
	 * 根据id获取公文summary
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/{id}")
	@RestInterfaceAnnotation
	public Response getEdocSummaryById(@PathParam("id") long id) throws BusinessException {
		EdocSummary summary = null;
		try {

			summary = edocManager.getEdocSummaryById(id, true);
		} catch (Exception e) {
			log.error("通过id获得公文summary对象报错!"+e);
		}
		return ok(summary);
	}

	/**
	 * 导出公文单信息
	 * @return
	 * @throws BusinessException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/export")
	@RestInterfaceAnnotation
	public String exportEdocBySummaryId(Map<String,Object> param) throws BusinessException {
		String resultInfo="0";
		 List<String> summaryinfo = (ArrayList<String>) param.get("summaryid");
		String folder=(String) param.get("folder");

		try {
			for(int i=0;i<summaryinfo.size();i++){
				Long summaryId=Long.parseLong(summaryinfo.get(i));
				boolean result=edocFormManager.writeForm2File(summaryId, folder);
				resultInfo=summaryId.toString()+result;
			}
		} catch (Exception e) {
			log.error("export documents error!"+e);
		}
		return resultInfo;
	}

	/**
	 * 导入公文单信息
	 * @return
	 * @throws BusinessException
	 * @throws OAInterfaceException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/import")
	@RestInterfaceAnnotation
	public String importEdoc(Map<String,Object> param) throws BusinessException, OAInterfaceException {
		String xmlData=(String) param.get("xmlData");
		Long edocSummary=getExportUtil().importEdoc(xmlData);
		String resultInfo="0";
		resultInfo=edocSummary.toString();
		return resultInfo;
	}

	private Map<String, Object> createCondition(User user,int edocType,int state,String listType){
		Map<String, Object> condition = new HashMap<String, Object>();
		condition.put("track", -1);
		condition.put("state", state);
		condition.put("edocType", edocType);
		condition.put("subEdocType", -1L);
	    condition.put("type", 10);
	    condition.put("user", user);
	    condition.put("userId", user.getId());
	    condition.put("accountId", user.getAccountId());
	    condition.put("listType", listType);
	    return condition;
	}

	private User getUser(String ticket,Long mId){
		Long memberId = null;
		if(Strings.isNotBlank(ticket)){
			//V6.1增加通过单点登录令牌获取人员ID
			SSOTicketManager.TicketInfo ticketInfo = null;
			ticketInfo=SSOTicketBean.getTicketInfoByticketOrname(ticket);
			if(ticketInfo!=null){
				memberId=ticketInfo.getMemberId();
			}
		  //xuqiangwei 解耦未完 TODO
//			try {
//				memberId = TicketUtil.getMemberIdFromTicket(ticket);
//			} catch (BusinessException e) {
//				log.error("通过ticket获得memberId出错!", e);
//			}
		}
		V3xOrgMember member = null;
		try {
			member = orgManager.getMemberById(memberId);
		} catch (BusinessException e) {
			log.error("",e);
		}
		User user = new User();
		user.setId(memberId);
		if(member!=null){
			user.setName(member.getName());
			user.setAccountId(member.getOrgAccountId());
		}
		return user;
	}


      	/**
          * @param currentPage 当前页
          * @param avgRows 每页显示数
          * @param ListInfo 需要分页的LIST
          * @return
          */

    public List<EdocSummaryModel> getPagerList(int currentPage,int avgRows,List<EdocSummaryModel> ListInfo) {
        List<EdocSummaryModel> newList = new ArrayList<EdocSummaryModel>();
        for(int i = (currentPage - 1) * avgRows; i < ListInfo.size() && i < currentPage * avgRows; i++) {
            newList.add(ListInfo.get(i));
        }
        return newList;
    }

    private IDataExportUtils getExportUtil() throws OAInterfaceException {
        return ExternalUse.getInstance().getDataExportUtils();
    }

    /**
     * 设置公文的默认发文单位/发文部门(用逗号隔开)
     * @param summary
     * @param user
     * @param setType 0发文单位 1发文部门
     * @return
     */
    private EdocSummary setEdocDefaultSendInfo(EdocSummary summary, User user, String setType) {
        try {
            if(0==summary.getEdocType() || 2==summary.getEdocType()) {
                if(setType.contains("0")) {
                    if(Strings.isBlank(summary.getSendUnit())) {
                        long accountId = user.getLoginAccount();
                        summary.setSendUnitId("Account|" + accountId);
                        summary.setSendUnit(orgManager.getAccountById(accountId).getName());
                    }
                }
                if(setType.contains("1")) {
                    if(Strings.isBlank(summary.getSendDepartment())) {
                        V3xOrgDepartment dept = orgManager.getCurrentDepartment();
                        if(dept != null) {
                            summary.setSendDepartmentId("Department|"+String.valueOf(user.getDepartmentId()));
                            summary.setSendDepartment(dept.getName());
                        }
                    }
                }
            }
        } catch(Exception e) {
            log.error("设置EdocSummary发文单位与发文部门出错", e);
        }
        return summary;
    }

    private EdocSummary requestToSummary(Map<String,Object> params, EdocSummary summary, long formId) {
        String fieldName = "";
        String fieldValue = "";
        if (summary.getEdocType() == com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_SEND) {
            EdocForm form = edocFormManager.getEdocForm(formId);
            if (form != null) {
                summary.setSubEdocType(form.getSubType());
            }
        }
        List<EdocFormElement> list = edocFormManager.getEdocFormElementByFormId(formId);
        try {
            //-----性能优化，将公文单的公文元素保存进ThreadLocal中
            if (SharedWithThreadLocal.getIsCanUse()) {
                SharedWithThreadLocal.setEdocFormElements(list);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        if (Strings.isNotEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                EdocElement element = elementManager.getEdocElementsById((list.get(i)).getElementId());
                fieldName = "my:" + element.getFieldName();
                fieldValue = (String) params.get(fieldName);
                if (fieldValue == null) {
                    continue;
                }
                DataUtil.setEdocSummaryValue(summary, element.getFieldName(), fieldValue);
            }
        }

        //---------------changyi 增加保存发文部门   start--------------------
        if (Strings.isNotBlank((String)params.get("my:send_department"))) {
            summary.setSendDepartment((String)params.get("my:send_department"));
        }
        //OA-12357 拟文时，选择全元素单，发文部门B这个元素，输入的内容保存不了。在待办中修改文单，这个字段还是保存不了。
        if (Strings.isNotBlank((String)params.get("my:send_department2"))) {
            summary.setSendDepartment2((String)params.get("my:send_department2"));
        }
        //---------------changyi 增加保存发文部门   end--------------------
        summary.setFormId(formId);
        //读取隐藏的单位ID值
        String sendUnitIds = (String)params.get("my:send_unit_id");
        summary.setSendUnitId(sendUnitIds);
        if (("".equals(summary.getSendUnit()) || summary.getSendUnit() == null) && Strings.isNotBlank(sendUnitIds)) {
            String[] arrSendUnit = sendUnitIds.split(",");
            for (int j = 0; j < arrSendUnit.length; j++) {
                String sendUnitStrTemp = arrSendUnit[j];
                if (Strings.isBlank(sendUnitStrTemp)) {
                    break;
                }
                String sendUnitStr = sendUnitStrTemp.split("\\|")[1];
                Long id = 0l;
                if (Strings.isNotBlank(sendUnitStr)) {
                    id = Long.parseLong(sendUnitStr);
                    try {
                        V3xOrgAccount account = orgManager.getAccountById(id);
                        if (account != null)
                            summary.setSendUnit(account.getName());
                    } catch (BusinessException e) {
                        log.error("获取单位错误：", e);
                    }
                }
            }
        }

        summary.setSendToId((String)params.get("my:send_to_id"));
        summary.setCopyToId((String)params.get("my:copy_to_id"));
        summary.setReportToId((String)params.get("my:report_to_id"));
        String sendDeptId = (String)params.get("my:send_department_id");
        summary.setSendDepartmentId(sendDeptId);
        if (Strings.isBlank(summary.getSendDepartment()) && Strings.isNotBlank(sendDeptId)) {
            String[] arrSendDeptId = sendDeptId.split(",");
            for (int k = 0; k < arrSendDeptId.length; k++) {
                String sendDeptIdStr = arrSendDeptId[k];
                if (Strings.isBlank(sendDeptIdStr)) {
                    break;
                }
                String sendDeptStr = sendDeptIdStr.split("\\|")[1];
                Long id = Long.parseLong(sendDeptStr);
                try {
                    V3xOrgDepartment d = orgManager.getDepartmentById(id);
                    if (d != null) {
                        summary.setSendDepartment(d.getName());
                    }
                } catch (Exception e) {
                    log.error("获取部门错误：", e);
                }
            }
        }
        String sendUnitIds2 = (String)params.get("my:send_unit_id2");
        summary.setSendUnitId2(sendUnitIds2);
        if (Strings.isBlank(summary.getSendUnit2()) && Strings.isNotBlank(sendUnitIds2)) {
            String[] arrSendUnit2 = sendUnitIds2.split(",");
            for (int j = 0; j < arrSendUnit2.length; j++) {
                String sendUnitStrTemp2 = arrSendUnit2[j];
                if (Strings.isBlank(sendUnitStrTemp2)) {
                    break;
                }
                String sendUnitStr2 = sendUnitStrTemp2.split("\\|")[1];
                Long id = 0l;
                if (Strings.isNotBlank(sendUnitStr2)) {
                    id = Long.valueOf(sendUnitStr2);
                    try {
                        V3xOrgAccount account = orgManager.getAccountById(id);
                        if (account != null)
                            summary.setSendUnit2(account.getName());
                    } catch (BusinessException e) {
                        log.error("获取单位错误：", e);
                    }
                }
            }
        }

        summary.setSendToId2((String)params.get("my:send_to_id2"));
        summary.setCopyToId2((String)params.get("my:copy_to_id2"));
        summary.setReportToId2((String)params.get("my:report_to_id2"));
        summary.setSendDepartmentId2((String)params.get("my:send_department_id2"));
        String sendDeptId2 = (String)params.get("my:send_department_id2");
        summary.setSendDepartmentId2(sendDeptId2);
        if (Strings.isBlank(summary.getSendDepartment2()) && Strings.isNotBlank(sendDeptId2)) {
            String[] arrsendDeptId2 = sendDeptId2.split(",");
            for (int l = 0; l < arrsendDeptId2.length; l++) {
                String sendDeptId2Str = arrsendDeptId2[l];
                if (Strings.isBlank(sendDeptId2Str)) {
                    break;
                }
                String sendDept2Str = sendDeptId2Str.split("\\|")[1];
                Long id = Long.parseLong(sendDept2Str);
                try {
                    V3xOrgDepartment d = orgManager.getDepartmentById(id);
                    if (d != null) {
                        summary.setSendDepartment2(d.getName());
                    }
                } catch (BusinessException e) {
                    log.error("获取部门错误：", e);
                }
            }
        }
        //huoqu
        if (Strings.isNotBlank((String)params.get("my:filesm"))) {
            summary.setFilesm((String)params.get("my:filesm"));
        }
        if (Strings.isNotBlank((String)params.get("my:filefz"))) {
            summary.setFilefz((String)params.get("my:filefz"));
        }
        if (Strings.isNotBlank((String)params.get("my:phone"))) {
            summary.setPhone((String)params.get("my:phone"));
        }
        if (Strings.isNotBlank((String)params.get("my:party"))) {
            summary.setParty((String)params.get("my:party"));
        }
        if (Strings.isNotBlank((String)params.get("my:administrative"))) {
            summary.setAdministrative((String)params.get("my:administrative"));
        }
        if (Strings.isNotBlank((String)params.get("my:print_unit_id"))) {
            summary.setPrintUnitId((String)params.get("my:print_unit_id"));
        }
        //读取所属单位
        String orgAccountId = (String)params.get("orgAccountId");
        if (Strings.isNotBlank(orgAccountId)) {
            summary.setOrgAccountId(Long.parseLong(orgAccountId));
        }
        //读取公文模板模板预归档目录
        String archiveId = (String)params.get("archiveId");
        if (Strings.isNotBlank(archiveId)) {
            summary.setArchiveId(Long.parseLong(archiveId));
        }
        //公文单中无发文单位元素
        if (Strings.isBlank(summary.getSendUnit())) {
            summary.setSendUnitId(null);
        }
        if (Strings.isBlank(summary.getSendUnit2())) {
            summary.setSendUnitId2(null);
        }

        if (Strings.isBlank(summary.getSendDepartment())) {
            summary.setSendDepartmentId(null);
        }
        if (Strings.isBlank(summary.getSendDepartment2())) {
            summary.setSendDepartmentId2(null);
        }
        //读取处理期限和提前提醒
        summary.setDeadline(ParamUtil.getLong(params, "deadline", -1L));
        if(Strings.isNotBlank(ParamUtil.getString(params, "deadLineDateTimeInput"))){
            summary.setDeadlineDatetime(ParamUtil.getDate(params, "deadLineDateTimeInput", null, "yyyy-MM-dd HH:mm", true));
        }
        summary.setAdvanceRemind(ParamUtil.getLong(params, "advanceRemind", -1L));
        summary.setCanTrack(ParamUtil.getInt(params, "track",0));
        summary.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        if(Strings.isNotBlank(ParamUtil.getString(params, "my:receipt_date"))){
            summary.setReceiptDate(new java.sql.Date(ParamUtil.getDate(params, "my:receipt_date").getTime()));
        }
        if(Strings.isNotBlank(ParamUtil.getString(params,"my:registration_date"))){
            summary.setRegistrationDate(new java.sql.Date(ParamUtil.getDate(params, "my:registration_date").getTime()));
        }
        String auditor = (String)params.get("my:auditor");
        if (Strings.isNotBlank(auditor)) {
            summary.setAuditor(auditor);
        }

        String review = (String)params.get("my:review");
        if (Strings.isNotBlank(review)) {
            summary.setReview(review);
        }
        String undertaker = (String)params.get("my:undertaker");
        if (Strings.isNotBlank(undertaker)) {
            summary.setUndertaker(undertaker);
        }
        if (Strings.isBlank((String)params.get("my:create_person")) && null == summary.getCreatePerson()) {
            summary.setCreatePerson(AppContext.getCurrentUser().getName());
        }
        //承办机构ID
        String undertakenofficeId = (String)params.get("my:undertakenoffice_id");
        String underTakenOffice = EdocHelper.getEntityNames(undertakenofficeId, "、", orgManager);
        summary.setUndertakenofficeId(undertakenofficeId);
        summary.setUndertakenoffice(underTakenOffice);

        return summary;
    }

    /**
     * 登记使用的文号,返回真正的文号串
     * @param markStr:掩码格式文号，详细见EdocMarkModel.parse()方法
     * @param markNum
     */
    private String registDocMark(Long summaryId, String markStr, int markNum, int edocType, boolean checkId, int markType) throws EdocMarkHistoryExistException {
        if (Strings.isNotBlank(markStr)) {
            markStr = markStr.replaceAll(String.valueOf((char) 160), String.valueOf((char) 32));
        }

        EdocMarkModel em = EdocMarkModel.parse(markStr);
        if (em != null) {
            Integer t = em.getDocMarkCreateMode();//0:未选择文号，1：下拉选择的文号，2：选择的断号，3.手工输入 4.预留文号
            String _edocMark = em.getMark(); //需要保存到数据库中的公文文号
            Long markDefinitionId = em.getMarkDefinitionId();
            Long edocMarkId = em.getMarkId();
            User user = AppContext.getCurrentUser();
            if (markType == EdocEnum.MarkType.edocMark.ordinal()) {//公文文号
                if (t != 0) {//等于0的时候没有进行公文文号修改
                    edocMarkManager.disconnectionEdocSummary(summaryId, markNum);
                }
                if (edocType != com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_SIGN) {
                    if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_NEW) { // 选择了一个新的公文文号
                        Integer currentNo = em.getCurrentNo();
                        edocMarkManager.createMark(markDefinitionId, currentNo, _edocMark, summaryId, markNum);
                    } else if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_OLD) { // 选择了一个断号
                        edocMarkManager.createMarkByChooseNo(edocMarkId, summaryId, markNum);
                    } else if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_RESERVE) { // 选择了一个预留文号
                        edocMarkManager.createMarkByChooseReserveNo(edocMarkId, summaryId, em.getCurrentNo(), markNum);
                    } else if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_INPUT) { // 手工输入一个公文文号
                        edocMarkManager.createMark(_edocMark, summaryId, markNum);
                    }
                } else {//签报处理
                    if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_NEW) {
                        Integer currentNo = em.getCurrentNo();
                        this.edocMarkHistoryManager.save(summaryId, currentNo, _edocMark, markDefinitionId, markNum, user.getId(), user.getId(), checkId, true);
                    } else if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_OLD) {
                        this.edocMarkHistoryManager.saveMarkHistorySelectOld(edocMarkId, _edocMark, summaryId, user.getId(), checkId);
                    } else if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_INPUT) {
                        this.edocMarkHistoryManager.save(summaryId, _edocMark, markDefinitionId, markNum, user.getId(), user.getId(), checkId, false);
                    }
                }
            } else if (markType == EdocEnum.MarkType.edocInMark.ordinal()) {//内部文号
                if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_NEW) {
                    this.edocMarkDefinitionManager.setEdocMarkCategoryIncrement(markDefinitionId);
                }
            }
            return _edocMark;
        }
        return null;
    }

  //根据拟文时选择的流程期限具体时间，换算成分钟数
    private Long getMinValueByDeadlineTime(String deadlineTime,Date createTime){
        Long minValue=-1L;
        try{
            if(Strings.isNotBlank(deadlineTime) && createTime!=null){
                Date deadline=Datetimes.parse(deadlineTime, null, Datetimes.datetimeWithoutSecondStyle);
                String sdate=Datetimes.formatDatetimeWithoutSecond(createTime);
                createTime=Datetimes.parse(sdate, null, Datetimes.datetimeWithoutSecondStyle);
                long minusDay = deadline.getTime() - createTime.getTime();
                if(minusDay>0){
                	double minusValue=(double)minusDay/(1000*60);
                    minValue=(long)Math.rint(minusValue);
                }
            }
        }catch(Exception e){
            log.error("公文拟文转换流程期限具体时间格式化错误。", e);
        }finally{
        }
        return minValue;
    }

    /**
     * 得到公文的所属部门的ID
     * 在主单位下，取主部门为公文所属部门，由于系统无法识别他由主岗发文还是由副岗发文，鉴于概率低，就取主岗部门了。
     * 在兼职单位下，取多个兼职部门中的一个（按排序号，兼职序号在前的哪个部门 ）为公文所属部门。
     * @param  accoutId :公文所属单位
     * @param agentUserId ： 被代理人ID， 如果没有被代理人传入null
     * @return  公文所属部门ID
     */
    private Long getEdocOwnerDepartmentId(Long accoutId, Long agentUserId) {
        Long userId = null;
        Long userAccountId = null;
        Long currentDeptId = null;
        if (agentUserId != null) {
            try {
                V3xOrgMember agentMember = orgManager.getMemberById(agentUserId);
                userId = agentMember.getId();
                userAccountId = agentMember.getOrgAccountId();
                currentDeptId = agentMember.getOrgDepartmentId();
            } catch (BusinessException e) {
                //这个异常不做处理
            }
        }

        if (userId == null) {
            User user = AppContext.getCurrentUser();
            userId = user.getId();
            userAccountId = user.getAccountId();
            currentDeptId = user.getDepartmentId();
        }

        if (!Strings.equals(accoutId, userAccountId)) {
            try {
                Map<Long, List<MemberPost>> map = orgManager.getConcurentPostsByMemberId(accoutId, userId);
                long min = -1;
                for (Long deptId : map.keySet()) {
                    List<MemberPost> list = map.get(deptId);
                    for (MemberPost concurrentPost : list) {
                        if (min == -1)
                            min = concurrentPost.getSortId();
                        if (concurrentPost.getSortId() <= min) {
                            min = concurrentPost.getSortId();
                            currentDeptId = deptId;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("公文所属部门判断异常:", e);
            }
        }
        return currentDeptId;
    }

    private void addAttachments(List<V3XFile> v3xFileList, Long meetingId, Long subReference) throws BusinessException {
        if(Strings.isNotEmpty(v3xFileList)){
            List<Attachment> attList = new ArrayList<Attachment>();
            for (V3XFile v3xFile : v3xFileList) {
                com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE type = null;
                switch (v3xFile.getType()) {
                    case 0:
                        type = com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE;
                        break;
                    case 1:
                        type = com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.IMAGE;
                        break;
                    case 2:
                        type = com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.DOCUMENT;
                        break;
                    case 3:
                        type = com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FormFILE;
                        break;
                    case 4:
                        type = com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FormDOCUMENT;
                        break;
                    default:
                        type = com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.NewsImage;
                        break;
                }
                Attachment att = new Attachment(v3xFile, ApplicationCategoryEnum.meeting, type);
                att.setIdIfNew();
                att.setDescription(v3xFile.getDescription());
                att.setGenesisId(v3xFile.getId());
                att.setReference(meetingId);
                att.setSubReference(subReference);
                attList.add(att);
            }
            attachmentManager.deleteByReference(meetingId, subReference);
            attachmentManager.create(attList);
        }else{
            attachmentManager.deleteByReference(meetingId, subReference);
        }
    }

    private void add2Index(int edocType, Long affairId) {
        if (AppContext.hasPlugin("index")) {
            try {
                indexManager.add(affairId, ApplicationCategoryEnum.edoc.getKey());
            } catch (BusinessException e) {
                log.error("公文增加全文检索抛出异常：", e);
            }
        }
    }

	/**
	 * 外部系统签收公文后向协同平台发送签收回执
	 * @return 0未回执成功 1回执成功
	 * @throws ServiceException
	 * @throws OAInterfaceException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/signedoc")
	@RestInterfaceAnnotation
	public int signEdoc(Map<String,Object> param) throws ServiceException, OAInterfaceException {
		int resultInfo=0;
		try{
			 DocumentService documentService=new DocumentServiceImpl();
		     Long edocSendId=Long.valueOf((String) param.get("edocSendId"));
		     Map<String,Object> edocSignReceipt=(Map<String,Object>) param.get("edocSignReceipt");
		     resultInfo=documentService.signEdocToRest(edocSendId, edocSignReceipt);
		}catch(Exception e){
			log.error("", e);
		}
		return resultInfo;
	}

	public String getHtmlText(String oldpath, String strEncoding) {

		File sourceFile = new File(oldpath);
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		FileInputStream fi = null;
		try {
			fi = new FileInputStream(sourceFile);
			byte[] buffer = new byte[fi.available()];
			fi.read(buffer);
			out.write(buffer);
			return new String(buffer, strEncoding);
		} catch (IOException e) {
			//e.printStackTrace();
			log.error(e.getMessage(), e);
			return "";
		} finally {
			try {
				if (fi != null) fi.close();
				out.close();
			} catch (IOException e) {
				//e.printStackTrace();
				log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * 根据ID导出公文单MHT格式信息
	 * @return
	 * @throws ServiceException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("id/exportmht")
	@RestInterfaceAnnotation
	public boolean exportEdocBySummaryIdMht(Map<String,Object> param){
		boolean resultInfo=false;

		ArrayList<String> summaryinfo=(ArrayList<String>) param.get("summaryid");
		String importpath=(String)param.get("folder");// 导出公文单HTML路径
		String htmlpath="";//html文件路径
		String exportpath="";//导出MHT路径
		String mhtpath="";//MHT文件路径
		String strEncoding="utf-8";//编码格式utf-8
		try{
			for(int i=0;i<summaryinfo.size();i++){
				Long summaryId=Long.parseLong(summaryinfo.get(i));
				boolean result=edocFormManager.writeForm2File(summaryId, importpath);
				htmlpath=importpath+summaryId.toString()+"\\form.html";
				mhtpath=importpath+summaryId.toString()+"\\form.mht";
				exportpath=importpath+summaryId.toString();
				String strText1 = getHtmlText(htmlpath,strEncoding);
				HtmlToHmtUntil htmltohmt = new HtmlToHmtUntil(strText1, htmlpath,
						strEncoding, mhtpath, exportpath);
				resultInfo=htmltohmt.compile();
				}
			}catch (Exception e) {
				log.error("export documents error!"+e);
			}
			return resultInfo;
	}

}

