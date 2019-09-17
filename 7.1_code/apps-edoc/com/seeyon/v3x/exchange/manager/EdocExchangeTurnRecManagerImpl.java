package com.seeyon.v3x.exchange.manager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.dao.EdocOpinionDao;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.exchange.dao.EdocExchangeTurnRecDao;
import com.seeyon.v3x.exchange.domain.EdocExchangeTurnRec;
import com.seeyon.v3x.exchange.domain.EdocSendRecord;

public class EdocExchangeTurnRecManagerImpl implements EdocExchangeTurnRecManager{

	private static final Log LOGGER = LogFactory.getLog(EdocExchangeTurnRecManagerImpl.class);
	private EdocExchangeTurnRecDao edocExchangeTurnRecDao;
	private AffairManager affairManager;
	private EdocOpinionDao edocOpinionDao;
	private EdocManager edocManager;
	private SendEdocManager sendEdocManager;
	private ProcessLogManager processLogManager;
	private OrgManager orgManager;
	
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public void setProcessLogManager(ProcessLogManager processLogManager) {
		this.processLogManager = processLogManager;
	}

	public void setSendEdocManager(SendEdocManager sendEdocManager) {
		this.sendEdocManager = sendEdocManager;
	}

	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}

	public void setEdocOpinionDao(EdocOpinionDao edocOpinionDao) {
		this.edocOpinionDao = edocOpinionDao;
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	public void setEdocExchangeTurnRecDao(EdocExchangeTurnRecDao edocExchangeTurnRecDao ){
		this.edocExchangeTurnRecDao = edocExchangeTurnRecDao;
	}
	
	public void save(EdocExchangeTurnRec edocExchangeTurnRec){
		edocExchangeTurnRecDao.save(edocExchangeTurnRec);
	}
	
	public EdocExchangeTurnRec findEdocExchangeTurnRecByEdocId(long edocId){
		return edocExchangeTurnRecDao.findEdocExchangeTurnRecByEdocId(edocId);
	}
	
	public Long findSupEdocId(long distributeEdocId){
		return edocExchangeTurnRecDao.findSupEdocId(distributeEdocId);
	}
	
	/**
	 * 下级单位进行意见汇报时，需要先通过ajax判断上级收文是否已经撤销了，如果撤销了给出一个提示
	 * 上级单位已将收文流程撤销，您填写的意见上级单位无法看到
	 * ajax调用
	 * @param turnRec
	 */
	public String isSupEdocCanceled(String subEdocId){
		String msg = "false";
		Long supEdocId = this.findSupEdocId(Long.parseLong(subEdocId));
		if(supEdocId != null ){
			try {
				CtpAffair affair = affairManager.getSenderAffair(supEdocId);
				if(affair!=null && affair.getSubState().intValue() == SubStateEnum.col_waitSend_cancel.key()){
					msg = "true";
				}
			} catch (BusinessException e) {
				LOGGER.error("根据公文id获得发文人affair数据报错!",e);
			}
		}
		return msg;
	}
	
	
	public void updateTurnRec(EdocExchangeTurnRec turnRec){
		edocExchangeTurnRecDao.update(turnRec);
	}
	
	public void delTurnRecByEdocId(long edocId){
		edocExchangeTurnRecDao.delTurnRecByEdocId(edocId);
	}
	
	public List<EdocOpinion> getDelStepBackSupOptions(long edocId,long supEdocId)throws BusinessException{
		List<EdocOpinion> ops = null;
		//先确定是否经过了退回操作
		List<CtpAffair> stepBackAffairs = affairManager.getAffairs(edocId, StateEnum.col_stepBack);
		if(stepBackAffairs!=null && stepBackAffairs.size()>0){
			List<Long> backAffairIds = new ArrayList<Long>();
			for(CtpAffair af : stepBackAffairs){
				backAffairIds.add(af.getId());
			}
			
			ops = edocOpinionDao.findReportToSupAccountOpinions(backAffairIds,edocId);
		}
    	return ops;
	}
	
	
	public List<EdocOpinion> getDelStepBackSupOptions(long edocId) throws BusinessException{
		Long supEdocId = this.findSupEdocId(edocId);
		if(supEdocId != null){
			return getDelStepBackSupOptions(edocId,supEdocId);
		}else{
			return null;
		}
	}
	
	
	public String transCreateSendDataByTurnRec(User user,long summaryId,int exchangeType,String grantedDepartIdStr,
			String opinion,String exchangeMemberId,String returnDeptId) throws BusinessException{
		String msg = "success";
		long exchangeOrgId = 0L;
		if(exchangeType == EdocSendRecord.Exchange_Send_iExchangeType_Dept){
			/**
			 * 如果收文发起人有副岗且也设置了部门收发员，那么在转收文时会弹出选择哪个部门进行交换
			 * returnDeptId 这个就是所选择的部门
			 * 如果没有副岗，returnDeptId就是收文发起人的主岗部门
			 */
			exchangeOrgId = Long.parseLong(returnDeptId);
		}else{
			exchangeOrgId = user.getLoginAccount();
		}
		CtpAffair affair = new CtpAffair();
		affair.setMemberId(user.getId());
		
		//这里是收文summary
		EdocSummary summary = edocManager.getEdocSummaryById(summaryId, true);
		try {
			String exchangeMemberIdStr = exchangeType == EdocSendRecord.Exchange_Send_iExchangeType_Org ? exchangeMemberId : returnDeptId;
			
			sendEdocManager.create(summary, exchangeOrgId, exchangeType,exchangeMemberIdStr , affair,true);
		} catch (Exception e) {
			msg = "fail";
			LOGGER.error("转收文生成待发送数据时报错!",e);
		}
		/**
		 * 生成转收文办理信息
		 */
		EdocExchangeTurnRec turnRec = new EdocExchangeTurnRec();
		turnRec.setIdIfNew();
		turnRec.setEdocId(summaryId);
		turnRec.setOpinion(opinion);
		turnRec.setUserId(user.getId());
		turnRec.setUserName(user.getName());
		turnRec.setCreateTime(new Timestamp(System.currentTimeMillis()));
		turnRec.setTypeAndIds(grantedDepartIdStr); 
		turnRec.setExchangeType(exchangeType);
		if(Strings.isNotBlank(exchangeMemberId)){
			turnRec.setExchangeUserId(Long.parseLong(exchangeMemberId));
		}
		this.save(turnRec);
		
		String processId = summary.getProcessId();
		String exchangeMsg = "";
		if(exchangeType == 0){
			exchangeMsg = ResourceUtil.getString("sys.role.rolename.Departmentexchange");
		}else{
			exchangeMsg = ResourceUtil.getString("sys.role.rolename.Accountexchange");
		}
		processLogManager.insertLog(user, Long.valueOf(processId), -1l, ProcessLogAction.turnRec, exchangeMsg) ;
		
		return msg;
	}
	
	
	
	
	/**
	 * ajax调用，是否已经转收文了
	 */
	public String isTurnReced(String edocId){
		String flag = "false";
		if(this.findEdocExchangeTurnRecByEdocId(Long.parseLong(edocId))!=null){
			flag = "true";
		}
		return flag;
	}
	
	/**
	 * ajax调用，补发时，已发送单位或部门 需要给提示，不能再发送了
	 */
	public String isCanSent(String edocId,String typeAndIds){
		StringBuilder sentUnits = new StringBuilder("true");
		EdocExchangeTurnRec turnRec = findEdocExchangeTurnRecByEdocId(Long.parseLong(edocId));
		String hasTypeAndIds = turnRec.getTypeAndIds();
		if(hasTypeAndIds != null){
			String[] oneTypeAndId = hasTypeAndIds.split("[,]");
			Map<String,String> map = new HashMap<String,String>();
			if(oneTypeAndId != null && oneTypeAndId.length>0){
				for(String ti : oneTypeAndId){
					map.put(ti, "");
				}
			}
			String[] newTypeAndIds = typeAndIds.split("[,]");
			if(newTypeAndIds!=null){
				for(String ti : newTypeAndIds){
					if(map.get(ti)!=null){
						if("true".equals(sentUnits.toString())){
							sentUnits =new StringBuilder("");
						}
						try {							
							String[] str = ti.split("[|]");
							if(ti.indexOf("Account")>-1){
								sentUnits.append(orgManager.getAccountById(Long.parseLong(str[1])).getName()).append(",");
							}else{
								sentUnits.append(orgManager.getDepartmentById(Long.parseLong(str[1])).getName()).append(",");
							}
						} catch (BusinessException e) {
							LOGGER.error("获得转收文主送单位出错!",e);
						}
					}
				}
			}
		}
		String retSentUnits = sentUnits.toString();
		if(!"true".equals(retSentUnits)){
		    retSentUnits = retSentUnits.substring(0,sentUnits.length()-1);
        }
		return retSentUnits;
	}
}








