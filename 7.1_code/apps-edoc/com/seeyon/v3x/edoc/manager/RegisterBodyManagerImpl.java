package com.seeyon.v3x.edoc.manager;

import static com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_HTML;
import static com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_OFFICE_EXCEL;
import static com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_OFFICE_WORD;
import static com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_PDF;
import static com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_WPS_EXCEL;
import static com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_WPS_WORD;
import static com.seeyon.ctp.common.constants.Constants.OFFICE_EDIT_TYPE_0_0;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.Constants.OFFICS_FILE_TYPE;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.office.trans.util.OfficeTransHelper;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.taglibs.functions.Functions;
import com.seeyon.v3x.common.taglibs.util.Constants;
import com.seeyon.v3x.edoc.dao.RegisterBodyDao;
import com.seeyon.v3x.edoc.domain.RegisterBody;

public class RegisterBodyManagerImpl implements RegisterBodyManager {

	private static final long serialVersionUID = -1967360290677077151L;
	private static Log LOG = CtpLogFactory.getLog(RegisterBodyManagerImpl.class);
	
	protected String type;
	protected String content;
	protected String contentName;
	protected String viewMode;
	protected String htmlId;
	protected String createDate;
	protected Long lastUpdateTime;
	
	//正文实际的大小，标签传入，取自v3x_file表的fileSize，主要用来判断office插件是否能打开当前正文。
	protected Long officeFileRealSize;
	protected String summaryId = null;
	private User user = null;
	private String basePath = null;	
	private FileManager fileManager;
	
	private RegisterBodyDao registerBodyDao = null;
	
	public RegisterBodyManagerImpl() {
		init();
	}

	private void init() {
		type = EDITOR_TYPE_HTML;
		htmlId = createDate = content = "";
		contentName="";
	}
	

	public StringBuffer getRegisterContent(HttpServletRequest request, RegisterBody registerBody) throws Exception {
		StringBuffer contentBuffer = new StringBuffer();
		type = registerBody.getContentType();
		content = registerBody.getContent();
		createDate = DateUtil.formatDate(registerBody.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
		String htmlId = "edoc-contentText";
		String viewMode = request.getParameter("viewMode");
    	basePath = request.getScheme() + "://" + request.getServerName() + ":" +request.getServerPort() + request.getContextPath();
    	user = AppContext.getCurrentUser();
		contentBuffer.append("<div style='display:none'>");
		contentBuffer.append("<input type='hidden' name='bodyType' id='bodyType' value='"+ type + "'>");
		contentBuffer.append("<input type=\"hidden\" name=\"bodyCreateDate\" value=\"" + registerBody.getCreateTime() + "\">");
		contentBuffer.append("</div>");
		contentBuffer.append("<input id=\"contentNameId\" type=\"hidden\" name=\"contentName\" value=\"\">");
			
		if (type.equals(EDITOR_TYPE_HTML)) {
			contentBuffer.append(com.seeyon.v3x.common.taglibs.util.Constants.getString("showContent.RTE.html", htmlId, content));
		} else if (type.equals(EDITOR_TYPE_OFFICE_WORD) || type.equals(EDITOR_TYPE_OFFICE_EXCEL) || type.equals(EDITOR_TYPE_WPS_WORD) || type.equals(EDITOR_TYPE_WPS_EXCEL)|| type.equals(EDITOR_TYPE_PDF)){
			if(type.equals(EDITOR_TYPE_OFFICE_WORD) || type.equals(EDITOR_TYPE_OFFICE_EXCEL)){
				viewMode = viewMode==null ? "view" : viewMode;
				if("view".equals(viewMode)){
					contentBuffer.append(getOfficeHtmlContent(request));
				}else{
					contentBuffer.append(getOfficeContent());
				}
			}else{
				contentBuffer.append(getOfficeContent());
			}
		}    
		return contentBuffer;
	}
	
	
	private String getOfficeHtmlContent(HttpServletRequest req) throws IOException {
		Long fileId =  org.apache.commons.lang.math.NumberUtils.toLong(content);
		V3XFile v3XFile = null;
		try {
			if(fileManager==null) {
				fileManager =(FileManager) AppContext.getBean("fileManager");
			}
			v3XFile = fileManager.getV3XFile(fileId);
		} catch (Exception e) {
		    LOG.error("", e);
		}
		//(5.0sprint3)-FIXED(yangfan) 
		if (!OfficeTransHelper.allowTrans(v3XFile)) {
			return getOfficeContent();
		}
		String htmlURL = OfficeTransHelper.buildCacheUrl(v3XFile, false);
		final String officeHtml = "<script>document.body.scroll='no'; trans2Html=true;</script>" + "<iframe style=\"width:100%;height:100%;margin-left:10px;margin-right:10px;\" src=\"" + htmlURL + "\"></iframe>";
		// 添加一个这个变量用来页面控制office插件的加载.
		req.setAttribute("isSucessTrans2Html", true);
		return officeHtml;
		
	}
	

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	private String getOfficeContent() throws IOException {
		String userName = user.getName();
		OFFICS_FILE_TYPE fileType = getContentType(type);
		if(lastUpdateTime==null){lastUpdateTime=0L;}
		String officeOcxUploadMaxSize = SystemProperties.getInstance().getProperty("officeFile.maxSize");
		String realSize = officeFileRealSize == null ? "":String.valueOf(officeFileRealSize/1024);
		if(content == null){
		    content = "";
		}
		
		String str = Constants.getString("showContent.office.html", basePath,
				content.toString(), createDate, "", OFFICE_EDIT_TYPE_0_0,
				DBstep.iMsgServer2000.Version(), "", true, Strings.escapeJavascript(userName), lastUpdateTime.toString(), 
				officeOcxUploadMaxSize, DBstep.iMsgServer2000.Version("iWebPdf"),realSize, Functions.resSuffix()
				);
		if(type.equals(EDITOR_TYPE_PDF)){
			str += "<script>showPdfDiv('" + fileType.name() + "');</script>";
		}else{
			str += "<script>showOfficeDiv('" + fileType.name() + "');</script>";
		}
		return str;
	}
	
	private OFFICS_FILE_TYPE getContentType(String contentType) {
		if(EDITOR_TYPE_OFFICE_WORD.equalsIgnoreCase(contentType)){return OFFICS_FILE_TYPE.doc;}
		if(EDITOR_TYPE_OFFICE_EXCEL.equalsIgnoreCase(contentType)){return OFFICS_FILE_TYPE.xls;}
		if(EDITOR_TYPE_WPS_WORD.equalsIgnoreCase(contentType)){return OFFICS_FILE_TYPE.wps;}
		if(EDITOR_TYPE_WPS_EXCEL.equalsIgnoreCase(contentType)){return OFFICS_FILE_TYPE.et;}		
		if(EDITOR_TYPE_PDF.equalsIgnoreCase(contentType)){return OFFICS_FILE_TYPE.pdf; }
		return OFFICS_FILE_TYPE.doc;		
	}
	
	public void updateReigsterBody(RegisterBody registerBody) {
		registerBodyDao.update(registerBody);
	}

	public void setRegisterBodyDao(RegisterBodyDao registerBodyDao) {
		this.registerBodyDao = registerBodyDao;
	}
	
}
