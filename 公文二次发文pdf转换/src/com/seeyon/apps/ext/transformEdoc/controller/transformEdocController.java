package com.seeyon.apps.ext.transformEdoc.controller;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.v3x.edoc.domain.CtpPdfSavepath;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.CtpPdfSavepathManager;
import com.seeyon.v3x.edoc.manager.CtpPdfSavepathManagerImpl;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.manager.EdocManagerImpl;
import com.seeyon.v3x.webmail.util.FileUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.xfire.client.Client;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.web.servlet.ModelAndView;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;

public class transformEdocController extends BaseController {

    private EdocManager edocManager = (EdocManager) AppContext.getBean("edocManager");
    private AttachmentManager attachmentManager = (AttachmentManager) AppContext.getBean("attachmentManager");
    private FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
    private CtpPdfSavepathManager ctpPdfSavepathManager = new CtpPdfSavepathManagerImpl();


    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("apps/ext/transformEdoc/index");
    }

    public ModelAndView toTransformPdf(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String summaryId = request.getParameter("summaryId");
        String contentId = request.getParameter("contentId");
        EdocSummary edocSummary = edocManager.getEdocSummaryById(Long.parseLong(summaryId), true);
        EdocBody edocBody = edocSummary.getFirstBody();
        List<Attachment> atts = attachmentManager.getByReference(edocSummary.getId());
        try {
            int flag = transitionPdf(edocSummary, edocBody);
            if (flag == -1) {
                StringBuffer sb = new StringBuffer();
                sb.append("alert('" + StringEscapeUtils.escapeJavaScript("转换服务出错了，请联系管理员") + "');");
                sb.append("history.back();");
                rendJavaScript(response, sb.toString());
                return null;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("code", 0);
            map.put("data", "success");
            JSONObject json = new JSONObject(map);
            render(response, json.toJSONString());
        } catch (Exception e) {
            logger.info("获取集团总人数出错了：" + e.getMessage());
        }
        return null;
    }

    public int transitionPdf(EdocSummary edocSummary, EdocBody body) {
        int flag = -1;
        try {
            //获取系统路径
            String spath = fileManager.getFolder(new Date(), false);
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);

            Long val = edocSummary.getId();
            System.out.println("表单信息ID：" + val);


            // 文单路径(文单模板)
            System.out.println("获取系统路径:" + spath);
//            String sFormFilePath = spath.substring(0, spath.lastIndexOf(String.valueOf(year) + "\\")) + "template" + File.separator + edocSummary.getFormId() + ".pdf";
            int p = spath.lastIndexOf(String.valueOf(year));
            String templateFilePath = (spath.substring(0, p) + "template" + File.separator).replaceAll("\\\\", "/");

            int insertRes = 0;

            // 文单存在，则插入文单域相应的内容
            String wdPdfPath = (spath + File.separator + (edocSummary.getId()).toString() + File.separator).replaceAll("\\\\", "/");
            // -2:表示模板不存在   -1：表示合并pdf失败
            flag = insertFormRegionValue(edocSummary, templateFilePath, wdPdfPath);
            if (flag != -2) {
                // 附件转换成cebx
                // 获取相关的附件列表
                List<Attachment> atts = attachmentManager.getByReference(edocSummary.getId());
                // 合并附件用到的附件参数
                String attFileName = "";
                // 附件后缀名
                String suffix = "";
                // 无后缀的附件路径
                String filePath = "";
                File file;
                for (Attachment att : atts) {
                    suffix = att.getFilename().substring(att.getFilename().lastIndexOf("."), att.getFilename().length());
                    filePath = (spath + File.separator + att.getFileUrl()).replaceAll("\\\\", "/");
                    file = new File(filePath);
                    if (file.exists() && (".doc".equals(suffix) || ".docx".equals(suffix) || ".pdf".equals(suffix) ||
                            ".ppt".equals(suffix) || ".pptx".equals(suffix) || ".wps".equals(suffix)
                            || ".xlsx".equals(suffix) || ".xls".equals(suffix)) || ".cebx".equals(suffix)) {
                        FileUtil.copy(filePath, filePath + suffix);
                        attFileName += (filePath + suffix + "|");
                    }
                }
                // 表示附件是可以合并的附件
                if (!"".equals(attFileName)) {
                    attFileName = attFileName.substring(0, attFileName.length() - 1);
                    System.out.println("合并附件路径：" + attFileName);
                }

                //获取正文文件所在的路径
                String sBodyPath = (spath + File.separator + body.getContent() + ".doc").replaceAll("\\\\", "/");

                // copy 一份正文的doc文件
                FileUtil.copy(new File((spath + File.separator + body.getContent()).replaceAll("\\\\", "/")), new File(sBodyPath));

                //获取正文文件路径
                File bodyFile = new File(sBodyPath);
                //获取文单pdf文件路径
                File formFile = new File(wdPdfPath.concat(edocSummary.getId().toString() + ".doc"));
                if (bodyFile.exists() && formFile.exists()) {
                    System.out.println("合并文件开始！！！");
                    // 2015-07-28 modify 默认发文
                    Boolean isReceive = false;
                    // 2015-07-28 modify 表示是收文
                    if (edocSummary.getEdocType() == 1) {
                        isReceive = true;
                    }
                    //ftp://root:xkjt1234@10.100.1.76:21/2007/word.docx
                    String mergeSavePath = wdPdfPath;
                    String ftpUpload = "z:" + wdPdfPath.substring(wdPdfPath.indexOf("upload") + 6).replaceAll("\\\\", "/");
                    String wendanP = ftpUpload.concat(edocSummary.getId().toString() + ".doc");
                    String zwp = sBodyPath.substring(sBodyPath.indexOf("upload") + 6).replaceAll("\\\\", "/");
                    String zhengwenP = "z:" + zwp;
//                String fujianP = "";
                    StringBuffer fujianP = new StringBuffer();
                    if (!("").equals(attFileName) && attFileName.length() > 0) {
                        String[] arr = attFileName.split("\\|");
                        for (int i = 0; i < arr.length; i++) {
                            fujianP.append("z:" + arr[i].substring(arr[i].indexOf("upload") + 6).replaceAll("\\\\", "/"));
                            fujianP.append("|");
                        }
                    }

                    System.out.println("公文单地址路径：" + wendanP);
                    System.out.println("正文地址路径：" + zhengwenP);
                    System.out.println("附件地址路径：" + fujianP);
                    String mergerpath = "";
                    String formId = Long.toString(edocSummary.getFormId()).trim();
                    if (null != formId && !"".equals(formId)) {
//                        合并正文
                        if (formId.equals("-7139423850050401892") || formId.equals("1542089478047025160")) {
                            mergerpath = wendanP.concat("|" + zhengwenP);
                        } else {
//                            不合并正文
                            mergerpath = wendanP;
                        }
                    } else {
                        mergerpath = wendanP;
                    }
                    if (!("").equals(fujianP.toString())) {
                        mergerpath = mergerpath.concat("|" + fujianP.toString());
                    }

                    //类根路径  F:\Seeyon\A8\ApacheJetspeed\webapps\seeyon\WEB-INF\classes
                    String classPath = this.getClass().getResource("/").getPath();
                    mergeSavePath = (classPath.substring(0, classPath.indexOf("WEB-INF"))).concat("pdf") + File.separator + edocSummary.getId() + File.separator;
                    File f = new File(mergeSavePath);
                    if (!f.exists()) {
                        f.mkdirs();
                    }

                    flag = mergeFormAndBody(edocSummary, "http://10.11.100.41:8088/convert/webservice/ConvertService?wsdl", mergerpath, mergeSavePath, isReceive);
                    bodyFile.delete();
                    formFile.delete();
                    if (flag != -1) {
                        String pathPdf = "smb://root:xkjt2019,@10.11.100.33/pdf" + File.separator + edocSummary.getId() + File.separator;
                        SmbFile smbFile = new SmbFile(pathPdf);
                        String filename = "";
                        if (smbFile.exists()) {
                            SmbFile[] smbFiles = smbFile.listFiles();
                            filename = smbFiles[0].getName();
                        }
                        smbGet(pathPdf + filename, mergeSavePath.concat(filename));
                        String pdfpath = mergeSavePath.concat(edocSummary.getId().toString() + ".pdf");
                        File deleteFile = new File(pdfpath);
                        deleteFile.delete();
                        returnFileName(mergeSavePath, "2", new File(pdfpath));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // 0:代表转换成功，-1：转换失败  -2：表示公文单模板不存在
        return flag;
    }

    public void smbGet(String remoteUrl, String pdfOfPdfDir) {
        InputStream in = null;
        OutputStream out = null;
        SmbFile remoteFile = null;
        try {
            remoteFile = new SmbFile(remoteUrl);
            if (null == remoteFile) {
                System.out.println("共享文件不存在");
                return;
            }
            File localFile = new File(pdfOfPdfDir);
            in = new BufferedInputStream(new SmbFileInputStream(remoteFile));
            out = new BufferedOutputStream(new FileOutputStream(localFile));
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1) {
                out.write(buffer);
                buffer = new byte[1024];
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException io) {
                io.printStackTrace();
            }
            try {
                remoteFile.delete();
            } catch (SmbException smb) {
                logger.error("删除共享文件出错：" + smb.getMessage());
            }
        }

    }

    public String returnFileName(String path, String type, File file) {
        File wdfile = new File(path);
        File[] filesWd = wdfile.listFiles();
        String fileName = "";
        for (int i = 0; i < filesWd.length; i++) {
            if (filesWd[i].isFile()) {
                if ("1".equals(type)) {
                    fileName = filesWd[i].getName();
                } else {
                    filesWd[i].renameTo(file);
                }
            }
        }
        return fileName;
    }

    /**
     * 合并公文单与正文
     */
    public int mergeFormAndBody(EdocSummary edocSummary, String url, String mergepath, String savepath, boolean flag) {

        int cbCode = -1;

        String reverPath = savepath.replaceAll("\\\\", "/");
        CtpPdfSavepath ctpPdfSavepath = new CtpPdfSavepath(edocSummary.getId(), reverPath);

        try {
            ctpPdfSavepathManager.deleteCtpPdfSavepath(ctpPdfSavepath);
            ctpPdfSavepathManager.insertCtpPdfSavepath(ctpPdfSavepath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Client client;
        String ftpdownload = "y:" + savepath.substring(savepath.indexOf("pdf") + 3).replaceAll("\\\\", "/");
        System.out.println("合并文件保存路径：" + ftpdownload);
        try {
            URL testUrl = new URL(url);
            HttpURLConnection huc = (HttpURLConnection) testUrl.openConnection();
            huc.setUseCaches(false);
            huc.setConnectTimeout(3000);
            int status = huc.getResponseCode();
            if (200 == status) {
                client = new Client(new URL(url));
                Object[] result = client.invoke("ConcatFiles",
                        new Object[]{mergepath, ftpdownload, 0, 5, url, "test"});
                System.out.println(result[0]);
                Document document = DocumentHelper.parseText((String) result[0]);
                Element rootElt = document.getRootElement();
                String ts = rootElt.elementText("result");
                System.out.println(ts);
                cbCode = Integer.parseInt(ts);
            }

        } catch (Exception e) {
            logger.error(e.getMessage() + "，转换服务出问题了 ~v~ 请联系管理员！");
        }
        return cbCode;
    }

    /**
     * 给前台渲染json数据
     *
     * @param response
     * @param text
     */
    private void render(HttpServletResponse response, String text) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.setContentLength(text.getBytes("UTF-8").length);
            response.getWriter().write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 插入文单域相应内容
     */
    private int insertFormRegionValue(EdocSummary edocSummary, String templetpath, String sFilePath) throws RemoteException {
        // edocSummary属性域
        StringBuffer sTarget = new StringBuffer();
        // edocSymmary属性域值
        StringBuffer sTargetValue = new StringBuffer();

        Map<String, Object> dataMap = new HashMap<String, Object>();
        if (edocSummary.getSubject() != null && !"".equals(edocSummary.getSubject())) {
            sTarget.append("subject").append("|");
            sTargetValue.append(edocSummary.getSubject()).append("|");
        }
        dataMap.put("subject", edocSummary.getSubject());

        if (edocSummary.getCreatePerson() != null && !"".equals(edocSummary.getCreatePerson())) {
            sTarget.append("create_person").append("|");
            sTargetValue.append(edocSummary.getCreatePerson()).append("|");
        }
        dataMap.put("create_person", edocSummary.getCreatePerson());

        if (edocSummary.getSerialNo() != null && !"".equals(edocSummary.getSerialNo())) {
            sTarget.append("serial_no").append("|");
            sTargetValue.append(edocSummary.getSerialNo()).append("|");
        }
        dataMap.put("serial_no", edocSummary.getSerialNo());

        if (edocSummary.getDocMark() != null && !"".equals(edocSummary.getDocMark())) {
            sTarget.append("doc_mark").append("|");
            sTargetValue.append(edocSummary.getDocMark()).append("|");
        }
        dataMap.put("doc_mark", edocSummary.getDocMark());

        if (edocSummary.getSendUnit() != null && !"".equals(edocSummary.getSendUnit())) {
            sTarget.append("send_unit").append("|");
            sTargetValue.append(edocSummary.getSendUnit()).append("|");
        }
        dataMap.put("send_unit", edocSummary.getSendUnit());

        if (edocSummary.getIssuer() != null && !"".equals(edocSummary.getIssuer())) {
            sTarget.append("issuer").append("|");
            sTargetValue.append(edocSummary.getIssuer()).append("|");
        }
        dataMap.put("issuer", edocSummary.getIssuer());

        if (edocSummary.getSendTo() != null && !"".equals(edocSummary.getSendTo())) {
            sTarget.append("send_to").append("|");
            sTargetValue.append(edocSummary.getSendTo()).append("|");
        }
        dataMap.put("send_to", edocSummary.getSendTo());

        if (edocSummary.getCopyTo() != null && !"".equals(edocSummary.getCopyTo())) {
            sTarget.append("copy_to").append("|");
            sTargetValue.append(edocSummary.getCopyTo()).append("|");

        }
        dataMap.put("copy_to", edocSummary.getCopyTo());

        if (edocSummary.getReportTo() != null && !"".equals(edocSummary.getReportTo())) {
            sTarget.append("report_to").append("|");
            sTargetValue.append(edocSummary.getReportTo()).append("|");

        }
        dataMap.put("report_to", edocSummary.getReportTo());

        if (edocSummary.getKeywords() != null && !"".equals(edocSummary.getKeywords())) {
            sTarget.append("keyword").append("|");
            sTargetValue.append(edocSummary.getKeywords()).append("|");

        }
        dataMap.put("keyword", edocSummary.getKeywords());

        if (edocSummary.getPrintUnit() != null && !"".equals(edocSummary.getPrintUnit())) {
            sTarget.append("print_unit").append("|");
            sTargetValue.append(edocSummary.getPrintUnit()).append("|");

        }
        dataMap.put("print_unit", edocSummary.getPrintUnit());

        if (edocSummary.getPrinter() != null && !"".equals(edocSummary.getPrinter())) {
            sTarget.append("printer").append("|");
            sTargetValue.append(edocSummary.getPrinter()).append("|");

        }
        dataMap.put("printer", edocSummary.getPrinter());

		/*
		if (edocSummary.getDocMark2() != null)
		{
			sTarget.append("doc_mark2").append("|");
			sTargetValue.append(edocSummary.getDocMark2()).append("|");
		}
		if (edocSummary.getSendTo2() != null)
		{
			sTarget.append("send_to2").append("|");
			sTargetValue.append(edocSummary.getSendTo2()).append("|");
		}
		if (edocSummary.getCopyTo2() != null)
		{
			sTarget.append("copy_to2").append("|");
			sTargetValue.append(edocSummary.getCopyTo2()).append("|");
		}
		if (edocSummary.getReportTo2() != null)
		{
			sTarget.append("report_to2").append("|");
			sTargetValue.append(edocSummary.getReportTo2()).append("|");
		}
		if (edocSummary.getSendUnit2() != null)
		{
			sTarget.append("send_unit2").append("|");
			sTargetValue.append(edocSummary.getSendUnit2()).append("|");
		}
		if (edocSummary.getSendDepartment2() != null)
		{
			sTarget.append("send_department2").append("|");
			sTargetValue.append(edocSummary.getSendDepartment2()).append("|");
		}*/

        // 0代表发文，发文需要send_department属性，1代表收文，收文不需要send_department属性
        if (edocSummary.getSendDepartment() != null && !"".equals(edocSummary.getSendDepartment()) && edocSummary.getEdocType() == 0) {
            sTarget.append("send_department").append("|");
            sTargetValue.append(edocSummary.getSendDepartment()).append("|");

        }
        dataMap.put("send_department", edocSummary.getSendDepartment());

        if (edocSummary.getFilesm() != null && !"".equals(edocSummary.getFilesm())) {
            sTarget.append("filesm").append("|");
            sTargetValue.append(edocSummary.getFilesm()).append("|");

        }
        dataMap.put("filesm", edocSummary.getFilesm());

        if (edocSummary.getFilefz() != null && !"".equals(edocSummary.getFilefz())) {
            sTarget.append("filefz").append("|");
            sTargetValue.append(edocSummary.getFilefz()).append("|");

        }
        dataMap.put("filefz", edocSummary.getFilefz());

        if (edocSummary.getPhone() != null && !"".equals(edocSummary.getPhone())) {
            sTarget.append("phone").append("|");
            sTargetValue.append(edocSummary.getPhone()).append("|");

        }
        dataMap.put("phone", edocSummary.getPhone());

        if (edocSummary.getAuditor() != null && !"".equals(edocSummary.getAuditor())) {
            sTarget.append("auditor").append("|");
            sTargetValue.append(edocSummary.getAuditor()).append("|");

        }
        dataMap.put("auditor", edocSummary.getAuditor());

        if (edocSummary.getReview() != null && !"".equals(edocSummary.getReview())) {
            sTarget.append("review").append("|");
            sTargetValue.append(edocSummary.getReview()).append("|");

        }
        dataMap.put("review", edocSummary.getReview());

        if (edocSummary.getUndertaker() != null && !"".equals(edocSummary.getUndertaker())) {
            sTarget.append("undertaker").append("|");
            sTargetValue.append(edocSummary.getUndertaker()).append("|");

        }
        dataMap.put("undertaker", edocSummary.getUndertaker());

        if (edocSummary.getAttachments() != null && !"".equals(edocSummary.getAttachments())) {
            sTarget.append("attachments").append("|");
            sTargetValue.append(edocSummary.getAttachments()).append("|");

        }
        dataMap.put("attachments", edocSummary.getAttachments());

        if (edocSummary.getCopies() != null && !"".equals(edocSummary.getCopies())) {
            sTarget.append("copies").append("|");
            sTargetValue.append(edocSummary.getCopies()).append("|");

        }
        dataMap.put("copies", edocSummary.getCopies());

		/*if (edocSummary.getCopies2() != null)
		{
			sTarget.append("copies2").append("|");
			sTargetValue.append(edocSummary.getCopies2()).append("|");
		}*/
        if (edocSummary.getSigningDate() != null && !"".equals(edocSummary.getSigningDate())) {
            sTarget.append("signing_date").append("|");
            sTargetValue.append(edocSummary.getSigningDate()).append("|");

        }
        dataMap.put("signing_date", edocSummary.getSigningDate());

		/*if (edocSummary.getCreateTime() != null)
		{
			sTarget.append("createdate").append("|");
			sTargetValue.append(edocSummary.getCreateTime()).append("|");
		}*/
        if (edocSummary.getPackTime() != null && !"".equals(edocSummary.getPackTime())) {
            sTarget.append("packdate").append("|");
            sTargetValue.append(edocSummary.getPackTime()).append("|");

        }
        dataMap.put("packdate", edocSummary.getPackTime());

        if (edocSummary.getReceiptDate() != null && !"".equals(edocSummary.getReceiptDate())) {
            sTarget.append("receipt_date").append("|");
            sTargetValue.append(edocSummary.getReceiptDate()).append("|");

        }
        dataMap.put("receipt_date", edocSummary.getReceiptDate());

        if (edocSummary.getRegistrationDate() != null && !"".equals(edocSummary.getRegistrationDate())) {
            sTarget.append("registration_date").append("|");
            sTargetValue.append(edocSummary.getRegistrationDate()).append("|");

        }
        dataMap.put("registration_date", edocSummary.getRegistrationDate());

        if (edocSummary.getDocType() != null && !"".equals(edocSummary.getDocType())) {
            sTarget.append("doc_type").append("|");
            sTargetValue.append(edocSummary.getDocType()).append("|");

        }
        dataMap.put("doc_type", edocSummary.getDocType());

        if (edocSummary.getSendType() != null && !"".equals(edocSummary.getSendType())) {
            sTarget.append("send_type").append("|");
            sTargetValue.append(edocSummary.getSendType()).append("|");

        }
        dataMap.put("send_type", edocSummary.getSendType());

        if (edocSummary.getSecretLevel() != null && !"".equals(edocSummary.getSecretLevel())) {
            sTarget.append("secret_level").append("|");

            if ("1".equals(edocSummary.getSecretLevel())) {
                sTargetValue.append("普通").append("|");
                dataMap.put("secret_level", "普通");

            } else if ("2".equals(edocSummary.getSecretLevel())) {
                sTargetValue.append("秘密").append("|");
                dataMap.put("secret_level", "秘密");

            } else if ("3".equals(edocSummary.getSecretLevel())) {
                sTargetValue.append("机密").append("|");
                dataMap.put("secret_level", "机密");

            } else {
                sTargetValue.append("绝密").append("|");
                dataMap.put("secret_level", "绝密");

            }
        }
        if ("1".equals(edocSummary.getSecretLevel())) {
            sTargetValue.append("普通").append("|");
            dataMap.put("secret_level", "普通");

        } else if ("2".equals(edocSummary.getSecretLevel())) {
            sTargetValue.append("秘密").append("|");
            dataMap.put("secret_level", "秘密");

        } else if ("3".equals(edocSummary.getSecretLevel())) {
            sTargetValue.append("机密").append("|");
            dataMap.put("secret_level", "机密");

        } else {
            sTargetValue.append("绝密").append("|");
            dataMap.put("secret_level", "绝密");

        }
        if (edocSummary.getUrgentLevel() != null && !"".equals(edocSummary.getUrgentLevel())) {
            sTarget.append("urgent_level").append("|");

            if ("1".equals(edocSummary.getUrgentLevel())) {
                sTargetValue.append("普通").append("|");
                dataMap.put("urgent_level", "普通");

            } else if ("2".equals(edocSummary.getUrgentLevel())) {
                sTargetValue.append("平急").append("|");
                dataMap.put("urgent_level", "平急");

            } else if ("3".equals(edocSummary.getUrgentLevel())) {
                sTargetValue.append("加急").append("|");
                dataMap.put("urgent_level", "加急");

            } else if ("4".equals(edocSummary.getUrgentLevel())) {
                sTargetValue.append("特急").append("|");
                dataMap.put("urgent_level", "特急");

            } else {
                sTargetValue.append("特提").append("|");
                dataMap.put("urgent_level", "特提");

            }
        }
        if ("1".equals(edocSummary.getUrgentLevel())) {
            sTargetValue.append("普通").append("|");
            dataMap.put("urgent_level", "普通");

        } else if ("2".equals(edocSummary.getUrgentLevel())) {
            sTargetValue.append("平急").append("|");
            dataMap.put("urgent_level", "平急");

        } else if ("3".equals(edocSummary.getUrgentLevel())) {
            sTargetValue.append("加急").append("|");
            dataMap.put("urgent_level", "加急");

        } else if ("4".equals(edocSummary.getUrgentLevel())) {
            sTargetValue.append("特急").append("|");
            dataMap.put("urgent_level", "特急");

        } else {
            sTargetValue.append("特提").append("|");
            dataMap.put("urgent_level", "特提");

        }
        if (edocSummary.getKeepPeriod() != null && !"".equals(edocSummary.getKeepPeriod())) {
            sTarget.append("keep_period").append("|");
            sTargetValue.append(edocSummary.getKeepPeriod()).append("|");
        }
        dataMap.put("keep_period", edocSummary.getKeepPeriod());

        if (edocSummary.getVarchar1() != null && !"".equals(edocSummary.getVarchar1())) {
            sTarget.append("string1").append("|");
            sTargetValue.append(edocSummary.getVarchar1()).append("|");

        }
        dataMap.put("string1", edocSummary.getVarchar1());

        if (edocSummary.getVarchar2() != null && !"".equals(edocSummary.getVarchar2())) {
            sTarget.append("string2").append("|");
            sTargetValue.append(edocSummary.getVarchar2()).append("|");

        }
        dataMap.put("string2", edocSummary.getVarchar2());

        if (edocSummary.getVarchar3() != null && !"".equals(edocSummary.getVarchar3())) {
            sTarget.append("string3").append("|");
            sTargetValue.append(edocSummary.getVarchar3()).append("|");

        }
        dataMap.put("string3", edocSummary.getVarchar3());

        if (edocSummary.getVarchar4() != null && !"".equals(edocSummary.getVarchar4())) {
            sTarget.append("string4").append("|");
            sTargetValue.append(edocSummary.getVarchar4()).append("|");

        }
        dataMap.put("string4", edocSummary.getVarchar4());

        if (edocSummary.getVarchar5() != null && !"".equals(edocSummary.getVarchar5())) {
            sTarget.append("string5").append("|");
            sTargetValue.append(edocSummary.getVarchar5()).append("|");

        }
        dataMap.put("string5", edocSummary.getVarchar5());

        if (edocSummary.getVarchar6() != null && !"".equals(edocSummary.getVarchar6())) {
            sTarget.append("string6").append("|");
            sTargetValue.append(edocSummary.getVarchar6()).append("|");

        }
        dataMap.put("string6", edocSummary.getVarchar6());

        if (edocSummary.getVarchar7() != null && !"".equals(edocSummary.getVarchar7())) {
            sTarget.append("string7").append("|");
            sTargetValue.append(edocSummary.getVarchar7()).append("|");

        }
        dataMap.put("string7", edocSummary.getVarchar7());

        if (edocSummary.getVarchar8() != null && !"".equals(edocSummary.getVarchar8())) {
            sTarget.append("string8").append("|");
            sTargetValue.append(edocSummary.getVarchar8()).append("|");

        }
        dataMap.put("string8", edocSummary.getVarchar8());

        if (edocSummary.getVarchar9() != null && !"".equals(edocSummary.getVarchar9())) {
            sTarget.append("string9").append("|");
            sTargetValue.append(edocSummary.getVarchar9()).append("|");

        }
        dataMap.put("string9", edocSummary.getVarchar9());

        if (edocSummary.getVarchar10() != null && !"".equals(edocSummary.getVarchar10())) {
            sTarget.append("string10").append("|");
            sTargetValue.append(edocSummary.getVarchar10()).append("|");

        }
        dataMap.put("string10", edocSummary.getVarchar10());

        if (edocSummary.getVarchar11() != null && !"".equals(edocSummary.getVarchar11())) {
            sTarget.append("string11").append("|");
            sTargetValue.append(edocSummary.getVarchar11()).append("|");

        }
        dataMap.put("string11", edocSummary.getVarchar11());

        if (edocSummary.getVarchar12() != null && !"".equals(edocSummary.getVarchar12())) {
            sTarget.append("string12").append("|");
            sTargetValue.append(edocSummary.getVarchar12()).append("|");

        }
        dataMap.put("string12", edocSummary.getVarchar12());

        if (edocSummary.getVarchar13() != null && !"".equals(edocSummary.getVarchar13())) {
            sTarget.append("string13").append("|");
            sTargetValue.append(edocSummary.getVarchar13()).append("|");

        }
        dataMap.put("string13", edocSummary.getVarchar13());

        if (edocSummary.getVarchar14() != null && !"".equals(edocSummary.getVarchar14())) {
            sTarget.append("string14").append("|");
            sTargetValue.append(edocSummary.getVarchar14()).append("|");

        }
        dataMap.put("string14", edocSummary.getVarchar14());

        if (edocSummary.getVarchar15() != null && !"".equals(edocSummary.getVarchar15())) {
            sTarget.append("string15").append("|");
            sTargetValue.append(edocSummary.getVarchar15()).append("|");

        }
        dataMap.put("string15", edocSummary.getVarchar15());

        if (edocSummary.getVarchar16() != null && !"".equals(edocSummary.getVarchar16())) {
            sTarget.append("string16").append("|");
            sTargetValue.append(edocSummary.getVarchar16()).append("|");

        }
        dataMap.put("string16", edocSummary.getVarchar16());

        if (edocSummary.getVarchar17() != null && !"".equals(edocSummary.getVarchar17())) {
            sTarget.append("string17").append("|");
            sTargetValue.append(edocSummary.getVarchar17()).append("|");

        }
        dataMap.put("string17", edocSummary.getVarchar17());

        if (edocSummary.getVarchar18() != null && !"".equals(edocSummary.getVarchar18())) {
            sTarget.append("string18").append("|");
            sTargetValue.append(edocSummary.getVarchar18()).append("|");

        }
        dataMap.put("string18", edocSummary.getVarchar18());

        if (edocSummary.getVarchar19() != null && !"".equals(edocSummary.getVarchar19())) {
            sTarget.append("string19").append("|");
            sTargetValue.append(edocSummary.getVarchar19()).append("|");

        }
        dataMap.put("string19", edocSummary.getVarchar19());

        if (edocSummary.getVarchar20() != null && !"".equals(edocSummary.getVarchar20())) {
            sTarget.append("string20").append("|");
            sTargetValue.append(edocSummary.getVarchar20()).append("|");

        }
        dataMap.put("string20", edocSummary.getVarchar20());

        if (edocSummary.getVarchar21() != null && !"".equals(edocSummary.getVarchar21())) {
            sTarget.append("string21").append("|");
            sTargetValue.append(edocSummary.getVarchar21()).append("|");

        }
        dataMap.put("string21", edocSummary.getVarchar21());

        if (edocSummary.getVarchar22() != null && !"".equals(edocSummary.getVarchar22())) {
            sTarget.append("string22").append("|");
            sTargetValue.append(edocSummary.getVarchar22()).append("|");
        }
        dataMap.put("string22", edocSummary.getVarchar22());

        if (edocSummary.getVarchar23() != null && !"".equals(edocSummary.getVarchar23())) {
            sTarget.append("string23").append("|");
            sTargetValue.append(edocSummary.getVarchar23()).append("|");

        }
        dataMap.put("string23", edocSummary.getVarchar23());

        if (edocSummary.getVarchar24() != null && !"".equals(edocSummary.getVarchar24())) {
            sTarget.append("string24").append("|");
            sTargetValue.append(edocSummary.getVarchar24()).append("|");

        }
        dataMap.put("string24", edocSummary.getVarchar24());

        if (edocSummary.getVarchar25() != null && !"".equals(edocSummary.getVarchar25())) {
            sTarget.append("string25").append("|");
            sTargetValue.append(edocSummary.getVarchar25()).append("|");

        }
        dataMap.put("string25", edocSummary.getVarchar25());

        if (edocSummary.getVarchar26() != null && !"".equals(edocSummary.getVarchar26())) {
            sTarget.append("string26").append("|");
            sTargetValue.append(edocSummary.getVarchar26()).append("|");

        }
        dataMap.put("string26", edocSummary.getVarchar26());

        if (edocSummary.getVarchar27() != null && !"".equals(edocSummary.getVarchar27())) {
            sTarget.append("string27").append("|");
            sTargetValue.append(edocSummary.getVarchar27()).append("|");

        }
        dataMap.put("string27", edocSummary.getVarchar27());

        if (edocSummary.getVarchar28() != null && !"".equals(edocSummary.getVarchar28())) {
            sTarget.append("string28").append("|");
            sTargetValue.append(edocSummary.getVarchar28()).append("|");

        }
        dataMap.put("string28", edocSummary.getVarchar28());

        if (edocSummary.getVarchar29() != null && !"".equals(edocSummary.getVarchar29())) {
            sTarget.append("string29").append("|");
            sTargetValue.append(edocSummary.getVarchar29()).append("|");

        }
        dataMap.put("string29", edocSummary.getVarchar29());

        if (edocSummary.getVarchar30() != null && !"".equals(edocSummary.getVarchar30())) {
            sTarget.append("string30").append("|");
            sTargetValue.append(edocSummary.getVarchar30()).append("|");

        }
        dataMap.put("string30", edocSummary.getVarchar30());

        if (edocSummary.getText1() != null && !"".equals(edocSummary.getText1())) {
            sTarget.append("text1").append("|");
            sTargetValue.append(edocSummary.getText1()).append("|");

        }
        dataMap.put("text1", edocSummary.getText1());

        if (edocSummary.getText2() != null && !"".equals(edocSummary.getText2())) {
            sTarget.append("text2").append("|");
            sTargetValue.append(edocSummary.getText2()).append("|");

        }
        dataMap.put("text2", edocSummary.getText2());

        if (edocSummary.getText3() != null && !"".equals(edocSummary.getText3())) {
            sTarget.append("text3").append("|");
            sTargetValue.append(edocSummary.getText3()).append("|");

        }
        dataMap.put("text3", edocSummary.getText3());

        if (edocSummary.getText4() != null && !"".equals(edocSummary.getText4())) {
            sTarget.append("text4").append("|");
            sTargetValue.append(edocSummary.getText4()).append("|");

        }
        dataMap.put("text4", edocSummary.getText4());

        if (edocSummary.getText5() != null && !"".equals(edocSummary.getText5())) {
            sTarget.append("text5").append("|");
            sTargetValue.append(edocSummary.getText5()).append("|");

        }
        dataMap.put("text5", edocSummary.getText5());

        if (edocSummary.getText6() != null && !"".equals(edocSummary.getText6())) {
            sTarget.append("text6").append("|");
            sTargetValue.append(edocSummary.getText6()).append("|");

        }
        dataMap.put("text6", edocSummary.getText6());

        if (edocSummary.getText7() != null && !"".equals(edocSummary.getText7())) {
            sTarget.append("text7").append("|");
            sTargetValue.append(edocSummary.getText7()).append("|");

        }
        dataMap.put("text7", edocSummary.getText7());

        if (edocSummary.getText8() != null && !"".equals(edocSummary.getText8())) {
            sTarget.append("text8").append("|");
            sTargetValue.append(edocSummary.getText8()).append("|");

        }
        dataMap.put("text8", edocSummary.getText8());

        if (edocSummary.getText9() != null && !"".equals(edocSummary.getText9())) {
            sTarget.append("text9").append("|");
            sTargetValue.append(edocSummary.getText9()).append("|");

        }
        dataMap.put("text9", edocSummary.getText9());

        if (edocSummary.getText10() != null && !"".equals(edocSummary.getText10())) {
            sTarget.append("text10").append("|");
            sTargetValue.append(edocSummary.getText10()).append("|");

        }
        dataMap.put("text10", edocSummary.getText10());

        if (edocSummary.getText11() != null && !"".equals(edocSummary.getText11())) {
            sTarget.append("text11").append("|");
            sTargetValue.append(edocSummary.getText11()).append("|");

        }
        dataMap.put("text11", edocSummary.getText11());

        if (edocSummary.getText12() != null && !"".equals(edocSummary.getText12())) {
            sTarget.append("text12").append("|");
            sTargetValue.append(edocSummary.getText12()).append("|");

        }
        dataMap.put("text12", edocSummary.getText12());

        if (edocSummary.getText13() != null && !"".equals(edocSummary.getText13())) {
            sTarget.append("text13").append("|");
            sTargetValue.append(edocSummary.getText13()).append("|");

        }
        dataMap.put("text13", edocSummary.getText13());

        if (edocSummary.getText14() != null && !"".equals(edocSummary.getText14())) {
            sTarget.append("text14").append("|");
            sTargetValue.append(edocSummary.getText14()).append("|");

        }
        dataMap.put("text14", edocSummary.getText14());

        if (edocSummary.getText15() != null && !"".equals(edocSummary.getText15())) {
            sTarget.append("text15").append("|");
            sTargetValue.append(edocSummary.getText15()).append("|");

        }
        dataMap.put("text15", edocSummary.getText15());

        if (edocSummary.getInteger1() != null && !"".equals(edocSummary.getInteger1())) {
            sTarget.append("integer1").append("|");
            sTargetValue.append(edocSummary.getInteger1()).append("|");

        }
        dataMap.put("integer1", edocSummary.getInteger1());

        if (edocSummary.getInteger2() != null && !"".equals(edocSummary.getInteger2())) {
            sTarget.append("integer2").append("|");
            sTargetValue.append(edocSummary.getInteger2()).append("|");

        }
        dataMap.put("integer2", edocSummary.getInteger2());

        if (edocSummary.getInteger3() != null && !"".equals(edocSummary.getInteger3())) {
            sTarget.append("integer3").append("|");
            sTargetValue.append(edocSummary.getInteger3()).append("|");

        }
        dataMap.put("integer3", edocSummary.getInteger3());

        if (edocSummary.getInteger4() != null && !"".equals(edocSummary.getInteger4())) {
            sTarget.append("integer4").append("|");
            sTargetValue.append(edocSummary.getInteger4()).append("|");

        }
        dataMap.put("integer4", edocSummary.getInteger4());

        if (edocSummary.getInteger5() != null && !"".equals(edocSummary.getInteger5())) {
            sTarget.append("integer5").append("|");
            sTargetValue.append(edocSummary.getInteger5()).append("|");

        }
        dataMap.put("integer5", edocSummary.getInteger5());

        if (edocSummary.getInteger6() != null && !"".equals(edocSummary.getInteger6())) {
            sTarget.append("integer6").append("|");
            sTargetValue.append(edocSummary.getInteger6()).append("|");

        }
        dataMap.put("integer6", edocSummary.getInteger6());

        if (edocSummary.getInteger7() != null && !"".equals(edocSummary.getInteger7())) {
            sTarget.append("integer7").append("|");
            sTargetValue.append(edocSummary.getInteger7()).append("|");

        }
        dataMap.put("integer7", edocSummary.getInteger7());

        if (edocSummary.getInteger8() != null && !"".equals(edocSummary.getInteger8())) {
            sTarget.append("integer8").append("|");
            sTargetValue.append(edocSummary.getInteger8()).append("|");

        }
        dataMap.put("integer8", edocSummary.getInteger8());

        if (edocSummary.getInteger9() != null && !"".equals(edocSummary.getInteger9())) {
            sTarget.append("integer9").append("|");
            sTargetValue.append(edocSummary.getInteger9()).append("|");

        }
        dataMap.put("integer9", edocSummary.getInteger9());

        if (edocSummary.getInteger10() != null && !"".equals(edocSummary.getInteger10())) {
            sTarget.append("integer10").append("|");
            sTargetValue.append(edocSummary.getInteger10()).append("|");

        }
        dataMap.put("integer10", edocSummary.getInteger10());

        if (edocSummary.getInteger11() != null && !"".equals(edocSummary.getInteger11())) {
            sTarget.append("integer11").append("|");
            sTargetValue.append(edocSummary.getInteger11()).append("|");

        }
        dataMap.put("integer11", edocSummary.getInteger11());

        if (edocSummary.getInteger12() != null && !"".equals(edocSummary.getInteger12())) {
            sTarget.append("integer12").append("|");
            sTargetValue.append(edocSummary.getInteger12()).append("|");

        }
        dataMap.put("integer12", edocSummary.getInteger12());

        if (edocSummary.getInteger13() != null && !"".equals(edocSummary.getInteger13())) {
            sTarget.append("integer13").append("|");
            sTargetValue.append(edocSummary.getInteger13()).append("|");

        }
        dataMap.put("integer13", edocSummary.getInteger13());

        if (edocSummary.getInteger14() != null && !"".equals(edocSummary.getInteger14())) {
            sTarget.append("integer14").append("|");
            sTargetValue.append(edocSummary.getInteger14()).append("|");

        }
        dataMap.put("integer14", edocSummary.getInteger14());

        if (edocSummary.getInteger15() != null && !"".equals(edocSummary.getInteger15())) {
            sTarget.append("integer15").append("|");
            sTargetValue.append(edocSummary.getInteger15()).append("|");

        }
        dataMap.put("integer15", edocSummary.getInteger15());

        if (edocSummary.getInteger16() != null && !"".equals(edocSummary.getInteger16())) {
            sTarget.append("integer16").append("|");
            sTargetValue.append(edocSummary.getInteger16()).append("|");

        }
        dataMap.put("integer16", edocSummary.getInteger16());

        if (edocSummary.getInteger17() != null && !"".equals(edocSummary.getInteger17())) {
            sTarget.append("integer17").append("|");
            sTargetValue.append(edocSummary.getInteger17()).append("|");

        }
        dataMap.put("integer17", edocSummary.getInteger17());

        if (edocSummary.getInteger18() != null && !"".equals(edocSummary.getInteger18())) {
            sTarget.append("integer18").append("|");
            sTargetValue.append(edocSummary.getInteger18()).append("|");

        }
        dataMap.put("integer18", edocSummary.getInteger18());

        if (edocSummary.getInteger19() != null && !"".equals(edocSummary.getInteger19())) {
            sTarget.append("integer19").append("|");
            sTargetValue.append(edocSummary.getInteger19()).append("|");

        }
        dataMap.put("integer19", edocSummary.getInteger19());

        if (edocSummary.getInteger20() != null && !"".equals(edocSummary.getInteger20())) {
            sTarget.append("integer20").append("|");
            sTargetValue.append(edocSummary.getInteger20()).append("|");

        }
        dataMap.put("integer20", edocSummary.getInteger20());

        if (edocSummary.getDecimal1() != null && !"".equals(edocSummary.getDecimal1())) {
            sTarget.append("decimal1").append("|");
            sTargetValue.append(edocSummary.getDecimal1()).append("|");

        }
        dataMap.put("decimal1", edocSummary.getDecimal1());

        if (edocSummary.getDecimal2() != null && !"".equals(edocSummary.getDecimal2())) {
            sTarget.append("decimal2").append("|");
            sTargetValue.append(edocSummary.getDecimal2()).append("|");

        }
        dataMap.put("decimal2", edocSummary.getDecimal2());

        if (edocSummary.getDecimal3() != null && !"".equals(edocSummary.getDecimal3())) {
            sTarget.append("decimal3").append("|");
            sTargetValue.append(edocSummary.getDecimal3()).append("|");

        }
        dataMap.put("decimal3", edocSummary.getDecimal3());

        if (edocSummary.getDecimal4() != null && !"".equals(edocSummary.getDecimal4())) {
            sTarget.append("decimal4").append("|");
            sTargetValue.append(edocSummary.getDecimal4()).append("|");

        }
        dataMap.put("decimal4", edocSummary.getDecimal4());

        if (edocSummary.getDecimal5() != null && !"".equals(edocSummary.getDecimal5())) {
            sTarget.append("decimal5").append("|");
            sTargetValue.append(edocSummary.getDecimal5()).append("|");

        }
        dataMap.put("decimal5", edocSummary.getDecimal5());

        if (edocSummary.getDecimal6() != null && !"".equals(edocSummary.getDecimal6())) {
            sTarget.append("decimal6").append("|");
            sTargetValue.append(edocSummary.getDecimal6()).append("|");

        }
        dataMap.put("decimal6", edocSummary.getDecimal6());

        if (edocSummary.getDecimal7() != null && !"".equals(edocSummary.getDecimal7())) {
            sTarget.append("decimal7").append("|");
            sTargetValue.append(edocSummary.getDecimal7()).append("|");

        }
        dataMap.put("decimal7", edocSummary.getDecimal7());

        if (edocSummary.getDecimal8() != null && !"".equals(edocSummary.getDecimal8())) {
            sTarget.append("decimal8").append("|");
            sTargetValue.append(edocSummary.getDecimal8()).append("|");

        }
        dataMap.put("decimal8", edocSummary.getDecimal8());

        if (edocSummary.getDecimal9() != null && !"".equals(edocSummary.getDecimal9())) {
            sTarget.append("decimal9").append("|");
            sTargetValue.append(edocSummary.getDecimal9()).append("|");

        }
        dataMap.put("decimal9", edocSummary.getDecimal9());

        if (edocSummary.getDecimal10() != null && !"".equals(edocSummary.getDecimal10())) {
            sTarget.append("decimal10").append("|");
            sTargetValue.append(edocSummary.getDecimal10()).append("|");

        }
        dataMap.put("decimal10", edocSummary.getDecimal10());

        if (edocSummary.getDecimal11() != null && !"".equals(edocSummary.getDecimal11())) {
            sTarget.append("decimal11").append("|");
            sTargetValue.append(edocSummary.getDecimal11()).append("|");

        }
        dataMap.put("decimal11", edocSummary.getDecimal11());

        if (edocSummary.getDecimal12() != null && !"".equals(edocSummary.getDecimal12())) {
            sTarget.append("decimal12").append("|");
            sTargetValue.append(edocSummary.getDecimal12()).append("|");

        }
        dataMap.put("decimal12", edocSummary.getDecimal12());

        if (edocSummary.getDecimal13() != null && !"".equals(edocSummary.getDecimal13())) {
            sTarget.append("decimal13").append("|");
            sTargetValue.append(edocSummary.getDecimal13()).append("|");

        }
        dataMap.put("decimal13", edocSummary.getDecimal13());

        if (edocSummary.getDecimal14() != null && !"".equals(edocSummary.getDecimal14())) {
            sTarget.append("decimal14").append("|");
            sTargetValue.append(edocSummary.getDecimal14()).append("|");

        }
        dataMap.put("decimal14", edocSummary.getDecimal14());

        if (edocSummary.getDecimal15() != null && !"".equals(edocSummary.getDecimal15())) {
            sTarget.append("decimal15").append("|");
            sTargetValue.append(edocSummary.getDecimal15()).append("|");

        }
        dataMap.put("decimal15", edocSummary.getDecimal15());

        if (edocSummary.getDecimal16() != null && !"".equals(edocSummary.getDecimal16())) {
            sTarget.append("decimal16").append("|");
            sTargetValue.append(edocSummary.getDecimal16()).append("|");

        }
        dataMap.put("decimal16", edocSummary.getDecimal16());

        if (edocSummary.getDecimal17() != null && !"".equals(edocSummary.getDecimal17())) {
            sTarget.append("decimal17").append("|");
            sTargetValue.append(edocSummary.getDecimal17()).append("|");

        }
        dataMap.put("decimal17", edocSummary.getDecimal17());

        if (edocSummary.getDecimal18() != null && !"".equals(edocSummary.getDecimal18())) {
            sTarget.append("decimal18").append("|");
            sTargetValue.append(edocSummary.getDecimal18()).append("|");

        }
        dataMap.put("decimal18", edocSummary.getDecimal18());

        if (edocSummary.getDecimal19() != null && !"".equals(edocSummary.getDecimal19())) {
            sTarget.append("decimal19").append("|");
            sTargetValue.append(edocSummary.getDecimal19()).append("|");

        }
        dataMap.put("decimal19", edocSummary.getDecimal19());

        if (edocSummary.getDecimal20() != null && !"".equals(edocSummary.getDecimal20())) {
            sTarget.append("decimal20").append("|");
            sTargetValue.append(edocSummary.getDecimal20()).append("|");

        }
        dataMap.put("decimal20", edocSummary.getDecimal20());

        if (edocSummary.getDate1() != null && !"".equals(edocSummary.getDate1())) {
            sTarget.append("date1").append("|");
            sTargetValue.append(edocSummary.getDate1()).append("|");

        }
        dataMap.put("date1", edocSummary.getDate1());

        if (edocSummary.getDate2() != null && !"".equals(edocSummary.getDate2())) {
            sTarget.append("date2").append("|");
            sTargetValue.append(edocSummary.getDate2()).append("|");

        }
        dataMap.put("date2", edocSummary.getDate2());

        if (edocSummary.getDate3() != null && !"".equals(edocSummary.getDate3())) {
            sTarget.append("date3").append("|");
            sTargetValue.append(edocSummary.getDate3()).append("|");

        }
        dataMap.put("date3", edocSummary.getDate3());

        if (edocSummary.getDate4() != null && !"".equals(edocSummary.getDate4())) {
            sTarget.append("date4").append("|");
            sTargetValue.append(edocSummary.getDate4()).append("|");

        }
        dataMap.put("date4", edocSummary.getDate4());

        if (edocSummary.getDate5() != null && !"".equals(edocSummary.getDate5())) {
            sTarget.append("date5").append("|");
            sTargetValue.append(edocSummary.getDate5()).append("|");

        }
        dataMap.put("date5", edocSummary.getDate5());

        if (edocSummary.getDate6() != null && !"".equals(edocSummary.getDate6())) {
            sTarget.append("date6").append("|");
            sTargetValue.append(edocSummary.getDate6()).append("|");

        }
        dataMap.put("date6", edocSummary.getDate6());

        if (edocSummary.getDate7() != null && !"".equals(edocSummary.getDate7())) {
            sTarget.append("date7").append("|");
            sTargetValue.append(edocSummary.getDate7()).append("|");

        }
        dataMap.put("date7", edocSummary.getDate7());

        if (edocSummary.getDate8() != null && !"".equals(edocSummary.getDate8())) {
            sTarget.append("date8").append("|");
            sTargetValue.append(edocSummary.getDate8()).append("|");

        }
        dataMap.put("date8", edocSummary.getDate8());

        if (edocSummary.getDate9() != null && !"".equals(edocSummary.getDate9())) {
            sTarget.append("date9").append("|");
            sTargetValue.append(edocSummary.getDate9()).append("|");

        }
        dataMap.put("date9", edocSummary.getDate9());

        if (edocSummary.getDate10() != null && !"".equals(edocSummary.getDate10())) {
            sTarget.append("date10").append("|");
            sTargetValue.append(edocSummary.getDate10()).append("|");

        }
        dataMap.put("date10", edocSummary.getDate10());

        if (edocSummary.getDate11() != null && !"".equals(edocSummary.getDate11())) {
            sTarget.append("date11").append("|");
            sTargetValue.append(edocSummary.getDate11()).append("|");

        }
        dataMap.put("date11", edocSummary.getDate11());

        if (edocSummary.getDate12() != null && !"".equals(edocSummary.getDate12())) {
            sTarget.append("date12").append("|");
            sTargetValue.append(edocSummary.getDate12()).append("|");

        }
        dataMap.put("date12", edocSummary.getDate12());

        if (edocSummary.getDate13() != null && !"".equals(edocSummary.getDate13())) {
            sTarget.append("date13").append("|");
            sTargetValue.append(edocSummary.getDate13()).append("|");

        }
        dataMap.put("date13", edocSummary.getDate13());

        if (edocSummary.getDate14() != null && !"".equals(edocSummary.getDate14())) {
            sTarget.append("date14").append("|");
            sTargetValue.append(edocSummary.getDate14()).append("|");

        }
        dataMap.put("date14", edocSummary.getDate14());

        if (edocSummary.getDate15() != null && !"".equals(edocSummary.getDate15())) {
            sTarget.append("date15").append("|");
            sTargetValue.append(edocSummary.getDate15()).append("|");

        }
        dataMap.put("date15", edocSummary.getDate15());

        if (edocSummary.getDate16() != null && !"".equals(edocSummary.getDate16())) {
            sTarget.append("date16").append("|");
            sTargetValue.append(edocSummary.getDate16()).append("|");

        }
        dataMap.put("date16", edocSummary.getDate16());

        if (edocSummary.getDate17() != null && !"".equals(edocSummary.getDate17())) {
            sTarget.append("date17").append("|");
            sTargetValue.append(edocSummary.getDate17()).append("|");

        }
        dataMap.put("date17", edocSummary.getDate17());

        if (edocSummary.getDate18() != null && !"".equals(edocSummary.getDate18())) {
            sTarget.append("date18").append("|");
            sTargetValue.append(edocSummary.getDate18()).append("|");

        }
        dataMap.put("date18", edocSummary.getDate18());

        if (edocSummary.getDate19() != null && !"".equals(edocSummary.getDate19())) {
            sTarget.append("date19").append("|");
            sTargetValue.append(edocSummary.getDate19()).append("|");

        }
        dataMap.put("date19", edocSummary.getDate19());

        if (edocSummary.getDate20() != null && !"".equals(edocSummary.getDate20())) {
            sTarget.append("date20").append("|");
            sTargetValue.append(edocSummary.getDate20()).append("|");

        }
        dataMap.put("date20", edocSummary.getDate20());

        if (edocSummary.getList1() != null && !"".equals(edocSummary.getList1())) {
            sTarget.append("list1").append("|");
            sTargetValue.append(edocSummary.getList1()).append("|");

        }
        dataMap.put("list1", edocSummary.getList1());

        if (edocSummary.getList2() != null && !"".equals(edocSummary.getList2())) {
            sTarget.append("list2").append("|");
            sTargetValue.append(edocSummary.getList2()).append("|");

        }
        dataMap.put("list2", edocSummary.getList2());

        if (edocSummary.getList3() != null && !"".equals(edocSummary.getList3())) {
            sTarget.append("list3").append("|");
            sTargetValue.append(edocSummary.getList3()).append("|");

        }
        dataMap.put("list3", edocSummary.getList3());

        if (edocSummary.getList4() != null && !"".equals(edocSummary.getList4())) {
            sTarget.append("list4").append("|");
            sTargetValue.append(edocSummary.getList4()).append("|");

        }
        dataMap.put("list4", edocSummary.getList4());

        if (edocSummary.getList5() != null && !"".equals(edocSummary.getList5())) {
            sTarget.append("list5").append("|");
            sTargetValue.append(edocSummary.getList5()).append("|");

        }
        dataMap.put("list5", edocSummary.getList5());

        if (edocSummary.getList6() != null && !"".equals(edocSummary.getList6())) {
            sTarget.append("list6").append("|");
            sTargetValue.append(edocSummary.getList6()).append("|");

        }
        dataMap.put("list6", edocSummary.getList6());

        if (edocSummary.getList7() != null && !"".equals(edocSummary.getList7())) {
            sTarget.append("list7").append("|");
            sTargetValue.append(edocSummary.getList7()).append("|");

        }
        dataMap.put("list7", edocSummary.getList7());

        if (edocSummary.getList8() != null && !"".equals(edocSummary.getList8())) {
            sTarget.append("list8").append("|");
            sTargetValue.append(edocSummary.getList8()).append("|");

        }
        dataMap.put("list8", edocSummary.getList8());

        if (edocSummary.getList9() != null && !"".equals(edocSummary.getList9())) {
            sTarget.append("list9").append("|");
            sTargetValue.append(edocSummary.getList9()).append("|");

        }
        dataMap.put("list9", edocSummary.getList9());

        if (edocSummary.getList10() != null && !"".equals(edocSummary.getList10())) {
            sTarget.append("list10").append("|");
            sTargetValue.append(edocSummary.getList10()).append("|");

        }
        dataMap.put("list10", edocSummary.getList10());

        if (edocSummary.getList11() != null && !"".equals(edocSummary.getList11())) {
            sTarget.append("list11").append("|");
            sTargetValue.append(edocSummary.getList11()).append("|");

        }
        dataMap.put("list11", edocSummary.getList11());

        if (edocSummary.getList12() != null && !"".equals(edocSummary.getList12())) {
            sTarget.append("list12").append("|");
            sTargetValue.append(edocSummary.getList12()).append("|");

        }
        dataMap.put("list12", edocSummary.getList12());

        if (edocSummary.getList13() != null && !"".equals(edocSummary.getList13())) {
            sTarget.append("list13").append("|");
            sTargetValue.append(edocSummary.getList13()).append("|");

        }
        dataMap.put("list13", edocSummary.getList13());

        if (edocSummary.getList14() != null && !"".equals(edocSummary.getList14())) {
            sTarget.append("list14").append("|");
            sTargetValue.append(edocSummary.getList14()).append("|");

        }
        dataMap.put("list14", edocSummary.getList14());

        if (edocSummary.getList15() != null && !"".equals(edocSummary.getList15())) {
            sTarget.append("list15").append("|");
            sTargetValue.append(edocSummary.getList15()).append("|");

        }
        dataMap.put("list15", edocSummary.getList15());

        if (edocSummary.getList16() != null && !"".equals(edocSummary.getList16())) {
            sTarget.append("list16").append("|");
            sTargetValue.append(edocSummary.getList16()).append("|");

        }
        dataMap.put("list16", edocSummary.getList16());

        if (edocSummary.getList17() != null && !"".equals(edocSummary.getList17())) {
            sTarget.append("list17").append("|");
            sTargetValue.append(edocSummary.getList17()).append("|");

        }
        dataMap.put("list17", edocSummary.getList17());

        if (edocSummary.getList18() != null && !"".equals(edocSummary.getList18())) {
            sTarget.append("list18").append("|");
            sTargetValue.append(edocSummary.getList18()).append("|");

        }
        dataMap.put("list18", edocSummary.getList18());

        if (edocSummary.getList19() != null && !"".equals(edocSummary.getList19())) {
            sTarget.append("list19").append("|");
            sTargetValue.append(edocSummary.getList19()).append("|");

        }
        dataMap.put("list19", edocSummary.getList19());

        if (edocSummary.getList20() != null && !"".equals(edocSummary.getList20())) {
            sTarget.append("list20").append("|");
            sTargetValue.append(edocSummary.getList20()).append("|");

        }
        dataMap.put("list20", edocSummary.getList20());


        System.out.println("sTarget::" + sTarget.substring(0, sTarget.lastIndexOf("|")).toString());
        System.out.println("sTargetValue::" + sTargetValue.substring(0, sTargetValue.lastIndexOf("|")).toString());


        int flag = 0;
        /**第二种方法：采用freemark模板 */
        Configuration configuration = new Configuration();
        try {
            configuration.setDefaultEncoding("UTF-8");
            configuration.setDirectoryForTemplateLoading(new File(templetpath));
            String wordpath = sFilePath + edocSummary.getId() + ".doc";
            File outFile = new File(wordpath);
            File fileParent = outFile.getParentFile();
            if (!fileParent.exists()) {
                fileParent.mkdirs();
            }
            Template template = configuration.getTemplate(edocSummary.getFormId().toString() + ".ftl", "utf-8");
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"), 10240);
            template.process(dataMap, out);
            out.close();
        } catch (IOException e) {
            flag = -2;
            logger.error(e.getMessage() + ",模板不存在！");
        } catch (Exception e) {
            flag = -2;
            logger.error(e.getMessage() + ",模板不存在！");
        }

        return flag;
    }


}
