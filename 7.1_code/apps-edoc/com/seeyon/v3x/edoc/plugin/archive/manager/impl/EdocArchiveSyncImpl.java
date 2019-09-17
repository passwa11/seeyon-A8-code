/**
 * Author : xuqw
 *   Date : 2015年5月22日 上午12:35:37
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.v3x.edoc.plugin.archive.manager.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.archive.bo.ArchiveDocBO;
import com.seeyon.apps.archive.manager.IArchiveSync;
import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocFormManager;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.util.FormParseExtInfo;
import com.seeyon.v3x.edoc.util.FormParseUtil;
import com.seeyon.v3x.edoc.util.XMLConverter;
import com.seeyon.v3x.system.signet.manager.V3xHtmDocumentSignatManager;

/**
 * <p>
 * Title : 应用模块名称
 * </p>
 * <p>
 * Description : 代码描述
 * </p>
 * <p>
 * Copyright : Copyright (c) 2012
 * </p>
 * <p>
 * Company : seeyon.com
 * </p>
 */
public class EdocArchiveSyncImpl implements IArchiveSync {
    
    private static final Log logger = LogFactory.getLog(EdocArchiveSyncImpl.class);
    
    private AffairManager affairManager = null;
    private EdocManager edocManager = null;
    private EdocFormManager edocFormManager = null;
    private TemplateManager templeteManager = null;
    private V3xHtmDocumentSignatManager htmSignetManager = null;
    private AttachmentManager attachmentManager = null;
    private XMLConverter xmlConverter = null;
    private FileManager fileManager = null;
    
    @Override
    public ApplicationCategoryEnum getAppCategory() {
        return ApplicationCategoryEnum.edoc;
    }

    @Override
    public String getTempFileFolderName() {

        return "formtemp";
    }

    @Override
    public void syncBefore(ArchiveDocBO vo) {

        logger.debug("EdocArchiveSyncImpl > syncBefore 开始执行档案归档处理...");
        
        Long summaryId = vo.getId();
        
        try {

            EdocSummary summary = FormParseUtil.getEdocSummary(summaryId);
            
            if(summary == null){
                logger.info("获取EdocSummary对象为空, ID=" + summaryId);
                return;
            }
            
            //处理文单信息
            String htmlContent = this.analysisFormContent(summary, vo);
            //文单
            vo.addBody("Edoc_FORM", MainbodyType.HTML, htmlContent);

        } catch (Exception e) {
            logger.error("公文档案归档解析异常, 事物ID=" + vo.getId(), e);
        }
    }

    @Override
    public void syncAfter(ArchiveDocBO vo) {

        // TODO
    }

    @Override
    public void checkRules(Collection<Long> ids, CheckMessage msgOjb) {
        
        if(Strings.isNotEmpty(ids)){
            
            StringBuilder unfinishedInfo = new StringBuilder();
            for(Long id : ids) {
                EdocSummary summary;
                try {
                    
                    summary = FormParseUtil.getEdocSummary(id);
                    if(summary != null){
                        int state = summary.getState();
                      
                        //ColConstant.flowState.run.ordinal()
                        if(state != EdocConstant.flowState.finish.ordinal() && state != EdocConstant.flowState.terminate.ordinal()){
                          
                            //《XX》流程尚未走完
                            unfinishedInfo.append("《");
                            unfinishedInfo.append(summary.getSubject());
                            unfinishedInfo.append("》");
                        }
                    }
                } catch (BusinessException e) {
                    logger.error("归档检查Summary状态错误，获取EdocSummary失败，　ID=" + id, e);
                }
            }
            if(unfinishedInfo.length() > 0){
                String msg = ResourceUtil.getString("edoc.alert.archive.flowUnfinished", unfinishedInfo.toString().replaceAll("\r\n", "").replaceAll("\r", "").replaceAll("\n", ""));
                msgOjb.addConfirmMessage(msg);
            }
        }
    }
    
    @Override
    public String getHtmlTempletePath() {
        return "apps_res/edoc/file/archive/edocArchiveHTMLTemplete.html";
    }
    
    /**
     * 解析文单内容
     * @Author      : xuqw
     * @Date        : 2015年5月22日上午1:05:02
     * @param edocSummary
     * @return
     * @throws BusinessException
     */
    private String analysisFormContent(EdocSummary summary, ArchiveDocBO vo) throws BusinessException{
        
        
        //正文信息
        EdocBody eBody = summary.getFirstBody();
        if("gd".equalsIgnoreCase(eBody.getContentType())){
            //GD正文
            String fId = eBody.getContent();
            if(Strings.isDigits(fId)){
                long fileId = Long.parseLong(fId);
                V3XFile vFile = fileManager.getV3XFile(fileId);
                vo.addBody(vFile, "gd");
            }
            
        }else{
            int bodyType = EdocHelper.getBodyTypeKey(eBody.getContentType());
            if(bodyType == MainbodyType.HTML.getKey()){
                //Office正文
                vo.addBody("Content", eBody.getContent());//作为内容放在HTML里面
            }else {
                vo.addTempFile("common/css/dd.css");
                vo.addTempFile("common/js/orgIndex/token-input.css");
                //HTML正文
                vo.addBody("Content", MainbodyType.getEnumByKey(bodyType), eBody.getContent());
            }
        }
        
        //解析文单信息
        FormParseExtInfo extInfo = FormParseUtil.formatFormContent(summary, vo.getTempFolderName());
        
        vo.addAllAttachment(extInfo.getAttFiles());//全部附件
        
        Set<String> files = extInfo.getFiles();
        for(String file : files){
            vo.addTempFile(file);
        }
        
        List<V3XFile> ctpFiles = extInfo.getCtpFiles();
        for(V3XFile ctpFile : ctpFiles){
            vo.addTempFile(ctpFile);
        }
        
        Map<String, byte[]> byteFiles = extInfo.getByteFile();
        Iterator<String> bFileNames = byteFiles.keySet().iterator();
        while(bFileNames.hasNext()){
            String name = bFileNames.next();
            byte[] bFile = byteFiles.get(name);
            
            vo.addTempFile(name, bFile);
        }
        
        Map<String, String[]> field2ValueMap = extInfo.getField2ValueMap();
        //获取发起人的登陆id
        if(summary.getStartUserId()!=null){
            V3xOrgMember startMember = OrgHelper.getMember(summary.getStartUserId());
            if (startMember != null) {
                field2ValueMap.put("start_user_login_name", new String[]{startMember.getLoginName()});
            }
        }
        //解析字段
        setExtendInfo(field2ValueMap, vo);
        return extInfo.getContent();
    }
    
    
    
    /**
     * 档案集成， 需要把公文的相关字段写入到XML里面
     * @Author      : xuqw
     * @Date        : 2015年5月26日下午7:44:05
     * @param vo
     */
    private void setExtendInfo(Map<String, String[]> field2ValueMap, ArchiveDocBO vo) {

        vo.addExtDesc("subject", field2ValueMap.get("subject") == null ? "" : field2ValueMap.get("subject")[1]); // 公文标题
        vo.addExtDesc("start_user_login_name", field2ValueMap.get("start_user_login_name") == null ? "" : field2ValueMap.get("start_user_login_name")[0]); //获取发起人的登陆名
        vo.addExtDesc("attachments", field2ValueMap.get("attachments") == null ? ""
                : field2ValueMap.get("attachments")[1]); // 附件
        vo.addExtDesc("auditor", field2ValueMap.get("auditor") == null ? "" : field2ValueMap.get("auditor")[1]); // 审核人
        //vo.addExtDesc("banli", field2ValueMap.get("banli") == null ? "" : field2ValueMap.get("banli")[1]); // 办理
        //vo.addExtDesc("chengban", field2ValueMap.get("chengban") == null ? "" : field2ValueMap.get("chengban")[1]); // 承办
        vo.addExtDesc("copies", field2ValueMap.get("copies") == null ? "" : field2ValueMap.get("copies")[1]); // 印发份数
        vo.addExtDesc("copies2", field2ValueMap.get("copies2") == null ? "" : field2ValueMap.get("copies2")[1]); // 印发份数B
        vo.addExtDesc("copy_to", field2ValueMap.get("copy_to") == null ? "" : field2ValueMap.get("copy_to")[1]); // 抄送单位
        vo.addExtDesc("copy_to2", field2ValueMap.get("copy_to2") == null ? "" : field2ValueMap.get("copy_to2")[1]); // 抄送单位B
        vo.addExtDesc("create_person",
                field2ValueMap.get("create_person") == null ? "" : field2ValueMap.get("create_person")[1]); // 拟稿人
        vo.addExtDesc("createdate", field2ValueMap.get("createdate") == null ? "" : field2ValueMap.get("createdate")[1]); // 拟稿日期
        vo.addExtDesc("date1", field2ValueMap.get("date1") == null ? "" : field2ValueMap.get("date1")[1]); // 日期类型1
        vo.addExtDesc("date10", field2ValueMap.get("date10") == null ? "" : field2ValueMap.get("date10")[1]); // 日期类型10
        vo.addExtDesc("date11", field2ValueMap.get("date11") == null ? "" : field2ValueMap.get("date11")[1]); // 日期类型11
        vo.addExtDesc("date12", field2ValueMap.get("date12") == null ? "" : field2ValueMap.get("date12")[1]); // 日期类型12
        vo.addExtDesc("date13", field2ValueMap.get("date13") == null ? "" : field2ValueMap.get("date13")[1]); // 日期类型13
        vo.addExtDesc("date14", field2ValueMap.get("date14") == null ? "" : field2ValueMap.get("date14")[1]); // 日期类型14
        vo.addExtDesc("date15", field2ValueMap.get("date15") == null ? "" : field2ValueMap.get("date15")[1]); // 日期类型15
        vo.addExtDesc("date16", field2ValueMap.get("date16") == null ? "" : field2ValueMap.get("date16")[1]); // 日期类型16
        vo.addExtDesc("date17", field2ValueMap.get("date17") == null ? "" : field2ValueMap.get("date17")[1]); // 日期类型17
        vo.addExtDesc("date18", field2ValueMap.get("date18") == null ? "" : field2ValueMap.get("date18")[1]); // 日期类型18
        vo.addExtDesc("date19", field2ValueMap.get("date19") == null ? "" : field2ValueMap.get("date19")[1]); // 日期类型19
        vo.addExtDesc("date2", field2ValueMap.get("date2") == null ? "" : field2ValueMap.get("date2")[1]); // 日期类型2
        vo.addExtDesc("date20", field2ValueMap.get("date20") == null ? "" : field2ValueMap.get("date20")[1]); // 日期类型20
        vo.addExtDesc("date3", field2ValueMap.get("date3") == null ? "" : field2ValueMap.get("date3")[1]); // 日期类型3
        vo.addExtDesc("date4", field2ValueMap.get("date4") == null ? "" : field2ValueMap.get("date4")[1]); // 日期类型4
        vo.addExtDesc("date5", field2ValueMap.get("date5") == null ? "" : field2ValueMap.get("date5")[1]); // 日期类型5
        vo.addExtDesc("date6", field2ValueMap.get("date6") == null ? "" : field2ValueMap.get("date6")[1]); // 日期类型6
        vo.addExtDesc("date7", field2ValueMap.get("date7") == null ? "" : field2ValueMap.get("date7")[1]); // 日期类型7
        vo.addExtDesc("date8", field2ValueMap.get("date8") == null ? "" : field2ValueMap.get("date8")[1]); // 日期类型8
        vo.addExtDesc("date9", field2ValueMap.get("date9") == null ? "" : field2ValueMap.get("date9")[1]); // 日期类型9
        vo.addExtDesc("decimal1", field2ValueMap.get("decimal1") == null ? "" : field2ValueMap.get("decimal1")[1]); // 小数类型1
        vo.addExtDesc("decimal10", field2ValueMap.get("decimal10") == null ? "" : field2ValueMap.get("decimal10")[1]); // 小数类型10
        vo.addExtDesc("decimal11", field2ValueMap.get("decimal11") == null ? "" : field2ValueMap.get("decimal11")[1]); // 小数类型11
        vo.addExtDesc("decimal12", field2ValueMap.get("decimal12") == null ? "" : field2ValueMap.get("decimal12")[1]); // 小数类型12
        vo.addExtDesc("decimal13", field2ValueMap.get("decimal13") == null ? "" : field2ValueMap.get("decimal13")[1]); // 小数类型13
        vo.addExtDesc("decimal14", field2ValueMap.get("decimal14") == null ? "" : field2ValueMap.get("decimal14")[1]); // 小数类型14
        vo.addExtDesc("decimal15", field2ValueMap.get("decimal15") == null ? "" : field2ValueMap.get("decimal15")[1]); // 小数类型15
        vo.addExtDesc("decimal16", field2ValueMap.get("decimal16") == null ? "" : field2ValueMap.get("decimal16")[1]); // 小数类型16
        vo.addExtDesc("decimal17", field2ValueMap.get("decimal17") == null ? "" : field2ValueMap.get("decimal17")[1]); // 小数类型17
        vo.addExtDesc("decimal18", field2ValueMap.get("decimal18") == null ? "" : field2ValueMap.get("decimal18")[1]); // 小数类型18
        vo.addExtDesc("decimal19", field2ValueMap.get("decimal19") == null ? "" : field2ValueMap.get("decimal19")[1]); // 小数类型19
        vo.addExtDesc("decimal2", field2ValueMap.get("decimal2") == null ? "" : field2ValueMap.get("decimal2")[1]); // 小数类型2
        vo.addExtDesc("decimal20", field2ValueMap.get("decimal20") == null ? "" : field2ValueMap.get("decimal20")[1]); // 小数类型20
        vo.addExtDesc("decimal3", field2ValueMap.get("decimal3") == null ? "" : field2ValueMap.get("decimal3")[1]); // 小数类型3
        vo.addExtDesc("decimal4", field2ValueMap.get("decimal4") == null ? "" : field2ValueMap.get("decimal4")[1]); // 小数类型4
        vo.addExtDesc("decimal5", field2ValueMap.get("decimal5") == null ? "" : field2ValueMap.get("decimal5")[1]); // 小数类型5
        vo.addExtDesc("decimal6", field2ValueMap.get("decimal6") == null ? "" : field2ValueMap.get("decimal6")[1]); // 小数类型6
        vo.addExtDesc("decimal7", field2ValueMap.get("decimal7") == null ? "" : field2ValueMap.get("decimal7")[1]); // 小数类型7
        vo.addExtDesc("decimal8", field2ValueMap.get("decimal8") == null ? "" : field2ValueMap.get("decimal8")[1]); // 小数类型8
        vo.addExtDesc("decimal9", field2ValueMap.get("decimal9") == null ? "" : field2ValueMap.get("decimal9")[1]); // 小数类型9
        //vo.addExtDesc("dengji", field2ValueMap.get("dengji") == null ? "" : field2ValueMap.get("dengji")[1]); // 分发
        vo.addExtDesc("doc_mark", field2ValueMap.get("doc_mark") == null ? "" : field2ValueMap.get("doc_mark")[1]); // 公文文号
        vo.addExtDesc("doc_mark2", field2ValueMap.get("doc_mark2") == null ? "" : field2ValueMap.get("doc_mark2")[1]); // 公文文号B
        vo.addExtDesc("doc_type", field2ValueMap.get("doc_type") == null ? "" : field2ValueMap.get("doc_type")[1]); // 公文种类
        //vo.addExtDesc("feedback", field2ValueMap.get("feedback") == null ? "" : field2ValueMap.get("feedback")[1]); // 下级单位意见反馈
        //vo.addExtDesc("fengfa", field2ValueMap.get("fengfa") == null ? "" : field2ValueMap.get("fengfa")[1]); // 封发
        vo.addExtDesc("filefz", field2ValueMap.get("filefz") == null ? "" : field2ValueMap.get("filefz")[1]); // 附注
        vo.addExtDesc("filesm", field2ValueMap.get("filesm") == null ? "" : field2ValueMap.get("filesm")[1]); // 附件说明
        //vo.addExtDesc("fuhe", field2ValueMap.get("fuhe") == null ? "" : field2ValueMap.get("fuhe")[1]); // 复核
        //vo.addExtDesc("huiqian", field2ValueMap.get("huiqian") == null ? "" : field2ValueMap.get("huiqian")[1]); // 会签
        vo.addExtDesc("integer1", field2ValueMap.get("integer1") == null ? "" : field2ValueMap.get("integer1")[1]); // 整数类型1
        vo.addExtDesc("integer10", field2ValueMap.get("integer10") == null ? "" : field2ValueMap.get("integer10")[1]); // 整数类型10
        vo.addExtDesc("integer11", field2ValueMap.get("integer11") == null ? "" : field2ValueMap.get("integer11")[1]); // 整数类型11
        vo.addExtDesc("integer12", field2ValueMap.get("integer12") == null ? "" : field2ValueMap.get("integer12")[1]); // 整数类型12
        vo.addExtDesc("integer13", field2ValueMap.get("integer13") == null ? "" : field2ValueMap.get("integer13")[1]); // 整数类型13
        vo.addExtDesc("integer14", field2ValueMap.get("integer14") == null ? "" : field2ValueMap.get("integer14")[1]); // 整数类型14
        vo.addExtDesc("integer15", field2ValueMap.get("integer15") == null ? "" : field2ValueMap.get("integer15")[1]); // 整数类型15
        vo.addExtDesc("integer16", field2ValueMap.get("integer16") == null ? "" : field2ValueMap.get("integer16")[1]); // 整数类型16
        vo.addExtDesc("integer17", field2ValueMap.get("integer17") == null ? "" : field2ValueMap.get("integer17")[1]); // 整数类型17
        vo.addExtDesc("integer18", field2ValueMap.get("integer18") == null ? "" : field2ValueMap.get("integer18")[1]); // 整数类型18
        vo.addExtDesc("integer19", field2ValueMap.get("integer19") == null ? "" : field2ValueMap.get("integer19")[1]); // 整数类型19
        vo.addExtDesc("integer2", field2ValueMap.get("integer2") == null ? "" : field2ValueMap.get("integer2")[1]); // 整数类型2
        vo.addExtDesc("integer20", field2ValueMap.get("integer20") == null ? "" : field2ValueMap.get("integer20")[1]); // 整数类型20
        vo.addExtDesc("integer3", field2ValueMap.get("integer3") == null ? "" : field2ValueMap.get("integer3")[1]); // 整数类型3
        vo.addExtDesc("integer4", field2ValueMap.get("integer4") == null ? "" : field2ValueMap.get("integer4")[1]); // 整数类型4
        vo.addExtDesc("integer5", field2ValueMap.get("integer5") == null ? "" : field2ValueMap.get("integer5")[1]); // 整数类型5
        vo.addExtDesc("integer6", field2ValueMap.get("integer6") == null ? "" : field2ValueMap.get("integer6")[1]); // 整数类型6
        vo.addExtDesc("integer7", field2ValueMap.get("integer7") == null ? "" : field2ValueMap.get("integer7")[1]); // 整数类型7
        vo.addExtDesc("integer8", field2ValueMap.get("integer8") == null ? "" : field2ValueMap.get("integer8")[1]); // 整数类型8
        vo.addExtDesc("integer9", field2ValueMap.get("integer9") == null ? "" : field2ValueMap.get("integer9")[1]); // 整数类型9
        vo.addExtDesc("issuer", field2ValueMap.get("issuer") == null ? "" : field2ValueMap.get("issuer")[1]); // 签发人
        vo.addExtDesc("keep_period", field2ValueMap.get("keep_period") == null ? ""
                : field2ValueMap.get("keep_period")[1]); // 保密期限
        vo.addExtDesc("keyword", field2ValueMap.get("keyword") == null ? "" : field2ValueMap.get("keyword")[1]); // 主题词
        vo.addExtDesc("list1", field2ValueMap.get("list1") == null ? "" : field2ValueMap.get("list1")[1]); // 列表类型1
        vo.addExtDesc("list10", field2ValueMap.get("list10") == null ? "" : field2ValueMap.get("list10")[1]); // 列表类型10
        vo.addExtDesc("list11", field2ValueMap.get("list11") == null ? "" : field2ValueMap.get("list11")[1]); // 列表类型11
        vo.addExtDesc("list12", field2ValueMap.get("list12") == null ? "" : field2ValueMap.get("list12")[1]); // 列表类型12
        vo.addExtDesc("list13", field2ValueMap.get("list13") == null ? "" : field2ValueMap.get("list13")[1]); // 列表类型13
        vo.addExtDesc("list14", field2ValueMap.get("list14") == null ? "" : field2ValueMap.get("list14")[1]); // 列表类型14
        vo.addExtDesc("list15", field2ValueMap.get("list15") == null ? "" : field2ValueMap.get("list15")[1]); // 列表类型15
        vo.addExtDesc("list16", field2ValueMap.get("list16") == null ? "" : field2ValueMap.get("list16")[1]); // 列表类型16
        vo.addExtDesc("list17", field2ValueMap.get("list17") == null ? "" : field2ValueMap.get("list17")[1]); // 列表类型17
        vo.addExtDesc("list18", field2ValueMap.get("list18") == null ? "" : field2ValueMap.get("list18")[1]); // 列表类型18
        vo.addExtDesc("list19", field2ValueMap.get("list19") == null ? "" : field2ValueMap.get("list19")[1]); // 列表类型19
        vo.addExtDesc("list2", field2ValueMap.get("list2") == null ? "" : field2ValueMap.get("list2")[1]); // 列表类型2
        vo.addExtDesc("list20", field2ValueMap.get("list20") == null ? "" : field2ValueMap.get("list20")[1]); // 列表类型20
        vo.addExtDesc("list3", field2ValueMap.get("list3") == null ? "" : field2ValueMap.get("list3")[1]); // 列表类型3
        vo.addExtDesc("list4", field2ValueMap.get("list4") == null ? "" : field2ValueMap.get("list4")[1]); // 列表类型4
        vo.addExtDesc("list5", field2ValueMap.get("list5") == null ? "" : field2ValueMap.get("list5")[1]); // 列表类型5
        vo.addExtDesc("list6", field2ValueMap.get("list6") == null ? "" : field2ValueMap.get("list6")[1]); // 列表类型6
        vo.addExtDesc("list7", field2ValueMap.get("list7") == null ? "" : field2ValueMap.get("list7")[1]); // 列表类型7
        vo.addExtDesc("list8", field2ValueMap.get("list8") == null ? "" : field2ValueMap.get("list8")[1]); // 列表类型8
        vo.addExtDesc("list9", field2ValueMap.get("list9") == null ? "" : field2ValueMap.get("list9")[1]); // 列表类型9
        //vo.addExtDesc("niban", field2ValueMap.get("niban") == null ? "" : field2ValueMap.get("niban")[1]); // 拟办
        /*vo.addExtDesc("niwen", field2ValueMap.get("niwen") == null ? "" : field2ValueMap.get("niwen")[1]); // 拟文
        vo.addExtDesc("opinion1", field2ValueMap.get("opinion1") == null ? "" : field2ValueMap.get("opinion1")[1]); // 自定义意见1
        vo.addExtDesc("opinion10", field2ValueMap.get("opinion10") == null ? "" : field2ValueMap.get("opinion10")[1]); // 自定义意见10
        vo.addExtDesc("opinion11", field2ValueMap.get("opinion11") == null ? "" : field2ValueMap.get("opinion11")[1]); // 自定义意见11
        vo.addExtDesc("opinion12", field2ValueMap.get("opinion12") == null ? "" : field2ValueMap.get("opinion12")[1]); // 自定义意见12
        vo.addExtDesc("opinion13", field2ValueMap.get("opinion13") == null ? "" : field2ValueMap.get("opinion13")[1]); // 自定义意见13
        vo.addExtDesc("opinion14", field2ValueMap.get("opinion14") == null ? "" : field2ValueMap.get("opinion14")[1]); // 自定义意见14
        vo.addExtDesc("opinion15", field2ValueMap.get("opinion15") == null ? "" : field2ValueMap.get("opinion15")[1]); // 自定义意见15
        vo.addExtDesc("opinion16", field2ValueMap.get("opinion16") == null ? "" : field2ValueMap.get("opinion16")[1]); // 自定义意见16
        vo.addExtDesc("opinion17", field2ValueMap.get("opinion17") == null ? "" : field2ValueMap.get("opinion17")[1]); // 自定义意见17
        vo.addExtDesc("opinion18", field2ValueMap.get("opinion18") == null ? "" : field2ValueMap.get("opinion18")[1]); // 自定义意见18
        vo.addExtDesc("opinion19", field2ValueMap.get("opinion19") == null ? "" : field2ValueMap.get("opinion19")[1]); // 自定义意见19
        vo.addExtDesc("opinion2", field2ValueMap.get("opinion2") == null ? "" : field2ValueMap.get("opinion2")[1]); // 自定义意见2
        vo.addExtDesc("opinion20", field2ValueMap.get("opinion20") == null ? "" : field2ValueMap.get("opinion20")[1]); // 自定义意见20
        vo.addExtDesc("opinion21", field2ValueMap.get("opinion21") == null ? "" : field2ValueMap.get("opinion21")[1]); // 自定义意见21
        vo.addExtDesc("opinion22", field2ValueMap.get("opinion22") == null ? "" : field2ValueMap.get("opinion22")[1]); // 自定义意见22
        vo.addExtDesc("opinion23", field2ValueMap.get("opinion23") == null ? "" : field2ValueMap.get("opinion23")[1]); // 自定义意见23
        vo.addExtDesc("opinion24", field2ValueMap.get("opinion24") == null ? "" : field2ValueMap.get("opinion24")[1]); // 自定义意见24
        vo.addExtDesc("opinion25", field2ValueMap.get("opinion25") == null ? "" : field2ValueMap.get("opinion25")[1]); // 自定义意见25
        vo.addExtDesc("opinion26", field2ValueMap.get("opinion26") == null ? "" : field2ValueMap.get("opinion26")[1]); // 自定义意见26
        vo.addExtDesc("opinion27", field2ValueMap.get("opinion27") == null ? "" : field2ValueMap.get("opinion27")[1]); // 自定义意见27
        vo.addExtDesc("opinion28", field2ValueMap.get("opinion28") == null ? "" : field2ValueMap.get("opinion28")[1]); // 自定义意见28
        vo.addExtDesc("opinion29", field2ValueMap.get("opinion29") == null ? "" : field2ValueMap.get("opinion29")[1]); // 自定义意见29
        vo.addExtDesc("opinion3", field2ValueMap.get("opinion3") == null ? "" : field2ValueMap.get("opinion3")[1]); // 自定义意见3
        vo.addExtDesc("opinion30", field2ValueMap.get("opinion30") == null ? "" : field2ValueMap.get("opinion30")[1]); // 自定义意见30
        vo.addExtDesc("opinion4", field2ValueMap.get("opinion4") == null ? "" : field2ValueMap.get("opinion4")[1]); // 自定义意见4
        vo.addExtDesc("opinion5", field2ValueMap.get("opinion5") == null ? "" : field2ValueMap.get("opinion5")[1]); // 自定义意见5
        vo.addExtDesc("opinion6", field2ValueMap.get("opinion6") == null ? "" : field2ValueMap.get("opinion6")[1]); // 自定义意见6
        vo.addExtDesc("opinion7", field2ValueMap.get("opinion7") == null ? "" : field2ValueMap.get("opinion7")[1]); // 自定义意见7
        vo.addExtDesc("opinion8", field2ValueMap.get("opinion8") == null ? "" : field2ValueMap.get("opinion8")[1]); // 自定义意见8
        vo.addExtDesc("opinion9", field2ValueMap.get("opinion9") == null ? "" : field2ValueMap.get("opinion9")[1]); // 自定义意见9
*/        vo.addExtDesc("packdate", field2ValueMap.get("packdate") == null ? "" : field2ValueMap.get("packdate")[1]); // 封发日期
        vo.addExtDesc("phone", field2ValueMap.get("phone") == null ? "" : field2ValueMap.get("phone")[1]); // 联系电话
        //vo.addExtDesc("pishi", field2ValueMap.get("pishi") == null ? "" : field2ValueMap.get("pishi")[1]); // 批示
        vo.addExtDesc("print_unit", field2ValueMap.get("print_unit") == null ? "" : field2ValueMap.get("print_unit")[1]); // 印发单位
        vo.addExtDesc("printer", field2ValueMap.get("printer") == null ? "" : field2ValueMap.get("printer")[1]); // 打印人
        //vo.addExtDesc("qianfa", field2ValueMap.get("qianfa") == null ? "" : field2ValueMap.get("qianfa")[1]); // 签发
        vo.addExtDesc("receipt_date",
                field2ValueMap.get("receipt_date") == null ? "" : field2ValueMap.get("receipt_date")[1]); // 签收日期
        vo.addExtDesc("registration_date",
                field2ValueMap.get("registration_date") == null ? "" : field2ValueMap.get("registration_date")[1]); // 登记日期
        //vo.addExtDesc("report", field2ValueMap.get("report") == null ? "" : field2ValueMap.get("report")[1]); // 上级单位意见汇报
        vo.addExtDesc("report_to", field2ValueMap.get("report_to") == null ? "" : field2ValueMap.get("report_to")[1]); // 抄报单位
        vo.addExtDesc("report_to2", field2ValueMap.get("report_to2") == null ? "" : field2ValueMap.get("report_to2")[1]); // 抄报单位B
        vo.addExtDesc("review", field2ValueMap.get("review") == null ? "" : field2ValueMap.get("review")[1]); // 复核人
        vo.addExtDesc("secret_level",
                field2ValueMap.get("secret_level") == null ? "" : field2ValueMap.get("secret_level")[1]); // 文件密级
        vo.addExtDesc("send_department",
                field2ValueMap.get("send_department") == null ? "" : field2ValueMap.get("send_department")[1]); // 发文部门
        vo.addExtDesc("send_department2",
                field2ValueMap.get("send_department2") == null ? "" : field2ValueMap.get("send_department2")[1]); // 发文部门B
        vo.addExtDesc("send_to", field2ValueMap.get("send_to") == null ? "" : field2ValueMap.get("send_to")[1]); // 主送单位
        vo.addExtDesc("send_to2", field2ValueMap.get("send_to2") == null ? "" : field2ValueMap.get("send_to2")[1]); // 主送单位B
        vo.addExtDesc("send_type", field2ValueMap.get("send_type") == null ? "" : field2ValueMap.get("send_type")[1]); // 行文类型
        vo.addExtDesc("send_unit", field2ValueMap.get("send_unit") == null ? "" : field2ValueMap.get("send_unit")[1]); // 发文单位
        vo.addExtDesc("send_unit2", field2ValueMap.get("send_unit2") == null ? "" : field2ValueMap.get("send_unit2")[1]); // 发文单位B
        vo.addExtDesc("serial_no", field2ValueMap.get("serial_no") == null ? "" : field2ValueMap.get("serial_no")[1]); // 内部文号
        //vo.addExtDesc("shenhe", field2ValueMap.get("shenhe") == null ? "" : field2ValueMap.get("shenhe")[1]); // 审核
        //vo.addExtDesc("shenpi", field2ValueMap.get("shenpi") == null ? "" : field2ValueMap.get("shenpi")[1]); // 审批
        vo.addExtDesc("signing_date",
                field2ValueMap.get("signing_date") == null ? "" : field2ValueMap.get("signing_date")[1]); // 签发日期
        vo.addExtDesc("string1", field2ValueMap.get("string1") == null ? "" : field2ValueMap.get("string1")[1]); // 单行文本1
        vo.addExtDesc("string10", field2ValueMap.get("string10") == null ? "" : field2ValueMap.get("string10")[1]); // 单行文本10
        vo.addExtDesc("string11", field2ValueMap.get("string11") == null ? "" : field2ValueMap.get("string11")[1]); // 单行文本11
        vo.addExtDesc("string12", field2ValueMap.get("string12") == null ? "" : field2ValueMap.get("string12")[1]); // 单行文本12
        vo.addExtDesc("string13", field2ValueMap.get("string13") == null ? "" : field2ValueMap.get("string13")[1]); // 单行文本13
        vo.addExtDesc("string14", field2ValueMap.get("string14") == null ? "" : field2ValueMap.get("string14")[1]); // 单行文本14
        vo.addExtDesc("string15", field2ValueMap.get("string15") == null ? "" : field2ValueMap.get("string15")[1]); // 单行文本15
        vo.addExtDesc("string16", field2ValueMap.get("string16") == null ? "" : field2ValueMap.get("string16")[1]); // 单行文本16
        vo.addExtDesc("string17", field2ValueMap.get("string17") == null ? "" : field2ValueMap.get("string17")[1]); // 单行文本17
        vo.addExtDesc("string18", field2ValueMap.get("string18") == null ? "" : field2ValueMap.get("string18")[1]); // 单行文本18
        vo.addExtDesc("string19", field2ValueMap.get("string19") == null ? "" : field2ValueMap.get("string19")[1]); // 单行文本19
        vo.addExtDesc("string2", field2ValueMap.get("string2") == null ? "" : field2ValueMap.get("string2")[1]); // 单行文本2
        vo.addExtDesc("string20", field2ValueMap.get("string20") == null ? "" : field2ValueMap.get("string20")[1]); // 单行文本20
        vo.addExtDesc("string21", field2ValueMap.get("string21") == null ? "" : field2ValueMap.get("string21")[1]); // 单行文本21
        vo.addExtDesc("string22", field2ValueMap.get("string22") == null ? "" : field2ValueMap.get("string22")[1]); // 单行文本22
        vo.addExtDesc("string23", field2ValueMap.get("string23") == null ? "" : field2ValueMap.get("string23")[1]); // 单行文本23
        vo.addExtDesc("string24", field2ValueMap.get("string24") == null ? "" : field2ValueMap.get("string24")[1]); // 单行文本24
        vo.addExtDesc("string25", field2ValueMap.get("string25") == null ? "" : field2ValueMap.get("string25")[1]); // 单行文本25
        vo.addExtDesc("string26", field2ValueMap.get("string26") == null ? "" : field2ValueMap.get("string26")[1]); // 单行文本26
        vo.addExtDesc("string27", field2ValueMap.get("string27") == null ? "" : field2ValueMap.get("string27")[1]); // 单行文本27
        vo.addExtDesc("string28", field2ValueMap.get("string28") == null ? "" : field2ValueMap.get("string28")[1]); // 单行文本28
        vo.addExtDesc("string29", field2ValueMap.get("string29") == null ? "" : field2ValueMap.get("string29")[1]); // 单行文本29
        vo.addExtDesc("string3", field2ValueMap.get("string3") == null ? "" : field2ValueMap.get("string3")[1]); // 单行文本3
        vo.addExtDesc("string30", field2ValueMap.get("string30") == null ? "" : field2ValueMap.get("string30")[1]); // 单行文本30
        vo.addExtDesc("string4", field2ValueMap.get("string4") == null ? "" : field2ValueMap.get("string4")[1]); // 单行文本4
        vo.addExtDesc("string5", field2ValueMap.get("string5") == null ? "" : field2ValueMap.get("string5")[1]); // 单行文本5
        vo.addExtDesc("string6", field2ValueMap.get("string6") == null ? "" : field2ValueMap.get("string6")[1]); // 单行文本6
        vo.addExtDesc("string7", field2ValueMap.get("string7") == null ? "" : field2ValueMap.get("string7")[1]); // 单行文本7
        vo.addExtDesc("string8", field2ValueMap.get("string8") == null ? "" : field2ValueMap.get("string8")[1]); // 单行文本8
        vo.addExtDesc("string9", field2ValueMap.get("string9") == null ? "" : field2ValueMap.get("string9")[1]); // 单行文本9
        vo.addExtDesc("text1", field2ValueMap.get("text1") == null ? "" : field2ValueMap.get("text1")[1]); // 多行文本1
        vo.addExtDesc("text10", field2ValueMap.get("text10") == null ? "" : field2ValueMap.get("text10")[1]); // 多行文本10
        vo.addExtDesc("text11", field2ValueMap.get("text11") == null ? "" : field2ValueMap.get("text11")[1]); // 多行文本11
        vo.addExtDesc("text12", field2ValueMap.get("text12") == null ? "" : field2ValueMap.get("text12")[1]); // 多行文本12
        vo.addExtDesc("text13", field2ValueMap.get("text13") == null ? "" : field2ValueMap.get("text13")[1]); // 多行文本13
        vo.addExtDesc("text14", field2ValueMap.get("text14") == null ? "" : field2ValueMap.get("text14")[1]); // 多行文本14
        vo.addExtDesc("text15", field2ValueMap.get("text15") == null ? "" : field2ValueMap.get("text15")[1]); // 多行文本15
        vo.addExtDesc("text2", field2ValueMap.get("text2") == null ? "" : field2ValueMap.get("text2")[1]); // 多行文本2
        vo.addExtDesc("text3", field2ValueMap.get("text3") == null ? "" : field2ValueMap.get("text3")[1]); // 多行文本3
        vo.addExtDesc("text4", field2ValueMap.get("text4") == null ? "" : field2ValueMap.get("text4")[1]); // 多行文本4
        vo.addExtDesc("text5", field2ValueMap.get("text5") == null ? "" : field2ValueMap.get("text5")[1]); // 多行文本5
        vo.addExtDesc("text6", field2ValueMap.get("text6") == null ? "" : field2ValueMap.get("text6")[1]); // 多行文本6
        vo.addExtDesc("text7", field2ValueMap.get("text7") == null ? "" : field2ValueMap.get("text7")[1]); // 多行文本7
        vo.addExtDesc("text8", field2ValueMap.get("text8") == null ? "" : field2ValueMap.get("text8")[1]); // 多行文本8
        vo.addExtDesc("text9", field2ValueMap.get("text9") == null ? "" : field2ValueMap.get("text9")[1]); // 多行文本9
        vo.addExtDesc("undertakenoffice",
                field2ValueMap.get("undertakenoffice") == null ? "" : field2ValueMap.get("undertakenoffice")[1]); // 承办机构
        vo.addExtDesc("undertaker", field2ValueMap.get("undertaker") == null ? "" : field2ValueMap.get("undertaker")[1]); // 承办人
        vo.addExtDesc("unit_level", field2ValueMap.get("unit_level") == null ? "" : field2ValueMap.get("unit_level")[1]); // 公文级别
        vo.addExtDesc("urgent_level",
                field2ValueMap.get("urgent_level") == null ? "" : field2ValueMap.get("urgent_level")[1]); // 紧急程度
        //vo.addExtDesc("wenshuguanli", field2ValueMap.get("wenshuguanli") == null ? "" : field2ValueMap.get("wenshuguanli")[1]); // 文书管理
        //vo.addExtDesc("yuedu", field2ValueMap.get("yuedu") == null ? "" : field2ValueMap.get("yuedu")[1]); // 阅读
        //vo.addExtDesc("zhihui", field2ValueMap.get("zhihui") == null ? "" : field2ValueMap.get("zhihui")[1]); // 知会
    }
    
    
    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }
    public FileManager getFileManager() {
        return fileManager;
    }
    public void setXmlConverter(XMLConverter xmlConverter) {
        this.xmlConverter = xmlConverter;
    }
    public XMLConverter getXmlConverter() {
        return xmlConverter;
    }
    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }
    public AttachmentManager getAttachmentManager() {
        return attachmentManager;
    }
    public void setHtmSignetManager(V3xHtmDocumentSignatManager htmSignetManager) {
        this.htmSignetManager = htmSignetManager;
    }
    public V3xHtmDocumentSignatManager getHtmSignetManager() {
        return htmSignetManager;
    }
    
    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }
    
    public AffairManager getAffairManager() {
        return affairManager;
    }
    
    public void setEdocManager(EdocManager edocManager) {
        this.edocManager = edocManager;
    }
    public EdocManager getEdocManager() {
        return edocManager;
    }
    
    public void setEdocFormManager(EdocFormManager edocFormManager) {
        this.edocFormManager = edocFormManager;
    }
    public EdocFormManager getEdocFormManager() {
        return edocFormManager;
    }
    public void setTempleteManager(TemplateManager templeteManager) {
        this.templeteManager = templeteManager;
    }
    public TemplateManager getTempleteManager() {
        return templeteManager;
    }
}
