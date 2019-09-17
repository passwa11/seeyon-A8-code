package com.seeyon.v3x.edoc.domain;
/**
 * 用于公文统计显示
 * @author kyt
 *
 */
public class EdocStatDisObj {
	
	// 行名称
	private String columnName;
	// 收文数
	private int recieveNum;
	// 发文数
	private int sendNum;
	// 签报数
	private int signNum;	
	// 总计
	private int totalNum;
	
	public String getColumnName() {
		return columnName;
	}
	
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
	public int getSendNum() {
		return sendNum;
	}
	
	public void setSendNum(Integer sendNum) {
		this.sendNum = sendNum;
	}
	
	public int getSignNum() {
		return signNum;
	}
	
	public void setSignNum(Integer signNum) {
		this.signNum = signNum;
	}
	
	public int getRecieveNum() {
		return recieveNum;
	}
	
	public void setRecieveNum(Integer recieveNum) {
		this.recieveNum = recieveNum;
	}
	
	public int getTotalNum() {
		return totalNum;
	}
	
	public void setTotalNum(Integer totalNum) {
		this.totalNum = totalNum;
	}	
	
}
