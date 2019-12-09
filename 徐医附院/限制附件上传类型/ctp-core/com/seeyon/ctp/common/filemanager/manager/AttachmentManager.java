package com.seeyon.ctp.common.filemanager.manager;

import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.util.FlipInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author <a href="mailto:tanmf@seeyon.com">Tanmf</a>
 * @version 1.0 2006-11-17
 */
public interface AttachmentManager {

    /**
             * 获取单个附件
     * 
     * @param id
     * @return
     */
    public Attachment getAttachment(Long id);
	
    /**
     * 本方法只为显示附件，并没有保存。只是把从页面获得的参数传递转化为Attachment对象的列表。
     * @param request
     * @return
     */
    public List<Attachment> getAttachmentsFromRequestNotRelition(HttpServletRequest request);

    public List<Attachment> getAttachmentsFromRequestNotRelition(String attachstr);

    /**
     * 该方法必须和 comp=type:'fileupload'  配合使用，在request中必须有的Parameter：String[] fileUrl,
     * String[] mimeType, String[] size, String[] createdate, String[] filename,
     * String[] type, String[] needClone
     * 
     * 怎么判断是否有附件呢，请看：
     * 
     * <pre>
     * //保存附件
     * String attaFlag = this.attachmentManager.create(ApplicationCategoryEnum.collaboration, colSummary.getId(), colSummary.getId(), request);
     * if(com.seeyon.v3x.common.filemanager.Constants.isUploadLocaleFile(attaFlag)){
     * 	   colSummary.setHasAttachments(true);
     * }
     * </pre>
     *  
     * @param category
     *             所属应用分类
     * @param reference
     *             主题Id，如协同的Id
     * @param subReference
     *             二级主题Id，如协同的回复Id，如果当前是给协同上传附件，则该subReference 与 reference相同
     * @param request
     * @return 附件类型任意组合 如："012"、"12"、"01"、"02"; 其中 0-文件附件 1-图片 2-关联文 ,排列无序
     * @see com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE_FILE
     * @see com.seeyon.v3x.common.filemanager.Constants.isUploadLocaleFile(String)
     * @throws Exception
     */
    public String create(ApplicationCategoryEnum category, Long reference, Long subReference, HttpServletRequest request)
            throws Exception;

    /**
     * 该方法必须和 comp=type:'fileupload' 配合使用，在request中必须有的Parameter：String[] fileUrl,
     * String[] mimeType, String[] size, String[] createdate, String[] filename,
     * String[] type, String[] needClone
     * 
     * 怎么判断是否有附件呢，请看：
     * 
     * <pre>
     * //保存附件
     * String attaFlag = this.attachmentManager.create(ApplicationCategoryEnum.collaboration, colSummary.getId(), colSummary.getId(), request);
     * if(com.seeyon.v3x.common.filemanager.Constants.isUploadLocaleFile(attaFlag)){
     *     colSummary.setHasAttachments(true);
     * }
     * </pre>
     *  
     * @param category
     *             所属应用分类
     * @param reference
     *             主题Id，如协同的Id
     * @param subReference
     *             二级主题Id，如协同的回复Id，如果当前是给协同上传附件，则该subReference 与 reference相同
     * @return 附件类型任意组合 如："012"、"12"、"01"、"02"; 其中 0-文件附件 1-图片 2-关联文 ,排列无序
     * @see com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE_FILE
     * @see com.seeyon.v3x.common.filemanager.Constants.isUploadLocaleFile(String)
     * @throws Exception
     */
    public String create(ApplicationCategoryEnum category, Long reference, Long subReference)
            throws Exception;
    /**
     * 保存附件信息，文件已经上传到服务器上，只需要在附件表和文件表中记录
     * @param attachments 附件列表
     * @param memberId 用户编号
     * @param orgAccountId 单位编号
     * @return
     */
    public String create(Collection<Attachment> attachments,Long memberId,Long orgAccountId);
    /**
     * 根据应用自己获取的附件定义列表创建附件信息，一般用于Ajax提交的情况
     * 
     * @param category
     *             所属应用分类
     * @param reference
     *             主题Id，如协同的Id
     * @param subReference
     *             二级主题Id，如协同的回复Id，如果当前是给协同上传附件，则该subReference 与 reference相同
     * @param attachList
     *             应用自己获取的附件定义列表
     * @return 附件类型任意组合 如："012"、"12"、"01"、"02"; 其中 0-文件附件 1-图片 2-关联文 ,排列无序
     * @throws Exception
     */
    public String create(ApplicationCategoryEnum category, Long reference, Long subReference, List<Map> attachList)
            throws Exception;

    /**
     * 保存附件信息，文件已经上传到服务器上，只需要在附件表和文件表中记录
     * 
     * @param reference
     * @param subReference
     * @param category
     * @param extensions
     * @return 附件类型任意组合 如："012"、"12"、"01"、"02" 其中 0-文件附件 1-图片 2-关联文
     * @see com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE_FILE
     */
    public String create(Collection<Attachment> attachments);
    /**
     * 保存附件信息
     * @param attachments
     * @param needSetSort 是否需要重设附件的sort,cap4中逻辑优化调整
     * @return
     */
    public String create(Collection<Attachment> attachments, boolean needSetSort);

    /**
     * 存附件信息，文件已经上传到服务器上，只需要在附件表和文件表中记录
     * 
     * @param v3xFiles
     * @param cotegory
     * @param reference
     * @param subReference
     * @return 附件类型任意组合 如："012"、"12"、"01"、"02" 其中 0-文件附件 1-图片 2-关联文
     * @see com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE_FILE
     * @deprecated
     */
/*    public String create(Collection<V3XFile> v3xFiles, ApplicationCategoryEnum cotegory, Long reference,
            Long subReference);*/

    /**
     * 从v3x:fileupload中取得数据，构造Attachment，此时没有存放到数据库中
     * 
     * @param category
     * @param reference
     * @param subReference
     * @param request
     * @return
     * @throws Exception
     */
    public List<Attachment> getAttachmentsFromRequest(ApplicationCategoryEnum category, Long reference,
            Long subReference, HttpServletRequest request) throws Exception;

    public List<Attachment> getAttachmentsFromAttachList(ApplicationCategoryEnum category, Long reference,
            Long subReference, List mapList) throws Exception;

    /**
     * 构造attachment对象
     * 
     * @param category
     * @param reference
     * @param subReference
     * @param fileUrl
     * @param mimeType
     * @param size
     * @param createdate
     * @param filename
     * @param type
     * @param needClone
     * @param description
     * @return
     * @deprecated
     * @throws Exception
     */
/*    public List<Attachment> getAttachmentsFromRequest(ApplicationCategoryEnum category, Long reference,
            Long subReference, String[] fileUrl, String[] mimeType, String[] size, String[] createdate,
            String[] filename, String[] type, String[] needClone, String[] description) throws Exception;*/

    /**
     * 该方法必须和v3x:fileupload 配合使用，在request中必须有的Parameter：String[] fileUrl,
     * String[] mimeType, String[] size, String[] createdate, String[] filename,
     * String[] type, String[] needClone<br>
     * 执行的策略是，现delete，然后create
     * 
     * @param category
     *            所属应用分类
     * @param reference
     *            主题Id，如协同的Id
     * @param subReference
     *            二级主题Id，如协同的回复Id，如果当前是给协同上传附件，则该subReference 与 reference相同
     * @param request
     * @return 附件类型任意组合 如："012"、"12"、"01"、"02" 其中 0-文件附件 1-图片 2-关联文
     * @see com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE_FILE
     * @throws Exception
     */
    public String update(ApplicationCategoryEnum category, Long reference, Long subReference, HttpServletRequest request)
            throws Exception;

    /**
     * 读取一个主题下的所有附件信息，包括二级主题，如：协同的附件和协同回复的附件
     * 
     * @param reference
     * @return
     */
    public List<Attachment> getByReference(Long reference);

    /**
     * 读取附件信息
     * 
     * @param reference
     * @param subReference
     * @return
     */
    public List<Attachment> getByReference(Long reference, Long subReference);

    /**
     * 读取附件信息
     * 
     * @param reference 主数据
     * @param subReferences 次数据多个
     * @return
     */
    public List<Attachment> getByReference(Long reference, Long... subReferences);
    /**
     * @param reference 文件关联的业务Id，比如groupId
     * @param type  类型
     * @param firstResult 数据行开始位置，从0开始，-1表示不限制
     * @param maxResults 取数据量，-1表示不限制
     * @return 附件列表
     */
    public List<Object[]> getByReference(Long reference, Integer type,FlipInfo flipInfo);
    /**
     * 读取某个主体下所有附件的fileURL
     * 
     * @param reference
     * @return Object[] -- 二位数组，第一列: fileUrl,第二列：createDate
     */
    public List<Object[]> getAllFileUrlByReference(Long reference);

    /**
     * 按照主数据删除: 文件没有做物理删除
     * 
     * @param reference
     */
    public void deleteByReference(Long reference) throws BusinessException;

    /**
     * 按照主数据删除: 文件没有做物理删除
     * 
     * @param reference
     * @param type 类型
     */
    public void deleteByReference(Long reference, int... type) throws BusinessException;

    /**
     * 按照主数据删除: 文件做物理删除
     * 
     * @param reference
     * @throws BusinessException
     */
    public void removeByReference(Long reference) throws BusinessException;

    /**
     * 按照主数据和次数据删除: 文件没有做物理删除
     * 
     * @param reference
     * @param subReference
     */
    public void deleteByReference(Long reference, Long subReference) throws BusinessException;

    /**
     * 按照主数据和次数据删除: 文件做物理删除
     * 
     * @param reference
     * @param subReference
     */
    public void removeByReference(Long reference, Long subReference) throws BusinessException;

    /**
     * 按照附件Id删除
     * 
     * @param attachmentId
     */
    public void deleteById(long attachmentId);
    /**
     * 按照附件Id删除
     * 
     * @param attachmentId
     */
    public void deleteByIds(List<Long> attachmentId);
    /**
     * 是否包含附件, 慎用，做好在自己的表中增加一个“是否有附件”的标记
     * 
     * @param reference
     * @param subReference
     * @return
     */
    public boolean hasAttachments(Long reference, Long subReference);

    /**
     * 根据v3xfile的id取得附件对象
     * 
     * @param fileURL
     * @return
     */
    public Attachment getAttachmentByFileURL(Long fileURL);

    /**
     * 根据附件id取得附件对象
     * @param id
     * @return
     */
    public Attachment getAttachmentById(Long id);

    /**
     * 
     * @param attachment
     */
    public void update(Attachment attachment);

    /**
     * 复制附件，并把附件存入表中
     * 
     * @param reference
     *            原主体的Id
     * @param subReference
     *            原主体的次Id
     * @param newReference
     *            新主体的Id
     * @param newSubReference
     *            新主体的次Id
     * @param category
     *            新的所属应用分类
     * @return 附件类型任意组合 如："012"、"12"、"01"、"02" 其中 0-文件附件 1-图片 2-关联文档
     * @see com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE_FILE
     * @see com.seeyon.ctp.common.filemanager.Constants.isUploadLocaleFile
     */
    public String copy(Long reference, Long subReference, Long newReference, Long newSubReference, Integer category);

    /**
     * 根据文件表示更新引用
     * @param fileUrl 文件标识
     * @param referenceId
     */
    public  void updateReference(Long fileUrl, Long referenceId);

    /**
     * 根据文件表示更新引用及子引用
     * @param fileUrl 文件标识
     * @param referenceId
     * @param subReference
     */
    public  void updateReferenceSubReference(Long fileUrl, Long referenceId, Long subReference);

    
    /**
     * 上面那个接口必须需要用户登录才能复制，因为要用到userId,accountId,直接提供接口，不需要用户登录，为的是方便系统复制
     */
    public String copy(Long reference, Long subReference, Long newReference, Long newSubReference, Integer category,
            Long userId, Long accountId);
    
    /**
     * 
     * @param reference
     * @param subReference
     * @param newReference
     * @param newSubReference
     * @param category
     * @param userId
     * @param accountId
     * @param filename 新的文件名
     * @return
     */
    public String copy(Long reference, Long subReference, Long newReference, Long newSubReference, Integer category,
    		Long userId, Long accountId, String filename);

    /**
     * 复制附件, 不存入表
     * 
     * @param reference
     * @param subReference
     * @return
     */
    public List<Attachment> copy(Long reference, Long subReference);

    /**
     * 存附件信息，文件已经上传到服务器并且已经保存到数据库，只需要在附件表中记录
     * 
     * @param v3xFileId
     * @param cotegory
     * @param reference
     * @param subReference
     * @return 附件类型任意组合 如："012"、"12"、"01"、"02" 其中 0-文件附件 1-图片 2-关联文
     * @see com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE_FILE
     */
    public String create(Long[] v3xFileId, ApplicationCategoryEnum cotegory, Long reference, Long subReference);
/*
    public void save(List<Attachment> attas);*/

    public long getAttSizeSum(long attId);

    /**
     * 检测是否是合法的来源
     * @param referenceId 关联协同的ID
     * @param genesisId 来源Id
     * @return
     */
    public boolean checkIsLicitGenesis(Long referenceId, Long genesisId);

    /**
     * 为前端显示附件获取一个主题下的所有附件信息，包括二级主题，
     * 如：协同的附件和协同回复的附件
     * 
     * @param reference
     * @return 返回附件列表的json字符串
     */
    public  String getAttListJSON(Long reference);

    /**
     * 根据逗号分隔的fileurl获取对应的附件
     * @param fileURLs
     * @return
     */
    public  List<Attachment> getAttachmentByFileURLStrings(String fileURLs);

    /**
     * 根据fileurl list获取对应的附件list
     * @param fileURLs
     * @return
     */
    public  List<Attachment> getAttachmentByFileURLs(List<Long> fileURLs);

    /**
     * 根据逗号分隔的fileurl更新对应的应用ID
     * @param fileUrls
     * @param referenceId
     */
    public  void updateReferenceByFileUrls(String fileUrls, Long referenceId);

    /**
     * 为每个子业务id（subReference）关联和父业务（reference）相同的附件信息
     * @param category
     * @param reference
     * @param subReference
     * @return
     * @throws Exception
     
    public String create(ApplicationCategoryEnum category, Long reference, List<Long> subReference) throws Exception;
    */

    /**
     * 批量删除附件
     * @param references
     * @throws BusinessException
     */
    public  void deleteByReference(List<Long> references) throws BusinessException;

    /**
     *  为前端显示附件获取一个主题下指定区域的附件信息
     * 如：协同的附件或者协同回复的附件
     * @param reference
     * @param subReference
     * @return 返回附件列表的json字符串
     */
    public  String getAttListJSON(Long reference, Long subReference);

    public abstract List<Attachment> setOfficeTransformEnable(List<Attachment> list);

    public abstract String getAttListJSON(List<Attachment> list);

    public abstract String getAttListJSON4JS(Long reference);

    public abstract String getAttListJSON4JS(Long reference, Long subReference);
    
    /**
     * 更新文件名
     * @param fileName
     * @param affairIdList
     */
	public void updateFileNameByAffairIds(String newSubject, List<Long> affairIdList);
	
	/**
	 *  根据subReference值查询 文件ID
	 * @param subReference
	 * @return
	 */
	public List<Long> getBySubReference(Long subReference);
	
	public void deleteOnlyAttByReference(Long reference);
	
	/**
	 * 
	 * @Title: getBySubReference 
	 * @Description: CAP4 需求增加接,通过 subReferenct 进行Attachment对象查询
	 * @param @param subReferences
	 * @param @return  
	 * @return List<Attachment>   
	 * @throws
	 */
	public List<Attachment> getBySubReference(List<Long> subReferences);
	
	//G6添加方法
	public void saveAsAtt(List<Attachment> attachments);
	
	/**
	 * 获取不包含后缀的文件名字符串，用|隔开
	 * @param attachments
	 * @return
	 */
	public String getFileNameExcludeSuffix(List<Attachment> attachments);
	
	/**
	 * 获取用于附件说明展示的名称,格式：
	 * 1.xxxx
	 * 2.xxxx
	 * @param attachments
	 * @return
	 */
	public String getFilesmName(List<Attachment> attachments);
	
	/**
	 * 
	 * @Title: getByReference 
	 * @Description: CAP4 需求增加接,通过 List<Long> references 进行Attachment多主体对象查询
	 * @param @param references
	 * @param @return  
	 * @return List<Attachment>   
	 * @throws
	 */
	public List<Attachment> getByReference(List<Long> references);
}