package com.seeyon.apps.ext.zxzyk.util;

import com.seeyon.apps.ext.zxzyk.po.OrgDept;

import java.util.ArrayList;
import java.util.List;

public class TreeUtil {


    public static List<OrgDept> getRootList(List<OrgDept> list) {
        List<OrgDept> rootlist = new ArrayList<>();
        List<OrgDept> children = null;
        List<String> arrList = new ArrayList<>();
        for (OrgDept m : list) {
            arrList.add(m.getDeptcode());
        }
        for (int i = 0; i < list.size(); i++) {
            OrgDept menu = list.get(i);
            String pid = menu.getSuperior();
            boolean flag = arrList.contains(pid);
            if (!flag) {
                children = getChildList(menu.getDeptcode(), list);
                menu.setList(children);
                rootlist.add(menu);
            }

        }
        return rootlist;
    }


    public static List<OrgDept> getChildList(String parentId, List<OrgDept> list) {
        List<OrgDept> child = new ArrayList<>();
        for (OrgDept m : list) {
            if (null != m.getSuperior() && m.getSuperior() != "0") {
                if (m.getSuperior().equals(parentId)) {
                    child.add(m);
                }
            }
        }

        for (OrgDept mu : child) {
            mu.setList(getChildList(mu.getDeptcode(), list));
        }
        return child;
    }
}
