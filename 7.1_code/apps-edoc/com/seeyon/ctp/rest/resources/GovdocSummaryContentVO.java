/**
 * Author : xuqw
 *   Date : 2015年12月11日 下午6:20:29
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.rest.resources;

import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.content.mainbody.CtpContentAllBean;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;

/**
 * <p>Title       : 协同正文VO</p>
 * <p>Description : 代码描述</p>
 * <p>Copyright   : Copyright (c) 2012</p>
 * <p>Company     : seeyon.com</p>
 */
public class GovdocSummaryContentVO {

    private CtpContentAllBean ctpContentAllBean = null;
    
    private String formRightId="";
    
    /**  正文最后更新时间， 主要用于Office前端缓存  **/
    private String lastModified = null;
    
    /** 是否是转发表单 **/
    private boolean isForwardForm = false;
    
    /** 表单正文是否有office正文 **/
    private boolean hasOffice = false;
    
    /** 表单多视图列表 **/
    private List<Map<String, String>> contentList = null; 
    
            
            
    public String getFormRightId() {
		return formRightId;
	}

	public void setFormRightId(String formRightId) {
		this.formRightId = formRightId;
	}

	private GovdocSummaryContentVO(CtpContentAllBean bean){
        this.ctpContentAllBean = bean;
    }
    
    public static GovdocSummaryContentVO valutOf(CtpContentAllBean bean){
        return new GovdocSummaryContentVO(bean);
    }
    
    
    public Long getId(){
        return ctpContentAllBean.getId();
    }
    
    /* 正文类型 */
    public Integer getContentType(){
        return ctpContentAllBean.getContentType();
    }
    
    /*正文id*/
    public Long getFileId(){
    	Long fId = ctpContentAllBean.getContentDataId();
    	MainbodyType mt = MainbodyType.getEnumByKey(ctpContentAllBean.getContentType());
    	if(mt!=null && (mt == MainbodyType.Ofd || mt == MainbodyType.OfficeExcel || mt == MainbodyType.OfficeWord || mt == MainbodyType.Pdf || mt == MainbodyType.WpsExcel || mt == MainbodyType.WpsWord)){
    		try{
    			fId = Long.parseLong(ctpContentAllBean.getContent());
    		}catch(Exception e){
    		}
    	}
    	return fId;
    }
    
    /* 正文内容转化成HTML后的内容 */
    public String getContentHtml(){
    	String contentHtml = ctpContentAllBean.getContentHtml();
    	if(Strings.isNotBlank(contentHtml) && contentHtml.indexOf("formmain_")<0){
    		contentHtml = StringUtil.replace(contentHtml, "</script>", "</scr\"+\"ipt>");
        }
        return contentHtml;
    }
	
    public Long getModuleId(){
        return ctpContentAllBean.getModuleId();
    }
    
    public String getRightId(){
        return ctpContentAllBean.getRightId();
    }
    
    public Long getContentDataId(){
        return ctpContentAllBean.getContentDataId();
    }
    
    public void setForwardForm(boolean isForwardForm) {
        this.isForwardForm = isForwardForm;
    }
    
    public boolean getIsForwardForm() {
        return this.isForwardForm;
    }
    
    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
    
    public String getLastModified() {
        return lastModified;
    }
    
    public boolean getIsLightForm(){
        boolean ret = false;
        Object isLightForm = ctpContentAllBean.getAttr("isLightForm");
        if(isLightForm != null){
            ret = (Boolean)isLightForm;
        }
        return ret;
    }

    public List<Map<String, String>> getContentList() {
        return contentList;
    }

    public void setContentList(List<Map<String, String>> contentList) {
        this.contentList = contentList;
    }

    public boolean isHasOffice() {
        return hasOffice;
    }

    public void setHasOffice(boolean hasOffice) {
        this.hasOffice = hasOffice;
    }
}
