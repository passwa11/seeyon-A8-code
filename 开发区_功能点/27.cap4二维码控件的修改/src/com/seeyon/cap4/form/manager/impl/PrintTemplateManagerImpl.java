package com.seeyon.cap4.form.manager.impl;

import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormTableBean;
import com.seeyon.cap4.form.dao.PrintTemplateDao;
import com.seeyon.cap4.form.manager.PrintTemplateManager;
import com.seeyon.cap4.form.modules.engin.base.formData.CAP4FormDataDAO;
import com.seeyon.cap4.form.po.PrintTemplate;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;

import java.sql.SQLException;
import java.util.List;

public class PrintTemplateManagerImpl implements PrintTemplateManager {

    private PrintTemplateDao printTemplateDao;
    private CAP4FormDataDAO cap4FormDataDAO;

    public CAP4FormDataDAO getCap4FormDataDAO() {
        return cap4FormDataDAO;
    }

    public void setCap4FormDataDAO(CAP4FormDataDAO cap4FormDataDAO) {
        this.cap4FormDataDAO = cap4FormDataDAO;
    }

    public PrintTemplateDao getPrintTemplateDao() {
        return printTemplateDao;
    }

    public void setPrintTemplateDao(PrintTemplateDao printTemplateDao) {
        this.printTemplateDao = printTemplateDao;
    }

    @Override
    public List<PrintTemplate> selectAllTemplate() {
        return printTemplateDao.selectAllTemplate();
    }

    @Override
    public PrintTemplate selectTemplateById(Long id) {
        return printTemplateDao.selectById(id);
    }


    @Override
    public List<FormDataMasterBean> selectMasterDataById(Long[] ids, FormTableBean tableBean, String[] fields) throws BusinessException, SQLException {
        return cap4FormDataDAO.selectMasterDataById(ids,tableBean,fields,false);
    }
}
