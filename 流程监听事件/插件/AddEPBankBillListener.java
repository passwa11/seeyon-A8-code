package com.seeyon.apps.yuwell.listener;

import java.util.List;
import java.util.Map;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.log4j.Logger;

import com.seeyon.apps.collaboration.event.CollaborationFinishEvent;
import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.yuwell.util.JDBCUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.event.EventTriggerMode;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormDataMasterBean;
import com.seeyon.ctp.form.service.FormManager;
import com.seeyon.ctp.form.service.FormService;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.po.OrgMember;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.annotation.ListenEvent;
import com.seeyon.ctp.workflow.event.AbstractWorkflowEvent;

public class AddEPBankBillListener extends AbstractWorkflowEvent {
	
	private static final Logger log = Logger.getLogger(AddEPBankBillListener.class);

	private FormManager formManager = (FormManager) AppContext.getBean("formManager");

	private ColManager colManager = (ColManager) AppContext.getBean("colManager");
	
	OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
	
	private String ipconfig;

	@Override
	public String getId() {
		return "20171212123213123";
	}

	@Override
	public String getLabel() {
		return "新增费用付款单";
	}
	
	@ListenEvent(event = CollaborationFinishEvent.class, mode = EventTriggerMode.afterCommit)
	// 协同发起成功提交事务后执行，异步模式。
	public String onFinish(final CollaborationFinishEvent event) throws Exception {
		System.out.println("**************进入新增接口run之前*****************");
		Thread t = new Thread(new Runnable() {
			
			@SuppressWarnings("unchecked")
			public void run() {
				try {
					Thread.sleep(30000);
				} catch (Exception e) {
					log.info("#客开日志：", e);
				}
				try {
					while (colManager.getColSummaryById(event.getSummaryId()) == null) {
						Thread.sleep(30000);
					}
					System.out.println("**************进入新增接口*****************");
					ColSummary colSummary = colManager.getColSummaryById(event.getSummaryId());
					List<CtpTemplate> ctpTemplateList = DBAgent
							.find("from com.seeyon.ctp.common.po.template.CtpTemplate t where t.formAppId= "
									+ colSummary.getFormAppid());
					//获取到表单的编码    终端形象核销：zdxxhx    会议培训核销：hypxhx   专柜核销：zghx  终端促销核销：zdcxhx   差旅费报销 ：clfbx   招待费报销：zdfbx
					String templateCode = ctpTemplateList.get(0).getTempleteNumber();
					String tableName = ctpTemplateList.get(0).getSubject().toString();//表名称
					System.out.println("templateCode = " + templateCode);
					if(templateCode != null && !"".equals(templateCode)){
						//根据编码获取对应表
						FormBean bean = formManager.getFormByFormCode(templateCode);
						//根据表名称和ID获取该流程数据
						FormDataMasterBean col = FormService.findDataById(colManager.getColSummaryById(event.getSummaryId()).getFormRecordid(), bean.getId());
						Map<String, Object> dataMap = col.getAllDataMap();
						String in0 = "江苏鱼跃医疗设备股份有限公司";//付款单位
						String in1 = "1104021009000097469";//付款账号
						String in2 = ""; //收款人名(对应表单中的  申请人)
						String in3 = "";//付款金额(对应表单中的   财务审核金额--double)
						String in4 = "";//备注(需要自己判断获取表单中文名称名称)
						String in5 = "";//CRM单号(对应表单中的   费用核销单号)
						String in6 = ""; //身份证号(暂时为空)
						//根据字段名称获取该条数据的表单值
						String approveId = (String) dataMap.get(bean.getMasterTableBean()
								.getFieldBeanByDisplay("申请人").getName());
						Long id = Long.valueOf(approveId);
						String sql = " SELECT name FROM ORG_MEMBER WHERE id = "
								+ id;
						List<Map> resultList = JDBCUtil.doQuery(sql);
						if (resultList.size() > 0) {
							in2 = (String) resultList.get(0).get("name");
						}
						System.out.println("申请人 = " + in2);
						Object mon =  dataMap.get(bean.getMasterTableBean()
								.getFieldBeanByDisplay("财务审核金额").getName());
						in3 = String.valueOf(mon);
						System.out.println("财务审核金额 = " + in3);
						in4 = tableName;
						System.out.println("表单名称= " + in4);
						in5 = (String) dataMap.get(bean.getMasterTableBean()
								.getFieldBeanByDisplay("费用核销单号").getName());//费用核销单号
						System.out.println("费用核销单号= " + in5);
						
//						通过用户ID获取对象
//						V3xOrgMember ss =  orgManager.getMemberById(id);
//						String sss = ss.getName();
						String ipconfig = getIpconfig();
						System.out.println("***********ip*********="+ipconfig);
						String code = null;
						try {
							System.out.println("************进入webservice接口调用方法************ ");
							String endpoint = ipconfig+"/YY_CRMToEPBank/services/CRMToEPBank";//URL路径  http://192.168.100.58:8080
							Service service = new Service();
							Call call = (Call) service.createCall();
							call.setTargetEndpointAddress(new java.net.URL(endpoint));
							call.setOperationName("AddEPBankBill");// //WSDL里面描述的接口名称
							System.out.println("************进入webservice接口调用方法001************ ");
							call.addParameter("in0", org.apache.axis.encoding.XMLType.XSD_STRING,javax.xml.rpc.ParameterMode.IN);//接口的参数
							call.addParameter("in1", org.apache.axis.encoding.XMLType.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
							call.addParameter("in2", org.apache.axis.encoding.XMLType.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
							call.addParameter("in3", org.apache.axis.encoding.XMLType.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
							call.addParameter("in4", org.apache.axis.encoding.XMLType.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
							call.addParameter("in5", org.apache.axis.encoding.XMLType.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
							call.addParameter("in6", org.apache.axis.encoding.XMLType.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
							System.out.println("************进入webservice接口调用方法002************ ");
							call.setTimeout(20000);
							call.setEncodingStyle("UTF-8");  
							System.out.println("************进入webservice接口调用方法003************ ");
							//入参
							call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);//设置返回类型 
							String result = (String)call.invoke(new Object[]{
												 in0,  //付款单位
												 in1,  //付款账号
												 in2,  //收款人名
												 in3,  //付款金额
												 in4,  //备注
												 in5,  //CRM单号
												 in6   //身份证号                            
								        });
							System.out.println("code is变化前 " + code);
							//给方法传递参数，并且调用方法
							System.out.println("result01 is " + result);
							code = result;
							System.out.println("code is变化后 " + code);
						} catch (Exception e) {
							System.out.println("调用接口出错！");
							System.err.println(e.toString());
						}
					}else{
						 
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
		return null;
	}

	protected String getIpconfig() {
		// TODO 自动生成的方法存根
		return ipconfig;
	}

	public void setIpconfig(String ipconfig) {
		this.ipconfig = ipconfig;
	}
}
