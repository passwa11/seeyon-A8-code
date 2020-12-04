create table temp_qr_code_bj(
	id NUMBER primary key,
	create_date date,
	bj_id VARCHAR2(50),
	file_url VARCHAR2(255),
	filename VARCHAR2(100),
	mime_type varchar2(50),
	create_member_id VARCHAR2(50),
	bj_p1 VARCHAR2(100),
	bj_p2 VARCHAR2(100),
	bj_p3 VARCHAR2(100)
);

