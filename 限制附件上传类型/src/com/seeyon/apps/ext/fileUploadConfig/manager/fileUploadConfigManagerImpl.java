package com.seeyon.apps.ext.fileUploadConfig.manager;

import com.seeyon.apps.ext.fileUploadConfig.dao.fileUploadConfigDaoImpl;
import com.seeyon.apps.ext.fileUploadConfig.po.ZOrgUnit;
import com.seeyon.apps.ext.fileUploadConfig.po.ZOrgUploadMember;
import com.seeyon.apps.ext.fileUploadConfig.po.ZorgMember;
import com.seeyon.apps.ldap.event.OrganizationLdapEvent;
import com.seeyon.apps.ldap.util.LdapUtils;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.po.OrgMember;
import com.seeyon.ctp.organization.webmodel.WebV3xOrgMember;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.seeyon.apps.ext.fileUploadConfig.dao.fileUploadConfigDao;
import com.seeyon.ctp.common.AppContext;
import org.apache.velocity.runtime.directive.Foreach;

import java.util.*;


public class fileUploadConfigManagerImpl implements fileUploadConfigManager {
    private static final Log logger = LogFactory.getLog(fileUploadConfigManagerImpl.class);

    private fileUploadConfigDao demoDao = new fileUploadConfigDaoImpl();

    @Override
    public List<ZOrgUploadMember> selectAllUploadMem() {
        return demoDao.selectAllUploadMem();
    }

    @Override
    public void insertUploadMember(List<ZOrgUploadMember> zOrgUploadMember) {
        demoDao.deleteUploadMember();
        for (ZOrgUploadMember z : zOrgUploadMember) {
            z.setStatus("1");
            demoDao.insertUploadMember(z);
        }

    }

    @Override
    public ZOrgUploadMember selectUploadMemberByuserId(String userid) {
        return demoDao.selectUploadMemberByuserId(userid);
    }

    @Override
    public List<Map<String, Object>> getUnitByAccountId(Long accountId) {
        List<ZOrgUnit> all = demoDao.getUnitByAccountId(accountId);
        List<Map<String, Object>> parent = new LinkedList<>();
        for (ZOrgUnit orgUnit : all) {
            String path = orgUnit.getPath();
            int i = path.substring(8).length();
            Map<String, Object> map = new LinkedHashMap<>();
            if (i > 0) {
            } else {
                map.put("id", orgUnit.getId().toString());
                map.put("name", orgUnit.getName());
                map.put("path", orgUnit.getPath());
                map.put("orgAccountId", orgUnit.getOrgAccountId().toString());
                map.put("children", "");
                parent.add(map);
            }
        }
        //为一级菜单设置子菜单
        for (Map<String, Object> m : parent) {
            String idVal = (String) m.get("path");
            m.put("children", getChild(idVal, all, 2));
        }
        return parent;
    }

    public List<Map<String, Object>> getChild(String pid, List<ZOrgUnit> allList, int iteam) {
        final int len = 4;
        List<Map<String, Object>> child = new LinkedList<>();
        for (ZOrgUnit z : allList) {
            String path = z.getPath();
            if (path.length() > pid.length()) {
                int start = path.substring(iteam * len).length();
                int end = path.substring((iteam + 1) * len).length();
                if (start > 0 && end == 0) {
                    String pidOfChild = z.getPath().substring(0, iteam * len);
                    if (pid.equals(pidOfChild)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", z.getId().toString());
                        map.put("name", z.getName());
                        map.put("path", z.getPath());
                        map.put("orgAccountId", z.getOrgAccountId().toString());
                        map.put("children", "");
                        child.add(map);
                    }
                }
            }
        }
        for (Map<String, Object> zorg : child) {
            String path = (String) zorg.get("path");
            String p2 = path.substring(iteam * len);
            int isContinu = p2.length();
            if (isContinu > 0) {
                String p = (String) zorg.get("path");
                zorg.put("children", getChild(p, allList, p.length() / len));
            }
        }
        if (child.size() == 0) {
            return null;
        }
        return child;
    }

    @Override
    public List<ZorgMember> showPeople(Map<String, Object> params) throws BusinessException {

        /********过滤和条件搜索*******/
        Map queryParams = new HashMap<String, Object>();
        Boolean enabled = null;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = (String) entry.getValue();
            if ("name".equals(key)) {
                queryParams.put("name", value);
            }
            if ("departmentId".equals(key)) {
                queryParams.put("departmentId", value);
            }
            if ("loginName".equals(key)) {
                queryParams.put("loginName", value);
            }
        }
        List<ZorgMember> list = demoDao.getAllMemberPO_New(queryParams, true, true);
        return list;
    }

    @Override
    public int selectUnitPeopleCount() {
        return 0;
    }


}
