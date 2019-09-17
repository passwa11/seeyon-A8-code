package com.seeyon.v3x.edoc.controller.newedoc;

import static java.io.File.separator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.edoc.enums.EdocEnum.MarkCategory;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.encrypt.CoderFactory;
import com.seeyon.ctp.common.filemanager.manager.Util;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.edoc.util.NewEdocHelper;

public class ForwordtosendEdocImpl extends NewEdocHandle{

    private static final Log log = LogFactory.getLog(ForwordtosendEdocImpl.class);
    
    @Override
    public void createEdocSummary(HttpServletRequest request, ModelAndView modelAndView) throws Exception{
        String strEdocId=request.getParameter("edocId");//strEdocId变量获取需要处理的表单 
        //如果是从待登记中进行转发文，会得到签收id
        String recieveId = request.getParameter("recieveId"); 
        if(Strings.isNotBlank(recieveId)){
            modelAndView.addObject("recieveId", recieveId);
        }
        String forwordType = request.getParameter("forwordType");
        if(Strings.isNotBlank(forwordType)){
            modelAndView.addObject("forwordType", forwordType);
        }
        
        checkOption=request.getParameter("checkOption");
        if(Strings.isNotBlank(checkOption)){
            modelAndView.addObject("checkOption", checkOption);
        }
        /******** puyc 添加  关联收文的路径 *******/
        //GOV-4782 收文管理-待办、待阅-转发文-新发文关联收文，点击发文关联收文，查看属性，内容为空，详见附件。
        String recAffairId = request.getParameter("recAffairId");
        //在拟文页面中设置收文affairId
        modelAndView.addObject("forwordtosend_recAffairId", recAffairId);
        String relationUrl = NewEdocHelper.relationReceive(strEdocId, "1")+"&affairId="+recAffairId;
        modelAndView.addObject("relationUrl", relationUrl);
        /******** puyc 添加 结束 *******/
        Long edocId=0L;
        modelAndView.addObject("forwordtosend", "1");//正文转附件后，第一次点击不提示正文已被修改
        String transmitSendNewEdocId=request.getParameter("transmitSendNewEdocId");
        if(strEdocId!=null && !"".equals(strEdocId)){edocId=Long.parseLong(strEdocId);}
        Long orgAccountId=user.getLoginAccount();
        body = null;
        summary=edocManager.getEdocSummaryById(edocId,true); //获取Summary对象
        if(summary == null) {//如果来自待分发，传递的是登记单id
            edocRegister = this.edocRegisterManager.getEdocRegister(edocId);
            if(edocRegister != null) {
                summary = new EdocSummary();
                BeanUtils.copyProperties(summary, edocRegister);
                registerBody = this.edocRegisterManager.findRegisterBodyByRegisterId(edocId);
                if(registerBody != null) {
                    body = new EdocBody();
                    body.setContent(registerBody.getContent());
                    body.setContentType(registerBody.getContentType());
                    body.setCreateTime(registerBody.getCreateTime());
                    summary.setEdocBodies(new HashSet<EdocBody>());
                    summary.getEdocBodies().add(body);
                }
            }
        }
        atts = NewEdocHelper.excludeType2ToNewAttachmentList(summary);//获取所有附件
        summary=NewEdocHelper.cloneNewSummaryAndSetProperties(user.getName(), summary, orgAccountId,iEdocType);//克隆summary对象
        //summary.setId(UUIDLong.longUUID());//以上操作将edocRegister的id赋值给summary的id，导致分发id与登记id一样
      //  summary.setFormId(defaultEdocForm.getId());//元素绑定表单
        summary.setEdocType(EdocEnum.edocType.recEdoc.ordinal());
        String subject=summary.getSubject();//获取原有标题
        body=summary.getFirstBody();//获取正文
        Date createDate = null;
        if(body !=null){
            content=body.getContent();//获取正文ID，用来取原正文到新拟文单正文
            createDate = body.getCreateTime();
        }else{
            content = "";
        }
            
        cloneOriginalAtts=true;
        if("1".equals(checkOption) || "0".equals(checkOption)){//正文转附件只取原文单标题,并将原有正文转为新公文附件
             summary=new EdocSummary();//产生新的表单对象，只保留原公文标题
             summary.setOrgAccountId(user.getLoginAccount());
             summary.setEdocType(iEdocType);
             
             //GOV-4946 收文转发文，勾选正文作为新公文的附件，文单中的内容没有带入，标题都是为空的，见附件。
             //GOV-4751 收文转发文时选择将收文的正文作为新发文的附件，结果查看新发文的正文，也显示成收文正文的内容了（正常应该显示为空，原收文的正文以附件形式显示）。
             if("0".equals(checkOption)){
                 summary.getEdocBodies().add(body);
             }
             summary.setSubject(subject);
             
             //转发文的时候，标题有回车显示有问题
             String attSubject = subject.replaceAll("\r\n", "").replaceAll("\n", "").replaceAll("\r", "");
             
             summary.setCanTrack(1);
             summary.setCreatePerson(user.getName());
             if("1".equals(checkOption)){
                //atts.add(edocSummaryManager.getAttsList(body, transmitSendNewEdocId, createDate, summary));
                if((Constants.EDITOR_TYPE_OFFICE_EXCEL.equals(body.getContentType())
                        ||Constants.EDITOR_TYPE_OFFICE_WORD.equals(body.getContentType())
                        ||Constants.EDITOR_TYPE_WPS_WORD.equals(body.getContentType())
                        ||Constants.EDITOR_TYPE_WPS_EXCEL.equals(body.getContentType()))&&body.getContent()!=null){//非html正文都以附件形势转发
                    InputStream in = null;
                    try { 
                        //查找清除了痕迹的公文。
                        Long srcFileId=Long.parseLong(body.getContent());
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
                        //GOV-4751 收文转发文时选择将收文的正文作为新发文的附件，结果查看新发文的正文，也显示成收文正文的内容了（正常应该显示为空，原收文的正文以附件形式显示）。
                        V3XFile f = fileManager.save(in, ApplicationCategoryEnum.edoc, attSubject + EdocUtil.getOfficeFileExt(body.getContentType()), createDate, false);
                        atts.add(new Attachment(f, ApplicationCategoryEnum.edoc, com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE));
                    }catch (Exception e) {
                        log.error("收文转发文错误 ", e);
                    }finally{
                        IOUtils.closeQuietly(in);
                    }
                }else if(Constants.EDITOR_TYPE_PDF.equals(body.getContentType())&&body.getContent()!=null){
                    String srcPath=fileManager.getFolder(createDate, true) + separator+ String.valueOf(body.getContent());
                    File srcFile = new File(srcPath);
                    if(srcFile.exists() && srcFile.isFile()){
                    	InputStream in = new FileInputStream(srcFile) ;
                    	//GOV-4751 收文转发文时选择将收文的正文作为新发文的附件，结果查看新发文的正文，也显示成收文正文的内容了（正常应该显示为空，原收文的正文以附件形式显示）。
                    	V3XFile f = fileManager.save(in, ApplicationCategoryEnum.edoc, attSubject + ".pdf", createDate, false);
                    	atts.add(new Attachment(f, ApplicationCategoryEnum.edoc, com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE));
                    }else{
                    	log.error("公文正文的文件不存在："+srcFile);
                    }
                }else
                {//html正文，先生成文件         
                    //GOV-4751 收文转发文时选择将收文的正文作为新发文的附件，结果查看新发文的正文，也显示成收文正文的内容了（正常应该显示为空，原收文的正文以附件形式显示）。
                    V3XFile f =fileManager.save(body.getContent()==null?"":body.getContent(), ApplicationCategoryEnum.edoc, attSubject+".htm", createDate, false);             
                    atts.add(new Attachment(f, ApplicationCategoryEnum.edoc, com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE));
                }
            }//将原有正文生成新公文附件 
        }else if("3".equals(checkOption)){
             int receive=1;
             modelAndView.addObject("ireive", receive);
             defaultEdocForm = edocFormManager.getDefaultEdocForm(user.getLoginAccount(),receive);      
        }
        //公文模板是否绑定了内部文号
        if (Strings.isNotBlank(templeteId)) {
            modelAndView.addObject("isBoundSerialNo",edocMarkDefinitionManager.getEdocMarkByTempleteId(Long.valueOf(templeteId), MarkCategory.serialNo)==null?false:true);
        }
        
    }
    

}
