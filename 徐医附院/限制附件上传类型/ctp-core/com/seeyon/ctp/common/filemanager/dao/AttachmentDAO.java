package com.seeyon.ctp.common.filemanager.dao;

import java.util.List;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.dao.CTPBaseDao;
import com.seeyon.ctp.common.dao.CTPBaseHibernateDao;
import com.seeyon.ctp.common.filemanager.Constants;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.util.FlipInfo;

public interface AttachmentDAO extends CTPBaseHibernateDao<Attachment>{
    /**
     * 获取单个附件
     * 
     * @param id
     * @return
     */
    public Attachment get(Long id);

    /**
     * 按照主数据ID获取所有附件，包括次数据附件
     * 
     * @param reference
     * @return
     */
    public List<Attachment> findAll(Long reference);

    /**
     * 按照主/次数据ID获取所有附件
     * 
     * @param reference
     * @param subReference
     * @return
     */
    public List<Attachment> findAll(Long reference, Long subReference);

    /**
     * 
     * @param reference 文件关联的业务Id，比如groupId
     * @param subReference  子业务Id
     * @param flipInfo 分页组件
     * @return
     */
    public List<Object[]> findAll(Long reference, Integer type,FlipInfo flipInfo) ;
    /**
     * 按照主/多个次数据ID获取所有附件
     * 
     * @param reference
     * @param subReference
     * @return
     */
    public List<Attachment> findAll(Long reference, Long... subReference);

    /**
     * 查找所有附件的fileURL
     * 
     * @param reference
     * @return Object[] {fileId<Long>, createDate<Date>}
     */
    public List<Object[]> findAllFileUrl(Long reference);

    /**
     * 查找所有附件的fileURL
     * 
     * @param reference
     * @return Object[] {fileId<Long>, createDate<Date>}
     */
    public java.util.List<Object[]> findAllFileUrl(Long reference, Long subReference);

    /**
     * 
     * @param attachment
     */
    public void save(Attachment attachment);

    /**
     * 删除单个附件
     * 
     * @param id
     */
    public void delete(Long id);

    /**
     * 按照主数据ID删除所有附件
     * 
     * @param reference
     */
    public void deleteByReference(Long reference);

    /**
     * 按照主数据ID和次数据Id删除所有附件
     * 
     * @param reference
     */
    public void deleteByReference(Long reference, Long subReference);

    public boolean hasAttachments(Long reference, Long subReference);

    /**
     * 按照主数据ID和次数据Id获取某个类型的第一个附件
     * 
     * @param reference
     * @param subReference
     * @param type
     * @return
     */
    public Attachment getFirst(Long reference, Long subReference, Constants.ATTACHMENT_TYPE type);

    /**
     * 根据v3xfile的id取得附件对象
     * 
     * @param fileURL
     * @return
     */
    public Attachment getAttachmentByFileURL(Long fileURL);

    /**
     * 
     * @param attachment
     */
    public void update(Attachment attachment);

    /**
     * 是否是合法的来源
     * @return
     */
    public boolean checkIsLicitGenesis(Long referenceId, Long genesisId);

    /**
     * 根据文件标识新引用
     * @param fileUrl 文件标识
     * @param referenceId 业务id
     */
    public  void updateReference(Long fileUrl, Long referenceId);

    /**
     * 根据文件标识更新引用及子引用
     * @param fileUrl 文件标识
     * @param referenceId 业务主ID
     * @param subReference 业务子ID
     */
    public  void updateReferenceSubReference(Long fileUrl, Long referenceId, Long subReference);

    /**
     * 根据附件fileurl获取对应的附件信息
     * @param fileurls
     * @return
     */
    public  java.util.List<Attachment> find(List<Long> fileurls);
    
    /**
     * 更新文件名
     * @param fileName
     * @param affairIdList
     */
	public void updateFileNameByAffairIds(String fileName, List<Long> affairIdList);
	
	/**
	 *  根据subReference值查询 文件ID
	 * @param subReference
	 * @return
	 */
	public List<Long> getBySubReference(Long subReference);
	/**
	 * 批量说明
	 * @param attachmentIds
	 */
	public void deleteByIds(List<Long> attachmentIds);
	
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