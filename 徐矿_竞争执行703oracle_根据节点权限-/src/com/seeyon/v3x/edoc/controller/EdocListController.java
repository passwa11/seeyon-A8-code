package com.seeyon.v3x.edoc.controller;

import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.taglibs.functions.Functions;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.manager.*;
import com.seeyon.v3x.edoc.webmodel.EdocSearchModel;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryModel;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import redis.clients.jedis.BinaryClient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class EdocListController extends BaseController {

	private static final org.apache.commons.logging.Log LOGGER = LogFactory.getLog(EdocListController.class);

	private OrgManager orgManager;
	private EnumManager enumManagerNew;
	private EdocListManager edocListManager;
	private EdocSummaryManager edocSummaryManager;

	private ModelAndView listEdoc(HttpServletRequest request, HttpServletResponse response, ModelAndView modelAndView, List<EdocSummaryModel> queryList, Map<String, Object> condition) {
		Map<String, CtpEnumBean> colMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.edoc);
		CtpEnumBean attitude = enumManagerNew.getEnum(EnumNameEnum.collaboration_attitude.name()); //处理意见 attitude
        colMetadata.put(EnumNameEnum.collaboration_attitude.toString(), attitude);
        CtpEnumBean deadline = enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name()); //处理期限 attitude
        colMetadata.put(EnumNameEnum.collaboration_deadline.toString(), deadline);

        CtpEnumBean secretLevel = enumManagerNew.getEnum(EnumNameEnum.edoc_secret_level.name());
        colMetadata.put(EnumNameEnum.edoc_secret_level.toString(), secretLevel);

        CtpEnumBean urgentLevel = enumManagerNew.getEnum(EnumNameEnum.edoc_urgent_level.name());
        colMetadata.put(EnumNameEnum.edoc_urgent_level.toString(), urgentLevel);

        String listType = condition.get("listType")==null ? "" : (String)condition.get("listType");
        int edocType = condition.get("edocType")==null?-1:(Integer)condition.get("edocType");
        int type = condition.get("type")==null?-1:(Integer)condition.get("type");
        User user = (User)condition.get("user");
        //是否包含“待登记”按钮
    	boolean hasRegistButton=false;
    	if(EdocEnum.edocType.recEdoc.ordinal()==edocType) {
    		hasRegistButton = true;
    		switch (type) {
				case EdocNavigationEnum.LIST_TYPE_PENDING:
				case EdocNavigationEnum.LIST_TYPE_DONE:
					modelAndView.addObject("newEdoclabel", "edoc.new.type.rec");
					break;
				case EdocNavigationEnum.LIST_TYPE_WAIT_SEND:
				case EdocNavigationEnum.LIST_TYPE_SENT:
					modelAndView.addObject("newEdoclabel", "edoc.element.receive.distribute");
					break;
				default:
					break;
			}
    	} else {
    		switch (type) {
				case EdocNavigationEnum.LIST_TYPE_PENDING:
					modelAndView.addObject("newEdoclabel", "edoc.new.type.send");
					break;
				case EdocNavigationEnum.LIST_TYPE_DONE:
					modelAndView.addObject("newEdoclabel", "edoccolMetadata.new.type.send");
					break;
				case EdocNavigationEnum.LIST_TYPE_WAIT_SEND:
				case EdocNavigationEnum.LIST_TYPE_SENT:
					modelAndView.addObject("newEdoclabel", "edoc.new.type.send");
					break;
				default:
					break;
			}
    	}
    	modelAndView.addObject("hasRegistButton", hasRegistButton);
    	modelAndView.addObject("isOpenRegister", EdocSwitchHelper.isOpenRegister());
    	//转发文
    	modelAndView.addObject("newForwardaRrticle", "edoc.new.type.forwardarticle");
    	try {
    		int roleEdocType = edocType==1?3:edocType;
    		//发文的拟文权限
         	boolean isSendEdocCreateRole= EdocRoleHelper.isEdocCreateRole(EdocEnum.edocType.sendEdoc.ordinal());
        	boolean isEdocCreateRole= EdocRoleHelper.isEdocCreateRole(user.getLoginAccount(), user.getId(), roleEdocType);
        	boolean isExchangeRole= EdocRoleHelper.isExchangeRole();
        	modelAndView.addObject("isSendEdocCreateRole", isSendEdocCreateRole);
        	modelAndView.addObject("isEdocCreateRole", isEdocCreateRole);
        	modelAndView.addObject("isExchangeRole", isExchangeRole);
        	modelAndView.addObject("colMetadata", colMetadata);
            modelAndView.addObject("controller", "edocListController.do");
            modelAndView.addObject("edocType", edocType);
            modelAndView.addObject("listType", listType);
            modelAndView.addObject("currentUserId", user.getId());
            modelAndView.addObject("currentUserAccountId", user.getLoginAccount());
            modelAndView.addObject("isGourpBy", condition.get("deduplication"));
    	} catch(BusinessException e) {
    		LOGGER.error("公文列表异常：",e);
    	}
    	return modelAndView;
	}

	/**
     * 在办公文列表
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listZcdb(HttpServletRequest request, HttpServletResponse response) {
    	ModelAndView modelAndView = new ModelAndView("edoc/listZcdb");
    	//区分待办、在办  listPending待办 listZcdb在办 listPendingAll 所有待办
        String listType = Strings.isBlank(request.getParameter("listType"))?"listZcdb":request.getParameter("listType");
        //公文类型
        int edocType = Strings.isBlank(request.getParameter("edocType"))?-1:Integer.parseInt(request.getParameter("edocType"));
        //公文单类型
        long subEdocType = Strings.isBlank(request.getParameter("subType"))?-1:Long.parseLong(request.getParameter("subType"));
        //过滤条件：跟踪
     	int track = Strings.isBlank(request.getParameter("track"))?-1:Integer.parseInt(request.getParameter("track"));
     	//是否是组件查询：1组合查询 0非组合查询
     	int comb_condition = Strings.isBlank(request.getParameter("comb_condition"))? EdocNavigationEnum.EdocListCombType.Comb_No.ordinal():Integer.parseInt(request.getParameter("comb_condition"));
     	int type = EdocNavigationEnum.EdocV5ListTypeEnum.getEnumByKey(listType).getType();
     	User user = AppContext.getCurrentUser();
     	Map<String, Object> condition = new HashMap<String, Object>();
        condition.put("track", track);
        condition.put("state", StateEnum.col_pending.key());
        condition.put("edocType", edocType);
        condition.put("subEdocType", subEdocType);
        if(edocType == EdocEnum.edocType.recEdoc.ordinal()) {
        	//办文阅文 1办文 2办文
         	int processType = EdocNavigationEnum.EdocProcessTypeEnum.ProcessType_Done.ordinal();
         	condition.put("processType", processType);
        }
        condition.put("listType", listType);
        condition.put("type", type);
        condition.put("user", user);
        condition.put("userId", user.getId());
		condition.put("accountId", user.getLoginAccount());
        condition.put("conditionKey", request.getParameter("condition"));
        condition.put("textfield", request.getParameter("textfield"));
        condition.put("textfield1", request.getParameter("textfield1"));
        List<EdocSummaryModel> queryList = null;
        try {
        	 V3xOrgMember theMember = orgManager.getEntityById(V3xOrgMember.class,user.getId());
	        if (theMember.getIsAssigned()) {
	        	if(comb_condition == EdocNavigationEnum.EdocListCombTypeEnum.Comb_Yes.ordinal()) {
	        		EdocSearchModel em = new EdocSearchModel();
	        		bind(request, em);
	        		queryList = edocListManager.combQueryByCondition(type, condition, em);
	        		modelAndView.addObject("combQueryObj", em);  //设置的查询条件还原到页面
	        		modelAndView.addObject("combCondition", "1");
	            } else {
	            	queryList = edocListManager.findEdocPendingList(type, condition);
	            }
	        } else {
	        	queryList = new ArrayList<EdocSummaryModel>();
	        }
	        if (queryList != null) {
        		modelAndView.addObject("pendingList", queryList);
        	}
        } catch(Exception e) {
        	LOGGER.error("公文在办列表异常：",e);
        }
        return listEdoc(request, response, modelAndView, queryList, condition);
    }

    /**
     * 待办公文列表
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
//    @CheckRoleAccess(roleTypes={Role_NAME.EdocModfiy,Role_NAME.SendEdoc,Role_NAME.SignEdoc,Role_NAME.RecEdoc})
    public ModelAndView listPending(HttpServletRequest request, HttpServletResponse response) {
    	ModelAndView modelAndView = new ModelAndView("edoc/listPending");
    	//区分待办、在办  listPending待办 listZcdb在办 listPendingAll 所有待办
        String listType = Strings.isBlank(request.getParameter("listType"))?"listPendingAll":request.getParameter("listType");
        //公文类型
        int edocType = Strings.isBlank(request.getParameter("edocType"))?-1:Integer.parseInt(request.getParameter("edocType"));
        //公文单类型
        long subEdocType = Strings.isBlank(request.getParameter("subType"))?-1:Long.parseLong(request.getParameter("subType"));
        //过滤条件：跟踪
     	int track = Strings.isBlank(request.getParameter("track"))?-1:Integer.parseInt(request.getParameter("track"));
     	//是否是组件查询：1组合查询 0非组合查询
     	int comb_condition = Strings.isBlank(request.getParameter("comb_condition"))? EdocNavigationEnum.EdocListCombType.Comb_No.ordinal():Integer.parseInt(request.getParameter("comb_condition"));
     	int type = EdocNavigationEnum.EdocV5ListTypeEnum.getEnumByKey(listType).getType();
     	User user = AppContext.getCurrentUser();
     	Map<String, Object> condition = new HashMap<String, Object>();
        condition.put("track", track);
        condition.put("state", StateEnum.col_pending.key());
        condition.put("edocType", edocType);
        condition.put("subEdocType", subEdocType);
        if(edocType == EdocEnum.edocType.recEdoc.ordinal()) {
        	//办文阅文 1办文 2办文
         	int processType = EdocNavigationEnum.EdocProcessTypeEnum.ProcessType_Done.ordinal();
         	condition.put("processType", processType);
        }
        condition.put("listType", listType);
        condition.put("type", type);
        condition.put("user", user);
        condition.put("userId", user.getId());
		condition.put("accountId", user.getLoginAccount());
        condition.put("conditionKey", request.getParameter("condition"));
        condition.put("textfield", request.getParameter("textfield"));
        condition.put("textfield1", request.getParameter("textfield1"));
        List<EdocSummaryModel> queryList = null;
        try {
        	 V3xOrgMember theMember = orgManager.getEntityById(V3xOrgMember.class,user.getId());
	        if (theMember.getIsAssigned()) {
	        	if(comb_condition == EdocNavigationEnum.EdocListCombTypeEnum.Comb_Yes.ordinal()) {
	        		EdocSearchModel em=new EdocSearchModel();
	        		bind(request, em);
	        		queryList = edocListManager.combQueryByCondition(type, condition, em);
	        		modelAndView.addObject("combQueryObj", em);  //设置的查询条件还原到页面
	        		modelAndView.addObject("combCondition", "1");
	            } else {
	            	queryList = edocListManager.findEdocPendingList(type, condition);
	            }
	        } else {
	        	queryList = new ArrayList<EdocSummaryModel>();
	        }
	        if (queryList != null) {
        		modelAndView.addObject("pendingList", queryList);
        	}
        } catch(Exception e) {
        	LOGGER.error("公文待办列表异常：",e);
        }
        return listEdoc(request, response, modelAndView, queryList, condition);
    }

    /**
     * 已办公文列表
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
//    @CheckRoleAccess(roleTypes={Role_NAME.EdocModfiy})
    public ModelAndView listDone(HttpServletRequest request, HttpServletResponse response) {
    	ModelAndView modelAndView = new ModelAndView("edoc/listDone");
     	Map<String, Object> condition = new HashMap<String, Object>();
    	//区分待办、在办  listPending待办 listZcdb在办 listPendingAll 所有待办
        String listType = Strings.isBlank(request.getParameter("listType"))?"listPendingAll":request.getParameter("listType");
        //公文类型
        int edocType = Strings.isBlank(request.getParameter("edocType"))?-1:Integer.parseInt(request.getParameter("edocType"));
        //公文单类型
        long subEdocType = Strings.isBlank(request.getParameter("subType"))?-1:Long.parseLong(request.getParameter("subType"));
        //过滤条件：跟踪
     	int track = Strings.isBlank(request.getParameter("track"))?-1:Integer.parseInt(request.getParameter("track"));
     	//是否是组件查询：1组合查询 0非组合查询
     	int comb_condition = Strings.isBlank(request.getParameter("comb_condition"))? EdocNavigationEnum.EdocListCombType.Comb_No.ordinal():Integer.parseInt(request.getParameter("comb_condition"));
     	int type = EdocNavigationEnum.EdocV5ListTypeEnum.getEnumByKey(listType).getType();
     	//同一流程只显示最后一条
        String deduplication = String.valueOf(request.getParameter("deduplication"));
        deduplication = Functions.toHTML(deduplication);
        if ("null".equals(deduplication) || Strings.isBlank(deduplication)) {
        	deduplication = "false";
        }
        condition.put("deduplication",deduplication);
     	User user = AppContext.getCurrentUser();
        condition.put("track", track);
        condition.put("state", StateEnum.col_pending.key());
        condition.put("edocType", edocType);
        condition.put("subEdocType", subEdocType);
        if(edocType == EdocEnum.edocType.recEdoc.ordinal()) {
        	//办文阅文 1办文 2办文
         	int processType = EdocNavigationEnum.EdocProcessTypeEnum.ProcessType_Done.ordinal();
         	condition.put("processType", processType);
        }
        condition.put("listType", listType);
        condition.put("type", type);
        condition.put("user", user);
        condition.put("userId", user.getId());
		condition.put("accountId", user.getLoginAccount());
        condition.put("conditionKey", request.getParameter("condition"));
        condition.put("textfield", request.getParameter("textfield"));
        condition.put("textfield1", request.getParameter("textfield1"));
        List<EdocSummaryModel> queryList = null;
        try {
        	 V3xOrgMember theMember = orgManager.getEntityById(V3xOrgMember.class,user.getId());
	        if (theMember.getIsAssigned()) {
	        	if(comb_condition == EdocNavigationEnum.EdocListCombTypeEnum.Comb_Yes.ordinal()) {
	        		EdocSearchModel em = new EdocSearchModel();
	        		bind(request, em);
	        		queryList = edocListManager.combQueryByCondition(type, condition, em);
	        		modelAndView.addObject("combQueryObj", em);  //设置的查询条件还原到页面
	        		modelAndView.addObject("combCondition", "1");
	            } else {
	            	queryList = edocListManager.findEdocDoneList(type, condition);
	            }
	        } else {
	        	queryList = new ArrayList<EdocSummaryModel>();
	        }
	        if (queryList != null) {
        		modelAndView.addObject("pendingList", queryList);
        	}
        } catch(Exception e) {
        	LOGGER.error("公文已办列表异常：",e);
        }
//        zhou:当 张三处理时在李四的已办中可以看到张三处理的数据，但是因为不是李四处理的所以处理时间为null,在已办中的数据顺序是乱的，所以加了一层排序处理
//		在EdocListManagerImpl.java 文件中的437行有"zhou"标记的地方加了判断给处理时间字段赋值。
//		EdocSummaryModel temp=null;
//		for (int i = 0; i < queryList.size()-1; i++) {
//			for (int j = 0; j < queryList.size()-1-i; j++) {
//				boolean flag=(null==queryList.get(j).getDealTime()?new Date():queryList.get(j).getDealTime()).before(null==queryList.get(j+1).getDealTime()?new Date():queryList.get(j+1).getDealTime());
//				if(flag){
//					temp=queryList.get(j);
//					queryList.set(j,queryList.get(j+1));
//					queryList.set(j+1,temp);
//				}
//			}
//		}
        return listEdoc(request, response, modelAndView, queryList, condition);
    }





    /**
     * 已发公文列表
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
//    @CheckRoleAccess(roleTypes={Role_NAME.SendEdoc,Role_NAME.SignEdoc,Role_NAME.RecEdoc})
    public ModelAndView listSent(HttpServletRequest request, HttpServletResponse response) {
    	ModelAndView modelAndView = new ModelAndView("edoc/listSent");
    	//区分待办、在办  listPending待办 listZcdb在办 listPendingAll 所有待办
        String listType = Strings.isBlank(request.getParameter("listType"))?"listPendingAll":request.getParameter("listType");
        //公文类型
        int edocType = Strings.isBlank(request.getParameter("edocType"))?-1:Integer.parseInt(request.getParameter("edocType"));
        //公文单类型
        long subEdocType = Strings.isBlank(request.getParameter("subType"))?-1:Long.parseLong(request.getParameter("subType"));
        //过滤条件：跟踪
     	int track = Strings.isBlank(request.getParameter("track"))?-1:Integer.parseInt(request.getParameter("track"));
     	int comb_condition = Strings.isBlank(request.getParameter("comb_condition"))? EdocNavigationEnum.EdocListCombType.Comb_No.ordinal():Integer.parseInt(request.getParameter("comb_condition"));
     	int type = EdocNavigationEnum.EdocV5ListTypeEnum.getEnumByKey(listType).getType();
     	int listTypeInt = Strings.isBlank(request.getParameter("listTypeInt"))?0:Integer.parseInt(request.getParameter("listTypeInt"));
     	User user = AppContext.getCurrentUser();
     	Map<String, Object> condition = new HashMap<String, Object>();
        condition.put("track", track);
        condition.put("state", StateEnum.col_pending.key());
        condition.put("edocType", edocType);
        condition.put("subEdocType", subEdocType);
        condition.put("listType", listType);
        condition.put("listTypeInt", listTypeInt);
        condition.put("type", type);
        condition.put("user", user);
        condition.put("userId", user.getId());
		condition.put("accountId", user.getLoginAccount());
        condition.put("conditionKey", request.getParameter("condition"));
        condition.put("textfield", request.getParameter("textfield"));
        condition.put("textfield1", request.getParameter("textfield1"));
        List<EdocSummaryModel> queryList = null;
        try {
        	 V3xOrgMember theMember = orgManager.getEntityById(V3xOrgMember.class,user.getId());
	         if (theMember.getIsAssigned()) {
	        	 if(comb_condition == EdocNavigationEnum.EdocListCombTypeEnum.Comb_Yes.ordinal()) {
	        		EdocSearchModel em = new EdocSearchModel();
	        		bind(request, em);
	        		queryList = edocListManager.combQueryByCondition(type, condition, em);
	        		modelAndView.addObject("combQueryObj", em);  //设置的查询条件还原到页面
	        		modelAndView.addObject("combCondition", "1");
	            } else {
	            	queryList = edocListManager.findEdocPendingList(type, condition);
	            }
	         } else {
	        	queryList = new ArrayList<EdocSummaryModel>();
	         }
	         if (queryList != null) {
	        	modelAndView.addObject("pendingList", queryList);
	         }
        } catch(Exception e) {
        	LOGGER.error("公文已发列表异常：",e);
        }
        return listEdoc(request, response, modelAndView, queryList, condition);
    }

    /**
     * 待发公文列表
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listWaitSend(HttpServletRequest request, HttpServletResponse response) {
    	ModelAndView modelAndView = new ModelAndView("edoc/listWaitSend");//草稿箱
    	//区分待办、在办  listPending待办 listZcdb在办 listPendingAll 所有待办
        String listType = Strings.isBlank(request.getParameter("listType"))?"listPendingAll":request.getParameter("listType");
        //公文类型
        int edocType = Strings.isBlank(request.getParameter("edocType"))?-1:Integer.parseInt(request.getParameter("edocType"));
        //公文单类型
        long subEdocType = Strings.isBlank(request.getParameter("subType"))?-1:Long.parseLong(request.getParameter("subType"));
        //过滤条件：跟踪
     	int track = Strings.isBlank(request.getParameter("track"))?-1:Integer.parseInt(request.getParameter("track"));
     	//是否是组件查询：1组合查询 0非组合查询
     	int comb_condition = Strings.isBlank(request.getParameter("comb_condition"))? EdocNavigationEnum.EdocListCombType.Comb_No.ordinal():Integer.parseInt(request.getParameter("comb_condition"));
     	int type = EdocNavigationEnum.EdocV5ListTypeEnum.getEnumByKey(listType).getType();
     	User user = AppContext.getCurrentUser();
     	Map<String, Object> condition = new HashMap<String, Object>();
        condition.put("track", track);
        condition.put("state", StateEnum.col_pending.key());
        condition.put("edocType", edocType);
        condition.put("subEdocType", subEdocType);
        condition.put("listType", listType);
        condition.put("type", type);
        condition.put("user", user);
        condition.put("userId", user.getId());
		condition.put("accountId", user.getLoginAccount());
        condition.put("conditionKey", request.getParameter("condition"));
        condition.put("textfield", request.getParameter("textfield"));
        condition.put("textfield1", request.getParameter("textfield1"));
        List<EdocSummaryModel> queryList = null;
        try {
        	 V3xOrgMember theMember = orgManager.getEntityById(V3xOrgMember.class,user.getId());
	        if (theMember.getIsAssigned()) {
	        	if(comb_condition == EdocNavigationEnum.EdocListCombTypeEnum.Comb_Yes.ordinal()) {
	        		EdocSearchModel em = new EdocSearchModel();
	        		bind(request, em);
	            } else {
	            	queryList = edocListManager.findEdocWaitSendList(type, condition);
	            }
	        	if (queryList != null) {
	        		modelAndView.addObject("pendingList", queryList);
	        	}
	        } else {
	        	queryList = new ArrayList<EdocSummaryModel>();
	        	modelAndView.addObject("pendingList", queryList);
	        }
        } catch(Exception e) {
        	LOGGER.error("公文待发列表异常：",e);
        }
        if("backBox".equals(listType) || "retreat".equals(listType)) {//backBox发文退稿箱,retreat收文退件箱
        	modelAndView = new ModelAndView("edoc/listBackBox");//跳转到退稿想
		}
        String hasBackBox= SystemProperties.getInstance().getProperty("edoc.hasBackBox");
        String backBoxType = "0";
        if(!"true".equals(hasBackBox)){//A8
        	backBoxType = "1";
        	modelAndView.addObject("backBoxType", backBoxType);
        }

        boolean isG6 = EdocHelper.isG6Version();

        if(isG6){
        	modelAndView.addObject("isOpenRegister", EdocSwitchHelper.isOpenRegister());
        }
        modelAndView.addObject("isG6", isG6);
        return listEdoc(request, response, modelAndView, queryList, condition);
    }

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public void setEnumManagerNew(EnumManager enumManager) {
		this.enumManagerNew = enumManager;
	}

	public void setEdocListManager(EdocListManager edocListManager) {
		this.edocListManager = edocListManager;
	}

	public void setEdocSummaryManager(EdocSummaryManager edocSummaryManager) {
		this.edocSummaryManager = edocSummaryManager;
	}

}
