/*==============================================================*/
/* DBMS name:            Microsoft SQL Server 2008              */
/* Created on:         2015/6/3 14:52:01                        */
/*==============================================================*/

/*==============================================================*/
/* Table: PLUGIN_SYN_UNIT(部门表)                               */
/*==============================================================*/
CREATE TABLE PLUGIN_SYN_UNIT (
    CODE NVARCHAR(50)                  NOT NULL,
    NAME NVARCHAR(50)                  NOT NULL,
    PARENT_CODE NVARCHAR(50)           DEFAULT NULL,
    SORT_ID INT                        DEFAULT NULL,
    DESCRIPTION NVARCHAR(500)          DEFAULT NULL,
    EXT_ATTR_1 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_2 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_3 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_4 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_5 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_6 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_7 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_8 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_9 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_10 NVARCHAR(255)          DEFAULT NULL,
   	CREATE_DATE DATETIME               NOT NULL,
  	SYNC_DATE DATETIME                 DEFAULT NULL,
  	SYNC_STATE SMALLINT                DEFAULT 0,
  	OA_ID NVARCHAR(50)                  DEFAULT NULL,
    PRIMARY KEY (CODE)
);

/*==============================================================*/
/* Table: PLUGIN_SYN_DEPT(部门表)                               */
/*==============================================================*/
CREATE TABLE PLUGIN_SYN_DEPT (
    CODE NVARCHAR(50)                  NOT NULL,
    NAME NVARCHAR(50)                  NOT NULL,
    PARENT_CODE NVARCHAR(50)           DEFAULT NULL,
    SORT_ID INT                        DEFAULT NULL,
    DESCRIPTION NVARCHAR(500)          DEFAULT NULL,
    EXT_ATTR_1 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_2 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_3 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_4 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_5 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_6 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_7 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_8 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_9 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_10 NVARCHAR(255)          DEFAULT NULL,
   	CREATE_DATE DATETIME               NOT NULL,
  	SYNC_DATE DATETIME                 DEFAULT NULL,
  	SYNC_STATE SMALLINT                DEFAULT 0,
    PRIMARY KEY (CODE)
);

/*==============================================================*/
/* Table: PLUGIN_SYN_MEMBER(人员表)                             */
/*==============================================================*/
CREATE TABLE PLUGIN_SYN_MEMBER (
    CODE NVARCHAR(50)                  NOT NULL,
    NAME NVARCHAR(50)                  NOT NULL,
    LOGIN_NAME NVARCHAR(50)            NOT NULL,
    PASSWORD NVARCHAR(50)              DEFAULT NULL,
    IS_ENABLE TINYINT                  DEFAULT 1,
    SORT_ID INT                        DEFAULT NULL,
    DEPARTMENT_CODE NVARCHAR(50)       NOT NULL,
    POST_CODE NVARCHAR(50)             DEFAULT NULL,
    LEVEL_CODE NVARCHAR(50)            DEFAULT NULL,
    DESCRIPTION NVARCHAR(500)          DEFAULT NULL,
    EMAIL NVARCHAR(255)                DEFAULT NULL,
    OFFICE_NUM NVARCHAR(255)           DEFAULT NULL,
    TEL_NUMBER NVARCHAR(255)           DEFAULT NULL,
    ADDRESS NVARCHAR(255)              DEFAULT NULL,
    POSTAL_CODE NVARCHAR(255)          DEFAULT NULL,
    WEBSITE NVARCHAR(255)              DEFAULT NULL,
    BLOG NVARCHAR(255)                 DEFAULT NULL,
    GENDER TINYINT                     DEFAULT NULL,
    BIRTHDAY DATETIME                  DEFAULT NULL,
    WEIBO NVARCHAR(255)                DEFAULT NULL,
    WEIXIN NVARCHAR(255)               DEFAULT NULL,
    ID_NUM NVARCHAR(255)               DEFAULT NULL,
    DEGREE NVARCHAR(255)               DEFAULT NULL,
    POST_ADDRESS NVARCHAR(255)         DEFAULT NULL,
    EXT_ATTR_1 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_2 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_3 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_4 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_5 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_6 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_7 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_8 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_9 NVARCHAR(255)           DEFAULT NULL,
    EXT_ATTR_10 NVARCHAR(255)          DEFAULT NULL,
    EXT_ATTR_11 NVARCHAR(255)          DEFAULT NULL,
    EXT_ATTR_12 NVARCHAR(255)          DEFAULT NULL,
    EXT_ATTR_13 NVARCHAR(255)          DEFAULT NULL,
    EXT_ATTR_14 NVARCHAR(255)          DEFAULT NULL,
    EXT_ATTR_15 NVARCHAR(255)          DEFAULT NULL,
    EXT_ATTR_16 NVARCHAR(255)          DEFAULT NULL,
    EXT_ATTR_17 NVARCHAR(255)          DEFAULT NULL,
    EXT_ATTR_18 NVARCHAR(255)          DEFAULT NULL,
    EXT_ATTR_19 NVARCHAR(255)          DEFAULT NULL,
    EXT_ATTR_20 NVARCHAR(255)          DEFAULT NULL,
    CREATE_DATE DATETIME               NOT NULL,
  	SYNC_DATE DATETIME                 DEFAULT NULL,
  	SYNC_STATE SMALLINT                DEFAULT 0,
    PRIMARY KEY (CODE)
);

/*==============================================================*/
/* Table: PLUGIN_SYN_MEMBER(职务表)                             */
/*==============================================================*/
CREATE TABLE PLUGIN_SYN_LEVEL (
    CODE NVARCHAR(50)                  NOT NULL,
    NAME NVARCHAR(50)                  NOT NULL,
    SORT_ID INT                        DEFAULT NULL,
    DESCRIPTION NVARCHAR(500)          DEFAULT NULL,
    CREATE_DATE DATETIME               NOT NULL,
  	SYNC_DATE DATETIME                 DEFAULT NULL,
  	SYNC_STATE SMALLINT                DEFAULT 0,
    PRIMARY KEY (CODE)
);

/*==============================================================*/
/* Table: PLUGIN_SYN_MEMBER(岗位表)                             */
/*==============================================================*/
CREATE TABLE PLUGIN_SYN_POST (
    CODE NVARCHAR(50)                  NOT NULL,
    NAME NVARCHAR(50)                  NOT NULL,
    SORT_ID INT                        DEFAULT NULL,
    DESCRIPTION NVARCHAR(500)          DEFAULT NULL,
    CREATE_DATE DATETIME               NOT NULL,
  	SYNC_DATE DATETIME                 DEFAULT NULL,
  	SYNC_STATE SMALLINT                DEFAULT 0,
    PRIMARY KEY (CODE)
);

/*==============================================================*/
/* Table: PLUGIN_SYN_LOG(日志表)                                */
/*==============================================================*/
CREATE TABLE PLUGIN_SYN_LOG (
    ID BIGINT                          NOT NULL,
    ENTITY_TYPE NVARCHAR(50)           NOT NULL,
    ENTITY_NAME NVARCHAR(50)           NOT NULL,
    ENTITY_CODE NVARCHAR(50)           NOT NULL,
    SYN_STATE SMALLINT                 NOT NULL,
    SYN_TYPE SMALLINT                  NOT NULL,
    SYN_LOG NVARCHAR(500)              DEFAULT NULL,
    SYN_DATE DATETIME                  DEFAULT NULL,
    PRIMARY KEY (ID)
);