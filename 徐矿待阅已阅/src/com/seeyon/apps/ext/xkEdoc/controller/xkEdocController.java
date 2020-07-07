package com.seeyon.apps.ext.xkEdoc.controller;

import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.xkEdoc.manager.XkjtSummaryAttManager;
import com.seeyon.apps.ext.xkEdoc.manager.XkjtSummaryAttManagerImpl;
import com.seeyon.apps.ext.xkEdoc.po.AttachmentEx;
import com.seeyon.apps.xkjt.po.XkjtLeaderDaiYue;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManagerImpl;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.filemanager.manager.FileManagerImpl;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.manager.EdocManagerImpl;
import com.seeyon.v3x.webmail.util.FileUtil;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;

public class xkEdocController extends BaseController {

    private EdocManager edocManager = new EdocManagerImpl();
    private AttachmentManager attachmentManager = new AttachmentManagerImpl();
    private FileManager fileManager = new FileManagerImpl();
    private XkjtSummaryAttManager xkjtSummaryAttManager = new XkjtSummaryAttManagerImpl();

    public ModelAndView pdfView(HttpServletRequest request, HttpServletResponse response) {
        try {
            String content = request.getParameter("content");
            String time = request.getParameter("time");
            String isQuickSend = request.getParameter("isQuickSend");
            String summaryId = request.getParameter("summaryId");

            //获取系统路径
            String spath = fileManager.getFolder(new Date(), false);
            String[] arrs = time.split("\\-");
            String p = spath.substring(0, spath.indexOf("upload") + 6);
            String y = "";

            String hostFileUrl = "";
            if (null != isQuickSend && !"".equals(isQuickSend)) {
                if (("true").equals(isQuickSend)) {
                    List<Map> listMap = xkjtSummaryAttManager.queryHostFile(summaryId);
                    if (listMap.size() > 0) {
                        for (Map map : listMap) {
                            BigDecimal bigDecimal = (BigDecimal) map.get("attachment_id");
                            hostFileUrl = bigDecimal.toString();
                        }
                    }
                    y = p.concat(File.separator + arrs[0]).concat(File.separator + arrs[1]).concat(File.separator + arrs[2]) + File.separator + hostFileUrl;
                } else {
                    y = p.concat(File.separator + arrs[0]).concat(File.separator + arrs[1]).concat(File.separator + arrs[2]) + File.separator + content;
                }
            }

            File fy = new File(y);
            if (fy.exists()) {
                File newFile = new File(y.concat(".pdf"));
                if (!newFile.exists()) {
                    newFile.createNewFile();
                }
                FileUtil.copy(new File(y), newFile);
                //pdf文件在服务器上的完整路径

                if (!newFile.exists()) {
                    response.sendError(404, "获取Pdf文档出错了！");
                    return null;
                }
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(newFile));
                byte[] buf = new byte[1024];
                int len = 0;
                response.reset(); // 非常重要
                // 在线打开方式  
                URL u = new URL("file:///" + y.concat(".pdf"));
                response.setContentType(u.openConnection().getContentType());
                response.setHeader("Content-Disposition", "inline; filename=" + new String(content.getBytes("utf-8"), "iso-8859-1"));
                // 文件名应该编码成UTF-8  
                OutputStream out = response.getOutputStream();
                while ((len = bis.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                bis.close();
                out.close();
            } else {
                response.sendError(404, "获取Pdf文档出错了！");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ModelAndView xkViewEdoc(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ModelAndView mav = new ModelAndView("apps/ext/xkEdoc/xk_viewEdoc");
        String s_summaryId = request.getParameter("summaryId");
        long summaryId = 0L;
        if (s_summaryId != null && !s_summaryId.isEmpty()) {
            summaryId = Long.parseLong(s_summaryId);
        }
        //start
        String sql = "select filename from  CTP_file where id =(select attachment_id from XKJT_SUMMARY_ATTACHMENT where  summary_id ='" + s_summaryId + "')";
        Connection connection = JDBCAgent.getRawConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String suffix = "";
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String filename = rs.getString("filename");
                suffix = filename.substring(filename.indexOf("."));
                mav.addObject("suffix", suffix);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != rs) {
                rs.close();
            }
            if (null != ps) {
                ps.close();
            }
            if (null != connection) {
                connection.close();
            }
        }
        //end
        EdocSummary summary = edocManager.getEdocSummaryById(summaryId, true);
        User user = AppContext.getCurrentUser();
        long memberId = user.getId();
        List<XkjtLeaderDaiYue> daiYues = xkjtSummaryAttManager.queryDaiyueByEdocIdAndLeaderId(memberId, summaryId);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String createTime = simpleDateFormat.format(summary.getCreateTime());
        mav.addObject("summary", summary);
        mav.addObject("createTime", createTime);
        if (daiYues.size() > 0) {
            Long daiYueId = daiYues.get(0).getId();
            mav.addObject("daiYueId", daiYueId);
        } else {
            mav.addObject("daiYueId", "");
        }

        Set<EdocBody> edocBodies = summary.getEdocBodies();
        Iterator<EdocBody> it = edocBodies.iterator();
        EdocBody edocBody = null;
        while (it.hasNext()) {
            edocBody = it.next();
            if (edocBody.getContentType().equals("Pdf")) {
                mav.addObject("content", edocBody.getContent());
            }
        }
        return mav;
    }

    public ModelAndView getFileList(HttpServletRequest request, HttpServletResponse response) {
        String summaryId = request.getParameter("summaryId");

        try {
            // 获取相关的附件列表
            List<Attachment> attachmentVOs = attachmentManager.getByReference(Long.parseLong(summaryId));

            String hostFileUrl = "";
            List<Map> listMap = xkjtSummaryAttManager.queryHostFile(summaryId);
            if (null != listMap && listMap.size() > 0) {
                for (Map map : listMap) {
                    BigDecimal bigDecimal = (BigDecimal) map.get("attachment_id");
                    hostFileUrl = bigDecimal.toString();
                }
            }
            List<AttachmentEx> list = new ArrayList<>();
            for (Attachment attachment : attachmentVOs) {
                String fileUrl = Long.toString(attachment.getFileUrl());
                if (!hostFileUrl.equals(fileUrl)) {
                    AttachmentEx attachmentEx = new AttachmentEx();
                    attachmentEx.setFilepath(Long.toString(attachment.getFileUrl()));
                    attachmentEx.setCreatedate(attachment.getCreatedate());
                    attachmentEx.setFileUrl(attachment.getFileUrl());
                    attachmentEx.setFilename(attachment.getFilename());
                    attachmentEx.setSize(attachment.getSize());
                    //用于区分是不是关联文档
                    attachmentEx.setType(attachment.getType());
                    list.add(attachmentEx);
                }
            }
            Map<String, Object> map = new HashMap<>();
            map.put("code", 0);
            map.put("message", "");
            map.put("total", list.size());
            map.put("data", list);
            JSONObject json = new JSONObject(map);
            render(response, json.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }


    /**
     * 发文单上获取附件列表
     */
    public ModelAndView sendFileList(HttpServletRequest request, HttpServletResponse response) {
        String summaryId = request.getParameter("summaryId");
        String isQuickSend = request.getParameter("isQuickSend");

        try {
            EdocSummary edocSummary = edocManager.getEdocSummaryById(Long.parseLong(summaryId), true);
            List<Map<String, Object>> bodyList = xkjtSummaryAttManager.queryEdocBody(summaryId);
            // 获取相关的附件列表
            List<Attachment> attachmentVOs = attachmentManager.getByReference(Long.parseLong(summaryId));
            //其他附件
            List<AttachmentEx> list = new ArrayList<>();
            //主附件
            List<AttachmentEx> mainList = new ArrayList<>();
//            String subject = (edocSummary.getSubject() + ".pdf").replace(" ", "");

            String hostFileUrl = "";
            if (null != isQuickSend && !"".equals(isQuickSend)) {
//                if (("true").equals(isQuickSend)) {
                List<Map> listMap = xkjtSummaryAttManager.queryHostFile(summaryId);
                if (listMap.size() > 0) {
                    for (Map map : listMap) {
                        BigDecimal bigDecimal = (BigDecimal) map.get("attachment_id");
                        hostFileUrl = bigDecimal.toString();
                    }
                }
                for (Attachment attachment : attachmentVOs) {
                    String fileUrl = Long.toString(attachment.getFileUrl());
                    AttachmentEx attachmentEx = new AttachmentEx();
                    attachmentEx.setFilepath(Long.toString(attachment.getFileUrl()));
                    attachmentEx.setCreatedate(attachment.getCreatedate());
                    attachmentEx.setFileUrl(attachment.getFileUrl());
                    attachmentEx.setFilename(attachment.getFilename());
                    attachmentEx.setSize(attachment.getSize());
//                    用于区分是不是关联文档
                    attachmentEx.setType(attachment.getType());
                    attachmentEx.setCategory(attachment.getCategory());
                    if (!hostFileUrl.equals(fileUrl)) {
                        list.add(attachmentEx);
                    } else {
                        mainList.add(attachmentEx);
                    }
                }
//                } else {
//
//                    for (Attachment attachment : attachmentVOs) {
//                        String fileUrl = Long.toString(attachment.getFileUrl());
//                        AttachmentEx attachmentEx = new AttachmentEx();
//                        attachmentEx.setFilepath(Long.toString(attachment.getFileUrl()));
//                        attachmentEx.setCreatedate(attachment.getCreatedate());
//                        attachmentEx.setFileUrl(attachment.getFileUrl());
//                        attachmentEx.setFilename(attachment.getFilename());
//                        attachmentEx.setSize(attachment.getSize());
//                        list.add(attachmentEx);
//                    }
//                    String fileName = edocSummary.getSubject() + ".pdf";
//                    Set<EdocBody> edocBodies = edocSummary.getEdocBodies();
//                    Iterator<EdocBody> it = edocBodies.iterator();
//                    EdocBody edocBody = null;
//                    while (it.hasNext()) {
//                        edocBody = it.next();
//                        if (edocBody.getContentType().equals("Pdf")) {
//                            AttachmentEx attachmentEx = new AttachmentEx();
//                            attachmentEx.setFilepath(edocBody.getContent());
//                            attachmentEx.setCreatedate(edocBody.getCreateTime());
//                            attachmentEx.setFileUrl(Long.parseLong(edocBody.getContent()));
//                            attachmentEx.setFilename(fileName);
//                            attachmentEx.setSize(0l);
//                            mainList.add(attachmentEx);
//                        }
//                    }
//                }
            }

            Map<String, Object> map = new HashMap<>();
            map.put("code", 0);
            map.put("message", "");
            map.put("total", list.size());
            map.put("data", list);
            map.put("main", mainList);
            JSONObject json = new JSONObject(map);
            render(response, json.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }

    public List<AttachmentEx> handlingList(List<Attachment> attachments) {

        return null;

    }

    /**
     * 用于判断关联文档是文单 还是其他附件
     * @param request
     * @param response
     * @return
     * @throws BusinessException
     */
    public ModelAndView toAnalyzeFileIsOpenOrUpload(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        //获取系统路径
        String filename = request.getParameter("filename");
        String fileId = request.getParameter("fileId");
        String createDate = request.getParameter("createDate");
        String summaryId = request.getParameter("summaryId");
        String y = "";
        try {
            String spath = fileManager.getFolder(new Date(), false);
            String[] arrs = createDate.split("\\-");
            String p = spath.substring(0, spath.indexOf("upload") + 6);
            y = p.concat(File.separator + arrs[0]).concat(File.separator + arrs[1]).concat(File.separator + arrs[2]) + File.separator + fileId;
            File file = new File(y);
            Map<String, Object> map = new HashMap<>();
            if (file.exists()) {
                map.put("isExist", "true");
            } else {
                map.put("isExist", "false");
            }
            JSONObject json = new JSONObject(map);
            render(response, json.toJSONString());
        } catch (BusinessException bus) {
            bus.printStackTrace();
            logger.error(bus);
        }

        return null;
    }

    public ModelAndView downloadfile(HttpServletRequest request, HttpServletResponse response) {

        String filename = request.getParameter("filename");
        try {
            filename = specialWord(URLDecoder.decode(filename, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String fileId = request.getParameter("fileId");
        String createDate = request.getParameter("createDate");
        String type = request.getParameter("type");
        String isQuickSend = request.getParameter("isQuickSend");
        String summaryId = request.getParameter("summaryId");
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        String n = "";
        try {
            //获取系统路径
            String spath = fileManager.getFolder(new Date(), false);
            String[] arrs = createDate.split("\\-");
            String p = spath.substring(0, spath.indexOf("upload") + 6);
            String y = "";
            String hostFileUrl = "";
            if (type.equals("2")) {
                List<Map> listMap = xkjtSummaryAttManager.queryHostFile(summaryId);
                if (listMap.size() > 0) {
                    for (Map map : listMap) {
                        BigDecimal bigDecimal = (BigDecimal) map.get("attachment_id");
                        hostFileUrl = bigDecimal.toString();
                    }
                }
                if (hostFileUrl.equals("")) {
                    y = p.concat(File.separator + arrs[0]).concat(File.separator + arrs[1]).concat(File.separator + arrs[2]) + File.separator + fileId;

                } else {
                    y = p.concat(File.separator + arrs[0]).concat(File.separator + arrs[1]).concat(File.separator + arrs[2]) + File.separator + hostFileUrl;
                }
            } else if (type.equals("1")) {
                y = p.concat(File.separator + arrs[0]).concat(File.separator + arrs[1]).concat(File.separator + arrs[2]) + File.separator + fileId;

            }

            n = p.concat(File.separator + arrs[0]).concat(File.separator + arrs[1]).concat(File.separator + arrs[2]) + File.separator + filename;
            File newFile = new File(n);
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            FileUtil.copy(new File(y), new File(n));
            File file = new File(n);
            bis = new BufferedInputStream(new FileInputStream(file));
            bos = new BufferedOutputStream(response.getOutputStream());
            // 设置response内容的类型
//            response.setCharacterEncoding("UTF-8");
//            response.setContentType(new MimetypesFileTypeMap().getContentType(file));
            response.setContentType("application/octet-stream;charset=utf-8");
            // 设置头部信息
            response.setHeader("Content-disposition", "attachment;filename=" + new String(filename.getBytes("GBK"), "iso-8859-1"));
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = bis.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }
            bos.flush();
        } catch (
                Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
                File file = new File(n);
                file.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    public String specialWord(String info) {
        String[] fbsArr = {"\t", "\n", "\b"};
        for (String key : fbsArr) {
            if (info.contains(key)) {
                info = info.replace(key, "");
            }
        }
        return info;
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

    public XkjtSummaryAttManager getXkjtSummaryAttManager() {
        return xkjtSummaryAttManager;
    }

    public void setXkjtSummaryAttManager(XkjtSummaryAttManager xkjtSummaryAttManager) {
        this.xkjtSummaryAttManager = xkjtSummaryAttManager;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }


    public void setEdocManager(EdocManager edocManager) {
        this.edocManager = edocManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }
}
