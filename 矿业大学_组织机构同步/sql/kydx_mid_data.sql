/*==============================================================*/
/* DBMS name:      ORACLE Version 11g                           */
/* Created on:     2020/6/15 8:50:16                            */
/*==============================================================*/


drop table M_ORG_LEVEL cascade constraints;

drop table M_ORG_MEMBER cascade constraints;

drop table M_ORG_POST cascade constraints;

drop table M_ORG_UNIT cascade constraints;

drop table THIRD_ORG_LEVEL cascade constraints;

drop table THIRD_ORG_MEMBER cascade constraints;

drop table THIRD_ORG_POST cascade constraints;

drop table THIRD_ORG_UNIT cascade constraints;

/*==============================================================*/
/* Table: M_ORG_LEVEL                                           */
/*==============================================================*/
create table M_ORG_LEVEL 
(
   ID                   VARCHAR2(50)         not null,
   NAME                 VARCHAR2(50),
   CODE                 VARCHAR2(50),
   SORT_ID              VARCHAR2(20),
   DESCRIPTION          VARCHAR2(200),
   IS_ENABLE            VARCHAR2(10),
   IS_DELETE            VARCHAR2(10),
   constraint PK_M_ORG_LEVEL primary key (ID)
);

comment on table M_ORG_LEVEL is
'职务级别记录表';

comment on column M_ORG_LEVEL.ID is
'ID';

comment on column M_ORG_LEVEL.NAME is
'职务名称';

comment on column M_ORG_LEVEL.CODE is
'职务编码';

comment on column M_ORG_LEVEL.SORT_ID is
'排序';

comment on column M_ORG_LEVEL.DESCRIPTION is
'描述';

comment on column M_ORG_LEVEL.IS_ENABLE is
'是否启用';

comment on column M_ORG_LEVEL.IS_DELETE is
'是否删除';

/*==============================================================*/
/* Table: M_ORG_MEMBER                                          */
/*==============================================================*/
create table M_ORG_MEMBER 
(
   ID                   VARCHAR2(50)         not null,
   USERID               VARCHAR2(50),
   NAME                 VARCHAR2(50),
   LOGINNAME            VARCHAR2(50),
   ORG_DEPARTMENT_ID    VARCHAR2(50),
   POST_ID              VARCHAR2(50),
   LEVEL_ID             VARCHAR2(50),
   PHONE                VARCHAR2(20),
   TEL                  VARCHAR2(20),
   EMAIL                VARCHAR2(50),
   IS_ENABLE            VARCHAR2(10),
   IS_DELETE            VARCHAR2(10),
   CODE                 VARCHAR2(50),
   DESCRIPTION          VARCHAR2(200),
   constraint PK_M_ORG_MEMBER primary key (ID)
);

comment on table M_ORG_MEMBER is
'人员记录表';

comment on column M_ORG_MEMBER.ID is
'ID';

comment on column M_ORG_MEMBER.USERID is
'人员编号';

comment on column M_ORG_MEMBER.NAME is
'姓名';

comment on column M_ORG_MEMBER.LOGINNAME is
'登陆账号';

comment on column M_ORG_MEMBER.ORG_DEPARTMENT_ID is
'所属部门编号';

comment on column M_ORG_MEMBER.POST_ID is
'岗位编号';

comment on column M_ORG_MEMBER.LEVEL_ID is
'职务级别';

comment on column M_ORG_MEMBER.PHONE is
'手机号';

comment on column M_ORG_MEMBER.TEL is
'固定电话';

comment on column M_ORG_MEMBER.EMAIL is
'电子邮箱';

comment on column M_ORG_MEMBER.IS_ENABLE is
'是否启用1启用，0禁用';

comment on column M_ORG_MEMBER.IS_DELETE is
'是否删除1删除，0未删除';

comment on column M_ORG_MEMBER.CODE is
'人员编码';

comment on column M_ORG_MEMBER.DESCRIPTION is
'描述';

/*==============================================================*/
/* Table: M_ORG_POST                                            */
/*==============================================================*/
create table M_ORG_POST 
(
   ID                   VARCHAR2(50)         not null,
   CODE                 VARCHAR2(50),
   NAME                 VARCHAR2(50),
   DESCRIPTION          VARCHAR2(100),
   IS_ENABLE            VARCHAR2(10),
   IS_DELETE            VARCHAR2(10),
   constraint PK_M_ORG_POST primary key (ID)
);

comment on table M_ORG_POST is
'岗位表记录表';

comment on column M_ORG_POST.ID is
'ID';

comment on column M_ORG_POST.CODE is
'编号';

comment on column M_ORG_POST.NAME is
'名称';

comment on column M_ORG_POST.DESCRIPTION is
'描述';

comment on column M_ORG_POST.IS_ENABLE is
'是否启用 1启用  ，0禁用';

comment on column M_ORG_POST.IS_DELETE is
'是否删除 1删除，0 未删除';

/*==============================================================*/
/* Table: M_ORG_UNIT                                            */
/*==============================================================*/
create table M_ORG_UNIT 
(
   ID                   VARCHAR2(50)         not null,
   NAME                 VARCHAR2(50),
   CODE                 VARCHAR2(50),
   UNIT                 VARCHAR2(50),
   SORT_ID              VARCHAR2(50),
   IS_ENABLE            VARCHAR2(10),
   IS_DELETE            VARCHAR2(10),
   STATUS               VARCHAR2(10),
   constraint PK_M_ORG_UNIT primary key (ID)
);

comment on table M_ORG_UNIT is
'单位记录表';

comment on column M_ORG_UNIT.ID is
'ID';

comment on column M_ORG_UNIT.NAME is
'名称';

comment on column M_ORG_UNIT.CODE is
'单位编码';

comment on column M_ORG_UNIT.UNIT is
'父级单位编码';

comment on column M_ORG_UNIT.SORT_ID is
'排序号';

comment on column M_ORG_UNIT.IS_ENABLE is
'是否启用';

comment on column M_ORG_UNIT.IS_DELETE is
'是否删除';

comment on column M_ORG_UNIT.STATUS is
'状态';

/*==============================================================*/
/* Table: THIRD_ORG_LEVEL                                       */
/*==============================================================*/
create table THIRD_ORG_LEVEL 
(
   ID                   VARCHAR2(50)         not null,
   NAME                 VARCHAR2(50),
   CODE                 VARCHAR2(50),
   DESCRIPTION          VARCHAR2(200),
   IS_ENABLE            VARCHAR2(10)         default '1',
   IS_DELETE            VARCHAR2(10)         default '0',
   constraint PK_THIRD_ORG_LEVEL primary key (ID)
);

comment on table THIRD_ORG_LEVEL is
'职务级别';

comment on column THIRD_ORG_LEVEL.ID is
'ID';

comment on column THIRD_ORG_LEVEL.NAME is
'职务名称';

comment on column THIRD_ORG_LEVEL.CODE is
'职务编码';

comment on column THIRD_ORG_LEVEL.DESCRIPTION is
'描述';

comment on column THIRD_ORG_LEVEL.IS_ENABLE is
'是否启用1启用  0 禁用';

comment on column THIRD_ORG_LEVEL.IS_DELETE is
'是否删除 1删除，0未删除';

/*==============================================================*/
/* Table: THIRD_ORG_MEMBER                                      */
/*==============================================================*/
create table THIRD_ORG_MEMBER 
(
   ID                   VARCHAR2(50)         not null,
   NAME                 VARCHAR2(50),
   CODE                 VARCHAR2(50),
   LOGINNAME            VARCHAR2(50),
   ORG_DEPARTMENT_ID    VARCHAR2(50),
   POST_ID              VARCHAR2(50),
   LEVEL_ID             VARCHAR2(50),
   PHONE                VARCHAR2(20),
   TEL                  VARCHAR2(20),
   EMAIL                VARCHAR2(50),
   IS_ENABLE            VARCHAR2(10)         default '1',
   IS_DELETE            VARCHAR2(10)         default '0',
   DESCRIPTION          VARCHAR2(200),
   constraint PK_THIRD_ORG_MEMBER primary key (ID)
);

comment on table THIRD_ORG_MEMBER is
'人员表';

comment on column THIRD_ORG_MEMBER.ID is
'ID';

comment on column THIRD_ORG_MEMBER.NAME is
'姓名';

comment on column THIRD_ORG_MEMBER.CODE is
'人员编码';

comment on column THIRD_ORG_MEMBER.LOGINNAME is
'登陆账号';

comment on column THIRD_ORG_MEMBER.ORG_DEPARTMENT_ID is
'所属部门编号';

comment on column THIRD_ORG_MEMBER.POST_ID is
'岗位编号';

comment on column THIRD_ORG_MEMBER.LEVEL_ID is
'职务级别';

comment on column THIRD_ORG_MEMBER.PHONE is
'手机号';

comment on column THIRD_ORG_MEMBER.TEL is
'固定电话';

comment on column THIRD_ORG_MEMBER.EMAIL is
'电子邮箱';

comment on column THIRD_ORG_MEMBER.IS_ENABLE is
'是否启用1启用，0禁用';

comment on column THIRD_ORG_MEMBER.IS_DELETE is
'是否删除1删除，0未删除';

comment on column THIRD_ORG_MEMBER.DESCRIPTION is
'描述';

/*==============================================================*/
/* Table: THIRD_ORG_POST                                        */
/*==============================================================*/
create table THIRD_ORG_POST 
(
   ID                   VARCHAR2(50)         not null,
   CODE                 VARCHAR2(50),
   NAME                 VARCHAR2(50),
   DESCRIPTION          VARCHAR2(100),
   IS_ENABLE            VARCHAR2(10)         default '1',
   IS_DELETE            VARCHAR2(10)         default '0',
   constraint PK_THIRD_ORG_POST primary key (ID)
);

comment on table THIRD_ORG_POST is
'岗位表';

comment on column THIRD_ORG_POST.ID is
'ID';

comment on column THIRD_ORG_POST.CODE is
'编号';

comment on column THIRD_ORG_POST.NAME is
'名称';

comment on column THIRD_ORG_POST.DESCRIPTION is
'描述';

comment on column THIRD_ORG_POST.IS_ENABLE is
'是否启用 1启用  ，0禁用';

comment on column THIRD_ORG_POST.IS_DELETE is
'是否删除 1删除，0 未删除';

/*==============================================================*/
/* Table: THIRD_ORG_UNIT                                        */
/*==============================================================*/
create table THIRD_ORG_UNIT 
(
   ID                   VARCHAR2(50)         not null,
   NAME                 VARCHAR2(50),
   CODE                 VARCHAR2(50),
   UNIT                 VARCHAR2(50),
   IS_ENABLE            VARCHAR2(10)         default '1',
   IS_DELETE            VARCHAR2(10)         default '0',
   STATUS               VARCHAR2(10)         default '0',
   CREATE_USER          VARCHAR2(40),
   CREATE_TIME          VARCHAR2(40),
   constraint PK_THIRD_ORG_UNIT primary key (ID)
);

comment on table THIRD_ORG_UNIT is
'单位表';

comment on column THIRD_ORG_UNIT.ID is
'ID';

comment on column THIRD_ORG_UNIT.NAME is
'名称';

comment on column THIRD_ORG_UNIT.CODE is
'编号';

comment on column THIRD_ORG_UNIT.UNIT is
'父级编号';

comment on column THIRD_ORG_UNIT.IS_ENABLE is
'是否启用 1启用true，0不启用';

comment on column THIRD_ORG_UNIT.IS_DELETE is
'是否删除 1删除，0未删除';

comment on column THIRD_ORG_UNIT.STATUS is
'状态';

comment on column THIRD_ORG_UNIT.CREATE_USER is
'创建人';

comment on column THIRD_ORG_UNIT.CREATE_TIME is
'创建时间';

