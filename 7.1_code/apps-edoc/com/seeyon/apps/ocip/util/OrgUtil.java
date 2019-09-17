package com.seeyon.apps.ocip.util;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.seeyon.apps.ocip.OCIPConstants;
//import com.seeyon.apps.ocip.org.OrgBeanAdapter;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ProductEditionEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ocip.common.IConstant;
import com.seeyon.ocip.common.entry.Address;
import com.seeyon.ocip.common.org.OcipOrgMember;
import com.seeyon.ocip.common.organization.IOrganizationManager;
import com.seeyon.ocip.common.utils.Global;
import com.seeyon.v3x.common.web.util.ThreadLocalUtil;

public class OrgUtil {

	/**
	 * 判断对象是否为平台对象，原理为通过{@link V3xOrgEntity#getDescription()}识别<br/>
	 * 改值在对象转换时设置
	 * 
//	 * @see OrgBeanAdapter
	 * @param entity
	 * @return
	 */
	public static boolean isPlatformEntity(V3xOrgEntity entity) {
		if (entity == null) {
			return false;
		}
		return OCIPConstants.OCIP_ENTITY_FLAG.equals(entity.getDescription());
	}
	
	public static Address getAddress(V3xOrgEntity entity) {
		if (entity != null) {
			// 本地的resourceid
			String resource = Global.getSysCode();
			// 通过特殊标志判断是否为平台对象
			if (OrgUtil.isPlatformEntity(entity)) {
				resource = IConstant.OCIP_RESOURCE;
			}
			Address address = new Address();
			address.resource = resource;
			address.name = entity.getName();
			address.type = entity.getEntityType().toLowerCase();
			address.id = String.valueOf(entity.getId());
			return address;
		}
		return null;
	}
	
	/**
	 * 根据id获取平台人员对象
	 * 
	 * @param memberId 人员id，该id可能为本地值，也可能为平台值
	 * @return 平台人员对象
	 */
//	public static OcipOrgMember getPlatformMember(Long memberId) {
//		if (isNullLong(memberId)) {
//			return null;
//		}
//		V3xOrgMember v3xReciver = null;
//		// 用于线程缓存对象
//		String cacheName = OrgUtil.class.getName() + ".getPlatformMember";
//		@SuppressWarnings("unchecked")
//		Map<Long,OcipOrgMember> threadMap = (Map<Long, OcipOrgMember>) ThreadLocalUtil.get(cacheName);
//		if (threadMap == null) {
//			threadMap = new HashMap<Long, OcipOrgMember>();
//			ThreadLocalUtil.set(cacheName, threadMap);
//		}
//		// 通一次线程调用不查询多次
//		OcipOrgMember ocipOrgMember = threadMap.get(memberId);
//		if (ocipOrgMember == null || /* 检查有效期 */new Date().after(ocipOrgMember.getUpdateTime())) {
//			try {
//				OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
//				v3xReciver = orgManager.getMemberById(memberId);
//			} catch (BusinessException e1) {
//			}
//			if (OrgUtil.isPlatformEntity(v3xReciver)) {
//				ocipOrgMember = OrgBeanAdapter.convert(v3xReciver);
//			} else {
//				IOrganizationManager organizationManager = (IOrganizationManager) AppContext.getBean("organizationManager");
//				ocipOrgMember = organizationManager.getMember(String.valueOf(memberId), Global.getSysCode());
//			}
//			// 将查询结果放入集合
//			if (ocipOrgMember != null) {
//				// 设置1分钟有效期，排除线程池，线程复用情况
//				Calendar now = Calendar.getInstance();
//				now.add(Calendar.MINUTE, 1);
//				ocipOrgMember.setUpdateTime(now.getTime());
//				threadMap.put(memberId, ocipOrgMember);
//			}
//		}
//		return ocipOrgMember;
//	}
	
	/**
	 * 根据id获取平台人员对象
	 * 
	 * @param memberId 人员id，该id可能为本地值，也可能为平台值
	 * @return 平台人员对象
	 */
//	public static OcipOrgMember getPlatformMember(String memberId) {
//		Long id = null;
//		try {
//			id = Long.parseLong(memberId);
//		} catch (Exception e) {
//			return null;
//		}
//		return getPlatformMember(id);
//	}
	
	/**
	 * 判断ID是否为特殊的值，是可以不进行ocip远程查询
	 * 
	 * @param id 值
	 * @return 是否为特殊的值
	 */
	private static boolean isNullLong(Long id) {
		if (id == null || id == 0 || id == 1 || id == -1) {
			return true;
		}
		return false;
	}
	
	public static boolean isGovermentSystem(){
		String version = ProductEditionEnum.getCurrentProductEditionEnum().getValue();
		if(version.equals(ProductEditionEnum.government.getValue())){
			return true;
		}
		return false;
	}
}
