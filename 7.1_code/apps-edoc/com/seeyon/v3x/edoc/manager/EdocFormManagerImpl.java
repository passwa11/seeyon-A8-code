package com.seeyon.v3x.edoc.manager;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.seeyon.ctp.cluster.notification.NotificationManager;
import com.seeyon.ctp.cluster.notification.NotificationType;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.form.bean.FormFieldComBean;
import com.seeyon.ctp.form.util.CharReplace;
import com.seeyon.ctp.form.util.Enums.FieldType;
import com.seeyon.ctp.form.util.StringUtils;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.json.JSONUtil;
//import com.seeyon.ctp.workflow.vo.WorkflowFormFieldVO;
import com.seeyon.v3x.edoc.dao.EdocFormAclDao;
import com.seeyon.v3x.edoc.dao.EdocFormDao;
import com.seeyon.v3x.edoc.dao.EdocFormElementDao;
import com.seeyon.v3x.edoc.dao.EdocFormExtendInfoDao;
import com.seeyon.v3x.edoc.dao.EdocFormFlowPermBoundDao;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.domain.EdocFormElement;
import com.seeyon.v3x.edoc.domain.EdocFormExtendInfo;
import com.seeyon.v3x.edoc.domain.EdocFormFlowPermBound;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.exception.EdocException;
import com.seeyon.v3x.edoc.util.Constants;
import com.seeyon.v3x.edoc.util.EdocFormHelper;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.edoc.util.FormParseUtil;
import com.seeyon.v3x.edoc.util.XMLConverter;
import com.seeyon.v3x.edoc.webmodel.EdocFormModel;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;

/**
 * EdocFormManagerImpl.java
 * 
 * @author 韩冬佑
 *
 */
public class EdocFormManagerImpl implements EdocFormManager {

	private static final Log LOGGER = LogFactory.getLog(EdocFormManagerImpl.class);
	private String baseFileFolder;
	private static String formFolder = "/form";
	private static String templateFolder = "/template";
	private static String orgFolder = "/orgnization";
	 
	private EdocFormDao edocFormDao;
	private EdocFormElementDao edocFormElementDao;
	private EdocFormAclDao edocFormAclDao;
	private EdocFormExtendInfoDao edocFormExtendInfoDao;
	private EdocCategoryManager edocCategoryManager;

	public void setEdocCategoryManager(EdocCategoryManager edocCategoryManager) {
		this.edocCategoryManager = edocCategoryManager;
	}
	private EdocElementManager edocElementManager;	
	private XMLConverter xmlConverter;
	private FileManager fileManager;
	private AttachmentManager attachmentManager;
	private EdocFormFlowPermBoundDao edocFormFlowPermBoundDao;
	private OrgManager orgManager;
	private TemplateManager templateManager;
	
	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public OrgManager getOrgManager() {
		return orgManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public void setEdocFormExtendInfoDao(EdocFormExtendInfoDao edocFormExtendInfoDao) {
		this.edocFormExtendInfoDao = edocFormExtendInfoDao;
	}
	private static Hashtable <String,EdocForm>defaultEdocForm=new Hashtable<String,EdocForm>();	
	
	public AttachmentManager getAttachmentManager() {
		return attachmentManager;
	}

	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}

	public FileManager getFileManager() {
		return fileManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public XMLConverter getXmlConverter() {
		return xmlConverter;
	}

	public void setXmlConverter(XMLConverter xmlConverter) {
		this.xmlConverter = xmlConverter;
	}

	public EdocFormManagerImpl() {		
	}
	
	
	public EdocFormDao getEdocFormDao() {
        return edocFormDao;
    }

    public void setEdocFormDao(EdocFormDao edocFormDao) {
        this.edocFormDao = edocFormDao;
    }  
    
    public EdocElementManager getEdocElementManager() {
		return edocElementManager;
	}

	public void setEdocElementManager(EdocElementManager edocElementManager) {
		this.edocElementManager = edocElementManager;
	}

	public EdocFormElementDao getEdocFormElementDao() {
    	return edocFormElementDao;
    }
    
    public void setEdocFormElementDao(EdocFormElementDao edocFormElementDao) {
    	this.edocFormElementDao = edocFormElementDao;
    }
    
    public EdocFormAclDao getEdocFormAclDao() {
    	return edocFormAclDao;
    }
    
    public void setEdocFormAclDao(EdocFormAclDao edocFormAclDao) {
    	this.edocFormAclDao = edocFormAclDao;
    }
    
    public void createEdocForm(EdocForm edocForm,List<Long> elementIdList){
    	int eleNum=0;
    	for(Long eleId:elementIdList)
    	{
    		EdocElement ele=edocElementManager.getEdocElementsById(eleId);
    		if("doc_mark".equals(ele.getFieldName()) || "send_to".equals(ele.getFieldName()) || "doc_mark2".equals(ele.getFieldName()) || "send_to2".equals(ele.getFieldName()))
    		{
    			eleNum++;
    		}
    	}
    	if(eleNum==4){edocForm.setIsunit(true);}
    	else{edocForm.setIsunit(false);}
    	
    	edocFormDao.save(edocForm);
    	saveEdocXmlData(edocForm.getId(),elementIdList);
    }
    
    public void updateEdocForm(EdocForm edocForm) throws Exception
    {    	
    	edocFormDao.update(edocForm);
    }
    public void updateEdocFormExtendInfo(EdocFormExtendInfo efinfo) throws Exception
    {    	
    	edocFormExtendInfoDao.update(efinfo);
    }    
    public EdocForm getEdocForm(long id)
    {
    	EdocForm tempForm = edocFormDao.get(id);
    	if(tempForm != null) {
    		/** 因为公文单不一定是本单位的，这里需要加载公文单所在单位的公文元素 */
    		edocElementManager.loadEdocElementByDomainId(tempForm.getDomainId());
    	}
    	return tempForm;
    }
    
    @Override
    public List<EdocForm> getAllEdocForms(Long domainId)
    {
    	return edocFormDao.getAllEdocForms(domainId);    	
    }    
    
    @Override
    public List<EdocForm> getToFixSysEdocForms(Long domainId) {
        
        return edocFormDao.getToFixSysEdocForms(domainId);
    }
    
    public List<EdocForm> getAllEdocFormsForWeb(User user,Long domainId,String condition,String textfield)
    {
    	String accountIds = "";
    	try{
    		accountIds = orgManager.getUserIDDomain(user.getId(),domainId, V3xOrgEntity.ORGENT_TYPE_ACCOUNT);
    	}catch(Exception e){
    		LOGGER.error("查询文单授权对象异常",e);
    		accountIds =""+user.getLoginAccount();
    	}
    	List<EdocForm> list = edocFormDao.getAllEdocFormsForWeb(user.getLoginAccount(),accountIds,condition,textfield);
    	this.edocCategoryManager.fillFormCategoryName(list);
    	return list;
    }  
    
    public List<EdocForm> getAllEdocFormsByType(Long domainId,int type)
    {    	
    	return edocFormDao.getAllEdocFormsByType(domainId,type);    	
    }    
    
    public List<EdocForm> getAllEdocFormsByStatus(Long domainId,int status)
    {
    	return edocFormDao.getAllEdocFormsByStatus(domainId,status);
    }
    
    public List<EdocForm> getAllEdocFormsByTypeAndStatus(Long domainId,int type, int status)
    {
    	return edocFormDao.getAllEdocFormsByTypeAndStatus(domainId,type, status);
    }
    
    public List<EdocForm> getEdocForms(long domainId,String domainIds ,int type) {
    	return getEdocForms(domainId,domainIds, type, -1);
    }    
    public List<EdocForm> getEdocForms(long domainId,String domainIds ,int type, long subType) {
    	return edocFormDao.getEdocForms(domainId,domainIds, type, subType);
    }    
    //OA-33741 客户bug：建立公文模板时，部分公文单无法选择到
    //重载方法，加了是否分页的参数
    public List<EdocForm> getEdocForms(long domainId,String domainIds ,int type, long subType,boolean isPage) {
        return edocFormDao.getEdocForms(domainId,domainIds, type, subType,isPage);
    } 
    
    public void removeEdocForm(long id) throws Exception
    {
    	edocFormDao.delete(id);
    }
    public List<EdocFormElement> getAllEdocFormElements(){
    	return edocFormElementDao.getAllEdocFormElements();
    }  
    public List<EdocFormElement> getEdocFormElementByFormId(long formId){
    	return edocFormElementDao.getEdocFormElementByFormId(formId);
    }
    public String getEdocFormXmlData(long formId,EdocSummary edocSummary,long actorId, int edocType)
    {
    	return getEdocFormXmlData(formId,edocSummary,actorId,edocType,false,false);
    }
    
    public String getEdocFormXmlData(long formId,EdocSummary edocSummary,long actorId, int edocType,boolean isTemplete,boolean isCallTemplete){
    	return getEdocFormXmlData(formId, edocSummary, actorId, edocType, isTemplete, isCallTemplete, null);
    }
    
    /**
     * 封装XML信息，带有affair
     * @Author      : xuqiangwei
     * @Date        : 2015年1月26日下午5:54:09
     * @param formId
     * @param edocSummary
     * @param actorId
     * @param edocType
     * @param isTemplete
     * @param isCallTemplete
     * @param affair
     * @return
     */
    public String getEdocFormXmlData(long formId,EdocSummary edocSummary,long actorId, 
            int edocType,boolean isTemplete,boolean isCallTemplete, CtpAffair affair){
        List <EdocFormElement>elements=getEdocFormElementByFormId(formId);
        StringBuffer sBuffer = xmlConverter.convert(elements,edocSummary,actorId, edocType,isTemplete ,isCallTemplete, affair);
        return sBuffer.toString();
    }
    
    @Override
    public Set<EdocFormElement> saveEdocXmlData(long id,List<Long> elementIdList){
        Set<EdocFormElement> eles = new HashSet<EdocFormElement>();
    	for(Long elementId:elementIdList){
    		
    		EdocFormElement eFormElement = new EdocFormElement();
    		eFormElement.setIdIfNew();
    		eFormElement.setFormId(id);
    		eFormElement.setElementId(elementId);
    		eFormElement.setRequired(false);
    		edocFormElementDao.save(eFormElement);
    		eles.add(eFormElement);
    	}
    	return eles;
    	
    }
    public EdocFormModel getEdocFormModel(long formId,EdocSummary edocSummary,long actorId) throws EdocException{
    	return getEdocFormModel(formId,edocSummary,actorId,false,false);
    }
    
    public EdocFormModel getEdocFormModel(long formId,EdocSummary edocSummary,long actorId, CtpAffair affair) throws EdocException{
        return getEdocFormModel(formId,edocSummary,actorId,false,false, affair);
    }
    
    public boolean hasRquiredElement(Long formId) {
    	List<EdocFormElement> list = edocFormElementDao.getEdocFormElementByFormIdNotInSubject(formId);
    	if(list!=null && list.size()>0) {
    		return true;
    	}
    	return false;
    }

    @Override
    public EdocFormModel getEdocFormModel(long formId,EdocSummary edocSummary,long actorId,boolean isTemplete,boolean isCallTemplete, CtpAffair affair) throws EdocException{

        EdocFormModel formModel=new EdocFormModel();
        EdocForm ef = this.getEdocForm(formId);
        User user=AppContext.getCurrentUser();    
        if(ef==null)
        {
            ef=getDefaultEdocForm(user.getAccountId(),edocSummary.getEdocType());
        }
        if(null!=ef){
            
            //检查文单内容是否丢失，有丢失则进行修复
            String content = getFormContentWithFix(ef, formId, edocSummary);
                
            content = EdocUtil.removeFormContentStyle(content);
//            content = content.replaceAll(">&#27;", "> ");//标签开始的&#27;字符替换成普通空格
            try{
                byte[] tempByte_b = CharReplace.doReplace_Decode(content.getBytes("UTF-8"));
                content = new String(tempByte_b,"UTF-8");
                
                // //&#27; 会被转换成 &#160; = &nbsp; 在Chrome先显示不同, 这里转换成&#8194; == &ensp; == en空格 （半个中文宽度）
//                content = content.replaceAll(String.valueOf((char)160), String.valueOf((char)8194));
            }catch(Exception e){
                LOGGER.error("getEdocFormModel得到公文单错误，formId="+formId,e);
                throw new EdocException(e);
            }
            formModel.setXslt(content);
            String xml = getEdocFormXmlData(formId,edocSummary,actorId, ef.getType().intValue(),isTemplete,isCallTemplete, affair);
            formModel.setXml(xml);
            formModel.setEdocFormId(ef.getId());
            formModel.setEdocSummary(edocSummary);
            return formModel;
        }else{
            return null;
        }
    
    }
    
    /**
     * 单子里面有一个BUG，会清空content, 这里做兼容
     * @Author      : xuqw
     * @Date        : 2015年5月20日上午12:01:01
     * @param ef
     * @param formId
     * @param summary
     * @param user
     * @return
     * @throws EdocException
     */
    @Override
    public String getFormContentWithFix(EdocForm ef, long formId, EdocSummary summary) throws EdocException{
        
        String content = ef.getContent();
        boolean toFix = Strings.isBlank(content);
        if(!toFix){
            for(int i = 0, len = EdocFormHelper.SPECIAL_FORM_FONT_SIZE.length; i < len; i++){
                String font = EdocFormHelper.SPECIAL_FORM_FONT_SIZE[i][0];
                if(content.indexOf(font) > 0){
                    toFix = true;
                    break;
                }
            }
        }
        
        if(toFix){
            try {
                if(ef.getIsSystem()){//如果是系统公文单走系统公文单的容错处理逻辑
                    this.updateFormContentToDBOnly();
                    ef=edocFormDao.get(formId);
                    if(ef==null)
                    {
                        User user =AppContext.getCurrentUser();
                        ef = getDefaultEdocForm(user.getAccountId(),summary.getEdocType());
                    }
                    content = ef.getContent();
                } else {//再次验证内容是否为空，为空则进行容错处理
                    
                    Long fileId = ef.getFileId();
                    if(fileId == null) {
                        fileId = 0L;
                    }
                    V3XFile v3xfile = fileManager.getV3XFile(fileId);
                    if(v3xfile != null) {
                        LOGGER.info("公文单ID：" + ef.getId() + "xslt内容为空，进行容错处理.");
                        String[] urls = new String[1];
                        urls[0] = v3xfile.getId().toString();
                        
                        String[] createDates = new String[1];
                        createDates[0] = Datetimes.formatDatetime(v3xfile.getCreateDate());
                        
                        String[] mimeTypes = new String[1];
                        mimeTypes[0] = v3xfile.getMimeType().toString();
                        String[] names = new String[1];
                        names[0] = v3xfile.getFilename().toString();
                            
                        String xsnFilePath = this.getDirectory(urls, createDates, mimeTypes, names);
                        String xsl = EdocFormHelper.parseInfoPathXSL(xsnFilePath);
                        if(Strings.isNotBlank(xsl)){
                            content = xsl;
                            ef.setContent(content);
                            edocFormDao.update(ef);//更新数据库
                        }
                    } else {
                        LOGGER.error("容错处理时,公文单.xsn文件未找到,公文单ID：" + ef.getId());
                    }
                }
            } catch (Exception e) {
            	if(ef != null){            		
            		LOGGER.error("展现表单时，容错处理抛出异常， 公文单ID:" + ef.getId(), e);
            	}
                throw new EdocException(e);
            }
        }
        
        return content;
    }
    
    @Override
    public EdocFormModel getEdocFormModel(long formId,EdocSummary edocSummary,long actorId,boolean isTemplete,boolean isCallTemplete) throws EdocException{
        return getEdocFormModel(formId, edocSummary, actorId, isTemplete, isCallTemplete, null);
    }
    public EdocFormModel getEdocFormModel(long formId,long actorId) throws EdocException
    {
    	EdocSummary edocSummary=new EdocSummary();
    	return getEdocFormModel(formId,edocSummary,actorId);
    }
    
    public void deleteFormElementByFormId(long id){
    	
    	List<EdocFormElement> list = edocFormElementDao.findBy("formId", id);
    	for(EdocFormElement ele:list){
    		edocFormElementDao.delete(ele);
    	}
    }
    
    /**
     * ajax方法
     * 方法描述：是否引用
     *
     */
    public String ajaxIsReferenced(String id)throws Exception{
    	boolean bool = edocFormDao.isReferenced(Long.valueOf(id));
		 if(bool){
				return "TRUE";
		}else{
			return "FALSE";
		}
    }
    
    /**
     * 判断是否被引用
     * 是:弹出提示,不允许删除
     * 否:首先删除引用的公文元素,其次删除公文单样式文件
     */
	 public String deleteForm(long id) throws Exception{
		 
		 List<EdocForm> edocList = edocFormDao.findBy("id", id);
		 EdocForm ef=edocList.get(0);
		 Set<EdocFormExtendInfo> infos = ef.getEdocFormExtendInfo();
		 for(EdocFormExtendInfo info : infos){
			 if(info.getIsDefault())
			 {
				 removeDefaultEdocForm(info.getAccountId(),ef.getType());			 
			 }
		 }
		
		 boolean bool = edocFormDao.isReferenced(id);
		 if(bool){
			return "alert(parent._('edocLang.edoc_form_referenced'));";
		 }
		 try{
		 attachmentManager.deleteByReference(id, id);
		 }catch(Exception e){
			 LOGGER.error("deleteForm函数，删除公文单错误，id="+id,e);
			 throw e;
		 }
		 		 
		 this.deleteFormElementByFormId(id);
		 edocFormDao.delete(ef);
		 return null;
	 }
	  public void updateForm(EdocForm edocForm){
		  edocFormDao.update(edocForm);
	  }
	  
    public String getDirectory(String[] urls, String[] createDates, String[] mimeTypes, String[] names)
            throws Exception {

        File file = null;
        try {
            for (int i = 0; i < urls.length; i++) {
                Long fileId = Long.parseLong(urls[i]);

                Date createDate = null;
                if (createDates[i].length() > 10)
                    createDate = Datetimes.parseDatetime(createDates[i]);
                else
                    createDate = Datetimes.parseDate(createDates[i]);
                file = fileManager.getFile(fileId, createDate);
                if (null == file)
                    return "";
            }
        } catch (Exception e) {
            LOGGER.error("获取公文单目录出错", e);
            return "";
        }

        return file.getPath();
    }
	  
	  public void updateDefaultEdocForm(long domainId,int type, Long subType,boolean hasSubType){
		//  edocFormDao.updateDefaultEdocForm(domainId, type);
		  //OA-45600发文单可以设置两个默认的公文单
		  //这个查询跟subType没有关系，因为subtype是即将改为默认的文单的值，而这里查询是之前默认的文单，所以subType传-1.
		edocFormExtendInfoDao.cancelDefaultEdocForm(domainId, type, subType,hasSubType);
	  }
	  
	  public void updateDefaultEdocForm(EdocFormExtendInfo newDefaultInfo,long domainId,int type, Long subType,boolean hasSubType) {
	      this.updateDefaultEdocForm(domainId, type, subType,hasSubType);
	      if(newDefaultInfo != null) {
	          newDefaultInfo.setIsDefault(true);
	          edocFormExtendInfoDao.update(newDefaultInfo);
	      }
	  }
	  
	  public void initAccountEdocForm(long accountId) throws Exception
	  {
		  List <EdocForm> forms=edocFormDao.getAllEdocForms(0L);
		  for(EdocForm form :forms)
		  {
			  form=(EdocForm)form.clone();
			  form.resetId();
			  form.setDomainId(accountId);
			  edocFormDao.save(form);
		  }
	  }
	  public void importEdocForm(String formIds) throws Exception
	  {
		  List <EdocForm> forms=edocFormDao.getEdocForms(formIds);
		  for(EdocForm form :forms)
		  {
			  form=(EdocForm)form.clone();
			  form.resetId();
			  form.setIsDefault(false);
			  form.setDomainId(AppContext.getCurrentUser().getLoginAccount());
			  edocFormDao.save(form);
		  }
	  }
	  /*************默认公文单 start************/
	  public EdocForm getDefaultEdocForm(Long domainId,int edocType, long subType) {
		  
		  //每次都读数据库了，缓存无用了，TODO。
		  EdocForm tempForm = null; 
		  
		  if(subType != -1) {
			  tempForm = getDefaultEdocFormFromDB(domainId, edocType, subType);
			  if(tempForm!=null) {
				  defaultEdocForm.put(domainId.toString()+"_"+edocType+"_"+subType, tempForm);
			  }
		  } else {
			  tempForm = getDefaultEdocFormFromDB(domainId, edocType);
			  if(tempForm!=null) {
				  defaultEdocForm.put(domainId.toString()+"_"+edocType, tempForm);
			  }
		  }
		  if(tempForm != null) {
			  /** 因为公文单不一定是本单位的，这里需要加载公文单所在单位的公文元素 */
			  edocElementManager.loadEdocElementByDomainId(tempForm.getDomainId());
		  }
		 
		  return tempForm;		  
	  }	 
	  public EdocForm getDefaultEdocForm(Long domainId,int edocType) {
		  
		  return getDefaultEdocForm(domainId, edocType, -1);	
		  
	  }
	  /*************默认公文单 end************/
	  
	  /************* 默认公文单 start************/
	  private EdocForm getDefaultEdocFormFromDB(Long domainId, int edocType, long suType){
		  User user =AppContext.getCurrentUser();
		  String domainIds = "";
		  try {
			 domainIds= orgManager.getUserIDDomain(user.getId(), user.getLoginAccount(), V3xOrgEntity.ORGENT_TYPE_ACCOUNT);
			 //加集团Id
			 domainIds = Strings.join(",", String.valueOf(orgManager.getRootAccount().getId()));
			 
		  } catch (BusinessException e) {
			LOGGER.error("获取默认公文单的时候查找domain对象异常",e);
		  }
		  return _getDefaultEdocForm(domainId,domainIds,edocType, suType);
	  }
	  private EdocForm getDefaultEdocFormFromDB(Long domainId, int edocType){
		  return getDefaultEdocFormFromDB(domainId,edocType,-1);
	  } 
	  /************* 默认公文单 end************/
	  
	  /************* 默认公文单 start************/
	  public EdocFormExtendInfo getEdocFormExtendInfoByForm(EdocForm edocForm ,Long loginAccount){
		  Set<EdocFormExtendInfo> edocFormExtendInfos = edocForm.getEdocFormExtendInfo();
		  if(Strings.isNotEmpty(edocFormExtendInfos)){
			  for(EdocFormExtendInfo _info:edocFormExtendInfos){
				  if(loginAccount.equals(_info.getAccountId())){
					  return _info;
				  }
			  }
		  }
		  return null;
	  }
	  /**
	   * 如果没有设置默认公文单，返回第一个公文单为默认公文单
	   * @param domainId
	   * @param edocType
	   * @return
	   */
	  private EdocForm _getDefaultEdocForm(Long domainId,String domainIds,int edocType, long subType) {
		  EdocForm edocForm=null;
		  List<EdocForm> ls = edocFormDao.getEdocForms(domainId,domainIds, edocType, subType);
		  Long loginAccountId = domainId;
		  /*User user = AppContext.getCurrentUser();
		  if(user!=null){
			  loginAccountId = user.getLoginAccount();
		  }*/
		  for(Iterator<EdocForm> it = ls.iterator();it.hasNext();) {
			  EdocForm ef = it.next();
			  EdocFormExtendInfo info = getEdocFormExtendInfoByForm(ef , loginAccountId);
			  if(!EdocForm.C_iStatus_Published.equals(info.getStatus())){
				  it.remove();
				  continue;
			  }
			  if(info.getIsDefault()) {
				  edocForm=ef;
				  break;
			  }
		  }
		  //没有设置默认公文单
		  if(edocForm==null) {
			  if(ls!=null && ls.size()>0) {
				  edocForm=ls.get(0);
			  }
		  }
		  if(edocForm != null) {
			  /** 因为公文单不一定是本单位的，这里需要加载公文单所在单位的公文元素 */
			  edocElementManager.loadEdocElementByDomainId(edocForm.getDomainId());
		  }
		  return edocForm;
	  }
/*findbugs修正，从未用到的私有方法
 * 	  private EdocForm _getDefaultEdocForm(Long domainId,String domainIds,int edocType) {
		  return _getDefaultEdocForm(domainId, domainIds, edocType, -1);
	  }*/
	  /************* 默认公文单 end************/
	  
	  public void setDefaultEdocForm(Long domainId,int edocType,EdocForm edocForm) {
		  defaultEdocForm.put(domainId.toString()+"_"+edocType,edocForm);		  
	  }
	  
	  public void removeDefaultEdocForm(Long domainId,int edocType)
	  {
		  defaultEdocForm.remove(domainId.toString()+"_"+edocType);		
		  NotificationManager.getInstance().send(NotificationType.DefaultEdocFormRemove, new Object[]{domainId,edocType});
	  }
	  
	  /**
	   * 检查公文单是否有重名
	   * @param name 名称
	   * @param type 类型(发文,收文,签报)
	   * @param status 状态,是否为启用
	   * @param domainId 单位标识
	   * @return
	   */
	  public boolean checkHasName(String name,int type){
		  
		  boolean bool = false;
		  
		  User user = AppContext.getCurrentUser();
		  
		  return edocFormDao.getEdocFormByName(null,user.getLoginAccount(), name, type)>0;
		 
	  }
	  
	  /**
	   * 方法描述：ajax方法，动态判断是否重名
	   *
	   */
	  public boolean ajaxCheckDuplicatedName(String name, String type, String id){
		  boolean bool = false;
		  
		  User user = AppContext.getCurrentUser();
		  
		 return edocFormDao.getEdocFormByName(id,user.getLoginAccount(), name,  Integer.valueOf(type).intValue())>0;
		
	  }
	  
	  /**
	   * 方法描述：ajax方法，动态判断本单位是否存在该文单
	   *
	   */
	  public boolean ajaxCheckIsExistInUnit(String formId){
		 User user = AppContext.getCurrentUser();
		 return edocFormDao.isExsitInUnit(formId,user.getLoginAccount());
	  }
	  
	  public void bound(String name, String boundName, String boundNameLabel, long edocFormId, String sortType,Long accountId)throws Exception{
	
		  List<EdocFormFlowPermBound> list = new ArrayList<EdocFormFlowPermBound>();
		  String[] boundList = boundName.split(",");
		  String[] boundNameList = boundNameLabel.split(",");
		  for(int i=0;i<boundList.length;i++){
			  EdocFormFlowPermBound bound = new EdocFormFlowPermBound();
			  bound.setIdIfNew();
			  bound.setEdocFormId(edocFormId);
			  bound.setFlowPermName(boundList[i]);
			  bound.setProcessName(name);
			  bound.setFlowPermNameLabel(boundNameList[i]);
			  bound.setSortType(sortType);
			  bound.setDomainId(accountId);
			  list.add(bound);
		  }
		  edocFormFlowPermBoundDao.saveAll(list);
	  }
	 
	  public List<EdocFormFlowPermBound> findBoundByFormId(long edocFormId)throws Exception{
		  
		  DetachedCriteria criteria = DetachedCriteria.forClass(EdocFormFlowPermBound.class);	
		  criteria.add(Restrictions.eq("edocFormId", edocFormId));
		  
		  List<EdocFormFlowPermBound> list = edocFormFlowPermBoundDao.executeCriteria(criteria, -1, -1);	 
		  
		  return list;
	  }
	  
	  public List<EdocFormFlowPermBound> findBoundByFormIdAndDomainId(long edocFormId,Long accountId)throws Exception{
		  
		  DetachedCriteria criteria = DetachedCriteria.forClass(EdocFormFlowPermBound.class);	
		  criteria.add(Restrictions.eq("edocFormId", edocFormId));
		  criteria.add(Restrictions.eq("domainId", accountId));
		  
		  List<EdocFormFlowPermBound> list = edocFormFlowPermBoundDao.executeCriteria(criteria, -1, -1);	 
		  
		  return list;
	  }
	  public List<EdocFormFlowPermBound> findBoundByFormId(long edocFormId, String processName)throws Exception{
		  
		  DetachedCriteria criteria = DetachedCriteria.forClass(EdocFormFlowPermBound.class);	
		  criteria.add(Restrictions.eq("edocFormId", edocFormId));
		  criteria.add(Restrictions.eq("processName", processName));
		  
		  List<EdocFormFlowPermBound> list = edocFormFlowPermBoundDao.executeCriteria(criteria, -1, -1);	 
		  
		  return list;
	  }
	  public List<EdocFormFlowPermBound> findBoundByFormId(long edocFormId, String processName,long accountId)throws Exception{
		  
		  DetachedCriteria criteria = DetachedCriteria.forClass(EdocFormFlowPermBound.class);	
		  criteria.add(Restrictions.eq("edocFormId", edocFormId));
		  criteria.add(Restrictions.eq("processName", processName));
		  criteria.add(Restrictions.eq("domainId", accountId));
		  List<EdocFormFlowPermBound> list = edocFormFlowPermBoundDao.executeCriteria(criteria, -1, -1);	 
		  
		  return list;
	  }
	  public List<EdocFormFlowPermBound> findBoundByFormId(long edocFormId, long accountId, String flowPermName)throws Exception{
		  
		  DetachedCriteria criteria = DetachedCriteria.forClass(EdocFormFlowPermBound.class);	
		  criteria.add(Restrictions.eq("edocFormId", edocFormId));
		  criteria.add(Restrictions.eq("domainId", accountId));
		  criteria.add(Restrictions.eq("flowPermName", flowPermName));
		  
		  List<EdocFormFlowPermBound> list = edocFormFlowPermBoundDao.executeCriteria(criteria, -1, -1);	 
		  
		  return list;
	  }
	  public List<EdocElement> getEdocFormElementByFormIdAndFieldName(long edocFormId, String fieldName){
		  return edocFormElementDao.getEdocFormElementByFormIdAndFieldName(edocFormId, fieldName);
	  }
	  public void deleteEdocFormFlowPermBoundByFormId(long edocFormId)throws Exception{
		  
		  edocFormFlowPermBoundDao.deleteFormFlowPermBoundByFormId(edocFormId);
		  
	  }
	  public void deleteEdocFormFlowPermBoundByFormIdAndAccountId(long edocFormId,long accountId)throws Exception{
		  
		  edocFormFlowPermBoundDao.deleteFormFlowPermBoundByFormId(edocFormId,accountId);
		  
	  }

	public EdocFormFlowPermBoundDao getEdocFormFlowPermBoundDao() {
		return edocFormFlowPermBoundDao;
	}

	public void setEdocFormFlowPermBoundDao(
			EdocFormFlowPermBoundDao edocFormFlowPermBoundDao) {
		this.edocFormFlowPermBoundDao = edocFormFlowPermBoundDao;
	}

	public Hashtable<String,String> getOpinionLocation(Long edocFormId,Long accountId)
	{
		Hashtable<String, String> hs=new Hashtable<String,String>();
		List <EdocFormFlowPermBound> ls=edocFormFlowPermBoundDao.findVarargs("from EdocFormFlowPermBound b where b.edocFormId=? and b.domainId=? ", new Object[]{edocFormId,accountId});
		for(EdocFormFlowPermBound eb:ls)
		{
			// hs.put(eb.getFlowPermName(),eb.getProcessName());
			// 公文元素名称为value_公文元素的排序方式
		    if(eb.getFlowPermName() != null){
		        hs.put(eb.getFlowPermName(), eb.getProcessName() + "_"
	                    + eb.getSortType());
		    }
		}
		return hs;
	}
	
	public List<String> getOpinionElementLocationNames(Long edocFormId,Long aclAccountId)
	{
		
		Hashtable<String,String> fbs= getOpinionLocation(edocFormId,aclAccountId);
		List <String> ens=new ArrayList<String>();
		Enumeration en = fbs.keys();
		String szStr;
		while(en.hasMoreElements())
		{
			szStr=fbs.get(en.nextElement().toString());
			if(ens.contains(szStr)==false)
			{
				ens.add(szStr);
			}
		}		
		return ens;
	}
	public List<String> getOpinionElementLocationNames(Long edocFormId)
	{
		User user = AppContext.getCurrentUser();
		return getOpinionElementLocationNames(edocFormId,user.getLoginAccount());
	}
	public void initialize(){
		baseFileFolder = SystemProperties.getInstance().getProperty("edoc.folder");
		try{
		LOGGER.info("执行公文单,公文模板,岗位导入模板,人员导入模板文件检查与复制...");
		copyEdocFile();
		}catch(Exception e){
			LOGGER.error("复制公文单与公文模板,岗位导入模板,人员导入模板文件失败", e);
		}
        updateFormContentToDBOnly();
	}
	
	/**
	 * 检查是否有公文单及套红模板文件存，如果不存在复制一份到指定分区
	 * @throws Exception
	 */
	private void copyEdocFile()throws Exception{
		
		String[] t_FileIds = new String[1];
		
		t_FileIds[0] = "-6001972826857714844"; //套红模板文件压缩包
		
		
		String[] f_FileIds = new String[3];
		// -- 公文单（签报，收文，发文）
		f_FileIds[0] = "-1766191165740134579"; 
		f_FileIds[1] = "-2921628185995099164";
		f_FileIds[2] = "6071519916662539448";		
		
		String[] o_FileIds = new String[2];
		//人员导入模板,岗位导入模板
		o_FileIds[0] = "43263267400010875";
		o_FileIds[1] = "-6777944130366976701";
		
		this.copyFile(t_FileIds, Constants.EDOC_FILE_TYPE_TEMPLATE);
		this.copyFile(f_FileIds, Constants.EDOC_FILE_TYPE_EDOCFORM);
		//不需要拷贝
		//1.物理目录下面文件名是member,post
		//2.模板下载的时候，文件名也是member,post.故不需要拷贝文件。
		//this.copyFile(o_FileIds, Constants.ORGNIZATION_FILE_TYPE);
		
	}
	
	private void copyFile(String[] fileIds, int type)throws Exception{
		
		String fileFolder = baseFileFolder;
		if(type == Constants.EDOC_FILE_TYPE_EDOCFORM){
			fileFolder += formFolder;
		}
		else if(type == Constants.EDOC_FILE_TYPE_TEMPLATE){
			fileFolder += templateFolder;
		}else if(type ==Constants.ORGNIZATION_FILE_TYPE){
			fileFolder += orgFolder; 
		}
		
		for(String id : fileIds){
			V3XFile v3xFile= fileManager.getV3XFile(Long.valueOf(id));
			if(null != v3xFile){
				File file = fileManager.getFile(v3xFile.getId()); 
					if(null == file){
						File tempFile = new File(fileFolder+ File.separator + id);
						if(null!=tempFile){
							String folder = fileManager.getFolder(new Date(), true);
							v3xFile.setUpdateDate(new Date());
							v3xFile.setCreateDate(new Date());
							fileManager.update(v3xFile);
							//Attachment attachment  = attachmentManager.getAttachmentByFileURL(v3xFile.getId());
							//attachment.setCreatedate(new Date());
							//attachmentManager.update(attachment);
							try{
                                File in = new File(fileFolder + File.separator + id);
                                File out = new File(folder + File.separator + id);
                                FileUtils.copyFile(in, out);
							}catch(Exception e){
								LOGGER.info("复制文件失败 id = " + id);
							}
						}
					}
			}
		}
		
	}
	
	public void saveEdocForm(EdocForm form){
		edocFormDao.save(form);
	}
	public void saveEdocFormExtendInfo(EdocFormExtendInfo form){
		edocFormExtendInfoDao.save(form);
	}
	/**
	 * 公文单的Content字段相对较大,无法使用初始化的方式插入
	 * 现插入3条记录，然后读取xml中得样式文件，将对应的数据插入到edocForm 中的 Content 字段中去
	 * 12-19修改，为统一插入sql，所有的数据库都一样，用读取xml插入
	 */
    public void updateFormContentToDBOnly() {
        // String dbType = com.seeyon.apps.doc.util.Constants.getDBType();
        // if("oracle".equalsIgnoreCase(dbType) ||
        // "sqlserver".equalsIgnoreCase(dbType)){
        try {
            LOGGER.info("预置公文单初始化数据......");
            String baseFileFold = SystemProperties.getInstance().getProperty("edoc.folder");
            byte[] byteArray = StringUtils.readFileData(baseFileFold + File.separator + "all.xml");
            String path = IOUtils.toString(byteArray, "UTF-8");
            String sendXsl = path.substring(path.indexOf(Constants.EDOC_EDOCFORM_XSL_SEND_START)
                    + Constants.EDOC_EDOCFORM_XSL_SEND_START.length(),
                    path.indexOf(Constants.EDOC_EDOCFORM_XSL_SEND_END));
            String recXsl = path
                    .substring(path.indexOf(Constants.EDOC_EDOCFORM_XSL_REC_START)
                            + Constants.EDOC_EDOCFORM_XSL_REC_START.length(),
                            path.indexOf(Constants.EDOC_EDOCFORM_XSL_REC_END));
            // 5.1新增收文阅文文单，原收文单位办文文单
            String recYueWenXsl = path.substring(path.indexOf(Constants.EDOC_EDOCFORM_XSL_REC_YUEWEN_START)
                    + Constants.EDOC_EDOCFORM_XSL_REC_YUEWEN_START.length(),
                    path.indexOf(Constants.EDOC_EDOCFORM_XSL_REC_YUEWEN_END));
            String signXsl = path.substring(path.indexOf(Constants.EDOC_EDOCFORM_XSL_SIGN_START)
                    + Constants.EDOC_EDOCFORM_XSL_SIGN_START.length(),
                    path.indexOf(Constants.EDOC_EDOCFORM_XSL_SIGN_END));

            // 5.1sp2新增预置收文单(用于收文转收文)
            String recOpinionXsl = path.substring(path.indexOf(Constants.EDOC_EDOCFORM_XSL_REC_OPINION_START)
                    + Constants.EDOC_EDOCFORM_XSL_REC_OPINION_START.length(),
                    path.indexOf(Constants.EDOC_EDOCFORM_XSL_REC_OPINION_END));

            /*
            ProductEditionEnum productEdition = ProductInfo.getEdition();
            if(productEdition.ordinal() == ProductEditionEnum.enterprise.ordinal() || productEdition.ordinal() == ProductEditionEnum.government.ordinal()){
                formList = this.getAllEdocForms(Long.valueOf("670869647114347"));//如果是企业版，复制此单位     
            }
            else if(productEdition.ordinal() == ProductEditionEnum.entgroup.ordinal() || productEdition.ordinal() == ProductEditionEnum.governmentgroup.ordinal()){
                formList= this.getAllEdocForms(V3xOrgEntity.VIRTUAL_ACCOUNT_ID);    //集团版
            }               
            */
            
            List<Long> domainIds = new ArrayList<Long>();

            if (((Boolean) SysFlag.sys_isGroupVer.getFlag()) == false) {
                domainIds.add(OrgConstants.ACCOUNTID);
            } else {
                domainIds.add(V3xOrgEntity.VIRTUAL_ACCOUNT_ID);

                //集团版
                List<V3xOrgAccount> unitList = orgManager.getAllAccounts();

                for (V3xOrgAccount unit : unitList) {
                    domainIds.add(unit.getId());
                }

            }

            for (Long accountId : domainIds) {

                List<EdocForm> formList = this.getToFixSysEdocForms(accountId);

                for (EdocForm form : formList) {

                    if (form.getIsSystem() && Strings.isBlank(form.getContent())) {

                        if (form.getType().intValue() == Constants.EDOC_FORM_TYPE_SEND) {
                            
                            form.setContent(sendXsl);
                            LOGGER.info("初始化发文单......");
                            this.updateForm(form);
                            
                        }else if (form.getType().intValue() == Constants.EDOC_FORM_TYPE_REC) {
                            
                            // TODO V5.1
                            // sprint1——杨帆——因为收文还没有区分办文和阅文，只有在做完收文办文阅文的区分之后，才能根据具体的区分标识来写这里，暂时这么写
                            if ("收文单（阅文）".equals(form.getName())) {
                                form.setContent(recYueWenXsl);
                                LOGGER.info("初始化收文（阅文）单......");
                            } else if ("收文单".equals(form.getName())) {
                                form.setContent(recXsl);
                                LOGGER.info("初始化收文（办文）单......");
                            }
                            // 汇报意见收文单(用于收文转收文)
                            else if ("收文单（转收文）".equals(form.getName())) {
                                form.setContent(recOpinionXsl);
                                LOGGER.info("初始化汇报意见收文单......");
                            }

                            this.updateForm(form);
                            
                        }else if (form.getType().intValue() == Constants.EDOC_FORM_TYPE_SIGN) {
                            
                            form.setContent(signXsl);
                            LOGGER.info("初始化签报单......");
                            this.updateForm(form);
                            
                        }
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("公文单初始化失败!", e);
        }
        LOGGER.info("公文单初始化完毕!");
        // }
    }	
	
	/** 
	 * 把公文单中的元素数据转换成表单对象,用于设置分支条件
	 * @param edocForm
	 * @return
     */
//	public List<WorkflowFormFieldVO> getElementByEdocForm(Long formId)
//	{
//
//		EdocForm edocForm=getEdocForm(formId);
//		List<WorkflowFormFieldVO> tfList=new ArrayList<WorkflowFormFieldVO>();
//		Set<EdocFormElement> eles=null;
//		try{
//			eles=edocForm.getEdocFormElements();
//		}catch(Exception e)
//		{
//			eles = getEdocForm(formId).getEdocFormElements();
//		}
//		for(EdocFormElement efe:eles)
//		{
//		 try{
//			 WorkflowFormFieldVO tf=new WorkflowFormFieldVO();
//			    EdocElement ele=edocElementManager.getEdocElementsById(efe.getElementId());
//				if(ele.getType()==EdocElement.C_iElementType_Decimal 
//					|| ele.getType()==EdocElement.C_iElementType_Integer
//					|| ele.getType()==EdocElement.C_iElementType_List){
//					tf.setInputType(FormFieldComBean.FormFieldComEnum.TEXT.getKey());
//					switch(ele.getType())
//					{
//						case EdocElement.C_iElementType_Date:
//							tf.setFieldType(FieldType.TIMESTAMP.getKey());
//							break;
//						case EdocElement.C_iElementType_Decimal:
//						case EdocElement.C_iElementType_Integer:
//							tf.setFieldType(FieldType.DECIMAL.getKey());
//							break;
//						case EdocElement.C_iElementType_List:
//							//如果是单位枚举，通过id+domain取公文元素
//							if(!ele.getIsSystem())
//								ele=edocElementManager.getEdocElement(efe.getElementId().toString());
//							tf.setInputType(FormFieldComBean.FormFieldComEnum.SELECT.getKey());
//							tf.setEnumId(ele.getMetadataId());
//							//tf.setTablename("main");
//							tf.setFieldType(FieldType.VARCHAR.getKey());
//							break;
//					}
//					tf.setName(ele.getFieldName());	
//					tf.setDisplay(ResourceUtil.getString(ele.getName()));
//					tfList.add(tf);
//				}
//			}catch(Exception e){
//				LOGGER.debug("",e);
//			}
//		}
//
//		return tfList;
//	}
//		 
	
	/**
	 * 方法描述: 二选一的参数
	 * ajaxCheckFormIsIdealy(String, isDefault, "") -- 是否默认公文单， is default form or not
	 * ajaxCheckFormIsIdealy(String, "", isEnabled) -- 是否是启用公文单，whether the form is enabled or disabled
	 */
	public boolean ajaxCheckFormIsIdealy(String edocFormStatusId, String isDefault, String isEnabled){
		if(!Strings.isBlank(edocFormStatusId)){
			EdocFormExtendInfo edocFormExtendInfo = this.getEdocFormExtendInfo(Long.valueOf(edocFormStatusId));
			if(null!=edocFormExtendInfo){
				if(!Strings.isBlank(isDefault)){
					return edocFormExtendInfo.getIsDefault();
				}
				if(!Strings.isBlank(isEnabled)){
					return edocFormExtendInfo.getStatus().intValue() == Constants.EDOC_DOCTEMPLATE_DISABLED ? false : true;	
				}
			}
		}
		return false;
	}
	
	/*********************************** 唐桂林 公文意见显示 start *************************************/
	/**
	 * 取得公文单意见显示设置
	 * @param formId  ： 公文单ID
	 * @param accountId ：单位ID
	 * @return
	 */
	public FormOpinionConfig getEdocOpinionDisplayConfig(Long formId, Long accountId) {
	    
		FormOpinionConfig displayConfig = null;
		// 公文单显示格式
		try {
			EdocForm form = getEdocForm(formId);
			Set<EdocFormExtendInfo> infos = form.getEdocFormExtendInfo();
		    for(EdocFormExtendInfo info : infos) {
	            if(info.getAccountId().equals(accountId)) {
	                displayConfig = JSONUtil.parseJSONString(info.getOptionFormatSet(), FormOpinionConfig.class);
	                break;
	            }
	        }
        } catch (Exception e) {
            LOGGER.error("获取公文单意见配置错误, ID=" + formId, e);
        }
		if(displayConfig == null){
		    displayConfig = new FormOpinionConfig();
		}
		return displayConfig;
	}
	/*********************************** 唐桂林 公文意见显示 end *************************************/
	
	/**
	 * lijl添加,通过参数获取EdocFormExtendInfo对象
	 * @param formId 
	 * @param accountId
	 * @return EdocFormExtendInfo对象
	 */
	public EdocFormExtendInfo getEdocOpinionConfig(Long formId,Long accountId){
		EdocFormExtendInfo edocFormExtendInfo=null;
		EdocForm form = getEdocForm(formId);
		Set<EdocFormExtendInfo> infos = form.getEdocFormExtendInfo();
		for(EdocFormExtendInfo info : infos ){
			if(info.getAccountId().equals(accountId)){
				edocFormExtendInfo = info;
				break;
			}
		}
		return edocFormExtendInfo;
	}
	@Override
	public Hashtable<String, String> getOpinionLocation(Long edocFormId) {
		User user =AppContext.getCurrentUser();
		
		return getOpinionLocation(edocFormId,user.getLoginAccount());
	}
	public  EdocFormExtendInfo getEdocFormExtendInfo(Long id){
		return edocFormExtendInfoDao.get(id);
	}
	
	public List<EdocForm> getEdocFormByAcl(String domainIds){
		return edocFormDao.getEdocFormByAcl(domainIds);
	}
	
	public boolean isExsit(Long formId){
		return edocFormDao.isExsit(formId);
	}

	public List<String> getEdocElementFieldNameByRequired(long formId, Boolean required) {
		List<String> names = new ArrayList<String>();
		List<EdocElement> elementList = edocFormElementDao.getEdocElementByRequired(formId, required);
		if(elementList != null && elementList.size()>0) {
			for(int i=0; i<elementList.size(); i++) {
				names.add(elementList.get(i).getFieldName());
			}
		}
		return names;
	}

	/**
	 * 
	 */
	public Object[] getEdocFormElementRequiredMsg(EdocForm edocForm, EdocSummary summary) throws Exception {
		Boolean isPass = Boolean.TRUE;
		StringBuilder notPassMsg = new StringBuilder();
		if(defaultEdocForm != null) {
    		List<String> names = this.getEdocElementFieldNameByRequired(edocForm.getId(), true);
    		Method[] methods = summary.getClass().getMethods();
    		for (Method method : methods) {   
    			Column c = method.getAnnotation(Column.class);
    			if(c != null && names!=null) {
    				if(names.contains(c.name())) {
    					Object value = method.invoke(summary);
        				if(value == null || "".equals(value)) {
        					notPassMsg.append("《"+EdocHelper.getInterceptStringByLength(summary.getSubject(), 10)+"》,");
        					isPass = Boolean.FALSE;
        					break;
        				}
       				}
    			}
    		}
    	}
		return new Object[]{isPass, notPassMsg.toString()};
	}

    @Override
    public void updateEdocFormElement(List<EdocFormElement> edocFormElements) throws Exception{
        edocFormDao.updatePatchAll(edocFormElements);
    }
	
	public String getFormIdByWorkflowId(String workflowId,boolean isSystem){
		CtpTemplate template = this.templateManager.getCtpTemplateByWorkFlowId(Long.parseLong(workflowId), isSystem);
		EdocSummary summary = (EdocSummary) XMLCoder.decoder(template.getSummary());
		if(summary != null && summary.getFormId() != null){
			return "{formId:'" + summary.getFormId().toString() + "'}"; 
		}
		return "{formId:0}";
	}
	/**
	 *验证当前文单是否属于本单位
	 * @param domainId
	 * @param type  
	 * @return
	 */
	public boolean getFormAccountEdoc(Long domainId,long edocFormId){
		List<EdocForm> accountForms=edocFormDao.getFormAccountEdoc(domainId, edocFormId);
		if(Strings.isNotEmpty(accountForms)){
			return true;//证明此文旦属于本单位
			
		}else{
			return false;//证明此文旦属于外单位授权
		}
		
		
	}

	@Override
	public void saveEdocForms(List<EdocForm> forms) {
		DBAgent.saveAll(forms);
	}
	
	
	@Override
	public boolean writeForm2File(Long summaryId, String folder) {

	    return FormParseUtil.writeForm2File(summaryId, folder);
	}
	
	@Override
	public boolean writeForm2File2(Long summaryId, String folder) {

	    return FormParseUtil.writeForm2File2(summaryId, folder);
	}
	
	
	
	public Boolean hasFormElement(Long formId, String fieldName) {
		Boolean hasElement = Boolean.FALSE;
		List<EdocFormElement> elementList = this.getEdocFormElementByFormId(formId);
		for(EdocFormElement formElement : elementList) {
			EdocElement element = edocElementManager.getEdocElementsById(formElement.getElementId());
			if(element!=null && fieldName.equals(element.getFieldName())) {
				hasElement = Boolean.TRUE;
			}
		}
		return hasElement; 
	}

//	@Override
//	public List<WorkflowFormFieldVO> getAllElementsByEdocForm(Long formId) {
//		EdocForm edocForm=getEdocForm(formId);
//		List<WorkflowFormFieldVO> tfList=new ArrayList<WorkflowFormFieldVO>();
//		Set<EdocFormElement> eles=null;
//		try{
//			eles=edocForm.getEdocFormElements();
//		}catch(Exception e){
//			eles = getEdocForm(formId).getEdocFormElements();
//		}
//		for(EdocFormElement efe:eles){
//			try{
//				 WorkflowFormFieldVO tf=new WorkflowFormFieldVO();
//				 EdocElement ele=edocElementManager.getEdocElementsById(efe.getElementId());
//				 if(ele.getType()==EdocElement.C_iElementType_String || ele.getType()==EdocElement.C_iElementType_Text || ele.getType()==EdocElement.C_iElementType_Comment){
//				 		tf.setFieldType(FieldType.VARCHAR.getKey());
//				 		tf.setInputType(FormFieldComBean.FormFieldComEnum.TEXT.getKey());
//				 }else if(ele.getType()==EdocElement.C_iElementType_Decimal || ele.getType()==EdocElement.C_iElementType_Integer){
//				 		tf.setFieldType(FieldType.DECIMAL.getKey());
//				 		tf.setInputType(FormFieldComBean.FormFieldComEnum.TEXT.getKey());
//				 }else if(ele.getType()== EdocElement.C_iElementType_Date){
//				 		tf.setFieldType(FieldType.TIMESTAMP.getKey());
//				 		tf.setInputType(FormFieldComBean.FormFieldComEnum.EXTEND_DATE.getKey());
//				 }else if(ele.getType()==EdocElement.C_iElementType_List){
//					 	//如果是单位枚举，通过id+domain取公文元素
//						if(!ele.getIsSystem()){
//							ele=edocElementManager.getEdocElement(efe.getElementId().toString());
//						}
//						tf.setFieldType(FieldType.VARCHAR.getKey());
//						tf.setInputType(FormFieldComBean.FormFieldComEnum.SELECT.getKey());
//						tf.setEnumId(ele.getMetadataId());
//				 }
//				 tf.setMasterField(true);
//				 tf.setName(ResourceUtil.getString(ele.getName()));
//				 tf.setDisplay(ResourceUtil.getString(ele.getName()));
//				 tf.setFieldName(ele.getFieldName());
//				 tfList.add(tf);
//				 
//			}catch(Exception e){
//					LOGGER.warn("",e);
//			}
//		}
//		return tfList;
//	}
	
	@Override
	public int countExtendInfo(Long formId,Long accountId){
	    return edocFormExtendInfoDao.countExtendInfo(formId, accountId);
	}
	
}