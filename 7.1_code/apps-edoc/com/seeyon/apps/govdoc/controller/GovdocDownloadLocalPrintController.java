package com.seeyon.apps.govdoc.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.govdoc.option.manager.FormOptionExtendManager;
import com.seeyon.apps.govdoc.option.manager.FormOptionSortManager;
import com.seeyon.apps.govdoc.po.FormOptionExtend;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.modules.bindprint.manager.FormPrintBindManager;
import com.seeyon.ctp.form.po.FromPrintBind;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.common.web.login.CurrentUser;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.util.CustomXWPFDocument;
import com.seeyon.v3x.edoc.util.EdocOpinionDisplayUtil;
import com.seeyon.v3x.edoc.util.PrintUtil;
import com.seeyon.v3x.edoc.webmodel.EdocOpinionModel;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;
import com.seeyon.v3x.system.signet.domain.V3xHtmDocumentSignature;
import com.seeyon.v3x.system.signet.enums.V3xHtmSignatureEnum;
import com.seeyon.v3x.system.signet.manager.V3xHtmDocumentSignatManager;
/**
 * 根据表单绑定模版 套红下载本地的控制类
 * @author xuym
 *
 */
public class GovdocDownloadLocalPrintController  extends BaseController{
	private static final Log LOGGER = CtpLogFactory.getLog(GovdocDownloadLocalPrintController.class);
	private AffairManager affairManager;
	private FileManager fileManager;
	private GovdocSummaryManager govdocSummaryManager;
	private FormPrintBindManager formPrintBindManager;
	private V3xHtmDocumentSignatManager htmSignetManager;
	private FormOptionExtendManager formOptionExtendManager;
	private FormOptionSortManager formOptionSortManager;
	private FormApi4Cap3 formApi4Cap3;
    /**
     * 增加“下载到本地打印”，处理件内容自动回显到本地的word模板中。
     * @param request
     * @param response
     * @return
     * @throws BusinessException
     */
	public ModelAndView exportPrint2Word(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
		try {
			String edocId = request.getParameter("edocId");
			EdocSummary summary = govdocSummaryManager.getSummaryById(Long.parseLong(edocId));
			if(summary == null){
				//公文信息没有保存，给出提示
				PrintWriter out = response.getWriter();
				out.println("<script>");
				out.println("alert('"+StringEscapeUtils.escapeJavaScript(ResourceUtil.getString("govdoc.print.download.needSave.label"))+"');");
				out.println("</script>");
				out.flush();
				return null;
			}
			Long formId = summary.getFormAppid();
			if(formId == null){
				//没有找到文单信息，给出提示
				PrintWriter out = response.getWriter();
				out.println("<script>");
				out.println("alert('"+StringEscapeUtils.escapeJavaScript(ResourceUtil.getString("govdoc.print.download.noTemplate.label"))+"');");
				out.println("</script>");
				out.flush();
				return null;
			}
			//cleanTempFolder();
			String projectAddr = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("projectAddr", projectAddr);
             String filePath = fetchPrintTemplate(formId);
             if(Strings.isBlank(filePath)){
         		PrintWriter out = response.getWriter();
             	out.println("<script>");
             	out.println("alert('"+StringEscapeUtils.escapeJavaScript(ResourceUtil.getString("govdoc.print.download.noTemplate.label"))+"');");
             	out.println("</script>");
             	out.flush();
         		return null;
             }
			Map<String, Object> opMap = fetchEdocOpinion4Print(request, summary);
			CustomXWPFDocument replaceTextDocx = PrintUtil.generateWord(params, filePath, null,
					summary, opMap);

			params.put("replaceImg", "");
			CustomXWPFDocument finalDocx = PrintUtil.generateWord(params, null, replaceTextDocx, summary, opMap);

			String fileName = summary.getSubject().replaceAll("\r", "").replaceAll("\n", "").trim();

			response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
			response.setHeader("Content-Disposition",
					"attachment; filename=" + URLEncoder.encode(fileName, "UTF-8") + ".docx");
			ServletOutputStream out = response.getOutputStream();
			finalDocx.write(out);
		} catch (Exception e) {
			LOGGER.error("下载本地打印异常", e);
     		try {
				PrintWriter out = response.getWriter();
				out.println("<script>");
				out.println("alert('"+StringEscapeUtils.escapeJavaScript("下载文件失败！")+"');");
				out.println("</script>");
				out.flush();
				return null;
			} catch (IOException e1) {
				LOGGER.error("获取out失败",e1);
			}
		} 
		return null;
	}

	/**
	 * 获取打印模板
	 * 
	 * @param summary
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private String fetchPrintTemplate(Long formId) throws IOException, FileNotFoundException {
		String root = "";
		User user = CurrentUser.get();
		FromPrintBind proEdocPrint = formPrintBindManager.findPrintMode(user.getAccountId(), formId);
		if (proEdocPrint != null) {
			if (Strings.isNotBlank(proEdocPrint.getFileUrl().toString())) {
				File file = null;
				try {
					file = fileManager.getFile(Long.valueOf(proEdocPrint.getFileUrl().toString()));
				    root = file.getAbsolutePath();
				} catch (NumberFormatException e) {
					LOGGER.error(e);
				} catch (BusinessException e) {
					LOGGER.error(e);
				}
			}
		}

		return root;
	}
	
	/**
	 * 获取公文的意见，为打印做准备
	 * 
	 * @param request
	 * @param summary
	 * @return
	 * @throws BusinessException 
	 * @throws NumberFormatException 
	 */
	private Map<String, Object> fetchEdocOpinion4Print(HttpServletRequest request, EdocSummary edocSummary) throws BusinessException{
		String affairId = request.getParameter("affairId");
		CtpAffair affair = null;
		try {
			affair = affairManager.get(Long.valueOf(affairId));
		} catch (NumberFormatException e) {
			LOGGER.error("",e);
		}
		Long formAppId = edocSummary.getFormAppid();
		FormOptionExtend formOptionExtend = formOptionExtendManager.findByFormId(formAppId);
		FormOpinionConfig displayConfig = null;
		// 公文单显示格式
		if(null!=formOptionExtend){
    	    displayConfig = JSONUtil.parseJSONString(formOptionExtend.getOptionFormatSet(), FormOpinionConfig.class);
		}
		if(displayConfig == null){
		    displayConfig = new FormOpinionConfig();
		}
		//String optionType = request.getParameter("optionType") != null ? request.getParameter("optionType") : "";// 从要打印的页面继承过来
		Map<String, EdocOpinionModel> map = formOptionSortManager.getGovdocOpinion(formAppId,edocSummary, displayConfig);
		FormBean formBean = formApi4Cap3.getForm(formAppId);
		List<FormFieldBean> fieldBeans = formBean.getAllFieldBeans();
		for(FormFieldBean fieldBean:fieldBeans){
			if(map.containsKey(fieldBean.getName())){
				map.put(fieldBean.getMappingField(), map.get(fieldBean.getName()));
				map.remove(fieldBean.getName());
			}
		}
		List<V3xHtmDocumentSignature> signatuers = htmSignetManager.findBySummaryIdAndType(edocSummary.getId(), V3xHtmSignatureEnum.HTML_SIGNATURE_DOCUMENT.getKey());
		return EdocOpinionDisplayUtil.convertOpinionToString(map, displayConfig,affair,true,signatuers);
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}

	public void setHtmSignetManager(V3xHtmDocumentSignatManager htmSignetManager) {
		this.htmSignetManager = htmSignetManager;
	}

	public void setFormOptionExtendManager(
			FormOptionExtendManager formOptionExtendManager) {
		this.formOptionExtendManager = formOptionExtendManager;
	}

	public void setFormOptionSortManager(FormOptionSortManager formOptionSortManager) {
		this.formOptionSortManager = formOptionSortManager;
	}

	public void setFormPrintBindManager(FormPrintBindManager formPrintBindManager) {
		this.formPrintBindManager = formPrintBindManager;
	}
	
	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }
	
}
