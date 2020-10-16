package com.seeyon.apps.synorg.dao;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.seeyon.apps.synorg.constants.SynOrgConstants;
import com.seeyon.apps.synorg.po.SynDepartment;
import com.seeyon.apps.synorg.po.SynUnit;
import com.seeyon.apps.synorg.scheduletask.SynOrgTask;
import com.seeyon.ctp.util.DBAgent;

/**
 * 部门管理实现类
 * @author Yang.Yinghai
 * @date 2015-8-18下午4:24:41
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SyncDepartmentDaoImpl implements SyncDepartmentDao {

    /**
     * {@inheritDoc}
     */
    @Override
    public void createAll(List<SynDepartment> deptList) {
        DBAgent.saveAll(deptList);
    }
    
	@Override
	public void create(SynDepartment dept) {
		DBAgent.save(dept);
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAll(List<SynDepartment> deptList) {
        DBAgent.updateAll(deptList);
    }
    
	@Override
	public SynDepartment findDepByCode(String code) {
		return DBAgent.get(SynDepartment.class, code);
	}

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<SynDepartment> findAll() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("from SynDepartment where syncState=");
        buffer.append(SynOrgConstants.SYN_STATE_NONE);
        // 过滤掉中间表的根节点
        if(StringUtils.isNotBlank(SynOrgTask.getRootDeptCode())) {
            buffer.append(" and code != '");
            buffer.append(SynOrgTask.getRootDeptCode());
            buffer.append("'");
        }
        buffer.append(" order by parentCode,code desc");
        return DBAgent.find(buffer.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAll() {
        DBAgent.bulkUpdate("delete from SynDepartment where syncState="+SynOrgConstants.SYN_STATE_SUCCESS);
    }
    
	@Override
	public void delete(SynDepartment dep) {
		DBAgent.delete(dep);
	}
}
