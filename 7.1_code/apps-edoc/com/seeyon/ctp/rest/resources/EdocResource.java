package com.seeyon.ctp.rest.resources;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.google.common.base.Joiner;
import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.enums.ColOpenFrom;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum;
import com.seeyon.apps.collaboration.vo.SeeyonPolicy;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.constants.DocConstants.PigeonholeType;
import com.seeyon.apps.edoc.api.EdocApi;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.govdoc.bo.DateSharedWithWorkflowEngineThreadLocal;
import com.seeyon.apps.govdoc.bo.GovdocLockObject;
import com.seeyon.apps.govdoc.constant.GovdocEnum.ExchangeDetailStatus;
import com.seeyon.apps.govdoc.constant.GovdocListEnum.GovdocListTypeEnum;
import com.seeyon.apps.govdoc.helper.GovdocContentHelper;
import com.seeyon.apps.govdoc.helper.GovdocExchangeHelper;
import com.seeyon.apps.govdoc.helper.GovdocRoleHelper;
import com.seeyon.apps.govdoc.manager.GovdocCommentManager;
import com.seeyon.apps.govdoc.manager.GovdocContentManager;
import com.seeyon.apps.govdoc.manager.GovdocContinueManager;
import com.seeyon.apps.govdoc.manager.GovdocExchangeManager;
import com.seeyon.apps.govdoc.manager.GovdocFormManager;
import com.seeyon.apps.govdoc.manager.GovdocListManager;
import com.seeyon.apps.govdoc.manager.GovdocLockManager;
import com.seeyon.apps.govdoc.manager.GovdocManager;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.govdoc.manager.GovdocWorkflowManager;
import com.seeyon.apps.govdoc.manager.QwqpManager;
import com.seeyon.apps.govdoc.manager.external.GovdocIndexEnableImpl;
import com.seeyon.apps.govdoc.option.manager.FormOptionExtendManager;
import com.seeyon.apps.govdoc.po.FormOptionExtend;
import com.seeyon.apps.govdoc.po.GovdocExchangeDetail;
import com.seeyon.apps.govdoc.util.GovDocOpenFromUtil;
import com.seeyon.apps.govdoc.util.GovDocOpenFromUtil.ColSummaryType;
import com.seeyon.apps.govdoc.vo.GovdocAttachmentVO;
import com.seeyon.apps.govdoc.vo.GovdocDealVO;
import com.seeyon.apps.govdoc.vo.GovdocListVO;
import com.seeyon.apps.govdoc.vo.GovdocRepealVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.apps.multicall.api.MultiCallApi;
import com.seeyon.apps.taskmanage.util.MenuPurviewUtil;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.SystemConfig;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.constants.CustomizeConstants;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.Comment.CommentType;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.content.mainbody.CtpContentAllBean;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.content.mainbody.handler.MainbodyHandler;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.customize.manager.CustomizeManager;
import com.seeyon.ctp.common.dao.paginate.Pagination;
import com.seeyon.ctp.common.detaillog.manager.DetaillogManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.Constants;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.filemanager.manager.Util;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.office.MSignatureManager;
import com.seeyon.ctp.common.office.MSignaturePicHandler;
import com.seeyon.ctp.common.office.UserUpdateObject;
import com.seeyon.ctp.common.office.manager.OfficeBakFileManager;
import com.seeyon.ctp.common.permission.bo.CustomAction;
import com.seeyon.ctp.common.permission.bo.DetailAttitude;
import com.seeyon.ctp.common.permission.bo.NodePolicy;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.enums.PermissionAction;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.permission.vo.PermissionMiniVO;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.phrase.manager.CommonPhraseManager;
import com.seeyon.ctp.common.phrase.po.CommonPhrase;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.supervise.enums.SuperviseEnum;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.supervise.vo.SuperviseMessageParam;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.template.util.TemplateUtil;
import com.seeyon.ctp.common.track.enums.TrackEnum;
import com.seeyon.ctp.common.track.manager.CtpTrackMemberManager;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormDataBean;
import com.seeyon.ctp.form.bean.FormDataMasterBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.po.FormPermissionConfig;
import com.seeyon.ctp.form.util.permission.factory.PermissionFatory;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.MemberPost;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgPost;
import com.seeyon.ctp.organization.bo.V3xOrgTeam;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.rest.resources.util.EdocResourceUtil;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.common.security.AccessControlBean;
import com.seeyon.v3x.common.security.SecurityCheck;
import com.seeyon.v3x.common.security.SecurityCheckParam;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocManagerModel;
import com.seeyon.v3x.edoc.domain.EdocMark;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocOpinion.OpinionType;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.enums.EdocOpenFrom;
import com.seeyon.v3x.edoc.manager.EdocFormManager;
import com.seeyon.v3x.edoc.manager.EdocH5Manager;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocListManager;
import com.seeyon.v3x.edoc.manager.EdocLockManager;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.manager.EdocMarkManager;
import com.seeyon.v3x.edoc.manager.EdocRegisterManager;
import com.seeyon.v3x.edoc.manager.EdocStatManager;
import com.seeyon.v3x.edoc.manager.EdocSummaryManager;
import com.seeyon.v3x.edoc.util.EdocOpenFromUtil.EdocSummaryType;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryBO;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryCountVO;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryModel;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;
import com.seeyon.v3x.exchange.manager.EdocExchangeManager;
import com.seeyon.v3x.system.signet.domain.V3xHtmDocumentSignature;
import com.seeyon.v3x.system.signet.enums.V3xHtmSignatureEnum;
import com.seeyon.v3x.system.signet.manager.V3xHtmDocumentSignatManager;
import com.seeyon.v3x.system.util.WaterMarkUtil;

import edu.emory.mathcs.backport.java.util.Arrays;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;
import www.seeyon.com.utils.json.JSONUtils;

@Path("edocResource")
@Produces({ MediaType.APPLICATION_JSON})
public class EdocResource extends BaseResource{
	private static final String ERROR_KEY          = "error_msg";
	private static Log LOGGER = CtpLogFactory.getLog(EdocResource.class);
    private static final String INFO_KEY          = "info_msg";
    
    /** 全部意见 **/
    private static final String COMMENT_TYPE_ALL = "all";
    private static final String COMMENT_TYPE_LIKE = "like";
    /** 同意意见 **/
    private static final String COMMENT_TYPE_AGREE = "agree";
    /** 不同意意见 **/
    private static final String COMMENT_TYPE_DISAGREE = "disagree";
    private static final String JSON_PARAMS        = "_json_params";
    private static final String SUCCESS_KEY        = "success"; 
    private static final String SUCCESS_VALUE_TRUE        = "true";
    private static final String SUCCESS_VALUE_FALSE        = "false";

	private AffairManager affairManager = (AffairManager)AppContext.getBean("affairManager");
	private CtpTrackMemberManager trackManager = (CtpTrackMemberManager)AppContext.getBean("trackManager");
	private CustomizeManager customizeManager = (CustomizeManager)AppContext.getBean("customizeManager");
	private EdocListManager edocListManager = (EdocListManager)AppContext.getBean("edocListManager");
	private EdocExchangeManager edocExchangeManager= (EdocExchangeManager)AppContext.getBean("edocExchangeManager");
	private EdocH5Manager edocH5Manager = (EdocH5Manager)AppContext.getBean("edocH5Manager");
	private FileManager         fileManager        = (FileManager) AppContext.getBean("fileManager");
    private CommonPhraseManager phraseManager = (CommonPhraseManager)AppContext.getBean("phraseManager");
    private EdocManager edocManager = (EdocManager)AppContext.getBean("edocManager");
    private PermissionManager   permissionManager  = (PermissionManager) AppContext.getBean("permissionManager");
    private EdocFormManager edocFormManager   = (EdocFormManager) AppContext.getBean("edocFormManager");
    private WorkflowApiManager wapi    = (WorkflowApiManager) AppContext.getBean("wapi");
    private V3xHtmDocumentSignatManager htmSignetManager = (V3xHtmDocumentSignatManager) AppContext.getBean("htmSignetManager");
    private EdocSummaryManager    edocSummaryManager   = (EdocSummaryManager) AppContext.getBean("edocSummaryManager");
    private MSignatureManager mSignatureManager = (MSignatureManager) AppContext.getBean("mSignatureManagerforM3");
    private OrgManager          orgManager         = (OrgManager) AppContext.getBean("orgManager");
    private EdocRegisterManager edocRegisterManager = (EdocRegisterManager) AppContext.getBean("edocRegisterManager");
    private GovdocIndexEnableImpl govdocIndexEnable = (GovdocIndexEnableImpl)AppContext.getBean("govdocIndexEnable");
    private OfficeBakFileManager officeBakFileManager   = (OfficeBakFileManager) AppContext.getBean("officeBakFileManager");
    private AttachmentManager  attachmentManager = (AttachmentManager)AppContext.getBean("attachmentManager");;
    private UserMessageManager  userMessageManager = (UserMessageManager)AppContext.getBean("userMessageManager");
    private EdocStatManager				edocStatManager = (EdocStatManager) AppContext.getBean("edocStatManager");
    private EdocMarkManager				edocMarkManager = (EdocMarkManager) AppContext.getBean("edocMarkManager");
    private GovdocSummaryManager govdocSummaryManager =(GovdocSummaryManager) AppContext.getBean("govdocSummaryManager");
    private TemplateManager     templateManager    = (TemplateManager) AppContext.getBean("templateManager");
	private GovdocFormManager govdocFormManager = (GovdocFormManager)AppContext.getBean("govdocFormManager");
    private MainbodyManager     ctpMainbodyManager = (MainbodyManager) AppContext.getBean("ctpMainbodyManager");
    private FormApi4Cap3 formApi4Cap3 = (FormApi4Cap3) AppContext.getBean("formApi4Cap3");
    private CommentManager commentManager= (CommentManager) AppContext.getBean("ctpCommentManager");
    private FormOptionExtendManager formOptionExtendManager = (FormOptionExtendManager)AppContext.getBean("formOptionExtendManager");
    private GovdocLockManager govdocLockManager=(GovdocLockManager) AppContext.getBean("govdocLockManager");
    private MultiCallApi        multiCallApi       = (MultiCallApi) AppContext.getBean("multiCallApi");
    private GovdocContinueManager govdocContinueManager=(GovdocContinueManager) AppContext.getBean("govdocContinueManager");
	private QwqpManager qwqpManager = (QwqpManager)AppContext.getBean("qwqpManager");
	private GovdocExchangeManager govdocExchangeManager =  (GovdocExchangeManager)AppContext.getBean("govdocExchangeManager");;
    private SuperviseManager        superviseManager   =(SuperviseManager) AppContext.getBean("superviseManager");        
    private EnumManager        enumManagerNew   =(EnumManager) AppContext.getBean("enumManagerNew"); 
    private GovdocManager govdocManager=(GovdocManager) AppContext.getBean("govdocManager");
    private GovdocCommentManager govdocCommentManager =(GovdocCommentManager) AppContext.getBean("govdocCommentManager");
    private CollaborationApi collaborationApi = (CollaborationApi) AppContext.getBean("collaborationApi");
    private EdocLockManager edocLockManager = (EdocLockManager) AppContext.getBean("edocLockManager");
    private DocApi              docApi             = (DocApi) AppContext.getBean("docApi");
    private GovdocListManager  govdocListManager = (GovdocListManager)AppContext.getBean("govdocListManager");
    private GovdocContentManager govdocContentManager = (GovdocContentManager)AppContext.getBean("govdocContentManager");
    private DetaillogManager detaillogManager   = (DetaillogManager)AppContext.getBean("detaillogManager");
    private GovdocWorkflowManager govdocWorkflowManager = (GovdocWorkflowManager) AppContext.getBean("govdocWorkflowManager");
	private CAPFormManager capFormManager = null;
	private SystemConfig systemConfig = (SystemConfig) AppContext.getBean("systemConfig");

    /**
	 * 公文事项设置是否跟踪
	 * url: edocResource/setTrack
	 * @param 
	 * 		类型        名称                    必填        备注
	 * 		Long  affairId    Y    事项ID
	 * @return 1设置跟踪，0设置不跟踪
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("setTrack")
	public Response setTrack(@QueryParam("affairId") Long affairId) throws BusinessException {
		
		CtpAffair affair = null; 
		try {
			affair = affairManager.get(affairId);
		} catch (BusinessException e) {
			LOGGER.error("",e);
			return fail("find affair is null");
		}
		boolean isTrack = Integer.valueOf(TrackEnum.all.ordinal()).equals(affair.getTrack())
				||Integer.valueOf(TrackEnum.part.ordinal()).equals(affair.getTrack());
		
		int trackValue = isTrack ? TrackEnum.no.ordinal() : TrackEnum.all.ordinal();
		try {
			
			if(Integer.valueOf(TrackEnum.part.ordinal()).equals(affair.getTrack())){
				trackManager.deleteTrackMembers(affair.getObjectId(), affairId);
			}	

			Map<String,Object> m = new HashMap<String,Object>();
			m.put("track", trackValue);
			affairManager.update(affairId, m);
		} catch (BusinessException e) {
			LOGGER.error("",e);
		}
		
		return ok(trackValue);
	}
	
	/**
	 * 公文事项是否设置了跟踪
	 * url: edocResource/trackValue
	 * @param 
	 * 		类型         名称                    必填        备注
	 * 		Long   affairId   Y    事项ID
	 * @return 1设置了跟踪，0未设置跟踪
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("trackValue")
	public Response trackValue(@QueryParam("affairId") Long affairId) throws BusinessException {
		User user = AppContext.getCurrentUser();
		CtpAffair affair = null; 
		Integer trackValue = TrackEnum.no.ordinal();
		try {
			affair = affairManager.get(affairId);
		} catch (BusinessException e) {
			LOGGER.error("",e);
		}
		if(affair == null){
		    return ok(trackValue);
		}
		String _trackValue = customizeManager.getCustomizeValue(user.getId(), CustomizeConstants.TRACK_PROCESS);
		if("true".equals(_trackValue) && Integer.valueOf(TrackEnum.no.ordinal()).equals(affair.getTrack()) && 
				!Integer.valueOf(SubStateEnum.col_pending_ZCDB.getKey()).equals(affair.getSubState())){
			trackValue = TrackEnum.all.ordinal();
		}else{
			trackValue = affair.getTrack();
		}
		return ok(trackValue);
	}
	
	
	/**
	 * 获取待办发文
	 * url: edocResource/getAllPending
	 * 
	 * @Params params 获取待办发文参数
	 *  类型             名称               必填        备注
	 *  Integer pageNo    Y 分页信息，第几页
	 *  Integer pageSize  Y 分页信息，每页多少条数据
	 *  以下是查询条件，查询条件为空时查询全部
	 *  String  startMemberName N  发起人
	 *  String  subject         N  标题
	 *  String  createDate      N  发起时间
	 * @return  FlipInfo 待办公文信息
	 * @throws BusinessException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("getAllPending")
	public Response getAllPending(Map<String,String> params) throws BusinessException {
		FlipInfo flipInfo = super.getFlipInfo();
		flipInfo.setNeedTotal(true);
		
		Pagination.setNeedCount(true);
		int nPageNo = Integer.parseInt(params.get("pageNo"));
		nPageNo = nPageNo<1 ? 1 : nPageNo;
		flipInfo.setPage(nPageNo);
		Integer pageSize = Integer.parseInt(params.get("pageSize"));
		Pagination.setMaxResults(pageSize);
		Pagination.setFirstResult((nPageNo-1)*pageSize);
		flipInfo.setSize(pageSize);
		
		User user = AppContext.getCurrentUser(); 
		
		List<EdocSummaryModel> summarys = null;
		if(flipInfo != null && Strings.isNotBlank(user.getName())){
			 Map<String, Object> condition = createCondition(user, null,StateEnum.col_pending.key(),null);
			 
			 condition.putAll(params);
			 String edocType = params.get("edocType");
			 if(Strings.isNotBlank(edocType)){
			     condition.put("edocType", Integer.valueOf(edocType));
			 }
			 
             try {
				 summarys = edocListManager.findEdocPendingList(10, condition);
			 } catch (BusinessException e) {
				 LOGGER.error("获得待办发文数据报错!",e);
			 }
             //处理名称
             dealReturnInfo(summarys);
             
             flipInfo.setTotal(Pagination.getRowCount());
             flipInfo.setData(EdocSummaryListVO.valueOf(summarys));
		}
		
		return ok(flipInfo);
	}
	
	private void dealReturnInfo(List<EdocSummaryModel> summarys){
		for(EdocSummaryModel summary : summarys){
			//加签、知会、当前会签  ps:回退优先，如果这条记录也被回退过，则优先显示回退图标，加签图标不显示
            if(summary.getAffair().getFromId() != null && summary.getAffair().getBackFromId()==null){
            	summary.setFromName(ResourceUtil.getString("edoc.pending.addOrJointly.label", Functions.showMemberName(summary.getAffair().getFromId())));
            }
            //回退、指定回退
            if(summary.getAffair().getBackFromId()!=null 
            		&& !Integer.valueOf(SubStateEnum.col_pending_specialBackCenter.getKey()).equals(summary.getAffair().getSubState())){
            	summary.setBackFromName(ResourceUtil.getString("edoc.pending.stepBack.label", Functions.showMemberName(summary.getAffair().getBackFromId())));
            }
		}
	}
	/**
	 * 获取所有列表记录数
	 * url: edocResource/getListSizeByEdocType
	 * @return  Object[]
	 * <pre>
	 * 		成功：[ {
	 *				  "listPendingSize" : 30,
	 *				  "listZcdbSize" : 15,
	 *				  "listSentSize" : 167,
	 *				  "listWaitSize" : 13,
	 *				  "listDoneAllSize" : 142,
	 *				  "edocType" : 0
	 *				}, {
	 *				  "listPendingSize" : 9,
	 *				  "listZcdbSize" : 0,
	 *				  "listSentSize" : 15,
 	 *				  "listWaitSize" : 1,
	 *				  "listDoneAllSize" : 4,
	 *				  "edocType" : 1
	 *				}, {
	 *				  "listPendingSize" : 1,
	 *				  "listZcdbSize" : 0,
	 *				  "listSentSize" : 0,
	 *				  "listWaitSize" : 0,
	 *				  "listDoneAllSize" : 0,
	 *				  "edocType" : 2
	 *				} ]
	 * </pre>
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("getListSizeByEdocType")
	public Response getListSizeByEdocType(){
		EdocSummaryCountVO countVo = null;
		Object[] objects = new Object[3];
		for(int i = 0; i < 3; i++) {		
			try {
			    FlipInfo flipInfo = new  FlipInfo();
			    HashMap<String, Object> condition = new HashMap<String, Object>();
            	countVo = new  EdocSummaryCountVO();
            	countVo.setListPendingSize(this.findNewEdocList(flipInfo, "listPendingAll", i, condition).getTotal());
            	countVo.setListZcdbSize(this.findNewEdocList(flipInfo, "listZcdb", i, condition).getTotal());
            	countVo.setListSentSize(this.findNewEdocList(flipInfo, "listSent", i, condition).getTotal());
            	countVo.setListWaitSize(this.findNewEdocList(flipInfo, "listWaitSend", i, condition).getTotal());
            	countVo.setListDoneAllSize(this.findNewEdocList(flipInfo, "listDoneAll", i, condition).getTotal());
            	countVo.setEdocType(i);
            	objects[i] = countVo;
            } catch (BusinessException e) {
            	LOGGER.error("获得待办发文数据报错!",e);
			}
		}
		return ok(objects);
	}

	/**
	 * 获取协同、公文（发文、收文、签报）各状态数量
     * @since   7.1
     * @date    2018-12-06
	 * @return
	 * <pre>
	 *      affair：协同	sentSize：已发数量	pendingSize：待办数量	doneSize：已办数量	waitSendSize：待发数量
	 *      edoc：公文	listPendingSize：待办数量	listZcdbSize：暂存待办数量	listSentSize：已发数量	listWaitSize：待发数量	listDoneAllSize：已办数量	edocType（0：发文	1：收文	 2：签报）
	 * 		成功：{
	 *			    "code" : 0,
	 *			    "data" : {
	 *			      "affair" : {
	 *			        "sentSize" : 0,
	 *			        "pendingSize" : 0,
	 *			        "doneSize" : 0,
	 *			        "waitSendSize" : 0
	 *			      },
	 *			      "edoc" : [ {
	 *			        "listPendingSize" : 1,
	 *			        "listZcdbSize" : 1,
	 *			        "listSentSize" : 0,
	 *			        "listWaitSize" : 0,
	 *			        "listDoneAllSize" : 1,
	 *			        "edocType" : 0
	 *			      }, {
	 *			        "listPendingSize" : 2,
	 *			        "listZcdbSize" : 1,
	 *			        "listSentSize" : 0,
	 *			        "listWaitSize" : 0,
	 *                  "listDoneAllSize" : 1,
	 *                  "edocType" : 1
	 *                }, {
	 *                  "listPendingSize" : 3,
	 *                  "listZcdbSize" : 2,
	 *                  "listSentSize" : 1,
	 *                  "listWaitSize" : 2,
	 *                  "listDoneAllSize" : 1,
	 *                  "edocType" : 2
	 *                } ]
	 *              },
	 *              "message" : ""
	 *            }
	 * </pre>
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("statistic")
    @RestInterfaceAnnotation
	public Response statistic() throws BusinessException {
		Map<String,Object> statisticResult = new HashMap<String,Object>(2);

		// 统计公文（发文、收文、签报）各状态数量
		List<EdocSummaryCountVO> edocSummaryCountList = edocListManager.getCountGroupByEdocType();
		statisticResult.put("edoc",edocSummaryCountList);// 公文统计结果

		// 统计协同各状态的数量
		Map<String,Object> affairStatisticResult = new HashMap<String,Object>(4);
		affairStatisticResult.put("pendingSize",this.getAffairCountByState(Integer.valueOf(StateEnum.col_pending.getKey())) );// 待办协同数量
		affairStatisticResult.put("doneSize",this.getAffairCountByState(Integer.valueOf(StateEnum.col_done.getKey())) );// 已办协同数量
		affairStatisticResult.put("waitSendSize",this.getAffairCountByState(Integer.valueOf(StateEnum.col_waitSend.getKey())));// 待发协同数量
		affairStatisticResult.put("sentSize",this.getAffairCountByState(Integer.valueOf(StateEnum.col_sent.getKey())));// 已发协同数量
		statisticResult.put("affair",affairStatisticResult);// 协同统计结果

		return success(statisticResult);
	}

	/**
	 * 按照公文类型和列表类型获取公文列表
	 * url: edocResource/getSummaryListByEdocTypeAndListType
	 * @param params 获取公文列表的参数
	 * <pre>
	 * 类型             名称                   必填           备注
	 * int	 edocType     N       公文类型
	 *    <pre>
	 *       0 发文
	 *       1 收文
	 *       2 签报
	 *    </pre>
	 * String listType   Y 打开来源
	 *    <pre>
	 *       listPending  待办
	 *       listSent     已发
	 *       listWaitSend 待发
	 *       listDoneAll  已办
	 *    </pre>
	 * String conditionKey  Y 搜索条件
	 * String textfield     Y 搜索传值
	 * String textfield1    N 搜索传值
	 * String pageNo        Y 第几页（大于1的整数）
	 * String pageSize      Y 每页多少条数据(大于1的整数)
	 * </pre>
	 * @return  FlipInfo
	 * @throws BusinessException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("getSummaryListByEdocTypeAndListType")
	public Response getSummaryListByEdocTypeAndListType(Map<String,String> params){
		int edocType = ParamUtil.getInt(params, "edocType");
		String listType = ParamUtil.getString(params, "listType");
		//因为新公文待办数据=待办数据+待阅数据，而前端传的参数listPending不包含待阅，考虑到修改前端的复杂性，所以在后端代码修改参数
		if("listPending".equals(listType)){
			listType="listPendingAll";
		}
		   //从业务生成器生成的公文列表
	    if(params.get("templeteIds") !=null && listType == null){
	    	listType = params.get("openFrom");
	    }
		String conditionKey = ParamUtil.getString(params, "conditionKey");
		String textfield = ParamUtil.getString(params, "textfield");
		String textfield1 = ParamUtil.getString(params, "textfield1");
		FlipInfo flipInfo = super.getFlipInfo();
		flipInfo.setNeedTotal(true);

		Pagination.setNeedCount(true);
		int nPageNo = Integer.parseInt(params.get("pageNo"));
		nPageNo = nPageNo<1 ? 1 : nPageNo;
		Integer pageSize = Integer.parseInt(params.get("pageSize"));
		Pagination.setMaxResults(pageSize);
		Pagination.setFirstResult((nPageNo-1)*pageSize);
		
		List<EdocSummaryModel> summarys = new ArrayList<EdocSummaryModel>();
		User user = AppContext.getCurrentUser(); 
		if(flipInfo != null && Strings.isNotBlank(user.getName())){
			 Map<String, Object> condition = createNewCondition(user,edocType,StateEnum.col_pending.key(),listType,conditionKey,textfield,textfield1,params);
             try {
            	 flipInfo.setSize(pageSize);
            	 flipInfo.setPage(nPageNo);
                // summarys = edocListManager.findEdocPendingList(listTypeInt, condition);
            	 flipInfo = this.findNewEdocList(flipInfo, listType, edocType,condition);
                  List<GovdocListVO> voList = flipInfo.getData();
            	  if(CollectionUtils.isNotEmpty(voList)){
            		  for(GovdocListVO govdocListVO:voList){
                     	 summarys.add(tranToEdocSummaryModel(govdocListVO));
                      }  
            	  }
                  //处理名称
                 dealReturnInfo(summarys);
                 flipInfo.setData(EdocSummaryListVO.valueOf(summarys));
                 //flipInfo.setTotal(Pagination.getRowCount());
			 } catch (BusinessException e) {
				 LOGGER.error("获得待办发文数据报错!",e);
			 }
		}
		return ok(flipInfo);
	}
	
	/**
	 * 
	 * @param user
	 * @param edocType
	 * @param state
	 * @param listType
	 * @param conditionKey 搜索条件
	 * @param textfield	搜索传值
	 * @param textfield1
	 * @return
	 */
    private Map<String, Object> createNewCondition(User user,int edocType,int state,String listType,String conditionKey,String textfield,String textfield1,Map<String, String> params){
    	Map<String, Object> condition = createCondition(user, edocType, state, listType);
	    condition.put("conditionKey",conditionKey);
	    condition.put("textfield",textfield);
	    condition.put("textfield1",textfield1);
	    //从业务生成器生成的公文列表
	    if(params.get("templeteIds") !=null){
	    	condition.put("templeteIds", params.get("templeteIds"));
	       if(null == conditionKey){ //业务生成器生成的列表手动输入条件
	           if(params.get("subject")!=null){
	        	   condition.put("subject", params.get("subject"));
	           }    	   
	    	   if(params.get("startMemberName")!=null){
	    		   condition.put("startMemberName",params.get("startMemberName"));
	    	   }
	    	   if(params.get("createDate") !=null){
	    		   condition.put("createDate",params.get("createDate"));
	    	   }
	       }
	    }
	    return condition;
	}
	
	/**
	 * 公文详情
	 * url: edocResource/edocSummary
	 * @param params 公文详情的参数
	 * <pre>
	 * 类型             名称                       必填           备注
	 * Long    affairId     Y     事项ID
	 * Long    summaryId    Y     协同ID
	 * String  openFrom     N     打开来源（默认值：listDoneAll）
	 *    <pre>
	 *       listPending  待办
	 *       listSent     已发
	 *       listWaitSend 待发
	 *       listDoneAll  已办
	 *       glwd         关联文档
	 *       docLib       文档中心
	 *       lenPotent    借阅
	 *    </pre>
	 * String baseObjectId  N 关联文档属于的数据ID
	 * String baseApp  N 关联文档属于的数据所在的模块 
	 * String docResId  N 文档ID，用于权限验证 
	 * </pre>
	 * @return  EdocSummaryBO
	 */
	@POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("edocSummary")
    public Response edocSummary(Map<String,Object> params){
	    
	    User user = AppContext.getCurrentUser();
	    
    	Long affairId = ParamUtil.getLong(params, "affairId",-1L);
    	Long summaryId = ParamUtil.getLong(params, "summaryId",-1L);
    	String openFrom = ParamUtil.getString(params, "openFrom", "listDoneAll");
    	String isfromIndexSearch =  ParamUtil.getString(params, "isTODO", "");
    	if("todo".equals(isfromIndexSearch) && affairId == -1l){
    		Map<String, Object> findSourceInfo;
			try {
				findSourceInfo = govdocIndexEnable.findSourceInfo(summaryId);
				affairId = (Long) findSourceInfo.get("sourceId");
				CtpAffair affair = affairManager.get(affairId);
				if(affair != null){
					if(StateEnum.col_pending.key() == affair.getState()){
						openFrom = "listPending";
					}else if(StateEnum.col_sent.key() == affair.getState()){
						openFrom = "listSent";
					}else if(StateEnum.col_waitSend.key() == affair.getState()){
						openFrom = "listWaitSend";
					}else if(StateEnum.col_done.key() == affair.getState()){
						openFrom = "listDoneAll";
					}
				}
			} catch (BusinessException e) {
				LOGGER.info("全文检索穿透报错" + e.getMessage());
			}
    	}
    	
    	EdocSummaryBO edocSummaryVo = new EdocSummaryBO();
    	try {
    		edocSummaryVo = edocH5Manager.getEdocSummaryBO(summaryId, affairId, openFrom, params);
    		
    		if(edocSummaryVo.getErrorRet().size() > 0) {
    			return ok(edocSummaryVo.getErrorRet());
    		}
    		
    		//当前登录信息
    		edocSummaryVo.setCurrentUser(user);
    		
    		//待办数据
    		if(EdocSummaryType.listPending.name().equals(edocSummaryVo.getListType())) {
    			CtpAffair affair = edocSummaryVo.getAffairObj(); 
	    		EdocSummary summary = edocSummaryVo.getSummaryObj();
    			
	    		//Office正文M3端缓存设置
	    		if(Strings.isNotEmpty(summary.getEdocBodies())){
	    		    EdocBody b = summary.getEdocBodies().iterator().next();
	    		    if(!com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_HTML.equals(b.getContentType())
	    		            && Strings.isNotBlank(b.getContent())){
	    		        V3XFile f = fileManager.getV3XFile(Long.valueOf(b.getContent()));
	    		        if(f != null){
	    		        	edocSummaryVo.setBodyLastModify(DateUtil.formatDateTime(f.getUpdateDate()));
	    		        }
	    		    }
	    		}
	    		
	    		
    			/*************************************M3文单签批开始******************************************/
    			//当前节点如果有文单签批权限
    			if(edocSummaryVo.getActions().contains("HtmlSign") && affairId != -1L) {
		    		V3xHtmDocumentSignature hsSignature = htmSignetManager.getBySummaryIdAffairIdAndType(summary.getId(), affairId, V3xHtmSignatureEnum.HTML_SIGNATURE_DOCUMENT.key());//.getByAffairId(affairId);
		    		String fildValue = "";
		    		if(hsSignature!=null){
		    			try {
		    				fildValue = MSignaturePicHandler.encodeSignatureDataForStandard(hsSignature.getFieldValue());
						} catch (IOException e) {
							LOGGER.error("文单签批解密出错：", e);
						}
		    		}
		    		edocSummaryVo.setFiledValue(fildValue);		
	    		}
    			String[] nodePolicyFromWorkflow = null;
        		if(affair != null && affair.getActivityId() !=null) {
        		    nodePolicyFromWorkflow= wapi.getNodePolicyIdAndName(ApplicationCategoryEnum.edoc.name(), edocSummaryVo.getSummaryObj().getProcessId(), String.valueOf(edocSummaryVo.getAffairObj().getActivityId()));
        		}
        		String nodePermissionPolicy = "shenpi";
        		//得到当前处理权限录入意见的显示位置
                if(nodePolicyFromWorkflow != null){ 
                	nodePermissionPolicy = nodePolicyFromWorkflow[0];
                	//流程取过来的权限名，替换特殊空格[160 -> 32]
                	if(nodePermissionPolicy != null){
                		nodePermissionPolicy=nodePermissionPolicy.replaceAll(new String(new char[]{(char)160}), " ");
                	}
                }
        		String disPosition=edocFormManager.getOpinionLocation(summary.getFormId(), EdocHelper.getFlowPermAccountId(summary, user.getLoginAccount())).get(nodePermissionPolicy);
        		if(disPosition!=null){
    				String[] dis = disPosition.split("[_]");
    				disPosition = dis[0];
    			}
        		edocSummaryVo.setDisPosition(disPosition);
        		if(affair != null){
        			edocSummaryVo.setNodePolicy(affair.getNodePolicy());
        		}        		
    		}
    		
    		/*************************************M3文单签批结束******************************************/
    	} catch (BusinessException e) {
			LOGGER.error("", e);
		}
    	//同步消息
        userMessageManager.updateSystemMessageStateByUserAndReference(AppContext.currentUserId(), affairId);
        
        return ok(edocSummaryVo);
    }
	
	/**
	 * 获取公文的节点权限列表
	 * url: edocResource/permissions
	 * @param   获取公文的节点权限列表参数    
	 * 类型             名称                              必填           备注 
	 * String  type        		Y    类型
	 * String  policyName  		Y    节点名称
	 * Long    orgAccountId   	Y    单位Id
	 * <pre>
	 *    类型 如 ：协同（collaboration）、表单(form)、发文(sendEdoc||edocSend)、收文(recEdoc||edocRec)、签报(edocSign||signReport)、信息报送(sendInfo)
	 * </pre>
	 * @return  List<Permission>
	 * @throws BusinessException
	 */
	@GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("permissions")
    public Response permissions(@DefaultValue("sendEdoc") @QueryParam("type") String type
                              , @QueryParam("policyName") String policyName,@QueryParam("orgAccountId") Long orgAccountId) throws BusinessException {

        User user = AppContext.getCurrentUser();
        
        String appName = type;
        Long accountId = user.getLoginAccount();
        if(null != orgAccountId){
        	accountId =  orgAccountId;
        }
        boolean isTemplate = false;
        
        List<Permission> result = getPermissions4WFNodeProperties(appName, policyName, accountId, isTemplate);
        
        return ok(PermissionMiniVO.valueOf(result));
    }
    
	/**
	 * 获取常用语
	 * url : edocResource/phrase
	 * @return  List<String>
	 */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("phrase")
    public Response getPhrase() {
    	List<CommonPhrase> phrases = new ArrayList<CommonPhrase>();
		try {
			phrases = phraseManager.getAllPhrases();
		} catch (BusinessException e) {
			LOGGER.error("", e);
		}
    	List<String> phraseNames = new ArrayList<String>();
    	for(CommonPhrase commonPhrase: phrases){
    		phraseNames.add(commonPhrase.getContent());
    	}
    	return getResponse(phraseNames);
    }
    
    /**
     * 处理
     * url: edocResource/submit
     * @param param 处理参数
     * <pre>
     * 	类型                 名称                                 必填           备注
     * 	long     summaryId        Y 		协同ID
     * 	Long     affairId         Y 		事项ID
     * 	String   opinionContent   Y 		处理意见
     * 	Integer  opinionAttibute  Y 		处理态度
     *    				-1 （没有录入态度时的默认值）
     *    				 1  已阅
     *   				 2  同意
     *    				 3  不同意
     * 	String    disPosition   	 N 		当前节点   转收文使用，当"report".equals(disPosition) 为下级单位公文向上级汇报意见
     * 	Integer   isTrack      	 Y 		跟踪
     *    				 1  跟踪
     *    				 2  不跟踪
     * 	boolean   isNewImg             Y 是否有文单签批
     * 	Map       workflow_definition  Y 流程相关数据
     * 	String    fileJson             N 附件信息串
     * 	String    oldOpinionIdStr      Y 之前的意见记录
     * </pre>
     * @return  Boolean true:处理成功 ，false:处理失败
     */
    @SuppressWarnings("unchecked")
	@POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("submit")
    public Response submit(Map<String,Object> param) {
    	User user = AppContext.getCurrentUser();
    	long summaryId = ParamUtil.getLong(param, "summaryId", -1L); 
        Long affairId = ParamUtil.getLong(param, "affairId", -1L);
        String opinionContent = ParamUtil.getString(param, "opinionContent");
        
        Long lockUserId = null;
        if(null != affairId){
            lockUserId = edocLockManager.canGetLock(affairId, user.getId());
            if (lockUserId != null ) {
                // 防止重复提交
                throw new RuntimeException("当前事项后台正在自动处理， 请稍后再操作。");
            }
        }
        
        boolean returnValue = true;
        
        try {
            
            if(Strings.isNotBlank(opinionContent)){
                opinionContent = Strings.removeEmoji(opinionContent);
            }
            Integer opinionAttibute = ParamUtil.getInt(param, "opinionAttibute",-1);
            String disPosition = ParamUtil.getString(param, "disPosition", "");
            String optionWay = ParamUtil.getString(param, "optionWay", "");
            Integer isTrack = ParamUtil.getInt(param, "isTrack");
            boolean isNewImg=checkisNewImg(param);
            if(isNewImg){//是否有文单签批
            	saveQianpiData(param,affairId,isNewImg);//文单签批
            }
          //流程相关数据
            Map<String, String> wfParamMap =  (Map<String, String>) param.get("workflow_definition");
            wfParamMap.put("fileJson", ParamUtil.getString(param, "fileJson","[]"));
            
            String processChangeMessage = ParamUtil.getString(wfParamMap, "processChangeMessage", "");   
            
            EdocManagerModel edocManagerModel = new EdocManagerModel();
            edocManagerModel.setAffairId(affairId);
            edocManagerModel.setOldOpinionIdStr(ParamUtil.getString(param, "oldOpinionIdStr", ""));
            
            EdocOpinion signOpinion = new EdocOpinion(); 
            signOpinion.setAffairId(affairId);
            signOpinion.setContent(opinionContent);
            signOpinion.setAttribute(opinionAttibute);
            signOpinion.setIdIfNew();
            
            signOpinion.isPipeonhole = false;
            if(isTrack == 0){
            	signOpinion.affairIsTrack = false;
            }
            if("report".equals(disPosition)) {
            	signOpinion.setIsReportToSupAccount(Boolean.TRUE);
            }
            edocManagerModel.setSignOpinion(signOpinion);
            edocManagerModel.setSummaryId(summaryId);
            edocManagerModel.setUser(user);
            edocManagerModel.setAffairTrack(isTrack);
            edocManagerModel.setProcessChangeMessage(processChangeMessage);
            edocManagerModel.setOptionWay(optionWay);
        
        
			edocH5Manager.transDealEdoc(edocManagerModel, wfParamMap);
		} catch (BusinessException e) {
			returnValue = false;
			LOGGER.error(e);
		} finally {
		    
		 // 解锁
            edocLockManager.unlock(affairId);
		    
			edocManager.unlockEdocAll(summaryId, null);
		}
    	return getResponse(returnValue);
    }
    
    private boolean checkisNewImg(Map<String,Object> param){
    	String _isNewImg= ParamUtil.getString(param, "isNewImg");
        boolean isNewImg=false;
        if(Strings.isNotBlank(_isNewImg)){
        	isNewImg=Boolean.valueOf(_isNewImg);
        }
        return isNewImg;
    }
    //保存签批数据
    private void saveQianpiData(Map<String,Object> param,Long affairId,boolean isNewImg){
    	String qianpiData = ParamUtil.getString(param, "qianpiData");
    	try {
			mSignatureManager.transSaveSignatureAndHistory(qianpiData,String.valueOf(affairId),String.valueOf(isNewImg));
		} catch (BusinessException e) {
			LOGGER.error(e);
		}
	}
    
    /**
     * 暂存待办 
     * url: edocResource/zcdb
     * @param param  暂存待办参数
     * <pre>
     * 	类型                 名称                                 	      必填           备注
     *  long      summaryId   			Y 	协同ID
     *  Long      affairId    			Y 	事项ID
     *  String    opinionContent    	Y 	处理意见
     *  Integer   opinionAttibute   	Y 	处理态度
     *   			 -1 （没有录入态度时的默认值）
     *   			  1  已阅
     *    			  2  同意
     *    			  3  不同意
     *  Integer   isTrack      		    Y 	跟踪
     *    			  1  跟踪
     *       	      2  不跟踪
     *  boolean   isNewImg              Y 	是否有文单签批
     *  String    processChangeMessage  Y 	流程改变信息
     *  Map       workflow_definition   Y 	流程相关数据
     *  String    fileJson              N 	附件信息串
     *  String    oldOpinionIdStr       Y  	之前的意见记录
     * </pre>
     * @return  Boolean true:暂存待办成功 ，false:暂存待办失败
     */
    @SuppressWarnings("unchecked")
	@POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("zcdb")
    public Response zcdb(Map<String,Object> param){
    	User user =AppContext.getCurrentUser();
    	long summaryId = ParamUtil.getLong(param, "summaryId", -1L); 
        Long affairId = ParamUtil.getLong(param, "affairId", -1L);
        String opinionContent = ParamUtil.getString(param, "opinionContent");
        if(Strings.isNotBlank(opinionContent)){
            opinionContent = Strings.removeEmoji(opinionContent);
        }
        
        Integer opinionAttibute = ParamUtil.getInt(param, "opinionAttibute",-1);
        Integer isTrack = ParamUtil.getInt(param, "isTrack");
        
        String processChangeMessage = ParamUtil.getString(param,"processChangeMessage");   
        
        boolean isNewImg=checkisNewImg(param);
        if(isNewImg){//是否有文单签批
        	saveQianpiData(param,affairId,isNewImg);//文单签批
        }
        
        //流程相关数据
        Map<String, String> wfParamMap =  (Map<String, String>) param.get("workflow_definition");
        wfParamMap.put("fileJson", ParamUtil.getString(param, "fileJson","[]"));
        
        EdocManagerModel edocManagerModel = new EdocManagerModel();
        edocManagerModel.setAffairId(affairId);
        edocManagerModel.setOldOpinionIdStr(ParamUtil.getString(param, "oldOpinionIdStr", ""));
        
        EdocOpinion signOpinion = new EdocOpinion(); 
        signOpinion.setAffairId(affairId);
        signOpinion.setContent(opinionContent);
        signOpinion.setAttribute(opinionAttibute);
        signOpinion.setIdIfNew();
        signOpinion.isPipeonhole = false;
        edocManagerModel.setSignOpinion(signOpinion);
        edocManagerModel.setSummaryId(summaryId);
        edocManagerModel.setUser(user);
        edocManagerModel.setProcessChangeMessage(processChangeMessage);
        edocManagerModel.setAffairTrack(isTrack);
        Boolean returnValue = true;
        try {
        	returnValue = edocH5Manager.transDoZCDB(edocManagerModel,user, wfParamMap);
		} catch (BusinessException e) {
			returnValue = false;
			LOGGER.error("", e);
		} finally {
			edocManager.unlockEdocAll(summaryId, null);
		}
    	return getResponse(returnValue);
    }
    
    /**
     * 回退
     * url: edocResource/stepback
     * 
     * @param param 回退参数
     * <pre>
     * 	类型                  		名称                                 	 必填           备注
     *  long 			summaryId   		 Y 		协同ID
     *  Long 			affairId    		 Y 		事项ID
     *  String 			opinionContent   	 Y 		处理意见
     *  Integer 		opinionAttibute  	 Y 		处理态度
     *   					-1 （没有录入态度时的默认值）
     *    					1  已阅
     *   				 	2  同意
     *    					3  不同意
     *  Integer         isTrack      		 N 		跟踪
     *    					1  跟踪
     *    					2  不跟踪
     *  boolean         isNewImg             Y      是否有文单签批
     *  String        processChangeMessage   Y 		流程改变信息
     *  String             policy            N      节点权限
     * </pre>
     * @return  String true:回退成功 ，false:回退失败
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("stepback")
    public Response stepback(Map<String,Object> param){
    	User user =AppContext.getCurrentUser();
    	Long summaryId = ParamUtil.getLong(param, "summaryId", -1L); 
        Long affairId = ParamUtil.getLong(param, "affairId", -1L);
        String opinionContent = ParamUtil.getString(param, "opinionContent");
        if(Strings.isNotBlank(opinionContent)){
            opinionContent = Strings.removeEmoji(opinionContent);
        }
        Integer opinionAttibute = ParamUtil.getInt(param, "opinionAttibute",-1);
        Integer isTrack = ParamUtil.getInt(param, "isTrack", TrackEnum.no.ordinal());
        String processChangeMessage=ParamUtil.getString(param,"processChangeMessage");    
        boolean isNewImg=checkisNewImg(param);
        if(isNewImg){//是否有文单签批
        	saveQianpiData(param,affairId,isNewImg);//文单签批
        }
        EdocOpinion signOpinion = new EdocOpinion(); 
        signOpinion.setAffairId(affairId);
        signOpinion.setContent(opinionContent);
        signOpinion.setAttribute(opinionAttibute);
        signOpinion.setCreateUserId(user.getId());
        signOpinion.setIdIfNew();
        signOpinion.isPipeonhole = false;
        signOpinion.setPolicy(ParamUtil.getString(param, "policy"));
        
        Map<String, Object> returnMap = new HashMap<String, Object>();
        String returnValue = "true";
        String errMsg = "";
        try {
			CtpAffair affair = affairManager.get(affairId);
			if(affair!=null){
				signOpinion.setNodeId(affair.getActivityId());
				signOpinion.setPolicy(affair.getNodePolicy());
			}
			if(affair == null){
			    returnMap.put("returnValue", "false");
                returnMap.put("errMsg", "affair is null,can`t do this operation!");
                return getResponse(returnMap);
			}
			EdocSummary summary = edocSummaryManager.findById(summaryId);
	    	if(summary.getFinished()) {
	    		Long flowPermAccountId = EdocHelper.getFlowPermAccountId(summary, summary.getOrgAccountId());
	    		String[] result = edocManager.edocCanStepBack(String.valueOf(affair.getSubObjectId()), String.valueOf(summary.getProcessId()), String.valueOf(affair.getActivityId()), String.valueOf(summary.getCaseId()), String.valueOf(flowPermAccountId), EdocEnum.getEdocAppName(summary.getEdocType()));
	    		if(!"true".equals(result[0])) {
        			errMsg = ResourceUtil.getString("edoc.stepback.cannot","《"+summary.getSubject()+"》");
    		        returnMap.put("returnValue", "false");
    		        returnMap.put("errMsg", errMsg);
    		    	return getResponse(returnMap);
	    		}
	    	}
		} catch (BusinessException e1) {
			LOGGER.error("H5公文回退异常", e1);
		}
        
        EdocManagerModel edocManagerModel = new EdocManagerModel();
        edocManagerModel.setAffairId(affairId);
        edocManagerModel.setSignOpinion(signOpinion);
        edocManagerModel.setSummaryId(summaryId);
        edocManagerModel.setUser(user);
        edocManagerModel.setProcessChangeMessage(processChangeMessage);
        edocManagerModel.setAffairTrack(isTrack);

        //保存附件
        String fileJson = ParamUtil.getString(param, "fileJson","[]");
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("fileJson", fileJson);
        try {
        	returnValue = edocH5Manager.transStepback(edocManagerModel,paramMap);
		} catch (BusinessException e) {
			returnValue = "false";
			LOGGER.error("H5公文回退异常", e);
		} finally {
			edocManager.unlockEdocAll(summaryId, null);
		}
        returnMap.put("returnValue", returnValue);
    	return getResponse(returnMap);
    }
    
    /**
     * 是否能进行回退
     * url: edocResource/canStepBack
     * 
     * @param param 回退参数
     * <pre>
     * 	类型                  		名称                                 	 必填           备注
     *  Long 			affairId    		 Y 		事项ID
     * </pre>
     * @return  Map 
     * 			map.canStepBack true:可以回退 ，false:不可以回退
     * 			map.error_msg    不可以回退是返回不可回退的信息，可以回退时返回空
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("canStepBack")
    public Response canStepBack(Map<String,Object> param){
        Long affairId = ParamUtil.getLong(param, "affairId", -1L);
        Map<String, Object> returnMap = new HashMap<String, Object>();
        returnMap.put("canStepBack", "true");
        try {
			CtpAffair affair = affairManager.get(affairId);
			EdocSummary summary = edocSummaryManager.findById(affair.getObjectId());
    		Long flowPermAccountId = EdocHelper.getFlowPermAccountId(summary, summary.getOrgAccountId());
    		String[] result = edocManager.edocCanStepBack(String.valueOf(affair.getSubObjectId()), String.valueOf(summary.getProcessId()), String.valueOf(affair.getActivityId()), String.valueOf(summary.getCaseId()), String.valueOf(flowPermAccountId), EdocEnum.getEdocAppName(summary.getEdocType()));
    		if(!"true".equals(result[0])) {
		        returnMap.put("canStepBack", "false");
		        returnMap.put("error_msg", result[1]);
		    	return getResponse(returnMap);
    		}
		} catch (BusinessException e1) {
			LOGGER.error("H5验证公文是否可以回退异常", e1);
			returnMap.put("canStepBack", "false");
			returnMap.put("error_msg", "affair error!");
		}
    	return getResponse(returnMap);
    }
	
    /**
     * 撤销
     * url: edocResource/cancel
     * 
     * @param param 撤销参数
     * <pre>
     * 	类型                  		名称                                 	 必填           备注
     *  long            summaryId  			Y 		协同ID
     *  Long            affairId    		Y 		事项ID
     *  String          opinionContent    	Y 		处理意见
     *  Integer         opinionAttibute  	Y	 	处理态度
     *    				-1 （没有录入态度时的默认值）
     *    				1  已阅
     *    				2  同意
     *    				3  不同意
     *  Integer         isTrack      		N 		跟踪
     *    				1  跟踪
     *   			    2  不跟踪
     *  boolean         isNewImg     		Y		 是否有文单签批
     *  String      processChangeMessage    Y 		流程改变信息
     *  String          policy      		N 		节点权限
     * </pre>
     * @return  map 
     * 			<pre>
     * 				成功{returnValue : true}
     * 				失败{returnValue : false}
     * 			</pre>
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("cancel")
    public Response cancel(Map<String,Object> param){
    	User user =AppContext.getCurrentUser();
    	Long summaryId = ParamUtil.getLong(param, "summaryId", -1L); 
        Long affairId = ParamUtil.getLong(param, "affairId", -1L);
        String opinionContent = ParamUtil.getString(param, "opinionContent");
        if(Strings.isNotBlank(opinionContent)){
            opinionContent = Strings.removeEmoji(opinionContent);
        }
        Integer opinionAttibute = ParamUtil.getInt(param, "opinionAttibute",-1);
        Integer isTrack = ParamUtil.getInt(param, "isTrack", TrackEnum.no.ordinal());
        String processChangeMessage=ParamUtil.getString(param,"processChangeMessage");    

        String returnValue = "true";
        String errMsg = "";
        try {
			EdocSummary summary = edocManager.getEdocSummaryById(summaryId, false);
			if(summary != null && summary.getFinished()){
				errMsg = ResourceUtil.getString("edoc.cancel.cannot");
    			Map<String, Object> returnMap = new HashMap<String, Object>();
		        returnMap.put("returnValue", "false");
		        returnMap.put("errMsg", errMsg);
		    	return getResponse(returnMap);
			}
		} catch (BusinessException e2) {
			LOGGER.error("H5获取主表信息异常", e2);
		}
        
        boolean isNewImg=checkisNewImg(param);
        if(isNewImg){//是否有文单签批
        	saveQianpiData(param,affairId,isNewImg);//文单签批
        }
        EdocOpinion signOpinion = new EdocOpinion(); 
        signOpinion.setAffairId(affairId);
        signOpinion.setContent(opinionContent);
        signOpinion.setAttribute(opinionAttibute);
        signOpinion.setCreateUserId(user.getId());
        signOpinion.setIdIfNew();
        signOpinion.isPipeonhole = false;
        signOpinion.setPolicy(ParamUtil.getString(param, "policy"));
        try {
			CtpAffair affair = affairManager.get(affairId);
			if(affair!=null){
				if(affair.getActivityId() != null) {
					signOpinion.setNodeId(affair.getActivityId());
				}
				signOpinion.setPolicy(affair.getNodePolicy());
			}
		} catch (BusinessException e1) {
			LOGGER.error("H5公文回退异常", e1);
		}
        
        EdocManagerModel edocManagerModel = new EdocManagerModel();
        edocManagerModel.setAffairId(affairId);
        edocManagerModel.setSignOpinion(signOpinion);
        edocManagerModel.setSummaryId(summaryId);
        edocManagerModel.setUser(user);
        edocManagerModel.setProcessChangeMessage(processChangeMessage);
        edocManagerModel.setAffairTrack(isTrack);

        //保存附件
        String fileJson = ParamUtil.getString(param, "fileJson","[]");
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("fileJson", fileJson);
        try {
        	returnValue = edocH5Manager.transRepeal(edocManagerModel,paramMap);
        	if("isNull".equals(returnValue)){
        		returnValue = "false";
        	}else if("isDelete".equals(returnValue)){
        		returnValue = "false";
        	}else if("isBack".equals(returnValue)){
        		returnValue = "false";
        	}else if("isStop".equals(returnValue)){
        		returnValue = "false";
        	}else if("isDone".equals(returnValue)){
        		returnValue = "false";
        	}
		} catch (BusinessException e) {
			returnValue = "false";
			LOGGER.error("", e);
		} finally {
			edocManager.unlockEdocAll(summaryId, null);
		}
        Map<String, Object> returnMap = new HashMap<String, Object>();
        returnMap.put("returnValue", returnValue);
    	return getResponse(returnMap);
    }
    
    /**
     * 取回
     * 
     * url: edocResource/takeBack
     * 
     * @param param 取回参数
     * <pre>
     * 	类型                  		名称                                 	 必填           备注
     *  long            summaryId  			Y 		协同ID
     *  Long            affairId    		Y 		事项ID
     * </pre>
     * @return  String 0:取回成功，15:取回失败
     * @throws BusinessException
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("takeBack")
    public Response takeBack(Map<String, Object> params) throws BusinessException {
    	String message = "0";
    	
    	Long affairId = ParamUtil.getLong(params, "affairId", 0l);
        Long summaryId = ParamUtil.getLong(params, "summaryId", 0l);
        String processId = "";
        boolean isBackOk = true;
        EdocSummary summary = null;
        try{
	        if (affairId != 0) {
                summary=edocManager.getEdocSummaryById(summaryId,false);
				processId = summary.getProcessId();
                CtpAffair affair = affairManager.get(affairId);
                
                //0-12
                message = edocManager.canTakeBack(ApplicationCategoryEnum.edoc.name(), String.valueOf(processId),
                		String.valueOf(affair.getActivityId()), String.valueOf(affair.getSubObjectId()), String.valueOf(affairId));
                //只有0的时候才能取回
                if ("0".equals(message)) {
	                isBackOk = edocManager.takeBack(affairId);
	                
	                //取回后，更新当前待办人
	                summary.setFinished(false);//被取回设置结束为false
	                summary.setCompleteTime(null);
	                //跟新当前待办人
	                EdocHelper.updateCurrentNodesInfo(summary,true);
	                
	                //取回失败
	                if(!isBackOk && affair!=null){
	    	        	message = "15";
	    	        }
                }
	        }
        }catch(Exception e){
        	LOGGER.error("公文取回时抛出异常：", e);
        } finally {
			edocManager.unlockEdocAll(summaryId, summary);
		}
    	return ok(message);
    }
    
    /**
     * 终止
     * url: edocResource/stepStop
     * 
     * @param param 取回参数
     * <pre>
     * 	类型                  		名称                                 	 必填           备注
     *  long            summaryId  			Y 		协同ID
     *  Long            affairId    		Y 		事项ID
     *  String			opinionContent      Y 		处理意见
     *  String          afterSign           Y 		处理态度
     *    				-1 （没有录入态度时的默认值）
     *    				1  已阅
     *    				2  同意
     *    				3  不同意
     *  String          isHidden          	N 		是否隐藏
     *  String          policy      		N 		节点权限
     * </pre>
     * @return true:终止成功
     *         null:终止失败
     * @throws BusinessException
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("stepStop")
    public Response stepStop(Map<String, Object> params) throws BusinessException{
    	User user = AppContext.getCurrentUser();
        EdocSummary summary = null;
        
        Long affairId = ParamUtil.getLong(params, "affairId", 0l);
        Long summaryId = ParamUtil.getLong(params, "summaryId", 0l);
        String content = ParamUtil.getString(params, "opinionContent","");
        if(Strings.isNotBlank(content)){
            content = Strings.removeEmoji(content);
        }
        //态度
        String afterSign = ParamUtil.getString(params, "afterSign","");
        String isHidden = ParamUtil.getString(params, "isHidden",null);
        //Long currentNodeId = ParamUtil.getLong(params, "currentNodeId", null);
        try{
	        
	        CtpAffair _affair = affairManager.get(affairId);
	        //当公文不是待办/在办的状态时，不能终止操作
	        if(_affair.getState() != StateEnum.col_pending.key()){
	            String msg = EdocHelper.getErrorMsgByAffair(_affair);
	            return ok(msg);
	        }
	        boolean isNewImg=checkisNewImg(params);
	        if(isNewImg){//是否有文单签批
	        	saveQianpiData(params,affairId,isNewImg);//文单签批
	        }
	        summary = edocManager.getEdocSummaryById(summaryId, true);
	        //保存终止时的意见,附件
	        EdocOpinion signOpinion = new EdocOpinion();
	        //TODO 2016-6-7 bind(request, signOpinion);
	        signOpinion.setContent(content);
	        //设置态度显示终止
	        signOpinion.setAttribute(OpinionType.stopOpinion.ordinal());
	        signOpinion.isDeleteImmediate = "delete".equals(afterSign);
	        signOpinion.affairIsTrack = "track".equals(afterSign);
	
	        signOpinion.setAffairId(affairId);
	        signOpinion.setPolicy(ParamUtil.getString(params, "policy"));
	        signOpinion.setIsHidden(isHidden != null);
	        signOpinion.setIdIfNew();
	        
	        if(_affair != null) {
	        	signOpinion.setNodeId(_affair.getActivityId());
	        	signOpinion.setPolicy(_affair.getNodePolicy());
	        }
	        
	      	//设置代理人信息
	        if(user.getId().longValue()!= _affair.getMemberId().longValue()) {
	        	signOpinion.setProxyName(user.getName());
	        }
	        signOpinion.setCreateUserId(_affair.getMemberId());

	        /*TODO //修改公文附件
            AttachmentEditHelper editHelper = new AttachmentEditHelper(request);
            if(editHelper.hasEditAtt()){//是否修改附件
            	attachmentManager.deleteByReference(summaryId, summaryId);//删除公文附件
            }
            //保存公文附件及回执附件，create方法中前台subReference传值为空，默认从java中传过去， 因为公文附件subReference从前台js传值 过来了，而回执附件没有传subReference，所以这里传回执的id
            this.attachmentManager.create(ApplicationCategoryEnum.edoc, summaryId, signOpinion.getId(), request);
            if(editHelper.hasEditAtt()){//是否修改附件
            	//设置summary附件标识,修改affair附件标识，设置附件元素，修改附件日志
                EdocHelper.updateAttIdentifier(summary, editHelper.parseProcessLog(Long.parseLong(processId), currentNodeId), true, "stepStop");
            } */
	        //保存附件
	        String relateInfo = ParamUtil.getString(params, "fileJson","[]");//ParamUtil.getString(wfParamMap, "fileJson", "[]");
	        List<Map> files = JSONUtil.parseJSONString(relateInfo, List.class);
	        try {
	        	List<Attachment> attList = attachmentManager.getAttachmentsFromAttachList(ApplicationCategoryEnum.edoc, summaryId, signOpinion.getId(), files);
	        	if(!attList.isEmpty()){
	        		attachmentManager.create(attList);
	        		signOpinion.setHasAtt(true);
	        		signOpinion.setOpinionAttachments(attList);
	        	}
			} catch (Exception ex) {
				LOGGER.error("",ex);
				throw new BusinessException(ex);
			}
            
	        Map<String,Object> map = new HashMap<String,Object>();
	        map.put("edocOpinion", signOpinion);
	        map.put("summaryId", summaryId);
	        
	        edocManager.transStepStop( affairId,map);  
	       
        	return ok(true);
        }catch(Exception e){
        	LOGGER.error("公文终止时抛出异常：", e);
        } finally {
			edocManager.unlockEdocAll(summaryId, summary);
		}
        return null;
    }
	
    /** 
     * 处理时验证，非查看校验
     * url: edocResource/checkAffairValid
     * 
     *  @param  处理时验证，非查看校验参数
     *  <pre>
     * 		类型                  		名称                                 	 必填           备注
     *  	String            affairId    		Y 		事项ID
     *      String 			  pageNodePolicy    Y       页面的节点权限id    
     *  </pre>
     * @return  Map<String, String>
     * 						<pre>
     * 							正常：返回值 {isOnlyView:false}
     * 							异常：返回值{isOnlyView:false,
     * 									  error_msg:  事项已被删除}
     * 							备注：isOnlyView始终是false,含义是一个入口的标识，代表从这个接口进入的
     * 							Map中是否含有等于error_msg的key，value的值是 事项已被删除，撤销，终止；表示affairs异常
     * 						</pre> 
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("checkAffairValid")
    public Response checkAffairValid(@QueryParam("affairId") String affairId,@QueryParam("pageNodePolicy") String pageNodePolicy) throws BusinessException {
    	Map<String, String> ret = new HashMap<String, String>();
    	ret.put("isOnlyView", "false");
    	if(Strings.isBlank(affairId)) {
    		affairId = "-1";
    	}
        edocH5Manager.isValidAffair(Long.parseLong(affairId),pageNodePolicy, ret);
        return ok(ret);
    }
    
    /**
     * 验证公文是否已交换
     * 
     * url: edocResource/checkCanTakeBack
     * @param 验证公文是否已交换参数
     * 
     *  <pre>
     * 		类型                  		    名称                                 	 必填           备注
     *  	String            summaryId    		 Y 		协同ID
     *  </pre> 
     * @return  boolean true:未交换，false:已交换
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("checkCanTakeBack")
    public Response checkIsExchanged(@QueryParam("summaryId") String summaryId) throws BusinessException {
    	boolean ret = true;
    	if(Strings.isNotBlank(summaryId)) {
    		ret = !edocManager.isBeSended(Long.parseLong(summaryId));
    	}
        return ok(ret);
    }
    
    /**
     * 校验公文文号
     * 
     * url: edocResource/checkEdocMarkIsUsed
     * @param 校验公文文号参数
     * 
     *  <pre>
     * 		类型                  		    名称                                 	 必填           备注
     *  	String            summaryId    		 Y 		协同ID
     *      String			  docMark            Y      公文文号
     *      String			  orgAccountId       Y      单位ID
     *  </pre>
     * @return  boolean true:成功，false:失败
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("checkEdocMarkIsUsed")
    public Response checkEdocMarkIsUsed(@QueryParam("summaryId") String summaryId, @QueryParam("docMark")String docMark, @QueryParam("orgAccountId")String orgAccountId) throws BusinessException {
    	boolean ret = edocH5Manager.checkEdocMarkisUsed(docMark, summaryId, orgAccountId); 
    	return ok(ret);
    }
    
    /**
     * 校验公文能否取回
     * url: edocResource/checkTakeBack
     * @param 校验公文能否取回参数
     * 
     *  <pre>
     * 		类型                  		    名称                                 	 必填           备注
     *  	String            affairId    		 Y 		事项ID
     *  </pre>
     * @return  String   
     * 					<pre>
     * 						-1  :  表示程序或数据发生异常,不可以取回
     * 						0  :   可以取回
     * 						1  :   当前流程已经结束,不可以取回
     * 						2  :   后面节点任务事项已处理完成,不可以取回
     * 						3  :   当前节点触发的子流程已经结束,不可以取回
     * 						4  :   当前节点触发的子流程中已核定通过,不可以取回
     *                      5  :   当前节点是知会节点,不可以取回
     *                      6  :   当前节点为核定节点,不可以取回
     * 						8  :   后面节点任务事项处于指定回退状态，不可以取回
     * 						11 :   公文已经被撤销，不能取回
     * 						12 :   回退公文已经被回退，不能取回
     * 					</pre>
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("checkTakeBack")
    public Response checkTakeBack(@QueryParam("affairId") String affairId) throws BusinessException {
    	String message = "0";
        if (Strings.isNotBlank(affairId) && !"0".equals(affairId) ) {
        	String processId = "";
        	Long currentAffairId = Long.parseLong(affairId);
        	try{
        		CtpAffair affair = affairManager.get(currentAffairId);
        		if(affair == null) {
        		    return ok("-1");
        		}
    			EdocSummary summary = edocManager.getEdocSummaryById(affair.getObjectId(), false);
            	if(summary != null) {
            		processId = summary.getProcessId();
            	}
            	message = edocManager.canTakeBack(ApplicationCategoryEnum.edoc.name(), String.valueOf(processId), String.valueOf(affair.getActivityId()), String.valueOf(affair.getSubObjectId()), String.valueOf(affairId));
			} catch(Exception e) {
	        	LOGGER.error("公文取回时抛出异常：", e);
	        }
        }        
    	return ok(message);
    }
    /**
     * 解全部锁
     * url: edocResource/unlockEdocAll
     * @param 解全部锁参数
     * 
     *  <pre>
     * 		类型                  		    名称                                 	 必填           备注
     *  	String            summaryId    		 Y 		协同ID
     *  </pre>
     * @return  String true:成功解全部锁
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("unlockEdocAll")
    public Response unlockEdocAll(@QueryParam("summaryId") Long summaryId) throws BusinessException {
    	edocManager.unlockEdocAll(summaryId, null);
    	return ok("true");
    }
    
    /**
     * 
     * @param user
     * @param edocType
     * @param state
     * @param listType
     * @return
     */
	private Map<String, Object> createCondition(User user, Integer edocType, int state,String listType){
		Map<String, Object> condition = new HashMap<String, Object>();
		condition.put("track", -1);
		condition.put("state", state);
		if(edocType != null){
		    condition.put("edocType", edocType);
		}
		condition.put("subEdocType", -1L); 
	    condition.put("type", 10);
	    condition.put("user", user);
	    condition.put("userId", user.getId());
	    condition.put("accountId", user.getLoginAccount());
	    condition.put("listType", listType);
	    condition.put("isOnlyPendingSurplusTime", true);
	    return condition;
	}
	
	/**
	 * 
	 * @param user
	 * @param edocType
	 * @param state
	 * @param listType
	 * @param conditionKey 搜索条件
	 * @param textfield	搜索传值
	 * @param textfield1
	 * @return
	 */
    private Map<String, Object> createNewCondition(User user,int edocType,int state,String listType,String conditionKey,String textfield,String textfield1){
    	Map<String, Object> condition = createCondition(user, edocType, state, listType);
	    condition.put("conditionKey",conditionKey);
	    condition.put("textfield",textfield);
	    condition.put("textfield1",textfield1);
	    return condition;
	}
    
    /**
	 * 公文待签收数据列表
	 * url: edocResource/signed
     * @param 公文待签收数据列表参数
     * 
     *  <pre>
     * 		类型                  		    名称                                 	 必填           备注
     *  	Long            memberId    		 Y 		人员ID
     *  </pre>
	 * @return List
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("signed")
	@RestInterfaceAnnotation
    public Response signed(@QueryParam("memberId") Long memberId) throws BusinessException{
		Map<String, Object> condition = new HashMap<String, Object>();
		V3xOrgMember memberById = orgManager.getMemberById(memberId);
		User user = new User();
		user.setId(memberId);
		String listType = "listExchangeToRecieve";
		int type = EdocNavigationEnum.EdocV5ListTypeEnum.getTypeName(listType);
		int state = Integer.parseInt(EdocNavigationEnum.EdocV5ListTypeEnum.getStateName(listType));
		condition.put("modelType", "toReceive");
		condition.put("state", state);
		condition.put("type", type);
		condition.put("user", user);
		condition.put("listType",listType);
		List findEdocExchangeRecordList = edocExchangeManager.findEdocExchangeRecordList(type, condition);
	    return ok(findEdocExchangeRecordList);
	}
    /**
	 * 公文待登记数据列表
	 * url: edocResource/registered
     * @param 公文待登记数据列表参数
     * 
     *  <pre>
     * 		类型                  		    名称                                 	 必填           备注
     *  	Long            memberId    		 Y 		人员ID
     *  </pre>
	 * @return List<EdocRegister>
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("registered")
	@RestInterfaceAnnotation
	public Response registered(@QueryParam("memberId") Long memberId){
		String condition ="";
		String[] value = new String[2];
		String listType = "listV5Register";
		User user  = new User();
		user.setId(memberId);
		user.setLoginAccount(AppContext.getCurrentUser().getLoginAccount());
		List<EdocRegister> findRegisterByState = edocRegisterManager.findRegisterByState(condition,value,EdocNavigationEnum.RegisterState.Registed.ordinal(),user);
	    return ok(findRegisterByState);
	}
    
    /**
     * 
     * @param object
     * @return
     */
    private Response getResponse(Object object){
    	String jsonStr = "";
    	if(null != object ){
    		jsonStr = JSONUtils.toJSON(object);
    	}
    	return ok(jsonStr);
    }
    
    /**
     * 当前人员的菜单权限（公文）
     * 
     * url: edocResource/edoc/user/privMenu
     * 
     * @return  Map<String,Object>
     * 						<pre>
     * 							haveEdocSend : false  没有发文管理权限  /  true 有发文权限
     * 							haveEdocSignReport : false 没有签报权限  / true 有签报权限
     * 							haveEdocRec :  false 没有收文权限   / true有收文权限
     * 						</pre>
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("edoc/user/privMenu")
    public Response edocUserPeivMenu() throws BusinessException {
    	
    	Map<String,Object> params = new HashMap<String,Object>();
    	User user = AppContext.getCurrentUser();
    	boolean haveEdocSend       = user.hasResourceCode("F20_govDocSendManage");  //发文管理
		boolean haveEdocSignReport = user.hasResourceCode("F20_signReport");   //签报管理
		boolean haveEdocRec        = user.hasResourceCode("F20_receiveManage");   //收文管理
    	
    	//G6添加判断角色下的已办，已发，待办，待发资源
    	boolean waitSendEdoc = user.hasResourceCode("F20_govDocWaitSend");  //待发公文
    	boolean doneEdoc = user.hasResourceCode("F20_govDocDone");  //已办公文
    	boolean SendEdoc = user.hasResourceCode("F20_gocDovSend");  //已发公文
    	boolean PendingEdoc = user.hasResourceCode("F20_govdocPending");  //待办公文
    	if(waitSendEdoc || doneEdoc || SendEdoc || PendingEdoc ){//兼容如果有G6 添加的上方4种资源，公文菜单都要显示
    		haveEdocSend = true;
    		haveEdocSignReport = true;
    		haveEdocRec = true;
    	}
		params.put("haveEdocSend", haveEdocSend);
		params.put("haveEdocSignReport", haveEdocSignReport);
		params.put("haveEdocRec", haveEdocRec);
		boolean haveAccountGovdocStat=orgManager.isRole(user.getId(), user.getAccountId(), OrgConstants.Role_NAME.AccountGovdocStat.name());//单位公文统计
		params.put("haveAccountGovdocStat", haveAccountGovdocStat);
    	return ok(params);
    }
    
    /**
     * 文单签批--加锁
     * url: edocResource/qianpiLock
     * @param 文单签批--加锁参数
     * 
     *  <pre>
     * 		类型                  		    名称                                 	 必填           备注
     *  	String            summaryId    		 Y 		协同ID
     *  </pre>
     * @return  UserUpdateObject
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("qianpiLock")
    public Response qianpiLock(@QueryParam("summaryId") String summaryId) throws BusinessException{
    	UserUpdateObject os = edocSummaryManager.editObjectState(summaryId);
    	return ok(os);
    }
    
    private static final String EXPORT_EDOC_ALL = "0";
   	private static final String EXPORT_EDOC_FORM = "1";
   	private static final String EXPORT_EDOC_BODY = "2";
   	/**
   	 * 根据公文id及导出类型导出公文到指定目录
   	 * @param params Map<String, Object> | 必填  | 其他参数
        * <pre>
        * summaryId     String  |  必填       |  公文ID
        * folder        String  |  必填       |  输出的目录
        * exportType    String  |  非必输   |  导出的类型
        *               0-全部；1-文单；2-正文(含花脸)
        *               不输入默认导出全部
        * </pre>
        * <pre>
   	 * @return Map<String, String>
   	 * 			<pre>
   	 * 				success: false 失败，true 成功
   	 * 				msg:结果描述
   	 * 			</pre>
   	 * @throws BusinessException
   	 */
   	@POST
   	@Produces({ MediaType.APPLICATION_JSON })
   	@Path("exportFile")
   	@RestInterfaceAnnotation
   	public Response exportEdocFile(Map<String, Object> param) throws BusinessException {
   		// 判断rest用户是否是单位公文收发员或者部门公文收发员
   		Map<String, Object> map = new HashMap<String, Object>();
   		if (GovdocRoleHelper.isAccountExchange() || GovdocRoleHelper.isDepartmentExchange()) {
   			return ok(this.exportFile(param));
   		} else {
   			map.put("success", false);
   			map.put("msg", ResourceUtil.getString("edoc.alert.no.export.label"));//"用户不是[单位公文收发员]或者[部门公文收发员]，不允许导出公文相关信息"
   			return ok(map);
   		}
   	}

   	/**
	 * 根据公文id及导出类型导出公文到指定目录
	 * @param param
	 * summaryId     String  |  必填       |  公文ID
     * folder        String  |  必填       |  输出的目录
     * exportType    String  |  非必输   |  导出的类型
     *               0-全部；1-文单；2-正文(含花脸)
     *               不输入默认导出全部
	 * @return
	 */
	private Map<String, Object> exportFile(Map<String, Object> param) {
		String summaryId = (String) param.get("summaryid");
		String folder = (String) param.get("folder");
		String exportType = (String) param.get("exportType");
		List<Long> bodyIds = new ArrayList<Long>();
		List<V3XFile> v3xList = new ArrayList<V3XFile>();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			if ((null != summaryId && !"".equals(summaryId)) && (null != folder && !"".equals(folder))) {
				if (null == exportType) {
					exportType = EXPORT_EDOC_ALL;
				}
				List<Long> fileList = new ArrayList<Long>();
				EdocSummary es = edocManager.getEdocSummaryById(Long.parseLong(summaryId), true);
				if (null != es) {
					Set<EdocBody> esb = es.getEdocBodies(); // 公文的正文不一定是一个，所以这里用循环取正文的花脸
					for (EdocBody edocBody : esb) {
						long ecid = Long.parseLong(edocBody.getContent());
						bodyIds.add(ecid);
						List<Long> obfList = officeBakFileManager.getOfficeBakFileIds(ecid);
						if (obfList.size() > 0) {
							fileList.addAll(obfList);
						}
						Long[] fids = new Long[obfList.size()];
						for (int i = 0; i < obfList.size(); i++) {
							fids[i] = obfList.get(i);
						}
						try {
							v3xList = fileManager.getV3XFile(fids);
							if (null != v3xList && !v3xList.isEmpty() && v3xList.size() > 1) {
							Collections.sort(v3xList, new Comparator<V3XFile>() {
								@Override
								public int compare(V3XFile file1, V3XFile file2) {
									if (file1.getCreateDate().compareTo(file2.getCreateDate()) > 0) {
										return -1;
									} else if (file1.getCreateDate().compareTo(file2.getCreateDate()) < 0) {
										return 1;
									} else {
										return 0;
									}
								}
							});
							}
						} catch (BusinessException e1) {
							throw new BusinessException(ResourceUtil.getString("edoc.alert.no.file.exception.label") + e1.getMessage());//获取文件异常：
						}
					}
					String root = folder.replaceAll("\\\\", "/").replaceAll("//", "/");
					if (!root.endsWith("/")) {
						root += File.separator;
					}
					String summaryRoot = root + summaryId;
					String edocformRoot = summaryRoot + File.separator + "edocform"; // 文单输出目录
					String edocbodyRoot = summaryRoot + File.separator + "edocbody"; // 正文(含花脸)输出目录
					File sf = new File(summaryRoot);
					if (sf.exists()) {
						try {
							FileUtils.deleteDirectory(sf);
						} catch (IOException e) {
							map.put("success", false);
							map.put("msg", ResourceUtil.getString("edoc.alert.file.operation.failed.label") + e.getMessage());//文件操作失败！原因：
						}
					}
					// 根据导出类型导出文单、正文(含花脸)
					if (EXPORT_EDOC_ALL.equals(exportType)) {
						map = this.write2File(Long.parseLong(summaryId), bodyIds, v3xList, edocbodyRoot);
						edocFormManager.writeForm2File(Long.parseLong(summaryId), edocformRoot);
					} else if (EXPORT_EDOC_FORM.equals(exportType)) {
						edocFormManager.writeForm2File(Long.parseLong(summaryId), edocformRoot);
					} else if (EXPORT_EDOC_BODY.equals(exportType)) {
						map = this.write2File(Long.parseLong(summaryId), bodyIds, v3xList, edocbodyRoot);
					}
					if (map.isEmpty()) {
						map.put("success", true);
						map.put("msg", ResourceUtil.getString("edoc.alert.file.output.address.label") + summaryRoot);//文件输出地址为：
					}
					return map;
				} else {
					throw new BusinessException(ResourceUtil.getString("edoc.alert.no.edoc.label"));//公文ID(summaryid)查询不到对应公文
				}
			} else {
				throw new BusinessException(ResourceUtil.getString("edoc.alert.summaryid.folder.null.label"));//公文ID(summaryid)及输出目录(folder)均不能为空
			}
		} catch (Exception e) {
			map.put("success", false);
			map.put("msg", ResourceUtil.getString("edoc.alert.file.operation.failed.label") + e.getMessage());
			return map;
		}
	}
	
	/**
	 * 输出文件
	 * @param summaryId 公文ID
	 * @param fileIds 正文及花脸ID集合
	 * @param summaryRoot 输出的文件夹目录
	 * @return
	 */
	private Map<String, Object> write2File(Long summaryId, List<Long> bodyList, List<V3XFile> v3xList,
			String summaryRoot) {
		Map<String, Object> map = new HashMap<String, Object>();
		File sf = new File(summaryRoot);
		if (sf.mkdirs()) {
			String tempPath = sf + File.separator;
			String srcFile = "";
			String destFile = "";
			try {
				// 处理正文
				for (int i = 0; i < bodyList.size(); i++) {
					V3XFile v3xfile = fileManager.getV3XFile(bodyList.get(i));
					// 获取解密后的文档
					File file = fileManager.getFile(bodyList.get(i), v3xfile.getCreateDate());
					// 拷贝文件到目录
					FileUtils.copyFileToDirectory(file, sf);
					// 转换为可使用的文件
					srcFile = tempPath + file.getName();
					destFile = tempPath + "(正文" + (i + 1) + ")" + bodyList.get(i) + getOfficeSuffix(v3xfile);
					map = file2StandardOffice(srcFile, destFile);
				}

				// 处理花脸
				if (null != v3xList && !v3xList.isEmpty()) {
					for (int i = 0; i < v3xList.size(); i++) {
						V3XFile v3xfile = v3xList.get(i);
						// 获取解密后的文档
						File file = fileManager.getFile(v3xfile.getId(), v3xfile.getCreateDate());
						// 拷贝文件到目录
						FileUtils.copyFileToDirectory(file, sf);
						// 转换为可使用的文件
						srcFile = tempPath + file.getName();
						destFile = tempPath + "(花脸" + (i + 1) + ")" + v3xfile.getId() + getOfficeSuffix(v3xfile);
						map = file2StandardOffice(srcFile, destFile);
					}
				}
			} catch (BusinessException e) {
				map.put("success", false);
				map.put("msg", ResourceUtil.getString("edoc.alert.file.operation.failed.label") + e.getMessage());
			} catch (IOException e) {
				map.put("success", false);
				map.put("msg", ResourceUtil.getString("edoc.alert.file.operation.failed.label") + e.getMessage());
			}
		}
		return map;
	}
	
	/**
	 * 转换为可使用的文件
	 * @param srcFile 源文件
	 * @param destFile 转换后的文件
	 * @return
	 */
	private Map<String, Object> file2StandardOffice(String srcFile, String destFile) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (Util.jinge2StandardOffice(srcFile, destFile)) {
			File stdTempFIle = new File(srcFile);
			if (stdTempFIle.exists() && stdTempFIle.isFile()) {
				// 删除文件
				try {
					stdTempFIle.delete();
				} catch (SecurityException e) {
					map.put("success", false);
					map.put("msg", ResourceUtil.getString("edoc.alert.file.operation.failed.label") +ResourceUtil.getString("edoc.alert.delete.file.failed.label"));//文件操作失败！原因：删除文件失败！
				}
			}
		} else {
			map.put("success", false);
			map.put("msg", ResourceUtil.getString("edoc.alert.file.operation.failed.label") +ResourceUtil.getString("edoc.alert.conversion.file.failed.label"));//文件操作失败！原因：转换文件失败！
		}
		return map;
	}
	
	/**
	 * 转换文件后缀
	 * @param file
	 * @return
	 * @throws BusinessException
	 */
	private String getOfficeSuffix(V3XFile file) throws BusinessException {
		String mimeType = "";
		String extension = null;
		if (file != null) {
			mimeType = file.getMimeType();
			if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(mimeType))
				extension = ".docx";
			else if ("application/msword".equals(mimeType))
				extension = ".doc";
			else if ("application/vnd.ms-excel".equals(mimeType))
				extension = ".xls";
			else if ("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(mimeType))
				extension = ".xlsx";
			else if ("application/kswps".equals(mimeType))
				extension = ".wps";
			else if ("application/kset".equals(mimeType))
				extension = ".et";
			else if ("msoffice".equals(mimeType)) {
				if (file.getFilename() != null && file.getFilename().toLowerCase().endsWith(".doc")
						|| file.getFilename().toLowerCase().endsWith(".docx")) {
					extension = ".doc";
				} else {
					extension = "";
				}
			}
		}
		return extension;
	}
	
	 /**
     * 移交功能
     * @param params Map<String,Object> | 必填 | 其它参数
     * @return response
     * @throws BusinessException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("transfer")
    public Response transfer(Map<String,Object> param) throws BusinessException  {
    	User user =AppContext.getCurrentUser();
    	Long transferMemberId = ParamUtil.getLong(param, "transferMemberId",-1L);
    	Long summaryId = ParamUtil.getLong(param, "summaryId", -1L); 
        Long affairId = ParamUtil.getLong(param, "affairId", -1L);
        String opinionContent = ParamUtil.getString(param, "opinionContent");
        if(Strings.isNotBlank(opinionContent)){
            opinionContent = Strings.removeEmoji(opinionContent);
        }
        Integer opinionAttibute = ParamUtil.getInt(param, "opinionAttibute",-1);
        Integer isTrack = ParamUtil.getInt(param, "isTrack", TrackEnum.no.ordinal());
        boolean isNewImg=checkisNewImg(param);
        if(isNewImg){//是否有文单签批
        	saveQianpiData(param,affairId,isNewImg);//文单签批
        }
        //移交的态度
        EdocOpinion transferOpinion = new EdocOpinion(); 
        transferOpinion.setAffairId(affairId);
        transferOpinion.setContent(opinionContent);
        transferOpinion.setAttribute(opinionAttibute);
        transferOpinion.setCreateUserId(user.getId());
        transferOpinion.setIdIfNew();
        transferOpinion.isPipeonhole = false;
        transferOpinion.setPolicy(ParamUtil.getString(param, "policy"));
        transferOpinion.setOpinionType(EdocOpinion.OpinionType.transferOpinion.ordinal());
        String returnValue = "true";
        String errMsg = "";
        CtpAffair affair = affairManager.get(affairId);
		if(affair!=null){
			transferOpinion.setNodeId(affair.getActivityId());
			transferOpinion.setPolicy(affair.getNodePolicy());
		}
//		EdocSummary summary = edocSummaryManager.findById(summaryId);
        EdocManagerModel edocManagerModel = new EdocManagerModel();
        edocManagerModel.setAffairId(affairId);
        edocManagerModel.setSignOpinion(transferOpinion);
        edocManagerModel.setSummaryId(summaryId);
        edocManagerModel.setUser(user);
        edocManagerModel.setAffairTrack(isTrack);

        //保存附件
        String fileJson = ParamUtil.getString(param, "fileJson","[]");
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("fileJson", fileJson);
  	 try {
  		errMsg = edocH5Manager.transEdocTransfer(edocManagerModel,transferMemberId,paramMap);
	} catch (BusinessException e) {
		returnValue = "false";
		errMsg = ResourceUtil.getString("edoc.transfer.error");
		LOGGER.error("H5公文移交异常", e);
	} finally {
		edocManager.unlockEdocAll(summaryId, null);
	}
    Map<String, Object> returnMap = new HashMap<String, Object>();
    if(Strings.isNotBlank(errMsg)){
      returnValue= "false";
    }
    returnMap.put("returnValue", returnValue);
    returnMap.put("errMsg", errMsg);
	return getResponse(returnMap);
    	
    }
    
    /**
     * 指定回退功能
     * @param params Map<String,Object> | 必填 | 其它参数
     * @return response
     * @throws BusinessException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("specifiesReturn")
    public Response specifiesReturn(Map<String,Object> param) throws BusinessException  {
    	User user = AppContext.getCurrentUser();
    	
		Long summaryId = ParamUtil.getLong(param,"summaryId",-1L);
		String content = ParamUtil.getString(param,"opinionContent","");
		Long currentAffairId = ParamUtil.getLong(param,"affairId",-1L);
		String theStepBackNodeId = ParamUtil.getString(param,"theStepBackNodeId");
		String appName = ParamUtil.getString(param,"appName");
		String policy = ParamUtil.getString(param,"policy");
		String submitStyle = ParamUtil.getString(param,"submitStyle");
		EdocSummary summary = edocManager.getEdocSummaryById(summaryId, true);
		CtpAffair affair = affairManager.get(currentAffairId);
		
		WorkflowBpmContext context = new WorkflowBpmContext();
		context.setCurrentWorkitemId(affair.getSubObjectId());
		context.setCaseId(affair.getCaseId());
		context.setProcessId(affair.getProcessId());
		context.setCurrentUserId(String.valueOf(user.getId()));
		context.setCurrentUserName(user.getName());
		context.setCurrentAccountId(String.valueOf(user.getLoginAccount()));
		context.setCurrentActivityId(String.valueOf(affair.getActivityId()));
		context.setBusinessData("EDOC_CONTENT_OP", content);
		context.setAppName(ApplicationCategoryEnum.edoc.name());
		context.setSelectTargetNodeId(theStepBackNodeId);// 被退回节点id
		context.setSubmitStyleAfterStepBack(submitStyle);// 退回方式 1直接提交给我 2流程重走
		context.setAppObject(summary);

		Integer attitude = ParamUtil.getInt(param,"opinionAttibute",com.seeyon.v3x.edoc.util.Constants.EDOC_ATTITUDE_NULL);
		EdocOpinion signOpinion = new EdocOpinion();
		signOpinion.setIdIfNew();
		signOpinion.setAttribute(attitude);
		signOpinion.setContent(content);
		signOpinion.isDeleteImmediate = false;// "delete".equals(afterSign);
		signOpinion.affairIsTrack = false;// "track".equals(afterSign);
		signOpinion.setNodeId(affair.getActivityId());
		signOpinion.setPolicy(affair.getNodePolicy());
		signOpinion.setAffairId(currentAffairId);
		signOpinion.setIsHidden(Strings.isNotBlank(ParamUtil.getString(param,"isHidden")));
		signOpinion.setOpinionType(EdocOpinion.OpinionType.backOpinion.ordinal());
		signOpinion.setEdocSummary(summary);

		Map<String, Object> tempMap = new HashMap<String, Object>();
		tempMap.put("summaryId", summaryId);
		tempMap.put("appName", appName);
		tempMap.put("submitStyle", submitStyle);
		tempMap.put("currentAffairId", currentAffairId);
		tempMap.put("selectTargetNodeId", theStepBackNodeId);
		tempMap.put("policy", policy);
		tempMap.put("content", content);
		tempMap.put("context", context);
		tempMap.put("summary", summary);
		tempMap.put("signOpinion", signOpinion);
		tempMap.put("oldOpinion", edocManager.findBySummaryIdAndAffairId(summaryId, currentAffairId));

		if ("start".equals(theStepBackNodeId)) {
			if ("0".equals(submitStyle)) {
				try {// 删除归档
					List<Long> ids = new ArrayList<Long>();
					ids.add(summary.getId());
					if (AppContext.hasPlugin("doc")) {
					    DocApi  docApi  = (DocApi) AppContext.getBean("docApi");
					    if(null!=docApi){
					    	docApi.deleteDocResources(user.getId(), ids);
					    }
					}
					summary.setHasArchive(false);
					//summary.setArchiveId(null);
					summary.setState(CollaborationEnum.flowState.cancel.ordinal());
					edocManager.update(summary);
					// 指定回退到发起者-流程重走撤销统计数据
					edocStatManager.deleteEdocStat(summary.getId());
					affairManager.updateAffairSummaryState(summary.getId(), summary.getState());
					// 流程撤销
					if (summary.getEdocType() == 0 || summary.getEdocType() == 2) {// 发文/签报撤销
						edocMarkManager.edocMarkCategoryRollBack(summary);
					}
				} catch (Exception e) {
					LOGGER.error("指定回退公文流程，删除归档文档:" + e);
				}
			}
		}
		
		String errMsg = "";
		String returnValue = "true";
		
		try {
			edocManager.appointStepBack(tempMap);
		} catch (Exception e) {
			errMsg = "指定回退异常";
			LOGGER.error("specifiesReturn error:", e);
			
		}finally {
			edocManager.unlockEdocAll(summaryId, null);
		}
		
		Map<String, Object> returnMap = new HashMap<String, Object>();
	    if(Strings.isNotBlank(errMsg)){
	      returnValue= "false";
	    }
	    returnMap.put("returnValue", returnValue);
	    returnMap.put("errMsg", errMsg);
		return getResponse(returnMap);
    	
    }
    /**
     * 暂存待办公文
     * @param affairId	Long | 必填 | 事项ID
     * @param params	Map | 必填 | 其他参数
     * <pre>
     * content	String | | 意见内容
     * hide		String | | 是否隐藏意见, true-隐藏意见，false-不隐藏
     * tracking	String | | 是否跟踪  ， true - 跟踪全部 ， false - 不跟踪
     * attitude	String | | 态度 {@link com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum}
     *    <pre>
     *        collaboration.dealAttitude.haveRead,已阅
     *        collaboration.dealAttitude.agree,同意
     *        collaboration.dealAttitude.disagree，不同意
     *    </pre>
     * likeSummary	String | | 是否点赞， true-点赞了， false-没有
     * fileUrlIds	String | | 附件ID串，使用,分隔
     * commentId	Long | | 使用草稿的评论ID  
     * </pre>
     * @return  Map<String, String>
     * @throws BusinessException
     */
    @POST
    @Path("doZCDB/{affairId}")
    public Response doZCDB(@PathParam("affairId") Long affairId, Map<String, Object> params) throws BusinessException{
        
         Map<String, String> ret = new HashMap<String, String>();
        User user = AppContext.getCurrentUser();

        //前端参数完整性校验TODO...
        
        CtpAffair affair = null;
        EdocSummary summary = null;
        
        boolean canDeal = true;
        boolean isLock = false;
        try {
            
            String jsonStr = ParamUtil.getString(params, JSON_PARAMS, "");
            putThreadContext(jsonStr);
            
            //获取对象
            affair = affairManager.get(affairId);
            summary = edocManager.getEdocSummaryById(affair.getObjectId(), true);
            
            
            String[] wfCheck = wapi.canTemporaryPending(toString(affair.getSubObjectId()));
            if("false".equals(wfCheck[0])){
                canDeal = false;
                ret.put(ERROR_KEY, wfCheck[1]);
            }
            
            if(canDeal){
                canDeal = lockWorkFlow(summary, user, 14, ret);
            }
            
            if(canDeal){
                //检验是否可以处理
                canDeal = checkCanDeal(affair, ret);
            }
            if(canDeal){
            	isLock = govdocLockManager.canGetLock(affairId);
            	if(!isLock) {
            		canDeal = false;
            		ret.put(ERROR_KEY, "同时项两个人正在同时处理");
            		LOGGER.error( AppContext.currentUserLoginName()+"同时项两个人正在同时处理。affairId"+affairId);
            	}
            }
            
            if (canDeal) {
            	//需要更新全文签批文件关联
                long aipFileId = ParamUtil.getLong(params, "aipFileId", -1L);
                if (aipFileId != -1) {
                	updateQwqp(summary.getId(), aipFileId);
                }

                //意见
                Comment comment = formComment(summary, affair, user, ColHandleType.wait, params);
               
                // 处理参数
                //Map<String, Object> dealParams = new HashMap<String, Object>();
                GovdocDealVO govdocDealVO = new GovdocDealVO();
                if(summary.getTempleteId() != null){
                	CtpTemplate template = templateManager.getCtpTemplate(summary.getTempleteId());
                    //取出模板信息
             /*   	dealParams.put("templateColSubject", template.getColSubject());
                	dealParams.put("templateWorkflowId", template.getWorkflowId());*/
              	govdocDealVO.setTemplateColSubject(template.getColSubject());
                	govdocDealVO.setTemplateWorkflowId(template.getWorkflowId().toString());
                }
                //跟踪
                govdocDealVO.setTrackPara(formTrackParam(params));
                //添加签收编号传递参数处理
                Map<String, String> expandParams = new HashMap<String, String>();
            	  String docMark2 = ParamUtil.getString(params, "docMark2", "");
                expandParams.put("docMark2", docMark2);
                govdocDealVO.setExpandParams(expandParams);
                this.transDoZcdb(govdocDealVO, affair, comment);
            }
        } catch (Exception e) {
      	  ret.put(ERROR_KEY, e.getMessage());
            LOGGER.error("处理系统异常", e);
        } finally {
            if(isLock){
    			//colLockManager.unlock(affairId);
    			govdocLockManager.unlock(affairId);
    		}
            removeThreadContext();
            if(canDeal){
            	//colManager.unlockCollAll(affairId,affair,summary);
          	  govdocLockManager.colDelLock(summary, affair);
            }
        }

      //结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        
        return ok(ret);
    }
    
    /**
     * 公文的评论列表
     * @param type		String | 必填 | 评论类型,只能取值：all-全部列表， like-点赞评论列表，agree-同意意见列表，disagree-不同意意见列表
     * @param summaryId	Long | 必填 | 协同ID
     * @param openFrom	Stirng | 必填 | 来源
     * @param affairID	Long | 必填 | 事项ID
     * @return	FlipInfo
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("comments/{type}/{summaryId}")
    public Response summaryComment(@PathParam("type") String type, @PathParam("summaryId") Long summaryId,@QueryParam("openFrom") String openFrom,@QueryParam("affairId") Long affairID) throws BusinessException {

        FlipInfo flipInfo = getFlipInfo();
        
        //ColSummary summary = collaborationApi.getColSummary(summaryId);
        EdocSummary summary =govdocSummaryManager.getSummaryById(summaryId);
        //评论，分页取20条
        List<GovdocSummaryCommentVO> vos = new ArrayList<GovdocSummaryCommentVO>(); 
        List<Comment> commentList = null;
        
        boolean isHistory = false;
        
        if(Strings.isNotBlank(openFrom) && null != affairID && !affairID.equals(Long.valueOf(-1))){
        	CtpAffair affair = affairManager.get(affairID);
        	if(affair == null && AppContext.hasPlugin("fk")){
                affair = affairManager.getByHis(affairID);
                if(affair != null){
                	isHistory = true;
                }
            }
        	
        	//控制隐藏的评论对发起人可见
        	AppContext.putThreadContext(Comment.THREAD_CTX_NOT_HIDE_TO_ID_KEY,summary.getStartMemberId());
        	if (!ColOpenFrom.supervise.name().equals(openFrom) && !ColOpenFrom.repealRecord.name().equals(openFrom)) {
        		if(affair != null){
        			AppContext.putThreadContext(Comment.THREAD_CTX_DOCUMENT_AFFAIR_MEMBER_ID,affair.getMemberId());
        		}        		
        	}
        	//控制表单查询：采用缩小权限策略：只要是设置了隐藏，不管有没有权限都隐藏（谭敏峰）
            if (ColOpenFrom.formQuery.name().equals(openFrom)
            		||ColOpenFrom.formStatistical.name().equals(openFrom)) {
                AppContext.putThreadContext(Comment.THREAD_CTX_NO_HIDDEN_COMMENT,"true");
            }  
        	
        	if(ColOpenFrom.glwd.name().equals(openFrom)){
        		List<Long> memberIds = affairManager.getAffairMemberIds(ApplicationCategoryEnum.collaboration, summary.getId());
        		AppContext.putThreadContext(Comment.THREAD_CTX_PROCESS_MEMBERS,Strings.isNotEmpty(memberIds) ? memberIds : new ArrayList<Long>());
        	}
        }
        
        
        if(COMMENT_TYPE_LIKE.equals(type)){
            commentList = commentManager.findLikeComment(ModuleType.edoc, 
                    summary.getId(), Comment.CommentType.comment, flipInfo, false, isHistory);
        } else if (COMMENT_TYPE_AGREE.equals(type)) {//同意
            commentList = commentManager.findCommentByAttitude(ModuleType.edoc, 
                    summary.getId(), Comment.CommentType.comment, flipInfo, CommentExtAtt1Enum.agree.i18nLabel(), false, isHistory);
        }else if(COMMENT_TYPE_DISAGREE.equals(type)){
            commentList = commentManager.findCommentByAttitude(ModuleType.edoc, 
                    summary.getId(), Comment.CommentType.comment, flipInfo, CommentExtAtt1Enum.disagree.i18nLabel(), false, isHistory);
        }else{
        	List<CommentType> ctypes = new ArrayList<Comment.CommentType>();
        	ctypes.add(Comment.CommentType.comment);
        	ctypes.add(Comment.CommentType.govdocniban);
            commentList = commentManager.findCommentByTypes(ModuleType.edoc, summary.getId(), ctypes, flipInfo, false, isHistory);
                    /*  if(summary.getReplyCounts() == null || 
            	 (	summary.getReplyCounts() != null && summary.getReplyCounts().intValue() !=  flipInfo.getTotal() )){
            	summary.setReplyCounts(flipInfo.getTotal());
            	if(!isHistory){//OA-121525转储数据不能进行更新，因为update的底层代码使用的是merge，如果调用merge，会向主库也插入一条数据。
            		colManager.updateColSummary(summary);
            	}
            }*/
        }
        
        if (Strings.isNotEmpty(commentList)) {//获取子回复
            List<Long> commentIds = new ArrayList<Long>();
            for (Comment c : commentList) {
                commentIds.add(c.getId());
            }
            Map<Long, List<Comment>> subComments = commentManager.findCommentReplay(commentIds);
            for (Comment c : commentList) {
          	  GovdocSummaryCommentVO vo = GovdocSummaryCommentVO.valueOf(c);
                //子回复
                List<Comment> subReplysList = subComments.get(c.getId());
                vo.setSubReplys(GovdocSummaryCommentVO.valueOf(subReplysList));
                vos.add(vo);
            }
            flipInfo.setData(vos);
        }
        
        AppContext.removeThreadContext(Comment.THREAD_CTX_NOT_HIDE_TO_ID_KEY);
        AppContext.removeThreadContext(Comment.THREAD_CTX_DOCUMENT_AFFAIR_MEMBER_ID);
        AppContext.removeThreadContext(Comment.THREAD_CTX_NO_HIDDEN_COMMENT);
        AppContext.removeThreadContext(Comment.THREAD_CTX_PROCESS_MEMBERS);
        
        return ok(flipInfo);
    }
    
    /**
     * 添加评论
     * @param summaryId		Long | 必填 | 协同ID
     * @param params		Map<String, Object> | 必填 | map类型参数
     * <pre>
     * content		String | | 评论内容
     * ctype		Integer | | 类型 {@link com.seeyon.ctp.common.content.comment.Comment.CommentType}
     *    -1, "发起人附言", 1, "回复"
     * fileUrlIds	String | | 附件ID串，使用,分隔
     * 
     * 如果ctype 值为  -1 则传递以下参数
     *    toSendMsg	String | | 是否发送消息 true - 发送消息, false 不发送
     * 如果ctype 值为 1 则传递以下参数
     *    affairId	Long | | 打开协同使用的affairId
     *    toSendMsg	String | | 是否发送消息 true - 发送消息, false 不发送
     *    hide		String | | 是否隐藏意见 true - 隐藏， false - 不隐藏
     *    hideToSender	String | | 对发起者隐藏， 需要 hide == true才有用， true - 对发起者隐藏，false-对发起者不隐藏
     *    commentId	Long | | 回复的意见ID
     * </pre>
     * @return  Map<String, String>
     */
    @POST
    @Path("comment/{summaryId}")
    public Response comment(@PathParam("summaryId") Long summaryId, Map<String, Object> params){
        
        Map<String, String> ret = new HashMap<String, String>();
        
        try {
            
            User user = AppContext.getCurrentUser();
            String content = ParamUtil.getString(params, "content");
            Integer cType = ParamUtil.getInt(params, "ctype");
            
            //附件
            /*String fileUrlIds = ParamUtil.getString(params, "fileUrlIds", null);
            List<Attachment> attrs = createAttachments(summaryId, fileUrlIds);*/
            String relateInfo = ParamUtil.getString(params, "fileJson", "[]");
            
            if(CommentType.sender.getKey() == cType){
                
                String toSendMsg = ParamUtil.getString(params, "toSendMsg");
                
                //附言
               addSenderComment(user.getId(), summaryId, content, relateInfo, "true".equals(toSendMsg));
            }else if(CommentType.reply.getKey() == cType) {
                //回复意见
                String toSendMsg = ParamUtil.getString(params, "toSendMsg");
                String hide = ParamUtil.getString(params, "hide");
                String hideToSender = ParamUtil.getString(params, "hideToSender");
                Long affairId = getLong(params, "affairId", null);
                Long commentId = getLong(params, "commentId", null);
                
                CtpAffair affair = affairManager.get(affairId);
                Comment c = commentManager.getComment(commentId);
                
                Comment comment = new Comment();
                comment.setContent(content);
                comment.setTitle(affair.getSubject());
                
                StringBuilder showToIds = new StringBuilder();
                boolean isHide = "true".equals(hide);
                boolean isHideToSender = "true".equals(hideToSender);
                boolean _hide = isHide || isHideToSender;
                comment.setHidden(_hide);
                
                if(_hide){
                	//移动端默认对被回执者不进行隐藏
                	showToIds.append(",Member|").append(c.getCreateId());
                	
                	//移动端默认对震荡回执者不进行隐藏
                	showToIds.append(",Member|").append(affair.getMemberId());
                	
                	if(!isHide) {
                		List<CtpAffair> affairs = affairManager.getAffairs(affair.getObjectId());
                		for(CtpAffair af : affairs){
                			if(!af.getMemberId().equals(affair.getSenderId())){
                				showToIds.append(",Member|").append(af.getMemberId());
                			}
                		}
                	}
                	if(!isHideToSender){
                		showToIds.append(",Member|").append(affair.getSenderId());
                	}
                	
                	comment.setShowToId(showToIds.toString().substring(1));
                }
                
                boolean isSendMsg = "true".equals(toSendMsg);
                comment.setPushMessage(isSendMsg);
                if(isSendMsg){
                    StringBuilder msgMembers = new StringBuilder();
                    
                    msgMembers.append("[");
                    
                    msgMembers.append("[")
                              .append("\"").append(c.getAffairId()).append("\"")
                              .append(",")
                              .append("\"").append(c.getCreateId()).append("\"")
                              .append("]");
                    
                    msgMembers.append("]");
                    comment.setPushMessageToMembers(msgMembers.toString());
                }
                
                comment.setClevel(c.getClevel() + 1);
                comment.setModuleType(c.getModuleType());
                comment.setModuleId(c.getModuleId());
                comment.setCtype(CommentType.reply.getKey());
                comment.setAffairId(affairId);
                
                //附件
                /*String relateInfo = "[]";
                if(Strings.isNotEmpty(attrs)){
                    relateInfo = JSONUtil.toJSONString(attrs);
                }*/
                comment.setRelateInfo(relateInfo);
                
                //at相关信息
                formatAtWho(comment, affair, params);
                
                collaborationApi.replyComment(user.getId(), commentId, comment);
                
            }else{
              //其他回复
            }
        } catch (BusinessException e) {
            ret.put(ERROR_KEY, e.getMessage());
            LOGGER.error("评论异常", e);
        }
        
      //结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        
        return ok(ret);
    }
    
	/**
	 * 暂存待办
	 * @param dealVo
	 * @param affair
	 * @throws BusinessException
	 */
	private void transDoZcdb(GovdocDealVO dealVo, CtpAffair affair,Comment comment) throws BusinessException {

		// 更新Affair的状态为暂存待办
		affair.setUpdateDate(new Timestamp(System.currentTimeMillis()));
		affair.setSubState(SubStateEnum.col_pending_ZCDB.key());
		affairManager.updateAffair(affair);
		EdocSummary summary = govdocSummaryManager.getSummaryById(affair.getObjectId());
		if(!"".equals(dealVo.getExpandParams().get("docMark2")))//设置签收
		summary.setDocMark2(dealVo.getExpandParams().get("docMark2"));
		// 督办设置
		@SuppressWarnings("unchecked")
		Map<String, Object> superviseMap = (Map<String, Object>) ParamUtil.getJsonDomain("superviseDiv");
		String isModifySupervise = (String) superviseMap.get("isModifySupervise");
		if ("1".equals(isModifySupervise)) {
			DateSharedWithWorkflowEngineThreadLocal.setColSummary(summary);
			SuperviseMessageParam smp = new SuperviseMessageParam(true, summary.getImportantLevel(), summary.getSubject(), summary.getForwardMember(),
					summary.getStartMemberId());
			if (summary.getGovdocType() != null) {
				this.superviseManager.saveOrUpdateSupervise4Process(smp, summary.getId(), SuperviseEnum.EntityType.govdoc);
			} else {
				this.superviseManager.saveOrUpdateSupervise4Process(smp, summary.getId(), SuperviseEnum.EntityType.summary);
			}
		}
		// 从request对象中对象中获取意见
	//	Comment comment = govdocPubManager.getCommnetFromRequest(GovdocContentUtil.OperationType.wait, affair.getMemberId(), affair.getObjectId());

		FormBean formBean = formApi4Cap3.getForm(summary.getFormAppid());
		if (formBean != null) {
			try {
				FormDataBean formDataBean = formApi4Cap3.getDataMasterBeanById(summary.getFormRecordid(), formBean, null);
				List<FormFieldBean> formFieldBeans = formBean.getAllFieldBeans();
				FormDataMasterBean formDataMasterBean = null;
				try {
					formDataMasterBean = formApi4Cap3.findDataById(summary.getFormRecordid(), summary.getFormAppid(), null);
				} catch (SQLException e) {
					LOGGER.error(e);
					//e.printStackTrace();
				}
				for (FormFieldBean formFieldBean : formFieldBeans) {
					if (formFieldBean.isMasterField()) {
						if (formFieldBean.getMappingField() != null) {
							if ("urgent_level".equals(formFieldBean.getMappingField())) {// 紧急程度
								Object value = formDataBean.getFieldValue(formFieldBean.getName());
								if (value == null || Strings.isBlank(value.toString())) {
									continue;
								}
								Map<String, Object> selectParams = new HashMap<String, Object>();
								selectParams.put("bizModel", true);
								selectParams.put("isFinalChild", formFieldBean.getIsFinalChild());
								selectParams.put("enumId", formFieldBean.getEnumId());
								selectParams.put("enumLevel", formFieldBean.getEnumLevel());
								List<CtpEnumItem> enumList = enumManagerNew.getFormSelectEnumItemList(selectParams);
								for (CtpEnumItem item : enumList) {
									if (item.getId().longValue() == Long.valueOf(value.toString())) {
										summary.setImportantLevel(Integer.valueOf(item.getEnumvalue()));
										affair.setImportantLevel(Integer.valueOf(item.getEnumvalue()));
										break;
									}
								}
							}
						}
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				LOGGER.error(e);
				//e.printStackTrace();
			}
		}
		// add by rz 2017-09-11 [添加代录人判断] start
		// String pishiFlag;
		// try {
		// pishiFlag =
		// orgManager.checkLeaderPishi(AppContext.getCurrentUser().getId(),
		// affair.getMemberId());
		// if("pishi".equals(pishiFlag)){
		// comment.setContent(comment.getContent()+"(由"+AppContext.currentUserName()+"代录)");
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// add by rz 2017-09-11 [添加代录人判断] end
		//transFinishAndZcdb(dealVo, affair, summary, comment, ColHandleType.wait);
		dealVo.setSummary(summary);
		dealVo.setAffair(affair);
		dealVo.setComment(comment);
		govdocManager.transFinishWorkItemPublic(dealVo, ColHandleType.wait);
	}
    
	@GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("newEdocSummary/{openFrom}/{affairId}/{summaryId}")
    public Response newEdocSummary(@PathParam("openFrom") String openFrom, 
                            @PathParam("affairId") Long affairId, 
                            @PathParam("summaryId") Long summaryId,
                            @QueryParam("pigeonholeType") String pigeonholeType,
                            @QueryParam("operationId") String operationId,
            
                            //权限相关的参数
                            @QueryParam("docResId") String docResId,
                            @QueryParam("baseObjectId") String baseObjectId,
                            @QueryParam("baseApp") String baseApp,
                            @QueryParam("taskId") String taskId) throws BusinessException {

        User user = AppContext.getCurrentUser();
        fixUser(user);
        boolean isHistory = false;
        EdocSummary summary = null;
        CtpAffair affair = null;
        //如果是从业务生成器生成的列表穿透过来
       openFrom = IsfromBizList(openFrom);
        //全文检索穿透
        if(affairId == -1l && "listPending".equals(openFrom) && null != summaryId){
        	Map<String, Object> findSourceInfo;
			try {
				findSourceInfo = govdocIndexEnable.findSourceInfo(summaryId);
				affairId = (Long) findSourceInfo.get("sourceId");
			} catch (BusinessException e) {
				LOGGER.info("全文检索穿透报错" + e.getMessage());
			}
        }
        
        affair = affairManager.get(affairId);
        EdocSummary edocSummary = null;
        if(affair != null){
        	edocSummary = edocManager.getEdocSummaryById(affair.getObjectId(),true);
        }
        //经过兼容后， affairId可能已经被替换
        if(affair != null){
            affairId = affair.getId();
        }
        
        if(affair==null && summaryId != -1) {
            //文档中心、关联文档、督办
            if(ColOpenFrom.docLib.name().equals(openFrom) 
                    || ColOpenFrom.glwd.name().equals(openFrom) 
                    || ColOpenFrom.supervise.name().equals(openFrom)
                    || EdocOpenFrom.lenPotent.name().equals(openFrom)) {
                
                isHistory = false;
                Long aId = affairId;
                if(aId == null || aId.intValue() == -1){
                    aId = summaryId;
                }
                affair = affairManager.get(aId);
                if(affair == null && AppContext.hasPlugin("fk")){
                    isHistory = true;
                    affair = affairManager.getByHis(aId);
                }
                if(affair == null){//解决全文检索公文穿透报错的问题
                	isHistory = false;
                    affair = affairManager.getSenderAffair(aId);
                    if(affair == null){
                        isHistory = true;
                        affair = affairManager.getSenderAffairByHis(aId);
                    }
                }
            }else if(ColOpenFrom.formRelation.name().equals(openFrom)
            	  || ColOpenFrom.formQuery.name().equals(openFrom)){
            	affair = affairManager.getSenderAffair(summaryId);
            	if(affair == null  && AppContext.hasPlugin("fk")){
            		affair = affairManager.getSenderAffairByHis(summaryId);
            		if(affair != null){
            			isHistory = true;
            		}
            	}
            }else if(ColOpenFrom.edocStatistics.name().equals(openFrom)){//如果从公文统计穿透而来
              	affair = affairManager.getSenderAffair(summaryId);
              	if(affair == null  && AppContext.hasPlugin("fk")){
              		affair = affairManager.getSenderAffairByHis(summaryId);
              		if(affair != null){
              			isHistory = true;
              		}
              	}
              }else{
                
                if (affair == null) {
                    isHistory = false;
                    affair = affairManager.getSenderAffair(affairId);
                    if(affair == null){
                        isHistory = true;
                        affair = affairManager.getSenderAffairByHis(affairId);
                    }
                }
            }
            
        } else {
            if(!ColOpenFrom.docLib.name().equals(openFrom) 
                    && !ColOpenFrom.glwd.name().equals(openFrom) 
                    && !ColOpenFrom.supervise.name().equals(openFrom)) {
                Map<String, String> errorRet = new HashMap<String, String>();
            }
        }
        
        if(affair != null && summary == null ){
        	summary = govdocSummaryManager.getSummaryById(affair.getObjectId());
        }
        
      //拼装内容
        Map<String, Object> map = new HashMap<String, Object>();
        
        //基本数据校验
        //TODO 公文需要做
        Map<String,String> errorMap = new HashMap<String,String>();
        //权限校验
        if(!ColOpenFrom.edocStatistics.name().equals(openFrom)){
            //权限校验
            SecurityCheckParam param = new SecurityCheckParam(ApplicationCategoryEnum.collaboration, user, affair.getId());
            param.setAffair(affair);
            param.addExt("openFrom", openFrom);
            param.addExt("docResId", docResId);
            param.addExt("baseObjectId", baseObjectId);
            param.addExt("baseApp", baseApp);
            param.addExt("taskId", taskId);
            
            param.addExt("col_Summary",summary);
            SecurityCheck.isLicit(param);
            if(!param.getCheckRet()){
                
                //协同驾驶舱统计， A统计B的数据， A和B在同一个流程里面，提示A无权查看B的事项，这里做一次防护
                //解决一种场景：在流程中，但是打开的不是自己的事项，以前在SecurityControlColImpl中有判断是否在流程中的逻辑，虽然返回true，但是打开的不是自己的事项，存在权限泄漏，
                //放在外边来，可以解决这种情况，始终打开自己的事项
                CtpAffair aff = findMemberAffair(affair.getObjectId(), isHistory, user.getId());
                if(aff == null){
                	 String msg = param.getCheckMsg();
                     if(Strings.isBlank(msg)){
                         msg = ResourceUtil.getString("collaboration.common.unauthorized");//越权访问
                     }
                     errorMap.put(ERROR_KEY, msg);
                     return ok(errorMap);
                }
            }
        }
        if(!"repealRecord".equals(openFrom) 
				&& !"stepBackRecord".equals(openFrom) 
				&& !"exchangeFallback".equals(openFrom)
				&& !"glwd".equals(openFrom)) {
			if (!AffairUtil.isAfffairValid(affair)) {
				String errorMsg = WFComponentUtil.getErrorMsgByAffair(affair);
				if (!Strings.isBlank(errorMsg)) {
					errorMap.put(ERROR_KEY, errorMsg);
					return ok(errorMap);
				}
			}
		}
        if(edocSummary == null){
        	edocSummary = edocManager.getEdocSummaryById(affair.getObjectId(),true);
        }
        if(affair!=null && affair.isDelete() && openFrom != null 
        		&& !ColOpenFrom.docLib.name().equals(openFrom)
        		&& !ColOpenFrom.edocStatistics.name().equals(openFrom)
        		&& !ColOpenFrom.glwd.name().equals(openFrom)){
        	errorMap.put(ERROR_KEY, "该公文可能已移交给别人，你无法处理！");
			return ok(errorMap);
        }
        if(edocSummary == null){
        	errorMap.put(ERROR_KEY, ResourceUtil.getString("govdoc.data.noExist.label"));
			return ok(errorMap);
        }
        
        GovdocSummaryDetailVO summaryVO  = GovdocSummaryDetailVO.valueOf(edocSummary);
        summaryVO.setBodyType(""+MainbodyType.FORM.getKey());
        summaryVO.setActivityId(affair.getActivityId());
        summaryVO.setAffairWorkitemId(affair.getSubObjectId());
        summaryVO.setAffairId(affair.getId());
        summaryVO.setAffairIsDelete(affair.isDelete());
        summaryVO.setHasFavorite(affair.getHasFavorite() ? "1" : "0");
        summaryVO.setAccountId(edocSummary.getOrgAccountId());
        //附件
        List<Attachment> attachments = attachmentManager.getByReference(summaryVO.getId(), summaryVO.getId());
        summaryVO.setAttachments(attachments);
        
        //affair状态
        summaryVO.setAffairState(affair.getState());
        summaryVO.setAffairSubState(affair.getSubState());
        summaryVO.setAffairTrack(affair.getTrack());
        //affair对应的当前节点id
        summaryVO.setActivityId(affair.getActivityId());
        if(null != summaryVO.getTemplateId()){
        	CtpTemplate template = templateManager.getCtpTemplate(summaryVO.getTemplateId());
        	if(null!=template){
        		boolean isSpecialSteped = affair != null && affair.getSubState() == SubStateEnum.col_pending_specialBacked.key();
    			if (template != null && template.getWorkflowId() != null) { // 系统模板 // & // 个人模板
    				if (!isSpecialSteped && TemplateUtil.isSystemTemplate(template)) {
    					summaryVO.setTemplateProcessId(template.getWorkflowId());
    					summaryVO.setProjectId(null);
    				}
    			}
        		if (template.isSystem()) {
        		    summaryVO.setSystemTemplate(true);
        		}
        		//扫码
        		summaryVO.setCanScanCode(null == template.getScanCodeInput() ? "0" : template.getScanCodeInput() ? "1" :"0");
        		//点赞
        		summaryVO.setCanPraise(template.getCanPraise());
        		//公文模板动态标题添加代码块 copy from GovdocPuhManagerimpl line 986 --start
        		if(Strings.isBlank(summaryVO.getSubject())) {
    				summaryVO.setSubject(template.getSubject());
    			}
        		String dynamicSubject = "";
        		if(Strings.isNotBlank(template.getColSubject())) { 
    				dynamicSubject = govdocFormManager.makeSubject(template, summary);
    			}
        		if(Strings.isBlank(dynamicSubject)) {
    				dynamicSubject = summaryVO.getSubject();
    			}
    			//若动态标题为空则将subject赋值
        		summaryVO.setSubject(dynamicSubject);
    			//--end
        	}
        }
       
        CtpTemplate template = null;
		if (summaryVO.getTemplateId() != null) {
			template = templateManager.getCtpTemplate(summaryVO.getTemplateId());
			if (String.valueOf(MainbodyType.FORM.getKey()).equals(summaryVO.getBodyType()) && Integer.valueOf(StateEnum.col_waitSend.getKey()).equals(affair.getState())
					&& Boolean.TRUE.equals(template.isDelete())) {
				/*
				 * 因为表单模板在待发中，要去读流程模板绑定的表单权限、但是如果模板被删除，ProcessTemplate被物理删除，
				 * 所以这里防护一下。 后续如果processTemplate改为逻辑删除，这个IF就可以干掉了
				 */
				errorMap.put(ERROR_KEY, ResourceUtil.getString("workflow.wapi.exception.msg001"));
				return ok(errorMap);
			}
		}
        //正文设置
        GovdocSummaryContentVO contentVO = findSummaryContent(edocSummary, affair, template,openFrom, pigeonholeType, operationId);
           
		CtpContentAll _content = GovdocContentHelper.getBodyContentByModuleId(edocSummary.getId());
		//转pdf、ofd后，移动端只显示pdf或ofd正文
		CtpContentAll transPdfContent = GovdocContentHelper.getOnlyTransBodyContentByModuleId(edocSummary.getId(), MainbodyType.Pdf.getKey());
		if (transPdfContent != null) {
			_content = transPdfContent;
		}
		CtpContentAll transOfdContent = GovdocContentHelper.getOnlyTransBodyContentByModuleId(edocSummary.getId(), MainbodyType.Ofd.getKey());
		if (transOfdContent != null) {
			_content = transOfdContent;
		}
		if(_content!=null){
        	CtpContentAllBean ccab = new CtpContentAllBean(_content);
        	if(MainbodyType.Ofd.getKey()!= ccab.getContentType()){//M3暂时屏蔽ofd正文
        		MainbodyHandler handler = ctpMainbodyManager.getContentHandler(ccab.getContentType());
    			handler.handleContentView(ccab);
        	}
        	map.put("content1", GovdocSummaryContentVO.valutOf(ccab));
        	//下方lastModified，onlineContentCreatData，onlineContentLastModifyData为zouy+yangwg添加的参数，为绕过壳 json转换问题TODO 优化
        	map.put("lastModified", DateUtil.format(_content.getModifyDate(), DateUtil.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_PATTERN));
        	map.put("onlineContentCreatData", DateUtil.format(_content.getCreateDate(), DateUtil.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_PATTERN));
        	map.put("onlineContentLastModifyData", _content.getModifyDate());
        	String onlineContentEnable = systemConfig.get("office_transform_enable");
        	if(Strings.isNotBlank(onlineContentEnable)){
        		map.put("onlineContentEnable", onlineContentEnable.equals("enable"));
        	}
		}
         if(contentVO!=null && String.valueOf(MainbodyType.FORM.getKey()).equals(summaryVO.getBodyType())) {
        	summaryVO.setFormRecordId(contentVO.getContentDataId());
        	summaryVO.setFormAppId(summaryVO.getFormAppid());
        	summaryVO.setRightId(contentVO.getRightId());
        	
        	//多视图兼容
        	if(Strings.isNotBlank(contentVO.getFormRightId())){
        		summaryVO.setRightId(contentVO.getFormRightId());
        	}
        	
        	summaryVO.setNodePolicy(affair.getNodePolicy());
        	if(AffairUtil.isFormReadonly(affair)) {
            	summaryVO.setAffairReadOnly(Boolean.TRUE);
            }
        }
    
        int viewState = CtpContentAllBean.viewState_readOnly;
        if(String.valueOf(MainbodyType.FORM.getKey()).equals(String.valueOf(summaryVO.getBodyType()))
                && Integer.valueOf(StateEnum.col_pending.key()).equals(affair.getState())
               // && !"zhihui".equals(collaborationApi.getPolicyByAffair(affair).getId())
                //&& !AffairUtil.isFormReadonly(affair)
                && !ColOpenFrom.glwd.name().equals(openFrom)
                && !ColOpenFrom.listDone.name().equals(openFrom)
                && !"lenPotent".equals(openFrom)){//isFormReadonly表单只读
        	
        	 viewState = CtpContentAllBean.viewState__editable;
        	 
        }
        
        summaryVO.setCanModify(summaryVO.isCanmodify() ? "true" : "false");
        summaryVO.setCanForward(summaryVO.isCanForward() ? "1" : "0");
        summaryVO.setCanArchive((canArchive(user) &&  summaryVO.isCanArchive()) ? "1" : "0");
        
//        //发起人附言, 不分页
        List<GovdocSummaryCommentVO> senderCommonts =  summarySenderComments(summary, isHistory);
        
        //页面设置参数， 界面显示操作等权限参数设置
        Map<String, Object> pageConfig = new HashMap<String, Object>();
        Map<String, Object> draftCommentMap = new HashMap<String, Object>();
        
        //设置节点权限
       affairNodeProperty(pageConfig, openFrom, affair, summary, user);        
        SeeyonPolicy currentPolicy = null;
        Boolean noFindPermission =  (Boolean) pageConfig.get("noFindPermission");
        if(noFindPermission != null && noFindPermission){
            map.put(INFO_KEY, ResourceUtil.getString("collaboration.summary.noFindNode"));
            String newPermission = (String)pageConfig.get("newPermission");
            currentPolicy = new SeeyonPolicy(newPermission, BPMSeeyonPolicy.getShowName(newPermission));
        }else{
            currentPolicy = collaborationApi.getPolicyByAffair(affair);
        }
        
        //当前节点权限
        map.put("currentPolicy", currentPolicy);
        
        
//        //意见草稿
        boolean canDeal = (Boolean)pageConfig.get("canDeal"); 
        
//        //设置协同已读
        if(!isHistory){
        	updateAffairState(affair);
       }
        
        //默认节点权限
        PermissionVO defPermission =this.getDefaultPermission(affair.getSubApp(), summary.getOrgAccountId());
        //默认个人设置
        String trackProcess = customizeManager.getCustomizeValue(user.getId(), CustomizeConstants.TRACK_PROCESS);
        if("false".equals(trackProcess)){
        	trackProcess = (Integer.valueOf(TrackEnum.no.ordinal()).equals(affair.getTrack()) || affair.getTrack() == null) ? "false" : "true";
        }
       
        String listType = GovDocOpenFromUtil.getListType(openFrom);
  
        if(!ColSummaryType.listPending.name().equals(listType)) {
            summaryVO.setCanScanCode("0");
        }   
        
        if(affair.getState().intValue()==StateEnum.col_done.key()) {//如果从待办中打开已经处理过的公文
            if(ColSummaryType.listPending.name().equals(listType)) {
                listType = ColSummaryType.listDone.name();
            }   
        }
        
        if((!user.getId().equals(summaryVO.getStartMemberId()) 
                && Integer.valueOf(StateEnum.col_sent.getKey()).equals(affair.getState()))
                || affair.isDelete()){
        	//打开已发事项，但是当前用户不是发起人的时候的时候不能操作，例如督办
        	listType = GovDocOpenFromUtil.ColSummaryType.onlyView.name();
        }
        
        //移动端是否能够进行处理
        List<String> allActions = new ArrayList<String>();
        allActions.addAll((List<String>)pageConfig.get("nodeActions"));
        allActions.addAll((List<String>)pageConfig.get("commonActions"));
        allActions.addAll((List<String>)pageConfig.get("advanceActions")); 		  		
        Boolean iscanHandle= this.iscanHandleByMobile(affair.getNodePolicy(),listType,allActions,affair);
        if(allActions.contains("ReSign") && iscanHandle){//如果当前节点策略包含签收并且是可以M3处理的，设置标记
        	 map.put("isM3DoSign", true);
        }
        //被回退者意见保留设置
        backOptionSet(affair, edocSummary,map);
        //设置打开权限参数
        summaryVO.setListType(listType);
        summaryVO.setIsCanComment(GovDocOpenFromUtil.isCanComment(openFrom));
        summaryVO.setIsCanHandle(iscanHandle);
        boolean inInSpecialSB = false;
    	if(StateEnum.col_pending.getKey() == affair.getState()) {
    		if(SubStateEnum.col_pending_specialBack.getKey() != affair.getSubState() &&
    			SubStateEnum.col_pending_specialBacked.getKey() !=affair.getSubState() &&
    			SubStateEnum.col_pending_specialBackCenter.getKey() != affair.getSubState()){//15 16 17
    			if(summaryVO.getCaseId() != null) {
    			    //接口返回的是是否可以回退，参数命名上就是取非，不能回退则是在指定回退状态
    			    inInSpecialSB = !wapi.isInSpecialStepBackStatus(summaryVO.getCaseId(), isHistory);
    			}
    		}
    	}

        //设置头部存在单位转发时显示单位简称
        summaryVO.setStartMemberName(Functions.showMemberName(summaryVO.getStartMemberId()));
//        //新建节点权限
//        NodePolicyVO newPolicy = colManager.getNewColNodePolicy(affair.getOrgAccountId());
        //判断是否有新建权限
        boolean isHaveNewColl = MenuPurviewUtil.isHaveNewColl(user);
        
        if(!summaryVO.isFinished() && summaryVO.getState() != null){
            summaryVO.setFinished(CollaborationEnum.flowState.terminate.ordinal() == summaryVO.getState()
                    || CollaborationEnum.flowState.finish.ordinal() == summaryVO.getState());
        }
        
        //处理列表数量设置
        Map<String, String> countsMap = new HashMap<String, String>(2);
        
        Map<String,Object> params = new HashMap<String,Object>();
        List<Integer> states = new ArrayList<Integer>(2);
        states.add(StateEnum.col_pending.getKey());
        params.put("delete", Boolean.valueOf(false));
        params.put("app", ApplicationCategoryEnum.edoc.getKey());
        params.put("objectId", summaryVO.getId());
        params.put("state", states);
        int count1 = 0;
        int count2 = 0;
        if(isHistory){
            count1 = affairManager.getCountByConditionsHis(params);
            states.add(StateEnum.col_done.getKey());
            count2 = affairManager.getCountByConditionsHis(params);
        }else{
        	count1 = affairManager.getCountByConditions(params);
        	states.add(StateEnum.col_done.getKey());
        	count2 = affairManager.getCountByConditions(params);
        }
        
        countsMap.put("all", String.valueOf(count2));
        countsMap.put("running", String.valueOf(count1));
        map.put("affairCount", countsMap);
        
        map.put("trackProcess", trackProcess);
        map.put("isHaveNewColl", isHaveNewColl);
        map.put("content", contentVO);
        map.put("senderCommonts", senderCommonts);
        map.put("pageConfig", pageConfig);
        map.put("draftComment", draftCommentMap);
        map.put("currentUser", GovdocCurrentUserInfoVO.valueOf(user));
        map.put("defPolicy", PermissionMiniVO.valueOf(defPermission));
        map.put("likeCommentCount", getCommentCount(affair.getObjectId(), COMMENT_TYPE_LIKE, isHistory));
        map.put("allCommentCount", getCommentCount(affair.getObjectId(), COMMENT_TYPE_ALL, isHistory));
        map.put("inInSpecialSB", inInSpecialSB);
        map.put("isHistory", isHistory);
        
        map.put("cancelOpinionPolicy", String.valueOf(defPermission.getCancelOpinionPolicy()));
        map.put("_viewState", viewState);
        map.put("SystemCurrentTimeMillis", System.currentTimeMillis());
        
        Map<String, Object> map_workflowCheck = new HashMap<String, Object>();
        map_workflowCheck.put("formAppId", summaryVO.getFormAppid());
        map_workflowCheck.put("formId", affair.getFormId());
        map_workflowCheck.put("formOperationId", affair.getFormOperationId());
        map.put("workflowCheckParam", map_workflowCheck);
        
        //表单高级
        if(AffairUtil.isFormReadonly(affair)){
        	map.put("deeReadOnly", 1);
        }else{
        	map.put("deeReadOnly", 0);
        }
        //如果是模板，处理流程追溯配置情况
        map.put("canTrackWorkflow", "0");
        //编辑office正文需要这个参数
        map.put("createDate", summaryVO.getCreateDate());
        if(template != null){
        	if(!Integer.valueOf(0).equals(template.getCanTrackWorkflow())){
        		map.put("canTrackWorkflow", template.getCanTrackWorkflow());
        	}
        	//节点描述
        	if(Integer.valueOf(StateEnum.col_pending.key()).equals(affair.getState())){
        		String nodeDesc =  wapi.getBPMActivityDesc(template.getWorkflowId(),String.valueOf(affair.getActivityId()));
        		map.put("nodeDesc", nodeDesc);
        	}
        }
        //是否已收藏
    	Map<String,String> para = new HashMap<String,String>();
    	para.put("checkBtns", "Favorite");
    	para.put("affairId", String.valueOf(affair.getId()));
    	if(affair != null){
    		boolean hasFavorite = affair.getHasFavorite();
    		map.put("isFavorite", hasFavorite);
    	}
    	
        //添加表单锁
        map.put("formIsLock", false);
        if(canDeal){
        	GovdocLockObject lockObject = govdocLockManager.formAddLock(affairId);
            if(null != lockObject && "0".equals(lockObject.getCanSubmit())){
                map.put("formIsLock", true);
                map.put("formLockMsg", ResourceUtil.getString("collaboration.common.flag.editingForm", lockObject.getLoginName(), lockObject.getFrom()));
            }
        }
         //传入公文类型
        summaryVO.setGovdocType(getNewGovdocType(affair.getSubApp()));;
        
        this.getMultiCallInfo(map);
        //续办相关
        GovdocSummaryVO govdocSummaryVO=new GovdocSummaryVO();
        govdocSummaryVO.setAffair(affair);
        govdocSummaryVO.setSummary(edocSummary);
        
        govdocContinueManager.fillSummaryVoByCustomDealWithForM3(govdocSummaryVO);
        summaryVO.setCanShowOpinion(govdocSummaryVO.getCanShowOpinion());
        summaryVO.setCanShowAttitude(govdocSummaryVO.getCanShowAttitude());
        summaryVO.setCanShowCommonPhrase(govdocSummaryVO.getCanShowCommonPhrase());
        summaryVO.setCanUploadAttachment(govdocSummaryVO.getCanUploadAttachment());
        summaryVO.setCanUploadRel(govdocSummaryVO.getCanUploadRel());
        summaryVO.setIsFaxingNode(govdocSummaryVO.getIsFaxingNode());
        summaryVO.setFormDefaultShow(govdocSummaryVO.getFormDefaultShow());
        summaryVO.setToEdocLibFlag(govdocSummaryVO.getToEdocLibFlag());
        summaryVO.setToEdocLibSelectFlag(govdocSummaryVO.getToEdocLibSelectFlag());
        summaryVO.setShowCustomDealWith(govdocSummaryVO.getShowCustomDealWith());
        summaryVO.setCustomDealWith(govdocSummaryVO.getCustomDealWith());
        summaryVO.setCustomDealWithPermission(govdocSummaryVO.getCustomDealWithPermission());
        summaryVO.setCustomDealWithMemberId(govdocSummaryVO.getCustomDealWithMemberId());
        summaryVO.setReturnPermissionsLength(govdocSummaryVO.getReturnPermissionsLength());
        summaryVO.setPermissions(govdocSummaryVO.getPermissions());
        summaryVO.setMembers(govdocSummaryVO.getMembers());
        summaryVO.setMemberJson(govdocSummaryVO.getMemberJson());
        summaryVO.setCurrentPolicyId(govdocSummaryVO.getCurrentPolicyId());
        summaryVO.setCurrentPolicyName(govdocSummaryVO.getCurrentPolicyName());
        summaryVO.setNotExistChengban(govdocSummaryVO.isNotExistChengban());
        summaryVO.setCurrentMember(govdocSummaryVO.getCurrentMember());
        summaryVO.setNextMember(govdocSummaryVO.getNextMember());
        
        //全文签批相关
        qwqpManager.setQwqpParam(govdocSummaryVO);
        summaryVO.setAipFileId(govdocSummaryVO.getAipFileId());
        
        //修复jari bug GOVA-983 新公文取消点赞
        summaryVO.setCanPraise(false);  
        //获取水印的数据
        Map<String, Object> waterMarkMap = new HashMap<String, Object>();
    	WaterMarkUtil.getWaterMarkSetings(waterMarkMap);
    	map.put("waterMarkMap", waterMarkMap);
        //summaryVO.setExchangeSendAffairId(summary.getExchangeSendAffairId());
        map.put("summary", summaryVO);
        return success(map);
    }
	
	/**
	 * 新公文-处理公文
	 * @param affairId	Long | 必填 | 事项ID
	 * @param params	Map | 必填 | 其他参数
	 * <pre>
	 * content	String | | 意见内容
	 * hide		String | | 是否隐藏意见, true-隐藏意见，false-不隐藏
	 * tracking	String | | 是否跟踪  ， true - 跟踪全部 ， false - 不跟踪
	 * attitude	String | | 态度 {@link com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum}
	 *    <pre>
	 *        collaboration.dealAttitude.haveRead,已阅
	 *        collaboration.dealAttitude.agree,同意
	 *        collaboration.dealAttitude.disagree，不同意
	 *    </pre>
	 * likeSummary	String | | 是否点赞， true-点赞了， false-没有
	 * fileUrlIds	String | | 附件ID串，使用,分隔
	 * commentId	Long | | 使用草稿的评论ID  
	 * </pre>
	 * @return  Map<String, String>
	 * @throws BusinessException
	 */
	@POST
	@Path("newEdocSummaryFinishWorkItem/{affairId}")
	public Response newEdocSummaryFinishWorkItem(@PathParam("affairId") Long affairId, Map<String, Object> params) throws BusinessException {
		Map<String, String> ret = new HashMap<String, String>();
	    User user = AppContext.getCurrentUser();
	    
	    CtpAffair affair = null;
	    //ColSummary summary = null;
	    EdocSummary summary = null;
	    boolean canDeal = false;
	    boolean isLock = false;
	    try {
	    	     
	        LOGGER.info(user.getName() + "," + AppContext.getCurrentUser().getUserAgentFrom() + ", Rest开始处理:" + affairId);

	        String jsonStr = ParamUtil.getString(params, JSON_PARAMS, "");
	        putThreadContext(jsonStr);

	        //获取对象
	        affair = affairManager.get(affairId);
	        if(affair==null){
	        	//ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
	        	ret.put(ERROR_KEY, "没有找到相关数据");
	        	return ok(ret);
	        }
	         // summary = collaborationApi.getColSummary(affair.getObjectId());
	        summary =govdocSummaryManager.getSummaryById(affair.getObjectId());
	        //检验锁和状态是否正常
	        canDeal = checkCanDeal(affair, ret);
	        if(canDeal){
	        	isLock = govdocLockManager.canGetLock(affairId);
	        	if(!isLock) {
	        		canDeal = false;
	        		ret.put(ERROR_KEY, "同时项两个人正在同时处理");
	        		LOGGER.error( AppContext.currentUserLoginName()+"同时项两个人正在同时处理。affairId"+affairId);
	        	}
	        }
	        
	        if(canDeal){
	            //WorkflowBpmContext arg0, CPMatchResultVO arg1
	            //WFAjax.transBeforeInvokeWorkFlow(context, resultVO);
	            //TODO 预提交， 加锁。。。
	        }
	        
	        if (canDeal) {
	        	
	        	//需要更新全文签批文件关联
                long aipFileId = ParamUtil.getLong(params, "aipFileId", -1L);
                if (aipFileId != -1) {
                	updateQwqp(summary.getId(), aipFileId);
                }

	            // 意见
	            Comment comment = formComment(summary, affair, user, ColHandleType.finish, params);

	            // 处理参数
	            Map<String, Object> dealParams = new HashMap<String, Object>();
	            
	            //跟踪
	          //  dealParams.put("trackParam", formTrackParam(params));
	            //处理后归档
	            String archiveValue = ParamUtil.getString(params, "archiveValue", "");
	            dealParams.put("archiveValue", archiveValue);
	            
	            GovdocDealVO finishVO = new GovdocDealVO();
	            finishVO.setSummary(summary);
	            finishVO.setAffair(affair);
	            finishVO.setComment(comment);

	          //取出模板信息
	            //Map<String,Object> templateMap = (Map<String,Object>)ParamUtil.getJsonDomain("colSummaryData");
	            if(summary.getTempleteId() != null){
	            	CtpTemplate template = templateManager.getCtpTemplate(summary.getTempleteId());
	            	dealParams.put("templateColSubject", template.getColSubject());
	            	dealParams.put("templateWorkflowId", template.getWorkflowId());
	            	finishVO.setTemplate(template);
	            }
	            
	            // 处理后归档
	            finishVO.setPigeonholeValue(archiveValue);
	            //跟踪
	            finishVO.setTrackPara(formTrackParam(params));
	            //续办取回添加接口数据，用于取回
	            String xbIds = ParamUtil.getString(params, "xbIds", "");
	        	if(null != xbIds && !"".equals(xbIds)){//续办回退特殊标记
	        		finishVO.setCustomDealWithActivitys(xbIds);
	        		govdocContinueManager.setCustomAffairExt(finishVO);
	        	}
	        	String docMark2 = ParamUtil.getString(params, "docMark2", "");
	        	if(null != docMark2 && !"".equals(docMark2)){
	        		finishVO.getSummary().setDocMark2(docMark2);
	        	}
	        	
	        	
	        	boolean isSignMark = false;//是否签收
	        	//如果是签收，如果是签收并分办节点处理时
	        	List<String> actionList = getActions(affair, summary);
	        	 if(actionList.contains("ReSign")){
	            	 GovdocExchangeDetail govdocExchangeDetails;
	            	 try {
	            		 govdocExchangeDetails = govdocExchangeManager.findDetailBySummaryId(affair.getObjectId());
	            		 if(govdocExchangeDetails != null && govdocExchangeDetails.getStatus()==ExchangeDetailStatus.waitSign.getKey()){//待签收
	            			 isSignMark = true;
	      	      	 	}
	            	 } catch (BusinessException e) {
	            		 LOGGER.error("",e);
	            		 //e.printStackTrace();
	            	 }
	            }
	        	 	
	        	if(actionList.contains("Distribute") && isSignMark){//如果签收编号有值并且有分办，那么是签收和分办，走暂存逻辑
	                govdocManager.transFinishWorkItemPublic(finishVO, ColHandleType.wait, dealParams);
	        	}else{
	                govdocManager.transFinishWorkItemPublic(finishVO, ColHandleType.finish, dealParams);
	        	}  
	            // 签收
	    		if (isSignMark) {
	    			govdocManager.transSign(finishVO);
	    		}
	            if(ret.isEmpty()){
	            	ret.put("succ_msg", "处理成功!");
	            }
	        }

	    } catch (Exception e) {
	        ret.put(ERROR_KEY, e.getMessage());
	        LOGGER.error("处理系统异常", e);
	    } finally {
	        if(isLock){
				//colLockManager.unlock(affairId);
	        	govdocLockManager.unlock(affairId);
			}
	        removeThreadContext();
	        if(canDeal){
	        	//unlockCollAll(affairId,affair,summary);
	        	govdocLockManager.colDelLock(summary, affair);
	        }
	    }

	  //结果封装
	    if(ret.containsKey(ERROR_KEY)){
	        ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
	    }else {
	        ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
	    }
	    
	    return ok(ret);
	}
	
	/**
     * 完善用户信息，rest接口获取不全
     * @param user	用户
     * @throws BusinessException 
     */
    private void fixUser(User user) throws BusinessException{
        
        V3xOrgMember member = orgManager.getMemberById(user.getId());
        user.setDepartmentId(member.getOrgDepartmentId());
        
        V3xOrgAccount account = orgManager.getAccountById(user.getLoginAccount());
        user.setLoginAccountName(account.getName());
        user.setLoginAccountShortName(account.getShortName());
    }
    
    /**
     * 是否从业务生成器生成的列表中穿透过来
     * @return
     */
    private String IsfromBizList(String openFrom){
    if("listSentAllBiz".equals(openFrom)){
    	openFrom = "listSent";
     }else if("listWaitSendAllBiz".equals(openFrom)){
    	 openFrom = "listWaitSend" ;
	}else if("listDoneAllBiz".equals(openFrom)){
		openFrom ="listDone"; 
	}else if("listPendingAllBiz".equals(openFrom)){
		openFrom= "listPending";
	 }
	return openFrom;
    }
    
    /**
     *  获取公文正文
     * @param summary ColSummary| 必填 |  协同对象
     * @param affair  CtpAffair| 必填 | affair对象
     * @param openFrom String | 必填 | 来源，用于权限判断， 目前不知道这个参数干什么用
     * @return 
     * 
     */
    private GovdocSummaryContentVO findSummaryContent(EdocSummary summary, CtpAffair affair, CtpTemplate template,String openFrom, String pigeonholeType, String operationId) {

        GovdocSummaryContentVO contentVO = null;

        // 正文
        try {
          
			String rightId = "";

			// 归档的时候有视图设置， 直接使用归档的视图设置
			if (ColOpenFrom.docLib.name().equals(openFrom)) {
				boolean isAcountPighole = String.valueOf(PigeonholeType.edoc_account.ordinal()).equals(pigeonholeType);
				// String docLibrightid =  ContentUtil.findRightIdbyAffairIdOrTemplateId(affair,template,isAcountPighole,"");
				// operationId = docLibrightid;
			}

			if (Strings.isBlank(operationId) || ColOpenFrom.subFlow.name().equals(openFrom)) {
				Map<String, Object> vomMap = new HashMap<String, Object>();
				vomMap.put("formAppid", summary.getFormAppid());
				vomMap.put("govdocType", summary.getGovdocType());
				rightId = govdocFormManager.getGovdocFormViewRight(vomMap, affair);
			} else {
				rightId = operationId;
				if (Strings.isNotBlank(rightId)) {
					rightId = rightId.replaceAll("[|]", "_");
				}
			}
			capFormManager = (CAPFormManager)AppContext.getBean("capFormManager");
        	capFormManager.addRight(rightId);


            LOGGER.info(AppContext.currentUserName()+"查找正文调用接口："+",summary.getId():"+summary.getId()+",rightId:"+rightId);
           
            List<CtpContentAllBean> contentList = ctpMainbodyManager.transContentViewResponse(ModuleType.edoc,
                    summary.getId(), CtpContentAllBean.viewState_readOnly, rightId, 0, -1l);
            
            int size = contentList.size();
            LOGGER.info(AppContext.currentUserName()+"正文列表："+size+",id:"+ (size >0 ? contentList.get(0).getId() : -1l));

            
            CtpContentAllBean contentAllBean = contentList.get(0);

            contentVO = GovdocSummaryContentVO.valutOf(contentAllBean);
            contentVO.setFormRightId(rightId);
            
            if(Integer.valueOf(MainbodyType.FORM.getKey()).equals(contentAllBean.getContentType())){
                //表单不需要正文
                contentAllBean.setContentHtml("");
                
                //多视图或正文
                if(contentList.size() > 0){
                    List<Map<String, String>> cList = new ArrayList<Map<String,String>>(contentList.size());
                    int index = -1;
                    for(CtpContentAllBean b : contentList){
                        index++;
                        Map<String, Object> ext = b.getExtraMap();
                        if(ext != null){
                            Map<String, String> m = new HashMap<String, String>();
                            m.put("index", String.valueOf(index));
                            m.put("viewTitle", String.valueOf(ext.get("viewTitle")));
                            m.put("isOffice", ext.get("isOffice") == null ? "false" : String.valueOf(ext.get("isOffice")));
                            m.put("isLightForm", ext.get("isLightForm") == null ? "false" : String.valueOf(ext.get("isLightForm")));
                            
                            String content = b.getContent();
                            if("true".equals(m.get("isOffice")) && content != null){
                                contentVO.setHasOffice(true);
                                Map<String, String> c = JSONUtil.parseJSONString(content, Map.class);
                                m.put("extension", c.get("extension"));
                                m.put("fileId", c.get("fileId"));
                                if(Strings.isDigits(contentAllBean.getContent())){
                            		Long contentId = Long.valueOf(contentAllBean.getContent());
                            		List<Attachment> attachments = attachmentManager.getByReference(contentAllBean.getContentDataId(), contentId);
                            		if(attachments != null){
                            			V3XFile v3XFile = fileManager.getV3XFile(attachments.get(0).getFileUrl());
                                    	if(v3XFile != null){
                                    		m.put("lastModified",DateUtil.format(v3XFile.getUpdateDate(), DateUtil.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_PATTERN));
                                    	}
                            		}
                            	}
                            }
                            cList.add(m);
                        }
                    }
                    contentVO.setContentList(cList);
                }
            }
            
            //表单转发协同
            if(Strings.isNotBlank(summary.getForwardMember()) 
                    && summary.getParentformSummaryid() != null && !summary.getCanEdit()){
                
                String contentHtml = contentAllBean.getContentHtml();
                if(contentHtml.indexOf("class=\"lightForm-phonePage\"") == -1){
                    //移动表单的视图

                    //表单转发的时候由表单组件进行查看
                    contentAllBean.setContentHtml("");
                    contentVO.setForwardForm(true);
                }
            }
            
            //非html或表单正文需要获取最后一次更新时间
            if(!Integer.valueOf(MainbodyType.HTML.getKey()).equals(contentAllBean.getContentType())
                    && !Integer.valueOf(MainbodyType.FORM.getKey()).equals(contentAllBean.getContentType())
                    && contentAllBean.getContentDataId() != null){
                
                V3XFile v3XFile = fileManager.getV3XFile(contentAllBean.getContentDataId());
                if(v3XFile != null){
                    contentVO.setLastModified(DateUtil.format(v3XFile.getUpdateDate(), DateUtil.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_PATTERN));
                }
            }

        } catch (Exception e) {
            LOGGER.error("获取协同正文失败.", e);
        }

        return contentVO;
    }
    
    /**
     * 节点权限，控制页面显示等设置， 页面参数，权限等相关配置
     * @param pageConfig Map<String, Object> || 其他参数
     * @param openFrom String || 打开来源
     * @param affair CtpAffair || 事项信息
     * @param summary ColSummary || 协同信息
     * @param user 用户
     * @throws BusinessException
     */
    private void affairNodeProperty(Map<String, Object> pageConfig,
            String openFrom, CtpAffair affair, EdocSummary summary, User user) throws BusinessException{
        
    	//设置是否可以进行回复
        boolean canReply = new Integer(CollaborationEnum.flowState.run.ordinal()).equals(summary.getState()) 
        					&& (ColOpenFrom.listPending.name().equals(openFrom) 
        								|| ColOpenFrom.listDone.name().equals(openFrom) 
        								|| ColOpenFrom.listSent.name().equals(openFrom)
        								|| EdocOpenFrom.listDoneAll.name().equals(openFrom)
        								|| (ColOpenFrom.listWaitSend.name().equals(openFrom) && Integer.valueOf(SubStateEnum.col_pending_specialBacked.getKey()).equals(affair.getSubState())));

        pageConfig.put("canReply", canReply);
        
        boolean isFinish = new Integer(CollaborationEnum.flowState.finish.ordinal()).equals(summary.getState())
        		|| new Integer(CollaborationEnum.flowState.terminate.ordinal()).equals(summary.getState());
        //是否可以添加附言
        pageConfig.put("canAddSenderComment", affair.getSenderId().equals(user.getId()) && canReply);
        
        //处理状态
        boolean canDeal = false;
        if(ColOpenFrom.listPending.name().equals(openFrom) && affair.getState() == StateEnum.col_pending.getKey()
                && !Integer.valueOf(SubStateEnum.col_pending_specialBack.getKey()).equals(affair.getSubState())){
            canDeal = true;
        }
        pageConfig.put("canDeal", canDeal); 
        
        Long AccountId = summary.getOrgAccountId();
 	   	if(summary.getFormAppid() != null && summary.getFormAppid() != 0){
	   		  FormBean formBean = formApi4Cap3.getForm(summary.getFormAppid());
	   		  if(formBean != null){//取表单模板的 所有者的单位id
	   			  AccountId = formBean.getOwnerAccountId();
	   		  }
	   	 }
        // 封装节点权限
     	String configItem = collaborationApi.getPolicyByAffair(affair).getId();
     	String category = PermissionFatory.getPermBySubApp(summary.getGovdocType(), configItem).getCategorty();
     	Permission permission = null;
     	try {
     		permission = permissionManager.getPermission(category, configItem, AccountId);
     	}catch(Exception e) {
    		LOGGER.error("获取节点权限报错category:" + category + " caonfigItem:" + configItem + " accountId:" + AccountId, e);
    	}
        //Permission permission =this.getPermisson(affair, summary);
        //用于判断当前节点权限是否存在
        if(permission != null && canDeal) { 
            if (!permission.getName().equals(affair.getNodePolicy())) {
                pageConfig.put("noFindPermission", true);
                pageConfig.put("newPermission", permission.getName());
            }
        }
        
        List<String> basicActionList = permissionManager.getActionList(permission, PermissionAction.basic);
        List<String> commonActionList = permissionManager.getActionList(permission, PermissionAction.common);
        List<String> advanceActionList = permissionManager.getActionList(permission, PermissionAction.advanced);
        if(basicActionList.contains("Edit") && !summary.get_canEdit()){
        	basicActionList.remove("Edit");
        }
        if(basicActionList.contains("Edit") || commonActionList.contains("Edit") || advanceActionList.contains("Edit")){
        	if(affair.getState() == StateEnum.col_pending.getKey()){//公文只有处于“待办”状态，才能修改正文、pdf圈阅等操作
        		pageConfig.put("canEditContent", true);
        	}
        }
        
        
        //根据条件移除转发操作
        List<String> hasForWardList = null;
        String forwardAction = "Forward";
        if(commonActionList.contains(forwardAction)){
            hasForWardList = commonActionList;
        }else if(advanceActionList.contains(forwardAction)){
            hasForWardList = advanceActionList;
        }
        
        if(hasForWardList != null){
            
            //如果是待发、已发列表则判断新建权限能否转发
            if(!summary.getCanForward()){
                hasForWardList.remove(forwardAction);
            }else if(affair.getState() == StateEnum.col_waitSend.getKey() 
                    || affair.getState() == StateEnum.col_sent.getKey()) {
                Permission permission1 = permissionManager.getPermission(EnumNameEnum.col_flow_perm_policy.name(), "newCol", user.getLoginAccount());
               // NodePolicyVO nodePolicy = new NodePolicyVO(permission1);
           /*     if (!nodePolicy.isForward()) {
                    hasForWardList.remove(forwardAction);
                }*/
            } 
        } 
        
        //不同意操设置
        if(permission!=null){
        	 pageConfig.put("customAction", getCustomAction(permission));
        }
           
        
        //已发或待发列表
        String track = "Track";
        if(!basicActionList.contains(track) 
                && (affair.getState() == StateEnum.col_sent.getKey() || affair.getState() == StateEnum.col_waitSend.getKey())){
            basicActionList.add(track);
        }
       
        //VJOIN外部人员不能使用常用语
        if (!user.isV5Member()) { 
            basicActionList.remove("CommonPhrase"); 
        }
        
        pageConfig.put("nodeActions", basicActionList);//基本操作和常用操作集合
        pageConfig.put("commonActions", commonActionList);//常用操作
        pageConfig.put("advanceActions", advanceActionList);//高级操作
        
        if(permission!=null){
        NodePolicy nodePolicy = permission.getNodePolicy();
        String attitude = nodePolicy.getAttitude();
        //态度按钮显示设置
        
        StringBuffer nodeattitude = new StringBuffer();
		DetailAttitude detailAttitude = permission.getNodePolicy().getDatailAttitude();
		String defaultAttitude = permission.getNodePolicy().getDefaultAttitude();
    	if (Strings.isNotBlank(attitude)) {
    		List<Map<String, String>> attitudeList = new ArrayList<Map<String, String>>();
    		String[] attitudeArr = attitude.split(",");
    		for (String att : attitudeArr) {
    			Map<String, String> attitudeMap = new HashMap<String,String>();
    			if(Strings.isNotBlank(att)){//升级数据左边有空格。。。。
    				att = att.trim();
    			}
    			if ("haveRead".equals(att)) {       
    				attitudeMap.put("value", detailAttitude.getHaveRead());
    				attitudeMap.put("showValue", ResourceUtil.getString(detailAttitude.getHaveRead()));
    				nodeattitude.append(detailAttitude.getHaveRead());
    			} else if ("agree".equals(att)) {
    				attitudeMap.put("value", detailAttitude.getAgree());
    				attitudeMap.put("showValue", ResourceUtil.getString(detailAttitude.getAgree()));
    				nodeattitude.append(detailAttitude.getAgree());
    			} else if ("disagree".equals(att)) {
    				attitudeMap.put("value", detailAttitude.getDisagree());
    				attitudeMap.put("showValue", ResourceUtil.getString(detailAttitude.getDisagree()));
    				nodeattitude.append(detailAttitude.getDisagree());
    			}
    			if(Strings.isNotBlank(att)){
    				attitudeMap.put("code", att);
    				attitudeList.add(attitudeMap);
    			}
    		}
    		//默认态度没在显示态度里面的时候取显示态度的第一个
    		if(!java.util.Arrays.asList(attitudeArr).contains(defaultAttitude)){
    			defaultAttitude = attitudeArr[0];
    		}
    		pageConfig.put("attitudeBtns", attitudeList);
    		pageConfig.put("defaultAttitude", defaultAttitude);
    	}
/*        String defaultAttitude = nodePolicy.getDefaultAttitude();
		//默认态度没在显示态度里面的时候取显示态度的第一个
		if(!java.util.Arrays.asList(attitudeArr).contains(defaultAttitude)){
			defaultAttitude = attitudeArr[0];
		}*/
        
		CustomAction customAction = nodePolicy.getCustomAction();
        if (customAction != null) {
        	String isOptional = customAction.getIsOptional();
        	String optionalAction = customAction.getOptionalAction();
        	String defaultAction = customAction.getDefaultAction();
        	pageConfig.put("isOptional", isOptional);
        	pageConfig.put("optionalAction", optionalAction);
        	pageConfig.put("defaultAction", defaultAction);
        	
        }
        //指定回退提交模式
        pageConfig.put("submitStyleCfg", nodePolicy.getSubmitStyle());
         
        //意见必填设置
        //得到所有'意见不能为空'的节点权限名称 集合,用于协同待办列表，如果该Affair的节点权限意见不能为空，则不能直接删除和归档.
        boolean forceComment = isExpectValue(nodePolicy.getOpinionPolicy(), 1);
        boolean forceCommentWhenCancel = isExpectValue(nodePolicy.getCancelOpinionPolicy(), 1);
        boolean forceCommentWhenDisagree = isExpectValue(nodePolicy.getDisAgreeOpinionPolicy(), 1);
        pageConfig.put("forceComment", forceComment);
        pageConfig.put("forceCommentWhenCancel", forceCommentWhenCancel);
        pageConfig.put("forceCommentWhenDisagree", forceCommentWhenDisagree);
        }
        //是否能收藏  和PC端逻辑一致
        boolean isContainOperation  = (Boolean)basicActionList.contains("Archive");
        //已发、待发事项都是受新建的归档限制
        if (StateEnum.col_sent.getKey() == affair.getState()
                ||StateEnum.col_waitSend.getKey() == affair.getState()) {
        	isContainOperation  = (Boolean)basicActionList.contains("Pigeonhole");
        }
        boolean isSenderOrCanArchive = Boolean.TRUE.equals(summary.getCanArchive()) ;
        boolean hasResourceCode = (AppContext.getCurrentUser().hasResourceCode("F04_docIndex")||AppContext.getCurrentUser().hasResourceCode("F04_myDocLibIndex")||AppContext.getCurrentUser().hasResourceCode("F04_accDocLibIndex")
        		||AppContext.getCurrentUser().hasResourceCode("F04_proDocLibIndex")||AppContext.getCurrentUser().hasResourceCode("F04_eDocLibIndex")||AppContext.getCurrentUser().hasResourceCode("F04_docLibsConfig"));
        hasResourceCode =  hasResourceCode && AppContext.hasPlugin("doc");
        String systemVer = AppContext.getSystemProperty("system.ProductId");
      //A6没有收藏功能。相对就没有权限
        boolean isA6 = false;
        if ("0".equals(systemVer) || "7".equals(systemVer)) {
            isA6 = true;
        }
        //否能收藏的权限无关
        String propertyFav = SystemProperties.getInstance().getProperty("doc.collectFlag");
        boolean propertyFavFlag = "true".equals(propertyFav);
        boolean canFavorite = isContainOperation && isSenderOrCanArchive && hasResourceCode && !isA6 && propertyFavFlag;
        if(!canFavorite){
        	LOGGER.info("協同ID：<"+summary.getId()+">,没有收藏权限,isContainOperation:"+isContainOperation+" isSenderOrCanArchive:"+isSenderOrCanArchive+" hasResourceCode："+hasResourceCode+",propertyFav:"+propertyFav+",!isA6:"+!isA6);
        }
        pageConfig.put("canFavorite", canFavorite);
        /** 根据权限配置控制正文是否显示 */
        if ("listSent".equals(openFrom) || "listWaitSend".equals(openFrom) || "formQuery".equals(openFrom)
				|| "edocStatics".equals(openFrom)) {// 已发和待发时可以查看正文
        	pageConfig.put("isShowContent", true);
		}else{
			FormBean formBean = formApi4Cap3.getForm(summary.getFormAppid());
			// 获取表单中设置的正文显示
			String showContentFlag = "true";// 默认显示正文
			FormPermissionConfig formPermissionConfig = formApi4Cap3.getConfigByFormId(formBean.getId());
			if (null != formPermissionConfig) {
				Map<String, String> conentShowMap = (Map<String, String>) JSONUtil.parseJSONString(formPermissionConfig.getShowContentConfig());
				if (null != conentShowMap) {
					for (String key : conentShowMap.keySet()) {
						if (permission != null && permission.getFlowPermId().longValue() == Long.valueOf(key)) {
							showContentFlag = conentShowMap.get(key);
							break;
						}
					}
				}
			}
			if ("true".equals(showContentFlag)) {
				pageConfig.put("isShowContent", true);
			} else {
				pageConfig.put("isShowContent", false);
			}
		}
        if(permission != null && permission.getNodePolicy() != null) {
			pageConfig.put("formDefaultShow", permission.getNodePolicy().getFormDefaultShow());
        }
        
        //如果是从文档中心或者收藏列表穿透过来的，不允许修改正文
        if("docLib".equals(openFrom)){
        	pageConfig.put("canEditContent", false);
        }
    }
    
    //是否为预期的值
    private boolean isExpectValue(Integer i, Integer expect){
        boolean ret = false;
        if(i != null && i.equals(expect)){
            ret = true;
        }
        return ret;
    }
    /**
     * 是否有归档权限
     * 
     * @return
     *
     * @Since A8-V5 6.1
     * @Author      : xuqw
     * @Date        : 2017年5月20日上午10:36:54
     *
     */
    private boolean canArchive(User user){
        
        boolean ret = false;
        
        ret = AppContext.hasPlugin(ApplicationCategoryEnum.doc.name());
        
        if(ret){
            boolean hasResource = false;
            String[] docCodes = new String[]{"F04_docIndex", "F04_myDocLibIndex", "F04_accDocLibIndex",
                                 "F04_proDocLibIndex", "F04_eDocLibIndex", "F04_docLibsConfig"};
            
            for(int i = 0, size = docCodes.length; i < size && !hasResource; i++){
                hasResource = user.hasResourceCode(docCodes[i]);
            }
            ret = hasResource;
        }
        
        return ret;
    }
    /**
     * 发起人附言设置
     * @param summary ColSummary | 必填 | 协同对象
     * @throws BusinessException
     */
    private List<GovdocSummaryCommentVO> summarySenderComments(EdocSummary summary, boolean isHistory) throws BusinessException{
        
        List<Comment> senderCommontList = commentManager.findCommentByType(ModuleType.edoc, 
                summary.getId(), Comment.CommentType.sender, null, false, isHistory);
        return GovdocSummaryCommentVO.valueOf(senderCommontList);
    }
    
  //设置公文为已读
    private void updateAffairState(CtpAffair affair){
        Integer sub_state = affair.getSubState();
        if (sub_state == null || sub_state.intValue() == SubStateEnum.col_pending_unRead.key()) {
            affair.setSubState(SubStateEnum.col_pending_read.key());
            try {
                affairManager.updateAffair(affair);
              //要把已读状态写写进流程
                if (affair.getSubObjectId() != null) {
                    wapi.readWorkItem(affair.getSubObjectId());
                }
            } catch (BusinessException e) {
                LOGGER.error("更新协同已读状态错误", e);
            }
        }
    }
    
  //获取默认节点权限
    public PermissionVO getDefaultPermission(int subApp,Long orgAccountId) throws BusinessException{
 	   String category="";
 	   if(subApp == ApplicationSubCategoryEnum.edoc_fawen.getKey()){  //发文
 		   category =EnumNameEnum.edoc_new_send_permission_policy.name();
        }else if(subApp == ApplicationSubCategoryEnum.edoc_shouwen.getKey()){  //收文
     	   category =EnumNameEnum.edoc_new_rec_permission_policy.name();
        }else if(subApp == ApplicationSubCategoryEnum.edoc_qianbao.getKey()){  //签报
     	   category =EnumNameEnum.edoc_new_qianbao_permission_policy.name();
        }else if(subApp == ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()){  //交换
     	   category=EnumNameEnum.edoc_new_change_permission_policy.name();
 		}
 	
 	return this.permissionManager.getDefaultPermissionByConfigCategory(category, orgAccountId);
 	   
    }
    
    /**
     * 判断当前节点移动端是否能够处理
     * @return
     */
    private Boolean iscanHandleByMobile(String nodePolicy,String listType,List<String> basiclist,CtpAffair affair){
  	   //分送如果有分送策略、分办如果有分办策略、签收节点、移动端不支持处理
        if ((basiclist.contains("FaDistribute"))||
      		  basiclist.contains("JointlyIssued") ||
      		  basiclist.contains("FaDistribute")||
      		 "listWaitSend".equals(listType)||affair.getState() == StateEnum.col_waitSend.getKey()) {
        		
      	  return false;
        	
  		}
        if(!basiclist.contains("ReSign")&& basiclist.contains("Distribute")){
      	  return false;
        }else if(basiclist.contains("ReSign")&& basiclist.contains("Distribute")){
        	 GovdocExchangeDetail govdocExchangeDetails;
  		try {
  			govdocExchangeDetails = govdocExchangeManager.findDetailBySummaryId(affair.getObjectId());
  			 if(govdocExchangeDetails != null && govdocExchangeDetails.getStatus()==2){//已签收
  	          	 return false;
  	      	 }else{
  	      		 return true;//如果签收分办在一个节点，并且美欧签收
  	      	 }
  		} catch (BusinessException e) {
  			LOGGER.error("",e);
  			//e.printStackTrace();
  		}
        	
        }
  	return true;
  	  
    }
    /**
     * 被回退者意见保留设置
     * @param affair
     * @param summary
     */
    private  void  backOptionSet(CtpAffair affair,EdocSummary summary, Map<String, Object> map){
    	Long isFlowBack = affair.getBackFromId();
    	if(isFlowBack != null) {
    		FormOptionExtend govdocFormExtend = formOptionExtendManager.findByFormId(summary.getFormAppid());
    		FormOpinionConfig displayConfig = null;
    		// 公文单显示格式
    		if(null != govdocFormExtend) {
        	    displayConfig = JSONUtil.parseJSONString(govdocFormExtend.getOptionFormatSet(), FormOpinionConfig.class);
    		}
    		if(displayConfig == null){
    		    displayConfig = new FormOpinionConfig();
    		}
    		map.put("opinionType", displayConfig.getOpinionType());
    	
    		if("3".equals(displayConfig.getOpinionType()) || "4".equals(displayConfig.getOpinionType())) {
    			
    			map.put("isAlertStepbackDialog", true);
    		}
    	}
    }
    
    /**
     * 新公文详情页面，评论数量统计
     * @param summaryId Long | 必填 | 协同ID
     * @param cType String || 类型  参见常量 COMMENT_TYPE_LIKE 等
     * @param isHistory Boolean || 类型  是否是转储数据
     * 
     * @throws BusinessException
     */
    private int getCommentCount(Long summaryId, String type, boolean isHistory) throws BusinessException{
        
        FlipInfo finfo = new FlipInfo();
        
        int ret = 0;
        
        //点赞数量
        if(COMMENT_TYPE_LIKE.equals(type)){
            
            commentManager.findLikeComment(ModuleType.edoc, 
                    summaryId, Comment.CommentType.comment, finfo, true, isHistory);
            ret = finfo.getTotal();
        
        }else if (COMMENT_TYPE_AGREE.equals(type)) {
            
            //同意数量
            commentManager.findCommentByAttitude(ModuleType.edoc, 
                    summaryId, Comment.CommentType.comment, finfo, CommentExtAtt1Enum.agree.i18nLabel(), true, isHistory);
            ret = finfo.getTotal();
            
        }else if (COMMENT_TYPE_DISAGREE.equals(type)) {
            
            commentManager.findCommentByAttitude(ModuleType.edoc, 
                    summaryId, Comment.CommentType.comment, finfo, CommentExtAtt1Enum.disagree.i18nLabel(), true, isHistory);
            ret = finfo.getTotal();
            
        } else if(COMMENT_TYPE_ALL.equals(type)) {
            commentManager.findCommentByType(ModuleType.edoc, 
                    summaryId, Comment.CommentType.comment, finfo, true, isHistory);
            ret = finfo.getTotal();
        }
        
        
        return ret;
    }
    
    /**
     * 获取新公文类型
     * @param subApp
     * @return
     */
    private String getNewGovdocType(int subApp){
  	String govdocType="";
  	  if(subApp == ApplicationSubCategoryEnum.edoc_fawen.getKey()){
  		  govdocType =ApplicationCategoryEnum.govdocSend.name();
  	}else if(subApp == ApplicationSubCategoryEnum.edoc_shouwen.getKey()){
  		govdocType=ApplicationCategoryEnum.govdocRec.name();
  	}else if(subApp == ApplicationSubCategoryEnum.edoc_qianbao.getKey()){
  		govdocType=ApplicationCategoryEnum.govdocSign.name();
  	}else if(subApp == ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()){
  		govdocType=ApplicationCategoryEnum.govdocExchange.name();
  	}
  	  return govdocType;
  	  
    }
    
    /**
     * 获取电话会议信息
     * @param map
     */
    private void getMultiCallInfo(Map<String, Object> map){
    	boolean showMeetingBtn = false;
        Map<String, String> meetingParams = new HashMap<String, String>();
        if(AppContext.hasPlugin("multicall")){
        	try {
        		showMeetingBtn = multiCallApi.isShowMultiCallBtn();
        		meetingParams = multiCallApi.getConferenceCallParams();
			} catch (Exception e) {
				LOGGER.info("调用电话会议接口报错：", e);
			}
        }

        map.put("showMeetingBtn", showMeetingBtn);
        map.put("meetingParams", meetingParams);
    }
    
    /*获取当前节点的节点权限*/
    public Permission getPermisson(CtpAffair affair, EdocSummary summary) throws BusinessException{
           Long AccountId = summary.getOrgAccountId();
    	   if(summary.getFormAppid() != null && summary.getFormAppid() != 0){
   	   		  FormBean formBean = formApi4Cap3.getForm(summary.getFormAppid());
   	   		  if(formBean != null){//取表单模板的 所有者的单位id
   	   			  AccountId = formBean.getOwnerAccountId();
   	   		  }
   	   	   }
           String configItem = govdocWorkflowManager.getPolicyByAffair(affair, summary.getProcessId()).getId();
           String category ="";
           if(affair.getSubApp() == ApplicationSubCategoryEnum.edoc_fawen.getKey()){  //发文
         	  category =EnumNameEnum.edoc_new_send_permission_policy.name();
           }else if(affair.getSubApp() == ApplicationSubCategoryEnum.edoc_shouwen.getKey()){  //收文
         	  category =EnumNameEnum.edoc_new_rec_permission_policy.name();
           }else if(affair.getSubApp() == ApplicationSubCategoryEnum.edoc_qianbao.getKey()){  //签报
         	  category =EnumNameEnum.edoc_new_qianbao_permission_policy.name();
           }else if(affair.getSubApp() == ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()){  //交换
         	  category=EnumNameEnum.edoc_new_change_permission_policy.name();
 		  }
          // String category = EnumNameEnum.col_flow_perm_policy.name();

          /* if(permissionAccountId == null){
               LOG.info("permissionAccountId is null ,summaryId:"+summary.getPermissionAccountId());
           }*/

           return permissionManager.getPermission(category, configItem, AccountId);
       }
    
    /**
     * 获取不同意操作策略
     * @param permission
     * @return
     */
    public String getCustomAction(Permission permission){
  	    String advanceAction = permission.getAdvancedOperation();
  		   String commonAction =permission.getCommonOperation();
  		   String[] advanceArray;
  		   String[] commonArray ;
  		   String customAction = "";
  		  ArrayList<String> customString = new ArrayList<String>();
  		  if(advanceAction.indexOf(",")!=-1){
  			  advanceArray=advanceAction.split("\\,");
  		  }else{
  			  advanceArray = new String []{advanceAction};
  		  }
  		  
  		  if(commonAction.indexOf(",")!=-1){
  			  commonArray = commonAction.split("\\,");
  		  }else {
  			  commonArray = new String[]{commonAction};
  		}
  		  
  	  ArrayList<String> customList = new ArrayList<String>();
  	  if(Arrays.asList(advanceArray).contains("Return")||Arrays.asList(commonArray).contains("Return")){  //回退
  		  customList.add("Return");
  	  }
  	  if(Arrays.asList(advanceArray).contains("Terminate")||Arrays.asList(commonArray).contains("Terminate")){  //终止
  		  customList.add("Terminate");
  	  }
  	  if(Arrays.asList(advanceArray).contains("Cancel")||Arrays.asList(commonArray).contains("Cancel")){  //撤销
  		  customList.add("Cancel");
  	  }
  	  return StringUtils.join(customList, ",");          
    }
    
    /**
     *  新公文-将 _json_params 设置到 线程中
     * 
     * @param jsonStr
     *
     * @Since A8-V5 6.1
     * @Author      : xuqw
     * @Date        : 2017年4月6日下午7:35:16
     *
     */
    private void putThreadContext(String jsonStr){
        
        AppContext.putThreadContext(GlobalNames.THREAD_CONTEXT_JSONSTR_KEY, jsonStr);
        
        Object jsonObj = JSONUtil.parseJSONString(Strings.removeEmoji(jsonStr), Map.class);
        AppContext.putThreadContext(GlobalNames.THREAD_CONTEXT_JSONOBJ_KEY, jsonObj);
    }
    /*
     * 转化成String
     */
    private String toString(Object o){
        String ret = "";
        if(o != null){
            ret = o.toString();
        }
        return ret;
    }
    
    /**
     * 处理加锁
     * @param summary	ColSummary | 必填 | 协同对象
     * @param user		User | 必填 | 用户
     * @param type		int | 必填 | 类型
     * @param ret
     * @return	boolean
     * @throws BusinessException 
     */
    private boolean lockWorkFlow(EdocSummary summary, User user, int type, Map<String, String> ret) throws BusinessException{
        boolean canDeal = true;
        String[] wfLockCheck = wapi.checkWorkflowLock(summary.getProcessId(), user.getId().toString(), type);
        if(wfLockCheck == null){
            canDeal = false;
            ret.put(ERROR_KEY, ResourceUtil.getString("workflow.wapi.exception.msg002"));
        }else if("false".equals(wfLockCheck[0])){
            canDeal = false;
            ret.put(ERROR_KEY, wfLockCheck[1]);
        }
        return canDeal;
    }
    
    /**
     * 新公文组装意见
     * @param params
     * @returnsubmitsubmit
     * @throws BusinessException 
     */
    private Comment formComment(EdocSummary summary, CtpAffair affair,
            User user, ColHandleType handleType, Map<String, Object> params) throws BusinessException{
        
        // 意见
        Comment comment = new Comment();

        Long commentId = getLong(params, "commentId", null);
        comment.setId(commentId);

        String content = ParamUtil.getString(params, "content");
        comment.setContent(content);

        // 意见隐藏
        String hide = ParamUtil.getString(params, "hide");
        boolean isHide = "true".equals(hide);
        comment.setHidden(isHide);

        StringBuilder showToIds = new StringBuilder();
        if (isHide) {
            showToIds.append("Member|").append(summary.getStartMemberId());
        }

        comment.setShowToId(showToIds.toString());
        comment.setTitle(summary.getSubject());
        comment.setPid(0l);
        comment.setClevel(1);
        //path先这样判空设置，正常情况肯定能取到值的，待平台方改好后直接取值即可
        comment.setPath(AppContext.getCurrentUser().getUserAgentFrom()==null?"pc":AppContext.getCurrentUser().getUserAgentFrom());
        comment.setModuleType(ApplicationCategoryEnum.edoc.getKey());
        comment.setModuleId(summary.getId());
        comment.setCtype(CommentType.comment.getKey());
        comment.setAffairId(affair.getId());
        
        comment.setCreateDate(DateUtil.currentDate());

        // 态度
        String attitude = ParamUtil.getString(params, "attitude");
        comment.setExtAtt1(attitude);

        // 点赞
        String likeSummary = ParamUtil.getString(params, "likeSummary");
        comment.setPraiseToSummary("true".equals(likeSummary));

        // 附件
        /*String fileUrlIds = ParamUtil.getString(params, "fileUrlIds", null);
        List<Attachment> attrs = createAttachments(summary.getId(), fileUrlIds);
        String relateInfo = "[]";
        if (Strings.isNotEmpty(attrs)) {
            relateInfo = JSONUtil.toJSONString(attrs);
        }*/
        //现在H5传回附件参数为fileJson对象json
        String relateInfo = ParamUtil.getString(params, "fileJson", "[]");
        comment.setRelateInfo(relateInfo);
        
        //at信息
        formatAtWho(comment, affair, params);
        
      //判断是否代理人
        Long userId = user.getId();
        if(!userId.equals(affair.getMemberId())){
            comment.setExtAtt2(user.getName());
        }
        if (handleType == ColHandleType.wait) {
            comment.setExtAtt1("");
            comment.setExtAtt3("collaboration.dealAttitude.temporaryAbeyance");
        } else if(handleType == ColHandleType.specialback || handleType == ColHandleType.stepBack ){
            comment.setExtAtt3("collaboration.dealAttitude.rollback");
        } else if (handleType == ColHandleType.stepStop) {
        	comment.setExtAtt3("collaboration.dealAttitude.termination");
        }
        return comment;
    }
    
    /*
     * 平台的ParamUtil.getLong方法有问题，这里自己实现
     */
    private Long getLong(Map<String, Object> param, String key, Long defualt){
        Long ret = defualt;
        
        if(param != null && param.get(key) != null){
            Object o = param.get(key);
            ret = Long.parseLong(o.toString());
        }
        return ret;
    }
    
    /**
     * 拼装at相关信息
     * 
     * @param comment 评论
     * @param affair 当前事项
     * @param params 评论参数
     * @throws BusinessException 
     *
     * @Author      : xuqw
     * @Date        : 2016年11月20日下午5:22:02
     *
     */
    private void formatAtWho(Comment comment, CtpAffair affair, Map<String, Object> params) throws BusinessException{
        
      //@人员
        String atWhoMembers = ParamUtil.getString(params, "atWhoSelected", "");
        if(Strings.isNotBlank(atWhoMembers) && Strings.isNotBlank(comment.getContent())){
            List<Map<String, String>> atWhos = JSONUtil.parseJSONString(atWhoMembers, List.class);
            if(Strings.isNotEmpty(atWhos)){
                
                Collections.sort(atWhos, new Comparator<Map<String, String>>() {
                    @Override
                    public int compare(Map<String, String> o1, Map<String, String> o2) {
                        
                        int ret = 1;
                        
                        String name1 = o1.get("name");
                        String name2 = o2.get("name");
                        if(name1 != null){
                            if(name2 == null){
                                ret = -1;
                            }else if(name1.length() > name2.length()){
                                ret = -1;
                            }else{
                                ret = 1;
                            }
                        }
                        return ret;
                    }
                });
                
                
                //拼装html
                boolean hasAtAll = false;
                String htmlContent = Strings.toHTML(comment.getContent());
                List<String> fiterList = new ArrayList<String>();
                //用于防止短名字替换长名字， 比如 ： user1, user111， 这种情况
                Map<String, String> richText = new HashMap<String, String>();
                
                for(Map<String, String> item : atWhos){
                    if(fiterList.contains(item.get("name"))){
                        continue;
                    }
                    StringBuilder it = new StringBuilder("<button class=\"atwho-inserted\" ");
                    it.append(" checkaffair=\"").append(affair.getId()).append("\"");
                    it.append(" data-atwho-at-query=\"@\"");
                    it.append(" data-atwho-at-validate=\"@").append(item.get("name")).append("\"");
                    it.append(" atinfo=\"@{affairId:'").append(item.get("affairId")).append("',memberId:'").append(item.get("memberId")).append("'}\"");
                    it.append(" onclick=\"return false;\" contenteditable=\"false\" style=\"color: rgb(49, 142, 217); font-size: 14px; border-width: 0px; padding: 0px; margin: 0px; background-color: transparent; background-image: none;\"");
                    it.append(">@").append(item.get("name")).append("</button>");
                    
                    if("All".equals(item.get("affairId"))){
                        hasAtAll = true;
                    }
                    fiterList.add(item.get("name"));
                    String newKey = UUID.randomUUID().toString();
                    richText.put(newKey, it.toString());
                    htmlContent = htmlContent.replace("@" + item.get("name"), newKey);
                }
                
                if(richText.size() > 0){
                    for(String key : richText.keySet()){
                        htmlContent = htmlContent.replace(key, richText.get(key));
                    }
                }
                
                if(hasAtAll){
                    Map<String, String> p = new HashMap<String, String>();
                    p.put("summaryId", affair.getObjectId().toString());
                    List<Map<String, Object>> ret = pushMessageToMembersList(p,false);
                    if(Strings.isNotEmpty(ret)){
                        List<Map<String, String>> pushMembers = new ArrayList<Map<String,String>>();
                        for(Map<String, Object> item : ret){
                            Map<String, String> m = new HashMap<String, String>();
                            m.put("affairId", item.get("id").toString());
                            m.put("memberId", item.get("memberId").toString());
                            pushMembers.add(m);
                        }
                        comment.setAtAllMembers(JSONUtil.toJSONString(pushMembers));
                    }
                }
                //单独处理M3 at部分人员发送系统消息  at by liusy 2018-05-19  是否对其它有影响待跟进
                if(!hasAtAll && Strings.isNotEmpty(atWhos)){
                	
                	ArrayList<Map<String,String>> als = new ArrayList<Map<String,String>> ();
                	for(Map<String, String> item : atWhos){
                		Map<String,String> ls = new HashMap<String,String>();
                		ls.put("affairId", item.get("affairId"));
                		ls.put("memberId", item.get("memberId"));
                		als.add(ls);
                	}
                	comment.setPushMessageToMembers(JSONUtil.toJSONString(als));
                    comment.setPushMessage(true);
                }
                comment.setRichContent(htmlContent);
            }
        }
    }
    
    public List<Map<String, Object>> pushMessageToMembersList(Map<String, String> params, boolean needPost) throws BusinessException{
        Long summaryId = Long.valueOf(params.get("summaryId"));
        
        List<StateEnum> states = new ArrayList<StateEnum>();
        states.add(StateEnum.col_sent);
        states.add(StateEnum.col_done);
        states.add(StateEnum.col_pending);
        states.add(StateEnum.col_waitSend);

        List<CtpAffair> pushMessageList = affairManager.getAffairs(summaryId, states);
        //排序顺序规则，发起人、已办、暂存待办
        Collections.sort(pushMessageList, new Comparator<CtpAffair>() {
            @Override
            public int compare(CtpAffair o1, CtpAffair o2) {
                if (o1.getState() == StateEnum.col_sent.getKey())
                    return -1;
                else if (o2.getState() == StateEnum.col_sent.getKey())
                    return 1;
                else {
                    if (o1.getState() == StateEnum.col_done.key())
                        return -1;
                    else if (o2.getState() == StateEnum.col_done.key())
                        return 1;
                    else
                        return 0;
                }
            }
        });
        //过滤掉自己和重复项
        Map<Long,Boolean> memberIdMap = new HashMap<Long, Boolean>(pushMessageList.size());
        
        List<Map<String, Object>> rset = new ArrayList<Map<String, Object>>();
        Long currentUserId = AppContext.currentUserId();
        for (CtpAffair r : pushMessageList) {
            //只显示已发、暂存待办和、已办的、回退者
            int subState = r.getSubState();
            int state = r.getState();
            if ((subState == SubStateEnum.col_pending_ZCDB.key() && state == StateEnum.col_pending
                    .key()) || state == StateEnum.col_done.key() || state == StateEnum.col_sent.key()
                    || subState == SubStateEnum.col_pending_specialBack.key()
                    || subState == SubStateEnum.col_pending_specialBacked.key()
                    || subState == SubStateEnum.col_pending_specialBackCenter.key()
                    || state == StateEnum.col_pending.key()) {
                Long memberId = r.getMemberId();
                if (!memberId.equals(currentUserId) && memberIdMap.get(memberId) == null/* && !memberIdList.contains(r.getMemberId())*/) {
                    memberIdMap.put(memberId, Boolean.TRUE);
                    
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("state", state);
                    map.put("subState", subState);
                    map.put("memberId", memberId);
                    map.put("backFromId", r.getBackFromId());
                    map.put("id", r.getId());
                    
                    V3xOrgMember member = orgManager.getMemberById(memberId);
                    map.put("name", member.getName());
                    map.put("phone", member.getTelNumber());
                    
                    //加载岗位信息
                    if(needPost){
                        V3xOrgPost post = orgManager.getPostById(member.getOrgPostId());
                        if(post != null){
                            map.put("postName", post.getName());
                        }
                    }
                    
                    if(state == StateEnum.col_sent.getKey()){
                        map.put("i18n", ResourceUtil.getString("cannel.display.column.sendUser.label"));
                    }else if(state == StateEnum.col_pending.getKey()){
                        map.put("i18n", ResourceUtil.getString("collaboration.default.currentToDo"));
                    }else if(state == StateEnum.col_done.getKey()){
                        map.put("i18n", ResourceUtil.getString("collaboration.default.haveBeenProcessedPe"));
                    }else if(subState == SubStateEnum.col_pending_specialBack.getKey()){
                        map.put("i18n", ResourceUtil.getString("collaboration.default.stepBack"));
                    }else if(subState == SubStateEnum.col_pending_specialBackCenter.getKey()){
                        map.put("i18n", ResourceUtil.getString("collaboration.default.specialBacked"));
                    }else if(state == StateEnum.col_waitSend.getKey() && subState == SubStateEnum.col_pending_specialBacked.getKey()){
                        map.put("i18n", ResourceUtil.getString("cannel.display.column.sendUser.label"));
                    }else{
                        map.put("i18n", ResourceUtil.getString("collaboration.default.stagedToDo"));
                    }
                    
                    rset.add(map);
                }
            }
        }
        return rset;
    }
    
    /*
     * 新公文-检验协同是否可以处理，防止两个客户端同时处理
     */
    private boolean checkCanDeal(CtpAffair affair, Map<String, String> ret) throws NumberFormatException, BusinessException{
        
        boolean canDeal = isValidAffair(affair.getId().toString(), ret);
        
        Long affairId = affair.getId();
        
      //锁检验
       
        
        if(canDeal){
          //状态检验
            String msg = checkAffairState(affair, StateEnum.col_pending);
            if(Strings.isNotBlank(msg)){
                canDeal = false;
                ret.put(ERROR_KEY, msg);
            }
        }
        
        if(canDeal){
          //检查代理，避免不是处理人也能处理了。
            String agentMsg =  WFComponentUtil.ajaxCheckAgent(affair.getMemberId(), affair.getSubject(), ModuleType.collaboration);
            if(Strings.isNotBlank(agentMsg)){
                canDeal = false;
                ret.put(ERROR_KEY, agentMsg);
            }
        }
        
        return canDeal;
    }
    
    /**
     * 新公文-原PC验证方式验证方式
     * @return
     * @throws BusinessException 
     * @throws NumberFormatException 
     */
    private boolean isValidAffair(String affairId, Map<String, String> ret) throws NumberFormatException, BusinessException{
        
        boolean canDeal = true;
      //状态检验
        String msg = edocManager.checkAffairValid(affairId);
        if(Strings.isNotBlank(msg)){
            canDeal = false;
            ret.put(ERROR_KEY, msg);
        }
        return canDeal;
    }
    
    /**
     * 新公文处理时查看affair的状态
     * @param affair
     * @param expact
     * @return
     */
    private String checkAffairState(CtpAffair affair, StateEnum expact){
        
        String ret = "";
        
        if(affair==null || affair.getState() != expact.key()){
            ret = WFComponentUtil.getErrorMsgByAffair(affair);
        }
        return ret;
    }
    
    /**
     * 组装跟踪参数
     * @param params
     * @return
     */
    private Map<String, Object> formTrackParam(Map<String, Object> params){
        
     // 跟踪
        Map<String, Object> trackParam = new HashMap<String, Object>();
        String tracking = ParamUtil.getString(params, "tracking");
        if("true".equals(tracking)){
      	  trackParam.put("isTrack", "1");
        }   
        return trackParam;
    }
    
    /**
     * 清理缓存数据
     * 
     *
     * @Since A8-V5 6.1
     * @Author      : xuqw
     * @Date        : 2017年4月5日下午4:05:17
     *
     */
    private void removeThreadContext(){
        AppContext.removeThreadContext(GlobalNames.THREAD_CONTEXT_JSONSTR_KEY);
        AppContext.removeThreadContext(GlobalNames.THREAD_CONTEXT_JSONOBJ_KEY);
    }
    
    public void addSenderComment(Long userId, Long summaryId, String content, String attrs, boolean toSendMsg)
            throws BusinessException {
        
    	if(summaryId != null) {
   	        CtpAffair sendAffair = affairManager.getSenderAffair(summaryId);
   	        if(sendAffair != null && !sendAffair.getSenderId().equals(userId)){
   	            throw new BusinessException(ResourceUtil.getString("collaboration.error.common.unauthorized"));//越权操作
   	        } else {
   		        Comment c = new Comment();
   		        c.setCreateId(userId);
   		        c.setCreateDate( new java.util.Date());
   		        c.setContent(content);
   		        c.setPushMessage(toSendMsg);
   		        c.setPid(0l);
   		        c.setClevel(1);//这个参数没有什么用
   		        c.setPath("00");//这个参数没有什么用
   		        c.setModuleType(ApplicationCategoryEnum.edoc.getKey());
   		        c.setCtype(Comment.CommentType.sender.getKey());
   		        c.setModuleId(summaryId);
   		        if(sendAffair != null){
   		        	c.setAffairId(sendAffair.getId());   		        	
   		        	c.setTitle(sendAffair.getSubject());
   		        }
   		        c.setRelateInfo(attrs);
   		        govdocCommentManager.insertComment(c, "");
   	        }
    	} else {
    		throw new BusinessException("summmaryId "+ ResourceUtil.getString("collaboration.error.common.empty"));//传递的summmaryId为null
    	}
    }
    
    private List<String> getActions(CtpAffair affair, EdocSummary summary){
   	 List<String> actionList = new ArrayList<String>();
   	Permission permission;
   	try {
   		permission = this.getPermisson(affair, summary);
   		List<String> basicActionList = permissionManager.getActionList(permission, PermissionAction.basic);
   	    List<String> commonActionList = permissionManager.getActionList(permission, PermissionAction.common);
   	    List<String> advanceActionList = permissionManager.getActionList(permission, PermissionAction.advanced);
   	    actionList.addAll(basicActionList);
   	    actionList.addAll(commonActionList);
   	    actionList.addAll(advanceActionList);
   	} catch (BusinessException e) {
   		LOGGER.error(e);
   		//e.printStackTrace();
   	}
   	return actionList;
   }
    
    /**
     * 判断是否是新公文（前台调用）
     */
  	@GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("isNewEdoc")
    public Response isNewEdoc(@QueryParam("affairId") String affairId)  throws BusinessException {
  		/*
           * 新公文归档时sourceId存的是affairId,老公文归档时sourceId存的是summaryID
           */
  		String subApp="";
  		CtpAffair ctpAffair=affairManager.get(Long.valueOf(affairId));
          if(ctpAffair!=null){//文档中心的新公文或者公文
          	subApp= ctpAffair.getSubApp()+"";
          }else{//文档中心的老公文
          	EdocApi edocApi = (EdocApi)AppContext.getBean("edocApi");
          	subApp= edocApi.getEdocSummaryGovdocTypeBySummaryID(Long.valueOf(affairId))+"";
          }
  	   return ok(subApp);
    }
  	
    /**
     * 回退公文
     * @param affairId	Long | 必填 | 事项ID
     * @param params	Map<String, Object> | 必填 | 其他参数
     * <pre>
     * content	String | 非必填 | 意见内容
     * hide		String | 非必填 | 是否隐藏意见, true-隐藏意见，false-不隐藏
     * isWFTrace	String | 非必填 | 是否跟踪  ， true - 跟踪全部 ， false - 不跟踪
     * traceWorkflow String | 非必填 | 是否追溯流程, true - 追溯流程, false - 不追溯
     * attitude	String | 非必填 | 态度 {@link com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum}
     *    <pre>
     *        collaboration.dealAttitude.haveRead,已阅
     *        collaboration.dealAttitude.agree,同意
     *        collaboration.dealAttitude.disagree，不同意
     *    </pre>
     * fileUrlIds	String | 非必填 | 附件ID串，使用,分隔
     * commentId	Long | 非必填 | 使用草稿的评论ID
     * affairId		Long | 必填 | 事项ID
     * </pre>
     * @return  Map<String, String>
  * @throws BusinessException 
     */
    @POST
    @Path("stepBack/{affairId}")
    public Response stepBack(@PathParam("affairId") Long affairId, Map<String, Object> params) throws BusinessException{
          
        Map<String, String> ret = new HashMap<String, String>();
        User user = AppContext.getCurrentUser();

        CtpAffair affair = null;
        EdocSummary summary = null;
        
        boolean canDeal = true;
        
        try {
          //获取对象
            affair = affairManager.get(affairId);
            summary = edocManager.getEdocSummaryById(affair.getObjectId(), true);
            
            String[] wfCheck = wapi.canStepBack(toString(affair.getSubObjectId()), 
                        toString(summary.getCaseId()), toString(summary.getProcessId()), 
                                        toString(affair.getActivityId()), "", "");
            if("false".equals(wfCheck[0])){
                canDeal = false;
                ret.put(ERROR_KEY, wfCheck[1]);
            }
        } catch (Exception e1) {
            ret.put(ERROR_KEY, "stepback collaboration failed, cause by:" + e1.getLocalizedMessage());
            canDeal = false;
            LOGGER.error(e1.getMessage(), e1);
        }
        boolean isLock = false;
        if(canDeal){
            try {
                
                if(canDeal){
                    canDeal = lockWorkFlow(summary, user, 9, ret);
                }
                
                if(canDeal){
                    //检验是否可以处理
                    canDeal = checkCanDeal(affair, ret);
                }
                if(canDeal){
                	isLock = govdocLockManager.canGetLock(affairId);
                    if(!isLock) {
                        canDeal = false;
                        ret.put(ERROR_KEY, "同时项两个人正在同时处理");
                        LOGGER.error( AppContext.currentUserLoginName()+"同时项两个人正在同时处理。affairId"+affairId);
                    }
                }
                
                
                if (canDeal) {
                    
                    //意见
                    Comment comment = formComment(summary, affair, user, ColHandleType.stepBack, params);
                    //是否修改正文
                    String modifyFlag = ParamUtil.getString(params, "modifyFlag", "");
                    String jsonComment = transComment2Str(comment,modifyFlag);
                    putThreadContext(jsonComment);
                   
                    //处理是否追溯流程
                    String isWFTrace = ParamUtil.getString(params, "isWFTrace", "0");
                    
                /*    Map<String,Object> tempMap=new HashMap<String, Object>();
                    tempMap.put("affairId", affair.getId().toString());
                    tempMap.put("summaryId", summary.getId().toString());
                    tempMap.put("targetNodeId", "");
                    tempMap.put("isWFTrace", isWFTrace);*/
                    GovdocDealVO dealVo = new GovdocDealVO();
                    dealVo.setAffairId(affair.getId());
                    dealVo.setSummaryId(summary.getId());
               //      dealVo.setSelectTargetNodeId("");
                    dealVo.setIsWFTrace(isWFTrace);
                    dealVo.setCurrentUser(user);
                    Boolean stepStopRet = govdocManager.transStepBack(dealVo);
                    if(!stepStopRet){
                        ret.put(ERROR_KEY, "回退失败"); 
                    }
                }
            } catch (Exception e) {
                ret.put(ERROR_KEY, e.getMessage());
                LOGGER.error("处理系统异常", e);
            } finally {
                if(isLock){
        			//colLockManager.unlock(affairId);
             	   govdocLockManager.unlock(affairId);
        		}
                removeThreadContext();
                if(canDeal){
                //	colManager.unlockCollAll(affairId,affair,summary);
             		govdocLockManager.colDelLock(summary, affair);
                }
            }
        }

        //结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        
        return ok(ret);
    }
    
    
    /**
     * 指定回退
     * @param params Map<String,Object> |必填| 其它参数
     * <pre>
     * affairId              Long |必填|            事项ID
 	 * activityId            String |必填|          节点ID
 	 * submitStyle           String |必填|          处理提交方式
 	 *    <pre>
 	 *    1(直接提交给我)、0(流程重走)
 	 *    <pre>
 	 * isWfTrace             String |必填|          是否追溯流程
 	 *    <pre>
 	 *    1(是)、2(否)
 	 *    <pre>
 	 * isCircleBack          String |非必填|          是否环形回退，用于流程追述的数据类型产生，默认指定回退
 	 *    <pre>
 	 *    0(否)、1(是)
 	 *    <pre>
 	 * theStepBackNodeId     String |必填|          被回退节点ID
 	 * processId             String |必填|          流程ID
 	 * caseId                String |必填|          流程实例ID（一个流程  对应一个 流程实例ID）
 	 * workitemId            String |必填|          当前记录对应的ID（一个流程有多个节点 每个节点有一个或多个人员  每个人员会对应一条数据  这条数据的ID 就是 当前记录对应的ID）
 	 * summaryId             String |必填|          协同ID
 	 * commentId             Long   |非必填|         意见对象的ID  如果有会拿他做意见对象的主键 没有的话 会自动生成一个UUID到意见对象。是存为草稿的时候的意见对应的id 
 	 * content               String |非必填|          意见
 	 * hide                  String |非必填|          意见隐藏 true-隐藏意见，false-不隐藏
 	 * attitude              String |非必填|          态度 {@link com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum}
     *    <pre>
     *        collaboration.dealAttitude.haveRead,已阅
     *        collaboration.dealAttitude.agree,同意
     *        collaboration.dealAttitude.disagree，不同意
     *    </pre>
 	 * likeSummary           String |非必填|          点赞， true-点赞了， false-没有
 	 * fileJson              String |非必填|          附件信息
 	 * </pre>
     * @return Map<String, String>
     * @throws BusinessException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("updateAppointStepBack")
    public Response updateAppointStepBack(Map<String,Object> params) throws BusinessException {
    	
    	Long affairId = getLong(params, "affairId", null);
    	
    	
    	 Map<String, String> ret = new HashMap<String, String>();
         
         User user = AppContext.getCurrentUser();
         boolean canDeal = false;
         CtpAffair affair = null;
         EdocSummary summary = null;
         boolean isLock = false;
         try {
             
             //获取对象
             affair = affairManager.get(affairId);
             
             summary = edocManager.getEdocSummaryById(affair.getObjectId(), true);
             
             //检验锁和状态是否正常
             canDeal = checkCanDeal(affair, ret);
             if(canDeal){

                 isLock = govdocLockManager.canGetLock(affairId);
                 if(!isLock) {
                     canDeal = false;
                     ret.put(ERROR_KEY, "同时项两个人正在同时处理");
                     LOGGER.error( AppContext.currentUserLoginName()+"同时项两个人正在同时处理。affairId"+affairId);
                 } 
             }
             
             
             if (canDeal) {

                 // 意见
                 Comment comment = formComment(summary, affair, user, ColHandleType.specialback, params);
                 //是否修改正文
                 String modifyFlag = ParamUtil.getString(params, "modifyFlag", "");
                 String jsonComment = transComment2Str(comment,modifyFlag);
                 putThreadContext(jsonComment);
            /*     params.put("affair", affair);
                 params.put("summary", summary);
                 params.put("comment", comment);
                 params.put("user", user);*/
                GovdocDealVO dealVo = new GovdocDealVO();
                getDealVo(params, dealVo,affair,summary,comment);                
                 if(summary.getTempleteId() != null){
                 	CtpTemplate template = templateManager.getCtpTemplate(summary.getTempleteId());
                     //取出模板信息
                /* 	params.put("templateColSubject", template.getColSubject());
                 	params.put("templateWorkflowId", template.getWorkflowId());*/
                 	dealVo.setTemplateColSubject(template.getColSubject());
                 	dealVo.setTemplateWorkflowId(String.valueOf(template.getWorkflowId()));
                 	
                 }
             	// colManager.updateAppointStepBack(params);
             	govdocManager.transAppointStepBack(dealVo);
             }
         } catch (Exception e) {
             ret.put(ERROR_KEY, e.getMessage());
             LOGGER.error("处理系统异常", e);
         } finally {
             if(isLock){
     			//colLockManager.unlock(affairId);
             	govdocLockManager.unlock(affairId);
     		 }
             removeThreadContext();
             if(canDeal){
            	// colManager.unlockCollAll(affairId,affair,summary);
             	govdocLockManager.colDelLock(summary, affair);
             }
         }
         
       //结果封装
         if(ret.containsKey(ERROR_KEY)){
             ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
         }else {
             ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
         }

         return ok(ret);
    }
    
    /**
     * 移交功能
     * @param params Map<String,Object> | 必填 | 其它参数
     * <pre>
     *    affairId String |必填| 转办事项的人的 事项ID
     *    transferMemberId  String |必填| 转办对象ID
     * </pre>
     * @return Map<String, String>
     * @throws BusinessException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("newTransfer")
    public Response newTransfer(Map<String,Object> params) throws BusinessException  {
    	Map<String, String> ret = new HashMap<String, String>();
        User user = AppContext.getCurrentUser();

        String affairId = ParamUtil.getString(params, "affairId");
        String transferMemberId = ParamUtil.getString(params, "transferMemberId");
        params.remove("affairId");
        params.remove("transferMemberId");
        
        //获取对象
        CtpAffair  affair = affairManager.get(Long.valueOf(affairId));
       // ColSummary summary = collaborationApi.getColSummary(affair.getObjectId());
        EdocSummary summary=edocManager.getEdocSummaryById(affair.getObjectId(), true);
        Comment comment = formComment(summary, affair, user, ColHandleType.finish, params);
        AppContext.putRequestContext("m3Comment", comment);
        //ret.put("m3comment", comment);
    	ret.put("affairId", affairId);
    	ret.put("transferMemberId", transferMemberId);
    	//是否修改正文
        String modifyFlag = ParamUtil.getString(params, "modifyFlag", "");
    	ret.put("modifyFlag", modifyFlag);
    	boolean isLock = false;
    	try {
        	isLock = govdocLockManager.canGetLock(affair.getId());
        	if(!isLock) {
        		String msg =  ResourceUtil.getString("collaboration.summary.notDuplicateSub");
        		LOGGER.error( AppContext.currentUserLoginName()+msg+",affairId"+affairId);
        	}
        	else{
        		//String message = this.colManager.transColTransfer(ret);
        		String message = govdocManager.transfer(ret);
        	}
		} finally {
			if(isLock){
				//colLockManager.unlock(Long.valueOf(affairId));
				govdocLockManager.unlock(Long.valueOf(affairId));
			}
		}
        
      //结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        
        return ok(ret);
    }
    
    /**
     *  处理人撤销公文
     *  {POST} /repeal 
     *  
     * @param affairId	Long | 必填 | 事项ID
     * @param params	Map<String, Object> | 必填 | 其他参数
     * <pre>
     * content	   String | 非必填 | 意见内容 
     * commentId   String | 非必填 | 回复意见ID
  	 * hide        String | 非必填 | 意见隐藏
     * traceWorkflow String | 非必填 | 是否追溯流程, true - 追溯流程, false - 不追溯
  	 * attitude    String | 非必填 | 态度 {@link com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum}
     *    <pre>
     *        collaboration.dealAttitude.haveRead,已阅
     *        collaboration.dealAttitude.agree,同意
     *        collaboration.dealAttitude.disagree，不同意
     *    </pre>
  	 * likeSummary String | 非必填 | 点赞
  	 * fileJson    String | 非必填 | 附件信息    
     * </pre>
     * @return  Map<String, String>
   * @throws BusinessException 
     */
    @POST
    @Path("repeal/{affairId}")
    public Response repeal(@PathParam("affairId") Long affairId, Map<String, Object> params) throws BusinessException{
        
        Map<String, String> ret = new HashMap<String, String>();
        User user = AppContext.getCurrentUser();
        
        //前端参数完整性校验TODO...
        
        CtpAffair affair = null;
        EdocSummary summary = null;
        
        boolean canDeal = true;
        try {
            
            //获取对象
            affair = affairManager.get(affairId);
            summary = edocManager.getEdocSummaryById(affair.getObjectId(), true);
            
            
            Map<String, String> checkMap = new HashMap<String, String>();
            checkMap.put("summaryId", summary.getId().toString());
            Map<String, String> checkRetMap = govdocManager.checkIsCanRepeal(checkMap);
            if(checkRetMap != null && Strings.isNotBlank(checkRetMap.get("msg"))){
                canDeal = false;
                ret.put(ERROR_KEY, checkRetMap.get("msg"));
            }
            
            if(canDeal){
                canDeal = isValidAffair(affairId.toString(), ret);
            }
            
            if(canDeal){
                String[] wfCheck = wapi.canRepeal(ApplicationCategoryEnum.edoc.name(), 
                                     toString(summary.getProcessId()), toString(affair.getActivityId()));
                if("false".equals(wfCheck[0])){
                    canDeal = false;
                    ret.put(ERROR_KEY, wfCheck[1]);
                }
            }
            
            if(canDeal){
                canDeal = lockWorkFlow(summary, user, 12, ret);
            }
            
            
  			
            if (canDeal) {
                
               /* Map<String,Object> tempMap=new HashMap<String, Object>();
                tempMap.put("summaryId", summary.getId().toString());
                tempMap.put("affairId", affair.getId().toString());
                tempMap.put("repealComment", params.get("content"));*/
          	  GovdocRepealVO repealVo = new GovdocRepealVO();
          	  repealVo.setSummaryIdStr(summary.getId().toString());
          	  repealVo.setAffairIdStr(affair.getId().toString());
          	  repealVo.setRepealComment(params.get("content").toString());
                //处理是否追溯流程
                String isWFTrace = ParamUtil.getString(params, "isWFTrace", "0");
                //流程会用到这个参数  是否流程追溯
              //  tempMap.put("isWFTrace", isWFTrace);
                repealVo.setIsWFTrace(isWFTrace);
                
                //意见
                Comment comment = formComment(summary, affair, user, null, params);
                //是否修改正文
                String modifyFlag = ParamUtil.getString(params, "modifyFlag", "");
                String jsonComment = transComment2Str(comment,modifyFlag);
                putThreadContext(jsonComment);
                
                String repalRet = govdocManager.transRepal(repealVo);
                if(Strings.isNotBlank(repalRet)){
                    ret.put(ERROR_KEY, repalRet);
                }
            }
        } catch (Exception e) {
            ret.put(ERROR_KEY, e.getMessage());
            LOGGER.error("处理系统异常", e);
        } finally {
            removeThreadContext();
            if( canDeal ){
            	//colManager.unlockCollAll(affairId,affair,summary);
            	govdocLockManager.colDelLock(summary, affair);
            }
        }

      //结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        return ok(ret);
    }
    
    /**
     * 终止公文
     * @param affairId	Long | 必填 | 事项ID
     * @param params	Map<String, Object> | 必填 | 其他参数
     * <pre>
     * content	String | 非必填 | 意见内容
     * hide		String | 非必填 | 是否隐藏意见, true-隐藏意见，false-不隐藏
     * tracking	String | 非必填 | 是否跟踪  ， true - 跟踪全部 ， false - 不跟踪
     * attitude	String | 非必填 | 态度 {@link com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum}
     *    <pre>
     *        collaboration.dealAttitude.haveRead,已阅
     *        collaboration.dealAttitude.agree,同意
     *        collaboration.dealAttitude.disagree，不同意
     *    </pre>
     * fileUrlIds	String | 非必填 | 附件ID串，使用,分隔
     * commentId	Long | 非必填 | 使用草稿的评论ID
     * affairId		Long | 必填 | 事项ID
     * </pre>
     * @return  Map<String, String>
     * @throws BusinessException 
     */
    @POST
    @Path("newStepStop/{affairId}")
    public Response newStepStop(@PathParam("affairId") Long affairId, Map<String, Object> params) throws BusinessException{
        
        Map<String, String> ret = new HashMap<String, String>();
        User user = AppContext.getCurrentUser();

        CtpAffair affair = null;
        EdocSummary summary = null;
        boolean canDeal = false;
        boolean isLock = false;
        try {
        	Object _objectCaseId = params.get("caseId");
        	if(null != _objectCaseId){
        		String _caseId = (String)_objectCaseId;
            	String[] canStopFlow = wapi.canStopFlow(_caseId);
            	if(null != canStopFlow && canStopFlow.length>1 && "false".equals(canStopFlow[0])){
            		ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
            		ret.put(ERROR_KEY, canStopFlow[1]);
            		return ok(ret);
            	}
        	}
            //获取对象
            affair = affairManager.get(affairId);
            summary = edocManager.getEdocSummaryById(affair.getObjectId(), true);
            
            //流程验证和加锁
            canDeal = lockWorkFlow(summary, user, 11, ret);
            
            if(canDeal){
                //检验是否可以处理
                canDeal = checkCanDeal(affair, ret);
            }
            if(canDeal){
            	isLock = govdocLockManager.canGetLock(affairId);
                if(!isLock) {
                    canDeal = false;
                    ret.put(ERROR_KEY, "同时项两个人正在同时处理");
                    LOGGER.error( AppContext.currentUserLoginName()+"同时项两个人正在同时处理。affairId"+affairId);
                }
            }
                        if (canDeal) {
                
                //意见
                Comment comment = formComment(summary, affair, user, ColHandleType.stepStop, params);
                //是否修改正文
                String modifyFlag = ParamUtil.getString(params, "modifyFlag", "");
                String jsonComment = transComment2Str(comment,modifyFlag);
                putThreadContext(jsonComment);
            /*    
                Map<String,Object> tempMap=new HashMap<String, Object>();
                tempMap.put("affairId", affair.getId().toString());*/
                GovdocDealVO govdocDealVO = new GovdocDealVO();
                govdocDealVO.setAffairId(affair.getId());
                govdocDealVO.setComment(comment);
                govdocDealVO.setExtAtt1(comment.getExtAtt1());
                govdocDealVO.setComment(comment);
                govdocDealVO.setSummary(summary);
                govdocDealVO.setAffair(affair);
                govdocDealVO.setCurrentUser(user);
                if(summary.getTempleteId() != null){
                	CtpTemplate template = templateManager.getCtpTemplate(summary.getTempleteId());
                    //取出模板信息
                /*	tempMap.put("templateColSubject", template.getColSubject());
                	tempMap.put("templateWorkflowId", template.getWorkflowId());*/
                	govdocDealVO.setTemplateColSubject(template.getColSubject());
                	govdocDealVO.setTemplateWorkflowId(template.getWorkflowId().toString());
                }     
               Boolean stepStopRet = govdocManager.transStepStop(govdocDealVO);
               if(!stepStopRet){
                   ret.put(ERROR_KEY, "终止失败");
               }
               if(ret.isEmpty()){
            	   ret.put("succ_msg", "已经成功终止!");
               }
            }
        } catch (Exception e) {
            ret.put(ERROR_KEY, Strings.isNotBlank(e.getMessage())? e.getMessage()+e.getLocalizedMessage() : e.getLocalizedMessage());
            LOGGER.error("处理系统异常", e);
        } finally {
            if(isLock){
    			//colLockManager.unlock(affairId);
            	govdocLockManager.unlock(affairId);
    		}
            removeThreadContext();
            if(canDeal){
            	//colManager.unlockCollAll(affairId,affair,summary);
            	govdocLockManager.colDelLock(summary, affair);
            }
        }
        
      //结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }

        return ok(ret);
    }
    
    /**
     * 将意见转化成字符串
     * @Author      : xuqw
     * @Date        : 2015年12月14日下午1:04:15
     * @param comment
     * @return
     */
    private String transComment2Str(Comment comment,String modifyFlag){
        
        Map<String, Object> ret = new HashMap<String, Object>();
        
      //设置意见参数
        Map<String, String> comment_deal = new HashMap<String, String>();
        comment_deal.put("id", toString(comment.getId()));
        comment_deal.put("draftCommentId", toString(comment.getId()));
        comment_deal.put("pid", toString(comment.getPid()));
        comment_deal.put("clevel", toString(comment.getClevel()));
        comment_deal.put("path", toString(comment.getPath()));
        comment_deal.put("moduleType", toString(comment.getModuleType()));
        comment_deal.put("moduleId", toString(comment.getModuleId()));
        comment_deal.put("extAtt1", toString(comment.getExtAtt1()));
        comment_deal.put("ctype", toString(comment.getCtype()));
        comment_deal.put("content", toString(comment.getContent()));
        comment_deal.put("hidden", toString(comment.isHidden()));
        comment_deal.put("showToId", toString(comment.getShowToId()));
        comment_deal.put("affairId", toString(comment.getAffairId()));
        comment_deal.put("relateInfo", toString(comment.getRelateInfo()));
        comment_deal.put("pushMessage", toString(comment.isPushMessage()));
        comment_deal.put("pushMessageToMembers", toString(comment.getPushMessageToMembers()));
        comment_deal.put("praiseInput", "0");
        
        ret.put("comment_deal", comment_deal);
        if("1".equals(modifyFlag)){
        	Map<String,String> modifyMap = new HashMap<String,String>();
        	modifyMap.put("modifyFlag", modifyFlag);
        	ret.put("colSummaryData", modifyMap);
        }
        return JSONUtil.toJSONString(ret);
    }
    
    /**
     * 封装dealVo
     * @param params
     * @param dealVo
     * @param affair
     * @param summary
     * @param comment
     */
   private void getDealVo(Map<String,Object> params,GovdocDealVO dealVo,CtpAffair affair,EdocSummary summary,Comment comment){
 	  dealVo.setCurrentUser(AppContext.getCurrentUser());
 	  dealVo.setSelectTargetNodeId(params.get("theStepBackNodeId").toString());
 	  dealVo.setWorkitemId(Long.parseLong(String.valueOf(params.get("workitemId"))));
 	  dealVo.setCaseId(Long.parseLong(String.valueOf(params.get("caseId"))));
 	  dealVo.setSubmitStyle(String.valueOf(params.get("submitStyle")));
 	  dealVo.setActivityId(Long.parseLong(String.valueOf(params.get("activityId"))));
 	  dealVo.setProcessId(String.valueOf(params.get("processId")));
 	  dealVo.setIsCircleBack(String.valueOf(params.get("isCircleBack")));;
 	  dealVo.setAffairId(affair.getId());
 	  dealVo.setSummaryId(summary.getId());
 	  dealVo.setAffair(affair);
 	  dealVo.setSummary(summary);
 	  dealVo.setComment(comment);  
 	  dealVo.setIsWFTrace(String.valueOf(params.get("isWFTrace")));

   }

	/**
	 * 根据指定的协同状态统计协同数量
	 * @param state  协同状态
	 * @return  协同数量
	 * @throws BusinessException
	 */
	private int getAffairCountByState(int state) throws BusinessException{
		Map<String,Object> queryParams = new HashMap<String,Object>(); // 统计协同条件参数
		queryParams.put("memberId", AppContext.currentUserId());
		queryParams.put("delete", Boolean.valueOf(false));
		queryParams.put("app", ApplicationCategoryEnum.collaboration.ordinal() );// 协同应用
		queryParams.put("state", state);// 状态

		return affairManager.getCountByConditions(queryParams);// 统计协同数量
	}
	/**
	 * 表单公文交换支持预留接口
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("getForwarTo")
	public Response getForwarTo(Map<String,Object> params) throws BusinessException {//空的交换接口
	  Map<String, Object> map = new HashMap<String, Object>();
	  return success(map);
	}
	
	  //增加新公文M3端收藏功能
	   /**
	    * 收藏
	    * @param affairId | 必填 |  affairId事项Id
	    * @param from | 非填 |  暂时没用到
	    * @return 
	    * @throws BusinessException
	    */
	   @GET
	   @Produces({ MediaType.APPLICATION_JSON })
	   @Path("favoriteAffair")
	   public Response favoriteAffair(@QueryParam("affairId") Long affairId, @QueryParam("from") String from) throws BusinessException {
		   User user = AppContext.getCurrentUser(); 
	       Map<String, Object> map = new HashMap<String, Object>();
	       CtpAffair affair = affairManager.get(affairId);
	       map.put("success", "true");
	       if (affair != null) {
	           Long summaryId = affair.getObjectId();
	           EdocSummary summary = govdocSummaryManager.getSummaryById(summaryId);
	           Boolean hasAtt = summary.isHasAttachments();
	           Integer favoriteType = 3;
	           try {
	               affair.setHasFavorite(Boolean.TRUE);
	               affairManager.updateAffair(affair);
	               List<String> nodePermissions = new ArrayList<String>();
	               
	               Permission permission = this.getPermisson(affair, summary);

	               List<String> basicActionList = permissionManager.getActionList(permission, PermissionAction.basic);
	               List<String> commonActionList = permissionManager.getActionList(permission, PermissionAction.common);
	               List<String> advanceActionList = permissionManager.getActionList(permission, PermissionAction.advanced);

	               nodePermissions.addAll(basicActionList);
	               nodePermissions.addAll(commonActionList);
	               nodePermissions.addAll(advanceActionList);
	               
	               if (nodePermissions.contains("Archive")) {
	                   docApi.favorite(user.getId(),user.getLoginAccount(), affairId, favoriteType, ApplicationCategoryEnum.edoc.key(), hasAtt);
	               }
	           } catch (BusinessException e) {
	           	map.put("success","false");
	           	map.put("message", e.getMessage());
	           	LOGGER.error("", e);
	           }
	       }else{
	       	map.put("success", "false");
	       }
	       return ok(map);
	   }
	   
	   /**
	    * 取消收藏
	    * @param affairId | 必填 |  affairId事项Id
	    * @param from | 非必填 |  暂时没用到
	    * @return 
	    * @throws BusinessException
	    */
	   @GET
	   @Produces({ MediaType.APPLICATION_JSON })
	   @Path("cancelFavoriteAffair")
	   public Response cancelFavoriteAffair(@QueryParam("affairId") Long affairId, @QueryParam("from") String from) throws BusinessException {
	   	Map<String, Object> map = new HashMap<String, Object>();
	   	CtpAffair affair = affairManager.get(affairId);
	       if (affair != null) {
	           affair.setHasFavorite(Boolean.FALSE);
	           affairManager.updateAffair(affair);
	       }
	       if (affairId != null) {
	           Boolean cancelFavorite = docApi.cancelFavorite(null, affairId);
	           map.put("success","true");
	           map.put("message",cancelFavorite);
	       }else{
	    	   map.put("success","false");
	       }
	       return ok(map);
	   }
	  //end 
	/**
	 * 发文登记簿 类型 名称 必填 备注 Integer pageNo Y 分页信息，第几页 Integer pageSize Y
	 * 分页信息，每页多少条数据 以下是查询条件，查询条件为空时查询全部 String startMemberName N 发起人 String
	 * subject N 标题 String createDate N 发起时间
	 * 
	 * @return FlipInfo 待办公文信息
	 * @throws BusinessException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("listSendRegister")
	public Response listSendRegister(Map<String, String> params) {
		FlipInfo flipInfo = super.getFlipInfo();
		flipInfo.setNeedTotal(true);

		// Pagination.setNeedCount(true);
		int nPageNo = Integer.parseInt(params.get("pageNo"));
		nPageNo = nPageNo < 1 ? 1 : nPageNo;
		Integer pageSize = Integer.parseInt(params.get("pageSize"));
		// Pagination.setMaxResults(pageSize);
		// Pagination.setFirstResult((nPageNo-1)*pageSize);
		flipInfo.setSize(pageSize);
		flipInfo.setPage(nPageNo);
		User user = AppContext.getCurrentUser();

		List<EdocSummaryModel> summarys = new ArrayList<EdocSummaryModel>();
		if (flipInfo != null && Strings.isNotBlank(user.getName())) {
			Map<String, String> condition = new HashMap<String, String>();
			condition.put("listType", GovdocListTypeEnum.listSendRegister + "");
			condition.put("govdocType", null);
			condition.put("memberId", user.getId() + "");
			condition.putAll(params);
			// 手输条件添加
			if (condition.get("conditionKey") != null) {
				this.handCondition(params, condition);
			}
			try {
				flipInfo = govdocListManager.findSendRegisterList(flipInfo,
						condition);
				List<GovdocListVO> voList = flipInfo.getData();
				for (GovdocListVO govdocListVO : voList) {
					summarys.add(tranToEdocSummaryModel(govdocListVO));
					// 获取公文的时候，授权以便穿透详情校验
					if (null != govdocListVO.getSummaryId()) {
						AccessControlBean.getInstance().addAccessControl(
								ApplicationCategoryEnum.edoc,
								govdocListVO.getSummaryId().toString(),
								AppContext.currentUserId());
					}
				}
			} catch (BusinessException e) {
				LOGGER.error("获得发文登记簿数据报错!", e);
			}
			// 处理名称
			// dealReturnInfo(summarys);
			// flipInfo.setTotal(Pagination.getRowCount());
			flipInfo.setData(EdocSummaryListVO.valueOf(summarys));
		}
		return ok(flipInfo);
	}

	/**
	 * 收文登记簿 类型 名称 必填 备注 Integer pageNo Y 分页信息，第几页 Integer pageSize Y
	 * 分页信息，每页多少条数据 以下是查询条件，查询条件为空时查询全部 String startMemberName N 发起人 String
	 * subject N 标题 String createDate N 发起时间
	 * 
	 * @return FlipInfo 待办公文信息
	 * @throws BusinessException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("listRecRegister")
	public Response listRecRegister(Map<String, String> params) {
		FlipInfo flipInfo = super.getFlipInfo();
		flipInfo.setNeedTotal(true);

		// Pagination.setNeedCount(true);
		int nPageNo = Integer.parseInt(params.get("pageNo"));
		nPageNo = nPageNo < 1 ? 1 : nPageNo;
		Integer pageSize = Integer.parseInt(params.get("pageSize"));
		// Pagination.setMaxResults(pageSize);
		// Pagination.setFirstResult((nPageNo-1)*pageSize);
		flipInfo.setSize(pageSize);
		flipInfo.setPage(nPageNo);
		User user = AppContext.getCurrentUser();

		List<EdocSummaryModel> summarys = new ArrayList<EdocSummaryModel>();
		if (flipInfo != null && Strings.isNotBlank(user.getName())) {
			Map<String, String> condition = new HashMap<String, String>();
			condition.put("listType", GovdocListTypeEnum.listRecRegister + "");
			condition.put("govdocType", null);
			condition.put("memberId", user.getId() + "");
			condition.putAll(params);
			// 手输条件添加
			if (condition.get("conditionKey") != null) {
				this.handCondition(params, condition);
			}
			try {
				flipInfo = govdocListManager.findRecRegisterList(flipInfo,
						condition);
				List<GovdocListVO> voList = flipInfo.getData();
				for (GovdocListVO govdocListVO : voList) {
					summarys.add(tranToEdocSummaryModel(govdocListVO));
					// 获取公文的时候，授权以便穿透详情校验
					if (null != govdocListVO.getSummaryId()) {
						AccessControlBean.getInstance().addAccessControl(
								ApplicationCategoryEnum.edoc,
								govdocListVO.getSummaryId().toString(),
								AppContext.currentUserId());
					}
				}
			} catch (BusinessException e) {
				LOGGER.error("获得收文登记簿数据报错!", e);
			}
			// 处理名称
			// dealReturnInfo(summarys);
			// flipInfo.setTotal(Pagination.getRowCount());
			flipInfo.setData(EdocSummaryListVO.valueOf(summarys));
		}
		return ok(flipInfo);
	}

	/**
	 * 手输条件添加
	 * 
	 * @param condition
	 * @param params
	 * @return
	 */
	private void handCondition(Map<String, ? extends Object> condition,
			Map<String, String> params) {

		if ("subject".equals(condition.get("conditionKey"))) { // 标题
			params.put("subject", condition.get("textfield").toString());
		}
		if ("startMemberName".equals(condition.get("conditionKey"))) { // 发起人
			params.put("startUserName", condition.get("textfield").toString());
		}
		if ("createDate".equals(condition.get("conditionKey"))) { // 发起时间
			String startDate = "";
			if (condition.get("textfield") != null
					&& condition.get("textfield").toString().length() > 0) {
				startDate = condition.get("textfield").toString();
			}
			startDate = startDate + "#";
			if (condition.get("textfield1") != null
					&& condition.get("textfield1").toString().length() > 0) {
				startDate = startDate + condition.get("textfield1").toString();
			}
			params.put("startTime", startDate);
		}
		if ("docMark".equals(condition.get("conditionKey"))) { // 公文文号
			params.put("docMark", condition.get("textfield").toString());
		}
		params.put("condition", "choose");

	}

	/**
	 * 新公文查询出来数据对象转换成原A8公文的数据对象
	 * 
	 * @param govdocListVO
	 * @return
	 * @throws BusinessException
	 */
	private EdocSummaryModel tranToEdocSummaryModel(GovdocListVO govdocListVO)
			throws BusinessException {
		EdocSummaryModel edocSummaryModel = new EdocSummaryModel();
		// 通过SummaryId来获取subApp来区分新老公文
		edocSummaryModel.setEdocType(edocManager
				.getGovdocTypeBySummaryId(govdocListVO.getSummaryId()) + "");
		edocSummaryModel.setHasAttachments(govdocListVO.getHasAtt());
		if (null != govdocListVO.getAffairSubState()) {
			edocSummaryModel.setState(govdocListVO.getAffairSubState());
		}
		edocSummaryModel.setTrack(govdocListVO.getAffairTrack());
		if (null != affairManager.get(govdocListVO.getAffairId())) {
			edocSummaryModel.setAffair(affairManager.get(govdocListVO
					.getAffairId()));
		} else {
			edocSummaryModel.setAffair(new CtpAffair());
		}
		edocSummaryModel.setSummary(govdocSummaryManager
				.getSummaryById(govdocListVO.getSummaryId()));
		edocSummaryModel.setHastenTimes(govdocListVO.getHastenTimes());
		edocSummaryModel
				.setStartDate(govdocListVO.getStartTime() != null ? new Date(
						govdocListVO.getStartTime().getTime()) : null);
		edocSummaryModel.setAdvanceRemindTime(govdocListVO
				.getAffairRemindDate());
		edocSummaryModel.setDeadLine(govdocListVO.getSummaryDeadline());
		edocSummaryModel.setDeadlineDisplay(govdocListVO
				.getSummaryDeadLineName());
		edocSummaryModel
				.setDealTime(govdocListVO.getAffairCompleteTime() != null ? new java.sql.Date(
						govdocListVO.getAffairCompleteTime().getTime()) : null);
		edocSummaryModel.setNodePolicy(govdocListVO.getAffairNodePolicy());
		edocSummaryModel.setLogicalPath(govdocListVO.getArchiveName());
		edocSummaryModel.setArchiveName(govdocListVO.getArchiveName());
		edocSummaryModel.setCaseId(govdocListVO.getCaseId() + "");
		edocSummaryModel.setAffairId(govdocListVO.getAffairId());
		edocSummaryModel.setSendToUnit(govdocListVO.getSendTo());
		edocSummaryModel.setSurplusTime(govdocListVO.getSurplusTime());
		edocSummaryModel.setDepartmentName(govdocListVO.getSendDepartment());
		edocSummaryModel.setDepartmentName(govdocListVO.getSendDepartment());
		if (null != edocSummaryModel.getAffair().getSubject()) {
			edocSummaryModel.getSummary().setSubject(
					edocSummaryModel.getAffair().getSubject());
		} else {
			edocSummaryModel.getSummary().setSubject(govdocListVO.getSubject());
		}
		edocSummaryModel.setSubject(govdocListVO.getSubject());
		edocSummaryModel.setEdocUnit(govdocListVO.getSendUnit());
		edocSummaryModel.setRecUserName(govdocListVO.getSignPerson());
		edocSummaryModel.setDealLineDateTime(govdocListVO
				.getAffairDeadLineName());
		edocSummaryModel.setRecieveUserName(govdocListVO.getSignPerson());
		edocSummaryModel.setCreatePerson(govdocListVO.getStartUserName());
		edocSummaryModel
				.setCreateDate(govdocListVO.getCreateTime() != null ? new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss").format(govdocListVO
						.getCreateTime().getTime()) : null);
//		edocSummaryModel
//				.setFinshed(govdocListVO.getAffairFinish() != null ? govdocListVO
//						.getAffairFinish() : false);
		if(null!=govdocListVO.getSummaryTransferStatusName() && "已结束".equals(govdocListVO.getSummaryTransferStatusName())){
			edocSummaryModel.setFinshed(true);
		}
		if(null!=edocSummaryModel.getSummary() && govdocListVO.getProxy().booleanValue()&&Strings.isNotBlank(govdocListVO.getProxyName())){
			String proxySubject =edocSummaryModel.getSummary().getSubject()+ "(代理"+govdocListVO.getProxyName()+")";
			edocSummaryModel.getSummary().setSubject(proxySubject);
		}
		return edocSummaryModel;
	}
	
	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("handleDetail")
	public Response handleDetail(Map<String, String> params) throws BusinessException {
		FlipInfo flipInfo = getFlipInfo();
		flipInfo.setPage(ParamUtil.getInt(params, "pageNo",1));
		flipInfo.setSize(ParamUtil.getInt(params, "pageSize",20));
		flipInfo = detaillogManager.getFlowNodeDetail(flipInfo, params);
		return success(flipInfo);
	}
	
	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("attachmentList")
	public Response getAttachmentListBySummaryId(Map<String,String> params) throws BusinessException {
    	String summaryId = params.get("summaryId");
    	String formAttrUrls = params.get("formAttrUrls");
    	String isHistoryFlag = params.get("isHistoryFlagStr");
    	
    	List<GovdocAttachmentVO> attachments = govdocContentManager.getAttachmentListBySummaryIdForMobile(Long.valueOf(summaryId), formAttrUrls, isHistoryFlag);
        ////获取附件的时候，授权校验，OA-105860
    	if(null!=attachments && attachments.size()>0){
    		AccessControlBean.getInstance().addAccessControl(ApplicationCategoryEnum.edoc,summaryId,AppContext.currentUserId());
    	}
    	return success(attachments);
    }
	
	/**
	 * 更新公文全文签批文件id
	 * @param summaryId
	 * @param fileId
	 * @throws BusinessException
	 */
	private void updateQwqp(long summaryId, long fileId) throws BusinessException {
		//判断文件是否存在
		V3XFile v3xFile = fileManager.getV3XFile(fileId);
		if (v3xFile != null) {
			qwqpManager.updateEdocFormFileRelations(summaryId, fileId);
		}
	}
	
	 /**
	   * 撤销时校验
	   * @param params Map<String,Object> | 必填 | 其它参数
	   * <pre>
	   *    summaryId String |必填| 协同ID
	   * </pre>
	   * @return Map<String, String>
	   * @throws BusinessException
	   */
	  @POST
	  @Produces({ MediaType.APPLICATION_JSON })
	  @Path("transRepalValid")
	  public Response transRepalValid(Map<String,Object> params) throws BusinessException  {
	  	
	  	Map<String, String> checkMap = new HashMap<String, String>();
	  	String summaryId = ParamUtil.getString(params, "summaryId");
	  	
	      checkMap.put("summaryId", summaryId);
	      Map<String, String> checkRetMap = govdocManager.checkIsCanRepeal(checkMap);
	      if(checkRetMap.isEmpty()){
	      	checkRetMap.put("succ_msg", "可以撤销");
	      }
	      String affairId = ParamUtil.getString(params, "affairId");
	  	
	  	if(Strings.isNotBlank(summaryId) && Strings.isNotBlank(affairId)){
	  		//ColSummary summary = colManager.getSummaryById(Long.parseLong(summaryId));
	  		EdocSummary summary = edocManager.getEdocSummaryById(Long.parseLong(summaryId), true);
	  		CtpAffair affair = affairManager.get(Long.parseLong(affairId));
	  		if(null != summary && null != affair){
	  			String[] wfCheck = wapi.canRepeal(ApplicationCategoryEnum.edoc.name(), 
	  					toString(summary.getProcessId()), toString(affair.getActivityId()));
	  			if("false".equals(wfCheck[0])){
	  				
	  				checkRetMap.put(ERROR_KEY, wfCheck[1]);
	  			}
	  		}
	  	}
	  	
	  	//结果封装
	      if(checkRetMap.containsKey(ERROR_KEY)){
	          checkRetMap.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
	      }else {
	          checkRetMap.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
	      }
	      
	      return ok(checkRetMap);
	  }
	  
	  
	  /**
	   *  发起人撤销公文
	   *  {POST} /transRepal 
	   * @param affairId	Long | 必填 | 事项ID
	   * @param params	Map<String, Object> | 必填 | 其他参数
	   * <pre>
	   * content	String | 非必填 | 意见内容 
	   * </pre>
	   * @return  Map<String, String>
	 * @throws BusinessException 
	   */
	  @POST
	  @Path("transRepeal/{affairId}")
	  public Response transRepal(@PathParam("affairId") Long affairId, Map<String, Object> params) throws BusinessException{
	      
	      Map<String, String> ret = new HashMap<String, String>();
	      User user = AppContext.getCurrentUser();
	      
	      //前端参数完整性校验TODO...
	      
	      CtpAffair affair = null;
	      EdocSummary summary = null;
	      boolean canDeal = false;
	      try {
	          
	          //获取对象
	          affair = affairManager.get(affairId);
	          summary = edocManager.getEdocSummaryById(affair.getObjectId(), true);
	          
//	          boolean canDeal = true;
//	          Map<String, String> checkMap = new HashMap<String, String>();
//	          checkMap.put("summaryId", summary.getId().toString());
//	          Map<String, String> checkRetMap = colManager.checkIsCanRepeal(checkMap);
//	          if(checkRetMap != null && Strings.isNotBlank(checkRetMap.get("msg"))){
//	              canDeal = false;
//	              ret.put(ERROR_KEY, checkRetMap.get("msg"));
//	          }
	          
//	          if(canDeal){
	          canDeal = isValidAffair(affairId.toString(), ret);
//	          }
	          
	          if(canDeal){
	              String[] wfCheck = wapi.canRepeal(ApplicationCategoryEnum.collaboration.name(), 
	                                   toString(summary.getProcessId()), toString(affair.getActivityId()));
	              if("false".equals(wfCheck[0])){
	                  canDeal = false;
	                  ret.put(ERROR_KEY, wfCheck[1]);
	              }
	          }
	          
	          if(canDeal){
	              canDeal = lockWorkFlow(summary, user, 12, ret);
	          }
	          
	          //TODO 表单collaborationFormBindEventListener.achieveTaskType
	          
	          
	          if (canDeal) {
	              
	             /* Map<String,Object> tempMap=new HashMap<String, Object>();
	              tempMap.put("affairId", affair.getId().toString());
	              tempMap.put("summaryId", summary.getId().toString());
	              tempMap.put("repealComment", params.get("content"));*/
	        	  GovdocRepealVO repealVo = new GovdocRepealVO();
	        	  repealVo.setAffairIdStr(affair.getId().toString());
	        	  repealVo.setSummaryIdStr(summary.getId().toString());
	        	  repealVo.setRepealComment(params.get("content").toString());
	              //流程会用到这个参数
	              //tempMap.put("isWFTrace", ParamUtil.getString(params, "isWFTrace", "0"));
	        	  repealVo.setIsWFTrace(ParamUtil.getString(params, "isWFTrace", "0"));
	              String repalRet = govdocManager.transRepal(repealVo);
	              
	              //formatAtWho(comment, affair, params);
	              
	              if(Strings.isNotBlank(repalRet)){
	                  ret.put(ERROR_KEY, repalRet);
	              }
	          }
	      } catch (Exception e) {
	          ret.put(ERROR_KEY, e.getMessage());
	          LOGGER.error("处理系统异常", e);
	      } finally {
	      	if(canDeal){
	      		//colManager.unlockCollAll(affairId,affair,summary);
	      		govdocLockManager.colDelLock(summary, affair);
	      	}
	      }

	    //结果封装
	      if(ret.containsKey(ERROR_KEY)){
	          ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
	      }else {
	          ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
	      }
	      
	      return ok(ret);
	  }
	  
	  /**
		 *  取回
	  *  @param affairId Long | 必填 |  事项ID
	  *  @param isSaveOpinion boolean | 必填 | 是否对愿意见修改
		 * @return  Map<String,String>
		 */
		@GET
		@Path("newTakeBack")
		public Response newTakeBack(@QueryParam("affairId") Long affairId,@QueryParam("isSaveOpinion") boolean isSaveOpinion) {
			
		    Map<String,String> messages = new HashMap<String,String>();
		    User user = AppContext.getCurrentUser();
		    
		    CtpAffair affair = null;
		    EdocSummary summary = null;
		    boolean isLock = false;
			try {
				isLock = govdocLockManager.canGetLock(affairId);
	         if(!isLock) {
	         	String msg =  ResourceUtil.getString("collaboration.summary.notDuplicateSub");
	             LOGGER.error( AppContext.currentUserLoginName()+msg+"-takeBack,affairId"+affairId);
	         }
	         if(isLock){
	         	
	         	//获取对象
	         	affair = affairManager.get(affairId);
	         	summary = edocManager.getEdocSummaryById(affair.getObjectId(), true);
	         	
	         	//检验锁和状态是否正常
	         	boolean canDeal = isValidAffair(affair.getId().toString(), messages);
	         	
	         	if(canDeal){
	         		String wfCheck = wapi.canTakeBack(ApplicationCategoryEnum.collaboration.name(), toString(summary.getProcessId()),
	         				toString(affair.getActivityId()), toString(affair.getSubObjectId()));
	         		Map<String, String> wfCheckMap = JSONUtil.parseJSONString(wfCheck, Map.class);
	         		if("false".equals(wfCheckMap.get("canTakeBack"))){
	         			canDeal = false;
	         			messages.put(ERROR_KEY, ResourceUtil.getString("collaboration.takeBackErr."+wfCheckMap.get("state")+".msg"));
	         		}
	         	}
	         	
	         	if(canDeal){
	         		canDeal = lockWorkFlow(summary, user, 13, messages);
	         	}
	         	
	         	if(canDeal){
	         		if(canDeal){
	             		String message = takeBack(affairId, isSaveOpinion);
	             		messages.put("message", message);
	             	}
	         	}
	         }
			} catch (BusinessException e) {
				LOGGER.error("取回失败！", e);
				messages.put(ERROR_KEY, e.getMessage());
			} finally{
				if(isLock){
					//colLockManager.unlock(affairId);
					govdocLockManager.unlock(affairId);
				}
	         try {
	            // colManager.colDelLock(summary, affair);
	        	 govdocLockManager.colDelLock(summary, affair);
	         } catch (BusinessException e) {
	             LOGGER.error("表单锁解锁失败", e);
	         }
			}
			
			//结果封装
	     if(messages.containsKey(ERROR_KEY)){
	         messages.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
	     }else {
	         messages.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
	     }
	     
			return ok(JSONUtil.toJSONString(messages));
		}
		
		/**
	     *  取回已办事项
	     * @param affairId
	     * @param isSaveOpinion 是否对愿意见修改
	     * @throws BusinessException
	     */
	    public String takeBack(long affairId,boolean isSaveOpinion) throws BusinessException {
	    	Map<String,Object> params = new HashMap<String,Object>();
	    	params.put("affairId", String.valueOf(affairId));
	    	params.put("isSaveOpinion", isSaveOpinion);
	    	String message = String.valueOf(govdocManager.transTakeBack(params).get("msg"));
	    	return message;
	    }

	    
		public FlipInfo findNewEdocList(FlipInfo flipInfo,String listType,int edocType,
				Map<String, Object> condition) throws BusinessException {
				String govdocType=getGovDocType(edocType, listType);
				HashMap<String, String> param = new HashMap<String,String>();
				
				
				//手输条件添加
				 if(condition.get("conditionKey")!=null){     
					this.handCondition(condition, param);
				 }else if (condition.get("conditionKey") == null && (condition.get("subject")!=null || condition.get("startMemberName")!=null || condition.get("createDate")!=null) ) {
					 this.handConditionFromBiz(condition, param);
				}
			     
			   
				//M3端不允许传递 ""或null,M3端增加传递 标记，用于标记M3端获取收文、发文、签报合并后的已发、已办、待发 
				if("999".equals(govdocType) || "998".equals(govdocType) ){
					govdocType = "";
				}
			      //从业务生成器生成的公文参数
				  if(condition.get("templeteIds")!= null){
					  param.put("templateIds", String.valueOf(condition.get("templeteIds"))) ;
					  govdocType=null;
					  if(param.get("condition") == null){
						  param.put("condition", "byUrl");  
					  }
					  
				  }
					//组装参数
				  param.put("govdocType", govdocType);
				  param.put("listType",listType);

				
			    if("listSent".equals(listType) || "listSentAllBiz".equals(listType)){    //已发
				   flipInfo=govdocListManager.findSentList(flipInfo, param);
					}
				if("listPendingAll".equals(listType) || "listPendingAllBiz".equals(listType)){  //待办
				    if("listPendingAllBiz".equals(listType)){
				    	param.put("needTotal", "true");
				    	}		   
				    	param.put("configId", null);
			   
				   flipInfo=govdocListManager.findPendingList(flipInfo, param);
				}    
			    if("listDoneAll".equals(listType)||"listZcdb".equals(listType)||"listDoneAllBiz".equals(listType)){//已办,在办,业务生成器生成的公文已办
			    	if("listZcdb".equals(listType)){
			    		param.put("listType","listDone");  
					}
			    	  param.put("configId", null);
					  flipInfo=govdocListManager.findDoneList(flipInfo, param);
				  } 
				if("listWaitSend".equals(listType) || "listWaitSendAllBiz".equals(listType)){ //待发
					   flipInfo=govdocListManager.findWaitSendList(flipInfo, param);
				}
				
				return flipInfo;
			}
		
		private String getGovDocType(int edocType, String listType) {
			String govdocType="";
			switch (edocType) {
			case 0: //发文
				govdocType=ApplicationSubCategoryEnum.edoc_fawen.getKey()+"";
				break;
	        case 1: //收文
	        	govdocType= "listWaitSend".equals(listType) ? ApplicationSubCategoryEnum.edoc_shouwen.getKey() + "" : ApplicationSubCategoryEnum.edoc_shouwen.getKey() + "," + ApplicationSubCategoryEnum.edoc_jiaohuan.getKey();
				break;
	        case 2: //签报 
	        	govdocType=ApplicationSubCategoryEnum.edoc_qianbao.getKey()+"";
				break;
			default: //add by liusy at 2018-03-06  M3端不允许传递 ""或null,M3端增加传递 标记，用于标记M3端获取收文、发文、签报合并后的已发、已办、待发 
				govdocType= String.valueOf(edocType);
				break;
			}
			return govdocType;
			
		}
		
		  /**
		   * 业务生成器生成列表手动查询
		   */
		  private void handConditionFromBiz(Map<String,Object> condition,Map<String, String> params){

				if(condition.get("subject")!=null){  //标题
					params.put("subject",String.valueOf(condition.get("subject")));
			     }
			     if(condition.get("startMemberName")!=null){ //发起人
			    	 params.put("startUserName",String.valueOf(condition.get("startMemberName")));
			     }
			     if(condition.get("createDate")!=null){ //发起时间
//			    	 String startDate="";
//			    	 if(condition.get("textfield")!=null&&condition.get("textfield").toString().length()>0){
//			    		 startDate=condition.get("textfield").toString();
//			    	 }
//			    	 startDate=startDate+"#";
//			    	 if(condition.get("textfield1")!=null&&condition.get("textfield1").toString().length()>0){
//			    	 startDate = startDate+condition.get("textfield1").toString();
//			    	 }
			    	 params.put("startTime",String.valueOf(condition.get("createDate")));
			     }
			     params.put("condition", "choose");
			  
		  }


	/**
	 * PDF圈阅是否授权
	 * 
	 * @return
	 * @throws BusinessException
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("getPdfAuthKey")
	public Response getPdfAuthKey() throws BusinessException {
		// 当前人员移动PDF圈阅是否授权
		Map<String, String> map = new HashMap<String, String>();
		Long userId = AppContext.getCurrentUser().getId();
		ConfigManager configManager = (ConfigManager) AppContext.getBean("configManager");
		List<Long> ids = new LinkedList<Long>();
		// 添加当前登录人id
		ids.add(userId);
		V3xOrgMember member = orgManager.getMemberById(userId);
		// 添加单位id
		Long accountId = member.getOrgAccountId();
		ids.add(accountId);
		// 部门id 及所有父部门id
		Long departmentId = member.getOrgDepartmentId();
		ids.add(departmentId);
		List<V3xOrgDepartment> parentDepartments = orgManager.getAllParentDepartments(departmentId);
		for (V3xOrgDepartment de : parentDepartments) {
			ids.add(de.getId());
		}
		// 组id
		List<V3xOrgTeam> teams = orgManager.getTeamsByMember(userId, accountId);
		for (V3xOrgTeam t : teams) {
			ids.add(t.getId());
		}
		// 岗位id
		List<MemberPost> posts = orgManager.getMemberPosts(accountId, userId);
		for (MemberPost p : posts) {
			ids.add(p.getPostId());
		}
		List<ConfigItem> items = configManager.listAllConfigByCategory("pdfContentSet", 1L);
		String authValue = "";
		String authOrgs = "";
		for (ConfigItem item : items) {
			if ("serialNo".equals(item.getConfigItem())) {
				authValue = item.getConfigValue();
			} else if ("authUsers".equals(item.getConfigItem())) {
				authOrgs = item.getExtConfigValue();
			}
		}
		if (Strings.isNotBlank(authOrgs)) {
			for (Long id : ids) {
				if (authOrgs.contains(id.toString())) {
					map.put("authKey", authValue);
				}
			}
		}
		return ok(map);
	}
	
	/**
	 * 获取公文的节点权限列表
	 * @param appName
	 * @param curPermName
	 * @param accountId
	 * @param isTemplete
	 * @return
	 * @throws BusinessException
	 */
	public List<Permission> getPermissions4WFNodeProperties(String appName, String curPermName,Long accountId,boolean isTemplete) throws BusinessException {
		String categoryName = "";
        if("sendEdoc".equalsIgnoreCase(appName) || "edocSend".equals(appName)){
            categoryName=EnumNameEnum.edoc_send_permission_policy.name();
        }else if("recEdoc".equalsIgnoreCase(appName) || "edocRec".equals(appName)){
            categoryName=EnumNameEnum.edoc_rec_permission_policy.name();
        }else if("signReport".equalsIgnoreCase(appName) || "edocSign".equals(appName)){
            categoryName=EnumNameEnum.edoc_qianbao_permission_policy.name();
        }else if("sendGovdoc".equalsIgnoreCase(appName) || "govdocSend".equals(appName)){
            categoryName=EnumNameEnum.edoc_new_send_permission_policy.name();
        }else if("recGovdoc".equalsIgnoreCase(appName) || "govdocRec".equals(appName)){
            categoryName=EnumNameEnum.edoc_new_rec_permission_policy.name();
        }else if("exchangeGovdoc".equalsIgnoreCase(appName) || "govdocExchange".equals(appName)){
            categoryName=EnumNameEnum.edoc_new_change_permission_policy.name();
        }else if("signGovdoc".equalsIgnoreCase(appName) || "govdocSign".equals(appName)){
            categoryName=EnumNameEnum.edoc_new_qianbao_permission_policy.name();
        }else if("sendInfo".equalsIgnoreCase(appName)){
            categoryName = "info_send_permission_policy";
        }else if("collaboration".equals(appName)|| "form".equalsIgnoreCase(appName)){
            categoryName = EnumNameEnum.col_flow_perm_policy.name();
        }else if("office".equals(appName)){
            categoryName = EnumNameEnum.office_flow_perm_policy.name();
        }else if("info".equals(appName)){
        	categoryName = EnumNameEnum.info_send_permission_policy.name();
        }
    	return permissionManager.getPermissions4WFNodeProperties(appName, categoryName, curPermName, accountId, isTemplete);
    }
	
    /**
     * 新公文解全部锁
     * @param summaryId
     * @return
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("unlockNewEdoc")
    public Response unlockNewEdoc(@QueryParam("summaryId") Long summaryId) throws BusinessException {
    	EdocSummary summary=edocManager.getEdocSummaryById(summaryId, true);
    	govdocLockManager.unlockAll(summary,AppContext.getCurrentUser().getId());
    	return ok("true");
    }
    
    /***********************************************S1用到相关接口-S**********************************************************/
    
    
    /**
                * 请求路径edocResource/queryEdocTemplate
                * 根据模板的名称，查找对应模板及其产生的数据条数
     * @param params  templateName 模板名称，pageNo 页码，pageSize 每页记录数
     * @return id 模板ID，subject 模板名称，moduleType 模板类型（401 发文模板，402 收文模板，403 签收模板，404 签报模板），orgAccountId 模板所属单位ID，dataCount 模板下产生的记录数
     * @throws BusinessException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON})
    @Path("queryEdocTemplate")
    @RestInterfaceAnnotation
    public Response selectEdocTemplate(Map<String,String> params) throws Exception{
    	List<Object> result = new ArrayList<Object>();
    	FlipInfo flipInfo = new FlipInfo();
		flipInfo.setPage(ParamUtil.getInt(params, "pageNo",1));
		flipInfo.setSize(ParamUtil.getInt(params, "pageSize",20));
		List<Map<String,Object>> resList = new ArrayList<Map<String,Object>>();
		
		
		flipInfo = EdocResourceUtil.getInstance().getEdocTemplateByTemplateName(params.get("templateName"),flipInfo);
		resList = flipInfo.getData();
		if(!resList.isEmpty()) {
			for (Map<String, Object> map : resList) {
				Map<String,Object> templateResult = new HashMap<String,Object>();
				templateResult.put("id", map.get("id"));
				templateResult.put("subject", map.get("subject"));
				templateResult.put("moduleType", map.get("module_type"));
				templateResult.put("orgAccountId", map.get("org_account_id"));
				templateResult.put("dataCount", EdocResourceUtil.getInstance().countEdocSummaryByTemplateId((Long)map.get("id")));
				result.add(templateResult);
			}
		}
		flipInfo.setData(result);
		
    	return success(flipInfo);
    }
    
    /**
                * 请求路径edocResource/queryEdocData
                * 根据传入条件，查询指定公文
     * @param params  templateName 模板名称，subject 标题，docMark 文号，innerMark 内部文号，发起时间 sDate eDate，pageNo 页码，pageSize 分页数据数量
     * @return	id summaryId，subject 标题， govdocType 公文类型（1发文，2收文，3签报，4交换），templateName 模板名称，senderName 发起人姓名，docMark 文号，innerMark 内部文号
     * @throws Exception
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON})
    @Path("queryEdocData")
    public Response selectEdocData(Map<String,String> params) throws BusinessException{
    	List<Object> result = new ArrayList<Object>();
    	FlipInfo flipInfo = new FlipInfo();
    	try {
    		flipInfo.setPage(ParamUtil.getInt(params, "pageNo",1));
    		flipInfo.setSize(ParamUtil.getInt(params, "pageSize",20));
    		Map<Long,String> templateIdAndName = new HashMap<Long,String>();
    		Map<Long,String> memberIdAndName = new HashMap<Long,String>();
    		Map<String,Object> queryParam = new HashMap<String,Object>();
    		List<Map<String,Object>> resList = new ArrayList<Map<String,Object>>();
    		EdocResourceUtil sourceUtil = EdocResourceUtil.getInstance();
    		if(!Strings.isBlank(ParamUtil.getString(params,"templateName"))) {
    			templateIdAndName = sourceUtil.getTemplateNameAndIdByName(ParamUtil.getString(params,"templateName"));
    			StringBuilder tepIds = new StringBuilder();
    			Iterator<Long> tIds = templateIdAndName.keySet().iterator();
    			while(tIds.hasNext()) {
    				tepIds.append(tIds.next());
    				if(tIds.hasNext()) {
    					tepIds.append(",");
    				}
    			}
    			queryParam.put("templateIds", tepIds.toString());
    		}
    		
    		if(!Strings.isBlank(ParamUtil.getString(params,"memberName"))) {
    			memberIdAndName = sourceUtil.getMemberIdsByName(ParamUtil.getString(params,"memberName"));
    			Iterator<Long> mIds = memberIdAndName.keySet().iterator();
    			StringBuilder merIds = new StringBuilder();
    			while(mIds.hasNext()) {
    				merIds.append(mIds.next());
    				if(mIds.hasNext()) {
    					merIds.append(",");
    				}
    			}
    			queryParam.put("memberIds", merIds.toString());
    		}
    		if(!Strings.isBlank(ParamUtil.getString(params,"subject"))) {
    			queryParam.put("subject", ParamUtil.getString(params,"subject"));
    		}
    		if(!Strings.isBlank(ParamUtil.getString(params,"docMark"))) {
    			queryParam.put("docMark", ParamUtil.getString(params,"docMark"));
    		}
    		if(!Strings.isBlank(ParamUtil.getString(params,"innerMark"))) {
    			queryParam.put("innerMark", ParamUtil.getString(params,"innerMark"));
    		}
    		if(!Strings.isBlank(ParamUtil.getString(params,"sDate"))) {
    			queryParam.put("sDate", ParamUtil.getDate(params,"sDate"));
    		}
    		if(!Strings.isBlank(ParamUtil.getString(params,"eDate"))) {
    			queryParam.put("eDate", ParamUtil.getDate(params,"eDate"));
    		}
    		
    		flipInfo = sourceUtil.getSummaryByCondition(queryParam,flipInfo);
    		resList.addAll(flipInfo.getData());
    		for (Map<String, Object> map : resList) {
    			Map<String,Object> summaryeResult = new HashMap<String,Object>();
    			summaryeResult.put("id", map.get("id"));
    			summaryeResult.put("subject", map.get("subject"));
    			summaryeResult.put("docMark", map.get("doc_mark"));
    			summaryeResult.put("innerMark", map.get("serial_no"));
    			summaryeResult.put("govdocType", map.get("govdoc_type"));
    			summaryeResult.put("templateName", templateIdAndName.get(map.get("templete_id")));
    			summaryeResult.put("senderName", memberIdAndName.get(map.get("start_user_id")));
    			summaryeResult.put("startTime", memberIdAndName.get(map.get("start_time")));
    			
    			result.add(summaryeResult);
    		}
    		
		
    	}catch(Exception e) {
    		LOGGER.error("接口获取公文列表异常 ",e);
    		return fail("接口获取公文列表异常"+e.getMessage());
    	}
     	
    	return success(flipInfo);
    }
    
    /**
                *请求路径 edocResource/deleteData
	    * 删除公文及其流程产生的相关数据
	 * @param opType 操作类型（0 根据公文模板id清除公文数据，1 根据summaryId清除公文数据）,默认为1
	 * @param needDelRec 是否需要删除发文产生的收文数据（0不需要，1需要）
	 * @param param 传入参数，根据操作类型传入templateId或者 List类型的summaryIds集合
	 * @return
	 */
	@POST
	@Path("deleteData/{opType}/{needDelRec}")
	@Produces({ MediaType.APPLICATION_JSON })
	@RestInterfaceAnnotation
	public Response deleteEdocData(@PathParam("opType") Integer opType,@PathParam("needDelRec") Integer needDelRec,Map<String, Object> param) throws BusinessException {
		if(param == null){
			return fail("参数为空，未执行任何清除任务...");
		}
		Integer type = opType == null?0:opType;
		if(type == 0) {
			String templateId = (String) param.get("templateId");
			if(!Strings.isBlank(templateId)){
				CtpTemplate template = templateManager.getCtpTemplate(Long.valueOf(templateId));
				if(template != null) {
					try {
						EdocResourceUtil.getInstance().deleteEdocDataByTemplate(template,needDelRec);
					} catch (Exception e) {
						LOGGER.error(e);
						return fail("根据模板删除公文数据失败...");
					}		
				}
			}	
		}else if(type == 1) {
			List<Long> summaryIds = (ArrayList<Long>) param.get("summaryIds");
			
			String objIds = Joiner.on(",").join(summaryIds);
			try {
				List<Map<String,Object>> opObjs = EdocResourceUtil.getInstance().getSummaryFormIdAndSummaryId(objIds);
				EdocResourceUtil.getInstance().deleteEdocDataBySummary(opObjs);
			} catch (Exception e) {
				LOGGER.error(e);
				return fail("根据公文ID删除公文数据部分失败...");
			}
			
			
			
		}else {
			return fail("不能识别的操作类型（opType）...");
		}
		
		return success(null,"true");
	}
	
    /**
     * 随机获取人员在协同里面对应的affair
     * @Author      : xuqw
     * @Date        : 2016年2月19日下午4:32:55
     * @param summaryId
     * @param _isHistoryFlag
     * @param memberId
     * @return
     * @throws BusinessException
     */
    private CtpAffair findMemberAffair(Long summaryId, boolean _isHistoryFlag, Long memberId)
                                                           throws BusinessException{
        
        CtpAffair ret = null;
        
        List<CtpAffair> affairs = new ArrayList<CtpAffair>();
        if (_isHistoryFlag) {
            affairs = affairManager.getAffairsHis(ApplicationCategoryEnum.collaboration, summaryId, memberId);
        } else {
            affairs = affairManager.getAffairs(ApplicationCategoryEnum.collaboration, summaryId, memberId);
        }
        if (Strings.isNotEmpty(affairs)) {
            for (CtpAffair aff : affairs) {
                if (!aff.isDelete()) {
                    ret = aff;
                    break;
                }
            }
        }
        return ret;
    }
    
	/**
	    * 替换公文文号
	 * @param summaryId
	 * @param docMark 需要替换的文号
	 * @param needDelRec 是否要删除发文产生的收文数据（0 不需要，1需要）
	 * @return
	 */
	@GET
	@Path("replaceEdocMark")
	@RestInterfaceAnnotation
	public Response replaceDocMark(@QueryParam("summaryId") Long summaryId,@QueryParam("docMark") String docMark,@QueryParam("needDelRec") Integer needDelRec) {
		if(summaryId != null && Strings.isNotBlank(docMark)) {
			try {
				EdocSummary summary = edocSummaryManager.findById(summaryId);
				
				if(summary != null) {
					//更新公文文号相关表中的值
					EdocResourceUtil.getInstance().replaceEdocMarkRecords(summary.getId(),docMark);
					//更新主表中docMark的值
					FormBean formBean = formApi4Cap3.getForm(summary.getFormAppid());
					if (formBean != null) {
						List<FormFieldBean> formFieldBeans = formBean.getAllFieldBeans();
						FormDataMasterBean formDataMasterBean = null;
						try {
							formDataMasterBean = formApi4Cap3.findDataById(summary.getFormRecordid(), summary.getFormAppid(), null);
						} catch (SQLException e) {
							LOGGER.error(e);
						}
						for (FormFieldBean formFieldBean : formFieldBeans) {
							if (formFieldBean.isMasterField()) {
								if(formFieldBean.getMappingField().equals("doc_mark")) {
									formApi4Cap3.updateData(summary.getFormRecordid(), formBean.getTableList().get(0).getTableName(), formFieldBean.getName(), docMark);
									break;
								}
							}
						}
					}
					//是否删除，发文产生的收文数据
					if(needDelRec !=null && needDelRec.intValue()==1){
						EdocResourceUtil.getInstance().delRecData(summary.getId());
					}
					
				}else {
					return fail("查询不到相关公文，请检查传入ID是否正确  ");
				}
				
			} catch (Exception e) {
				LOGGER.error(e);
				return fail("替换公文文号异常  "+e.getMessage());
			}
			
		}
		
		return success(null,"true");
	}
	
	/**
	   * 根据条件，查询待办的affair
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@Path("queryPendingAffairForEdoc")
	public Response selectAffairData(Map<String,String> params){
		List<Object> result = new ArrayList<Object>();
		FlipInfo flipInfo = new FlipInfo();
		try {
			flipInfo.setPage(ParamUtil.getInt(params, "pageNo",1));
			flipInfo.setSize(ParamUtil.getInt(params, "pageSize",20));
			Map<Long,String> memberIdAndName = new HashMap<Long,String>();
			Map<String,Object> queryParam = new HashMap<String,Object>();
			List<Map<String,Object>> resList = new ArrayList<Map<String,Object>>();
			EdocResourceUtil sourceUtil = EdocResourceUtil.getInstance();
			
			if(!Strings.isBlank(ParamUtil.getString(params,"memberName"))) {
				memberIdAndName = sourceUtil.getMemberIdsByName(ParamUtil.getString(params,"memberName"));
				Iterator<Long> mIds = memberIdAndName.keySet().iterator();
				StringBuilder merIds = new StringBuilder();
				while(mIds.hasNext()) {
					merIds.append(mIds.next());
					if(mIds.hasNext()) {
						merIds.append(",");
					}
				}
				queryParam.put("memberIds", merIds.toString());
			}
			if(!Strings.isBlank(ParamUtil.getString(params,"subject"))) {
				queryParam.put("subject", ParamUtil.getString(params,"subject"));
			}
			if(!Strings.isBlank(ParamUtil.getString(params,"sDate"))) {
				queryParam.put("sDate", ParamUtil.getDate(params,"sDate"));
			}
			if(!Strings.isBlank(ParamUtil.getString(params,"eDate"))) {
				queryParam.put("eDate", ParamUtil.getDate(params,"eDate"));
			}
			flipInfo = sourceUtil.getPendingAffairByCondition(queryParam,flipInfo);
			
			resList.addAll(flipInfo.getData());
			for (Map<String, Object> map : resList) {
				Map<String,Object> affairResult = new HashMap<String,Object>();
				affairResult.put("id", map.get("id"));
				affairResult.put("subject", map.get("subject"));
				affairResult.put("memberName", map.get("name"));
				affairResult.put("startTime", map.get("create_date"));
				
				result.add(affairResult);
			}
			flipInfo.setData(result);
		}catch(Exception e) {
			LOGGER.error("接口查询待办affair出错",e);
			return fail("接口查询待办affair出错  "+e.getMessage());
		}
		
		return success(flipInfo);
	}
	
	/**
	    * 异常待办置已办
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("exAffairToDone")
	@RestInterfaceAnnotation
	public Response exProcedureAffairToDone(Map<String,Object> params){
		try {
			
			if(!params.isEmpty()) {
				if(!Strings.isBlank(ParamUtil.getString(params,"affairIds"))) {
					List<Long> affairIds = (ArrayList<Long>) params.get("affairIds");
					String objIds = Joiner.on(",").join(affairIds);
					EdocResourceUtil.getInstance().exAffairToDone(objIds);
				}
			}
		}catch(Exception e) {
			LOGGER.error("异常待办置已办错误 ",e);
			return fail("操作失败！");
		}
		
		return success(null,"true");
	}
	
	/**
	    * 公文正文附件替换
	 * @param reContent 是否替换正文（0 不替换，1替换）
	 * @param reAtt	是否替换附件（0 不替换，1替换）
	 * @param params oriSummaryId 被替换的公文ID， reSummaryId 替换的公文ID
	 * @return 是否成功
	 */
	@POST
	@Path("replaceContentAndAtt/{reContent}/{reAtt}")
	@RestInterfaceAnnotation
	public Response replaceContentAndAtt(@PathParam("reContent") Integer reContent, @PathParam("reAtt") Integer reAtt,Map<String,Object> params) {
		Long oriSummaryId = ParamUtil.getLong(params, "oriSummaryId");
		Long reSummaryId = ParamUtil.getLong(params, "reSummaryId");
		if(oriSummaryId != null && reSummaryId != null) {
			try {
				
				EdocSummary oriSummary = edocSummaryManager.findById(oriSummaryId);
				EdocSummary reSummary = edocSummaryManager.findById(reSummaryId);
				if(oriSummary == null || reSummary == null) {
					return fail("查询不到对应ID的公文！");
				}
				//替换正文
				if(reContent.intValue() == 1) {
					boolean needUpdateSummaryAndAffair = false;
					CtpContentAll reCtpContent = GovdocContentHelper.getTransBodyContentByModuleId(reSummary.getId());
					CtpContentAll oriCtpContent = GovdocContentHelper.getTransBodyContentByModuleId(oriSummary.getId());
					oriCtpContent.setContentType(reCtpContent.getContentType());
					oriCtpContent.setModifyDate(new java.util.Date());
					//替换的正文类型
					if(Integer.valueOf(reSummary.getBodyType()) != MainbodyType.HTML.getKey()) {
						//复制正文
						Long copyContentFileId = fileManager.copyFileBeforeModify(Long.valueOf(reCtpContent.getContent()));
						oriCtpContent.setContent(String.valueOf(copyContentFileId));
						//如果替换前后正文类型不一致
						if(Integer.valueOf(oriSummary.getBodyType()) != Integer.valueOf(reSummary.getBodyType())) {
							needUpdateSummaryAndAffair = true;
							oriSummary.setBodyType(reSummary.getBodyType());
						}
						
					}else {
						oriCtpContent.setContent(reCtpContent.getContent());
						//如果替换前后正文类型不一致
						if(Integer.valueOf(oriSummary.getBodyType()) != Integer.valueOf(reSummary.getBodyType())) {
							needUpdateSummaryAndAffair = true;
							oriSummary.setBodyType(reSummary.getBodyType());
						}
						
					}
					ctpMainbodyManager.saveOrUpdateContentAll(oriCtpContent);
					//需要更新Summary和Affair的正文类型
					if(needUpdateSummaryAndAffair) {
						edocSummaryManager.updateEdocSummary(oriSummary);
						EdocResourceUtil.getInstance().updateAffairBodyType(oriSummary.getId(),reSummary.getBodyType());
					}
					
				}
				//替换附件
				if(reAtt.intValue() == 1) {
					//删除原有附件
					attachmentManager.deleteByReference(oriSummary.getId(), oriSummary.getId());
					//获取需要替换公文中的正式附件
					List<Attachment> reAttList = attachmentManager.getByReference(reSummary.getId(), reSummary.getId());
					if(!reAttList.isEmpty()) {
						//复制的附件
						List<Attachment> newAttachmentsTemp = new ArrayList<Attachment>();
						//复制附件对应的ctp_file表中的记录
						List<V3XFile> reV3xFileList = new ArrayList<V3XFile>();
						for (Attachment attachment : reAttList) {
							if (attachment.getType() != Constants.ATTACHMENT_TYPE.FILE.ordinal()) {
								continue;
							}
							//复制附件对象
							Attachment newAttachment = (Attachment) attachment.clone();
							newAttachment.setNewId();
							newAttachment.setCreatedate(new java.util.Date());
							newAttachment.setCategory(ApplicationCategoryEnum.edoc.getKey());
							newAttachment.setReference(oriSummaryId);
							newAttachment.setSubReference(oriSummaryId);
							newAttachmentsTemp.add(newAttachment);
							
							//原始附件对应的文件对象
							V3XFile v3xInFile = fileManager.getV3XFile(attachment.getFileUrl());
							//复制的附件文件    发文单对应的附件文件， 每一个文件只复制一份，所有相同的附件指向同一个附件文件对象
							V3XFile v3xOutFile = (V3XFile) v3xInFile.clone();
							v3xOutFile.setNewId();
							v3xOutFile.setCreateDate(newAttachment.getCreatedate());
							v3xOutFile.setUpdateDate(newAttachment.getCreatedate());
							reV3xFileList.add(v3xOutFile);
							//设置附件文件为新复制的文件id
							newAttachment.setFileUrl(v3xOutFile.getId());
							//upload目录
							String uploadFolder = fileManager.getFolder(newAttachment.getCreatedate(), true);
							//原始文件
							File fileIn = fileManager.getFile(v3xInFile.getId(), v3xInFile.getCreateDate());
							//复制的文件
							File FileOut = new File(uploadFolder + File.separator + v3xOutFile.getId());
							GovdocExchangeHelper.copyFile(fileIn, FileOut);
							
							
						}
						//保存数据库记录
						fileManager.save(reV3xFileList);
						attachmentManager.saveAsAtt(newAttachmentsTemp);
						
					}

				}
				
				return success(null,"true");
			}catch(Exception e) {
				LOGGER.error("替换公文正文附件时出现异常：",e);
				return fail("出现异常，请查看日志！");
			}
		}else {
			
			return fail("传入公文Id为空！");
		}
	}
	
    /***********************************************S1用到相关接口-E**********************************************************/
}
