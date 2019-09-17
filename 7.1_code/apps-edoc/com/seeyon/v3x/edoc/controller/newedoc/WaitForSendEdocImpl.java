package com.seeyon.v3x.edoc.controller.newedoc;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseTemplateRole;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.track.manager.CtpTrackMemberManager;
import com.seeyon.ctp.common.track.po.CtpTrackMember;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.edoc.constants.EdocOrgConstants;
import com.seeyon.v3x.edoc.dao.EdocOpinionDao;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.domain.EdocFormAcl;
import com.seeyon.v3x.edoc.domain.EdocFormExtendInfo;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.domain.EdocSummaryQuick;
import com.seeyon.v3x.edoc.domain.EdocSummaryRelation;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocSummaryRelationManager;
import com.seeyon.v3x.edoc.manager.EdocSwitchHelper;
import com.seeyon.v3x.edoc.util.EdocOpinionDisplayUtil;
import com.seeyon.v3x.edoc.util.NewEdocHelper;
import com.seeyon.v3x.exchange.domain.EdocSendRecord;

public class WaitForSendEdocImpl extends NewEdocHandle {
	private static final Log LOGGER = LogFactory.getLog(WaitForSendEdocImpl.class);

	@Override
	public void createEdocSummary(HttpServletRequest request, ModelAndView modelAndView) throws Exception {
		boolean isTrack = false;
		long summaryId = Long.parseLong(s_summaryId);
		summary = edocManager.getEdocSummaryById(summaryId, true);

		// 设置文号为空，用户自己去选择-OA-55004兼职人员快速发文，拟文选择下拉文号后，保存待发，在liud02单位编缉，文单，文号，交换类型使用的不是同一个单位的
		if (summary != null && summary.getOrgAccountId().longValue() != user.getLoginAccount().longValue()) {

			summary.setDocMark("");
			summary.setDocMark2("");
			summary.setSerialNo("");
		}
		iEdocType = summary.getEdocType();// 修改待发，类型参数没有传递进来

		// ***快速发文相关变量 start***
		boolean isQuickSend = false; // 快速发文标识
		isQuickSend = summary.getIsQuickSend() == null ? false : summary.getIsQuickSend();
		// ***快速发文相关变量 end***

		body = summary.getFirstBody();

		atts = getAttachmentsIncludeSender(summaryId);

		senderOpinion = summary.getSenderOpinion();// 发起人附言
		String affairId = request.getParameter("affairId");
		try {
			CtpAffair affair = affairManager.get(Long.parseLong(affairId));

			modelAndView.addObject("waitSendFlag", "true");

			if (affair != null && affair.getSubState() != null
					&& affair.getSubState().equals(SubStateEnum.col_pending_specialBacked.getKey())) {
				if (!affair.getSubState().equals(SubStateEnum.col_pending_specialBacked.getKey())) {
					modelAndView.addObject("fromSendBack", true);
				}
			}
			modelAndView.addObject("affair", affair);
			modelAndView.addObject("subState", affair.getSubState());
		} catch (NumberFormatException nfe) {
			affairId = "-1";
		}

		if (summary.getProcessId() != null) {
			modelAndView.addObject("isForm", "false");
		}

		modelAndView.addObject("templeteId", summary.getTempleteId());
		String docMarkByTemplateJs = EdocHelper.exeTemplateMarkJs(null, null, null);
		if (summary.getTempleteId() != null) {

			// OA-33679 客户bug：公文模版被删除后，正在走的流程受到影响
			// 注释下面的 模板检测
			// 后台设置的模板，只是在调用的时候限制，对于已经发出的使用该模板的公文是不受影响的，bug描述中的情况应该是可以继续处理这个公文的
			/*
			 * Long tId=summary.getTempleteId(); boolean b =
			 * templeteManager.checkTemplete(tId, user.getId()); if(!b){
			 * StringBuffer msg = new StringBuffer(); msg.append("<script>");
			 * msg.append("alert('');"); msg.append("history.back()");
			 * msg.append("</script>"); throw new
			 * NewEdocHandleException(msg.toString(),NewEdocHandleException.
			 * PRINT_CODE); }
			 */
			// List<ColBranch> branchs =
			// this.templeteManager.getBranchsByTemplateId(summary.getTempleteId(),ApplicationCategoryEnum.edoc.ordinal());
			// //显示分支条件使用流程中保留的，如果为空使用模板中的

			templete = templeteManager.getCtpTemplate(summary.getTempleteId());

			templeteType = templete.getType();// 这里设置模版类型

			boolean hasAuth = templeteManager.isTemplateEnabled(templete, AppContext.currentUserId());
			modelAndView.addObject("temTraceType", templete.getCanTrackWorkflow());
			// OA-48412 调用模板保存待发后将模板停用了，选中该公文，单击编辑按钮，还能进行编辑，应该是提示模板被停用
			if (templete.isDelete() || templete.getState() == 1 || !hasAuth) {
				StringBuilder msg = new StringBuilder();
				msg.append("<script>");
				msg.append("alert('" + ResourceUtil.getString("edoc.alert.templete.stop.or.delete") + "');");
				msg.append("parent.parent.location.href='edocController.do?method=listIndex&from=listWaitSend&edocType="
						+ summary.getEdocType() + "&listType=listWaitSend';");
				msg.append("</script>");
				throw new NewEdocHandleException(msg.toString(), NewEdocHandleException.PRINT_CODE);
			}

			// <v3x:fileUpload attachments="${attachments}"
			// canDeleteOriginalAtts="${canDeleteOriginalAtts}"
			// 上面就是拟文页面用到的附件组件，其中canDeleteOriginalAtts属性就表示是否可以修改附件
			// ******************
			// OA-26792 lixsh调用带有附件的发文模板拟文，附件不可以删除，但保存待发后就可以编辑了
			// OA-27253 lixsh调用系统模板后插入附件，保存待发后编辑，拟文时插入的附件也不可删除了，预归档目录也不可以编辑了
			// ******************以上两个bug只能通过下面这种方式来解决

			// 当模板中有附件时，这样保存待发之后，编辑附件依然不能修改
			// isHasAttachments返回true表示有附件，那么就不能删除附件
			// canDeleteOriginalAtts =
			// !CtpTemplateUtil.isHasAttachments(templete);

			// 现在统一改为附件可以删除的
			canDeleteOriginalAtts = true;
			// ********************************************************

			// OA-23515 调用模版，新建发文，在已发中撤销了，待发中编辑，编辑页面丢了基准时长
			standarduration = templete.getStandardDuration() == null ? 0
					: Integer.parseInt(templete.getStandardDuration().toString());

			EdocSummary templasteSummary = (EdocSummary) XMLCoder.decoder(templete.getSummary());
			docMarkByTemplateJs = EdocHelper.exeTemplateMarkJs(templasteSummary.getDocMark(),
					templasteSummary.getDocMark2(), templasteSummary.getSerialNo());
			// OA-24783 lixsh调用模板后，选择预归档目录发送后，在已发中撤销后，在待发中编辑，预归档目录不可以编辑了
			// 判断模板中是否设置了预归档 (设置了就不为无的状态)
			Long archiveId = templasteSummary.getArchiveId();
			if (archiveId != null) {
				modelAndView.addObject("setArchive", "true");
			}

			isSystem = templete.isSystem();
			// 当调用模板之后(模板中已经设置了deadline)，存为个人模板,再保存待发，编辑，之前的模板中已经设置的数据，不能编辑
			// modelAndView.addObject("isFromTemplate", templete.isSystem());
			modelAndView.addObject("isFromTemplate", templete.isSystem() || templete.getFormParentid() != null);
			// OA-8634 拟文，输入标题和流程后另存为个人模版，然后去调用这个模版，修改模版的标题，再次另存为后调用，报null
			Long workflowId = templete.getWorkflowId();

			boolean toSetXML = false;// 是否设置模版的xml作为流程初始化xml
			if (!isSystem) {

				Long parentTempId = templete.getFormParentid();
				if (parentTempId != null) {
					CtpTemplate parentTemp = templeteManager.getCtpTemplate(parentTempId);
					if (parentTemp == null || !parentTemp.isSystem()) {
						toSetXML = true;
					}
				} else {
					toSetXML = true;
				}

				// 已经修改个人模版的流程
				if (toSetXML && summary.getProcessId() != null) {
					toSetXML = false;
				}
			}

			if (workflowId != null && toSetXML) {// 自由公文的个人模板
				WorkflowApiManager wapi  = (WorkflowApiManager) AppContext.getBean("wapi");
				String process_xml = wapi.selectWrokFlowTemplateXml(String.valueOf(workflowId.longValue()));
				if (Strings.isNotBlank(process_xml))
					process_xml = Strings.escapeJavascript(process_xml);
				modelAndView.addObject("process_xml", process_xml);
			}

			modelAndView.addObject("summaryFromTemplate", templasteSummary); // 获得模板的edcosummary对象。比如流程期限，如果模板流程期限设置了，需要把流程期限置灰。
			modelAndView.addObject("templateType", templete.getType());
			modelAndView.addObject("templeteProcessId", templete.getWorkflowId());
			boolean sVisorsFromTemplate = false;

			CtpSuperviseDetail detail = superviseManager.getSupervise(summary.getTempleteId());

			Set<String> sIdSet = new HashSet<String>();
			if (detail != null) {
				List<CtpSupervisor> superviseors = superviseManager.getSupervisors(detail.getId());
				for (CtpSupervisor supervisor : superviseors) {
					sIdSet.add(supervisor.getSupervisorId().toString());
				}
				List<CtpSuperviseTemplateRole> roleList = superviseManager.findRoleByTemplateId(templete.getId());

				for (CtpSuperviseTemplateRole role : roleList) {
					if (null == role.getRole() || "".equals(role.getRole())) {
						continue;
					}
					if (role.getRole().toLowerCase().equals(EdocOrgConstants.ORGENT_META_KEY_SEDNER.toLowerCase())) {
						sIdSet.add(String.valueOf(user.getId()));
					}
					if (role.getRole().toLowerCase().equals(EdocOrgConstants.ORGENT_META_KEY_DEPMANAGER.toLowerCase())
							|| role.getRole().toLowerCase().equals(EdocOrgConstants.ORGENT_META_KEY_SEDNER.toLowerCase()
									+ EdocOrgConstants.ORGENT_META_KEY_DEPMANAGER.toLowerCase())) {
						V3xOrgRole orgRole = orgManager.getRoleByName(EdocOrgConstants.ORGENT_META_KEY_DEPMANAGER,
								user.getLoginAccount());
						if (null != orgRole) {
							List<V3xOrgDepartment> depList = orgManager.getDepartmentsByUser(user.getId());
							for (V3xOrgDepartment dep : depList) {
								List<V3xOrgMember> managerList = orgManager.getMembersByRole(dep.getId(),
										orgRole.getId());
								for (V3xOrgMember mem : managerList) {
									sIdSet.add(mem.getId().toString());
								}
							}
						}
					}

					if (role.getRole().toLowerCase().equals(EdocOrgConstants.ORGENT_META_KEY_SEDNER.toLowerCase()
							+ EdocOrgConstants.ORGENT_META_KEY_SUPERDEPMANAGER.toLowerCase())) {
						V3xOrgRole orgRole = orgManager.getRoleByName(EdocOrgConstants.ORGENT_META_KEY_SUPERDEPMANAGER,
								user.getLoginAccount());
						if (null != orgRole) {
							List<V3xOrgDepartment> depList = orgManager.getDepartmentsByUser(user.getId());
							for (V3xOrgDepartment dep : depList) {
								List<V3xOrgMember> superManagerList = orgManager.getMembersByRole(dep.getId(),
										orgRole.getId());
								for (V3xOrgMember mem : superManagerList) {
									sIdSet.add(mem.getId().toString());
								}
							}
						}
					}
				}

				sVisorsFromTemplate = (detail.getSupervisors() != null && !detail.getSupervisors().isEmpty());
				modelAndView.addObject("unCancelledVisor", Strings.join(sIdSet, ","));
			}
			if (!sVisorsFromTemplate) {
				List<CtpSuperviseTemplateRole> roleList = superviseManager
						.findRoleByTemplateId(summary.getTempleteId());
				sVisorsFromTemplate = (null != roleList && !roleList.isEmpty());
			}
			modelAndView.addObject("sVisorsFromTemplate", sVisorsFromTemplate);// 公文调用的督办模板是否设置了督办人
			// 用来保存模板自带的督办人员 BUG-OA-39192
		}

		CtpSuperviseDetail detail = superviseManager.getSupervise(summaryId);

		modelAndView.addObject("fromSend", "true");
		if (detail != null) {
			List<CtpSupervisor> supervisors = superviseManager.getSupervisors(detail.getId());
			if (supervisors != null && supervisors.size() > 0) {
				StringBuilder ids = new StringBuilder();
				for (CtpSupervisor supervisor : supervisors) {
					ids.append(supervisor.getSupervisorId() + ",");
				}
				modelAndView.addObject("colSupervisors", ids.substring(0, ids.length() - 1));
			}
			modelAndView.addObject("colSupervise", detail);
			modelAndView.addObject("superviseDate",
					Datetimes.format(detail.getAwakeDate(), Datetimes.datetimeWithoutSecondStyle));
		}

		edocFormId = summary.getFormId();
		// 检查模版公文单是否存在
		defaultEdocForm = edocFormManager.getEdocForm(edocFormId);

		// OA-42056
		// 升级测试：从会议纪要转公文，然后将公文保存待发，待发列表进行编辑的文单样式或者首页进行编辑的文单样式和单击待发列表打开的文单不同
		boolean isGroupVer = (Boolean) (SysFlag.sys_isGroupVer.getFlag());// 是否是集团版本
		// OA-36056 检查原来调用的公文单是否已被取消授权，但是调用模板的除外
		if (templete == null && isGroupVer) {// 如果不是集团版本，就不用判断文单的授权了
			Set<EdocFormAcl> formAcls = defaultEdocForm.getEdocFormAcls();
			Set<EdocFormExtendInfo> formExs = defaultEdocForm.getEdocFormExtendInfo();
			boolean containFlag = false;
			for (EdocFormAcl ec : formAcls) {
				if (ec.getDomainId().longValue() == user.getLoginAccount().longValue()) {
					containFlag = true;
					break;
				}
			}
			// OA-58465政务版：被应用的文单停用后，编辑草稿状态的流程，没提示文单已停用，且还能使用
			for (EdocFormExtendInfo ef : formExs) {
				if (ef.getAccountId().longValue() == user.getLoginAccount().longValue()
						&& ef.getStatus().intValue() != EdocForm.C_iStatus_Published.intValue()) {
					defaultEdocForm = null;
					modelAndView.addObject("formDisableWarning", "yes"); // 需要给前端传递参数提示文单被停用
					break;
				}
			}

			if (!containFlag) {
				defaultEdocForm = null;
			}
		}
		// 读取流程中的处理意见进行显示，待发中的公文有可能是回退、撤销到待发的，要显示历史处理过程中的意见
		// 公文处理意见回显到公文单,排序
		LinkedHashMap lhs = edocManager.getEdocOpinion(summaryId, user.getId(), summary.getStartUserId(), "newEdoc");
		Hashtable hs = edocManager.getEdocOpinion(summary.getFormId(), lhs);
		String opinionsJs = EdocOpinionDisplayUtil.optionToJs(hs);
		modelAndView.addObject("opinionsJs", opinionsJs);

		// 公文处理意见回显到公文单,排序
		List<String> ols = edocFormManager.getOpinionElementLocationNames(summary.getFormId(),
				summary.getOrgAccountId());
		String hwjs = htmlHandWriteManager.getHandWritesJs(summaryId, user.getName(), ols);
		modelAndView.addObject("hwjs", hwjs);
		// 发起人意见
		modelAndView.addObject("senderOpinion", hs.get("senderOpinionList"));
		List<EdocOpinion> senderOpinions = (List<EdocOpinion>) hs.get("senderOpinionList");
		//if (senderOpinion != null &&Strings.isNotEmpty(senderOpinions)) {
		//6.1中解除EdocSummary和EdocOpinion的hibernate关联关系，在此添加发起人意见解析
		if (Strings.isNotEmpty(senderOpinions)) {
			StringBuilder _opinionConttens = new StringBuilder();
			for (EdocOpinion eo : senderOpinions) {
				if(senderOpinion==null){
					senderOpinion=eo;
				}else{
					if(senderOpinion.getCreateTime().after(eo.getCreateTime())){
						senderOpinion=eo;
					}
				}
				if(eo.getState() == 0){
					_opinionConttens.append(eo.getContent()).append("\r\n");
				}
			}
			if(senderOpinion!=null){
				senderOpinion.setContent(_opinionConttens.toString());
			}
		}
		if(senderOpinion == null) {
			LOGGER.error("老公文编辑时发起意见为空：" + summary.getId());
		}
		// 获得设置的跟踪
		CtpTrackMemberManager trackManager = (CtpTrackMemberManager)AppContext.getBean("trackManager");
		// OA-40773 唐桂林 wf从待发中编辑一个收文流程，跟踪设置中显示的跟踪人员显示2遍。而实际发起这个流程时并没有设置跟踪人员
		if (Strings.isNotBlank(affairId)) {
			List<CtpTrackMember> tracks = trackManager.getTrackMembers(Long.valueOf(affairId));
			if (tracks != null && tracks.size() > 0) {
				StringBuilder ids = new StringBuilder();
				for (CtpTrackMember colTrackMember : tracks) {
					if (ids.length() > 0) {
						ids.append(",");
					}
					ids.append(colTrackMember.getTrackMemberId());
				}

				modelAndView.addObject("trackIds", ids);
			}
			CtpAffair affairTrack = affairManager.get(Long.valueOf(affairId));
			isTrack = affairTrack.getTrack() == 0 ? false : true;
			modelAndView.addObject("isTrack", isTrack);
		}
		// 归档可修改
		if (from != null && "archived".equals(from)) {
			cloneOriginalAtts = true;// 正文要复制一份,用于归档修改后留痕
			modelAndView.addObject("archivedModify", true);
			summary.setFrom(from);
		}
		if (iEdocType == 1) {// 收文
			// 设置登记时间(注意：若是指定回退流程重走，则取之前的登记日期)
			Date oldDate = summary.getRegistrationDate();
			summary.setRegistrationDate(new Date(System.currentTimeMillis()));
			List<CtpAffair> affairs = affairManager.getAffairs(summary.getId(), StateEnum.col_waitSend);
			if (Strings.isNotEmpty(affairs)) {
				for (CtpAffair affair : affairs) {
					if (affair.getSubState() == SubStateEnum.col_pending_specialBackToSenderCancel.key()
							|| affair.getSubState() == SubStateEnum.col_pending_specialBacked.key()) {
						summary.setRegistrationDate(oldDate);
						break;
					}
				}
			}

			edocRegister = edocRegisterManager.findRegisterByDistributeEdocId(summary.getId());
			if (edocRegister != null) {
				registerBody = edocRegisterManager.findRegisterBodyByRegisterId(edocRegister.getId());
				registerId = edocRegister.getId();// 登记id
				distributeEdocId = edocRegister.getDistributeEdocId();// 获取收文id
				// OA-29588 新建收文，单位管理员去撤销了该公文，待发中进行编辑，正文类型置灰了（当前公文是自由流程）
				// OA-34500 wangchw登记交换过来的发文发送后，在收文已发送中撤销，在待发中再次编辑，可以修改正文类型了

				if (!EdocHelper.isG6Version()) {
					comm = "distribute";
					record = recieveEdocManager.getEdocRecieveRecordByReciveEdocId(summary.getId());
					if (record != null) {
						// 设置签收时间
						summary.setReceiptDate(new Date(record.getRecTime().getTime()));
					}

					if (edocRegister.getRegisterType() == EdocRegister.REGISTER_TYPE_BY_PAPER_REC_EDOC) {// A8纸质登记
						modelAndView.addObject("isA8PaperRegister", true);// 用于控制前端正文类型可切换
					}
				}
				// 电子登记发送,撤销后，待发编辑 正文类型不能选择
				else if (edocRegister.getRegisterType() == 1) {
					comm = "distribute";// 登记收文-草稿箱中调用模板时，进入登记收文条件openTempleteOfExchangeRegist
				}
			}
			isFromWaitSend = true;
		}

		// OA-32958
		// 收文转发文，选择新发文关联收文。然后在发文-拟文时保存待发，在待发中编辑，发文关联收文的字样不在了。但是发送以后，在已发中打开，可以关联到收文。
		// 如果是转发文(新发文关联收文)后，保存待发了，再编辑，需要在拟文页面显示出 发文关联收文 字样
		EdocSummaryRelationManager edocSummaryRelationManager = (EdocSummaryRelationManager)AppContext.getBean("edocSummaryRelationManager");
		EdocSummaryRelation edocSummaryRelationR = edocSummaryRelationManager.findRecEdoc(summaryId, 0);
		if (edocSummaryRelationR != null) {
			modelAndView.addObject("newContactReceive", "1");
			String relationUrl = NewEdocHelper.relationReceive(String.valueOf(edocSummaryRelationR.getRelationEdocId()),
					"1") + "&affairId=" + edocSummaryRelationR.getRecAffairId();
			modelAndView.addObject("relationUrl", relationUrl);
			// OA-33197 收文转发文，先保存待发，然后编辑，发送，页面出异常。但是系统消息显示该公文发送成功了。
			modelAndView.addObject("relationRecId", edocSummaryRelationR.getRelationEdocId());
		}
		// OA-36095
		// wangchw登记了纸质公文，在待办中转发文--收文关联新发文，收文处理节点查看有关联链接，处理时回退该流程，发起人在待发中查看有此链接直接发送后已发待办中也有，但是若在待发中编辑没有此链接，发送后也没此链接
		// 收文关联发文
		edocSummaryRelationR = edocSummaryRelationManager.findRecEdoc(summaryId, 1);
		if (edocSummaryRelationR != null) {
			modelAndView.addObject("newContactReceive", "2");
			modelAndView.addObject("recEdocId", summaryId);
			modelAndView.addObject("recType", 1);

		}
		modelAndView.addObject("docMarkByTemplateJs", docMarkByTemplateJs);

		// ***快速发文，读取相关信息from quick表 start ***
		if (isQuickSend) {
			EdocSummaryQuick edocSummaryQuick = edocSummaryQuickManager.findBySummaryId(summaryId);
			String taohongTemplateUrl = edocSummaryQuick.getTaohongTemplateUrl();
			taohongTemplateUrl = Strings.escapeJavascript(taohongTemplateUrl);// 特殊字符替换
			edocSummaryQuick.setTaohongTemplateUrl(taohongTemplateUrl);

			modelAndView.addObject("edocSummaryQuick", edocSummaryQuick);
			if (summary.getEdocType() == 0 && summary.getOrgAccountId().equals(user.getLoginAccount())) {// 兼职读取的是兼职单位的设置

				int exchangeTypeValue = edocSummaryQuick.getExchangeType() == null
						? EdocSwitchHelper.getDefaultExchangeType() : edocSummaryQuick.getExchangeType();
				modelAndView.addObject("exchangeTypeValue", exchangeTypeValue);

				String exchangeDeptTypeValue = edocSummaryQuick.getExchangeDeptType();
				if (Strings.isNotBlank(exchangeDeptTypeValue)
						|| exchangeTypeValue != EdocSendRecord.Exchange_Send_iExchangeType_Dept) {// 设置默认值

					String exchangeDeptTypeSwitchValue = EdocSwitchHelper
							.getDefaultExchangeDeptType(user.getLoginAccount());
					edocSummaryQuick.setExchangeDeptType(exchangeDeptTypeSwitchValue);
				}
			}

			// 预归档
			Long archiveId = null;
			String quickArchiveName = "";
			if (edocSummaryQuick.getArchiveId() != null) {
				archiveId = edocSummaryQuick.getArchiveId();
				if (AppContext.hasPlugin("doc")) {
					quickArchiveName = docApi.getDocResourceName(archiveId);
					modelAndView.addObject("hasDoc", true);
				}
			}
			modelAndView.addObject("quickArchiveName", quickArchiveName);
			modelAndView.addObject("quickArchiveId", archiveId);
		}
		// ***快速发文，读取相关信息from quick表 end ***

		if (summary.getEdocType() == 1) {
			modelAndView.addObject("edocId", summary.getId());
		}
	}

	private List<Attachment> getAttachmentsIncludeSender(long summaryId) {
		List<Attachment> __atts = new ArrayList<Attachment>();

		List<Attachment> _atts = attachmentManager.getByReference(summaryId);
		EdocOpinionDao edocOpinionDao = (EdocOpinionDao)AppContext.getBean("edocOpinionDao");
		List<EdocOpinion> senderOpinions = edocOpinionDao.getSenderOpinions(summaryId);
		List<Long> opinionIds = new ArrayList<Long>();
		if (Strings.isNotEmpty(senderOpinions)) {
			for (EdocOpinion eo : senderOpinions) {
				opinionIds.add(eo.getId());
			}
		}

		if (Strings.isNotEmpty(_atts)) {
			for (Attachment a : _atts) {
				if (opinionIds.contains(a.getSubReference()) || Long.valueOf(summaryId).equals(a.getSubReference())) {
					Attachment aclone = null;
					try {
						aclone = (Attachment) a.clone();
						aclone.setSubReference(a.getReference());
					} catch (CloneNotSupportedException e) {
						LOGGER.error("", e);
					}
					if (aclone != null) {
						__atts.add(aclone);
					}
				}
			}
		}

		return __atts;
	}
}
