package com.seeyon.cap4.form.dao;

import com.seeyon.cap4.form.po.PrintTemplate;

import java.util.List;

public interface PrintTemplateDao {
    public List<PrintTemplate> selectAllTemplate();

    public PrintTemplate selectById(Long id);
}
