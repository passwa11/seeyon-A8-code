package com.seeyon.v3x.edoc.domain;
import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.seeyon.v3x.common.domain.BaseModel;

/**
 * The persistent class for the edoc_obj_team_member database table.
 * 
 * @author BEA Workshop Studio
 */
public class EdocObjTeamMember  extends BaseModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4070148254114937543L;
	//default serial version id, required for serializable classes.
	private long memberId;
	private long teamId;
	private String teamType;
	private int sortNum;  //排序字段


	public String toObjStr()
	{
		return teamType+"|"+memberId;
	}

    public EdocObjTeamMember() {
    }

	public long getMemberId() {
		return this.memberId;
	}
	public void setMemberId(long memberId) {
		this.memberId = memberId;
	}
	
	
	public int getSortNum() {
		return sortNum;
	}

	public void setSortNum(int sortNum) {
		this.sortNum = sortNum;
	}

	public long getTeamId() {
		return this.teamId;
	}
	public void setTeamId(long teamId) {
		this.teamId = teamId;
	}

	public String getTeamType() {
		return this.teamType;
	}
	public void setTeamType(String teamType) {
		this.teamType = teamType;
	}

	public String toString() {
		return new ToStringBuilder(this)
			.append("id", getId())
			.toString();
	}
}