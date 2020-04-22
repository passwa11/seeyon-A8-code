package com.seeyon.apps.ext;

//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.seeyon.apps.ext.Portal190724.po.Result;
//import com.seeyon.apps.ext.Portal190724.po.ResultInfo;


//import com.alibaba.fastjson.JSONArray;
import com.seeyon.apps.ext.Portal190724.po.Contract;
import com.seeyon.apps.ext.Portal190724.po.Result;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 2019-8-27.
 */
public class TestDemo {
    public static void main(String[] args) {
//        String json = "{\"status\":200,\"message\":\"成功\",\"result\":{\"expires_in\":\"3600000\",\"token\":\"8A356A48B066F03233DAA0ACC531694D\"}}";
//        JSONObject jsonObject = JSONObject.fromObject(json);
//        Result result = (Result) JSONObject.toBean(jsonObject, Result.class);
//        System.out.println(result);
//        JSONObject jsonObject=JSONObject.parseObject(json);
//        ResultInfo resultInfo=JSONObject.parseObject(jsonObject.get("result").toString(),ResultInfo.class);
//        System.out.println(resultInfo.getToken());
//
//        Result result= JSON.parseObject(json,Result.class);
//        System.out.println(result.getResultInfo());
//        System.out.println(jsonArray);

        method2();
    }


    public static void method2(){
        String str="{\"status\":200,\"message\":\"成功\",\"result\":{\"total\":1,\"currPage\":1,\"data\":[{\"taskName\":\"供电协议-杨屯镇人民政府（农望达塑料厂-姚西17线路）使用网供电协议\",\"busiType\":\"合同基本信息\",\"busiTypeId\":\"DAO_CONT_INFO\",\"createOrg\":\"江苏大屯电热有限公司\",\"createUser\":\"刘柱海\",\"beginTime\":\"2019-6-20 18:11:50\",\"taskUrl\":\"http://172.16.3.108:9595/law/todo/approvalTask.htm?userId=1555652101824&actTaskId=203715832\",\"handleUser\":\"张新军\",\"appTaskId\":\"203715832\"}],\"totalPage\":1,\"pageSize\":100}}";
        com.alibaba.fastjson.JSONObject jsonObject= com.alibaba.fastjson.JSONObject.parseObject(str);
        com.alibaba.fastjson.JSONObject result= com.alibaba.fastjson.JSONObject.parseObject(jsonObject.get("result").toString());
        System.out.println(result.get("data"));
        List<Contract> list= com.alibaba.fastjson.JSONObject.parseArray(result.get("data").toString(),Contract.class);
        System.out.println(list.size());

        Result s= com.alibaba.fastjson.JSONObject.parseObject(str,Result.class);
        System.out.println(s);
        JSONArray json = JSONArray.fromObject(jsonObject.get("result"));
//        System.out.println(array.toString());
    }
}
