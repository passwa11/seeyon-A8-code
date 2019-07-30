package com.seeyon.apps.synorg.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.synorg.constants.SynOrgConstants;
import com.seeyon.apps.synorg.manager.SyncMemberManagerImpl;
import com.seeyon.apps.synorg.po.SynMember;
import com.seeyon.apps.synorg.po.SynUnit;
import com.seeyon.ctp.util.DBAgent;

/**
 * 人员管理实现类
 * @author Yang.Yinghai
 * @date 2015-8-18下午4:24:41
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SyncMemberDaoImpl implements SyncMemberDao {
	private static final Log log = LogFactory.getLog(SyncMemberDaoImpl.class);
    /**
     * {@inheritDoc}
     */
    @Override
    public void createAll(List<SynMember> memberList) {
    	try{
    		DBAgent.saveAll(memberList);
    	}catch (Exception e) {
			log.error("大渡河MQ导入人员账号数据异常！",e);
			System.out.println("大渡河MQ导入数据异常！");
		}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAll(List<SynMember> memberList) {
        DBAgent.updateAll(memberList);
    }
    
	@Override
	public void update(SynMember member) {
		DBAgent.update(member);
	}

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<SynMember> findAll() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("from SynMember where syncState=" + SynOrgConstants.SYN_STATE_NONE);
        return DBAgent.find(buffer.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAll() {
        DBAgent.bulkUpdate("delete from SynMember where syncState="+SynOrgConstants.SYN_STATE_SUCCESS);
    }
    
	@Override
	public void delete(SynMember member) {
		DBAgent.delete(member);
	}

	@Override
	public SynMember findPersonByAccount(String loginName) {
		return DBAgent.get(SynMember.class, loginName);
	}

}
