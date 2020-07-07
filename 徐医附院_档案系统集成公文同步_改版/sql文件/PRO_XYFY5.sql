create or replace procedure pro_xyfy5(flag number) IS
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
    end if;
END;
