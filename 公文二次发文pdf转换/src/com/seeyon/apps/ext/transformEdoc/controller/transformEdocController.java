package com.seeyon.apps.ext.transformEdoc.controller;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.webmail.util.FileUtil;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.xfire.client.Client;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.web.servlet.ModelAndView;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class transformEdocController extends BaseController {

    private EdocManager edocManager = (EdocManager) AppContext.getBean("edocManager");
    private FileManager fileManager = (FileManager) AppContext.getBean("fileManager");


    public ModelAndView toTransformPdf(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String summaryId = request.getParameter("summaryId");
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        EdocSummary edocSummary = edocManager.getEdocSummaryById(Long.parseLong(summaryId), true);
        EdocBody edocBody = edocSummary.getFirstBody();
        try {
            int flag = transitionPdf(edocSummary, edocBody, year, month, day);
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

    public int transitionPdf(EdocSummary edocSummary, EdocBody body, String year, String month, String day) {
        int flag = -1;
        try {
            //获取系统路径
            String spath = fileManager.getFolder(new Date(), false);
            spath = spath.substring(0, spath.indexOf("upload") + 6) + File.separator;
            Date timestamp = body.getCreateTime();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(timestamp);
//            String year = Integer.toString(calendar.get(Calendar.YEAR));
//            String month = calendar.get(Calendar.MONTH) + 1 > 9 ? Integer.toString(calendar.get(Calendar.MONTH) + 1) : "0" + (calendar.get(Calendar.MONTH) + 1);
//            String day = calendar.get(Calendar.DAY_OF_MONTH) > 9 ? Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)) : "0" + calendar.get(Calendar.DAY_OF_MONTH);
//            String year=requ
//            获取正文文件所在的路径
            spath = spath.concat(year + File.separator).concat(month + File.separator).concat(day);
            String sBodyPath = (spath + File.separator + body.getContent() + ".doc").replaceAll("\\\\", "/");
            // copy 一份正文的doc文件
            FileUtil.copy(new File((spath + File.separator + body.getContent()).replaceAll("\\\\", "/")), new File(sBodyPath));

            //获取正文文件路径
            File bodyFile = new File(sBodyPath);
            //ftp://root:xkjt1234@10.100.1.76:21/2007/word.docx
            String zwp = sBodyPath.substring(sBodyPath.indexOf("upload") + 6).replaceAll("\\\\", "/");
            String zhengwenP = "z:" + zwp;
            String formId = Long.toString(edocSummary.getFormId()).trim();
            String transform = "";
            if (null != formId && !"".equals(formId)) {
                //合并正文
                if (formId.equals("-7139423850050401892") || formId.equals("1542089478047025160")) {
                    transform = zhengwenP;
                }
            }
            //类根路径  F:\Seeyon\A8\ApacheJetspeed\webapps\seeyon\WEB-INF\classes
            String classPath = this.getClass().getResource("/").getPath();
            String mergeSavePath = (classPath.substring(0, classPath.indexOf("WEB-INF"))).concat("pdf") + File.separator + edocSummary.getId() + File.separator + "tmp" + File.separator;
            File f = new File(mergeSavePath);
            if (!f.exists()) {
                f.mkdirs();
            }

            flag = mergeFormAndBody(edocSummary, "http://10.11.100.41:8088/convert/webservice/ConvertService?wsdl", transform, mergeSavePath);
            bodyFile.delete();
            if (flag != -1) {
                String pathPdf = "smb://root:xkjt2019,@10.11.100.33/pdf" + File.separator + edocSummary.getId() + File.separator + "tmp" + File.separator;
                SmbFile smbFile = new SmbFile(pathPdf);
                String filename = "";
                if (smbFile.exists()) {
                    SmbFile[] smbFiles = smbFile.listFiles();
                    filename = smbFiles[0].getName();
                }

                //把oa中对应的公文单正文合并文件上传pdf共享目录
                String uploadPath = "smb://root:xkjt2019,@10.11.100.33/pdf" + File.separator + edocSummary.getId() + File.separator;
                String oaPdfOfPath = (classPath.substring(0, classPath.indexOf("WEB-INF"))).concat("pdf") + File.separator + edocSummary.getId() + File.separator + edocSummary.getId() + ".pdf";
                smbPost(uploadPath, oaPdfOfPath);

                //pdf插入操作
                insertPdf(edocSummary, "http://10.11.100.41:8088/convert/webservice/ConvertService?wsdl", filename, mergeSavePath);
                //从共享文件夹中下载转换好的pdf文件
                String twoPdfPath = "smb://root:xkjt2019,@10.11.100.33/pdf" + File.separator + edocSummary.getId() + File.separator + "two" + File.separator;
                SmbFile twosmbFile = new SmbFile(twoPdfPath);
                String twofilename = "";
                if (twosmbFile.exists()) {
                    SmbFile[] twosmbFiles = twosmbFile.listFiles();
                    twofilename = twosmbFiles[0].getName();
                }
                String twooaPdfOfPath = (classPath.substring(0, classPath.indexOf("WEB-INF"))).concat("pdf") + File.separator + edocSummary.getId() + File.separator;

                String remoteRootUrl = "smb://root:xkjt2019,@10.11.100.33/pdf" + File.separator + edocSummary.getId() + File.separator;
                smbGet(twoPdfPath + twofilename, twooaPdfOfPath.concat(twofilename), remoteRootUrl);
                //从共享文件夹下载文件成功后，开始删除原有pdf文件
                File dfile = new File(twooaPdfOfPath.concat(edocSummary.getId() + ".pdf"));
                dfile.delete();
                returnFileName(twooaPdfOfPath, "2", dfile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // 0:代表转换成功，-1：转换失败  -2：表示公文单模板不存在
        return flag;
    }

    //向共享目录上传文件
    public void smbPost(String remoteUrl, String pdfOfPdfDir) {
        InputStream in = null;
        OutputStream out = null;
        File localFile = new File(pdfOfPdfDir);
        try {
            String filename = localFile.getName();
            SmbFile remoteFile = new SmbFile(remoteUrl + File.separator + filename);
            in = new BufferedInputStream(new FileInputStream(localFile));
            out = new BufferedOutputStream(new SmbFileOutputStream(remoteFile));
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //从共享目录下载文件
    public void smbGet(String remoteUrl, String pdfOfPdfDir, String remoteRootUrl) {
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

        }
        try {
            SmbFile file = new SmbFile(remoteRootUrl);
            SmbFile[] list = file.listFiles();
            foreachList(list);
        } catch (Exception smb) {
            logger.error("删除共享文件出错：" + smb.getMessage());
        }

    }

    /**
     * 递归删除文件
     *
     * @param list
     */
    public void foreachList(SmbFile[] list) {

        try {
            for (int i = 0; i < list.length; i++) {
                if (list[i].isDirectory()) {
                    foreachList(list[i].listFiles());
                } else {
                    list[i].delete();
                }
            }

//            for (int j = 0; j < list.length; j++) {
//                list[j].delete();
//            }
        } catch (SmbException e) {
            logger.error("删除共享文件出错：" + e.getMessage());
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
     * 开始转换
     */
    public int mergeFormAndBody(EdocSummary edocSummary, String url, String mergepath, String savepath) {

        int cbCode = -1;
        Client client;
        String ftpdownload = "y:" + savepath.substring(savepath.indexOf("pdf") + 3).replaceAll("\\\\", "/");
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
     * pdf插入操作
     */
    public int insertPdf(EdocSummary edocSummary, String url, String filename, String savepath) {

        int cbCode = -1;
        Client client;
        try {
            URL testUrl = new URL(url);
            HttpURLConnection huc = (HttpURLConnection) testUrl.openConnection();
            huc.setUseCaches(false);
            huc.setConnectTimeout(3000);
            int status = huc.getResponseCode();
            //目标文件路径
            String strDestPDFFile = "y:" + File.separator + edocSummary.getId() + File.separator + edocSummary.getId() + ".pdf";
            //源文件路径
            String strSrcPDFFile = "y:" + File.separator + edocSummary.getId() + File.separator + "tmp" + File.separator + filename;

            String p = savepath.substring(0, savepath.indexOf("tmp")) + "two" + File.separator;
            File f = new File(p);
            if (!f.exists()) {
                f.mkdirs();
            }
            //转换储存目录
            String strDestFolder = "y:" + p.substring(p.indexOf("pdf") + 3).replaceAll("\\\\", "/");
            if (200 == status) {
                client = new Client(new URL(url));
                Object[] result = client.invoke("InsertPageToPDF",
                        new Object[]{strDestPDFFile, strSrcPDFFile, 2, 1, -1, strDestFolder, 5, "", "pdf"});
                Document document = DocumentHelper.parseText((String) result[0]);
                Element rootElt = document.getRootElement();
                String ts = rootElt.elementText("result");
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


}
