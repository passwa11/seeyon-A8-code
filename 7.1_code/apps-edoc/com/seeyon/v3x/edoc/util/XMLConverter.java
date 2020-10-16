package com.seeyon.v3x.edoc.util;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.edoc.enums.EdocEnum.MarkCategory;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.organization.bo.MemberPost;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.dao.EdocMarkAclDAO;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.domain.EdocElementFlowPermAcl;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.domain.EdocFormElement;
import com.seeyon.v3x.edoc.domain.EdocMarkAcl;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocElementFlowPermAclManager;
import com.seeyon.v3x.edoc.manager.EdocElementManager;
import com.seeyon.v3x.edoc.manager.EdocFormManager;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocMarkDefinitionManager;
import com.seeyon.v3x.edoc.webmodel.EdocMarkModel;

public class XMLConverter {
    
    private static final Log LOGGER = LogFactory.getLog(XMLConverter.class);
    
    private EdocElementFlowPermAclManager edocElementFlowPermAclManager;    
    
    private EnumManager enumManagerNew;
    private EdocMarkDefinitionManager edocMarkDefinitionManager;
    private EdocElementManager edocElementManager;
    private OrgManager orgManager;
    private EdocFormManager edocFormManager = null;
    
    /**
     * 根据EdocSummary数据,公文单上元素组合XML数据
     * @param elements  :公文单中包含的公文元素
     * @param edocSummary:公文数据
     * @param actorId:公文处理权限ID,如果actorId<0,默认为编辑
     * @return
     */
    public StringBuffer convert(List <EdocFormElement>elements,EdocSummary edocSummary,long actorId, int edocType) {
        return convert(elements,edocSummary,actorId,edocType,false,false);
    }
    
    /**
     * 根据EdocSummary数据,公文单上元素组合XML数据
     * @param elements  :公文单中包含的公文元素
     * @param edocSummary:公文数据
     * @param actorId:公文处理权限ID,如果actorId<0,默认为编辑
     * @param isTemplete : 是否是公文模板新建或者修改页面
     * @param isNoShowOriginalDocMark : 是否不显示当前问号，调用模板的时候不显示
     * @return
     */
    public StringBuffer convert(List <EdocFormElement>elements,EdocSummary edocSummary,long actorId, int edocType,boolean isTemplete,boolean isNoShowOriginalDocMark) {
        return convert(elements, edocSummary, actorId, edocType, isTemplete, isNoShowOriginalDocMark, null);
    }
    
    /**
     *  重载convert方法，支持传入affair
     * @Author      : xuqiangwei
     * @Date        : 2015年1月26日下午5:56:47
     * @param elements
     * @param edocSummary
     * @param actorId
     * @param edocType
     * @param isTemplete
     * @param isNoShowOriginalDocMark
     * @param affair
     * @return
     */
    public StringBuffer convert(List <EdocFormElement>elements,EdocSummary edocSummary,
            long actorId, int edocType,boolean isTemplete,boolean isNoShowOriginalDocMark, CtpAffair affair) {
        Hashtable <Long,EdocElementFlowPermAcl> actorsAcc=edocElementFlowPermAclManager.getEdocElementFlowPermAclsHs(actorId);
        //V3xOrgMember user = edocSummary.getStartMember() ;
        Long orgAccountId = null ;
        if(edocSummary != null) {
            orgAccountId = edocSummary.getOrgAccountId() ;
        }
        //OA-28678  postgre环境，公文单绑定了本单位枚举后授权给外单位，外单位调用时不能显示该枚举值  
        //需要取得创建文单的单位id
        Long formAccountId = null;
        EdocFormElement formElement = null;
        //动态拼接XML第一部分,如果是文本框,在第一部分赋值,如果类型为下拉列表,在第二部分赋值
        StringBuffer xml = new StringBuffer("");
        String topHeader = "&&&&&&&&  xsl_start  &&&&&&&& Url=view1.xsl &&&&&&&&  data_start  &&&&&&&&";
        String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        String bodyHeader = "<my:myFields xmlns:my=\"www.seeyon.com/form/2007\">";
        String bodyEnder   = "</my:myFields>";
        String leftBracket = "<";
        String domainName  = "my:";
        String rightBracket = ">"; 
        String bias  = "/";
        String fieldName = "";
        String fieldValue = "";
        String fieldType="text";
        StringBuffer xmlBody = new StringBuffer("");
        
        //动态拼接XML第二部分,所有元素的格式在这里定义
        StringBuffer inputBody = new StringBuffer(""); 
        
        String inputHeader = "&&&&&&&&  input_start  &&&&&&&&";
        String fieldStart = "<FieldInputList>";
        String fieldEnd   = "</FieldInputList>";
        String fieldInput = "<FieldInput name=\"";
        String fieldInputEnd = "</FieldInput>";     
        String display = "<Input display=\"";
        String displayValue = "\" value=\"";
        String displayEnd = "\"/>";
        String name = "";
        String dbFieldType="varchar";
        //用来做V50新分支类型遍历
        String branchFieldDbType="varchar";
        
        EdocElementFlowPermAcl elementAcl=null;
        for(int i=0;i<elements.size();i++){
            boolean setListValueFlag = true;//是否设置List的值
            fieldName="";
            fieldType="text";
            fieldValue="";
            dbFieldType="";
            branchFieldDbType="";
            CtpEnumItem metaDataItem = null;
            List<CtpEnumItem> metaItem = null;
            formElement = elements.get(i);
            if(i==0){
                //OA-29616 test02上传文单报错
                if(formElement.getFormId()!=null){
                    EdocForm edocForm = edocFormManager.getEdocForm(formElement.getFormId());
                    formAccountId = edocForm.getDomainId();
                }
            }
            
//          判断是否是签报单，是否有行文类型字段，有则跳出！            
           /* if(edocType == Constants.EDOC_FORM_TYPE_SIGN && formElement.getElementId() == 3){
                continue;
            }       */    
//          判断是否为处理意见
            if((formElement.getElementId()>=203 && formElement.getElementId()<=207) || (formElement.getElementId()>=281 && formElement.getElementId()<=290)){
                continue;
            }
            String access="browse";
            //权限actorId<0时,对公文元素的操作权限默认为编辑,用于建立模版
            //公文单中的元素没用早到对应的权限,默认为只读
            elementAcl=actorsAcc.get(formElement.getElementId());
            if((actorId<0 && actorId>-100) || (elementAcl!=null && elementAcl.getAccess()==EdocElementFlowPermAcl.ACCESS_STATE.edit.ordinal()))
            {
                access=EdocElementFlowPermAcl.ACCESS_STATE.edit.name();
            }
            //doc_mark,doc_mark2,serial_no
            if(formElement.getElementId()==4 || formElement.getElementId()==5 || formElement.getElementId()==21) {
                if(edocSummary!=null && edocSummary.getFinished() && "archived".equals(edocSummary.getFrom())) {
                    access = EdocElementFlowPermAcl.ACCESS_STATE.read.name();
                }
            }
            String text = "\" type=\"text\" access=\""+access+"\" required=\""+formElement.isRequired()+"\" allowprint=\"true\" allowtransmit=\"true\" />";
            String select = "\" type=\"select\" access=\""+access+"\"  required=\""+formElement.isRequired()+"\" allowprint=\"true\" allowtransmit=\"true\">";
            String textarea="\" type=\"textarea\" access=\""+access+"\"  required=\""+formElement.isRequired()+"\" allowprint=\"true\" allowtransmit=\"true\" />";
            DecimalFormat df =  new DecimalFormat("###########0.####");
            if(formElement.getElementId()==1){
                
                fieldName = "subject";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                fieldType="textarea"; //xiangfan 添加 修复 GOV-4372标题多行问题
                if(null!=edocSummary && null!=edocSummary.getSubject()){
                    if(edocSummary.getSubject().length()>30){
                        fieldType="textarea";
                        dbFieldType="longtext";                     
                    }
                    fieldValue = edocSummary.getSubject();
                }
                //标题的必填项必须是true。因为初始化数据都是0，所以这里强制要求标题必填
                textarea="\" type=\"textarea\" access=\""+access+"\"  required=\"true\" allowprint=\"true\" allowtransmit=\"true\" />";
            }
            else if(formElement.getElementId()==2){
                fieldType="select";             
                fieldName = "doc_type";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getDocType())){
                    fieldValue = edocSummary.getDocType();
                }
                        metaItem =  enumManagerNew.getEnumItems(EnumNameEnum.edoc_doc_type);
                        inputBody.append(fieldInput).append(domainName).append(fieldName).append(select);
                        boolean bool = false;
                        StringBuilder strb=new StringBuilder();
                        for(int x=0;x<metaItem.size();x++){
                            metaDataItem = (CtpEnumItem)metaItem.get(x);
                            if(metaDataItem.getState().intValue() == 0)continue;  //com.seeyon.v3x.system.Constants.METADATAITEM_SWITCH_DISABLE = 0;  //停用
                            name = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", metaDataItem.getLabel());
                            if(null!=edocSummary && !"".equals(edocSummary.getDocType())&& (metaDataItem.getValue()).equals(edocSummary.getDocType())){
                                strb.append(display).append(Strings.toHTML(Strings.toXmlStr(name),false)).append(displayValue).append(metaDataItem.getValue()).append("\" select=\"true\" ").append("/>");                             
                                bool = true;
                            }else{
                                strb.append(display).append(Strings.toHTML(Strings.toXmlStr(name),false)).append(displayValue).append(metaDataItem.getValue()).append(displayEnd);
                            }
                        }
                        if(bool == false && null!=edocSummary && edocSummary.getDocType()!=null){
                            CtpEnumItem newItem = enumManagerNew.getEnumItem(EnumNameEnum.edoc_doc_type, edocSummary.getDocType());
                            if(null!=newItem){
                                name = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", newItem.getLabel());
                                strb.append(display).append(Strings.toHTML(Strings.toXmlStr(name),false)).append(displayValue).append(newItem.getValue()).append("\" select=\"true\" ").append("/>");
                                bool = true;
                            }
                        }
                        if (bool) {
                            inputBody.append(display).append("").append(displayValue).append("").append(displayEnd);
                        }else{
                            inputBody.append(display).append("").append(displayValue).append("").append("\" select=\"true").append(displayEnd);
                        }
                        inputBody.append(strb);
                    inputBody.append(fieldInputEnd);
            }
            else if(formElement.getElementId()==3){
                fieldType="select";
                fieldName = "send_type";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getSendType())){
                    fieldValue = edocSummary.getSendType();
                }
        //      if( null!=edocSummary.getSendType()){ 
                    metaItem =  enumManagerNew.getEnumItems(EnumNameEnum.edoc_send_type);
                    inputBody.append(fieldInput).append(domainName).append(fieldName).append("\" branchFieldDbType =\"int").append(select);
                    boolean bool = false;
                    StringBuilder strb=new StringBuilder();
                    for(int x=0;x<metaItem.size();x++){
                        metaDataItem = (CtpEnumItem)metaItem.get(x);
                        if(metaDataItem.getState().intValue() == 0)continue;//com.seeyon.v3x.system.Constants.METADATAITEM_SWITCH_DISABLE = 0;  //停用
                        name = ResourceUtil.getString(metaDataItem.getLabel());
                        if(null!=edocSummary && !"".equals(edocSummary.getSendType())&& (metaDataItem.getValue()).equals(edocSummary.getSendType())){
                            strb.append(display).append(Strings.toHTML(Strings.toXmlStr(name),false)).append(displayValue).append(metaDataItem.getValue()).append("\" select=\"true\" ").append("/>");                             
                            bool = true;
                        }else{
                            strb.append(display).append(Strings.toHTML(Strings.toXmlStr(name),false)).append(displayValue).append(metaDataItem.getValue()).append(displayEnd);
                        }
                    }
                    if(bool == false && null!=edocSummary && edocSummary.getSendType()!=null){
                        CtpEnumItem newItem = enumManagerNew.getEnumItem(EnumNameEnum.edoc_doc_type, edocSummary.getSendType());
                        if(null!=newItem){
                            name = ResourceUtil.getString(newItem.getLabel());
                            strb.append(display).append(Strings.toHTML(Strings.toXmlStr(name),false)).append(displayValue).append(newItem.getValue()).append("\" select=\"true\" ").append("/>");                      
                            bool = true;
                        }
                    }
                    if (bool) {
                        inputBody.append(display).append("").append(displayValue).append("").append(displayEnd);
                    }else{
                        inputBody.append(display).append("").append(displayValue).append("").append("\" select=\"true").append(displayEnd);
                    }
                    inputBody.append(strb);
                    inputBody.append(fieldInputEnd);
        //      }
            }
            else if(formElement.getElementId()==4){
                fieldName = "doc_mark"; 
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getDocMark()){
                    String docMark = edocSummary.getDocMark();
                    //OA-37020 weblogic环境：a1新建模版，绑定了文号，新建成功后，管理员将该文号删除了，这时打开模版查看，发现记录的是id
                    if(validateEdocMark(docMark,fieldName,edocSummary.getTempleteId())){
                        fieldValue = docMark;
                    }
                } 
                
                if(null!=edocSummary && !"".equals(edocSummary.getEdocType()+"") && (EdocEnum.edocType.sendEdoc.ordinal() == edocSummary.getEdocType() || EdocEnum.edocType.signReport.ordinal() == edocSummary.getEdocType()))
                {
                    addDocMarkListList(inputBody,fieldName,fieldValue,access,EdocEnum.MarkType.edocMark.ordinal(),
                            orgAccountId,edocSummary,isTemplete,isNoShowOriginalDocMark,formElement.isRequired(),actorId, affair);
                }
                //OA-43494 调用绑定文号的模版拟文，文号处显示了id+文号
                //当拟文时文号为不可编辑，且调用绑定了文号的模板时，文号显示需要特殊处理
                if(isNoShowOriginalDocMark){
                    fieldValue = getMarkFromTemplate(edocSummary, fieldValue,fieldName);
                }
            }
            else if(formElement.getElementId()==21){
                fieldName = "doc_mark2";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getDocMark2()){
                    if(validateEdocMark(edocSummary.getDocMark2(),fieldName,edocSummary.getTempleteId())){
                        fieldValue = edocSummary.getDocMark2();
                    }
                }
                
                if(null!=edocSummary && !"".equals(edocSummary.getEdocType()+"") && (EdocEnum.edocType.sendEdoc.ordinal() == edocSummary.getEdocType() || EdocEnum.edocType.signReport.ordinal() == edocSummary.getEdocType()))
                {           
                    addDocMarkListList(inputBody,fieldName,fieldValue,access,EdocEnum.MarkType.edocMark.ordinal(),
                            orgAccountId,edocSummary,isTemplete,isNoShowOriginalDocMark,formElement.isRequired(),actorId, affair);
                }
                
                if(isNoShowOriginalDocMark){
                    fieldValue = getMarkFromTemplate(edocSummary, fieldValue,fieldName);
                }
            }
            else if(formElement.getElementId()==5){
                fieldName = "serial_no";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if( null!=edocSummary && null!=edocSummary.getSerialNo()){
                    if(validateEdocMark(edocSummary.getSerialNo(),fieldName,edocSummary.getTempleteId())){
                        fieldValue = edocSummary.getSerialNo();
                    }
                }
                if( null!=edocSummary ){
                    addDocMarkListList(inputBody,fieldName,fieldValue,access,EdocEnum.MarkType.edocInMark.ordinal(),
                            orgAccountId,edocSummary,isTemplete,isNoShowOriginalDocMark,formElement.isRequired(),actorId, affair);  
                } 
                if(isNoShowOriginalDocMark){
                    fieldValue = getMarkFromTemplate(edocSummary, fieldValue,fieldName);
                }
            }
            else if(formElement.getElementId()==6){
                fieldType="select";
                fieldName = "secret_level";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getSecretLevel())){
                fieldValue = edocSummary.getSecretLevel();
                }
            //  if( null!=edocSummary.getSecretLevel()){
                    metaItem =  enumManagerNew.getEnumItems(EnumNameEnum.edoc_secret_level);
                    inputBody.append(fieldInput).append(domainName).append(fieldName).append("\" branchFieldDbType =\"int").append(select);
                    boolean bool = false;//判断是否初始化过默认值，就是说是否是查看状态（非新建）
                    StringBuilder strb=new StringBuilder();
                    for(int x=0;x<metaItem.size();x++){
                        metaDataItem = (CtpEnumItem)metaItem.get(x);
                        if(metaDataItem.getState().intValue() == 0)continue;//com.seeyon.v3x.system.Constants.METADATAITEM_SWITCH_DISABLE = 0;  //停用
                        name = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", metaDataItem.getLabel());
                        if(null!=edocSummary && !"".equals(edocSummary.getSecretLevel())&& (metaDataItem.getValue()).equals(edocSummary.getSecretLevel())){
                            strb.append(display).append(Strings.toHTML(Strings.toXmlStr(name),false)).append(displayValue).append(metaDataItem.getValue()).append("\" select=\"true\" ").append("/>");                             
                            bool = true;
                        }else{
                            strb.append(display).append(Strings.toHTML(Strings.toXmlStr(name),false)).append(displayValue).append(metaDataItem.getValue()).append(displayEnd);
                        }
                    }
                    //如果其中的字段不为空，并且没有处在新建状态的话
                    if(bool == false && null!=edocSummary && edocSummary.getSecretLevel()!=null){
                        CtpEnumItem newItem = enumManagerNew.getEnumItem(EnumNameEnum.edoc_secret_level, edocSummary.getSecretLevel());
                        if(null!=newItem){
                            name = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", newItem.getLabel());
                            strb.append(display).append(Strings.toHTML(Strings.toXmlStr(name),false)).append(displayValue).append(newItem.getValue()).append("\" select=\"true\" ").append("/>");                      
                            bool = true;
                        }
                    }
                    if (bool) {
                        inputBody.append(display).append("").append(displayValue).append("").append(displayEnd);
                    }else{
                        inputBody.append(display).append("").append(displayValue).append("").append("\" select=\"true").append(displayEnd);
                    }
                    inputBody.append(strb);
                    inputBody.append(fieldInputEnd);
            //  }
            }
            else if(formElement.getElementId()==7){
                fieldType="select";
                fieldName = "urgent_level";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getUrgentLevel())){
                    fieldValue = edocSummary.getUrgentLevel();
                } 
        //      if( null!=edocSummary.getUrgentLevel()){
                    metaItem =  enumManagerNew.getEnumItems(EnumNameEnum.edoc_urgent_level);
                    inputBody.append(fieldInput).append(domainName).append(fieldName).append("\" branchFieldDbType =\"int").append(select);
                    boolean bool = false;
                    StringBuilder strb=new StringBuilder();
                    for(int x=0;x<metaItem.size();x++){
                        metaDataItem = (CtpEnumItem)metaItem.get(x);
                        if(metaDataItem.getState().intValue() == 0)continue; //com.seeyon.v3x.system.Constants.METADATAITEM_SWITCH_DISABLE = 0;  //停用
                        name = ResourceUtil.getString(metaDataItem.getLabel());
                        if(null!=edocSummary && !"".equals(edocSummary.getUrgentLevel())&& (metaDataItem.getValue()).equals(edocSummary.getUrgentLevel())){
                            strb.append(display).append(Strings.toHTML(Strings.toXmlStr(name),false)).append(displayValue).append(metaDataItem.getValue()).append("\" select=\"true\" ").append("/>");                             
                            bool = true;
                        }else{
                            strb.append(display).append(Strings.toHTML(Strings.toXmlStr(name),false)).append(displayValue).append(metaDataItem.getValue()).append(displayEnd);
                        }
                    }
                    if(bool == false && null!=edocSummary && edocSummary.getUrgentLevel()!=null){
                        CtpEnumItem newItem = enumManagerNew.getEnumItem(EnumNameEnum.edoc_urgent_level, edocSummary.getUrgentLevel());
                        if(null!=newItem){
                            name = ResourceUtil.getString(newItem.getLabel());
                            strb.append(display).append(Strings.toHTML(Strings.toXmlStr(name),false)).append(displayValue).append(newItem.getValue()).append("\" select=\"true\" ").append("/>");                      
                            bool = true;
                        }
                    }
                    if (bool) {
                        inputBody.append(display).append("").append(displayValue).append("").append(displayEnd);
                    }else{
                        inputBody.append(display).append("").append(displayValue).append("").append("\" select=\"true").append(displayEnd);
                    }
                    inputBody.append(strb);
                    inputBody.append(fieldInputEnd);
        //      }
            }
            else if(formElement.getElementId()==350){//公文级别
                fieldType="select";
                fieldName = "unit_level";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getUnitLevel())){
                    fieldValue = edocSummary.getUnitLevel();
                } 
                metaItem =  enumManagerNew.getEnumItems(EnumNameEnum.edoc_unit_level);
                inputBody.append(fieldInput).append(domainName).append(fieldName).append("\" branchFieldDbType =\"int").append(select);
                boolean bool = false;
                StringBuilder strb=new StringBuilder();
                Collections.sort(metaItem);//枚举排序
                for(int x=0;x<metaItem.size();x++){
                    metaDataItem = (CtpEnumItem)metaItem.get(x);
                    if(metaDataItem.getState().intValue() == 0)continue; //com.seeyon.v3x.system.Constants.METADATAITEM_SWITCH_DISABLE = 0;  //停用
                    name = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", metaDataItem.getLabel());
                    if(null!=edocSummary && !"".equals(edocSummary.getUnitLevel())&& (metaDataItem.getValue()).equals(edocSummary.getUnitLevel())){
                        strb.append(display).append(Strings.toHTML(Strings.toXmlStr(name),false)).append(displayValue).append(metaDataItem.getValue()).append("\" select=\"true\" ").append("/>");                             
                        bool = true;
                    }else{
                        strb.append(display).append(Strings.toHTML(Strings.toXmlStr(name),false)).append(displayValue).append(metaDataItem.getValue()).append(displayEnd);
                    }
                }
                if(bool == false && null!=edocSummary && edocSummary.getUnitLevel()!=null){
                    CtpEnumItem newItem = enumManagerNew.getEnumItem(EnumNameEnum.edoc_unit_level, edocSummary.getUnitLevel());
                    if(null!=newItem){
                        name = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", newItem.getLabel());
                        strb.append(display).append(Strings.toHTML(Strings.toXmlStr(name),false)).append(displayValue).append(newItem.getValue()).append("\" select=\"true\" ").append("/>");                      
                        bool = true;
                    }
                }
                if (bool) {
                    inputBody.append(display).append("").append(displayValue).append("").append(displayEnd);
                }else{
                    inputBody.append(display).append("").append(displayValue).append("").append("\" select=\"true").append(displayEnd);
                }
                inputBody.append(strb);
                inputBody.append(fieldInputEnd);
            }
            else if(formElement.getElementId()==8){
                fieldType="select";
                fieldName = "keep_period";
                branchFieldDbType ="int";
                if(null!=edocSummary && edocSummary.getKeepPeriod()!=null){
                    fieldValue = String.valueOf(edocSummary.getKeepPeriod());
                }
        //      if( null!=edocSummary.getKeepPeriod()){
                    metaItem =  enumManagerNew.getEnumItems(EnumNameEnum.edoc_keep_period);
                    inputBody.append(fieldInput).append(domainName).append(fieldName).append("\" branchFieldDbType =\"int").append(select);
                    boolean bool = false;
                    StringBuilder strb=new StringBuilder();
                    for(int x=0;x<metaItem.size();x++){
                        metaDataItem = (CtpEnumItem)metaItem.get(x);
                        if(metaDataItem.getState().intValue() == 0)continue;//com.seeyon.v3x.system.Constants.METADATAITEM_SWITCH_DISABLE = 0;  //停用
                        name = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", metaDataItem.getLabel());
                        if(null!=edocSummary && edocSummary.getKeepPeriod()!=null && edocSummary.getKeepPeriod().toString().equals(metaDataItem.getValue())){
                            strb.append(display).append(Strings.toHTML(Strings.toXmlStr(name),false)).append(displayValue).append(metaDataItem.getValue()).append("\" select=\"true\" ").append("/>");                             
                            bool = true;
                        }else{
                            strb.append(display).append(Strings.toHTML(Strings.toXmlStr(name),false)).append(displayValue).append(metaDataItem.getValue()).append(displayEnd);
                        }
                    }
                    if(bool == false && null!=edocSummary && edocSummary.getKeepPeriod()!=null){
                        CtpEnumItem newItem = enumManagerNew.getEnumItem(EnumNameEnum.edoc_keep_period, edocSummary.getKeepPeriod().toString());
                        if(null!=newItem){
                            name = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", newItem.getLabel());
                            strb.append(display).append(Strings.toHTML(Strings.toXmlStr(name),false)).append(displayValue).append(newItem.getValue()).append("\" select=\"true\" ").append("/>");                      
                            bool = true;
                        }
                    }
                    if (bool) {
                        inputBody.append(display).append("").append(displayValue).append("").append(displayEnd);
                    }else{
                        inputBody.append(display).append("").append(displayValue).append("").append("\" select=\"true").append(displayEnd);
                    }
                    inputBody.append(strb);
                    inputBody.append(fieldInputEnd);
        //      }
            }
            else if(formElement.getElementId()==9){
                fieldName = "create_person";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getCreatePerson()){
                    fieldValue = edocSummary.getCreatePerson();
                }
            }
            else if(formElement.getElementId()==10){
                fieldName = "send_unit";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                fieldType="textarea";
                if(null!=edocSummary && null!=edocSummary.getSendUnit()){
                    if(edocSummary.getSendUnit().length()>15){
                        fieldType="textarea";
                        dbFieldType="longtext";                     
                    }
                    fieldValue = edocSummary.getSendUnit();
                }
            }
            else if(formElement.getElementId()==26){
                fieldName = "send_unit2";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                fieldType="textarea";
                if(null!=edocSummary && null!=edocSummary.getSendUnit2()){
                    if(edocSummary.getSendUnit2().length()>15){
                        fieldType="textarea";
                        dbFieldType="longtext";                     
                    }
                    fieldValue = edocSummary.getSendUnit2();
                }
            }
            //------------changyi 增加附件和发文部门公文元素  start-------------------------
            else if(formElement.getElementId()==311) {
                fieldType="textarea";
                fieldName = "attachments";
                fieldValue = "";
                dbFieldType="varchar";
                if(null!=edocSummary && null!=edocSummary.getAttachments()){
                    fieldValue = edocSummary.getAttachments();
                }
            }
            
            else if(formElement.getElementId()==312){
                fieldType="textarea";
                branchFieldDbType ="varchar";
                fieldName = "send_department";
                fieldValue = "";
                dbFieldType="longtext";
                fieldValue=getSendDepartmentField(edocSummary,"send_department");
            } 
            
            else if(formElement.getElementId()==313){
                fieldType="textarea";
                branchFieldDbType ="varchar";
                fieldName = "send_department2";
                fieldValue = "";
                dbFieldType="longtext";
                fieldValue=getSendDepartmentField(edocSummary,"send_department2");
            }
            //------------changyi 增加附件和发文部门公文元素  end-------------------------
            else if(formElement.getElementId()==11){
                fieldName = "issuer";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getIssuer()){
                    fieldValue = edocSummary.getIssuer();
                }
            }
            else if(formElement.getElementId()==12){
                fieldName = "signing_date";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getSigningDate()){
                    fieldValue = Datetimes.formatDate(edocSummary.getSigningDate());                    
                }
            }
            else if(formElement.getElementId()==13){
                fieldType="textarea";
                fieldName = "send_to";
                fieldValue = "";
                dbFieldType="longtext";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getSendTo()){
                    if(edocSummary.getSendTo().length()>30){
                        fieldType="textarea";
                    }
                    fieldValue = edocSummary.getSendTo();
                }
            }
            else if(formElement.getElementId()==23){
                fieldType="textarea";
                fieldName = "send_to2";
                fieldValue = "";
                dbFieldType="longtext";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getSendTo2()){
                    if(edocSummary.getSendTo2().length()>30){
                        fieldType="textarea";
                    }
                    fieldValue = edocSummary.getSendTo2();
                }
            }
            else if(formElement.getElementId()==14){
                fieldType="textarea";
                fieldName = "copy_to";
                fieldValue = "";
                dbFieldType="longtext";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getCopyTo()){
                    if(edocSummary.getCopyTo().length()>30){
                        fieldType="textarea";
                    }
                    fieldValue = edocSummary.getCopyTo();
                }
            }
            else if(formElement.getElementId()==24){
                fieldType="textarea";
                fieldName = "copy_to2";
                fieldValue = "";
                dbFieldType="longtext";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getCopyTo2()){
                    if(edocSummary.getCopyTo2().length()>30){
                        fieldType="textarea";
                    }
                    fieldValue = edocSummary.getCopyTo2();
                }
            }
            else if(formElement.getElementId()==15){
                fieldType="textarea";
                fieldName = "report_to";
                fieldValue = "";
                dbFieldType="longtext";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getReportTo()){
                    if(edocSummary.getReportTo().length()>30){
                        fieldType="textarea";
                    }
                    fieldValue = edocSummary.getReportTo();
                }
            }
            else if(formElement.getElementId()==25){
                fieldType="textarea";
                fieldName = "report_to2";
                fieldValue = "";
                dbFieldType="longtext";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getReportTo2()){
                    if(edocSummary.getReportTo2().length()>30){
                        fieldType="textarea";
                    }
                    fieldValue = edocSummary.getReportTo2();
                }
            }
            else if(formElement.getElementId()==16){
                fieldName = "keyword";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getKeywords()){
                    /*xiangfan 添加，主题词多行显示！start*/
                    if(edocSummary.getKeywords().length() > 11){
                        fieldType="textarea";
                        dbFieldType="longtext";
                    }
                    /*xiangfan 添加，主题词多行显示！end*/
                    fieldValue = edocSummary.getKeywords();
                }
            }
            else if(formElement.getElementId()==17){
                fieldType="textarea";
                fieldName = "print_unit";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getPrintUnit()){
                    fieldValue = edocSummary.getPrintUnit();
                }
            }
            else if(formElement.getElementId()==18){
                fieldName = "copies";
                fieldValue = "";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getCopies()){
                    fieldValue = String.valueOf(edocSummary.getCopies());
                }
            }
            else if(formElement.getElementId()==22){
                fieldName = "copies2";
                fieldValue = "";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getCopies2()){
                    fieldValue = String.valueOf(edocSummary.getCopies2());
                }
            }
            else if(formElement.getElementId()==19){
                fieldName = "printer";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getPrinter()){
                    fieldValue = edocSummary.getPrinter();
                }
            }
            else if(formElement.getElementId()==201){
                fieldName = "createdate";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getStartTime()){
                    fieldValue = Datetimes.formatDate(edocSummary.getStartTime());
                }
            }
            else if(formElement.getElementId()==202){
                fieldName = "packdate";
                fieldValue = "";                
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getPackTime()){
                    fieldValue = Datetimes.formatDate(edocSummary.getPackTime());                   
                }
            }
            /*
             * wangwei 20110929 增加附件说明、附注、会签人、党务类型、政务类型
             */
            else if(formElement.getElementId()==320){
                fieldName = "filesm";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getFilesm()){
                    /*if(edocSummary.getFilesm().length()>30){
                        fieldType="textarea";
                        dbFieldType="longtext";                     
                    }*/
                    fieldValue = edocSummary.getFilesm();
                }
            }
            else if(formElement.getElementId()==321){
            	fieldName = "filefz";
                fieldValue = "";
                dbFieldType="varchar"; 
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getFilefz()){
                    fieldValue = edocSummary.getFilefz();
                }
            }
            //杨帆新增公文元素：联系电话
            else if(formElement.getElementId()==322){
                fieldName = "phone";
                fieldValue = "";
                dbFieldType="varchar"; 
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getPhone()){
                    fieldValue = edocSummary.getPhone();
                }
            }
            /*else if(formElement.getElementId()==324){
                fieldName = "cperson";
                fieldValue = "";
                dbFieldType="varchar";
                if(null!=edocSummary && null!=edocSummary.getCperson()){
                    if(edocSummary.getCperson().length()>30){
                        fieldType="textarea";
                        dbFieldType="longtext";                     
                    }
                    fieldValue = edocSummary.getCperson();
                }
            }*/
            else if(formElement.getElementId()==325){
                fieldType="select";
                fieldName = "party";  
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getParty())){
                    fieldValue = edocSummary.getParty();  
                }  
                
                //GOV-3222 【公文管理】-【基础数据】-【文单定义】，新建发文单，上传文单确定后出现红三角页面
                boolean required = false;
                if(formElement.isRequired()!=null){
                    required = formElement.isRequired();
                }
                
                addListStr(inputBody,fieldName,fieldValue,access,orgAccountId,required);
            }
            else if(formElement.getElementId()==326){
                fieldType="select";
                fieldName = "administrative";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getAdministrative())){
                    fieldValue = edocSummary.getAdministrative();
                }
                //GOV-3222 【公文管理】-【基础数据】-【文单定义】，新建发文单，上传文单确定后出现红三角页面
                boolean required = false;
                if(formElement.isRequired()!=null){
                    required = formElement.isRequired();
                }
                addListStr(inputBody,fieldName,fieldValue,access,orgAccountId,required);
            }
            //签收日期
            else if(formElement.getElementId()==329){
                fieldName = "receipt_date";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getReceiptDate()){
                    fieldValue = Datetimes.formatDate(edocSummary.getReceiptDate());                    
                }
            }
            //登记日期
            else if(formElement.getElementId()==330){
                fieldName = "registration_date";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getRegistrationDate()){
                    fieldValue = Datetimes.formatNoTimeZone(edocSummary.getRegistrationDate(),Datetimes.dateStyle);                    
                }
            }
            //审核人
            else if(formElement.getElementId()==331){
                fieldName = "auditor";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getAuditor()){
                    if(edocSummary.getAuditor().length()>30){
                        fieldType="textarea";
                        dbFieldType="longtext";                     
                    }
                    fieldValue = edocSummary.getAuditor();
                }
            }
            //复核人
            else if(formElement.getElementId()==332){
                fieldName = "review";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getReview()){
                    if(edocSummary.getReview().length()>30){
                        fieldType="textarea";
                        dbFieldType="longtext";                     
                    }
                    fieldValue = edocSummary.getReview();
                }
            }
            //承办人
            else if(formElement.getElementId()==333){
                fieldName = "undertaker";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getUndertaker()){
                    if(edocSummary.getUndertaker().length()>30){
                        fieldType="textarea";
                        dbFieldType="longtext";                     
                    }
                    fieldValue = edocSummary.getUndertaker();
                }
            }
            //承办机构
            else if(formElement.getElementId()==349){
                fieldName = "undertakenoffice";
                fieldValue = "";
                dbFieldType = "varchar";
                fieldType="textarea";
                if(edocSummary != null && edocSummary.getUndertakenoffice() != null){
                    if(edocSummary.getUndertakenoffice().length()>30){
                        fieldType="textarea";
                        dbFieldType="longtext";                     
                    }
                    fieldValue = edocSummary.getUndertakenoffice();
                }
            }
            
            /*
            if(formElement.getElementId()==31){
                fieldName = "shenhe";
                fieldValue = "";
            }
            if(formElement.getElementId()==32){
                fieldName = "shenpi";
                fieldValue = "";
            }
            if(formElement.getElementId()==33){
                fieldName = "huiqian";
                fieldValue = "";
            }
            if(formElement.getElementId()==34){
                fieldName = "qianfa";
                fieldValue = "";
            }
            if(formElement.getElementId()==35){
                fieldName = "fuhe";
                fieldValue = "";
            }
            if(formElement.getElementId()==36){
                fieldName = "yuedu";
                fieldValue = "";
            }
            if(formElement.getElementId()==37){
                fieldName = "niban";
                fieldValue = "";
            }
            if(formElement.getElementId()==38){
                fieldName = "piban";
                fieldValue = "";
            }
            if(formElement.getElementId()==39){
                continue;
                //fieldName = "banli";
                //fieldValue = "";
            }
            */          
            else if(formElement.getElementId()==51){
                fieldName = "string1";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getVarchar1()){
                    fieldValue = edocSummary.getVarchar1();
                }
            }
            else if(formElement.getElementId()==52){
                fieldName = "string2";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getVarchar2()){
                    fieldValue = edocSummary.getVarchar2();
                }
            }
            else if(formElement.getElementId()==53){
                fieldName = "string3";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getVarchar3()){
                    fieldValue = edocSummary.getVarchar3();
                }
            }
            else if(formElement.getElementId()==54){
                fieldName = "string4";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getVarchar4()){
                    fieldValue = edocSummary.getVarchar4();
                }
            }
            else if(formElement.getElementId()==55){
                fieldName = "string5";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getVarchar5()){
                    fieldValue = edocSummary.getVarchar5();
                }
            }
            else if(formElement.getElementId()==56){
                fieldName = "string6";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getVarchar6()){
                    fieldValue = edocSummary.getVarchar6();
                }
            }
            else if(formElement.getElementId()==57){
                fieldName = "string7";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getVarchar7()){
                    fieldValue = edocSummary.getVarchar7();
                }
            }
            else if(formElement.getElementId()==58){
                fieldName = "string8";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getVarchar8()){
                    fieldValue = edocSummary.getVarchar8();
                }
            }
            else if(formElement.getElementId()==59){
                fieldName = "string9";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getVarchar9()){
                    fieldValue = edocSummary.getVarchar9();
                }
            }
            else if(formElement.getElementId()==60){
                fieldName = "string10";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if( null!=edocSummary && null!=edocSummary.getVarchar10()){
                    fieldValue = edocSummary.getVarchar10();
                }
            }
            else if(formElement.getElementId()==241){
                fieldName = "string11";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if( null!=edocSummary && null!=edocSummary.getVarchar11()){
                    fieldValue = edocSummary.getVarchar11();
                }
            }
            else if(formElement.getElementId()==242){
                fieldName = "string12";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if( null!=edocSummary && null!=edocSummary.getVarchar12()){
                    fieldValue = edocSummary.getVarchar12();
                }
            }
            else if(formElement.getElementId()==243){
                fieldName = "string13";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if( null!=edocSummary && null!=edocSummary.getVarchar13()){
                    fieldValue = edocSummary.getVarchar13();
                }
            }
            else if(formElement.getElementId()==244){
                fieldName = "string14";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if( null!=edocSummary && null!=edocSummary.getVarchar14()){
                    fieldValue = edocSummary.getVarchar14();
                }
            }
            else if(formElement.getElementId()==245){
                fieldName = "string15";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if( null!=edocSummary && null!=edocSummary.getVarchar15()){
                    fieldValue = edocSummary.getVarchar15();
                }
            }
            else if(formElement.getElementId()==246){
                fieldName = "string16";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if( null!=edocSummary && null!=edocSummary.getVarchar16()){
                    fieldValue = edocSummary.getVarchar16();
                }
            }
            else if(formElement.getElementId()==247){
                fieldName = "string17";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if( null!=edocSummary && null!=edocSummary.getVarchar17()){
                    fieldValue = edocSummary.getVarchar17();
                }
            }
            else if(formElement.getElementId()==248){
                fieldName = "string18";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if( null!=edocSummary && null!=edocSummary.getVarchar18()){
                    fieldValue = edocSummary.getVarchar18();
                }
            }
            else if(formElement.getElementId()==249){
                fieldName = "string19";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if( null!=edocSummary && null!=edocSummary.getVarchar19()){
                    fieldValue = edocSummary.getVarchar19();
                }
            }
            else if(formElement.getElementId()==250){
                fieldName = "string20";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if( null!=edocSummary && null!=edocSummary.getVarchar20()){
                    fieldValue = edocSummary.getVarchar20();
                }
            }
            else if(formElement.getElementId()==61){
                fieldType="textarea";
                fieldName = "text1";
                dbFieldType="longtext";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getText1()){
                    fieldValue = edocSummary.getText1();
                }
            }
            else if(formElement.getElementId()==62){
                fieldType="textarea";
                fieldName = "text2";
                fieldValue = "";
                dbFieldType="longtext";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getText2()){
                    fieldValue = edocSummary.getText2();
                }
            }
            else if(formElement.getElementId()==63){
                fieldType="textarea";
                fieldName = "text3";
                fieldValue = "";
                dbFieldType="longtext";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getText3()){
                    fieldValue = edocSummary.getText3();
                }
            }
            else if(formElement.getElementId()==64){
                fieldType="textarea";
                fieldName = "text4";
                fieldValue = "";
                dbFieldType="longtext";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getText4()){
                    fieldValue = edocSummary.getText4();
                }
            }
            else if(formElement.getElementId()==65){
                fieldType="textarea";
                fieldName = "text5";
                fieldValue = "";
                dbFieldType="longtext";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getText5()){
                    fieldValue = edocSummary.getText5();
                }
            }
            else if(formElement.getElementId()==66){
                fieldType="textarea";
                fieldName = "text6";
                fieldValue = "";
                dbFieldType="longtext";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getText6()){
                    fieldValue = edocSummary.getText6();
                }
            }
            else if(formElement.getElementId()==67){
                fieldType="textarea";
                fieldName = "text7";
                fieldValue = "";
                dbFieldType="longtext";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getText7()){
                    fieldValue = edocSummary.getText7();
                }
            }
            else if(formElement.getElementId()==68){
                fieldType="textarea";
                fieldName = "text8";
                fieldValue = "";
                dbFieldType="longtext";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getText8()){
                    fieldValue = edocSummary.getText8();
                }
            }
            else if(formElement.getElementId()==69){
                fieldType="textarea";
                fieldName = "text9";
                fieldValue = "";
                dbFieldType="longtext";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getText9()){
                    fieldValue = edocSummary.getText9();
                }
            }
            else if(formElement.getElementId()==70){
                fieldType="textarea";
                fieldName = "text10";
                fieldValue = "";
                dbFieldType="longtext";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getText10()){
                    fieldValue = edocSummary.getText10();
                }
            }
            else if(formElement.getElementId()==71){
                fieldName = "integer1";
                fieldValue = "";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getInteger1()){
                    fieldValue = String.valueOf(edocSummary.getInteger1());
                }
            }
            else if(formElement.getElementId()==72){
                fieldName = "integer2";
                fieldValue = "";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getInteger2()){
                    fieldValue = String.valueOf(edocSummary.getInteger2());
                }
            }
            else if(formElement.getElementId()==73){
                fieldName = "integer3";
                fieldValue = "";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getInteger3()){
                    fieldValue = String.valueOf(edocSummary.getInteger3());
                }
            }
            else if(formElement.getElementId()==74){
                fieldName = "integer4";
                fieldValue = "";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getInteger4()){
                    fieldValue = String.valueOf(edocSummary.getInteger4());
                }
            }
            else if(formElement.getElementId()==75){
                fieldName = "integer5";
                fieldValue = "";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getInteger5()){
                    fieldValue = String.valueOf(edocSummary.getInteger5());
                }
            }
            else if(formElement.getElementId()==76){
                fieldName = "integer6";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getInteger6()){
                    fieldValue = String.valueOf(edocSummary.getInteger6());
                }
            }
            else if(formElement.getElementId()==77){
                fieldName = "integer7";
                fieldValue = "";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getInteger7()){
                    fieldValue = String.valueOf(edocSummary.getInteger7());
                }
            }
            else if(formElement.getElementId()==78){
                fieldName = "integer8";
                fieldValue = "";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getInteger8()){
                    fieldValue = String.valueOf(edocSummary.getInteger8());
                }
            }
            else if(formElement.getElementId()==79){
                fieldName = "integer9";
                fieldValue = "";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getInteger9()){
                    fieldValue = String.valueOf(edocSummary.getInteger9());
                }
            }
            else if(formElement.getElementId()==80){
                fieldName = "integer10";
                fieldValue = "";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getInteger10()){
                    fieldValue = String.valueOf(edocSummary.getInteger10());
                }
            }
            else if(formElement.getElementId()==231){
                fieldName = "integer11";
                fieldValue = "";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getInteger11()){
                    fieldValue = String.valueOf(edocSummary.getInteger11());
                }
            }
            else if(formElement.getElementId()==232){
                fieldName = "integer12";
                fieldValue = "";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getInteger12()){
                    fieldValue = String.valueOf(edocSummary.getInteger12());
                }
            }
            else if(formElement.getElementId()==233){
                fieldName = "integer13";
                fieldValue = "";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getInteger13()){
                    fieldValue = String.valueOf(edocSummary.getInteger13());
                }
            }
            else if(formElement.getElementId()==234){
                fieldName = "integer14";
                fieldValue = "";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getInteger14()){
                    fieldValue = String.valueOf(edocSummary.getInteger14());
                }
            }
            else if(formElement.getElementId()==235){
                fieldName = "integer15";
                fieldValue = "";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getInteger15()){
                    fieldValue = String.valueOf(edocSummary.getInteger15());
                }
            }
            else if(formElement.getElementId()==236){
                fieldName = "integer16";
                fieldValue = "";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getInteger16()){
                    fieldValue = String.valueOf(edocSummary.getInteger16());
                }
            }
            else if(formElement.getElementId()==237){
                fieldName = "integer17";
                fieldValue = "";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getInteger17()){
                    fieldValue = String.valueOf(edocSummary.getInteger17());
                }
            }
            else if(formElement.getElementId()==238){
                fieldName = "integer18";
                fieldValue = "";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getInteger18()){
                    fieldValue = String.valueOf(edocSummary.getInteger18());
                }
            }
            else if(formElement.getElementId()==239){
                fieldName = "integer19";
                fieldValue = "";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getInteger19()){
                    fieldValue = String.valueOf(edocSummary.getInteger19());
                }
            }
            else if(formElement.getElementId()==240){
                fieldName = "integer20";
                fieldValue = "";
                dbFieldType="int";
                branchFieldDbType ="int";
                if(null!=edocSummary && null!=edocSummary.getInteger20()){
                    fieldValue = String.valueOf(edocSummary.getInteger20());
                }
            }
            
            else if(formElement.getElementId()==81){
                fieldName = "decimal1"; 
                fieldValue = "";
                dbFieldType="decimal";
                branchFieldDbType ="decimal";
                if(null!=edocSummary && null!=edocSummary.getDecimal1()){
                    fieldValue = df.format(edocSummary.getDecimal1());
                }
            }
            else if(formElement.getElementId()==82){
                fieldName = "decimal2";
                fieldValue = "";
                dbFieldType="decimal";
                branchFieldDbType ="decimal";
                if(null!=edocSummary && null!=edocSummary.getDecimal2()){
                    fieldValue = df.format(edocSummary.getDecimal2());
                }
            }
            else if(formElement.getElementId()==83){
                fieldName = "decimal3";
                fieldValue = "";
                dbFieldType="decimal";
                branchFieldDbType ="decimal";
                if(null!=edocSummary && null!=edocSummary.getDecimal3()){
                    fieldValue = df.format(edocSummary.getDecimal3());
                }
            }
            else if(formElement.getElementId()==84){
                fieldName = "decimal4";
                fieldValue = "";
                dbFieldType="decimal";
                branchFieldDbType ="decimal";
                if(null!=edocSummary && null!=edocSummary.getDecimal4()){
                    fieldValue = df.format(edocSummary.getDecimal4());
                }
            }
            else if(formElement.getElementId()==85){
                fieldName = "decimal5";
                fieldValue = "";
                dbFieldType="decimal";
                branchFieldDbType ="decimal";
                if(null!=edocSummary && null!=edocSummary.getDecimal5()){
                    fieldValue = df.format(edocSummary.getDecimal5());
                }
            }
            else if(formElement.getElementId()==86){
                fieldName = "decimal6";
                fieldValue = "";
                dbFieldType="decimal";
                branchFieldDbType ="decimal";
                if(null!=edocSummary && null!=edocSummary.getDecimal6()){
                    fieldValue = df.format(edocSummary.getDecimal6());
                }
            }
            else if(formElement.getElementId()==87){
                fieldName = "decimal7";
                fieldValue = "";
                dbFieldType="decimal";
                branchFieldDbType ="decimal";
                if(null!=edocSummary && null!=edocSummary.getDecimal7()){
                    fieldValue = df.format(edocSummary.getDecimal7());
                }
            }
            else if(formElement.getElementId()==88){
                fieldName = "decimal8";
                fieldValue = "";
                dbFieldType="decimal";
                branchFieldDbType ="decimal";
                if(null!=edocSummary && null!=edocSummary.getDecimal8()){
                    fieldValue = df.format(edocSummary.getDecimal8());
                }
            }
            else if(formElement.getElementId()==89){
                fieldName = "decimal9";
                fieldValue = "";
                dbFieldType="decimal";
                branchFieldDbType ="decimal";
                if(null!=edocSummary && null!=edocSummary.getDecimal9()){
                    fieldValue = df.format(edocSummary.getDecimal9());
                }
            }
            else if(formElement.getElementId()==90){
                fieldName = "decimal10";
                fieldValue = "";
                dbFieldType="decimal";
                branchFieldDbType ="decimal";
                if(null!=edocSummary && null!=edocSummary.getDecimal10()){
                    fieldValue = df.format(edocSummary.getDecimal10());
                }
            }
            else if(formElement.getElementId()==251){
                fieldName = "decimal11";
                fieldValue = "";
                dbFieldType="decimal";
                branchFieldDbType ="decimal";
                if(null!=edocSummary && null!=edocSummary.getDecimal11()){
                    fieldValue = df.format(edocSummary.getDecimal11());
                }
            }
            else if(formElement.getElementId()==252){
                fieldName = "decimal12";
                fieldValue = "";
                dbFieldType="decimal";
                branchFieldDbType ="decimal";
                if(null!=edocSummary && null!=edocSummary.getDecimal12()){
                    fieldValue = df.format(edocSummary.getDecimal12());
                }
            }
            else if(formElement.getElementId()==253){
                fieldName = "decimal13";
                fieldValue = "";
                dbFieldType="decimal";
                branchFieldDbType ="decimal";
                if(null!=edocSummary && null!=edocSummary.getDecimal13()){
                    fieldValue = df.format(edocSummary.getDecimal13());
                }
            }
            else if(formElement.getElementId()==254){
                fieldName = "decimal14";
                fieldValue = "";
                dbFieldType="decimal";
                branchFieldDbType ="decimal";
                if(null!=edocSummary && null!=edocSummary.getDecimal14()){
                    fieldValue = df.format(edocSummary.getDecimal14());
                }
            }
            else if(formElement.getElementId()==255){
                fieldName = "decimal15";
                fieldValue = "";
                dbFieldType="decimal";
                branchFieldDbType ="decimal";
                if(null!=edocSummary && null!=edocSummary.getDecimal15()){
                    fieldValue = df.format(edocSummary.getDecimal15());
                }
            }
            else if(formElement.getElementId()==256){
                fieldName = "decimal16";
                fieldValue = "";
                dbFieldType="decimal";
                branchFieldDbType ="decimal";
                if(null!=edocSummary && null!=edocSummary.getDecimal16()){
                    fieldValue = df.format(edocSummary.getDecimal16());
                }
            }
            else if(formElement.getElementId()==257){
                fieldName = "decimal17";
                fieldValue = "";
                dbFieldType="decimal";
                branchFieldDbType ="decimal";
                if(null!=edocSummary && null!=edocSummary.getDecimal17()){
                    fieldValue = df.format(edocSummary.getDecimal17());
                }
            }
            else if(formElement.getElementId()==258){
                fieldName = "decimal18";
                fieldValue = "";
                dbFieldType="decimal";
                branchFieldDbType ="decimal";
                if(null!=edocSummary && null!=edocSummary.getDecimal18()){
                    fieldValue = df.format(edocSummary.getDecimal18());
                }
            }
            else if(formElement.getElementId()==259){
                fieldName = "decimal19";
                fieldValue = "";
                dbFieldType="decimal";
                branchFieldDbType ="decimal";
                if(null!=edocSummary && null!=edocSummary.getDecimal19()){
                    fieldValue = df.format(edocSummary.getDecimal19());
                }
            }
            else if(formElement.getElementId()==260){
                fieldName = "decimal20";
                fieldValue = "";
                dbFieldType="decimal";
                branchFieldDbType ="decimal";
                if(null!=edocSummary && null!=edocSummary.getDecimal20()){
                    fieldValue = df.format(edocSummary.getDecimal20());
                }
            }
            else if(formElement.getElementId()==91){
                fieldName = "date1";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getDate1()){
                    fieldValue = Datetimes.formatDate(edocSummary.getDate1());
                }
            }
            else if(formElement.getElementId()==92){
                fieldName = "date2";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getDate2()){
                    fieldValue = Datetimes.formatDate(edocSummary.getDate2());
                }
            }
            else if(formElement.getElementId()==93){
                fieldName = "date3";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getDate3()){
                    fieldValue = Datetimes.formatDate(edocSummary.getDate3());                  
                }
            }
            else if(formElement.getElementId()==94){
                fieldName = "date4";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getDate4()){
                    fieldValue = Datetimes.formatDate(edocSummary.getDate4());
                }
            }
            else if(formElement.getElementId()==95){
                fieldName = "date5";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getDate5()){
                    fieldValue = Datetimes.formatDate(edocSummary.getDate5());
                }
            }
            else if(formElement.getElementId()==96){
                fieldName = "date6";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getDate6()){
                    fieldValue = Datetimes.formatDate(edocSummary.getDate6());
                }
            }
            else if(formElement.getElementId()==97){
                fieldName = "date7";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getDate7()){
                    fieldValue = Datetimes.formatDate(edocSummary.getDate7());
                }
            }
            else if(formElement.getElementId()==98){
                fieldName = "date8";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getDate8()){
                    fieldValue = Datetimes.formatDate(edocSummary.getDate8());
                }
            }
            else if(formElement.getElementId()==99){
                fieldName = "date9";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getDate9()){
                    fieldValue = Datetimes.formatDate(edocSummary.getDate9());
                }
            }
            else if(formElement.getElementId()==100){
                fieldName = "date10";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getDate10()){
                    fieldValue = Datetimes.formatDate(edocSummary.getDate10());
                }
            }
            else if(formElement.getElementId()==271){
                fieldName = "date11";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getDate11()){
                    fieldValue = Datetimes.formatDate(edocSummary.getDate11());
                }
            }
            else if(formElement.getElementId()==272){
                fieldName = "date12";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getDate12()){
                    fieldValue = Datetimes.formatDate(edocSummary.getDate12());
                }
            }
            else if(formElement.getElementId()==273){
                fieldName = "date13";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getDate13()){
                    fieldValue = Datetimes.formatDate(edocSummary.getDate13());
                }
            }
            else if(formElement.getElementId()==274){
                fieldName = "date14";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getDate14()){
                    fieldValue = Datetimes.formatDate(edocSummary.getDate14());
                }
            }
            else if(formElement.getElementId()==275){
                fieldName = "date15";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getDate15()){
                    fieldValue = Datetimes.formatDate(edocSummary.getDate15());
                }
            }
            else if(formElement.getElementId()==276){
                fieldName = "date16";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getDate16()){
                    fieldValue = Datetimes.formatDate(edocSummary.getDate16());
                }
            }
            else if(formElement.getElementId()==277){
                fieldName = "date17";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getDate17()){
                    fieldValue = Datetimes.formatDate(edocSummary.getDate17());
                }
            }
            else if(formElement.getElementId()==278){
                fieldName = "date18";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getDate18()){
                    fieldValue = Datetimes.formatDate(edocSummary.getDate18());
                }
            }
            else if(formElement.getElementId()==279){
                fieldName = "date19";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getDate19()){
                    fieldValue = Datetimes.formatDate(edocSummary.getDate19());
                }
            }
            else if(formElement.getElementId()==280){
                fieldName = "date20";
                fieldValue = "";
                dbFieldType="date";
                branchFieldDbType ="date";
                if(null!=edocSummary && null!=edocSummary.getDate20()){
                    fieldValue = Datetimes.formatDate(edocSummary.getDate20());
                }
            }
            else if(formElement.getElementId()==101){               
                fieldType="select";
                fieldName = "list1";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getList1())){
                    fieldValue = edocSummary.getList1();
                }
                //addListStr(inputBody,fieldName,fieldValue,access,orgAccountId,formElement.isRequired());
                //OA-28678  postgre环境，公文单绑定了本单位枚举后授权给外单位，外单位调用时不能显示该枚举值  
                //传入创建文单的单位id，这样才能获取到那个单位的枚举值
                setListValueFlag = addListStr(inputBody,fieldName,fieldValue,access,formAccountId,formElement.isRequired());
            }
            else if(formElement.getElementId()==102){               
                fieldType="select";
                fieldName = "list2";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getList2())){
                    fieldValue = edocSummary.getList2();
                }
                setListValueFlag = addListStr(inputBody,fieldName,fieldValue,access,formAccountId,formElement.isRequired());
            }
            else if(formElement.getElementId()==103){               
                fieldType="select";
                fieldName = "list3";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getList3())){
                    fieldValue = edocSummary.getList3();
                }
                setListValueFlag = addListStr(inputBody,fieldName,fieldValue,access,formAccountId,formElement.isRequired());
            }
            else if(formElement.getElementId()==104){               
                fieldType="select";
                fieldName = "list4";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getList4())){
                    fieldValue = edocSummary.getList4();
                }
                setListValueFlag = addListStr(inputBody,fieldName,fieldValue,access,formAccountId,formElement.isRequired());
            }
            else if(formElement.getElementId()==105){               
                fieldType="select";
                fieldName = "list5";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getList5())){
                    fieldValue = edocSummary.getList5();
                }
                setListValueFlag = addListStr(inputBody,fieldName,fieldValue,access,formAccountId,formElement.isRequired());
            }
            else if(formElement.getElementId()==106){               
                fieldType="select";
                fieldName = "list6";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getList6())){
                    fieldValue = edocSummary.getList6();
                }
                setListValueFlag = addListStr(inputBody,fieldName,fieldValue,access,formAccountId,formElement.isRequired());
            }
            else if(formElement.getElementId()==107){               
                fieldType="select";
                fieldName = "list7";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getList7())){
                    fieldValue = edocSummary.getList7();
                }
                setListValueFlag = addListStr(inputBody,fieldName,fieldValue,access,formAccountId,formElement.isRequired());
            }
            else if(formElement.getElementId()==108){               
                fieldType="select";
                fieldName = "list8";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getList8())){
                    fieldValue = edocSummary.getList8();
                }
                setListValueFlag = addListStr(inputBody,fieldName,fieldValue,access,formAccountId,formElement.isRequired());
            }
            else if(formElement.getElementId()==109){               
                fieldType="select";
                fieldName = "list9";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getList9())){
                    fieldValue = edocSummary.getList9();
                }
                setListValueFlag = addListStr(inputBody,fieldName,fieldValue,access,formAccountId,formElement.isRequired());
            }
            else if(formElement.getElementId()==110){               
                fieldType="select";
                fieldName = "list10";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getList10())){
                    fieldValue = edocSummary.getList10();
                }
                setListValueFlag = addListStr(inputBody,fieldName,fieldValue,access,formAccountId,formElement.isRequired());
            }
            else if(formElement.getElementId()==261){               
                fieldType="select";
                fieldName = "list11";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getList11())){
                    fieldValue = edocSummary.getList11();
                }
                setListValueFlag = addListStr(inputBody,fieldName,fieldValue,access,formAccountId,formElement.isRequired());
            }
            else if(formElement.getElementId()==262){               
                fieldType="select";
                fieldName = "list12";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getList12())){
                    fieldValue = edocSummary.getList12();
                }
                setListValueFlag = addListStr(inputBody,fieldName,fieldValue,access,formAccountId,formElement.isRequired());
            }
            else if(formElement.getElementId()==263){               
                fieldType="select";
                fieldName = "list13";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getList13())){
                    fieldValue = edocSummary.getList13();
                }
                setListValueFlag = addListStr(inputBody,fieldName,fieldValue,access,formAccountId,formElement.isRequired());
            }
            else if(formElement.getElementId()==264){               
                fieldType="select";
                fieldName = "list14";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getList14())){
                    fieldValue = edocSummary.getList14();
                }
                setListValueFlag = addListStr(inputBody,fieldName,fieldValue,access,formAccountId,formElement.isRequired());
            }
            else if(formElement.getElementId()==265){               
                fieldType="select";
                fieldName = "list15";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getList15())){
                    fieldValue = edocSummary.getList15();
                }
                setListValueFlag = addListStr(inputBody,fieldName,fieldValue,access,formAccountId,formElement.isRequired());
            }
            else if(formElement.getElementId()==266){               
                fieldType="select";
                fieldName = "list16";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getList16())){
                    fieldValue = edocSummary.getList16();
                }
                setListValueFlag = addListStr(inputBody,fieldName,fieldValue,access,formAccountId,formElement.isRequired());
            }
            else if(formElement.getElementId()==267){               
                fieldType="select";
                fieldName = "list17";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getList17())){
                    fieldValue = edocSummary.getList17();
                }
                setListValueFlag = addListStr(inputBody,fieldName,fieldValue,access,formAccountId,formElement.isRequired());
            }
            else if(formElement.getElementId()==268){               
                fieldType="select";
                fieldName = "list18";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getList18())){
                    fieldValue = edocSummary.getList18();
                }
                setListValueFlag = addListStr(inputBody,fieldName,fieldValue,access,formAccountId,formElement.isRequired());
            }
            else if(formElement.getElementId()==269){               
                fieldType="select";
                fieldName = "list19";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getList19())){
                    fieldValue = edocSummary.getList19();
                }
                setListValueFlag = addListStr(inputBody,fieldName,fieldValue,access,formAccountId,formElement.isRequired());
            }
            else if(formElement.getElementId()==270){               
                fieldType="select";
                fieldName = "list20";
                branchFieldDbType ="int";
                if(null!=edocSummary && !"".equals(edocSummary.getList20())){
                    fieldValue = edocSummary.getList20();
                }
                setListValueFlag = addListStr(inputBody,fieldName,fieldValue,access,formAccountId,formElement.isRequired());
            }else if(formElement.getElementId()==291){              
                fieldName = "string21";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getVarchar21()){
                    fieldValue = edocSummary.getVarchar21();
                }
            }else if(formElement.getElementId()==292){              
                fieldName = "string22";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getVarchar22()){
                    fieldValue = edocSummary.getVarchar22();
                }
            }else if(formElement.getElementId()==293){              
                fieldName = "string23";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getVarchar23()){
                    fieldValue = edocSummary.getVarchar23();
                }
            }else if(formElement.getElementId()==294){              
                fieldName = "string24";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getVarchar24()){
                    fieldValue = edocSummary.getVarchar24();
                }
            }else if(formElement.getElementId()==295){              
                fieldName = "string25";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getVarchar25()){
                    fieldValue = edocSummary.getVarchar25();
                }
            }else if(formElement.getElementId()==296){              
                fieldName = "string26";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getVarchar26()){
                    fieldValue = edocSummary.getVarchar26();
                }
            }else if(formElement.getElementId()==297){              
                fieldName = "string27";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getVarchar27()){
                    fieldValue = edocSummary.getVarchar27();
                }
            }else if(formElement.getElementId()==298){              
                fieldName = "string28";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getVarchar28()){
                    fieldValue = edocSummary.getVarchar28();
                }
            }else if(formElement.getElementId()==299){              
                fieldName = "string29";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getVarchar29()){
                    fieldValue = edocSummary.getVarchar29();
                }
            }else if(formElement.getElementId()==300){              
                fieldName = "string30";
                fieldValue = "";
                dbFieldType="varchar";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getVarchar30()){
                    fieldValue = edocSummary.getVarchar30();
                }
            }else if(formElement.getElementId()==301){
                fieldType="textarea";
                fieldName = "text11";
                fieldValue = "";
                dbFieldType="longtext";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getText11()){
                    fieldValue = edocSummary.getText11();
                }
            }else if(formElement.getElementId()==302){
                fieldType="textarea";
                fieldName = "text12";
                fieldValue = "";
                dbFieldType="longtext";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getText12()){
                    fieldValue = edocSummary.getText12();
                }
            }else if(formElement.getElementId()==303){
                fieldType="textarea";
                fieldName = "text13";
                fieldValue = "";
                dbFieldType="longtext";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getText13()){
                    fieldValue = edocSummary.getText13();
                }
            }else if(formElement.getElementId()==304){
                fieldType="textarea";
                fieldName = "text14";
                fieldValue = "";
                dbFieldType="longtext";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getText14()){
                    fieldValue = edocSummary.getText14();
                }
            }else if(formElement.getElementId()==305){
                fieldType="textarea";
                fieldName = "text15";
                fieldValue = "";
                dbFieldType="longtext";
                branchFieldDbType ="varchar";
                if(null!=edocSummary && null!=edocSummary.getText15()){
                    fieldValue = edocSummary.getText15();
                }
            }
            /*
            if(formElement.getElementId()==102){
                fieldType="select";
                fieldName = "list2";
                if(null!=edocSummary && !"".equals(edocSummary.getList2())){
                    fieldValue = edocSummary.getList2();
                }
                EdocElement element = edocElementManager.getByFieldName(fieldName);
                
                if(null!=element && element.getStatus()!=0){
                Long metadataId = element.getMetadataId();
                if(null!=metadataId){
                Metadata metadata = metadataManager.getMetadata(metadataId);
                metaItem =  metadataManager.getMetadataItems(metadata.getName());
                inputBody.append(fieldInput).append(domainName).append(fieldName).append(select);
                    
                    for(int x=0;x<metaItem.size();x++){
                            metaDataItem = (MetadataItem)metaItem.get(x);
                            name = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", metaDataItem.getLabel());
                            if(null!=edocSummary && !"".equals(edocSummary.getList2())&& (metaDataItem.getValue()).equals(edocSummary.getList2())){
                                inputBody.append(display).append(name).append(displayValue).append(metaDataItem.getValue()).append("\" select=\"true\" ").append("/>");                             
                            }else{
                                inputBody.append(display).append(name).append(displayValue).append(metaDataItem.getValue()).append(displayEnd);
                            }
                        }
                    inputBody.append(fieldInputEnd);
                    }
                }
            }
            if(formElement.getElementId()==103){
                fieldType="select";
                fieldName = "list3";
                if(null!=edocSummary && !"".equals(edocSummary.getList3())){
                    fieldValue = edocSummary.getList3();
                }
                EdocElement element = edocElementManager.getByFieldName(fieldName);
                
                if(null!=element && element.getStatus()!=0){
                Long metadataId = element.getMetadataId();
                if(null!=metadataId){
                Metadata metadata = metadataManager.getMetadata(metadataId);
                metaItem =  metadataManager.getMetadataItems(metadata.getName());
                inputBody.append(fieldInput).append(domainName).append(fieldName).append(select);
                    
                    for(int x=0;x<metaItem.size();x++){
                            metaDataItem = (MetadataItem)metaItem.get(x);
                            name = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", metaDataItem.getLabel());
                            if(null!=edocSummary && !"".equals(edocSummary.getList3())&& (metaDataItem.getValue()).equals(edocSummary.getList3())){
                                inputBody.append(display).append(name).append(displayValue).append(metaDataItem.getValue()).append("\" select=\"true\" ").append("/>");                             
                            }else{
                                inputBody.append(display).append(name).append(displayValue).append(metaDataItem.getValue()).append(displayEnd);
                            }
                        }
                    inputBody.append(fieldInputEnd);
                    }
                }
            }
            if(formElement.getElementId()==104){
                fieldType="select";
                fieldName = "list4";
                if(null!=edocSummary && !"".equals(edocSummary.getList4())){
                    fieldValue = edocSummary.getList4();
                }
                EdocElement element = edocElementManager.getByFieldName(fieldName);
                
                if(null!=element && element.getStatus()!=0){
                Long metadataId = element.getMetadataId();
                if(null!=metadataId){
                Metadata metadata = metadataManager.getMetadata(metadataId);
                metaItem =  metadataManager.getMetadataItems(metadata.getName());
                inputBody.append(fieldInput).append(domainName).append(fieldName).append(select);
                    
                    for(int x=0;x<metaItem.size();x++){
                            metaDataItem = (MetadataItem)metaItem.get(x);
                            name = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", metaDataItem.getLabel());
                            if(null!=edocSummary && !"".equals(edocSummary.getList4())&& (metaDataItem.getValue()).equals(edocSummary.getList4())){
                                inputBody.append(display).append(name).append(displayValue).append(metaDataItem.getValue()).append("\" select=\"true\" ").append("/>");                             
                            }else{
                                inputBody.append(display).append(name).append(displayValue).append(metaDataItem.getValue()).append(displayEnd);
                            }
                        }
                    inputBody.append(fieldInputEnd);
                }
                }
            }
            if(formElement.getElementId()==105){
                fieldType="select";
                fieldName = "list5";
                if(null!=edocSummary && !"".equals(edocSummary.getList5())){
                    fieldValue = edocSummary.getList5();
                }
                EdocElement element = edocElementManager.getByFieldName(fieldName);
                
                if(null!=element && element.getStatus()!=0){
                Long metadataId = element.getMetadataId();
                if(null!=metadataId){
                Metadata metadata = metadataManager.getMetadata(metadataId);
                metaItem =  metadataManager.getMetadataItems(metadata.getName());
                inputBody.append(fieldInput).append(domainName).append(fieldName).append(select);
                    
                    for(int x=0;x<metaItem.size();x++){
                            metaDataItem = (MetadataItem)metaItem.get(x);
                            name = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", metaDataItem.getLabel());
                            if(null!=edocSummary && !"".equals(edocSummary.getList5())&& (metaDataItem.getValue()).equals(edocSummary.getList5())){
                                inputBody.append(display).append(name).append(displayValue).append(metaDataItem.getValue()).append("\" select=\"true\" ").append("/>");                             
                            }else{
                                inputBody.append(display).append(name).append(displayValue).append(metaDataItem.getValue()).append(displayEnd);
                            }
                        }
                    inputBody.append(fieldInputEnd);
                }
                }
            }
            if(formElement.getElementId()==106){
                fieldType="select";
                fieldName = "list6";
                if(null!=edocSummary && !"".equals(edocSummary.getList6())){
                    fieldValue = edocSummary.getList6();
                }
                EdocElement element = edocElementManager.getByFieldName(fieldName);
                
                if(null!=element && element.getStatus()!=0){
                Long metadataId = element.getMetadataId();
                if(null!=metadataId){
                Metadata metadata = metadataManager.getMetadata(metadataId);
                metaItem =  metadataManager.getMetadataItems(metadata.getName());
                inputBody.append(fieldInput).append(domainName).append(fieldName).append(select);
                    
                    for(int x=0;x<metaItem.size();x++){
                            metaDataItem = (MetadataItem)metaItem.get(x);
                            name = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", metaDataItem.getLabel());
                            if(null!=edocSummary && !"".equals(edocSummary.getList6())&& (metaDataItem.getValue()).equals(edocSummary.getList6())){
                                inputBody.append(display).append(name).append(displayValue).append(metaDataItem.getValue()).append("\" select=\"true\" ").append("/>");                             
                            }else{
                                inputBody.append(display).append(name).append(displayValue).append(metaDataItem.getValue()).append(displayEnd);
                            }
                        }
                    inputBody.append(fieldInputEnd);
                }
                }
            }
            if(formElement.getElementId()==107){
                fieldType="select";
                fieldName = "list7";
                if(null!=edocSummary && !"".equals(edocSummary.getList7())){
                    fieldValue = edocSummary.getList7();
                }
                EdocElement element = edocElementManager.getByFieldName(fieldName);
                
                if(null!=element && element.getStatus()!=0){
                Long metadataId = element.getMetadataId();
                if(null!=metadataId){
                Metadata metadata = metadataManager.getMetadata(metadataId);
                metaItem =  metadataManager.getMetadataItems(metadata.getName());
                inputBody.append(fieldInput).append(domainName).append(fieldName).append(select);
                    
                    for(int x=0;x<metaItem.size();x++){
                            metaDataItem = (MetadataItem)metaItem.get(x);
                            name = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", metaDataItem.getLabel());
                            if(null!=edocSummary && !"".equals(edocSummary.getList7())&& (metaDataItem.getValue()).equals(edocSummary.getList7())){
                                inputBody.append(display).append(name).append(displayValue).append(metaDataItem.getValue()).append("\" select=\"true\" ").append("/>");                             
                            }else{
                                inputBody.append(display).append(name).append(displayValue).append(metaDataItem.getValue()).append(displayEnd);
                            }
                        }
                    inputBody.append(fieldInputEnd);
                }
                }
            }
            if(formElement.getElementId()==108){
                fieldType="select";
                fieldName = "list8";
                if(null!=edocSummary && !"".equals(edocSummary.getList8())){
                    fieldValue = edocSummary.getList8();
                }
                EdocElement element = edocElementManager.getByFieldName(fieldName);
                
                if(null!=element && element.getStatus()!=0){
                Long metadataId = element.getMetadataId();
                if(null!=metadataId){
                Metadata metadata = metadataManager.getMetadata(metadataId);
                metaItem =  metadataManager.getMetadataItems(metadata.getName());
                inputBody.append(fieldInput).append(domainName).append(fieldName).append(select);
                    
                    for(int x=0;x<metaItem.size();x++){
                            metaDataItem = (MetadataItem)metaItem.get(x);
                            name = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", metaDataItem.getLabel());
                            if(null!=edocSummary && !"".equals(edocSummary.getList8())&& (metaDataItem.getValue()).equals(edocSummary.getList8())){
                                inputBody.append(display).append(name).append(displayValue).append(metaDataItem.getValue()).append("\" select=\"true\" ").append("/>");                             
                            }else{
                                inputBody.append(display).append(name).append(displayValue).append(metaDataItem.getValue()).append(displayEnd);
                            }
                        }
                    inputBody.append(fieldInputEnd);
                }
                }
            }
            if(formElement.getElementId()==109){
                fieldType="select";
                fieldName = "list9";
                if(null!=edocSummary && !"".equals(edocSummary.getList9())){
                    fieldValue = edocSummary.getList9();
                }
                EdocElement element = edocElementManager.getByFieldName(fieldName);
                
                if(null!=element && element.getStatus()!=0){
                Long metadataId = element.getMetadataId();
                if(null!=metadataId){
                Metadata metadata = metadataManager.getMetadata(metadataId);
                metaItem =  metadataManager.getMetadataItems(metadata.getName());
                inputBody.append(fieldInput).append(domainName).append(fieldName).append(select);
                    
                    for(int x=0;x<metaItem.size();x++){
                            metaDataItem = (MetadataItem)metaItem.get(x);
                            name = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", metaDataItem.getLabel());
                            if(null!=edocSummary && !"".equals(edocSummary.getList9())&& (metaDataItem.getValue()).equals(edocSummary.getList9())){
                                inputBody.append(display).append(name).append(displayValue).append(metaDataItem.getValue()).append("\" select=\"true\" ").append("/>");                             
                            }else{
                                inputBody.append(display).append(name).append(displayValue).append(metaDataItem.getValue()).append(displayEnd);
                            }
                        }
                    inputBody.append(fieldInputEnd);
                }
                }
            }
            if(formElement.getElementId()==110){
                fieldType="select";
                fieldName = "list10";
                if(null!=edocSummary && !"".equals(edocSummary.getList10())){
                    fieldValue = edocSummary.getList10();
                }

            }
            */  
            if(!StringUtils.isBlank(fieldName)){
                if(fieldValue==null){fieldValue="";}       
                xmlBody.append(leftBracket);
                xmlBody.append(domainName);
                xmlBody.append(fieldName);
                xmlBody.append(rightBracket);
                /*
                if("textarea".equals(fieldType))
                {
                    fieldValue=fieldValue.replaceAll("\r\n", "\\\\n");
                }
                */
                //多行文本中的回车特殊处理
                if("textarea".equals(fieldType)){
                    xmlBody.append(Strings.toHTML(Strings.toXmlStr(fieldValue),false).replaceAll("<br/>", "&amp;lt;br&amp;gt;"));
                } else if("select".equals(fieldType) && !setListValueFlag){
                    setSummaryFieldEmptyValue(edocSummary, fieldName);
                } else {
                    xmlBody.append(Strings.toHTML(Strings.toXmlStr(fieldValue),false));
                }
                xmlBody.append(leftBracket);
                xmlBody.append(bias);
                xmlBody.append(domainName);
                xmlBody.append(fieldName);
                xmlBody.append(rightBracket);
                //增加数据库字段类型属性，用于数据校验                
                //下拉列表已经在上面单独初始化
                if("text".equals(fieldType))
                {
                    inputBody.append(fieldInput).append(domainName).append(fieldName)
                    .append("\" fieldtype=\"").append(dbFieldType).append("\" branchFieldDbType=\"").append(branchFieldDbType)
                    .append(text);
                }
                else if("textarea".equals(fieldType))
                {
                    inputBody.append(fieldInput).append(domainName).append(fieldName)
                    .append("\" fieldtype=\"").append(dbFieldType).append("\" branchFieldDbType=\"").append(branchFieldDbType)
                    .append(textarea);                  
                }else{
                    //used temporarily, optimize in the future
                    inputBody.append(fieldInput).append(domainName).append(fieldName).append("\" branchFieldDbType =\"").append(branchFieldDbType).append(text);
                }   
            }
        }
        xml.append(topHeader).append(xmlHeader).append(bodyHeader).append(xmlBody).append(bodyEnder);
        xml.append(inputHeader).append(fieldStart).append(inputBody).append(fieldEnd);
        
        return xml;
        
        /*
        StringBuffer xml = new StringBuffer("");
        String topHeader = "&&&&&&&&  xsl_start  &&&&&&&& Url=view1.xsl &&&&&&&&  data_start  &&&&&&&&";
        String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        String bodyHeader = "<my:myFields xmlns:my=\"www.seeyon.com/form/2007\">";
        String bodyEnder   = "</my:myFields>";
        
        String xmlBody = "<my:shenhe>同意</my:shenhe><my:printer>张华</my:printer><my:keyword>工资上调15%</my:keyword><my:print_unit>工商银行</my:print_unit><my:copies>10</my:copies><my:issuer>张总</my:issuer><my:send_unit>新华社</my:send_unit><my:copy_to>用友上海公司</my:copy_to><my:report_to>用友北京公司</my:report_to><my:create_user>张华</my:create_user><my:urgent_level/><my:secret_level/><my:doc_mark/><my:serial_no/><my:send_type/><my:doc_type/><my:subject>关于工资上调的通知</my:subject><my:send_to>新华社</my:send_to><my:keep_period/>";
        
        String inputHeader = "&&&&&&&&  input_start  &&&&&&&&";
        String fieldStart = "<FieldInputList>";
        String fieldEnd   = "</FieldInputList>";
        
        xml.append(topHeader).append(xmlHeader).append(bodyHeader).append(xmlBody).append(bodyEnder);
        
        String inputBody = "<FieldInput name=\"my:subject\" type=\"text\" access=\"edit\" allowprint=\"true\" allowtransmit=\"true\" />"+
        "<FieldInput name=\"my:doc_type\" type=\"select\" access=\"edit\" allowprint=\"true\" allowtransmit=\"true\">"+
            "<Input display=\"公报\" value=\"1\"/>"+
            "<Input display=\"决议\" value=\"2\"/>"+
            "<Input display=\"决定\" value=\"3\"/>"+
            "<Input display=\"指示\" value=\"4\"/>"+
            "<Input display=\"条例\" value=\"5\"/>"+
            "<Input display=\"规定\" value=\"6\"/>"+
            "<Input display=\"通知\" value=\"7\"/>"+
            "<Input display=\"通报\" value=\"8\"/>"+
            "<Input display=\"请示\" value=\"9\"/>"+
            "<Input display=\"报告\" value=\"11\"/>"+
            "<Input display=\"批复\" value=\"12\"/>"+
            "<Input display=\"会议记要\" value=\"13\"/>"+
            "<Input display=\"函\" value=\"14\"/>"+
            "<Input display=\"签报\" value=\"15\"/>"+
            "<Input display=\"电传明文\" value=\"16\"/>"+
        "</FieldInput>"+
        "<FieldInput name=\"my:send_type\" type=\"select\" access=\"edit\" allowprint=\"true\" allowtransmit=\"true\">"+
            "<Input display=\"上行文\" value=\"1\"/>"+
            "<Input display=\"下行文\" value=\"2\"/>"+
            "<Input display=\"平行文\" value=\"3\"/>"+
            "<Input display=\"内部行文\" value=\"4\"/>"+
            "<Input display=\"外部行文\" value=\"5\"/>"+
        "</FieldInput>"+
        "<FieldInput name=\"my:doc_mark\" type=\"select\" access=\"edit\" allowprint=\"true\" allowtransmit=\"true\">"+
                "<Input display=\"请选择公文文号\" value=\"\"/>"+
        "</FieldInput>"+
        "<FieldInput name=\"my:serial_no\" type=\"text\" access=\"edit\" allowprint=\"true\" allowtransmit=\"true\" />"+
        "<FieldInput name=\"my:secret_level\" type=\"select\" access=\"edit\" allowprint=\"true\" allowtransmit=\"true\">"+
            "<Input display=\"普通\" value=\"1\"/>"+
            "<Input display=\"秘密\" value=\"2\"/>"+
            "<Input display=\"机密\" value=\"3\"/>"+
            "<Input display=\"绝密\" value=\"4\"/>"+
        "</FieldInput>"+
        "<FieldInput name=\"my:urgent_level\" type=\"select\" access=\"edit\" allowprint=\"true\" allowtransmit=\"true\">"+
            "<Input display=\"普通\" value=\"1\"/>"+
            "<Input display=\"紧急\" value=\"2\"/>"+
            "<Input display=\"特急\" value=\"3\"/>"+
        "</FieldInput>"+
        "<FieldInput name=\"my:create_user\" type=\"text\" access=\"edit\" allowprint=\"true\" allowtransmit=\"true\" />"+
        "<FieldInput name=\"my:keep_period\" type=\"select\" access=\"edit\" allowprint=\"true\" allowtransmit=\"true\">"+
            "<Input display=\"无期限\" value=\"1\"/>"+
            "<Input display=\"1天\" value=\"2\"/>"+
            "<Input display=\"3天\" value=\"3\"/>"+
            "<Input display=\"5天\" value=\"5\"/>"+
            "<Input display=\"7天\" value=\"7\"/>"+
            "<Input display=\"10天\" value=\"10\"/>"+
            "<Input display=\"15天\" value=\"15\"/>"+
            "<Input display=\"1月\" value=\"30\"/>"+
            "<Input display=\"2月\" value=\"60\"/>"+
            "<Input display=\"3月\" value=\"90\"/>"+
            "<Input display=\"6月\" value=\"180\"/>"+
            "<Input display=\"9月\" value=\"270\"/>"+
            "<Input display=\"1年\" value=\"365\"/>"+
            "<Input display=\"2年\" value=\"730\"/>"+
            "<Input display=\"3年\" value=\"1095\"/>"+
            "<Input display=\"5年\" value=\"1825\"/>"+
            "<Input display=\"8年\" value=\"2920\"/>"+
            "<Input display=\"10年\" value=\"3650\"/>"+
            "<Input display=\"15年\" value=\"5475\"/>"+
            "<Input display=\"20年\" value=\"7300\"/>"+
            "<Input display=\"30年\" value=\"10950\"/>"+
        "</FieldInput>"+
        "<FieldInput name=\"my:send_to\" type=\"text\" access=\"edit\" allowprint=\"true\" allowtransmit=\"true\" />"+
        "<FieldInput name=\"my:report_to\" type=\"text\" access=\"edit\" allowprint=\"true\" allowtransmit=\"true\" />"+
        "<FieldInput name=\"my:copy_to\" type=\"text\" access=\"edit\" allowprint=\"true\" allowtransmit=\"true\" />"+
        "<FieldInput name=\"my:send_unit\" type=\"text\" access=\"edit\" allowprint=\"true\" allowtransmit=\"true\" />"+
        "<FieldInput name=\"my:issuer\" type=\"text\" access=\"edit\" allowprint=\"true\" allowtransmit=\"true\" />"+
        "<FieldInput name=\"my:print_unit\" type=\"text\" access=\"edit\" allowprint=\"true\" allowtransmit=\"true\" />"+
        "<FieldInput name=\"my:print_unit\" type=\"text\" access=\"edit\" allowprint=\"true\" allowtransmit=\"true\" />"+
        "<FieldInput name=\"my:keyword\" type=\"text\" access=\"edit\" allowprint=\"true\" allowtransmit=\"true\" />"+
        "<FieldInput name=\"my:printer\" type=\"text\" access=\"edit\" allowprint=\"true\" allowtransmit=\"true\" />"+
        "<FieldInput name=\"my:shenhe\" type=\"text\" access=\"edit\" allowprint=\"true\" allowtransmit=\"true\" />"+
        "<FieldInput name=\"my:copies\" type=\"text\" access=\"edit\" allowprint=\"true\" allowtransmit=\"true\" />";

        xml.append(inputHeader).append(fieldStart).append(inputBody).append(fieldEnd);

        return xml;
        */
    }
    
    /**
     * 
     * 解析公文表单字段与值的映射
     * 
     * @Author      : xuqw
     * @Date        : 2015年5月18日下午5:30:40
     * @param elements
     * @param edocSummary
     * @return
     */
    public Map<String, String[]> field2ValMap(long formId, EdocSummary edocSummary,Long actorId) {
        List <EdocFormElement> elements =  edocFormManager.getEdocFormElementByFormId(formId);
        // 保存字段到值的映射
        Map<String, String[]> field2ValMap = new HashMap<String, String[]>();

        Long orgAccountId = null;
        if (edocSummary != null) {
            orgAccountId = edocSummary.getOrgAccountId();
        }

        Long formAccountId = null;
        DecimalFormat df = new DecimalFormat("###########0.####");

        // 动态拼接XML第一部分,如果是文本框,在第一部分赋值,如果类型为下拉列表,在第二部分赋值
        EdocElementFlowPermAcl elementAcl=null;
        
        Hashtable<Long,EdocElementFlowPermAcl> actorsAcc = null;
        if(null != actorId && -1L != actorId){
            actorsAcc = edocElementFlowPermAclManager.getEdocElementFlowPermAclsHs(actorId);
        }
        
        for (int i = 0; i < elements.size(); i++) {

            // 元素默认值区域
            String fieldName = "";
            String fieldType = "text";
            String fieldValue = "";

            // 循环下的内容
            EdocFormElement formElement = elements.get(i);

            if (i == 0) {
                if (formElement.getFormId() != null) {
                    EdocForm edocForm = edocFormManager.getEdocForm(formElement.getFormId());
                    formAccountId = edocForm.getDomainId();
                }
            }

            // 判断是否为处理意见
            if ((formElement.getElementId() >= 203 && formElement.getElementId() <= 207)
                    || (formElement.getElementId() >= 281 && formElement.getElementId() <= 290)) {
                continue;
            }
            String access="browse";
            String required = "";
            if(null != actorId && -1L != actorId){
                //权限actorId<0时,对公文元素的操作权限默认为编辑,用于建立模版
                //公文单中的元素没用早到对应的权限,默认为只读
                elementAcl=actorsAcc.get(formElement.getElementId());
                if((actorId<0 && actorId>-100) || (elementAcl!=null && elementAcl.getAccess()==EdocElementFlowPermAcl.ACCESS_STATE.edit.ordinal())){
                    access=EdocElementFlowPermAcl.ACCESS_STATE.edit.name();
                }
                //doc_mark,doc_mark2,serial_no
                if(formElement.getElementId()==4 || formElement.getElementId()==5 || formElement.getElementId()==21) {
                    if(edocSummary!=null && edocSummary.getFinished() && "archived".equals(edocSummary.getFrom())) {
                        access = EdocElementFlowPermAcl.ACCESS_STATE.read.name();
                    }
                }
               required = formElement.isRequired().toString();
            }
            
            if (formElement.getElementId() == 1) {

                fieldName = "subject";
                fieldType = "textarea"; // xiangfan 添加 修复 GOV-4372标题多行问题
                fieldValue = edocSummary.getSubject();

            } else if (formElement.getElementId() == 2) {
                fieldType = "select";
                fieldName = "doc_type";
                fieldValue = edocSummary.getDocType();

                if (Strings.isNotEmpty(fieldValue)) {
                    CtpEnumItem item = enumManagerNew.getEnumItem(EnumNameEnum.edoc_doc_type, fieldValue);
                    if (item != null) {
                        fieldValue = ResourceUtil.getString(item.getLabel());
                    }
                }
            } else if (formElement.getElementId() == 3) {
                fieldType = "select";
                fieldName = "send_type";
                fieldValue = edocSummary.getSendType();

                if (Strings.isNotEmpty(fieldValue)) {
                    CtpEnumItem item = enumManagerNew.getEnumItem(EnumNameEnum.edoc_send_type, fieldValue);
                    if (item != null) {
                        fieldValue = ResourceUtil.getString(item.getLabel());
                    }
                }

            } else if (formElement.getElementId() == 4) {
                fieldName = "doc_mark";
                fieldValue = edocSummary.getDocMark();

            } else if (formElement.getElementId() == 21) {
                fieldName = "doc_mark2";
                fieldValue = edocSummary.getDocMark2();

            } else if (formElement.getElementId() == 5) {
                fieldName = "serial_no";
                fieldValue = edocSummary.getSerialNo();
            } else if (formElement.getElementId() == 6) {
                fieldType = "select";
                fieldName = "secret_level";
                fieldValue = edocSummary.getSecretLevel();

                if (Strings.isNotEmpty(fieldValue)) {
                    CtpEnumItem item = enumManagerNew.getEnumItem(EnumNameEnum.edoc_secret_level, fieldValue);
                    if (item != null) {
                        fieldValue = ResourceUtil.getString(item.getLabel());
                    }
                }

            } else if (formElement.getElementId() == 7) {
                fieldType = "select";
                fieldName = "urgent_level";
                fieldValue = edocSummary.getUrgentLevel();

                if (Strings.isNotEmpty(fieldValue)) {
                    CtpEnumItem item = enumManagerNew.getEnumItem(EnumNameEnum.edoc_urgent_level, fieldValue);
                    if (item != null) {
                        fieldValue = ResourceUtil.getString(item.getLabel());
                    }
                }
            } else if (formElement.getElementId() == 350) {// 公文级别
                fieldType = "select";
                fieldName = "unit_level";
                fieldValue = edocSummary.getUnitLevel();
                if (Strings.isNotEmpty(fieldValue)) {
                    CtpEnumItem item = enumManagerNew.getEnumItem(EnumNameEnum.edoc_unit_level, fieldValue);
                    if (item != null) {
                        fieldValue = ResourceUtil.getString(item.getLabel());
                    }
                }
            } else if (formElement.getElementId() == 8) {
                fieldType = "select";
                fieldName = "keep_period";
                Integer value = edocSummary.getKeepPeriod();
                if (value != null) {
                    fieldValue = value.toString();
                    CtpEnumItem item = enumManagerNew.getEnumItem(EnumNameEnum.edoc_keep_period, fieldValue);
                    if (item != null) {
                        fieldValue = ResourceUtil.getString(item.getLabel());
                    }
                }
            } else if (formElement.getElementId() == 9) {
                fieldName = "create_person";
                fieldValue = edocSummary.getCreatePerson();
            } else if (formElement.getElementId() == 10) {
                fieldName = "send_unit";
                fieldType = "textarea";
                fieldValue = edocSummary.getSendUnit();
            } else if (formElement.getElementId() == 26) {
                fieldName = "send_unit2";
                fieldType = "textarea";
                fieldValue = edocSummary.getSendUnit2();
            } else if (formElement.getElementId() == 311) {
                fieldType = "textarea";
                fieldName = "attachments";
                fieldValue = edocSummary.getAttachments();
            }

            else if (formElement.getElementId() == 312) {
                fieldType = "textarea";
                fieldName = "send_department";
                fieldValue = getSendDepartmentField(edocSummary, "send_department");
            }

            else if (formElement.getElementId() == 313) {
                fieldType = "textarea";
                fieldName = "send_department2";
                fieldValue = getSendDepartmentField(edocSummary, "send_department2");
            } else if (formElement.getElementId() == 11) {
                fieldName = "issuer";
                fieldValue = edocSummary.getIssuer();
            } else if (formElement.getElementId() == 12) {
                fieldName = "signing_date";
                fieldValue = formatDate(edocSummary.getSigningDate());
            } else if (formElement.getElementId() == 13) {
                fieldType = "textarea";
                fieldName = "send_to";
                fieldValue = edocSummary.getSendTo();
            } else if (formElement.getElementId() == 23) {
                fieldType = "textarea";
                fieldName = "send_to2";
                fieldValue = edocSummary.getSendTo2();
            } else if (formElement.getElementId() == 14) {
                fieldType = "textarea";
                fieldName = "copy_to";
                fieldValue = edocSummary.getCopyTo();
            } else if (formElement.getElementId() == 24) {
                fieldType = "textarea";
                fieldName = "copy_to2";
                fieldValue = edocSummary.getCopyTo2();
            } else if (formElement.getElementId() == 15) {
                fieldType = "textarea";
                fieldName = "report_to";
                fieldValue = edocSummary.getReportTo();
            } else if (formElement.getElementId() == 25) {
                fieldType = "textarea";
                fieldName = "report_to2";
                fieldValue = edocSummary.getReportTo2();
            } else if (formElement.getElementId() == 16) {
                fieldName = "keyword";
                fieldType = "textarea";
                fieldValue = edocSummary.getKeywords();
            } else if (formElement.getElementId() == 17) {
                fieldType = "textarea";
                fieldName = "print_unit";
                fieldValue = edocSummary.getPrintUnit();
            } else if (formElement.getElementId() == 18) {
                fieldName = "copies";
                if(edocSummary.getCopies() != null){
                    fieldValue = edocSummary.getCopies().toString();
                }
                
            } else if (formElement.getElementId() == 22) {
                fieldName = "copies2";
                if(edocSummary.getCopies2() != null){
                    fieldValue = edocSummary.getCopies2().toString();
                }
            } else if (formElement.getElementId() == 19) {
                fieldName = "printer";
                fieldValue = edocSummary.getPrinter();
            } else if (formElement.getElementId() == 201) {
                fieldName = "createdate";
                fieldValue = formatDate(edocSummary.getStartTime());
            } else if (formElement.getElementId() == 202) {
                fieldName = "packdate";
                fieldValue = formatDate(edocSummary.getPackTime());
            } else if (formElement.getElementId() == 320) {
                fieldName = "filesm";
                //fieldType = "textarea";
                fieldType = "varchar";
                fieldValue = edocSummary.getFilesm();
            } else if (formElement.getElementId() == 321) {
                fieldName = "filefz";
                fieldValue = edocSummary.getFilefz();
            }
            // 杨帆新增公文元素：联系电话
            else if (formElement.getElementId() == 322) {
                fieldName = "phone";
                fieldValue = edocSummary.getPhone();
            } else if (formElement.getElementId() == 325) {
                fieldType = "select";
                fieldName = "party";
                fieldValue = edocSummary.getParty();

                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, orgAccountId);
                }

            } else if (formElement.getElementId() == 326) {
                fieldType = "select";
                fieldName = "administrative";
                fieldValue = edocSummary.getAdministrative();
                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, orgAccountId);
                }
            }
            // 签收日期
            else if (formElement.getElementId() == 329) {
                fieldName = "receipt_date";
                fieldValue = formatDate(edocSummary.getReceiptDate());
            }
            // 登记日期
            else if (formElement.getElementId() == 330) {
                fieldName = "registration_date";
                fieldValue = formatDate(edocSummary.getRegistrationDate());
            }
            // 审核人
            else if (formElement.getElementId() == 331) {
                fieldName = "auditor";
                fieldType = "textarea";
                fieldValue = edocSummary.getAuditor();
            }
            // 复核人
            else if (formElement.getElementId() == 332) {
                fieldName = "review";
                fieldType = "textarea";
                fieldValue = edocSummary.getReview();
            }
            // 承办人
            else if (formElement.getElementId() == 333) {
                fieldName = "undertaker";
                fieldType = "textarea";
                fieldValue = edocSummary.getUndertaker();
            }
            // 承办机构
            else if (formElement.getElementId() == 349) {
                fieldName = "undertakenoffice";
                fieldType = "textarea";
                fieldValue = edocSummary.getUndertakenoffice();
            }

            else if (formElement.getElementId() == 51) {
                fieldName = "string1";
                fieldValue = edocSummary.getVarchar1();
            } else if (formElement.getElementId() == 52) {
                fieldName = "string2";
                fieldValue = edocSummary.getVarchar2();
            } else if (formElement.getElementId() == 53) {
                fieldName = "string3";
                fieldValue = edocSummary.getVarchar3();
            } else if (formElement.getElementId() == 54) {
                fieldName = "string4";
                fieldValue = edocSummary.getVarchar4();
            } else if (formElement.getElementId() == 55) {
                fieldName = "string5";
                fieldValue = edocSummary.getVarchar5();
            } else if (formElement.getElementId() == 56) {
                fieldName = "string6";
                fieldValue = edocSummary.getVarchar6();
            } else if (formElement.getElementId() == 57) {
                fieldName = "string7";
                fieldValue = edocSummary.getVarchar7();
            } else if (formElement.getElementId() == 58) {
                fieldName = "string8";
                fieldValue = edocSummary.getVarchar8();
            } else if (formElement.getElementId() == 59) {
                fieldName = "string9";
                fieldValue = edocSummary.getVarchar9();
            } else if (formElement.getElementId() == 60) {
                fieldName = "string10";
                fieldValue = edocSummary.getVarchar10();
            } else if (formElement.getElementId() == 241) {
                fieldName = "string11";
                fieldValue = edocSummary.getVarchar11();
            } else if (formElement.getElementId() == 242) {
                fieldName = "string12";
                fieldValue = edocSummary.getVarchar12();
            } else if (formElement.getElementId() == 243) {
                fieldName = "string13";
                fieldValue = edocSummary.getVarchar13();
            } else if (formElement.getElementId() == 244) {
                fieldName = "string14";
                fieldValue = edocSummary.getVarchar14();
            } else if (formElement.getElementId() == 245) {
                fieldName = "string15";
                fieldValue = edocSummary.getVarchar15();
            } else if (formElement.getElementId() == 246) {
                fieldName = "string16";
                fieldValue = edocSummary.getVarchar16();
            } else if (formElement.getElementId() == 247) {
                fieldName = "string17";
                fieldValue = edocSummary.getVarchar17();
            } else if (formElement.getElementId() == 248) {
                fieldName = "string18";
                fieldValue = edocSummary.getVarchar18();
            } else if (formElement.getElementId() == 249) {
                fieldName = "string19";
                fieldValue = edocSummary.getVarchar19();
            } else if (formElement.getElementId() == 250) {
                fieldName = "string20";
                fieldValue = edocSummary.getVarchar20();
            } else if (formElement.getElementId() == 61) {
                fieldType = "textarea";
                fieldName = "text1";
                fieldValue = edocSummary.getText1();
            } else if (formElement.getElementId() == 62) {
                fieldType = "textarea";
                fieldName = "text2";
                fieldValue = edocSummary.getText2();
            } else if (formElement.getElementId() == 63) {
                fieldType = "textarea";
                fieldName = "text3";
                fieldValue = edocSummary.getText3();
            } else if (formElement.getElementId() == 64) {
                fieldType = "textarea";
                fieldName = "text4";
                fieldValue = edocSummary.getText4();
            } else if (formElement.getElementId() == 65) {
                fieldType = "textarea";
                fieldName = "text5";
                fieldValue = edocSummary.getText5();
            } else if (formElement.getElementId() == 66) {
                fieldType = "textarea";
                fieldName = "text6";
                fieldValue = edocSummary.getText6();
            } else if (formElement.getElementId() == 67) {
                fieldType = "textarea";
                fieldName = "text7";
                fieldValue = edocSummary.getText7();
            } else if (formElement.getElementId() == 68) {
                fieldType = "textarea";
                fieldName = "text8";
                fieldValue = edocSummary.getText8();
            } else if (formElement.getElementId() == 69) {
                fieldType = "textarea";
                fieldName = "text9";
                fieldValue = edocSummary.getText9();
            } else if (formElement.getElementId() == 70) {
                fieldType = "textarea";
                fieldName = "text10";
                fieldValue = edocSummary.getText10();
            } else if (formElement.getElementId() == 71) {
                fieldName = "integer1";
                if(edocSummary.getInteger1() != null){
                    fieldValue = edocSummary.getInteger1().toString();
                }
            } else if (formElement.getElementId() == 72) {
                fieldName = "integer2";
                if(edocSummary.getInteger2() != null){
                    fieldValue = edocSummary.getInteger2().toString();
                }
            } else if (formElement.getElementId() == 73) {
                fieldName = "integer3";
                if(edocSummary.getInteger3() != null){
                    fieldValue = edocSummary.getInteger3().toString();
                }
            } else if (formElement.getElementId() == 74) {
                fieldName = "integer4";
                if(edocSummary.getInteger4() != null){
                    fieldValue = edocSummary.getInteger4().toString();
                }
            } else if (formElement.getElementId() == 75) {
                fieldName = "integer5";
                if(edocSummary.getInteger5() != null){
                    fieldValue = edocSummary.getInteger5().toString();
                }
            } else if (formElement.getElementId() == 76) {
                fieldName = "integer6";
                if(edocSummary.getInteger6() != null){
                    fieldValue = edocSummary.getInteger6().toString();
                }
            } else if (formElement.getElementId() == 77) {
                fieldName = "integer7";
                if(edocSummary.getInteger7() != null){
                    fieldValue = edocSummary.getInteger7().toString();
                }
            } else if (formElement.getElementId() == 78) {
                fieldName = "integer8";
                if(edocSummary.getInteger8() != null){
                    fieldValue = edocSummary.getInteger8().toString();
                }
            } else if (formElement.getElementId() == 79) {
                fieldName = "integer9";
                if(edocSummary.getInteger9() != null){
                    fieldValue = edocSummary.getInteger9().toString();
                }
            } else if (formElement.getElementId() == 80) {
                fieldName = "integer10";
                if(edocSummary.getInteger10() != null){
                    fieldValue = edocSummary.getInteger10().toString();
                }
            } else if (formElement.getElementId() == 231) {
                fieldName = "integer11";
                if(edocSummary.getInteger11() != null){
                    fieldValue = edocSummary.getInteger11().toString();
                }
            } else if (formElement.getElementId() == 232) {
                fieldName = "integer12";
                if(edocSummary.getInteger12() != null){
                    fieldValue = edocSummary.getInteger12().toString();
                }
            } else if (formElement.getElementId() == 233) {
                fieldName = "integer13";
                if(edocSummary.getInteger13() != null){
                    fieldValue = edocSummary.getInteger13().toString();
                }
            } else if (formElement.getElementId() == 234) {
                fieldName = "integer14";
                if(edocSummary.getInteger14() != null){
                    fieldValue = edocSummary.getInteger14().toString();
                }
            } else if (formElement.getElementId() == 235) {
                fieldName = "integer15";
                if(edocSummary.getInteger15() != null){
                    fieldValue = edocSummary.getInteger15().toString();
                }
            } else if (formElement.getElementId() == 236) {
                fieldName = "integer16";
                if(edocSummary.getInteger16() != null){
                    fieldValue = edocSummary.getInteger16().toString();
                }
            } else if (formElement.getElementId() == 237) {
                fieldName = "integer17";
                if(edocSummary.getInteger17() != null){
                    fieldValue = edocSummary.getInteger17().toString();
                }
            } else if (formElement.getElementId() == 238) {
                fieldName = "integer18";
                if(edocSummary.getInteger18() != null){
                    fieldValue = edocSummary.getInteger18().toString();
                }
            } else if (formElement.getElementId() == 239) {
                fieldName = "integer19";
                if(edocSummary.getInteger19() != null){
                    fieldValue = edocSummary.getInteger19().toString();
                }
            } else if (formElement.getElementId() == 240) {
                fieldName = "integer20";
                if(edocSummary.getInteger20() != null){
                    fieldValue = edocSummary.getInteger20().toString();
                }
            }

            else if (formElement.getElementId() == 81) {
                fieldName = "decimal1";
                if(edocSummary.getDecimal1() != null){
                    fieldValue = df.format(edocSummary.getDecimal1());
                }
            } else if (formElement.getElementId() == 82) {
                fieldName = "decimal2";
                if(edocSummary.getDecimal2() != null){
                    fieldValue = df.format(edocSummary.getDecimal2());
                }
            } else if (formElement.getElementId() == 83) {
                fieldName = "decimal3";
                if(edocSummary.getDecimal3() != null){
                    fieldValue = df.format(edocSummary.getDecimal3());
                }
            } else if (formElement.getElementId() == 84) {
                fieldName = "decimal4";
                if(edocSummary.getDecimal4() != null){
                    fieldValue = df.format(edocSummary.getDecimal4());
                }
            } else if (formElement.getElementId() == 85) {
                fieldName = "decimal5";
                if(edocSummary.getDecimal5() != null){
                    fieldValue = df.format(edocSummary.getDecimal5());
                }
            } else if (formElement.getElementId() == 86) {
                fieldName = "decimal6";
                if(edocSummary.getDecimal6() != null){
                    fieldValue = df.format(edocSummary.getDecimal6());
                }
            } else if (formElement.getElementId() == 87) {
                fieldName = "decimal7";
                if(edocSummary.getDecimal7() != null){
                    fieldValue = df.format(edocSummary.getDecimal7());
                }
            } else if (formElement.getElementId() == 88) {
                fieldName = "decimal8";
                if(edocSummary.getDecimal8() != null){
                    fieldValue = df.format(edocSummary.getDecimal8());
                }
            } else if (formElement.getElementId() == 89) {
                fieldName = "decimal9";
                if(edocSummary.getDecimal9() != null){
                    fieldValue = df.format(edocSummary.getDecimal9());
                }
            } else if (formElement.getElementId() == 90) {
                fieldName = "decimal10";
                if(edocSummary.getDecimal10() != null){
                    fieldValue = df.format(edocSummary.getDecimal10());
                }
            } else if (formElement.getElementId() == 251) {
                fieldName = "decimal11";
                if(edocSummary.getDecimal11() != null){
                    fieldValue = df.format(edocSummary.getDecimal11());
                }
            } else if (formElement.getElementId() == 252) {
                fieldName = "decimal12";
                if(edocSummary.getDecimal12() != null){
                    fieldValue = df.format(edocSummary.getDecimal12());
                }
            } else if (formElement.getElementId() == 253) {
                fieldName = "decimal13";
                if(edocSummary.getDecimal13() != null){
                    fieldValue = df.format(edocSummary.getDecimal13());
                }
            } else if (formElement.getElementId() == 254) {
                fieldName = "decimal14";
                if(edocSummary.getDecimal14() != null){
                    fieldValue = df.format(edocSummary.getDecimal14());
                }
            } else if (formElement.getElementId() == 255) {
                fieldName = "decimal15";
                if(edocSummary.getDecimal15() != null){
                    fieldValue = df.format(edocSummary.getDecimal15());
                }
            } else if (formElement.getElementId() == 256) {
                fieldName = "decimal16";
                if(edocSummary.getDecimal16() != null){
                    fieldValue = df.format(edocSummary.getDecimal16());
                }
            } else if (formElement.getElementId() == 257) {
                fieldName = "decimal17";
                if(edocSummary.getDecimal17() != null){
                    fieldValue = df.format(edocSummary.getDecimal17());
                }
            } else if (formElement.getElementId() == 258) {
                fieldName = "decimal18";
                if(edocSummary.getDecimal18() != null){
                    fieldValue = df.format(edocSummary.getDecimal18());
                }
            } else if (formElement.getElementId() == 259) {
                fieldName = "decimal19";
                if(edocSummary.getDecimal19() != null){
                    fieldValue = df.format(edocSummary.getDecimal19());
                }
            } else if (formElement.getElementId() == 260) {
                fieldName = "decimal20";
                if(edocSummary.getDecimal20() != null){
                    fieldValue = df.format(edocSummary.getDecimal20());
                }
            } else if (formElement.getElementId() == 91) {
                fieldName = "date1";
                fieldValue = formatDate(edocSummary.getDate1());
            } else if (formElement.getElementId() == 92) {
                fieldName = "date2";
                fieldValue = formatDate(edocSummary.getDate2());
            } else if (formElement.getElementId() == 93) {
                fieldName = "date3";
                fieldValue = formatDate(edocSummary.getDate3());
            } else if (formElement.getElementId() == 94) {
                fieldName = "date4";
                fieldValue = formatDate(edocSummary.getDate4());
            } else if (formElement.getElementId() == 95) {
                fieldName = "date5";
                fieldValue = formatDate(edocSummary.getDate5());
            } else if (formElement.getElementId() == 96) {
                fieldName = "date6";
                fieldValue = formatDate(edocSummary.getDate6());
            } else if (formElement.getElementId() == 97) {
                fieldName = "date7";
                fieldValue = formatDate(edocSummary.getDate7());
            } else if (formElement.getElementId() == 98) {
                fieldName = "date8";
                fieldValue = formatDate(edocSummary.getDate8());
            } else if (formElement.getElementId() == 99) {
                fieldName = "date9";
                fieldValue = formatDate(edocSummary.getDate9());
            } else if (formElement.getElementId() == 100) {
                fieldName = "date10";
                fieldValue = formatDate(edocSummary.getDate10());
            } else if (formElement.getElementId() == 271) {
                fieldName = "date11";
                fieldValue = formatDate(edocSummary.getDate11());
            } else if (formElement.getElementId() == 272) {
                fieldName = "date12";
                fieldValue = formatDate(edocSummary.getDate12());
            } else if (formElement.getElementId() == 273) {
                fieldName = "date13";
                fieldValue = formatDate(edocSummary.getDate13());
            } else if (formElement.getElementId() == 274) {
                fieldName = "date14";
                fieldValue = formatDate(edocSummary.getDate14());
            } else if (formElement.getElementId() == 275) {
                fieldName = "date15";
                fieldValue = formatDate(edocSummary.getDate15());
            } else if (formElement.getElementId() == 276) {
                fieldName = "date16";
                fieldValue = formatDate(edocSummary.getDate16());
            } else if (formElement.getElementId() == 277) {
                fieldName = "date17";
                fieldValue = formatDate(edocSummary.getDate17());
            } else if (formElement.getElementId() == 278) {
                fieldName = "date18";
                fieldValue = formatDate(edocSummary.getDate18());
            } else if (formElement.getElementId() == 279) {
                fieldName = "date19";
                fieldValue = formatDate(edocSummary.getDate19());
            } else if (formElement.getElementId() == 280) {
                fieldName = "date20";
                fieldValue = formatDate(edocSummary.getDate20());
            } else if (formElement.getElementId() == 101) {
                fieldType = "select";
                fieldName = "list1";
                fieldValue = edocSummary.getList1();

                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, formAccountId);
                }
            } else if (formElement.getElementId() == 102) {
                fieldType = "select";
                fieldName = "list2";
                fieldValue = edocSummary.getList2();
                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, formAccountId);
                }
            } else if (formElement.getElementId() == 103) {
                fieldType = "select";
                fieldName = "list3";
                fieldValue = edocSummary.getList3();
                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, formAccountId);
                }
            } else if (formElement.getElementId() == 104) {
                fieldType = "select";
                fieldName = "list4";
                fieldValue = edocSummary.getList4();
                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, formAccountId);
                }
            } else if (formElement.getElementId() == 105) {
                fieldType = "select";
                fieldName = "list5";
                fieldValue = edocSummary.getList5();
                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, formAccountId);
                }
            } else if (formElement.getElementId() == 106) {
                fieldType = "select";
                fieldName = "list6";
                fieldValue = edocSummary.getList6();
                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, formAccountId);
                }
            } else if (formElement.getElementId() == 107) {
                fieldType = "select";
                fieldName = "list7";
                fieldValue = edocSummary.getList7();
                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, formAccountId);
                }
            } else if (formElement.getElementId() == 108) {
                fieldType = "select";
                fieldName = "list8";
                fieldValue = edocSummary.getList8();
                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, formAccountId);
                }
            } else if (formElement.getElementId() == 109) {
                fieldType = "select";
                fieldName = "list9";
                fieldValue = edocSummary.getList9();
                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, formAccountId);
                }
            } else if (formElement.getElementId() == 110) {
                fieldType = "select";
                fieldName = "list10";
                fieldValue = edocSummary.getList10();
                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, formAccountId);
                }
            } else if (formElement.getElementId() == 261) {
                fieldType = "select";
                fieldName = "list11";
                fieldValue = edocSummary.getList11();
                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, formAccountId);
                }
            } else if (formElement.getElementId() == 262) {
                fieldType = "select";
                fieldName = "list12";
                fieldValue = edocSummary.getList12();
                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, formAccountId);
                }
            } else if (formElement.getElementId() == 263) {
                fieldType = "select";
                fieldName = "list13";
                fieldValue = edocSummary.getList13();
                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, formAccountId);
                }
            } else if (formElement.getElementId() == 264) {
                fieldType = "select";
                fieldName = "list14";
                fieldValue = edocSummary.getList14();
                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, formAccountId);
                }
            } else if (formElement.getElementId() == 265) {
                fieldType = "select";
                fieldName = "list15";
                fieldValue = edocSummary.getList15();
                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, formAccountId);
                }
            } else if (formElement.getElementId() == 266) {
                fieldType = "select";
                fieldName = "list16";
                fieldValue = edocSummary.getList16();
                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, formAccountId);
                }
            } else if (formElement.getElementId() == 267) {
                fieldType = "select";
                fieldName = "list17";
                fieldValue = edocSummary.getList17();
                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, formAccountId);
                }
            } else if (formElement.getElementId() == 268) {
                fieldType = "select";
                fieldName = "list18";
                fieldValue = edocSummary.getList18();
                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, formAccountId);
                }
            } else if (formElement.getElementId() == 269) {
                fieldType = "select";
                fieldName = "list19";
                fieldValue = edocSummary.getList19();
                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, formAccountId);
                }
            } else if (formElement.getElementId() == 270) {
                fieldType = "select";
                fieldName = "list20";
                fieldValue = edocSummary.getList20();
                if (Strings.isNotBlank(fieldValue)) {
                    fieldValue = getListValue(fieldName, fieldValue, formAccountId);
                }
            } else if (formElement.getElementId() == 291) {
                fieldName = "string21";
                fieldValue = edocSummary.getVarchar21();
            } else if (formElement.getElementId() == 292) {
                fieldName = "string22";
                fieldValue = edocSummary.getVarchar22();
            } else if (formElement.getElementId() == 293) {
                fieldName = "string23";
                fieldValue = edocSummary.getVarchar23();
            } else if (formElement.getElementId() == 294) {
                fieldName = "string24";
                fieldValue = edocSummary.getVarchar24();
            } else if (formElement.getElementId() == 295) {
                fieldName = "string25";
                fieldValue = edocSummary.getVarchar25();
            } else if (formElement.getElementId() == 296) {
                fieldName = "string26";
                fieldValue = edocSummary.getVarchar26();
            } else if (formElement.getElementId() == 297) {
                fieldName = "string27";
                fieldValue = edocSummary.getVarchar27();
            } else if (formElement.getElementId() == 298) {
                fieldName = "string28";
                fieldValue = edocSummary.getVarchar28();
            } else if (formElement.getElementId() == 299) {
                fieldName = "string29";
                fieldValue = edocSummary.getVarchar29();
            } else if (formElement.getElementId() == 300) {
                fieldName = "string30";
                fieldValue = edocSummary.getVarchar30();
            } else if (formElement.getElementId() == 301) {
                fieldType = "textarea";
                fieldName = "text11";
                fieldValue = edocSummary.getText11();
            } else if (formElement.getElementId() == 302) {
                fieldType = "textarea";
                fieldName = "text12";
                fieldValue = edocSummary.getText12();
            } else if (formElement.getElementId() == 303) {
                fieldType = "textarea";
                fieldName = "text13";
                fieldValue = edocSummary.getText13();
            } else if (formElement.getElementId() == 304) {
                fieldType = "textarea";
                fieldName = "text14";
                fieldValue = edocSummary.getText14();
            } else if (formElement.getElementId() == 305) {
                fieldType = "textarea";
                fieldName = "text15";
                fieldValue = edocSummary.getText15();
            }

            if (!StringUtils.isBlank(fieldName)) {

                String[] values = new String[4];
                values[0] = fieldType;
                
                if(fieldValue == null){
                    fieldValue = "";
                }
                
                values[1] = fieldValue;
                values[2] = access;
                values[3] = required;
                field2ValMap.put(fieldName, values);
            }
        }

        return field2ValMap;

    }

    
    /**
     * 获取列表类型的枚举值
     * 
     * @Author : xuqw
     * @Date : 2015年5月18日下午2:34:43
     * @param fieldName
     * @param fieldValue
     * @param userAccountId
     * @return
     */
    private String getListValue(String fieldName, String fieldValue, Long userAccountId) {

        String ret = "";

        Long accountId = userAccountId;
        if (accountId == null) {
            accountId = AppContext.getCurrentUser().getLoginAccount();
        }
        EdocElement element = edocElementManager.getByFieldName(fieldName, accountId);

        if (element != null) {
            Long metadataId = element.getMetadataId();
            if (metadataId != null) {

                CtpEnumItem metaDataItem = enumManagerNew.getEnumItem(metadataId, fieldValue);
                if (metaDataItem != null) {
                    ret = ResourceUtil.getString(metaDataItem.getLabel());
                }
            }
        }

        return ret;
    }
    
    /**
     * 转换时间
     * @Author      : xuqw
     * @Date        : 2015年5月18日下午5:00:11
     * @param dateVal
     * @return
     */
    private String formatDate(Date dateVal){
        
        if(dateVal == null){
            return "";
        }
        
        return Datetimes.formatDate(dateVal);
    }
    
    /**
     * 获得模板中绑定的文号
     * @param edocSummary
     * @param fieldValue
     * @param fieldName 
     * @return
     */
	private String getMarkFromTemplate(EdocSummary edocSummary,
			String fieldValue, String fieldName) {
		boolean flag = false;
		if(fieldValue.indexOf("|")>-1){
			flag = true;
		}else{
			//当调用外单位模板时，模板绑定了文号，但此时fieldValue为空，需要通过模板获得绑定的文号
			TemplateManager templateManager = (TemplateManager)AppContext.getBean("templateManager");
			try {
				if(edocSummary.getTempleteId() != null){
					CtpTemplate tep = templateManager.getCtpTemplate(edocSummary.getTempleteId());
					if(tep != null && !tep.getOrgAccountId().equals(Long.valueOf(AppContext.currentAccountId()))){
						flag = true;
					}
				}
			} catch (BusinessException e) {
				LOGGER.error("获得公文模板报错!",e);
			}
		}
		if(flag){
			MarkCategory markCategory=MarkCategory.docMark;
			if(edocSummary.getTempleteId() != null){
	            if("serial_no".equals(fieldName)) {
	            	markCategory=MarkCategory.serialNo;
	            }else if("doc_mark".equals(fieldName)) {
	            	markCategory=MarkCategory.docMark;
	            }else if("doc_mark2".equals(fieldName)){
	            	markCategory=MarkCategory.docMark2;
	            } 
	        }
			EdocMarkModel markModel = edocMarkDefinitionManager.getEdocMarkByTempleteId(edocSummary.getTempleteId(),markCategory);
			if(markModel != null){
				String mark = markModel.getMark();
				if(Strings.isNotBlank(mark)){
					fieldValue = mark;
				}
			}
		}
		return fieldValue;
	}
    
    public EdocElementManager getEdocElementManager() {
        return edocElementManager;
    }
    public void setEdocElementManager(EdocElementManager edocElementManager) {
        this.edocElementManager = edocElementManager;
    }
    
    /**
     * 
     * 给List元素拼接下拉元素
     * @Author      : xuqiangwei
     * @Date        : 2014年10月24日下午2:22:50
     * @param sb ： infobody文单的全部内容拼接字符串
     * @param fieldName : 元素的名称，e.g:list1
     * @param fieldValue ：元素值
     * @param access : edit（元素可编辑）或brower(元素只能查看)
     * @param userAccountId : 元素所在的单位ID
     * @param required : 原始是否必填
     * @return true:元素正常显示， false：元素有值，但是没有找到对应的枚举， 用于抹掉list元素的值
     */
    private boolean addListStr(StringBuffer sb,String fieldName,String fieldValue,String access,Long userAccountId,Boolean required)
    {
        
        boolean ret = true;
        
		String domainName = "my:";
		String fieldInput = "<FieldInput name=\"";
		String fieldInputEnd = "</FieldInput>";
		String display = "<Input display=\"";
		String displayValue = "\" value=\"";
		String displayEnd = "\"/>";
		String name = "";
		List<CtpEnumItem> metaItem = null;
		CtpEnumItem metaDataItem = null;
		String branchFieldDbType = "int";
		// 紧急G6BUG_G6_v1.0_徐州市元申软件有限公司_公文单上传后报错您访问的页面不可用_20120710011530--start
		required = required == null ? false : required;
		// 紧急G6BUG_G6_v1.0_徐州市元申软件有限公司_公文单上传后报错您访问的页面不可用_20120710011530--end
		/**
		 * boolean flag = false ; try{ flag =
		 * this.orgManager.isGroupAdmin(user.getLoginName()) ; }catch(Exception
		 * e) {
		 * 
		 * }
		 **/
		Long accountId = userAccountId;
		if (accountId == null) {
			accountId = AppContext.getCurrentUser().getLoginAccount();
		}
		EdocElement element = edocElementManager.getByFieldName(fieldName,accountId);

		boolean bool = false;
		
		if (null != element && element.getStatus() != 0) {
			Long metadataId = element.getMetadataId();
			if (null != metadataId) {
				CtpEnumBean metadata = enumManagerNew.getEnum(metadataId);
				metaItem = enumManagerNew.getEnumItemInDatabse(metadata.getId());
				String select = "\" type=\"select\" access=\"" + access + "\" required=\"" + required
						+ "\" branchFieldDbType=\"" + branchFieldDbType + "\" allowprint=\"true\" allowtransmit=\"true\">";
				sb.append(fieldInput).append(domainName).append(fieldName).append(select);
				StringBuffer strb=new StringBuffer();
				for (int x = 0; x < metaItem.size(); x++) {
					metaDataItem = (CtpEnumItem) metaItem.get(x);
					// OA-28686
					// postgre环境，liud02单位的公文单"单位枚举和系统枚举"字段公文元素为list02，绑定了多级枚举，新建模板时，多级枚举值全部显示出来了
					if ((metaDataItem.getParentId() != null && metaDataItem.getParentId() != 0)
							|| metaDataItem.getState().intValue() == 0)
						continue; // com.seeyon.v3x.system.Constants.METADATAITEM_SWITCH_DISABLE = 0; //停用
					name = ResourceBundleUtil.getString(metadata.getResourceBundle(), metaDataItem.getLabel());
					if (Strings.isNotBlank(fieldValue) && (metaDataItem.getValue()).equals(fieldValue)) {
						strb.append(display).append(name).append(displayValue)
								.append(metaDataItem.getValue()).append("\" select=\"true\" ").append("/>");
						bool = true;
					} else {
						strb.append(display).append(name).append(displayValue).append(metaDataItem.getValue()).append(displayEnd);
					}
				}
				if (bool) {
                    sb.append(display).append("").append(displayValue).append("").append(displayEnd);
                }else{
                    sb.append(display).append("").append(displayValue).append("").append("\" select=\"true").append(displayEnd);
                }
				sb.append(strb);
				sb.append(fieldInputEnd);
			}
		}
		if(Strings.isNotBlank(fieldValue) && !bool){
            ret = false;
        }
		return ret;
        
    }
    /**
     * 公文单页面文号显示列表
     * @param sb
     * @param fieldName
     * @param fieldValue
     * @param access
     * @param markType
     * @param orgAccountId
     * @param summary
     * @param isTemplete : 是否是公文的模板的新建或者编辑显示之用
     * @param isShowOriginalDocMark ： 是否不显示当前的文号值（调用模板的时候不显示模板设置的文号值）
     */
    private void addDocMarkListList(StringBuffer sb,String fieldName,String fieldValue,String access,int markType,
        Long orgAccountId,EdocSummary summary,boolean isTemplete,boolean isNoShowOriginalDocMark,
        boolean isRequired,Long actorId, CtpAffair affair) {
    	
    	
        String domainName  = "my:";
        String fieldInput = "<FieldInput name=\"";
        String fieldInputEnd = "</FieldInput>";     
        String display = "<Input display=\"";
        String displayValue = "\" value=\"";
        String displayEnd = "\"/>";     
        
        //是否是调用公文模板，并且公文模板绑定了字号
        Long templeteId = summary.getTempleteId();
        User user = AppContext.getCurrentUser();
        EdocMarkModel fieldValueDocMarkModel=null;
        EdocMarkModel model = null;
        if(templeteId != null){
            if("serial_no".equals(fieldName)) {
                model = edocMarkDefinitionManager.getEdocMarkByTempleteId(templeteId, MarkCategory.serialNo);
            }else if("doc_mark".equals(fieldName)) {
                model = edocMarkDefinitionManager.getEdocMarkByTempleteId(templeteId, MarkCategory.docMark);
            }else if("doc_mark2".equals(fieldName)){
                model = edocMarkDefinitionManager.getEdocMarkByTempleteId(templeteId, MarkCategory.docMark2);
            } 
        }
        List<EdocMarkModel> list = new ArrayList<EdocMarkModel>();
        try {
            
            //GOV-4731.调用发文模版，文号默认为空 start
            if(model != null){
                //list = new ArrayList<EdocMarkModel>();
                //list.add(model);
            } //else {
            
            Long userId = user.getId();
            if(affair != null){
                userId = affair.getMemberId();
            }
            
            /*long domainId = user.getLoginAccount();//当前登陆单位ID
            long depId = user.getDepartmentId();//当前登陆单位部门ID
            if(user.getLoginAccount() != user.getAccountId()) {//兼职
                List<V3xOrgRelationship> cntListOrg = orgManager.getAllConcurrentPostByAccount(domainId);       
                if(cntListOrg!=null && cntListOrg.size()>0) {
                    for(V3xOrgRelationship rel : cntListOrg) {
                        if(rel.getSourceId() == user.getId()) {
                            depId = rel.getObjectiveId();
                            break;
                        }
                    }
                }
            }*/
            
                //内部文号只取本单位的OA-65854
                Long tempAccountId = null;
                if(markType==EdocEnum.MarkType.edocInMark.ordinal()){
                	//TODO 这里应该取summary中的orgAccoutnId，先不修改 yuhj at 2015-5-8
                    tempAccountId = user.getLoginAccount();
                }else{
                    tempAccountId = V3xOrgEntity.VIRTUAL_ACCOUNT_ID;
                }
            
                StringBuilder deptIds = new StringBuilder(orgManager.getUserIDDomain(userId, tempAccountId, V3xOrgEntity.ORGENT_TYPE_DEPARTMENT, V3xOrgEntity.ORGENT_TYPE_ACCOUNT));
                if(isTemplete ||(isNoShowOriginalDocMark && model!=null)){//是单位管理员新建或者修改模板的话显示授权给这个单位下面部门的文号 or 如果调用模板且模板绑定了文号，才需要查所有文号
                    String allDepIds = getAllDepartmentIdsByAccountId(user.getLoginAccount());
                    if(Strings.isNotBlank(allDepIds)){
                        deptIds.append(",");
                        deptIds.append(allDepIds);
                    }
                }
                /**
                 * 兼职单位的文号 什么情况下获取
                 * 
                 */
                //OA-36598weblogic环境：q1是兼职liud02和gw单位的兼职人员，q1待办处理公文，文号都取的是当前登录单位的，应该是取自己和兼职单位的文号
                if(summary.getId()!=null && user.getAccountId().longValue()!=user.getLoginAccount().longValue()){
                    List<V3xOrgAccount> otherAccounts = orgManager.getConcurrentAccounts(userId);
                    //获得兼职的单位
                    for(V3xOrgAccount account : otherAccounts){
                        
                        Map<Long, List<MemberPost>> map=orgManager.getConcurentPostsByMemberId(account.getId(), userId);
                        Set<Long> set = map.keySet();
                        if(Strings.isNotEmpty(set)){
                            //获得兼职的部门
                            V3xOrgDepartment dep = orgManager.getDepartmentById(set.iterator().next());
                            deptIds.append(",");
                            deptIds.append(dep.getId());
                            deptIds.append(",");
                            deptIds.append(account.getId());
                        }
                    }
                    //当兼职登录时，要加上自己单位的
                    if(user.getAccountId().longValue()!=user.getLoginAccount().longValue()){
                    	PermissionManager permissionManager = (PermissionManager)AppContext.getBean("permissionManager");
                    	PermissionVO pv=permissionManager.getPermission(actorId);
                    	if(!(pv!=null && "niwen".equals(pv.getName()))){
                            String myDeptIds = orgManager.getUserIDDomain(userId, user.getAccountId(), V3xOrgEntity.ORGENT_TYPE_DEPARTMENT, V3xOrgEntity.ORGENT_TYPE_ACCOUNT);
                            deptIds.append(",");
                            deptIds.append(myDeptIds);
                    	}
                    }
                }
                
                if(deptIds.length() > 0){
                    list = edocMarkDefinitionManager.getEdocMarkDefinitions(deptIds.toString(), markType);
                }
                
                if(list == null){
                    list = new ArrayList<EdocMarkModel>();
                }
            //}
            //GOV-4731.调用发文模版，文号默认为空 end
        }
        catch (Exception e) {
            LOGGER.error("读取公文文号时出现错误!" ,e);
        }
        
        /***
         * 
         * 1当绑定的文号被取消授权或者授权给外单位、外单位的部门后，此时模板自动解除绑定文号，其他情况都可以使用此文号
         * 2模板与文号的关系在使用本单位授权模板、外单位授权模板逻辑一致
         * 3当外部单位的模板使用的文号 没有授权给本单位的时候，在本单位调用该模板时，也要显示该文号
         * 
         */
        
        
        //是否使用外部单位的模板 （绑定了文号的）
        boolean isUseOuterAccountTemplateBindingEdocMark = false;
        //调用模板后，如果模板中绑定了文号，则model为绑定的文号
        
        if(model!=null ){
          //这里还要加上当前调用的模板是外部单位的模板
            boolean isOuterTemplate = false;
            if(templeteId != null){
                TemplateManager templateManager = (TemplateManager)AppContext.getBean("templateManager");
                try {
                    CtpTemplate tmp = templateManager.getCtpTemplate(templeteId);
                    long templateAccountId = tmp.getOrgAccountId();
                    long currentAccountId = AppContext.getCurrentUser().getLoginAccount();
                    //表示是否调用外单位模板
                    if(templateAccountId!= currentAccountId){
                        isOuterTemplate = true;
                    }
                } catch (BusinessException e) {
                    LOGGER.error("", e);
                }
            }
            
            EdocMarkAclDAO edocMarkAclDao = (EdocMarkAclDAO)AppContext.getBean("edocMarkAclDAO");
            List<EdocMarkAcl> acls = edocMarkAclDao.findEdocMarkAclByProperty("markDefId",model.getMarkDefinitionId());
            //当外单位的模板绑定的文号 授权了的 
            if(isOuterTemplate && acls.size()!=0 ){
                int count=0;
                for(EdocMarkModel m : list){
                    if(m.toString().equals(model.toString())){
                        count++;
                    }
                }
                if(count == 0 && Strings.isBlank(fieldValue)){
                    //如果当前单位能够使用的文号list中没有 模板绑定的文号，那么需要将文号加入进去
                    list.add(model);
                    //当外部单位的模板使用的文号 没有授权给本单位的时候，在本单位调用该模板时，也要显示该文号(之前的判断 没有包含这种情况，所以在补上)
                    isUseOuterAccountTemplateBindingEdocMark = true;
                    fieldValue = model.toString();
                }
            }
        }
        
        //模板绑定的文号是否被删除
        boolean templateEdocMarkIsDelete = false;
        if(model != null){
            EdocMarkDefinition markDef = edocMarkDefinitionManager.getMarkDefinition(model.getMarkDefinitionId());
            //当模板绑定的文号被删除后 在文单中就不显示该文号了
            if(markDef.getStatus() == 2){
                templateEdocMarkIsDelete = true;
            }
        }
        
        //OA-25353  文单定义中修改文单必填项，拟文时：文号看不出来是否设置了必填项  
        String select = "\" type=\"select\" access=\""+access+"\" allowprint=\"true\" allowtransmit=\"true\"" +
                " required=\""+isRequired+"\">";
        sb.append(fieldInput).append(domainName).append(fieldName).append(select);
        
        //----如果fieldValue不为空或不等于"",拼接成<Input display=... "0|文号||0(Constants.EDOC_MARK_EDIT_NONE)".... select="true" />
        if(null!=fieldValue && !"".equals(fieldValue)){
            
            /*
            //OA-12652  模板绑定文号，将文号删除，在拟文时调用该模板，发送没有提示。  
            boolean isDelete = true;
            //当从模板中读取文号  格式：-8010307253303099035|ddd〔2013〕0001号|1|1
            //OA-21313 已发公文，撤销后，在待发中编辑。然后切换文单，文单中的数据没有同步保留。但是经过手动更改的数据，会同步保留。
            //但如果是 0|ddd〔2013〕0001号|0，则isDelete为false,场景是从待发中编辑后，切换文旦，依然要显示之前的文号
            if(fieldValue.startsWith("0|")){
                isDelete = false;
            }
            else if(fieldValue.indexOf("|")>-1){
                //已选择的文号 
                long markId = Long.parseLong(fieldValue.split("[|]")[0]);
                for(EdocMarkModel mark : list){
                    if(mark.getMarkDefinitionId().longValue() == markId){
                        isDelete = false;
                        break;
                    }
                }
            }*/
            
        	
            //模板中使用的文号没有删除时
            if(!templateEdocMarkIsDelete /*&& (fieldValue.indexOf("|") == -1 || !isDelete)*/){
                //切换文单时候，录入的临时文号需要解析
                try{
                    fieldValueDocMarkModel =  EdocMarkModel.parse(fieldValue);
                }catch(Exception e){
                    LOGGER.error("", e);
                }
                
                if(fieldValueDocMarkModel==null){
                    sb.append(display);
                    sb.append(Strings.toHTML(Strings.toXmlStr(fieldValue),false));
                    sb.append(displayValue);
                    sb.append("0|" + Strings.toHTML(Strings.toXmlStr(fieldValue),false) + "||" + Constants.EDOC_MARK_EDIT_NONE);//
                    sb.append("\" select=\"true " + displayEnd);
                }else{
                    //GOV-4731.调用发文模版，文号默认为空 start
                    if(isNoShowOriginalDocMark) {
                        try {
                            //当前操作时调用模板的时候不显示模板设置的字号
                            EdocMarkDefinition markDef = edocMarkDefinitionManager.getMarkDefinition(fieldValueDocMarkModel.getMarkDefinitionId());
                            //OA-33699 客户bug：授权给外单位来用的公文模板，外单位拟文人，拟文节点权限跟外单位的一致
//                          if(markDef != null && edocMarkDefinitionManager.isEdocMarkAclByDefinitionId(user, markDef,templateOrgAccountId)) {
                            if(markDef != null) {
                                Calendar cal = Calendar.getInstance();
                                String yearNo = String.valueOf(cal.get(Calendar.YEAR));
                                EdocMarkModel currentModel = edocMarkDefinitionManager.markDef2Mode(markDef, yearNo, null);
                                String strTemp = currentModel.getMarkDefinitionId() + "|" + currentModel.getMark() + "|" + currentModel.getCurrentNo() + "|" + Constants.EDOC_MARK_EDIT_SELECT_NEW;
                                sb.append(display);
                                
                                String markWordNo = currentModel.getMark() + EdocHelper.getEdocMarkDispalyName(user.getLoginAccount(), currentModel);
                                sb.append(Strings.toHTML(Strings.toXmlStr(markWordNo),false));
                                
                                sb.append(displayValue);
                                sb.append(Strings.toHTML(Strings.toXmlStr(strTemp),false));             
                                sb.append("\" select=\"true " + displayEnd);
                                //将模板中的公文名称（没有公文当前号）的docMark修改
                                fieldValue = strTemp;
                            }
                        } catch(Exception e) {
                            LOGGER.error("", e);
                        }
                    //GOV-4731.调用发文模版，文号默认为空 end
                    }else{
                    	if(!isTemplete){//OA-51828新建收文模板，选定一个收文编号，管理员去修改模板时，收文编号下拉框出现2个原文号--模板编辑会把所有文号查出，包括已经调用的文号，所以不需要再加了
                            sb.append(display);
                            sb.append(Strings.toHTML(Strings.toXmlStr(fieldValueDocMarkModel.getMark()),false));
                            sb.append(displayValue);                
                            sb.append(Strings.toHTML(Strings.toXmlStr(fieldValue),false));              
                            sb.append("\" select=\"true " + displayEnd);
                    	}

                    }
                }
            }
        } //else{//如果fieldValue没有默认值,显示下面的提示：<请选择公文文号>,修改问号的时候要能取消原来的问号，所以每个下拉列表都显示一个<..请选择..>
        
        //OA-37968 应用检查：系统建立了模板【文单、文号都是本单位的】，绑定了文号"丫丫"，前台调用时文号还可以选择其他可用的文号 
        //当调用模板，且模板绑定了文号时，调用后文号就不能选择其他的了
        boolean isDisplayOtherMark = false;
        if(Strings.isBlank(fieldValue)){
            isDisplayOtherMark = true;
        }else{
            //加上model !=null 表示拟文和处理时如果调用的模板绑定了文号，就只显示该文号了
            if((model != null && validateEdocMark(fieldValue,fieldName,templeteId)) || isUseOuterAccountTemplateBindingEdocMark){
                isDisplayOtherMark = false;
            }else{
                isDisplayOtherMark = true;
            }
        }
        
        if (isDisplayOtherMark || templateEdocMarkIsDelete){
            
            String r ="com.seeyon.v3x.edoc.resources.i18n.EdocResource";
            String value="";
            if(markType==EdocEnum.MarkType.edocMark.ordinal()){
                value = ResourceBundleUtil.getString(r, "edoc.mark.empty.label");
            }else if(markType==EdocEnum.MarkType.edocInMark.ordinal()){
                value = ResourceBundleUtil.getString(r, "edoc.inmark.empty.label");
            }
            sb.append(display);
            sb.append(Strings.toHTML(Strings.toXmlStr(value),false));
            sb.append(displayValue);
            sb.append("");//
            sb.append("\" select=\"true " + displayEnd);
        }   
        //}
        
        String strTemp="";
        //如果文号定义的列表中存在数据,用列表的形式保存
        //OA-37968应用检查：系统建立了模板【文单、文号都是本单位的】，绑定了文号"丫丫"，前台调用时文号还可以选择其他可用的文号 
        if ((isDisplayOtherMark || templateEdocMarkIsDelete) && list != null && list.size()>0) {
            /*
             * OA-83740 这个BUG的时候屏蔽的
            EdocMark edocMark= null;
            if(summary!=null && summary.getId()!=null){
                
                String tempMark = null;
                if("serial_no".equals(fieldName)) {
                    tempMark = summary.getSerialNo();
                }else if("doc_mark".equals(fieldName)) {
                    tempMark = summary.getDocMark();
                }else if("doc_mark2".equals(fieldName)){
                    tempMark = summary.getDocMark2();
                } 
                
                List<EdocMark> summaryMarks = edocMarksManager.findEdocMarkByEdocSummaryId(summary.getId());
                if(Strings.isNotEmpty(summaryMarks)){
                    for(EdocMark mark : summaryMarks){
                        if(mark.getDocMark().equals(tempMark)){
                            edocMark = mark;
                            break;
                        }
                    }
                }
            }*/
            for(int x=0;x<list.size();x++){
                EdocMarkModel markModel = (EdocMarkModel)list.get(x);
                if (null != markModel) {
                    Long definitionId = markModel.getMarkDefinitionId();
                    //如果公文已经存在并且使用了一个文号定义，该公文使用的文号定义不需要再显示了
                    /*
                     *OA-83740 这个BUG的时候屏蔽的
                     * if(edocMark!=null 
                            && edocMark.getEdocMarkDefinition()!=null
                            && edocMark.getEdocMarkDefinition().getId()!=null
                            && edocMark.getEdocMarkDefinition().getId().longValue() ==definitionId.longValue()
                            ) {
                        if(summary!=null && summary.getId()!=null && Strings.isNotBlank(summary.getDocMark())) {
                            if("doc_mark".equals(fieldName) && Strings.isNotBlank(summary.getDocMark())) {
                                continue;   
                            }
                            else if("doc_mark2".equals(fieldName) && Strings.isNotBlank(summary.getDocMark2())) {
                                continue;
                            }
                        }
                    }
                    */
                    strTemp=definitionId + "|" + markModel.getMark() + "|" + markModel.getCurrentNo() + "|" + Constants.EDOC_MARK_EDIT_SELECT_NEW;
                    if(strTemp.equals(fieldValue)){
                        continue;
                    }
                    sb.append(display);       
                    
                    //GOV-3467 公文管理-基础数据-模版管理，新建发文模版时，选择公文文号时，文号显示不全
                    //GOV-4731.调用发文模版，文号默认为空 start(3467引起的这个BUG，3467不需修改)
                    //所以注释以下 模板的判断
                    if(isTemplete){
                        //新建或者修改模板的时候，只显示字号，不显示流水号
                    	Integer templateDocMarkNo = markModel.getCurrentNo();
                    	if(fieldValueDocMarkModel != null && definitionId.equals(fieldValueDocMarkModel.getMarkDefinitionId())){
                    		//修改模板的时候前端可能已经调用过这个模板来，流水号已经增加了，为了达到修改选中的效果，将下拉的流水号的当前值设置为模板保存当时的值。
                    		templateDocMarkNo = fieldValueDocMarkModel.getCurrentNo();
                    	}
                        String markWordNo = markModel.getWordNo() + EdocHelper.getEdocMarkDispalyName(user.getLoginAccount(), markModel);
                        sb.append(Strings.toHTML(Strings.toXmlStr(markWordNo),false));
                        sb.append(displayValue);
                        strTemp = definitionId + "|" + markModel.getWordNo() + "|" + templateDocMarkNo + "|" + Constants.EDOC_MARK_EDIT_SELECT_NEW;
                        sb.append(Strings.toHTML(Strings.toXmlStr(strTemp),false));
                    }else{
                        String markWordNo = markModel.getMark() + EdocHelper.getEdocMarkDispalyName(user.getLoginAccount(), markModel);
                        sb.append(Strings.toHTML(Strings.toXmlStr(markWordNo),false));
                        sb.append(displayValue);
                        sb.append(Strings.toHTML(Strings.toXmlStr(strTemp),false));
                    }
                    sb.append(displayEnd);
                }
            }   
        }     
        sb.append(fieldInputEnd);
    }
//  private Set<EdocMarkModel> fiterDuplicateEdocMark(List<EdocMarkModel> list ){
//      
//      if(list == null || list.isEmpty()) return null;
//      
//      List<EdocMarkModel> flist = new ArrayList<EdocMarkModel>();
//      
//      for(EdocMarkModel model : list){
//          if()
//      }
//  }
    private String getAllDepartmentIdsByAccountId(Long _orgAccountId)
            throws BusinessException {
        StringBuilder sd = new StringBuilder();
        List<V3xOrgDepartment> depts = orgManager.getAllDepartments(_orgAccountId);
        for(V3xOrgDepartment dep : depts){
            if(sd.length()<=0)sd.append(dep.getId());
            else sd.append(",").append(dep.getId());
        }
        return sd.toString();
    }
    
    /**
     * 方法封装：生成文单时，发起者部门的获得
     */
    private String getSendDepartmentField(EdocSummary edocSummary,String departType){
        String fieldValue="";
        
        String department= null;
        if("send_department2".equals(departType)){
            department=(edocSummary==null?null:edocSummary.getSendDepartment2());
        }else{
            department=(edocSummary==null?null:edocSummary.getSendDepartment());
        }
       
        if(Strings.isNotBlank(department)){
        	fieldValue =department; 
        }
        else{
            User user = AppContext.getCurrentUser();
            long depId = user.getDepartmentId();
            V3xOrgDepartment dep = null;
            try {
                //GOV-4961.A单位人员a兼职到B单位的b部门，发文时切换到B单位，发文单上的发文部门却显示的是他的主岗单位 start
                if(!Strings.equals(user.getLoginAccount(), user.getAccountId())) {
                    //得到所有跨单位兼职列表
                    Map<Long, List<MemberPost>> map=orgManager.getConcurentPostsByMemberId(user.getLoginAccount(), user.getId());
                    Set<Long> set = map.keySet();
                    if(Strings.isNotEmpty(set)){
                        dep = orgManager.getDepartmentById(set.iterator().next());
                    }
                }else{
                        dep = orgManager.getDepartmentById(depId);
                }
                //GOV-4961.A单位人员a兼职到B单位的b部门，发文时切换到B单位，发文单上的发文部门却显示的是他的主岗单位 end
            } catch (BusinessException e) {
                LOGGER.error("当前登陆单位所在 部门出错："+depId,e);
            }                   
            //使用单位管理员建模版，应该找不到当前用户的发起部门
            if(dep != null)
            fieldValue = dep.getName();
        }
    
        return fieldValue;
    }
    
    
    private boolean validateEdocMark(String docMark,String fieldName,Long templeteId){
    	User user = AppContext.getCurrentUser();
    	long aclAccountId = user.getLoginAccount();
    	Long parentTmpAccountId = 0L;//如果是调用的模板保存的个人模板则取原模板的单位id
    	if(templeteId != null){
            TemplateManager templateManager = (TemplateManager)AppContext.getBean("templateManager");
            try {
                CtpTemplate tmp = templateManager.getCtpTemplate(templeteId);
                if(tmp!=null){
                	Long formParentId = tmp.getFormParentid();
                	if(formParentId != null){
                		CtpTemplate parentTmp = templateManager.getCtpTemplate(formParentId);
                		if(parentTmp !=null){
                			parentTmpAccountId = parentTmp.getOrgAccountId();
                		}
                	}
                	aclAccountId = tmp.getOrgAccountId();
                }
            } catch (BusinessException e) {
               LOGGER.error("", e);
            }
        }else{
        	aclAccountId=user.getLoginAccount().longValue();
        }
        if(Strings.isNotBlank(docMark)){
            String[] marks = docMark.split("[|]");
            if(marks.length == 1) return true;
            long markId = 0;
            try{
                markId= Long.parseLong(marks[0]);
            }catch(Exception e){
                return true;
            }
            //如果是手工输入文号，就不验证了
            if(marks.length==4 && "3".equals(marks[3])){
            	return true;
            }
            
            EdocMarkDefinition def = edocMarkDefinitionManager.getMarkDefinition(markId);
            if(def !=null && def.getStatus() != 2){
                EdocMarkAclDAO edocMarkAclDao = (EdocMarkAclDAO)AppContext.getBean("edocMarkAclDAO");
                List<EdocMarkAcl> acls = edocMarkAclDao.findEdocMarkAclByProperty("markDefId",markId);
                
                if(Strings.isNotEmpty(acls)){
                    for(EdocMarkAcl acl : acls){
                        if("Account".equals(acl.getAclType())){
                            long accountId = acl.getDeptId();
                            if(accountId == aclAccountId || accountId == parentTmpAccountId){//调用的模板保存的时候，取原模板的单位id
                                return true;
                            }
                        }else{
                            long departmentId = acl.getDeptId();
                            try {
                                V3xOrgDepartment dep = orgManager.getDepartmentById(departmentId);
                                //如果是单位管理员，且授权的部门就是本单位的，也可以查看文号
                                if(dep!=null && (dep.getOrgAccountId().longValue() == aclAccountId || dep.getOrgAccountId().longValue() == parentTmpAccountId)){
                                    return true;
                                }
                            } catch (BusinessException e) {
                                LOGGER.error("获取部门出错",e);
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * 将Summary的指定字段值置空
     * @Author      : xuqiangwei
     * @Date        : 2014年10月24日下午2:45:20
     * @param summary
     * @param filedName
     */
    private void setSummaryFieldEmptyValue(EdocSummary summary, String filedName){
        if(summary != null && Strings.isNotBlank(filedName)){
            String methodName = "set" + filedName.substring(0, 1).toUpperCase() + filedName.substring(1);
            try {
                Method method = EdocSummary.class.getMethod(methodName, String.class);
                method.invoke(summary, new Object[]{null});
            } catch (Exception e) {
            	LOGGER.error("", e);
            } 
        }
    }

	public void setEnumManagerNew(EnumManager enumManager) {
        this.enumManagerNew = enumManager;
    }

	public void setEdocMarkDefinitionManager(EdocMarkDefinitionManager edocMarkDefinitionManager) {
        this.edocMarkDefinitionManager = edocMarkDefinitionManager;
    }
    public void setEdocElementFlowPermAclManager(EdocElementFlowPermAclManager edocElementFlowPermAclManager) {
        this.edocElementFlowPermAclManager=edocElementFlowPermAclManager;
    }
    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }
    
    public void setEdocFormManager(EdocFormManager edocFormManager) {
        this.edocFormManager = edocFormManager;
    }
}