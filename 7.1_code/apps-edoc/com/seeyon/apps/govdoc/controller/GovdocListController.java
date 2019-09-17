package com.seeyon.apps.govdoc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.govdoc.helper.GovdocExcelHelper;
import com.seeyon.apps.govdoc.helper.GovdocListHelper;
import com.seeyon.apps.govdoc.manager.GovdocElementManager;
import com.seeyon.apps.govdoc.manager.GovdocListConfigManager;
import com.seeyon.apps.govdoc.manager.GovdocListManager;
import com.seeyon.apps.govdoc.manager.GovdocOpenManager;
import com.seeyon.apps.govdoc.manager.GovdocPishiManager;
import com.seeyon.apps.govdoc.manager.GovdocStatPushManager;
import com.seeyon.apps.govdoc.util.GovdocParamUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.excel.DataRecord;
import com.seeyon.ctp.common.excel.FileToExcelManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.organization.bo.MemberRole;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.edoc.domain.EdocRegisterCondition;

/**
 * 新公文列表控制器
 * @author 唐桂林
 *
 */
public class GovdocListController extends BaseController {
	
	private static final Log LOGGER = LogFactory.getLog(GovdocListController.class);
	
	private GovdocListManager govdocListManager;
	private GovdocElementManager govdocElementManager;
	private GovdocListConfigManager govdocListConfigManager;
	private GovdocOpenManager govdocOpenManager;
	private GovdocPishiManager govdocPishiManager;
	private GovdocStatPushManager govdocStatPushManager;
	private EnumManager enumManagerNew;
	private FileToExcelManager fileToExcelManager;
	private OrgManager orgManager;
	
    /**
     * 待办公文列表
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listPending(HttpServletRequest request, HttpServletResponse response) {
    	ModelAndView mav = new ModelAndView("govdoc/list/listPending");
    	try {
	    	//区分待办、在办  listPending待办 listZcdb在办 listPendingAll 所有待办
	        String listType = Strings.isBlank(request.getParameter("listType"))?"listPendingAll":request.getParameter("listType");
	        
	        Map<String, String> condition = new HashMap<String, String>();
	        condition.put("listType", listType);
	        condition.put("govdocType", request.getParameter("govdocType"));
	        condition.put("configId", request.getParameter("configId"));
	        condition.put("condition", request.getParameter("condition"));
	        condition.put("templateIds", request.getParameter("templateIds"));
	        
	        FlipInfo flipInfo = new FlipInfo();
	        flipInfo.setParams(condition);
	        flipInfo = govdocListManager.findPendingList(flipInfo, condition);
        	
	        fillZhuanfawenParams(mav);
	        request.setAttribute("fflistPending", flipInfo);
	        boolean isContainRec=GovdocListHelper.isContainRecTemplate(request.getParameter("templateIds"));
	        request.setAttribute("isContainRec", isContainRec);
	        return listGovdoc(mav);
    	} catch(Exception e) {
    		LOGGER.error("获取待办公文列表出错", e);
    	}
    	return mav;
    }
    
    /**
     * 已办公文列表
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listDone(HttpServletRequest request, HttpServletResponse response) {
    	ModelAndView mav = new ModelAndView("govdoc/list/listDone");
    	try {
	    	Map<String, String> condition = new HashMap<String, String>();
	        condition.put("listType", request.getParameter("listType"));
	        condition.put("govdocType", request.getParameter("govdocType"));
	        condition.put("configId", request.getParameter("configId"));
	        condition.put("condition", request.getParameter("condition"));
	        condition.put("templateIds", request.getParameter("templateIds"));

	        FlipInfo flipInfo = new FlipInfo();
	        flipInfo.setParams(condition);
	        flipInfo = govdocListManager.findDoneList(flipInfo, condition);
	        
	        fillZhuanfawenParams(mav);
	        request.setAttribute("fflistDone", flipInfo);
	        boolean isContainRec=GovdocListHelper.isContainRecTemplate(request.getParameter("templateIds"));
	        request.setAttribute("isContainRec", isContainRec);
	        return listGovdoc(mav);
    	} catch(Exception e) {
    		LOGGER.error("获取待办公文列表出错", e);
    	}
    	return mav;
    }
    
    /**
     * 已发公文列表
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listSent(HttpServletRequest request, HttpServletResponse response) {
    	ModelAndView mav = new ModelAndView("govdoc/list/listSent");
    	try {
	    	Map<String, String> condition = new HashMap<String, String>();
	        condition.put("listType", request.getParameter("listType"));
	        condition.put("govdocType", request.getParameter("govdocType"));
	        condition.put("condition", request.getParameter("condition"));
	        condition.put("templateIds", request.getParameter("templateIds"));
	        
	        FlipInfo flipInfo = new FlipInfo();
	        flipInfo.setParams(condition);
	        flipInfo = govdocListManager.findSentList(flipInfo, condition);
        		        
	        request.setAttribute("fflistSent", flipInfo);
	        return listGovdoc(mav);
    	} catch(Exception e) {
    		LOGGER.error("获取待办公文列表出错", e);
    	}
    	return mav;
    }
    
    /**
     * 待发公文列表
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listWaitSend(HttpServletRequest request, HttpServletResponse response) {
    	ModelAndView mav = new ModelAndView("govdoc/list/listWaitSend");
    	try {
	    	Map<String, String> condition = new HashMap<String, String>();
	        condition.put("listType", request.getParameter("listType"));
	        condition.put("govdocType", request.getParameter("govdocType"));
	        condition.put("condition", request.getParameter("condition"));
	        condition.put("templateIds", request.getParameter("templateIds"));
	        
	        FlipInfo flipInfo = new FlipInfo();
	        flipInfo.setParams(condition);
	        flipInfo = govdocListManager.findWaitSendList(flipInfo, condition);
	        
	        request.setAttribute("fflistWaitSend", flipInfo);
	        return listGovdoc(mav);
    	} catch(Exception e) {
    		LOGGER.error("获取待办公文列表出错", e);
    	}
    	return mav;
    }
    
    /**
     * 发文登记簿
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public ModelAndView listSendRegister(HttpServletRequest request, HttpServletResponse response) {
    	ModelAndView mav = new ModelAndView("govdoc/stat/register_list_send");
    	try {
	    	Map<String, String> condition = new HashMap<String, String>();
	        condition.put("listType", request.getParameter("listType"));
	        
            String dataNum = request.getParameter("dataNum");
            String conditionId = request.getParameter("conditionId");
            if (Strings.isNotBlank(conditionId)) {
                EdocRegisterCondition register = govdocStatPushManager.getRegisterConditionById(Long.parseLong(conditionId));
                if(register != null) {
                	mav.addObject("queryCol", register.getQueryCol());
                	String ext1 = register.getContentExt1();
                	if(Strings.isNotBlank(ext1)) {
                		Map<String, String> map = (Map<String, String>)JSONUtil.parseJSONString(ext1);
                		for(String key : map.keySet()) {
                			String value = map.get(key);
                			if(Strings.isNotBlank(value)) {
                				condition.put(key, value);
                				mav.addObject("condition_" + key, value);
                			}
                		}
                		if(condition.get("condition") == null) {
        					condition.put("condition", conditionId);
        				}
                	}
                }
            }
            
    	    List<String> leaderName = govdocPishiManager.getAllLeaderName(AppContext.currentAccountId());
    	    mav.addObject("leaderNames", leaderName);

    	    FlipInfo flipInfo = new FlipInfo();
	        flipInfo.setParams(condition);
	        if(Strings.isNotBlank(dataNum) && Strings.isDigits(dataNum)) {
	        	flipInfo.setSize(Integer.parseInt(dataNum));
	        	mav.addObject("dataNum", dataNum);
	        }
	        flipInfo = govdocListManager.findSendRegisterList(flipInfo, condition);
	        request.setAttribute("fflistSendRegister", flipInfo);
	        
	        return listGovdoc(mav);
    	} catch(Exception e) {
    		LOGGER.error("获取待办公文列表出错", e);
    	}
    	return mav;
    }
    
    /**
     * 收文登记簿
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public ModelAndView listRecRegister(HttpServletRequest request, HttpServletResponse response) {
    	ModelAndView mav = new ModelAndView("govdoc/stat/register_list_rec");
    	try {
	    	Map<String, String> condition = new HashMap<String, String>();
	        condition.put("listType", request.getParameter("listType"));

	        String dataNum = request.getParameter("dataNum");
            String conditionId = request.getParameter("conditionId");
            if (Strings.isNotBlank(conditionId)) {
                EdocRegisterCondition register = govdocStatPushManager.getRegisterConditionById(Long.parseLong(conditionId));
                if(register != null) {
                	mav.addObject("queryCol", register.getQueryCol());
                	String ext1 = register.getContentExt1();
                	if(Strings.isNotBlank(ext1)) {
                		Map<String, String> map = (Map<String, String>)JSONUtil.parseJSONString(ext1);
                		for(String key : map.keySet()) {
                			String value = map.get(key);
                			if(Strings.isNotBlank(value)) {
                				condition.put(key, value);
                				mav.addObject("condition_" + key, value);
                			}
                		}
                		if(condition.get("condition") == null) {
        					condition.put("condition", conditionId);
        				}
                	}
                }
            }	
	        
	        FlipInfo flipInfo = new FlipInfo();
	        flipInfo.setParams(condition);
	        if(Strings.isNotBlank(dataNum) && Strings.isDigits(dataNum)) {
	        	flipInfo.setSize(Integer.parseInt(dataNum));
	        	mav.addObject("dataNum", dataNum);
	        }
	        flipInfo = govdocListManager.findRecRegisterList(flipInfo, condition);
	        request.setAttribute("fflistRecRegister", flipInfo);
            
            List<String> leaderName = govdocPishiManager.getAllLeaderName(AppContext.currentAccountId());
    	    mav.addObject("leaderNames", leaderName);
    	    
	        return listGovdoc(mav);
    	} catch(Exception e) {
    		LOGGER.error("获取待办公文列表出错", e);
    	}
    	return mav;
    }
    
    /**
     * 签报登记簿
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listSignRegister(HttpServletRequest request, HttpServletResponse response) {
    	ModelAndView mav = new ModelAndView("govdoc/stat/register_list_sign");
    	try {
	    	Map<String, String> condition = new HashMap<String, String>();
	        condition.put("listType", request.getParameter("listType"));

		    List<String> leaderName = govdocPishiManager.getAllLeaderName(AppContext.currentAccountId());
		    mav.addObject("leaderNames", leaderName);

	        FlipInfo flipInfo = new FlipInfo();
	        flipInfo.setParams(condition);
	        flipInfo = govdocListManager.findSignRegisterList(flipInfo, condition);        
	        request.setAttribute("fflistSignRegister", flipInfo);
	        
	        return listGovdoc(mav);
    	} catch(Exception e) {
    		LOGGER.error("获取待办公文列表出错", e);
    	}
    	return mav;
    }
    
    /**
     * 公文查询导出Excel
     * @param request
     * @param response
     * @return
     */
	public ModelAndView queryToExcel(HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> params = GovdocListHelper.getConditionsByRequest(request);//公文查询是老框架，只能通过request来取值
			FlipInfo flipInfo = govdocListManager.findQueryResultList(null, params);
			DataRecord dataRecord = GovdocExcelHelper.getQueryListExcel(flipInfo, params);
			fileToExcelManager.save(response, dataRecord.getTitle(), dataRecord);
		} catch(Exception e) {
			LOGGER.error("公文查询导出Excel出错", e);
		}
	    return null;
	}
    
    /**
     * 公文登记簿导出Excel
     * @param request
     * @param response
     * @return
     */
	@SuppressWarnings("unchecked")
	public ModelAndView listRegisterToExcel(HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> params = ParamUtil.getJsonDomain("conditionForm");
			FlipInfo flipInfo = new FlipInfo();
            flipInfo.setNeedTotal(true);
            flipInfo.setSize(-1);
    		flipInfo.setPage(-1);
			String listType = request.getParameter("listType");
			if(Strings.isBlank(params.get("listType"))) {
				params.put("listType", listType);
			}
			//按照条件查询的集合导出有问题，需要condition
			params.put("condition", "true");
			if("listSendRegister".equals(listType)) {
				flipInfo = govdocListManager.findSendRegisterList(flipInfo, params);
			} else if("listRecRegister".equals(listType)) {
				flipInfo = govdocListManager.findRecRegisterList(flipInfo, params);
			} else if("listSignRegister".equals(listType)) {
				flipInfo = govdocListManager.findSignRegisterList(flipInfo, params);
			}
			
			DataRecord dataRecord = GovdocExcelHelper.getRegisterListExcel(flipInfo, params);
			fileToExcelManager.save(response, dataRecord.getTitle(), dataRecord);
		} catch(Exception e) {
			LOGGER.error("公文登记簿导出Excel出错", e);
		}
	    return null;
	}
    
    /**
     * 分送列表
     * @param request
     * @param response
     * @return
     */
    public ModelAndView listExchange(HttpServletRequest request, HttpServletResponse response) {
    	ModelAndView mav = new ModelAndView();
    	try {
	    	Map<String, String> condition = new HashMap<String, String>();
	        condition.put("listType", request.getParameter("listType"));
	        FlipInfo flipInfo = new FlipInfo();
	        flipInfo.setParams(condition);
	        String listType = request.getParameter("listType");
	        if ("listExchangeSendPending".equals(listType) || "listExchangeSignPending".equals(listType)) {
	        	mav.setViewName("govdoc/list/listPending");
	        	flipInfo = govdocListManager.findPendingList(flipInfo, condition);
	        	request.setAttribute("fflistPending", flipInfo);
			}else if ("listExchangeSendDone".equals(listType) || "listExchangeSignDone".equals(listType) || "listExchangeFallback".equals(listType)) {
				mav.setViewName("govdoc/list/listDone");
				flipInfo = govdocListManager.findDoneList(flipInfo, condition);
				request.setAttribute("fflistDone", flipInfo);
			}
	        return listGovdoc(mav);
    	} catch(Exception e) {
    		LOGGER.error("获取公文列表出错", e);
    	}
    	return mav;
    }
    
    /**
     * 
     * @param request
     * @param response
     * @param modelAndView
     * @param queryList
     * @param condition
     * @return
     */
    private ModelAndView listGovdoc(ModelAndView mav) throws BusinessException {
    	List<CtpEnumItem> secretLevelList = enumManagerNew.getEnumItemByProCode(EnumNameEnum.edoc_secret_level);
        List<CtpEnumItem> urgentLevelList = enumManagerNew.getEnumItemByProCode(EnumNameEnum.edoc_urgent_level);
        List<CtpEnumItem> unitLevelList = enumManagerNew.getEnumItemByProCode(EnumNameEnum.edoc_unit_level);
        List<CtpEnumItem> keepPeriodList = enumManagerNew.getEnumItemByProCode(EnumNameEnum.edoc_keep_period);
        List<CtpEnumItem> sendTypeList = enumManagerNew.getEnumItemByProCode(EnumNameEnum.edoc_send_type);
        List<CtpEnumItem> docTypeList = enumManagerNew.getEnumItemByProCode(EnumNameEnum.edoc_doc_type);
        
        for (CtpEnumItem item : secretLevelList) {
        	item.setDescription(ResourceUtil.getString(item.getDescription()));
		}
        for (CtpEnumItem item : urgentLevelList) {
        	item.setDescription(ResourceUtil.getString(item.getDescription()));
        }
        for (CtpEnumItem item : unitLevelList) {
        	item.setDescription(ResourceUtil.getString(item.getDescription()));
        }
        for (CtpEnumItem item : keepPeriodList) {
        	item.setDescription(ResourceUtil.getString(item.getDescription()));
        }
        
        mav.addObject("secretLevels", JSONUtil.toJSONString(secretLevelList));
        mav.addObject("urgentLevels", JSONUtil.toJSONString(urgentLevelList));
        mav.addObject("keepPeriods", JSONUtil.toJSONString(keepPeriodList));
        mav.addObject("unitLevels", JSONUtil.toJSONString(unitLevelList));
        
        mav.addObject("secretLevelList", secretLevelList);
        mav.addObject("urgentLevelList", urgentLevelList);
        mav.addObject("keepPeriodList", urgentLevelList);
        mav.addObject("unitLevelList", unitLevelList);
        mav.addObject("sendTypeList", sendTypeList);
        mav.addObject("docTypeList", docTypeList);
    	return mav;
    }

    /**
	 * 打开公文列表组合查询页面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
    public ModelAndView openComQueryDialog(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView("govdoc/list/list_com_query_dialog");
        try {
	        return listGovdoc(mav);
        } catch(Exception e) {
        	LOGGER.error("打开公文列表组合查询页面出错", e);
        }
        return mav;
    }
    
    /**
     * 打开公文列表分类配置页面
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView openListConfigDialog(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView modelAndView = new ModelAndView("govdoc/list/list_policy_config_dialog");
		String listType = request.getParameter("listType");
		int type = 1;
		if(Strings.isNotBlank(listType)) {
			if(listType.startsWith("listPending")) {
				type = 1;
			} else if(listType.startsWith("listDone")) {
				type = 2;
			}
		}
		List<PermissionVO> permissionList =  govdocListConfigManager.getListPermissions(AppContext.currentAccountId());
		List<String> configList = govdocListConfigManager.findListConfigResult(type, AppContext.currentUserId());
		modelAndView.addObject("permission", permissionList);
		modelAndView.addObject("govdocDoneAll", configList);
		return modelAndView;
	}

    /**
	  * 显示公文表单锁列表
	  * @param request
	  * @param response
	  * @return
	  * @throws Exception
	  */
	 public ModelAndView listGovdocformLock(HttpServletRequest request, HttpServletResponse response) throws Exception {
		 ModelAndView mav = new ModelAndView("govdoc/list/listGovdocformLock");
		 Map<String, String> paramMap = new HashMap<String, String>();
		 paramMap.put("summaryId", request.getParameter("summaryId"));
		 return mav;
	 }
    
	/**
     * 公文查询
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView queryCondition(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView("govdoc/query/query_condition");
        try {
	        return listGovdoc(mav);	        
        } catch(Exception e) {
        	LOGGER.error("公文查询出错", e);
        }
        return mav;
    }
	
    /**
     * 公文查询结果
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView queryResult(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView("govdoc/query/query_result");
        try {
	        CtpEnumBean secretLevel = enumManagerNew.getEnumByProCode(EnumNameEnum.edoc_secret_level.name());
	        CtpEnumBean urgentLevel = enumManagerNew.getEnumByProCode(EnumNameEnum.edoc_urgent_level.name());
	        mav.addObject(EnumNameEnum.edoc_secret_level.toString(), secretLevel);
	        mav.addObject(EnumNameEnum.edoc_urgent_level.toString(), urgentLevel);
        } catch(Exception e) {
        	LOGGER.error("公文查询出错", e);
        }
        return mav;
    }
    
    /**
     * 打开增加查询条件页面
     * @param request
     * @param response
     * @return
     */
  	public ModelAndView openConditionDialog(HttpServletRequest request, HttpServletResponse response) {
  		ModelAndView mav = new ModelAndView("govdoc/query/condition_setting_dialog");
  		try {
  			Map<String, String> fieldMap = govdocElementManager.getEdocElementFieldNames(AppContext.currentAccountId());
  			
  			List<String> conditionList = GovdocListHelper.getQueryConditionList();
  			for(String fieldName : conditionList) {
  				fieldMap.remove(fieldName);
  			}

  			mav.addObject("fieldMap", fieldMap);
		} catch(Exception e) {
  			LOGGER.error("打开增加查询条件页面出错", e);
  		}
  		return mav;
  	}
  	
  	/**
  	 * 打开查询自定义页面
  	 * @param request
  	 * @param response
  	 * @return
  	 */
  	public ModelAndView openQuerySettingDialog(HttpServletRequest request, HttpServletResponse response) {
  		ModelAndView mav = new ModelAndView("govdoc/query/query_setting_dialog");
  		try {
  			Map<String, String> fieldMap = govdocElementManager.getEdocElementFieldNames(AppContext.currentAccountId());
  			
	  		mav.addObject("fieldMap", fieldMap);
  		} catch(Exception e) {
  			LOGGER.error("打开查询自定义页面出错", e);
  		}
  		return mav;
  	}
  	
  	/**
	 * 列出我的待办、已办、已发，并根据是否允许转发进行权限过滤，用在协同用引用场�?
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
    public ModelAndView list4Quote(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	ModelAndView mav = new ModelAndView("govdoc/list/list4Quote");
    	try {
    		String listType = GovdocParamUtil.getString(request, "listType", "list4Quote");
    		String govdocType = GovdocParamUtil.getString(request, "govdocType", "1");
    		
	    	Map<String, String> condition = new HashMap<String, String>();
	        condition.put("listType", listType);
	        condition.put("govdocType", govdocType);
	        
	        FlipInfo flipInfo = new FlipInfo();
	        flipInfo.setParams(condition);
	        flipInfo = govdocListManager.find4QuoteList(flipInfo, condition);
	        
	        request.setAttribute("fflist4Quote", flipInfo);
	        return listGovdoc(mav);
    	} catch(Exception e) {
    		LOGGER.error("获取待办公文列表出错", e);
    	}
    	return mav;

    }
  	
	
    private void fillZhuanfawenParams(ModelAndView mav) throws BusinessException{
    	List<MemberRole> memberRoles = orgManager.getMemberRoles(AppContext.getCurrentUser().getId(), null);
		for (MemberRole memberRole : memberRoles) {
			if("SendEdoc".equals(memberRole.getRole().getCode()) || "EdocQuickSend".equals(memberRole.getRole().getCode())){
				mav.addObject("sendedocRole",1);//有发文拟文权限，快速发文权限，可以进行转发文操作
			}
		}
		
		//boolean isZiDongBanJie=govdocOpenManager.isEdocFawenZidongBanjie();//收文是否自动办结
		String zhuanFaWen=govdocOpenManager.checkEdocZhuanfawen();//转发文默认设置
		mav.addObject("zhuanfawen",zhuanFaWen);
		//boolean isZhuanFaWenTactics=govdocOpenManager.isEdocZhuanfawenTactics();//是否启用-收文节点转发文策略
		//mav.addObject("zhuanfawenTactics",isZhuanFaWenTactics);
		//mav.addObject("zidongbanjie",isZiDongBanJie);
    }
    
    public void setEnumManagerNew(EnumManager enumManagerNew) {
		this.enumManagerNew = enumManagerNew;
	}
	public void setGovdocListManager(GovdocListManager govdocListManager) {
		this.govdocListManager = govdocListManager;
	}
	public void setGovdocElementManager(GovdocElementManager govdocElementManager) {
		this.govdocElementManager = govdocElementManager;
	}
	public void setGovdocListConfigManager(GovdocListConfigManager govdocListConfigManager) {
		this.govdocListConfigManager = govdocListConfigManager;
	}
	public void setFileToExcelManager(FileToExcelManager fileToExcelManager) {
		this.fileToExcelManager = fileToExcelManager;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public void setGovdocOpenManager(GovdocOpenManager govdocOpenManager) {
		this.govdocOpenManager = govdocOpenManager;
	}
	public void setGovdocPishiManager(GovdocPishiManager govdocPishiManager) {
		this.govdocPishiManager = govdocPishiManager;
	}
	public void setGovdocStatPushManager(GovdocStatPushManager govdocStatPushManager) {
		this.govdocStatPushManager = govdocStatPushManager;
	}
	
}
