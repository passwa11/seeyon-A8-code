package com.seeyon.cap4.form.po;

import com.seeyon.ctp.common.po.BasePO;

import java.util.Date;

/**
 * This is an object that contains data related to the cap_print_template table.
 * <p>
 * 标签打印模板
 *
 * @hibernate.class table="cap_print_template"
 */
public class PrintTemplate extends BasePO {

    /**
     * 模板名称
     */
    private String _templateName;

    /**
     * 创建人
     */
    private Long _createId;

    /**
     * 创建时间
     */
    private Date _createDate;

    /**
     * 修改时间
     */
    private Date _modifyDate;

    /**
     * 模板内容
     */
    private String _content;

    /**
     * 类型 0:系统预制 1：用户创建
     */
    private Integer _type;

    /**
     * 状态 0：停用或者删除 1：启用
     */
    private Integer _state;

    public PrintTemplate(){
        initialize();
    }

    public PrintTemplate(Long _id){
        this.setId(_id);
        initialize();
    }

    protected void initialize() {
    }

    public String getTemplateName() {
        return _templateName;
    }

    public void setTemplateName(String _templateName) {
        this._templateName = _templateName;
    }

    public Long getCreateId() {
        return _createId;
    }

    public void setCreateId(Long _createId) {
        this._createId = _createId;
    }

    public Date getCreateDate() {
        return _createDate;
    }

    public void setCreateDate(Date _createDate) {
        this._createDate = _createDate;
    }

    public Date getModifyDate() {
        return _modifyDate;
    }

    public void setModifyDate(Date _modifyDate) {
        this._modifyDate = _modifyDate;
    }

    public String getContent() {
        return _content;
    }

    public void setContent(String _content) {
        this._content = _content;
    }

    public Integer getType() {
        return _type;
    }

    public void setType(Integer _type) {
        this._type = _type;
    }

    public Integer getState() {
        return _state;
    }

    public void setState(Integer _state) {
        this._state = _state;
    }
}
