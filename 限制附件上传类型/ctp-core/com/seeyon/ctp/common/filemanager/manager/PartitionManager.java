package com.seeyon.ctp.common.filemanager.manager;

import java.util.Date;
import java.util.List;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.filemanager.Partition;

public interface PartitionManager {

    /**
     * 所有分区
     * 
     * @return
     */
    public List<Partition> getAllPartitions();

    /**
     * 根据id，得到分区
     * 
     * @param id
     * @return
     */
    public Partition getPartition(Long id);

    /**
     * 根据文件创建时间，得到分区
     * 
     * @param createDate
     * @param isOnlyEnable 是否仅仅是可用的分区，true - 只取得可用的分区 false - 所有分区
     * @return
     */
    public Partition getPartition(Date createDate, boolean isOnlyEnable);

    /**
     * 拆分分区
     * 
     * @param originPartition
     *            被拆分的分区的id
     * @param newPartitionName
     *            新分区名称
     * @param newPartitionPath
     *            新分区路径
     * @param splitDate
     *            拆分时间点
     * @param newPartitionDescription
     */
    public void splitPartition(Long originPartition, String newPartitionName, String newPartitionPath, Date splitDate,
            String newPartitionDescription) throws BusinessException;

    /**
     * 创建分区
     * 
     * @param partition
     */
    public void create(Partition partition);

    /**
     * 修改分区信息
     * 
     * @param partition
     */
    public void update(Partition partition) throws BusinessException;

    /**
     * 删除分区
     * 
     * @param id
     */
    public void delete(long id);

    /**
     * 取得符合时间段的所有的分区
     * 
     * @param startDate
     * @param endDate
     * @param isOnlyEnabled 是否仅仅是可用的分区，true - 只取得可用的分区 false - 所有分区
     * @return
     */
    public List<Partition> getPartition(Date startDate, Date endDate, boolean isOnlyEnable);

    /**
     * 验证路径的正确性
     * 
     * @param path
     * @return true - 有效的
     */
    public boolean validatePath(String path);

    /**
     * 得到指定时间点的分区路径
     * 
     * @param createDate
     *            时间点
     * @param isOnlyEnable
     *            是否仅仅是可用的分区，true - 只取得可用的分区 false - 所有分区
     * @return
     */
    public String getPartitionPath(Date createDate, boolean isOnlyEnable);

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
     * 判断分区名称是否重复
     * 
     * Administrator 
     * Created on 2009-8-21
     */
    public boolean isPartitionNameDuple(String name);

    /**
     * 致信3.0中文件存在在V5端，针对致信存在有特殊处理
     * upload/zx/2017/07/12,为了避免对原来的接口产生变动添加针对致信的接口
     * @param createDate           文件创建时间
     * @param createWhenNoExist    当不存在该文件夹时创建之
     * @return                     文件的存放路径
     * @throws BusinessException
     */
    public String getFolderForUC(Date createDate, boolean createWhenNoExist)throws BusinessException;
}