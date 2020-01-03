insert into MID_POST(id,post_code,post_name,post_desc,post_id)
select id,code,name,description ,id from org_post where ORG_ACCOUNT_ID=116743462829769671 and code is not null and IS_DELETED=0 and IS_ENABLE=1;
