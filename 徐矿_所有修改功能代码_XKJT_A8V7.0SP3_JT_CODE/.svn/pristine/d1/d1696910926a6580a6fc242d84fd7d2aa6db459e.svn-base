package com.seeyon.apps.collaboration.listener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.collaboration.event.CollaborationAutoSkipEvent;
import com.seeyon.apps.collaboration.manager.ColMessageManager;
import com.seeyon.apps.collaboration.quartz.IsOvertopTimeJob;
import com.seeyon.apps.system.signet.manager.CtpSignetManager;
import com.seeyon.apps.xkjt.manager.XkjtManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.event.MemberAccountChangeEvent;
import com.seeyon.ctp.organization.event.MoveDepartmentEvent;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.annotation.ListenEvent;
import com.seeyon.v3x.system.signet.domain.V3xSignet;
import com.seeyon.v3x.system.signet.manager.SignetManager;

public class CollaborationListener {
	private static Log log = LogFactory.getLog(CollaborationListener.class);
	private TemplateManager templateManager;
	private OrgManager orgManager;
	private SignetManager    signetManager;
    private CtpSignetManager ctpSignetManager;
    private IsOvertopTimeJob affairIsOvertopTimeJob;
    private ColMessageManager colMessageManager;
    
    
    public ColMessageManager getColMessageManager() {
        return colMessageManager;
    }

    public void setColMessageManager(ColMessageManager colMessageManager) {
        this.colMessageManager = colMessageManager;
    }

  

	public IsOvertopTimeJob getAffairIsOvertopTimeJob() {
        return affairIsOvertopTimeJob;
    }

    public void setAffairIsOvertopTimeJob(IsOvertopTimeJob affairIsOvertopTimeJob) {
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

	@ListenEvent(event = MoveDepartmentEvent.class)
	public void onMoveDepartmentEvent(MoveDepartmentEvent event) {
		V3xOrgDepartment oldDept = event.getOldDepartment();
		V3xOrgDepartment dept = event.getDepartment();
		// 删除模板分类授权
		if (!dept.getOrgAccountId().equals(oldDept.getOrgAccountId())) {
			try {
				List<V3xOrgMember> members = orgManager.getMembersByDepartment(oldDept.getId(), false);
				List<Long> ids = new ArrayList<Long>();
				for (V3xOrgMember m : members) {
					ids.add(m.getId());
				}
				templateManager.deleteCtpTemplatetAuths(ids,-1);
			} catch (BusinessException e) {
				log.error("", e);
			}
		}
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
		Thread t = new Thread(new Runnable(){  
            public void run(){  
            	Queue<Map<String, String>> queues  = event.getQueues();
        		while(!queues.isEmpty()){
        			Map<String, String> map = queues.poll();
        			if(map != null){
        				try {
        					Thread.sleep(5000); //3秒延迟，使意见区的意见有序
        				} catch (InterruptedException e) {
        					log.error("", e);
        				}
        				affairIsOvertopTimeJob.execute(map);
        				DBAgent.commit();
        			}
        		}
            }});  
        t.start();  
		
	}
	
	@ListenEvent(event = CollaborationAffairAssignedMsgEvent.class,async = true)
    public void transSendAffairAssignedMsg(CollaborationAffairAssignedMsgEvent evt) {
		AffairData af = evt.getAffairData();
        List<MessageReceiver> receivers = evt.getReceivers();
        List<MessageReceiver> receivers1 = evt.getReceivers1();
        Date d = evt.getReceiveTime();
        try {
            colMessageManager.sendMessage(af, receivers, receivers1, d);
        } catch (BusinessException e) {
            log.error("", e);
        }
    }
}
