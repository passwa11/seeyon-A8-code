package com.seeyon.apps.govdoc.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.edoc.bo.GovdocTemplateDepAuthBO;
import com.seeyon.apps.govdoc.constant.GovdocEnum.GovdocWorkflowTypeEnum;
import com.seeyon.apps.govdoc.constant.GovdocEnum.TransferStatus;
import com.seeyon.apps.govdoc.listener.GovdocWorkflowEventListener;
import com.seeyon.apps.govdoc.po.GovdocExchangeDetail;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormDataMasterBean;
import com.seeyon.ctp.form.po.GovdocTemplateDepAuth;
import com.seeyon.ctp.form.util.Enums.FormType;
import com.seeyon.ctp.form.util.FormConstant;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.engine.enums.BPMSeeyonPolicySetting;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.vo.BPMSeeyonPolicyVO;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.system.signet.domain.V3xDocumentSignature;
import com.seeyon.v3x.system.signet.manager.SignetManager;

import net.joinwork.bpm.definition.BPMAbstractNode;
import net.joinwork.bpm.definition.BPMEnd;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMStart;
import net.joinwork.bpm.definition.BPMTransition;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

/**
 * 新公文工具类
 * 
 * @author 唐桂林
 * 
 */
public class GovdocUtil {
	
	private static final Log LOGGER = LogFactory.getLog(GovdocUtil.class);
    private static CAPFormManager capFormManager;
    private static WorkflowApiManager wapi;
    
    /**
	 * 新老公文edocType与govdocType互转
	 * @param edocType
	 * @return
	 */
	public static String getCategoryBySubApp(int subApp, String configItem) {
		if (subApp == ApplicationSubCategoryEnum.edoc_fawen.key()) {
			return EnumNameEnum.edoc_new_send_permission_policy.name();
		} else if (subApp == ApplicationSubCategoryEnum.edoc_shouwen.key()) {
			return EnumNameEnum.edoc_new_rec_permission_policy.name();
		} else if (subApp == ApplicationSubCategoryEnum.edoc_qianbao.key()) {
			return EnumNameEnum.edoc_new_qianbao_permission_policy.name();
		} else if (subApp == ApplicationSubCategoryEnum.edoc_jiaohuan.key()) {
			if("zhihui".equals(configItem)) {
				return EnumNameEnum.edoc_new_rec_permission_policy.name();
			}
			return EnumNameEnum.edoc_new_change_permission_policy.name();
		} else if (subApp == ApplicationSubCategoryEnum.old_edocSend.key()) {
			return EnumNameEnum.edoc_send_permission_policy.name();
		} else if (subApp == ApplicationSubCategoryEnum.old_edocRec.key()) {
			return EnumNameEnum.edoc_rec_permission_policy.name();
		} else if (subApp == ApplicationSubCategoryEnum.old_edocSign.key()) {
			return EnumNameEnum.edoc_qianbao_permission_policy.name();
		}
		return EnumNameEnum.edoc_new_send_permission_policy.name();
	}
    
	/**
	 * 新老公文edocType与govdocType互转
	 * @param edocType
	 * @return
	 */
	public static Integer getGovdocTypeByEdocType(int edocType) {
		int govdocType = 0;
		if (edocType == 0) {
			govdocType = ApplicationSubCategoryEnum.edoc_fawen.key();
		} else if (edocType == 1) {
			govdocType = ApplicationSubCategoryEnum.edoc_shouwen.key();
		} else if (edocType == 2) {
			govdocType = ApplicationSubCategoryEnum.edoc_qianbao.key();
		}
		return govdocType;
	}

	/**
	 * 新老公文edocType与govdocType互转
	 * @param govdocType
	 * @return
	 */
	public static Integer getEdocTypeByGovdoc(int govdocType) {
		int edocType = 0;
		if (govdocType == ApplicationSubCategoryEnum.edoc_fawen.key()) {
			edocType = 0;
		} else if (govdocType == ApplicationSubCategoryEnum.edoc_shouwen.key()) {
			edocType = 1;
		} else if (govdocType == ApplicationSubCategoryEnum.edoc_qianbao.key()) {
			edocType = 2;
		} else if (govdocType == ApplicationSubCategoryEnum.edoc_jiaohuan.key()) {
			edocType = 3;
		}
		return edocType;
	}
	
	/**
	 * 根据模板类型获取govdocType
	 * @param moduleType
	 * @return
	 */
	public static Integer getGovdocTypeByModuleType(int moduleType) {
		int govdocType = 1;
		if (moduleType == ApplicationCategoryEnum.govdocSend.getKey()) {
			govdocType = ApplicationSubCategoryEnum.edoc_fawen.getKey();
	    } else if (moduleType == ApplicationCategoryEnum.govdocRec.getKey()) {
	    	govdocType = ApplicationSubCategoryEnum.edoc_shouwen.getKey();
	    } else if (moduleType == ApplicationCategoryEnum.govdocExchange.getKey()) {
	    	govdocType = ApplicationSubCategoryEnum.edoc_jiaohuan.getKey();
	    } else if (moduleType == ApplicationCategoryEnum.govdocSign.getKey()) {
	    	govdocType = ApplicationSubCategoryEnum.edoc_qianbao.getKey();
	    }
		return govdocType;
	}
	
	/**
	 * 根据模板类型获取分送状态
	 * @param moduleType
	 * @return
	 */
	public static Integer getTransferStatusByModuleType(int moduleType) {
		Integer transferStatus = TransferStatus.defaultStatus.getKey();
		if (moduleType == ApplicationCategoryEnum.govdocExchange.getKey()) {
	    	transferStatus = TransferStatus.waitSigned.getKey();
		}
		return transferStatus;
	}
	
	public static String getNPSAppName(int govdocType, int edocType) {
		String subAppName = ApplicationCategoryEnum.edoc.name();
		if(govdocType == 0) {//老公文
			 if(edocType == 0) {
				 subAppName = ApplicationCategoryEnum.edocSend.name();
			 }else if(edocType == 1) {
				 subAppName = ApplicationCategoryEnum.edocRec.name();
			 }else if(edocType == 2) {
				 subAppName = ApplicationCategoryEnum.edocSign.name();
			 }
		} else {//新公文
			 if(govdocType == 1) {
				 subAppName = ApplicationCategoryEnum.govdocSend.name();
			 } else if(govdocType == 2) {
				 subAppName = ApplicationCategoryEnum.govdocRec.name();
			 } else if(govdocType == 3) {
				 subAppName = ApplicationCategoryEnum.govdocSign.name();
			 }
		}
		return subAppName;
	}
	
	/**
	 * 公文正文类型与字段串互转
	 * @param contentType
	 * @return
	 */
	public static String getBodyType(int contentType) {
		String govdocBodyType = new String();
		if (contentType == 10) {
			govdocBodyType = MainbodyType.HTML.name();
		} else if (contentType == 41) {
			govdocBodyType = MainbodyType.OfficeWord.name();
		} else if (contentType == 42) {
			govdocBodyType = MainbodyType.OfficeExcel.name();
		} else if (contentType == 43) {
			govdocBodyType = MainbodyType.WpsWord.name();
		} else if (contentType == 44) {
			govdocBodyType = MainbodyType.WpsExcel.name();
		} else if (contentType == 45) {
			govdocBodyType = MainbodyType.Pdf.name();
			// 流版组件改造
		} else if (contentType == 46) {
			govdocBodyType = MainbodyType.Ofd.name();
		} else {
			govdocBodyType = MainbodyType.OfficeWord.name();
		}
		return govdocBodyType;
	}
	
	/**
	 * 公文正文类型与字段串互转
	 * @param bodyType
	 * @return
	 */
	public static int getContentType(String bodyType) {
		if (MainbodyType.HTML.name().equals(bodyType) || "10".equals(bodyType)) {
			return MainbodyType.HTML.getKey();
		} else if (MainbodyType.OfficeWord.name().equals(bodyType) || "41".equals(bodyType)) {
			return MainbodyType.OfficeWord.getKey();
		} else if (MainbodyType.OfficeExcel.name().equals(bodyType) || "42".equals(bodyType)) {
			return MainbodyType.OfficeExcel.getKey();
		} else if (MainbodyType.WpsWord.name().equals(bodyType) || "43".equals(bodyType)) {
			return MainbodyType.WpsWord.getKey();
		} else if (MainbodyType.WpsExcel.name().equals(bodyType) || "44".equals(bodyType)) {
			return MainbodyType.WpsExcel.getKey();
		} else if (MainbodyType.Pdf.name().equals(bodyType) || "45".equals(bodyType)) {
			return MainbodyType.Pdf.getKey();
		} else if (MainbodyType.Ofd.name().equals(bodyType) || "46".equals(bodyType)) {
			return MainbodyType.Ofd.getKey();
		}
		return MainbodyType.OfficeWord.getKey();
	}

	/**
	 * 公文正文类型与字段串互转
	 * @param contentType
	 * @return
	 */
	public static String getBodyTypeText(int contentType) {
		String govdocBodyTypeText = "正文";
		if (contentType == 41) {
			govdocBodyTypeText = ResourceUtil.getString(MainbodyType.OfficeWord.getText());
		} else if (contentType == 45) {
			govdocBodyTypeText = ResourceUtil.getString(MainbodyType.Pdf.getText());
			// 流版组件改造
		} else if (contentType == 46) {
			govdocBodyTypeText = ResourceUtil.getString(MainbodyType.Ofd.getText());
		}
		return govdocBodyTypeText;
	}
	
	/**
	 * 判断文单是否为公文单
	 * @param formType
	 * @return
	 */
	public static boolean isGovdocForm(int formType) {
		return formType == FormType.govDocSignForm.getKey() 
				|| formType == FormType.govDocSendForm.getKey()
				|| formType == FormType.govDocReceiveForm.getKey() 
				|| formType == FormType.govDocExchangeForm.getKey();
	}

	/**
	 * 公文文号表单类型名与元素名互转
	 * @param markType
	 * @return
	 */
	public static Integer getMarkTypeValueByType(String markType) {
		if ("edocDocMark".equals(markType) || "doc_mark".equals(markType) || "0".equals(markType)) {
			return 0;
		} else if ("edocInnerMark".equals(markType) || "serial_no".equals(markType) || "1".equals(markType)) {
			return 1;
		} else if ("edocSignMark".equals(markType) || "sign_mark".equals(markType) || "2".equals(markType)) {
			return 2;
		}
		return null;
	}

	/**
	 * 按文号长度在流水号前补0
	 * @param number
	 * @param length
	 * @return
	 */
	public static String getNumberByFormat(int number, int length) {
		String number_str = "";
		String number_s = String.valueOf(number);
		for (int i = number_s.length(); i < length; i++) {
			number_str += "0";
		}
		number_str += number;
		return number_str;
	}

	/**
	 * 特殊字符转化
	 * @param str
	 * @return
	 */
	public static String convertSpecialChat(String str) {
		if (Strings.isNotBlank(str)) {
			StringBuilder buffer = new StringBuilder();
			for (int i = 0; i < str.length(); i++) {
				if (str.charAt(i) == '\'') {
					buffer.append("\\'");
				} else {
					buffer.append(str.charAt(i));
				}
			}
			str = SQLWildcardUtil.escape(buffer.toString());
		}
		return str;
	}
	
	/**
	 * 后端弹出Alert
	 * @param response
	 * @param msg
	 * @throws IOException
	 */
	public static void newCollAlertAndBack(HttpServletResponse response, String msg) throws IOException {
		PrintWriter out = response.getWriter();
		out.println("<script>");
		out.println("alert('" + msg + "');");
		out.print("parent.window.close();");
		out.print("parent.window.history.back();");
		out.println("</script>");
		out.flush();
	}
	
	/**
	 * 后端弹出Alert
	 * @param response
	 * @param msg
	 * @throws IOException
	 */
	public static void newCollAlert(HttpServletResponse response, String msg) throws IOException {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		out.println("<script>");
		out.println("alert('" + msg + "');");
		out.print("parent.window.close();");
		out.println("</script>");
		out.flush();
	}
	
	/**
	 * 后端弹出Alert
	 * @param response
	 * @param msg
	 * @throws IOException
	 */
	public static void webAlertAndClose(HttpServletResponse response,String msg) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<script>");
        out.println("alert('"+ StringEscapeUtils.escapeJavaScript(msg)+"');");
        out.println("if(window.parentDialogObj && window.parentDialogObj['dialogDealColl']){");
        out.println(" window.parentDialogObj['dialogDealColl'].close();");
        out.println("}else if(window.dialogArguments){"); //弹出
        out.println("  if(window.dialogArguments.parentDialogObj){");
        out.println("    try{window.dialogArguments.parentDialogObj.close();}catch(e){}");
        out.println("  }else{");
        out.println("    window.close();");
        out.println("  }");
        out.println("}else{");
        out.println(" window.close();");
        out.println("}");
        out.println("</script>");
    }
	
	/**
	 * 后端弹出Alert
	 * @param response
	 * @param msg
	 * @param dialogId
	 * @throws IOException
	 */
	public static void webAlertAndCloseDialog(HttpServletResponse response, String msg, String dialogId) throws IOException {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		out.println("<script>");
		out.println("alert('" + StringEscapeUtils.escapeJavaScript(msg) + "');");
		out.println("var box = window.parent.document.getElementById('" + dialogId + "');");
		out.println("var mask = window.parent.document.getElementById('" + dialogId + "_mask');");
		out.println("if(mask){mask.parentNode.removeChild(mask);box.parentNode.removeChild(box);}");
		out.println("else if(window.parentDialogObj && window.parentDialogObj['dialogDealColl']){");
		out.println(" window.parentDialogObj['dialogDealColl'].close();");
		out.println("}else if(window.dialogArguments){"); // 弹出
		out.println("  if(window.dialogArguments.parentDialogObj){");
		out.println("    try{window.dialogArguments.parentDialogObj.close();}catch(e){}");
		out.println("  }else{");
		out.println("    window.close();");
		out.println("  }");
		out.println("}else{");
		out.println(" window.close();");
		out.println("}");
		out.println("</script>");
	}	

	/**
	 * 替换字符串并让它的下一个字母为大写
	 * @param srcStr
	 * @param org
	 * @param ob
	 * @return
	 */
	public static String replaceUnderlineAndfirstToUpper(String srcStr, String org) {
		String newString = "";
		int first = 0;
		while (srcStr.indexOf(org) != -1) {
			first = srcStr.indexOf(org);
			if (first != srcStr.length()) {
				newString = newString + srcStr.substring(0, first);
				srcStr = srcStr.substring(first + org.length(), srcStr.length());
				srcStr = firstCharacterToUpper(srcStr);
			}
		}
		newString = newString + srcStr;
		return newString;
	}

	/**
	 * 首字母大写
	 * @param srcStr
	 * @return
	 */
	private static String firstCharacterToUpper(String srcStr) {
		return srcStr.substring(0, 1).toUpperCase() + srcStr.substring(1);
	}

	/**
	 * SQL脚本处理
	 * @param str
	 * @return
	 */
	public static String getSQLStr(String str) {
		if (Strings.isNotBlank(str)) {
			str = str.replaceAll(String.valueOf((char) 160), String.valueOf((char) 32));
			str = SQLWildcardUtil.escape(str);
		}
		return str;
	}

	public static List<Long> getIdList(String idstr) {
		List<Long> idList = new ArrayList<Long>();
		if(Strings.isNotBlank(idstr)) {
			String[]  idArr = idstr.split(",");
			for (String string : idArr) {
				if(Strings.isNotBlank(string)) {
					idList.add(Long.valueOf(string));
				}
			}	
		}
		return idList;
	}
	
	public static Map<String,String> cloneMap(Map<String,String> sourceMap) {
		if(sourceMap == null) {
			return null;
		}
		Set<String> keys = sourceMap.keySet();
		Map<String,String> newMap = new HashMap<String,String>();
		for(String key : keys) {
			newMap.put(key,sourceMap.get(key));
		}
		return newMap;
	}
	
	/**
	 * List分页
	 * @param targe
	 * @param size
	 * @return
	 */
	public static List<List<Long>> createList(List<Long> targe, int size) {
		List<List<Long>> listArr = new ArrayList<List<Long>>();
		// 获取被拆分的数组个数
		int arrSize = targe.size() % size == 0 ? targe.size() / size : targe.size() / size + 1;
		for (int i = 0; i < arrSize; i++) {
			List<Long> sub = new ArrayList<Long>();
			// 把指定索引数据放入到list中
			for (int j = i * size; j <= size * (i + 1) - 1; j++) {
				if (j <= targe.size() - 1) {
					sub.add(targe.get(j));
				}
			}
			listArr.add(sub);
		}
		return listArr;
	}
	
	/**
	 * 设置govdocExchangeDetail扩展字段值
	 */
	public static void setExtProperty(GovdocExchangeDetail detail,Map<String,Object> map) {
		if(map != null) {
			detail.setExtAttr(XMLCoder.encoder(map));
		}
	}
	
	/**
	 * 获取govdocExchangeDetail扩展字段值
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> getExtProperty(GovdocExchangeDetail detail) {
		return  (Map<String,Object>) (Strings.isBlank(detail.getExtAttr()) ? new HashMap<String,Object>() : (Map<String,Object>) XMLCoder.decoder(detail.getExtAttr()));
	}
	
	public static boolean isExchangeSendPendingList(String listType) {
		return "listExchangeSendPending".equals(listType);
	}
	
	public static boolean isExchangeSignList(String listType) {
		return ("listExchangeSignDone".equals(listType) || "listExchangeFallback".equals(listType) || "listExchangeSignPending".equals(listType));
	}
	
	public static boolean isGovdocWfOld(Integer app, Integer subApp) {
		return app==ApplicationCategoryEnum.edoc.key() && 
					(subApp.intValue() == ApplicationSubCategoryEnum.old_edocSend.key() 
						|| subApp.intValue() == ApplicationSubCategoryEnum.old_edocRec.key()
						|| subApp.intValue() == ApplicationSubCategoryEnum.old_edocSign.key());
	}
	
	public static boolean isGovdocWf(Integer app, Integer subApp) {
		return app==ApplicationCategoryEnum.edoc.key() && 
					(subApp.intValue() == ApplicationSubCategoryEnum.edoc_fawen.key() 
						|| subApp.intValue() == ApplicationSubCategoryEnum.edoc_qianbao.key()
						|| subApp.intValue() == ApplicationSubCategoryEnum.edoc_shouwen.key()
						|| subApp.intValue() == ApplicationSubCategoryEnum.edoc_jiaohuan.key());
	}
	
	public static List<V3xOrgMember> getPersonNextNode(FormBean formBean,FormDataMasterBean dataMasterBean,User user,CtpTemplate ctpTemplate) throws BPMException{
		List<V3xOrgMember> listmember = new ArrayList<V3xOrgMember>();
		WorkflowBpmContext context = new WorkflowBpmContext();
		context.setAppName(ModuleType.edoc.name());
		context.getBusinessData().put("subAppName", GovdocWorkflowTypeEnum.formedoc.name());
        context.setDebugMode(false);
        context.setFormData(""+formBean.getId());
        context.setMastrid(""+dataMasterBean.getId());
        //设置正文内容，用来发送邮件的时候显示正文内容
        context.setStartUserId(String.valueOf(user.getId()));
        context.setStartUserName(user.getName());
        context.setBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_MEMBER_ID, user.getId());
        context.setStartAccountId(String.valueOf(user.getLoginAccount()));
        context.setStartAccountName("seeyon");
        context.setVersion("2.0");
        
        WorkflowApiManager wapi = (WorkflowApiManager)AppContext.getBean("wapi");
        String xml1 = wapi.selectWrokFlowTemplateXml(ctpTemplate.getWorkflowId().toString());
        BPMProcess bpmProcess = BPMProcess.fromXML(xml1);
        context.setProcess(bpmProcess);
        List<BPMTransition> bpmTransitions = bpmProcess.getLinks();
        for (BPMTransition bpmTransition : bpmTransitions) {
	       	 BPMAbstractNode node = bpmTransition.getFrom();
	       	 if(node instanceof BPMStart){//如果是发起节点
	       		BPMAbstractNode nextNode = bpmTransition.getTo();
	       		if("split".equals(nextNode.getName())){
	        		List<BPMTransition> bpmTransitions2 = nextNode.getDownTransitions();
	        		for (BPMTransition bpmTransitio : bpmTransitions2) {
			       		try {
								listmember.addAll(wapi.getUserList("", (BPMHumenActivity)bpmTransitio.getTo(),context,true));
							} catch (Exception e) {
								LOGGER.error(e);
								//e.printStackTrace();
							}
			       	}
	        	}else{
		       		if(nextNode instanceof BPMHumenActivity || nextNode instanceof BPMEnd){//如果当前节点不是起始节点
		       			try {
								listmember.addAll(wapi.getUserList("", (BPMHumenActivity)nextNode,context,true));
							} catch (Exception e) {
								LOGGER.error(e);
								//e.printStackTrace();
							}
		       		}
	        	}
	        }
		}
        return listmember;
	}
	
	public static List<Integer> getSubAppByEdocType(int edocType){
		List<Integer> subApp = new ArrayList<Integer>();
		if (edocType == 0) {
			subApp.add(ApplicationSubCategoryEnum.edoc_fawen.key());
			subApp.add(ApplicationSubCategoryEnum.old_edocSend.key());
			//subApp.add(ApplicationSubCategoryEnum.old_exSend.key());
		} else if (edocType == 1) {
			subApp.add(ApplicationSubCategoryEnum.edoc_shouwen.key());
			subApp.add(ApplicationSubCategoryEnum.old_edocRec.key());
			//subApp.add(ApplicationSubCategoryEnum.old_exSign.key());
			//subApp.add(ApplicationSubCategoryEnum.old_edocRecDistribute.key());
		} else if (edocType == 2) {
			subApp.add(ApplicationSubCategoryEnum.edoc_qianbao.key());
			subApp.add(ApplicationSubCategoryEnum.old_edocSign.key());
		}
		return subApp;
	}
	
	public static boolean isH5() {
        Object tag = AppContext.getThreadContext(FormConstant.h5Tag);
        boolean h5Tag = (tag==null)?false:(Boolean)tag;//预留h5参数
        return h5Tag;
    }
	/**
	 * 复制正文中的签章
	 * @param fileManager
	 * @param signetManager
	 * @param oldFileId
	 * @param newFileId
	 * @throws Exception
	 */
    public static void copySignet(FileManager fileManager,SignetManager signetManager,long oldFileId,long newFileId) throws Exception{

		V3XFile contentFile = fileManager.getV3XFile(oldFileId);
		List<V3xDocumentSignature> signlist =signetManager.findDocumentSignatureByDocumentId(contentFile.getId().toString());
		if(signlist!=null&&signlist.size()>0){
			for (V3xDocumentSignature v3xDocumentSignature : signlist) {
				v3xDocumentSignature.setId(UUIDLong.longUUID());
				v3xDocumentSignature.setRecordId(String.valueOf(newFileId));
				signetManager.save(v3xDocumentSignature);
			}
		}
    }
    
	public static boolean canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType dealType,EdocSummary summary){
		String mergeDealType = "";
		if(summary!=null){
			mergeDealType = summary.getMergeDealType();
		}
		if(Strings.isNotBlank(mergeDealType)){
			String mergeDealTypeValue= dealType.getValue();
			Map<String,String> mergeDealTypeMap  = (Map<String, String>) JSONUtil.parseJSONString(mergeDealType);
			if(null!=mergeDealTypeMap && !mergeDealTypeMap.isEmpty()){
				return mergeDealTypeValue.equals(mergeDealTypeMap.get(dealType.name()));
			}
		}
		return false;
	}
	
	public static GovdocTemplateDepAuthBO toGovdocTemplateDepAuthBO(GovdocTemplateDepAuth auth){
		GovdocTemplateDepAuthBO bo = new GovdocTemplateDepAuthBO();
		bo.setId(auth.getId());
		bo.setAccountId(auth.getAccountId());
		bo.setAuthType(auth.getAuthType());
		bo.setOrgId(auth.getOrgId());
		bo.setOrgName(auth.getOrgName());
		bo.setOrgType(auth.getOrgType());
		bo.setTemplateId(auth.getTemplateId());
		return bo;
	}
	
	public static GovdocTemplateDepAuth toGovdocTemplateDepAuth(GovdocTemplateDepAuthBO bo){
		GovdocTemplateDepAuth auth = new GovdocTemplateDepAuth();
		auth.setId(bo.getId());
		auth.setAccountId(bo.getAccountId());
		auth.setAuthType(bo.getAuthType());
		auth.setOrgId(bo.getOrgId());
		auth.setOrgName(bo.getOrgName());
		auth.setOrgType(bo.getOrgType());
		auth.setTemplateId(bo.getTemplateId());
		return auth;
	}
    /**
     * 产生最终标题
     *
     * @param template
     * @param summary
     * @param sender
     * @return
     * @throws BusinessException
     */
    public static String makeSubject4NewWF(CtpTemplate template, EdocSummary summary, User sender) throws BusinessException{
        //String  subject  = ResourceUtil.getString("collaboration.newflow.fire.subject", template.getSubject() + "(" + sender.getName() + " " + Datetimes.formatDatetimeWithoutSecond(summary.getCreateDate()) + ")");
        String  subject  =  template.getSubject() + "(" + sender.getName() + " " + Datetimes.formatDatetimeWithoutSecond(summary.getCreateTime()) + ")";
        if(Strings.isBlank(template.getColSubject())){
            return subject;
        }

        if(Strings.isNotBlank(template.getColSubject())){
            WorkflowApiManager wapi = getWorkflowApiManager();
            CAPFormManager capFormManager = getCapFormManager();
            BPMSeeyonPolicyVO startPolicy  = wapi.getStartNodeFormPolicy(template.getWorkflowId());
            
            if(startPolicy != null){
                
                String subjectForm = capFormManager.getCollSubjuet(summary.getFormAppid(), template.getColSubject(), summary.getFormRecordid(), true);
                
                LOGGER.info(AppContext.currentUserLoginName()+"，表单接口,协同标题生成：Param:appid:"+summary.getFormAppid()+",template.getColSubject()："+template.getColSubject()+",recordId:"+summary.getFormRecordid());
                
                //转移换行符，标题中不能用换行符
                subjectForm = Strings.toText(subjectForm);
                
                if (Strings.isBlank(subjectForm)) {
                    subjectForm = "{" + ResourceUtil.getString("collaboration.subject.default") + "}";
                }
                
                subject = subjectForm;
                subject = Strings.toText(subject);
                if (subject.length() > 300) {
                    subject = subject.substring(0, 295) + "...";
                }
                LOGGER.info("最终标题："+subject);
            }
        }
        return subject;
    }
    public static String makeSubject(CtpTemplate template, EdocSummary summary, User sender) throws BusinessException{
        if(template == null || Strings.isBlank(template.getColSubject())){
            return summary.getSubject();
        }
        String subject = summary.getSubject();
        if(Strings.isNotBlank(template.getColSubject())){
            WorkflowApiManager wapi = getWorkflowApiManager();
            CAPFormManager capFormManager = getCapFormManager();
            BPMSeeyonPolicyVO startPolicy = wapi.getStartNodeFormPolicy(template.getWorkflowId());
            if(startPolicy != null){
                
            	LOGGER.info(AppContext.currentUserLoginName()+"，表单接口,协同标题生成：Param:appid:"+summary.getFormAppid()+",template.getColSubject()："+template.getColSubject()+",recordId:"+summary.getFormRecordid());
                subject = capFormManager.getCollSubjuet(summary.getFormAppid(), template.getColSubject(), summary.getFormRecordid(), false);
                LOGGER.info("协同标题生成成功, 协同ID=："+summary.getId());
                //转移换行符，标题中不能用换行符
                subject = Strings.toText(subject);
                if (subject.length() > 300) {
                    subject = subject.substring(0, 295) + "...";
                }
            }
        }
        return subject;
    }
	public static boolean isForm(int bodyType){
	    return WFComponentUtil.isForm(bodyType);
	}
	public static boolean isForm(String bodyType){
        return WFComponentUtil.isForm(bodyType);
    }
    public static boolean isNotBlank(String val) {
        return WFComponentUtil.isNotBlank(val);
    }
    private static WorkflowApiManager getWorkflowApiManager() {
        if (wapi == null) {
            wapi = (WorkflowApiManager) AppContext.getBean("wapi");
        }
        return wapi;
    }
    private static CAPFormManager getCapFormManager(){
        if(capFormManager == null){
            capFormManager = (CAPFormManager)AppContext.getBean("capFormManager");
        }
        return capFormManager;
    }
}
