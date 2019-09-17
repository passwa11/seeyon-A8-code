package com.seeyon.v3x.edoc.domain;

import com.seeyon.apps.govdoc.vo.OrgTeamVo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.domain.BaseModel;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.seeyon.ctp.organization.bo.V3xOrgEntity.TOXML_PROPERTY_NAME;
import static com.seeyon.ctp.organization.bo.V3xOrgEntity.TOXML_PROPERTY_id;

/**
 * The persistent class for the edoc_obj_team database table.
 * 
 * @author BEA Workshop Studio
 */
public class EdocObjTeam  extends BaseModel implements Serializable {
	//default serial version id, required for serializable classes.
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7576292870451560996L;
	public static Byte STATE_DEL=0;
	public static Byte STATE_USE=1;
	public static Byte STATE_STOP=2;
	
	public static final String ENTITY_TYPE_OrgTeam = "OrgTeam";
	
	
	private java.sql.Timestamp createTime;
	private String description;
	private String name;
	private long orgAccountId;
	private Long ownerId;
	private Integer sortId = 1;
	private Byte state;
	private java.sql.Timestamp updateTime;
	private Set<EdocObjTeamMember>edocObjTeamMembers;

	//客开 项目名称： 作者：fzc 修改日期：2018-4-16 [修改功能：]start
    private List<OrgTeamVo> orgTeamVos = new ArrayList<OrgTeamVo>();

    public List<OrgTeamVo> getOrgTeamVos() {
        return orgTeamVos;
    }

    public void setOrgTeamVos(List<OrgTeamVo> orgTeamVos) {
        this.orgTeamVos = orgTeamVos;
    }
    //客开 项目名称： 作者：fzc 修改日期：2018-4-16 [修改功能：]end

	private String selObjsStr;
	

	public void changeTeamMember()
	{
		if(selObjsStr!=null && !"".equals(selObjsStr))
		{
			String [] objs=selObjsStr.split(",");
			for(int i=0;i<objs.length;i++)
			{
				String [] items=objs[i].split("[|]");
				EdocObjTeamMember eot=new EdocObjTeamMember();
				eot.setIdIfNew();
				eot.setTeamId(this.id);
				eot.setTeamType(items[0]);
				eot.setMemberId(Long.parseLong(items[1]));		
				eot.setSortNum(i);
				this.edocObjTeamMembers.add(eot);
			}
		}
	}
	public void changeSelObjsStr()
	{
	    StringBuilder _selObjStr = new StringBuilder();
        getEdocObjTeamMembers(); //lazy
        for(EdocObjTeamMember etm:edocObjTeamMembers)
        {
            if(_selObjStr.length() > 0){
                _selObjStr.append(",");
             }
            _selObjStr.append(etm.toObjStr());
        }
        selObjsStr = _selObjStr.toString();
	}
	
    public EdocObjTeam() {
    	edocObjTeamMembers=new HashSet<EdocObjTeamMember>();
    	state=STATE_USE;
    	createTime=updateTime=new java.sql.Timestamp(System.currentTimeMillis());
    }

	public java.sql.Timestamp getCreateTime() {
		return this.createTime;
	}
	public void setCreateTime(java.sql.Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public long getOrgAccountId() {
		return this.orgAccountId;
	}
	public void setOrgAccountId(long orgAccountId) {
		this.orgAccountId = orgAccountId;
	}

	public Long getOwnerId() {
		return this.ownerId;
	}
	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}

	public Integer getSortId() {
		return this.sortId;
	}
	public void setSortId(Integer sortId) {
		this.sortId = sortId == null ? 1 : sortId;
	}

	public Byte getState() {
		return this.state;
	}
	public void setState(Byte state) {
		this.state = state;
	}

	public java.sql.Timestamp getUpdateTime() {
		return this.updateTime;
	}
	public void setUpdateTime(java.sql.Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public String toString() {
		return new ToStringBuilder(this)
			.append("id", getId())
			.toString();
	}

	public Set<EdocObjTeamMember> getEdocObjTeamMembers() {
		return edocObjTeamMembers;
	}

	public void setEdocObjTeamMembers(Set<EdocObjTeamMember> edocObjTeamMembers) {
		this.edocObjTeamMembers = edocObjTeamMembers;
	}

	public String getSelObjsStr() {
		return selObjsStr;
	}

	public void setSelObjsStr(String selObjsStr) {
		this.selObjsStr = selObjsStr;
	}
	
	/**
	 * 给选人界面用的，不要轻易修改
	 */
	public void toJsonString(StringBuilder o) {
		o.append("{");
		o.append(TOXML_PROPERTY_id).append(":\"").append(this.getId()).append("\"");
		o.append(",").append(TOXML_PROPERTY_NAME).append(":\"").append(Strings.escapeJavascript(this.getName())).append("\"");
		o.append("}");
	}
	
}