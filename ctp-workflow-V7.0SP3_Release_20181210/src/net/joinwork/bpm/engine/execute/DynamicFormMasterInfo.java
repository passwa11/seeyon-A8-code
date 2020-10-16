/**
 * 
 */
package net.joinwork.bpm.engine.execute;

import java.util.List;

/**
 * @author : muj
 * @Date : 2017年3月17日
 */
public class DynamicFormMasterInfo {
	/* 动态底表Id baseFormAppId */
	private String formAppId;
	/* 节点Id */
	private List<String> nodeIds;
	/* 底表数据Id */
	private List<String> masterIds;

	public String getFormAppId() {
		return formAppId;
	}

	public void setFormAppId(String formAppId) {
		this.formAppId = formAppId;
	}

	public List<String> getNodeIds() {
		return nodeIds;
	}

	public void setNodeIds(List<String> nodeIds) {
		this.nodeIds = nodeIds;
	}

	public List<String> getMasterIds() {
		return masterIds;
	}

	public void setMasterIds(List<String> masterIds) {
		this.masterIds = masterIds;
	}

	

	
}
