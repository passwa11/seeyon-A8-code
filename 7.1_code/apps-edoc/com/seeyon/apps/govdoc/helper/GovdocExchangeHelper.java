package com.seeyon.apps.govdoc.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.encrypt.CoderFactory;
import com.seeyon.ctp.organization.bo.V3xOrgUnit;
import com.seeyon.ocip.common.IConstant.AddressType;
import com.seeyon.ocip.common.entry.Address;
import com.seeyon.ocip.common.organization.IOrganizationManager;

public class GovdocExchangeHelper extends GovdocHelper {

	private static final Log LOGGER = LogFactory.getLog(GovdocHelper.class);
	
	private static final IOrganizationManager organizationManager = (IOrganizationManager)AppContext.getBean("organizationManager");
	
	/**
	 * 
	 * @param fileIn
	 * @param fileOut
	 */
	public static void copyFile(File fileIn, File fileOut) {
		FileOutputStream fop = null;
	    FileInputStream fin=null;
		    try {
				if (!fileOut.exists()){
					if (!fileOut.getParentFile().exists()) {
					    if (!fileOut.getParentFile().mkdirs()) {
					    	System.out.println("新建文件目录失败（" + fileOut.getParentFile() + "）");
					    	LOGGER.error("新建文件目录失败（" + fileOut.getParentFile() + "）");
				    }
				}
				try {
				    fileOut.createNewFile();
				}catch (IOException e){
				    LOGGER.error(e);
				}
			}
			fop = new FileOutputStream(fileOut);
			fin = new FileInputStream(fileIn);
			CoderFactory.getInstance().download(fin, fop);
			fop.flush();
		} catch (IOException e) {
			LOGGER.error("文件=" + fileIn.getName() + " 解密错误！",e);
			throw new IllegalArgumentException("文件=" + fileIn.getName() + " 解密错误！");
		} catch (Exception e) {
			LOGGER.error("文件=" + fileIn.getName() + " 解密错误！",e);
			throw new IllegalArgumentException("文件=" + fileIn.getName() + "  解密错误！");
		} finally {
		    try {
		        if (fop != null) {
		            fop.close();
		        }
		        if (fin != null) {
		            fin.close();
		        }
		    } catch (IOException e) {
		    	LOGGER.error("文件=" + fileIn.getName() + " 解密关闭流错误！",e);
		    }
		}
	}

	/**
	 * 
	 * @param address
	 * @return
	 */
	public static String getTypeAndIds(Address address) {
		String typeAndIds = "";
		if (address.getType().equals(AddressType.account.name())) {
			typeAndIds = "Account|" + address.getId();
		} else if (address.getType().equals(AddressType.department.name())) {
			typeAndIds = "Department|" + address.getId();
		} else if (address.getType().equals(AddressType.member.name())) {
			typeAndIds = "Member|" + address.getId();
		}
		return typeAndIds;
	}

	/**
	 * 
	 * @param address
	 * @return
	 */
	public static V3xOrgUnit getLocalOrgUnit(Address address) {
		V3xOrgUnit orgUnit = null;
		try {
			String localObjectId = address.getId();
			if(organizationManager!=null)
				localObjectId = organizationManager.getLocalObjectId(address);
			orgUnit = orgManager.getUnitById(Long.parseLong(localObjectId));
		} catch (Exception e) {
			LOGGER.error("平台单位信息转换为本系统单位信息失败", e);
		}
		return orgUnit;
	}
	
	
}
