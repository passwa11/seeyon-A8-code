package com.seeyon.v3x.mobile.adapter.sms;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.common.kit.HttpKit;
import com.seeyon.v3x.mobile.adapter.AdapterMobileMessageManger;
import com.seeyon.v3x.mobile.message.domain.MobileReciver;

/**
 * Description
 * 
 * <pre>
 * 短信插件
 * </pre>
 * 
 * @author FanGaowei<br>
 *         Date 2018年3月9日 下午4:12:33<br>
 *         MessagePipeline Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class AdaptMobileImpl implements AdapterMobileMessageManger {

	private static Log log = LogFactory.getLog(AdaptMobileImpl.class);

	private String account;
	private String pwd;
	private String url;
	

	// datasource=jdbc:sqlserver://10.100.1.174:1433
	// DatabaseName=xkmsg
	// username=sa
	// password=xkjt2014~

	public void setAccount(String account) {
		this.account = account;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	// 短信网关名称
	@Override
	public String getName() {
		return "短信插件";
	}

	// 检查适配器是否适用
	public boolean isAvailability() {
		return true;
	}

	// 是否支持群发
	@Override
	public boolean isSupportQueueSend() {
		return false;
	}

	// 是否支持收短信
	@Override
	public boolean isSupportRecive() {
		return false;
	}

	// 从手机端返回协同平台
	@Override
	public List<MobileReciver> recive() {
		return null;
	}

	@Override
	public boolean sendMessage(Long messageId, String srcPhone, String destPhone, String content) {
		/*
		 * try { content = URLEncoder.encode(content, "UTF-8"); } catch
		 * (UnsupportedEncodingException e) {
		 * 
		 * }
		 */
		
		Connection conn = null;
		PreparedStatement ps = null;
		// ResultSet rs = null;

		try {
			// 加载数据库驱动
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			// 声明数据库信息
			String url = "jdbc:sqlserver://10.100.1.174:1433;databaseName=xkmsg";
			String user = "sa";// sa超级管理员
			String password = "xkjt2014~";// 密码
			// 建立数据库连接
			conn = DriverManager.getConnection(url, user, password);
			String sql = "insert into SendSms (phoneNumber,smsContent,smsTime,smsUser,status) values(?,?,?,?,?)"; // 生成一条sql语句
			ps = conn.prepareStatement(sql);
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss.SSS");
			ps.setString(1, destPhone);
			ps.setString(2, content);
			ps.setString(3, df.format(new Date()));  //插入datetime类型，精确到毫秒
			ps.setString(4, "1");   //发送人，默认为1
			ps.setInt(5, 0);        //状态，默认0，消息发送成功后改为1
			int result = ps.executeUpdate();
			// 关闭声明对象连接
			ps.close();
			// 关闭数据库连接对象
			conn.close();
		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
		}

		log.info("----------发送短信结束------------");
		

//		String msgUrl = url + "?account=" + account + "&pwd=" + pwd + "&mobile=" + destPhone + "&msg=" + content;
//		try {
//			String res = HttpKit.get(msgUrl);
//			// 记录日志，可以在ctp.log里面看到
//			log.info("发送短信结果：" + res);
//		} catch (Exception e) {
//			log.error("发送短信网络异常：", e);
//		}
		return true;
	}

	public boolean sendMessage(Long messageId, String srcPhone, String destPhone, String content,
			PreparedStatement ps) {
		/*
		 * try { content = URLEncoder.encode(content, "UTF-8"); } catch
		 * (UnsupportedEncodingException e) {
		 * 
		 * }
		 */

		try {			
			Date date=new Date();	
			ps.setString(1,destPhone);
			ps.setString(2, content);
			//ps.setDate(3, new java.sql.Date(date.getDate()));
			ps.setDate(3, new java.sql.Date(date.getDate()));
			ps.setString(4, "1");   //发送人，默认为1
			ps.setInt(5, 0);        //状态，默认0，消息发送成功后改为1
			ps.addBatch();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean sendMessage(Long messageId, String srcPhone, Collection<String> destPhoneList, String content) {

		Connection conn = null;
		PreparedStatement ps = null;
		// ResultSet rs = null;

		try {
			// 加载数据库驱动
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			// 声明数据库信息
			String url = "jdbc:sqlserver://10.100.1.174:1433;databaseName=xkmsg";
			String user = "sa";// sa超级管理员
			String password = "xkjt2014~";// 密码
			// 建立数据库连接
			conn = DriverManager.getConnection(url, user, password);
			String sql = "insert into SendSms (phoneNumber,smsContent,smsTime,smsUser,status) values(?,?,?,?,?)"; // 生成一条sql语句
			ps = conn.prepareStatement(sql);
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss.SSS");

			for (String dest : destPhoneList) {			
				ps.setString(1, dest);
				ps.setString(2, content);
				ps.setString(3, df.format(new Date()));  //插入datetime类型，精确到毫秒
				ps.setString(4, "1");   //发送人，默认为1
				ps.setInt(5, 0);        //状态，默认0，消息发送成功后改为1
				ps.addBatch();
				//sendMessage(messageId++, srcPhone, dest, content, ps);
			}
			int result = ps.executeUpdate();
			// 关闭声明对象连接
			ps.close();
			// 关闭数据库连接对象
			conn.close();
		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
		}

		log.info("----------发送短信结束------------");
		return true;
	}

}
