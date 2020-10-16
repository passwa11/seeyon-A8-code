package com.seeyon.v3x.edoc.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormDataMasterBean;
import com.seeyon.ctp.form.util.infopath.ElementUtil.GovDocElement;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.system.signet.domain.V3xHtmDocumentSignature;
import com.seeyon.v3x.system.signet.enums.V3xHtmSignatureEnum;
import com.seeyon.v3x.system.signet.manager.V3xHtmDocumentSignatManager;

import www.seeyon.com.utils.DateUtil;

/**
 * 适用于word 2007
 */
public class PrintUtil {
	private static final Logger  LOGGER     = Logger.getLogger(PrintUtil.class);
	/**
	 * 根据指定的参数值、模板，生成 word 文档
	 * 
	 * @param param
	 *            需要替换的变量
	 * @param template
	 *            模板
	 */
	public static CustomXWPFDocument generateWord(Map<String, Object> param,
			String template, CustomXWPFDocument doc, EdocSummary summary, Map<String, Object> opMap) {
		try {
			if(doc == null){
				OPCPackage pack = POIXMLDocument.openPackage(template);
				doc = new CustomXWPFDocument(pack);
			}
			if ((param != null && param.size() > 0) || summary != null) {
				// 处理段落
				// List<XWPFParagraph> paragraphList = doc.getParagraphs();
				// processParagraphs(paragraphList, param, doc, summary,
				// edocElementManager, metadataManager, opMap);
				// 处理表格
				
				Iterator<XWPFTable> it = doc.getTablesIterator();
				while (it.hasNext()) {
					XWPFTable table = it.next();
					List<XWPFTableRow> rows = table.getRows();
					for (XWPFTableRow row : rows) {
						List<XWPFTableCell> cells = row.getTableCells();
						for (XWPFTableCell cell : cells) {
							List<XWPFParagraph> paragraphListTable = cell
									.getParagraphs();
							processParagraphs(paragraphListTable, param, doc,summary, opMap);
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("下载打印生成word文件异常", e);
		}
		return doc;
	}

	/**
	 * 处理段落
	 * 
	 * @param paragraphList
	 */
	public static void processParagraphs(List<XWPFParagraph> paragraphList,
			Map<String, Object> param, CustomXWPFDocument doc,
			EdocSummary summary, Map<String, Object> opMap) {
		if (paragraphList != null && paragraphList.size() > 0) {
			for (XWPFParagraph paragraph : paragraphList) {
				//书签
/*				CTP ctp = paragraph.getCTP();
				CTBookmark bookMark = ctp.getBookmarkStartList().get(0);*/
				List<XWPFRun> runs = paragraph.getRuns();
				int originalRunsLen = runs.size();
				Map<XWPFRun,String> run2Remove = new HashMap<XWPFRun,String>(); 
				for (int i = 0; i < originalRunsLen; i++) {
					XWPFRun xwpfRun = runs.get(0);
					String fontFamily = xwpfRun.getFontFamily();
					int fontSize = xwpfRun.getFontSize();
					XWPFRun run = runs.get(i);
					String text = run.getText(0);
					String originalText = text;
					if (isReplacement(doc, text,param)) {
						boolean isSetText = false;

						if (summary == null) {// 保留下原来的逻辑
							isSetText = replaceByParams(doc, param, paragraph,
									isSetText, text);
						} else {
							// 公文处理的相关逻辑
							try {
								text = fetchValByEdocElementCode(paragraph, summary,opMap, text.trim());
								isSetText = true;
							} catch (IllegalAccessException e) {
								LOGGER.error(e);
							} catch (InvocationTargetException e) {
								LOGGER.error(e);
							} catch (Exception e) {
								LOGGER.error(e);
							}
						}
						if(text.indexOf("<br/>")!=-1){
							String[] tVals = text.split("<br/>");
							XWPFRun trun;
							boolean isFirst = true;
							for( int j = 0;j < tVals.length; j++){
								if(isFirst){
									if("".equals(tVals[j])){
										continue;
									}
									isFirst = false; 
									modifyFont(originalText, run);
									run.setText(tVals[j],0);
									run.addBreak(); 
								}else{
									trun = paragraph.createRun();
									modifyFont(originalText, trun);
									trun.setText(tVals[j]);
									trun.addBreak();
								}
							}
							break;//跳出来，因为这时run的数量已经增加了，如果不跳出来会对刚增加的run继续循环
						}else if(text.indexOf("\n")!=-1){
							String[] tVals = text.split("\n");
							XWPFRun trunN;
							for( int j = 0;j < tVals.length; j++){
								if(j==0){
									modifyFont(originalText, run);
									run.setText(tVals[j],0);
									run.addBreak();
								}else{
									trunN = paragraph.createRun();
									modifyFont(originalText, trunN);
									trunN.setText(tVals[j]);
									trunN.addBreak();
								}
							}
							break;//跳出来，因为这时run的数量已经增加了，如果不跳出来会对刚增加的run继续循环
						}else if (isSetText  ){
							run.setText(text, 0);
						} 
					} else if (param.get("replaceImg") != null && text != null) {
						String projectAddr = param.get("projectAddr") != null ? param
								.get("projectAddr").toString() : "";
						text = replaceImg(doc, paragraph,run, text, projectAddr);
						/*发文单下载到本地打印，领导批示签名位置错误，且请取消签名图片上下的空格 start*/
						if(!"".equals(text)){
							if ("toRemove".equals(text)) {
								run2Remove.put(run,text);
							}else {
								if (originalRunsLen > 1) {
									run2Remove.put(run,"");
									run = paragraph.createRun();
									if (fontSize!=-1) {
										run.setFontFamily(fontFamily);
										run.setFontSize(fontSize);
									}else {
										run.setFontFamily("仿宋_GB2312");
										//run.setFontSize(12);
									}
									
									run.setText(text,0);
									run.addBreak();
								}
							}
						}
						/*发文单下载到本地打印，领导批示签名位置错误，且请取消签名图片上下的空格 end*/
					}
				}
				
				
				if(run2Remove.size()>0){
					for(int i = originalRunsLen; i >=0 ; i--){
						if( run2Remove.get(runs.get(i))!=null ){
							paragraph.removeRun(i);
						}
					}
				}
				
			}
		}
	}

	private static void modifyFont(String originalText, XWPFRun trun) {
		if("pishi".equals(originalText) || "text1".equals(originalText) || "img".equals(originalText) || "IMG".equals(originalText)){
			//trun.setFontFamily("仿宋_GB2312");
			/**
			 *  12：小四，14：四号,参考：http://zhidao.baidu.com/link?url=ESc8GpPWCW2Di_zzjbPXDa7r4M0uYgxJ7198cEZB3u8XygXRhgYyaf65X7f6DHWps1v8kDLOlGBzYQ9Ve_Qw6K
			 */
			//trun.setFontSize(12);
		}
	}

	/**
	 * 是否占位符
	 * 
	 * @param text
	 * @param key
	 * @return
	 */
	private static boolean isReplacement(CustomXWPFDocument doc, String text, Map<String, Object> param) {
		if (text == null) {
			return false;
		}
		if(param.get("replaceImg")!=null){
			return false;
		}

		Pattern pattern = Pattern.compile("[\\w_]+");
		return pattern.matcher(text).find() && text.indexOf("<img src") == -1;
	}

	private static String replaceImg(CustomXWPFDocument doc,
			XWPFParagraph paragraph, XWPFRun run, String text, String projectAddr) {
		/**
		 * 将如下格式的内容替
		 * 【已阅】 文书处 <img src=/seeyon/apps_res/edoc/images/signets/7907068041618410201.jpg?r=1431849539061 class='link-blue' style='vertical-align: text-top;' onclick='javascript:showV3XMemberCard("7907068041618410201")'></img> 2015-04-13 09:20
		 * 换为
		 * 【已阅】 文书处 img标签代表的图片 2015-04-13 09:20
		 *  2015-8-16，图片的html片段发现一种新的格式：
		 *  <img name='signimg' src=/seeyon/apps_res/edoc/images/signets/7907068041618410201.jpg?r=1439694491388 class='link-blue'  onclick='javascript:showV3XMemberCard("7907068041618410201")'></img>
		 */
		String[] opa = text.split("\\n");
		Pattern pattern = Pattern.compile("<img src=/seeyon/signatPicControllermethod=writeGIF.+?>");
		V3xHtmDocumentSignatManager htmSignetManager = (V3xHtmDocumentSignatManager) AppContext.getBean("htmSignetManager");
		ByteArrayInputStream byteInputStream;
		byte[] byteArray;
		int picType;
		String strBeforeImg;
		String strAfterImg;
		int imgStart;
		int runIndex=0;
		boolean find = false;
		String originalText = text;
		for(String t:opa){
			Matcher matcher = pattern.matcher(t);
		try {
			while (matcher.find()) {
				find = true;
				
				String tmp = matcher.group();
				imgStart = t.indexOf(tmp);
				strBeforeImg = t.substring(0,imgStart);
				strAfterImg = t.substring(imgStart+tmp.length());
				
				if(runIndex==0){
					run.setText("",0);
					run = paragraph.createRun();
				}
				/*发文单下载到本地打印，领导批示签名位置错误，且请取消签名图片上下的空格 start*/
				String fontFamily = run.getFontFamily();
				int fontSize = run.getFontSize();
				XWPFRun run2 =  paragraph.createRun();
				if (fontSize!=-1) {
					run2.setFontFamily(fontFamily);
				}
				run2.setText(strBeforeImg,0);
				run2.addBreak();
				run2.addBreak();
				/*发文单下载到本地打印，领导批示签名位置错误，且请取消签名图片上下的空格 end*/
				runIndex++;
				
				strAfterImg = t.substring(imgStart+tmp.length());
				t = t.replace(tmp, "");
				int start = tmp.indexOf("/seeyon");
				int end = tmp.indexOf(">");//>
				String affairid = tmp.substring(start, end).split("affairId=")[1];
				String recordId=tmp.substring(tmp.indexOf("&RECORDID="), tmp.indexOf("&FIELDNAME=")).split("RECORDID=")[1];
				String filename=tmp.substring(tmp.indexOf("&FIELDNAME="), tmp.indexOf("&isNewImg=")).split("FIELDNAME=")[1];
				//V3xHtmDocumentSignature signature = htmSignetManager.getById(Long.valueOf(id));
				List<V3xHtmDocumentSignature> dsList = htmSignetManager.findBySummaryIdPolicyAndType(Long.valueOf(recordId), filename,
				        V3xHtmSignatureEnum.HTML_SIGNATURE_DOCUMENT.getKey());
/*					if (dsList != null) {
						String body = signature.getFieldValue();
						byteArray = EdocHelper.hex2byte(body);

						
						 * url = new URL(projectAddr + tmp.substring(start, end
						 * - 1).trim()); conn = (HttpURLConnection)
						 * url.openConnection(); conn.setRequestMethod("GET");
						 * conn.setConnectTimeout(5 * 1000); inStream =
						 * conn.getInputStream();
						 
						byteInputStream = new ByteArrayInputStream(byteArray);

						picType = getPictureType("jpg");
						doc.addPictureData(byteInputStream, picType);
						doc.createPicture(doc.getAllPictures().size() - 1, 120, 60, paragraph);
						byteInputStream.close();
						// inStream.close();
						//
					}*/
				String srcData = "";
				if (dsList != null && dsList.size() > 0) {
			        // 老数据没有affairId数据直接载入
			        if (dsList.size() == 1 && dsList.get(0).getAffairId() == null) {
			          srcData = dsList.get(0).getFieldValue();
			        } else {
			          for (V3xHtmDocumentSignature s : dsList) {
			            if (s.getAffairId() != null && s.getAffairId().equals(Long.valueOf(affairid))) {
			              srcData = s.getFieldValue();
			            }
			          }
			        }
			    }
				
				if(Strings.isNotBlank(srcData)){
					DBstep.iMsgServer2000 msgObj = new DBstep.iMsgServer2000();
					byteArray = msgObj.LoadRevisionAsImgByte(srcData);
					//byteArray = EdocHelper.hex2byte(srcData);
					byteInputStream = new ByteArrayInputStream(byteArray);

					//picType = getPictureType("jpg");
					paragraph.createRun();
					doc.addPictureData(byteInputStream, CustomXWPFDocument.PICTURE_TYPE_BMP);
					doc.createPicture(doc.getAllPictures().size() - 1, 500,100, paragraph);
					byteInputStream.close();
				}
				if(runIndex>0){
					run = paragraph.createRun();
				}
				modifyFont("img",run);
				run.setText(strAfterImg);
				if(Strings.isNotBlank(srcData)){
					run.addBreak();//文单签批图片时换行
				}
				runIndex++;
			}
		} catch (MalformedURLException e) {
			LOGGER.error("下载打印生成word文件异常", e);
		} catch (IOException e) {
			LOGGER.error("下载打印生成word文件异常", e);
		} catch (InvalidFormatException e) {
			LOGGER.error("下载打印生成word文件异常", e);
		}
		}
		
		return find ? "toRemove" : originalText;
	}

	private static boolean replaceByParams(CustomXWPFDocument doc,
			Map<String, Object> param, XWPFParagraph paragraph,
			boolean isSetText, String text) {
		for (Entry<String, Object> entry : param.entrySet()) {
			String key = entry.getKey();
			if (text.indexOf(key) != -1) {
				isSetText = true;
				Object value = entry.getValue();
				if (value instanceof String) {// 文本替换
					text = text.replace(key, value.toString());
				} else if (value instanceof Map) {// 图片替换
					text = text.replace(key, "");
					Map pic = (Map) value;
					int width = Integer.parseInt(pic.get("width").toString());
					int height = Integer.parseInt(pic.get("height").toString());
					int picType = getPictureType(pic.get("type").toString());
					byte[] byteArray = (byte[]) pic.get("content");
					ByteArrayInputStream byteInputStream = new ByteArrayInputStream(
							byteArray);
					try {
						String ind = doc.addPictureData(byteInputStream,
								picType);
						doc.createPicture(doc.getAllPictures().size() - 1,
								width, height, paragraph);
					} catch (Exception e) {
						LOGGER.error("下载打印生成word文件异常", e);
					}
				}
			}
		}
		return isSetText;
	}

	/**
	 * 根据图片类型，取得对应的图片类型代码
	 * 
	 * @param picType
	 * @return int
	 */
	private static int getPictureType(String picType) {
		int res = CustomXWPFDocument.PICTURE_TYPE_PICT;
		if (picType != null) {
			if ("png".equalsIgnoreCase(picType)) {
				res = CustomXWPFDocument.PICTURE_TYPE_PNG;
			} else if ("dib".equalsIgnoreCase(picType)) {
				res = CustomXWPFDocument.PICTURE_TYPE_DIB;
			} else if ("emf".equalsIgnoreCase(picType)) {
				res = CustomXWPFDocument.PICTURE_TYPE_EMF;
			} else if ("jpg".equalsIgnoreCase(picType)
					|| "jpeg".equalsIgnoreCase(picType)) {
				res = CustomXWPFDocument.PICTURE_TYPE_JPEG;
			} else if ("wmf".equalsIgnoreCase(picType)) {
				res = CustomXWPFDocument.PICTURE_TYPE_WMF;
			}
		}
		return res;
	}

	/**
	 * 将输入流中的数据写入字节数组
	 * 
	 * @param inStream
	 * @return
	 */
	public static byte[] inputStream2ByteArray(InputStream inStream, boolean isClose) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
        byte[] buffer = new byte[5120];  
        int len = 0;  
        try {
			while( (len=inStream.read(buffer)) != -1 ){  
			    outStream.write(buffer, 0, len);  
			}
		} catch (IOException e) {
			LOGGER.error("下载打印生成word文件异常", e);
		} finally{
			if (isClose) {
				try {
					inStream.close();
				} catch (Exception e2) {

					LOGGER.error("下载打印生成word文件异常，关闭流失败", e2);
				}
			}
		}
        return outStream.toByteArray(); 
	}

	private static String fetchValByEdocElementCode(XWPFParagraph paragraph, EdocSummary summary,
			Map<String, Object> opMap, String repalcementName) throws IllegalAccessException,
			InvocationTargetException, BusinessException {
		String val = "";
		Method method;
		String[] eleParts;
		String postMethod;
		
		if ("keyword".equals(repalcementName)) {
			repalcementName += "s";
		}
		if ("packdate".equals(repalcementName)) {
			repalcementName = "packTime";
		}
		eleParts = repalcementName.split("_");
		postMethod = "";
		for (String elePart : eleParts) {
			elePart = elePart.replace("string", "Varchar");
			postMethod += elePart.substring(0, 1).toUpperCase()
					+ elePart.substring(1);
		}
		try {
			method = summary.getClass().getMethod("get" + postMethod);
			Object obj = method.invoke(summary);
			if(obj instanceof String){ 
			    val = (String)obj;
			}else if(obj instanceof java.sql.Date){
				//公文处理笺下载到本地打印时，日期会少一天 
				val =Datetimes.format((java.sql.Date)obj, Datetimes.dateStyle);
			}else if(obj instanceof Timestamp){
				//SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");  
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");  
				val = df.format(obj);
			}else if(obj != null){
			    val = obj.toString();
			}
			if(val!=null){
				val = val.replaceAll("\r\n", "<br>");//做个标识，在后续逻辑里面统一处理
			}
		} catch (NoSuchMethodException e) {

			// 如果method不存在，则该元素是意见，
			Object opObj = opMap.get(repalcementName.toLowerCase());
			if("otherOpinion".equals(repalcementName)){
				opObj = opMap.get(repalcementName);
			}
			val = opObj != null ? opObj.toString() : "";
			
			V3xHtmDocumentSignatManager htmSignetManager = (V3xHtmDocumentSignatManager) AppContext.getBean("htmSignetManager");
			List<V3xHtmDocumentSignature> dsList = htmSignetManager.findBySummaryIdAndType(summary.getId(),V3xHtmSignatureEnum.HTML_SIGNATURE_DOCUMENT.getKey());
			if(dsList != null){
				GovdocSummaryManager govdocSummaryManager = (GovdocSummaryManager) AppContext.getBean("govdocSummaryManager");
				FormApi4Cap3 formApi4Cap3 = (FormApi4Cap3)AppContext.getBean("formApi4Cap3");
				EdocSummary edocSummary = govdocSummaryManager.getSummaryById(summary.getId());
				FormBean fb = formApi4Cap3.getForm(edocSummary.getFormAppid());
				String fieldName =fb.getFieldNameByMappingField(repalcementName);
				if(!Strings.isNotBlank(fieldName)){
			    	String[] divArr = val.split("<div id='");
			    	for (int i = 1; i < divArr.length; i++) {
			    		divArr[i] = "<div id='" +divArr[i];
			    	}
					for(V3xHtmDocumentSignature signature : dsList){
						if(Strings.isNotBlank(signature.getFieldName()) && signature.getFieldName().equals("hw"+fieldName)){
							for (int i = 1; i < divArr.length; i++) {
								String divStr = divArr[i];
								if(divStr.indexOf(String.valueOf(signature.getAffairId())) > -1){
									divArr[i] = "<img src=/seeyon/signatPicControllermethod=writeGIF&RECORDID="+signature.getSummaryId()+"&FIELDNAME="+signature.getFieldName()
											  +"&isNewImg=true&affairId="+ signature.getAffairId() + ">"+ divStr;
								}
							}
						}
					}
					if(divArr.length > 0){
						val = StringUtils.join(divArr, "");
					} 
				}
			}
			
			// 替换val中的html片段
			val = val.replaceAll("&nbsp;", " ").replaceAll("<span.+?>", " ")
					.replaceAll("</span>", " ").replaceAll("</div>", "\r\n").replace("&ensp;", "");
			//发文单下载到本地打印，领导批示签名位置错误，且请取消签名图片上下的空格
			if(Strings.isNotBlank(val)&&val.indexOf("<br><img")>-1){
				val = val.replaceAll("<br><img", "<img");
			}
			
			if(Strings.isNotBlank(val)&&val.indexOf("<br>")>-1){
				val = val.replaceAll("<br/>", " ");
			} 
		}
		val = getOtherVal(summary, repalcementName, val);
		
		//拟办意见处理
		try{
			if("nibanyijian".equals(repalcementName)){
				GovdocSummaryManager govdocSummaryManager = (GovdocSummaryManager) AppContext.getBean("govdocSummaryManager");
				FormApi4Cap3 formApi4Cap3 = (FormApi4Cap3)AppContext.getBean("formApi4Cap3");
				EdocSummary edocSummary = govdocSummaryManager.getSummaryById(summary.getId());
				FormBean fb = formApi4Cap3.getForm(edocSummary.getFormAppid());
		    	String nibanyijianFieldName = fb.getFieldNameByMappingField("nibanyijian");
		    	FormDataMasterBean f = formApi4Cap3.findDataById(edocSummary.getFormRecordid(), fb.getId() );
		    	String nibanyijianValue = f.getFieldValue(nibanyijianFieldName).toString();
		    	val = nibanyijianValue;
		    	
			}
		}catch(Exception e){
			
		}
		if(val!=null){
			val = val.replaceAll("</div>", "");
			val = val.replaceAll("<a.+?>", "");
			//val = val.replaceAll("<img.+?>", "");
			val = val.replaceAll("</a>", "");
			val = val.replaceAll("<div[^>]*?>","<br/>").trim();
			val = val.replaceAll("\r\n","<br/>");
			
			val = val.replaceAll("<br>","<br/>");

			//意见里的附件，需去掉img
			if(Strings.isNotBlank(val)&&val.indexOf("<img src='/seeyon/common/images/attachmentICON")>-1){
				val = val.replaceAll("<img src='/seeyon/common/images/attachmentICON.+?>", "");
			}
		}
		
		return val==null ? "" : val ;
	}

	private static String getOtherVal(EdocSummary summary,
			String repalcementName, String val) {
		// 根据占位符拼接其可能对应的get方法名
		// eleName = elePlacement.substring(1,elePlacement.length()-1);
		try {
			//如果是附件，则需要查询附件标题
			if("attachments".equals(repalcementName)){
			    AttachmentManager attachmentManager = (AttachmentManager) AppContext.getBean("attachmentManager");
			    List<Attachment> attachments = attachmentManager.getByReference(summary.getId(),Long.valueOf(val));
			    StringBuffer sb = new StringBuffer("");
			    for (int i = 0; i < attachments.size(); i++) {
					if(i > 0){
						sb.append("<br/>");
					}
					sb.append(attachments.get(i).getFilename());
				}
			    val = sb.toString();
			}else if ("createdate".equals(repalcementName)) {// createdate对应的get方法比较特殊
				val = DateUtil.getDate(summary.getCreateTime(), "yyyy-MM-dd");
			} else if ("doc_type".equals(repalcementName)
					|| "send_type".equals(repalcementName)
					|| "secret_level".equals(repalcementName)
					|| "urgent_level".equals(repalcementName)
					|| "keep_period".equals(repalcementName)
					|| "unit_level".equals(repalcementName)
					|| "public_info".equals(repalcementName)) {
				GovDocElement docElement = GovDocElement.getElementByCode(repalcementName);
				com.seeyon.ctp.common.ctpenumnew.manager.EnumManager enumManager = (com.seeyon.ctp.common.ctpenumnew.manager.EnumManager) AppContext.getBean("enumManagerNew");
				List<com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem> items = enumManager.getEmumItemByEmumId(Long.valueOf(docElement.getEnumParams()));
				for (com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem ctpEnumItem : items) {
					if (ctpEnumItem.getEnumvalue().equals(val.toString())) {
						val = ResourceUtil.getString(ctpEnumItem.getShowvalue());
						break;
					} 
				}
			}else if("check_person".equals(repalcementName)){
				try {
					if(Strings.isNotBlank(val)){
						String[] personIds = val.split(",");
						String checkPersonName ="";
						OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
						for(int i=0;i<personIds.length;i++){
							Long memberId = Long.valueOf(personIds[i].toString());
							V3xOrgMember member = orgManager.getMemberById(memberId);
							if (member != null) {
								checkPersonName += member.getName();
								if(i < personIds.length-1 ) {
									checkPersonName += ",";
								}
							}
						}
						val = checkPersonName;
					}
					
				} catch (Exception e) {
					
				}
			}else {// 其他的统一处理
				
			}
		} catch (NumberFormatException e) {

		} catch (BusinessException e) {

		}
		return val;
	}
}
