package com.seeyon.v3x.edoc.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.supervise.vo.SuperviseSetVO;
import com.seeyon.ctp.common.usermessage.MessageReceiver;

public class EdocSuperviseHelper {
	
	public static Object[] getSupervisorIdsBySummaryId(long summaryId) throws BusinessException{
		
		SuperviseManager edocSuperviseManager= (SuperviseManager)AppContext.getBean("superviseManager");
		
     	CtpSuperviseDetail detail = edocSuperviseManager.getSupervise(summaryId);
     	if(null!=detail){
			
    		List<CtpSupervisor> set = edocSuperviseManager.getSupervisors(detail.getId());
    		
    		StringBuffer superviseIds = new StringBuffer("");
    		
    		for(CtpSupervisor sor: set){
    			superviseIds.append(sor.getSupervisorId());
    			superviseIds.append(",");
    		}
    		
    		if(superviseIds.toString().endsWith(",")){
    			superviseIds.deleteCharAt(superviseIds.length() - 1);
    		}
    		
    		Date endDate = detail.getAwakeDate();
    		Object[] objcet = new Object[2];
    		objcet[0] = superviseIds.toString();
    		objcet[1] = endDate;
    		return objcet;
    		}else{
    			return null;
    		}
	}
	
	public static List<MessageReceiver> getRecieverBySummaryId(Long summaryId) throws BusinessException{
	    
	    SuperviseManager edocSuperviseManager= (SuperviseManager)AppContext.getBean("superviseManager");
	    
		CtpSuperviseDetail detail = edocSuperviseManager.getSupervise(summaryId);
		if(null!=detail){
		    List<CtpSupervisor> set = edocSuperviseManager.getSupervisors(detail.getId());
			List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();			
			for(CtpSupervisor sor: set){
				receivers.add(new MessageReceiver(detail.getId(), sor.getSupervisorId()));
			}
			return receivers;
			
		}else{
			return null;
		}
	}
	

	public static void saveSuperviseForPersonalTemplate(HttpServletRequest request,String subject,long templateId,boolean isNew,int state,SuperviseManager superviseManager) throws BusinessException {
		String supervisorId = request.getParameter("supervisorId");
        String supervisors = request.getParameter("supervisors");
        String awakeDate = request.getParameter("awakeDate");
        
        //TODO 这里应该有逻辑问题，修改的时候删除了督办信息
        if(supervisorId != null && !"".equals(supervisorId) && awakeDate != null && !"".equals(awakeDate)) {
	        String superviseTitle = request.getParameter("superviseTitle");
	        Long detailId = null;
	        if(!isNew){
	            CtpSuperviseDetail d = superviseManager.getSupervise(templateId);
	            if(d != null){
	                detailId = d.getId();
	            }
	        }
	        
	        SuperviseSetVO sVO = new SuperviseSetVO();
            //sVO.setTemplateDateTerminal();
            sVO.setAwakeDate(awakeDate);
            sVO.setDetailId(detailId);
            //sVO.setRole(role);
            sVO.setSupervisorIds(supervisorId);
            sVO.setSupervisorNames(supervisors);
            sVO.setTitle(superviseTitle);
            sVO.setUnCancelledVisor(null);
            
            superviseManager.saveOrUpdateSupervise4Template(templateId, sVO);
        }
	}
}
