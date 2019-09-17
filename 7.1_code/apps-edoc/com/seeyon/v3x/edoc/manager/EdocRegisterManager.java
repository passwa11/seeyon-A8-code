package com.seeyon.v3x.edoc.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.RegisterBody;

/**
 * @author 唐桂林
 */
public interface EdocRegisterManager {

    /**
     * 登记公文
     * @param edocRegister 登记对象
     * @return boolean 登记是否成功
     */
    public void createEdocRegister(EdocRegister edocRegister);

    /**
     * 批量登记公文
     * @param edocRegister
     * @return
     */
    public void createEdocRegister(List<EdocRegister> list);

    
    /**
     * 批量登记公文
     * @param edocRegister
     * @return
     */
    public EdocRegister getEdocRegister(long id);    
    
    /**
     * 删除登记
     * @param ids
     */
    public void deleteEdocRegister(EdocRegister edocRegister);

    /**
     * 
     * @param type
     * @param condition
     * @return
     */
    public List<EdocRegister> findEdocRegisterList(int type, Map<String, Object> condition)  throws BusinessException;
    
    /**
     * 查询登记信息
     * @param orgAccountId 单位id
     * @param state 登记状态
     * @param registerType
     * @return
     */
    public List<EdocRegister> findList(String registerIds, int state, int registerType, String condition, String[] value);
    
	/**
	 * 
	 * @param ids
	 * @return
	 */
	public List<EdocRegister> findList(String[] ids);
	
    /**
     * 修改登记信息，与edoc_summary表建立关联
     * @param edocRegister 登记对象
     * @return boolean 修改是否成功
     */
    public void updateEdocRegister(EdocRegister edocRegister);
    
    /**
     * 批量修改登记信息，与edoc_summary表建立关联
     * @param edocRegister 登记对象
     * @return boolean 修改是否成功
     */    
    public void updateEdocRegister(List<EdocRegister> registerList);    
    
    /**
     * 修改登记状态
     * @param registerId
     * @param state
     */
    public void updateEdocRegisterState(Long registerId, int state);
    
    /**
     * 批量修改登记状态
     * @param registerId
     * @param state
     */    
    public void updateEdocRegisterState(Long[] registerId, int state);    
    
    /**
	 * 获取已登记的收文
	 * @author lijl
	 * @param state 状态:0草稿 1已登记
	 * @param currentUser 当前用户
	 * @return List
	 */
	public List<EdocRegister> findRegisterByState(String condition, String[] values,int state,User user);
	/**
	 * 根据ID获取EdocRegister对象
	 * @author lijl
	 * @param id Register对象Id
	 * @return EdocRegister对象
	 */
	public EdocRegister findRegisterById(long id);
	
	/**
	 * 根据签收id获得EdocRegister对象
	 * @param recieveId签收id
	 * @return
	 */
	public EdocRegister findRegisterByRecieveId(long recieveId);
	
	/**
	 * 在分发的时候,修改一些数据(状态,分发人信息等)
	 * @author lijl
	 * @param edocRegister 根据id查询出的EdocRegister对象
	 */
	public void update(EdocRegister edocRegister);
	/**
	 * 根据distribute_edoc_id获取EdocRegister对象
	 * @author lijl
	 * @param id Register对象Id
	 * @return EdocRegister对象
	 */
	public EdocRegister findRegisterByDistributeEdocId(long id);
	
	/**
	 * 查询登记正文
	 * @param registerId
	 * @return
	 */
    public RegisterBody findRegisterBodyByRegisterId(long registerId);
    
	/**
	 * 删除登记正文
	 * @param registerId
	 * @return
	 */
    public void deleteRegisterBody(RegisterBody registerBody);
    /**
	 * 根据distribute_edoc_id获取list
	 * @author weijb
	 * @param id Register对象Id
	 * @return EdocRegister对象
	 */
    public List<EdocRegister> findRegister(long summaryId);
    
    
    /**
     * 某单位下是否有登记待发数据(用于登记开关关闭时的 前提条件判断，当有登记待发数据时，不能关闭)
     * @param accountId
     * @return
     */
    public List<EdocRegister> isHasWaitRegistersByAccountId(long accountId);

    public int findWaitRegisterCountByAccountId(Long accountId);
    
    /**
     * 收文登记 内部文号判重
     */
    public String checkRegisterSerialNoExcludeSelf(String registerId,String serialNo);
}
