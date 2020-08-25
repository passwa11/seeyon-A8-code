package com.seeyon.ctp.rest.resources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.collaboration.vo.PermissionMiniVO;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.CustomizeConstants;
import com.seeyon.ctp.common.content.affair.AffairManager;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.affair.constants.TrackEnum;
import com.seeyon.ctp.common.customize.manager.CustomizeManager;
import com.seeyon.ctp.common.dao.paginate.Pagination;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.filemanager.manager.Util;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.office.MSignatureManager;
import com.seeyon.ctp.common.office.MSignaturePicHandler;
import com.seeyon.ctp.common.office.UserUpdateObject;
import com.seeyon.ctp.common.office.manager.OfficeBakFileManager;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.phrase.manager.CommonPhraseManager;
import com.seeyon.ctp.common.phrase.po.CommonPhrase;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.track.manager.CtpTrackMemberManager;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocManagerModel;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocOpinion.OpinionType;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocFormManager;
import com.seeyon.v3x.edoc.manager.EdocH5Manager;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocIndexEnableImpl;
import com.seeyon.v3x.edoc.manager.EdocListManager;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.manager.EdocMarkManager;
import com.seeyon.v3x.edoc.manager.EdocRegisterManager;
import com.seeyon.v3x.edoc.manager.EdocRoleHelper;
import com.seeyon.v3x.edoc.manager.EdocStatManager;
import com.seeyon.v3x.edoc.manager.EdocSummaryManager;
import com.seeyon.v3x.edoc.util.EdocOpenFromUtil.EdocSummaryType;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryBO;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryCountVO;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryModel;
import com.seeyon.v3x.exchange.manager.EdocExchangeManager;
import com.seeyon.v3x.system.signet.domain.V3xHtmDocumentSignature;
import com.seeyon.v3x.system.signet.enums.V3xHtmSignatureEnum;
import com.seeyon.v3x.system.signet.manager.V3xHtmDocumentSignatManager;

import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;
import www.seeyon.com.utils.json.JSONUtils;

@Path("edocResource")
@Produces({ MediaType.APPLICATION_JSON})
public class EdocResource extends BaseResource{
	
	private static Log LOGGER = CtpLogFactory.getLog(EdocResource.class);
	
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
    private EdocIndexEnableImpl edocIndexEnable = (EdocIndexEnableImpl)AppContext.getBean("edocIndexEnable");
    private OfficeBakFileManager officeBakFileManager   = (OfficeBakFileManager) AppContext.getBean("officeBakFileManager");
    private AttachmentManager  attachmentManager = (AttachmentManager)AppContext.getBean("attachmentManager");;
    private UserMessageManager  userMessageManager = (UserMessageManager)AppContext.getBean("userMessageManager");
    private EdocStatManager				edocStatManager = (EdocStatManager) AppContext.getBean("edocStatManager");
    private EdocMarkManager				edocMarkManager = (EdocMarkManager) AppContext.getBean("edocMarkManager");
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
		Integer pageSize = Integer.parseInt(params.get("pageSize"));
		Pagination.setMaxResults(pageSize);
		Pagination.setFirstResult((nPageNo-1)*pageSize);
		
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
		Object[] objects = new Object[3];
		try {
			List<EdocSummaryCountVO> list = edocListManager.getCountGroupByEdocType();
			objects[0] = list.get(0);
			objects[1] = list.get(1);
			objects[2] = list.get(2);
		} catch (BusinessException e) {
        	LOGGER.error("获得待办发文数据报错!",e);
		}
		return ok(objects);
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
			 Map<String, Object> condition = createNewCondition(user,edocType,StateEnum.col_pending.key(),listType,conditionKey,textfield,textfield1);
             try {
                 int listTypeInt = EdocNavigationEnum.LIST_TYPE_PENDING;
                 if("listSent".equals(listType)){
                	  listTypeInt = EdocNavigationEnum.LIST_TYPE_SENT;
                 }
                 if("listWaitSend".equals(listType)){
                	 listTypeInt = EdocNavigationEnum.LIST_TYPE_WAIT_SEND;
                 }
                 if("listDoneAll".equals(listType)){
                	 listTypeInt = EdocNavigationEnum.LIST_TYPE_DONE;
                 }
                 summarys = edocListManager.findEdocPendingList(listTypeInt, condition);
                 //处理名称
                 dealReturnInfo(summarys);
                 flipInfo.setData(EdocSummaryListVO.valueOf(summarys));
                 flipInfo.setTotal(Pagination.getRowCount());
			 } catch (BusinessException e) {
				 LOGGER.error("获得待办发文数据报错!",e);
			 }
		}
		return ok(flipInfo);
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
				findSourceInfo = edocIndexEnable.findSourceInfo(summaryId);
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
        		
        		edocSummaryVo.setNodePolicy(affair.getNodePolicy());
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
        
        List<Permission> result = permissionManager.getPermissions4WFNodeProperties(appName, policyName,accountId,isTemplate);
        
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
        
        Boolean returnValue = true;
        try {
			edocH5Manager.transDealEdoc(edocManagerModel, wfParamMap);
		} catch (BusinessException e) {
			returnValue = false;
			LOGGER.error(e);
		} finally {
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
    	
    	boolean haveEdocSend       = user.hasResourceCode("F07_sendManager");  //发文管理
		boolean haveEdocSignReport = user.hasResourceCode("F07_signReport");   //签报管理
		boolean haveEdocRec        = user.hasResourceCode("F07_recManager");   //收文管理
		
		params.put("haveEdocSend", haveEdocSend);
		params.put("haveEdocSignReport", haveEdocSignReport);
		params.put("haveEdocRec", haveEdocRec);
        	
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
   		if (EdocRoleHelper.isAccountExchange() || EdocRoleHelper.isDepartmentExchange()) {
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
    

}
