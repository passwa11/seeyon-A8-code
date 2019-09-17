package com.seeyon.apps.govdoc.exchange.util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

public class GovDataFormTableXMLObj {
	private long id;
	private String name;
	private List<GovDataRowXMLObj> row = new ArrayList<GovDataRowXMLObj>();
	@XmlAttribute(name = "id")
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	@XmlAttribute(name = "name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<GovDataRowXMLObj> getRow() {
		return row;
	}
	public void setRow(List<GovDataRowXMLObj> row) {
		this.row = row;
	}

}
