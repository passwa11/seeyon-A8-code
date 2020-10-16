package com.seeyon.v3x.edoc.webmodel;

import java.util.ArrayList;
import java.util.List;

import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;


public class MoreSignSelectPerson {
	
	private V3xOrgEntity selObj;
	private List <V3xOrgMember> selPersons;
	public V3xOrgEntity getSelObj() {
		return selObj;
	}
	public void setSelObj(V3xOrgEntity selObj) {
		this.selObj = selObj;
	}
	public List<V3xOrgMember> getSelPersons() {
		return selPersons;
	}
	public void setSelPersons(List<V3xOrgMember> selPersons) {
		//去掉重复
		List<Long> memIdList=new ArrayList<Long>();
		List<V3xOrgMember> tempList=new ArrayList<V3xOrgMember>();
		for(V3xOrgMember mem:selPersons)
		{
			//修改bug 允许人员重复 26753
			//if(!memIdList.contains(mem.getId()))
			{
				memIdList.add(mem.getId());
				tempList.add(mem);
			}
		}
		this.selPersons = tempList;
	}

}
