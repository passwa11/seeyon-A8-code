/*==============================================================*/
/* DBMS name:      ORACLE Version 11g                           */
/* Created on:     2015/6/3 14:41:02                            */
/*==============================================================*/

/*==============================================================*/
/* Table: PLUGIN_SYN_UNIT(单位表)                               */
/*==============================================================*/
CREATE TABLE PLUGIN_SYN_UNIT (
  	CODE VARCHAR2(50)                 NOT NULL ENABLE,
  	NAME VARCHAR2(100)                 NOT NULL ENABLE,
  	PARENT_CODE VARCHAR2(50)          DEFAULT NULL,
  	SORT_ID NUMBER(10, 0)             DEFAULT NULL,
 	DESCRIPTION VARCHAR2(500)         DEFAULT NULL,
 	EXT_ATTR_1 VARCHAR2(255)          DEFAULT NULL,
 	EXT_ATTR_2 VARCHAR2(255)          DEFAULT NULL,
 	EXT_ATTR_3 VARCHAR2(255)          DEFAULT NULL,
 	EXT_ATTR_4 VARCHAR2(255)          DEFAULT NULL,
 	EXT_ATTR_5 VARCHAR2(255)          DEFAULT NULL,
 	EXT_ATTR_6 VARCHAR2(255)          DEFAULT NULL,
 	EXT_ATTR_7 VARCHAR2(255)          DEFAULT NULL,
  	EXT_ATTR_8 VARCHAR2(255)          DEFAULT NULL,
  	EXT_ATTR_9 VARCHAR2(255)          DEFAULT NULL,
  	EXT_ATTR_10 VARCHAR2(255)         DEFAULT NULL,
  	CREATE_DATE DATE                   NOT NULL ENABLE, 
  	SYNC_DATE DATE                     DEFAULT NULL,              
  	SYNC_STATE NUMBER(4, 0)            DEFAULT 0,
  	OA_ID VARCHAR2(100)                DEFAULT NULL,
  	PRIMARY KEY (CODE)
);

/*==============================================================*/
/* Table: PLUGIN_SYN_DEPT(部门表)                               */
/*==============================================================*/
CREATE TABLE PLUGIN_SYN_DEPT (
  	CODE VARCHAR2(50)                 NOT NULL ENABLE,
  	NAME VARCHAR2(100)                 NOT NULL ENABLE,
  	PARENT_CODE VARCHAR2(50)          DEFAULT NULL,
  	SORT_ID NUMBER(10, 0)             DEFAULT NULL,
 	DESCRIPTION VARCHAR2(500)         DEFAULT NULL,
 	EXT_ATTR_1 VARCHAR2(255)          DEFAULT NULL,
 	EXT_ATTR_2 VARCHAR2(255)          DEFAULT NULL,
 	EXT_ATTR_3 VARCHAR2(255)          DEFAULT NULL,
 	EXT_ATTR_4 VARCHAR2(255)          DEFAULT NULL,
 	EXT_ATTR_5 VARCHAR2(255)          DEFAULT NULL,
 	EXT_ATTR_6 VARCHAR2(255)          DEFAULT NULL,
 	EXT_ATTR_7 VARCHAR2(255)          DEFAULT NULL,
  	EXT_ATTR_8 VARCHAR2(255)          DEFAULT NULL,
  	EXT_ATTR_9 VARCHAR2(255)          DEFAULT NULL,
  	EXT_ATTR_10 VARCHAR2(255)         DEFAULT NULL,
  	CREATE_DATE DATE                   NOT NULL ENABLE, 
  	SYNC_DATE DATE                     DEFAULT NULL,              
  	SYNC_STATE NUMBER(4, 0)            DEFAULT 0,
  	PRIMARY KEY (CODE)
);

/*==============================================================*/
/* Table: PLUGIN_SYN_MEMBER(人员表)                             */
/*==============================================================*/
CREATE TABLE PLUGIN_SYN_MEMBER (
  	CODE VARCHAR2(50)                 NOT NULL ENABLE,
  	NAME VARCHAR2(100)                 NOT NULL ENABLE,
  	LOGIN_NAME VARCHAR2(50)           NOT NULL ENABLE, 
  	PASSWORD VARCHAR2(50)             DEFAULT NULL,
  	IS_ENABLE NUMBER(4, 0)             DEFAULT 1,
  	SORT_ID NUMBER(10, 0)              DEFAULT NULL,
  	DEPARTMENT_CODE VARCHAR2(50)      DEFAULT NULL,
  	POST_CODE VARCHAR2(50)            DEFAULT NULL,
  	LEVEL_CODE VARCHAR2(50)           DEFAULT NULL,
  	DESCRIPTION VARCHAR2(500)         DEFAULT NULL,
  	EMAIL VARCHAR2(255)                DEFAULT NULL,
  	OFFICE_NUM VARCHAR2(255)           DEFAULT NULL,
  	TEL_NUMBER VARCHAR2(255)           DEFAULT NULL,
  	ADDRESS VARCHAR2(255)             DEFAULT NULL,
  	POSTAL_CODE VARCHAR2(255)          DEFAULT NULL,
  	WEBSITE VARCHAR2(255)             DEFAULT NULL,
  	BLOG VARCHAR2(255)                DEFAULT NULL,
  	GENDER NUMBER(4, 0)                DEFAULT NULL,
  	BIRTHDAY DATE                      DEFAULT NULL,
  	WEIBO VARCHAR2(255)               DEFAULT NULL,
  	WEIXIN VARCHAR2(255)              DEFAULT NULL,
  	ID_NUM VARCHAR2(255)               DEFAULT NULL,
  	DEGREE VARCHAR2(255)               DEFAULT NULL,
  	POST_ADDRESS VARCHAR2(255)         DEFAULT NULL,
  	EXT_ATTR_1 VARCHAR2(255)           DEFAULT NULL,
  	EXT_ATTR_2 VARCHAR2(255)           DEFAULT NULL,
  	EXT_ATTR_3 VARCHAR2(255)           DEFAULT NULL,
  	EXT_ATTR_4 VARCHAR2(255)           DEFAULT NULL,
    EXT_ATTR_5 VARCHAR2(255)           DEFAULT NULL,
    EXT_ATTR_6 VARCHAR2(255)           DEFAULT NULL,
    EXT_ATTR_7 VARCHAR2(255)           DEFAULT NULL,
    EXT_ATTR_8 VARCHAR2(255)           DEFAULT NULL,
    EXT_ATTR_9 VARCHAR2(255)           DEFAULT NULL,
    EXT_ATTR_10 VARCHAR2(255)          DEFAULT NULL,
    EXT_ATTR_11 VARCHAR2(255)          DEFAULT NULL,
    EXT_ATTR_12 VARCHAR2(255)          DEFAULT NULL,
    EXT_ATTR_13 VARCHAR2(255)          DEFAULT NULL,
    EXT_ATTR_14 VARCHAR2(255)          DEFAULT NULL,
    EXT_ATTR_15 VARCHAR2(255)          DEFAULT NULL,
    EXT_ATTR_16 VARCHAR2(255)          DEFAULT NULL,
    EXT_ATTR_17 VARCHAR2(255)          DEFAULT NULL,
    EXT_ATTR_18 VARCHAR2(255)          DEFAULT NULL,
    EXT_ATTR_19 VARCHAR2(255)          DEFAULT NULL,
    EXT_ATTR_20 VARCHAR2(255)          DEFAULT NULL,
    CREATE_DATE DATE                   NOT NULL ENABLE, 
  	SYNC_DATE DATE                     DEFAULT NULL,              
  	SYNC_STATE NUMBER(4, 0)            DEFAULT 0,
  PRIMARY KEY (CODE)
);

/*==============================================================*/
/* Table: PLUGIN_SYN_MEMBER(职务表)                             */
/*==============================================================*/
CREATE TABLE PLUGIN_SYN_LEVEL (
  	CODE VARCHAR2(50)                 NOT NULL ENABLE,
  	NAME VARCHAR2(50)                 NOT NULL ENABLE,
  	SORT_ID NUMBER(10, 0)              DEFAULT NULL,
  	DESCRIPTION VARCHAR2(500)         DEFAULT NULL,
  	CREATE_DATE DATE                   NOT NULL ENABLE, 
  	SYNC_DATE DATE                     DEFAULT NULL,              
  	SYNC_STATE NUMBER(4, 0)            DEFAULT 0,
  	PRIMARY KEY (CODE)
);

/*==============================================================*/
/* Table: PLUGIN_SYN_MEMBER(岗位表)                             */
/*==============================================================*/
CREATE TABLE PLUGIN_SYN_POST (
  	CODE VARCHAR2(50)                 NOT NULL ENABLE,
  	NAME VARCHAR2(50)                 NOT NULL ENABLE,
  	SORT_ID NUMBER(10, 0)              DEFAULT NULL,
  	DESCRIPTION VARCHAR2(500)         DEFAULT NULL,
  	CREATE_DATE DATE                   NOT NULL ENABLE, 
  	SYNC_DATE DATE                     DEFAULT NULL,              
  	SYNC_STATE NUMBER(4, 0)            DEFAULT 0,
  	PRIMARY KEY (CODE)
);

/*==============================================================*/
/* Table: PLUGIN_SYN_LOG(日志表)                             */
/*==============================================================*/
CREATE TABLE PLUGIN_SYN_LOG (
  	ID NUMBER(19, 0)                   NOT NULL ENABLE,
  	ENTITY_TYPE VARCHAR2(50)          NOT NULL ENABLE,
  	ENTITY_NAME VARCHAR2(100)          NOT NULL ENABLE,
  	ENTITY_CODE VARCHAR2(50)          NOT NULL ENABLE,
  	SYN_STATE NUMBER(4, 0)             NOT NULL ENABLE,
  	SYN_TYPE NUMBER(4, 0)              NOT NULL ENABLE,
  	SYN_LOG VARCHAR2(500)              DEFAULT NULL,
  	SYN_DATE DATE                      DEFAULT NULL,
  	PRIMARY KEY (ID)
);