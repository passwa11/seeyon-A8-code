package com.seeyon.apps.ext.kydx.util;


import com.seeyon.apps.ext.kydx.po.OrgDept;

import java.util.ArrayList;
import java.util.List;

public class TreeUtil {


    public static List<OrgDept> getRootList(List<OrgDept> list) {
        List<OrgDept> rootlist = new ArrayList<>();
        List<OrgDept> children = null;
        List<String> arrList = new ArrayList<>();
        for (OrgDept m : list) {
            arrList.add(m.getDeptCode());
        }
        for (int i = 0; i < list.size(); i++) {
            OrgDept menu = list.get(i);
            String pid = menu.getDeptParentId();
            boolean flag = arrList.contains(pid);
            if (!flag) {
                children = getChildList(menu.getDeptCode(), list);
                menu.setList(children);
                rootlist.add(menu);
            }

        }
        return rootlist;
    }


    public static List<OrgDept> getChildList(String parentId, List<OrgDept> list) {
        List<OrgDept> child = new ArrayList<>();
        for (OrgDept m : list) {
            if (null != m.getDeptParentId() && m.getDeptParentId() != "000000") {
                if (m.getDeptParentId().equals(parentId)) {
                    child.add(m);
                }
            }
        }

        for (OrgDept mu : child) {
            mu.setList(getChildList(mu.getDeptCode(), list));
        }
        return child;
    }
}
