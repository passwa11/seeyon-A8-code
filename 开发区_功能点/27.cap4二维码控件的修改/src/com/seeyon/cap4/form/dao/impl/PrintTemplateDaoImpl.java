package com.seeyon.cap4.form.dao.impl;

import com.seeyon.cap4.form.dao.PrintTemplateDao;
import com.seeyon.cap4.form.po.PrintTemplate;
import com.seeyon.ctp.util.DBAgent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrintTemplateDaoImpl implements PrintTemplateDao {

    @Override
    public List<PrintTemplate> selectAllTemplate() {
        return DBAgent.find("from PrintTemplate as c order by c.templateName asc");
    }

    @Override
    public PrintTemplate selectById(Long id) {
        String hql = "from PrintTemplate as c where c.id=:id";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        List<PrintTemplate> logos = DBAgent.find(hql, map);
        return logos.get(0);
    }
}
