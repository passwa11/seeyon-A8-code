create or replace procedure pro_xyfy2(flag number) IS
BEGIN

    if (flag = 1) then

            begin

                insert into TEMP_NUMBER10(ID,STATUS) select to_char(A.id),0 from edoc_summary A left
                join (select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from CTP_CONTENT_ALL
where to_char(content) in (select to_char(id) from ctp_file) and trim(translate(nvl(CONTENT,'x'),'-0123456789',' ')) is NULL  and length(content)<30) zall,ctp_file cf where ZALL.CONTENT=CF.id) B on A.Id = B.MODULE_ID
                where has_archive = 1
                and A.id not in (select id from TEMP_NUMBER10);

            exception
                when others then
                    ROLLBACK;
                    DBMS_OUTPUT.put_line('TEMP_NUMBER10 Error No:' || SQLCODE);
                    DBMS_OUTPUT.put_line(SQLERRM);
            end;

    elsif (flag = 2) then
        --公文信息表
        begin
              --发文
                insert into TEMP_NUMBER20
                 select A.id,subject,doc_mark, issuer,A.send_department,A.pack_date, 0,to_char(a.create_time,'yyyyMMdd') create_time,to_char(a.create_time,'yyyy') year,  CASE A .EDOC_TYPE WHEN 0 THEN  '发文' WHEN 1 THEN  '收文' ELSE  '签报' END EDOC_TYPE,''
                  from edoc_summary A, (select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from CTP_CONTENT_ALL where to_char(content) in (select to_char(id) from ctp_file)
 and trim(translate(nvl(CONTENT,'x'),'-0123456789',' ')) is NULL  and length(content)<30) zall,ctp_file cf where ZALL.CONTENT=CF.id) b, org_member c, (select CA.OBJECT_ID,DR.CREATE_USER_ID from CTP_AFFAIR ca,DOC_RESOURCES dr where ca.id=DR.SOURCE_ID) d, ORG_UNIT e
                  where A.has_archive = 1
                    and a.id = b.MODULE_ID
                    and c.id = d.create_user_id
                    and c.org_department_id = e.id
                    and a.id = d.OBJECT_ID and A.id not in (select id from temp_number10 where status='1') and a.EDOC_TYPE=0 ;
              --收文
                insert into TEMP_NUMBER20
                                    select A.id,subject,doc_mark, issuer,A.department_name,A.pack_date, 0,to_char(a.create_time,'yyyyMMdd') create_time,to_char(a.create_time,'yyyy') year,  CASE A .EDOC_TYPE WHEN 0 THEN  '发文' WHEN 1 THEN  '收文' ELSE  '签报' END EDOC_TYPE,
                  a.send_unit
                  from (select tw.id,tw.subject,tw.doc_mark,tw.issuer,tw.send_unit,tw.pack_date,tw.create_time,tw.edoc_type,tw.has_archive,th.department_name from edoc_summary tw left join (
select b.id,listagg(b.department_name ,'、')within group(order by b.id) department_name from
(select s.*,o.department_name from edoc_summary s left join (select * from edoc_opinion where policy='field0010') o on s.id= o.edoc_id ) b group by b.id
) th on tw.id=th.id ) A,
                  (select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from CTP_CONTENT_ALL where to_char(content) in (select to_char(id) from ctp_file)
 and trim(translate(nvl(CONTENT,'x'),'-0123456789',' ')) is NULL  and length(content)<30) zall,ctp_file cf where ZALL.CONTENT=CF.id) b, org_member c,
(select CA.OBJECT_ID,DR.CREATE_USER_ID from CTP_AFFAIR ca,DOC_RESOURCES dr where ca.id=DR.SOURCE_ID) d, ORG_UNIT e
                  where A.has_archive = 1
                    and a.id = b.MODULE_ID
                    and c.id = d.create_user_id
                    and c.org_department_id = e.id
                    and a.id = d.OBJECT_ID and A.id not in (select id from temp_number10 where status='1') and  a.EDOC_TYPE=1;
              --签报
                insert into TEMP_NUMBER20
                 select A.id,subject,doc_mark, issuer,A.send_department,A.pack_date, 0,to_char(a.create_time,'yyyyMMdd') create_time,to_char(a.create_time,'yyyy') year,  CASE A .EDOC_TYPE WHEN 0 THEN  '发文' WHEN 1 THEN  '收文' ELSE  '签报' END EDOC_TYPE,''
                  from edoc_summary A, (select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from CTP_CONTENT_ALL where to_char(content) in (select to_char(id) from ctp_file)
 and trim(translate(nvl(CONTENT,'x'),'-0123456789',' ')) is NULL  and length(content)<30) zall,ctp_file cf where ZALL.CONTENT=CF.id) b, org_member c, (select CA.OBJECT_ID,DR.CREATE_USER_ID from CTP_AFFAIR ca,DOC_RESOURCES dr where ca.id=DR.SOURCE_ID) d, ORG_UNIT e
                  where A.has_archive = 1
                    and a.id = b.MODULE_ID
                    and c.id = d.create_user_id
                    and c.org_department_id = e.id
                    and a.id = d.OBJECT_ID and A.id not in (select id from temp_number10 where status='1') and a.EDOC_TYPE=2;


        exception
            when others then
                ROLLBACK;
                DBMS_OUTPUT.put_line('TEMP_NUMBER20 Error No:' || SQLCODE);
                DBMS_OUTPUT.put_line(SQLERRM);
        end;
    elsif (flag = 3) then
        --公文信息正文表
        begin

            insert into TEMP_NUMBER30
            select B.id,A.id C_MIDRECID,case when instr(b.mime_type,'office')>0  then a.subject ||'.doc' when instr(b.mime_type,'msword')>0 then a.subject ||'.doc' when instr(b.mime_type,'pdf')>0 then a.subject ||'.pdf' when instr(b.mime_type,'excel')>0 then a.subject ||'.xls' end c_filetitle,
                   '/upload/' ||
                   substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 0, 4) || '/' ||
                   substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 6, 2) || '/' ||
                   substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 9, 2) || '/' ||
                   case when instr(b.mime_type,'office')>0  then C.Filename  when instr(b.mime_type,'msword')>0 then C.Filename  when instr(b.mime_type,'pdf')>0 then C.Filename when instr(b.mime_type,'excel')>0 then C.Filename  end C_FTPFILEPATH,
                   '正文' C_TYPE,
                   C.FILE_SIZE I_SIZE,
                   case when instr(b.mime_type,'office')>0  then '.doc' when instr(b.mime_type,'msword')>0 then '.doc' when instr(b.mime_type,'pdf')>0 then '.pdf' when instr(b.mime_type,'excel')>0 then '.xls' end META_TYPE,
                     0
            from edoc_summary A
                     left join (select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from CTP_CONTENT_ALL where to_char(content) in (select to_char(id) from ctp_file) and trim(translate(nvl(CONTENT,'x'),'-0123456789',' ')) is NULL  and length(content)<30) zall,ctp_file cf where ZALL.CONTENT=CF.id) B on B.MODULE_ID = A.Id left join ctp_file C on to_char(B.content) = C.Id
            where B.Id is not null  and A.id in (select id from TEMP_NUMBER10 where status='0');

            -- 添加新增文单的功能
            insert into TEMP_NUMBER30
            select B.id,
                   A.id C_MIDRECID,
                   a.subject || '.html' c_filetitle,
                   '/upload/' ||
                   substr(to_char(a.Create_time, 'yyyy-mm-dd'), 0, 4) || '/' ||
                   substr(to_char(a.Create_time, 'yyyy-mm-dd'), 6, 2) || '/' ||
                   substr(to_char(a.Create_time, 'yyyy-mm-dd'), 9, 2) || '/' ||
                   a.id C_FTPFILEPATH,
                   '文单' C_TYPE,
                   2048 I_SIZE,
                   '.html' META_TYPE,
                   0
            from edoc_summary A
                     left join (select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from CTP_CONTENT_ALL where to_char(content) in (select to_char(id) from ctp_file) and trim(translate(nvl(CONTENT,'x'),'-0123456789',' ')) is NULL  and length(content)<30) zall,ctp_file cf where ZALL.CONTENT=CF.id) B
                               on B.MODULE_ID = A.Id
            where B.Id is not null  and A.id in (select id from TEMP_NUMBER10 where status='0');
        exception
            when others then
                ROLLBACK;
                DBMS_OUTPUT.put_line('TEMP_NUMBER30 Error No:' || SQLCODE);
                DBMS_OUTPUT.put_line(SQLERRM);
        end;
    elsif (flag = 4) then
        --公文信息表附件表
        begin

            insert into TEMP_NUMBER30
            select C.id, A.Id C_MIDRECID,
                   C.Filename C_FILETITLE,
                   '/upload/' ||
                   substr(to_char(C.createdate, 'yyyy-mm-dd'), 0, 4) || '/' ||
                   substr(to_char(C.createdate, 'yyyy-mm-dd'), 6, 2) || '/' ||
                   substr(to_char(C.createdate, 'yyyy-mm-dd'), 9, 2) || '/' ||
                   C.file_url  C_FTPFILEPATH,
                   '附件' C_TYPE,
                   C.attachment_size I_SIZE,
                  substr(C.Filename, instr(C.Filename, '.', -1, 1)) META_TYPE,
                  0
            from edoc_summary A
                     left join (select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from CTP_CONTENT_ALL where to_char(content) in (select to_char(id) from ctp_file) and trim(translate(nvl(CONTENT,'x'),'-0123456789',' ')) is NULL  and length(content)<30) zall,ctp_file cf where ZALL.CONTENT=CF.id) B
                               on A.Id = B.MODULE_ID
                     left join ctp_attachment C
                               on b.MODULE_ID = c.att_reference
            where C.id is not null
              and A.id in (select id from TEMP_NUMBER10 where status='0');

        exception
            when others then
                ROLLBACK;
                DBMS_OUTPUT.put_line('TEMP_NUMBER40 Error No:' || SQLCODE);
                DBMS_OUTPUT.put_line(SQLERRM);
        end;
    end if;
END;
