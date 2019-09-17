package com.seeyon.v3x.exchange.domain;

import java.io.Serializable;
import java.sql.Timestamp;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.domain.BaseModel;
import com.seeyon.v3x.edoc.dao.EdocObjTeamDao;
import com.seeyon.v3x.edoc.domain.EdocObjTeam;
import com.seeyon.v3x.exchange.dao.ExchangeAccountDao;

public class EdocExchangeTurnRec extends BaseModel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6227546263610982141L;
	public final static int NOT_TURN_REC_TYPE = 0;
	public final static int TURN_REC_TYPE = 1;
	
	private long edocId;
	private String opinion;
	private Timestamp createTime;
	private long userId;
	private String userName;
	private String typeAndIds;
	private int exchangeType;
	private Long exchangeUserId;
	
	public Long getExchangeUserId() {
		return exchangeUserId;
	}
	public void setExchangeUserId(Long exchangeUserId) {
		this.exchangeUserId = exchangeUserId;
	}
	public int getExchangeType() {
		return exchangeType;
	}
	public void setExchangeType(int exchangeType) {
		this.exchangeType = exchangeType;
	}
	public long getEdocId() {
		return edocId;
	}
	public void setEdocId(long edocId) {
		this.edocId = edocId;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getOpinion() {
		return opinion;
	}
	public void setOpinion(String opinion) {
		this.opinion = opinion;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getTypeAndIds() {
		return typeAndIds;
	}
	public void setTypeAndIds(String typeAndIds) {
		this.typeAndIds = typeAndIds;
	}
	
	public String getSendUnitNames(OrgManager orgManager)throws BusinessException{
		EdocObjTeamDao edocObjTeamDao = (EdocObjTeamDao)AppContext.getBean("edocObjTeamDao");
		ExchangeAccountDao exchangeAccountDao = (ExchangeAccountDao)AppContext.getBean("exchangeAccountDao");
		StringBuilder sendUnitNames = new StringBuilder();
		String _sq = ",";
		if(Strings.isNotBlank(typeAndIds)){
			String[] typeAndIdAttr = typeAndIds.split("[,]");
			
			String tempName = null;
			for(String typeAndId :typeAndIdAttr){
				String[] tAttr = typeAndId.split("[|]");
				String type = tAttr[0];
				String unitId = tAttr[1];
				if("Account".equals(type)){
					V3xOrgAccount account = orgManager.getAccountById(Long.parseLong(unitId));
					if(account != null){
					    tempName = account.getName();
					}
				}else if("Department".equals(type)){
					V3xOrgDepartment dep = orgManager.getDepartmentById(Long.parseLong(unitId));
					if(dep!=null){
					    tempName = dep.getName();
					}
				}else if("OrgTeam".equals(type)){
					EdocObjTeam team = edocObjTeamDao.get(Long.parseLong(unitId));
					if(team!=null){
					    tempName = team.getName();
					}
				}else if("ExchangeAccount".equals(type)){
					ExchangeAccount exchangeAccount = exchangeAccountDao.get(Long.parseLong(unitId));
					if(exchangeAccount!=null){
					    tempName = exchangeAccount.getName();
					}
				}
				if(tempName != null){
				    if(sendUnitNames.length() > 0){
				        sendUnitNames.append(_sq);
				    }
				    sendUnitNames.append(tempName);
				}
			}
		}
		return sendUnitNames.toString();
	}
}
