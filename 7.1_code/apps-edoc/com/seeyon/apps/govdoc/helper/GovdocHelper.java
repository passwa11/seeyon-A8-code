package com.seeyon.apps.govdoc.helper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.collaboration.event.CollaborationAutoSkipEvent;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.constants.DocConstants;
import com.seeyon.apps.govdoc.bo.DateSharedWithWorkflowEngineThreadLocal;
import com.seeyon.apps.govdoc.manager.GovdocContentManager;
import com.seeyon.apps.govdoc.manager.GovdocDocTemplateManager;
import com.seeyon.apps.govdoc.manager.GovdocExchangeAccountManager;
import com.seeyon.apps.govdoc.manager.GovdocExchangeManager;
import com.seeyon.apps.govdoc.manager.GovdocFormManager;
import com.seeyon.apps.govdoc.manager.GovdocObjTeamManager;
import com.seeyon.apps.govdoc.manager.GovdocOpenManager;
import com.seeyon.apps.govdoc.manager.GovdocPishiManager;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.govdoc.manager.GovdocWorkflowManager;
import com.seeyon.apps.govdoc.mark.manager.GovdocMarkManager;
import com.seeyon.apps.govdoc.mark.manager.GovdocMarkOpenManager;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.apps.meeting.api.MeetingApi;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.customize.manager.CustomizeManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.bo.CustomAction;
import com.seeyon.ctp.common.permission.bo.DetailAttitude;
import com.seeyon.ctp.common.permission.bo.NodePolicy;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.dao.OrgDao;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.enums.EdocMessageFilterParamEnum;
import com.seeyon.v3x.edoc.enums.EdocOpenFrom;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

public class GovdocHelper extends EdocHelper {
	
	public static final String _IsNewGovdoc = "_isNewGovdoc";//用于保存edoc_summary是添加扩展字段属性

	private static final Log LOGGER = LogFactory.getLog(GovdocHelper.class);
	
	private static final int          CURRENT_NODES_INFO_SIZE  = 10;//当前待办人数量
	
	protected static final GovdocSummaryManager govdocSummaryManager = (GovdocSummaryManager)AppContext.getBean("govdocSummaryManager");
	protected static final GovdocExchangeManager govdocExchangeManager = (GovdocExchangeManager) AppContext.getBean("govdocExchangeManager");
	protected static final GovdocExchangeAccountManager govdocExchangeAccountManager = (GovdocExchangeAccountManager)AppContext.getBean("govdocExchangeAccountManager");
	protected static final GovdocObjTeamManager govdocObjTeamManager = (GovdocObjTeamManager)AppContext.getBean("govdocObjTeamManager");
	protected static final GovdocOpenManager govdocOpenManager = (GovdocOpenManager) AppContext.getBean("govdocOpenManager");
	protected static final GovdocMarkOpenManager govdocMarkOpenManager = (GovdocMarkOpenManager)AppContext.getBean("govdocMarkOpenManager");
	protected static final GovdocMarkManager govdocMarkManager = (GovdocMarkManager)AppContext.getBean("govdocMarkManager");
	protected static final GovdocDocTemplateManager govdocDocTemplateManager = (GovdocDocTemplateManager)AppContext.getBean("govdocDocTemplateManager");
	protected static final GovdocContentManager govdocContentManager = (GovdocContentManager) AppContext.getBean("govdocContentManager");
	protected static final GovdocWorkflowManager govdocWorkflowManager = (GovdocWorkflowManager) AppContext.getBean("govdocWorkflowManager");
	protected static final GovdocFormManager govdocFormManager = (GovdocFormManager) AppContext.getBean("govdocFormManager");
	protected static final GovdocPishiManager govdocPishiManager = (GovdocPishiManager) AppContext.getBean("govdocPishiManager");
	
	protected static final AffairManager affairManager = (AffairManager) AppContext.getBean("affairManager");
	protected static final EnumManager enumManagerNew = (EnumManager) AppContext.getBean("enumManagerNew");
	protected static final PermissionManager permissionManager = (PermissionManager) AppContext.getBean("permissionManager");
	protected static final CommentManager ctpCommentManager = (CommentManager) AppContext.getBean("ctpCommentManager");
	protected static final MainbodyManager ctpMainbodyManager = (MainbodyManager) AppContext.getBean("ctpMainbodyManager");
	protected static final AttachmentManager attachmentManager = (AttachmentManager) AppContext.getBean("attachmentManager");
	protected static final FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
	protected static final CustomizeManager customizeManager = (CustomizeManager) AppContext.getBean("customizeManager");
	protected static final MeetingApi meetingApi = (MeetingApi) AppContext.getBean("meetingApi");
	protected static final FormApi4Cap3 formApi4Cap3 = (FormApi4Cap3) AppContext.getBean("formApi4Cap3");
	protected static final WorkTimeManager workTimeManager = (WorkTimeManager)AppContext.getBean("workTimeManager");
	protected static final TemplateManager templateManager = (TemplateManager)AppContext.getBean("templateManager");
	protected static final WorkflowApiManager wapi = (WorkflowApiManager)AppContext.getBean("wapi");
	
	protected static final DocApi docApi = (DocApi)AppContext.getBean("docApi");
	protected static final OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
	protected static final OrgDao orgDao = (OrgDao) AppContext.getBean("orgDao");
	
	
	/**
	 * 发送消息 控制
	 * @param affair
	 * @return
	 */
	public static Integer getImportantLevel(EdocSummary summary) throws BusinessException {
		if (summary == null || summary.getImportantLevel() == null) {
			return 1;
		}
		if (summary.getGovdocType() != null) {// 新公文
			System.out.println("消息通道过滤，通过summary查询 importantLevel:" + summary.getImportantLevel());
			int appEnum = ApplicationCategoryEnum.edocSend.getKey();
			if (summary.getGovdocType() == ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()
					|| summary.getGovdocType() == ApplicationSubCategoryEnum.edoc_shouwen.getKey()) {
				appEnum = ApplicationCategoryEnum.edocRec.getKey();
			}
			EdocMessageFilterParamEnum en = GovdocMessageHelper.getSystemMessageFilterParam(appEnum, summary.getImportantLevel());
			if (en != null) {
				return en.key;
			}
		}
		// 模板协同
		if (summary.getTempleteId() != null) {
			switch (summary.getImportantLevel()) {
			case 1:
				return 4;
			case 2:
				return 5;
			case 3:
				return 6;
			default:
				break;
			}
		}
		return 1;
	}

	/**
	 * 获取流程模板对应的单位
	 * @param defaultAccountId
	 * @param summary
	 * @param templateManager
	 * @return
	 * @throws BusinessException
	 */
	public static Long getFlowPermAccountId(Long defaultAccountId, EdocSummary summary) throws BusinessException {
		Long flowPermAccountId = defaultAccountId;
		Long templeteId = summary.getTempleteId();
		if (templeteId != null) {
			CtpTemplate templete = templateManager.getCtpTemplate(templeteId);
			if (templete != null) {
				flowPermAccountId = templete.getOrgAccountId();
			}
		} else {
			if (summary.getOrgAccountId() != null) {
				flowPermAccountId = summary.getOrgAccountId();
			} else if(summary.getStartMemberId() != null && summary.getStartMemberId().longValue() != 0) {
				V3xOrgMember orgMember = orgManager.getMemberById(summary.getStartMemberId());
				flowPermAccountId = orgMember.getOrgAccountId();
			}
		}
		return flowPermAccountId;
	}

	/**
	 * 发送自动跳过的事件
	 * @param source
	 */
	@Deprecated  
	public static void fireAutoSkipEvent(Object source) {	
		Map<String, String> map = DateSharedWithWorkflowEngineThreadLocal.getRepeatAffairs();
		if (map != null && !map.isEmpty()) {
			java.util.Queue<Map<String, String>> affairsQueue = new LinkedList<Map<String, String>>();
			//获取自动跳过的json串
			String skipJsonString = map.get("skipJsonString");
			JSONArray skipJsonArray = JSON.parseArray(skipJsonString);
			if (skipJsonArray != null && !skipJsonArray.isEmpty()) {
				for (Object obj : skipJsonArray) {
					JSONObject beanObj = (JSONObject) obj;
					Map<String, String> dataMap = GovdocUtil.cloneMap(map);
					dataMap.put("_affairIds", beanObj.getString("affairId"));
					dataMap.put("_skipAgentId", beanObj.getString("skipAgentId"));
					affairsQueue.offer(dataMap);
				}
			}
			CollaborationAutoSkipEvent event = new CollaborationAutoSkipEvent(source);
			event.setDataQueues(affairsQueue);
			EventDispatcher.fireEventAfterCommit(event);
		}
	}
	
	public static void fireAutoSkipEvent(Object source, Map<String, String> map) {
		if (map != null && !map.isEmpty()) {
			java.util.Queue<Map<String, String>> affairsQueue = new LinkedList<Map<String, String>>();
			//获取自动跳过的json串
			String skipJsonString = map.get("skipJsonString");
			List<Map<String,String>> skipJsonArray = (List<Map<String, String>>) JSONUtil.parseJSONString(skipJsonString);
			if (skipJsonArray != null && !skipJsonArray.isEmpty()) {
				for (Map<String,String> beanObj : skipJsonArray) {
					Map<String, String> dataMap = cloneMap(map);
					dataMap.put("_affairIds", beanObj.get("affairId"));
					dataMap.put("_skipAgentId", beanObj.get("skipAgentId"));
					dataMap.put("mergeDealType", beanObj.get("mergeDealType"));
					dataMap.put("_commentId", beanObj.get("_commentId"));
					affairsQueue.offer(dataMap);
				}
			}
			CollaborationAutoSkipEvent event = new CollaborationAutoSkipEvent(source);
			event.setDataQueues(affairsQueue);
			EventDispatcher.fireEventAfterCommit(event);
		}		
	}
	
	public static boolean checkAgent(CtpAffair affair, boolean isWebAlert) throws BusinessException {
		try {
			return WFComponentUtil.checkAgent(affair.getMemberId(), affair.getSubject(), ModuleType.edoc, isWebAlert, AppContext.getRawRequest());
		} catch(Exception e) {
			throw new BusinessException(e);
		}
	}
	

	/**
	 * 判断是否给代理人发送消息.可能已经取消代理，或者代理过期了，这种情况就不发消息了
	 * @param affairMemberId : affair的memberID
	 * @param affairTransactorId : affair.TransactorId affair的代理人的ID
	 * @return
	 */
	public static boolean isGovdocProxy(Long affairMemberId, Long affairTransactorId) {
		// 我设置了XX给我干活，返回他的Id
		Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.ordinal(), affairMemberId);
		if (agentId != null && agentId.equals(affairTransactorId)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 重新更新当前待办人信息
	 * @param summary
	 * @param isUpdateToDB 是否直接更新到数据库
	 * @throws BusinessException
	 */
	@SuppressWarnings("unused")
	public static void updateCurrentNodesInfo(EdocSummary summary, boolean isUpdateToDb) throws BusinessException {
		List<Integer> states = new ArrayList<Integer>();
		states.add(StateEnum.col_pending.key());
		states.add(StateEnum.col_pending_repeat_auto_deal.getKey());
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objectId", summary.getId());
		map.put("state", states);
		map.put("delete", false);
		List<CtpAffair> affairs = null;
		if(summary.getExtraDataContainer().get(GovdocHelper._IsNewGovdoc) != null){
			affairs = DateSharedWithWorkflowEngineThreadLocal.getWorkflowAssignedAllAffairs();
		}else{
			affairs = affairManager.getByConditions(null, map);
		}
		String currentNode = "";
		String controlNode = "";
		if (!affairs.isEmpty()) {
			int count = 0;
			String current = "";
			// 最多只存10个待办人
			for (int k = 0; k < affairs.size() && count < 20; k++) {
				CtpAffair cf = affairs.get(k);
				String policy = cf.getNodePolicy();
				// 知会节点不算待办
				if (Strings.isNotBlank(policy) && !"inform".equals(policy)) {
					long memberId = cf.getMemberId();
					if (current.indexOf(memberId + "") != -1) {// 一个人只需要显示一次
						continue;
					}
					if (k != affairs.size() - 1) {
						current += cf.getMemberId() + ";";
					} else {
						current += cf.getMemberId();
					}
					count++;
				}
			}
			summary.setCurrentNodesInfo(current);
			if (isUpdateToDb) {
				govdocSummaryManager.updateEdocSummary(summary,false);
			}
		}
	}
	
	/**
	 * 修改当前待办人-从缓存中获取affairList
	 * @param summary
	 * @param needRemoveCurrentMemberId
	 * @throws BusinessException
	 */
	public static void updateCurrentNodesInfoFromCache(EdocSummary summary, Long needRemoveCurrentMemberId) throws BusinessException {
		List<CtpAffair> affairs = DateSharedWithWorkflowEngineThreadLocal.getWorkflowAssignedAllAffairs();
		int count = 0;
		List<String> _newCurrentInfos =  new ArrayList<String>();
		if (Strings.isNotEmpty(affairs)) {
			// 最多只存10个待办人
			for (int k = 0; k < affairs.size() && count < CURRENT_NODES_INFO_SIZE; k++) {
				CtpAffair cf = affairs.get(k);
				String policy = cf.getNodePolicy();
				// 知会节点不算待办
				if (Strings.isNotBlank(policy) && !"inform".equals(policy)) {
					long memberId = cf.getMemberId();
					_newCurrentInfos.add(String.valueOf(memberId));
					count++;
				}
			}
		}
		//如果已经新生成的》2了，就不用解析老的了，性能优化
		String oldInfos = summary.getCurrentNodesInfo();
		if(count < CURRENT_NODES_INFO_SIZE && oldInfos != null){
		    int leftCount = CURRENT_NODES_INFO_SIZE - count;
			String[] _oldInfos = oldInfos.split("[;]");
			String oldMenberId = String.valueOf(needRemoveCurrentMemberId);
			for(int i = 0;i < _oldInfos.length && leftCount > 0; i++){
			    String s = _oldInfos[i];
			    if(!s.equals(oldMenberId)){
			        _newCurrentInfos.add(s);
			        leftCount--;
			    }
			}
		}
		
		if(_newCurrentInfos.size() != 0){
		    String __str  = Strings.join(_newCurrentInfos, ";");
	        //if(Strings.isBlank(__str)){
	            LOGGER.info("协同id="+summary.getId()+",时间：+"+new Date());
	        //}
	        summary.setCurrentNodesInfo(__str);
		} else {
		    updateCurrentNodesInfo(summary, false);
		}
		/*移除线程变量*/
		DateSharedWithWorkflowEngineThreadLocal.removeWorkflowAssignedAllAffairs();
	}
	
	/**
     * 设置Summary的运行时长，超时时长，按工作时间设置的运行时长，按工作时间设置的超时时长。
     * @param affair
     */
    public static void setTime2Summary(EdocSummary summary) throws BusinessException {
        if(summary == null){
            return ;
        }
        //工作日计算运行时间和超期时间。
        Long orgAccountId = summary.getOrgAccountId();
        Date startDate = summary.getCreateTime();
        Long deadLine = summary.getDeadline();
        long runWorkTime = workTimeManager.getDealWithTimeValue(startDate,new Date(),orgAccountId);
        runWorkTime = runWorkTime/(60*1000);
        Long workDeadline = workTimeManager.convert2WorkTime(deadLine, orgAccountId);
        //超期工作时间
        Long overWorkTime = 0L;
        //设置了处理期限才进行计算,没有设置处理期限的话,默认为0;
        if(workDeadline!=null&&workDeadline!=0){
            long ow = runWorkTime - workDeadline;
            overWorkTime =  ow >0 ? ow: null ;
        }
        //自然日计算运行时间和超期时间
        Long runTime = (System.currentTimeMillis() - startDate.getTime())/(60*1000);
        Long overTime = 0L;
        if( deadLine!= null &&  deadLine!=0){
            Long o = runTime - deadLine;
            overTime = o >0 ? o : null;
        }
        summary.setOverTime(overTime);
        summary.setOverWorkTime(overWorkTime);
        summary.setRunTime(runTime);
        summary.setRunWorkTime(runWorkTime);
    }
    
    
	public static List<AgentModel> getEdocAgentList(Long memberId) {
		List<AgentModel> edocAgentList = new ArrayList<AgentModel>();
		List<AgentModel> agentModelList = MemberAgentBean.getInstance().getAgentModelList(memberId);
		if(Strings.isNotEmpty(agentModelList)) {
			Date currentDate = DateUtil.currentDate();
			for(AgentModel agent : agentModelList) {
				if(agent.isHasEdoc()) {
					if(agent.getStartDate().before(currentDate) && agent.getEndDate().after(currentDate)) {
						edocAgentList.add(agent);
					}
				}
			}
		}
		return edocAgentList;
	}    

	public static boolean isParentColTemplete(Long templeteId) throws BusinessException {
		CtpTemplate t = getParentSystemTemplete(templeteId);
		if (t == null)
			return false;
		if (TemplateEnum.Type.template.name().equals(t.getType()))
			return true;
		else
			return false;
	}
	public static boolean isParentTextTemplete(Long templeteId) throws BusinessException {
		CtpTemplate t = getParentSystemTemplete(templeteId);
		if (t == null)
			return false;
		if (TemplateEnum.Type.text.name().equals(t.getType()))
			return true;
		else
			return false;
	}
	public static boolean isParentWrokFlowTemplete(Long templeteId) throws BusinessException {
		CtpTemplate t = getParentSystemTemplete(templeteId);
		if (t == null)
			return false;
		if (TemplateEnum.Type.workflow.name().equals(t.getType()))
			return true;
		else
			return false;
	}
	public static boolean isParentSystemTemplete(Long templeteId) throws BusinessException {
		CtpTemplate t = getParentSystemTemplete(templeteId);
		if (t == null)
			return false;
		if (t.isSystem())
			return true;
		else
			return false;
	}

	/**
	 * 得到父级系统模板
	 *
	 * @param templeteId
	 * @return
	 * @throws BusinessException
	 */
	public static CtpTemplate getParentSystemTemplete(Long templeteId) throws BusinessException {
		if (templeteId == null) {
			return null;
		}
		boolean needQueryParent = true;
		CtpTemplate t = null;
		while (needQueryParent) {
			t = templateManager.getCtpTemplate(templeteId);
			if (t == null) {
				needQueryParent = false;
				return null;
			}
			if (t.isSystem()) {
				needQueryParent = false;
				return t;
			}
			if (t.getFormParentid() == null) {
				needQueryParent = false;
				return null;
			}
			templeteId = t.getFormParentid();
		}
		return t;
	}
	
    /**
     * 设置Summary的超期时间
     * @param summary
     * @throws BusinessException
     */
    public static void setDeadlineData(EdocSummary summary) throws BusinessException {
	    //计算流程超期时间。把时间段换成具体时间点
	    if(summary.getDeadline() != null) {
	    	Map<String,String> params = new HashMap<String,String>();
	    	params.put("minutes", summary.getDeadline() == null ? "0":String.valueOf(summary.getDeadline()) );
	    	//交换的时候需要设置当前人员
	    	createCurrentUser(summary.getStartMemberId());
	    	Long deadlineDatetime = calculateWorkDatetime(params, summary.getOrgAccountId());
	    	Date date = new Date(deadlineDatetime);
	        String format2 = Datetimes.formatDatetimeWithoutSecond(date);
	        summary.setDeadlineDatetime(Datetimes.parseDatetime(String.valueOf(format2)));
	    	if(summary.getDeadline() == null ||  (null != summary.getDeadline() && summary.getDeadline() == 0l)){
	    		summary.setDeadlineDatetime(null);
	    	}
	    }
	    if(null == summary.getDeadlineDatetime() && null == summary.getDeadline()) {
        	summary.setAdvanceRemind(null);
        }
    }

    /**
	 * 获取指定分钟数后的日期
	 * @param minutes
	 * @return
	 */
	public static Long calculateWorkDatetime(Map<String, String> params,Long currentAccuntId) throws BusinessException {
		Date newDate = new Date();
		int m = 0;
		String strMinutes = params.get("minutes");
		if(strMinutes != null){
		    m = Integer.valueOf(params.get("minutes"));
		}
		String datetime = params.get("datetime");
		if (Strings.isNotBlank(datetime)) {
			Date fromDate = Datetimes.parseDatetimeWithoutSecond(datetime);
			newDate = workTimeManager.getRemindDate(fromDate, m);
		}
		else {
			int minutes = m;
			
			//自定义时间的计算
			if(Strings.isNotBlank(params.get("isCustomMinute"))) {
			    return workTimeManager.getCompleteDate4Worktime(new Date(), m, currentAccuntId).getTime();
			}
			int workDayCount = workTimeManager.getWorkDaysByWeek();
			//如果未设置工作日，按照自然日计算
			if (workDayCount == 0) {
			    workDayCount = 7;
			}

			switch (minutes) {
			case 0:
			case 5:
			case 10:
			case 15:
			case 30:
				newDate = workTimeManager.getCompleteDate4Worktime(new Date(), m, currentAccuntId);
				break;
			case 60:// 1小时
			case 120:// 2小时
			case 180:// 3小时
			case 240:// 4小时
			case 300:// 5小时
			case 360:// 6小时
			case 420:// 7小时
			case 480:// 8小时
			case 720:// 0.5天
				long hours = Long.valueOf(m / 60);
				newDate = workTimeManager.getComputeDate(new Date(), "+", hours, "hour", currentAccuntId);
				break;
			case 1440:// 1天
				newDate = workTimeManager.getComputeDate(new Date(), "+", 1, "day", currentAccuntId);
				break;
			case 2880:// 2天
				newDate = workTimeManager.getComputeDate(new Date(), "+", 2, "day", currentAccuntId);
				break;
			case 4320:// 3天
				newDate = workTimeManager.getComputeDate(new Date(), "+", 3, "day", currentAccuntId);
				break;
			case 5760:// 4天
				newDate = workTimeManager.getComputeDate(new Date(), "+", 4, "day", currentAccuntId);
				break;
			case 7200:// 5天
				newDate = workTimeManager.getComputeDate(new Date(), "+", 5, "day", currentAccuntId);
				break;
			case 8640:// 6天
				newDate = workTimeManager.getComputeDate(new Date(), "+", 6, "day", currentAccuntId);
				break;
			case 14400:// 10天
				newDate = workTimeManager.getComputeDate(new Date(), "+", 10, "day", currentAccuntId);
				break;
			case 10080:// 1周
				newDate = workTimeManager.getComputeDate(new Date(), "+", workDayCount, "day", currentAccuntId);
				break;
			case 20160:// 2周
				newDate = workTimeManager.getComputeDate(new Date(), "+", Long.valueOf(workDayCount * 2L), "day", currentAccuntId);
				break;
			case 21600:// 半个月
				newDate = workTimeManager.getComputeDate(new Date(), "+", 15, "day", currentAccuntId);
				break;
			case 30240:// 3周
				newDate = workTimeManager.getComputeDate(new Date(), "+", Long.valueOf(workDayCount * 3L), "day", currentAccuntId);
				break;
			case 43200:// 1个月
				newDate = workTimeManager.getComputeDate(new Date(), "+", 30, "day", currentAccuntId);
				break;
			case 86400:// 2个月
				newDate = workTimeManager.getComputeDate(new Date(), "+", 60, "day", currentAccuntId);
				break;
			case 129600:// 3个月
				newDate = workTimeManager.getComputeDate(new Date(), "+", 90, "day", currentAccuntId);
				break;
			default:
				newDate = workTimeManager.getComputeDate(new Date(), "+", workDayCount, "day", currentAccuntId);
				break;
			}
		}
		return newDate.getTime();
	}
	
	/**
	 * 取得模板预归档显示明细的RightId
	 * @param template
	 * @return
	 * @throws BusinessException
	 */
	public static String findRightIdByTemplate(CtpTemplate template) throws BusinessException{
		String rightId = "-1";
		if(template !=null){
			EdocSummary tsummary =  (EdocSummary)XMLCoder.decoder(template.getSummary());
			if(null != tsummary.getArchiveId()){
				String s = (String)tsummary.getExtraMap().get("archiverFormid");
				if(Strings.isNotBlank(s)){
					if(s.indexOf("null") < 0) {//公文单模板归档视图，原设置为文单视图id.null，现没有设置视图编辑权限的入口了，默认为第一个，升级上来的数据rightId设置为-1可查看
						if(s.endsWith("|")){
							s = s.substring(0,s.length()-1);
						}
						rightId = s;
					}
				}
			}
		}
		rightId =rightId==null ? null : rightId.replaceAll("[|]","_");
		return rightId;
	}
	
	public static EdocBody getFirstBody(Long summaryId) {
		try {
			return govdocSummaryManager.getFirstBody(summaryId);
		} catch(Exception e) {
			LOGGER.error("获取老公文firstBody出错", e);
			return null;
		}
	}
	
	public static List<EdocBody> getEdocBodys(Long summaryId) {
		try {
			return govdocSummaryManager.getEdocBodys(summaryId);
		} catch(Exception e) {
			return null;
		}
	}	

	public static String getEdocAttSizeAndAttDocSize(Long edocId) {
		int attSize = 0;
		int attDocSize = 0;
		List<Attachment> atts = attachmentManager.getByReference(edocId);
		if(Strings.isNotEmpty(atts)){
			for(Attachment att : atts){
				if(att.getType() == 0){
					attSize++;
				}else if(att.getType() == 2){
					attDocSize++;
				}
			}
		}
		return attSize + ","+attDocSize;
	}
	
	public static boolean isSpecialBacked(int subState) {
		return subState == SubStateEnum.col_pending_specialBacked.getKey()  || subState == SubStateEnum.col_pending_specialBackToSenderCancel.getKey();
	}
	
	public static boolean isAffairBacked(CtpAffair affair) {
		return affair.getState()==StateEnum.col_pending.getKey() &&
				(affair.getSubState() == SubStateEnum.col_pending_specialBack.getKey()
				 	||affair.getSubState() == SubStateEnum.col_pending_specialBackToSenderCancel.getKey());
	}
	
	/**
	 * 是否指定回退
	 * @param affair
	 * @return
	 */
	public static boolean isAffairAppointBacked(CtpAffair affair) {
		return SubStateEnum.col_pending_specialBack.getKey() == affair.getSubState()
				|| SubStateEnum.col_pending_specialBacked.getKey() == affair.getSubState()
				|| SubStateEnum.col_pending_specialBackCenter.getKey() == affair.getSubState();	
	}
	
	
	public static boolean isWaitAffairNotBacked(CtpAffair affair) {
		return Integer.valueOf(StateEnum.col_waitSend.getKey()).equals(affair.getState()) && SubStateEnum.col_pending_specialBacked.getKey() != affair.getSubState();
	}
	
	/**
	 * 公文打开不验证Affair是否删除
	 * @param openFrom
	 * @return
	 */
	public static boolean isNotCheckAffairDeleteOfOpenFrom(String openFrom) {
		return openFrom.equals(EdocOpenFrom.docLib.name()) 
				|| openFrom.equals(EdocOpenFrom.supervise.name())
				|| openFrom.equals(EdocOpenFrom.stepBackRecord.name()) 
				|| openFrom.equals(EdocOpenFrom.glwd.name())
				|| openFrom.equals(EdocOpenFrom.lenPotent.name())
				|| openFrom.equals(EdocOpenFrom.F8Reprot.name());
	}
	
	/**
	 * 公文打开不验证Affair是否有效
	 * @param openFrom
	 * @return
	 */
	public static boolean isNotCheckAffairValidOfOpenFrom(String openFrom) {
		return openFrom.equals(EdocOpenFrom.repealRecord.name()) 
				|| openFrom.equals(EdocOpenFrom.stepBackRecord.name())
				|| openFrom.equals(EdocOpenFrom.exchangeFallback.name()) 
				|| openFrom.equals(EdocOpenFrom.glwd.name());
	}
	
	/**
	 * 公文查看状态是否为只读
	 * @param openFrom
	 * @return
	 */
	public static boolean isReadonlyOfOpenFrom(String openFrom) {
		return EdocOpenFrom.docLib.name().equals(openFrom)
				|| EdocOpenFrom.favorite.name().equals(openFrom)
				|| EdocOpenFrom.F8Reprot.name().equals(openFrom)
				|| EdocOpenFrom.formStatistical.name().equals(openFrom)
				|| EdocOpenFrom.formQuery.name().equals(openFrom)
				|| EdocOpenFrom.formRelation.name().equals(openFrom)
				|| EdocOpenFrom.glwd.name().equals(openFrom);
	}
	
	/**
	 * 公文是否节点权限显示正文
	 * @param openFrom
	 * @return
	 */
	public static boolean isCheckShowContentByGovdocNodePropertyConfigOfOpenFrom(String openFrom) {
		return "listSent".equals(openFrom) 
				|| "listWaitSend".equals(openFrom) 
				|| "formQuery".equals(openFrom)
				|| "edocStatics".equals(openFrom) 
				|| EdocOpenFrom.exchangeRelation.name().equals(openFrom);
	}
	
	/**
	 * 公文查看是否开自文档中心
	 * @param summaryVO
	 * @return
	 */
	public static boolean isSummaryFromDoc(GovdocSummaryVO summaryVO) {
		return Strings.isNotBlank(summaryVO.getLenPotent()) && (EdocOpenFrom.docLib.name().equals(summaryVO.getOpenFrom())) || "lenPotent".equals(summaryVO.getOpenFrom());
	}
	
	/**
	 * 节点权限是否支持续办
	 * @param nodePolicy
	 * @return
	 */
	public static boolean isNodePolicyShowCustomDealWith(NodePolicy nodePolicy) {
		return (Strings.isNotBlank(nodePolicy.getPermissionRange()) || Strings.isNotBlank(nodePolicy.getPermissionRange1()));
	}
	
	public static boolean isOldEdoc(int subApp) {
		return subApp==ApplicationSubCategoryEnum.old_edocSend.key() 
				|| subApp==ApplicationSubCategoryEnum.old_edocSend.key() 
				|| subApp==ApplicationSubCategoryEnum.old_edocSend.key();
	}
	
	/**
	 * 判断一种内容类型的名称是否需要国际化，即是否预置类型 
	 */
	public static boolean needI18n(long type) {
		Set<Long> types = new HashSet<Long>();
		types.add(DocConstants.FOLDER_MINE);
		types.add(DocConstants.FOLDER_CORP);
		types.add(DocConstants.ROOT_ARC);
		types.add(DocConstants.FOLDER_ARC_PRE);
		types.add(DocConstants.FOLDER_PROJECT_ROOT);		
		types.add(DocConstants.FOLDER_PLAN);
		types.add(DocConstants.FOLDER_TEMPLET);
		types.add(DocConstants.FOLDER_SHARE);
		types.add(DocConstants.FOLDER_BORROW);
		types.add(DocConstants.FOLDER_PLAN_DAY);
		types.add(DocConstants.FOLDER_PLAN_MONTH);
		types.add(DocConstants.FOLDER_PLAN_WEEK);
		types.add(DocConstants.FOLDER_PLAN_WORK);
		types.add(DocConstants.DEPARTMENT_BORROW);
		types.add(DocConstants.ROOT_GROUP);
		types.add(DocConstants.FOLDER_EDOC);		
		
		return (types.contains(type));		
	}
	
	public static User createCurrentUser(Long memeberId) throws BusinessException {
		V3xOrgMember currentMember = orgManager.getMemberById(memeberId);
		User user = new User();
		user.setId(currentMember.getId());
		user.setDepartmentId(currentMember.getOrgDepartmentId());
		user.setAccountId(currentMember.getOrgAccountId());
		user.setLoginAccount(currentMember.getOrgAccountId());
		user.setLoginName(currentMember.getLoginName());
		user.setName(currentMember.getName());
		user.setLocale(AppContext.getLocale());
		AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY, user);
		return user;
	}
	
	public static int getWorkDayCount(Timestamp start, Timestamp endt) throws Exception {
		Date begDate = start;
		Date endDate = endt;
        if (begDate.after(endDate)) {
        	throw new Exception("日期范围非法");
        }
         // 总天数 
        int days = (int) ((endDate.getTime() - begDate.getTime()) / (24 * 60 * 60 * 1000)) + 1; 
        // 总周数， 
        int weeks = days / 7; 
        int rs = 0; 
        // 整数周 
        if (days % 7 == 0) { 
        	rs = days - 2 * weeks; 
        } else { 
        	Calendar begCalendar = Calendar.getInstance(); 
        	Calendar endCalendar = Calendar.getInstance(); 
        	begCalendar.setTime(begDate); 
        	endCalendar.setTime(endDate); 
        	// 周日为1，周六为7 
        	int beg = begCalendar.get(Calendar.DAY_OF_WEEK); 
        	int end = endCalendar.get(Calendar.DAY_OF_WEEK); 
        	if (beg > end) { 
        		rs = days - 2 * (weeks + 1); 
        	} else if (beg < end) { 
        		if (end == 7) { 
        			rs = days - 2 * weeks - 1; 
        		} else {  
        			rs = days - 2 * weeks; 
        		} 
        	} else { 
        		if (beg == 1 || beg == 7) { 
        			rs = days - 2 * weeks - 1; 
        		} else { 
        			rs = days - 2 * weeks; 
        		} 
        	} 
        } 
        return rs;
	}

	

	private static Map<String,String> cloneMap(Map<String,String> sourceMap){
		if(sourceMap == null){
			return null;
		}
		Set<String> keys = sourceMap.keySet();
		Map<String,String> newMap = new HashMap<String,String>();
		for(String key :keys){
			newMap.put(key,sourceMap.get(key));
		}
		return newMap;
	}
	
	public static void fillSummaryVoByAttitude(Permission permission) {
		// 指定回退再处理的流转方式 show1 重新流转 show2 提交回退者
		boolean show1 = true;
		boolean show2 = true;
  		if (permission != null) {
			String attitude = permission.getNodePolicy().getAttitude();
			CustomAction customAction = permission.getNodePolicy().getCustomAction();
	        if (customAction != null) {
	        	String isOptional = customAction.getIsOptional();
	        	String optionalAction = customAction.getOptionalAction();
	        	String defaultAction = customAction.getDefaultAction();
	        	AppContext.putRequestContext("isOptional", isOptional);
	        	AppContext.putRequestContext("optionalAction", optionalAction);
	        	if("-1".equals(defaultAction)){
	        		defaultAction = "Return";
	        	}else if("-2".equals(defaultAction)){
	        		defaultAction = "SpecifiesReturn";
	        	}
	        	AppContext.putRequestContext("defaultAction", defaultAction);
	        	
	        }
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
        		AppContext.putRequestContext("attitudeNum", attitudeList.size());
        		AppContext.putRequestContext("attitudeList", attitudeList);
        		AppContext.putRequestContext("defaultAttitude", defaultAttitude);
        	}
			AppContext.putRequestContext("nodeattitude", attitude);			
			AppContext.putRequestContext("permissionId", permission.getFlowPermId());
			Integer submitStyle = permission.getNodePolicy().getSubmitStyle();
			if (submitStyle != null) {
				switch (submitStyle) {
				case 0:
					show1 = true;
					show2 = false;
					break;
				case 1:
					show1 = false;
					show2 = true;
					break;
				}
			}
			AppContext.putRequestContext("canDeleteORarchive", permission.getNodePolicy().getOpinionPolicy() != null && permission.getNodePolicy().getOpinionPolicy() == 1);
			AppContext.putRequestContext("cancelOpinionPolicy", permission.getNodePolicy().getCancelOpinionPolicy());
			AppContext.putRequestContext("disAgreeOpinionPolicy", permission.getNodePolicy().getDisAgreeOpinionPolicy());
		}
		AppContext.putRequestContext("show1", show1);
		AppContext.putRequestContext("show2", show2);
	}
	
	public static boolean isA6() {
		String systemVer = AppContext.getSystemProperty("system.ProductId");
		// A6没有收藏功能。相对就没有权限
		boolean isA6 = false;
		if ("0".equals(systemVer) || "7".equals(systemVer)) {
			isA6 = true;
		}
		return isA6;
	}
	
}
