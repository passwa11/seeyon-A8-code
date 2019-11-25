package com.seeyon.ctp.organization.po;

import com.seeyon.ctp.common.po.BasePO;
import com.seeyon.ctp.organization.OrgConstants;


/**
 * This is an object that contains data related to the ORG_MEMBER table.
 *
 *  人 
 *
 * @hibernate.class
 *  table="ORG_MEMBER"
 */
public class OrgMember extends BasePO {

/*[IDTCODE MARKER BEGIN]*/


  //
  /***
   * 创建263邮箱开关
   */
  private Boolean _create263;
  // fields
  /**
   *  姓名 
   */
  private java.lang.String _name;
  /**
   *  编号 
   */
  private java.lang.String _code;
  /**
   *  是否是内部 
   */
  private java.lang.Boolean _internal = false;
  /**
   *  是否可以登录 
   */
  private java.lang.Boolean _loginable = false;
  /**
   *  是否是虚拟账号 
   */
  private java.lang.Boolean _virtual = false;
  /**
   *  是否是管理员 
   */
  private java.lang.Boolean _admin = false;
  /**
   *  是否被分配 
   */
  private java.lang.Boolean _assigned = false;
  /**
   *  枚举：正式/非正式/.. 
   */
  private java.lang.Integer _type;
  /**
   *  在职/离职 
   */
  private java.lang.Integer _state;
  /**
   *  是否启用 
   */
  private java.lang.Boolean _enable = false;
  /**
   *  是否被删除 
   */
  private java.lang.Boolean _deleted = false;
  /**
   *  枚举：在职/离职/.. 
   */
  private java.lang.Integer _status;
  /**
   *  排序 
   */
  private java.lang.Long _sortId;
  /**
   *  主岗部门 
   */
  private java.lang.Long _orgDepartmentId;
  /**
   *  主岗岗位 
   */
  private java.lang.Long _orgPostId;
  /**
   *  主岗职务级别 
   */
  private java.lang.Long _orgLevelId;
  /**
   *  主岗单位 
   */
  private java.lang.Long _orgAccountId;
  /**
   *  描述 
   */
  private java.lang.String _description;
  
  /**
   * V-Join元素类型
   */
  private Integer externalType  = OrgConstants.ExternalType.Inner.ordinal();
  /**
   *  创建时间 
   */
  private java.util.Date _createTime;
  /**
   *  更新时间 
   */
  private java.util.Date _updateTime;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr1;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr2;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr3;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr4;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr5;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr6;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr7;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr8;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr9;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr10;
  /**
   *  预留字段 
   */
  private java.lang.Integer _extAttr11;
  /**
   *  预留字段 
   */
  private java.lang.Integer _extAttr12;
  /**
   *  预留字段 
   */
  private java.lang.Integer _extAttr13;
  /**
   *  预留字段 
   */
  private java.lang.Integer _extAttr14;
  /**
   *  预留字段 
   */
  private java.lang.Integer _extAttr15;
  /**
   *  预留字段 
   */
  private java.lang.Long _extAttr16;
  /**
   *  预留字段 
   */
  private java.lang.Long _extAttr17;
  /**
   *  预留字段 
   */
  private java.lang.Long _extAttr18;
  /**
   *  预留字段 
   */
  private java.lang.Long _extAttr19;
  /**
   *  预留字段 
   */
  private java.lang.Long _extAttr20;
  /**
   *  预留字段 
   */
  private java.util.Date _extAttr21;
  /**
   *  预留字段 
   */
  private java.util.Date _extAttr22;
  /**
   *  预留字段 
   */
  private java.util.Date _extAttr23;
  /**
   *  预留字段 
   */
  private java.util.Date _extAttr24;
  /**
   *  预留字段 
   */
  private java.util.Date _extAttr25;
  /**
   *  预留字段 
   */
  private java.util.Date _extAttr26;
  /**
   *  预留字段 
   */
  private java.util.Date _extAttr27;
  /**
   *  预留字段 
   */
  private java.util.Date _extAttr28;
  /**
   *  预留字段 
   */
  private java.util.Date _extAttr29;
  /**
   *  预留字段 
   */
  private java.util.Date _extAttr30;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr31;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr32;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr33;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr34;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr35;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr36;
  /**
   *  预留字段 
   */
  private java.lang.Long _extAttr37;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr38;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr39;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr40;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr41;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr42;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr43;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr44;
  /**
   *  预留字段 
   */
  private java.lang.String _extAttr45;


  // constructors
  public OrgMember () {
    initialize();
  }

  /**
   * Constructor for primary key
   */
  public OrgMember (java.lang.Long _id) {
    this.setId(_id);
    initialize();
  }

  protected void initialize () {}


  public Boolean getCreate263() {
    return this._create263;
  }

  public void setCreate263(Boolean _create263) {
    this._create263 = _create263;
  }

  /**
   *  姓名 
   */
  public java.lang.String getName () {
    return _name;
  }

  /**
   *  姓名 
   * @param _name the NAME value
   */
  public void setName (java.lang.String _name) {
    this._name = _name;
  }


  /**
   *  编号 
   */
  public java.lang.String getCode () {
    return _code;
  }

  /**
   *  编号 
   * @param _code the CODE value
   */
  public void setCode (java.lang.String _code) {
    this._code = _code;
  }


  /**
   *  是否是内部 
   */
  public java.lang.Boolean isInternal () {
    return _internal;
  }

  /**
   *  是否是内部 
   * @param _internal the IS_INTERNAL value
   */
  public void setInternal (java.lang.Boolean _internal) {
    this._internal = _internal;
  }


  /**
   *  是否可以登录 
   */
  public java.lang.Boolean isLoginable () {
    return _loginable;
  }

  /**
   *  是否可以登录 
   * @param _loginable the IS_LOGINABLE value
   */
  public void setLoginable (java.lang.Boolean _loginable) {
    this._loginable = _loginable;
  }


  /**
   *  是否是虚拟账号 
   */
  public java.lang.Boolean isVirtual () {
    return _virtual;
  }

  /**
   *  是否是虚拟账号 
   * @param _virtual the IS_VIRTUAL value
   */
  public void setVirtual (java.lang.Boolean _virtual) {
    this._virtual = _virtual;
  }


  /**
   *  是否是管理员 
   */
  public java.lang.Boolean isAdmin () {
    return _admin;
  }

  /**
   *  是否是管理员 
   * @param _admin the IS_ADMIN value
   */
  public void setAdmin (java.lang.Boolean _admin) {
    this._admin = _admin;
  }


  /**
   *  是否被分配 
   */
  public java.lang.Boolean isAssigned () {
    return _assigned;
  }

  /**
   *  是否被分配 
   * @param _assigned the IS_ASSIGNED value
   */
  public void setAssigned (java.lang.Boolean _assigned) {
    this._assigned = _assigned;
  }


  /**
   *  枚举：正式/非正式/.. 
   */
  public java.lang.Integer getType () {
    return _type;
  }

  /**
   *  枚举：正式/非正式/.. 
   * @param _type the TYPE value
   */
  public void setType (java.lang.Integer _type) {
    this._type = _type;
  }


  /**
   *  在职/离职 
   */
  public java.lang.Integer getState () {
    return _state;
  }

  /**
   *  在职/离职 
   * @param _state the STATE value
   */
  public void setState (java.lang.Integer _state) {
    this._state = _state;
  }


  /**
   *  是否启用 
   */
  public java.lang.Boolean isEnable () {
    return _enable;
  }

  /**
   *  是否启用 
   * @param _enable the IS_ENABLE value
   */
  public void setEnable (java.lang.Boolean _enable) {
    this._enable = _enable;
  }


  /**
   *  是否被删除 
   */
  public java.lang.Boolean isDeleted () {
    return _deleted;
  }

  /**
   *  是否被删除 
   * @param _deleted the IS_DELETED value
   */
  public void setDeleted (java.lang.Boolean _deleted) {
    this._deleted = _deleted;
  }


  /**
   *  枚举：在职/离职/.. 
   */
  public java.lang.Integer getStatus () {
    return _status;
  }

  /**
   *  枚举：在职/离职/.. 
   * @param _status the STATUS value
   */
  public void setStatus (java.lang.Integer _status) {
    this._status = _status;
  }


  /**
   *  排序 
   */
  public java.lang.Long getSortId () {
    return _sortId;
  }

  /**
   *  排序 
   * @param _sortId the SORT_ID value
   */
  public void setSortId (java.lang.Long _sortId) {
    this._sortId = _sortId;
  }


  /**
   *  主岗部门 
   */
  public java.lang.Long getOrgDepartmentId () {
    return _orgDepartmentId;
  }

  /**
   *  主岗部门 
   * @param _orgDepartmentId the ORG_DEPARTMENT_ID value
   */
  public void setOrgDepartmentId (java.lang.Long _orgDepartmentId) {
    this._orgDepartmentId = _orgDepartmentId;
  }


  /**
   *  主岗岗位 
   */
  public java.lang.Long getOrgPostId () {
    return _orgPostId;
  }

  /**
   *  主岗岗位 
   * @param _orgPostId the ORG_POST_ID value
   */
  public void setOrgPostId (java.lang.Long _orgPostId) {
    this._orgPostId = _orgPostId;
  }


  /**
   *  主岗职务级别 
   */
  public java.lang.Long getOrgLevelId () {
    return _orgLevelId;
  }

  /**
   *  主岗职务级别 
   * @param _orgLevelId the ORG_LEVEL_ID value
   */
  public void setOrgLevelId (java.lang.Long _orgLevelId) {
    this._orgLevelId = _orgLevelId;
  }


  /**
   *  主岗单位 
   */
  public java.lang.Long getOrgAccountId () {
    return _orgAccountId;
  }

  /**
   *  主岗单位 
   * @param _orgAccountId the ORG_ACCOUNT_ID value
   */
  public void setOrgAccountId (java.lang.Long _orgAccountId) {
    this._orgAccountId = _orgAccountId;
  }


  /**
   *  描述 
   */
  public java.lang.String getDescription () {
    return _description;
  }

  /**
   *  描述 
   * @param _description the DESCRIPTION value
   */
  public void setDescription (java.lang.String _description) {
    this._description = _description;
  }


  /**
   *  创建时间 
   */
  public java.util.Date getCreateTime () {
    return _createTime;
  }

  /**
   *  创建时间 
   * @param _createTime the CREATE_TIME value
   */
  public void setCreateTime (java.util.Date _createTime) {
    this._createTime = _createTime;
  }


  /**
   *  更新时间 
   */
  public java.util.Date getUpdateTime () {
    return _updateTime;
  }

  /**
   *  更新时间 
   * @param _updateTime the UPDATE_TIME value
   */
  public void setUpdateTime (java.util.Date _updateTime) {
    this._updateTime = _updateTime;
  }


  /**
   *  预留字段 
   */
  public java.lang.String getExtAttr1 () {
    return _extAttr1;
  }

  /**
   *  预留字段 
   * @param _extAttr1 the EXT_ATTR_1 value
   */
  public void setExtAttr1 (java.lang.String _extAttr1) {
    this._extAttr1 = _extAttr1;
  }


  /**
   *  预留字段 
   */
  public java.lang.String getExtAttr2 () {
    return _extAttr2;
  }

  /**
   *  预留字段 
   * @param _extAttr2 the EXT_ATTR_2 value
   */
  public void setExtAttr2 (java.lang.String _extAttr2) {
    this._extAttr2 = _extAttr2;
  }


  /**
   *  预留字段 
   */
  public java.lang.String getExtAttr3 () {
    return _extAttr3;
  }

  /**
   *  预留字段 
   * @param _extAttr3 the EXT_ATTR_3 value
   */
  public void setExtAttr3 (java.lang.String _extAttr3) {
    this._extAttr3 = _extAttr3;
  }


  /**
   *  预留字段 
   */
  public java.lang.String getExtAttr4 () {
    return _extAttr4;
  }

  /**
   *  预留字段 
   * @param _extAttr4 the EXT_ATTR_4 value
   */
  public void setExtAttr4 (java.lang.String _extAttr4) {
    this._extAttr4 = _extAttr4;
  }


  /**
   *  预留字段 
   */
  public java.lang.String getExtAttr5 () {
    return _extAttr5;
  }

  /**
   *  预留字段 
   * @param _extAttr5 the EXT_ATTR_5 value
   */
  public void setExtAttr5 (java.lang.String _extAttr5) {
    this._extAttr5 = _extAttr5;
  }


  /**
   *  预留字段 
   */
  public java.lang.String getExtAttr6 () {
    return _extAttr6;
  }

  /**
   *  预留字段 
   * @param _extAttr6 the EXT_ATTR_6 value
   */
  public void setExtAttr6 (java.lang.String _extAttr6) {
    this._extAttr6 = _extAttr6;
  }


  /**
   *  预留字段 
   */
  public java.lang.String getExtAttr7 () {
    return _extAttr7;
  }

  /**
   *  预留字段 
   * @param _extAttr7 the EXT_ATTR_7 value
   */
  public void setExtAttr7 (java.lang.String _extAttr7) {
    this._extAttr7 = _extAttr7;
  }


  /**
   *  预留字段 
   */
  public java.lang.String getExtAttr8 () {
    return _extAttr8;
  }

  /**
   *  预留字段 
   * @param _extAttr8 the EXT_ATTR_8 value
   */
  public void setExtAttr8 (java.lang.String _extAttr8) {
    this._extAttr8 = _extAttr8;
  }


  /**
   *  预留字段 
   */
  public java.lang.String getExtAttr9 () {
    return _extAttr9;
  }

  /**
   *  预留字段 
   * @param _extAttr9 the EXT_ATTR_9 value
   */
  public void setExtAttr9 (java.lang.String _extAttr9) {
    this._extAttr9 = _extAttr9;
  }


  /**
   *  预留字段 
   */
  public java.lang.String getExtAttr10 () {
    return _extAttr10;
  }

  /**
   *  预留字段 
   * @param _extAttr10 the EXT_ATTR_10 value
   */
  public void setExtAttr10 (java.lang.String _extAttr10) {
    this._extAttr10 = _extAttr10;
  }


  /**
   *  预留字段 
   */
  public java.lang.Integer getExtAttr11 () {
    return _extAttr11;
  }

  /**
   *  预留字段 
   * @param _extAttr11 the EXT_ATTR_11 value
   */
  public void setExtAttr11 (java.lang.Integer _extAttr11) {
    this._extAttr11 = _extAttr11;
  }


  /**
   *  预留字段 
   */
  public java.lang.Integer getExtAttr12 () {
    return _extAttr12;
  }

  /**
   *  预留字段 
   * @param _extAttr12 the EXT_ATTR_12 value
   */
  public void setExtAttr12 (java.lang.Integer _extAttr12) {
    this._extAttr12 = _extAttr12;
  }


  /**
   *  预留字段 
   */
  public java.lang.Integer getExtAttr13 () {
    return _extAttr13;
  }

  /**
   *  预留字段 
   * @param _extAttr13 the EXT_ATTR_13 value
   */
  public void setExtAttr13 (java.lang.Integer _extAttr13) {
    this._extAttr13 = _extAttr13;
  }


  /**
   *  预留字段 
   */
  public java.lang.Integer getExtAttr14 () {
    return _extAttr14;
  }

  /**
   *  预留字段 
   * @param _extAttr14 the EXT_ATTR_14 value
   */
  public void setExtAttr14 (java.lang.Integer _extAttr14) {
    this._extAttr14 = _extAttr14;
  }


  /**
   *  预留字段 
   */
  public java.lang.Integer getExtAttr15 () {
    return _extAttr15;
  }

  /**
   *  预留字段 
   * @param _extAttr15 the EXT_ATTR_15 value
   */
  public void setExtAttr15 (java.lang.Integer _extAttr15) {
    this._extAttr15 = _extAttr15;
  }


  /**
   *  预留字段 
   */
  public java.lang.Long getExtAttr16 () {
    return _extAttr16;
  }

  /**
   *  预留字段 
   * @param _extAttr16 the EXT_ATTR_16 value
   */
  public void setExtAttr16 (java.lang.Long _extAttr16) {
    this._extAttr16 = _extAttr16;
  }


  /**
   *  预留字段 
   */
  public java.lang.Long getExtAttr17 () {
    return _extAttr17;
  }

  /**
   *  预留字段 
   * @param _extAttr17 the EXT_ATTR_17 value
   */
  public void setExtAttr17 (java.lang.Long _extAttr17) {
    this._extAttr17 = _extAttr17;
  }


  /**
   *  预留字段 
   */
  public java.lang.Long getExtAttr18 () {
    return _extAttr18;
  }

  /**
   *  预留字段 
   * @param _extAttr18 the EXT_ATTR_18 value
   */
  public void setExtAttr18 (java.lang.Long _extAttr18) {
    this._extAttr18 = _extAttr18;
  }


  /**
   *  预留字段 
   */
  public java.lang.Long getExtAttr19 () {
    return _extAttr19;
  }

  /**
   *  预留字段 
   * @param _extAttr19 the EXT_ATTR_19 value
   */
  public void setExtAttr19 (java.lang.Long _extAttr19) {
    this._extAttr19 = _extAttr19;
  }


  /**
   *  预留字段 
   */
  public java.lang.Long getExtAttr20 () {
    return _extAttr20;
  }

  /**
   *  预留字段 
   * @param _extAttr20 the EXT_ATTR_20 value
   */
  public void setExtAttr20 (java.lang.Long _extAttr20) {
    this._extAttr20 = _extAttr20;
  }


  /**
   *  预留字段 
   */
  public java.util.Date getExtAttr21 () {
    return _extAttr21;
  }

  /**
   *  预留字段 
   * @param _extAttr21 the EXT_ATTR_21 value
   */
  public void setExtAttr21 (java.util.Date _extAttr21) {
    this._extAttr21 = _extAttr21;
  }


  /**
   *  预留字段 
   */
  public java.util.Date getExtAttr22 () {
    return _extAttr22;
  }

  /**
   *  预留字段 
   * @param _extAttr22 the EXT_ATTR_22 value
   */
  public void setExtAttr22 (java.util.Date _extAttr22) {
    this._extAttr22 = _extAttr22;
  }


  /**
   *  预留字段 
   */
  public java.util.Date getExtAttr23 () {
    return _extAttr23;
  }

  /**
   *  预留字段 
   * @param _extAttr23 the EXT_ATTR_23 value
   */
  public void setExtAttr23 (java.util.Date _extAttr23) {
    this._extAttr23 = _extAttr23;
  }


  /**
   *  预留字段 
   */
  public java.util.Date getExtAttr24 () {
    return _extAttr24;
  }

  /**
   *  预留字段 
   * @param _extAttr24 the EXT_ATTR_24 value
   */
  public void setExtAttr24 (java.util.Date _extAttr24) {
    this._extAttr24 = _extAttr24;
  }


  /**
   *  预留字段 
   */
  public java.util.Date getExtAttr25 () {
    return _extAttr25;
  }

  /**
   *  预留字段 
   * @param _extAttr25 the EXT_ATTR_25 value
   */
  public void setExtAttr25 (java.util.Date _extAttr25) {
    this._extAttr25 = _extAttr25;
  }


  /**
   *  预留字段 
   */
  public java.util.Date getExtAttr26 () {
    return _extAttr26;
  }

  /**
   *  预留字段 
   * @param _extAttr26 the EXT_ATTR_26 value
   */
  public void setExtAttr26 (java.util.Date _extAttr26) {
    this._extAttr26 = _extAttr26;
  }


  /**
   *  预留字段 
   */
  public java.util.Date getExtAttr27 () {
    return _extAttr27;
  }

  /**
   *  预留字段 
   * @param _extAttr27 the EXT_ATTR_27 value
   */
  public void setExtAttr27 (java.util.Date _extAttr27) {
    this._extAttr27 = _extAttr27;
  }


  /**
   *  预留字段 
   */
  public java.util.Date getExtAttr28 () {
    return _extAttr28;
  }

  /**
   *  预留字段 
   * @param _extAttr28 the EXT_ATTR_28 value
   */
  public void setExtAttr28 (java.util.Date _extAttr28) {
    this._extAttr28 = _extAttr28;
  }


  /**
   *  预留字段 
   */
  public java.util.Date getExtAttr29 () {
    return _extAttr29;
  }

  /**
   *  预留字段 
   * @param _extAttr29 the EXT_ATTR_29 value
   */
  public void setExtAttr29 (java.util.Date _extAttr29) {
    this._extAttr29 = _extAttr29;
  }


  /**
   *  预留字段 
   */
  public java.util.Date getExtAttr30 () {
    return _extAttr30;
  }

  /**
   *  预留字段 
   * @param _extAttr30 the EXT_ATTR_30 value
   */
  public void setExtAttr30 (java.util.Date _extAttr30) {
    this._extAttr30 = _extAttr30;
  }


  /**
   *  预留字段 
   */
  public java.lang.String getExtAttr31 () {
    return _extAttr31;
  }

  /**
   *  预留字段 
   * @param _extAttr31 the EXT_ATTR_31 value
   */
  public void setExtAttr31 (java.lang.String _extAttr31) {
    this._extAttr31 = _extAttr31;
  }


  /**
   *  预留字段 
   */
  public java.lang.String getExtAttr32 () {
    return _extAttr32;
  }

  /**
   *  预留字段 
   * @param _extAttr32 the EXT_ATTR_32 value
   */
  public void setExtAttr32 (java.lang.String _extAttr32) {
    this._extAttr32 = _extAttr32;
  }


  /**
   *  预留字段 
   */
  public java.lang.String getExtAttr33 () {
    return _extAttr33;
  }

  /**
   *  预留字段 
   * @param _extAttr33 the EXT_ATTR_33 value
   */
  public void setExtAttr33 (java.lang.String _extAttr33) {
    this._extAttr33 = _extAttr33;
  }


  /**
   *  预留字段 
   */
  public java.lang.String getExtAttr34 () {
    return _extAttr34;
  }

  /**
   *  预留字段 
   * @param _extAttr34 the EXT_ATTR_34 value
   */
  public void setExtAttr34 (java.lang.String _extAttr34) {
    this._extAttr34 = _extAttr34;
  }


  /**
   *  预留字段 
   */
  public java.lang.String getExtAttr35 () {
    return _extAttr35;
  }

  /**
   *  预留字段 
   * @param _extAttr35 the EXT_ATTR_35 value
   */
  public void setExtAttr35 (java.lang.String _extAttr35) {
    this._extAttr35 = _extAttr35;
  }
  
  /**
   *  预留字段 
   */
  public java.lang.String getExtAttr36 () {
    return _extAttr36;
  }

  /**
   *  预留字段 
   * @param _extAttr36 the EXT_ATTR_36 value
   */
  public void setExtAttr36 (java.lang.String _extAttr36) {
    this._extAttr36 = _extAttr36;
  }
  
  /**
   *  预留字段 
   */
  public java.lang.Long getExtAttr37 () {
    return _extAttr37;
  }

  /**
   *  预留字段 
   * @param _extAttr37 the EXT_ATTR_37 value
   */
  public void setExtAttr37 (java.lang.Long _extAttr37) {
    this._extAttr37 = _extAttr37;
  }

public Integer getExternalType() {
	if(externalType==null){
		externalType = OrgConstants.ExternalType.Inner.ordinal();
	}
	return externalType;
}

public void setExternalType(Integer externalType) {
	this.externalType = externalType;
}

/*[IDTCODE MARKER END]*/

}