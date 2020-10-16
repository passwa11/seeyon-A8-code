package com.seeyon.apps.govdoc.helper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.apps.govdoc.vo.GovdocBaseVO;
import com.seeyon.apps.govdoc.vo.GovdocBodyVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.content.ContentConfig;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.domain.ReplaceBase64Result;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;

public class GovdocContentHelper extends GovdocHelper {
	
    private static final Log LOGGER = CtpLogFactory.getLog(GovdocContentHelper.class);

    /**
     * 获取某公文的所有content数据
     * @param moduleId
     * @return
     */
    public static List<CtpContentAll> getContentListByModuleId(Long moduleId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("moduleId", moduleId);
		return ctpMainbodyManager.getContentList(params);
	}
    
    /**
     * 获取某公文的表单数据
     * @param moduleId
     * @return
     */
    public static CtpContentAll getFormContentByModuleId(Long moduleId) {
		List<CtpContentAll> list = getContentListByModuleId(moduleId);
		if (Strings.isNotEmpty(list)) {
			for (int i = 0; i < list.size(); i++) {
				CtpContentAll c = list.get(i);
				if (null != c.getContentType()) {
					if (c.getContentType().intValue() == MainbodyType.FORM.getKey()) {
						return c;
					}
				}
			}
		}
		return null;
	}
    
    /**
     * 通过contentDataId获取某公文的表单数据
     * @param moduleType
     * @param contentDataId
     * @return
     */
    public static CtpContentAll getFormContentByDataIdAndModuleType(Integer moduleType, Long contentDataId) {
        List<CtpContentAll> contentList = new ArrayList<CtpContentAll>();
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("moduleType", moduleType);
        params.put("contentDataId", contentDataId);
		contentList = ctpMainbodyManager.getContentList(params);
		if(Strings.isNotEmpty(contentList)) {
			return contentList.get(0);
		}
        return null;
    }
    
    /**
     * 获取公文所有的正文content数据
     * @param moduleId
     * @return
     */
    public static List<CtpContentAll> getBodyContentListByModuleId(Long moduleId) {
    	List<CtpContentAll> contentList = new ArrayList<CtpContentAll>();
    	List<CtpContentAll> list = getContentListByModuleId(moduleId);
		if(list!=null&&!list.isEmpty()){
			boolean hasForm = false;
			for(int i = 0; i < list.size() ; i++) {
				CtpContentAll c = list.get(i);
				if(c.getContentType()!=null && c.getContentType().intValue() == MainbodyType.FORM.getKey()) {
					hasForm = true;
				} else {
					contentList.add(c);
				}
			}
			//若没有公文单，则是流程追溯的数据，正文列表将文单的数据清除
			if(!hasForm && list.size()>1) {//针对公文撤销做的处理 唐桂林/刘涛
				contentList.remove(0);
			}
		}
		return contentList;
    }
    
    /**
     * 获取某公文的第一个正文数据(非转版)
     * @param moduleId
     * @return
     */
    public static CtpContentAll getFirstBodyContentByModuleId(Long moduleId) {
		List<CtpContentAll> list = getContentListByModuleId(moduleId);
		if(list!=null&&!list.isEmpty()){
			boolean hasForm = false;
			CtpContentAll content = null;
			for(int i = 0;i < list.size() ; i++){
				CtpContentAll c = list.get(i);
				if(c.getContentType()!=null && c.getContentType().intValue() == MainbodyType.FORM.getKey()) {
					hasForm = true;
				} else {
					if(c.getSort() !=0 && c.getSort().intValue() != 3) {//3为转版正文
						content = c;
					}
				}
			}
			//没有公文单，则返回第一个正文，非转版的
			if(!hasForm && list.size()>1) {//针对公文撤销做的处理 唐桂林/刘涛
				return list.get(1);
			} else {
				return content;
			}
		}
		return null;
	}
    
    /**
     * 获取某公文的正文数据-直到取到为止
     * @param moduleId
     * @return
     */
    public static CtpContentAll getBodyContentByModuleId(Long moduleId) {
		List<CtpContentAll> list = getContentListByModuleId(moduleId);
		if(list!=null&&!list.isEmpty()){
			boolean hasForm = false;
			CtpContentAll content = null;
			for(int i = 0;i < list.size() ; i++){
				CtpContentAll c = list.get(i);
				if(c.getContentType()!=null && c.getContentType().intValue() == MainbodyType.FORM.getKey()) {
					hasForm = true;
				} else {
					if(c.getSort() != 3) {
						content = c;
					}
				}
			}
			//没有公文单，则返回第一个正文，非转版的
			if(!hasForm && list.size()>1) {//针对公文撤销做的处理 唐桂林/刘涛
				return list.get(1);
			} else {
				return content;
			}
		}
		return null;
	}
    
    /**
     * 获取某公文转换正文数据pdf/ofd，若获取不到，则查询普通正文
     * @param moduleId
     * @return
     */
    public static CtpContentAll getTransBodyContentByModuleId(Long moduleId) {
    	return getTransBodyContentByModuleId(moduleId, false, null);
    } 
    
    public static CtpContentAll getOnlyTransBodyContentByModuleId(Long moduleId, Integer moduleType) {
    	return getTransBodyContentByModuleId(moduleId, true, moduleType);
    }
    
    /**
     * 获取某公文转换正文数据pdf/ofd，只查询转换正文数据pdf/ofd
     * @param moduleId
     * @return
     */
    public static CtpContentAll getOnlyTransBodyContentByModuleId(Long moduleId) {
    	return getTransBodyContentByModuleId(moduleId, true, null);
    }
    
    /**
     * 获取某公文转换正文数据-pdf/ofd
     * @param moduleId
     * @param onlyTrans true:只查询转换正文数据pdf/ofd false:若获取不到，则查询普通正文
     * @return
     */
    public static CtpContentAll getTransBodyContentByModuleId(Long moduleId, boolean onlyTrans, Integer contentType) {
    	List<CtpContentAll> contents = getBodyContentListByModuleId(moduleId);
    	CtpContentAll bodyContent = null;
    	CtpContentAll transPdfBody = null;
    	CtpContentAll transOfdBody = null;
    	for(CtpContentAll content : contents) {
    		if(content.getSort().intValue() == 3) {//转版的pdf或ofd的sort为3
    			if(content.getContentType().intValue() == MainbodyType.Pdf.getKey()) {
    				transPdfBody = content;
    			} else if(content.getContentType().intValue() == MainbodyType.Ofd.getKey()) {
    				transOfdBody = content;
    			}
    		}
    	}
    	if(contentType != null) {//传什么返回什么
			if(transPdfBody != null && transPdfBody.getContentType().intValue()==contentType.intValue()) {
	    		bodyContent = transPdfBody;
	    	} else if(transOfdBody != null && transOfdBody.getContentType().intValue()==contentType.intValue()) {
	    		bodyContent = transOfdBody;
	    	}	
		} else {
			if(transOfdBody != null) {//公文交换时，Ofd转版优先交换，Pdf其次，最后是原正文
				bodyContent = transOfdBody;
			} else if(transPdfBody != null) {
				bodyContent = transPdfBody;
			}
			if(!onlyTrans) {
	    		if(bodyContent == null) {
	    			bodyContent = getFirstBodyContentByModuleId(moduleId);
	    		}
	    	}
		}
    	return bodyContent;
    }
    /**
     * 获取指定类型的正文
     * @param moduleId
     * @param onlyTrans
     * @param type
     * @return
     */
    public static CtpContentAll getBodyContentByModuleIdAndType(Long moduleId, MainbodyType bodyType) {
    	List<CtpContentAll> contents = getContentListByModuleId(moduleId);
    	CtpContentAll bodyContent = null;
    	Iterator<CtpContentAll> content = contents.iterator();
    	while (content.hasNext()) {
    		bodyContent = content.next();
    		if (bodyContent.getContentType() == bodyType.getKey()) {
				return bodyContent;
    		}
    	}
    	return null;
    }
    
	public static void fillNewBodyData(GovdocNewVO newVo) throws BusinessException {
		// 快速发文显示套红模板
		if(newVo.getIsQuickSend()) {
			newVo.getBodyVo().setTaohongList(govdocDocTemplateManager.getEdocDocTemplateList("false", newVo.getCurrentUser().getLoginAccount(), newVo.getCurrentUser(), "edoc", "officeword"));
		}
		//Office能上传最大值
		String officeOcxUploadMaxSize = SystemProperties.getInstance().getProperty("fileUpload.maxSize");
		newVo.getBodyVo().setOfficeOcxUploadMaxSize(officeOcxUploadMaxSize);
		
		if(newVo.getTemplate() != null) {//系统模板若存设置了公文正文类型
			CtpContentAll bodyContent = GovdocContentHelper.getBodyContentByModuleId(newVo.getTemplate().getId());
			if(bodyContent != null) {
				newVo.getBodyVo().setTemplateContentType(bodyContent.getContentType());
				if("-1".equals(bodyContent.getContent())) {
					newVo.getBodyVo().setContentT("-1");	
				}
			}
		}
	}

	/**
	 * 公文查看时显示正文
	 * @param summaryVO
	 * @throws BusinessException
	 */
	public static void fillSummaryBodyData(GovdocSummaryVO summaryVO) throws BusinessException {
		if (summaryVO.getSummaryId() == null) {
			return;
		}
		try {
			if(summaryVO.getSummary() == null) {
				summaryVO.setSummary(govdocSummaryManager.getSummaryById(summaryVO.getSummaryId()));
			}
			Long summaryId = summaryVO.getSummary().getId();
			GovdocBodyVO bodyVo = summaryVO.getBodyVo();
			
			CtpContentAll bodyContent = GovdocContentHelper.getBodyContentByModuleId(Long.valueOf(summaryId)); // newModule
			if (bodyContent != null) {
                try {
                    // 此处是为了升级历史数据
                    ReplaceBase64Result result = fileManager.replaceBase64Image(bodyContent.getContent());
                    if( result.isConvertBase64Img() ){// 替换过正文内容才执行更新
                        bodyContent.setContent(result.getHtml());
                        DBAgent.update(bodyContent);
                    }
                } catch (Exception e) {// 查看时，如果转换失败就不转换了
                	LOGGER.error("将正文中base64编码图片转为URL时发生异常！",e);
                }

				String govdocBodyType = GovdocUtil.getBodyType(bodyContent.getContentType());
				String govdocBodyTypeText = GovdocUtil.getBodyTypeText(bodyContent.getContentType());
				if (Strings.isNotBlank(bodyContent.getContent()) && !govdocBodyType.equals(MainbodyType.HTML.name()) ) {
					String fileName = "";
					V3XFile v3xFile = fileManager.getV3XFile(Long.parseLong(bodyContent.getContent()));
					if (v3xFile != null) {
						fileName = v3xFile.getFilename();
					}
					bodyVo.setFileName(fileName);
				}
				bodyVo.setBodyType(govdocBodyType);// 正文类型文字
				bodyVo.setBodyTypeText(govdocBodyTypeText);// 正文label
				bodyVo.setContentType(bodyContent.getContentType());
				bodyVo.setCreateDate((Timestamp) bodyContent.getCreateDate());// 公文正文对象的时间需要单独处理一下
				bodyVo.setContent(bodyContent.getContent());	
				if(bodyVo.getContentType()!=null) {
					if(bodyVo.getContentType().intValue() != MainbodyType.HTML.getKey()) {
						bodyVo.setFileId(Long.parseLong(bodyContent.getContent()));
					}
				}
				bodyVo.setBodyContent(bodyContent);// 公文正文对象
			}
			CtpContentAll transPdfContent = GovdocContentHelper.getOnlyTransBodyContentByModuleId(summaryVO.getSummaryId(), MainbodyType.Pdf.getKey());
			if (transPdfContent != null) {
				bodyVo.setPdfFileId(transPdfContent.getContent());
			}
			CtpContentAll transOfdContent = GovdocContentHelper.getOnlyTransBodyContentByModuleId(summaryVO.getSummaryId(), MainbodyType.Ofd.getKey());
			if (transOfdContent != null) {
				bodyVo.setOfdFileId(transOfdContent.getContent());
			}
			// 需要获得创建公文的单位的开关_正文套红日期
			bodyVo.setTaohongriqiSwitch(govdocOpenManager.isTaohongriqiSwitch());
			summaryVO.setBodyVo(bodyVo);
			
			CtpAffair affair = summaryVO.getAffair();
			if(affair != null) {
				ContentConfig _config = ContentConfig.getConfig(ModuleType.edoc);
				AppContext.putRequestContext("contentCfg", _config);
				
				// 该变量在HTML内容超多的时候会导致页面面瘫 但同时IE8下正文套红的JS错误又恢复 TODO
				// HTML内容区隔
				if (MainbodyType.HTML.getKey() != summaryVO.getBodyVo().getBodyContent().getContentType().intValue()) {
					AppContext.putRequestContext("currentContentId", summaryVO.getBodyVo().getBodyContent().getContent());
				}
				
				if(affair.getMemberId() != AppContext.currentUserId()){// 代理或者代理 需要将被代理/代录人的id和name传到前台页面
					V3xOrgMember member = orgManager.getMemberById(affair.getMemberId());
					summaryVO.setAgencyAffairMerberId(member.getId());
					summaryVO.setAgencyAffairMerberName(member.getName());
				}	
			}
		} catch (Exception e) {
			throw new BusinessException(e);
		}
	}
	
	/**
	 * 公文正文回填时参数设置
	 * @param bodyVo
	 * @param bodyContent
	 */
	public static void fillTemplateBodyVo(GovdocBodyVO bodyVo, CtpContentAll bodyContent) {
		String currContent = bodyContent.getContent();
		Integer currContentType = bodyContent.getContentType();
		
		if(Strings.isBlank(currContent)){
			currContent="-1";
		}
		//如果正文类型是word，content为-1，代表模板设置的正文为 无
		if(currContentType == 41 && ("-1".equals(currContent))){
			currContentType = -1;
		}
		
		bodyVo.setBodyType(GovdocUtil.getBodyType(currContentType));
		bodyVo.setContentType(currContentType);
		bodyVo.setContent(currContent);
		if(bodyVo.getContentType()!=null) {
			if(currContentType == -1){
				bodyVo.setFileId(-1l);
			}else{
				if(bodyVo.getContentType().intValue() != MainbodyType.HTML.getKey() && Strings.isNotBlank(bodyContent.getContent())) {
					bodyVo.setFileId(Long.parseLong(bodyContent.getContent()));
				}	
			}
		}
		bodyVo.setCreateDate(bodyContent.getCreateDate());
	}
	
	/**
	 * 公文模板保存正文相关
	 * @param bodyVo
	 * @param template
	 * @param baseInfo
	 * @throws BusinessException
	 */
    public static GovdocBodyVO fillSendBodyVo(GovdocBaseVO baseVo, Map<String, String> para) throws BusinessException {
    	//公文正文相关，用于公文发送
		GovdocBodyVO bodyVo = new GovdocBodyVO();
		bodyVo.setBodyType(ParamUtil.getString(para, "govdocBodyType", null));
		bodyVo.setContentType(ParamUtil.getInt(para, "govdocContentType", null, false));
		bodyVo.setContent(ParamUtil.getString(para, "govdocContent", null));
		bodyVo.setFileName(ParamUtil.getString(para, "myContentNameId", null));
		bodyVo.setBodyContent(bodyVo.getContentType(), bodyVo.getContent(), new Date());
		baseVo.setBodyVo(bodyVo);
		return bodyVo;
    }
    

	/**
	 * 公文模板保存正文相关
	 * @param bodyVo
	 * @param template
	 * @param baseInfo
	 * @throws BusinessException
	 */
    public static void fillSaveTemplateBodyVo(GovdocBodyVO bodyVo, CtpTemplate template, Map<String, String> baseInfo) throws BusinessException {
		//正文参数
      	String govdocContent = (String)baseInfo.get("govdocContent");
      	String govdocBodyType = (String)baseInfo.get("govdocBodyType");
        //正文套红模板
        Long bindTHTemplateId = Strings.isBlank(baseInfo.get("bindTHTemplateId")) ? -1 : Long.parseLong((String)baseInfo.get("bindTHTemplateId"));        
        //正文设置
  	  	if(StringUtils.isNotBlank(govdocBodyType) && "-1".equals(govdocBodyType)){//如果是无正文模板则默认保存为word类型  正文ID为-1
	  	  	govdocContent = "-1";
	        govdocBodyType = MainbodyType.OfficeWord.name();
        }
  	  	Integer govdocContentType = GovdocUtil.getContentType(govdocBodyType);
  	  	//公文正文对像设置
        Object obj = template.getExtraAttr("govdocContentAll");
        if(obj != null) {
        	bodyVo.setBodyContent((CtpContentAll)obj);
        } else {
        	bodyVo.setBodyContent(govdocContentType, govdocContent, new Date());
  	  		bodyVo.getBodyContent().setCreateId(template.getModifyMember());
        }
  	  	bodyVo.getBodyContent().setSort(2);//模板的正文sort为2
  	  	bodyVo.getBodyContent().setModifyDate(new Date());
	  	bodyVo.getBodyContent().setModifyId(template.getModifyMember());
	  	bodyVo.getBodyContent().setModuleId(template.getId());
	  	bodyVo.getBodyContent().setModuleTemplateId(-1L);
	  	bodyVo.getBodyContent().setContentType(govdocContentType);
	  	bodyVo.getBodyContent().setContent(govdocContent);
	  	bodyVo.setBindTHTemplateId(bindTHTemplateId);
    }
    
}
