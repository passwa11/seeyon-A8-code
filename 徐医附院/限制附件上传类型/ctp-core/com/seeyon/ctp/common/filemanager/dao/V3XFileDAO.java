package com.seeyon.ctp.common.filemanager.dao;

import java.util.List;

import com.seeyon.ctp.common.po.filemanager.V3XFile;

public interface V3XFileDAO {
    /**
     * 得到V3XFile对象
     * 
     * @param id
     * @return
     */
    public V3XFile get(Long id);

    /**
     * 
     * @param file
     */
    public void update(V3XFile file);

    /**
     * 多个
     * 
     * @param id
     * @return
     */
    public List<V3XFile> get(Long[] ids);

    /**
     * 插入一条数据
     * 
     * @param fileMapping
     */
    public void save(V3XFile file);

    /**
     * 插入多条数据
     * 
     * @param files
     */
    public void save(List<V3XFile> files);

    /**
     * 删除一条数据
     * 
     * @param id
     */
    public void delete(Long id);

    /**
     * 根据文件名称查询
     * @param fileName
     * @return
     */
    public List<V3XFile> findByFileName(String fileName);

}