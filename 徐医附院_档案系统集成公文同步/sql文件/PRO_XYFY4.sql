create or replace procedure pro_xyfy4(flag number) IS
BEGIN
    if (flag = 1) then
            begin
                insert into TEMP_NUMBER10(ID,STATUS)
                  select DS.id,0 from (
                  select s.* from EDOC_SUMMARY s,CTP_CONTENT_ALL ca where s.id=MODULE_ID and  s.HAS_ARCHIVE=1 and CA.CONTENT_TYPE in (10,30)
                  ) ds where not EXISTS (select * from TEMP_NUMBER10 t10 where DS.id = t10.id);

                insert into temp_number40

                select SS.module_id,SS.CONTENT,SS.content_type,SS.sort,SS.id from EDOC_SUMMARY es,
(select c.id,s.module_id,c.content,c.content_type,s.sort from (
select max(sort) sort,module_id from (select module_id,sort from CTP_CONTENT_ALL where CONTENT_TYPE in (10,30))  GROUP BY module_id
) s LEFT JOIN  CTP_CONTENT_ALL c on s.sort=c.sort and s.module_id=c.module_id ) ss where  ES.id=SS.MODULE_ID;



            exception
                when others then
                    ROLLBACK;
                    DBMS_OUTPUT.put_line('TEMP_NUMBER10 Error No:' || SQLCODE);
                    DBMS_OUTPUT.put_line(SQLERRM);
            end;

    elsif (flag = 2) then
        --公文信息表
        begin
              --发文、收文、签报
                insert into TEMP_NUMBER20
                 select id,subject,doc_mark,issuer,send_department,pack_date,val,create_time,year,edoc_type,org from (
                select A.id,subject,doc_mark, issuer,A.send_department,A.pack_date, 0 val,to_char(a.create_time,'yyyyMMdd') create_time,to_char(a.create_time,'yyyy') year,  CASE A .EDOC_TYPE WHEN 0 THEN  '发文' WHEN 1 THEN  '收文' ELSE  '签报' END EDOC_TYPE,'' org
                from edoc_summary A, (
                SELECT  zall.* FROM  (select * from TEMP_NUMBER40 where CONTENT_TYPE in (10,30) ) zall
                ) b where A.has_archive = 1
                and a.id = b.MODULE_ID
--                  and a.EDOC_TYPE =0
                 ) cd where  exists(select * from temp_number10 t where t.status='0' and cd.id=t.id);



              --收文
--               insert into TEMP_NUMBER20
--
--                  select id,subject,doc_mark,issuer,send_department,pack_date,val,create_time,year,edoc_type,org from (
-- select A.id,subject,doc_mark, issuer,A.send_department,A.pack_date, 0 val,to_char(a.create_time,'yyyyMMdd') create_time,to_char(a.create_time,'yyyy') year,  CASE A .EDOC_TYPE WHEN 0 THEN  '发文' WHEN 1 THEN  '收文' ELSE  '签报' END EDOC_TYPE,'' org
-- from edoc_summary A, (
-- SELECT  zall.* FROM  (select * from TEMP_NUMBER40 where CONTENT_TYPE in (10,30) ) zall
-- ) b where A.has_archive = 1
-- and a.id = b.MODULE_ID
--  and a.EDOC_TYPE =1) cd where  exists(select * from temp_number10 t where t.status='0' and cd.id=t.id);



              --签报
--               insert into TEMP_NUMBER20
--                  select id,subject,doc_mark,issuer,send_department,pack_date,val,create_time,year,edoc_type,org from (
--                 select A.id,subject,doc_mark, issuer,A.send_department,A.pack_date, 0 val,to_char(a.create_time,'yyyyMMdd') create_time,to_char(a.create_time,'yyyy') year,  CASE A .EDOC_TYPE WHEN 0 THEN  '发文' WHEN 1 THEN  '收文' ELSE  '签报' END EDOC_TYPE,'' org
--                 from edoc_summary A, (
--                 SELECT  zall.* FROM  (select * from TEMP_NUMBER40 where CONTENT_TYPE in (10,30) ) zall
--                 ) b where A.has_archive = 1
--                 and a.id = b.MODULE_ID
--                  and a.EDOC_TYPE =2) cd where  exists(select * from temp_number10 t where t.status='0' and cd.id=t.id);
        exception
            when others then
                ROLLBACK;
                DBMS_OUTPUT.put_line('TEMP_NUMBER20 Error No:' || SQLCODE);
                DBMS_OUTPUT.put_line(SQLERRM);
        end;
    elsif (flag = 3) then

        begin

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
              SELECT  zall.* FROM  (select * from TEMP_NUMBER40 where  CONTENT_TYPE IN (10,30) ) zall
              ) B
              on B.MODULE_ID = A.Id and  A.has_archive = 1
              where B.Id is not null ) cd where exists (select * from TEMP_NUMBER10 t where t.status='0' and CD.C_MIDRECID=t.id);
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
              left join (
              SELECT  zall.* FROM  (select * from TEMP_NUMBER40 where   CONTENT_TYPE IN (10,30) ) zall
              ) B
              on A.Id = B.MODULE_ID and  A.has_archive = 1
              left join ctp_attachment C
              on b.MODULE_ID = c.att_reference
              where C.id is not null ) cd
              where exists (select * from TEMP_NUMBER10 t where t.status='0' and CD.C_MIDRECID=t.id);

        exception
            when others then
                ROLLBACK;
                DBMS_OUTPUT.put_line('TEMP_NUMBER40 Error No:' || SQLCODE);
                DBMS_OUTPUT.put_line(SQLERRM);
        end;
    end if;
END;
