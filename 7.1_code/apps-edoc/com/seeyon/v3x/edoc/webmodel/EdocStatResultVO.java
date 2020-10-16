package com.seeyon.v3x.edoc.webmodel;

import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.v3x.edoc.util.EdocStatEnum;
import com.seeyon.v3x.edoc.util.EdocStatEnum.EdocStatDisplayTimeTypeEnum;
import com.seeyon.v3x.edoc.util.EdocStatEnum.EdocStatDisplayTypeEnum;

public class EdocStatResultVO implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -631843451254918501L;
	/**
	 * 
	 */
	
	private Integer resultType;
	private String displayId;
	private String displayRow;
	private Integer displayRowCount;
	private String nodePolicy;
	private Integer affairSubApp;
	private Integer countAffairSubApp;
	
	public void setDisplayRowValue(Object[] object, int displayType, int displayTimeType, int resultType) {
		int i = 0;
		if(displayType == EdocStatDisplayTypeEnum.department.key() || displayType==EdocStatDisplayTypeEnum.member.key()) {
			this.setDisplayId(object[i++]+"");
		} else {
			if(displayTimeType == EdocStatDisplayTimeTypeEnum.year.key()) {
				this.setDisplayId(displayTimeType + "-" + object[i++]);
			} else if(displayTimeType == EdocStatDisplayTimeTypeEnum.quarter.key()
			        || displayTimeType == EdocStatDisplayTimeTypeEnum.month.key()) {
				this.setDisplayId(displayTimeType + "-" + object[i++] + "-" + object[i++]);
			} else {
				this.setDisplayId(displayTimeType + "-" + object[i++] + "-" + object[i++] + "-" + object[i++]);
			}
		}
		this.setDisplayRow(object[i++]+"");		
		if(resultType == EdocStatEnum.EdocStatResultTypeEnum.doAndRead.key()) {
		    Object o = object[i++];
			this.setAffairSubApp(o==null ? ApplicationSubCategoryEnum.edocRecHandle.key() : Integer.parseInt(o+""));
		}
		this.setDisplayRowCount(Integer.valueOf(object[i++]+""));
		if(resultType == EdocStatEnum.EdocStatResultTypeEnum.doAndRead.key()) {
			this.setCountAffairSubApp(Integer.parseInt(object[i++]+""));
		}
	}
	
	public void setDisplayRowResultValue(Object[] object, int displayType, int displayTimeType, int resultType) {
		int i = 0;
		if(displayType == EdocStatDisplayTypeEnum.department.key() || displayType==EdocStatDisplayTypeEnum.member.key()) {
			this.setDisplayId(object[i++]+"");
		} else {
			if(displayTimeType == EdocStatDisplayTimeTypeEnum.year.key()) {
				this.setDisplayId(displayTimeType + "-" + object[i++]);
			} else if(displayTimeType == EdocStatDisplayTimeTypeEnum.quarter.key()
			        || displayTimeType == EdocStatDisplayTimeTypeEnum.month.key()) {
				this.setDisplayId(displayTimeType + "-" + object[i++] + "-" + object[i++]);
			} else {
				this.setDisplayId(displayTimeType + "-" + object[i++] + "-" + object[i++] + "-" + object[i++]);
			}
		}
		if(resultType == EdocStatEnum.EdocStatResultTypeEnum.undertaker.key()) {
			this.setDisplayRow(object[i++]+"");
			this.setNodePolicy(object[i++]+"");
		} else {
			this.setDisplayRowCount(Integer.valueOf(object[i++]+""));
		}
	}

	public String getDisplayRow() {
		return displayRow;
	}

	public void setDisplayRow(String displayRow) {
		this.displayRow = displayRow;
	}

	public Integer getDisplayRowCount() {
		return displayRowCount;
	}

	public void setDisplayRowCount(Integer displayRowCount) {
		this.displayRowCount = displayRowCount;
	}

	public Integer getResultType() {
		return resultType;
	}

	public void setResultType(Integer resultType) {
		this.resultType = resultType;
	}

	public String getDisplayId() {
		return displayId;
	}

	public void setDisplayId(String displayId) {
		this.displayId = displayId;
	}

	public Integer getAffairSubApp() {
		return affairSubApp;
	}

	public void setAffairSubApp(Integer affairSubApp) {
		this.affairSubApp = affairSubApp;
	}

	public Integer getCountAffairSubApp() {
		return countAffairSubApp;
	}

	public void setCountAffairSubApp(Integer countAffairSubApp) {
		this.countAffairSubApp = countAffairSubApp;
	}

	public String getNodePolicy() {
		return nodePolicy;
	}

	public void setNodePolicy(String nodePolicy) {
		this.nodePolicy = nodePolicy;
	}
	
}
