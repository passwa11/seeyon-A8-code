package com.seeyon.v3x.edoc.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.dao.EdocRegisterDao;
import com.seeyon.v3x.edoc.dao.RegisterBodyDao;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.RegisterBody;

/**
 * 
 * @author 唐桂林
 * 
 */
public class EdocRegisterManagerImpl implements EdocRegisterManager {

	private EdocRegisterDao edocRegisterDao = null;
	private RegisterBodyDao registerBodyDao = null;
	private ConfigManager configManager = null;

	public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }
	
	public ConfigManager getConfigManager() {
        return configManager;
    }
	
	/**
	 * @param edocRegisterDao
	 *            the edocRegisterDao to set
	 */
	public void setEdocRegisterDao(EdocRegisterDao edocRegisterDao) {
		this.edocRegisterDao = edocRegisterDao;
	}

	/**
	 * @param registerBodyDao
	 *            the registerBodyDao to set
	 */
	public void setRegisterBodyDao(RegisterBodyDao registerBodyDao) {
		this.registerBodyDao = registerBodyDao;
	}	
	

	@Override
	public void createEdocRegister(EdocRegister edocRegister) {
		edocRegisterDao.save(edocRegister);

	}

	@Override
	public void createEdocRegister(List<EdocRegister> list) {
		DBAgent.saveAll(list);
	}

	@Override
	public void deleteEdocRegister(EdocRegister edocRegister) {
		edocRegisterDao.deleteEdocRegister(edocRegister);
	}
	
	@Override
	public List<EdocRegister> findList(String registerIds, int state, int registerType, String condition, String[] value) {
		return edocRegisterDao.findList(registerIds, state,	registerType, condition, value);
	}
	
	/**
	 * 为代理人所能查看的会议加上代理标识
	 */
	private List<EdocRegister> convertEdocRegistersAgent(List<EdocRegister> registers,Long currentUserId, AgentModel agent) {
		List<EdocRegister> result = null;		
		if(CollectionUtils.isNotEmpty(registers)) {
			result = new ArrayList<EdocRegister>();
			//判断当前用户是不是让别人干活的人
			boolean isAgentTo = agent.getAgentToId().equals(currentUserId);
			for(EdocRegister er : registers) {
				try {
					EdocRegister register = (EdocRegister) er.clone();
					register.setId(er.getId());
					//设置代理时列表信息全部显示为蓝色的错误
					if(register!=null && null != register.getUpdateTime()) {
						if(!register.getRegisterUserId().equals(AppContext.getCurrentUser().getId())){
							if(agent.getStartDate().compareTo(register.getUpdateTime())<=0 && agent.getEndDate().compareTo(register.getUpdateTime())>=0) {
								er.setProxy(true);
								er.setProxyId(isAgentTo?null:agent.getAgentToId());
								result.add(er);
							}
						} else {
							er.setProxy(true);
							er.setProxyId(isAgentTo?null:agent.getAgentToId());
							result.add(er);
						}
					}
				} catch (CloneNotSupportedException e) {
					//log.error("克隆会议出现异常：", e);
				}
			}
		}
		return result;
	}
	
	@Override
	public List<EdocRegister> findEdocRegisterList(int state, Map<String, Object> condition) throws BusinessException {
		Long userId = (Long)condition.get("userId");
		condition.put("memberId", userId);
		List<EdocRegister> self_edocs = edocRegisterDao.findEdocRegisterList(state, condition);
		
		
		
		//代理事项
		if(state == EdocNavigationEnum.RegisterState.WaitRegister.ordinal() || state == EdocNavigationEnum.RegisterState.Registed.ordinal()) {
			List<AgentModel> agentModels = EdocHelper.getEdocAgents();
	        if(CollectionUtils.isNotEmpty(agentModels)) {
	        	for(AgentModel agent : agentModels) {
	        		if(agent != null) {
	        			if(agent.getAgentId().equals(userId)) {//我给别人做事，标题显示蓝色，标题后添加(代理某某)
		        			condition.put("agentModel", agent);
		        			condition.put("memberId", agent.getAgentToId());
		        			List<EdocRegister> agent_meetings = edocRegisterDao.findEdocRegisterList(state, condition);
		        			agent_meetings = this.convertEdocRegistersAgent(agent_meetings, userId, agent);
		        			Strings.addAllIgnoreEmpty(self_edocs, agent_meetings);
	        			}else if(agent.getAgentToId().equals(userId)) {//别人给我做事，标题显示蓝色
	        				self_edocs = this.convertEdocRegistersAgent(self_edocs, userId, agent);
	        			}
	        		} else {
	        			//log.warn("用户[id=" + userId + "]代理对象集合中存在无效元素！");
	        		}
	        	}
	        	if(self_edocs != null) {
	        		//Collections.sort(self_edocs);
	        	}
	        }
		}
		
		 RegisterBody registerBody = null;
		 if(self_edocs != null) {
	         for(int i=0; i<self_edocs.size(); i++) {
	        	 registerBody = this.findRegisterBodyByRegisterId(self_edocs.get(i).getId());
	        	 self_edocs.get(i).setRegisterBody(registerBody);
	         } 
		 }
		return self_edocs;
	}

	@Override
	public List<EdocRegister> findList(String[] ids) {
		return edocRegisterDao.findList(ids);
	}
	
	@Override
	public EdocRegister getEdocRegister(long id) {
		return edocRegisterDao.getEdocRegister(id);
	}
	@Override
	public EdocRegister findRegisterByRecieveId(long recieveId){
		return edocRegisterDao.findRegisterByRecieveId(recieveId);
	}

	@Override
	public void updateEdocRegister(EdocRegister edocRegister) {
		edocRegisterDao.update(edocRegister);
	}
	
	@Override
	public void updateEdocRegister(List<EdocRegister> list) {
	    DBAgent.saveAll(list);
	}	
	
	@Override
	public void updateEdocRegisterState(Long registerId, int state) {
		edocRegisterDao.updateEdocRegisterState(registerId, state);
	}	
	
	@Override
	public void updateEdocRegisterState(Long[] registerId, int state) {
		edocRegisterDao.updateEdocRegisterState(registerId, state);
	}		
	
	@Override
	public void deleteRegisterBody(RegisterBody registerBody) {
		registerBodyDao.deleteRegisterBody(registerBody);
	}
	
	public RegisterBody findRegisterBodyByRegisterId(long registerId) {
		List<RegisterBody> list = registerBodyDao.findRegisterBodyByRegisterId(registerId);
		if(list!=null && list.size()>0) {
			return list.get(0);
		}
		return null;
	}
	

	/**
	 * 获取已登记的收文
	 * @author lijl
	 * @param state 状态:0草稿 1已登记
	 * @param currentUser 当前用户
	 * @return List
	 */
	@Override
	public List<EdocRegister> findRegisterByState(String condition, String[] values,int state, User user) {
		List<EdocRegister> list = edocRegisterDao.findRegisterByState(condition,values,state, user);
		return list;
	}

	/**
	 * 根据ID获取EdocRegister对象
	 * @author lijl
	 * @param id Register对象Id
	 * @return EdocRegister对象
	 */
	public EdocRegister findRegisterById(long id){
		return edocRegisterDao.findRegisterById(id);
	}
	
	/**
	 * 在分发的时候,修改一些数据(状态,分发人信息等)
	 * @author lijl
	 * @param edocRegister 根据id查询出的EdocRegister对象
	 */
	public void update(EdocRegister edocRegister) {
		
		edocRegisterDao.update(edocRegister);
	}
	/**
	 * 根据distribute_edoc_id获取EdocRegister对象
	 * @author lijl
	 * @param id Register对象Id
	 * @return EdocRegister对象
	 */
	public EdocRegister findRegisterByDistributeEdocId(long id){
		return edocRegisterDao.findRegisterByDistributeEdocId(id);
	}

	@Override
	public List<EdocRegister> findRegister(long summaryId) {
		return edocRegisterDao.findRegister(summaryId);
	}
	
	/**
     * 某单位下是否有登记待发数据(用于登记开关关闭时的 前提条件判断，当有登记待发数据时，不能关闭)
     * @param accountId
     * @return
     */
    public List<EdocRegister> isHasWaitRegistersByAccountId(long accountId){
    	return edocRegisterDao.isHasWaitRegistersByAccountId(accountId);
    }
    
    public int findWaitRegisterCountByAccountId(Long accountId){
    	return edocRegisterDao.findWaitRegisterCountByAccountId(accountId);
    }

	/**
	 * 判断收文登记的时候 当前收文编号是否已经被占用（除开公文自己本身占用的文号）
	 * @param edocSummaryId  :公文ID
	 * @param serialNo		 :内部文号
	 * @return 0:不存在，1:已存在
	 */
	public String checkRegisterSerialNoExcludeSelf(String registerId,String serialNo){
		User user=AppContext.getCurrentUser();
		int exsit= edocRegisterDao.checkSerialNoExsit(registerId, serialNo, user.getAccountId());;
		return String.valueOf(exsit);
	}
}
