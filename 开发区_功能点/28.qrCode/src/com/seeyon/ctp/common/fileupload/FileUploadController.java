/**
 *
 */
package com.seeyon.ctp.common.fileupload;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.PerformanceConfig;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.encrypt.CoderFactory;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.Constants;
import com.seeyon.ctp.common.filemanager.NoSuchPartitionException;
import com.seeyon.ctp.common.filemanager.event.FileDownloadEvent;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.filemanager.manager.FileSecurityManager;
import com.seeyon.ctp.common.filemanager.manager.PartitionManager;
import com.seeyon.ctp.common.fileupload.bean.*;
import com.seeyon.ctp.common.fileupload.util.FileServiceUtil;
import com.seeyon.ctp.common.fileupload.util.FileServiceUtil.ModeEnum;
import com.seeyon.ctp.common.fileupload.util.FileServiceUtil.RequestTypeEnum;
import com.seeyon.ctp.common.fileupload.util.FileUploadUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.Partition;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.web.util.WebUtil;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.util.*;
import com.seeyon.ctp.util.annotation.NeedlessCheckLogin;
import com.seeyon.ctp.util.annotation.SetContentType;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:tanmf@seeyon.com">Tanmf</a>
 * @version 1.0 2006-11-15
 */
public class FileUploadController extends BaseController {
    private final static Log log = CtpLogFactory.getLog(FileUploadController.class);
    // 下载限制
    private static Integer maxDownloadConnections = PerformanceConfig.getInstance().getIntegerConfig(
            "fileDowload.maxConnections", 65535);
    private static OperationControllable downloadCounter = new OperationCounter(maxDownloadConnections);
    private static Map<String, String> RTE_type = new HashMap<String, String>();


    static {
        RTE_type.put("image", "image/jpeg");
        RTE_type.put("flash", "application/x-shockwave-flash");
    }

    private static final String HEADER_IFMODSINCE = "If-Modified-Since";
    private static final String HEADER_LASTMOD = "Last-Modified";
    private static final long Etag_CacheDate = 1000L * 60 * 60 * 12 * 7;

    private FileManager fileManager;
    private AttachmentManager attachmentManager;
    private FileSecurityManager fileSecurityManager;
    private PartitionManager partitionManager;
    private String clientAbortExceptionName = "ClientAbortException";
    private String contentTypeCharset = "UTF-8";                                      //使用统一编码 LEIGF 20090911
    private String htmlSuffix;

    public void setFileSecurityManager(FileSecurityManager fileSecurityManager) {
        this.fileSecurityManager = fileSecurityManager;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    public void setPartitionManager(PartitionManager partitionManager) {
        this.partitionManager = partitionManager;
    }


    public void setHtmlSuffix(String htmlSuffix) {
        this.htmlSuffix = htmlSuffix;
    }

    /**
     * 用户关闭下载窗口时候，有servlet容器抛出的异常
     * @param clientAbortExceptionName 类的simapleName，如<code>ClientAbortException</code>
     */
    public void setClientAbortExceptionName(String clientAbortExceptionName) {
        this.clientAbortExceptionName = clientAbortExceptionName;
    }

    /**
     * 上传文件
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView processUpload(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = null;
        String userAgent = request.getHeader("User-Agent");
        if ((userAgent != null) && (userAgent.toUpperCase().contains("IE") || userAgent.toUpperCase().contains("RV:11"))) {
            //当客户端为IE浏览器时直接进入到原来的页面使用精灵
            modelAndView = new ModelAndView("ctp/common/fileUpload/uploadForGeniues");
        } else {
            modelAndView = new ModelAndView("ctp/common/fileUpload/upload");
        }
        //ModelAndView modelAndView = new ModelAndView("ctp/common/fileUpload/upload");

        String extensions = request.getParameter("extensions");
        String applicationCategory = request.getParameter("applicationCategory");
//        String destDirectory = null;//request.getParameter("destDirectory"); //为了安全，此字段永远为空 tanmf
//        String destFilename = null;//request.getParameter("destFilename");   //为了安全，此字段永远为空 tanmf
        String typeStr = request.getParameter("type");
        String firstSave = request.getParameter("firstSave") == null ? "" : request.getParameter("firstSave");
        String maxSizeStr = request.getParameter("maxSize");
        String from = request.getParameter("from");
        String serverTime = request.getParameter("serverTime");
//        String targetAction=request.getParameter("targetAction");
        TimeZone serverTimeZone = TimeZone.getDefault();
        if ("true".equals(serverTime)) {
            modelAndView.addObject("timeZoneValue", serverTimeZone.getID());
        }
        Constants.ATTACHMENT_TYPE type = null;
        if (StringUtils.isNotBlank(typeStr)) {
            type = EnumUtil.getEnumByOrdinal(Constants.ATTACHMENT_TYPE.class,
                    Integer.valueOf(typeStr));
        } else {
            type = Constants.ATTACHMENT_TYPE.FILE;
        }

        if (StringUtils.isBlank(firstSave)) {
            firstSave = "false";
        }

        ApplicationCategoryEnum category = null;
        if (StringUtils.isNotBlank(applicationCategory)) {
            category = ApplicationCategoryEnum.valueOf(Integer.valueOf(applicationCategory));
        } else {
            category = ApplicationCategoryEnum.global;
            log.warn("上传文件：v3x:fileUpload没有设定applicationCategory属性，将设置为‘全局’。");
        }

        Long maxSize = Long.parseLong(PerformanceConfig.getInstance().getConfig("fileUpload.maxSize")) * 1024 * 1024;
        if (StringUtils.isNotBlank(maxSizeStr)) {
            maxSize = Long.valueOf(maxSizeStr);
        }

        Map<String, V3XFile> v3xFiles = null;
        try {
//            File destFile = null;
//            if (StringUtils.isNotBlank(destFilename)) { //存为指定的文件
//                destFile = new File(FilenameUtils.separatorsToSystem(destFilename));
//                v3xFiles = fileManager.uploadFiles(request, extensions, destFile, maxSize);
//            } else if (StringUtils.isNotBlank(destDirectory)) { //存到指定的文件夹
//                v3xFiles = fileManager.uploadFiles(request, extensions, destDirectory, maxSize);
//            } else { //系统默认分区
            v3xFiles = fileManager.uploadFiles(request, extensions, maxSize);
//            }
            if (v3xFiles != null) {
                List<String> keys = new ArrayList<String>(v3xFiles.keySet());
                Collections.sort(keys, new FileFieldComparator());

                List<Attachment> atts = new ArrayList<Attachment>();
                for (String key : keys) {
                    Attachment att = new Attachment(v3xFiles.get(key), category, type);
                    atts.add(att);
                }

                if ("true".equalsIgnoreCase(firstSave)) {
                    attachmentManager.create(atts);
                }

                modelAndView.addObject("atts", atts);
                HttpSession session = request.getSession();
                session.setAttribute("repeat", request.getParameter("repeat"));

                if (from != null && "a8genius".equals(from)) {
                    //在jsonObject中设置中文出现乱码此处需要设置编码（因英福美控件问题，此处先设置为中文，后期调整）
                    response.setContentType("text/html;charset=UTF-8");
                    PrintWriter out = response.getWriter();
/*                    Attachment att = atts.get(0);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.putOpt("type", att.getType());
                    jsonObject.putOpt("filename", att.getFilename());
                    jsonObject.putOpt("mimeType", att.getMimeType());
                    jsonObject.putOpt("createDate", Datetimes.formatDatetime(att.getCreatedate()));
                    jsonObject.putOpt("size", att.getSize());
                    jsonObject.putOpt("fileUrl", String.valueOf(att.getFileUrl()));
                    jsonObject.putOpt("extension", att.getExtension());
                    jsonObject.putOpt("icon", att.getIcon());
                    out.println(jsonObject.toString());*/
                    String jsonStr = JSONUtil.toJSONString(atts);
//                    log.info("确认json转换中文是否出错："+jsonStr);
                    out.println(jsonStr);
                    out.flush();
                    return null;
                }
            }
        } catch (NoSuchPartitionException e) {
            log.error("", e);
            modelAndView.addObject("e", e);
        } catch (BusinessException e) {
            log.error(e.getLocalizedMessage(), e);
            ;
            modelAndView.addObject("e", e);
        } catch (Exception e) {
            log.error("", e);
            modelAndView.addObject("e", new BusinessException("fileupload.exception.unknown", e.getMessage()));
        }
        return modelAndView;

    }


    /**
     * 上传文件
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView processUploadForCap4(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = null;
        String userAgent = request.getHeader("User-Agent");
        if ((userAgent != null) && (userAgent.toUpperCase().contains("IE") || userAgent.toUpperCase().contains("RV:11"))) {
            //当客户端为IE浏览器时直接进入到原来的页面使用精灵
            modelAndView = new ModelAndView("ctp/common/fileUpload/uploadForGeniues");
        } else {
            modelAndView = new ModelAndView("ctp/common/fileUpload/upload");
        }

        String extensions = request.getParameter("extensions");
        String applicationCategory = request.getParameter("applicationCategory");
        String typeStr = request.getParameter("type");
        String firstSave = request.getParameter("firstSave") == null ? "" : request.getParameter("firstSave");
        String maxSizeStr = request.getParameter("maxSize");

        Constants.ATTACHMENT_TYPE type = null;
        if (StringUtils.isNotBlank(typeStr)) {
            type = EnumUtil.getEnumByOrdinal(Constants.ATTACHMENT_TYPE.class,
                    Integer.valueOf(typeStr));
        } else {
            type = Constants.ATTACHMENT_TYPE.FILE;
        }

        if (StringUtils.isBlank(firstSave)) {
            firstSave = "false";
        }

        ApplicationCategoryEnum category = null;
        if (StringUtils.isNotBlank(applicationCategory)) {
            category = ApplicationCategoryEnum.valueOf(Integer.valueOf(applicationCategory));
        } else {
            category = ApplicationCategoryEnum.global;
            log.warn("上传文件：v3x:fileUpload没有设定applicationCategory属性，将设置为‘全局’。");
        }

        Long maxSize = Long.parseLong(PerformanceConfig.getInstance().getConfig("fileUpload.maxSize")) * 1024 * 1024;
        if (StringUtils.isNotBlank(maxSizeStr)) {
            maxSize = Long.valueOf(maxSizeStr);
        }

        Map<String, V3XFile> v3xFiles = null;
        try {
            v3xFiles = fileManager.uploadFiles(request, extensions, maxSize);
            if (v3xFiles != null) {
                List<String> keys = new ArrayList<String>(v3xFiles.keySet());
                Collections.sort(keys, new FileFieldComparator());

                List<Attachment> atts = new ArrayList<Attachment>();
                for (String key : keys) {
                    Attachment att = new Attachment(v3xFiles.get(key), category, type);
                    atts.add(att);
                }

                if ("true".equalsIgnoreCase(firstSave)) {
                    attachmentManager.create(atts);
                }

                modelAndView.addObject("atts", atts);
                HttpSession session = request.getSession();
                session.setAttribute("repeat", request.getParameter("repeat"));

                //在jsonObject中设置中文出现乱码此处需要设置编码（因英福美控件问题，此处先设置为中文，后期调整）
                response.setContentType("text/html;charset=UTF-8");
                PrintWriter out = response.getWriter();
                String jsonStr = JSONUtil.toJSONString(atts);
                out.println(jsonStr);
                out.flush();
                return null;
            }
        } catch (NoSuchPartitionException e) {
            log.error("", e);
            modelAndView.addObject("e", e);
        } catch (BusinessException e) {
            log.error(e.getLocalizedMessage(), e);
            ;
            modelAndView.addObject("e", e);
        } catch (Exception e) {
            log.error("", e);
            modelAndView.addObject("e", new BusinessException("fileupload.exception.unknown", e.getMessage()));
        }
        return modelAndView;

    }

    /**
     * 显示正文编辑器中图片或flash
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @SetContentType
    @NeedlessCheckLogin
    public ModelAndView showRTE(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long fileId = Long.valueOf(request.getParameter("fileId"));
        User user = AppContext.getCurrentUser();
        String ucFlag = request.getParameter("ucFlag");
        if (user == null) {
            boolean accessDenied = true;
            if (fileSecurityManager != null) {
                accessDenied = !fileSecurityManager.isNeedlessLogin(fileId);
            }
            if (accessDenied) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return null;
            }
        }

        String small = Strings.escapeNULL(request.getParameter("showType"), "");
        String smallPX = Strings.escapeNULL(request.getParameter("smallPX"), ""); //缩略图尺寸

/*        if(user!=null && com.seeyon.ctp.common.constants.Constants.login_useragent_from.weixin.equals(user.getUserAgentFromEnum())){
        	small = "small";
        }*/

        String etag = String.valueOf(fileId) + small + smallPX;
        if (WebUtil.checkEtag(request, response, etag)) { //匹配，没有修改，浏览器已经做了缓存
            return null;
        }
        Date createDate = null;
        if (Strings.isNotBlank(request.getParameter("createDate"))) {
            createDate = Datetimes.parseDate(request.getParameter("createDate"));
        } else {
            V3XFile f = this.fileManager.getV3XFile(fileId);
            if (f == null) {
                //防护校验，防止数据库中对应的记录已被删除(2016-11-17,支艳强)
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return null;
            }
            createDate = f.getCreateDate();
        }

        String type = request.getParameter("type");
        if (type != null && type.startsWith("flash")) {
            type = "flash";
        }
        String mimeType = RTE_type.get(type);
        if (mimeType == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        WebUtil.writeETag(request, response, etag, Etag_CacheDate);

        response.setContentType(mimeType);

        File f = null;
        if (!"small".equals(small)) {
            if ("yes".equals(ucFlag)) {
                //致信端特殊处理
                f = this.fileManager.getFileForUC(fileId, createDate);
            } else {
                f = this.fileManager.getFile(fileId, createDate);
            }
        } else { //缩略图
            if (Strings.isNotBlank(smallPX)) {
                if ("yes".equals(ucFlag)) {
                    f = this.fileManager.getThumFileForUC(fileId, createDate, smallPX);
                } else {
                    f = this.fileManager.getThumFile(fileId, createDate, Integer.parseInt(smallPX));
                }
            } else {
                if ("yes".equals(ucFlag)) {
                    f = fileManager.getThumFileForUC(fileId, createDate, smallPX);
                } else {
                    f = this.fileManager.getThumFile(fileId, createDate);
                }
            }
        }
        if (f == null) { //是否提供提示信息？
            return null;
        }

        InputStream in = null;
        ServletOutputStream out = response.getOutputStream();
        try {
            in = new FileInputStream(f);
            if (in != null) {
                CoderFactory.getInstance().download(in, out);
            }
        } catch (FileNotFoundException e) {
            log.warn(e);
        } catch (Exception e) {
            if (e.getClass().getSimpleName().equals(this.clientAbortExceptionName)) {
                log.debug("用户关闭下载窗口: " + e.getMessage());
            } else {
                log.error("", e);
            }
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }

        return null;
    }

    public ModelAndView download(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ModelAndView m = new ModelAndView("ctp/common/fileUpload/download");

        //	若开启了文件下载微服务并且强制协同本地下载标识不为true的情况下，跳转至微服务下载地址
        if (isRemoteDownload(request)) {

            String ticket = FileServiceUtil.generateDownloadTicket(Long.valueOf(request.getParameter("fileId")));
            String remoteDownUrl = generateRemoteDownloadUrl(request, ticket);
            if (remoteDownUrl != null) {
                response.sendRedirect(Strings.toHTML(remoteDownUrl));
                return null;
            }
        }

        if (!downloadCounter.check()) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println("<script>alert('" + Strings.toHTML(ResourceUtil.getStringByParams("fileuoload.downoad.exceedConnection", this.maxDownloadConnections)) + "');</script>");
            return null;
        }

        // 通知计数器下载开始。
        downloadCounter.start();

        Map<String, String> ps = getParameterMap(request);

        m.addObject("ps", ps);

        String filename = request.getParameter("filename");
        String suffix = FilenameUtils.getExtension(filename).toLowerCase();

        //如果从精灵发送的请求，这里直接下载，不区分html页
        String from = request.getParameter("from");
        if (from != null && "a8geniues".equals(from)) {
            m.addObject("isHTML", Pattern.matches(htmlSuffix, suffix));
        } else {
            if (Pattern.matches(htmlSuffix, suffix) && !"mobile".equals(from)) {
                m.addObject("isHTML", true);
            }
        }

        return m;

    }

    private boolean isRemoteDownload(HttpServletRequest request) {

        String isSystemRecieveForm = request.getParameter("isSystemForm"); // 是否是系统预置的收文单
        String isSystemRedTemplete = request.getParameter("isSystemRedTemplete");// 是否是系统预置的套红模板。
        String isInfoTemplate = request.getParameter("isInfoTemplate");//信息报送模版下载
        String fileId = request.getParameter("fileId");

        if (!FileServiceUtil.isRemoteMode()) {
            return false;
        } else if ("true".equals(request.getParameter("forceLocalDownload"))) {
            return false;
        }

        int appType = request.getParameter("appType") == null ? 0 : Integer.parseInt(request.getParameter("appType"));
        if ("true".equals(isSystemRecieveForm)) {
            return false;
        }

        if ("true".equals(isSystemRedTemplete)) {
            return false;
        }

        if ("true".equals(isInfoTemplate)) {
            return false;
        }

        if ("607151236662539448".equals(fileId) && appType == ApplicationCategoryEnum.info.key()) {
            return false;
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getParameterMap(HttpServletRequest request) {
        Map<String, String> ps = new HashMap<String, String>();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String name = params.nextElement();
            if ("method".equalsIgnoreCase(name)) {
                continue;
            }

            String value = request.getParameter(name);

            ps.put(name, value);
        }
        return ps;
    }

    public ModelAndView doDownload4html(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new FileDownloader(request, response) {
            void setOutput() {
                String swf = request.getParameter("swf");
                if ("true".equals(swf)) {
                    setFlashContenttype();
                } else {
                    setDownloadContentType(response, filename, "attachment", "application/octet-stream");
                }
            }

            void getInputstream() throws Exception {
                String comm = request.getParameter("comm");
                if ("byFileId".equals(comm)) {
                    V3XFile v3xfile = fileManager.getV3XFile(fileId);
                    createDate = v3xfile.getCreateDate();
                    filename = v3xfile.getFilename();
                } else {
                    createDate = Datetimes.parseDate(request.getParameter("createDate"));
                    filename = request.getParameter("filename");
                    //filename=new String(filename.getBytes("iso8859-1"),"UTF-8"); //在浏览器打开和保存使用的编码转换存不同，打开时没有进行编码转换，暂时改用GBK
                    filename = new String(filename.getBytes(), "iso8859-1");//针对form提交进行转换
                }
                in = fileManager.getFileInputStream(fileId, createDate);
            }

        }.download();
    }

    abstract class FileDownloader {
        protected final HttpServletRequest request;
        protected final HttpServletResponse response;
        protected Long fileId = null;
        protected Date createDate = null;
        protected InputStream in = null;
        protected ServletOutputStream out;
        protected String filename = null;
        protected String contentType;
        private V3XFile v3xFile;

        public FileDownloader(HttpServletRequest request, HttpServletResponse response) {
            super();
            this.request = request;
            this.response = response;
        }

        // 避免多次调用getV3XFile
        protected V3XFile getFile() throws BusinessException {
            if (v3xFile == null)
                v3xFile = fileManager.getV3XFile(fileId);
            return v3xFile;
        }

        public ModelAndView download() throws Exception {
            request.setCharacterEncoding(contentTypeCharset);
            fileId = Long.valueOf(request.getParameter("fileId"));

            if (!beforeDownload())
                return null;
            out = response.getOutputStream();
            try {
                contentType = "application/octet-stream";
                getInputstream();

                if (in == null) {
                    if ("m1".equals(request.getParameter("from")))
                        throw new Exception("FileNoFound");

                    // 触发文件下载事件，应用可对不存在的id进行处理
                    FileDownloadEvent event = new FileDownloadEvent(this, null, fileId, request, response, fileManager);
                    event.setInputStream(in);
                    EventDispatcher.fireEventWithException(event);
                    if (event.getInputStream() == null) {
                        showError("FileNoFound", null, out, request, response);
                    } else {
                        CoderFactory.getInstance().download(event.getInputStream(), out);
                    }
                    return null;
                } else {
                    setOutput();
                    // 触发文件下载事件
                    FileDownloadEvent event = new FileDownloadEvent(this, getFile(), fileId, request, response, fileManager);
                    event.setInputStream(in);
                    EventDispatcher.fireEventWithException(event);
                    CoderFactory.getInstance().download(event.getInputStream(), out);
                }
            } catch (NoSuchPartitionException e) {
                log.error("文件下载出错，分区异常：", e);
                showError(null, e, out, request, response);
                return null;
            } catch (Throwable e) {
                log.error("文件下载出错：", e);
                if (e.getClass().getSimpleName().equals(clientAbortExceptionName)) {
                    log.debug("用户关闭下载窗口: " + e.getMessage());
                } else {
                    if ("m1".equals(request.getParameter("from"))) {
                        log.error(e.getLocalizedMessage(), e);
                        throw new Exception("FileNoFound");
                    }
                    showError("Exception", null, out, request, response);
                    return null;
                }
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);

                if ("true".equalsIgnoreCase(request.getParameter("deleteFile"))) {
                    log.info("deleteFile 附件已被删除,附件ID===" + fileId);
                    fileManager.deleteFile(fileId, createDate, true);
                }
                // 通知计数器下载结束，释放。
                downloadCounter.end();
            }

            return null;
        }

        protected void setDownloadContentType(HttpServletResponse response, String filename, String mode,
                                              String contentType1) {
            // 分号会造成文件名截断，trim
            String name = filename.replace(";", "");
            String userAgent = request.getHeader("User-Agent");
            boolean isIe7 = (userAgent != null) && userAgent.toLowerCase().contains("msie 7");
            if (isIe7) {
                response.setContentType(contentType1);
            } else {
                response.setContentType(contentType1 + "; charset=" + contentTypeCharset);
            }
            String fname;
            if (name.startsWith("filename=") || name.startsWith("filename*=")) {
                fname = ";" + name;
            } else {
                fname = ";filename=\"" + name + "\"";
            }
            response.setHeader("Content-disposition", mode + fname);
        }

        protected void setFlashContenttype() {
            setDownloadContentType(response, filename, "inline", "application/x-shockwave-flash");
        }

        // 下载前控制，可以中止下载，跳转到新的目标页面
        protected boolean beforeDownload() throws Exception {
            return true;
        }

        abstract void setOutput();

        abstract void getInputstream() throws Exception;

    }

    /**
     * 下载正文（office正文、wps正文）
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @SetContentType
    public ModelAndView doDownload4Office(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new FileDownloader(request, response) {
            void setOutput() {
                String type = request.getParameter("type");
                String fileName = FileUploadUtil.getOfficeName(fileId, type);
                String attachment = "true".equals(request.getParameter("isOpenFile")) ? "" : "attachment";
                setDownloadContentType(response, fileName, attachment, FileUploadUtil.getOfficeHeader(type));
            }

            void getInputstream() throws Exception {
                String comm = request.getParameter("comm");
                if ("byFileId".equals(comm)) {
                    V3XFile v3xfile = fileManager.getV3XFile(fileId);
                    createDate = v3xfile.getCreateDate();
                } else {
                    createDate = Datetimes.parseDate(request.getParameter("createDate"));
                }
                in = fileManager.getStandardOfficeInputStream(fileId, createDate);
            }
        }.download();
    }

    /**
     * 下载文件
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @SetContentType
    public ModelAndView doDownload(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new FileDownloader(request, response) {
            protected boolean beforeDownload() throws Exception {
                return true;
            }

            void setOutput() {
                String swf = request.getParameter("swf");
                if ("true".equals(swf)) {
                    setFlashContenttype();
                } else {
                    if (!"mobile".equals(request.getParameter("from"))) {
                        contentType = "application/octet-stream";
                    }
                    setDownloadContentType(response, filename, "attachment", contentType);
                }
            }

            void getInputstream() throws BusinessException {
                String comm = request.getParameter("comm");
                if ("byFileId".equals(comm)) {
                    V3XFile v3xfile = getFile();
                    createDate = v3xfile.getCreateDate();
                    filename = v3xfile.getFilename();
                    try {
                        filename = java.net.URLEncoder.encode(filename, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        log.error(e);
                    }
                } else if ("mobile".equals(request.getParameter("from"))) {
                    V3XFile v3xfile = getFile();
                    contentType = v3xfile.getMimeType();
                    createDate = v3xfile.getCreateDate();
                    filename = FileUploadUtil.escapeFileName(v3xfile);
                } else {
                    createDate = Datetimes.parseDate(request.getParameter("createDate"));
                    filename = request.getParameter("filename");

                    // filename=new
                    // String(filename.getBytes("iso8859-1"),"UTF-8");
                    // //在浏览器打开和保存使用的编码转换存不同，打开时没有进行编码转换，暂时改用GBK
/*                    try {
                        if(request.getHeader("User-Agent").toLowerCase().indexOf("macintosh")<0){
                            filename= java.net.URLEncoder.encode(filename, "UTF-8");
                        }

                    } catch (UnsupportedEncodingException e) {
                        log.warn("当前系统不支持UTF-8编码转换：",e);
                    }*/
                    filename = FileUtil.getDownloadFileName(request, filename);
                }
                in = appsPreHandle();

                if (in == null) {
                    long openfileStart = System.currentTimeMillis();
                    in = fileManager.getFileInputStream(fileId, createDate);
                    //带有时区设置的解析中,createDate的解析可能存在问题，eg：服务器上时间为3.22，客户端时间为3.23
                    //文件实际存在3.22目录，而解析成3.23;3.23再次解析无法还原到3.22了，为了解决这种情况有必要
                    //再根据fileId获取一次
                    if (in == null) {
                        V3XFile v3xfile = getFile();
                        createDate = v3xfile.getCreateDate();
                        in = fileManager.getFileInputStream(fileId, createDate);
                    }
                    if (in == null) {
                        //文件不存在，不存在，配合批量下载插件，设置header的response
                        response.addHeader("filestatus", "no");
                    }
                    if (System.currentTimeMillis() - openfileStart > 1000) {
                        log.warn("文件系统打开附件过慢！耗时：" + (System.currentTimeMillis() - openfileStart));
                    }
                }
            }

            // 应用耦合代码，可采用下载事件实现，但考虑性能，先做此实现
            private InputStream appsPreHandle() throws BusinessException {
                String isSystemRecieveForm = request.getParameter("isSystemForm"); // 是否是系统预置的收文单
                String isSystemRedTemplete = request.getParameter("isSystemRedTemplete");// 是否是系统预置的套红模板。
                String isInfoTemplate = request.getParameter("isInfoTemplate");//信息报送模版下载
                int appType = request.getParameter("appType") == null ? 0 : Integer.parseInt(request.getParameter("appType"));
                if ("true".equals(isSystemRecieveForm)) {
                    try { // 系统预置收文单去指定目录查找，不去分区查找。
                        String formType = request.getParameter("formType");
                        String systemFormId = "-2921628185995099164"; //收文
                        String folder = "edoc.folder";
                        if (appType == ApplicationCategoryEnum.info.key()) {//信息报送
                            folder = "govform.folder";
                            systemFormId = "607151236662539448";
                        } else {//公文
                            folder = "edoc.folder";
                            if ("0".equals(formType)) {
                                systemFormId = "6071519916662539448";
                            } else if ("1".equals(formType)) {
                                systemFormId = "-2921628185995099164";
                            } else if ("10".equals(formType)) {
                                systemFormId = "-2921628185995099166";
                            } else {
                                systemFormId = "-1766191165740134579";
                            }
                        }
                        return new FileInputStream(new File(SystemProperties.getInstance().getProperty(folder)
                                + File.separator + "form" + File.separator + systemFormId));
                    } catch (Exception e) {
                        return fileManager.getFileInputStream(fileId, createDate);
                    }
                } else if ("true".equals(isSystemRedTemplete)) {
                    try { // 系统预置示例套红模板去指定目录查找，不去分区查找。
                        return new FileInputStream(new File(SystemProperties.getInstance().getProperty("edoc.folder")
                                + File.separator + "template" + File.separator + "-6001972826857714844"));
                    } catch (Exception e) {
                        return fileManager.getFileInputStream(fileId, createDate);
                    }
                } else if ("true".equals(isInfoTemplate)) {
                    try { // 系统预置示例套红模板去指定目录查找，不去分区查找。
                        StringBuffer filePathBuff = new StringBuffer(SystemProperties.getInstance().getProperty("infosend.folder"));
                        String subPath = "";//相对路径
                        String tempType = request.getParameter("tempType");
                        if ("0".equals(tempType)) {//期刊版式 - 下载模板书签列表
                            subPath = "template/5282854121826306408";
                        }
                        filePathBuff.append(File.separator);
                        filePathBuff.append(subPath);
                        return new FileInputStream(new File(filePathBuff.toString()));
                    } catch (Exception e) {
                        return fileManager.getFileInputStream(fileId, createDate);
                    }
                } else if ("607151236662539448".equals(fileId.toString()) && appType == ApplicationCategoryEnum.info.key()) {
                    //老信息报送升级后，默认的报送单被替换成了普通的报送单,文件ID未变，下载时做兼容
                    String folder = "govform.folder";
                    String systemFormId = "607151236662539447";
                    try {
                        return new FileInputStream(new File(SystemProperties.getInstance().getProperty(folder)
                                + File.separator + "form" + File.separator + systemFormId));
                    } catch (FileNotFoundException e) {
                        return fileManager.getFileInputStream(fileId, createDate);
                    }
                }
                return null;
            }
        }.download();
    }

    private static void showError(String error, BusinessException e, ServletOutputStream out,
                                  HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html;charset=UTF-8");
        String message = null;
        if (error != null) {
            message = ResourceUtil.getString("fileupload.document." + error);
        }

        if (e != null) {
            message = ResourceUtil.getString("fileupload.document.Exception");
        }

        response.addHeader("Rang", "-1"); //不存在

        if (message != null) {
            //response.setContentType("text/html; charset=UTF-8");
            PrintWriter writer = new PrintWriter(out);
            writer.println("<script type=\"text/javascript\">");
            //writer.println("alert(\"" + Functions.urlEncoder(Functions.escapeJavascript(message)) + "\");");
            String str = Strings.escapeJavascript(message);
            String encode;
            try {
                encode = java.net.URLEncoder.encode(str, "UTF-8");
            } catch (UnsupportedEncodingException e1) {
                encode = java.net.URLEncoder.encode(str);
            }
            writer.println("alert(decodeURI(\"" + encode
                    + "\"));");
            if ("blank".equals(request.getParameter("from"))) {
                writer.println("window.open(\"/closeIE7.htm\", \"_self\");");
            }
            writer.println("</script>");
            if ("mobile".equals(request.getParameter("from"))) {//手机端下载
                writer.println(message);
                writer.println("<html><head>");
                writer.println("<meta http-equiv=\"Refresh\" content=\"3;url=mob.do?method=showAffairs\">");
                writer.println("</head></html>");
            }
            writer.flush();
            writer.close();
        }
    }

    /**
     * 删除文件
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView deleteFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("ctp/common/fileUpload/upload");

        try {
            Long fileId = Long.parseLong(request.getParameter("fileId"));
            Date createDate = Datetimes.parseDatetime(request.getParameter("createDate"));

            this.fileManager.deleteFile(fileId, createDate, true);
        } catch (Exception e) {
        }

        return modelAndView;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.seeyon.ctp.common.web.BaseController#index(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = null;
        User user = AppContext.getCurrentUser();
        String userAgent = request.getHeader("User-Agent");
        //V-Join人员不能使用批量上传插件
        if (user.isV5Member() && userAgent != null && (userAgent.toUpperCase().contains("IE") || userAgent.toUpperCase().contains("RV:11"))) {
            //当客户端为IE浏览器时直接进入到原来的页面使用精灵
            modelAndView = new ModelAndView("ctp/common/fileUpload/uploadForGeniues");
        } else {
            modelAndView = new ModelAndView("ctp/common/fileUpload/upload");
        }
        String isA8geniusAdded = "false";
        String quantity = request.getParameter("quantity");
        if (!"1".equals(quantity)) {
            isA8geniusAdded = request.getParameter("isA8geniusAdded");
        }
        modelAndView.addObject("isA8geniusAdded", isA8geniusAdded);
        String str = FileuploadManagerImpl.getMaxSizeStr();
        modelAndView.addObject("maxSize", str);
        return modelAndView;
    }

    public ModelAndView indexforForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("ctp/common/fileUpload/uploadForForm");
        String isA8geniusAdded = request.getParameter("isA8geniusAdded");
        modelAndView.addObject("isA8geniusAdded", isA8geniusAdded);
        String str = FileuploadManagerImpl.getMaxSizeStr();
        modelAndView.addObject("maxSize", str);
        return modelAndView;
    }

    public ModelAndView indexforFormImage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("ctp/common/fileUpload/upLoadImage");
        String isA8geniusAdded = request.getParameter("isA8geniusAdded");
        modelAndView.addObject("isA8geniusAdded", isA8geniusAdded);
        String str = FileuploadManagerImpl.getMaxSizeStr();
        modelAndView.addObject("maxSize", str);
        return modelAndView;
    }


    //	是否有框架层在core中的校验机制，需要确认！@CheckRoleAccess(roleTypes={com.seeyon.ctp.organization.OrgConstants.Role_NAME.SystemAdmin})
    public ModelAndView getFileServiceCofig(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        if (user != null) {
            if (!user.isSystemAdmin() && !user.isAdministrator()) {
                //资源权限验证失败
                StringBuilder sb = new StringBuilder(ResourceUtil.getStringByParams("loginUserState.wuquanfangwen"));
                BusinessException be = new BusinessException(sb.toString());
                //此errorCode在CTPDispatcherServlet做日志记录处理
                be.setCode("invalid_resource_code");
                //此类异常全页面显示，带有整体UE界面设计效果
                be.setFullPage(true);
                throw be;
            }
        }
        ModelAndView modelAndView = new ModelAndView("ctp/common/fileUpload/serviceConfig");
        loadOriginConfig(modelAndView);
        return modelAndView;

    }


    public ModelAndView updateFileServiceCofig(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("ctp/common/fileUpload/serviceConfig");

        String flag = request.getParameter("flag");
        String serverIp = request.getParameter("serverIp");
        int serverPort = Integer.parseInt(request.getParameter("serverPort"));
        String maxDownloadNumStr = request.getParameter("maxDownloadNum");

        boolean originRemoteMode = FileServiceUtil.isRemoteMode();

        FileServiceUtil.switchToMode("on".equals(flag) ? ModeEnum.REMOTE : ModeEnum.LOCAL);
        FileServiceUtil.ip = serverIp;
        FileServiceUtil.port = serverPort;

        modelAndView.addObject("flag", flag);
        modelAndView.addObject("serverIp", serverIp);
        modelAndView.addObject("serverPort", serverPort);
        if (StringUtils.isNotEmpty(maxDownloadNumStr)) {
            int maxDownloadNum = Integer.parseInt(maxDownloadNumStr);
            modelAndView.addObject("maxDownloadNum", maxDownloadNum);
        }
        modelAndView.addObject("showMsg", "menu.system.service.file.setting.ok");

        modelAndView.addObject("localMaxDownloadNum", PerformanceConfig.getInstance().getIntegerConfig("fileDowload.maxConnections"));

        if (FileServiceUtil.isRemoteMode() && StringUtils.isNotEmpty(serverIp)) {

            //	更新远程文件下载微服务的响应配置
            if (StringUtils.isNotEmpty(maxDownloadNumStr)) {
                int maxDownloadNum = Integer.parseInt(maxDownloadNumStr);
                modelAndView.addObject("maxDownloadNum", maxDownloadNum);
                Thresholds thresholds = new Thresholds();
                thresholds.setDownloadNum(maxDownloadNum);
                try {
                    int status = StatusCode.SUCCESS;
                    // 模式未发生变化
                    if (originRemoteMode) {
                        status = setFileServiceThresholds(FileServiceUtil.getRequestUrl(RequestTypeEnum.SET_THRESHOLD), thresholds);
                    } else {
                        status = updateFileServiceConfig(thresholds);
                    }

                    if (status != StatusCode.SUCCESS) {
                        modelAndView.addObject("showMsg", StatusCode.getDesc(status));
                        FileServiceUtil.loadProperties();
                        loadOriginConfig(modelAndView);
                        return modelAndView;
                    }
                } catch (IOException e) {
                    modelAndView.addObject("showMsg", "menu.system.service.file.connect.fail.hint");
                    FileServiceUtil.loadProperties();
                    loadOriginConfig(modelAndView);
                    return modelAndView;
                }
            }
        } else {
            FileServiceUtil.switchToMode(ModeEnum.LOCAL);
        }

        if (!FileServiceUtil.saveToProperties()) {
            modelAndView.addObject("showMsg", "menu.system.service.file.connect.fail.hint");
            FileServiceUtil.loadProperties();
            loadOriginConfig(modelAndView);
            return modelAndView;
        }

        return modelAndView;

    }

    private void loadOriginConfig(ModelAndView modelAndView) {
        modelAndView.addObject("flag", FileServiceUtil.isRemoteMode() ? "on" : "off");
        modelAndView.addObject("serverIp", FileServiceUtil.ip);
        modelAndView.addObject("serverPort", FileServiceUtil.port);
        modelAndView.addObject("localMaxDownloadNum", PerformanceConfig.getInstance().getIntegerConfig("fileDowload.maxConnections"));

        if (FileServiceUtil.isRemoteMode()) {
            try {
                Thresholds thretholds = getFileServiceThresholds(FileServiceUtil.getRequestUrl(RequestTypeEnum.GET_THRESHOLD));
                if (thretholds != null) {
                    int maxDownloadNum = thretholds.getDownloadNum();
                    modelAndView.addObject("maxDownloadNum", maxDownloadNum);
                } else {
                    modelAndView.addObject("showMsg", "menu.system.service.file.connect.fail.hint");
                    FileServiceUtil.switchToMode(ModeEnum.LOCAL);
                }
            } catch (IOException e) {
                modelAndView.addObject("showMsg", "menu.system.service.file.connect.fail.hint");
                FileServiceUtil.switchToMode(ModeEnum.LOCAL);
            }
        }
    }


    private static class FileFieldComparator implements Comparator<String>, java.io.Serializable {
        private static final long serialVersionUID = -1350845417478340152L;

        public int compare(String o1, String o2) {
            //对形如file1_1,file1_2,file2_1,file2_2的key进行排序
            try {
                String keyO1 = o1.substring(4);
                String keyO2 = o2.substring(4);
                String[] indexArrayO1 = keyO1.split("_");
                String[] indexArrayO2 = keyO2.split("_");
                int inputIndexO1 = Integer.valueOf(indexArrayO1[0]);
                int fileIndexO1 = Integer.valueOf(indexArrayO1[1]);
                int inputIndexO2 = Integer.valueOf(indexArrayO2[0]);
                int fileIndexO2 = Integer.valueOf(indexArrayO2[1]);
                if (inputIndexO1 != inputIndexO2) {
                    return inputIndexO1 - inputIndexO2;
                } else {
                    return fileIndexO1 - fileIndexO2;
                }
                //return Integer.valueOf(o1.substring(4)).compareTo(Integer.valueOf(o2.substring(4)));
            } catch (Exception e) {
                return o1.compareTo(o2);
            }
        }

    }

    /**
     * 获取文件微服务的配置信息
     * @param reqUrl    文件微服务获取阀值设定的接口URL地址
     * @return 阀值配置，若失败则为NULL
     * @throws IOException
     */
    private Thresholds getFileServiceThresholds(String reqUrl) throws IOException {


        FileServiceResponseBean resBean = null;

        HttpClientUtil httpClient = new HttpClientUtil(5000);

        try {
            httpClient.openGet(reqUrl);
            int status = httpClient.send();
            if (status == HttpStatus.SC_OK) {
                resBean = httpClient.getResponseJsonAsObject(FileServiceResponseBean.class);
            } else {
                return null;
            }
        } catch (IOException e) {
            throw e;
        } finally {
            httpClient.close();
        }


        return resBean != null ? resBean.getThresholds() : null;
    }

    private int setFileServiceThresholds(String reqUrl, Thresholds thresholds) throws IOException {


        FileServiceRequestBean reqBean = new FileServiceRequestBean();
        reqBean.setThresholds(thresholds);

        HttpClientUtil httpClient = new HttpClientUtil(5000);
        try {
            httpClient.openPost(reqUrl);
            httpClient.setRequestBodyJson(reqBean);
            int status = httpClient.send();
            if (status == HttpStatus.SC_OK) {
                FileServiceResponseBean resBean = httpClient.getResponseJsonAsObject(FileServiceResponseBean.class);
                if (resBean.getStatus() != StatusCode.SUCCESS) {
                    return resBean.getStatus();
                }
            } else {
                return status;
            }
        } catch (IOException e) {
            throw e;
        } finally {
            httpClient.close();
        }
        return StatusCode.SUCCESS;

    }

    private void addRemoteDownloadTicket(String reqUrl, String ticket, FileProperties fileProperties) throws IOException {

        FileServiceRequestBean reqBean = new FileServiceRequestBean();
        reqBean.setTicket(ticket);
        reqBean.setFileProperties(fileProperties);

        HttpClientUtil httpClient = new HttpClientUtil(5000);
        try {
            httpClient.openPost(reqUrl);
            httpClient.setRequestBodyJson(reqBean);
            int status = httpClient.send();
            if (status == HttpStatus.SC_OK) {
            }
        } catch (IOException e) {
            throw e;
        } finally {
            httpClient.close();
        }

    }

    private String generateRemoteDownloadUrl(HttpServletRequest request, String ticket) throws BusinessException {

        String downUrl = FileServiceUtil.getRequestUrl(RequestTypeEnum.FILE_DOWNLOAD);

        String comm = request.getParameter("comm");
        long fileId = Long.valueOf(request.getParameter("fileId"));

        Date createDate = null;
        String filename = null;
        if ("byFileId".equals(comm)) {
            V3XFile v3xfile = fileManager.getV3XFile(fileId);
            createDate = v3xfile.getCreateDate();
            filename = v3xfile.getFilename();
            try {
                filename = java.net.URLEncoder.encode(filename, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.error(e);
            }
        } else if ("mobile".equals(request.getParameter("from"))) {
            V3XFile v3xfile = fileManager.getV3XFile(fileId);
//            String contentType = v3xfile.getMimeType();
            createDate = v3xfile.getCreateDate();
            filename = FileUploadUtil.escapeFileName(v3xfile);
        } else {
            createDate = Datetimes.parseDate(request.getParameter("createDate"));

            filename = FileUtil.getDownloadFileName(request, request.getParameter("filename"));
        }

        downUrl += "/" + DateFormatUtils.format(createDate, Constants.DATE_TO_URL_STYLE) + "/" + fileId;

        FileProperties fileProperties = new FileProperties();
        fileProperties.setFileId(fileId);
        fileProperties.setFileName(filename);
        try {
            addRemoteDownloadTicket(FileServiceUtil.getRequestUrl(RequestTypeEnum.ADD_TICKET), ticket, fileProperties);
        } catch (IOException e) {
            return null;
        }

        downUrl += "?ticket=" + ticket;

        return downUrl;
    }


    private int updateFileServiceConfig(Thresholds thresholds) throws IOException {

        FileServiceRequestBean reqBean = new FileServiceRequestBean();
        reqBean.setThresholds(thresholds);

        List<FileServicePartition> partitions = new ArrayList<FileServicePartition>();
        List<Partition> partitionlist = partitionManager.getAllPartitions();
        for (Partition originPartition : partitionlist) {
            partitions.add(new FileServicePartition(originPartition));
        }
        reqBean.setPartitionList(partitions);

        HttpClientUtil httpClient = new HttpClientUtil(5000);
        try {
            httpClient.openPost(FileServiceUtil.getRequestUrl(RequestTypeEnum.SET_SERVER_CONFIG));
            httpClient.setRequestBodyJson(reqBean);
            int status = httpClient.send();
            if (status == HttpStatus.SC_OK) {
                FileServiceResponseBean resBean = httpClient.getResponseJsonAsObject(FileServiceResponseBean.class);
                if (resBean.getStatus() != StatusCode.SUCCESS) {
                    return resBean.getStatus();
                }
            } else {
                return status;
            }
        } catch (IOException e) {
            throw e;
        } finally {
            httpClient.close();
        }
        return StatusCode.SUCCESS;

    }

}