package com.seeyon.apps.ext.copyFile.pojo;

/*
 * KDoc-实体
 */
public class DocEntity {

	// id
	private long id;
	// doc_lib_id
	private long doc_lib_id;
	// parent_fr_id
	private long parent_fr_id;
	// fr_name
	private String fr_name;
	// is_folder
	private int is_folder;
	// 文件号
	private long source_id;
	// 逻辑目录
	private String logical_path;

	/**
	 * Default Constructor
	 */
	public DocEntity() {
		// do nothing
	}

	// Bean methods......
	public long getId() {
		return this.id;
	}

	public void setId(long theId) {
		this.id = theId;
	}

	public long getDoc_lib_id() {
		return this.doc_lib_id;
	}

	public void setDoc_lib_id(long theDoc_lib_id) {
		this.doc_lib_id = theDoc_lib_id;
	}

	public long getParent_fr_id() {
		return this.parent_fr_id;
	}

	public void setParent_fr_id(long theParent_fr_id) {
		this.parent_fr_id = theParent_fr_id;
	}

	public String getFr_name() {
		return this.fr_name;
	}

	public void setFr_name(String theFr_name) {
		this.fr_name = theFr_name;
	}

	public int getIs_folder() {
		return this.is_folder;
	}

	public void setIs_folder(int theIs_folder) {
		this.is_folder = theIs_folder;
	}

	public long getSource_id() {
		return this.source_id;
	}

	public void setSource_id(long theSource_id) {
		this.source_id = theSource_id;
	}

	public String getLogical_path() {
		return this.logical_path;
	}

	public void setLogical_path(String theLogical_path) {
		this.logical_path = theLogical_path;
	}

	public String toString() {
		StringBuffer returnString = new StringBuffer();
		returnString.append("DocEntity[");
		// Set member attributes
		returnString.append("id = " + this.id + ";\n");
		returnString.append("doc_lib_id = " + this.doc_lib_id + ";\n");
		returnString.append("parent_fr_id = " + this.parent_fr_id + ";\n");
		returnString.append("fr_name = " + this.fr_name + ";\n");
		returnString.append("is_folder = " + this.is_folder + ";\n");
		returnString.append("source_id = " + this.source_id + ";\n");
		returnString.append("logical_path = " + this.logical_path + ";\n");
		returnString.append("]\n");
		return returnString.toString();
	}
}