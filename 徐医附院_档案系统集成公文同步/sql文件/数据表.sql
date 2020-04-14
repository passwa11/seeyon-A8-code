create table TEMP_NUMBER10
(
  id     VARCHAR2(100),
  status VARCHAR2(6)
)


create table TEMP_NUMBER20
(
  id              VARCHAR2(200) not null,
  subject         VARCHAR2(255),
  doc_mark        VARCHAR2(255),
  issuer          VARCHAR2(255),
  send_department VARCHAR2(255),
  pack_date       VARCHAR2(30),
  status          VARCHAR2(6),
  create_time     varchar2(30),
  year              varchar2(6),
  edoc_type       varchar2(20)
)

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
)

