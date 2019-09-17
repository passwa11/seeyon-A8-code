package com.seeyon.v3x.edoc.controller.newedoc;

import static java.io.File.separator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.edoc.enums.EdocEnum.MarkCategory;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.encrypt.CoderFactory;
import com.seeyon.ctp.common.filemanager.manager.Util;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseTemplateRole;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.v3x.edoc.constants.EdocOrgConstants;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.domain.EdocFormExtendInfo;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocFormManager;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocSwitchHelper;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.edoc.util.NewEdocHelper;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.domain.EdocSendDetail;
import com.seeyon.v3x.exchange.domain.EdocSendRecord;
import com.seeyon.v3x.exchange.manager.SendEdocManager;

public class FromTemplateToSendEdocImpl  extends NewEdocHandle{

    private static final Log LOGGER = LogFactory.getLog(FromTemplateToSendEdocImpl.class);
    
    @Override
    public void createEdocSummary(HttpServletRequest request, ModelAndView modelAndView) throws Exception {
        String forwardMember = request.getParameter("forwardMember");
        if(Strings.isNotBlank(forwardMember)){
            modelAndView.addObject("forwardMember", forwardMember);
        }
        String newSummaryId = request.getParameter("newSummaryId");
        if(Strings.isNotBlank(newSummaryId)){
            modelAndView.addObject("newSummaryId", newSummaryId);
        }
        
        
        String registerIdStr =  request.getParameter("registerId");
        String distributeEdocIdStr = request.getParameter("distributeEdocId");
        Long id = Long.valueOf(templeteId);
        //检查是否有调用此模板的权限
        boolean isTemplate=templeteManager.isTemplateEnabled(id, user.getId());
        
        templete= templeteManager.getCtpTemplate(id);
        
        modelAndView.addObject("temTraceType", templete.getCanTrackWorkflow());
        //OA-26502  lixsh调用格式模板后，编辑流程并插入关联文档，选择流程期限，提前提醒时间等后，存为个人模板在调用，关联文档和流程都不可以编辑了，流程期限和提醒没有了  
        String templeteTypeStr = templete.getType();
        
        //OA-44756调用格式模版另存为的个人模版，流程不能被编辑，只能查看
        //当调用模板之后，存为个人模板，这里需要找到个人模板的来源系统模板的类型
        if(!templete.isSystem() && templete.getFormParentid() != null){ 
            CtpTemplate fromTemplate = templeteManager.getCtpTemplate(templete.getFormParentid());
            templeteTypeStr = fromTemplate.getType();
            EdocSummary parentSummary = (EdocSummary) XMLCoder.decoder(fromTemplate.getSummary());
            modelAndView.addObject("parentSummary", parentSummary);
        } 
        
        // OA-8634  拟文，输入标题和流程后另存为个人模版，然后去调用这个模版，修改模版的标题，再次另存为后调用，报null  
        Long workflowId = templete.getWorkflowId();
        
        if(workflowId != null){
            
            if(!templete.isSystem()){//自由公文的个人模板
                
                boolean toSetXML = false;
                Long parentTempId = templete.getFormParentid();
                if(parentTempId != null){
                    CtpTemplate parentTemp = templeteManager.getCtpTemplate(parentTempId);
                    if(parentTemp == null || !parentTemp.isSystem()){
                        toSetXML = true;
                    }
                }else {
                    toSetXML = true;
                }
                
                //个人模版没有被修改的情况下
                if(toSetXML && (summary == null || summary.getProcessId() == null)){
                    
                    String process_xml = wapi.selectWrokFlowTemplateXml(String.valueOf(workflowId.longValue()));
                    if(Strings.isNotBlank(process_xml))
                        process_xml = Strings.escapeJavascript(process_xml);
                    modelAndView.addObject("process_xml", process_xml);
                }
            }else if("text".equals(templeteTypeStr) && !templete.isSystem()){//这段代码应该不会进入
                String process_xml = wapi.selectWrokFlowTemplateXml(String.valueOf(workflowId.longValue()));
                if(Strings.isNotBlank(process_xml))
                    process_xml = Strings.escapeJavascript(process_xml);
                modelAndView.addObject("process_xml", process_xml);
            }
        }
        
        isSystem = templete.isSystem();
        //OA-28258 被授权的人员调用模版未发送时，管理员去停用和删除该模版的处理方式不一致
        //模板删除了也是没有权限调用的
        if((isTemplate==false && isSystem==true)||templete.isDelete() ) {//没有公文发起权不能发送
            String errMsg=ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource","alert_not_edoctempleteload");
            
            //OA-28382 后台关闭自建流程，前台调用模版，同时管理员停用该模版，前台这时在调用界面确定，报js
            StringBuilder msg = new StringBuilder();
            msg.append("<script>");
            msg.append("alert('"+errMsg+"');");
            msg.append("parent.parent.location.href='edocController.do?method=listIndex&listType=newEdoc&edocType="+edocType+"';");
            msg.append("</script>");
            throw new NewEdocHandleException(msg.toString(),NewEdocHandleException.PRINT_CODE);
        }
        
        /**
         * 校验模板使用的文单是否启用
         */
        EdocSummary templasteSummary = (EdocSummary) XMLCoder.decoder(templete.getSummary());
       
        //不做修改的Templete里面的Summary
        EdocSummary srcTempSummary = (EdocSummary) XMLCoder.decoder(templete.getSummary());
        
        if(templasteSummary.getCanTrack() == 0 ){
          modelAndView.addObject("templatSetTrackNot", "true");
        }
        EdocFormManager edocFormManager = (EdocFormManager)AppContext.getBean("edocFormManager");
        //流程模板就不校验了，因为流程模板没有保存文单信息，没有与文单进行绑定
        if(templasteSummary.getFormId() != null){
        	EdocForm form = edocFormManager.getEdocForm(templasteSummary.getFormId());
            if(form != null){
            	Set<EdocFormExtendInfo> infos = form.getEdocFormExtendInfo();
                Iterator<EdocFormExtendInfo> it = infos.iterator();
                boolean flag = false;
                while(it.hasNext()){
                	EdocFormExtendInfo info = it.next();
                	if(info.getStatus() == 1){
                		flag = true;
                		break;
                	}
                }
                if(!flag){
                	StringBuilder msg = new StringBuilder();
                    msg.append("<script>");
                    msg.append("alert('"+ResourceUtil.getString("edoc.alert.templete.formNotAvailable")+"');");//模板使用的文单已不能使用，请联系管理员!
                    msg.append("parent.parent.location.href='edocController.do?method=listIndex&listType=newEdoc&edocType="+edocType+"';");
                    msg.append("</script>");
                    throw new NewEdocHandleException(msg.toString(),NewEdocHandleException.PRINT_CODE);
                }
            }
        }
        
        
        standarduration = templete.getStandardDuration()== null ? 0 : Integer.parseInt(templete.getStandardDuration().toString());
       
        //yangzd 如果新建公文时，先调用流程模板，再调用格式模板，则会本次要保持流程模板；反之也是。
        
        CtpTemplate oldtemplete=null; 
        Long oldid=null;
        //yangzd
        if (StringUtils.isNotEmpty(oldtempleteId)&&!"1".equals(oldtempleteId)) {
            oldid = Long.valueOf(oldtempleteId);
            oldtemplete = templeteManager.getCtpTemplate(oldid);
        }
        modelAndView.addObject("isSystem", templete.isSystem());
        modelAndView.addObject("isFormParentId", templete.getFormParentid() != null);


        
        
      //现在统一改为附件可以删除的
        canDeleteOriginalAtts = true;
        
        //OA-26502  lixsh调用格式模板后，编辑流程并插入关联文档，选择流程期限，提前提醒时间等后，存为个人模板在调用，关联文档和流程都不可以编辑了，流程期限和提醒没有了  end
        
        
        modelAndView.addObject("isFromTemplate", templete.isSystem() || templete.getFormParentid() != null);
        
        

        String docMark = templasteSummary.getDocMark();
        if(Strings.isNotBlank(docMark) && docMark.indexOf("|")>-1){
        	docMark = docMark.split("[|]")[0];
        	if(edocMarkDefinitionManager.judgeEdocDefinitionExsit(Long.parseLong(docMark))==0){
        		templasteSummary.setDocMark(null);
        	}
        }
        String docMark2 =templasteSummary.getDocMark2();
        if(Strings.isNotBlank(docMark2) && docMark2.indexOf("|")>-1){
        	docMark2 = docMark2.split("[|]")[0];
        	if(edocMarkDefinitionManager.judgeEdocDefinitionExsit(Long.parseLong(docMark2))==0){
        		templasteSummary.setDocMark2(null);
        	}
        }
        String SerialNo =templasteSummary.getSerialNo();
        if(Strings.isNotBlank(SerialNo) && SerialNo.indexOf("|")>-1){
        	SerialNo = SerialNo.split("[|]")[0];
        	if(edocMarkDefinitionManager.judgeEdocDefinitionExsit(Long.parseLong(SerialNo))==0){
        		templasteSummary.setSerialNo(null);
        	}
        }
        
        String docMarkByTemplateJs = EdocHelper.exeTemplateMarkJs(templasteSummary.getDocMark(), 
                templasteSummary.getDocMark2(), templasteSummary.getSerialNo());
        modelAndView.addObject("docMarkByTemplateJs", docMarkByTemplateJs); 
        
        //OA-47358升级测试：调用格式模版，在拟文页面流程期限是置灰的
        /**
         * 升级前 创建模板时，如果没有设置流程期限，那么保存后的template表中的summary数据没有包含流程期限<deadline>0</deadline>
         * 但升级后 这样创建模板时包含了 <deadline>0</deadline>
         * 所以可以在这里加上一个判断，当deadline为Null时 设置其值为0
         */
        if(templasteSummary.getDeadline() == null){
            templasteSummary.setDeadline(0L);
        }
        //和流程期限处理方式一样
        if(templasteSummary.getAdvanceRemind() == null){
            templasteSummary.setAdvanceRemind(0L);
        }
        modelAndView.addObject("summaryFromTemplate", templasteSummary); //获得模板的edcosummary对象。比如流程期限，如果模板流程期限设置了，需要把流程期限置灰。
        modelAndView.addObject("hasTemplate", true); //xiangfan 添加 修复 设置不允许公文之间流程，拟文 选择个人模板时无限弹出选择模板窗口的错误 GOV-5038 
        modelAndView.addObject("templateType",templeteTypeStr);
        templeteType = templete.getType();
        //OA-27739 was环境：单位管理员新建流程模板，设置了预归档，前台调用时可以修改预归档
        Long archiveId = templasteSummary.getArchiveId();
        if(archiveId!=null){
            //从待发编辑如果来自模板 同样设置了这个
            modelAndView.addObject("setArchive", "true");
        }
        
        //当调用的不是正文模板时，就保存模板id,表示流程已经编辑了，前台发文时就可以直接发送了
        if(!TemplateEnum.Type.text.name().equals(templeteType)){
            //非正文模板才有 流程id
            templeteProcessId = templete.getWorkflowId()==null?-1l:templete.getWorkflowId();
            modelAndView.addObject("templeteProcessId",templeteProcessId);
            //OA-22553 兼职人员test01在兼职单位的公文自建流程开关未开，流程框也提示不可自建流程，但编辑流程可以新建
            //下面也是从request中取的，从modelAndView中取不到
            request.setAttribute("templeteProcessId", templeteProcessId);
        }
        
        if(templete.getFormParentid() != null){//系统模板存为个人模板，则通过父级模板ID查到workflowId
            CtpTemplate pTemplate= templeteManager.getCtpTemplate(templete.getFormParentid());
            if(pTemplate.getWorkflowId()!=null){
                templeteProcessId= pTemplate.getWorkflowId();
            }
            modelAndView.addObject("templeteProcessId",templeteProcessId);
        }
        
        if(iEdocType==-1) {
            iEdocType=EdocUtil.getEdocTypeByTemplateType(templete.getModuleType());
            edocType=Integer.toString(iEdocType);
        }
         
        //yangzd
        iEdocType = EdocEnum.getEdocTypeByTemplateCategory(Integer.parseInt(templete.getCategoryId().toString()));
        String waitRegister_recieveId = request.getParameter("waitRegister_recieveId");
        
        /*待登记 选一个电子公文 进行收文分发，调用模板之后，需要保存该签收id,这样在发送或 保存待发*/
        if(Strings.isNotBlank(waitRegister_recieveId)){
            modelAndView.addObject("waitRegister_recieveId",waitRegister_recieveId);
        }
        
        if(Strings.isNotBlank(newSummaryId)){ 
            EdocRecieveRecord edRe = recieveEdocManager.getEdocRecieveRecordByReciveEdocId(Long.parseLong(newSummaryId));
            if(edRe!=null)
            waitRegister_recieveId = String.valueOf(edRe.getId());
        }
        if(Strings.isNotBlank(waitRegister_recieveId)){//登记电子公文的时候
        	canUpdateContent = EdocSwitchHelper.canUpdateAtOutRegist(user.getLoginAccount());//判断当前单位是否允许修改外来文的正文内容
        }
        
        
        //通过登记进行分发，调用模板 
        if(openTempleteOfExchangeRegist || Strings.isNotBlank(waitRegister_recieveId)) {
            String strEdocId = request.getParameter("strEdocId");
            Long orgAccountId = Strings.isBlank(request.getParameter("orgAccountId")) ? 0L : Long.parseLong(request.getParameter("orgAccountId"));

            //分发直接与登记关联，去掉与签收表之间的关联
            edocRegister = edocRegisterManager.findRegisterById(registerId);
            //A8收文登记调用模板
            if(Strings.isNotBlank(waitRegister_recieveId)) {
                distributeEdocId = Strings.isBlank(request.getParameter("distributeEdocId")) ? -1 : Long.parseLong(request.getParameter("distributeEdocId"));
                Long sendEdocId = distributeEdocId;//发文的ID
                if(distributeEdocId!=-1 && distributeEdocId!=0) {
                	summary = edocManager.getEdocSummaryById(distributeEdocId, true);
                	if(summary == null){
                		if(Strings.isNotBlank(waitRegister_recieveId)){
                			record = recieveEdocManager.getEdocRecieveRecord(Long.parseLong(waitRegister_recieveId));
                			if(record!=null){
                				summary = edocManager.getEdocSummaryById(record.getEdocId(), true);
                				
                				if(Strings.isNotEmpty(record.getReplyId())){//书生交换replyId为空
                                    long sendDetailId = Long.parseLong(record.getReplyId());
                                    
                                  //获取送文单上的主送单位
                                    SendEdocManager sendEdocManager = (SendEdocManager)AppContext.getBean("sendEdocManager");
                                    EdocSendDetail sendDetail = sendEdocManager.getSendRecordDetail(sendDetailId);
                                   
                                    String sendToId = "";
                                    String sendToNames = "";
                                    if(sendDetail == null){ //A8书生收文没有sendDetail数据
                                        sendToNames = record.getSendTo();
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
                                    //2015年11月5日 15:53:11 lt 调用模版后 来文时间丢失
                	                if(edocRegister != null && edocRegister.getRecTime() != null){
                	                	summary.setReceiptDate(new java.sql.Date(edocRegister.getRecTime().getTime()));
                	                }
                                    
                                  //文号相关的信息与模版保持一致
                                    if(Strings.isBlank(summary.getDocMark())){
                                        //收文，保持原有的文号
                                        summary.setDocMark(srcTempSummary.getDocMark());
                                    }
                                    
                                  //份数
                                    if(summary.getCopies() == null || summary.getCopies() == 0){
                                        //收文，保持原有的文号
                                        summary.setCopies(srcTempSummary.getCopies());
                                    }
                                    
                                    //紧急程度
                                    if(Strings.isBlank(summary.getUrgentLevel())){
                                        //收文，保持原有的文号
                                        summary.setUrgentLevel(srcTempSummary.getUrgentLevel());
                                    }
                                    
                                    //密级
                                    if(Strings.isBlank(summary.getSecretLevel())){
                                        //收文，保持原有的文号
                                        summary.setSecretLevel(srcTempSummary.getSecretLevel());
                                    }
                                    
                                    summary.setDocMark2(srcTempSummary.getDocMark2());
                                    summary.setSerialNo(srcTempSummary.getSerialNo());
                                }
                				
                				sendEdocId = record.getEdocId();
                				openTempleteOfExchangeRegist = true;
                			}
                		}
                	}
                    if(summary == null) {
                        summary = new EdocSummary();
                        body = new EdocBody();
                        summary.getEdocBodies().add(body);
                    }
                    
                    if(record!=null){
                    	//登记的时候，来文是经过word转pdf的，就有两个正文。那么就要根据签收表的content_no来查正文是哪个。
                    	body=summary.getBody(record.getContentNo());
                    }else{
                    	body = summary.getFirstBody();
                    }
                    int contentNo = body==null?0:body.getContentNo();
                    atts = NewEdocHelper.excludeType2ToNewAttachmentList(summary);
                    String serialNo = summary.getSerialNo();
                    summary = NewEdocHelper.cloneNewSummaryAndSetProperties(user.getName(), summary, orgAccountId, contentNo);
                    summary.setSerialNo(serialNo);
                    if(summary.isNew()) {
                        summary.setId(distributeEdocId);
                    }
                }
                canDeleteOriginalAtts = false;    //不依赖登记的收文调用模板，系统模板不能删，个人模板可删除附件
                comm = "register";//控制正文类型不可以切换
            }
            //G6分发调用模板
            else if(edocRegister != null) {
                registerId = edocRegister.getId();
                registerBody = edocRegisterManager.findRegisterBodyByRegisterId(edocRegister.getId());
                //登记已分发
                distributeEdocId = edocRegister.getDistributeEdocId();
                if(distributeEdocId!=-1 && distributeEdocId!=0) {
                    summary = edocManager.getEdocSummaryById(distributeEdocId, true);
                    summary = NewEdocHelper.cloneNewSummaryAndSetProperties(user.getName(), summary, orgAccountId, registerBody.getContentNo());
                    summary.setSigningDate(edocRegister.getEdocDate());
                    if(summary.isNew()) {
                        summary.setId(distributeEdocId);
                    }
                    atts = attachmentManager.getByReference(distributeEdocId, distributeEdocId);
                } else {//登记未分发
                    summary = new EdocSummary();                        
                    body = new EdocBody();
                    //登记正文                  
                    bodyContentType = null;
                    if(null != registerBody) {
                        body.setContent(registerBody.getContent());
                        body.setContentNo(registerBody.getContentNo());
                        body.setContentType(registerBody.getContentType());
                        body.setCreateTime(registerBody.getCreateTime());
                        bodyContentType = body.getContentType();
                    }
                    body.setContentType(EdocHelper.getEdocBodyContentType(bodyContentType));
                    summary.getEdocBodies().add(body);
                    
                    atts = attachmentManager.getByReference(registerId, registerId);
                    //登记单附件
                    if(distributeEdocId==-1 && Strings.isEmpty(atts)){ //没有登记环节，直接签收过来到待分发
                        
                        List<Attachment> attList = attachmentManager.getByReference(edocRegister.getEdocId(), edocRegister.getEdocId());
                        atts = new ArrayList<Attachment>();
                        
                        //只要拟文添加和处理修改添加的附件，不要处理时填意见所添加的附件
                        for(Attachment att : attList){
                            //type=0表示附件 (附件元素上不显示关联文档)
                            if(att.getType().intValue() != 2){// 等于2是关联文档，需要过滤
                                atts.add(att);
                            }
                        }
                    }
                    
                    //将登记同属性值赋给收文
                    BeanUtils.copyProperties(summary, edocRegister);
                    summary.setSigningDate(edocRegister.getEdocDate());
                    //2015年11月5日 15:53:11 lt 调用模版后 来文时间丢失
                    summary.setReceiptDate(new java.sql.Date(edocRegister.getRecTime() == null ? 0 : edocRegister.getRecTime().getTime()));
                    //复制summary部分参数，该操作后id将后置为null
                    summary.setSendUnit(edocRegister.getEdocUnit());//来文单文
                    //lijl注消(与桂林沟通过),GOV-4569.成都市教育局bug：公文登记后分发时来文单位不显示，紧急程度变成普通，如果更换模板，公文公文号需要重新选择.
                    //summary=cloneNewSummaryAndSetProperties(user.getName(), summary, orgAccountId, registerBody.getContentNo());
                    //收文登记未经过分发，summary重新获取id需
                    summary.setId(UUIDLong.longUUID());//以上操作将edocRegister的id赋值给summary的id，导致分发id与登记id一样
                }
                canDeleteOriginalAtts = true;    //登记过来的收文调用模板，附件可删除
                comm = "distribute"; //不能去掉这个设置，页面分发调用模板的时候需要根据这个参数来过滤显示模板
                
                //文号相关的信息与模版保持一致
                if(summary.getEdocType() == com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_REC 
                        && Strings.isNotBlank(summary.getDocMark())){
                    //收文，保持原有的文号
                }else{
                    summary.setDocMark(srcTempSummary.getDocMark());
                }
                
              //份数
                if(summary.getEdocType() == com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_REC 
                        && summary.getCopies() != null && summary.getCopies() != 0){
                    //收文，保持原有的文号
                }else{
                    summary.setCopies(srcTempSummary.getCopies());
                }
                
                //紧急程度
                if(summary.getEdocType() == com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_REC 
                        && Strings.isNotBlank(summary.getUrgentLevel())){
                    //收文，保持原有的文号
                }else{
                    summary.setUrgentLevel(srcTempSummary.getUrgentLevel());
                }
                
                //密级
                if(summary.getEdocType() == com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_REC 
                        && Strings.isNotBlank(summary.getSecretLevel())){
                    //收文，保持原有的文号
                }else{
                    summary.setSecretLevel(srcTempSummary.getSecretLevel());
                }
                
                summary.setDocMark2(srcTempSummary.getDocMark2());

                //summary.setSerialNo(srcTempSummary.getSerialNo());
                if(summary.getEdocType() == com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_REC 
                        && Strings.isNotBlank(summary.getSerialNo())){
                    //收文，保持原有的文号
                } else {
                	summary.setSerialNo(srcTempSummary.getSerialNo());
                }
                summary.setReceiptDate(edocRegister.getRegisterDate());
            } 
            
            
            //调用模板使用的默认公文单
            if(!TemplateEnum.Type.workflow.name().equals(templete.getType())) {
                summary.setFormId(srcTempSummary.getFormId());
            } else {
                defaultEdocForm = edocFormManager.getDefaultEdocForm(user.getLoginAccount(),iEdocType);
                summary.setFormId(defaultEdocForm.getId());
            }

            iEdocType = EdocEnum.edocType.recEdoc.ordinal();
            summary.setEdocType(iEdocType);
            
            modelAndView.addObject("strEdocId", strEdocId);
            
            //设置预归档目录
            summary.setArchiveId(srcTempSummary.getArchiveId());
            summary.setCanTrack(srcTempSummary.getCanTrack());
            summary.setDeadline(srcTempSummary.getDeadline());
            summary.setAdvanceRemind(srcTempSummary.getAdvanceRemind());
            //设置当前登记日期
            java.sql.Date oldDate = summary.getRegistrationDate();
            summary.setRegistrationDate(new java.sql.Date(System.currentTimeMillis()));
            if(oldDate != null){
                List<CtpAffair> affairs = affairManager.getAffairs(summary.getId(), StateEnum.col_waitSend);
                if(Strings.isNotEmpty(affairs)){
                    for(CtpAffair affair : affairs){
                        if(affair.getSubState() == SubStateEnum.col_pending_specialBackToSenderCancel.key()
                                || affair.getSubState() == SubStateEnum.col_pending_specialBacked.key()){
                            summary.setRegistrationDate(oldDate);
                            break;
                        }
                    }
                }
            }
        } 
        //直接收文分发，调用模板
        else {
            EdocRecieveRecord record2 = null;
            if(Strings.isNotBlank(waitRegister_recieveId)){
                record2 = recieveEdocManager.getEdocRecieveRecord(Long.parseLong(waitRegister_recieveId));
            }
            
            ////OA-42870 被交换出去的公文，调用模版时，原发文内容会被模版内容覆盖掉
            //当公文经过交换到收文流程，是电子登记时调用收文模板，正文内容不被模板覆盖
            distributeEdocId = Strings.isBlank(request.getParameter("distributeEdocId")) ? -1 : Long.parseLong(request.getParameter("distributeEdocId"));
            EdocSummary oldSummary = edocManager.getEdocSummaryById(distributeEdocId, true);
            if(!(iEdocType==1 && ((oldSummary != null && oldSummary.getEdocBodies().size() > 0) || Strings.isNotBlank(waitRegister_recieveId)))){
                MainbodyManager contentManager = (MainbodyManager) AppContext.getBean("ctpMainbodyManager");
                ModuleType moduleType = EdocUtil.getModuleTypeInSystem(templete.getModuleType());
                List<CtpContentAll> contents = null;
                if(!TemplateEnum.Type.workflow.name().equals(templete.getType())){
                	contents = contentManager.getContentList(moduleType, templete.getId(), "");
                }

                if(contents != null && contents.size()>0){
                    CtpContentAll ctpcontent = contents.get(0);
                    body = new EdocBody();
                    body.setIdIfNew();
                    if(ctpcontent.getContentType() == 10){
                        body.setContentType("HTML");
                        body.setContent(ctpcontent.getContent());
                    }else if(ctpcontent.getContentType() == 41){
                        body.setContentType("OfficeWord");
                        body.setContent(String.valueOf(ctpcontent.getContentDataId()));
                    }else if(ctpcontent.getContentType() == 42){
                        body.setContentType("OfficeExcel");
                        body.setContent(String.valueOf(ctpcontent.getContentDataId()));
                    }else if(ctpcontent.getContentType() == MainbodyType.Pdf.getKey()) {
                        body.setContentType(MainbodyType.Pdf.name());
                        body.setContent(String.valueOf(ctpcontent.getContentDataId()));
                    }else if(ctpcontent.getContentType() == MainbodyType.WpsWord.getKey()) {
                        body.setContentType(MainbodyType.WpsWord.name());
                        body.setContent(String.valueOf(ctpcontent.getContentDataId()));
                    }else if(ctpcontent.getContentType() == MainbodyType.WpsExcel.getKey()) {
                        body.setContentType(MainbodyType.WpsExcel.name());
                        body.setContent(String.valueOf(ctpcontent.getContentDataId()));
                    }
                    body.setCreateTime((Timestamp)ctpcontent.getCreateDate());
                }
            }
            
            if(!TemplateEnum.Type.workflow.name().equals(templete.getType())) {//非流程模板
                //不是通过交换进行登记，调用模板        
                summary = (EdocSummary)XMLCoder.decoder(templete.getSummary());
                summary.setOrgAccountId(null);//单位ID由被调用者赋值
                if(body != null){
                    summary.getEdocBodies().add(body);
                }else{
                    //OA-42870 被交换出去的公文，调用模版时，原发文内容会被模版内容覆盖掉
                    //body为null，表示没有用模板的正文内容，沿用发文的正文内容
                    //EdocSummary sendSummary = edocManager.getEdocSummaryById(record2.getEdocId(), true);
                    Set<EdocBody> sendBodies = oldSummary.getEdocBodies();
                    for(EdocBody body:sendBodies){
                        summary.getEdocBodies().add(body);
                    }                    
                    //OA-46766 lip登记外来文，使用默认文单时，送文单位显示的来文单位，调用了模板后就变成了当前单位
                    //如果模板没有设置送文单位，则调用模板后，直接显示送文单的来文单位，否则用模板设置的送文单位
                    if(Strings.isBlank(summary.getSendUnit())){
                        summary.setSendUnit(oldSummary.getSendUnit());
                        summary.setSendUnitId(oldSummary.getSendUnitId());
                    }
                }
                iEdocType=summary.getEdocType();
            } else {//流程模板
                 //读取上一次的非流程模板的正文
                if(null!=oldtemplete && TemplateEnum.Type.workflow.name().equals(oldtemplete.getType())) {
                    summary = (EdocSummary)XMLCoder.decoder(oldtemplete.getSummary());
                    summary.setOrgAccountId(null);//单位ID由被调用者赋值
                    summary.getEdocBodies().add(body);
                    iEdocType=summary.getEdocType();
                } else {//读取上一次的是流程模板的正文
                    summary = new EdocSummary();
                    summary.setEdocType(iEdocType);
                    body = new EdocBody();
                    defaultEdocForm=edocFormManager.getDefaultEdocForm(user.getLoginAccount(),iEdocType);
                    summary.setFormId(defaultEdocForm.getId());
                    if(record2==null){
                        bodyContentType=Constants.EDITOR_TYPE_OFFICE_WORD;
                        if(com.seeyon.ctp.common.SystemEnvironment.hasPlugin("officeOcx")==false){
                            bodyContentType=Constants.EDITOR_TYPE_HTML;
                        }
                        body.setContentType(bodyContentType);
                        summary.getEdocBodies().add(body); 
                        summary.setCanTrack(1);
                    }else{
                        //电子收文 调用流程模板
                        EdocSummary sendSummary = edocManager.getEdocSummaryById(record2.getEdocId(), true);
                        Set<EdocBody> sendBodies = sendSummary.getEdocBodies();
                        for(EdocBody body:sendBodies){
                            summary.getEdocBodies().add(body);
                        }                    
                        //OA-46766 lip登记外来文，使用默认文单时，送文单位显示的来文单位，调用了模板后就变成了当前单位
                        //如果模板没有设置送文单位，则调用模板后，直接显示送文单的来文单位，否则用模板设置的送文单位
                        if(Strings.isBlank(summary.getSendUnit())){
                            summary.setSendUnit(sendSummary.getSendUnit());
                            summary.setSendUnitId(sendSummary.getSendUnitId());
                        }
                        //电子收文调用流程模板 正文组件也要置灰，不能选择
                        modelAndView.addObject("dianziRec_workflow_template", "true");
                    }
                }
            }
            Long registerId = StringUtils.isBlank(registerIdStr) ? -1 : Long.valueOf(registerIdStr);
            EdocRegister edocRegister = edocRegisterManager.findRegisterById(registerId);
            if(edocRegister != null){
            	BeanUtils.copyProperties(summary, edocRegister);
            	if(summary.getReceiptDate() == null) summary.setReceiptDate(edocRegister.getRegisterDate());
            }
            if(oldSummary != null){
            	if(summary.getSerialNo() == null)summary.setSerialNo(oldSummary.getSerialNo());
            	if(summary.getDocMark() == null)summary.setDocMark(oldSummary.getDocMark());
            	if(summary.getDocMark2() == null)summary.setDocMark2(oldSummary.getDocMark2());
            	if(summary.getSendUnit() == null || StringUtils.isBlank(summary.getSendUnit())) summary.setSendUnit(oldSummary.getSendUnit());
            }
            if(record2!=null){
                summary.setSubject(record2.getSubject());
                //OA-46229wangchw登记电子公文自由流程时，来文文号显示，调用收文模板后，文号就不显示了
                if(record2.getDocMark() != null){
                    summary.setDocMark(record2.getDocMark());//来文字号
                }
            }
            
            //设置预归档目录
            summary.setArchiveId(srcTempSummary.getArchiveId());
            summary.setCanTrack(srcTempSummary.getCanTrack());
            summary.setDeadline(srcTempSummary.getDeadline());
            summary.setAdvanceRemind(srcTempSummary.getAdvanceRemind());
            
            if(distributeEdocId != -1 && iEdocType==1) {
                summary.setId(distributeEdocId);
            }
            
           
            //------------上面只是电子收文的时候，调用模板后原来的附件和模板附件都显示
            
            
            summary.setCreatePerson(user.getName());
            EdocHelper.reLoadAccountName(summary, EdocHelper.getI18nSeperator(request));
        }
        
        //合并模板的附件，即交换过来的公文的附件取发文+模板的
        List<Attachment> tatts = attachmentManager.getByReference(templete.getId(), templete.getId());
        if(Strings.isEmpty(atts)){
        	atts = new ArrayList<Attachment>();
        }
        atts.addAll(tatts);
        
        
        cloneOriginalAtts = true;
        if(summary != null) {
            senderOpinion = summary.getSenderOpinion();
        }           
        edocFormId = summary.getFormId();
        //summary.setOrgAccountId(user.getLoginAccount());//NewEdocHandle里面有配置
        summary.setTempleteId(Long.parseLong(templeteId));
        //检查模版公文单是否存在
        
        if(null != edocFormId) {
            if(defaultEdocForm==null){defaultEdocForm=edocFormManager.getEdocForm(edocFormId);}
            if(defaultEdocForm==null){defaultEdocForm=edocFormManager.getDefaultEdocForm(summary.getOrgAccountId(),iEdocType);}
        }
                
        modelAndView.addObject("templete", templete);
        
        //模板督办
       CtpSuperviseDetail detail = superviseManager.getSupervise(id);
        if(detail != null) {
            Long terminalDate = detail.getTemplateDateTerminal();
            //OA-50015  当收文模板什么数据都预置了后，调用该模板无法另存为个人模板  
            if(!templete.isSystem() && detail.getAwakeDate()!=null){
                String date = Datetimes.format(detail.getAwakeDate(),Datetimes.datetimeWithoutSecondStyle);
                modelAndView.addObject("superviseDate", date);
            }
            else if(null!=terminalDate) {
                Date superviseDate = Datetimes.addDate(new Date(), terminalDate.intValue());
                String date = Datetimes.format(superviseDate,Datetimes.datetimeWithoutSecondStyle);
                detail.setAwakeDate(superviseDate);
                modelAndView.addObject("superviseDate", date);
            }
            List<CtpSupervisor> superviseors =  superviseManager.getSupervisors(detail.getId());
            Set<String> sIdSet = new HashSet<String>();
            StringBuilder ids = new StringBuilder();
            StringBuilder names = new StringBuilder();
            
            for(CtpSupervisor supervisor:superviseors){
                sIdSet.add(supervisor.getSupervisorId().toString());
            }
            List<CtpSuperviseTemplateRole> roleList = superviseManager.findRoleByTemplateId(templete.getId());
            if((null!=roleList && !roleList.isEmpty()) || !sIdSet.isEmpty()){
                modelAndView.addObject("sVisorsFromTemplate", "true");//公文调用的督办模板是否设置了督办人
            }
            V3xOrgRole orgRole = null;
            
            for(CtpSuperviseTemplateRole role : roleList) {
                if(null==role.getRole() || "".equals(role.getRole())){
                    continue;
                }
                if(role.getRole().toLowerCase().equals(EdocOrgConstants.ORGENT_META_KEY_SEDNER.toLowerCase())){
                    sIdSet.add(String.valueOf(user.getId()));
                }
                boolean haveManager = false;
                if(role.getRole().toLowerCase().equals(EdocOrgConstants.ORGENT_META_KEY_DEPMANAGER.toLowerCase())||role.getRole().toLowerCase().equals(EdocOrgConstants.ORGENT_META_KEY_SEDNER.toLowerCase() + EdocOrgConstants.ORGENT_META_KEY_DEPMANAGER.toLowerCase())){
                    orgRole = orgManager.getRoleByName(EdocOrgConstants.ORGENT_META_KEY_DEPMANAGER, user.getLoginAccount());
                    if(null!=orgRole){
                        List<V3xOrgDepartment> depList = orgManager.getDepartmentsByUser(user.getId());
                        for(V3xOrgDepartment dep : depList){
                            List<V3xOrgMember> managerList = orgManager.getMembersByRole(dep.getId(), orgRole.getId());
                            for(V3xOrgMember mem : managerList){
                                haveManager = true;
                                sIdSet.add(mem.getId().toString());
                            }
                        }
                    }
                } else {
                    modelAndView.addObject("isOnlySender", "true");
                }
                if(!haveManager){
                    modelAndView.addObject("noDepManager", "true");
                }

                if(role.getRole().toLowerCase().equals(EdocOrgConstants.ORGENT_META_KEY_SEDNER.toLowerCase() + EdocOrgConstants.ORGENT_META_KEY_SUPERDEPMANAGER.toLowerCase())){
                    orgRole = orgManager.getRoleByName(EdocOrgConstants.ORGENT_META_KEY_SUPERDEPMANAGER, user.getLoginAccount());
                    if(null!=orgRole){
                        List<V3xOrgDepartment> depList = orgManager.getDepartmentsByUser(user.getId());
                        for(V3xOrgDepartment dep : depList){
                        List<V3xOrgMember> superManagerList = orgManager.getMembersByRole(dep.getId(), orgRole.getId());
                            for(V3xOrgMember mem : superManagerList){
                                sIdSet.add(mem.getId().toString());
                            }
                        }
                    }
                }   
            }
            
            for(String s : sIdSet){
                V3xOrgMember mem = orgManager.getMemberById(Long.valueOf(s));
                if(mem!=null && mem.getId()!=0) {
                    ids.append(mem.getId());
                    ids.append(",");
                    names.append(mem.getName());
                    names.append(",");
                }
            }
            
            if(ids.length()>1 && names.length()>1){
                modelAndView.addObject("colSupervisors", ids.substring(0, ids.length()-1));
                detail.setSupervisors(names.substring(0, names.length()-1));
            }
            //OA-26859 调用发文模板后将其存为个人模板，督办设置，基准时长没有保存（如图）
            modelAndView.addObject("awakeDate", Datetimes.format(detail.getAwakeDate(), Datetimes.datetimeWithoutSecondStyle));
            modelAndView.addObject("unCancelledVisor",Strings.join(sIdSet,","));
            modelAndView.addObject("colSupervise", detail);
        }
        if("transmitSend".equals(request.getParameter("fromState"))) {//转发调用模版操作
            List<Attachment> attTemp = (List<Attachment>)request.getSession().getAttribute("transmitSendAtts");
            if(attTemp!=null && attTemp.size()>0) {
                atts.addAll(attTemp);
            }
        }
        
        //BUG20120725011863_G6_v1.0_徐州市元申软件有限公司_公文收文结束转发文，调用模板附件、正文、文单内容丢失--start
        //收文转发文
        if("forwordtosend".equals(request.getParameter("fromState"))){
            
            modelAndView.addObject("comm", comm);
            
            String strEdocId=request.getParameter("edocId");//strEdocId变量获取需要处理的表单 
            if(Strings.isNotBlank(strEdocId)){//调用模板后，收文id要传递过去
                modelAndView.addObject("receiveEdocIdFromTemplate", strEdocId);
            }
            //如果是从待登记中进行转发文，会得到签收id
            String recieveId = request.getParameter("recieveId"); 
            if(Strings.isNotBlank(recieveId)){
                modelAndView.addObject("recieveId", recieveId);
            }
            String forwordType = request.getParameter("forwordType");
            if(Strings.isNotBlank(forwordType)){
                modelAndView.addObject("forwordType", forwordType);
            }
            
            //是否关联收文
            String relationRecd=request.getParameter("relationRecd");
            if(Strings.isNotBlank(relationRecd)){
                modelAndView.addObject("relationRecd", relationRecd);
            }
            //是否关联发文
            String relationSend=request.getParameter("relationSend");
            if(Strings.isNotBlank(relationSend)){
                modelAndView.addObject("relationSend", relationSend);
            }
            
            String recSummaryId=request.getParameter("recSummaryId");
            if(Strings.isNotBlank(recSummaryId)){
                modelAndView.addObject("recSummaryId", recSummaryId);
            }
            
            checkOption=request.getParameter("checkOption");
            /******** puyc 添加  关联收文的路径 *******/
            //OA-34506 wangchw在收文已发送中勾选数据进行转发文，在拟文页面调用模板后，在点击发文关联收文，报null
            String recAffairId = request.getParameter("recAffairId");
            //在拟文页面中设置收文affairId
            modelAndView.addObject("forwordtosend_recAffairId", recAffairId);
            String relationUrl = NewEdocHelper.relationReceive(strEdocId, "1")+"&affairId="+recAffairId;;
            modelAndView.addObject("relationUrl", relationUrl);
            /******** puyc 添加 结束 *******/
            Long edocId=0L;
            modelAndView.addObject("forwordtosend", "1");//正文转附件后，第一次点击不提示正文已被修改
            String transmitSendNewEdocId=request.getParameter("transmitSendNewEdocId");
            if(strEdocId!=null && !"".equals(strEdocId)){edocId=Long.parseLong(strEdocId);}
            Long orgAccountId=0L;
            EdocBody body_orinal=null;
            summary=edocManager.getEdocSummaryById(edocId,true); //获取Summary对象
            if(summary == null) {//如果来自待分发，传递的是登记单id
                edocRegister = this.edocRegisterManager.getEdocRegister(edocId);
                if(edocRegister != null) {
                    summary = new EdocSummary();
                    BeanUtils.copyProperties(summary, edocRegister);
                    //2015年11月5日 15:53:11 lt 调用模版后 来文时间丢失
                    summary.setReceiptDate(new java.sql.Date(edocRegister.getRecTime().getTime()));
                    registerBody = this.edocRegisterManager.findRegisterBodyByRegisterId(edocId);
                    if(registerBody != null) {
                        body_orinal = new EdocBody();
                        body_orinal.setContent(registerBody.getContent());
                        body_orinal.setContentType(registerBody.getContentType());
                        body_orinal.setCreateTime(registerBody.getCreateTime());
                        summary.setEdocBodies(new HashSet<EdocBody>());
                        summary.getEdocBodies().add(body_orinal);
                    }
                }
            }
            //GOV-4935.（需求）收文转发文，调用发文模版，但是文单中内容仍然为原收文文单内容。附件的合并--start
            List<Attachment> atts2=NewEdocHelper.excludeType2ToNewAttachmentList(summary);
            if(atts!=null && atts2!=null ){
                atts.addAll(atts2);
            }else{
                atts=atts2;
            }
            //GOV-4935.（需求）收文转发文，调用发文模版，但是文单中内容仍然为原收文文单内容。附件的合并---end
            summary=NewEdocHelper.cloneNewSummaryAndSetProperties(user.getName(), summary, orgAccountId,iEdocType);//克隆summary对象
            //summary.setId(UUIDLong.longUUID());//以上操作将edocRegister的id赋值给summary的id，导致分发id与登记id一样
            //defaultEdocForm=edocFormManager.getDefaultEdocForm(user.getLoginAccount(),iEdocType);//获取默认公文单
            //summary.setFormId(defaultEdocForm.getId());//元素绑定表单
            summary.setEdocType(EdocEnum.edocType.recEdoc.ordinal());
            String subject=summary.getSubject();//获取原有标题
            body_orinal=summary.getFirstBody();//获取正文
            
            summary.setTempleteId(Long.parseLong(templeteId));//设置模版信息
            
            //文号相关的信息与模版保持一致
            if(summary.getEdocType() == com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_REC 
                    && Strings.isNotBlank(summary.getDocMark())){
                //收文，保持原有的文号
            }else {
                summary.setDocMark(srcTempSummary.getDocMark());
            }
            
          //份数
            if(summary.getEdocType() == com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_REC 
                    && summary.getCopies() != null && summary.getCopies() != 0){
                //收文，保持原有的文号
            }else{
                summary.setCopies(srcTempSummary.getCopies());
            }
            
            //紧急程度
            if(summary.getEdocType() == com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_REC 
                    && Strings.isNotBlank(summary.getUrgentLevel())){
                //收文，保持原有的文号
            }else{
                summary.setUrgentLevel(srcTempSummary.getUrgentLevel());
            }
            
            //密级
            if(summary.getEdocType() == com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_REC 
                    && Strings.isNotBlank(summary.getSecretLevel())){
                //收文，保持原有的文号
            }else{
                summary.setSecretLevel(srcTempSummary.getSecretLevel());
            }
            
            summary.setDocMark2(srcTempSummary.getDocMark2());
            summary.setSerialNo(srcTempSummary.getSerialNo());
            
            
            Date createDate = null;
            if(body_orinal !=null){
                content=body_orinal.getContent();//获取正文ID，用来取原正文到新拟文单正文
                createDate = body_orinal.getCreateTime();
            }else{
                content = "";
            }
                
            cloneOriginalAtts=true; 
            if("1".equals(checkOption) || "0".equals(checkOption)){//正文转附件只取原文单标题,并将原有正文转为新公文附件
                 summary=new EdocSummary();//产生新的表单对象，只保留原公文标题
                 
                 
                summary.setArchiveId(srcTempSummary.getArchiveId());
                summary.setCanTrack(srcTempSummary.getCanTrack());
                summary.setDeadline(srcTempSummary.getDeadline());
                summary.setAdvanceRemind(srcTempSummary.getAdvanceRemind());
                ////GOV-5141 公文管理，收文转发文后调用模板，模板设置的预归档信息没有带入且预归档后的输入框为置灰状态，设置的跟踪信息也没有带入 end 
                
                 summary.setOrgAccountId(user.getLoginAccount());
                 summary.setEdocType(iEdocType);
                 if("0".equals(checkOption)){
                     summary.getEdocBodies().add(body_orinal); 
                 }
                 
                 summary.setSubject(subject);
                 summary.setCanTrack(1);
                 summary.setCreatePerson(user.getName());
                 if("1".equals(checkOption) && body_orinal.getContent()!=null){
                    //atts.add(edocSummaryManager.getAttsList(body, transmitSendNewEdocId, createDate, summary));
                    if(Constants.EDITOR_TYPE_OFFICE_EXCEL.equals(body_orinal.getContentType())
                            ||Constants.EDITOR_TYPE_OFFICE_WORD.equals(body_orinal.getContentType())
                            ||Constants.EDITOR_TYPE_WPS_WORD.equals(body_orinal.getContentType())
                            ||Constants.EDITOR_TYPE_WPS_EXCEL.equals(body_orinal.getContentType())){//非html正文都以附件形势转发
                        InputStream in = null;
                        try { 
                            //查找清除了痕迹的公文。
                            Long srcFileId=Long.parseLong(body_orinal.getContent());
                            if(transmitSendNewEdocId!=null&&!"".equals(transmitSendNewEdocId))
                            srcFileId=Long.parseLong(transmitSendNewEdocId);
                            String srcPath=fileManager.getFolder(createDate, true) + separator+ String.valueOf(srcFileId);
                            //1.解密文件
                            String newPath=CoderFactory.getInstance().decryptFileToTemp(srcPath);
                            //2.转换成标准正文
                            String newPathName = SystemEnvironment.getSystemTempFolder() + separator + String.valueOf(UUIDLong.longUUID());
                            Util.jinge2StandardOffice(newPath, newPathName);
                            //3.构造输入流
                            in = new FileInputStream(new File(newPathName)) ;
                            V3XFile f = fileManager.save(in, ApplicationCategoryEnum.edoc, summary.getSubject() + EdocUtil.getOfficeFileExt(body_orinal.getContentType()), createDate, false);
                            atts.add(new Attachment(f, ApplicationCategoryEnum.edoc, com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE));
                        }catch (Exception e) {
                            LOGGER.error("收文转发文错误 ", e);
                        }finally{
                            IOUtils.closeQuietly(in);
                        }
                    }else if(Constants.EDITOR_TYPE_PDF.equals(body_orinal.getContentType())){
                        String srcPath=fileManager.getFolder(createDate, true) + separator+ String.valueOf(body_orinal.getContent());
                        File srcFile = new File(srcPath);
                        if(srcFile != null && srcFile.exists() && srcFile.isFile()){
                        	InputStream in = new FileInputStream(srcFile) ;
                        	V3XFile f = fileManager.save(in, ApplicationCategoryEnum.edoc, summary.getSubject() + ".pdf", createDate, false);
                        	atts.add(new Attachment(f, ApplicationCategoryEnum.edoc, com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE));
                        }
                    }else
                    {//html正文，先生成文件             
                        V3XFile f =fileManager.save(body_orinal.getContent()==null?"":body_orinal.getContent(), ApplicationCategoryEnum.edoc, summary.getSubject()+".htm", createDate, false);              
                        atts.add(new Attachment(f, ApplicationCategoryEnum.edoc, com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE));
                    }
                }//将原有正文生成新公文附件 
            }else if("3".equals(checkOption)){
                 int receive=1;
                 modelAndView.addObject("ireive", receive);
                 
                 //OA-80067 这行代码有问题
                 //defaultEdocForm = edocFormManager.getDefaultEdocForm(user.getLoginAccount(),receive);      
            }
            
          //GOV-5141 公文管理，收文转发文后调用模板，模板设置的预归档信息没有带入且预归档后的输入框为置灰状态，设置的跟踪信息也没有带入 start
            modelAndView.addObject("checkOption",checkOption);  
            
            if("0".equals(checkOption)){//GOV-4935 如果是转发为正文，收文原正文要保持
                body=body_orinal;
            }
        }
      //BUG20120725011863_G6_v1.0_徐州市元申软件有限公司_公文收文结束转发文，调用模板附件、正文、文单内容丢失-end

        //公文模板是否绑定了内部文号
        modelAndView.addObject("isBoundSerialNo",edocMarkDefinitionManager.getEdocMarkByTempleteId(Long.valueOf(templeteId), MarkCategory.serialNo)==null?false:true);
    }

}
