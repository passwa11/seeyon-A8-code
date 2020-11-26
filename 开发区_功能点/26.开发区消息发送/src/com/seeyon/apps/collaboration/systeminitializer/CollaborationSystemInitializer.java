package com.seeyon.apps.collaboration.systeminitializer;



import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.collaboration.quartz.CollaborationJob;
import com.seeyon.apps.collaboration.quartz.UpdateAffairState4RepeatAffairJob;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemInitializer;
import com.seeyon.ctp.plugins.resources.CollaborationPluginManager;


/**
 * 每天定时清理合并处理未自动跳过的数据(置为待办手动处理)
 * @author xusx
 *
 */
public class CollaborationSystemInitializer implements SystemInitializer{
    
	private static Log LOGGER = LogFactory.getLog(CollaborationSystemInitializer.class);
	
	public CollaborationPluginManager collaborationPluginManager = null;
	
	public void setCollaborationPluginManager(CollaborationPluginManager collaborationPluginManager) {
        this.collaborationPluginManager = collaborationPluginManager;
    }
	
	@Override
	public void initialize() {
		
		// 创建清理垃圾数据的定时任务
	    createUpdateAffairStateJob();
		
		//创建清理协同接收人信息的定时任务
		//CollaborationJob.createDeleteColReceiversJob();
		
		// 扫描协同插件
		collaborationPluginManager.init();
	}

	@Override
	public void destroy() {
		
	}
	
	
	
	private void createUpdateAffairStateJob() {
	    
	  //创建清理垃圾数据的定时任务
        CollaborationJob.createUpdateAffairState4RepeatAffairJob();
        UpdateAffairState4RepeatAffairJob  updateAffairState4RepeatAffairJob = (UpdateAffairState4RepeatAffairJob) AppContext.getBean("updateAffairState4RepeatAffairJob");
        if(updateAffairState4RepeatAffairJob != null){
            updateAffairState4RepeatAffairJob.execute(new HashMap<String,String>());
        }
	}

}
