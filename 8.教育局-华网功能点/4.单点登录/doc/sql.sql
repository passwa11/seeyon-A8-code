 drop table login_record;
create table login_record(
id number primary key ,
login_name VARCHAR2(50),
login_type VARCHAR2(20),
login_time date
);