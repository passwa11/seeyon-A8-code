package com.seeyon.apps.govdoc.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.apps.agent.bo.AgentDetailModel;
import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.doc.bo.DocResourceBO;
import com.seeyon.apps.govdoc.po.EdocLeaderPishiNo;
import com.seeyon.apps.govdoc.util.GovdocParamUtil;
import com.seeyon.apps.govdoc.vo.GovdocListVO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.enums.AffairExtPropEnums;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.taglibs.functions.Functions;
import com.seeyon.v3x.worktimeset.exception.WorkTimeSetExecption;

/**
 * 公文列表辅助类
 */
public class GovdocListHelper extends GovdocHelper {
	
	private static final String  PROXYSTR= ResourceUtil.getString("collaboration.proxy");
	
	/**
	 * 用于公文查询设置自定义条件
	 * @return
	 * @throws BusinessException
	 */
	public static List<String> getQueryConditionList() throws BusinessException {
		List<String> list = new ArrayList<String>();
		list.add("subject");
		list.add("serial_no");
		list.add("doc_mark");
		list.add("send_type");
		list.add("doc_type");
		list.add("create_person");
		list.add("createdate");
		list.add("send_unit");
		list.add("send_to");
		list.add("send_department");
		list.add("issuer");
		list.add("signing_date");
		list.add("send_department");
		list.add("archiveId");
		list.add("archiveName");
		list.add("copy_to");
		list.add("report_to");
		list.add("receive_unit");
		list.add("baopiyijian");
		list.add("feedback");
		return list;
	}
	
	/**
	 * 公文自定义查询时设置列表数据
	 * @param name
	 * @param vo
	 * @return
	 */
	public static String getListColumnValue(String name, GovdocListVO vo) {
		String value = ""; 
		if("subject".equals(name)) {
			value = vo.getSubject();
		} else if("docMark".equals(name)) {
			value = vo.getDocMark();
		}else if("registerDate".equals(name)) {
			if(vo.getCreateTime() != null) {
				value = Datetimes.format(vo.getCreateTime(), "yyy-MM-dd HH:mm");
			}
		}else if("serialNo".equals(name)) {
			value = vo.getSerialNo();
		} else if("sendunit".equals(name.toLowerCase())) {
			value = vo.getSendUnit();
		} else if("sendDepartment".equals(name)) {
			value = vo.getSendDepartment();
		} else if("startTime".equals(name)) {
			if(vo.getStartTime() != null) {
				value = Datetimes.format(vo.getStartTime(), "yyy-MM-dd");
			}
		} else if("currentNodesInfo".equals(name)) {
			value = vo.getCurrentNodesInfo();
		} else if("sendTo".equals(name)) {
			value = vo.getSendTo();
		} else if("copyTo".equals(name)) {
			value = vo.getCopyTo();
		} else if("doctype".equals(name.toLowerCase())) {
			value = vo.getDocType();
		} else if("sendtype".equals(name.toLowerCase())) {
			value = vo.getSendType();
		} else if("unitlevel".equals(name.toLowerCase())) {
			value = vo.getUnitLevel();
		} else if("secretlevel".equals(name.toLowerCase())) {
			value = vo.getSecretLevel();
		} else if("keepperiodtxt".equals(name.toLowerCase())) {
			value = vo.getKeepPeriodTxt();
		} else if("urgentlevel".equals(name.toLowerCase())) {
			value = vo.getUrgentLevel();
		} else if("issuer".equals(name)) {
			value = vo.getIssuer();
		} else if("signingDate".equals(name)) {
			if(vo.getSigningDate() != null) {
				value = Datetimes.format(vo.getSigningDate(), "yyy-MM-dd");
			}
		} else if("packTime".equals(name)) {
			if(vo.getPackTime() != null) {
				value = Datetimes.format(vo.getPackTime(), "yyy-MM-dd");
			}
		} else if("createPerson".equals(name)) {
			value = vo.getCreatePerson();
		} else if("transferStatus".equals(name) || "summaryTransferStatusName".equals(name)) {
			value = vo.getSummaryTransferStatusName();
		} else if("review".equals(name)) {
			value = vo.getReview();
		} else if("copies".equals(name)) {
			if(vo.getCopies() != null) {
				value = vo.getCopies() + "";
			}
		} else if("archiveId".equals(name)) {
			if(vo.getHasArchive() != null) {
				if(vo.getHasArchive()) {
	    			vo.setHasArchiveTxt(ResourceUtil.getString("govdoc.canArchive.label.yes"));
	    		} else {
	    			vo.setHasArchiveTxt(ResourceUtil.getString("govdoc.canArchive.label.no"));
	    		}
				value = vo.getHasArchiveTxt();
			}
		} else if("archiveName".equals(name)) {
			if(Strings.isNotBlank(vo.getArchiveName())) {
				value = vo.getArchiveName();
			}
		} else if("createTime".equals(name)) {//添加字段值判断，回写
			if(vo.getCreateTime() != null) {
				value = Datetimes.format(vo.getCreateTime(), "yyy-MM-dd");
			}
		} else if("receiptDate".equals(name)) {
			if(vo.getReceiptDate() != null) {
				value = Datetimes.format(vo.getReceiptDate(), "yyy-MM-dd");
			}
		} else if("registrationDate".equals(name)) {
			if(vo.getRegistrationDate() != null) {
				value = Datetimes.format(vo.getRegistrationDate(), "yyy-MM-dd");
			}
		} else if("undertaker".equals(name)) {
			value=vo.getUndertaker();
		} else if("signPerson".equals(name)) {
			value=vo.getSignPerson();
		} else if("signMark".equals(name)) {
			value=vo.getSignMark();
		} else if("leaderCommondNo".equals(name)) {
			value = vo.getLeaderCommondNo();
		} else if("keepPeriod".equals(name)) {
			value = vo.getKeepPeriodTxt();
		} else if("undertakenoffice".equals(name)) {
			value = vo.getUndertakenoffice();
		}
		return value;
	}

	/**
	 * 公文自定义查询时，从前端获取查询元素
	 * @param name
	 * @param vo
	 * @return
	 */
	public static List<Map<String, String>> getQueryListColumn(Map<String, String> params) {
		List<Map<String, String>> columns = new ArrayList<Map<String, String>>();
		
		if(Strings.isNotBlank(params.get("customColumns"))) {
			String[] columnArr = params.get("customColumns").split(",");
			String[] displayArr = params.get("customDisplayColumns").split(",");
			Map<String, String> paramMap = null;
			for(int i = 0; i<columnArr.length; i++) {
				paramMap = new HashMap<String, String>();
				paramMap.put("name", columnArr[i]);
				paramMap.put("display", displayArr[i]);
				columns.add(paramMap);
			}
		} else {
			String govdocType = ParamUtil.getString(params, "govdocType", "");
			//文件密级
			Map<String, String> paramMap = new HashMap<String, String>();
			paramMap.put("name", "secretLevel");
			paramMap.put("display", ResourceUtil.getString("govdoc.secretLevel.label"));
			columns.add(paramMap);
			//紧急程度
			paramMap = new HashMap<String, String>();
			paramMap.put("name", "urgentLevel");
			paramMap.put("display", ResourceUtil.getString("govdoc.urgentLevel.label"));
			columns.add(paramMap);
			//保密期限
			paramMap = new HashMap<String, String>();
			paramMap.put("name", "keepPeriodTxt");
			paramMap.put("display", ResourceUtil.getString("govdoc.keepPeriod.label"));
			columns.add(paramMap);
			//公文级别
			paramMap = new HashMap<String, String>();
			paramMap.put("name", "unitLevel");
			paramMap.put("display", ResourceUtil.getString("govdoc.unitLevel.label"));
			columns.add(paramMap);
			//标题
			paramMap = new HashMap<String, String>();
			paramMap.put("name", "subject");
			paramMap.put("display", ResourceUtil.getString("common.subject.label"));
			columns.add(paramMap);
			//文号
			paramMap = new HashMap<String, String>();
			paramMap.put("name", "docMark");
			paramMap.put("display", ResourceUtil.getString("govdoc.docMark.label"));
			columns.add(paramMap);
			if("1".equals(govdocType) || "3".equals(govdocType)) {//发文、签报
				//主送单位
				paramMap = new HashMap<String, String>();
				paramMap.put("name", "sendTo");
				paramMap.put("display", ResourceUtil.getString("edoc.element.sendtounit"));
				columns.add(paramMap);
				//建文人
				paramMap = new HashMap<String, String>();
				paramMap.put("name", "issuer");
				paramMap.put("display", ResourceUtil.getString("edoc.element.issuer"));
				columns.add(paramMap);
				//发文时间
				paramMap = new HashMap<String, String>();
				paramMap.put("name", "signingDate");
				paramMap.put("display", ResourceUtil.getString("edoc.element.sendingdate"));
				columns.add(paramMap);	
			} else if("2,4".equals(govdocType)) {//收文
				//来文单位
				paramMap = new HashMap<String, String>();
				paramMap.put("name", "sendUnit");
				paramMap.put("display", ResourceUtil.getString("edoc.edoctitle.fromUnit.label"));
				columns.add(paramMap);
				//登记人
				paramMap = new HashMap<String, String>();
				paramMap.put("name", "createPerson");
				paramMap.put("display", ResourceUtil.getString("edoc.edoctitle.regPerson.label"));
				columns.add(paramMap);
				//登记日期
				paramMap = new HashMap<String, String>();
				paramMap.put("name", "registerDate");
				paramMap.put("display", ResourceUtil.getString("edoc.edoctitle.regDate.label"));
				columns.add(paramMap);
			}
			//拟稿人
			paramMap = new HashMap<String, String>();
			paramMap.put("name", "createPerson");
			paramMap.put("display", ResourceUtil.getString("edoc.edoctitle.createPerson.label"));
			columns.add(paramMap);
			//公文种类
			paramMap = new HashMap<String, String>();
			paramMap.put("name", "docType");
			paramMap.put("display", ResourceUtil.getString("edoc.element.doctype"));
			columns.add(paramMap);
			//行文类型
			paramMap = new HashMap<String, String>();
			paramMap.put("name", "sendType");
			paramMap.put("display", ResourceUtil.getString("edoc.element.sendtype"));
			columns.add(paramMap);
			//内部文号
			paramMap = new HashMap<String, String>();
			paramMap.put("name", "serialNo");
			paramMap.put("display", ResourceUtil.getString("govdoc.serialNo.label"));
			columns.add(paramMap);
			//发文单位
			paramMap = new HashMap<String, String>();
			paramMap.put("name", "sendUnit");
			paramMap.put("display", ResourceUtil.getString("edoc.element.sendunit"));
			columns.add(paramMap);
			//是否归档
			paramMap = new HashMap<String, String>();
			paramMap.put("name", "archiveId");
			paramMap.put("display", ResourceUtil.getString("govdoc.canArchive.label"));
			columns.add(paramMap);
			//归档路径
			paramMap = new HashMap<String, String>();
			paramMap.put("name", "archiveName");
			paramMap.put("display", ResourceUtil.getString("govdoc.archiveName.label"));
			columns.add(paramMap);
			//抄送份数
			paramMap = new HashMap<String, String>();
			paramMap.put("name", "copies");
			paramMap.put("display", ResourceUtil.getString("govdoc.copies.label"));
			columns.add(paramMap);
		}
		return columns;
	}
	
	/**
	 * 公文查询时从前端Request中获取数据
	 * @param request
	 * @return
	 */
    public static Map<String, String> getConditionsByRequest(HttpServletRequest request) {
    	Map<String, String> params = new HashMap<String, String>();
    	params.put("listType", GovdocParamUtil.getString(request, "listType"));
    	params.put("govdocType", GovdocParamUtil.getString(request, "govdocType"));
    	params.put("statTitle", GovdocParamUtil.getString(request, "statTitle"));
    	params.put("condition", GovdocParamUtil.getString(request, "condition"));
    	params.put("deduplication", GovdocParamUtil.getString(request, "deduplication"));
    	params.put("subject", GovdocParamUtil.getString(request, "subject"));
    	params.put("serialNo", GovdocParamUtil.getString(request, "serialNo"));
    	params.put("createPerson", GovdocParamUtil.getString(request, "createPerson"));
    	params.put("sendUnit", GovdocParamUtil.getString(request, "sendUnit"));
    	params.put("sendDepartment", GovdocParamUtil.getString(request, "sendDepartment"));
    	params.put("sendTo", GovdocParamUtil.getString(request, "sendTo"));
    	params.put("printer", GovdocParamUtil.getString(request, "printer"));
    	params.put("issuer", GovdocParamUtil.getString(request, "issuer"));
    	params.put("review", GovdocParamUtil.getString(request, "review"));
    	params.put("auditor", GovdocParamUtil.getString(request, "auditor"));
    	params.put("signPerson", GovdocParamUtil.getString(request, "signPerson"));
    	params.put("undertaker", GovdocParamUtil.getString(request, "undertaker"));
    	params.put("undertakenoffice", GovdocParamUtil.getString(request, "undertakenoffice"));
    	params.put("printUnit", GovdocParamUtil.getString(request, "printUnit"));
    	params.put("copies", GovdocParamUtil.getString(request, "copies"));
    	params.put("sendType", GovdocParamUtil.getString(request, "sendType"));
    	params.put("docType", GovdocParamUtil.getString(request, "docType"));
    	params.put("keepPeriod", GovdocParamUtil.getString(request, "keepPeriod"));
    	params.put("urgentLevel", GovdocParamUtil.getString(request, "urgentLevel"));
    	params.put("unitLevel", GovdocParamUtil.getString(request, "unitLevel"));
    	params.put("secretLevel", GovdocParamUtil.getString(request, "secretLevel"));
    	params.put("customColumns", GovdocParamUtil.getString(request, "customColumns"));
    	params.put("customDisplayColumns", GovdocParamUtil.getString(request, "customDisplayColumns"));
    	
    	params.put("docMark", GovdocParamUtil.getString(request, "docMark"));	
    	params.put("startTime", GovdocParamUtil.getString(request, "startTime"));
    	params.put("createTime", GovdocParamUtil.getString(request, "createTime"));
    	params.put("signingDate", GovdocParamUtil.getString(request, "signingDate"));
    	params.put("packTime", GovdocParamUtil.getString(request, "packTime"));
    	params.put("recieveDate", GovdocParamUtil.getString(request, "recieveDate"));
    	params.put("registerDate", GovdocParamUtil.getString(request, "registerDate"));
    	
    	return params;
    }
    
    /**
     * 计算节点处理剩余时间
     * @param date 流程到达时间
     * @param expecetProcessTime 节点处理期限（具体时间）
     * @return 剩余时间（不足一天单位为：小时，反之单位为：天）
     */
    @SuppressWarnings("deprecation")
    public static int[] calculateSurplusTime(java.util.Date date,java.util.Date expecetProcessTime) throws BusinessException {
        int[] surplusTime = null;
        User user = AppContext.getCurrentUser();
        try {
            if(expecetProcessTime !=null) {
                long days = 0;
                long hours = 0;
                long minutes = 0;
                // 获取系统当前时间
                java.util.Date nowTime = new java.util.Date();
                // 得到节点处理的最后时间
                //java.util.Date overTime = workTimeManager.getCompleteDate4Nature(date, deadlineDate, user.getAccountId());
                // 未超期
                if(nowTime.before(expecetProcessTime)) {
                    // 得到剩余处理时间（分钟）
                	long surplusMinu = workTimeManager.getDealWithTimeValue(nowTime, expecetProcessTime, user.getAccountId()) / (1000 * 60);
                    // 得到当前单位当月的日工作时间（分钟）
                    long dayOfMinu = workTimeManager.getEachDayWorkTime(nowTime.getYear(), user.getAccountId());
                    if(surplusMinu >= dayOfMinu) {
                        // 天数
                        days = surplusMinu / dayOfMinu;
                        long shenyufen = surplusMinu - days * dayOfMinu;
                        if(shenyufen < 60) {
                            minutes = shenyufen;
                        } else {
                            hours = shenyufen / 60;
                            minutes = shenyufen - hours * 60;
                        }
                    } else if(60 <= surplusMinu && surplusMinu < dayOfMinu) {
                        hours = surplusMinu / 60;
                        minutes = surplusMinu - hours * 60;
                    } else {
                        minutes = surplusMinu;
                    }
                }
                surplusTime = new int[3];
                surplusTime[0]=(int)days;
                surplusTime[1]=(int)hours;
                surplusTime[2]=(int)minutes;
            }
        } catch(WorkTimeSetExecption e) {
            //log.error("计算节点处理剩余时间抛出异常", e);
        }
        return surplusTime;
    }
	
    /**
	 * 列表查询数据-回填代理人
	 * @param vo
	 * @param showProxyInfo4A
	 * @param _userId
	 * @param listType
	 * @throws BusinessException
	 */
	public static void fillListAgent(GovdocListVO vo,boolean showProxyInfo4A,Long _userId,String listType) throws BusinessException{
    	Long userId = _userId;
    	if(_userId == null){
    		userId = AppContext.getCurrentUser().getId();
        }
        /*
         * A设B为代理
         *
         * B登录，获取我的被代理人A的事项，并把这些事项标记为蓝色（只要“事项的MemberId!=我”即可）
         * A登录，仅按照"member=我的id"即可，但要把我代理出去的事项标记为蓝色（事项是否落入到代理条件内）
         *
         * 查询我的代理列表(我为被代理人，我找别人干活)   key: 模板ID,自由协同为AllSelf,全部模板为AllTemplate
         */
        Map<Object, AgentModel> agentToModelColl = new HashMap<Object, AgentModel>();
        if(showProxyInfo4A){ //A登录
        	//查询我的代理人
            List<AgentModel> agentToModelList = MemberAgentBean.getInstance().getAgentModelToList(userId);
            if(!Strings.isEmpty(agentToModelList)){
                for (AgentModel agentTo : agentToModelList) {
                    if(agentTo.getStartDate().before(new Date()) && agentTo.getEndDate().after(new Date())){
                        if(agentTo.isHasCol()){
                        	agentToModelColl.put("AllSelf", agentTo);  //表示自由协同
                        }
                        if(agentTo.isHasEdoc()){
                        	agentToModelColl.put("EdocAllSelf", agentTo);  //表示公文
                        }
                        if(agentTo.isHasTemplate()){
                            if(!Strings.isEmpty(agentTo.getAgentDetail())){
                                for (AgentDetailModel agentDetailModel : agentTo.getAgentDetail()) {
                                    agentToModelColl.put(agentDetailModel.getEntityId(), agentTo);  //指定模板
                                }
                            }
                            else{
                                agentToModelColl.put("AllTemplate", agentTo);  //表示全部模板
                            }
                        }
                    }
                }
            }
            Long templateId = vo.getTempleteId();
            if(!vo.getAffairMemberId().equals(userId)){
            	vo.setProxyName(Functions.showMemberNameOnly(vo.getAffairMemberId()));
            	vo.setProxy(true);
            }
            else if(agentToModelColl != null && !agentToModelColl.isEmpty()){
                Date early = null;
                if(templateId == null && agentToModelColl.get("AllSelf") != null){ //自由协同
                    early = agentToModelColl.get("AllSelf").getStartDate();
                }
                else if(templateId != null && agentToModelColl.get("AllTemplate") != null){
                    early = agentToModelColl.get("AllTemplate").getStartDate();
                }
                else if(templateId != null && agentToModelColl.get(templateId) != null){
                    early = agentToModelColl.get(templateId).getStartDate();
                }
                else if(agentToModelColl.get("EdocAllSelf") != null){
                	early = agentToModelColl.get("EdocAllSelf").getStartDate();
                }
                if(vo.getAffairReceiveTime() != null){//BDGW-2193代理人设置之后，代理人的已办公文报错
                	if(early != null && early.before(vo.getAffairReceiveTime())){
                        vo.setProxy(true);
                    }
                }
            }
            String showAgent = "";
			if(listType.contains("Pending")) {
				showAgent = GovdocListHelper.showToPendingAgent(vo);
			} else if(listType.contains("Done")) {
				showAgent = GovdocListHelper.showToDoneAgent(vo);
			}
			if(!vo.getSubject().contains("代录)")){
	            vo.setSubject(vo.getSubject() + showAgent);
			}
        }
	}
    
    /**
     * 拼接代理标识
     * @param vo
     * @return
     * @throws BusinessException 
     */
    public static String showToPendingAgent(GovdocListVO vo) throws BusinessException{
        if(Strings.isEmpty(vo.getProxyName())){
            return "";
        }
        String colProxyLabel = "";
        if(Boolean.TRUE.equals(vo.getProxy())){
        	//我的代理列表(我代表别人)，我是代理人
            List<AgentModel> _agentModelList = MemberAgentBean.getInstance().getAgentModelList(AppContext.getCurrentUser().getId());
            //代理我的代理人列表，我是被代理人
            List<AgentModel> _agentModelToList = MemberAgentBean.getInstance().getAgentModelToList(AppContext.getCurrentUser().getId());
            boolean agentToFlag = false;
            if(_agentModelList != null && !_agentModelList.isEmpty()){
                agentToFlag = false;
            }else if(_agentModelToList != null && !_agentModelToList.isEmpty()){
                agentToFlag = true;
            }
            if(agentToFlag){
            	//(XX)代理
                colProxyLabel = "(" + vo.getProxyName() + PROXYSTR + ")";
            }else{
                if(vo.getAgentDeal()){      //被代理人自己处理
                	//(XX)处理
                    colProxyLabel = "(" + ResourceUtil.getString("collaboration.proxy.deal", vo.getProxyName()) + ")";
                }else{
                	//add by rz 2017-08-19 [添加代录人权限判断]
                	CtpAffair affair = affairManager.get(vo.getAffairId());
                	Map<String, Object> map = AffairUtil.getExtProperty(affair);
                	if(map.get(AffairExtPropEnums.dailu_pishi_mark.toString()) != null 
                			&& !"".equals(map.get(AffairExtPropEnums.dailu_pishi_mark.toString()))){
                		//(xx)代录
                		colProxyLabel = "(" + vo.getProxyName() +  "代录)";
                	}else{
                		//代理(XX)
                		colProxyLabel = "(" + ResourceUtil.getString("collaboration.proxy") + vo.getProxyName() + ")";
                	}
                }
            }
        }
        return colProxyLabel;
    }
    
    public static String showToDoneAgent(GovdocListVO vo){

        V3xOrgMember member = null;
        String colProxyLabel = "";
        boolean isAgent = true;
        User user = AppContext.getCurrentUser();
        long userId = user.getId();
        if(vo.getAffairTransactorId() != null){//当前登录人就是代理人的时候不需要显示代理信息
            Long memberId = vo.getAffairTransactorId();
            if(memberId.longValue()==userId){
                memberId = vo.getAffairMemberId();
            }else{
                isAgent = false;
            }
            try {
                member = orgManager.getMemberById(memberId);
                if(!member.getIsAdmin()){
                  if(member != null){
                    if(isAgent){
                      //代理(XX)
                      colProxyLabel = "(" + ResourceUtil.getString("collaboration.proxy") + member.getName() + ")";
                    }else {
                    	//add by rz 2017-08-19 [添加代录人权限判断]
                    	CtpAffair affair = affairManager.get(vo.getAffairId());
                    	Map<String, Object> map = AffairUtil.getExtProperty(affair);
                    	if(map.get(AffairExtPropEnums.dailu_pishi_mark.toString()) != null 
                    			&& !"".equals(map.get(AffairExtPropEnums.dailu_pishi_mark.toString()))){
                    		//(xx)代录
                    		colProxyLabel = "(" + member.getName() +  "代录)";
                    	}else{
                    		//(XX)代理
                    		colProxyLabel = "(" + member.getName() + ResourceUtil.getString("collaboration.proxy") + ")";
                    	}
                    }
                  }
                }

            } catch (Exception e) {
            }
          }else if(vo.getAffairMemberId() != userId){//代理人已办查看被代理人处理的
        	try{
    			member = orgManager.getMemberById(vo.getAffairMemberId());
    			//(XX)处理
                colProxyLabel = "(" + ResourceUtil.getString("collaboration.proxy.deal", member.getName()) + ")";
    		}catch(Exception e){
    		}
        }else{
        	vo.setProxy(false);
        }
        return colProxyLabel;
    
    }
    /**
     * @Title: isAllShouWenTemplate
     * @Description: 检查模板是否包含收文模板-用于业务生成器公文模板列表展示
     * @throws BusinessException 
     */
    public static boolean isContainRecTemplate(String templateIds) throws BusinessException {
    	if(Strings.isNotBlank(templateIds)) {
			List<Long> templateIdList = new ArrayList<Long>();
			String[] templateIdArr = templateIds.split(",");
			if(templateIdArr != null) {
				for(int i=0; i<templateIdArr.length; i++) {
					templateIdList.add(Long.parseLong(templateIdArr[i]));
				}
			}
			List<CtpTemplate> templateList=templateManager.getTemplatesByIds(templateIdList);
			for(CtpTemplate template:templateList){
				if(template.getModuleType()!=null && template.getModuleType().intValue()==ApplicationCategoryEnum.govdocRec.getKey()){
					//只要其中有一个收文模板，则返回true
					return true;
				}
			}
		}
    	return false;
    }
    
    /**
     * 设置列表的归档显示值
     * @param listVo
     * @throws BusinessException
     */
    public static void fillListArchiveData(GovdocListVO listVo) throws BusinessException {
    	List<DocResourceBO> docs = docApi.findDocResourcesBySourceId(listVo.getAffairId());
   		if(Strings.isNotEmpty(docs)) {
   			String frName = "";
   			for(DocResourceBO bo : docs){
   				bo = docApi.getDocResource(bo.getParentFrId());      				
   				String tempFrName=bo.getFrName();
   				//获取完整文字路径
   				String fullPath= docApi.getPhysicalPath(bo.getLogicalPath(), "\\", false, 0);
   				if (GovdocHelper.needI18n(bo.getFrType())){
   					tempFrName = ResourceUtil.getString(tempFrName);
   				}
   				
   				if(bo.getLogicalPath()!=null && bo.getLogicalPath().split("\\.").length>1){
   					tempFrName=fullPath;
   				}
   				frName = frName + "|"+tempFrName;
   			}
   			listVo.setArchiveName(frName.length() > 1 ? frName.substring(1) : "");
   		} else {
   			listVo.setHasArchive(false);
   		}
    }
    
    /**
     * 设置列表枚举显示值
     * @param listVo
     * @param docType
     * @param sendType
     * @param keepPeriod
     * @param secretLevel
     * @param urgentLevel
     * @param unitLevel
     */
    public static void fillListEnumLabel(GovdocListVO listVo, 
    		CtpEnumBean docType, CtpEnumBean sendType, CtpEnumBean keepPeriod, 
    		CtpEnumBean secretLevel,  CtpEnumBean urgentLevel, CtpEnumBean unitLevel) {
    	//公文种类
        if(Strings.isNotBlank(listVo.getDocType())) {
        	for(com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem enmu : docType.getItems()) {
        		if(listVo.getDocType().equals(enmu.getEnumvalue())) {
        			listVo.setDocType(ResourceUtil.getString(enmu.getShowvalue()));
        			break;
        		}
        	}
		}
        //行文类型
        if(Strings.isNotBlank(listVo.getSendType())) {
        	for(com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem enmu : sendType.getItems()) {
        		if(listVo.getSendType().equals(enmu.getEnumvalue())) {
        			listVo.setSendType(ResourceUtil.getString(enmu.getShowvalue()));
        			break;
        		}
        	}
		}
        //密级
        if(Strings.isNotBlank(listVo.getSecretLevel())) {
        	for(com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem enmu : secretLevel.getItems()) {
        		if(listVo.getSecretLevel().equals(enmu.getEnumvalue())) {
        			listVo.setSecretLevel(ResourceUtil.getString(enmu.getShowvalue()));
        			break;
        		}
        	}
		}
		//紧急程度
       	if(Strings.isNotBlank(listVo.getUrgentLevel())) {
        	for(com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem enmu : urgentLevel.getItems()) {
    			if(listVo.getUrgentLevel().equals(enmu.getEnumvalue())) {
					 listVo.setUrgentLevel(ResourceUtil.getString(enmu.getShowvalue()));
					 break;
    			}
    		}
        }
       	//公文类别
       	if(Strings.isNotBlank(listVo.getUnitLevel())) {
        	for(com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem enmu : unitLevel.getItems()) {
    			if(listVo.getUnitLevel().equals(enmu.getEnumvalue())) {
					 listVo.setUnitLevel(ResourceUtil.getString(enmu.getShowvalue()));
					 break;
    			}
    		}
        }
       	//保密期限
       	if(listVo.getKeepPeriod() != null) {
        	for(com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem enmu : keepPeriod.getItems()) {
        		if(Strings.isNotEmpty(enmu.getEnumvalue())) {
	    			if(listVo.getKeepPeriod().intValue() == Integer.parseInt(enmu.getEnumvalue())) {
	    				listVo.setKeepPeriodTxt(ResourceUtil.getString(enmu.getShowvalue()));
						 break;
	    			}
        		}
    		}
        }
    }
    
    /**
	 * 获取当前公文的全部批示编号
	 * @param summaryId
	 * @return
	 */
	public static String getAllLeaderPishi(Long summaryId) throws BusinessException {
		String allLeaderPishi="";
		List<EdocLeaderPishiNo> list = govdocPishiManager.getAllLeaderPishi(summaryId);
		if(!list.isEmpty()){
			for(EdocLeaderPishiNo leaderPishiNo:list){
				if(leaderPishiNo.getPishiName() != null && leaderPishiNo.getPishiNo() != null){
					allLeaderPishi+=leaderPishiNo.getPishiName()+" ";
					allLeaderPishi+=leaderPishiNo.getPishiYear()+"年";
					allLeaderPishi+="第"+leaderPishiNo.getPishiNo()+"号";
					allLeaderPishi+="	";
				}
			}
		}
		return allLeaderPishi;
	}
    
}
