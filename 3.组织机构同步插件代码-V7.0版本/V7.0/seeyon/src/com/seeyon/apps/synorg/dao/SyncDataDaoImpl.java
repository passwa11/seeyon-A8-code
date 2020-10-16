package com.seeyon.apps.synorg.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.synorg.constants.SynOrgConstants;
import com.seeyon.apps.synorg.vo.SyncDataListVO;
import com.seeyon.apps.synorg.vo.SyncMemberListVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;

/**
 * Description
 * <pre></pre>
 * @author FanGaowei<br>
 * Date 2018年2月24日 下午5:01:15<br>
 * Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SyncDataDaoImpl implements SyncDataDao {

	@Override
	public List<SyncDataListVO> queryData(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		String type = condition.get("entityType");
		// 查询单位
		if(SynOrgConstants.ORG_ENTITY_UNIT.equals(type)) {
			return queryUnit(flipInfo, condition);
		}
		// 查询职务
		if(SynOrgConstants.ORG_ENTITY_LEVEL.equals(type)) {
			return queryLevel(flipInfo, condition);
		}
		// 查询岗位
		if(SynOrgConstants.ORG_ENTITY_POST.equals(type)) {
			return queryPost(flipInfo, condition);
		}
		// 查询岗位
		if(SynOrgConstants.ORG_ENTITY_MEMBER.equals(type)) {
			return queryMember(flipInfo, condition);
		}
		return queryDept(flipInfo, condition);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SyncMemberListVO> queryMemberData(FlipInfo flipInfo, Map<String, String> condition)
			throws BusinessException {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
        StringBuilder hql = new StringBuilder();
        hql.append("select entity.code, entity.name, entity.loginName, entity.departmentCode as deptCode, entity.postCode, entity.levelCode, entity.email, entity.telNumber as telNum, entity.gender, entity.syncState ");
        hql.append("from SynMember as entity where 1 = 1 ");
        // 同步状态
        if(Strings.isNotBlank(condition.get("synState"))) {
            String synState = condition.get("synState");
            hql.append("and entity.syncState=:synState ");
            if("1".equals(synState)) {
                parameterMap.put("synState", SynOrgConstants.SYN_STATE_SUCCESS);
            } else if("-1".equals(synState)) {
                parameterMap.put("synState", SynOrgConstants.SYN_STATE_FAILURE);
            } else {
                parameterMap.put("synState", SynOrgConstants.SYN_STATE_NONE);
            }
        }
        // 实体名称
        if(Strings.isNotBlank(condition.get("name"))) {
            hql.append("and entity.name like :name ");
            parameterMap.put("name", "%" + SQLWildcardUtil.escape(condition.get("name")) + "%");
        }
        // 实体编码
        if(Strings.isNotBlank(condition.get("code"))) {
            hql.append("and entity.code like :code ");
            parameterMap.put("code", "%" + SQLWildcardUtil.escape(condition.get("code")) + "%");
        }
        // 登录名
        if(Strings.isNotBlank(condition.get("loginName"))) {
            hql.append("and entity.loginName like :loginName ");
            parameterMap.put("loginName", "%" + SQLWildcardUtil.escape(condition.get("loginName")) + "%");
        }
        if(Strings.isNotBlank(condition.get("deptCode"))) {
            hql.append("and entity.departmentCode like :deptCode ");
            parameterMap.put("deptCode", "%" + SQLWildcardUtil.escape(condition.get("deptCode")) + "%");
        }
        if(Strings.isNotBlank(condition.get("postCode"))) {
            hql.append("and entity.postCode like :postCode ");
            parameterMap.put("postCode", "%" + SQLWildcardUtil.escape(condition.get("postCode")) + "%");
        }
        if(Strings.isNotBlank(condition.get("levelCode"))) {
            hql.append("and entity.levelCode like :levelCode ");
            parameterMap.put("levelCode", "%" + SQLWildcardUtil.escape(condition.get("levelCode")) + "%");
        }
        if(Strings.isNotBlank(condition.get("email"))) {
            hql.append("and entity.email like :email ");
            parameterMap.put("email", "%" + SQLWildcardUtil.escape(condition.get("email")) + "%");
        }
        if(Strings.isNotBlank(condition.get("gender"))) {
            hql.append("and entity.gender = :gender ");
            parameterMap.put("gender", Integer.valueOf(condition.get("gender")));
        }
        if(Strings.isNotBlank(condition.get("synState"))) {
            String synState = condition.get("synState");
            hql.append("and entity.syncState=:synState ");
            if("1".equals(synState)) {
                parameterMap.put("synState", SynOrgConstants.SYN_STATE_SUCCESS);
            } else if("-1".equals(synState)) {
                parameterMap.put("synState", SynOrgConstants.SYN_STATE_FAILURE);
            } else {
                parameterMap.put("synState", SynOrgConstants.SYN_STATE_NONE);
            }
        }
        hql.append("order by entity.code asc");
        List<Object[]> result = DBAgent.find(hql.toString(), parameterMap, flipInfo);
		return convert2SyncMemberData(result);
	}
	
	private List<SyncDataListVO> queryDept(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		return queryData(flipInfo, condition, "SynDepartment");
	}
	
	private List<SyncDataListVO> queryUnit(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		return queryData(flipInfo, condition, "SynUnit");
	}
	
	private List<SyncDataListVO> queryLevel(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		return queryData(flipInfo, condition, "SynLevel");
	}
	
	private List<SyncDataListVO> queryPost(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		return queryData(flipInfo, condition, "SynPost");
	}
	private List<SyncDataListVO> queryMember(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		return queryData(flipInfo, condition, "SynMember");
	}
	
	@SuppressWarnings("unchecked")
	private List<SyncDataListVO> queryData(FlipInfo flipInfo, Map<String, String> condition, String type) {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
        StringBuilder hql = new StringBuilder();
        String pcode = "";
        if("SynDepartment".equals(type)) {
        	hql.append("select entity.code, entity.name, entity.parentCode, entity.createDate, entity.syncState ");
        	 if(Strings.isNotBlank(condition.get("pcode"))) {
                 pcode = "and entity.parentCode like :pcode ";
                 parameterMap.put("pcode", "%" + SQLWildcardUtil.escape(condition.get("pcode")) + "%");
             }
        } else if("SynUnit".equals(type)){
        	hql.append("select entity.code, entity.name, entity.parentCode, entity.createDate, entity.syncState ");
       	 if(Strings.isNotBlank(condition.get("pcode"))) {
                pcode = "and entity.parentCode like :pcode ";
                parameterMap.put("pcode", "%" + SQLWildcardUtil.escape(condition.get("pcode")) + "%");
            }
        }else {
        	hql.append("select entity.code, entity.name, '--' as parentCode, entity.createDate, entity.syncState ");
        }
        hql.append("from " + type + " as entity where 1 = 1 ");
        hql.append(pcode);
        hql.append(parseCondition(condition, parameterMap));
        List<Object[]> result = DBAgent.find(hql.toString(), parameterMap, flipInfo);
		return convert2SyncData(result);
	}
	
	/**
	 * Description:
	 * <pre>实体转化成前台展示的数据</pre>
	 * @param result
	 * @return
	 */
	private List<SyncDataListVO> convert2SyncData(List<Object[]> result) {
        List<SyncDataListVO> models = new ArrayList<SyncDataListVO>();
        if(result != null && !result.isEmpty()) {
            for(int i = 0; i < result.size(); i++) {
            	SyncDataListVO vo = new SyncDataListVO();
            	// code name parentCode createDate synState
            	Object[] object = result.get(i);
                vo.setCode((String) object[0]);
                vo.setName((String)object[1]);
                // 上级编码
                vo.setParentCode((String)object[2]);
                // 创建时间
                vo.setCreateDate((Date)object[3]);
                // 同步状态
                if(SynOrgConstants.SYN_STATE_SUCCESS == ((Integer)object[4]).intValue()) {
                    vo.setSyncState("成功");
                } else if(SynOrgConstants.SYN_STATE_FAILURE == ((Integer)object[4]).intValue()){
                    vo.setSyncState("失败");
                } else {
                	vo.setSyncState("未同步");
                }
            	models.add(vo);
            }
        }
		return models;
	}
	
	private List<SyncMemberListVO> convert2SyncMemberData(List<Object[]> result) {
        List<SyncMemberListVO> models = new ArrayList<SyncMemberListVO>();
        if(result != null && !result.isEmpty()) {
            for(int i = 0; i < result.size(); i++) {
            	SyncMemberListVO vo = new SyncMemberListVO();
            	// code name parentCode createDate synState
            	Object[] object = result.get(i);
                vo.setCode((String) object[0]);
                vo.setName((String)object[1]);
                vo.setLoginName((String) object[2]);
                vo.setDeptCode((String) object[3]);
                vo.setPostCode((String) object[4]);
                vo.setLevelCode((String) object[5]);
                vo.setEmail((String) object[6]);
                vo.setTelNum((String) object[7]);
                try {
                	int gen = ((Integer) object[8]).intValue();
                	if(gen == 1) {
                		vo.setGender("男");
                	} else {
                		vo.setGender("女");
                	}
                } catch(Exception e) {
                	vo.setGender("");
                }
                // 同步状态
                if(SynOrgConstants.SYN_STATE_SUCCESS == ((Integer)object[9]).intValue()) {
                    vo.setSyncState("成功");
                } else if(SynOrgConstants.SYN_STATE_FAILURE == ((Integer)object[9]).intValue()){
                    vo.setSyncState("失败");
                } else {
                	vo.setSyncState("未同步");
                }
            	models.add(vo);
            }
        }
        return models;
	}
	
	private String parseCondition(Map<String, String> condition, Map<String, Object> parameterMap) {
		StringBuffer hql = new StringBuffer();
		if(Strings.isNotBlank(condition.get("synState"))) {
            String synState = condition.get("synState");
            hql.append("and entity.syncState=:synState ");
            if("1".equals(synState)) {
                parameterMap.put("synState", SynOrgConstants.SYN_STATE_SUCCESS);
            } else if("-1".equals(synState)) {
                parameterMap.put("synState", SynOrgConstants.SYN_STATE_FAILURE);
            } else {
                parameterMap.put("synState", SynOrgConstants.SYN_STATE_NONE);
            }
        }
        // 创建时间
        if(Strings.isNotBlank(condition.get("createDate"))) {
            String createDate = condition.get("createDate");
            String[] date = createDate.split("#");
            if(date != null && date.length > 0) {
                if(Strings.isNotBlank(date[0])) {
                    hql.append("and entity.createDate >= :timestamp1 ");
                    parameterMap.put("timestamp1", Datetimes.getTodayFirstTime(date[0]));
                }
                if(date.length > 1) {
                    if(Strings.isNotBlank(date[1])) {
                        hql.append("and entity.createDate <= :timestamp2 ");
                        parameterMap.put("timestamp2", Datetimes.getTodayLastTime(date[1]));
                    }
                }
            }
        }
        // 实体名称
        if(Strings.isNotBlank(condition.get("name"))) {
            hql.append("and entity.name like :name ");
            parameterMap.put("name", "%" + SQLWildcardUtil.escape(condition.get("name")) + "%");
        }
        // 实体编码
        if(Strings.isNotBlank(condition.get("code"))) {
            hql.append("and entity.code like :code ");
            parameterMap.put("code", "%" + SQLWildcardUtil.escape(condition.get("code")) + "%");
        }
        hql.append("order by entity.createDate desc, entity.syncState asc");
        return hql.toString();
	}
	
	
}
