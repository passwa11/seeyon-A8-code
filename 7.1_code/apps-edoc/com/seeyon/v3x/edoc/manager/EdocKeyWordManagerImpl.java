package com.seeyon.v3x.edoc.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AbstractSystemInitializer;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.cache.CacheAccessable;
import com.seeyon.ctp.common.cache.CacheFactory;
import com.seeyon.ctp.common.cache.CacheMap;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.dao.EdocKeyWordDao;
import com.seeyon.v3x.edoc.domain.EdocKeyWord;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * 主题词库管理器实现类
 * @author Yang.Yinghai
 * @date 2011-10-10下午03:51:53
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class EdocKeyWordManagerImpl extends AbstractSystemInitializer implements EdocKeyWordManager {
	
	private static final Log log = LogFactory.getLog(EdocKeyWordManagerImpl.class);

    /** 集团ID */
    private static long groupDomainId = 0L;

    /** 单位ID，主题词列表 映射 */
    private static CacheMap<Long, ArrayList<EdocKeyWord>> cmpKeyWordTable;

    /** 关键词数据层Dao对象 */
    private EdocKeyWordDao edocKeyWordDao;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initCmpKeyWords(long accountId) {
        // 新单位的主题词列表对象
        ArrayList<EdocKeyWord> newGroupKeyWord = new ArrayList<EdocKeyWord>();
        List<EdocKeyWord> thisKeyWords = edocKeyWordDao.getEdocKeyWordListByDomainId(accountId);
        if(Strings.isNotEmpty(thisKeyWords)){
            log.info("单位" + accountId + "已经有关键字， 不在进行复制");
            return;
        }
        
        // 获取系统预置的主题词库
        List<EdocKeyWord> groupKeyWord = cmpKeyWordTable.get(groupDomainId);
        // 预置数据 和 新数据的ID映射关系
        Map<Long, Long> idMap = new HashMap<Long, Long>();
        if(groupKeyWord != null && groupKeyWord.size() != 0) {
            for(EdocKeyWord oldKeyword : groupKeyWord) {
                // ??克隆的时候把创建人的ID设为该单位的单位管理员ID
                EdocKeyWord newKeyword = oldKeyword.clone(accountId);
                newGroupKeyWord.add(newKeyword);
                idMap.put(oldKeyword.getId(), newKeyword.getId());
            }
            for(EdocKeyWord newKeyword : newGroupKeyWord) {
            	if(newKeyword.getParentId() != 0) {
                	if(idMap.get(newKeyword.getParentId()) != null) {
                		newKeyword.setParentId(idMap.get(newKeyword.getParentId()));
                	} else {
                		log.error("新建单位复制出错的关键字:"+newKeyword.getId()+"《"+newKeyword.getName()+"》 parentId:"+newKeyword.getParentId());
                	}
                }
            }
            try {
            	edocKeyWordDao.savePatchAll(newGroupKeyWord);
            } catch(Exception e) {
            	log.error("公文关键字批量添加报错", e);
            }
        }
        // 加入缓存中
        cmpKeyWordTable.put(accountId, newGroupKeyWord);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(EdocKeyWord edocKeyWord) {
        edocKeyWordDao.save(edocKeyWord);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EdocKeyWord getById(long id) {
        return edocKeyWordDao.get(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EdocKeyWord> queryByCondition(long parentId, String name) {
        List<EdocKeyWord> result = new ArrayList<EdocKeyWord>();
        // 获取当前登录人员对象
        User user = AppContext.getCurrentUser();
        // 从内存中获取人员所在单位的全部关键词　
        //lijl注销,从原来从存内中获取单位下的主题词改为从数据库中获取
//        List<EdocKeyWord> cmpEdocKeyWord = cmpKeyWordTable.get(user.getLoginAccount());
        List<EdocKeyWord> cmpEdocKeyWord =null;
        //如果parentId==0只查根节点,如果不等于0则根据父节点查询该节点下的所有子节点
        if(parentId==0){
        	cmpEdocKeyWord = edocKeyWordDao.getEdocKeyWordByAccountId(user.getLoginAccount());
        }else{
        	cmpEdocKeyWord = edocKeyWordDao.getEdocKeyWordByParentId(parentId);
        }
        if(cmpEdocKeyWord != null) {
        	if(cmpEdocKeyWord.size()>0){
        		// 获取系统预置主题词
        		for(EdocKeyWord keyWord : cmpEdocKeyWord) {
        			// 只取一二级
        			if(keyWord.getIsSystem() && keyWord.getParentId() == parentId) {
        				// 设置是否有子元素
        				for(EdocKeyWord tempKeyWord : cmpEdocKeyWord) {
        					if(tempKeyWord.getParentId() == keyWord.getId()) {
        						keyWord.setHasChild(true);
        						break;
        					}
        				}
        				result.add(keyWord);
        			}
        		}
        	}
        }
        Collections.sort(result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EdocKeyWord> getTreeList() {
        // 获取人员所在单位的一二级关键词
        return edocKeyWordDao.getEdocKeyWordTreeByDomainId(AppContext.currentAccountId());
    }
    
    @Override
    public List<EdocKeyWord> getList() {
    	//获取人员所在单位的一二级关键词
        return edocKeyWordDao.getEdocKeyWordListByDomainId(AppContext.currentAccountId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteByIds(String ids) {
        edocKeyWordDao.deleteByIds(ids);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(EdocKeyWord edocKeyWord) {
        edocKeyWordDao.update(edocKeyWord);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean ajaxNameIsExist(String keyWord) {
        boolean result = false;
        // 从内存中获取人员所在单位的全部关键词
        List<EdocKeyWord> cmpEdocKeyWord = edocKeyWordDao.getEdocKeyWordListByDomainId(AppContext.currentAccountId());
        if(cmpEdocKeyWord != null && cmpEdocKeyWord.size() != 0) {
            // 获取用户自定义主题词
            for(EdocKeyWord edocKeyWord : cmpEdocKeyWord) {
                if(keyWord.equals(edocKeyWord.getName())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 设置edocKeyWordDao
     * @param edocKeyWordDao edocKeyWordDao
     */
    public void setEdocKeyWordDao(EdocKeyWordDao edocKeyWordDao) {
        this.edocKeyWordDao = edocKeyWordDao;
    }

    /**
     * 初始化主题词库信息到内存中
     */
	public synchronized void initialize() {
		CacheAccessable factory = CacheFactory.getInstance(EdocKeyWordManagerImpl.class);
    	cmpKeyWordTable = factory.createMap("cmpKeyWordTable");
        // 数据库中集团所有的关键词
        cmpKeyWordTable.put(groupDomainId,  (ArrayList<EdocKeyWord>)edocKeyWordDao.getEdocKeyWordListByDomainId(groupDomainId));
    }
    
    /**
     * lijl添加,通过单位ID获取主题词
     * @param accountId 单位ID
     * @return　List<EdocKeyWork>
     */
    public List<EdocKeyWord> getEdocKeyWordByAccountId(Long accountId){
    	return edocKeyWordDao.getEdocKeyWordByAccountId(accountId);
    }
}
