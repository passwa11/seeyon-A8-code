package com.seeyon.v3x.edoc.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.util.Strings;

public class StringUtils {

    private static Log LOG = CtpLogFactory.getLog(StringUtils.class);
	public static Long convertTo(String str){
		Long result = null;
		try{
			result = Long.valueOf(str);
		}catch(Exception e){
		    LOG.error("", e);
		}
		return result;
	}
	
	public static List xmlElementToList(String xml){
		  String f_xml = xml;
		  int a = xml.indexOf(">");
		  int c = xml.indexOf("</my:myFields>");
		  xml = xml.substring(a+1,c);
		 // String xml_a = f_xml.substring(0,a+1);
		  
		  List<String> list = new ArrayList<String>();
		  
		  String[] str = xml.split("/>");
		  StringBuilder temp = new StringBuilder();
		  for(int i=0;i<str.length-1;i++){
			  i+=1;
			  String str_a = str[i];
			  int x  = str_a.indexOf(":");
			  str_a = str_a.substring(x+1, str_a.length());
			  if(!str_a.startsWith("field")){
				  list.add(str_a);
				  temp.append("<my:");
				  temp.append(str_a);
                  temp.append("></my:");
                  temp.append(str_a);
                  temp.append(">");
			 }
		  }
		  return list;		  
	}
	
	public static String xmlElementToString(String xml){
		  String f_xml = xml;
		  int a = xml.indexOf(">");
		  int c = xml.indexOf("</my:myFields>");
		  xml = xml.substring(a+1,c);
		 // String xml_a = f_xml.substring(0,a+1);
		  String[] str = xml.split("<my:");
		  StringBuilder temp = new StringBuilder();
		  for(int i=1;i<str.length;i++){
			  String str_a = str[i];
			  int x  = str_a.indexOf(":");
			  int y = str_a.indexOf(">",0);
			  str_a = str_a.substring(0,y);
			  if(!str_a.startsWith("field")){
				  temp.append(str_a);
				  temp.append("|");
			 }
		  }
		  if(temp.length() > 0){
		      return temp.substring(0,temp.length()-1);
		  }else{
			  return temp.toString();
		  }
	}
	
	public static List<String> findEdocElementFromConfig(String type) throws Exception{
		User user = AppContext.getCurrentUser();
		PermissionManager permissionManager= (PermissionManager)AppContext.getBean("permissionManager");
		List<Permission> permListSend = permissionManager.getPermissionsByCategory("edoc_send_permission_policy", user.getLoginAccount());
		List<String> eleListSend = new ArrayList<String>();
		for(Permission perm:permListSend){
			eleListSend.add(perm.getName());
		}
		
		List<Permission> permListRec = permissionManager.getPermissionsByCategory("edoc_rec_permission_policy", user.getLoginAccount());
		List<String> eleListRec = new ArrayList<String>();
		for(Permission perm:permListRec){
			eleListRec.add(perm.getName());
		}
		
		List<Permission> permListQianBao = permissionManager.getPermissionsByCategory("edoc_qianbao_permission_policy", user.getLoginAccount());
		List<String> eleListQianBao = new ArrayList<String>();
		for(Permission perm:permListQianBao){
			eleListQianBao.add(perm.getName());
		}
		
		List<String> allList = new ArrayList<String>();
		allList.addAll(eleListSend);
		allList.addAll(eleListRec);
		allList.addAll(eleListQianBao);
		
		if(null!=type && Integer.valueOf(type).intValue() == Constants.EDOC_FORM_TYPE_SEND){
			return eleListSend;
		}else if(null!=type && Integer.valueOf(type).intValue() == Constants.EDOC_FORM_TYPE_REC){
			return eleListRec;
		}else if(null!=type && Integer.valueOf(type).intValue() == Constants.EDOC_FORM_TYPE_SIGN){
			return eleListQianBao;
		}else{
			return allList;
		}
	}
	
	/**
	 * 将以特定分隔符分割的字符串id，转换成List<Long>
	 * @param ids  字符串id
	 * @param split 分隔符
	 * @return
	 */
	public static List<Long> convertStringToLongList(String ids,String split) {
		List<Long> listLong = new ArrayList<Long>();
		if (Strings.isBlank(ids)) {
			return listLong;
		}
		String[] idSplit = ids.split(split);
		for (String isStr : idSplit) {
			try {
				Long idLong = Long.valueOf(isStr);
				listLong.add(idLong);
			} catch (Exception e) {
			    LOG.error("", e);
			}
		}
		return listLong;
	}
}
