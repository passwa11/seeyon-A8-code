package com.seeyon.apps.synorg.po;

import java.io.Serializable;
import java.util.Date;

/**
 * 人员中间表实体对象
 * @author Yang.Yinghai
 * @date 2015-8-18下午4:30:08
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SynMember implements Serializable {

    /** 序列化ID */
    private static final long serialVersionUID = -6991635654972889874L;

    /** 编号 */
    private String code;

    /** 姓名 */
    private String name;

    /** 登录名 */
    private String loginName;

    /** 编号 */
    private String password;

    /** 是否启用：0-停用 1-启用 */
    private Boolean enable = true;

    /** 排序号 */
    private Long sortId = 1L;

    /** 部门编号 */
    private String departmentCode;

    /** 岗位编号 */
    private String postCode;

    /** 职务级别编号 */
    private String levelCode;

    /** 描述 */
    private String description;

    /** 邮件地址(对应ORG_MEMBER表：EXT_ATTR_2) */
    private String email;

    /** 办公电话(对应ORG_MEMBER表：EXT_ATTR_3) */
    private String officeNum;

    /** 手机号码(对应ORG_MEMBER表：EXT_ATTR_1) */
    private String telNumber;

    /** 家庭住址(对应ORG_MEMBER表：EXT_ATTR_4) */
    private String address;

    /** 邮政编码(对应ORG_MEMBER表：EXT_ATTR_5) */
    private String postalCode;

    /** 个人网页(对应ORG_MEMBER表：EXT_ATTR_6) */
    private String website;

    /** 博客链接(对应ORG_MEMBER表：EXT_ATTR_7) */
    private String blog;

    /** 性别 1 - 男 2 - 女(对应ORG_MEMBER表：EXT_ATTR_11) */
    private Integer gender;

    /** 出生日期(对应ORG_MEMBER表：EXT_ATTR_21) */
    private Date birthday;

    /** 微博(对应ORG_MEMBER表：EXT_ATTR_31) */
    private String weibo;

    /** 微信(对应ORG_MEMBER表：EXT_ATTR_32) */
    private String weixin;

    /** 身份证号码(对应ORG_MEMBER表：EXT_ATTR_33) */
    private String idNum;

    /** 学位(对应ORG_MEMBER表：EXT_ATTR_34) */
    private String degree;

    /** 通信地址(对应ORG_MEMBER表：EXT_ATTR_35) */
    private String postAddress;

    /** 预留字段1 */
    private String extAttr1;

    /** 预留字段2 */
    private String extAttr2;

    /** 预留字段3 */
    private String extAttr3;

    /** 预留字段4 */
    private String extAttr4;

    /** 预留字段5 */
    private String extAttr5;

    /** 预留字段6 */
    private String extAttr6;

    /** 预留字段7 */
    private String extAttr7;

    /** 预留字段8 */
    private String extAttr8;

    /** 预留字段9 */
    private String extAttr9;

    /** 预留字段10 */
    private String extAttr10;

    /** 预留字段11 */
    private String extAttr11;

    /** 预留字段12 */
    private String extAttr12;

    /** 预留字段13 */
    private String extAttr13;

    /** 预留字段14 */
    private String extAttr14;

    /** 预留字段15 */
    private String extAttr15;

    /** 预留字段16 */
    private String extAttr16;

    /** 预留字段17 */
    private String extAttr17;

    /** 预留字段18 */
    private String extAttr18;

    /** 预留字段19 */
    private String extAttr19;

    /** 预留字段20 */
    private String extAttr20;

    /** 创建时间 */
    private Date createDate;

    /** 同步时间 */
    private Date syncDate;

    /** 同步状态 */
    private Integer syncState;

    /**
     * 获取name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * 设置name
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取code
     * @return code
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置code
     * @param code code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取password
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置password
     * @param password password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取enable
     * @return enable
     */
    public Boolean getEnable() {
        return enable;
    }

    /**
     * 设置enable
     * @param enable enable
     */
    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    /**
     * 获取sortId
     * @return sortId
     */
    public Long getSortId() {
        return sortId;
    }

    /**
     * 设置sortId
     * @param sortId sortId
     */
    public void setSortId(Long sortId) {
        this.sortId = sortId;
    }

    /**
     * 获取departmentCode
     * @return departmentCode
     */
    public String getDepartmentCode() {
        return departmentCode;
    }

    /**
     * 设置departmentCode
     * @param departmentCode departmentCode
     */
    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    /**
     * 获取postCode
     * @return postCode
     */
    public String getPostCode() {
        return postCode;
    }

    /**
     * 设置postCode
     * @param postCode postCode
     */
    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    /**
     * 获取levelCode
     * @return levelCode
     */
    public String getLevelCode() {
        return levelCode;
    }

    /**
     * 设置levelCode
     * @param levelCode levelCode
     */
    public void setLevelCode(String levelCode) {
        this.levelCode = levelCode;
    }

    /**
     * 获取description
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置description
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取email
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置email
     * @param email email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 获取officeNum
     * @return officeNum
     */
    public String getOfficeNum() {
        return officeNum;
    }

    /**
     * 设置officeNum
     * @param officeNum officeNum
     */
    public void setOfficeNum(String officeNum) {
        this.officeNum = officeNum;
    }

    /**
     * 获取telNumber
     * @return telNumber
     */
    public String getTelNumber() {
        return telNumber;
    }

    /**
     * 设置telNumber
     * @param telNumber telNumber
     */
    public void setTelNumber(String telNumber) {
        this.telNumber = telNumber;
    }

    /**
     * 获取address
     * @return address
     */
    public String getAddress() {
        return address;
    }

    /**
     * 设置address
     * @param address address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * 获取postalCode
     * @return postalCode
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * 设置postalCode
     * @param postalCode postalCode
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * 获取website
     * @return website
     */
    public String getWebsite() {
        return website;
    }

    /**
     * 设置website
     * @param website website
     */
    public void setWebsite(String website) {
        this.website = website;
    }

    /**
     * 获取blog
     * @return blog
     */
    public String getBlog() {
        return blog;
    }

    /**
     * 设置blog
     * @param blog blog
     */
    public void setBlog(String blog) {
        this.blog = blog;
    }

    /**
     * 获取gender
     * @return gender
     */
    public Integer getGender() {
        return gender;
    }

    /**
     * 设置gender
     * @param gender gender
     */
    public void setGender(Integer gender) {
        this.gender = gender;
    }

    /**
     * 获取birthday
     * @return birthday
     */
    public Date getBirthday() {
        return birthday;
    }

    /**
     * 设置birthday
     * @param birthday birthday
     */
    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    /**
     * 获取weibo
     * @return weibo
     */
    public String getWeibo() {
        return weibo;
    }

    /**
     * 设置weibo
     * @param weibo weibo
     */
    public void setWeibo(String weibo) {
        this.weibo = weibo;
    }

    /**
     * 获取weixin
     * @return weixin
     */
    public String getWeixin() {
        return weixin;
    }

    /**
     * 设置weixin
     * @param weixin weixin
     */
    public void setWeixin(String weixin) {
        this.weixin = weixin;
    }

    /**
     * 获取idNum
     * @return idNum
     */
    public String getIdNum() {
        return idNum;
    }

    /**
     * 设置idNum
     * @param idNum idNum
     */
    public void setIdNum(String idNum) {
        this.idNum = idNum;
    }

    /**
     * 获取degree
     * @return degree
     */
    public String getDegree() {
        return degree;
    }

    /**
     * 设置degree
     * @param degree degree
     */
    public void setDegree(String degree) {
        this.degree = degree;
    }

    /**
     * 获取postAddress
     * @return postAddress
     */
    public String getPostAddress() {
        return postAddress;
    }

    /**
     * 设置postAddress
     * @param postAddress postAddress
     */
    public void setPostAddress(String postAddress) {
        this.postAddress = postAddress;
    }

    /**
     * 获取extAttr1
     * @return extAttr1
     */
    public String getExtAttr1() {
        return extAttr1;
    }

    /**
     * 设置extAttr1
     * @param extAttr1 extAttr1
     */
    public void setExtAttr1(String extAttr1) {
        this.extAttr1 = extAttr1;
    }

    /**
     * 获取extAttr2
     * @return extAttr2
     */
    public String getExtAttr2() {
        return extAttr2;
    }

    /**
     * 设置extAttr2
     * @param extAttr2 extAttr2
     */
    public void setExtAttr2(String extAttr2) {
        this.extAttr2 = extAttr2;
    }

    /**
     * 获取extAttr3
     * @return extAttr3
     */
    public String getExtAttr3() {
        return extAttr3;
    }

    /**
     * 设置extAttr3
     * @param extAttr3 extAttr3
     */
    public void setExtAttr3(String extAttr3) {
        this.extAttr3 = extAttr3;
    }

    /**
     * 获取extAttr4
     * @return extAttr4
     */
    public String getExtAttr4() {
        return extAttr4;
    }

    /**
     * 设置extAttr4
     * @param extAttr4 extAttr4
     */
    public void setExtAttr4(String extAttr4) {
        this.extAttr4 = extAttr4;
    }

    /**
     * 获取extAttr5
     * @return extAttr5
     */
    public String getExtAttr5() {
        return extAttr5;
    }

    /**
     * 设置extAttr5
     * @param extAttr5 extAttr5
     */
    public void setExtAttr5(String extAttr5) {
        this.extAttr5 = extAttr5;
    }

    /**
     * 获取extAttr6
     * @return extAttr6
     */
    public String getExtAttr6() {
        return extAttr6;
    }

    /**
     * 设置extAttr6
     * @param extAttr6 extAttr6
     */
    public void setExtAttr6(String extAttr6) {
        this.extAttr6 = extAttr6;
    }

    /**
     * 获取extAttr7
     * @return extAttr7
     */
    public String getExtAttr7() {
        return extAttr7;
    }

    /**
     * 设置extAttr7
     * @param extAttr7 extAttr7
     */
    public void setExtAttr7(String extAttr7) {
        this.extAttr7 = extAttr7;
    }

    /**
     * 获取extAttr8
     * @return extAttr8
     */
    public String getExtAttr8() {
        return extAttr8;
    }

    /**
     * 设置extAttr8
     * @param extAttr8 extAttr8
     */
    public void setExtAttr8(String extAttr8) {
        this.extAttr8 = extAttr8;
    }

    /**
     * 获取extAttr9
     * @return extAttr9
     */
    public String getExtAttr9() {
        return extAttr9;
    }

    /**
     * 设置extAttr9
     * @param extAttr9 extAttr9
     */
    public void setExtAttr9(String extAttr9) {
        this.extAttr9 = extAttr9;
    }

    /**
     * 获取extAttr10
     * @return extAttr10
     */
    public String getExtAttr10() {
        return extAttr10;
    }

    /**
     * 设置extAttr10
     * @param extAttr10 extAttr10
     */
    public void setExtAttr10(String extAttr10) {
        this.extAttr10 = extAttr10;
    }

    /**
     * 获取extAttr11
     * @return extAttr11
     */
    public String getExtAttr11() {
        return extAttr11;
    }

    /**
     * 设置extAttr11
     * @param extAttr11 extAttr11
     */
    public void setExtAttr11(String extAttr11) {
        this.extAttr11 = extAttr11;
    }

    /**
     * 获取extAttr12
     * @return extAttr12
     */
    public String getExtAttr12() {
        return extAttr12;
    }

    /**
     * 设置extAttr12
     * @param extAttr12 extAttr12
     */
    public void setExtAttr12(String extAttr12) {
        this.extAttr12 = extAttr12;
    }

    /**
     * 获取extAttr13
     * @return extAttr13
     */
    public String getExtAttr13() {
        return extAttr13;
    }

    /**
     * 设置extAttr13
     * @param extAttr13 extAttr13
     */
    public void setExtAttr13(String extAttr13) {
        this.extAttr13 = extAttr13;
    }

    /**
     * 获取extAttr14
     * @return extAttr14
     */
    public String getExtAttr14() {
        return extAttr14;
    }

    /**
     * 设置extAttr14
     * @param extAttr14 extAttr14
     */
    public void setExtAttr14(String extAttr14) {
        this.extAttr14 = extAttr14;
    }

    /**
     * 获取extAttr15
     * @return extAttr15
     */
    public String getExtAttr15() {
        return extAttr15;
    }

    /**
     * 设置extAttr15
     * @param extAttr15 extAttr15
     */
    public void setExtAttr15(String extAttr15) {
        this.extAttr15 = extAttr15;
    }

    /**
     * 获取extAttr16
     * @return extAttr16
     */
    public String getExtAttr16() {
        return extAttr16;
    }

    /**
     * 设置extAttr16
     * @param extAttr16 extAttr16
     */
    public void setExtAttr16(String extAttr16) {
        this.extAttr16 = extAttr16;
    }

    /**
     * 获取extAttr17
     * @return extAttr17
     */
    public String getExtAttr17() {
        return extAttr17;
    }

    /**
     * 设置extAttr17
     * @param extAttr17 extAttr17
     */
    public void setExtAttr17(String extAttr17) {
        this.extAttr17 = extAttr17;
    }

    /**
     * 获取extAttr18
     * @return extAttr18
     */
    public String getExtAttr18() {
        return extAttr18;
    }

    /**
     * 设置extAttr18
     * @param extAttr18 extAttr18
     */
    public void setExtAttr18(String extAttr18) {
        this.extAttr18 = extAttr18;
    }

    /**
     * 获取extAttr19
     * @return extAttr19
     */
    public String getExtAttr19() {
        return extAttr19;
    }

    /**
     * 设置extAttr19
     * @param extAttr19 extAttr19
     */
    public void setExtAttr19(String extAttr19) {
        this.extAttr19 = extAttr19;
    }

    /**
     * 获取extAttr20
     * @return extAttr20
     */
    public String getExtAttr20() {
        return extAttr20;
    }

    /**
     * 设置extAttr20
     * @param extAttr20 extAttr20
     */
    public void setExtAttr20(String extAttr20) {
        this.extAttr20 = extAttr20;
    }

    /**
     * 获取loginName
     * @return loginName
     */
    public String getLoginName() {
        return loginName;
    }

    /**
     * 设置loginName
     * @param loginName loginName
     */
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    /**
     * 获取createDate
     * @return createDate
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * 设置createDate
     * @param createDate createDate
     */
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /**
     * 获取syncDate
     * @return syncDate
     */
    public Date getSyncDate() {
        return syncDate;
    }

    /**
     * 设置syncDate
     * @param syncDate syncDate
     */
    public void setSyncDate(Date syncDate) {
        this.syncDate = syncDate;
    }

    /**
     * 获取syncState
     * @return syncState
     */
    public Integer getSyncState() {
        return syncState;
    }

    /**
     * 设置syncState
     * @param syncState syncState
     */
    public void setSyncState(Integer syncState) {
        this.syncState = syncState;
    }
}
