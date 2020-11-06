 drop table login_record;
 --oracle
create table login_record(
id number primary key ,
login_name VARCHAR2(50),
login_type VARCHAR2(20),
login_time date
);

--mysql
CREATE TABLE LOGIN_RECORD(
ID BIGINT PRIMARY KEY ,
LOGIN_NAME VARCHAR(50),
LOGIN_TYPE VARCHAR(20),
LOGIN_TIME DATETIME
);