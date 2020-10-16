package com.seeyon.apps.govdoc.manager.external;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.event.CollaborationCancelEvent;
import com.seeyon.apps.govdoc.manager.GovdocListManager;
import com.seeyon.apps.govdoc.manager.GovdocManager;
import com.seeyon.apps.govdoc.vo.GovdocRepealVO;
import com.seeyon.apps.index.api.IndexApi;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.workflowmanage.handler.WorkflowManageHandler;
import com.seeyon.ctp.common.workflowmanage.vo.WorkflowData;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.listener.EdocWorkflowManageHandler;
import com.seeyon.v3x.edoc.manager.EdocManager;

public class GovdocWorkflowManageHandler implements WorkflowManageHandler {
	
	private static final Log LOG = CtpLogFactory.getLog(EdocWorkflowManageHandler.class);
    
	public static final int SEND_CONDITION = 19; //发文
    public static final int REC_CONDITION = 20;  //收文
    public static final int SIGN_CONDITION = 21; //签报
    public static final int GOVSEND_CONDITION = 401; //新公文，发文
    public static final int GOVREC_CONDITION = 402;  //新公文，收文
    public static final int GOVSIGN_CONDITION = 404; //新公文，签报
    
    public static final int RUN = 0;
    public static final int STOP = 1;
    public static final int CANCEL = 2;
    public static final int FINISH = 3;
    
    public static final String COMMON_WORKFLOW = "self";   //自由流程
    public static final String TEMPLETE_WORKFLOW = "template"; //模板流程
    
    private AffairManager affairManager = null;
    private EdocManager edocManager = null;
	private SuperviseManager superviseManager = null;
    private IndexApi indexApi = null;
    private GovdocManager govdocManager = null;
    private GovdocListManager govdocListManager = null;
    private PermissionManager permissionManager;
    
    
	public PermissionManager getPermissionManager() {
		return permissionManager;
	}

	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}

	@Override
    public ModuleType getModuleType() {
        return ModuleType.edoc;
    }
    
    /**
     * 流程管理查询（分页数据）
     * @throws com.seeyon.ctp.common.exceptions.BusinessException 
     */
    public FlipInfo selectPageWorkflowDataByCondition(Map<String,Object> conditionParam, FlipInfo fi) throws BusinessException{
        long accountId = AppContext.getCurrentUser().getAccountId();
        Map<String,Object> map = getParamMap(conditionParam);

        List<WorkflowData> list = govdocListManager.getAdminWfDataList(fi,map,accountId,true,fi);
        
        String con = (String)conditionParam.get("condition");
        int condition = ApplicationCategoryEnum.edocSend.key();
        if(Strings.isNotBlank(con)){
            condition = Integer.parseInt(con);
        }
        PermissionVO vo = null;
        if(Strings.isNotEmpty(list)){
        	for(WorkflowData data : list){
        		vo = permissionManager.getDefaultPermissionByApp(ApplicationCategoryEnum.valueOf(condition), data.getAccountId());
        		if(vo != null){
            		data.setDefaultNodeName(vo.getName());
            		data.setDefaultNodeLable(vo.getLabel());
        		}
        	}
        }
        fi.setData(list);
        return fi;
    }

    /**
     * 流程管理数据查询(全部数据，不分页)
     * @throws com.seeyon.ctp.common.exceptions.BusinessException 
     */
    public List<WorkflowData> selectWorkflowDataByCondition(Map<String,Object> conditionParam) throws BusinessException{
        long accountId = AppContext.getCurrentUser().getAccountId();
        Map<String,Object> map = getParamMap(conditionParam);

        Map<String,List<Long>> senderMap = (Map<String,List<Long>>)map.get("senderMap");
        List<WorkflowData> list = govdocListManager.getAdminWfDataList(null, map,accountId,false,null);
        return list;
    }

    @Override
    public String transRepal(Map<String, Object> map) throws BusinessException {
        long userId = AppContext.getCurrentUser().getId();
        String _affairId = (String) map.get("affairId");
        
        CtpAffair affair=affairManager.get(Long.valueOf(_affairId));
        long summaryId = affair.getObjectId();
        String repealComment = String.valueOf(map.get("repealComment"));
        
		if (affair.getSubApp()<=4) {// 新公文
			GovdocRepealVO vo = new GovdocRepealVO();
			vo.setSummaryIdStr(summaryId + "");
			vo.setAffairIdStr(_affairId);
			vo.setRepealComment(repealComment);
			govdocManager.transRepal(vo);
			return null;
		}
        int result =  edocManager.cancelSummary(userId,summaryId,affair,repealComment,"cancelColl",null);
	       
        if(result == 0){
        	
        	try {
        		// 已发撤销后，需要删除已经发出去的全文检索文件
        		if (AppContext.hasPlugin("index")) {
        		    indexApi.delete(affair.getObjectId(), ApplicationCategoryEnum.edoc.getKey());
        		}
        	} catch (Exception e) {
        		LOG.error("撤销公文流程，更新全文检索异常", e);
        	}
        	// 撤销流程事件
        	CollaborationCancelEvent event = new CollaborationCancelEvent(this);
        	event.setSummaryId(affair.getObjectId());
        	event.setUserId(userId);
        	event.setMessage(repealComment);
        	EventDispatcher.fireEvent(event);
        	// 发送消息给督办人，更新督办状态，并删除督办日志、删除督办记录、删除催办次数
        	
        	superviseManager.updateStatus2Cancel(summaryId);
        }
        return null;
    }

    @Override
    public String transStepStop(Map<String, Object> map) throws BusinessException {
    	 String _affairId = (String) map.get("affairId");
         AffairManager affairManager = (AffairManager)AppContext.getBean("affairManager");
         CtpAffair affair=affairManager.get(Long.valueOf(_affairId));
         long summaryId = affair.getObjectId();
        List<CtpAffair> affairList = affairManager.getAffairs(summaryId, StateEnum.col_pending);
        if(affairList!=null && affairList.size()>0) {
           Map<String,Object> params = new HashMap<String,Object>();
           params.put("summaryId", summaryId);
           edocManager.transStepStop(affairList.get(0).getId(),params);
        }
        return null;
    }

    private Map getParamMap(Map<String,Object> conditionParam) {
        Date bDate = null;
        Date eDate = null;
        String beginDate = (String) conditionParam.get("beginDate");
        String endDate = (String) conditionParam.get("endDate");
        if(Strings.isNotBlank(beginDate)){
            bDate = Datetimes.getTodayFirstTime(beginDate);
        }
        if(Strings.isNotBlank(endDate)){
            eDate = Datetimes.getTodayLastTime(endDate);
        }
        String con = (String)conditionParam.get("condition");
        int condition = ApplicationCategoryEnum.collaboration.key();
        if(Strings.isNotBlank(con)){
            condition = Integer.parseInt(con);
        }
        int edocType = 0;
        if(condition == SEND_CONDITION || condition == GOVSEND_CONDITION){
            edocType = 0;
        }else if(condition == REC_CONDITION || condition == GOVREC_CONDITION){
            edocType = 1;
        }else if(condition == SIGN_CONDITION || condition == GOVSIGN_CONDITION){
            edocType = 2;
        }
        String sendersStr = (String) conditionParam.get("senders");
        String[] sendersArr = sendersStr.split(",");
        List<String> sendersList = new ArrayList<String>();
        if(sendersArr != null && sendersArr.length>0){
            for(int i=0; i<sendersArr.length; i++){
                if(!"".equals(sendersArr[i])){
                    sendersList.add(sendersArr[i]);
                }
            }
        }
        //将发起对象，按照各自的类别分别存放到List中，因为选人时，可以混选单位，部门，人员
        Map<String,List<Long>> senderMap = new HashMap<String,List<Long>>();
        for(int i=0;i<sendersList.size();i++){
            String sender = sendersList.get(i);
            String[] senders = sender.split("[|]");
            String type = senders[0];
            long id = Long.parseLong(senders[1]);
            if(senderMap.get(type)==null){
                List<Long> senderList = new ArrayList<Long>();
                senderList.add(id);
                senderMap.put(type, senderList);
            }else{
                List<Long> senderList = senderMap.get(type);
                senderList.add(id);
                senderMap.put(type, senderList);
            }
        }
        
        conditionParam.put("bDate", bDate);
        conditionParam.put("eDate", eDate);
        conditionParam.put("edocType", edocType);
        conditionParam.put("senderMap", senderMap);
        return conditionParam;   
    }
    
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}
	public void setSuperviseManager(SuperviseManager superviseManager) {
		this.superviseManager = superviseManager;
	}
	public void setIndexApi(IndexApi indexApi) {
        this.indexApi = indexApi;
    }
	public void setGovdocManager(GovdocManager govdocManager) {
		this.govdocManager = govdocManager;
	}
	public void setGovdocListManager(GovdocListManager govdocListManager) {
		this.govdocListManager = govdocListManager;
	}

	@Override
	public String validateReliveProcess(Map<String, Object> map) throws BusinessException {
		return null;
	}
	
	public String getMyManagerTemplateCount() throws BusinessException{
	    return "0";
	}

}
