/*
 * Created on 2004-11-26
 *
 */
package net.joinwork.bpm.engine.wapi;

import org.dom4j.Element;

import com.seeyon.ctp.workflow.xml.StringXMLElement;

/**
 * 流程模板信息
 * @author sehnjian
 * @version 1.0
 */
public interface ProcessObject {
	public static final String VERSION = "2.3.1";
	/**
	 * 流程类型：协同流程
	 */
	public static final String FLOWTYPE_WORKFLOW = "workflow";
	/**
		 * 流程类型：会话流程
		 */
	public static final String FLOWTYPE_SESSIONFLOW = "sessionflow";
	/**
	 * 将流程模板转换到XML Element
	 * @param Root
	 */
	public void toXML(StringXMLElement Root);
	/**
	 * 从XML Element得到流程模板信息
	 * @param Root
	 */
	public void fromXML(Element Root);
	/**
		 * 返回模板流程类型
		 */
	public String getFlowType();
}
