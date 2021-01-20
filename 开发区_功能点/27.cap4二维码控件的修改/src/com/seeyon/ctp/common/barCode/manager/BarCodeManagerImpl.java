package com.seeyon.ctp.common.barCode.manager;

import com.seeyon.ctp.common.barCode.dao.BarCodeDao;
import com.seeyon.ctp.common.barCode.domain.BarCodeInfo;
import com.seeyon.ctp.common.barCode.uitl.BarCodeEncoder;
import com.seeyon.ctp.common.barCode.uitl.BarCodeUtil;
import com.seeyon.ctp.common.barCode.vo.BarCodeParamVo;
import com.seeyon.ctp.common.barCode.vo.ResultVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.logging.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarCodeManagerImpl implements BarCodeManager {

    private static Log log = CtpLogFactory.getLog(BarCodeManagerImpl.class);

    private static final String CONTENT_PARAM_CONTENT = "content";
    private static final String CONTENT_PARAM_CODE_TYPE = "codeType";
    private String tmpdir = System.getProperty("java.io.tmpdir");

    private BarCodeDao barCodeDao;
    private FileManager fileManager;
    private AttachmentManager attachmentManager;

    /**
     * 保存或更新二维码信息
     *
     * @param objectId   对象id，比如：公文id
     * @param fileName   服务器保存的物理文件名
     * @param categoryId 应用分类
     */
    @Override
    public void saveBarCode(Long objectId, Long fileName, String fileExt, Integer categoryId) {
        BarCodeInfo info = this.barCodeDao.getByObjectId(objectId);
        Date today = new Date();
        if (info != null) {
            info.setFileName(fileName);
            info.setFileExt(fileExt);
            info.setUpdateDate(today);
            this.barCodeDao.update(info);
        } else {
            info = new BarCodeInfo();
            info.setNewId();
            info.setCategoryId(categoryId);
            info.setCreateDate(today);
            info.setUpdateDate(today);
            info.setFileName(fileName);
            info.setObjectId(objectId);
            info.setFileExt(fileExt);
            this.barCodeDao.save(info);
        }
    }

    @Override
    public ResultVO getBarCodeFile(BarCodeParamVo paramVo, Map<String, Object> customParam) throws BusinessException {
        BarCodeTypeManager typeManager = BarCodeUtil.getTypeManager(paramVo.getCodeType());
        String contents = typeManager.getContentStr(customParam);
        if (Strings.isBlank(contents)) {
            return new ResultVO(false, ResourceUtil.getString("common.barcode.value.empty"));
        } else if (BarCodeUtil.checkContentLength(contents, paramVo.getMaxLength())) {
            //需要抛出异常的抛出异常
            if (paramVo.isThrowException()) {
                return new ResultVO(false, ResourceUtil.getString("common.barcode.length.more"));
            } else {
                //不需要抛出异常继续生成二维码的，就生成一个带提示提示信息的二维码，在扫描该二维码的时候好给出超长的提示
                if (log.isDebugEnabled()) {
                    log.debug("生成的二维码超过长度，内容为：" + contents);
                }
                contents = typeManager.getContent4OutOfLength(customParam);
            }
        }
        String baseFolder = getTempDir();
        File codeFile = new File(baseFolder, UUIDLong.longUUID() + "." + paramVo.getFileExt());
        try {
            contents = BarCodeEncoder.getInstance().encode(contents, paramVo.getEncodeVersion());
            Map<String, String> contentMap = new HashMap<String, String>();
            contentMap.put(CONTENT_PARAM_CONTENT, contents);
            contentMap.put(CONTENT_PARAM_CODE_TYPE, paramVo.getCodeType());
            contents = JSONUtil.toJSONString(contentMap);
            if (log.isDebugEnabled()) {
                log.debug(contents);
            }
            BarCodeUtil.encode(contents, paramVo, codeFile);
        } catch (Exception e) {
            log.error("二维码图片生成异常：" + e.getMessage(), e);
            if ("Data too big".equals(e.getMessage()) || "Unable to fit message in columns".equals(e.getMessage())) {
                if (paramVo.isThrowException()) {
                    return new ResultVO(false, ResourceUtil.getString("common.barcode.length.more"));
                } else {
                    //不需要抛出异常继续生成二维码的，就生成一个带提示提示信息的二维码，在扫描该二维码的时候好给出超长的提示
                    if (log.isDebugEnabled()) {
                        log.debug("生成的二维码超过长度，内容为：" + contents);
                    }
                    contents = typeManager.getContent4OutOfLength(customParam);
                    try {
                        contents = BarCodeEncoder.getInstance().encode(contents, paramVo.getEncodeVersion());
                        Map<String, String> contentMap = new HashMap<String, String>();
                        contentMap.put(CONTENT_PARAM_CONTENT, contents);
                        contentMap.put(CONTENT_PARAM_CODE_TYPE, paramVo.getCodeType());
                        contents = JSONUtil.toJSONString(contentMap);
                        BarCodeUtil.encode(contents, paramVo, codeFile);
                    } catch (Exception e2) {
                        log.info("转换为生成超长标识之后，还是异常：" + e2.getMessage(), e2);
                    }
                }
            } else {
                return new ResultVO(false, ResourceUtil.getString("common.barcode.error"));
            }
        }
        V3XFile file = fileManager.save(codeFile, paramVo.getCategoryEnum(), codeFile.getName(), DateUtil.currentDate(), true);
        boolean result = codeFile.delete();
        log.info("二维码临时文件删除状态：" + result);
        return new ResultVO(true, file);
    }

    @Override
    public ResultVO getBarCodeFile(Map<String, Object> codeParam, Map<String, Object> customParam) throws BusinessException {
        BarCodeParamVo paramVo = new BarCodeParamVo(codeParam);
        return getBarCodeFile(paramVo, customParam);
    }

    @Override
    public Long getBarCode(Map<String, Object> codeParam, Map<String, Object> customParam) throws BusinessException {
        ResultVO vo = getBarCodeFile(codeParam, customParam);
        if (vo.isSuccess()) {
            V3XFile file = getBarCodeFile(codeParam, customParam).getFile();
            return file.getId();
        } else {
            return 0L;
        }
    }

    @Override
    public ResultVO getBarCodeAttachment(Map<String, Object> codeParam, Map<String, Object> customParam) throws BusinessException {
        BarCodeParamVo paramVo = new BarCodeParamVo(codeParam);
        //logo图片 zhou
//        paramVo.setLogoPath("F:\\Seeyon\\A87.1_sp1_07\\ApacheJetspeed\\temp\\dog.jpg");
        return getBarCodeAttachment(paramVo, customParam);
    }

    @Override
    public ResultVO getBarCodeAttachment(BarCodeParamVo paramVo, Map<String, Object> customParam) throws BusinessException {
        ResultVO vo = getBarCodeFile(paramVo, customParam);
        if (!vo.isSuccess()) {
            return vo;
        }
        V3XFile file = vo.getFile();
        Attachment attachment = new Attachment(file);
        attachment.setReference(paramVo.getReference());
        attachment.setSubReference(paramVo.getSubReference());
        List<Attachment> list = new ArrayList<Attachment>();
        list.add(attachment);
        attachmentManager.create(list);
        return new ResultVO(true, attachment);
    }

    @Override
    public Object decodeBarCode(String codeType, String codeStr, Map<String, Object> customParam) throws Exception {
        Object json;
        try {
//            codeStr = "{\"codeType\":\"form\",\"content\":\"/1.0/{\\\"moduleId\\\":8163468476697433739,\\\"formId\\\":-144923262596201231,\\\"dataId\\\":8163468476697433739,\\\"rightId\\\":-2936839306397070477,\\\"contentType\\\":20,\\\"viewState\\\":1,\\\"formType\\\":2}\"}";
            json = JSONUtil.parseJSONString(codeStr);
        } catch (Exception e) {
            log.error("直接json解析二维码数据异常，转换为用给定的类型解析：" + codeStr, e);
            return decode(codeType, codeStr, customParam);
        }
        if (json == null) {
            return codeStr;
        }
        if (json instanceof Map) {
            //noinspection unchecked
            Map<String, String> map = (Map) json;
            String content = map.get(CONTENT_PARAM_CONTENT);
            return decode(codeType, content, customParam);
        }
        return decode(codeType, codeStr, customParam);
    }

    private Object decode(String codeType, String codeStr, Map<String, Object> customParam) throws Exception {
        BarCodeTypeManager typeManager = BarCodeUtil.getTypeManager(codeType);
        String content = BarCodeEncoder.getInstance().decode(codeStr);
        return typeManager.decode(content, customParam);
    }

    @Override
    public void deleteBarCode(Long reference, Long subReference) throws BusinessException {
        attachmentManager.deleteByReference(reference, subReference);
    }

    private String getTempDir() {
        return tmpdir;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void setBarCodeDao(BarCodeDao barCodeDao) {
        this.barCodeDao = barCodeDao;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }
}
