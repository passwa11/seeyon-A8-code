package com.seeyon.ctp.common.filemanager.manager;

import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.domain.ReplaceBase64Result;
import com.seeyon.ctp.common.po.filemanager.V3XFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface FileManager {

    /**
     * 记录文件信息，只在文件表中记录信息，文件是否已经上传不关心
     * 
     * @param file
     */
    public void save(V3XFile file);
    
    public void save(List<V3XFile> files);

    public void update(V3XFile file);

    /**
     * 把文件存到文件系统中去
     * 
     * @param file
     *            要保存的文件对象
     * @param category
     *            所属应用类别
     * @param filename
     *            显示文件名
     * @param createDate
     *            文件存储日期，一般就是此刻，用new Date()
     * @param isSaveToDB
     *            是否把V3XFile对象存到数据库中去，如果不存，在清理文件时将会被清理掉
     * @return
     * @throws BusinessException
     */
    public V3XFile save(File file, ApplicationCategoryEnum category, String filename, Date createDate,
            Boolean isSaveToDB) throws BusinessException;

    /**
     * 把输入流存到文件系统中去
     * 
     * @param in
     * @param category
     *            所属应用类别
     * @param filename
     *            显示文件名
     * @param createDate
     *            文件存储日期，一般就是此刻，用new Date()
     * @param isSaveToDB
     *            是否把V3XFile对象存到数据库中去，如果不存，在清理文件时将会被清理掉
     * @return
     * @throws BusinessException
     */
    public V3XFile save(InputStream in, ApplicationCategoryEnum category, String filename, Date createDate,
            Boolean isSaveToDB) throws BusinessException;

    /**
     * 把文本存到文件系统中去
     * 
     * @param bodyData
     *            文本内容
     * @param category
     *            所属分类
     * @param filename
     *            显示文件名
     * @param createDate
     *            文件存储日期，一般就是此刻，用new Date()
     * @param isSaveToDB
     *            是否把V3XFile对象存到数据库中去，如果不存，在清理文件时将会被清理掉
     * @return V3XFile
     * @throws BusinessException
     */
    public V3XFile save(String bodyData, ApplicationCategoryEnum category, String filename, Date createDate,
            Boolean isSaveToDB) throws BusinessException;

    /**
     * 将正文中的base64位编码的图片转为图片路径
     * @param html
     * @return
     */
    public ReplaceBase64Result replaceBase64Image(String html) throws BusinessException;

    /**
     * 将图片的base64位字符串转换后保存到磁盘中
     * @param base64Str
     * @param fileName
     * @param param
     * @return
     * @throws BusinessException
     */
    public V3XFile saveBase64Img(String base64Str,String fileName,Map<String,Object> param) throws BusinessException;

    /**
     * 从前端通过v3x:fileupload组件上传的文件直接存到文件系统中去<br>
     * 该方法不负责文件的上传
     * 
     * @param category
     * @param request
     * @return
     * @throws BusinessException
     */
    public List<V3XFile> create(ApplicationCategoryEnum category, HttpServletRequest request) throws BusinessException;

    /**
     * 得到当前的存储路径,目录结构: 分区目录/yyyy/MM/dd
     * 
     * @param createWhenNoExist
     * @return 如： F:/upload/2006/05/09
     * @throws BusinessException
     */
    public String getNowFolder(boolean createWhenNoExist) throws BusinessException;

    /**
     * 根据文件创建时间，获取文件上传目录，目录结构: 分区目录/yyyy/MM/dd<br>
     * 如: F:/upload/2006/05/09
     * 
     * @param createDate
     *            文件创建时间
     * @param createWhenNoExist
     *            当不存在该文件夹时创建之
     * @return
     * @throws BusinessException
     *             没有分区
     */
    public String getFolder(Date createDate, boolean createWhenNoExist) throws BusinessException;

    /**
     * 得到V3XFile文件对象
     * 
     * @param fileId
     * @return
     * @throws BusinessException
     */
    public V3XFile getV3XFile(Long fileId) throws BusinessException;

    /**
     * 得到V3XFile文件对象
     * 
     * @param fileId
     * @return
     * @throws BusinessException
     */
    public List<V3XFile> getV3XFile(Long[] fileIds) throws BusinessException;

    /**
     * 根据文件Id，获得文件对象
     * 
     * @param fileId
     * @return
     * @throws BusinessException
     *             文件不存在
     */
    public File getFile(Long fileId) throws BusinessException;

    /**
     * 根据文件Id，获得文件对象
     * 
     * @param fileIds
     * @return
     * @throws BusinessException
     */
    // public List<File> getFiles(Long[] fileIds) throws BusinessException;
    /**
     * 根据文件名和文件生成时间，获得文件对象
     * 
     * @param fileName 对应file表中的id,attachment表的fileurl
     * @param createDate
     *            文件不存在
     * @return
     * @throws BusinessException
     */
    public File getFile(Long fileName, Date createDate) throws BusinessException;

    /**
     * 致信端获取文件特殊处理
     * @param fileId
     * @param createDate
     * @return
     * @throws BusinessException
     */
    public File getFileForUC(Long fileId,Date createDate) throws BusinessException;

    public File getThumFile(Long fileId, Date createDate, int px) throws BusinessException;

    /**
     * 致信端获取文件的缩略图
     * @param fileId
     * @param createDate
     * @param px
     * @return
     * @throws BusinessException
     */
    public File getThumFileForUC(Long fileId, Date createDate, String pxStr) throws BusinessException;
    
    /**
     * 取得缩略图，大小默认600
     * @param fileId
     * @param createDate
     * @return
     * @throws BusinessException
     */
    public File getThumFile(Long fileId, Date createDate) throws BusinessException;

    /**
     * 根据多个文件Id和文件生成时间，获得文件对象
     * 
     * @param fileIds
     * @param createDate
     * @return
     * @throws BusinessException
     */
    // public List<File> getFiles(Long[] fileIds, Date createDate)
    // throws BusinessException;
    /**
     * 得到文件IO
     * 
     * @param fileId
     * @return
     */
    public InputStream getFileInputStream(Long fileId) throws BusinessException;

    /**
     * 得到文件IO
     * 
     * @param fileId
     * @param createDate
     * @return
     */
    public InputStream getFileInputStream(Long fileId, Date createDate) throws BusinessException;

    /**
     * 致信文件服务，提供的接口，为避免影响原来的内容，添加新接口
     * @param fileId
     * @param createDate
     * @return
     * @throws BusinessException
     */
    public InputStream getFileInputStreamForUC(Long fileId, Date createDate) throws BusinessException;

    /**
     * 得到文件字节
     * 
     * @param fileId
     * @return
     */
    public byte[] getFileBytes(Long fileId) throws BusinessException;

    /**
     * 得到文件字节
     * 
     * @param fileId
     * @return
     */
    public byte[] getFileBytes(Long fileId, Date createDate) throws BusinessException;

    /**
     * 通过Portlet方式上传多个文件<br>
     * 只负责把文件存放在硬盘上<br>
     * 不把文件信息写进v3x_file表<br>
     * 
     * @param request
     * @param allowExtensions
     *            允许的后缀名，多个用,分割，如： jpeg,jpg,gif,png，不区分大小写，可以为空
     * @param maxSize
     *            最大byte
     * @return Map<String, V3XFile> fieldName-V3XFile对
     * @throws BusinessException
     */
    public Map<String, V3XFile> uploadFiles(HttpServletRequest request, String allowExtensions, Long maxSize)
            throws BusinessException;

    /**
     * 通过Portlet方式上传一个文件<br>
     * 只负责把文件存放在指定的位置<br>
     * 不把文件信息写进v3x_file表<br>
     * 约定：<code>&lt;input type='file' name='file1'&gt;</code>的name为file1
     * 
     * @param request
     * @param allowExtensions
     *            允许的后缀名，多个用,分割，如： jpeg,jpg,gif,png，不区分大小写，可以为空
     * @param destFile
     *            指定的文件名，采用全名，如c:\ext\log.txt
     * @param maxSize
     *            最大byte
     * @return Map<String, V3XFile> fieldName-V3XFile对
     * @throws BusinessException
     */
    public Map<String, V3XFile> uploadFiles(HttpServletRequest request, String allowExtensions, File destFile,
            Long maxSize) throws BusinessException;

    /**
     * 通过Portlet方式上传一个文件<br>
     * 只负责把文件存放在指定的位置<br>
     * 不把文件信息写进v3x_file表<br>
     * 
     * @param request
     * @param allowExtensions
     * @param destFiles
     *            Map<String, File> fieldName-File对
     * @return
     * @throws BusinessException
     */
    public Map<String, V3XFile> uploadFiles(HttpServletRequest request, String allowExtensions,
            Map<String, File> destFiles, Long maxSize) throws BusinessException;

    /**
     * 通过Portlet方式上传多个文件<br>
     * 只负责把文件存放在指定的位置<br>
     * 不把文件信息写进v3x_file表<br>
     * 
     * @param request
     * @param allowExtensions
     *            允许的后缀名，多个用,分割，如： jpeg,jpg,gif,png，不区分大小写，可以为空
     * @param type
     * @param destDirectory
     *            上传到指定的文件夹
     * @param maxSize
     *            最大byte
     * @return Map<String, V3XFile> fieldName-V3XFile对
     * @throws BusinessException
     */
    public Map<String, V3XFile> uploadFiles(HttpServletRequest request, String allowExtensions, String destDirectory,
            Long maxSize) throws BusinessException;
    /**
     * 通过Portlet方式上传多个文件<br>
     * 只负责把文件存放在硬盘上<br>
     * 不把文件信息写进v3x_file表<br>
     * 
     * @param request
     * @param allowExtensions
     *            允许的后缀名，多个用,分割，如： jpeg,jpg,gif,png，不区分大小写，可以为空
     * @param maxSize
     *            最大byte
     * @param memberId
     *            用户编号         
     * @param accountId
     *            单位编号
     * @return Map<String, V3XFile> fieldName-V3XFile对
     * @throws BusinessException
     */
    public Map<String, V3XFile> uploadFiles(HttpServletRequest request,Long memberId,Long accountId, String allowExtensions, Long maxSize)
            throws BusinessException;
    /**
     * 删除文件
     * 
     * @param fileId
     * @param deletePhysicsFile
     *            是否删除物理文件
     * @throws BusinessException
     */
    public void deleteFile(Long fileId, Boolean deletePhysicsFile) throws BusinessException;

    /**
     * 删除文件
     * 
     * @param fileId
     * @param createDate
     * @param deletePhysicsFile
     *            是否删除物理文件
     * @throws BusinessException
     */
    public void deleteFile(Long fileId, Date createDate, Boolean deletePhysicsFile) throws BusinessException;

    /**
     * 复制一个文件，现从文件总表中根据originalFileId读取文件信息，再复制文件，设置新的fileId
     * 
     * @param originalFileId
     *            要复制的文件的id
     * @param saveDB
     *            是否同时保存到数据库
     * @return
     * @throws BusinessException
     */
    public V3XFile clone(Long originalFileId, boolean saveDB) throws BusinessException, FileNotFoundException;

    /**
     * 复制一个文件，现从文件总表中根据originalFileId读取文件信息，再复制文件，设置新的fileId，文件名称前加copy
     * 用于office控件清稿备份
     * @param originalFileId
     *            要复制的文件的id
     * @return
     * @throws BusinessException
     */
    public V3XFile clone(Long originalFileId) throws BusinessException, FileNotFoundException;

    /**
     * 根据originalFileId和createDate能够定位文件物理位置，并按照新的创建时间复制
     * 
     * @param originalFileId
     * @param originalCreateDate
     * @param newFileId
     * @param newCreateDate
     * @return
     * @throws BusinessException
     * @throws FileNotFoundException
     */
    public void clone(Long originalFileId, Date originalCreateDate, Long newFileId, Date newCreateDate)
            throws BusinessException, FileNotFoundException;

    /**
     * 得到Office正文的标准格式
     * 
     * @param fileId
     * @param createDate
     * @return
     * @throws BusinessException
     */
    public File getStandardOffice(Long fileId, Date createDate) throws BusinessException;

    /**
     * 得到Office正文的标准格式
     * 
     * @param fileId
     * @param createDate
     * @return
     * @throws BusinessException
     */
    public InputStream getStandardOfficeInputStream(Long fileId, Date createDate) throws BusinessException,
            FileNotFoundException;

    public List<V3XFile> findByFileName(String fileName);

    /**
     * 在进行编辑上传类型的文件时，替换之前保存一份历史，区别于正常的替换
     */
    public Long copyFileBeforeModify(Long fileId);

    /**
     * 通过文档的sourceId获得file
     */
    public Long getFileIdByDocResSourceId(Long fileId);

    /**
     * 手动备份WPS的上传类型----文字和表格，备份的命名规则和普通office相同，见HandWriterManage.java----L340
     */
    public void copyWPS(Long sourceId);

    /**
     * 传入文件绝对路径
     */
    public File getStandardOffice(String fileAbsolutePath) throws BusinessException;

    /**
     * 解密文件。因通过附件组件上传的文件时加密的，在使用前需要解密。
     * @param file 被加密的文件
     * @return 解密后的文件
     */
    public  File decryptionFile(File file);

    /**
     * 获取解密后的文件
     * @param fileId
     * @param createDate
     * @return
     * @throws BusinessException
     */
    public  File getFileDecryption(Long fileId, Date createDate) throws BusinessException;

    /**
     *  获取解密后的文件
     * @param fileId
     * @return
     * @throws BusinessException
     */
    public  File getFileDecryption(Long fileId) throws BusinessException;

    /**
     * 根据是否需要解密获取文件
     * @param fileId
     * @param decryption true：需要解密，false不需要解密
     * @return
     * @throws BusinessException
     */
    public  File getSpicFile(Long fileId, boolean decryption) throws BusinessException;

    /**
     * 删除物理文件
     * @param fileId
     * @throws BusinessException
     */
    public  void deletePhysicsFile(Long fileId) throws BusinessException;
    
    /** 
     * <pre>
     * 获取Office正文的扩展名
     *     
     * @param file 正文文件
     * @return
     * @throws BusinessException      
     * @return: String  
     * @date:   2019年7月2日 
     * @author: yaodj
     * @since   v7.1 sp1    
     * </pre> 
     */ 
    public String getOfficeSuffix(V3XFile file) throws BusinessException;
}