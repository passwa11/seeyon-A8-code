package com.seeyon.apps.sap.rest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.sap.entity.FileEntity;
import com.seeyon.apps.sap.kit.Base64Kit;
import com.seeyon.apps.sap.kit.HttpKit;
import com.seeyon.apps.sap.kit.JsonKit;

/**
 * Description
 * 
 * <pre></pre>
 */
public class RestTest {

    // 修改为客户环境的ip地址
    private static String URL = "http://127.0.0.1/seeyon/rest/";
    
    //private static String URL = "http://122.224.100.82:6789/seeyon/rest/";
    public static void main(String[] args) throws Exception {
        String token = postToken("kkdo", "fgw123456");
        // 发起表单
        //mm(token);
        
        // 上传附件
        upload(token);
    }
    
    private static void mm(String token) throws Exception {
    	//SystemPropertyAction.
    	Map<String, Object> param = new HashMap<String, Object>();
    	String url = URL + "flow/discard?token=" + token;
    	// 登录名 固定
    	param.put("senderLoginName", "seeyon");
    	// 采购订单标题
    	param.put("subject", "测试发起OA报废单");
    	// 固定值 固定参数
    	param.put("transfertype", "json");
    	
    	Map<String, Object> data = new HashMap<String, Object>();
    	
    	data.put("申请人", "seeyon");
    	data.put("申请时间", "2019-04-30");
    	data.put("申请人电话", "17313129779");
    	data.put("名称", "电脑");
    	data.put("内部编号", "201904300001");
    	data.put("始用日期", "2019-01-01");
    	data.put("价格", "3200.50");
    	data.put("资金来源", "未知");
    	data.put("申报原因", "内存条太小，无法使用了");




    	/*List<Map<String, Object>> subs = new ArrayList<>();
		Map<String, Object> sub1 = new HashMap<>();
		sub1.put("料品编码", "001|0002");
		sub1.put("料品名称", "测试料品2");
		sub1.put("数量", 2);
		sub1.put("单价", 22);
		sub1.put("交期", "");
		sub1.put("计划到货日", "");
		subs.add(sub1);
		
		Map<String, Object> sub2 = new HashMap<>();
		sub2.put("料品编码", "001|0003");
		sub2.put("料品名称", "测试料品3");
		sub2.put("数量", 1);
		sub2.put("单价", 23);
		sub2.put("交期", "");
		sub2.put("计划到货日", "");
		subs.add(sub2);
		
		Map<String, Object> sub3 = new HashMap<>();
		sub3.put("料品编码", "001|0001");
		sub3.put("料品名称", "测试料品1");
		sub3.put("数量", 23);
		sub3.put("单价", 12);
		sub3.put("交期", "");
		sub3.put("计划到货日", "");
		subs.add(sub3);*/
    	
    	//data.put("sub", subs);
    	
    	param.put("data", data);

    	FileUtils.writeStringToFile(new File("E:\\sap\\qingqiu.json"), JsonKit.toJson(param));
    	String res = HttpKit.post(url, JsonKit.toJson(param));
    	System.out.println(res);

        //FileUtils.writeStringToFile(new File("E:\\sap\\flow.json"), res);
    	
    	//map.put("", value);
    }
    
    private static void upload(String token) throws Exception {
    	Map<String, Object> params = new HashMap<>();
    	List<FileEntity> files = new ArrayList<>();
    	FileEntity file1 = new FileEntity();
    	file1.setFileName("qingqiu");
    	file1.setSuffix("json");
    	file1.setBase64Str(Base64Kit.getBase64(new File("E:/test/qingqiu.json")));
    	files.add(file1);
    	
    	FileEntity file2 = new FileEntity();
    	file2.setFileName("test");
    	file2.setSuffix("docx");
    	file2.setBase64Str(Base64Kit.getBase64(new File("E:/test/test.docx")));
    	files.add(file2);
    	
    	FileEntity file3 = new FileEntity();
    	file3.setFileName("合同底表");
    	file3.setSuffix("pdf");
    	file3.setBase64Str(Base64Kit.getBase64(new File("E:/test/合同底表.pdf")));
    	files.add(file3);
    	
    	params.put("loginName", "dsz");
    	params.put("files", files);
    	
    	String url = URL + "upload/file?token=" + token;
    	
    	String jsonpara = JsonKit.toJson(params);
    	
        FileUtils.writeStringToFile(new File("E:\\test\\files.json"), jsonpara);

    	String res = HttpKit.post(url, jsonpara);
    	
        FileUtils.writeStringToFile(new File("E:\\test\\res.json"), res);
    	
    	JSONObject json = JSON.parseObject(res);
    	
    	int code = json.getIntValue("code");
    	// code=0 接口成功
    	if(code == 0) {
    		System.out.println("返回的fieldIds == " + json.getString("data"));
    	} else {
    		System.out.println("错误信息：" + json.getString("msg"));
    	}
    	
    	

    }


    private static String postToken(String user, String password) throws Exception {
        String url = URL + "token";
        Map<String, Object> restuser = new HashMap<String, Object>();
        restuser.put("userName", user);
        restuser.put("password", password);
        String res = HttpKit.post(url, JsonKit.toJson(restuser));
        FileUtils.writeStringToFile(new File("E:\\sap\\token.json"), res);
        String token = "";
        try {
            token = JsonKit.parse(res).getString("id");
        } catch(Exception e) {
            token = res;
        }
        return token;
    }

}
