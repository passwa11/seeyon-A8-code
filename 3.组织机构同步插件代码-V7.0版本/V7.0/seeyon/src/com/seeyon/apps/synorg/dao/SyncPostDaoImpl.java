package com.seeyon.apps.synorg.dao;

import java.util.List;

import com.seeyon.apps.synorg.constants.SynOrgConstants;
import com.seeyon.apps.synorg.po.SynMember;
import com.seeyon.apps.synorg.po.SynPost;
import com.seeyon.ctp.util.DBAgent;

/**
 * 岗位管理实现类
 * @author Yang.Yinghai
 * @date 2015-8-18下午4:24:41
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SyncPostDaoImpl implements SyncPostDao {

    /**
     * {@inheritDoc}
     */
    @Override
    public void createAll(List<SynPost> postList) {
        DBAgent.saveAll(postList);
    }
    
	@Override
	public void create(SynPost post) {
		DBAgent.save(post);
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAll(List<SynPost> postList) {
        DBAgent.updateAll(postList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<SynPost> findAll() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("from SynPost where syncState=" + SynOrgConstants.SYN_STATE_NONE);
        return DBAgent.find(buffer.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAll() {
        DBAgent.bulkUpdate("delete from SynPost where syncState="+SynOrgConstants.SYN_STATE_SUCCESS);
    }
    
	@Override
	public void delete(SynPost post) {
		DBAgent.delete(post);
	}

	@Override
	public SynPost findPostByCode(String code) {
		return DBAgent.get(SynPost.class, code);
	}
}
