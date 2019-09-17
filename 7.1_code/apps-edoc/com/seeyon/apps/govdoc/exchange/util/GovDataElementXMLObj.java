package com.seeyon.apps.govdoc.exchange.util;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "obj") 
public class GovDataElementXMLObj {
	private long id = -1;
	private String name;
	private String mappingName;
	private String value;
	private String desc;
	private String displayValue;
	
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
	@XmlAttribute(name = "value")
	public String getValue() {
		return value==null?"":String.valueOf(value);
	}
	public void setValue(String value) {
		this.value = value;
	}
	@XmlAttribute(name = "desc")
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	@XmlValue
	public String getDisplayValue() {
		return displayValue;
	}
	public void setDisplayValue(String displayValue) {
		this.displayValue = displayValue;
	}
	
	public static void main(String[] args){
		JaxbUtil requestBinder = new JaxbUtil(GovDataXMLObj.class); 
		GovDataXMLObj go = new GovDataXMLObj();
		go.setSubject("dfdfdfdd");
		GovDataFormTableXMLObj tb = new GovDataFormTableXMLObj();
		tb.setName("table1");
		GovDataElementXMLObj d = new GovDataElementXMLObj();
		d.setDesc("desc");
		d.setDisplayValue("组织1");
		d.setId(111);
		d.setName("6211110285420416291");
		d.setValue("6211110285420416291");
		GovDataRowXMLObj row = new GovDataRowXMLObj();
		row.getElements().add(d);
		row.getElements().add(d);
		tb.getRow().add(row);
		GovDataFormTableXMLObj tb2 = new GovDataFormTableXMLObj();
		tb2.setName("table2");
		row = new GovDataRowXMLObj();
		row.getElements().add(d);
		row.getElements().add(d);
		row.getElements().add(d);
		row.getElements().add(d);
		tb2.getRow().add(row);
		row = new GovDataRowXMLObj();
		row.getElements().add(d);
		row.getElements().add(d);
		row.getElements().add(d);
		row.getElements().add(d);
		tb2.getRow().add(row);
		go.getTable().add(tb);
		go.getTable().add(tb2);
		System.out.println(requestBinder.toXml(go, "utf-8"));
		
		GovDataXMLObj oo = requestBinder.fromXml(requestBinder.toXml(go, "utf-8"));
		System.out.println(oo.getTable().size());
	}
	
	@XmlAttribute(name = "mappingName")
	public String getMappingName() {
		return mappingName;
	}
	public void setMappingName(String mappingName) {
		this.mappingName = mappingName;
	}
}
