package com.seeyon.apps.synorg.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.synorg.constants.SynOrgConstants;
import com.seeyon.apps.synorg.po.SynLog;
import com.seeyon.apps.synorg.vo.SynLogListVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;

/**
 * 日志管理实现类
 * @author Yang.Yinghai
 * @date 2015-8-18下午4:24:41
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SyncLogDaoImpl implements SyncLogDao {

    /**
     * {@inheritDoc}
     */
    @Override
    public void createAll(List<SynLog> logList) {
        DBAgent.saveAll(logList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<SynLogListVO> queryByCondition(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        StringBuilder hql = new StringBuilder();
        hql.append("select log.id, log.entityType, log.entityName, log.entityCode, log.synState, log.synType, log.synLog, log.synDate ");
        hql.append("from SynLog as log ");
        // 实体类型
        if(Strings.isNotBlank(condition.get("entityType"))) {
            hql.append("where log.entityType=:entityType ");
            parameterMap.put("entityType", condition.get("entityType"));
        } else {
            hql.append("where log.entityType in('Unit','Department','Post','Level','Member') ");
        }
        // 同步状态
        if(Strings.isNotBlank(condition.get("synState"))) {
            String synState = condition.get("synState");
            hql.append("and log.synState=:synState ");
            if("1".equals(synState)) {
                parameterMap.put("synState", SynOrgConstants.SYN_STATE_SUCCESS);
            } else {
                parameterMap.put("synState", SynOrgConstants.SYN_STATE_FAILURE);
            }
        }
        // 操作类型
        if(Strings.isNotBlank(condition.get("synType"))) {
            String synState = condition.get("synType");
            hql.append("and log.synType=:synType ");
            if("1".equals(synState)) {
                parameterMap.put("synType", SynOrgConstants.SYN_OPERATION_TYPE_CREATE);
            } else if("2".equals(synState)) {
                parameterMap.put("synType", SynOrgConstants.SYN_OPERATION_TYPE_UPDATE);
            } else if("3".equals(synState)) {
                parameterMap.put("synType", SynOrgConstants.SYN_OPERATION_TYPE_DELETE);
            }
        }
        // 同步时间
        if(Strings.isNotBlank(condition.get("synDate"))) {
            String createDate = condition.get("synDate");
            String[] date = createDate.split("#");
            if(date != null && date.length > 0) {
                if(Strings.isNotBlank(date[0])) {
                    hql.append("and log.synDate >= :timestamp1 ");
                    parameterMap.put("timestamp1", Datetimes.getTodayFirstTime(date[0]));
                }
                if(date.length > 1) {
                    if(Strings.isNotBlank(date[1])) {
                        hql.append("and log.synDate <= :timestamp2 ");
                        parameterMap.put("timestamp2", Datetimes.getTodayLastTime(date[1]));
                    }
                }
            }
        }
        // 实体名称
        if(Strings.isNotBlank(condition.get("entityName"))) {
            hql.append("and log.entityName like :entityName ");
            parameterMap.put("entityName", "%" + SQLWildcardUtil.escape(condition.get("entityName")) + "%");
        }
        // 实体编码
        if(Strings.isNotBlank(condition.get("entityCode"))) {
            hql.append("and log.entityCode like :entityCode ");
            parameterMap.put("entityCode", "%" + SQLWildcardUtil.escape(condition.get("entityCode")) + "%");
        }
        hql.append("order by log.synDate desc");
        List<Object[]> result = DBAgent.find(hql.toString(), parameterMap, flipInfo);
        return convert2SynLogListVO(result);
    }

    /**
     * @param object
     * @param vo
     */
    private List<SynLogListVO> convert2SynLogListVO(List<Object[]> result) {
        List<SynLogListVO> models = new ArrayList<SynLogListVO>();
        if(result != null && !result.isEmpty()) {
            for(int i = 0; i < result.size(); i++) {
                SynLogListVO vo = new SynLogListVO();
                Object[] object = result.get(i);
                for(int j = 0; j < object.length; j++) {
                    switch(j) {
                        case 0 :
                            vo.setId((Long)object[j]);
                            break;
                        case 1 :
                            // 设置实体类型
                            String entityType = (String)object[j];
                            if(SynOrgConstants.ORG_ENTITY_DEPARTMENT.equals(entityType)) {
                                vo.setEntityType("部门");
                            } else if(SynOrgConstants.ORG_ENTITY_UNIT.equals(entityType)) {
                                vo.setEntityType("单位");
                            } else if(SynOrgConstants.ORG_ENTITY_POST.equals(entityType)) {
                                vo.setEntityType("岗位");
                            } else if(SynOrgConstants.ORG_ENTITY_LEVEL.equals(entityType)) {
                                vo.setEntityType("职务");
                            } else {
                                vo.setEntityType("人员");
                            }
                            break;
                        case 2 :
                            // 实体名称
                            vo.setEntityName((String)object[j]);
                            break;
                        case 3 :
                            // 实体编码
                            vo.setEntityCode((String)object[j]);
                            break;
                        case 4 :
                            // 同步状态
                            if(SynOrgConstants.SYN_STATE_SUCCESS == ((Integer)object[j]).intValue()) {
                                vo.setSynState("成功");
                            } else {
                                vo.setSynState("失败");
                            }
                            break;
                        case 5 :
                            // 同步类型
                            if(SynOrgConstants.SYN_OPERATION_TYPE_CREATE == ((Integer)object[j]).intValue()) {
                                vo.setSynType("新增");
                            } else if(SynOrgConstants.SYN_OPERATION_TYPE_UPDATE == ((Integer)object[j]).intValue()) {
                                vo.setSynType("更新");
                            } else {
                                vo.setSynType("删除");
                            }
                            break;
                        case 6 :
                            vo.setSynLog((String)object[j]);
                            break;
                        case 7 :
                            vo.setSynDate((Date)object[j]);
                            break;
                    }
                }
                models.add(vo);
            }
        }
        return models;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAll() {
        DBAgent.bulkUpdate("delete from SynLog");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteSyncLogByIds(String ids) {
        DBAgent.bulkUpdate("delete from SynLog where id in(" + ids + ")");
    }
}
