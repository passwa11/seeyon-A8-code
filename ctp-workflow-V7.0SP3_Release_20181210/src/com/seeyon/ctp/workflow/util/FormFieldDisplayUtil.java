package com.seeyon.ctp.workflow.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.bo.WorkflowFormFieldBO;

public class FormFieldDisplayUtil {
    
    
    /**
     * 
     * @param fieldList
     * @return
     * @throws Exception
     */
    public static String parseWorkflowBranchFormFieldVOListToJSON(List<WorkflowFormFieldBO> fieldList) throws Exception{
        String json = "[]";
        if(fieldList!=null && fieldList.size()>0){
            List<Map<String, Object>> fieldMapList = new ArrayList<Map<String, Object>>(fieldList.size());
            for(WorkflowFormFieldBO field : fieldList){
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("name", field.getName());
                map.put("display", field.getDisplay());
                map.put("fieldType", field.getFieldType());
                map.put("ownerTableName", field.getOwnerTableName());
                map.put("inputType", field.getInputType());
                map.put("enumId", String.valueOf(field.getEnumId()));
                fieldMapList.add(map);
            }
            json = JSONUtil.toJSONString(fieldMapList);
        }
        return json;
    }
}
