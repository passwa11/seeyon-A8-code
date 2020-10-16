package com.seeyon.apps.formrelation.kit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.customFieldCtrl.kit.CAP4FormKit;
import com.seeyon.apps.customFieldCtrl.kit.StrKit;
import com.seeyon.apps.formrelation.db.ConditionKit;
import com.seeyon.apps.formrelation.db.FormCondition;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.ctp.util.JDBCAgent;

/**
 * <pre>
 * 直接操作数据库的类
 * </pre>
 * @date 2018年11月8日 上午11:10:15
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class DBKit {
    
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> excuteSQL(String sql, List<Object> params) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        JDBCAgent jdbc = new JDBCAgent(true);
        try {
            if(null == params) {
                jdbc.execute(sql);
            } else {
                jdbc.execute(sql, params);
            }
            if(!sql.startsWith("update")) {
                list = jdbc.resultSetToList();
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            jdbc.close();
        }
        return list;
    }
    
    /**
     * 不成熟，按照这个自己进行sql的编写，只需要获取字段是 数据库的 field000？ 就行  根据cap4formkit。getFieldTableId
     * sub表也一样，只是多一步判断而已，如果只有一个重复表就不存在
     * @param formbean  根据表单编号获取
     * @param fields    field1,"所属部门"  会解析成   field000x as field1, 如果不存在则直接用后面的
     * @param conditions
     * @param params
     * @return
     * @throws Exception
     */
    public static List<Map<String, Object>> selectSQL(FormBean formbean, Map<String, String> fields, List<FormCondition> conditions) throws Exception {
        List<Object> params = new ArrayList<Object>();
        StringBuffer sb = new StringBuffer();
        sb.append("select ");
        for(String key : fields.keySet()) {
            String field = CAP4FormKit.getFieldTaleId(formbean, fields.get(key));
            if(StrKit.isNull(field)) {
                field = fields.get(key);
            }
            sb.append(" ").append(field).append(" as ").append(key).append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append(" from ").append(formbean.getMasterTableBean().getTableName());
        sb.append(" where 1 = 1 ");
        if(!StrKit.isNull(conditions)) {
            for(FormCondition c : conditions) {
                sb.append(ConditionKit.getWhere(formbean, c));
                params.add(c.getParam1());
                if(null != c.getParam2()) {
                    params.add(c.getParam2());
                }
            }
        }
        return excuteSQL(sb.toString(), params);
    }
    
}
