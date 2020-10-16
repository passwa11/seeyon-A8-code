package com.seeyon.apps.govdoc.manager.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.govdoc.helper.GovdocRoleHelper;
import com.seeyon.apps.govdoc.manager.GovdocStatPushManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.edoc.domain.EdocRegisterCondition;
import com.seeyon.v3x.edoc.manager.EdocManager;

public class GovdocStatPushManagerImpl implements GovdocStatPushManager {

	private EdocManager edocManager;

	@Override
	public List<EdocRegisterCondition> getRegisterConditionList(User user, Map<String, Object> paramMap) {
		if("listSendRegister".equals(paramMap.get("listType"))) {
			paramMap.put("type", "1");
		} else if("listRecRegister".equals(paramMap.get("listType"))) {
			paramMap.put("type", "2");
		}
		return edocManager.getEdocRegisterCondition(user.getLoginAccount(), paramMap, user);
	}
	
	@Override
	public EdocRegisterCondition getRegisterConditionById(Long conditionId) throws BusinessException {
		return edocManager.getEdocRegisterConditionById(conditionId);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void saveRegisterCondition(String listType, User user) throws BusinessException {
		//显示的列
        List<Map<String, String>> columnDomainList = ParamUtil.getJsonDomainGroup("columnDomain");
        //查询条件
        Map<String, String> queryDomainMap = ParamUtil.getJsonDomain("queryDomain");

        String title = queryDomainMap.get("pushConditionName");
        String type = "1";
        if ("listSendRegister".equals(listType)) {//发文登记薄
            type = "1";
        } else if ("listRecRegister".equals(listType)) {
            type = "2";
        }

        StringBuilder queryCol = new StringBuilder();

        if (Strings.isEmpty(columnDomainList)) {//兼容处理，ParamUtil.getJsonDomainGroup只有一条数据的时候不会返回数据
            Map<String, String> column = ParamUtil.getJsonDomain("columnDomain");
            if (column.size() > 0) {//有数据
                columnDomainList.add(column);
            }
        }

        for (Map<String, String> columnMap : columnDomainList) {
            String name = columnMap.get("name");
            if ("".equals(queryCol.toString())) {
                queryCol.append(name);
            } else {
                queryCol.append("," + name);
            }
        }

        queryDomainMap.remove("pushConditionName");//移除推送的名字

        String condition = JSONUtil.toJSONString(queryDomainMap);

        String exchangeDepts = null;
        //不是单位管理员需要保存推送的范围
        if (!GovdocRoleHelper.isAccountExchange(user.getId())) {
            String departmentIds = GovdocRoleHelper.getUserExchangeDepartmentIds();
            if (Strings.isNotBlank(departmentIds)) {
                String[] depIds = departmentIds.split("[,]");
                if (depIds.length > 0) {
                    exchangeDepts = departmentIds;
                }
            }
        }

        EdocRegisterCondition regCon = new EdocRegisterCondition();
        regCon.setIdIfNew();
        regCon.setUserId(user.getId());
        regCon.setAccountId(user.getLoginAccount());

        Timestamp now = new Timestamp(System.currentTimeMillis());
        regCon.setCreateTime(now);
        regCon.setType(Integer.parseInt(type));
        regCon.setStarttime(null);//这个属性废弃了
        regCon.setEndtime(null);//这个属性废弃了
        regCon.setQueryCol(queryCol.toString());
        regCon.setTitle(title);
        regCon.setContentExt1(condition);
        regCon.setContentExt2(exchangeDepts);//部门权限范围
        edocManager.saveEdocRegisterCondition(regCon);
	}
	
	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}
	
}
