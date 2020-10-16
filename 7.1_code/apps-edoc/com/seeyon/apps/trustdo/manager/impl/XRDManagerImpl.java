package com.seeyon.apps.trustdo.manager.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import com.seeyon.ctp.util.Strings;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.trustdo.constants.TrustdoErrorMsg;
import com.seeyon.apps.trustdo.dao.XRDDao;
import com.seeyon.apps.trustdo.manager.XRDManager;
import com.seeyon.apps.trustdo.model.sdk.CancelData;
import com.seeyon.apps.trustdo.model.sdk.KeyIdData;
import com.seeyon.apps.trustdo.model.sdk.Result;
import com.seeyon.apps.trustdo.po.XRDUserPO;
import com.seeyon.apps.trustdo.utils.XRDAppUtils;
import com.seeyon.apps.trustdo.utils.XRDModelUtils;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgUnit;
import com.seeyon.ctp.organization.manager.OrgManager;
//import com.seeyon.ctp.organization.principal.PrincipalManager;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.ctp.util.json.JSONUtil;

public class XRDManagerImpl implements XRDManager {
	
	private static final Log LOGGER = CtpLogFactory.getLog(XRDManagerImpl.class);

	private OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");

//	private PrincipalManager principalManager = (PrincipalManager) AppContext.getBean("principalManager");

	private XRDDao xrdDao;

	public XRDDao getXrdDao() {
		return xrdDao;
	}

	public void setXrdDao(XRDDao xrdDao) {
		this.xrdDao = xrdDao;
	}

	@Override
	public void save(XRDUserPO xrdPo) {
		xrdDao.save(xrdPo);
	}

	@Override
	public void del(XRDUserPO xrdPo) {
		xrdDao.del(xrdPo);
	}

	@Override
	public void saveList(List<XRDUserPO> xrdPoList) {
		for (XRDUserPO xrdPo : xrdPoList) {
			this.save(xrdPo);
		}
	}
	
	@Override
	@AjaxAccess
	public int delList(List<String> appID) {
		int resultCode = 0;
		if ((appID != null) && (!(appID.isEmpty()))) {
			for (Iterator<String> localIterator = appID.iterator(); localIterator.hasNext();) {
				String idStr = (String) localIterator.next();
				XRDUserPO xrdPo = xrdDao.getOneByid(Long.valueOf(Long.parseLong(idStr)));
				if (xrdPo != null) {
					Result<?> removeResult = XRDAppUtils.removeBind(xrdPo.getTrustdoAccount());
					CancelData cancelData = JSONUtil.parseJSONString(JSONUtil.toJSONString(removeResult.getData()==null?"":removeResult.getData()), CancelData.class);
					if(removeResult != null && removeResult.getErrCode().equals(TrustdoErrorMsg.SUCCESS_CODE) && cancelData.isOperate()){
						LOGGER.debug("removeResult is : " + removeResult);
						this.del(xrdPo);
						resultCode = 200;
					} else {
						LOGGER.debug(xrdPo.getTrustdoAccount() + " REMOVE_BIND_FAILED!");
						resultCode = 201;
						break;
					}
				} else {
					resultCode = 202;
				}
			}
		}
		return resultCode;
	}

	@Override
	@AjaxAccess
	public FlipInfo getAll(FlipInfo info, Map<String, Object> params) {
		List<XRDUserPO> list = xrdDao.getAll(params, info);
		info.setData(list);
		return info;
	}
	
	
	/**@Override
	@AjaxAccess
	public String bind(JSONObject json) {
		List<Map<String,String>> resultList = new ArrayList<Map<String,String>>();
		String authedScopeIds = json.getString("authedScopeIds");
		Map<String,String> map = new HashMap<String,String>();
		Map<Long,String> loginMap = principalManager.getMemberIdLoginNameMap();
		if(!StringUtils.isBlank(authedScopeIds)){
			LOGGER.debug("bind params is : " + authedScopeIds);
			String[] str = authedScopeIds.split(",");
			V3xOrgMember vm = null;
			V3xOrgUnit vu = null;
			String loginName = null;
			Result<?> bindResult = null;
			for (String s : str) {
				LOGGER.debug("bind ------------log: userId is = " + s);
				s = s.substring(s.indexOf("|") + 1, s.length());
				try {
					vm = this.orgManager.getMemberById(Long.parseLong(s));
					LOGGER.debug("bind ------------log: vm is = " + vm);
//					loginName = principalManager.getLoginNameByMemberId(Long.parseLong(s));
					loginName = loginMap.get(Long.parseLong(s));
					LOGGER.debug("bind ------------log: loginName is = " + loginName);
					vu = this.orgManager.getUnitById(vm.getOrgDepartmentId());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (BusinessException e) {
					e.printStackTrace();
				}
					
				if (vm != null) {
					List<XRDUserPO> queryList = xrdDao.getOneByTrustdoAccount(loginName);
					if(queryList!=null && queryList.size()>0){
						map.put(loginName, TrustdoErrorMsg.ACCOUNT_HAS_BIND);
						resultList.add(map);
					}else{
						bindResult = XRDAppUtils.toNewBind(XRDModelUtils.transMOdel(vm, loginName));
						if (bindResult != null && bindResult.getErrCode().equals(TrustdoErrorMsg.SUCCESS_CODE)) {
							LOGGER.debug("bindResult is : " + bindResult);
							KeyIdData keyIdData = JSONUtil.parseJSONString(JSONUtil.toJSONString(bindResult.getData()), KeyIdData.class);
							XRDUserPO po = XRDModelUtils.transPO(vm, vu, loginName);
							po.setKeyId(keyIdData.getKeyId());
							this.save(po);
						} else {
							map.put(loginName, TrustdoErrorMsg.ACCOUNT_BIND_FAILED+":"+bindResult.getMsg());
							resultList.add(map);
						}
					}
				}
			}
		}else{
			map.put(TrustdoErrorMsg.ERROR_MSG, TrustdoErrorMsg.ACCOUNT_CHOOSE_ERROR);
			resultList.add(map);
		}
		return JSONUtil.toJSONString(resultList);
	}*/

	@Override
	public XRDUserPO get(String id) {
		return xrdDao.getOneByid(Long.parseLong(id));
	}
	
	
	@Override
	@AjaxAccess
	public String bind(JSONObject json) {
		List<Map<String,String>> resultList = new ArrayList<Map<String,String>>();
		String authedScopeIds = json.getString("authedScopeIds");
		Map<String,String> map = new HashMap<String,String>();
		if(!StringUtils.isBlank(authedScopeIds)){
			LOGGER.debug("bind params is : " + authedScopeIds);
			String[] str = authedScopeIds.split(",");
			List<String> departmentIds = new ArrayList<String>();
			try {
				//过滤单位id，先进行个人账户绑定
				for (String s : str) {
					if (s.startsWith("Department")) {
						departmentIds.add(s.substring(s.indexOf("|") + 1, s.length()));
						continue;
					}
					s = s.substring(s.indexOf("|") + 1, s.length());
					V3xOrgMember vm = this.orgManager.getMemberById(Long.parseLong(s));
					resultList = toBind(vm, resultList);
				}
				//获取并循环绑定企业下人员进行绑定
				for (String s : departmentIds) {
					List<V3xOrgMember> list = orgManager.getMembersByDepartment(Long.parseLong(s), false);
					for (V3xOrgMember vm : list) {
						resultList = toBind(vm, resultList);
					}
				}
			} catch (Exception e) {
				LOGGER.error(e);
				//e.printStackTrace();
			} 
		}else{
			map.put(TrustdoErrorMsg.ERROR_MSG, TrustdoErrorMsg.ACCOUNT_CHOOSE_ERROR);
			resultList.add(map);
		}
		return JSONUtil.toJSONString(resultList);
	}
	
	private List<Map<String,String>> toBind(V3xOrgMember vm, List<Map<String,String>> resultList) throws BusinessException {
		V3xOrgUnit vu = null;
		Result<?> bindResult = null;
		Map<String,String> map = null;
		if (vm != null) {
			map = new HashMap<String,String>();
			List<XRDUserPO> queryList = xrdDao.getOneByTrustdoAccount(vm.getLoginName());
			if(queryList!=null && queryList.size()>0){
				return resultList;
			}else{
				bindResult = XRDAppUtils.toNewBind(XRDModelUtils.transMOdel(vm));
				LOGGER.info("loginName : "+vm.getLoginName()+" bindResult is " + bindResult);
				if (bindResult != null && bindResult.getErrCode().equals(TrustdoErrorMsg.SUCCESS_CODE)) {
					KeyIdData keyIdData = JSONUtil.parseJSONString(JSONUtil.toJSONString(bindResult.getData()), KeyIdData.class);
					vu = this.orgManager.getUnitById(vm.getOrgDepartmentId());
					V3xOrgAccount va = this.orgManager.getAccountById(vm.getOrgAccountId());
					XRDUserPO po = XRDModelUtils.transPO(vm, vu, va);
					if (keyIdData != null && Strings.isNotEmpty(keyIdData.getKeyId())) {
						po.setKeyId(keyIdData.getKeyId());
						this.save(po);
					} else {
						map.put(vm.getLoginName(), TrustdoErrorMsg.ACCOUNT_BIND_FAILED+":"+bindResult.getMsg());
						resultList.add(map);
					}
				} else {
					if(bindResult != null){
						map.put(vm.getLoginName(), TrustdoErrorMsg.ACCOUNT_BIND_FAILED+":"+bindResult.getMsg());
					}					
					resultList.add(map);
				}
			}
		}
		return resultList;
	}
	
	/*public static void main(String[] args) {
		String authedScopeIds = "Department|-1648018871201872857,Member|6064676801193394736,Member|6064676801193394748";
		String[] str = authedScopeIds.split(",");
		List<String> departmentIds = new ArrayList<String>();
		for (String s : str) {
			if (s.startsWith("Department")) {
				departmentIds.add(s.substring(s.indexOf("|") + 1, s.length()));
				continue;
			}
			s = s.substring(s.indexOf("|") + 1, s.length());
			System.out.println(s);
		}
	}*/
	
}
