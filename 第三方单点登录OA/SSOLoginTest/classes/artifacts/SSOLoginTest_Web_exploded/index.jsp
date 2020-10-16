<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>单点登录测试页面</title>

<script type="text/javascript">
  function loginout(){
	 location.href = "http://127.0.0.1:8080/SSO/SsoLoginOut";
     window.open(url); 
   }
    function login(){
    var username=document.getElementById("userid").value;
	 location.href = "http://127.0.0.1:8080/SSO/SsoLogin?username="+username+"&doc=1";
     window.open(url); 
   }
   function logindoc(){
    var username=document.getElementById("userid").value;
	 location.href = "http://127.0.0.1:8080/SSO/SsoLogin?username="+username+"&doc=0";
     window.open(url); 
   }
</script>
  </head>
  <body> 

  输入用户名： <input type="text" name="username" id="userid"/>
    <input type="button" id="login" name="login" onClick="login()" value="单点登录"/>
    <input type="button" id="logindoc" name="logindoc" onClick="logindoc()" value="单点登录进入协同表单"/>
    <input type="button" name="loginout" onclick="loginout()" value="退出登录"/>

  </body>
</html>
