/**
 * 
 */
package com.seeyon.ctp.common.filemanager.manager;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.SystemConfig;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.Constants;
import com.seeyon.ctp.common.filemanager.dao.AttachmentDAO;
import com.seeyon.ctp.common.filemanager.event.AttachmentSaveEvent;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.office.trans.util.OfficeTransHelper;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.datasource.annotation.DataSourceName;
import com.seeyon.ctp.datasource.annotation.ProcessInDataSource;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:tanmf@seeyon.com">Tanmf</a>
 * @version 1.0 2006-11-16
 */
@ProcessInDataSource(name = DataSourceName.BASE)
public class AttachmentManagerImpl implements AttachmentManager {
    private static Log    log = CtpLogFactory.getLog(AttachmentManagerImpl.class);

    private FileManager   fileManager;

    private AttachmentDAO attachmentDAO;
    
    private SystemConfig  systemConfig;
    
    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void setAttachmentDAO(AttachmentDAO attachmentDAO) {
        this.attachmentDAO = attachmentDAO;
    }
    
    public void setSystemConfig(SystemConfig systemConfig) {
		this.systemConfig = systemConfig;
	}

	public String create(Collection<Attachment> attachments) {
        return this.create(attachments,true);
    }

    @Override
    public String create(Collection<Attachment> attachments, boolean needSetSort) {
        Set<Integer> r = new HashSet<Integer>();
        User user = AppContext.getCurrentUser();
        if (user == null) {
            return null;
        }

        int i = 0;
        for (Attachment attachment : attachments) {
            if(needSetSort) attachment.setSort(i++);
            saveAttachment(attachment, user);
            r.add(attachment.getType());
        }

        return parseR(r);
    }

    private void saveAttachment(Attachment attachment, Long userId, Long accountId) {
        attachment.setIdIfNew();

        /*******************************************************************
         * 只有关联文档不写入文件总表
         */
                if (attachment.getType() != Constants.ATTACHMENT_TYPE.DOCUMENT.ordinal()) {
/*                    if (attachment.getType() != Constants.ATTACHMENT_TYPE.DOCUMENT.ordinal()
                            && attachment.getType() != Constants.ATTACHMENT_TYPE.FormDOCUMENT.ordinal()) {*/
        V3XFile file = new V3XFile(attachment.getFileUrl());
        file.setCategory(attachment.getCategory());
        file.setType(attachment.getType());
        file.setFilename(attachment.getFilename());
        file.setMimeType(attachment.getMimeType());
        file.setCreateDate(attachment.getCreatedate());
        file.setSize(attachment.getSize());
        file.setDescription(attachment.getDescription());
        Long fileCreateMember=userId;
        try{
            Long fileUrl=attachment.getFileUrl();
            if(fileUrl!=null){
                V3XFile v3XFile=this.fileManager.getV3XFile(fileUrl);
                if(v3XFile!=null){
                    fileCreateMember=v3XFile.getCreateMember();
                }
            }

        }
        catch (Exception e){
            log.error("根据attachment获取v3xFile出错!",e);
        }
        file.setCreateMember(fileCreateMember);
        file.setAccountId(accountId);
        file.setUpdateDate(new Date());
        this.fileManager.save(file);
    }
        /*        } else { // 关联文档，把FileUrl置为null, 因为有外键
                    attachment.setFileUrl(null);
                }*/
        this.attachmentDAO.save(attachment);

        // 触发附件保存事件。
        EventDispatcher.fireEvent(new AttachmentSaveEvent(attachment, this));
    }

    private void saveAttachment(Attachment attachment, User user) {
        saveAttachment(attachment, user.getId(), user.getAccountId());
    }

    public List<Attachment> getByReference(Long reference) {
        return  setOfficeTransformEnable(this.attachmentDAO.findAll(reference));
    }

    @Override
    public List<Attachment> setOfficeTransformEnable(List<Attachment> list){
        String de = this.systemConfig.get("office_transform_enable");
        if (de != null) {
            for (Attachment att : list) {
                //因为没使用标准接口，导致已经初始过系统开关
/*                if(!"disable".equals(att.getOfficeTransformEnable())){
                    log.error("初始状态不正常！"+att.getFilename()+",传入值为："+att.getOfficeTransformEnable()+",当前系统开关"+de,new Exception());
                }*/
            	if(OfficeTransHelper.allowTrans(att)){
            		att.setOfficeTransformEnable(de);
//            		log.info("设置系统开关"+att.getFilename()+","+att.getOfficeTransformEnable()+",当前系统开关"+de);
            	}else{
            	    att.setOfficeTransformEnable("disable");
            	}
            }
        }
        return list;
    }
    public String getAttListJSON(Long reference) {
        String tem=JSONUtil.toJSONString(setOfficeTransformEnable(fileNameESCHtml(this.attachmentDAO.findAll(reference))));
        return tem;
    }
    private  List<Attachment> fileNameESCHtml(List<Attachment>  list){
        List<Attachment> attlist=new ArrayList<Attachment>();
        for (Attachment att : list) {
            Attachment attc;
            try {
                attc = (Attachment)att.clone();
                attc.setId(att.getId());
                attc.setFilename(Strings.toHTML(attc.getFilename().replace((char)160, (char)32)).replaceAll("&quot;", "\\\""));
                attlist.add(attc);
            } catch (CloneNotSupportedException e) {
                log.error("",e);
            } 
        }
        return attlist;
    }
    private  List<Attachment> fileNameESCJS(List<Attachment>  list){
        List<Attachment> attlist=new ArrayList<Attachment>();
        for (Attachment att : list) {
            Attachment attc;
            try {
                attc = (Attachment)att.clone();
                attc.setId(att.getId());
                attc.setFilename(att.getFilename());
                attc.setFilename(attc.getFilename().replace((char)160, (char)32).replace("'", "\\'"));
                attlist.add(attc);
            } catch (CloneNotSupportedException e) {
                log.error("",e);
            } 
        }
        return attlist;
    }
    public String getAttListJSON(List<Attachment> list) {
        String tem=JSONUtil.toJSONString(setOfficeTransformEnable(fileNameESCHtml(list)));
        return tem;
    }
    
    public String getAttListJSON(Long reference, Long subReference) {
        String tem=JSONUtil.toJSONString(setOfficeTransformEnable(fileNameESCHtml(this.attachmentDAO.findAll(reference, subReference))));
        return tem;
    }
    public String getAttListJSON4JS(Long reference, Long subReference) {
        String tem=JSONUtil.toJSONString(setOfficeTransformEnable(fileNameESCJS(this.attachmentDAO.findAll(reference, subReference))));
        return  tem.replace("\\\\'", "\\'");
    }   
    public String getAttListJSON4JS(Long reference) {
        String tem=JSONUtil.toJSONString(setOfficeTransformEnable(fileNameESCJS(this.attachmentDAO.findAll(reference))));
         return  tem.replace("\\\\'", "\\'");
    }
    public List<Attachment> getByReference(Long reference, Long subReference) {
        return setOfficeTransformEnable(this.attachmentDAO.findAll(reference, subReference));
    }

    public List<Attachment> getByReference(Long reference, Long... subReferences) {
        return setOfficeTransformEnable(this.attachmentDAO.findAll(reference, subReferences));
    }

    public void deleteByReference(Long reference) throws BusinessException {
        deleteByReference(reference, false);
    }

    /**
     * 删除多个附件
     */
    @Override
    public void deleteByReference(List<Long> references) throws BusinessException {
        for(Long reference:references){
        deleteByReference(reference, false);
        }
    }
    public void deleteByReference(Long reference, int... type) throws BusinessException {
        if (type == null) {
            deleteByReference(reference, false);
        } else {
            deleteByReference(reference, false, type);
        }
    }

    private void deleteByReference(Long reference, boolean isDeleteFromDisc, int... type) throws BusinessException {
        if (type == null) {
            deleteByReference(reference, isDeleteFromDisc);
        } else {
            List<Attachment> attachments = this.attachmentDAO.findAll(reference);
            if (attachments != null && !attachments.isEmpty()) {
                for (Attachment objects : attachments) {
                    for (int typeIndex = 0; typeIndex < type.length; typeIndex++) {
                        if (objects.getType().intValue() == type[typeIndex]) {
                            this.attachmentDAO.delete(objects.getId());
                            if (objects.getFileUrl() != null && objects.getCreatedate() != null) {
                                this.fileManager.deleteFile(objects.getFileUrl(), objects.getCreatedate(),
                                        isDeleteFromDisc);
                            }
                        }
                    }
                }
            }
        }
    }

    public void removeByReference(Long reference) throws BusinessException {
        deleteByReference(reference, true);
    }

    private void deleteByReference(Long reference, boolean isDeleteFromDisc) throws BusinessException {
        List<Object[]> attachments = this.getAllFileUrlByReference(reference);

        if (attachments != null && !attachments.isEmpty()) {
            this.attachmentDAO.deleteByReference(reference);

            for (Object[] objects : attachments) {
                Long fileId = (Long) objects[0];
                Date createDate = (Date) objects[1];

                if (fileId != null && createDate != null) {
                    this.fileManager.deleteFile(fileId, createDate, isDeleteFromDisc);
                }
            }
        }

        log.debug("删除附件: [reference = " + reference + "]");
    }

    public void deleteByReference(Long reference, Long subReference) throws BusinessException {
        deleteByReference(reference, subReference, false);
    }
    
    public void deleteOnlyAttByReference(Long reference){
    	 this.attachmentDAO.deleteByReference(reference);
    }
    
    public void removeByReference(Long reference, Long subReference) throws BusinessException {
        deleteByReference(reference, subReference, true);
    }

    private void deleteByReference(Long reference, Long subReference, boolean isDeleteFromDisc)
            throws BusinessException {
        List<Object[]> file = this.attachmentDAO.findAllFileUrl(reference, subReference);

        if (file != null && !file.isEmpty()) {
            this.attachmentDAO.deleteByReference(reference, subReference);

            for (Object[] objects : file) {
                Long fileId = (Long) objects[0];
                Date createDate = (Date) objects[1];

                if (fileId != null && createDate != null) {
                    this.fileManager.deleteFile(fileId, createDate, isDeleteFromDisc);
                }
            }
        }

        log.debug("删除附件: [reference = " + reference + " subReference = " + subReference + "]");
    }

    public void deleteById(long attachmentId) {
        this.attachmentDAO.delete(attachmentId);
    }

    /*    public String create(Collection<V3XFile> v3xFiles, ApplicationCategoryEnum cotegory, Long reference,
                Long subReference) {
            Set<Integer> r = new HashSet<Integer>();

            int i = 0;
            for (V3XFile file : v3xFiles) {
                file.setCategory(cotegory.getKey());

                Attachment attachment = new Attachment();
                attachment.setIdIfNew();

                attachment.setSort(i++);

                attachment.setReference(reference);
                attachment.setSubReference(subReference);
                attachment.setCategory(cotegory.getKey());
                attachment.setType(file.getType() == null ? Constants.ATTACHMENT_TYPE.FILE.ordinal() : file.getType());

                attachment.setFilename(file.getFilename());
                attachment.setMimeType(file.getMimeType());
                attachment.setFileUrl(file.getId());
                attachment.setCreatedate(file.getCreateDate());
                attachment.setSize(file.getSize());

                this.fileManager.save(file);
                this.attachmentDAO.save(attachment);

                r.add(attachment.getType());
            }

            return parseR(r);
        }
    */
    public String create(Long[] v3xFileId, ApplicationCategoryEnum cotegory, Long reference, Long subReference) {
        Set<Integer> r = new HashSet<Integer>();
        for (int i = 0; i < v3xFileId.length; i++) {
            V3XFile file = null;
            try {
                file = fileManager.getV3XFile(v3xFileId[i]);

                if (file != null) {
                    file.setCategory(cotegory.getKey());

                    Attachment attachment = new Attachment();
                    attachment.setIdIfNew();

                    attachment.setSort(i);

                    attachment.setReference(reference);
                    attachment.setSubReference(subReference);
                    attachment.setCategory(cotegory.getKey());
                    attachment.setType(file.getType() == null ? Constants.ATTACHMENT_TYPE.FILE.ordinal() : file
                            .getType());

                    attachment.setFilename(file.getFilename());
                    attachment.setMimeType(file.getMimeType());
                    attachment.setFileUrl(file.getId());
                    attachment.setCreatedate(file.getCreateDate());
                    attachment.setSize(file.getSize());
                    this.attachmentDAO.save(attachment);

                    r.add(attachment.getType());
                }
            } catch (BusinessException e) {
                log.error(e);
                continue;
            }
        }
        return parseR(r);
    }

    public List<Object[]> getAllFileUrlByReference(Long reference) {
        return this.attachmentDAO.findAllFileUrl(reference);
    }

    public boolean hasAttachments(Long reference, Long subReference) {
        return this.attachmentDAO.hasAttachments(reference, subReference);
    }

    public String create(ApplicationCategoryEnum category, Long reference, Long subReference) throws Exception {
        return create(category, reference, subReference,
                (HttpServletRequest) AppContext.getThreadContext(GlobalNames.THREAD_CONTEXT_REQUEST_KEY));
    }

    /**
     * 为周期性事件增加附件，已废弃
     * @param category
     * @param reference
     * @param subReference
     * @return
     * @throws Exception
     
    public String create(ApplicationCategoryEnum category, Long reference, List<Long> subReference) throws Exception {
        if (subReference==null || subReference.size()<1) {
            return create(category, reference, null,
                    (HttpServletRequest) AppContext.getThreadContext(GlobalNames.THREAD_CONTEXT_REQUEST_KEY));
        }else{
            List<Attachment> attachments = this.getAttachmentsFromRequest(category, reference,  subReference.get(0),
                    (HttpServletRequest) AppContext.getThreadContext(GlobalNames.THREAD_CONTEXT_REQUEST_KEY));
            List<Attachment> attsSubRef =new ArrayList<Attachment>();
            Set<Integer> r = new HashSet<Integer>();
            for(int i=0; i<subReference.size(); i++){
                Long subref=subReference.get(i);
                for(Attachment att: attachments){
                    Attachment newAtt = (Attachment) att.clone();
                    newAtt.setId( UUIDLong.longUUID());
                    newAtt.setSubReference(subref);
                    attsSubRef.add(newAtt);
                    r.add(newAtt.getType());
                }
            }
//            String refStr= this.create(attachments);
            this.attachmentDAO.savePatchAll(attsSubRef);
            return parseR(r);
        }
    }
    */
    
    public String create(ApplicationCategoryEnum category, Long reference, Long subReference, List<Map> attachList)
            throws Exception {
        return create(getAttachmentsFromAttachList(category, reference, subReference, attachList));
    }

    public String create(ApplicationCategoryEnum category, Long reference, Long subReference, HttpServletRequest request)
            throws Exception {
        /*       用更简洁的方式更新附件信息 
                AttachmentEditHelper editHelper = new AttachmentEditHelper(request);
                if (editHelper.hasEditAtt()) {
                    this.deleteByReference(editHelper.getReference(), editHelper.getSubReference());
                    //记录操作日志
                }*/
        if(request.getAttribute("HASSAVEDATTACHMENT")!=null){
            return "";
        }else{
            request.setAttribute("HASSAVEDATTACHMENT",new Date());
        }
        if (Strings.isNotBlank(request.getParameter("isEditAttachment"))) {
            deleteByReference(reference, subReference);
        }
        List<Attachment> attachments = this.getAttachmentsFromRequest(category, reference, subReference, request);

        return this.create(attachments);
    }

    public List<Attachment> getAttachmentsFromRequest(ApplicationCategoryEnum category, Long reference,
            Long subReference, HttpServletRequest request) throws Exception {
        List groups1 = ParamUtil.getJsonDomainGroup("attachmentInputs");
        int lsize = groups1.size();
        Map dMap = ParamUtil.getJsonDomain("attachmentInputs");
        if (lsize == 0 && dMap.size() > 0) {
            groups1.add(dMap);
        }
        List result = getAttachmentsFromAttachList(category, reference, subReference, groups1);
        if (result.isEmpty()) {
            String[] fileUrl = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_fileUrl);
            String[] mimeType = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_mimeType);
            String[] size = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_size);
            String[] createdate = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_createDate);
            String[] filename = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_filename);
            String[] type = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_type);
            String[] needClone = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_needClone);
            String[] description = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_description);
            String[] extReference = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_extReference);
            String[] extSubReference = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_extSubReference);
            String[]  subReferencea =request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_subReference);
            return this.getAttachmentsFromRequest(category, reference, subReference, fileUrl, mimeType, size,
                    createdate, filename, type, needClone, description, extReference, extSubReference, subReferencea);
        } else{
            return result;
        }
    }

    public List<Attachment> getAttachmentsFromAttachList(ApplicationCategoryEnum category, Long reference,
            Long subReference, List mapList) throws Exception {
        List groups1 = mapList;
        int lsize = groups1.size();
        int i = 0;
        String[] fileUrl = new String[lsize];
        String[] extSubReference = new String[lsize];
        String[] mimeType = new String[lsize];
        String[] size = new String[lsize];
        String[] createdate = new String[lsize];
        String[] filename = new String[lsize];
        String[] type = new String[lsize];
        String[] needClone = new String[lsize];
        String[] description = new String[lsize];
        String[] extReference = new String[lsize];
        String[] subReferencea = new String[lsize];

        for (Object o : groups1) {
            if (o instanceof Map) {
                Map map = (Map) o;
                String fileUrlStr=null;
                Object fileUrlObject=map.get(Constants.FILEUPLOAD_INPUT_NAME_fileUrl);
                try{
                    fileUrlStr=String.valueOf(fileUrlObject);
                }
                catch(Exception e){
                    log.error(e.getMessage(),e);
                    fileUrlStr=String.valueOf(fileUrlObject);
                }
                fileUrl[i]=fileUrlStr;
                //fileUrl[i] = (String) map.get(Constants.FILEUPLOAD_INPUT_NAME_fileUrl);
                mimeType[i] = (String) map.get(Constants.FILEUPLOAD_INPUT_NAME_mimeType);
                Object sizeObject=map.get(Constants.FILEUPLOAD_INPUT_NAME_size);
                String sizeStr=null;
                try{
                    sizeStr=String.valueOf(sizeObject);
                }
                catch (Exception e){
                    log.error(e.getMessage(),e);
                    sizeStr=String.valueOf(sizeObject);
                }
                size[i]=sizeStr;
              //  size[i] = (String) map.get(Constants.FILEUPLOAD_INPUT_NAME_size);
                createdate[i] = (String) map.get(Constants.FILEUPLOAD_INPUT_NAME_createDate);
                filename[i] = (String) map.get(Constants.FILEUPLOAD_INPUT_NAME_filename);
                String typeStr=null;
                Object typeObject=map.get(Constants.FILEUPLOAD_INPUT_NAME_type);
                try{
                    typeStr=String.valueOf(typeObject);
                }
                catch(Exception e){
                    log.error(e.getMessage(),e);
                    typeStr=String.valueOf(typeObject);
                }
                type[i]=typeStr;
                //type[i] = (String) map.get(Constants.FILEUPLOAD_INPUT_NAME_type);
                needClone[i] = (String) map.get(Constants.FILEUPLOAD_INPUT_NAME_needClone);
                description[i] = (String) map.get(Constants.FILEUPLOAD_INPUT_NAME_description);
                //extReference[i] = (String) map.get(Constants.FILEUPLOAD_INPUT_NAME_extReference);
                extReference[i] = (String) map.get(Constants.FILEUPLOAD_INPUT_NAME_extReference);
                subReferencea[i] = (String) map.get(Constants.FILEUPLOAD_INPUT_NAME_subReference);
                i++;
            }
        }

        return this.getAttachmentsFromRequest(category, reference, subReference, fileUrl, mimeType, size, createdate,
                filename, type, needClone, description, extReference, extSubReference,subReferencea);
    }

    private List<Attachment> getAttachmentsFromRequest(ApplicationCategoryEnum category, Long reference,
            Long subReference, String[] fileUrl, String[] mimeType, String[] size, String[] createdate,
            String[] filename, String[] type, String[] needClone, String[] description, String[] extReference,
            String[] extSubReference,  String[] subReferencea) throws Exception {
        List<Attachment> attachments = new ArrayList<Attachment>();
        if (fileUrl == null || mimeType == null || size == null || createdate == null || filename == null
                || type == null || needClone == null) {
            return attachments;
        }
        
        for (int i = 0; i < fileUrl.length; i++) {
            Date originalCreateDate = Datetimes.parseDatetime(createdate[i]);
            Integer _type = Integer.valueOf(type[i]);

            Attachment attachment = new Attachment();
            attachment.setIdIfNew();

            attachment.setSort(i);
            attachment.setCategory(category.getKey());

            if (extReference != null && extReference.length >= i && Strings.isNotBlank(extReference[i])) {
                attachment.setReference(Long.valueOf(extReference[i]));
            } else if (reference != null) { //避免空值插入产生异常
                attachment.setReference(reference);
            }

            if (extSubReference != null && extSubReference.length >= i && Strings.isNotBlank(extSubReference[i])) {
                attachment.setSubReference(Long.valueOf(extSubReference[i]));
            } else if(subReference!=null){
                attachment.setSubReference(subReference);
            }else if(Strings.isDigits(subReferencea[i])){
                attachment.setSubReference(Long.valueOf(subReferencea[i]));
            }

            attachment.setMimeType(mimeType[i]);
            attachment.setSize(Long.valueOf(size[i]));
            attachment.setFilename(Strings.nobreakSpaceToSpace(filename[i]));
            attachment.setType(_type);
            attachment.setDescription(description[i]);
            if (Strings.isNotBlank(description[i])
                    && !"null".equals(description[i])
                    && (_type.equals(Constants.ATTACHMENT_TYPE.DOCUMENT.ordinal()) || _type
                            .equals(Constants.ATTACHMENT_TYPE.FormDOCUMENT.ordinal()))) {
                //将description字段值拷贝到GenesisId，用于提高查询效率(前端改动量大，暂不修改)
                Long genesisId = Long.parseLong(description[i]);
                attachment.setGenesisId(genesisId);
            }

            boolean _needClone = Boolean.parseBoolean(needClone[i]);
            boolean _isFile = Constants.ATTACHMENT_TYPE.DOCUMENT.ordinal() != _type.intValue()
                    && Constants.ATTACHMENT_TYPE.FormDOCUMENT.ordinal() != _type.intValue();

            if (_isFile && _needClone) {
                Long newFileId = UUIDLong.longUUID();
                Date newCreateDate = new Date();
                try {
                    this.fileManager.clone(Long.valueOf(fileUrl[i]), originalCreateDate, newFileId, newCreateDate);
                } catch (FileNotFoundException e) {
                    log.warn(e.getMessage());
                } catch (Exception e) {
                    log.error("Clone 附件", e);
                }

                attachment.setFileUrl(newFileId);
                attachment.setCreatedate(newCreateDate);
            } else {
                attachment.setCreatedate(originalCreateDate);

                if (NumberUtils.isNumber(fileUrl[i])) {
//                    if (_isFile && NumberUtils.isNumber(fileUrl[i])) { 对关联文档改造后，表单部分的关联文档也使用fileurl记录相关属性
                    attachment.setFileUrl(Long.valueOf(fileUrl[i]));
                }
            }

            attachments.add(attachment);
        }

        return attachments;
    }

    /**
     * 
     * @param attachstr  如#。。。。。。。。。。
     * @return
     */
    public List<Attachment> getAttachmentsFromRequestNotRelition(String attachstr) {
        List<Attachment> attachments = new ArrayList<Attachment>();
        String[] attstrs = attachstr.split("#");
        for (int i = 1; i < attachstr.length(); i++) {
            String[] attstr = attstrs[i].split(";");
            Attachment attachment = new Attachment();
            if (attstr[0].split("=")[1] != null) {
                attachment.setId(Long.parseLong(attstr[0].split("=")[1]));
            }
            if (attstr[1].split("=")[1] != null) {
                attachment.setReference(Long.parseLong(attstr[1].split("=")[1]));
            }
            if (attstr[2].split("=")[1] != null) {
                attachment.setSubReference(Long.parseLong(attstr[2].split("=")[1]));
            }
            if (attstr[3].split("=")[1] != null) {
                attachment.setCategory(Integer.parseInt(attstr[3].split("=")[1]));
            }
            Integer type = null;
            if (attstr[4].split("=")[1] != null) {
                type = Integer.parseInt(attstr[4].split("=")[1]);
                attachment.setType(type);
            }
            if (attstr[5].split("=")[1] != null) {
                attachment.setFilename(attstr[5].split("=")[1]);
            }
            if (attstr[6].split("=")[1] != null) {
                attachment.setMimeType(attstr[6].split("=")[1]);
            }
            /*			if(attstr[7].split("=")[1]!=null)
            			{
            				attachment.setCreatedate(createdate)
            			}*/
            if (attstr[8].split("=")[1] != null) {
                attachment.setSize(Long.parseLong(attstr[8].split("=")[1]));
            }
            if (attstr[9].split("=")[1] != null) {
                attachment.setFileUrl(Long.parseLong(attstr[9].split("=")[1]));
            }
            if (attstr[10].split("=")[1] != null) {
                String genesisIdStr = attstr[10].split("=")[1];
                attachment.setDescription(genesisIdStr);
                boolean typeIsDocument = (type==(Constants.ATTACHMENT_TYPE.DOCUMENT.ordinal())) || (type==(Constants.ATTACHMENT_TYPE.FormDOCUMENT.ordinal()));
				if (Strings.isNotBlank(genesisIdStr)
                        && !"null".equals(genesisIdStr)
                        && typeIsDocument) {
                    //将description字段值拷贝到GenesisId，用于提高查询效率(前端改动量大，暂不修改)
                    Long genesisId = Long.parseLong(genesisIdStr);
                    attachment.setGenesisId(genesisId);
                }
            }

            attachments.add(attachment);
        }
        return attachments;
    }

    /**
     * 本方法只为显示附件，并没有保存。只是把从页面获得的参数传递转化为Attachment对象的列表。
     * @param request
     * @return
     */
    public List<Attachment> getAttachmentsFromRequestNotRelition(HttpServletRequest request) {
        String[] fileUrl = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_fileUrl);
        String[] mimeType = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_mimeType);
        String[] size = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_size);
        String[] createdate = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_createDate);
        String[] filename = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_filename);
        String[] type = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_type);
        String[] needClone = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_needClone);
        String[] description = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_description);
        String[] genesisIds = request.getParameterValues(Constants.FILEUPLOAD_INPUT_NAME_genesisId);
        List<Attachment> attachments = new ArrayList<Attachment>();
        if (fileUrl == null || mimeType == null || size == null || createdate == null || filename == null
                || type == null || needClone == null) {
            return attachments;
        }

        for (int i = 0; i < fileUrl.length; i++) {
            Date originalCreateDate = Datetimes.parseDatetime(createdate[i]);
            Integer _type = Integer.valueOf(type[i]);

            Attachment attachment = new Attachment();
            attachment.setMimeType(mimeType[i]);
            attachment.setSize(Long.valueOf(size[i]));
            attachment.setFilename(filename[i]);
            attachment.setType(_type);

            attachment.setDescription(description[i]);
            if (Strings.isNotBlank(description[i])
                    && !"null".equals(description[i])
                    && (_type.equals(Constants.ATTACHMENT_TYPE.DOCUMENT.ordinal()) || _type
                            .equals(Constants.ATTACHMENT_TYPE.FormDOCUMENT.ordinal()))) {
                //将description字段值拷贝到GenesisId，用于提高查询效率(前端改动量大，暂不修改)
                Long genesisId = Long.parseLong(description[i]);
                attachment.setGenesisId(genesisId);
            }

            boolean _needClone = Boolean.parseBoolean(needClone[i]);
            boolean _isFile = Constants.ATTACHMENT_TYPE.DOCUMENT.ordinal() != _type.intValue()
                    && Constants.ATTACHMENT_TYPE.FormDOCUMENT.ordinal() != _type.intValue();

            if (_isFile && _needClone) {
                Long newFileId = UUIDLong.longUUID();
                Date newCreateDate = new Date();
                try {
                    this.fileManager.clone(Long.valueOf(fileUrl[i]), originalCreateDate, newFileId, newCreateDate);
                } catch (FileNotFoundException e) {
                    log.warn(e.getMessage());
                } catch (Exception e) {
                    log.error("Clone 附件", e);
                }

                attachment.setFileUrl(newFileId);
                attachment.setCreatedate(newCreateDate);
            } else {
                attachment.setCreatedate(originalCreateDate);

                if (_isFile && NumberUtils.isNumber(fileUrl[i])) {
                    attachment.setFileUrl(Long.valueOf(fileUrl[i]));
                }
            }

            attachments.add(attachment);
        }

        return attachments;
    }

    public String update(ApplicationCategoryEnum category, Long reference, Long subReference, HttpServletRequest request)
            throws Exception {
        this.deleteByReference(reference, subReference);
        return this.create(category, reference, subReference, request);
    }

    private static String parseR(Set<Integer> r) {
        StringBuilder str = new StringBuilder();
        for (Integer integer : r) {
            str.append(integer);
        }
        return str.toString();
    }

    public Attachment getFirstImageAttachment(Long reference, Long subReference) {
        return this.attachmentDAO.getFirst(reference, subReference, Constants.ATTACHMENT_TYPE.IMAGE);
    }

    public Attachment getAttachmentByFileURL(Long fileURL) {
        return attachmentDAO.getAttachmentByFileURL(fileURL);
    }

    @Override
    public Attachment getAttachmentById(Long id) {
        return attachmentDAO.get(id);
    }

    @Override
    public List<Attachment> getAttachmentByFileURLs(List<Long> fileURLs) {
        return attachmentDAO.find(fileURLs);
    }

    @Override
    public List<Attachment> getAttachmentByFileURLStrings(String fileURLs) {
        List<Long> arrs = new ArrayList<Long>();
        for (String s : fileURLs.split(",")) {
            arrs.add(Long.parseLong(s));
        }
        return attachmentDAO.find(arrs);
    }

    public void update(Attachment attachment) {
        attachmentDAO.update(attachment);
    }
    
    @Transactional(propagation= Propagation.REQUIRED,rollbackFor=com.seeyon.ctp.common.exceptions.BusinessException.class)
    public String copy(Long reference, Long subReference, Long newReference, Long newSubReference, Integer category,
    		Long userId, Long accountId) {
    	return this.copy(reference, subReference, newReference, newSubReference, category, userId, accountId, null);
    }
    
    @Transactional(propagation= Propagation.REQUIRED,rollbackFor=com.seeyon.ctp.common.exceptions.BusinessException.class)
    public String copy(Long reference, Long subReference, Long newReference, Long newSubReference, Integer category,
            Long userId, Long accountId, String filename) {
        List<Attachment> atts = this.getByReference(reference, subReference);
        java.util.Date now = new java.util.Date();
        Set<Integer> r = new HashSet<Integer>();
        for (Attachment att : atts) {
            Attachment newAtt = null;
            Long newFileId = UUIDLong.longUUID();
            try {
                newAtt = (Attachment) att.clone();
            } catch (Exception e1) {
                log.warn("复制见附件对象异�常 [reference = " + reference + "]", e1);
                continue;
            }
//            newAtt.setIdIfNew();
            newAtt.setId(UUIDLong.longUUID());

            try {
                if (att.getType() != Constants.ATTACHMENT_TYPE.DOCUMENT.ordinal()
                        && att.getType() != Constants.ATTACHMENT_TYPE.FormDOCUMENT.ordinal()) {
                    this.fileManager.clone(att.getFileUrl(), att.getCreatedate(), newFileId, now);
                } else {
                    newFileId = att.getFileUrl();
                }
            } catch (FileNotFoundException e) {
                log.warn(e.getMessage());
            } catch (Exception e) {
                log.warn("复制见附件文件异�常 [reference = " + reference + "]", e);
            }

            newAtt.setFileUrl(newFileId);
            newAtt.setReference(newReference);
            newAtt.setSubReference(newSubReference);
            newAtt.setCreatedate(now);
            newAtt.setCategory(category);
            
            if(filename != null){
            	newAtt.setFilename(filename);
            }

            this.saveAttachment(newAtt, userId, accountId);
            r.add(newAtt.getType());
        }

        return parseR(r);
    }

    public String copy(Long reference, Long subReference, Long newReference, Long newSubReference, Integer category) {
        User user = AppContext.getCurrentUser();
        if (user == null) {
            return null;
        }
        return this.copy(reference, subReference, newReference, newSubReference, category, user.getId(),
                user.getAccountId());
    }

    public List<Attachment> copy(Long reference, Long subReference) {
        List<Attachment> result = new ArrayList<Attachment>();
        List<Attachment> atts = this.getByReference(reference, subReference);

        java.util.Date now = new java.util.Date();

        for (Attachment att : atts) {
            Attachment newAtt = null;
            Long newFileId = UUIDLong.longUUID();
            try {
                newAtt = (Attachment) att.clone();
            } catch (Exception e1) {
                log.warn("复制见附件对象异�常 [reference = " + reference + "]", e1);
                continue;
            }

            newAtt.setIdIfNew();

            try {
                if (att.getType() != Constants.ATTACHMENT_TYPE.DOCUMENT.ordinal()
                        && att.getType() != Constants.ATTACHMENT_TYPE.FormDOCUMENT.ordinal()) {
                    this.fileManager.clone(att.getFileUrl(), att.getCreatedate(), newFileId, now);
                } else {
                    newFileId = att.getFileUrl();
                }
            } catch (FileNotFoundException e) {
                log.warn(e.getMessage());
            } catch (Exception e) {
                log.warn("复制见附件文件异�常 [reference = " + reference + "]", e);
            }

            newAtt.setFileUrl(newFileId);
            newAtt.setReference(null);
            newAtt.setSubReference(null);
            newAtt.setCreatedate(now);

            result.add(newAtt);
        }

        return result;
    }

    /*
        public void save(List<Attachment> attas) {
            for (Attachment a : attas) {
                this.attachmentDAO.save(a);
            }
        }*/

    public long getAttSizeSum(long attId) {
        return this.attachmentDAO.get(attId).getSize();
    }

    public boolean checkIsLicitGenesis(Long referenceId, Long genesisId) {
        return attachmentDAO.checkIsLicitGenesis(referenceId, genesisId);
    }

    public void updateReference(Long fileUrl, Long referenceId) {
        attachmentDAO.updateReference(fileUrl, referenceId);
    }

    @Override
    public void updateReferenceByFileUrls(String fileUrls, Long referenceId) {
        for (String fileUrl : fileUrls.split(",")) {
            attachmentDAO.updateReference(Long.parseLong(fileUrl), referenceId);
        }
    }

    public void updateReferenceSubReference(Long fileUrl, Long referenceId, Long subReference) {
        attachmentDAO.updateReferenceSubReference(fileUrl, referenceId, subReference);
    }
    
    @Override
	public void updateFileNameByAffairIds(String fileName, List<Long> affairIdList) {
		attachmentDAO.updateFileNameByAffairIds(fileName,affairIdList);
	}
    
    @Override
	public List<Long> getBySubReference(Long subReference) {
		return attachmentDAO.getBySubReference(subReference);
	}

	@Override
	public String create(Collection<Attachment> attachments, Long memberId, Long orgAccountId) {
		 Set<Integer> r = new HashSet<Integer>();
	        User user = new User();
	        user.setId(memberId);
	        user.setAccountId(orgAccountId);

	        int i = 0;
	        for (Attachment attachment : attachments) {
	            attachment.setSort(i++);
	            saveAttachment(attachment, user);
	            r.add(attachment.getType());
	        }

	        return parseR(r);
	}

	@Override
	public List<Object[]> getByReference(Long reference, Integer type, FlipInfo flipInfo) {
		// TODO Auto-generated method stub
		return this.attachmentDAO.findAll(reference, type, flipInfo);
	}

	@Override
	public void deleteByIds(List<Long> attachmentIds) {
		 attachmentDAO.deleteByIds(attachmentIds);
		
	}

	@Override
	public List<Attachment> getBySubReference(List<Long> subReferences) {
		return attachmentDAO.getBySubReference(subReferences);
	}
	@Override
	public void saveAsAtt(List<Attachment> attachments) {
		int i = 0;
		Iterator arg4 = attachments.iterator();

		while (arg4.hasNext()) {
			Attachment attachment = (Attachment) arg4.next();
			attachment.setIdIfNew();
			attachment.setSort(i++);
			this.attachmentDAO.save(attachment);
		}
	}

	@Override
	public String getFileNameExcludeSuffix(List<Attachment> attachments) {
		StringBuilder nameString = new StringBuilder();
		if(attachments != null && attachments.size()>0){
			for (int i=0;i<attachments.size();i++) {
				Attachment tempAtt = attachments.get(i);
				String name = tempAtt.getFilename();
				String extension = tempAtt.getExtension();
				if(!Strings.isBlank(extension)){
					int index = name.lastIndexOf("."+extension);
					if(index != -1){
						nameString.append(name.substring(0, index));
					}else{
						nameString.append(name);
					}
				}else{
					nameString.append(name);
				}
				if(i+1 != attachments.size()){
					nameString.append("|");
				}
			}	
		}
		return nameString.toString();
	}

	@Override
	public String getFilesmName(List<Attachment> attachments) {
		StringBuilder nameString = new StringBuilder();
		if(attachments != null && attachments.size()>0){
			int ccount = 0;
			for (int i=0;i<attachments.size();i++) {
				++ccount;
				Attachment tempAtt =  attachments.get(i);
				String currAttName = tempAtt.getFilename();
				int index = currAttName.lastIndexOf(".");
				//如果没有后缀名
				if(index < 0){
					nameString.append(ccount + "." + currAttName.substring(0));
				}else{
					nameString.append(ccount + "." + currAttName.substring(0,index));
				}
				if(i+1 != attachments.size()){
					nameString.append("<br>");
				}
			}
		}
		return nameString.toString();
	}

	@Override
	public List<Attachment> getByReference(List<Long> references) {
		return this.attachmentDAO.getByReference(references);
	}

	@Override
	public Attachment getAttachment(Long id) {
		return attachmentDAO.get(id);
	}
}