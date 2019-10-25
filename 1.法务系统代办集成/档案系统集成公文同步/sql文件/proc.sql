--从82上拷贝下来的文件
create or replace procedure pro_number_test(flag number) IS

-- 2015-06-12 modify
-- temp_id TEMP_NUMBER4.id%type;
-- temp_filetitle TEMP_NUMBER4.c_filetitle%type;
-- temp_filepath TEMP_NUMBER4.c_ftpfilepath%type;

BEGIN

  if (flag = 1) then
    --临时记录此次同步的公文信息表主键
    begin
      insert into TEMP_NUMBER1

        select to_char(A.id)
          from edoc_summary A
          left join edoc_body B
            on A.Id = B.Edoc_Id
         where has_archive = 1
           and A.id not in (select id from S_OADATA@DBLINK_OA_FOR_ARC);

    end;
  elsif (flag = 2) then
      --公文信息表
      begin
        insert into TEMP_NUMBER2
          select A.id,
                 subject,
                 doc_mark,
                 case edoc_type
                   when 0 then
                    '发文'
                   when 1 then
                    '收文'
                 end edoc_type,
                 7secret_level,
                 '',
                 '',
                 '',
                 '',
                 '',
                 '',
                 issuer,
                 '核稿人',
                 A.send_department,
                 A.issuer,
                 A.pack_date,
                 A.COPIES2,
                 '参加人',
                 to_char(A.SEND_TO2),
                 to_char(A.COPY_TO2),
                 '分送领导',
                 to_char(A.SEND_DEPARTMENT),
                 '分送',
                 keywords,
                 '主要内容',
                 A.Send_Unit,
                 '印发时间',
                 '来文字号',
                 '所属单位：大屯',
                 '',
                 0,
                 c.name,
                 e.name
            from edoc_summary A, edoc_body b, org_member c, doc_resources d, ORG_UNIT e
           where A.has_archive = 1
             and a.id = b.edoc_id
             and c.id = d.create_user_id
             and c.org_department_id = e.id
             and a.id = d.source_id

             and A.id not in (select id from S_OADATA@DBLINK_OA_FOR_ARC);

        insert into S_OADATA@DBLINK_OA_FOR_ARC
          (id,
           fldsubject,
           fldfwbh,
           fldlx,
           fldmiji,
           fldmeetingtime,
           fldmeetinglocal,
           fldjiluren,
           flddianhua,
           fldzhuchiren,
           fldfuzeren,
           fldqianfa,
           fldhegao,
           fldzbdw,
           fldnigaoren,
           fldqicaorq,
           fldfenshu,
           fldcanjiaren,
           fldzhusongdw_show,
           fldchaosong_show,
           fldfasongry,
           fldchaobao_show,
           fldfensong,
           fldtopic,
           fldmemo,
           fldlwjg,
           fldyinfadate,
           fldzh,
           fldcompany,
           fldtype,
           i_state,
           fldperName,
           fldorgName)
          select * from TEMP_NUMBER2;
      exception
        when others then
          ROLLBACK;
          DBMS_OUTPUT.put_line('TEMP_NUMBER2 Error No:' || SQLCODE);
          DBMS_OUTPUT.put_line(SQLERRM);
      end;
  elsif (flag = 3) then
        --公文信息正文表
        begin
          insert into TEMP_NUMBER3
            select B.id,
                   A.id C_MIDRECID,
                   -- 2014.11.03 modify
                   --C.Filename C_FILETITLE,
                   a.subject || '.doc' c_filetitle,
                   '/upload/' ||
                   substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 0, 4) || '/' ||
                   substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 6, 2) || '/' ||
                   substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 9, 2) || '/' ||
                   -- 20141022 modify 去掉filename替换功能，只保留原有的filename
                   -- replace(C.Filename, '-', '') || '/' || C.Filename C_FTPFILEPATH,
                   C.Filename || '.doc' C_FTPFILEPATH,
                   '正文' C_TYPE,
                   C.FILE_SIZE I_SIZE
              from edoc_summary A
              left join (select *
                           from edoc_body
                          where content_type <> 'HTML') B
                on B.Edoc_Id = A.Id
              left join ctp_file C
                on to_char(B.content) = C.Id
             where B.Id is not null
             -- 2014/10/10 modify
               and A.id in (select id from TEMP_NUMBER1);

          -- 2015-06-11 modify 添加新增文单的功能
          insert into TEMP_NUMBER3
          select B.id,
                   A.id C_MIDRECID,
                   a.subject || '.html' c_filetitle,
                   '/upload/' ||
                   substr(to_char(a.Create_time, 'yyyy-mm-dd'), 0, 4) || '/' ||
                   substr(to_char(a.Create_time, 'yyyy-mm-dd'), 6, 2) || '/' ||
                   substr(to_char(a.Create_time, 'yyyy-mm-dd'), 9, 2) || '/' ||
                   a.id || '.html' C_FTPFILEPATH,
                   '文单' C_TYPE,
                   2048 I_SIZE
              from edoc_summary A
              left join edoc_body B
                on B.Edoc_Id = A.Id
             where B.Id is not null
             and A.id in (select id from TEMP_NUMBER1);

          insert into S_OAFILE@DBLINK_OA_FOR_ARC
            (id, c_midrecid, c_filetitle, c_ftpfilepath, c_type, i_size)
            select * from TEMP_NUMBER3;

        exception
          when others then
            ROLLBACK;
            DBMS_OUTPUT.put_line('TEMP_NUMBER3 Error No:' || SQLCODE);
            DBMS_OUTPUT.put_line(SQLERRM);
        end;
  elsif (flag = 4) then
        --公文信息表附件表
        begin
          insert into TEMP_NUMBER4
            select C.id,
                   A.Id C_MIDRECID,
                   C.Filename C_FILETITLE,
                   '/upload/' ||
                   substr(to_char(C.createdate, 'yyyy-mm-dd'), 0, 4) || '/' ||
                   substr(to_char(C.createdate, 'yyyy-mm-dd'), 6, 2) || '/' ||
                   substr(to_char(C.createdate, 'yyyy-mm-dd'), 9, 2) || '/' ||
                   -- C.file_url  C_FTPFILEPATH,
                   -- 2015-06-12 modify
                   C.file_url || substr(C.Filename, instr(C.Filename, '.', -1, 1)) C_FTPFILEPATH,
                   '附件' C_TYPE,
                   C.attachment_size I_SIZE
              from edoc_summary A
              left join edoc_body B
                on A.Id = B.Edoc_Id
              left join ctp_attachment C
                -- 2014/11/04 modify
                --on B.id = C.reference
                  on b.Edoc_Id = c.att_reference
             where C.id is not null
             --  2014/10/10 modify
               and A.id in (select id from TEMP_NUMBER1);
             -- 2014/11/04 modify
          insert into S_OAFILE@DBLINK_OA_FOR_ARC
            (id, c_midrecid, c_filetitle, c_ftpfilepath, c_type, i_size)
            select * from TEMP_NUMBER4;

        exception
          when others then
            ROLLBACK;
            DBMS_OUTPUT.put_line('TEMP_NUMBER4 Error No:' || SQLCODE);
            DBMS_OUTPUT.put_line(SQLERRM);
        end;

        -- 文单信息表
        begin
           insert into S_OAFORM@DBLINK_OA_FOR_ARC
           (id ,fldsubject, formid, form_name, form_type,  formcontent, create_time)
           select a.id, a.subject, b.id, b.name,
           b.type,
           b.content,
           b.create_time
           from edoc_summary a, edoc_form b
           where
           a.has_archive = 1
           and a.form_id = b.id
           and A.id in (select id from TEMP_NUMBER1);

           insert into S_OAOPINION@DBLINK_OA_FOR_ARC
           (id, attribute, opinion_type, content, is_hidden, create_time, name)
           select a.id, c.attribute, c.opinion_type, c.content, c.is_hidden, c.create_time, d.name
           from edoc_summary a, edoc_opinion c,org_member d
           where
           a.has_archive = 1
           and a.id = c.edoc_id
           and c.create_user_id = d.id
           and a.id in (select id from TEMP_NUMBER1);

          exception
            when others then
              ROLLBACK;
              DBMS_OUTPUT.put_line('TEMP_NUMBER5 Error No:' || SQLCODE);
              DBMS_OUTPUT.put_line(SQLERRM);
          end;

  end if;
END;
