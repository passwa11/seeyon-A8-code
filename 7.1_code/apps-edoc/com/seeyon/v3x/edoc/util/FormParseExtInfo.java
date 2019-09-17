/**
 * Author : xuqw
 *   Date : 2015年6月15日 下午12:40:35
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.v3x.edoc.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;

/**
 * <p>Title       : 应用模块名称</p>
 * <p>Description : 代码描述</p>
 * <p>Copyright   : Copyright (c) 2012</p>
 * <p>Company     : seeyon.com</p>
 */
public class FormParseExtInfo {

	private FormOpinionConfig FormOpinionConfig;
    
    private Set<String> files = new HashSet<String>();
    private List<Attachment> attFiles = new ArrayList<Attachment>();
    private List<V3XFile> ctpFiles = new ArrayList<V3XFile>();
    private Map<String, byte[]> byteFile = new HashMap<String, byte[]>();
    private String content = null;
    private Map<String, String[]> field2ValueMap = null;
    
    public void addFiles(String file){
        this.files.add(file);
    }
    
    public void addAttFile(Attachment attFile){
        this.attFiles.add(attFile);
    }
    
    public void addAllAttFIle(List<Attachment> attFile){
        this.attFiles.addAll(attFile);
    }
    
    public void addCtpFile(V3XFile ctpFile){
        this.ctpFiles.add(ctpFile);
    }
    
    public void addByteFile(String name, byte[] fByte){
        this.byteFile.put(name, fByte);
    }
    
    public void setField2ValueMap(Map<String, String[]> field2ValueMap) {
        this.field2ValueMap = field2ValueMap;
    }
    
    public Map<String, String[]> getField2ValueMap() {
        return field2ValueMap;
    }
    
    public Set<String> getFiles() {
        return files;
    }
    
    public List<Attachment> getAttFiles() {
        return attFiles;
    }
    
    public List<V3XFile> getCtpFiles() {
        return ctpFiles;
    }
    
    public Map<String, byte[]> getByteFile() {
        return byteFile;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getContent() {
        return content;
    }

	public FormOpinionConfig getFormOpinionConfig() {
		return FormOpinionConfig;
	}

	public void setFormOpinionConfig(FormOpinionConfig formOpinionConfig) {
		FormOpinionConfig = formOpinionConfig;
	}
    
}
