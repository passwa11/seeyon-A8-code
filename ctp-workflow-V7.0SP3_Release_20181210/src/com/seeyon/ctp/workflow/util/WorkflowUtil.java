/*
 * Created on 2005-1-19
 *
 */
package com.seeyon.ctp.workflow.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.formula.manager.FormulaManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.formula.Formula;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgPost;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.organization.manager.JoinOrgManagerDirect;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.UniqueList;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.bo.WorkflowFormFieldBO;
import com.seeyon.ctp.workflow.engine.enums.ConditionType;
import com.seeyon.ctp.workflow.engine.listener.ActionRunner;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.manager.ProcessManager;
import com.seeyon.ctp.workflow.manager.ProcessOrgManager;
import com.seeyon.ctp.workflow.manager.ProcessTemplateManager;
import com.seeyon.ctp.workflow.manager.ProcessXmlTempManager;
import com.seeyon.ctp.workflow.manager.WorkFlowAppExtendInvokeManager;
import com.seeyon.ctp.workflow.manager.WorkFlowMatchUserManager;
import com.seeyon.ctp.workflow.manager.WorkflowFormDataMapInvokeManager;
import com.seeyon.ctp.workflow.manager.WorkflowSimulationManager;
import com.seeyon.ctp.workflow.po.HistoryWorkitemDAO;
import com.seeyon.ctp.workflow.po.ProcessInRunningDAO;
import com.seeyon.ctp.workflow.po.ProcessTemplete;
import com.seeyon.ctp.workflow.po.ProcessXmlTemp;
import com.seeyon.ctp.workflow.po.SubProcessSetting;
import com.seeyon.ctp.workflow.po.WorkitemDAO;
import com.seeyon.ctp.workflow.supernode.beans.SuperNodeBean;
import com.seeyon.ctp.workflow.supernode.manager.WorkflowSuperNodeManager;
import com.seeyon.ctp.workflow.util.condition.ConditionValidateUtil;
import com.seeyon.ctp.workflow.util.condition.Expression;
import com.seeyon.ctp.workflow.util.condition.ExpressionFactory;
import com.seeyon.ctp.workflow.vo.BPMChangeMessageVO;
import com.seeyon.ctp.workflow.vo.CPMatchResultVO;
import com.seeyon.ctp.workflow.vo.TemplateIEMessageVO;
import com.seeyon.ctp.workflow.vo.User;
import com.seeyon.ctp.workflow.wapi.WorkFlowAppExtendManager;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.ctp.workflow.wapi.WorkflowFormDataMapManager;
import com.seeyon.ctp.workflow.xml.StringXMLElement;

import net.joinwork.bpm.definition.BPMAbstractNode;
import net.joinwork.bpm.definition.BPMAbstractNode.NodeType;
import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMActor;
import net.joinwork.bpm.definition.BPMAndRouter;
import net.joinwork.bpm.definition.BPMCircleTransition;
import net.joinwork.bpm.definition.BPMEnd;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMParticipant;
import net.joinwork.bpm.definition.BPMParticipantType;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;
import net.joinwork.bpm.definition.BPMStart;
import net.joinwork.bpm.definition.BPMStatus;
import net.joinwork.bpm.definition.BPMTransition;
import net.joinwork.bpm.definition.ObjectName;
import net.joinwork.bpm.engine.execute.BPMCase;
import net.joinwork.bpm.engine.execute.DynamicFormMasterInfo;
import net.joinwork.bpm.engine.execute.ProcessEngineImpl;
import net.joinwork.bpm.engine.log.BPMCaseLog;
import net.joinwork.bpm.engine.wapi.CaseDetailLog;
import net.joinwork.bpm.engine.wapi.WorkItem;
import net.joinwork.bpm.engine.wapi.WorkItemManager;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;
import net.joinwork.bpm.task.BPMWorkItemList;
import net.joinwork.bpm.task.WorkitemInfo;

/**
 * 
 * @author jackywcw
 *
 */
public class WorkflowUtil {
    private final static Log       logger = CtpLogFactory.getLog(WorkflowUtil.class);
	public final static String ENCODING = "UTF-8";
	
	private static WorkflowApiManager wapi = (WorkflowApiManager)AppContext.getBean("wapi");
	
	public final static String VJOIN= "SVjoin";
	/**
	 * 
	 */
	private WorkflowUtil() {
	}
	
	public static String parseConditionsOfNodes(CPMatchResultVO crvo) {
		String conditon_Str= "";
           Set<String> allSelectNodes= crvo.getAllSelectNodes();
           int i= 0;
           if(null!= allSelectNodes && !allSelectNodes.isEmpty()){
               for (String value : allSelectNodes) {
                   if(i==0){
                       conditon_Str +="{\"condition\":[";
                       conditon_Str +="{\"nodeId\":\""+value+"\",";
                       conditon_Str +="\"isDelete\":\"false\"}";
                   }else{
                       conditon_Str +=",{\"nodeId\":\""+value+"\",";
                       conditon_Str +="\"isDelete\":\"false\"}";
                   }
                   i++;
               }
           }
           Set<String> allNotSelectNodes= crvo.getAllNotSelectNodes();
           if(null!= allNotSelectNodes && !allNotSelectNodes.isEmpty()){
               for (String value : allNotSelectNodes) {
                   if(i==0){
                       conditon_Str +="{\"condition\":[";
                       conditon_Str +="{\"nodeId\":\""+value+"\",";
                       conditon_Str +="\"isDelete\":\"true\"}";
                   }else{
                       conditon_Str +=",{\"nodeId\":\""+value+"\",";
                       conditon_Str +="\"isDelete\":\"true\"}";
                   }
                   i++;
               }
           }
           if(Strings.isNotBlank(conditon_Str)){
        	   conditon_Str +="]}";
           }
		return conditon_Str;
	}
	
	public static String replaceMark(
		String str,
		String destStr,
		String srcStr) {


		StringBuffer retVal = new StringBuffer();

		//记录查找到相似字符的位置

		int findStation = str.indexOf(destStr);

		int resumStation = 0;

		if (findStation > -1) {
			String findStr = str.substring(resumStation, findStation);
			
			retVal.append(findStr);

			retVal.append(srcStr);
			retVal.append(str.substring(findStation));
			return retVal.toString();
		}
		
		return str;

	}
	public static String replaceMarkAll(
			String str,
			String destStr,
			String srcStr) {


			StringBuffer retVal = new StringBuffer();

			//记录查找到相似字符的位置

			int findStation = str.indexOf(destStr);

			int resumStation = 0;

			while (findStation > -1) {
				String findStr = str.substring(resumStation, findStation);
			
				retVal.append(findStr);

				retVal.append(srcStr);

				resumStation = findStation + destStr.length();

				findStation = str.indexOf(destStr, resumStation);

			}
			retVal.append(str.substring(resumStation));
			return retVal.toString();

		}

	/**
	 * 此方法将给出的字符串source使用delim划分为单词数组�?
	 * @param source 需要进行划分的原字符串
	 * @param delim 单词的分隔字符串
	 * @return 划分以后的数组，如果source为null的时候返回以source为唯一元素的数组，
	 *         如果delim为null则使用逗号作为分隔字符串�?
	 * @since  0.1
	 */
	public static String[] split(String source, String delim) {
		String[] wordLists;
		if (source == null) {
			wordLists = new String[1];
			wordLists[0] = "";
			return wordLists;
		}
		if (delim == null) {
			delim = ",";
		}
		StringTokenizer st = new StringTokenizer(source, delim);
		int total = st.countTokens();
		wordLists = new String[total];
		for (int i = 0; i < total; i++) {
			wordLists[i] = st.nextToken();
		}
		return wordLists;
	}

	/**
	 * 此方法将给出的字符串source使用delim划分为单词数组�?
	 * @param source 需要进行划分的原字符串
	 * @param delim 单词的分隔字�?
	 * @return 划分以后的数组，如果source为null的时候返回以source为唯一元素的数组�?
	 * @since  0.2
	 */
	public static String[] split(String source, char delim) {
		return split(source, String.valueOf(delim));
	}

	/**
	 * 此方法将给出的字符串source使用逗号划分为单词数组�?
	 * @param source 需要进行划分的原字符串
	 * @return 划分以后的数组，如果source为null的时候返回以source为唯一元素的数组�?
	 * @since  0.1
	 */
	public static String[] split(String source) {
		return split(source, ",");
	}

	/**
	 * 循环打印字符串数组�?
	 * 字符串数组的各元素间以指定字符分隔，如果字符串中已经包含指定字符则在字符串的两端加上双引号�?
	 * @param strings 字符串数�?
	 * @param delim 分隔�?
	 * @param out 打印到的输出�?
	 * @since  0.4
	 */
	public static void printStrings(
		String[] strings,
		String delim,
		OutputStream out) {
		try {
			if (strings != null) {
				int length = strings.length - 1;
				for (int i = 0; i < length; i++) {
					if (strings[i] != null) {
						if (strings[i].indexOf(delim) > -1) {
							out.write(
								("\"" + strings[i] + "\"" + delim).getBytes());
						} else {
							out.write((strings[i] + delim).getBytes());
						}
					} else {
						out.write("null".getBytes());
					}
				}
				if (strings[length] != null) {
					if (strings[length].indexOf(delim) > -1) {
						out.write(("\"" + strings[length] + "\"").getBytes());
					} else {
						out.write(strings[length].getBytes());
					}
				} else {
					out.write("null".getBytes());
				}
			} else {
				out.write("null".getBytes());
			}
			//	out.write(Constants.LINE_SEPARATOR.getBytes());
		} catch (IOException e) {

		}
	}

	/**
	 * 循环打印字符串数组到标准输出�?
	 * 字符串数组的各元素间以指定字符分隔，如果字符串中已经包含指定字符则在字符串的两端加上双引号�?
	 * @param strings 字符串数�?
	 * @param delim 分隔�?
	 * @since  0.4
	 */
	/*public static void printStrings(String[] strings, String delim) {
		printStrings(strings, delim, System.out);
	}*/

	/**
	 * 循环打印字符串数组�?
	 * 字符串数组的各元素间以逗号分隔，如果字符串中已经包含逗号则在字符串的两端加上双引号�?
	 * @param strings 字符串数�?
	 * @param out 打印到的输出�?
	 * @since  0.2
	 */
	public static void printStrings(String[] strings, OutputStream out) {
		printStrings(strings, ",", out);
	}

	/**
	 * 循环打印字符串数组到系统标准输出流System.out�?
	 * 字符串数组的各元素间以逗号分隔，如果字符串中已经包含逗号则在字符串的两端加上双引号�?
	 * @param strings 字符串数�?
	 * @since  0.2
	 */
	/*public static void printStrings(String[] strings) {
		printStrings(strings, ",", System.out);
	}
*/
	/**
	 * 将字符串中的变量使用values数组中的内容进行替换�?
	 * 替换的过程是不进行嵌套的，即如果替换的内容中包含变量表达式时不会替换�?
	 * @param prefix 变量前缀字符�?
	 * @param source 带参数的原字符串
	 * @param values 替换用的字符串数�?
	 * @return 替换后的字符串�?
	 *         如果前缀为null则使用�?”作为前缀�?
	 *         如果source或者values为null或者values的长度为0则返回source�?
	 *         如果values的长度大于参数的个数，多余的值将被忽略；
	 *         如果values的长度小于参数的个数，则后面的所有参数都使用最后一个值进行替换�?
	 * @since  0.2
	 */
	public static String getReplaceString(
		String prefix,
		String source,
		String[] values) {
		String result = source;
		if (source == null || values == null || values.length < 1) {
			return source;
		}
		if (prefix == null) {
			prefix = "%";
		}

		for (int i = 0; i < values.length; i++) {
			String argument = prefix + Integer.toString(i + 1);
			int index = result.indexOf(argument);
			if (index != -1) {
				String temp = result.substring(0, index);
				if (i < values.length) {
					temp += values[i];
				} else {
					temp += values[values.length - 1];
				}
				temp += result.substring(index + 2);
				result = temp;
			}
		}
		return result;
	}

	/**
	 * 将字符串中的变量（以�?”为前导后接数字）使用values数组中的内容进行替换�?
	 * 替换的过程是不进行嵌套的，即如果替换的内容中包含变量表达式时不会替换�?
	 * @param source 带参数的原字符串
	 * @param values 替换用的字符串数�?
	 * @return 替换后的字符�?
	 * @since  0.1
	 */
	public static String getReplaceString(String source, String[] values) {
		return getReplaceString("%", source, values);
	}

	/**
	 * 字符串数组中是否包含指定的字符串�?
	 * @param strings 字符串数�?
	 * @param string 字符�?
	 * @param caseSensitive 是否大小写敏�?
	 * @return 包含时返回true，否则返回false
	 * @since  0.4
	 */
	public static boolean contains(
		String[] strings,
		String string,
		boolean caseSensitive) {
		for (int i = 0; i < strings.length; i++) {
			if (caseSensitive == true) {
				if (strings[i].equals(string)) {
					return true;
				}
			} else {
				if (strings[i].equalsIgnoreCase(string)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 字符串数组中是否包含指定的字符串。大小写敏感�?
	 * @param strings 字符串数�?
	 * @param string 字符�?
	 * @return 包含时返回true，否则返回false
	 * @since  0.4
	 */
	public static boolean contains(String[] strings, String string) {
		return contains(strings, string, true);
	}

	/**
	 * 不区分大小写判定字符串数组中是否包含指定的字符串�?
	 * @param strings 字符串数�?
	 * @param string 字符�?
	 * @return 包含时返回true，否则返回false
	 * @since  0.4
	 */
	public static boolean containsIgnoreCase(String[] strings, String string) {
		return contains(strings, string, false);
	}

	/**
	 * 将字符串数组使用指定的分隔符合并成一个字符串�?
	 * @param array 字符串数�?
	 * @param delim 分隔符，为null的时候使�?"作为分隔符（即没有分隔符�?
	 * @return 合并后的字符�?
	 * @since  0.4
	 */
	public static String combineStringArray(String[] array, String delim) {
		int length = array.length - 1;
		if (delim == null) {
			delim = "";
		}
		StringBuffer result = new StringBuffer(length * 8);
		for (int i = 0; i < length; i++) {
			result.append(array[i]);
			result.append(delim);
		}
		result.append(array[length]);
		return result.toString();
	}

	/**
	 * 以指定的字符和长度生成一个该字符的指定长度的字符串�?
	 * @param c 指定的字�?
	 * @param length 指定的长�?
	 * @return 最终生成的字符�?
	 * @since  0.6
	 */
	public static String fillString(char c, int length) {
		String ret = "";
		for (int i = 0; i < length; i++) {
			ret += c;
		}
		return ret;
	}

	/**
	 * 去除左边多余的空格�?
	 * @param value 待去左边空格的字符串
	 * @return 去掉左边空格后的字符�?
	 * @since  0.6
	 */
	public static String trimLeft(String value) {
		String result = value;
		if (result == null)
			return result;
		char ch[] = result.toCharArray();
		int index = -1;
		for (int i = 0; i < ch.length; i++) {
			if (Character.isWhitespace(ch[i])) {
				index = i;
			} else {
				break;
			}
		}
		if (index != -1) {
			result = result.substring(index + 1);
		}
		return result;
	}

	/**
	 * 去除右边多余的空格�?
	 * @param value 待去右边空格的字符串
	 * @return 去掉右边空格后的字符�?
	 * @since  0.6
	 */
	public static String trimRight(String value) {
		String result = value;
		if (result == null)
			return result;
		char ch[] = result.toCharArray();
		int endIndex = -1;
		for (int i = ch.length - 1; i > -1; i--) {
			if (Character.isWhitespace(ch[i])) {
				endIndex = i;
			} else {
				break;
			}
		}
		if (endIndex != -1) {
			result = result.substring(0, endIndex);
		}
		return result;
	}

	/**
	 * 根据转义列表对字符串进行转义�?
	 * @param source 待转义的字符�?
	 * @param escapeCharMap 转义列表
	 * @return 转义后的字符�?
	 * @since  0.6
	 */
	public static String escapeCharacter(
		String source,
		HashMap escapeCharMap) {
		if (source == null || source.length() == 0)
			return source;
		if (escapeCharMap.size() == 0)
			return source;
		StringBuffer sb = new StringBuffer();
		StringCharacterIterator sci = new StringCharacterIterator(source);
		for (char c = sci.first();
			c != StringCharacterIterator.DONE;
			c = sci.next()) {
			String character = String.valueOf(c);
			if (escapeCharMap.containsKey(character))
				character = (String) escapeCharMap.get(character);
			sb.append(character);
		}
		return sb.toString();
	}
	/**
	 * 得到字符串的字节长度�?
	 * @param source 字符�?
	 * @return 字符串的字节长度
	 * @since  0.6
	 */
	public static int getByteLength(String source) {
		int len = 0;
		for (int i = 0; i < source.length(); i++) {
			char c = source.charAt(i);
			int highByte = c >>> 8;
			len += highByte == 0 ? 1 : 2;
		}
		return len;
	}
	/**
	 * 得到字符串中的子串的个数�?
	 * @param source 字符�?
	 * @param sub 子串
	 * @return 字符串中的子串的个数
	 * @since  0.6
	 */
	public static int getSubtringCount(String source, String sub) {
		if (source == null || source.length() == 0) {
			return 0;
		}
		int count = 0;
		int index = source.indexOf(sub);
		while (index >= 0) {
			count++;
			index = source.indexOf(sub, index + 1);
		}
		return count;
	}

	public static String toUTF8(String str) {
		try {
			if (str == null || "".equals(str))
				return str;
			String retStr = new String(str.getBytes("ISO8859_1"), "UTF-8");
			return retStr;
		} catch (UnsupportedEncodingException e) {
		    logger.error("发生异常!",e);
			return "";
		}
	}

    //seeyon. modified by lius.
    //oupput .process encoding issue.
    public static String asXML(Document doc){
		if(doc==null) return "";
		StringWriter writer = new StringWriter();
        try {
            doc.write(writer);
        } catch (IOException e) {
            logger.error("发生异常!",e);
			return "";
		}
		return writer.getBuffer().toString();
    }
    
    /**
     * 是否为非空（空的话返回false，非空返回true）
     * @param string
     * @return
     */
    public static boolean isNotNull(String string) {
        if(string==null){
            return false;
        }
        if("".equals(string.trim())){
            return false;
        }
        return true;
    }
    
    private static String[] hex = { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D",
            "0E", "0F", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1A", "1B", "1C", "1D", "1E", "1F",
            "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2A", "2B", "2C", "2D", "2E", "2F", "30", "31",
            "32", "33", "34", "35", "36", "37", "38", "39", "3A", "3B", "3C", "3D", "3E", "3F", "40", "41", "42", "43",
            "44", "45", "46", "47", "48", "49", "4A", "4B", "4C", "4D", "4E", "4F", "50", "51", "52", "53", "54", "55",
            "56", "57", "58", "59", "5A", "5B", "5C", "5D", "5E", "5F", "60", "61", "62", "63", "64", "65", "66", "67",
            "68", "69", "6A", "6B", "6C", "6D", "6E", "6F", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79",
            "7A", "7B", "7C", "7D", "7E", "7F", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "8A", "8B",
            "8C", "8D", "8E", "8F", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9A", "9B", "9C", "9D",
            "9E", "9F", "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "AA", "AB", "AC", "AD", "AE", "AF",
            "B0", "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "BA", "BB", "BC", "BD", "BE", "BF", "C0", "C1",
            "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "CA", "CB", "CC", "CD", "CE", "CF", "D0", "D1", "D2", "D3",
            "D4", "D5", "D6", "D7", "D8", "D9", "DA", "DB", "DC", "DD", "DE", "DF", "E0", "E1", "E2", "E3", "E4", "E5",
            "E6", "E7", "E8", "E9", "EA", "EB", "EC", "ED", "EE", "EF", "F0", "F1", "F2", "F3", "F4", "F5", "F6", "F7",
            "F8", "F9", "FA", "FB", "FC", "FD", "FE", "FF" };

    private static byte[]   val = { 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x00, 0x01,
            0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x0A, 0x0B, 0x0C,
            0x0D, 0x0E, 0x0F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F };

    /**
     * 编码,模拟js的escape函数.<br> 
     * escape不编码字符有69个：*+-./@_0-9a-zA-Z 
     *  
     * @param s 
     *            字符串 
     * @return 转义后的字符串或者null 
     */
    public static String escape(String s) {
        if (s == null) {
            return null;
        }
        StringBuffer sbuf = new StringBuffer();
        int len = s.length();
        for (int i = 0; i < len; i++) {
            int ch = s.charAt(i);
            if ('A' <= ch && ch <= 'Z') {
                sbuf.append((char) ch);
            } else if ('a' <= ch && ch <= 'z') {
                sbuf.append((char) ch);
            } else if ('0' <= ch && ch <= '9') {
                sbuf.append((char) ch);
            } else if (ch == '*' || ch == '+' || ch == '-' || ch == '/' || ch == '_' || ch == '.' || ch == '@') {
                sbuf.append((char) ch);
            } else if (ch <= 0x007F) {
                sbuf.append('%');
                sbuf.append(hex[ch]);
            } else {
                sbuf.append('%');
                sbuf.append('u');
                sbuf.append(hex[(ch >>> 8)]);
                sbuf.append(hex[(0x00FF & ch)]);
            }
        }
        return sbuf.toString();
    }

    /**
     * 解码,模拟js的unescape函数. 说明：本方法保证 不论参数s是否经过escape()编码，均能得到正确的“解码”结果 
     * @param s
     * @return
     */
    public static String unescape(String s) {
        if (s == null) {
            return null;
        }
        StringBuffer sbuf = new StringBuffer();
        int i = 0;
        int len = s.length();
        while (i < len) {
            int ch = s.charAt(i);
            if ('A' <= ch && ch <= 'Z') {
                sbuf.append((char) ch);
            } else if ('a' <= ch && ch <= 'z') {
                sbuf.append((char) ch);
            } else if ('0' <= ch && ch <= '9') {
                sbuf.append((char) ch);
            } else if (ch == '*' || ch == '+' || ch == '-' || ch == '/' || ch == '_' || ch == '.' || ch == '@') {
                sbuf.append((char) ch);
            } else if (ch == '%') {
                int cint = 0;
                if ('u' != s.charAt(i + 1)) {
                    cint = (cint << 4) | val[s.charAt(i + 1)];
                    cint = (cint << 4) | val[s.charAt(i + 2)];
                    i += 2;
                } else {
                    cint = (cint << 4) | val[s.charAt(i + 2)];
                    cint = (cint << 4) | val[s.charAt(i + 3)];
                    cint = (cint << 4) | val[s.charAt(i + 4)];
                    cint = (cint << 4) | val[s.charAt(i + 5)];
                    i += 5;
                }
                sbuf.append((char) cint);
            } else {
                sbuf.append((char) ch);
            }
            i++;
        }
        String str= sbuf.toString();
        str= replaceAscii160ToAscii36(str);
        return str;
    }

    /**
     * 将字符串转换成Javascript，将对\r \n < > & 空格进行转换
     * 
     * @param text 
     * @return
     */
    public static String escapeJavascript(String str) {
        if (str == null) {
            return str;
        }
        StringBuffer out = new StringBuffer();
        int sz;
        sz = str.length();
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);

            if (ch < 32) {
                switch (ch) {
                    case '\b':
                        out.append('\\');
                        out.append('b');
                        break;
                    case '\n':
                        out.append('\\');
                        out.append('n');
                        break;
                    case '\t':
                        out.append('\\');
                        out.append('t');
                        break;
                    case '\f':
                        out.append('\\');
                        out.append('f');
                        break;
                    case '\r':
                        out.append('\\');
                        out.append('r');
                        break;
                    default:
                        if (ch > 0xf) {
                            out.append("\\u00" + hex(ch));
                        } else {
                            out.append("\\u000" + hex(ch));
                        }
                        break;
                }
            } else {
                switch (ch) {
                    case '\'':
                        out.append('\\');
                        out.append('\'');
                        break;
                    case '"':
                        out.append("\\\"");
                        break;
                    case '\\':
                        out.append('\\');
                        out.append('\\');
                        break;
                    case '/':
                        out.append("\\/");
                        break;
                    default:
                        out.append(ch);
                        break;
                }
            }
        }
        return out.toString();
    }

    private static String hex(char ch) {
        return Integer.toHexString(ch).toUpperCase();
    }

    /**
     * 检测是否是空字符串, 不允许空格
     * 
     * <pre>
     * Strings.isBlank(null)      = true
     * Strings.isBlank("")        = true
     * Strings.isBlank(" ")       = true
     * Strings.isBlank("bob")     = false
     * Strings.isBlank("  bob  ") = false
     * </pre>
     * 
     * @param str
     * @return
     */
    public static boolean isBlank(String str) {
        return StringUtils.isBlank(str);
    }

    /**
     * 检测是否不是空字符串, 不允许空格
     * 
     * <pre>
     * Strings.isNotBlank(null)      = false
     * Strings.isNotBlank("")        = false
     * Strings.isNotBlank(" ")       = false
     * Strings.isNotBlank("bob")     = true
     * Strings.isNotBlank("  bob  ") = true
     * </pre>
     * 
     * @param str
     * @return
     */
    public static boolean isNotBlank(String str) {
        return StringUtils.isNotBlank(str);
    }

    /**
     * 压缩
     * @param str
     * @return
     * @throws IOException
     */
    public static String compress(String str){
        return str;
//        if (Strings.isBlank(str) || str.length() == 0) {
//            return str;
//        }
//        try{
//            long startTime= System.currentTimeMillis();
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            GZIPOutputStream gzip = new GZIPOutputStream(out);
//            gzip.write(str.getBytes());
//            gzip.close();
//            log.info("压缩耗时：="+(System.currentTimeMillis()-startTime)+" ms");
//            return out.toString("ISO-8859-1");
//        }catch(IOException e){
//            log.warn(e);
//            return str;
//        }        
    }

    /**
     * 解压缩
     * @param str
     * @return
     * @throws IOException
     */
    public static String uncompress(String str){
        return str;
//        if (Strings.isBlank(str) || str.length() == 0) {
//            return str;
//        }
//        try{
//            long startTime= System.currentTimeMillis();
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));
//            GZIPInputStream gunzip = new GZIPInputStream(in);
//            byte[] buffer = new byte[256];
//            int n;
//            while ((n = gunzip.read(buffer)) >= 0) {
//                out.write(buffer, 0, n);
//            }
//            log.info("解压缩耗时：="+(System.currentTimeMillis()-startTime)+" ms");
//            //toString()使用默认编码，也可以显式的指定如toString("GBK")
//            return out.toString();
//        }catch(IOException e){
//            log.warn(e);
//            return str;
//        } 
    }
    
    private static String format = "yyyy-MM-dd hh:mm a";
	private static SimpleDateFormat myFormatter = new SimpleDateFormat(format
				);
	public static String getDateFormat(){
		return format;
	}
	public static void setDateFormat(String newFormat){
		myFormatter = new SimpleDateFormat(newFormat);
		format = newFormat;
	}
	public static String toString(Date date){
		return myFormatter.format(date);
	}
	
	public static Date fromString(String date) throws ParseException{
		return myFormatter.parse(date);
	}
	
	/**
     * 将一个子流程绑定数据的属性全部放入Map
     * @param subSetting
     * @return
     */
    public static Map<String, Object> subSettingToMap(SubProcessSetting subSetting){
        Map<String, Object> result = new HashMap<String, Object>();
        if(subSetting!=null){
            result.put("id", String.valueOf(subSetting.getId()));
            result.put("conditionBase", subSetting.getConditionBase());
            result.put("conditionTitle", subSetting.getConditionTitle());
            result.put("flowRelateType", subSetting.getFlowRelateType());
            result.put("isCanViewByMainFlow", subSetting.getIsCanViewByMainFlow());
            result.put("isCanViewMainFlow", subSetting.getIsCanViewMainFlow());
            result.put("isForce", subSetting.getIsForce());
            result.put("newflowSender", subSetting.getNewflowSender());
            result.put("newflowTempleteId", subSetting.getNewflowTempleteId());
            result.put("nodeId", subSetting.getNodeId());
            result.put("subject", subSetting.getSubject());
            result.put("templeteId", subSetting.getTempleteId());
            result.put("triggerCondition", subSetting.getTriggerCondition());
        }
        return result;
    }
    
    /**
     * 将List中的子流程绑定数据转换成List中的Map
     * @param subSettingList
     * @return
     */
    public static List<Map<String, Object>> subSettingListToMapList(List<SubProcessSetting> subSettingList){
        List<Map<String, Object>> resultlList = new ArrayList<Map<String,Object>>();
        if(subSettingList!=null && subSettingList.size()>0){
            for(SubProcessSetting subSetting : subSettingList){
                Map<String, Object> temp = subSettingToMap(subSetting);
                if(temp!=null && temp.size()>0){
                    resultlList.add(temp);
                }
            }
        }
        return resultlList;
    }
    
    public static final ProcessOrgManager processOrgManager = (ProcessOrgManager)AppContext.getBean("processOrgManager");
    
    
    /**
     * 组织模型的V3xOrgMemberList转换成工作流vo里面的UserList，没有depName\PostName\memberTitle
     * @param members
     * @return
     */
    public static List<User> v3xOrgMemberToWorkflowUser(List<V3xOrgMember> members, boolean isUseAdditonUserIds){
        if(members!=null && members.size()>0){
            List<User> users = new ArrayList<User>();
            for(V3xOrgMember m : members){
                if(m != null && (isUseAdditonUserIds || (!isUseAdditonUserIds && m.isValid()))){
                    User temp = new User();
                    temp.setId(m.getId().toString());
                    temp.setAccountId(m.getOrgAccountId().toString());
                    temp.setName(m.getName());
                    
                    users.add(temp);
                }
            }
            
            return users;
        }
        
        return null;
    }
    
    
    /**
     * 组织模型的V3xOrgMemberList转换成工作流vo里面的UserList
     * @param members
     * @return
     */
    public static List<User> v3xOrgMemberToWorkflowUser(List<V3xOrgMember> members,boolean isUseAdditonUserIds,boolean isInvokeOrgFunctions){
        if(members!=null && members.size()>0){
            List<User> users = new ArrayList<User>();
            for(V3xOrgMember m : members){
                if(m!=null){
                    User temp = v3xOrgMemberToWorkflowUser(m,isUseAdditonUserIds,isInvokeOrgFunctions);
                    if(temp!=null){
                        users.add(temp);
                    }
                }
            }
            return users;
        }
        return null;
    }
    
    /**
     * 组织模型的V3xOrgMember转换成工作流vo里面的User
     * @param member
     * @return
     */
    public static User v3xOrgMemberToWorkflowUser(V3xOrgMember member,boolean isNotValidate,boolean isInvokeOrgFunctions){
        V3xOrgDepartment dep = null;
        V3xOrgPost post = null;
        try{
            dep = (V3xOrgDepartment)processOrgManager.getEntity(V3xOrgEntity.ORGENT_TYPE_DEPARTMENT, member.getOrgDepartmentId());
        }catch(Exception e){
            logger.error("", e);
        }
        try{
            post = (V3xOrgPost)processOrgManager.getEntity(V3xOrgEntity.ORGENT_TYPE_POST, member.getOrgPostId());
        }catch(Exception e){
            logger.error("", e);
        }
        if(isNotValidate){
            if(member!=null){
                User user = new User();
                user.setAccountId(String.valueOf(member.getOrgAccountId()));
                user.setId(String.valueOf(member.getId()));
                if(null!=member.getSortId()){
                    user.setSortId(Integer.parseInt(member.getSortId().toString()));
                }else{
                    user.setSortId(0);
                }
                user.setName(Functions.showMemberName(member));
                if(dep!=null && dep.getName()!=null){
                    user.setDepName(dep.getName());
                }
                if(post!=null && post.getName()!=null){
                    user.setPostName(post.getName());
                }
                if(isInvokeOrgFunctions){
                    try{
                        String title= Functions.showMemberAltWithFullDeptPath(member.getId());
                        user.setTitle(title);
                    }catch(Throwable e){
                        logger.warn(e.getMessage(),e);
                    }
                }
                return user;
            }
        }else{
            if(member!=null && member.isValid()){
                User user = new User();
                user.setAccountId(String.valueOf(member.getOrgAccountId()));
                user.setId(String.valueOf(member.getId()));
                if(null!=member.getSortId()){
                    user.setSortId(Integer.parseInt(member.getSortId().toString()));
                }else{
                    user.setSortId(0);
                }
                user.setName(Functions.showMemberName(member));
                if(dep!=null && dep.getName()!=null){
                    user.setDepName(dep.getName());
                }
                if(post!=null && post.getName()!=null){
                    user.setPostName(post.getName());
                }
                if(isInvokeOrgFunctions){
                    try{
                        String title= Functions.showMemberAltWithFullDeptPath(member.getId());
                        user.setTitle(title);
                    }catch(Throwable e){
                        logger.warn(e.getMessage(),e);
                    }
                }
                return user;
            }
        }
        return null;
    }
    
    /**
     * 获得人员匹配token(JSON格式的)
     * @param popNodeCondition
     * @return
     * @throws JSONException
     */
    public static String getWorkflowMatchRequestToken(String popNodeCondition){
    	try{
	    	if( Strings.isNotBlank(popNodeCondition) && popNodeCondition.indexOf("matchRequestToken")>=0){
	        	JSONObject popNodeConditionObj= new JSONObject(popNodeCondition);
	        	if(!popNodeConditionObj.isNull("matchRequestToken")){
	        		String matchRequestToken= popNodeConditionObj.getString("matchRequestToken");
	            	return matchRequestToken;
	        	}
	        }
    	}catch(Throwable e){
    		logger.warn("", e);
    	}
    	return "";
	}
    
    /**
     * 获得条件分支信息(JSON格式的)
     * @param popNodeCondition
     * @param map
     * @return
     * @throws JSONException
     */
    public static Map<String, String> getPopNodeConditionValues(String popNodeCondition,Map<String, Map<String,Object>> map) throws JSONException {
    	Map<String,String> nodeIdToIsDeleteMap = new HashMap<String,String>();
    	try{
	    	if(Strings.isNotBlank(popNodeCondition)){
	        	JSONObject popNodeConditionObj= new JSONObject(popNodeCondition);
	        	if(!popNodeConditionObj.isNull("condition")){
		        	JSONArray popNodeConditionAr= popNodeConditionObj.getJSONArray("condition");
		        	for (int i = 0; i < popNodeConditionAr.length(); i++) {
		        		JSONObject jsonObj = popNodeConditionAr.getJSONObject(i);
		        		String nodeId= jsonObj.getString("nodeId");
		        		String isDelete= jsonObj.getString("isDelete");
		        		nodeIdToIsDeleteMap.put(nodeId, isDelete);
		        		if("true".equals(isDelete)){
		        			map.remove(nodeId);
		        		}
					}
	        	}
	        }
    	}catch(Throwable e){
    		logger.warn("",e);
    	}
    	return nodeIdToIsDeleteMap;
	}
	/**
     * 获得节点上选择的执行人员字符串（json格式的）
     * @param popNodeSelected
     * @return
     * <pre>
     * {
     * NodeId : {
     *          isOrderExecute : "true"/"false" //   是否开启多人按序执行
     *          pepole : String[]  //节点的执行人
     *          }
     * }
     * </pre>
     * @throws JSONException
     */
    public static Map<String, Map<String,Object>> getPopNodeSelectedValues(String popNodeSelected){
    	Map<String,  Map<String,Object>> map = new HashMap<String,  Map<String,Object>>();
    	try{
	    	if(Strings.isNotBlank(popNodeSelected)){
	        	JSONObject popNodeSelectedObj= new JSONObject(popNodeSelected);
	        	JSONArray jsonAr= popNodeSelectedObj.getJSONArray("nodeAdditon");
	        	for (int i = 0; i < jsonAr.length(); i++) {
	        	    Map<String,Object> nodeParam = new HashMap<String,Object>();
	    			JSONObject jsonObj= jsonAr.getJSONObject(i);
	    			String nodeId= jsonObj.getString("nodeId");
	    			JSONArray peoplesJson= jsonObj.getJSONArray("pepole");
	    			if(null!=peoplesJson){
	    			    String[] pepoles= new String[peoplesJson.length()];
	    			    for (int j = 0; j < peoplesJson.length(); j++) {
	    			        pepoles[j]= peoplesJson.getString(j);
	                    }
	    			    nodeParam.put("pepole", pepoles);
	    			}
	    			String isOrderExecute= "";
	    			if(jsonObj.has(ProcessEngineImpl.IS_ORDER_EXECUTE)){
	    			    isOrderExecute = jsonObj.getString(ProcessEngineImpl.IS_ORDER_EXECUTE);
	    			    nodeParam.put(ProcessEngineImpl.IS_ORDER_EXECUTE, isOrderExecute);
	    			}
	    			
	    			//并发节点串发执行
	    			if(jsonObj.has(ProcessEngineImpl.MULTI_ADD_MEMBER)){
	    			    
	    			    JSONArray multiAddMember= jsonObj.getJSONArray(ProcessEngineImpl.MULTI_ADD_MEMBER);
	    			    if(null != multiAddMember){
	                        String[] members = new String[multiAddMember.length()];
	                        for (int j = 0; j < multiAddMember.length(); j++) {
	                            members[j]= multiAddMember.getString(j);
	                        }
	                        nodeParam.put(ProcessEngineImpl.MULTI_ADD_MEMBER, members);
	                    }
	    			}
	    			
	    			/*
	    			 //记日志要精确记录， 不能有这个设置
	    			 if(Strings.isBlank(isOrderExecute)){
	    			    isOrderExecute=  String.valueOf(Boolean.FALSE);
	    			}*/
	    			
	    			map.put(nodeId, nodeParam);
	    		}
	    	}
    	}catch(Throwable e){
    		logger.warn("popNodeSelected："+popNodeSelected,e);
    	}
		return map;
	}
    
    /**
     * 
     * @param informNodes
     * @param key
     * @return
     * @throws JSONException
     */
    public static JSONArray getPopInformNodeSelectedValues(String informNodes,String key) throws JSONException {
    	Map<String, String[]> map = new HashMap<String, String[]>();
    	if(informNodes!=null && !"".equals(informNodes.trim())){
        	JSONObject popInformNodeSelectedObj= new JSONObject(informNodes);
        	JSONArray jsonAr= popInformNodeSelectedObj.getJSONArray(key);
        	return jsonAr;
    	}
    	return null;
	}
    
    public static final String replaceUUID= "6525302505919040842";

    /**
     * 获得longUUID
     * @return
     */
    public static long getTableKey() {
        return UUIDLong.longUUID();
    }

    /**
     * 读取blob字段
     * @param byteArray
     * @return
     */
    public static Object getObjectFromBlob(byte[] byteArray) {
        Object obj = null;
        if (null != byteArray) {
            InputStream fin = null;
            ObjectInputStream objectinput = null;
            
            try {
                fin = new ByteArrayInputStream(byteArray);
                objectinput = new ObjectInputStream(fin);
                obj = objectinput.readObject();
            } catch (Throwable e) {
                String eMsg= getExcpetionStackTraceMessage(e);
                if(null!=eMsg && eMsg.indexOf("WorkFlowDesignerController.showDiagram")!=-1){
                    logger.warn("may be a exception,ignore."+e.getMessage());
                }else{
                    logger.error(e.getMessage(), e);
                }
            } finally {
                if(fin != null){
                    try {
                        fin.close();
                    } catch (IOException e) {
                        logger.error("", e);
                    }
                }
                
                if(objectinput != null){
                    try {
                        objectinput.close();
                    } catch (IOException e) {
                        logger.error("", e);
                    }
                }
                
            }
        }
        return obj;
    }
    
    /**
     * 将Object对象解析为byte[]
     * @param object
     * @return
     */
    public static byte[] createByteArrayFromObject(Object object){
        byte[] byteArray= null;
        if (null != object) {
            try {
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                os.flush();
                os.writeObject(object);
                os.flush();
                byteArray = byteStream.toByteArray();
                os.close();
                return byteArray;
            }catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }
        return byteArray;
    }
    
    /**
     * 
     * @param e
     * @return
     */
    public static String getExcpetionStackTraceMessage(Throwable e) {
        if(null!=e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw, true);
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
            
            String ret = sw.toString();
            
            try {
                sw.close();
            } catch (IOException e1) {
                logger.error("", e1);
            }
            pw.close();
            
            return ret;
        }else{
            return null;
        }
    }
    
    /**
     * 发送或者处理的时候获取表单数据（key是fieldName）
     * @param context
     * @throws BPMException
     */
    public static void addFormDataDisplayName(WorkflowBpmContext context, String rightId) throws BPMException{
        if(context==null){
            return;
        }
        String masterId = context.getMastrid();
        String formAppId = context.getFormData();
        if(masterId==null || formAppId==null){
            return;
        }
        masterId = masterId.trim();
        formAppId = formAppId.trim();
        if(isLong(masterId) && isLong(formAppId)){
            
            WorkflowFormDataMapManager formDataMapManager = WorkflowFormDataMapInvokeManager.getAppManager("form");
            
            Map<String,Object> rowDataDisplayName = formDataMapManager.getFormValueMap(Long.parseLong(formAppId), Long.parseLong(masterId), rightId);
            context.setBusinessData(EventDataContext.CTP_FORM_DATA, rowDataDisplayName);
            Map<String,WorkflowFormFieldBO> formFieldDefMap= formDataMapManager.getFormFieldMap(formAppId);
            context.setBusinessData(EventDataContext.CTP_FORM_DATA_DEF, formFieldDefMap);
        }
    }

    /**
     * 生成公文结构化数据，以符合工作流分支条件匹配的数据要求
     * @param context
     */
    public static void addEdocDataDisplayName(WorkflowBpmContext context) {
        String formData= context.getFormData();
        logger.info("当前公文单数据formData:="+formData);
        Map<String,Object> edocFormDataPage = null;
        if(Strings.isNotBlank(formData) && !Strings.equals(formData, "-1")){
            
            edocFormDataPage = new HashMap<String, Object>();
            
        	Map<String,Map<String,String>> edocFormData1 = (Map<String,Map<String,String>>)JSONUtil.parseJSONString(formData);
            if(null!=edocFormData1){
                Set<String> fieldSet= edocFormData1.keySet();
                for (String fieldName : fieldSet) {
                    Map<String,String> aFiledValueAndTypeMap= edocFormData1.get(fieldName);
                    String value= aFiledValueAndTypeMap.get("value");
                    String type= aFiledValueAndTypeMap.get("type");
                    //后续版本必须要公文模块明确没个字段的类型，目前可以先这样来处理，公文目前只有整数类型做分支
                    /*if(null!=value && !"".equals(value.trim())){
                        try{
                            edocFormData.put(fieldName, Integer.parseInt(value));
                        }catch(Throwable e){
                            log.warn("edoc filed type is error,"+e);
                            edocFormData.put(fieldName, value);
                        }
                    }else{
                        edocFormData.put(fieldName, "");
                    }*/
                    if("varchar".equals(type)){
                    	edocFormDataPage.put(fieldName, value);
                    }else if("int".equals(type)){
                        value = value.trim();
                        if(null!=value && !"".equals(value) && !"null".equals(value) && !"undefined".equals(value)){
                            try{
                            	edocFormDataPage.put(fieldName, Integer.parseInt(value));
                            }catch(Throwable e){
                                logger.warn("edoc filed int type is error:"+value);
                                edocFormDataPage.put(fieldName, value);
                            }
                        }else{
                        	edocFormDataPage.put(fieldName, null);
                        }
                    }else if("decimal".equals(type)){
                        value = value.trim();
                        if(null!=value && !"".equals(value) && !"null".equals(value) && !"undefined".equals(value)){
                            try{
                            	edocFormDataPage.put(fieldName, Double.parseDouble(value));
                            }catch(Throwable e){
                            	logger.warn("edoc filed decimal type  is error:"+value);
                            	edocFormDataPage.put(fieldName, value);
                            }
                        }else{
                        	edocFormDataPage.put(fieldName, null);
                        }
                    }else if("date".equals(type)){
                    	value = value.trim();
                        if(null!=value && !"".equals(value) && !"null".equals(value) && !"undefined".equals(value)){
                            try{
                            	edocFormDataPage.put(fieldName, Datetimes.parse(value));
                            }catch(Throwable e){
                            	logger.warn("edoc filed date type is error:"+value);
                            	edocFormDataPage.put(fieldName, null);
                            }
                        }else{
                        	edocFormDataPage.put(fieldName, null);
                        }
                    }else{
                    	edocFormDataPage.put(fieldName, value);
                    }
                }
            }
        }
        Map<String,Object> edocFormDataDb = null;
        if(edocFormDataPage != null){
            context.setBusinessData(EventDataContext.CTP_FORM_DATA, edocFormDataPage);
        }else{
            
            if (Strings.isNotBlank(context.getBussinessId())) {
                edocFormDataDb= WorkFlowAppExtendInvokeManager.getAppManager(context.getAppName()).getFormData(context.getBussinessId());
            }
            if(edocFormDataDb == null){
                edocFormDataDb = new HashMap<String, Object>();
            }
            context.setBusinessData(EventDataContext.CTP_FORM_DATA, edocFormDataDb);
        }
        
        logger.info("当前公文单数据edocFormDataDb:="+edocFormDataDb);
        logger.info("当前公文单数据edocFormDataPage:="+edocFormDataPage);
    }
    
    /**
     * 
     * @param context
     */
    public static void addAppDataDisplayName(WorkflowBpmContext context) {
        WorkFlowAppExtendManager matchOrgMemberManager = WorkFlowAppExtendInvokeManager.getAppManager(context.getAppName());
        if(null!=matchOrgMemberManager){//给应用扩展留个接口
            Map<String,Object> appFormData = matchOrgMemberManager.getFormData(context.getFormData());
            if(null!=appFormData){
                context.setBusinessData(EventDataContext.CTP_FORM_DATA, appFormData);
                logger.info("当前应用表单数据formData:="+appFormData);
            }
        }
    }
    
  //获得标识知会的两个常量
    private final static String informActivityPolicy = BPMSeeyonPolicy.SEEYON_POLICY_INFORM.getId();
	private final static String edocInformActivityPolicy = BPMSeeyonPolicy.EDOC_POLICY_ZHIHUI.getId();
	
	public static final String xmlSpecialChar_angleBrackets= "><";//><符号
	public static final String regExp_angleBrackets= ">\\s+?<";
	public static final String xmlSpecialChar_Tab= "&#x09;";//Tab
	public static final String regExp_Tab= "\t";
    public static final String xmlSpecialChar_Enter= "&#x0D;";//回车
    public static final String regExp_Enter= "\r";
    public static final String xmlSpecialChar_Newline= "&#x0A;";//换行
    public static final String regExp_Newline= "\n";
    public static Map<String,String> oldTag2newTagMap= null;
    public static Map<String,String> oldAttribute2newAttributeMap= null;
    static{
        if(null==oldTag2newTagMap){
            oldTag2newTagMap= new HashMap<String, String>();
            oldTag2newTagMap.put("processes", "ps");
            oldTag2newTagMap.put("process", "p");
            oldTag2newTagMap.put("node", "n");
            oldTag2newTagMap.put("actor", "a");
            oldTag2newTagMap.put("seeyonPolicy", "s");
            oldTag2newTagMap.put("link", "l");
            oldTag2newTagMap.put("clink", "cl");
        }
        if(null==oldAttribute2newAttributeMap){
            oldAttribute2newAttributeMap= new HashMap<String, String>();
            oldAttribute2newAttributeMap.put("id", "i");
            oldAttribute2newAttributeMap.put("name", "n");
            oldAttribute2newAttributeMap.put("type", "t");
            oldAttribute2newAttributeMap.put("desc","d");
            oldAttribute2newAttributeMap.put("y","y");
            oldAttribute2newAttributeMap.put("x","x");
            
            oldAttribute2newAttributeMap.put("isShowShortName","a");
            oldAttribute2newAttributeMap.put("uns","u");
            
            oldAttribute2newAttributeMap.put("task_num_value","a");
            oldAttribute2newAttributeMap.put("personStatus","b");
            oldAttribute2newAttributeMap.put("task_num","c");
            oldAttribute2newAttributeMap.put("finishNum2","l");
            oldAttribute2newAttributeMap.put("finishNum","e");
            oldAttribute2newAttributeMap.put("pt","f");
            oldAttribute2newAttributeMap.put("cf","g");
            oldAttribute2newAttributeMap.put("cn","h");
            oldAttribute2newAttributeMap.put("parallelismNodeId","o");
            oldAttribute2newAttributeMap.put("start","p");
            oldAttribute2newAttributeMap.put("fromType","q");
            oldAttribute2newAttributeMap.put("isValid","r");
            
            oldAttribute2newAttributeMap.put("accountShortName","a");
            oldAttribute2newAttributeMap.put("accountId","b");
            oldAttribute2newAttributeMap.put("condition","c");
            oldAttribute2newAttributeMap.put("partyIdName","d");
            oldAttribute2newAttributeMap.put("partyTypeName","e");
            oldAttribute2newAttributeMap.put("partyId","f");
            oldAttribute2newAttributeMap.put("partyType","g");
            oldAttribute2newAttributeMap.put("partyExcludeChildDepartment","h");
            oldAttribute2newAttributeMap.put("includeChild","i");
            oldAttribute2newAttributeMap.put("const","j");
            oldAttribute2newAttributeMap.put("role","k");
            oldAttribute2newAttributeMap.put("addition","l");
            oldAttribute2newAttributeMap.put("raddition","m");
            
            oldAttribute2newAttributeMap.put("formfiled","a");
            oldAttribute2newAttributeMap.put("NF","b");
            oldAttribute2newAttributeMap.put("matchScope","c");
            
            //这三个属性废弃了
            oldAttribute2newAttributeMap.put("operationName","r");
            oldAttribute2newAttributeMap.put("form","e");
            oldAttribute2newAttributeMap.put("operationm","z");
            
            oldAttribute2newAttributeMap.put("formApp","f");
            oldAttribute2newAttributeMap.put("isOvertopTime","g");
            oldAttribute2newAttributeMap.put("hstv","h");
            oldAttribute2newAttributeMap.put("processMode","j");
            oldAttribute2newAttributeMap.put("remindTime","k");
            oldAttribute2newAttributeMap.put("cycleRemindTime","cy");
            oldAttribute2newAttributeMap.put("dealTerm","l");
            oldAttribute2newAttributeMap.put("isDelete","m");
            oldAttribute2newAttributeMap.put("isPass","s");
            oldAttribute2newAttributeMap.put("dealTermUserName","o");
            oldAttribute2newAttributeMap.put("dealTermUserId","p");
            oldAttribute2newAttributeMap.put("dealTermType","q");
            oldAttribute2newAttributeMap.put("hst","u");
            oldAttribute2newAttributeMap.put("rup","v");
            oldAttribute2newAttributeMap.put("pup","w");
            oldAttribute2newAttributeMap.put("na","na");
            oldAttribute2newAttributeMap.put("FR","FR");
            oldAttribute2newAttributeMap.put("added","ad");
            oldAttribute2newAttributeMap.put("addedFromId","fd");
            oldAttribute2newAttributeMap.put("isColAssign","ca");
            oldAttribute2newAttributeMap.put("queryIds","qid");
            oldAttribute2newAttributeMap.put("statisticsIds","sid");
            oldAttribute2newAttributeMap.put("systemAdd","sa");
            
            oldAttribute2newAttributeMap.put("conditionBase","a");
            oldAttribute2newAttributeMap.put("isForce","b");
            oldAttribute2newAttributeMap.put("conditionTitle","c");
            oldAttribute2newAttributeMap.put("formCondition","m");
            oldAttribute2newAttributeMap.put("conditionId","e");
            oldAttribute2newAttributeMap.put("conditionType","h");
            oldAttribute2newAttributeMap.put("to","j");
            oldAttribute2newAttributeMap.put("from","k");
            oldAttribute2newAttributeMap.put("submitStyle","s");
            oldAttribute2newAttributeMap.put("sortIndex","o");
            oldAttribute2newAttributeMap.put("tolerantModel","tm");
        }
    }

	/**
	 * 判断当前节点是否处于state状态
	 * @param theCase               case实例
	 * @param currentNodeId         当前节点id
	 * @param whichState            状态
	 * @return
	 */
	public static boolean isThisState(BPMCase theCase,String currentNodeId,int... states) {
		int state = getNodeState(theCase,currentNodeId);
		for(int s:states) {
			if(s == state)
				return true;
		}
		return false;
	}
	
	/**
     * 判断当前知会节点是否处于state状态
     * @param theCase               case实例
     * @param currentNodeId         当前节点id
     * @param whichState            状态
     * @return
     */
    public static boolean isThisStateInform(BPMCase theCase,BPMAbstractNode from,int... states) {
        String currentNodeId= from.getId();
        int state = getNodeState(theCase,currentNodeId);
        if( state==0 || from.getNodeType().equals(BPMAbstractNode.NodeType.join)){//继续递归找父亲节点
            Map<String,BPMAbstractNode> findedMaps= new HashMap<String, BPMAbstractNode>();
            List<Integer> preStates= new ArrayList<Integer>();
            List<Integer> preInformStates= new ArrayList<Integer>();
            findPreviousNodeState(theCase,from,findedMaps,preStates,preInformStates);
            if(preInformStates.size()>0 && preStates.size()==0){
                state= CaseDetailLog.STATE_READY;
            }else if( preStates.size()>0){
                state= 0;
            }
        }
        for(int s:states) {
            if(s == state)
                return true;
        }
        return false;
    }
    
    private static void findPreviousNodeState(BPMCase theCase,BPMAbstractNode from,Map<String,BPMAbstractNode> findedMaps,List<Integer> preStates,List<Integer> preInformStates) {
        if(null!=findedMaps.get(from.getId())){
            return;
        }else{
            findedMaps.put(from.getId(), from);
        }
        List<BPMTransition> ups = from.getUpTransitions();
        for (BPMTransition bpmTransition : ups) {
            BPMAbstractNode from1= bpmTransition.getFrom();
            String policy1 = from1.getSeeyonPolicy().getId();
            String isDelete= getNodeConditionFromCase(theCase, from1, "isDelete");
            if(!"true".equalsIgnoreCase(isDelete)){
                if(from1.getNodeType().equals(BPMAbstractNode.NodeType.start)){//这种情况是有问题的，兼容处理下，将isdelete设置为true。
                    preStates.add(0);
                    break;
                }else if(from1.getNodeType().equals(BPMAbstractNode.NodeType.join)){
                    findPreviousNodeState(theCase, from1,findedMaps,preStates,preInformStates);
                }else if(from1.getNodeType().equals(BPMAbstractNode.NodeType.split)){
                    findPreviousNodeState(theCase, from1,findedMaps,preStates,preInformStates);
                }else if(from1.getNodeType().equals(BPMAbstractNode.NodeType.humen)){
                	if(BPMSeeyonPolicy.SEEYON_POLICY_INFORM.getId().equalsIgnoreCase(policy1)
                            || BPMSeeyonPolicy.EDOC_POLICY_ZHIHUI.getId().equalsIgnoreCase(policy1)){//知会节点
                        int state1 = getNodeState(theCase,from1.getId());
                        if(state1==0){
                            findPreviousNodeState(theCase, from1,findedMaps,preStates,preInformStates);
                        }else if(state1==CaseDetailLog.STATE_READY || state1== CaseDetailLog.STATE_ZCDB || state1== CaseDetailLog.STATE_INFORM){
                            preInformStates.add(CaseDetailLog.STATE_READY);
                        }
                    }else{//非知会节点
                        int state1 = getNodeState(theCase,from1.getId());
                        if(state1==0 || state1==CaseDetailLog.STATE_READY || state1== CaseDetailLog.STATE_ZCDB || state1== CaseDetailLog.STATE_INFORM){
                            preStates.add(0);
                            break;
                        }
                    }
                }
            }
        }
    }
	
	/**
	 * 取当前节点状态
	 * @param theCase
	 * @param currentNodeId
	 * @return
	 */
	public static int getNodeState(BPMCase theCase,String currentNodeId) {
		int state = 0;
		List<BPMCaseLog> caseLogs = theCase.getCaseLogList();
		for (BPMCaseLog step : caseLogs) {
			List<CaseDetailLog> frames = step.getDetailLogList();
			if(frames == null){
				continue;
			}
			
			for (CaseDetailLog frame : frames) {
				String nodeId = frame.nodeId;
				if (nodeId.equals(currentNodeId)){
				    
					if(state != CaseDetailLog.STATE_STOP 
					        || frame.getState() == CaseDetailLog.STATE_READY){
                        state = frame.getState();
                    }
				}
			}
		}
		return state;
	}
	
	
	/**
	 * 获取case中所有节点的状态
	 * 
	 * @param theCase
	 * @return
	 *
	 * @Since A8-V5 7.0
	 * @Author      : xuqw
	 * @Date        : 2018年3月14日下午2:12:19
	 *
	 */
	public static Map<String, Integer> getAllNodeState(BPMCase theCase){
	    
	    Map<String, Integer> ret = new HashMap<String, Integer>();
	    
	    List<BPMCaseLog> caseLogs = theCase.getCaseLogList();
        for (BPMCaseLog step : caseLogs) {
            
            List<CaseDetailLog> frames = step.getDetailLogList();
            if(frames == null){
                continue;
            }
            
            for (CaseDetailLog frame : frames) {
                String nodeId = frame.nodeId;
                ret.put(nodeId, frame.getState());
            }
        }
	    
	    return ret;
	}
	
	
	/**
	 * 把XML中的一个节点下所有的属性读取成Map。注意，从Map中读取属性值时，如果改属性不存在，返回Empty，而不是<code>null</code>（因为dom4j也是注意）
	 * @param node
	 * @return
	 */
	public static Map<String, String> getNodeAttributes(Element node){
		Map<String, String> r = new HashMap<String, String>(){
			private static final long serialVersionUID = -3476377988348094388L;
			public String get(Object key) {
				String v = super.get(key);
				return v == null ? "" : v;
			}
		};
		
		List atts = node.attributes();
		if(atts != null) {
			for (int i = 0; i < atts.size(); i++) {
				Attribute a = (Attribute)atts.get(i);
				r.put(a.getName(), a.getValue());
			}
		}
		
		return r;
	}

	/**
	 * 获取属性值
	 * @param attrs
	 * @param oldAttrName
	 * @return
	 */
	public static String getAttr(Map<String, String> attrs, String oldAttrName){
	    if(attrs == null){
	        return null;
	    }
	    String newAttrName= oldAttribute2newAttributeMap.get(oldAttrName);
	    String value = attrs.get(oldAttrName);
	    if(Strings.isBlank(value) && Strings.isNotBlank(newAttrName)){
	        String newvalue = attrs.get(newAttrName);
	        if(Strings.isNotBlank(newvalue)){
	            value= newvalue;
	        }
	    }else{
	        value = value.trim();
	    }
	    
	    return value;
	}
	
	/**
	 * 
	 * 
	 * @param attrs
	 * @param oldAttrName
	 * @param def
	 * @return
	 *
	 * @Since A8-V5 6.1
	 * @Author      : xuqw
	 * @Date        : 2016年12月23日下午1:33:41
	 *
	 */
	public static String getAttr(Map<String, String> attrs, String oldAttrName, String def){
        String v = getAttr(attrs, oldAttrName);
        if(Strings.isBlank(v)){
            v = def;
        }
        return v;
    }
	
	
	/**
     * isAllHumenNodeValid()
     * 递归计算出本次可回退的状态标志。
     * 注意：第一次调用本方法时currBackNode节点只能为humen(非知会节点)，后续递归调用只能是humen(知会节点)
     * @param currBackNode 人工活动节点(只能为知会或非知会)
     * @return 返回结构说明： Map<String,Object>: key——"result",value——0: 正常回退 1：需要撤消整个流程 -1:不允许回退;
     *                                           key——"normal_nodes",value——可回退到的正常节点id，但前提是result的value不为-1或1，只能为0，否则该参数无用;
     */
    public static Map<String,Object>  isAllHumenNodeValid(BPMActivity currBackNode,BPMCase theCase) {
    	Map<String,Object> resultMap= new HashMap<String, Object>();
    	Map<String,String> normalNodesMap= new HashMap<String, String>();
    	//获得当前回退节点的所有指向它的up线(肯定只有一条up线)
		BPMTransition currBackNodeUpLinks = (BPMTransition)currBackNode.getUpTransitions().get(0);
		//通过up线获得指向当前回退节点的from节点
        BPMAbstractNode fromNodeOfcurrBackNode = currBackNodeUpLinks.getFrom();
        //humen->humen(回退节点)
        if(fromNodeOfcurrBackNode.getNodeType().equals(BPMAbstractNode.NodeType.humen)){
        	BPMHumenActivity fromHumenNodeOfcurrBackNode = (BPMHumenActivity)fromNodeOfcurrBackNode;
        	if(!isNodeValid(theCase,fromHumenNodeOfcurrBackNode)){
				resultMap.put("result", "-1");
				resultMap.put("invalidateNodeName", fromHumenNodeOfcurrBackNode.getName());
				return resultMap;
			}
        	String currFromHumenNodePolicy = fromHumenNodeOfcurrBackNode.getSeeyonPolicy().getId();
        	//计算出当前回退节点的from节点是否为知会节点
        	boolean currFromHumenNodeIsInformNode = 
        		currFromHumenNodePolicy.equals(informActivityPolicy) || currFromHumenNodePolicy.equals(edocInformActivityPolicy);
        	boolean isAutoSkip= isAutoSkip(theCase,fromHumenNodeOfcurrBackNode);
        	if(currFromHumenNodeIsInformNode || isAutoSkip){//humen(知会)->humen(回退节点)
        		//以该知会节点为起始节点，继续往回查找
        		Map<String,Object> tempResultMap= isAllHumenNodeValid(fromHumenNodeOfcurrBackNode,theCase);
        		if(null!=tempResultMap.get("normal_nodes")){
            		normalNodesMap.putAll((Map<String,String>)tempResultMap.get("normal_nodes"));
            	}
        		resultMap.put("normal_nodes", normalNodesMap);
        		resultMap.put("result", tempResultMap.get("result"));
        		resultMap.put("invalidateNodeName", tempResultMap.get("invalidateNodeName"));
        		return resultMap;
        	}else{//humen(非知会)->humen(回退节点)
        		normalNodesMap.put(fromNodeOfcurrBackNode.getId(), fromNodeOfcurrBackNode.getId());
        		resultMap.put("normal_nodes", normalNodesMap);
        		resultMap.put("result", "0");
        		return resultMap;
        	}
        }
        
        //join->humen(回退节点)
        if(fromNodeOfcurrBackNode.getNodeType().equals(BPMAbstractNode.NodeType.join)){
        	//遇到join节点，此处为join节点第一处理的【唯一入口】和【唯一出口】
        	Map tempMap= isWithdrawActivityOfJoinValid((BPMActivity)fromNodeOfcurrBackNode,(BPMActivity)fromNodeOfcurrBackNode,theCase);
        	//获得第一个join节点的fromNodeOfcurrBackNode的递归运算结果
			Map<String,BPMActivity> subDesNodeSetTmp= (Map<String,BPMActivity>)tempMap.get("subDesNodeSet");
            Map<String,List<BPMActivity>> subRelationInfoSplitMapTmp= (Map<String,List<BPMActivity>>)tempMap.get("subRelationInfoSplitMap");
            String result_str= String.valueOf(tempMap.get("result"));
            //如果为-1，则表示有人工活动节点的人员不可用，则直接返回
    		if("-1".equals(result_str)){
    			resultMap.put("result", "-1");
    			resultMap.put("invalidateNodeName", tempMap.get("invalidateNodeName"));
        		return resultMap;
    		}
    		//必须要对join节点进行评估计算(收缩处理)，看是否需要穿透其对应的split节点
    		if(subDesNodeSetTmp.size()==1){//如果最终为都归集到了split节点，则穿过该split节点
    			Iterator<String> iter= subDesNodeSetTmp.keySet().iterator();
    			String key= iter.next();
    			BPMActivity desSplitNode= subDesNodeSetTmp.get(key);
    			if(desSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.split)){
    				//汇聚节点为split，继续递归调用isWithdrawActivityOfSplitValid进行判断人工节点的人员状态
    				Map<String,Object> tempMap1= isWithdrawActivityOfSplitValid(desSplitNode,theCase);
    				if(null!=tempMap1.get("normal_nodes")){
    					normalNodesMap.putAll((Map<String,String>)tempMap1.get("normal_nodes"));
    				}
    				resultMap.put("stepBackToStart", tempMap1.get("stepBackToStart"));
    				resultMap.put("result", tempMap1.get("result"));
    				resultMap.put("normal_nodes", normalNodesMap);
    				resultMap.put("invalidateNodeName", tempMap1.get("invalidateNodeName"));
    				return resultMap;
    			}else{
    				if(null!=tempMap.get("normal_nodes")){
        				normalNodesMap.putAll((Map)tempMap.get("normal_nodes"));
        			}
        			resultMap.put("normal_nodes",normalNodesMap);
        			resultMap.put("result", tempMap.get("result"));
        			resultMap.put("invalidateNodeName", tempMap.get("invalidateNodeName"));
            		return resultMap;
    			}
    		}else{//如果最终为没有都归集到了split节点，则不穿过该split节点
    			if(null!=tempMap.get("normal_nodes")){
    				normalNodesMap.putAll((Map)tempMap.get("normal_nodes"));
    			}
    			resultMap.put("normal_nodes",normalNodesMap);
    			resultMap.put("result", tempMap.get("result"));
    			resultMap.put("invalidateNodeName", tempMap.get("invalidateNodeName"));
        		return resultMap;
    		}
        }
        
        //split->humen(回退节点)
        if(fromNodeOfcurrBackNode.getNodeType().equals(BPMAbstractNode.NodeType.split)){
        	Map<String,Object> tempMap= isWithdrawActivityOfSplitValid(fromNodeOfcurrBackNode,theCase);
        	if(null!=tempMap.get("normal_nodes")){
				normalNodesMap.putAll((Map)tempMap.get("normal_nodes"));
			}
        	resultMap.put("stepBackToStart", tempMap.get("stepBackToStart"));
			resultMap.put("normal_nodes",normalNodesMap);
			resultMap.put("result", tempMap.get("result"));
			resultMap.put("invalidateNodeName", tempMap.get("invalidateNodeName"));
    		return resultMap;
        }
        
        //from节点为start节点
        if(fromNodeOfcurrBackNode.getNodeType().equals(BPMAbstractNode.NodeType.start)){
        	//则不再回退，返回1，表示撤消流程，变为待发，让ColHelper根据1进行流程撤销的业务处理
        	if(!isNodeValid(theCase,fromNodeOfcurrBackNode)){
				resultMap.put("result", "-1");
				resultMap.put("invalidateNodeName", fromNodeOfcurrBackNode.getName());
				resultMap.put("stepBackToStart", "true");
				return resultMap;
			}
        	normalNodesMap.put(fromNodeOfcurrBackNode.getId(), fromNodeOfcurrBackNode.getId());
        	resultMap.put("normal_nodes",normalNodesMap);
			resultMap.put("result", "1");
    		return resultMap;
        }
        resultMap.put("normal_nodes",normalNodesMap);
		resultMap.put("result", "0");
		return resultMap;
	}

    /**
     * 判断节点是否自动跳过了
     * @param theCase
     * @param node
     * @return
     */
    public static boolean isAutoSkip(BPMCase theCase, BPMAbstractNode node) {
        String normalNodeId= node.getId();
        BPMSeeyonPolicy normalPolicy = node.getSeeyonPolicy();
        Map<String,String> nodeAdditionMap= null;
        Map<String, Map<String,String>> nodeConditionChangeInfoMap= null;
        Object result1= null;
        if(null!=theCase){
            result1= theCase.getData(ActionRunner.WF_NODE_ADDITION_KEY);
        }
        nodeAdditionMap= result1==null?new HashMap<String, String>():(Map<String,String>)result1;
        Object result2= null;
        if(null!=theCase){
            result2= theCase.getData(ActionRunner.WF_NODE_CONDITION_CHANGE_KEY);
        }
        nodeConditionChangeInfoMap= result2==null?new HashMap<String, Map<String,String>>():(Map<String, Map<String,String>>)result2;
        Map<String,String> nodeConditionMap= nodeConditionChangeInfoMap.get(normalNodeId);
        String isDelete= "false";
        if(null!=nodeConditionMap){
            isDelete= nodeConditionMap.get("isDelete");
        }
        if("2".equals(normalPolicy.getNa()) && "false".equals(isDelete) 
                && (null==nodeAdditionMap.get(normalNodeId) || Strings.isBlank(nodeAdditionMap.get(normalNodeId)))){//自动跳过的节点
            return true;
        }
        return false;
    }

    /**
	 * isWithdrawActivityOfSplitValid()
	 * 对一次遇到split节点后，递归调用该方法进行人员状态读取，直到遇到humen(非知会)为止，或者遇到join节点调用isWithdrawActivityOfJoinValid()进行处理。
	 * 注意：第一调用该方法时,fromNodeOfcurrBackNode必须为split节点对象，后面递归过程currentJoinNode可以为join/split/humen(知会)
	 * @param fromNodeOfcurrBackNode 递归调用过程中传入的节点对象，可以为join/split/humen(知会)
	 * @return 返回回退结果：0: 正常回退 1：需要撤消整个流程 -1:不允许回退
	 * @throws BPMException
	 */
    private static Map<String,Object> isWithdrawActivityOfSplitValid(BPMAbstractNode fromNodeOfcurrBackNode,BPMCase theCase) {
    	Map<String,Object> resultMap= new HashMap<String, Object>();
    	Map<String,String> normalNodesMap= new HashMap<String, String>();
    	//获得标识知会的两个常量
        String informActivityPolicy = BPMSeeyonPolicy.SEEYON_POLICY_INFORM.getId();
    	String edocInformActivityPolicy = BPMSeeyonPolicy.EDOC_POLICY_ZHIHUI.getId();
    	//获得该split节点的父亲节点
    	//获得该split节点的所有指向它的up线(肯定只有一条up线)
		BPMTransition fromNodeOfcurrBackNodeUpLinks = (BPMTransition)fromNodeOfcurrBackNode.getUpTransitions().get(0);
		//通过up线获得指向该split节点的from节点
		BPMAbstractNode fromNodeOfcurrSplitNode = fromNodeOfcurrBackNodeUpLinks.getFrom();
		//humen->split->split->humen(回退节点)
		if (fromNodeOfcurrSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.humen)) {
			BPMHumenActivity fromHumenNodeOfSplitNode = (BPMHumenActivity)fromNodeOfcurrSplitNode;
			if(!isNodeValid(theCase,fromHumenNodeOfSplitNode)){
				resultMap.put("result", "-1");
				resultMap.put("invalidateNodeName", fromHumenNodeOfSplitNode.getName());
				return resultMap;
			}
	       	String currFromHumenNodePolicy = fromHumenNodeOfSplitNode.getSeeyonPolicy().getId();
	       	//计算出当前回退节点的from节点是否为知会节点
	       	boolean currFromHumenNodeIsInformNode = 
	       		currFromHumenNodePolicy.equals(informActivityPolicy) || currFromHumenNodePolicy.equals(edocInformActivityPolicy);
	       	boolean isAutoSkip= isAutoSkip(theCase,fromHumenNodeOfSplitNode);
	       	if(currFromHumenNodeIsInformNode || isAutoSkip){//humen(知会)->split->split->humen(回退节点)
	       		//则继续往回退
	    		Map<String,Object> tempMap= isWithdrawActivityOfSplitValid(fromHumenNodeOfSplitNode,theCase);
	    		String result_str= String.valueOf(tempMap.get("result"));
	    		resultMap.put("invalidateNodeName", tempMap.get("invalidateNodeName"));
	    		resultMap.put("stepBackToStart", resultMap.get("stepBackToStart"));
	    		
	    		if("-1".equals(result_str)){
	    			resultMap.put("result", "-1");
					return resultMap;
	    		}
	    		resultMap.put("result", tempMap.get("result"));
	    		if(null != tempMap.get("normal_nodes")){
	    			normalNodesMap.putAll((Map)tempMap.get("normal_nodes"));
	    		}
	    		resultMap.put("normal_nodes", normalNodesMap);
	    		return resultMap;
	       	}else{//humen(非知会)->split->split->humen(回退节点)
	       		//该非知会节点是否触发了新的子流程
    			normalNodesMap.put(fromHumenNodeOfSplitNode.getId(), fromHumenNodeOfSplitNode.getId());
    			resultMap.put("normal_nodes", normalNodesMap);
        		resultMap.put("result", "0");
        		return resultMap;
	       	}
		}
		//split->split->split->humen(回退节点)
	    if(fromNodeOfcurrSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.split)){
	    	//int result= isWithdrawActivityOfSplitValid(fromNodeOfcurrSplitNode);
	    	Map<String,Object> tempMap= isWithdrawActivityOfSplitValid(fromNodeOfcurrSplitNode,theCase);
	    	resultMap.put("invalidateNodeName", tempMap.get("invalidateNodeName"));
	    	resultMap.put("stepBackToStart", tempMap.get("stepBackToStart"));
	    	String result_str= String.valueOf(tempMap.get("result"));
	    	if("-1".equals(result_str)){
    			resultMap.put("result", "-1");
				return resultMap;
    		}
	    	resultMap.put("result", tempMap.get("result"));
    		if(null != tempMap.get("normal_nodes")){
    			normalNodesMap.putAll((Map)tempMap.get("normal_nodes"));
    		}
    		resultMap.put("normal_nodes", normalNodesMap);
    		return resultMap;
	    }
	    //join->split->split->humen(回退节点)
	    if(fromNodeOfcurrSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.join)){
	    	//遇到join节点(join的扩散处理)，此处为join节点第一处理的【唯一入口】和【唯一出口】
	    	Map tempMap= isWithdrawActivityOfJoinValid((BPMActivity)fromNodeOfcurrSplitNode,(BPMActivity)fromNodeOfcurrSplitNode,theCase);
	    	//获得第一个join节点的fromNodeOfcurrBackNode的递归运算结果
	    	Map<String,BPMActivity> subDesNodeSetTmp= (Map<String,BPMActivity>)tempMap.get("subDesNodeSet");
            Map<String,List<BPMActivity>> subRelationInfoSplitMapTmp= (Map<String,List<BPMActivity>>)tempMap.get("subRelationInfoSplitMap");
            String result_str= String.valueOf(tempMap.get("result"));
            resultMap.put("invalidateNodeName", tempMap.get("invalidateNodeName"));
            //如果为-1，则表示有人工活动节点的人员不可用，则直接返回
    		if("-1".equals(result_str)){
    			resultMap.put("result", "-1");
				return resultMap;
    		}
    		//必须要对join节点进行评估计算(收缩处理)，看是否需要穿透其对应的split节点
    		if(subDesNodeSetTmp.size()==1){//如果最终为都归集到了split节点，则穿过该split节点
    			Iterator<String> iter= subDesNodeSetTmp.keySet().iterator();
    			String key= iter.next();
    			BPMActivity desSplitNode= subDesNodeSetTmp.get(key);
    			if(desSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.split)){
    				//汇聚节点为split，继续递归
    				Map tempMap1= isWithdrawActivityOfSplitValid(desSplitNode,theCase);
    				String result_str1= String.valueOf(tempMap1.get("result"));
    				resultMap.put("invalidateNodeName", tempMap1.get("invalidateNodeName"));
    				resultMap.put("stepBackToStart", tempMap1.get("stepBackToStart"));
    				if("-1".equals(result_str1)){
    					resultMap.put("result", "-1");
    					return resultMap;
    				}
    				resultMap.put("result", tempMap1.get("result"));
    	    		if(null != tempMap1.get("normal_nodes")){
    	    			normalNodesMap.putAll((Map)tempMap1.get("normal_nodes"));
    	    		}
    	    		resultMap.put("normal_nodes", normalNodesMap);
    	    		return resultMap;
    			}else{
    				resultMap.put("result", tempMap.get("result"));
            		if(null != tempMap.get("normal_nodes")){
            			normalNodesMap.putAll((Map)tempMap.get("normal_nodes"));
            		}
            		resultMap.put("normal_nodes", normalNodesMap);
            		return resultMap;
    			}
    		}else{//如果最终为没有都归集到了split节点，则不穿过该split节点
    			resultMap.put("result", tempMap.get("result"));
        		if(null != tempMap.get("normal_nodes")){
        			normalNodesMap.putAll((Map)tempMap.get("normal_nodes"));
        		}
        		resultMap.put("normal_nodes", normalNodesMap);
        		resultMap.put("invalidateNodeName", tempMap.get("invalidateNodeName"));
        		return resultMap;
    		}
	    }
	    //start->split->split->humen(回退节点)
	    if(fromNodeOfcurrSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.start)){
        	if(!isNodeValid(theCase,fromNodeOfcurrSplitNode)){
				resultMap.put("result", "-1");
				resultMap.put("invalidateNodeName", fromNodeOfcurrBackNode.getName());
				resultMap.put("stepBackToStart", "true");
				resultMap.put("normal_nodes", normalNodesMap);
				return resultMap;
			}
	       	//则不再回退，返回1，表示撤消流程，变为待发，让ColHelper根据1进行流程撤销的业务处理
	    	normalNodesMap.put(fromNodeOfcurrSplitNode.getId(), fromNodeOfcurrSplitNode.getId());
	    	resultMap.put("result", "1");
		    resultMap.put("normal_nodes", normalNodesMap);
		    return resultMap;
	    }
	    resultMap.put("result", "0");
	    resultMap.put("normal_nodes", normalNodesMap);
	    return resultMap;
	}

    public static boolean isNodeValid(BPMCase theCase,BPMAbstractNode bpmHumenActivity) {
    	BPMActor bpmactor= (BPMActor)bpmHumenActivity.getActorList().get(0);
		BPMParticipant party= bpmactor.getParty();
		if(isBlankNode(bpmHumenActivity) || isAutoSkip(theCase, bpmHumenActivity)){ 
			return true;
		}
		String addition = "";
		boolean checkAgent = true;
		
		//超级节点
		if(ObjectName.WF_SUPER_NODE.equals(party.getType().id)){
		    return true;
		}
		String processMode = bpmHumenActivity.getSeeyonPolicy().getProcessMode();
		
		if(bpmHumenActivity.getNodeType().equals(BPMAbstractNode.NodeType.start)){
			addition =  party.getId();
			checkAgent =false;
		}else if("competition".equals(processMode)){
			//竞争执行模式只需要判断处理人是否可用即可
			addition = getDealMember4CompetitionMode(theCase, bpmHumenActivity, checkAgent);
		}else{
			//其他的情况必须全部可用才能进行回退
			addition = WorkflowUtil.getNodeAdditionFromCase(theCase, bpmHumenActivity.getId(), party, "addition");
		}
		
		if(Strings.isNotBlank(addition)){
			String[] additionList = addition.split(",");
			for(String subAddition : additionList){
				String memberId = subAddition.trim();
				if(Strings.isNotBlank(memberId) && isLong(memberId)){
					boolean memberIsValid = memberValid(Long.valueOf(memberId),checkAgent);
					if(!memberIsValid){
						return false;
					} 
				}
			}
			return true;
		}
		
    	return false;
	}
    
    /**
     * 
     * @Title: getDealMember4CompetitionMode   
     * @Description: 竞争执行模式获取处理人
     * @param theCase
     * @param bpmHumenActivity
     * @param checkAgent
     * @return      
     * @return: String  
     * @date:   2018年10月26日 下午3:47:02
     * @author: xusx
     * @throws
     */
    private static String getDealMember4CompetitionMode(BPMCase theCase,BPMAbstractNode bpmHumenActivity,boolean checkAgent){
    	String competitionMemberId = "";
		WorkItemManager  workItemManager = (WorkItemManager) AppContext.getBean("workItemManager");
		List<WorkitemInfo> workItems = new ArrayList<WorkitemInfo>();

		try {
			workItems = workItemManager.getWorkItemList(theCase.getProcessId(), theCase.getId(), bpmHumenActivity.getId(), "", WorkItem.STATE_FINISHED);
			if(Strings.isEmpty(workItems)){
				BPMWorkItemList  itemlist = (BPMWorkItemList) AppContext.getBean("bpmWorkItemList");
				List<HistoryWorkitemDAO> historyWorkItems = itemlist.getHistoryWorkitemList("", "", theCase.getProcessId(), theCase.getId(), bpmHumenActivity.getId(),new Integer[]{WorkItem.STATE_FINISHED});
				if(Strings.isNotEmpty(historyWorkItems)){
					workItems.addAll(historyWorkItems);
				}
			}
		} catch (BPMException e) {
			logger.error("", e);
		}
		if(Strings.isNotEmpty(workItems)){
			WorkitemInfo item = workItems.get(0);
			competitionMemberId = item.getPerformer();
		}
	    return competitionMemberId;
	 }
    
    /**
     * 校验人员是否有效，是否需要检查代理
     * @param memberId 人员id
     * @param checkAgent true:检查代理，false:不检查代理
     * @return
     */
    private static boolean memberValid(Long memberId,boolean checkAgent){
    	Boolean isValid = true;
    	OrgManager orgManager= (OrgManager)AppContext.getBean("orgManager");
    	if(null!=memberId){
            try {
            	V3xOrgMember member = orgManager.getMemberById(memberId);
				if(null!=member){
					isValid = member.isValid();
					if(!isValid && checkAgent){//是否设置代理
						List<AgentModel> agentToList = MemberAgentBean.getInstance().getAgentModelToList(member.getId());
				        if(Strings.isEmpty(agentToList)){
				        	isValid = false;
				        }else{
				        	isValid = true;
				        }
					}
                }else{
                	isValid = false;
                }
            }catch (Throwable e) {
           	 logger.warn("",e);
            }
        }
    	return isValid;
    }

    /**
     * 是否为空节点
     * @param bpmHumenActivity
     * @return
     */
	public static boolean isBlankNode(BPMAbstractNode bpmHumenActivity) {
		List actorList = bpmHumenActivity.getActorList();
		if(null==actorList){
			return false;
		}
		BPMActor actor = (BPMActor) actorList.get(0);
        BPMParticipant party = actor.getParty();
        //actor标签的partyType属性值
        String partyTypeId = party.getType().id;
        //actor标签的partyId属性值
        String partyId = party.getId();
        if (partyId.equals(WorkFlowMatchUserManager.ORGENT_META_KEY_BlankNode)){
        	return true;
        }
		return false;
	}
	
    /**
     * 判断是否是知会节点 
     * 
     * @return
     */
    public static boolean isInformNode(BPMAbstractNode node) {
        String bPolicy = node.getSeeyonPolicy().getId();
        boolean isInformNode = bPolicy.equals(BPMSeeyonPolicy.SEEYON_POLICY_INFORM.getId())
                || bPolicy.equals(BPMSeeyonPolicy.EDOC_POLICY_ZHIHUI.getId());
        return isInformNode;
    }

	/**
	 * isWithdrawActivityOfJoinValid()
	 * 对一次遇到join节点后，递归调用该方法进行查找人员状态，直到遇到与之对应的split节点或humen(非知会)为止。
	 * 注意：第一调用该方法时,firstJoinNode和currentJoinNode必须为同一个join节点对象，后面递归过程currentJoinNode可以为join/split/humen(知会)
	 * @param firstJoinNode 第一次调用该方法传入的join节点对象
	 * @param currentJoinNode 递归调用过程中传入的节点对象，可以为join/split/humen(知会)
	 * @return Map:
	 *         <"subDesNodeSet",本次递归遇到的没能穿过的节点集合(由1个split节点或多个humen(非知会)组成)>
	 *         <"subRelationInfoSplitMap",本次递归遇到没能穿过的split节点指向的所有humen(非知会)节点列表>
	 *         <"result",本次查找到的人员状态信息(可能为0或-1)>
	 * @throws BPMException
	 */
	private static Map<String,Object> isWithdrawActivityOfJoinValid(
			BPMActivity firstJoinNode,
			BPMActivity currentJoinNode,BPMCase theCase) {
		//为每个currentJoinNode节点定义2个临时存储递归过程运算的结果信息subDesNodeSet和subRelationInfoSplitMap
		Map<String,BPMActivity> subDesNodeSet= new HashMap<String,BPMActivity>();
		Map<String,List<BPMActivity>> subRelationInfoSplitMap= new HashMap<String, List<BPMActivity>>();
		Map<String,String> normalNodesMap= new HashMap<String, String>();
		int result= 0;
		String invalidateNodeName= "";
		//获得标识知会的两个常量
	    String informActivityPolicy = BPMSeeyonPolicy.SEEYON_POLICY_INFORM.getId();
		String edocInformActivityPolicy = BPMSeeyonPolicy.EDOC_POLICY_ZHIHUI.getId();
	
		//循环遍历currentJoinNode节点的所有up线
		List links_ba = currentJoinNode.getUpTransitions();
		for (Iterator iterator = links_ba.iterator(); iterator.hasNext();) {
			BPMTransition upLink = (BPMTransition) iterator.next();
			BPMAbstractNode fromNode = upLink.getFrom();
	        String fromNodeIsDelete = getNodeConditionFromCase(theCase, fromNode, "isDelete");
	        if("false".equals(fromNodeIsDelete)){//from节点没有被删除
	        	//对fromNode节点的类型进行判断
	        	BPMAbstractNode.NodeType fromNodeType = fromNode.getNodeType();
	        	if (fromNodeType.equals(BPMAbstractNode.NodeType.join)) {//join->join
	        		//如果fromNode节点为join节点，则递归调用isWithdrawActivityOfJoinValid继续查找
	                Map tempMap= isWithdrawActivityOfJoinValid(firstJoinNode,(BPMActivity)fromNode,theCase);
	                //获得join节点fromNode的递归运算结果中的result信息
	                String result_str= String.valueOf(tempMap.get("result"));
	                //如果result为-1，则表示有节点上的人员不可用，则直接返回-1
	        		if("-1".equals(result_str)){
	        			invalidateNodeName= (String)tempMap.get("invalidateNodeName");
	        			result= -1;
	        			break;
	        		}else{
	        			//否则，将join节点fromNode的递归运算结果合并到currentJoinNode节点的运算结果中
	        			if(null!= tempMap.get("subDesNodeSet")){
		                	Map<String,BPMActivity> subDesNodeSetTmp= (Map<String,BPMActivity>)tempMap.get("subDesNodeSet");
		                	subDesNodeSet.putAll(subDesNodeSetTmp);
		                }
	        			if(null!= tempMap.get("subRelationInfoSplitMap")){
	        				Map<String,List<BPMActivity>> subRelationInfoSplitMapTmp= (Map<String,List<BPMActivity>>)tempMap.get("subRelationInfoSplitMap");
	        				if(subRelationInfoSplitMapTmp.size()>0){
			                	Iterator<String> iterSplitTmp= subRelationInfoSplitMapTmp.keySet().iterator();
			                	while (iterSplitTmp.hasNext()) {
			                		String splitIdTmp= iterSplitTmp.next();
			                		List<BPMActivity> lastInfoList= subRelationInfoSplitMap.get(splitIdTmp);
			                		if(lastInfoList!=null){
			                			lastInfoList.addAll(subRelationInfoSplitMapTmp.get(splitIdTmp));
			                		}else{
			                			subRelationInfoSplitMap.put(splitIdTmp, subRelationInfoSplitMapTmp.get(splitIdTmp));
			                		}
			                	}
							}
	        			}
	        			if(null!=tempMap.get("normal_nodes")){
	        				normalNodesMap.putAll((Map<String,String>)tempMap.get("normal_nodes"));
	        			}
	        		}
	        	}
	        	if (fromNodeType.equals(BPMAbstractNode.NodeType.humen)) {//humen->join
	        		BPMHumenActivity fromNodeOfJoinNode = (BPMHumenActivity)fromNode;
	        		if(!isNodeValid(theCase,fromNodeOfJoinNode)){
	        			invalidateNodeName= fromNodeOfJoinNode.getName();
	        			result= -1;
	        			break;
	    			}
		        	String currFromHumenNodePolicy = fromNode.getSeeyonPolicy().getId();
		        	//计算出当前回退节点的from节点是否为知会节点
		        	boolean currFromHumenNodeIsInformNode = 
		        		currFromHumenNodePolicy.equals(informActivityPolicy) || currFromHumenNodePolicy.equals(edocInformActivityPolicy);
		        	boolean isAutoSkip= isAutoSkip(theCase,fromNode);
		        	if(currFromHumenNodeIsInformNode || isAutoSkip){//humen(知会)->join
		        		//如果fromNode节点为humen(知会)节点，则递归调用isWithdrawActivityOfJoinValid继续查找
		                Map tempMap= isWithdrawActivityOfJoinValid(firstJoinNode,fromNodeOfJoinNode,theCase);
		                //如果result为-1，则表示有节点上的人员不可用，则直接返回-1
		                String result_str= String.valueOf(tempMap.get("result"));
		        		if("-1".equals(result_str)){
		        			invalidateNodeName= (String)tempMap.get("invalidateNodeName");
		        			result= -1;
		        			break;
		        		}else{
		        			//否则，将humen(知会)节点fromNode的的递归运算结果合并到currentJoinNode节点的运算结果中
		        			if(null!= tempMap.get("subDesNodeSet")){
			                	Map<String,BPMActivity> subDesNodeSetTmp= (Map<String,BPMActivity>)tempMap.get("subDesNodeSet");
			                	subDesNodeSet.putAll(subDesNodeSetTmp);
			                }
		        			if(null!= tempMap.get("subRelationInfoSplitMap")){
		        				Map<String,List<BPMActivity>> subRelationInfoSplitMapTmp= (Map<String,List<BPMActivity>>)tempMap.get("subRelationInfoSplitMap");
		        				if(subRelationInfoSplitMapTmp.size()>0){
				                	Iterator<String> iterSplitTmp= subRelationInfoSplitMapTmp.keySet().iterator();
				                	while (iterSplitTmp.hasNext()) {
				                		String splitIdTmp= iterSplitTmp.next();
				                		List<BPMActivity> lastInfoList= subRelationInfoSplitMap.get(splitIdTmp);
				                		if(lastInfoList!=null){
				                			lastInfoList.addAll(subRelationInfoSplitMapTmp.get(splitIdTmp));
				                		}else{
				                			subRelationInfoSplitMap.put(splitIdTmp, subRelationInfoSplitMapTmp.get(splitIdTmp));
				                		}
				                	}
								}
		        			}
		        			if(null!=tempMap.get("normal_nodes")){
		        				normalNodesMap.putAll((Map<String,String>)tempMap.get("normal_nodes"));
		        			}
		        		}
		        	}else{//humen(非知会)->join
		        		//该非知会节点是否触发了新的子流程
		        		normalNodesMap.put(fromNode.getId(), fromNode.getId());
		        		//将该非知会节点对象作为一个终点对象保存到desNodeSet中
		        		subDesNodeSet.put(fromNodeOfJoinNode.getId()+"",fromNodeOfJoinNode);
		        	}
	        	}
	        	if (fromNodeType.equals(BPMAbstractNode.NodeType.split)) {//split----->join
	        		//将该非知会节点对象作为一个终点对象保存到subDesNodeSet中
	        		subDesNodeSet.put(fromNode.getId()+"",(BPMActivity)fromNode);
	        		List<BPMActivity> lastInfoList= subRelationInfoSplitMap.get(fromNode.getId());
	        		if(lastInfoList== null){
	        			lastInfoList= new ArrayList<BPMActivity>();
	        		}
	        		//注意：currentJoinNode肯定为知会节点，fromNode节点才有可能为split节点
	        		lastInfoList.add(currentJoinNode);
	        		subRelationInfoSplitMap.put(fromNode.getId()+"", lastInfoList);
	        	}
	        	if (fromNodeType.equals(BPMAbstractNode.NodeType.start)) {//start----->join
	        		normalNodesMap.put(fromNode.getId(), fromNode.getId());
	        		//撤销流程
	        		result= 1;
	        	}
	        }
		}
		//分支回退特殊处理
	    if(currentJoinNode.getNodeType().equals(BPMAbstractNode.NodeType.join) && result!=-1){
			//还没回到第一个Join节点，则做如下处理
			if(!firstJoinNode.getId().equals(currentJoinNode.getId())){
				if(subDesNodeSet.size()==1){//如果最终为都归集到了split节点，则穿过该split节点
	    			Iterator<String> iter= subDesNodeSet.keySet().iterator();
	    			String key= iter.next();
	    			BPMActivity desSplitNode= subDesNodeSet.get(key);
	    			if(desSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.split)){
	    				//由于该join节点currentJoinNode对应的split节点desSplitNode被穿过，
	    				//所以currentJoinNode的预算结果不许保存，否则会对其他join节点产生影响
	    		        subDesNodeSet.remove(key);
	    		        subRelationInfoSplitMap.remove(key);
	    		        //汇聚节点为split，穿过去，继续调用isWithdrawActivityOfJoinValid()对desSplitNode进行递归运算
	    		        Map tempMap= isWithdrawActivityOfJoinValid(firstJoinNode,desSplitNode,theCase);
	    		        //如果result为-1，则表示有节点上的人员不可用，则直接返回-1
		                String result_str= String.valueOf(tempMap.get("result"));
		        		if("-1".equals(result_str)){
		        			invalidateNodeName= (String)tempMap.get("invalidateNodeName");
		        			result= -1;
		        		}else{
		        			//否则，将split节点desSplitNode的递归运算结果合并到currentJoinNode节点的运算结果中
		        			if(null!= tempMap.get("subDesNodeSet")){
			                	Map<String,BPMActivity> subDesNodeSetTmp= (Map<String,BPMActivity>)tempMap.get("subDesNodeSet");
			                	subDesNodeSet.putAll(subDesNodeSetTmp);
			                }
		        			if(null!= tempMap.get("subRelationInfoSplitMap")){
		        				Map<String,List<BPMActivity>> subRelationInfoSplitMapTmp= (Map<String,List<BPMActivity>>)tempMap.get("subRelationInfoSplitMap");
		        				if(subRelationInfoSplitMapTmp.size()>0){
				                	Iterator<String> iterSplitTmp= subRelationInfoSplitMapTmp.keySet().iterator();
				                	while (iterSplitTmp.hasNext()) {
				                		String splitIdTmp= iterSplitTmp.next();
				                		List<BPMActivity> lastInfoList= subRelationInfoSplitMap.get(splitIdTmp);
				                		if(lastInfoList!=null){
				                			lastInfoList.addAll(subRelationInfoSplitMapTmp.get(splitIdTmp));
				                		}else{
				                			subRelationInfoSplitMap.put(splitIdTmp, subRelationInfoSplitMapTmp.get(splitIdTmp));
				                		}
				                	}
								}
		        			}
		        			if(null!=tempMap.get("normal_nodes")){
		        				normalNodesMap.putAll((Map<String,String>)tempMap.get("normal_nodes"));
		        			}
		        		}
	    			}
	    		}else{//如果最终为没有都归集到了split节点，则不穿过该split节点
	    			//do nothing
	    			subRelationInfoSplitMap.clear();
	    		}
			}
	    }
	    //返回currentJoinNode节点的运算结果
		Map tempMap= new HashMap();
		tempMap.put("subDesNodeSet", subDesNodeSet);
		tempMap.put("subRelationInfoSplitMap", subRelationInfoSplitMap);
		tempMap.put("result", String.valueOf(result));
		tempMap.put("normal_nodes", normalNodesMap);
		tempMap.put("invalidateNodeName", invalidateNodeName);
		return tempMap;
	}
	/**
	 * getAllNFNodes()
	 * 以指定的非知会节点集合为起点，查找后续的所有带有子流程的非知会节点列表
	 * @param normalNodes 非知会节点集合
	 * @param process 流程模板定义对象
	 * @return
	 */
	public static List<String> getAllNFNodes(Map normalNodes,BPMProcess process,BPMCase theCase) {
		List<String> returnList= new ArrayList<String>();
		Iterator iter= normalNodes.keySet().iterator();
		Map<String,String> passedNodes= new HashMap<String, String>();
		while (iter.hasNext()) {
			String activityId = (String) iter.next();
			if(null!= activityId){
				if("start".equals(activityId)){
					BPMAbstractNode activity= process.getStart();
					getAllNFNodesByActivityNode(activity,returnList,theCase,passedNodes);
				}else{
					BPMActivity activity= process.getActivityById(activityId);
					if(null!= activity){
						getAllNFNodesByActivityNode(activity,returnList,theCase,passedNodes);
					}
				}
			}
		}
		return returnList;
	}

	/**
	 * 
	 * @param activity
	 * @param returnList
	 */
	private static void getAllNFNodesByActivityNode(BPMAbstractNode activity,
			List<String> returnList,BPMCase theCase,Map<String,String> passedNodes) {
		if(activity.getNodeType().equals(BPMAbstractNode.NodeType.end)){
			return;
		}
	    if(null!=passedNodes.get(activity.getId())){
	        return;
	    }
	    passedNodes.put(activity.getId(), activity.getId());
		String isDelete= getNodeConditionFromCase(theCase, activity, "isDelete");
		if("false".equals(isDelete)){
			if(activity.getNodeType().equals(BPMAbstractNode.NodeType.humen)){
				BPMHumenActivity humenActivity= (BPMHumenActivity)activity;
				String seeyonPolicyId= humenActivity.getSeeyonPolicy().getId();
				boolean isInformNode = 
					seeyonPolicyId.equals(informActivityPolicy) || seeyonPolicyId.equals(edocInformActivityPolicy);
				if(!isInformNode){//非知会节点
					if("1".equals(humenActivity.getSeeyonPolicy().getNF())){
						if(!returnList.contains(activity.getId())){
							returnList.add(activity.getId());
						}
					}
					List downs= activity.getDownTransitions();
					BPMAbstractNode toNode= ((BPMTransition)downs.get(0)).getTo();
					getAllNFNodesByActivityNode(toNode,returnList,theCase,passedNodes);
				}else{//知会节点
					List downs= activity.getDownTransitions();
					BPMAbstractNode toNode= ((BPMTransition)downs.get(0)).getTo();
					getAllNFNodesByActivityNode(toNode,returnList,theCase,passedNodes);
				}
			}else if(activity.getNodeType().equals(BPMAbstractNode.NodeType.join)){
				List downs= activity.getDownTransitions();
				BPMAbstractNode toNode= ((BPMTransition)downs.get(0)).getTo();
				getAllNFNodesByActivityNode(toNode,returnList,theCase,passedNodes);
			}else if(activity.getNodeType().equals(BPMAbstractNode.NodeType.split)){
				List downs= activity.getDownTransitions();
				for (Iterator iterator = downs.iterator(); iterator.hasNext();) {
					BPMTransition down = (BPMTransition) iterator.next();
					BPMAbstractNode toNode= down.getTo();
					getAllNFNodesByActivityNode(toNode,returnList,theCase,passedNodes);
				}
			}else if(activity.getNodeType().equals(BPMAbstractNode.NodeType.start)){
				List downs= activity.getDownTransitions();
				BPMAbstractNode toNode= ((BPMTransition)downs.get(0)).getTo();
				getAllNFNodesByActivityNode(toNode,returnList,theCase,passedNodes);
			}
		}
	}

    /**
     * 判断str数组中中是否包含了某一个userId
     * @param userId 需要判断的userId
     * @param str 给定的字符串userIdString
     * @return true表示userId在字符串中，false表示不在
     */
    public static boolean userInString(String userId,String str){
        if(userId==null||"".equals(userId)||str==null||"".equals(str)) return false;
        String[] ret = str.split(";");
        for(int i=0;i<ret.length;i++){
            if(userId.equalsIgnoreCase(ret[i])) return true;
        }
        return false;
    }
    
    /**
     * isCanNotStepOfGivenPolicy()
     * 递归计算出本次可回退的状态标志，如果有遇到给定的节点权限，则允许回退（例如核定节点）。
     * 注意：第一次调用本方法时currBackNode节点只能为humen(非知会节点)，后续递归调用只能是humen(知会节点)
     * @param currBackNode 人工活动节点(只能为知会或非知会)
     * @param givenPolicyId 给定的节点权限(例如核定)，可以为空，但如果为空，则表示正常回退
     * @param isConsiderHumenValid 布尔类型，用来标志回退时是否考虑人员不可用的情况（例如人员离职等），true表示考虑，false表示不考虑
     * @return 返回结构说明： Map<String,Object>: key——"result",value——0: 正常回退 1：需要撤消整个流程 -1:不允许回退;
     *                                           key——"normal_nodes",value——可回退到的正常节点id，但前提是result的value不为-1或1，只能为0，否则该参数无用;
     */
    public static Map<String,Object>  isCanNotStepOfGivenPolicy(
    		BPMActivity currBackNode,
    		final String givenPolicyId,
    		final boolean isConsiderHumenValid,BPMCase theCase) {
    	Map<String,Object> resultMap= new HashMap<String, Object>();
    	Map<String,String> normalNodesMap= new HashMap<String, String>();
    	if(null== currBackNode){
    		resultMap.put("normal_nodes", normalNodesMap);
    		resultMap.put("result", "-1");
    		return resultMap;
    	}
    	//获得当前回退节点的所有指向它的up线(肯定只有一条up线)
		BPMTransition currBackNodeUpLinks = (BPMTransition)currBackNode.getUpTransitions().get(0);
		//通过up线获得指向当前回退节点的from节点
        BPMAbstractNode fromNodeOfcurrBackNode = currBackNodeUpLinks.getFrom();
        //humen->humen(回退节点)
        if(fromNodeOfcurrBackNode.getNodeType().equals(BPMAbstractNode.NodeType.humen)){
        	BPMHumenActivity fromHumenNodeOfcurrBackNode = (BPMHumenActivity)fromNodeOfcurrBackNode;
        	if(isConsiderHumenValid){
                BPMActor actor = (BPMActor) fromHumenNodeOfcurrBackNode.getActorList().get(0);
                BPMParticipant party = actor.getParty();
                String partyTypeId= party.getType().id;
            	if(!"normal".equals(fromHumenNodeOfcurrBackNode.isValid()) && "user".equals(partyTypeId)){
	        		resultMap.put("result", "-1");
	        		return resultMap;
            	}
        	}
        	String currFromHumenNodePolicy = fromHumenNodeOfcurrBackNode.getSeeyonPolicy().getId();
        	//计算出当前回退节点的from节点是否为知会节点
        	boolean currFromHumenNodeIsInformNode = 
        		currFromHumenNodePolicy.equals(informActivityPolicy) || currFromHumenNodePolicy.equals(edocInformActivityPolicy);
        	if(currFromHumenNodeIsInformNode){//humen(知会)->humen(回退节点)
        		//以该知会节点为起始节点，继续往回查找
        		Map<String,Object> tempResultMap= isCanNotStepOfGivenPolicy(fromHumenNodeOfcurrBackNode,givenPolicyId,isConsiderHumenValid,theCase);
        		if(null!=tempResultMap.get("normal_nodes")){
            		normalNodesMap.putAll((Map<String,String>)tempResultMap.get("normal_nodes"));
            	}
        		resultMap.put("normal_nodes", normalNodesMap);
        		resultMap.put("result", tempResultMap.get("result"));
        		return resultMap;
        	}else{//humen(非知会)->humen(回退节点)
        		if( null!=givenPolicyId && !"".equals(givenPolicyId.trim()) 
        				&& currFromHumenNodePolicy.trim().equals(givenPolicyId.trim())){
        			//遇到指定的节点权限的节点,则不允许回退，否则可以回退
        			normalNodesMap.put(fromNodeOfcurrBackNode.getId(), fromNodeOfcurrBackNode.getId());
            		resultMap.put("normal_nodes", normalNodesMap);
            		resultMap.put("result", "-1");
            		return resultMap;
        		}else{
        			normalNodesMap.put(fromNodeOfcurrBackNode.getId(), fromNodeOfcurrBackNode.getId());
            		resultMap.put("normal_nodes", normalNodesMap);
            		resultMap.put("result", "0");
            		return resultMap;
        		}
        	}
        }
        
        //join->humen(回退节点)
        if(fromNodeOfcurrBackNode.getNodeType().equals(BPMAbstractNode.NodeType.join)){
        	//遇到join节点，此处为join节点第一处理的【唯一入口】和【唯一出口】
        	Map tempMap= isCanNotStepOfGivenPolicyForJoin((BPMActivity)fromNodeOfcurrBackNode,
        			(BPMActivity)fromNodeOfcurrBackNode,givenPolicyId,isConsiderHumenValid,theCase);
        	//获得第一个join节点的fromNodeOfcurrBackNode的递归运算结果
			Map<String,BPMActivity> subDesNodeSetTmp= (Map<String,BPMActivity>)tempMap.get("subDesNodeSet");
            Map<String,List<BPMActivity>> subRelationInfoSplitMapTmp= (Map<String,List<BPMActivity>>)tempMap.get("subRelationInfoSplitMap");
            String result_str= String.valueOf(tempMap.get("result"));
            //如果为-1，则表示有人工活动节点的人员不可用或遇到指定节点权限givenPolicyId，则直接返回
    		if("-1".equals(result_str)){
    			resultMap.put("result", "-1");
        		return resultMap;
    		}
    		//必须要对join节点进行评估计算(收缩处理)，看是否需要穿透其对应的split节点
    		if(subDesNodeSetTmp.size()==1){//如果最终为都归集到了split节点，则穿过该split节点
    			Iterator<String> iter= subDesNodeSetTmp.keySet().iterator();
    			String key= iter.next();
    			BPMActivity desSplitNode= subDesNodeSetTmp.get(key);
    			if(desSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.split)){
    				//汇聚节点为split，继续递归调用isWithdrawActivityOfSplitValid进行判断人工节点的人员状态
    				Map<String,Object> tempMap1= isCanNotStepOfGivenPolicyForSplit(desSplitNode,givenPolicyId,isConsiderHumenValid,theCase);
    				if(null!=tempMap1.get("normal_nodes")){
    					normalNodesMap.putAll((Map<String,String>)tempMap1.get("normal_nodes"));
    				}
    				resultMap.put("result", tempMap1.get("result"));
    				resultMap.put("normal_nodes", normalNodesMap);
    				return resultMap;
    			}else{
    				if(null!=tempMap.get("normal_nodes")){
        				normalNodesMap.putAll((Map)tempMap.get("normal_nodes"));
        			}
        			resultMap.put("normal_nodes",normalNodesMap);
        			resultMap.put("result", tempMap.get("result"));
            		return resultMap;
    			}
    		}else{//如果最终为没有都归集到了split节点，则不穿过该split节点
    			if(null!=tempMap.get("normal_nodes")){
    				normalNodesMap.putAll((Map)tempMap.get("normal_nodes"));
    			}
    			resultMap.put("normal_nodes",normalNodesMap);
    			resultMap.put("result", tempMap.get("result"));
        		return resultMap;
    		}
        }
        
        //split->humen(回退节点)
        if(fromNodeOfcurrBackNode.getNodeType().equals(BPMAbstractNode.NodeType.split)){
        	Map<String,Object> tempMap= isCanNotStepOfGivenPolicyForSplit(fromNodeOfcurrBackNode,givenPolicyId,isConsiderHumenValid,theCase);
        	if(null!=tempMap.get("normal_nodes")){
				normalNodesMap.putAll((Map)tempMap.get("normal_nodes"));
			}
			resultMap.put("normal_nodes",normalNodesMap);
			resultMap.put("result", tempMap.get("result"));
    		return resultMap;
        }
        
        //from节点为start节点
        if(fromNodeOfcurrBackNode.getNodeType().equals(BPMAbstractNode.NodeType.start)){
        	//则不再回退，返回1，表示撤消流程，变为待发，让ColHelper根据1进行流程撤销的业务处理
        	normalNodesMap.put(fromNodeOfcurrBackNode.getId(), fromNodeOfcurrBackNode.getId());
        	resultMap.put("normal_nodes",normalNodesMap);
			resultMap.put("result", "1");
    		return resultMap;
        }
        resultMap.put("normal_nodes",normalNodesMap);
		resultMap.put("result", "0");
		return resultMap;
	}

    /**
	 * isCanNotStepOfGivenPolicyForSplit()
	 * 对一次遇到split节点后，递归调用该方法进行人员状态读取，直到遇到humen(非知会)为止，或者遇到join节点调用isWithdrawActivityOfJoinValid()进行处理。
	 * 注意：第一调用该方法时,fromNodeOfcurrBackNode必须为split节点对象，后面递归过程currentJoinNode可以为join/split/humen(知会)
	 * @param fromNodeOfcurrBackNode 递归调用过程中传入的节点对象，可以为join/split/humen(知会)
	 * @param givenPolicyId 给定的节点权限(例如核定)，可以为空，但如果为空，则表示正常回退
     * @param isConsiderHumenValid 布尔类型，用来标志回退时是否考虑人员不可用的情况（例如人员离职等），true表示考虑，false表示不考虑
	 * @return 返回回退结果：0: 正常回退 1：需要撤消整个流程 -1:不允许回退
	 * @throws BPMException
	 */
    private static Map<String,Object> isCanNotStepOfGivenPolicyForSplit(
    		BPMAbstractNode fromNodeOfcurrBackNode,
    		final String givenPolicyId,
    		final boolean isConsiderHumenValid,BPMCase theCase) {
    	Map<String,Object> resultMap= new HashMap<String, Object>();
    	Map<String,String> normalNodesMap= new HashMap<String, String>();
    	//获得标识知会的两个常量
        String informActivityPolicy = BPMSeeyonPolicy.SEEYON_POLICY_INFORM.getId();
    	String edocInformActivityPolicy = BPMSeeyonPolicy.EDOC_POLICY_ZHIHUI.getId();
    	//获得该split节点的父亲节点
    	//获得该split节点的所有指向它的up线(肯定只有一条up线)
		BPMTransition fromNodeOfcurrBackNodeUpLinks = (BPMTransition)fromNodeOfcurrBackNode.getUpTransitions().get(0);
		//通过up线获得指向该split节点的from节点
		BPMAbstractNode fromNodeOfcurrSplitNode = fromNodeOfcurrBackNodeUpLinks.getFrom();
		//humen->split->split->humen(回退节点)
		if (fromNodeOfcurrSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.humen)) {
			BPMHumenActivity fromHumenNodeOfSplitNode = (BPMHumenActivity)fromNodeOfcurrSplitNode;
			if(isConsiderHumenValid){
			    BPMActor actor = (BPMActor) fromHumenNodeOfSplitNode.getActorList().get(0);
                BPMParticipant party = actor.getParty();
                String partyTypeId= party.getType().id;
				if(!"normal".equals(fromHumenNodeOfSplitNode.isValid()) && "user".equals(partyTypeId)){
					resultMap.put("result", "-1");
					return resultMap;
		       	}
			}
	       	String currFromHumenNodePolicy = fromHumenNodeOfSplitNode.getSeeyonPolicy().getId();
	       	//计算出当前回退节点的from节点是否为知会节点
	       	boolean currFromHumenNodeIsInformNode = 
	       		currFromHumenNodePolicy.equals(informActivityPolicy) || currFromHumenNodePolicy.equals(edocInformActivityPolicy);
	       	if(currFromHumenNodeIsInformNode){//humen(知会)->split->split->humen(回退节点)
	       		//则继续往回退
	    		Map<String,Object> tempMap= isCanNotStepOfGivenPolicyForSplit(fromHumenNodeOfSplitNode,givenPolicyId,isConsiderHumenValid,theCase);
	    		String result_str= String.valueOf(tempMap.get("result"));
	    		if("-1".equals(result_str)){
	    			resultMap.put("result", "-1");
					return resultMap;
	    		}
	    		resultMap.put("result", tempMap.get("result"));
	    		if(null != tempMap.get("normal_nodes")){
	    			normalNodesMap.putAll((Map)tempMap.get("normal_nodes"));
	    		}
	    		resultMap.put("normal_nodes", normalNodesMap);
	    		return resultMap;
	       	}else{//humen(非知会)->split->split->humen(回退节点)
	       		if( null!=givenPolicyId && !"".equals(givenPolicyId.trim()) 
        				&& currFromHumenNodePolicy.trim().equals(givenPolicyId.trim())){
        			//遇到指定的节点权限的节点,则不允许回退，否则可以回退
	       			normalNodesMap.put(fromHumenNodeOfSplitNode.getId(), fromHumenNodeOfSplitNode.getId());
	    			resultMap.put("normal_nodes", normalNodesMap);
	        		resultMap.put("result", "-1");
	        		return resultMap;
	       		}else{
	       			normalNodesMap.put(fromHumenNodeOfSplitNode.getId(), fromHumenNodeOfSplitNode.getId());
	    			resultMap.put("normal_nodes", normalNodesMap);
	        		resultMap.put("result", "0");
	        		return resultMap;
	       		}
	       	}
		}
		//split->split->split->humen(回退节点)
	    if(fromNodeOfcurrSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.split)){
	    	Map<String,Object> tempMap= isCanNotStepOfGivenPolicyForSplit(fromNodeOfcurrSplitNode,givenPolicyId,isConsiderHumenValid,theCase);
	    	String result_str= String.valueOf(tempMap.get("result"));
	    	if("-1".equals(result_str)){
    			resultMap.put("result", "-1");
				return resultMap;
    		}
	    	resultMap.put("result", tempMap.get("result"));
    		if(null != tempMap.get("normal_nodes")){
    			normalNodesMap.putAll((Map)tempMap.get("normal_nodes"));
    		}
    		resultMap.put("normal_nodes", normalNodesMap);
    		return resultMap;
	    }
	    //join->split->split->humen(回退节点)
	    if(fromNodeOfcurrSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.join)){
	    	//遇到join节点(join的扩散处理)，此处为join节点第一处理的【唯一入口】和【唯一出口】
	    	Map tempMap= isCanNotStepOfGivenPolicyForJoin(
	    			(BPMActivity)fromNodeOfcurrSplitNode,(BPMActivity)fromNodeOfcurrSplitNode,givenPolicyId,isConsiderHumenValid,theCase);
	    	//获得第一个join节点的fromNodeOfcurrBackNode的递归运算结果
	    	Map<String,BPMActivity> subDesNodeSetTmp= (Map<String,BPMActivity>)tempMap.get("subDesNodeSet");
            Map<String,List<BPMActivity>> subRelationInfoSplitMapTmp= (Map<String,List<BPMActivity>>)tempMap.get("subRelationInfoSplitMap");
            String result_str= String.valueOf(tempMap.get("result"));
            //如果为-1，则表示有人工活动节点的人员不可用，则直接返回
    		if("-1".equals(result_str)){
    			resultMap.put("result", "-1");
				return resultMap;
    		}
    		//必须要对join节点进行评估计算(收缩处理)，看是否需要穿透其对应的split节点
    		if(subDesNodeSetTmp.size()==1){//如果最终为都归集到了split节点，则穿过该split节点
    			Iterator<String> iter= subDesNodeSetTmp.keySet().iterator();
    			String key= iter.next();
    			BPMActivity desSplitNode= subDesNodeSetTmp.get(key);
    			if(desSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.split)){
    				//汇聚节点为split，继续递归
    				Map tempMap1= isCanNotStepOfGivenPolicyForSplit(desSplitNode,givenPolicyId,isConsiderHumenValid,theCase);
    				String result_str1= String.valueOf(tempMap1.get("result"));
    				if("-1".equals(result_str1)){
    					resultMap.put("result", "-1");
    					return resultMap;
    				}
    				resultMap.put("result", tempMap1.get("result"));
    	    		if(null != tempMap1.get("normal_nodes")){
    	    			normalNodesMap.putAll((Map)tempMap1.get("normal_nodes"));
    	    		}
    	    		resultMap.put("normal_nodes", normalNodesMap);
    	    		return resultMap;
    			}else{
    				resultMap.put("result", tempMap.get("result"));
            		if(null != tempMap.get("normal_nodes")){
            			normalNodesMap.putAll((Map)tempMap.get("normal_nodes"));
            		}
            		resultMap.put("normal_nodes", normalNodesMap);
            		return resultMap;
    			}
    		}else{//如果最终为没有都归集到了split节点，则不穿过该split节点
    			resultMap.put("result", tempMap.get("result"));
        		if(null != tempMap.get("normal_nodes")){
        			normalNodesMap.putAll((Map)tempMap.get("normal_nodes"));
        		}
        		resultMap.put("normal_nodes", normalNodesMap);
        		return resultMap;
    		}
	    }
	    //start->split->split->humen(回退节点)
	    if(fromNodeOfcurrSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.start)){
	       	//则不再回退，返回1，表示撤消流程，变为待发，让ColHelper根据1进行流程撤销的业务处理
	    	normalNodesMap.put(fromNodeOfcurrSplitNode.getId(), fromNodeOfcurrSplitNode.getId());
	    	resultMap.put("result", "1");
		    resultMap.put("normal_nodes", normalNodesMap);
		    return resultMap;
	    }
	    resultMap.put("result", "0");
	    resultMap.put("normal_nodes", normalNodesMap);
	    return resultMap;
	}

    /**
	 * isCanNotStepOfGivenPolicyForJoin()
	 * 对一次遇到join节点后，递归调用该方法进行查找人员状态，直到遇到与之对应的split节点或humen(非知会)为止。
	 * 注意：第一调用该方法时,firstJoinNode和currentJoinNode必须为同一个join节点对象，后面递归过程currentJoinNode可以为join/split/humen(知会)
	 * @param firstJoinNode 第一次调用该方法传入的join节点对象
	 * @param currentJoinNode 递归调用过程中传入的节点对象，可以为join/split/humen(知会)
	 * @param givenPolicyId 给定的节点权限id
	 * @param isConsiderHumenValid 布尔类型，用来标志回退时是否考虑人员不可用的情况（例如人员离职等），true表示考虑，false表示不考虑
	 * @return Map:
	 *         <"subDesNodeSet",本次递归遇到的没能穿过的节点集合(由1个split节点或多个humen(非知会)组成)>
	 *         <"subRelationInfoSplitMap",本次递归遇到没能穿过的split节点指向的所有humen(非知会)节点列表>
	 *         <"result",本次查找到的人员状态信息(可能为0或-1)>
	 * @throws BPMException
	 */
	private static Map<String,Object> isCanNotStepOfGivenPolicyForJoin(
			BPMActivity firstJoinNode,
			BPMActivity currentJoinNode,
			final String givenPolicyId,
			final boolean isConsiderHumenValid,BPMCase theCase) {
		//为每个currentJoinNode节点定义2个临时存储递归过程运算的结果信息subDesNodeSet和subRelationInfoSplitMap
		Map<String,BPMActivity> subDesNodeSet= new HashMap<String,BPMActivity>();
		Map<String,List<BPMActivity>> subRelationInfoSplitMap= new HashMap<String, List<BPMActivity>>();
		Map<String,String> normalNodesMap= new HashMap<String, String>();
		int result= 0;
		//获得标识知会的两个常量
	    String informActivityPolicy = BPMSeeyonPolicy.SEEYON_POLICY_INFORM.getId();
		String edocInformActivityPolicy = BPMSeeyonPolicy.EDOC_POLICY_ZHIHUI.getId();
	
		//循环遍历currentJoinNode节点的所有up线
		List links_ba = currentJoinNode.getUpTransitions();
		for (Iterator iterator = links_ba.iterator(); iterator.hasNext();) {
			BPMTransition upLink = (BPMTransition) iterator.next();
			BPMAbstractNode fromNode = upLink.getFrom();
			String fromNodeIsDelete= getNodeConditionFromCase(theCase, fromNode, "isDelete");
	        if("false".equals(fromNodeIsDelete)){//from节点没有被删除
	        	//对fromNode节点的类型进行判断
	        	BPMAbstractNode.NodeType fromNodeType = fromNode.getNodeType();
	        	if (fromNodeType.equals(BPMAbstractNode.NodeType.join)) {//join->join
	        		//如果fromNode节点为join节点，则递归调用isWithdrawActivityOfJoinValid继续查找
	                Map tempMap= isCanNotStepOfGivenPolicyForJoin(firstJoinNode,(BPMActivity)fromNode,givenPolicyId,isConsiderHumenValid,theCase);
	                //获得join节点fromNode的递归运算结果中的result信息
	                String result_str= String.valueOf(tempMap.get("result"));
	                //如果result为-1，则表示有节点上的人员不可用或遇到指定节点权限id的节点，则直接返回-1
	        		if("-1".equals(result_str)){
	        			result= -1;
	        			break;
	        		}else{
	        			//否则，将join节点fromNode的递归运算结果合并到currentJoinNode节点的运算结果中
	        			if(null!= tempMap.get("subDesNodeSet")){
		                	Map<String,BPMActivity> subDesNodeSetTmp= (Map<String,BPMActivity>)tempMap.get("subDesNodeSet");
		                	subDesNodeSet.putAll(subDesNodeSetTmp);
		                }
	        			if(null!= tempMap.get("subRelationInfoSplitMap")){
	        				Map<String,List<BPMActivity>> subRelationInfoSplitMapTmp= (Map<String,List<BPMActivity>>)tempMap.get("subRelationInfoSplitMap");
	        				if(subRelationInfoSplitMapTmp.size()>0){
			                	Iterator<String> iterSplitTmp= subRelationInfoSplitMapTmp.keySet().iterator();
			                	while (iterSplitTmp.hasNext()) {
			                		String splitIdTmp= iterSplitTmp.next();
			                		List<BPMActivity> lastInfoList= subRelationInfoSplitMap.get(splitIdTmp);
			                		if(lastInfoList!=null){
			                			lastInfoList.addAll(subRelationInfoSplitMapTmp.get(splitIdTmp));
			                		}else{
			                			subRelationInfoSplitMap.put(splitIdTmp, subRelationInfoSplitMapTmp.get(splitIdTmp));
			                		}
			                	}
							}
	        			}
	        			if(null!=tempMap.get("normal_nodes")){
	        				normalNodesMap.putAll((Map<String,String>)tempMap.get("normal_nodes"));
	        			}
	        		}
	        	}
	        	if (fromNodeType.equals(BPMAbstractNode.NodeType.humen)) {//humen->join
	        		BPMHumenActivity fromNodeOfJoinNode = (BPMHumenActivity)fromNode;
	        		if(isConsiderHumenValid){
	        		    BPMActor actor = (BPMActor) fromNodeOfJoinNode.getActorList().get(0);
	                    BPMParticipant party = actor.getParty();
	                    String partyTypeId= party.getType().id;
	        			if(!"normal".equals(fromNodeOfJoinNode.isValid()) && "user".equals(partyTypeId)){
			        		result= -1;
		        			break;
			        	}
	        		}
		        	String currFromHumenNodePolicy = fromNode.getSeeyonPolicy().getId();
		        	//计算出当前回退节点的from节点是否为知会节点
		        	boolean currFromHumenNodeIsInformNode = 
		        		currFromHumenNodePolicy.equals(informActivityPolicy) || currFromHumenNodePolicy.equals(edocInformActivityPolicy);
		        	if(currFromHumenNodeIsInformNode){//humen(知会)->join
		        		//如果fromNode节点为humen(知会)节点，则递归调用isWithdrawActivityOfJoinValid继续查找
		                Map tempMap= isCanNotStepOfGivenPolicyForJoin(firstJoinNode,fromNodeOfJoinNode,givenPolicyId,isConsiderHumenValid,theCase);
		                //如果result为-1，则表示有节点上的人员不可用或遇到指定节点权限id的节点，则直接返回-1
		                String result_str= String.valueOf(tempMap.get("result"));
		        		if("-1".equals(result_str)){
		        			result= -1;
		        			break;
		        		}else{
		        			//否则，将humen(知会)节点fromNode的的递归运算结果合并到currentJoinNode节点的运算结果中
		        			if(null!= tempMap.get("subDesNodeSet")){
			                	Map<String,BPMActivity> subDesNodeSetTmp= (Map<String,BPMActivity>)tempMap.get("subDesNodeSet");
			                	subDesNodeSet.putAll(subDesNodeSetTmp);
			                }
		        			if(null!= tempMap.get("subRelationInfoSplitMap")){
		        				Map<String,List<BPMActivity>> subRelationInfoSplitMapTmp= (Map<String,List<BPMActivity>>)tempMap.get("subRelationInfoSplitMap");
		        				if(subRelationInfoSplitMapTmp.size()>0){
				                	Iterator<String> iterSplitTmp= subRelationInfoSplitMapTmp.keySet().iterator();
				                	while (iterSplitTmp.hasNext()) {
				                		String splitIdTmp= iterSplitTmp.next();
				                		List<BPMActivity> lastInfoList= subRelationInfoSplitMap.get(splitIdTmp);
				                		if(lastInfoList!=null){
				                			lastInfoList.addAll(subRelationInfoSplitMapTmp.get(splitIdTmp));
				                		}else{
				                			subRelationInfoSplitMap.put(splitIdTmp, subRelationInfoSplitMapTmp.get(splitIdTmp));
				                		}
				                	}
								}
		        			}
		        			if(null!=tempMap.get("normal_nodes")){
		        				normalNodesMap.putAll((Map<String,String>)tempMap.get("normal_nodes"));
		        			}
		        		}
		        	}else{//humen(非知会)->join
		        		if( null!=givenPolicyId && !"".equals(givenPolicyId.trim()) 
		        				&& currFromHumenNodePolicy.trim().equals(givenPolicyId.trim())){
		        			normalNodesMap.put(fromNode.getId(), fromNode.getId());
			        		//将该非知会节点对象作为一个终点对象保存到desNodeSet中
			        		subDesNodeSet.put(fromNodeOfJoinNode.getId()+"",fromNodeOfJoinNode);
			        		result= -1;
			        		break;
		        		}else{
		        			normalNodesMap.put(fromNode.getId(), fromNode.getId());
			        		//将该非知会节点对象作为一个终点对象保存到desNodeSet中
			        		subDesNodeSet.put(fromNodeOfJoinNode.getId()+"",fromNodeOfJoinNode);
		        		}
		        	}
	        	}
	        	if (fromNodeType.equals(BPMAbstractNode.NodeType.split)) {//split----->join
	        		//将该非知会节点对象作为一个终点对象保存到subDesNodeSet中
	        		subDesNodeSet.put(fromNode.getId()+"",(BPMActivity)fromNode);
	        		List<BPMActivity> lastInfoList= subRelationInfoSplitMap.get(fromNode.getId());
	        		if(lastInfoList== null){
	        			lastInfoList= new ArrayList<BPMActivity>();
	        		}
	        		//注意：currentJoinNode肯定为知会节点，fromNode节点才有可能为split节点
	        		lastInfoList.add(currentJoinNode);
	        		subRelationInfoSplitMap.put(fromNode.getId()+"", lastInfoList);
	        	}
	        	if (fromNodeType.equals(BPMAbstractNode.NodeType.start)) {//start----->join
	        		normalNodesMap.put(fromNode.getId(), fromNode.getId());
	        		//撤销流程
	        		result= 1;
	        	}
	        }
		}
		//分支回退特殊处理
	    if(currentJoinNode.getNodeType().equals(BPMAbstractNode.NodeType.join) && result!=-1){
			//还没回到第一个Join节点，则做如下处理
			if(!firstJoinNode.getId().equals(currentJoinNode.getId())){
				if(subDesNodeSet.size()==1){//如果最终为都归集到了split节点，则穿过该split节点
	    			Iterator<String> iter= subDesNodeSet.keySet().iterator();
	    			String key= iter.next();
	    			BPMActivity desSplitNode= subDesNodeSet.get(key);
	    			if(desSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.split)){
	    				//由于该join节点currentJoinNode对应的split节点desSplitNode被穿过，
	    				//所以currentJoinNode的预算结果不许保存，否则会对其他join节点产生影响
	    		        subDesNodeSet.remove(key);
	    		        subRelationInfoSplitMap.remove(key);
	    		        //汇聚节点为split，穿过去，继续调用isWithdrawActivityOfJoinValid()对desSplitNode进行递归运算
	    		        Map tempMap= isCanNotStepOfGivenPolicyForJoin(firstJoinNode,desSplitNode,givenPolicyId,isConsiderHumenValid,theCase);
	    		        //如果result为-1，则表示有节点上的人员不可用或遇到指定节点权限id的节点，则直接返回-1
		                String result_str= String.valueOf(tempMap.get("result"));
		        		if("-1".equals(result_str)){
		        			result= -1;
		        		}else{
		        			//否则，将split节点desSplitNode的递归运算结果合并到currentJoinNode节点的运算结果中
		        			if(null!= tempMap.get("subDesNodeSet")){
			                	Map<String,BPMActivity> subDesNodeSetTmp= (Map<String,BPMActivity>)tempMap.get("subDesNodeSet");
			                	subDesNodeSet.putAll(subDesNodeSetTmp);
			                }
		        			if(null!= tempMap.get("subRelationInfoSplitMap")){
		        				Map<String,List<BPMActivity>> subRelationInfoSplitMapTmp= (Map<String,List<BPMActivity>>)tempMap.get("subRelationInfoSplitMap");
		        				if(subRelationInfoSplitMapTmp.size()>0){
				                	Iterator<String> iterSplitTmp= subRelationInfoSplitMapTmp.keySet().iterator();
				                	while (iterSplitTmp.hasNext()) {
				                		String splitIdTmp= iterSplitTmp.next();
				                		List<BPMActivity> lastInfoList= subRelationInfoSplitMap.get(splitIdTmp);
				                		if(lastInfoList!=null){
				                			lastInfoList.addAll(subRelationInfoSplitMapTmp.get(splitIdTmp));
				                		}else{
				                			subRelationInfoSplitMap.put(splitIdTmp, subRelationInfoSplitMapTmp.get(splitIdTmp));
				                		}
				                	}
								}
		        			}
		        			if(null!=tempMap.get("normal_nodes")){
		        				normalNodesMap.putAll((Map<String,String>)tempMap.get("normal_nodes"));
		        			}
		        		}
	    			}
	    		}else{//如果最终为没有都归集到了split节点，则不穿过该split节点
	    			//do nothing
	    			subRelationInfoSplitMap.clear();
	    		}
			}
	    }
	    //返回currentJoinNode节点的运算结果
		Map tempMap= new HashMap();
		tempMap.put("subDesNodeSet", subDesNodeSet);
		tempMap.put("subRelationInfoSplitMap", subRelationInfoSplitMap);
		tempMap.put("result", String.valueOf(result));
		tempMap.put("normal_nodes", normalNodesMap);
		return tempMap;
	}
	
	public static String specailCharReplacement(String oldString,String regExp, String newString) {
    	Pattern p = Pattern.compile(regExp);
    	Matcher m = p.matcher(oldString);
    	StringBuffer sb = new StringBuffer();
    	while(m.find()){
    		String group= m.group();
    		m.appendReplacement(sb, newString);
    	}
    	m.appendTail(sb);
		return sb.toString();
	}

    /**
     * 找到指定节点前面相邻的所有人工节点（包括发起者节点）
     * @param node 指定的节点
     * @return 前面相邻的所有节点
     */
    public static List<BPMAbstractNode> findAllParentNodes(BPMAbstractNode node){
        List<BPMAbstractNode> results = new ArrayList<BPMAbstractNode>();
        if(node!=null){
            @SuppressWarnings("rawtypes")
            List nodeups = node.getUpTransitions();
            for(int i=0, ilen=nodeups.size(); i<ilen; i++){
                BPMAbstractNode tempParent = ((BPMTransition)nodeups.get(i)).getFrom();
                if(tempParent instanceof BPMHumenActivity){
                    results.add(tempParent);
                } else if(tempParent instanceof BPMStart){
                    results.add(tempParent);
                } else if(tempParent instanceof BPMAndRouter){
                    results.addAll(findAllParentNodes(tempParent));
                }
            }
        }
        return results;
    }

    /**
     * 找到指定节点前面相邻的所有人工节点（不包括发起者节点）
     * @param node 指定的节点
     * @return 前面相邻的所有节点
     */
    public static List<BPMHumenActivity> findAllParentHumenActivitys(BPMAbstractNode node){
        List<BPMHumenActivity> results = new ArrayList<BPMHumenActivity>();
        if(node!=null){
            @SuppressWarnings("rawtypes")
            List nodeups = node.getUpTransitions();
            for(int i=0, ilen=nodeups.size(); i<ilen; i++){
                BPMAbstractNode tempParent = ((BPMTransition)nodeups.get(i)).getFrom();
                if(tempParent instanceof BPMHumenActivity){
                    results.add((BPMHumenActivity)tempParent);
                } else if(tempParent instanceof BPMAndRouter){
                    results.addAll(findAllParentHumenActivitys(tempParent));
                }
            }
        }
        return results;
    }
    
    /**
     * 寻到指定节点的所有祖先节点（不包括发起者节点）
     * @param node
     * @return
     */
    public static List<BPMHumenActivity> findAllAncestorHumenActivitys(BPMAbstractNode node){
        List<BPMHumenActivity> results = null;
        if(node!=null){
            Map<String, BPMHumenActivity> nodeMap = new HashMap<String, BPMHumenActivity>();
            @SuppressWarnings("rawtypes")
            List nodeups = node.getUpTransitions();
            for(int i=0, ilen=nodeups.size(); i<ilen; i++){
                BPMAbstractNode tempParent = ((BPMTransition)nodeups.get(i)).getFrom();
                if(tempParent instanceof BPMHumenActivity){
                    nodeMap.put(tempParent.getId(), (BPMHumenActivity)tempParent);
                    List<BPMHumenActivity> tempList = findAllAncestorHumenActivitys(tempParent);
                    if(tempList!=null && tempList.size()>0){
                        for(BPMHumenActivity tn : tempList){
                            nodeMap.put(tn.getId(), tn);
                        }
                    }
                } else if(tempParent instanceof BPMAndRouter){
                    List<BPMHumenActivity> tempList = findAllAncestorHumenActivitys(tempParent);
                    if(tempList!=null && tempList.size()>0){
                        for(BPMHumenActivity tn : tempList){
                            nodeMap.put(tn.getId(), tn);
                        }
                    }
                } else if(tempParent instanceof BPMStart){
                    return results;
                }
            }
            if(nodeMap.size()>0){
                results = new ArrayList<BPMHumenActivity>();
                for(Map.Entry<String, BPMHumenActivity> entry : nodeMap.entrySet()){
                    results.add(entry.getValue());
                }
            }
        }
        return results;
    }
    
    /**
     * 寻到指定节点的所有后代节点（不包括结束节点）
     * @param node
     * @return
     */
    public static List<BPMHumenActivity> findAllChildHumenActivitys(BPMAbstractNode node){
        List<BPMHumenActivity> results = null;
        if(node!=null){
            Map<String, BPMHumenActivity> nodeMap = new HashMap<String, BPMHumenActivity>();
            @SuppressWarnings("rawtypes")
            List nodeups = node.getDownTransitions();
            for(int i=0, ilen=nodeups.size(); i<ilen; i++){
                BPMAbstractNode tempParent = ((BPMTransition)nodeups.get(i)).getTo();
                if(tempParent instanceof BPMHumenActivity){
                    nodeMap.put(tempParent.getId(), (BPMHumenActivity)tempParent);
                    List<BPMHumenActivity> tempList = findAllChildHumenActivitys(tempParent);
                    if(tempList!=null && tempList.size()>0){
                        for(BPMHumenActivity tn : tempList){
                            nodeMap.put(tn.getId(), tn);
                        }
                    }
                } else if(tempParent instanceof BPMAndRouter){
                    List<BPMHumenActivity> tempList = findAllChildHumenActivitys(tempParent);
                    if(tempList!=null && tempList.size()>0){
                        for(BPMHumenActivity tn : tempList){
                            nodeMap.put(tn.getId(), tn);
                        }
                    }
                } else if(tempParent instanceof BPMEnd){
                    return results;
                }
            }
            if(nodeMap.size()>0){
                results = new ArrayList<BPMHumenActivity>();
                for(Map.Entry<String, BPMHumenActivity> entry : nodeMap.entrySet()){
                    results.add(entry.getValue());
                }
            }
        }
        return results;
    }
    
    public static List<SubProcessSetting> createSubSettingFromStringArray(String process_subsetting){
        List<SubProcessSetting> subSettingList = null;
        Object obj = JSONUtil.parseJSONString(process_subsetting);
        if(obj instanceof Map){
            Map map = (Map)obj;
            if(map.size()>0){
                subSettingList = new ArrayList<SubProcessSetting>();
                for(Object entryObj : map.entrySet()){
                    Map.Entry entry = (Map.Entry)entryObj;
                    if(entry.getValue() instanceof List){
                        List list = (List)entry.getValue();
                        for(Object object : list){
                            SubProcessSetting setting = createSubSettingFromString(object);
                            if(setting!=null){
                                subSettingList.add(setting);
                            }
                        }
                    } else {
                        SubProcessSetting setting = createSubSettingFromString(entry.getValue());
                        if(setting!=null){
                            subSettingList.add(setting);
                        }
                    }
                }
            }
        }
        return subSettingList;
    }
    
    public static String removeNFFlag(String processXml) {
        String newProcessXML = processXml;
        if(newProcessXML!=null){
            BPMProcess process = BPMProcess.fromXML(newProcessXML);
            List<BPMHumenActivity> nfNodeList = getAllNFList(process);
            if(nfNodeList!=null && nfNodeList.size()>0){
                for(BPMHumenActivity node : nfNodeList){
                    node.getSeeyonPolicy().setNF("0");
                }
                newProcessXML = process.toXML(null,true);
            }
        }
        return newProcessXML;
    }
    
    public static List<BPMHumenActivity> getAllNFList(BPMProcess process){
        List<BPMHumenActivity> result = null;
        if(process!=null){
            result = new ArrayList<BPMHumenActivity>();
            List<BPMAbstractNode> allNodes = process.getActivitiesList();//findAllChildHumenActivitys(process.getStart());
            if(allNodes!=null && allNodes.size()>0){
                for(BPMAbstractNode node : allNodes){
                    if(node.getNodeType().equals(BPMAbstractNode.NodeType.humen)){
                        if("1".equals(node.getSeeyonPolicy().getNF())){
                            result.add((BPMHumenActivity)node);
                        }
                    }
                }
            }
        }
        return result;
    }
    
    public static TemplateIEMessageVO templateToVO(ProcessTemplete template){
        TemplateIEMessageVO vo = new TemplateIEMessageVO();
        if(template!=null){
            vo = new TemplateIEMessageVO();
            vo.setTemplateId(String.valueOf(template.getId()));
            vo.setWorkflow(template.getWorkflow());
        }
        return vo;
    }
    
    public static List<TemplateIEMessageVO> templateListToVOList(List<ProcessTemplete> templeteList){
        List<TemplateIEMessageVO> voList = null;
        if(templeteList!=null && templeteList.size()>0){
            voList = new ArrayList<TemplateIEMessageVO>();
            for(ProcessTemplete templete : templeteList){
                TemplateIEMessageVO vo = templateToVO(templete);
                if(vo!=null){
                    voList.add(vo);
                }
            }
        }
        return voList;
    }
    
    /**
     * 直接使用 valueOf会导致 null 问题
     * @param o
     * @return
     */
    private static String transObj2Str(Object o){
        String ret = null;
        if(o != null){
            ret = String.valueOf(o);
        }
        return ret;
    }
    
    private static SubProcessSetting createSubSettingFromString(Object object){
        SubProcessSetting result = null;
        if(object!=null){
            if(object instanceof Map){
                Map map = (Map)object;
                result = new SubProcessSetting();
                result.setNodeId(transObj2Str(map.get("nodeId")));
                result.setNewflowTempleteId(Long.parseLong(transObj2Str(map.get("newflowTempleteId"))));
                result.setNewflowSender(transObj2Str(map.get("newflowSender")));
                result.setTriggerCondition(transObj2Str(map.get("triggerCondition")));
                result.setConditionTitle(transObj2Str(map.get("conditionTitle")));
                result.setConditionBase(transObj2Str(map.get("conditionBase")));
                result.setIsForce("true".equals(transObj2Str(map.get("isForce")))?true:false);
                result.setFlowRelateType(Integer.parseInt(transObj2Str(map.get("flowRelateType"))));
                result.setIsCanViewMainFlow("true".equals(transObj2Str(map.get("isCanViewMainFlow")))?true:false);
                result.setIsCanViewByMainFlow("true".equals(transObj2Str(map.get("isCanViewByMainFlow")))?true:false);
                result.setSubject(transObj2Str(map.get("subject")));
            } else {
                String string = object.toString();
                if(string!=null && string.trim().length()>0){
                    String[] attrs = string.split("@");
                    if(attrs!=null && attrs.length>0){
                        result = new SubProcessSetting();
                        result.setNodeId(attrs[0]);
                        result.setNewflowTempleteId(Long.parseLong(attrs[1]));
                        result.setNewflowSender(attrs[2]);
                        result.setTriggerCondition(attrs[3]);
                        result.setConditionTitle(attrs[4]);
                        result.setConditionBase(attrs[5]);
                        result.setIsForce("true".equals(attrs[6])?true:false);
                        result.setFlowRelateType(Integer.parseInt(attrs[7]));
                        result.setIsCanViewMainFlow("true".equals(attrs[8])?true:false);
                        result.setIsCanViewByMainFlow("true".equals(attrs[9])?true:false);
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * 判断节点权限是否是审核节点。
     * @param policyId
     * @return
     */
    public static boolean isVerifyPolicy(String policyId){
        boolean result = false;
        if(WorkFlowFinal.CANOT_REPEAL_POLICY_IDS!=null && WorkFlowFinal.CANOT_REPEAL_POLICY_IDS.length>0){
            for(String temp : WorkFlowFinal.CANOT_REPEAL_POLICY_IDS){
                if(temp.equals(policyId)){
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 检测当前节点的前节点是否有新流程
     * @param activity
     * @return 有新流程的节点Ids
     * @throws BPMException
     */
    public static List<String> checkPrevNodeHasNewflow(BPMActivity activity,BPMCase theCase)throws BPMException {
        List<String> result = new ArrayList<String>();
        Set<String> passedNodeMap= new HashSet<String>();
        if(activity != null){
            List upTransitions = activity.getUpTransitions();
            checkPrevNodeHasNewflowHelper(upTransitions, result,theCase,passedNodeMap);
        }
        return result;
    }

    /**
     * 
     * @param upTransitions
     * @param result
     * @throws BPMException
     */
    private static void checkPrevNodeHasNewflowHelper(List upTransitions, List<String> result,BPMCase theCase,Set<String> passedNodeMap)throws BPMException{
        if (upTransitions != null){
            for (int i = 0; i < upTransitions.size(); i++){
                BPMTransition trans = (BPMTransition) upTransitions.get(i);
                BPMAbstractNode from = trans.getFrom();
                if(passedNodeMap.contains(from.getId())){
                	continue;
                }
                passedNodeMap.add(from.getId());
                String isDelete= getNodeConditionFromCase(theCase,from,"isDelete");
                if("false".equals(isDelete)){//没有被delete
                    if(from.getNodeType().equals(BPMAbstractNode.NodeType.start)){
                        return;
                    }else if(from.getNodeType().equals(BPMAbstractNode.NodeType.split) || from.getNodeType().equals(BPMAbstractNode.NodeType.join)){
                        checkPrevNodeHasNewflowHelper(from.getUpTransitions(), result,theCase,passedNodeMap);
                    }else if(from.getNodeType().equals(BPMAbstractNode.NodeType.humen)){
                        if(from.getSeeyonPolicy().getId().equals(BPMSeeyonPolicy.SEEYON_POLICY_INFORM.getId())
                                || from.getSeeyonPolicy().getId().equals(BPMSeeyonPolicy.EDOC_POLICY_ZHIHUI.getId())){//知会节点
                            checkPrevNodeHasNewflowHelper(from.getUpTransitions(), result,theCase,passedNodeMap);
                        }else{//非知会节点
                            if("1".equals(from.getSeeyonPolicy().getNF())){
                                result.add(from.getId());
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 获得指定节点和key的BPMSeeyonPolicy值，key目前支持isDelete和isPass两个
     * @param theCase
     * @param nodeId
     * @param key
     * @param node
     * @return
     */
    public static String getNodeConditionFromCase(BPMCase theCase, BPMAbstractNode node,String key) {
        String isDelete= "";
        String nodeId= node.getId();
        Object result= null;
        if(null!=theCase){
            result= theCase.getData(ActionRunner.WF_NODE_CONDITION_CHANGE_KEY);
        }
        Map<String, Map<String,String>> myMap= result==null?new HashMap<String, Map<String,String>>():(Map<String, Map<String,String>>)result;
        if(myMap.get(nodeId)!=null){
            if(myMap.get(nodeId).get(key)!=null){
                isDelete= myMap.get(nodeId).get(key);
            }
        }
        if(Strings.isBlank(isDelete) && "isDelete".equals(key)){
            isDelete= node.getSeeyonPolicy().getIsDelete();
        }
        if(Strings.isBlank(isDelete) && "isPass".equals(key)){
            isDelete= node.getSeeyonPolicy().getIsPass();
        }
        return isDelete;
    }
    
    public static String getNodeConditionFromContext(WorkflowBpmContext context, BPMAbstractNode node,String key) {
        String isDelete= "";
        String nodeId= node.getId();
        Map<String, Map<String,String>> myMap= context.getNodeConditionChangeInfoMap();
        if(myMap.get(nodeId)!=null){
            if(myMap.get(nodeId).get(key)!=null){
                isDelete= myMap.get(nodeId).get(key);
            }
        }
        if(Strings.isBlank(isDelete) && "isDelete".equals(key)){
            isDelete= node.getSeeyonPolicy().getIsDelete();
        }
        if(Strings.isBlank(isDelete) && "isPass".equals(key)){
            isDelete= node.getSeeyonPolicy().getIsPass();
        }
        return isDelete;
    }
    
    
    public static void putNodeConditionToContext(WorkflowBpmContext context,BPMAbstractNode toNode,String key,String value) {
        Map<String,String> myMap= context.getNodeConditionChangeInfoMap().get(toNode.getId());
        if(null== myMap){
            myMap= new HashMap<String, String>();
        }
        myMap.put(key, value);
        context.getNodeConditionChangeInfoMap().put(toNode.getId(), myMap);
    }
    
    /**
     * 将addition或raddition放入到context中去
     * @param context
     * @param nodeId
     * @param party
     * @param key
     * @param value
     */
    public static void putNodeAdditionToContext(WorkflowBpmContext context,String nodeId,BPMParticipant party,String key,String value){
        if("addition".equals(key)){
            context.getNodeAdditionMap().put(nodeId, value);
        }
        if("raddition".equals(key)){
            context.getNodeRAdditionMap().put(nodeId, value);
        }
    }
    
    public static String getNodeAdditionFromContext(WorkflowBpmContext context,String nodeId,BPMParticipant party,String key){
        String addition= "";
        if("addition".equals(key)){
            addition = context.getNodeAdditionMap().get(nodeId);
            if(Strings.isBlank(addition)){
                addition = party.getAddition();
            }
        }
        if("raddition".equals(key)){
            addition = context.getNodeRAdditionMap().get(nodeId);
            if(Strings.isBlank(addition)){
                addition = party.getRaddition();
            }
        }
        return addition;
    }
    
    public static String getNodeAdditionFromCase(BPMCase theCase,String nodeId,BPMParticipant party,String key){
        String addition= "";
        Object result= null;
        if("addition".equals(key)){
            if(null!=theCase){
                result= theCase.getData(ActionRunner.WF_NODE_ADDITION_KEY);
            }
            Map<String,String> myMap= result==null?new HashMap<String, String>():(Map<String,String>)result;
            addition = myMap.get(nodeId);
            if(Strings.isBlank(addition)){
                addition = party.getAddition();
            }
        }
        if("raddition".equals(key)){
            if(null!=theCase){
                result= theCase.getData(ActionRunner.WF_NODE_RADDITION_KEY);
            }
            Map<String,String> myMap= result==null?new HashMap<String, String>():(Map<String,String>)result;
            addition = myMap.get(nodeId);
            if(Strings.isBlank(addition)){
                addition = party.getRaddition();
            }
        }
        return addition;
    }
    
    /**
     * 是否需要流程重走
     * @param context
     * @param theCase
     * @param currentActivity
     * @return
     */
    public static boolean isRego(WorkflowBpmContext context,BPMCase theCase,BPMAbstractNode currentActivity){
    	//P2和End之前所有没有删除的linkId
        logger.info(AppContext.currentUserName()+"in isRego 判断开始_______");
        Set<String> p22endLinkIdsOld= new HashSet<String>();
        Set<String> p22endLinkIdsNew= new HashSet<String>();
        WorkflowUtil.processAllChildLink(context, theCase, p22endLinkIdsOld, p22endLinkIdsNew, currentActivity);
        if(p22endLinkIdsOld.equals(p22endLinkIdsNew)){//直接提交到P1
            logger.info("计算结果：false，不用rego,p22endLinkIdsOld:"+p22endLinkIdsOld+",p22endLinkIdsNew:"+p22endLinkIdsNew);
        	return false;
        }else{
            logIsRego(context.getProcess(),currentActivity,p22endLinkIdsOld,p22endLinkIdsNew);
        	return true;
        }
    }
    
    /**
     * @param p22endLinkIdsOld
     * @param p22endLinkIdsNew
     * @param addlinks
     * @param addlinks2
     */
    private static void logIsRego(BPMProcess process ,BPMAbstractNode currentActivity,Set<String> p22endLinkIdsOld, Set<String> p22endLinkIdsNew) {
    
        StringBuilder  info = new StringBuilder();
        List<String> addlinks = new ArrayList<String>();
        List<String> deletelinks = new ArrayList<String>();

        logger.info("计算结果：true,p22endLinkIdsOld:"+p22endLinkIdsOld+",p22endLinkIdsNew:"+p22endLinkIdsNew);
        
        if(Strings.isNotEmpty(p22endLinkIdsOld) && Strings.isNotEmpty(p22endLinkIdsNew)){
            for(String oldId : p22endLinkIdsOld){
                if(!p22endLinkIdsNew.contains(oldId)){
                    deletelinks.add(oldId);
                }
            }
            
            for(String newId :p22endLinkIdsNew){
                if(!p22endLinkIdsOld.contains(newId)){
                    addlinks.add(newId);
                }
            }
            
           if(Strings.isNotEmpty(addlinks) || Strings.isNotEmpty(deletelinks)){
               info.append("导致流转模式变成流程重走 ,用户"+AppContext.currentUserName()+"["+currentActivity.getBPMAbstractNodeName()+"("+currentActivity.getId()+")]发送/处理流程的时候，估算流转路径：");
               if(Strings.isNotEmpty(addlinks)){
                   info.append("新增了");
               }
               else if(Strings.isNotEmpty(deletelinks)){
                   info.append("删除了");
               }
           }
            
           if(Strings.isNotEmpty(addlinks)){
              log(process, info, addlinks);
              
           }
           if(Strings.isNotEmpty(deletelinks)){
               log(process, info, deletelinks);
            }

           logger.info("p22endLinkIdsOld:"+p22endLinkIdsOld.toString()+",p22endLinkIdsNew:"+p22endLinkIdsNew.toString());
        }
        
    }

    private static void log(BPMProcess process, StringBuilder info, List<String> addlinks) {
        String id  = addlinks.get(0);
          BPMTransition link  = process.getLinkById(id);
          BPMAbstractNode from = link.getFrom();
          BPMAbstractNode to = link.getTo();
          String fromName = from.getBPMAbstractNodeName();
          String toName = to.getBPMAbstractNodeName();
          
          
          info.append("节点[").append(fromName).append("(").append(from.getId()).append(")]").append(" 至 节点 [" )
          .append(toName).append("(").append(to.getId()).append(")]之间的流转路径(").append(link.getId()).append(")");
          
          info.append("路径的分支条件为：").append(link.getConditionTitle()).append(",").append(link.getFormCondition());
          logger.info(info);
    }

    /**
     * 处理node节点之后的分支条件
     * @param context
     * @param theCase
     * @param p22endLinkIdsOld
     * @param p22endLinkIdsNew
     * @param node
     */
    public static void processAllChildLink(WorkflowBpmContext context,BPMCase theCase,Set<String> p22endLinkIdsOld,Set<String> p22endLinkIdsNew,
    		BPMAbstractNode node){
    	Map<String,String> passedNodes= new HashMap<String, String>();
    	processAllChildLink(context, theCase,p22endLinkIdsOld, p22endLinkIdsNew, node,passedNodes);
    }
    public static Map<String,String> getConditionMapFromCase(BPMCase theCase,String nodeId){
        Object result= null;
        if(null!=theCase){
            result= theCase.getData(ActionRunner.WF_NODE_CONDITION_CHANGE_KEY);
        }
        Map<String,Map<String,String>> map= result==null?new HashMap<String, Map<String,String>>():(Map<String, Map<String,String>>)result;
        if(null!=map.get(nodeId)){
            return map.get(nodeId);
        }
        return new HashMap<String, String>();
    }
    
    /**
     * 判断节点是否激活状态
     */
    private static boolean checkNodePathPassed(BPMAbstractNode node,BPMCase theCase){
        boolean isNodePassed= true;
        
        if(node.getNodeType().equals(BPMAbstractNode.NodeType.humen)){
            isNodePassed= isThisState(theCase, node.getId(), 
                    CaseDetailLog.STATE_READY,
                    CaseDetailLog.STATE_ZCDB,
                    CaseDetailLog.STATE_FINISHED,
                    CaseDetailLog.STATE_NEEDREDO_TOME,
                    CaseDetailLog.STATE_SUSPENDED);
          
            StringBuilder sb  = new StringBuilder();
            sb.append("nodeName:").append(node.getName()).append(",NodeId:").append(node.getId()).append(",isNodePassed:").append(isNodePassed);
            
            if(!isNodePassed){
                Map<String,String> conditionMap = getConditionMapFromCase(theCase,node.getId());
                isNodePassed = "true".equals(conditionMap.get("isDelete")); //删除状态表示这个路径也是走过的。
             
                sb.append(",isDel:"+isNodePassed+",theCaseId:"+theCase.getId());
            }
            
            logger.info(sb.toString());
        }
        else if(isBlankNode(node) || node.getNodeType().equals(BPMAbstractNode.NodeType.split)
               || node.getNodeType().equals(BPMAbstractNode.NodeType.join)){
            List<BPMTransition> downList = node.getDownTransitions();
            if(downList!=null && downList.size()>0){
                for(BPMTransition link : downList){
                   return checkNodePathPassed(link.getTo(),theCase);
                }
            }
        }
        return isNodePassed;
    }
    
    
    /**
     * 递归方法
     * @param context
     * @param theCase
     * @param p22endLinkIdsOld
     * @param p22endLinkIdsNew
     * @param node
     * @param passedNodes
     * @return
     */
    private static void processAllChildLink(WorkflowBpmContext context,BPMCase theCase,Set<String> p22endLinkIdsOld,
    		Set<String> p22endLinkIdsNew,BPMAbstractNode node,Map<String,String> passedNodes){
        if(node!=null){
        	if(passedNodes.containsKey(node.getId())){
        		return ;
        	}
        	passedNodes.put(node.getId(), node.getId());
        	if(node instanceof BPMEnd){
                return ;
            }else{
                List<BPMTransition> downList = node.getDownTransitions();
                Map<String, BPMTransition> linkMap = new HashMap<String, BPMTransition>();
                //将当前节点的所有down线中具备分支条件的link放入Map中
                if(downList!=null && downList.size()>0){
                    for(BPMTransition link : downList){
                        if(null==link){
                            continue;
                        }
                        String toNodeId = link.getTo().getId();
                        Map<String,String> conditionMap = getConditionMapFromCase(theCase,toNodeId);
                        boolean isNodeDeleted = "true".equals(conditionMap.get("isDelete"));

                        boolean hasPassed = checkNodePathPassed(link.getTo(),theCase);
                        
                        logger.info("isNodeDeleted:"+isNodeDeleted+",hasspassed:"+hasPassed+"，节点名："+link.getTo().getName()+",id:"+link.getTo().getId());
                        
                        if(!isNodeDeleted && hasPassed){
	                        boolean result1= caculateLink(context, p22endLinkIdsOld, p22endLinkIdsNew, link);
	                        if(result1){
		                        //将子节点的所有down线中具备分支条件的link放入Map中，进行递归
		                        processAllChildLink(context, theCase, p22endLinkIdsOld, p22endLinkIdsNew, link.getTo(),passedNodes);
	                        }
                        }else if(isNodeDeleted){
                        	Set<String> p22endLinkIdsNewTemp= new HashSet<String>();
                        	
                        	int conditionType = link.getConditionType();
                        	//强制自动分支
                        	boolean isAutoConditionForce =  ConditionType.isAutoConditionType(conditionType) && "1".equals(link.getIsForce()) ;
                        	if(conditionType == ConditionType.noCondition.key()){
                        	    processAllChildLink(context, theCase, null, p22endLinkIdsNew, link.getTo(),passedNodes);
                        	}
                        	else if(isAutoConditionForce){
                        	    boolean result1= caculateLink(context, null, p22endLinkIdsNewTemp, link);
                        	    if(result1){
                        	        if(!p22endLinkIdsNewTemp.isEmpty()){
                                        p22endLinkIdsNew.addAll(p22endLinkIdsNewTemp);
                                        return;
                                    }
                        	    }
                        	}
                        }
                    }
                }
            }
        }
    }
    
    private static boolean caculateLink(WorkflowBpmContext context, Set<String> p22endLinkIdsOld, Set<String> p22endLinkIdsNew, BPMTransition link) {
        boolean result = true;
        String formCondition = link.getFormCondition();
        int conditionType = link.getConditionType();
        String conditionBase = link.getConditionBase();
        String isForce = link.getIsForce();
        //有分支的情况才计算，无分支的情况不计算。
        
        if(conditionType == ConditionType.handCondition.key() || ConditionType.isAutoConditionType(conditionType)){
            if (null != p22endLinkIdsOld) {
                p22endLinkIdsOld.add(link.getId());
            }
        }

        if (ConditionType.isAutoConditionType(conditionType)) {// 自动分支
            isForce = (isForce == null || "null".equals(isForce.trim()) || "".equals(isForce.trim()) || "undefined".equals(isForce.trim())) ? "0" : isForce;
            if (!"1".equals(isForce)) {// 非强制分支条件
                if (null != p22endLinkIdsOld && p22endLinkIdsOld.contains(link.getId())) {
                    p22endLinkIdsNew.add(link.getId());
                }
            }
            else {
                String formConditionAfterReplace = formCondition;
                String logConditionReplaceTitle = "";
               // if ("currentNode".equals(conditionBase)) {
                boolean isTrue = false;
                if (null != p22endLinkIdsOld && p22endLinkIdsOld.contains(link.getId())) {
                    isTrue = true;
                }
                formConditionAfterReplace = formCondition.replaceAll(
                        "(isAccount|isNotAccount|isDep|isNotDep|isPost|isNotPost|isNotRole|isRole|isNotLoginAccount|isLoginAccount|isLevel|compareLevel|isNotLevel)\\s?\\([^()]*?\\)",
                        String.valueOf(isTrue));
                logConditionReplaceTitle += AppContext.currentUserName()+"formCondition :"+formCondition+",formConditionAfterReplace:"+formConditionAfterReplace;
                
                formConditionAfterReplace = formConditionAfterReplace.replaceAll("(include\\s?\\('Team'|exclude\\s?\\('Team')[^()]*?\\)", String.valueOf(isTrue));
                logConditionReplaceTitle += ",formConditionAfterReplace2:"+formConditionAfterReplace;
               // }
                result = ActionRunner.getConditionValue(context, formConditionAfterReplace, conditionBase);
               
                logger.info("caculateLink_____:"+result+",link.id:"+link.getId()+",toNodeId:"+link.getTo().getId()+",conditionReplaceTitle:"+logConditionReplaceTitle);
                
                if (result) {// 非强制分支
                    p22endLinkIdsNew.add(link.getId());
                }
            }
        }
        else if (conditionType == ConditionType.handCondition.key()) {// 手动分支
            if (null != p22endLinkIdsOld && p22endLinkIdsOld.contains(link.getId())) {
                p22endLinkIdsNew.add(link.getId());
            }
        }
        return result;
    }
    
    /**
     * 得到activiey的子节点,如果不是人工节点递归读取;
     *
     * @param activity
     * @return
     */
    public static List<BPMHumenActivity> getChildHumens(BPMActivity activity) {
        List<BPMHumenActivity> humenList = getChildHumens(activity, true);
        return humenList;
    }
    
    /**
     * 得到activiey的子节点,如果不是人工节点递归读取;
     *
     * @param activity
     * @return
     */
    public static List<BPMHumenActivity> getChildHumens(BPMActivity activity,boolean isPassInformNode) {
        List<BPMHumenActivity> humenList = new UniqueList<BPMHumenActivity>();
        List<BPMTransition> transitions = activity.getDownTransitions();
        for (BPMTransition trans : transitions) {
            BPMAbstractNode child = trans.getTo();
            String policy = child.getSeeyonPolicy().getId();

            if (child.getNodeType() == BPMAbstractNode.NodeType.humen && ("inform".equals(policy) || "zhihui".equals(policy))) {
                if(isPassInformNode){
                    humenList.addAll(getChildHumens((BPMActivity) child,isPassInformNode));
                }else{
                    humenList.add((BPMHumenActivity) child);
                }
            } else if (child.getNodeType() == BPMAbstractNode.NodeType.humen) {
                humenList.add((BPMHumenActivity) child);
            } else if (child.getNodeType() == BPMAbstractNode.NodeType.join
                    || child.getNodeType() == BPMAbstractNode.NodeType.split) {
                humenList.addAll(getChildHumens((BPMActivity) child,isPassInformNode));
            }
        }
        return humenList;
    }
    
    /**
     * 前方是否存在发散节点，如果存在，那么不允许设置手工分支可选数
     * @param node
     * @return
     */
    public static boolean preIsSplitNode(BPMAbstractNode node){
    	boolean result = false;
    	if(node!=null){
    	    List<BPMTransition> upLinks = (List<BPMTransition>)node.getUpTransitions();
            if(upLinks!=null && upLinks.size()>0){
                for(int i=0;i<upLinks.size();i++){
                    BPMAbstractNode rup = upLinks.get(i).getFrom();
                    if(rup instanceof BPMHumenActivity){
                        if(ObjectName.isInformObject(rup)){
                            boolean result1= preIsSplitNode(rup);
                            if(result1){
                                result= true;
                                break;
                            }
                        }
                    }else if(rup instanceof BPMAndRouter){
                        BPMAndRouter joinOrSplit = (BPMAndRouter)rup;
                        if(joinOrSplit.isStartAnd()){
                            result = true;
                            break;
                        }else{
                            boolean result1= preIsSplitNode(rup);
                            if(result1){
                                result= true;
                                break;
                            }
                        }
                    }
                }
            }
    	}
    	return result;
    }
    
    
    
    
    /**
     * 找到当前节点后面的第一个split节点的hst（手动选择最大分支数量）属性的值
     * 如果当前节点后面的第一个节点是人员节点或结束节点，返回-1；
     * @param node
     * @return
     */
    public static String getFirstNextSplitNode(BPMAbstractNode node){
    	String hst = "-1";
    	if(node!=null){
    		BPMAbstractNode finalNode = getFirstNextSplitNodeNode(node);
    		if(finalNode!=null){
    			hst = finalNode.getSeeyonPolicy().getHst();
    		}
    	}
    	return hst;
    }
    
    public static BPMAbstractNode getFirstNextSplitNodeNode(BPMAbstractNode node){
    	boolean flag = true;
		BPMAbstractNode next = node;
		while(flag){
		    
			List<BPMTransition> downLinks = (List<BPMTransition>)next.getDownTransitions();
			//防止意外
			if(downLinks==null || downLinks.size()==0){
				break;
			}
			BPMAbstractNode rnext = downLinks.get(0).getTo();
			
			if(rnext == null){
			    logger.error("getFirstNextSplitNodeNode 流程图错误，没找到下节点" + node.getId());
			    break;
			}
			
			//下一节点是结束节点，或者人工节点，退出循环
			if(rnext instanceof BPMEnd){
				break;
			}else if(rnext instanceof BPMHumenActivity){
				BPMHumenActivity current = (BPMHumenActivity)rnext;
				String policy = current.getSeeyonPolicy().getId();
				//如果下一节点是知会节点或者空节点，那么继续找
				if( isInformNode(rnext) || isBlankNode(rnext) ){
					next = rnext;
				}else{
					break;
				}
			}else if(rnext instanceof BPMAndRouter){
				BPMAndRouter joinOrSplit = (BPMAndRouter)rnext;
				//如果是发散节点，需要更仔细的寻找，需要判断发散节点后面的每一个节点是否都是知会或者空节点
				if(joinOrSplit.isStartAnd()){
					if("-1".equals(joinOrSplit.getSeeyonPolicy().getHst())){
						List<BPMTransition> joinDowns = (List<BPMTransition>)joinOrSplit.getDownTransitions();
						
						//现状，两个split节点之间， 第一个split的join节点和第二个join节点之间没有人员节点并且不包括知会等节点，前端是不让设置的
						
						//这里面只需要递归寻找下一个发散节点，如果多个down线找到的都是一个发散节点，表示找到的都是知会或者空节点。
						BPMAbstractNode lnext = joinDowns.get(0).getTo();
						if(isInformNode(lnext) || isBlankNode(lnext)){
                            //知会节点才穿透
                            next = getFirstNextSplitNodeNode(lnext);
                        }else{
                            next = rnext;
                        }
					}else{
						next = rnext;
					}
					break;
				}else{
					//如果是汇聚节点，那么继续寻找
					next = rnext;
				}
			}
		}
    	BPMAbstractNode result = next;
		return result;
    }
    
    /**
     * 得到activiey的子节点,如果不是人工节点递归读取;
     *
     * @param activity
     * @return
     */
    public static List<BPMHumenActivity> getChildHumensWithoutDelete(BPMActivity activity,boolean isPassInformNode,BPMCase theCase) {
        List<BPMHumenActivity> humenList = new UniqueList<BPMHumenActivity>();
        List<BPMTransition> transitions = activity.getDownTransitions();
        for (BPMTransition trans : transitions) {
            BPMAbstractNode child = trans.getTo();
            BPMSeeyonPolicy seeyonPolicy= child.getSeeyonPolicy();
            String policy = seeyonPolicy.getId();
            String isDelete= getNodeConditionFromCase(theCase, child, "isDelete");
            if(Strings.isNotBlank(isDelete) && "true".equals(isDelete.trim())){
                continue;
            }
            if (child.getNodeType() == BPMAbstractNode.NodeType.humen && ("inform".equals(policy) || "zhihui".equals(policy))) {
                if(isPassInformNode){
                    humenList.addAll(getChildHumens((BPMActivity) child));
                }else{
                    humenList.add((BPMHumenActivity) child);
                }
            } else if (child.getNodeType() == BPMAbstractNode.NodeType.humen) {
                humenList.add((BPMHumenActivity) child);
            } else if (child.getNodeType() == BPMAbstractNode.NodeType.join
                    || child.getNodeType() == BPMAbstractNode.NodeType.split) {
                humenList.addAll(getChildHumens((BPMActivity) child,isPassInformNode));
            }
        }
        return humenList;
    }
    
    public static List<BPMHumenActivity> findDirectHumenChildrenXNDX(
            BPMAbstractNode current_node,
            Map<String,String> condtionResult,
            Map<String,String> nodeTypes) {
        //人工活动节点列表
        List<BPMHumenActivity> result = new ArrayList<BPMHumenActivity>();
        //当前节点对应的分支条件
        String upCondition = condtionResult.get(current_node.getId());
        String preForce = null;
        if(upCondition != null && upCondition.endsWith("↗1")) {
            upCondition = upCondition.substring(0,upCondition.indexOf("↗1"));
            preForce = "1";
        }
        String currentIsStart= "false";
        //判断当前节点是否为发起节点
        if("start".equals(current_node.getId())){
            currentIsStart= "true";
        }
        //获得当前节点的所有down线
        List<BPMTransition> down_links = current_node.getDownTransitions();
        //循环遍历每条down线
        for (BPMTransition down_link : down_links) {
            //获得down线的目的节点toNode
            BPMAbstractNode toNode = down_link.getTo();
            //判断该down线是否为强制条件
            String isForce = preForce==null?down_link.getIsForce():preForce;
            //获得down线上的条件表达式
            String currentCondition = down_link.getFormCondition();
            //获得down线上的条件的参考对象：当前节点或发起者
            String conditionBase = down_link.getConditionBase();
            if(currentCondition != null){
                //如果分支条件不为空，则作如下处理
                currentCondition =
                    currentCondition.replaceAll("isNotRole", "isnotrole").
                    replaceAll("isRole", "isrole").replaceAll("isPost", "ispost").
                    replaceAll("isNotPost", "isNotpost");
            }
            if("start".equals(conditionBase) && !"true".equals(currentIsStart)){
                //如果分支条件是基于发起者start，且当前处理节点不是发起节点，则作如下处理
                currentCondition=
                    currentCondition.replace("[Level]", "[startlevel]").replace("[Account]", "[startaccount]")
                    .replace("Concurrent_Acunt", "Start_ConcurrentAcunt").replace("Concurrent_Levl", "Start_ConcurrentLevl")
                    .replace("Account,Concurrent_Acunt", "startaccount,Start_ConcurrentAcunt").replace("Level,Concurrent_Levl", "startlevel,Start_ConcurrentLevl")
                    .replace("[StartMemberLoginAcuntLevl]", "[startStartMemberLoginAcuntLevl]");
                currentCondition =
                    currentCondition.replaceAll("Department", "startdepartment").
                    replaceAll("Post", "startpost").replaceAll("Level", "startlevel").
                    replaceAll("team", "startTeam").replaceAll("secondpost", "startSecondpost").
                    replaceAll("Account", "startaccount").replaceAll("standardpost", "startStandardpost").
                    replaceAll("grouplevel", "startGrouplevel").replaceAll("Role", "startrole").
                    replaceAll("ispost", "isStartpost").replaceAll("isNotpost", "isNotStartpost").
                    replaceAll("isNotDep", "isNotStartDep").replaceAll("isDep", "isStartDep");
            }
            if("1".equals(isForce)) {
                //如果分支条件为强制分支，则做如下处理?
                String str = condtionResult.get(toNode.getId());
                if(str!=null&&!"".equals(str)) {
                    String[] arr = str.split("↗");
                    if(arr!=null) {
                        switch(arr.length) {
                            case 3: {
                                isForce = null;
                                break;
                            }
                        }
                    }
                }
            }else{
                isForce = null;
            }
            //根据目的节点toNode不同类型，做不同的处理
            //获得当前节点的节点类型
            NodeType nodeType= toNode.getNodeType();
            if ( nodeType.equals(NodeType.humen) ) {//人工活动节点(humen->humen)
                BPMHumenActivity hNode = (BPMHumenActivity) toNode;
                result.add(hNode);
                //然后对指向该人工活动节点的分支条件做如下处理：
                BPMSeeyonPolicy policy = hNode.getSeeyonPolicy();
                //判断该人工活动节点对应的分支条件是否已存在
                boolean isNew = condtionResult.get(hNode.getId())==null;
                //获得人工活动节点的名称
                String nodeName = hNode.getBPMAbstractNodeName();
                if(upCondition==null||"".equals(upCondition)){
                    //如果当前处理节点对应的分支条件为空
                    if(down_link.getConditionType()==1||down_link.getConditionType()==4){
                        //如果down线为自动分支或强制分支，则建立起目的节点和该分支条件的对应关系
                        condtionResult.put(hNode.getId(), nodeName+"↗"+currentCondition);
                    }else if(down_link.getConditionType()==2){
                        //如果为手动分支，则建立起目的节点和该手动分支的对应关系
                        condtionResult.put(hNode.getId(), nodeName+"↗handCondition");
                    }
                }else if(upCondition.indexOf("↗")==-1){
                    //如果当前处理节点对应的分支条件不为空
                    if(down_link.getConditionType()==1||down_link.getConditionType()==4) {
                        //如果down线为自动分支或强制分支，则建立起目的节点和该分支条件的对应关系
                        String currentConditionValue= "";
                        if(currentCondition==null||"".equals(currentCondition)){
                            currentConditionValue= nodeName+"↗("+upCondition + ")";
                        }else{
                            currentConditionValue= nodeName+"↗("+upCondition + ") && (" + currentCondition + ")";
                        }
                        condtionResult.put(hNode.getId(), currentConditionValue);
                    }else if(down_link.getConditionType()==2) {
                        //如果为手动分支，则建立起目的节点和该手动分支的对应关系
                        String currentConditionValue= "";
                        if(currentCondition==null||"".equals(currentCondition)){
                            currentConditionValue= nodeName+"↗("+upCondition + ")";
                        }else{
                            currentConditionValue= nodeName+"↗("+upCondition + ") && (handCondition)";
                        }
                        condtionResult.put(hNode.getId(), currentConditionValue);
                    }else{
                        //如果不为条件分支，则直接从上一节点继承过来
                        condtionResult.put(hNode.getId(), nodeName+"↗"+upCondition);
                    }
                }
                if(isNew) {//如果为新发现的节点，则将节点数nodeCount加1
                    if(condtionResult.get("nodeCount")==null){
                        condtionResult.put("nodeCount", "1");
                    }else {
                        int count = Integer.parseInt(condtionResult.get("nodeCount"))+1;
                        condtionResult.put("nodeCount", String.valueOf(count));
                    }
                }
                if(policy != null) {
                    //建立起人工活动节点Id与分支条件Id之间的对应关系
                    condtionResult.put("linkTo"+hNode.getId(), down_link.getId());
                }
                //记录节点的遍历顺序
                String order = condtionResult.get("order");
                if(order==null){
                    condtionResult.put("order", hNode.getId());
                }else{
                    condtionResult.put("order", order+"$"+hNode.getId());
                }
                if(isForce!=null && condtionResult.get(hNode.getId()) != null){
                    //如果为强制分支，则在分支条件表达式后面加上强制分支标志
                    condtionResult.put(hNode.getId(), condtionResult.get(hNode.getId())+"↗"+isForce);
                }
                //如果是知会节点，则如何处理?继续递归处理，直到遇到humen（非知会）为止或遇到split为出口
                if(policy != null && ("inform".equals(policy.getId()) || "zhihui".equals(policy.getId()))) {//humen->humen(知会)
                    nodeTypes.put(hNode.getId(), "inform");
                }else{
                    nodeTypes.put(hNode.getId(), "normal");
                }
//                if(log.isInfoEnabled()){
//                  log.info("nodeName:="+nodeName);
//                }
            }else if( nodeType.equals(NodeType.split) || nodeType.equals(NodeType.join) ){//split节点(humen->split)或(humen->join)
                if(upCondition!=null && !"".equals(upCondition)){
                    //如果当前处理节点对应的分支条件不为空
                    if(down_link.getConditionType()==1 || down_link.getConditionType()==4){
                        //如果为自动分支或强制分支
                        condtionResult.put(toNode.getId(), "(" + upCondition + ")" + " && (" + currentCondition + ")");
                    }else if(down_link.getConditionType()==2){
                        //如果为手动分支
                        condtionResult.put(toNode.getId(), "(" + upCondition + ")" + " && (handCondition)");
                    }else{
                        condtionResult.put(toNode.getId(), upCondition);
                    }
                }else{
                    //如果当前处理节点对应的分支条件为空
                    if(down_link.getConditionType()==1 || down_link.getConditionType()==4){
                        //如果为自动分支或强制分支
                        String currentConditionValue= "";
                        if(currentCondition==null||"".equals(currentCondition)){
                            currentConditionValue= "";
                        }else{
                            currentConditionValue= "(" + currentCondition + ")";
                        }
                        condtionResult.put(toNode.getId(), currentConditionValue);
                    }else if(down_link.getConditionType()==2){
                        //如果为手动分支
                        condtionResult.put(toNode.getId(), "(handCondition)");
                    }else{
                        //如果没有分支条件
                        condtionResult.put(toNode.getId(), upCondition);
                    }
                }
                if(isForce!=null){
                    //如果为强制分支，则加上强制分支的标志
                    condtionResult.put(toNode.getId(), condtionResult.get(toNode.getId())+"↗"+isForce);
                }
//              if(log.isInfoEnabled()){
//                  log.info("nodeName:="+toNode.getName());
//                }
                //循环递归查找后续人工活动节点
                List<BPMHumenActivity> children = findDirectHumenChildrenXNDX(toNode,condtionResult,nodeTypes);
                result.addAll(children);
                condtionResult.remove(toNode.getId());
            }else if ( nodeType.equals(NodeType.end) ) {//结束节点
                return new ArrayList<BPMHumenActivity>(0);
            }
        }
        return result;
    }
    
    public static HashMap<String,Object> splitCondition(HashMap<String,String> hash){
		HashMap<String,Object> result = new HashMap<String,Object>();
		Set<Map.Entry<String, String>> entry = hash.entrySet();
    	List<String> keys = new ArrayList<String>();
    	List<String> nodeNames = new ArrayList<String>();
    	List<String> conditions = new ArrayList<String>();
    	List<String> forces = new ArrayList<String>();
    	List<String> links = new ArrayList<String>();
    	String[] temp = null;
    	String[] temp1 = null;
    	String order = hash.get("order");
    	if(order != null && order.indexOf("$")!=-1) {
    		temp1 = StringUtils.split(order,"$");
    	}

    	StringBuffer sb = new StringBuffer();
    	if(temp1!=null && temp1.length>0) {
    		for(String item:temp1){
    			String value = hash.get(item);
        		if(value!=null&&value.indexOf("↗")!=-1){
        			sb.append(item+":");
	        		keys.add(item);
	        		links.add(hash.get("linkTo"+item));
	        		temp = value.split("↗");
	        		if(temp != null){
	        			nodeNames.add(temp[0]);
	        			//temp[1] = temp[1].replaceAll("handCondition", "false");
	        			conditions.add(temp[1]);
	        			if(temp.length==3 && "1".equals(temp[2]))
	        				forces.add("true");
	        			else
	        				forces.add("false");
	        		}
        		}
        	}
    	}else {
        	for(Map.Entry<String, String> item:entry){
        		if(item.getValue()!=null&&item.getValue().indexOf("↗")!=-1){
        			sb.append(item.getKey()+":");
	        		keys.add(item.getKey());
	        		links.add(hash.get("linkTo"+item.getKey()));
	        		temp = item.getValue().split("↗");
	        		if(temp != null){
	        			nodeNames.add(temp[0]);
	        			//temp[1] = temp[1].replaceAll("handCondition", "false");
	        			conditions.add(temp[1]);
	        			if(temp.length==3)
	        				forces.add("true");
	        			else
	        				forces.add("false");
	        		}
        		}
        	}
    	}
    	if(keys.size() > 0 && conditions.size() >0){
    		result.put("allNodes", sb.toString());
    		result.put("keys", keys);
    		result.put("names", nodeNames);
    		result.put("conditions", conditions);
    		result.put("nodeCount", hash.get("nodeCount"));
    		result.put("forces", forces);
    		result.put("links", links);
        }
		return result;
	}
    
    /**
     * 
     * @param theCase
     * @param process
     * @param form
     * @return
     */
    public static String changeFormFieldNodeName(BPMCase theCase,BPMProcess process,String newFormApp){
        if(process==null){
            return null;
        }
        boolean hasChange = changeFormFieldNodeName(process,newFormApp);
        if(hasChange){
           String result = process.toXML(theCase,true);
           return result;
        }
        return null;
    }
    
    public static String conditionToFieldDisplay(String condition, Map<String, WorkflowFormFieldBO> fieldMap){
        String result = condition;
        Pattern pattern = Pattern.compile("(field\\d+)");
        Matcher macher = pattern.matcher(result);
        //如果分支条件中找到表单单元格的话，再去转换
        if(macher.find() && fieldMap!=null && !fieldMap.isEmpty()){
            //数据准备完毕，开始替换
            StringBuffer sb = new StringBuffer();
            do{
                String name = macher.group(1);
                WorkflowFormFieldBO field = fieldMap.get(name);
                if(field!=null){
                    macher.appendReplacement(sb, "{"+field.getDisplay()+"}");
                }else{
                    macher.appendReplacement(sb, name);
                }
            }while(macher.find());
            macher.appendTail(sb);
            result = sb.toString();
        }
        return result;
    }
    
    private static boolean changeFormFieldNodeName(BPMProcess process,String newFormApp){
    	boolean hasChange = false;
    	if(process==null){
    		return hasChange;
    	}
    	BPMStatus start = process.getStart();
    	String formApp = start.getSeeyonPolicy().getFormApp();
    	String useFormAppId= newFormApp;
        if(Strings.isBlank(newFormApp) && isLong(formApp)){
            useFormAppId = formApp;
        }
        if(Strings.isBlank(useFormAppId) || !WorkflowUtil.isLong(useFormAppId)){
        	return false;
        }
      //动态审批表字段集合
        Map<String, Map<String, WorkflowFormFieldBO>> dynamicFormMap = new HashMap<String, Map<String,WorkflowFormFieldBO>>();
    	Map<String, WorkflowFormFieldBO> fieldMap = WorkflowFormDataMapInvokeManager.getAppManager("form").getFormFieldMap(useFormAppId);
    	if(fieldMap!=null && !fieldMap.isEmpty()){
	    	List<BPMAbstractNode> allNodes = process.getActivitiesList();
	        if(allNodes!=null && allNodes.size()>0){
	            
	            WorkflowSuperNodeManager workflowSuperNodeManager = (WorkflowSuperNodeManager) AppContext.getBean("workflowSuperNodeManager");
	            
	            for(BPMAbstractNode node : allNodes){
	                if(node.getNodeType().equals(BPMAbstractNode.NodeType.humen)){
	                	List actorList = node.getActorList();
	                	BPMActor actor = (BPMActor) actorList.get(0);
	                    BPMParticipant party = actor.getParty();
	                    //actor标签的partyType属性值
	                    String partyTypeId = party.getType().id;
	                    //actor标签的partyId属性值
	                    String partyId = party.getId();
	                    //Member@field0001#合同编号#roleIdorName
	                    if(WorkFlowFinal.FORMFIELD.equals(partyTypeId) && partyId!=null){
	                    	//这里要做的替换其实就是fieldname相同，而display不同的情况，其他的表单id、节点权限id通通不修改。
	                    	String[] partyIdValue = partyId.split("[@|#]");
	                        String type = "Member";
	                        String fieldName = "";
	                        String displayName = "";
	                        String roleIdOrName = "";
	                        if(partyIdValue.length>=4){
	                            type = partyIdValue[0];
	                            fieldName = partyIdValue[1];
	                            displayName = partyIdValue[2];
	                            roleIdOrName = "#"+partyIdValue[3];
	                        }else if(partyIdValue.length>=3){
	                            type = partyIdValue[0];
	                            fieldName = partyIdValue[1];
	                            displayName = partyIdValue[2];
	                        } else if(partyIdValue.length>0){
	                            displayName = partyIdValue[0];
	                        }
	                        WorkflowFormFieldBO field = fieldMap.get(fieldName);
	                        if(field!=null && !displayName.equals(field.getDisplay())){
	                        	String newName = node.getName().replaceFirst(displayName, field.getDisplay());
	                        	party.setId(type+"@"+fieldName+"#"+field.getDisplay()+roleIdOrName);
	                        	node.setName(newName);
	                        	actor.setName(newName);
	                        	hasChange = true;
	                        }
	                    }else if("WFDynamicForm".equals(partyTypeId) && partyId != null){
                            
                            String[] dynamicFormInfo = partyId.split("[@|#]");
                            String formId = dynamicFormInfo[0];
                            String filedType = dynamicFormInfo[1];//Member, 表单字段类型
                            String filedId = dynamicFormInfo[2];//field0001
                            String displayName = dynamicFormInfo[3];
                            
                            
                            
                            Map<String, WorkflowFormFieldBO> dynamicFormFieldMap = null;
                            
                            if(dynamicFormMap.containsKey(formId)){
                                dynamicFormFieldMap = dynamicFormMap.get(formId);
                            }else{
                                dynamicFormFieldMap = WorkflowFormDataMapInvokeManager.getAppManager("form").getFormFieldMap(formId);
                                dynamicFormMap.put(formId, dynamicFormFieldMap);
                            }
                            
                            if(dynamicFormFieldMap != null){
                                WorkflowFormFieldBO field = dynamicFormFieldMap.get(filedId);
                                if(field != null && !displayName.equals(field.getDisplay())){
                                    String newName = node.getName().replaceFirst(displayName, field.getDisplay());
                                    party.setId(formId + "@" + filedType + "@" + filedId + "#" + field.getDisplay());
                                    node.setName(newName);
                                    actor.setName(newName);
                                    hasChange = true;
                                }
                            }
	                    }else if(ObjectName.isSuperNode(node)){// 超级节点
	                        
	                        String superNodeId = actor.getParty().getId();
	                        SuperNodeBean superNode = workflowSuperNodeManager.getSuperNode(superNodeId);
	                        if(superNode != null && !superNode.getName().equals(node.getName())){
	                            String newName = superNode.getName();
	                            node.setName(newName);
                                actor.setName(newName);
                                hasChange = true;
	                        }
	                    }
	                }
	            }
	            //替换分支
	            List<BPMTransition> links = process.getLinks();
	            if(links!=null && !links.isEmpty()){
	            	for(BPMTransition link : links){
	            		boolean hasChange1= resetLinkFormCondition(useFormAppId, fieldMap, link);
	            		if(hasChange1){
	            			hasChange= true;
	            		}
	            	}
	            }
	            
	            //替换分支
	            List<BPMCircleTransition> clinks = process.getClinks();
	            if(clinks!=null && !clinks.isEmpty()){
	            	for(BPMTransition link : clinks){
	            		boolean hasChange1= resetLinkFormCondition(useFormAppId, fieldMap, link);
	            		if(hasChange1){
	            			hasChange= true;
	            		}
	            	}
	            }
	        }
    	}
    	return hasChange;
    }
    
    
	private static boolean resetLinkFormCondition(String useFormAppId, Map<String, WorkflowFormFieldBO> fieldMap,BPMTransition link) {
		String appName = "form";
		Pattern pattern = Pattern.compile("field\\d+");
	
		String branchExpression = link.getFormCondition();
		if(branchExpression!=null && !"".equals(branchExpression.trim())){
			boolean find = false;
			Matcher matcher = pattern.matcher(branchExpression);
			StringBuffer sb = new StringBuffer();
			while(matcher.find()){
				String fieldName = matcher.group();
				WorkflowFormFieldBO field = fieldMap.get(fieldName);
				if(field!=null){
					String display = "{"+field.getDisplay()+"}";
					matcher.appendReplacement(sb, display);
					find = true;
				}
			}
			matcher.appendTail(sb);
			branchExpression = sb.toString();
			if(find){
	            String newTitle= ExpressionFactory.parseConditionTitle(appName, branchExpression, useFormAppId, fieldMap);
	            if(Strings.isNotBlank(newTitle)){
	                link.setConditionTitle(newTitle);
	                return true;
	            }
			}
	        
		}
		return false;
	}
    
    /**
     * 获得指定元素下的子节点集合
     * @param parent
     * @param oldTag
     * @param newTag
     * @return
     */
    public static List<Element> getChildElements(Element parent, String oldTag) {
        List<Element> elements= parent.elements(oldTag);
        String newTag= oldTag2newTagMap.get(oldTag);
        if((null==elements || elements.size()<=0) && null!=newTag){
            elements= parent.elements(newTag);
        }
        return elements;
    }
    
    /**
     * fixed OA-68903节点权限名称中有空格，流程复制的时候判断成了节点权限不存在
     * @param value
     * @return
     */
    public static String replaceAscii160ToAscii36(String value) {
        if(Strings.isNotBlank(value)){
            //160的空格是 &nbsp;, 在数据库中是乱码
            value=value.replace((char)160, ' ');
        }
        return value;
    }
    
    public static Long[] stringToLongArray(String value, String split){
    	Long[] result = null;
    	if(value!=null && !"".equals(value.trim()) && split!=null){
        	String[] items = value.split(split);
        	List<Long> idList = new ArrayList<Long>();
    		if(items!=null && items.length>0){
    			int index = 0;
    			for(String item : items){
    				try{
    					idList.add(Long.parseLong(item));
        				index++;
    				}catch(NumberFormatException e){
    					logger.error("转换Long类型错误！value=" + value, e);
    				}
    			}
    		}
    		if(!idList.isEmpty()){
    			result = new Long[idList.size()];
    			idList.toArray(result);
    		}
    	}
    	return result;
    }
    
    public static String stringIfNullToDefault(String value, String defaultValue){
    	String result = value;
    	if(value==null || "".equals(value.trim())){
    		result = defaultValue;
    	}
    	return result;
    }
    
    /**
     * 是否为long类型
     * @param id
     * @return
     */
    public static boolean isLong(String id){
        if(Strings.isBlank(id)){
            return false;
        }
        String regex= "[-]{0,1}[\\d]+?";
        if(id.matches(regex)){//id为数字
            return true;
        }
        return false;
    }
    
    public static String[] splitString(String idString, String seprator){
        String[] result = null;
        if(idString!=null){
            List<String> resultList = new ArrayList<String>();
            if(seprator==null){
                seprator = ",";
            }
            int seperatorLength = seprator.length();
            StringBuilder sb = new StringBuilder();
            for(int i=0,len=idString.length(); i<len; i++){
                char ch = idString.charAt(i);
                if(i+seperatorLength<=len){
                    String temp = idString.substring(i, i+seperatorLength);
                    if(seprator.equals(temp)){
                        resultList.add(sb.toString());
                        sb.setLength(0);
                    }else{
                        sb.append(ch);
                    }
                }else{
                    sb.append(idString.substring(i));
                }
            }
            resultList.add(sb.toString());
            if(resultList!=null && !resultList.isEmpty()){
                result = new String[resultList.size()];
                resultList.toArray(result);
            }
        }
        return result;
    }
    
    public static Map<String, String> findSplitJoinMap(BPMProcess process) {
        Set<String> temp= new HashSet<String>();
        Map<String,String> splitJoinMap= new HashMap<String, String>();
        Map<String,String> joinSplitMap= new HashMap<String, String>();
        List<BPMAbstractNode> splitArr = new ArrayList<BPMAbstractNode>();
        makePairSplitAndJoinMap(process.getStart(), splitArr, temp,splitJoinMap,joinSplitMap);
        return splitJoinMap;
    }
    
    private static void makePairSplitAndJoinMap(BPMAbstractNode beginNode, List<BPMAbstractNode> splitArr, Set<String> temp
            ,Map<String,String> splitJoinMap,Map<String,String> joinSplitMap){
        if (beginNode.getNodeType().equals(BPMAbstractNode.NodeType.end)) {//结束节点
            return;
        }
        if (temp.contains(beginNode.getId())) {
            return;
        }
        temp.add(beginNode.getId());
        List<BPMTransition> downs = beginNode.getDownTransitions();
        if(null==downs){//a6升级上来的错误数据，这儿会报错
            return;
        }
        for (BPMTransition bpmTransition : downs) {
            BPMAbstractNode child = bpmTransition.getTo();
            if (null!=child && isSplit(child)) {//split节点
                splitArr.add(child);
            } else if (null!=child && isJoin(child)) {//join节点
                if (null != joinSplitMap.get(child.getId())) {
                    continue;
                }
                int index = splitArr.size() - 1;
                if(index>=0){
                    BPMAbstractNode sNode = splitArr.remove(index);
                    splitJoinMap.put(sNode.getId(), child.getId());
                    joinSplitMap.put(child.getId(), sNode.getId());
                }
            }
            if(null!=child){
                makePairSplitAndJoinMap(child, splitArr, temp,splitJoinMap,joinSplitMap);
            }
        }
    }
    
    /**
     * 是否为join节点
     * @param node
     * @return
     */
    public static boolean isJoin(BPMAbstractNode node){
        if( null!=node.getUpTransitions() && node.getUpTransitions().size()>1){
            if(node.getNodeType().equals(BPMAbstractNode.NodeType.join)){
                return true;
            }else{
            	if(logger.isInfoEnabled()){
                    logger.info("find problem join node:"+node.getId());
                }
            }
        }
        return false;
    }
    
    /**
     * 是否为split节点
     * @param node
     * @return
     */
    public static boolean isSplit(BPMAbstractNode node){
        if( null!=node.getDownTransitions() && node.getDownTransitions().size()>1){
            if(node.getNodeType().equals(BPMAbstractNode.NodeType.split)){
                return true;
            }else{
                if(logger.isInfoEnabled()){
                    logger.info("find problem split node:"+node.getId());
                }
            }
        }
        return false;
    }

    public static boolean isAllChildDelete(BPMAbstractNode node,BPMCase theCase, WorkflowBpmContext context) {
        boolean isDelete = true;
        List<BPMHumenActivity> subs = BPMProcess.findDirectHumenChildrenCascade(node);
        if (subs != null && subs.size() > 0) {
            for (BPMHumenActivity sub : subs) {
                String _isDelete= sub.getSeeyonPolicy().getIsDelete();
                if ("false".equals(_isDelete)) {
                    isDelete = false;
                    break;
                }else{
                    putNodeConditionToContext(context, sub, "isDelete", "true");
                }
            }
        }
        return isDelete;
    }

    /**
     * 从case中获得单人节点备份信息
     * @param theCase
     * @param keyId
     * @return
     */
    public static String getOldUserInfoFromCase(BPMCase theCase, String keyId) {
        if(null!=theCase){
            if(null!=theCase.getDataMap().get(keyId)){
                return theCase.getDataMap().get(keyId).toString(); 
            }
        }
        return null;
    }

    /**
     * 删除备份信息
     * @param theCase
     * @param nodeId
     */
    public static void removeNodeBakUserInfoFromCase(BPMCase theCase, String nodeId) {
        String keyId= nodeId+"_bakUserId";
        String keyName= nodeId+"_bakUserName";
        String keyAccountId= nodeId+"_bakAccountId";
        if(null!=theCase){
            theCase.getDataMap().remove(keyId);
            theCase.getDataMap().remove(keyName);
            theCase.getDataMap().remove(keyAccountId);
        }
    }
    
    /**
     * 还原超期替换节点相关信息
     */
    public static void recoverNodeBakUserInfo(BPMHumenActivity humenActivity,WorkflowBpmContext context) {
        try{
            List<BPMActor> actors = humenActivity.getActorList();
            BPMActor actor = actors.get(0);
            String nodeId= humenActivity.getId();
            String keyId= nodeId+"_bakUserId";
            String keyName= nodeId+"_bakUserName";
            String keyAccountId= nodeId+"_bakAccountId";
            String keyAddition= nodeId+"_bakAddition";
            String keyRAddition= nodeId+"_bakRAddition";
            String keyProcessMode= nodeId+"_bakProcessMode";
            BPMCase theCase= context.getTheCase();
            String oldUserId= getOldUserInfoFromCase(theCase,keyId);
            String oldAddition= getOldUserInfoFromCase(theCase,keyAddition);
            String oldRAddition= getOldUserInfoFromCase(theCase,keyRAddition);
            String oldProcessMode= getOldUserInfoFromCase(theCase,keyProcessMode);
            if(Strings.isNotBlank(oldUserId)){
                String oldUserName= getOldUserInfoFromCase(theCase,keyName);
                String oldUserAccountId= getOldUserInfoFromCase(theCase,keyAccountId);
                
                actor.getParty().setId(oldUserId);
                actor.getParty().setAccountId(oldUserAccountId);
                
                humenActivity.setName(oldUserName);
                actor.getParty().setName(oldUserName);
                BPMParticipantType type= new BPMParticipantType("user");
                actor.getParty().setType(type);
                humenActivity.getSeeyonPolicy().setProcessMode("single");
                
                removeNodeBakUserInfoFromCase(theCase,nodeId);
                
                Map<String,String> nodeAdditionMap= getNodeAdditionMap(theCase);
                Map<String,String> nodeRAdditionMap= getNodeRAdditionMap(theCase);
                actor.getParty().setAddition(oldUserId);
                actor.getParty().setRaddition(oldUserId);
                nodeAdditionMap.put(humenActivity.getId(), oldUserId);
                nodeRAdditionMap.put(humenActivity.getId(), oldUserId);
                
            }else if(Strings.isNotBlank(oldAddition)){
                
                Map<String,String> nodeAdditionMap= getNodeAdditionMap(theCase);
                Map<String,String> nodeRAdditionMap= getNodeRAdditionMap(theCase);
                actor.getParty().setAddition(oldAddition);
                actor.getParty().setRaddition(oldRAddition);
                nodeAdditionMap.put(humenActivity.getId(), oldAddition);
                nodeRAdditionMap.put(humenActivity.getId(), oldRAddition);
                
                if(Strings.isNotBlank(oldProcessMode)){
                    humenActivity.getSeeyonPolicy().setProcessMode(oldProcessMode);
                }
            }else{
            	//Map<String,String> nodeAdditionMap= getNodeAdditionMap(theCase);
                Map<String,String> nodeRAdditionMap= getNodeRAdditionMap(theCase);
                //actor.getParty().setAddition(""); 
                actor.getParty().setRaddition("");
                //nodeAdditionMap.put(humenActivity.getId(), "");
                nodeRAdditionMap.put(humenActivity.getId(), ""); 
            }
        }catch(Throwable e){
            logger.warn("还原被退回节点"+humenActivity.getId()+"出现异常!", e);
        }
    }

    /**
     * 将单人节点信息<key,value>备份到case中,以便流程回退时使用
     * @param theCase
     * @param bpmActivity
     */
    public static void putNodeBakUserInfoToCase(BPMCase theCase, BPMHumenActivity bpmActivity) {
        List<BPMActor> actors = bpmActivity.getActorList();
        BPMActor actor = actors.get(0);
        String oldUserId= actor.getParty().getId();
        String nodeId= bpmActivity.getId();
        String keyId= nodeId+"_bakUserId";//将单人节点ID备份到case中,以便流程回退时使用
        String keyName= nodeId+"_bakUserName";//将单人节点名称备份到case中,以便流程回退时使用
        String keyAccountId= nodeId+"_bakAccountId";//将单人节点单位ID备份到case中,以便流程回退时使用
        if(null!=theCase){
            theCase.getDataMap().put(keyId,oldUserId);
            theCase.getDataMap().put(keyName,bpmActivity.getName());
            theCase.getDataMap().put(keyAccountId,actor.getParty().getAccountId());
        }
    }
    
    /**
     * 从case中获得所有节点的addition信息
     * @param theCase
     * @return
     */
    public static Map<String, String> getNodeAdditionMap(BPMCase theCase){
        Object result= null;
        if(null!=theCase){
            result= theCase.getData(ActionRunner.WF_NODE_ADDITION_KEY);
        }
        return result==null?new HashMap<String, String>():(Map<String,String>)result;
    }

    /**
     * 从case中获得所有节点的raddition信息
     * @param theCase
     * @return
     */
    public static Map<String, String> getNodeRAdditionMap(BPMCase theCase){
        Object result= null;
        if(null!=theCase){
            result= theCase.getData(ActionRunner.WF_NODE_RADDITION_KEY);
        }
        return result==null?new HashMap<String, String>():(Map<String,String>)result;
    }
    
    /**
     * 从case中获得所有节点的分支匹配信息
     * @param theCase
     * @return
     */
    public static Map<String, Map<String,String>> getNodeConditionChangeInfoMap(BPMCase theCase){
        Object result= null;
        if(null!=theCase){
            result= theCase.getData(ActionRunner.WF_NODE_CONDITION_CHANGE_KEY);
        }
        return result==null?new HashMap<String, Map<String,String>>():(Map<String, Map<String,String>>)result;
    }

    /**
     * 将case中的addition、raddition、isPass和isDelete信息放入到context对象中
     * @param theCase
     * @param context
     */
    public static void putCaseToWorkflowBPMContext(BPMCase theCase, WorkflowBpmContext context) {
        if(null!=theCase && null!=context){
            context.setNodeAdditionMap(getNodeAdditionMap(theCase));
            context.setNodeRAdditionMap(getNodeRAdditionMap(theCase));
            context.setNodeConditionChangeInfoMap(getNodeConditionChangeInfoMap(theCase));
        }
    }

    public static void putCaseDynamicFormToContext(WorkflowBpmContext context,String activityId,DynamicFormMasterInfo dyInfo){
    	if(context!=null){
    		Map<String,DynamicFormMasterInfo> m = context.getDynamicFormMap();
    		m.put(activityId, dyInfo);
    	}
    }
    
    public static void putContextDynamicFormToCase(WorkflowBpmContext context, BPMCase theCase,String activityId){
    	
    	if(null==theCase || null==context){
    		return; 
    	}
    	DynamicFormMasterInfo currentDyInfo = (DynamicFormMasterInfo)context.getDynamicFormMap().get(activityId);
    	if(currentDyInfo == null){
    		return ; 
    	}
    	String dyForm = (String) theCase.getDataMap().get(ActionRunner.WF_DYNAMIC_FORM_KEY);
    	
    	if(dyForm.indexOf(activityId)!=-1){
    		return ;
    	}
    	else{
    		List<DynamicFormMasterInfo> toMasters = getCaseDyList(dyForm);
			toMasters.add(currentDyInfo);
    		theCase.getDataMap().put(ActionRunner.WF_DYNAMIC_FORM_KEY, JSONUtil.toJSONString(toMasters));	
    	}
		
    }

	private static List<DynamicFormMasterInfo> getCaseDyList(String dyForm) {
		List<DynamicFormMasterInfo> toMasters = new ArrayList<DynamicFormMasterInfo>();
		
		List dfmis = null;
		try {
			dfmis = JSONUtil.parseJSONString(dyForm, List.class);
		} catch (Throwable e) {
			logger.error("", e);
		}

		if (Strings.isNotEmpty(dfmis)) {
			for (Object o : dfmis) {
				if (o instanceof Map) {
					DynamicFormMasterInfo dfmi = new DynamicFormMasterInfo();
					ParamUtil.mapToBean((Map) o, dfmi, false);
					toMasters.add(dfmi);
				}
			}
		}
		return toMasters;
	}
    /*清楚流程动态匹配表中存储的底表的值*/
	public static void removeWFDynamicFormMasterIds(WorkflowBpmContext context, BPMCase theCase, String activityId) {
		String dyForm = (String) theCase.getDataMap().get(ActionRunner.WF_DYNAMIC_FORM_KEY);
		if (Strings.isNotBlank(dyForm)) {
			List dfmis = null;
			try {
				dfmis = JSONUtil.parseJSONString(dyForm, List.class);
			} catch (Throwable e) {
				logger.error("", e);
			}

			List<DynamicFormMasterInfo> toMasters = new ArrayList<DynamicFormMasterInfo>();
			if (Strings.isNotEmpty(dfmis)) {
				for (Object o : dfmis) {
					if (o instanceof Map) {
						DynamicFormMasterInfo dfmi = new DynamicFormMasterInfo();
						ParamUtil.mapToBean((Map) o, dfmi, false);
						List<String> nodeIds = dfmi.getNodeIds();
						if (nodeIds.contains(activityId)) {
							nodeIds.remove(activityId);
							putCaseDynamicFormToContext(context, activityId, dfmi);
						}
						if (nodeIds.size() != 0) { // 等于0，说明所有的动态匹配节点都已经被删除掉了。这样清空存储的值，重新流转的时候让重新匹配。
							toMasters.add(dfmi);
						}
					}
				}
			}
			String todb = JSONUtil.toJSONString(toMasters);
			theCase.getDataMap().put(ActionRunner.WF_DYNAMIC_FORM_KEY, todb);
		}
	}
    /**
     * 将context对象中的addition、raddition、isPass和isDelete信息放入到case对象中
     * @param context
     * @param theCase
     */
    public static void putWorkflowBPMContextToCase(WorkflowBpmContext context, BPMCase theCase) {
        if(null!=theCase && null!=context){
            theCase.getDataMap().put(ActionRunner.WF_NODE_ADDITION_KEY,context.getNodeAdditionMap());
            theCase.getDataMap().put(ActionRunner.WF_NODE_RADDITION_KEY,context.getNodeRAdditionMap());
            theCase.getDataMap().put(ActionRunner.WF_NODE_CONDITION_CHANGE_KEY,context.getNodeConditionChangeInfoMap());
        }
    }
    
    /**
     * 将nodeAdditionMap、nodeRAdditionMap、nodeConditionChangeInfoMap信息放入到case对象中
     * @param nodeAdditionMap
     * @param nodeRAdditionMap
     * @param nodeConditionChangeInfoMap
     * @param theCase
     */
    public static void putWorkflowBPMContextToCase(Map<String, String> nodeAdditionMap, Map<String, String> nodeRAdditionMap,
            Map<String, Map<String, String>> nodeConditionChangeInfoMap, BPMCase theCase) {
        if(null!=theCase){
            theCase.getDataMap().put(ActionRunner.WF_NODE_ADDITION_KEY,nodeAdditionMap);
            theCase.getDataMap().put(ActionRunner.WF_NODE_RADDITION_KEY,nodeRAdditionMap);
            theCase.getDataMap().put(ActionRunner.WF_NODE_CONDITION_CHANGE_KEY,nodeConditionChangeInfoMap);
        }
    }
    
    /**
     * 将nodeAdditionMap、nodeRAdditionMap信息放入到case对象中
     * @param nodeAdditionMap
     * @param nodeRAdditionMap
     * @param theCase
     */
    public static void putWorkflowBPMContextToCase(Map<String, String> nodeAdditionMap, Map<String, String> nodeRAdditionMap,
            BPMCase theCase) {
        if(null!=theCase){
            theCase.getDataMap().put(ActionRunner.WF_NODE_ADDITION_KEY,nodeAdditionMap);
            theCase.getDataMap().put(ActionRunner.WF_NODE_RADDITION_KEY,nodeRAdditionMap);
        }
    }

    /**
     * 备份additon和raddition和processMode
     * @param theCase
     * @param bpmActivity
     * @param addition
     * @param raddition
     */
    public static void putNodeBakAdditionInfoToCase(BPMCase theCase, BPMHumenActivity bpmActivity, String addition,
            String raddition) {
        if(null!=theCase){
            String nodeId= bpmActivity.getId();
            Object oldAddition = theCase.getDataMap().get(nodeId+"_bakAddition");
            if(null == oldAddition || (String)oldAddition == ""){
            	theCase.getDataMap().put(nodeId+"_bakAddition",addition);
            }
            Object oldRAddition = theCase.getDataMap().get(nodeId+"_bakRAddition");
            if(null == oldRAddition || (String)oldRAddition == ""){
            	theCase.getDataMap().put(nodeId+"_bakRAddition",raddition);
            }
            Object oldProcessMode = theCase.getDataMap().get(nodeId+"_bakRAddition");
            if(null == oldProcessMode || (String)oldProcessMode == ""){
            	theCase.getDataMap().put(nodeId+"_bakProcessMode",bpmActivity.getSeeyonPolicy().getProcessMode());
            }
        }
    }
    
    public static List<BPMHumenActivity> getParentHumens(BPMCase theCase,BPMAbstractNode currentNode) {
		Map<String, String> nodeIds= new HashMap<String,String>();
		List<BPMHumenActivity> parents= getParentHumens(theCase,currentNode,currentNode,true, nodeIds);
		return parents;
	}
	
	/**
	 * 
	 * @param currentNode
	 * @param isPassInformNode
	 * @param nodeIds
	 * @return
	 */
	public static List<BPMHumenActivity> getParentHumens(BPMCase theCase,BPMAbstractNode sourceNode,BPMAbstractNode currentNode,boolean isPassInformNode,Map<String, String> nodeIds) {
		List<BPMHumenActivity> humenList= new ArrayList<BPMHumenActivity>();
		if(nodeIds.containsKey(currentNode.getId())){
			return humenList;
		}
		nodeIds.put(currentNode.getId(), currentNode.getId());
		List<BPMTransition> ups= currentNode.getUpTransitions();
		for (Iterator iterator = ups.iterator(); iterator.hasNext();) {
			BPMTransition bpmTransition = (BPMTransition) iterator.next();
			BPMAbstractNode from = bpmTransition.getFrom();
			String policy = from.getSeeyonPolicy().getId();
			String isDelete= getNodeConditionFromCase(theCase, from, "isDelete");
            if(!"true".equalsIgnoreCase(isDelete)){
            	if (from.getNodeType() == BPMAbstractNode.NodeType.humen && ("inform".equals(policy) || "zhihui".equals(policy))) {
                    if(isPassInformNode){
                        humenList.addAll(getParentHumens(theCase,sourceNode,from,isPassInformNode,nodeIds));
                    }else{
                        humenList.add((BPMHumenActivity) from);
                    }
                } else if (from.getNodeType() == BPMAbstractNode.NodeType.humen) {
                    humenList.add((BPMHumenActivity) from);
                } else if (from.getNodeType() == BPMAbstractNode.NodeType.join){
                	humenList.addAll(getParentHumens(theCase,sourceNode,from,isPassInformNode,nodeIds));
                } else if (from.getNodeType() == BPMAbstractNode.NodeType.split) {
                	humenList.addAll(getParentHumens(theCase,sourceNode,from,isPassInformNode,nodeIds));
                }
            }
		}
		return humenList;
	}
	
	/**
	 * 获得指定节点的所有孩子节点
	 * @param toNode
	 * @param b
	 * @param theCase
	 */
	public static List<BPMAbstractNode> getChildren(BPMAbstractNode toNode) {
		List<BPMAbstractNode> children = new ArrayList<BPMAbstractNode>();
		List<BPMTransition> downLinks= toNode.getDownTransitions();
		if(null!=downLinks){
			for (BPMTransition bpmTransition : downLinks) {
				BPMAbstractNode ctoNode= bpmTransition.getTo();
				String isDelete= ctoNode.getSeeyonPolicy().getIsDelete();
				if(!"true".equals(isDelete)){
					children.add(ctoNode);
				}
			}
		}
		return children;
	}
	
	/**
	 * 获得指定节点的所有父亲节点
	 * @param toNode
	 * @return
	 */
	public static List<BPMAbstractNode> getParent(BPMAbstractNode toNode) {
		List<BPMAbstractNode> parent = new ArrayList<BPMAbstractNode>();
		List<BPMTransition> upLinks= toNode.getUpTransitions();
		if(null!=upLinks){
			for (BPMTransition bpmTransition : upLinks) {
				BPMAbstractNode cfromNode= bpmTransition.getFrom();
				String isDelete= cfromNode.getSeeyonPolicy().getIsDelete();
				if(!"true".equals(isDelete)){
					parent.add(cfromNode);
				}
			}
		}
		return parent;
	}
	
	/**
	 * 获得指定节点的环形分支的所有孩子节点
	 * @param toNode
	 * @param b
	 * @param theCase
	 */
	public static List<BPMAbstractNode> getClinkChildren(BPMAbstractNode toNode) {
		List<BPMAbstractNode> children = new ArrayList<BPMAbstractNode>();
		List<BPMCircleTransition> downLinks= toNode.getDownCirlcleTransitions();
		if(null!=downLinks){
			for (BPMTransition bpmTransition : downLinks) {
				BPMAbstractNode ctoNode= bpmTransition.getTo();
				String isDelete= ctoNode.getSeeyonPolicy().getIsDelete();
				if(!"true".equals(isDelete)){
					children.add(ctoNode);
				}
			}
		}
		return children;
	}
	/**
     * JSON字符串特殊字符处理
     * @param s
     * @return String
     */
    public static String escapeString4Json(String s) {
    	if(Strings.isBlank(s)){
    		return s;
    	}
        StringBuffer sb = new StringBuffer();      
        for (int i=0; i<s.length(); i++) {
        	char c = s.charAt(i);  
        	 switch (c){
        	 case '\"':      
                 sb.append("\\\"");      
                 break;      
             case '\\':      
                 sb.append("\\\\");      
                 break;      
             case '/':      
                 sb.append("\\/");      
                 break;      
             case '\b':      
                 sb.append("\\b");      
                 break;      
             case '\f':      
                 sb.append("\\f");      
                 break;      
             case '\n':      
                 sb.append("\\n");      
                 break;      
             case '\r':      
                 sb.append("\\r");      
                 break;      
             case '\t':      
                 sb.append("\\t");      
                 break;      
             default:      
                 sb.append(c);   
        	 }
         }    
        return sb.toString();   
    }
    
    /**
     * 创建一个空的流程对象：只有发起节点和结束节点
     * @param defaultPolicyId
     * @param defaultPolicyName
     * @param currentUserId
     * @param currentUserName
     * @param currentAccountId
     * @return
     */
	public static BPMProcess createEmptyProcess(String defaultPolicyId,String defaultPolicyName,String currentUserId,
			String currentUserName,String currentAccountId) {
		String processId= String.valueOf(UUIDLong.longUUID());
		BPMProcess process= new BPMProcess(processId, processId);

        //根据xml更新process
        process.setId(processId);
        process.setIndex(processId);
        process.setName(processId);
		
		BPMSeeyonPolicy startPolicy= new BPMSeeyonPolicy(defaultPolicyId, defaultPolicyName);
		BPMSeeyonPolicy endPolicy= new BPMSeeyonPolicy("collaboration", ResourceUtil.getString("workflow.createProcessXml.collaboration")/*"协同"*/);
		
		BPMStart startNode= new BPMStart("start", "start");
		startNode.setName(currentUserName);
		BPMActor startUserActor = new BPMActor(currentUserId, currentUserName, new BPMParticipantType("user"), "roleadmin",
	                BPMActor.CONDITION_OR, false, currentAccountId);
	    List<BPMActor> actorList = new ArrayList<BPMActor>();
	    actorList.add(startUserActor);
	    startNode.setActorList(actorList);
	    startNode.setSeeyonPolicy(startPolicy);
		
		BPMEnd endNode= new BPMEnd("end", "end");
		endNode.setSeeyonPolicy(endPolicy);
		
		BPMTransition link= new BPMTransition(startNode,endNode);
		
		process.addChild(startNode);
		process.addChild(endNode);
		process.addLink(link);
		
		return process;
	}
	private static boolean isRoleExist(String roleIdorName, String accountIdString) throws BPMException{
    	boolean result = false;
    	if("".equals(roleIdorName) || WorkFlowMatchUserManager.ORGENT_META_KEY_DEPMEMBER.equals(roleIdorName)
                || WorkFlowMatchUserManager.ORGENT_META_KEY_SUPERDEPMANAGER.equals(roleIdorName)
                || OrgConstants.Role_NAME.DepAdmin.name().equals(roleIdorName)
                || OrgConstants.Role_NAME.DepManager.name().equals(roleIdorName)
                || OrgConstants.Role_NAME.DepLeader.name().equals(roleIdorName)
                || OrgConstants.Role_NAME.Departmentexchange.name().equals(roleIdorName)
                || WorkFlowMatchUserManager.ORGENT_META_KEY_BlankNode.equals(roleIdorName)
                || WorkFlowMatchUserManager.ReciprocalRoleReporter.equals(roleIdorName)){
    		result = true;
        } else {
        	V3xOrgRole entity = null;
            try{
            	if(roleIdorName.startsWith("Vjoin")){//可能是vjoin角色
            		JoinOrgManagerDirect joinOrgManagerDirect= (JoinOrgManagerDirect)AppContext.getBean("joinOrgManagerDirect");
             		entity= joinOrgManagerDirect.getRoleByCode(roleIdorName, null);
            	}
            	if(null==entity){
	                Long partyIdLong = Long.parseLong(roleIdorName);
	                entity = processOrgManager.getRoleById(partyIdLong);
	                if(null==entity){
	                	try{
	                        Long accountId = Long.parseLong(accountIdString);
	                        entity = processOrgManager.getRoleByName(roleIdorName, accountId);
	                    } catch (NumberFormatException ex) {
	                    }
	                }
            	}
            } catch (Throwable e) {
                try{
                    Long accountId = Long.parseLong(accountIdString);
                    entity = processOrgManager.getRoleByName(roleIdorName, accountId);
                } catch (NumberFormatException ex) {
                }
            }
            //status==0表示选人界面不可见，1表示选人界面可见
            if(entity!=null && entity.isValid()){
            	if(entity.getExternalType()==OrgConstants.ExternalType.Inner.ordinal()){
            		if(entity.getStatus().equals(1)){
                		result = true;
                	}
            	}else{//vjoin外部角色
            		result = true;
            	}
            }
        }
    	return result;
    }
	/**
	 * 检查流程节点是否为可用状态,有两个地方需要使用
	 * (1)在展示流程时，对于不可用的节点，节点的图标需要置灰的
	 * (2)在保存流程时，需要进行检验，对于不正确的情况需要提示
	 * 由于这两个地方都需要使用，故将其提出到工具类中，方便使用和维护
	 * 创建人:zhiyanqiang	  
	 * 创建时间：2016年8月1日 下午5:56:25    
	 * @param partyId 就是action标签的partyType属性的值
	 * @return 返回相关的参数
	 * nodeStatusFlag:true,正常态;false,有问题的状态
	 * tempStatus:类型
	 * nodeMsgStr:提示信息
	 * @throws BPMException 
	 */
	public static Map<String,Object> checkNodeStatus4FormField(BPMAbstractNode node,String newFormApp) throws BPMException
	{
		List<BPMActor> actors = node.getActorList();
		Boolean nodeStatusFlag=true;
		Integer tempStatus=new Integer(1);
		//String nodeMsgStr=null;
        if(actors!=null && actors.size()>0){
            BPMActor actor = actors.get(0);
            BPMParticipant party = actor.getParty();
            //partyId就是actor标签的partyType属性的值
            String partyId = party.getId();
            String[] partyIdValue = partyId.split("[@|#]");
    		if(partyIdValue!=null){
            	//if(partyIdValue.length==3){
                    //String fieldName = partyId.substring(firstIndex+1, secondIndex);
                    //FormFieldBean newFieldBean = newFormBean.getFieldBeanByName(fieldName);
                    String displayName = "";
                    if(partyIdValue.length>=3){
                    	displayName = partyIdValue[2];
                    } else if(partyIdValue.length>0){
                    	displayName = partyIdValue[0];
                    }
                    int temp = 1;
                    if(partyIdValue.length<=3){
                        temp = ConditionValidateUtil.canBeFormFieldNode(newFormApp, displayName);
                    }else if(partyIdValue.length==4){
                    	String roleName= partyIdValue[3];
                    	if(roleName.startsWith(WorkflowUtil.VJOIN)){
                    		String roleId= roleName.substring(WorkflowUtil.VJOIN.length());
                    		if(isLong(roleId)){
                    			roleName= roleId;
                    		}
                    	}
                        temp = ConditionValidateUtil.canBeFormFieldRoleNode(newFormApp, displayName,roleName);
                    }
                    if(temp==0){
                    	nodeStatusFlag=false;
                    	//nodeMsgStr= ResourceUtil.getString("workflow.flowCopyValidate.7", node.getName());
                    	tempStatus=new Integer(0);
                    }else if(temp==-1){
                    	nodeStatusFlag=false;
                    	//nodeMsgStr=ResourceUtil.getString("workflow.flowCopyValidate.8", node.getName());
                    	tempStatus=new Integer(-1);
                    }
                  
                    if(partyIdValue.length==4){
                    		String roleIdorName = partyIdValue[3];
                    		if(roleIdorName.startsWith(WorkflowUtil.VJOIN)){
                        		String roleId= roleIdorName.substring(WorkflowUtil.VJOIN.length());
                        		if(isLong(roleId)){
                        			roleIdorName= roleId;
                        		}
                        	}
                    		if(!isRoleExist(roleIdorName, party.getAccountId())){
                    			nodeStatusFlag=false;
                    			tempStatus=new Integer(-2);
                    			//nodeMsgStr=ResourceUtil.getString("workflow.flowCopyValidate.5", node.getName(), partyId);
                    		}
                    }
                    
            	    
            	}
        }
        Map<String,Object> checkStatusMap=new HashMap<String, Object>(2);
        checkStatusMap.put("nodeStatusFlag", nodeStatusFlag);
        checkStatusMap.put("tempStatus", tempStatus);
        //checkStatusMap.put("nodeMsgStr", nodeMsgStr);
        return checkStatusMap;
		
	}
	
	/**
	 * 当前表单是否配置了动态表单映射、用于流程复制的时候节点是否可以使用
	 * @param currentFormAppId
	 * @param baseFormAppId
	 * @return
	 */
	public static boolean isCurrentFormConfigDynamicForm(String currentFormAppId,String baseFormAppId){
		WorkFlowAppExtendManager workFlowAppExtendManager = WorkFlowAppExtendInvokeManager.getAppManager("collaboration");
		return workFlowAppExtendManager.hasWFDynamicForm(currentFormAppId, baseFormAppId);
	}
	
	/**
	 * 获取流程processXml
	 * @param processXml
	 * @param processXmlTempId
	 * @return
	 */
	public static String getTempProcessXml(String processXml) {
		if(Strings.isNotBlank(processXml)) {
			if(WorkflowUtil.isLong(processXml)) {
				Long processXmlTempId = Long.parseLong(processXml);
				if(processXmlTempId != null) {
					ProcessXmlTempManager processXmlTempManager = (ProcessXmlTempManager)AppContext.getBean("processXmlTempManager");
					try {
						ProcessXmlTemp processTemp = processXmlTempManager.getProcessXmlTemp(processXmlTempId);
		        		if(processTemp != null && Strings.isNotBlank(processTemp.getProcessXml())) {
		        			processXml = processTemp.getProcessXml();
		        		}
		        	} catch(Exception e) {
		        		logger.error("从流程临时表查询数据失败", e);
		        	}
				}
			} else {
				logger.info("非移动端传值processXml");	
			}			
        }
		return processXml;
	}
	
	public static String savedProcessXmlTempAndReturnId(String processId, String processXml, String activityId, String userId, String action) {
		String result = processXml;
		try {
			ProcessXmlTempManager processXmlTempManager = (ProcessXmlTempManager)AppContext.getBean("processXmlTempManager");
			ProcessXmlTemp temp = processXmlTempManager.saveProcessXmlTemp(processId, processXml, activityId, userId, action);
			if(temp != null) {
				result = String.valueOf(temp.getId());
			}
		} catch(Exception e) {
			logger.error("H5传输清空processXml失败", e);
		}
		return result;
	}

	/**
	 * 
	 * @param activity
	 * @return
	 */
	public static List<BPMHumenActivity> getAllChildHumens(BPMHumenActivity activity) {
		Set<String> nodeIdSet= new HashSet<String>();
		return getAllChildHumens(activity, nodeIdSet);
	}
	
	public static List<BPMHumenActivity> getAllChildHumens(BPMActivity activity,Set<String> nodeIdSet) {
		List<BPMHumenActivity> humenList = new UniqueList<BPMHumenActivity>(); 
		if(nodeIdSet.contains(activity.getId())){
			return humenList;
		}
		nodeIdSet.add(activity.getId());
        List<BPMTransition> transitions = activity.getDownTransitions();
        for (BPMTransition trans : transitions) {
            BPMAbstractNode child = trans.getTo();
            String policy = child.getSeeyonPolicy().getId();
            
            if (child.getNodeType() == BPMAbstractNode.NodeType.humen 
            		||child.getNodeType() == BPMAbstractNode.NodeType.join
                    || child.getNodeType() == BPMAbstractNode.NodeType.split) {
            	if (child.getNodeType() == BPMAbstractNode.NodeType.humen){
            		if(!isInformNode(child)){
            			humenList.add((BPMHumenActivity)child);
            		}
            	}
                humenList.addAll(getAllChildHumens((BPMActivity) child,nodeIdSet));
            }
        }
        return humenList;
    }
	
	public static List<Map<String,String>> getNodeMemberInfos(BPMProcess process) throws BPMException {
		List<Map<String,String>> nodeInfos = new ArrayList<Map<String,String>>();// [NodeName,NodeId,PartType,PartId]
		if(process != null){
			List<BPMAbstractNode> processes = wapi.getHumenNodeInOrderFromProcess(process);
			List<BPMAbstractNode> nodes = new ArrayList<BPMAbstractNode>(processes.size());
			for (Object _node : processes) {
				BPMAbstractNode node = (BPMAbstractNode) _node;
				if (!"start".equals(node.getId()) && !"end".equals(node.getId()) && !"join".equals(node.getName())
						&& !"split".equals(node.getName())) {
					if (Strings.isNotEmpty(node.getActorList())) {
					    nodes.add(node);
					} 
				} 
			}
			
            //解析
			for(BPMAbstractNode node : nodes){
				
				Map<String,String> map = new HashMap<String,String>();
				
                BPMActor actor = (BPMActor) node.getActorList().get(0);
                
                String nodeId = node.getId();
                String type = actor.getType().id;
                String nodeName = node.getBPMAbstractNodeName();
                String accountId =  actor.getParty().getAccountId();

                map.put("nodeId", nodeId);
                map.put("nodeName", nodeName);
                map.put("actorTypeId", type);
                map.put("actorPartyId", actor.getParty().getId());
                map.put("actorPartyAccountId", accountId);
                nodeInfos.add(map);
			}
		}
		return nodeInfos;
	}

	/**
	 * 初始化发起者ID和单位、processId信息
	 * @param processXml
	 * @return
	 */
	public static String initStartAndProcessIdInfo(String processXml,BPMProcess process) {
		String statNodeId="i=\"start\"";
		int startIdPosition= processXml.indexOf(statNodeId);
		int startNodeBeginPosition= -1;
		int startNodeEndPosition= -1;
		String preXml= "";
		String sufixXml= "";
		if(startIdPosition>=0){//新数据
			startNodeBeginPosition= processXml.lastIndexOf("<n ", startIdPosition);
			startNodeEndPosition= processXml.indexOf("</n>", startIdPosition)+"</n>".length();
		}else{//老数据
			String oldStatNodeId="id=\"start\"";
			startIdPosition= processXml.indexOf(oldStatNodeId);
			startNodeBeginPosition= processXml.lastIndexOf("<node ", startIdPosition);
			startNodeEndPosition= processXml.indexOf("</node>", startIdPosition)+"</node>".length();
		}
		preXml= processXml.substring(0,startNodeBeginPosition);
		sufixXml= processXml.substring(startNodeEndPosition);
		
		StringXMLElement startNodeXmlElement = new StringXMLElement("n");
		process.getStart().toXML(startNodeXmlElement,null);
		String runtimeStartNodeXml= startNodeXmlElement.toString();
		String runtimeProcessXml= preXml + runtimeStartNodeXml + sufixXml;
		return runtimeProcessXml;
	}
	
	public static StringBuilder getMemberNames(List<V3xOrgMember> members){
	    return getMemberNames(members, true);
	}
	
	/**
	 * 
	 * 拼装人员信息
	 * 
	 * @param members
	 * @param limit 是否限制
	 * @return
	 *
	 */
    public static StringBuilder getMemberNames(List<V3xOrgMember> members, boolean limit) {
		StringBuilder membersBuilder= new StringBuilder("<font color='red'>");
		if(null!=members && !members.isEmpty()){
			int j=0;
			for (V3xOrgMember v3xOrgMember : members) { 
				if(j>0){
					membersBuilder.append("、");
				}
				membersBuilder.append(v3xOrgMember.getName());
                if (j == 9 && limit) {
					membersBuilder.append(ResourceUtil.getString("workflow.label.staffSize", members.size())/*"等共"+members.size()+"人"*/);
					break;
				}
				j++; 
			}
		}else{
			membersBuilder.append(ResourceUtil.getString("common.none")/*"无"*/);
		}
		membersBuilder.append(".");
		return membersBuilder.append("</font>");
	}

	public static boolean isMyDirectParentNode(BPMCase theCase, BPMAbstractNode toNode, String autoSkipNodeId) {
		Map<String,String> passedNodeIds= new HashMap<String, String>();
		Set<String> parentNodeIds= getParentNodeIds(theCase,toNode,true,passedNodeIds);
		if(parentNodeIds.contains(autoSkipNodeId)){
			return true;
		}
		return false;
	}

	private static Set<String> getParentNodeIds(BPMCase theCase, BPMAbstractNode currentNode,boolean isPassInformNode,Map<String, String> nodeIds) {
		Set<String> parentIdSet= new HashSet<String>();
		if(nodeIds.containsKey(currentNode.getId())){
			return parentIdSet;
		}
		nodeIds.put(currentNode.getId(), currentNode.getId());
		List<BPMTransition> ups= currentNode.getUpTransitions();
		for (Iterator iterator = ups.iterator(); iterator.hasNext();) {
			BPMTransition bpmTransition = (BPMTransition) iterator.next();
			BPMAbstractNode from = bpmTransition.getFrom();
			String policy = from.getSeeyonPolicy().getId();
			String isDelete= getNodeConditionFromCase(theCase, from, "isDelete");
            if(!"true".equalsIgnoreCase(isDelete)){
            	if (from.getNodeType() == BPMAbstractNode.NodeType.humen && ("inform".equals(policy) || "zhihui".equals(policy))) {
                    if(isPassInformNode){
                    	parentIdSet.addAll(getParentNodeIds(theCase,from,isPassInformNode,nodeIds));
                    }else{
                    	parentIdSet.add(from.getId());
                    }
                } else if (from.getNodeType() == BPMAbstractNode.NodeType.humen) {
                	parentIdSet.add(from.getId());
                } else if (from.getNodeType() == BPMAbstractNode.NodeType.join){
                	parentIdSet.addAll(getParentNodeIds(theCase,from,isPassInformNode,nodeIds));
                } else if (from.getNodeType() == BPMAbstractNode.NodeType.split) {
                	parentIdSet.addAll(getParentNodeIds(theCase,from,isPassInformNode,nodeIds));
                }
            }
		}
		return parentIdSet;
	}

	/**
	 * 获得不可用人员节点集合
	 * @param invalidateActivityMap
	 * @return
	 */
	public static String getInvalidateActivityMapStr(Map<String, String> invalidateActivityMap,boolean useI18n) {
		String invalidateActivityMapStr= "";
		if(null!=invalidateActivityMap && invalidateActivityMap.size()>0){//有人员节点不可用，不可提交
            Set<String> keys= invalidateActivityMap.keySet();
            for (String key : keys) {
                String nodeName= invalidateActivityMap.get(key);
                invalidateActivityMapStr += invalidateActivityMapStr==""?nodeName:","+nodeName;
            }
            if(useI18n){
            	invalidateActivityMapStr= ResourceUtil.getString("workflow.invalidateActivity.label", invalidateActivityMapStr);
            }
        }
		return invalidateActivityMapStr;
	}

	/**
	 * 
	 * @param nodeMsg
	 * @return
	 */
	public static LinkedHashMap<String, List<String>> getWorkflowMatchMsgMap(String nodeMsg) {
		LinkedHashMap<String, List<String>> workflowMatchMsgMap= new LinkedHashMap<String, List<String>>();
		if(Strings.isNotBlank(nodeMsg)){
			try {
				JSONObject nodeMsgObj = new JSONObject(nodeMsg);
				workflowMatchMsgMap.put("step0", getStepList(nodeMsgObj,"step0"));
				workflowMatchMsgMap.put("step1", getStepList(nodeMsgObj,"step1"));
				workflowMatchMsgMap.put("step2", getStepList(nodeMsgObj,"step2"));
				workflowMatchMsgMap.put("step3", getStepList(nodeMsgObj,"step3"));
				workflowMatchMsgMap.put("step4", getStepList(nodeMsgObj,"step4"));
				workflowMatchMsgMap.put("step5", getStepList(nodeMsgObj,"step5")); 
				workflowMatchMsgMap.put("step6", getStepList(nodeMsgObj,"step6")); 
				workflowMatchMsgMap.put("step8", getStepList(nodeMsgObj,"step8")); 
				workflowMatchMsgMap.put("step9", getStepList(nodeMsgObj,"step9")); 
			} catch (JSONException e) {
				logger.warn("",e);
			}
		}
		return workflowMatchMsgMap;
	}
	
	private static List<String> getStepList(JSONObject nodeMsgObj,String stepKey) throws JSONException{ 
		List<String> msgList= new ArrayList<String>();
		if(!nodeMsgObj.isNull(stepKey) && nodeMsgObj.has(stepKey)){
			JSONArray nodeMsgArr= nodeMsgObj.getJSONArray(stepKey);
			for (int i = 0; i < nodeMsgArr.length(); i++) {
        		String myNodeMsg = nodeMsgArr.getString(i);
        		msgList.add(myNodeMsg);
			}
    	}
		return msgList;
	}
	
	/**
     * 获得process和state信息
     * @param processXml
     * @param processId
     * @param processTemplateId
     * @param currentNodeId
     * @return
     * @throws BPMException
     */
    public static Object[] doInitBPMProcessForCache(String matchRequestToken,String processXml,
    		String processId,String processTemplateId,String currentNodeId) throws BPMException {
    	WorkFlowMatchUserManager workflowMatchUserManager= (WorkFlowMatchUserManager)AppContext.getBean("workflowMatchUserManager");
    	ProcessTemplateManager processTemplateManager= (ProcessTemplateManager)AppContext.getBean("processTemplateManager");
    	ProcessManager processManager= (ProcessManager)AppContext.getBean("processManager");
    	BPMProcess process= null;
    	int state = -1;
    	BPMProcess cacheProcess= workflowMatchUserManager.getBPMProcessFromCacheRequestScope(matchRequestToken);
    	if(null==cacheProcess){
	        if(Strings.isNotBlank(processXml)){
	        	process = BPMProcess.fromXML(processXml);
	        }else{ 
	        	if(WorkflowUtil.isLong(processTemplateId) && !"0".equals(processTemplateId)  && !"-1".equals(processTemplateId)){//优先判断模板参数
	        		processXml = processTemplateManager.selectProcessTempateXml(processTemplateId);
	        		if(Strings.isNotBlank(processXml)){
		                process = BPMProcess.fromXML(processXml);
	        		}else{
	        			ProcessInRunningDAO runningProcess= processManager.getProcessInRunningDAO(processId);
	        			if(null!=runningProcess){
	        				try {
	        					process = runningProcess.getProcess();
	        				} catch (Throwable e) {
								logger.error("",e);
							}
	        				if ("start".equals(currentNodeId)) {
			        			state= runningProcess.getState();
			        		}
	        			}
	        		}
	        	}else if(WorkflowUtil.isLong(processId)){
	        		ProcessInRunningDAO runningProcess= processManager.getProcessInRunningDAO(processId);
        			if(null!=runningProcess){
        				try {
        					process = runningProcess.getProcess();
        				} catch (Throwable e) {
							logger.error("",e);
						}
        				if ("start".equals(currentNodeId)) {
		        			state= runningProcess.getState();
		        		}
        			}
	        	}
	        }
    	}else{
    		process= cacheProcess;
    		Integer cacheState= workflowMatchUserManager.getProcessStateFromCacheRequestScope(matchRequestToken);
    		if(null!=cacheState){
    			state= cacheState.intValue();
    		}
    	}
        if (null == process) {
            
            //"根据参数 processId[" + processId + "],processTemplateId[" + processTemplateId + "]查不到对应的流程XML"
            String msg = ResourceUtil.getString("workflow.label.notFindXML", processId, processTemplateId);
            throw new BPMException(msg);
        }
        workflowMatchUserManager.putProcessStateToCacheRequestScope(matchRequestToken, state);
        workflowMatchUserManager.putBPMProcessToCacheRequestScope(matchRequestToken, process);
        workflowMatchUserManager.putProcessXmlToCacheRequestScope(matchRequestToken, processXml);
        return new Object[]{process,state};
	}
	
    
    
    
    /**
     * 获取节点绑定的表单操作权限
     * 
     * @param node
     * @return
     *
     * @Since A8-V5 7.0
     * @Author      : xuqw
     * @Date        : 2018年4月13日下午5:25:44
     *
     */
    public static String getFormRightId(BPMAbstractNode node){
        
      //获取节点操作权限, 批处理， 系统默认字段用于分支判断的时候会匹配错误
        String rightId = "";
        if(node != null
                && !WorkflowUtil.isBlankNode(node)
                && !WorkflowUtil.isInformNode(node)
                ){
            
            //判断是否是加签只读
            String fr = node.getSeeyonPolicy().getFR();
            if(!"1".equals(fr)){
              //节点配置的表单视图和操作权限
                rightId = node.getSeeyonPolicy().getFormViewOperation();
            }
        }
        
        return rightId;
    }
    
    public static void doInitFormDataForCache(WorkflowBpmContext context, String formAppId, String rightId) throws BPMException {
        
        doInitFormDataForCache(context, formAppId, rightId, true);
    }
    
    
	/**
     * 获取表单数据
     * @param context
     * @param process
     * @throws BPMException
     */
    public static void doInitFormDataForCache(WorkflowBpmContext context, String formAppId, String rightId, boolean fromCache) throws BPMException {
    	String matchRequestToken= context.getMatchRequestToken();
    	WorkFlowMatchUserManager workFlowMatchUserManager= (WorkFlowMatchUserManager)AppContext.getBean("workflowMatchUserManager");
    	
    	Map<String,Object> bussinessFormData = null;
    	if(fromCache){
    	    bussinessFormData= workFlowMatchUserManager.getWorkflowFormDataFromCacheRequestScope(matchRequestToken);
    	}
    	
    	Map<String,WorkflowFormFieldBO> bussinessFormDataDef= workFlowMatchUserManager.getWorkflowFormDataDefFromCacheRequestScope(matchRequestToken);
    	if("collaboration".equals(context.getAppName()) 
                || "form".equals(context.getAppName())){//添加表单数据
    	    
            if(context.getFormData() != null && context.getFormData().equals(context.getMastrid())){//这种情况是有问题的，要特殊处理下才行
                context.setFormData(formAppId);
            }
            
          //设置是否是CAP4
            if(isLong(formAppId)){
                WorkflowFormDataMapManager formDataMapManager = WorkflowFormDataMapInvokeManager.getAppManager("form");
                if(formDataMapManager!=null){
                    ApplicationCategoryEnum catg = formDataMapManager.getCapType(formAppId);
                    if(ApplicationCategoryEnum.cap4Form.equals(catg)){
                        context.setCAP4(true);
                    }
                }
            }
        }
    	if(null!=bussinessFormData && !bussinessFormData.isEmpty()){
    	    context.setBusinessData(EventDataContext.CTP_FORM_DATA, bussinessFormData);
    	    context.setBusinessData(EventDataContext.CTP_FORM_DATA_DEF, bussinessFormDataDef);
    	}else{
	    	String formData= context.getFormData();
	    	boolean isFromForm = Strings.isNotBlank(formData);
	    	
	    	//流程仿真在这里重置了
	        String simulationId = context.getSimulationId();
	    	if(Strings.isNotBlank(simulationId)){
	    		WorkflowUtil.addSimulationDataDisplayName(context);
	        } else if (isFromForm) {//表单流程
	                if("collaboration".equals(context.getAppName()) 
	                        || "form".equals(context.getAppName())){//添加表单数据
	                    if(context.getFormData().equals(context.getMastrid())){//这种情况是有问题的，要特殊处理下才行
	                        context.setFormData(formAppId);
	                    }
	                    WorkflowUtil.addFormDataDisplayName(context, rightId);
	                }else if("edoc".equals(context.getAppName()) 
	                        || "sendEdoc".equals(context.getAppName())
	                        || "edocSend".equals(context.getAppName()) 
	                        || "signReport".equals(context.getAppName())
	                        || "edocSign".equals(context.getAppName())
	                        || "recEdoc".equals(context.getAppName())
	                        || "edocRec".equals(context.getAppName())){//添加公文数据
	                    WorkflowUtil.addEdocDataDisplayName(context);
	                }else{
	                    WorkflowUtil.addAppDataDisplayName(context);
	                }
	        } else if(context.isSysAutoFinishFlag()){
	            if("edoc".equals(context.getAppName()) 
	                    || "sendEdoc".equals(context.getAppName())
	                    || "edocSend".equals(context.getAppName()) 
	                    || "signReport".equals(context.getAppName())
	                    || "edocSign".equals(context.getAppName())
	                    || "recEdoc".equals(context.getAppName())
	                    || "edocRec".equals(context.getAppName())){//添加公文数据
	                WorkflowUtil.addEdocDataDisplayName(context);
	            }
	        }
	    	Object formDataObj= context.getBusinessData(EventDataContext.CTP_FORM_DATA);
	    	Object formDataObjDef= context.getBusinessData(EventDataContext.CTP_FORM_DATA_DEF);
	    	if(null!=formDataObj){
	    		workFlowMatchUserManager.putWorkflowFormDataToCacheRequestScope(matchRequestToken, (Map<String,Object>)formDataObj);
	    		workFlowMatchUserManager.putWorkflowFormDataDefToCacheRequestScope(matchRequestToken, (Map<String,WorkflowFormFieldBO>)formDataObjDef);
	    	}
    	}
	}

	/**
	 * @param context
	 */
	private static void addSimulationDataDisplayName(WorkflowBpmContext context) {
	 	if(context==null){
            return;
        }
	 	if(Strings.isBlank(context.getSimulationId())){
	 		return;
	 	}
	 	WorkflowSimulationManager workflowSimulationManager = (WorkflowSimulationManager)AppContext.getBean("workflowSimulationManager");
	 	Map<String,Object> formData  = 	workflowSimulationManager.getFormDataMap(context.getSimulationId());
	 	context.setBusinessData(EventDataContext.CTP_FORM_DATA, formData);
        
        Map<String,WorkflowFormFieldBO> formFieldDefMap= WorkflowFormDataMapInvokeManager.getAppManager("form").getFormFieldMap(context.getFormAppId());
        context.setBusinessData(EventDataContext.CTP_FORM_DATA_DEF, formFieldDefMap);
	}

	public static boolean hasAgent(String partyId) {
		V3xOrgMember member;
        try {
        	OrgManager orgManager= (OrgManager)AppContext.getBean("orgManager");
			member = orgManager.getMemberById(new Long(partyId));
			if(null!=member){
				if(!member.isValid()){//是否设置代理
					List<AgentModel> agentToList = MemberAgentBean.getInstance().getAgentModelToList(member.getId());
			        if(Strings.isEmpty(agentToList)){
			        	return false;
			        }
				}
				return true;
            }else{
           	 	return false;
            }
        }catch (Throwable e) {
       	 logger.warn("",e);
        }
        return false;
	}
	
	/**
     * 替换节点时，清除复制、粘贴的信息
     * @param process
     * @param activityId
     */
    public static void clearCopyNodeProperty(BPMProcess process, String activityId){
        BPMActivity currentActivity = process.getActivityById(activityId);
        String copyForm = currentActivity.getCopyFrom();
        if(Strings.isNotBlank(copyForm) && !"null".equals(copyForm) && !"undefined".equals(copyForm)){ //当前节点为粘贴替换节点
            BPMActivity copyedActivity = process.getActivityById(copyForm);
            if(copyedActivity != null){
                String pasteTo = copyedActivity.getPasteTo();
                if(Strings.isNotBlank(pasteTo) && !"null".equals(pasteTo) && !"undefined".equals(pasteTo)){
                    if(pasteTo.equals(activityId)){
                        copyedActivity.setPasteTo("");
                    } else if(pasteTo.indexOf(activityId) != -1){
                        pasteTo = pasteTo.replace("," + activityId, "").replace(activityId + ",", "");
                        copyedActivity.setPasteTo(pasteTo);
                    }
                }
                if(Strings.isBlank(copyedActivity.getPasteTo())){ //处理copyNumber
                    copyedActivity.setCopyNumber("");
                }
            }
            currentActivity.setCopyFrom("");
            currentActivity.setCopyNumber("");
            currentActivity.setPasteTo("");
        } else {//当前节点为复制节点
            String pasteTo = currentActivity.getPasteTo();
            if(Strings.isNotBlank(pasteTo) && !"null".equals(pasteTo) && !"undefined".equals(pasteTo)){
                String[] pasteActivityIds = pasteTo.split(",");
                for (String pasteActivityId : pasteActivityIds) {
                    BPMActivity pastedActivity = process.getActivityById(pasteActivityId);
                    if(pastedActivity != null){
                        pastedActivity.setCopyFrom("");
                        pastedActivity.setCopyNumber("");
                    }
                }
                currentActivity.setCopyFrom("");
                currentActivity.setCopyNumber("");
                currentActivity.setPasteTo("");
            }
        }
    }

	public static String getAppName(String appName) {
		if(ApplicationCategoryEnum.edocRec.name().equals(appName)
                || ApplicationCategoryEnum.edocSend.name().equals(appName)
                || ApplicationCategoryEnum.edocSign.name().equals(appName)
                || "sendEdoc".equals(appName) 
                || "recEdoc".equals(appName)
                || "signReport".equals(appName)){//公文种类较多，兼容处理下
            appName= "edoc";
        }else if("sendInfo".equals(appName)){
            appName= "info";
        }
		return appName;
	}
	
	/**
     * 解析分支条件
     * @param appName app
     * @param formApp 表单id
     * @param branchExpression 分支条件
     * @param fieldMap 表单值
     */
    public static String[] branchTranslateBranchExpression(String appName, String formApp ,String branchExpression,Map<String, WorkflowFormFieldBO> fieldMap)
            throws BPMException {
        //不能包含$字符
        if(branchExpression!=null && branchExpression.indexOf("$")>-1){
            return new String[]{"false",ResourceUtil.getString("workflow.branchTranslate.1")+":     $",""};
        }
        //获取到所有的表单域，转换分支条件时需要
        //如果表单Id不存在，那么也就不必校验表单单元格了。
        //如果分支条件表达式校验不通过，那么也没有必要校验表单单元格
        //判断表达式中的表达是否都在指定的表单中
        Long formAppId = null;
        boolean isEdocFlag = false;
        formApp = formApp.trim();
        if(ApplicationCategoryEnum.edocRec.name().equals(appName)
                || ApplicationCategoryEnum.edocSend.name().equals(appName)
                || ApplicationCategoryEnum.edocSign.name().equals(appName)
                || "sendEdoc".equals(appName) 
                || "recEdoc".equals(appName)
                || "signReport".equals(appName)){
        	if(WorkflowUtil.isLong(formApp) && !"0".equals(formApp) && !"-1".equals(formApp)){
            	isEdocFlag = true;
            }else{
            	logger.info("formApp:"+formApp);
            }
        }else{
        	if(WorkflowUtil.isLong(formApp) && !"0".equals(formApp) && !"-1".equals(formApp)){
        		formAppId = Long.valueOf(formApp);
            }else{
            	logger.info("formApp:"+formApp);
            }
        }
        String[] result = new String[4];
        result[2] = "";
        if (null == branchExpression || "".equals(branchExpression.trim()) || "null".equals(branchExpression.trim())
                || "undefined".equals(branchExpression.trim()) || null == appName || "".equals(appName.trim())
                || "null".equals(appName.trim()) || "undefined".equals(appName.trim())) {
            result[0] = "true";
            result[1] = ResourceUtil.getString("workflow.branchTranslate.6");//"无";
            return result;
        }
        //原始表达式
        String orignial = branchExpression;
        branchExpression = branchExpression.trim();
        String originalExpression = branchExpression;
        Expression e = ExpressionFactory.createExpression(branchExpression, fieldMap);
        e.validate(appName, formAppId, fieldMap);
        if (!e.isSuccess()) {
            result = new String[4];
            result[0] = "false";
            result[1] = e.getErrorMsg();
            //result[3] = e.translate(1, appName, formBean, fieldMap);
            return result;
        }
        branchExpression = e.translate(2, appName, formAppId, fieldMap);
        
        //如果是公文的话，转换公文枚举
        if(isEdocFlag){
        	String[] edocResult= translateEdocEnum(branchExpression, fieldMap);
        	branchExpression= edocResult[2];
        	if(edocResult[0] == "true"){
        		result[0] = edocResult[0];
                //枚举值不存在
                result[1] = edocResult[1];
        		return result;
        	}else{
        		Set<String> edocFieldSet= fieldMap.keySet();
        		for (String edocField : edocFieldSet) {
        			branchExpression= branchExpression.replaceAll(edocField, fieldMap.get(edocField).getDisplay());
				}
        	}
        }
        result[0] = "true";
        branchExpression= translateExpressionTitle(fieldMap,branchExpression);
        FormulaManager formulaManager= (FormulaManager)AppContext.getBean("formulaManager");
        List<Formula> systemVariablesList= formulaManager.getAllVariable(null);
        if(null!=systemVariablesList){
	        for (Formula formula : systemVariablesList) {
	        	if(null!=formula){
		        	String formulaName= formula.getFormulaName();
		        	String formulaDisplayName= formula.getFormulaAlias();
		        	branchExpression= branchExpression.replaceAll(formulaName, formulaDisplayName);
	        	}
			}
        }
        result[1] = branchExpression;
        if ("false".equals(result[0])) {
            return result;
        }
        
        String newTitle = branchExpression.replaceAll("<span[ 0-9A-Za-z,()=\"]+>", "")
                .replaceAll("</span>", "")
                .replace("&lt;", "<").replace("&gt;", ">");
        newTitle= translateExpressionTitle(fieldMap,newTitle);
        result[3] = newTitle;
        return result;
    }

    private static String[] translateEdocEnum(String branchExpression,Map<String, WorkflowFormFieldBO> fieldMap) {
    	String[] result= new String[]{"","",""};
    	String newBranchExpression= branchExpression;
    	String translateGroup = "";
    	Pattern p = Pattern.compile("([^\\s()\\[\\]!=<>&|\\^+*/%\\$#'\":;,?\\\\]+)\\s*(==|!=)\\s*(\\d+)");
    	Matcher m = p.matcher(branchExpression);
        StringBuffer sb = new StringBuffer();
    	while(m.find()){
    		String first = m.group(1);
    		String second = m.group(2);
    		String third = m.group(3);
    		
    		String srcFirst = first;
            
            if(first.contains("{")){
                first = first.replace("{", "").replace("}", "");
            }
    		
    		WorkflowFormFieldBO field = fieldMap.get(first);
    		if(field!=null && field.getEnumId()!=null && field.getEnumId()!=0L && field.getEnumId()!=-1L){
                //找枚举项
	            CtpEnumItem enumItem = null;
                try {
                	EnumManager enumManagerNew = (EnumManager)AppContext.getBean("enumManagerNew");
                    //公文枚举通通使用第一层枚举
                    enumItem = enumManagerNew.getCtpEnumItem(field.getEnumId(), 0, third);
                } catch (BusinessException e1) {
                    logger.error(e1);
                }
	            if (enumItem!=null) {
                    String label = enumItem.getShowvalue();
                    String newLabel = label;
                    if(enumItem.getI18n()!=null && enumItem.getI18n().intValue()==1){
                        newLabel = ResourceUtil.getString(newLabel);
                    }
                    if (newLabel != null && !"".equals(newLabel.trim())) {
                        third = newLabel;
                    } else {
                    	third = label;
                    }
	        		translateGroup = srcFirst + " " + second + " " + third;
	                m.appendReplacement(sb, translateGroup);
	            }else{
	                result[0] = "true";
                    //枚举值不存在
                    result[1] = ResourceUtil.getString("workflow.branchValidate.7",m.group(),third);
                    return result;
	            }
    		}
        }
        m.appendTail(sb);
        newBranchExpression = sb.toString();
        result[2]= newBranchExpression;
        return result;
	}

	private static String translateExpressionTitle(Map<String, WorkflowFormFieldBO> fieldMap,String expressionTitle) {
    	String newTitle= expressionTitle;
    	//鼠标放上去的分支转义需要加上一步，将表单的fieldName转换为display
        if(fieldMap!=null && fieldMap.size()>0){
            Pattern fieldPattern = Pattern.compile("field\\d+");
            Matcher fieldMatcher = fieldPattern.matcher(expressionTitle);
            StringBuffer sb = new StringBuffer("");
            while(fieldMatcher.find()){
            	String fgroup = fieldMatcher.group();
            	WorkflowFormFieldBO field = fieldMap.get(fgroup);
            	if(field!=null){
            		String display = field.getDisplay();
            		fieldMatcher.appendReplacement(sb, display);
            	}else{
            		fieldMatcher.appendReplacement(sb, fgroup);
            	}
            }
            fieldMatcher.appendTail(sb);
            newTitle = sb.toString();
        }
        return newTitle;
	}

    /**
     * 解析表单控件,仅工作流内部使用
     * 
     * @param partyId
     * @return [类型， 表单字段名， 表单实际字段名]
     */
    public static String[] parseFormField(String partyId) {

        // 3.5中matchMesssage就是formField的display,5.0中是FormField|display
        // 因为3.5中只支持选人的表单控件，5.0中人、部门都支持。

        int index = partyId.indexOf("|");
        int firstIndex = partyId.indexOf("@");
        int sencondIndex = partyId.indexOf("#");
        int thirdIndex = partyId.lastIndexOf("#");
        String role = null;
        String entityType = "Member";
        String fieldDisplayName = partyId;// 老的还是用DisplayName,新的用filedName
        String formFieldName = fieldDisplayName;
        if (firstIndex > -1 && sencondIndex > -1) {
            entityType = partyId.substring(0, firstIndex);
            fieldDisplayName = partyId.substring(firstIndex + 1, sencondIndex);
            formFieldName = partyId.substring(sencondIndex + 1);
            if (sencondIndex != thirdIndex) {
                role = partyId.substring(thirdIndex + 1);
            }
        } else if (index > -1) {// 兼容5.0老格式,避免bug
            entityType = partyId.substring(0, index);
            fieldDisplayName = partyId.substring(index + 1);
        }

        return new String[] { entityType, fieldDisplayName, role, formFieldName };
    }
    
    /**
     * 将xml中的id替换成新的id
     * @param xml
     * @param old2NewIdMap
     * @return
     */
    public static String replaceWorkflowXmlId(String xml,Map<String,String> old2NewIdMap){
    	BPMProcess process= BPMProcess.fromXML(xml);
        if(process!=null){
            List<BPMAbstractNode> nodes= process.getActivitiesList();
            BPMStart startNode= (BPMStart)process.getStart();
            List<BPMAbstractNode> allNodesWithStart = new ArrayList<BPMAbstractNode>(nodes.size() + 1);
            allNodesWithStart.addAll(nodes);
            allNodesWithStart.add(startNode);
            
            for (BPMAbstractNode node : allNodesWithStart) {
                String nodeFormAppId= node.getSeeyonPolicy().getFormApp();
                if(null!=old2NewIdMap.get(nodeFormAppId)){
                    node.getSeeyonPolicy().setFormApp(old2NewIdMap.get(nodeFormAppId));
                }
                
                String formViewOperation = node.getSeeyonPolicy().getFormViewOperation();
                String[] allViews = formViewOperation.split("[_]");
                StringBuilder newFormViewOperation = new StringBuilder();
                for(int k = 0; k < allViews.length; k++){
                    
                    String[] viewInfo = allViews[k].split("[.]");
                    String viewid = "";
                    String operationId = null;
                    if(viewInfo.length > 1){
                        viewid = viewInfo[0];
                        operationId = viewInfo[1];
                    }else {
                        operationId = viewInfo[0];
                    }
                    if(null != old2NewIdMap.get(viewid)){
                        viewid = old2NewIdMap.get(viewid);
                    }
                    if(null != old2NewIdMap.get(operationId)){
                        operationId = old2NewIdMap.get(operationId);
                    }
                    
                    //拼接
                    if(newFormViewOperation.length() > 0){
                        newFormViewOperation.append("_");
                    }
                    if(!WorkflowUtil.isBlank(viewid)){
                        newFormViewOperation.append(viewid).append(".");
                    }
                    newFormViewOperation.append(operationId);
                }
                node.getSeeyonPolicy().setFormViewOperation(newFormViewOperation.toString());
                node.getSeeyonPolicy().setForm("");
                node.getSeeyonPolicy().setOperationName("");
            }
            xml= process.toXML(null,true);
            if(null!=old2NewIdMap){//对一些老的ID做统一的替换 
                Set<String> oldIds= old2NewIdMap.keySet();
                for (String oldId : oldIds) {
                	xml= xml.replaceAll(oldId, old2NewIdMap.get(oldId));
                }
            }
        }
    	return xml;
    }
    
    /**
     * 
     * @Title: deleteSuperNodeInProcess   
     * @Description: 删除流程中的超级节点
     * @param processXml
     * @throws BPMException      
     * @return: void  
     * @date:   2018年9月3日 下午7:41:21
     * @author: xusx
     * @since   V7.0SP2	       
     * @throws
     */
    public static String deleteSuperNodeInProcess(String processXml) throws BPMException {
        
		if(Strings.isBlank(processXml)){
			return null;
		}
		
		BPMProcess process = BPMProcess.fromXML(processXml);
        List<BPMAbstractNode>  list = process.getActivitiesList();
        List<String> superNodeIds = new ArrayList<String>();
        if(Strings.isEmpty(list)){
        	return processXml;
        }
        
        for(BPMAbstractNode node : list){
        	if(NodeType.end.name().equals(node.getNodeType().name()) 
					||  NodeType.split.name().equals(node.getNodeType().name())
					||  NodeType.join.name().equals(node.getNodeType().name()) 
					||  WorkflowUtil.isBlankNode(node)){
				continue;
			}
        	BPMParticipant party = ((BPMActor) (node.getActorList().get(0))).getParty();
			if(ObjectName.WF_SUPER_NODE.equals(party.getType().id)){
				superNodeIds.add(node.getId());
			}
        }
        
        if(Strings.isEmpty(superNodeIds)){
        	return processXml;
        }
        
        BPMChangeMessageVO vo = new BPMChangeMessageVO();
        vo.setDeleteAcitivityIdList(superNodeIds);
        vo.setProcess(process);
        
		vo = BPMChangeUtil.deleteNode(vo);
		
		String newWorklfowXML= vo.getProcess().toXML();
		
		return newWorklfowXML;
	}
}

