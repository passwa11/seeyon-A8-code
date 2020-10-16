package com.seeyon.v3x.edoc.manager;

import java.util.List;

import com.seeyon.v3x.edoc.domain.EdocKeyWord;

/**
 * 公文主题词库管理器
 * @author Yang.Yinghai
 * @date 2011-10-10下午03:48:45
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public interface EdocKeyWordManager {

    /**
     * 为新建单位添加预置主题词库
     * @param accountId 单位ID
     */
    public void initCmpKeyWords(long accountId);

    /**
     * 保存关键词到数据库
     * @param edocKeyWord 关键词对象
     */
    public void save(EdocKeyWord edocKeyWord);

    /**
     * 根据条件查询主题词列表
     * @param parentId 父节点ID
     * @param name 关键词名字
     * @return
     */
    public List<EdocKeyWord> queryByCondition(long parentId, String name);

    /**
     * 根据ID返回EdocKeyWord对象
     * @param id 关键词ID
     * @return
     */
    public EdocKeyWord getById(long id);

    /**
     * 查找主题词库信息
     * @return 主题词库
     */
    public List<EdocKeyWord> getTreeList();
    public List<EdocKeyWord> getList();

    /**
     * 批量删除关键词
     * @param ids 关键词IDs
     */
    public void deleteByIds(String ids);

    /**
     * 更新关键词
     * @param edocKeyWord 关键词对象
     */
    public void update(EdocKeyWord edocKeyWord);

    /**
     * 判断在单位中是否已经存在该关键词
     * @param keyWord 关键词
     * @return true 存在 false 不存在
     */
    public boolean ajaxNameIsExist(String keyWord);
    
    /**
     * lijl添加,通过单位ID获取主题词　
     * @param accountId 单位ID
     * @return　List<EdocKeyWork>
     */
    public List<EdocKeyWord> getEdocKeyWordByAccountId(Long accountId);
}
