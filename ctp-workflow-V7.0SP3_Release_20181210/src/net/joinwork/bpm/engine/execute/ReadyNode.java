/*
 * Created on 2004-5-11
 *
 */
package net.joinwork.bpm.engine.execute;

import java.io.Serializable;

import net.joinwork.bpm.definition.BPMAbstractNode;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;


/**
 * 运行中的节点信息
 * @version 1.00
 */
public class ReadyNode implements Serializable{
public static final long serialVersionUID = 1;
	/**
	 * 节点Id
	 */
	private String id;
	/**
	 * 节点名称
	 */
	private String name;
	/**
	 * 节点数量
	 */
	private int num;
	/**
	 * 节点类型
	 */
	private String policy;	
	
	public ReadyNode(BPMAbstractNode node){
		id=node.getId();
		name = node.getName();
		num=1;
		BPMSeeyonPolicy nodePolicy= node.getSeeyonPolicy();
		if(null!= nodePolicy){
		    policy= nodePolicy.getId();
		}
	}
	public String toString(){
		
		return "readyNode: id="+id+" name="+name+" policy="+policy;
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
	 * @return
	 */
	public int getNum() {
		return num;
	}

	/**
	 * @param i
	 */
	public void setNum(int i) {
		num = i;
	}
    /**
     * @return the policy
     */
    public String getPolicy() {
        return policy;
    }

}
