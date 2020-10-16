package com.seeyon.v3x.edoc.listener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.event.CollaborationCancelEvent;
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
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.workflowmanage.vo.WorkflowData;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.dao.EdocSummaryDao;
import com.seeyon.v3x.edoc.manager.EdocManager;

public class EdocWorkflowManageHandler {
	private static final Log LOG = CtpLogFactory.getLog(EdocWorkflowManageHandler.class);
    public static final int SEND_CONDITION = 19; //发文
    public static final int REC_CONDITION = 20;  //收文
    public static final int SIGN_CONDITION = 21; //签报
    
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
    
    public AffairManager getAffairManager() {
		return affairManager;
	}


	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	public EdocManager getEdocManager() {
		return edocManager;
	}


	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}


	public SuperviseManager getSuperviseManager() {
		return superviseManager;
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

    public ModuleType getModuleType() {
        return ModuleType.edoc;
    }

    
    private Map getParamMap(String beginDate, String endDate, int flowstate,int condition,List<String> sendersList) {
        Map<String, Object> map = new HashMap<String, Object>();
        Date bDate = null;
        Date eDate = null;
        if(Strings.isNotBlank(beginDate)){
            bDate = Datetimes.getTodayFirstTime(beginDate);
        }
        if(Strings.isNotBlank(endDate)){
            eDate = Datetimes.getTodayLastTime(endDate);
        }
        
        int edocType = 0;
        if(condition == SEND_CONDITION){
            edocType = 0;
        }else if(condition == REC_CONDITION){
            edocType = 1;
        }else if(condition == SIGN_CONDITION){
            edocType = 2;
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
        
        map.put("bDate", bDate);
        map.put("eDate", eDate);
        map.put("flowstate", flowstate);
        map.put("edocType", edocType);
        map.put("senderMap", senderMap);
        return map;
        
    }
    
    
    /**
     * 流程管理查询（分页数据）
     * @throws com.seeyon.ctp.common.exceptions.BusinessException 
     */
    public FlipInfo selectPageWorkflowDataByCondition(String subject, String beginDate, String endDate, List<String> sendersList, 
            int flowstate, int condition, String operationType, String[] operationTypeIds, FlipInfo fi) throws BusinessException{
        long accountId = AppContext.getCurrentUser().getAccountId();
        Map map = getParamMap(beginDate, endDate, flowstate,condition,sendersList);
        Date bDate = (Date)map.get("bDate");
        Date eDate = (Date)map.get("eDate");
        flowstate = (Integer)map.get("flowstate");
        int edocType = (Integer)map.get("edocType");
        Map<String,List<Long>> senderMap = (Map<String,List<Long>>)map.get("senderMap");
        EdocSummaryDao dao = (EdocSummaryDao)AppContext.getBean("edocSummaryDao");
        List<WorkflowData> list = dao.selectWorkflowDataByCondition(fi,subject,bDate,eDate,senderMap,flowstate,edocType,operationType
                ,operationTypeIds,accountId,true,fi);
        fi.setData(list);
        return fi;
    }

    

    /**
     * 流程管理数据查询(全部数据，不分页)
     * @throws com.seeyon.ctp.common.exceptions.BusinessException 
     */
    public List<WorkflowData> selectWorkflowDataByCondition(String subject, String beginDate, String endDate, List<String> sendersList, 
            int flowstate, int condition, String operationType, String[] operationTypeIds) throws BusinessException{
        long accountId = AppContext.getCurrentUser().getAccountId();
        Map map = getParamMap(beginDate, endDate, flowstate,condition,sendersList);
        Date bDate = (Date)map.get("bDate");
        Date eDate = (Date)map.get("eDate");
        flowstate = (Integer)map.get("flowstate");
        int edocType = (Integer)map.get("edocType");
        Map<String,List<Long>> senderMap = (Map<String,List<Long>>)map.get("senderMap");
        EdocSummaryDao dao = (EdocSummaryDao)AppContext.getBean("edocSummaryDao");
        List<WorkflowData> list = dao.selectWorkflowDataByCondition(subject,bDate,eDate,senderMap,flowstate,edocType,operationType
                ,operationTypeIds,accountId,false,null);
        return list;
    }


    public String transRepal(Map<String, Object> map) throws BusinessException {
        long userId = AppContext.getCurrentUser().getId();
        String _affairId = (String) map.get("affairId");
        
        CtpAffair affair=affairManager.get(Long.valueOf(_affairId));
        long summaryId = affair.getObjectId();
        String repealComment = String.valueOf(map.get("repealComment"));
        
		if (map.get("appName").toString().contains("govdoc")) {// 新公文
			GovdocRepealVO vo = new GovdocRepealVO();
			vo.setSummaryIdStr(summaryId + "");
			vo.setAffairIdStr(_affairId);
			vo.setCommentContent(repealComment);
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


    public String transStepStop(Map<String, Object> map) throws BusinessException {
       EdocManager manager = (EdocManager)AppContext.getBean("edocManager");
    	 String _affairId = (String) map.get("affairId");
         AffairManager affairManager = (AffairManager)AppContext.getBean("affairManager");
         CtpAffair affair=affairManager.get(Long.valueOf(_affairId));
         long summaryId = affair.getObjectId();
        List<CtpAffair> affairList = affairManager.getAffairs(summaryId, StateEnum.col_pending);
        if(affairList!=null && affairList.size()>0){
            
           Map<String,Object> params = new HashMap<String,Object>();
           params.put("summaryId", summaryId);
           manager.transStepStop(affairList.get(0).getId(),params);
        }
        return null;
    }

}
