create or replace procedure pro_xyfy6(flag number) IS
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
    end if;
END;
