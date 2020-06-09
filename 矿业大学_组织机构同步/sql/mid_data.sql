/*==============================================================*/
/* DBMS name:      ORACLE Version 11g                           */
/* Created on:     2020/6/9 10:14:35                            */
/*==============================================================*/


drop table THIRD_ORG_LEVEL cascade constraints;

drop table THIRD_ORG_MEMBER cascade constraints;

drop table THIRD_ORG_POST cascade constraints;

drop table THIRD_ORG_UNIT cascade constraints;

drop table THIR_ORG_UNIT cascade constraints;

/*==============================================================*/
/* Table: THIRD_ORG_LEVEL                                       */
/*==============================================================*/
create table THIRD_ORG_LEVEL 
(
   ID                   VARCHAR2(50)         not null,
   UNIT_ID              VARCHAR2(50),
   LEVEL_CODE           VARCHAR2(50),
   LEVEL_NAME           VARCHAR2(100),
   LEVEL_DESCRIPTION    VARCHAR2(200),
   constraint PK_THIRD_ORG_LEVEL primary key (ID)
);

comment on table THIRD_ORG_LEVEL is
'职务级别信息表';

comment on column THIRD_ORG_LEVEL.ID is
'ID';

comment on column THIRD_ORG_LEVEL.UNIT_ID is
'单位id';

comment on column THIRD_ORG_LEVEL.LEVEL_CODE is
'职务级别编码';

comment on column THIRD_ORG_LEVEL.LEVEL_NAME is
'职务级别名称';

comment on column THIRD_ORG_LEVEL.LEVEL_DESCRIPTION is
'职务级别描述';

/*==============================================================*/
/* Table: THIRD_ORG_MEMBER                                      */
/*==============================================================*/
create table THIRD_ORG_MEMBER 
(
   ID                   VARCHAR2(50)         not null,
   UNIT_ID              VARCHAR2(50),
   DEPT_ID              VARCHAR2(50),
   LEVEL_ID             VARCHAR2(50),
   POST_ID              VARCHAR2(50),
   USER_CODE            VARCHAR2(50),
   USER_NAME            VARCHAR2(50),
   USER_TEL             VARCHAR2(50),
   USER_OFFICE_NUM      VARCHAR2(20),
   USER_SEX             VARCHAR2(10),
   USER_LOGIN_NAME      VARCHAR2(50),
   USER_BIRTHDAY        VARCHAR2(30),
   constraint PK_THIRD_ORG_MEMBER primary key (ID)
);

comment on table THIRD_ORG_MEMBER is
'人员信息';

comment on column THIRD_ORG_MEMBER.ID is
'ID';

comment on column THIRD_ORG_MEMBER.UNIT_ID is
'单位id';

comment on column THIRD_ORG_MEMBER.DEPT_ID is
'部门id';

comment on column THIRD_ORG_MEMBER.LEVEL_ID is
'职务id';

comment on column THIRD_ORG_MEMBER.POST_ID is
'岗位id';

comment on column THIRD_ORG_MEMBER.USER_CODE is
'人员编号';

comment on column THIRD_ORG_MEMBER.USER_NAME is
'人员姓名';

comment on column THIRD_ORG_MEMBER.USER_TEL is
'人员移动电话';

comment on column THIRD_ORG_MEMBER.USER_OFFICE_NUM is
'人员办公电话';

comment on column THIRD_ORG_MEMBER.USER_SEX is
'人员性别';

comment on column THIRD_ORG_MEMBER.USER_LOGIN_NAME is
'人员登录名称';

comment on column THIRD_ORG_MEMBER.USER_BIRTHDAY is
'人员出生日期';

/*==============================================================*/
/* Table: THIRD_ORG_POST                                        */
/*==============================================================*/
create table THIRD_ORG_POST 
(
   ID                   VARCHAR2(50)         not null,
   POST_NAME            VARCHAR2(100),
   POST_CODE            VARCHAR2(50),
   POST_DESCRIPTION     VARCHAR2(200),
   POST_TYPE            VARCHAR2(20),
   UNIT_ID              VARCHAR2(50),
   constraint PK_THIRD_ORG_POST primary key (ID)
);

comment on table THIRD_ORG_POST is
'岗位信息表';

comment on column THIRD_ORG_POST.ID is
'编号';

comment on column THIRD_ORG_POST.POST_NAME is
'岗位名称';

comment on column THIRD_ORG_POST.POST_CODE is
'岗位编码';

comment on column THIRD_ORG_POST.POST_DESCRIPTION is
'岗位描述';

comment on column THIRD_ORG_POST.POST_TYPE is
'岗位类型(管理类是1，技术类是2，营销类是3，职能类是4)';

comment on column THIRD_ORG_POST.UNIT_ID is
'单位ID';

/*==============================================================*/
/* Table: THIRD_ORG_UNIT                                        */
/*==============================================================*/
create table THIRD_ORG_UNIT 
(
   ID                   VARCHAR2(50)         not null,
   UNIT_NAME            VARCHAR2(60),
   UNIT_CODE            VARCHAR2(50),
   UNIT_DESCRIPTION     VARCHAR2(200),
   UNIT_SHORT_NAME      VARCHAR2(50),
   UNIT_SORT            VARCHAR2(20),
   UNIT_PARENT_ID       VARCHAR2(50),
   constraint PK_THIRD_ORG_UNIT primary key (ID)
);

comment on table THIRD_ORG_UNIT is
'单位';

comment on column THIRD_ORG_UNIT.ID is
'ID';

comment on column THIRD_ORG_UNIT.UNIT_NAME is
'单位名称';

comment on column THIRD_ORG_UNIT.UNIT_CODE is
'单位编码';

comment on column THIRD_ORG_UNIT.UNIT_DESCRIPTION is
'单位描述';

comment on column THIRD_ORG_UNIT.UNIT_SHORT_NAME is
'单位简称';

comment on column THIRD_ORG_UNIT.UNIT_SORT is
'单位序号';

comment on column THIRD_ORG_UNIT.UNIT_PARENT_ID is
'父级单位id';

/*==============================================================*/
/* Table: THIR_ORG_UNIT                                         */
/*==============================================================*/
create table THIR_ORG_UNIT 
(
   ID                   VARCHAR2(50)         not null,
   DEPT_NAME            VARCHAR2(80),
   DEPT_CODE            VARCHAR2(50),
   DEPT_DESCRIPTION     VARCHAR2(200),
   DEPT_ENABLE          VARCHAR2(10),
   DEPT_PARENT_ID       VARCHAR2(50),
   constraint PK_THIR_ORG_UNIT primary key (ID)
);

comment on table THIR_ORG_UNIT is
'部门';

comment on column THIR_ORG_UNIT.ID is
'ID';

comment on column THIR_ORG_UNIT.DEPT_NAME is
'部门名称';

comment on column THIR_ORG_UNIT.DEPT_CODE is
'部门编号';

comment on column THIR_ORG_UNIT.DEPT_DESCRIPTION is
'部门描述';

comment on column THIR_ORG_UNIT.DEPT_ENABLE is
'是否启用';

comment on column THIR_ORG_UNIT.DEPT_PARENT_ID is
'父部门ID';

