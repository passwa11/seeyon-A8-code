package com.seeyon.apps.synorg.dao;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.seeyon.apps.synorg.constants.SynOrgConstants;
import com.seeyon.apps.synorg.po.SynUnit;
import com.seeyon.apps.synorg.scheduletask.SynOrgTask;
import com.seeyon.ctp.util.DBAgent;

public class SyncUnitDaoImpl implements SyncUnitDao{

	@Override
	public void createAll(List<SynUnit> unitList) {
		DBAgent.saveAll(unitList);
	}
	
	@Override
	public void create(SynUnit unit) {
		DBAgent.save(unit);
	}

	@Override
	public void updateAll(List<SynUnit> unitList) {
		DBAgent.updateAll(unitList);
	}

	@Override
	public List<SynUnit> findAll() {
		StringBuilder buffer = new StringBuilder();
        buffer.append("from SynUnit where syncState=");
        buffer.append(SynOrgConstants.SYN_STATE_NONE);
        // 过滤掉中间表的根节点
//        if(StringUtils.isNotBlank(SynOrgTask.getRootDeptCode())) {
//            buffer.append(" and code != '");
//            buffer.append(SynOrgTask.getRootDeptCode());
//            buffer.append("'");
//        }
        buffer.append(" order by parentCode,code desc");
        return DBAgent.find(buffer.toString());
	}

	@Override
	public SynUnit findAllByCode(String code) {
        return DBAgent.get(SynUnit.class, code);
	}

	@Override
	public List<SynUnit> findAllSynUnit() {
		StringBuilder buffer = new StringBuilder();
        buffer.append("from SynUnit where syncState=");
        buffer.append(SynOrgConstants.SYN_STATE_SUCCESS);
        // 过滤掉中间表的根节点
//        if(StringUtils.isNotBlank(SynOrgTask.getRootDeptCode())) {
//            buffer.append(" and code != '");
//            buffer.append(SynOrgTask.getRootDeptCode());
//            buffer.append("'");
//        }
        buffer.append(" order by parentCode,code desc");
        return DBAgent.find(buffer.toString());
	}

	@Override
	public void delete(SynUnit unit) {
		DBAgent.delete(unit);
	}

}
