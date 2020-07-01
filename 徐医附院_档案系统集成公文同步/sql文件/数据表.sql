drop table TEMP_NUMBER10;
create table TEMP_NUMBER10
(
  id     VARCHAR2(100),
  status VARCHAR2(6)
);


drop table TEMP_NUMBER20;
-- pack_date字段的长度为30时报错  插入数据时字段长度过小，所以改成40
create table TEMP_NUMBER20
(
  id              VARCHAR2(200) not null,
  subject         VARCHAR2(255),
  doc_mark        VARCHAR2(255),
  issuer          VARCHAR2(255),
  send_department VARCHAR2(255),
  pack_date       VARCHAR2(40),
  status          VARCHAR2(6),
  create_time     varchar2(40),
  year              varchar2(6),
  edoc_type       varchar2(20),
  organizer       varchar2(400)
);
drop table TEMP_NUMBER30;
create table TEMP_NUMBER30
(
  id            VARCHAR2(64),
  c_midrecid    VARCHAR2(64),
  c_filetitle   VARCHAR2(512),
  c_ftpfilepath VARCHAR2(512),
  c_type        VARCHAR2(64),
  i_size        VARCHAR2(64),
  meta_type     VARCHAR2(20),
  status        VARCHAR2(6)
);


DROP TABLE "TEMP_NUMBER40";
CREATE TABLE "TEMP_NUMBER40" (
"MODULE_ID" VARCHAR2(50 BYTE) NULL ,
"CONTENT" CLOB NULL ,
"CONTENT_TYPE" VARCHAR2(50 BYTE) NULL ,
"SORT" VARCHAR2(50 BYTE) NULL ,
"ID" VARCHAR2(50 BYTE) NULL
)
