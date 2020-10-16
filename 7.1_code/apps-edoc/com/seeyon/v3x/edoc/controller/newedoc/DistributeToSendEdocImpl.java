package com.seeyon.v3x.edoc.controller.newedoc;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.util.NewEdocHelper;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.util.Constants;

public class DistributeToSendEdocImpl extends NewEdocHandle {

	@Override
    public void createEdocSummary(HttpServletRequest request, ModelAndView modelAndView) throws Exception {
        summary = new EdocSummary();
        body = new EdocBody();
        
        ///////////////////////////////公文属性值设置
        summary.setEdocType(iEdocType); 
        summary.setCanTrack(1);
        summary.setCreatePerson(user.getName());
        
        //从待登记中点击 登记电子公文，直接进行分页页面，要带入签收数据
        String recieveId = request.getParameter("recieveId");
        if(Strings.isNotBlank(recieveId)){
            EdocRecieveRecord  recieveRecord = edocExchangeManager.getReceivedRecord(Long.parseLong(recieveId));
            summary.setSubject(recieveRecord.getSubject());
            summary.setDocType(recieveRecord.getDocType());
            summary.setDocMark(recieveRecord.getDocMark());
            summary.setSecretLevel(recieveRecord.getSecretLevel());
            summary.setUrgentLevel(recieveRecord.getUrgentLevel());
            summary.setKeepPeriod(Integer.parseInt(recieveRecord.getKeepPeriod()));
            summary.setSendUnit(recieveRecord.getSendUnit());
            summary.setIssuer(recieveRecord.getIssuer());
            summary.setSendTo(recieveRecord.getSendTo());
            summary.setCopyTo(recieveRecord.getCopyTo());
            summary.setReportTo(recieveRecord.getReportTo());
            Long recAccountId = null;
            if(recieveRecord.getExchangeType() == EdocRecieveRecord.Exchange_Receive_iAccountType_Org){
            	recAccountId = recieveRecord.getExchangeOrgId();
            }else if(recieveRecord.getExchangeType() == EdocRecieveRecord.Exchange_Receive_iAccountType_Dept){
            	V3xOrgDepartment entity = orgManager.getDepartmentById(recieveRecord.getExchangeOrgId());
            	if(entity != null)
            		recAccountId = entity.getOrgAccountId();
            }
            if(recAccountId != null){
            	summary.setOrgAccountId(recAccountId);
            }
            
            //OA-19089  待登记的电子公文，保存草稿，在待发中编辑，第一次会报js，后来就不出现了；正文类型变成可选状态了，应该是默认原来的正文类型   start
            /*------获得发文的正文类型，电子登记收文正文类型和发文一致----*/
            //发文的id
            long edocId = recieveRecord.getEdocId();
            EdocSummary sendSummary = edocManager.getEdocSummaryById(edocId, true);
            summary.setSendType(sendSummary.getSendType());
            summary.setKeepPeriod(sendSummary.getKeepPeriod());
            summary.setKeywords(sendSummary.getKeywords());
            summary.setSigningDate(sendSummary.getSigningDate());
            Set<EdocBody> edocBodies = sendSummary.getEdocBodies();
            Iterator<EdocBody> it = edocBodies.iterator();
            while(it.hasNext()){
                EdocBody onebody = it.next();
                bodyContentType = onebody.getContentType();
                body.setNewId();
                body.setContent(onebody.getContent());
                body.setContentName(onebody.getContentName());
                body.setContentNo(onebody.getContentNo());
                body.setContentStatus(onebody.getContentStatus());
                body.setContentType(onebody.getContentType());
                body.setCreateTime(new Timestamp(System.currentTimeMillis()));
                body.setEdocId(summary.getId());
            }
            cloneOriginalAtts = true;//office正文需要复制一份
            //OA-19089  待登记的电子公文，保存草稿，在待发中编辑，第一次会报js，后来就不出现了；正文类型变成可选状态了，应该是默认原来的正文类型   end
            
            
            //OA-36339  weblogic环境：a1新建公文，插入关联文档，封发交换出去，交换的单位是外单位的，比如：liud02单位，人员是test01，交换完成后到了收文待登记页面，仍然能看到原单位的关联文档  
            //当发文单位和登记收文单位不是一个单位时，才进行如下判断
            if(sendSummary.getOrgAccountId().longValue() != summary.getOrgAccountId().longValue()){
                List<Attachment> sendAttrs = attachmentManager.getByReference(edocId, edocId); 
                atts = new ArrayList<Attachment>();
                for(Attachment att : sendAttrs){
                    if(att.getType() == 2){
                        EdocSummary relationSummary = edocManager.getEdocSummaryById(att.getReference(), false);
                        if(relationSummary.getOrgAccountId().longValue() == summary.getOrgAccountId().longValue()){
                            atts.add(att);
                        }
                    }else{
                        atts.add(att);
                    }
                }
            }else{
                atts = attachmentManager.getByReference(edocId, edocId); 
            }
            
            
            //判断是否已经登记
            boolean hasRegisted = false;
            if (recieveRecord!=null && recieveRecord.getStatus()==EdocRecieveRecord.Exchange_iStatus_Registered) {
        		hasRegisted = true;
        	}
            record = recieveEdocManager.getEdocRecieveRecord(Long.parseLong(recieveId));
            if (record.getRegisterUserId() != user.getId()) {
            	Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(),record.getRegisterUserId() );
					if(!Long.valueOf(user.getId()).equals(agentId)){
						// 公文登记人已经转换
		                String errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", "alert_hasChanged_register");
		                // 转到待登记
		              	StringBuilder msg = new StringBuilder();
		              	msg.append("<script>");
		              	msg.append("alert(\"" + errMsg + "\");");
		              	msg.append("if(window.dialogArguments){"); // 弹出
		              	msg.append("  window.returnValue = \"true\";");
		              	msg.append("  window.close();");
		              	msg.append("}else{");
		              	msg.append("   parent.parent.location.href='govdoc/govdoc.do?method=index&listType=listPending&govdocType=2,4&_resourceCode=F20_receiveManage';");
		              	msg.append("}");
		                msg.append("</script>");
		                throw new NewEdocHandleException(msg.toString(),NewEdocHandleException.PRINT_CODE);
					}
    }
            if(hasRegisted) {
//		        modelAndView = new ModelAndView("common/redirect");
	            String errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", "alert_has_registe");
	            modelAndView.addObject("redirectURL", BaseController.REDIRECT_BACK);
	            StringBuilder msg = new StringBuilder();
	            msg.append("<script>");
	            msg.append("alert(\"" + errMsg + "\");");
	            msg.append("if(window.dialogArguments){"); // 弹出
	            msg.append("  window.returnValue = \"true\";");
	            msg.append("  window.close();");
	            msg.append("} else {");
	            if("agent".equals(request.getParameter("app")) && user.getId()!=record.getRegisterUserId()) {//代理人跳转到代理事项
	            	msg.append("parent.parent.parent.location.href='main.do?method=morePending4App&app=agent';");
	            } else {
	            	msg.append("parent.parent.location.href='govdoc/govdoc.do?method=index&listType=listPending&govdocType=2,4&_resourceCode=F20_receiveManage';");
	            }
	            msg.append("}");
	            msg.append("</script>");
	            throw new NewEdocHandleException(msg.toString(),NewEdocHandleException.PRINT_CODE);
	        }
         // 登记公文，判断当前操作人是否可以登记此公文
          if(null != record){
        	  if(record.getStatus()==Constants.C_iStatus_Retreat) {//被退回
        	      StringBuilder szJs = new StringBuilder();
        	      szJs.append("<script>alert('")
        	          .append(ResourceUtil.getString("edoc.alert.flow.edocStepBack", record.getSubject()))
        	          .append("');")//公文《"+record.getSubject()+"》已经被退回。
        	          .append("if(window.dialogArguments) {") // 弹出
        	          .append("   window.returnValue = \"true\";")
        	          .append("   window.close();")
                      .append("} else {")
                      .append("   parent.parent.location.href='govdoc/govdoc.do?method=index&listType=listPending&govdocType=2,4&_resourceCode=F20_receiveManage';")
                      .append("}")
                      .append("</script>");
                  throw new NewEdocHandleException(szJs.toString(),NewEdocHandleException.PRINT_CODE);
              } 
          }
        }
        
        edocRegister = edocRegisterManager.findRegisterById(registerId);
        /*防护：没有登记数据的时候，从历史待分发消息中 分发时要提示 公文已被回退*/
        if(edocRegister == null){
        	StringBuilder szJs = new StringBuilder("<script>alert('")
        	              .append(ResourceUtil.getString("edoc.alert.flow.edocStepBack1"))
        	              .append("');");//该公文已经被退回。
            if("agent".equals(request.getParameter("app"))) {//代理进去
                szJs.append("if(window.dialogArguments) {") // 弹出
                    .append("   window.returnValue = \"true\";")
                    .append("   window.close();")
                    .append("} else {")
                    .append("   parent.parent.location.href='collaboration/pending.do?method=morePending&from=Agent';")
                    .append("}");
            } else {
                szJs.append("if(window.dialogArguments) {") // 弹出
                    .append("   window.returnValue = \"true\";")
                    .append("   window.close();")
                    .append("} else {")
                    .append("   parent.parent.location.href='govdoc/govdoc.do?method=index&listType=listPending&govdocType=2,4&_resourceCode=F20_receiveManage';")
                    .append("}");
            }
            szJs.append("</script>");
            throw new NewEdocHandleException(szJs.toString(),NewEdocHandleException.PRINT_CODE);
        }
        
        //无权查看该主题
    	if(!user.getId().equals(edocRegister.getDistributerId())) {
            Long agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(), edocRegister.getDistributerId());
            String pishiFlag = govdocPishiManager.checkLeaderPishi(user.getId(), edocRegister.getDistributerId());
            if(agentMemberId == null && !"pishi".equals(pishiFlag)) {
                StringBuilder msg = new StringBuilder();
                msg.append("<script>");
                msg.append("alert('"+ResourceUtil.getString("edoc.alert.right.noThemeView")+"');");//您无权查看该主题!
                msg.append("if(window.dialogArguments){"); // 弹出
                msg.append("   window.returnValue = \"true\";");
                msg.append("   window.close();");
                msg.append("} else {");
                if ("agent".equals(request.getParameter("app"))) {//无权查看主题，代理页面跳转到代理事项。
                    msg.append("   parent.parent.parent.location.href='main.do?method=morePending4App&app=agent';");
                } else {//无权查看主题，被代理页面跳转到代理事项。
                	msg.append("   parent.parent.location.href='govdoc/govdoc.do?method=index&listType=listPending&govdocType=2,4&_resourceCode=F20_receiveManage';");
                }
                msg.append("}");
                msg.append("</script>");
                throw new NewEdocHandleException(msg.toString(),NewEdocHandleException.PRINT_CODE);
            } else {
                Long agentToId = edocRegister.getDistributerId();
                String agentToName = "";
                String agentToAccountShortName = "";
                V3xOrgMember member = orgManager.getMemberById(agentToId);
                if(member != null) {
                    agentToName = member.getName();
                    agentToAccountShortName = orgManager.getAccountById(member.getOrgAccountId()).getShortName();               
                }
                modelAndView.addObject("agentToId", agentToId);
                modelAndView.addObject("agentToName", agentToName);
                modelAndView.addObject("agentToAccountShortName", agentToAccountShortName);
            }
        }
         
        //被退回
        if(edocRegister.getState()==EdocNavigationEnum.RegisterState.retreat.ordinal() ||
        		//分发退回到签收
        		edocRegister.getState()==EdocNavigationEnum.RegisterState.Register_StepBacked.ordinal()) {//被退回
            StringBuilder szJs = new StringBuilder("<script>alert('")
                    .append(ResourceUtil.getString("edoc.alert.flow.edocStepBack", edocRegister.getSubject()))
                    .append("');");//公文《"+edocRegister.getSubject()+"》已经被退回。
            if("agent".equals(request.getParameter("app")) && !user.getId().equals(edocRegister.getDistributerId())) {//代理进去
                szJs.append("if(window.dialogArguments) {") // 弹出
                    .append("   window.returnValue = \"true\";")
                    .append("   window.close();")
                    .append("} else {")
                    .append("   parent.parent.location.href='collaboration/pending.do?method=morePending&from=Agent';")
                    .append("}");
            } else {
                szJs.append("if(window.dialogArguments) {") // 弹出
                    .append("   window.returnValue = \"true\";")
                    .append("   window.close();")
                    .append("} else {")
                    .append("   parent.parent.location.href='govdoc/govdoc.do?method=index&listType=listPending&govdocType=2,4&_resourceCode=F20_receiveManage';")
                    .append("}");
            }
            szJs.append("</script>");
            throw new NewEdocHandleException(szJs.toString(),NewEdocHandleException.PRINT_CODE);
        } 
        //公文已经分发，不能进行重复分发。
        else if(edocRegister.getDistributeState() == EdocNavigationEnum.EdocDistributeState.Distributed.ordinal()) { // 公文已经分发
            StringBuilder szJs = new StringBuilder("<script>alert(\"")
                    .append(ResourceBundleUtil.getString("com.seeyon.v3x.exchange.resources.i18n.ExchangeResource","edoc.distributed"))
                    .append("\");");
            if(!user.getId().equals(edocRegister.getDistributerId())) {//代理进去
                if("agent".equals(request.getParameter("app"))) {
                    szJs.append("if(window.dialogArguments) {") // 弹出
                        .append("   window.returnValue = \"true\";")
                        .append("   window.close();")
                        .append("} else {")
                        .append("   parent.parent.location.href='collaboration/pending.do?method=morePending&from=Agent';")
                        .append("}");
                } else {
                    szJs.append("if(window.dialogArguments) {") // 弹出
                        .append("   window.returnValue = \"true\";")
                        .append("   window.close();")
                        .append("} else {")
                        .append("   parent.parent.location.href='govdoc/govdoc.do?method=index&listType=listPending&govdocType=2,4&_resourceCode=F20_receiveManage';")
                        .append("}");
                }
            } else {
                szJs.append("if(window.dialogArguments) {") // 弹出
                    .append("   window.returnValue = \"true\";")
                    .append("   window.close();")
                    .append("} else {")
                    .append("   parent.parent.location.href='govdoc/govdoc.do?method=index&listType=listPending&govdocType=2,4&_resourceCode=F20_receiveManage';")
                    .append("}");
            }
            szJs.append("</script>");
            throw new NewEdocHandleException(szJs.toString(),NewEdocHandleException.PRINT_CODE);
        }
        //被撤销
        else if(edocRegister.getState()==EdocNavigationEnum.RegisterState.DraftBox.ordinal() || edocRegister.getState()==EdocNavigationEnum.RegisterState.deleted.ordinal()) {//被撤销
            StringBuilder szJs = new StringBuilder("<script>alert('")
                            .append(ResourceUtil.getString("edoc.alert.flow.edocStepBack", edocRegister.getSubject()))
                            .append("');");//公文《"+edocRegister.getSubject()+"》已经被撤销。
            if("agent".equals(request.getParameter("app")) && !user.getId().equals(edocRegister.getDistributerId())) {//代理进去
                //szJs += "parent.parent.parent.location.href='main.do?method=morePending4App&app=agent';";
            	szJs.append("parent.parent.location.href='collaboration/pending.do?method=morePending&from=Agent';");
            } else {
            	szJs.append("parent.parent.location.href='govdoc/govdoc.do?method=index&listType=listPending&govdocType=2,4&_resourceCode=F20_receiveManage';")
                    .append(edocType)
                    .append("'");
            }
            szJs.append("</script>");
            throw new NewEdocHandleException(szJs.toString(),NewEdocHandleException.PRINT_CODE);
        }
    
        registerId = edocRegister.getId();//登记id
        registerBody = edocRegisterManager.findRegisterBodyByRegisterId(edocRegister.getId());
        
        //登记单附件
        atts = attachmentManager.getByReference(edocRegister.getId(), edocRegister.getId()); 
        if(atts.size() == 0){
        	//从发文中找附件，这种情况产生的可能是 登记开关关闭了，直接签收到待分发了
        	/**先这样修改，最好的做法是在签收产生自动登记数据时，再创建登记对应的附件，以后重构时再完善**/
        	Long sendEdocId = edocRegister.getEdocId();
        	if(sendEdocId != null && sendEdocId != -1){
        		atts = attachmentManager.getByReference(sendEdocId, sendEdocId); 
        		if(Strings.isNotEmpty(atts)){
        			//只要发文的附件，不要关联文档
        			List<Attachment>  atts2 = new ArrayList<Attachment>();
                    for(Attachment att : atts){
                        if(att.getType() == 0){
                        	atts2.add(att);
                        }
                    }
                    atts = atts2;
        		}
        	}
        	
        }
        
        //电子登记的，先从发文获取summary
        if(edocRegister.getRegisterType() == 1) {
        	summary = edocManager.getEdocSummaryById(edocRegister.getEdocId(), true);
        	
        	if(edocRegister.getRecieveId()!=null && edocRegister.getRecieveId().longValue()!=0 && edocRegister.getRecieveId().longValue()!=-1) {
	            EdocRecieveRecord  recieveRecord = edocExchangeManager.getReceivedRecord(edocRegister.getRecieveId());
	            Long recAccountId = null;
	            if(recieveRecord.getExchangeType() == EdocRecieveRecord.Exchange_Receive_iAccountType_Org){
	                recAccountId = recieveRecord.getExchangeOrgId();
	            }else if(recieveRecord.getExchangeType() == EdocRecieveRecord.Exchange_Receive_iAccountType_Dept){
	                V3xOrgDepartment entity = orgManager.getDepartmentById(recieveRecord.getExchangeOrgId());
	                if(entity != null)
	                    recAccountId = entity.getOrgAccountId();
	            }
	            summary = NewEdocHelper.cloneNewSummaryAndSetProperties(user.getName(), summary, recAccountId, recieveRecord.getContentNo());
        	}
        	
        	summary.setIsQuickSend(false);
        	summary.setEdocType(1);
        	
        	/*
        	 //清空发文单上的抄送单位，抄报单位，主送单位B， 抄送单位B，抄报单位B
        	summary.setCopyTo(null);//清除抄送单位的数据
            summary.setCopyToId(null);
            */
            summary.setReportTo(null);
            summary.setReportToId(null);
            summary.setSendTo2(null);
            summary.setSendToId2(null);
            summary.setCopyTo2(null);
            summary.setCopyToId2(null);
            summary.setReportTo2(null);
            summary.setReportToId2(null);
        }
        
        //从登记中获取登记数据
        EdocHelper.copyEdocSummaryFromRegister(summary, edocRegister);

        //初始化收文自己的数据
        summary.setId(UUIDLong.longUUID());//以上操作将edocRegister的id赋值给summary的id，导致分发id与登记id一样
        summary.setCreatePerson(edocRegister.getRegisterUserName());
        
        //登记单正文
        registerBody = edocRegisterManager.findRegisterBodyByRegisterId(edocRegister.getId());
        if(null != registerBody){
            //BeanUtils.copyProperties(body, registerBody);   BeanUtils.copyProperties遇到空日期会报错，手动复制
            body.setContent(registerBody.getContent());
            body.setContentNo(registerBody.getContentNo());
            body.setContentType(registerBody.getContentType());
            body.setCreateTime(registerBody.getCreateTime());
            bodyContentType = body.getContentType();
        }
        cloneOriginalAtts = true;//正文是否要复制一份
        modelAndView.addObject("strEdocId", edocRegister.getEdocId());
        
        //用于切换文单
        if(edocRegister != null && edocRegister.getEdocId() != -1){
        	modelAndView.addObject("edocId", edocRegister.getEdocId());
        }
            
        
        ///////////////////////////////公文正文类型，如果没有office控件，默认显示为html
        body.setContentType(EdocHelper.getEdocBodyContentType(bodyContentType));
        Set<EdocBody> edocBodys= new HashSet<EdocBody>();
        edocBodys.add(body);
        summary.setEdocBodies(edocBodys);
        
        
        //G6电子收文调用模板发送后，又被回退到待分发时, 收文单页面要保持调用模板的状态
        if(s_summaryId == null){
        	if(edocRegister.getDistributeEdocId() != -1){
        		summary = edocManager.getEdocSummaryById(edocRegister.getDistributeEdocId(), false);
        		if(summary.getTempleteId() != null){
        			s_summaryId = String.valueOf(edocRegister.getDistributeEdocId());
        			templete = templeteManager.getCtpTemplate(summary.getTempleteId());
        			defaultEdocForm = edocFormManager.getEdocForm(summary.getFormId());
        			
        			modelAndView.addObject("isFromTemplate", templete.isSystem() || templete.getFormParentid() != null);
        			modelAndView.addObject("templateType",templete.getType());
                    modelAndView.addObject("templeteProcessId", templete.getWorkflowId());
                    modelAndView.addObject("newSummaryId",s_summaryId);
                    modelAndView.addObject("templeteId", summary.getTempleteId());
                }
        	}
        }
        //判断是否有收文待发页签
        boolean enableRecWaitSend = privilegeManager.checkByReourceCode(EdocConstant.F07_RECWAITSEND);
        modelAndView.addObject("enableRecWaitSend", String.valueOf(enableRecWaitSend)); 

    }

}
