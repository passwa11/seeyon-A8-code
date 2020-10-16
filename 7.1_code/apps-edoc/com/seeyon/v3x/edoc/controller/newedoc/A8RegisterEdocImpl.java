package com.seeyon.v3x.edoc.controller.newedoc;

import java.sql.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.privilege.manager.PrivilegeManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocRoleHelper;
import com.seeyon.v3x.edoc.util.NewEdocHelper;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.domain.EdocSendDetail;
import com.seeyon.v3x.exchange.domain.EdocSendRecord;
import com.seeyon.v3x.exchange.manager.EdocExchangeManager;
import com.seeyon.v3x.exchange.manager.SendEdocManager;
import com.seeyon.v3x.exchange.util.Constants;

public class A8RegisterEdocImpl extends NewEdocHandle {

	@Override
    public void createEdocSummary(HttpServletRequest request, ModelAndView modelAndView) throws Exception {
        summary = new EdocSummary();
        
       
        //从待登记中点击 登记电子公文，直接进行分页页面，要带入签收数据
        String recieveId = request.getParameter("recieveId");
        Long edocId=null;
        
        if(Strings.isNotBlank(recieveId) && !"0".equals(recieveId) && !"-1".equals(recieveId)){
        	EdocExchangeManager edocExchangeManager = (EdocExchangeManager)AppContext.getBean("edocExchangeManager");
            EdocRecieveRecord  recieveRecord = edocExchangeManager.getReceivedRecord(Long.parseLong(recieveId));
            
            Long recAccountId = null;
            if(recieveRecord.getExchangeType() == EdocRecieveRecord.Exchange_Receive_iAccountType_Org){
                recAccountId = recieveRecord.getExchangeOrgId();
            }else if(recieveRecord.getExchangeType() == EdocRecieveRecord.Exchange_Receive_iAccountType_Dept){
                V3xOrgDepartment entity = orgManager.getDepartmentById(recieveRecord.getExchangeOrgId());
                if(entity != null)
                    recAccountId = entity.getOrgAccountId();
            }
            summary = edocManager.getEdocSummaryById(recieveRecord.getEdocId(), true);
            atts = NewEdocHelper.excludeType2ToNewAttachmentList(summary);
            summary = NewEdocHelper.cloneNewSummaryAndSetProperties(user.getName(), summary, recAccountId, recieveRecord.getContentNo());
            
            //获取送文单上的主送单位
            SendEdocManager sendEdocManager = (SendEdocManager)AppContext.getBean("sendEdocManager");
            if(Strings.isNotEmpty(recieveRecord.getReplyId())){//书生交换replyId为空
                long sendDetailId = Long.parseLong(recieveRecord.getReplyId());
                EdocSendDetail sendDetail = sendEdocManager.getSendRecordDetail(sendDetailId);
               
                String sendToId = "";
                String sendToNames = "";
                if(sendDetail == null){ //A8书生收文没有sendDetail数据
                	sendToNames = recieveRecord.getSendTo();
                }else{
                	Long sendId = sendDetail.getSendRecordId();
                	if(sendId != null){
                		EdocSendRecord sendRecord = sendEdocManager.getEdocSendRecord(sendId);
                		if(sendRecord != null){
                			sendToId = sendRecord.getSendedTypeIds();
                			sendToNames = sendRecord.getSendEntityNames();
                		}
                	}
                }
                
                summary.setSendTo(sendToNames);
                summary.setSendToId(sendToId);
                summary.setCopyTo(null);//清除抄送单位的数据
                summary.setCopyToId(null);
                summary.setReportTo(null);
                summary.setReportToId(null);
                summary.setSendTo2(null);
                summary.setSendToId2(null);
                summary.setCopyTo2(null);
                summary.setCopyToId2(null);
                summary.setReportTo2(null);
                summary.setReportToId2(null);
            }
            
            //设置签收时间
            summary.setReceiptDate(new Date(recieveRecord.getRecTime().getTime()));
            if(recieveRecord.getKeepPeriod() == null){
                summary.setKeepPeriod(0);
            }else{
                summary.setKeepPeriod(Integer.parseInt(recieveRecord.getKeepPeriod()));
            }
            edocId = recieveRecord.getEdocId();
            EdocSummary sendSummary = edocManager.getEdocSummaryById(edocId, true);
            summary.setSendDepartment(sendSummary.getSendDepartment());
            summary.setSendDepartment2(sendSummary.getSendDepartment2());
            cloneOriginalAtts = true;//office正文需要复制一份
            
            //判断是否已经登记
            boolean hasRegisted = false;
            if (recieveRecord.getStatus()==EdocRecieveRecord.Exchange_iStatus_Registered ||
            		//登记保存待发 也是已经登记了
            		recieveRecord.getStatus() == EdocRecieveRecord.Exchange_iStatus_RegisterToWaitSend) {
                hasRegisted = true;
            }
            record = recieveEdocManager.getEdocRecieveRecord(Long.parseLong(recieveId));
            String openFrom = request.getParameter("openFrom");
            if (record.getRegisterUserId() != user.getId()) {
                Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(),record.getRegisterUserId() );
                    if(!Long.valueOf(user.getId()).equals(agentId)){
                        // 公文登记人已经转换
                        String errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", "alert_hasChanged_register");
                            // 转到待登记
                          StringBuilder msg = new StringBuilder();
                          msg.append("<script>");
                          msg.append("alert(\"" + errMsg + "\");");
                          if(Strings.isNotBlank(openFrom) && "ucpc".equals(openFrom)){
                        	  msg.append("if(typeof(getA8Top)!='undefined') {");
                        	  msg.append(" getA8Top().window.close();");
                        	  msg.append("} else {");
                        	  msg.append("	parent.parent.parent.window.close();");
                        	  msg.append("}");
                          }else{
                        	  msg.append("if(window.dialogArguments){"); // 弹出
                        	  msg.append("  window.returnValue = \"true\";");
                        	  msg.append("  window.close();");
                        	  msg.append("}else{");
                        	  String registerUserChange=SystemProperties.getInstance().getProperty("edoc.registerUserChange");
                        	  if("true".equals(registerUserChange)){//G6
                        		  if(EdocRoleHelper.isEdocCreateRole(EdocEnum.edocType.recEdoc.ordinal())) {
                        			  msg.append("   parent.parent.location.href='edocController.do?method=listIndex&from=listRegister&edocType=1';");
                        		  } else {
                        			  msg.append("   parent.parent.location.href='edocController.do?method=listIndex&from=listPending&edocType=1';");
                        		  }
                        	  }else{//A8
                        		  //判断是否有待登记的权限
                        		  User user = AppContext.getCurrentUser();
                        		  V3xOrgMember member = EdocHelper.getFirstEdocRole(user.getAccountId(), user.getId(), EdocEnum.edocType.recEdoc.ordinal());
                        		  if(member != null) {
                        			  msg.append("   parent.parent.location.href='edocController.do?method=entryManager&entry=recManager&listType=listV5Register';");
                        		  }else{
                        			  msg.append("   parent.parent.location.href='edocController.do?method=entryManager&entry=recManager&listType=listPending';");
                        		  }
                        	  }
                        	  msg.append("}");
                        	  msg.append("");
                          }
                                 
                            msg.append("</script>");
                            throw new NewEdocHandleException(msg.toString(),NewEdocHandleException.PRINT_CODE);
                    }
            }
            if(hasRegisted) {
//              modelAndView = new ModelAndView("common/redirect");
                String errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", "alert_has_registe");
                modelAndView.addObject("redirectURL", BaseController.REDIRECT_BACK);
                StringBuilder msg = new StringBuilder();
                msg.append("<script>");
                msg.append("alert(\"" + errMsg + "\");");
                if(Strings.isNotBlank(openFrom) && "ucpc".equals(openFrom)){
              	  msg.append("if(typeof(getA8Top)!='undefined') {");
              	  msg.append(" getA8Top().window.close();");
              	  msg.append("} else {");
              	  msg.append("	parent.parent.parent.window.close();");
              	  msg.append("}");
                }else{
	                msg.append("if(window.dialogArguments){"); // 弹出
	                msg.append("  window.returnValue = \"true\";");
	                msg.append("  window.close();");
	                msg.append("} else {");
	                if("agent".equals(request.getParameter("app")) && user.getId()!=record.getRegisterUserId()) {//代理人跳转到代理事项
	                    msg.append("parent.parent.parent.location.href='main.do?method=morePending4App&app=agent';");
	                } else {
	                    msg.append("parent.parent.location.href='edocController.do?method=listIndex&from=listDistribute&listType=listPending&edocType="+edocType+"';");
	//                  msg.append("parent.parent.location.href='edocController.do?method=listIndex&from=listRegister&listType=registerDone&edocType="+edocType+"'");
	                }        
	                msg.append("}");
                }    
                msg.append("</script>");
                throw new NewEdocHandleException(msg.toString(),NewEdocHandleException.PRINT_CODE);
            }
         // 登记公文，判断当前操作人是否可以登记此公文
          if(record.getStatus()==Constants.C_iStatus_Retreat) {//被退回
              String szJs = "<script>alert('" + ResourceUtil.getString("edoc.alert.flow.edocStepBack", record.getSubject()) + "');";//公文《"+record.getSubject()+"》已经被退回。
	              if(Strings.isNotBlank(openFrom) && "ucpc".equals(openFrom)){
	            	  szJs+="if(typeof(getA8Top)!='undefined') {";
	            	  szJs+=" getA8Top().window.close();";
	            	  szJs+="} else {";
	            	  szJs+="	parent.parent.parent.window.close();";
	            	  szJs+="}";
	              }else{
	              	  szJs += "if(window.dialogArguments) {"; // 弹出
	                  szJs += "   window.returnValue = \"true\";";
	                  szJs += "   window.close();";
	                  szJs += "} else {";
	                  szJs += "   parent.parent.location.href='edocController.do?method=entryManager&entry=recManager&listType=listPending&edocType="+edocType+"'";
	                  szJs += "}";
	              }   
                szJs += "</script>";
              throw new NewEdocHandleException(szJs,NewEdocHandleException.PRINT_CODE);
          } 
        }
        summary.setEdocType(iEdocType); 
        summary.setCanTrack(1);
        summary.setCreatePerson(user.getName());
        //设置当前登记日期
        summary.setRegistrationDate(new Date(System.currentTimeMillis()));
       
        cloneOriginalAtts = true;//正文是否要复制一份
        
      
        
        /**
         * 在待登记列表中  传入了登记id
         */
        edocRegister = edocRegisterManager.findRegisterById(registerId);
        if(edocRegister == null){
        	//消息，首页，工作桌面这些地方都没有传递registerId参数，只有通过recieveId来获得登记
        	edocRegister = edocRegisterManager.findRegisterByRecieveId(Long.parseLong(recieveId));
        	if(edocRegister != null){
        	    registerId = edocRegister.getId();
        	}
        }
        
        //判断是否有收文待发页签
        PrivilegeManager privilegeManager = (PrivilegeManager)AppContext.getBean("privilegeManager");
        boolean enableRecWaitSend = privilegeManager.checkByReourceCode(EdocConstant.F07_RECWAITSEND);
        if(edocId!=null){
        	modelAndView.addObject("edocId", edocId);
        }
        modelAndView.addObject("enableRecWaitSend", String.valueOf(enableRecWaitSend)); 
    }
}
