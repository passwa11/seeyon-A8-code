package com.seeyon.apps.wpstrans.manager;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import kingsoft.WPS_ERROR_NO;
import kingsoft.WpsConvertTool;

public class WpsTest {

	public static void main(String[] args) {
		TTransport transport = null;
		try {
			transport = new TSocket("10.3.10.164",8888);
			transport.open();

			TProtocol protocol = new TBinaryProtocol(transport);
			WpsConvertTool.Client client = new WpsConvertTool.Client(protocol);

			// 获取服务器状态
			if (client.getServerState() != WPS_ERROR_NO.WPS_ERROR_OK) {
				System.out.println("Wps转版服务不可用!");
				return;
			}
		} catch (Exception e) {
			System.out.println("获取Wps转版服务出错");
			return;
		} finally {
			if(transport != null){				
				try {
					transport.close();
				} catch (Exception e) {
					System.out.println("Wps转版流关闭失败，请注意！！！");
					return;
				}
			}
		}
		System.out.println("Wps转版服务可用!");
	}

}
