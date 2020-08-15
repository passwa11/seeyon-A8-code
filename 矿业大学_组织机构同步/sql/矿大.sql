-- drop table seeyon_oa_dw;
-- drop table seeyon_oa_jzgjbxx;
-- drop table m_org_unit;
-- drop table m_org_member;

create table seeyon_oa_dw(
dwid varchar(40) primary key not null,
dwmc varchar(600),
dwjc varchar(180),
lsdwh varchar(10),
dwh varchar(10),
sfsy varchar(1),
dz_dqzyjg varchar (50)
);

create table seeyon_oa_jzgjbxx(
zwjbm varchar(20),
jzgid varchar(40) primary key not null,
xm varchar(36),
gh varchar(20),
dwh varchar(10),
dzzw varchar(90),
yddh varchar(30),
bglxdh varchar(30),
dzxx varchar(40),
grjj longtext,
yrfsdm varchar(10),
dqztm varchar(35),
dqzt varchar(6)
);


create table m_org_unit(
oaid VARCHAR(50) primary key not null ,
dwid varchar(50),
dwmc varchar(100),
dwjc varchar(100),
dwh varchar(20),
lsdwh varchar(20),
sfsy varchar(1),
dz_dqzyjg varchar (50)
);

create table m_org_member(
memberId VARCHAR(50) primary key not null ,
jzgid VARCHAR(50),
xm VARCHAR(50),
gh VARCHAR(50),
yddh VARCHAR(50),
bglxdh varchar(50),
dzxx varchar(50),
grjj VARCHAR(4000),
oaUnitId varchar(50),
dwh varchar(50),
yrfsdm varchar(10),
dqztm varchar(35),
dqzt varchar(6)
);
