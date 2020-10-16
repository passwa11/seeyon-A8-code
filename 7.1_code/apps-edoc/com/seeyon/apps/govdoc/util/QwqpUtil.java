package com.seeyon.apps.govdoc.util;

import com.seeyon.ctp.common.AppContext;

public class QwqpUtil {
	public static final String PDF_FILE="pdf";
	public static final String AIP_FILE="aip";
	
	public static String getNowFileType(){
		String aipEnabled = AppContext.getSystemProperty("qwqp.aip.enabled");
		if("1".equals(aipEnabled)){
			return AIP_FILE;//启用点聚的aip全文签批功能
		}
		String jgPDFEnabled = AppContext.getSystemProperty("qwqp.jinge.enabled");
		if("1".equals(jgPDFEnabled)){
			return PDF_FILE;//启用金格的pdf全文签批功能
		}
		return "noQWQP";//没有启用全文圈批功能
	}
	public static String getAipCustomName(){
		return AppContext.getSystemProperty("qwqp.aip.customName");
	}
}
