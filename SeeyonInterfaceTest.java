package com.seeyon.apps.cslg.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.cslg.util.JDBCUtil;
import com.seeyon.client.CTPRestClient;
import com.seeyon.client.CTPServiceClientManager;

public class SeeyonInterfaceTest {

	public static void main(String[] args) {


		String url = "http://127.0.0.1:8086";//OA的登录URL
		CTPServiceClientManager clientManager = CTPServiceClientManager.getInstance(url);
		CTPRestClient client = clientManager.getRestClient();
		client.authenticate("rest", "123456");//由致远OA提供
		//生成token的代码
		String s = client.get("/token/rest/123456", String.class);
		JSONObject jsons = JSONObject.parseObject(s);
		String token = (String) jsons.get("id");
		System.out.println("token : " + token);

		//根据用户名获取对应的ID
		String userName = "yanfa01";//员工账号
		JSONObject jsonstr = JSONObject.parseObject(client.get("/orgMember?loginName="+userName, String.class));
		String memberId = jsonstr.getString("id");
		System.out.println("memberId : " + memberId);
		
		
		
		List<Map> rows11 = new ArrayList<Map>();
		String sql = "SELECT * from ctp_affair where MEMBER_ID = '"+memberId+"' and STATE = '3' order by RECEIVE_TIME desc";
		List<Map> sss = JDBCUtil.doQuery(sql);
		System.out.println("sss.size() : " + sss.size());
		if (sss.size() < 1) {
		} else {
			if(sss.size()>5){
				for (int i = 0; i < 5; i++) {
					Map<String, Object> jsonObject = sss.get(i);
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("subject", String.valueOf(jsonObject.get("subject")));
					map.put("id",String.valueOf(jsonObject.get("id")));
					map.put("receiveTime",String.valueOf(jsonObject.get("receive_time")));
					map.put("app",String.valueOf(jsonObject.get("app")));
					rows11.add(map);
				}
			}else{
				for (int i = 0; i < sss.size(); i++) {
					Map<String, Object> jsonObject = sss.get(i);
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("subject", String.valueOf(jsonObject.get("subject")));
					map.put("id",String.valueOf(jsonObject.get("id")));
					map.put("receiveTime",String.valueOf(jsonObject.get("receive_time")));
					map.put("app",String.valueOf(jsonObject.get("app")));
					rows11.add(map);
				}
			}
		}
		
		
		
		
		
		
		
		//根据用户名加密获取对应的ticket(身份认证)
		char[] encodeStringCharArray = userName.toCharArray();
		for (int i = 0; i < encodeStringCharArray.length; i++) {
			encodeStringCharArray[i] += 1;
		}
		String ticket = new String(java.util.Base64.getEncoder().encode(new String(encodeStringCharArray).getBytes()));
		System.out.println("ticket ： "+ticket);
		//根据人员ID和ticket获取待办协同 -- ok
		String json =client.get("/affairs/pending?ticket="+ticket+"&memberId="+memberId+"&apps=1&pageSize="+"5",String.class);
		//System.out.println(userName+"的协同待办 ： "+json);
				
		//通过人员编码获取所有的待办  -- ok
		//这里的人员编码要设置成和人员登录名一样即可  http://127.0.0.1/seeyon/rest/affairs/pending/code/002?ticket=cnn 
		String json03 =client.get("/affairs/pending/code/"+userName+"?ticket="+ticket+"&pageNo=1&pageSize=5",String.class);
		System.out.println("通过人员编码获取所有的待办 ： "+json03);
		List<Map> rows = new ArrayList<Map>();
		String pendingListStr = "[ " + json03 + " ]";
		if (pendingListStr != null && !"".equals(pendingListStr)) {
			JSONArray json11 = JSONArray.fromObject(pendingListStr); // 首先把字符串转成JSONArray对象
			if (json11.size() > 0) {
				System.out.println("json不为空======");
				net.sf.json.JSONObject jsonObject = json11.getJSONObject(0);
				String dataStr = jsonObject.getString("data");// 标识符
				if(dataStr != null && !"".equals(dataStr)){
					JSONArray dataJson = JSONArray.fromObject(dataStr); //这里就是待办的列表
					if (dataJson.size() > 0) {
						for (int i = 0; i < dataJson.size(); i++) {
							Map<String, Object> map = new HashMap<String, Object>();
							net.sf.json.JSONObject dataJsonObject = dataJson.getJSONObject(i);
							map.put("subject", dataJsonObject.getString("subject"));
							map.put("id",(String)dataJsonObject.getString("id"));
							map.put("receiveTime",(String)dataJsonObject.getString("receiveTime"));
							map.put("app",(String)dataJsonObject.getString("app"));
							rows.add(map);
						}
					}
				}
			}
		
		}
//		System.out.println("rows======"+rows);
//		List<Map> rows01 = new ArrayList<Map>();
//		Map temp = null;
//		if(rows.size()>1){
//			for (int i = 0; i < rows.size(); i++){
//				for (int j = 0; j < rows.size()-1-i; j++){
//					Map mapi = rows.get(j);
//					Map mapj = rows.get(j+1);
//					if (Long.valueOf((String) mapi.get("receiveTime")) > Long.valueOf((String) mapj.get("receiveTime"))){
//						temp = rows.get(j+1);
//						mapj = rows.get(j);
//						mapi = temp;
//					}
//					
//				}
//				rows01.add(rows.get(i));
//			}
//		}
//		
//		
//		
//		System.out.println("rows01======"+rows01);
		
		
		
		
		
		
		
		
				
		//按照单位获取个人的对应的公告列表
		String json07 =client.get("/bulletin/unit/-8695347076351768967?ticket="+userName,String.class);
//		System.out.println("按照人员登录名获取单位下个人对应的公告列表 ： "+json07);
		

		
	}
	
	
	

}
