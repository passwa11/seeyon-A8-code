package com.seeyon.cap4.form.manager;

import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormTableBean;
import com.seeyon.cap4.form.po.PrintTemplate;
import com.seeyon.ctp.common.exceptions.BusinessException;

import java.sql.SQLException;
import java.util.List;

public interface PrintTemplateManager {
    public List<PrintTemplate> selectAllTemplate();

    public PrintTemplate selectTemplateById(Long id);
    /**
     * 通过ID获取表单主表信息列表
     *
     * @param id        主表数据ID
     * @param tableBean 所属的表对象
     * @param fields    字段名数组
     * @return List<FormDataMasterBean> 不包含从表内容
     * @throws SQLException SQL异常
     */
    List<FormDataMasterBean> selectMasterDataById(Long[] id, FormTableBean tableBean, String[] fields) throws BusinessException, SQLException;

}
