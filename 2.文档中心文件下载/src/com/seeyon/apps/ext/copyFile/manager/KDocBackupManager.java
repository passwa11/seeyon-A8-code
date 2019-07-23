package com.seeyon.apps.ext.copyFile.manager;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.seeyon.apps.ext.copyFile.util.ConfigProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.doc.manager.DocHierarchyManager;
import com.seeyon.apps.ext.copyFile.dao.DocEntityDao;
import com.seeyon.apps.ext.copyFile.pojo.DocEntity;
import com.seeyon.apps.ext.copyFile.util.OAFileUtil;
import com.seeyon.apps.ext.copyFile.util.UtilString;
import com.seeyon.client.CTPRestClient;
import com.seeyon.client.CTPServiceClientManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.v3x.edoc.exception.EdocException;
import com.seeyon.v3x.edoc.manager.EdocSummaryManager;


/**
 * 文档备份管理
 */
public class KDocBackupManager {
    private static final Log log = LogFactory.getLog(KDocBackupManager.class);

    private FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
    private long docLibID;
    private String[] folderIDs;
    private Map<String, String> pathMap;
    private List<DocEntity> fileList;
    private String root = ConfigProperties.getDownloadFileToSavePath();
    private static OAFileUtil oAFileUtil;
    private DocHierarchyManager docHierarchyManager;
    private static EdocSummaryManager edocSummaryManager = (EdocSummaryManager) AppContext
            .getBean("edocSummaryManager");
    private static AttachmentManager attachmentManager = (AttachmentManager) AppContext.getBean("attachmentManager");

    public static OAFileUtil getoAFileUtil() {
        return oAFileUtil;
    }

    public static void setoAFileUtil(OAFileUtil oAFileUtil) {
        KDocBackupManager.oAFileUtil = oAFileUtil;
    }

    public EdocSummaryManager getEdocSummaryManager() {
        return edocSummaryManager;
    }

    public void setEdocSummaryManager(EdocSummaryManager edocSummaryManager) {
        this.edocSummaryManager = edocSummaryManager;
    }

    public KDocBackupManager(long docLibID_, String[] folderIDs_) {
        this.docLibID = docLibID_;
        this.folderIDs = folderIDs_;
    }

    public FileManager getFileManager() {
        return this.fileManager;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    /**
     * 下载方法
     */
    public void download() throws Exception {
        DocEntityDao dao = new DocEntityDao();
        this.pathMap = dao.loadPath(this.docLibID);
        this.fileList = dao.load(this.docLibID, this.folderIDs);
        List tmpDocList = this.fileList;
        int size = (tmpDocList == null) ? 0 : tmpDocList.size();
        for (int i = 0; i < size; ++i) {
            download((DocEntity) tmpDocList.get(i));
        }
    }

    private void download(DocEntity theDoc) throws Exception {
        String tmpPath = getFile(theDoc.getLogical_path());
        // 做去特殊字符处理
        if (null != theDoc.getFr_name() && !"".equals(theDoc.getFr_name())) {
            String newName = theDoc.getFr_name().trim().replaceAll("\\pP|\\pS", "").replaceAll(" ", "");
            tmpPath = tmpPath + File.separator + newName;
        }
        log.info("tmpPath处理后路径========" + tmpPath);
        System.out.println("tmpPath处理后路径========" + tmpPath);
        if (theDoc.getIs_folder() != 1) {// 为1是表明该对象是文件夹

            //Map summaryMap = dao.getSummaryId(theDoc.getSource_id());
            // Long summaryId = Long.valueOf((String)
            // summaryMap.get(String.valueOf(theDoc.getSource_id())));
            Long summaryId = theDoc.getSource_id();
            mht2pdfService(summaryId, tmpPath);
        }
    }

    private String getFile(String theLogicPath) {
        String[] ids = UtilString.tokenize(theLogicPath, ".");
        String file = this.root;
        String name = null;
        int len = ids.length - 1;
        for (int i = 0; i < len; ++i) {
            name = (String) this.pathMap.get(ids[i]);
            file = file + File.separator + name;
        }
        return file;
    }

    private void mht2pdfService(Long id, String folderPath) throws Exception {
        String folder = folderPath;
        log.info("folder========" + folder);
        File file = new File(folderPath);

        if ((!(file.exists())) && (!(file.isDirectory()))) {
            file.mkdirs();
        } else {
            log.info("目录" + folder + "存在");
        }
        String summaryId = String.valueOf(id);
        String msg = exportFile(summaryId, folderPath);

    }

    public static String exportFile(String summaryId, String path) {
        // 指定协议、IP和端口，获取ClientManager
        User users = AppContext.getCurrentUser();
        String userName = users.getLoginName();
//        String userName = "zhangsan";
        CTPServiceClientManager clientManager = CTPServiceClientManager.getInstance(ConfigProperties.getServerIp());
        CTPRestClient client = clientManager.getRestClient();
        if (client.authenticate(ConfigProperties.getRestName(), ConfigProperties.getRestPassword())) {
            client.bindUser(userName);
            String[] summaryIdStr = new String[1];
            summaryIdStr[0] = summaryId;
            Map params = new HashMap();
            //导出公文单以及附件参数
            //params.put("summaryid", summaryIdStr);
            //导出公文单正文以及附件参数
            params.put("summaryid", summaryId);
            params.put("folder", path);
            params.put("exportType", "0");
            log.info("2.导出公文单");
//           String msg = client.post("edoc/export", params, String.class);
            //导出公文单以及正文
            String msg = client.post("edocResource/exportFile", params, String.class);
            return msg;
        }

        return null;
    }

}