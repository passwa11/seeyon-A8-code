package com.seeyon.apps.meetingSeat.po;

/*
 * 参会人员信息
 * 
 * */

public class MeetingSeatPerson {

	private String name;
	private String dep;
	private String col;
	private String row;

	public MeetingSeatPerson() {
	}

	public MeetingSeatPerson(String name, String dep, String col, String row) {
		this.name = name;
		this.dep = dep;
		this.col = col;
		this.row = row;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDep() {
		return dep;
	}

	public void setDep(String dep) {
		this.dep = dep;
	}

	public String getCol() {
		return col;
	}

	public void setCol(String col) {
		this.col = col;
	}

	public String getRow() {
		return row;
	}

	public void setRow(String row) {
		this.row = row;
	}

}
