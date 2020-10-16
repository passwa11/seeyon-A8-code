/*
 * Created on 2004-6-3
 *
 */
package com.seeyon.ctp.workflow.manager.impl;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.workflow.config.EngineConfig;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.manager.ProcessDefManager;

import net.joinwork.bpm.definition.BPMDefinitionManager;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.engine.wapi.ProcessObject;

/**
 * @author dinghong
 */
public class ProcessDefManagerFileImpl implements ProcessDefManager {
    
    private final static Log       logger = CtpLogFactory.getLog(ProcessDefManagerFileImpl.class);

    /*
      * (non-Javadoc)
      *
      * @see com.joinwork.bpm.engine.wapi.ProcessDefManager#addProcess(com.joinwork.bpm.definition.BPMProcess)
      */
    public void addProcessInDev(ProcessObject process1) throws BPMException {
        if (process1 == null)
            return;
        BPMProcess process = (BPMProcess) process1;
        process.fillData();
        BPMProcess oldprocess = devProcess.getProcessById(process.getId());
        if (oldprocess != null)
            throw new BPMException(BPMException.EXCEPTION_CODE_PROCESS_EXITE);
        process.setCreateDate(new Date());
        process.setUpdateDate(new Date());
        devProcess.addProcess(process);
    }

    /*
      * (non-Javadoc)
      *
      * @see com.joinwork.bpm.engine.wapi.ProcessDefManager#updateProcess(java.lang.String,
      *      com.joinwork.bpm.definition.BPMProcess, java.lang.String)
      */
    public boolean updateProcessInDev(ProcessObject process1)
            throws BPMException {
        if (process1 == null)
            return false;
        BPMProcess process = (BPMProcess) process1;
        BPMProcess oldProcess = devProcess.getProcessById(process.getId());
        if (oldProcess != null) {
            process.setCreateDate(oldProcess.getCreateDate());
        }
        devProcess.deleteProcess(process.getId());
        process.setUpdateDate(new Date());
        devProcess.addProcess(process);
        return true;

    }

    /*
      * (non-Javadoc)
      *
      * @see com.joinwork.bpm.engine.wapi.ProcessDefManager#deleteProcessInDev(java.lang.String,
      *      int)
      */
    public void deleteProcessInDev(String userId, String processId)
            throws BPMException {
        devProcess.deleteProcess(processId);

    }

    /*
      * (non-Javadoc)
      *
      * @see com.joinwork.bpm.engine.wapi.ProcessDefManager#setProcessInDevReady(java.lang.String,
      *      int)
      */
    public void setProcessInDevReady(String processId) throws BPMException {
        BPMProcess process = devProcess.getProcessById(processId);
        if (process == null) {
            throw new BPMException(BPMException.EXCEPTION_CODE_CASE_NOT_EXITE);
        }
        BPMProcess oldProcess = readyProcess.getProcessById(process.getId());
        //BPMProcess newProcess = BPMProcess.fromXML(process.toXML());
        if (oldProcess == null) {
            readyProcess.addProcess(process);
            process.setCreateDate(new Date());
            process.setUpdateDate(new Date());
        } else {
            //readyProcess.deleteProcess(process.getId());
            process.setCreateDate(oldProcess.getCreateDate());
            process.setUpdateDate(new Date());
            readyProcess.addProcess(process);
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see com.joinwork.bpm.engine.wapi.ProcessDefManager#getProcessInDev(java.lang.String,
      *      int)
      */
    public ProcessObject getProcessInDev(String userId, String processId)
            throws BPMException {
        BPMProcess process = devProcess.getProcessById(processId);
        return process;

    }

    /*
      * (non-Javadoc)
      *
      * @see com.joinwork.bpm.engine.wapi.ProcessDefManager#getProcessInReady(java.lang.String,
      *      int)
      */
    public ProcessObject getProcessInReady(String processId) throws BPMException {
        BPMProcess process = readyProcess.getProcessById(processId);
        return process;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.joinwork.bpm.engine.wapi.ProcessDefManager#deleteProcessInRunning(java.lang.String,
      *      int)
      */
    public void deleteProcessInReady(String processId) throws BPMException {
        readyProcess.deleteProcess(processId);
    }

    /*
      * (non-Javadoc)
      *
      * @see com.joinwork.bpm.engine.wapi.ProcessDefManager#LoadProcessFromFile(java.lang.String)
      */
    public ProcessObject LoadProcessFromFile(String filename) {
        return BPMDefinitionManager.LoadProcess(filename);
    }

    private String domain;

    private BPMDefinitionManager devProcess;

    private BPMDefinitionManager readyProcess;

    /*
      * (non-Javadoc)
      *
      * @see net.joinwork.bpm.engine.wapi.ProcessDefManager#getDomain()
      */
    public String getDomain() {
        return domain;
    }

    /*
      * (non-Javadoc)
      *
      * @see net.joinwork.bpm.engine.wapi.ProcessDefManager#setDomain(java.lang.String)
      */
    public void setDomain(String domain) throws BPMException {
        this.domain = domain;
        devProcess = new BPMDefinitionManager(EngineConfig.getEngine_Home()+ "/" + domain + "/dev/");
        readyProcess = new BPMDefinitionManager(EngineConfig.getEngine_Home()+ "/" + domain + "/ready/");
    }

    /*
      * (non-Javadoc)
      *
      * @see net.joinwork.bpm.engine.wapi.ProcessDefManager#addProcessInDev(java.lang.String)
      */
    public void addProcessInDev(String processXML) throws BPMException {
        try {
            Document doc = DocumentHelper.parseText(processXML);
            BPMProcess process = new BPMProcess("xml", "XML");
            process.fromXML(doc.getRootElement());
            addProcessInDev(process);
        } catch (DocumentException e) {
            logger.error("出现异常",e);
            throw new BPMException(BPMException.EXCEPTION_CODE_XML_ERROR);
        }
    }

    //seeyon added by lius.
    public void saveOrUpdateProcessInReady(ProcessObject _process) throws BPMException {
        BPMProcess process = (BPMProcess) _process;
        BPMProcess oldProcess = readyProcess.getProcessById(process.getId());
        BPMProcess newProcess = process;
        if (oldProcess == null) {
        	newProcess.setCreateDate(new Date());
            newProcess.setUpdateDate(new Date());
            readyProcess.addProcess(newProcess);
        } else {
            readyProcess.deleteProcess(oldProcess.getId());
            newProcess.setCreateDate(oldProcess.getCreateDate());
            newProcess.setUpdateDate(new Date());
            readyProcess.addProcess(newProcess);
        }
    }

    //seeyon end
}
