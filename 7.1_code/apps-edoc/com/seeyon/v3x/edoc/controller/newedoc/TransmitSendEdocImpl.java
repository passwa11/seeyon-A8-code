package com.seeyon.v3x.edoc.controller.newedoc;

import static java.io.File.separator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.encrypt.CoderFactory;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.filemanager.manager.Util;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.util.EdocUtil;

public class TransmitSendEdocImpl extends NewEdocHandle{
    private static final Log log = LogFactory.getLog(TransmitSendEdocImpl.class);

	@Override
    public void createEdocSummary(HttpServletRequest request, ModelAndView modelAndView) throws Exception {
        String strEdocId=request.getParameter("edocId");
        Long edocId=0L;
        if(strEdocId!=null && !"".equals(strEdocId)){edocId=Long.parseLong(strEdocId);}
        summary=edocManager.getEdocSummaryById(edocId,true);          
        
        //设置affair中forwardMember值为被转发的公文的startMemberId
        long forwardMemberId = summary.getStartUserId();
        modelAndView.addObject("forwardMember", forwardMemberId);
        //forwardMemberId设置到newEdoc隐藏域中
        //还要考虑到，转发后，调用模板后newEdoc页面中依然有forwardMemberId值
        
        atts = attachmentManager.getByReference(summary.getId(), summary.getId());
//        canDeleteOriginalAtts = false;
        //OA-30740 待办中进行转发，原正文即作为了新发文的附件，该附件应该是可删除的才对，即后面有×，目前是没有
        canDeleteOriginalAtts = true;
        cloneOriginalAtts=true;         
        body=summary.getFirstBody(); 
        Date createDate = body.getCreateTime();
        FileManager fileManager = (FileManager)AppContext.getBean("fileManager");
        if(Constants.EDITOR_TYPE_OFFICE_EXCEL.equals(body.getContentType())
                ||Constants.EDITOR_TYPE_OFFICE_WORD.equals(body.getContentType())
                ||Constants.EDITOR_TYPE_WPS_WORD.equals(body.getContentType())
                ||Constants.EDITOR_TYPE_WPS_EXCEL.equals(body.getContentType())){//非html正文都以附件形势转发
            InputStream in = null;
            try { 
                //查找清除了痕迹的公文。
                Long srcFileId=Long.parseLong(body.getContent());
              //经常会出现edoc_body中的update_date
                V3XFile v3xFile = fileManager.getV3XFile(srcFileId);
                if(v3xFile != null) {
                	if(v3xFile.getUpdateDate() != null) {
                		createDate = v3xFile.getUpdateDate();
                	}
                }
                //这里注释了,不然在处理公文时，点转发文有问题，会找不到公文的正文文件
//                if(transmitSendNewEdocId!=null&&!"".equals(transmitSendNewEdocId))
//                    srcFileId=Long.parseLong(transmitSendNewEdocId);
                String srcPath=fileManager.getFolder(createDate, true) + separator+ String.valueOf(srcFileId);
                //1.解密文件
                String newPath=CoderFactory.getInstance().decryptFileToTemp(srcPath);
                //2.转换成标准正文
                String newPathName = SystemEnvironment.getSystemTempFolder() + separator + String.valueOf(UUIDLong.longUUID());
                Util.jinge2StandardOffice(newPath, newPathName);
                //3.构造输入流
                in = new FileInputStream(new File(newPathName)) ;
                V3XFile f = fileManager.save(in, ApplicationCategoryEnum.edoc, summary.getSubject() + EdocUtil.getOfficeFileExt(body.getContentType()), createDate, false);
                atts.add(new Attachment(f, ApplicationCategoryEnum.edoc, com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE));
            }catch (Exception e) {
                log.error("收文转发文错误 ", e);
            }finally{
                IOUtils.closeQuietly(in);
            }
        }else if(Constants.EDITOR_TYPE_PDF.equals(body.getContentType())){
            String srcPath=fileManager.getFolder(createDate, true) + separator+ String.valueOf(body.getContent());
            InputStream in = new FileInputStream(new File(srcPath)) ;
            V3XFile f = fileManager.save(in, ApplicationCategoryEnum.edoc, summary.getSubject() + ".pdf", createDate, false);
            atts.add(new Attachment(f, ApplicationCategoryEnum.edoc, com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE));
        }else
        {//html正文，先生成文件             
            V3XFile f =fileManager.save(body.getContent()==null?"":body.getContent(), ApplicationCategoryEnum.edoc, summary.getSubject()+".htm", createDate, false);                
            atts.add(new Attachment(f, ApplicationCategoryEnum.edoc, com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE));
        }
        summary =new EdocSummary();
        summary.setCreatePerson(user.getName());
        body = new EdocBody();
        bodyContentType=Constants.EDITOR_TYPE_OFFICE_WORD;
        if(com.seeyon.ctp.common.SystemEnvironment.hasPlugin("officeOcx")==false){bodyContentType=Constants.EDITOR_TYPE_HTML;}
        body.setContentType(bodyContentType);
        summary.getEdocBodies().add(body); 
        summary.setOrgAccountId(user.getLoginAccount());
        summary.setEdocType(EdocEnum.edocType.sendEdoc.ordinal());
        //转发时候，放入session，调用模版时候备用
        request.getSession().setAttribute("transmitSendAtts",atts);
    }
    

}
