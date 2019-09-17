package com.seeyon.v3x.edoc.webmodel;

public class EdocStatListResultVO {

	private String id;   //displayId 部门、人员ID
	private String n;    //displayName 显示文字
	private Integer t;   //displayType 按部门、人员、时间类型显示
	private Integer tt;  //displayTimeType 1年 2季 3月 4日
	
	private Integer dAll = 0; //countDoAll 总办件(已办结+未办结)
	private Integer hAll = 0; //countHandleAll 总经办(待办+在办+已办)
	private Integer f = 0;    //countFinish 已办结
	private Integer wf = 0;   //countWaitFinish 未办结
	private Integer s = 0;    //countSent 已发
	private Integer d = 0;    //countDone 已办
	private Integer p = 0;    //countPending 待办
	
	public EdocStatListResultVO(EdocStatVO vo){
		this.setId(vo.getDisplayId());
		this.setN(vo.getDisplayName());
		this.setT(vo.getDisplayType());
		this.setTt(vo.getDisplayTimeType());
		this.setdAll(vo.getCountDoAll());
		this.sethAll(vo.getCountHandleAll());
		this.setF(vo.getCountFinish());
		this.setWf(vo.getCountWaitFinish());
		this.setS(vo.getCountSent());
		this.setD(vo.getCountDone());
		this.setP(vo.getCountPending());
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getN() {
		return n;
	}
	public void setN(String n) {
		this.n = n;
	}
	public Integer getT() {
		return t;
	}
	public void setT(Integer t) {
		this.t = t;
	}
	public Integer getTt() {
		return tt;
	}
	public void setTt(Integer tt) {
		this.tt = tt;
	}
	public Integer getdAll() {
		return dAll;
	}
	public void setdAll(Integer dAll) {
		this.dAll = dAll;
	}
	public Integer gethAll() {
		return hAll;
	}
	public void sethAll(Integer hAll) {
		this.hAll = hAll;
	}
	public Integer getF() {
		return f;
	}
	public void setF(Integer f) {
		this.f = f;
	}
	public Integer getWf() {
		return wf;
	}
	public void setWf(Integer wf) {
		this.wf = wf;
	}
	public Integer getS() {
		return s;
	}
	public void setS(Integer s) {
		this.s = s;
	}
	public Integer getD() {
		return d;
	}
	public void setD(Integer d) {
		this.d = d;
	}
	public Integer getP() {
		return p;
	}
	public void setP(Integer p) {
		this.p = p;
	}
}
