package com.seeyon.v3x.edoc.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.edoc.domain.EdocKeyWord;

/**
 * 关键词数据层Dao对象
 * @author Yang.Yinghai
 * @date 2011-10-10下午02:36:13
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class EdocKeyWordDao extends BaseHibernateDao<EdocKeyWord> {

    /**
     * 批量删除关键词
     * @param ids 关键词IDs
     */
    public void deleteByIds(String ids) {
        String hql = "delete EdocKeyWord where id in (:ids)";
        List<Long> idList = new ArrayList<Long>();
        String[] tmp = ids.split(",");
        for(String id : tmp) {
            idList.add(Long.valueOf(id));
        }
        Map<String, Object> parameter = new HashMap<String, Object>();
        parameter.put("ids", idList);
        super.bulkUpdate(hql, parameter);
    }
    
    /**
     * lijl添加,通过单位ID获取主题词
     * @param accountId 单位ID
     * @return　List<EdocKeyWork>
     */
    public List<EdocKeyWord> getEdocKeyWordByAccountId(Long accountId){
    	String hql="from EdocKeyWord where accountId=:accountId and parentId=0 order by sortNum";
    	Map map=new HashMap();
    	map.put("accountId",accountId);
    	return super.find(hql, map);
    }
    
    /**
     * lijl添加,通过单位ID获取主题词
     * @param accountId 单位ID
     * @return　List<EdocKeyWork>
     */
    @SuppressWarnings("unchecked")
	public List<EdocKeyWord> getEdocKeyWordListByDomainId(Long domainId){
    	String hql="from EdocKeyWord where accountId=:domainId and isSystem=:isSystem order by levelNum, sortNum";
    	Map<String, Object> map = new HashMap<String, Object>();
    	map.put("domainId", domainId);
    	map.put("isSystem", Boolean.TRUE);
    	return super.find(hql, -1, -1, map);
    }
    
    @SuppressWarnings("unchecked")
	public List<EdocKeyWord> getEdocKeyWordTreeByDomainId(Long domainId){
    	String hql="from EdocKeyWord where accountId=:domainId and levelNum in (1,2) order by levelNum, sortNum";
    	Map<String, Object> map = new HashMap<String, Object>();
    	map.put("domainId", domainId);
    	return super.find(hql, -1, -1, map);
    }
    
    /**
     * 通过父节点来获取该节点下的所有子节点
     * @param parentId 父节点Id
     * @return List<EdocKeyWord>
     */
    public List<EdocKeyWord> getEdocKeyWordByParentId(Long parentId){
    	String hql="from EdocKeyWord ek where ek.parentId=:parentId order by sortNum";
    	Map map=new HashMap();
    	map.put("parentId",parentId);
    	return super.find(hql, map); 
    } 

}
