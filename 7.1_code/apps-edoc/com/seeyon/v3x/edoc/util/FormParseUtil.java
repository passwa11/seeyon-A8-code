/**
 * Author : xuqw
 *   Date : 2015年6月15日 下午12:24:31
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.v3x.edoc.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.govdoc.option.manager.FormOptionExtendManager;
import com.seeyon.apps.govdoc.option.manager.FormOptionSortManager;
import com.seeyon.apps.govdoc.po.FormOptionExtend;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.content.mainbody.CtpContentAllBean;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocFormManager;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.webmodel.EdocOpinionModel;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;
import com.seeyon.v3x.system.signet.domain.V3xHtmDocumentSignature;
import com.seeyon.v3x.system.signet.enums.V3xHtmSignatureEnum;
import com.seeyon.v3x.system.signet.manager.V3xHtmDocumentSignatManager;

/**
 * <p>
 * Title : 应用模块名称
 * </p>
 * <p>
 * Description : 代码描述
 * </p>
 * <p>
 * Copyright : Copyright (c) 2012
 * </p>
 * <p>
 * Company : seeyon.com
 * </p>
 */
public class FormParseUtil {

    private static final Log LOGGER = LogFactory.getLog(FormParseUtil.class);

    /************ 基本业务信息定义 start *****************/

    // HTML文档类型
    // infopath采用的是杂项模式

    private static final String htmlBegin = "<html>";
    private static final String htmlEnd = "</html>";

    private static final String headBegin = "<head>";
    private static final String headEnd = "</head>";

    private static final String bodyBegin = "<body onload=\"_init_()\">";
    private static final String bodyEnd = "</body>";
    
    /** 公文单元素的前缀  **/
    private static final String FIELD_PREFIX = "my:";

    // 截取正则
    private static  Pattern templatePatt = Pattern.compile("<xsl:template[\\s\\S]*?>([\\s\\S]*?)</xsl:template>",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    // 文单内图片正则
    private static Pattern nomalImgPatt = Pattern.compile("<img[^>]*?srcId=\"([^>]*?)\"[^>]*?>", Pattern.CASE_INSENSITIVE
            | Pattern.MULTILINE);

    // 截取字段正则
    private static  Pattern fieldSpanPatt = Pattern.compile("<span([^>]*?xd:binding=\"my:(.*?)\"[\\s\\S]*?)>[\\s\\S]*?</span>",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    // 截取字段正则2
    private static  Pattern fieldSpanPatt2 = Pattern.compile("<span([^>]*?id='(field\\d+)'[\\s\\S]*?)>([\\s\\S]*?)</span>",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    
    // 附件图标正则
    private static  Pattern attImgPatt = Pattern.compile("(<img[^>]*?src=')([^>]*?\\/attachmentICON\\/[^>]*?)('[^>]*?>)");

    // 意见附件正则
    private static  Pattern attFilePatt = Pattern
            .compile("(<a [^>]*?)href='[^>]*?fileDownload\\.do[^>]*?fileId=.+?&[^>]*?'([^>]*?>)");

    // 图片签名正则
    private static  Pattern picSignPatt = Pattern.compile("<img[^>]*?src=\"[^>]*?id=([^>]*?)\"[^>]*?>",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    // 提取一个地址中的文件ID
    private static  Pattern ctpFileIdPatt = Pattern.compile("fileId=([-]?\\d+)");

    /************ 基本业务信息定义 end *****************/

    /**
     * 将文单写入文件
     * 
     * @Author : xuqw
     * @Date : 2015年6月15日上午12:25:36
     * @param summaryId
     * @param folder
     * @return
     * @throws Exception
     */
    public static boolean writeForm2File(Long summaryId, String folder){

        boolean ret = true;

        try {

            EdocSummary summary = getEdocSummary(summaryId);
            if (summary == null) {
                LOGGER.info("获取EdocSummary对象为空, ID=" + summaryId);
                ret = false;
            }

            String webRoot = SystemEnvironment.getApplicationFolder() + File.separator;
            String fileFolder = "files";// css和图片路径
            String attFolder = "attFiles";// 附件存放路径

            // archiveTempFolderPath = new
            // StringBuilder(SystemEnvironment.getSystemTempFolder()).append(File.separator).append(ArchiveUtil.archiveTempFolderName).toString();
            // File file = new File(SystemEnvironment.getApplicationFolder() +
            // File.separator + path);

            String root = folder.replaceAll("\\\\", "/").replaceAll("//", "/");
            if (!root.endsWith("/")) {
                root += "/";
            }

            String summaryRoot = root + summary.getId();

            File sFolder = new File(summaryRoot);
            if (sFolder.exists()) {
                FileUtils.cleanDirectory(sFolder);
            }

            String fFolder = summaryRoot + "/" + fileFolder;
            String aFolder = summaryRoot+ "/" + attFolder;

            // 创建文件夹
            createFolder(aFolder);
            createFolder(fFolder);

            FormParseExtInfo extInfo = formatFormContent(summary, fileFolder);

            // 将文单写成HTML
            File contentFile = new File(summaryRoot + "/form.html");
            FileUtils.writeStringToFile(contentFile, extInfo.getContent(), "utf-8");

            // 附件拷贝
            for (Attachment att : extInfo.getAttFiles()) {

                if (att.getType().intValue() != 2) {// 等于2是关联文档，需要过滤

                    FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
                    
                    V3XFile v3xFile = fileManager.getV3XFile(att.getFileUrl());
                    File file = fileManager.getFile(att.getFileUrl(), v3xFile.getCreateDate());

                    if (file != null) {
                        String fileName = att.getFilename();
                        File destFile = new File(aFolder + "/" + fileName);
                        int num = 1;
                        while (destFile.exists() && destFile.isFile()) {
                            destFile = new File(aFolder + "/" + num + fileName);
                            num++;
                        }

                        FileUtils.copyFile(file, destFile);

                    } else {
                        LOGGER.info("文件不存在， id=" + att.getFileUrl());
                    }
                }
            }

            Set<String> files = extInfo.getFiles();
            for (String file : files) {
                File aFile = new File(webRoot + file);
                if (aFile.exists()) {

                    String destFile = fFolder + "/" + getFilePath(file);
                    
                    File destFolder = createFolder(destFile);
                    FileUtils.copyFileToDirectory(aFile, destFolder);
                } else {
                    LOGGER.info("样式或图片不存在" + file);
                }
            }

            List<V3XFile> ctpFiles = extInfo.getCtpFiles();
            for (V3XFile ctpFile : ctpFiles) {

                copyV3xFile(ctpFile, fFolder);
            }

            Map<String, byte[]> byteFiles = extInfo.getByteFile();
            Iterator<String> bFileNames = byteFiles.keySet().iterator();
            while (bFileNames.hasNext()) {
                String name = bFileNames.next();
                byte[] bFile = byteFiles.get(name);

                File byteFile = new File(fFolder + "/" + name);

                FileUtils.writeByteArrayToFile(byteFile, bFile);
            }

        } catch (Exception e) {
            LOGGER.error("解析文单信息失败", e);
            ret = false;
        }

        return ret;
    }
    
    /**
     * 将文单写入文件(新公文)
     * 
     * @Author : caihl
     * @Date : 2017年9月7日
     * @param colSummary
     * @param folder
     * @return
     * @throws Exception
     */
    public static boolean writeForm2File2(Long summaryId,String folder){
    	boolean ret = true;

        try {
        	EdocSummary edocSummary = getEdocSummary(summaryId);
            if (edocSummary == null) {
                LOGGER.info("获取EdocSummary对象为空, ID=" + summaryId);
                ret = false;
            }
        	
            String webRoot = SystemEnvironment.getApplicationFolder() + File.separator;
            String fileFolder = "files";// css和图片路径
            String attFolder = "attFiles";// 附件存放路径

            String root = folder.replaceAll("\\\\", "/").replaceAll("//", "/");
            if (!root.endsWith("/")) {
                root += "/";
            }

            String summaryRoot = root + edocSummary.getId();

            File sFolder = new File(summaryRoot);
            if (sFolder.exists()) {
                FileUtils.cleanDirectory(sFolder);
            }

            String fFolder = summaryRoot + "/" + fileFolder;
            String aFolder = summaryRoot+ "/" + attFolder;

            // 创建文件夹
            createFolder(aFolder);
            createFolder(fFolder);

            FormParseExtInfo extInfo = formatFormContent2(edocSummary, fileFolder, fFolder);

            // 将文单写成HTML
            File contentFile = new File(summaryRoot + "/form.html");
            FileUtils.writeStringToFile(contentFile, extInfo.getContent(), "utf-8");

            // 附件拷贝
            for (Attachment att : extInfo.getAttFiles()) {

                if (att.getType().intValue() != 2) {// 等于2是关联文档，需要过滤

                    FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
                    
                    V3XFile v3xFile = fileManager.getV3XFile(att.getFileUrl());
                    File file = fileManager.getFile(att.getFileUrl(), v3xFile.getCreateDate());

                    if (file != null) {
                        String fileName = att.getFilename();
                        File destFile = new File(aFolder + "/" + fileName);
                        int num = 1;
                        while (destFile.exists() && destFile.isFile()) {
                            destFile = new File(aFolder + "/" + num + fileName);
                            num++;
                        }

                        FileUtils.copyFile(file, destFile);

                    } else {
                    	LOGGER.info("文件不存在， id=" + att.getFileUrl());
                    }
                }
            }

            Set<String> files = extInfo.getFiles();
            for (String file : files) {
                File aFile = new File(webRoot + file);
                if (aFile.exists()) {

                    String destFile = fFolder + "/" + getFilePath(file);
                    
                    File destFolder = createFolder(destFile);
                    FileUtils.copyFileToDirectory(aFile, destFolder);
                } else {
                	LOGGER.info("样式或图片不存在" + file);
                }
            }

            List<V3XFile> ctpFiles = extInfo.getCtpFiles();
            for (V3XFile ctpFile : ctpFiles) {

                copyV3xFile(ctpFile, fFolder);
            }

            Map<String, byte[]> byteFiles = extInfo.getByteFile();
            Iterator<String> bFileNames = byteFiles.keySet().iterator();
            while (bFileNames.hasNext()) {
                String name = bFileNames.next();
                byte[] bFile = byteFiles.get(name);

                File byteFile = new File(fFolder + "/" + name);

                FileUtils.writeByteArrayToFile(byteFile, bFile);
            }

        } catch (Exception e) {
        	LOGGER.error("解析文单信息失败", e);
            ret = false;
        }

        return ret;
    }

    /**
     * 获取文件的文件夹
     * @Author      : xuqw
     * @Date        : 2015年6月15日下午1:39:58
     * @param allPath
     * @return
     */
    public static String getFilePath(String allPath) {

        String ret = null;
        
        if(Strings.isBlank(allPath)){
            ret = "";
        }else{
            
            String srcPath = allPath.replace("\\\\", "/").replaceAll("//", "/");
            int index = srcPath.lastIndexOf("/");
            
            if (index != -1) {
                ret = srcPath.substring(0, index + 1);
            } else {
                ret = srcPath;
            }
        }
        
        return ret;
    }
    
    /**
     * 拷贝文件
     * 
     * @Author : xuqw
     * @Date : 2015年6月15日上午10:16:54
     * @param ctpFile
     * @param folder
     * @throws Exception
     */
    private static void copyV3xFile(V3XFile ctpFile, String folder) throws Exception {

        String[] urls = new String[1];
        urls[0] = ctpFile.getId().toString();

        String[] createDates = new String[1];
        createDates[0] = Datetimes.formatDatetime(ctpFile.getCreateDate());

        String[] mimeTypes = new String[1];
        mimeTypes[0] = ctpFile.getMimeType().toString();
        String[] names = new String[1];
        names[0] = ctpFile.getFilename().toString();

        String filePath = FormParseUtil.getDirectory(urls, createDates, mimeTypes, names);
        String newPath = folder + "/" + ctpFile.getId() + "decryption";

        File srcFile = new File(filePath);
        File destFile = new File(newPath);

        FileUtils.copyFile(srcFile, destFile);
    }

    /**
     * 获取V3x的物理路径
     * 
     * @Author : xuqw
     * @Date : 2015年6月15日上午9:45:22
     * @param urls
     * @param createDates
     * @param mimeTypes
     * @param names
     * @return
     * @throws Exception
     */
    private static String getDirectory(String[] urls, String[] createDates, String[] mimeTypes, String[] names)
            throws Exception {

        File file = null;
        try {
            for (int i = 0; i < urls.length; i++) {
                Long fileId = Long.parseLong(urls[i]);

                Date createDate = null;
                if (createDates[i].length() > 10)
                    createDate = Datetimes.parseDatetime(createDates[i]);
                else
                    createDate = Datetimes.parseDate(createDates[i]);
                
                FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
                file = fileManager.getFile(fileId, createDate);
                if (null == file)
                    return "";
            }
        } catch (Exception e) {
            LOGGER.error("获取公文单目录出错", e);
            return "";
        }

        return file.getPath();
    }

    /**
     * 创建文件夹
     * 
     * @Author : xuqw
     * @Date : 2015年6月15日上午1:03:24
     * @param path
     * @return
     */
    private static File createFolder(String path) {

        File ret = null;

        File file = new File(path);

        if (!file.exists()) {
            if (file.mkdirs()) {
                ret = file;
            }
        }else {
            ret = file;
        }
        return ret;
    }

    /**
     * 获取EdocSummary对象
     * 
     * @Author : xuqw
     * @Date : 2015年5月26日下午6:42:59
     * @param id
     * @return
     * @throws BusinessException
     */
    public static EdocSummary getEdocSummary(Long id) throws BusinessException {

        EdocManager edocManager = (EdocManager) AppContext.getBean("edocManager");
        AffairManager affairManager = (AffairManager) AppContext.getBean("affairManager");
        
        // 获取基本数据
        EdocSummary summary = edocManager.getEdocSummaryById(id, true);

        if (summary == null) {
            CtpAffair affair = affairManager.get(id);
            if (affair != null) {
                id = affair.getObjectId();
            }
            summary = edocManager.getEdocSummaryById(id, true);
        }

        return summary;
    }

    /**
     * 获取意见里面的附件信息
     * 
     * @Author : xuqw
     * @Date : 2015年6月14日下午3:20:45
     * @param field2OptionMap
     * @param containDraft 是否包含暂存待办意见
     * @return
     */
    private static Map<String, List<Attachment>> getOpinionAttMap(Map<String, EdocOpinionModel> field2OptionMap, boolean containDraft) {

        Map<String, List<Attachment>> opinionAttMap = new HashMap<String, List<Attachment>>();
        Iterator<String> opinionIter = field2OptionMap.keySet().iterator();
        while (opinionIter.hasNext()) {

            // 公文单上元素位置
            String fieldName = opinionIter.next();
            EdocOpinionModel model = field2OptionMap.get(fieldName);
            List<EdocOpinion> opinions = model.getOpinions();
            for (EdocOpinion opinion : opinions) {
                // 取回或者暂存待办的意见回写到意见框中，所以要跳过；其他情况下显示到意见区域
                if (opinion.getOpinionType().intValue() == EdocOpinion.OpinionType.provisionalOpinoin.ordinal()
                        || opinion.getOpinionType().intValue() == EdocOpinion.OpinionType.draftOpinion.ordinal()){
                	if(!containDraft){
                		continue;
                	}
                }

                List<Attachment> tempAtts = null;
                if (null != opinion.getPolicy() && opinion.getPolicy().equals(EdocOpinion.FEED_BACK)) {
                    Long subOpinionId = opinion.getSubOpinionId();
                    if (subOpinionId != null) {
                        tempAtts = EdocHelper.getOpinionAttachmentsNotRelationDoc(opinion.getSubEdocId(), subOpinionId);
                    }
                } else {
                    tempAtts = opinion.getOpinionAttachments();
                }
                if (Strings.isNotEmpty(tempAtts)) {
                    List<Attachment> oAtts = opinionAttMap.get(fieldName);
                    if (oAtts == null) {
                        oAtts = new ArrayList<Attachment>();
                    }
                    oAtts.addAll(tempAtts);
                    opinionAttMap.put(fieldName, oAtts);
                }
            }
        }

        return opinionAttMap;
    }

    /**
     * 获取文单的配置信息
     * 
     * @Author : xuqw
     * @Date : 2015年6月14日下午3:27:12
     * @param summary
     * @return
     */
    private static FormOpinionConfig getDisplayConfig(EdocSummary summary) {

        EdocFormManager edocFormManager = (EdocFormManager) AppContext.getBean("edocFormManager");
        TemplateManager templeteManager = (TemplateManager) AppContext.getBean("templeteManager");
        
        long flowPermAccout = EdocHelper.getFlowPermAccountId(summary.getOrgAccountId(), summary, templeteManager);
        FormOpinionConfig displayConfig = edocFormManager.getEdocOpinionDisplayConfig(summary.getFormId(),
                flowPermAccout);

        return displayConfig;
    }

    /**
     * 解析公文单
     * 
     * @Author : xuqw
     * @Date : 2015年6月14日下午11:47:01
     * @param summary
     * @param tempFile
     * @return
     * @throws BusinessException
     */
    public static FormParseExtInfo formatFormContent(EdocSummary summary, String tempFile) throws BusinessException {
        return formatFormContent(summary, -1L, true, tempFile,null,false);
    }
    
    
    /**
     * 解析公文单
     * 
     * @Author : xuqw
     * @Date : 2015年6月14日下午11:47:01
     * @param summary
     * @param tempFile
     * @param actorId 权限id
     * @return
     * @throws BusinessException
     */
    public static FormParseExtInfo formatFormContentForH5(EdocSummary summary, String tempFile,Long actorId,CtpAffair affair) throws BusinessException {
        return formatFormContent(summary, actorId, false, SystemEnvironment.getContextPath(),affair,true);
    }
    
    /**
     * 解析公文单
     * 
     * @param summary
     * @param actorId 节点权限ID
     * @param toHTML 是否生成HTML结构， true - 生成HTML结构， false只解析表单正文
     * @param tempFile 导出文件路径， 如果toHTML为false， 这个参数才生效
     * @return
     * @throws BusinessException
     *
     * @Author      : xuqw
     * @Date        : 2016年7月27日下午3:59:26
     *
     */
    private static FormParseExtInfo formatFormContent(EdocSummary summary, Long actorId, boolean toHTML, String tempFile,CtpAffair affair,boolean canSeeMyselfOpinion) throws BusinessException {

        if(tempFile == null){
            tempFile = "";
        }
        
        EdocManager edocManager = (EdocManager) AppContext.getBean("edocManager");
        V3xHtmDocumentSignatManager htmSignetManager = (V3xHtmDocumentSignatManager) AppContext.getBean("htmSignetManager");
        EdocFormManager edocFormManager = (EdocFormManager) AppContext.getBean("edocFormManager");
        AttachmentManager attachmentManager = (AttachmentManager) AppContext.getBean("attachmentManager");
        XMLConverter xmlConverter = (XMLConverter) AppContext.getBean("xmlConverter");
        
        FormParseExtInfo extInfo = new FormParseExtInfo();

        // 手写签批信息获取
        List<V3xHtmDocumentSignature> handWrites = htmSignetManager.findBySummaryIdAndType(summary.getId(),
                V3xHtmSignatureEnum.HTML_SIGNATURE_DOCUMENT.getKey());

        // 意见信息获取
        FormOpinionConfig displayConfig = getDisplayConfig(summary);
        Map<String, EdocOpinionModel> field2OptionMap = edocManager.getEdocOpinion(summary, displayConfig);
        Map<String, Object> optionMap = EdocOpinionDisplayUtil._convertOpinionToString(field2OptionMap, displayConfig,
                affair, false, handWrites,canSeeMyselfOpinion, true);

        // 意见附件
        Map<String, List<Attachment>> opinionAttMap = getOpinionAttMap(field2OptionMap, !toHTML);

        // 附件信息获取
        Long summaryId = summary.getId();
        List<Attachment> attachs = attachmentManager.getByReference(summaryId, summaryId);
        extInfo.addAllAttFIle(attachs);

        // 文单信息
        Long formId = summary.getFormId();
        EdocForm form = edocFormManager.getEdocForm(summary.getFormId());
        String content = edocFormManager.getFormContentWithFix(form, formId, summary);

        // 填充值
        Map<String, String[]> field2ValueMap = xmlConverter.field2ValMap(formId, summary,actorId);
        extInfo.setField2ValueMap(field2ValueMap);

        // 最终写入文件的字符串
        StringBuilder htmlForm = new StringBuilder();

        // 去掉空白
        String formContent = content.replaceAll(">[\\s]+", ">").replaceAll("[\\s]+<", "<").replaceAll("> <", "><")
                .replaceAll("&#27;", "&nbsp;");
        // .replaceAll("(?s)>[ ]*[ ]+[ ]+[ ]*<", "><")
        // .replaceAll(">[ ]<", "><");//160的空白

        // 移除文单自带的样式
        formContent = EdocUtil.removeFormContentStyle(formContent);

        if(toHTML){
            htmlForm.append(htmlBegin);

            htmlForm.append(headBegin);

            htmlForm.append("<title>" + Strings.toHTML(summary.getSubject()) + "</title>");
            htmlForm.append("<meta charset=\"UTF-8\"></meta>");
            htmlForm.append("<meta content=\"text/html\" http-equiv=\"Content-Type\"></meta>");
            htmlForm.append("<STYLE media=print> .pageNext { PAGE-BREAK-AFTER: always }  .Noprint { DISPLAY: none }  .tdfirstclass { BORDER-BOTTOM: #999999 1px solid; BORDER-RIGHT: #999999 1px solid } </STYLE>  <STYLE> .body-class { BORDER-BOTTOM: #ededed 0px solid; BORDER-LEFT: #ededed 10px solid; MARGIN: 0px; BACKGROUND: #fff; BORDER-TOP: #ededed 10px solid; BORDER-RIGHT: #ededed 10px solid }  .body-class-print { BORDER-BOTTOM: #ededed 0px solid; BORDER-LEFT: #ededed 0px solid; MARGIN: 0px; BACKGROUND: #fff; BORDER-TOP: #ededed 0px solid; BORDER-RIGHT: #ededed 0px solid }  .header { BACKGROUND: #ededed }  #context { MARGIN: 0px auto; BACKGROUND: #ffffff; OVERFLOW: hidden }  .contentText { PADDING-BOTTOM: 0px; PADDING-LEFT: 0px; PADDING-RIGHT: 0px; PADDING-TOP: 0px }  @media Print { #header { DISPLAY: none } .body { BORDER-BOTTOM: 0px; BORDER-LEFT: 0px; MARGIN: 0px; BORDER-TOP: 0px; BORDER-RIGHT: 0px } }  #checkOption LABEL { FONT-WEIGHT: normal }  .buttonSmall { WIDTH: 47px; importent: } </STYLE>");

            // 样式处理
            htmlForm.append("<LINK rel=\"stylesheet\" type=\"text/css\"  href=\"" + tempFile + "/common/css/default.css\" media=all>");
            htmlForm.append("<LINK rel=\"stylesheet\" type=\"text/css\"  href=\"" + tempFile + "/common/skin/default/skin.css\">");
            htmlForm.append("<LINK rel=\"stylesheet\" type=\"text/css\"  href=\"" + tempFile + "/apps_res/form/css/SeeyonForm.css\">");
            htmlForm.append("<LINK rel=\"stylesheet\" type=\"text/css\"  href=\"" + tempFile + "/apps_res/edoc/css/edoc.css\">");
            htmlForm.append("<LINK rel=\"stylesheet\" type=\"text/css\"  href=\"" + tempFile + "/common/all-min.css\">");
            htmlForm.append("<LINK rel=\"stylesheet\" type=\"text/css\"  href=\"" + tempFile + "/common/RTE/editor/css/fck_editorarea4Show.css\">");
            htmlForm.append("<LINK rel=\"stylesheet\" type=\"text/css\"  href=\"" + tempFile + "/apps_res/edoc/css/edocDisplay.css\">");
            htmlForm.append("<LINK rel=\"stylesheet\" type=\"text/css\"  href=\"" + tempFile + "/apps_res/edoc/css/edocPrintDisplay.css\">");

            // 调用接口进行文件物理拷贝
            extInfo.addFiles("common/css/default.css");
            extInfo.addFiles("common/skin/default/skin.css");
            extInfo.addFiles("apps_res/form/css/SeeyonForm.css");
            extInfo.addFiles("apps_res/edoc/css/edoc.css");
            extInfo.addFiles("common/all-min.css");
            extInfo.addFiles("common/RTE/editor/css/fck_editorarea4Show.css");
            extInfo.addFiles("apps_res/edoc/css/edocDisplay.css");
            extInfo.addFiles("apps_res/edoc/css/edocPrintDisplay.css");

            htmlForm.append("<script type=\"text/javascript\">");
            htmlForm.append("function _init_() { _fixFieldHeightAndWidth(); } ");
            htmlForm.append("function _fixFieldHeightAndWidth() { var fieldNodes = _getFieldNodes(\"span\"); fieldNodes = fieldNodes.concat(_getFieldNodes(\"div\")); if (fieldNodes.length > 0) { for (var i = 0; i < fieldNodes.length; i++) { var fieldNode = fieldNodes[i]; /*var filedHeight = fieldNode.style.height; if (filedHeight) { filedHeight = parseInt(filedHeight, 10); } else { filedHeight = 0; }*/ var filedScoreHeight = Math.max(fieldNode.scrollHeight, fieldNode.offsetHeight, fieldNode.clientHeight); var fieldWidth = _getContentAreaWidth(fieldNode); var fieldW = fieldWidth - _getAttWidth(fieldNode); if (\"logoimg\" == fieldNode.getAttribute(\"id\")) { var imgObj = document.getElementById(\"logoimg_img\"); if (imgObj) { imgObj.style.width = fieldW + \"px\"; imgObj.style.height = filedScoreHeight + \"px\"; fieldNode.style.display = \"none\"; imgObj.style.display = \"\"; } } else { if (fieldW > 0) { fieldNode.style.width = fieldW + \"px\"; } /*if (filedScoreHeight > filedHeight) { fieldNode.style.height = filedScoreHeight + \"px\"; }*/ } } } }");
            htmlForm.append("function _getRuntimeStyle(obj, k) { var v = null; if (obj.currentStyle){ v = obj.currentStyle[k]; } else{ v = window.getComputedStyle(obj, null).getPropertyValue(k); } return v; } ");
            htmlForm.append("function _getContentAreaWidth(domObj){  var spanClientWidth = domObj.clientWidth;  var paddingWidth = 0; var paddingLeft = _getRuntimeStyle(domObj, \"paddingLeft\") || _getRuntimeStyle(domObj, \"padding-left\"); if (paddingLeft) { if (paddingLeft.indexOf(\"px\") != -1) { paddingWidth += parseInt(paddingLeft.replace(\"px\", \"\")); } else if (paddingLeft.indexOf(\"pt\") != -1) { paddingWidth += parseInt(paddingLeft.replace(\"pt\", \"\")) * (4 / 3); } } var paddingRight = _getRuntimeStyle(domObj, \"paddingRight\") || _getRuntimeStyle(domObj, \"padding-right\"); if (paddingRight) { if (paddingRight.indexOf(\"px\") != -1) { paddingWidth += parseInt(paddingRight.replace(\"px\", \"\")); } else if (paddingRight.indexOf(\"pt\") != -1) { paddingWidth += parseInt(paddingRight.replace(\"pt\", \"\")) * (4 / 3); } }  return (spanClientWidth - paddingWidth); }");
            htmlForm.append("function _getAttWidth(eNode, type) {  var totalValue = 0; if(type){ var tValue = _getRuntimeStyle(eNode, type); totalValue = parseInt(tValue, 10); if(!totalValue){ totalValue = 0; } }else{ var fBorderL = _getRuntimeStyle(eNode, \"borderLeftWidth\") || _getRuntimeStyle(eNode, \"border-left-width\"); var fBorderR = _getRuntimeStyle(eNode, \"borderRightWidth\") || _getRuntimeStyle(eNode, \"border-right-width\"); var fPaddingL = _getRuntimeStyle(eNode, \"paddingLeft\") || _getRuntimeStyle(eNode, \"padding-left\"); var fPaddingR = _getRuntimeStyle(eNode, \"paddingRight\") || _getRuntimeStyle(eNode, \"padding-right\"); var fMarginL = _getRuntimeStyle(eNode, \"marginLeft\") || _getRuntimeStyle(eNode, \"margin-left\"); var fMarginR = _getRuntimeStyle(eNode, \"marginRight\") || _getRuntimeStyle(eNode, \"margin-right\");  var bLValue = parseInt(fBorderL, 10); var bRValue = parseInt(fBorderR, 10); var pLValue = parseInt(fPaddingL, 10); var pRValue = parseInt(fPaddingR, 10); var mLValue = parseInt(fMarginL, 10); var mRValue = parseInt(fMarginR, 10);   if (bLValue) { totalValue += bLValue; } if (bRValue) { totalValue += bRValue; } if (pLValue) { totalValue += pLValue; } if (pRValue) { totalValue += pRValue; } if (mLValue) { totalValue += mLValue; } if (mRValue) { totalValue += mRValue; } }  return totalValue; }  ");
            htmlForm.append("function _getFieldNodes(tageName) { var ret = []; var fieldNodes = document.getElementsByTagName(tageName); if (fieldNodes && fieldNodes.length > 0) { for (var i = 0; i < fieldNodes.length; i++) { var fieldNode = fieldNodes[i]; var _nodeType = fieldNode.getAttribute(\"_nodeType\"); if (\"_formFieldNode_\" == _nodeType) { ret.push(fieldNode); } } } return ret; }");
            htmlForm.append("</script>");

            htmlForm.append(headEnd);
            htmlForm.append(bodyBegin);
        }

        // 截取公文单内容
        Matcher matcher = templatePatt.matcher(formContent);
        String newFormContent = formContent;
        if (matcher.find()) {
            newFormContent = matcher.group(1);
        }

        // 提取文单里面的Image
        Matcher m = nomalImgPatt.matcher(newFormContent);
        while (m.find()) {
            String imgStr = m.group();
            String srcId = m.group(1);
            if (Strings.isNotBlank(srcId)) {
                long fileId = Long.parseLong(srcId);

                String newImg = "";
                if(toHTML){
                    V3XFile file = getCtpFile(fileId);
                    if (file != null) {
                        extInfo.addCtpFile(file);
                        String fileName = file.getId() + "";
                        newImg = replaceSrc(imgStr, tempFile + "/" + fileName);
                    }
                }else{
                    String imgSrc = getNodeAttr(imgStr, "src");
                    if(Strings.isNotBlank(imgStr)){
                        newImg = replaceSrc(imgStr, tempFile + imgSrc);
                    }
                }
                newFormContent = newFormContent.replace(imgStr, newImg);
            }
        }

        // 匹配字段
        Matcher matcherSpan = fieldSpanPatt.matcher(newFormContent);
        String finalFormContent = newFormContent;
        while (matcherSpan.find()) {

            String spanNode = matcherSpan.group(0);// 字段SPAN
            String spanInfo = matcherSpan.group(1);// SPAN的全部属性
            String field = matcherSpan.group(2);// fieldName
            String[] values = field2ValueMap.get(field);// 解析出来的字段值

            String styleAttr = getNodeAttr(spanInfo, "style");// style信息
            String classAttr = getNodeAttr(spanInfo, "class");// class信息
            String fieldHeight = getStyleValue(styleAttr, "height");
            String fieldWidth = getStyleValue(styleAttr, "width");
            String fontSize = getStyleValue(styleAttr, "font-size");
            String fontfamily = getStyleValue(styleAttr, "font-family");

            String minHeight = "";
            String tagName = null;
            String fieldContent = "";
            String afterContent = "";
            
            if (Strings.isNotEmpty(fieldHeight)) {
                minHeight = "min-height:" + fieldHeight + ";";
            }
            String commonCss = ";overflow-x:hidden;overflow-y:auto;white-space:normal;word-break: break-word;word-wrap: break-word;border:0px;height:auto;"
                    + minHeight;

            
            if (values != null) {

                String value = values[1];
                String type = values[0];

                if (Strings.isBlank(value)) {
                    value = "";
                }
                
              //附件好坑
                if("attachments".equals(field)){
                    value = value.replace("&#x0A;", "\r\n"); 
                }

                if ("text".equalsIgnoreCase(type) || "select".equalsIgnoreCase(type)) {
                    // finalFormContent = finalFormContent.replace(spanNode,
                    // "<input id=\""+field+"\" style=\"" + styleAttr +
                    // ";\" class=\"" + classAttr +
                    // "\" readonly=\"readonly\" value=\""+Strings.toXmlStr(value)+"\"/>");//++"</div>");//
                    // finalFormContent = finalFormContent.replace(spanNode,
                    // "<textarea readonly=\"readonly\" id=\""+field+"\" rows=\"1\" style=\""
                    // + styleAttr +
                    // ";overflow-x:hidden;overflow-y:auto;white-space:normal;word-break: break-all;word-wrap: break-word;resize: none;\" class=\""
                    // + classAttr + "\">"+Strings.toHTML(value)+"</textarea>");
                    
                    tagName = "span";
                    
                } else if ("textarea".equalsIgnoreCase(type)) {
                    tagName = "div";
                }
                
                fieldContent = Strings.toHTML(value, false);
                
            } else {

                if ("logoimg".equals(field)) {// Logo

                    // 拷贝文件
                    String srcValut = EdocHelper.getLoginURLSrc(summary.getOrgAccountId());

                    if(toHTML){
                        if (srcValut.indexOf("fileUpload.do") != -1) {// 重新上传的Logo

                            Matcher logoM = ctpFileIdPatt.matcher(srcValut);
                            if (logoM.find()) {

                                String srcId = logoM.group(1);
                                long fileId = Long.parseLong(srcId);

                                V3XFile file = getCtpFile(fileId);
                                if (file != null) {
                                    extInfo.addCtpFile(file);
                                    String fileName = file.getId() + "";
                                    srcValut = fileName;
                                }
                            }

                        } else {// 默认的logo
                            extInfo.addFiles(srcValut);// 文件拷贝
                        }
                    }
                    
                    // String width = getStyleValue(styleAttr, "width");
                    // String height = getStyleValue(styleAttr, "height");
                    
                    if (!srcValut.startsWith("/")) {
                        srcValut = "/" + srcValut;
                    }
                    srcValut = tempFile + srcValut;
                    
                    String logoImg = "<img id=\"logoimg_img\" style=\"width:" + fieldWidth + ";height:" + fieldHeight + ";\" src=\""+srcValut+"\"/>";

                    tagName = "span";
                    fieldContent = "&nbsp;";
                    afterContent = logoImg;
                    //finalFormContent = finalFormContent.replace(spanNode, logoImg);
                    
                    // finalFormContent = finalFormContent.replace(spanNode,
                    // "<span _nodeType=\"_formFieldNode_\" id=\""+field+"\" style=\""
                    // + styleAttr +
                    // ";white-space:normal;word-break: break-all;word-wrap: break-word;border:0px;\" class=\""
                    // + classAttr + "\"></span>" + logoURL);
                } else {
                    String optionValue = "";

                    if ("senderOpinion".equals(field)) {
                        // 这条分支不会走， 没有这个元素
                        // Map<Long, StringBuilder> senderAttMap = (Map<Long,
                        // StringBuilder>)
                        // optionMap.get("senderOpinionAttStr");;
                        // List<EdocOpinion> senderOpinions =
                        // (List<EdocOpinion>)
                        // optionMap.get("senderOpinionList");;

                    } else {
                        String options = (String) optionMap.get(field);
                        if (Strings.isNotBlank(options)) {

                            optionValue = options;
                            //和前端保持一致， 修改字体
                            optionValue = changeFontsize(optionValue, new String[][]{{"font-Size", fontSize}, {"font-family", fontfamily}});
                            
                            
                            //附件信息
                            List<Attachment> oAtts = opinionAttMap.get(field);
                            if (Strings.isNotEmpty(oAtts)) {
                                extInfo.addAllAttFIle(oAtts);
                            }
                            
                         // 去掉onlick
                            optionValue = optionValue.replaceAll("(?i)onclick='javascript:showV3XMemberCard(.*?)'", "");
                         
                            // 去掉附件里面的高度设置
                            optionValue = optionValue.replaceAll("(?i)(<div id='attachmentDiv_[^>]*?)height:[^>]*?;([^>]*?>)", "$1height:auto;$2");

                            
                            if(toHTML){
                                // 去掉关联文档
                                optionValue = optionValue.replaceAll("(?i)(?i)<div id='attachmentDiv_[^<]*?><img[^<]*?><a class=\"like-a\".*?<\\/div>", "");
                                
                             // 附件文件
                                Matcher attM = attFilePatt.matcher(optionValue);
                                while (attM.find()) {

                                    String attStr = attM.group(0);
                                    String aBegin = attM.group(1);
                                    String aEnd = attM.group(2);
                                    optionValue = optionValue.replace(attStr, aBegin + " " + aEnd);
                                }
                                
                            }else{
                                //去掉关联文档的点击事件
                                optionValue = optionValue.replaceAll("(?i)onclick=\"openDetailURL(.*?)\"", "");
                                Pattern attPatt = Pattern.compile("<div id='attachmentDiv_([^<]*?)'");
                                Matcher attM = attPatt.matcher(optionValue);
                                while (attM.find()) {
                                    
                                    String attImg = attM.group(0);
                                    String fileUrl = attM.group(1);
                                    if(Strings.isNotEmpty(oAtts)){
                                        for(Attachment a : oAtts){
                                            if(fileUrl.equals(String.valueOf(a.getFileUrl()))){
                                                StringBuilder newAtt = new StringBuilder("<div class='allow-click-attachment' see-att-data='")
                                                        .append(JSONUtil.toJSONString(a).replace("'", "&#39;"))
                                                        .append("'")
                                                        .append(" id='attachmentDiv_")
                                                        .append(fileUrl).append("'");
                                                
                                                optionValue = optionValue.replace(attImg, newAtt.toString());
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            // 附件图标
                            Matcher attImgM = attImgPatt.matcher(optionValue);
                            while (attImgM.find()) {
                                
                                String attImg = attImgM.group(0);
                                String imgBegin = attImgM.group(1);
                                String attUrl = attImgM.group(2);
                                String imgEnd = attImgM.group(3);
                                if(attUrl.startsWith(SystemEnvironment.getContextPath())){
                                    attUrl = attUrl.substring(SystemEnvironment.getContextPath().length());
                                }
                                if (!attUrl.startsWith("/")) {
                                    attUrl = "/" + attUrl;
                                }
                                extInfo.addFiles(attUrl.substring(1, attUrl.length()));// 文件拷贝
                                optionValue = optionValue.replace(attImg, imgBegin + tempFile + attUrl + imgEnd);
                            }
                            
                            
                            if(toHTML){
                                
                                // 图片签名
                                Matcher signm = picSignPatt.matcher(optionValue);
                                while (signm.find()) {
                                    String imgStr = signm.group();
                                    String srcId = signm.group(1);
                                    if (Strings.isNotBlank(srcId)) {
                                        
                                        long id = Long.parseLong(srcId);

                                        V3xHtmDocumentSignature signet = htmSignetManager.getById(id);
                                        if (signet != null) {
                                            String body = signet.getFieldValue();
                                            byte[] b = EdocHelper.hex2byte(body);
                                            String fileName = "pic_sign_" + id;

                                            extInfo.addByteFile(fileName, b);

                                            String newImg = replaceSrc(imgStr, tempFile + "/" + fileName);
                                            optionValue = optionValue.replace(imgStr, newImg);
                                        }
                                    }
                                }
                            }
                            
                         // 手写签批处理
                            if (Strings.isNotEmpty(handWrites)) {

                                for (V3xHtmDocumentSignature handWrite : handWrites) {
                                    String filedName = handWrite.getFieldName();
                                    Long affairId = handWrite.getAffairId();
                                    String opinionLocation = "<div id='" + affairId + "'";// 签批定位
                                    if (("hw" + field).equals(filedName) && optionValue.indexOf(opinionLocation) != -1) {

                                        if(toHTML){
                                            
                                            // 写图片
                                            String srcData = handWrite.getFieldValue();
                                            String fileName = "hand_sign_" + affairId;
                                            fileName = fileName.replace("-", "0");
                                            byte[] signPicByte = getPic(srcData);
                                            if (signPicByte != null) {
                                                extInfo.addByteFile(fileName, signPicByte);
                                                String imgStr = "<img alt=\"签名\" src=\"" + tempFile + "/" + fileName + "\">";
                                                optionValue = optionValue.replace(opinionLocation, imgStr + opinionLocation);
                                            }
                                        }else {
                                            String imgSrc = tempFile + "/signatPicController.do?method=writeGIF&RECORDID="+handWrite.getSummaryId()+"&FIELDNAME="+handWrite.getFieldName()
                                                         +"&isNewImg=true&affairId="+handWrite.getAffairId();
                                            String imgStr = "<img alt=\"签名\" src=\"" + imgSrc + "\" id=\"" + affairId + "\">" ;
                                           // optionValue = optionValue.replace(opinionLocation, imgStr + opinionLocation);
                                            //OA-109639M3端查看已经被文单签批过的公文时，文档上所有意见都显示了该签批,affairId相同时和PC一样只显示一个签批数据
                                            optionValue = optionValue.replaceFirst(opinionLocation, imgStr + opinionLocation);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    
                    tagName = "div";
                    fieldContent = optionValue;
                    
                }
            }
            
            if(Strings.isNotBlank(tagName)){
                
                StringBuilder newSpanNode = new StringBuilder();
                newSpanNode.append("<").append(tagName).append(" _nodeType=\"_formFieldNode_\" ")
                           .append(" id=\"");
                
                if(!toHTML){
                    newSpanNode.append(FIELD_PREFIX);
                }
                
                newSpanNode.append(field).append("\" ")
                           .append(" style=\"").append(styleAttr).append(commonCss).append("\" ")
                           .append(" class=\"").append(classAttr).append("\" ");
                
                
                if(values != null && !toHTML){
                    
                    String access = values[2];
                    String required = values[3];
                    
                    newSpanNode.append(" access=\"").append(access).append("\" ")
                               .append(" required=\"").append(required).append("\" ");
                }
                
                newSpanNode.append(">");
                
                newSpanNode.append(fieldContent);
                newSpanNode.append("</").append(tagName).append(">").append(afterContent);
                
                
                finalFormContent = finalFormContent.replace(spanNode, newSpanNode.toString());
            }
        }

        htmlForm.append(finalFormContent);

        if(toHTML){
            // 样式补充
            // htmlForm.append("<style type=\"text/css\"> .browse_class span { color: blue; }  .xdTableHeader TD { min-height: 10px; }  .radio_com { margin-right: 0px; }  .xdTextBox { BORDER-BOTTOM: #dcdcdc 1pt solid; min-height: 12px; TEXT-ALIGN: left; BORDER-LEFT: #dcdcdc 1pt solid; BACKGROUND-COLOR: window; DISPLAY: inline-block; WHITE-SPACE: nowrap; COLOR: windowtext; OVERFLOW: hidden; BORDER-TOP: #dcdcdc 1pt solid; BORDER-RIGHT: #dcdcdc 1pt solid; }  .xdRichTextBox { font-size: 12px; BORDER-BOTTOM: #dcdcdc 1pt solid; TEXT-ALIGN: left; BORDER-LEFT: #dcdcdc 1pt solid; BACKGROUND-COLOR: window; FONT-STYLE: normal; min-height: 12px; display: inline-block; WORD-WRAP: break-word; COLOR: windowtext; BORDER-TOP: #dcdcdc 1pt solid; BORDER-RIGHT: #dcdcdc 1pt solid; TEXT-DECORATION: none; }  span.xdRichTextBox { VERTICAL-ALIGN: bottom !important; }  span.design_class { vertical-align: bottom; }  span.edit_class { vertical-align: bottom; }  .mainbodyDiv div,.mainbodyDiv input,.mainbodyDiv textarea,.mainbodyDiv p,.mainbodyDiv th,.mainbodyDiv td,.mainbodyDiv ul,.mainbodyDiv li { font-family: inherit; layout-grid-mode: none; }  span.biggerThanMax { background-color: yellow; }  .insert_pic_16 { margin-top: 2px; } span.browse_class>span { BACKGROUND-COLOR: #F8F8F8; min-height: 14px; overflow-y: hidden; white-space: pre-wrap; }  span.browse_class>label { BACKGROUND-COLOR: #F8F8F8; }  span.browse_class>input { BACKGROUND-COLOR: #F8F8F8; }  span.browse_class>div.left { BACKGROUND-COLOR: #F8F8F8; }  span.browse_class>div.right { BACKGROUND-COLOR: #F8F8F8; }  td{ text-align: left; } </style>");
            htmlForm.append("<STYLE>.body-detail-HTML { WIDTH: 800px } .body-detail { WIDTH: 800px } .contentText P { LINE-HEIGHT: 1.5; FONT-FAMILY: \"Microsoft YaHei\",SimSun,Arial,Helvetica,sans-serif; FONT-SIZE: 16px; WORD-BREAK: break-all } TD { LINE-HEIGHT: 1.5; FONT-FAMILY: \"Microsoft YaHei\",SimSun,Arial,Helvetica,sans-serif; FONT-SIZE: 16px; WORD-BREAK: break-all;text-align:left } UL { LINE-HEIGHT: 1.5; FONT-FAMILY: \"Microsoft YaHei\",SimSun,Arial,Helvetica,sans-serif; FONT-SIZE: 16px; WORD-BREAK: break-all } LI { LINE-HEIGHT: 1.5; FONT-FAMILY: \"Microsoft YaHei\",SimSun,Arial,Helvetica,sans-serif; FONT-SIZE: 16px; WORD-BREAK: break-all } DIV { LINE-HEIGHT: 1.5; FONT-FAMILY: \"Microsoft YaHei\",SimSun,Arial,Helvetica,sans-serif; FONT-SIZE: 16px; WORD-BREAK: break-all } A { LINE-HEIGHT: 1.5; FONT-FAMILY: \"Microsoft YaHei\",SimSun,Arial,Helvetica,sans-serif; FONT-SIZE: 16px; WORD-BREAK: break-all } OL { LINE-HEIGHT: 1.5; FONT-FAMILY: \"Microsoft YaHei\",SimSun,Arial,Helvetica,sans-serif; FONT-SIZE: 16px; WORD-BREAK: break-all } PRE { LINE-HEIGHT: 1.5; FONT-FAMILY: \"Microsoft YaHei\",SimSun,Arial,Helvetica,sans-serif; FONT-SIZE: 16px; WORD-BREAK: break-all } </STYLE>");
            
            htmlForm.append(bodyEnd);
            
            htmlForm.append(htmlEnd);
        }

        extInfo.setContent(htmlForm.toString());
        
        extInfo.setFormOpinionConfig(displayConfig);

        return extInfo;
    }
    
    
    /**
     * 解析公文单(新公文)
     * 
     * @Author : caihl
     * @Date : 2018年7月16日
     * @param summary
     * @param tempFile
     * @return
     * @throws BusinessException
     */
    public static FormParseExtInfo formatFormContent2(EdocSummary summary, String tempFile,String fFolder) throws BusinessException {
        return formatFormContent2(summary, -1L, true, tempFile,fFolder,null,false);
    }
    
    /**
     * 解析公文单(新公文)
     * 
     * @Author : caihl
     * @Date 2018年7月16日
     * @param summary
     * @param actorId 节点权限ID
     * @param toHTML 是否生成HTML结构， true - 生成HTML结构， false只解析表单正文
     * @param tempFile 导出文件路径， 如果toHTML为false， 这个参数才生效
     * @return
     * @throws BusinessException
     */
    private static FormParseExtInfo formatFormContent2(EdocSummary summary, Long actorId, boolean toHTML, String tempFile,String fFolder,CtpAffair affair,boolean canSeeMyselfOpinion) throws BusinessException {
        V3xHtmDocumentSignatManager htmSignetManager = (V3xHtmDocumentSignatManager) AppContext.getBean("htmSignetManager");
        AttachmentManager attachmentManager = (AttachmentManager) AppContext.getBean("attachmentManager");
        FormOptionExtendManager govdocFormExtendManager = (FormOptionExtendManager) AppContext.getBean("formOptionExtendManager");
        FormOptionSortManager govdocFormOpinionSortManager = (FormOptionSortManager) AppContext.getBean("formOptionSortManager");
        MainbodyManager ctpMainbodyManager = (MainbodyManager) AppContext.getBean("ctpMainbodyManager");
        
        FormParseExtInfo extInfo = new FormParseExtInfo();
        Long formAppId = summary.getFormAppid();
        Long formId = summary.getFormAppid();
        // 手写签批信息获取
        List<V3xHtmDocumentSignature> handWrites = htmSignetManager.findBySummaryIdAndType(summary.getId(),
                V3xHtmSignatureEnum.HTML_SIGNATURE_DOCUMENT.getKey());

        // 意见信息获取
        FormOptionExtend govdocFormExtend = govdocFormExtendManager.findByFormId(formAppId);
        FormOpinionConfig displayConfig = JSONUtil.parseJSONString(govdocFormExtend.getOptionFormatSet(), FormOpinionConfig.class);
        Map<String, EdocOpinionModel> field2OptionMap = govdocFormOpinionSortManager.getGovdocOpinion(formId, summary, displayConfig);
        Map<String, Object> optionMap = EdocOpinionDisplayUtil.convertOpinionToString(field2OptionMap, displayConfig,
                null, false, handWrites);

        // 意见附件
        Map<String, List<Attachment>> opinionAttMap = getOpinionAttMap(field2OptionMap, !toHTML);

        // 附件信息获取
        Long summaryId = summary.getId();
        List<Attachment> attachs = attachmentManager.getByReference(summaryId, summaryId);
        extInfo.addAllAttFIle(attachs);

        // 文单信息
        List<CtpContentAllBean> contentList = ctpMainbodyManager.transContentViewResponse(ModuleType.edoc, summary.getId(), 2, "", 0, -1L);
        String content = "";
        for(CtpContentAllBean content1 : contentList) {
        	content = content1.getContentHtml();
        	if(Strings.isNotBlank(content)) {
        		break;
        	}
        }

        // 最终写入文件的字符串
        StringBuilder htmlForm = new StringBuilder();

        // 去掉空白
        String formContent = content.replaceAll(">[\\s]+", ">").replaceAll("[\\s]+<", "<").replaceAll("> <", "><")
                .replaceAll("&#27;", "&nbsp;");
        
        //分离文单自带样式
        String[] conAndSty = EdocUtil.divFormContentStyle(formContent);
        formContent = conAndSty[0];
        try {
        	String inFolder = fFolder + "/innerForm/css";
        	createFolder(inFolder);
        	File styFile = new File(inFolder + "/innerForm.css");
			FileUtils.writeStringToFile(styFile, conAndSty[1], "utf-8");
		} catch (IOException e) {
			LOGGER.error("取得表单内部样式出错！" + e);
		}

        htmlForm.append(htmlBegin);

        htmlForm.append(headBegin);

        htmlForm.append("<title>" + Strings.toHTML(summary.getSubject()) + "</title>");
        htmlForm.append("<meta charset=\"UTF-8\"></meta>");
        htmlForm.append("<meta content=\"text/html\" http-equiv=\"Content-Type\"></meta>");
        htmlForm.append("<STYLE media=print> .pageNext { PAGE-BREAK-AFTER: always }  .Noprint { DISPLAY: none }  .tdfirstclass { BORDER-BOTTOM: #999999 1px solid; BORDER-RIGHT: #999999 1px solid } </STYLE>  <STYLE> .body-class { BORDER-BOTTOM: #ededed 0px solid; BORDER-LEFT: #ededed 10px solid; MARGIN: 0px; BACKGROUND: #fff; BORDER-TOP: #ededed 10px solid; BORDER-RIGHT: #ededed 10px solid }  .body-class-print { BORDER-BOTTOM: #ededed 0px solid; BORDER-LEFT: #ededed 0px solid; MARGIN: 0px; BACKGROUND: #fff; BORDER-TOP: #ededed 0px solid; BORDER-RIGHT: #ededed 0px solid }  .header { BACKGROUND: #ededed }  #context { MARGIN: 0px auto; BACKGROUND: #ffffff; OVERFLOW: hidden }  .contentText { PADDING-BOTTOM: 0px; PADDING-LEFT: 0px; PADDING-RIGHT: 0px; PADDING-TOP: 0px }  @media Print { #header { DISPLAY: none } .body { BORDER-BOTTOM: 0px; BORDER-LEFT: 0px; MARGIN: 0px; BORDER-TOP: 0px; BORDER-RIGHT: 0px } }  #checkOption LABEL { FONT-WEIGHT: normal }  .buttonSmall { WIDTH: 47px; importent: } </STYLE>");

        // 样式处理
        htmlForm.append("<LINK rel=\"stylesheet\" type=\"text/css\"  href=\"" + tempFile + "/common/css/default.css\" media=all>");
        htmlForm.append("<LINK rel=\"stylesheet\" type=\"text/css\"  href=\"" + tempFile + "/common/skin/default/skin.css\">");
        htmlForm.append("<LINK rel=\"stylesheet\" type=\"text/css\"  href=\"" + tempFile + "/apps_res/form/css/SeeyonForm.css\">");
        htmlForm.append("<LINK rel=\"stylesheet\" type=\"text/css\"  href=\"" + tempFile + "/apps_res/edoc/css/edoc.css\">");
        htmlForm.append("<LINK rel=\"stylesheet\" type=\"text/css\"  href=\"" + tempFile + "/common/all-min.css\">");
        htmlForm.append("<LINK rel=\"stylesheet\" type=\"text/css\"  href=\"" + tempFile + "/common/RTE/editor/css/fck_editorarea4Show.css\">");
        htmlForm.append("<LINK rel=\"stylesheet\" type=\"text/css\"  href=\"" + tempFile + "/common/css/default.css\">");
        htmlForm.append("<LINK rel=\"stylesheet\" type=\"text/css\"  href=\"" + tempFile + "/apps_res/edoc/css/edocDisplay.css\">");
        htmlForm.append("<LINK rel=\"stylesheet\" type=\"text/css\"  href=\"" + tempFile + "/apps_res/edoc/css/edocPrintDisplay.css\">");
        htmlForm.append("<LINK rel=\"stylesheet\" type=\"text/css\"  href=\"" + tempFile + "/innerForm/css/innerForm.css\">");

        // 调用接口进行文件物理拷贝
        extInfo.addFiles("common/css/default.css");
        extInfo.addFiles("common/skin/default/skin.css");
        extInfo.addFiles("apps_res/form/css/SeeyonForm.css");
        extInfo.addFiles("apps_res/edoc/css/edoc.css");
        extInfo.addFiles("common/all-min.css");
        extInfo.addFiles("common/RTE/editor/css/fck_editorarea4Show.css");
        extInfo.addFiles("common/css/default.css");
        extInfo.addFiles("apps_res/edoc/css/edocDisplay.css");
        extInfo.addFiles("apps_res/edoc/css/edocPrintDisplay.css");

        htmlForm.append("<script type=\"text/javascript\">");
        htmlForm.append("function _init_() { _fixFieldHeightAndWidth(); } ");
        htmlForm.append("function _fixFieldHeightAndWidth() { var fieldNodes = _getFieldNodes(\"span\"); fieldNodes = fieldNodes.concat(_getFieldNodes(\"div\")); if (fieldNodes.length > 0) { for (var i = 0; i < fieldNodes.length; i++) { var fieldNode = fieldNodes[i]; /*var filedHeight = fieldNode.style.height; if (filedHeight) { filedHeight = parseInt(filedHeight, 10); } else { filedHeight = 0; }*/ var filedScoreHeight = Math.max(fieldNode.scrollHeight, fieldNode.offsetHeight, fieldNode.clientHeight); var fieldWidth = _getContentAreaWidth(fieldNode); var fieldW = fieldWidth - _getAttWidth(fieldNode); if (\"logoimg\" == fieldNode.getAttribute(\"id\")) { var imgObj = document.getElementById(\"logoimg_img\"); if (imgObj) { imgObj.style.width = fieldW + \"px\"; imgObj.style.height = filedScoreHeight + \"px\"; fieldNode.style.display = \"none\"; imgObj.style.display = \"\"; } } else { if (fieldW > 0) { fieldNode.style.width = fieldW + \"px\"; } /*if (filedScoreHeight > filedHeight) { fieldNode.style.height = filedScoreHeight + \"px\"; }*/ } } } }");
        htmlForm.append("function _getRuntimeStyle(obj, k) { var v = null; if (obj.currentStyle){ v = obj.currentStyle[k]; } else{ v = window.getComputedStyle(obj, null).getPropertyValue(k); } return v; } ");
        htmlForm.append("function _getContentAreaWidth(domObj){  var spanClientWidth = domObj.clientWidth;  var paddingWidth = 0; var paddingLeft = _getRuntimeStyle(domObj, \"paddingLeft\") || _getRuntimeStyle(domObj, \"padding-left\"); if (paddingLeft) { if (paddingLeft.indexOf(\"px\") != -1) { paddingWidth += parseInt(paddingLeft.replace(\"px\", \"\")); } else if (paddingLeft.indexOf(\"pt\") != -1) { paddingWidth += parseInt(paddingLeft.replace(\"pt\", \"\")) * (4 / 3); } } var paddingRight = _getRuntimeStyle(domObj, \"paddingRight\") || _getRuntimeStyle(domObj, \"padding-right\"); if (paddingRight) { if (paddingRight.indexOf(\"px\") != -1) { paddingWidth += parseInt(paddingRight.replace(\"px\", \"\")); } else if (paddingRight.indexOf(\"pt\") != -1) { paddingWidth += parseInt(paddingRight.replace(\"pt\", \"\")) * (4 / 3); } }  return (spanClientWidth - paddingWidth); }");
        htmlForm.append("function _getAttWidth(eNode, type) {  var totalValue = 0; if(type){ var tValue = _getRuntimeStyle(eNode, type); totalValue = parseInt(tValue, 10); if(!totalValue){ totalValue = 0; } }else{ var fBorderL = _getRuntimeStyle(eNode, \"borderLeftWidth\") || _getRuntimeStyle(eNode, \"border-left-width\"); var fBorderR = _getRuntimeStyle(eNode, \"borderRightWidth\") || _getRuntimeStyle(eNode, \"border-right-width\"); var fPaddingL = _getRuntimeStyle(eNode, \"paddingLeft\") || _getRuntimeStyle(eNode, \"padding-left\"); var fPaddingR = _getRuntimeStyle(eNode, \"paddingRight\") || _getRuntimeStyle(eNode, \"padding-right\"); var fMarginL = _getRuntimeStyle(eNode, \"marginLeft\") || _getRuntimeStyle(eNode, \"margin-left\"); var fMarginR = _getRuntimeStyle(eNode, \"marginRight\") || _getRuntimeStyle(eNode, \"margin-right\");  var bLValue = parseInt(fBorderL, 10); var bRValue = parseInt(fBorderR, 10); var pLValue = parseInt(fPaddingL, 10); var pRValue = parseInt(fPaddingR, 10); var mLValue = parseInt(fMarginL, 10); var mRValue = parseInt(fMarginR, 10);   if (bLValue) { totalValue += bLValue; } if (bRValue) { totalValue += bRValue; } if (pLValue) { totalValue += pLValue; } if (pRValue) { totalValue += pRValue; } if (mLValue) { totalValue += mLValue; } if (mRValue) { totalValue += mRValue; } }  return totalValue; }  ");
        htmlForm.append("function _getFieldNodes(tageName) { var ret = []; var fieldNodes = document.getElementsByTagName(tageName); if (fieldNodes && fieldNodes.length > 0) { for (var i = 0; i < fieldNodes.length; i++) { var fieldNode = fieldNodes[i]; var _nodeType = fieldNode.getAttribute(\"_nodeType\"); if (\"_formFieldNode_\" == _nodeType) { ret.push(fieldNode); } } } return ret; }");
        htmlForm.append("</script>");

        htmlForm.append(headEnd);
        htmlForm.append(bodyBegin);

        // 截取公文单内容
        Matcher matcher = templatePatt.matcher(formContent);
        String newFormContent = formContent;
        if (matcher.find()) {
            newFormContent = matcher.group(1);
        }

        // 提取文单里面的Image
        Matcher m = nomalImgPatt.matcher(newFormContent);
        while (m.find()) {
            String imgStr = m.group();
            String srcId = m.group(1);
            if (Strings.isNotBlank(srcId)) {
                long fileId = Long.parseLong(srcId);

                V3XFile file = getCtpFile(fileId);
                if (file != null) {
                    extInfo.addCtpFile(file);
                    String fileName = file.getId() + "decryption";
                    ;
                    String newImg = replaceSrc(imgStr, tempFile + "/" + fileName);
                    newFormContent = newFormContent.replace(imgStr, newImg);
                }
            }
        }

        // 匹配字段
        Matcher matcherSpan = fieldSpanPatt2.matcher(newFormContent);
        String finalFormContent = newFormContent;
		while (matcherSpan.find()) {
			String spanNode = matcherSpan.group(0);// 字段SPAN
			String spanInfo = matcherSpan.group(1);// SPAN的全部属性
			String field = matcherSpan.group(2);// fieldName
			String spanValue = matcherSpan.group(3);//SPAN中的值
			

			String styleAttr = getNodeAttr(spanInfo, "style");// style信息
			String classAttr = getNodeAttr(spanInfo, "class");// class信息
			String fieldHeight = getStyleValue(styleAttr, "height");
			String fieldWidth = getStyleValue(styleAttr, "width");

			String minHeight = "";
			if (Strings.isNotEmpty(fieldHeight)) {
				minHeight = "min-height:" + fieldHeight + ";";
			}
			String commonCss = ";overflow-x:hidden;overflow-y:auto;white-space:normal;word-break: break-all;word-wrap: break-word;border:0px;height:auto;"
					+ minHeight;

			String fieldValue = "";

			String options = (String) optionMap.get(field);
			if (Strings.isNotBlank(options)) {

				// 去掉关联文档
				fieldValue = options.replaceAll(
						"(?i)(?i)<div id='attachmentDiv_[^<]*?><img[^<]*?><a class=\"like-a\".*?<\\/div>",
						"");

				// 去掉附件里面的高度设置
				fieldValue = fieldValue.replaceAll(
						"(?i)(<div id='attachmentDiv_[^>]*?)height:[^>]*?;([^>]*?>)",
						"$1height:auto;$2");

				// 去掉onlick
				fieldValue = fieldValue.replaceAll(
						"(?i)onclick='javascript:showV3XMemberCard(.*?)'", "");

				// 附件图标
				Matcher attImgM = attImgPatt.matcher(fieldValue);
				while (attImgM.find()) {

					String attImg = attImgM.group(0);
					String imgBegin = attImgM.group(1);
					String attUrl = attImgM.group(2);
					String imgEnd = attImgM.group(3);

					attUrl = attUrl.replace(
							AppContext.getRawRequest().getContextPath(), "");
					if (!attUrl.startsWith("/")) {
						attUrl = "/" + attUrl;
					}
					extInfo.addFiles(attUrl.substring(1, attUrl.length()));// 文件拷贝
					fieldValue = fieldValue.replace(attImg,
							imgBegin + tempFile + attUrl + imgEnd);
				}

				// 附件文件
				Matcher attM = attFilePatt.matcher(fieldValue);
				List<Attachment> oAtts = opinionAttMap.get(field);
				if (Strings.isNotEmpty(oAtts)) {
					extInfo.addAllAttFIle(oAtts);
				}
				while (attM.find()) {

					String attStr = attM.group(0);
					String aBegin = attM.group(1);
					String aEnd = attM.group(2);
					fieldValue = fieldValue.replace(attStr,
							aBegin + " " + aEnd);
				}

				// 图片签名
				Matcher signm = picSignPatt.matcher(fieldValue);
				while (signm.find()) {
					String imgStr = signm.group();
					String srcId = signm.group(1);
					if (Strings.isNotBlank(srcId)) {
						long id = Long.parseLong(srcId);

						V3xHtmDocumentSignature signet = htmSignetManager
								.getById(id);
						if (signet != null) {
							String body = signet.getFieldValue();
							byte[] b = EdocHelper.hex2byte(body);
							String fileName = "pic_sign_" + id;

							extInfo.addByteFile(fileName, b);

							String newImg = replaceSrc(imgStr,
									tempFile + "/" + fileName);
							fieldValue = fieldValue.replace(imgStr, newImg);
						}
					}
				}

				// 手写签批处理
				if (Strings.isNotEmpty(handWrites)) {

					for (V3xHtmDocumentSignature handWrite : handWrites) {
						String filedName = handWrite.getFieldName();
						Long affairId = handWrite.getAffairId();
						String opinionLocation = "<div id='" + affairId + "'";// 签批定位
						if (("hw" + field).equals(filedName)
								&& fieldValue.indexOf(opinionLocation) != -1) {

							// 写图片
							String srcData = handWrite.getFieldValue();
							String fileName = "hand_sign_" + affairId;
							fileName = fileName.replace("-", "0");
							byte[] signPicByte = getPic(srcData);
							if (signPicByte != null) {
								extInfo.addByteFile(fileName, signPicByte);
								String imgStr = "<img alt=\"签名\" src=\""
										+ tempFile + "/" + fileName
										+ "\" style=\"width:100%;height:100%;\">";
								fieldValue = fieldValue.replace(
										opinionLocation,
										imgStr + opinionLocation);
							}
						}
					}
				}
			}else{
				//直接去拼装好的span中的值
				fieldValue = spanValue;
			}
			
			// 意见等信息
			finalFormContent = finalFormContent.replace(spanNode,
					"<div _nodeType=\"_formFieldNode_\" id=\"" + field
					+ "\" style=\"" + styleAttr + commonCss
					+ "\" class=\"" + classAttr + "\">"
					+ fieldValue + "</div>");

		}

        htmlForm.append(finalFormContent);

        // 样式补充
        htmlForm.append("<STYLE>.body-detail-HTML { WIDTH: 800px } .body-detail { WIDTH: 800px } .contentText P { LINE-HEIGHT: 1.5; FONT-FAMILY: \"Microsoft YaHei\",SimSun,Arial,Helvetica,sans-serif; FONT-SIZE: 16px; WORD-BREAK: break-all } TD { LINE-HEIGHT: 1.5; FONT-FAMILY: \"Microsoft YaHei\",SimSun,Arial,Helvetica,sans-serif; FONT-SIZE: 16px; WORD-BREAK: break-all;text-align:left } UL { LINE-HEIGHT: 1.5; FONT-FAMILY: \"Microsoft YaHei\",SimSun,Arial,Helvetica,sans-serif; FONT-SIZE: 16px; WORD-BREAK: break-all } LI { LINE-HEIGHT: 1.5; FONT-FAMILY: \"Microsoft YaHei\",SimSun,Arial,Helvetica,sans-serif; FONT-SIZE: 16px; WORD-BREAK: break-all } DIV { LINE-HEIGHT: 1.5; FONT-FAMILY: \"Microsoft YaHei\",SimSun,Arial,Helvetica,sans-serif; FONT-SIZE: 16px; WORD-BREAK: break-all } A { LINE-HEIGHT: 1.5; FONT-FAMILY: \"Microsoft YaHei\",SimSun,Arial,Helvetica,sans-serif; FONT-SIZE: 16px; WORD-BREAK: break-all } OL { LINE-HEIGHT: 1.5; FONT-FAMILY: \"Microsoft YaHei\",SimSun,Arial,Helvetica,sans-serif; FONT-SIZE: 16px; WORD-BREAK: break-all } PRE { LINE-HEIGHT: 1.5; FONT-FAMILY: \"Microsoft YaHei\",SimSun,Arial,Helvetica,sans-serif; FONT-SIZE: 16px; WORD-BREAK: break-all } </STYLE>");

        htmlForm.append(bodyEnd);

        htmlForm.append(htmlEnd);

        extInfo.setContent(htmlForm.toString());

        return extInfo;
    	
    }

    private static String changeFontsize(String opinion, String[][] styles) {
        
        String myOpinion = opinion;
        
        if(styles != null){
            StringBuilder cssStr = new StringBuilder();
            for(int i = 0; i < styles.length; i++){
                String[] style = styles[i];
                if(Strings.isNotBlank(style[1])){
                    cssStr.append(";").append(style[0]).append(":").append(style[1]);
                }
            }
            
            if(cssStr.length() > 0){
                String css = cssStr.toString();
                myOpinion = addStyle(myOpinion, "div", css);
                myOpinion = addStyle(myOpinion, "span", css);
            }
        }
        return myOpinion;
    }
    
    private static String addStyle(String opinion, String tagNode, String styleVal){
        
        String upTagNode = "<" + tagNode.toUpperCase();
        String lowTagNode = "<" + tagNode.toLowerCase();
        
        String newStr = opinion;
        
        String[] tempArry = new String[]{upTagNode, lowTagNode};
        
        for(int i = 0; i < tempArry.length; i++){
            
            String tempTagNode = tempArry[i];
            int index = opinion.indexOf(tempTagNode, 0);//从0点开始搜索
            while(index != -1){
                int tempEnd = opinion.indexOf(">", index);
                String attrStr = opinion.substring(index + tempTagNode.length(), tempEnd);
                
                if(attrStr.indexOf("attachmentDiv") == -1){//排除附件DIV
                    
                    String newSttStr = attrStr;
                    if(newSttStr.matches("(?i)style")){
                        newSttStr = newSttStr.replace("(?i)style[ ]*?=([\"']{1})(.*?)[\"']{1}", "style='$2;"+styleVal+";'");
                    }else {
                        newSttStr += " style='"+styleVal+"'";
                    }
                    newStr = newStr.replace(tempTagNode + attrStr + ">", tempTagNode + newSttStr + ">");
                }
                
                index = opinion.indexOf(tempTagNode, tempEnd);//从继续搜索
            }
        }
        return newStr;
    }
    
    /**
     * 截取html节点的属性
     * 
     * @Author : xuqw
     * @Date : 2015年5月18日下午7:07:31
     * @param node
     * @param styleName
     * @return
     */
    private static String getNodeAttr(String node, String styleName) {

        String ret = "";

        // 只保留style属性
        String styleReg = styleName + "=[\"']([\\s\\S]*?)[\"']";
        Pattern stylePatt = Pattern.compile(styleReg, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher styleMatcher = stylePatt.matcher(node);
        if (styleMatcher.find()) {
            ret = styleMatcher.group(1);
        }
        return ret;
    }

    /**
     * 解析元素名称
     * 
     * @Author : xuqw
     * @Date : 2015年5月21日上午11:16:26
     * @param styleStr
     * @param styleName
     * @return
     */
    private static String getStyleValue(String styleStr, String styleName) {

        String ret = "";

        // 只保留style属性
        String styleReg = styleName + "[ ]*:[ ]*([^;]*);?";
        Pattern stylePatt = Pattern.compile(styleReg, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher styleMatcher = stylePatt.matcher(styleStr);
        if (styleMatcher.find()) {
            ret = styleMatcher.group(1);
        }
        return ret;
    }

    /**
     * 替换img的src
     * 
     * @Author : xuqiangwei
     * @Date : 2015年2月15日上午1:35:17
     * @param img
     * @param newSrc
     * @return
     */
    private static String replaceSrc(String img, String newSrc) {

        String ret = img.replaceAll("src=['\"].*?['\"]", "src=\"" + newSrc + "\"");

        return ret;
    }

    /**
     * 获取V3xfile
     * 
     * @Author : xuqw
     * @Date : 2015年5月22日上午11:44:26
     * @param fileId
     * @return
     * @throws BusinessException
     */
    private static V3XFile getCtpFile(long fileId) throws BusinessException {

        FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
        
        V3XFile v3xfile = fileManager.getV3XFile(fileId);

        return v3xfile;
    }

    /**
     * 解析Office控件的签名
     * 
     * @Author : xuqw
     * @Date : 2015年5月20日下午4:43:11
     * @param srcData
     * @param filePath
     * @return
     */
    private static byte[] getPic(String srcData) {

        byte[] pictureValue = null;
        DBstep.iMsgServer2000 msgObj = new DBstep.iMsgServer2000();
        if (Strings.isNotBlank(srcData)) {
            pictureValue = msgObj.LoadRevisionAsImgByte(srcData);
        }

        return pictureValue;
    }
}
