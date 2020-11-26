package com.seeyon.apps.collaboration.listener;

import com.seeyon.apps.collaboration.constants.ColConstant.SendType;
import com.seeyon.apps.collaboration.event.CollaborationAutoSkipEvent;
import com.seeyon.apps.collaboration.event.CollaborationStartEvent;
import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.manager.ColMessageManager;
import com.seeyon.apps.collaboration.manager.ColPubManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.system.signet.manager.CtpSignetManager;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.quartz.QuartzJob;
import com.seeyon.ctp.common.template.manager.CollaborationTemplateManager;
import com.seeyon.ctp.common.template.manager.FormTemplateDesignManager;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.form.modules.event.FlowFormSaveAllEvent;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.event.MemberAccountChangeEvent;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.annotation.ListenEvent;
import com.seeyon.v3x.system.signet.domain.V3xSignet;
import com.seeyon.v3x.system.signet.manager.SignetManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class CollaborationListener {
	private static Log log = LogFactory.getLog(CollaborationListener.class);
	private TemplateManager templateManager;
	private OrgManager orgManager;
	private SignetManager    signetManager;
    private CtpSignetManager ctpSignetManager;
    private QuartzJob affairIsOvertopTimeJob;
    private ColMessageManager colMessageManager;
    private CollaborationTemplateManager collaborationTemplateManager;
    private FormTemplateDesignManager formTemplateDesignManager;
    private ColPubManager colPubManager;
    private ColManager colManager;

    public CollaborationTemplateManager getCollaborationTemplateManager() {
        return collaborationTemplateManager;
    }

    public void setCollaborationTemplateManager(CollaborationTemplateManager collaborationTemplateManager) {
        this.collaborationTemplateManager = collaborationTemplateManager;
    }
    
  

	public FormTemplateDesignManager getFormTemplateDesignManager() {
		return formTemplateDesignManager;
	}

	public void setFormTemplateDesignManager(FormTemplateDesignManager formTemplateDesignManager) {
		this.formTemplateDesignManager = formTemplateDesignManager;
	}

	public ColMessageManager getColMessageManager() {
        return colMessageManager;
    }

    public void setColMessageManager(ColMessageManager colMessageManager) {
        this.colMessageManager = colMessageManager;
    }

  

	public QuartzJob getAffairIsOvertopTimeJob() {
        return affairIsOvertopTimeJob;
    }

    public void setAffairIsOvertopTimeJob(QuartzJob affairIsOvertopTimeJob) {
        this.affairIsOvertopTimeJob = affairIsOvertopTimeJob;
    }

    public void setSignetManager(SignetManager signetManager) {
		this.signetManager = signetManager;
	}

	public void setCtpSignetManager(CtpSignetManager ctpSignetManager) {
		this.ctpSignetManager = ctpSignetManager;
	}

	public TemplateManager getTemplateManager() {
		return templateManager;
	}

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public OrgManager getOrgManager() {
		return orgManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public void setColPubManager(ColPubManager colPubManager) {
		this.colPubManager = colPubManager;
	}
	
	public void setColManager(ColManager colManager) {
		this.colManager = colManager;
	}

	/**
	 * 人员单位调动的时候 把签名带到新的单位
	 * @param evt
	 */
	@ListenEvent(event = MemberAccountChangeEvent.class)
	public void onAddAccount(MemberAccountChangeEvent evt) {
		Long newAccountId = evt.getAccount();
		Long oldAccountId = evt.getOldAccount();
		V3xOrgMember member = evt.getMember();
		List<V3xSignet> signets;
		try {
			signets = signetManager.findAllAccountID(oldAccountId);
			for(V3xSignet signet:signets){
				if(member.getId().toString().equals(signet.getUserName())){
					if(Integer.valueOf(0).equals(signet.getMarkType())){//签名需要带到新单位去
						signet.setOrgAccountId(newAccountId);
					}else if(Integer.valueOf(1).equals(signet.getMarkType())){//签章授权取消
						signet.setUserName("");
					}
					ctpSignetManager.update(signet);
				}
			}
		} catch (Exception e) {
			log.error("人员签名调出报错：", e);
		}
	}
	
	@ListenEvent(event = CollaborationAutoSkipEvent.class,async = true)
	public void transOnAutoSkip(CollaborationAutoSkipEvent evt){
		final CollaborationAutoSkipEvent event = evt;
    	Queue<Map<String, String>> queues  = event.getQueues();
		while(!queues.isEmpty()){
			Map<String, String> map = queues.poll();
			if(map != null){
				try {
					Thread.sleep(5000); //5秒延迟，使意见区的意见有序
				} catch (InterruptedException e) {
					log.error("", e);
				}
				affairIsOvertopTimeJob.execute(map);
				DBAgent.commit();
			}
		}
          
	}
	
	@ListenEvent(event = CollaborationAffairAssignedMsgEvent.class,async = true)
    public void transSendAffairAssignedMsg(CollaborationAffairAssignedMsgEvent evt) {
        AffairData af = evt.getAffairData();
        List<MessageReceiver> receivers = evt.getReceivers();
        List<MessageReceiver> receivers1 = evt.getReceivers1();
        Date d = evt.getReceiveTime();
        String subject=af.getSubject();
        try {
            colMessageManager.sendMessage(af, receivers, receivers1, d);
        } catch (BusinessException e) {
            log.error("", e);
        }
    }
	
    @ListenEvent(event = FlowFormSaveAllEvent.class)
    public void saveFlowTemplate2DB(FlowFormSaveAllEvent evt) throws BusinessException{
    	
        formTemplateDesignManager.saveFlowTemplate2DB(evt.getFormTemplateSaveAllParam());
        
    }
    
    @ListenEvent(event = CollaborationStartEvent.class,async = false)
    public void transCollaborationStart(CollaborationStartEvent event) {
    	//微服务模式在事件中进行归档操作
    	if(SystemEnvironment.isDistributedMode()) {
    		CtpAffair affair = event.getAffair();
    		ColSummary summary = event.getSummary();
    		SendType sendType = event.getSendtype();
    		try {
    			colPubManager.transPigeonhole(sendType, summary, affair);
    			colManager.updateColSummary(summary);
    		} catch (BusinessException e) {
    			log.error("", e);
    		}
    	}
    }
}
