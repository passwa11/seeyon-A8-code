package com.seeyon.apps.ext.downloadDetail.util;

import com.sun.jna.Platform;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.util.FileItemHeadersImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;


public class HtmlToPDFUtils {
    private static Logger logger = LoggerFactory.getLogger(HtmlToPDFUtils.class);
    private static File wkHomeDir = Platform.isLinux() ? new File("/wkHome") : new File("C:\\wkHome");
    private static File wkTmpDir = Platform.isLinux() ? new File("/wkTemp") : new File("C:\\wkTemp");
    private static File simsunFontDir = Platform.isLinux() ? new File("/usr/share/fonts/chinese/TrueType") : new File("C:\\Windows\\Fonts");
    private static File wktool;
    private static File simsunFont;

    private static boolean canUse = true;
    private static boolean able = true;


    static {
        logger.info("Tools are only  available for Windows 64 and Linux 64 platforms !!!");
        forceInit();
    }

    public static boolean forceInit() {
        Long initc = 0L;
        if (!wkHomeDir.exists()) {
            able = wkHomeDir.mkdirs();
        }
        logger.info("{},check wkHomeDir ,result:{}", ++initc, able);
        if (!wkTmpDir.exists()) {
            able = wkTmpDir.mkdirs();
        }
        logger.info("{},check wkTmpDir ,result:{}", ++initc, able);

        if (!simsunFontDir.exists()) {
            able = simsunFontDir.mkdirs();
        }
        logger.info("{},check simsunFontDir ,result:{}", ++initc, able);

        InputStream wkhtmltoxAsStream = null;
        InputStream simsunAsStream = null;
        if (able) {
            wkhtmltoxAsStream = Platform.isLinux() ? HtmlToPDFUtils.class.getResourceAsStream("/wkhtmltox") : HtmlToPDFUtils.class.getResourceAsStream("/wkhtmltopdf.exe");
            simsunAsStream = HtmlToPDFUtils.class.getResourceAsStream("/simfang.ttf");
        }
        if (null == wkhtmltoxAsStream || simsunAsStream == null) {
            logger.error("{},load wkhtmltoxAsStream :{},load simsunAsStream:{}", ++initc, null == wkhtmltoxAsStream, simsunAsStream == null);
            able = false;
        }
        logger.info("{},load wktool and font source ,result:{}", ++initc, able);

        if (able) {
            File font = new File(simsunFontDir, "simfang.ttf");
            File wk = new File(wkHomeDir, Platform.isLinux() ? "wkhtmltox" : "wkhtmltopdf.exe");

            try {
                if (!font.exists()) {
                    able = 1 < Files.copy(simsunAsStream, font.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                logger.info("{},copy font source to {},result:{}", ++initc, font.toPath(), able);
                if (!wk.exists()) {
                    able = 1 < Files.copy(wkhtmltoxAsStream, wk.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                logger.info("{},copy wktools source to {},result:{}", ++initc, font.toPath(), able);

                if (able) {
                    wktool = wk;
                    simsunFont = font;
                }
            } catch (IOException e) {
                e.printStackTrace();
                able = false;
                logger.error("{}, error when copy source : {} ", ++initc, e.getMessage());
            }
        }

        if (able) {
            if (Platform.isLinux()) {
                boolean canExe = exePermissionCheck();
                logger.info("{},check run permission,result: {}  ", ++initc, canExe ? "has permission" : "no permission");
                if (!canExe) {
                    simpleExecComand("chmod +x " + wktool.getPath());
                    if (!exePermissionCheck()) {
                        logger.error("{},add permission failed", ++initc);
                        able = false;
                    }
                }
            }
        }
        if (able) {
            able = cleanTempDir();
        }
        if (able) {
            logger.info("{},init success!", ++initc);
        } else {
            logger.info("{},init failed!", ++initc);
            canUse = false;
        }
        return able;
    }

    public String getSimsunPath() {
        logger.info("world path:" + simsunFont.getPath());
        return simsunFont.getPath();
    }

    public static HtmlToPDFUtils build() {
        return new HtmlToPDFUtils();
    }

    private static boolean exePermissionCheck() {
        String permissionLog = simpleExecComand("ls -l " + wktool.getPath());
        return null != permissionLog && permissionLog.length() >= 10 && 120 == permissionLog.charAt(9);
    }

    private static boolean cleanTempDir() {
        if (wkTmpDir.exists()) {
            canUse = deleteFiles(wkTmpDir) ? wkTmpDir.mkdirs() : canUse;
            logger.info("cleanTempDir,result:{} ", canUse);
        } else {
            canUse = wkTmpDir.mkdirs();
        }
        return canUse;
    }

    public synchronized FileItem convertPDFFromText(String text, String name) {
        cleanTempDir();
        if (!canUse) {
            logger.info("tools crash,can invoke forceInit() method see reason !!!");
            return null;
        }
        File html = new File(wkTmpDir, name + ".html");
        File pdf = new File(wkTmpDir, name + ".pdf");

        try (FileWriter fileWriter = new FileWriter(html)) {
            fileWriter.write(text);
        } catch (IOException e) {
            logger.error("create html file failed");
            e.printStackTrace();
            return null;
        }
//        try (FileOutputStream fos=new FileOutputStream(html)){
//            fos.write(text.getBytes("UTF-8"));
//        }catch (IOException e) {
//            logger.error("create html file failed");
//            e.printStackTrace();
//            return null;
//        }
        if (html.exists() && html.isFile()) {
            logger.info("exec html to  pdf ，wktoolPath=>{}", wktool.getPath());
            simpleExecComand(wktool.getPath() + " " + html.getPath() + " " + pdf.getPath());
        }
        if (pdf.exists() && pdf.isFile()) {
            try (FileInputStream fileInputStream = new FileInputStream(pdf); ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[2014];
                while (fileInputStream.read(buffer) != -1) {
                    byteArrayOutputStream.write(buffer);
                }
                byteArrayOutputStream.flush();
                byte[] data = byteArrayOutputStream.toByteArray();
                if (data.length > 0) {
                    logger.info("html to  pdf  success ");
                    SimplePDFFileItem file = new SimplePDFFileItem(pdf, data, Files.probeContentType(pdf.toPath()), "file");
                    logger.info("pdf size :{}", file.getSize());
                    return file;
                }
            } catch (IOException e) {
                logger.error("html to  pdf  failed");
                e.printStackTrace();
                return null;
            }
        }
        logger.info("html to  pdf  failed,no data");
        return null;
    }

    private static boolean deleteFiles(File file) {
        if (!file.exists()) {
            logger.info("del the file:{},is not exists", file.getPath());
            return false;
        }
        if (file.isFile()) {
            return file.delete();
        }
        File[] subFiles = file.listFiles();
        if (null != subFiles && subFiles.length > 0) {
            Arrays.asList(subFiles).forEach(HtmlToPDFUtils::deleteFiles);
        }

        return file.delete();
    }

    private static String simpleExecComand(String cmd) {
        try {
            String[] linux = {"/bin/sh", "-c", cmd};
            String[] windows = {"cmd", "/c", cmd};
            String[] cmdA = Platform.isLinux() ? linux : windows;
            Process process = Runtime.getRuntime().exec(cmdA);
            LineNumberReader br = new LineNumberReader(new InputStreamReader(process.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                logger.info(line);
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static class SimplePDFFileItem implements FileItem {
        private static final long serialVersionUID = 2237570099615271025L;
        public static final String DEFAULT_CHARSET = "ISO-8859-1";
        private String fieldName;
        private String fileName;
        private boolean isFormField;
        private byte[] cachedContent;
        private String contentType;
        private File dfosFile;
        private FileItemHeaders headers;

        public SimplePDFFileItem(File dfosFile, byte[] cachedContent, String contentType, String fieldName) {
            this.fieldName = fieldName;
            this.fileName = dfosFile.getName();
            this.isFormField = false;
            this.cachedContent = null == cachedContent || cachedContent.length < 1 ? new byte[0] : cachedContent;
            this.contentType = contentType;
            this.dfosFile = dfosFile;
            this.headers = new FileItemHeadersImpl();
        }

        public SimplePDFFileItem(String fieldName, String fileName, boolean isFormField, byte[] cachedContent, String contentType, File dfosFile, FileItemHeaders headers) {
            this.fieldName = fieldName;
            this.fileName = fileName;
            this.isFormField = isFormField;
            this.cachedContent = cachedContent;
            this.contentType = contentType;
            this.dfosFile = dfosFile;
            this.headers = headers;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (null == this.dfosFile) {
                return new ByteArrayInputStream(this.cachedContent);
            }
            return new FileInputStream(dfosFile);
        }

        @Override
        public String getContentType() {
            return this.contentType;
        }

        @Override
        public String getName() {
            return this.fileName;
        }

        @Override
        public boolean isInMemory() {
            return this.cachedContent.length > 0;
        }

        @Override
        public long getSize() {
            return this.cachedContent.length;
        }

        @Override
        public byte[] get() {
            return this.cachedContent;
        }

        @Override
        public String getString(String s) throws UnsupportedEncodingException {
            return getString();
        }

        @Override
        public String getString() {
            return new String(cachedContent, StandardCharsets.UTF_8);
        }

        @Override
        public void write(File file) throws Exception {
            Files.write(file.toPath(), cachedContent, StandardOpenOption.CREATE);
        }

        @Override
        public void delete() {
            boolean delete = dfosFile.delete();
        }

        @Override
        public String getFieldName() {
            return this.fieldName;
        }

        @Override
        public void setFieldName(String s) {
            this.fieldName = s;
        }

        @Override
        public boolean isFormField() {
            return this.isFormField;
        }

        @Override
        public void setFormField(boolean b) {
            this.isFormField = b;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            if (null == this.dfosFile) {
                return new ByteArrayOutputStream(1024);
            }
            return new FileOutputStream(this.dfosFile);
        }

        @Override
        public FileItemHeaders getHeaders() {
            return this.headers;
        }

        @Override
        public void setHeaders(FileItemHeaders fileItemHeaders) {
            this.headers = fileItemHeaders;
        }

        @Override
        public String toString() {
            return "SimplePDFFileItem{" + "fieldName='" + fieldName + '\'' + ", fileName='" + fileName + '\'' + ", isFormField=" + isFormField + ", cachedContent=[size]" + cachedContent.length + ", contentType='" + contentType + '\'' + ", dfosFile=" + dfosFile + '}';
        }
    }


}





