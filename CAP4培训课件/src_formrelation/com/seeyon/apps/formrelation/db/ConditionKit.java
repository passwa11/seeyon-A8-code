package com.seeyon.apps.formrelation.db;

import com.seeyon.apps.customFieldCtrl.kit.CAP4FormKit;
import com.seeyon.cap4.form.bean.FormBean;

/**
 * @author Fangaowei
 * 
 *         <pre>
 *         解析where条件
 *         </pre>
 * 
 * @date 2018年11月13日 下午9:46:16 
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class ConditionKit {

    public static String getWhere(FormBean form, FormCondition condition) {
        String where = "";
        String field = CAP4FormKit.getFieldTaleId(form, condition.getDisplay());
        if(null == field) {
            return "";
        }
        Operation opera = condition.getOpera();
        where = " and " + field + " " + opera.getOpera();
        switch(opera) {
            case between :
                where += " ? and ? ";
                break;
            case in :
                // 这里直接写成 1,2,3,4,5
                where += " (?) ";
                break;
            case like :
                where += " %?% ";
                break;
            default :
                where += " ? ";
                break;
        }
        return where;
    }
}
