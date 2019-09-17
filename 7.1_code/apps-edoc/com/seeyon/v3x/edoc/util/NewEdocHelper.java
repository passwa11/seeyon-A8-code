package com.seeyon.v3x.edoc.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.domain.EdocFormExtendInfo;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocFormManager;
import com.seeyon.v3x.edoc.manager.EdocSummaryRelationManager;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.manager.RecieveEdocManager;

public class NewEdocHelper {
    private static final Log LOGGER = LogFactory.getLog(NewEdocHelper.class);

    /**
     * 查找签收部门所属单位或者签收单位
     * @param exchangeOrgId 签收ID（单位ID|部门ID）
     * @param exchangeOrgType  签收类型（部门|单位）
     * @return
     */
    public static Long getAccountIdOfRegisterByOrgIdAndOrgType(Long exchangeOrgId, int exchangeOrgType) {
        OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
        if (EdocRecieveRecord.Exchange_Receive_iAccountType_Dept == exchangeOrgType) {
            V3xOrgDepartment dept;
            try {
                dept = orgManager.getDepartmentById(exchangeOrgId);
                return dept.getOrgAccountId();
            } catch (BusinessException e) {
                LOGGER.error("查找部门异常:", e);
            }
        } else {
            return exchangeOrgId;
        }
        return 0L;
    }

    /**
     * 查找签收部门所属单位或者签收单位
     * @param exchangeId   EdocRecieveRecord的ID
     * @return
     */
    public static Long getAccountIdOfRegisterByExchangeId(Long exchangeId) {
        RecieveEdocManager recieveEdocManager = (RecieveEdocManager) AppContext.getBean("recieveEdocManager");
        EdocRecieveRecord record = recieveEdocManager.getEdocRecieveRecord(Long.valueOf(exchangeId));
        return getAccountIdOfRegisterByOrgIdAndOrgType(record.getExchangeOrgId(), record.getExchangeType());
    }

    public static List<Attachment> excludeType2ToNewAttachmentList(EdocSummary summary) {
        AttachmentManager attachmentManager = (AttachmentManager) AppContext.getBean("attachmentManager");
        List<Attachment> atts= attachmentManager.getByReference(summary.getId(), summary.getId());
		List<Attachment> exclude2List=new ArrayList<Attachment>();//需要重新new一个List，不能在atts的基础上使用remove.
		for(Attachment att:atts){
			if(!Integer.valueOf(2).equals(att.getType()))
				exclude2List.add(att);
		}
		return exclude2List;  
    }

    /**
     * 
     * @param createPersionName
     * @param summary
     * @param orgAccountId
     * @param contentNo 收文记录接受到的正文的编号。
     * @return
     * @throws CloneNotSupportedException
     */
    public static EdocSummary cloneNewSummaryAndSetProperties(String createPersionName, EdocSummary summary,
            Long orgAccountId, Integer contentNo) throws CloneNotSupportedException {
        EdocSummary edocSummary = null;
        if (summary != null) {
            edocSummary = (EdocSummary) summary.clone();
            edocSummary.setCreatePerson(createPersionName);
            edocSummary.setDeadline(0L);
            edocSummary.setAdvanceRemind(0L);
            edocSummary.setOrgAccountId(orgAccountId);
            edocSummary.setSerialNo(null);
            edocSummary.setProcessId(null);
            edocSummary.setCaseId(null);
            edocSummary.setHasArchive(false);
            edocSummary.setArchiveId(null);
            edocSummary.setIsQuickSend(false); //OA-57209登记电子公文时，调用模板，自动变成了快速收文_把发文的快速发文属性给读取过来了
            //传入的参数ContentnO可能为空，所以进行防护性处理，先取出一个EdocBody.
            EdocBody eb = edocSummary.getFirstBody();
            for (EdocBody ebody : edocSummary.getEdocBodies()) {
                if (ebody.getContentNo().equals(contentNo)) {
                    eb = ebody;
                    break;
                }
            }
            edocSummary.setEdocBodies(new HashSet<EdocBody>());
            edocSummary.getEdocBodies().add(eb);
            //登记时候,为了保证印章校验有效,必须保持原来的文件名称不变(office控件的FileName属性一致才可以)
            EdocBody firsetBody = summary.getFirstBody();
            if (firsetBody!=null && !com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_HTML.equals(firsetBody.getContentType())) {
            	EdocBody newFirstBody = edocSummary.getFirstBody();
            	if(newFirstBody != null) {
            		newFirstBody.setContentName(firsetBody.getContent());
            	}
            }
        }
        if (edocSummary == null) {
            edocSummary = new EdocSummary();
        }
        return edocSummary;
    }

    /**
     * puyc 关联收文
     * 通过edocId和edocType查找，如果在EdocSummary有记录，则返回收文单
     * 如果没有记录，则在EdocRegister 查找，如果有记录，则返回登记单 
     * 如果没有记录，则在EdocRecieveRecord查找，如果有记录，则返回签收单
     * 如果没有记录，则提示，资源不存在
     */
    public static String relationReceive(String relationEdocIdStr, String edocTypeStr) {
        EdocSummaryRelationManager edocSummaryRelationManager = (EdocSummaryRelationManager) AppContext
                .getBean("edocSummaryRelationManager");
        Long relationEdocId = Strings.isNotBlank(relationEdocIdStr) ? Long.parseLong(relationEdocIdStr) : null;
        int relationEdocType = Strings.isNotBlank(edocTypeStr) ? Integer.parseInt(edocTypeStr) : -1;
        String relationUrl = "";//收文的路径
        if (relationEdocId != null && relationEdocType != -1) {
            EdocSummary edocSummary = edocSummaryRelationManager.findEdocSummary(relationEdocId, relationEdocType);
            if (edocSummary != null) {// 收文单
                relationUrl = "edocController.do?method=detailIFrame&summaryId=" + relationEdocId;
            } else {
                EdocRegister edocRegister = edocSummaryRelationManager.findEdocRegister(relationEdocId,
                        relationEdocType);
                if (edocRegister != null) {// 登记单
                    relationUrl = "edocController.do?method=edocRegisterDetail&registerId=" + edocRegister.getId();
                } else {
                    EdocRecieveRecord edocRecieveRecord = edocSummaryRelationManager
                            .findEdocRecieveRecord(relationEdocId);
                    if (edocRecieveRecord != null) {//签收单
                        relationUrl = "exchangeEdoc.do?method=edit&modelType=received&from=tobook&id="
                                + edocRecieveRecord.getId();
                    }
                }
            }
        }
        return relationUrl;
    }


    /**
     * 取得指定【单位，公文单类型】或者【指定模板公文单】的公文单列表。
     * 非流程模板，只取模板公文单，流程模板和自由流程取所有公文单。
     * @param accountId ：特定单位
     * @param iEdocType ：公文单类型
     * @param formId    ：公文单ID
     * @param templeteTypeName:模板类型，流程模板，格式模板
     * @return
     */
    public static List<EdocForm> getLoginAccountOrCurrentTempleteEdocForms(long accountId, String domainIds,
            int iEdocType, String templeteId, long formId, String templeteTypeName, long subType) {
        EdocFormManager edocFormManager = (EdocFormManager) AppContext.getBean("edocFormManager");
        List<EdocForm> edocForms = new ArrayList<EdocForm>();
        
        //OA-48276 选择一条待登记的电子公文进行登记，将当前登记页面填写内容后另存为个人模板，然后去调用这个个人模板，收文单只能看到存个人模板时的文单，看不到其他的文单
        boolean isDisplayAllForms = true;//是否显示全部的文单
        if(Strings.isNotBlank(templeteId)){
            TemplateManager templateManager = (TemplateManager) AppContext.getBean("templateManager");
            try {
                /*当调用模板时，只有当调用的 个人模板时(该个人模板没有父级模板或父级模板不是系统模板)，就显示全部的文单*/
                CtpTemplate template = templateManager.getCtpTemplate(Long.parseLong(templeteId));
                if(template.isSystem()){
                    isDisplayAllForms = false;
                }else{
                    if(template.getFormParentid()!=null){
                        CtpTemplate pTemplate = templateManager.getCtpTemplate(template.getFormParentid());
                        //个人模板的父模板如果是流程模板时，要显示全部文单
                        if(pTemplate.isSystem()&& !pTemplate.getType().equals(TemplateEnum.Type.workflow.name())){
                            isDisplayAllForms = false;
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("获取模板发生异常 "+e.getMessage());
            }
        }
        //OA-48276 --- end
        
        if (!isDisplayAllForms && Strings.isNotBlank(templeteId) && !TemplateEnum.Type.workflow.name().equals(templeteTypeName)) {
            EdocForm ef = edocFormManager.getEdocForm(formId);
            edocForms.add(ef);
        } else {
        	edocForms = edocFormManager.getEdocForms(accountId, domainIds, iEdocType, subType);

            //过滤掉兼职重复的公文单
            List<EdocForm> l = new ArrayList<EdocForm>();
            Set<Long> filter = new HashSet<Long>();
            for (EdocForm form : edocForms) {
                if (!filter.contains(form.getId())) {
                    filter.add(form.getId());
                    l.add(form);
                }
            }
            edocForms = l;
        }
        //去掉停用的
        for (Iterator<EdocForm> it = edocForms.iterator(); it.hasNext();) {
            EdocForm ef = it.next();
            Long loginAccountId = accountId;
            EdocFormExtendInfo info = edocFormManager.getEdocFormExtendInfoByForm(ef,loginAccountId);
            if (info==null  || (info.getStatus().intValue() != EdocForm.C_iStatus_Published.intValue())){
            	it.remove();
            }
        }
        return edocForms == null ? new ArrayList<EdocForm>():edocForms;
    }

}
