package com.seeyon.cap4.form.bean.fieldCtrl.enums;

import com.seeyon.cap4.form.bean.FormFieldComEnum;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.groovy.util.Maps;

import java.io.Serializable;
import java.util.Map;

public class CreditQueryConstant {

    public static void main(String[] args) {

        System.out.println(JSONUtil.toJSONString(BusinessInfoEnum.NAME.toString()));
    }

    /**
     * 分类
     */
    public enum Category {
        PARTNER_INFO("partners", "股东信息", PartnerInfoEnum.class),
        BUSINESS_INFO("business_info", "工商信息", BusinessInfoEnum.class),
        KEY_PERSON_INFO("employees", "主要人员信息", KeyPersonEnum.class),
        BRANCH("branches", "分支信息", BranchEnum.class),
        BUSINESS_CHANGE("changerecords", "工商变更", BusinessChangeEnum.class),
        ABNORMAL("abnormal_items", "经营异常", AbnormalEnum.class);

        private String key;
        private String categoryName;
        private Class subEnum;

        Category(String key, String categoryName, Class subEnum) {
            this.key = key;
            this.categoryName = categoryName;
            this.subEnum = subEnum;
        }

        public Class getSubEnum() {
            return subEnum;
        }

        public void setSubEnum(Class subEnum) {
            this.subEnum = subEnum;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }
    }

    /**
     * 工商信息字段
     */
    public enum BusinessInfoEnum {
        ENTERPRISE_NUMBER("id", "企业编号", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        NAME("name", "公司名称", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        ECON_KIND("econ_kind", "企业类型", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        REGIST_CAPI("regist_capi", "注册资本", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        ADDRESS("address", "地址", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        REG_NO("reg_no", "企业注册号", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        SCOPE("scope", "经营范围", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        TERM_START("term_start", "营业开始日期", Enums.FieldType.TIMESTAMP, new String[]{FormFieldComEnum.EXTEND_DATE.getKey()}),
        TERM_END("term_end", "营业结束日期", Enums.FieldType.TIMESTAMP, new String[]{FormFieldComEnum.EXTEND_DATE.getKey()}),
        BELONG_ORG("belong_org", "所属工商局", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        OPER_NAME("oper_name", "法人", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        start_date("start_date", "成立日期", Enums.FieldType.TIMESTAMP, new String[]{FormFieldComEnum.EXTEND_DATE.getKey()}),
        END_DATE("end_date", "注销日期", Enums.FieldType.TIMESTAMP, new String[]{FormFieldComEnum.EXTEND_DATE.getKey()}),
        CHECK_DATE("check_date", "核准日期", Enums.FieldType.TIMESTAMP, new String[]{FormFieldComEnum.EXTEND_DATE.getKey()}),
        STATUS("status", "经营状态", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        ORG_NO("org_no", "组织机构号", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        CREDIT_NO("credit_no", "统一社会信用代码", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        PROVINCE("province", "省份缩写", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        PROVINCE1("province1", "省份", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        CITY("city", "城市", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        DOMAINS("domains", "行业", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()});
//        STATUS("status", "经营状态", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()});


        BusinessInfoEnum(String key, String fieldName, Enums.FieldType fieldType, String[] supportFieldType) {
            this.key = key;
            this.fieldName = fieldName;
            this.fieldType = fieldType;
            this.supportFieldType = supportFieldType;
        }

        private String key;
        private String fieldName;
        private Enums.FieldType fieldType;
        private String[] supportFieldType;


        @Override
        public String toString() {
            Map<String, Serializable> map = Maps.of("key", key, "fieldName", fieldName, "fieldType", fieldType.getKey(), "supportFieldType", supportFieldType);
            return JSONUtil.toJSONString(map);
        }
    }

    /**
     * 股东信息
     */
    public enum PartnerInfoEnum {
        PARTNER_NAME("name", "股东姓名", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        PARTNER_STOCK_TYPE("stock_type", "股东类型", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()});

        PartnerInfoEnum(String key, String fieldName, Enums.FieldType fieldType, String[] supportFieldType) {
            this.key = key;
            this.fieldName = fieldName;
            this.fieldType = fieldType;
            this.supportFieldType = supportFieldType;
        }

        private String key;
        private String fieldName;
        private Enums.FieldType fieldType;
        private String[] supportFieldType;

        @Override
        public String toString() {
            Map<String, Serializable> map = Maps.of("key", key, "fieldName", fieldName, "fieldType", fieldType.getKey(), "supportFieldType", supportFieldType);
            return JSONUtil.toJSONString(map);
        }
    }

    /**
     * 主要人员信息
     */
    public enum KeyPersonEnum {
        KEY_PERSON_JOB_TITLE("job_title", "主要人员职位", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        KEY_PERSON_NAME("name", "主要人员姓名", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()});

        KeyPersonEnum(String key, String fieldName, Enums.FieldType fieldType, String[] supportFieldType) {
            this.key = key;
            this.fieldName = fieldName;
            this.fieldType = fieldType;
            this.supportFieldType = supportFieldType;
        }

        private String key;
        private String fieldName;
        private Enums.FieldType fieldType;
        private String[] supportFieldType;

        @Override
        public String toString() {
            Map<String, Serializable> map = Maps.of("key", key, "fieldName", fieldName, "fieldType", fieldType.getKey(), "supportFieldType", supportFieldType);
            return JSONUtil.toJSONString(map);
        }
    }

    /**
     * 分支机构信息
     */
    public enum BranchEnum {
        BRANCH_NAME("name", "分支机构名称", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()});

        BranchEnum(String key, String fieldName, Enums.FieldType fieldType, String[] supportFieldType) {
            this.key = key;
            this.fieldName = fieldName;
            this.fieldType = fieldType;
            this.supportFieldType = supportFieldType;
        }

        private String key;
        private String fieldName;
        private Enums.FieldType fieldType;
        private String[] supportFieldType;

        @Override
        public String toString() {
            Map<String, Serializable> map = Maps.of("key", key, "fieldName", fieldName, "fieldType", fieldType.getKey(), "supportFieldType", supportFieldType);
            return JSONUtil.toJSONString(map);
        }
    }

    /**
     * 工商变更
     */
    public enum BusinessChangeEnum {
        CHANGE_ITEM("change_item", "变更项目", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        CHANGE_DATE("change_date", "变更日期", Enums.FieldType.TIMESTAMP, new String[]{FormFieldComEnum.EXTEND_DATE.getKey()}),
        BEFORE_CONTENT("before_content", "变更前内容", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        AFTER_CONTENT("after_content", "变更后内容", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        TAG("tag", "历史信息标签", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()});

        BusinessChangeEnum(String key, String fieldName, Enums.FieldType fieldType, String[] supportFieldType) {
            this.key = key;
            this.fieldName = fieldName;
            this.fieldType = fieldType;
            this.supportFieldType = supportFieldType;
        }

        private String key;
        private String fieldName;
        private Enums.FieldType fieldType;
        private String[] supportFieldType;

        @Override
        public String toString() {
            Map<String, Serializable> map = Maps.of("key", key, "fieldName", fieldName, "fieldType", fieldType.getKey(), "supportFieldType", supportFieldType);
            return JSONUtil.toJSONString(map);
        }
    }

    /**
     * 经营异常
     */
    public enum AbnormalEnum {
        REASON("in_reason", "经营异常列入原因", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        IN_DATA("in_date", "列入日期", Enums.FieldType.TIMESTAMP, new String[]{FormFieldComEnum.EXTEND_DATE.getKey()}),
        OUT_REASON("out_reason", "移出原因", Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()}),
        OUT_DATE("out_date", "移出日期", Enums.FieldType.TIMESTAMP, new String[]{FormFieldComEnum.EXTEND_DATE.getKey()});
//        ADDRESS("address","地址",Enums.FieldType.VARCHAR, new String[]{FormFieldComEnum.TEXT.getKey(), FormFieldComEnum.TEXTAREA.getKey()});

        AbnormalEnum(String key, String fieldName, Enums.FieldType fieldType, String[] supportFieldType) {
            this.key = key;
            this.fieldName = fieldName;
            this.fieldType = fieldType;
            this.supportFieldType = supportFieldType;
        }

        private String key;
        private String fieldName;
        private Enums.FieldType fieldType;
        private String[] supportFieldType;

        @Override
        public String toString() {
            Map<String, Serializable> map = Maps.of("key", key, "fieldName", fieldName, "fieldType", fieldType.getKey(), "supportFieldType", supportFieldType);
            return JSONUtil.toJSONString(map);
        }
    }
}
