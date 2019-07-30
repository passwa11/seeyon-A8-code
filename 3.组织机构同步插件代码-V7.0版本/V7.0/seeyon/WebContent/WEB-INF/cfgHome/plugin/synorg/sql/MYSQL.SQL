/*==============================================================*/
/* DBMS name:      MYSQL                                        */
/* Created on:     2015/6/3 14:41:02                            */
/*==============================================================*/


/*==============================================================*/
/* Table: PLUGIN_SYN_DEPT(单位表)                               */
/*==============================================================*/
CREATE TABLE PLUGIN_SYN_UNIT (	
  	CODE VARCHAR(50)                  NOT NULL                   COMMENT '编号',
  	NAME VARCHAR(50)                  NOT NULL                   COMMENT '名称',
  	PARENT_CODE VARCHAR(50)           DEFAULT NULL               COMMENT '上级部门编码',
  	SORT_ID INT(10)                    DEFAULT NULL               COMMENT '排序',
  	DESCRIPTION VARCHAR(500)          DEFAULT NULL               COMMENT '描述',
  	EXT_ATTR_1 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段1',
  	EXT_ATTR_2 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段2',
  	EXT_ATTR_3 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段3',
  	EXT_ATTR_4 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段4',
  	EXT_ATTR_5 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段5',
    EXT_ATTR_6 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段6',
    EXT_ATTR_7 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段7',
    EXT_ATTR_8 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段8',
    EXT_ATTR_9 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段9',
    EXT_ATTR_10 VARCHAR(255)          DEFAULT NULL               COMMENT '预留字段10',
  	CREATE_DATE DATETIME               NOT NULL                   COMMENT '创建时间',
  	SYNC_DATE DATETIME                 DEFAULT NULL               COMMENT '同步时间',
  	SYNC_STATE SMALLINT(4)             DEFAULT 0                  COMMENT '同步状态：-1：失败 0-未同步     1-成功',
  	OA_ID VARCHAR(50)                  DEFAULT NULL               COMMENT 'OA系统中单位id',
  	PRIMARY KEY (CODE)
);

/*==============================================================*/
/* Table: PLUGIN_SYN_DEPT(部门表)                               */
/*==============================================================*/
CREATE TABLE PLUGIN_SYN_DEPT (	
  	CODE VARCHAR(50)                  NOT NULL                   COMMENT '编号',
  	NAME VARCHAR(50)                  NOT NULL                   COMMENT '名称',
  	PARENT_CODE VARCHAR(50)           DEFAULT NULL               COMMENT '上级部门编码',
  	SORT_ID INT(10)                    DEFAULT NULL               COMMENT '排序',
  	DESCRIPTION VARCHAR(500)          DEFAULT NULL               COMMENT '描述',
  	EXT_ATTR_1 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段1',
  	EXT_ATTR_2 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段2',
  	EXT_ATTR_3 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段3',
  	EXT_ATTR_4 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段4',
  	EXT_ATTR_5 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段5',
    EXT_ATTR_6 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段6',
    EXT_ATTR_7 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段7',
    EXT_ATTR_8 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段8',
    EXT_ATTR_9 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段9',
    EXT_ATTR_10 VARCHAR(255)          DEFAULT NULL               COMMENT '预留字段10',
  	CREATE_DATE DATETIME               NOT NULL                   COMMENT '创建时间',
  	SYNC_DATE DATETIME                 DEFAULT NULL               COMMENT '同步时间',
  	SYNC_STATE SMALLINT(4)             DEFAULT 0                  COMMENT '同步状态：-1：失败 0-未同步     1-成功',
  	PRIMARY KEY (CODE)
);

/*==============================================================*/
/* Table: PLUGIN_SYN_MEMBER(人员表)                             */
/*==============================================================*/
CREATE TABLE PLUGIN_SYN_MEMBER (
 	CODE VARCHAR(50)                  NOT NULL                   COMMENT '编号',
  	NAME VARCHAR(50)                  NOT NULL                   COMMENT '姓名',
  	LOGIN_NAME VARCHAR(50)            NOT NULL                   COMMENT '登录名',
  	PASSWORD VARCHAR(50)              DEFAULT NULL               COMMENT '登录密码',
  	IS_ENABLE SMALLINT(4)              DEFAULT 1                  COMMENT '是否启用：0-停用     1-启用',
  	SORT_ID INT(10)                    DEFAULT NULL               COMMENT '排序',
  	DEPARTMENT_CODE VARCHAR(50)       NOT NULL                   COMMENT '主岗部门编号',
  	POST_CODE VARCHAR(50)             DEFAULT NULL               COMMENT '主岗岗位编号',
  	LEVEL_CODE VARCHAR(50)            DEFAULT NULL               COMMENT '主岗职务级别编号',
  	DESCRIPTION VARCHAR(500)          DEFAULT NULL               COMMENT '描述',
  	EMAIL VARCHAR(255)                 DEFAULT NULL               COMMENT '邮件地址(对应ORG_MEMBER表：EXT_ATTR_2)',
  	OFFICE_NUM VARCHAR(255)            DEFAULT NULL               COMMENT '办公电话(对应ORG_MEMBER表：EXT_ATTR_3)',
  	TEL_NUMBER VARCHAR(255)            DEFAULT NULL               COMMENT '手机号码(对应ORG_MEMBER表：EXT_ATTR_1)',
  	ADDRESS VARCHAR(255)              DEFAULT NULL               COMMENT '家庭住址(对应ORG_MEMBER表：EXT_ATTR_4)',
  	POSTAL_CODE VARCHAR(255)           DEFAULT NULL               COMMENT '邮政编码(对应ORG_MEMBER表：EXT_ATTR_5)',
  	WEBSITE VARCHAR(255)              DEFAULT NULL               COMMENT '个人网页(对应ORG_MEMBER表：EXT_ATTR_6)',
  	BLOG VARCHAR(255)                 DEFAULT NULL               COMMENT '博客链接(对应ORG_MEMBER表：EXT_ATTR_7)',
  	GENDER SMALLINT(4)                 DEFAULT NULL               COMMENT '性别   1 - 男       2 - 女(对应ORG_MEMBER表：EXT_ATTR_11)',
  	BIRTHDAY DATETIME                  DEFAULT NULL               COMMENT '出生日期(对应ORG_MEMBER表：EXT_ATTR_21)',
  	WEIBO VARCHAR(255)                DEFAULT NULL               COMMENT '微博(对应ORG_MEMBER表：EXT_ATTR_31)',
  	WEIXIN VARCHAR(255)               DEFAULT NULL               COMMENT '微信(对应ORG_MEMBER表：EXT_ATTR_32)',
  	ID_NUM VARCHAR(255)                DEFAULT NULL               COMMENT '身份证号码(对应ORG_MEMBER表：EXT_ATTR_33)',
  	DEGREE VARCHAR(255)                DEFAULT NULL               COMMENT '学位(对应ORG_MEMBER表：EXT_ATTR_34)',
  	POST_ADDRESS VARCHAR(255)          DEFAULT NULL               COMMENT '通信地址(对应ORG_MEMBER表：EXT_ATTR_35)',
    EXT_ATTR_1 VARCHAR(255)            DEFAULT NULL               COMMENT '预留字段1',
    EXT_ATTR_2 VARCHAR(255)            DEFAULT NULL               COMMENT '预留字段2',
    EXT_ATTR_3 VARCHAR(255)            DEFAULT NULL               COMMENT '预留字段3',
    EXT_ATTR_4 VARCHAR(255)            DEFAULT NULL               COMMENT '预留字段4',
    EXT_ATTR_5 VARCHAR(255)            DEFAULT NULL               COMMENT '预留字段5',
    EXT_ATTR_6 VARCHAR(255)            DEFAULT NULL               COMMENT '预留字段6',
    EXT_ATTR_7 VARCHAR(255)            DEFAULT NULL               COMMENT '预留字段7',
    EXT_ATTR_8 VARCHAR(255)            DEFAULT NULL               COMMENT '预留字段8',
    EXT_ATTR_9 VARCHAR(255)            DEFAULT NULL               COMMENT '预留字段9',
    EXT_ATTR_10 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段10',
    EXT_ATTR_11 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段11',
    EXT_ATTR_12 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段12',
    EXT_ATTR_13 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段13',
    EXT_ATTR_14 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段14',
    EXT_ATTR_15 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段15',
    EXT_ATTR_16 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段16',
    EXT_ATTR_17 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段17',
    EXT_ATTR_18 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段18',
    EXT_ATTR_19 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段19',
    EXT_ATTR_20 VARCHAR(255)           DEFAULT NULL               COMMENT '预留字段20',
  	CREATE_DATE DATETIME               NOT NULL                   COMMENT '创建时间',
  	SYNC_DATE DATETIME                 DEFAULT NULL               COMMENT '同步时间',
  	SYNC_STATE SMALLINT(4)             DEFAULT 0                  COMMENT '同步状态：-1：失败 0-未同步     1-成功',
 	PRIMARY KEY (CODE)
);

/*==============================================================*/
/* Table: PLUGIN_SYN_MEMBER(职务表)                             */
/*==============================================================*/
CREATE TABLE PLUGIN_SYN_LEVEL (
	CODE VARCHAR(50)                  NOT NULL                   COMMENT '编码',
  	NAME VARCHAR(50)                  NOT NULL                   COMMENT '名称',
  	SORT_ID INT(10)                    DEFAULT NULL               COMMENT '排序',
  	DESCRIPTION VARCHAR(500)          DEFAULT NULL               COMMENT '描述',
  	CREATE_DATE DATETIME               NOT NULL                   COMMENT '创建时间',
  	SYNC_DATE DATETIME                 DEFAULT NULL               COMMENT '同步时间',
  	SYNC_STATE SMALLINT(4)             DEFAULT 0                  COMMENT '同步状态：-1：失败 0-未同步     1-成功',
  	PRIMARY KEY (CODE)
);

/*==============================================================*/
/* Table: PLUGIN_SYN_MEMBER(岗位表)                             */
/*==============================================================*/
CREATE TABLE PLUGIN_SYN_POST (
  	CODE VARCHAR(50)                  NOT NULL                   COMMENT '编码',
  	NAME VARCHAR(50)                  NOT NULL                   COMMENT '名称',
  	SORT_ID INT(10)                    DEFAULT NULL               COMMENT '排序',
  	DESCRIPTION VARCHAR(500)          DEFAULT NULL               COMMENT '描述',
  	CREATE_DATE DATETIME               NOT NULL                   COMMENT '创建时间',
  	SYNC_DATE DATETIME                 DEFAULT NULL               COMMENT '同步时间',
  	SYNC_STATE SMALLINT(4)             DEFAULT 0                  COMMENT '同步状态：-1：失败 0-未同步     1-成功',
  	PRIMARY KEY (CODE)
);

/*==============================================================*/
/* Table: PLUGIN_SYN_LOG(日志表)                             */
/*==============================================================*/
CREATE TABLE PLUGIN_SYN_LOG (
  	ID BIGINT(20)                      NOT NULL                   COMMENT '主键id',
  	ENTITY_TYPE VARCHAR(50)           NOT NULL                   COMMENT '实体类型：Department-部门   Member-人员   Post-职务  Level-岗位',
  	ENTITY_NAME VARCHAR(50)           NOT NULL                   COMMENT '实体名称',
  	ENTITY_CODE VARCHAR(50)           NOT NULL                   COMMENT '实体编码',
  	SYN_STATE SMALLINT(4)              NOT NULL                   COMMENT '同步状态     0-失败     1-成功',
  	SYN_TYPE SMALLINT(4)               NOT NULL                   COMMENT '同步操作类型：     1-新建     2-更新  3-删除',
  	SYN_LOG VARCHAR(500)               DEFAULT NULL               COMMENT '同步日志',
  	SYN_DATE DATETIME                  DEFAULT NULL               COMMENT '同步时间',
  	PRIMARY KEY (ID)
);