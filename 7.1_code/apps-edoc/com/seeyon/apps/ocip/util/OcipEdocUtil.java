package com.seeyon.apps.ocip.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import com.seeyon.apps.ocip.OCIPConstants;
//import com.seeyon.apps.ocip.exchange.edoc.EdocOFCExchangeHandler;
//import com.seeyon.apps.ocip.org.OrgBeanAdapter;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.form.util.EntityPo;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.dao.OrgDao;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ocip.common.IConstant;
import com.seeyon.ocip.common.IConstant.AddressType;
import com.seeyon.ocip.common.entry.Address;
import com.seeyon.ocip.common.organization.IOrganizationManager;
import com.seeyon.ocip.common.utils.Global;
import com.seeyon.ocip.exchange.model.Organization;
import com.seeyon.v3x.edoc.dao.EdocObjTeamDao;
import com.seeyon.v3x.edoc.domain.EdocObjTeam;
import com.seeyon.v3x.exchange.domain.ExchangeAccount;
import com.seeyon.v3x.exchange.manager.ExchangeAccountManager;

public class OcipEdocUtil {
	
	private static final Log LOGGER = CtpLogFactory.getLog(OcipEdocUtil.class);
	public static final OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
	public static final OrgDao orgDao = (OrgDao) AppContext.getBean("orgDao");
	public static final EdocObjTeamDao edocObjTeamDao = (EdocObjTeamDao) AppContext.getBean("edocObjTeamDao");
	public static final ExchangeAccountManager exchangeAccountManager = (ExchangeAccountManager) AppContext.getBean("exchangeAccountManager");

	public static List<Organization> convertStringToOrganization(IOrganizationManager organizationManager,String typeAndIds) {
		List<Organization> organizations = new ArrayList<Organization>();
		if(Strings.isNotBlank(typeAndIds)){
			try {
				OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
				String[] accounts = typeAndIds.split(",");
				List entities = new ArrayList();
				for (String string : accounts) {
					try{
						V3xOrgEntity entity = orgManager.getEntity(string);
						entities.add(entity);
					}catch(Exception e){
						entities.add(string);
					}
				}
				for (Object obj : entities) {
					if(obj instanceof V3xOrgEntity){
						V3xOrgEntity v3xOrgEntity = (V3xOrgEntity)obj;
						Address add = new Address();
						Organization org = new Organization();
						add = OrgUtil.getAddress(v3xOrgEntity);
						if(!OrgUtil.isPlatformEntity(v3xOrgEntity)){
							if (organizationManager != null) {
								String id = organizationManager.getPlatformId(add);
								if (id != null) {
									add.setId(id);
									add.setResource(IConstant.OCIP_RESOURCE);
								} 
							}
						}
						org.setIdentification(add);
						org.setName(v3xOrgEntity.getName());
						organizations.add(org);
					}
					if(obj instanceof String){
						String account = (String)obj;
						Address add = new Address();
						Organization org = new Organization();
						add.setId(account);
						add.setName(account);
						add.setResource(account);
						add.setType(AddressType.system.name());
						org.setIdentification(add);
						org.setName(account);
						organizations.add(org);
					}
				}
			} catch (Exception e) {
				LOGGER.error(e);
				//e.printStackTrace();
			}
		}
		return organizations;
	}
	//public static List<Organization> getOrganization(IOrganizationManager organizationManager,String value) throws BusinessException {
//		List<Organization> organizations = new ArrayList<Organization>();
//		String unitName = value;
//		StringBuilder unitId = new StringBuilder();
//		Pattern pattern = Pattern.compile("([a-zA-Z]+[\\|])?([-]?[0-9]{10,20})");
//		Matcher matcher = pattern.matcher(value);
//		String group = null;
//		Long entityid = null;
//		List entities = new ArrayList();
//		List<EntityPo> entitypo = new ArrayList<EntityPo>();
//		while (matcher.find()) {//如果有account+id
//			EntityPo entityPo = new EntityPo();
//			group = matcher.group(0);
//			entityid = Long.parseLong(matcher.group(2));
//			V3xOrgEntity entity = orgManager.getEntityOnlyById(entityid);
//			if (entity != null) {
//				unitName = unitName.replace(group, entity.getName());
//				unitId.append(group).append(",");
//				entityPo.setAccountId(group);
//				entityPo.setAccountName(entity.getName());
//				entities.add(entity);
//			}
//		}
//		unitName = unitName.replaceAll("[,]+", "");
//		try {
//			//需要提炼出手写单位，很麻烦
//			
//			String[] unitarray =  unitName.split("、");
//			for (EntityPo enpo : entitypo) {
//				if
//			}
//			
//			for (Object obj : entities) {
//				if(obj instanceof V3xOrgEntity){
//					V3xOrgEntity v3xOrgEntity = (V3xOrgEntity)obj;
//					Address add = new Address();
//					Organization org = new Organization();
//					add = OrgUtil.getAddress(v3xOrgEntity);
//					if(!OrgUtil.isPlatformEntity(v3xOrgEntity)){
//						if (organizationManager != null) {
//							String id = organizationManager.getPlatformId(add);
//							if (id != null) {
//								add.setId(id);
//								add.setResource(IConstant.OCIP_RESOURCE);
//							} 
//						}
//					}
//					org.setIdentification(add);
//					org.setName(v3xOrgEntity.getName());
//					organizations.add(org);
//				}
//				if(obj instanceof String){
//					String account = (String)obj;
//					Address add = new Address();
//					Organization org = new Organization();
//					add.setId(account);
//					add.setName(account);
//					add.setResource(account);
//					add.setType(AddressType.system.name());
//					org.setIdentification(add);
//					org.setName(account);
//					organizations.add(org);
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return result;
	//}
}
