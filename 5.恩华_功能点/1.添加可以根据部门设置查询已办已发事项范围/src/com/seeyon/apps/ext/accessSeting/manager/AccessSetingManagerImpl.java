package com.seeyon.apps.ext.accessSeting.manager;

import com.seeyon.apps.ext.accessSeting.dao.AccessSetingDao;
import com.seeyon.apps.ext.accessSeting.dao.AccessSetingDaoImpl;
import com.seeyon.apps.ext.accessSeting.po.DepartmentViewTimeRange;

import java.math.BigDecimal;
import java.util.*;

public class AccessSetingManagerImpl implements AccessSetingManager {

    private AccessSetingDao dao = new AccessSetingDaoImpl();

    @Override
    public void saveDepartmentViewTimeRange(DepartmentViewTimeRange range) {
        dao.saveDepartmentViewTimeRange(range);
    }

    @Override
    public void updateDepartmentViewTimeRange(DepartmentViewTimeRange range) {
        dao.updateDepartmentViewTimeRange(range);
    }

    @Override
    public List<DepartmentViewTimeRange> getDepartmentViewTimeRange(Map<String, Object> range) {
        return dao.getDepartmentViewTimeRange(range);
    }

    @Override
    public List<Map<String, Object>> queryAllUnit(Long accountId) {
        List<Map<String, Object>> deptList = dao.queryAllDepartment(accountId);
        List<Map<String, Object>> parent = new LinkedList<>();
        for (int i = 0; i < deptList.size(); i++) {
            BigDecimal id = (BigDecimal) deptList.get(i).get("id");
            BigDecimal orgaccountId = (BigDecimal) deptList.get(i).get("org_account_id");
            if (id.longValue() == orgaccountId.longValue()) {
                Map<String, Object> m = dealMapVal(deptList.get(i));
                parent.add(m);
            }
        }
        for (Map<String, Object> m : parent) {
            String idVal = (String) m.get("path");
            m.put("children", getChild(idVal, deptList));
        }
        return parent;
    }

    public List<Map<String, Object>> getChild(String pid, List<Map<String, Object>> allList) {
        final int len = pid.length();
        List<Map<String, Object>> child = new LinkedList<>();
        for (int i = 0; i < allList.size(); i++) {
            String chPath = (String) allList.get(i).get("path");
            //截取到父级path的长度
            if (chPath.length() > len) {
                String subAfter = chPath.substring(0, len);
                if (pid.equals(subAfter) && chPath.length() == (len + 4)) {
                    Map<String, Object> map = dealMapVal(allList.get(i));
                    child.add(map);
                }
            }
        }

        for (Map<String, Object> map : child) {
            String path = (String) map.get("path");
            map.put("children", getChild(path, allList));
        }

        return child;
    }

    public Map<String, Object> dealMapVal(Map<String, Object> map) {
        Map<String, Object> m_ = new HashMap<>();
        m_.put("id", map.get("id").toString() + "");
        m_.put("name", map.get("name").toString() + "");
        m_.put("path", map.get("path").toString() + "");
        m_.put("orgAccountId", map.get("org_account_id").toString() + "");
        m_.put("children", "");
        return m_;
    }

}
