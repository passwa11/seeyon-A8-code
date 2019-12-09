/**
 * 
 */
package com.seeyon.ctp.common.filemanager;

import static java.io.File.separator;

import com.seeyon.ctp.util.Strings;

/**
 * @author <a href="mailto:tanmf@seeyon.com">Tanmf</a>
 * @version 1.0 2006-11-15
 */
public class Constants {

    /**
     * 分区状态
     */
    public static enum PARTITION_STATE {
        used, // 启用
        unused, // 停用
    }

    /**
     * 日期格式 ： yyyy/MM/dd
     */
    public static final String DATE_TO_FOLDER_STYLE = "yyyy" + separator + "MM" + separator + "dd";
    
    public static final String DATE_TO_URL_STYLE = "yyyy/MM/dd";

    /**
     * 上传文件最大大小 51200K
     */
    public static final long   FILE_UPLOAD_SIZE_MAX = 51200;

    /***************************************************************************
     * 注意：如果需要增加附件类型，同时该附件类型不需要写入v3xfile表，<br>
     * 需要在AttachmentManagerImpl.create(Collection<Attachment>attachments)方法中特别指定
     * 
     */
    public static enum ATTACHMENT_TYPE {
        FILE, //本地上传的文件，将在附件区显示
        IMAGE, //正文中的图片，不在附件区显示
        DOCUMENT, //关联文档
        FormFILE, //表单中的附件
        FormDOCUMENT, //表单中的关联文档
        NewsImage, //新闻图片
    }

    /***************************************************************************
     * 附件/文件上传<code>&lt;input&gt;</code>的名称属性的定义
     */
    public static final String FILEUPLOAD_INPUT_NAME_reference       = "attachment_reference";

    public static final String FILEUPLOAD_INPUT_NAME_subReference    = "attachment_subReference";

    public static final String FILEUPLOAD_INPUT_NAME_fileUrl         = "attachment_fileUrl";

    public static final String FILEUPLOAD_INPUT_NAME_mimeType        = "attachment_mimeType";

    public static final String FILEUPLOAD_INPUT_NAME_size            = "attachment_size";

    public static final String FILEUPLOAD_INPUT_NAME_createDate      = "attachment_createDate";

    public static final String FILEUPLOAD_INPUT_NAME_filename        = "attachment_filename";

    public static final String FILEUPLOAD_INPUT_NAME_type            = "attachment_type";

    public static final String FILEUPLOAD_INPUT_NAME_needClone       = "attachment_needClone";

    public static final String FILEUPLOAD_INPUT_NAME_description     = "attachment_description";

    public static final String FILEUPLOAD_INPUT_NAME_extReference    = "attachment_extReference";

    public static final String FILEUPLOAD_INPUT_NAME_extSubReference = "attachment_extSubReference";

    public static final String FILEUPLOAD_INPUT_NAME_genesisId       = "attachment_genesisId";

    public static final int    FILE_NAME_MAX_LENGTH                  = 120;

    /**
     * 检测是否上传了本地文件，用来标示有附件
     * 
     * @param result attachmentManager.create()返回的值
     * @return true - 有附件
     */
    public static boolean isUploadLocaleFile(String result) {
        if (Strings.isBlank(result)) {
            return false;
        }

        return result.indexOf(String.valueOf(ATTACHMENT_TYPE.FILE.ordinal())) > -1
                || result.indexOf(String.valueOf(ATTACHMENT_TYPE.DOCUMENT.ordinal())) > -1;
    }
}
