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
                File file = new File(mergeSavePath);
                String filename = "";
                if (file.exists()) {
                    File[] files = file.listFiles();
                    filename = files[0].getName();
                }

                //pdf插入操作
                insertPdf(edocSummary, "http://10.11.100.41:8088/convert/webservice/ConvertService?wsdl", filename, mergeSavePath);

                String twooaPdfOfPath = (classPath.substring(0, classPath.indexOf("WEB-INF"))).concat("pdf") + File.separator + edocSummary.getId() + File.separator;

                File dfile = new File(twooaPdfOfPath.concat(edocSummary.getId() + ".pdf"));
                returnFileName(twooaPdfOfPath, "2", dfile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // 0:代表转换成功，-1：转换失败  -2：表示公文单模板不存在
        return flag;
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

            String p = savepath.substring(0, savepath.indexOf("tmp")) ;

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
            File deleteZwFile = new File(savepath + filename);
            deleteZwFile.delete();
            File deleteFistPdf = new File(savepath.substring(0, savepath.indexOf("tmp")) + edocSummary.getId() + ".pdf");
            deleteFistPdf.delete();
        } catch (Exception e) {
            logger.error(e.getMessage() + "，转换服务出问题了 ~v~ 请联系管理员！");
        }
        return cbCode;
    }

    /**
     * 递归删除文件
     *
     * @param list
     */
    public void foreachList(File[] list) {

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
        } catch (Exception e) {
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
            huc.setDoOutput(true);
            huc.setUseCaches(false);
            huc.setConnectTimeout(20*1000);
            huc.setReadTimeout(20*1000);
            huc.connect();
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


}
