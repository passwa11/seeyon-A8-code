package com.test;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import java.util.*;


public class RestTest {

    /***
     * method_name: getTest2
     * param: []
     * return java.lang.String
     * describe:发送公文正文接口
     * create_date: 2020-12-25
     * create_time: 8:43
     **/

    public static String getTest2() {
        RestTemplate rest = new RestTemplate();
        HttpHeaders header = new HttpHeaders();
        header.set("Accept", "application/json");
        HttpEntity<String> httpEntity = new HttpEntity<String>(header);
        String token = rest.getForObject("http://10.60.1.243:8888/seeyon/rest/token/test3/zenyu123?loginName=test1",String.class,httpEntity);
        System.out.println("token:"+token);
        JSONObject param = new JSONObject();
        param.put("edoctable", "1733856539970063176");//重要写死
        param.put("my:create_person", "test1");//写死
        param.put("my:send_unit", "才子科技");//单位写死
        param.put("my:send_unit_id", "Account|-5203401707280839389");//单位ID写死
        param.put("my:send_department", "销售部");//部门写死
        param.put("my:send_department_id", "Department|-6846924598524754243");//部门写死
        param.put("my:doc_mark", "0|GW0005||3");//公文
        param.put("subject", "测试文件标题15");//公文标题
        param.put("my:text1", "测试备注");//备注
        param.put("templeteId", "2549384184740269144");//流程模板
        param.put("templeteProcessId", "5730328126105275384");//
        param.put("workflow_node_peoples_input", "");
        HashMap hh=new HashMap();
        hh.put("matchRequestToken","PC-6124365271652712753-t-1608875121653");
        List<HashMap> list=new ArrayList<>();
        HashMap child=new HashMap();
        child.put("isDelete",false);
        child.put("nodeId","15522671729601");
        list.add(child);
        param.put("workflow_node_condition_input",list);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        HttpEntity ____httpEntity = new HttpEntity<>(param, headers);

        Map res = rest.postForObject("http://10.60.1.243:8888/seeyon/rest/edocResource/sendExternal?token="+token, ____httpEntity,Map.class);
        System.out.println(res+"");
        return "";
    }
    /***
     * method_name: getTest
     * param: []
     * return java.lang.String
     * describe:撤销公文接口
     * create_date: 2020-12-25
     * create_time: 8:44
     **/

    public static String getTest() {
        RestTemplate rest = new RestTemplate();
        HttpHeaders header = new HttpHeaders();
        header.set("Accept", "application/json");
        HttpEntity<String> httpEntity = new HttpEntity<String>(header);
        String token = rest.getForObject("http://10.60.1.243:8888/seeyon/rest/token/test3/zenyu123?loginName=test1",String.class,httpEntity);
        System.out.println("token:"+token);
        JSONObject param = new JSONObject();
        param.put("affairId", "3226296646353506122");//在公文里面有返回
        param.put("id", "-4587226644105838206");//公文的ID
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        HttpEntity ____httpEntity = new HttpEntity<>(param, headers);
        Map res = rest.postForObject("http://10.60.1.243:8888/seeyon/rest/edocResource/repealExternal?token="+token, ____httpEntity,Map.class);
        System.out.println(res+"");
        return token;
    }
    /***
     * method_name: getTest3
     * param: []
     * return java.lang.String
     * describe:附件和公文绑定  多个附件调用多次
     * create_date: 2020-12-25
     * create_time: 8:58
     **/

    public static String getTest3() {
        RestTemplate rest = new RestTemplate();
        HttpHeaders header = new HttpHeaders();
        header.set("Accept", "application/json");
        HttpEntity<String> httpEntity = new HttpEntity<String>(header);
        String token = rest.getForObject("http://10.60.1.243:8888/seeyon/rest/token/test3/zenyu123?loginName=test1",String.class,httpEntity);
        System.out.println("token:"+token);
        JSONObject param = new JSONObject();
        param.put("reference", "-4587226644105838206");//公文ID
        param.put("filename", "报表模板-线索统计分析表.xlsx");//文件名
        param.put("file_url", "5597846397060795638"); //ctp_file附件的ID
        param.put("mime_type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");//ctp_file这个表里有
        param.put("attachment_size", "9297");//ctp_file这个表里有
        param.put("sort", "0");//顺序
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        HttpEntity ____httpEntity = new HttpEntity<>(param, headers);
        Map res = rest.postForObject("http://10.60.1.243:8888/seeyon/rest/edocResource/insertCtpAttachment?token="+token, ____httpEntity,Map.class);
        System.out.println(res+"");
        return token;
    }
    public static void main(String[] args) {
        //{affairId=3226296646353506122, returnValue=true, id=-4587226644105838206}
        //getTest3(); //附件和公文绑定
        //getTest2();// 发公文
        getTest();//撤销公文
    }

}
