package com.seeyon.apps.govdoc.manager.impl;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.exchange.engine.service.ExchangeService;
import com.seeyon.apps.govdoc.bo.DateSharedWithWorkflowEngineThreadLocal;
import com.seeyon.apps.govdoc.constant.GovdocEnum;
import com.seeyon.apps.govdoc.constant.GovdocEnum.ExchangeDetailStatus;
import com.seeyon.apps.govdoc.constant.GovdocEnum.TransferStatus;
import com.seeyon.apps.govdoc.dao.GovdocExchangeDao;
import com.seeyon.apps.govdoc.helper.GovdocContentHelper;
import com.seeyon.apps.govdoc.manager.GovdocExchangeAccountManager;
import com.seeyon.apps.govdoc.manager.GovdocExchangeManager;
import com.seeyon.apps.govdoc.manager.GovdocFormManager;
import com.seeyon.apps.govdoc.manager.GovdocObjTeamManager;
import com.seeyon.apps.govdoc.manager.GovdocPubManager;
import com.seeyon.apps.govdoc.manager.GovdocRegisterManager;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.govdoc.mark.manager.GovdocMarkManager;
import com.seeyon.apps.govdoc.po.GovdocExchangeDetail;
import com.seeyon.apps.govdoc.po.GovdocExchangeDetailLog;
import com.seeyon.apps.govdoc.po.GovdocExchangeMain;
import com.seeyon.apps.govdoc.po.JointlyIssyedVO;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.apps.govdoc.vo.GovdocBaseVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
//import com.seeyon.apps.ocip.exchange.edoc.IOCIPExchangeEdocManager;
import com.seeyon.apps.ocip.util.OcipEdocUtil;
import com.seeyon.apps.ocip.util.OrgUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormDataMasterBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.manager.GovdocTemplateDepAuthManager;
import com.seeyon.ctp.form.po.GovdocTemplateDepAuth;
import com.seeyon.ctp.form.util.EntityPo;
import com.seeyon.ctp.form.util.FormEntityName;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.BeanUtils;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.ocip.common.IConstant.AddressType;
import com.seeyon.ocip.common.entry.Address;
import com.seeyon.ocip.common.organization.IOrganizationManager;
import com.seeyon.ocip.common.utils.Global;
import com.seeyon.ocip.exchange.model.BIZContentType;
import com.seeyon.ocip.exchange.model.BIZExchangeData;
import com.seeyon.ocip.exchange.model.BIZMessage;
import com.seeyon.ocip.exchange.model.Organization;
import com.seeyon.ocip.exchange.model.PropertyValue;
import com.seeyon.ocip.exchange.model.edoc.EdocOperation;
import com.seeyon.ocip.exchange.model.edoc.OFCEdocObject;
import com.seeyon.ocip.exchange.model.edoc.RETEdocObject;
import com.seeyon.ocip.exchange.model.edoc.SeeyonEdoc;
import com.seeyon.ocip.online.OnlineChecker;
import com.seeyon.v3x.edoc.dao.EdocObjTeamDao;
import com.seeyon.v3x.edoc.domain.EdocObjTeam;
import com.seeyon.v3x.edoc.domain.EdocObjTeamMember;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocObjTeamManager;
import com.seeyon.v3x.exchange.domain.ExchangeAccount;

/**
 * 新公文交换管理类
 * 
 * @author 唐桂林
 *
 */
public class GovdocExchangeManagerImpl implements GovdocExchangeManager {
	
	private static final Log LOGGER = CtpLogFactory.getLog(GovdocExchangeManagerImpl.class);
	
	private GovdocExchangeDao govdocExchangeDao;
	private GovdocSummaryManager govdocSummaryManager;
	private GovdocMarkManager govdocMarkManager;
	private GovdocRegisterManager govdocRegisterManager;
	private GovdocObjTeamManager govdocObjTeamManager;
	private GovdocExchangeAccountManager govdocExchangeAccountManager;
	private GovdocTemplateDepAuthManager govdocTemplateDepAuthManager;
	private GovdocFormManager govdocFormManager;
	private OrgManager orgManager;
	private IOrganizationManager organizationManager;
//	private IOCIPExchangeEdocManager ocipExchangeEdocManager;
	private AffairManager affairManager;
	private GovdocPubManager govdocPubManager;
	private TemplateManager templateManager;
	private WorkflowApiManager wapi;
	private FormApi4Cap3 formApi4Cap3;

	/*************************** 111111 公文交换页面查询 start ******************************/
	@SuppressWarnings("unchecked")
	@Override
	public FlipInfo findGovdocExchangeDetail(FlipInfo flipInfo, Map<String, String> conditionMap) {
		List<GovdocExchangeDetail> list = govdocExchangeDao.findGovdocExchangeDetail(flipInfo,conditionMap);
		List<Map<String,String>> newList = new ArrayList<Map<String,String>>();
		if(null!=list && list.size()>0) {
			for (GovdocExchangeDetail detail : list) {
				String jsonStr = JSONUtil.toJSONString(detail);
				Map<String,String> map = (Map<String, String>) JSONUtil.parseJSONString(jsonStr);
				map.put("statusName", GovdocEnum.ExchangeDetailStatus.getExchangeDetailStatus(detail.getStatus()).getValue());
				//交换单发送单位
				try {
					V3xOrgMember member = orgManager.getMemberById(detail.getRecUserId());
					if (member != null) {
						map.put("recUserPhone", member.getTelNumber());
					}
					//收文单位,外单位要把单位名称加上
					V3xOrgEntity orgEntity = orgManager.getEntityOnlyById(detail.getRecOrgId());
					if (orgEntity instanceof V3xOrgDepartment){
						if (orgEntity.getOrgAccountId().longValue() != detail.getSendAccountId().longValue()) {
							map.put("recOrgName", orgEntity.getName() + "(" + orgManager.getAccountById(orgEntity.getOrgAccountId()).getShortName() + ")");
						}
					}
				} catch (BusinessException e) {
					LOGGER.error("交换详情页面，发送文单获取出错",e);
				}
				
				//签收回退或撤销时，不显示签收人，签收编号，签收时间
				if(detail.getStatus()==GovdocEnum.ExchangeDetailStatus.waitSend.getKey()
						|| detail.getStatus()==GovdocEnum.ExchangeDetailStatus.hasCancel.getKey()) {
					map.put("recNo", "");
					map.put("recUserName", "");
					map.put("recTime", "");
				}
				newList.add(map);
			}
		}
		
		flipInfo.setData(newList);
		return flipInfo;
	}
	
	@Override
	public List<GovdocExchangeDetail> getGovdocExchangeDetailList(FlipInfo flipInfo, Map<String, String> conditionMap) {
		List<GovdocExchangeDetail> list = govdocExchangeDao.findGovdocExchangeDetail(flipInfo,conditionMap);
		return list;
	}
	
	@Override
	public FlipInfo findGovdocExchangeDetailLog(FlipInfo flipInfo, Map<String, String> conditionMap) {
		List<GovdocExchangeDetailLog> list = govdocExchangeDao.findGovdocExchangeDetailLog(flipInfo,conditionMap);
		flipInfo.setData(list);
		return flipInfo;
	}
	/*************************** 111111 公文交换页面查询   end ******************************/
	
	
	/*************************** 222222 公文交换对象获取 start ******************************/
	public List<GovdocExchangeMain> findByReferenceIdId(Long summaryId, Integer exchangerType){
		return govdocExchangeDao.findByReferenceIdId(summaryId, exchangerType);
	}
	@Override
	public GovdocExchangeMain getGovdocExchangeMainById(Long mainId) {
		return govdocExchangeDao.getGovdocExchangeMainById(mainId);
	}
	@Override
	public GovdocExchangeMain findBySummaryId(Long summaryId, Integer exchangeType) throws BusinessException {
		return govdocExchangeDao.findBySummaryId(summaryId, exchangeType);
	}
	@Override
	public int findRelationBySummaryIdAndReference(Long summaryId,Long referenceId) throws BusinessException{
		return govdocExchangeDao.findRelationBySummaryIdAndReference(summaryId, referenceId);
	}
	@Override
	public List<GovdocExchangeDetail> getDetailByMainId(Long mainId) throws BusinessException {
		return govdocExchangeDao.getDetailByMainId(mainId);
	}
	@Override
	public GovdocExchangeDetail findDetailBySummaryId(Long summaryId) throws BusinessException{
		return govdocExchangeDao.findDetailBySummaryId(summaryId);
	}
	@Override
	public GovdocExchangeDetail findDetailByRecSummaryId(Long recSummaryId) {
		return govdocExchangeDao.findDetailByRecSummaryId(recSummaryId);
	}
	@AjaxAccess
	@Override
	public GovdocExchangeDetail getExchangeDetailById(Long detailId) {
		return govdocExchangeDao.getExchangeDetailById(detailId);
	}
	@Override
	public String govdocExchangeDetailCount(Long mainId) throws BusinessException {
		List<Object[]>  reCountDetail = govdocExchangeDao.findDetailCountBySummaryId(mainId);
    	Map<String, Object> result = new HashMap<String, Object>();
    	if(reCountDetail != null && reCountDetail.size()>0){
    		Object[] data = reCountDetail.get(0);
    		result.put("totalCount", data[0]);
    		result.put("sendCount", data[1]);
    		result.put("waitSignCount", data[2]);
    		result.put("backCount", data[3]);
    	}
    	return JSONUtil.toJSONString(result);
	}
	@Override
	public void showExchangeState(GovdocSummaryVO summaryVO) {
		try{//一屏式 分送状态
			GovdocExchangeMain main = this.findBySummaryId(summaryVO.getSummary().getId(),null);
			int govdocview_delivery = 0;//共分送
			int govdocview_signed = 0;
			int govdocview_waitSign = 0;//待签收
			int govdocview_hasBack = 0;//已回退
			List<CtpAffair> affairs = affairManager.getAffairs(summaryVO.getSummary().getId());
			boolean hasMember = false;
			for (CtpAffair ctpAffair : affairs) {
				if(ctpAffair.getMemberId() == AppContext.currentUserId()){
					hasMember=true;
					break;
				}
			}
			if(main!=null&&hasMember){
				if(main.getStartUserId()==AppContext.currentUserId()){
					//mav.addObject("isSender",1);
					AppContext.putRequestContext("isSender", 1);
				}
				List<GovdocExchangeDetail> details = this.getDetailByMainId(main.getId());
				govdocview_delivery = details.size();
				for (GovdocExchangeDetail govdocExchangeDetail : details) {
					if(govdocExchangeDetail.getStatus()==ExchangeDetailStatus.waitSign.getKey()){
						govdocview_waitSign++;
					}
					if(govdocExchangeDetail.getStatus()==ExchangeDetailStatus.hasBack.getKey()){
						govdocview_hasBack++;
					}
					if(govdocExchangeDetail.getStatus()==ExchangeDetailStatus.hasSign.getKey()||govdocExchangeDetail.getStatus()==ExchangeDetailStatus.hasFenBan.getKey()
							||govdocExchangeDetail.getStatus()==ExchangeDetailStatus.draftFenBan.getKey()){
						govdocview_signed++;
					}
				}
			}
			/*mav.addObject("govdocview_signed",govdocview_signed);
			mav.addObject("govdocview_delivery",govdocview_delivery);
			mav.addObject("govdocview_waitSign",govdocview_waitSign);
			mav.addObject("govdocview_hasBack",govdocview_hasBack);*/
			AppContext.putRequestContext("govdocview_signed", govdocview_signed);
			AppContext.putRequestContext("govdocview_delivery", govdocview_delivery);
			AppContext.putRequestContext("govdocview_waitSign", govdocview_waitSign);
			AppContext.putRequestContext("govdocview_hasBack", govdocview_hasBack);
		}catch(Exception e){
			LOGGER.error("获取分送状态失败", e);
		}
	}
	@Override
	public Map<String, Object> getChuantouchakanId(GovdocSummaryVO summaryVO) throws BusinessException {
		Map<String, Object> result = new HashMap<String, Object>();
		GovdocExchangeMain main = null;
		GovdocExchangeDetail detail = null;
		switch (summaryVO.getGovdocType()) {
		case 1:
			/*main = govdocExchangeManager.findBySummaryId(summaryVO.getSummaryId(),null);
			if (main != null) {
				result.put("govdocExchangeMainId", main.getId());
			}*/
			break;
		case 2:
			detail = this.findDetailByRecSummaryId(summaryVO.getSummaryId());
			if (detail != null) {
				main = this.getGovdocExchangeMainById(detail.getMainId());
//				if(AppContext.hasPlugin("ocip")) {
//					try{
//						V3xOrgEntity entity = orgManager.getEntityOnlyById(main.getStartUserId());
//						if(OrgUtil.isPlatformEntity(entity)){
//							result.put("exchangeOutAccount", entity.getId());
//							AppContext.putRequestContext("exchangeOutAccount", entity.getId());
//						}
//					}catch(Exception e){
//						LOGGER.error(e);
//					}
//				}
				result.put("govdocExchangeSignSummaryId", detail.getSummaryId());
				EdocSummary summary = govdocSummaryManager.getSummaryById(detail.getSummaryId());
				result.put("govdocExchangeSignSummarySubject", summary.getSubject());
				result.put("govdocExchangeSendSummaryId", main.getSummaryId());
				result.put("govdocExchangeSendSummarySubject", main.getSubject());
				
				AppContext.putRequestContext("govdocExchangeSignSummaryId", detail.getSummaryId());
				AppContext.putRequestContext("govdocExchangeSignSummarySubject", summary.getSubject());
				AppContext.putRequestContext("govdocExchangeSendSummaryId", main.getSummaryId());
				AppContext.putRequestContext("govdocExchangeSendSummarySubject", main.getSubject());
			}
			break;
		case 4:
			detail = this.findDetailBySummaryId(summaryVO.getSummaryId());
			if (detail != null) {
				main = this.getGovdocExchangeMainById(detail.getMainId());
				
//				if(AppContext.hasPlugin("ocip")) {
//					try{
//						V3xOrgEntity entity = orgManager.getEntityOnlyById(main.getStartUserId());
//						if(OrgUtil.isPlatformEntity(entity)){
//							result.put("exchangeOutAccount", entity.getId());
//						}
//					}catch(Exception e){
//						LOGGER.error(e);
//					}
//				}
				result.put("govdocExchangeSendSummaryId", main.getSummaryId());
				result.put("govdocExchangeSendSummarySubject", main.getSubject());
				AppContext.putRequestContext("govdocExchangeSendSummaryId", main.getSummaryId());
				AppContext.putRequestContext("govdocExchangeSendSummarySubject", main.getSubject());
				if (detail.getRecSummaryId() != null && detail.getStatus() == GovdocEnum.ExchangeDetailStatus.hasFenBan.getKey()) {
					result.put("govdocExchangeRecSummaryId", detail.getRecSummaryId());
					EdocSummary summary = govdocSummaryManager.getSummaryById(detail.getRecSummaryId());
					result.put("govdocExchangeRecSummarySubject", summary.getSubject());
					AppContext.putRequestContext("govdocExchangeRecSummaryId", detail.getRecSummaryId());
					AppContext.putRequestContext("govdocExchangeRecSummarySubject", summary.getSubject());
				}
			}
			break;
		default:
			break;
		}

		return result;
	}
	/*************************** 222222 公文交换对象获取   end ******************************/
	
	
	/*************************** 333333 公文交换对象保存  start ******************************/
	@Override
	public void saveOrUpdateMain(GovdocExchangeMain main) throws BusinessException {
		govdocExchangeDao.saveOrUpdateMain(main);
	}
	@Override
	public void saveDetailList(List<GovdocExchangeDetail> details) throws BusinessException {
		govdocExchangeDao.saveDetailList(details);
	}
	@Override
	public void updateDetailList(List<GovdocExchangeDetail> details) {
		govdocExchangeDao.updateDetailList(details);
	}
	@Override
	public void updateDetail(GovdocExchangeDetail govdocExchangeDetail) {
		govdocExchangeDao.updateDetail(govdocExchangeDetail);
		govdocRegisterManager.saveByDetail(govdocExchangeDetail);
	}
	@Override	
	public void updateExchangeDetailState(Long detailId,ExchangeDetailStatus state){
		govdocExchangeDao.updateExchangeDetailState(detailId, state);
	}
	@Override	
	public void saveDetailLogList(List<GovdocExchangeDetailLog> logs) {
		govdocExchangeDao.saveDetailLogList(logs);
	}
	@Override
	public void saveDetailLog(GovdocExchangeDetailLog detailLog) {
		govdocExchangeDao.saveDetailLog(detailLog);
	}
	/*************************** 333333 公文交换对象保存    end ******************************/
	
	
	/*************************** 444444 公文交换操作  start ******************************/
	//分送
	@Override
	public void exchangeSend(EdocSummary summary,Long currentUserId,Long sendAffairId, Map<String,Object> extendParam) throws BusinessException {
		if (currentUserId == null) {
			currentUserId = AppContext.currentUserId();
		}
		V3xOrgMember member = orgManager.getMemberById(currentUserId);
		int exchangeType = extendParam != null && extendParam.containsKey("exchangeType") ? (Integer) extendParam.get("exchangeType") : GovdocExchangeMain.EXCHANGE_TYPE_JIAOHUAN;
		GovdocExchangeMain main = this.findBySummaryId(summary.getId(), exchangeType);
		Date now = new Date();
		// main 为空则是第一次发送， 否则为补发
		if (main == null) {
			
			main = new GovdocExchangeMain();
			main.setIdIfNew();
			main.setCreateTime(now);
			main.setStartTime(now);
			main.setSubject(summary.getSubject());
			main.setStartUserId(currentUserId);
			main.setCreateTime(now);
			main.setSummaryId(summary.getId());
			main.setAffairId(sendAffairId);
			main.setType(exchangeType);
			main.setCreatePerson(orgManager.getMemberById(currentUserId).getName());
			//发送方公文，是否是收文来的
			GovdocExchangeDetail detail = this.findDetailByRecSummaryId(summary.getId());
			if(detail!=null){
				GovdocExchangeMain mainFirst = this.getGovdocExchangeMainById(detail.getMainId());
				if(mainFirst.getOriginalFileId()!=null){
					main.setOriginalFileId(mainFirst.getOriginalFileId());
				}
			}
			if(main.getOriginalFileId()==null){
				CtpContentAll ctpContentAll = GovdocContentHelper.getBodyContentByModuleId(summary.getId());
				if(ctpContentAll!=null&&ctpContentAll.getContentType()!=MainbodyType.HTML.getKey()){
					try{
						Long fileId = Long.parseLong(ctpContentAll.getContent());
						main.setOriginalFileId(fileId);
					}catch (Exception e) {
						LOGGER.error("获取MyContentNameId错误");
					}
				}
			}
			
		}
		
		//发送数据的人
		Address sender = new Address();
		String localSystemCode = Global.getConfig("sysCode");
		sender.setName(member.getName());
		sender.setResource(localSystemCode);
		sender.setId(member.getId().toString());
		sender.setType(AddressType.member.name());
		Organization sendOrg = Address.addressToOrgnation(sender);
		//接收数据的单位
		String sendToId = summary.getSendToId();
		if (Strings.isNotBlank(summary.getCopyToId())) {
			if (Strings.isNotBlank(sendToId)) {
				sendToId += ("," + summary.getCopyToId());
			}else{
				sendToId = summary.getCopyToId();
			}
		}
		if (Strings.isNotBlank(summary.getReportToId())) {
			if (Strings.isNotBlank(sendToId)) {
				sendToId += ("," + summary.getReportToId());
			}else{
				sendToId = summary.getReportToId();
			}
		}
		// 考虑到转收文,转收文的单位不是通过summary中获取的, 联合发文也一样
		if (extendParam != null && extendParam.containsKey("sendToId")) {
			sendToId = extendParam.get("sendToId").toString();
		}
		String[] sendToIdArr = sendToId.split(",");
		List<Organization> recivers = new ArrayList<Organization>();
		List<GovdocExchangeDetail> details = new ArrayList<GovdocExchangeDetail>();
		List<GovdocExchangeDetailLog> detailLogs = new ArrayList<GovdocExchangeDetailLog>();
		//这个集合保存接收单位的id， 防止有重复的单位
		List<Long> recieveIds = new ArrayList<Long>(); 
		HashMap<Long, Long> orgIdAndDetailId = new HashMap<Long, Long>();

		BIZExchangeData data = new BIZExchangeData();
		data.setIdentifier(UUID.randomUUID().toString().replaceAll("-", ""));
		//组装地址
		for (String idStr : sendToIdArr) {
			if (idStr.contains("undefined")){
				continue;
			}
			V3xOrgEntity entity = orgManager.getEntity(idStr);
			if (entity != null && entity.getEnabled()) {
				if (recieveIds.contains(entity.getId())) {
					continue;
				}
				Address reciver = new Address();
				reciver = OrgUtil.getAddress(entity);
				recieveIds.add(entity.getId());
				recivers.add(Address.addressToOrgnation(reciver));
				GovdocExchangeDetail detail = createDetail(member, main, now, reciver);
				detail.setSendUnit(summary.getSendUnit());
				Map<String,Object> extAttr = new HashMap<String, Object>();
				extAttr.put("identifier", data.getIdentifier());
				GovdocUtil.setExtProperty(detail, extAttr);
				detail.setOpinion(extendParam != null && extendParam.containsKey("opinion") ? extendParam.get("opinion").toString() : null);
				details.add(detail);
				orgIdAndDetailId.put(Long.valueOf(reciver.getId()), detail.getId());
				
				if (main.getType() != GovdocExchangeMain.EXCHANGE_TYPE_LIANHE) {
					GovdocExchangeDetailLog detailLog = createDetailLog(member, detail);
					detailLogs.add(detailLog);
				}
			}else if (idStr.indexOf("OrgTeam") > -1){
				EdocObjTeamDao edocObjTeamDao = (EdocObjTeamDao) AppContext.getBean("edocObjTeamDao");
                EdocObjTeam edocObjTeam = edocObjTeamDao.get(Long.valueOf(idStr.split("\\|")[1]));
                Set<EdocObjTeamMember> edocObjTeamMembers = edocObjTeam.getEdocObjTeamMembers();
            	if(CollectionUtils.isNotEmpty(edocObjTeamMembers)){
					for (EdocObjTeamMember edocObjTeamMember : edocObjTeamMembers) {
						entity = orgManager.getEntity(edocObjTeamMember.getTeamType() + "|" + edocObjTeamMember.getMemberId());
						if (entity != null && entity.getEnabled()) {
							if (recieveIds.contains(entity.getId())) {
								continue;
							}
							Address reciver = new Address();
							reciver = OrgUtil.getAddress(entity);
							recieveIds.add(entity.getId());
							recivers.add(Address.addressToOrgnation(reciver));
							GovdocExchangeDetail detail = createDetail(member, main, now, reciver);
							detail.setSendUnit(summary.getSendUnit());
							detail.setOpinion(extendParam != null && extendParam.containsKey("opinion") ? extendParam.get("opinion").toString() : null);
							Map<String,Object> extAttr = new HashMap<String, Object>();
							extAttr.put("identifier", data.getIdentifier());
							GovdocUtil.setExtProperty(detail, extAttr);
							details.add(detail);
							orgIdAndDetailId.put(Long.valueOf(reciver.getId()), detail.getId());
							
							if (main.getType() != GovdocExchangeMain.EXCHANGE_TYPE_LIANHE) {
								GovdocExchangeDetailLog detailLog = createDetailLog(member, detail);
								detailLogs.add(detailLog);
							}
						}else{
							LOGGER.info("机构组中该单位不存在：" + edocObjTeamMember.getTeamType() + "|" + edocObjTeamMember.getMemberId());
						}
					}
            	}
			}else{
				LOGGER.info("该主送单位不存在：" + sendToIdArr);
			}
		}
		
		if (CollectionUtils.isEmpty(recieveIds)) {
			LOGGER.info("未找到主送单位：" + sendToIdArr);
		}
		
		BIZMessage bizMessage = new BIZMessage();
		bizMessage.setContentType(BIZContentType.OFC);
		OFCEdocObject object = new OFCEdocObject();
		object.setTitle(summary.getSubject());
		if(summary.getBodyType() != null && Strings.isNotBlank(String.valueOf(summary.getBodyType()))) {
			object.setContentType(MainbodyType.getEnumByKey(Integer.parseInt(summary.getBodyType())).name());
		}
		SeeyonEdoc seeyonEdoc = new SeeyonEdoc();
		seeyonEdoc.setMainId(main.getId().toString());
		seeyonEdoc.setDocumentIdentifier(summary.getId().toString());
		seeyonEdoc.setDetailIds(orgIdAndDetailId);
		object.setExtendAttr(seeyonEdoc);
		bizMessage.setContent(object);
		
		//拼装ocip数据包
		this.initFormData(object, seeyonEdoc, summary);
		
		
		data.setRecivers(recivers);
		Address source = new Address();
		source.setId(localSystemCode);
		source.setName(localSystemCode);
		data.setSource(source);
		data.setSender(sendOrg);
		data.setSubject(summary.getSubject());
		data.setBussnissMessage(bizMessage);
		
		this.saveOrUpdateMain(main);
		this.saveDetailList(details);
		this.saveDetailLogList(detailLogs);
		
		
		if (exchangeType == GovdocExchangeMain.EXCHANGE_TYPE_JIAOHUAN) {
			Map<String, Object> columns = new HashMap<String, Object>();
			if (summary.getExchangeSendAffairId() == null) {
				columns.put("exchangeSendAffairId", sendAffairId);
			}
			if (summary.getPackTime() == null) {
				columns.put("packTime", new java.sql.Timestamp(System.currentTimeMillis()));
			}
			if (columns.size() > 0) {
				govdocSummaryManager.update(summary.getId(), columns);
			} 
		}
		//过滤OCIP单位与本系统单位
		ocipOrgFilter(data);
		ExchangeService.sendObject(data);
	}

	//重发
	@Override
	public String reSend(Map<String, String> params) throws BusinessException {
		EdocSummary edocSummary = govdocSummaryManager.getSummaryById(Long.valueOf(params.get("summaryId")));
		Long detailId = Long.valueOf(params.get("detailId"));
    	String mainId = params.get("mainId");
    	String recOrgId = params.get("recOrgId");
    	Integer recOrgType = Integer.valueOf(params.get("recOrgType").trim());
		String recOrgIdStr = "Account";
		if (recOrgType == GovdocEnum.GovdocExchangeOrgType.department.getKey()) {
			recOrgIdStr = "Department";
		}
		recOrgIdStr += ("|" + recOrgId);
		this.exchangeReSend(edocSummary,mainId, detailId, recOrgIdStr);
		return "";
	}
	
	/**
	 * 获取外单位交换单位类型
	 * @param recOrgType
	 * @param id
	 * @return
	 */
	private  String getTypeAndIds(int recOrgType, Long id) {
		String typeAndIds = "";
		if (GovdocEnum.GovdocExchangeOrgType.department.getKey() == recOrgType) {
			typeAndIds = "Department|" + id;
		} else if (GovdocEnum.GovdocExchangeOrgType.account.getKey() == recOrgType) {
			typeAndIds = "Account|" + id;
		}
		return typeAndIds;
	}

	private void ocipOrgFilter(BIZExchangeData bizExchangeData) {
		BIZMessage message = bizExchangeData.getBussnissMessage();
		
		
		if(message.getContentType()==BIZContentType.OFC){
			List<Organization> ownSysList = new ArrayList<Organization>();
			List<Organization> otherSysList = new ArrayList<Organization>();
			List<Organization> organizations = bizExchangeData.getRecivers();
			for (Organization organization : organizations) {
				try{
					Address add = organization.getIdentification();
					V3xOrgEntity entity = orgManager.getEntityOnlyById(Long.parseLong(add.getId()));
					if(entity!=null&&OrgUtil.isPlatformEntity(entity)){
						otherSysList.add(organization);
					}else{
						ownSysList.add(organization);
					}
				}catch(Exception e){
					LOGGER.error(e);
				}
			}
			if(CollectionUtils.isNotEmpty(ownSysList)){
				bizExchangeData.setRecivers(ownSysList);
			}
//			if(AppContext.hasPlugin("ocip")&&CollectionUtils.isNotEmpty(otherSysList)){
//				BIZExchangeData bizExchangeDataOut = new BIZExchangeData();
//				Organization sender = new Organization();
//				Address address = new Address();
//				//只复制data的话，里面的sender指向同一个对象，所以也要复制 chenyq
//				BeanUtils.convert(bizExchangeDataOut, bizExchangeData);
//				BeanUtils.convert(sender, bizExchangeData.getSender());
//				BeanUtils.convert(address, bizExchangeData.getSender().getIdentification());
//				sender.setIdentification(address);
//				bizExchangeDataOut.setSender(sender);
//				bizExchangeDataOut.setRecivers(otherSysList);
//				try {
//					ocipExchangeEdocManager.sendEdoc(bizExchangeDataOut,AppContext.getCurrentUser());
//				}catch (Exception e) {
//					LOGGER.error("处理发送外单位公文失败",e);
//				}
//			}
		}
	}

	/**
	 * @param member
	 * @param detail
	 * @return
	 */
	private GovdocExchangeDetailLog createDetailLog(V3xOrgMember member, GovdocExchangeDetail detail) {
		GovdocExchangeDetailLog detailLog = new GovdocExchangeDetailLog();
		detailLog.setNewId();
		detailLog.setDetailId(detail.getId());
		detailLog.setUserName(member.getName());
		detailLog.setStatus(GovdocEnum.ExchangeDetailStatus.waitSend.getKey());
		detailLog.setDescription("已发送，待收文单位接收");
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		detailLog.setTime(date);
		detailLog.setTimeMS(calendar.get(Calendar.MILLISECOND));
		return detailLog;
	}

	/**
	 * @param member
	 * @param main
	 * @param now
	 * @param reciver
	 * @return
	 * @throws NumberFormatException
	 */
	private GovdocExchangeDetail createDetail(V3xOrgMember member, GovdocExchangeMain main, Date now, Address reciver)
			throws NumberFormatException {
		GovdocExchangeDetail detail = new GovdocExchangeDetail();
		detail.setIdIfNew();
		detail.setMainId(main.getId());
		detail.setSendAccountId(member.getOrgAccountId());
		detail.setRecOrgId(Long.valueOf(reciver.getId()));
		detail.setRecOrgName(reciver.getName());
		if(GovdocEnum.GovdocExchangeOrgType.department.name().equalsIgnoreCase(reciver.getType())){
			try {
				V3xOrgDepartment department = orgManager.getDepartmentById(Long.valueOf(reciver.getId()));
				detail.setRecAccountId(department.getOrgAccountId());
			} catch (BusinessException e) {
				LOGGER.error("获取部门失败",e);
			}
		}else{
			detail.setRecAccountId(Long.valueOf(reciver.getId()));
		}
		detail.setRecOrgType(GovdocEnum.GovdocExchangeOrgType.valueOf(reciver.getType().toLowerCase()).getKey());
		detail.setStatus(GovdocEnum.ExchangeDetailStatus.waitSend.getKey());
		detail.setCreateTime(now);
		return detail;
	}
	
	//重发
	private void exchangeReSend(EdocSummary summary,String mainId, Long detailId,String recOrgIdStr) throws BusinessException {
		//发送数据的人
		Address sender = new Address();
		String localSystemCode = Global.getConfig("sysCode");
		sender.setName(AppContext.currentUserName());
		sender.setResource(localSystemCode);
		sender.setId(String.valueOf(AppContext.currentUserId()));
		sender.setType(AddressType.member.name());
		Organization sendOrg = Address.addressToOrgnation(sender);
		//接收数据的单位
		List<Organization> recivers = new ArrayList<Organization>();
		HashMap<Long, Long> orgIdAndDetailId = new HashMap<Long, Long>();

		//组装地址
		V3xOrgEntity entity = orgManager.getEntity(recOrgIdStr);
		if (entity != null && entity.getEnabled()) {
			Address reciver = new Address();
			reciver = OrgUtil.getAddress(entity);
			recivers.add(Address.addressToOrgnation(reciver));
			orgIdAndDetailId.put(Long.valueOf(reciver.getId()), detailId);
		}
		BIZMessage bizMessage = new BIZMessage();
		bizMessage.setContentType(BIZContentType.OFC);
		OFCEdocObject object = new OFCEdocObject();
		object.setTitle(summary.getSubject());
		if(summary.getBodyType() != null && Strings.isNotBlank(summary.getBodyType())) {
			object.setContentType(MainbodyType.getEnumByKey(Integer.parseInt(summary.getBodyType())).name());
		}
		SeeyonEdoc seeyonEdoc = new SeeyonEdoc();
		seeyonEdoc.setMainId(mainId);
		seeyonEdoc.setDocumentIdentifier(summary.getId().toString());
		seeyonEdoc.setDetailIds(orgIdAndDetailId);
		object.setExtendAttr(seeyonEdoc);
		bizMessage.setContent(object);
		
		//拼装ocip数据包
		this.initFormData(object, seeyonEdoc, summary);
		
		BIZExchangeData data = new BIZExchangeData();
		data.setIdentifier(UUID.randomUUID().toString().replaceAll("-", ""));
		data.setRecivers(recivers);
		data.setSender(sendOrg);
		data.setSubject(summary.getSubject());
		data.setBussnissMessage(bizMessage);
		
		Address source = new Address();
		source.setId(localSystemCode);
		source.setName(localSystemCode);
		data.setSource(source);
		GovdocExchangeDetailLog detailLog = new GovdocExchangeDetailLog();
		detailLog.setNewId();
		detailLog.setDetailId(detailId);
		detailLog.setUserName(AppContext.currentUserName());
		detailLog.setStatus(GovdocEnum.ExchangeDetailStatus.waitSend.getKey());
		detailLog.setDescription("已发送，待收文单位接收");
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		detailLog.setTime(date);
		detailLog.setTimeMS(calendar.get(Calendar.MILLISECOND));
		GovdocExchangeDetail detail = this.getExchangeDetailById(detailId);
		detail.setCreateTime(new Date());
		detail.setStatus(GovdocEnum.ExchangeDetailStatus.waitSend.getKey());
		Map<String,Object> extAttr = GovdocUtil.getExtProperty(detail);
		extAttr.put("identifier", data.getIdentifier());
		GovdocUtil.setExtProperty(detail, extAttr);
		this.updateDetail(detail);
		GovdocExchangeMain govdocExchangeMain = getGovdocExchangeMainById(detail.getMainId());
		int type = govdocExchangeMain.getType() == GovdocExchangeMain.EXCHANGE_TYPE_LIANHE ? GovdocExchangeMain.EXCHANGE_TYPE_LIANHE
				: GovdocExchangeMain.EXCHANGE_TYPE_JIAOHUAN;
		if(type != GovdocExchangeMain.EXCHANGE_TYPE_LIANHE){
			this.saveDetailLog(detailLog);
		}

		//过滤OCIP单位与本系统单位
		ocipOrgFilter(data);
		ExchangeService.sendObject(data);
	}
	
	//补发
	@Override
	public String exchangeReissue(Long summaryId,String orgStr) throws BusinessException{
		try {
			EdocSummary edocSummary = govdocSummaryManager.getSummaryById(summaryId);
			edocSummary.setSendToId(orgStr);
			edocSummary.setCopyToId(null);
			edocSummary.setReportToId(null);
			this.exchangeSend(edocSummary, AppContext.currentUserId(), null, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error(e);
			//e.printStackTrace();
		}
		return "";
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initFormData(OFCEdocObject object,SeeyonEdoc seeyonEdoc,EdocSummary edocSummary) {
		try{
			Class clazz = seeyonEdoc.getClass();
			FormDataMasterBean formDataMasterBean = formApi4Cap3.findDataById(edocSummary.getFormRecordid(), edocSummary.getFormAppid(), null);
			FormBean formBean = formApi4Cap3.getForm(edocSummary.getFormAppid());
			List<FormFieldBean> formFieldBeans = formBean.getAllFieldBeans();
			Map<String, Object> DataMap = formDataMasterBean.getAllDataMap();
			for (FormFieldBean formFieldBean : formFieldBeans) {
				Method method = null;
				Class elementType = null;
				String elementName = formFieldBean.getMappingField();
				if (Strings.isBlank(elementName)) {
					continue;
				}
				try {
					elementType = clazz.getDeclaredField(GovdocUtil.replaceUnderlineAndfirstToUpper(elementName,"_")).getType();
					method = clazz.getMethod("set" + GovdocUtil.replaceUnderlineAndfirstToUpper("_" + elementName,"_"),new Class[]{elementType});
				} catch (NoSuchMethodException e) {
					continue;
				} catch (NoSuchFieldException e) {
					continue;
				}
				if("urgent_level".equals(formFieldBean.getMappingField())){
					Object[] displayObj = getDisplayByName(DataMap,formFieldBean);
					object.setUrgentLevel(displayObj == null ? "" : String.valueOf(displayObj[1]));
				}
				if("secret_level".equals(formFieldBean.getMappingField())){
					Object[] displayObj = getDisplayByName(DataMap,formFieldBean);
					object.setSecretLevel(displayObj == null ? "" : String.valueOf(displayObj[1]));
				}
				if (elementType.equals(String.class)) {
					Object[] displayObj = getDisplayByName(DataMap,formFieldBean);
					if(displayObj != null){
						method.invoke(seeyonEdoc, displayObj[1]);
					}
				}else if (elementType.equals(List.class)) {
					String value = (String)DataMap.get(formFieldBean.getName());
					if(null!=value){
						Object[] displayValue = formFieldBean.getDisplayValue(value);
						if((String)displayValue[2]!=null){
							List<Organization> orgList = OcipEdocUtil.convertStringToOrganization(organizationManager,(String)displayValue[2]);
							if(CollectionUtils.isNotEmpty(orgList)){
								method.invoke(seeyonEdoc, orgList);
							}
						}
					}
				}else if (elementType.equals(PropertyValue.class)) {
					PropertyValue docMark = new PropertyValue();
					Object value = DataMap.get(formFieldBean.getName());
					if(null!=value){
						Object[] displayValue = formFieldBean.getDisplayValue(value);
						docMark.setDisplay(displayValue[1]==null?"":(String)displayValue[1]);
					}
					docMark.setValue((String)value);
					method.invoke(seeyonEdoc, docMark);
				}else if (elementType.equals(Organization.class)) {
					String value = (String)DataMap.get(formFieldBean.getName());
					if(null!=value){
						Object[] displayValue = formFieldBean.getDisplayValue(value);
						if((String)displayValue[2]!=null){
							List<Organization> orgList = OcipEdocUtil.convertStringToOrganization(organizationManager,(String)displayValue[2]);
							if(CollectionUtils.isNotEmpty(orgList)){
								method.invoke(seeyonEdoc, orgList.get(0));
							}
						}
					}
				}
			}
		}catch (Exception e){
			LOGGER.error("公文交换初始化form表单数据时出错",e);
		}
	}
	private Object[] getDisplayByName(Map<String,Object> datamap,FormFieldBean formFieldBean){
		Object[] displayObj = null;
		Object value = datamap.get(formFieldBean.getName());
		try {
			displayObj = formFieldBean.getDisplayValue(value);
		} catch (NumberFormatException e) {
			LOGGER.error(e);
			//e.printStackTrace();
		} catch (BusinessException e) {
			LOGGER.error(e);
			//e.printStackTrace();
		}
		return displayObj;
	}
	
	//签收
	@SuppressWarnings("unchecked")
	@Override
	public void exchangeSign(GovdocBaseVO baseVo) throws BusinessException {
		EdocSummary summary = baseVo.getSummary();
		CtpAffair currentAffair = baseVo.getAffair();
		Long currentUserId = baseVo.getCurrentUser().getId();
		GovdocExchangeDetail govdocExchangeDetail = this.findDetailBySummaryId(summary.getId());
		if(govdocExchangeDetail != null && govdocExchangeDetail.getStatus()==ExchangeDetailStatus.waitSign.getKey()){//如果是待签收数据
			//设置为已签收
    		govdocExchangeDetail.setSignAffairId(currentAffair.getId());
			govdocExchangeDetail.setRecUserName(orgManager.getMemberById(currentUserId).getName());
			govdocExchangeDetail.setRecUserId(currentUserId);
			govdocExchangeDetail.setRecTime(new Date());
			govdocExchangeDetail.setStatus(ExchangeDetailStatus.hasSign.getKey());	
			govdocExchangeDetail.setNodeInfo(currentAffair.getActivityId());
			Long formId = summary.getFormAppid();
			FormDataMasterBean formDataMasterBean = null;
			if(formId!=null){
				try {
					formDataMasterBean = formApi4Cap3.findDataById(summary.getFormRecordid(), summary.getFormAppid(), null);
					FormBean fb = govdocFormManager.getForm(formId);
					List<FormFieldBean> fs = fb.getAllFieldBeans();
					for (FormFieldBean formFieldBean : fs) {
						if (formFieldBean.isMasterField()) {
							if (formFieldBean.getMappingField() != null) {
								if ("sign_mark".equals(formFieldBean.getMappingField())) {// 公文文号
									Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
									if (value == null || Strings.isBlank(value.toString())) {
										summary.setDocMark2(null);
										continue;
									}
									String[] marks = value.toString().split("[|]");
									if(marks.length == 1) {
										summary.setDocMark2(marks[0]);
									} else if(marks.length > 1) {
										summary.setDocMark2(marks[1]);
									}
									if(baseVo.getSignMarkVo() != null) {
										baseVo.getSignMarkVo().setIsMapping(true);
									}
									break;
								} 
							} else {
								//如果是签收编号
								if("edocSignMark".equals(formFieldBean.getInputType())){
									Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
									if (null != value) {
										String[] marks = value.toString().split("[|]");
										if(marks.length == 1) {
											summary.setDocMark2(marks[0]);
										} else if(marks.length > 1) {
											summary.setDocMark2(marks[1]);
										}
										break;
									}
									if(baseVo.getSignMarkVo() != null) {
										baseVo.getSignMarkVo().setIsMapping(false);
									}
								}
							}
						}
					}
				} catch (SQLException e) {
					LOGGER.error("签收操作获取签收编号出错：" + summary.getSubject(),e);
				}
			}
			// 填写签收编号
			govdocExchangeDetail.setRecNo(summary.getDocMark2());
			if(baseVo.getSignMarkVo() != null) {
				baseVo.getSignMarkVo().setSubject(summary.getSubject());
				baseVo.getSignMarkVo().setSummaryId(summary.getId());
				baseVo.getSignMarkVo().setDomainId(summary.getOrgAccountId());
				baseVo.getSignMarkVo().setFormDataId(formDataMasterBean.getId());
				baseVo.getSignMarkVo().setGovdocType(summary.getGovdocType());
			}
			govdocMarkManager.saveDealMark(baseVo);
			
			//记录日志
			GovdocExchangeDetailLog govdocExchangeDetailLog = new GovdocExchangeDetailLog();
			govdocExchangeDetailLog.setIdIfNew();
			govdocExchangeDetailLog.setDetailId(govdocExchangeDetail.getId());
			govdocExchangeDetailLog.setStatus(ExchangeDetailStatus.hasSign.getKey());
			govdocExchangeDetailLog.setDescription("已签收，进行中");
			govdocExchangeDetailLog.setUserName(govdocExchangeDetail.getRecUserName());
			Date date = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			govdocExchangeDetailLog.setTime(date);
			govdocExchangeDetailLog.setTimeMS(calendar.get(Calendar.MILLISECOND));
			this.updateDetail(govdocExchangeDetail);
			this.saveDetailLog(govdocExchangeDetailLog);
			govdocPubManager.updateEdocSummaryTransferStatus(govdocExchangeDetail.getSummaryId(), TransferStatus.receiveSigned);
			
			/** OCIP 1.0-公文交换开始*/
//			if (AppContext.hasPlugin("ocip")) {
//				try {
//					String commentStr = "";
//					try {
//						Comment comment = (Comment) ParamUtil.mapToBean(ParamUtil.getJsonDomain("comment_deal"), new Comment(), false);
//						Map parm = ParamUtil.getJsonDomain("comment_deal");
//						if(parm.get("content_coll")!= null){
//							String content_coll=(String)parm.get("content_coll");
//							comment.setContent(content_coll);
//				        }
//						commentStr = comment.getContent();
//					} catch (Exception e) {
//					}
//					IOCIPExchangeEdocManager m = (IOCIPExchangeEdocManager) AppContext.getBean("ocipExchangeEdocManager");
//					m.receiveEdoc(govdocExchangeDetail.getMainId(), govdocExchangeDetail.getId(), commentStr);
//				} catch (Exception e) {
//					LOGGER.error("公文交接至OCIP封包出错", e);
//				}
//			}
			/** OCIP 1.0-公文交换开始 */
		}
	}
	
	//签收
	@SuppressWarnings("unchecked")
	@Override
	public void exchangeSign(EdocSummary summary,CtpAffair currentAffair,Long currentUserId) throws BusinessException {
		GovdocExchangeDetail govdocExchangeDetail = this.findDetailBySummaryId(summary.getId());
		if(govdocExchangeDetail != null && govdocExchangeDetail.getStatus()==ExchangeDetailStatus.waitSign.getKey()){//如果是待签收数据
			//设置为已签收
    		govdocExchangeDetail.setSignAffairId(currentAffair.getId());
			govdocExchangeDetail.setRecUserName(orgManager.getMemberById(currentUserId).getName());
			govdocExchangeDetail.setRecUserId(currentUserId);
			govdocExchangeDetail.setRecTime(new Date());
			govdocExchangeDetail.setStatus(ExchangeDetailStatus.hasSign.getKey());	
			govdocExchangeDetail.setNodeInfo(currentAffair.getActivityId());
			Long formId = summary.getFormAppid();
			FormDataMasterBean formDataMasterBean = null;
			if(formId!=null){
				try {
					formDataMasterBean = formApi4Cap3.findDataById(summary.getFormRecordid(), summary.getFormAppid(), null);
					FormBean fb = govdocFormManager.getForm(formId);
					List<FormFieldBean> fs = fb.getAllFieldBeans();
					for (FormFieldBean formFieldBean : fs) {
						//如果是签收编号
						if("edocSignMark".equals(formFieldBean.getInputType())){
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (null != value) {
								String recNo = formFieldBean.getDisplayValue(value) == null ? "" : formFieldBean.getDisplayValue(value)[1].toString();
								govdocExchangeDetail.setRecNo(recNo);
								break;
							}
						}
					}
				} catch (SQLException e) {
					LOGGER.error("签收操作获取签收编号出错：" + summary.getSubject(),e);
				}
			}
			//记录日志
			GovdocExchangeDetailLog govdocExchangeDetailLog = new GovdocExchangeDetailLog();
			govdocExchangeDetailLog.setIdIfNew();
			govdocExchangeDetailLog.setDetailId(govdocExchangeDetail.getId());
			govdocExchangeDetailLog.setStatus(ExchangeDetailStatus.hasSign.getKey());
			govdocExchangeDetailLog.setDescription("已签收，进行中");
			govdocExchangeDetailLog.setUserName(govdocExchangeDetail.getRecUserName());
			Date date = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			govdocExchangeDetailLog.setTime(date);
			govdocExchangeDetailLog.setTimeMS(calendar.get(Calendar.MILLISECOND));
			this.updateDetail(govdocExchangeDetail);
			this.saveDetailLog(govdocExchangeDetailLog);
			govdocPubManager.updateEdocSummaryTransferStatus(govdocExchangeDetail.getSummaryId(), TransferStatus.receiveSigned);
			
			/** OCIP 1.0-公文交换开始*/
//			if (AppContext.hasPlugin("ocip")) {
//				try {
//					String commentStr = "";
//					try {
//						Comment comment = (Comment) ParamUtil.mapToBean(ParamUtil.getJsonDomain("comment_deal"), new Comment(), false);
//						Map parm = ParamUtil.getJsonDomain("comment_deal");
//						if(parm.get("content_coll")!= null){
//							String content_coll=(String)parm.get("content_coll");
//							comment.setContent(content_coll);
//				        }
//						commentStr = comment.getContent();
//					} catch (Exception e) {
//					}
//					IOCIPExchangeEdocManager m = (IOCIPExchangeEdocManager) AppContext.getBean("ocipExchangeEdocManager");
//					m.receiveEdoc(govdocExchangeDetail.getMainId(), govdocExchangeDetail.getId(), commentStr);
//				} catch (Exception e) {
//					LOGGER.error("公文交接至OCIP封包出错", e);
//				}
//			}
			/** OCIP 1.0-公文交换开始 */
		}
	}
	
	//分办
	@Override
	public void exchangeDistribute(EdocSummary exchangeSummary,CtpAffair senderAffair,CtpAffair distributeAffair,Long currentUserId, Long recSummaryId) throws BusinessException {
		GovdocExchangeDetail govdocExchangeDetail = this.findDetailBySummaryId(exchangeSummary.getId());
		Date now = new Date();
		if (govdocExchangeDetail != null) {
			if (govdocExchangeDetail.getStatus() == ExchangeDetailStatus.waitSign.getKey()
					&& Strings.isBlank(govdocExchangeDetail.getRecUserName())) {
				govdocExchangeDetail.setSignAffairId(distributeAffair.getId());
				govdocExchangeDetail.setRecUserName(orgManager.getMemberById(currentUserId).getName());
				govdocExchangeDetail.setRecTime(now);
				govdocExchangeDetail.setNodeInfo(distributeAffair.getActivityId());
				Long formId = exchangeSummary.getFormAppid();
				FormDataMasterBean formDataMasterBean = null;
				if (formId != null) {
					try {
						formDataMasterBean = formApi4Cap3.findDataById(exchangeSummary.getFormRecordid(), exchangeSummary.getFormAppid(),
								null);
						FormBean fb = govdocFormManager.getForm(formId);
						List<FormFieldBean> fs = fb.getAllFieldBeans();
						for (FormFieldBean formFieldBean : fs) {
							//如果是签收编号
							if ("edocSignMark".equals(formFieldBean.getInputType())) {
								Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
								if (null != value) {
									String recNo = formFieldBean.getDisplayValue(value) == null ? ""
											: formFieldBean.getDisplayValue(value)[1].toString();
									govdocExchangeDetail.setRecNo(recNo);
									break;
								}
							}
						}
					} catch (SQLException e) {
						LOGGER.error("签收操作获取签收编号出错：" + exchangeSummary.getSubject(), e);
					}
				}
			}
			GovdocExchangeDetailLog govdocExchangeDetailLog = new GovdocExchangeDetailLog();
			govdocExchangeDetail.setRecSummaryId(recSummaryId);
			if (senderAffair != null && senderAffair.getState() == StateEnum.col_waitSend.getKey()) {
				govdocExchangeDetail.setStatus(ExchangeDetailStatus.draftFenBan.getKey());
				govdocExchangeDetailLog.setNewId();
				govdocExchangeDetailLog.setDetailId(govdocExchangeDetail.getId());
				govdocExchangeDetailLog.setStatus(ExchangeDetailStatus.draftFenBan.getKey());
				govdocExchangeDetailLog.setDescription("分办时保存待发操作");
				govdocExchangeDetailLog.setUserName(orgManager.getMemberById(currentUserId).getName());
			}else{
				govdocExchangeDetail.setStatus(ExchangeDetailStatus.hasFenBan.getKey());
				govdocExchangeDetailLog.setNewId();
				govdocExchangeDetailLog.setDetailId(govdocExchangeDetail.getId());
				govdocExchangeDetailLog.setStatus(ExchangeDetailStatus.hasFenBan.getKey());
				govdocExchangeDetailLog.setDescription("进行分办操作");
				govdocExchangeDetailLog.setUserName(orgManager.getMemberById(currentUserId).getName());
				govdocPubManager.updateEdocSummaryTransferStatus(govdocExchangeDetail.getSummaryId(), TransferStatus.receiveFenbanEletric);
			}
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(now);
			govdocExchangeDetailLog.setTime(now);
			govdocExchangeDetailLog.setTimeMS(calendar.get(Calendar.MILLISECOND));
			this.updateDetail(govdocExchangeDetail);
			this.saveDetailLog(govdocExchangeDetailLog);
		}
	}
	
	//撤销
	@Override
	public String exchangeCancel(Long detailId,String repealComment) throws BusinessException {
        try{
            GovdocExchangeDetail detail = this.getExchangeDetailById(detailId);
        	if (detail != null && detail.getStatus() != ExchangeDetailStatus.hasBack.getKey()) {
        		detail.setStatus(GovdocEnum.ExchangeDetailStatus.hasCancel.getKey());
        		this.updateDetail(detail);
        		//记录日志
        		GovdocExchangeDetailLog govdocExchangeDetailLog = new GovdocExchangeDetailLog();
        		govdocExchangeDetailLog.setBackOpinion(repealComment);
        		govdocExchangeDetailLog.setDetailId(detail.getId());
        		govdocExchangeDetailLog.setNewId();
        		govdocExchangeDetailLog.setUserName(AppContext.currentUserName());
        		govdocExchangeDetailLog.setTime(new Date());
        		govdocExchangeDetailLog.setDescription("进行了撤销操作，交换状态为已撤销");
        		govdocExchangeDetailLog.setStatus(ExchangeDetailStatus.hasCancel.getKey());
        		this.saveDetailLog(govdocExchangeDetailLog);
        	}
        	govdocRegisterManager.saveByDetail(detail);
        }catch (Exception e) {
            LOGGER.error("撤销操作出现错误",e);
            throw new BusinessException("撤销操作出现错误",e);
        } finally {
        }
        return null;
	}
	
	//收文回退导致撤销
	@Override
	public String recSummaryCancelUpdateExchangeStatus(Long detailId, String repealComment)  throws BusinessException{
		try{
            GovdocExchangeDetail detail = this.getExchangeDetailById(detailId);
        	if (detail != null && detail.getStatus() == ExchangeDetailStatus.hasFenBan.getKey()) {
        		detail.setStatus(GovdocEnum.ExchangeDetailStatus.draftFenBan.getKey());
    			detail.setOpinion(repealComment);
        		this.updateDetail(detail);
        		//记录日志
        		GovdocExchangeDetailLog govdocExchangeDetailLog = new GovdocExchangeDetailLog();
        		govdocExchangeDetailLog.setBackOpinion(repealComment);
        		govdocExchangeDetailLog.setDetailId(detail.getId());
        		govdocExchangeDetailLog.setNewId();
        		govdocExchangeDetailLog.setUserName(AppContext.currentUserName());
        		govdocExchangeDetailLog.setTime(new Date());
        		govdocExchangeDetailLog.setDescription("收文进行了撤销操作，交换状态为分办待发");
        		govdocExchangeDetailLog.setStatus(ExchangeDetailStatus.draftFenBan.getKey());
        		this.saveDetailLog(govdocExchangeDetailLog);
        	}
        	govdocRegisterManager.saveByDetail(detail);
        }catch (Exception e) {
            LOGGER.error("撤销操作出现错误",e);
            throw new BusinessException("撤销操作出现错误",e);
        } finally {
        }
        return null;
	}

	//回退
	@Override
	public String exchangeReturn(Long summaryId,CtpAffair cuurentAffair,String repealComment) throws BusinessException {
        try{
        	CtpAffair signAffair = null;
        	if(CollectionUtils.isNotEmpty(DateSharedWithWorkflowEngineThreadLocal.getWorkflowAssignedAllAffairs())){
        		signAffair = DateSharedWithWorkflowEngineThreadLocal.getWorkflowAssignedAllAffairs().get(0);
        	}
            GovdocExchangeDetail govdocExchangeDetail = this.findDetailBySummaryId(summaryId);
            if (govdocExchangeDetail != null) {
				if (signAffair == null) {
					//状态改为待已回退
    				govdocExchangeDetail.setStatus(ExchangeDetailStatus.hasBack.getKey());
    				GovdocExchangeMain main = getGovdocExchangeMainById(govdocExchangeDetail.getMainId());
    				
    				if(main.getType() != GovdocExchangeMain.EXCHANGE_TYPE_LIANHE){
    					govdocExchangeDetail.setSummaryId(null);
    				}
    				govdocExchangeDetail.setRecNo(null);
    				govdocExchangeDetail.setRecUserName(null);
    				govdocExchangeDetail.setRecUserId(null);
    				govdocExchangeDetail.setRecTime(null);
    				govdocExchangeDetail.setNodeInfo(null);
    				govdocExchangeDetail.setSignAffairId(null);
    				this.updateDetail(govdocExchangeDetail);
    				//记录日志
        			GovdocExchangeDetailLog govdocExchangeDetailLog = new GovdocExchangeDetailLog();
    				govdocExchangeDetailLog.setDetailId(govdocExchangeDetail.getId());
    				govdocExchangeDetailLog.setNewId();
    				govdocExchangeDetailLog.setUserName(orgManager.getMemberById(cuurentAffair.getMemberId()).getName());
    				govdocExchangeDetailLog.setTime(new Date());
    				govdocExchangeDetailLog.setDescription("进行了回退操作，交换状态为已回退");
    				govdocExchangeDetailLog.setStatus(ExchangeDetailStatus.hasBack.getKey());
    				govdocExchangeDetailLog.setBackOpinion(repealComment);
    				govdocExchangeDetailLog.setBackSummaryId(summaryId);
    				this.saveDetailLog(govdocExchangeDetailLog);
    				Map<String, Object> columns = new HashMap<String, Object>();
    				columns.put("transferStatus", TransferStatus.stepbackWaitSend.getKey());
    				columns.put("completeTime", new java.sql.Timestamp(System.currentTimeMillis()));
    				govdocSummaryManager.update(summaryId, columns);
//    				if(AppContext.hasPlugin("ocip")){
//    					V3xOrgEntity entity = orgManager.getEntityOnlyById(main.getStartUserId());
//    					if(null != entity && OrgUtil.isPlatformEntity(entity))
//    						ocipExchangeEdocManager.stepbackEdoc(govdocExchangeDetail.getMainId(), govdocExchangeDetail.getId(), repealComment);
//    				}
    				DateSharedWithWorkflowEngineThreadLocal.addAffairDistributeMap(main.getStartUserId(), main.getAffairId());
    				//联合发文的协办流程撤销后,不能在待发里面
					if(main.getType() == GovdocExchangeMain.EXCHANGE_TYPE_LIANHE){
						List<CtpAffair> affairs = affairManager.getAffairs(summaryId, StateEnum.col_waitSend);
						for (CtpAffair ctpAffair : affairs) {
							ctpAffair.setDelete(true);
						}
						affairManager.updateAffairs(affairs);
					}
				} else if (govdocExchangeDetail.getStatus() == GovdocEnum.ExchangeDetailStatus.hasSign.getKey() 
						&& signAffair.getActivityId().longValue() == govdocExchangeDetail.getNodeInfo().longValue()) {
					//状态改为待签收
    				govdocExchangeDetail.setStatus(ExchangeDetailStatus.waitSign.getKey());
    				govdocExchangeDetail.setRecNo(null);
    				govdocExchangeDetail.setRecUserName(null);
    				govdocExchangeDetail.setRecUserId(null);
    				govdocExchangeDetail.setRecTime(null);
    				govdocExchangeDetail.setNodeInfo(null);
    				govdocExchangeDetail.setSignAffairId(null);
    				this.updateDetail(govdocExchangeDetail);
    				//记录日志
        			GovdocExchangeDetailLog govdocExchangeDetailLog = new GovdocExchangeDetailLog();
    				govdocExchangeDetailLog.setDetailId(govdocExchangeDetail.getId());
    				govdocExchangeDetailLog.setNewId();
    				govdocExchangeDetailLog.setUserName(orgManager.getMemberById(cuurentAffair.getMemberId()).getName());
    				govdocExchangeDetailLog.setTime(new Date());
    				govdocExchangeDetailLog.setDescription("进行了回退操作，交换状态为待签收");
    				govdocExchangeDetailLog.setStatus(ExchangeDetailStatus.waitSign.getKey());
    				govdocExchangeDetailLog.setBackOpinion(repealComment);
    				this.saveDetailLog(govdocExchangeDetailLog);
    				Map<String, Object> columns = new HashMap<String, Object>();
    				columns.put("transferStatus", TransferStatus.waitSigned.getKey());
    				govdocSummaryManager.update(summaryId, columns);
//    				if(AppContext.hasPlugin("ocip")){
//    					GovdocExchangeMain main = getGovdocExchangeMainById(govdocExchangeDetail.getMainId());
//    					V3xOrgEntity entity = orgManager.getEntityOnlyById(main.getStartUserId());
//    					if(null != entity && OrgUtil.isPlatformEntity(entity))
//    						ocipExchangeEdocManager.signedAndStepback( govdocExchangeDetail.getId(), repealComment);
//    				}
				} else if(govdocExchangeDetail.getStatus() == GovdocEnum.ExchangeDetailStatus.hasFenBan.getKey()){
					//状态改为已签收
    				govdocExchangeDetail.setStatus(ExchangeDetailStatus.hasSign.getKey());
    				this.updateDetail(govdocExchangeDetail);
    				//记录日志
        			GovdocExchangeDetailLog govdocExchangeDetailLog = new GovdocExchangeDetailLog();
    				govdocExchangeDetailLog.setDetailId(govdocExchangeDetail.getId());
    				govdocExchangeDetailLog.setNewId();
    				govdocExchangeDetailLog.setUserName(orgManager.getMemberById(cuurentAffair.getMemberId()).getName());
    				govdocExchangeDetailLog.setTime(new Date());
    				govdocExchangeDetailLog.setDescription("进行了回退操作，交换状态为已签收");
    				govdocExchangeDetailLog.setStatus(ExchangeDetailStatus.waitSign.getKey());
    				govdocExchangeDetailLog.setBackOpinion(repealComment);
    				this.saveDetailLog(govdocExchangeDetailLog);
    				Map<String, Object> columns = new HashMap<String, Object>();
    				columns.put("transferStatus", TransferStatus.receiveSigned.getKey());
    				govdocSummaryManager.update(summaryId, columns);
				}
			}else{
				govdocExchangeDetail = this.findDetailByRecSummaryId(summaryId);
				if (govdocExchangeDetail != null) {
					if (govdocExchangeDetail.getStatus() == GovdocEnum.ExchangeDetailStatus.hasFenBan.getKey()) {
						//状态改为分办待发
	    				govdocExchangeDetail.setStatus(ExchangeDetailStatus.draftFenBan.getKey());
	    				this.updateDetail(govdocExchangeDetail);
	    				//记录日志
	        			GovdocExchangeDetailLog govdocExchangeDetailLog = new GovdocExchangeDetailLog();
	    				govdocExchangeDetailLog.setDetailId(govdocExchangeDetail.getId());
	    				govdocExchangeDetailLog.setNewId();
	    				govdocExchangeDetailLog.setUserName(orgManager.getMemberById(cuurentAffair.getMemberId()).getName());
	    				govdocExchangeDetailLog.setTime(new Date());
	    				govdocExchangeDetailLog.setDescription("收文进行了回退操作，交换状态为分办待发");
	    				govdocExchangeDetailLog.setStatus(ExchangeDetailStatus.draftFenBan.getKey());
	    				govdocExchangeDetailLog.setBackOpinion(repealComment);
	    				govdocExchangeDetailLog.setBackSummaryId(summaryId);
	    				this.saveDetailLog(govdocExchangeDetailLog);
					}
				}
			}
        }catch (Exception e) {
            LOGGER.error("回退操作出现错误",e);
            throw new BusinessException("回退操作出现错误",e);
        } finally {
        }
        return null;
	}
	
	//指定回退
	@Override
	public void exchangeTakeBack(Long summaryId, CtpAffair currentAffair, boolean stepBackDistribute) throws BusinessException {
		GovdocExchangeDetail govdocExchangeDetail = this.findDetailBySummaryId(summaryId);
		if (govdocExchangeDetail != null) {
			govdocExchangeDetail.setStatus(ExchangeDetailStatus.hasSign.getKey());
			govdocExchangeDetail.setRecSummaryId(null);
			GovdocExchangeDetailLog govdocExchangeDetailLog = new GovdocExchangeDetailLog();
			govdocExchangeDetailLog.setDetailId(govdocExchangeDetail.getId());
			govdocExchangeDetailLog.setNewId();
			govdocExchangeDetailLog.setUserName(AppContext.getCurrentUser().getName());
			govdocExchangeDetailLog.setTime(new Date());
			govdocExchangeDetailLog.setDescription("进行了回退操作，交换状态为已签收");
			govdocExchangeDetailLog.setStatus(ExchangeDetailStatus.hasSign.getKey());
			Long nodeInfo = currentAffair.getActivityId();
			
			Map<String, Object> columnValue = new HashMap<String, Object>();
			columnValue.put("transferStatus", GovdocEnum.TransferStatus.receiveSigned.getKey());
			if (nodeInfo.longValue() == govdocExchangeDetail.getNodeInfo().longValue()) {
				// 状态改为待签收
				govdocExchangeDetail.setStatus(ExchangeDetailStatus.waitSign.getKey());
				govdocExchangeDetail.setRecUserName(null);
				govdocExchangeDetail.setRecUserId(null);
				govdocExchangeDetail.setRecTime(null);
				govdocExchangeDetail.setNodeInfo(null);
				govdocExchangeDetail.setRecNo(null);
				// 记录日志
				if(!stepBackDistribute){
					govdocExchangeDetailLog.setDescription("进行了取回操作，交换状态为待签收");
				}else{
					govdocExchangeDetailLog.setDescription("进行了回退操作，交换状态为待签收");
				}
				govdocExchangeDetailLog.setStatus(ExchangeDetailStatus.waitSign.getKey());
				columnValue.put("transferStatus", GovdocEnum.TransferStatus.waitSigned.getKey());
			}
			this.saveDetailLog(govdocExchangeDetailLog);
			this.updateDetail(govdocExchangeDetail);
			
			govdocSummaryManager.updateEdocSummary(govdocExchangeDetail.getSummaryId(), columnValue);
		}
	}

	//终止
	@SuppressWarnings("rawtypes")
	public void GovdocExchangeStop(GovdocExchangeDetail govdocExchangeDetail) throws BusinessException{
		GovdocExchangeDetailLog govdocExchangeDetailLog = new GovdocExchangeDetailLog();
		govdocExchangeDetailLog.setDetailId(govdocExchangeDetail.getId());
		govdocExchangeDetailLog.setId(UUIDLong.longUUID());
		govdocExchangeDetailLog.setUserName(AppContext.getCurrentUser().getName());
		govdocExchangeDetailLog.setTime(new Date());
		Map parm = ParamUtil.getJsonDomain("comment_deal");
		String content_coll = parm.get("content_coll")==null?"":(String)parm.get("content_coll");
		govdocExchangeDetailLog.setBackOpinion(content_coll);
		//状态改为已回退
		govdocExchangeDetail.setStatus(ExchangeDetailStatus.hasStop.getKey());
		//记录日志
		govdocExchangeDetailLog.setDescription("进行了终止操作，该流程已终止");
		govdocExchangeDetailLog.setStatus(ExchangeDetailStatus.hasStop.getKey());

		this.saveDetailLog(govdocExchangeDetailLog);
		this.updateDetail(govdocExchangeDetail);
	}
	
	/**
	 * 催办
	 */
	@Override
	public String press(Map<String, String> params) throws BusinessException {
		String reStr = "";
    	String detailIds = params.get("detailIds");
    	String content = params.get("content");
    	try{
    		List<GovdocExchangeDetail> details = new ArrayList<GovdocExchangeDetail>();
	    	String[] dIds = detailIds.split(",");
	    	if (dIds.length > 0) {
				for (int i = 0; i < dIds.length; i++) {
					 GovdocExchangeDetail govdocExchangeDetail = this.getExchangeDetailById(Long.parseLong(dIds[i]));
					 Long summaryId = govdocExchangeDetail.getSummaryId();
					 if(govdocExchangeDetail.getSummaryId() != null ){
						 if(govdocExchangeDetail.getStatus() == GovdocEnum.ExchangeDetailStatus.waitSign.getKey()){
								 dealPressInner(summaryId,content);
						 }
					 }else{
						 if(govdocExchangeDetail.getStatus() == GovdocEnum.ExchangeDetailStatus.waitSign.getKey()){
//							 if(AppContext.hasPlugin("ocip")) {
//								V3xOrgEntity entity = orgManager.getEntity(getTypeAndIds(govdocExchangeDetail.getRecOrgType(), govdocExchangeDetail.getRecOrgId()));
//								if (OrgUtil.isPlatformEntity(entity)){ //跨平台催办
//	                                //this.pressExchangeSystem(main.getSubject(), govdocExchangeDetail, entity,content);
//									ocipExchangeEdocManager.press(govdocExchangeDetail.getMainId(),govdocExchangeDetail.getId(),content);
//								}
//							 }
						 }
					 }
					 //记录本地detailLog记录催办信息
					 govdocExchangeDetail.setCuibanNum(govdocExchangeDetail.getCuibanNum()+1);
					 details.add(govdocExchangeDetail);
					 initPressDetailLog(Long.parseLong(dIds[i]));
				}
				this.updateDetailList(details);
			}
    	}catch(Exception e) {
    		reStr = "催办数据失败";
    		LOGGER.error("催办数据失败", e);
    	}
		return reStr;
	}
	/**
	 * 跨系统催办
	 * @param subject
	 * @param govdocExchangeDetail
	 * @param entity
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private void pressExchangeSystem(String subject,GovdocExchangeDetail govdocExchangeDetail,V3xOrgEntity entity,String content) throws Exception{	
		List<Organization> recivers = new ArrayList<Organization>();		
		String localSystemCode = Global.getConfig("sysCode");
		//接收数据者
		Address reciver = new Address();
		reciver = OrgUtil.getAddress(entity);
		recivers.add(Address.addressToOrgnation(reciver));
		//发送数据的人
		V3xOrgMember member = orgManager.getMemberById(AppContext.currentUserId());		
		Address sender = new Address();
		sender.setName(member.getName());
		sender.setResource(localSystemCode);
		sender.setId(member.getId().toString());
		sender.setType(AddressType.member.name());
		Organization sendOrg = Address.addressToOrgnation(sender);
		
		BIZExchangeData data = new BIZExchangeData();
		data.setIdentifier(UUID.randomUUID().toString().replaceAll("-", ""));
		data.setRecivers(recivers);
		Address source = new Address();
		source.setId(localSystemCode);
		source.setName(localSystemCode);
		data.setSource(source);
		data.setSender(sendOrg);
		data.setSubject(subject);
		//交换信息
		BIZMessage bizMessage = new BIZMessage();
		bizMessage.setContentType(BIZContentType.RET);
		RETEdocObject object = new RETEdocObject();
		object.setOperation(EdocOperation.HASTEN);
		object.setDatetime(new Date());
		object.setDetailId(String.valueOf(govdocExchangeDetail.getId()));
		object.setRecNo(govdocExchangeDetail.getRecNo());
		object.setOpinion(content);//催办内容
		bizMessage.setContent(object);
		data.setBussnissMessage(bizMessage);
		ExchangeService.sendObject(data);
	}
	/**
	 * 催办日志
	 * @param detailId
	 */
	private void initPressDetailLog(long detailId){
		GovdocExchangeDetailLog detailLog = new GovdocExchangeDetailLog();
		detailLog.setNewId();
		detailLog.setDetailId(detailId);
		detailLog.setUserName(AppContext.currentUserName());
		detailLog.setStatus(GovdocEnum.ExchangeDetailStatus.waitSign.getKey());
		detailLog.setDescription("已催办接收单位进行处理");
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		detailLog.setTime(date);
		detailLog.setTimeMS(calendar.get(Calendar.MILLISECOND));
		this.saveDetailLog(detailLog);
	}
	/**
	 * 催办处理
	 * @param summaryId
	 * @throws Exception
	 */
	private void dealPressInner(long summaryId,String content) throws Exception{
		 //查出所有的待办进行催办信息的发送
		 List<CtpAffair> affairList = affairManager.getAffairs(summaryId,StateEnum.col_pending);
		 EdocSummary summary = govdocSummaryManager.getSummaryById(summaryId);
         for(CtpAffair affair : affairList){
        	 if(affair.getState() == StateEnum.col_pending.getKey()){
        		 List<String> personIds = new ArrayList<String>();
        		 personIds.add(String.valueOf(affair.getMemberId()));
        		 wapi.hasten(String.valueOf(summary.getProcessId()), String.valueOf(affair.getActivityId()), "edoc", personIds, null, content, false);
        	 }
         }

	}
	/*************************** 444444 公文交换操作    end ******************************/
	
	
	/*************************** 555555 公文转办相关  start ******************************/
	@SuppressWarnings("unchecked")
	@AjaxAccess
	public FlipInfo findListByPage(FlipInfo flipInfo, Map<String, String> query) throws BusinessException{
		FlipInfo fi = this.govdocExchangeDao.findListByPage(flipInfo,query);
		List<GovdocExchangeMain> list = fi.getData();
		for(GovdocExchangeMain gr : list){
			EdocSummary summary = govdocSummaryManager.getSummaryById(gr.getSummaryId());
			if(summary != null){
				//保存待发后，再次编辑发起，标题修改后获取summary的标题
				gr.setSubject(summary.getSubject());
				gr.setDocMark(summary.getDocMark() == null ? "" : summary.getDocMark());
			}else{
				gr.setDocMark("");
			}
			
		}
		return fi;
	}
	@Override
	public void saveTransferByExchange(Long summaryId, Long memberId) throws BusinessException {
		List<Integer> exchangeType = new ArrayList<Integer>();
		exchangeType.add(GovdocEnum.GovdocExchangeTypeEnum.lianhe.ordinal());
		exchangeType.add(GovdocEnum.GovdocExchangeTypeEnum.zhuansw.ordinal());
		exchangeType.add(GovdocEnum.GovdocExchangeTypeEnum.zhuanfw.ordinal());
		govdocExchangeDao.saveTransferByExchange(summaryId, memberId, exchangeType);
	}
	/*************************** 555555 公文转办相关   end ******************************/
	
	
	/*************************** 666666 联合发文相关 start ******************************/
	@Override
	public List<JointlyIssyedVO> findMainBySummaryId4Lianhe(long summaryId, boolean needSubject) throws BusinessException {
		List<JointlyIssyedVO> jList = new ArrayList<JointlyIssyedVO>();
		GovdocExchangeDetail lhDetail = findDetailBySummaryId(summaryId);
		Long mainId = null;
		GovdocExchangeMain main = null;
		if (lhDetail == null){//说明当前打开的是主办流程
			main = findBySummaryId(summaryId, GovdocExchangeMain.EXCHANGE_TYPE_LIANHE);
			mainId = main.getId();
			Map<String, String> conditionMap = new HashMap<String, String>();
			conditionMap.put("mainId", mainId.toString());
			List<GovdocExchangeDetail> details = govdocExchangeDao.findGovdocExchangeDetail(new FlipInfo(), conditionMap);
			for(GovdocExchangeDetail detail :details){
				JointlyIssyedVO v = new JointlyIssyedVO();
				try{
					if(detail.getSummaryId()!=null){
						v.setSummaryId(Long.valueOf(detail.getSummaryId()));
						List<StateEnum> states = new ArrayList<StateEnum>();
						states.add(StateEnum.col_sent);
						states.add(StateEnum.col_cancel);
						states.add(StateEnum.col_waitSend);
						List<CtpAffair> affairs = affairManager.getAffairs(detail.getSummaryId(), states);
						CtpAffair ctpAffair = affairs.get(0);
						v.setAffairId(ctpAffair.getId());
						v.setAffairApp(ctpAffair.getApp());
						if(Strings.isBlank(ctpAffair.getProcessId())){
							EdocSummary summary = govdocSummaryManager.getSummaryById(detail.getSummaryId());
							if(summary!=null){
								v.setProcessId(summary.getProcessId());
							}
						}else{
							v.setProcessId(ctpAffair.getProcessId());
						}
					}
					v.setExchangeDetailId(detail.getId());
					v.setSummary_unit(detail.getRecOrgName());
					v.setlDate(detail.getCreateTime()==null?"":DateUtil.format(detail.getCreateTime(), DateUtil.YMDHMS_PATTERN));
				}catch(Exception e){
					LOGGER.error("联合发文查询列表由异常", e);
				}
				v.setState(detail.getStatus());
				v.setSendFlow(true);
				v.setOrgType("Account");
				v.setOrgName(detail.getRecOrgName());
				v.setType_str("协办单位");
				jList.add(v);
			}
		}else {
			mainId = lhDetail.getMainId();
			main = getGovdocExchangeMainById(mainId);
			Map<String, String> conditionMap = new HashMap<String, String>();
			conditionMap.put("mainId", mainId.toString());
			List<GovdocExchangeDetail> details = govdocExchangeDao.findGovdocExchangeDetail(new FlipInfo(), conditionMap);
			JointlyIssyedVO v = new JointlyIssyedVO();
			v.setState(-1);
			v.setSummaryId(main.getSummaryId());
			v.setType_str("主办单位");
			v.setSendFlow(false);
			v.setExchangeDetailId(-1l);
			
			jList.add(v);
			String sendUnit = "";
			Long accountId = 0l;
			if (details != null && details.size() > 0) {
				try {
					if (details.get(0).getSendAccountId() == null) {
						accountId = orgManager.getMemberById(main.getStartUserId()).getOrgAccountId();
					} else {
						accountId = details.get(0).getSendAccountId();
					}
					V3xOrgEntity entity = orgManager.getAccountById(accountId);
					v.setlDate(details.get(0).getCreateTime() == null ? "" : DateUtil.format(details.get(0).getCreateTime(), DateUtil.YMDHMS_PATTERN));
					if (entity != null) {
						sendUnit = String.valueOf(entity.getName());
					}
					v.setSummary_unit(sendUnit);
				} catch (Exception ee) {
					LOGGER.error("联合发文查询列表查询组织机构有异常", ee);
				}
			}
			for (GovdocExchangeDetail detail : details) {
				if (detail.getSummaryId() == null || detail.getSummaryId().longValue() == summaryId) {
					continue;
				}
				v = new JointlyIssyedVO();
				v.setExchangeDetailId(detail.getId());
				v.setState(detail.getStatus());
				v.setSummaryId(Long.valueOf(detail.getSummaryId()));
				v.setType_str("协办单位");
				v.setSendFlow(true);
				v.setlDate(detail.getCreateTime() == null ? "" : DateUtil.format(detail.getCreateTime(), DateUtil.YMDHMS_PATTERN));
				v.setSummary_unit(detail.getRecOrgName());
				jList.add(v);
			}
		}
		if (needSubject) {
			List<JointlyIssyedVO> jList1 = jList;
			jList = new ArrayList<JointlyIssyedVO>();

			User user = AppContext.getCurrentUser();
			Long userId = user.getId();

			for (JointlyIssyedVO vo : jList1) {
				EdocSummary summary = govdocSummaryManager.getSummaryById(vo.getSummaryId());// 先这样吧
				if (summary != null) {
					vo.setSubject(summary.getSubject());
					vo.setSummaryState(summary.getState());
					if (summary.getStartMemberId().equals(userId)) {
						vo.setSendUserFlag(true);
					} else {
						vo.setSendUserFlag(false);
					}
				}
				jList.add(vo);
			}
		}
		return jList;
	}
	@Override
	@AjaxAccess
	public String[] validateJointlyHasNextPersion(Long summaryId, String units)
			throws BusinessException {
		String[] result = {"","",""};//Account|-8570577038226994488
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isNotBlank(units)){
			CtpTemplate ctpTemplate = null;
			FormBean formBean = null;
			FormDataMasterBean dataMasterBean = null;
			String[] utils = units.split(",");
			for(String ut : utils){
				Long utid = Long.valueOf(ut.split("\\|")[1]);
				V3xOrgAccount account = orgManager.getAccountById(utid);
				List<GovdocTemplateDepAuth> auths = govdocTemplateDepAuthManager.findByOrgIdAndAccountId4Lianhe(utid, utid);
				if (auths != null && auths.size() > 0) {
					ctpTemplate = templateManager.getCtpTemplate(auths.get(0).getTemplateId());
					if (ctpTemplate != null) {
						formBean = govdocFormManager.getFormByTemplate4Govdoc(ctpTemplate);
					}
				}
				dataMasterBean = new FormDataMasterBean(formBean.getNewFormAuthViewBean(),
						formBean.getMasterTableBean(), true);
				List<V3xOrgMember> members = GovdocUtil.getPersonNextNode(formBean, dataMasterBean, AppContext.getCurrentUser(), ctpTemplate);
				if(null == members || members.size() == 0){
					sb.append(account.getName()).append("、");
				}
			}
		}
		if(sb.length() > 0){
			result[0] = "以下单位流程首节点没有人员："+sb.substring(0, sb.length()-1);
		}
		return result;
	}
	@Override
	@AjaxAccess
	public String[] validateJointlyIssyedUnit(Long summaryId,String units) throws BusinessException {
		String[] result = {"","",""};
		GovdocExchangeMain main = govdocExchangeDao.findBySummaryId(summaryId, GovdocExchangeMain.EXCHANGE_TYPE_LIANHE);
		Map<String, String> condition = new HashMap<String,String>();
		condition.put("mainId", main.getId()+"");
		List<GovdocExchangeDetail> details = govdocExchangeDao.findGovdocExchangeDetail(new FlipInfo(),condition);
		if(details!=null&&!details.isEmpty()){
			Set<String> s = new HashSet<String>();
			for(GovdocExchangeDetail d:details){
				s.add("Account|"+d.getRecOrgId());
			}
			String[] unit = units.split(",");
			StringBuilder sb = new StringBuilder("");
			StringBuilder sb1 = new StringBuilder("");
			StringBuilder sb2 = new StringBuilder("");
			for(String u:unit){
				V3xOrgAccount account = orgManager.getAccountById(Long.parseLong(u.split("\\|")[1]));
				if(s.contains(u)){
					if(account!=null){
						if(sb.length()==0){
							sb.append(account.getName());
						}else{
							sb.append("、").append(account.getName());
						}
					}
				}else{
					if(account!=null){
						if(sb1.length()==0){
							sb1.append(u);
						}else{
							sb1.append("、").append(u);
						}
						if(sb2.length()==0){
							sb2.append(account.getName());
						}else{
							sb2.append("、").append(account.getName());
						}
					}
					
				}
			}
			result[0] = sb.toString();
			result[1] = sb1.toString();
			result[2] = sb2.toString();
		}
		return result;
	}
	@Override
	public void transUpdateLianheStatus(Long summaryId, ExchangeDetailStatus status) throws BusinessException {
		GovdocExchangeDetail detail = this.findDetailBySummaryId(summaryId);
		if (detail != null) {
			GovdocExchangeMain main = this.getGovdocExchangeMainById(detail.getMainId());
			if (main.getType() == GovdocExchangeMain.EXCHANGE_TYPE_LIANHE){
				detail.setStatus(status.getKey());
				this.updateDetail(detail);
				if(status.getKey() == ExchangeDetailStatus.ended.getKey()){
					GovdocExchangeDetailLog log = new GovdocExchangeDetailLog();
					log.setNewId();
					log.setDetailId(detail.getId());
					log.setDescription("收文单位流程已结束");
					log.setStatus(ExchangeDetailStatus.ended.getKey());
					log.setUserName(AppContext.currentUserName());
					Date date = new Date();
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(date);
					log.setTime(date);
					log.setTimeMS(calendar.get(Calendar.MILLISECOND));
					this.saveDetailLog(log);
				}
			}
		}
	}
	/*************************** 666666 联合发文相关   end ******************************/
	
	
	/*************************** 888888 公文交换工具方法 start ******************************/
	@Override
	public String getExchangeUnit(String idArrays) throws BusinessException {
		String[] idStrings = idArrays.split(",");
		String names = "";
		EntityPo entityPo = null;
		for(String id:idStrings){
			if(!"、".equals(id)){
				entityPo = FormEntityName.getNames(id);
				if(entityPo != null){
					names +=entityPo.getAccountName()+",";
				}
			}
		}
		return names;
	}
	@Override
	public String isConnectWhenSend(String ids) throws BusinessException {
		List<V3xOrgEntity> list = new ArrayList<V3xOrgEntity>();
		String ocipConnVal = addString2Org(ids, list);
		return ocipConnVal;
	}
	@Override
	public String validateExistAccountOrDepartment(String orgIdNames) throws BusinessException{
		String returnVal = ""; 
		if(Strings.isNotBlank(orgIdNames)){ 
			String[] orgIdArr = orgIdNames.split(",");
			for (int i=0;i<orgIdArr.length;i++){
				V3xOrgEntity v3xOrgEntity = orgManager.getEntityOnlyById(Long.parseLong(orgIdArr[i]));
				if(v3xOrgEntity==null){
					EdocObjTeam edocObjTeam = govdocObjTeamManager.getObjTeamById(Long.parseLong(orgIdArr[i]));
					if(edocObjTeam==null){
						ExchangeAccount exchangeAccount = govdocExchangeAccountManager.getExchangeAccount(Long.parseLong(orgIdArr[i]));
						if(exchangeAccount!=null){
							returnVal +=exchangeAccount.getId()+",";
						}
					}
				}
			}
			if(Strings.isNotBlank(returnVal)){
				returnVal = returnVal.substring(0, returnVal.lastIndexOf(","));
			}
		}
		return returnVal;
	}
	@Override
	@AjaxAccess
	public String validateExistAccount(String orgString) throws BusinessException{
		String returnVal = "";
		List<V3xOrgEntity> list = new ArrayList<V3xOrgEntity>();
		if(Strings.isNotBlank(orgString)){
			addString2Org(orgString,list);
			for (V3xOrgEntity v : list) {
				/** OCIP公文交换，屏蔽掉平台对象对表单的检查 renx* start 2017-7-7 */
//				if(AppContext.hasPlugin("ocip")) {
//					if (OrgUtil.isPlatformEntity(v)) {
//						continue;
//					}
//				}
				/** OCIP公文交换，屏蔽掉平台对象对表单的检查 renx* end 2017-7-7 */
				GovdocTemplateDepAuth govdocTemplateDepAuth = govdocTemplateDepAuthManager.findExchangeByOrgId(v.getId());
				boolean flag = false;
				if(null==govdocTemplateDepAuth){
					if("Department".equals(v.getEntityType())){
						GovdocTemplateDepAuth govdocTemplateDepAuth2 = govdocTemplateDepAuthManager.findExchangeByOrgId(v.getOrgAccountId());
						if(null==govdocTemplateDepAuth2){
							flag = true;
						}else{
							flag = !govdocFormManager.formIsEnable(govdocTemplateDepAuth2.getTemplateId());
						}
					}else{
						flag = true;
					}
				}else{
					if("Department".equals(v.getEntityType())){
						if(!govdocFormManager.formIsEnable(govdocTemplateDepAuth.getTemplateId())){
							GovdocTemplateDepAuth govdocTemplateDepAuth2 = govdocTemplateDepAuthManager.findExchangeByOrgId(v.getOrgAccountId());
							if(null==govdocTemplateDepAuth2){
								flag = true;
							}else{
								flag = !govdocFormManager.formIsEnable(govdocTemplateDepAuth2.getTemplateId());
							}
						}
					}
					
				}
				if(flag){
					returnVal+=v.getName() +" ";
				}
			}
		}
		return returnVal;
	}
	
	@Override
	public String validateReissueeRepeat(Long summaryId,String orgString) throws BusinessException{
		String values = "";
		String names = "";
		List<V3xOrgEntity> list = new ArrayList<V3xOrgEntity>();
		if(Strings.isNotBlank(orgString)){
			addString2Org(orgString, list);//要补发的单位LIST
			Map<String,String> map = new HashMap<String, String>();
			List<V3xOrgEntity> entities = new ArrayList<V3xOrgEntity>();//保存已经发送了的单位LITS
			for (V3xOrgEntity v : list) {
				GovdocExchangeMain main = this.findBySummaryId(summaryId, null);
				List<GovdocExchangeDetail> mydetails = govdocExchangeDao.getDetailByMainId(main.getId());
				for (GovdocExchangeDetail detail : mydetails) {
					if(detail.getRecOrgId().longValue() == v.getId().longValue()&&detail.getStatus()!=ExchangeDetailStatus.waitSend.getKey()){
						entities.add(v);
					}
				}
			} 
			if(entities!=null&&entities.size()>0){
				Set<V3xOrgEntity> vSet = new HashSet<V3xOrgEntity>();
				vSet.addAll(entities);
				for (V3xOrgEntity v : vSet) {
					names+=v.getName()+",";
				}
				list.removeAll(vSet);
				for (V3xOrgEntity v : list) {
					values+=v.getEntityType()+"|"+v.getId()+",";
				}
			}
			if(!"".equals(names)){
				if(!"".equals(values)){
				   values=values.substring(0,values.length()-1);
				}
				names=names.substring(0,names.length()-1);
				map.put("values", values);
				map.put("names", names); 
				return JSONUtil.toJSONString(map);
			}
		}
		return null;
	}

	private String addString2Org(String orgString,List<V3xOrgEntity> list) throws NumberFormatException, BusinessException{
		String[] ids = orgString.split(",");
		String ocipConnVal = "";
		for (String org : ids) {
			if(Strings.isNotBlank(org)) {
				if(org.split("\\|").length > 1) {
					String orgStr = org.split("\\|")[0];
					String orgId = org.split("\\|")[1]; 
					V3xOrgEntity v = null;
					if("Account".equals(orgStr)){
						v = orgManager.getAccountById(Long.parseLong(orgId));
					}else if("Department".equals(orgStr)){
						v = orgManager.getDepartmentById(Long.parseLong(orgId));
					}else if("OrgTeam".equals(orgStr)){
						EdocObjTeamManager edocObjTeamManager = (EdocObjTeamManager)AppContext.getBean("edocObjTeamManager");
						EdocObjTeam edocObjTeam  = edocObjTeamManager.getById(Long.parseLong(orgId));
						if(Strings.isNotBlank(edocObjTeam.getSelObjsStr())){
							ocipConnVal = addString2Org(edocObjTeam.getSelObjsStr(),list);
						}
					}
					if(v!=null){
						list.add(v);
					}else{
						if(!"OrgTeam".equals(orgStr)&&AppContext.hasPlugin("ocip")&&!OnlineChecker.isOnline()){//如果开启了ocip插件但未连接上并且单位为空
							ocipConnVal = "ocipNotConn";
						}
					}
				}
			}
		}
		return ocipConnVal;
	}
	/*************************** 888888 公文交换工具方法   end ******************************/
	
	/*************************** 99999 Spring注入，请将业务写在上面 start ******************************/
	public void setOrganizationManager(IOrganizationManager organizationManager) {
		this.organizationManager = organizationManager;
	}
	public void setGovdocExchangeDao(GovdocExchangeDao govdocExchangeDao) {
		this.govdocExchangeDao = govdocExchangeDao;
	}
	public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}
	public void setGovdocObjTeamManager(GovdocObjTeamManager govdocObjTeamManager) {
		this.govdocObjTeamManager = govdocObjTeamManager;
	}
	public void setGovdocExchangeAccountManager(GovdocExchangeAccountManager govdocExchangeAccountManager) {
		this.govdocExchangeAccountManager = govdocExchangeAccountManager;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public void setGovdocFormManager(GovdocFormManager govdocFormManager) {
		this.govdocFormManager = govdocFormManager;
	}
	public void setGovdocTemplateDepAuthManager(GovdocTemplateDepAuthManager govdocTemplateDepAuthManager) {
		this.govdocTemplateDepAuthManager = govdocTemplateDepAuthManager;
	}
//	public void setOcipExchangeEdocManager(IOCIPExchangeEdocManager ocipExchangeEdocManager) {
//		this.ocipExchangeEdocManager = ocipExchangeEdocManager;
//	}
	public void setGovdocRegisterManager(GovdocRegisterManager govdocRegisterManager) {
		this.govdocRegisterManager = govdocRegisterManager;
	}	
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public void setGovdocPubManager(GovdocPubManager govdocPubManager) {
		this.govdocPubManager = govdocPubManager;
	}
	public void setGovdocMarkManager(GovdocMarkManager govdocMarkManager) {
		this.govdocMarkManager = govdocMarkManager;
	}

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public WorkflowApiManager getWapi() {
		return wapi;
	}

	public void setWapi(WorkflowApiManager wapi) {
		this.wapi = wapi;
	}
	
	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }
	
	/*************************** 99999 Spring注入，请将业务写在上面   end ******************************/
	
}
