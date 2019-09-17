package com.seeyon.apps.ocip.util;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
/**
 *
 */
public class OcipAxis2Client {
	private String baseUrl;

	public OcipAxis2Client(String baseUrl) {
		super();
		this.baseUrl = baseUrl;
	}

	private String buildEPR(String serviceName) {
		return baseUrl + "/seeyon/services/" + serviceName +"?wsdl";
	}

	/**
	 * 使用Axis2 RPCServiceClient调用远程SOAP服务。
	 * 
	 * @param serviceName
	 * @param methodName
	 * @param returnType
	 * @param targetNamespace
	 * @param params
	 * @return
	 * @throws AxisFault
	 */
	public <T> T invoke(String serviceName, String methodName,
			Class<T> returnType, String targetNamespace, Object[] params)
			throws AxisFault {
		// 创建一个RPC的客户端实例
		RPCServiceClient rpcServiceClient = new RPCServiceClient();
		// 拿到相关的配置
		Options options = rpcServiceClient.getOptions();
		// 创建一个远程的访问地址
		EndpointReference target = new EndpointReference(buildEPR(serviceName));
		options.setTo(target);
		// 创建一个Qname的命名空间,默认的是域名倒过来写,第二个参数的写方法名
		QName qgetname = new QName(targetNamespace, methodName);
		// 数组的实例
		Class[] getobj = new Class[] { returnType };
		// 返回的数组实例.
		Object[] response = rpcServiceClient.invokeBlocking(qgetname, params,
				getobj);
		return (T) response[0];
	}
}
