/*
 * Created on 2004-5-11
 *
 */
package net.joinwork.bpm.engine.wapi;


/**
 * 运行中的节点信息
 * @version 1.00
 */
public class NodeInfo {
	/**
	 * 节点Id
	 */
	public String id;
	/**
	 * 节点名称
	 */
	public String name;
	/**
	 * 节点描述
	 */
	public String desc;
	/**
	 * 节点批次
	 */
	public int batch;
	public String toString(){
		
		return "NodeInfo: id="+id+" name="+name+" batch="+batch;
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

	

}
