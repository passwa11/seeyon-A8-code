package com.seeyon.apps.govdoc.exchange.util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;

public class GovDataRowXMLObj {
	private long id;
	private String name;
	private int sort;
	private List<GovDataElementXMLObj> elements = new ArrayList<GovDataElementXMLObj>();
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
	
	public List<GovDataElementXMLObj> getElements() {
		return elements;
	}
	public void setElements(List<GovDataElementXMLObj> elements) {
		this.elements = elements;
	}
	
	@XmlAttribute(name = "sort")
	public int getSort() {
		return sort;
	}
	public void setSort(int sort) {
		this.sort = sort;
	}

}
