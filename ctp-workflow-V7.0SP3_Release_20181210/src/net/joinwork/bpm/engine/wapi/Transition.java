/*
 * Created on 2004-5-18
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.joinwork.bpm.engine.wapi;

import java.io.Serializable;

/**
 * 连接信息
 * @author dinghong
 * @version 1.00
 */
public class Transition implements Serializable{
	/**
	 * 
	 */
	public Transition() {
		
	}

	/**
	 * 状态Id
	 */
	public String id;
	/**
	 * 状态名称
	 */
	public String name;
	/**
	 * 状态描述
	 */
	public String desc;
	public String toString(){
		return name+"(id:"+id+")"; 
	}
	/**
	 * @return
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param string
	 */
	public void setDesc(String string) {
		desc = string;
	}

	/**
	 * @param string
	 */
	public void setId(String string) {
		id = string;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

}
