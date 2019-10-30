/*
  1、在OA服务器上创建临时表，用于记录需要同步的公文信息表主键及正文内码
*/

create global temporary table TEMP_NUMBER1
(
   id   varchar2(100)
)
on commit preserve rows;

/*
  2、在OA服务器上创建临时表，用于记录需要同步的公文信息表
*/
create global temporary table TEMP_NUMBER2
(
   ID                VARCHAR2(50),
  FLDSUBJECT        VARCHAR2(1000),
  FLDFWBH           VARCHAR2(255),
  FLDLX             VARCHAR2(255),
  FLDMIJI           VARCHAR2(255),
  FLDMEETINGTIME    VARCHAR2(255),
  FLDMEETINGLOCAL   VARCHAR2(255),
  FLDJILUREN        VARCHAR2(100),
  FLDDIANHUA        VARCHAR2(255),
  FLDZHUCHIREN      VARCHAR2(255),
  FLDFUZEREN        VARCHAR2(255),
  FLDQIANFA         VARCHAR2(255),
  FLDHEGAO          VARCHAR2(255),
  FLDZBDW           VARCHAR2(255),
  FLDNIGAOREN       VARCHAR2(255),
  FLDQICAORQ        VARCHAR2(255),
  FLDFENSHU         VARCHAR2(255),
  FLDCANJIAREN      VARCHAR2(1000),
  FLDZHUSONGDW_SHOW VARCHAR2(1000),
  FLDCHAOSONG_SHOW  VARCHAR2(1000),
  FLDFASONGRY       VARCHAR2(500),
  FLDCHAOBAO_SHOW   VARCHAR2(1000),
  FLDFENSONG        VARCHAR2(100),
  FLDTOPIC          VARCHAR2(255),
  FLDMEMO           VARCHAR2(1000),
  FLDLWJG           VARCHAR2(4000),
  FLDYINFADATE      VARCHAR2(255),
  FLDZH             VARCHAR2(100),
  FLDCOMPANY        VARCHAR2(100),
  FLDTYPE           VARCHAR2(100),
  I_STATE           VARCHAR2(10) ,
  FLDPERNAME VARCHAR2(255) ,
  FLDORGNAME VARCHAR2(255)
)
on commit preserve rows;

/*
  3、在OA服务器上创建临时表，用于记录需要同步的公文正文表
*/

create global temporary table TEMP_NUMBER3
(
  ID            VARCHAR2(64),
  C_MIDRECID    VARCHAR2(64),
  C_FILETITLE   VARCHAR2(512),
  C_FTPFILEPATH VARCHAR2(512),
  C_TYPE        VARCHAR2(64),
  I_SIZE        VARCHAR2(64)
)
on commit preserve rows;

/*
  4、在OA服务器上创建临时表，用于记录需要同步的公文附件表
*/

create global temporary table TEMP_NUMBER4
(
  ID            VARCHAR2(64),
  C_MIDRECID    VARCHAR2(64),
  C_FILETITLE   VARCHAR2(512),
  C_FTPFILEPATH VARCHAR2(512),
  C_TYPE        VARCHAR2(64),
  I_SIZE        VARCHAR2(64)
)
on commit preserve rows;
