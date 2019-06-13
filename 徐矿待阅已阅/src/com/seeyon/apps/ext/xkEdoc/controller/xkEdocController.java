package com.seeyon.apps.ext.xkEdoc.controller;

import java.util.Date;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.xkEdoc.manager.XkjtSummaryAttManager;
import com.seeyon.apps.ext.xkEdoc.manager.XkjtSummaryAttManagerImpl;
import com.seeyon.apps.ext.xkEdoc.po.AttachmentEx;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManagerImpl;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.filemanager.manager.FileManagerImpl;
import com.seeyon.ctp.common.po.filemanager.Attachment;
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
        EdocSummary summary = edocManager.getEdocSummaryById(summaryId, true);
        mav.addObject("summary", summary);

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
                    list.add(attachmentEx);
                }
            }
            System.out.println(attachmentVOs.size());
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

        try {
            EdocSummary edocSummary = edocManager.getEdocSummaryById(Long.parseLong(summaryId), true);
            List<Map<String, Object>> bodyList = xkjtSummaryAttManager.queryEdocBody(summaryId);
            // 获取相关的附件列表
            List<Attachment> attachmentVOs = attachmentManager.getByReference(Long.parseLong(summaryId));

            List<AttachmentEx> list = new ArrayList<>();
            String subject = (edocSummary.getSubject() + ".pdf").replace(" ","");
            for (int i = 0; i < bodyList.size(); i++) {
                String contentType = (String) bodyList.get(i).get("content_type");
                if (contentType.equals("Pdf")) {
                    AttachmentEx attachmentEx = new AttachmentEx();
                    attachmentEx.setFilepath(((String) bodyList.get(i).get("content")));
                    attachmentEx.setCreatedate((Date) bodyList.get(i).get("create_time"));
                    attachmentEx.setFileUrl(Long.parseLong((String) bodyList.get(i).get("content")));
                    attachmentEx.setFilename(subject);
                    attachmentEx.setSize(0l);
                    list.add(attachmentEx);
                }

            }

            for (Attachment attachment : attachmentVOs) {
                AttachmentEx attachmentEx = new AttachmentEx();
                attachmentEx.setFilepath(Long.toString(attachment.getFileUrl()));
                attachmentEx.setCreatedate(attachment.getCreatedate());
                attachmentEx.setFileUrl(attachment.getFileUrl());
                attachmentEx.setFilename(attachment.getFilename());
                attachmentEx.setSize(attachment.getSize());
                list.add(attachmentEx);
            }
            System.out.println(attachmentVOs.size());
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


    public ModelAndView downloadfile(HttpServletRequest request, HttpServletResponse response) {
        String filename = request.getParameter("filename");
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
                if (null != isQuickSend && !"".equals(isQuickSend)) {
                    if (("true").equals(isQuickSend)) {
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
                    } else {
                        y = p.concat(File.separator + arrs[0]).concat(File.separator + arrs[1]).concat(File.separator + arrs[2]) + File.separator + fileId;
                    }

                }
            } else {
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
            System.out.println(spath);
        } catch (Exception e) {
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
