/*
 * Created on 2004-5-17
 *
 */
package com.seeyon.ctp.workflow.manager;

import net.joinwork.bpm.engine.wapi.ProcessObject;

import com.seeyon.ctp.workflow.exception.BPMException;

/**
 * 流程定义模板管理接口.
 * @author shenjian
 * @version 2.00
 */
public interface ProcessDefManager {
    /**
     * 得到流程引擎ID.
     * @return 流程引擎ID
     */
    public String getDomain();

    /**
     * 设置流程引擎ID.
     * @param domain 流程引擎ID
     */
    public void setDomain(String domain) throws BPMException;

    /**
     * 从指定文件加载流程定义模板.
     * @param filename 流程定义模板文件名（*.process）
     * @return 流程定义模板
     */
    public ProcessObject LoadProcessFromFile(String filename);

    /**
     * 在开发库中查询指定的流程定义模板.
     * 查询用户对应于指定的流程定义模板，必须有可查询处于开发状态的流程模板(QueryDeveloping )的权限 
     * @param userId 查询用户Id
     * @param processId 流程定义模板Id
     * @return 流程定义模板
     * //@throws DatabaseException 指定模板不存在或数据库错误，将抛此例外
     */
    public ProcessObject getProcessInDev(String userId, String processId) throws BPMException;

    /**
     * 在可执行库中查询指定的流程定义模板.
     * @param processId 流程定义模板Id
     * @return 流程定义模板
     * @throws BPMException 指定模板不存在或数据库错误，将抛此例外
     */
    public ProcessObject getProcessInReady(String processId) throws BPMException;

    /**
     * 删除开发库中的指定流程定义模板.
     * 操作用户对应于指定的流程定义模板，必须有可删除处于开发状态的流程模板(DeleteDeveloping)的权限 
     * @param userId 用户Id
     * @param processId 流程定义模板Id
     * @throws BPMException
     */
    public void deleteProcessInDev(String userId, String processId) throws BPMException;

    /**
     * 删除可执行库中的指定流程定义模板.
     * @param processId 流程定义模板Id
     * @throws BPMException
     */
    public void deleteProcessInReady(String processId) throws BPMException;

    /**
     * 将开发库中的指定流程定义模板设置为可执行.
     * @param processId
     * @throws BPMException
     */
    public void setProcessInDevReady(String processId) throws BPMException;

    /**
     * 更改开发库中的指定流程定义模板.
     * @param process
     * @return
     * @throws BPMException
     */
    public boolean updateProcessInDev(ProcessObject process) throws BPMException;

    /**
     * 在开发库中增加指定的流程定义模板.
     * @param process 新的流程定义模板
     * @throws BPMException
     */
    public void addProcessInDev(ProcessObject process) throws BPMException;

    /**
     * 在开发库中增加指定的流程定义模板.
     * @param processXML 新的流程定义模板
     * @throws BPMException
     */
    public void addProcessInDev(String processXML) throws BPMException;

    /**
     * 立即更新一个流程更义。
     * @param process
     * @throws BPMException
     */
    public void saveOrUpdateProcessInReady(ProcessObject process) throws BPMException;

}
