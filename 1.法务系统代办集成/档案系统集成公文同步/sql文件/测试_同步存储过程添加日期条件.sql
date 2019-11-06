CREATE OR REPLACE
procedure pro_dt_test(flag number,date1 in varchar2,date2 in varchar2) IS
BEGIN

    if (flag = 1) then
        --临时记录此次同步的公文信息表主键

        begin
        if(date1<>'null') then
                insert into TEMP_NUMBER1(ID)
                select to_char(A.id) from edoc_summary A left join (select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from CTP_CONTENT_ALL where to_char(content) in (select to_char(id) from ctp_file)) zall,ctp_file cf where ZALL.CONTENT=CF.id) B on A.Id = B.MODULE_ID  where has_archive = 1
                and (a.CREATE_TIME BETWEEN to_date(date1,'yyyy-MM-dd') and to_date(date2,'yyyy-MM-dd'))
                and A.id not in (select id from S_OADATA@testlink);
        else
        insert into TEMP_NUMBER1(ID) select to_char(A.id) from edoc_summary A left
                join (select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from CTP_CONTENT_ALL where to_char(content) in (select to_char(id) from ctp_file)) zall,ctp_file cf where ZALL.CONTENT=CF.id) B on A.Id = B.MODULE_ID
                where has_archive = 1
                and A.id not in (select id from S_OADATA@testlink);
        end if;
        exception
        when others then
                ROLLBACK;
                DBMS_OUTPUT.put_line('TEMP_NUMBER2 Error No:' || SQLCODE);
                DBMS_OUTPUT.put_line(SQLERRM);
        end;

    elsif (flag = 2) then
        --公文信息表
        begin
				if(date1<>'null') then
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
                   0
                   --,c.name,
                   -- e.name
            from edoc_summary A, (select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from CTP_CONTENT_ALL where to_char(content) in (select to_char(id) from ctp_file)) zall,ctp_file cf where ZALL.CONTENT=CF.id) b, org_member c, (select CA.OBJECT_ID,DR.CREATE_USER_ID from CTP_AFFAIR ca,DOC_RESOURCES dr where ca.id=DR.SOURCE_ID) d, ORG_UNIT e
            where A.has_archive = 1
              and a.id = b.MODULE_ID
              and c.id = d.create_user_id
              and c.org_department_id = e.id
              and a.id = d.OBJECT_ID
							and (a.CREATE_TIME BETWEEN to_date(date1,'yyyy-MM-dd') and to_date(date2,'yyyy-MM-dd'))
              and A.id not in (select id from S_OADATA@testlink);
						ELSE
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
												 0
												 --,c.name,
												 -- e.name
									from edoc_summary A, (select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from CTP_CONTENT_ALL where to_char(content) in (select to_char(id) from ctp_file)) zall,ctp_file cf where ZALL.CONTENT=CF.id) b, org_member c, (select CA.OBJECT_ID,DR.CREATE_USER_ID from CTP_AFFAIR ca,DOC_RESOURCES dr where ca.id=DR.SOURCE_ID) d, ORG_UNIT e
									where A.has_archive = 1
										and a.id = b.MODULE_ID
										and c.id = d.create_user_id
										and c.org_department_id = e.id
										and a.id = d.OBJECT_ID
										and A.id not in (select id from S_OADATA@testlink);
						end if;
            insert into S_OADATA@testlink
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
             i_state
                --fldperName,
                --fldorgName
            )
            select id,
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
                   i_state from TEMP_NUMBER2;

        exception
            when others then
                ROLLBACK;
                DBMS_OUTPUT.put_line('TEMP_NUMBER2 Error No:' || SQLCODE);
                DBMS_OUTPUT.put_line(SQLERRM);
        end;
    elsif (flag = 3) then
        --公文信息正文表
        begin
					if(date1<>'null') then
            insert into TEMP_NUMBER3
            select B.id,A.id C_MIDRECID,
                   case when instr(b.mime_type,'office')>0  then a.subject ||'.doc' when instr(b.mime_type,'msword')>0 then a.subject ||'.doc' when instr(b.mime_type,'pdf')>0 then a.subject ||'.pdf'
                        when instr(b.mime_type,'excel')>0 then a.subject ||'.xls' end c_filetitle,
                   '/upload/' ||
                   substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 0, 4) || '/' ||
                   substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 6, 2) || '/' ||
                   substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 9, 2) || '/' ||
                   case when instr(b.mime_type,'office')>0  then C.Filename ||'.doc' when instr(b.mime_type,'msword')>0 then C.Filename ||'.doc' when instr(b.mime_type,'pdf')>0 then C.Filename ||'.pdf'
                        when instr(b.mime_type,'excel')>0 then C.Filename ||'.xls' end C_FTPFILEPATH,
                   '正文' C_TYPE,
                   C.FILE_SIZE I_SIZE
            from edoc_summary A
                     left join (select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from CTP_CONTENT_ALL where to_char(content) in (select to_char(id) from ctp_file)) zall,ctp_file cf where ZALL.CONTENT=CF.id) B
                               on B.MODULE_ID = A.Id
                     left join ctp_file C
                               on to_char(B.content) = C.Id
            where B.Id is not null
							and (a.CREATE_TIME BETWEEN to_date(date1,'yyyy-MM-dd') and to_date(date2,'yyyy-MM-dd'))
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
                     left join (select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from CTP_CONTENT_ALL where to_char(content) in (select to_char(id) from ctp_file)) zall,ctp_file cf where ZALL.CONTENT=CF.id) B
                               on B.MODULE_ID = A.Id
            where B.Id is not null
							and (a.CREATE_TIME BETWEEN to_date(date1,'yyyy-MM-dd') and to_date(date2,'yyyy-MM-dd'))
              and A.id in (select id from TEMP_NUMBER1);
					ELSE
						insert into TEMP_NUMBER3
            select B.id,A.id C_MIDRECID,
                   case when instr(b.mime_type,'office')>0  then a.subject ||'.doc' when instr(b.mime_type,'msword')>0 then a.subject ||'.doc' when instr(b.mime_type,'pdf')>0 then a.subject ||'.pdf'
                        when instr(b.mime_type,'excel')>0 then a.subject ||'.xls' end c_filetitle,
                   '/upload/' ||
                   substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 0, 4) || '/' ||
                   substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 6, 2) || '/' ||
                   substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 9, 2) || '/' ||
                   case when instr(b.mime_type,'office')>0  then C.Filename ||'.doc' when instr(b.mime_type,'msword')>0 then C.Filename ||'.doc' when instr(b.mime_type,'pdf')>0 then C.Filename ||'.pdf'
                        when instr(b.mime_type,'excel')>0 then C.Filename ||'.xls' end C_FTPFILEPATH,
                   '正文' C_TYPE,
                   C.FILE_SIZE I_SIZE
            from edoc_summary A
                     left join (select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from CTP_CONTENT_ALL where to_char(content) in (select to_char(id) from ctp_file)) zall,ctp_file cf where ZALL.CONTENT=CF.id) B
                               on B.MODULE_ID = A.Id
                     left join ctp_file C
                               on to_char(B.content) = C.Id
            where B.Id is not null

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
                     left join (select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from CTP_CONTENT_ALL where to_char(content) in (select to_char(id) from ctp_file)) zall,ctp_file cf where ZALL.CONTENT=CF.id) B
                               on B.MODULE_ID = A.Id
            where B.Id is not null

              and A.id in (select id from TEMP_NUMBER1);
					end if;

            insert into S_OAFILE@testlink
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
					if(date1<>'null') then
            insert into TEMP_NUMBER4
            select C.id,
                   A.Id C_MIDRECID,
                   C.Filename C_FILETITLE,
                   '/upload/' ||
                   substr(to_char(C.createdate, 'yyyy-mm-dd'), 0, 4) || '/' ||
                   substr(to_char(C.createdate, 'yyyy-mm-dd'), 6, 2) || '/' ||
                   substr(to_char(C.createdate, 'yyyy-mm-dd'), 9, 2) || '/' ||
                   C.file_url || substr(C.Filename, instr(C.Filename, '.', -1, 1)) C_FTPFILEPATH,
                   '附件' C_TYPE,
                   C.attachment_size I_SIZE
            from edoc_summary A
                     left join (select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from CTP_CONTENT_ALL where to_char(content) in (select to_char(id) from ctp_file)) zall,ctp_file cf where ZALL.CONTENT=CF.id) B
                               on A.Id = B.MODULE_ID
                     left join ctp_attachment C
                               on b.MODULE_ID = c.att_reference
            where C.id is not null
							and (a.CREATE_TIME BETWEEN to_date(date1,'yyyy-MM-dd') and to_date(date2,'yyyy-MM-dd'))
              and A.id in (select id from TEMP_NUMBER1);
					ELSE
						insert into TEMP_NUMBER4
            select C.id,
                   A.Id C_MIDRECID,
                   C.Filename C_FILETITLE,
                   '/upload/' ||
                   substr(to_char(C.createdate, 'yyyy-mm-dd'), 0, 4) || '/' ||
                   substr(to_char(C.createdate, 'yyyy-mm-dd'), 6, 2) || '/' ||
                   substr(to_char(C.createdate, 'yyyy-mm-dd'), 9, 2) || '/' ||
                   C.file_url || substr(C.Filename, instr(C.Filename, '.', -1, 1)) C_FTPFILEPATH,
                   '附件' C_TYPE,
                   C.attachment_size I_SIZE
            from edoc_summary A
                     left join (select zall.*,CF.MIME_TYPE,CF.id from (select to_number(content) content,MODULE_ID from CTP_CONTENT_ALL where to_char(content) in (select to_char(id) from ctp_file)) zall,ctp_file cf where ZALL.CONTENT=CF.id) B
                               on A.Id = B.MODULE_ID
                     left join ctp_attachment C
                               on b.MODULE_ID = c.att_reference
            where C.id is not null
              and A.id in (select id from TEMP_NUMBER1);
					end if;
            -- 2014/11/04 modify
            insert into S_OAFILE@testlink
            (id, c_midrecid, c_filetitle, c_ftpfilepath, c_type, i_size)
            select * from TEMP_NUMBER4;

        exception
            when others then
                ROLLBACK;
                DBMS_OUTPUT.put_line('TEMP_NUMBER4 Error No:' || SQLCODE);
                DBMS_OUTPUT.put_line(SQLERRM);
        end;
    end if;
END;
