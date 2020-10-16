package com.sso.login;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SsoLoginOut extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doPost(request, response);

	}
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String username=null;
		Cookie[] all_cookises=request.getCookies();
		for(int i=0;i<all_cookises.length;i++){
			Cookie myCookie=all_cookises[i];
			if(myCookie.getName().equals("login")){
				username=myCookie.getValue();
			}
			}
		String url = "http://127.0.0.1:80/seeyon/login/ssologout?from=sample&ticket="+username;
		response.sendRedirect(url);
		out.flush();
		out.close();
	}

}
