create or replace procedure pro_xyfy3(flag number) IS
BEGIN
    if (flag = 1) then
            begin
                insert into TEMP_NUMBER10(ID,STATUS)
                select DS.id,0 from (select s.* from EDOC_SUMMARY s,
                (select s.sort,s.module_id,c.content,c.content_type from (
                select max(sort) sort,module_id from (select * from CTP_CONTENT_ALL where content is not null )  GROUP BY module_id
                ) s LEFT JOIN  CTP_CONTENT_ALL c on s.sort=c.sort and s.module_id=c.module_id ) ca where s.id=MODULE_ID and  s.HAS_ARCHIVE=1
                and CA.CONTENT_TYPE in (41,42,43,44,45)
                ) ds where not EXISTS (select * from TEMP_NUMBER10 t10 where DS.id = t10.id);
--这是过滤(41,42,43,44,45)的数据
insert into temp_number40
(select s.module_id,c.content,c.content_type,s.sort,C.ID from (
select max(sort) sort,module_id from (select module_id,sort from CTP_CONTENT_ALL where CONTENT_TYPE in (41,42,43,44,45) and content is not null )  GROUP BY module_id
) s LEFT JOIN  CTP_CONTENT_ALL c on s.sort=c.sort and s.module_id=c.module_id ) ;


            exception
                when others then
                    ROLLBACK;
                    DBMS_OUTPUT.put_line('TEMP_NUMBER10 Error No:' || SQLCODE);
                    DBMS_OUTPUT.put_line(SQLERRM);
            end;

    elsif (flag = 2) then
        --公文信息表
        begin
              --发文、收文、签报    a.EDOC_TYPE = 0控制读写哪些数据
                insert into TEMP_NUMBER20
                  select id,subject,doc_mark,issuer,send_department,pack_date,val,create_time,year,edoc_type,org from (
                      select A.id,subject,doc_mark, issuer,A.send_department,A.pack_date, 0 val,
                      to_char(a.create_time,'yyyyMMdd') create_time,to_char(a.create_time,'yyyy') year,
                      CASE A .EDOC_TYPE WHEN 0 THEN  '发文' WHEN 1 THEN  '收文' ELSE  '签报' END EDOC_TYPE,'' org
                      from
                    (select  es.* from (select * from  EDOC_SUMMARY where has_archive = 1 and EDOC_TYPE in (0,1,2)) es,
                    (select * from temp_number40 where content_type in (41,42,43,44,45) and  content is not null) con
                    where con.module_id=es.id )
                    A, (
                        select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from temp_number40 where to_char(content)
                        in (select to_char(id) from ctp_file) and CONTENT_TYPE in (41,42,43,44,45)
                        ) zall,ctp_file cf where ZALL.CONTENT=CF.id
                      ) b
                      where A.has_archive = 1
                      and a.id = b.MODULE_ID
                    --and a.EDOC_TYPE = 0
                    ) c where  exists (select * from temp_number10 t where status='0' and c.id= t.id);

              --收文
--               insert into TEMP_NUMBER20
--                 select id,subject,doc_mark,issuer,send_department,pack_date,val,create_time,year,edoc_type,org from (
--   select A.id,subject,doc_mark, issuer,A.send_department,A.pack_date, 0 val,
--   to_char(a.create_time,'yyyyMMdd') create_time,to_char(a.create_time,'yyyy') year,
--   CASE A .EDOC_TYPE WHEN 0 THEN  '发文' WHEN 1 THEN  '收文' ELSE  '签报' END EDOC_TYPE,'' org
--   from
-- (select  es.* from (select * from  EDOC_SUMMARY where has_archive = 1 and EDOC_TYPE in (0,1,2)) es,
-- (select * from temp_number40 where content_type in (41,42,43,44,45) and  content is not null) con
-- where con.module_id=es.id )
-- A, (
--     select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from temp_number40 where to_char(content)
--     in (select to_char(id) from ctp_file) and CONTENT_TYPE in (41,42,43,44,45)
--     ) zall,ctp_file cf where ZALL.CONTENT=CF.id
--   ) b
--   where A.has_archive = 1
--   and a.id = b.MODULE_ID
-- and a.EDOC_TYPE =1
-- ) c where  exists (select * from temp_number10 t where status='0' and c.id= t.id);
--
--
--
--               --签报
--               insert into TEMP_NUMBER20
--                 select id,subject,doc_mark,issuer,send_department,pack_date,val,create_time,year,edoc_type,org from (
--                 select A.id,subject,doc_mark, issuer,A.send_department,A.pack_date, 0 val,
--                 to_char(a.create_time,'yyyyMMdd') create_time,to_char(a.create_time,'yyyy') year,
--                 CASE A .EDOC_TYPE WHEN 0 THEN  '发文' WHEN 1 THEN  '收文' ELSE  '签报' END EDOC_TYPE,'' org
--                 from
--               (select  es.* from (select * from  EDOC_SUMMARY where has_archive = 1 and EDOC_TYPE in (0,1,2)) es,
--               (select * from temp_number40 where content_type in (41,42,43,44,45) and  content is not null) con
--               where con.module_id=es.id )
--               A, (
--                   select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from temp_number40 where to_char(content)
--                   in (select to_char(id) from ctp_file) and CONTENT_TYPE in (41,42,43,44,45)
--                   ) zall,ctp_file cf where ZALL.CONTENT=CF.id
--                 ) b
--                 where A.has_archive = 1
--                 and a.id = b.MODULE_ID
--               and a.EDOC_TYPE =2
--               ) c where  exists (select * from temp_number10 t where status='0' and c.id= t.id);
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
              select id,C_MIDRECID,c_filetitle,C_FTPFILEPATH,C_TYPE,I_SIZE,META_TYPE,status from(
              select B.id,A.id C_MIDRECID,case when instr(b.mime_type,'office')>0  then a.subject ||'.doc' when instr(b.mime_type,'msword')>0 then a.subject ||'.doc' when instr(b.mime_type,'pdf')>0 then a.subject ||'.pdf' when instr(b.mime_type,'excel')>0 then a.subject ||'.xls' end c_filetitle,
              '/upload/' ||
              substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 0, 4) || '/' ||
              substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 6, 2) || '/' ||
              substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 9, 2) || '/' ||
              case when instr(b.mime_type,'office')>0  then C.Filename  when instr(b.mime_type,'msword')>0 then C.Filename  when instr(b.mime_type,'pdf')>0 then C.Filename when instr(b.mime_type,'excel')>0 then C.Filename  end C_FTPFILEPATH,
              '正文' C_TYPE,
              C.FILE_SIZE I_SIZE,
              case when instr(b.mime_type,'office')>0  then '.doc' when instr(b.mime_type,'msword')>0 then '.doc' when instr(b.mime_type,'pdf')>0 then '.pdf' when instr(b.mime_type,'excel')>0 then '.xls' end META_TYPE,
              0 status
              from edoc_summary A
              left join (
              select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from temp_number40 where to_char(content) in (select to_char(id) from ctp_file) and CONTENT_TYPE in (41,42,43,44,45)
 ) zall,ctp_file cf where ZALL.CONTENT=CF.id
              ) B on B.MODULE_ID = A.Id and  A.has_archive = 1
               left join ctp_file C on to_char(B.content) = C.Id
              where B.Id is not null ) cd where exists (select * from TEMP_NUMBER10 t where t.status='0' and cd.C_MIDRECID=t.id);



            -- 添加新增文单的功能
            insert into TEMP_NUMBER30
              select id,C_MIDRECID,c_filetitle,C_FTPFILEPATH,C_TYPE,I_SIZE,META_TYPE,status from (
              select B.id,A.id C_MIDRECID,
              a.subject || '.html' c_filetitle,
              '/upload/' ||
              substr(to_char(a.Create_time, 'yyyy-mm-dd'), 0, 4) || '/' ||
              substr(to_char(a.Create_time, 'yyyy-mm-dd'), 6, 2) || '/' ||
              substr(to_char(a.Create_time, 'yyyy-mm-dd'), 9, 2) || '/' ||
              a.id C_FTPFILEPATH,
              '文单' C_TYPE,
              2048 I_SIZE,
              '.html' META_TYPE,
              0 status
              from edoc_summary A
              left join (
select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from temp_number40 where to_char(content) in (select to_char(id) from ctp_file) and CONTENT_TYPE in (41,42,43,44,45)
 ) zall,ctp_file cf where ZALL.CONTENT=CF.id) B
              on B.MODULE_ID = A.Id and  A.has_archive = 1
              where B.Id is not null ) cd where exists (select * from TEMP_NUMBER10 t where t.status='0' and cd.C_MIDRECID=t.id);
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

select id,C_MIDRECID,C_FILETITLE,C_FTPFILEPATH,C_TYPE,I_SIZE,META_TYPE,status from (
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
              0 status
              from edoc_summary A
              left join (select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from CTP_CONTENT_ALL where to_char(content) in (select to_char(id) from ctp_file) and CONTENT_TYPE in (41,42,43,44,45)
 ) zall,ctp_file cf where ZALL.CONTENT=CF.id) B
              on A.Id = B.MODULE_ID and  A.has_archive = 1
              left join ctp_attachment C
              on b.MODULE_ID = c.att_reference
              where C.id is not null ) cd
              where exists(select * from TEMP_NUMBER10 t where t.status='0' and cd.C_MIDRECID=t.id);

        exception
            when others then
                ROLLBACK;
                DBMS_OUTPUT.put_line('TEMP_NUMBER40 Error No:' || SQLCODE);
                DBMS_OUTPUT.put_line(SQLERRM);
        end;
    end if;
END;
