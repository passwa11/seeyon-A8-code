package com.seeyon.apps.trustdo.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.seeyon.apps.trustdo.model.XRDBindModel;
import com.seeyon.apps.trustdo.po.XRDUserPO;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgUnit;

/**
 * 模型转换工具类
 * @author zhaopeng
 *
 */
public class XRDModelUtils {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static XRDBindModel transMOdel(V3xOrgMember vm){
		XRDBindModel xm = new XRDBindModel();
		xm.setAccount(vm==null?"":vm.getLoginName());
		xm.setRealName(vm==null?"":vm.getName());
		xm.setPhone(vm==null?"":vm.getTelNumber());
		xm.setIdCard(vm==null?"":vm.getIdNum());
		xm.setCardType("1");
		return xm;
	}
	
	public static XRDUserPO transPO(V3xOrgMember vm, V3xOrgUnit vu, V3xOrgAccount va){
		XRDUserPO xo = new XRDUserPO();
		xo.setIdIfNew();
		xo.setSeeyonUserName(vm==null?"":vm.getName());
		xo.setSeeyonUserDept(vu==null?"":vu.getName());
		xo.setTrustdoAccount(vm==null?"":vm.getLoginName());
		xo.setSeeyonLoginName(vm==null?"":vm.getLoginName());
		xo.setBindTime(sdf.format(new Date()));
		xo.setExtend2(va==null?"":va.getName());
		return xo;
	}
	
}
