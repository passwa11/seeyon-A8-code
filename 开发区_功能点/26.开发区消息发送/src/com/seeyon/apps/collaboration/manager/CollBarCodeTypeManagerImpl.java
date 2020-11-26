package com.seeyon.apps.collaboration.manager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.enums.ColOpenFrom;
import com.seeyon.apps.collaboration.vo.ColListSimpleVO;
import com.seeyon.ctp.common.affair.bo.PendingRow;
import com.seeyon.ctp.common.affair.manager.PendingManager;
import com.seeyon.ctp.common.barCode.manager.BarCodeTypeManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.supervise.bo.SuperviseWebModel;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.supervise.vo.SuperviseModelVO;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;

/**
 * 二维码扫描
 * @author chenxd
 *
 */
public class CollBarCodeTypeManagerImpl implements BarCodeTypeManager{
	
	private static Log LOG = CtpLogFactory.getLog(CollBarCodeTypeManagerImpl.class);
	private CollaborationApi             collaborationApi;
	private SuperviseManager             superviseManager;
	private PendingManager               pendingManager;
	public CollaborationApi getCollaborationApi() {
		return collaborationApi;
	}

	public void setCollaborationApi(CollaborationApi collaborationApi) {
		this.collaborationApi = collaborationApi;
	}

	public SuperviseManager getSuperviseManager() {
		return superviseManager;
	}

	public void setSuperviseManager(SuperviseManager superviseManager) {
		this.superviseManager = superviseManager;
	}
	@SuppressWarnings("unchecked")
	@Override
	public Object decode(String codeStr, Map<String, Object> param) throws SQLException, BusinessException {

		Map<String, Object> retMap = new HashMap<String, Object>();
		Map<String, String> codeParam = null;
		try {
			codeParam = (Map<String, String>) JSONUtil.parseJSONString(codeStr);
		} catch (Exception e) {
			LOG.error("默认实现json解析二维码数据异常，直接返回结果值：" + codeStr, e);
			return codeStr;
		}
		if (codeParam.get("moduleId") == null) {
			LOG.error("解析二维码数据moduleId is null ：" + codeStr);
			return codeStr;
		}
		Map<String, String> queryMap = new HashMap<String, String>();
		

        /*if(Strings.isNotBlank(codeParam.get("version"))){
            //CAP4不支持扫描打开
            retMap.put("ErrMsg", ResourceUtil.getString("collaboration.erweima.notSuportOpen")*//* 暂不支持此类表单数据打开。 *//*);
            return retMap;
        }*/

		// 获取前台传递的状态(待办,已办...)
		String openFrom = (String) param.get("openFrom");
		String objectId = String.valueOf(codeParam.get("moduleId"));
		if (!"moreSupervise".equals(openFrom)) {
			for (Iterator<String> it = param.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				queryMap.put(key, (String) param.get(key));
			}
			queryMap.put("objectId", objectId);
		}

		Long affairId = null;

		FlipInfo fi = new FlipInfo();
		if (ColOpenFrom.listDone.name().equals(openFrom)) {// 已办
			FlipInfo fiInfo = collaborationApi.findDoneAffairs(fi, queryMap);
			affairId = getAffairId(fiInfo);
		}
		else if (ColOpenFrom.listPending.name().equals(openFrom)) {// 待办
			FlipInfo fiInfo = collaborationApi.findPendingAffairs(fi, queryMap);
			affairId = getAffairId(fiInfo);
		}
		else if (ColOpenFrom.listWaitSend.name().equals(openFrom)) {// 待发
			FlipInfo fiInfo = collaborationApi.findWaitSentAffairs(fi, queryMap);
			List list = fiInfo.getData();
	    	if(Strings.isNotEmpty(list)){
	    		ColListSimpleVO vo = (ColListSimpleVO)list.get(0);
	    		affairId = vo.getAffairId();
	    		/*OA-94134 流程表单二维码扫一扫URL-待发协同打开不直接进入编辑页面*/
				retMap.put("summaryId", vo.getSummaryId());
				retMap.put("subState", vo.getSubState());
	    	}
		}
		else if (ColOpenFrom.listSent.name().equals(openFrom)) {// 已发
			FlipInfo fiInfo = collaborationApi.findSentAffairs(fi, queryMap);
			affairId = getAffairId(fiInfo);
		}
		else if (ColOpenFrom.supervise.name().equals(openFrom)) {// 督办
			FlipInfo fiInfo = superviseManager.getSuperviseList4App(fi, queryMap);
			List list = fiInfo.getData();
			if (Strings.isNotEmpty(list)) {
				SuperviseWebModel vo = (SuperviseWebModel) list.get(0);
				affairId = vo.getAffairId();
			}
		}
		else if ("moreDone".equals(openFrom)) {// 栏目已办
			FlipInfo fiInfo = pendingManager.getMoreList4SectionContion(fi, queryMap);
			affairId = getAffairId4PendingRow(fiInfo);
			openFrom = "listDone";
		}
		else if ("morePending".equals(openFrom)) {// 栏目待办
			FlipInfo fiInfo = pendingManager.getMoreList4SectionContion(fi, queryMap);
			affairId = getAffairId4PendingRow(fiInfo);
			openFrom = "listPending";
		}
		else if ("moreWaitSend".equals(openFrom)) {// 栏目待发
			FlipInfo fiInfo = pendingManager.getMoreList4SectionContion(fi, queryMap);
			List list = fiInfo.getData();
	    	if(Strings.isNotEmpty(list)){
	    		PendingRow vo = (PendingRow)list.get(0);
	    		affairId = vo.getId();
	    		retMap.put("summaryId", vo.getObjectId());
				retMap.put("subState", vo.getSubState());
	    	}
			openFrom = "listWaitSend";
		}
		else if ("moreSent".equals(openFrom)) {// 栏目已发
			FlipInfo fiInfo = pendingManager.getMoreList4SectionContion(fi, queryMap);
			affairId = getAffairId4PendingRow(fiInfo);
			openFrom = "listSent";
		}
		else if ("moreSupervise".equals(openFrom)) {// 栏目督办
			Map<String, List<Object>> supMap = new HashMap<String, List<Object>>();
			if (param != null) {
				for (Iterator<String> it = param.keySet().iterator(); it.hasNext();) {
					String key1 = it.next();
					if ("openFrom".equals(key1)) {
						continue;
					}
					supMap.put(key1, (List<Object>) param.get(key1));
				}
				List<Object> obj = new ArrayList<Object>();
				obj.add(objectId);
				supMap.put("objectId", obj);
			}
			FlipInfo fiInfo = superviseManager.getSuperviseList4Portal(fi, supMap);
			List list = fiInfo.getData();
			if (Strings.isNotEmpty(list)) {
				SuperviseModelVO vo = (SuperviseModelVO) list.get(0);
				affairId = vo.getAffairId();
			}
			openFrom = "supervise";
		}
		else if ("moreTrack".equals(openFrom)) {// 栏目跟踪
			FlipInfo fiInfo = pendingManager.getMoreList4SectionContion(fi, queryMap);
			affairId = getAffairId4PendingRow(fiInfo);
			openFrom = "listSent";
		}

		if (affairId != null) {
			retMap.put("affairId", affairId);
			retMap.put("openFrom", openFrom);
		}
		return retMap;
	}
	
	public Long getAffairId(FlipInfo fiInfo){
		List list = fiInfo.getData();
		Long affairId = null;
    	if(Strings.isNotEmpty(list)){
    		ColListSimpleVO vo = (ColListSimpleVO)list.get(0);
    		affairId = vo.getAffairId();
    	}
		return affairId;
	}
	
	public Long getAffairId4PendingRow(FlipInfo fiInfo){
		List list = fiInfo.getData();
		Long affairId = null;
    	if(Strings.isNotEmpty(list)){
    		PendingRow vo = (PendingRow)list.get(0);
    		affairId = vo.getId();
    	}
		return affairId;
	}
	
	@Override
	public String getContentStr(Map<String, Object> param) {
		return JSONUtil.toJSONString(param);
	}

	/**
	 * 当生成的二维码内容超过长度之后，生成自己想要的特殊二维码
	 *
	 * @param param
	 * @return
	 */
	@Override
	public String getContent4OutOfLength(Map<String, Object> param) {
		return "";
	}

	@Override
	public String getType() {
		return "codeflowurl";
	}

	public PendingManager getPendingManager() {
		return pendingManager;
	}

	public void setPendingManager(PendingManager pendingManager) {
		this.pendingManager = pendingManager;
	}

	

}
