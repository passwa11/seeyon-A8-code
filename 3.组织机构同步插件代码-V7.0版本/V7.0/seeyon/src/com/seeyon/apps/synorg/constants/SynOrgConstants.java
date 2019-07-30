package com.seeyon.apps.synorg.constants;

/**
 * 同步插件常量
 * @author Yang.Yinghai
 * @date 2016年8月2日下午4:18:55
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SynOrgConstants {

    /** 单组织版默认单位ID */
    public static final long DEFAULT_ACCOUNT_ID = -2448138224022240533L;

    /** 同步操作类型：新建 */
    public static final int SYN_OPERATION_TYPE_CREATE = 1;

    /** 同步操作类型：更新 */
    public static final int SYN_OPERATION_TYPE_UPDATE = 2;

    /** 同步操作类型：删除 */
    public static final int SYN_OPERATION_TYPE_DELETE = 3;

    /** 同步状态：成功 */
    public static final int SYN_STATE_SUCCESS = 1;

    /** 同步状态：未同步 */
    public static final int SYN_STATE_NONE = 0;

    /** 同步状态：失败 */
    public static final int SYN_STATE_FAILURE = -1;
    
    /** 组织机构实体类型：单位 */
    public static final String ORG_ENTITY_UNIT = "Unit";

    /** 组织机构实体类型：部门 */
    public static final String ORG_ENTITY_DEPARTMENT = "Department";

    /** 组织机构实体类型：岗位 */
    public static final String ORG_ENTITY_POST = "Post";

    /** 组织机构实体类型：职务 */
    public static final String ORG_ENTITY_LEVEL = "Level";
    
    /** 组织机构实体类型：部门角色 */
    public static final String ORG_ENTITY_DEPTROLE = "DeptRole";

    /** 组织机构实体类型：人员 */
    public static final String ORG_ENTITY_MEMBER = "Member";

    /** 组织模型同步参数类别 */
    public static final String SYNORG_CONFIGURATION = "com.seeyon.apps.synorg.Configuration";

    /** 组织模型同步日期 */
    public static final String ORG_SYNC_DATE = "ORG_SYNC_DATE";

    /** 组织模型同步时间 */
    public static final String ORG_SYNC_TIME = "ORG_SYNC_TIME";

    /** 轮循同步间隔时间 */
    public static final String ORG_INTERVAL_TIME = "ORG_INTERVAL_TIME";

    /** 是否启用自动同步 */
    public static final String CANAUTOORGSYNC = "AUTOORGSYNC";

    /** 组织模型同步类型 */
    public static final String ORG_SYNC_TYPE = "ORG_SYNC_TYPE";

    /** 同步范围 */
    public static final String ORG_SYNC_SCOPE = "ORG_SYNC_SCOPE";

    /** 默认岗位名 */
    public static final String ORG_SYNC_DEFAULT_POST_NAME = "ORG_SYNC_DEFAULT_POST_NAME";

    /** 默认职务名 */
    public static final String ORG_SYNC_DEFAULT_LEVEL_NAME = "ORG_SYNC_DEFAULT_LEVEL_NAME";

    /** 默认登录密码 */
    public static final String ORG_SYNC_DEFAULT_PASSWORD = "ORG_SYNC_DEFAULT_PASSWORD";

    /** 根节点编码 */
    public static final String ORG_SYNC_DEFAULT_ROOTCODE = "ORC_SYNC_DEFAULT_ROOTCODE";

    /** 是否同步人员密码 */
    public static final String ORG_SYNC_PASSWORD_STATE = "ORG_SYNC_PASSWORD_STATE";
    
    /** 如果没有设置岗位信息：则创建[普通员工]为默认岗位 */
    public static final String ORG_SYNC_CREATE_POST_NAME = "普通员工";
    
    /** 如果没有设置岗位信息：则创建[待定职务]为默认岗位 */
    public static final String ORG_SYNC_CREATE_LEVEL_NAME = "待定职务";
    /** 如果没有设置职务信息：则创建[待定职务]为默认职务  */
    public static final String ORG_SYNC_CREATE_LEVEL_CODE = "defaultlevel";
    
    /** 是否是集团版，如果选择的同步单位信息则是集团版 */
    public static Boolean ORG_SYNC_IS_GROUP = false;
    
    
    public boolean getOrgSyncIsGroup(){
    	return this.ORG_SYNC_IS_GROUP;
    }
    
    public void setOrgSyncIsGroup(boolean orgSyncIsGroup){
    	this.ORG_SYNC_IS_GROUP=orgSyncIsGroup;
    }


    /**
     * 自动同步时间类型
     * @author Yang.Yinghai
     * @date 2016年8月2日下午10:50:24
     * @Copyright(c) Beijing Seeyon Software Co.,LTD
     */
    public static enum SYNC_TIME_TYPE{
        /** 指定时间 */
        setTime,
        /** 间隔时间 */
        intervalTime
    }

    /**
     * @author Yang.Yinghai
     * @date 2016年8月2日下午4:35:28
     * @Copyright(c) Beijing Seeyon Software Co.,LTD
     */
    public enum SynOrgTriggerDate{
        /** 每天 */
        everyday(0),
        /** 每周日 */
        sunday(1),
        /** 每周一 */
        monday(2),
        /** 每周二 */
        tuesday(3),
        /** 每周三 */
        wednesday(4),
        /** 每周四 */
        thursday(5),
        /** 每周五 */
        friday(6),
        /** 每周六 */
        saturday(7);

        /** KEY */
        private int key;

        /**
         * @param key
         */
        SynOrgTriggerDate(int key) {
            this.key = key;
        }

        /**
         * @return
         */
        public int getKey() {
            return this.key;
        }

        public int key() {
            return this.key;
        }
    }
}
